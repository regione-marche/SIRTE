package it.caribel.app.sinssnt.bean.nuovi;
/**

* CARIBEL S.r.l. - SINSS: produzione elenco assistiti 
*13/06/2016 -  Minerba
*/

import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Hashtable;

public class FoAltroEleAssEJB extends SINSSNTConnectionEJB {

public FoAltroEleAssEJB() {}

String ragg="";
String dom_res;
String dr;
boolean inMolise = false;
//hash per calcolare il numero totale di assistiti
Hashtable hContaGeneraleS= new Hashtable();
Hashtable hContaGeneraleA= new Hashtable();
Hashtable hContaAssistitiA= new Hashtable();
int totaleInf =0;

	it.pisa.caribel.util.ISASUtil utl = new it.pisa.caribel.util.ISASUtil();
	private String MIONOME = "11-FoAltroEleAssEJB. ";
	private static final String CONSTANTS_CODICE_MOLISE ="6"; // codice ubicazione per il molise 
	private static final String CONSTANTS_UBICAZIONE = "ADRSA_UBIC"; // indicatore di ubicazione


/**
* restituisce un parametro data come stringa nel formato gg/mm/aaaa
*/
private String getStringDate(Hashtable par, String k) {
	try {
		String s = (String)par.get(k);
		s = s.substring(8,10)+"/"+s.substring(5,7)+"/"+s.substring(0,4);
		return s;
	} catch(Exception e) {
		debugMessage("getStringDate("+par+", "+k+"): "+e);
		return "";
	}
}

/**
* restituisce un campo data come stringa
*/
private String getDateField(ISASRecord dbr, String f) {
	try {
		if(dbr.get(f)==null)
			return "";
		String d = ((java.sql.Date)dbr.get(f)).toString();
		d = d.substring(8,10)+"/"+d.substring(5,7)+"/"+d.substring(0,4);
		return d;
	} catch(Exception e) {
		debugMessage("getStringField("+dbr+", "+f+"): "+e);
		return "";
	}
}

private String getComune(ISASConnection dbc, String codice) throws Exception {
String comune="";
try{
	if(!codice.equals("")){
		String sel = "SELECT descrizione FROM comuni "+
			"WHERE codice = '"+codice+"'";
		//debugMessage("FoEleSocEJB.getRagioneSociale(): "+sel);
		ISASRecord dbcom = dbc.readRecord(sel);
		comune=(String)dbcom.get("descrizione");
	     }
} catch(Exception e) {
	debugMessage("getComune("+dbc+", "+codice+"): "+e);
	return "";
}
   return comune;
}

private String getMotivoUscita(ISASRecord dbr,ISASConnection dbc)
throws SQLException{
        String decod="";
        try {
              if(dbr.get("ski_dimissioni")!=null && !(""+ dbr.get("ski_dimissioni")).equals(""))
              {
                 String codice=""+ dbr.get("ski_dimissioni");
                 String sel="SELECT tab_descrizione FROM tab_voci "+
                            " WHERE tab_cod='ICHIUS' AND tab_val='"+codice +"'";
                 //System.out.println("MOTIVO CHIUSURA-->"+sel);
                 ISASRecord dbDecod=dbc.readRecord(sel);
                 if (dbDecod!=null && dbDecod.get("tab_descrizione")!=null)
                 {
                     decod=(String)dbDecod.get("tab_descrizione");
                     if(decod.trim().equals("."))decod="";
                 }
              }
          return decod;
	} catch(Exception e) {
		debugMessage("FoAltroEleAssEJB.getMotivoUscita(): "+e);
		throw new SQLException("Errore eseguendo getMotivoUscita()");
	}
}
/**
* restituisce la select per la stampa sintetica.
*/

private String getSelectSintetica(ISASConnection dbc, Hashtable par) {
	String punto = MIONOME + "getSelectSintetica ";
	
	String s = "SELECT DISTINCT c.n_cartella, "+
                "c.cognome,c.nome,c.data_nasc,"+
                "u.cod_zona,"+"u.cod_distretto"+" as cod_distretto,"+
                "u.des_zona descrizione_zona ,";
        // if (par.get("oper")!=null && par.get("oper").equals("SI"))
		if (par.get("oper")!=null)
         s=s+" op.cognome cognome_oper,op.nome nome_oper, op.dipend_conv, ";//29-09-2003 aggiunta divisione per operatore

         s=s+"u.codice,u.descrizione,u.des_distretto des_distr"+
                " FROM cartella c,anagra_c a,skfpg skf,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";

        
         if (par.get("oper")!=null)
             s=s+",operatori op";

        try {
		String w = getSelectParteWhereSint(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
		if (inMolise){
			LOG.debug(punto + " sono in molise ");
			s += aggiungiCondizioneMotivo(par);
		}
	} catch(Exception e) {
		debugMessage("FoAltroEleAssEJB.getSelectSintetica(): "+e);
		e.printStackTrace();
	}
        s = s +" ORDER BY ";
        //29-09-2003 aggiunta divisione per operatore
        if (par.get("oper")!=null && par.get("oper").equals("SI"))
        if (par.get("oper")!=null)
           s = s + " op.cognome,op.nome, ";

	s = s + " descrizione_zona,des_distr,descrizione,"+
			"c.cognome,c.nome,c.n_cartella";
	LOG.debug(punto + "Query>"+s);
	return s;
}

private String aggiungiCondizioneMotivo(Hashtable par) {
	String punto = MIONOME + "aggiungiCondizioneMotivo ";
	String motivo = ISASUtil.getValoreStringa(par, "motivo");
	String condizione = "";
	if (ISASUtil.valida(motivo)&& !motivo.equals("0")){
		condizione= " and skf_motivo = '"+motivo+ "' ";
		LOG.debug(punto + " aggiungo motivo ");
	}
	
	return condizione;
}



private boolean recuperaConfUbicazione(ISASConnection dbc) {
	boolean inMolise = false;
	
	String punto = MIONOME + "recuperaConfUbicazione ";
	String query = "SELECT * FROM conf  WHERE conf_kproc = 'SINS' AND conf_key = '" +CONSTANTS_UBICAZIONE+"'";
	ISASRecord dbrConf = null;
	try {
		dbrConf = dbc.readRecord(query);
	} catch (Exception e) {
		e.printStackTrace();
		LOG.error(punto + " Errore nel recuperare la configurazione del molise>"+ query+"<");
	}
	String sonoInMolise = ISASUtil.getValoreStringa(dbrConf, "conf_txt");
	
	inMolise = (ISASUtil.valida(sonoInMolise)&& sonoInMolise.equals(CONSTANTS_CODICE_MOLISE));
	LOG.debug(punto + " query>"+query + "<sonoInMolise>" +sonoInMolise+"< \nesito inMolise>"+inMolise+"<");
	return inMolise;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/


private String getSelectParteWhereSint(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();

	String myselect="";
        String data_fine="";
        //controllo data fine
		if (par.get("data_fine") != null)
		{
		  data_fine=(String)(par.get("data_fine"));
		  myselect =myselect+" skf.skfpg_data_apertura<="+formatDate(dbc,data_fine);
        }

        //controllo data inizio
	if (par.get("data_inizio") != null)
        {
		  String scr=(String)(par.get("data_inizio"));
                  myselect =myselect+" AND (skf.skfpg_data_uscita is null OR skf.skfpg_data_uscita>="+formatDate(dbc,scr)+")";
        }
	String livello=(String)(par.get("motivo"));	
    if (livello!=null && !livello.equals("-1"))
    	myselect=myselect+" AND skf.skfpg_motivo='"+livello+"'";
   
        if (par.get("oper")!=null)
        myselect =myselect+" AND skf.skfpg_operatore = op.codice ";

	
        String condWhere = getFiltroUbicazione(par, su);
        myselect=myselect+ " AND " + condWhere;
        
       myselect += " AND a.n_cartella=c.n_cartella"+
         " AND skf.n_cartella=a.n_cartella"+
         " AND skf.n_cartella=c.n_cartella"+
         " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"+
	 " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";

	return myselect;
}

public String getFiltroUbicazione(Hashtable<String, String> par, ServerUtility su) {

	String condWhere = "";
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));
	String raggruppamento = (String) par.get("ragg");
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, raggruppamento);
	String dom_res = ISASUtil.getValoreStringa(par, "dom_res");
	if (!ISASUtil.valida(dom_res)) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
					+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"
					+ " AND u.codice=a.dom_areadis)" + " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "
					+ " AND u.codice=a.areadis))";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = skf.skfpg_cod_presidio ";
		}
	} else if (dom_res.equals("D")) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND u.codice=a.dom_citta";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND u.codice=a.dom_areadis";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = skf.skfpg_cod_presidio ";
		}
	} else if (dom_res.equals("R")) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND u.codice=a.citta";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND u.codice=a.areadis";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = skf.skfpg_cod_presidio ";
		}
	}
	return condWhere;
}
/**
* restituisce la select per la stampa analitica.
*/
private String getSelectAnalitica(ISASConnection dbc, Hashtable par) {
	String punto = MIONOME + "getSelectAnalitica ";
	boolean inMolise = recuperaConfUbicazione(dbc);	

        String s ="SELECT c.n_cartella, "+
                "c.cognome, c.nome, c.data_nasc," +
               // ",ski.ski_tipocura,"+
				"a.dom_citta, a.dom_indiriz, a.indirizzo, a.citta,a.nome_camp, a.telefono1, "+
				"a.cod_med, " + // 07/06/11
				"skf.skfpg_data_apertura, skf.skfpg_data_uscita,"+
				"skf.skfpg_operatore, ";
                   s=s+"op.cognome cognome_oper,op.nome nome_oper, op.dipend_conv,";//29-09-2003 aggiunta divisione per operatore
                s=s+"u.cod_zona,"+"u.cod_distretto"+" as cod_distretto,"+
                "u.des_zona descrizione_zona ,"+
                "u.codice,u.descrizione,u.des_distretto des_distr"+
                " FROM cartella c, anagra_c a, skfpg skf, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";				
                
                  s=s+", operatori op ";
	try {
		String w = getSelectParteWhereAna(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
		if (inMolise){
				LOG.debug(punto + " sono in molise ");
				s += aggiungiCondizioneMotivo(par);
		}
		
	} catch(Exception e) {
		debugMessage("FoAltroEleAssEJB.getSelectAnalitica(): "+e);
		e.printStackTrace();
	}
        s = s +" ORDER BY ";
        //29-09-2003 aggiunta divisione per operatore
        if (par.get("oper")!=null && par.get("oper").equals("SI"))
        //{
           s = s + " op.cognome,op.nome, ";
	//}
	s = s +  " descrizione_zona,des_distr,descrizione,"+
			"c.cognome,c.nome,c.n_cartella";
	debugMessage("FoAltroEleAssEJB.getSelectAnalitica(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhereAna(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();

	String myselect="";
	String data_fine="";
        //controllo data fine
		if (par.get("data_fine") != null)
		{
		  data_fine=(String)(par.get("data_fine"));
		  myselect =myselect+" skf.skfpg_data_apertura<="+formatDate(dbc,data_fine);
        }

        //controllo data inizio
	if (par.get("data_inizio") != null)
        {
		  String scr=(String)(par.get("data_inizio"));
                  myselect =myselect+" AND (skf.skfpg_data_uscita is null OR skf.skfpg_data_uscita>="+formatDate(dbc,scr)+")";
        }
	
	String livello=(String)(par.get("motivo"));
    if (livello!=null && !livello.equals("-1"))
 	   myselect=myselect+" AND skf.skfpg_motivo='"+livello+"'";
        
        //29-09-2003 aggiunta divisione per operatore
        //if (par.get("oper")!=null && par.get("oper").equals("SI"))
        myselect =myselect+" AND skf.skfpg_operatore = op.codice ";

	
        String condWhere = getFiltroUbicazione(par, su);
        myselect=myselect+ " AND " + condWhere;
       
       myselect += " AND a.n_cartella=c.n_cartella"+
// 07/06/11: la join su MEDICI provoca lo scarto di numerosi record         " AND mecodi=a.cod_med"+
         " AND skf.n_cartella=a.n_cartella"+
         " AND skf.n_cartella=c.n_cartella"+
         " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"+
	 " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";

	return myselect;
}

/**
* stampa sintetica-analitica: sezione layout del documento
*/
private void mkLayout(ISASConnection dbc,
	Hashtable par, mergeDocument doc, String ass) {

	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();

	ht.put("#txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));
	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
    String assistenza = "";
		
		if (inMolise){
			String descrMotivo = "";
			String motivo = ISASUtil.getValoreStringa(par, "motivo");
			if (ISASUtil.valida(motivo)){
				descrMotivo = recuperaDescrizioneMotivo(dbc, motivo);
				if (ISASUtil.valida(descrMotivo)){
					descrMotivo = " Motivo: "+ descrMotivo;
				}
			}
			assistenza +=(ISASUtil.valida(assistenza)? " - ":"")+descrMotivo;  
		}
		ht.put("#assistenza#", assistenza);
        
        
	doc.writeSostituisci("layout",ht);
}

private String recuperaDescrizioneMotivo(ISASConnection dbc, String motivo) {
	String punto = MIONOME + "recuperaDescrizioneMotivo ";
	String descrizioneMotivo = "";
	String query = "select * from motivo_s where codice = '" +motivo +"' ";
	
	try {
		ISASRecord dbrMotivos = dbc.readRecord(query);
		descrizioneMotivo = ISASUtil.getValoreStringa(dbrMotivos, "descrizione");
	} catch (Exception e) {
		e.printStackTrace();
		LOG.error(punto + " Erorre nel recuperare la descrizione del motivo con Query>"+query);
	}
	LOG.debug(punto + "query>"+query +" descrzione>"+descrizioneMotivo+"<");
	 
	return descrizioneMotivo;
}

/**
* stampa sintetica: sezione coordinate geografiche
*/
private void mkCoordinate(mergeDocument doc,Hashtable par,
	String cur_zona, String cur_dist, String cur_comu) {

	Hashtable ht = new Hashtable();
	ht.put("#descrizione_zona#", cur_zona);
	ht.put("#des_distr#", cur_dist);

	String ragg = faiRaggruppamento((String)par.get("ragg"));
	ht.put("#tipologia#", ragg);
	if (this.dom_res!=null)ht.put("#dom_res#", this.dr);
	else ht.put("#dom_res#", "(Domicilio)");
	String ragg1 =(String)par.get("ragg");
	if (ragg1.equals("P"))
		ht.put("#dom_res#", "");
	ht.put("#descrizione#", cur_comu);
	doc.writeSostituisci("zona", ht);
}
/**
* stampa sintetica: sezione nuovo operatore
* 29-09-2003 aggiunta divisione per operatore
*/
private void mkTaglioOperatore(mergeDocument doc,Hashtable par,
	String cur_oper) {

	Hashtable ht = new Hashtable();
        ht.put("#descrizione_operatore#", cur_oper);
	doc.writeSostituisci("operatore", ht);
}
/**
* stampa analitica: sezione totalemedico
* 29-09-2003 aggiunta divisione per operatore
*/
private void mkTotaleMed(mergeDocument doc) {
        Hashtable ht = new Hashtable();
	ht.put("#descrizioneTotMed#", " Totale n. assistiti: ");
//	ht.put("#totaleMed#", " "+tot);
        ht.put("#totaleMed#", " "+totaleInf);
        totaleInf=0;
	doc.writeSostituisci("totaleMed", ht);
}
/**
* stampa sintetica: sezione totalemedico
* 29-09-2003 aggiunta divisione per operatore
*/
private void mkTotaleMed(mergeDocument doc,int tot) {
        Hashtable ht = new Hashtable();
	ht.put("#descrizioneTotMed#", " Totale n. assistiti:");
	ht.put("#totaleMed#", " "+tot);
	doc.writeSostituisci("totaleMed", ht);
}
/**
* stampa sintetica: sezione pagina nuova
* 29-09-2003 aggiunta divisione per operatore
*/
private void mkPaginaNuova(mergeDocument doc) {
        doc.write("taglia");
}
/**
* stampa sintetica: sezione inizio tabella
*/
private void mkIniziaTabella(mergeDocument doc, ISASRecord dbr) {

	Hashtable h = new Hashtable();
	if (this.dom_res!=null)  h.put("#dom_res#",this.dr);
	else h.put("#dom_res#","(Domicilio)");
	doc.writeSostituisci("iniziotab",h);

}

/**
* stampa sintetica: sezione riga tabella
*/
private void mkSinteticaRigaTabella(ISASConnection dbc,
	ISASRecord dbr, mergeDocument doc) {

	Hashtable ht = new Hashtable();
	try {
		ht.put("#assistito#", (String)dbr.get("cognome")+" "+
			(String)dbr.get("nome"));
		ht.put("#data_nasc#", getDateField(dbr, "data_nasc"));
		if (dbr.get("n_cartella")==null)
			ht.put("#cartella#", " ");
		else
			ht.put("#cartella#",
				((Integer)dbr.get("n_cartella")).toString());

	hContaGeneraleS.put(((Integer)dbr.get("n_cartella")).toString(),"");
	} catch(Exception e) {
		ht.put("#assistito#", "*** ERRORE ***");
		ht.put("#data_nasc#"," ");
		ht.put("#cartella#", " ");
	}
	doc.writeSostituisci("tabella", ht);
}
/**
* stampa analitica: sezione fine tabella
*/
private void mkFineTabellaA(mergeDocument doc) {
	doc.write("finetab");

	Hashtable ht = new Hashtable();
	ht.put("#descrizione#", " Totale n. assistiti: ");
	//ht.put("#totale#", " "+conta);
        ht.put("#totale#", " "+hContaAssistitiA.size());
        totaleInf=totaleInf + hContaAssistitiA.size();
        hContaAssistitiA.clear();
	doc.writeSostituisci("totale", ht);////COMMENTATO IL 04-07-03
}
/**
* stampa sintetica: sezione fine tabella
*/
private void mkFineTabella(mergeDocument doc, int conta) {
	doc.write("finetab");

	Hashtable ht = new Hashtable();
	ht.put("#descrizione#", " Totale n. assistiti: ");
	ht.put("#totale#", " "+conta);
	doc.writeSostituisci("totale", ht);////COMMENTATO IL 04-07-03
}

/**
* stampa sintetica: corpo
*/
private void mkSinteticaBody(ISASConnection dbc, ISASCursor dbcur,
	Hashtable par, mergeDocument doc) throws Exception {
System.out.println("Ok qui");
	boolean first_time = true;
        //29-09-2003 aggiunta divisione per operatore
        String old_oper = "*", cur_oper = "";

	String old_zona = "*", cur_zona = "";
	String old_dist = "*", cur_dist = "";
	String old_comu = "*", cur_comu = "";
	int conta = 0;
	int contaTot = 0;
        int contaAssMed=0;
	try {
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
                        //29-09-2003 aggiunta divisione per operatore
                        if (par.get("oper")!=null && par.get("oper").equals("SI"))
                        {
                          String tipo_operatore=(String)dbr.get("dipend_conv");
                          String tipo_op="";
                          if (tipo_operatore!=null && !tipo_operatore.equals("")){
                        	  if (tipo_operatore.equals("D"))
                        		  tipo_op="Operatore dipendente";
                        	  else if (tipo_operatore.equals("C"))
                        		  tipo_op="Operatore convenzionato";
                          }  
                          cur_oper=(String)dbr.get("cognome_oper")+" "+(String)dbr.get("nome_oper") + " " + tipo_op;
                        }
			cur_zona = (String)dbr.get("descrizione_zona");
			cur_dist = (String)dbr.get("des_distr");
			cur_comu = (String)dbr.get("descrizione");
			if ((par.get("oper")!=null &&
                          par.get("oper").equals("SI")&&
                          !old_oper.equals(cur_oper))||
                          ((! old_zona.equals(cur_zona)) ||
				(! old_dist.equals(cur_dist)) ||
				(! old_comu.equals(cur_comu)))
			) {
				/*if (first_time) {
					first_time = ! first_time;
				} else {*/
                                if (!first_time) {
					// chiudi tabella precedente
					mkFineTabella(doc, conta);
					conta = 0;
				}
                                //29-09-2003 aggiunta divisione per operatore
				if (par.get("oper")!=null &&
                                     par.get("oper").equals("SI")&&
                                     !old_oper.equals(cur_oper))
                                {
                                       if (!first_time){
                                          //System.out.println("pagina nuova");
                                          mkTotaleMed(doc,contaAssMed);
                                          mkPaginaNuova(doc);
                                          contaAssMed=0;}
                                       mkTaglioOperatore(doc,par,cur_oper);
                                       // intestazione cambio coordinate
					mkCoordinate(doc,par,
						cur_zona, cur_dist, cur_comu);
                                }
				else if ((! old_zona.equals(cur_zona)) ||
					(! old_dist.equals(cur_dist)) ||
					(! old_comu.equals(cur_comu))
				) {
					// intestazione cambio coordinate
					mkCoordinate(doc,par,
						cur_zona, cur_dist, cur_comu);
				}
				// apri tabella con muovo operatore
				mkIniziaTabella(doc,dbr);
			}
                        // 29-09-2003 aggiunta divisione per operatore
			old_oper = cur_oper;
			old_zona = cur_zona;
			old_dist = cur_dist;
			old_comu = cur_comu;

			if (first_time) {
					first_time = ! first_time;
				}
                        // stampa riga
			mkSinteticaRigaTabella(dbc, dbr, doc);
			conta++;
                        contaAssMed++;
                        contaTot++;
		}
		if (!first_time) {
			// chiudi tabella precedente
			mkFineTabella(doc, conta);
                        if (par.get("oper")!=null &&
                          par.get("oper").equals("SI"))
          	          mkTotaleMed(doc,contaAssMed);
          	        Hashtable htot = new Hashtable();
  	                htot.put("#descrizione#", " Totale generale assistiti: ");
  	                htot.put("#totale#", " "+hContaGeneraleS.size());
	                doc.writeSostituisci("totale", htot);
		}
	} catch(Exception e) {
		debugMessage("FoAltroEleAssEJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}

/**
* stampa sintetica: entry point
*/
public byte[] query_altrosint(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {
	String punto = MIONOME  + "query_altrosint "; 
	LOG.info(punto+" Inizio con dati>"+par +"< ");
	
	ISASConnection dbc = null;
	boolean done = false;
	try {
		String type = par.get("TYPE")!=null?par.get("TYPE").toString():"PDF";
	
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="(Residenza)";
		else if (this.dom_res.equals("D")) this.dr="(Domicilio)";
		}
		myLogin lg = new myLogin();
		String selectedLanguage = (String)par.get(FileMaker.printParamLang);
		lg.put(utente,passwd,selectedLanguage);
		//lg.put(utente,passwd);
		dbc = super.logIn(lg);
		inMolise = recuperaConfUbicazione(dbc);
		
		ISASCursor dbcur=dbc.startCursor(getSelectSintetica(dbc,par));
		mkLayout(dbc, par, doc,(String)par.get("ass"));
		if (dbcur == null) {
			doc.write("messaggio");
			doc.write("finale");
			debugMessage("FoAltroEleAssEJB.query_altrosint(): "+
				"cursore nullo.");
		} else  {
			if (dbcur.getDimension() <= 0) {
				doc.write("messaggio");
			} else  {
			if (type.equals("PDF"))
				{
				mkSinteticaBody(dbc, dbcur, par, doc);
				}
				else mkSinteticaExcel(dbc, dbcur, par, doc);
			}
			doc.write("finale");
		}
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;

		doc.close();
		byte[] rit = (byte[])doc.get();		
		return rit;
	} catch(Exception e) {
		e.printStackTrace();
		debugMessage("FoAltroEleAssEJB.query_altrosint(): "+e);
		throw new SQLException("Errore eseguendo query_altrosint()");
		
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				e1.printStackTrace();
				debugMessage("FoAltroEleAssEJB.query_altrosint(): "+e1);
			}
		}
	}
}

/**
* stampa analitica: sezione riga tabella
*/
private void mkAnaliticaRigaTabella(ISASConnection dbc,
	ISASRecord dbr, mergeDocument doc, Hashtable par) {

	Hashtable ht1 = new Hashtable();
	Hashtable ht2 = new Hashtable();
        Hashtable ht3 = new Hashtable();
	String n_cartella = "";
	String nome_camp = "";
	try {
		if (hContaAssistitiA.containsKey(((Integer)dbr.get("n_cartella")).toString())==false)
                {
                    hContaAssistitiA.put(((Integer)dbr.get("n_cartella")).toString(),"");

                    ht1.put("#assistito#", (String)dbr.get("cognome")+" "+
                              (String)dbr.get("nome"));
                    ht1.put("#data_nasc#", getDateField(dbr, "data_nasc"));
					/*if (dbr.get("ski_tipocura")!=null && !dbr.get("ski_tipocura").toString().equals(""))
					ht1.put("#ski_tipocura#",utl.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
												"SAOADI", dbr.get("ski_tipocura").toString(), "tab_descrizione"));*/
                    try {
                            n_cartella=((Integer)dbr.get("n_cartella")).toString();
                    } catch(Exception e) {
                            n_cartella = "";
                    }
                    ht1.put("#cartella#", n_cartella);

	if (this.dom_res==null)
	{
	                    ht2.put("#indirizzo#",(String)dbr.get("dom_indiriz"));
	                    ht2.put("#citta#",getComune(dbc,(String)dbr.get("dom_citta")));
	}
	else if (this.dom_res.equals("R"))
	{ht2.put("#indirizzo#",(String)dbr.get("indirizzo"));
	ht2.put("#citta#",getComune(dbc,(String)dbr.get("citta")));
		}
	else if (this.dom_res.equals("D"))
	{ht2.put("#indirizzo#",(String)dbr.get("dom_indiriz"));
	ht2.put("#citta#",getComune(dbc,(String)dbr.get("dom_citta")));
		}

	
                    ht2.put("#telefono1#",
                            (String)dbr.get("telefono1"));

                    if(!((String)dbr.get("nome_camp")).equals(""))
                            nome_camp="("+(String)dbr.get("nome_camp")+")";
                    ht2.put("#nome_camp#",nome_camp);

/** 07/06/11:					
					ht2.put("#medico#",(String)dbr.get("mecogn")+" "+
										(String)dbr.get("menome"));
**/
					// 07/06/11
                    ht2.put("#medico#", utl.getDecode(dbc, "medici", "mecodi",
													(String)dbr.get("cod_med"), 
													"TRIM(NVL(mecogn, ''))||' '||TRIM(NVL(menome, ''))",
													"nome_mmg"));									   
                    }else{
                    ht1.put("#assistito#", "");
                    ht1.put("#data_nasc#", "");
                    ht1.put("#cartella#","");
					//ht1.put("#ski_tipocura#","");
                    ht2.put("#indirizzo#","");
                    ht2.put("#citta#","");
                    ht2.put("#telefono1#","");
                    ht2.put("#nome_camp#","");
                    ht2.put("#medico#","");
                }
                //***
                    ht3.put("#skf_data_apertura#",getDateField(dbr, "skfpg_data_apertura"));
                    ht3.put("#skf_data_uscita#",getDateField(dbr, "skfpg_data_uscita"));
                   // ht3.put("#skf_dimissioni#",getMotivoUscita(dbr,dbc));
                //***
	        hContaGeneraleA.put(((Integer)dbr.get("n_cartella")).toString(),"");
	} catch(Exception e) {
		ht1.put("#assistito#", "*** ERRORE ***");
		ht1.put("#data_nasc#"," ");
		ht1.put("#cartella#", n_cartella);
		//ht1.put("#ski_tipocura#","");
		ht2.put("#indirizzo#", "*** ERRORE ***");
		ht2.put("#citta#"," ");
		ht2.put("#nome_camp#", " ");
		ht2.put("#telefono1#"," ");
		ht2.put("#medico#", " ");

                ht3.put("#skf_data_apertura#", " ");
		ht3.put("#skf_data_uscita#", " ");
		//ht3.put("#skf_dimissioni#", " ");

	}
	doc.writeSostituisci("assistito1", ht1);
        doc.writeSostituisci("contatti", ht3);
	//mkAnaliticaContatti(dbc, n_cartella, doc, par);
	 doc.writeSostituisci("assistito2", ht2);
}

/**
* stampa analitica: sezione blocco ripetitivo "CONTATTI" riga tabella
*/
private void mkAnaliticaContatti(ISASConnection dbc, String n_cartella,
	mergeDocument doc, Hashtable par) {
	Hashtable ht = new Hashtable();
	try {
		ISASCursor dbcur=dbc.startCursor(
				getSelectContatti(dbc, par,n_cartella));

		if (dbcur == null) {
			ht.put("#skf_data_apertura#", " ");
        		ht.put("#skf_data_uscita#"," ");
        		//ht.put("#ski_dimissioni#", " ");
        		doc.writeSostituisci("contatti", ht);
		} else {
			while (dbcur.next()) {
				ISASRecord dbinf = dbcur.getRecord();
				ht.put("#skf_data_apertura#",
					getDateField(dbinf, "data_inizio"));
				ht.put("#skf_data_uscita#",
					getDateField(dbinf, "data_fine"));
				/*ht.put("#ski_dimissioni#",
					getMotivoUscita(dbinf,dbc));*/
          			doc.writeSostituisci("contatti", ht);
			}
		}
		dbcur.close();
	} catch(Exception e) {
		ht.put("#skf_data_apertura#", "*** ERRORE ***");
		ht.put("#skf_data_uscita#"," ");
		//ht.put("#ski_dimissioni#", " ");
		doc.writeSostituisci("contatti", ht);
	}
}

/**
* stampa analitica: corpo
*/
private void mkAnaliticaBody(ISASConnection dbc, ISASCursor dbcur,
	Hashtable par, mergeDocument doc) throws Exception {

	boolean first_time = true;
	//29-09-2003 aggiunta divisione per operatore
        String old_oper = "*", cur_oper = "";

        String old_zona = "*", cur_zona = "";
	String old_dist = "*", cur_dist = "";
	String old_comu = "*", cur_comu = "";

	try {
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			//29-09-2003 aggiunta divisione per operatore
                        if (par.get("oper")!=null && par.get("oper").equals("SI"))
                        {
                        	String tipo_operatore=(String)dbr.get("dipend_conv");
                            String tipo_op="";
                            if (tipo_operatore!=null && !tipo_operatore.equals("")){
                          	  if (tipo_operatore.equals("D"))
                          		  tipo_op="Operatore dipendente";
                          	  else if (tipo_operatore.equals("C"))
                          		  tipo_op="Operatore convenzionato";
                            }  
                          cur_oper=(String)dbr.get("cognome_oper")+" "+(String)dbr.get("nome_oper") + " " + tipo_op;
                        }
                        cur_zona = (String)dbr.get("descrizione_zona");
			cur_dist = (String)dbr.get("des_distr");
			cur_comu = (String)dbr.get("descrizione");
			boolean flag = (! old_zona.equals(cur_zona)) ||
					(! old_dist.equals(cur_dist)) ||
					(! old_comu.equals(cur_comu));
			//29-09-2003 aggiunta divisione per operatore
                        if ((par.get("oper")!=null &&
                          par.get("oper").equals("SI")&&
                          !old_oper.equals(cur_oper))||
                          (flag)) {
				if (!first_time) {
					mkFineTabellaA(doc);
				}
				//29-09-2003 aggiunta divisione per operatore
				if (par.get("oper")!=null &&
                                     par.get("oper").equals("SI")&&
                                     !old_oper.equals(cur_oper))
                                {
                                       if (!first_time){
                                          mkTotaleMed(doc);
                                          mkPaginaNuova(doc);
                                          }
                                          mkTaglioOperatore(doc,par,cur_oper);
                                        mkCoordinate(doc,par,
						cur_zona, cur_dist, cur_comu);
                                                }
				else if (flag){
					mkCoordinate(doc,par,
						cur_zona, cur_dist, cur_comu);
				}
				mkIniziaTabella(doc,dbr);
			}
			// 29-09-2003 aggiunta divisione per operatore
			old_oper = cur_oper;

                        old_zona = cur_zona;
			old_dist = cur_dist;
			old_comu = cur_comu;
                        if (first_time) {
					first_time = ! first_time;
				}

			// stampa riga
			mkAnaliticaRigaTabella(dbc, dbr, doc, par);

		}
		if (!first_time) {
			// chiudi tabella precedente
			mkFineTabellaA(doc);
                        if (par.get("oper")!=null &&
                          par.get("oper").equals("SI"))
                        mkTotaleMed(doc);
          	        Hashtable htot = new Hashtable();
  	                htot.put("#descrizione#", " Totale generale assistiti: ");
  	                htot.put("#totale#", " "+hContaGeneraleA.size());
	                doc.writeSostituisci("totale", htot);
		}
	} catch(Exception e) {
		debugMessage("FoAltroEleAssEJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}

/**
* stampa analitica: entry point
*/
public byte[] query_altro(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	try {
	String type = par.get("TYPE")!=null?par.get("TYPE").toString():"PDF";

		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="(Residenza)";
		else if (this.dom_res.equals("D")) this.dr="(Domicilio)";
		}
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);
		inMolise = recuperaConfUbicazione(dbc);
		
		ISASCursor dbcur=dbc.startCursor(getSelectAnalitica(dbc,par));
		mkLayout(dbc, par, doc, (String)par.get("ass"));
		if (dbcur == null) {
			doc.write("messaggio");
			doc.write("finale");
			debugMessage("FoAltroEleAssEJB.query_altro(): "+
				"cursore nullo.");
		} else  {
			if (dbcur.getDimension() <= 0) {
				doc.write("messaggio");
			} else  {
			if (type.equals("PDF"))
				mkAnaliticaBody(dbc, dbcur, par, doc);
			else 	mkAnaliticaExcel(dbc, dbcur, par, doc);
			}
			doc.write("finale");
		}
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;

		doc.close();
		byte[] rit = (byte[])doc.get();
		
		return rit;
	} catch(Exception e) {
		debugMessage("FoAltroEleAssEJB.query_altroeref(): "+e);
		throw new SQLException("Errore eseguendo query_altro()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				debugMessage("FoAltroEleAssEJB.query_altro(): "+e1);
			}
		}
	}
}

private String getSelectContatti(ISASConnection dbc, Hashtable par,
	String n_cartella) {

	String selConta="";
	String dt_ini = (String)par.get("data_inizio");
	String dt_fine = (String)par.get("data_fine");

	selConta = "SELECT skf_data_apertura data_inizio, "+
		"skf_data_uscita data_fine " +
		//",ski_dimissioni " +
		"FROM skfpg s "+
		"WHERE s.n_cartella = "+ n_cartella+
		" AND s.skf_data_apertura <= "+
		formatDate(dbc,dt_fine)+
		" AND (s.skf_data_uscita IS NULL"+
		" OR s.skf_data_uscita = "+
		formatDate(dbc,"1000-01-01")+
		" OR s.skf_data_uscita >="+
		formatDate(dbc,dt_ini)+")";

	System.out.println("Select Contatti"+selConta);
	return selConta;
}

private String faiRaggruppamento(String tipo) {
	String raggruppa="";
	if(this.dom_res==null)
    {
    if (tipo.trim().equals("A"))
    	raggruppa="Area Distr.";
    else if (tipo.trim().equals("C"))
    	raggruppa="Comune";
    else if (tipo.trim().equals("P"))
    	raggruppa="Presidio";
    else	raggruppa="TIPO NON VALIDO";
    }else if (this.dom_res.equals("D"))
    {
        if (tipo.trim().equals("A"))
        	raggruppa="Area Distr. di Domicilio";
        else if (tipo.trim().equals("C"))
        	raggruppa="Comune di Domicilio";
        else if (tipo.trim().equals("P"))
        	raggruppa="Presidio";
        else	raggruppa="TIPO NON VALIDO";
        }else if (this.dom_res.equals("R"))
        {
            if (tipo.trim().equals("A"))
            	raggruppa="Area Distr. di Residenza";
            else if (tipo.trim().equals("C"))
            	raggruppa="Comune di Residenza";
            else if (tipo.trim().equals("P"))
            	raggruppa="Presidio";
            else	raggruppa="TIPO NON VALIDO";
            }  	

	return raggruppa;
}

private void mkSinteticaExcel(ISASConnection dbc, ISASCursor dbcur, Hashtable par, mergeDocument doc)
    throws Exception{
            Hashtable p = new Hashtable();
            String ragg = faiRaggruppamento((String)par.get("ragg"));
      		p.put("#raggruppamento#", ragg);
            doc.writeSostituisci("iniziotab", p);
			p.clear();
			
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
				
			     p.put("#descrizione_zona#", dbr.get("descrizione_zona").toString());
				
                p.put("#des_distr#", dbr.get("des_distr").toString());
				
                p.put("#descrizione#",dbr.get("descrizione").toString());
                String tipo_operatore=(String)dbr.get("dipend_conv");
                String tipo_op="";
                if (tipo_operatore!=null && !tipo_operatore.equals("")){
              	  if (tipo_operatore.equals("D"))
              		  tipo_op="Operatore dipendente";
              	  else if (tipo_operatore.equals("C"))
              		  tipo_op="Operatore convenzionato";
                }  
				p.put("#operatore#",dbr.get("nome_oper").toString()+ " "+dbr.get("cognome_oper").toString() + " " + tipo_op);
			
                p.put("#nome_ass#", (String)dbr.get("cognome")+" "+(String)dbr.get("nome"));
				
             
		p.put("#data_nasc#", "("+getDateField(dbr, "data_nasc")+")");
	
		if (dbr.get("n_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
              p.put("#n_cartella#",((Integer)dbr.get("n_cartella")).toString());
          
				doc.writeSostituisci("inizio_riga",p);
				p.clear();
            }
            doc.write("finetab");
    }




private void mkAnaliticaExcel(ISASConnection dbc, ISASCursor dbcur, Hashtable par, mergeDocument doc)
    throws Exception{
            Hashtable p = new Hashtable();
            String ragg = faiRaggruppamento((String)par.get("ragg"));
            p.put("#raggruppamento#", ragg);
			String indirizzo = "";
			String dom_indiriz= "";
            doc.writeSostituisci("iniziotab", p);
			p.clear();
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
				System.out.println("mkAnaliticaExcel:0 "+dbr.getHashtable().toString());
				System.out.println("mkAnaliticaExcel:1 "+par.toString());
				p.put("#descrizione_zona#", dbr.get("descrizione_zona").toString());
                p.put("#des_distr#", dbr.get("des_distr").toString());
                p.put("#descrizione#",dbr.get("descrizione").toString());
                p.put("#nome_ass#", (String)dbr.get("cognome")+" "+(String)dbr.get("nome"));
                System.out.println("nomeass"+(String)dbr.get("cognome")+" "+(String)dbr.get("nome"));                
                String tipo_operatore=(String)dbr.get("dipend_conv");
                String tipo_op="";
                if (tipo_operatore!=null && !tipo_operatore.equals("")){
              	  if (tipo_operatore.equals("D"))
              		  tipo_op="Operatore dipendente";
              	  else if (tipo_operatore.equals("C"))
              		  tipo_op="Operatore convenzionato";
                }  
                p.put("#operatore#",dbr.get("nome_oper").toString()+ " "+dbr.get("cognome_oper").toString()+ " " + tipo_op);
				System.out.println("operatore"+dbr.get("nome_oper").toString()+ " "+dbr.get("cognome_oper").toString());
             System.out.println("mkAnaliticaExcel: 2");
		p.put("#data_nasc#", "("+getDateField(dbr, "data_nasc")+")");
		/*if (dbr.get("ski_tipocura")!=null && !dbr.get("ski_tipocura").toString().equals(""))
					p.put("#ski_tipocura#",utl.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
												"SAOADI", dbr.get("ski_tipocura").toString(), "tab_descrizione"));*/
		if (dbr.get("n_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
                    p.put("#n_cartella#",((Integer)dbr.get("n_cartella")).toString());
				doc.writeSostituisci("inizio_riga",p);
				p.clear();
				mkAnaliticaContatti(dbc,dbr.get("n_cartella").toString(), doc, par);
				System.out.println("mkAnaliticaExcel: 5");
				p.put("#mmg#",utl.getDecode(dbc, "medici", "mecodi",
													(String)dbr.get("cod_med"), 
													"TRIM(NVL(mecogn, ''))||' '||TRIM(NVL(menome, ''))",
													"nome_mmg"));
				System.out.println("mkAnaliticaExcel: 6");
				if (dbr.get("indirizzo")!=null)
				indirizzo = dbr.get("indirizzo").toString();
				if (dbr.get("dom_indiriz")!=null)
				dom_indiriz = dbr.get("dom_indiriz").toString();
				System.out.println("mkAnaliticaExcel: 7");
				if(this.dom_res == null) p.put("#indirizzo#",dom_indiriz);
				else p.put("#indirizzo#",(this.dom_res.equals("R")?indirizzo:dom_indiriz));
				System.out.println("mkAnaliticaExcel: 8");
				doc.writeSostituisci("fine_riga",p);
				p.clear();
            }
            doc.write("finetab");
    }


}	// End of FoFAssEle class

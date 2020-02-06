package it.caribel.app.sinssnt.bean.modificati;
/* ============================================================================
 * CARIBEL S.r.l. - www.caribel.pisa.it
 * ----------------------------------------------------------------------------
 *
 * 12/09/2003 - SINSS: Statistica Assistenza Infermieristica Domiciliare
 *
 * Roberto Bonsignori
 * Ilaria Mancini 07/10/2003
 * ============================================================================
 */

import java.io.*;
import java.sql.*;
import java.util.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.util.*;

public class FoStatDomiciliareEJB extends SINSSNTConnectionEJB  {
	String dom_res;
	String dr;
	private static final String CODICE_TIPO_AREA_DISTRETTUALE="A";
	private static final String CODICE_TIPO_COMUNE ="C";
	private static final String CODICE_TIPO_PRESIDIO ="P";
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO ="D";
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA ="R";
	private static final String CODICE_SANITARIO = "02";
	private static final String CODICE_SOCIALE = "01";;

	
public FoStatDomiciliareEJB() {}
   
private class Totali {
	public int t0 = 0, t1 = 0, t2 = 0, t3 = 0, t4 = 0;
	public void setZero() {
		this.t0 = 0; this.t1 = 0; this.t2 = 0;
                this.t3 = 0; this.t4 = 0;
	}
	public void add(Totali t) {
		this.t0 += t.t0;
		this.t1 += t.t1;
		this.t2 += t.t2;
       		this.t3 += t.t3;
		this.t4 += t.t4;

	}
}	// end of Totali class

String motivo="";   String unifun="";   String list="";   String from_uni="";
String from_cont="";    String where_uni="";    String where_cont="";
String order="";  String tipo="";
private static final  String MIONOME ="2-FoStatDomiciliareEJB.";

private static String getjdbcDate() {
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}

public byte[] query_statdom(String utente, String passwd,
	Hashtable par, mergeDocument mdoc) throws SQLException {
	String punto = MIONOME + "query_statdom ";

	boolean done = false;
	ISASConnection dbc = null;
	try{
		
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}
		
		myLogin lg = new myLogin();
		lg.put(utente, passwd);
		dbc = super.logIn(lg);
                unifun=(String)par.get("unifun");
		mkLayout(dbc, par, mdoc, par);
		mkAccess(dbc, par, mdoc);
		mdoc.write("finale");
		mdoc.close();
		dbc.close();
		super.close(dbc);
		byte[] rep = (byte[])mdoc.get();
		//debugMessage("FoStatDomiciliareEJB.query_stadom(): "+
		//	(new String(rep)));
		done=true;
		stampa(punto + "\n Fine esecuzione messaggio");
		return rep;
	}catch(Exception e){
		debugMessage("FoStatDomiciliareEJB.query_stadom(): "+e);
		throw new SQLException("Errore eseguendo una query_stadom()");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

private void mkLayout(ISASConnection dbc, Hashtable par, mergeDocument mdoc, Hashtable par2) {
	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();

	String linea = "";
	String valore = "";
	//	tipo=C&
//		ragg=A&&dom_res=R&socsan=01
//		distretto=1  pca=1 		zona=1
	String codZona = ISASUtil.getValoreStringa(par, "zona");
	if (ISASUtil.valida(codZona)){
		String descrZona = recuperaDescrizioneZona(dbc, codZona);
		if (ISASUtil.valida(descrZona)){
			linea += "Zona: "+ descrZona+ " ";
		}
	}
	String codDistretto = ISASUtil.getValoreStringa(par, "distretto");
	if (ISASUtil.valida(codDistretto)){
		String descrDistretto = recuperaDescrizioneDistretto(dbc, codDistretto);
		if (ISASUtil.valida(descrDistretto)){
			linea += "Distretto: "+ descrDistretto + " ";;
		}
	}
	
	String raggruppamento = ISASUtil.getValoreStringa(par, "ragg");
	if (ISASUtil.valida(raggruppamento)) {
		if (raggruppamento.equals(CODICE_TIPO_COMUNE)) {
			valore = "Comune ";
			String codComune = ISASUtil.getValoreStringa(par, "pca");
			if (ISASUtil.valida(codComune)){
				String descrComune = recuperaDescrizioneComune(dbc, codComune);
				if (ISASUtil.valida(descrComune)){
					valore += descrComune + " ";
				}
			}
		} else if(raggruppamento.equals(CODICE_TIPO_AREA_DISTRETTUALE)) {
			valore = "Area distrettuale";
			String codAreaDis = ISASUtil.getValoreStringa(par, "pca");
			if (ISASUtil.valida(codAreaDis)){
				String descrAreaDistret= recuperaDescrizioneAreaDistrettuale(dbc, codAreaDis);
				if (ISASUtil.valida(descrAreaDistret)){
					valore += ": "+ descrAreaDistret  + " ";
				}
			}
		}
		else if(raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
			valore = "Presidio";
			String codPresidio = ISASUtil.getValoreStringa(par, "pca");
			if (ISASUtil.valida(codPresidio)){
				String descrPresidio= recuperaDescrizionePresidio(dbc, codPresidio);
				if (ISASUtil.valida(descrPresidio)){
					valore += ": "+ descrPresidio  + " ";
				}
			}
		}
		linea += "Raggruppamento: " + valore;
	}
	String linea1 = "";
	String tipologiaUbicazione = ISASUtil.getValoreStringa(par, "dom_res");
	if (ISASUtil.valida(tipologiaUbicazione)) {
		if (tipologiaUbicazione.equals(CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA)) {
			valore = "Residenza";
		} else if (tipologiaUbicazione.equals(CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO)){
			valore = "Domicilio";
		}
		linea1 += "Tipologia di Ubicazione: " + valore;
	}
	String socialeSan = ISASUtil.getValoreStringa(par, "socsan");
	if (ISASUtil.valida(socialeSan)) {
		if (socialeSan.equals(CODICE_SOCIALE)) {
			valore = "Sociale ";
		} else if (socialeSan.equals(CODICE_SANITARIO)) {
			valore = "Sanitario";
		}
		if (ISASUtil.valida(tipologiaUbicazione)){
			linea1+=", "+valore;
		}else {
			linea1 += "Tipologia di Ubicazione: " + valore;
		}
	}
	ht.put("#filtri#",linea);
	ht.put("#filtri1#",linea1);
	
	ht.put("#conf_txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));
	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	ht.put("#figura_professionale#",
		getFiguraProfessionale(
			par.get("figprof")).toUpperCase());
	//minerba 06/03/2013
	ht.put("#qualifica#",
			getQualifica(
				par.get("qualifica")).toUpperCase());
	//fine minerba	
	mdoc.writeSostituisci("layout",ht);
}

//M.Minerba 28/02/2013 per Pistoia
	private String recuperaDescrizionePresidio(ISASConnection dbc, String codPresidio) {
		String descrizionePresidio = "";
		String query = "SELECT * FROM presidi WHERE codpres = '" + codPresidio + "' ";
		ISASRecord dbrPresidio = getRecord(dbc, query);
		descrizionePresidio = ISASUtil.getValoreStringa(dbrPresidio, "descpres");
		return descrizionePresidio;
	}//fine M.Minerba 28/02/2013 per Pistoia

private String recuperaDescrizioneAreaDistrettuale(ISASConnection dbc, String codAreaDis) {
	String descrizioneComune = "";
	String query ="SELECT * FROM areadis WHERE codice = '" +codAreaDis +"' ";
	ISASRecord dbrComune = getRecord(dbc, query);
	descrizioneComune = ISASUtil.getValoreStringa(dbrComune, "descrizione");
	return descrizioneComune;
}

private String recuperaDescrizioneComune(ISASConnection dbc, String codComune) {
	String descrizioneComune = "";
	String query =" SELECT * FROM comuni  WHERE codice = '" +codComune+"' ";
	ISASRecord dbrDistretto = getRecord(dbc, query);
	descrizioneComune = ISASUtil.getValoreStringa(dbrDistretto, "descrizione");
	return descrizioneComune;
}

private String recuperaDescrizioneDistretto(ISASConnection dbc, String codDistretto) {
	String descrizioneDistretto = "";
	String query =" Select * from distretti WHERE cod_distr = '" +codDistretto +"' ";
	ISASRecord dbrDistretto = getRecord(dbc, query);
	descrizioneDistretto = ISASUtil.getValoreStringa(dbrDistretto, "des_distr");
	return descrizioneDistretto;
}

private String recuperaDescrizioneZona(ISASConnection dbc, String codZona) {
	String descrizioneZona = "";
	String query =" select * from zone where codice_zona = '" +codZona +"' ";
	ISASRecord dbrZone = getRecord(dbc, query);
	descrizioneZona = ISASUtil.getValoreStringa(dbrZone, "descrizione_zona");
	return descrizioneZona;
}

private ISASRecord getRecord(ISASConnection dbc, String query) {
	String punto = MIONOME + ".getRecord ";
	ISASRecord dbr = null;
	try {
		System.out.println(punto + "Query da eseguire> " + query + "<");
		dbr = dbc.readRecord(query);
	} catch (Exception ex) {
		System.out.println(punto + "Errore nel leggere il record con query>" + query + "<");
	}
	return dbr;
}


private void mkAccess(ISASConnection dbc, Hashtable par, mergeDocument mdoc)
	throws Exception {
String punto = "mkAccess ";
	ISASCursor dbcur =null;
        ISASCursor dbcurZona=null;
        String ragg =(String)par.get("ragg");
        FaiCriteri(dbc, par);
        //14/02/2006 richiamo sempre la selext territorio perch� in interv
        //ho tutti i campi, compresi comune e area distrettuale
        //if (ragg.equals("P"))
                dbcur=dbc.startCursor(getSelectTerritorio(dbc, par));
//        else
//                dbcur = dbc.startCursor(getSelectTerritorioComuni(dbc, par));
	if (dbcur == null) {
		mdoc.write("messaggio");
		debugMessage("FoStatDomiciliareEJB.mkAccess(): cursore nullo.");
	} else {
		if (dbcur.getDimension() > 0){
/*                        if (!ragg.equals("P")){
                              dbcurZona=mkZona(dbc, dbcur,par);
                              if (dbcurZona.getDimension() > 0)
                                 if(!unifun.equals(""))
                                    mkAccessBodyUnita(dbc, dbcurZona, par, mdoc);
                                 else
                                    mkAccessBody(dbc, dbcurZona, par, mdoc);
                              else
                                    mdoc.write("messaggio");
                        }
                        else*/
			stampa(punto + "\nunifun>"+ unifun+"<");
                          if(!unifun.equals("") && !unifun.equals("TUTTO"))
                                 mkAccessBodyUnita(dbc, dbcur, par, mdoc);
                          else   mkAccessBody(dbc, dbcur, par, mdoc);
                }
                else
             	    mdoc.write("messaggio");
	}
	dbcur.close();
}

private void mkAccessBody(ISASConnection dbc, ISASCursor dbcur,
	Hashtable par, mergeDocument mdoc) throws Exception {
        boolean first_time = true;
	String o_zona = "*", c_zona = "";	// old and current values
	String o_dist = "*", c_dist = "";	// old and current values

	Totali totali = new Totali();
	Totali t_zona = new Totali();
	Totali t_dist = new Totali();

	/*
        *  ILARIA INIZIO:se e' stato scelto come raggruppamento
        *  il presidio non devo stampare la colonna dei dimessi
        *  perche' per calcolare i dimessi vado su contsan_n e non ho
        *  l 'informazione del presidio
        */
        String ragg="";
        String sezione="";
        if (par.get("ragg")!=null )
                ragg=(String)par.get("ragg");
        if (ragg.equals("P"))
              sezione="_pres";
        //ILARIA FINE
        Hashtable htt = new Hashtable();
	htt.put("#titolo#", getTitolo(par));
	mdoc.writeSostituisci("tabella1"+sezione, htt);
	Hashtable prtDati = new Hashtable();
	while (dbcur.next()) {
        	ISASRecord dbr=dbcur.getRecord();
		c_zona = (String)dbr.get("des_zona");
		c_dist = (String)dbr.get("des_distretto");
		prtDati.put("#unita#", "");
		prtDati.put("#c_zona#", c_zona);
		prtDati.put("#c_distretto#", c_dist);
		if (! o_zona.equals(c_zona)) {
			if (first_time) {
				first_time = ! first_time;
			} else {
				t_zona.add(t_dist);
				mkRiga(mdoc, "tot_dist1"+sezione,
					"#descr#", o_dist, t_dist);
				totali.add(t_zona);
				mkRiga(mdoc, "tot_zona1"+sezione,
					"#descr#", o_zona, t_zona);
			}
			mkRiga(mdoc, "zona1"+sezione, "#descr#", c_zona, null);
			mkRiga(mdoc, "dist1"+sezione, "#descr#", c_dist, null);
		} else if (! o_dist.equals(c_dist)) {
			t_zona.add(t_dist);
			mkRiga(mdoc, "tot_dist1"+sezione,
				"#descr#", o_dist, t_dist);
			mkRiga(mdoc, "dist1"+sezione,
				"#descr#", c_dist, null);
		}

                int n0= getAccessi(dbc, par,  (String)dbr.get("codice"));
                int n1= getPrestazioni(dbc, par,  (String)dbr.get("codice"));
                //Ilaria-inizio
                //int n2 = parteIntera(dbr.get("assistiti"));
                int n2= getAssistiti(dbc, par,  (String)dbr.get("codice"));
		//vado a prendere gli ultra 65
                int n3= getUltra65(dbc, par,  (String)dbr.get("codice"));
                //vado a prendere i dimessi nel caso in cui il raggruppamento
                //� comune o area distrettuale
                int n4=0;
                if (par.get("ragg")!=null && !((String)par.get("ragg")).equals("P"))
                       n4= getDimessi(dbc, par, (String)dbr.get("codice"));
                //Ilaria-fine

                Hashtable ht = new Hashtable();
		ht.put("#descr#", (String)dbr.get("descrizione"));
		prtDati.put("#des_pace#", (String)dbr.get("descrizione"));
		prtDati.put("#t0#", ""+n0);
		prtDati.put("#t1#", ""+n1);
		prtDati.put("#t2#", ""+n2);
		prtDati.put("#t3#", ""+n3);
		prtDati.put("#t4#", ""+n4);
                
		ht.put("#t0#", ""+n0);
		ht.put("#t1#", ""+n1);
                ht.put("#t2#", ""+n2);
                ht.put("#t3#", ""+n3);
                ht.put("#t4#", ""+n4);
                
                mdoc.writeSostituisci("comu1"+sezione, ht);
                mdoc.writeSostituisci("tabella1_corpo"+sezione, prtDati);
                
                t_dist.t0 += n0;
                t_dist.t1 += n1;
                t_dist.t2 += n2;
                t_dist.t3 += n3;
                t_dist.t4 += n4;
                o_zona = c_zona;
		o_dist = c_dist;
	}
	if (! first_time) {
		t_zona.add(t_dist);
		mkRiga(mdoc, "tot_dist1"+sezione, "#descr#", c_dist, t_dist);
		totali.add(t_zona);
		mkRiga(mdoc, "tot_zona1"+sezione, "#descr#", c_zona, t_zona);
	}
	mkRiga(mdoc, "tot_gene1"+sezione, "#descr#", "", totali);
	mdoc.write("finetab1");
        if(((String)par.get("ragg")).equals("P")){
          Hashtable ht = new Hashtable();
          ht.put("#d3#", ""+getAssistiti(dbc, par, ""));
          ht.put("#d4#", ""+getUltra65(dbc, par, ""));
          mdoc.writeSostituisci("tot_depurato",ht);
        }
	//mdoc.write("taglia");
}

private String getTitolo(Hashtable par) {
	String ragg = (String)par.get("ragg");
	String raggruppa="";
	if(this.dom_res==null)
    {
    if (ragg.trim().equals("A"))
    	raggruppa="Area Distr.";
    else if (ragg.trim().equals("C"))
    	raggruppa="Comune";
    else if (ragg.trim().equals("P"))
    	raggruppa="Presidio";
    else	raggruppa="-";
    }else if (this.dom_res.equals("D"))
    {
        if (ragg.trim().equals("A"))
        	raggruppa="Area Distr. di Domicilio";
        else if (ragg.trim().equals("C"))
        	raggruppa="Comune di Domicilio";
        else	raggruppa="-";
        }else if (this.dom_res.equals("R"))
        {
            if (ragg.trim().equals("A"))
            	raggruppa="Area Distr. di Residenza";
            else if (ragg.trim().equals("C"))
            	raggruppa="Comune di Residenza";
            else	raggruppa="-";
            }  	

	return raggruppa;
}

private String getSelectUltra65(ISASConnection dbc, Hashtable par,
	 String terzolivello) {

	ServerUtility su = new ServerUtility();
	String s ="SELECT COUNT (DISTINCT c.n_cartella ) sessanta5 "+
                  "FROM interv i, cartella c "+from_cont+
                  " WHERE i.int_cartella = c.n_cartella "+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	s = su.addWhere(s, su.REL_AND, "i.int_ambdom",
		su.OP_EQ_STR, "D");

	if (!((String)par.get("figprof")).equals("00"))
		s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
			su.OP_EQ_STR, (String)par.get("figprof"));
//Jessy 18/11/04 se terzolivello � vuoto � perch� mi sto calcolando i totali
//senza tener conto del raggruppamento
        if (!(terzolivello.equals("")))
        {
          String ragg = (String)par.get("ragg");
//          if (ragg.equals("A"))
//                  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",
//                          su.OP_EQ_STR, terzolivello);
//          else if (ragg.equals("C"))
//                  s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",
//                          su.OP_EQ_STR, terzolivello);
//          else  if (ragg.equals("P"))
//                  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
//                          su.OP_EQ_STR,terzolivello);
          
//Aggiunto Controllo Domicilio/Residenza (BYSP)
  		if(this.dom_res==null)
		{
		if(ragg.equals("A"))
 		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                    su.OP_EQ_STR,terzolivello);
        else if(ragg.equals("C"))
        	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                    su.OP_EQ_STR,terzolivello);
        else if (ragg.equals("P"))
    	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
    			  su.OP_EQ_STR,terzolivello);
		}
		else if(this.dom_res.equals("D"))
		{
			if(ragg.equals("A"))
            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                        su.OP_EQ_STR,terzolivello);
            else if(ragg.equals("C"))
            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                        su.OP_EQ_STR,terzolivello);
        }
		else if(this.dom_res.equals("R"))
			{
				if(ragg.equals("A"))
	            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
	                        su.OP_EQ_STR,terzolivello);
	            else if(ragg.equals("C"))
	            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
	                        su.OP_EQ_STR,terzolivello);
	            
			}
          
          
				

          
          
        }
	s += " AND c.data_nasc <= "+formatDate(dbc,getData65());
	debugMessage("FoStatDomiciliareEJB.getSelectUltra65(): "+s);
	return s;
}
/*gb 11/06/07 *******
private String getSelectDimessi(ISASConnection dbc, Hashtable par,
	String terzolivello) {

	ServerUtility su = new ServerUtility();
	String ragg =(String)par.get("ragg");
	String s = "SELECT COUNT (DISTINCT n.n_cartella ) dimessi "+
                   "FROM  contsan n, anagra_c a "+from_cont+" WHERE "+
                   " n.n_cartella = a.n_cartella ";
        if(!from_cont.equals("")){
          if(tipo.equals("01")){
            s+=" AND n.n_cartella=co.n_cartella "+
                 " AND n.n_contatto=co.n_contatto";
            if(!motivo.equals("TUTTO"))
              s+=" AND co.motivo='"+motivo+"'";
          }else if(tipo.equals("02")){
            s+=" AND n.n_cartella=sk.n_cartella "+
               " AND n.n_contatto=sk.n_contatto";
            if(!motivo.equals("TUTTO"))
              s+=" AND sk.ski_motivo='"+motivo+"'";
          }
        }
	String finale = "";
	if (tipo.equals("01")) finale="sociale";
	else if (tipo.equals("02")) finale="infer";
	else if (tipo.equals("03")) finale="medico";
	else if (tipo.equals("04")) finale="fisiot";

	if (ragg.equals("C"))
		s += " AND a.dom_citta='"+terzolivello+"'";
	else if (ragg.equals("A"))
		s += " AND a.dom_areadis='"+terzolivello+"'";

	s += " AND a.data_variazione IN ("+
		" SELECT MAX (anagra_c.data_variazione)"+
		" FROM anagra_c WHERE anagra_c.n_cartella=n.n_cartella )";
	s = su.addWhere(s, su.REL_AND, "n.data_chius_"+finale,
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "n.data_chius_"+finale,
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	debugMessage("FoStatDomiciliareEJB.getSelectDimessi(): "+s);
	return s;
}
*gb 11/06/07: fine *******/
//gb 11/06/07 *******





private String getSelectDimessi(ISASConnection dbc, Hashtable par,
		String terzolivello) {

		String strNomeTabella = "";
		if (tipo.equals("01")) strNomeTabella = "ass_progetto";
		else if (tipo.equals("02")) strNomeTabella="skinf";
		else if (tipo.equals("03")) strNomeTabella="skmedico";
		else if (tipo.equals("04")) strNomeTabella="skfis";
		else if (tipo.equals("52")) strNomeTabella="skmedpal";


		ServerUtility su = new ServerUtility();
		String ragg =(String)par.get("ragg");
		String s = "SELECT COUNT (DISTINCT n.n_cartella ) dimessi "+
	                   " FROM " + strNomeTabella + " n, anagra_c a" +
			   " WHERE n.n_cartella = a.n_cartella ";

	//gb 11/06/07: prendo in considerazione l'eventuale motivo
//			solo per casi tpo_operatore 01 o 02
	        if(tipo.equals("01")){
	            if(!motivo.equals("TUTTO"))
	              s+=" AND n.ap_motivo='"+motivo+"'";
	        }else if(tipo.equals("02")){
	            if(!motivo.equals("TUTTO"))
	              s+=" AND n.ski_motivo='"+motivo+"'";
	        }

		String strNomeCampo = "";
		if (tipo.equals("01")) strNomeCampo="ap_data_chiusura";
		else if (tipo.equals("02")) strNomeCampo="ski_data_uscita";
		else if (tipo.equals("03")) strNomeCampo="skm_data_chiusura";
		else if (tipo.equals("04")) strNomeCampo="skf_data_chiusura";
		else if (tipo.equals("52")) strNomeCampo="skm_data_chiusura";

		if(this.dom_res==null)
		{
		if (ragg.equals("C"))
			s += " AND a.dom_citta='"+terzolivello+"'";
		else if (ragg.equals("A"))
			s += " AND a.dom_areadis='"+terzolivello+"'";
		}else if (this.dom_res.equals("D"))
		{
			if (ragg.equals("C"))
				s += " AND a.dom_citta='"+terzolivello+"'";
			else if (ragg.equals("A"))
				s += " AND a.dom_areadis='"+terzolivello+"'";
		}else if (this.dom_res.equals("R"))
		{
			if (ragg.equals("C"))
				s += " AND a.citta='"+terzolivello+"'";
			else if (ragg.equals("A"))
				s += " AND a.areadis='"+terzolivello+"'";
		}
		s += " AND a.data_variazione IN ("+
			" SELECT MAX (anagra_c.data_variazione)"+
			" FROM anagra_c WHERE anagra_c.n_cartella=n.n_cartella )";
		s = su.addWhere(s, su.REL_AND, "n." + strNomeCampo,
			su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
		s = su.addWhere(s, su.REL_AND, "n." + strNomeCampo,
			su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

		debugMessage("FoStatDomiciliareEJB.getSelectDimessi(): "+s);
		return s;
	}
//gb 11/06/07: fine *******

private String getData65() {
	String datasystem = this.getjdbcDate();
	java.util.GregorianCalendar DefGreg = new java.util.GregorianCalendar();
	String datasistema = datasystem.substring(8,10)+
		datasystem.substring(5,7)+datasystem.substring(0,4);
	DataWI dataWI = new DataWI(datasistema);
	DefGreg.setTime(dataWI.getSqlDate());
	DefGreg.add(java.util.Calendar.YEAR, -65);
	java.sql.Date df = new java.sql.Date((DefGreg.getTime()).getTime());
	return "" + df;
}

private java.sql.Date get65YearAgo() {
	String datasystem = this.getjdbcDate();
	java.util.GregorianCalendar DefGreg = new java.util.GregorianCalendar();
	String datasistema = datasystem.substring(8,10)+
		datasystem.substring(5,7)+datasystem.substring(0,4);
	DataWI dataWI = new DataWI(datasistema);
	DefGreg.setTime(dataWI.getSqlDate());
	DefGreg.add(java.util.Calendar.YEAR, -65);
	java.sql.Date df = new java.sql.Date((DefGreg.getTime()).getTime());
	return df;
}

private int getUltra65(ISASConnection dbc, Hashtable par,
	 String terzolivello) throws Exception {

	int numero65 = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectUltra65(dbc, par, terzolivello));
	if (dbrec != null )
		numero65 = parteIntera(dbrec.get("sessanta5"));
	return numero65;
}

private int getAssistiti(ISASConnection dbc, Hashtable par,
	 String terzolivello) throws Exception {

	int numeroass = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectAssistiti(dbc, par, terzolivello));
	if (dbrec != null )
		numeroass = parteIntera(dbrec.get("assistiti"));
	return numeroass;
}


private int getAccessi(ISASConnection dbc, Hashtable par,
	 String terzolivello) throws Exception {

	int numeroacc = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectAccessi(dbc, par, terzolivello));
	if (dbrec != null )
		numeroacc = parteIntera(dbrec.get("accessi"));
	return numeroacc;
}

private int getPrestazioni(ISASConnection dbc, Hashtable par,
	 String terzolivello) throws Exception {

	int numeropres = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectPrestazioni(dbc, par, terzolivello));
	if (dbrec != null )
		numeropres = parteIntera(dbrec.get("prestazioni"));
	return numeropres;
}

private int parteIntera(Object campo) {
	int ret=0;
	if (campo!=null) {
		String conta = ""+campo;
                if (conta.indexOf(".")!=-1)
            		conta = conta.substring(0, conta.indexOf("."));
		ret = Integer.parseInt(conta);
	}
	return ret;
}

private int getDimessi(ISASConnection dbc, Hashtable par,
	String terzolivello) throws Exception {

        int dimessi = 0;
        ISASRecord dbrec = dbc.readRecord(
		getSelectDimessi(dbc, par, terzolivello));
	if (dbrec != null && dbrec.get("dimessi")!=null)
		dimessi = parteIntera(dbrec.get("dimessi"));
	return dimessi;
}

private ISASCursor mkZona(ISASConnection dbc,ISASCursor dbcur ,
	Hashtable par) throws Exception {

	try {
		ServerUtility su = new ServerUtility();
		String select="";
		String ragg =(String)par.get("ragg");
		String tabella="";
		if (ragg.equals("C"))
			tabella=" comuni ";
		else
			if (ragg.equals("A"))
				tabella=" areadis ";
		select = "SELECT d.cod_zona ,z.descrizione_zona des_zona, "+
			"c.codice,c.descrizione, d.cod_distr, "+
			"d.des_distr des_distretto "+
			"FROM "+tabella+" c ,distretti d,zone z "+
			"WHERE c.cod_distretto = d.cod_distr AND "+
			"d.cod_zona = z.codice_zona ";

		select = su.addWhere(select, su.REL_AND, "d.cod_zona",
			su.OP_EQ_STR, (String)par.get("zona"));
		select = su.addWhere(select, su.REL_AND, "d.cod_distr",
			su.OP_EQ_STR, (String)par.get("distretto"));
		select = su.addWhere(select, su.REL_AND, "c.codice",
			su.OP_EQ_STR, (String)par.get("pca"));

		String s="";
		while(dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			s = su.addWhere(s, su.REL_OR, "c.codice",
				su.OP_EQ_STR,  (String)dbr.get("codice"));
		}
		select += " AND ("+s+") ORDER BY z.descrizione_zona, "+
			"d.des_distr, c.descrizione, d.cod_zona, "+
			"d.cod_distr, c.codice";
		System.out.println("FoStatDomiciliare.mkZona-->"+select);
		ISASCursor dbcurZona = dbc.startCursor(select);
		return dbcurZona;
	} catch(Exception e) {
		debugMessage("FoStatDomiciliareEJB.mkZona(): "+e);
		throw(e);
	}
}

/**
* genera una riga di intestazione o di totali (azzerandoli)
*/
private void mkRiga(mergeDocument mdoc, String sectionName,
	String tagName, String descrizione, Totali totali) {

	Hashtable ht = new Hashtable();
	ht.put(tagName, descrizione);
	if (totali != null) {
		ht.put("#t0#", ""+totali.t0);
		ht.put("#t1#", ""+totali.t1);
		ht.put("#t2#", ""+totali.t2);
                //Ilaria inizio
                ht.put("#t3#", ""+totali.t3);
                ht.put("#t4#", ""+totali.t4);
                //Ilaria fine
		totali.setZero();
	} else {
		ht.put("#t0#", "0");
		ht.put("#t1#", "0");
		ht.put("#t2#", "0");
                //Ilaria inizio
                ht.put("#t3#", "0");
                ht.put("#t4#", "0");
                //Ilaria fine
        }
	mdoc.writeSostituisci(sectionName, ht);
}


private String getSelectTerritorio(ISASConnection dbc, Hashtable par) {

	String s ="SELECT DISTINCT u.cod_zona, u.des_zona, u.cod_distretto, "+
		"u.des_distretto, u.codice, u.descrizione "+ list +
                "FROM "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, interv i "+ from_uni+from_cont+" WHERE "+
		getCommonWhere(dbc, par)+where_uni+where_cont;
                if(!list.equals(""))    order=" i.int_coduf, t.descrizione,";

	s+=	" ORDER BY "+order+" u.des_zona, u.des_distretto, u.descrizione ,"+
		"u.cod_zona, u.cod_distretto, u.codice";


	debugMessage("FoStatDomiciliareEJB.getSelectTerritorio(): "+s);
	return s;
}

private String getSelectTerritorioComuni(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();
	String s ="SELECT DISTINCT ";
        String ragg = (String)par.get("ragg");
        String campo="";
  if(this.dom_res==null)
  {
        
        if (ragg.equals("A"))
                campo="int_cod_areadis";
	else if (ragg.equals("C"))
                campo="int_cod_comune";
	else
                campo="int_codpres";     
  }else if(this.dom_res.equals("D"))
  {
      if (ragg.equals("A"))
          campo="int_cod_areadis";
else if (ragg.equals("C"))
          campo="int_cod_comune";
else
          campo="int_codpres";    
  }else if(this.dom_res.equals("R"))
  {
      if (ragg.equals("A"))
          campo="int_cod_areadis";
else if (ragg.equals("C"))
          campo="int_cod_comune";
else
          campo="int_codpres";    
  }
        s=s+campo+ " codice "+list+" FROM interv i "+from_uni+from_cont+
                   " WHERE int_ambdom='D' "+where_uni+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	s= su.addWhere(s, su.REL_AND, campo,
		su.OP_EQ_STR, (String)par.get("pca"));

	if (!tipo.equals("00"))
            s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
                            su.OP_EQ_STR, (String)par.get("figprof"));

/*CJ 14/02/2006 Commento la parte UNION perch� tira fuori ci comuni delle cartella che
hanno un contatto non ancora chiuso o chiuso nel range di date, non tenendo conto
dell'esistenza di un accesso. I comuni e le aree distrettuali li tiro fuori direttamente
dalla tabella interv


    	 s =s+" UNION SELECT DISTINCT ";
         String campo2="";
         if (ragg.equals("C"))
                campo2= " a.dom_citta ";
         else if (ragg.equals("A"))
                campo2=" a.dom_areadis ";
	 s=s+campo2+" codice FROM  contsan n, anagra_c a "+from_cont+
                " WHERE n.n_cartella = a.n_cartella ";

         if(!from_cont.equals("")){
          if(tipo.equals("01")){
            s+=" AND n.n_cartella=co.n_cartella "+
                 " AND n.n_contatto=co.n_contatto";
            if(!motivo.equals("TUTTO"))
              s+=" AND co.motivo='"+motivo+"'";
          }else if(tipo.equals("02")){
            s+=" AND n.n_cartella=sk.n_cartella "+
               " AND n.n_contatto=sk.n_contatto";
            if(!motivo.equals("TUTTO"))
              s+=" AND sk.ski_motivo='"+motivo+"'";
          }
        }

	s= su.addWhere(s, su.REL_AND, campo2,
		su.OP_EQ_STR, (String)par.get("pca"));

         String finale="";
         if (tipo.equals("01"))
            finale="sociale";
         else if (tipo.equals("02"))
             finale="infer";
         else if (tipo.equals("03"))
            finale="medico";
         else if (tipo.equals("04"))
            finale="fisiot";
        s += " AND a.data_variazione IN ("+
             " SELECT MAX (anagra_c.data_variazione)"+
             " FROM anagra_c WHERE anagra_c.n_cartella=n.n_cartella )";
        s = su.addWhere(s, su.REL_AND, "n.data_chius_"+finale,
            su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
        s = su.addWhere(s, su.REL_AND, "n.data_chius_"+finale,
            su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));
*/
	debugMessage("FoStatDomiciliareEJB.getSelectTerritorioComuni(): "+s);
	return s;
}

private void FaiCriteri(ISASConnection dbc, Hashtable par){
    if(!((String)par.get("unifun")).equals("") &&
      !((String)par.get("unifun")).equals("TUTTO")){
      list = ",i.int_coduf, t.descrizione des_unita ";
      from_uni = ",tabuf t";
      where_uni = " AND i.int_coduf=t.codice";
      if(!((String)par.get("unifun")).equals("TUTTO"))
        where_uni += " AND i.int_coduf='"+(String)par.get("unifun")+"'";
    }

    tipo=(String)par.get("figprof");
    motivo=(String)par.get("motivo");
    if(tipo.equals("01")){
      if(!motivo.equals("")){
/*gb 11/06/07 *******
        from_cont=", contatti co ";
        where_cont=" AND i.int_cartella=co.n_cartella "+
                   " AND i.int_contatto=co.n_contatto ";
        if(!motivo.equals("TUTTO"))
          where_cont+=" AND co.motivo='"+motivo+"' ";
*gb 11/06/07: fine *******/
//gb 11/06/07 *******
        from_cont=", ass_progetto co ";
        where_cont=" AND i.int_cartella = co.n_cartella "+
                   " AND i.n_progetto = co.n_progetto ";
        if(!motivo.equals("TUTTO"))
          where_cont+=" AND co.ap_motivo='"+motivo+"' ";
//gb 11/06/07: fine *******
      }
    }else if(tipo.equals("02")){
      if(!motivo.equals("")){
        from_cont=", skinf sk ";
        where_cont=" AND i.int_cartella=sk.n_cartella "+
                   " AND i.int_contatto=sk.n_contatto ";
        if(!motivo.equals("TUTTO"))
          where_cont+=" AND sk.ski_motivo='"+motivo+"' ";
      }
    }
  //Minerba 06/03/2013		
   String qualifica = (String) par.get("qualifica");
   if (!(tipo.equals("00"))){
	if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
		where_cont +=" AND i.int_qual_oper='"+qualifica+"'";
	}
   }//fine Minerba 06/03/2013
  }

private String getSelectAccessi(ISASConnection dbc, Hashtable par,
	String terzolivello) {

	ServerUtility su = new ServerUtility();
        String s ="SELECT COUNT(i.int_contatore) accessi "+
                  "FROM interv i "+from_cont+" WHERE i.int_ambdom = 'D' "+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	if (!tipo.equals("00"))
          s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper", su.OP_EQ_STR, tipo);

	String ragg = (String)par.get("ragg");
//	if (ragg.equals("A"))
//  	  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("C"))
//          s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("P"))
//  	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,su.OP_EQ_STR,terzolivello);

		if(this.dom_res==null)
		{
		if(ragg.equals("A"))
 		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                    su.OP_EQ_STR,terzolivello);
        else if(ragg.equals("C"))
        	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                    su.OP_EQ_STR,terzolivello);
        else if (ragg.equals("P"))
    	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
    			  su.OP_EQ_STR,terzolivello);
		}
		else if(this.dom_res.equals("D"))
		{
			if(ragg.equals("A"))
            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                        su.OP_EQ_STR,terzolivello);
            else if(ragg.equals("C"))
            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                        su.OP_EQ_STR,terzolivello);
        }
		else if(this.dom_res.equals("R"))
			{
				if(ragg.equals("A"))
	            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
	                        su.OP_EQ_STR,terzolivello);
	            else if(ragg.equals("C"))
	            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
	                        su.OP_EQ_STR,terzolivello);
	            
			}
          
				
	
	debugMessage("FoStatDomiciliareEJB.getSelectAccessi(): "+s);
	return s;
}

private String getSelectPrestazioni(ISASConnection dbc, Hashtable par,
	String terzolivello) {

	ServerUtility su = new ServerUtility();
	String s = "SELECT SUM( p.pre_numero) prestazioni "+
                   "FROM intpre p, interv i "+from_cont+
                   " WHERE i.int_contatore = p.pre_contatore AND "+
                   " i.int_anno = p.pre_anno "+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	if (!((String)par.get("figprof")).equals("00"))
		s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
			su.OP_EQ_STR, (String)par.get("figprof"));

	s = su.addWhere(s, su.REL_AND, "i.int_ambdom", su.OP_EQ_STR, "D");

	String ragg = (String)par.get("ragg");
//	if (ragg.equals("A"))
//		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",
//			su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("C"))
//		s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",
//			su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("P"))
//		s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
//			su.OP_EQ_STR,terzolivello);
	
    //Aggiunto Controllo Domicilio/Residenza (BYSP)
	if(this.dom_res==null)
	{
	if(ragg.equals("A"))
		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                su.OP_EQ_STR,terzolivello);
    else if(ragg.equals("C"))
    	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                su.OP_EQ_STR,terzolivello);
    else if (ragg.equals("P"))
	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
			  su.OP_EQ_STR,terzolivello);
	}
	else if(this.dom_res.equals("D"))
	{
		if(ragg.equals("A"))
        s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                    su.OP_EQ_STR,terzolivello);
        else if(ragg.equals("C"))
        s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                    su.OP_EQ_STR,terzolivello);
    }
	else if(this.dom_res.equals("R"))
		{
			if(ragg.equals("A"))
            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                        su.OP_EQ_STR,terzolivello);
            else if(ragg.equals("C"))
            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                        su.OP_EQ_STR,terzolivello);
            
		}
      

	debugMessage("FoStatDomiciliareEJB.getSelectPrestazioni(): "+s);
	return s;
}

private String getSelectAssistiti(ISASConnection dbc, Hashtable par,
	String terzolivello) {

	ServerUtility su = new ServerUtility();
	String s = "SELECT COUNT (DISTINCT i.int_cartella) assistiti "+
    		   "FROM interv i "+from_cont+" WHERE i.int_ambdom='D' "+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	if (!((String)par.get("figprof")).equals("00"))
		s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
			su.OP_EQ_STR, (String)par.get("figprof"));

//Jessy 18/11/04 se terzolivello � vuoto � perch� mi sto calcolando i totali
//senza tener conto del raggruppamento
        if (!(terzolivello.equals(""))){
          String ragg = (String)par.get("ragg");
//          if (ragg.equals("A"))
//                  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",
//                          su.OP_EQ_STR, terzolivello);
//          else if (ragg.equals("C"))
//                  s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",
//                          su.OP_EQ_STR, terzolivello);
//          else if (ragg.equals("P"))
//                  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
//                          su.OP_EQ_STR,terzolivello);
          
          //Aggiunto Controllo Domicilio/Residenza (BYSP)
      	if(this.dom_res==null)
      	{
      	if(ragg.equals("A"))
      		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                      su.OP_EQ_STR,terzolivello);
          else if(ragg.equals("C"))
          	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                      su.OP_EQ_STR,terzolivello);
          else if (ragg.equals("P"))
      	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
      			  su.OP_EQ_STR,terzolivello);
      	}
      	else if(this.dom_res.equals("D"))
      	{
      		if(ragg.equals("A"))
              s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                          su.OP_EQ_STR,terzolivello);
              else if(ragg.equals("C"))
              s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                          su.OP_EQ_STR,terzolivello);
          }
      	else if(this.dom_res.equals("R"))
      		{
      			if(ragg.equals("A"))
                  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                              su.OP_EQ_STR,terzolivello);
                  else if(ragg.equals("C"))
                  s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                              su.OP_EQ_STR,terzolivello);
                  
      		}
                     
          
        }
	debugMessage("FoStatDomiciliareEJB.getSelectAssititi(): "+s);
	return s;
}

/**
* restituisce la parte comune della clausola WHERE
*/
private String getCommonWhere(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();

	String s = su.addWhere("", su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	if (!((String)par.get("figprof")).equals("00"))
		s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
			su.OP_EQ_STR, (String)par.get("figprof"));

	String ragg = (String)par.get("ragg");
	s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
//        if (ragg.equals("A"))
//		s = su.addWhere(s, su.REL_AND, "u.codice",
//			su.OP_EQ_NUM, "i.int_cod_areadis");
//	else if (ragg.equals("C"))
//		s = su.addWhere(s, su.REL_AND, "u.codice",
//			su.OP_EQ_NUM, "i.int_cod_comune");
//	else
//		s = su.addWhere(s, su.REL_AND, "u.codice",
//			su.OP_EQ_NUM, "i.int_codpres");
	
    //Aggiunto Controllo Domicilio/Residenza (BYSP)
	if(this.dom_res==null)
	{
        if (ragg.equals("A"))
    		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_cod_areadis");
    	else if (ragg.equals("C"))
    		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_cod_comune");
    	else
    		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_codpres");
	}
	else if(this.dom_res.equals("D"))
	{
		if (ragg.equals("A"))
	
  		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_cod_areadis");
    	else if (ragg.equals("C"))
    		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_cod_comune");
	}
	else if(this.dom_res.equals("R"))
	
		{
		if (ragg.equals("A"))
		
  		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_cod_areadis");
    	else if (ragg.equals("C"))
    		s = su.addWhere(s, su.REL_AND, "u.codice",
    			su.OP_EQ_NUM, "i.int_cod_comune");
            
		
		}
	

	s = su.addWhere(s, su.REL_AND, "i.int_ambdom",
		su.OP_EQ_STR, "D");
	s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));

	return s;
}

/**
* stampa elenco: sezione layout del documento
*/
private void mkLayout(ISASConnection dbc, Hashtable par, mergeDocument mdoc) {

	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();

	ht.put("#conf_txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));
	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	ht.put("#figura_professionale#",
		getFiguraProfessionale(
			par.get("figprof")).toUpperCase());

	mdoc.writeSostituisci("layout",ht);
}

/**
* restituisce un parametro data come stringa nel formato gg/mm/aaaa
*/
private String getStringDate(Hashtable par, String k) {
	try {
		String s = (String)par.get(k);
		return s.substring(8,10) + "/"+ s.substring(5,7) +
			"/" + s.substring(0,4);
	} catch(Exception e) {
		debugMessage("FoStatDomiciliareEJB.getStringDate("+
			par+", "+k+"): "+e);
		return "";
	}
}
/**
* restituisce un parametro data come stringa nel formato gg/mm/aaaa
*/
/**
* restituisce la decodifica della figura professionale
*/
private String getFiguraProfessionale(Object Otipo) {
	String fp = "";
	try {
                String tipo=(String)Otipo;
		if (tipo.equals("00"))
			fp = "TUTTE LE FIGURE PROFESSIONALI";
		else if (tipo.equals("01"))
			fp = "Assistente sociale";
		else if (tipo.equals("02"))
			fp = "Infermiere";
		else if (tipo.equals("03"))
			fp = "Medico";
		else if (tipo.equals("04"))
			fp = "Fisioterapista";
		else if (tipo.equals("52"))
			fp = "Oncologo";
		else if (tipo.equals("98"))
			fp = "Medico specialista";
		else
			fp = "FIGURA PROFESSIONALE NON VALIDA";
	} catch(Exception e) {
		fp = "FIGURA PROFESSIONALE ERRATA";
	}
	return fp;
}
//minerba 06/03/2012
private String getQualifica(Object Oqualifica) {
	String fp = "";
	try {
                String qualifica=(String)Oqualifica;
                if (qualifica.equals("TUTTO"))
					fp = " TUTTE LE QUALIFICHE ";
				else if (qualifica.equals("F"))
					fp = "LOGOPEDISTA ";
				else if (qualifica.equals("H"))
					fp = "PSICOLOGO ";
				else if (qualifica.equals("1"))
					fp = "ASSISTENTE SOCIALE ";
				else if (qualifica.equals("2"))
					fp = "INFERMIERE ";
				else if (qualifica.equals("A"))
					fp = "OSA ";
				else if (qualifica.equals("O"))
					fp = "OTA ";
				else if (qualifica.equals("4"))
					fp = "FISIATRA ";
				else if (qualifica.equals("3"))
					fp = "FISIOTERAPISTA ";
				else if (qualifica.equals("5"))
					fp = "MEDICO ";
				else if (qualifica.equals("6"))
					fp = "AMMINISTRATIVO 1.LIV ";
				else if (qualifica.equals("7"))
					fp = "AMMINISTRATIVO 2.LIV ";
				else if (qualifica.equals("8"))
					fp = "CAPO SALA ";
				else if (qualifica.equals("9"))
					fp = "O.S.S. ";
				else if (qualifica.equals("G"))
					fp = "MED. MEDICINA GENER. ";		
				else
					fp = "TUTTE LE QUALIFICHE";
	} catch(Exception e) {
		fp = "QUALIFICA ERRATA";
	}
	return fp;
}//fine minerba

/*******************METODI PER UNITA DI VALUTAZIONE*****************************/
private void mkAccessBodyUnita(ISASConnection dbc, ISASCursor dbcur,
	Hashtable par, mergeDocument mdoc) throws Exception {
String punto = MIONOME  + "mkAccessBodyUnita ";
stampa(punto + "Inizio con dati>"+ par+"<\n");
	boolean first_time = true;
        String o_unit = "*", c_unit = "";	// old and current values
	String o_zona = "*", c_zona = "";	// old and current values
	String o_dist = "*", c_dist = "";	// old and current values

	Totali totali = new Totali();
        Totali t_unit = new Totali();
	Totali t_zona = new Totali();
	Totali t_dist = new Totali();
        System.out.println("******************* ");
	/*
        *  ILARIA INIZIO:se e' stato scelto come raggruppamento
        *  il presidio non devo stampare la colonna dei dimessi
        *  perche' per calcolare i dimessi vado su contsan_n e non ho
        *  l 'informazione del presidio
        */
        String ragg="";
        String sezione="";
        if (par.get("ragg")!=null )
                ragg=(String)par.get("ragg");
        if (ragg.equals("P"))
              sezione="_pres";
        //ILARIA FINE
        Hashtable htt = new Hashtable();
	htt.put("#titolo#", getTitolo(par));
	mdoc.writeSostituisci("tabella1"+sezione, htt);
	Hashtable prtDati = new Hashtable();
	while (dbcur.next()) {
            ISASRecord dbr=dbcur.getRecord();
            //System.out.println("ISASRecord: "+dbr.getHashtable().toString());
            c_unit = (String)dbr.get("des_unita");
            c_zona = (String)dbr.get("des_zona");
            c_dist = (String)dbr.get("des_distretto");

            prtDati.put("#unita#", c_unit);
            prtDati.put("#c_zona#", c_zona);
            prtDati.put("#c_distretto#", c_dist);
           
            if (! o_unit.equals(c_unit)) {
                System.out.println("****Unita diverse");
                if (first_time) {
                        first_time = ! first_time;
                } else {
                        System.out.println("****Non � la prima volta");
                        t_zona.add(t_dist);
                        mkRiga(mdoc, "tot_dist1"+sezione,
                                "#descr#", o_dist, t_dist);
                        t_unit.add(t_zona);
                        mkRiga(mdoc, "tot_zona1"+sezione,
                                "#descr#", o_zona, t_zona);
                        totali.add(t_unit);
                        mkRiga(mdoc, "tot_unita1"+sezione,
                                "#descr#", o_unit, t_unit);
                }
                mkRiga(mdoc, "unita1"+sezione, "#descr#", c_unit, null);
                mkRiga(mdoc, "zona1"+sezione, "#descr#", c_zona, null);
                mkRiga(mdoc, "dist1"+sezione, "#descr#", c_dist, null);
            }else if (! o_zona.equals(c_zona)) {
                t_zona.add(t_dist);
                  mkRiga(mdoc, "tot_dist1"+sezione,
                          "#descr#", o_dist, t_dist);
                t_unit.add(t_zona);
                  mkRiga(mdoc, "tot_zona1"+sezione,
                          "#descr#", o_zona, t_zona);
            }else if (! o_dist.equals(c_dist)) {
                t_zona.add(t_dist);
                mkRiga(mdoc, "tot_dist1"+sezione,
                        "#descr#", o_dist, t_dist);
                mkRiga(mdoc, "dist1"+sezione,
                        "#descr#", c_dist, null);
            }

            int n0= getAccessiUnita(dbc, par,  (String)dbr.get("codice"),
                                          (String)dbr.get("int_coduf"));
            int n1= getPrestazioniUnita(dbc, par,  (String)dbr.get("codice"),
                                              (String)dbr.get("int_coduf"));
            //Ilaria-inizio
            //int n2 = parteIntera(dbr.get("assistiti"));
            int n2= getAssistitiUnita(dbc, par,  (String)dbr.get("codice"),
                                            (String)dbr.get("int_coduf"));
            //vado a prendere gli ultra 65
            int n3= getUltra65Unita(dbc, par,  (String)dbr.get("codice"),
                                               (String)dbr.get("int_coduf"));
            //vado a prendere i dimessi nel caso in cui il raggruppamento
            //� comune o area distrettuale
            int n4=0;
            if (par.get("ragg")!=null && !((String)par.get("ragg")).equals("P"))
                   n4= getDimessi(dbc, par, (String)dbr.get("codice"));
            //Ilaria-fine

            Hashtable ht = new Hashtable();
            ht.put("#descr#", (String)dbr.get("descrizione"));

            prtDati.put("#des_pace#", (String)dbr.get("descrizione"));
            prtDati.put("#t0#", ""+n0);
            prtDati.put("#t1#", ""+n1);
            prtDati.put("#t2#", ""+n2);
            prtDati.put("#t3#", ""+n3);
            prtDati.put("#t4#", ""+n4);
            
            ht.put("#t0#", ""+n0);
            ht.put("#t1#", ""+n1);
            ht.put("#t2#", ""+n2);
            ht.put("#t3#", ""+n3);
            ht.put("#t4#", ""+n4);
            mdoc.writeSostituisci("comu1"+sezione, ht);
            
            mdoc.writeSostituisci("tabella1_corpo"+sezione, prtDati);

            t_dist.t0 += n0;
            t_dist.t1 += n1;
            t_dist.t2 += n2;
            t_dist.t3 += n3;
            t_dist.t4 += n4;
            o_unit = c_unit;
            o_zona = c_zona;
            o_dist = c_dist;
	}
	if (! first_time) {
                System.out.println("****if non � la prima volta");
		t_zona.add(t_dist);
		mkRiga(mdoc, "tot_dist1"+sezione, "#descr#", c_dist, t_dist);
                t_unit.add(t_zona);
		mkRiga(mdoc, "tot_zona1"+sezione, "#descr#", c_zona, t_zona);
		totali.add(t_unit);
		mkRiga(mdoc, "tot_unita1"+sezione, "#descr#", c_unit, t_unit);
	}
	mkRiga(mdoc, "tot_gene1"+sezione, "#descr#", "", totali);
	
	
	
	mdoc.write("finetab1");
        if(((String)par.get("ragg")).equals("P")){
          Hashtable ht = new Hashtable();
          ht.put("#d3#", ""+getAssistiti(dbc, par, ""));
          ht.put("#d4#", ""+getUltra65(dbc, par, ""));
          mdoc.writeSostituisci("tot_depurato",ht);
        }
	//mdoc.write("taglia");
}

private void stampa(String messaggio) {
	System.out.println(messaggio);
}

private int getAssistitiUnita(ISASConnection dbc, Hashtable par,
	 String terzolivello, String unita) throws Exception {

	int numeroass = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectAssistitiUnita(dbc, par, terzolivello, unita));
	if (dbrec != null )
		numeroass = parteIntera(dbrec.get("assistiti"));
	return numeroass;
}


private int getAccessiUnita(ISASConnection dbc, Hashtable par,
	 String terzolivello, String unita) throws Exception {

	int numeroacc = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectAccessiUnita(dbc, par, terzolivello, unita));
	if (dbrec != null )
		numeroacc = parteIntera(dbrec.get("accessi"));
	return numeroacc;
}

private int getPrestazioniUnita(ISASConnection dbc, Hashtable par,
	 String terzolivello, String unita) throws Exception {

	int numeropres = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectPrestazioniUnita(dbc, par, terzolivello, unita));
	if (dbrec != null )
		numeropres = parteIntera(dbrec.get("prestazioni"));
	return numeropres;
}

private String getSelectAccessiUnita(ISASConnection dbc, Hashtable par,
	String terzolivello, String unita) {

	ServerUtility su = new ServerUtility();
        String s ="SELECT COUNT(i.int_contatore) accessi "+
                  "FROM interv i "+ from_uni+from_cont+
                  " WHERE i.int_ambdom = 'D'" + where_uni+where_cont;
 
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));
        s = su.addWhere(s, su.REL_AND, "i.int_coduf" , su.OP_EQ_STR,unita);

	if (!tipo.equals("00"))
	  s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",su.OP_EQ_STR, tipo);

	String ragg = (String)par.get("ragg");
//	if (ragg.equals("A"))
//  	  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("C"))
//  	  s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("P"))
//  	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,su.OP_EQ_STR,terzolivello);
	
    //Aggiunto Controllo Domicilio/Residenza (BYSP)
  	if(this.dom_res==null)
  	{
  	if(ragg.equals("A"))
  		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                  su.OP_EQ_STR,terzolivello);
      else if(ragg.equals("C"))
      	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                  su.OP_EQ_STR,terzolivello);
      else if (ragg.equals("P"))
  	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
  			  su.OP_EQ_STR,terzolivello);
  	}
  	else if(this.dom_res.equals("D"))
  	{
  		if(ragg.equals("A"))
          s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                      su.OP_EQ_STR,terzolivello);
          else if(ragg.equals("C"))
          s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                      su.OP_EQ_STR,terzolivello);
      }
  	else if(this.dom_res.equals("R"))
  		{
  			if(ragg.equals("A"))
              s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                          su.OP_EQ_STR,terzolivello);
              else if(ragg.equals("C"))
              s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                          su.OP_EQ_STR,terzolivello);
              
  		}
                
	
	
	debugMessage("FoStatDomiciliareEJB.getSelectAccessiUnita(): "+s);
	return s;
}

private String getSelectPrestazioniUnita(ISASConnection dbc, Hashtable par,
	String terzolivello, String unita) {

	ServerUtility su = new ServerUtility();
	String s = "SELECT SUM( p.pre_numero) prestazioni "+
		   "FROM intpre p, interv i "+from_uni+from_cont+
		   " WHERE i.int_contatore = p.pre_contatore AND "+
		   " i.int_anno = p.pre_anno "+ where_uni+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));
        s = su.addWhere(s, su.REL_AND, "i.int_coduf" , su.OP_EQ_STR,unita);

	if (!tipo.equals("00"))
          s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",su.OP_EQ_STR, tipo);

	s = su.addWhere(s, su.REL_AND, "i.int_ambdom", su.OP_EQ_STR, "D");

	String ragg = (String)par.get("ragg");
//	if (ragg.equals("A"))
//    	  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("C"))
//	  s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, terzolivello);
//	else if (ragg.equals("P"))
//  	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,su.OP_EQ_STR,terzolivello);
	
    //Aggiunto Controllo Domicilio/Residenza (BYSP)
  	if(this.dom_res==null)
  	{
  	if(ragg.equals("A"))
  		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                  su.OP_EQ_STR,terzolivello);
      else if(ragg.equals("C"))
      	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                  su.OP_EQ_STR,terzolivello);
      else if (ragg.equals("P"))
  	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
  			  su.OP_EQ_STR,terzolivello);
  	}
  	else if(this.dom_res.equals("D"))
  	{
  		if(ragg.equals("A"))
          s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                      su.OP_EQ_STR,terzolivello);
          else if(ragg.equals("C"))
          s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                      su.OP_EQ_STR,terzolivello);
      }
  	else if(this.dom_res.equals("R"))
  		{
  			if(ragg.equals("A"))
              s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                          su.OP_EQ_STR,terzolivello);
              else if(ragg.equals("C"))
              s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                          su.OP_EQ_STR,terzolivello);
              
  		}
                
      		

	debugMessage("FoStatDomiciliareEJB.getSelectPrestazioniUnita(): "+s);
	return s;
}

private String getSelectAssistitiUnita(ISASConnection dbc, Hashtable par,
	String terzolivello, String unita) {

	ServerUtility su = new ServerUtility();
	String s = "SELECT COUNT (DISTINCT i.int_cartella) assistiti "+
		   "FROM interv i "+from_uni+from_cont+
                   " WHERE i.int_ambdom='D' " + where_uni+where_cont;

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));
        s = su.addWhere(s, su.REL_AND, "i.int_coduf" , su.OP_EQ_STR,unita);

	if (!tipo.equals("00"))
          s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",su.OP_EQ_STR, tipo);

//Jessy 18/11/04 se terzolivello � vuoto � perch� mi sto calcolando i totali
//senza tener conto del raggruppamento
        if (!(terzolivello.equals(""))){
          String ragg = (String)par.get("ragg");
//          if (ragg.equals("A"))
//            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",su.OP_EQ_STR, terzolivello);
//          else if (ragg.equals("C"))
//            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, terzolivello);
//          else if (ragg.equals("P"))
//            s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,su.OP_EQ_STR,terzolivello);
        	
            //Aggiunto Controllo Domicilio/Residenza (BYSP)
          	if(this.dom_res==null)
          	{
          	if(ragg.equals("A"))
          		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                          su.OP_EQ_STR,terzolivello);
              else if(ragg.equals("C"))
              	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                          su.OP_EQ_STR,terzolivello);
              else if (ragg.equals("P"))
          	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
          			  su.OP_EQ_STR,terzolivello);
          	}
          	else if(this.dom_res.equals("D"))
          	{
          		if(ragg.equals("A"))
                  s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                              su.OP_EQ_STR,terzolivello);
                  else if(ragg.equals("C"))
                  s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                              su.OP_EQ_STR,terzolivello);
              }
          	else if(this.dom_res.equals("R"))
          		{
          			if(ragg.equals("A"))
                      s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                                  su.OP_EQ_STR,terzolivello);
                      else if(ragg.equals("C"))
                      s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                                  su.OP_EQ_STR,terzolivello);
                      
          		}
                           	
        	
        }
	debugMessage("FoStatDomiciliareEJB.getSelectAssititiUnita(): "+s);
	return s;
}


private int getUltra65Unita(ISASConnection dbc, Hashtable par,
	 String terzolivello, String unita) throws Exception {

	int numero65 = 0;
	ISASRecord dbrec = dbc.readRecord(
		getSelectUltra65Unita(dbc, par, terzolivello, unita));
	if (dbrec != null )
		numero65 = parteIntera(dbrec.get("sessanta5"));
	return numero65;
}

private String getSelectUltra65Unita(ISASConnection dbc, Hashtable par,
	 String terzolivello, String unita) {

	ServerUtility su = new ServerUtility();
	String s ="SELECT COUNT (DISTINCT c.n_cartella ) sessanta5 "+
		  "FROM interv i, cartella c "+from_uni+from_cont+
                  " WHERE i.int_cartella = c.n_cartella "+ where_uni+where_cont;
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM, formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_LE_NUM, formatDate(dbc, (String)par.get("data_fine")));

	s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, "D");
        s = su.addWhere(s, su.REL_AND, "i.int_coduf" , su.OP_EQ_STR,unita);

	if (!tipo.equals("00"))
          s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",su.OP_EQ_STR, tipo);

        if (!(terzolivello.equals(""))){
          String ragg = (String)par.get("ragg");
//          if (ragg.equals("A"))
//            s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis",su.OP_EQ_STR, terzolivello);
//          else if (ragg.equals("C"))
//            s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, terzolivello);
//          else  if (ragg.equals("P"))
//            s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,su.OP_EQ_STR,terzolivello);
          
          //Aggiunto Controllo Domicilio/Residenza (BYSP)
        	if(this.dom_res==null)
        	{
        	if(ragg.equals("A"))
        		s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                        su.OP_EQ_STR,terzolivello);
            else if(ragg.equals("C"))
            	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                        su.OP_EQ_STR,terzolivello);
            else if (ragg.equals("P"))
        	  s = su.addWhere(s, su.REL_AND, "i.int_codpres" ,
        			  su.OP_EQ_STR,terzolivello);
        	}
        	else if(this.dom_res.equals("D"))
        	{
        		if(ragg.equals("A"))
                s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                            su.OP_EQ_STR,terzolivello);
                else if(ragg.equals("C"))
                s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                            su.OP_EQ_STR,terzolivello);
            }
        	else if(this.dom_res.equals("R"))
        		{
        			if(ragg.equals("A"))
                    s = su.addWhere(s, su.REL_AND, "i.int_cod_areadis" ,
                                su.OP_EQ_STR,terzolivello);
                    else if(ragg.equals("C"))
                    s = su.addWhere(s, su.REL_AND, "i.int_cod_comune" ,
                                su.OP_EQ_STR,terzolivello);
                    
        		}
                      
          
        }
	s += " AND c.data_nasc <= "+formatDate(dbc,getData65());
	debugMessage("FoStatDomiciliareEJB.getSelectUltra65Unita(): "+s);
	return s;
}

/*******************FINE UNITA DI VALUTAZIONE***********************************/


}	// End of FoStatDomiciliareEJB class

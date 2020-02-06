package it.caribel.app.sinssnt.bean.modificati;
/**
* CARIBEL S.r.l. - SINSS: produzione elenco assistiti
*
* 22/10/2007 - bargi [ONCOLOGO]
* 06/02/2002 - Giulia Brogi
* 30/06/2003 - Roberto Bonsignori / Francesco Greco
*/

import java.sql.*;
import java.util.*;

import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class FoIAssEleEJB extends SINSSNTConnectionEJB {

	String dom_res;
	String dr;
	private boolean inPiemonte = false;
	
public FoIAssEleEJB() {}

//hash per calcolare il numero totale di assistiti
Hashtable hContaGeneraleS= new Hashtable();
Hashtable hContaGeneraleA= new Hashtable();

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
* restituisce un parametro come stringa
*/
private String getStringField(ISASRecord dbr, String f) {
	try {
		return (dbr.get(f)).toString();
	} catch(Exception e) {
		debugMessage("getStringField("+dbr+", "+f+"): "+e);
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
                if (dbcom!=null && dbcom.get("descrizione")!=null)
		    comune=(String)dbcom.get("descrizione");
	     }
} catch(Exception e) {
	debugMessage("getComune("+dbc+", "+codice+"): "+e);
	return "";
}
   return comune;
}


/**
* restituisce la decodifica del motivo uscita
*/
/*private String getMotivoUscita(ISASRecord dbr) {
	try {

	if(dbr.get("dimissione")==null)
		return "";

		switch (((Integer)dbr.get("dimissione")).intValue()) {
		case 0: return "";
                case 1: return "TRASFERIMENTO";
		case 2: return "DECESSO";
		case 3: return "FINE PRESTAZIONE";
		case 4: return "GUARIGIONE";
		case 5: return "RICOVERO OSPEDALIERO";
                case 6: return "RICOVERO IN RSA";
                case 7: return "RICOVERO IN STRUTTURE RESIDENZIALI";
                case 8: return "ALTRI PROFILI DI ASSISTENZA DOMICILIARE";
                case 9: return "ALTRO";
		default : return " ";
		}
	} catch(Exception e) {
		return "";
	}
}
*/
private String getMotivoUscita(ISASRecord dbr,ISASConnection dbc,String tipo_oper)
throws SQLException{
        String decod="";
        try {
              String tipo="";
              if (tipo_oper.equals("01"))
                    tipo="A";
              else if (tipo_oper.equals("02"))
                    tipo="I";
              else if (tipo_oper.equals("03"))
                    tipo="M";
              else if (tipo_oper.equals("04"))
                    tipo="F";
              else if (tipo_oper.equals("52"))
                  tipo="O";
              else return "";
              if(dbr.get("dimissione")!=null)
              {
                 String codice=""+ dbr.get("dimissione");
                 String sel="SELECT tab_descrizione FROM tab_voci "+
                            " WHERE tab_cod='"+tipo+"CHIUS' AND tab_val='"+codice +"'";
              //   System.out.println("MOTIVO CHIUSURA-->"+sel);
                 ISASRecord dbDecod=dbc.readRecord(sel);
                 if (dbDecod!=null && dbDecod.get("tab_descrizione")!=null)
                 {
                     decod=(String)dbDecod.get("tab_descrizione");
                     if(decod.trim().equals("."))decod="";
                 }
              }
          return decod;
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.getMotivoUscita(): "+e);
		throw new SQLException("Errore eseguendo getMotivoUscita()");
	}
}
/**
* restituisce la decodifica della figura professionale
*/
private String getFiguraProfessionale(String tipo) {
	String fp = "";
	try {
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
		else
			fp = "";
	} catch(Exception e) {
		fp = "FIGURA PROFESSIONALE ERRATA";
	}
	return fp;
}

/**
* restituisce la select per la stampa sintetica.
*/

private String getSelectSintetica(ISASConnection dbc, Hashtable par) throws Exception{
System.out.println("parametri FoIAssEleEJB.getSelectSintetica(): "+par.toString());

if (par.get("piem")!=null && par.get("piem").toString().equals("S")) this.inPiemonte = true;
else { ISASRecord dbr = dbc.readRecord("select conf_txt from conf where conf_key = 'ADRSA_UBIC'");
if (dbr!=null)this.inPiemonte = dbr.get("conf_txt").toString().trim().equals("3");
} 
 
 
String figprof="";
if(!par.get("figprof").equals("00"))
	figprof=(String)par.get("figprof");
String tipocura = par.get("tc").toString();
String livello_ass=(String)par.get("motivo");
	String tabella = "";
	String campo = "";
	if (figprof.equals("02"))  
	{tabella = "skinf"; campo = "ski_tipocura";}
	else if (figprof.equals("03")) {tabella = "skmedico"; campo = "skm_tipocura";}
	String s="";
	if (inPiemonte && !tipocura.equals("TUTTO") && (figprof.equals("02")||figprof.equals("03")))
	
	s =	"SELECT DISTINCT "+
		"r.int_cartella, r.int_tipo_oper, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"u.descrizione, u.des_zona, t."+campo+", u.des_distretto "+
		"FROM interv r, cartella c, "+tabella+" t, "+
		((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
		else
		 s = "SELECT DISTINCT "+
		"r.int_cartella, r.int_tipo_oper, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"u.descrizione, u.des_zona, u.des_distretto "+
		"FROM interv r, cartella c, "+
		""+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
		if (livello_ass!=null && !livello_ass.equals("") && !livello_ass.equals("-1"))
			s +=" ,rm_skso rm, rm_skso_mmg m";
	try {
		String w = getSelectParteWhereSint(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.getSelectSintetica(): "+e);
		e.printStackTrace();
	}
	s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione, "+
		" r.int_tipo_oper,c.cognome";
	debugMessage("FoIAssEleEJB.getSelectSintetica(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhereSint(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();
String tabella = "";
	String campo = "";
	String campo_motivo = "";
String figprof="";
if(!par.get("figprof").equals("00"))
	figprof=(String)par.get("figprof");
	
	if (figprof.equals("02")) 
	{tabella = "skinf"; campo = "ski_tipocura";}
	else if (figprof.equals("03")) {tabella = "skmedico"; campo = "skm_tipocura";}
	
	if (figprof.equals("02")) 
	{tabella = "skinf"; campo_motivo = "ski_motivo";}
	else if (figprof.equals("03")) {tabella = "skmedico"; campo_motivo = "skm_motivo";}
	
	String tipocura = par.get("tc").toString();
	String s = su.addWhere("", su.REL_AND, "r.int_tipo_oper",
		su.OP_EQ_STR, figprof);

	s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));

	String scr = (String)par.get("ragg");//RAGGRUPPAMENTO
	s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);

                //RAGGRUPPAMENTO
//                if (scr!=null && scr.equals("C"))
//                  s += " AND r.int_cod_comune=u.codice ";
//                else if (scr!=null && scr.equals("A"))
//                  s += " AND r.int_cod_areadis=u.codice ";
//                else if (scr!=null && scr.equals("P"))
//                  s += " AND u.codice=r.int_codpres ";
	
	if(this.dom_res==null)
    {
    	
    if (scr!=null && scr.equals("C"))
      s += " AND r.int_cod_comune=u.codice ";
    else if (scr!=null && scr.equals("A"))
      s += " AND r.int_cod_areadis=u.codice ";
    else if (scr!=null && scr.equals("P"))
      s += " AND u.codice=r.int_codpres ";
    }
    else if (this.dom_res.equals("D"))
    		{
        if (scr!=null && scr.equals("C"))
            s += " AND r.int_cod_comune=u.codice ";
          else if (scr!=null && scr.equals("A"))
            s += " AND r.int_cod_areadis=u.codice ";

          }
    else if (this.dom_res.equals("R"))
    		{
    	if (scr!=null && scr.equals("C"))
            s += " AND r.int_cod_comune=u.codice ";
          else if (scr!=null && scr.equals("A"))
            s += " AND r.int_cod_res_areadis=u.codice ";

    		}
	

	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_GE_NUM,
		formatDate(dbc, (String)par.get("data_inizio")));

	if (! s.equals("")) s += " AND ";
	s += " r.int_cartella = c.n_cartella";
	
	if (inPiemonte && !tipocura.equals("TUTTO") && (figprof.equals("02")||figprof.equals("03")))
	{

		s += " AND r.int_cartella=t.n_cartella ";
		s += " AND r.int_contatto=t.n_contatto ";
		
	if (figprof.equals("02")||figprof.equals("03"))
	{
		if (!tipocura.equals("TUTTO") && !tipocura.equals("NESDIV"))
		s = su.addWhere(s, su.REL_AND, "t."+campo,
		su.OP_EQ_STR, tipocura);
	}
	}
	String livello_ass=(String)(par.get("motivo"));
	String data_ini=(String)par.get("data_inizio");
	String data_fine=(String)par.get("data_fine");
    if (livello_ass!=null && !livello_ass.equals("-1")) 
    	s +=" AND rm.n_cartella=r.int_cartella"+
    			" AND rm.data_presa_carico_skso >= " + formatDate(dbc,data_ini) + 
    			" AND rm.data_presa_carico_skso <= " + formatDate(dbc,data_fine) + 
    			" AND rm.n_cartella=m.n_cartella and rm.id_skso=m.id_skso"+
    			" AND m.tipocura='"+livello_ass+"'";
	return s;
}

/**
* restituisce la select per la stampa analitica.
*/
private String getSelectAnalitica(ISASConnection dbc, Hashtable par) {
this.inPiemonte = par.get("piem").toString().equals("S");
String figprof="";
if(!par.get("figprof").equals("00"))
	figprof=(String)par.get("figprof");
String tipocura = par.get("tc").toString();
	String tabella = "";
	String campo = "";
	String campo_motivo = "";
	if (figprof.equals("02")) 
	{tabella = "skinf"; campo = "ski_tipocura";}
	else if (figprof.equals("03")) {tabella = "skmedico"; campo = "skm_tipocura";}
	String livello_ass=(String)(par.get("motivo"));
	String s="";
	if (inPiemonte && !tipocura.equals("TUTTO") && (figprof.equals("02")||figprof.equals("03")))
		s = "SELECT DISTINCT "+
		"r.int_cartella, r.int_tipo_oper, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"a.dom_citta,a.dom_indiriz,a.indirizzo,a.citta,a.nome_camp, a.telefono1, "+
		"u.descrizione, u.des_zona, u.des_distretto, "+
		"mecogn, menome, t."+campo+
		" FROM interv r, cartella c, "+tabella+" t, anagra_c a,"+
		""+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, medici ";
	else 
	s = "SELECT DISTINCT "+
		"r.int_cartella, r.int_tipo_oper, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"a.dom_citta,a.dom_indiriz,a.indirizzo,a.citta,a.nome_camp, a.telefono1, "+
		"u.descrizione, u.des_zona, u.des_distretto, "+
		"mecogn, menome "+
		"FROM interv r, cartella c, anagra_c a,"+
		""+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, medici ";
		if (livello_ass!=null && !livello_ass.equals("") && !livello_ass.equals("-1"))
			s +=" ,rm_skso rm, rm_skso_mmg m";
	
	try {
		String w = getSelectParteWhereAna(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.getSelectAnalitica(): "+e);
		e.printStackTrace();
	}
	s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione, "+
		" c.cognome";
	debugMessage("FoIAssEleEJB.getSelectAnalitica(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhereAna(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();
String tipocura = par.get("tc").toString();
String figprof="";
String tabella = "";
	String campo = "";
	String campo_motivo = "";
if(!par.get("figprof").equals("00"))
	figprof=(String)par.get("figprof");
	
if (figprof.equals("02")) 
	{tabella = "skinf"; campo = "ski_tipocura";}
else if (figprof.equals("03")) {tabella = "skmedico"; campo = "skm_tipocura";}


if (figprof.equals("02")) {tabella = "skinf"; campo_motivo = "ski_motivo";}
else if (figprof.equals("03")) {tabella = "skmedico"; campo_motivo = "skm_motivo";}
else if (figprof.equals("04")) {tabella = "skfis"; campo_motivo = "skf_motivo";}
else  {tabella = "skfpg"; campo_motivo = "skfpg_motivo";}

	String s = su.addWhere("", su.REL_AND, "r.int_tipo_oper",
		su.OP_EQ_STR, figprof);

	s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));

	String scr = (String)par.get("ragg");
	s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);
                //RAGGRUPPAMENTO
//                if (scr!=null && scr.equals("C"))
//                  s += " AND r.int_cod_comune=u.codice ";
//                else if (scr!=null && scr.equals("A"))
//                  s += " AND r.int_cod_areadis=u.codice ";
//                else if (scr!=null && scr.equals("P"))
//                  s += " AND u.codice=r.int_codpres ";

	if(this.dom_res==null)
    {
    	
    if (scr!=null && scr.equals("C"))
      s += " AND r.int_cod_comune=u.codice ";
    else if (scr!=null && scr.equals("A"))
      s += " AND r.int_cod_areadis=u.codice ";
    else if (scr!=null && scr.equals("P"))
      s += " AND u.codice=r.int_codpres ";
    }
    else if (this.dom_res.equals("D"))
    		{
        if (scr!=null && scr.equals("C"))
            s += " AND r.int_cod_comune=u.codice ";
          else if (scr!=null && scr.equals("A"))
            s += " AND r.int_cod_areadis=u.codice ";

          }
    else if (this.dom_res.equals("R"))
    		{
    	if (scr!=null && scr.equals("C"))
            s += " AND r.int_cod_comune=u.codice ";
          else if (scr!=null && scr.equals("A"))
            s += " AND r.int_cod_areadis=u.codice ";

    		}
	
	
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_GE_NUM,
		formatDate(dbc, (String)par.get("data_inizio")));
	String data_ini=(String)par.get("data_inizio");
	String data_fine=(String)par.get("data_fine");
	if (! s.equals("")) s += " AND ";
	s += "mecodi (+)= a.cod_med "+
		" AND r.int_cartella = c.n_cartella "+
		" AND r.int_cartella = a.n_cartella "+
		" AND a.data_variazione IN ( SELECT MAX (data_variazione) "+
		" FROM anagra_c WHERE a.n_cartella = anagra_c.n_cartella )";
		
		
		if (inPiemonte && !tipocura.equals("TUTTO") && (figprof.equals("02")||figprof.equals("03")))
	{

		s += " AND r.int_cartella=t.n_cartella ";
		s += " AND r.int_contatto=t.n_contatto ";
		
	if (figprof.equals("02")||figprof.equals("03"))
	{
		if (!tipocura.equals("TUTTO") && !tipocura.equals("NESDIV"))
		s = su.addWhere(s, su.REL_AND, "t."+campo,
		su.OP_EQ_STR, tipocura);
	}
	}
		String livello_ass=(String)(par.get("motivo"));
        if (livello_ass!=null && !livello_ass.equals("-1")) 
        	s +=" AND rm.n_cartella=r.int_cartella"+
        			" AND rm.data_presa_carico_skso >= " + formatDate(dbc,data_ini) + 
        			" AND rm.data_presa_carico_skso <= " + formatDate(dbc,data_fine) + 
        			" AND rm.n_cartella=m.n_cartella and rm.id_skso=m.id_skso"+
        			" AND m.tipocura='"+livello_ass+"'";
        	
	return s;
}

/**
* stampa sintetica-analitica: sezione layout del documento
*/
private void mkLayout(ISASConnection dbc,
	Hashtable par, mergeDocument doc) throws Exception{

	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();

	ht.put("#txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));
	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	ht.put("#fig_prof#",
		getFiguraProfessionale(
			(String)par.get("figprof")).toUpperCase());
	if (par.get("piem").equals("S"))
	{
		if (par.get("tc")!=null && !par.get("tc").toString().trim().equals("TUTTO")) ht.put("#tipocura_desc#",ISASUtil.getDecode(dbc,"tab_voci","tab_cod","tab_val","SAOADI",par.get("tc"),"tab_descrizione"));
		else ht.put("#tipocura_desc#","TUTTE");
		if (par.get("tprest")!=null && !par.get("tprest").toString().trim().equals("")) ht.put("#tipoprest_desc#",ISASUtil.getDecode(dbc,"prestaz","prest_cod",par.get("tprest"),"prest_des"));
		else ht.put("#tipoprest_desc#","TUTTE");
	}
	doc.write("layout");
	doc.writeSostituisci("before",ht);
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
	ht.put("#raggruppamento#", ragg);
	ht.put("#descrizione#", cur_comu);
	doc.writeSostituisci("area", ht);
}

/**
* stampa sintetica: sezione inizio tabella
*/
private void mkIniziaTabella(mergeDocument doc, ISASRecord dbr) {

	Hashtable h = new Hashtable();
	if (this.dom_res==null)
	{
		h.put("#dom_res#","Domicilio");
	}
	else h.put("#dom_res#",this.dr);
	doc.writeSostituisci("iniziotab",h);

}

/**
* stampa sintetica: sezione riga tabella
*/
private void mkSinteticaRigaTabella(ISASConnection dbc,
	ISASRecord dbr, mergeDocument doc) {

	Hashtable ht = new Hashtable();
	try {
		ht.put("#assistito#", (String)dbr.get("cogn")+" "+
			(String)dbr.get("nomeut"));
		ht.put("#data_ass#", getDateField(dbr, "data_nasc"));
		if (dbr.get("int_cartella")==null)
			ht.put("#cartella#", " ");
		else
			ht.put("#cartella#",
				((Integer)dbr.get("int_cartella")).toString());

	hContaGeneraleS.put(((Integer)dbr.get("int_cartella")).toString(),"");
	} catch(Exception e) {
		ht.put("#assistito#", "*** ERRORE ***");
		ht.put("#data_ass#"," ");
		ht.put("#cartella#", " ");
	}
	doc.writeSostituisci("assistito", ht);
}

/**
* stampa sintetica: sezione fine tabella
*/
private void mkFineTabella(mergeDocument doc, int conta) {
	doc.write("finetab");

	Hashtable ht = new Hashtable();
	ht.put("#totale#", " Totale n. assistiti: "+conta);
	doc.writeSostituisci("totale", ht);////COMMENTATO IL 04-07-03
}

/**
* stampa sintetica: corpo
*/
private void mkSinteticaBody(ISASConnection dbc, ISASCursor dbcur,
	Hashtable par, mergeDocument doc) throws Exception {

	boolean first_time = true;
	String old_zona = "*", cur_zona = "";
	String old_dist = "*", cur_dist = "";
	String old_comu = "*", cur_comu = "";
	int conta = 0;
	int contaTot = 0;
	try {
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			cur_zona = (String)dbr.get("des_zona");
			cur_dist = (String)dbr.get("des_distretto");
			cur_comu = (String)dbr.get("descrizione");
			if ((! old_zona.equals(cur_zona)) ||
				(! old_dist.equals(cur_dist)) ||
				(! old_comu.equals(cur_comu))
			) {
				if (first_time) {
					first_time = ! first_time;
				} else {
					// chiudi tabella precedente
					mkFineTabella(doc, conta);
					conta = 0;
				}
				if ((! old_zona.equals(cur_zona)) ||
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
			old_zona = cur_zona;
			old_dist = cur_dist;
			old_comu = cur_comu;

			// stampa riga
			mkSinteticaRigaTabella(dbc, dbr, doc);
			conta++;
                        contaTot++;
		}
		if (!first_time) {
			// chiudi tabella precedente
			mkFineTabella(doc, conta);
          	        Hashtable htot = new Hashtable();
  	                htot.put("#totale#", " Totale generale assistiti: "+hContaGeneraleS.size());
	                doc.writeSostituisci("totale", htot);
		}
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}

/**
* stampa sintetica: entry point
*/
public byte[] query_infesint(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	try {
		if (par.get("tc").toString().equals("NESDIV")) par.put("tc","TUTTO");
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
		String tp = par.get("tp")!=null?par.get("tp").toString():"1";	
		myLogin lg = new myLogin();
		String selectedLanguage = (String)par.get(FileMaker.printParamLang);
		lg.put(utente,passwd,selectedLanguage);
		//lg.put(utente,passwd);
		dbc = super.logIn(lg);
		ISASCursor dbcur=dbc.startCursor(getSelectSintetica(dbc,par));
		mkLayout(dbc, par, doc);
		if (dbcur == null) {
			doc.write("messaggio");
			doc.write("finale");
			debugMessage("FoIAssEleEJB.query_infesint(): "+
				"cursore nullo.");
		} else  {
			if (dbcur.getDimension() <= 0) {
				doc.write("messaggio");
			} else  {
			if(tp.equals("1"))
				mkSinteticaBody(dbc, dbcur, par, doc);
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
		debugMessage("FoIAssEleEJB.query_infesint(): documento ["+
			(new String(rit))+"]");
		return rit;
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.query_infesint(): "+e);
		throw new SQLException("Errore eseguendo query_infesint()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				debugMessage("FoIAssEleEJB.query_infesint(): "+e1);
			}
		}
	}
}

/**
* stampa analitica: sezione riga tabella
*/
private void mkAnaliticaRigaTabella(ISASConnection dbc,
	ISASRecord dbr, mergeDocument doc, Hashtable par)
  throws SQLException{

	Hashtable ht1 = new Hashtable();
	Hashtable ht2 = new Hashtable();
	String n_cartella = "";
	String nome_camp = "";
	try {
            try {
                    ht1.put("#nomeut#", (String)dbr.get("cogn")+" "+
                              (String)dbr.get("nomeut"));
                    ht1.put("#data_nasc#", getDateField(dbr, "data_nasc"));
                    try {
                            n_cartella=((Integer)dbr.get("int_cartella")).toString();
                    } catch(Exception e) {
                            n_cartella = "";
                    }
                    ht1.put("#cartella#", n_cartella);

                    if (this.dom_res==null)
                    {
                    ht2.put("#indirizzo#",
                            (String)dbr.get("dom_indiriz"));
                    ht2.put("#citta#",
                            getComune(dbc,(String)dbr.get("dom_citta")));
                    }else if (this.dom_res.equals("D"))
                    {
                        ht2.put("#indirizzo#",
                                (String)dbr.get("dom_indiriz"));
                        ht2.put("#citta#",
                                getComune(dbc,(String)dbr.get("dom_citta")));
                        }else if (this.dom_res.equals("R"))
                        {
                            ht2.put("#indirizzo#",
                                    (String)dbr.get("indirizzo"));
                            ht2.put("#citta#",
                                    getComune(dbc,(String)dbr.get("citta")));
                            }
                    ht2.put("#telefono1#",
                            (String)dbr.get("telefono1"));

                    if(!((String)dbr.get("nome_camp")).equals(""))
                            nome_camp="("+(String)dbr.get("nome_camp")+")";
                    ht2.put("#nome_camp#",nome_camp);

                    ht2.put("#medico#",(String)dbr.get("mecogn")+" "+
                                       (String)dbr.get("menome"));

            hContaGeneraleA.put(((Integer)dbr.get("int_cartella")).toString(),"");
            } catch(Exception e) {
                    ht1.put("#nomeut#", "*** ERRORE ***");
                    ht1.put("#data_nasc#"," ");
                    ht1.put("#cartella#", n_cartella);
                    ht2.put("#indirizzo#", "*** ERRORE ***");
                    ht2.put("#citta#"," ");
                    ht2.put("#nome_camp#", " ");
                    ht2.put("#telefono1#"," ");
                    ht2.put("#medico#", " ");
            }

              doc.writeSostituisci("assistito1", ht1);
              mkAnaliticaContatti(dbc, n_cartella,
                      getStringField(dbr, "int_tipo_oper"), doc, par);
              doc.writeSostituisci("assistito2", ht2);
        }catch(Exception ex){
            System.out.println("ERRORE in mkAnaliticaRigaTabella :"+ex);
        }
}

/**
* stampa analitica: sezione blocco ripetitivo "CONTATTI" riga tabella
*/
private void mkAnaliticaContatti(ISASConnection dbc, String n_cartella,
	String tipo_oper, mergeDocument doc, Hashtable par)
throws SQLException{
	String dt_ini="";
	String dt_fine="";
	Hashtable ht = new Hashtable();
        ISASCursor dbcur=null;
	String strSelect = "";
	try {
		dt_ini = (String)par.get("data_inizio");
		dt_fine = (String)par.get("data_fine");
		strSelect = getSelectContatti(dbc, par, tipo_oper, n_cartella);
		System.out.println("mkAnaliticaContatti/strSelect: " + strSelect);
		if (!strSelect.equals(""))
		   dbcur = dbc.startCursor(strSelect);
		if (dbcur == null) {
			System.out.println("mkAnaliticaContatti/dbcur == null");
			ht.put("#data_in#", " ");
        		ht.put("#data_out#"," ");
        		ht.put("#reg_dimissioni#", " ");
        		doc.writeSostituisci("contatti", ht);
		} else {
			while (dbcur.next()) {
				ISASRecord dbinf = dbcur.getRecord();
				ht.put("#data_in#",
					getDateField(dbinf, "data_inizio"));
				ht.put("#data_out#",
					getDateField(dbinf, "data_fine"));
				ht.put("#reg_dimissioni#",
					getMotivoUscita(dbinf,dbc,tipo_oper));
          			doc.writeSostituisci("contatti", ht);
			}
		}
		if (dbcur != null)
			dbcur.close();
	} catch(Exception e) {
		ht.put("#data_in#", "*** ERRORE ***");
		ht.put("#data_out#"," ");
		ht.put("#reg_dimissioni#", " ");
		doc.writeSostituisci("contatti", ht);
                try{
                if (dbcur!=null)dbcur.close();}
                catch(Exception ex){}
                throw new SQLException("ERRORE IN mkAnaliticaContatti-->"+e);
	}

}

/**
* stampa analitica: corpo
*/
private void mkAnaliticaBody(ISASConnection dbc, ISASCursor dbcur,
	Hashtable par, mergeDocument doc) throws Exception {

	boolean first_time = true;
	String old_zona = "*", cur_zona = "";
	String old_dist = "*", cur_dist = "";
	String old_comu = "*", cur_comu = "";
	int conta = 0;
	int contaTot = 0;
	try {
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			cur_zona = (String)dbr.get("des_zona");
			cur_dist = (String)dbr.get("des_distretto");
			cur_comu = (String)dbr.get("descrizione");
			boolean flag = (! old_zona.equals(cur_zona)) ||
					(! old_dist.equals(cur_dist)) ||
					(! old_comu.equals(cur_comu));
			if (flag) {
				if (first_time) {
					first_time = ! first_time;
				} else {
					mkFineTabella(doc, conta);
					conta = 0;
				}
				if (flag) {
					mkCoordinate(doc,par,
						cur_zona, cur_dist, cur_comu);
				}
				mkIniziaTabella(doc,dbr);
			}
			old_zona = cur_zona;
			old_dist = cur_dist;
			old_comu = cur_comu;

			// stampa riga
			mkAnaliticaRigaTabella(dbc, dbr, doc, par);
			conta++;
                        contaTot++;
		}
		if (!first_time) {
			// chiudi tabella precedente
			mkFineTabella(doc, conta);
          	        Hashtable htot = new Hashtable();
  	                htot.put("#totale#", " Totale generale assistiti: "+hContaGeneraleA.size());
	                doc.writeSostituisci("totale", htot);
		}
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}


private void mkSinteticaExcel(ISASConnection dbc, ISASCursor dbcur, Hashtable par, mergeDocument doc)
    throws Exception{
            Hashtable p = new Hashtable();
            String ragg = faiRaggruppamento((String)par.get("ragg"));
            String figprof = "";
			String tipocura = "";
			p.put("#raggruppamento#", ragg);
            doc.writeSostituisci("iniziotab", p);
			p.clear();
			
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
				figprof = getStringField(dbr, "int_tipo_oper");
				tipocura = par.get("tc")!=null?par.get("tc").toString():"";
                p.put("#descrizione_zona#", dbr.get("des_zona").toString());
                p.put("#des_distr#", dbr.get("des_distretto").toString());
                p.put("#descrizione#",dbr.get("descrizione").toString());
                
                p.put("#nome_ass#", (String)dbr.get("cogn")+" "+(String)dbr.get("nomeut"));
             
		p.put("#data_nasc#", "("+getDateField(dbr, "data_nasc")+")");
		if (dbr.get("int_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
              p.put("#n_cartella#",((Integer)dbr.get("int_cartella")).toString());
                if (inPiemonte && (dbr.get("ski_tipocura")!=null || dbr.get("skm_tipocura")!=null))  p.put("#tipocura#",ISASUtil.getDecode(dbc,"tab_voci","tab_cod","tab_val","SAOADI",dbr.get("ski_tipocura")!=null?dbr.get("ski_tipocura").toString():dbr.get("skm_tipocura").toString(),"tab_descrizione"));
				else p.put("#tipocura#","NON SPECIFICATO");
				
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
			String figprof = "";
			String tipocura = "";
			String indirizzo = "";
			String dom_indiriz= "";
            doc.writeSostituisci("iniziotab", p);
			p.clear();
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
				System.out.println("mkAnaliticaExcel:0 "+dbr.getHashtable().toString());
				System.out.println("mkAnaliticaExcel:1 "+par.toString());
				figprof = getStringField(dbr, "int_tipo_oper");
				tipocura = par.get("tc")!=null?par.get("tc").toString():"";
                p.put("#descrizione_zona#", dbr.get("des_zona").toString());
                p.put("#des_distr#", dbr.get("des_distretto").toString());
                p.put("#descrizione#",dbr.get("descrizione").toString());
                p.put("#nome_ass#", (String)dbr.get("cogn")+" "+(String)dbr.get("nomeut"));
             System.out.println("mkAnaliticaExcel: 2");
		p.put("#data_nasc#", "("+getDateField(dbr, "data_nasc")+")");
		if (dbr.get("int_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
                    p.put("#n_cartella#",((Integer)dbr.get("int_cartella")).toString());
					System.out.println("mkAnaliticaExcel: 3");
              if (inPiemonte && (dbr.get("ski_tipocura")!=null || dbr.get("skm_tipocura")!=null))  p.put("#tipocura#",ISASUtil.getDecode(dbc,"tab_voci","tab_cod","tab_val","SAOADI",dbr.get("ski_tipocura")!=null?dbr.get("ski_tipocura").toString():dbr.get("skm_tipocura").toString(),"tab_descrizione"));
				else p.put("#tipocura#","NON SPECIFICATO");
				System.out.println("mkAnaliticaExcel: 4");
				doc.writeSostituisci("inizio_riga",p);
				p.clear();
				mkAnaliticaContatti(dbc,dbr.get("int_cartella").toString(),dbr.get("int_tipo_oper").toString(), doc, par);
				System.out.println("mkAnaliticaExcel: 5");
				p.put("#mmg#",(dbr.get("menome")!=null?dbr.get("menome").toString():"") +" "+(dbr.get("mecogn")!=null?dbr.get("mecogn").toString():""));
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



/**
* stampa analitica: entry point
*/
public byte[] query_inferef(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	try {
	if (par.get("tc").toString().equals("NESDIV")) par.put("tc","TUTTO");
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
			
		String tp = par.get("tp")!=null?par.get("tp").toString():"1";
		
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);
		ISASCursor dbcur=dbc.startCursor(getSelectAnalitica(dbc,par));
		mkLayout(dbc, par, doc);
		if (dbcur == null) {
			doc.write("messaggio");
			doc.write("finale");
			debugMessage("FoIAssEleEJB.query_inferef(): "+
				"cursore nullo.");
		} else  {
			if (dbcur.getDimension() <= 0) {
				doc.write("messaggio");
			} else  {
			if (tp.equals("1"))
				mkAnaliticaBody(dbc, dbcur, par, doc);
				else mkAnaliticaExcel(dbc, dbcur, par, doc);
			}
			doc.write("finale");
		}
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;

		doc.close();
		byte[] rit = (byte[])doc.get();
		debugMessage("FoIAssEleEJB.query_inferef(): documento ["+
		(new String(rit))+"]");
		return rit;
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.query_inferef(): "+e);
		throw new SQLException("Errore eseguendo query_inferef()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				debugMessage("FoIAssEleEJB.query_inferef(): "+e1);
			}
		}
	}
}

private String getSelectContatti(ISASConnection dbc, Hashtable par,
	String tipo_oper, String n_cartella) {

	String selConta="";
	String dt_ini = (String)par.get("data_inizio");
	String dt_fine = (String)par.get("data_fine");
	tipo_oper=tipo_oper.trim();

	if(tipo_oper.equals("01")){
//gb 20/06/07		selConta = "SELECT data_contatto data_inizio, "+
		selConta = "SELECT ap_data_apertura data_inizio, "+ //gb 20/06/07
//gb 20/06/07			"data_chiusura data_fine,motivo_chiusura dimissione FROM contatti r "+
			"ap_data_chiusura data_fine, motivo_chiusura dimissione FROM ass_progetto r "+ //gb 20/06/07
			"WHERE r.n_cartella = "+ n_cartella+
//gb 20/06/07			" AND r.data_contatto <= "+
			" AND r.ap_data_apertura <= "+ //gb 20/06/07
			formatDate(dbc,dt_fine)+
//gb 20/06/07			" AND (r.data_chiusura IS NULL"+
			" AND (r.ap_data_chiusura IS NULL"+ //gb 20/06/07
//gb 20/06/07			" OR r.data_chiusura = "+
			" OR r.ap_data_chiusura = "+ //gb 20/06/07
			formatDate(dbc,"1000-01-01")+
//gb 20/06/07			" OR r.data_chiusura >="+
			" OR r.ap_data_chiusura >="+ //gb 20/06/07
			formatDate(dbc,dt_ini)+")";}
	else if(tipo_oper.equals("02")){
              selConta = "SELECT  s.ski_data_apertura data_inizio, "+
                      "s.ski_data_uscita data_fine, "+
                      "s.ski_dimissioni dimissione "+
                      " FROM  skinf s "+
                      "WHERE s.n_cartella = "+ n_cartella+
                      " AND s.ski_data_apertura <= "+
                      formatDate(dbc,dt_fine)+
                      " AND (s.ski_data_uscita IS NULL"+
                      " OR s.ski_data_uscita = "+
                      formatDate(dbc,"1000-01-01")+
                      " OR s.ski_data_uscita >="+
                      formatDate(dbc,dt_ini)+")";}
	else if(tipo_oper.equals("03")){
		selConta = "SELECT r.skm_data_apertura data_inizio, "+
			"r.skm_data_chiusura data_fine,r.skm_motivo_chius dimissione FROM skmedico r "+
			"WHERE r.n_cartella = "+ n_cartella+
			" AND r.skm_data_apertura <= "+
			formatDate(dbc,dt_fine)+
			" AND (r.skm_data_chiusura IS NULL"+
			" OR r.skm_data_chiusura = "+
			formatDate(dbc,"1000-01-01")+
			" OR r.skm_data_chiusura >="+
			formatDate(dbc,dt_ini)+")";}
        else if(tipo_oper.equals("04")){
		selConta = "SELECT r.skf_data data_inizio, "+
			"r.skf_data_chiusura data_fine,r.skf_motivo_chius dimissione FROM skfis r "+
			"WHERE r.n_cartella = "+ n_cartella+
			" AND r.skf_data <= "+
			formatDate(dbc,dt_fine)+
			" AND (r.skf_data_chiusura IS NULL"+
			" OR r.skf_data_chiusura = "+
			formatDate(dbc,"1000-01-01")+
			" OR r.skf_data_chiusura >="+
			formatDate(dbc,dt_ini)+")";}
	else if(tipo_oper.equals("52")){
		selConta = "SELECT r.skm_data_apertura data_inizio, "+
			"r.skm_data_chiusura data_fine,r.skm_motivo_chius dimissione FROM skmedpal r "+
			"WHERE r.n_cartella = "+ n_cartella+
			" AND r.skm_data_apertura <= "+
			formatDate(dbc,dt_fine)+
			" AND (r.skm_data_chiusura IS NULL"+
			" OR r.skm_data_chiusura = "+
			formatDate(dbc,"1000-01-01")+
			" OR r.skm_data_chiusura >="+
			formatDate(dbc,dt_ini)+")";}
	else
     		selConta="";
	//System.out.println("Select Contatti"+selConta);
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
    else if(tipo.trim().equals("P"))
		raggruppa=" Presidio ";
    else	raggruppa="TIPO NON VALIDO";
    }else if (this.dom_res.equals("D"))
    {
        if (tipo.trim().equals("A"))
        	raggruppa="Area Distr. di Domicilio";
        else if (tipo.trim().equals("C"))
        	raggruppa="Comune di Domicilio";
        else	raggruppa="TIPO NON VALIDO";
        }else if (this.dom_res.equals("R"))
        {
            if (tipo.trim().equals("A"))
            	raggruppa="Area Distr. di Residenza";
            else if (tipo.trim().equals("C"))
            	raggruppa="Comune di Residenza";
            else	raggruppa="TIPO NON VALIDO";
            }  	
//	if(tipo.trim().equals("C"))
//		raggruppa=" Comune ";
//	else if(tipo.equals("A"))
//		raggruppa=" Area ";
	
	return raggruppa;
}
//private String faiRaggruppamento(String tipo) {
//	String raggruppa="";
//
//	if(tipo.trim().equals("C"))
//		raggruppa=" Comune ";
//	else if(tipo.equals("A"))
//		raggruppa=" Area ";
//	else if(tipo.equals("P"))
//		raggruppa=" Presidio ";
//	else
//     		raggruppa="TIPO NON VALIDO";
//	return raggruppa;
//}

}	// End of FoIAssEle class

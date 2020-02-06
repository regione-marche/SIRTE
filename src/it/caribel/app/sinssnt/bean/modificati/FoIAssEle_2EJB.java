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

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class FoIAssEle_2EJB extends SINSSNTConnectionEJB {
	String dom_res;
	String dr;
	String figprof = "";
	String op = "";
public FoIAssEle_2EJB() {}

//hash per decodifica operatore
Hashtable hOperatore= new Hashtable();
// se ho pi� schede coppie per uno stesso
//utente devo stampare solo i dati diversi
String vecchiaCartella="";
String raggruppamento="";
//hash per calcolare il numero totale di assistiti
Hashtable hContaGenerale= new Hashtable();
Hashtable hContaOperatore= new Hashtable();

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

/**
* restituisce la decodifica della figura professionale
*/
private String getFiguraProfessionale(Object otipo) {
	String fp = "";
        String tipo = "";
	try {
                tipo = otipo.toString();
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
			fp = "FIGURA PROFESSIONALE NON VALIDA";
	} catch(Exception e) {
		fp = "FIGURA PROFESSIONALE ERRATA";
	}
	return fp;
}

/**
* restituisce la select per la stampa sintetica.
*/

private String getSelectSintetica(ISASConnection dbc, Hashtable par) {
	String s = "SELECT DISTINCT r.int_tipo_oper,"+
		"r.int_cartella, r.int_tipo_oper,"+
                //"r.int_contatto, "+
                "o.codice cod_oper,nvl(trim(o.cognome),'') || ' ' ||nvl(trim(o.nome),'') "+
                "operatore,"+
                "c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"u.descrizione, u.des_zona, u.des_distretto "+
		"FROM interv r, cartella c, operatori o, "+
		" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u ";
                //,anagra_c a";
	try {
		String w = getSelectParteWhereSint(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
	} catch(Exception e) {
		debugMessage("FoIAssEle_2EJB.getSelectSintetica(): "+e);
		e.printStackTrace();
	}
	s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione, "+
		" r.int_tipo_oper,c.cognome,r.int_cartella";
	debugMessage("FoIAssEle_2EJB.getSelectSintetica(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhereSint(ISASConnection dbc, Hashtable par) {
  ServerUtility su = new ServerUtility();
  String figprof="";
  if(!par.get("figprof").equals("00"))
	figprof=(String)par.get("figprof");
	String s = su.addWhere("", su.REL_AND, "r.int_tipo_oper",
		su.OP_EQ_STR, figprof);

        raggruppamento = (String)par.get("ragg");
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
            s += " AND r.int_cod_areadis=u.codice ";

    		}
	
	s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));

	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_GE_NUM,
		formatDate(dbc, (String)par.get("data_inizio")));


	String cod_fine="";
        //controllo codice fine operatore
		if (par.get("op_fine") != null)
		{
		  cod_fine=(String)(par.get("op_fine"));
		  if (!cod_fine.equals(""))
		  	s=s+" AND r.int_cod_oper<='"+cod_fine+"'";
        	}

	String cod_inizio="";
        //controllo codice inizio operatore
		if (par.get("op_ini") != null)
		{
		  cod_inizio=(String)(par.get("op_ini"));
		  if (!cod_inizio.equals(""))
		  	s=s+" AND r.int_cod_oper>='"+cod_inizio+"'";
        	}

	if (! s.equals("")) s += " AND";
	s += " r.int_cartella = c.n_cartella "+
// 24/090/09 m.          " AND r.int_contatto<>0 "+
			// 24/09/09  m. --
			" AND r." + (figprof.trim().equals("01")?"n_progetto":"int_contatto") + " <> 0" + 
			" AND r." + (figprof.trim().equals("01")?"n_progetto":"int_contatto") + " IS NOT NULL" + 
			// 24/09/09  m. --
          " AND r.int_cod_oper = o.codice ";
	return s;
}

/**
* restituisce la select per la stampa analitica.
*/
private String getSelectAnalitica(ISASConnection dbc, Hashtable par) {
	String dt_ini="";
	String dt_fine="";
		dt_ini = (String)par.get("data_inizio");
		dt_fine = (String)par.get("data_fine");
	String s = "SELECT DISTINCT r.int_cartella,r.int_tipo_oper,"+
//gb 21/06/07                "r.int_contatto, " +
		"r.int_tipo_oper, "+
                "o.codice cod_oper,nvl(trim(o.cognome),'') || ' ' ||nvl(trim(o.nome),'') "+
                "operatore,c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"a.dom_citta,a.dom_indiriz,a.indirizzo,a.citta,a.nome_camp, a.telefono1, "+
		"u.descrizione, u.des_zona, u.des_distretto, "+
		"mecogn, menome ";
        String s_from = " FROM interv r, cartella c, anagra_c a,"+
		""+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, medici, operatori o ";
        String s_where = "";
        String w = "";
	try {
		w = getSelectParteWhereAna(dbc, par);
	} catch(Exception e) {
		debugMessage("FoIAssEleEJB.getSelectAnalitica(): "+e);
		e.printStackTrace();
	}

        String tipo_oper = ((String)par.get("figprof")).trim();
	if(tipo_oper.equals("01")){
//gb 20/06/07                s +=",co.data_contatto data_inizio,co.data_chiusura data_fine,co.motivo_chiusura dimissione ";
                s +=",co.ap_data_apertura data_inizio, co.ap_data_chiusura data_fine, co.motivo_chiusura dimissione "; //gb 20/06/07
//gb 20/06/07                s_from += ",contatti co";
                s_from += ",ass_progetto co"; //gb 20/06/07
                s_where =" AND co.n_cartella = r.int_cartella"+
//gb 20/06/07                         " AND co.n_contatto = r.int_contatto";
                         " AND co.n_progetto = r.n_progetto"; //gb 20/06/07
/** 24/09/09 m.
			 " AND r.n_progetto <> 0" + //gb 20/06/07
			 " AND r.n_progetto IS NOT NULL"; //gb 20/06/07
**/			 
        }else if(tipo_oper.equals("02")){
               s += ", s.ski_data_apertura data_inizio, "+
			"s.ski_data_uscita data_fine, "+
			"s.ski_dimissioni dimissione ";
                s_from += ",skinf s ";
                s_where = " AND s.n_cartella = r.int_cartella"+
                        " AND s.n_contatto = r.int_contatto";
       }else if(tipo_oper.equals("03")){
      		s += ",s.skm_data_apertura data_inizio, "+
                  "s.skm_data_chiusura data_fine,skm_motivo_chius dimissione";
                s_from += ",skmedico s ";
                s_where = " AND s.n_cartella = r.int_cartella"+
                        " AND s.n_contatto = r.int_contatto";
      }else if(tipo_oper.equals("04")){
      		s += ",s.skf_data data_inizio, "+
                  "s.skf_data_chiusura data_fine,s.skf_motivo_chius dimissione  ";
                s_from += ",skfis s";
                s_where = " AND s.n_cartella = r.int_cartella"+
                        " AND s.n_contatto = r.int_contatto";
      }
//gb 20/06/07: caso degli oncologi *******
	else if(tipo_oper.equals("52")){
      		s += ",s.skm_data_apertura data_inizio, "+
                  "s.skm_data_chiusura data_fine, s.skm_motivo_chius dimissione";
                s_from += ",skmedpal s ";
                s_where = " AND s.n_cartella = r.int_cartella"+
                        " AND s.n_contatto = r.int_contatto";
      }
//gb 20/06/07: fine *******
// 24/09/09  m. --
			s_where += " AND r." + (tipo_oper.trim().equals("01")?"n_progetto":"int_contatto") + " <> 0" + 
			" AND r." + (tipo_oper.trim().equals("01")?"n_progetto":"int_contatto") + " IS NOT NULL";
			// 24/09/09  m. --

        if (!s_from.equals("")) s = s + s_from;
        if (! w.equals("")) s = s + " WHERE " + w;
        if (!s_where.equals("")) s = s + s_where;
	s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione, "+
		" r.int_tipo_oper,c.cognome,r.int_cartella";
	debugMessage("FoIAssEleEJB.getSelectAnalitica(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhereAna(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();

        String figprof="";
        if(!par.get("figprof").equals("00"))
	  figprof=(String)par.get("figprof");

	String s = su.addWhere("", su.REL_AND, "r.int_tipo_oper",
		su.OP_EQ_STR, figprof);

	s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));

        raggruppamento = (String)par.get("ragg");
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

	String cod_fine="";
        //controllo codice fine operatore
		if (par.get("op_fine") != null)
		{
		  cod_fine=(String)(par.get("op_fine"));
		  if (!cod_fine.equals(""))
		  	s=s+" AND r.int_cod_oper<='"+cod_fine+"'";
        	}

	String cod_inizio="";
        //controllo codice inizio operatore
		if (par.get("op_ini") != null)
		{
		  cod_inizio=(String)(par.get("op_ini"));
		  if (!cod_inizio.equals(""))
		  	s=s+" AND r.int_cod_oper>='"+cod_inizio+"'";
        	}
	if (! s.equals("")) s += " AND ";
	s += "mecodi = a.cod_med "+
		" AND r.int_cartella = c.n_cartella "+
		" AND r.int_cartella = a.n_cartella "+
	        " AND r.int_cod_oper = o.codice "+
		" AND a.data_variazione IN ( SELECT MAX (data_variazione) "+
		" FROM anagra_c WHERE a.n_cartella = anagra_c.n_cartella )";
	return s;
}

/**
* stampa sintetica-analitica: sezione layout del documento
*/
private void preparaLayout(mergeDocument doc,
        ISASConnection dbc,Hashtable par) {

	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();
	
	String type = par.get("TYPE")!=null?par.get("TYPE").toString():"PDF";
	if (type.equals("PDF")){
	System.out.println("tipo = "+type);
	ht.put("#txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));
	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	ht.put("#fig_prof#",
		getFiguraProfessionale(par.get("figprof")).toUpperCase());

	doc.write("layout");
	doc.writeSostituisci("before",ht);
	}
	else 
	{
	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#fig_prof#",	getFiguraProfessionale(par.get("figprof")).toUpperCase());
	doc.writeSostituisci("layout",ht);
	}
}


/**
* stampa sintetica: entry point
*/
public byte[] query_infesint_2(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	try {
		
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
		
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);
                System.out.println("USER"+ utente);
                /*ILARIA questa stampa per come � stata impostata
                non pu� essere fatta per TUTTE le figure professionali
                andrebbe rifatta nuova quindi si � deciso di togliere
                il TUTTI. per evitare eventuali errori e se arriva il tipo oper
                uguale a 00
                */
                String tipo="";
                if (par.get("figprof")!=null)
                           tipo=(String)par.get("figprof");
                if (tipo.trim().equals("") || tipo.trim().equals("00"))
                {
                      String seloperatori=" SELECT tipo from operatori"+
                        " where codice='"+ utente+"'";
                      ISASRecord dbop=dbc.readRecord(seloperatori) ;
                      tipo=(String) dbop.get("tipo");
                      par.put("figprof",tipo);
                }
		ISASCursor dbcur=dbc.startCursor(getSelectSintetica(dbc,par));
		preparaLayout(doc, dbc, par);
		System.out.println("IAssEle2: 1");
		
		
        Hashtable hGenerale=new Hashtable();
		while (dbcur.next())
                {
                   ISASRecord dbr= dbcur.getRecord();
                   hGenerale=AnalizzaOperatore(dbr,hGenerale,dbc);
                }
                if (hGenerale.size()!=0)
				   StampaGenerale(hGenerale,doc,dbc,"sint");
				else doc.write("messaggio");
		
		
		        doc.write("finale");
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;

		doc.close();
		byte[] rit = (byte[])doc.get();
		//debugMessage("FoIAssEle_2EJB.query_infesint_2(): documento ["+
		//	(new String(rit))+"]");
		return rit;
	} catch(Exception e) {
		debugMessage("FoIAssEle_2EJB.query_infesint_2(): "+e);
		throw new SQLException("Errore eseguendo query_infesint_2()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				debugMessage("FoIAssEle_2EJB.query_infesint_2(): "+e1);
			}
		}
	}
}


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
			fp = "FIGURA PROFESSIONALE NON VALIDA";
	} catch(Exception e) {
		fp = "FIGURA PROFESSIONALE ERRATA";
	}
	return fp;
}




/**
* stampa analitica: entry point
*/
public byte[] query_inferef_2(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	try {
		String type = par.get("TYPE")!=null?par.get("TYPE").toString():"PDF";

		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}		
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);
                System.out.println("USER"+ utente);
                /*ILARIA questa stampa per come � stata impostata
                non pu� essere fatta per TUTTE le figure professionali
                andrebbe rifatta nuova quindi si � deciso di togliere
                il TUTTI. per evitare eventuali errori e se arriva il tipo oper
                uguale a 00
                */
                String tipo="";
                if (par.get("figprof")!=null)
                           tipo=(String)par.get("figprof");
               System.out.println("TIPOOOOOOO->"+tipo);
                if (tipo.trim().equals("") || tipo.trim().equals("00"))
                {
                      String seloperatori=" SELECT tipo from operatori"+
                        " where codice='"+ utente+"'";
                      ISASRecord dbop=dbc.readRecord(seloperatori) ;
                      tipo=(String) dbop.get("tipo");
                      par.put("figprof",tipo);
                }

		ISASCursor dbcur=dbc.startCursor(getSelectAnalitica(dbc,par));
                preparaLayout(doc,dbc,par);
				
				
		
                Hashtable hGenerale=new Hashtable();
		while (dbcur.next())
                {
                   ISASRecord dbr= dbcur.getRecord();
                   hGenerale=AnalizzaOperatore(dbr,hGenerale,dbc);
                }
                if (hGenerale.size()!=0)
                   StampaGenerale(hGenerale,doc,dbc,"ana");
                else
                      doc.write("messaggio");
					  
		
                doc.write("finale");
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;

		doc.close();
		byte[] rit = (byte[])doc.get();
		//debugMessage("FoIAssEle_2EJB.query_inferef_2(): documento ["+
		//	(new String(rit))+"]");
		return rit;
	} catch(Exception e) {
		debugMessage("FoIAssEle_2EJB.query_inferef_2(): "+e);
		throw new SQLException("Errore eseguendo query_inferef_2()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				debugMessage("FoIAssEle_2EJB.query_inferef_2(): "+e1);
			}
		}
	}
}


public Hashtable AnalizzaOperatore(ISASRecord dbr ,Hashtable hGenerale,ISASConnection dbc)
throws SQLException
{
        try
        {
          Hashtable hZona= new Hashtable();
          if (dbr.get("cod_oper")!=null && !((String)dbr.get("cod_oper")).equals(""))
          {
              String  chiave=(String)dbr.get("cod_oper");
              if (dbr.get("operatore")!=null && dbr.get("int_tipo_oper")!=null)
                      hOperatore.put(chiave,"" +dbr.get("operatore")+""+
                      dbr.get("int_tipo_oper"));
              if (hGenerale!=null && ((Hashtable)hGenerale.get(chiave))!=null)
                  hZona=(Hashtable)hGenerale.get(chiave);
              hZona=AnalizzaZona(dbr,hZona,chiave,dbc);
              hGenerale.put(chiave,hZona);
          }
          return hGenerale;

 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaOperatore");
	}

}

private void StampaGenerale(Hashtable hGenerale,mergeDocument doc,ISASConnection dbc,String tipo)
throws SQLException{
//tipo mi definisce se la stampa � analitica o sintetica
//quindi se stampo o no
try
{
      Hashtable hZona= new Hashtable();
      Enumeration enumGenerale =orderedKeys( hGenerale);
      Hashtable hConta=new Hashtable();
      Hashtable htitolo=new Hashtable();
      while (enumGenerale.hasMoreElements())
      {
          String chiave = "" + enumGenerale.nextElement();
          String cur_oper="";
          String oper = ""+hOperatore.get(chiave);
          this.op = oper.substring(0,oper.length()-2);
          cur_oper = oper.substring(oper.length()-2,oper.length());
		  this.figprof = getFiguraProfessionale(cur_oper);
		  htitolo.put("#tipo_figprof#", figprof);
          htitolo.put("#nome_figprof#",op);
	  doc.writeSostituisci("figuraprofessionale", htitolo);
          if (hGenerale.get(chiave)!=null)
          {
             hZona=(Hashtable)hGenerale.get(chiave);
             StampaDaZona( hZona, doc,chiave,tipo,cur_oper);
             hConta.put("#descrizione#","Totale numero assistiti per operatore:");
             hConta.put("#totale#",""+hContaOperatore.size());
             doc.writeSostituisci("totale",hConta);
             hContaOperatore.clear();
          }
      }//fine ciclo
       hConta.put("#descrizione#","Totale numero assistiti:");
       hConta.put("#totale#",""+hContaGenerale.size());
       doc.writeSostituisci("totale",hConta);

}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaOperatore");
	}

}

private void StampaDaZona(Hashtable hzona,mergeDocument doc,String chiave,String tipo,String cur_oper)
{
    Enumeration enumZona =orderedKeys( hzona);
    while (enumZona.hasMoreElements())
    {
        String zona = "" + enumZona.nextElement();
        Hashtable hdistretti = (Hashtable)hzona.get(zona);
        Enumeration enumDistretti = orderedKeys(hdistretti);
        while (enumDistretti.hasMoreElements())
        {
              String distretto = "" + enumDistretti.nextElement();
              Hashtable hcomuni = (Hashtable) hdistretti.get(distretto);
              Enumeration enumComuni = orderedKeys(hcomuni);
               while (enumComuni.hasMoreElements())
              {
                  String comune = "" + enumComuni.nextElement();
                  Vector vDati = (Vector) hcomuni.get(comune);
                  Hashtable htab=new Hashtable();
                  htab.put("#descrizione_zona#",zona);
                  htab.put("#des_distr#",distretto);
                  String tipologia="";
//                  if (raggruppamento.equals("A"))
//                      tipologia="Area Distr.";
//                  else if (raggruppamento.equals("C"))
//                        tipologia="Comune";
//                  else if (raggruppamento.equals("P"))
//                      tipologia="Presidio";
                  
                  
                  if(this.dom_res==null)
               {
               if (raggruppamento.equals("A"))
                   tipologia="Area Distr.";
               else if (raggruppamento.equals("C"))
                     tipologia="Comune";
               else if (raggruppamento.equals("P"))
            	   	tipologia="Presidio";
               }else if (this.dom_res.equals("D"))
               {
                   if (raggruppamento.equals("A"))
                       tipologia="Area Distr. di Domicilio";
                   else if (raggruppamento.equals("C"))
                         tipologia="Comune di Domicilio";
                   }else if (this.dom_res.equals("R"))
                   {
                       if (raggruppamento.equals("A"))
                           tipologia="Area Distr. di Residenza";
                       else if (raggruppamento.equals("C"))
                             tipologia="Comune di Residenza";
                       }  	
                  
                  htab.put("#tipologia#",tipologia);
                  htab.put("#descrizione#",comune);
				  
                  doc.writeSostituisci("zona",htab);
/*                  String motivo="";
                  if (tipo.equals("ana"))
                  {
                      if (cur_oper.equals("02") ||cur_oper.equals("04"))
                              motivo="motivo";
                  }*/
                  htab.put("#motivo#","motivo");
				  htab.put("#tipologia#",tipologia);
//                  doc.write("iniziotab");
                  doc.writeSostituisci("iniziotab",htab);
                  Enumeration enumVett = vDati.elements();
                  while (enumVett.hasMoreElements())
                  {
                      Hashtable hDati=(Hashtable)enumVett.nextElement();
                      hContaGenerale.put(""+hDati.get("#cartella#"),"");
                      hContaOperatore.put(""+hDati.get("#cartella#"),"");

                      if (tipo.equals("ana")){
                        if (vecchiaCartella.equals(""+hDati.get("#cartella#")))
                                hDati=PulisciCampi(hDati);
                        else
                              vecchiaCartella=""+hDati.get("#cartella#");
                      }
						hDati.put("#descrizione#",comune);
						 hDati.put("#descrizione_zona#",zona);
                  hDati.put("#des_distr#",distretto);
				  hDati.put("#tipo_figprof#", this.figprof);
					hDati.put("#nome_figprof#",this.op);
                      doc.writeSostituisci("assistito",hDati);
                  }//fine ciclo sul vettore
                  doc.write("finetab");
                  vecchiaCartella = "";
              }//fine  ciclo comuni
        }//fine  ciclo distretti
    }//fine  ciclo zona
}

private Hashtable AnalizzaZona(ISASRecord dbr,Hashtable hzona,String chiave,ISASConnection dbc)
throws SQLException
{
  try{
    Hashtable hdistretti=new Hashtable();
    if (dbr.get("des_zona")!=null && !((String)dbr.get("des_zona")).equals(""))
    {
      String  zona=(String)dbr.get("des_zona");
      if (hzona!=null && ((Hashtable)hzona.get(zona))!=null)
        hdistretti=(Hashtable)hzona.get(zona);
      hdistretti=AnalizzaDistretti(dbr,hdistretti,chiave,dbc);
      hzona.put(zona,hdistretti);
    }//fine descrizione distretti
    return hzona;
    }catch(Exception e){
      e.printStackTrace();
      throw new SQLException("Errore eseguendo una AnalizzaZona");
    }
}

private Hashtable AnalizzaDistretti(ISASRecord dbr,Hashtable hdistretti,String chiave,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hcomuni=new Hashtable();
            if (dbr.get("des_distretto")!=null && !((String)dbr.get("des_distretto")).equals(""))
            {
                String  distretto=(String)dbr.get("des_distretto");
                if (hdistretti!=null && ((Hashtable)hdistretti.get(distretto))!=null)
                      hcomuni=(Hashtable)hdistretti.get(distretto);
                hcomuni=AnalizzaComuni(dbr,hcomuni,dbc);
                hdistretti.put(distretto,hcomuni);
            }//fine descrizione distretti
            return hdistretti;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaDistretti");
	}
}

private Hashtable AnalizzaComuni(ISASRecord dbr,Hashtable hcomuni,ISASConnection dbc)
throws SQLException
{
        try
        {
            Vector vDati=new Vector();
            if (dbr.get("descrizione")!=null && !((String)dbr.get("descrizione")).equals(""))
            {
                String  comune=(String)dbr.get("descrizione");
                if (hcomuni!=null && ((Vector)hcomuni.get(comune))!=null)
                     vDati=(Vector)hcomuni.get(comune);
                vDati=caricaDati(dbr,vDati,dbc);
                hcomuni.put(comune,vDati);
            }//fine descrizione comuni
            return hcomuni;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaComuni");
	}
}

public  Vector caricaDati(ISASRecord dbr ,Vector vDati,ISASConnection dbc)
throws SQLException
{
        try
        {
          Hashtable tab=new Hashtable();
          int cartella=0;
          if (dbr.get("int_cartella")!=null)
              cartella=((Integer)dbr.get("int_cartella")).intValue();
          tab.put("#cartella#","" + cartella);

          String cognome="";
          if (dbr.get("cogn")!=null && !((String)dbr.get("cogn")).equals(""))
                 cognome=(String)dbr.get("cogn");

          String nome="";
          String nome_camp="";
          if (dbr.get("nomeut")!=null && !((String)dbr.get("nomeut")).equals(""))
                  nome=(String)dbr.get("nomeut");
          tab.put("#nomeut#",cognome.trim()+" " + nome.trim());
          tab.put("#data_nasc#", getDateField(dbr, "data_nasc"));
          if (this.dom_res==null)
          {
          if(dbr.get("dom_indiriz")!=null)
            tab.put("#indirizzo#",(String)dbr.get("dom_indiriz")+" - ");
          else tab.put("#indirizzo#","");
          }else if (this.dom_res.equals("D"))
          {
              if(dbr.get("dom_indiriz")!=null)
                tab.put("#indirizzo#",(String)dbr.get("dom_indiriz")+" - ");
              else tab.put("#indirizzo#","");
              }else if (this.dom_res.equals("R"))
              {
                  if(dbr.get("indirizzo")!=null)
                    tab.put("#indirizzo#",(String)dbr.get("indirizzo")+" - ");
                  else tab.put("#indirizzo#","");
                  }
          if(dbr.get("descrizione")!=null)
            tab.put("#citta#",(String)dbr.get("descrizione"));
          else tab.put("#citta#","");
          if(dbr.get("telefono1")!=null)
            tab.put("#telefono1#",(String)dbr.get("telefono1"));
          else tab.put("#telefono1#","");
          if(dbr.get("nome_camp")!=null &&
          !((String)dbr.get("nome_camp")).equals(""))
            nome_camp="("+(String)dbr.get("nome_camp")+")";
            tab.put("#nome_camp#",nome_camp);
          if(dbr.get("mecogn")!=null && dbr.get("menome")!=null)
            tab.put("#medico#",(String)dbr.get("mecogn")+" "+
            (String)dbr.get("menome"));
          else tab.put("#medico#","");
          if(dbr.get("data_inizio")!=null)
          { String d = ((java.sql.Date)dbr.get("data_inizio")).toString();
            tab.put("#data_in#",getDateField(dbr, "data_inizio"));
          }else{ tab.put("#data_in#","");
         }
          if(dbr.get("data_fine")!=null)
            tab.put("#data_out#",getDateField(dbr, "data_fine"));
          else tab.put("#data_out#","");

          String tipo_oper="";
          if (dbr.get("int_tipo_oper")!=null)
                  tipo_oper=(String)dbr.get("int_tipo_oper");
  	 tab.put("#reg_dimissioni#",getMotivoUscita(dbr,dbc,tipo_oper));
           vDati.add(tab);
           return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}

}
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
               //  System.out.println("MOTIVO CHIUSURA-->"+sel);
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
 public Enumeration orderedKeys(Hashtable hOrdinare)
  {
    Enumeration keys = hOrdinare.keys();
    Vector temp = new Vector();
    while( keys.hasMoreElements() )
    {
      temp.addElement("" + keys.nextElement() );
    }
     Collections.sort(temp);
     return temp.elements();
 }


private Hashtable PulisciCampi(Hashtable hDati)
{//questa funzione sbianca i campi dell'assistito gi� stampato
    hDati.put("#cartella#","");
    hDati.put("#nomeut#","");
    hDati.put("#data_nasc#","");
    hDati.put("#nome_camp#","");
    hDati.put("#medico#","");
    hDati.put("#indirizzo#","");
    hDati.put("#citta#","");
    hDati.put("#nome_camp#","");
    hDati.put("#telefono1#","");
    return hDati;
}



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
 }	// End of FoIAssEle_2 class

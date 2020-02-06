package it.caribel.app.sinssnt.bean.modificati;
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 12/09/2003 - EJB di connessione alla procedura SINS Tabella FoMAssEle
//
// Francesco Greco
//
// ==========================================================================


import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.math.*;

import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;	// fo merge

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.DataWI;

public class FoMAssEleEJB extends SINSSNTConnectionEJB {

it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

//hash per decodifica operatore
Hashtable hOperatore= new Hashtable();
String dom_res;
String dr;
String ragg="";
// se ho pi� schede coppie per uno stesso
//utente devo stampare solo i dati diversi
String vecchiaCartella="";
//hash per calcolare il numero totale di assistiti
Hashtable hContaGenerale= new Hashtable();
Hashtable hContaOperatore= new Hashtable();
Hashtable hContaRagg= new Hashtable();
private String ubic = "0";

private void preparaLayout(mergeDocument md, ISASConnection dbc,String data_inizio,String data_fine) {
	Hashtable htxt = new Hashtable();
	try {
		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ragione_sociale'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
	} catch (Exception ex) {
		htxt.put("#txt#", "ragione_sociale");
	}
        ServerUtility su =new ServerUtility();
        String data=data_inizio.substring(8, 10)+"/"+
                  data_inizio.substring(5, 7)+"/"+data_inizio.substring(0, 4);
        htxt.put("#data_inizio#",data);
        data=data_fine.substring(8, 10)+"/"+
                data_fine.substring(5, 7)+"/"+data_fine.substring(0, 4);
        htxt.put("#data_fine#", data);
		 System.out.println("1");
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        /*if (ass.equals("1"))
          htxt.put("#assistenza#", "ASSISTENZA DOMICILIARE");
        else if (ass.equals("2"))
          htxt.put("#assistenza#", "ASSISTENZA AMBULATORIALE");
        else  */htxt.put("#assistenza#", "");
	md.writeSostituisci("layout",htxt);
}

public static String getjdbcDate()
{
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}


public FoMAssEleEJB() {}


public byte[] query_elencoSint(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	   String dataini="";
        String datafine="";
        String tipo="";
	boolean entrato=false;
	try{
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}

		myLogin lg = new myLogin();
		String selectedLanguage = (String)par.get(FileMaker.printParamLang);
		lg.put(utente,passwd,selectedLanguage);
		//lg.put(utente,passwd);
		dbc=super.logIn(lg);
		ISASRecord ubi = dbc.readRecord("select conf_txt from conf where conf_key ='ADRSA_UBIC'");
		this.ubic = ubi.get("conf_txt").toString();
		String datasystem="";
		int eta=0;

		if (par.get("data_fine") != null && !((String)par.get("data_fine")).equals(""))
			datafine=(String)par.get("data_fine");
		if (par.get("data_inizio") != null && !((String)par.get("data_inizio")).equals(""))
			dataini=(String)par.get("data_inizio");

		String myselect = mkSelectSintetica(dbc, par);
                System.out.println("SELECT "+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
			
                preparaLayout(doc,dbc,dataini,datafine);//,(String)par.get("ass"));
				System.out.println("2");
                Hashtable hGenerale=new Hashtable();
                
		while (dbcur.next())
                {
				ISASRecord dbr= dbcur.getRecord();
                   if (par.get("TYPE")!=null && !par.get("TYPE").toString().equals("PDF"))
					StampaRigaExcel(dbc,doc,dbr);
				   else 
				   hGenerale=AnalizzaOperatore(dbc,dbr,hGenerale,"S");
                }
				System.out.println("3");
                if (hGenerale.size()!=0)
                   StampaGenerale(hGenerale,doc,dbc,"S");
                else if (par.get("TYPE")==null || par.get("TYPE").toString().equals("PDF"))
                doc.write("messaggio");
				System.out.println("4");
				if (par.get("TYPE")!=null && !par.get("TYPE").toString().equals("PDF")) doc.write("fineTab");
                doc.write("finale");
				System.out.println("5");
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_elencoSint()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}


private void StampaRigaExcel(ISASConnection dbc, mergeDocument doc, ISASRecord dbr) throws it.pisa.caribel.isas2.ISASMisuseException,java.sql.SQLException,java.lang.Exception
{
Hashtable h = faiHashVuotaExcel();
h.put("#medico#",dbr.get("operatore").toString());
h.put("#zona#",dbr.get("descrizione_zona").toString());
h.put("#distretto#",dbr.get("des_distr").toString());
h.put("#comune#",dbr.get("descrizione").toString());
h.put("#cartella#",dbr.get("n_cartella").toString());
h.put("#nomecog#",dbr.get("cognome").toString()+" "+dbr.get("nome").toString());
h.put("#data_nasc#",dbr.get("data_nasc").toString());
String ski_data_apertura="";
          if (dbr.get("ski_data_apertura")!=null)
          {
            ski_data_apertura=((java.sql.Date)dbr.get("ski_data_apertura")).toString();
            if (ski_data_apertura.length()==10)
              ski_data_apertura=ski_data_apertura.substring(8,10)+"/"+
                    ski_data_apertura.substring(5,7)+"/"+ski_data_apertura.substring(0,4);
          }
          h.put("#reg_data_in#",ski_data_apertura);

          String ski_data_uscita="";
          if (dbr.get("ski_data_uscita")!=null)
          {
            ski_data_uscita=((java.sql.Date)dbr.get("ski_data_uscita")).toString();
            if (ski_data_uscita.length()==10)
              ski_data_uscita=ski_data_uscita.substring(8,10)+"/"+
                    ski_data_uscita.substring(5,7)+"/"+ski_data_uscita.substring(0,4);
          }
          h.put("#reg_data_out#",ski_data_uscita);
          //J23/03/2004
          String descr="";
          if (dbr.get("ski_descr_contatto")!=null && !((String)dbr.get("ski_descr_contatto")).equals(""))
            descr=(String)dbr.get("ski_descr_contatto");
			if (this.ubic.equals("3") && dbr.get("ski_tipocura")!=null)
			descr = (String)util.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "SAOADI", (String)dbr.get("ski_tipocura"), "tab_descrizione");
          h.put("#descr_contatto#",descr);
         
          String referente="";
          if (dbr.get("referente")!=null && !((String)dbr.get("referente")).equals(""))
                  referente=(String)dbr.get("referente");
          h.put("#inf_refe#",referente);

          String motivo=getMotivoUscita(dbr,dbc);
          h.put("#skf_motivo_chius#",motivo);


doc.writeSostituisci("tabella",h);

}


private Hashtable faiHashVuotaExcel(){
Hashtable h = new Hashtable();
h.put("#medico#","");
h.put("#zona#","");
h.put("#distretto#","");
h.put("#comune#","");
h.put("#cartella#","");
h.put("#nomecog#","");
h.put("#data_nasc#","");
h.put("#motivo#","");
h.put("#descr_contatto#","");
h.put("#inf_ref#","");
return h;
}


private String mkSelectSintetica(ISASConnection dbc, Hashtable par)
{
        String myselect="";
        ServerUtility su =new ServerUtility();
        String assis="";
        String operato=""  ;
        if (par.get("op")!=null)
                operato=(String)  par.get("op");
        if (par.get("ass")!=null)
                assis=(String)par.get("ass");
         myselect= "SELECT DISTINCT c.n_cartella, "+
                " c.cognome,c.nome,c.data_nasc,"+
                " u.cod_zona,u.cod_distretto,"+
                " u.des_zona descrizione_zona ,"+
                " u.codice,u.descrizione,u.des_distretto des_distr,";
                if (operato.equals("SI"))
                       myselect=myselect + "me.mecodi cod_oper,nvl(trim(me.mecogn),'') || ' '||nvl(trim(me.menome),'') operatore";
                else
                      myselect=myselect + "'NO_OPERATORE' cod_oper,'OPERATORE' operatore";
                myselect=myselect +" FROM cartella c,anagra_c a,skinf ski,ubicazioni_n u,medici me"+
                " WHERE ";


	String data_fine="";
        //controllo data fine
		if (par.get("data_fine") != null)
		{
		  data_fine=(String)(par.get("data_fine"));
		  myselect =myselect+" ski.ski_data_apertura<="+formatDate(dbc,data_fine);
        }

        //controllo data inizio
	if (par.get("data_inizio") != null)
        {
		  String scr=(String)(par.get("data_inizio"));
                  myselect =myselect+" AND (ski.ski_data_uscita is null OR ski.ski_data_uscita>="+formatDate(dbc,scr)+")";
        }

	String cod_inizio="";
        //controllo codice inizio operatore
        if (par.get("codice_inizio") != null)
        {
          cod_inizio=(String)(par.get("codice_inizio"));
          if (!cod_inizio.equals(""))
                myselect=myselect+" AND me.mecodi='"+cod_inizio+"'";
        }
        
                myselect=myselect+" AND ski_modalita='1'";
        
       String livello=(String)(par.get("motivo"));
       if (livello!=null && !livello.equals("-1"))
    	   myselect=myselect+" AND ski.ski_motivo='"+livello+"'";

       String condWhere = getFiltroUbicazione(par, su);
       myselect=myselect+ " AND " + condWhere;
       /*ragg = (String)par.get("ragg");
        myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

        //Aggiunto Controllo Domicilio/Residenza (BYSP)
        if((String)par.get("dom_res") == null)
        {
                if (ragg.equals("C"))
                  myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                        " AND u.codice=a.dom_citta)"+
        		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                        " AND u.codice=a.citta))";
                else if (ragg.equals("A"))
                  myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                        " AND u.codice=a.dom_areadis)"+
        		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                        " AND u.codice=a.areadis))";
                else if (ragg.equals("P"))
                    myselect += " AND u.codice=ski.ski_cod_presidio";
        }
        else if (((String)par.get("dom_res")).equals("D"))
                                  {
                                   if (ragg.equals("C"))
                  myselect += " AND u.codice=a.dom_citta";
                                    else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.dom_areadis";
                                    else if (ragg.equals("P"))
                  myselect += " AND u.codice=ski.ski_cod_presidio";
                                  }

        else if (((String)par.get("dom_res")).equals("R"))
                        {
                        if (ragg.equals("C"))
                  myselect += " AND u.codice=a.citta";
                else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.areadis";
                else if (ragg.equals("P"))
                    myselect += " AND u.codice=ski.ski_cod_presidio";
                        }


	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));*/


       myselect += " AND a.n_cartella=c.n_cartella"+
         " AND me.mecodi=a.cod_med "+
         " AND ski.n_cartella=c.n_cartella"+
         " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"+
	 " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";

        myselect =myselect+" ORDER BY cod_oper,descrizione_zona,"+
                          "des_distr,descrizione,c.cognome,c.nome,c.n_cartella"       ;
	return myselect;
}


public byte[] query_elencoAss(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	byte[] jessy;

        String dataini="";
        String datafine="";
        String tipo="";
	boolean entrato=false;
	try{
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}

		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);
		ISASRecord ubi = dbc.readRecord("select conf_txt from conf where conf_key ='ADRSA_UBIC'");
		this.ubic = ubi.get("conf_txt").toString();
		String datasystem="";
		int eta=0;

		if (par.get("data_fine") != null && !((String)par.get("data_fine")).equals(""))
			datafine=(String)par.get("data_fine");
		if (par.get("data_inizio") != null && !((String)par.get("data_inizio")).equals(""))
			dataini=(String)par.get("data_inizio");

		String myselect = mkSelectAnalitica(dbc, par);
                System.out.println("SELECT "+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
                preparaLayout(doc,dbc,dataini,datafine);//,(String)par.get("ass"));
                Hashtable hGenerale=new Hashtable();
                
		while (dbcur.next())
                {
					ISASRecord dbr= dbcur.getRecord();
				     if (par.get("TYPE")!=null && !par.get("TYPE").toString().equals("PDF"))
					StampaRigaExcel(dbc,doc,dbr);
					else 
                   hGenerale=AnalizzaOperatore(dbc,dbr,hGenerale,"A");
                }
                if (hGenerale.size()!=0)
                   StampaGenerale(hGenerale,doc,dbc,"A");
                else if (par.get("TYPE")==null || par.get("TYPE").toString().equals("PDF"))
                      doc.write("messaggio");
				if (par.get("TYPE")!=null && !par.get("TYPE").toString().equals("PDF"))
				doc.write("fineTab");
                doc.write("finale");
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_elencoAss()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}

private String mkSelectAnalitica(ISASConnection dbc, Hashtable par)
{
        String myselect="";
        ServerUtility su =new ServerUtility();
        String assis="";
        String operato=""  ;
        if (par.get("op")!=null)
                operato=(String)  par.get("op");

         if (par.get("ass")!=null)
                assis=(String)par.get("ass");
         myselect= "SELECT c.n_cartella, "+
                   "c.cognome,c.nome,c.data_nasc,ski.ski_descr_contatto,  ski.ski_tipocura,"+
                //J23/03/04"a.indirizzo,a.citta,a.nome_camp,a.telefono1,"+
                "ski.ski_data_apertura,ski.ski_data_uscita,ski_dimissioni,"+
                "u.cod_zona,u.cod_distretto,"+
                "u.des_zona descrizione_zona ,"+
                "u.codice,u.descrizione,u.des_distretto des_distr,"+
                " nvl(trim(op.cognome),'') || ' '||nvl(trim(op.nome),'') referente,";
                if (operato.equals("SI"))
                       myselect=myselect + "me.mecodi cod_oper,nvl(trim(me.mecogn),'') || ' '||nvl(trim(me.menome),'') operatore";
                else
                      myselect=myselect + "'NO_OPERATORE' cod_oper,'OPERATORE' operatore";
                myselect=myselect +" FROM cartella c,anagra_c a,operatori op,skinf ski,ubicazioni_n u,medici me"+
                " WHERE ";


	String data_fine="";
        //controllo data fine
		if (par.get("data_fine") != null)
		{
		  data_fine=(String)(par.get("data_fine"));
		  myselect =myselect+" ski.ski_data_apertura<="+formatDate(dbc,data_fine);
        }

        //controllo data inizio
	if (par.get("data_inizio") != null)
        {
		  String scr=(String)(par.get("data_inizio"));
                  myselect =myselect+" AND (ski.ski_data_uscita is null OR ski.ski_data_uscita>="+formatDate(dbc,scr)+")";
        }

	String cod_inizio="";
        //controllo codice inizio operatore
        if (par.get("codice_inizio") != null)
        {
          cod_inizio=(String)(par.get("codice_inizio"));
          if (!cod_inizio.equals(""))
                myselect=myselect+" AND me.mecodi='"+cod_inizio+"'";
        }

/*	String cod_fine="";
        //controllo codice fine operatore
        if (par.get("codice_fine") != null)
        {
          cod_fine=(String)(par.get("codice_fine"));
          if (!cod_fine.equals(""))
                myselect=myselect+" AND ski.ski_infermiere<='"+cod_fine+"'";
        }
*/
        //if (!assis.equals("E"))
                myselect=myselect+" AND ski_modalita='1'";
          
       String livello=(String)(par.get("motivo"));
       if (livello!=null && !livello.equals("-1"))
    	   myselect=myselect+" AND ski.ski_motivo='"+livello+"'";
       
       String condWhere = getFiltroUbicazione(par, su);
       myselect=myselect+ " AND " + condWhere;
       /*ragg = (String)par.get("ragg");
        myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

        //Aggiunto Controllo Domicilio/Residenza (BYSP)
        if((String)par.get("dom_res") == null)
        {
                if (ragg.equals("C"))
                  myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                        " AND u.codice=a.dom_citta)"+
        		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                        " AND u.codice=a.citta))";
                else if (ragg.equals("A"))
                  myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                        " AND u.codice=a.dom_areadis)"+
        		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                        " AND u.codice=a.areadis))";
                else if (ragg.equals("P"))
                    myselect += " AND u.codice=ski.ski_cod_presidio";
        }
        else if (((String)par.get("dom_res")).equals("D"))
                                  {
                                   if (ragg.equals("C"))
                  myselect += " AND u.codice=a.dom_citta";
                                    else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.dom_areadis";
                                    else if (ragg.equals("P"))
                 myselect += " AND u.codice=ski.ski_cod_presidio";
                                  }

        else if (((String)par.get("dom_res")).equals("R"))
                        {
                        if (ragg.equals("C"))
                  myselect += " AND u.codice=a.citta";
                else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.areadis";
                else if (ragg.equals("P"))
                    myselect += " AND u.codice=ski.ski_cod_presidio";
                        }


	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));*/

       myselect += " AND a.n_cartella=c.n_cartella"+
         " AND op.codice=ski.ski_infermiere"+
         " AND me.mecodi=a.cod_med "+
         " AND ski.n_cartella=c.n_cartella"+
         " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"+
         " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";

        myselect =myselect+" ORDER BY cod_oper,descrizione_zona,"+
                          "des_distr,descrizione,c.cognome,c.nome,c.n_cartella"       ;
	return myselect;
}

private void StampaGenerale(Hashtable hGenerale,mergeDocument doc,ISASConnection dbc, String stampa)
throws SQLException{

try
{
      Vector tab_riep=new Vector();
      Hashtable hZona= new Hashtable();
      Enumeration enumGenerale =orderedKeys( hGenerale);
      Hashtable hConta=new Hashtable();
      Hashtable htitolo=new Hashtable();
      while (enumGenerale.hasMoreElements())
      {
          String chiave = "" + enumGenerale.nextElement();
          
          htitolo.put("#operatore#",""+hOperatore.get(chiave));
          if (!chiave.equals("NO_OPERATORE"))
                  doc.writeSostituisci("operatore",htitolo) ;
          if (hGenerale.get(chiave)!=null)
          {
             hZona=(Hashtable)hGenerale.get(chiave);
             StampaDaZona( hZona, doc);
             if (!chiave.equals("NO_OPERATORE"))
             {
                   hConta.put("#descrizione#","Totale numero assistiti per operatore:");
                   hConta.put("#totale#",""+hContaOperatore.size());
                   doc.writeSostituisci("totale",hConta);
                   doc.write("taglia");
             }
             hContaOperatore.clear();
          }
      }//fine ciclo
       hConta.put("#descrizione#","Totale generale numero assistiti:");
       hConta.put("#totale#",""+hContaGenerale.size());
       doc.writeSostituisci("totale",hConta);

}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaOperatore");
	}

}
public Hashtable AnalizzaOperatore(ISASConnection dbc,ISASRecord dbr ,Hashtable hGenerale, String stampa)
throws SQLException
{
        try
        {
          Hashtable hZona= new Hashtable();
          if (dbr.get("cod_oper")!=null && !((String)dbr.get("cod_oper")).equals(""))
          {
              String  chiave=(String)dbr.get("cod_oper");
              if (dbr.get("operatore")!=null )
                      hOperatore.put(chiave,"" +dbr.get("operatore"));
              if (hGenerale!=null && ((Hashtable)hGenerale.get(chiave))!=null)
                  hZona=(Hashtable)hGenerale.get(chiave);
              hZona=AnalizzaZona(dbc,dbr,hZona,chiave,stampa);
              hGenerale.put(chiave,hZona);
          }
          return hGenerale;

 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaOperatore");
	}

}


private void StampaDaZona(Hashtable hzona,mergeDocument doc)
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
                  if(this.dom_res==null)
                  {
                  if (ragg.equals("A"))
                      tipologia="Area Distr.";
                  else if (ragg.equals("C"))
                        tipologia="Comune";
                  else if (ragg.equals("P"))
                      tipologia="Presidio";
                  }else if (this.dom_res.equals("D"))
                  {
                      if (ragg.equals("A"))
                          tipologia="Area Distr. di Domicilio";
                      else if (ragg.equals("C"))
                            tipologia="Comune di Domicilio";                      
                      else if (ragg.equals("P"))
                    	  tipologia="Presidio";
                     
                      }else if (this.dom_res.equals("R"))
                      {
                          if (ragg.equals("A"))
                              tipologia="Area Distr. di Residenza";
                          else if (ragg.equals("C"))
                                tipologia="Comune di Residenza";
                          else if (ragg.equals("P"))
                              tipologia="Presidio";
                          }  
//                  if (ragg.equals("A"))
//                      tipologia="Area Distr.";
//                  else if (ragg.equals("C"))
//                        tipologia="Comune";
                  htab.put("#tipologia#",tipologia);
                  htab.put("#descrizione#",comune);
                  doc.writeSostituisci("zona",htab);
                  doc.write("iniziotab");
                  Enumeration enumVett = vDati.elements();
                  while (enumVett.hasMoreElements())
                  {
                      Hashtable hDati=(Hashtable)enumVett.nextElement();
                      hContaGenerale.put(""+hDati.get("#cartella#"),"");
                      hContaOperatore.put(""+hDati.get("#cartella#"),"");
                      hContaRagg.put(""+hDati.get("#cartella#"),"");
                      if (vecchiaCartella.equals(""+hDati.get("#cartella#")))
                              hDati=PulisciCampi(hDati);
                      else
                            vecchiaCartella=""+hDati.get("#cartella#");
                      doc.writeSostituisci("tabella",hDati);
                  }//fine ciclo sul vettore
                  doc.write("finetab");
                  htab.put("#descrizione#","totale numero assistiti :");
                  htab.put("#totale#",""+hContaRagg.size());
                  doc.writeSostituisci("totale",htab);
                  hContaRagg.clear();
                  vecchiaCartella="";
              }//fine  ciclo comuni
        }//fine  ciclo distretti
    }//fine  ciclo zona
}

private Hashtable PulisciCampi(Hashtable hDati)
{//questa funzione sbianca i campi dell'assistito gi� stampato
    hDati.put("#cartella#","");
    hDati.put("#assistito#","");
    hDati.put("#data_nasc#","");
    //J23/04/2004
    //hDati.put("#indirizzo#","");
    //hDati.put("#citta#","");
    //hDati.put("#telefono1#","");
    //hDati.put("#nome_camp#","");
    //Fine J23/04/2004
    hDati.put("#inf_refe#","");
    return hDati;
}
private Hashtable AnalizzaZona(ISASConnection dbc,ISASRecord dbr,Hashtable hzona,String chiave, String stampa)
throws SQLException
{
        try
        {
            Hashtable hdistretti=new Hashtable();
            if (dbr.get("descrizione_zona")!=null && !((String)dbr.get("descrizione_zona")).equals(""))
            {
                String  zona=(String)dbr.get("descrizione_zona");
                if (hzona!=null && ((Hashtable)hzona.get(zona))!=null)
                      hdistretti=(Hashtable)hzona.get(zona);
                hdistretti=AnalizzaDistretti(dbc,dbr,hdistretti,chiave,stampa);
                hzona.put(zona,hdistretti);
            }//fine descrizione distretti
            return hzona;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaZona");
	}
}

private Hashtable AnalizzaDistretti(ISASConnection dbc,ISASRecord dbr,Hashtable hdistretti,String chiave, String stampa)
throws SQLException
{
        try
        {
            Hashtable hcomuni=new Hashtable();
            if (dbr.get("des_distr")!=null && !((String)dbr.get("des_distr")).equals(""))
            {
                String  distretto=(String)dbr.get("des_distr");
                if (hdistretti!=null && ((Hashtable)hdistretti.get(distretto))!=null)
                      hcomuni=(Hashtable)hdistretti.get(distretto);
                hcomuni=AnalizzaComuni(dbc,dbr,hcomuni,stampa);
                hdistretti.put(distretto,hcomuni);
            }//fine descrizione distretti
            return hdistretti;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaDistretti");
	}
}

private Hashtable AnalizzaComuni(ISASConnection dbc,ISASRecord dbr,Hashtable hcomuni, String stampa)
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
if(stampa.equals("S"))
	vDati=caricaDatiSint(dbr,vDati);
else
	vDati=caricaDatiAna(dbc,dbr,vDati);
                hcomuni.put(comune,vDati);
            }//fine descrizione comuni
            return hcomuni;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaComuni");
	}
}

public  Vector caricaDatiSint(ISASRecord dbr ,Vector vDati)
throws SQLException
{
        try
        {
          Hashtable tab=new Hashtable();
          int cartella=0;
          if (dbr.get("n_cartella")!=null)
              cartella=((Integer)dbr.get("n_cartella")).intValue();
          tab.put("#cartella#","" + cartella);

          String cognome="";
          if (dbr.get("cognome")!=null && !((String)dbr.get("cognome")).equals(""))
                 cognome=(String)dbr.get("cognome");

          String nome="";
          if (dbr.get("nome")!=null && !((String)dbr.get("nome")).equals(""))
                  nome=(String)dbr.get("nome");
          tab.put("#assistito#",cognome.trim()+" " + nome.trim());

          String data_nasc="";
          if (dbr.get("data_nasc")!=null)
          {
            data_nasc=((java.sql.Date)dbr.get("data_nasc")).toString();
            if (data_nasc.length()==10)
              data_nasc=data_nasc.substring(8,10)+"/"+
                    data_nasc.substring(5,7)+"/"+data_nasc.substring(0,4);
          }
          tab.put("#data_nasc#",data_nasc);

           vDati.add(tab);
           return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}

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
		debugMessage("FoInfEleAssEJB.getMotivoUscita(): "+e);
		throw new SQLException("Errore eseguendo getMotivoUscita()");
	}
}

public  Vector caricaDatiAna(ISASConnection dbc,ISASRecord dbr ,Vector vDati)
throws SQLException
{
        try
        {
          Hashtable tab=new Hashtable();
          int cartella=0;
          if (dbr.get("n_cartella")!=null)
              cartella=((Integer)dbr.get("n_cartella")).intValue();
          tab.put("#cartella#","" + cartella);

          String cognome="";
          if (dbr.get("cognome")!=null && !((String)dbr.get("cognome")).equals(""))
                 cognome=(String)dbr.get("cognome");

          String nome="";
          if (dbr.get("nome")!=null && !((String)dbr.get("nome")).equals(""))
                  nome=(String)dbr.get("nome");
          tab.put("#assistito#",cognome.trim()+" " + nome.trim());

          String data_nasc="";
          if (dbr.get("data_nasc")!=null)
          {
            data_nasc=((java.sql.Date)dbr.get("data_nasc")).toString();
            if (data_nasc.length()==10)
              data_nasc=data_nasc.substring(8,10)+"/"+
                    data_nasc.substring(5,7)+"/"+data_nasc.substring(0,4);
          }
          tab.put("#data_nasc#",data_nasc);

          /* J23/03/2004
          String indirizzo="";
          if (dbr.get("indirizzo")!=null && !((String)dbr.get("indirizzo")).equals(""))
                  indirizzo=(String)dbr.get("indirizzo");
          tab.put("#indirizzo#",indirizzo.trim()+" - ");

          String citta="";
          if (dbr.get("citta")!=null && !((String)dbr.get("citta")).equals(""))
                  citta=(String)dbr.get("citta");
          tab.put("#citta#",getComune(dbc,citta));
          */
          String ski_data_apertura="";
          if (dbr.get("ski_data_apertura")!=null)
          {
            ski_data_apertura=((java.sql.Date)dbr.get("ski_data_apertura")).toString();
            if (ski_data_apertura.length()==10)
              ski_data_apertura=ski_data_apertura.substring(8,10)+"/"+
                    ski_data_apertura.substring(5,7)+"/"+ski_data_apertura.substring(0,4);
          }
          tab.put("#reg_data_in#",ski_data_apertura);

          String ski_data_uscita="";
          if (dbr.get("ski_data_uscita")!=null)
          {
            ski_data_uscita=((java.sql.Date)dbr.get("ski_data_uscita")).toString();
            if (ski_data_uscita.length()==10)
              ski_data_uscita=ski_data_uscita.substring(8,10)+"/"+
                    ski_data_uscita.substring(5,7)+"/"+ski_data_uscita.substring(0,4);
          }
          tab.put("#reg_data_out#",ski_data_uscita);
          //J23/03/2004
          String descr="";
          if (dbr.get("ski_descr_contatto")!=null && !((String)dbr.get("ski_descr_contatto")).equals(""))
            descr=(String)dbr.get("ski_descr_contatto");
		if (this.ubic.equals("3") && dbr.get("ski_tipocura")!=null)
			descr = (String)util.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "SAOADI", (String)dbr.get("ski_tipocura"), "tab_descrizione");
          tab.put("#descr_contatto#",descr);
          /*J23/03/2004
          String nome_camp="";
          if (dbr.get("nome_camp")!=null && !((String)dbr.get("nome_camp")).equals(""))
                  nome_camp=(String)dbr.get("nome_camp");
          tab.put("#nome_camp#",nome_camp);

          String telefono1="";
          if (dbr.get("telefono1")!=null && !((String)dbr.get("telefono1")).equals(""))
                  telefono1=(String)dbr.get("telefono1");
          tab.put("#telefono1#",telefono1);
          */
          String referente="";
          if (dbr.get("referente")!=null && !((String)dbr.get("referente")).equals(""))
                  referente=(String)dbr.get("referente");
          tab.put("#inf_refe#",referente);

          String motivo=getMotivoUscita(dbr,dbc);
          tab.put("#skf_motivo_chius#",motivo);
          vDati.add(tab);
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
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
			condWhere += " AND u.codice  = ski.ski_cod_presidio ";
		}
	} else if (dom_res.equals("D")) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND u.codice=a.dom_citta";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND u.codice=a.dom_areadis";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = ski.ski_cod_presidio ";
		}
	} else if (dom_res.equals("R")) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND u.codice=a.citta";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND u.codice=a.areadis";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = ski.ski_cod_presidio ";
		}
	}
	return condWhere;
}
}	// End of FoMAssEle class


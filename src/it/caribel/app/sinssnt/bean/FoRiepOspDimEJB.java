
package it.caribel.app.sinssnt.bean;
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 31/03/2005 - EJB di connessione alla procedura SINS Tabella FoRiepOspDim
//
//bargi
//
// ==========================================================================


import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.math.*;

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
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.DataWI;

public class FoRiepOspDimEJB extends SINSSNTConnectionEJB {

	String dom_res;
	String dr;
//hash per decodifica reparto
Hashtable hReparto= new Hashtable();
Hashtable hDescRep = new Hashtable();
///hash per decodifica ospedale
Hashtable hOspedale= new Hashtable();
String ragg="";
String data_fine= "";
String distretto = "";
String zona = "";
String pca = "";
String reparto = "";
String ospedale = "";
// se ho pi� schede coppie per uno stesso
//utente devo stampare solo i dati diversi
String vecchiaCartella="";
//hash per calcolare il numero totale di assistiti
Hashtable hContaGenerale= new Hashtable();
Hashtable hContaOspedale= new Hashtable();
private void preparaLayout(mergeDocument md, ISASConnection dbc,String data_inizio,String data_fine,Hashtable par) {
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
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        htxt.put("#assistenza#", "");

//gb 01/08/07 *******
	if (!((String)par.get("zona")).equals(""))
	   {
	   zona = (String)par.get("zona");
	   htxt.put("#zona#",  decodifica("zone","codice_zona",zona,"descrizione_zona",dbc));
	   }
	else
	   htxt.put("#zona#",  " ");
	if (!((String)par.get("distretto")).equals(""))
	   {
	   distretto = (String)par.get("distretto");
	   htxt.put("#distretto#",  decodifica("distretti","cod_distr",distretto,"des_distr",dbc));
	   }
	else
	   htxt.put("#distretto#",  " ");

	
	if (!((String)par.get("pca")).equals(""))
	   pca = (String)par.get("pca");
	htxt.put("#pca#",  " ");
	
	
	if (this.dom_res == null)
	{
	if (((String)par.get("ragg")).equals("C"))
	   {
           htxt.put("#titolo_pca#",  "Comune");
		   
	   if (!pca.equals(""))
	      htxt.put("#pca#",  decodifica("comuni", "codice", pca, "descrizione", dbc));
	   }
	else if (((String)par.get("ragg")).equals("A"))
	   {
	   htxt.put("#titolo_pca#",  "Area Distr.");
	   if (!pca.equals(""))
	      htxt.put("#pca#",  decodifica("areadis", "codice", pca, "descrizione", dbc));
	   }
	//M.Minerba 20/02/2013 per Pistoia
	else if (((String)par.get("ragg")).equals("P"))
	   {
	   htxt.put("#titolo_pca#",  "Presidio");
	   if (!pca.equals(""))
	      htxt.put("#pca#",  decodifica("presidi", "codpres", pca, "despres", dbc));
	   }//fine M.Minerba 20/02/2013 per Pistoia
	}else if (this.dom_res.equals("R"))
	{
		if (((String)par.get("ragg")).equals("C"))
		   {
	           htxt.put("#titolo_pca#",  "Comune di Residenza");
		   if (!pca.equals(""))
		      htxt.put("#pca#",  decodifica("comuni", "codice", pca, "descrizione", dbc));
		   }
		else if (((String)par.get("ragg")).equals("A"))
		   {
		   htxt.put("#titolo_pca#",  "Area Distr. di Residenza");
		   if (!pca.equals(""))
		      htxt.put("#pca#",  decodifica("areadis", "codice", pca, "descrizione", dbc));
		   }
		//M.Minerba 20/02/2013 per Pistoia
		else if (((String)par.get("ragg")).equals("P"))
		   {
		   htxt.put("#titolo_pca#",  "Presidio");
		   if (!pca.equals(""))
		      htxt.put("#pca#",  decodifica("presidi", "codpres", pca, "despres", dbc));
		   }//fine M.Minerba 20/02/2013 per Pistoia
		}else if (this.dom_res.equals("D"))
		{
			if (((String)par.get("ragg")).equals("C"))
			   {
		           htxt.put("#titolo_pca#",  "Comune di Domicilio");
			   if (!pca.equals(""))
			      htxt.put("#pca#",  decodifica("comuni", "codice", pca, "descrizione", dbc));
			   }
			else if (((String)par.get("ragg")).equals("A"))
			   {
			   htxt.put("#titolo_pca#",  "Area Distr. di Domicilio");
			   if (!pca.equals(""))
			      htxt.put("#pca#",  decodifica("areadis", "codice", pca, "descrizione", dbc));
			   }
			//M.Minerba 20/02/2013 per Pistoia
			else if (((String)par.get("ragg")).equals("P"))
			   {
			   htxt.put("#titolo_pca#",  "Presidio");
			   if (!pca.equals(""))
			      htxt.put("#pca#",  decodifica("presidi", "codpres", pca, "despres", dbc));
			   }//fine M.Minerba 20/02/2013 per Pistoia
			}
//gb 01/08/07: fine *******
		
	 		String Tipooper= ((String)par.get("tipoope")).trim();
	 			if(Tipooper.equals("I"))
	 				 htxt.put("#tipoOperatore#","INFERMIERI");
	 						 else   htxt.put("#tipoOperatore#","MEDICI");
	
        md.writeSostituisci("layout",htxt);
}

public static String getjdbcDate()
{
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}

public FoRiepOspDimEJB() {}

private String mkSelectInfermiere(ISASConnection dbc, Hashtable par) {
  ServerUtility su = new ServerUtility();
          String myselect="";
          debugMessage("mkSelectInfermiere");
            myselect= "SELECT c.n_cartella, "+
		" c.cognome, c.nome, c.data_nasc, c.cod_com_nasc,"+
		" ski_osp_dim osp_dim,ski_uo_dim uo_dim,ski_data_apertura data_apertura"+
//gb 01/08/07		" FROM cartella c, skinf s WHERE"+
		" FROM cartella c, anagra_c a, skinf s,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u" + //gb 01/08/07
		" WHERE s.n_cartella=c.n_cartella";

        if (par.get("data_fine") != null)
		myselect = myselect+" AND s.ski_data_apertura<="+
			formatDate(dbc,(String)par.get("data_fine"));

        if (par.get("data_inizio") != null)
		myselect = myselect+" AND s.ski_data_apertura>="+
			formatDate(dbc,(String)par.get("data_inizio"));

        String ospedale="";
        if (par.get("ospedale") != null && !((String)par.get("ospedale")).equals("")){
          ospedale=(String)(par.get("ospedale"));
          myselect=myselect+" AND ski_osp_dim='"+ospedale+"'";
        }
        String reparto="";
        if (par.get("reparto") != null && !((String)par.get("reparto")).equals("")){
           reparto=(String)(par.get("reparto"));
           myselect=myselect+" AND ski_uo_dim='"+reparto+"'";
        }

//gb 01/08/07 *******
        String ragg = (String)par.get("ragg");
        myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

//          if (ragg!=null && ragg.equals("C"))
//          myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                " AND u.codice=a.dom_citta)"+
//                " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                " AND u.codice=a.citta))";
//        else if (ragg!=null && ragg.equals("A"))
//          myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                " AND u.codice=a.dom_areadis)"+
//                " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                " AND u.codice=a.areadis))";

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
                //M.Minerba 20/02/2013 per Pistoia
                else if (ragg.equals("P"))
                	myselect += " AND u.codice=s.ski_cod_presidio";
                //fine M.Minerba 20/02/2013 per Pistoia
        }
        else if (((String)par.get("dom_res")).equals("D"))
                                  {
                                   if (ragg.equals("C"))
                  myselect += " AND u.codice=a.dom_citta";
                                    else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.dom_areadis";
                                 //M.Minerba 20/02/2013 per Pistoia
                                    else if (ragg.equals("P"))
                 myselect += " AND u.codice=s.ski_cod_presidio";
                                //fine M.Minerba 20/02/2013 per Pistoia
                                  }

        else if (((String)par.get("dom_res")).equals("R"))
                        {
                        if (ragg.equals("C"))
                  myselect += " AND u.codice=a.citta";
                else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.areadis";
                      //M.Minerba 20/02/2013 per Pistoia
                else if (ragg.equals("P"))
                	myselect += " AND u.codice=s.ski_cod_presidio";
                //fine M.Minerba 20/02/2013 per Pistoia
                        }
      
        
	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));

       myselect += " AND a.n_cartella=c.n_cartella"+
         " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"+
	 " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";
//gb 01/08/07: fine *******

        myselect=myselect+" AND (ski_uo_dim is not null or ski_osp_dim is not null)";
        myselect+=" ORDER BY ski_osp_dim,ski_uo_dim,c.cognome,c.n_cartella";
	return myselect;
}

private String mkSelectMedico(ISASConnection dbc, Hashtable par) {
  ServerUtility su = new ServerUtility();
          String myselect="";
          debugMessage("mkSelectMedico");
            myselect= "SELECT c.n_cartella, "+
		" c.cognome, c.nome, c.data_nasc, c.cod_com_nasc,"+
		" skm_osp_dim osp_dim,skm_uo_dim uo_dim,skm_data_apertura data_apertura"+
		" FROM cartella c, skmedico s WHERE"+
		" s.n_cartella=c.n_cartella";

        if (par.get("data_fine") != null)
		myselect = myselect+" AND s.skm_data_apertura<="+
			formatDate(dbc,(String)par.get("data_fine"));

        if (par.get("data_inizio") != null)
		myselect = myselect+" AND s.skm_data_apertura>="+
			formatDate(dbc,(String)par.get("data_inizio"));

        String ospedale="";
        if (par.get("ospedale") != null && !((String)par.get("ospedale")).equals("")){
          ospedale=(String)(par.get("ospedale"));
          myselect=myselect+" AND skm_osp_dim='"+ospedale+"'";
        }
        String reparto="";
        if (par.get("reparto") != null && !((String)par.get("reparto")).equals("")){
           reparto=(String)(par.get("reparto"));
           myselect=myselect+" AND skm_uo_dim='"+reparto+"'";
        }
        myselect=myselect+" AND (skm_uo_dim is not null or skm_osp_dim is not null)";
        myselect+=" ORDER BY skm_osp_dim,skm_uo_dim,c.cognome,c.n_cartella";
	return myselect;
}

public byte[] query_ospdim(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	byte[] jessy;

        String dataini="";
        String datafine="";
        String tipo="";
	boolean entrato=false;
	try{
		String type = par.get("TYPE")!=null?par.get("TYPE").toString():"PDF";
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);

		if (par.get("data_fine") != null && !((String)par.get("data_fine")).equals(""))
			datafine=(String)par.get("data_fine");
                data_fine=datafine;
		if (par.get("data_inizio") != null && !((String)par.get("data_inizio")).equals(""))
			dataini=(String)par.get("data_inizio");
                String Tipooper= ((String)par.get("tipoope")).trim();
                debugMessage("Tipo operatore =["+Tipooper+"]");
		String myselect ="";
                if(Tipooper.equals("I"))
                 myselect=mkSelectInfermiere(dbc, par);
                else myselect=mkSelectMedico(dbc, par);
                System.out.println("SELECT che eseguo=="+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
                preparaLayout(doc,dbc,dataini,datafine,par);
                Hashtable hGenerale=new Hashtable();
		
		while (dbcur.next()){
                   ISASRecord dbr= dbcur.getRecord();
                   hGenerale=AnalizzaOspedale(dbc,dbr,hGenerale);
                }
                if (hGenerale.size()!=0)
                   StampaGenerale(hGenerale,doc,dbc);
                else
                      doc.write("messaggio");
		
                doc.write("finale");
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
                //System.out.println("Rit:"+new String(doc.get()));
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_ospdim()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}


/*

private void mkSinteticaExcel(ISASConnection dbc, ISASCursor dbcur, Hashtable par, mergeDocument doc)
    throws Exception{
            Hashtable p = new Hashtable();
            String ragg = faiRaggruppamento((String)par.get("ragg"));
            String figprof = "";
			String tipocura = "";
			String data_nasc = "";
			p.put("#raggruppamento#", ragg);
			System.out.println("FoInfEleMotEJB: 2");
            doc.writeSostituisci("iniziotab", p);
			p.clear();
			
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
				p.put("#descrizione_zona#", dbr.get("des_zona").toString());
                p.put("#des_distr#", dbr.get("des_distretto").toString());
                p.put("#descrizione#",dbr.get("descrizione").toString());
			    p.put("#nome_ass#", (String)dbr.get("cognome")+" "+(String)dbr.get("nome"));
				p.put("#data_apertura#", dbr.get("data_apertura").toString());
				p.put("#osp_dim#",getOspedaleDim(dbr,dbc));
				p.put("#uo_dim#",getRepartoDim(dbr,dbc));
				data_nasc = ((java.sql.Date)dbr.get("data_nasc")).toString();
		p.put("#data_nasc#", data_nasc!=null?data_nasc:"");
		p.put("#eta#",""+ConvertData(data_nasc,data_fine));
		if (dbr.get("n_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
              p.put("#n_cartella#",((Integer)dbr.get("n_cartella")).toString());
        
		 
		  
				doc.writeSostituisci("inizio_riga",p);
				p.clear();
            }
            doc.write("finetab");
    }
*/

private void StampaGenerale(Hashtable hGenerale,mergeDocument doc,ISASConnection dbc)
throws SQLException{
try{
      Vector tab_riep=new Vector();
      Enumeration enumGenerale =orderedKeys(hGenerale);
      Hashtable hConta=new Hashtable();
      Hashtable htitolo=new Hashtable();
      while (enumGenerale.hasMoreElements()){
          String chiave = "" + enumGenerale.nextElement();
          htitolo.put("#ospedale#",""+hOspedale.get(chiave));
		  this.ospedale = ""+hOspedale.get(chiave);
          doc.writeSostituisci("ospedale",htitolo) ;
          if (hGenerale.get(chiave)!=null)
          {
             hReparto=(Hashtable)hGenerale.get(chiave);
          }
          StampaDaReparto( hReparto, doc);
         hConta.put("#descrizione#","Totale numero assistiti per ospedale:");
         hConta.put("#totale#",""+hContaOspedale.size());
         doc.writeSostituisci("totale",hConta);
         doc.write("taglia");
             hContaOspedale.clear();
      }//fine ciclo
      hConta.put("#descrizione#","Totale generale numero assistiti:");
      hConta.put("#totale#",""+hContaGenerale.size());
      doc.writeSostituisci("totale",hConta);
}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una StampaGenerale");
	}
}

public Hashtable AnalizzaOspedale(ISASConnection dbc,ISASRecord dbr ,Hashtable hGenerale)
throws SQLException
{
  String chiave="";
  String descri ="";

        try{
            Vector vDati=new Vector();
            if (dbr.get("osp_dim")!=null && !((String)dbr.get("osp_dim")).equals("")){
              chiave=(String)dbr.get("osp_dim");
              descri = getOspedaleDim(dbr,dbc);
            }else{
              chiave="NIENTE";
              descri="NON RILEVATA";
            }
            hOspedale.put(chiave,descri);
            if (hGenerale!=null && ((Hashtable)hGenerale.get(chiave))!=null)
               hReparto=(Hashtable)hGenerale.get(chiave);
            else
               hReparto=new Hashtable();
              hReparto=AnalizzaReparto(dbc,dbr,hReparto);
              hGenerale.put(chiave,hReparto);
          return hGenerale;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaOspedale");
	}

}
private Hashtable AnalizzaReparto(ISASConnection dbc,ISASRecord dbr,Hashtable hreparto)
throws SQLException
{
  String chiave="";
  String descri ="";
        try
        {
            Vector vDati=new Vector();
            if (dbr.get("uo_dim")!=null && !((String)dbr.get("uo_dim")).equals("")){
              chiave=(String)dbr.get("uo_dim");
              descri = getRepartoDim(dbr,dbc);
            }else{
              chiave="NIENTE";
              descri="NON RILEVATA";
            }
            hDescRep.put(chiave,descri);
            if (hreparto!=null && ((Vector)hreparto.get(chiave))!=null)
                     vDati=(Vector)hreparto.get(chiave);
                        vDati=caricaDati(dbc,dbr,vDati);
                        hreparto.put(chiave,vDati);
            return hreparto;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaReparto");
	}
}
private void StampaDaReparto(Hashtable hreparto,mergeDocument doc)
{
    Hashtable hContaRagg= new Hashtable();
    Enumeration enumReparti = orderedKeys(hreparto);
       while (enumReparti.hasMoreElements())
      {
          String reparto = "" + enumReparti.nextElement();
          Vector vDati = (Vector) hreparto.get(reparto);
          Hashtable htab=new Hashtable();
          htab.put("#reparto#",(String)hDescRep.get(reparto));
		  this.reparto = (String)hDescRep.get(reparto);
          doc.writeSostituisci("reparto",htab);
          doc.write("iniziotab");

          Enumeration enumVett = vDati.elements();
          while (enumVett.hasMoreElements())
          {
              Hashtable hDati=(Hashtable)enumVett.nextElement();
			  hDati.put("#reparto#",this.reparto);
			  hDati.put("#ospedale#",this.ospedale);
              hContaGenerale.put(""+hDati.get("#cartella#"),"");
              hContaOspedale.put(""+hDati.get("#cartella#"),"");
              hContaRagg.put(""+hDati.get("#cartella#"),"");
              doc.writeSostituisci("tabella",hDati);
          }//fine ciclo sul vettore
          doc.write("finetab");
          htab.put("#descrizione#","totale numero assistiti per reparto :");
          htab.put("#totale#",""+hContaRagg.size());
          doc.writeSostituisci("totale",htab);
          hContaRagg.clear();
          vecchiaCartella="";
    }//fine  ciclo

}
private Hashtable PulisciCampi(Hashtable hDati)
{//questa funzione sbianca i campi dell'assistito gi� stampato
    hDati.put("#cartella#","");
    hDati.put("#assistito#","");
    hDati.put("#data_nasc#","");
    return hDati;
}

public  Vector caricaDati(ISASConnection dbc,ISASRecord dbr ,Vector vDati)
throws SQLException{
        try{
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

          if (dbr.get("data_nasc")!=null){
            data_nasc=((java.sql.Date)dbr.get("data_nasc")).toString();
            if (data_nasc.length()==10)
              data_nasc=data_nasc.substring(8,10)+"/"+
                    data_nasc.substring(5,7)+"/"+data_nasc.substring(0,4);
          }

            int eta=0;
            //calcolo eta'
            String mydate="";
            if (dbr.get("data_nasc")!=null)
            {
                String datarif=data_fine;
                mydate=((java.sql.Date)dbr.get("data_nasc")).toString();
                if (!mydate.equals(""))
                  eta=this.ConvertData(mydate, datarif);
                else eta=0;
            }
          tab.put("#data_nasc#",data_nasc);
          tab.put("#eta#",""+eta);
          String data_apertura="";

          if (dbr.get("data_apertura")!=null){
            data_apertura=((java.sql.Date)dbr.get("data_apertura")).toString();
            if (data_apertura.length()==10)
              data_apertura=data_apertura.substring(8,10)+"/"+
                    data_apertura.substring(5,7)+"/"+data_apertura.substring(0,4);
          }
          tab.put("#data_apertura#",data_apertura);
          vDati.add(tab);
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}
}



private String decodifica(String tabella, String nome_cod,
	Object val_codice,String descrizione,ISASConnection dbc) {

	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione +
			" descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
		//debugMessage("FoEleSocEJB.decodifica(): "+mysel);
                ISASRecord dbtxt = dbc.readRecord(mysel);
//gb 01/08/07                if (dbtxt==null && dbtxt.get("descrizione")==null)return " ";
                if (dbtxt==null || dbtxt.get("descrizione")==null)return " "; //gb 01/08/07: altrimenti se dbtxt � null schianterebbe
		return ((String)dbtxt.get("descrizione"));
	} catch (Exception ex) {
		return " ";
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
private String getOspedaleDim(ISASRecord dbr,ISASConnection dbc)
throws SQLException{
        String decod="";
        try {
                 String codice=(String)dbr.get("osp_dim");
                 String sel="SELECT descosp FROM ospedali "+
                            " WHERE codosp='"+codice +"'";
                 //System.out.println("OSPEDALE DIMISSIONE-->"+sel);
                 ISASRecord dbDecod=dbc.readRecord(sel);
                 if (dbDecod!=null && dbDecod.get("descosp")!=null)
                 {
                     decod=(String)dbDecod.get("descosp");
                 }
          return decod;
	} catch(Exception e) {
		debugMessage("ForRiepOspDimEJB.getOspedaleDim(): "+e);
		throw new SQLException("Errore eseguendo getOspedaleDim()");
	}
}
private String getRepartoDim(ISASRecord dbr,ISASConnection dbc)
throws SQLException{
        String decod="";
        try {
                 String codice=(String)dbr.get("uo_dim");
                 String sel="SELECT reparto FROM reparti "+
                            " WHERE cd_rep='"+codice +"'";
                 //System.out.println("OSPEDALE DIMISSIONE-->"+sel);
                 ISASRecord dbDecod=dbc.readRecord(sel);
                 if (dbDecod!=null && dbDecod.get("reparto")!=null)
                 {
                     decod=(String)dbDecod.get("reparto");
                 }
          return decod;
	} catch(Exception e) {
		debugMessage("ForRiepOspDimEJB.getrepartoDim(): "+e);
		throw new SQLException("Errore eseguendo getrepartoDim()");
	}
}
public int ConvertData (String dataold,String datanew)
{
        //inizializzazione della variabile eta
        int eta=0;
        int tempeta=0;

        //preparazione primo array
        int[] datavecchia= new int[3];
        Integer giorno = new Integer(dataold.substring(8,10));
        datavecchia[0]= giorno.intValue();
        Integer mese = new Integer(dataold.substring(5,7));
        datavecchia[1]= mese.intValue();
        Integer anno = new Integer(dataold.substring(0,4));
        datavecchia[2]= anno.intValue();

        //preparazione secondo array

        int[] datanuova= new int[3];
        Integer day = new Integer(datanew.substring(8,10));
        datanuova[0]= day.intValue();
        Integer mounth = new Integer(datanew.substring(5,7));
        datanuova[1]= mounth.intValue();
         Integer year = new Integer(datanew.substring(0,4));
        datanuova[2]= year.intValue();

        tempeta= datanuova[2]-datavecchia[2];
        //confronto mese
        if (datanuova[1] < datavecchia[1])
                tempeta=tempeta-1;      // anni non ancora compiuti
        else if (datanuova[1] == datavecchia[1])
                if (datanuova[0] < datavecchia[0])      //confronto giorno
                        tempeta=tempeta-1;      // anni non ancora compiuti
        eta=tempeta;
        return eta;
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
        else if(tipo.trim().equals("P"))
    		raggruppa=" Presidio ";
        else	raggruppa="TIPO NON VALIDO";
        }else if (this.dom_res.equals("R"))
        {
            if (tipo.trim().equals("A"))
            	raggruppa="Area Distr. di Residenza";
            else if (tipo.trim().equals("C"))
            	raggruppa="Comune di Residenza";
            else if(tipo.trim().equals("P"))
        		raggruppa=" Presidio ";
            else	raggruppa="TIPO NON VALIDO";
            }  	

	return raggruppa;
}
}	// End of FoMAssEle class


package it.caribel.app.sinssnt.bean;
// --------------------------------------------------------------------------
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//Minerba Mariarita
//23/01/2013- EJB di stampa della procedura SINS Test sc_conley
//
// ==========================================================================

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

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.DataWI;

public class FoTestConleyEJB extends SINSSNTConnectionEJB {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

public FoTestConleyEJB() {}


private void preparaBody(ISASConnection dbc,mergeDocument doc, Hashtable par, String tp)
throws Exception{
	Hashtable ht=new Hashtable();
	int totale=0;
	FaiHashVuota(ht);	
	String mentale = "";
	String deambulazione = "";
	String comportamento = "";
	String umore = "";
	
	//doc.write("iniziotab");
	
	 ht.put("#domanda_pci#","");
	 ht.put("#domanda_c1#","");
	 ht.put("#domanda_c2#","");
	 ht.put("#domanda_c3#","");
	 ht.put("#domanda_det#","");
	 ht.put("#domanda_ag#","");
	 ht.put("#domanda_giu#","");	 
	 ht.put("#punteggio#","");
	 
	 String domanda_pci="";  
	 String domanda_c1="";	 
	 String domanda_c2="";	
	 String domanda_c3="";	
	 String domanda_det="";	
	 String domanda_ag="";	
	 String domanda_giu="";	 
	 
	 if(tp.equals("1")){     	
        	
        	if (par.get("domanda_pci")!=null && !((String)par.get("domanda_pci")).equals("")) {
       		 	domanda_pci=par.get("domanda_pci").toString();   
       		 	ht.put("#domanda_pci#",domanda_pci);
        	}
        	if (domanda_pci.equals(""))
         		domanda_pci="0";
        	
        	if (par.get("domanda_c1")!=null && !((String)par.get("domanda_c1")).equals("")) {
       		 	domanda_c1=par.get("domanda_c1").toString();   
       		 	ht.put("#domanda_c1#",domanda_c1);
        	}
        	if (domanda_c1.equals(""))
         		domanda_c1="0";
        	
        	if (par.get("domanda_c2")!=null && !((String)par.get("domanda_c2")).equals("")) {
       		 	domanda_c2=par.get("domanda_c2").toString();   
       		 	ht.put("#domanda_c2#",domanda_c2);
        	}
        	if (domanda_c2.equals(""))
         		domanda_c2="0";
        	
        	if (par.get("domanda_c3")!=null && !((String)par.get("domanda_c3")).equals("")) {
       		 	domanda_c3=par.get("domanda_c3").toString();   
       		 	ht.put("#domanda_c3#",domanda_c3);
        	}
        	if (domanda_c3.equals(""))
         		domanda_c3="0";
        	
        	if (par.get("domanda_det")!=null && !((String)par.get("domanda_det")).equals("")) {
       		 	domanda_det=par.get("domanda_det").toString();   
       		 	ht.put("#domanda_det#",domanda_det);
        	}
        	if (domanda_det.equals(""))
         		domanda_det="0";
        	
        	if (par.get("domanda_ag")!=null && !((String)par.get("domanda_ag")).equals("")) {
       		 	domanda_ag=par.get("domanda_ag").toString();   
       		 	ht.put("#domanda_ag#",domanda_ag);
        	}
        	if (domanda_ag.equals(""))
         		domanda_ag="0";
        	
        	if (par.get("domanda_giu")!=null && !((String)par.get("domanda_giu")).equals("")) {
       		 	domanda_giu=par.get("domanda_giu").toString();   
       		 	ht.put("#domanda_giu#",domanda_giu);
        	}
        	if (domanda_giu.equals(""))
         		domanda_giu="0";
        	
        	int punteggio=Integer.parseInt(domanda_c1)+Integer.parseInt(domanda_c2)+ Integer.parseInt(domanda_c3)+Integer.parseInt(domanda_det)+Integer.parseInt(domanda_ag)+Integer.parseInt(domanda_giu);
            ht.put("#punteggio#","" + punteggio);
                        	                        	
        }
	
	
        
    
	if(par.get("data_test")!=null)
		ht.put("#data_test#",
			(""+par.get("data_test")).substring(8,10)+"/"+
			(""+par.get("data_test")).substring(5,7)+"/"+
			(""+par.get("data_test")).substring(0,4));
	if(par.get("nome")!=null)
		ht.put("#nome_test#",""+par.get("nome"));
	if (par!=null && par.get("conley_punt")!=null)
        totale = Integer.parseInt(""+par.get("conley_punt"));
    if(totale>0)
        ht.put("#totale#",""+totale);
    
    doc.writeSostituisci("tabConley",ht);
}


private void FaiHashVuota(Hashtable ht){
	/*for (int i=1;i<6;i++){
	  ht.put("#punt"+i+"#","");
	}*/
	ht.put("#nome_test#","");
	ht.put("#data_test#","____/____/________");
	ht.put("#totale#", "__________");
}


public byte[] query_report(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;

	try{
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);

		//preparo i titoli di stampa
		preparaLayout(doc, dbc, par);

		Hashtable helabora = new Hashtable();
		if(par.get("tp").equals("1"))
                    helabora=LeggiDati(dbc,par);

                preparaBody(dbc,doc,helabora, (String)par.get("tp"));

		doc.write("finale");
		
		doc.close();
		dbc.close();
		super.close(dbc);
		done=true;
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("FoTestMdsUmore Errore eseguendo una query_report()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}

private Hashtable LeggiDati(ISASConnection dbc,Hashtable par)throws Exception{
  Hashtable hret = new Hashtable();
  try{

      String cartella = ""+par.get("cartella");
     //bargi 6/5/2009 schianta!! String pr_data = ""+par.get("pr_data");
      String data = ""+par.get("data");

      String mysel = "SELECT a.*,cartella.sesso FROM sc_conley a,cartella WHERE"+
		     " a.n_cartella="+cartella+
		     " AND a.data = "+formatDate(dbc,data)+
		     " AND a.n_cartella=cartella.n_cartella";
      ISASRecord dbr = dbc.readRecord(mysel);
      if(dbr!=null)
	hret = dbr.getHashtable();
      else hret = null;
  }catch(Exception e){
    System.out.println("FoTestConley Errore eseguendo una leggiDati: "+e);
  }
  return hret;
}




private void preparaLayout(mergeDocument md, ISASConnection dbc,Hashtable par){
	Hashtable htxt = new Hashtable();
	
	try {
		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ragione_sociale'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
		String cart = ""+par.get("cartella");
		it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
		htxt.put("#assistito#",ut.getDecode(dbc,"cartella","n_cartella",cart,
		      "nvl(cognome,'')|| ' ' ||nvl(nome,'')","nomeass"));
		
		if(par.get("tp").equals("1"))// caso stampa dati
		  htxt.put("#data#",""+par.get("data"));
		else //caso modello vuoto
		  htxt.put("#data#","____/____/________");
	} catch (Exception ex) {
		htxt.put("#txt#", "ragione_sociale");
		htxt.put("#assistito#", " ");
		htxt.put("#data#", "__/__/____");
	}
	ServerUtility su =new ServerUtility();
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	md.writeSostituisci("layout",htxt);
	
	
}

public static String getjdbcDate(){
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}



}	// End of FoTestAsgo class


package it.caribel.app.sinssnt.bean;
// --------------------------------------------------------------------------
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 15/12/2006 - EJB di stampa della procedura SINS Test Barthel
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

public class FoTestBarthelEJB extends SINSSNTConnectionEJB {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

public FoTestBarthelEJB() {}

private void preparaBody(ISASConnection dbc,mergeDocument doc, Hashtable par, String tp)
throws Exception{
	Hashtable ht=new Hashtable();
        Hashtable htot=new Hashtable();
        int chiave = 0;
        int totale=0;
        double totale_agg=0;
        for (int i=0; i<=11; i++){
          for (int j=0; j<=4; j++){
            ht.put("#check"+i+"."+j+"#", "");
          }
        }
        htot.put("#totale#","__________");
        htot.put("#data_test#","__/__/____");
        htot.put("#livello#","__________");
        if(tp.equals("1")){
          chiave=((Integer)par.get("skbt_alimentazione")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check1.0#", "x");
            break;
            case 5:
                 ht.put("#check1.1#", "x");
            break;
            case 10:
                 ht.put("#check1.2#", "x");
            break;
           
          }
          System.out.println("****2");
          chiave=((Integer)par.get("skbt_abbigliamento")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check2.0#", "x");
            break;
            case 5:
                 ht.put("#check2.1#", "x");
            break;
            case 10:
                 ht.put("#check2.2#", "x");
            break;
           
          }
          chiave=((Integer)par.get("skbt_toilette")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check3.0#", "x");
            break;
            case 1:
                 ht.put("#check3.1#", "x");
            break;
            case 5:
                 ht.put("#check3.2#", "x");
            break;
           
          }
          System.out.println("****3");
          chiave=((Integer)par.get("skbt_bagno")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check4.0#", "x");
            break;
            case 1:
                 ht.put("#check4.1#", "x");
            break;
            case 5:
                 ht.put("#check4.2#", "x");
            break;
            
          }
          chiave=((Integer)par.get("skbt_cont_intestinale")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check5.0#", "x");
            break;
            case 5:
                 ht.put("#check5.1#", "x");
            break;
            case 10:
                 ht.put("#check5.2#", "x");
            break;
            
          }
          chiave=((Integer)par.get("skbt_cont_urinaria")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check6.0#", "x");
            break;
            case 5:
                 ht.put("#check6.1#", "x");
            break;
            case 10:
                 ht.put("#check6.2#", "x");
            break;
			
          }
          chiave=((Integer)par.get("skbt_trasf_lettosedia")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check7.0#", "x");
            break;
            case 10:
                 ht.put("#check7.1#", "x");
            break;
			case 15:
                 ht.put("#check7.2#", "x");
            break;
			
          }
          chiave=((Integer)par.get("skbt_deambulazione")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check8.0#", "x");
            break;
            case 5:
                 ht.put("#check8.1#", "x");
            break;
            case 10:
                 ht.put("#check8.2#", "x");
            break;
           
          }
          chiave=((Integer)par.get("skbt_carrozzina")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check9.0#", "x");
            break;
            case 10:
                 ht.put("#check9.1#", "x");
            break;
			case 15:
                 ht.put("#check9.2#", "x");
            break;
			
          }
          chiave=((Integer)par.get("skbt_scale")).intValue();
          switch(chiave) {
            case 0:
                 ht.put("#check10.0#", "x");
            break;
            case 5:
                 ht.put("#check10.1#", "x");
            break;
            case 10:
                 ht.put("#check10.2#", "x");
            break;
            
          }
          
         
          if(par.get("data_test")!=null){
            String data=""+(java.sql.Date)par.get("data_test");
            data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
            htot.put("#data_test#", data);
          }

          if (par!=null && par.get("skbt_punt")!=null){
                  totale = Integer.parseInt(""+par.get("skbt_punt"));
                 
          }
          if(totale>0){
                  htot.put("#totale#",""+totale);
          }
        }

        doc.writeSostituisci("rigaTab",ht);
	doc.writeSostituisci("totaliTab",htot);
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
System.out.println("Dopo lettura dati");
                preparaBody(dbc,doc,helabora, (String)par.get("tp"));

		doc.write("finale");
		doc.close();
		dbc.close();
		super.close(dbc);
		done=true;
                return (byte[])doc.get();
 	}catch(Exception e){
 		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("FoTestBarthel Errore eseguendo una query_report()  ");
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
      String data = ""+par.get("data");

      String mysel = "SELECT * FROM sc_barthel WHERE"+
		     " n_cartella="+cartella+
                     " AND data = "+formatDate(dbc,data);
      ISASRecord dbr = dbc.readRecord(mysel);
      if(dbr!=null)
	hret = dbr.getHashtable();
      else hret = null;
  }catch(Exception e){
	  e.printStackTrace();
    System.out.println("FoTestBarthel Errore eseguendo una leggiDati: "+e);
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



}	// End of FoBarthel class


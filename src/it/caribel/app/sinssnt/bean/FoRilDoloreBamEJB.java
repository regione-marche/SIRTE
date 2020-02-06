package it.caribel.app.sinssnt.bean;
// --------------------------------------------------------------------------
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 07/11/2006 - EJB di stampa della procedura SINS Scala Wound
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

public class FoRilDoloreBamEJB extends SINSSNTConnectionEJB 
{
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	public FoRilDoloreBamEJB() {}


   
	public byte[] query_report(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;

		try{
			myLogin lg = new myLogin();
			lg.put(utente,passwd);
			dbc=super.logIn(lg);

			System.out.println("query_report/par ininput: " + par.toString()); //gb 22.12.08

			//preparo i titoli di stampa
			preparaLayout(doc, dbc, par);
	
			Hashtable helabora = new Hashtable();
//gb 22.12.08			if(par.get("tp").equals("1"))
			if(((String)par.get("tp")).equals("1"))
            		  helabora=LeggiDati(dbc,par);
			System.out.println("query_report/helabora: " + helabora.toString()); //gb 22.12.08
            		preparaBody(dbc, doc, helabora, (String)par.get("tp"));

			doc.write("finale");
			doc.close();
			dbc.close();
			super.close(dbc);
			done=true;
            return (byte[])doc.get();
 		}catch(Exception e){
 			e.printStackTrace();
			e.printStackTrace();
			throw new SQLException("FoScalaWound Errore eseguendo una query_report()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					e1.printStackTrace();
					System.out.println(e1);}
			}
		}

	}

	private Hashtable LeggiDati(ISASConnection dbc,Hashtable par)throws Exception
	{
	  	Hashtable hret = new Hashtable();
	  	try{
	    	String cartella = ""+par.get("cartella");
			String data = ""+par.get("data");
	
	      	String mysel = "SELECT * FROM sc_ril_dolore_bam WHERE"+
			     " n_cartella="+cartella+
	             " AND data = "+formatDate(dbc,data);
		System.out.println("LeggiDati/mysel: " + mysel); //gb 22.12.08
	      	ISASRecord dbr = dbc.readRecord(mysel);
	      	if(dbr!=null)
				hret = dbr.getHashtable();
	      	else 
				hret = null;
	  	}catch(Exception e){
	    	System.out.println("FoRilDoloreBam Errore eseguendo una leggiDati: "+e);
	  	}
	  	return hret;
	}

	private void preparaLayout(mergeDocument md, ISASConnection dbc,Hashtable par)
	{
		Hashtable htxt = new Hashtable();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE "+
				"conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String)dbtxt.get("conf_txt"));

			String cart = ""+par.get("cartella");
			it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
			htxt.put("#assistito#", ut.getDecode(dbc, "cartella", "n_cartella", cart,
							      "nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nomeass"));

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

	private void preparaBody(ISASConnection dbc, mergeDocument doc, Hashtable par, String tp) throws Exception
	{
		
		Hashtable h_xstampa = new Hashtable();
		/*h_xstampa.put("#domanda1_1#", "1");
		h_xstampa.put("#domanda1_2#", "2");
		h_xstampa.put("#domanda1_3#", "3");
		h_xstampa.put("#domanda1_4#", "4");
		h_xstampa.put("#domanda1_5#", "5");
		h_xstampa.put("#domanda1_6#", "6");
		h_xstampa.put("#domanda1_7#", "7");
		h_xstampa.put("#domanda1_8#", "8");
		h_xstampa.put("#domanda1_9#", "9");
		h_xstampa.put("#domanda1_10#", "10");*/
		h_xstampa.put("#domanda1_1#", "");
		h_xstampa.put("#domanda1_2#", "");
		h_xstampa.put("#domanda1_3#", "");
		h_xstampa.put("#domanda1_4#", "");
		h_xstampa.put("#domanda1_5#", "");
		h_xstampa.put("#domanda1_6#", "");
		h_xstampa.put("#domanda1_7#", "");
		h_xstampa.put("#domanda1_8#", "");
		h_xstampa.put("#domanda1_9#", "");
		h_xstampa.put("#domanda1_10#", "");
		h_xstampa.put("#domanda2#", "");		
		/*h_xstampa.put("#domanda3_1#", "Espressione neutra o sorriso");
		h_xstampa.put("#domanda3_2#", "Smorfie occasionali,espressione disinteressata");
		h_xstampa.put("#domanda3_3#", "Aggrottamento ciglia, da costante a frequente,tremore del mento");
		h_xstampa.put("#domanda4_1#", "Posizione normale o rilassata ");
		h_xstampa.put("#domanda4_2#", "Movimenti delle gambe a scatti o scalcianti,muscoli tesi");
		h_xstampa.put("#domanda4_3#", "Scalcia e ritrae le gambe in modo più frequente");
		h_xstampa.put("#domanda5_1#", "Posizione normale e tranquilla, si muove naturalmente");
		h_xstampa.put("#domanda5_2#", "Si agita, si dondola avanti e indietro, è teso");
		h_xstampa.put("#domanda5_3#", "Inarcato e rigido, si muove a scatti");
		h_xstampa.put("#domanda6_1#", "Assenza di pianto");
		h_xstampa.put("#domanda6_2#", "Geme e piagnucola, lamenti occasionali");
		h_xstampa.put("#domanda6_3#", "Piange in modo continuo, urla e singhiozza, si lamenta frequentemente");
		h_xstampa.put("#domanda7_1#", "Soddisfatto, rilassato");
		h_xstampa.put("#domanda7_2#", "E' rassicurato dall'abbraccio, dal tono della voce, è distraibile");
		h_xstampa.put("#domanda7_3#", "Difficoltà a consolarlo e confortarlo");*/
		h_xstampa.put("#domanda3_1#", "");
		h_xstampa.put("#domanda3_2#", "");
		h_xstampa.put("#domanda3_3#", "");
		h_xstampa.put("#domanda4_1#", "");
		h_xstampa.put("#domanda4_2#", "");
		h_xstampa.put("#domanda4_3#", "");
		h_xstampa.put("#domanda5_1#", "");
		h_xstampa.put("#domanda5_2#", "");
		h_xstampa.put("#domanda5_3#", "");
		h_xstampa.put("#domanda6_1#", "");
		h_xstampa.put("#domanda6_2#", "");
		h_xstampa.put("#domanda6_3#", "");
		h_xstampa.put("#domanda7_1#", "");
		h_xstampa.put("#domanda7_2#", "");
		h_xstampa.put("#domanda7_3#", "");
		
		String percorso=doc.getPath();
		if (percorso!=null)
			h_xstampa.put("#percorso#" ,percorso);
		if (tp.trim().equals("1")) {
			String domanda1 = (String)par.get("domanda_b1");
			if (domanda1!=null && !domanda1.equals("")){
				if (domanda1.equals("1 ")){
					h_xstampa.put("#domanda1_1#", "1");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
					h_xstampa.put("#domanda1_4#", "");
					h_xstampa.put("#domanda1_5#", "");
					h_xstampa.put("#domanda1_6#", "");
					h_xstampa.put("#domanda1_7#", "");
					h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				   
				else if (domanda1.equals("2 ")){
					h_xstampa.put("#domanda1_1#", "");
				    h_xstampa.put("#domanda1_2#", "2");
				    h_xstampa.put("#domanda1_3#", "");
					h_xstampa.put("#domanda1_4#", "");
					h_xstampa.put("#domanda1_5#", "");
					h_xstampa.put("#domanda1_6#", "");
					h_xstampa.put("#domanda1_7#", "");
					h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("3 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
				    h_xstampa.put("#domanda1_3#", "3");
				    h_xstampa.put("#domanda1_4#", "");
					h_xstampa.put("#domanda1_5#", "");
					h_xstampa.put("#domanda1_6#", "");
					h_xstampa.put("#domanda1_7#", "");
					h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("4 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "4");
				    h_xstampa.put("#domanda1_5#", "");
					h_xstampa.put("#domanda1_6#", "");
					h_xstampa.put("#domanda1_7#", "");
					h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("5 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "");
				    h_xstampa.put("#domanda1_5#", "5");
				    h_xstampa.put("#domanda1_6#", "");
					h_xstampa.put("#domanda1_7#", "");
					h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("6 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "");
				    h_xstampa.put("#domanda1_5#", "");
				    h_xstampa.put("#domanda1_6#", "6");
				    h_xstampa.put("#domanda1_7#", "");
					h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("7 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "");
				    h_xstampa.put("#domanda1_5#", "");
				    h_xstampa.put("#domanda1_6#", "");
				    h_xstampa.put("#domanda1_7#", "7");
				    h_xstampa.put("#domanda1_8#", "");
					h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("8 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "");
				    h_xstampa.put("#domanda1_5#", "");
				    h_xstampa.put("#domanda1_6#", "");
				    h_xstampa.put("#domanda1_7#", "");
				    h_xstampa.put("#domanda1_8#", "8");
				    h_xstampa.put("#domanda1_9#", "");
					h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("9 ")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "");
				    h_xstampa.put("#domanda1_5#", "");
				    h_xstampa.put("#domanda1_6#", "");
				    h_xstampa.put("#domanda1_7#", "");
				    h_xstampa.put("#domanda1_8#", "");
				    h_xstampa.put("#domanda1_9#", "9");
				    h_xstampa.put("#domanda1_10#", "");
				}
				else if (domanda1.equals("10")){
					h_xstampa.put("#domanda1_1#", "");
					h_xstampa.put("#domanda1_2#", "");
					h_xstampa.put("#domanda1_3#", "");
				    h_xstampa.put("#domanda1_4#", "");
				    h_xstampa.put("#domanda1_5#", "");
				    h_xstampa.put("#domanda1_6#", "");
				    h_xstampa.put("#domanda1_7#", "");
				    h_xstampa.put("#domanda1_8#", "");
				    h_xstampa.put("#domanda1_9#", "");
				    h_xstampa.put("#domanda1_10#", "10");
				}
			}
				
				
			String domanda2 = (String)par.get("domanda_b2");
			if (domanda2!=null && !domanda2.equals("")){
				if (domanda2.equals("1 "))
				    h_xstampa.put("#domanda2#", "0 - Nessun male");
				else if (domanda2.equals("2 "))
				    h_xstampa.put("#domanda2#", "2 - Un po' di male");
				else if (domanda2.equals("4 "))
				    h_xstampa.put("#domanda2#", "4 - Un po' piu' di male");
				else if (domanda2.equals("6 "))
				    h_xstampa.put("#domanda2#", "6 - Ancora piu' di male");
				else if (domanda2.equals("8 "))
				    h_xstampa.put("#domanda2#", "8 - Molto piu' di male");
				else if (domanda2.equals("10"))
				    h_xstampa.put("#domanda2#", "10 - Il peggior male possibile");
				
			}
		
			String domanda3 = (String)par.get("domanda_b3");
			if (domanda3!=null && !domanda3.equals("")){
				if (domanda3.equals("0")){
				    h_xstampa.put("#domanda3_1#", "Espressione neutra o sorriso");
				    h_xstampa.put("#domanda3_2#", "");
				    h_xstampa.put("#domanda3_3#", "");
				}
				else if (domanda3.equals("1")){
					h_xstampa.put("#domanda3_1#", "");
				    h_xstampa.put("#domanda3_2#", "Smorfie occasionali,espressione disinteressata");
				    h_xstampa.put("#domanda3_3#", "");
				}
				else if (domanda3.equals("2")){
					h_xstampa.put("#domanda3_1#", "");
				    h_xstampa.put("#domanda3_2#", "");
				    h_xstampa.put("#domanda3_3#", "Aggrottamento ciglia, da costante a frequente,tremore del mento");
				}
				
			}
			
			String domanda4 = (String)par.get("domanda_b4");
			if (domanda4!=null && !domanda4.equals("")){
				if (domanda4.equals("0")){
					h_xstampa.put("#domanda4_2#", "");
				    h_xstampa.put("#domanda4_3#", "");
				    h_xstampa.put("#domanda4_1#", "Posizione normale o rilassata");
				}
				else if (domanda4.equals("1")){
					h_xstampa.put("#domanda4_1#", "");
				    h_xstampa.put("#domanda4_3#", "");
				    h_xstampa.put("#domanda4_2#", "Movimenti delle gambe a scatti o scalcianti,muscoli tesi");
				}
				else if (domanda4.equals("2")){
					h_xstampa.put("#domanda4_1#", "");
				    h_xstampa.put("#domanda4_2#", "");
				    h_xstampa.put("#domanda4_3#", "Scalcia e ritrae le gambe in modo più frequente");
				}
				
			}
			
			String domanda5 = (String)par.get("domanda_b5");
			if (domanda5!=null && !domanda5.equals("")){
				if (domanda5.equals("0")){
					h_xstampa.put("#domanda5_2#", "");
				    h_xstampa.put("#domanda5_3#", "");
				    h_xstampa.put("#domanda5_1#", "Posizione normale e tranquilla, si muove naturalmente");
				}
				else if (domanda5.equals("1")){
					h_xstampa.put("#domanda5_1#", "");
				    h_xstampa.put("#domanda5_3#", "");
				    h_xstampa.put("#domanda5_2#", "Si agita, si dondola avanti e indietro, è teso");
				}
				else if (domanda5.equals("2")){
					h_xstampa.put("#domanda5_2#", "");
				    h_xstampa.put("#domanda5_1#", "");
				    h_xstampa.put("#domanda5_3#", "Inarcato e rigido, si muove a scatti");
				}
				
			}
			
			String domanda6 = (String)par.get("domanda_b6");
			if (domanda6!=null && !domanda6.equals("")){
				if (domanda6.equals("0")){
					h_xstampa.put("#domanda6_2#", "");
				    h_xstampa.put("#domanda6_3#", "");
				    h_xstampa.put("#domanda6_1#", "Assenza di pianto");
				}
				else if (domanda6.equals("1")){
					h_xstampa.put("#domanda6_1#", "");
				    h_xstampa.put("#domanda6_3#", "");
				    h_xstampa.put("#domanda6_2#", "Geme e piagnucola, lamenti occasionali");
				}
				else if (domanda6.equals("2")){
					h_xstampa.put("#domanda6_2#", "");
				    h_xstampa.put("#domanda6_1#", "");
				    h_xstampa.put("#domanda6_3#", "Piange in modo continuo, urla e singhiozza, si lamenta frequentemente");
				}
				
			}
			
			String domanda7 = (String)par.get("domanda_b7");
			if (domanda7!=null && !domanda7.equals("")){
				if (domanda7.equals("0")){
					h_xstampa.put("#domanda7_2#", "");
				    h_xstampa.put("#domanda7_3#", "");
				    h_xstampa.put("#domanda7_1#", "Soddisfatto, rilassato");
				}
				else if (domanda7.equals("1")){
					h_xstampa.put("#domanda7_1#", "");
				    h_xstampa.put("#domanda7_3#", "");
				    h_xstampa.put("#domanda7_2#", "E' rassicurato dall'abbraccio, dal tono della voce, è distraibile");
				}
				else if (domanda7.equals("2")){
					h_xstampa.put("#domanda7_2#", "");
				    h_xstampa.put("#domanda7_1#", "");
				    h_xstampa.put("#domanda7_3#", "Difficoltà a consolarlo e confortarlo");
				}
				
			}
			
			//String punteggio = (String)par.get("dol_punt");
			//h_xstampa.put("#punteggio#", punteggio);
			
		}
			
		doc.writeSostituisci("inizioTab",h_xstampa);
		
		doc.write("fineTab");

		// totale
		Hashtable hTotale = new Hashtable();
		if (tp.trim().equals("1")) {
			if (par != null) {	        
				if (par.get("data_test") != null) {
			          String data=""+(java.sql.Date)par.get("data_test");
	    		    	  data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
	        		  hTotale.put("#data_test#", data);
				} else
				  hTotale.put("#data_test#", "____/____/_______");

				if ((par.get("nome") != null) && (!((String)par.get("nome")).trim().equals(""))){
					hTotale.put("#nome_test#", ((String)par.get("nome")).trim());
			    	} else
					hTotale.put("#nome_test#", "_________________");
			} else {
				hTotale.put("#data_test#", "____/____/_______");
				hTotale.put("#nome_test#", "_________________");
			}
		} else {
			hTotale.put("#data_test#", "____/____/_______");
			hTotale.put("#nome_test#", "_________________");
		}

	    doc.writeSostituisci("totale", hTotale);
	}

	

}	// End of FoRilDoloreBam class


package it.caribel.app.sinssnt.bean;
// --------------------------------------------------------------------------
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 07/11/2006 - EJB di stampa della procedura SINS Scala Braden
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

public class FoScalaBradenEJB extends SINSSNTConnectionEJB 
{
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	public FoScalaBradenEJB() {}


    private String[] arrVociBraden = {"Percezione sensoriale", "Macerazione", 
		"Attivita'",	"Mobilita'",
		"Nutrizione", "Frizione e scivolamento"};

    private String[] arrDbNameVociBraden = {"skb_percezione_sens", "skb_umidita",
		"skb_attivita", "skb_mobilita",
		"skb_nutrizione", "skb_fraz_sciv"};

    private String[][] arrArrVociRadioB = {{"Completamente limitata", "Molto limitata", "Leggermente limitata", "Nessuna limitazione"}, 
		{"Costantemente umida", "Molto umida", "Occasionalemnte umida", "Raramente umida"}, 
		{"Allettato", "In poltrona", "Cammina occasionalmente", "Cammina di frequente"}, 
		{"Completamente immobile", "Molto limitata", "Parzialmente limitata", "Limitazioni assenti"}, 
		{"Molto povera", "Probabilmente inadeguata", "Adeguata", "Eccelente"},
		{"Problema", "Problema potenziale", "Senza problemi apparenti"}};

    private String[][] arrArrValRadioB = {{"1", "2", "3", "4"}, {"1", "2", "3", "4"}, 
		{"1", "2", "3", "4"}, {"1", "2", "3", "4"},
		{"1", "2", "3", "4"}, {"1", "2", "3"}};



	// x gruppi di rischio
	private String[] arrVociRadioBGr = {"Rischio molto elevato", "Rischio elevato", "Rischio medio", "Rischio ridotto o assente"};

	private String[] arrValRadioBGr = {"1", "2", "3", "4"};

	private String[] arrLabRadioGr = {"minore o = 13", "14 - 16", "17 - 20", "maggiore di 20"};


	public byte[] query_report(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException 
	{
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

            preparaBody(dbc, doc, helabora, (String)par.get("tp"));

			doc.write("finale");
			doc.close();
			dbc.close();
			super.close(dbc);
			done=true;
            return (byte[])doc.get();
 		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("FoScalaBraden Errore eseguendo una query_report()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}

	}

	private Hashtable LeggiDati(ISASConnection dbc,Hashtable par)throws Exception
	{
	  	Hashtable hret = new Hashtable();
	  	try{
	    	String cartella = ""+par.get("cartella");
			String data = ""+par.get("data");
	
	      	String mysel = "SELECT * FROM sc_braden WHERE"+
			     " n_cartella="+cartella+
	             " AND data = "+formatDate(dbc,data);
	      	ISASRecord dbr = dbc.readRecord(mysel);
	      	if(dbr!=null)
				hret = dbr.getHashtable();
	      	else 
				hret = null;
	  	}catch(Exception e){
	    	System.out.println("FoScalaBraden Errore eseguendo una leggiDati: "+e);
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
		doc.write("inizioTab");
		stampaVoce(doc, par, tp);
		doc.write("fineTab");

		// totale
		Hashtable hTotale = new Hashtable();
		if (tp.trim().equals("1")) {
			if (par != null) {	        
				if (par.get("data") != null) {
			        String data=""+(java.sql.Date)par.get("data");
	    		    data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
	        		hTotale.put("#data_test#", data);
			    } else
					hTotale.put("#data_test#", "____/____/_______");

				if ((par.get("nome") != null) && (!((String)par.get("nome")).trim().equals(""))){
					hTotale.put("#nome_test#", ((String)par.get("nome")).trim());
			    } else
					hTotale.put("#nome_test#", "_________________");

				if (par.get("skb_valutazione") != null){
					String score = ((Integer)par.get("skb_valutazione")).toString();					
					hTotale.put("#tot#", score);
				} else
					hTotale.put("#tot#", "");
			} else {
				hTotale.put("#data_test#", "____/____/_______");
				hTotale.put("#nome_test#", "_________________");
				hTotale.put("#tot#", "");
			}
		} else {
			hTotale.put("#data_test#", "____/____/_______");
			hTotale.put("#nome_test#", "_________________");
			hTotale.put("#tot#", "");
		}
	    doc.writeSostituisci("totale", hTotale);

		// gruppo di rischio
		doc.write("inizioTabGruppo");
		stampaGruppo(doc, par, tp);
		doc.write("fineTabGruppo");

	}

	private void stampaVoce(mergeDocument doc, Hashtable par, String tp) throws Exception
	{
		String valVoce = "";

		for (int k=0; k<arrVociBraden.length; k++) {
			if (tp.trim().equals("1")) 
				valVoce = leggiValVoce(par, arrDbNameVociBraden[k]);

			stampaValVoce(doc, par, tp, 
							arrVociBraden[k], valVoce, 
							arrArrVociRadioB[k], arrArrValRadioB[k], null);

			doc.write("rigaSpazio");
		}
	}

	private void stampaValVoce(mergeDocument doc, Hashtable par, String tp,
							String voce, String valVoce, 
							String[] arrVociRadioB, String[] arrValRadioB, String[] arrLabRadio) throws Exception
	{
		boolean isGruppo = (voce == null);

		for (int j=0; j<arrVociRadioB.length;j++) {
			Hashtable h_xstampa = new Hashtable();

			if (!isGruppo)
				h_xstampa.put("#voce#", (j==0?voce:""));
			else // stampa gruppo
				h_xstampa.put("#voce#", arrValRadioB[j]);

			if (tp.trim().equals("1")) {
				if ((valVoce != null) && (valVoce.trim().equals(arrValRadioB[j])))
					h_xstampa.put("#check#", "x");	
				else
					h_xstampa.put("#check#", "");	
			} else
				h_xstampa.put("#check#", "");	

			h_xstampa.put("#valore#", arrVociRadioB[j]);

			if (arrLabRadio != null)
				h_xstampa.put("#score#", "(" + arrLabRadio[j] + ")");
			else
				h_xstampa.put("#score#", "");

			doc.writeSostituisci("rigaTab" + (isGruppo?"Gruppo":""), h_xstampa);
		}
	}

	private String leggiValVoce(Hashtable par, String chiave) throws Exception
	{
		String ret = "";
		if (par != null) {
			if (par.get(chiave) != null)
				ret = "" + par.get(chiave);
		}
		return ret;
	}

	private void stampaGruppo(mergeDocument doc, Hashtable par, String tp) throws Exception
	{
		String valVoceGr = "";

		if (tp.trim().equals("1")) 
			valVoceGr = leggiValVoce(par, "skb_valutazione");

		if ((valVoceGr != null) && (!valVoceGr.trim().equals(""))) {
			int val = Integer.parseInt(valVoceGr);
			if (val<=13)
				valVoceGr = "1";
			else if (val>13 && val<=16)
				valVoceGr = "2";
			else if (val>16 && val<=20)
				valVoceGr = "3";
			else if (val>20)
				valVoceGr = "4";
		}

		stampaValVoce(doc, par, tp, 
							null, valVoceGr, 
							arrVociRadioBGr, arrValRadioBGr, arrLabRadioGr);
	
	}

}	// End of FoScalaBraden class


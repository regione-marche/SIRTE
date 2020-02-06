package it.caribel.app.sinssnt.bean;
// --------------------------------------------------------------------------
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 30/05/2006 - EJB di stampa della procedura SINS Test BADL
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

public class FoScalaRugEJB extends SINSSNTConnectionEJB {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

public FoScalaRugEJB() {}

private void preparaBody(ISASConnection dbc,mergeDocument doc, Hashtable par, String tp, String data)
throws Exception{
	Hashtable ht=new Hashtable();
    Hashtable htot=new Hashtable();

    
	FaiHashVuota(ht);
	ht.put("#cartella#",(Integer)par.get("n_cartella"));
	ht.put("#cognome#",(String)par.get("cognome"));
	ht.put("#nome#",(String)par.get("nome"));
	if (par.get("sesso").equals("M"))		
		ht.put("#sesso#","Maschio");
	else if (par.get("sesso").equals("F"))		
		ht.put("#sesso#","Femmina");
	else		
		ht.put("#sesso#","");
	String datan=((java.sql.Date)par.get("data_nasc")).toString();
	datan=datan.substring(8,10)+"/"+datan.substring(5,7)+"/"+
            datan.substring(0,4);
	//String data="";
	//if (par.get("data")!=null && !par.get("data").equals("")){
		//data=((java.sql.Date)par.get("data")).toString();
		//data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+
			//	data.substring(0,4);
	//}
	
	ht.put("#data_nascita#", datan);
	if (par.get("indirizzo")!=null && !par.get("indirizzo").equals(""))
		ht.put("#indirizzo#",(String)par.get("indirizzo") + " - " + (String)par.get("comune_res"));
	else
		ht.put("#indirizzo#", "");
	if (par.get("stato_civile")!=null && !par.get("stato_civile").equals(""))
		ht.put("#stato_civile#",(String)par.get("desciv"));
	else
		ht.put("#stato_civile#", "");
	ht.put("#data_val#", data);
	
	ht.put("#a8#","");
	ht.put("#a13a#","");
	ht.put("#a13b#","");
	ht.put("#a13c#","");	
	ht.put("#a14#","");
	ht.put("#a12#","");
	ht.put("#c1#","");
	ht.put("#e3a#","");
	ht.put("#e3b#","");
	ht.put("#e3c#","");
	ht.put("#e3d#","");
	ht.put("#e3e#","");
	ht.put("#e3f#","");
	ht.put("#g1a#","");
	ht.put("#g1d#","");
	ht.put("#g1e#","");
	ht.put("#g2g#","");
	ht.put("#g2h#","");
	ht.put("#g2i#","");
	ht.put("#g2j#","");
	ht.put("#i1e#","");
	ht.put("#i1f#","");
	ht.put("#i1i#","");
	ht.put("#i1r#","");
	ht.put("#i1u#","");
	ht.put("#i2a#","");
	ht.put("#i2b#","");
	
	ht.put("#j3h#","");
	ht.put("#j3i#","");
	ht.put("#j3j#","");
	ht.put("#j3n#","");
	ht.put("#j3r#","");
	ht.put("#j3s#","");
	ht.put("#j7#","");
	ht.put("#k2a#","");
	ht.put("#k2b#","");
	ht.put("#k3#","");
	ht.put("#l1#","");
	ht.put("#l4#","");
	ht.put("#l5#","");
	ht.put("#l7#","");
	
	ht.put("#n2a#","");
	ht.put("#n2b#","");
	ht.put("#n2d#","");
	ht.put("#n2e#","");
	ht.put("#n2f#","");
	ht.put("#n2g#","");
	ht.put("#n2h#","");
	ht.put("#n2i#","");
	ht.put("#n2j#","");
	ht.put("#n2k#","");
	
	if(tp.equals("1")){
    	if(par.get("a8")!=null && !par.get("a8").equals(""))    		
    		ht.put("#a8#",(String)par.get("a8"));
    	else 
    		ht.put("#a8#","");
    	
    	if(par.get("a13a")!=null && !par.get("a13a").equals(""))        
    		ht.put("#a13a#",(String)par.get("a13a"));
    	else 
    		ht.put("#a13a#","");
    	
    	if(par.get("a13b")!=null && !par.get("a13b").equals(""))        
    		ht.put("#a13b#",(String)par.get("a13b"));
    	else 
    		ht.put("#a13b#","");
    	
    	if(par.get("a13c")!=null && !par.get("a13c").equals(""))        
    		ht.put("#a13c#",(String)par.get("a13c"));
    	else 
    		ht.put("#a13c#","");
    	
    	if(par.get("a12")!=null && !par.get("a12").equals(""))        
    		ht.put("#a12#",(String)par.get("a12"));
    	else 
    		ht.put("#a12#","");
    	
    	if(par.get("a14")!=null && !par.get("a14").equals(""))        
    		ht.put("#a14#",(String)par.get("a14"));
    	else 
    		ht.put("#a14#","");
    	
    	if(par.get("c1")!=null && !par.get("c1").equals(""))        
    		ht.put("#c1#",(String)par.get("c1"));
    	else 
    		ht.put("#c1#","");
    	
    	if(par.get("e3a")!=null && !par.get("e3a").equals(""))        
    		ht.put("#e3a#",(String)par.get("e3a"));
    	else 
    		ht.put("#e3a#","");
    	
    	if(par.get("e3b")!=null && !par.get("e3b").equals(""))        
    		ht.put("#e3b#",(String)par.get("e3b"));
    	else 
    		ht.put("#e3b#","");
    	
    	if(par.get("e3c")!=null && !par.get("e3c").equals(""))        
    		ht.put("#e3c#",(String)par.get("e3c"));
    	else 
    		ht.put("#e3c#","");
    	
    	if(par.get("e3d")!=null && !par.get("e3d").equals(""))        
    		ht.put("#e3d#",(String)par.get("e3d"));
    	else 
    		ht.put("#e3d#","");
    	
    	if(par.get("e3e")!=null && !par.get("e3e").equals(""))        
    		ht.put("#e3e#",(String)par.get("e3e"));
    	else 
    		ht.put("#e3e#","");
    	
    	if(par.get("e3f")!=null && !par.get("e3f").equals(""))        
    		ht.put("#e3f#",(String)par.get("e3f"));
    	else 
    		ht.put("#e3f#","");
    	
    	if(par.get("g1a")!=null && !par.get("g1a").equals(""))        
    		ht.put("#g1a#",(String)par.get("g1a"));
    	else 
    		ht.put("#g1a#","");
    	
    	if(par.get("g1d")!=null && !par.get("g1d").equals(""))        
    		ht.put("#g1d#",(String)par.get("g1d"));
    	else 
    		ht.put("#g1d#","");
    	
    	if(par.get("g1e")!=null && !par.get("g1e").equals(""))        
    		ht.put("#g1e#",(String)par.get("g1e"));
    	else 
    		ht.put("#g1e#","");
    	
    	if(par.get("g2g")!=null && !par.get("g2g").equals(""))        
    		ht.put("#g2g#",(String)par.get("g2g"));
    	else 
    		ht.put("#g2g#","");
    	
    	if(par.get("g2h")!=null && !par.get("g2h").equals(""))        
    		ht.put("#g2h#",(String)par.get("g2h"));
    	else 
    		ht.put("#g2h#","");
    	
    	if(par.get("g2i")!=null && !par.get("g2i").equals(""))        
    		ht.put("#g2i#",(String)par.get("g2i"));
    	else 
    		ht.put("#g2i#","");
    	
    	if(par.get("g2j")!=null && !par.get("g2j").equals(""))        
    		ht.put("#g2j#",(String)par.get("g2j"));
    	else 
    		ht.put("#g2j#","");
    	
    	if(par.get("i1e")!=null && !par.get("i1e").equals(""))        
    		ht.put("#i1e#",(String)par.get("i1e"));
    	else 
    		ht.put("#i1e#","");
    	
    	if(par.get("i1f")!=null && !par.get("i1f").equals(""))        
    		ht.put("#i1f#",(String)par.get("i1f"));
    	else 
    		ht.put("#i1f#","");
    	
    	if(par.get("i1i")!=null && !par.get("i1i").equals(""))        
    		ht.put("#i1i#",(String)par.get("i1i"));
    	else 
    		ht.put("#i1i#","");
    	
    	if(par.get("i1r")!=null && !par.get("i1r").equals(""))        
    		ht.put("#i1r#",(String)par.get("i1r"));
    	else 
    		ht.put("#i1r#","");
    	
    	if(par.get("i1u")!=null && !par.get("i1u").equals(""))        
    		ht.put("#i1u#",(String)par.get("i1u"));
    	else 
    		ht.put("#i1u#","");
    	
    	if(par.get("i2a").equals("S")) 
    		ht.put("#i2a#","SI");
    	else if(par.get("i2a").equals("N")) 
    		ht.put("#i2a#","NO");
    	else 
    		ht.put("#i2a#","");
    	
    	if(par.get("i2b").equals("S")) 
    		ht.put("#i2b#","SI");
    	else if(par.get("i2b").equals("N")) 
    		ht.put("#i2b#","NO");
    	else 
    		ht.put("#i2b#","");
    	
    	if(par.get("j3h")!=null && !par.get("j3h").equals(""))        
    		ht.put("#j3h#",(String)par.get("j3h"));
    	else 
    		ht.put("#j3h#","");
    	
    	if(par.get("j3i")!=null && !par.get("j3i").equals(""))        
    		ht.put("#j3i#",(String)par.get("j3i"));
    	else 
    		ht.put("#j3i#","");
    	
    	if(par.get("j3j")!=null && !par.get("j3j").equals(""))        
    		ht.put("#j3j#",(String)par.get("j3j"));
    	else 
    		ht.put("#j3j#","");
    	
    	if(par.get("j3n")!=null && !par.get("j3n").equals(""))        
    		ht.put("#j3n#",(String)par.get("j3n"));
    	else 
    		ht.put("#j3n#","");
    	
    	if(par.get("j3r")!=null && !par.get("j3r").equals(""))        
    		ht.put("#j3r#",(String)par.get("j3r"));
    	else 
    		ht.put("#j3r#","");
    	
    	if(par.get("j3s")!=null && !par.get("j3s").equals(""))        
    		ht.put("#j3s#",(String)par.get("j3s"));
    	else 
    		ht.put("#j3s#","");
    	
    	if(par.get("j7")!=null && !par.get("j7").equals(""))        
    		ht.put("#j7#",(String)par.get("j7"));
    	else 
    		ht.put("#j7#","");
    	
    	if(par.get("k2a")!=null && !par.get("k2a").equals(""))        
    		ht.put("#k2a#",(String)par.get("k2a"));
    	else 
    		ht.put("#k2a#","");
    	
    	if(par.get("k2b")!=null && !par.get("k2b").equals(""))        
    		ht.put("#k2b#",(String)par.get("k2b"));
    	else 
    		ht.put("#k2b#","");
    	
    	if(par.get("k3")!=null && !par.get("k3").equals(""))        
    		ht.put("#k3#",(String)par.get("k3"));
    	else 
    		ht.put("#k3#","");
    	
    	if(par.get("l1")!=null && !par.get("l1").equals(""))        
    		ht.put("#l1#",(String)par.get("l1"));
    	else 
    		ht.put("#l1#","");
    	
    	if(par.get("l4")!=null && !par.get("l4").equals(""))        
    		ht.put("#l4#",(String)par.get("l4"));
    	else 
    		ht.put("#l4#","");
    	
    	if(par.get("l5")!=null && !par.get("l5").equals(""))        
    		ht.put("#l5#",(String)par.get("l5"));
    	else 
    		ht.put("#l5#","");
    	
    	if(par.get("l7")!=null && !par.get("l7").equals(""))        
    		ht.put("#l7#",(String)par.get("l7"));
    	else 
    		ht.put("#l7#","");
    	
    	if(par.get("n2a")!=null && !par.get("n2a").equals(""))        
    		ht.put("#n2a#",(String)par.get("n2a"));
    	else 
    		ht.put("#n2a#","");
    	
    	if(par.get("n2b")!=null && !par.get("n2b").equals(""))        
    		ht.put("#n2b#",(String)par.get("n2b"));
    	else 
    		ht.put("#n2b#","");
    	
    	if(par.get("n2d")!=null && !par.get("n2d").equals(""))        
    		ht.put("#n2d#",(String)par.get("n2d"));
    	else 
    		ht.put("#n2d#","");
    	
    	if(par.get("n2e")!=null && !par.get("n2e").equals(""))        
    		ht.put("#n2e#",(String)par.get("n2e"));
    	else 
    		ht.put("#n2e#","");
    	
    	if(par.get("n2f")!=null && !par.get("n2f").equals(""))        
    		ht.put("#n2f#",(String)par.get("n2f"));
    	else 
    		ht.put("#n2f#","");
    	
    	if(par.get("n2g")!=null && !par.get("n2g").equals(""))        
    		ht.put("#n2g#",(String)par.get("n2g"));
    	else 
    		ht.put("#n2g#","");
    	
    	if(par.get("n2h")!=null && !par.get("n2h").equals(""))        
    		ht.put("#n2h#",(String)par.get("n2h"));
    	else 
    		ht.put("#n2h#","");
    	
    	if(par.get("n2i")!=null && !par.get("n2i").equals(""))        
    		ht.put("#n2i#",(String)par.get("n2i"));
    	else 
    		ht.put("#n2i#","");
    	
    	if(par.get("n2k")!=null && !par.get("n2k").equals(""))        
    		ht.put("#n2k#",(String)par.get("n2k"));
    	else 
    		ht.put("#n2k#","");
    	
    	if(par.get("n2j")!=null && !par.get("n2j").equals(""))        
    		ht.put("#n2j#",(String)par.get("n2j"));
    	else 
    		ht.put("#n2j#","");
        	
    }
	if(par.get("data_test")!=null)
		ht.put("#data_test#",
			(""+par.get("data_test")).substring(8,10)+"/"+
			(""+par.get("data_test")).substring(5,7)+"/"+
			(""+par.get("data_test")).substring(0,4));

	if(par.get("nome")!=null)
		ht.put("#nome_test#",""+par.get("nome"));

        doc.writeSostituisci("tabRug",ht);
}

private void FaiHashVuota(Hashtable ht){
	for (int i=1;i<8;i++){
	  ht.put("#punt"+i+"#","");
	}
	ht.put("#domanda1_1#","");
	ht.put("#domanda1_2#","");
	ht.put("#domanda1_3#","");
	ht.put("#domanda2_1#","");
	ht.put("#domanda2_2#","");
	ht.put("#domanda2_3#","");
	ht.put("#domanda3_1#","");
	ht.put("#domanda3_2#","");
	ht.put("#domanda3_3#","");
	ht.put("#domanda4_1#","");
	ht.put("#domanda4_2#","");
	ht.put("#domanda4_3#","");
	ht.put("#domanda5_1#","");
	ht.put("#domanda5_2#","");
	ht.put("#domanda5_3#","");
	ht.put("#domanda6_1#","");
	ht.put("#domanda6_2#","");
	ht.put("#domanda6_3#","");
	ht.put("#punteggio_tot#","");
	ht.put("#nome_test#","");
	ht.put("#data_test#","____/____/________");
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
		//if(par.get("tp").equals("1"))
                    helabora=LeggiDati(dbc,par);
		
                preparaBody(dbc,doc,helabora, (String)par.get("tp"),(String)par.get("data"));

		doc.write("finale");
		doc.close();
		dbc.close();
		super.close(dbc);
		done=true;
                return (byte[])doc.get();
 	}catch(Exception e){
 		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("FoScalaRug Errore eseguendo una query_report()  ");
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

private Hashtable LeggiDati(ISASConnection dbc,Hashtable par)throws Exception{
  Hashtable hret = new Hashtable();
  try{

      String cartella = ""+par.get("cartella");
    
      String data = ""+par.get("data");
      if (par.get("tp").equals("2")) {
    	  leggiAss1(dbc, par, cartella, data);
    	  return par;
      }
      String mysel = "SELECT r.* FROM rugiii_hc r" +
      				" WHERE r.n_cartella="+cartella+
      				" AND r.data = "+formatDate(dbc,data);
		     

      ISASRecord dbr = dbc.readRecord(mysel);
      if(dbr!=null){
    	leggiAss(dbc, dbr, cartella, data);
    	hret = dbr.getHashtable();
      }
      else hret = null;
  }catch(Exception e){
    System.out.println("FoScalaRug Errore eseguendo una leggiDati: "+e);
  }
  return hret;
}


private void leggiAss(ISASConnection mydbc, ISASRecord mydbr, 
		String numCartella, String data) throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
		{		
		ISASRecord dbrDec = null;
		try{

			String selA = "SELECT c.n_cartella," +
					" c.nome," +
					" c.cognome," +
					" c.data_nasc," +					
					" c.sesso," +					
					" ac.indirizzo, " +
					" ac.citta, " +		
					" ac.stato_civile" +
					" FROM cartella c," +
					" anagra_c ac" +
					" WHERE c.n_cartella = " + numCartella +
					" AND ac.n_cartella = " + numCartella +
					" AND ac.data_variazione IN (SELECT MAX(data_variazione)" +
					" FROM anagra_c WHERE n_cartella = " + numCartella + 
					" AND data_variazione <=" +  formatDate(mydbc, data) +
					")";

			System.out.println(" =====> leggiAss(): selA=[" + selA + "]");	

			dbrDec = mydbc.readRecord(selA);
			if (dbrDec != null){
				mydbr.put("n_cartella", dbrDec.get("n_cartella"));
				mydbr.put("nome", (String)dbrDec.get("nome"));
				mydbr.put("cognome", (String)dbrDec.get("cognome"));
				mydbr.put("sesso", (String)dbrDec.get("sesso"));
				mydbr.put("stato_civile", (String)dbrDec.get("stato_civile"));
				mydbr.put("data_nasc", (java.sql.Date)dbrDec.get("data_nasc"));
				mydbr.put("indirizzo", (String)dbrDec.get("indirizzo"));
				mydbr.put("citta", (String)dbrDec.get("citta"));

				String codRes = (String)mydbr.get("citta");        	

				if (codRes!=null && !(codRes.equals("")))
					leggiComuneRes(mydbc, mydbr, codRes);
				
				String stat_civile = (String)mydbr.get("stato_civile");        	

				if (stat_civile!=null && !(stat_civile.equals("")))
					leggiStatoCivile(mydbc, mydbr, stat_civile);
			}
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		} catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw newEjbException("Errore in RIspAss leggiAss: " + e.getMessage(), e);
		} 
		}// END leggiAss

private void leggiAss1(ISASConnection mydbc,Hashtable par, 
		String numCartella, String data) throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
		{		
		ISASRecord dbrDec = null;
		try{

			String selA = "SELECT c.n_cartella," +
					" c.nome," +
					" c.cognome," +
					" c.data_nasc," +					
					" c.sesso," +					
					" ac.indirizzo, " +
					" ac.citta, " +		
					" ac.stato_civile" +
					" FROM cartella c," +
					" anagra_c ac" +
					" WHERE c.n_cartella = " + numCartella +
					" AND ac.n_cartella = " + numCartella +
					" AND ac.data_variazione IN (SELECT MAX(data_variazione)" +
					" FROM anagra_c WHERE n_cartella = " + numCartella + 
					" AND data_variazione <=" +  formatDate(mydbc, data) +
					")";

			System.out.println(" =====> leggiAss(): selA=[" + selA + "]");	

			dbrDec = mydbc.readRecord(selA);
			if (dbrDec != null){
				par.put("n_cartella", dbrDec.get("n_cartella"));
				par.put("nome", (String)dbrDec.get("nome"));
				par.put("cognome", (String)dbrDec.get("cognome"));
				par.put("sesso", (String)dbrDec.get("sesso"));
				par.put("stato_civile", (String)dbrDec.get("stato_civile"));
				par.put("data_nasc", (java.sql.Date)dbrDec.get("data_nasc"));
				par.put("indirizzo", (String)dbrDec.get("indirizzo"));
				par.put("citta", (String)dbrDec.get("citta"));

				String codRes = (String)par.get("citta");        	

				if (codRes!=null && !(codRes.equals("")))
					leggiComuneRes1(mydbc, par, codRes);
				
				String stat_civile = (String)par.get("stato_civile");        	

				if (stat_civile!=null && !(stat_civile.equals("")))
					leggiStatoCivile1(mydbc, par, stat_civile);
			}
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		} catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw newEjbException("Errore in RIspAss leggiAss: " + e.getMessage(), e);
		} 
		}// END leggiAss

private Hashtable LeggiSesso(ISASConnection dbc,Hashtable par)throws 
Exception{
  Hashtable hret = new Hashtable();
  try{

      String cartella = ""+par.get("cartella");

      String mysel = "SELECT sesso FROM cartella WHERE"+
		     " n_cartella="+cartella;

      ISASRecord dbr = dbc.readRecord(mysel);
      if(dbr!=null)
	hret = dbr.getHashtable();
      else hret = null;
  }catch(Exception e){
    System.out.println("FoScalaRug Errore eseguendo una leggiDati: "+e);
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

private void leggiComuneRes(ISASConnection mydbc, ISASRecord mydbr, String codRes) throws Exception
{		
	ISASRecord dbrDec = null;

	String selRes = "SELECT *" +
				  " FROM  comuni" +
				  " WHERE codice = " + codRes;

	System.out.println(" Query SelRes" + selRes);	

	dbrDec = mydbc.readRecord(selRes);
	if (dbrDec != null){	
		if(dbrDec.get("codice")!=null && !(dbrDec.get("codice").equals(""))){
			mydbr.put("codice", (String)dbrDec.get("codice"));
			mydbr.put("comune_res", (String)dbrDec.get("descrizione"));	
			
		}			
	}
}// END leggiComuneRes

private void leggiStatoCivile(ISASConnection mydbc, ISASRecord mydbr, String stato_civ) throws Exception
{		
	ISASRecord dbrDec = null;

	String selRes = "SELECT desciv, cd_civ" +
					" FROM staciv" +
					" WHERE cd_civ = '" + stato_civ + "'";

	System.out.println(" Query SelRes" + selRes);	

	dbrDec = mydbc.readRecord(selRes);
	if (dbrDec != null){	
		if(dbrDec.get("cd_civ")!=null && !(dbrDec.get("cd_civ").equals(""))){
			mydbr.put("cd_civ", (String)dbrDec.get("cd_civ"));
			mydbr.put("desciv", (String)dbrDec.get("desciv"));	
			
		}			
	}
}// END leggiStatoCivile

private void leggiComuneRes1(ISASConnection mydbc, Hashtable par, String codRes) throws Exception
{		
	ISASRecord dbrDec = null;

	String selRes = "SELECT *" +
				  " FROM  comuni" +
				  " WHERE codice = " + codRes;

	System.out.println(" Query SelRes" + selRes);	

	dbrDec = mydbc.readRecord(selRes);
	if (dbrDec != null){	
		if(dbrDec.get("codice")!=null && !(dbrDec.get("codice").equals(""))){
			par.put("codice", (String)dbrDec.get("codice"));
			par.put("comune_res", (String)dbrDec.get("descrizione"));	
			
		}			
	}
}// END leggiComuneRes

private void leggiStatoCivile1(ISASConnection mydbc, Hashtable par, String stato_civ) throws Exception
{		
	ISASRecord dbrDec = null;

	String selRes = "SELECT desciv, cd_civ" +
					" FROM staciv" +
					" WHERE cd_civ = '" + stato_civ + "'";

	System.out.println(" Query SelRes" + selRes);	

	dbrDec = mydbc.readRecord(selRes);
	if (dbrDec != null){	
		if(dbrDec.get("cd_civ")!=null && !(dbrDec.get("cd_civ").equals(""))){
			par.put("cd_civ", (String)dbrDec.get("cd_civ"));
			par.put("desciv", (String)dbrDec.get("desciv"));	
			
		}			
	}
}// END leggiStatoCivile
/**
 * restituisce un campo data come stringa
 */
private String getDateField(ISASRecord dbr, String f) {
	try {
		if (dbr.get(f) == null)
			return "";
		String d = ((java.sql.Date) dbr.get(f)).toString();
		d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/"
				+ d.substring(0, 4);
		return d;
	} catch (Exception e) {
		debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
		return "";
	}
}

}	// End of FoScalaRug class


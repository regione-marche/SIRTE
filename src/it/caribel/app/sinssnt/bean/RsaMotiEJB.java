package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------

//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
// ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
// 28/01/2003 - EJB di connessione alla procedura SINS Tabella MotivoDimissioni
//
// Jessica Caccavale
//
// ==========================================================================


import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
//import it.pisa.caribel.sins_rs.connection.*;
//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
//ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
public class RsaMotiEJB extends SINSSNTConnectionEJB  
//public class RsaMotiEJB extends SINSSNTConnectionEJB  
{
	public RsaMotiEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from rsa_motdim where "+
			"md_codice='"+(String)h.get("md_codice")+"'";
			ISASRecord dbr=dbc.readRecord(myselect);
			System.out.println("QueryKey rsamoti "+myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public String duplicateChar(String s, String c) {
		if ((s == null) || (c == null)) return s;
		String mys = new String(s);
		int p = 0;
		while (true) {
			int q = mys.indexOf(c, p);
			if (q < 0) return mys;
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	
	// Elisa 10/07/09
	public Hashtable query_combo(myLogin mylogin,Hashtable h,Vector vkey) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException
	{
		System.out.println("RSA MOTDIM: query_combo -- H == " + h.toString());
		System.out.println("CHIAVE VETTORE: " + vkey.elementAt(0).toString());
		
		Hashtable res = new Hashtable();
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try
		{
			dbc = super.logIn(mylogin);
			String myselect = "SELECT md_codice codice,  md_descri descrizione FROM rsa_motdim ORDER BY md_descri";
			
			System.out.println("RSA MOTDIM: query_combo -- select: " + myselect);

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();

			// la chiave e': "MOTDIM" se non mi interessa un valore vuoto, altrimenti e' "MOTDIM1"
			if(vdbr != null && vdbr.size() > 0)
			{
				if(vkey.elementAt(0).toString().equals("MOTDIM1"))
				{
					ISASRecord r = dbc.newRecord("rsa_motdim");
					r.put("codice", "!!");
					r.put("descrizione", "");
					
					vdbr.insertElementAt(r, 0);
				}
				
				res.put(vkey.elementAt(0).toString(),vdbr);
			}
			else res = null;

			if (dbcur != null)	dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return res;
		}
		catch(Exception e)
		{
			System.out.println("Errore eseguendo RSA MOTDIM: query_combo " + e);
			throw new SQLException("Errore eseguendo RSA MOTDIM: query_combo ");
		}
		finally
		{
			if(!done)
			{
				try
				{		
					if (dbcur != null)	dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2)
				{   System.out.println("Errore eseguendo MOTDIM -- MOTDIM " + e2);   }
			}
		}
	}


	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		String scr=" ";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			boolean dato_presente = false;
			String myselect="";

			//Controllo esistenza descrizione sportello
			try{
				scr=(String)(h.get("md_descri"));
				if(!scr.equals("") && scr!=null) {
					scr=duplicateChar(scr,"'");
					if (myselect.equals(""))
						myselect=myselect+" md_descri like '"+scr+"%'";
					else
						myselect=myselect+" md_descri like '"+scr+"%'";
				}
			}catch (Exception e){}
			if(!myselect.equals(""))
				myselect=" WHERE "+myselect;

			myselect=" SELECT * FROM rsa_motdim "+myselect+
			" ORDER BY md_codice,md_descri";
			System.out.println("QueryPaginate su rsamoti: "+myselect);
			ISASCursor dbcur=dbc.startCursor(myselect);
			//Vector vdbr=dbcur.getAllRecord();
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public ISASRecord insert(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		String codice=null;
		ISASConnection dbc=null;
		try {
			codice=(String)h.get("md_codice");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			ISASRecord dbr=dbc.newRecord("rsa_motdim");
			Enumeration n=h.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbc.writeRecord(dbr);
			String myselect="Select * from rsa_motdim where "+
			"md_codice='"+codice+"'";
			dbr=dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	public ISASRecord update(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		String codice=null;
		ISASConnection dbc=null;
		try {
			codice=(String)dbr.get("md_codice");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.writeRecord(dbr);
			String myselect="Select * from rsa_motdim where "+
			"md_codice='"+codice+"'";
			dbr=dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una update() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	public void delete(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done=true;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


}

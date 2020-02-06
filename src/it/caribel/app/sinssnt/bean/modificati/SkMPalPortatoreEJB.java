package it.caribel.app.sinssnt.bean.modificati;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//Scheda Portatore
//
//Elisa 19/11/09
//==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class SkMPalPortatoreEJB extends SINSSNTConnectionEJB  
{
	private static String nomeEJB = "SkMPalPortatoreEJB ";

	public SkMPalPortatoreEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " queryKey() - H == " + h.toString());
		boolean done=false;
		ISASConnection dbc=null;

		String cartella = null;
		String progr = null;

		try
		{
			cartella = h.get("n_cartella").toString();
			progr = h.get("skp_progr").toString();
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
			throw new SQLException(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
		}

		try
		{
			dbc=super.logIn(mylogin);

			String myselect="SELECT * FROM skmpal_portatore WHERE "+
			" n_cartella = "+ cartella + 
			" AND skp_progr = " + progr;

			System.out.println(nomeEJB + " QueryKey() - " + myselect);
			ISASRecord dbr=dbc.readRecord(myselect);

			dbc.close();
			super.close(dbc);
			done=true;

			return dbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE queryKey() - " + e);
			throw new SQLException(nomeEJB + " ERRORE queryKey() - " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{   System.out.println(nomeEJB + " ERRORE queryKey() - " + e);   }
			}
		}
	}

	// 25/09/07: legge l'ultimo rec (per data e progr) e vi aggiunge gli isas record per la griglia
	public ISASRecord queryKeyLast(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		try{
			dbc=super.logIn(mylogin);

			String mysel = "SELECT * FROM skmpal_portatore" +
			" WHERE n_cartella = " + (String)h.get("n_cartella");		
			
			String myselCur = mysel + " ORDER BY skp_data DESC, skp_progr DESC";

			System.out.println("SkMPalSintEJB: QueryKeyLast - myselCur=["+myselCur+"]");
			ISASCursor dbcur = dbc.startCursor(myselCur);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			if ((vdbr != null) && (vdbr.size() > 0)){
				ISASRecord dbr_1 = (ISASRecord)vdbr.firstElement();
				String myselRec = mysel + " AND skp_progr = " + dbr_1.get("skp_progr");
				System.out.println("SkMPalSintEJB: QueryKeyLast - myselRec=["+myselRec+"]");

				dbr = dbc.readRecord(myselRec);					
				if (dbr != null)
					dbr.put("griglia", (Vector)vdbr);
			}

			dbc.close();
			super.close(dbc);
			done=true;

			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKeyLast()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}



	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " query() - H == " + h.toString());

		boolean done=false;
		ISASConnection dbc=null;

		String cartella = null;

		try
		{
			cartella = h.get("n_cartella").toString();
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
			throw new SQLException(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
		}

		try
		{
			dbc=super.logIn(mylogin);

			String myselect = "SELECT * FROM skmpal_portatore WHERE "+
			"n_cartella = " + cartella + 
			" ORDER BY skp_data DESC ";

			System.out.println(nomeEJB + " query skmpal_portatore() -  "+myselect);

			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;

			return vdbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE query() - " + e);
			throw new SQLException(nomeEJB + " ERRORE query() - " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{   System.out.println(nomeEJB + " ERRORE query() - " + e);   }
			}
		}
	}

	public ISASRecord insert(myLogin mylogin,Hashtable h) 
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " insert() - H == " + h.toString());

		boolean done=false;
		String n_cartella=null;
		int progr=0;
		ISASConnection dbc=null;

		try 
		{
			n_cartella=(String)h.get("n_cartella");
		}
		catch (Exception e)
		{
			System.out.println(nomeEJB + " ERRORE insert() - MANCA CHIAVE - " + e);
			throw new SQLException(nomeEJB + " ERRORE insert() - MANCA CHIAVE - " + e);
		}

		try
		{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			ISASRecord dbr = dbc.newRecord("skmpal_portatore");

			String selProg = "SELECT MAX(skp_progr) progr FROM skmpal_portatore WHERE "+
			"n_cartella = " + n_cartella;
			ISASRecord dbrProg = dbc.readRecord(selProg);

			if(dbrProg.get("progr") != null)
				progr = Integer.parseInt(""+dbrProg.get("progr"));

			progr++;

			Enumeration n=h.keys();
			while(n.hasMoreElements())
			{
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}

			dbr.put("skp_progr", ""+progr);
			System.out.println("REC CHE SCRIVO == " + dbr.getHashtable().toString());
			dbc.writeRecord(dbr); 

			String myselect="SELECT * FROM skmpal_portatore WHERE n_cartella = " + n_cartella
			+	" AND skp_progr = " + progr;

			dbr=dbc.readRecord(myselect);
			System.out.println(nomeEJB + " insert() - select - "+myselect);

			if (dbr != null) 
				dbr.put("griglia", (Vector)leggiTuttiRec(dbc, n_cartella));

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " ERRORE insert() - " + e);
			try{	dbc.rollbackTransaction();   }
			catch(Exception e1)
			{
				throw new DBRecordChangedException(nomeEJB + " Errore eseguendo una rollback() - "+  e1);
			}

			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " ERRORE insert() - " + e);
			try{   dbc.rollbackTransaction();   }
			catch(Exception e1)
			{
				throw new ISASPermissionDeniedException(nomeEJB + " Errore eseguendo una rollback() - "+  e1);
			}

			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE insert() - " + e);
			try{	dbc.rollbackTransaction();   }
			catch(Exception ex)
			{
				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
			}

			throw new SQLException(nomeEJB + " ERRORE insert() - " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{ System.out.println(nomeEJB + " ERRORE insert() - " + e);   }
			}
		}
	}

	public ISASRecord update(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;

		String cartella=null;
		String progr=null;

		try 
		{
			cartella=(String)dbr.get("n_cartella");
			progr=""+dbr.get("skp_progr");
		}
		catch (Exception e)
		{
			System.out.println(nomeEJB + " ERRORE update - MANCA CHIAVE " + e);
			throw new SQLException(nomeEJB + " ERRORE update - MANCA CHIAVE " + e);
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM skmpal_portatore WHERE "+
			"n_cartella = " + cartella + 
			" AND skp_progr = " + progr;
			dbr=dbc.readRecord(myselect);

			if (dbr != null)
				dbr.put("griglia", (Vector)leggiTuttiRec(dbc, cartella));

			dbc.close();
			super.close(dbc);
			done=true;

			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " ERRORE update() - " + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " ERRORE update() - " + e);
			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE update() - " + e);
			throw new SQLException(nomeEJB + " ERRORE update() - " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{   System.out.println(nomeEJB + " ERRORE update() - " + e);   }
			}
		}
	}

	public void delete(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;

		try
		{
			dbc=super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done=true;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " ERRORE delete() - " + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " ERRORE delete() - " + e);
			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE delete() - " + e);
			throw new SQLException(nomeEJB + " ERRORE delete() - " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{  System.out.println(nomeEJB + " ERRORE delete() - " + e);   }
			}
		}
	}

	private Vector leggiTuttiRec(ISASConnection mydbc, String cart) throws Exception
	{
		String myselect = "SELECT * FROM skmpal_portatore" +
		" WHERE n_cartella = " + cart +
		" ORDER BY skp_data DESC, skp_progr DESC";

		ISASCursor dbcur = mydbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		dbcur.close();
		return vdbr;
	}
}
package it.caribel.app.sinssnt.bean;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------

//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
// ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
// 29/06/2009 - EJB di connessione alla procedura SINS Tabella rsa_tipo_assistito
//
// Elisa Croci
//
//==========================================================================

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
//ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
public class RsaTipoAssistitoEJB extends SINSSNTConnectionEJB  
{
	private static String nomeEJB = "RsaTipoAssistitoEJB ";
	
	public RsaTipoAssistitoEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException, Exception
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		String codice = "";
		
		try	{	
			codice = h.get("codice").toString();	}
		catch(Exception e)
		{
			return null;
		}
		
		try
		{
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM rsa_tipo_assistito WHERE  codice = '" + codice + "' ";
			System.out.println(nomeEJB + " QueryKey == " + myselect);
			
			ISASRecord dbr = dbc.readRecord(myselect);			
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SQLException(nomeEJB + " Errore eseguendo una queryKey()  ");
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
				catch(Exception e1){   System.out.println(nomeEJB + " QueryKey(): " + e1);   }
			}
		}
	} // fine query key

	public String duplicateChar(String s, String c) 
	{
		if ((s == null) || (c == null)) return s;
	
		String mys = new String(s);
		int p = 0;
		while (true) 
		{
			int q = mys.indexOf(c, p);
			if (q < 0) return mys;
		
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " queryPaginate() - Hash: " + h.toString());
		boolean done = false;
		String scr = null;
		ISASConnection dbc = null;
	
		try
		{
			dbc = super.logIn(mylogin);
			
			String myselect =  "SELECT * FROM rsa_tipo_assistito ";
			String endselect = " ORDER BY descrizione ";
			
			if(h.containsKey("descrizione"))
			{	
				scr = h.get("descrizione").toString();
				
				if(scr != null && !scr.equals(""))
				{
					scr = duplicateChar(scr,"'");
					myselect = myselect + " WHERE descrizione like '" + scr + "%'";
				}
			}
			
			myselect = myselect + endselect;
			
			System.out.println(nomeEJB + " QueryPaginate() - select == " + myselect);
			ISASCursor dbcur=dbc.startCursor(myselect);
			
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			
			if(vdbr != null && vdbr.size() > 0)
			{
				for(int i = 0; i < vdbr.size()-1; i++)
				{
					ISASRecord r = (ISASRecord) vdbr.get(i);
					
					if(r != null)
					{
						// Elisa 01/09/09: eliminato il tipo utente da "rsa_tipo_assistito"
						/*if(r.get("cod_tipute") != null)
						{
							String desc_ute = ISASUtil.getDecode(dbc, "tipute", "codice", 
									r.get("cod_tipute").toString(), "descrizione");
							
							r.put("desc_tipute", desc_ute);
						}*/
						
						if(r.get("cod_tipass") != null && !r.get("cod_tipass").toString().equals(""))
						{
							String cod_tipass = r.get("cod_tipass").toString();
							String desc_tipass = "";
							
							if(cod_tipass.equals("1"))
								desc_tipass = "minore";
							else if(cod_tipass.equals("2"))
								desc_tipass = "adulto";
							else desc_tipass = "anziano";
							
							r.put("desc_tipass", desc_tipass);
						}
						
						if(r.get("cod_autosu") != null && !r.get("cod_autosu").toString().equals(""))
						{
							String cod_autosu = r.get("cod_autosu").toString();
							String desc_autosu = "";
							
							if(cod_autosu.equals("S"))
								desc_autosu = "autosuff";
							else desc_autosu = "non autosuff";
							
							r.put("desc_autosu", desc_autosu);
						}
					}
				}
			}
						
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			
			return vdbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + e);
			throw new SQLException(nomeEJB + "Errore eseguendo una QueryPaginate()  ");
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
				catch(Exception e1){System.out.println(nomeEJB + e1);}
			}
		}
	} // fine paginate

	public ISASRecord insert(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " - insert(), HASH: " + h.toString());
		
		boolean done = false;
		String codice = null;
		String comune = null;
		String anno = null;
		ISASConnection dbc = null;
	
		try{   codice = h.get("codice").toString();		}
		catch (Exception e)
		{
			System.out.println(nomeEJB + e);
			throw new SQLException(nomeEJB + " - insert() ==  manca il codice");
		}
		
		try
		{
			dbc = super.logIn(mylogin);
			ISASRecord dbr = dbc.newRecord("rsa_tipo_assistito");
			
			Enumeration n = h.keys();
			while(n.hasMoreElements())
			{
				String e = n.nextElement().toString();
				dbr.put(e,h.get(e));
			}
			
			dbc.writeRecord(dbr);
						
			String myselect = "SELECT * FROM rsa_tipo_assistito WHERE codice = '" + codice + "' ";
			System.out.println(nomeEJB + " select dopo insert record == " + myselect);
			
			dbr = dbc.readRecord(myselect);
			
			dbc.close();
			super.close(dbc);
			done = true;
			
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " insert() " + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " insert() " + e);
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - "+  e1);
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
				catch(Exception e2)
				{   System.out.println(nomeEJB + " insert() " + e2);   }
			}
		}
	} // fine insert

	public ISASRecord update(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " update() - DBR == " + dbr.getHashtable().toString());
		
		boolean done = false;
		String codice = "";
		ISASConnection dbc = null;
		
		try{	codice=(String)dbr.get("codice"); }
		catch (Exception e)
		{
			System.out.println(nomeEJB + " - update() - " + e);
			throw new SQLException(nomeEJB + " - update() - " + "Errore: manca la chiave primaria");
		}
		
		try
		{
			dbc = super.logIn(mylogin);
			dbc.writeRecord(dbr);
			
			String myselect = "SELECT * FROM rsa_tipo_assistito WHERE codice = '" + codice + "' ";
			dbr = dbc.readRecord(myselect);
			
			dbc.close();
			super.close(dbc);
			done = true;
			
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " - update() - " + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " - update() - " + e);
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(nomeEJB + " - update() - " + e1);
			throw new SQLException("Errore eseguendo una update() - "+  e1);
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
				catch(Exception e2)
				{    System.out.println(nomeEJB + " - update() - " + e2);   }
			}
		}
	} // fine update


	public void delete(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " delete() - DBR == " + dbr.getHashtable().toString());
		
		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			
			dbc.close();
			super.close(dbc);
			done = true;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " delete() - " + e);
			throw e;
		}catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " delete() - " + e);
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(nomeEJB + " delete() - " + e1);
			throw new SQLException(nomeEJB + " delete() - " +  e1);
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
				catch(Exception e2)
				{   System.out.println(nomeEJB + " delete() - " + e2);   }
			}
		}
	} // fine delete
}

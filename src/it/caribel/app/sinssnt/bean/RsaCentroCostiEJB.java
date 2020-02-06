package it.caribel.app.sinssnt.bean;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//
//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
// ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
// 26/06/2009 - EJB di connessione alla procedura SINS Tabella rsa_centrocosto
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
import it.pisa.caribel.util.ISASUtil;

//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
//ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
public class RsaCentroCostiEJB extends SINSSNTConnectionEJB  
{
	private static String nomeEJB = "RsaCentroCostiEJB ";
	
	public RsaCentroCostiEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException, Exception
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		String codice = "";
		
		try	{	codice = h.get("codice").toString();	}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " queryKey() -- manca codice! [" + e + "] ");
			throw e;
		}
		
		try
		{
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM rsa_centrocosto WHERE  codice = '" + codice + "' ";
			System.out.println(nomeEJB + " QueryKey == " + myselect);
			
			ISASRecord dbr = dbc.readRecord(myselect);			
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " Errore permessi eseguendo una queryKey() - " + e);
			throw new ISASPermissionDeniedException(nomeEJB + " Errore eseguendo una  queryKey() - "+  e);
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " Errore eseguendo una queryKey() - " + e);
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
		
			String myselect =  "SELECT * FROM rsa_centrocosto ";
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
	
	//Elisa 12/11/09 - aggiunti zona e distretto come criteri di selezione
	// chiamata dalla frame di stampa dei raggruppamenti dei centri di costo
	public Vector queryPaginateRaggrup(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " queryPaginate() - Hash: " + h.toString());
		boolean done = false;
		String scr = null;
		ISASConnection dbc = null;

		try
		{
			dbc = super.logIn(mylogin);

			String myselect =  " SELECT DISTINCT(a.cod_centrocosto_padre), cc.descrizione," +
					" cc.cod_zona, z.descrizione_zona, cc.cod_distretto, d.des_distr " +
					" FROM rsa_assoc_ccosto a, zone z, distretti d, " +
					" rsa_centrocosto cc WHERE a.cod_centrocosto_padre = cc.codice " +
					" AND cc.cod_zona = z.codice_zona AND cc.cod_distretto = d.cod_distr ";

			if(h.containsKey("descrizione"))
			{	
				scr = h.get("descrizione").toString();

				if(scr != null && !scr.equals(""))
				{
					scr = duplicateChar(scr,"'");
					myselect += " AND cc.descrizione like '" + scr + "%'";
				}
			}
			
			if(h.containsKey("cod_zona") && !h.get("cod_zona").equals(""))
				myselect += " AND cc.cod_zona = '" + h.get("cod_zona") + "' ";
			
			if(h.containsKey("cod_distr") && !h.get("cod_distr").equals(""))
				myselect += " AND cc.cod_distr = '" + h.get("cod_distr") + "' ";			

			System.out.println(nomeEJB + " QueryPaginate() - select == " + myselect);
			ISASCursor dbcur=dbc.startCursor(myselect);

			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);

			if(vdbr != null)
			{
				for(int i = 0; i < vdbr.size(); i++)
				{
					Object o = vdbr.get(i);
					if(o instanceof ISASRecord)
					{
						ISASRecord rec = (ISASRecord)o;
						String cod = rec.get("cod_centrocosto_padre").toString();
						String descr = ISASUtil.getDecode(dbc, "rsa_centrocosto", "codice", cod, "descrizione");
						rec.put("descrizione", descr);
						rec.put("codice", cod);
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
			System.out.println(nomeEJB + "Errore eseguendo una queryPaginate()  " + e);
			throw new SQLException(nomeEJB + "Errore eseguendo una queryPaginate()  ");
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
				catch(Exception e1)
				{   System.out.println(nomeEJB + "Errore eseguendo una queryPaginateStampa()  " + e1);   }
			}
		}
	} // fine paginate stampa

	public ISASRecord insert(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " - insert(), HASH: " + h.toString());
		
		boolean done = false;
		String codice = null;
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
			ISASRecord dbr = dbc.newRecord("rsa_centrocosto");
			
			Enumeration n = h.keys();
			while(n.hasMoreElements())
			{
				String e = n.nextElement().toString();
				dbr.put(e,h.get(e));
			}
			
			dbc.writeRecord(dbr);
						
			String myselect = "SELECT * FROM rsa_centrocosto WHERE codice = '" + codice + "' ";
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
			
			String myselect = "SELECT * FROM rsa_centrocosto WHERE codice ='" + codice + "' ";
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
			String codicecc = dbr.get("codice").toString();
			
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			
			// Elisa 25/11/09 - eliminazione del raggruppamento, se esiste!
			String sel = " SELECT * FROM rsa_assoc_ccosto WHERE cod_centrocosto_padre = '" + codicecc + "' ";
			ISASCursor cur = dbc.startCursor(sel);
			
			if(cur != null)
			{
				while(cur.next())
				{
					ISASRecord rec = cur.getRecord();
					String codfiglio = rec.get("cod_centrocosto").toString();
					sel = " SELECT * FROM rsa_assoc_ccosto WHERE " +
							" cod_centrocosto_padre = '" + codicecc + 
							"' AND cod_centrocosto = '" + codfiglio + "' ";
					
					ISASRecord r = dbc.readRecord(sel);
					if(r != null) dbc.deleteRecord(r);
				}
				
				cur.close();
			}
			// --------------------------------------------------------------------
			
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
	
	// Elisa 07/09/09
	public Vector queryCombo(myLogin mylogin,Hashtable h) throws SQLException 
	{
		System.out.println(nomeEJB + " queryCombo() -- H == " + h.toString());
		
		boolean done = false;
		ISASConnection dbc = null;
	
		try 
		{
			Vector v = new Vector();
			String zona = null;
			String distretto = null;

			if(h.get("cod_zona") != null && !((String)h.get("cod_zona")).equals("") 
					&& !((String)h.get("cod_zona")).equals("TUTTO"))
				zona = h.get("cod_zona").toString();
			
			if(h.get("cod_distretto") != null && !((String)h.get("cod_distretto")).equals("") 
					&& !((String)h.get("cod_distretto")).equals("TUTTO"))
				distretto = h.get("cod_distretto").toString();
			
			String sel = "SELECT * FROM rsa_centrocosto ";
			String where = null;
			
			if(zona != null) where = " cod_zona = '" + zona + "' ";
			if(distretto != null)
				if(where != null) where += " AND cod_distretto = '" + distretto + "' ";
				else where += " cod_distretto = '" + distretto + "' ";

			if(where != null) sel = sel + " WHERE " + where;
			sel += " ORDER BY descrizione";
			
			dbc = super.logIn(mylogin);
			System.out.println(nomeEJB + " queryCombo() -- " + sel);
			ISASCursor curd = dbc.startCursor(sel);
			
			v = curd.getAllRecord();
			
			ISASRecord dbr = dbc.newRecord("rsa_centrocosto");
			dbr.put("codice", "TUTTO");
			dbr.put("descrizione", "TUTTI");
			v.insertElementAt((Object)dbr,0);
			
			dbc.close();
			super.close(dbc);
			done = true;
			
			return v;
		} 
		catch(Exception e) 
		{
			System.out.println(nomeEJB + " ERRORE queryCombo() -- " + e);
			throw new SQLException(nomeEJB + " ERRORE queryCombo() " + e);
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
				{	System.out.println(nomeEJB + " ERRORE queryCombo() -- " + e);		}
			}
		}
	}// fine queryCombo()
}


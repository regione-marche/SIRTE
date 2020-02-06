package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// 04/03/09 - EJB di connessione alla procedura SINS Tab. tab_pap
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;


public class TabPapEJB extends SINSSNTConnectionEJB  
{
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	public TabPapEJB() {}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;

		try{
			dbc = super.logIn(mylogin);
	
			String myselect = "SELECT * FROM tab_pap" +
					" WHERE codice = '" + (String)h.get("codice") + "'";

//          System.out.println("TabPap_querykey: myselect=[" + myselect + "]");
			ISASRecord dbr = dbc.readRecord(myselect);
			
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}catch(Exception e){
			System.out.println("TabPapEJB: queryKey - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("TabPapEJB: queryKey - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END queryKey

	public Vector query(myLogin mylogin, Hashtable h) throws  SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try{
			dbc = super.logIn(mylogin);
			String myselect = faiSQLdaDesc(h);
	        System.out.println("TabPapEJB: query= " + myselect);

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("TabPapEJB: query - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("TabPapEJB: query - Eccezione nella chiusura della connessione= " + e1);
				}
	   	    }
	   	}
	}// END query

	public Vector queryPaginate(myLogin mylogin, Hashtable h) throws SQLException 
	{
		boolean done = false;
    	ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try{
			dbc=super.logIn(mylogin);
			String myselect = faiSQLdaDesc(h);
	        System.out.println("TabPapEJB: queryPaginate= " + myselect);

		    dbcur = dbc.startCursor(myselect, 200);
            int start = Integer.parseInt((String)h.get("start"));
            int stop = Integer.parseInt((String)h.get("stop"));
            Vector vdbr = dbcur.paginate(start, stop);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("TabPapEJB: queryPaginate - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("TabPapEJB: queryPaginate - Eccezione nella chiusura della connessione= " + e1);
				}
	   	    }
	   	}
	}// END queryPaginate

	private String faiSQLdaDesc(Hashtable h0) 
	{
		it.pisa.caribel.util.ServerUtility servUtil = new it.pisa.caribel.util.ServerUtility();
		String sel = "SELECT * FROM tab_pap";

		String desc = (String)h0.get("descrizione");
		
		if ((desc != null) && (!desc.trim().equals(""))) {
			desc = duplicateChar(desc, "'");
			sel += " WHERE descrizione LIKE '" + desc + "%'";
		}

		sel += " ORDER BY descrizione";
		return sel;
	}// END faiSQLdaDesc



	public ISASRecord insert(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done = false;
		String codice = null;
		ISASConnection dbc = null;
		
		try{
			dbc = super.logIn(mylogin);

			ISASRecord dbr = dbc.newRecord("tab_pap");
			Enumeration n = h.keys();
			while (n.hasMoreElements()){
				String e = (String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM tab_pap" +
					" WHERE codice = '" + (String)h.get("codice") + "'";

			dbr = dbc.readRecord(myselect);

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}catch(DBRecordChangedException e){
			System.out.println("TabPapEJB: insert - DBRecordChangedException= " + e);
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("TabPapEJB: insert - ISASPermissionDeniedException= " + e);
			throw e;
		}catch(Exception e1){
			System.out.println("TabPapEJB: insert - Eccezione= " + e1);
			throw new SQLException("TabPapEJB: Errore eseguendo una insert() - " + e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("TabPapEJB: insert - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END insert

	public ISASRecord update(myLogin mylogin, ISASRecord dbr)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done = false;
		String codice = null;
		ISASConnection dbc = null;
		
		try{
			dbc = super.logIn(mylogin);

			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM tab_pap" +
					" WHERE codice = '" + (String)dbr.get("codice") + "'";
			dbr = dbc.readRecord(myselect);

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}catch(DBRecordChangedException e){
			System.out.println("TabPapEJB: update - DBRecordChangedException= " + e);
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("TabPapEJB: update - ISASPermissionDeniedException= " + e);
			throw e;
		}catch(Exception e1){
			System.out.println("TabPapEJB: update - Eccezione= " + e1);
			throw new SQLException("TabPapEJB: Errore eseguendo una update() - " + e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("TabPapEJB: update - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END update


	public void delete(myLogin mylogin, ISASRecord dbr)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);

			dbc.close();
			super.close(dbc);
			done = true;
		}catch(DBRecordChangedException e){
			System.out.println("TabPapEJB: delete - DBRecordChangedException= " + e);
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("TabPapEJB: delete - ISASPermissionDeniedException= " + e);
			throw e;
		}catch(Exception e1){
			System.out.println("TabPapEJB: delete - Eccezione= " + e1);
			throw new SQLException("TabPapEJB: Errore eseguendo una delete() - " + e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("TabPapEJB: delete - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END delete


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

}

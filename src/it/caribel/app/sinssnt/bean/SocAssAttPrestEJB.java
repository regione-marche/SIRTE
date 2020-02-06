package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 30/05/2000 - EJB di connessione alla procedura SINS Tabella subzona
//
// paolo ciampolini
//
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class SocAssAttPrestEJB extends SINSSNTConnectionEJB  {

public SocAssAttPrestEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	try{
		dbc = super.logIn(mylogin);
		String myselect = "SELECT * FROM ass_attiv_prest WHERE cod_attivita ='"+
			(String)h.get("cod_attivita")+"'";
		System.out.println("Query AssAttPrest in queryKey : " + myselect);
		ISASRecord dbr = dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	} catch(Exception e) {
		System.out.println("SocAssAttPrestEJB.query_key(): "+e);
		throw new SQLException("Errore eseguendo una queryKey()");
	} finally {
		if(!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				System.out.println(e1);
			}
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


public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    String scr=" ";
    try{
        dbc=super.logIn(mylogin);
        String myselect="Select * from ass_attiv_prest";

       //controllo valore corretto di DES_ATTIVITA

        scr=(String)(h.get("des_attivita"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where des_attivita like '"+scr+"%'";
              }
        myselect=myselect+" order by des_attivita ";
        System.out.println("query GridAssAttPrest in query : " + myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        System.out.println("Fatta la startcursor");
	    Vector vdbr=dbcur.getAllRecord();
        System.out.println("Creato il vettore");
	    dbcur.close();
		dbc.close();
	    super.close(dbc);
	    done=true;
		return vdbr;
    } catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
   	}
   	finally{
   	    if(!done){
   	        try{
		dbc.close();
			super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}

}

public Vector queryPaginate(myLogin mylogin,Hashtable h)
throws  SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    String scr=" ";
    try{
        dbc=super.logIn(mylogin);
        String myselect="Select * from ass_attiv_prest";

       //controllo valore corretto di DES_ATTIVITA

        scr=(String)(h.get("des_attivita"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where des_attivita like '"+scr+"%'";
              }
        myselect = myselect + " order by des_attivita ";
        System.out.println("query GridAssAttPrest in queryPaginate : " + myselect);
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
    } catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
   	}
   	finally{
   	    if(!done){
   	        try{
		dbc.close();
			super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}

}


public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String codice=null;
    ISASConnection dbc=null;
    try {
        codice=(String)h.get("cod_attivita");
    }
    catch (Exception e){
        e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        ISASRecord dbr=dbc.newRecord("ass_attiv_prest");
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        String myselect="Select * from ass_attiv_prest where cod_attivita = '" + codice + "'";
        System.out.println("query AssAttPrest in insert : " + myselect);
                dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
    }
    catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }
    catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }

    catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una insert() - "+  e1);
    }
    finally{
   	    if(!done){
   	        try{
		dbc.close();
			super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}

}


public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String codice=null;
    ISASConnection dbc=null;
    try {
        codice=(String)dbr.get("cod_attivita");
    }
    catch (Exception e){
        e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        dbc.writeRecord(dbr);
        String myselect="Select * from ass_attiv_prest where cod_attivita = '" + codice + "'";
        System.out.println("query AssAttPrest in update : " + myselect);
		dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
    }
    catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }
    catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }

    catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una update() - "+  e1);
    }
    finally{
   	    if(!done){
   	        try{
		dbc.close();
			super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}

}


public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        dbc.deleteRecord(dbr);
		dbc.close();
		super.close(dbc);
		done=true;
    }
    catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }

    catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }

    catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una delete() - "+  e1);
    }
    finally{
   	    if(!done){
   	        try{
		dbc.close();
			super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}

}
}

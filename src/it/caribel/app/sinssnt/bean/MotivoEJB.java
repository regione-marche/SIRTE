package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 08/05/2000 - EJB di connessione alla procedura SINS Tabella Motivo
//
// paolo ciampolini
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

public class MotivoEJB extends SINSSNTConnectionEJB  {

public MotivoEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from motivo where "+
			"codice='"+(String)h.get("codice")+"'";
		ISASRecord dbr=dbc.readRecord(myselect);
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

public Vector queryCombo(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	try {
		Vector v = query(mylogin,h);
        	dbc=super.logIn(mylogin);
        	ISASRecord dbr = dbc.newRecord("motivo");
		dbr.put("codice", "TUTTO");
		dbr.put("descrizione", "TUTTI");
		v.insertElementAt((Object)dbr,0);
		dbc.close();
		super.close(dbc);
		done = true;
		return v;
	} catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryCombo()  ");
   	} finally {
		if(!done) {
			try {
				dbc.close();
				super.close(dbc);
			}catch(Exception e1) {
				System.out.println(e1);
			}
		}
   	}
}

public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from motivo";

      //controllo valore corretto descrizione

        scr=(String)(h.get("descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
	       scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione like '"+scr+"%'";
              }
        myselect=myselect+" ORDER BY descrizione ";
        System.out.println("query GridMotivo: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
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
//Metodo che carica tutte le voci della tabella aggiungendo 'TUTTI'
public Vector queryTutte(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
	    dbc=super.logIn(mylogin);
	    String myselect="SELECT * FROM motivo ORDER BY descrizione";
            System.out.println("query GridMotivo: "+myselect);
            ISASCursor dbcur=dbc.startCursor(myselect);
            Vector vdbr=dbcur.getAllRecord();
            ISASRecord dbr = dbc.newRecord("motivo");
            dbr.put("codice", "TUTTO");
            dbr.put("descrizione", "TUTTI");
            vdbr.insertElementAt((Object)dbr,0);
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
public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from motivo";

      //controllo valore corretto descrizione

        scr=(String)(h.get("descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
	       scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione like '"+scr+"%'";
              }
        myselect=myselect+" ORDER BY descrizione ";
        System.out.println("query GridMotivo: "+myselect);
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
		codice=(String)h.get("codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("motivo");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from motivo where "+
			"codice='"+codice+"'";
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
		codice=(String)dbr.get("codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from motivo where "+
			"codice='"+codice+"'";
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

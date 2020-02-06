package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 07/10/2004 - EJB di connessione alla procedura SINS Tabella Tabbias
//
// Giulia Brogi
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
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class BisAssEJB extends SINSSNTConnectionEJB  {

public BisAssEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * from tabbias where "+
			"tba_codice="+(String)h.get("tba_codice");
		ISASRecord dbr=dbc.readRecord(myselect);
                System.out.println("querykey tabbias: "+myselect);
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

public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	ServerUtility su =new ServerUtility();
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from tabbias";

        //controllo valore corretto tba_descrizione
	String tba_descrizione="";
	String cod_zona="";
	if(h.get("tba_descrizione")!=null)
	  tba_descrizione=(String)(h.get("tba_descrizione"));

	if(!tba_descrizione.equals(""))
	  myselect=myselect+" WHERE ";

	myselect = su.addWhere(myselect, su.REL_AND, "tba_descrizione", su.OP_LIKE,duplicateChar(tba_descrizione,"'"));
	String mywhere = su.addWhere("", su.REL_AND, "cod_zona", su.OP_EQ_STR,cod_zona);
	myselect=myselect+mywhere;
        myselect=myselect+" ORDER BY tba_descrizione ";
        System.out.println("query GridBisAss: "+myselect);
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

public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from tabbias";

        //controllo valore corretto tba_descrizione

        scr=(String)(h.get("tba_descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where tba_descrizione like '"+scr+"%'";
              }
        myselect=myselect+" ORDER BY tba_descrizione ";
        System.out.println("query GridBisAss: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
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
	String tba_codice=null;
	ISASConnection dbc=null;
	try {
		tba_codice=(String)h.get("tba_codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("tabbias");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from tabbias where tba_codice='"+tba_codice+"'";
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
	String tba_codice=null;
	ISASConnection dbc=null;
	try {
		tba_codice=(String)dbr.get("tba_codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from tabbias where "+
			"tba_codice="+tba_codice;
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

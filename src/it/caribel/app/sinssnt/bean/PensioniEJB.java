package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 27/01/2003 - EJB di connessione alla procedura SINS Tabella Pensioni
//
// Jessica Caccavale
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

public class PensioniEJB extends SINSSNTConnectionEJB  {
public PensioniEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from rsa_pensioni where "+
			"pe_codice='"+(String)h.get("pe_codice")+"'";
		ISASRecord dbr=dbc.readRecord(myselect);
                System.out.println("QueryKey pensioni prova "+myselect);
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
                  scr=(String)(h.get("pe_descri"));
                  if(!scr.equals("") && scr!=null) {
                      scr=duplicateChar(scr,"'");
                      if (myselect.equals(""))
                        myselect=myselect+" pe_descri like '"+scr+"%'";
                      else
                        myselect=myselect+" pe_descri like '"+scr+"%'";
                  }
                }catch (Exception e){}
                if(!myselect.equals(""))
		    myselect=" WHERE "+myselect;

                myselect=" SELECT * FROM rsa_pensioni "+myselect+
                         " ORDER BY pe_codice,pe_descri";
		System.out.println("QueryPaginate su pensioni: "+myselect);
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
		codice=(String)h.get("pe_codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("rsa_pensioni");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from rsa_pensioni where "+
			"pe_codice='"+codice+"'";
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
		codice=(String)dbr.get("pe_codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from rsa_pensioni where "+
			"pe_codice='"+codice+"'";
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

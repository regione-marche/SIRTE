package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 16/03/2004 - EJB di connessione alla procedura SINS Tabella SSoctpre
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

public class SSoctpreEJB extends SINSSNTConnectionEJB  {

public SSoctpreEJB() {}
private static final String MIONOME = "2-SSoctpreEJB.";
public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	String punto = MIONOME + "queryKey ";
	stampa(punto + " inizio con dati>"+ h+ "<");
	boolean obsoleto = getObsoleto(h);
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * from ssoctpre where "+
			"codice='"+(String)h.get("codice")+"' ";
		
		if (obsoleto){
			myselect += " AND obsoleto = 'N'";
		}
		stampa(punto + " query>" +myselect);
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

private boolean getObsoleto(Hashtable h) {
	String punto = MIONOME + "getObsoleto ";
	boolean obsoleto = false;
	try {
		String valObsoleto = h.get("parobsoleto")+"";
		obsoleto = (valObsoleto!=null && (valObsoleto.trim().equalsIgnoreCase("S")));
	} catch (Exception e) {
		stampa(punto + "Errore nel recuperare il filtro di obsoleto ");
	}
	stampa(punto + " obsoleto>"+ obsoleto+"<");

	return obsoleto;
}


private void stampa(String messaggio) {
	System.out.println(messaggio);
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
	String punto = MIONOME + "query ";
	stampa(punto + "\n dati che ricevo>"+ h+"<\n");
	ServerUtility su =new ServerUtility();
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String query="Select * from ssoctpre ";
		String myselect ="";
        //controllo valore corretto descrizione
	String descrizione="";
	String cod_zona="";
	if(h.get("descrizione")!=null)
	  descrizione=(String)(h.get("descrizione"));

//	if(!descrizione.equals(""))
//	  myselect=myselect+" WHERE ";

	myselect = su.addWhere(myselect, su.REL_AND, "descrizione", su.OP_LIKE,duplicateChar(descrizione,"'"));
	String mywhere = su.addWhere("", su.REL_AND, "cod_zona", su.OP_EQ_STR,cod_zona);
	
	boolean obsoleto = getObsoleto(h);
	if(obsoleto) {
		myselect += (myselect.trim().length()>0? " AND ":"") +" obsoleto = 'N' "; 
	}
	
	if(myselect.trim().length()>0){
		myselect =" WHERE " +myselect;
	}
	
	myselect=query + myselect+mywhere;
        myselect=myselect+" ORDER BY descrizione ";
        stampa(punto + " query>"+ myselect);
        
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
	String punto = MIONOME + "queryPaginate ";
    stampa(punto + "\n dati che ricevo>"+ h+"<\n");
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	boolean obsoleto = getObsoleto(h);
	try{
		dbc=super.logIn(mylogin);
		String query="Select * from ssoctpre ";
		String myselect = "";
        //controllo valore corretto descrizione

        scr=(String)(h.get("descrizione"));
	    if (!(scr==null)){
	    	
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" descrizione like '"+scr+"%'";
              }
	    }
	    if (obsoleto){
	    	myselect += (myselect.trim().length()>0 ? " and ": "" )+"  obsoleto = 'N' ";
	    }
	    
	    myselect =(myselect.trim().length()>0?" WHERE ":"")+ myselect;
	    
        myselect=query + myselect+" ORDER BY descrizione ";
        stampa(punto +"Query>"+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
//		Vector vdbr=dbcur.getAllRecord();
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
		ISASRecord dbr=dbc.newRecord("ssoctpre");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from ssoctpre where codice='"+codice+"'";
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
		String myselect="Select * from ssoctpre where "+
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

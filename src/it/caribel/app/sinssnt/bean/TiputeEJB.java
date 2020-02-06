package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 08/05/2000 - EJB di connessione alla procedura SINS Tabella Tipute
// paolo ciampolini
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

public class TiputeEJB extends SINSSNTConnectionEJB  {



public TiputeEJB() {}







public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    try{
		    dbc=super.logIn(mylogin);
		    String myselect="Select * from tipute where codice='"+(String)h.get("codice")+"'";
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

//ILARIA 11/02/2005 INIZIO

public Vector queryComboNESDIV(myLogin mylogin,Hashtable h) throws
SQLException
{
        boolean done = false;
        ISASConnection dbc = null;
        try {
                Vector v = query(mylogin,h);
                dbc=super.logIn(mylogin);
                ISASRecord dbr = dbc.newRecord("tipute");
                dbr.put("codice", "NESDIV");
                dbr.put("descrizione", "NESSUNA DIVISIONE");

                v.insertElementAt((Object)dbr,0);
                dbr = dbc.newRecord("tipute");
                dbr.put("codice", "TUTTO");
                dbr.put("descrizione", "TUTTE");
                v.insertElementAt((Object)dbr,0);
                dbc.close();
                super.close(dbc);
                done = true;
                return v;
        } catch(Exception e) {
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una queryCombo()");
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



public Vector queryCombo(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	try {
		Vector v = query(mylogin,h);
        	dbc=super.logIn(mylogin);
        	ISASRecord dbr = dbc.newRecord("tipute");
		dbr.put("codice", "TUTTO");
		dbr.put("descrizione", "TUTTE");
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
        String myselect="Select * from tipute";


      //controllo valore corretto descrizione
System.out.println("forza sara");
        scr=(String)(h.get("descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione like '"+scr+"%'";
              }
        myselect=myselect+" ORDER BY descrizione ";
        System.out.println("query GridTipute: "+myselect);
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

//gb 13/11/07 *******
//	metodo per selezionare le voci della combo 'Tipo utente' nella form
//	JFrameASAssProgetto del sinssnt.
public Vector query_asprogetto(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    String strAreaInterv="";
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        String myselect="SELECT * FROM tipute";


	System.out.println("Tipute/query_asprogetto");
        strAreaInterv = (String)(h.get("area_interv"));
	if ((strAreaInterv!=null) && !strAreaInterv.equals(""))
	   {
	   myselect=myselect+" WHERE area_interv = '"+strAreaInterv+"'";
	   }
        myselect=myselect+" ORDER BY descrizione ";
        System.out.println("Tipute/query_asprogetto: "+myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        Vector vdbr=dbcur.getAllRecord();
        dbcur.close();
		dbc.close();
	    super.close(dbc);
	    done=true;
		return vdbr;
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_asprogetto()  ");
   	}finally{
   	    if(!done){
   	        try{
		dbc.close();
			super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}
}


public Vector queryComboasProgetto(myLogin mylogin,Hashtable h) throws 
SQLException 
{
        boolean done = false;
        ISASConnection dbc = null;
        try {
                Vector v = query_asprogetto(mylogin,h);
                dbc=super.logIn(mylogin);
                ISASRecord dbr = dbc.newRecord("tipute");
                dbr.put("codice", "NESDIV");
                dbr.put("descrizione", "NESSUNA DIVISIONE");
                v.insertElementAt((Object)dbr,0);
                dbr = dbc.newRecord("tipute");
                dbr.put("codice", "TUTTO");
                dbr.put("descrizione", "TUTTE");
                v.insertElementAt((Object)dbr,0);

                dbc.close();
                super.close(dbc);
                done = true;
                return v;
        } catch(Exception e) {
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una queryCombo()");
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

public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    String scr=" ";
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        String myselect="Select * from tipute";


      //controllo valore corretto descrizione

        scr=(String)(h.get("descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione like '"+scr+"%'";
              }
        myselect=myselect+" ORDER BY descrizione ";
        System.out.println("query GridTipute: "+myselect);
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


public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
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
        ISASRecord dbr=dbc.newRecord("tipute");
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        String myselect="Select * from tipute where codice='"+codice+"'";
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


public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
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
        String myselect="Select * from tipute where codice='"+codice+"'";
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


public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
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

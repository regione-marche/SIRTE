package it.caribel.app.sinssnt.bean.modificati;
//============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 18/07/2003 - EJB di connessione alla procedura SINS Tabella Branca
//
// Jessica Caccavale
//
// ============================================================================

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

public class BrancaEJB extends SINSSNTConnectionEJB  {

public BrancaEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
        ServerUtility su =new ServerUtility();
	boolean done=false;
	String tipo="";
	String tipo_new="";
        String mysel="";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM branca WHERE ";
                mysel=su.addWhere(mysel,su.REL_AND,"codice",su.OP_EQ_STR,(String)h.get("codice"));
                mysel=su.addWhere(mysel,su.REL_AND,"cod_tippre",su.OP_EQ_STR,(String)h.get("cod_tippre"));
                myselect=myselect+mysel;
                ISASRecord dbr=dbc.readRecord(myselect);
                if(dbr != null){
                    if(dbr.get("cod_tippre") != null && !(dbr.get("cod_tippre").equals(""))){
                      String pres = (String)dbr.get("cod_tippre");
                      String sel = "SELECT tippre_des FROM tippre WHERE tippre_cod='"+pres+"'";
                      ISASRecord dbpres = dbc.readRecord(sel);
                      dbr.put("tip_descrizione",dbpres.get("tippre_des"));
                    }
                   
                  }
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryKey=>Branche()  ");
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
public Vector query_combo(myLogin mylogin,Hashtable h) throws  SQLException {
	String metodo = "query_combo ";
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	ISASCursor dbcur= null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select cod_tippre || '|' || codice cod_branca,descrizione  from branca ORDER BY descrizione";

               System.out.println("Branca.query_combo: "+myselect);
		dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
                ISASRecord dbr = dbc.newRecord("branca");
                dbr.put("cod_branca", "TUT|TUT");
                dbr.put("descrizione", "TUTTE");
                vdbr.insertElementAt((Object)dbr,0);
                dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_combo()  ");
	}finally{
		close_dbcur_nothrow(metodo, dbcur);
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}
}


public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
        boolean condiz = false;
        ISASRecord dbr = null;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from branca ";


      //controllo valore corretto prest_des

        scr=(String)(h.get("descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione like '"+scr+"%' ";
               condiz = true;
              }

        scr=(String)(h.get("cod_tippre"));
	    if (!(scr==null) && !(scr.trim().equals(""))) {
            if(condiz)
                myselect=myselect+" and cod_tippre = '"+scr+"' ";
            else
                myselect=myselect+" where cod_tippre = '"+scr+"' ";
        }

        myselect=myselect+" ORDER BY codice,descrizione ";
        System.out.println("query GridBranca: "+myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        Vector vdbr=dbcur.getAllRecord();
        if ((vdbr != null) && (vdbr.size() > 0)){
          for(int i=0; i<vdbr.size()-1; i++)
          {
            dbr=(ISASRecord)vdbr.elementAt(i);
            String selIsti="SELECT tippre_des FROM tippre WHERE "+
                           "tippre_cod='"+(String)dbr.get("cod_tippre")+"'";
            ISASRecord dbIsti=dbc.readRecord(selIsti);
            dbr.put("desc_tipo",(String)dbIsti.get("tippre_des"));
          }
        }
        dbcur.close();
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
        boolean condiz = false;
        ISASRecord dbr = null;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from branca ";


      //controllo valore corretto prest_des

        scr=(String)(h.get("descrizione"));
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione like '"+scr+"%' ";
               condiz = true;
              }

        scr=(String)(h.get("cod_tippre"));
	    if (!(scr==null) && !(scr.trim().equals(""))) {
            if(condiz)
                myselect=myselect+" and cod_tippre = '"+scr+"' ";
            else
                myselect=myselect+" where cod_tippre = '"+scr+"' ";
        }

        myselect=myselect+" ORDER BY codice,descrizione ";
        System.out.println("query GridBranca: "+myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        int start = Integer.parseInt((String)h.get("start"));
        int stop = Integer.parseInt((String)h.get("stop"));
        Vector vdbr = dbcur.paginate(start, stop);
        if ((vdbr != null) && (vdbr.size() > 0)){
          for(int i=0; i<vdbr.size()-1; i++)
          {
            dbr=(ISASRecord)vdbr.elementAt(i);
            String selIsti="SELECT tippre_des FROM tippre WHERE "+
                           "tippre_cod='"+(String)dbr.get("cod_tippre")+"'";
            ISASRecord dbIsti=dbc.readRecord(selIsti);
            dbr.put("desc_tipo",(String)dbIsti.get("tippre_des"));
            System.out.println("DBR: "+dbr.getHashtable().toString());
          }
        }
        dbcur.close();
        super.close(dbc);
        done=true;
        return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryPaginate()  ");
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
        String cod_tippre=null;
        String tipo=null;
        String mysel="";
	ISASConnection dbc=null;
        ServerUtility su =new ServerUtility();

	try {
		codice=(String)h.get("codice");
                cod_tippre=(String)h.get("cod_tippre");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("branca");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="SELECT * FROM branca WHERE ";
                mysel=su.addWhere(mysel,su.REL_AND,"codice",su.OP_EQ_STR,codice);
                mysel=su.addWhere(mysel,su.REL_AND,"cod_tippre",su.OP_EQ_STR,cod_tippre);
                myselect=myselect+mysel;
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
        String cod_tippre=null;
        String mysel="";
	ISASConnection dbc=null;
        ServerUtility su =new ServerUtility();

	try {
		codice=(String)dbr.get("codice");
                cod_tippre=(String)dbr.get("cod_tippre");
	} catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="SELECT * FROM branca WHERE ";
                mysel=su.addWhere(mysel,su.REL_AND,"codice",su.OP_EQ_STR,codice);
                mysel=su.addWhere(mysel,su.REL_AND,"cod_tippre",su.OP_EQ_STR,cod_tippre);
                myselect=myselect+mysel;
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

package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 07/01/2003 - EJB di connessione alla procedura SINS Tabella Banche
//
// Jessica Caccavale
//
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class BancheEJB extends SINSSNTConnectionEJB  {

public BancheEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from banche where "+
			"ban_codice_abi='"+(String)h.get("ban_codice_abi")+"' and "+
			"ban_cab_sport='"+(String)h.get("ban_cab_sport")+"'";
		ISASRecord dbr=dbc.readRecord(myselect);
                System.out.println("QueryKey banche "+myselect);
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
	boolean done=false;
	ISASConnection dbc=null;
	ISASRecord dbr = null;
	ISASCursor dbcur = null;
	String myselect = "";
	String codreg = "";
        String scr=" ";
	String codazsan = "";
	try{
		dbc=super.logIn(mylogin);
		myselect="Select conf_key, conf_txt from conf where "+
			"conf_kproc ='SINS' and "+
			"(conf_key ='codice_regione' or "+
			"conf_key ='codice_usl')";
		dbcur=dbc.startCursor(myselect);
		for(int i=0; i<2; i++) {
			dbcur.next();
			dbr=dbcur.getRecord();
			if((dbr.get("conf_key")).equals("codice_regione")) {
				codreg = (String) dbr.get("conf_txt");
			}else if((dbr.get("conf_key")).equals("codice_usl")) {
				codazsan = (String) dbr.get("conf_txt");
			}
		}
		dbcur.close();
		myselect="Select * from presidi where ";

                //controllo valore corretto despres

                scr=(String)(h.get("despres"));
	        if (!(scr==null))
                  if (!(scr.equals(" ")))
                    {
                     scr=duplicateChar(scr,"'");
                     myselect=myselect+" despres like '"+scr+"%' and";
                    }
		myselect=myselect+" codreg='"+codreg+"' and codazsan='"+codazsan+ "' ORDER BY despres";
                System.out.println("query GridPresidi: "+myselect);

                dbcur=dbc.startCursor(myselect);
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
		boolean dato_presente = false;
		String myselect="";

                //controllo valore corretto codice abi
                try{
                  scr=(String)(h.get("ban_codice_abi"));
	          if(!scr.equals("") && scr!=null)
                  {
                    if (myselect.equals(""))
  		      myselect=myselect+" ban_codice_abi='"+scr+"'";
                    else
                      myselect=myselect+" AND ban_codice_abi='"+scr+"'";
                  }
               }catch (Exception e){}
                //Controllo esistenza codice cab sportello
                try{
                  scr=(String)(h.get("ban_cab_sport"));
                  if(!scr.equals("") && scr!=null) {
                      if (myselect.equals(""))
                        myselect=myselect+" ban_cab_sport='"+scr+"'";
                      else
                        myselect=myselect+" AND ban_cab_sport='"+scr+"'";
                   }
                }catch (Exception e){}
                //Controllo esistenza citta sportello
                try{
                  scr=(String)(h.get("ban_citta_sport"));
                  if(!scr.equals("") && scr!=null) {
                      if (myselect.equals(""))
                        myselect=myselect+" ban_citta_sport like '"+scr+"%'";
                      else
                        myselect=myselect+" AND ban_citta_sport like '"+scr+"%'";
                  }
                }catch (Exception e){}
                //Controllo esistenza localita' sportello
                try{
                  scr=(String)(h.get("ban_localita_sport"));
                  if(!scr.equals("") && scr!=null) {
                      if (myselect.equals(""))
                        myselect=myselect+" ban_localita_sport like '"+scr+"%'";
                      else
                        myselect=myselect+" AND ban_localita_sport like '"+scr+"%'";
                  }
                }catch (Exception e){}
                //Controllo esistenza cap sportello
                try{
                  scr=(String)(h.get("ban_cap_sport"));
                  if(!scr.equals("") && scr!=null) {
                      if (myselect.equals(""))
                         myselect=myselect+" ban_cap_sport='"+scr+"'";
                      else
                         myselect=myselect+" AND ban_cap_sport='"+scr+"'";
                   }
                }catch (Exception e){}
                //Controllo esistenza descrizione sportello
                try{
                  scr=(String)(h.get("ban_descr_sport"));
                  if(!scr.equals("") && scr!=null) {
                      scr=duplicateChar(scr,"'");
                      if (myselect.equals(""))
                        myselect=myselect+" ban_descr_sport like '"+scr+"%'";
                      else
//gb 30.01.09                        myselect=myselect+" ban_descr_sport like '"+scr+"%'";
                        myselect=myselect+" AND ban_descr_sport like '"+scr+"%'"; //gb 30.01.09
                  }
                }catch (Exception e){}
                if(!myselect.equals(""))
		    myselect=" WHERE "+myselect;

                myselect=" SELECT * FROM banche "+myselect+
                         " ORDER BY ban_codice_abi,ban_cab_sport";
		System.out.println("QueryPaginate su banche: "+myselect);
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
	String abi=null;
	String cab=null;
	ISASConnection dbc=null;
	try {
		abi=(String)h.get("ban_codice_abi");
		cab=(String)h.get("ban_cab_sport");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("banche");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from banche where "+
			"ban_codice_abi='"+abi+"' and "+
			"ban_cab_sport='"+cab+"'";
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
	String abi=null;
	String cab=null;
	ISASConnection dbc=null;
	try {
		abi=(String)dbr.get("ban_codice_abi");
		cab=(String)dbr.get("ban_cab_sport");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from banche where "+
			"ban_codice_abi='"+abi+"' and "+
			"ban_cab_sport='"+cab+"'";
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

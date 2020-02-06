package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 06/08/2003 - EJB di connessione alla procedura SINS Tabella Conti
//
// Jessica Caccavale
///*bargi 24/08/2012 cambiaTO QUERY_PAGINATE CON DECODIFICA SU CO_COMUNI E ALTRI FILITRI
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

public class ContiEJB extends SINSSNTConnectionEJB  {
public ContiEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from co_economici where "+
                        "eco_anno='"+(String)h.get("eco_anno")+
			"' AND eco_codice='"+(String)h.get("eco_codice")+
                        "' AND eco_comune='"+(String)h.get("eco_comune")+"'";
		ISASRecord dbr=dbc.readRecord(myselect);
                System.out.println("QueryKey conti "+myselect);
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


/*bargi 24/08/2012 cambiaTA
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
                  scr=(String)(h.get("eco_descri"));
                  if(!scr.equals("") && scr!=null) {
                      scr=duplicateChar(scr,"'");
                      if (myselect.equals(""))
                        myselect=myselect+" eco_descri like '"+scr+"%'";
                      else
                        myselect=myselect+" eco_descri like '"+scr+"%'";
                  }
                }catch (Exception e){}
                if(!myselect.equals(""))
		    myselect=" WHERE "+myselect;

                myselect=" SELECT * FROM co_economici "+myselect+
                         " ORDER BY eco_anno,eco_codice,eco_comune,eco_descri";
		System.out.println("QueryPaginate su conti: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		//Vector vdbr=dbcur.getAllRecord();
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
                for(int i=0;i<vdbr.size()-1;i++)
                {
                  ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                  //preparazione fornitore
                  if (dbr.get("eco_comune")!=null && !((String)dbr.get("eco_comune")).equals(""))
                  {
                    String w_codice  = (String)dbr.get("eco_comune");
                    String w_select = "SELECT * FROM comuni WHERE codice='"+w_codice+"'";
                    ISASRecord w_dbr=dbc.readRecord(w_select);
                    dbr.put("eco_descri", w_dbr.get("descrizione"));
                  }else dbr.put("eco_descri", "");
                }//end for
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
*/
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
		  if(h.get("eco_descri")!=null){
                  	scr=(String)(h.get("eco_descri"));
                  	if(!scr.equals("") && scr!=null) {
                      		scr=duplicateChar(scr,"'");
                      	myselect=" eco_descri like '"+scr+"%'";
                  }
		 }
                }catch (Exception e){System.out.println("ContiEJB - Errore nella Paginate: "+e);}

		//Controllo esistenza anno
                try{
		  if (h.get("eco_anno")!=null){
	                  scr=(String)(h.get("eco_anno"));
        	          if(!scr.equals("") && scr!=null) {
                	      scr=duplicateChar(scr,"'");
	                      if (myselect.equals(""))
        	                myselect=myselect+" eco_anno = '"+scr+"'";
                	      else
                        	myselect=myselect+" AND eco_anno = '"+scr+"'";
                  	}
		  }
                }catch (Exception e){System.out.println("ContiEJB - Errore nella Paginate: "+e);}


		//Controllo esistenza comune
                try{
		  if(h.get("eco_comune")!=null){
	                  scr=(String)(h.get("eco_comune"));
        	          if(!scr.equals("") && scr!=null) {
                	      scr=duplicateChar(scr,"'");
	                      if (myselect.equals(""))
        	                myselect=myselect+" eco_comune = '"+scr+"'";
                	      else
                        	myselect=myselect+" AND eco_comune = '"+scr+"'";
	                  }
		  }
                }catch (Exception e){System.out.println("ContiEJB - Errore nella Paginate: "+e);}

                if(!myselect.equals(""))
		    myselect=" WHERE "+myselect;

                myselect=" SELECT * FROM co_economici "+myselect+
                         " ORDER BY eco_anno,eco_codice,eco_comune,eco_descri";
		System.out.println("QueryPaginate su conti: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		//Vector vdbr=dbcur.getAllRecord();
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
                for(int i=0;i<vdbr.size()-1;i++){
                  ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                  //preparazione fornitore
                  if (dbr.get("eco_comune")!=null && !((String)dbr.get("eco_comune")).equals(""))                  {
                    String w_codice  = (String)dbr.get("eco_comune");
                    //CJ 29/01/2008 indirizzata decodifica su nuova tabella co_comuni
		    //String w_select = "SELECT * FROM comuni WHERE codice='"+w_codice+"'";
		    String w_select = "SELECT * FROM co_comuni WHERE codice='"+w_codice+"'";
                    ISASRecord w_dbr=dbc.readRecord(w_select);
		    if(w_dbr!=null)
                    	dbr.put("desc_ccosto", w_dbr.get("descrizione"));
                  }else dbr.put("desc_ccosto", "");
                  if(dbr.get("eco_socsan")!=null)
                    if(((String)dbr.get("eco_socsan")).equals("0"))
                      dbr.put("descr_flag","Sociale");
                    else if(((String)dbr.get("eco_socsan")).equals("1"))
                      dbr.put("descr_flag","Sanitario");
                    else
                      dbr.put("descr_flag","");
                  else
                      dbr.put("descr_flag","");
//System.out.println("Prima della fine del for");

                }//end for
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
        String comune=null;
        String anno=null;
	ISASConnection dbc=null;
	try {
                anno=(String)h.get("eco_anno");
		codice=(String)h.get("eco_codice");
                comune=(String)h.get("eco_comune");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("co_economici");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
                System.out.println("dopo write!");
		String myselect="Select * from co_economici where "+
                        "eco_anno='"+anno+"' AND "+
			"eco_codice='"+codice+"' AND "+
                        "eco_comune='"+comune+"'";
		dbr=dbc.readRecord(myselect);
		System.out.println("dopo select:"+myselect);
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
        String comune=null;
        String anno=null;
	ISASConnection dbc=null;
	try {
                anno=(String)dbr.get("eco_anno");
		codice=(String)dbr.get("eco_codice");
                comune=(String)dbr.get("eco_comune");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from co_economici where "+
                        "eco_anno='"+anno+"' AND "+
			"eco_codice='"+codice+"' AND "+
                        "eco_comune='"+comune+"'";
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

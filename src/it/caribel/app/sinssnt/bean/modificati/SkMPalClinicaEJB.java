package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
//	28/08/07: 
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.util.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*; //gb 04/05/07
import it.pisa.caribel.sinssnt.connection.*;

public class SkMPalClinicaEJB extends SINSSNTConnectionEJB  {

	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();


public SkMPalClinicaEJB() {}

//Carlo Volpicelli - 15/01/2017
public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from skmpal_relcli where "+
                                " n_cartella="+(String)h.get("n_cartella")+                                
                                " AND skr_data="+formatDate(dbc,(String)h.get("skr_data"));                           
		System.out.println("QueryKey SkMPalClinicaEJB:"+myselect);
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

public Vector query_clinica(myLogin mylogin, Hashtable h) throws  SQLException
  {
    boolean done=false;
    ISASConnection dbc=null;
    try{

        dbc=super.logIn(mylogin);
        String selg="SELECT *" +
		" FROM skmpal_relcli" +
		" WHERE n_cartella = " + (String)h.get("n_cartella") +
                " ORDER BY skr_data";
	System.out.println("SkMPalClinicaEJB/queryKey: " + selg);
        ISASCursor dbgriglia=dbc.startCursor(selg);
        Vector vdbg=dbgriglia.getAllRecord();

	dbgriglia.close();
	dbc.close();
	super.close(dbc);
	done=true;
	return vdbg;
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

public void delete_clinica(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String sel_del = "SELECT *" +
				 " FROM skmpal_relcli" +
				 " WHERE n_cartella = " + (String)h.get("n_cartella") +
           		 " AND skr_data = " + formatDate(dbc,((String)h.get("skr_data")));
        	System.out.println("SkMPalClinicaEJB/delete_clinica: sel_del = " + sel_del);
		ISASRecord dbr=dbc.readRecord(sel_del);
                //System.out.println("Delete_eventi: "+sel_del);
		if (dbr!=null)
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


//Carlo Volpicelli - 15/01/2017
public void delete(myLogin mylogin,ISASRecord ir)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		Hashtable h = ir.getHashtable();
		dbc=super.logIn(mylogin);
		String sel_del = "SELECT *" +
				 " FROM skmpal_relcli" +
				 " WHERE n_cartella = " + h.get("n_cartella").toString() +
           		 " AND skr_data = " + formatDate(dbc,(h.get("skr_data").toString()));
        	System.out.println("SkMPalClinicaEJB/delete_clinica: sel_del = " + sel_del);
		ISASRecord dbr=dbc.readRecord(sel_del);
                //System.out.println("Delete_eventi: "+sel_del);
		if (dbr!=null)
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

public ISASRecord insert_clinica(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String n_cartella=null;
    String data=null;
    ISASConnection dbc=null;
    try {
        //System.out.println("h in isert==="+h.toString());
        n_cartella=(String)h.get("n_cartella");
        data=(String)h.get("skr_data");
    	}
    catch (Exception e){
        e.printStackTrace();
	throw new SQLException("SkMPalClinicaEJB/insert_clinica: Errore: manca la chiave primaria");
    	}
    try{
        dbc=super.logIn(mylogin);
        dbc.startTransaction();
        ISASRecord dbr=dbc.newRecord("skmpal_relcli");
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        String myselect="SELECT *" +
			" FROM skmpal_relcli" +
			" WHERE n_cartella = " + n_cartella +
			" AND skr_data = " + formatDate(dbc,data);
        System.out.println("SkMPalClinicaEJB/insert_clinica: select dopo l'inserimento: " + myselect);
        dbr=dbc.readRecord(myselect);
        dbc.commitTransaction();
        dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
    }catch(DBRecordChangedException e){
        e.printStackTrace();
        try{
            dbc.rollbackTransaction();
        }catch(Exception e1){
            throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
        }
        throw e;
    }catch(ISASPermissionDeniedException e){
        e.printStackTrace();
        try{
            dbc.rollbackTransaction();
        }catch(Exception e1){
            throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
        }
        throw e;
    }catch(Exception e1){
        System.out.println(e1);
        try{
            dbc.rollbackTransaction();
        }catch(Exception ex){
            throw new SQLException("Errore eseguendo una rollback() - "+  ex);
        }
        throw new SQLException("Errore eseguendo una insert_clinica() - "+  e1);
    }finally{
        if(!done){
            try{
            dbc.close();
	    super.close(dbc);
	    }catch(Exception e2){System.out.println(e2);}
        }
    }
}

public ISASRecord update_clinica(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String n_cartella=null;
    String skr_data=null;
    ISASConnection dbc=null;

    try {
//        System.out.println("***HASH "+h.toString());
        n_cartella=(String)h.get("n_cartella");
        skr_data=(String)h.get("skr_data");
    	}
    catch (Exception e){
        e.printStackTrace();
        throw new SQLException("SkMPalClinicaEJB/update_clinica: Errore: manca la chiave primaria");
    	}
    try{
        dbc=super.logIn(mylogin);
        dbc.startTransaction();
        String myselect="SELECT *" +
			" FROM skmpal_relcli" +
			" WHERE n_cartella = " + n_cartella +
			" AND skr_data = " + formatDate(dbc,skr_data);
        System.out.println("SkMPalClinicaEJB/update_clinica myselect = " + myselect);
        ISASRecord dbr=dbc.readRecord(myselect);
        if (dbr!=null){
          dbr.put("skr_nota",h.get("skr_nota"));
        }

        ISASRecord dbamb=dbc.newRecord("skmpal_relcli");
        Enumeration n=dbr.getHashtable().keys();
        while(n.hasMoreElements()){
                String e=(String)n.nextElement();
                dbamb.put(e,dbr.get(e));
        }

        dbc.deleteRecord(dbr);
        dbc.writeRecord(dbamb);
        String sel="SELECT *" +
		   " FROM skmpal_relcli" +
		   " WHERE n_cartella = " + n_cartella +
		   " AND skr_data = " + formatDate(dbc,skr_data);
        dbr=dbc.readRecord(sel);
        dbc.commitTransaction();
        dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
    }catch(DBRecordChangedException e){
        e.printStackTrace();
        try{
            dbc.rollbackTransaction();
        }catch(Exception e1){
            throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
        }
        throw e;
    }catch(ISASPermissionDeniedException e){
        e.printStackTrace();
        try{
            dbc.rollbackTransaction();
        }catch(Exception e1){
            throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
        }
        throw e;
    }catch(Exception e1){
        System.out.println(e1);
        try{
            dbc.rollbackTransaction();
        }catch(Exception ex){
            throw new SQLException("Errore eseguendo una rollback() - "+  ex);
        }
        throw new SQLException("Errore eseguendo una update_clinica() - "+  e1);
    }finally{
        if(!done){
            try{
            dbc.close();
	    super.close(dbc);
	    }catch(Exception e2){System.out.println(e2);}
        }
    }
}


//Carlo Volpicelli - 15/01/2017
public ISASRecord update(myLogin mylogin,ISASRecord ir) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String n_cartella=null;
	String skr_data=null;
	ISASConnection dbc=null;
	Hashtable h = ir.getHashtable();
	try {
		
		//        System.out.println("***HASH "+h.toString());
		n_cartella=h.get("n_cartella").toString();
		skr_data=h.get("skr_data").toString();
	}
	catch (Exception e){
		e.printStackTrace();
		throw new SQLException("SkMPalClinicaEJB/update_clinica: Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.startTransaction();
		String myselect="SELECT *" +
				" FROM skmpal_relcli" +
				" WHERE n_cartella = " + n_cartella +
				" AND skr_data = " + formatDate(dbc,skr_data);
		System.out.println("SkMPalClinicaEJB/update_clinica myselect = " + myselect);
		ISASRecord dbr=dbc.readRecord(myselect);
		if (dbr!=null){
			dbr.put("skr_nota",h.get("skr_nota"));
		}

		ISASRecord dbamb=dbc.newRecord("skmpal_relcli");
		Enumeration n=dbr.getHashtable().keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbamb.put(e,dbr.get(e));
		}

		dbc.deleteRecord(dbr);
		dbc.writeRecord(dbamb);
		String sel="SELECT *" +
				" FROM skmpal_relcli" +
				" WHERE n_cartella = " + n_cartella +
				" AND skr_data = " + formatDate(dbc,skr_data);
		dbr=dbc.readRecord(sel);
		dbc.commitTransaction();
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(Exception e1){
		System.out.println(e1);
		try{
			dbc.rollbackTransaction();
		}catch(Exception ex){
			throw new SQLException("Errore eseguendo una rollback() - "+  ex);
		}
		throw new SQLException("Errore eseguendo una update_clinica() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}//fine metodo

}

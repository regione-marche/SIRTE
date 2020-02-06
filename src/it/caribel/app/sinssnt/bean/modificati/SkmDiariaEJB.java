package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
//	28/08/07: 
// ==========================================================================

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
//gb 04/05/07

public class SkmDiariaEJB extends SINSSNTConnectionEJB  {

	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	private static final String ver = "2-";

public SkmDiariaEJB() {}

	public ISASRecord update_diaria(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "update_diaria ";
		LOG.debug(punto + " dati che esamino>>"+ (dbr!=null ? dbr.getHashtable()+"": " non dati "));
		Hashtable<String, Object>dati = convertiDati(dbr);
		return update_diaria(mylogin, dati);
	}
	
	public void delete_diaria(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
			String punto = ver + "delete_diaria ";
			LOG.debug(punto + " dati che esamino>>"+ (dbr!=null ? dbr.getHashtable()+"": " non dati "));
			Hashtable<String, Object>dati = convertiDati(dbr);
			delete_diaria(mylogin, dati);
	}

	private Hashtable<String, Object> convertiDati(ISASRecord dbr) {
		Hashtable<String, Object> datiConvertire = new Hashtable<String, Object>();
		datiConvertire.putAll(dbr.getHashtable());
		datiConvertire.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA));
		datiConvertire.put(CostantiSinssntW.N_CONTATTO, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CONTATTO));
		datiConvertire.put(CostantiSinssntW.SKD_DATA, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.SKD_DATA));
		
		return datiConvertire;
	}

	public ISASRecord queryKeyTerapia(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKeyTerapia ";
		ISASConnection dbc = null;
		ISASRecord dbrDiario = null;
		String query = "";
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		String skdData = ISASUtil.getValoreStringa(h, CostantiSinssntW.SKD_DATA);
		
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			dbc = super.logIn(mylogin);
			
			query = "SELECT *" + " FROM skmdiaria" + " WHERE n_cartella = " + nCartella + " AND n_contatto = " + nContatto
					+ " AND skd_data = " +formatDate(dbc, skdData);
			LOG.trace(punto + " query>>" + query);
			dbrDiario = dbc.readRecord(query);

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati del diario  ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrDiario;
	}

public Vector query_diaria(myLogin mylogin, Hashtable h) throws  SQLException
  {
    boolean done=false;
    ISASConnection dbc=null;
    try{

        dbc=super.logIn(mylogin);
        String selg="SELECT *" +
		" FROM skmdiaria" +
		" WHERE n_cartella = " + (String)h.get("n_cartella") +
                " AND n_contatto = " + (String)h.get("n_contatto") +
                " ORDER BY skd_data";
	System.out.println("SkmDiariaEJB/queryKey: " + selg);
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

public void delete_diaria(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String sel_del = "SELECT *" +
				 " FROM skmdiaria" +
				 " WHERE n_cartella = " + (String)h.get("n_cartella") +
				 " AND n_contatto = " + (String)h.get("n_contatto") +
           		 " AND skd_data = " + formatDate(dbc,((String)h.get("skd_data")));
        	System.out.println("SkmDiariaEJB/delete_diaria: sel_del = " + sel_del);
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

public ISASRecord insert_diaria(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	String punto = ver + "insert_diaria ";
    boolean done=false;
    String n_cartella=null;
    String n_contatto=null;
    String data=null;
    ISASConnection dbc=null;
    LOG.info(punto + " dati che ricevo>>"+ (h!=null ? h : " no dati "));
    try {
        n_cartella=(String)h.get("n_cartella");
        n_contatto=(String)h.get("n_contatto");
        data=(String)h.get("skd_data");
    	}
    catch (Exception e){
        e.printStackTrace();
	throw new SQLException("SkmDiariaEJB/insert_diaria: Errore: manca la chiave primaria");
    	}
    try{
        dbc=super.logIn(mylogin);
        dbc.startTransaction();
        ISASRecord dbr=dbc.newRecord("skmdiaria");
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        String myselect="SELECT *" +
			" FROM skmdiaria" +
			" WHERE n_cartella = " + n_cartella +
			" AND n_contatto = " + n_contatto +
			" AND skd_data = " + formatDate(dbc,data);
        LOG.trace(punto + " query>>" + myselect+"<< ");
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
        throw new SQLException("Errore eseguendo una insert_diaria() - "+  e1);
    }finally{
        if(!done){
            try{
            dbc.close();
	    super.close(dbc);
	    }catch(Exception e2){System.out.println(e2);}
        }
    }
}

public ISASRecord update_diaria(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String n_cartella=null;
    String n_contatto=null;
    String skd_data=null;
    ISASConnection dbc=null;

    try {
//        System.out.println("***HASH "+h.toString());
        n_cartella=(String)h.get("n_cartella");
        n_contatto=(String)h.get("n_contatto");
        skd_data=(String)h.get("skd_data");
    	}
    catch (Exception e){
        e.printStackTrace();
        throw new SQLException("SkmDiariaEJB/update_diaria: Errore: manca la chiave primaria");
    	}
    try{
        dbc=super.logIn(mylogin);
        dbc.startTransaction();
        String myselect="SELECT *" +
			" FROM skmdiaria" +
			" WHERE n_cartella = " + n_cartella +
            " AND n_contatto = " + n_contatto +
			" AND skd_data = " + formatDate(dbc,skd_data);
        System.out.println("SkmDiariaEJB/update_diaria myselect = " + myselect);
        ISASRecord dbr=dbc.readRecord(myselect);
        if (dbr!=null){
          dbr.put("skd_nota",h.get("skd_nota"));
        }

        ISASRecord dbamb=dbc.newRecord("skmdiaria");
        Enumeration n=dbr.getHashtable().keys();
        while(n.hasMoreElements()){
                String e=(String)n.nextElement();
                dbamb.put(e,dbr.get(e));
        }

        dbc.deleteRecord(dbr);
        dbc.writeRecord(dbamb);
        String sel="SELECT *" +
		   " FROM skmdiaria" +
		   " WHERE n_cartella = " + n_cartella +
		   " AND n_contatto = " + n_contatto +
		   " AND skd_data = " + formatDate(dbc,skd_data);
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
        throw new SQLException("Errore eseguendo una update_diaria() - "+  e1);
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

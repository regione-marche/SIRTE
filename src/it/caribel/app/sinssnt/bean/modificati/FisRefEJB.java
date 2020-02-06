package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 28/10/2002 - EJB di connessione alla procedura SINS Tabella FisRef
//
// Jessica Caccavale
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.ISASUtil;

public class FisRefEJB extends SINSSNTConnectionEJB  {
public FisRefEJB() {}
public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
	String punto = "update ";
	LOG.debug(punto + ">>dati che ricervo>>"+dbr+"<<<");
	Hashtable<String, String> dati = convertiDati(dbr);
	return salva(mylogin, dati);
}  

public void delete(myLogin myLogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
	String punto = " delete "; 
	LOG.debug(punto + "\n>>dati che ricervo per cancellazione >>"+dbr+"<<< \n");
	Hashtable<String, String> dati = convertiDati(dbr);
	
	delete_detta(myLogin, dati);
}
private Hashtable<String, String> convertiDati(ISASRecord dbr) {
	Hashtable<String, String> datiConvertire = new Hashtable<String, String>();
	datiConvertire.putAll(dbr.getHashtable());

	return datiConvertire;
}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	String punto = "skf_fisiot_da ";
	ISASConnection dbc=null;   
	ISASRecord dbr= null;    
	try{
		dbc=super.logIn(mylogin);
		String myselect = recuperaQuery(h, dbc);
		LOG.debug(punto + " query>>"+ myselect);
		dbr = dbc.readRecord(myselect);
		if (dbr!=null){
			recuperaDescrizioneZonaOperatore(dbc, new Hashtable<String, String>(), dbr);
		}
	}catch(Exception e){
		LOG.error(punto + " Errore ");
		throw new SQLException("Errore eseguendo una query()  ");
	}finally{
		logout_nothrow(punto, dbc);
	}
	return dbr;
}
private String recuperaQuery(Hashtable h, ISASConnection dbc) {
	String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
	String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
	String skfFiotDa = ISASUtil.getValoreStringa(h, "skf_fisiot_da");
	
	String myselect="Select * from skfis_referente where "+
		"n_cartella= "+nCartella+" AND "+ "n_contatto= "+nContatto+" and skf_fisiot_da = " + formatDate(dbc, skfFiotDa)+
	                " ORDER BY skf_fisiot_da ASC ";
	return myselect;
}

public ISASRecord query_massima(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        try{
		dbc=super.logIn(mylogin);
                String sel="SELECT r.* FROM skfis_referente r WHERE "+
		"r.n_cartella="+(String)h.get("n_cartella")+" and "+
		"r.n_contatto="+(String)h.get("n_contatto")+" and "+
                "r.skf_fisiot_da IN (SELECT MAX(skf_fisiot_da) FROM "+
                "skfis_referente s WHERE s.n_contatto=r.n_contatto "+
                "and s.n_cartella=r.n_cartella)";
                ISASRecord dbr=dbc.readRecord(sel);
                dbr.put("desc_infref",decodifica("operatori","codice",(String)dbr.get("skf_fisiot"),
                        "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",dbc));
		dbc.close();
		super.close(dbc);
		done=true;
                return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_massima()  ");
	}finally{
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
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from skfis_referente where "+
			"n_cartella= "+(String)h.get("n_cartella")+" AND "+
                        "n_contatto= "+(String)h.get("n_contatto")+
                        " ORDER BY skf_fisiot_da ASC ";
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		Hashtable zone = new Hashtable();
		if (vdbr!=null && vdbr.size()>0){
			zone = recuperaZone(dbc);
		}
				String descrizioneZona = "";
				String codZona ="";
                for (Enumeration enumk=vdbr.elements();enumk.hasMoreElements(); ){
                    ISASRecord dbr=(ISASRecord)enumk.nextElement();
                    if(dbr.get("skf_fisiot")!=null && !dbr.get("skf_fisiot").equals("")){
                      String mysel="SELECT * FROM operatori"+
                                   " WHERE codice='"+dbr.get("skf_fisiot")+"'";
                      ISASRecord dbrcomunires=dbc.readRecord(mysel);
                      codZona = ISASUtil.getValoreStringa(dbrcomunires, "cod_zona");
                      descrizioneZona = "";
                      if (dbrcomunires!=null){
                    	  dbr.put("descop",dbrcomunires.get("cognome")+" "+
                    			  	dbrcomunires.get("nome"));
                    	  descrizioneZona = ISASUtil.getValoreStringa(zone, codZona);
                      }else  
                    	  dbr.put("descop","");
                      
                      dbr.put("des_zona", descrizioneZona);
                    }
                }
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
private void recuperaDescrizioneZonaOperatore(ISASConnection dbc, Hashtable zone, ISASRecord dbr) throws ISASMisuseException,
ISASPermissionDeniedException, DBMisuseException, DBSQLException {
	String descrizioneZona;
	String codZona;
	if(dbr !=null && dbr.get("skf_fisiot")!=null && !dbr.get("skf_fisiot").equals("")){
		String mysel="SELECT * FROM operatori"+
				" WHERE codice='"+dbr.get("skf_fisiot")+"'";
		ISASRecord dbrsel=dbc.readRecord(mysel);
		descrizioneZona = "";
		codZona = ISASUtil.getValoreStringa(dbrsel, "cod_zona");
		if (dbrsel!=null){
			dbr.put("descop",dbrsel.get("cognome")+" "+
					dbrsel.get("nome"));
			descrizioneZona = ISASUtil.getValoreStringa(zone, codZona);
		}else
			dbr.put("descop","");
		dbr.put("des_zona", descrizioneZona);
	}
}

private Hashtable recuperaZone(ISASConnection dbc) {
	Hashtable zone = new Hashtable();
	String query = "select * from zone ";
	ISASCursor dbrCursor = null;
	try {
		dbrCursor = dbc.startCursor(query);
		while (dbrCursor.next()) {
			ISASRecord dbrZone= (ISASRecord) dbrCursor.getRecord();
			String codZona = ISASUtil.getValoreStringa(dbrZone, "codice_zona");
			String descZona =ISASUtil.getValoreStringa(dbrZone, "descrizione_zona");
			zone.put(codZona, descZona);
		}
		dbrCursor.close();
	} catch (DBSQLException e) {
		e.printStackTrace();
	} catch (DBMisuseException e) {
		e.printStackTrace();
	} catch (ISASMisuseException e) {
		e.printStackTrace();
	} catch (ISASPermissionDeniedException e) {
		e.printStackTrace();
	}finally{
		try {
			if(dbrCursor!=null){
				dbrCursor.close();
			}
		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		}		
	}
	return zone;
}


public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;
	String contatto=null;
	String data2=null;
        String infermiere=null;
        String data_da=null;
	ISASConnection dbc=null;
        try {
		dbc=super.logIn(mylogin);
		cartella=(String)h.get("n_cartella");
		contatto=(String)h.get("n_contatto");
		data2=(String)h.get("skf_fisiot_da");
                infermiere=(String)h.get("skf_fisiot");
                data_da=(String)h.get("skf_fisiot_da");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		ISASRecord dbr=dbc.newRecord("skfis_referente");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from skfis_referente where "+
			"n_cartella="+cartella+" and "+
                        "n_contatto="+contatto+" and "+
                        "skf_fisiot_da="+formatDate(dbc,data2);
                //System.out.println("Select insert: "+myselect);
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




public ISASRecord salva(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;
	String contatto=null;
	String data2=null;
        String infermiere=null;
        String data_da=null;
	ISASConnection dbc=null;
	try {
		dbc=super.logIn(mylogin);
                //System.out.println("HASH "+h.toString());
		cartella=(String)h.get("n_cartella");
		contatto=(String)h.get("n_contatto");
		data2=(String)h.get("data_vecchia");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
                String myselect="Select * from skfis_referente where "+
			"n_cartella="+cartella+" and "+
                        "n_contatto="+contatto+" and "+
                        "skf_fisiot_da="+formatDate(dbc,data2);
		ISASRecord dbr=dbc.readRecord(myselect);
                infermiere=(String)h.get("skf_fisiot");
                data_da=(String)h.get("skf_fisiot_da");
                if (dbr!=null){
                  dbr.put("n_cartella",cartella);
                  dbr.put("n_contatto",contatto);
                  dbr.put("skf_fisiot_da",h.get("skf_fisiot_da"));
                  dbr.put("skf_fisiot",h.get("skf_fisiot"));
		}
                dbc.writeRecord(dbr);
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


public void delete_detta(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	String punto= "delete_detta ";
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String sel_del = recuperaQuery(h, dbc);
		LOG.trace(punto + " query>>" +sel_del);
		ISASRecord dbr=dbc.readRecord(sel_del);
        if (dbr!=null){
        	dbc.deleteRecord(dbr);
        }
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


private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		return ((String)dbtxt.get("descrizione"));
	} catch (Exception ex) {
		return " ";
	}
}


}

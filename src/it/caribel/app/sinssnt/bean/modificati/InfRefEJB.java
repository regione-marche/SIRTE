package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 28/10/2002 - EJB di connessione alla procedura SINS Tabella InfRef
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
import it.pisa.caribel.util.ISASUtil;


public class InfRefEJB extends SINSSNTConnectionEJB  {


public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
	Hashtable<String, Object> dati = convertiDati(dbr);
	return salva(mylogin, dati);
}  

public void delete(myLogin myLogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
	Hashtable<String, Object> dati = convertiDati(dbr);
	
	delete_detta(myLogin, dati);
}
private Hashtable<String, Object> convertiDati(ISASRecord dbr) {
	Hashtable<String, Object> datiConvertire = new Hashtable<String, Object>();
	datiConvertire.putAll(dbr.getHashtable());
	datiConvertire.put("n_cartella", ISASUtil.getValoreStringa(dbr, "n_cartella"));
	datiConvertire.put("n_contatto", ISASUtil.getValoreStringa(dbr, "n_contatto"));
	datiConvertire.put("skir_infermiere_da", ISASUtil.getValoreStringa(dbr, "skir_infermiere_da"));
	
	return datiConvertire;
}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	String punto = "skir_infermiere_da ";
	ISASConnection dbc=null;   
	ISASRecord dbr= null;    
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from skinf_referente where "+
			"n_cartella= "+(String)h.get("n_cartella")+" AND "+
                        "n_contatto= "+(String)h.get("n_contatto")+
                        " and skir_infermiere_da = " + formatDate(dbc, h.get("skir_infermiere_da")+"")+
                        " ORDER BY skir_infermiere_da ASC ";
		LOG.debug(punto + " query>>"+ myselect);
		dbr = dbc.readRecord(myselect);
        recuperaDescrizioneZonaOperatore(dbc, new Hashtable<String, String>(), dbr);
	}catch(Exception e){
		LOG.error(punto + " Errore ");
		throw new SQLException("Errore eseguendo una query()  ");
	}finally{
		logout_nothrow(punto, dbc);
	}
	return dbr;
}

public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from skinf_referente where "+
			"n_cartella= "+(String)h.get("n_cartella")+" AND "+
                        "n_contatto= "+(String)h.get("n_contatto")+
                        " ORDER BY skir_infermiere_da ASC ";
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		Hashtable zone =new Hashtable();
		if (vdbr.size()>0){
			zone = recuperaZone(dbc);
		}
		String descrizioneZona = ""; 
		String codZona = "";
		//System.out.println("Voglio vedere la select****"+myselect);
                for (Enumeration enumk=vdbr.elements();enumk.hasMoreElements(); ){
                    ISASRecord dbr=(ISASRecord)enumk.nextElement();
                    recuperaDescrizioneZonaOperatore(dbc, zone, dbr);
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
	String skmMedico = ISASUtil.getValoreStringa(dbr, "skir_infermiere");
	if(ISASUtil.valida(skmMedico)){
	  String mysel="SELECT * FROM operatori"+
	               " WHERE codice='"+dbr.get("skir_infermiere")+"'";
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
	String punto = "recuperaZone ";
	Hashtable zone = new Hashtable();
	String query = "select * from zone ";
	ISASCursor dbrCursor = null;
	try {
		dbrCursor = dbc.startCursor(query);
		while (dbrCursor.next()) {
			ISASRecord dbrZone= (ISASRecord) dbrCursor.getRecord();
			String codZona = ISASUtil.getValoreStringa(dbrZone, "codice_zona");
			String descZona = ISASUtil.getValoreStringa(dbrZone, "descrizione_zona");
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

public ISASRecord query_massima(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        try{
		dbc=super.logIn(mylogin);
                String sel="SELECT r.* FROM skinf_referente r WHERE "+
		"r.n_cartella="+(String)h.get("n_cartella")+" and "+
		"r.n_contatto="+(String)h.get("n_contatto")+" and "+
                "r.skir_infermiere_da IN (SELECT MAX(skir_infermiere_da) FROM "+
                "skinf_referente s WHERE s.n_contatto=r.n_contatto "+
                "and s.n_cartella=r.n_cartella)";
                ISASRecord dbr=dbc.readRecord(sel);
                dbr.put("desc_infref",decodifica("operatori","codice",(String)dbr.get("skir_infermiere"),
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

public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;
	String contatto=null;
        String infermiere=null;
        String data_da=null;
	ISASConnection dbc=null;
        try {
		dbc=super.logIn(mylogin);
		cartella=(String)h.get("n_cartella");
		contatto=(String)h.get("n_contatto");
                infermiere=(String)h.get("skir_infermiere");
         data_da = (String)h.get("skir_infermiere_da");
	}catch (Exception e){
		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		ISASRecord dbr=dbc.newRecord("skinf_referente");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from skinf_referente where "+
			"n_cartella="+cartella+" and "+
                        "n_contatto="+contatto+" and "+
                        "skir_infermiere_da="+formatDate(dbc,data_da.toString());
                //System.out.println("Select insert: "+myselect);
		dbr=dbc.readRecord(myselect);
                //this.updateSkInf(dbc,data1,data_da,infermiere,cartella,contatto);
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
	String data_da=null;
        String infermiere=null;
	ISASConnection dbc=null;
	try {
		dbc=super.logIn(mylogin);
                //System.out.println("HASH "+h.toString());
		cartella=(String)h.get("n_cartella");
		contatto=(String)h.get("n_contatto");
		data_da=(String)h.get("data_vecchia");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
                //System.out.println("Prima di select ");
		String myselect="Select * from skinf_referente where "+
			"n_cartella="+cartella+" and "+
                        "n_contatto="+contatto+" and "+
                        "skir_infermiere_da="+formatDate(dbc,data_da);
		ISASRecord dbr=dbc.readRecord(myselect);
                infermiere=(String)h.get("skir_infermiere");
                data_da=(String)h.get("skir_infermiere_da");
                //System.out.println("***Dopo select in insert"+myselect);
		if (dbr!=null){
                  dbr.put("n_cartella",cartella);
                  dbr.put("n_contatto",contatto);
                  dbr.put("skir_infermiere_da",h.get("skir_infermiere_da"));
                  dbr.put("skir_infermiere",infermiere);
		}
                dbc.writeRecord(dbr);
                //System.out.println("***SCRITTO ");
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
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String sel_del="SELECT * FROM skinf_referente WHERE"+
		 " n_cartella="+(String)h.get("n_cartella")+
		 " AND n_contatto="+(String)h.get("n_contatto")+
                 " AND skir_infermiere_da="+formatDate(dbc,((String)h.get("skir_infermiere_da")));
		ISASRecord dbr=dbc.readRecord(sel_del);
                //System.out.println("Delete_terapia: "+sel_del);
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
	
	
	
	
	



private ISASRecord updateSkInf(ISASConnection dbc,String data1,String data_da,String infermiere,String cartella,String contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    try{

        String myselect="Select * from skinf where "+
			" n_cartella="+cartella+
                        " and n_contatto="+contatto+
                        " and ski_data_apertura="+formatDate(dbc,data1);
        //System.out.println("select prima di aggiornamento su skinf da infref==="+myselect);
        ISASRecord dbr=dbc.readRecord(myselect);
        dbr.put("ski_infermiere",infermiere);
        dbr.put("ski_infermiere_da",data_da);
        //System.out.println("sono nell'update SkInf");
        dbc.writeRecord(dbr);
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


}

package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 20/05/2004 - EJB di connessione alla procedura SINS Tabella MedPalRef
//
// Jessica Caccavale
//
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.util.Date;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.ISASUtil;

public class MedPalRefEJB extends SINSSNTConnectionEJB  
{
	private static final String MIONOME = "1-MedPalRefEJB ";

	public MedPalRefEJB() {}

	//Carlo Volpicelli - 22/02/2017
	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skmedpal_referente where "+
					" n_cartella="+(String)h.get("n_cartella")+
					" AND n_contatto = " + ISASUtil.getValoreStringa(h, "n_contatto")+
//					" AND skm_medico_da="+(String)h.get("skm_medico_da");
			" AND skm_medico_da='"+getDataFormatoOracle((String)h.get("skm_medico_da"))+"'";

			System.out.println("QueryKey MedPalRefEJB:"+myselect);
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

	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
		String punto = MIONOME  + "query ";
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skmedpal_referente where "+
					"n_cartella= "+(String)h.get("n_cartella")+" AND "+
					"n_contatto= "+(String)h.get("n_contatto")+
					" ORDER BY skm_medico_da ASC ";
			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			Hashtable zone = new Hashtable();
			if (vdbr!=null && vdbr.size()>0){
				zone = recuperaZone(dbc);
			}
			stampa(punto + " ZONE RECUPERATE>"+zone);   
			String codZone = "";
			String descrizioneZona = "";
			//System.out.println("Voglio vedere la select****"+myselect);
			for (Enumeration enume=vdbr.elements();enume.hasMoreElements(); ){
				ISASRecord dbr=(ISASRecord)enume.nextElement();
				if(dbr.get("skm_medico")!=null && !dbr.get("skm_medico").equals("")){
					String mysel="SELECT * FROM operatori"+
							" WHERE codice='"+dbr.get("skm_medico")+"'";
					ISASRecord dbrsel=dbc.readRecord(mysel);
					descrizioneZona = "";
					codZone = ISASUtil.getValoreStringa(dbrsel, "cod_zona");
					if (dbrsel!=null){
						dbr.put("descop",dbrsel.get("cognome")+" "+
								dbrsel.get("nome"));
						descrizioneZona = ISASUtil.getValoreStringa(zone, codZone);
					}else{
						dbr.put("descop","");
					}
					dbr.put("zona_operatore", descrizioneZona);
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

	private void stampa(String messaggio) {
		System.out.println(messaggio);

	}
	private Hashtable recuperaZone(ISASConnection dbc) {
		String punto = "recuperaZone ";
		Hashtable zone = new Hashtable();
		String query = "select * from zone ";
		//	codice_zona, descrizione_zona 
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

	public ISASRecord query_massima(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String sel="SELECT r.* FROM skmedpal_referente r WHERE "+
					"r.n_cartella="+(String)h.get("n_cartella")+" and "+
					"r.n_contatto="+(String)h.get("n_contatto")+" and "+
					"r.skm_medico_da IN (SELECT MAX(skm_medico_da) FROM "+
					"skmedpal_referente s WHERE s.n_contatto=r.n_contatto "+
					"and s.n_cartella=r.n_cartella)";
			ISASRecord dbr=dbc.readRecord(sel);
			dbr.put("desc_infref",decodifica("operatori","codice",(String)dbr.get("skm_medico"),
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
			infermiere=(String)h.get("skm_medico");
			data_da=(String)h.get("skm_medico_da");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			ISASRecord dbr=dbc.newRecord("skmedpal_referente");
			Enumeration n=h.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbc.writeRecord(dbr);
			String myselect="Select * from skmedpal_referente where "+
					"n_cartella="+cartella+" and "+
					"n_contatto="+contatto+" and "+
					"skm_medico_da="+formatDate(dbc,data_da);
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
			String myselect="Select * from skmedpal_referente where "+
					"n_cartella="+cartella+" and "+
					"n_contatto="+contatto+" and "+
					"skm_medico_da="+formatDate(dbc,data_da);
			ISASRecord dbr=dbc.readRecord(myselect);
			infermiere=(String)h.get("skm_medico");
			data_da=(String)h.get("skm_medico_da");
			//System.out.println("***Dopo select in insert"+myselect);
			if (dbr!=null){
				dbr.put("n_cartella",cartella);
				dbr.put("n_contatto",contatto);
				dbr.put("skm_medico_da",h.get("skm_medico_da"));
				dbr.put("skm_medico",infermiere);
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
			String sel_del="SELECT * FROM skmedpal_referente WHERE"+
					" n_cartella="+(String)h.get("n_cartella")+
					" AND n_contatto="+(String)h.get("n_contatto")+
					" AND skm_medico_da="+formatDate(dbc,((String)h.get("skm_medico_da")));
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
	
	//Carlo Volpicelli - 22/02/2017
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
	
	//Carlo Volpicelli - 23/02/2017
	public ISASRecord update(myLogin mylogin,ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		
		String n_cartella=null;
		String n_contatto=null;
		String skm_medico_da=null;
		
		ISASConnection dbc=null;
		try {
			n_cartella=dbr.get("n_cartella").toString();
			n_contatto=""+dbr.get("n_contatto").toString();
			//skm_medico_da=""+dbr.get("skm_medico_da").toString();
			skm_medico_da=getDataFormatoOracle(dbr.get("skm_medico_da").toString());
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.writeRecord(dbr);
			String myselect="SELECT * FROM skmedpal_referente WHERE "+
					"n_cartella="+n_cartella+
					" AND n_contatto="+n_contatto+" " +
					" AND skm_medico_da='"+skm_medico_da+"'";
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
	
	
	//Carlo Volpicelli - questo metodo riceve una stringa del tipo 2016-12-31 e restituisce una stringa 
	//di tipo 31-DIC-2016
	private String getDataFormatoOracle(String data)
	{
		String risultato = null;

		String dd = data.substring(8, 10);
		String meseNumerico = data.substring(5, 7);
		String meseLetterale = "GEN";
		String yyyy = data.substring(0, 4);

		if(meseNumerico.equals("02"))
		{
			meseLetterale = "FEB";
		}
		else if(meseNumerico.equals("03"))
		{
			meseLetterale = "MAR";
		}
		else if(meseNumerico.equals("04"))
		{
			meseLetterale = "APR";
		}
		else if(meseNumerico.equals("05"))
		{
			meseLetterale = "MAG";
		}
		else if(meseNumerico.equals("06"))
		{
			meseLetterale = "GIU";
		}
		else if(meseNumerico.equals("07"))
		{
			meseLetterale = "LUG";
		}
		else if(meseNumerico.equals("08"))
		{
			meseLetterale = "AGO";
		}
		else if(meseNumerico.equals("09"))
		{
			meseLetterale = "SET";
		}
		else if(meseNumerico.equals("10"))
		{
			meseLetterale = "OTT";
		}
		else if(meseNumerico.equals("11"))
		{
			meseLetterale = "NOV";
		}
		else if(meseNumerico.equals("12"))
		{
			meseLetterale = "DIC";
		}

		risultato = dd+"-"+meseLetterale+"-"+yyyy;
		return risultato;
	}

	/* --------------------------------------------------------------------------------------------------------------------- */

}

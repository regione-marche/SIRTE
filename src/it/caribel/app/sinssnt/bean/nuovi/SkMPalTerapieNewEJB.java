package it.caribel.app.sinssnt.bean.nuovi;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// Terapia
//
// ==========================================================================

import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.ISASUtil;

public class SkMPalTerapieNewEJB extends SINSSNTConnectionEJB  
{
	public SkMPalTerapieNewEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skmpal_terapie_new where "+
					" n_cartella="+(String)h.get("n_cartella")+ " AND n_contatto="+(String)h.get("n_contatto")+                               
					" AND id_terapia="+(String)h.get("id_terapia");
			System.out.println("QueryKey SkMPalTerapieNewEJB:"+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			decodificaInformazioni(dbc, dbr);
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

	// 25/09/07: legge l'ultimo rec (per data e progr) e vi aggiunge gli isas record per la griglia
	public ISASRecord queryKeyLast(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		try{
			dbc=super.logIn(mylogin);

			String mysel = "SELECT * FROM skmpal_terapie_new" +
					" WHERE n_cartella = " + (String)h.get("n_cartella");								

			String myselCur = mysel + " ORDER BY data_inizio DESC, id_terapia DESC";

			System.out.println("SkMPalTerapieNewEJB: QueryKeyLast - myselCur=["+myselCur+"]");
			ISASCursor dbcur = dbc.startCursor(myselCur);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			if ((vdbr != null) && (vdbr.size() > 0)){
				ISASRecord dbr_1 = (ISASRecord)vdbr.firstElement();
				String myselRec = mysel + " AND id_terapia = " + dbr_1.get("id_terapia");
				System.out.println("SkMPalTerapieNewEJB: QueryKeyLast - myselRec=["+myselRec+"]");

				dbr = dbc.readRecord(myselRec);					
				if (dbr != null)
					dbr.put("griglia", (Vector)vdbr);
			}

			dbc.close();
			super.close(dbc);
			done=true;

			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKeyLast()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	//	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException 
	//	{
	//		boolean done=false;
	//		ISASConnection dbc=null;
	//
	//		try{
	//			dbc=super.logIn(mylogin);
	//			String myselect="SELECT * FROM skmpal_terapie_new WHERE "+
	//					"n_cartella="+(String)h.get("n_cartella")+" AND n_contatto="+(String)h.get("n_contatto");
	//			myselect=myselect+" ORDER BY data_inizio DESC, id_terapia DESC";
	//
	//			System.out.println("query skmpal_terapie_new: "+myselect);
	//			ISASCursor dbcur=dbc.startCursor(myselect);
	//			Vector vdbr=dbcur.getAllRecord();
	//			
	//			
	//			for (Enumeration enume=vdbr.elements();enume.hasMoreElements(); ){
	//				ISASRecord dbr=(ISASRecord)enume.nextElement();
	//				if(dbr.get("mecodi")!=null && !dbr.get("mecodi").equals("")){
	//					String mysel="SELECT * FROM medici"+
	//							" WHERE mecodi='"+dbr.get("mecodi")+"'";
	//					ISASRecord dbrsel=dbc.readRecord(mysel);
	//					if (dbrsel!=null){
	//						dbr.put("mecodi_desc",dbrsel.get("mecogn")+" "+
	//								dbrsel.get("menome"));
	//					}else{
	//						dbr.put("mecodi_desc","");
	//					}
	//				}				
	//				
	//				if(dbr.get("sf_codice")!=null && !dbr.get("sf_codice").equals("")){
	//					String mysel="SELECT * FROM farmaci"+
	//							" WHERE sf_codice='"+dbr.get("sf_codice")+"'";
	//					ISASRecord dbrsel=dbc.readRecord(mysel);
	//					if (dbrsel!=null){
	//						dbr.put("sf_codice_desc",dbrsel.get("sf_descrizione"));
	//					}else{
	//						dbr.put("sf_codice_desc","");
	//					}
	//				}
	//				
	//				if(dbr.get("cod_operatore")!=null && !dbr.get("cod_operatore").equals("")){
	//					String mysel="SELECT * FROM operatori"+
	//							" WHERE codice='"+dbr.get("cod_operatore")+"'";
	//					ISASRecord dbrsel=dbc.readRecord(mysel);
	//					if (dbrsel!=null){
	//						dbr.put("cod_operatore_desc",dbrsel.get("cognome")+" "+dbrsel.get("nome"));
	//					}else{
	//						dbr.put("sf_codice_desc","");
	//					}
	//				}
	//			}
	//			
	//			
	//			dbcur.close();
	//			dbc.close();
	//			super.close(dbc);
	//			done=true;
	//			return vdbr;
	//		}catch(Exception e){
	//			e.printStackTrace();
	//			throw new SQLException("Errore eseguendo una query()  ");
	//		}finally{
	//			if(!done){
	//				try{
	//					dbc.close();
	//					super.close(dbc);
	//				}catch(Exception e1){System.out.println(e1);}
	//			}
	//		}
	//	}

	public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		String n_cartella=null;
		int idTerapia=0;
		ISASConnection dbc=null;
		try {
			n_cartella=(String)h.get("n_cartella");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkMPalTerapieNew insert: Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			ISASRecord dbr=dbc.newRecord("skmpal_terapie_new");
			//INSERISCO NELLA TABELLA SKMPAL_TERAPIE_NEW
			/*Mi vado a calcolare il progressivo*/
			String selProg = "SELECT MAX(id_terapia) progr FROM skmpal_terapie_new WHERE "+
					"n_cartella="+n_cartella;
			ISASRecord dbrProg = dbc.readRecord(selProg);
			if(dbrProg.get("progr")!=null){
				System.out.println("Dentro l'if");
				idTerapia=Integer.parseInt(""+dbrProg.get("progr"));
				System.out.println("Progr:"+idTerapia);
			}
			idTerapia++;
			Enumeration n=h.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbr.put("id_terapia", ""+idTerapia);

			System.out.println("Ho scritto il record:"+dbr.getHashtable().toString());
			dbc.writeRecord(dbr);
			String myselect="SELECT * FROM skmpal_terapie_new WHERE n_cartella="+n_cartella+
					" AND id_terapia="+idTerapia;
			dbr=dbc.readRecord(myselect);
			System.out.println("select skmpal_terapie_new insert: "+myselect);

			// 25/09/07
			if (dbr != null)
				dbr.put("griglia", (Vector)leggiTuttiRec(dbc, n_cartella));

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			System.out.println("select skmpal_terapie_new insert: "+dbr.getHashtable().toString());
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
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
			{
		boolean done=false;
		String cartella=null;

		String idTerapia=null;
		ISASConnection dbc=null;

		try 
		{
			cartella=dbr.get("n_cartella").toString();
			idTerapia=""+dbr.get("id_terapia");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.writeRecord(dbr);
			String myselect="SELECT * FROM skmpal_terapie_new WHERE "+
					"n_cartella="+cartella+
					" AND id_terapia="+idTerapia;
			dbr=dbc.readRecord(myselect);

			// 25/09/07
			if (dbr != null)
				dbr.put("griglia", (Vector)leggiTuttiRec(dbc, cartella));

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
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
			{
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


	// 25/09/07
	private Vector leggiTuttiRec(ISASConnection mydbc, String cart) throws Exception
	{
		String myselect = "SELECT * FROM skmpal_terapie_new" +
				" WHERE n_cartella = " + cart +
				" ORDER BY data_inizio DESC, id_terapia DESC";

		ISASCursor dbcur = mydbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		dbcur.close();
		return vdbr;
	}


	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h) throws Exception {
		String punto = " query ";
		//LOG.info(punto + " inizio con dati" + h);
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = null;
		String nCartella = ISASUtil.getValoreStringa(h, "n_cartella");
		String nContatto = ISASUtil.getValoreStringa(h, "n_contatto");
		String statoTerapia = ISASUtil.getValoreStringa(h, "stato_terapia");
		String da_data = ISASUtil.getValoreStringa(h, "da_data");
		String a_data = ISASUtil.getValoreStringa(h, "a_data");

		try {
			dbc = super.logIn(mylogin);

			vdbr = recuperaRsaTerapie(dbc, nCartella, nContatto, false, null,statoTerapia,da_data,a_data);
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbrRC = (ISASRecord) vdbr.get(i);
				decodificaInformazioni(dbc, dbrRC);
			}

		} catch (Exception e) {
			LOG.info(punto + " ERRORE query " + e);
			throw e;
		} finally {
			logout_nothrow(punto, dbcur, dbc);
		}
		return vdbr;
	}


	public Vector<ISASRecord> recuperaRsaTerapie(ISASConnection dbc, String nCartella, String nContatto, 
			boolean isTterapiaValida,String tipoPrescrizione,String statoTerapia,String da_data,String a_data) throws Exception {
		String punto = "recuperaRsaTerapie ";
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = null;
		String query = queryKey(dbc, nCartella, nContatto, -1, false, isTterapiaValida, tipoPrescrizione,statoTerapia,da_data,a_data);
		//LOG.trace(punto +  " query>" +query);
		try {
			dbcur = dbc.startCursor(query);
			vdbr = dbcur.getAllRecord();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}finally{
			close_dbcur_nothrow(punto, dbcur);
		}

		LOG.trace(punto +  " elementi recuperati>" +(vdbr!=null ? vdbr.size()+"": " no dati "));
		return vdbr;
	}


	private String queryKey(ISASConnection dbc, String nCartella, String nContatto, int idTerapia, boolean isQueryKey,
			boolean isTterapiaValida, String tipoPrescrizione, String statoTerapia,String da_data,String a_data) {
		//String punto = ver + "queryKey ";
		StringBuffer query = new StringBuffer();

		query.append("select * from skmpal_terapie_new where n_cartella =");
		query.append(nCartella);
		query.append(" and n_contatto = ");
		query.append(nContatto);
		if (isQueryKey ) {
			query.append(" and id_terapia = ");
			query.append(idTerapia);
		}
		if (isTterapiaValida){
			query.append(" and ( ( data_fine is null ) or  ( data_fine is not null and data_fine >= sysdate ) ) ");
		}

		if (ISASUtil.valida(statoTerapia) && (!statoTerapia.equals("ALL_VALUE"))){
			if(statoTerapia.equals("ATTIVE")){
				query.append(" and (data_fine is null or TRUNC(data_fine)>=TRUNC(sysdate))");
			}else if(statoTerapia.equals("CONCLUSE")){
				query.append(" and TRUNC(data_fine)<TRUNC(sysdate)");
			}	
		}

		if (ManagerDate.validaData(da_data)) {
			query.append(" AND  (data_fine is null or data_fine >= " + dbc.formatDbDate(da_data)+")");
		}

		if (ManagerDate.validaData(a_data)) {
			query.append(" AND data_inizio <= " + dbc.formatDbDate(a_data));
		}

		query.append(" order by data_inizio ");
		//LOG.debug(punto + " query>" + query);
		return query.toString();
	}


	private void decodificaInformazioni(ISASConnection dbc, ISASRecord dbrRsaTerapia)
			throws Exception {
		if (dbrRsaTerapia != null) 
		{
			String codOperatore = ISASUtil.getValoreStringa(dbrRsaTerapia, Costanti.COD_OPERATORE);
			String cognomeNome = "";
			if (ISASUtil.valida(codOperatore)){
				cognomeNome = ISASUtil.getDecode(dbc, "operatori", "codice", codOperatore, 
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')", "desc_op");
			}
			dbrRsaTerapia.put(Costanti.COD_OPERATORE+"_desc", cognomeNome);

			String mecodi =ISASUtil.getValoreStringa(dbrRsaTerapia, Costanti.MECODI);
			String medico = "";
			if (ISASUtil.valida(mecodi)){
				medico = ISASUtil.getDecode(dbc, "medici", "mecodi", mecodi, 
						"nvl(trim(mecogn),'') ||' '  ||nvl(trim(menome),'')", "medico");
			}
			dbrRsaTerapia.put(Costanti.MECODI+"_desc", medico);

			String farmacoCod = ISASUtil.getValoreStringa(dbrRsaTerapia, Costanti.FARMACO_COD);
			String farmaco="";
			if (ISASUtil.valida(farmacoCod)){
				farmaco=  ISASUtil.getDecode(dbc, "farmaci", "sf_codice", farmacoCod, 
						"nvl(trim(sf_descrizione),'')", "farmaco");
			}
			dbrRsaTerapia.put(Costanti.FARMACO_COD+"_desc", farmaco);
		}
	}


}
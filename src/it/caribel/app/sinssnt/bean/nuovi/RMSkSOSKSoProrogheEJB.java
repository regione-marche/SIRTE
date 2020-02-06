package it.caribel.app.sinssnt.bean.nuovi;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 30/09/2014 - //     scrittura nella tabella rm_skso_proroghe
//
// ==========================================================================

import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;

public class RMSkSOSKSoProrogheEJB extends RMSkSOSKSoProrogheBaseEJB {
	public RMSkSOSKSoProrogheEJB() {
	}

	private String ver = "1-";

//	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
//		ISASConnection dbc = null;
//		String punto = ver + "query ";
//		ISASCursor dbcur = null;
//		Vector vdbr = new Vector();
//		try {
//			dbc = super.logIn(mylogin);
//			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//			String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//			String myselect = recuperaQueryKey(nCartella, idSkSo, "", false);
//			LOG.debug(punto + " query>>" + myselect);
//			dbcur = dbc.startCursor(myselect);
//			vdbr = dbcur.getAllRecord();
//
//		
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new SQLException("Errore eseguendo una query()  ", e);
//		} finally {
//			logout_nothrow(punto, dbcur, dbc);
//		}
//		return vdbr;
//	}
	
//	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException {
//		String punto = ver + "queryKey ";
//		ISASConnection dbc = null;
//		ISASRecord dbrProroghe = null;
//		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//		String idProroga = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SO_ID_PROROGA);
//
//		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
//		try {
//			if (ISASUtil.valida(idProroga)) {
//				dbc = super.logIn(mylogin);
//				String myselect = recuperaQueryKey(nCartella, idSkSo, idProroga, true);
//				LOG.trace(punto + " query>>" + myselect);
//				dbrProroghe = dbc.readRecord(myselect);
//			}
//
//		} catch (Exception e) {
//			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return dbrProroghe;
//	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ISASRecord insert(myLogin mylogin, Hashtable dati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "insert ";
		String nCartella = null;
		String idSkSo = null;
		ISASConnection dbc = null;
		ISASRecord dbrInsert = null;
		boolean done = false;
		PianoAssistEJB paEjb = new PianoAssistEJB();

		try {
			nCartella = ISASUtil.getValoreStringa(dati, "n_cartella");
			idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		} catch (Exception e) {
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbrInsert = dbc.newRecord("rm_skso_proroghe");
			int idProroga = getSelectProgressivo(dbc, nCartella);
			Enumeration n = dati.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbrInsert.put(e, dati.get(e));
			}
			dbrInsert.put(CostantiSinssntW.CTS_SO_ID_PROROGA, idProroga);
			dbc.writeRecord(dbrInsert);
			String ris = null;
			//se ho già impostato la riapertura copio i piani.
			if(dati.containsKey(CostantiSinssntW.CTS_DT_PROROGA_INIZIO)){
				dati.put(CostantiSinssntW.DATACHIUSURAPIANO, dati.get(CostantiSinssntW.CTS_DT_PROROGA_INIZIO));
				dati.put("forzaDataFinePiano", ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_DT_PROROGA_FINE));
				dati.put("tipoVariazione", "proroga");
//				dati.put("dataFinePrestazioniDaProrogare", dati.get("data_fine"));
				ris = paEjb.gestisci_aperturePerSospensioneOProroga(mylogin, dbc, dati);
			}
			
			
			String query = recuperaQueryKey(nCartella, idSkSo, idProroga+"", true);
			dbrInsert = dbc.readRecord(query);
	        dbc.commitTransaction();
		    done=true;
		}catch(DBRecordChangedException e){
			throw e;
		}catch(ISASPermissionDeniedException e){
			throw e;
		}catch(Exception e){
			throw newEjbException("Errore in "+punto+": "+ e.getMessage(),e);
		}finally{
			if (!done)
				rollback_nothrow(punto, dbc);
			logout_nothrow(punto, dbc);
		}
		return dbrInsert;
	}

	private int getSelectProgressivo(ISASConnection mydbc, String nCartella) throws Exception {
		String punto = ver+ "getProgressivo ";
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String query = "SELECT MAX(id_proroga) max_progr FROM rm_skso_proroghe " +
				"WHERE n_cartella = " + nCartella;
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbr = mydbc.readRecord(query);
		if (dbr != null)
			intProgressivo = u.getIntField(dbr, "max_progr");

		intProgressivo++;
		return intProgressivo;
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String punto = ver + "update";
		ISASConnection dbc = null;
		PianoAssistEJB paEjb = new PianoAssistEJB();

		boolean done = false;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			
			String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
			String idProroga = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SO_ID_PROROGA);

			//se ho impostato la chiusura della proroga chiudo i piani.
			if(dbr.getHashtable().containsKey(CostantiSinssntW.CTS_DT_PROROGA_FINE)){
				Hashtable h = dbr.getHashtable();
				h.put(CostantiSinssntW.DATACHIUSURAPIANO, dbr.get(CostantiSinssntW.CTS_DT_PROROGA_FINE));
				h.put("tipoVariazione", "proroga");
				paEjb.gestisci_chiusurePerSospensioneOProroga(mylogin, dbc, h);
			}
			dbc.writeRecord(dbr);
			
			String myselect = recuperaQueryKey(nCartella, idSkSo, idProroga, true);
			LOG.trace(punto + " - query>>" + myselect);
			ISASRecord dbret = dbc.readRecord(myselect);
			dbc.commitTransaction();
			done = true;
			
			return dbret;

		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			if (!done)
				rollback_nothrow(punto, dbc);
			logout_nothrow(punto, dbc);
		}
	}

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String punto = ver + "delete";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);

		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

//	private String recuperaQueryKey(String nCartella, String idSkSo, String idProroga, boolean queryKey) {
//		String punto = ver + "recuperaQueryKey ";
//		String query = "SELECT * FROM rm_skso_proroghe WHERE n_cartella=" + nCartella + " AND id_skso=" + idSkSo;
//		if (queryKey) {
//			query += " AND id_proroga= " + idProroga;
//		}
//		LOG.trace(punto + " query>>" + query + " \nqueryKey>>" + queryKey);
//		return query;
//	}

	public boolean esisteSovrapposizione(myLogin myLogin, Hashtable dati) {
		boolean sovrapposizione = false;
		String punto = ver + "";
		ISASConnection dbc = null;
		ISASCursor dbrRmSkSoProroghe =null; 
		try {
			dbc = super.logIn(myLogin);
			LOG.trace(punto + " dati che ricevo>>" + dati +"<<");
			String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
			String idProroga = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_SO_ID_PROROGA);
			String dtProrogaInizio = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_DT_PROROGA_INIZIO);
			String dtProrogaFine = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_DT_PROROGA_FINE);
			
			String queryModello = recuperaQueryKey(nCartella, idSkSo, idProroga, false);
			if (ISASUtil.valida(idProroga)){
				queryModello += " and id_proroga <> " +idProroga ;
			}
			String query = sovrappossizioneIntere(dbc, dtProrogaInizio, dtProrogaFine,
					queryModello);
			LOG.trace(punto + "Query "+ query);
			dbrRmSkSoProroghe = dbc.startCursor(query);
			LOG.debug(punto + " dimensione>>"+ dbrRmSkSoProroghe.getDimension()+"<");
			sovrapposizione = (dbrRmSkSoProroghe.getDimension()>0);
			if (!sovrapposizione){
				query = sovrappossizioneAperte(dbc, dtProrogaInizio, dtProrogaFine, queryModello);
				LOG.trace(punto + "Query "+ query);
				dbrRmSkSoProroghe = dbc.startCursor(query);
				LOG.debug(punto + " dimensione>>"+ dbrRmSkSoProroghe.getDimension()+"<");
				sovrapposizione = (dbrRmSkSoProroghe.getDimension()>0);
				if (!sovrapposizione){
					query = sovrappossizioneInterne(dbc, dtProrogaInizio, dtProrogaFine, queryModello);
					LOG.trace(punto + "Query "+ query);
					dbrRmSkSoProroghe = dbc.startCursor(query);
					LOG.debug(punto + " dimensione>>"+ dbrRmSkSoProroghe.getDimension()+"<");
					sovrapposizione = (dbrRmSkSoProroghe.getDimension()>0);
				}else {
					LOG.trace(punto + " non ci sono sovrapposizioni sovrappossizioneAperte");
				}
			}else {
				LOG.trace(punto + " non ci sono sovrapposizioni ");
			}
			
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recupera i dati", e );
		}finally{
			logout_nothrow(punto,  dbrRmSkSoProroghe, dbc);
		}
		
		return sovrapposizione;
	}

//	private String sovrappossizioneInterne(ISASConnection dbc,
//			String dtProrogaInizio, String dtProrogaFine, String query) {
//		if (ManagerDate.validaData(dtProrogaInizio)){
//			query += " AND " + formatDate(dbc, dtProrogaInizio)+ " < DT_PROROGA_INIZIO ";
//		}
//		if (ManagerDate.validaData(dtProrogaFine)){
//			query +=" AND ( DT_PROROGA_FINE IS NULL OR " + formatDate(dbc, dtProrogaFine) +
//					" > DT_PROROGA_FINE) ";
//		}
//		return query;
//	}
//
//	private String sovrappossizioneAperte(ISASConnection dbc,
//		String dtProrogaInizio, String dtProrogaFine, String query) {
//
//		if (ManagerDate.validaData(dtProrogaInizio)){
//				query += " AND dt_proroga_fine IS NULL and " +
//						formatDate(dbc, dtProrogaInizio) +" > dt_proroga_inizio"; 
//		}
//			return query;
//	}
//
//	private String sovrappossizioneIntere(ISASConnection dbc,
//			String dtProrogaInizio, String dtProrogaFine, String query) {
//		if (ManagerDate.validaData(dtProrogaInizio)){
//			query +=" AND( (   dt_proroga_fine IS NULL OR " + formatDate(dbc, dtProrogaInizio) +
//					"< dt_proroga_fine )   AND " + formatDate(dbc, dtProrogaInizio) +
//					" > dt_proroga_inizio ";  
//		}
//		if (ManagerDate.validaData(dtProrogaFine)){
//			query+="  or (   dt_proroga_fine IS NULL OR " +formatDate(dbc, dtProrogaFine) + " < dt_proroga_fine ) " +
//					" AND " +formatDate(dbc, dtProrogaFine) +" > dt_proroga_inizio ) ";
//		}else {
//			query += " )";
//		}
//		
//		return query;
//	}

	public boolean esisteProroghe(ISASConnection dbc, String nCartella, String idSkso) {
		String punto = ver  +"esisteIntolleranzeAllergie ";
		
		StringBuffer query = new StringBuffer();
		query.append("select count(*) numero from rm_skso_proroghe where n_cartella = ");
		query.append(nCartella);
		query.append(" and id_skso = ");
		query.append(idSkso);
		
		LOG.debug(punto + " query>> "+ query);
		ISASRecord dbrProroghe = null;
		int numeroProroga = -1; 
		try {
			dbrProroghe = dbc.readRecord(query.toString());
			numeroProroga = ISASUtil.getValoreIntero(dbrProroghe, "numero");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (numeroProroga > 0);
	}

	public String getMaxDataProroga(ISASConnection dbc, String nCartella, String idSkso) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String dtFineProroga = "";
		// la fine proroga è obbligatoria: non prendo in considerazione il caso
		// in cui la fine proroga non sia valorizzata.
		String query = "select max(dt_proroga_fine) as dt_fine from rm_skso_proroghe where n_cartella = " + nCartella
				+ " and id_skso = " + idSkso;
		ISASRecord dbrSkSoProroghe = dbc.readRecord(query);
		dtFineProroga = ISASUtil.getValoreStringa(dbrSkSoProroghe, "dt_fine");
		return dtFineProroga;
	}

	public String getMaxDataProrogaML(myLogin myLogin, String nCartella, String idSkso) {
		String punto = ver + "getMaxDataProrogaML ";
		ISASConnection dbc = null;
		String dtMaxProroga = ""; 
		try {
			dbc = super.logIn(myLogin);
			LOG.trace(punto + " dati che ricevo nCartella>>" + nCartella+"<< idSkso>>" +idSkso+"<<");

			dtMaxProroga = getMaxDataProroga(dbc, nCartella, idSkso);
		
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recupera i dati", e );
		}finally{
			logout_nothrow(punto, dbc);
		}
		return dtMaxProroga;
	}

}

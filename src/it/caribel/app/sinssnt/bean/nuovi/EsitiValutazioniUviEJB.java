package it.caribel.app.sinssnt.bean.nuovi;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// Boffa
// ==========================================================================
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.EsitiValutazioniUviCtrl;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class EsitiValutazioniUviEJB extends SINSSNTConnectionEJB {
	public EsitiValutazioniUviEJB() {
	}

	private String ver = "7-";

	Hashtable<String, String> datiDecodificaRevisione = new Hashtable<String, String>();
	Hashtable<String, String> datiDecodificaEsito = new Hashtable<String, String>();

	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h) throws SQLException {
		ISASConnection dbc = null;
		String punto = ver + "query ";
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
			String myselect = recuperaQueryKey(nCartella, idSkSo, "", false);
			LOG.debug(punto + " query>>" + myselect);
			dbcur = dbc.startCursor(myselect);
			vdbr = dbcur.getAllRecord();

			if (vdbr != null && vdbr.size() > 0) {
				datiDecodificaRevisione = EsitiValutazioniUviCtrl.recuperaDecodificaRevisione();
				datiDecodificaEsito = EsitiValutazioniUviCtrl.recuperaDecodificaEsito();

				for (int i = 0; i < vdbr.size(); i++) {
					ISASRecord dbrRmEsitiValutazioniUvi = (ISASRecord) vdbr.get(i);
					decodificaDati(dbrRmEsitiValutazioniUvi);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ", e);
		} finally {
			logout_nothrow(punto, dbcur, dbc);
		}
		return vdbr;
	}

	private void decodificaDati(ISASRecord dbrRmEsitiValutazioniUvi) throws ISASMisuseException {
		//		Hashtable<String , String> datiDecodificaRevisione = recuperaDecodificaRevisione();
		//		Hashtable<String , String> datiDecodificaEsito = recuperaDecodificaEsito();
		if (dbrRmEsitiValutazioniUvi != null) {
			String esitoValutazione = ISASUtil.getValoreStringa(dbrRmEsitiValutazioniUvi,
					CostantiSinssntW.CTS_ESITO_VALUTAZIONE);
			String esitoValutazioneDesc = "";
			if (ISASUtil.valida(esitoValutazione)) {
				esitoValutazioneDesc = ISASUtil.getValoreStringa(datiDecodificaEsito, esitoValutazione);
			}
			dbrRmEsitiValutazioniUvi.put(CostantiSinssntW.CTS_ESITO_VALUTAZIONE + "_desc", esitoValutazioneDesc);
			String prRevisione = ISASUtil.getValoreStringa(dbrRmEsitiValutazioniUvi, CostantiSinssntW.CTS_PR_REVISIONE);
			String prRevisioneDesc = "";
			if (ISASUtil.valida(prRevisione)) {
				prRevisioneDesc = ISASUtil.getValoreStringa(datiDecodificaRevisione, prRevisione);
			}
			dbrRmEsitiValutazioniUvi.put(CostantiSinssntW.CTS_PR_REVISIONE + "_desc", prRevisioneDesc);
		}
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKey ";
		ISASConnection dbc = null;
		ISASRecord dbrProroghe = null;
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		String idEsitoValutazione = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_ESITO_VALUTAZIONE);

		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			if (ISASUtil.valida(idEsitoValutazione)) {
				dbc = super.logIn(mylogin);
				String myselect = recuperaQueryKey(nCartella, idSkSo, idEsitoValutazione, true);
				LOG.trace(punto + " query>>" + myselect);
				dbrProroghe = dbc.readRecord(myselect);
			}

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrProroghe;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ISASRecord insert(myLogin mylogin, Hashtable dati) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, ISASMisuseException, DBMisuseException, DBSQLException {
		String punto = ver + "insert ";
		String nCartella = null;
		String idSkSo = null;
		ISASConnection dbc = null;
		ISASRecord dbrInsert = null;
		boolean done = false;
		try {
			nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		} catch (Exception e) {
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			dbrInsert = dbc.newRecord("rm_esiti_valutazioni_uvi");
			int idEsitoValutazione = getSelectProgressivo(dbc, nCartella);
			Enumeration n = dati.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbrInsert.put(e, dati.get(e));
			}
			dbrInsert.put(CostantiSinssntW.CTS_ID_ESITO_VALUTAZIONE, idEsitoValutazione);
			dbc.writeRecord(dbrInsert);

			Hashtable<String, Object> datiAggiornati = aggiornaRmSkso(dbc, nCartella, idSkSo, dbrInsert, mylogin);
			LOG.debug(punto + " datiAggiornati recuperati>>" + (datiAggiornati));
			
			String query = recuperaQueryKey(nCartella, idSkSo, idEsitoValutazione + "", true);
			dbrInsert = dbc.readRecord(query);

			Enumeration<String> keys = datiAggiornati.keys();
			String key;
			LOG.debug(punto + " prima >>" + (dbrInsert!=null ? dbrInsert.getHashtable()+"":" no dati " ));
			while (keys.hasMoreElements()) {
				key = (String) keys.nextElement();
				dbrInsert.put(key, ISASUtil.getValoreStringa(datiAggiornati, key));
			}
			LOG.debug(punto + "datiInseriti>>" + (dbrInsert!=null ? dbrInsert.getHashtable()+"":" no dati " ));

			dbc.commitTransaction();
			done = true;
		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in aggiornamento Esito valutazione " + punto + ": " + e.getMessage(), e);
		} finally {
			if (!done) {
				rollback_nothrow(punto, dbc);
				LOG.error(punto + " Si e' verificato errore in salvataggio dati ");
			}
			logout_nothrow(punto, dbc);
		}
		return dbrInsert;
	}

	private Hashtable<String, Object> aggiornaRmSkso(ISASConnection dbc, String nCartella, String idSkSo,
			ISASRecord dbrRmEsitiValutazioneUvi, myLogin mylogin) throws ISASMisuseException, DBMisuseException,
			DBSQLException, ISASPermissionDeniedException, DBRecordChangedException, ParseException, CariException,
			SQLException, Exception {
		String punto = ver + "aggiornaRmSkso ";
		Hashtable<String, Object> datiAggiornati = new Hashtable<String, Object>();

		String esitoValutazione = ISASUtil.getValoreStringa(dbrRmEsitiValutazioneUvi, CostantiSinssntW.CTS_ESITO_VALUTAZIONE);
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		LOG.debug(punto + " esitoValutazione>>" + esitoValutazione + "<<");
		ISASRecord dbrRmSksoNew = null;
		if (ISASUtil.valida(esitoValutazione)) {
			if (esitoValutazione.equals(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_CAMBIA_PIANO)) {
				LOG.debug(punto + " eseguo la procedura di cambio piano ");
				
				ISASRecord dbrRmSkso = rmSkSOEJB.queryKeyRmSkso(nCartella, idSkSo, dbc);
				if (dbrRmSkso!=null){
					String dtValutazione = ISASUtil.getValoreStringa(dbrRmEsitiValutazioneUvi, CostantiSinssntW.CTS_DT_VALUTAZIONE);
					Hashtable<String, String> datiDaInvia = new Hashtable<String, String>();
					datiDaInvia.putAll(dbrRmSkso.getHashtable());
					datiDaInvia.put(CostantiSinssntW.DATA_CHIUSURA, dtValutazione);
					
					LOG.debug(punto + " dati recuperati >>" + datiDaInvia);
					dbrRmSksoNew = rmSkSOEJB.cambioPiano(mylogin, datiDaInvia, dbc);
					String idSksNew = ISASUtil.getValoreStringa(dbrRmSksoNew, CostantiSinssntW.CTS_ID_SKSO);
					
					rimuoviValutazioneFromNewSO(dbc, nCartella, rmSkSOEJB, idSksNew);
					
					datiAggiornati.put(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_ID_SKSO_NEW, idSksNew);
				}else {
					LOG.debug(punto + " Errore nel recupera dbr rmskso ");
				}
			}
		}
		rmSkSOEJB.aggiornaValutazioneUVI(dbc, nCartella, idSkSo, dbrRmEsitiValutazioneUvi);

		return datiAggiornati;
	}

	public void rimuoviValutazioneFromNewSO(ISASConnection dbc, String nCartella, RMSkSOEJB rmSkSOEJB, String idSksNew)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException {
		String punto = ver + " rimuoviValutazioneFromNewSO ";
		LOG.debug(punto + " Rimuovo dal nuovo rmskso la valutazione vecchia ");
		
		ISASRecord dbrRmSkso =   rmSkSOEJB.queryKeyRmSkso(nCartella, idSksNew, dbc);
		
		dbrRmSkso.put(CostantiSinssntW.CTS_RMSKSO_PR_DATA_REVISIONE, null);
		dbrRmSkso.put(CostantiSinssntW.CTS_RMSKSO_PR_REVISIONE, null);
		
		LOG.trace(punto + " salvo i dati >>" + (dbrRmSkso!=null ? dbrRmSkso.getHashtable()+"": ""));
		
		dbc.writeRecord(dbrRmSkso);
		
	}

	private int getSelectProgressivo(ISASConnection mydbc, String nCartella) throws Exception {
		String punto = ver + "getProgressivo ";
		int intProgressivo = 0;

		String query = "SELECT MAX(id_esito_valutazione) max_progr FROM rm_esiti_valutazioni_uvi "
				+ "WHERE n_cartella = " + nCartella;
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbr = mydbc.readRecord(query);
		if (dbr != null) {
			intProgressivo = ISASUtil.getIntField(dbr, "max_progr");
		}

		intProgressivo++;
		return intProgressivo;
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbrEsitoValutazione) throws DBRecordChangedException,
			ISASPermissionDeniedException, Exception {
		String punto = ver + "update";
		ISASConnection dbc = null;
		boolean done = false;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			String nCartella = ISASUtil.getValoreStringa(dbrEsitoValutazione, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dbrEsitoValutazione, CostantiSinssntW.CTS_ID_SKSO);
			String idEsitoValutazione = ISASUtil.getValoreStringa(dbrEsitoValutazione, CostantiSinssntW.CTS_ID_ESITO_VALUTAZIONE);

			dbc.writeRecord(dbrEsitoValutazione);

			Hashtable<String, Object> datiAggiornati = aggiornaRmSkso(dbc, nCartella, idSkSo, dbrEsitoValutazione, mylogin);
			LOG.debug(punto + " datiAggiornati recuperati>>" + (datiAggiornati));

			
			String myselect = recuperaQueryKey(nCartella, idSkSo, idEsitoValutazione, true);
			LOG.trace(punto + " - query>>" + myselect);
			ISASRecord dbret = dbc.readRecord(myselect);
			dbc.commitTransaction();
			done = true;

			return dbret;

		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (CariException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			if (!done)
				rollback_nothrow(punto, dbc);
			logout_nothrow(punto, dbc);
		}
	}

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException,
			Exception {
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

	private String recuperaQueryKey(String nCartella, String idSkSo, String idEsitoValutazione, boolean queryKey) {
		String punto = ver + "recuperaQueryKey ";
		String query = "SELECT * FROM rm_esiti_valutazioni_uvi WHERE n_cartella=" + nCartella + " AND id_skso="
				+ idSkSo;
		if (queryKey) {
			query += " AND id_esito_valutazione = " + idEsitoValutazione;
		} else {
			query += " order by dt_prossima_valutazione desc ";
		}
		LOG.trace(punto + " query>>" + query + " \nqueryKey>>" + queryKey);
		return query;
	}

	public Hashtable<String, String> recuperaDatiUltimaValutazioneUvi(myLogin mylogin, Hashtable<String, Object> dati)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "recuperaDatiUltimaValutazioneUvi ";
		Hashtable<String, String> datiUvi = new Hashtable<String, String>();
		String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			ISASRecord dbrUltimaValutazioneUvi = null;

			String dtUltimaValutazioneUvi = "";
			String sceltaRevisione = "";
			dbrUltimaValutazioneUvi = recuperaUltimaValutazione(nCartella, idSkso, dbc);

			if (dbrUltimaValutazioneUvi != null) {
				LOG.trace(punto + " recupero ultimo record della valutazione uvi ");
				dtUltimaValutazioneUvi = ISASUtil.getValoreStringa(dbrUltimaValutazioneUvi,
						CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE);
				sceltaRevisione = ISASUtil.getValoreStringa(dbrUltimaValutazioneUvi, CostantiSinssntW.CTS_PR_REVISIONE);
			} else {
				LOG.trace(punto + " recupero dalla scheda so  ");
				RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
				dbrUltimaValutazioneUvi = rmSkSOEJB.queryKey(dbc, nCartella, idSkso);
				dtUltimaValutazioneUvi = ISASUtil.getValoreStringa(dbrUltimaValutazioneUvi,
						CostantiSinssntW.CTS_RMSKSO_PR_DATA_REVISIONE);
				sceltaRevisione = ISASUtil.getValoreStringa(dbrUltimaValutazioneUvi, CostantiSinssntW.CTS_RMSKSO_PR_REVISIONE);
			}
			datiUvi.put(CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE, dtUltimaValutazioneUvi);
			datiUvi.put(CostantiSinssntW.CTS_PR_REVISIONE, sceltaRevisione);

		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return datiUvi;
	}

	public ISASRecord recuperaUltimaValutazione(String nCartella, String idSkso, ISASConnection dbc)
			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException {
		String punto = ver + "recuperaUltimaValutazione ";

		ISASCursor dbcur = null;
		ISASRecord dbrUltimaValutazioneUvi = null;

		String myselect = recuperaQueryKey(nCartella, idSkso, "", false);
		LOG.debug(punto + " query>>" + myselect);
		try {
			dbcur = dbc.startCursor(myselect);
			if (dbcur != null && dbcur.next()) {
				dbrUltimaValutazioneUvi = (ISASRecord) dbcur.getRecord();
			}
		} finally {
			close_dbcur_nothrow(punto, dbcur);
		}
		LOG.debug(punto + " ho recuperto i dati>"
				+ (dbrUltimaValutazioneUvi != null ? dbrUltimaValutazioneUvi.getHashtable() : " no dati "));

		return dbrUltimaValutazioneUvi;
	}

	public boolean esistoSchedeSuccessive(myLogin myLogin, Hashtable<String, Object> dati) throws Exception {
		String punto = ver + "esistoSchedeSuccessive ";
		boolean esistonoSchedeSuccessive = false;
		String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		String idEsitoValutazione = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_ESITO_VALUTAZIONE);
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(myLogin);
			StringBuffer query = new StringBuffer();
			query.append("SELECT count(*) as numero FROM rm_esiti_valutazioni_uvi r WHERE r.n_cartella = ");
			query.append(nCartella);
			query.append(" AND r.id_skso = ");
			query.append(idSkso);
			query.append(" AND r.dt_prossima_valutazione > (SELECT x.dt_prossima_valutazione FROM rm_esiti_valutazioni_uvi x ");
			query.append(" WHERE x.n_cartella = r.n_cartella AND x.id_skso = r.id_skso AND id_esito_valutazione = ");
			query.append(idEsitoValutazione);
			query.append(" ) ");

			LOG.debug(punto + " query>>" + query);
			ISASRecord dbrConteggio = dbc.readRecord(query.toString());
			int numeroSchede = ISASUtil.getValoreIntero(dbrConteggio, "numero");
			esistonoSchedeSuccessive = numeroSchede > 0;
		} finally {
			logout_nothrow(punto, dbc);
		}

		LOG.debug(punto + "esistonoSchedeSuccessive>>" + esistonoSchedeSuccessive + "<");
		return esistonoSchedeSuccessive;
	}

	public void recuperaUltimaValutazioneUvi(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException,
			DBMisuseException, DBSQLException, ISASPermissionDeniedException {
		String punto = ver + "recuperaUltimaValutazioneUvi ";
		String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
		String dtUltimaValutazioneUvi = "";

		ISASRecord dbrUltimaValutazioneUvi = recuperaUltimaValutazione(nCartella, idSkso, dbc);

		if (dbrUltimaValutazioneUvi != null) {
			LOG.trace(punto + " recupero ultimo record della valutazione uvi ");
			dtUltimaValutazioneUvi = ISASUtil.getValoreStringa(dbrUltimaValutazioneUvi,
					CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE);
		}
		dbr.put(CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE, dtUltimaValutazioneUvi);
	}

	public ISASRecord recuperaUltimaValutazione(myLogin myLogin, Hashtable<String, Object> dati) {
		String punto = ver + "recuperaUltimaValutazione ";
		String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		ISASConnection dbc = null;
		ISASRecord dbrUltimaValutazioneUvi = null;
		try {
			dbc = super.logIn(myLogin);
			dbrUltimaValutazioneUvi = recuperaUltimaValutazione(nCartella, idSkso, dbc);
			LOG.debug(punto + "dbrUltimaValutazioneUvi>>" + (dbrUltimaValutazioneUvi != null ? "" : " no dati ") + "<");

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare ultima data della valutazione ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}

		return dbrUltimaValutazioneUvi;
	}

}

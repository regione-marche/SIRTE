package it.caribel.app.sinssnt.bean.nuovi;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// 30/09/2014 - //     scrittura nella tabella rm_skso_sospensioni
// ==========================================================================

import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
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

public class RMSkSOSKSoSospensioniEJB extends SINSSNTConnectionEJB {
	public RMSkSOSKSoSospensioniEJB() {
	}

	private String ver = "1-";

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		ISASConnection dbc = null;
		String punto = ver + "query ";
		ISASCursor dbcur = null;
		Vector vdbr = new Vector();
		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
			String myselect = recuperaQueryKey(nCartella, idSkSo, "", false);
			LOG.debug(punto + " query>>" + myselect);
			dbcur = dbc.startCursor(myselect);
			vdbr = dbcur.getAllRecord();
			decodificaDati(dbc, vdbr);

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ", e);
		} finally {
			logout_nothrow(punto, dbcur, dbc);
		}
		return vdbr;
	}

	private void decodificaDati(ISASConnection dbc, Vector vdbr) {
		String punto = ver + "";
		String motivo, motivoDescr;
		LOG.trace(punto + " Inizio la decodifica  ");
		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
		for (int i = 0; i < vdbr.size(); i++) {
			ISASRecord dbrRmSkSoSospensione = (ISASRecord) vdbr.get(i);
			motivo = ISASUtil.getValoreStringa(dbrRmSkSoSospensione, "motivo");
			if (ISASUtil.valida(motivo)) {
				motivoDescr = "";
				try {
					motivoDescr = ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
							CostantiSinssntW.TAB_VAL_SO_MOTIVO_SOSPENSIONE, motivo, "tab_descrizione");
					dbrRmSkSoSospensione.put("motivo_descr", motivoDescr);
				} catch (Exception e) {
					LOG.debug(punto + "Errore nella decodifica del codice>>" + motivo, e);
				}
			}

		}

	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKey ";
		ISASConnection dbc = null;
		ISASRecord dbrProroghe = null;
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		String idSospensione = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SO_ID_SOSPENSIONE);

		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			if (ISASUtil.valida(idSospensione)) {
				dbc = super.logIn(mylogin);
				String myselect = recuperaQueryKey(nCartella, idSkSo, idSospensione, true);
				LOG.trace(punto + " query>>" + myselect);
				dbrProroghe = dbc.readRecord(myselect);
			}

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della sospensione ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrProroghe;
	}

	public ISASRecord queryKey(ISASConnection dbc, Hashtable h) throws SQLException {
		String punto = ver + "queryKey ";
		ISASRecord dbrProroghe = null;
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		String idSospensione = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SO_ID_SOSPENSIONE);

		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			if (ISASUtil.valida(idSospensione)) {
				String myselect = recuperaQueryKey(nCartella, idSkSo, idSospensione, true);
				LOG.trace(punto + " query>>" + myselect);
				dbrProroghe = dbc.readRecord(myselect);
			}

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della sospensione ", e);
		}
		return dbrProroghe;
	}

	public ISASRecord insert(myLogin mylogin, Hashtable dati) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		String punto = ver + "insert ";
		String nCartella = null;
		String idSkSo = null;
		ISASConnection dbc = null;
		ISASRecord dbrInsert = null;
		ISASCursor dbcur = null;
		Vector vdbr = new Vector();
		PianoAssistEJB paEjb = new PianoAssistEJB();
		try {
			nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		} catch (Exception e) {
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			dbrInsert = dbc.newRecord("rm_skso_sospensioni");
			int idSospensione = getSelectProgressivo(dbc, nCartella);
			Enumeration n = dati.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbrInsert.put(e, dati.get(e));
			}
			dbrInsert.put(CostantiSinssntW.CTS_SO_ID_SOSPENSIONE, idSospensione);
			gestisciFlag(dbc, dbrInsert, true);
			dbc.writeRecord(dbrInsert);
			
			dati.put("tipoVariazione", "sospensione");
			// chiudo i pianiAssistenziali alla data di sospensione
			// recupero credenziali per la chiusura dei piani
			// e chiudo i piani per gli operatori designati.
			dati.put(CostantiSinssntW.DATACHIUSURAPIANO, dati.get(CostantiSinssntW.CTS_DT_SOSPENSIONE_INIZIO));
			paEjb.gestisci_chiusurePerSospensioneOProroga(mylogin, dbc, dati);

			// se ho giÃ  impostato la riapertura copio i piani.
			if (dati.containsKey(CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE)) {
				dati.put(CostantiSinssntW.DATACHIUSURAPIANO, dati.get(CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE));
				dati.put("forzaDataFinePiano", ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_DT_SOSPENSIONE_INIZIO));//copia anche le prestazioni che finiscono esattamente alla data fine piano
				paEjb.gestisci_aperturePerSospensioneOProroga(mylogin, dbc, dati);
			}

			String query = recuperaQueryKey(nCartella, idSkSo, idSospensione + "", true);
			dbrInsert = dbc.readRecord(query);

		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrInsert;
	}

	private int getSelectProgressivo(ISASConnection mydbc, String nCartella) throws Exception {
		String punto = ver + "getProgressivo ";
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String query = "SELECT MAX(id_sospensione) max_progr FROM rm_skso_sospensioni " + "WHERE n_cartella = "
				+ nCartella;
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbr = mydbc.readRecord(query);
		if (dbr != null)
			intProgressivo = u.getIntField(dbr, "max_progr");

		intProgressivo++;
		return intProgressivo;
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException,
			ISASPermissionDeniedException, Exception {
		String punto = ver + "update";
		ISASConnection dbc = null;
		PianoAssistEJB paEjb = new PianoAssistEJB();

		try {
			dbc = super.logIn(mylogin);

			String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
			String idSospensione = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SO_ID_SOSPENSIONE);
			// se ho impostato la riapertura copio i piani.
			if (dbr.getHashtable().containsKey(CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE)) {
				dbr.getDBRecord().getHashtable()
						.put(CostantiSinssntW.DATACHIUSURAPIANO, dbr.getHashtable().get(CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE));
				dbr.getDBRecord().getHashtable().put("forzaDataFinePiano", ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_DT_SOSPENSIONE_INIZIO));//copia anche le prestazioni che finiscono esattamente alla data fine piano
				dbr.getDBRecord().getHashtable().put("tipoVariazione", "sospensione");
				paEjb.gestisci_aperturePerSospensioneOProroga(mylogin, dbc, dbr.getHashtable());
			}
			gestisciFlag(dbc, dbr, false);
			dbc.writeRecord(dbr);

			String myselect = recuperaQueryKey(nCartella, idSkSo, idSospensione, true);
			LOG.trace(punto + " - query>>" + myselect);
			ISASRecord dbret = dbc.readRecord(myselect);

			return dbret;

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

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException,CariException,
			Exception {
		String punto = ver + "delete";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			
			if (dbr.get("flag_sent")==null || dbr.get("flag_sent").toString().equals("0")){
			dbc.deleteRecord(dbr);
			}
			else throw new CariException("Impossibile cancellare l'informazione perchÃ© risulta giÃ  inviata tramite flussi ministeriali.");         

		}catch(CariException e){			
			throw e;
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

	private String recuperaQueryKey(String nCartella, String idSkSo, String idSospensione, boolean queryKey) {
		String punto = ver + "recuperaQueryKey ";
		String query = "SELECT * FROM rm_skso_sospensioni WHERE n_cartella=" + nCartella + " AND id_skso=" + idSkSo;
		if (queryKey) {
			query += " AND id_sospensione = " + idSospensione;
		}
		LOG.trace(punto + " query>>" + query + " \nqueryKey>>" + queryKey);
		return query;
	}

	public boolean esisteSovrapposizione(myLogin myLogin, Hashtable dati) {
		boolean sovrapposizione = false;
		String punto = ver + "";
		ISASConnection dbc = null;
		ISASCursor dbrRmSkSoProroghe = null;
		try {
			dbc = super.logIn(myLogin);
			LOG.trace(punto + " dati che ricevo>>" + dati + "<<");
			String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
			String idSospensione = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_SO_ID_SOSPENSIONE);
			String dtSospensioneInizio = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_DT_SOSPENSIONE_INIZIO);
			String dtSospensioneFine = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE);

			String queryModello = recuperaQueryKey(nCartella, idSkSo, idSospensione, false);
			if (ISASUtil.valida(idSospensione)) {
				queryModello += " and id_sospensione <> " + idSospensione;
			}
			String query = sovrappossizioneIntere(dbc, dtSospensioneInizio, dtSospensioneFine, queryModello);
			LOG.trace(punto + "Query " + query);
			dbrRmSkSoProroghe = dbc.startCursor(query);
			LOG.debug(punto + " dimensione>>" + dbrRmSkSoProroghe.getDimension() + "<");
			sovrapposizione = (dbrRmSkSoProroghe.getDimension() > 0);
			if (!sovrapposizione) {
				query = sovrappossizioneAperte(dbc, dtSospensioneInizio, dtSospensioneFine, queryModello);
				LOG.trace(punto + "Query " + query);
				dbrRmSkSoProroghe = dbc.startCursor(query);
				LOG.debug(punto + " dimensione>>" + dbrRmSkSoProroghe.getDimension() + "<");
				sovrapposizione = (dbrRmSkSoProroghe.getDimension() > 0);
				if (!sovrapposizione) {
					query = sovrappossizioneInterne(dbc, dtSospensioneInizio, dtSospensioneFine, queryModello);
					LOG.trace(punto + "Query " + query);
					dbrRmSkSoProroghe = dbc.startCursor(query);
					LOG.debug(punto + " dimensione>>" + dbrRmSkSoProroghe.getDimension() + "<");
					sovrapposizione = (dbrRmSkSoProroghe.getDimension() > 0);
				} else {
					LOG.trace(punto + " non ci sono sovrapposizioni sovrappossizioneAperte");
				}
			} else {
				LOG.trace(punto + " non ci sono sovrapposizioni ");
			}

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recupera i dati", e);
		} finally {
			logout_nothrow(punto, dbrRmSkSoProroghe, dbc);
		}

		return sovrapposizione;
	}

	private String sovrappossizioneInterne(ISASConnection dbc, String dtSospensioneInizio, String dtSospensioneFine,
			String query) {
		if (ManagerDate.validaData(dtSospensioneInizio)) {
			query += " AND " + formatDate(dbc, dtSospensioneInizio) + " < DT_sospensione_INIZIO ";
		}
		if (ManagerDate.validaData(dtSospensioneFine)) {
			query += " AND ( DT_sospensione_FINE IS NULL OR " + formatDate(dbc, dtSospensioneFine)
					+ " > DT_sospensione_FINE) ";
		}
		return query;
	}

	private String sovrappossizioneAperte(ISASConnection dbc, String dtSospensioneInizio, String dtSospensioneFine,
			String query) {

		if (ManagerDate.validaData(dtSospensioneInizio)) {
			query += " AND DT_sospensione_FINE IS NULL and " + formatDate(dbc, dtSospensioneInizio)
					+ " > DT_sospensione_INIZIO";
		}
		return query;
	}

	// private String sovrappossizioneIntere_(ISASConnection dbc,
	// String dtSospensioneInizio, String dtSospensioneFine, String query) {
	// if (ManagerDate.validaData(dtSospensioneInizio)){
	// query += " and ( DT_sospensione_FINE is null or " +
	// formatDate(dbc, dtSospensioneInizio)+ " < DT_sospensione_FINE) and  " +
	// formatDate(dbc, dtSospensioneInizio)+ " > DT_sospensione_INIZIO ";
	// }
	// if (ManagerDate.validaData(dtSospensioneFine)){
	// query+=" and ( DT_sospensione_FINE is null or " +formatDate(dbc,
	// dtSospensioneFine)+ " < DT_sospensione_FINE) and ";
	// query += formatDate(dbc, dtSospensioneFine)+ " > DT_sospensione_INIZIO ";
	// }
	// return query;
	// }

	private String sovrappossizioneIntere(ISASConnection dbc, String dtProrogaInizio, String dtProrogaFine, String query) {
		if (ManagerDate.validaData(dtProrogaInizio)) {
			query += " AND( (   DT_sospensione_FINE IS NULL OR " + formatDate(dbc, dtProrogaInizio)
					+ "< DT_sospensione_FINE )   AND " + formatDate(dbc, dtProrogaInizio) + " > DT_sospensione_INIZIO ";
		}
		if (ManagerDate.validaData(dtProrogaFine)) {
			query += "  or (   DT_sospensione_FINE IS NULL OR " + formatDate(dbc, dtProrogaFine)
					+ " < DT_sospensione_FINE ) " + " AND " + formatDate(dbc, dtProrogaFine)
					+ " > DT_sospensione_INIZIO ) ";
		} else {
			query += " )";
		}

		return query;
	}

	private void gestisciFlag(ISASConnection dbc, ISASRecord dbr, boolean inInsert) throws Exception {
		if (inInsert)
			dbr.put(CostantiSinssntW.FLAG_SENT, CostantiSinssntW.FLAG_DA_INVIARE_I);
		else {
			// aggiornamento valutazione o rivalutazione
			ISASRecord dbr_prec = queryKey(dbc, dbr.getHashtable());
			String flag_sent = dbr_prec.get("flag_sent") != null ? dbr_prec.get("flag_sent").toString()
					: CostantiSinssntW.FLAG_DA_INVIARE_I;
			if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_I))
				dbr.put("flag_sent", CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_I);
			else if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_V))
				dbr.put("flag_sent", CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_V);
			else if (flag_sent.equals(CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO))
				dbr.put("flag_sent", CostantiSinssntW.FLAG_DA_INVIARE_V);

		}
	}

	public boolean esisteSospensione(ISASConnection dbc, String nCartella, String idSkso) {
		String punto = ver + "esisteSospensione ";

		StringBuffer query = new StringBuffer();
		query.append("select count(*) numero from rm_skso_sospensioni where n_cartella = ");
		query.append(nCartella);
		query.append(" and id_skso = ");
		query.append(idSkso);

		LOG.debug(punto + " query>> " + query);
		ISASRecord dbrSospensioni = null;
		int numeroSospensioni = -1;
		try {
			dbrSospensioni = dbc.readRecord(query.toString());
			numeroSospensioni = ISASUtil.getValoreIntero(dbrSospensioni, "numero");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (numeroSospensioni > 0);
	}
}

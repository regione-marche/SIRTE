package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 19/09/2003 - EJB di connessione alla procedura SINS Tabella FoIntervOpe
//
// elisa b 29/09/11
// ==========================================================================

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.dateutility;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;

public class FoElencoSospConclEJB extends SINSSNTConnectionEJB {

	private String dtInizio = null;
	private String dtFine = null;
	private static final String MIONOME = "42-FoElencoSospConclEJB.";

	/**      
	 * restituisce un parametro data come stringa nel formato gg/mm/aaaa
	 */
	private String getStringDate(Hashtable par, String k) {
		try {
			String s = (String) par.get(k);
			s = s.substring(8, 10) + "/" + s.substring(5, 7) + "/" + s.substring(0, 4);
			return s;
		} catch (Exception e) {
			debugMessage("getStringDate(" + par + ", " + k + "): " + e);
			return "";
		}
	}

	/**
	 * restituisce un parametro come stringa
	 */
	private String getStringField(ISASRecord dbr, String f) {
		try {
			return (dbr.get(f)).toString();
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	/**
	 * restituisce un campo data come stringa
	 */
	private String getDateField(ISASRecord dbr, String f) {
		try {
			if (dbr.get(f) == null)
				return "";
			String d = ((java.sql.Date) dbr.get(f)).toString();
			d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/" + d.substring(0, 4);
			return d;
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	public FoElencoSospConclEJB() {
	}

	private void mkLayoutSospensioni(ISASConnection dbc, Hashtable par, mergeDocument doc) {
		String punto = MIONOME + "mkLayoutSospensioni ";
		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String tipo = (String) par.get("tipo");
		String filtri = "";
		try {
			ht.put("#txt#", getConfStringField(dbc, "SINS", "ragione_sociale", "conf_txt"));
			ht.put("#d1#", getStringDate(par, "d1"));
			ht.put("#d2#", getStringDate(par, "d2"));
			ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

			if (tipo.equals("S"))
				ht.put("#tipologia#", "SOSPENSIONI");
			else
				ht.put("#tipologia#", "CONCLUSIONI");

			if (!((String) par.get("zona")).equals("")) {
				String zona = (String) par.get("zona");
				filtri += " Zona: " + ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona");
			} else
				filtri += " Zona: TUTTE";

			if (!((String) par.get("distretto")).equals("")) {
				String distretto = (String) par.get("distretto");
				filtri += " Distretto: " + ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr");
			} else
				filtri += " Distretto: TUTTI";

			String ragg = ISASUtil.getValoreStringa(par, "ragg");

			if (ragg.equals("C")) {
				if (!((String) par.get("pca")).equals("")) {
					String pca = (String) par.get("pca");
					filtri += " Comune: " + ISASUtil.getDecode(dbc, "comuni", "codice", pca, "descrizione");
				} else
					filtri += " Comune: TUTTE ";
			} else if (ragg.equals("A")) {
				if (!((String) par.get("pca")).equals("")) {
					String pca = (String) par.get("pca");
					filtri += " Area Distrettuale: " + ISASUtil.getDecode(dbc, "areadis", "codice", pca, "descrizione");
				} else
					filtri += " Area Distrettuale: TUTTE ";
			}

			ht.put("#filtri#", filtri);
		} catch (Exception e) {
			stampa(punto + "\n Errore nel recuperare le informazioni sull'intestazione");
		}

		doc.writeSostituisci("layout", ht);

	}

	private void mkLayout(ISASConnection dbc, Hashtable par, mergeDocument doc) throws Exception {

		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String tipo = (String) par.get("tipo");
		//		String complessita = (String) par.get("compl_ass");
		String complessita = ISASUtil.getValoreStringa(par, "compl_ass");
		String filtri = "";

		ht.put("#txt#", getConfStringField(dbc, "SINS", "ragione_sociale", "conf_txt"));

		ht.put("#d1#", getStringDate(par, "d1"));
		ht.put("#d2#", getStringDate(par, "d2"));
		ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

		if (tipo.equals("S"))
			ht.put("#tipologia#", "SOSPENSIONI");
		else
			ht.put("#tipologia#", "CONCLUSIONI");

		if (!((String) par.get("zona")).equals("")) {
			String zona = (String) par.get("zona");
			filtri += " Zona: " + ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona");
		} else
			filtri += " Zona: TUTTE";

		if (!((String) par.get("distretto")).equals("")) {
			String distretto = (String) par.get("distretto");
			filtri += " Distretto: " + ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr");
		} else
			filtri += " Distretto: TUTTI";

		if (!((String) par.get("pca")).equals("")) {
			String pca = (String) par.get("pca");
			filtri += " Area Distrettuale: " + ISASUtil.getDecode(dbc, "areadis", "codice", pca, "descrizione");
		} else
			filtri += " Area Distrettuale: TUTTE";

		if (!complessita.equals(""))
			filtri += "Livello di complessita': "
					+ ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "COMPLASS", complessita, "tab_descrizione");

		ht.put("#filtri#", filtri);

		doc.writeSostituisci("layout", ht);
	}

	/**
	 * 
	 * @param dbc
	 * @param dbr
	 * @param cartella
	 * @throws Exception
	 */
	private void recuperaDatiAssistito(ISASConnection dbc, ISASRecord dbr, String cartella) throws Exception {
		ISASRecord dbrinfoAss = null;

		String query = "SELECT cognome, nome FROM cartella" + " WHERE n_cartella = " + cartella;

		dbrinfoAss = dbc.readRecord(query);
		if (dbrinfoAss != null) {
			dbr.put("nome", dbrinfoAss.get("nome").toString());
			dbr.put("cognome", dbrinfoAss.get("cognome").toString());
		}

		//System.out.println("recuperaDatiAssistito " + query);
	}

	/**
	 * Stampa l'elenco delle sospensioni
	 * @param md
	 * @param dbcur
	 * @param par
	 * @param dbc
	 * @throws Exception
	 */
	private void mkBodySospensioni(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String zona = "";
		String distretto = "";
		String areadis = "";
		String zonaOld = "";
		String distrettoOld = "";
		String areadisOld = "";

		dateutility du = new dateutility();

		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();

			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();

			if ((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld)) || (!areadis.equals(areadisOld))) {
				if (!zonaOld.equals(""))
					md.write("finetabSospensioni");

				Hashtable hZ = new Hashtable();
				hZ.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
				hZ.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
				hZ.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
				md.writeSostituisci("zona", hZ);

				zonaOld = zona;
				distrettoOld = distretto;
				areadisOld = areadis;

				md.write("iniziotabSospensioni");
			}

			String cartella = dbr.get("n_cartella").toString();
			recuperaDatiAssistito(dbc, dbr, cartella);

			//calcolo il numero di giorni
			int nGiorni = du.getNGiorni(dbr.get("dt_inizio").toString(), dbr.get("dt_fine").toString()) - 2;

			Hashtable htAss = new Hashtable();
			htAss.put("#n_cartella#", cartella);
			htAss.put("#cognome#", dbr.get("cognome").toString());
			htAss.put("#nome#", dbr.get("nome").toString());
			htAss.put("#dt_inizio#", getDateField(dbr, "dt_inizio"));
			htAss.put("#dt_fine#", getDateField(dbr, "dt_fine"));
			htAss.put("#n_giorni#", "" + (nGiorni + 1));
			htAss.put("#complessita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "COMPLASS", (String) dbr
					.get("skpa_complessita"), "tab_descrizione"));

			md.writeSostituisci("tabellaSospensioni", htAss);

		}
		md.write("finetabSospensioni");

	}

	/**
	 * In caso di stampa su foglio excel si deve stamapre un'unica tabella in 
	 * cui le informazioni su zona/distretto e area dis sono scritte su 3 
	 *  colonne aggiuntive
	 * @param md
	 * @param dbcur
	 * @param par
	 * @param dbc
	 * @throws Exception
	 */
	private void mkBodySospensioniFoglioCalcolo(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String zona = "";
		String distretto = "";
		String areadis = "";
		dateutility du = new dateutility();

		md.write("iniziotabSospensioni");

		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();

			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();

			String cartella = dbr.get("n_cartella").toString();
			recuperaDatiAssistito(dbc, dbr, cartella);

			//calcolo il numero di giorni
			int nGiorni = du.getNGiorni(dbr.get("dt_inizio").toString(), dbr.get("dt_fine").toString()) - 2;

			Hashtable htAss = new Hashtable();
			htAss.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
			htAss.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
			htAss.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));

			htAss.put("#n_cartella#", cartella);
			htAss.put("#cognome#", dbr.get("cognome").toString());
			htAss.put("#nome#", dbr.get("nome").toString());
			htAss.put("#dt_inizio#", getDateField(dbr, "dt_inizio"));
			htAss.put("#dt_fine#", getDateField(dbr, "dt_fine"));
			htAss.put("#n_giorni#", "" + (nGiorni + 1));
			htAss.put("#complessita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "COMPLASS", (String) dbr
					.get("skpa_complessita"), "tab_descrizione"));

			md.writeSostituisci("tabellaSospensioni", htAss);

		}
		md.write("finetabSospensioni");

	}

	private void mkBodyConclusioni(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String zona = "";
		String distretto = "";
		String areadis = "";
		String zonaOld = "";
		String distrettoOld = "";
		String areadisOld = "";

		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();

			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();

			if ((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld)) || (!areadis.equals(areadisOld))) {
				if (!zonaOld.equals(""))
					md.write("finetabConclusioni");

				Hashtable hZ = new Hashtable();
				hZ.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
				hZ.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
				hZ.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
				md.writeSostituisci("zona", hZ);

				zonaOld = zona;
				distrettoOld = distretto;
				areadisOld = areadis;

				md.write("iniziotabConclusioni");
			}

			String cartella = dbr.get("n_cartella").toString();
			recuperaDatiAssistito(dbc, dbr, cartella);
			String motivo = dbr.get("motivo").toString();
			if (motivo.length() == 1)
				motivo = "0" + motivo;
			//System.out.println("mkBodyConclusioni motivo: " + motivo);

			Hashtable htAss = new Hashtable();
			htAss.put("#n_cartella#", cartella);
			htAss.put("#cognome#", dbr.get("cognome").toString());
			htAss.put("#nome#", dbr.get("nome").toString()); 
			htAss.put("#dt_conclusione#", getDateField(dbr, "dt_conclusione"));
			htAss.put("#motivo#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", CostantiSinssntW.TAB_VAL_MOTIVO_CONCLUSIONE_FLUSSI_SIAD, motivo, "tab_descrizione"));
			htAss.put("#complessita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "COMPLASS", (String) dbr
					.get("skpa_complessita"), "tab_descrizione"));
			md.writeSostituisci("tabellaConclusioni", htAss);

		}
		md.write("finetabConclusioni");
	}

	/**
	 * In caso di stampa su foglio excel si deve stamapre un'unica tabella in 
	 * cui le informazioni su zona/distretto e area dis sono scritte su 3 
	 *  colonne aggiuntive
	 * @param md
	 * @param dbcur
	 * @param par
	 * @param dbc
	 * @throws Exception
	 */
	private void mkBodyConclusioniFoglioCalcolo(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String zona = "";
		String distretto = "";
		String areadis = "";

		md.write("iniziotabConclusioni");

		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();

			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();

			String cartella = dbr.get("n_cartella").toString();
			recuperaDatiAssistito(dbc, dbr, cartella);
			String motivo = dbr.get("motivo").toString();
			if (motivo.length() == 1)
				motivo = "0" + motivo;
			//System.out.println("mkBodyConclusioni motivo: " + motivo);

			Hashtable htAss = new Hashtable();
			htAss.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
			htAss.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
			htAss.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));

			htAss.put("#n_cartella#", cartella);
			htAss.put("#cognome#", dbr.get("cognome").toString());
			htAss.put("#nome#", dbr.get("nome").toString());
			htAss.put("#dt_conclusione#", getDateField(dbr, "dt_conclusione"));
			htAss.put("#motivo#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", CostantiSinssntW.TAB_VAL_MOTIVO_CONCLUSIONE_FLUSSI_SIAD, motivo, "tab_descrizione"));
			htAss.put("#complessita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "COMPLASS", (String) dbr
					.get("skpa_complessita"), "tab_descrizione"));
			md.writeSostituisci("tabellaConclusioni", htAss);

		}
		md.write("finetabConclusioni");
	}

	/**
	 * Select casi sospesi
	 * @param dbc
	 * @param par
	 * @return
	 */
	private String getSelectSospesi(ISASConnection dbc, Hashtable par) {
		ServerUtility su = new ServerUtility();
		String complessita = (String) par.get("compl_ass");

		String select = "SELECT c.*, a.skpa_complessita," + "u.cod_zona, u.cod_distretto," + "u.des_zona descrizione_zona, u.codice"
				+ " FROM caso_sospensione c" + " LEFT JOIN piano_assist a ON c.n_cartella = a.n_cartella," + " ubicazioni_n u, anagra_c ac"
				+ " WHERE dt_fine <= " + formatDate(dbc, dtFine) + " AND ac.n_cartella = c.n_cartella" + " AND pa_data IN ("
				+ " SELECT MAX(pa_data) FROM piano_assist a1" + " WHERE a1.n_cartella = a.n_cartella" + " AND a1.pa_data <= c.dt_fine"
				+ " AND (a1.pa_data_chiusura IS NULL" + " OR a1.pa_data_chiusura >= dt_fine" + ")" + ")" + " AND ac.data_variazione IN ("
				+ "SELECT MAX (data_variazione) FROM anagra_c" + " WHERE ac.n_cartella=anagra_c.n_cartella) "
				+ " AND u.codice = ac.areadis";

		if ((complessita != null) && (!complessita.equals(""))) {
			select += " AND a.skpa_complessita = '" + complessita + "'";
		}

		select = su.addWhere(select, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		select = su.addWhere(select, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		select = su.addWhere(select, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

		select += " ORDER BY cod_zona,u.cod_distretto,u.codice,a.skpa_complessita";
		debugMessage("FoElencoSospConclEJB.getSelectSospesi: " + select);

		return select;
	}

	/**
	 * Select casi conclusi
	 * @param dbc
	 * @param par
	 * @return
	 */
	private String getSelectConclusi(ISASConnection dbc, Hashtable par) {
		ServerUtility su = new ServerUtility();
		String complessita = (String) par.get("compl_ass");

		String select = "SELECT c.*, a.skpa_complessita," + "u.cod_zona, u.cod_distretto," + "u.des_zona descrizione_zona, u.codice"
				+ " FROM caso c" + " LEFT JOIN piano_assist a ON c.n_cartella = a.n_cartella," + " ubicazioni_n u, anagra_c ac"
				+ " WHERE dt_conclusione <= " + formatDate(dbc, dtFine) + " AND ac.n_cartella = c.n_cartella" + " AND pa_data IN ("
				+ " SELECT MAX(pa_data) FROM piano_assist a1" + " WHERE a1.n_cartella = a.n_cartella"
				+ " AND a1.pa_data <= c.dt_conclusione" + ")" + " AND ac.data_variazione IN ("
				+ "SELECT MAX (data_variazione) FROM anagra_c" + " WHERE ac.n_cartella=anagra_c.n_cartella) "
				+ " AND u.codice = ac.areadis";

		if ((complessita != null) && (!complessita.equals(""))) {
			select += " AND a.skpa_complessita = '" + complessita + "'";
		}

		select = su.addWhere(select, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		select = su.addWhere(select, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		select = su.addWhere(select, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

		select += " ORDER BY cod_zona,u.cod_distretto,u.codice,a.skpa_complessita";
		debugMessage("FoElencoSospConclEJB.getSelectConclusi: " + select);

		return select;
	}

	public byte[] query_report(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		dtInizio = (String) par.get("d1");
		dtFine = (String) par.get("d2");
		String tipo = (String) par.get("tipo");
		String type = (String) par.get("TYPE");

		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			ISASCursor dbcur = null;

			if (tipo.equals("S"))
				dbcur = dbc.startCursor(getSelectSospesi(dbc, par));
			else
				dbcur = dbc.startCursor(getSelectConclusi(dbc, par));

			mkLayout(dbc, par, eve);

			if (dbcur == null) {
				eve.write("messaggio");
				eve.write("finale");
				System.out.println("FoElencoSospConclEJB.query_IntervOpe(): " + "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					eve.write("messaggio");
					eve.write("finale");
				} else {
					if (tipo.equals("S")) {
						if (type.equalsIgnoreCase("PDF"))
							mkBodySospensioni(eve, dbcur, par, dbc);
						else
							mkBodySospensioniFoglioCalcolo(eve, dbcur, par, dbc);
					} else {
						if (type.equalsIgnoreCase("PDF"))
							mkBodyConclusioni(eve, dbcur, par, dbc);
						else
							mkBodyConclusioniFoglioCalcolo(eve, dbcur, par, dbc);
					}
					eve.write("finale");
				}
			}
			dbcur.close();
			eve.close();

			dbc.close();
			super.close(dbc);
			done = true;
			//System.out.println(eve.get());
			return eve.get(); // restituisco il bytearray
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoElencoSospConclEJB: " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("FoElencoSospConclEJB.query_intervope(): " + e1);
				}
			}
		}
	}

	public byte[] query_elencoSospensioniSospConc(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_elencoSospensioniSospConc";
		stampa(punto + " Inizio con dati>" + par + "<\n");
		boolean done = false;
		ISASConnection dbc = null;

		String tipo = (String) par.get("tipo");

		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			ISASCursor dbcur = null;

			if (tipo.equals("S"))
				dbcur = dbc.startCursor(getSelectSospesiTbCasoSospeso(dbc, par));
			else
				dbcur = dbc.startCursor(getSelectConclusiTbCaso(dbc, par));

			stampa(punto + "ho recuperato record>" + ((dbcur != null) ? dbcur.getDimension() + "" : " non ho recuperato record"));
			mkLayoutSospensioni(dbc, par, eve);

			if (dbcur == null) {
				eve.write("messaggio");
				eve.write("finale");
				stampa(punto + "\n NON HO RECUPERATO RECORD");
			} else {
				if (dbcur.getDimension() <= 0) {
					eve.write("messaggio");
					eve.write("finale");
				} else {
					if (tipo.equals("S")) {
						mkBodySospensioniTbCasoSospeso(eve, dbcur, par, dbc);
					} else {
						mkBodyConclusioniTbCaso(eve, dbcur, par, dbc);
					}
					eve.write("finale");
				}
			}
			dbcur.close();
			eve.close();

			dbc.close();
			super.close(dbc);
			done = true;
			stampa(punto + "\n Fine esecuzione programma\n");
			//			stampa(punto + "invio \n" + new String(eve.get()) + "\n");
			return eve.get(); // restituisco il bytearray
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoElencoSospConclEJB: " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("FoElencoSospConclEJB.query_intervope(): " + e1);
				}
			}
		}
	}

	private void mkBodyConclusioniTbCaso(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String punto = MIONOME + "mkBodyConclusioniTbCaso ";
		stampa(punto + " processo i dati ");
		String zona = " ";
		String distretto = " ";
		String areadis = " ";
		String zonaOld = "";
		String distrettoOld = "";
		String areadisOld = "";
		String dataConclusione = "";
		String assistito = "";
		String motivo = "";
		Hashtable motivoConclusione = caricaMotivoConclusione(dbc);
		String cartellaOld = "";
		long totAssistitiAreaDis = 0;
		long totAssistitiDistretto = 0;
		long totAssistitiZona = 0;
		long TotaleGenerale = 0;
		String descrZona = "";
		String descrDistretto = "";
		String descrAreaDis = "";

		Hashtable prtDati = new Hashtable();
		String ragg = ISASUtil.getValoreStringa(par, "ragg");
		String descr = "";
		if (ISASUtil.valida(ragg)) {
			if (ragg.equalsIgnoreCase("C")) {
				descr = "Comune ";
			} else {
				descr = "Area distrettuale ";
			}
		}
		prtDati.put("#pace#", descr);
		md.writeSostituisci("iniziotabConclusioni-Html", prtDati);

		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			zona = ISASUtil.getValoreStringa(dbr, "cod_zona");
			distretto = ISASUtil.getValoreStringa(dbr, "cod_distretto");
			areadis = ISASUtil.getValoreStringa(dbr, "descrizione");
			if ((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld)) || (!areadis.equals(areadisOld))) {
				if (!zonaOld.equals("")) {
					md.write("finetabConclusioni");

					if (!areadis.equals(areadisOld)) {
						prtDati.put("#totale_descr#", "Totale assisititi per " + descr + " ");
						prtDati.put("#info#", descrAreaDis);
						prtDati.put("#totAssistiti#", totAssistitiAreaDis + "");
						md.writeSostituisci("totaliConclusioni", prtDati);
						totAssistitiAreaDis = 0;
					}
					if (!distretto.equals(distrettoOld)) {
						prtDati.put("#totale_descr#", "Totale assisititi per distretto ");
						prtDati.put("#info#", descrDistretto);
						prtDati.put("#totAssistiti#", totAssistitiDistretto + "");
						md.writeSostituisci("totaliConclusioni", prtDati);
						totAssistitiDistretto = 0;
					}
					if (!zona.equals(zonaOld)) {
						prtDati.put("#totale_descr#", "Totale assisititi per zona ");
						prtDati.put("#info#", descrZona);
						prtDati.put("#totAssistiti#", totAssistitiZona + "");
						md.writeSostituisci("totaliConclusioni", prtDati);
						totAssistitiZona = 0;
					}
				}

				descrZona = ISASUtil.getValoreStringa(dbr, "descrizione_zona");
				descrDistretto = ISASUtil.getValoreStringa(dbr, "des_distr");
				descrAreaDis = ISASUtil.getValoreStringa(dbr, "descrizione");

				Hashtable hZ = new Hashtable();

				hZ.put("#descrizione_zona#", descrZona);
				hZ.put("#des_distr#", descrDistretto);
				hZ.put("#descrizione#", descrAreaDis);
				hZ.put("#pace#", descr);
				md.writeSostituisci("zona", (Hashtable) hZ.clone());
				md.writeSostituisci("zonaConclusioni", hZ);

				zonaOld = zona;
				distrettoOld = distretto;
				areadisOld = areadis;
				//				descrZona = ISASUtil.getValoreStringa(dbr, "descrizione_zona");
				//				descrDistretto = ISASUtil.getValoreStringa(dbr, "des_distr");
				//				descrAreaDis = ISASUtil.getValoreStringa(dbr, "descrizione");
				md.write("iniziotabConclusioni");
			}

			String cartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
			dataConclusione = ISASUtil.getValoreStringa(dbr, "dt_conclusione");
			motivo = ISASUtil.getValoreStringa(dbr, "motivo");
			motivo = ISASUtil.getValoreStringa(motivoConclusione, motivo);

			Hashtable htAss = new Hashtable();
			if (!cartella.equals(cartellaOld)) {
				totAssistitiZona += 1;
				TotaleGenerale += 1;
				totAssistitiDistretto += 1;
				totAssistitiAreaDis += 1;
			}
			cartellaOld = cartella;

			assistito = ISASUtil.getValoreStringa(dbr, "cognome");
			assistito += (ISASUtil.valida(assistito) ? " " : "") + ISASUtil.getValoreStringa(dbr, "nome");

			htAss.put("#descrizione_zona#", descrZona);
			htAss.put("#des_distr#", descrDistretto);
			htAss.put("#descrizione#", descrAreaDis);

			htAss.put("#n_cartella#", cartella);
			htAss.put("#assistito#", assistito);
			htAss.put("#dt_conclusione#", formattaData(dataConclusione));
			htAss.put("#motivo#", motivo);

			md.writeSostituisci("tabellaConclusioni", htAss);
		}
		md.write("finetabConclusioni");

		prtDati.put("#totale_descr#", "Totale assisititi per " + descr + " ");
		prtDati.put("#info#", descrAreaDis);
		prtDati.put("#totAssistiti#", totAssistitiAreaDis + "");
		md.writeSostituisci("totaliConclusioni", prtDati);

		prtDati.put("#totale_descr#", "Totale assisititi per distretto ");
		prtDati.put("#info#", descrDistretto);
		prtDati.put("#totAssistiti#", totAssistitiDistretto + "");
		md.writeSostituisci("totaliConclusioni", prtDati);

		prtDati.put("#totale_descr#", "Totale assisititi per zona ");
		prtDati.put("#info#", descrZona);
		prtDati.put("#totAssistiti#", totAssistitiZona + "");
		md.writeSostituisci("totaliConclusioni", prtDati);

		prtDati.put("#totale_descr#", "");
		prtDati.put("#info#", "Totale Generale Assistiti ");
		prtDati.put("#totAssistiti#", TotaleGenerale + "");
		md.writeSostituisci("totaliConclusioni", (Hashtable) (prtDati.clone()));
		md.writeSostituisci("totaliConclusioni-Html", prtDati);
	}

	private Hashtable caricaMotivoConclusione(ISASConnection dbc) {
		String punto = MIONOME + "caricaMotivoConclusione ";

		String query = "SELECT * FROM TAB_VOCI WHERE TAB_COD = 'FTMOTCON'";
		stampa(punto + " Query>" + query + "\n");
		Hashtable motivoConclusione = caricaHashtable(dbc, query);
		stampa(punto + "ho caricato descrizioni>" + motivoConclusione + "<\n");

		return motivoConclusione;
	}

	private void mkBodySospensioniTbCasoSospeso(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String punto = MIONOME + "mkBodySospensioniTbCasoSospeso ";
		stampa(punto + " processo i dati ");
		String zona = " ";
		String distretto = " ";
		String areadis = " ";
		String zonaOld = "";
		String distrettoOld = "";
		String areadisOld = "";

		String dtInizioPeriodo = ISASUtil.getValoreStringa(par, "d1");
		String dtFinePeriodo = ISASUtil.getValoreStringa(par, "d2");
		Calendar calDataInizioPeriodo = recuperaGiorno(dtInizioPeriodo);
		Calendar calDataFinePeriodo = recuperaGiorno(dtFinePeriodo);

		String dataInizio = "";
		String dataFine = "";
		int nGiorni = 0;
		String assistito = "";
		String motivo = "";
		String cartellaOld = "";
		Hashtable motivoSospensione = caricaMotivoSospensione(dbc);

		long totGiorniSospesiAreaDis = 0;
		long totGiorniSospesiDistretto = 0;
		long totGiorniSospesiZona = 0;

		long totAssistitiAreaDis = 0;
		long totAssistitiDistretto = 0;
		long totAssistitiZona = 0;
		long TotaleGeneraleAssistiti = 0;
		long TotaleGeneraleGiornoSospesi = 0;
		String descrZona = "";
		String descrDistretto = "";
		String descrAreaDis = "";

		Hashtable prtDati = new Hashtable();
		String ragg = ISASUtil.getValoreStringa(par, "ragg");
		String descr = "";
		if (ISASUtil.valida(ragg)) {
			if (ragg.equalsIgnoreCase("C")) {
				descr = "Comune ";
			} else {
				descr = "Area distrettuale ";
			}
		}
		prtDati.put("#pace#", descr);
		md.writeSostituisci("iniziotabSospensioni-Html", prtDati);

		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			zona = ISASUtil.getValoreStringa(dbr, "cod_zona");
			distretto = ISASUtil.getValoreStringa(dbr, "cod_distretto");
			areadis = ISASUtil.getValoreStringa(dbr, "codice");
			prtDati = new Hashtable();

			if ((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld)) || (!areadis.equals(areadisOld))) {
				if (!zonaOld.equals("")) {
					md.write("finetabSospensioni");
					if (!areadis.equals(areadisOld)) {
						prtDati.put("#descrizione#", "Totale assisititi per " + descr + " ");
						prtDati.put("#info#", descrAreaDis);
						prtDati.put("#totAssistiti#", totAssistitiAreaDis + "");
						prtDati.put("#totSospesi#", " giorni sospensioni " + totGiorniSospesiAreaDis + "");
						md.writeSostituisci("totaliSospesi", prtDati);
						totGiorniSospesiAreaDis = 0;
						totAssistitiAreaDis = 0;
					}
					if (!distretto.equals(distrettoOld)) {
						prtDati.put("#descrizione#", "Totale assisititi per distretto ");
						prtDati.put("#info#", descrDistretto);
						prtDati.put("#totAssistiti#", totAssistitiDistretto + "");
						prtDati.put("#totSospesi#", " giorni sospensioni " + totGiorniSospesiDistretto);
						md.writeSostituisci("totaliSospesi", prtDati);
						totGiorniSospesiDistretto = 0;
						totAssistitiDistretto = 0;
					}
					if (!zona.equals(zonaOld)) {
						prtDati.put("#descrizione#", "Totale assisititi per zona ");
						prtDati.put("#info#", descrZona);
						prtDati.put("#totAssistiti#", totAssistitiZona + "");
						prtDati.put("#totSospesi#", " giorni sospensioni " + totGiorniSospesiZona + "");
						md.writeSostituisci("totaliSospesi", prtDati);
						totGiorniSospesiZona = 0;
						totAssistitiZona = 0;
					}
				}

				Hashtable hZ = new Hashtable();
				descrZona = ISASUtil.getValoreStringa(dbr, "descrizione_zona");
				descrDistretto = ISASUtil.getValoreStringa(dbr, "des_distr");
				descrAreaDis = ISASUtil.getValoreStringa(dbr, "descrizione");

				hZ.put("#descrizione_zona#", descrZona);
				hZ.put("#des_distr#", descrDistretto);
				hZ.put("#descrizione#", descrAreaDis);
				hZ.put("#pace#", descr);
				md.writeSostituisci("zona", (Hashtable) hZ.clone());
				md.writeSostituisci("zonaSospensioni", hZ);

				zonaOld = zona;
				distrettoOld = distretto;
				areadisOld = areadis;

				md.write("iniziotabSospensioni");
			}

			String cartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
			dataInizio = recuperaDataPeriodo(dbr, "dt_inizio", dtInizioPeriodo, calDataInizioPeriodo);
			dataFine = recuperaDataPeriodo(dbr, "dt_fine", dtFinePeriodo, calDataFinePeriodo);
			nGiorni = calcolaDifferenzaGiorni(dataInizio, dataFine) + 1;
			motivo = ISASUtil.getValoreStringa(dbr, "motivo");
			motivo = ISASUtil.getValoreStringa(motivoSospensione, motivo);

			Hashtable htAss = new Hashtable();
			htAss.put("#n_cartella#", cartella);
			if (!cartella.equals(cartellaOld)) {
				totAssistitiAreaDis += 1;
				totAssistitiDistretto += 1;
				totAssistitiZona += 1;
				TotaleGeneraleAssistiti += 1;

				cartellaOld = cartella;
			}

			assistito = ISASUtil.getValoreStringa(dbr, "cognome");
			assistito += (ISASUtil.valida(assistito) ? " " : "") + ISASUtil.getValoreStringa(dbr, "nome");

			htAss.put("#descrizione_zona#", descrZona);
			htAss.put("#des_distr#", descrDistretto);
			htAss.put("#descrizione#", descrAreaDis);

			htAss.put("#assistito#", assistito);
			htAss.put("#dt_inizio#", formattaData(dataInizio));
			htAss.put("#dt_fine#", formattaData(dataFine));
			totGiorniSospesiAreaDis += (nGiorni);
			totGiorniSospesiDistretto += (nGiorni);
			totGiorniSospesiZona += (nGiorni);
			TotaleGeneraleGiornoSospesi += (nGiorni);
			htAss.put("#n_giorni#", "" + (nGiorni));
			htAss.put("#motivo#", motivo);

			md.writeSostituisci("tabellaSospensioni", htAss);
		}
		md.write("finetabSospensioni");
		prtDati.put("#descrizione#", "Totale assisititi per " + descr + " ");
		prtDati.put("#info#", descrAreaDis);
		prtDati.put("#totAssistiti#", totAssistitiAreaDis + "");
		prtDati.put("#totSospesi#", " giorni sospensioni " + totGiorniSospesiAreaDis + "");
		md.writeSostituisci("totaliSospesi", prtDati);

		prtDati.put("#descrizione#", "Totale assisititi per distretto ");
		prtDati.put("#info#", descrDistretto);
		prtDati.put("#totAssistiti#", totAssistitiDistretto + "");
		prtDati.put("#totSospesi#", " giorni sospensioni " + totGiorniSospesiDistretto + "");
		md.writeSostituisci("totaliSospesi", prtDati);

		prtDati.put("#descrizione#", "Totale assisititi per zona ");
		prtDati.put("#info#", descrZona);
		prtDati.put("#totAssistiti#", totAssistitiZona + "");
		prtDati.put("#totSospesi#", " giorni sospensioni " + totGiorniSospesiZona + "");
		md.writeSostituisci("totaliSospesi", prtDati);

		prtDati.put("#descrizione#", "");
		prtDati.put("#info#", "Totale assisititi " + TotaleGeneraleAssistiti + " Totale giorni sospensioni " + TotaleGeneraleGiornoSospesi);
		prtDati.put("#totAssistiti#", "");
		prtDati.put("#totSospesi#", "");
		md.writeSostituisci("totaliSospesi", (Hashtable) (prtDati.clone()));

		md.writeSostituisci("totaliSospesi-Html", prtDati);

	}

	private Hashtable caricaMotivoSospensione(ISASConnection dbc) {
		String punto = MIONOME + "caricaMotivoSospensione ";
		String query = "SELECT * FROM TAB_VOCI WHERE TAB_COD = 'FTMOTSOS '";
		stampa(punto + " Query>" + query + "\n");
		Hashtable motivoSospesione = caricaHashtable(dbc, query);
		stampa(punto + "ho caricato descrizioni>" + motivoSospesione + "<\n");

		return motivoSospesione;
	}

	private Hashtable caricaHashtable(ISASConnection dbc, String query) {
		String punto = MIONOME + "caricaHashtable ";
		Hashtable hashtable = new Hashtable();
		ISASCursor dbcur = null;
		try {
			dbcur = dbc.startCursor(query);
			String tabVal, tabDescrizione;
			while (dbcur.next()) {
				ISASRecord dbrTabVoci = dbcur.getRecord();
				tabVal = ISASUtil.getValoreStringa(dbrTabVoci, "tab_val");
				tabDescrizione = ISASUtil.getValoreStringa(dbrTabVoci, "tab_descrizione");
				hashtable.put(tabVal, tabDescrizione);
			}
			dbcur.close();
		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		} finally {
			if (dbcur != null) {
				try {
					dbcur.close();
				} catch (Exception e) {
					stampa(punto + "\n Errore nel chiudere il cursore");
					e.printStackTrace();
				}
			}
		}
		return hashtable;
	}

	private Object decodificaMotivo(ISASConnection dbc, ISASRecord dbr) {
		String descrizioneMotivo = "";

		return descrizioneMotivo;
	}

	private int calcolaDifferenzaGiorni(String dataInizio, String dataFine) {
		String punto = MIONOME + "calcolaDifferenzaGiorni ";
		int nGiorni = 0;
		dateutility du = new dateutility();
		try {
			nGiorni = du.getNGiorni(dataInizio, dataFine) - 2;
		} catch (Exception e) {
			stampa(punto + "Errore nel recuperare il numero giorno dtinizio>" + dtInizio + "<dtFine>" + dtFine + "<");
			e.printStackTrace();
		}
		return nGiorni;
	}

	private String formattaData(String data) {
		String punto = MIONOME + "formattaData ";
		String dataIta = "";
		try {
			if (ISASUtil.valida(data) && data.trim().length() >= 10) {
				dataIta = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
			}
		} catch (Exception e) {
			stampa(punto + "Errore nel recupero data>" + data + "<");
		}
		return dataIta;
	}

	private Calendar recuperaGiorno(String data) {
		String punto = " recupeaGiorno";
		int giorno = -1, mese = -1, anno = -1;
		Calendar calender = null;
		if (ISASUtil.valida(data)) {
			int pos = data.indexOf("-");
			if (pos > 0) {
				anno = getIntero(data.substring(0, 4));
			}
			String val = data.substring(pos + 1);
			pos = val.indexOf("-");
			if (pos > 0) {
				mese = getIntero(val.substring(0, pos));
				giorno = getIntero(val.substring(pos + 1));
			}
			mese = (mese > 0 ? mese - 1 : mese);
			calender = new GregorianCalendar(anno, mese, giorno);
		} else {
			stampa(punto + "\n Data non valida\n ");
		}
		return calender;
	}

	private static int getIntero(String numero) {
		int valore = -1;

		try {
			valore = Integer.parseInt(numero);
		} catch (Exception e) {
		}
		return valore;
	}

	private String recuperaDataPeriodo(ISASRecord predbr, String key, String dataPeriodo, Calendar calDataPeriodo) {
		String punto = MIONOME + "recuperaDataPeriodo ";
		String dataInizio = "";
		String dtdbInizio = ISASUtil.getValoreStringa(predbr, key);

		if (ISASUtil.valida(dtdbInizio)) {
			Calendar caldbInizio = recuperaGiorno(dtdbInizio);
			if (caldbInizio.before(calDataPeriodo)) {
				dataInizio = dataPeriodo;
			} else {
				dataInizio = dtdbInizio;
			}
		} else {
			dataInizio = dataPeriodo;
		}

		return dataInizio;
	}

	private String getSelectConclusiTbCaso(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getSelectConclusiTbCaso ";
		String dtInizio = ISASUtil.getValoreStringa(par, "d1");
		String dtFine = ISASUtil.getValoreStringa(par, "d2");
		ServerUtility su = new ServerUtility();

		String query = "SELECT u.cod_zona,u.cod_distretto, u.des_zona descrizione_zona , u.codice,u.descrizione,u.des_distretto des_distr, "
				+ "c.cognome,c.nome, c.data_nasc, c.n_cartella, cs.dt_presa_carico, cs.dt_conclusione, cs.motivo, a.dom_citta, a.dom_indiriz, "
				+ " a.citta,a.indirizzo,a.nome_camp "
				+ " FROM caso cs, anagra_c a, cartella c, "
				+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
				+ " u "
				+ " WHERE cs.dt_conclusione <= "
				+ (ISASUtil.valida(dtFine) ? dbc.formatDbDate(dtFine) : "")
				+ " and cs.dt_conclusione >= "
				+ (ISASUtil.valida(dtInizio) ? dbc.formatDbDate(dtInizio) : "") + " ";

		String ragg = ISASUtil.getValoreStringa(par, "ragg");
		String myselect = " AND a.n_cartella = cs.n_cartella ";
		myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

		//Aggiunto Controllo Domicilio/Residenza (BYSP)
		if ((String) par.get("dom_res") == null) {
			if (ragg.equals("C"))
				myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
						+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
			else if (ragg.equals("A"))
				myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
						+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.dom_citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.dom_areadis";
		}

		else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.areadis";
		}

		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

		myselect += " AND a.n_cartella=c.n_cartella"
				+ " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"
				+ " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella AND anagra_c.data_variazione<= TO_DATE ('2012-02-12', 'YYYY-MM-DD' ) )";

		query += myselect + " order by descrizione_zona,des_distr,descrizione, c.cognome,c.nome ";

		stampa(punto + "Query\n" + query + "\n");

		return query;
	}

	private String getSelectSospesiTbCasoSospeso(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getSelectSospesiTbCasoSospeso ";
		String dtInizio = ISASUtil.getValoreStringa(par, "d1");
		String dtFine = ISASUtil.getValoreStringa(par, "d2");
		ServerUtility su = new ServerUtility();

		String query = "SELECT u.cod_zona,u.cod_distretto, u.des_zona descrizione_zona , u.codice,u.descrizione,u.des_distretto des_distr, "
				+ "c.cognome,c.nome, c.data_nasc, c.n_cartella, dt_inizio, dt_fine, motivo, a.dom_citta, a.dom_indiriz, "
				+ " a.citta,a.indirizzo,a.nome_camp "
				+ " FROM caso_sospensione cs, anagra_c a, cartella c, "
				+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
				+ " u "
				+ " WHERE   ( (dt_fine is null ) or (dt_fine <= "
				+ (ISASUtil.valida(dtFine) ? dbc.formatDbDate(dtFine) : "")

				+ " ) ) and ( (dt_fine is null ) or (dt_fine >= "
				+ (ISASUtil.valida(dtInizio) ? dbc.formatDbDate(dtInizio) : "")
				+ " )  ) and ( (dt_inizio is null) or (dt_inizio >= "
				+ (ISASUtil.valida(dtInizio) ? dbc.formatDbDate(dtInizio) : "")
				+ " ) ) "
				+ " and ( (dt_inizio is null) or (dt_inizio <= "
				+ (ISASUtil.valida(dtFine) ? dbc.formatDbDate(dtFine) : "")
				+ " ) )";

		String ragg = ISASUtil.getValoreStringa(par, "ragg");
		String myselect = " AND a.n_cartella = cs.n_cartella ";
		myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

		//Aggiunto Controllo Domicilio/Residenza (BYSP)
		if ((String) par.get("dom_res") == null) {
			if (ragg.equals("C"))
				myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
						+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
			else if (ragg.equals("A"))
				myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
						+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.dom_citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.dom_areadis";
		}

		else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.areadis";
		}

		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

		myselect += " AND a.n_cartella=c.n_cartella"
				+ " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"
				+ " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella AND anagra_c.data_variazione<= TO_DATE ('2012-02-12', 'YYYY-MM-DD' ) )";

		query += myselect + " order by descrizione_zona,des_distr,descrizione, c.cognome,c.nome ";

		stampa(punto + "Query\n" + query + "\n");
		return query;
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

} // End of FoIntervOpe class

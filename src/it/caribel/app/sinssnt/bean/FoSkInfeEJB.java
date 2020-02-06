package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//ATTENZIONE cartella e contatto racchiusi con '' nelle select(12-07-2004)
// 25/09/2003 - EJB di connessione alla procedura SINS Tabella FoSkInfe
//
// Francesco Greco
// Jessica 06/05/04 *** tolta la data apertura contatto dalle schede
//
// G.Brogi 09/01/06 vers. 05.03 aggiunto akdd_stadio = 0 - guarigione e
//				skdd_diametro = 0
// G.Brogi 12/05/2006 vers. 06.03 aggiunte le pagine di stampa delle schede
//              Nucleo Familiare
//				Caregiver
//				Indice di Braden
//
// 16/11/06 m.: eliminato "n_contatto" e modificato nomi tabelle dai metodi x stampaPagine:
//				ADL, IADL, PFEIFFER, CAREGIVER e BRADEN.
// 24/11/06 m.: aggiunto pagina scala TIQ
// 07/12/06 m.: sostituito campi su SKPATOLOGIE con nuova tabella DIAGNOSI.
// ==========================================================================

import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.caribel.app.sinssnt.comuni_nascita.ComuniNascita;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.caribel.app.sinssnt.util.DecodificheVcoLesioni;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Hashtable;

public class FoSkInfeEJB extends SINSSNTConnectionEJB {
	private String TIPO_OPERATORE_INFERMIERE = "02";

	// 05/02/13
	private EveUtils eveUtl = new EveUtils();
	private static final String MIONOME = "5-FoSkInfeEJB.";
	
	public FoSkInfeEJB() {
	}

	// dati che arrivano dalla chiamata della stampa
	public ISASRecord dbana = null;
	public String n_cartella = "";
	public String n_contatto = "";
	public String data_apertura = "";
	public String data_scheda = "";// arriva solo nel caso di stampa singola
	public String data_chiusura = "";
	public String tipoOperatore = TIPO_OPERATORE_INFERMIERE;
	public String intesta_nome = "";

	// 24/11/06 m.: dati scala TIQ --------------------
	private String[] arrTitDisturbiTIQ = { "Dolore", "Mal di testa",
			"Insonnia", "Problemi nel dormire", "Sonnolenza", "Vertigini",
			"Tremori", "Confusione", "Sensazione di debolezza",
			"Sensazione di stanchezza", "Mancanza di appetito",
			"Bocca asciutta", "Difficolta' ad inghiottire", "Nausea", "Vomito",
			"Dolore allo stomaco", "Difficolta' a digerire", "Diarrea",
			"Stitichezza", "Singhiozzo", "Tosse", "Difficolta' a respirare",
			"Sudorazione", "Prurito", "Altro" };

	private String[] arrTitProblemiTIQ = {
			"E' stato fisicamente male",
			"Ha avuto difficolta' a svolgere il suo lavoro od i mestieri di casa",
			"Ha avuto difficolta' nello svolgere le solite attivita' di tempo libero",
			"Ha avuto bisogno di aiuto per mangiare, vestirsi od andare in bagno",
			"Si e' sentito triste e depresso",
			"Si e' sentito ansioso e spaventato",
			"Si e' sentito nervoso, irrequieto o irritabile",
			"Si e' sentito insicuro",
			"Ha avuto difficolta' di concentrazione o di attenzione",
			"Ha trovato difficile distrarsi",
			"Ha avuto momenti di disaccordo con la famiglia",
			"Si e' sentito isolato dagli altri" };

	private String[] arrDbNameDisturbiTIQ = { "tiq_dolore", "tiq_malditesta",
			"tiq_insonnia", "tiq_dormire", "tiq_sonnolenza", "tiq_vertigini",
			"tiq_tremori", "tiq_confusione", "tiq_debolezza", "tiq_stanchezza",
			"tiq_appetito", "tiq_bocca", "tiq_inghiottire", "tiq_nausea",
			"tiq_vomito", "tiq_stomaco", "tiq_digerire", "tiq_diarrea",
			"tiq_stitichezza", "tiq_singhiozzo", "tiq_tosse", "tiq_respirare",
			"tiq_sudorazione", "tiq_prurito", "tiq_altro" };

	private String[] arrDbNameProblemiTIQ = { "tiq_male",
			"tiq_difficolta_lavoro", "tiq_difficolta_libero", "tiq_aiuto",
			"tiq_depresso", "tiq_ansioso", "tiq_nervoso", "tiq_insicuro",
			"tiq_concentrazione", "tiq_distrarsi", "tiq_famiglia",
			"tiq_isolato" };

	private String[] arrVociKPS = {
			"Normale - Nessun disturbo - Nessuna evidenza di malattia",
			"In grado di svolgere le attivita' comuni - Segni minori di sintomi di malattia",
			"Attivita' normali svolte con sforzo - Alcuni segnali o sintomi di malattia",
			"Preoccupazione per la propria persona - Incapacita' di svolgere attivita' normali o di svolgere un lavoro attivo",
			"Richiede occasionalmente assistenza, ma e' autosufficiente per la maggior parte delle proprie necessita'",
			"Richiede un assistenza considerevole e frequenti cure mediche",
			"Disabile - Richiede cure ed assistenza speciali",
			"Gravemente disabile - E' indicata l'ospedalizzazione sebbene non in pericolo di vita",
			"Molto malato - Necessita' dell'ospedalizzazione - Necessita' di un trattamento di supporto attivo",
			"Agonizzante - Processi fatali in rapida evoluzione", "Morte" };

	private String[] arrValTIQ = { "NO", "UN PO'", "MOLTO", "MOLTISSIMO" };

	

	// 24/11/06 m.: dati scala TIQ --------------------

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

	private String dataIta(String dateita) {
		if (dateita != null) {
			if (dateita.length() == 10)
				dateita = dateita.substring(8, 10) + "/"
						+ dateita.substring(5, 7) + "/"
						+ dateita.substring(0, 4);
		}
		return dateita;
	}

	/**
	 * restituisce un campo data come stringa
	 */
	private String getDateField(ISASRecord dbr, String f) {
		try {
			if (dbr.get(f) == null)
				return "";
			String d = ((java.sql.Date) dbr.get(f)).toString();
			d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/"
					+ d.substring(0, 4);
			return d;
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	/***
	 * /* Controlla il valore della casella e il valore preso dal DB /* e se
	 * corrispondono inserisce la X nel campo a video /*
	 */
	private String faiCrocetta(String valore, String crocetta) {
		String stringa = "";
		if (!crocetta.equals("")) {
			if (crocetta.equals(valore))
				stringa = "X";
		}
		return stringa;
	}

	/***
	 * /* Restituisce l'hashtable con i valori vuoti da stampare se non trova il
	 * record
	 */
	private Hashtable faiVuota(Hashtable h, String[] campi) {
		for (int i = 0; i < campi.length; i++) {
			h.put("#" + campi[i] + "#", "");
		}
		return h;
	}

	/***
	 * /* Controlla il valore preso dal DB e inserisco nell'Hashtable /* il
	 * check del campo SI o NO
	 */
	private Hashtable faiSiNo(Hashtable h, String campoFo, String crocetta) {
		if (!crocetta.equals("")) {
			if (crocetta.equals("S")) {
				h.put("#" + campoFo + "_s#", "X");
				h.put("#" + campoFo + "_n#", "");
			} else if (crocetta.equals("N")) {
				h.put("#" + campoFo + "_s#", "");
				h.put("#" + campoFo + "_n#", "X");
			} else {
				h.put("#" + campoFo + "_s#", "");
				h.put("#" + campoFo + "_n#", "");
			}
		} else {
			h.put("#" + campoFo + "_s#", "");
			h.put("#" + campoFo + "_n#", "");
		}
		return h;
	}

	private String getDecodifica(ISASConnection dbc, String campo1,
			String campo2, String tabella, String codice) throws Exception {
		String decod = "";
		try {
			if (!codice.equals("")) {
				String sel = "SELECT " + campo1 + " FROM " + tabella
						+ " WHERE " + campo2 + " = '" + codice + "'";
				debugMessage("FoSkInfeEJB.getDecodifica(): " + sel);
				ISASRecord dbcom = dbc.readRecord(sel);
				decod = (String) dbcom.get(campo1);
			}
		} catch (Exception e) {
			debugMessage("getDecodifica(" + dbc + ", " + codice + "): " + e);
			return "";
		}
		return decod;
	}

	private String getOperatore(ISASConnection dbc, String codice)
			throws Exception {
		String decod = "";
		try {
			String sel = "SELECT cognome,nome FROM operatori "
					+ "WHERE codice = '" + codice + "'";
			debugMessage("FoEleSocEJB.getOperatore(): " + sel);
			ISASRecord dbcom = dbc.readRecord(sel);
			String cognome = (String) dbcom.get("cognome");
			String nome = (String) dbcom.get("nome");
			decod = cognome + " " + nome;
		} catch (Exception e) {
			debugMessage("getOperatore(" + dbc + ", " + decod + "): " + e);
			return "";
		}
		return decod;
	}

	private String getMedico(ISASConnection dbc, String codice)
			throws Exception {
		String decod = "";
		try {
			String sel = "SELECT mecogn,menome FROM medici "
					+ "WHERE mecodi = '" + codice + "'";
			debugMessage("FoSkInfeEJB.getMedico(): " + sel);
			ISASRecord dbcom = dbc.readRecord(sel);
			String cognome = (String) dbcom.get("mecogn");
			String nome = (String) dbcom.get("menome");
			decod = cognome + " " + nome;
		} catch (Exception e) {
			debugMessage("getMedico(" + dbc + ", " + decod + "): " + e);
			return "";
		}
		return decod;
	}

	// 07/12/06
	private String getDiagnosi(ISASConnection dbc, String codice)
			throws Exception {
		String decod = "";
		try {
			String sel = "SELECT diagnosi FROM tab_diagnosi "
					+ "WHERE cod_diagnosi = '" + codice + "'";
			debugMessage("FoSkInfeEJB.getDiagnosi(): " + sel);
			ISASRecord dbcom = dbc.readRecord(sel);
			decod = (String) dbcom.get("diagnosi");
		} catch (Exception e) {
			debugMessage("getDiagnosi(" + decod + "): " + e);
			return "";
		}
		return decod;
	}

	private void queryEsenzioni(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT  es_data_inizio,es_data_fine,cod_esenzione FROM anagra_esenzioni WHERE cod_usl='"
					+ (String) ((ISASRecord) dbr).get("cod_usl") + "'";
			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbese = dbcur.getRecord();

					ht.put("#data_inizio_ese#", getDateField(dbese,
							"es_data_inizio"));
					ht.put("#data_fine_ese#", getDateField(dbese,
							"es_data_fine"));
					ht.put("#esenzione_des#", getDecodifica(dbc, "descrizione",
							"cod_esenzione", "esenzioni", getStringField(dbese,
									"cod_esenzione")));
					doc.writeSostituisci("esenzioni", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_inizio_ese#", "");
				ht.put("#data_fine_ese#", "");
				ht.put("#esenzione_des#", "");
				doc.writeSostituisci("esenzioni", ht);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una query_esenzioni()  ");
		}

	}

	private void queryAusili(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT skad_data_rich,skad_tipo_rich,skad_data_forn,skad_tipo_forn "
					+ " FROM skiausili_d "
					+ " WHERE n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND n_contatto='"
					+ n_contatto
					+ "'"
					+
					// " AND ski_data_apertura="+formatDate(dbc,data_apertura)+
					" AND skat_data="
					+ formatDate(dbc, ((java.sql.Date) dbr.get("skat_data"))
							.toString());
			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbska = dbcur.getRecord();
					ht
							.put("#data_rich#", getDateField(dbska,
									"skad_data_rich"));
					ht.put("#tipo_aus_rich#", getTipoAusilio(getStringField(
							dbska, "skad_tipo_rich")));
					ht
							.put("#data_forn#", getDateField(dbska,
									"skad_data_forn"));
					ht.put("#tipo_aus_forn#", getTipoAusilio(getStringField(
							dbska, "skad_tipo_forn")));
					doc.writeSostituisci("ausili", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_rich#", "");
				ht.put("#tipo_aus_rich#", "");
				ht.put("#data_forn#", "");
				ht.put("#tipo_aus_forn#", "");
				doc.writeSostituisci("ausili", ht);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryAusili()  ");
		}

	}

	private void queryDiagnEscret(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT skg_data_diag,skg_sintomi,skg_indagine,skg_risultato,skg_provvedimenti "
					+ " FROM skidiagnosi "
					+ " WHERE n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND n_contatto='"
					+ n_contatto
					+ "'"
					+
					// " AND ski_data_apertura="+formatDate(dbc,data_apertura)+
					" AND ske_data="
					+ formatDate(dbc, ((java.sql.Date) dbr.get("ske_data"))
							.toString());

			ISASCursor dbcur = dbc.startCursor(select);
			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbske = dbcur.getRecord();
					ht
							.put("#data_diagn#", getDateField(dbske,
									"skg_data_diag"));
					ht.put("#sintomi#", getStringField(dbske, "skg_sintomi"));
					ht.put("#indagine#", getIndagine(getStringField(dbske,
							"skg_indagine")));
					ht.put("#risultato#",
							getStringField(dbske, "skg_risultato"));
					ht.put("#provvedimento#", getStringField(dbske,
							"skg_provvedimenti"));
					doc.writeSostituisci("diagnEscret", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_diagn#", "");
				ht.put("#sintomi#", "");
				ht.put("#indagine#", "");
				ht.put("#risultato#", "");
				ht.put("#provvedimento#", "");
				doc.writeSostituisci("diagnEscret", ht);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryDiagnEscret()  ");
		}

	}

	private void queryLesioniDecub(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT skdd_sede,skdd_stadio,skdd_diametro,skdd_data_val,skdd_data_guarigione "
					+ " FROM skidecubito_d "
					+ " WHERE n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND n_contatto='"
					+ n_contatto
					+ "'"
					+
					// " AND ski_data_apertura="+formatDate(dbc,data_apertura)+
					" AND skdt_data="
					+ formatDate(dbc, ((java.sql.Date) dbr.get("skdt_data"))
							.toString());
			ISASCursor dbcur = dbc.startCursor(select);
			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbdec = dbcur.getRecord();
					String diametro = "";
					ht.put("#data_val#", getDateField(dbdec, "skdd_data_val"));
					ht.put("#data_guar#", getDateField(dbdec,
							"skdd_data_guarigione"));
					ht.put("#sede#", getSedeLesDecub(getStringField(dbdec,
							"skdd_sede")));
					ht.put("#stadio#", getStadioLesione(getStringField(dbdec,
							"skdd_stadio")));
					if (getStringField(dbdec, "skdd_diametro").equals("1"))
						diametro = "minore di 5";
					else if (getStringField(dbdec, "skdd_diametro").equals("2"))
						diametro = "tra 5 e 10";
					else if (getStringField(dbdec, "skdd_diametro").equals("3"))
						diametro = "maggiore di 10";
					else if (getStringField(dbdec, "skdd_diametro").equals("0"))
						diametro = "";
					ht.put("#diametro#", diametro);
					doc.writeSostituisci("lesioniDecub", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_val#", "");
				ht.put("#sede#", "");
				ht.put("#stadio#", "");
				ht.put("#diametro#", "");
				doc.writeSostituisci("lesioniDecub", ht);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryLesioneDecub()  ");
		}

	}

	private void queryUlcerePie(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT skud_sede,skud_stadio,skud_data_val,skud_data_guarigione "
					+ " FROM skiulcere_d "
					+ " WHERE n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND n_contatto='"
					+ n_contatto
					+ "'"
					+
					// " AND ski_data_apertura="+formatDate(dbc,data_apertura)+
					" AND skut_data="
					+ formatDate(dbc, ((java.sql.Date) dbr.get("skut_data"))
							.toString());
			ISASCursor dbcur = dbc.startCursor(select);
			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbdec = dbcur.getRecord();
					String diametro = "";
					ht.put("#data_val#", getDateField(dbdec, "skud_data_val"));
					ht.put("#skud_data_guarigione#", getDateField(dbdec,
							"skud_data_guarigione"));
					ht.put("#sede#", getSedeLesUlcerePie(getStringField(dbdec,
							"skud_sede")));
					ht.put("#stadio#", getStadioLesione(getStringField(dbdec,
							"skud_stadio")));
					doc.writeSostituisci("ulcerePie", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_val#", "");
				ht.put("#sede#", "");
				ht.put("#stadio#", "");
				doc.writeSostituisci("ulcerePie", ht);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryUlcerePie()  ");
		}

	}

	private void queryLesioniVasco(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT skvd_sede,skvd_stadio,skvd_periles,skvd_data_val,"
					+ "skvd_bendaggio,skvd_data_guarigione"
					+ " FROM skivasco_d "
					+ " WHERE n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND n_contatto='"
					+ n_contatto
					+ "'"
					+ " AND skvt_data="
					+ formatDate(dbc, ((java.sql.Date) dbr.get("skvt_data"))
							.toString());
			ISASCursor dbcur = dbc.startCursor(select);
			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbdec = dbcur.getRecord();
					ht.put("#data_val#", getDateField(dbdec, "skvd_data_val"));
					ht.put("#sede#", getSedeLesVasco(getStringField(dbdec,
							"skvd_sede")));
					ht.put("#stadio#", getStadioLesione(getStringField(dbdec,
							"skvd_stadio")));
					ht.put("#tessuto#", getTessutoLesione(getStringField(dbdec,
							"skvd_periles")));
					ht.put("#skvd_bendaggio#", getStringField(dbdec,
							"skvd_bendaggio"));
					ht.put("#skvd_data_guarigione#", getDateField(dbdec,
							"skvd_data_guarigione"));
					doc.writeSostituisci("lesioniVasco", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_val#", "");
				ht.put("#sede#", "");
				ht.put("#stadio#", "");
				ht.put("#tessuto#", "");
				doc.writeSostituisci("lesioniVasco", ht);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryLesioniVasco()  ");
		}

	}

	private void queryPianoInterv(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc, String paData) throws SQLException {
		String punto = MIONOME + "queryPianoInterv";
		Hashtable ht = new Hashtable();
		ISASCursor dbcur=null;
		try {
//			String select = "SELECT pi_data_inizio,pi_prest_cod,pi_data_fine,pi_freq,pi_modalita "
//					+ " FROM pianointerv  WHERE n_cartella='"+ n_cartella+ "'"
//					+ " AND n_contatto='"+ n_contatto+ "'"+ " AND pi_tipo_oper='02'"+
//					// " AND ski_data_apertura="+formatDate(dbc,data_apertura)+
//					" AND skpa_data="+ formatDate(dbc, ((java.sql.Date) dbr.get("pa_data")).toString());
			String select = "select pi_data_inizio,pi_data_fine, pi_prest_cod, pi_freq, pi_modalita " +
					" from piano_accessi where n_cartella = " +n_cartella + " and n_progetto = " +
							n_contatto +" and pa_tipo_oper = '" +tipoOperatore+"' and pa_data = "+formatDate(dbc, paData);
		stampa(punto + " query>"+select+ "<");
			
			dbcur = dbc.startCursor(select);

			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbdec = dbcur.getRecord();
					ht.put("#data_ini_int#", getDateField(dbdec,"pi_data_inizio"));
					ht.put("#data_fine_int#", getDateField(dbdec, "pi_data_fine"));
					ht.put("#mod_ind#", getStringField(dbdec, "pi_modalita"));
					ht.put("#modalita_int#", decodifica("prestaz", "prest_cod",getStringField(dbdec, "pi_prest_cod"), "prest_des", dbc));
					ht.put("#freq_int#", getFrequenza(getStringField(dbdec, "pi_freq"), dbc));
					doc.writeSostituisci("pianoInt", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_ini_int#", "");
				ht.put("#data_fine_int#", "");
				ht.put("#modalita_int#", "");
				ht.put("#freq_int#", "");
				ht.put("#mod_ind#", "");
				doc.writeSostituisci("pianoInt", ht);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryPianoInterv()  ");
		}finally{
			close_dbcur_nothrow(punto, dbcur);
		}
	}

	private void queryVerifica(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc, String paData ) throws SQLException {
		String punto = MIONOME + "queryVerifica ";
		Hashtable ht = new Hashtable();
		ISASCursor dbcur = null;
		try {
//			String select = "SELECT skve_data,skve_testo "
//					+ " FROM skiverifica " + " WHERE n_cartella='"
//					+ n_cartella+ "'"+ " AND n_contatto='"+ n_contatto+ "'"+// " AND ski_data_apertura="+formatDate(dbc,data_apertura)+
//					" AND skpa_data="+ formatDate(dbc, ((java.sql.Date) dbr.get("pa_data")).toString());
			String select = "select ve_data, ve_testo from piano_verifica where n_cartella = " +n_cartella + 
			" and n_progetto = " +n_contatto + " and pa_tipo_oper = '" +tipoOperatore + "' and pa_data = "+formatDate(dbc, paData);
			
			stampa(punto +  " query>"+select +"<");
			dbcur = dbc.startCursor(select);
			if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbdec = dbcur.getRecord();
					ht.put("#data_ver#", getDateField(dbdec, "ve_data"));
					ht.put("#descr_ver#", getStringField(dbdec, "ve_testo"));
					doc.writeSostituisci("verifica", ht);
				}
			} else {// stampa riga vuota
				ht.put("#data_ver#", "");
				ht.put("#descr_ver#", "");
				doc.writeSostituisci("verifica", ht);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG FoSkInfe :Errore eseguendo una queryVerifica()  ");
		}finally{
			close_dbcur_nothrow(punto, dbcur);
		}

	}

	/**
	 * stampa sintetica-analitica: sezione layout del documento
	 */
	private void mkLayout(ISASConnection dbc, mergeDocument doc) {

		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String cognome = "";
		String nome = "";
		String tipo = "";
		try {
			ht.put("#txt#", getConfStringField(dbc));
		} catch (Exception e) {
			ht.put("#txt#", " ");
		}
		ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		doc.writeSostituisci("layout", ht);
	}

	private String getConfStringField(ISASConnection dbc) throws Exception {
		String sel = "SELECT conf_txt from conf "
				+ "WHERE conf_kproc = 'SINS' AND "
				+ "conf_key='ragione_sociale'";
		debugMessage("FoSkInfeEJB.getRagioneSociale(): " + sel);
		ISASRecord dbconf = dbc.readRecord(sel);
		return (String) dbconf.get("conf_txt");
	}

	private void leggiAnagrafica(ISASConnection dbc) throws SQLException {
		try {

			String nome = "";
			String cognome = "";

			String sel = "SELECT ac.*, c.* "
					+ " FROM cartella c, anagra_c ac"
					+ " WHERE c.n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND ac.n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND ac.data_variazione IN ( SELECT MAX (data_variazione) "
					+ " FROM anagra_c WHERE n_cartella ='" + n_cartella + "')";

			dbana = dbc.readRecord(sel);
			if (dbana != null) {
				cognome = getStringField(dbana, "cognome");
				nome = getStringField(dbana, "nome");
				intesta_nome = cognome + " " + nome;
			}

		} catch (Exception e) {
			debugMessage("leggiAnagrafica(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaIniziale(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		ISASRecord dbana = null;
		String cartella = "";
		String data_nasc = "";
		String cod_sanitario = "";
		String cod_fiscale = "";
		String comune_nascita = "";
		String cittadinanza = "";
		String cognome = "";
		String nome = "";
		String residenza = "";
		String comune_res = "";
		String prov_res = "";
		String local_res = "";
		String domicilio = "";
		String comune_dom = "";
		String tel_dom = "";
		String prov_dom = "";
		String local_dom = "";
		String nome_camp = "";

		String sessoF = "";
		String sessoM = "";
		try {
			String sel = "SELECT ac.*, c.* "
					+ " FROM cartella c, anagra_c ac"
					+ " WHERE c.n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND ac.n_cartella='"
					+ n_cartella
					+ "'"
					+ " AND ac.data_variazione IN ( SELECT MAX (data_variazione) "
					+ " FROM anagra_c WHERE n_cartella ='" + n_cartella + "')";

			dbana = dbc.readRecord(sel);

			cartella = getStringField(dbana, "n_cartella");
			data_nasc = getDateField(dbana, "data_nasc");
			cod_sanitario = getStringField(dbana, "cod_reg");
			cod_fiscale = getStringField(dbana, "cod_fisc");
//			comune_nascita = getDecodifica(dbc, "descrizione", "codice",
//					"comuni", getStringField(dbana, "cod_com_nasc"));
			String codComNascita = ISASUtil.getValoreStringa(dbana,"cod_com_nasc");
			String dtNascita = ISASUtil.getValoreStringa(dbana, "data_nasc");
			comune_nascita = ComuniNascita.getDecodeComuneNascita(dbc, codComNascita, dtNascita);
			
			cittadinanza = getDecodifica(dbc, "des_cittadin", "cd_cittadin",
					"cittadin", getStringField(dbana, "cittadinanza"));
			cognome = getStringField(dbana, "cognome");
			nome = getStringField(dbana, "nome");
			residenza = getStringField(dbana, "indirizzo");
			comune_res = getDecodifica(dbc, "descrizione", "codice", "comuni",
					getStringField(dbana, "citta"));
			tel_dom = getStringField(dbana, "telefono1");
			prov_res = getStringField(dbana, "prov");
			local_res = getStringField(dbana, "localita");
			domicilio = getStringField(dbana, "dom_indiriz");
			comune_dom = getDecodifica(dbc, "descrizione", "codice", "comuni",
					getStringField(dbana, "dom_citta"));
			prov_dom = getStringField(dbana, "dom_prov");
			local_dom = getStringField(dbana, "dom_localita");
			nome_camp = getStringField(dbana, "nome_camp");
			sessoF = faiCrocetta("F", getStringField(dbana, "sesso"));
			sessoM = faiCrocetta("M", getStringField(dbana, "sesso"));

		} catch (Exception e) {
			debugMessage("scriviPaginaIniziale(" + dbc + " ) : " + e);
		}

		Hashtable ht = new Hashtable();
		intesta_nome = cognome + " " + nome;
		ht.put("#n_cartella#", cartella);
		ht.put("#data_nasc#", data_nasc);
		ht.put("#cod_sanitario#", cod_sanitario);
		ht.put("#cod_fiscale#", cod_fiscale);
		ht.put("#comune_nascita#", comune_nascita);
		ht.put("#cittadinanza#", cittadinanza);
		ht.put("#cognome#", cognome);
		ht.put("#nome#", nome);
		ht.put("#residenza#", residenza);
		ht.put("#comune_res#", comune_res);
		ht.put("#tel_dom#", tel_dom);
		ht.put("#prov_res#", prov_res);
		ht.put("#local_res#", local_res);
		ht.put("#domicilio#", domicilio);
		ht.put("#comune_dom#", comune_dom);
		ht.put("#tel_dom#", tel_dom);
		ht.put("#prov_dom#", prov_dom);
		ht.put("#local_dom#", local_dom);
		ht.put("#nome_camp#", nome_camp);
		ht.put("#sessof#", sessoF);
		ht.put("#sessom#", sessoM);
		eve.writeSostituisci("paginaIniziale", ht);
		queryEsenzioni(dbc, dbana, eve);
		eve.write("fineTabPaginaIniziale");
	}

	private void scriviPaginaSchedaInfe(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		ISASRecord dbski = null;
		String data_ape = "";
		String operatore = "";
		String modalita = "";
		String descr_contatto = "";
		String infermiere = "";
		String inviato = "";
		String tipout = "";
		String osp_dim = "";
		String data_dimiss = "";
		String uo_dim = "";
		String trasm_sk_s = "";
		String trasm_sk_n = "";
		String les_dec_s = "";
		String les_dec_n = "";
		String sintesi_do = "";
		String anamnesi_1 = "";
		String anamnesi_2 = "";
		String data_uscita = "";
		String dimissioni = "";
		String trasfer = "";
		String note = "";
		// 05/02/13
		String presidioCont = "";
		
		try {
			String sel = "SELECT * " + " FROM skinf " + " WHERE n_cartella='"
					+ n_cartella + "'" + " AND n_contatto='" + n_contatto + "'"
					+ " AND ski_data_apertura="
					+ formatDate(dbc, data_apertura);
			System.out.println("SELECT CONTROLLO-->" + sel);
			dbski = dbc.readRecord(sel);

			data_ape = getDateField(dbski, "ski_data_apertura");
			operatore = getOperatore(dbc,
					getStringField(dbski, "ski_operatore"));
			modalita = getStringField(dbski, "ski_modalita");
			if (modalita.equals("1")) {
				modalita = "Assistenza Domiciliare";
			} else if (modalita.equals("2")) {
				modalita = "Assistenza Ambulatoriale";
			} else {
				modalita = "";
			}

			descr_contatto = getStringField(dbski, "ski_descr_contatto");
			infermiere = getOperatore(dbc, getStringField(dbski,
					"ski_infermiere"));
			inviato = getDecodifica(dbc, "descrizione", "codice", "segnala",
					getStringField(dbski, "ski_inviato"));
			/*
			 * Giulia Brogi 07/04/05 tipout =
			 * getDecodifica(dbc,"descrizione","codice",
			 * "tipute",getStringField(dbski, "ski_tipout"));
			 */
			tipout = getDecodifica(dbc, "descrizione", "codice", "tipute_s",
					getStringField(dbski, "ski_tipout"));
			osp_dim = getDecodifica(dbc, "descosp", "codosp", "ospedali",
					getStringField(dbski, "ski_osp_dim"));
			data_dimiss = getDateField(dbski, "ski_data_dimiss");
			uo_dim = getDecodifica(dbc, "reparto", "cd_rep", "reparti",
					getStringField(dbski, "ski_uo_dim"));
			trasm_sk_s = faiCrocetta("S", getStringField(dbski, "ski_trasm_sk"));
			trasm_sk_n = faiCrocetta("N", getStringField(dbski, "ski_trasm_sk"));
			les_dec_s = faiCrocetta("S", getStringField(dbski, "ski_les_dec"));
			les_dec_n = faiCrocetta("N", getStringField(dbski, "ski_les_dec"));
			sintesi_do = getStringField(dbski, "ski_sintesi_do");
			anamnesi_1 = getStringField(dbski, "ski_anamnesi_1");
			anamnesi_2 = getStringField(dbski, "ski_anamnesi_2");
			data_uscita = getDateField(dbski, "ski_data_uscita");
			// dimissioni =
			// getMotivoDimissione(((Integer)dbski.get("ski_dimissioni")).toString());
			dimissioni = getMotivoDimissione(dbski, dbc);
			trasfer = getStringField(dbski, "ski_trasfer");
			note = getStringField(dbski, "ski_note");
			
			// 05/02/13
			presidioCont = decodPresidio(dbc, (String)dbski.get("ski_cod_presidio"), (String)dbski.get("ski_infermiere"));
		} catch (Exception e) {
			debugMessage("scriviPaginaSchedaInfe(" + dbc + " ) : " + e);
		}
		Hashtable ht = new Hashtable();
		ht.put("#n_cartella#", n_cartella);
		ht.put("#nome_assistito#", intesta_nome);
		ht.put("#n_contatto#", n_contatto);
		ht.put("#data_apertura_contatto#", dataIta(data_apertura));
		ht.put("#data_aper_inf#", data_ape);
		ht.put("#operatore_infe#", operatore);
		ht.put("#modalita_infe#", modalita);
		ht.put("#descr_conta_infe#", descr_contatto);
		ht.put("#inf_referente#", infermiere);
		ht.put("#inviato_da_infe#", inviato);
		ht.put("#tipo_utenza_infe#", tipout);
		ht.put("#dimissione_infe#", osp_dim);
		ht.put("#data_dimissione_infe#", data_dimiss);
		ht.put("#rep_dimis_infe#", uo_dim);
		ht.put("#tram_sch_dim_n#", trasm_sk_n);
		ht.put("#tram_sch_dim_s#", trasm_sk_s);
		ht.put("#lesione_dec_n#", les_dec_n);
		ht.put("#lesione_dec_s#", les_dec_s);
		ht.put("#sintesi_infe#", sintesi_do);
		ht.put("#anamnesi_1_inf#", anamnesi_1);
		ht.put("#anamnesi_2_inf#", anamnesi_2);
		ht.put("#data_uscita_inf#", data_uscita);
		ht.put("#motivo_dim_inf#", dimissioni);
		ht.put("#desc_trasf_inf#", trasfer);
		ht.put("#annotazioni_inf#", note);
		// 05/02/13
		ht.put("#presidio_cont#", presidioCont);
		
		eve.writeSostituisci("paginaSchedaInfe", ht);
	}

	private void scriviPaginaAmbula(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		String data_inizio = "";
		String prestaz = "";
		String freq = "";
		String data_fine = "";
		String allerg_intoll = "";

		try {
			String selSki = "SELECT ski_allerg_intoll " + " FROM skinf "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			ISASRecord dbski = dbc.readRecord(selSki);
			allerg_intoll = (String) dbski.get("ski_allerg_intoll");

			Hashtable ht = new Hashtable();
			ht.put("#n_cartella#", n_cartella);
			ht.put("#nome_assistito#", intesta_nome);
			ht.put("#n_contatto#", n_contatto);
			ht.put("#data_apertura_contatto#", dataIta(data_apertura));
			ht.put("#allergie_inf_amb#", allerg_intoll);
			eve.writeSostituisci("paginaAmbula", ht);

			String sel = "SELECT * " + " FROM skintamb " + " WHERE n_cartella="
					+ n_cartella + " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			ISASCursor dbcur = dbc.startCursor(sel);
			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#data_inizio_ambul#", "");
				hterr.put("#prestazioni_ambul#", "***ERRORE***");
				hterr.put("#frequenza_ambul#", "");
				hterr.put("#data_fine_ambul#", "");
				eve.writeSostituisci("ambulatorio", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaAmbula(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#data_inizio_ambul#", "");
					hterr1.put("#prestazioni_ambul#",
							"NESSUNA PRESTAZIONE TROVATA ");
					hterr1.put("#frequenza_ambul#", "");
					hterr1.put("#data_fine_ambul#", "");
					eve.writeSostituisci("ambulatorio", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbska = dbcur.getRecord();

						data_inizio = getDateField(dbska, "ska_data_inizio");
						prestaz = getDecodifica(dbc, "prest_des", "prest_cod",
								"prestaz", getStringField(dbska, "ska_prestaz"));
						// freq = getFrequenza(getStringField(dbska,
						// "ska_freq"));
						freq = getFrequenza(getStringField(dbska, "ska_freq"),
								dbc);
						data_fine = getDateField(dbska, "ska_data_fine");

						Hashtable ht_a = new Hashtable();
						ht_a.put("#data_inizio_ambul#", data_inizio);
						ht_a.put("#prestazioni_ambul#", prestaz);
						ht_a.put("#frequenza_ambul#", freq);
						ht_a.put("#data_fine_ambul#", data_fine);

						eve.writeSostituisci("ambulatorio", ht_a);
					}
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaAmbula(" + dbc + " ) : " + e);
		}
		eve.write("fineTabPaginaAmbula");
	}

	
	private void scriviPaginaProgAssi(ISASConnection dbc, mergeDocument eve) throws SQLException {
		String punto = MIONOME  + "scriviPaginaProgAssi ";
		ISASCursor dbcur = null;
		try {
			// String sel = "SELECT * " + " FROM skiprogass "
			// + " WHERE n_cartella=" + n_cartella + " AND n_contatto="
			// + n_contatto;
			String sel = "select * from piano_assist where n_cartella = " + n_cartella + " and n_progetto = " + n_contatto
					+ " and pa_tipo_oper = '" + tipoOperatore + "' order by pa_data desc ";
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);
			stampa(punto + "Query>" + sel);
			dbcur = dbc.startCursor(sel);
			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto);
				hterr.put("#data_apertura_contatto#", dataIta(data_apertura));
				hterr.put("#data_scheda#", "");
				hterr.put("#descr_prog#", "***ERRORE***");
				hterr.put("#tipo_prog#", "");
				hterr = faiSiNo(hterr, "pp1", "");
				hterr = faiSiNo(hterr, "pp2", "");
				hterr = faiSiNo(hterr, "pp3", "");
				hterr = faiSiNo(hterr, "pp4", "");
				hterr = faiSiNo(hterr, "pp5", "");
				hterr = faiSiNo(hterr, "pp6", "");
				hterr = faiSiNo(hterr, "pp7", "");
				hterr = faiSiNo(hterr, "pp8", "");
				hterr = faiSiNo(hterr, "pp9", "");
				hterr = faiSiNo(hterr, "pp10", "");
				hterr = faiSiNo(hterr, "pp11", "");
				hterr = faiSiNo(hterr, "pp12", "");
				hterr = faiSiNo(hterr, "pp13", "");
				eve.writeSostituisci("paginaProgAssi", hterr);
				// eve.writeSostituisci("progettoAss",ht_a);
				eve.write("fineTabPaginaProgAssi");
				System.out.println("FoSkInfeEJB.scriviPaginaProgAssi(): " + "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto);
					hterr1.put("#data_apertura_contatto#", dataIta(data_apertura));
					hterr1.put("#tipo_prog#", "");
					hterr1.put("#data_scheda#", "");
					hterr1.put("#descr_prog#", "");
					hterr1 = faiSiNo(hterr1, "pp1", "");
					hterr1 = faiSiNo(hterr1, "pp2", "");
					hterr1 = faiSiNo(hterr1, "pp3", "");
					hterr1 = faiSiNo(hterr1, "pp4", "");
					hterr1 = faiSiNo(hterr1, "pp5", "");
					hterr1 = faiSiNo(hterr1, "pp6", "");
					hterr1 = faiSiNo(hterr1, "pp7", "");
					hterr1 = faiSiNo(hterr1, "pp8", "");
					hterr1 = faiSiNo(hterr1, "pp9", "");
					hterr1 = faiSiNo(hterr1, "pp10", "");
					hterr1 = faiSiNo(hterr1, "pp11", "");
					hterr1 = faiSiNo(hterr1, "pp12", "");
					hterr1 = faiSiNo(hterr1, "pp13", "");
					eve.writeSostituisci("paginaProgAssi", hterr1);
					// eve.writeSostituisci("progettoAss",ht_a);
					eve.write("fineTabPaginaProgAssi");
				} else {
					boolean tagliare = false;
					while (dbcur.next()) {
						ISASRecord dbpass = dbcur.getRecord();
						String progetto = "";
						if (tagliare){
							eve.write("taglia");
						}
						Hashtable ht_a = new Hashtable();
						ht_a.put("#n_cartella#", n_cartella);
						ht_a.put("#nome_assistito#", intesta_nome);
						ht_a.put("#n_contatto#", n_contatto); // ht_a.put("#data_apertura_contatto#",dataIta(data_apertura));
//						pa_data, n_progetto, skpa_tipo_progetto
						
						ht_a.put("#data_scheda#", getDateField(dbpass, "pa_data"));
						ht_a.put("#descr_prog#", getStringField(dbpass, "n_progetto"));
						if (getStringField(dbpass, "skpa_tipo_progetto").equals("N"))
							progetto = "SOLO PROTOCOLLO E PROCEDURA";
						else if (getStringField(dbpass, "skpa_tipo_progetto").equals("S"))
							progetto = "PROGETTO";
						ht_a.put("#tipo_prog#", progetto);

						ht_a = faiSiNo(ht_a, "pp1", getStringField(dbpass, "skpa_pp1"));
						ht_a = faiSiNo(ht_a, "pp2", getStringField(dbpass, "skpa_pp2"));
						ht_a = faiSiNo(ht_a, "pp3", getStringField(dbpass, "skpa_pp3"));
						ht_a = faiSiNo(ht_a, "pp4", getStringField(dbpass, "skpa_pp4"));
						ht_a = faiSiNo(ht_a, "pp5", getStringField(dbpass, "skpa_pp5"));
						ht_a = faiSiNo(ht_a, "pp6", getStringField(dbpass, "skpa_pp6"));
						ht_a = faiSiNo(ht_a, "pp7", getStringField(dbpass, "skpa_pp7"));
						ht_a = faiSiNo(ht_a, "pp8", getStringField(dbpass, "skpa_pp8"));
						ht_a = faiSiNo(ht_a, "pp9", getStringField(dbpass, "skpa_pp9"));
						ht_a = faiSiNo(ht_a, "pp10", getStringField(dbpass, "skpa_pp10"));
						ht_a = faiSiNo(ht_a, "pp11", getStringField(dbpass, "skpa_pp11"));
						ht_a = faiSiNo(ht_a, "pp12", getStringField(dbpass, "skpa_pp12"));
						ht_a = faiSiNo(ht_a, "pp13", getStringField(dbpass, "skpa_pp13"));

//						stampa(punto + " dati che ho recuperato>\n"+ ht_a+"<\n");
						String paData = ISASUtil.getValoreStringa(dbpass, "pa_data");
						eve.writeSostituisci("paginaProgAssi", ht_a);
						queryPianoInterv(dbc, dbpass, eve, paData);
						eve.write("chiudiIntervApriVer");
						queryVerifica(dbc, dbpass, eve, paData);
						eve.write("fineTabPaginaProgAssi");
						tagliare = true;
					}
				}
//				dbcur.close();
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaProgAssi(" + dbc + " ) : " + e);
		} finally {
			close_dbcur_nothrow(punto, dbcur);
		}
		// eve.write("fineTabPaginaProgAssi");
	}
	
//	private void scriviPaginaProgAssi(ISASConnection dbc, mergeDocument eve)
//			throws SQLException {
//		/*
//		 * Hashtable ht = new Hashtable(); ht.put("#n_cartella#",n_cartella);
//		 * ht.put("#nome_assistito#",intesta_nome);
//		 * ht.put("#n_contatto#",n_contatto);
//		 * ht.put("#data_apertura_contatto#",dataIta(data_apertura));
//		 * eve.writeSostituisci("paginaProgAssi",ht);
//		 */
//		ISASCursor dbcur = null;
//		try {
////			String sel = "SELECT * " + " FROM skiprogass "
////					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
////					+ n_contatto;
//			String sel = "select * from piano_assist where n_cartella = " +n_cartella+ " and n_progetto = " +n_contatto+
//					" and pa_tipo_oper = '" +tipoOperatore +
//					"'";
//			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);
//			System.out.println("" + sel);
////			ISASCursor dbcur = dbc.startCursor(sel);
//			dbcur = dbc.startCursor(sel);
//			if (dbcur == null) {
//				Hashtable hterr = new Hashtable();
//				hterr.put("#n_cartella#", n_cartella);
//				hterr.put("#nome_assistito#", intesta_nome);
//				hterr.put("#n_contatto#", n_contatto);
//				hterr.put("#data_apertura_contatto#", dataIta(data_apertura));
//				hterr.put("#data_scheda#", "");
//				hterr.put("#descr_prog#", "***ERRORE***");
//				hterr.put("#tipo_prog#", "");
//				hterr = faiSiNo(hterr, "pp1", "");
//				hterr = faiSiNo(hterr, "pp2", "");
//				hterr = faiSiNo(hterr, "pp3", "");
//				hterr = faiSiNo(hterr, "pp4", "");
//				hterr = faiSiNo(hterr, "pp5", "");
//				hterr = faiSiNo(hterr, "pp6", "");
//				hterr = faiSiNo(hterr, "pp7", "");
//				hterr = faiSiNo(hterr, "pp8", "");
//				hterr = faiSiNo(hterr, "pp9", "");
//				hterr = faiSiNo(hterr, "pp10", "");
//				hterr = faiSiNo(hterr, "pp11", "");
//				hterr = faiSiNo(hterr, "pp12", "");
//				hterr = faiSiNo(hterr, "pp13", "");
//				eve.writeSostituisci("paginaProgAssi", hterr);
//				// eve.writeSostituisci("progettoAss",ht_a);
//				eve.write("fineTabPaginaProgAssi");
//				System.out.println("FoSkInfeEJB.scriviPaginaProgAssi(): "
//						+ "cursore non valido");
//			} else {
//				if (dbcur.getDimension() <= 0) {
//					Hashtable hterr1 = new Hashtable();
//					hterr1.put("#n_cartella#", n_cartella);
//					hterr1.put("#nome_assistito#", intesta_nome);
//					hterr1.put("#n_contatto#", n_contatto);
//					hterr1.put("#data_apertura_contatto#",
//							dataIta(data_apertura));
//					hterr1.put("#tipo_prog#", "");
//					hterr1.put("#data_scheda#", "");
//					hterr1.put("#descr_prog#", "");
//					hterr1 = faiSiNo(hterr1, "pp1", "");
//					hterr1 = faiSiNo(hterr1, "pp2", "");
//					hterr1 = faiSiNo(hterr1, "pp3", "");
//					hterr1 = faiSiNo(hterr1, "pp4", "");
//					hterr1 = faiSiNo(hterr1, "pp5", "");
//					hterr1 = faiSiNo(hterr1, "pp6", "");
//					hterr1 = faiSiNo(hterr1, "pp7", "");
//					hterr1 = faiSiNo(hterr1, "pp8", "");
//					hterr1 = faiSiNo(hterr1, "pp9", "");
//					hterr1 = faiSiNo(hterr1, "pp10", "");
//					hterr1 = faiSiNo(hterr1, "pp11", "");
//					hterr1 = faiSiNo(hterr1, "pp12", "");
//					hterr1 = faiSiNo(hterr1, "pp13", "");
//					eve.writeSostituisci("paginaProgAssi", hterr1);
//					// eve.writeSostituisci("progettoAss",ht_a);
//					eve.write("fineTabPaginaProgAssi");
//				} else {
//					while (dbcur.next()) {
//						ISASRecord dbpass = dbcur.getRecord();
//						String progetto = "";
//
//						Hashtable ht_a = new Hashtable();
//						ht_a.put("#n_cartella#", n_cartella);
//						ht_a.put("#nome_assistito#", intesta_nome);
//						ht_a.put("#n_contatto#", n_contatto); // ht_a.put("#data_apertura_contatto#",dataIta(data_apertura));
//						ht_a.put("#data_scheda#", getDateField(dbpass,"skpa_data"));
//						ht_a.put("#descr_prog#", getStringField(dbpass,"skpa_progetto"));
//						if (getStringField(dbpass, "skpa_tipo_progetto")
//								.equals("N"))
//							progetto = "SOLO PROTOCOLLO E PROCEDURA";
//						else if (getStringField(dbpass, "skpa_tipo_progetto")
//								.equals("S"))
//							progetto = "PROGETTO";
//						ht_a.put("#tipo_prog#", progetto);
//
//						ht_a = faiSiNo(ht_a, "pp1", getStringField(dbpass,
//								"skpa_pp1"));
//						ht_a = faiSiNo(ht_a, "pp2", getStringField(dbpass,
//								"skpa_pp2"));
//						ht_a = faiSiNo(ht_a, "pp3", getStringField(dbpass,
//								"skpa_pp3"));
//						ht_a = faiSiNo(ht_a, "pp4", getStringField(dbpass,
//								"skpa_pp4"));
//						ht_a = faiSiNo(ht_a, "pp5", getStringField(dbpass,
//								"skpa_pp5"));
//						ht_a = faiSiNo(ht_a, "pp6", getStringField(dbpass,
//								"skpa_pp6"));
//						ht_a = faiSiNo(ht_a, "pp7", getStringField(dbpass,
//								"skpa_pp7"));
//						ht_a = faiSiNo(ht_a, "pp8", getStringField(dbpass,
//								"skpa_pp8"));
//						ht_a = faiSiNo(ht_a, "pp9", getStringField(dbpass,
//								"skpa_pp9"));
//						ht_a = faiSiNo(ht_a, "pp10", getStringField(dbpass,
//								"skpa_pp10"));
//						ht_a = faiSiNo(ht_a, "pp11", getStringField(dbpass,
//								"skpa_pp11"));
//						ht_a = faiSiNo(ht_a, "pp12", getStringField(dbpass,
//								"skpa_pp12"));
//						ht_a = faiSiNo(ht_a, "pp13", getStringField(dbpass,
//								"skpa_pp13"));
//
//						eve.writeSostituisci("paginaProgAssi", ht_a);
//						queryPianoInterv(dbc, dbpass, eve);
//						eve.write("chiudiIntervApriVer");
//						queryVerifica(dbc, dbpass, eve);
//						eve.write("fineTabPaginaProgAssi");
//					}
//				}
//				dbcur.close();
//			}
//			
//		} catch (Exception e) {
//			debugMessage("scriviPaginaProgAssi(" + dbc + " ) : " + e);
//		}finally{
//			if(dbcur!=null){
//				try {
//					dbcur.close();
//				} catch (DBSQLException e) {
//					e.printStackTrace();
//				} catch (DBMisuseException e) {
//					e.printStackTrace();
//				} catch (ISASMisuseException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		// eve.write("fineTabPaginaProgAssi");
//	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	private void scriviPaginaEventi(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		String data_eventi = "";
		String descrizione = "";

		Hashtable ht = new Hashtable();
		ht.put("#n_cartella#", n_cartella);
		ht.put("#nome_assistito#", intesta_nome);
		ht.put("#n_contatto#", n_contatto);
		ht.put("#data_apertura_contatto#", dataIta(data_apertura));
		eve.writeSostituisci("paginaEventi", ht);
		try {

			String sel = "SELECT skev_data,skev_note " + " FROM skieventi "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			ISASCursor dbcur = dbc.startCursor(sel);
			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#data_eventi#", "");
				hterr.put("#descr_eventi#", "***ERRORE***");
				eve.writeSostituisci("eventi", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaEventi(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#data_eventi#", "");
					hterr1.put("#descr_eventi#", "");
					eve.writeSostituisci("eventi", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbpass = dbcur.getRecord();
						data_eventi = getDateField(dbpass, "skev_data");
						descrizione = getStringField(dbpass, "skev_note");

						Hashtable ht_a = new Hashtable();
						ht_a.put("#data_eventi#", data_eventi);
						ht_a.put("#descr_eventi#", descrizione);
						eve.writeSostituisci("eventi", ht_a);
					}
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaEventi(" + dbc + " ) : " + e);
		}
		eve.write("fineTabPaginaEventi");
	}

	private void scriviPaginaPrestRich(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		String data_prestaz = "";
		String descr_prestaz = "";
		String prescri_prestaz = "";

		Hashtable ht = new Hashtable();
		ht.put("#n_cartella#", n_cartella);
		ht.put("#nome_assistito#", intesta_nome);
		ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
		eve.writeSostituisci("paginaPrestRich", ht);
		try {

			String sel = "SELECT skp_data,skp_descrizione,skp_medico "
					+ " FROM skiprestaz " + " WHERE n_cartella=" + n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr = faiVuota(hterr, new String[] { "data_prestaz",
						"descr_prestaz", "prescri_prestaz" });
				eve.writeSostituisci("prestazioni", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaPrestRich(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1 = faiVuota(hterr1, new String[] { "data_prestaz",
							"descr_prestaz", "prescri_prestaz" });
					eve.writeSostituisci("prestazioni", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbpass = dbcur.getRecord();

						data_prestaz = getDateField(dbpass, "skp_data");
						descr_prestaz = getStringField(dbpass,
								"skp_descrizione");
						prescri_prestaz = getMedico(dbc, getStringField(dbpass,
								"skp_medico"));

						Hashtable ht_a = new Hashtable();
						ht_a.put("#data_prestaz#", data_prestaz);
						ht_a.put("#descr_prestaz#", descr_prestaz);
						ht_a.put("#prescri_prestaz#", prescri_prestaz);
						eve.writeSostituisci("prestazioni", ht_a);
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaPrestRich(" + dbc + " ) : " + e);
		}
		eve.write("fineTabPaginaPrestRich");
	}

	private void scriviPaginaAccoFam(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {

			String sel = "SELECT * " + " FROM skiaccoglienza "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				sel = sel + " AND ska_data=" + formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_accoglienza", "acc1", "acc2", "acc3", "acc4",
						"acc5", "acc6", "acc7", "acc8", "acc9" });
				eve.writeSostituisci("paginaAccoFam", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaAccoFam(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_accoglienza", "acc1", "acc2", "acc3", "acc4",
							"acc5", "acc6", "acc7", "acc8", "acc9" });
					eve.writeSostituisci("paginaAccoFam", hterr1);
				} else {
					while (dbcur.next()) {

						ISASRecord dbacc = dbcur.getRecord();
						Hashtable ht_a = new Hashtable();

						ht_a.put("#n_cartella#", n_cartella);
						ht_a.put("#nome_assistito#", intesta_nome);
						ht_a.put("#n_contatto#", n_contatto); // ht_a.put("#data_apertura_contatto#",dataIta(data_apertura));

						ht_a.put("#data_scheda#", getDateField(dbacc,
								"ska_data"));
						ht_a.put("#note_accoglienza#", getStringField(dbacc,
								"ska_note"));
						ht_a.put("#acc1#", faiCrocetta("S", getStringField(
								dbacc, "ska_d1")));
						ht_a.put("#acc2#", faiCrocetta("S", getStringField(
								dbacc, "ska_d2")));
						ht_a.put("#acc3#", faiCrocetta("S", getStringField(
								dbacc, "ska_d3")));
						ht_a.put("#acc4#", faiCrocetta("S", getStringField(
								dbacc, "ska_d4")));
						ht_a.put("#acc5#", faiCrocetta("S", getStringField(
								dbacc, "ska_d5")));
						ht_a.put("#acc6#", faiCrocetta("S", getStringField(
								dbacc, "ska_d6")));
						ht_a.put("#acc7#", faiCrocetta("S", getStringField(
								dbacc, "ska_d7")));
						ht_a.put("#acc8#", faiCrocetta("S", getStringField(
								dbacc, "ska_d8")));
						ht_a.put("#acc9#", faiCrocetta("S", getStringField(
								dbacc, "ska_d9")));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaAccoFam", ht_a);
						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale

		} catch (Exception e) {
			debugMessage("scriviPaginaAccoFam(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaSitFam(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String sel = "SELECT * " + " FROM skifamiglia "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				sel = sel + " AND skf_data=" + formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(sel);
			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(
						hterr,
						new String[] { "data_scheda", "note_nucleo_fam",
								"persona_rif", "telesoc", "nuc1", "nuc2",
								"nuc3", "nuc4", "nuc5", "nuc6", "nuc7", "nuc8" });
				eve.writeSostituisci("paginaSitFam", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaSitFam(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_nucleo_fam", "persona_rif", "telesoc",
							"nuc1", "nuc2", "nuc3", "nuc4", "nuc5", "nuc6",
							"nuc7", "nuc8" });
					eve.writeSostituisci("paginaSitFam", hterr1);
				} else {
					while (dbcur.next()) {

						ISASRecord dbnuc = dbcur.getRecord();
						Hashtable ht_a = new Hashtable();

						ht_a.put("#n_cartella#", n_cartella);
						ht_a.put("#nome_assistito#", intesta_nome);
						ht_a.put("#n_contatto#", n_contatto); // ht_a.put("#data_apertura_contatto#",dataIta(data_apertura));

						ht_a.put("#data_scheda#", getDateField(dbnuc,
								"skf_data"));
						ht_a.put("#note_nucleo_fam#", getStringField(dbnuc,
								"skf_note"));
						ht_a.put("#nuc1#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d1")));
						ht_a.put("#persona_rif#", getStringField(dbnuc,
								"skf_nome"));
						ht_a.put("#nuc2#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d2")));
						ht_a.put("#nuc3#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d3")));
						ht_a.put("#nuc4#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d4")));
						ht_a.put("#telesoc#", getStringField(dbnuc,
								"skf_telesoc"));
						ht_a.put("#nuc5#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d5")));
						ht_a.put("#nuc6#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d6")));
						ht_a.put("#nuc7#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d7")));
						ht_a.put("#nuc8#", faiCrocetta("S", getStringField(
								dbnuc, "skf_d8")));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaSitFam", ht_a);
						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale

		} catch (Exception e) {
			debugMessage("scriviPaginaSitFam(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaNucleoFam(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT * FROM skinucleo_fam "
					+ " WHERE n_cartella=" + n_cartella;
			if (data_scheda != null)
				select = select + " AND data_variazione="
						+ formatDate(dbc, data_scheda);
			else
				select += " ORDER BY data_variazione";

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr = faiVuota(hterr, new String[] { "data_scheda" });
				eve.writeSostituisci("paginaNucleoFam", hterr);
				eve.write("fineTabPaginaNucleoFam");
				System.out.println("FoSkInfeEJB.scriviPaginaLesioniDecub(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda" });
					eve.writeSostituisci("paginaNucleoFam", hterr1);
					eve.write("fineTabPaginaNucleoFam");
				} else {
					it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
					String new_data = "";
					String old_data = "";
					while (dbcur.next()) {
						ISASRecord dbfam = dbcur.getRecord();
						new_data = (String) ut.getObjectField(dbfam,
								"data_variazione", 'T');
						if (i == 0 || !old_data.equals(new_data)) {
							Hashtable htest = new Hashtable();
							htest.put("#n_cartella#", n_cartella);
							htest.put("#nome_assistito#", intesta_nome);
							htest.put("#data_scheda#", getDateField(dbfam,
									"data_variazione"));
							if (i > 0) {
								eve.write("fineTabPaginaNucleoFam");
								System.out
										.println("***SCRITTO fineTabPaginaNucleoFam");
								eve.write("taglia");
								System.out.println("***SCRITTO taglia");
							}
							eve.writeSostituisci("paginaNucleoFam", htest);
							System.out.println("***SCRITTO paginaNucleoFam");
						}

						Hashtable ht = new Hashtable();
						ht.put("#cognome#", (String) ut.getObjectField(dbfam,
								"cognome", 'S'));
						ht.put("#nome#", (String) ut.getObjectField(dbfam,
								"nome", 'S'));
						String parent = ut
								.getDecode(dbc, "parent", "codice", ""
										+ ut.getObjectField(dbfam, "parentela",
												'S'), "descrizione");
						ht.put("#parentela#", parent);
						ht.put("#residenza#", faiResidenza(dbc, dbfam));
						ht.put("#telefono#", (String) ut.getObjectField(dbfam,
								"telefono", 'S'));
						String caregiver = (String) ut.getObjectField(dbfam,
								"caregiver", 'S');
						ht.put("#caregiver#", caregiver.equals("S") ? "SI"
								: "NO");
						eve.writeSostituisci("nucleoFam", ht);
						System.out.println("***SCRITTO nucleoFam");
						i++;
						old_data = new_data;
					}
					eve.write("fineTabPaginaNucleoFam");

				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaNucleoFam(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaTerInAtto(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {

			String sel = "SELECT * " + " FROM skiterapia "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				sel = sel + " AND skt_data=" + formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(sel);
			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"descr_ter_atto", "terapia_rich", "aller_intoll",
						"ter1", "ter2", "ter3", "rich1", "rich2", "rich3" });
				eve.writeSostituisci("paginaTerInAtto", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaTerInAtto(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"descr_ter_atto", "terapia_rich", "aller_intoll",
							"ter1", "ter2", "ter3", "rich1", "rich2", "rich3" });
					eve.writeSostituisci("paginaTerInAtto", hterr1);
				} else {
					while (dbcur.next()) {

						ISASRecord dbter = dbcur.getRecord();
						Hashtable ht_a = new Hashtable();

						ht_a.put("#n_cartella#", n_cartella);
						ht_a.put("#nome_assistito#", intesta_nome);
						ht_a.put("#n_contatto#", n_contatto); // ht_a.put("#data_apertura_contatto#",dataIta(data_apertura));

						ht_a.put("#data_scheda#", getDateField(dbter,
								"skt_data"));
						ht_a.put("#ter1#", faiCrocetta("S", getStringField(
								dbter, "skt_d2")));
						ht_a.put("#ter2#", faiCrocetta("S", getStringField(
								dbter, "skt_d3")));
						ht_a.put("#ter3#", faiCrocetta("S", getStringField(
								dbter, "skt_d4")));
						ht_a.put("#descr_ter_atto#", getStringField(dbter,
								"skt_terapia_atto"));
						ht_a.put("#rich1#", faiCrocetta("1", getStringField(
								dbter, "skt_richiesta")));
						ht_a.put("#rich2#", faiCrocetta("2", getStringField(
								dbter, "skt_richiesta")));
						ht_a.put("#rich3#", faiCrocetta("3", getStringField(
								dbter, "skt_richiesta")));
						ht_a.put("#terapia_rich#", getStringField(dbter,
								"skt_terapia_rich"));
						ht_a.put("#aller_intoll#", getStringField(dbter,
								"skt_aller_intoll"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaTerInAtto", ht_a);
						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale

		} catch (Exception e) {
			debugMessage("scriviPaginaTerInAtto(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaIndNorton(ISASConnection dbc, mergeDocument eve,
			Hashtable par) throws SQLException {

		int i = 0;
		String tipo = (String) par.get("tipo");// INIZIALE O ATTUALE(solo nel
												// caso di stampa singola)
		System.out.println("TIPO" + tipo);
		try {

			String sel = "SELECT skno_data,skno_vri_data,skno_vri_cg,skno_vri_sm, "
					+ "skno_vri_de,skno_vri_mo,skno_vri_in,skno_vri_pu, "
					+ "skno_att_data,skno_att_cg,skno_att_sm,skno_att_de,"
					+ "skno_att_mo,skno_att_in,skno_att_pu "
					+ " FROM skinorton "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				sel = sel + " AND skno_data=" + formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				Hashtable hterrI = new Hashtable();
				Hashtable hterrA = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr.put("#data_scheda#", "");

				hterrI = faiVuota(hterrI, new String[] { "data_val_ini",
						"cond_ini", "stato_ment_ini", "deamb_ini", "mobil_ini",
						"incont_ini", "punt_rischio_ini" });
				hterrA = faiVuota(hterrA, new String[] { "data_val_att",
						"cond_att", "stato_ment_att", "deamb_att", "mobil_att",
						"incont_att", "punt_rischio_att" });
				eve.writeSostituisci("paginaIndNorton", hterr);
				eve.writeSostituisci("nortonIniziale", hterrI);
				eve.writeSostituisci("nortonAttuale", hterrA);
				eve.write("finePaginaIndNorton");
				System.out.println("FoSkInfeEJB.scriviPaginaIndNorton(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					Hashtable hterr2 = new Hashtable();
					Hashtable hterr3 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1.put("#data_scheda#", "");

					hterr2 = faiVuota(hterr2, new String[] { "data_val_ini",
							"cond_ini", "stato_ment_ini", "deamb_ini",
							"mobil_ini", "incont_ini", "punt_rischio_ini" });
					hterr3 = faiVuota(hterr3, new String[] { "data_val_att",
							"cond_att", "stato_ment_att", "deamb_att",
							"mobil_att", "incont_att", "punt_rischio_att" });
					eve.writeSostituisci("paginaIndNorton", hterr1);
					eve.writeSostituisci("nortonIniziale", hterr2);
					eve.writeSostituisci("nortonAttuale", hterr3);
					eve.write("finePaginaIndNorton");
				} else {
					while (dbcur.next()) {

						if (i > 0)
							eve.write("taglia");

						ISASRecord dbter = dbcur.getRecord();
						Hashtable ht_a = new Hashtable();

						ht_a.put("#n_cartella#", n_cartella);
						ht_a.put("#nome_assistito#", intesta_nome);
						ht_a.put("#n_contatto#", n_contatto); // ht_a.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht_a.put("#data_scheda#", getDateField(dbter,
								"skno_data"));
						eve.writeSostituisci("paginaIndNorton", ht_a);

						if (tipo == null || tipo.equals("I")) {
							ht_a.put("#data_val_ini#", getDateField(dbter,
									"skno_vri_data"));
							ht_a.put("#cond_ini#", getCondGen(getStringField(
									dbter, "skno_vri_cg")));
							ht_a.put("#stato_ment_ini#",
									getStatoMent(getStringField(dbter,
											"skno_vri_sm")));
							ht_a.put("#deamb_ini#",
									getDeambulazione(getStringField(dbter,
											"skno_vri_de")));
							ht_a.put("#mobil_ini#", getMobilita(getStringField(
									dbter, "skno_vri_mo")));
							ht_a.put("#incont_ini#",
									getIncontinenza(getStringField(dbter,
											"skno_vri_in")));
							ht_a.put("#punt_rischio_ini#", getStringField(
									dbter, "skno_vri_pu"));
							eve.writeSostituisci("nortonIniziale", ht_a);
						}

						if (tipo == null || tipo.equals("A")) {
							ht_a.put("#data_val_att#", getDateField(dbter,
									"skno_att_data"));
							ht_a.put("#cond_att#", getCondGen(getStringField(
									dbter, "skno_att_cg")));
							ht_a.put("#stato_ment_att#",
									getStatoMent(getStringField(dbter,
											"skno_att_sm")));
							ht_a.put("#deamb_att#",
									getDeambulazione(getStringField(dbter,
											"skno_att_de")));
							ht_a.put("#mobil_att#", getMobilita(getStringField(
									dbter, "skno_att_mo")));
							ht_a.put("#incont_att#",
									getIncontinenza(getStringField(dbter,
											"skno_att_in")));
							ht_a.put("#punt_rischio_att#", getStringField(
									dbter, "skno_att_pu"));
							eve.writeSostituisci("nortonAttuale", ht_a);
						}

						// eve.writeSostituisci("paginaIndNorton",ht_a);

						eve.write("finePaginaIndNorton");

						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale

		} catch (Exception e) {
			debugMessage("scriviPaginaIndNorton(" + dbc + " ) : " + e);
		}

	}

	// gb 22.12.08: Riportata stampa e corretta.
	// 06/10/2008 SCALA KPS
	private void scriviPaginaKPS(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		try {
			String sel = "SELECT * FROM sc_kps WHERE n_cartella = "
					+ n_cartella;

			if (data_scheda != null)
				sel = sel + " AND data=" + formatDate(dbc, data_scheda);
			else { // scale comprese nel contatto
				sel += " AND data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					sel += " AND data <= " + formatDate(dbc, data_chiusura);

				sel += " ORDER BY data";
			}

			ISASCursor dbcur = dbc.startCursor(sel);
			int i = 0;

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					if (i > 0)
						eve.write("taglia");

					ISASRecord dbr = dbcur.getRecord();

					// inizio
					Hashtable htit = new Hashtable();
					htit.put("#nomeTest#", "K.P.S.");
					htit.put("#data#", getDateField(dbr, "data"));
					htit.put("#n_cartella#", n_cartella);
					htit.put("#n_contatto#", n_contatto);
					htit.put("#nome_assistito#", intesta_nome);
					htit
							.put("#data_apertura_contatto#",
									dataIta(data_apertura));
					eve.writeSostituisci("paginaKPS", htit);

					String valVoce = getStringField(dbr, "kps_valore");
					// generica voce
					eve.write("inizioTabKPS");
					for (int k = 0; k < arrVociKPS.length; k++) {
						Hashtable h_xstampa = new Hashtable();
						h_xstampa.put("#voceKPS#", arrVociKPS[k]);

						int punt = (100 - (k * 10));
						h_xstampa.put("#punt#", "" + punt);

						if (valVoce.trim().equals("" + punt))
							h_xstampa.put("#check#", "x");
						else
							h_xstampa.put("#check#", "");

						eve.writeSostituisci("rigaTabKPS", h_xstampa);
					}
					eve.write("finePaginaKPS");

					// totale
					Hashtable hTotale = new Hashtable();
					hTotale.put("#data_test#", getDateField(dbr, "data_test"));
					hTotale.put("#nome_test#", getStringField(dbr, "nome")); // 03/01/07
					hTotale.put("#tot#", "");
					eve.writeSostituisci("totaleKPS", hTotale);
					i++;
				}
			} else {
				Hashtable hvuota = new Hashtable();
				hvuota.put("#nomeTest#", "K.P.S.");
				hvuota.put("#data#", "");
				hvuota.put("#n_cartella#", n_cartella);
				hvuota.put("#n_contatto#", n_contatto);
				hvuota.put("#nome_assistito#", intesta_nome);
				hvuota.put("#data_apertura_contatto#", dataIta(data_apertura));

				eve.writeSostituisci("paginaKPS", hvuota);
				// gb 22.12.08 eve.write("finePaginaKPS");
				eve.write("finePaginaKPS_vuoto"); // gb 22.12.08
				eve.write("messaggio");
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaKPS(" + dbc + " ) : " + e);
		}
	}

	private void scriviPaginaValDol(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		Hashtable ht = new Hashtable();
		ht.put("#n_cartella#", n_cartella);
		ht.put("#nome_assistito#", intesta_nome);
		ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
		eve.writeSostituisci("paginaValDol", ht);
		try {

			String sel = "SELECT * " + " FROM skidolore "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#data_dolore#", "");
				hterr.put("#scala#", "***ERRORE***");
				hterr.put("#esito_val#", "");
				hterr.put("#specialista#", "");
				hterr.put("#annota#", "");
				hterr.put("#sede_testa_n#", "");
				hterr.put("#sede_testa_s#", "");
				hterr.put("#sede_tronco_n#", "");
				hterr.put("#sede_tronco_s#", "");
				hterr.put("#sede_artosup_dx_n#", "");
				hterr.put("#sede_artosup_dx_s#", "");
				hterr.put("#sede_artosup_sx_n#", "");
				hterr.put("#sede_artosup_sx_s#", "");
				hterr.put("#sede_artoinf_dx_n#", "");
				hterr.put("#sede_artoinf_dx_s#", "");
				hterr.put("#sede_artoinf_sx_n#", "");
				hterr.put("#sede_artoinf_sx_s#", "");
				hterr.put("#rilevazione#", "");
				eve.writeSostituisci("dolore", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaValDol(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#data_dolore#", "");
					hterr1.put("#scala#", "");
					hterr1.put("#esito_val#", "");
					hterr1.put("#specialista#", "");
					hterr1.put("#annota#", "");
					hterr1.put("#dolore#", "");
					hterr1.put("#tipologia#", "");
					hterr1.put("#sede_testa_n#", "");
					hterr1.put("#sede_testa_s#", "");
					hterr1.put("#sede_tronco_n#", "");
					hterr1.put("#sede_tronco_s#", "");
					hterr1.put("#sede_artosup_dx_n#", "");
					hterr1.put("#sede_artosup_dx_s#", "");
					hterr1.put("#sede_artosup_sx_n#", "");
					hterr1.put("#sede_artosup_sx_s#", "");
					hterr1.put("#sede_artoinf_dx_n#", "");
					hterr1.put("#sede_artoinf_dx_s#", "");
					hterr1.put("#sede_artoinf_sx_n#", "");
					hterr1.put("#sede_artoinf_sx_s#", "");
					hterr1.put("#rilevazione#", "");
					eve.writeSostituisci("dolore", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbdol = dbcur.getRecord();

						Hashtable ht_a = new Hashtable();
						ht_a.put("#data_dolore#", getDateField(dbdol,
								"skid_data"));
						ht_a
								.put("#scala#", getStringField(dbdol,
										"skid_scala"));
						ht_a.put("#esito_val#", getStringField(dbdol,
								"skid_esito"));
						ht_a.put("#specialista#", getStringField(dbdol,
								"skid_specialista"));
						ht_a
								.put("#annota#", getStringField(dbdol,
										"skid_note"));
						String dolore = getStringField(dbdol, "skid_dolore");
						if (dolore.equals("1"))
							ht_a.put("#dolore#", "Riferito dal paziente");
						else if (dolore.equals("2"))
							ht_a.put("#dolore#", "Dedotto da segni diretti");
						else if (dolore.equals("3"))
							ht_a.put("#dolore#", "Riferito dai familiari");
						else
							ht_a.put("#dolore#", "");
						String tipo = getStringField(dbdol, "skid_tipologia");
						if (tipo.equals("1"))
							ht_a.put("#tipologia#", "Continuo intermittente");
						else if (tipo.equals("2"))
							ht_a.put("#tipologia#", "Continuo remittente");
						else if (tipo.equals("3"))
							ht_a.put("#tipologia#", "Parossistico");
						else
							ht_a.put("#tipologia#", "");

						boolean testa = getStringField(dbdol, "skid_sede_testa")
								.compareTo("S") == 0;
						boolean tronco = getStringField(dbdol,
								"skid_sede_tronco").compareTo("S") == 0;
						boolean artosupdx = getStringField(dbdol,
								"skid_sede_artosup_dx").compareTo("S") == 0;
						boolean artosupsx = getStringField(dbdol,
								"skid_sede_artosup_sx").compareTo("S") == 0;
						boolean artoinfdx = getStringField(dbdol,
								"skid_sede_artoinf_dx").compareTo("S") == 0;
						boolean artoinfsx = getStringField(dbdol,
								"skid_sede_artoinf_sx").compareTo("S") == 0;
						String sede = "";
						if (testa)
							sede = "TESTA";
						if (tronco) {
							if (!sede.equals(""))
								sede += ", ";
							sede += "TRONCO";
						}
						if (artosupdx) {
							if (!sede.equals(""))
								sede += ", ";
							sede += "ARTO SUPERIORE DX";
						}
						if (artosupsx) {
							if (!sede.equals(""))
								sede += ", ";
							sede += "ARTO SUPERIORE SX";
						}
						if (artoinfdx) {
							if (!sede.equals(""))
								sede += ", ";
							sede += "ARTO INFERIORE DX";
						}
						if (artoinfsx) {
							if (!sede.equals(""))
								sede += ", ";
							sede += "ARTO INFERIORE SX";
						}
						ht_a.put("#sede#", sede);

						ht_a.put("#rilevazione#", getStringField(dbdol,
								"skid_rilevazione"));
						eve.writeSostituisci("dolore", ht_a);
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaValDol(" + dbc + " ) : " + e);
		}
		eve.write("fineTabPaginaValDol");
	}

	private void scriviPaginaAusili(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skat_data,skat_note "
					+ " FROM skiausili_t " + " WHERE n_cartella=" + n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skat_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_ausilio" });
				eve.writeSostituisci("paginaAusili", hterr);
				eve.write("fineTabPaginaAusili");
				System.out.println("FoSkInfeEJB.scriviPaginaAusili(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_ausilio" });
					eve.writeSostituisci("paginaAusili", hterr1);
					eve.write("fineTabPaginaAusili");
				} else {
					while (dbcur.next()) {
						ISASRecord dbaus = dbcur.getRecord();
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbaus, "skat_data"));
						ht.put("#note_ausilio#", getStringField(dbaus,
								"skat_note"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaAusili", ht);
						queryAusili(dbc, dbaus, eve);
						eve.write("fineTabPaginaAusili");
						i++;
					}
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaAusili(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaRelazione(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skr_dat,skr_note " + " FROM skirelaz "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skr_dat="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);
			if (dbcur == null || dbcur.getDimension() <= 0) {
				// stampa riga vuota
				Hashtable ht_err = new Hashtable();
				ht_err.put("#n_cartella#", n_cartella);
				ht_err.put("#nome_assistito#", intesta_nome);
				ht_err.put("#n_contatto#", n_contatto); // ht_err.put("#data_apertura_contatto#",dataIta(data_apertura));
				ht_err.put("#data_scheda#", "");
				ht_err.put("#nota_relazione#", "");

				eve.writeSostituisci("paginaRelazione", ht_err);

			} else if (dbcur != null) {
				while (dbcur.next()) {
					ISASRecord dbaus = dbcur.getRecord();
					Hashtable ht = new Hashtable();
					ht.put("#n_cartella#", n_cartella);
					ht.put("#nome_assistito#", intesta_nome);
					ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
					ht.put("#data_scheda#", getDateField(dbaus, "skr_dat"));
					ht.put("#nota_relazione#",
							getStringField(dbaus, "skr_note"));

					if (i > 0)
						eve.write("taglia");

					eve.writeSostituisci("paginaRelazione", ht);
					i++;
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaRelazione(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaStatoCosc(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skc_data,skc_stato,skc_ausili,skc_note "
					+ " FROM skicoscienza " + " WHERE n_cartella=" + n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skc_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_stato_cosc", "stato_cosc", "aus_s", "aus_n" });
				eve.writeSostituisci("paginaStatoCosc", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaStatoCosc(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_stato_cosc", "stato_cosc", "aus_s", "aus_n" });
					eve.writeSostituisci("paginaStatoCosc", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbstac = dbcur.getRecord();
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbstac, "skc_data"));
						ht.put("#note_stato_cosc#", getStringField(dbstac,
								"skc_note"));
						ht.put("#stato_cosc#", getCondGenerali(getStringField(
								dbstac, "skc_stato")));
						ht = faiSiNo(ht, "aus", getStringField(dbstac,
								"skc_ausili"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaStatoCosc", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaStatoCosc(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaUmore(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT sku_data,sku_tono,sku_farma,sku_psichia,sku_segnala,sku_note "
					+ " FROM skiumore "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND sku_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_umore", "tono_umore", "farm_s", "farm_n",
						"psi_s", "psi_n", "segn_s", "segn_n" });
				eve.writeSostituisci("paginaUmore", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaUmore(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_umore", "tono_umore", "farm_s", "farm_n",
							"psi_s", "psi_n", "segn_s", "segn_n" });
					eve.writeSostituisci("paginaUmore", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbstac = dbcur.getRecord();
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbstac, "sku_data"));
						ht.put("#note_umore#", getStringField(dbstac,
								"sku_note"));
						ht.put("#tono_umore#", getTonoUmore(getStringField(
								dbstac, "sku_tono")));
						ht = faiSiNo(ht, "farm", getStringField(dbstac,
								"sku_farma"));
						ht = faiSiNo(ht, "psi", getStringField(dbstac,
								"sku_psichia"));
						ht = faiSiNo(ht, "segn", getStringField(dbstac,
								"sku_segnala"));

						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("paginaUmore", ht);
						i++;
					}
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaUmore(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaRespiraz(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		int i = 0;

		try {
			String select = "SELECT skr_data,skr_respirazione,skr_tracheo,skr_data_inizio,"
					+ "skr_data_ultima,skr_aspira,skr_aspira_spec,skr_secrez,skr_secrez_cons,skr_secrez_qta,"
					+ "skr_secrez_colore,skr_specialista,skr_note"
					+ " FROM respiraz "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skr_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_respiraz", "data_intr", "data_ult_intr",
						"tracheo", "asp_s", "asp_n", "spec_asp", "secr_s",
						"secr_n", "consistenza", "qta_secr", "colore_secr",
						"spe_s", "spe_n", "respirazione" });
				eve.writeSostituisci("paginaRespiraz", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaRespiraz(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_respiraz", "data_intr", "data_ult_intr",
							"tracheo", "asp_s", "asp_n", "spec_asp", "secr_s",
							"secr_n", "consistenza", "qta_secr", "colore_secr",
							"spe_s", "spe_n", "respirazione" });
					eve.writeSostituisci("paginaRespiraz", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbstac = dbcur.getRecord();
						Hashtable ht = new Hashtable();
						String respirazione = "";
						String consistenza = "";
						String quantita = "";
						String colore = "";
						String tracheo = "";

						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbstac, "skr_data"));
						ht.put("#note_respiraz#", getStringField(dbstac,
								"skr_note"));
						ht.put("#data_intr#", getDateField(dbstac,
								"skr_data_inizio"));
						ht.put("#data_ult_intr#", getDateField(dbstac,
								"skr_data_ultima"));

						if (getStringField(dbstac, "skr_tracheo").equals("1"))
							tracheo = "senza cannula";
						else if (getStringField(dbstac, "skr_tracheo").equals(
								"2"))
							tracheo = "con cannula, respiro spontaneo";
						ht.put("#tracheo#", tracheo);

						ht = faiSiNo(ht, "asp", getStringField(dbstac,
								"skr_aspira"));
						ht.put("#spec_asp#", getSpecAspiraz(getStringField(
								dbstac, "skr_aspira_spec")));
						ht = faiSiNo(ht, "secr", getStringField(dbstac,
								"skr_secrez"));

						if (getStringField(dbstac, "skr_secrez_cons").equals(
								"1"))
							consistenza = "schiumose";
						else if (getStringField(dbstac, "skr_secrez_cons")
								.equals("2"))
							consistenza = "dense";
						ht.put("#consistenza#", consistenza);

						if (getStringField(dbstac, "skr_secrez_qta")
								.equals("1"))
							quantita = "assenti";
						else if (getStringField(dbstac, "skr_secrez_qta")
								.equals("2"))
							quantita = "abbondanti";
						ht.put("#qta_secr#", quantita);

						if (getStringField(dbstac, "skr_secrez_colore").equals(
								"1"))
							colore = "ematiche";
						else if (getStringField(dbstac, "skr_secrez_colore")
								.equals("2"))
							colore = "chiare";
						ht.put("#colore_secr#", colore);

						ht = faiSiNo(ht, "spe", getStringField(dbstac,
								"skr_specialista"));

						if (getStringField(dbstac, "skr_respirazione").equals(
								"1"))
							respirazione = "normale";
						else if (getStringField(dbstac, "skr_respirazione")
								.equals("2"))
							respirazione = "difficoltosa";
						ht.put("#respirazione#", respirazione);

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaRespiraz", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaRespiraz(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaStatoNutriz(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		int i = 0;

		try {
			String select = "SELECT skn_data,skn_stato,skn_spec,skn_dentatura,"
					+ " skn_spec_data,skn_alimentazione,skn_famiglia,skn_note"
					+ " FROM skinutriz " + " WHERE n_cartella=" + n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skn_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr,
						new String[] { "data_scheda", "note_nutriz",
								"stato_nutriz", "alimentaz", "comport_fam",
								"consul_spe", "data_consu", "dentatura" });
				eve.writeSostituisci("paginaStatoNutriz", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaStatoNutriz(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_nutriz", "stato_nutriz", "alimentaz",
							"comport_fam", "consul_spe", "data_consu",
							"dentatura" });
					eve.writeSostituisci("paginaStatoNutriz", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbstac = dbcur.getRecord();
						String consulenza = "";
						String stato_nutriz = "";
						String dentatura = "";
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbstac, "skn_data"));
						ht.put("#note_nutriz#", getStringField(dbstac,
								"skn_note"));
						if (getStringField(dbstac, "skn_stato").equals("1"))
							stato_nutriz = "normale";
						else if (getStringField(dbstac, "skn_stato")
								.equals("2"))
							stato_nutriz = "eccessivo";
						else if (getStringField(dbstac, "skn_stato")
								.equals("3"))
							stato_nutriz = "carente";
						ht.put("#stato_nutriz#", stato_nutriz);

						ht.put("#alimentaz#", getAlimentArti(getStringField(
								dbstac, "skn_alimentazione")));
						ht.put("#comport_fam#", getSpecAspiraz(getStringField(
								dbstac, "skn_famiglia")));

						if (getStringField(dbstac, "skn_spec").equals("1"))
							consulenza = "non necessaria";
						else if (getStringField(dbstac, "skn_spec").equals("2"))
							consulenza = "effettuata";
						else if (getStringField(dbstac, "skn_spec").equals("3"))
							consulenza = "da richiedere";
						ht.put("#consul_spe#", consulenza);
						if (getStringField(dbstac, "skn_dentatura").equals("1"))
							dentatura = "dentulo";
						else if (getStringField(dbstac, "skn_dentatura")
								.equals("2"))
							dentatura = "edentulo";
						else if (getStringField(dbstac, "skn_dentatura")
								.equals("3"))
							dentatura = "fa uso di protesi";
						ht.put("#dentatura#", dentatura);
						ht.put("#data_consu#", getDateField(dbstac,
								"skn_spec_data"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaStatoNutriz", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaStatoNutriz(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaEscret(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT ske_data,ske_intest,ske_ritenz,ske_incontinenza,"
					+ "ske_portatore,ske_porta_inizio,ske_porta_ultima,ske_catetere,"
					+ "ske_diagn,ske_stomie,ske_famiglia "
					+ " FROM skiescret "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND ske_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr,
						new String[] { "data_scheda", "uri_s", "uri_n",
								"elim_inte", "indagini_diagn", "stomie",
								"incontinenza", "famiglia", "portatore_di",
								"introduz_catete", "ult_introduz_catete",
								"tipo_catete" });
				eve.writeSostituisci("paginaEscretorie", hterr);
				eve.write("fineTabPaginaEscretorie");
				System.out.println("FoSkInfeEJB.scriviPaginaEscret(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"uri_s", "uri_n", "elim_inte", "indagini_diagn",
							"stomie", "incontinenza", "famiglia",
							"portatore_di", "introduz_catete",
							"ult_introduz_catete", "tipo_catete" });
					eve.writeSostituisci("paginaEscretorie", hterr1);
					eve.write("fineTabPaginaEscretorie");
				} else {
					while (dbcur.next()) {
						ISASRecord dbescr = dbcur.getRecord();
						String elim_inte = "";
						String indagini = "";
						String incontinenza = "";
						String portatore = "";

						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbescr, "ske_data"));
						ht = faiSiNo(ht, "uri", getStringField(dbescr,
								"ske_ritenz"));
						if (getStringField(dbescr, "ske_intest").equals("1"))
							elim_inte = "regolare";
						else if (getStringField(dbescr, "ske_intest").equals(
								"2"))
							elim_inte = "con aiuto";
						ht.put("#elim_inte#", elim_inte);

						if (getStringField(dbescr, "ske_diagn").equals("1"))
							indagini = "non effettuate";
						else if (getStringField(dbescr, "ske_diagn")
								.equals("2"))
							indagini = "richieste";
						else if (getStringField(dbescr, "ske_diagn")
								.equals("3"))
							indagini = "effettuate";
						ht.put("#indagini_diagn#", indagini);
						ht.put("#stomie#", getStomie(getStringField(dbescr,
								"ske_stomie")));

						if (getStringField(dbescr, "ske_incontinenza").equals(
								"1"))
							incontinenza = "no";
						else if (getStringField(dbescr, "ske_incontinenza")
								.equals("2"))
							incontinenza = "si, urine";
						else if (getStringField(dbescr, "ske_incontinenza")
								.equals("3"))
							incontinenza = "si, feci";
						else if (getStringField(dbescr, "ske_incontinenza")
								.equals("4"))
							incontinenza = "si, urine e feci";
						ht.put("#incontinenza#", incontinenza);

						ht.put("#famiglia#", getSpecAspiraz(getStringField(
								dbescr, "ske_famiglia")));

						if (getStringField(dbescr, "ske_portatore").equals("1"))
							portatore = "niente";
						else if (getStringField(dbescr, "ske_portatore")
								.equals("2"))
							portatore = "pannolone";
						else if (getStringField(dbescr, "ske_portatore")
								.equals("3"))
							portatore = "profilattico";
						else if (getStringField(dbescr, "ske_portatore")
								.equals("4"))
							portatore = "catetere";
						ht.put("#portatore_di#", portatore);

						ht.put("#introduz_catete#", getDateField(dbescr,
								"ske_porta_inizio"));
						ht.put("#ult_introduz_catete#", getDateField(dbescr,
								"ske_porta_ultima"));
						ht.put("#tipo_catete#", getStringField(dbescr,
								"ske_catetere"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaEscretorie", ht);
						queryDiagnEscret(dbc, dbescr, eve);
						eve.write("fineTabPaginaEscretorie");
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaEscret(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaMotric(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		int i = 0;

		try {
			String select = "SELECT skd_data,skd_mobilita,skd_consulenza,skd_posture,"
					+ " skd_posture_obb,skd_note "
					+ " FROM skimotric "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skd_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_motric", "mobilita", "rich_consu", "cambi_post",
						"post_s", "post_n" });
				eve.writeSostituisci("paginaMotric", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaMotric(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_motric", "mobilita", "rich_consu",
							"cambi_post", "post_s", "post_n" });
					eve.writeSostituisci("paginaMotric", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbmot = dbcur.getRecord();
						String mobilita = "";
						String consulenza = "";
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht
								.put("#data_scheda#", getDateField(dbmot,
										"skd_data"));
						ht.put("#note_motric#", getStringField(dbmot,
								"skd_note"));
						if (getStringField(dbmot, "skd_mobilita").equals("1"))
							mobilita = "normale";
						else if (getStringField(dbmot, "skd_mobilita").equals(
								"2"))
							mobilita = "cammina con aiuto";
						else if (getStringField(dbmot, "skd_mobilita").equals(
								"3"))
							mobilita = "costretto a letto";
						else if (getStringField(dbmot, "skd_mobilita").equals(
								"4"))
							mobilita = "immobile";
						ht.put("#mobilita#", mobilita);

						if (getStringField(dbmot, "skd_consulenza").equals("1"))
							consulenza = "fisiatra";
						else if (getStringField(dbmot, "skd_consulenza")
								.equals("2"))
							consulenza = "fisioterapista";
						else if (getStringField(dbmot, "skd_consulenza")
								.equals("3"))
							consulenza = "altro";
						else if (getStringField(dbmot, "skd_consulenza")
								.equals("4"))
							consulenza = "non richiesta";
						ht.put("#rich_consu#", consulenza);

						ht = faiSiNo(ht, "post", getStringField(dbmot,
								"skd_posture_obb"));
						ht.put("#cambi_post#", getCambiPostu(getStringField(
								dbmot, "skd_posture")));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaMotric", ht);
						i++;
					}
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaMotric(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaAltriPar(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		int i = 0;

		try {
			String select = "SELECT skx_data,skx_vista,skx_udito,skx_sonno,skx_note "
					+ " FROM skialtro "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skx_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_altro", "def_vista", "def_udi", "sonno" });
				eve.writeSostituisci("paginaAltro", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaMotric(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_altro", "def_vista", "def_udi", "sonno" });
					eve.writeSostituisci("paginaAltro", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbalt = dbcur.getRecord();
						String def_vista = "";
						String def_udi = "";
						String sonno = "";
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht
								.put("#data_scheda#", getDateField(dbalt,
										"skx_data"));
						ht.put("#note_altro#",
								getStringField(dbalt, "skx_note"));
						if (getStringField(dbalt, "skx_vista").equals("1"))
							def_vista = "no";
						else if (getStringField(dbalt, "skx_vista").equals("2"))
							def_vista = "si, corretto";
						else if (getStringField(dbalt, "skx_vista").equals("3"))
							def_vista = "si, non corretto";
						ht.put("#def_vista#", def_vista);

						if (getStringField(dbalt, "skx_udito").equals("1"))
							def_udi = "no";
						else if (getStringField(dbalt, "skx_udito").equals("2"))
							def_udi = "si, corretto";
						else if (getStringField(dbalt, "skx_udito").equals("3"))
							def_udi = "si, non corretto";
						ht.put("#def_udi#", def_udi);

						if (getStringField(dbalt, "skx_sonno").equals("1"))
							sonno = "sonno spontaneo e regolare";
						else if (getStringField(dbalt, "skx_sonno").equals("2"))
							sonno = "con aiuto farmacologico";
						else if (getStringField(dbalt, "skx_sonno").equals("3"))
							sonno = "con aiuto con aiuto di tecniche complementari";
						else if (getStringField(dbalt, "skx_sonno").equals("4"))
							sonno = "insonnia";
						else if (getStringField(dbalt, "skx_sonno").equals("5"))
							sonno = "sonno intermittente";
						ht.put("#sonno#", sonno);

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaAltro", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaAltriPar(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaLesioniDecub(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skdt_data,skdt_dolore,skdt_note, skdt_trattamento "
					+ " FROM skidecubito_t "
					+ " WHERE n_cartella="
					+ n_cartella + " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skdt_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"pres_dolore", "note_decub", "trattamento" });
				eve.writeSostituisci("paginaLesioniDecub", hterr);
				eve.write("fineTabPaginaLesioniDecub");
				System.out.println("FoSkInfeEJB.scriviPaginaLesioniDecub(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"pres_dolore", "note_decub", "trattamento" });
					eve.writeSostituisci("paginaLesioniDecub", hterr1);
					eve.write("fineTabPaginaLesioniDecub");
				} else {
					while (dbcur.next()) {
						ISASRecord dblesd = dbcur.getRecord();

						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#", getDateField(dblesd,
								"skdt_data"));
						ht.put("#pres_dolore#",
								getPresenzaDolore(getStringField(dblesd,
										"skdt_dolore")));
						ht.put("#note_decub#", getStringField(dblesd,
								"skdt_note"));
						ht.put("#trattamento#", getStringField(dblesd,
								"skdt_trattamento"));

						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("paginaLesioniDecub", ht);
						queryLesioniDecub(dbc, dblesd, eve);
						eve.write("fineTabPaginaLesioniDecub");
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaLesioniDecub(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaUlcerePie(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skut_data,skut_dolore,skut_note,skut_trattamento "
					+ " FROM skiulcere_t "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skut_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"pres_dolore", "note_ulcere", "skut_trattamento" });
				eve.writeSostituisci("paginaUlcerePie", hterr);
				eve.write("fineTabPaginaUlcerePie");
				System.out.println("FoSkInfeEJB.scriviPaginaUlcerePie(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"pres_dolore", "note_ulcere", "skut_trattamento" });
					eve.writeSostituisci("paginaUlcerePie", hterr1);
					eve.write("fineTabPaginaUlcerePie");
				} else {
					while (dbcur.next()) {
						ISASRecord dbulc = dbcur.getRecord();

						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbulc, "skut_data"));
						ht.put("#pres_dolore#",
								getPresenzaDolore(getStringField(dbulc,
										"skut_dolore")));
						ht.put("#note_ulcere#", getStringField(dbulc,
								"skut_note"));
						ht.put("#skut_trattamento#", getStringField(dbulc,
								"skut_trattamento"));

						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("paginaUlcerePie", ht);
						queryUlcerePie(dbc, dbulc, eve);
						eve.write("fineTabPaginaUlcerePie");
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaUlcerePie(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaLesVasco(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skvt_data,skvt_tipo,skvt_edema,skvt_eritema,"
					+ "skvt_varici,skvt_pesantezza,skvt_dolore,skvt_epis_flebici,"
					+ "skvt_lesio_arti,skvt_specialista,skvt_note,skvt_trattamento "
					+ " FROM skivasco_t "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;

			if (data_scheda != null)
				select = select + " AND skvt_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"tipo_lesione", "pres_dolore", "ede_s", "ede_n",
						"eri_s", "eri_n", "var_s", "var_n", "pesa_s", "pesa_n",
						"fleb_s", "fleb_n", "inf_s", "inf_n", "spe_s", "spe_n",
						"note_vasco", "skvt_trattamento" });
				eve.writeSostituisci("paginaLesVasco", hterr);
				eve.write("fineTabPaginaLesVasco");
				System.out.println("FoSkInfeEJB.scriviPaginaLesVasco(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1,
							new String[] { "data_scheda", "tipo_lesione",
									"pres_dolore", "ede_s", "ede_n", "eri_s",
									"eri_n", "var_s", "var_n", "pesa_s",
									"pesa_n", "fleb_s", "fleb_n", "inf_s",
									"inf_n", "spe_s", "spe_n", "note_vasco",
									"skvt_trattamento" });
					eve.writeSostituisci("paginaLesVasco", hterr1);
					eve.write("fineTabPaginaLesVasco");
				} else {
					while (dbcur.next()) {
						ISASRecord dbvas = dbcur.getRecord();
						String tipoles = "";
						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbvas, "skvt_data"));
						if (getStringField(dbvas, "skvt_tipo").equals("1"))
							tipoles = "Arteriosa";
						else if (getStringField(dbvas, "skvt_tipo").equals("2"))
							tipoles = "Venosa";
						else if (getStringField(dbvas, "skvt_tipo").equals("3"))
							tipoles = "Mista";
						else if (getStringField(dbvas, "skvt_tipo").equals("4"))
							tipoles = "Non rilevabile";
						ht.put("#tipo_lesione#", tipoles);
						ht.put("#pres_dolore#",
								getPresenzaDolore(getStringField(dbvas,
										"skvt_dolore")));
						ht = faiSiNo(ht, "ede", getStringField(dbvas,
								"skvt_edema"));
						ht = faiSiNo(ht, "eri", getStringField(dbvas,
								"skvt_eritema"));
						ht = faiSiNo(ht, "var", getStringField(dbvas,
								"skvt_varici"));
						ht = faiSiNo(ht, "pesa", getStringField(dbvas,
								"skvt_pesantezza"));
						ht = faiSiNo(ht, "fleb", getStringField(dbvas,
								"skvt_epis_flebici"));
						ht = faiSiNo(ht, "inf", getStringField(dbvas,
								"skvt_lesio_arti"));
						ht = faiSiNo(ht, "spe", getStringField(dbvas,
								"skvt_specialista"));
						ht.put("#note_vasco#", getStringField(dbvas,
								"skvt_note"));
						ht.put("#skvt_trattamento#", getStringField(dbvas,
								"skvt_trattamento"));

						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("paginaLesVasco", ht);
						queryLesioniVasco(dbc, dbvas, eve);
						eve.write("fineTabPaginaLesVasco");
						i++;
					}
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaLesVasco(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaAlterazioni(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skal_data,skal_note " + " FROM skialteraz "
					+ " WHERE n_cartella=" + n_cartella + " AND n_contatto="
					+ n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);

			if (data_scheda != null)
				select = select + " AND skal_data="
						+ formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);
			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"note_altre" });
				eve.writeSostituisci("paginaAlterazioni", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaAlterazioni(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"note_altre" });
					eve.writeSostituisci("paginaAlterazioni", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbalte = dbcur.getRecord();

						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#", getDateField(dbalte,
								"skal_data"));
						ht.put("#note_altre#", getStringField(dbalte,
								"skal_note"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaAlterazioni", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaAlterazioni(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaIndDiagno(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			String select = "SELECT skes_data,skes_esame,skes_risultato,skes_note "
					+ " FROM skiesami "
					+ " WHERE n_cartella="
					+ n_cartella
					+ " AND n_contatto=" + n_contatto;
			// " AND ski_data_apertura="+formatDate(dbc,data_apertura);
			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#n_contatto#", n_contatto); // hterr.put("#data_apertura_contatto#",dataIta(data_apertura));
				hterr = faiVuota(hterr, new String[] { "data_scheda",
						"esame_diagn", "risult_diagn", "annota_diagn" });
				eve.writeSostituisci("paginaIndDiagno", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaIndDiagno(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#n_contatto#", n_contatto); // hterr1.put("#data_apertura_contatto#",dataIta(data_apertura));
					hterr1 = faiVuota(hterr1, new String[] { "data_scheda",
							"esame_diagn", "risult_diagn", "annota_diagn" });
					eve.writeSostituisci("paginaIndDiagno", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbesa = dbcur.getRecord();

						Hashtable ht = new Hashtable();
						ht.put("#n_cartella#", n_cartella);
						ht.put("#nome_assistito#", intesta_nome);
						ht.put("#n_contatto#", n_contatto); // ht.put("#data_apertura_contatto#",dataIta(data_apertura));
						ht.put("#data_scheda#",
								getDateField(dbesa, "skes_data"));
						ht.put("#esame_diagn#", getStringField(dbesa,
								"skes_esame"));
						ht.put("#risult_diagn#", getStringField(dbesa,
								"skes_risultato"));
						ht.put("#annota_diagn#", getStringField(dbesa,
								"skes_note"));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("paginaIndDiagno", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaIndDiagno(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaTabAdl(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;

		try {
			/***
			 * 16/11/06 m. String select=
			 * "SELECT bere,naso,pettinarsi,unghie,radersi,mangiare,rubinetto,luce,"
			 * +
			 * " bottoni,pantofole,denti,telefono,firmare,porta,ora,cammin,nome,data_test"
			 * + " FROM tabadl "+ " WHERE n_cartella='"+n_cartella+"'"+
			 * " AND n_contatto='"+n_contatto+"'";
			 ***/
			// 16/11/06 m.
			String select = "SELECT * FROM sc_adl" + " WHERE n_cartella = '"
					+ n_cartella + "'";

			if (data_scheda != null)
				select = select + " AND data=" + formatDate(dbc, data_scheda);
			else {// 16/11/06 m.: scale comprese nel contatto
				select += " AND data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					select += " AND data <= " + formatDate(dbc, data_chiusura);

				select += " ORDER BY data";
			}

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				// 16/11/06 m. hterr.put("#n_contatto#",n_contatto);
				hterr = faiVuota(hterr, new String[] { "cognome", "nome",
						"indirizzo", "comune", "provincia", "data_test",
						"nome_test", "bere", "naso", "pettine", "unghie",
						"rader", "cucchi", "rubin", "luce", "bottoni", "panto",
						"denti", "telef", "firma", "porta", "ora", "passi",
						"adl_punt" });
				eve.writeSostituisci("tabAdl", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaTabAdl(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					// 16/11/06 m. hterr1.put("#n_contatto#",n_contatto);
					hterr1 = faiVuota(hterr1, new String[] { "cognome", "nome",
							"indirizzo", "comune", "provincia", "data_test",
							"nome_test", "bere", "naso", "pettine", "unghie",
							"rader", "cucchi", "rubin", "luce", "bottoni",
							"panto", "denti", "telef", "firma", "porta", "ora",
							"passi", "adl_punt" });
					eve.writeSostituisci("tabAdl", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbr = dbcur.getRecord();

						Hashtable ht = new Hashtable();

						ht.put("#cognome#", getStringField(dbana, "cognome"));
						ht.put("#nome#", getStringField(dbana, "nome"));
						ht.put("#indirizzo#",
								getStringField(dbana, "indirizzo"));
						ht.put("#comune#", getDecodifica(dbc, "descrizione",
								"codice", "comuni", getStringField(dbana,
										"citta")));
						ht.put("#provincia#", getStringField(dbana, "prov"));

						ht.put("#data_test#", getDateField(dbr, "data_test"));
						ht.put("#nome_test#", getStringField(dbr, "nome"));
						ht.put("#adl_punt#", getStringField(dbr, "adl_punt"));

						ht.put("#bere#", faiCrocetta("1", getStringField(dbr,
								"bere")));
						ht.put("#naso#", faiCrocetta("1", getStringField(dbr,
								"naso")));
						ht.put("#pettine#", faiCrocetta("1", getStringField(
								dbr, "pettinarsi")));

						ht.put("#unghie#", faiCrocetta("1", getStringField(dbr,
								"unghie")));
						ht.put("#rader#", faiCrocetta("1", getStringField(dbr,
								"radersi")));
						ht.put("#cucchi#", faiCrocetta("1", getStringField(dbr,
								"mangiare")));

						ht.put("#rubin#", faiCrocetta("1", getStringField(dbr,
								"rubinetto")));
						ht.put("#luce#", faiCrocetta("1", getStringField(dbr,
								"luce")));
						ht.put("#bottoni#", faiCrocetta("1", getStringField(
								dbr, "bottoni")));

						ht.put("#panto#", faiCrocetta("1", getStringField(dbr,
								"pantofole")));
						ht.put("#denti#", faiCrocetta("1", getStringField(dbr,
								"denti")));
						ht.put("#telef#", faiCrocetta("1", getStringField(dbr,
								"telefono")));

						ht.put("#firma#", faiCrocetta("1", getStringField(dbr,
								"firmare")));
						ht.put("#porta#", faiCrocetta("1", getStringField(dbr,
								"porta")));
						ht.put("#ora#", faiCrocetta("1", getStringField(dbr,
								"ora")));
						ht.put("#passi#", faiCrocetta("1", getStringField(dbr,
								"cammin")));

						if (i > 0)
							eve.write("taglia");

						eve.writeSostituisci("tabAdl", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaTabAdl(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaTabIAdl(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;
		try {
			/***
			 * 16/11/06 m. String select=
			 * "SELECT telefono,acquisti,spostam,medicinali,denaro,nome,data_test"
			 * + " FROM tabiadl "+ " WHERE n_cartella='"+n_cartella+"'"+
			 * " AND n_contatto='"+n_contatto+"'";
			 ***/
			// 16/11/06
			String select = "SELECT * FROM sc_iadl" + " WHERE n_cartella = '"
					+ n_cartella + "'";

			if (data_scheda != null)
				select = select + " AND data=" + formatDate(dbc, data_scheda);
			else {// 16/11/06 m.: scale comprese nel contatto
				select += " AND data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					select += " AND data <= " + formatDate(dbc, data_chiusura);

				select += " ORDER BY data";
			}

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				// 16/11/06 m. hterr.put("#n_contatto#",n_contatto);
				hterr = faiVuota(hterr, new String[] { "cognome", "nome",
						"indirizzo", "comune", "provincia", "data_test",
						"nome_test", "telIni", "telCon", "telRisp", "telNo",
						"speSA", "pacqSA", "acqAcc", "acqInc", "trasPub",
						"notrasPub", "trasPubAcc", "trasPubAcc", "trasAltri",
						"noSpost", "medOK", "medPrep", "medNo", "denAuto",
						"piccAcq", "denInca", "iadl_punt" });
				eve.writeSostituisci("tabIAdl", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaTabAdl(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					// 16/11/06 m. hterr1.put("#n_contatto#",n_contatto);
					hterr1 = faiVuota(hterr1, new String[] { "cognome", "nome",
							"indirizzo", "comune", "provincia", "data_test",
							"nome_test", "telIni", "telCon", "telRisp",
							"telNo", "speSA", "pacqSA", "acqAcc", "acqInc",
							"trasPub", "notrasPub", "trasPubAcc", "trasPubAcc",
							"trasAltri", "noSpost", "medOK", "medPrep",
							"medNo", "denAuto", "piccAcq", "denInca",
							"iadl_punt" });
					eve.writeSostituisci("tabIAdl", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbr = dbcur.getRecord();
						Hashtable ht = new Hashtable();

						ht.put("#cognome#", getStringField(dbana, "cognome"));
						ht.put("#nome#", getStringField(dbana, "nome"));
						ht.put("#indirizzo#",
								getStringField(dbana, "indirizzo"));
						ht.put("#comune#", getDecodifica(dbc, "descrizione",
								"codice", "comuni", getStringField(dbana,
										"citta")));
						ht.put("#provincia#", getStringField(dbana, "prov"));

						ht.put("#data_test#", getDateField(dbr, "data_test"));
						ht.put("#nome_test#", getStringField(dbr, "nome"));
						ht.put("#iadl_punt#", getStringField(dbr, "iadl_punt"));

						ht.put("#telIni#", faiCrocetta("1", getStringField(dbr,
								"iadl_telefono")));
						ht.put("#telCon#", faiCrocetta("2", getStringField(dbr,
								"iadl_telefono")));
						ht.put("#telRisp#", faiCrocetta("3", getStringField(
								dbr, "iadl_telefono")));
						ht.put("#telNo#", faiCrocetta("0", getStringField(dbr,
								"iadl_telefono")));

						ht.put("#speSA#", faiCrocetta("3", getStringField(dbr,
								"iadl_acquisti")));
						ht.put("#pacqSA#", faiCrocetta("2", getStringField(dbr,
								"iadl_acquisti")));
						ht.put("#acqAcc#", faiCrocetta("1", getStringField(dbr,
								"iadl_acquisti")));
						ht.put("#acqInc#", faiCrocetta("0", getStringField(dbr,
								"iadl_acquisti")));

						ht.put("#trasPub#", faiCrocetta("4", getStringField(
								dbr, "iadl_spostam")));
						ht.put("#notrasPub#", faiCrocetta("3", getStringField(
								dbr, "iadl_spostam")));
						ht.put("#trasPubAcc#", faiCrocetta("2", getStringField(
								dbr, "iadl_spostam")));
						ht.put("#trasAltri#", faiCrocetta("1", getStringField(
								dbr, "iadl_spostam")));
						ht.put("#noSpost#", faiCrocetta("0", getStringField(
								dbr, "iadl_spostam")));

						ht.put("#medOK#", faiCrocetta("2", getStringField(dbr,
								"iadl_medicinali")));
						ht.put("#medPrep#", faiCrocetta("1", getStringField(
								dbr, "iadl_medicinali")));
						ht.put("#medNo#", faiCrocetta("0", getStringField(dbr,
								"iadl_medicinali")));

						ht.put("#denAuto#", faiCrocetta("2", getStringField(
								dbr, "iadl_denaro")));
						ht.put("#piccAcq#", faiCrocetta("1", getStringField(
								dbr, "iadl_denaro")));
						ht.put("#denInca#", faiCrocetta("0", getStringField(
								dbr, "iadl_denaro")));

						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("tabIAdl", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaTabIAdl(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaPfeiffer(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;
		try {
			/***
			 * 16/11/06 m. String select=
			 * "SELECT oggi,giorno,posto,telefono,indirizzo,anni,nato,presidente,"
			 * + " precedente,madre,sottraz,nome,data_test"+ " FROM pfeiffer "+
			 * " WHERE n_cartella='"+n_cartella+"'"+
			 * " AND n_contatto='"+n_contatto+"'";
			 ***/
			// 16/11/06 m.
			String select = "SELECT * FROM sc_pfeiffer"
					+ " WHERE n_cartella = '" + n_cartella + "'";

			if (data_scheda != null)
				select = select + " AND data=" + formatDate(dbc, data_scheda);
			else {// 16/11/06 m.: scale comprese nel contatto
				select += " AND data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					select += " AND data <= " + formatDate(dbc, data_chiusura);

				select += " ORDER BY data";
			}

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				// 16/11/06 m. hterr.put("#n_contatto#",n_contatto);
				hterr = faiVuota(hterr, new String[] { "cognome", "nome",
						"indirizzo", "comune", "provincia", "data_test",
						"nome_test", "dataS", "dataN", "gioS", "gioN", "postS",
						"postN", "telS", "telN", "indS", "indN", "etaS",
						"etaN", "natS", "natN", "presS", "presN", "oldPreS",
						"oldPreN", "mamS", "mamN", "matS", "matN",
						"pfeiffer_punt" });
				eve.writeSostituisci("pfeiffer", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaTabAdl(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					// 16/11/06 m. hterr1.put("#n_contatto#",n_contatto);
					hterr1 = faiVuota(hterr1, new String[] { "cognome", "nome",
							"indirizzo", "comune", "provincia", "data_test",
							"nome_test", "dataS", "dataN", "gioS", "gioN",
							"postS", "postN", "telS", "telN", "indS", "indN",
							"etaS", "etaN", "natS", "natN", "presS", "presN",
							"oldPreS", "oldPreN", "mamS", "mamN", "matS",
							"matN", "pfeiffer_punt" });
					eve.writeSostituisci("pfeiffer", hterr1);
				} else {
					while (dbcur.next()) {
						ISASRecord dbr = dbcur.getRecord();
						Hashtable ht = new Hashtable();

						ht.put("#cognome#", getStringField(dbana, "cognome"));
						ht.put("#nome#", getStringField(dbana, "nome"));
						ht.put("#indirizzo#",
								getStringField(dbana, "indirizzo"));
						ht.put("#comune#", getDecodifica(dbc, "descrizione",
								"codice", "comuni", getStringField(dbana,
										"citta")));
						ht.put("#provincia#", getStringField(dbana, "prov"));

						ht.put("#data_test#", getDateField(dbr, "data_test"));
						ht.put("#nome_test#", getStringField(dbr, "nome"));
						ht.put("#pfeiffer_punt#", getStringField(dbr,
								"pfeiffer_punt"));

						ht.put("#dataS#", faiCrocetta("1", getStringField(dbr,
								"oggi")));
						ht.put("#dataN#", faiCrocetta("0", getStringField(dbr,
								"oggi")));
						ht.put("#gioS#", faiCrocetta("1", getStringField(dbr,
								"giorno")));
						ht.put("#gioN#", faiCrocetta("0", getStringField(dbr,
								"giorno")));

						ht.put("#postS#", faiCrocetta("1", getStringField(dbr,
								"posto")));
						ht.put("#postN#", faiCrocetta("0", getStringField(dbr,
								"posto")));
						ht.put("#telS#", faiCrocetta("1", getStringField(dbr,
								"telefono")));
						ht.put("#telN#", faiCrocetta("0", getStringField(dbr,
								"telefono")));

						ht.put("#indS#", faiCrocetta("1", getStringField(dbr,
								"indirizzo")));
						ht.put("#indN#", faiCrocetta("0", getStringField(dbr,
								"indirizzo")));
						ht.put("#etaS#", faiCrocetta("1", getStringField(dbr,
								"anni")));
						ht.put("#etaN#", faiCrocetta("0", getStringField(dbr,
								"anni")));

						ht.put("#natS#", faiCrocetta("1", getStringField(dbr,
								"nato")));
						ht.put("#natN#", faiCrocetta("0", getStringField(dbr,
								"nato")));
						ht.put("#presS#", faiCrocetta("1", getStringField(dbr,
								"presidente")));
						ht.put("#presN#", faiCrocetta("0", getStringField(dbr,
								"presidente")));

						ht.put("#oldPreS#", faiCrocetta("1", getStringField(
								dbr, "precedente")));
						ht.put("#oldPreN#", faiCrocetta("0", getStringField(
								dbr, "precedente")));
						ht.put("#mamS#", faiCrocetta("1", getStringField(dbr,
								"madre")));
						ht.put("#mamN#", faiCrocetta("0", getStringField(dbr,
								"madre")));
						ht.put("#matS#", faiCrocetta("1", getStringField(dbr,
								"sottraz")));
						ht.put("#matN#", faiCrocetta("0", getStringField(dbr,
								"sottraz")));

						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("pfeiffer", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaPfeiffer(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaBarthel(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;
		try {
			String select = "SELECT * FROM sc_barthel" + " WHERE n_cartella = "
					+ n_cartella;

			if (data_scheda != null)
				select = select + " AND skbt_data="
						+ formatDate(dbc, data_scheda);
			else {// scale comprese nel contatto
				select += " AND skbt_data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					select += " AND skbt_data <= "
							+ formatDate(dbc, data_chiusura);

				select += " ORDER BY skbt_data";
			}

			ISASCursor dbcur = dbc.startCursor(select);

			if (dbcur == null || dbcur.getDimension() <= 0) {
				Hashtable ht = new Hashtable();
				ht.put("#n_cartella#", n_cartella);
				ht.put("#nome_assistito#", intesta_nome);
				for (int l = 0; l <= 11; l++) {
					for (int j = 0; j <= 4; j++) {
						ht.put("#check" + l + "." + j + "#", "");
					}
				}
				ht.put("#totale#", "__________");
				ht.put("#data_test#", "__/__/____");
				ht.put("#nome_test#", "__________");
				ht.put("#data#", "__/__/____");
				ht.put("#livello#", "__________");
				eve.writeSostituisci("paginaBarthel", ht);
				System.out.println("FoSkInfeEJB.scriviPaginaBarthel(): "
						+ "cursore non valido");
			} else {
				while (dbcur.next()) {
					ISASRecord par = dbcur.getRecord();
					Hashtable ht = new Hashtable();
					ht.put("#n_cartella#", n_cartella);
					ht.put("#nome_assistito#", intesta_nome);

					int chiave = 0;
					int totale = 0;
					for (int m = 0; m <= 11; m++) {
						for (int j = 0; j <= 4; j++) {
							ht.put("#check" + m + "." + j + "#", "");
						}
					}
					ht.put("#totale#", "__________");
					ht.put("#data_test#", "__/__/____");
					ht.put("#nome_test#", "__________");
					ht.put("#data#", "__/__/____");
					ht.put("#livello#", "__________");
					chiave = ((Integer) par.get("skbt_igiene_pers")).intValue();
					switch (chiave) {
					case 0:
						ht.put("#check1.0#", "x");
						break;
					case 1:
						ht.put("#check1.1#", "x");
						break;
					case 3:
						ht.put("#check1.2#", "x");
						break;
					case 4:
						ht.put("#check1.3#", "x");
						break;
					case 5:
						ht.put("#check1.4#", "x");
						break;
					}
					System.out.println("****2");
					chiave = ((Integer) par.get("skbt_bagno")).intValue();
					switch (chiave) {
					case 0:
						ht.put("#check2.0#", "x");
						break;
					case 1:
						ht.put("#check2.1#", "x");
						break;
					case 3:
						ht.put("#check2.2#", "x");
						break;
					case 4:
						ht.put("#check2.3#", "x");
						break;
					case 5:
						ht.put("#check2.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_alimentazione"))
							.intValue();
					switch (chiave) {
					case 0:
						ht.put("#check3.0#", "x");
						break;
					case 2:
						ht.put("#check3.1#", "x");
						break;
					case 5:
						ht.put("#check3.2#", "x");
						break;
					case 8:
						ht.put("#check3.3#", "x");
						break;
					case 10:
						ht.put("#check3.4#", "x");
						break;
					}
					System.out.println("****3");
					chiave = ((Integer) par.get("skbt_abbigliamento"))
							.intValue();
					switch (chiave) {
					case 0:
						ht.put("#check4.0#", "x");
						break;
					case 2:
						ht.put("#check4.1#", "x");
						break;
					case 5:
						ht.put("#check4.2#", "x");
						break;
					case 8:
						ht.put("#check4.3#", "x");
						break;
					case 10:
						ht.put("#check4.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_cont_intestinale"))
							.intValue();
					switch (chiave) {
					case 0:
						ht.put("#check5.0#", "x");
						break;
					case 2:
						ht.put("#check5.1#", "x");
						break;
					case 5:
						ht.put("#check5.2#", "x");
						break;
					case 8:
						ht.put("#check5.3#", "x");
						break;
					case 10:
						ht.put("#check5.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_cont_urinaria"))
							.intValue();
					switch (chiave) {
					case 0:
						ht.put("#check6.0#", "x");
						break;
					case 2:
						ht.put("#check6.1#", "x");
						break;
					case 5:
						ht.put("#check6.2#", "x");
						break;
					case 8:
						ht.put("#check6.3#", "x");
						break;
					case 10:
						ht.put("#check6.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_trasf_lettosedia"))
							.intValue();
					switch (chiave) {
					case 0:
						ht.put("#check7.0#", "x");
						break;
					case 3:
						ht.put("#check7.1#", "x");
						break;
					case 8:
						ht.put("#check7.2#", "x");
						break;
					case 12:
						ht.put("#check7.3#", "x");
						break;
					case 15:
						ht.put("#check7.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_toilette")).intValue();
					switch (chiave) {
					case 0:
						ht.put("#check8.0#", "x");
						break;
					case 2:
						ht.put("#check8.1#", "x");
						break;
					case 5:
						ht.put("#check8.2#", "x");
						break;
					case 8:
						ht.put("#check8.3#", "x");
						break;
					case 10:
						ht.put("#check8.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_scale")).intValue();
					switch (chiave) {
					case 0:
						ht.put("#check9.0#", "x");
						break;
					case 2:
						ht.put("#check9.1#", "x");
						break;
					case 5:
						ht.put("#check9.2#", "x");
						break;
					case 8:
						ht.put("#check9.3#", "x");
						break;
					case 10:
						ht.put("#check9.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_deambulazione"))
							.intValue();
					switch (chiave) {
					case 0:
						ht.put("#check10.0#", "x");
						break;
					case 3:
						ht.put("#check10.1#", "x");
						break;
					case 8:
						ht.put("#check10.2#", "x");
						break;
					case 12:
						ht.put("#check10.3#", "x");
						break;
					case 15:
						ht.put("#check10.4#", "x");
						break;
					}
					chiave = ((Integer) par.get("skbt_carrozzina")).intValue();
					switch (chiave) {
					case 0:
						ht.put("#check11.0#", "x");
						break;
					case 1:
						ht.put("#check11.1#", "x");
						break;
					case 3:
						ht.put("#check11.2#", "x");
						break;
					case 4:
						ht.put("#check11.3#", "x");
						break;
					case 5:
						ht.put("#check11.4#", "x");
						break;
					}
					if (par.get("skbt_tipo") != null) {
						chiave = ((Integer) par.get("skbt_tipo")).intValue();
						System.out.println("chiave:" + chiave);
						if (chiave != 0) {
							switch (chiave) {
							case 1:
								ht.put("#livello#", "Iniziale");
								break;
							case 2:
								ht.put("#livello#", "Intermedia");
								break;
							case 3:
								ht.put("#livello#", "Finale");
								break;
							}
						} else
							ht.put("#livello#", "");
					} else
						ht.put("#livello#", "");

					if (par.get("data_test") != null) {
						String data = "" + (java.sql.Date) par.get("data_test");
						data = data.substring(8, 10) + "/"
								+ data.substring(5, 7) + "/"
								+ data.substring(0, 4);
						ht.put("#data_test#", data);
					}
					if (par.get("skbt_data") != null) {
						String data = "" + (java.sql.Date) par.get("skbt_data");
						data = data.substring(8, 10) + "/"
								+ data.substring(5, 7) + "/"
								+ data.substring(0, 4);
						ht.put("#data#", data);
					}
					if (par != null && par.get("skbt_punt") != null) {
						totale = Integer.parseInt("" + par.get("skbt_punt"));

					}
					if (totale > 0) {
						ht.put("#totale#", "" + totale);
					}
					if (par.get("nome") != null)
						ht.put("#nome_test#", "" + par.get("nome"));

					if (i > 0)
						eve.write("taglia");
					eve.writeSostituisci("paginaBarthel", ht);
					i++;
				}
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaBarthel(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaCaregiver(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;
		try {

			/***
			 * 16/11/06 m. String sel="SELECT * FROM skicaregiver "+
			 * " WHERE n_cartella="+n_cartella+ " AND n_contatto="+n_contatto;
			 ***/
			// 16/11/06 m.
			String sel = "SELECT * FROM sc_caregiver" + " WHERE n_cartella = "
					+ n_cartella;

			if (data_scheda != null)
				sel = sel + " AND skcg_data=" + formatDate(dbc, data_scheda);
			else { // 16/11/06 m.: scale comprese nel contatto
				sel += " AND skcg_data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					sel += " AND skcg_data <= "
							+ formatDate(dbc, data_chiusura);

				sel += " ORDER BY skcg_data";
			}

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				Hashtable hterrA = new Hashtable();
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#skcg_data#", "");

				/***
				 * 16/11/06 m. String [] hvuota = new String[] {}; for(int
				 * j=1;j<23; j++){ if(j<10) hvuota[j]="skcg_0"+j; else
				 * hvuota[j]="skcg_"+j; } hvuota[23] = "skcg_23";
				 ***/
				// 16/11/06 m. ---
				String[] hvuota = new String[23];
				for (int j = 0; j < 23; j++)
					hvuota[j] = "skcg_" + (j < 9 ? "0" : "") + (j + 1);
				// 16/11/06 m. ---

				hterr = faiVuota(hterr, hvuota);
				eve.writeSostituisci("paginaCaregiver", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaIndCaregiver(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					Hashtable hterr2 = new Hashtable();
					Hashtable hterr3 = new Hashtable();
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#skcg_data#", "");
					/***
					 * 16/11/06 m. String [] hvuota = new String[] {}; for(int
					 * j=1;j<23; j++){ if(j<10) hvuota[j]="skcg_0"+j; else
					 * hvuota[j]="skcg_"+j; } hvuota[23] = "skcg_23";
					 ***/
					// 16/11/06 m. ---
					String[] hvuota = new String[23];
					for (int j = 0; j < 23; j++)
						hvuota[j] = "skcg_" + (j < 9 ? "0" : "") + (j + 1);
					// 16/11/06 m. ---

					hterr1 = faiVuota(hterr1, hvuota);
					eve.writeSostituisci("paginaCaregiver", hterr1);
				} else {
					it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
					while (dbcur.next()) {

						if (i > 0)
							eve.write("taglia");

						ISASRecord dbter = dbcur.getRecord();
						Hashtable ht_a = new Hashtable();
						ht_a.put("#nome_assistito#", intesta_nome);
						ht_a.put("#skcg_data#",
								getDateField(dbter, "skcg_data"));

						for (int j = 1; j < 23; j++) {
							String val = "";
							if (j > 9) {
								val = (String) ut.getObjectField(dbter, "skcg_"
										+ j, 'I');
								ht_a.put("#skcg_" + j + "#", val);
							} else {
								val = (String) ut.getObjectField(dbter,
										"skcg_0" + j, 'I');
								ht_a.put("#skcg_0" + j + "#", val);
							}
						}
						ht_a.put("#skcg_23#", (String) ut.getObjectField(dbter,
								"skcg_valutazione", 'I'));
						eve.writeSostituisci("paginaCaregiver", ht_a);

						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale

		} catch (Exception e) {
			debugMessage("scriviPaginaCaregiver(" + dbc + " ) : " + e);
		}

	}

	private void scriviPaginaBraden(ISASConnection dbc, mergeDocument eve)
			throws SQLException {

		int i = 0;
		try {

			/***
			 * 16/11/06 m. String sel="SELECT * FROM skibraden "+
			 * " WHERE n_cartella="+n_cartella+ " AND n_contatto="+n_contatto;
			 ***/
			// 16/11/06 m.
			String sel = "SELECT * FROM sc_braden" + " WHERE n_cartella = "
					+ n_cartella;

			if (data_scheda != null)
				sel = sel + " AND skb_data=" + formatDate(dbc, data_scheda);
			else { // 16/11/06 m.: scale comprese nel contatto
				sel += " AND skb_data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					sel += " AND skb_data <= " + formatDate(dbc, data_chiusura);

				sel += " ORDER BY skb_data";
			}

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				Hashtable hterrI = new Hashtable();
				Hashtable hterrA = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				// 16/11/06 m. hterr.put("#n_contatto#",n_contatto);
				hterr.put("#skb_data#", "");
				hterr.put("#skb_percezione_sens#", "");
				hterr.put("#skb_umidita#", "");
				hterr.put("#skb_attivita#", "");
				hterr.put("#skb_mobilita#", "");
				hterr.put("#skb_nutrizione#", "");
				hterr.put("#skb_fraz_sciv#", "");
				hterr.put("#skb_valutazione#", "");
				eve.writeSostituisci("paginaBraden", hterr);
				System.out.println("FoSkInfeEJB.scriviPaginaBraden(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					Hashtable hterr2 = new Hashtable();
					Hashtable hterr3 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					// 16/11/06 m. hterr1.put("#n_contatto#",n_contatto);
					hterr1.put("#skb_data#", "");
					hterr1.put("#skb_percezione_sens#", "");
					hterr1.put("#skb_umidita#", "");
					hterr1.put("#skb_attivita#", "");
					hterr1.put("#skb_mobilita#", "");
					hterr1.put("#skb_nutrizione#", "");
					hterr1.put("#skb_fraz_sciv#", "");
					hterr1.put("#skb_valutazione#", "");

					eve.writeSostituisci("paginaBraden", hterr1);
				} else {
					it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
					while (dbcur.next()) {

						if (i > 0)
							eve.write("taglia");

						ISASRecord dbter = dbcur.getRecord();
						Hashtable ht_a = new Hashtable();

						ht_a.put("#n_cartella#", n_cartella);
						ht_a.put("#nome_assistito#", intesta_nome);
						// 16/11/06 m. ht_a.put("#n_contatto#",n_contatto);
						ht_a.put("#skb_data#", getDateField(dbter, "skb_data"));

						String psens = ""
								+ ut.getObjectField(dbter,
										"skb_percezione_sens", 'I');
						if (psens.equals("1"))
							psens = "COMPLETAMENTE LIMITATA";
						else if (psens.equals("2"))
							psens = "MOLTO LIMITATA";
						else if (psens.equals("3"))
							psens = "LEGGERMENTE LIMITATA";
						else if (psens.equals("4"))
							psens = "NON LIMITATA";
						ht_a.put("#skb_percezione_sens#", psens);

						String umi = ""
								+ ut.getObjectField(dbter, "skb_umidita", 'I');
						if (umi.equals("1"))
							umi = "COSTANTEMENTE BAGNATO";
						else if (umi.equals("2"))
							umi = "SPESSO BAGNATO";
						else if (umi.equals("3"))
							umi = "OCCASIONALMENTE BAGNATO";
						else if (umi.equals("4"))
							umi = "RARAMENTE BAGNATO";
						ht_a.put("#skb_umidita#", umi);

						String att = ""
								+ ut.getObjectField(dbter, "skb_attivita", 'I');
						if (att.equals("1"))
							att = "ALLETTATO";
						else if (att.equals("2"))
							att = "IN POLTRONA";
						else if (att.equals("3"))
							att = "CAMMINA OCCASIONALMENTE";
						else if (att.equals("4"))
							att = "CAMMINA FREQUENTEMENTE";
						ht_a.put("#skb_attivita#", att);

						String mob = ""
								+ ut.getObjectField(dbter, "skb_mobilita", 'I');
						if (mob.equals("1"))
							mob = "COMPLETAMENTE IMMOBILE";
						else if (mob.equals("2"))
							mob = "MOLTO LIMITATA";
						else if (mob.equals("3"))
							mob = "PARZIALMENTE LIMITATA";
						else if (mob.equals("4"))
							mob = "LIMITAZIONI ASSENTI";
						ht_a.put("#skb_mobilita#", mob);

						String nut = ""
								+ ut.getObjectField(dbter, "skb_nutrizione",
										'I');
						if (nut.equals("1"))
							nut = "MOLTO POVERA";
						else if (nut.equals("2"))
							nut = "PROBABILMENTE INADEGUATA";
						else if (nut.equals("3"))
							nut = "ADEGUATA";
						else if (nut.equals("4"))
							nut = "ECCELLENTE";
						ht_a.put("#skb_nutrizione#", nut);

						String fraz = ""
								+ ut
										.getObjectField(dbter, "skb_fraz_sciv",
												'I');
						if (fraz.equals("1"))
							fraz = "PROBLEMA";
						else if (fraz.equals("2"))
							fraz = "PROBLEMA POTENZIALE";
						else if (fraz.equals("3"))
							fraz = "SENZA PROBLEMI APPARENTI";
						ht_a.put("#skb_fraz_sciv#", fraz);

						String sval = "";
						int val = Integer.parseInt(""
								+ ut.getObjectField(dbter, "skb_valutazione",
										'I'));
						if (val <= 13)
							sval = "RISCHIO MOLTO ELEVATO";
						else if (val > 13 && val <= 16)
							sval = "RISCHIO ELEVATO";
						else if (val > 16 && val <= 20)
							sval = "RISCHIO MEDIO";
						else if (val > 20)
							sval = "RISCHIO RIDOTTO O ASSENTE";
						ht_a.put("#skb_valutazione#", sval);
						ht_a.put("#braden_punt#", "" + val);
						eve.writeSostituisci("paginaBraden", ht_a);

						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale

		} catch (Exception e) {
			debugMessage("scriviPaginaBraden(" + dbc + " ) : " + e);
		}

	}

	// 24/11/06 m.
	private void scriviPaginaTIQ(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
		int i = 0;
		try {
			String sel = "SELECT * FROM sc_tiq" + " WHERE n_cartella = "
					+ n_cartella;

			if (data_scheda != null)
				sel = sel + " AND data=" + formatDate(dbc, data_scheda);
			else { // scale comprese nel contatto
				sel += " AND data >= " + formatDate(dbc, data_apertura);
				if ((data_chiusura != null)
						&& (!data_chiusura.trim().equals("")))
					sel += " AND data <= " + formatDate(dbc, data_chiusura);

				sel += " ORDER BY data";
			}

			ISASCursor dbcur = dbc.startCursor(sel);

			if (dbcur == null) {
				Hashtable hterr = new Hashtable();
				hterr.put("#n_cartella#", n_cartella);
				hterr.put("#nome_assistito#", intesta_nome);
				hterr.put("#data#", "");
				eve.writeSostituisci("paginaTIQ", hterr);

				// generica voce di "Disturbi"
				Hashtable hTit_D = new Hashtable();
				hTit_D.put("#titolo#",
						"Nel corso della settimana, quali disturbi ha avuto?");
				eve.writeSostituisci("inizioTabTIQ", hTit_D);
				stampaVoceTIQ(eve, null, arrTitDisturbiTIQ,
						arrDbNameDisturbiTIQ);
				eve.write("fineTabTIQ");

				// generica voce "Problemi"
				Hashtable hTit_P = new Hashtable();
				hTit_P.put("#titolo#", "Sempre nel corso della settimana:");
				eve.writeSostituisci("inizioTabTIQ", hTit_P);
				stampaVoceTIQ(eve, null, arrTitProblemiTIQ,
						arrDbNameProblemiTIQ);
				eve.write("finePaginaTIQ");

				// totale
				Hashtable htot = new Hashtable();
				htot.put("#tiq_totale#", "");
				htot.put("#nome_test#", "");
				eve.writeSostituisci("totaleTIQ", htot);

				System.out.println("FoSkInfeEJB.scriviPaginaTIQ(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable hterr1 = new Hashtable();
					hterr1.put("#n_cartella#", n_cartella);
					hterr1.put("#nome_assistito#", intesta_nome);
					hterr1.put("#data#", "");
					eve.writeSostituisci("paginaTIQ", hterr1);

					// generica voce di "Disturbi"
					Hashtable hTit_D = new Hashtable();
					hTit_D
							.put("#titolo#",
									"Nel corso della settimana, quali disturbi ha avuto?");
					eve.writeSostituisci("inizioTabTIQ", hTit_D);
					stampaVoceTIQ(eve, null, arrTitDisturbiTIQ,
							arrDbNameDisturbiTIQ);
					eve.write("fineTabTIQ");

					// generica voce "Problemi"
					Hashtable hTit_P = new Hashtable();
					hTit_P.put("#titolo#", "Sempre nel corso della settimana:");
					eve.writeSostituisci("inizioTabTIQ", hTit_P);
					stampaVoceTIQ(eve, null, arrTitProblemiTIQ,
							arrDbNameProblemiTIQ);
					eve.write("finePaginaTIQ");

					// totale
					Hashtable htot = new Hashtable();
					htot.put("#tiq_totale#", "");
					htot.put("#nome_test#", "");
					eve.writeSostituisci("totaleTIQ", htot);
				} else {
					while (dbcur.next()) {
						if (i > 0)
							eve.write("taglia");

						ISASRecord dbter = dbcur.getRecord();

						Hashtable hterr1 = new Hashtable();
						hterr1.put("#n_cartella#", n_cartella);
						hterr1.put("#nome_assistito#", intesta_nome);
						hterr1.put("#data#", getDateField(dbter, "data"));
						eve.writeSostituisci("paginaTIQ", hterr1);

						// generica voce di "Disturbi"
						Hashtable hTit_D = new Hashtable();
						hTit_D
								.put("#titolo#",
										"Nel corso della settimana, quali disturbi ha avuto?");
						eve.writeSostituisci("inizioTabTIQ", hTit_D);
						stampaVoceTIQ(eve, dbter, arrTitDisturbiTIQ,
								arrDbNameDisturbiTIQ);
						eve.write("fineTabTIQ");

						// generica voce "Problemi"
						Hashtable hTit_P = new Hashtable();
						hTit_P.put("#titolo#",
								"Sempre nel corso della settimana:");
						eve.writeSostituisci("inizioTabTIQ", hTit_P);
						stampaVoceTIQ(eve, dbter, arrTitProblemiTIQ,
								arrDbNameProblemiTIQ);
						eve.write("finePaginaTIQ");

						// totale
						Hashtable htot = new Hashtable();
						String sval = ""
								+ ut.getObjectField(dbter, "tiq_totale", 'I');
						htot.put("#tiq_totale#", sval);
						htot.put("#nome_test#", getStringField(dbter, "nome"));
						eve.writeSostituisci("totaleTIQ", htot);

						i++;
					}// chiudo while
				}// chiudo else interno
			}// chiudo else principale
		} catch (Exception e) {
			debugMessage("scriviPaginaTIQ: " + e);
		}
	}

	// 24/11/06 m.
	private void stampaVoceTIQ(mergeDocument doc, ISASRecord dbr,
			String[] arrVoci, String[] arrChiavi) throws Exception {
		it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
		Hashtable h_xstampa = new Hashtable();
		for (int k = 0; k < arrVoci.length; k++) {
			h_xstampa.put("#voceTIQ#", arrVoci[k]);

			String sval = "";
			String score = "";
			if (dbr != null) {
				int val = Integer.parseInt(""
						+ ut.getObjectField(dbr, arrChiavi[k], 'I'));
				score = "( " + val + " )";
				sval = arrValTIQ[val];
			}
			h_xstampa.put("#valTIQ#", sval);
			h_xstampa.put("#scoreTIQ#", score);

			doc.writeSostituisci("rigaTabTIQ", h_xstampa);
		}
	}

	// 07/12/06
	private void scriviPaginaDiagnosi(ISASConnection dbc, mergeDocument eve)
			throws SQLException {
		Hashtable ht = new Hashtable();
		try {
			String select = "SELECT * FROM diagnosi" + " WHERE n_cartella = "
					+ n_cartella;

			if ((data_chiusura != null) && (!data_chiusura.trim().equals("")))
				select += " AND data_diag <= " + formatDate(dbc, data_chiusura);

			select += " ORDER BY data_diag DESC";
			ISASCursor dbcur = dbc.startCursor(select);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				// inizio
				ht.put("#n_cartella#", n_cartella);
				ht.put("#n_contatto#", n_contatto);
				ht.put("#nome_assistito#", intesta_nome);
				ht.put("#data_apertura_contatto#", dataIta(data_apertura));
				eve.writeSostituisci("paginaPatologia", ht);

				while (dbcur.next()) {
					ISASRecord dbr = dbcur.getRecord();
					Hashtable h_xstampa = new Hashtable();
					h_xstampa
							.put("#data_diag#", getDateField(dbr, "data_diag"));
					for (int j = 1; j < 6; j++) {
						h_xstampa.put("#diag" + j + "#", getStringField(dbr,
								("diag" + j)));
						h_xstampa.put("#desc_diag" + j + "#", getDiagnosi(dbc,
								getStringField(dbr, ("diag" + j))));
						h_xstampa.put("#diag" + j + "_ids#", getStringField(
								dbr, ("diag" + j + "_ids")));
					}
					eve.writeSostituisci("rigaPatologia", h_xstampa);
				}
				eve.write("fineTabPatologia");
			}
		} catch (Exception e) {
			debugMessage("scriviPaginaDiagnosi(" + dbc + " ) : " + e);
		}
	}

	private void scriviPagine(ISASConnection dbc, char carattere,
			mergeDocument eve, Hashtable par) throws SQLException {

		ISASRecord dbrAnagra = null;
		ISASRecord dbrEse = null;
		String sel = "";
		try {
			switch (carattere) {
			case 'A':
				// scrivo PAGINA INIZIALE
				scriviPaginaIniziale(dbc, eve);
				break;
			case 'B':
				// scrivo PAGINA SCHEDAINFE SCHEDA INFERMIERI
				scriviPaginaSchedaInfe(dbc, eve);
				break;
			case 'C':
				// scrivo PAGINA AMBULA SCHEDA INFERMIERI - AMBULATORIO
				scriviPaginaAmbula(dbc, eve);
				break;
			case 'D':
				// scrivo PAGINA PROGASSI SCHEDA INFERMIERI - PROGETTO
				// ASSISTENZIALE
				scriviPaginaProgAssi(dbc, eve);
				break;
			case 'E':
				// scrivo PAGINA EVENTI SCHEDA INFERMIERI - EVENTI
				scriviPaginaEventi(dbc, eve);
				break;
			case 'F':
				// scrivo PAGINA PRESTRICH SCHEDE ACCERTAMENTI - PRESTAZIONI
				// RICHIESTE
				scriviPaginaPrestRich(dbc, eve);
				break;
			case 'G':
				// scrivo PAGINA ACCOFAM SCHEDE ACCERTAMENTI - ACCOGLIENZA
				// FAMILIARI
				scriviPaginaAccoFam(dbc, eve);
				break;
			case 'H':
				// scrivo PAGINA SITFAM SCHEDE ACCERTAMENTI - SITUAZIONE
				// FAMILIARE
				scriviPaginaSitFam(dbc, eve);
				break;
			case 'Z':
				// scrivo PAGINA NUCLEOFAM SCHEDE ACCERTAMENTI - NUCLEO
				// FAMILIARE
				scriviPaginaNucleoFam(dbc, eve);
				break;
			case 'I':
				// scrivo PAGINA TERINATTO SCHEDE ACCERTAMENTI - TERAPIA IN ATTO
				scriviPaginaTerInAtto(dbc, eve);
				break;
			case 'J':
				// scrivo PAGINA INDNORTON SCHEDE INTEGRITA CUTE E MUCOSE -
				// INDICE NORTON
				scriviPaginaIndNorton(dbc, eve, par);
				break;
			case 'K':
				// scrivo PAGINA VALDOL SCHEDE ACCERTAMENTI - VALUTAZIONE DOLORE
				scriviPaginaValDol(dbc, eve);
				break;
			case 'L':
				// scrivo PAGINA AUSILI SCHEDE ACCERTAMENTI - AUSILI
				scriviPaginaAusili(dbc, eve);
				break;
			case 'M':
				// scrivo PAGINA RELAZIONE SCHEDE ACCERTAMENTI - RELAZIONE
				scriviPaginaRelazione(dbc, eve);
				break;
			case 'N':
				// scrivo PAGINA STATOCOSC SCHEDE STATO SALUTE - STATO COSCIENZA
				scriviPaginaStatoCosc(dbc, eve);
				break;
			case 'O':
				// scrivo PAGINA UMORE SCHEDE STATO SALUTE - TONO UMORE
				scriviPaginaUmore(dbc, eve);
				break;
			case 'P':
				// scrivo PAGINA RESPIRAZIONE SCHEDE STATO SALUTE - RESPIRAZIONE
				scriviPaginaRespiraz(dbc, eve);
				break;
			case 'Q':
				// scrivo PAGINA STATONUTRIZ SCHEDE STATO SALUTE - STATO
				// NUTRIZIONALE
				scriviPaginaStatoNutriz(dbc, eve);
				break;
			case 'R':
				// scrivo PAGINA ESCRET SCHEDE STATO SALUTE - FUNZIONI
				// ESCRETORIE
				scriviPaginaEscret(dbc, eve);
				break;
			case 'S':
				// scrivo PAGINA MOTRIC SCHEDE STATO SALUTE - MOTRICITA E
				// DEAMBULAZIONE
				scriviPaginaMotric(dbc, eve);
				break;
			case 'T':
				// scrivo PAGINA ALTRIPAR SCHEDE STATO SALUTE - ALTRI PARAMETRI
				scriviPaginaAltriPar(dbc, eve);
				break;
			case 'U':
				// scrivo PAGINA LESIONIDECUB SCHEDE INTEGRITA CUTE E MUCOSE -
				// LESIONI DECUBITO
				scriviPaginaLesioniDecub(dbc, eve);
				break;
			case 'V':
				// scrivo PAGINA ULCEREPIEDE SCHEDE INTEGRITA CUTE E MUCOSE -
				// ULCERE PIEDE
				scriviPaginaUlcerePie(dbc, eve);
				break;
			case 'W':
				// scrivo PAGINA LESVASCO SCHEDE INTEGRITA CUTE E MUCOSE -
				// LESIONI VASCOLARI
				scriviPaginaLesVasco(dbc, eve);
				break;
			case 'X':
				// scrivo PAGINA ALTERAZIONI SCHEDE INTEGRITA CUTE E MUCOSE -
				// ALTRE ALTERAZIONI
				scriviPaginaAlterazioni(dbc, eve);
				break;
			case 'Y':
				// scrivo PAGINA INDDIAGNO SCHEDE INTEGRITA CUTE E MUCOSE -
				// INDAGINI DIAGNOSTICHE
				scriviPaginaIndDiagno(dbc, eve);
				break;
			case 'a':
				// scrivo PAGINA TABADL SCHEDE ACCERTAMENTI - TABADL
				scriviPaginaTabAdl(dbc, eve);
				break;
			case 'b':
				// scrivo PAGINA TABIADL SCHEDE ACCERTAMENTI - TABIADL
				scriviPaginaTabIAdl(dbc, eve);
				break;
			case 'c':
				// scrivo PAGINA PFEIFFER SCHEDE ACCERTAMENTI - PFEIFFER
				scriviPaginaPfeiffer(dbc, eve);
				break;
			case 'd':
				// scrivo PAGINA CAREGIVER SCHEDE ACCERTAMENTI - CAREGIVER
				scriviPaginaCaregiver(dbc, eve);
				break;
			case 'e':
				// scrivo PAGINA INDICE BRADEN SCHEDE ACCERTAMENTI - BRADEN
				scriviPaginaBraden(dbc, eve);
				break;
			case 'f':
				// scrivo PAGINA TIQ SCHEDE ACCERTAMENTI - TIQ
				scriviPaginaTIQ(dbc, eve);
				break;
			case 'g': // 07/12/06
				// scrivo PAGINA DIAGNOSI
				scriviPaginaDiagnosi(dbc, eve);
				break;
			case 'h':
				// scrivo PAGINA INDICE DI BARTHEL SCHEDE ACCERTAMENTI - BARTHEL
				scriviPaginaBarthel(dbc, eve);
			case 'i': // gb 22.12.08
				// scrivo PAGINA SCHEDE VAL KPS
				scriviPaginaKPS(dbc, eve);
				break;
			case 'j': // elisa b 13/07/10
				// scrivo SCHEDA INTEGRITA CUTE E MUCOSE - VCOLESIONI
				String tipoLesione = par.get("vco_sk_tipo").toString();
				scriviPaginaVcoLesioni(dbc, eve, tipoLesione);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"DEBUG SKINFE :Errore eseguendo una scriviPagine()  ");
		}

	}

	public byte[] query_skinfe(String utente, String passwd, Hashtable par,
			mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_skinfe ";
		LOG.info( punto + " inizio con dati>"+par+ "<");
		System.out.println("SINSFoSkinfe.query_skinfe(): DEBUG inizio...");
		boolean done = false;
		ISASConnection dbc = null;

		String pagine = (String) par.get("pagine");
		char[] numpagine = pagine.toCharArray();

		n_cartella = (String) par.get("n_cart");
		n_contatto = (String) par.get("n_conta");
		data_apertura = (String) par.get("data_apertura");
		data_scheda = (String) par.get("data_scheda");
		// 16/11/06 m.
		data_chiusura = (String) par.get("data_chiusura");

		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			leggiAnagrafica(dbc);
			System.out.println("PAGINE" + numpagine.length);
			for (int i = 0; i < numpagine.length; i++) {
				if (i == 0)
					mkLayout(dbc, eve);

				scriviPagine(dbc, numpagine[i], eve, par);
				System.out.println("PAGINE" + numpagine[i]);
				if (i < numpagine.length - 1)
					eve.write("taglia");
			}
			eve.write("finale");
			eve.close();

			/*
			 * System.out.println("SINSFoSkinfe.query_skinfe(): DEBUG "+
			 * "documento restituito ["+(new String(eve.get()))+"]");
			 */
			dbc.close();
			super.close(dbc);
			done = true;
/*
			// Ivan 22/10/2010 x prove applet rich text
			java.io.File fx = new java.io.File("/home/caribel/applicazioni/dumpIvan.tmp");
			if (fx.exists()){
				// Fai il dump su disco
				String filename = "/home/caribel/applicazioni/dumpMergeDocument.ser";
				java.io.FileOutputStream fos = null;
				java.io.ObjectOutputStream out = null;
				try{
					fos = new java.io.FileOutputStream(filename);
					out = new java.io.ObjectOutputStream(fos);
					out.writeObject(eve);
					out.close();
				}catch(java.io.IOException ex){
					ex.printStackTrace();
				}
			}
			// Fine Ivan
*/
			return eve.get(); // restituisco il bytearray
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SINSFoSkinfe.query_skinfe(): " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("SINSFoSkinfe.query_skinfe(): " + e1);
				}
			}
		}
	} // End of query_skinfe() method

	/**
	 * restituisce la decodifica del motivo dimissione(SCHEDA INFERMIERI)
	 */
	/*
	 * private String getMotivoDimissione(String motivo) { String mot = ""; try
	 * { if (motivo.equals("0")) mot = ""; else if (motivo.equals("1")) mot =
	 * "Trasferimento"; else if (motivo.equals("2")) mot = "Decesso"; else if
	 * (motivo.equals("3")) mot = "Fine prestazione"; else if
	 * (motivo.equals("4")) mot = "Guarigione"; else if (motivo.equals("5")) mot
	 * = "Ricovero ospedaliero"; else if (motivo.equals("6")) mot =
	 * "Ricovero in RSA"; else if (motivo.equals("7")) mot =
	 * "Ricovero in strutture residenziali"; else if (motivo.equals("8")) mot =
	 * "Altri profili di assistenza domiciliare"; else if (motivo.equals("9"))
	 * mot = "Altro"; else mot = ""; } catch(Exception e) { mot =
	 * "CODICE MOTIVO ERRATO"; } return mot; }
	 */
	private String getMotivoDimissione(ISASRecord dbr, ISASConnection dbc)
			throws SQLException {
		String decod = "";
		try {
			if (dbr.get("ski_dimissioni") != null
					&& !(("" + dbr.get("ski_dimissioni")).trim()).equals("")
					&& !(("" + dbr.get("ski_dimissioni")).trim()).equals(".")) {
				String codice = "" + dbr.get("ski_dimissioni");
				String sel = "SELECT tab_descrizione FROM tab_voci "
						+ " WHERE tab_cod='ICHIUS' AND tab_val='" + codice
						+ "'";
				System.out.println("MOTIVO CHIUSURA-->" + sel);
				ISASRecord dbDecod = dbc.readRecord(sel);
				if (dbDecod != null && dbDecod.get("tab_descrizione") != null) {
					decod = (String) dbDecod.get("tab_descrizione");
					if (decod.trim().equals("."))
						decod = "";
				} else
					decod = "CODICE MOTIVO ERRATO";
			}
			return decod;
		} catch (Exception e) {
			debugMessage("FoSkInfeEJB.getMotivoDimissione(): " + e);
			throw new SQLException("Errore eseguendo getMotivoDimissione()");
		}
	}

	/**
	 * restituisce la decodifica della frequenza(SCHEDA INFERMIERI -
	 * AMBULATORIO)
	 */
	private String getFrequenza(String freque, ISASConnection dbc) {
		String freq = "";
		try {
			String sel = "SELECT tab_descrizione FROM tab_voci WHERE "
					+ " tab_cod='FREQAC' AND tab_val='" + freque + "'";
			ISASRecord dbr = dbc.readRecord(sel);
			if (dbr != null)
				freq = (String) dbr.get("tab_descrizione");
			else
				freq = "";
		} catch (Exception e) {
			freq = "CODICE FREQUENZA ERRATO";
		}
		return freq;
	}

	/**
	 * restituisce la decodifica del tipo ausilio(SCHEDA ACCERTAMENTO - AUSILI)
	 */
	private String getTipoAusilio(String tipo) {
		String ausilio = "";
		try {
			if (tipo.equals("0"))
				ausilio = "";
			else if (tipo.equals("1"))
				ausilio = "Ausili a fluttuazione dinamica";
			else if (tipo.equals("2"))
				ausilio = "Letti a cessione d'aria";
			else if (tipo.equals("3"))
				ausilio = "Letti Fluidizzati";
			else if (tipo.equals("4"))
				ausilio = "Materassi a cessione d'aria";
			else if (tipo.equals("5"))
				ausilio = "Materassi fibra";
			else if (tipo.equals("6"))
				ausilio = "Materassi in schiuma";
			else if (tipo.equals("7"))
				ausilio = "Sovramaterassi a cessione d'aria";
			else if (tipo.equals("8"))
				ausilio = "Sovramaterassi ad aria statica";
			else if (tipo.equals("9"))
				ausilio = "Sovramaterassi a pressione dinamica";
			else if (tipo.equals("10"))
				ausilio = "Sovramaterassi in fibra";
			else if (tipo.equals("11"))
				ausilio = "Gomitiere";
			else if (tipo.equals("12"))
				ausilio = "Ginocchiere";
			else if (tipo.equals("13"))
				ausilio = "Archetto alza coperte";
			else if (tipo.equals("15"))
				ausilio = "Letto ortopedico ad 1 manovella";
			else if (tipo.equals("16"))
				ausilio = "Letto ortopedico a 2 manovelle";
			else if (tipo.equals("17"))
				ausilio = "Sponde di contenimento";
			else if (tipo.equals("18"))
				ausilio = "Carrozzella pieghevole";
			else if (tipo.equals("19"))
				ausilio = "Poltrona comoda con ruote";
			else if (tipo.equals("20"))
				ausilio = "Cuscino antidecubito";
			else if (tipo.equals("14"))
				ausilio = "Altri";
			else
				ausilio = "";
		} catch (Exception e) {
			ausilio = "CODICE AUSILIO ERRATO";
		}
		return ausilio;
	}

	/**
	 * restituisce la decodifica della condizione generale(SCHEDA STATO SALUTE -
	 * STATO COSCIENZA)
	 */
	private String getCondGenerali(String cond) {
		String condizione = "";
		try {
			if (cond.equals("1"))
				condizione = "orientato sempre";
			else if (cond.equals("2"))
				condizione = "orientato solo in certi momenti";
			else if (cond.equals("3"))
				condizione = "disorientato sempre";
			else if (cond.equals("4"))
				condizione = "disorientato solo in certi momenti";
			else if (cond.equals("5"))
				condizione = "disorientato solo di notte";
			else if (cond.equals("6"))
				condizione = "stato soporoso";
			else if (cond.equals("7"))
				condizione = "coma vegetativo";
			else
				condizione = "";
		} catch (Exception e) {
			condizione = "CODICE CONDIZIONE ERRATO";
		}
		return condizione;
	}

	/**
	 * restituisce la decodifica del tono umore(SCHEDA STATO SALUTE - TONO
	 * UMORE)
	 */
	private String getTonoUmore(String tono) {
		String umore = "";
		try {
			if (tono.equals("1"))
				umore = "preoccupato";
			else if (tono.equals("2"))
				umore = "ansioso";
			else if (tono.equals("3"))
				umore = "depresso";
			else if (tono.equals("4"))
				umore = "aggressivo";
			else if (tono.equals("5"))
				umore = "apatico";
			else if (tono.equals("6"))
				umore = "euforico";
			else if (tono.equals("7"))
				umore = "impossibile da valutare";
			else
				umore = "";
		} catch (Exception e) {
			umore = "CODICE TONO UMORE ERRATO";
		}
		return umore;
	}

	/**
	 * restituisce la decodifica delle specifiche aspirazioni(SCHEDA STATO
	 * SALUTE - RESPIRAZIONE)
	 */
	private String getSpecAspiraz(String asp) {
		String aspirazioni = "";
		try {
			if (asp.equals("1"))
				aspirazioni = "la famiglia addestrata e' in grado di occuparsene";
			else if (asp.equals("2"))
				aspirazioni = "la famiglia non e' in grado di occuparsene";
			else if (asp.equals("3"))
				aspirazioni = "la famiglia inizia l'addestramento";
			else if (asp.equals("4"))
				aspirazioni = "la famiglia non collabora";
			else
				aspirazioni = "";
		} catch (Exception e) {
			aspirazioni = "CODICE ASPIRAZIONE ERRATO";
		}
		return aspirazioni;
	}

	/**
	 * restituisce la decodifica dell'alimentazione artificiale(SCHEDA STATO
	 * SALUTE - STATO NUTRIZIONALE)
	 */
	private String getAlimentArti(String stato) {
		String nutriz = "";
		try {
			if (stato.equals("1"))
				nutriz = "non presente";
			else if (stato.equals("2"))
				nutriz = "parentale";
			else if (stato.equals("3"))
				nutriz = "enterale - S.N.G.";
			else if (stato.equals("4"))
				nutriz = "enterala - PEG";
			else if (stato.equals("5"))
				nutriz = "altro";
			else
				nutriz = "";
		} catch (Exception e) {
			nutriz = "CODICE ALIMENTAZIONE ERRATO";
		}
		return nutriz;
	}

	/**
	 * restituisce la decodifica dell'indagine della diagnosi(SCHEDA STATO
	 * SALUTE - FUNZIONI ESCRETORIE)
	 */
	private String getIndagine(String ind) {
		String indagine = "";
		try {
			if (ind.equals("1"))
				indagine = "esame urine";
			else if (ind.equals("2"))
				indagine = "es.colturale";
			else if (ind.equals("3"))
				indagine = "es.colturale+atb";
			else if (ind.equals("4"))
				indagine = "es.colturale per indagine prevalenza";
			else if (ind.equals("5"))
				indagine = "es.colturale per indagine incidenza";
			else
				indagine = "";
		} catch (Exception e) {
			indagine = "CODICE INDAGINE ERRATO";
		}
		return indagine;
	}

	/**
	 * restituisce la decodifica della presenza stomie(SCHEDA STATO SALUTE -
	 * FUNZIONI ESCRETORIE)
	 */
	private String getStomie(String sto) {
		String stomie = "";
		try {
			if (sto.equals("1"))
				stomie = "no";
			else if (sto.equals("2"))
				stomie = "si, uretrale";
			else if (sto.equals("3"))
				stomie = "si, gastro-intestinale";
			else if (sto.equals("4"))
				stomie = "si, entrambe";
			else
				stomie = "";
		} catch (Exception e) {
			stomie = "CODICE STOMIE ERRATO";
		}
		return stomie;
	}

	/**
	 * restituisce la decodifica del cambio postura(SCHEDA STATO SALUTE -
	 * MOTRICI E DEAMBULAZIONE)
	 */
	private String getCambiPostu(String postu) {
		String cambi = "";
		try {
			if (postu.equals("1"))
				cambi = "non necessari";
			else if (postu.equals("2"))
				cambi = "necessari, gestiti dalla famiglia";
			else if (postu.equals("3"))
				cambi = "necessari, la famiglia inizia l'addestramento";
			else if (postu.equals("4"))
				cambi = "necessari, la famiglia non collabora";
			else if (postu.equals("5"))
				cambi = "necessari, la famiglia non e' in grado";
			else
				cambi = "";
		} catch (Exception e) {
			cambi = "CODICE CAMBIO POSTURA ERRATO";
		}
		return cambi;
	}

	/**
	 * restituisce la decodifica del tipo di sede della lesione(SCHEDA INTEGRITA
	 * CUTE E MUCOSE - LESIONI DECUBITO)
	 */
	private String getSedeLesDecub(String les) {
		String sede = "";
		try {
			if (les.equals("1"))
				sede = "occipite";
			else if (les.equals("2"))
				sede = "padiglione auricolare dx";
			else if (les.equals("3"))
				sede = "padiglione auricolare sx";
			else if (les.equals("4"))
				sede = "rachide";
			else if (les.equals("5"))
				sede = "sacro-coccigeo";
			else if (les.equals("6"))
				sede = "trocantere dx";
			else if (les.equals("7"))
				sede = "trocantere sx";
			else if (les.equals("8"))
				sede = "tallone dx";
			else if (les.equals("9"))
				sede = "tallone sx";
			else if (les.equals("10"))
				sede = "malleolo int. dx";
			else if (les.equals("11"))
				sede = "malleolo int. sx";
			else if (les.equals("12"))
				sede = "malleolo est. dx";
			else if (les.equals("13"))
				sede = "malleolo est. sx";
			else if (les.equals("14"))
				sede = "altra sede";
			else
				sede = "";
		} catch (Exception e) {
			sede = "CODICE SEDE LESIONE ERRATO";
		}
		return sede;
	}

	/**
	 * restituisce la decodifica dello stadio della lesione(SCHEDA INTEGRITA
	 * CUTE E MUCOSE - LESIONE DECUBITO--ULCERE PIEDE)
	 */
	private String getStadioLesione(String les) {
		String stadio = "";
		try {
			if (les.equals("1"))
				stadio = "stadio I";
			else if (les.equals("2"))
				stadio = "stadio I-N";
			else if (les.equals("3"))
				stadio = "stadio II";
			else if (les.equals("4"))
				stadio = "stadio II-N";
			else if (les.equals("5"))
				stadio = "stadio III";
			else if (les.equals("6"))
				stadio = "stadio III-N";
			else if (les.equals("7"))
				stadio = "stadio IV";
			else if (les.equals("8"))
				stadio = "stadio IV-N";
			// G.Brogi 09/01/06 aggiunto stadio 0=guarigione
			else if (les.equals("0"))
				stadio = "guarigione";
			else
				stadio = "";
		} catch (Exception e) {
			stadio = "CODICE STADIO LESIONE ERRATO";
		}
		return stadio;
	}

	/**
	 * restituisce la decodifica del tipo di sede della lesione delle
	 * ulcere(SCHEDA INTEGRITA CUTE E MUCOSE - ULCERE PIEDE)
	 */
	private String getSedeLesUlcerePie(String les) {
		String sede = "";
		try {
			if (les.equals("1"))
				sede = "Tallone dx";
			else if (les.equals("2"))
				sede = "Tallone sn";
			else if (les.equals("3"))
				sede = "Malleolo int. dx.";
			else if (les.equals("4"))
				sede = "Malleolo int. sn.";
			else if (les.equals("5"))
				sede = "Malleolo est. dx.";
			else if (les.equals("6"))
				sede = "Malleolo est. sn.";
			else
				sede = "";
		} catch (Exception e) {
			sede = "CODICE SEDE LESIONE ULCERE ERRATO";
		}
		return sede;
	}

	/**
	 * restituisce la decodifica PRESENZA DOLORE(SCHEDA INTEGRITA CUTE E MUCOSE)
	 */
	private String getPresenzaDolore(String les) {
		String dolore = "";
		try {
			if (les.equals("1"))
				dolore = "no";
			else if (les.equals("2"))
				dolore = "si, da valutare";
			else if (les.equals("3"))
				dolore = "si, valutato";
			else
				dolore = "";
		} catch (Exception e) {
			dolore = "CODICE SEDE LESIONE ULCERE ERRATO";
		}
		return dolore;
	}

	/**
	 * restituisce la decodifica del tipo di sede della lesione vascolare(SCHEDA
	 * INTEGRITA CUTE E MUCOSE - LESIONI VASCOLARI)
	 */
	private String getSedeLesVasco(String les) {
		String sede = "";
		try {
			if (les.equals("1"))
				sede = "Falangi piede dx.";
			else if (les.equals("2"))
				sede = "Falangi piede sn.";
			else if (les.equals("3"))
				sede = "Piede dx.";
			else if (les.equals("4"))
				sede = "Piede sn.";
			else if (les.equals("5"))
				sede = "Metatarso";
			else if (les.equals("6"))
				sede = "Tallone dx.";
			else if (les.equals("7"))
				sede = "Tallone sn";
			else if (les.equals("8"))
				sede = "Malleolo int. dx.";
			else if (les.equals("9"))
				sede = "Malleolo int. sn.";
			else if (les.equals("10"))
				sede = "Malleolo est. dx.";
			else if (les.equals("11"))
				sede = "Malleolo est. sn.";
			else if (les.equals("12"))
				sede = "A manicotto";
			else if (les.equals("13"))
				sede = "Terzo medio";
			else
				sede = "";
		} catch (Exception e) {
			sede = "CODICE SEDE LESIONE VASCOLARE ERRATO";
		}
		return sede;
	}

	/**
	 * restituisce la decodifica del tipo di tessuto(SCHEDA INTEGRITA CUTE E
	 * MUCOSE - LESIONI VASCOLARI)
	 */
	private String getTessutoLesione(String les) {
		String tessuto = "";
		try {
			if (les.equals("1"))
				tessuto = "Atrofia bianca";
			else if (les.equals("2"))
				tessuto = "Dermoipodermite";
			else if (les.equals("3"))
				tessuto = "Distrofia e/o ipercomia";
			else if (les.equals("4"))
				tessuto = "Desquamazione";
			else if (les.equals("5"))
				tessuto = "Vescicole";
			else if (les.equals("6"))
				tessuto = "Non valutabile";
			else
				tessuto = "";
		} catch (Exception e) {
			tessuto = "CODICE SEDE LESIONE ULCERE ERRATO";
		}
		return tessuto;
	}

	/**
	 * restituisce la decodifica delle condizioni generali(SCHEDA ACCERTAMENTO -
	 * INDICE NORTON)
	 */
	private String getCondGen(String cond) {
		String condgen = "";
		try {
			if (cond.equals("1"))
				condgen = "Pessime";
			else if (cond.equals("2"))
				condgen = "Scadenti";
			else if (cond.equals("3"))
				condgen = "Discrete";
			else if (cond.equals("4"))
				condgen = "Buone";
			else
				condgen = "";
		} catch (Exception e) {
			condgen = "CODICE CONDIZIONI GENERALI ERRATO";
		}
		return condgen;
	}

	/**
	 * restituisce la decodifica dello stato mentale(SCHEDA ACCERTAMENTO -
	 * INDICE NORTON)
	 */
	private String getStatoMent(String stato) {
		String statoMent = "";
		try {
			if (stato.equals("1"))
				statoMent = "Stuporoso";
			else if (stato.equals("2"))
				statoMent = "Confuso ";
			else if (stato.equals("3"))
				statoMent = "Apatico";
			else if (stato.equals("4"))
				statoMent = "Lucido";
			else
				statoMent = "";
		} catch (Exception e) {
			statoMent = "CODICE STATO MENTALE ERRATO";
		}
		return statoMent;
	}

	/**
	 * restituisce la decodifica della deambulazione(SCHEDA ACCERTAMENTO -
	 * INDICE NORTON)
	 */
	private String getDeambulazione(String deamb) {
		String deambulazione = "";
		try {
			if (deamb.equals("1"))
				deambulazione = "Immobile";
			else if (deamb.equals("2"))
				deambulazione = "Costretto su sedia";
			else if (deamb.equals("3"))
				deambulazione = "Cammina con aiuto";
			else if (deamb.equals("4"))
				deambulazione = "Normale";
			else
				deambulazione = "";
		} catch (Exception e) {
			deambulazione = "CODICE DEAMBULAZIONE ERRATO";
		}
		return deambulazione;
	}

	/**
	 * restituisce la decodifica della mobilita(SCHEDA ACCERTAMENTO - INDICE
	 * NORTON)
	 */
	private String getMobilita(String mob) {
		String mobilita = "";
		try {
			if (mob.equals("1"))
				mobilita = "Immobile";
			else if (mob.equals("2"))
				mobilita = "Molto limitata";
			else if (mob.equals("3"))
				mobilita = "Moderat. limitata";
			else if (mob.equals("4"))
				mobilita = "Piena";
			else
				mobilita = "";
		} catch (Exception e) {
			mobilita = "CODICE MOBILITA ERRATO";
		}
		return mobilita;
	}

	/**
	 * restituisce la decodifica dell'incontinenza(SCHEDA ACCERTAMENTO - INDICE
	 * NORTON)
	 */
	private String getIncontinenza(String inco) {
		String incontinenza = "";
		try {
			if (inco.equals("1"))
				incontinenza = "Doppia";
			else if (inco.equals("2"))
				incontinenza = "Abituale";
			else if (inco.equals("3"))
				incontinenza = "Occasionale";
			else if (inco.equals("4"))
				incontinenza = "Assente";
			else
				incontinenza = "";
		} catch (Exception e) {
			incontinenza = "CODICE INCONTINENZA ERRATO";
		}
		return incontinenza;
	}

	// --------------------------------------------------------
	// 12/05/2006 G.Brogi - faiResidenza
	// funzione utilizzata per la pagina nucleo familiare
	// --------------------------------------------------------
	private String faiResidenza(ISASConnection dbc, ISASRecord dbfam)
			throws Exception {
		String ret = "";
		try {
			it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
			String ind = (String) util.getObjectField(dbfam, "res_indirizzo",
					'S');
			String citt = (String) util.getObjectField(dbfam, "res_citta", 'S');
			citt = util.getDecode(dbc, "comuni", "codice", citt, "descrizione");
			String cap = (String) util.getObjectField(dbfam, "res_cap", 'S');
			String prov = (String) util.getObjectField(dbfam, "res_prov", 'S');

			ret = ind + " - " + cap + " " + citt + "(" + prov + ")";
		} catch (Exception e) {
			System.out.println("Errore eeguendo una faiResidenza(): " + e);
			ret = "";
		}
		return ret;
	}

	private String decodifica(String tabella, String nome_cod,
			Object val_codice, String descrizione, ISASConnection dbc) {
		Hashtable htxt = new Hashtable();
		if (val_codice == null)
			return " ";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM "
					+ tabella + " WHERE " + nome_cod + " ='"
					+ val_codice.toString() + "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			return ((String) dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return " ";
		}
	}

	private void scriviPaginaVcoLesioni(ISASConnection dbc, mergeDocument eve, String tipoLesione)
			throws SQLException {

		int i = 0;
		String[] elementiScheda = new String[] { "bordi",
				"sede", "infezione",
				"prof_lesioni", "intervento",
				"npuap", "essudato_qual", "raggiunto",
				"colore", "dolore_scala", "tipo_lesione",
				"odore", "essudato_qta", "cute",
				"obiettivo", "note", "dim_lunghezza",
				"dim_larghezza", "dim_profondita", "tratti_sede",
				"tratti_lunghezza", "tratti_posizione", "infezione"};
		Hashtable ht = new Hashtable();
		
		DecodificheVcoLesioni decoder = new DecodificheVcoLesioni();
		
		try {
			String select = "SELECT *" +
							" FROM skidecubito" +
							" WHERE n_cartella = " + n_cartella +
							" AND n_contatto = "+ n_contatto +
							" AND sk_tipo= '" + tipoLesione + "'";

			if (data_scheda != null)
				select = select + " AND skdt_data=" + formatDate(dbc, data_scheda);

			ISASCursor dbcur = dbc.startCursor(select);
			
			/* dati da scrivere sempre */
			ht.put("#n_cartella#", n_cartella);
			ht.put("#nome_assistito#", intesta_nome);
			ht.put("#n_contatto#", n_contatto);
			
			/* il tipo scheda determina il titolo */
			if(tipoLesione.equalsIgnoreCase("1"))
				ht.put("#vco_nome#", "LESIONI DA DECUBITO");
			else if(tipoLesione.equalsIgnoreCase("2"))
				ht.put("#vco_nome#", "ULCERE DEL PIEDE");
			else if(tipoLesione.equalsIgnoreCase("3"))
				ht.put("#vco_nome#", "LESIONI VASCOLARI");			
			
			ht = faiVuota(ht, elementiScheda);

			if (dbcur == null) {
				ht = faiVuota(ht, elementiScheda);
				eve.writeSostituisci("paginaVcoLesioni", ht);
				System.out.println("FoSkInfeEJB.scriviPaginaLesioniDecub(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					ht = faiVuota(ht, elementiScheda);
					eve.writeSostituisci("paginaLesioniDecub", ht);
				} else {
					while (dbcur.next()) {
						ISASRecord dbr = dbcur.getRecord();
						
						ht.put("#data_scheda#", getDateField(dbr,
								"skdt_data"));
						ht.put("#tipo_lesione#", decoder.getTipoLesione(dbr));					
						ht.put("#dimensioni#", getStringField(dbr,
								"dim_lunghezza") + " cm, " 
								+ getStringField(dbr, "dim_larghezza")
								+ " cm, "+ getStringField(dbr, "dim_profondita") + " cm");
						ht.put("#npuap#", decoder.getNpuap(dbr));
						ht.put("#prof_lesioni#", decoder.getProfAltreLesioni(dbr));
						ht.put("#bordi#", decoder.getBordi(dbr));
						ht.put("#cute#", decoder.getCutePerilesionale(dbr));
						ht.put("#tratti#", getStringField(dbr,
									"tratti_sede") + ", " 
									+ getStringField(dbr, "tratti_lunghezza")
									+ " cm, "+ getStringField(dbr, "tratti_posizione"));
						ht.put("#colore#", decoder.getColore(dbr));
						ht.put("#essudato_qta#", decoder.getEssudatoQta(dbr));
						ht.put("#essudato_qual#", decoder.getEssudatoQual(dbr));						
						ht.put("#dolore#", getStringField(dbr, "dolore_scala"));
						if(getStringField(dbr, "odore").equalsIgnoreCase("S"))
							ht.put("#odore#", "SI");
						else
							ht.put("#odore#", "NO");
						ht.put("#infezione#", decoder.getInfezione(dbr));
						ht.put("#obiettivo#", getStringField(dbr, "obiettivo"));
						ht.put("#sede#", getStringField(dbr, "sede"));
						if(getStringField(dbr, "raggiunto").equalsIgnoreCase("S"))
							ht.put("#raggiunto#", "SI");
						else
							ht.put("#raggiunto#", "NO");
						ht.put("#intervento#", getStringField(dbr, "intervento"));
						ht.put("#note#", getStringField(dbr, "note"));
					
						if (i > 0)
							eve.write("taglia");
						eve.writeSostituisci("paginaVcoLesioni", ht);
						i++;
					}
				}
			}

		} catch (Exception e) {
			debugMessage("scriviPaginaLesioniDecub(" + dbc + " ) : " + e);
		}
	}
	
	
	// 05/02/13
	private String decodPresidio(ISASConnection dbc, String codPres, String codOper) throws Exception
	{
		String ret = "";
		Hashtable h_conf = eveUtl.leggiConf(dbc, codOper, new String[]{"codice_regione", "codice_usl"});
			
		StringBuffer strBufSel = new StringBuffer("SELECT * FROM presidi WHERE codreg = '");
		strBufSel.append((String)h_conf.get("codice_regione"));
		strBufSel.append("' AND codazsan = '");
		strBufSel.append((String)h_conf.get("codice_usl"));
		strBufSel.append("' AND codpres = '");
		strBufSel.append(codPres);
		strBufSel.append("'");
			
		ISASRecord dbr = dbc.readRecord(strBufSel.toString());
		if ((dbr != null) && (dbr.get("despres") != null))
			ret = ((String)dbr.get("despres")).trim();
		return ret;
	}	
} // End of FoSkInfe class

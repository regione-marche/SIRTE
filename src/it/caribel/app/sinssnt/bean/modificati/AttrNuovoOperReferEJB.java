package it.caribel.app.sinssnt.bean.modificati;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 16/12/2005 - EJB di connessione alla procedura SINS Tabella Contrib
// Ilaria Mancini
//
// ==========================================================================

import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CaricaAgendaPrestazioni;
import it.pisa.caribel.sinssnt.controlli.CollSinsPua;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class AttrNuovoOperReferEJB extends SINSSNTConnectionEJB {

	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	private static final String CTS_TIPO_OPERATORE_SKFPG = "skfpg_tipo_operatore";
	private static final String ver = "2-AttrNuovoOperReferEJB. ";
	CollSinsPua collSins=new CollSinsPua();

	public AttrNuovoOperReferEJB() {
	}

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException, CariException {
		String punto = ver + "query ";
		ISASCursor dbcur = null;
		ISASConnection dbc = null;
		Vector vdbr = new Vector();
		String strCodOperatore = "";
		String strTipoOperatore = "";
		String strTipo = "";
		String dataRiferimento = "";
		String dataAttribuzione = "";
		String strQuery = "";
		LOG.info(punto + " inizio con dati>>" + h);
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				strCodOperatore = (String) h.get("codice_oper_start");
				strTipoOperatore = (String) h.get("tipo_oper_start");
				strTipo = (String) h.get("tipo_attribuzione");
				
			} catch (Exception ex) {
				throw new SQLException("AttrNuovoOperReferEJB.query()-->MANCANO LE CHIAVI PRIMARIE" + ex);
			}
			//A secondo del tipo operatore viene formata la query.

			if (strTipo.equals("R"))
				strQuery = recuperaQueryFromTipoOperatore(strTipoOperatore, strCodOperatore);
			else if (strTipo.equals("A")){
				dataRiferimento = h.get("data_riferimento").toString();
				dataAttribuzione = h.get("data_attribuzione").toString();
				strQuery = getQueryAgenda(dbc, strCodOperatore, dataRiferimento, dataAttribuzione);
			}
			else if (strTipo.equals("0")){
				dataRiferimento = h.get("data_riferimento").toString();
				strQuery = getQueryAgendaPianificazione(dbc, strCodOperatore, dataRiferimento);
			}
			//			
			System.out.println("AttrNuovoOperReferEJB/query: " + strQuery);
			dbcur = dbc.startCursor(strQuery);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				vdbr = dbcur.getAllRecord();
				decodQuery(dbc, vdbr);
			}
			if (dbcur != null)
				dbcur.close();

		} catch (ISASPermissionDeniedException e) {
			System.out.println("AttrNuovoOperReferEJB/query: eccezione permesso negato " + e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una AttrNuovoOperReferEJB.query() " + e);
		} finally {
			logout_nothrow(punto, dbcur, dbc);
		}
		return vdbr;
	}

	private String recuperaQueryFromTipoOperatore(String tipoOp, String codOperatore) throws Exception {
		String punto = ver + "recuperaQueryFromTipoOperatore ";
		String query = "";
		if (ISASUtil.valida(tipoOp)) {
			if (tipoOp.equals(GestTpOp.CTS_COD_INFERMIERE)) {
				query = getQueryInf(codOperatore);
			} else if (tipoOp.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)) {
				query = getQueryFis(codOperatore);
			} else if (tipoOp.equals(GestTpOp.CTS_COD_MEDICO)) {
				query = getQueryMed(codOperatore);
			} else {
				query = getQueryOperatoreGenerico(tipoOp, codOperatore);
			}
		}
		LOG.debug(punto + " query>>" + query);
		return query;
	}
	
	private String getQueryAgenda(ISASConnection dbc, String strCodOperatore,String dataRiferimento,String dataAttribuzione) 
			throws Exception {
		try {
			String strQuery = "SELECT DISTINCT a.ag_cartella AS n_cartella, c.cognome,"
					+ " c.nome, a.ag_contatto AS n_contatto"
					+ " FROM agendant_interv a, cartella c"
					+ " WHERE a.ag_oper_ref = '" + strCodOperatore + "'"
					+ " AND ag_data >=" + formatDate(dbc, dataRiferimento)
					+ " AND ag_data <=" + formatDate(dbc, dataAttribuzione)
					+ " AND a.ag_cartella = c.n_cartella"
					+ " ORDER BY cognome, nome";
			return strQuery;
		} catch (Exception ex) {
			throw new Exception(
					"Errore eseguendo una AttrNuovoOpeRefPianificazioneEJB.getQueryAgenda "
							+ ex);
		}
	}

	private String getQueryAgendaPianificazione(ISASConnection dbc, String strCodOperatore, String dataRiferimento) 
	throws Exception {
		try {
			

			String strQuery = "SELECT DISTINCT p.n_cartella, c.cognome,"
					+ " c.nome, n_progetto AS n_contatto, cod_obbiettivo,"
					+ " n_intervento, pa_data AS data, pi_data_inizio, pi_data_fine, n_progetto" 
					+ " FROM piano_accessi p, cartella c"
					+ " WHERE p.pi_op_esecutore = '" + strCodOperatore + "'"
					+ " AND pi_data_inizio <=" + formatDate(dbc, dataRiferimento)
					+ " AND (pi_data_fine IS NULL" +
						" OR pi_data_fine > " + formatDate(dbc, dataRiferimento) +
						")"
					+ " AND p.n_cartella = c.n_cartella"
					+ " ORDER BY cognome, nome";
			return strQuery;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception(
					"Errore eseguendo una getQueryAgendaPianificazione " + ex);
		}
	}

	private String getQueryOperatoreGenerico(String tipoOp, String codOperatore) {
		String query = " SELECT   p.n_cartella, c.cognome, c.nome, p.n_contatto, p.skfpg_data_apertura data_apertura_contatto "
				+ " FROM skfpg p, cartella c WHERE p.skfpg_referente = '"
				+ codOperatore
				+ "' AND p.skfpg_tipo_operatore = '"
				+ tipoOp
				+ "' and p.skfpg_data_uscita IS NULL AND p.n_cartella = c.n_cartella ORDER BY cognome, nome ";
		return query;
	}

	private String getQueryInf(String strCodOperatore) throws Exception {
		try {
			String strQuery = "SELECT p.n_cartella, c.cognome, c.nome, p.n_contatto, p.ski_data_apertura data_apertura_contatto"
					+ " FROM skinf p, cartella c"
					+ " WHERE p.ski_infermiere = '"
					+ strCodOperatore
					+ "'"
					+ " AND p.ski_data_uscita IS NULL" + " AND p.n_cartella = c.n_cartella" + " ORDER BY cognome, nome";
			return strQuery;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getQueryInf()- " + ex);
		}
	}

	private String getQueryMed(String strCodOperatore) throws Exception {
		try {
			String strQuery = "SELECT p.n_cartella, c.cognome, c.nome, p.n_contatto, p.skm_data_apertura data_apertura_contatto"
					+ " FROM skmedico p, cartella c"
					+ " WHERE p.skm_medico = '"
					+ strCodOperatore
					+ "'"
					+ " AND p.skm_data_chiusura IS NULL"
					+ " AND p.n_cartella = c.n_cartella"
					+ " ORDER BY cognome, nome";
			return strQuery;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getQueryMed()- " + ex);
		}
	}

	private String getQueryFis(String strCodOperatore) throws Exception {
		try {
			String strQuery = "SELECT p.n_cartella, c.cognome, c.nome, p.n_contatto, p.skf_data data_apertura_contatto"
					+ " FROM skfis p, cartella c" + " WHERE p.skf_fisiot = '" + strCodOperatore + "'"
					+ " AND p.skf_data_chiusura IS NULL" + " AND p.n_cartella = c.n_cartella"
					+ " ORDER BY cognome, nome";
			return strQuery;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getQueryFis()- " + ex);
		}
	}

	private String getQueryOnc(String strCodOperatore) throws Exception {
		try {
			String strQuery = "SELECT p.n_cartella, c.cognome, c.nome, p.n_contatto, p.skm_data_apertura data_apertura_contatto"
					+ " FROM skmedpal p, cartella c"
					+ " WHERE p.skm_medico = '"
					+ strCodOperatore
					+ "'"
					+ " AND p.skm_data_chiusura IS NULL"
					+ " AND p.n_cartella = c.n_cartella"
					+ " ORDER BY cognome, nome";
			return strQuery;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getQueryOnc()- " + ex);
		}
	}

	//Decodifica la query aggiungendo il DBName 'assistito' (cognome e nome).
	private void decodQuery(ISASConnection dbc, Vector vdbr) throws Exception

	{
		try {
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				//String strCognNome = getCognNomeAssistito(dbc, dbr);
				String strCognome = (String) dbr.get("cognome");
				String strNome = (String) dbr.get("nome");
				dbr.put("assistito", strCognome + " " + strNome);
			}
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.decodQuery()- " + ex);
		}

	}

	//Ritorna la concatenazione del cognome e nome dell'assistito.
	private String getCognNomeAssistito(ISASConnection dbc, ISASRecord dbr) throws Exception {
		try {
			String strCognome = "";
			String strNome = "";

			String strNCartella = "" + dbr.get("n_cartella");
			String strQueryCart = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + strNCartella;
			System.out.println("AttrNuovoOperReferEJB/getCognNomeAssistito/strQueryCart: " + strQueryCart);
			ISASRecord dbrCart = dbc.readRecord(strQueryCart);
			if (dbrCart != null) {
				strCognome = (String) dbrCart.get("cognome");
				strNome = (String) dbrCart.get("nome");
			}
			return strCognome + " " + strNome;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getCognNomeAssistito()- " + ex);
		}
	}

	/**
	* 	Riceve un Vector di Hashtable contenenti cart e cont degli assistiti selezionati
	*	x il cambio operatore referente.
	*	pi� il codice e il tipo dell'operatore da attribuire e la data di attribuzione.
	*	A secondo del tipo dell'operatore, per ogni coppia cartella,contatto,
	*	nella tabella individuata dal tipo operatore:
	*	- legge il record individuato da cartella e contatto,
	*	- modifica l'operatore referente,
	*	- riscrive il contatto.
	*	- scrive un nuovo record nella tabella degli operatori referenti secondo il tipo operatore.
	*/
	public ISASRecord cambiaOperatore(myLogin mylogin, Hashtable par, Vector vettSel) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		String punto = ver + "";
		ISASConnection dbc = null;
		ISASRecord dbr = null;// mi serve solo x restituire qualcosa al client
		ISASCursor dbcur = null;
		try {
			dbc = super.logIn(mylogin);
			String strTipoOperAttrib = (String) par.get("tipo_oper_end");
			String strDataAttrib = (String) par.get("data_attribuzione");
			String strCodOperAttrib = (String) par.get("codice_oper_end");
			String strCodOperStart = (String) par.get("codice_oper_start");
			//Ottengo i nomi tabella e nomi campi dipendenti dal tipo operatori
			Hashtable htParsFromTipoOper = getParsFromTipoOper(strTipoOperAttrib);
			if (htParsFromTipoOper == null) {
				return dbr;
			}
			dbc.startTransaction();

			//gb 06.02.09 Se si tratta di un operatore di tipo 01 (sociale)
			String strCodPresidioOper = "";
			/* NON MODIFICO PER NESSUNO PRESIDIO */
			//			if (strTipoOperAttrib.equals("01")) {
			//				strCodPresidioOper = getCodPresidioDaOper(dbc, strCodOperAttrib);
			//			}

			if (vettSel != null) {
				for (int j = 0; j < vettSel.size(); j++) {
					Hashtable hDati = (Hashtable) vettSel.elementAt(j);
					//Leggo il j-esimo record dalla tabella contatti
					//(che cambia a secondo del tipo operatore: ass. sociale, infermieri, ...)
					dbr = getDbr(dbc, hDati, htParsFromTipoOper);
					if (dbr == null) {
						//Non si dovrebbe mai verificare, ma non si sa mai.
						String messaggioCE = "Record N.Cartella=" + hDati.get("n_cartella") + " / N.Contatto: "
								+ hDati.get("n_contatto")
								+ "\nNon reperibile: Attenzione, Intera Operazione Annullata!";
						throw new CariException(messaggioCE, -2);
					}
					//Riscrivo il record nella tabella dei contatti
					//col nuovo codice operatore referente.
					reWriteDbr(dbc, dbr, strCodOperAttrib, strDataAttrib, htParsFromTipoOper, strTipoOperAttrib,
							strCodPresidioOper);
					//Aggiungo un nuovo record nella tabella degli operaori referenti
					//Il nome tabella dipende dal tipo operatore (sociale, infermieri, ...)
					addDbrInTabReferente(dbc, strCodOperAttrib, strDataAttrib, hDati, htParsFromTipoOper,
							strTipoOperAttrib, strCodPresidioOper);
					//					if (strTipoOperAttrib.equals("01"))
					//						//Nel caso di operatore tipo ass. sociale, devo aggiornare
					//						//anche la tabella 'ASS_OPABIL'.
					//						updAssOperAbilitati(dbc, strCodOperAttrib, hDati, strCodOperStart);
				} // fine for
			} // fine if
			System.out.println("AttrNuovoOperReferEJB.cambiaOperatore/Prima di commitTransaction");
			dbc.commitTransaction();
			System.out.println("AttrNuovoOperReferEJB.cambiaOperatore/Dopo commitTransaction");
			//			System.out.println(">>>>> 3 ChiudiProgettoEJB/cambiaOperatore: HO COMMITTATO");

			// 01/09/10: x ritornare il valore al client
			dbr.put("codice_oper_start", strCodOperStart);

			dbc.close();
			super.close(dbc);
			System.out.println("AttrNuovoOperReferEJB.cambiaOperatore/Prima di return dbr");
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				System.out.println("AttrNuovoOperReferEJB.cambiaOperatore() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + ce);
			}
			throw ce;
		} catch (DBRecordChangedException e) {
			System.out.println("AttrNuovoOperReferEJB.cambiaOperatore(): Eccezione= " + e);
			try {
				System.out.println("AttrNuovoOperReferEJB.cambiaOperatore() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("AttrNuovoOperReferEJB.cambiaOperatore(): Eccezione= " + e);
			try {
				System.out.println("AttrNuovoOperReferEJB.cambiaOperatore() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("AttrNuovoOperReferEJB.cambiaOperatore(): Eccezione= " + e);
			try {
				System.out.println("AttrNuovoOperReferEJB.cambiaOperatore() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una cambiaOperatore() - " + e);
		} finally {
			logout_nothrow(punto, dbcur, dbc);
		}
	}// END cambiaOperatore

	//gb 06.02.09
	private String getCodPresidioDaOper(ISASConnection dbc, String strCodOperAttrib) throws Exception {
		String punto = "getCodPresidioDaOper ";
		String str = "";
		String strSqlQuery = "";
		try {
			strSqlQuery = "SELECT cod_presidio" + " FROM operatori" + " WHERE codice = '" + strCodOperAttrib + "'";
			LOG.debug(punto + " recupero query Presido operatore>> " + strSqlQuery);
			ISASRecord dbr = dbc.readRecord(strSqlQuery);
			if ((dbr != null) && (dbr.get("cod_presidio") != null))
				str = (String) dbr.get("cod_presidio");
			return str;
		} catch (Exception ex) {
			LOG.error(punto + " Errore nel recuperare il codice del presidio dell'operatore >>>" + strSqlQuery);
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getCodPresidioDaOper()- " + ex);
		}
	}

	//Inizializza una Hashtable con i nomi delle tabelle dei contatti e degli operatori referenti
	//e di alcuni nomi di campo.
	private Hashtable<String, String> getParsFromTipoOper(String strTipoOperAttrib) throws Exception {
		Hashtable<String, String> htParsFromTipoOper = new Hashtable<String, String>();

		try {
			if (ISASUtil.valida(strTipoOperAttrib)) {
				if (strTipoOperAttrib.equals(GestTpOp.CTS_COD_INFERMIERE)) {
					setParsFromTipoOper(htParsFromTipoOper, "skinf", "n_contatto", "ski_infermiere",
							"ski_infermiere_da", "skinf_referente", "n_contatto", "skir_infermiere",
							"skir_infermiere_da", "");
				} else if (strTipoOperAttrib.equals(GestTpOp.CTS_COD_MEDICO)) {
					setParsFromTipoOper(htParsFromTipoOper, "skmedico", "n_contatto", "skm_medico", "skm_medico_da",
							"skmed_referente", "n_contatto", "skm_medico", "skm_medico_da", "");
				} else if (strTipoOperAttrib.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)) {
					setParsFromTipoOper(htParsFromTipoOper, "skfis", "n_contatto", "skf_fisiot", "skf_fisiot_da",
							"skfis_referente", "n_contatto", "skf_fisiot", "skf_fisiot_da", "");
				} else {
					setParsFromTipoOper(htParsFromTipoOper, "skfpg", "n_contatto", "skfpg_referente",
							"skfpg_referente_da", "skfpg_referente", "n_contatto", "skfpg_referente",
							"skfpg_referente_da", "skfpg_tipo_operatore");
				}
			}
			return htParsFromTipoOper;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getParsFromTipoOper()- " + ex);
		}
	}

	private void setParsFromTipoOper(Hashtable<String, String> htParsFromTipoOper, String contNomeTabella,
			String contNomeFldContatto, String contNomeFldOperRef, String contNomeFldOperRefData,
			String refNomeTabella, String refNContatto, String refNomeFldContatto, String refNomeFldData,
			String skfpgTipoOperatore) {
		htParsFromTipoOper.put("cont_nome_tabella", contNomeTabella);
		htParsFromTipoOper.put("cont_nome_fld_contatto", contNomeFldContatto);
		htParsFromTipoOper.put("cont_nome_fld_oper_ref", contNomeFldOperRef);
		htParsFromTipoOper.put("cont_nome_fld_oper_ref_data", contNomeFldOperRefData);
		htParsFromTipoOper.put("ref_nome_tabella", refNomeTabella);
		htParsFromTipoOper.put("ref_nome_fld_n_contatto", refNContatto);
		htParsFromTipoOper.put("ref_nome_fld_contatto", refNomeFldContatto);
		htParsFromTipoOper.put("ref_nome_fld_data", refNomeFldData);
		htParsFromTipoOper.put(CTS_TIPO_OPERATORE_SKFPG, skfpgTipoOperatore);
	}

	private ISASRecord getDbr(ISASConnection dbc, Hashtable hDati, Hashtable htParsFromTipoOper) throws Exception {
		String strNCartella = (String) hDati.get("n_cartella");
		String strNContatto = (String) hDati.get("n_contatto");
		try {
			String strQuery = "SElECT *" + " FROM " + htParsFromTipoOper.get("cont_nome_tabella")
					+ " WHERE n_cartella = " + strNCartella + " AND "
					+ htParsFromTipoOper.get("cont_nome_fld_contatto") + " = " + strNContatto;
			System.out.println("AttrNuovoOperReferEJB/getDbr/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("AttrNuovoOperReferEJB.getDbr(): Eccezione= " + e);
			throw e;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.getDbr()- " + ex);
		}
	}

	private void reWriteDbr(ISASConnection dbc, ISASRecord dbr, String strCodOperAttrib, String strDataAttrib,
			Hashtable htParsFromTipoOper, String strTipoOperAttrib, String strCodPresidioOper_) throws Exception {
		String punto = ver + "reWriteDbr ";
		try {
			String strFldName = (String) htParsFromTipoOper.get("cont_nome_fld_oper_ref");
			String strFldNameData = (String) htParsFromTipoOper.get("cont_nome_fld_oper_ref_data");
			dbr.put(strFldName, strCodOperAttrib);
			dbr.put(strFldNameData, strDataAttrib);

			gestisciTipoOperatoreGenerico(htParsFromTipoOper, strTipoOperAttrib, dbr);

			LOG.debug(punto + " dati che aggiorno>>" + (dbr.getHashtable()));
			dbc.writeRecord(dbr);
		} catch (ISASPermissionDeniedException e) {
			LOG.error(punto + " Eccezione= " + e);
			throw e;
		} catch (Exception ex) {
			LOG.error(punto + " Eccezione= " + ex);
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.reWriteDbr()- " + ex);
		}
	}

	private void addDbrInTabReferente(ISASConnection dbc, String strCodOperAttrib, String strDataAttrib,
			Hashtable hDati, Hashtable htParsFromTipoOper, String strTipoOperAttrib, String strCodPresidioOper)
			throws Exception, ISASPermissionDeniedException {
		ISASRecord dbr = null;
		String strNCartella = (String) hDati.get("n_cartella");
		String strNContatto = (String) hDati.get("n_contatto");
		String strNomeTabella = (String) htParsFromTipoOper.get("ref_nome_tabella");
		String strNomeFldDataAttrib = (String) htParsFromTipoOper.get("ref_nome_fld_data");
		String strNomeFldCodOperAttrib = (String) htParsFromTipoOper.get("ref_nome_fld_contatto");
		String strNomeNContatto = (String) htParsFromTipoOper.get("ref_nome_fld_n_contatto");
		try {
			String strQuery = "SELECT *" + " FROM " + strNomeTabella + " WHERE n_cartella = " + strNCartella + " AND "
					+ strNomeNContatto + " = " + strNContatto + " AND " + strNomeFldDataAttrib + " = "
					+ formatDate(dbc, strDataAttrib);
			System.out.println("AttrNuovoOperReferEJB/addDbrInTabReferente/strQuery: " + strQuery);
			dbr = dbc.readRecord(strQuery);
			if (dbr != null)
				dbr.put(strNomeFldCodOperAttrib, strCodOperAttrib);
			else {
				dbr = dbc.newRecord(strNomeTabella);
				dbr.put("n_cartella", new Integer(strNCartella));
				dbr.put(strNomeNContatto, new Integer(strNContatto));
				dbr.put(strNomeFldDataAttrib, strDataAttrib);
				dbr.put(strNomeFldCodOperAttrib, strCodOperAttrib);
			}

			gestisciTipoOperatoreGenerico(htParsFromTipoOper, strTipoOperAttrib, dbr);

			//			//gb 06.02.09
			//			if ((strTipoOperAttrib != null) && strTipoOperAttrib.equals("01"))
			//				dbr.put("opref_presidio", strCodPresidioOper);

			dbc.writeRecord(dbr);
		} catch (ISASPermissionDeniedException e) {
			System.out.println("AttrNuovoOperReferEJB.addDbrInTabReferente(): Eccezione= " + e);
			throw e;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.addDbrInTabReferente()- " + ex);
		}
	}

	private void gestisciTipoOperatoreGenerico(Hashtable htParsFromTipoOper, String strTipoOperAttrib, ISASRecord dbr)
			throws ISASMisuseException {
		String tpOperatore = ISASUtil.getValoreStringa(htParsFromTipoOper, CTS_TIPO_OPERATORE_SKFPG);
		if (ISASUtil.valida(tpOperatore)) {
			dbr.put(tpOperatore, strTipoOperAttrib);
		}
	}

	private void updAssOperAbilitati(ISASConnection dbc, String strCodOperAttrib, Hashtable hDati,
			String strCodOperStart) throws Exception {
		String strNCartella = (String) hDati.get("n_cartella");
		String strNProgetto = (String) hDati.get("n_contatto");
		try {
			String strQuery = "SELECT *" + " FROM ass_opabil" + " WHERE n_cartella = " + strNCartella
					+ " AND n_progetto = " + strNProgetto + " AND opabil_cod = '";

			// 01/09/10 ---
			String strQuery_1 = strQuery + strCodOperStart + "'";
			String strQuery_2 = strQuery + strCodOperAttrib + "'";

			// cancellazione rec vecchio referente da operatori abilitati
			System.out.println("AttrNuovoOperReferEJB/updAssOperAbilitati/strQuery_1: " + strQuery_1);
			ISASRecord dbr_1 = dbc.readRecord(strQuery_1);
			if (dbr_1 != null)
				dbc.deleteRecord(dbr_1);
			// 01/09/10 ---

			System.out.println("AttrNuovoOperReferEJB/updAssOperAbilitati/strQuery_2: " + strQuery_2);
			ISASRecord dbr_2 = dbc.readRecord(strQuery_2);
			if (dbr_2 != null)
				return;

			dbr_2 = dbc.newRecord("ass_opabil");
			dbr_2.put("n_cartella", new Integer(strNCartella));
			dbr_2.put("n_progetto", new Integer(strNProgetto));
			dbr_2.put("opabil_cod", strCodOperAttrib);
			dbr_2.put("opabil_liv", "3");
			dbc.writeRecord(dbr_2);
		} catch (ISASPermissionDeniedException e) {
			System.out.println("AttrNuovoOperReferEJB.updAssOperAbilitati(): Eccezione= " + e);
			throw e;
		} catch (Exception ex) {
			throw new Exception("Errore eseguendo una AttrNuovoOperReferEJB.updAssOperAbilitati()- " + ex);
		}
	}
	
	public void trasferisciPianificazioneAgenda(myLogin mylogin, Hashtable h, Vector vettSel)
			throws SQLException, CariException {
				ISASConnection dbc = null;
				boolean done = false;
				ISASCursor dbcur = null;
				Hashtable hApp = new Hashtable();
				Hashtable hPar = new Hashtable();
				Hashtable hTmp = new Hashtable();
				String condizCartella = "";
				System.out.println("DATA h-- "+h.toString());
				String dtInizio = h.get("data_riferimento").toString();
				
				String dtFine = "31/12/3000";
				if((h.get("data_attribuzione") != null) && (!h.get("data_attribuzione").equals("")))
					dtFine = h.get("data_attribuzione").toString();
				System.out.println("DATA dtInizio " + dtInizio);
				
				
				try {
					dbc = super.logIn(mylogin);
					if (vettSel != null) {
						for (int j = 0; j < vettSel.size(); j++) {
							Hashtable hDati = (Hashtable) vettSel.elementAt(j);
							String assistiti = hDati.get("n_cartella").toString();
							assistiti = assistiti.replaceAll("\\|", ",");
							condizCartella = " AND ag_cartella IN(" + assistiti + ")";
							String mysel = "SELECT *"
									+ " FROM agendant_interv"
									+ " WHERE ag_data >= " + formatDate(dbc, dtInizio)
									+ " AND ag_data <= " + formatDate(dbc, dtFine)
									+ " AND ag_oper_ref = '" + ((String) h.get("codice_oper_start")).trim() + "'"					
									+ condizCartella //elisa b 06/07/11
									+ " ORDER BY ag_cartella, ag_contatto, cod_obbiettivo, n_intervento, ag_orario, ag_data";

							debugMessage("agendaModOperatoreEJB/selectAgendaInterv, mysel: " + mysel);

							dbcur = dbc.startCursor(mysel);
							Vector vdbr = dbcur.getAllRecord();
							String chiave = "";

							/* creo un hashtable con la struttura seguente: 
							 * hApp={data={chiave=[v]}}}
							 */
							if ((vdbr != null) && (vdbr.size() > 0))
								for (int i = 0; i < vdbr.size(); i++) {
									ISASRecord dbrec = (ISASRecord) vdbr.elementAt(i);	


									if (dbrec != null) {
										String data = dbrec.get("ag_data").toString();

										chiave = ((Integer) dbrec.get("ag_cartella")).toString()
												+ "-" 
												+ ((Integer) dbrec.get("ag_contatto")).toString()
												+ "-" 
												+ (String) dbrec.get("cod_obbiettivo")
												+ "-" 
												+ ((Integer) dbrec.get("n_intervento")).toString();

										if(dbrec.get("ag_orario").toString().equals("0"))
											chiave += "-"+dbrec.get("ag_orario").toString();
										else
											chiave += "-"+dbrec.get("ag_orario").toString();

										//debugMessage("agendaModOperatoreEJB/selectAgendaInterv: chiave "+ chiave);

										if(hApp.containsKey(data)){
											hTmp = (Hashtable)hApp.get(data);					
										}else{
											hTmp = new Hashtable();
										}
										if(!hTmp.containsKey(chiave)){
											Vector v = new Vector();
											v.add(dbrec.get("ag_progr").toString());
											hTmp.put(chiave, v);
										}else{
											Vector v = (Vector)hTmp.get(chiave);
											v.add(dbrec.get("ag_progr").toString());
											hTmp.put(chiave, v);
										}	

										hApp.put(data, hTmp);

										// debugMessage("stessa riga dopo="+(recriga.getHashtable()).toString());
									}
								}

							//creo un hashtable con la struttura che si aspetta il metodo esistente
							hPar.put("referente_new", (String)h.get("codice_oper_end"));
							hPar.put("referente", (String)h.get("codice_oper_start"));

							dbc.startTransaction();

							Enumeration eDate = hApp.keys();
							while(eDate.hasMoreElements()){
								String data=(String)eDate.nextElement();
								System.out.println("DATA key==>"+data);
								Hashtable hC = (Hashtable)hApp.get(data);					
								System.out.println("hC"+hC.toString());
								Enumeration eCart=hC.keys();
								while(eCart.hasMoreElements()){
									String cartella = (String)eCart.nextElement();                 
									Vector vP = (Vector)hC.get((cartella));
									debugMessage("aggiorno:["+data+"],\n"+",["+cartella+"],\n["+vP.toString()+"]");
									aggiornamentiAgenda(dbc,data,cartella,vP,hPar);
								}
							}
						}
					}
					if (dbcur != null)
						dbcur.close();
					done = true;

					dbc.commitTransaction();
					//dbc.rollbackTransaction();
					super.close(dbc);
					System.out.println("COMMIT!!");
					//System.out.println("rollbackTransaction!!");
				}catch(CariException e2){
					throw e2;
				} catch (Exception e) {
					e.printStackTrace();
					throw new SQLException(
							"AgendaModOperatoreEJB: Errore eseguendo una selectAgendaInterv() - "+ e);
				} finally {
					if (!done) {
						try {
							if (dbcur != null)
								dbcur.close();
							dbc.rollbackTransaction();
							System.out.println("ROLLBACK!!");
							super.close(dbc);
						} catch (Exception e2) {
							debugMessage("" + e2);
						}
					}
				}
	}

	private void aggiornamentiAgenda(ISASConnection dbc,String data,String cartella,Vector progressivi,Hashtable h)throws SQLException, CariException
	{
		try{
			Enumeration en=progressivi.elements();
			while(en.hasMoreElements())
			{
				String prog=(String)en.nextElement();
				String mysel = "SELECT * "+
						//				gb 18/09/07				" from agenda_interv"+
						" from agendant_interv" + //gb 18/09/07
						" where ag_data="+formatDate(dbc,data)+
						" and ag_progr="+prog+
						" and ag_stato in (0,3,9)"+//[PUA]
						" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
				debugMessage("AgendaModOperatore/aggiornamenti, SELECT mysel: "+mysel);
				ISASRecord dbr=dbc.readRecord(mysel);
				if (dbr != null)
				{ 
					int ag_progr=caricoAppuntAgendaInterv(dbc,dbr,h);
					caricoAppuntAgendaIntpre(dbc,dbr,h,ag_progr);
					dbc.deleteRecord(dbr);
				}else {
					throw new CariException ("Impossibile attribuire un nuovo operatore referente!");

				}

			}	
		}catch(CariException e){
			throw e;

		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaModOperatore: Errore eseguendo una aggiornamenti() - "+  e1);
		}
	}
	

	
	private int caricoAppuntAgendaInterv(ISASConnection dbc,ISASRecord dbrOld,Hashtable h)
			throws Exception
			{
				String oper=(String)h.get("referente_new");
				boolean done=false;
				ISASRecord dbr = null;
				try{
					//controllo se per stessa cartella esiste gi� appuntamento in agenda del nuovo operatore
					//in modo da accodarla
					String selprog = "SELECT * "+
					" FROM agendant_interv " + //gb 19/09/07
					" WHERE ag_data =" +formatDate(dbc,((java.sql.Date)dbrOld.get("ag_data")).toString())+
					" AND ag_oper_ref='"+oper+"' and "+
					" ag_cartella="+dbrOld.get("ag_cartella")+" and "+
					" ag_contatto="+dbrOld.get("ag_contatto")+" and "+			
					" n_intervento = " + dbrOld.get("n_intervento") + " and "+
					" ag_orario="+dbrOld.get("ag_orario");
					if (! ((String)dbrOld.get("cod_obbiettivo")).trim() .equals(""))
						selprog+=" and cod_obbiettivo = '" + (String)dbrOld.get("cod_obbiettivo") + "'" ;
					else 	 selprog+=" and cod_obbiettivo is null" ;
					debugMessage("AgendaModOperatore/caricoAppuntAgendaInterv, selprog: "+selprog);
					dbr = dbc.readRecord(selprog);
					int progressivo=0;
					if (dbr != null)
					{
						//aggiorna stato agenda se diverso da zero
						progressivo=((Integer)dbr.get("ag_progr")).intValue();
						String stato=(String)dbr.get("ag_stato");
						if(!stato.equals("0")&& !stato.equals("9"))//[PUA]
						{
							progressivo= -1;//non posso accodare
						}
					}
					
					if(dbr==null || progressivo==-1)
					{
						// 1) prelevo il max progr inserito per quel giorno
						ISASRecord dbmax = null;
						String selmax = "SELECT MAX(ag_progr) max "+
						" FROM agendant_interv " + //gb 19/09/07
						" WHERE ag_data =" +formatDate(dbc,((java.sql.Date)dbrOld.get("ag_data")).toString())+
						" AND ag_oper_ref='"+oper+"'";
						//  debugMessage("select su agenda_interv="+selmax);
						dbmax = dbc.readRecord(selmax);
						if (dbmax != null)
						{
							if(dbmax.get("max")!=null)
							{
								int max = ((Integer)dbmax.get("max")).intValue();
								max++;
								progressivo = max;
							}
						}
//						debugMessage("progressivo="+progressivo);
						//2)inserisco nuovo record su agenda_interv
//						gb 19/09/07		ISASRecord rec_ag = dbc.newRecord("agenda_interv");
						ISASRecord rec_ag = dbc.newRecord("agendant_interv"); //gb 19/09/07
						Hashtable h_read = dbrOld.getHashtable();
						Enumeration n=h_read.keys();
						while(n.hasMoreElements())
						{
							String e=(String)n.nextElement();
							rec_ag.put(e,h_read.get(e));
						}
						rec_ag.put("ag_progr", new Integer(progressivo));
						rec_ag.put("ag_oper_ref",oper);
						/*if(rec_ag.get("pr_data")!=null && rec_ag.get("pr_progr")!=null){							  
							updatePuauvm(dbc,rec_ag.getHashtable(),"I");
						}*/
						if(rec_ag.get("num_scheda_pua")!=null && !(rec_ag.get("num_scheda_pua").toString()).equals("")){
							Hashtable hUpd=new Hashtable();
							hUpd.put("data",rec_ag.get("ag_data").toString());
							hUpd.put("num_scheda",rec_ag.get("num_scheda_pua"));
							hUpd.put("cod_oper",oper);
							collSins.designaOperatore(dbc,hUpd,"I");				
						}
						debugMessage("AgendaModOperatore/caricoAppuntAgendaInterv******carico su agenda_interv il record: \n"+(rec_ag.getHashtable()).toString());
						dbc.writeRecord(rec_ag);
						done=true;
					}
					return progressivo;
				}
				catch(Exception e1){
					debugMessage(""+e1);
					throw new SQLException("AgendaModOperatore: Errore eseguendo una caricoAppuntAgendaInterv() - "+  e1);
				}        
			}
	
	private void caricoAppuntAgendaIntpre(ISASConnection dbc,ISASRecord dbrOld,Hashtable h,int progressivo)
			throws Exception
			{
				ISASCursor dbcur=null;
				boolean done=false;
				try{
					String oper=(String)h.get("referente_new");
					//leggo prestazioni da spostare
					String sel = "SELECT *" +
//					gb 19/09/07			" FROM agenda_intpre WHERE "+
					" FROM agendant_intpre WHERE "+ //gb 19/09/07
					" ap_data="+formatDate(dbc,((java.sql.Date)dbrOld.get("ag_data")).toString())+
					" AND ap_oper_ref='"+dbrOld.get("ag_oper_ref")+"'"+
					" AND ap_progr="+dbrOld.get("ag_progr");
					debugMessage("AgendaModOperatore/caricoAppuntAgendaIntpre, sel: "+sel);
					dbcur=dbc.startCursor(sel);
					while (dbcur.next())
					{  
						ISASRecord dbrc=dbcur.getRecord();
						sel = "SELECT *" +
//						gb 19/09/07			" FROM agenda_intpre WHERE "+
						" FROM agendant_intpre WHERE "+ //gb 19/09/07
						"ap_data="+formatDate(dbc,((java.sql.Date)dbrc.get("ap_data")).toString())+
						" AND ap_oper_ref='"+oper+"'"+
						" AND ap_progr="+progressivo+
						" AND ap_prest_cod='"+dbrc.get("ap_prest_cod")+"'";
						debugMessage("AgendaModOperatore/caricoAppuntAgendaIntpre, sel (2): "+sel);
						ISASRecord is = dbc.readRecord(sel);
						if(is==null)
						{
							Hashtable h_read = dbrc.getHashtable();
							Enumeration n=h_read.keys();
//							gb 19/09/07		   ISASRecord rec_ag = dbc.newRecord("agenda_intpre");
							ISASRecord rec_ag = dbc.newRecord("agendant_intpre"); //gb 19/09/07
							while(n.hasMoreElements())
							{
								String e=(String)n.nextElement();
								rec_ag.put(e,h_read.get(e));
							}
							rec_ag.put("ap_progr", new Integer(progressivo));
							rec_ag.put("ap_oper_ref",oper);
							dbc.writeRecord(rec_ag);
						}
						else
						{
							debugMessage("**** IL RECORD RISULTA CARICATO: \n"+(is.getHashtable()).toString());
							//non carico nuovamente l aprestazione e proseguo
						}
						String sel2="SELECT *" +
//						gb 19/09/07			    " FROM agenda_intpre WHERE "+
						" FROM agendant_intpre WHERE "+ //gb 19/09/07
						" ap_data="+formatDate(dbc,((java.sql.Date)dbrc.get("ap_data")).toString())+
						" AND ap_oper_ref='"+dbrc.get("ap_oper_ref")+"'"+
						" AND ap_progr="+dbrc.get("ap_progr")+
						" AND ap_prest_cod='"+dbrc.get("ap_prest_cod")+"'";
						debugMessage("AgendaModOperatore/caricoAppuntAgendaIntpre, sel2 (3): "+sel2);
						ISASRecord is2 = dbc.readRecord(sel2);
						dbc.deleteRecord(is2);
					}
					if (dbcur != null)dbcur.close();
					done=true;
				}catch(Exception e1){
					debugMessage(""+e1);
					throw new SQLException("AgendaModOperatore: Errore eseguendo una caricoAppuntAgendaIntpre() - "+  e1);
				}
				finally{
					if(!done){
						try{
							if (dbcur != null)
								dbcur.close();
						}catch(Exception e2){debugMessage(""+e2);}
					}
				}
			}
	
	public void trasferisciPianificazione(myLogin mylogin, Hashtable h, Vector vettSel)	throws SQLException {
		ISASConnection dbc = null;
		boolean done = false;
		ISASCursor dbcur = null;

		LOG.info("trasferisciPianificazione " + h.toString());

		String messaggiAgenda = "";
		
		String nCartella = "";
		String nProgetto = "";
		String codObiettivo = "";
		String nIntervento = "";
		String dtAttribuzione ="";
		String tipoOper = (String) h.get("tipo_oper_start");	
		if (h.get("data_attribuzione")!=null && !h.get("data_attribuzione").equals(""))
			dtAttribuzione = h.get("data_attribuzione").toString();
		String codOpeNew = (String) h.get("codice_oper_end");
		String pa_data="";

		/* hashtable con chiave il numero di cartella e valore un vettore
		 * contenente i dati del piano dell'assistito */
		//Hashtable hDati = (Hashtable)h.get("dati_assistiti");
		String dataDB ="";
		if (h.get("data_attribuzione")!=null && !h.get("data_attribuzione").equals(""))
			dataDB =dtAttribuzione;

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction(); //bargi 02/07/2013
//			Enumeration key = h.keys();
//			while(key.hasMoreElements()){
				if (vettSel != null) {
					for (int j = 0; j < vettSel.size(); j++) {
						Hashtable hDati = (Hashtable) vettSel.elementAt(j);
						nCartella = hDati.get("n_cartella").toString();
						codObiettivo = hDati.get("cod_obbiettivo").toString();
						nIntervento = hDati.get("n_intervento").toString();
						nProgetto = hDati.get("n_progetto").toString();
						pa_data = hDati.get("data").toString();
						//nCartella = (String)key.nextElement();
						Vector vD = (Vector) h.get(nCartella);
						//LOG.debug("++trasferisciPianificazione vD " + vD.size());
						//nProgetto = (String) vD.get(0);
						//codObiettivo = (String) vD.get(1);
						//nIntervento = (String) vD.get(2);
						//String pa_data = (String) vD.get(3);

						Hashtable hPar = new Hashtable();
						hPar.put("operatore_new", codOpeNew);
						hPar.put("n_cartella", nCartella);
						hPar.put("n_progetto", nProgetto);
						hPar.put("cod_obbiettivo", codObiettivo);
						hPar.put("n_intervento", nIntervento);
						hPar.put("pa_tipo_oper", tipoOper);
						hPar.put("data_apertura", pa_data);
						hPar.put("data_chiusura", dtAttribuzione);
						//hPar.put("data_chiusura", dataDB);
						hPar.put("preservaChiusura", true); // passo questo parametro per dire al duplica piano che la data di fine rimane sempre quella e non deve cancellarla
						CaricaAgendaPrestazioni caricaAg = new CaricaAgendaPrestazioni();

						try {
							caricaAg.duplicaPiano(dbc, hPar);
						} catch (CariException e) {
							LOG.debug("Msg restituito " + e.getMessage());
							messaggiAgenda = e.getMessage();
						}				

					}

				}
//			}
			//FIXME
//			dbc.commitTransaction();
			LOG.debug("COMMIT!!");
			//dbc.rollbackTransaction();
			//LOG.debug("ROLLBACK!!");
			done = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw newEjbException("Errore in trasferisciPianificazione", e);
		} finally {
			if(!done)
				rollback_nothrow("trasferisciPianificazione", dbc);
			close_dbcur_nothrow("trasferisciPianificazione", dbcur);	
			logout_nothrow("trasferisciPianificazione", dbc);
		}
	}

}
package it.caribel.app.sinssnt.bean.modificati;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
//
// ==========================================================================

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.NumberDateFormat;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

public class ChiudiContattoEJB extends SINSSNTConnectionEJB {
	public ChiudiContattoEJB() {
	}

	//11/09/12: x gestione chiusura CASO
	private GestCasi gestore_casi = new GestCasi();
	private EveUtils evUtl = new EveUtils();

	//	private Hashtable hOperatore_ = new Hashtable();
	private String ver = "2-" + this.getClass().getName() + " ";
	private static final String CTS_MOTIVO_TESTO = "motivo_txt";
	public static final String CTS_COD_OPERATORE_CHIUSURA = "cod_oper_profile";
	public static final String CTS_DATA_CHIUSURA = "data_chiusura";
	public static final String CTS_MOTIVO_CHIUSURA = "mot_chiusura";
	public static final String CTS_TIPO_OPERATORE = "tipo_op";

	/**
	*  Restituisce un elenco di assistiti che hanno:
	*  - un contatto aperto prima della data ottenuta facendo (oggi - gg periodo scelto);
	*  - dtChiusura = null
	*  - non hanno accessi dopo la data ottenuta
	*/

	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "query ";
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		LOG.info(punto + " inizio con dati>>" + h);
		try {
			dbc = super.logIn(mylogin);
			String myselectCont = mkSelect(dbc, h);

			LOG.debug("Query>> \n" + myselectCont);
			LOG.debug("ChiudiContattoEJB queryCONT INIZIO" + currentTime());
			dbcur = dbc.startCursor(myselectCont);
			LOG.debug("ChiudiContattoEJB query FINE" + currentTime() + " - RECORD TROVATI-->" + dbcur.getDimension());
			Vector<ISASRecord> vdbr = preparaBody(dbcur, h, dbc);
			LOG.debug("ChiudiContattoEJB query FINE" + currentTime() + " - RECORD RIMASTI-->" + vdbr.size());
			dbcur.close();
			dbc.close();
			super.close(dbc);

			LOG.debug(" record Recuperati: " + vdbr.size());
			LOG.debug("ChiudiContattoEJB query FINE ELABORAZIONE" + currentTime());
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error("ChiudiContattoEJB: query - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		} finally {
			logout_nothrow(punto, dbcur, dbc);
		}
	}// END query

	private Hashtable<String, String> recuperaInfTabellaOperatore(String tipoOp) {
		Hashtable<String, String> infoTabella = new Hashtable<String, String>();

		if (ISASUtil.valida(tipoOp)) {
			if (tipoOp.equals(GestTpOp.CTS_COD_INFERMIERE)) {
				recuperaTabellaInfermiere(infoTabella);
			} else if (tipoOp.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)) {
				recuperaTabellaFisioterapista(infoTabella);
			} else if (tipoOp.equals(GestTpOp.CTS_COD_MEDICO)) {
				recuperaTabellaMedico(infoTabella);
			} else {
				recuperaTabellaOperatoreGenerico(infoTabella);
			}
		}
		return infoTabella;
	}

	private void recuperaTabellaOperatoreGenerico(Hashtable<String, String> infoTabella) {
		String punto = ver + "recuperaTabellaOperatoreGenerico ";
		infoTabella.put("tabella", "skfpg");
		infoTabella.put("n_contatto", "n_contatto");
		infoTabella.put("tipo_operatore", "skfpg_tipo_operatore");
		infoTabella.put("data_apertura", "skfpg_data_apertura");
		infoTabella.put("data_chiusura", "skfpg_data_uscita");
		infoTabella.put("descr_contatto", "skfpg_descr_contatto");
		infoTabella.put("motivo", "skfpg_motivo_uscita");
		infoTabella.put(CTS_MOTIVO_TESTO, "skfpg_motivo_txt");
		infoTabella.put("operatore", "skfpg_operatore");
		infoTabella.put("data_chiusura_contsan", "");
		LOG.trace(punto + " datiTabella>" + infoTabella);
	}

	private void recuperaTabellaMedico(Hashtable<String, String> infoTabella) {
		String punto = ver + "recuperaTabellaMedico ";

		infoTabella.put("tabella", "skmedico");
		infoTabella.put("n_contatto", "n_contatto");
		infoTabella.put("data_apertura", "skm_data_apertura");
		infoTabella.put("data_chiusura", "skm_data_chiusura");
		infoTabella.put("descr_contatto", "skm_descr_contatto");
		infoTabella.put("motivo", "skm_motivo_chius");
		// non previsto il campo motivo dimissione testo  
		infoTabella.put("operatore", "skm_medico");
		infoTabella.put("data_chiusura_contsan", "data_chius_medico");

		LOG.trace(punto + " datiTabella>" + infoTabella);
	}

	private void recuperaTabellaFisioterapista(Hashtable<String, String> infoTabella) {
		String punto = ver + "recuperaTabellaFisioterapista ";
		infoTabella.put("tabella", "skfis");
		infoTabella.put("n_contatto", "n_contatto");
		infoTabella.put("data_apertura", "skf_data");
		infoTabella.put("data_chiusura", "skf_data_chiusura");
		infoTabella.put("descr_contatto", "skf_descr_contatto");
		infoTabella.put("motivo", "skf_motivo_chius");
		// non previsto il campo motivo dimissione testo  
		infoTabella.put("operatore", "skf_fisiot");
		infoTabella.put("data_chiusura_contsan", "data_chius_fisiot");

		LOG.trace(punto + " datiTabella>" + infoTabella);
	}

	private void recuperaTabellaInfermiere(Hashtable<String, String> infoTabella) {
		String punto = ver + "recuperaTabellaInfermiere ";
		infoTabella.put("tabella", "skinf");
		infoTabella.put("n_contatto", "n_contatto");
		infoTabella.put("data_apertura", "ski_data_apertura");
		infoTabella.put("data_chiusura", "ski_data_uscita");
		infoTabella.put("descr_contatto", "ski_descr_contatto");
		infoTabella.put("motivo", "ski_dimissioni");
		infoTabella.put(CTS_MOTIVO_TESTO, "ski_dimissioni_txt");
		infoTabella.put("operatore", "ski_infermiere");
		infoTabella.put("data_chiusura_contsan", "data_chius_infer");

		LOG.trace(punto + " datiTabella>" + infoTabella);
	}

	//	private void caricaHash() {
	//Operatore sociale
	//		Hashtable hCampiSoc = new Hashtable();
	//		hCampiSoc.put("tabella", "ass_progetto");
	//		hCampiSoc.put("n_contatto", "n_progetto");
	//		hCampiSoc.put("data_apertura", "ap_data_apertura");
	//		hCampiSoc.put("data_chiusura", "ap_data_chiusura");
	//		hCampiSoc.put("descr_contatto", "'Nessuna descrizione'");
	//		hCampiSoc.put("motivo", "motivo_chiusura");
	//		hCampiSoc.put("operatore", "ap_ass_ref");
	//		//gb 31/10/07: fine *******
	//		hOperatore.put("01", hCampiSoc);
	//Infermiere
	//		Hashtable hCampiInf = new Hashtable();
	//		hCampiInf.put("tabella", "skinf");
	//		hCampiInf.put("n_contatto", "n_contatto");
	//		hCampiInf.put("data_apertura", "ski_data_apertura");
	//		hCampiInf.put("data_chiusura", "ski_data_uscita");
	//		hCampiInf.put("descr_contatto", "ski_descr_contatto");
	//		hCampiInf.put("motivo", "ski_dimissioni");
	//		hCampiInf.put("operatore", "ski_infermiere");
	//		hCampiInf.put("data_chiusura_contsan", "data_chius_infer");
	//		hOperatore.put("02", hCampiInf);
	//Medico
	//		Hashtable hCampiMed = new Hashtable();
	//		hCampiMed.put("tabella", "skmedico");
	//		hCampiMed.put("n_contatto", "n_contatto");
	//		hCampiMed.put("data_apertura", "skm_data_apertura");
	//		hCampiMed.put("data_chiusura", "skm_data_chiusura");
	//		hCampiMed.put("descr_contatto", "skm_descr_contatto");
	//		hCampiMed.put("motivo", "skm_motivo_chius");
	//		hCampiMed.put("operatore", "skm_medico");
	//		hCampiMed.put("data_chiusura_contsan", "data_chius_medico");
	//		hOperatore.put("03", hCampiMed);
	//Fisioterapista
	//		Hashtable hCampiFis = new Hashtable();
	//		hCampiFis.put("tabella", "skfis");
	//		hCampiFis.put("n_contatto", "n_contatto");
	//		hCampiFis.put("data_apertura", "skf_data");
	//		hCampiFis.put("data_chiusura", "skf_data_chiusura");
	//		hCampiFis.put("descr_contatto", "skf_descr_contatto");
	//		hCampiFis.put("motivo", "skf_motivo_chius");
	//		hCampiFis.put("operatore", "skf_fisiot");
	//		hCampiFis.put("data_chiusura_contsan", "data_chius_fisiot");
	//		hOperatore.put("04", hCampiFis);
	//gb 02/11/07 *******
	//Medico cure palliative (Oncologo)
	//		Hashtable hCampiMedPal = new Hashtable();
	//		hCampiMedPal.put("tabella", "skmedpal");
	//		hCampiMedPal.put("n_contatto", "n_contatto");
	//		hCampiMedPal.put("data_apertura", "skm_data_apertura");
	//		hCampiMedPal.put("data_chiusura", "skm_data_chiusura");
	//		hCampiMedPal.put("descr_contatto", "skm_descr_contatto");
	//		hCampiMedPal.put("motivo", "skm_motivo_chius");
	//		hCampiMedPal.put("operatore", "skm_medico");
	//		hCampiMedPal.put("data_chiusura_contsan", "data_chius_ostetr");
	//		hOperatore.put("52", hCampiMedPal);
	//gb 02/11/07: fine *******
	//	}

	private String mkSelect(ISASConnection mydbc, Hashtable par) throws Exception {
		String mySelect = "";
		ServerUtility su = new ServerUtility();
		try {
			//Controllo se mi sono arrivati i filtri per la zona/distr/presidio
			String zona = "";
			if (par.get("zona") != null && !par.get("zona").equals("TUTTO"))
				zona = (String) par.get("zona");
			if (zona.equals("") && !zona.equals("TUTTO"))
				mySelect = mkSelectSing(mydbc, par);
			else
				mySelect = mkSelectTerr(mydbc, par);

			return mySelect;
		} catch (Exception e) {
			e.printStackTrace();
			debugMessage("ChiudiContattoEJB.mkSelect(): " + e);
			throw new SQLException("Errore eseguendo ChiudiContattoEJB.mkSelect()");
		}
	}

	private String mkSelectSing(ISASConnection mydbc, Hashtable par) throws Exception {
		ServerUtility su = new ServerUtility();
		try {
			String dataPeriodo = (String) par.get("periodo");

			String tipoOp = (String) par.get(CTS_TIPO_OPERATORE);
			//			Hashtable hCampi = (Hashtable) hOperatore.get(tipoOp);
			Hashtable<String, String> hCampi = recuperaInfTabellaOperatore(tipoOp);

			String mySelect = "SELECT nvl(TRIM(ca.cognome),'') || ' ' || nvl(TRIM(ca.nome),'') assistito,"
					+ " co.n_cartella,"
					+
					//gb 31/10/07				" co.n_contatto,"+
					" co."
					+ hCampi.get("n_contatto")
					+ " n_contatto,"
					+ //gb 31/10/07
					" nvl( TRIM(" + hCampi.get("descr_contatto") + "),' ')  descrizione," + " co."
					+ hCampi.get("data_apertura") + " data_contatto" + " FROM " + hCampi.get("tabella") + " co,"
					+ " cartella ca " + " WHERE ca.n_cartella = co.n_cartella" + " AND co."
					+ hCampi.get("data_chiusura") + "  IS NULL" + " AND co." + hCampi.get("data_apertura") + " <= "
					+ formatDate(mydbc, dataPeriodo);
			mySelect = su.addWhere(mySelect, su.REL_AND, "" + hCampi.get("operatore"), su.OP_EQ_STR,
					(String) par.get("codice"));
			mySelect = mySelect + " ORDER BY assistito," +
			//gb 31/10/07      		    		    " co.n_cartella, co.n_contatto";
					" co.n_cartella, n_contatto"; //gb 31/10/07
			LOG.debug("ChiudiContatto sel: " + mySelect);
			return mySelect;
		} catch (Exception e) {
			e.printStackTrace();
			debugMessage("ChiudiContattoEJB.mkSelectSing(): " + e);
			throw new SQLException("Errore eseguendo ChiudiContattoEJB.mkSelectSing()");
		}

	}// END mkSelect

	private String mkSelectTerr(ISASConnection mydbc, Hashtable par) throws Exception {
		ServerUtility su = new ServerUtility();
		try {
			String dataPeriodo = (String) par.get("periodo");

			String tipoOp = (String) par.get(CTS_TIPO_OPERATORE);
			//			Hashtable hCampi = (Hashtable) hOperatore.get(tipoOp);
			Hashtable<String, String> hCampi = recuperaInfTabellaOperatore(tipoOp);
			String mySelect = "SELECT nvl(TRIM(ca.cognome),'') || ' ' || nvl(TRIM(ca.nome),'') assistito,"
					+ " co.n_cartella," +
					//gb 31/10/07                                      " co.n_contatto,"+
					" co."
					+ hCampi.get("n_contatto")
					+ " n_contatto,"
					+ //gb 31/10/07
					" nvl( TRIM("
					+ hCampi.get("descr_contatto")
					+ "),' ')  descrizione,"
					+ " co."
					+ hCampi.get("data_apertura")
					+ " data_contatto"
					+ " FROM "
					+ hCampi.get("tabella")
					+ " co,"
					+ " cartella ca ,ubicazioni_n u , operatori op "
					+ " WHERE ca.n_cartella = co.n_cartella"
					+ " AND co."
					+ hCampi.get("data_chiusura")
					+ "  IS NULL"
					+ " AND co."
					+ hCampi.get("data_apertura")
					+ " <= "
					+ formatDate(mydbc, dataPeriodo)
					+ " AND co."
					+ hCampi.get("operatore")
					+ "=op.codice"
					+ " AND u.codice=op.cod_presidio";
			//filtro per la parte territoriale
			mySelect = su.addWhere(mySelect, su.REL_AND, "u.tipo", su.OP_EQ_STR, "P");
			mySelect = su.addWhere(mySelect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			LOG.debug("distr" + par.get("distr"));
			String distretto = "";
			distretto = (String) par.get("distr");
			LOG.debug("distr1" + distretto);
			if (distretto != null && !distretto.equals("") && !distretto.equals("TUTTO"))
				mySelect = su
						.addWhere(mySelect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distr"));
			String presidio = "";
			presidio = (String) par.get("pres");
			if (presidio != null && !presidio.equals("") && !presidio.equals("TUTTI"))
				mySelect = su.addWhere(mySelect, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pres"));

			mySelect = su.addWhere(mySelect, su.REL_AND, "" + hCampi.get("operatore"), su.OP_EQ_STR,
					(String) par.get("codice"));
			mySelect = mySelect + " ORDER BY assistito," +
			//gb 31/10/07                                          " co.n_cartella, co.n_contatto";
					" co.n_cartella, n_contatto"; //gb 31/10/07
			LOG.debug("ChiudiContatto sel: " + mySelect);
			return mySelect;
		} catch (Exception e) {
			e.printStackTrace();
			debugMessage("ChiudiContattoEJB.mkSelectSing(): " + e);
			throw new SQLException("Errore eseguendo ChiudiContattoEJB.mkSelectSing()");
		}

	}// END mkSelectTerr

	private Vector<ISASRecord> preparaBody(ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		String punto = ver + "";
		Vector<ISASRecord> vRet = new Vector<ISASRecord>();
		NumberDateFormat su = new NumberDateFormat();
		String dataPeriodo = (String) par.get("periodo");
		String dataRife = dataPeriodo.substring(8, 10) + "/" + dataPeriodo.substring(5, 7) + "/"
				+ dataPeriodo.substring(0, 4);

		String tipoOper = (String) par.get(CTS_TIPO_OPERATORE);
		try {
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String dataPrest = EsistePrest(dbc, dbr, tipoOper);
				if (dataPrest.equals("") || su.dateCompare(dataPrest, dataRife) == 2) {
					/*dataPrest==""-->
						non esistono interventi per quel contatto/cartella/operatore
						devo aggiungere il record al vettore*/
					/*==2->dataRife � maggiore della data prestazione massima --> che
					non esistono interventi con data maggiore alla data periodo->
					devo aggiungerlo
					 */
					dbr.put("data_prest", dataPrest);
					vRet.addElement(dbr);
				} else {
					LOG.trace(punto + " scarto il record, in quanto esiste un intervento che va oltre>>" + dataPrest
							+ "< data dataRife>" + dataRife + "<");
				}
			}
			return vRet;
		} catch (Exception e) {
			debugMessage("ChiudiContattoEJB.preparaBody(): " + e);
			throw new SQLException("Errore eseguendo ChiudiContattoEJB.preparaBody()");
		}
	}

	private String EsistePrest(ISASConnection dbc, ISASRecord dbrCont, String tipoOp) throws Exception {
		String punto = ver + "EsistePrest ";
		/*restituisce se la trova la data dell'ultimo intervento inserito */
		String dataPrest = "";
		try {
			if (dbrCont != null) {
				String n_cartella = "";
				String n_contatto = "";
				String strNClausolaContatto = "";

				if (dbrCont.get("n_cartella") != null)
					n_cartella = "" + dbrCont.get("n_cartella");
				/*gb 31/10/07 *******
					if (dbrCont.get("n_contatto")!=null)
					   n_contatto=""+dbrCont.get("n_contatto");
				*gb 31/10/07: fine *******/
				/*** 10/06/09 m			  
				//gb 31/10/07 *******
					if (tipoOp.equals("01"))
					   {
					   if (dbrCont.get("n_progetto")!=null)
				              n_contatto=""+dbrCont.get("n_progetto");
					   strNClausolaContatto =  " AND n_progetto = " + n_contatto;
					   }
					else
					   {
					   if (dbrCont.get("n_contatto")!=null)
				              n_contatto=""+dbrCont.get("n_contatto");
					   strNClausolaContatto =  " AND int_contatto = " + n_contatto;
					   }
				//gb 31/10/07: fine *******
				*** 10/06/09 m */
				// 10/06/09 m: � mappato come "n_contatto" anche per AS ---
				if (dbrCont.get("n_contatto") != null)
					n_contatto = "" + dbrCont.get("n_contatto");
				strNClausolaContatto = " AND " + (tipoOp.equals("01") ? "n_progetto" : "int_contatto") + " = "
						+ n_contatto;
				// 10/06/09 m ---

				if (n_cartella.equals("") || n_contatto.equals(""))
					return "";
				String sel = "SELECT MAX(int_data_prest) data_prestazione FROM interv  " + " WHERE int_cartella="
						+ n_cartella +
						//gb 31/10/07          " AND int_contatto="+ n_contatto+
						strNClausolaContatto + //gb 31/10/07
						" AND int_tipo_oper='" + tipoOp + "'";
				LOG.trace(punto + " query Interv>>" + sel);
				ISASRecord dbr = dbc.readRecord(sel);
				//		LOG.debug("ChiudiContattoEJB.chiudiAllCont() => sel=["+sel+"]");	
				if (dbr != null && dbr.get("data_prestazione") != null) {
					dataPrest = ((java.sql.Date) dbr.get("data_prestazione")).toString();
				}
				// 06/10/09 m.: x il sociale si devono considerare anche i record su ASS_INTERVENTI ---------------
				/*
				if (tipoOp.equals("01")) {
					String sel_2 = "SELECT MAX(int_data_ins) dt_assinterv" + " FROM ass_interventi"
							+ " WHERE n_cartella = " + n_cartella + strNClausolaContatto;
					ISASRecord dbr_2 = dbc.readRecord(sel_2);
					//			LOG.debug("ChiudiContattoEJB.chiudiAllCont() => sel_2=["+sel_2+"]");			
					// se esiste data su ASS_INTERVENTI si confronta con quella di INTERV
					if ((dbr_2 != null) && (dbr_2.get("dt_assinterv") != null)) {
						String dtPrest = "";
						if (dataPrest.length() == 10)
							dtPrest = dataPrest.substring(0, 4) + dataPrest.substring(5, 7)
									+ dataPrest.substring(8, 10);
						DataWI dtAssInterv = new DataWI((java.sql.Date) dbr_2.get("dt_assinterv"));
						//				LOG.debug("ChiudiContattoEJB.chiudiAllCont() => dataPrest=["+dataPrest+"] - dtAssInterv=["+dtAssInterv.getFormattedString2(1)+"]");					
						if (!dtPrest.trim().equals("")) { // se esiste data su INTERV
							// se data ASS_INTERVENTI � maggiore --> si considera quella su ASS_INTERVENTI
							if (dtAssInterv.isSuccessiva(dtPrest))
								dataPrest = dtAssInterv.getFormattedString2(1);
						} else
							// non esiste data su INTERV --> si considera quella su ASS_INTERVENTI
							dataPrest = dtAssInterv.getFormattedString2(1);
					}
				}
				*/
				// 06/10/09 m --------------------------------------------------------------------------------------------------------------------
				if (dataPrest.length() == 10)
					dataPrest = dataPrest.substring(8, 10) + "/" + dataPrest.substring(5, 7) + "/"
							+ dataPrest.substring(0, 4);
				else
					dataPrest = "";
			}
			return dataPrest;
		} catch (Exception e) {
			debugMessage("ChiudiContattoEJB.EsistePrest(): " + e);
			throw new SQLException("Errore eseguendo ChiudiContattoEJB.EsistePrest()");
		}
	}

	/**
	* 	Riceve un vettore di hashtable contenenti cart e cont degli assistiti selezionati
	*	x la chiusura del contatto.
	*	Per ognuno:
	*	- legge il record sullo specifica contatto, modifica la data chiusura ed il motivo e lo riscrive;
	*/
	public ISASRecord chiudiAllCont(myLogin mylogin, Hashtable par, Vector vettSel) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		String punto = ver + "chiudiAllCont ";
		ISASConnection dbc = null;
		ISASRecord dbr = null;// mi serve solo x restituire qualcosa al client
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			//			caricaHash();
//			Hashtable statoRichiesta = decod.caricaDaTabVoci(dbc, Costanti.TAB_VAL_RSA_RICHIESTA_FLAG_STATO);
			ManagerDecod decod = new ManagerDecod();
			Hashtable<String, String> motivoChiusuraTabVoci = decod.caricaDaTabVoci(dbc, CostantiSinssntW.TAB_VAL_ICHIUS);
					
			if (vettSel != null) {
				for (int j = 0; j < vettSel.size(); j++) {
					Hashtable hDati = (Hashtable) vettSel.elementAt(j);
					dbr = aggContatti(dbc, hDati, par, motivoChiusuraTabVoci);
				}
			}
			dbc.commitTransaction();
			//			LOG.debug(">>>>> 3 ChiudiContattoEJB: HO COMMITTATO");
			return dbr;
		}
		//gb 31/10/07 **************
		catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				LOG.error("ChiudiContattoEJB.chiudiAllCont() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + ce);
			}
			throw ce;
		}
		//gb 31/10/07: fine **************
		catch (DBRecordChangedException e) {
			LOG.error("ChiudiContattoEJB.chiudiAllCont(): Eccezione= " + e);
			try {
				LOG.error("ChiudiContattoEJB.chiudiAllCont() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			LOG.error("ChiudiContattoEJB.chiudiAllCont(): Eccezione= " + e);
			try {
				LOG.error("ChiudiContattoEJB.chiudiAllCont() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			LOG.error("ChiudiContattoEJB.chiudiAllCont(): Eccezione= " + e);
			try {
				LOG.error("ChiudiContattoEJB.chiudiAllCont() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una insert() - " + e);

		} finally {
			logout_nothrow(punto, dbc);
		}
	}// END chiudiAllCont

	/*gb 31/10/07 *******
		private ISASRecord aggContatti(ISASConnection mydbc,
	                                          Hashtable hDati,Hashtable par) throws Exception
		{
	                String n_cartella=""+hDati.get("n_cartella");
	                String n_contatto=""+hDati.get("n_contatto");
	                String data_chiusura=""+hDati.get("data_chiusura");
	                data_chiusura=data_chiusura.substring(6,10)+"-"+
	                              data_chiusura.substring(3,5)+"-"+
	                              data_chiusura.substring(0,2);
	                String motivo=""+par.get("motivo");

	                String tipoOp=(String)par.get(CTS_TIPO_OPERATORE);
	                Hashtable hCampi=(Hashtable) hOperatore.get(tipoOp);

			String selContatto = "SELECT * FROM "+ hCampi.get("tabella")+
	                                            " WHERE n_cartella = " + n_cartella +
	                                            " AND n_contatto = " + n_contatto;
			ISASRecord dbrCont = mydbc.readRecord(selContatto);
	                //devo aggiornare contsan
			String selContsan = "SELECT * FROM contsan "+
	                                            " WHERE n_cartella = " + n_cartella +
	                                            " AND n_contatto = " + n_contatto;
	                ISASRecord dbrContsan = mydbc.readRecord(selContsan);
	                dbrContsan.put(""+hCampi.get("data_chiusura_contsan"),data_chiusura);
	                mydbc.writeRecord(dbrContsan);
	                dbrCont.put(""+hCampi.get("data_chiusura"),data_chiusura);
			dbrCont.put(""+hCampi.get("motivo"), motivo);
			mydbc.writeRecord(dbrCont);
			return dbrCont;
		}// END aggSMScheda
	*gb 31/10/07. fine *******/

	//gb 31/10/07 *******
	private ISASRecord aggContatti(ISASConnection mydbc, Hashtable hDati, Hashtable par, Hashtable<String,String> motivoChiusuraTabVoci) throws Exception,
			CariException {
		String n_cartella = "" + hDati.get("n_cartella");
		String n_contatto = "" + hDati.get("n_contatto");
		String data_chiusura = "" + hDati.get(CTS_DATA_CHIUSURA );
		data_chiusura = data_chiusura.substring(6, 10) + "-" + data_chiusura.substring(3, 5) + "-"
				+ data_chiusura.substring(0, 2);
		String motivo = "" + par.get(CTS_MOTIVO_CHIUSURA);
		String tipoOp = (String) par.get(CTS_TIPO_OPERATORE);

		//gb 31/10/07 *******
		String strCodOperChiusura = (String) par.get(CTS_COD_OPERATORE_CHIUSURA );
		// Controlli e chiusure entit� sottostanti
		CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
		// date prestazioni erogate della tabella interv.
		// date aper. e date chius. dei piani assitenziali.
		// date aper. dei piani accessi.
		// date aper. e date chius. degli obiettivi, interventi
		String strMsgCheckDtCh = clCcec.checkDtChDaContProgGTDtApeDtCh(mydbc, n_cartella, n_contatto, data_chiusura,
				tipoOp);
		if (!strMsgCheckDtCh.equals(""))
			throw new CariException(strMsgCheckDtCh, -2);

/* BOFFA: non abbiamo ass_progetto da rimuovere 
 		if (tipoOp.equals("01")) {
			// Chiusure entit� che stanno sotto il progetto:
			// Piani assistenziali
			// Piani accessi
			// Obiettivi, Interventi e verifiche
			// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
			clCcec.chiudoDaProgettoAssSocialeInGiu(mydbc, n_cartella, n_contatto, data_chiusura, strCodOperChiusura);
		} else {
			// Chiusure entit� che stanno sotto il contatto:
			// Piani assistenziali
			// Piani accessi
			// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
			clCcec.chiudoDaContattoInGiu(mydbc, n_cartella, n_contatto, data_chiusura, tipoOp, strCodOperChiusura);
		}
 */
		
		clCcec.chiudoDaContattoInGiu(mydbc, n_cartella, n_contatto, data_chiusura, tipoOp, strCodOperChiusura);
		//gb 31/10/07. fine *******

		//		Hashtable hCampi = (Hashtable) hOperatore.get(tipoOp);
		Hashtable<String, String> hCampi = (Hashtable<String, String>) recuperaInfTabellaOperatore(tipoOp);

		String selContatto = "SELECT * FROM " + hCampi.get("tabella") + " WHERE n_cartella = " + n_cartella +
		//gb 31/10/07                                     " AND n_contatto = " + n_contatto;
				" AND " + hCampi.get("n_contatto") + " = " + n_contatto; //gb 31/10/07
		ISASRecord dbrCont = mydbc.readRecord(selContatto);
		dbrCont.put("" + hCampi.get("data_chiusura"), data_chiusura);
		dbrCont.put("" + hCampi.get("motivo"), motivo);
		gestisciMotivoChiusuraTesto(dbrCont, hCampi, motivo, motivoChiusuraTabVoci);
		mydbc.writeRecord(dbrCont);

		// 11/09/12: x gestione chiusura CASO 
		gestCasoAndConcl(mydbc, n_cartella, data_chiusura, motivo, (String) par.get("cod_oper_profile"), tipoOp);

		return dbrCont;
	}

	private void gestisciMotivoChiusuraTesto(ISASRecord dbrCont, Hashtable<String, String> hCampi, String motivo,
			Hashtable<String, String> motivoChiusuraTabVoci) throws ISASMisuseException {
		String punto = ver + "gestisciMotivoChiusuraTesto ";
		String campoTxt = ISASUtil.getValoreStringa(hCampi, CTS_MOTIVO_TESTO );
		LOG.trace(punto + " motivo>"+motivo+"< campoTxt>"+campoTxt+"<");
		if (ISASUtil.valida(motivo) &&ISASUtil.valida(campoTxt)){
			String descMotivo = ISASUtil.getValoreStringa(motivoChiusuraTabVoci, motivo);
			dbrCont.put(campoTxt, descMotivo);
		}else {
			LOG.trace(punto + " non aggiorno il motivo in quanto >>motivo>"+motivo+"< campoTxt>"+campoTxt+"<");
		}
	}

	//gb 31/10/07 *******

	// 11/09/12
	private int gestCasoAndConcl(ISASConnection dbc, String n_cartella, String data_chiusura, String motivo,
			String oper, String tipoOp) throws Exception {
		int risu = 0;
		try {
			String pr_data = null;
			Integer idCaso = null;
			Hashtable h_par = new Hashtable();

			ISASRecord progettoRif = getProgetto(dbc, n_cartella, data_chiusura);
			if (progettoRif != null)
				pr_data = progettoRif.get("pr_data").toString();

			if (pr_data != null)
				h_par.put("pr_data", pr_data);
			else {
				LOG.debug("*** ChiudiContattoEJB.gestCasoAndConcl - cart=[" + n_cartella
						+ "]: non e' stato trovato un PROGETTO attivo" + "\n alla data " + data_chiusura
						+ ", quindi NON provo a comunicare CONCLUSIONE ***");
				return -1;
			}

			h_par.put("n_cartella", n_cartella);
			h_par.put("operZonaConf", oper);
			h_par.put("dtRif", data_chiusura);

			ISASRecord casoRif = (ISASRecord) gestore_casi.getCasoAttivoAllaData(dbc, h_par);
			if (casoRif != null)
				idCaso = (Integer) casoRif.get("id_caso");

			if (idCaso != null)
				h_par.put("id_caso", idCaso);
			else {
				LOG.debug("*** ChiudiContattoEJB.gestCasoAndConcl - cart=[" + n_cartella
						+ "]: non e' stato trovato un CASO attivo" + "\n alla data " + data_chiusura
						+ ", quindi NON provo a comunicare CONCLUSIONE ***");
				return -1;
			}

			boolean unico = false;
			int orig = GestCasi.CASO_SAN;
			if (tipoOp.equals("01")) { // controllo  i soli contatti SOC
				unico = gestore_casi.query_checkUnicoContAperto(dbc, h_par, true, false, true);
				orig = GestCasi.CASO_SOC;
			} else
				// controllo i soli contatti SAN
				unico = gestore_casi.query_checkUnicoContAperto(dbc, h_par, true, true);

			if (idCaso.intValue() != -1 && unico) {
				if (((Integer) casoRif.get("origine")).intValue() == orig) {
					Hashtable hCaso = new Hashtable();
					hCaso.put("n_cartella", h_par.get("n_cartella"));
					hCaso.put("pr_data", h_par.get("pr_data"));
					hCaso.put("id_caso", idCaso);
					hCaso.put("dt_conclusione", data_chiusura);
					String motChiuFlux = evUtl.getTabVociCodReg(dbc, "ICHIUS", motivo, "99");
					hCaso.put("motivo", motChiuFlux);
					hCaso.put("operZonaConf", h_par.get("operZonaConf")); // 15/10/09
					Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
					if (r != null)
						risu = r.intValue();
				}
			}

			if (risu <= 0)
				LOG.debug("::: ChiudiContattoEJB.gestCasoAndConcl - NON segnalata CONCLUSIONE! :::");
			// LOG.debug("::: ChiudiContattoEJB.gestCasoAndConcl: segnalata CONCLUSIONE - risu=["+risu+"] :::");				
			return risu;
		} catch (Exception e) {
			LOG.debug("ChiudiContattoEJB.gestCasoAndConcl: ERRORE - e=" + e);
			throw e;
		}
	}

	// 11/09/12
	private ISASRecord getProgetto(ISASConnection dbc, String cartella, String dataRif) throws Exception {
		ISASRecord rec = null;

		try {
			String sel = " SELECT * FROM progetto WHERE n_cartella = " + cartella + " AND pr_data <= "
					+ dbc.formatDbDate(dataRif) + " AND (pr_data_chiusura IS NULL " + " OR pr_data_chiusura >= "
					+ dbc.formatDbDate(dataRif) + ")";

			rec = dbc.readRecord(sel);

			return rec;
		} catch (Exception e) {
			LOG.error("ChiudiContattoEJB.getProgetto: ERRORE - e=" + e);
			throw e;
		}
	}

	// ================================= Utilita' ============================== //

	public String currentTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(" HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(cal.getTime());
	}

}

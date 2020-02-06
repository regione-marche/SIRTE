package it.caribel.app.sinssnt.bean.modificati;

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMDiarioEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.ScaleVal;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.casi_adrsa.GestSegnalazione;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
// 15/07/09
// 07/08/07
//gb 01/10/07

public class SkFisioEJB extends SINSSNTConnectionEJB {
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	private EveUtils eveUtl = new EveUtils();
	
	// 10/09/07 m.: aggiunto "catch" e "throw" dell'eccezione ISASPermissionDenied
	// anche sui metodi "getContattoFisCorrente()", "queryKey()".
	// Nel metodo "query()": modificato "SELECT campo1, campo2,.." in "SELECT *"
	// perche' vengano eseguiti i ctrl ISAS.

	// 06/04/2007 bargi aggiunto data chiusura del progetto piano assistenziale

	// 06/12/06 m.: sostituito campi di SKPATOLOGIE con quelli della nuova tabella DIAGNOSI.
	// 31/10/06 m.: aggiunto ONCOLOGO nel metodo "deleteContsan()".
	private String ver ="1-";
	private static final String CONSTANTS_TIPO_DEFI = "TIPDEF";
	private static final String CONSTANTS_IMPORTO_TICKET_ESENTE_TOTALE_KEY ="SPR_TCK_TOT";
	private static final String CONSTANTS_IMPORTO_TICKET_QUOTA_RICETTA_KEY ="SPR_TCK_RIC";
	private static final String CONSTANTS_IMPORTO_TICKET_PAGANTE_KEY ="SPR_TCK_PAG";
	
	private static final String CONSTANTS_FLAG_POS_TICKET_COD_ESENTE_TOTALE_KEY="SPR_TCK_ESTOT";
	private static final String CONSTANTS_FLAG_POS_TICKET_COD_QUOTA_RICETTA_KEY="SPR_TCK_QRIC";
	private static final String CONSTANTS_FLAG_POS_QUOTA_COD_PAGANTE_KEY = "SPR_TCK_QTPAG";
	private static final String TICKET_TIPOLOGIA_COD_NON_ESENTE_KEY = "SPR_TCK_NOEST";
	
	// 10/09/07
	//private String msgNoD = "Mancano i diritti per leggere il record";

	// 05/09 Elisa Croci
	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();
	// 15/07/09
	private ScaleVal gest_scaleVal = new ScaleVal();
	private boolean mydebug = true;      

	private static final String CONSTANTS_ABL_FLUSSI_SPR ="ABL_GST_SPR";
	// TODO inserire il codice del tipo di fisioterapista
	private static final String CONSTANTS_TIPO_OPERATORE_FISIOTERAPISTA = "04";
	private static final String MIONOME = "22-SkFisioEJB.";
//	cod_regime, cod_accesso, numero_proroga,
	private static final String CONSTANTS_COD_REGIME = "3"; // codice regime 3 domiciliare
	private static final String CONSTANTS_COD_ACCESSO = "1"; // codice accesso 1 validazione
	private static final String CONSTANTS_NUMERO_PROROGA = "0"; // numero proroga 0
	
	private static final String CONSTANTS_FLAG_INVIATO_INSERIMENTO = "0"; // il record viene inserito
	private static final String CONSTANTS_FLAG_INVIATO_INVIATO = "1"; // il record � stato modificato
	private static final String CONSTANTS_FLAG_INVIATO_VARIATO = "2"; // il record viene modificato
	
	public SkFisioEJB() {
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException, CariException {
		// 10/09/07
		boolean done = false;
		ISASConnection dbc = null;
		String n_cartella = null;
		String n_contatto = null;
		ISASCursor dbcur = null;// 06/12/06 m.
		String dtAssistitoChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);

		try {
			n_cartella = "" + h.get("n_cartella");
			n_contatto = "" + h.get("n_contatto");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkFisio queryKey: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			String myselect = " Select s.* from skfis s where s.n_cartella = " + n_cartella;
			if (ISASUtil.getValoreIntero(n_contatto)>0){
				myselect += " and s.n_contatto=" + n_contatto;
			}
			if (ManagerDate.validaData(dtAssistitoChiusura)){
				myselect += " and s.n_contatto in ( select max(x.n_contatto) from skfis x where x.n_cartella = s.n_cartella ) ";
			}

			LOG.info("SkFisio_queryKey " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				dbr = verificaEAggiornareIdSkso(dbc, dbr, myselect);
				dbr.put("des_operatore",
						decodifica("operatori", "codice", dbr.get("skf_operatore"), "nvl(cognome,'') || nvl(nome,'')", dbc));
				dbr
						.put("des_operat_refe", decodifica("operatori", "codice", dbr.get("skf_fisiot"), "nvl(cognome,'') || nvl(nome,'')",
								dbc));

				// 22/04/11: xRME --
				dbr.put("desc_ospdim", decodifica("ospedali", "codosp", dbr.get("skf_osp_dim"), "descosp", dbc));
				dbr.put("desc_rep", decodifica("reparti", "cd_rep", dbr.get("skf_uo_dim"), "reparto", dbc));
				// 22/04/11: xRME --  

				/*** 10/09/07 m.: piano assistenziale scorporato dal contatto
				//06/04/2007 bargi ,fipa_data_chiusura 
				String myprest="SELECT fipa_data,fipa_progetto,fipa_data_chiusura FROM fisprogass WHERE "+
				              "n_cartella="+h.get("n_cartella")+" and "+
				  "n_contatto="+h.get("n_contatto")+
				              " ORDER BY fipa_data";;
				ISASCursor cur_ass=dbc.startCursor(myprest);
				Vector dbass=cur_ass.getAllRecord();
				dbr.put("griglia_ass",dbass);
				cur_ass.close();
				 ***/

				// 06/12/06 m.
				leggiDiagnosi(dbc, dbr);

				// 20/05/09 Elisa Croci
				if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
					// 15/06/09 Elisa Croci ********************************************************
					if (h.containsKey("ubicazione") && h.get("ubicazione") != null)
						dbr.put("ubicazione", h.get("ubicazione"));
					if (h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
						dbr.put("update_segnalazione", h.get("update_segnalazione"));
					// *********************************************************************************

					int caso = prendi_dati_caso(dbc, dbr);
					LOG.info("CASO DEL CONTATTO CORRENTE: " + caso);
					if (prendi_segnalazione(dbc, caso, dbr))
						prendi_presacarico(dbc, caso, dbr);
				}
				recuperaDescrizioni(dbc, dbr);
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch(ISASPermissionDeniedException e)
		{		
			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					LOG.error(e1);
				}
			}
		}
	}

	private ISASRecord verificaEAggiornareIdSkso(ISASConnection dbc, ISASRecord dbr, String query){
		String punto = ver + "verificaAggiornareIdSkso ";
		String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
		if (ISASUtil.valida(idSkso)) {
			LOG.trace(punto + " idskso e' valorizzato non modifico ");
		} else {
			LOG.trace(punto + " idskso non e' valorizzato, provo a recuperarlo ");
			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
			String nCartella = ISASUtil.getValoreStringa(dbr, Costanti.N_CARTELLA);
			String skiDataApertura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.SKF_DATA);
			
			ISASRecord dbrRmSkSoMMG;
			try {
				dbrRmSkSoMMG = rmSkSOEJB.recuperaRmSksoMmg(dbc, nCartella, skiDataApertura);
				idSkso = ISASUtil.getValoreStringa(dbrRmSkSoMMG, CostantiSinssntW.CTS_ID_SKSO);
				if (ISASUtil.valida(idSkso)){
					dbr.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
					dbc.writeRecord(dbr);
					LOG.trace(punto + " query per rileggere il record aggiornato \n:"+query);
					dbr = dbc.readRecord(query);
				}
			} catch (ISASMisuseException e) {
				LOG.error(punto + " Errore mancano diritti per leggere le info ", e);				
			} catch (ISASPermissionDeniedException e) {
				LOG.error(punto + " Errore mancano i permesssi per leggere le info ", e);
			} catch (DBMisuseException e) {
				LOG.error(punto + " Errore mancano per leggere le info ", e);
			} catch (DBSQLException e) {
				LOG.error(punto + " Errore nelle query ", e);
			} catch (Exception e) {
				LOG.error(punto + " Errore generico ", e);
			}
		}
		return dbr;
	}
	
	
	// 10/09/07
	public ISASRecord getContattoFisCorrente(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException,
			CariException {
		String punto = MIONOME + "getContattoFisCorrente ";
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null; // 07/08/07
		
		String n_cartella = (String) h.get("n_cartella");
		//String strDtSkVal = (String) h.get("pr_data");// 07/08/07
		stampa(punto + " Inizio con dati>"+ h+"<");
		try {
//			if (strDtSkVal == null) {
//				LOG.info("\nSkFisioEJB -->> getContattoFisCorrente: dataSkVal NULLA!!");
//				done = true;
//				return dbr;
//			}
			
			// Ottengo la connessione al database
			dbc = super.logIn(mylogin);

			// Preparo la SELECT del record
			String myselect = "SELECT * FROM skfis" + " WHERE n_cartella = " + n_cartella + " AND skf_data_chiusura IS NULL";

			LOG.info("SkFisioEJB/getContattoFisCorrente: " + myselect);
			dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				dbr.put("des_operatore",
						decodifica("operatori", "codice", dbr.get("skf_operatore"), "nvl(cognome,'') || nvl(nome,'')", dbc));
				dbr
						.put("des_operat_refe", decodifica("operatori", "codice", dbr.get("skf_fisiot"), "nvl(cognome,'') || nvl(nome,'')",
								dbc));

				// 22/04/11: xRME --
				dbr.put("desc_ospdim", decodifica("ospedali", "codosp", dbr.get("skf_osp_dim"), "descosp", dbc));
				dbr.put("desc_rep", decodifica("reparti", "cd_rep", dbr.get("skf_uo_dim"), "reparto", dbc));
				// 22/04/11: xRME --

				// 07/12/06 m.
				leggiDiagnosi(dbc, dbr);

//				dbr.put("pr_data", strDtSkVal);// 07/08/07

				// 05/09 Elisa Croci
//				if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
//					// 15/06/09 Elisa Croci ********************************************************
//					if (h.containsKey("ubicazione") && h.get("ubicazione") != null)
//						dbr.put("ubicazione", h.get("ubicazione"));
//					if (h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
//						dbr.put("update_segnalazione", h.get("update_segnalazione"));
//					// *********************************************************************************
//
//					int caso = prendi_dati_caso(dbc, dbr);
//					LOG.info("CASO DEL CONTATTO CORRENTE: " + caso);
//					if (prendi_segnalazione(dbc, caso, dbr))
//						prendi_presacarico(dbc, caso, dbr);
//				}
				recuperaDescrizioni(dbc, dbr);
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e1) {
			LOG.error("SkFisioEJB.getContattoFisCorrente(): " + e1);
			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkFisioEJB.getContattoFisCorrente(): " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	// 10/09/07: caricamento della grid della frame "JFrameGridSkFis"
	public Vector query_loadGridSkFis(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException {
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String) h.get("pr_data");// 26/10/06
		Vector vdbr = new Vector();
		boolean contAperti = false;
		try {
			dbc = super.logIn(mylogin);
			// 26/10/06 ---
			if (strDtSkVal == null) {
				LOG.info("\nSkFisioEJB -->> query_loadGridSkFis: dataSkVal NULLA!!");
				//cerco il progetto
				ISASRecord progetto = getProgetto(dbc, strNAssistito, (String)h.get("skf_data"));
				if (progetto!=null)
					strDtSkVal = progetto.get("pr_data").toString();
				else{
				done = true;
				return vdbr;
				}
			}
			// 26/10/06 ---

			// Connessione al database

			contAperti = ((h.get("contAperti") != null) && (((String)h.get("contAperti")).trim().equals("S")));
			String critDtChius = " AND skf.skf_data_chiusura IS" + (contAperti?"":" NOT") + " NULL";
			if (contAperti)
				critDtChius += " AND skf.skf_data NOT IN (SELECT MAX(a.skf_data) FROM skfis a" +
				" WHERE a.n_cartella = skf.n_cartella" +
				" AND a.skf_data_chiusura IS NULL)";
			// Compongo la SELECT
			String myselect = "SELECT skf.* FROM skfis skf" +
//					", progetto_cont pc"+ // 26/10/06
					" WHERE skf.n_cartella = " + strNAssistito +
					critDtChius+
					// 26/10/06 : x estrarre solo quelli collegati ad una scheda valutaz
//					" AND pc.prc_tipo_op = '04'" + " AND pc.n_cartella = skf.n_cartella" + " AND pc.pr_data = "
//					+ formatDate(dbc, strDtSkVal) + " AND pc.prc_n_contatto = skf.n_contatto" +
					// 26/10/06 --------------------------------------------------------
					" ORDER BY skf.skf_data, skf.skf_data_chiusura";

			LOG.info("-->>query GridSkFis: " + myselect);

			// Leggo i record
			ISASCursor dbcur = dbc.startCursor(myselect);

			// Metto i record letti in un vector (un vector di ISASRecord).
			vdbr = dbcur.getAllRecord();

			// Decodifica dei Cognomi e Nomi degli operatori in tutti gli ISASRecord del Vector
			decodificaQueryInfo(dbc, vdbr);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la query_loadGridSkFis()  ",e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					LOG.error(e1);
				}
			}
		}
	}

	// 10/09/07
	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr) throws Exception {
		for (int i = 0; i < vdbr.size(); i++) {
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			decodificaQueryOperatore(mydbc, dbr, "skf_operatore", "operatore_apertura");
			decodificaQueryOperatore(mydbc, dbr, "skf_fisiot", "operatore_referente");
		}
	}

	// 10/09/07
	private void decodificaQueryOperatore(ISASConnection mydbc, ISASRecord dbr, String dbFldNameCod, String dbName) throws Exception {
		String strCodOperatore = (String) dbr.get(dbFldNameCod);

		String strCognome = "";
		String strNome = "";

		if (strCodOperatore == null) {
			dbr.put(dbName, "");
			return;
		}
		String selS = "SELECT cognome, nome FROM operatori" + " WHERE codice = '" + strCodOperatore + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null) {
			if (rec.get("cognome") != null)
				strCognome = (String) rec.get("cognome");
			if (rec.get("nome") != null)
				strNome = (String) rec.get("nome");
		}
		dbr.put(dbName, strCognome + " " + strNome);
	}

	/*
	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
	    dbc=super.logIn(mylogin);
	    String myselect="Select * from skfis where n_cartella= "+
			(String)h.get("n_cartella")+" ORDER BY skf_data DESC ";
	        ISASCursor dbcur=dbc.startCursor(myselect);
	    LOG.info("SkFisio_query "+myselect);
	    Vector vdbr=dbcur.getAllRecord();
	    dbcur.close();
		dbc.close();
	    super.close(dbc);
	    done=true;
		return vdbr;
	}catch(Exception e){
		LOG.info(e);
		throw new SQLException("Errore eseguendo una query()  ");
	}finally{
	    if(!done){
	        try{
		dbc.close();
	super.close(dbc);
	}catch(Exception e1){LOG.error(e1);}
	        }
	}

	}
	 */
	//
	// Query di controllo per verificare se ci sono ancora schede di
	// Regfis aperte, prima di immettere una nuova scheda
	//
	/*commento perch� credo che non venga chiamata da nessuno
	public Vector queryStillOpen(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
	    dbc=super.logIn(mylogin);
	    String codice=(String)h.get("n_cartella");
	    String myselect="Select * from skfis where n_cartella= "+
	        (String)h.get("n_cartella")+
	        " and (skf_data_chiusura is null or skf_data_chiusura <'1900-01-01')"+
	        " ORDER BY skf_data DESC ";
	        ISASCursor dbcur=dbc.startCursor(myselect);
	    Vector vdbr=dbcur.getAllRecord();
	    dbcur.close();
	    dbc.close();
	    super.close(dbc);
	    done=true;
	        return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryStillOpen()  ");
	}finally{
	    if(!done){
	        try{
		dbc.close();
	super.close(dbc);
	}catch(Exception e1){LOG.error(e1);}
	        }
	}

	}

	 */

	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
			CariException // 10/09/07
	{
		String punto = MIONOME + "insert ";
		ISASConnection dbc = null;
		ISASCursor dbcur = null;// 06/12/06 m.
		boolean done = false;


		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			ISASRecord dbr = insertTransactional(dbc, h);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				LOG.error("SkFisioEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new CariException("Errore eseguendo la rollback() - " + e1);
			}

			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("SkFisioEJB-->Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("SkFisioEJB-->Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (Exception e1) {
			LOG.error(e1);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e) {
				throw new ISASPermissionDeniedException("SkFisioEJB-->Errore eseguendo una rollback() - " + e);
			}
			throw new SQLException("Errore eseguendo una insert() - ", e1);
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	/**
	 * Permette di eseguire la insert rimanendo all'interno di una transazione
	 * 
	 * @param dbc	la connessione sulla quale effettuare la insert
	 * @param h		l'hashtabel con i parametri di insert
	 * @return		l'ISASRecord inserito
	 */
	public ISASRecord insertTransactional(ISASConnection dbc, Hashtable h) throws SQLException,
			Exception, CariException, ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException, DBRecordChangedException {
		String n_cartella = null;
		String n_contatto = null;
		String data_apertura = null;
		try {
			n_cartella = h.get("n_cartella").toString();
			n_contatto = h.get("n_contatto").toString();
			data_apertura = h.get("skf_data").toString();
		} catch (Exception e) {
			throw new SQLException("Errore: manca la chiave primaria", e);
		}
		
		String strDtSkVal = (String) h.get("pr_data");// 10/09/07
		// 10/09/07 *************************
		if (dtApeContLEMaxDtContChius(dbc, h)) {
			String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
			throw new CariException(msg, -2);
		}
		// ************************************

		// 10/09/07: si ottiene il nuovo progressivo (non si usa pi� CONTSAN).
		int intProgressivo = getProgressivo(dbc, n_cartella);
		Integer iProgressivo = new Integer(intProgressivo);
		n_contatto = iProgressivo.toString();
		// ************************************

		ISASRecord dbr = dbc.newRecord("skfis");
		Enumeration n = h.keys();
		while (n.hasMoreElements()) {
			String e = (String) n.nextElement();
			dbr.put(e, h.get(e));
		}

		// 10/09/07: Si setta il campo 'n_contatto' col nuovo progressivo
		dbr.put("n_contatto", iProgressivo);
		dbr.put("flag_inviato", CONSTANTS_FLAG_INVIATO_INSERIMENTO);
//		aggiornaSkinfValoriCostantiSinssntW(dbr);
		dbc.writeRecord(dbr);
		
		ISASUtil.checkUnicoContattoAperto(dbc, h, n_contatto, "skfis", "skf_data_chiusura", Labels.getLabel("contatti.msg.contattoUnicoApertoViolato"));
		
		// Simone 25/11/14 Aggiornamento id_skso su rm_diario
		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//			if (h.get("id_skso")!=null&&!h.get("id_skso").toString().trim().equals("")){
		if(ISASUtil.valida(idSkso)){
			Hashtable h_rm_diario = (Hashtable)h.clone();
			h_rm_diario.put("tipo_operatore", CostantiSinssntW.TIPO_OPERATORE_FISIOTERAPISTA);
			h_rm_diario.put("id_skso", idSkso);
			try {
				Boolean id_skso_updated = RMDiarioEJB.updateIdSkso(dbc, h_rm_diario);
				LOG.debug("Esito aggiornamento id_skso su diario = "+id_skso_updated.booleanValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Hashtable dati = (Hashtable)h.clone();
			dati.put(CostantiSinssntW.CTS_OP_INSERIRE_PV, CostantiSinssntW.CTS_SI);
			dati.put(CostantiSinssntW.CTS_COD_OP_CORRENTE, dbc.getKuser());
			try {
				RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
				rmSkSOOpCoinvoltiEJB.inserisciOperatoriFigure(dati,dbc, n_cartella, idSkso);
				LOG.debug("Operatore inserito ");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		// 10/09/07: scrittura su PROGETTO_CONT ed, eventualmente, PROGETTO -----
		if (strDtSkVal == null) {
			ISASRecord progetto = getProgetto(dbc, n_cartella, dbr.get("skf_data").toString());
			if (progetto == null)			
				strDtSkVal=scriviProgetto(dbc, n_cartella, strDtSkVal);
			else
			strDtSkVal = progetto.get("pr_data").toString();
			// gb 02/11/06
			// Mettere controllo che data_ape sk_valutaz. fittizia sia >=
			// data chiusura di ultima sk_valutaz. chiusa pre-esistente.
			if (dtApeMinoreMaxDtChius(dbc, n_cartella, strDtSkVal)) {
				String msg = "Attenzione: Data apertura antecedente a data chiusura di ultima Scheda valutazione chiusa!";
				throw new CariException(msg, -2);
			}
			// gb 01/06/07: Controllo che la data di apertura del contatto (skf_data)
			// sia >= data_apetura della tab. cartella.
			if (dtApeContattoLTDtApeCartella(dbc, n_cartella, strDtSkVal)) {
				String msg = "Attenzione: Data apertura contatto e' antecedente alla data apertura dell'assistito!";
				throw new CariException(msg, -2);
			}
			
		}

//			scriviProgettoCont(dbc, n_cartella, strDtSkVal, "04", n_contatto);
		//scriviProgettoCont(dbc, n_cartella, strDtSkVal, CONSTANTS_TIPO_OPERATORE_FISIOTERAPISTA, n_contatto);
		// 10/09/07 -------------------------------------------------------------

		String myselect = "SELECT * FROM skfis WHERE n_cartella=" + n_cartella + " AND n_contatto=" + n_contatto;
		dbr = dbc.readRecord(myselect);

		String data_chiusura = "";
		if (dbr.get("skf_data_chiusura") != null)
			data_chiusura = ((java.sql.Date) dbr.get("skf_data_chiusura")).toString();

		// inserimento del fisioterapista referente
		String skf_data = (String) h.get("skf_data");
		String opref = (String) h.get("skf_fisiot");
		String data_ref = (String) h.get("skf_fisiot_da");
		insertOpRef(dbc, opref, data_ref, n_cartella, n_contatto);

		/** 10/09/07: eliminato tabella CONTSAN
		//aggiornamento di contsan
		String descr_fis=(String)h.get("skf_descr_contatto");
		String data_chiusura="";
		if (h.get("skf_data_chiusura")!=null)
		data_chiusura=(String)h.get("skf_data_chiusura");
		String operatore=(String)dbr.get("skf_operatore");
		String selcont="SELECT * FROM contsan where n_cartella="+n_cartella+
		           " AND n_contatto="+n_contatto;
		ISASRecord dbcon=dbc.readRecord(selcont);
		if(dbcon!=null)
		this.updateContsan(dbc,skf_data,data_chiusura,descr_fis,n_cartella,n_contatto);
		else
		this.insertContsan(dbc,skf_data,data_chiusura,descr_fis,n_cartella,operatore);
		 **/

		if (data_chiusura != null && !(data_chiusura.equals(""))) {
			// bargi 16/04/2007
			// chiudo piani assistenziali
			/*** 13/09/07: piani assistenziali gestibili solo dopo inserimento contatto
			AggiornaData("fisprogass",n_cartella,n_contatto,"fipa_data_chiusura",data_chiusura,"fipa_data",dbc);

			//rimuovo da agenda appuntamenti caricati per la cartella chiusa
			rimuovoAgendaCaricata(n_cartella,n_contatto,data_chiusura,dbc);	
			 ***/
		}

		// 22/11/06 m.
		leggiDiagnosi(dbc, dbr);

		// 10/09/07
		dbr.put("pr_data", strDtSkVal);

		// 22/04/11: xRME --
		dbr.put("desc_ospdim", decodifica("ospedali", "codosp", dbr.get("skf_osp_dim"), "descosp", dbc));
		dbr.put("desc_rep", decodifica("reparti", "cd_rep", dbr.get("skf_uo_dim"), "reparto", dbc));
		// 22/04/11: xRME --

		// 15/07/09 m. ------------------
		// lettura dtConclusione CASO precedente
		h.put("pr_data", strDtSkVal);
		String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
		String tempoT = (String) h.get("tempo_t");

		// letture scale max
		gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, strDtSkVal, "", dtChiusCasoPrec, "", tempoT, "04");
		dbr.put("tempo_t", tempoT);
		// 15/07/09 m. ------------------

		int idCaso = -1;
		// 21/05/09 Elisa Croci
		if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
			idCaso = prendi_dati_caso(dbc, dbr);
			gestione_segnalazione(dbc, dbr, h, "insert");

			// 15/06/09 ***************************************************************************
			printError("insert() -- Fatta gestione segnalazione per il caso: " + idCaso);

		} else {
			h.put("dt_segnalazione", data_apertura);
			h.put("dt_presa_carico", data_apertura);
			idCaso = gestore_casi.apriCasoSan(dbc, h).intValue();
		}
		if (data_chiusura != null && !data_chiusura.equals("")) {
			printError("insert() -- Controllo contatto UNICO SANITARIO H == " + h.toString());
			boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true, true);
			if (idCaso != -1 && unico) {
				printError("insert() -- Gestisco la chiusura del caso");
				// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
				// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
				// sociale associato!
				int origine = -1;
				if (dbr.get("origine") != null && !(dbr.get("origine").toString()).equals(""))
					origine = Integer.parseInt(dbr.get("origine").toString());
				else if (h.get("origine") != null && !(h.get("origine").toString()).equals(""))
					origine = Integer.parseInt(h.get("origine").toString());
				if (origine != -1) {
					printError("insert() -- Origine del caso: " + origine);
					if (origine == GestCasi.CASO_SAN) {
						Hashtable hCaso = new Hashtable();
						hCaso.put("n_cartella", h.get("n_cartella"));
						hCaso.put("pr_data", h.get("pr_data"));
						hCaso.put("id_caso", new Integer(idCaso));
						hCaso.put("dt_conclusione", dbr.get("skf_data_chiusura"));
						// 26/03/10 hCaso.put("motivo", "99");
						// 26/03/10 ----
						String motChiu = (String) h.get("skf_motivo_chius");
						String motChiuFlux = getTabVociCodReg(dbc, "FCHIUS", motChiu);
						hCaso.put("motivo", motChiuFlux);
						// 26/03/10 ----
						hCaso.put("operZonaConf", (String) dbr.get("skf_operatore")); // 15/10/09
						printError(" insert() -- Chiudi caso = HashCaso: " + hCaso.toString());
						Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
						printError("insert() -- Ritorno di ChiudiCaso == " + r);
					}
				}
			}
		}
		// *****************************************************************************************

		// 15/06/09 Elisa Croci ******************************************************
		if (h.containsKey("ubicazione"))
			dbr.put("ubicazione", h.get("ubicazione"));
		if (h.containsKey("update_segnalazione"))
			dbr.put("update_segnalazione", h.get("update_segnalazione"));
		// ****************************************************************************

		try {
			dbr.put("des_operatore",
					decodifica("operatori", "codice", dbr.get("skf_operatore"), "nvl(cognome,'') || nvl(nome,'')", dbc));
			dbr
					.put("des_operat_refe", decodifica("operatori", "codice", dbr.get("skf_fisiot"), "nvl(cognome,'') || nvl(nome,'')",
							dbc));
		} catch (Exception e) {
			LOG.error(" Errore nel recuperare la decodifica operatori");
		}

		recuperaDescrizioni(dbc, dbr);
		LOG.info("SkFisioEJB: 2insert -- DBR restituito === " + dbr.getHashtable().toString());
		return dbr;
	}

//	private void aggiornaSkinfValoriCostantiSinssntW(ISASRecord dbr) {
//		String punto = MIONOME + "aggiornaValoriCostantiSinssntW ";
////		 vengono impostati i valori di default che hanno chiesto
//		try {
//			dbr.put("cod_regime", CONSTANTS_COD_REGIME);
//			dbr.put("cod_accesso", CONSTANTS_COD_ACCESSO);
//			dbr.put("numero_proroga", CONSTANTS_NUMERO_PROROGA);
//		} catch (ISASMisuseException e) {
//			stampa(punto + " Errore in inserimento dati di default ");
//			e.printStackTrace();
//		}
//	}
	public ISASRecord getProgetto(ISASConnection dbc, String cartella, String dataRif) throws Exception
	{
		ISASRecord rec = null;

		try {
			
			String sel = " SELECT * FROM progetto WHERE n_cartella = " + cartella
			+ ((dataRif != null)?" AND pr_data <= " + dbc.formatDbDate(dataRif):"")
			+ ((dataRif != null)?" AND (pr_data_chiusura IS NULL " + " OR pr_data_chiusura >= " + dbc.formatDbDate(dataRif) + ")":"");

			rec = dbc.readRecord(sel);

			return rec;
		} catch (Exception e) {
			LOG.error("SkFisioEJB.getProgetto: ERRORE - e=" + e);
			throw e;
		}
	}	
	private void recuperaDescrizioni(ISASConnection dbc, ISASRecord dbr) {
		String punto = MIONOME + "recuperaDescrizioni ";
		stampa(punto + " recupero descrizioni ");
		String codMedico = ISASUtil.getValoreStringa(dbr, "mecodi");
		String codPresidio = ISASUtil.getValoreStringa(dbr, "cod_presidio");
		String codEsenzione = ISASUtil.getValoreStringa(dbr, "ticket_cod_esenzione");
		String codPrestazione = ISASUtil.getValoreStringa(dbr, "cod_prestaz");
		// mecodi_des
		// despres
		// cod_prestaz_des
		
		// 05/02/13
		String codPresidioCont = ISASUtil.getValoreStringa(dbr, "skf_cod_presidio");

		try {
			stampa(punto + " codmedico>"+codMedico+"<");
			String cognomeNomeMedico = decodifica("medici", "mecodi", codMedico, "nvl(trim(mecogn)||' ','') || nvl(menome,'')", dbc);
			dbr.put("mecodi_des", cognomeNomeMedico);
			String descrizionePresidio = recuperaDescrizioniPresidio(dbc, codPresidio);
			dbr.put("despres", descrizionePresidio);
			// 05/02/13 ---
			String descrizionePresidioCont = recuperaDescrizioniPresidio(dbc, codPresidioCont);
			dbr.put("desc_presidio", descrizionePresidioCont);
			// 05/02/13 ---
			String descrPrestazione = recuperaDescrizioniPrestazione(dbc, codPrestazione);
			dbr.put("cod_prestaz_des", descrPrestazione);
			String descrEsenzione= recuperaDescrizioniEsenzioni(dbc, codEsenzione);
			dbr.put("ticket_cod_esenzione_des", descrEsenzione);
		} catch (Exception e) {
			stampa(punto + " Errore nel recuperare informazioni ");
		}
	}

	private String recuperaDescrizioniEsenzioni(ISASConnection dbc, String codEsenzione) {
		String punto = MIONOME + "recuperaDescrizioniPrestazione ";
		String descrizioneEsenzioni= "";
		String query = "select * from esenzioni where cod_esenzione = '" +codEsenzione+ "' ";
		stampa(punto + "query>"+query);
		try {
			ISASRecord dbrEsenzioni = dbc.readRecord(query);
			descrizioneEsenzioni = ISASUtil.getValoreStringa(dbrEsenzioni, "descrizione");
		} catch (Exception e) {
			stampa(punto + " Errore nel recuperare le prestazioni ");
			e.printStackTrace();
		}
		return descrizioneEsenzioni;
	}

	private String recuperaDescrizioniPrestazione(ISASConnection dbc, String codPrestazione) {
		String punto = MIONOME + "recuperaDescrizioniPrestazione ";
		String descrizionePrestazione = "";
		String tipoPrestazioni = tipoPrestazioneOperatore(dbc, CONSTANTS_TIPO_OPERATORE_FISIOTERAPISTA);
		String query = "Select * from prestaz  where prest_tipo = '" + tipoPrestazioni + "' and prest_cod = '" + codPrestazione
				+ "' ";
		stampa(punto + "query>"+query);
		try {
			ISASRecord dbrPrestaz = dbc.readRecord(query);
			descrizionePrestazione = ISASUtil.getValoreStringa(dbrPrestaz, "prest_des");
		} catch (Exception e) {
			stampa(punto + " Errore nel recuperare le prestazioni ");
			e.printStackTrace();
		}
		
		return descrizionePrestazione;
	}

	private String tipoPrestazioneOperatore(ISASConnection dbc, String tipoOperatore) {
		String punto = "tipoPrestazioneOperatore ";
		String tipoPrestazOperatore = "";
		try {
			String query = "select * from conf where conf_key = '" + CONSTANTS_TIPO_DEFI + tipoOperatore + "' and conf_kproc = 'SINS'";
			stampa(punto + " query>" + query);
			ISASRecord dbr = dbc.readRecord(query);
			tipoPrestazOperatore = ISASUtil.getValoreStringa(dbr, "conf_txt");

		} catch (Exception e1) {
			LOG.error(punto + " - Eccezione=[" + e1 + "]");
		}
		return tipoPrestazOperatore;
	}
	
	public Hashtable getImportoTicketSPR(myLogin mylogin, Hashtable h){
		String punto = MIONOME + "getImportoTicketSPR ";
		boolean done = false;
		ISASConnection dbc = null;
		Hashtable datiInvio = new Hashtable();
		stampa(punto + " inizio ");
		ISASCursor dbrConf = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "select * from conf where conf_kproc = 'SINS' AND CONF_KEY IN ('" +
			CONSTANTS_IMPORTO_TICKET_ESENTE_TOTALE_KEY+ "', '" +
			CONSTANTS_IMPORTO_TICKET_PAGANTE_KEY+"', '" +	
			CONSTANTS_FLAG_POS_TICKET_COD_ESENTE_TOTALE_KEY +"', '"+
			 CONSTANTS_FLAG_POS_TICKET_COD_QUOTA_RICETTA_KEY+"', '"+
			CONSTANTS_FLAG_POS_QUOTA_COD_PAGANTE_KEY+ "', '"+
			TICKET_TIPOLOGIA_COD_NON_ESENTE_KEY+ "', '"+			
			CONSTANTS_IMPORTO_TICKET_QUOTA_RICETTA_KEY+"')" ;
			stampa(punto + " query>"+myselect);
			dbrConf = dbc.startCursor(myselect);
			String key = "";String valore = "";
			while (dbrConf.next()) {
				ISASRecord dbr = dbrConf.getRecord();
//				conf_key, conf_txt
				key= ISASUtil.getValoreStringa(dbr, "conf_key");
				valore = ISASUtil.getValoreStringa(dbr, "conf_txt");
				datiInvio.put(key, valore);
			}
			dbrConf.close();
			
			dbc.close();	
			super.close(dbc);
			done = true;

		} catch(Exception e1){
			e1.printStackTrace();
			//return (Boolean)null;
			return null;
		} finally{
			if(!done){
				if(dbrConf!=null){
					try {
						dbrConf.close();
					} catch (Exception e) {
						stampa(punto + " Errore in chiusura corsore ");
						e.printStackTrace();
					}
				}
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
		return datiInvio;
	}

	
	
	
	
	
	private String recuperaDescrizioniPresidio(ISASConnection dbc, String codPresidio) {
		// TODO recuperare le informazioni su codice regionale e codice usl
		String punto = MIONOME + "recuperaDescrizioniPresidio ";
		String descrizionePresidio = "";
		Hashtable conf = new Hashtable();
		try {
			conf = getCodRegioneCodUsl(dbc);
			String strCodRegione = ISASUtil.getValoreStringa(conf, "codice_regione"); // codice regione.
			String strCodAzSan = ISASUtil.getValoreStringa(conf, "codice_usl"); // codice usl.

			String query = "select * from presidi where codreg = '" + strCodRegione + "' and codazsan = '" + strCodAzSan
					+ "' and codpres = '" + codPresidio + "' ";
			stampa(punto + " query>" + query);
			ISASRecord dbrPresidi = dbc.readRecord(query);
			descrizionePresidio = ISASUtil.getValoreStringa(dbrPresidi, "despres");

		} catch (Exception e) {
			stampa(punto + " Errore nel recuperare ");
			e.printStackTrace();
		}
		stampa(punto+ " descrizione pre>" +descrizionePresidio +"");
		return descrizionePresidio;
	}

	private Hashtable getCodRegioneCodUsl(ISASConnection dbc) throws Exception {
		String strCodRegione = "R";
		String strCodUsl = "U";
		Hashtable htCodRegCodUsl = new Hashtable();
		try {
			String mysel = "SELECT conf_txt FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND conf_key = 'codice_regione'";
			// LOG.info("ConvocaPuacEJB/getCodRegioneCodUsl/regione: " + mysel);
			ISASRecord dbConf = dbc.readRecord(mysel);
			if (dbConf != null && dbConf.get("conf_txt") != null)
				strCodRegione = (String) dbConf.get("conf_txt");

			mysel = "SELECT conf_txt FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND conf_key = 'codice_usl'";
			// LOG.info("ConvocaPuacEJB/getCodRegioneCodUsl/usl: " + mysel);
			dbConf = dbc.readRecord(mysel);
			if (dbConf != null && dbConf.get("conf_txt") != null)
				strCodUsl = (String) dbConf.get("conf_txt");

			htCodRegCodUsl.put("codice_regione", strCodRegione);
			htCodRegCodUsl.put("codice_usl", strCodUsl);
			return htCodRegCodUsl;
		} catch (Exception e) {
			throw new Exception("ConvocaPuacEJB: Errore eseguendo una getCodRegioneCodUsl() - " + e, e);
		}
	}

	// 10/09/07
	private boolean dtApeContattoLTDtApeCartella(ISASConnection dbc, String strNAssistito, String strDtApeCont) throws Exception {
		String mySel = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + strNAssistito + " AND data_apertura > "
				+ formatDate(dbc, strDtApeCont);

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile
		else
			return true;
	}

	// 10/09/07
	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, String strNAssistito, String strDtSkVal) throws Exception {
		String dt = strDtSkVal;
		// 25/06/07 dt = dt.substring(0,2) + dt.substring(3,5) + dt.substring(6,10);
		dt = dt.substring(8, 10) + dt.substring(5, 7) + dt.substring(0, 4);
		DataWI dataWIApertura = new DataWI(dt);

		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" + " FROM progetto" + " WHERE n_cartella = " + strNAssistito
				+ " AND pr_data_chiusura IS NOT NULL";

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile

		if ((java.sql.Date) rec.get("max_data_chius") == null)
			return false; // Ammissibile

		dt = ((java.sql.Date) rec.get("max_data_chius")).toString();
		if (dt.equals(""))
			return false; // Ammissibile

		dt = dt.substring(0, 4) + dt.substring(5, 7) + dt.substring(8, 10);
		String max_data_chiusura = dt;
		int rit = dataWIApertura.confrontaConDt(max_data_chiusura);
		// Codici ritornati da confrontaConDt:
		// se data_apertura � maggiore di data_chiusura restituisce 1
		// se data_apertura � minore di data_chiusura restituisce 2
		// se data_apertura � = di data_chiusura restituisce 0
		// se da errore -1
		if ((rit == 2) || (rit == 0))
			return true; // Non ammissibile
		else if (rit < 0) {
			throw new Exception("SkFisioEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
			// Si � verificato un errore nel metodo di confronto delle 2 date.
		} else
			// (rit == 1)
			return false; // Ammissibile
	}

	// 10/09/07
	// 13/09/07 m.: aggiunto crit su contatto diverso da quello in oggetto.
	private boolean dtApeContLEMaxDtContChius(ISASConnection dbc, Hashtable h) throws Exception {
		String punto = MIONOME + "dtApeContLEMaxDtContChius ";
		String strNCartella = h.get("n_cartella").toString();
		String strDataApeContatto =  h.get("skf_data").toString();
		String strNContatto =  h.get("n_contatto").toString(); // 13/09/07 m.

		String mySel = "SELECT skf_data_chiusura" + " FROM skfis" + " WHERE n_cartella = " + strNCartella
				+ (strNContatto != null ? " AND n_contatto <> " + strNContatto : "") + // 13/09/07 m.
				" AND skf_data_chiusura >= " + formatDate(dbc, strDataApeContatto) + " AND skf_data_chiusura IS NOT NULL";
		stampa(punto + " Query>" + mySel);
		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() > 0))
			return true;
		else
			return false;
	}

	private void stampa(String messaggio) {
		LOG.info(messaggio);
	}

	// 10/09/07: inserimento su tabella PROGETTO di un record con i soli valori della chiave
	private String scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception {
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		if (dtSkVal==null) dtSkVal=getDataAperturaCartella(mydbc,numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		LOG.info("\n SkFisioEJB -->> insert: Inserito record su tabella PROGETTO");
		return dtSkVal;
	}

	private String getDataAperturaCartella(ISASConnection mydbc, String numCart) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		ISASRecord dbr = mydbc.readRecord("select data_apertura from cartella where n_cartella ="+numCart); 
		return dbr.get("data_apertura").toString();
	}

	// 10/09/07: inserimento su tabella PROGETTO_CONT
	private void scriviProgettoCont(ISASConnection mydbc, String numCart, String dtSkVal, String tpOper, String numProg) throws Exception {
		ISASRecord dbrPrgCont = mydbc.newRecord("progetto_cont");
		dbrPrgCont.put("n_cartella", numCart);
		dbrPrgCont.put("pr_data", dtSkVal);
		dbrPrgCont.put("prc_tipo_op", tpOper);
		dbrPrgCont.put("prc_n_contatto", new Integer(numProg));
		mydbc.writeRecord(dbrPrgCont);
		LOG.info("\n SkFisioEJB -->> insert: Inserito record su tabella PROGETTO_CONT");
	}

	// 10/09/07: ricava il nuovo progressivo per le operazioni di 'insert'.
	private int getProgressivo(ISASConnection mydbc, String strNAssistito) throws Exception {
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String myselect = "SELECT nvl(MAX(n_contatto),0) max_n_contatto FROM skfis  WHERE n_cartella = " + strNAssistito;
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			intProgressivo = u.getIntField(dbr, "max_n_contatto");

		intProgressivo++;
		return intProgressivo;
	}

	private ISASRecord insertOpRef(ISASConnection dbc, String opref, String data_ref, String n_cartella,
			String n_contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {

		boolean done = false;
		try {
			ISASRecord dbref = dbc.newRecord("skfis_referente");
			dbref.put("n_cartella", n_cartella);
			dbref.put("n_contatto", n_contatto);
			dbref.put("skf_fisiot", opref);
			dbref.put("skf_fisiot_da", data_ref);
			dbc.writeRecord(dbref);
			done = true;
			return dbref;
		} catch (DBRecordChangedException e) {
			LOG.error("SkFisioEJB.insertIntRef(): " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			LOG.error("SkFisioEJB.insertIntRef(): " + e);
			throw e;
		} catch (Exception e1) {
			LOG.error("SkFisioEJB.insertIntRef(): " + e1);
			throw new SQLException("Errore eseguendo una insertIntRef() - " + e1);
		}
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
			CariException // 10/09/07
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr_ret = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbr_ret = update(dbc,dbr);
			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr_ret;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				LOG.error("SkFisioEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + ce);
			}

			throw ce;
		} catch (DBRecordChangedException e) {
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("SkFisioEJB-->Errore eseguendo una rollback() - " + e1);
			}

			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("SkFisioEJB-->Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (Exception e1) {
			LOG.error(e1);
			throw new SQLException("SkFisioEJB Errore eseguendo una update() - " , e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error("SkFisioEJB update()" + e2);
				}
			}
		}
	}

//	private void aggiornaFlagInviato(ISASConnection dbc, ISASRecord dbr, String n_cartella, String n_contatto) throws ISASMisuseException, DBSQLException, DBMisuseException, ISASPermissionDeniedException {
//		String punto = MIONOME + "aggiornaFlagInviato";
//
//		String query = "select * from skfis where n_cartella = " + n_cartella + " and n_contatto = " + n_contatto;
//		ISASRecord dbrSkFis = null;
//		dbrSkFis = dbc.readRecord(query);
//		
//		if (dbrSkFis != null) {
//			String flagInviato = ISASUtil.getValoreStringa(dbrSkFis, "flag_inviato");
//			stampa(punto + " dato letto>" + flagInviato + "<");
//			if (ISASUtil.valida(flagInviato) && flagInviato.equals(CONSTANTS_FLAG_INVIATO_INSERIMENTO)) {
//				stampa(punto + " NON EFFETTUO L'AGGIORNAMENTO DEL FLAG_INVIATO: flusso non ancora inivato");
//			} else {
//				stampa(punto + " variazione apportato sul record inviato o oppure variato precendentemente ");
//				flagInviato = CONSTANTS_FLAG_INVIATO_VARIATO;
//			}
//			stampa(punto + " dato che scrivo>" + flagInviato + "<");
//			dbr.put("flag_inviato", flagInviato);
//			
//			dbr.put("proresu", ISASUtil.getValoreStringa(dbrSkFis, "proresu"));
//			dbr.put("comresu", ISASUtil.getValoreStringa(dbrSkFis, "comresu"));
//			dbr.put("uslstrut", ISASUtil.getValoreStringa(dbrSkFis, "uslstrut"));
//			dbr.put("struttu", ISASUtil.getValoreStringa(dbrSkFis, "struttu"));
//			dbr.put("annoinvio", ISASUtil.getValoreStringa(dbrSkFis, "annoinvio"));
//			dbr.put("numav", ISASUtil.getValoreStringa(dbrSkFis, "numav"));
//			dbr.put("triminvi", ISASUtil.getValoreStringa(dbrSkFis, "triminvi"));
//		}
//	}

	public Object deleteAll(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = MIONOME + "deleteAll ";
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String codiceOperatore ="";
			try {
				codiceOperatore =mylogin.getUser();
			} catch (Exception e) {
				stampa(punto + " errore nel recuperare l'operatore>"+codiceOperatore+"<");
			}
			dbc.startTransaction();

			String cartella = ""+dbr.get("n_cartella");
			String contatto = ""+dbr.get("n_contatto");

			String ris = VerificaInterv(dbc, dbr);
			// LOG.info("CONTROLLO-->"+ ris) ;
			if (ris.equals("N")) {
				aggiornaFlussiSprCancellati(dbc, cartella, contatto, codiceOperatore);
				dbc.deleteRecord(dbr);
//				deleteLegameProgetto(dbc, cartella, contatto, "04");
				deleteLegameProgetto(dbc, cartella, contatto, CONSTANTS_TIPO_OPERATORE_FISIOTERAPISTA);
				// 10/09/07 deleteContsan(dbc,cartella,contatto); // eliminata tabella CONTSAN
				// VADO A CANCELLARE TUTTE LE SCHEDE ASSOCIATE AL CONTATTO
				String[] Vschede = { "skfis_referente", "skicf" };
				for (int i = 0; i < Vschede.length; i++) {
					deleteSchede(dbc, Vschede[i], cartella, contatto);
				}
				// 10/09/07 ---
				deletePianoAssist(dbc, cartella, contatto);
				deletePianoAccessi(dbc, cartella, contatto);
				deletePianoVerifiche(dbc, cartella, contatto);
				// 10/09/07 ---
			}
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			if (ris.equals("N"))
				return new Integer(0);
			else
				return new Integer(1);
		} catch (DBRecordChangedException e) {
			LOG.error("SkFisioEJB.delete1(): " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("SkFisioEJB-->Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			LOG.error("SkFisioEJB.delete2(): " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e1) {
			LOG.error("SkFisioEJB.delete3(): " + e1);
			try {
				dbc.rollbackTransaction();
			} catch (Exception ex) {
				throw new SQLException("deleteAll-->Errore eseguendo una rollback() - " + ex);
			}
			throw new SQLException("deleteAll-->Errore eseguendo una deleteAll() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	
	private boolean ablFlussiSPR(ISASConnection dbc, String codiceOperatore) {
//		devo recuperare se ho abilitazione per flussi spr
		String punto = MIONOME + "ablFlussiSPR ";
		boolean ablFlussiSpr = false;
		stampa(punto + "abilitazione per flussi spr >"+CONSTANTS_ABL_FLUSSI_SPR  +"<");
		try {
			Hashtable conf = eveUtl.leggiConf(dbc, codiceOperatore, new String[] { CONSTANTS_ABL_FLUSSI_SPR });
			stampa(punto + "dati recuperati>" + (conf != null ? conf + "" : "no dati" + ""));
			String valoreletto = ISASUtil.getValoreStringa(conf, CONSTANTS_ABL_FLUSSI_SPR);
			ablFlussiSpr = (ISASUtil.valida(valoreletto) && valoreletto.equalsIgnoreCase("SI"));
			stampa(punto + "valoreletto>"+valoreletto+"< ablFlussiSpr>"+ ablFlussiSpr + "<");
		} catch (Exception e) {
			stampa(punto + "\t Errore nel recuperare abilitazione flussi spr per il codice operatore>" + codiceOperatore + "<");
			e.printStackTrace();
		}
		return ablFlussiSpr;
	}
	
	
	private void aggiornaFlussiSprCancellati(ISASConnection dbc, String cartella, String contatto, String codiceOperatore) throws ISASMisuseException, DBSQLException, DBMisuseException, ISASPermissionDeniedException, DBRecordChangedException {
		
		String punto = MIONOME + "aggiornaFlussiSprCancellati ";
		if (ablFlussiSPR(dbc, codiceOperatore)){
			stampa(punto + " abilitazione flussi spr ");
			String query = "select * from skfis where n_cartella = " +cartella + " and n_contatto = " +contatto;
			ISASRecord dbrSkFis =null;
				dbrSkFis = dbc.readRecord(query);
				if (dbrSkFis!=null){
					stampa(punto + " Record Recuperato ");
					String flagInviato = ISASUtil.getValoreStringa(dbrSkFis, "flag_inviato");
					if (ISASUtil.valida(flagInviato) && (
							(flagInviato.equals(CONSTANTS_FLAG_INVIATO_INVIATO) || 
							 flagInviato.equals(CONSTANTS_FLAG_INVIATO_VARIATO))) ){
						stampa(punto + " devo salvare le chiavi del flusso: ");
					ISASRecord dbrFlussiSprCancellati = dbc.newRecord("flussi_spr_cancellati");

					dbrFlussiSprCancellati.put("proresu", ISASUtil.getValoreStringa(dbrSkFis, "proresu"));
					dbrFlussiSprCancellati.put("comresu", ISASUtil.getValoreStringa(dbrSkFis, "comresu"));
					dbrFlussiSprCancellati.put("uslstrut", ISASUtil.getValoreStringa(dbrSkFis, "uslstrut"));
					dbrFlussiSprCancellati.put("struttu", ISASUtil.getValoreStringa(dbrSkFis, "struttu"));
					dbrFlussiSprCancellati.put("annoinvio", ISASUtil.getValoreStringa(dbrSkFis, "annoinvio"));
					dbrFlussiSprCancellati.put("numav", ISASUtil.getValoreStringa(dbrSkFis, "numav"));
					dbrFlussiSprCancellati.put("triminvi", ISASUtil.getValoreStringa(dbrSkFis, "triminvi"));
					stampa(punto + " dati che aggiorno>"+ (dbrFlussiSprCancellati!=null ? dbrFlussiSprCancellati.getHashtable() +"":" no dati"));
					dbc.writeRecord(dbrFlussiSprCancellati);
					}else {
						stampa(punto + " Il record non � stato inviato: NON devo inviare la cancellazione del flussso");
					}
				}else {
					stampa(punto + " Record non recuperato ");
				}
		}else {
			stampa(punto + " DISABILITAZIONE flussi spr ");
		}
	}

	// 10/09/07
	private void deletePianoAssist(ISASConnection dbc, String cartella, String contatto) throws Exception {
		String myselect = "SELECT * FROM piano_assist" + " WHERE pa_tipo_oper = '04'" + " AND n_cartella = " + cartella
				+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0";
		LOG.info("deletePianoAssist " + myselect);

		ISASCursor dbcur = dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_assist" + " WHERE pa_tipo_oper = '04'" + " AND n_cartella = " + cartella
					+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pa_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	// 10/09/07
	private void deletePianoAccessi(ISASConnection dbc, String cartella, String contatto) throws Exception {
		String myselect = "SELECT * FROM piano_accessi" + " WHERE pa_tipo_oper = '04'" + " AND n_cartella = " + cartella
				+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0";
		LOG.info("deletePianoAccessi " + myselect);

		ISASCursor dbcur = dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_accessi" + " WHERE pa_tipo_oper = '04'" + " AND n_cartella = " + cartella
					+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pa_data"))) + " AND pi_prog = " + (Integer) dbr.get("pi_prog");
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	// 10/09/07
	private void deletePianoVerifiche(ISASConnection dbc, String cartella, String contatto) throws Exception {
		String myselect = "SELECT * FROM piano_verifica" + " WHERE pa_tipo_oper = '04'" + " AND n_cartella = " + cartella
				+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0";
		LOG.info("deletePianoVerifiche " + myselect);
		ISASCursor dbcur = dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_verifica" + " WHERE pa_tipo_oper = '04'" + " AND n_cartella = " + cartella
					+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pa_data"))) + " AND ve_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("ve_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	private void deleteLegameProgetto(ISASConnection dbc, String cartella, String contatto, String figprof)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASCursor dbcur = null;
		try {
			String myselect = "SELECT * FROM progetto_cont WHERE " + " n_cartella=" + cartella + " AND prc_n_contatto=" + contatto
					+ " AND prc_tipo_op='" + figprof + "'";
			debugMessage("deleteLegameProgetto=>" + myselect);
			dbcur = dbc.startCursor(myselect);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String sel = "SELECT * FROM progetto_cont WHERE " + " n_cartella=" + cartella + " AND prc_n_contatto=" + contatto
						+ " AND prc_tipo_op='" + figprof + "'" + " AND pr_data = "
						+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pr_data")));
				ISASRecord dbr2 = dbc.readRecord(sel);
				dbc.deleteRecord(dbr2);
			}
			dbcur.close();
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			LOG.error(e1);
			throw new SQLException("Errore eseguendo una deleteLegameProgetto() - " + e1);
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	private void deleteSchede(ISASConnection dbc, String tabella, String cartella, String contatto) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		boolean done = false;
		try {
			String myselect = "SELECT * FROM " + tabella + " WHERE " + " n_cartella=" + cartella + " AND n_contatto=" + contatto;
			LOG.info("SkFisio-->DeleteSchede " + myselect);
			ISASRecord dbr2 = dbc.readRecord(myselect);
			if (dbr2 != null)
				dbc.deleteRecord(dbr2);
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			LOG.error(e1);
			throw new SQLException("Errore eseguendo una deleteSchede() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	/**** 10/09/07: eliminato tabella CONTSAN  
	private ISASRecord insertContsan(ISASConnection dbc, String data_apertura,
	String data_chiusura, String descr_infer, String n_cartella,String operatore)
	    throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException {
	    int contatto=0;
	try{
	    ISASRecord dbmax=null;
	    if (n_cartella!=null && !n_cartella.equals(""))
	    {//aggiornamento di nmax_contatti su cartella
	        String selmax="SELECT * FROM"+
	                      " cartella WHERE n_cartella="+n_cartella;
	         dbmax=dbc.readRecord(selmax);
	         int max=((Integer)dbmax.get("nmax_contatti")).intValue();
	         max++;
	         contatto=max;
	         dbmax.put("nmax_contatti",new Integer(max));
	         dbc.writeRecord(dbmax);
	    }
	    ISASRecord dbr=dbc.newRecord("contsan");
	    dbr.put("n_contatto",(new Integer(contatto)).toString());
	    dbr.put("n_cartella",n_cartella);
	    dbr.put("cod_operatore",operatore);
	    dbr.put("data_contatto",data_apertura);
	    dbr.put("data_fisiot",data_apertura);
	    if(data_chiusura.compareTo("")!=0)
	      dbr.put("data_chius_fisiot",data_chiusura);
	    dbr.put("descr_fisiot",descr_infer);
	    dbc.writeRecord(dbr);
	    String myselect="Select * from contsan where "+
	            "n_cartella="+n_cartella+" and "+
	            "n_contatto="+contatto;
	    dbr=dbc.readRecord(myselect);
	    return dbr;
	}catch(DBRecordChangedException e){
	    LOG.error("SkFisioEJB.insertContsan(): "+e);
	throw e;
	}catch(ISASPermissionDeniedException e){
	    LOG.error("SkFisioEJB.insertContsan(): "+e);
	throw e;
	}catch(Exception e1){
	    LOG.error("SkFisioEJB.insertContsan(): "+e1);
	throw new SQLException("Errore eseguendo una insertContsan() - "+  e1);
	}
	}


	private ISASRecord updateContsan(ISASConnection dbc, String data_apertura,
	String data_chiusura, String descr_infer, String n_cartella,
	String n_contatto) throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException {

	try{

	    String myselect="Select * from contsan where n_cartella="+n_cartella+
	                    " and n_contatto="+n_contatto;
	    LOG.error("SkFisioEJB.updateContsan(): "+myselect);
	    ISASRecord dbr=dbc.readRecord(myselect);

	    dbr.put("data_fisiot",data_apertura);
	    dbr.put("data_chius_fisiot",data_chiusura);
	    dbr.put("descr_fisiot",descr_infer);
	    dbc.writeRecord(dbr);
	    //FACCIO LA SELECT SULLA VISTA CONTSAN_N PER TROVARE LA DATA MINIMA
	    //FRA QUELLE INSERITE DAI VARI OPERATORI
	    CalcolaDataMinima(dbc,n_cartella,n_contatto);
	    return dbr;
	}catch(DBRecordChangedException e){
	    LOG.error("SkFisioEJB.updateContsan(): "+e);
	throw e;
	}catch(ISASPermissionDeniedException e){
	    LOG.error("SkFisioEJB.updateContsan(): "+e);
	throw e;
	}catch(Exception e1){
	    LOG.error("SkFisioEJB.updateContsan(): "+e1);
	throw new SQLException("Errore eseguendo una update() - "+  e1);
	}
	}
	private void CalcolaDataMinima(ISASConnection dbc, String cartella, String contatto)
	throws DBRecordChangedException,ISASPermissionDeniedException, SQLException {

	try{
	    String mysel="SELECT data_inizio FROM contsan_n WHERE n_cartella="+cartella+
	                 " AND n_contatto="+contatto+
	                 " ORDER BY data_inizio";
	    LOG.info("SkFisioEJB.Contsan_n(): "+mysel);
	    ISASRecord dbrcont=dbc.readRecord(mysel);
	    if (dbrcont!=null){
	      String data_minore=((java.sql.Date)dbrcont.get("data_inizio")).toString();
	      String selcon="Select * from contsan where n_cartella="+cartella+
	                    " and n_contatto="+contatto;
	      ISASRecord dbrupdate=dbc.readRecord(selcon);
	      dbrupdate.put("data_contatto",data_minore);
	      dbc.writeRecord(dbrupdate);
	    }
	}catch(DBRecordChangedException e){
	    LOG.error("SkFisioEJB.CalcolaDataMinima(): "+e);
	throw e;
	}catch(ISASPermissionDeniedException e){
	    LOG.error("SkFisioEJB.CalcolaDataMinima(): "+e);
	throw e;
	}catch(Exception e1){
	    LOG.error("SkFisioEJB.CalcolaDataMinima(): "+e1);
	throw new SQLException("Errore eseguendo una CalcolaDataMinima() - "+  e1);
	}
	}


	private ISASRecord deleteContsan(ISASConnection dbc,String cartella,String contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String data_inf="";
	String data_soc="";
	String data_med="";
	String data_onc="";// 31/10/06 m.
	try{

	    String myselect="Select * from contsan where "+
			" n_cartella="+cartella+
	                    " and n_contatto="+contatto;
	    ISASRecord dbr=dbc.readRecord(myselect);
	    if(dbr.get("data_infer")!=null && !(dbr.get("data_infer")).equals(""))
	      data_inf=((java.sql.Date)dbr.get("data_infer")).toString();
	    if(dbr.get("data_sociale")!=null && !(dbr.get("data_sociale")).equals(""))
	      data_soc=((java.sql.Date)dbr.get("data_sociale")).toString();
	    if(dbr.get("data_medico")!=null && !(dbr.get("data_medico")).equals(""))
	      data_med=((java.sql.Date)dbr.get("data_medico")).toString();
		// 31/10/06 m. ---
		if(dbr.get("data_ostetr")!=null && !(dbr.get("data_ostetr")).equals(""))
	      data_onc=((java.sql.Date)dbr.get("data_ostetr")).toString();
		// 31/10/06 m. ---

	    if(data_inf.equals("") && data_soc.equals("") && data_med.equals("")
			&& data_onc.equals(""))// 31/10/06 m.
	       dbc.deleteRecord(dbr);
	    else{
	       dbr.put("data_fisiot","");
	       dbr.put("data_chius_fisiot","");
	       dbr.put("descr_fisiot","");
	       dbc.writeRecord(dbr);
	    }
	    CalcolaDataMinima(dbc,cartella,contatto);
	    done=true;
	    return dbr;
	}catch(DBRecordChangedException e){
	    e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
	    e.printStackTrace();
		throw e;
	}catch(Exception e1){
	    LOG.error(e1);
		throw new SQLException("Skfisio-->Errore eseguendo una deleteContsan() - "+  e1);
	}finally{
	    if(!done){
	        try{
	        dbc.close();
	    super.close(dbc);
	    }catch(Exception e2){LOG.error(e2);}
	    }
	}
	}
	 ******* 10/09/07: eliminato tabella CONTSAN ********/

	public ISASRecord query_salvataggio(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select * from skfis where " + "n_cartella=" + (String) h.get("n_cartella") + " and " + "n_contatto="
					+ (String) h.get("n_contatto") + " and " + "skf_data=" + formatDate(dbc, (String) h.get("skf_data"));

			LOG.info("select query_salvataggio su skfisio===" + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_salvataggio()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	/*** 10/09/07: piano assistenziale scorporato dal contatto
	public Vector query_progass(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
	    dbc=super.logIn(mylogin);
	    String myselect="Select * from fisprogass where n_cartella="+
	        (String)h.get("n_cartella")+" and n_contatto="+
	    (String)h.get("n_contatto")+" ORDER BY fipa_data";
	    ISASCursor dbcur=dbc.startCursor(myselect);
	    Vector vdbr=dbcur.getAllRecord();
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
	}catch(Exception e1){LOG.error(e1);}
	        }
	}
	}
	 *******/

	public ISASRecord query_controlloData(myLogin mylogin, Hashtable h) throws SQLException {
		String ritorno = "";
		String ritorno_max = "";
		ISASConnection dbc = null;
		ISASRecord dbtxt = null;
		boolean done = false;
		try {
			dbc = super.logIn(mylogin);
			String mysel = "SELECT MIN (int_data_prest) data " + ", MAX (int_data_prest) data_max" + " FROM interv WHERE "
					+ " int_cartella =" + (String) h.get("n_cartella") + " AND int_contatto =" + (String) h.get("n_contatto")
					+ " AND int_tipo_oper='04'";
			// debugMessage("Dentro VerificaContatti=>"+mysel);
			dbtxt = dbc.readRecord(mysel);
			if (dbtxt != null)
				if (dbtxt.get("data") != null) {

					ritorno = "" + ((java.sql.Date) dbtxt.get("data"));
					if (dbtxt.get("data_max") != null)
						ritorno_max = "" + ((java.sql.Date) dbtxt.get("data_max"));

				} else
					ritorno = "N";
			else
				ritorno = "N";
			dbtxt.put("trova_interv", ritorno);
			dbtxt.put("trova_interv_max", ritorno_max);
			dbtxt.put("n_cartella", (String) h.get("n_cartella"));
			dbtxt.put("n_contatto", (String) h.get("n_contatto"));
			dbc.close();
			super.close(dbc);
			done = true;
			return dbtxt;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controlloData()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					LOG.error(e1);
				}
			}
		}
	}

	private String VerificaInterv(ISASConnection dbc, ISASRecord dbr) throws SQLException {
		String ritorno = "";
		try {
			String mysel = "SELECT int_cartella FROM interv WHERE " + " int_cartella =" + dbr.get("n_cartella") + " AND int_contatto ="
					+ dbr.get("n_contatto") + " AND int_tipo_oper='04'";
			// LOG.info("SkFisio-->Dentro VerificaInterv=>"+mysel);
			ISASRecord dbtxt = dbc.readRecord(mysel);
			if (dbtxt != null)
				ritorno = "S";
			else
				ritorno = "N";
			// LOG.info("SkFisio-->Dentro VerificaInterv ritorno=>"+ritorno);
			return ritorno;
		} catch (Exception ex) {
			return ritorno = "";
		}
	}

	private String decodifica(String tabella, String nome_cod, Object val_codice, String descrizione, ISASConnection dbc) {
		String ret = " ";
		if (val_codice == null)
			return " ";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE " + nome_cod + " ='" + val_codice.toString()
					+ "'";
			// LOG.info("Decodifica-->tabella:"+ tabella+" Select: "+ mysel);
			ISASRecord dbtxt = dbc.readRecord(mysel);
			if (dbtxt != null && dbtxt.get("descrizione") != null) {
				ret = (String) dbtxt.get("descrizione");
			}
			return ret;
		} catch (Exception ex) {
			return " ";
		}
	}

	public void leggiDiagnosi(ISASConnection mydbc, ISASRecord mydbr) throws Exception{
		String punto = ver + "leggiDiagnosi ";
		Vector vdbr = new Vector();
		DiagnosiEJB diagnosiEJB = new DiagnosiEJB();
		Hashtable dati = mydbr.getHashtable();
		ISASCursor dbcur = null;
		try {
			diagnosiEJB.leggiDiagnosi_interno(mydbc, dbcur, vdbr, dati, false);
		} finally{
			close_dbcur_nothrow(punto, dbcur);
		}
		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	
	
	
	// 06/12/06 m.: x DIAGNOSI --------------------------------------------------
	private void leggiDiagnosi_(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String cart = ((Integer) mydbr.get("n_cartella")).toString();
		Object dtApertura = (Object) mydbr.get("skf_data");
		Object dtChiusura = (Object) mydbr.get("skf_data_chiusura");

		Vector vdbr = new Vector();

		String critDtChius = "";
		if (dtChiusura != null)
			critDtChius = " AND data_diag <= " + formatDate(mydbc, dtChiusura.toString());

		String myselect = "SELECT * FROM diagnosi" + " WHERE n_cartella = " + cart + critDtChius + " ORDER BY data_diag DESC";

		// LOG.info("SkFisioEJB: leggiDiagnosi - myselect=[" + myselect + "]");

		ISASRecord recD = mydbc.readRecord(myselect);

		if (recD != null) {
			String dataIni = "";
			if (dtApertura != null)
				dataIni = dtApertura.toString();
			String dtIni = dataIni.substring(0, 4) + dataIni.substring(5, 7) + dataIni.substring(8, 10);
			decodificaDiagn(mydbc, recD);
			decodificaOper(mydbc, recD);
			boolean isDataInContesto = checkData(recD, dtIni);
			costruisci5Rec(mydbc, recD, vdbr, (isDataInContesto ? "C" : "") + "0");
		}

		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	// Costruisce 5 record da quello letto: hanno tutti i campi del DB uguali, pi� le colonne fittizie del
	// codice e della descrizione, ognuno con i valori corrispondenti(rec1 con diag_1 e desc_diag_1, ecc).
	private void costruisci5Rec(ISASConnection mydbc, ISASRecord mydbr, Vector vett, String coloreCol) throws Exception {
		// aggiungo colonne fittizie al primo record
		mydbr.put("cod_alldiag", (String) mydbr.get("diag1"));
		mydbr.put("desc_alldiag", (String) mydbr.get("desc_diag1"));
		mydbr.put("progr", "1");
		mydbr.put("dt_diag", (java.sql.Date) mydbr.get("data_diag"));
		mydbr.put("clr_column", coloreCol);
		vett.addElement((ISASRecord) mydbr);

		// copio rec letto nei 4 nuovi record
		Hashtable h_1 = (Hashtable) mydbr.getHashtable();
		for (int j = 2; j < 6; j++) {
			ISASRecord dbr_i = mydbc.newRecord("diagnosi");
			copiaRec(h_1, dbr_i);

			// aggiungo colonne fittizie agli altri 4 record
			dbr_i.put("cod_alldiag", (String) dbr_i.get("diag" + j));
			dbr_i.put("desc_alldiag", (String) dbr_i.get("desc_diag" + j));
			dbr_i.put("progr", "" + j);
			dbr_i.put("dt_diag", "");
			vett.addElement((ISASRecord) dbr_i);
		}
	} // END costruisci6Rec

	private void copiaRec(Hashtable h_1, ISASRecord mydbr) throws Exception {
		Enumeration n_1 = h_1.keys();
		while (n_1.hasMoreElements()) {
			String e = (String) n_1.nextElement();
			mydbr.put(e, h_1.get(e));
		}
	} // END copiaRec

	// 11/12/06: restituisce true se la data diagnosi e' >= della dataInizio del contesto
	private boolean checkData(ISASRecord mydbr, String dataI) throws Exception {
		DataWI dtDiag = new DataWI((java.sql.Date) mydbr.get("data_diag"));
		return dtDiag.isUguOSucc(dataI);
	} // END checkData

	// 06/12/06m.: x DIAGNOSI -------------------------------------------------

	// ============== Decodifiche ==========================

	private void decodificaDiagn(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		for (int k = 1; k < 6; k++) {
			String cod = (String) mydbr.get("diag" + k);
			String desc = util.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi", cod, "diagnosi");
			mydbr.put("desc_diag" + k, desc);
		}
	}// END decodificaDiagn

	private void decodificaOper(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String cod = (String) mydbr.get("cod_operatore");
		String desc = util.getDecode(mydbc, "operatori", "codice", cod, "nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nome_oper");
		mydbr.put("desc_oper", desc);
	}// END decodificaOper

	// gb 12/09/07 *******
	private void AggiornaData(String strNomeTabella, String strNCartella, String strNContatto, String strNomeFldDataChiusura,
			String strDataChiusura, String strNomeFldDataApertura, ISASConnection dbc) throws SQLException {
		try {
			debugMessage("chiudo TABELLA-->" + strNomeTabella);
			String mysel = "SELECT *" + " FROM " + strNomeTabella + " WHERE n_cartella = " + strNCartella + " AND n_progetto = "
					+ strNContatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper = '04'" + " AND "
					+ strNomeFldDataChiusura + " IS NULL";
			debugMessage("chiudo -->" + strNomeTabella + " select-->" + mysel);
			ISASCursor dbcur = dbc.startCursor(mysel);
			Vector vdbr = dbcur.getAllRecord();
			for (Enumeration senum = vdbr.elements(); senum.hasMoreElements();) {
				ISASRecord dbr = (ISASRecord) senum.nextElement();
				String strDataApertura = ((java.sql.Date) dbr.get(strNomeFldDataApertura)).toString();
				LOG.info(strNomeFldDataApertura + " " + strDataApertura);
				String sel = "SELECT *" + " FROM " + strNomeTabella + " WHERE n_cartella = " + strNCartella + " AND n_progetto = "
						+ strNContatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper = '04'"
						+ " AND " + strNomeFldDataApertura + " = " + formatDate(dbc, strDataApertura);
				debugMessage("chiudo TABELLA-->" + sel);
				ISASRecord dbrDett = dbc.readRecord(sel);
				if (dbrDett.get(strNomeFldDataChiusura) == null) {
					dbrDett.put(strNomeFldDataChiusura, strDataChiusura);
				}
				dbc.writeRecord(dbrDett);
				if (dbrDett != null)
					AggiornaDataPianointerv(strNCartella, strNContatto, strDataChiusura, strDataApertura, dbc);
			}// fine for
			dbcur.close();
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}

	// gb 12/09/07: fine *******

	// bargi 160407 chiusura piani assist pianointerv e agenda alla chiusura del contatto
	/*gb 12/09/07 *******
	private void AggiornaData(String tabella, String cartella,String contatto,String data_tabella,String data_chiusura,String data_ini_tab,ISASConnection dbc)
	throws  SQLException{
	    try {
	debugMessage("chiudo TABELLA-->"+tabella);
		String mysel = "SELECT * FROM " + tabella + " WHERE "+
			"n_cartella =" + cartella +
			" and n_contatto =" + contatto+
			" and "+data_tabella +" is null";
		debugMessage("chiudo -->"+tabella+" select-->"+mysel);
		ISASCursor dbcur=dbc.startCursor(mysel);
		Vector vdbr=dbcur.getAllRecord();
	            for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
	            {
	              ISASRecord dbr=(ISASRecord)senum.nextElement();
	              String data_inizio=((java.sql.Date)dbr.get(data_ini_tab)).toString();
	              LOG.info(data_ini_tab +" "+data_inizio);
	              String sel = "SELECT * FROM " + tabella + " WHERE "+
			       "n_cartella = " + cartella + " AND "+
	                           "n_contatto = " + contatto+ " AND "+
	//                               data_ini_tab + " = '" + data_inizio+"'";
	                           data_ini_tab + " = " + formatDate(dbc,data_inizio);
							   debugMessage("chiudo TABELLA-->"+sel);
	              ISASRecord dbrDett=dbc.readRecord(sel);
	              if(dbrDett.get(data_tabella)==null){
	                dbrDett.put(data_tabella,data_chiusura);					
				  }				  
	              dbc.writeRecord(dbrDett);
				  if(dbrDett!=null)
				  AggiornaDataPianointerv(cartella,contatto,data_chiusura,data_inizio,dbc);
	            }//fine for
	            dbcur.close();
	} catch (Exception ex) {
		LOG.error(ex);
		throw new SQLException("Errore eseguendo una AggiornaData()  ");
	}
	}
	 *gb 12/09/07: fine *******/

	// gb 12/09/07 *******
	private void AggiornaDataPianointerv(String strNCartella, String strNContatto, String strDataChiusura, String strDataApertura,
			ISASConnection dbc) throws SQLException {
		try {
			debugMessage("chiudo TABELLA--> piano_accessi");
			String mysel = "SELECT *" + " FROM piano_accessi" + " WHERE n_cartella = " + strNCartella + " AND n_progetto =" + strNContatto
					+ " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper ='04'" + " AND pa_data = "
					+ formatDate(dbc, strDataApertura) + " AND ( pi_data_fine IS NULL OR pi_data_fine > "
					+ formatDate(dbc, strDataChiusura) + ")";
			debugMessage("piano_accessi da chiudere select-->" + mysel);
			// se pi_data_fine � valorizzata ma data > della data chiusura questa viene anticipata
			ISASCursor dbcur = dbc.startCursor(mysel);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String sel = "SELECT *" + " FROM piano_accessi" + " WHERE n_cartella = " + strNCartella + " AND n_progetto ="
						+ strNContatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper ='04'"
						+ " AND pa_data = " + formatDate(dbc, strDataApertura) + " AND pi_prog = " + (Integer) dbr.get("pi_prog");
				debugMessage("chiudo piano_accessi select-->" + sel);
				ISASRecord dbrDett = dbc.readRecord(sel);
				if (dbrDett != null) {
					dbrDett.put("pi_data_fine", strDataChiusura);
					dbc.writeRecord(dbrDett);
				}
			}// fine for
			dbcur.close();
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}

	// gb 12/09/07: fine *******

	/*gb 12/09/07 *******
	private void AggiornaDataPianointerv(String cartella,String contatto,String data_chiusura,String data_apertura,ISASConnection dbc)
	throws  SQLException{
	    try {
	debugMessage("chiudo TABELLA--> pianointerv");
		String mysel = "SELECT * FROM pianointerv WHERE "+
			"n_cartella =" + cartella +
			" and n_contatto =" + contatto +
			" and pi_tipo_oper ='04'" + 
			" and skpa_data =" + formatDate(dbc,data_apertura) +
		    " AND ( pi_data_fine is null OR pi_data_fine > "+
			formatDate(dbc,data_chiusura)+")";
		debugMessage("chiudo pianointerv select-->"+mysel);
		//se pi_data_fine � valorizzata ma data > della data chiusura questa viene anticipata
		ISASCursor dbcur=dbc.startCursor(mysel);
		        while (dbcur.next())
	            {
	              ISASRecord dbr=dbcur.getRecord();
	              String data_inizio=((java.sql.Date)dbr.get("skpa_data")).toString();
	              String sel = "SELECT * FROM pianointerv WHERE "+
							   "n_cartella = " + cartella + " AND "+
	                           "n_contatto = " + (Integer)dbr.get("n_contatto") + " AND "+
	                           "skpa_data = " + formatDate(dbc,data_inizio)+" AND "+
	                           "pi_prog = " + (Integer)dbr.get("pi_prog") + " AND "+
							   "pi_tipo_oper= '04'";
	              ISASRecord dbrDett=dbc.readRecord(sel);
	              if(dbrDett!=null){
	                dbrDett.put("pi_data_fine",data_chiusura);
					dbc.writeRecord(dbrDett);
				  }
	            }//fine for
	            dbcur.close();
	} catch (Exception ex) {
		LOG.error(ex);
		throw new SQLException("Errore eseguendo una AggiornaData()  ");
	}
	}
	 *gb 12/09/07 *******/

	// gb 12/09/07 private void rimuovoAgendaCaricata(String cartella,String contatto,String data_chiusura,ISASConnection dbc)
	private void rimuovoAgendaCaricata(String strNCartella, String strNContatto, String strDataChiusura, ISASConnection dbc)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASCursor dbcur = null;
		try {
			// gb 12/09/07 String mysel="select * from agenda_interv,agenda_intpre "+
			String mysel = "select * from agendant_interv, agendant_intpre "
					+ // gb 12/09/07
					" where ag_data>" + formatDate(dbc, strDataChiusura) + " and " + " ag_cartella=" + strNCartella + " and "
					+ " ag_contatto=" + strNContatto + " and " + " ag_tipo_oper='04' and " + " ag_stato=0 and " + // cancello solo appunt con stato a 0
					" ag_data=ap_data and " + " ag_progr=ap_progr and " + " ag_oper_ref=ap_oper_ref " + " order by ag_data";
			debugMessage("rimuovoAgendaCaricata select==" + mysel);
			dbcur = dbc.startCursor(mysel);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				cancellaAppuntam(dbr, dbc);
			}
			if (dbcur != null)
				dbcur.close();
			done = true;
		} catch (Exception e) {
			LOG.error("Errore in cancella agenda_intpre..." + e);
			throw new SQLException("Errore eseguendo rimuovoAgendaCaricata()  ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	private void cancellaAppuntam(ISASRecord dbrec, ISASConnection dbc) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		try {

			String data = ((java.sql.Date) dbrec.get("ap_data")).toString();
			String selag = "SELECT *"
					+
					// gb 12/09/07 " FROM agenda_intpre WHERE "+
					" FROM agendant_intpre WHERE "
					+ // gb 12/09/07
					"ap_data=" + formatDate(dbc, data) + " AND " + "ap_progr=" + dbrec.get("ap_progr") + " AND " + "ap_oper_ref='"
					+ (String) dbrec.get("ap_oper_ref") + "' AND " + "ap_prest_cod='" + (String) dbrec.get("ap_prest_cod") + "'";
			ISASRecord dbag = dbc.readRecord(selag);
			if (dbag != null) {
				dbc.deleteRecord(dbag);
				dbag = null;
				// devo controllare se sono rimasti record su agenda_intpre se non
				// ce ne sono occorre cancellare anche il record su agenda_interv
				selag = "SELECT COUNT(*) tot"
						+
						// gb 12/09/07 " FROM agenda_intpre WHERE "+
						" FROM agendant_intpre WHERE "
						+ // gb 12/09/07
						"ap_data=" + formatDate(dbc, data) + " AND " + "ap_progr=" + dbrec.get("ap_progr") + " AND " + "ap_oper_ref='"
						+ (String) dbrec.get("ap_oper_ref") + "'";
				dbag = dbc.readRecord(selag);

				int t = 0;
				if (dbag != null)
					t = util.getIntField(dbag, "tot");// convNumDBToInt("tot",dbag);
				if (t == 0) {
					// cancello da agenda_interv
					selag = "SELECT *"
							+
							// gb 12/09/07 " FROM agenda_interv WHERE "+
							" FROM agendant_interv WHERE "
							+ // gb 12/09/07
							"ag_data=" + formatDate(dbc, data) + " AND " + "ag_progr=" + dbrec.get("ag_progr") + " AND " + "ag_oper_ref='"
							+ (String) dbrec.get("ag_oper_ref") + "'";
					dbag = dbc.readRecord(selag);
					dbc.deleteRecord(dbag);
				}
			}
		} catch (Exception e) {
			LOG.error("Errore in cancella agenda_intpre..." + e);
			throw new SQLException("Errore eseguendo cancellaAppuntam()  ", e);
		}
	}

	// gb 12/09/07 *****************************************************************
	// Restituisce un messaggio appropriato se si verifica che le date di apertura
	// e chiusura della scheda contatto fisioterapisti non sono congrue con le rispettive
	// date del piano assistenziale (tabella 'piano_assist').
	// Se invece sono congrue il metodo ritorna "" (stringa vuota).
	//	
	private String checkDateContEDatePianoAssist(ISASConnection dbc, Hashtable h) throws Exception {
		ISASCursor dbcur = null;
		String strNCartella =  h.get("n_cartella").toString();
		String strNContatto =  h.get("n_contatto").toString();
		String strDataApeContatto = h.get("skf_data").toString();
		String strDataChiuContatto = null;
		String msg = "";

		String mySel = "SELECT *" + " FROM piano_assist" + " WHERE n_cartella = " + strNCartella + " AND n_progetto = " + strNContatto
				+ " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper = '04'" + " AND pa_data < "
				+ formatDate(dbc, strDataApeContatto);
		LOG.info("SkFisEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
		dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() > 0))
			msg = "Attenzione esistono Piani Assistenziali la cui data di apertura � antecedente della data apertura della scheda contatto.";
		else {
			dbcur = null;
			if (h.get("skf_data_chiusura") != null) {
				strDataChiuContatto = "" + h.get("skf_data_chiusura");

				if (strDataChiuContatto != null && !(strDataChiuContatto.equals(""))) {
					mySel = "SELECT *" + " FROM piano_assist" + " WHERE n_cartella = " + strNCartella + " AND n_progetto = " + strNContatto
							+ " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper = '04'"
							+ " AND pa_data_chiusura > " + formatDate(dbc, strDataChiuContatto) + " AND pa_data_chiusura IS NOT NULL ";
					LOG.info("SkFisEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
					dbcur = dbc.startCursor(mySel);

					if ((dbcur != null) && (dbcur.getDimension() > 0))
						msg = "Attenzione esistono Piani Assistenziali la cui data di chiusura � successiva alla data chiusura della scheda contatto.";
				}
			}
		}

		if (dbcur != null)
			dbcur.close();
		return msg;
	}

	// 13/09/07 m.: ctrl esistenza contatti successivi ad una certa data
	public Boolean query_checkContSuccessivi(myLogin mylogin, Hashtable h0) {
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String) h0.get("n_cartella");
			String dtRiferimento = (String) h0.get("dataRif");

			String myselect = "SELECT * FROM skfis" + " WHERE n_cartella = " + cart + " AND skf_data > " + formatDate(dbc, dtRiferimento);

			LOG.info("SkFisEJB: query_checkContSuccessivi - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(risu));
		} catch (Exception e1) {
			LOG.error("SkFisEJB.query_checkContSuccessivi - Eccezione=[" + e1 + "]");
			return (Boolean) null;
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					LOG.error(e2);
				}
			}
		}
	}

	// 12/10/07: chiusura skValutazione -> aggiornamento dataChiusura
	private void chiudiSkValutaz(ISASConnection mydbc, String numCart, String dtSkVal, String data_chiusura) throws Exception {
		String mysel = "SELECT p.* FROM progetto p" + " WHERE p.n_cartella = " + numCart + " AND p.pr_data = " + formatDate(mydbc, dtSkVal);

		LOG.info("SkFisioEJB -->> chiudiSkValutaz: mysel=[" + mysel + "]");
		ISASRecord mydbr = mydbc.readRecord(mysel);
		if (mydbr != null) {
			mydbr.put("pr_data_chiusura", data_chiusura);
			mydbc.writeRecord(mydbr);
		}
	}

	// 11/01/08
	private String leggiConf(ISASConnection mydbc, String cod) throws Exception {
		String desc = "";

		String selConf = "SELECT conf_txt" + " FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND conf_key = '" + cod + "'";

		ISASRecord dbrDec = mydbc.readRecord(selConf);
		if (dbrDec != null)
			if (dbrDec.get("conf_txt") != null)
				desc = (String) dbrDec.get("conf_txt");

		return desc;
	}// END leggiConf

	// 20/05/09 Elisa Croci
	/* 1) Il caso non esiste: creo il caso e la segnalazione
	 * 2) Il caso esiste ma e' chiuso: creo il caso e la segnalazione
	 * 3) Il caso e' attivo: aggiorno la segnalazione
	 */
	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov) throws NumberFormatException,
			ISASMisuseException, CariException {
		LOG.info("SkFisioEJB: gestione_segnalazione -- HASH: " + h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;

		h.put("operZonaConf", (String) dbr.get("skf_operatore")); // 15/10/09

		if (h.get("id_caso") != null && !h.get("id_caso").equals("-1")) {
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(h.get("stato").toString());
			id_caso = Integer.parseInt(h.get("id_caso").toString());
		}

		// se sono in insert e il caso non esiste oppure e' concluso, devo crearne uno!
		if (prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU)) {
			// se il caso non esiste, non c'e' nemmeno la segnalazione, allora la creo!
			try {
				h.put("tipo_caso", new Integer(GestCasi.CASO_SAN));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_SANITARIO));

				if (h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals(""))
					h.put("dt_segnalazione", h.get("skf_data"));

				if (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
					h.put("dt_presa_carico", h.get("dt_segnalazione"));

				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
				// arriva la data del progetto, cosi' me la copio dal dbr!
				h.put("pr_data", dbr.get("pr_data"));

				ISASRecord rec_segn = gestore_segnalazioni.insert(dbc, h);

				if (rec_segn != null) {
					Enumeration en = rec_segn.getHashtable().keys();
					while (en.hasMoreElements()) {
						String chiave = en.nextElement().toString();
						dbr.put(chiave, rec_segn.get(chiave));
					}

					ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
					if (rec_pc != null) {
						Enumeration en1 = rec_pc.getHashtable().keys();
						while (en1.hasMoreElements()) {
							String chiave = en1.nextElement().toString();
							dbr.put(chiave, rec_pc.get(chiave));
						}

						dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
					}

					return Integer.parseInt(rec_segn.get("id_caso").toString());
				} else
					return -1;
			} catch (CariException e) // 17/11/09
			{
				LOG.error("SkFisioEJB gestione_segnalazione, insert -- " + e);
				throw e;
			} catch (DBRecordChangedException e) {
				LOG.error("SkFisioEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} catch (ISASPermissionDeniedException e) {
				LOG.error("SkFisioEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} catch (SQLException e) {
				LOG.error("SkFisioEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} catch (Exception e) {
				LOG.error("SkFisioEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
		}
		// 29/03/10 else if(id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU && stato_caso != -1))
		else if (id_caso != -1 && (stato_caso != -1)) {
			// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione e della presa in carico
			try {
				Enumeration e = dbr.getHashtable().keys();
				while (e.hasMoreElements()) {
					String chiave = e.nextElement().toString();

					if (!h.containsKey(chiave))
						h.put(chiave, dbr.get(chiave));
				}

				printError(" gestione_segnalazione - UPDATE, H: " + h.toString());

				// 12/08/10 m ---------------
				if (dbr.get("origine") != null && !dbr.get("origine").equals("")) {
					int origine = Integer.parseInt(dbr.get("origine").toString());
					printError("gestione_segnalazione: Origine del caso " + id_caso + " =[" + origine + "]");

					// aggiorno solo se il caso nel frattempo non � diventato UVM, altrimenti prtono comunicazioni di EVENTI non previste
					if (origine == GestCasi.CASO_SAN) { // 12/08/10 m ---
						ISASRecord new_segnalazione = gestore_segnalazioni.update(dbc, h);

						if (new_segnalazione != null) {
							Enumeration en = new_segnalazione.getHashtable().keys();
							while (en.hasMoreElements()) {
								String chiave = en.nextElement().toString();
								dbr.put(chiave, new_segnalazione.get(chiave));
							}
						}

						// 29/03/12: aggiunto cntrl su esistenza rec, dato che il CASO e la SEGNALAZIONE potrebbero essere stati inseriti da Sins_PUA
						// (e quindi necessita update) , ma la PRESACARICO potrebbe dover essere in insert.
						if (!esistePresaCar(dbc, h)) {
							if (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
								h.put("dt_presa_carico", h.get("skf_data"));

							ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
							if (rec_pc != null) {
								gestore_casi.presaCaricoCaso(dbc, h);

								Enumeration en1 = rec_pc.getHashtable().keys();
								while (en1.hasMoreElements()) {
									String chiave = en1.nextElement().toString();
									dbr.put(chiave, rec_pc.get(chiave));
								}

								dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
							}
						} else {
							ISASRecord update_presacarico = gestore_presacarico.update(dbc, h);
							if (update_presacarico != null) {
								Enumeration en = update_presacarico.getHashtable().keys();
								while (en.hasMoreElements()) {
									String chiave = en.nextElement().toString();
									dbr.put(chiave, update_presacarico.get(chiave));
								}

								dbr.put("cod_usl", dbr.get("reg_ero").toString() + dbr.get("asl_ero").toString());
							}
						}
					}
				}

				return id_caso;
			} catch (CariException e) // 17/11/09
			{
				LOG.error("SkFisioEJB gestione_segnalazione, update -- " + e);
				throw e;
			} catch (Exception e) {
				LOG.error("SkFisioEJB gestione_segnalazione, update() -- " + e);
				return id_caso;
			}
		} else
			return id_caso;
	}// END gestione_segnalazione

	// 25/05/09 Elisa Croci
	private void prendi_presacarico(ISASConnection dbc, int caso, ISASRecord dbr) {
		try {
			if (caso != -1) {
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				h.put("ubicazione", dbr.get("ubicazione"));

				ISASRecord res = gestore_presacarico.queryKey(dbc, h);

				if (res != null) {
					Enumeration e = res.getHashtable().keys();
					while (e.hasMoreElements()) {
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}

					dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());
				}
			}
		} catch (ISASMisuseException e1) {
			LOG.error("SkFisioEJB prendi_presacarico, ERRORE REPERIMENTO CHIAVE! -- " + e1);
		} catch (Exception e) {
			LOG.error("SkFisioEJB prendi_presacarico, fallimento! -- " + e);
		}
	}// END prendi_presacarico

	// 20/05/09 Elisa Croci
	// prendo la segnalazione relativa al caso a cui il contatto deve fare riferimento
	private boolean prendi_segnalazione(ISASConnection dbc, int caso, ISASRecord dbr) {
		try {
			/* prendo la segnalazione solo se il caso esiste e se sono in un contesto in cui si
				gestiscono le segnalazioni
			 */
			if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN) && caso != -1) {
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				ISASRecord res = gestore_segnalazioni.queryKey(dbc, h);

				if (res != null) {
					Enumeration e = res.getHashtable().keys();
					while (e.hasMoreElements()) {
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}
				}

				return true;
			} else
				return false;
		} catch (ISASMisuseException e1) {
			LOG.error("SkFisioEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
			return false;
		} catch (Exception e) {
			LOG.error("SkFisioEJB prendi_segnalazione, fallimento! -- " + e);
			return false;
		}
	}// END prendi_segnalazione

	// 20/05/09 Elisa Croci
	// dato un contatto, prendo il caso attivo se esiste altrimenti quello chiuso piu' recente!
	private int prendi_dati_caso(ISASConnection dbc, ISASRecord dbr) {
		Hashtable h = new Hashtable();

		try {
			h.put("n_cartella", dbr.get("n_cartella"));
			h.put("pr_data", dbr.get("pr_data"));

			LOG.info("SkFisioEJB -- prendi dati caso: " + h.toString());

			ISASRecord rec = gestore_casi.getCasoRif(dbc, h);
			if (rec != null) {
				Enumeration e = rec.getHashtable().keys();
				while (e.hasMoreElements()) {
					String chiave = e.nextElement().toString();
					dbr.put(chiave, rec.get(chiave));
				}

				int caso = Integer.parseInt(dbr.get("id_caso").toString());
				return caso;
			} else
				return -1;
		} catch (ISASMisuseException e) {
			LOG.error("SkFisioEJB prendi_dati_caso, manca chiave primaria! -- " + e);
			return -1;
		} catch (Exception e) {
			LOG.error("SkFisioEJB prendi_dati_caso, fallimento! -- " + e);
			return -1;
		}
	}// END prendi_dati_caso

	// 15/07/09 m.: lettura dtConclusione CASO precedente
	private String getDtChiuCasoPrec(ISASConnection dbc, Hashtable h) throws Exception {
		String dtChiusPrec = "";
		// 11/06/10
		h.put("orig_caso_chiuso", new Integer(gestore_casi.CASO_UVM));

		ISASRecord lastCasoChiu = (ISASRecord) gestore_casi.getLastCasoChiuso(dbc, h);

		if ((lastCasoChiu != null) && (lastCasoChiu.get("dt_conclusione") != null))
			dtChiusPrec = "" + lastCasoChiu.get("dt_conclusione");
		printError("getDtChiuCasoPrec - dtChiusPrec=[" + dtChiusPrec + "]");

		return dtChiusPrec;
	}

	// 26/03/10
	private String getTabVociCodReg(ISASConnection dbc, String tbCod, String tbVal) throws Exception {
		String codReg = "99";
		String sel = "SELECT tab_codreg FROM tab_voci" + " WHERE tab_cod = '" + tbCod + "'" + " AND tab_val = '" + tbVal + "'";

		ISASRecord dbr1 = dbc.readRecord(sel);
		if ((dbr1 != null) && (dbr1.get("tab_codreg") != null))
			codReg = (String) dbr1.get("tab_codreg");
		return codReg;
	}

	// 29/03/12: cntrl esistenza rec PRESACARICO
	private boolean esistePresaCar(ISASConnection dbc, Hashtable h) throws Exception {
		ISASRecord recPC = (ISASRecord) gestore_presacarico.queryKey(dbc, h);
		return (recPC != null);
	}

	private void printError(String msg) {
		if (mydebug)
			LOG.error("SkInfEJB: " + msg);
	}
	
	public ISASRecord update(ISASConnection dbc,ISASRecord dbr)throws Exception{

		String n_cartella = null;
		String n_contatto = null;
//		String data_apertura = null;
//		String strDtSkVal = null;// 10/09/07

		try {
			n_cartella = dbr.get("n_cartella").toString();
			n_contatto = dbr.get("n_contatto").toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}


		// 10/09/07 *************************
		Hashtable h = dbr.getHashtable();
		if (dtApeContLEMaxDtContChius(dbc, h)) {
			String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
			throw new CariException(msg, -2);
		}
		// 10/09/07: fine *************************

		// gb 12/09/07 *************************
		String strMsgCheckDatePianoAssist = checkDateContEDatePianoAssist(dbc, h);
		if (!strMsgCheckDatePianoAssist.equals(""))
			throw new CariException(strMsgCheckDatePianoAssist, -2);
		// gb 12/09/07: fine *************************
//		aggiornaSkinfValoriCostantiSinssntW(dbr);
//		aggiornaFlagInviato(dbc,dbr, n_cartella, n_contatto);
		dbc.writeRecord(dbr);

//		data_apertura = dbr.get("skf_data").toString();
		String myselect = "SELECT * FROM skfis where " + "n_cartella=" + n_cartella + " and" + " n_contatto=" + n_contatto;
		LOG.info("Update: " + myselect);
		dbr = dbc.readRecord(myselect);

		String descr_fis = (String) dbr.get("skf_descr_contatto");
		String data_chiusura = "";

		if (dbr.get("skf_data_chiusura") != null)
			data_chiusura = ((java.sql.Date) dbr.get("skf_data_chiusura")).toString();

		// 10/09/07 this.updateContsan(dbc,data_apertura,data_chiusura,descr_fis,n_cartella,n_contatto); // eliminato tabella CONTSAN

		if (data_chiusura != null && !(data_chiusura.equals(""))) {
			// bargi 16/04/2007
			// gb 01/10/07: Controlli e chiusure entit� sottostanti
			CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
			// date prestazioni erogate della tabella interv.
			// date aper. e date chius. dei piani assitenziali.
			// date aper. dei piani accessi.
			String strMsgCheckDtCh = clCcec.checkDtChDaContProgGTDtApeDtCh(dbc, n_cartella, n_contatto, data_chiusura, "04");
			if (!strMsgCheckDtCh.equals(""))
				throw new CariException(strMsgCheckDtCh, -2);

			// Chiusure entit� che stanno sotto il contatto:
			// Piani assistenziali
			// Piani accessi
			// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
			clCcec.chiudoDaContattoInGiu(dbc, n_cartella, n_contatto, data_chiusura, "04", (String) dbr.get("skf_operatore"));
			// gb 01/10/07: fine *******
			// chiudo piani assistenziali
			// gb 12/09/07 AggiornaData("fisprogass",n_cartella,n_contatto,"fipa_data_chiusura",data_chiusura,"fipa_data",dbc);
			/*gb 01/10/07 *******
			AggiornaData("piano_assist", n_cartella,  n_contatto, "pa_data_chiusura", data_chiusura, "pa_data", dbc);

			//rimuovo da agenda appuntamenti caricati per la cartella chiusa
			rimuovoAgendaCaricata(n_cartella, n_contatto, data_chiusura, dbc);	
			 *gb 01/10/07: fine *******/

//			// 12/10/07 m. ---
//			if (strDtSkVal!=)
//			String skValDaChiudere = (String) h.get("skValDaChiudere");
//			if ((skValDaChiudere != null) && (skValDaChiudere.trim().equals("S")))
//				chiudiSkValutaz(dbc, n_cartella, strDtSkVal, data_chiusura);
//			// 12/10/07 m. ---
		}

		// 07/12/06 m.
		leggiDiagnosi(dbc, dbr);

		// 22/04/11: xRME --
		dbr.put("desc_ospdim", decodifica("ospedali", "codosp", dbr.get("skf_osp_dim"), "descosp", dbc));
		dbr.put("desc_rep", decodifica("reparti", "cd_rep", dbr.get("skf_uo_dim"), "reparto", dbc));
		// 22/04/11: xRME --

		// 10/09/07: per rimandare indietro al client la data della scheda valutazione
//		dbr.put("pr_data", strDtSkVal);

		// 15/07/09 m. ------------------
		// lettura dtConclusione CASO precedente
		String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
		String tempoT = (String) h.get("tempo_t");

		// letture scale max
		gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, (String) h.get("pr_data"), "", dtChiusCasoPrec, "", tempoT, "04");
		// 15/07/09 m. ------------------
		
		// Simone: 17/03/2015 gestione caso non necessaria
//		int idCaso = -1;
//		// 21/05/09 Elisa Croci
//		if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
//			idCaso = prendi_dati_caso(dbc, dbr);
//			gestione_segnalazione(dbc, dbr, h, "update");
//
//			printError("Fatta gestione segnalazione per il caso: " + idCaso);
//
//		} else {
//			h.put("dt_segnalazione", data_apertura);
//			h.put("dt_presa_carico", data_apertura);
//			h.put("origine", "" + GestCasi.CASO_SAN);
//			idCaso = gestore_casi.getIdCasoOrigine(dbc, h).intValue();
//		}
//
//		// 15/06/09 Elisa Croci ***************************************************************
//		if (data_chiusura != null && !data_chiusura.equals("")) {
//			printError("Controllo contatto UNICO SANITARIO H == " + h.toString());
//			boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true, true);
//			if (idCaso != -1 && unico) {
//				printError("Gestisco la chiusura del caso");
//				// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
//				// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
//				// sociale associato!
//				int origine = -1;
//				if (dbr.get("origine") != null && !(dbr.get("origine").toString()).equals(""))
//					origine = Integer.parseInt(dbr.get("origine").toString());
//				else if (h.get("origine") != null && !(h.get("origine").toString()).equals(""))
//					origine = Integer.parseInt(h.get("origine").toString());
//				if (origine != -1) {
//					printError("Origine del caso: " + origine);
//					if (origine == GestCasi.CASO_SAN) {
//						Hashtable hCaso = new Hashtable();
//						hCaso.put("n_cartella", h.get("n_cartella"));
//						hCaso.put("pr_data", h.get("pr_data"));
//						hCaso.put("id_caso", new Integer(idCaso));
//						hCaso.put("dt_conclusione", dbr.get("skf_data_chiusura"));
//						// 26/03/10 hCaso.put("motivo", "99");
//						// 26/03/10 ----
//						String motChiu = (String) h.get("skf_motivo_chius");
//						String motChiuFlux = getTabVociCodReg(dbc, "FCHIUS", motChiu);
//						hCaso.put("motivo", motChiuFlux);
//						// 26/03/10 ----
//						hCaso.put("operZonaConf", (String) dbr.get("skf_operatore")); // 15/10/09
//						printError(" -- update(): Chiudi caso = HashCaso: " + hCaso.toString());
//						Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
//						printError("Ritorno di ChiudiCaso == " + r);
//					}
//				}
//			}
//		}
//		
		
		String opref = (String) h.get("skf_fisiot");
		String data_ref = h.get("skf_fisiot_da").toString();
		// Aggiorno l'operatore referente nel caso in cui sia diverso da quello precedente, altrimenti ne inserisco uno nuovo.
		String selref="SELECT * FROM skfis_referente WHERE "+
				"n_cartella="+n_cartella+" AND "+
				"n_contatto="+n_contatto+" AND " +
				"skf_fisiot_da="+formatDate(dbc, data_ref);
		ISASRecord dbr_ref = dbc.readRecord(selref);
		if (dbr_ref == null){
			this.insertOpRef(dbc,opref,data_ref,n_cartella,n_contatto);
		}
		else if (!dbr_ref.get("skf_fisiot").toString().equals(dbr.get("skf_fisiot").toString()))
		{
			dbr_ref.put("skf_fisiot",dbr.get("skf_fisiot").toString());
			dbc.writeRecord(dbr_ref);
		}
		
		
		
		
		
		// ****************************************************************************************

		// 15/06/09 Elisa Croci ***************************************************************
		if (h.containsKey("ubicazione") && dbr != null)
			dbr.put("ubicazione", h.get("ubicazione"));
		if (h.containsKey("update_segnalazione") && dbr != null)
			dbr.put("update_segnalazione", h.get("update_segnalazione"));
		// *************************************************************************************
		try {
			dbr.put("des_operatore",
					decodifica("operatori", "codice", dbr.get("skf_operatore"), "nvl(cognome,'') || nvl(nome,'')", dbc));
			dbr
					.put("des_operat_refe", decodifica("operatori", "codice", dbr.get("skf_fisiot"), "nvl(cognome,'') || nvl(nome,'')",
							dbc));
		} catch (Exception e) {
			LOG.error(" Errore nel recuperare la decodifica operatori");
		}
		recuperaDescrizioni(dbc, dbr);
		
		
		// Simone 25/11/14 Aggiornamento id_skso su rm_diario
		if (h.get("id_skso")!=null&&!h.get("id_skso").toString().trim().equals("")){
			Hashtable h_rm_diario = (Hashtable)h.clone();
			h_rm_diario.put("tipo_operatore", CostantiSinssntW.TIPO_OPERATORE_FISIOTERAPISTA);
			h_rm_diario.put("id_skso", h.get("id_skso"));
			try {
				Boolean id_skso_updated = RMDiarioEJB.updateIdSkso(dbc, h_rm_diario);
				LOG.debug("Esito aggiornamento id_skso su diario = "+id_skso_updated.booleanValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			}
		
		LOG.info("\n FINE \n");
		return dbr;
	}

	public ISASRecord recuperaMaxSkFisio(ISASConnection dbc, String nCartella) {
		String punto = ver + "recuperaMaxSkFisio ";
		ISASRecord dbrSkMed = null;
		try {
			String query = "SELECT k.* FROM skfis k WHERE k.n_cartella = " +nCartella+
					" AND k.n_contatto IN (SELECT MAX (x.n_contatto) " +
					" FROM skfis x WHERE x.n_cartella = k.n_cartella) ";
			LOG.trace(punto + " query>>" + query);
			dbrSkMed = dbc.readRecord(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbrSkMed;
	}
}

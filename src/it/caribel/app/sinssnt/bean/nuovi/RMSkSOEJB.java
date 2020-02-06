package it.caribel.app.sinssnt.bean.nuovi;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 25/10/06 - EJB di connessione alla procedura SINS Skvalutaz
//     scrittura nella tabella RM_SKSO
//
// ==========================================================================

import it.caribel.app.common.ejb.RsaPreferenzeEJB;
import it.caribel.app.sins_pht.util.CostantiPHT;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkFisioEJB;
import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.bean.modificati.SkMedEJB;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.PreferenzeStruttureCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SegreteriaOrganizzativaFormCtrl;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerAnagraC;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.casi_adrsa.GestRivalutazione;
import it.pisa.caribel.sinssnt.casi_adrsa.GestSegnalazione;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.NumberDateFormat;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.zkoss.util.resource.Labels;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RMSkSOEJB extends RMSkSOBaseEJB {
	public RMSkSOEJB() {       
	}

	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();


	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();
	private GestRivalutazione gestore_rivalutazioni = new GestRivalutazione();
	
	private boolean myDebug = true;
	private String nomeEJB = "25-RMSkSOEJB ";
	private ScaleVal gest_scaleVal = new ScaleVal();

	private Hashtable hash_tp_oper = faiHashTpOper();
	private final Hashtable HASH_MAP_MOTCHIUS = faiHashMapMotChius();

	private int CTS_NUMERO_ACCESSI_MOSTRARE = 1;
	
	public String CTS_NO_SCHEDA_SO_PRESENTE = "accessi.no.scheda.so.presente";
	public String CTS_NO_SCHEDA_SO_PRESENTE_SCHEDA_SCADUTA = "accessi.no.scheda.so.presente.scaduta";
	
	public static String CTS_DATA_ACCESSO = "int_data_acc";
	public static String ESCLUSO_ESTREMO = "escl_est";


	//public static String CTS_DIAGNOSI_ASSOCIATE = "diagn_associate";
	private static final String ver = "37-";
	
//	public ISASRecord queryKey(ISASConnection dbc, String nCartella, String idSkso) throws CariException,
//			ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "queryKey ";
//		ISASRecord dbrRmSkso = null;
//		LOG.info(punto + " dati da esaminare cartella>>"+nCartella+"< idskso>>" + idSkso);
//		
//		try {
//			String query = "SELECT p.*, richiedente, stato_civile, num_fam, badante, a.tipocura FROM rm_skso p join rm_skso_mmg a" +
//					" on a.n_cartella = p.n_cartella and a.id_skso = p.id_skso" +
//					" WHERE p.n_cartella = " + nCartella + " and p.id_skso = " + idSkso;
//			LOG.debug(punto + " QUERY>>" + query+"<<");
//			dbrRmSkso = dbc.readRecord(query);
//			
//			if(dbrRmSkso!=null){
//				decodificaDati(dbc, dbrRmSkso);
//			}
//			
//		} catch (Exception e) {
//			LOG.error(punto + " Errore nel recupera i dati ");
//		}
//		
//		return dbrRmSkso;
//	}

//	public ISASRecord queryKey(myLogin mylogin, String nCartella, String idSkso) {
//		String methodName = "queryKey ";
//		ISASRecord dbrRmSkso = null;
//		ISASConnection dbc = null;	
//		
//		try {
//			dbc = super.logIn(mylogin);
//			dbrRmSkso = queryKeyRmSkso(nCartella, idSkso, dbc);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {
////			close_dbcur_nothrow(methodName, dbcur);
//			logout_nothrow(methodName, dbc);
//		}
//		
//		return dbrRmSkso;
//	}

//	public ISASRecord selectSkValCorrenteNoISAS(myLogin mylogin, Hashtable h) throws CariException {
//		String punto = nomeEJB + "selectSkValCorrente ";
//		LOG.info(punto + " dati da esaminare>>" + (h != null ? h + "" : " no dati "));
//		ISASConnection dbc = null;
//		ISASRecord dbr = null;
//		String nCartella = (String) h.get(CostantiSinssntW.N_CARTELLA);
//		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//		String idRich = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_RICH);
//		String sonoSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SONO_IN_SO);
//        ISASCursor dbgriglia=null;
//		
//		try {
//			dbc = super.logIn(mylogin);
//			dbc.startTransaction();
//			String query = "";
//			if (ISASUtil.valida(idSkso)) {
//				query = "SELECT n_cartella, id_skso, pr_data_puac, data_presa_carico_skso, pr_protoc_domanda, " +
//						"pr_data_protoc, pr_note, pr_data_verbale_uvm, pr_num_verbale, pr_valutazione, pr_obiettivo, " +
//						"pr_piano_terapeutico, pr_revisione, pr_data_revisione, pr_data_chiusura, pr_motivo_chiusura, " +
//						"pr_note_chiusura, tipo_ute, jdbinterf_version, jdbinterf_lastcng, caregiver_nome, " +
//						"caregiver_cognome, caregiver_telefono, caregiver_grado_parentela, cod_case_manager, " +
//						"dt_presa_carico_livello, presa_carico_livello, cod_distretto_verbale, cod_zona_verbale, " +
//						"cod_presidio, case_manager_mmg, pv_tp_operatore, pv_cod_operatore, pv_dt_visita, " +
//						"cod_commis_uvm, vista_da_so, frequenza, ispianocongelato " +
//						"FROM rm_skso p WHERE p.n_cartella = " + nCartella + " and p.id_skso = " + idSkso;
//			} else {
//				query = "SELECT n_cartella, id_skso, pr_data_puac, data_presa_carico_skso, pr_protoc_domanda, " +
//						"pr_data_protoc, pr_note, pr_data_verbale_uvm, pr_num_verbale, pr_valutazione, pr_obiettivo, " +
//						"pr_piano_terapeutico, pr_revisione, pr_data_revisione, pr_data_chiusura, pr_motivo_chiusura, " +
//						"pr_note_chiusura, tipo_ute, jdbinterf_version, jdbinterf_lastcng, caregiver_nome, " +
//						"caregiver_cognome, caregiver_telefono, caregiver_grado_parentela, cod_case_manager, " +
//						"dt_presa_carico_livello, presa_carico_livello, cod_distretto_verbale, cod_zona_verbale, " +
//						"cod_presidio, case_manager_mmg, pv_tp_operatore, pv_cod_operatore, pv_dt_visita, " +
//						"cod_commis_uvm, vista_da_so, frequenza, ispianocongelato  " +
//						"FROM rm_skso p WHERE p.n_cartella = " + nCartella + " AND p.pr_data_chiusura IS NULL";
//			}
//			LOG.trace(punto + " query>>" + query + "]");
//			dbr = dbc.readRecord(query);
//			
//			if (dbr!=null){
//				idSkso= ISASUtil.getValoreStringa(dbr, "id_skso");
//				query = "select x.conviventi, x.* from rm_skso_mmg x where x.n_cartella = "+ nCartella +" and id_skso ="+ idSkso;
//				ISASRecord dbrRmSkso = dbc.readRecord(query);
//				dbr.put(CostantiSinssntW.CTS_CONVIVENTI, ISASUtil.getValoreStringa(dbrRmSkso,CostantiSinssntW.CTS_CONVIVENTI));
//			}
//		   
//		} catch (Exception e) {
//			LOG.trace(punto + " Errore nel recuperare l'ultima scheda ", e);
//			try {
//				dbc.rollbackTransaction();
//			} catch (ISASMisuseException e1) {
//				e1.printStackTrace();
//			} catch (ISASPermissionDeniedException e1) {
//				e1.printStackTrace();
//			} catch (DBMisuseException e1) {
//				e1.printStackTrace();
//			} catch (DBSQLException e1) {
//				e1.printStackTrace();
//			}
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return dbr;
//	}

//	public ISASRecord selectSkValCorrente(myLogin mylogin, Hashtable h) throws CariException {
//		String punto = nomeEJB + "selectSkValCorrente ";
//		LOG.info(punto + " dati da esaminare>>" + (h != null ? h + "" : " no dati "));
//		ISASConnection dbc = null;
//		ISASRecord dbr = null;
//		try {
//			dbc = super.logIn(mylogin);
//			dbc.startTransaction();
//			
//			
//			dbr = selectSkValCorrente(dbc, h);
//			
//			dbr.put(CostantiSinssntW.CTS_FONTE, ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_FONTE));
//			dbr.put(CostantiSinssntW.CTS_ID_RICHIESTA, ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_RICHIESTA));
//			
//			dbc.commitTransaction();
//		} catch (ISASPermissionDeniedException e) {
//			try {
//				dbc.rollbackTransaction();
//			} catch (ISASMisuseException e1) {
//				e1.printStackTrace();
//			} catch (ISASPermissionDeniedException e1) {
//				e1.printStackTrace();
//			} catch (DBMisuseException e1) {
//				e1.printStackTrace();
//			} catch (DBSQLException e1) {
//				e1.printStackTrace();
//			}
//			LOG.trace(punto + " SkValutazEJB.selectSkValCorrente():>>" + e);
//			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
//		} catch (Exception e) {
//			LOG.trace(punto + " Errore nel recuperare l'ultima scheda ", e);
//			try {
//				dbc.rollbackTransaction();
//			} catch (ISASMisuseException e1) {
//				e1.printStackTrace();
//			} catch (ISASPermissionDeniedException e1) {
//				e1.printStackTrace();
//			} catch (DBMisuseException e1) {
//				e1.printStackTrace();
//			} catch (DBSQLException e1) {
//				e1.printStackTrace();
//			}
//			e.printStackTrace();
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return dbr;
//	}

//	public ISASRecord selectSkValCorrente(ISASConnection dbc, Hashtable h) throws ISASMisuseException, ISASPermissionDeniedException,
//			DBMisuseException, DBSQLException, Exception {
//		String punto = ver + "selectSkValCorrente ";
//		
//		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//		String idRich = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_RICH);
//		String sonoSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SONO_IN_SO);
//		String dtAssistitoChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
//		String fonte = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_FONTE);
//		
//		ISASRecord dbr;
//		String query = recuperaQuery(nCartella, idSkso, dtAssistitoChiusura);
//		LOG.trace(punto + " query>>" + query + "]");
//		dbr = dbc.readRecord(query);
//
//		if (dbr != null) {
//			idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
//			
//			if(ISASUtil.valida(sonoSo) && sonoSo.equals(CostantiSinssntW.CTS_SI) ){
//				dbr = aggiornaVistaDaSO(dbc, dbr, nCartella, idSkso);
//			}
//			
//			recuperaSkSoMMG(dbc, nCartella, idSkso, dbr);
//			decodificaDati(dbc, dbr);
//			// Si prendono data_apertura e data_chiusura dalla tabella 'cartella'
//			putInfoFromCartellaIntoDbr(dbc, dbr, nCartella);
//			
//			leggiPatologie(dbc, dbr);
//			decodificaStatoPrimaVisita(dbr);
//		} else {
//			dbr = dbc.newRecord("rm_skso");
//			dbr.put(CostantiSinssntW.N_CARTELLA, nCartella);
//			dbr.put(CostantiSinssntW.CTS_RECORD_FITTIZIO, CostantiSinssntW.CTS_RECORD_FITTIZIO_SI);
//			
//			if (ISASUtil.valida(fonte) && fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+"")){
//				recuperaInfoAnagrafica(dbc, nCartella, dbr);
//				String richiesta = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_RICHIESTA);
//				ISASRecord dbrPht2 = recuperaTipoCuraFromPHT2(dbc, nCartella, richiesta);
//				String tipoCura = ISASUtil.getValoreStringa(dbrPht2, Costanti.CTS_PHT2_TIPO_CURA);
//				if (ISASUtil.valida(tipoCura)){
//					dbr.put(CostantiSinssntW.CTS_TIPOCURA, tipoCura);
//				}
//				if (dbrPht2 !=null && dbrPht2.get(Costanti.CTS_PHT2_DATA_DIMISSIONE_PREVISTA)!=null){
//					dbr.put(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO, dbrPht2.get(Costanti.CTS_PHT2_DATA_DIMISSIONE_PREVISTA));
//				}
//			}else if (ISASUtil.valida(idRich)) {
//				LOG.debug(punto + " inserisco recod fittizio per caricare le richieste mmg ");
//				dbr.put(CostantiSinssntW.CTS_ID_RICH, idRich);
//				dbr.putHashtable(recuperaInfoFromRichiesteMMG(dbc, nCartella, idRich));
//				recuperaDescrizioneMedico(dbc, dbr);
//				dbr.put(CostantiSinssntW.CTS_SKM_DATA_APERTURA, ISASUtil.getValoreStringa(dbr, "pr_mmg_data_richiesta"));
//				recuperaInfoAnagrafica(dbc, dbr, nCartella);
//			}else {
//				recuperaInfoAnagrafica(dbc, nCartella, dbr);
//			}
//			
//		}
//		if(ISASUtil.valida(idSkso)){
//			Vector<ISASRecord> vdbg= new Vector();
//			vdbg =(Vector<ISASRecord>) recuperaPrestazioniPai(dbc, nCartella, idSkso, CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE);
//			dbr.put("gridPianoPAI",vdbg);
//
//			Vector<ISASRecord> vdbg1= new Vector();
//			vdbg1 =(Vector<ISASRecord>) recuperaPrestazioniPai(dbc, nCartella, idSkso, CostantiSinssntW.CTS_COD_CURE_PRESTAZIONALI);
//			dbr.put("gridPianoCP",vdbg1);
//			
//			Vector<ISASRecord> vdbg2= new Vector();
//			vdbg2 =(Vector<ISASRecord>) recuperaObiettiviPai(dbc, nCartella, idSkso);
//			dbr.put("gridObiettivi",vdbg2);
//		}
//
//		recuperaInfoScheda(dbc, dbr);
//		LOG.debug(punto + " dati recuperati>>" + (dbr != null ? dbr.getHashtable() + "" : " no dati "));
//		return dbr;
//	}

//	private ISASRecord recuperaTipoCuraFromPHT2(ISASConnection dbc, String nCartella, String idScheda) {
//		String punto = ver + "recuperaTipoCuraFromPHT2 ";
//		String query = "select * from pht2_generale where n_cartella = " +nCartella +" and id_scheda = "+ idScheda;
//		LOG.debug(punto + " query>>" +query);
//		ISASRecord dbrPht2Generale = null;
//		try {
//			dbrPht2Generale = dbc.readRecord(query);
//		} catch (ISASMisuseException e) {
//			LOG.error(punto + " Errore nel recuperare i dati dal pht ", e);
//		} catch (ISASPermissionDeniedException e) {
//			LOG.error(punto + " Errore nel recuperare i dati dal pht ", e);
//		} catch (DBMisuseException e) {
//			LOG.error(punto + " Errore nel recuperare i dati dal pht ", e);
//		} catch (DBSQLException e) {
//			LOG.error(punto + " Errore nel recuperare i dati dal pht ", e);
//		}
//		
//		return dbrPht2Generale;
//	}

//	public void recuperaInfoScheda(ISASConnection dbc, ISASRecord dbrRmSkso) throws ISASMisuseException,
//			ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String nCartella = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.N_CARTELLA);
//		String idSkso = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_ID_SKSO);
//		ISASRecord dbrRmsksoRead =null;
//		if (ISASUtil.valida(idSkso)){
//			dbrRmsksoRead = selectSkSoAllDatiCorrente(dbc, nCartella, idSkso);
//			recuperaDatiUltimaProroghe(dbc, nCartella, idSkso, dbrRmSkso);
//			recuperaDatiSospenzioni(dbc, nCartella, idSkso, dbrRmSkso);
//		}
//		verificaEsistenzaSchedeConcluse(dbc, nCartella, dbrRmSkso);
//		verificaStatoSchede(dbc, nCartella, idSkso, dbrRmSkso, dbrRmsksoRead);
//	}

//	private void verificaStatoSchede(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbrRmSkso,ISASRecord dbrRmSksoRead)
//			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "verificaStatoSchede ";
//		String prDataChiusura = ISASUtil.getValoreStringa(dbrRmSksoRead, "pr_data_chiusura");
//		String query = "";
//		if (ManagerDate.validaData(prDataChiusura)) {
//			dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_CONCLUSA);
//		} else {
//			if (ISASUtil.valida(idSkso)) {
//				query = recuperaQuerySospensione(nCartella, idSkso);
//				LOG.trace(punto + " query>>" + query);
//				ISASRecord dbrSospensione = dbc.readRecord(query);
//				if (dbrSospensione != null) {
//					dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_SOSPESA);
//				} else {
//					query = recuperaQueryProroghe(nCartella, idSkso);
//					LOG.trace(punto + " query>>" + query);
//					ISASRecord dbrProroghe = dbc.readRecord(query);
//					if (dbrProroghe != null) {
//						dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_PROROGA);
//					} else {
//						boolean pianoScaduto = scadutoPiano(dbrRmSksoRead);
//						String dataPresaCaricoSkso = ISASUtil.getValoreStringa(dbrRmSksoRead, CostantiSinssntW.CTS_DATA_PRESA_CARICO_SKSO);
//						if(!ManagerDate.validaData(dataPresaCaricoSkso)){
//							dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_IN_DEFINIZIONE);
//						}else if(!pianoScaduto){
//							String stato= verificaStatoSchedaRsa(dbc, dbrRmSkso);
//							dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, stato);
////							dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_ATTIVA);
//						}else{
//							dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_SCADUTA);
//						}
//					}
//				}
//			}else {
//				dbrRmSkso.put(CostantiSinssntW.CTS_SKSO_STATO, CostantiSinssntW.CTS_SKSO_STATO_NUOVA);
//			}
//		}
//	}
	
//	public String recuperaQueryProroghe(String nCartella, String idSkso) {
//		String query;
//		query = "select * from rm_skso_proroghe where n_cartella = "
//				+ nCartella
//				+ " and id_skso = "
//				+ idSkso
//				+ " and dt_proroga_inizio  <= sysdate and (dt_proroga_fine >= sysdate or dt_proroga_fine is null) ";
//		return query;
//	}

//	public String recuperaQuerySospensione(String nCartella, String idSkso) {
//		String query;
//		query = "select * from rm_skso_sospensioni where n_cartella = "
//				+ nCartella
//				+ " and id_skso = "
//				+ idSkso
//				+ " and dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null) ";
//		return query;
//	}

//	private boolean scadutoPiano(ISASRecord dbrRmSkso) {
//		boolean scaduta = false;
//		String punto = ver + "scadutoPiano ";
//		String dtFine = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//		String dtAttuale = sdf.format(new Date());
//		if (ManagerDate.validaData(dtFine) && ManagerDate.validaData(dtAttuale)){
//			java.sql.Date daData = (java.sql.Date) ManagerDate.getDate(dtAttuale);
//			java.sql.Date aData = (java.sql.Date) ManagerDate.getDate(dtFine);
//			if (daData.after(aData)) {
//				scaduta = true;
//			}
//		}
//		LOG.trace(punto + " Data non valida>"+scaduta+"<");
//		
//		return scaduta;
//	}

//	private void verificaEsistenzaSchedeConcluse(ISASConnection dbc, String nCartella, ISASRecord dbrRmSkso)
//			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//
//		if (ISASUtil.valida(nCartella)) {
//			String query = " select count(*) numero from rm_skso where n_cartella = " + nCartella
//					+ " and  pr_data_chiusura IS NOT NULL ";
//			ISASRecord dbrRmSksoSto = dbc.readRecord(query);
//			dbrRmSkso.put(CostantiSinssntW.CTS_NUMERO_SKSO_CONCLUSE, ISASUtil.getValoreStringa(dbrRmSksoSto, "numero"));
//		}
//	}
//
//	private void recuperaDatiUltimaProroghe(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbrRmSkso)
//			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		if (ISASUtil.valida(nCartella) && ISASUtil.valida(idSkso)) {
////			String query = " select  x.* from rm_skso_proroghe x where x.n_cartella = " + nCartella + " and id_skso = "
////					+ idSkso + " and  ( dt_proroga_fine in ( select max(r.dt_proroga_fine) "
////					+ " from rm_skso_proroghe r where r.n_cartella = x.n_cartella and r.id_skso = x.id_skso ) or"
////					+ " dt_proroga_fine is null )";
////			ISASRecord dbrProroghe = dbc.readRecord(query);
////			if (dbrProroghe !=null){
////				dbrRmSkso.put(CostantiSinssntW.CTS_DATA_ULTIMA_PROROGA_SKSO, ISASUtil.getValoreStringa(dbrProroghe, "dt_proroga_fine"));
////			}
//			RMSkSOSKSoProrogheEJB rmSkSOSKSoProrogheEJB = new RMSkSOSKSoProrogheEJB();
//			String dtMaxProroga = rmSkSOSKSoProrogheEJB.getMaxDataProroga(dbc, nCartella, idSkso);
//			if (ManagerDate.validaData(dtMaxProroga)){
//				dbrRmSkso.put(CostantiSinssntW.CTS_DATA_ULTIMA_PROROGA_SKSO, dtMaxProroga);
//			}
//		}
//	}
//
//	private void recuperaDatiSospenzioni(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbrRmSkso) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "recuperaDatiSospenzioni ";
//		if (ISASUtil.valida(nCartella) && ISASUtil.valida(idSkso)) {
//			String query = " SELECT * FROM rm_skso_sospensioni WHERE  n_cartella = " + nCartella 
//					+ " and id_skso = " + idSkso + " and dt_sospensione_inizio <= SYSDATE and " 
//					+ " (dt_sospensione_fine >= SYSDATE  OR dt_sospensione_fine IS NULL) ";
//			LOG.trace(punto + " query>" + query);
//			ISASRecord dbrSospensione = dbc.readRecord(query);
//			if (dbrSospensione !=null){
//				dbrRmSkso.put(CostantiSinssntW.CTS_SOSPENSIONE_SKSO_DT_INIZIO, ISASUtil.getValoreStringa(dbrSospensione, "dt_sospensione_inizio"));
//				dbrRmSkso.put(CostantiSinssntW.CTS_SOSPENSIONE_SKSO_DT_FINE, ISASUtil.getValoreStringa(dbrSospensione, "dt_sospensione_fine"));
//			}
//		}
//	}
	
	
//	private String recuperaQuery(String nCartella, String idSkso) {
//		return recuperaQuery(nCartella, idSkso, "");
//	}
//	
//	private String recuperaQuery(String nCartella, String idSkso, String dtAssistitoChiusura) {
//		String query;
//		if (ISASUtil.valida(idSkso)) {
//			query = "SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + nCartella + " and id_skso = " + idSkso;
//		}else if(ManagerDate.validaData(dtAssistitoChiusura)){
//			query = "SELECT P.* FROM RM_SKSO P WHERE P.N_CARTELLA = " + nCartella + 
//					" AND P.ID_SKSO IN (SELECT MAX(X.ID_SKSO) FROM RM_SKSO X WHERE X.N_CARTELLA = P.N_CARTELLA ) ";
//		} else {
//			query = "SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + nCartella + " AND p.pr_data_chiusura IS NULL";
//		}
//		return query;
//	}

//	public Vector<ISASRecord> recuperaPrestazioniPai(ISASConnection dbc, String nCartella, String idSkso, String tipoCura)
//			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException, Exception {
//		String punto = ver + "recuperaPrestazioniPai ";
//		Vector<ISASRecord> vdbg = new Vector<ISASRecord>();
//		ISASCursor dbgriglia = null;
//		String selg = "Select i.*,p.prest_des, p.prest_tipo FROM " + " pai i,prestaz p WHERE " + "i.N_CARTELLA ='"
//				+ nCartella + "' and " + "i.id_Skso= " + idSkso + " and " + "i.prest_cod = p.prest_cod " +
//						" AND tipoCura = " + tipoCura + 
//						" ORDER BY i.prest_cod ";
//		dbgriglia = dbc.startCursor(selg);
//		if (dbgriglia != null && dbgriglia.getDimension() > 0) {
//			ISASRecord tipDef = null;
//			while (dbgriglia.next()) {
//				ISASRecord dbr_rich = dbgriglia.getRecord();
//
//				// recuperare il codice figura professionale associata alla
//				// prestazione da conf e poi
//				// la descrizione della fig_prof da gestTpOp.
//				String myselectconf = "Select conf_key  from conf where "
//						+ "conf_kproc ='SINS' and conf_key <> 'TIPDEF05' and conf_key like 'TIPDEF%' "
//						+ " and conf_txt ='" + dbr_rich.get("prest_tipo") + "'"; // +"TIPDEF"+tipo+"'";
//				tipDef = dbc.readRecord(myselectconf);
//
//				String cod_fig_prof = tipDef.get("conf_key").toString().substring(6);
//				dbr_rich.put("cod_fig_prof", cod_fig_prof);
//				dbr_rich.put("figura_profesionale", ManagerOperatore.decodificaTipoOperatore(dbc,cod_fig_prof,null));
//				//decodifica l'operatore se presente
//				if(dbr_rich.get("pai_cod_operatore")!=null){
//					String desc_operatore = decodificaGenerica("operatori", "codice", dbr_rich.get("pai_cod_operatore"), "cognome", dbc)+" "+ decodificaGenerica("operatori", "codice", dbr_rich.get("pai_cod_operatore"), "nome", dbc);
//					dbr_rich.put(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE, desc_operatore);
//				}
//
//				vdbg.add(dbr_rich);
//			}
//			dbgriglia.close();
//		}
//		close_dbcur_nothrow(punto, dbgriglia);
//		return vdbg;
//	}

//	public Vector<ISASRecord> recuperaObiettiviPai(ISASConnection dbc, String nCartella, String idSkso)
//			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException {
//		String punto = ver + "recuperaObiettiviPai ";
//		Vector<ISASRecord> vdbg = new Vector<ISASRecord>();
//		ISASCursor dbgriglia = null;
//		String selg = " Select * " +
//					  " FROM obiettivi_pai i" +
//					  " WHERE i.N_CARTELLA ='" + nCartella + "' and " + "i.id_Skso= " + idSkso + 
//					  " ORDER BY i.livello, i.obiettivo ";
//		dbgriglia = dbc.startCursor(selg);
//		if (dbgriglia != null && dbgriglia.getDimension() > 0) {
//			vdbg.addAll(dbgriglia.getAllRecord());
////			ISASRecord tipDef = null;
////			while (dbgriglia.next()) {
////				ISASRecord dbr_rich = dbgriglia.getRecord();
////
////				// recuperare il codice figura professionale associata alla
////				// prestazione da conf e poi
////				// la descrizione della fig_prof da gestTpOp.
////				String myselectconf = "Select conf_key  from conf where "
////						+ "conf_kproc ='SINS' and conf_key <> 'TIPDEF05' and conf_key like 'TIPDEF%' "
////						+ " and conf_txt ='" + dbr_rich.get("prest_tipo") + "'"; // +"TIPDEF"+tipo+"'";
////				tipDef = dbc.readRecord(myselectconf);
////				String cod_fig_prof = tipDef.get("conf_key").toString().substring(6);
////				dbr_rich.put("cod_fig_prof", cod_fig_prof);
////				dbr_rich.put("figura_profesionale", ManagerOperatore.decodificaTipoOperatore(dbc,cod_fig_prof,null););
////				vdbg.add(dbr_rich);
////			}
//			dbgriglia.close();
//		}
//		close_dbcur_nothrow(punto, dbgriglia);
//		return vdbg;
//	}
	
	public ISASRecord selectZonaSkValCorrente(myLogin mylogin, Hashtable h) throws CariException {
		String punto = nomeEJB + "selectSkValCorrente ";
		LOG.info(punto + " dati da esaminare>>" + (h != null ? h + "" : " no dati "));
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		String nCartella = (String) h.get(CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		
		try {
			dbc = super.logIn(mylogin);
			String query = "";
			if (ISASUtil.valida(idSkso)) {
				query = "SELECT (select a.descrizione_zona from zone a where a.jisas_gid = p.jisas_gid) zona_desc," +
						"(select a.codice_zona from zone a where a.jisas_gid = p.jisas_gid) zona_cod," +
						"jisas_gid gid,id_skso, cod_distretto_verbale, cod_distretto FROM rm_skso p " +
						"WHERE p.n_cartella = " + nCartella + " " +
						"and id_skso = " + idSkso+ " " 
						//+"and pr_data_verbale_uvm is not null"
						;
			} else {
				query = "SELECT (select a.descrizione_zona from zone a where a.jisas_gid = p.jisas_gid) zona_desc," +
						"(select a.codice_zona from zone a where a.jisas_gid = p.jisas_gid) zona_cod," +
						"p.jisas_gid gid, p.id_skso, p.cod_distretto_verbale, g.cod_distretto  FROM rm_skso p, rm_skso_mmg g " +
						"WHERE  p.n_cartella = g.n_cartella and p.id_skso = g.id_skso and p.n_cartella = " + nCartella + " " +
						"AND p.pr_data_chiusura IS NULL " 
						//+"and pr_data_verbale_uvm is not null"
						;
			}

			LOG.trace(punto + " query>>" + query + "]");
			dbr = dbc.readRecord(query);

		} catch (Exception e) {
			LOG.trace(punto + " Errore nel recuperare la zona dell'ultima scheda ", e);
			
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbr;
	}
	
	
//	private void decodificaStatoPrimaVisita(ISASRecord dbr) throws ISASMisuseException {
//		String punto = ver + "decodificaStatoPrimaVisita ";
//		LOG.trace(punto + " decodifico stato operatore prima visita ");
//		String vistaDaSoDescr = "";
//		String vistaDaSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO);
//		if (ISASUtil.valida(vistaDaSo)){
//			if (vistaDaSo.equals(CostantiSinssntW.CTS_FLAG_STATO_FATTA)){
//				LOG.trace(punto + "Commentata: PRESA IN CARICO ");
////				vistaDaSoDescr = " PRESA IN CARICO ";
//			}else {
//				if (vistaDaSo.equals(CostantiSinssntW.CTS_FLAG_STATO_VISTA)){
////					vistaDaSoDescr = " VISTA IN SO ";
//					LOG.trace(punto + "Commentata: VISTA IN SO ");
//				}else {
//					if (vistaDaSo.equals(CostantiSinssntW.CTS_FLAG_STATO_RIMOSSA)){
//						vistaDaSoDescr = " ANNULLATA ";
//					}
//				}
//			}
//		}
//		dbr.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO+"_descr", vistaDaSoDescr);
//	}

	
//	private ISASRecord aggiornaVistaDaSO(ISASConnection dbc, ISASRecord dbr, String nCartella, String idSkso) throws Exception {
//		String punto = ver + "aggiornaVistaDaSO ";
//		LOG.trace(punto + " aggiorno che Ã¨ stata vista dalla SO ");
//		String dtPvVisita = ISASUtil.getValoreStringa(dbr, "pv_dt_visita");
//		RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
//		if (dbr!=null && ManagerDate.validaData(dtPvVisita) ){
////			dbr.put("vista_da_so", "S");
////			dbr.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, CostantiSinssntW.CTS_FLAG_STATO_VISTA);
//			dbr = aggiornaVistaDaSo(dbc, nCartella, idSkso, CostantiSinssntW.CTS_FLAG_STATO_VISTA);
//			LOG.trace(punto + " dati che aggiorno>>"+ (dbr!=null ? dbr.getHashtable()+"": "no dati "));
//			dbc.writeRecord(dbr);
//			String query = "select * from rm_skso where n_cartella = " +nCartella + 
//					" and id_skso = " +idSkso;
//			LOG.trace(punto + " query>>" +query );
//			dbc.readRecord(query);
//			dbr = dbc.readRecord(query);
//		
//			/*
//			 * TODO SISTEMARE 
//			Hashtable<String, String> dati = new Hashtable<String, String>();
//			dati.put(CostantiSinssntW.CTS_OP_INSERIRE_PV, CostantiSinssntW.CTS_SI);
//			rmSkSOOpCoinvoltiEJB.inserisciOperatoriFigure(dati, dbc, nCartella, idSkso);
//			
//			if (possoInserireAccessi(dbc, nCartella, idSkso)){
//				dati.put(CostantiSinssntW.CTS_OP_INSERIRE_MMG, CostantiSinssntW.CTS_SI);
//				rmSkSOOpCoinvoltiEJB.inserisciOperatoriFigure(dati, dbc, nCartella, idSkso);
//			}else {
//				LOG.trace(punto + " non inserisco l'mmg");
//			}
//			 */
//			
//		}
//		rmSkSOOpCoinvoltiEJB.aggiornaOpCoinvoltiVistaSO(dbc, nCartella, idSkso);
//		
//		return dbr;
//	}

//	private boolean possoInserireAccessi(ISASConnection dbc, String nCartella,
//			String idSkso) {
//		String punto = ver + "possoInserireAccessi ";
//		ISASRecord dbrRmsksoMMG;
//		boolean possoInserire = false;
//		String query = "select * from rm_skso_mmg where n_cartella = " +nCartella +" and id_skso = " +
//				idSkso +" and accessi_mmg > 0 ";
//		LOG.trace(punto + " query>"+query);
//		try {
//			dbrRmsksoMMG = dbc.readRecord(query);
//			possoInserire = (dbrRmsksoMMG!=null);
//		} catch (ISASMisuseException e) {
//			e.printStackTrace();
//		} catch (ISASPermissionDeniedException e) {
//			e.printStackTrace();
//		} catch (DBMisuseException e) {
//			e.printStackTrace();
//		} catch (DBSQLException e) {
//			e.printStackTrace();
//		}
//		LOG.trace(punto + " possoInserire>>"+ possoInserire);
//		return possoInserire;
//	}


//	public ISASRecord aggiornaVistaDaSo(ISASConnection dbc, String nCartella, String idSkso, String vistaDaSo) throws ISASException, 
//		ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException{
//		String punto = ver + "aggiornaVistaDaSo ";
//		LOG.trace(punto + " inizio con dati ");
//		String query = "select * from rm_skso where n_cartella = " +nCartella + 
//				" and id_skso = " +idSkso;
//		LOG.trace(punto + " query>>"+ query);
//		ISASRecord dbrRmSkso = dbc.readRecord(query);
//		if (dbrRmSkso!=null) {
//			dbrRmSkso.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, vistaDaSo);
//			dbc.writeRecord(dbrRmSkso);
//			dbrRmSkso = dbc.readRecord(query);
//		}
//		return dbrRmSkso;
//	}

//	public ISASRecord selectSkSoCorrente(myLogin mylogin, Hashtable h) throws CariException {
//		String punto = nomeEJB + "selectSkSoCorrente ";
//		LOG.info(punto + " dati da esaminare>>" + (h != null ? h + "" : " no dati "));
//		ISASConnection dbc = null;
//		ISASRecord dbr = null;
//		String nCartella = (String) h.get("n_cartella");
//		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//		try {
//			dbc = super.logIn(mylogin);
//			dbr = selectSkSoCorrente(dbc, nCartella, idSkso);
//			
//		} catch (ISASPermissionDeniedException e) {
//			LOG.trace(punto + " Errore " + e);
//			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
//		} catch (Exception e) {
//			LOG.trace(punto + " Errore nel recuperare l'ultima scheda ", e);
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return dbr;
//	}

//	private ISASRecord selectSkSoCorrente(ISASConnection dbc, String nCartella, String idSkso)
//			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "selectSkSoCorrente ";
//		ISASRecord dbr ;
//		String query = recuperaQuery(nCartella, idSkso);
//
//		LOG.trace(punto + " query>>" + query + "]");
//		dbr = dbc.readRecord(query);
//		
//		LOG.debug(punto + " dati recuperati>>" + (dbr != null ? dbr.getHashtable() + "" : " no dati "));
//		return dbr;
//	}

//	private ISASRecord selectSkSoAllDatiCorrente(ISASConnection dbc, String nCartella, String idSkso)
//			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "selectSkSoCorrente ";
//		ISASRecord dbr ;
//		String query;
//		if (ISASUtil.valida(idSkso)) {
//			query = "SELECT p.*, g.* FROM rm_skso p, rm_skso_mmg g WHERE p.n_cartella = g.n_cartella and p.id_skso = g.id_skso " +
//					" and p.n_cartella = " +nCartella + " and p.id_skso = " +idSkso;
//		} else {
//			query = "SELECT p.*, g.* FROM rm_skso p, rm_skso_mmg g WHERE p.n_cartella = g.n_cartella and p.id_skso = g.id_skso " +
//					" and p.n_cartella = " +nCartella + " and p.id_skso = " +idSkso +" AND p.pr_data_chiusura IS NULL";
//		}
//		LOG.trace(punto + " query>>" + query + "]");
//		dbr = dbc.readRecord(query);
//		
//		LOG.debug(punto + " dati recuperati>>" + (dbr != null ? dbr.getHashtable() + "" : " no dati "));
//		return dbr;
//	}
//	
//	private void leggiPatologie(ISASConnection dbc, ISASRecord dbr) throws Exception {
//		String punto = ver + "leggiPatologie ";
//		LOG.debug(punto + " inizio con dati>>" + (dbr!=null ? dbr.getHashtable()+"": "no dati "));
//		String dtSkmDataApertura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SKM_DATA_APERTURA);
//		
//		if (!ManagerDate.validaData(dtSkmDataApertura)){
//			dbr.put(CostantiSinssntW.CTS_SKM_DATA_APERTURA, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_PR_DATA_PUAC));
//		}
//		
//		SkMedEJB skMedEJB = new SkMedEJB();
//		skMedEJB.leggiDiagnosi(dbc, dbr);
//	}
//
//	private void recuperaInfoAnagrafica(ISASConnection dbc, String nCartella, ISASRecord dbr) {
//		String punto = nomeEJB + "recuperaInfoAnagrafica ";
//		RmRichiesteMMGEJB richiesteMMGEJB = new RmRichiesteMMGEJB();
//		Hashtable dati = new Hashtable();
//		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
//		try {
////			ISASRecord dbrAnagrafica = richiesteMMGEJB.getInfoAnagrafica(dbc, dati);
//			ISASRecord dbrAnagrafica =  recuperaInfoAnagrafica(dbc, dbr, nCartella);
//			copiaDati(dbr, dbrAnagrafica);
//			leggiPatologie(dbc, dbrAnagrafica);
//			if (dbrAnagrafica!=null && dbrAnagrafica.get("diagn_associate")!=null){
//				dbr.put("diagn_associate", dbrAnagrafica.get("diagn_associate"));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			LOG.error(punto + " Errore nel recuperare i dati ", e);
//		}
//	}

//	private void decodificaDati(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = nomeEJB + "decodificaDati ";
//		recuperaDescrizioneMedico(dbc, dbr);
//		recuperaDatiOperatore(dbc, dbr);
//		recuperaSePresenteMMG(dbc,dbr);
//		recuperaInfoUltimaValutazioneUvi(dbc, dbr);
//	}

//	private void recuperaInfoUltimaValutazioneUvi(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException,
//			DBMisuseException, DBSQLException, ISASPermissionDeniedException {
//		EsitiValutazioniUviEJB esitiValutazioniUviEJB = new EsitiValutazioniUviEJB();
//		esitiValutazioniUviEJB.recuperaUltimaValutazioneUvi(dbc, dbr);
//	}

//	private void recuperaSePresenteMMG(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
//		rmSkSOOpCoinvoltiEJB.verificaOperatoriPresenti(dbc, dbr);
//	}


//	private void recuperaDatiOperatore(ISASConnection dbc, ISASRecord dbr) {
//		String punto = nomeEJB +"recuperaDatiOperatore ";
//		String codCaseManager = ISASUtil.getValoreStringa(dbr, "cod_case_manager");
//		String codOpPrimaVisita = ISASUtil.getValoreStringa(dbr, "pv_cod_operatore");
//		String descrCaseManager = "";
//		String codOpPrimaVisitaDescr ="";
//		String telefono = "";
//
//		try {
//			if (ISASUtil.valida(codCaseManager)) {
//				ISASRecord dbrOperatori = recuperaOperatore(dbc, codCaseManager);
//				descrCaseManager = ISASUtil.getValoreStringa(dbrOperatori,
//						"cognome");
//				descrCaseManager += (ISASUtil.valida(descrCaseManager) ? " "
//						: "") + ISASUtil.getValoreStringa(dbrOperatori, "nome");
//				telefono = ISASUtil.getValoreStringa(dbrOperatori, "telefono1");
//			}
//			if (ISASUtil.valida(codOpPrimaVisita)) {
//				ISASRecord dbrOperatore= recuperaOperatore(dbc, codOpPrimaVisita);
//				codOpPrimaVisitaDescr = ISASUtil.getValoreStringa(dbrOperatore,
//						"cognome");
//				codOpPrimaVisitaDescr += (ISASUtil.valida(descrCaseManager) ? " "
//						: "") + ISASUtil.getValoreStringa(dbrOperatore, "nome");
//			}
//			dbr.put("cod_case_manager_descr", descrCaseManager);
//			dbr.put("telefono1", telefono);
//			dbr.put("pv_cod_operatore_descr", codOpPrimaVisitaDescr);
//		} catch (Exception e) {
//			e.printStackTrace();
//			LOG.error(punto + " Errore nel recuperare i dati dell'operatore "+e);
//		}
//
//	}
//
//	private ISASRecord recuperaOperatore(ISASConnection dbc,
//			String codCaseManager) throws ISASMisuseException,
//			ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String query = "select * from operatori where codice = '"
//				+ codCaseManager + "' ";
//		ISASRecord dbrOperatori = dbc.readRecord(query);
//		return dbrOperatori;
//	}
//
//	private String recuperaDescrizioneMedico(ISASConnection dbc, ISASRecord dbr) {
//		String punto = nomeEJB + "";
//		String descrizioneMedico = "";
//		String codMed = ISASUtil.getValoreStringa(dbr, "cod_med");
//		
//		if (ISASUtil.valida(codMed)){
//			String query = " select * from medici where mecodi = '" +codMed+"' ";
//			LOG.trace(punto + " query>>" +query);
//			try {
//				ISASRecord dbrMedici = dbc.readRecord(query);
//				descrizioneMedico = ISASUtil.getValoreStringa(dbrMedici, "mecogn");
//				descrizioneMedico += " "+ ISASUtil.getValoreStringa(dbrMedici, "menome");
//				dbr.put(CostantiSinssntW.CTS_MEDICO_DESCRIZIONE, descrizioneMedico);
//				
//				dbr.put("metel_amb", ISASUtil.getValoreStringa(dbrMedici, "metel_amb"));
//				dbr.put("metel_cell", ISASUtil.getValoreStringa(dbrMedici, "metel_cell"));
//				
//			} catch (Exception e) {
//				LOG.error(punto + " Errore ");
//			}
//		}
//		
//		return descrizioneMedico;
//	}

//	private void recuperaSkSoMMG(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbr) throws ISASMisuseException,
//			ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "recuperaSkSoMMG ";
//		String query = "select * from rm_skso_mmg where n_cartella = " + nCartella + "  and id_skso = " + idSkso;
//		LOG.trace(punto + " query>>" + query + "<");
//		ISASRecord dbrSkSoMMG = dbc.readRecord(query);
//		if (dbrSkSoMMG != null) {
//			LOG.trace(punto + " dati recuperati>>" + dbrSkSoMMG.getHashtable() + "<");
//			copiaDati(dbr, dbrSkSoMMG);
//		}
//		LOG.trace(punto + " dati recuperati>>" + dbr.getHashtable() + "<");
//	}

//	private void copiaDati(ISASRecord destinazione, ISASRecord sorgente) throws ISASMisuseException {
//		String punto = nomeEJB + "copiaDati ";
//		
//		if (sorgente != null) {
//			Enumeration d = sorgente.getHashtable().keys();
//			String key;
//			LOG.trace(punto + " dati da copiare>>>" + (sorgente != null ? sorgente.getHashtable() + "" : " no dati "));
//			while (d.hasMoreElements()) {
//				key = (String) d.nextElement();
//				destinazione.put(key, sorgente.get(key));
//			}
//		}
//		LOG.trace(punto + " record>>>" + (destinazione != null ? destinazione.getHashtable() + "" : " no dati "));
//	}

	public Vector query_skVal_chiuse(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "query_skVal_chiuse ";
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String query = "select * from rm_skso where n_cartella = " + nCartella + " and pr_data_chiusura is not null";
			LOG.trace(punto + " query>> " + query + "]");
			dbcur = dbc.startCursor(query);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			String prMotivoChiusura = "";
			for (Enumeration e = vdbr.elements(); e.hasMoreElements();) {
				ISASRecord dbr_1 = (ISASRecord) e.nextElement();
				if (dbr_1 != null) {
					prMotivoChiusura = ISASUtil.getValoreStringa(dbr_1, "pr_motivo_chiusura");
					dbr_1.put("pr_motivo_chiusura_descr", decodificaTabVoci(dbc, prMotivoChiusura, CostantiSinssntW.TAB_VAL_MOTIVO_CONCLUSIONE_FLUSSI_SIAD));
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		} catch (Exception e) {
			System.out.println("SkValutazEJB: query_skVal_chiuse - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("SkValutazEJB: query_skVal_chiuse - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query_skVal_chiuse

//	private String decodificaTabVoci(ISASConnection mydbc, String val, String codice) throws Exception {
//		String strDescrizione = "";
//		if ((val != null) && (!val.trim().equals(""))) {
//			String selS = "SELECT tab_descrizione" + " FROM tab_voci" + " WHERE tab_cod = '" + codice + "'" + " AND tab_val = '" + val
//					+ "'";
//			ISASRecord rec = mydbc.readRecord(selS);
//			if (rec != null)
//				strDescrizione = (String) rec.get("tab_descrizione");
//			if (strDescrizione == null)
//				strDescrizione = "";
//		}
//		return strDescrizione;
//	}

//	private Hashtable recuperaInfoFromRichiesteMMG(ISASConnection dbc, String nCartella, String idRich) throws Exception {
//		String punto = nomeEJB + "recuperaInfoFromRichiesteMMG ";
//		Hashtable datiRecuperati = new Hashtable();
//		if (ISASUtil.valida(nCartella) && ISASUtil.valida(idRich)) {
//			String query = "select * from rm_rich_mmg where n_cartella = " + nCartella + " and id_rich = " + idRich;
//			LOG.debug(punto + " query>>" + query);
//			ISASRecord dbrRmRichMMG = dbc.readRecord(query);
//			if (dbrRmRichMMG != null) {
//				recuperaPatologie(dbc, dbrRmRichMMG);
//				datiRecuperati = dbrRmRichMMG.getHashtable();
//			}
//		}
//		LOG.trace(punto + " dati che invio>>" + datiRecuperati + "");
//		
//		return datiRecuperati;
//	}

//	private void recuperaPatologie(ISASConnection dbc, ISASRecord dbrRmRichMMG) throws Exception {
//		String punto = ver + "recuperaPatologie ";
//		SkMedEJB skMedEJB = new SkMedEJB();
//		skMedEJB.decodificaDiagn(dbc, dbrRmRichMMG);
//		
//		Vector vdbr = new Vector(); 
//		dbrRmRichMMG.put(CostantiSinssntW.CTS_DATA_DIAG, (java.sql.Date)dbrRmRichMMG.get(CostantiSinssntW.CTS_DATA_DIAG));
//		skMedEJB.costruisci5Rec(dbc, dbrRmRichMMG, vdbr, "0");
//		LOG.trace(punto + " Elementi recuperati>>" +(vdbr !=null ? vdbr.size()+"": " no dati "));
//		dbrRmRichMMG.put("diagn_associate", vdbr);
//	}

	private void putInfoFromCartellaIntoDbr(ISASConnection dbc, ISASRecord dbr, String nCartella) throws Exception {
		java.sql.Date dateDataApertura = null;
		java.sql.Date dateDataChiusura = null;

		String mySel = "SELECT data_apertura, data_chiusura" + " FROM cartella" + " WHERE n_cartella = " + nCartella;

		ISASRecord rec = dbc.readRecord(mySel);

		if (rec != null) {
			if (rec.get("data_apertura") != null) {
				dateDataApertura = (java.sql.Date) rec.get("data_apertura");
				dbr.put("data_apertura_cartella", dateDataApertura);
			}
			if (rec.get("data_chiusura") != null) {
				dateDataChiusura = (java.sql.Date) rec.get("data_chiusura");
				dbr.put("data_chiusura_cartella", dateDataChiusura);
			}
		}
		
		recuperaInfoAnagrafica(dbc, dbr, nCartella);
	}

	private ISASRecord recuperaInfoAnagrafica(ISASConnection dbc, ISASRecord dbr, String nCartella) throws Exception,
			ISASMisuseException {
		ISASRecord dbrAnagrafica =null;
		
		ManagerAnagraC mac = new ManagerAnagraC();
		dbrAnagrafica = mac.getInfoAnagrafica(dbc, nCartella);
		dbr.put(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO+"_desc", ISASUtil.getValoreStringa(dbrAnagrafica, CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO+"_desc"));
		dbr.put(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO+"_anagra", ISASUtil.getValoreStringa(dbrAnagrafica, CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO));
		
		return dbrAnagrafica;
	}

	// 08/01/08
	private void decodAllOper(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		decodOper(mydbc, mydbr, "pr_oper_ultmod", "ultmod");

		leggiMed(mydbc, mydbr);
		decodMed(mydbc, mydbr, "cod_med", "sk1");
	}

	// 08/01/08
	private void decodOper(ISASConnection mydbc, ISASRecord mydbr, String nomeCampo, String nomeDesc) throws Exception {
		decodOperAndMed(mydbc, mydbr, "operatori", "codice", "cognome", "nome", nomeCampo, nomeDesc);
	}

	// 08/01/08
	private void decodMed(ISASConnection mydbc, ISASRecord mydbr, String nomeCampo, String nomeDesc) throws Exception {
		decodOperAndMed(mydbc, mydbr, "medici", "mecodi", "mecogn", "menome", nomeCampo, nomeDesc);
	}

	// 08/01/08
	private void decodOperAndMed(ISASConnection mydbc, ISASRecord mydbr, String nomeTab, String nomeCampoCod, String nomeCampoCogn,
			String nomeCampoNome, String nomeCampo, String nomeDesc) throws Exception {
		String cod = (String) mydbr.get(nomeCampo);
		String nomeD = "decod_" + nomeDesc;
		String cognOpe = "";
		String nomeOpe = "";

		if ((cod != null) && (!cod.trim().equals(""))) {
			String sel = "SELECT NVL(" + nomeCampoCogn + ", '') cogn_ope," + " NVL(" + nomeCampoNome + ", '') nome_ope" + " FROM "
					+ nomeTab + " WHERE " + nomeCampoCod + " = '" + cod + "'";

			ISASRecord dbr_1 = mydbc.readRecord(sel);
			if (dbr_1 != null) {
				cognOpe = (String) dbr_1.get("cogn_ope");
				nomeOpe = (String) dbr_1.get("nome_ope");
			}
		}
		mydbr.put((nomeD + "_cogn_ope"), cognOpe);
		mydbr.put((nomeD + "_nome_ope"), nomeOpe);
		// 03/03/08: x la griglia -> unica descrizione
		mydbr.put((nomeDesc + "_desc"), cognOpe + " " + nomeOpe);
	}

	// 08/01/08
	private void leggiMed(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String sel = "SELECT a.* FROM anagra_c a" + " WHERE a.n_cartella = " + mydbr.get("n_cartella")
				+ " AND a.data_variazione IN (SELECT MAX(anagra_c.data_variazione)"
				+ " FROM anagra_c WHERE anagra_c.n_cartella = a.n_cartella)";

		ISASRecord dbr_1 = mydbc.readRecord(sel);
		if ((dbr_1 != null) && (((String) dbr_1.get("cod_med")) != null))
			mydbr.put("cod_med", (String) dbr_1.get("cod_med"));
	}

	// 04/03/09
	private void decodTabPap(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String desc = util.getDecode(mydbc, "tab_pap", "codice", mydbr.get("pr_pianoint"), "descrizione");
		mydbr.put("desc_pianoint", desc);
	}

	

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM rl_puauvm" + " WHERE n_cartella = " + (String) h.get("n_cartella") + " AND pr_data = "
					+ formatDate(dbc, (String) h.get("pr_data"));

			mySystemOut("query=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: query - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("RLSkPuacEJB: query - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query

	// 22/02/08: aggiunto progr in key
	private int getProgressivo(ISASConnection mydbc, Hashtable h0) throws Exception {
		String punto = nomeEJB + "getProgressivo ";
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		//		String myselect = "SELECT MAX(pr_progr) max_progr FROM rl_puauvm" +
		//		" WHERE n_cartella = " + (String)h0.get("n_cartella") +
		//		" AND pr_data = " + formatDate(mydbc, (String)h0.get("pr_data"));

		String query = "SELECT MAX(id_skso) max_progr FROM rm_skso " + " WHERE n_cartella = " + (String) h0.get("n_cartella");
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbr = mydbc.readRecord(query);
		if (dbr != null)
			intProgressivo = u.getIntField(dbr, "max_progr");

		intProgressivo++;
		return intProgressivo;
	}

	private boolean dtApeChiusPuac(ISASConnection dbc, Hashtable h) throws Exception {
		String strNCartella = (String) h.get("n_cartella");
		String strDataApeContatto = (String) h.get("pr_data_puac");
		if (strDataApeContatto == null) // 19/06/09: x chiamata da ElencoCasiPuac
			strDataApeContatto = (String) h.get("pr_data_richiesta");
		String pr_progetto = h.get("pr_data").toString();

		String mySel = "SELECT * FROM rl_puauvm WHERE n_cartella = " + strNCartella + " AND pr_data = " + formatDate(dbc, pr_progetto)
				+ (h.get("pr_progr") != null ? " AND pr_progr <> " + h.get("pr_progr").toString() : "") + " AND ((pr_data_verbale_uvm >= "
				+ formatDate(dbc, strDataApeContatto) + " AND pr_data_verbale_uvm IS NOT NULL) " + " OR (pr_data_chiusura >= "
				+ formatDate(dbc, strDataApeContatto) + " AND pr_data_chiusura IS NOT NULL)) ";

		mySystemOut("dtApeChiusPuac -- " + mySel);
		ISASCursor dbcur;
		try {
			dbcur = dbc.startCursor(mySel);
			if ((dbcur != null) && (dbcur.getDimension() > 0))
				return true;
			else
				return false;

		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: dtApeChiusPuac - Eccezione= " + e);
			throw e;
		}
	}

	// 19/06/09: x chiamata da ElencoCasiPuac
	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
			CariException {
		return insertConScale(mylogin, h, null);
	}

	// Inserimento su tabella rl_puauvm ed, eventualmente, PROGETTO
	public ISASRecord insertConScale(myLogin mylogin, Hashtable h, Vector vettSc) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		String punto = nomeEJB + "insertConScale ";
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : "no dati "));
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction(); // 26/02/08
			dbr = execInsert(dbc, h, punto, mylogin);
			aggiornaStatoPht2(dbc, h);
			aggiornaStatoPua(dbc, h);
			aggiornaStatoZkRsaRichiesta(dbc, dbr.getHashtable());
			
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			return dbr;
		} catch (CariException ce) {
			LOG.trace(punto + " Errore nei dati " + ce);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new CariException("Errore eseguendo la rollback() - " + e1);
			}
			throw ce;
		} catch (DBRecordChangedException e) {
			LOG.trace(punto + " Errore nei dati " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo la rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			try {
				LOG.trace(punto + " Errore nei permessi  " + e);
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo la rollback() - " + e1);
			}
			throw e;
		} catch (Exception e) {  
			LOG.trace(punto + " Errore generico " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + e1);
			}
			throw newEjbException("Errore eseguendo una insert() - ", e);
//			throw new SQLException("Errore eseguendo una insert() - " + e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

	private ISASRecord execInsert(ISASConnection dbc, Hashtable h, String punto, myLogin mylogin)
			throws ISASMisuseException, DBMisuseException, DBSQLException, Exception, ISASPermissionDeniedException,
			CariException, DBRecordChangedException {
		ISASRecord dbr;
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String idRich = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_RICH);
		dbr = dbc.newRecord("rm_skso");
		int idSkSo = getProgressivo(dbc, h);
		h.put("id_skso", new Integer(idSkSo));
		Enumeration n = h.keys();
		while (n.hasMoreElements()) {
			String e = (String) n.nextElement();
			dbr.put(e, h.get(e));
		}
		dbr.put("cod_presidio", ManagerOperatore.recuperaCodPresidioOperatore(dbc, mylogin.getUser()));
		
		gestisciFlag(dbc, dbr, true);
		dbr.put(CostantiSinssntW.CTS_ID_SKSO, new Integer(idSkSo));
		if (ISASUtil.valida(idRich)) {
			dbr.put(CostantiSinssntW.CTS_ID_RICH, new Integer(idRich));
		}
		LOG.trace(punto + " dati che aggiorno>>" + (dbr != null ? dbr.getHashtable() + "" : " no dati "));
		dbc.writeRecord(dbr);
		registraRichiestaMMG(dbc, nCartella, idRich, idSkSo, dbr, h);
		inserisciMMG(dbc, nCartella, idSkSo+"", dbr);
		String myselect = "SELECT * FROM rm_skso" + " WHERE n_cartella = " + nCartella + " AND id_skso = " + idSkSo;

		LOG.trace(punto + " query " + myselect);
		dbr = dbc.readRecord(myselect);

		recuperaSkSoMMG(dbc, nCartella, idSkSo + "", dbr);
		
		updateIdSksoScBisogni(dbc,h);
		
		LOG.trace(punto + " inserimento " + dbr.getHashtable().toString());

		decodificaDati(dbc, dbr);
		
		
		return dbr;
	}

	private void updateIdSksoScBisogni(ISASConnection dbc, Hashtable h) throws Exception {
		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		
		String myselect = "SELECT * FROM sc_bisogni WHERE n_cartella = "+nCartella+
							" and id_skso is null";
		
		ISASCursor dbcur = dbc.startCursor(myselect);
		
		while (dbcur.next()){
			ISASRecord dbr = dbcur.getRecord();
			ISASRecord dbrw = new SCBisogniEJB().queryKey(dbc, dbr.getHashtable());
			dbrw.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
			dbc.writeRecord(dbrw);
		}
		dbcur.close();
	}


	private void allienaPatologie(ISASConnection dbc, Hashtable h) throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException, DBRecordChangedException {
		String punto = ver + "allienaPatologie ";
		LOG.trace(punto + " inizio con dati>>"+ h);
		
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String dataDiag = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_DT_DIAG);
		if (ManagerDate.validaData(dataDiag)) {
			String query = "select * from diagnosi where n_cartella = " + nCartella + " and data_diag = "
					+ formatDate(dbc, dataDiag);
			LOG.trace(punto + " query>>" + query + "<");
			ISASRecord dbrDiagnosi = dbc.readRecord(query);
			if (dbrDiagnosi == null) {
				LOG.trace(punto + " creo record ");
				dbrDiagnosi = dbc.newRecord("diagnosi");
				dbrDiagnosi.put(CostantiSinssntW.N_CARTELLA, nCartella);
				dbrDiagnosi.put("data_diag", dataDiag);
				dbrDiagnosi.put("cod_operatore", dbc.getKuser());
			} else {
				LOG.trace(punto + " Record esistente: lo aggiorno ");
			}
			String codDiag;
			String key;
			for (int j = 1; j < 6; j++) {
				key = "diag" + j;
				codDiag = ISASUtil.getValoreStringa(h, key);
				dbrDiagnosi.put(key, codDiag);
			}
			LOG.trace(punto + " dati che aggiorno>"+(dbrDiagnosi!=null ? dbrDiagnosi.getHashtable()+"":" no dati "));
			dbc.writeRecord(dbrDiagnosi);
		}else {
			LOG.debug(punto + " Non sono state inserite le patologie ");
		}
	}

	private void settaZonaDistretto(ISASConnection dbc, ISASRecord dbr,
			String user) throws Exception {
		String punto = ver + "settaZonaDistretto ";
		String query = "";
		String codiceRegione = ISASUtil.getDecode(dbc, "conf", "conf_key", CostantiSinssntW.CTS_CONF_CODICE_REGIONE, "conf_txt");
		String codiceAzSan = ISASUtil.getDecode(dbc, "conf", "conf_key", CostantiSinssntW.CTS_CONF_CODAZSAN, "conf_txt");
		
		
		query = "SELECT o.cod_zona, p.coddistr FROM operatori o, presidi p where codice = '" +user+"' " +
				" and o.cod_presidio = p.codpres and p.codreg = '" +codiceRegione +"' and p.codazsan = '" +
						codiceAzSan +"' ";
		LOG.trace(punto + " query>> " +query);
		ISASRecord dbrOperatore = dbc.readRecord(query);
		String codZona = ISASUtil.getValoreStringa(dbrOperatore, "cod_zona");
		String codDistretto = ISASUtil.getValoreStringa(dbrOperatore, "coddistr");
		dbr.put("cod_zona", codZona);
		dbr.put("cod_distretto", codDistretto);

		LOG.trace(punto + " dati recuperati >>"+  (dbr!=null ? dbr.getHashtable()+"" : " No dati "));
		
	}

	private void registraRichiestaMMG(ISASConnection dbc, String nCartella, String idRich, int idSkSo, ISASRecord dbr, Hashtable dati)
			throws Exception {
		String punto = nomeEJB + "registraRichiestaMMG ";
		if (ISASUtil.valida(idRich)) {
			String query = "select * from rm_rich_mmg where n_cartella = " + nCartella + " and id_rich = " + idRich;
			LOG.trace(punto + " query>>" + query);

			ISASRecord dbrRmRichMMG = dbc.readRecord(query);
			if (dbrRmRichMMG != null) {
				dbrRmRichMMG.put(CostantiSinssntW.CTS_DATA_PRESA_CARICO, dbr.get(CostantiSinssntW.CTS_DATA_PRESA_CARICO_SKSO));
				dbrRmRichMMG.put(CostantiSinssntW.CTS_STATO, RmRichiesteMMGEJB.STATO_RICH_MMG_ATTIVATA);
				dbrRmRichMMG.put(CostantiSinssntW.CTS_ID_SKSO_MMG, dbr.get(CostantiSinssntW.CTS_ID_SKSO));
				
				dbrRmRichMMG.put(CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE, dbr.get(CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE));
				dbrRmRichMMG.put(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO, dbr.get(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO));
				dbrRmRichMMG.put(CostantiSinssntW.CTS_SKSO_MMG_DATA_PROTOCOLLO, dbr.get(CostantiSinssntW.CTS_SKSO_MMG_DATA_PROTOCOLLO));
				dbrRmRichMMG.put(CostantiSinssntW.CTS_SKSO_MMG_NUMERO_PROTOCOLLO, dbr.get(CostantiSinssntW.CTS_SKSO_MMG_NUMERO_PROTOCOLLO));
				
				dbc.writeRecord(dbrRmRichMMG);
				allienaPatologie(dbc, dati);
			}
		}
		aggiornaRmSkSoMMG(dbc, nCartella, idSkSo + "", dati);
		dbr.put("cod_zona", ISASUtil.getValoreStringa(dati, "cod_zona"));
		dbr.put("cod_distretto", ISASUtil.getValoreStringa(dati, "cod_distretto"));
		LOG.trace(punto + " dati che aggiorno>>" + (dbr != null ? dbr.getHashtable() + "" : " no dati "));
	}

	private void aggiornaRmSkSoMMG(ISASConnection dbc, String nCartella, String idSkso, Hashtable dati) throws Exception {
		String punto = nomeEJB + "aggiornaRmSkSoMMG ";
		boolean creazioneRecord = false;
		LOG.debug(punto + " ");

		ISASRecord dbrRmSksoMMG = recuperaRmSkSoMMG(dbc, nCartella, idSkso);
		if (dbrRmSksoMMG== null){
			LOG.trace(punto + " Creo il record ");
			dbrRmSksoMMG = dbc.newRecord("rm_skso_mmg");
			creazioneRecord = true;
		}else {
			LOG.trace(punto + " Aggiorno il record ");
		}

		Enumeration e = dati.keys();
		while (e.hasMoreElements()) {
			String chiave = e.nextElement().toString();
			if (ISASUtil.valida(chiave)) {
				dbrRmSksoMMG.put(chiave, dati.get(chiave));
			}
		}
		if (creazioneRecord){
			settaZonaDistretto(dbc, dbrRmSksoMMG, dbc.getKuser());
		}else {
			LOG.trace(punto + "NON  setto zona e distretto ");
		}
		dbrRmSksoMMG.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dbrRmSksoMMG.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
		
		dati.put("cod_zona", ISASUtil.getValoreStringa(dbrRmSksoMMG, "cod_zona"));
		dati.put("cod_distretto", ISASUtil.getValoreStringa(dbrRmSksoMMG, "cod_distretto"));
		LOG.trace(punto + " dati che aggiorno>>" + (dbrRmSksoMMG != null ? dbrRmSksoMMG.getHashtable() + "" : " no dati "));
		dbc.writeRecord(dbrRmSksoMMG);
	}

	private ISASRecord recuperaRmSkSoMMG(ISASConnection dbc, String nCartella, String idSkso) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		ISASRecord dbrRmSkSoMMG = null;

		if (ISASUtil.valida(idSkso)) {
			String query = "select * from rm_skso_mmg where n_cartella = " + nCartella + " and id_skso = " + idSkso;
			dbrRmSkSoMMG = dbc.readRecord(query);
		}

		return dbrRmSkSoMMG;
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr, Vector vettSc, Vector vettCompComm) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		String punto = nomeEJB + "update ";
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			String fonte = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_FONTE);
			String idScheda = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_RICHIESTA);
			String nCartella = ISASUtil.getValoreStringa(dbr, Costanti.N_CARTELLA);
			String idSkso = ISASUtil.getValoreStringa(dbr, Costanti.CTS_ID_SKSO); 
			
			dbr = execUpdate(dbc, mylogin, dbr, vettSc, vettCompComm);
			dbr.put(CostantiSinssntW.CTS_FONTE, fonte);
			dbr.put(CostantiSinssntW.CTS_ID_RICHIESTA, idScheda);
			
			aggiornaStatoPht2(dbc, dbr.getHashtable());
			aggiornaStatoPua(dbc, dbr.getHashtable());
			aggiornaStatoZkRsaRichiesta(dbc, dbr.getHashtable());
			
			recuperaDatiPai(dbc, nCartella, idSkso, dbr);
			recuperaInfoScheda(dbc, dbr);
			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			return dbr;
		} catch (CariException ce) {
			ce.printStackTrace();
			LOG.error(punto + "Dati da esaminare>>" + ce);
			ce.setISASRecord(null);
			try {
				LOG.trace(punto + " Effettuo rollback ");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new CariException("Errore eseguendo la rollback() - " + e1);
			}
			throw ce;
		} catch (DBRecordChangedException e) {
			LOG.trace(punto + " Errore nel salvataggio dei dati ", e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			LOG.trace(punto + " Errore nel salvataggio dei dati " + e);
			try {
				LOG.trace(punto + "Effettuo rollback ", e);
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				LOG.error(punto + " Errrore nel salvataggio dei dati ", e);
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw newEjbException("Errore eseguendo un update: " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}// END update

	private ISASRecord execUpdate(ISASConnection dbc, myLogin mylogin, ISASRecord dbr, Vector vettSc,
			Vector vettCompComm) throws ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException, DBRecordChangedException, CariException, Exception, SQLException {
		String punto = ver + "execUpdate ";
		String msg = "";
		boolean isSospUVM = false;
		Hashtable h = (Hashtable) dbr.getHashtable();
		LOG.info("update -- HASH: " + h.toString());

		
		String idSkSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
		String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
		String nuovaScheda = ISASUtil.getValoreStringa(dbr,"nuovaScheda");
		String nuovaData = ISASUtil.getValoreStringa(dbr,"nuovaData");
		
		allineaDatiTipoCura(dbc, nCartella, idSkSo, dbr);
		
		gestisciFlag(dbc,dbr,false);
		
		String dtChiusura = ISASUtil.getValoreStringa(dbr, "pr_data_chiusura");
		if (ManagerDate.validaData(dtChiusura)){
			LOG.debug(punto + " effettuo il controllo delle chiusure delle sospensioni ");
			chiudiProrogheSospensioniAttive(dbc, nCartella, idSkSo, dtChiusura);
		}
		dbc.writeRecord(dbr);

		
		idSkSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
		aggiornaRmSkSoMMG(dbc, nCartella, idSkSo + "", h);

		String myselect = "SELECT * FROM rm_skso WHERE n_cartella = " + nCartella + " AND id_skso = " + idSkSo;

		LOG.trace(punto + "update: " + myselect);
		dbr = dbc.readRecord(myselect);
		if (dbr != null) {
			idSkSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
			recuperaSkSoMMG(dbc, nCartella, idSkSo, dbr);
		}
		

		boolean delete = false;
		int num;
		Vector hv;
		if(vettSc!=null){
			delete = deleteDettagli(dbc, nCartella, idSkSo, -1);
			hv=(Vector)h.get("vettore");
			num=insertDettagli(dbc, nCartella, idSkSo, vettSc, (String) h.get("tipoCura"));
			LOG.info("Salvate: "+num+" prestazioni per la scheda:"+idSkSo);
		}
		
		if(vettCompComm!=null){
			delete = deleteDettagliObiettivi(dbc, nCartella, idSkSo);
			hv=(Vector)h.get("vettore");
			num=insertDettagliObiettivi(dbc, nCartella, idSkSo, vettCompComm, (String) h.get("tipoCura"));
			LOG.info("Salvati "+num+" obiettivi per la scheda:"+idSkSo + " della cartella:"+nCartella);
		}
		
		decodificaDati(dbc, dbr);
		inserisciMMG(dbc, nCartella, idSkSo, dbr);
		
		if (ManagerDate.validaData(dtChiusura)){
			if(!nuovaData.isEmpty()){
				dbr.put("nuovaScheda", nuovaScheda);
				dbr.put("nuovaData", nuovaData);
			}
			msg = gestisci_chiusura(mylogin,dbc,dbr, false);
			
		}
		
		if (!msg.equals("")) throw new CariException("Chiusura annullata. Si Ã¨ verificato un errore nella chiusura delle seguenti schede: "+msg+" Contattare l'assistenza!");
		
		updateIdSksoScBisogni(dbc,h);
//		aggiornaStatoPht2(dbc, h);
		return dbr;
	}

	private void aggiornaStatoPht2(ISASConnection dbc, Hashtable h) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException {
		String punto = ver + "aggiornaStatoPht2 ";
		LOG.debug(punto + " inizio con dati >>" + h);
		int fonte = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_FONTE);
		int idSkso = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_ID_SKSO);
		int idScheda = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_ID_RICHIESTA);
		if (fonte == CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA && idSkso > 0 && idScheda>0) {
			String query = "select p.* from pht2_generale p where p.id_scheda = "+ idScheda;
			LOG.debug(punto + " query >>" + query);
			ISASRecord dbrPht2 = dbc.readRecord(query);
			String statoScheda = ISASUtil.getValoreStringa(dbrPht2,"stato_scheda");
			if (dbrPht2 != null && ISASUtil.valida(statoScheda) && 
					statoScheda.equalsIgnoreCase(CostantiPHT.statoCompleta)) {
				dbrPht2.put("stato_scheda", CostantiPHT.statoRicevuta);
				LOG.debug(punto + " aggiorno dati>>" + (dbrPht2!=null? dbrPht2.getHashtable()+"":" no dati"));
				dbc.writeRecord(dbrPht2);
			}
		} else {
			LOG.trace(punto + " non aggiorno pht2");
		}
	}
	
	
	private void aggiornaStatoPua(ISASConnection dbc, Hashtable h) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException {
		String punto = ver + "aggiornaStatoPua ";
		LOG.debug(punto + " inizio con dati >>" + h);
		int fonte = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_FONTE);
		int idSkso = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_ID_SKSO);
		int idScheda = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_ID_RICHIESTA);
		if (fonte == CostantiSinssntW.CTS_TIPO_FONTE_PUA && idSkso > 0 && idScheda > 0) {
			String query = "SELECT presa_carico, presa_carico_data, id_skso, presa_carico_oper FROM ass_anagrafica WHERE progressivo = " + idScheda;
			LOG.debug(punto + " query >>" + query);
			ISASRecord dbrPua = dbc.readRecord(query);
			String presaCarico = ISASUtil.getValoreStringa(dbrPua, "presa_carico");
			if (dbrPua != null && !ISASUtil.valida(presaCarico)) {
				dbrPua.put("presa_carico", Costanti.CTS_S);
				dbrPua.put("presa_carico_data", ISASUtil.getValoreStringa(h, Costanti.CTS_PR_DATA_PUAC));
				dbrPua.put("id_skso", idSkso);
				dbrPua.put("presa_carico_oper", dbc.getKuser());
				LOG.debug(punto + " aggiorno dati>>" + (dbrPua != null ? dbrPua.getHashtable() + "" : " no dati"));
				dbc.writeRecord(dbrPua);
			}
		} else {
			LOG.trace(punto + " non aggiorno pua");
		}
	}
	
	
	private void aggiornaStatoZkRsaRichiesta(ISASConnection dbc, Hashtable h) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException {
		String punto = ver + "aggiornaStatoZkRsaRichiesta ";
		LOG.debug(punto + " inizio con dati >>" + h);
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		int tipoCura = ISASUtil.getValoreIntero(h, CostantiSinssntW.CTS_TIPOCURA);
   
		if (ISASUtil.valida(idSkso) && ISASUtil.valida(nCartella)
				&& QuadroSanitarioMMGCtrl.isTipoCuraResidenzialita(tipoCura)) {
			String dataPresaCaricoSkso = ISASUtil.getValoreStringa(h, "data_presa_carico_skso");
			if (ManagerDate.validaData(dataPresaCaricoSkso)) {
				RsaPreferenzeEJB rsaPreferenzeEJB = new RsaPreferenzeEJB();
				rsaPreferenzeEJB.aggiornaStato(dbc, nCartella, idSkso, PreferenzeStruttureCtrl.CTS_ID_RICHIESTA,
						RsaPreferenzeEJB.CTS_IN_ATTESA_GRADUATORIA);
			}
			String prDataChiusura = ISASUtil.getValoreStringa(h, "pr_data_chiusura");
			if (ManagerDate.validaData(prDataChiusura)) {
				RsaPreferenzeEJB rsaPreferenzeEJB = new RsaPreferenzeEJB();
				rsaPreferenzeEJB.aggiornaStato(dbc, nCartella, idSkso, PreferenzeStruttureCtrl.CTS_ID_RICHIESTA,
						RsaPreferenzeEJB.CTS_IN_CHIUSURA);
			}
		} else {
			LOG.trace(punto + " non aggiorno pht2");
		}
	}

	private void gestisciFlag(ISASConnection dbc, ISASRecord dbr, boolean inInsert) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, CariException {
		if (inInsert){
			if (ISASUtil.validaStringa(dbr,"pr_data_puac")){
				if (ISASUtil.validaStringa(dbr,"data_presa_carico_skso")){
					dbr.put(CostantiSinssntW.FLAG_SENT, CostantiSinssntW.FLAG_DA_INVIARE_I);
				}
				else dbr.put(CostantiSinssntW.FLAG_SENT, CostantiSinssntW.FLAG_DA_NON_INVIARE);
			}
		}
		else {
			//aggiornamento presa in carico o conclusione
			ISASRecord skso_prec = queryKey(dbc, dbr.get("n_cartella").toString(), dbr.get("id_skso").toString());
			if (dbr.get("pr_data_chiusura")==null){
			if (ISASUtil.validaStringa(dbr,"data_presa_carico_skso")){				
					String flag_sent = skso_prec.get(CostantiSinssntW.FLAG_SENT)!=null?skso_prec.get("flag_sent").toString():CostantiSinssntW.FLAG_DA_INVIARE_I;
					if (flag_sent.equals(CostantiSinssntW.FLAG_DA_NON_INVIARE)) dbr.put(CostantiSinssntW.FLAG_SENT, CostantiSinssntW.FLAG_DA_INVIARE_I);
					else if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_I)) dbr.put(CostantiSinssntW.FLAG_SENT,CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_I);
					else if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_V)) dbr.put(CostantiSinssntW.FLAG_SENT,CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_V);
					else if (flag_sent.equals(CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO)) dbr.put(CostantiSinssntW.FLAG_SENT,CostantiSinssntW.FLAG_DA_INVIARE_V);
			}		
				else dbr.put(CostantiSinssntW.FLAG_SENT, CostantiSinssntW.FLAG_DA_NON_INVIARE);
			}
			else{
				String flag_sent = skso_prec.get(CostantiSinssntW.FLAG_SENT_CONCL)!=null?skso_prec.get(CostantiSinssntW.FLAG_SENT_CONCL).toString():CostantiSinssntW.FLAG_DA_INVIARE_I;
				if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_I)) dbr.put(CostantiSinssntW.FLAG_SENT_CONCL,CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_I);
				else if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_V)) dbr.put(CostantiSinssntW.FLAG_SENT_CONCL,CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_V);
				else if (flag_sent.equals(CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO)) dbr.put(CostantiSinssntW.FLAG_SENT_CONCL,CostantiSinssntW.FLAG_DA_INVIARE_V);
				
			}	
		}
	}

	private boolean differsPC(ISASRecord dbr, ISASRecord skso_prec) throws ISASMisuseException {
		String old_pc,new_pc,old_richiedente,new_richiedente,old_num_fam,new_num_fam,old_badante,new_badante, old_stato_civile,
		new_stato_civile;
		boolean ret = true;
		
		old_richiedente = skso_prec.get("richiedente")!=null?skso_prec.get("richiedente").toString():"";
		new_richiedente = dbr.get("richiedente")!=null?skso_prec.get("richiedente").toString():"";
		
		old_num_fam = skso_prec.get("num_fam")!=null?skso_prec.get("num_fam").toString():"";
		new_num_fam = dbr.get("num_fam")!=null?dbr.get("num_fam").toString():"";
		
		old_badante = skso_prec.get("badante")!=null?skso_prec.get("badante").toString():"";
		new_badante = dbr.get("badante")!=null?dbr.get("badante").toString():"";
		
		old_stato_civile = skso_prec.get("stato_civile")!=null?skso_prec.get("stato_civile").toString():"";
		new_stato_civile = dbr.get("stato_civile")!=null?dbr.get("stato_civile").toString():"";
		
		old_pc = skso_prec.get("data_presa_carico_skso")!=null?skso_prec.get("data_presa_carico_skso").toString():"";
		new_pc = dbr.get("data_presa_carico_skso")!=null?dbr.get("data_presa_carico_skso").toString():"";
		
		if (old_richiedente.equals(new_richiedente) && old_num_fam.equals(new_num_fam) &&
				old_badante.equals(new_badante) && old_stato_civile.equals(new_stato_civile)
				&& old_pc.equals(new_pc))
			ret = false;
		return ret;
		  
	}

	private void inserisciMMG(ISASConnection dbc, String nCartella, String idSkSo, ISASRecord dbrSkso)
			throws Exception {
		String punto = ver + "inserisciMMG ";
	 
		int accessi = ISASUtil.getValoreIntero(dbrSkso, CostantiSinssntW.CTS_ACCESSI_MMG);
		RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
		if (accessi>0){
			rmSkSOOpCoinvoltiEJB.inserisciMMGPLS(dbc, nCartella, idSkSo, dbrSkso.getHashtable());
		}else {
			rmSkSOOpCoinvoltiEJB.rimuoviMMGPLS(dbc, nCartella, idSkSo, dbrSkso.getHashtable());
			LOG.debug(punto + " Non inserisco l'mmg e il pls: non ci sono accessi previsti ");
		}
	}

	public void updatePerChiusura(myLogin mylogin, ISASRecord dbrx_chiu) throws SQLException, CariException{
		ISASConnection dbc = null;
		String methodName = "updatePerChiusura";
		String msg = "";
		try {
			dbc = super.logIn(mylogin);
			
		
		String idSkSo = ISASUtil.getValoreStringa(dbrx_chiu, CostantiSinssntW.CTS_ID_SKSO);
		String nCartella = ISASUtil.getValoreStringa(dbrx_chiu, CostantiSinssntW.N_CARTELLA);
		boolean chiusuraForzata = dbrx_chiu.getHashtable().containsKey(CostantiSinssntW.FORZA_CHIUSURA);
		
		String sql = "select * from rm_skso where n_cartella = "+nCartella+
						" and id_skso = "+idSkSo;
		ISASRecord dbr = dbc.readRecord(sql);
		
		dbr.put("pr_data_chiusura", dbrx_chiu.get("pr_data_chiusura"));
		dbr.put("pr_motivo_chiusura", dbrx_chiu.get("pr_motivo_chiusura"));
		
		dbc.writeRecord(dbr);
		
			String dtChiusura = ISASUtil.getValoreStringa(dbr, "pr_data_chiusura");
		if (ManagerDate.validaData(dtChiusura)){
			chiudiProrogheSospensioniAttive(dbc, nCartella, idSkSo, dtChiusura);
		
		}
				
		if (ManagerDate.validaData(dtChiusura)){
			msg = gestisci_chiusura(mylogin,dbc,dbr, chiusuraForzata);
		}
		if (!msg.equals("")) throw new CariException(msg);
			
		}catch (CariException ce){
			throw ce;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName + ": " + e.getMessage(), e);
		}finally {
//			close_dbcur_nothrow(methodName, dbcur);
			logout_nothrow(methodName, dbc);
		}
	}
	
	private String gestisci_chiusura(myLogin mylogin, ISASConnection dbc, ISASRecord dbr, boolean chiusuraForzata) throws  CariException{
		// Chiusura Contatto Inf
		String msg="";
		String sep="";
		String cartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
		String id_skso  = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
		String nuovaScheda = ISASUtil.getValoreStringa(dbr,"nuovaScheda");
		String nuovaData = ISASUtil.getValoreStringa(dbr,"nuovaData");
		
		String scheda="infermieristica";
		try{

			SkInfEJB skinf_ejb = new SkInfEJB();
			String ski_select="select * from skinf where n_cartella = "+cartella
					+" and id_skso = "+id_skso
					+" and ski_data_uscita is null";
			ISASRecord ski_dbr = dbc.readRecord(ski_select);
			if (ski_dbr!=null){
				ISASRecord ski_qk = skinf_ejb.queryKey(mylogin, ski_dbr.getHashtable());
				ski_qk.put(CostantiSinssntW.SKI_DATA_USCITA,dbr.get("pr_data_chiusura"));
				ski_qk.put("ski_motivo",dbr.get("9"));
				skinf_ejb.update(dbc, ski_qk, (ISASCursor)null);
				if(!nuovaScheda.isEmpty()){
					Hashtable nuovoContatto = ski_qk.getHashtable();
					nuovoContatto.remove(CostantiSinssntW.SKI_DATA_USCITA);
					nuovoContatto.remove("ski_motivo");
					nuovoContatto.put(CostantiSinssntW.CTS_ID_SKSO, nuovaScheda);
					nuovoContatto.put(CostantiSinssntW.SKI_DATA_APERTURA, nuovaData);
					skinf_ejb.insertTransactional(dbc, nuovoContatto);
				}
			}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException && !chiusuraForzata) throw (CariException)e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		try{
			//Chiusura contatto Fisio
			scheda="fisioterapista";
			SkFisioEJB skfisio_ejb = new SkFisioEJB();
			String skf_select="select * from skfis" +
					" where n_cartella = "+cartella
					+" and id_skso = "+ id_skso
					+" and skf_data_chiusura is null";
			ISASRecord skf_dbr = dbc.readRecord(skf_select);
			if (skf_dbr!=null){
				ISASRecord skf_qk = skfisio_ejb.queryKey(mylogin, skf_dbr.getHashtable());
				skf_qk.put("skf_data_chiusura",dbr.get("pr_data_chiusura"));
				skf_qk.put("skf_motivo_chius",dbr.get("9"));
				skfisio_ejb.update(dbc, skf_qk);
				if(!nuovaScheda.isEmpty()){
					Hashtable nuovoContatto = skf_qk.getHashtable();
					nuovoContatto.remove("skf_data_chiusura");
					nuovoContatto.remove("skf_motivo_chius");
					nuovoContatto.put(CostantiSinssntW.CTS_ID_SKSO, nuovaScheda);
					nuovoContatto.put(CostantiSinssntW.SKF_DATA, nuovaData);
					skfisio_ejb.insertTransactional(dbc, nuovoContatto);
				}
			}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException && !chiusuraForzata) throw (CariException)e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		try{
			//Chiusura contatto medico
			scheda="medica";
			SkMedEJB skfmed_ejb = new SkMedEJB();
			String skm_select="select * from skmedico" +
					" where n_cartella = "+cartella
					+" and id_skso = "+ id_skso
					+" and skm_data_chiusura is null";
			ISASRecord skm_dbr = dbc.readRecord(skm_select);
			if (skm_dbr!=null){
				ISASRecord skm_qk = skfmed_ejb.queryKey(mylogin, skm_dbr.getHashtable());
				skm_qk.put("skm_data_chiusura",dbr.get("pr_data_chiusura"));
				skm_qk.put("skm_motivo_chius",dbr.get("9"));
				skfmed_ejb.update(dbc, skm_qk);
				if(!nuovaScheda.isEmpty()){
					Hashtable nuovoContatto = skm_qk.getHashtable();
					nuovoContatto.remove("skm_data_chiusura");
					nuovoContatto.remove("skm_motivo_chius");
					nuovoContatto.put(CostantiSinssntW.CTS_ID_SKSO, nuovaScheda);
					nuovoContatto.put("skm_data_apertura", nuovaData);
					skfmed_ejb.insertTransactional(dbc, nuovoContatto);
				}
			}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException && !chiusuraForzata) throw (CariException)e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		try{
			//Chiusura contatto generico
			scheda="operatore generico";
			SkFpgEJB skfpg_ejb = new SkFpgEJB();
			String skfpg_select="select * from skfpg" +
					" where n_cartella = "+cartella
					+" and id_skso = "+ id_skso
					+" and skfpg_data_uscita is null";
			ISASCursor skfpg_dbcur = dbc.startCursor(skfpg_select);
			if (skfpg_dbcur!=null && skfpg_dbcur.getDimension()>0){
				while (skfpg_dbcur.next()){
					ISASRecord curr_skfpg = skfpg_dbcur.getRecord();
					ISASRecord skfpg_qk = skfpg_ejb.queryKey(mylogin, curr_skfpg.getHashtable());
					skfpg_qk.put("skfpg_data_uscita",dbr.get("pr_data_chiusura"));
					skfpg_qk.put("skfpg_motivo_uscita",dbr.get("9"));
					skfpg_ejb.update(dbc, skfpg_qk);
					if(!nuovaScheda.isEmpty()){
						Hashtable nuovoContatto = skfpg_qk.getHashtable();
						nuovoContatto.remove("skfpg_data_uscita");
						nuovoContatto.remove("skfpg_motivo_uscita");
						nuovoContatto.put(CostantiSinssntW.CTS_ID_SKSO, nuovaScheda);
						nuovoContatto.put("skfpg_data_apertura", nuovaData);
						skfpg_ejb.insertTransactional(dbc, nuovoContatto);
					}
				}
				skfpg_dbcur.close();
			}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException && !chiusuraForzata) throw (CariException)e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		try{
		//Gestione richiesta accolta
		
		String sql = "select * from richieste_chiusura where n_cartella = "+cartella
							+" and id_skso = "+ id_skso
							+" and esito_richiesta = "+CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA;
		
				
		ISASCursor dbcur = dbc.startCursor(sql);
		if (dbcur!=null && dbcur.getDimension()>0){
			while (dbcur.next()){
			ISASRecord dbr_rich = dbcur.getRecord();
			sql =  "SELECT * FROM richieste_chiusura WHERE n_cartella = " + cartella + " and id_skso = " +  id_skso
					+ " and data_richiesta = "+dbc.formatDbDate(dbr_rich.get("data_richiesta").toString())
					+ " and cod_zona_richiedente = "+dbr_rich.get("cod_zona_richiedente").toString();
			ISASRecord dbrw = dbc.readRecord(sql);
			dbrw.put("esito_richiesta", CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_CONFERMATA);
			dbrw.put("cod_operatore_chiusura",dbc.getKuser());
			dbrw.put("data_chiusura",new java.sql.Date(Calendar.getInstance().getTimeInMillis()));			
			dbc.writeRecord(dbrw);
			}
			dbcur.close();
		}
		}catch(Exception e){
			e.printStackTrace();			
			throw new CariException("Errore nell'aggiornamento della richiesta di chiusura");			
		}		
		return msg;
	}

	private boolean deleteDettagli(ISASConnection dbc, String cartella, String id_skso, int tipoCura) throws SQLException{
		return deleteDettagli(dbc, cartella, id_skso, "PAI", "pai_prog", tipoCura);
	}
	
	private boolean deleteDettagliObiettivi(ISASConnection dbc, String cartella, String id_skso) throws SQLException{
		return deleteDettagliObiettivi(dbc, cartella, id_skso, -1);
	}
	private boolean deleteDettagliObiettivi(ISASConnection dbc, String cartella, String id_skso, int tipoCura) throws SQLException{
		return deleteDettagli(dbc, cartella, id_skso, "OBIETTIVI_PAI", "obiettivi_prog", tipoCura);
	}

	private boolean deleteDettagli(ISASConnection dbc, String cartella, String id_skso, String tabella, String progressivo, int tipoCura) throws SQLException{
		String methodName = "deleteDettagli";
		ISASCursor dbcur = null;
		try{
			String myselect="SELECT * from " + tabella + " where n_cartella = '" +cartella+"' AND id_skso = "+id_skso;
			if (tipoCura>0){
				myselect +=" and tipocura = "+ tipoCura;
			}
			LOG.trace(methodName + " query> " +myselect);
			
			dbcur=dbc.startCursor(myselect) ;
			while(dbcur.next()){
				ISASRecord dbrec=(ISASRecord)dbcur.getRecord() ;
				if (dbrec!=null && dbrec.get(progressivo)!=null){
					String selectCal = myselect + " AND " + progressivo + " =" +(Integer)dbrec.get(progressivo);
					ISASRecord dbprest=dbc.readRecord(selectCal);
					dbc.deleteRecord(dbprest);
				}
			}
			return true;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(methodName, dbcur);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked"})
	private int insertDettagli(ISASConnection dbc, String cartella, String id_skso, Vector hv1, String tipoCura) throws SQLException{
		return insertDettagli(dbc, cartella, id_skso, hv1, tipoCura, "PAI","pai_prog");
	}
	
	@SuppressWarnings("rawtypes")
	private int insertDettagliObiettivi(ISASConnection dbc, String cartella, String id_skso, Vector hv1, String tipoCura) throws SQLException{
		return insertDettagli(dbc, cartella, id_skso, hv1, tipoCura, "obiettivi_pai","obiettivi_prog");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked"})
	private int insertDettagli(ISASConnection dbc, String cartella, String id_skso, Vector hv1, String tipoCura, String tabella, String progressivo) throws SQLException{
		boolean SALVASOLOSELEZIONATI = false;
		String methodName = "insertDettagli";
//		int numero=0;
//		String codPrestazione = "";
		try{
			Hashtable h = new Hashtable();
			ISASRecord dbr = null;
			int i=0;
			for(Enumeration<Hashtable> enume=hv1.elements(); enume.hasMoreElements(); ){
				h=(Hashtable)enume.nextElement();
				Enumeration<String> n=h.keys();
//				String myselect="SELECT * from " + tabella + " where n_cartella = '" +cartella+"' AND "+ progressivo + " = "+ progr + 
//						"AND id_skso = "+id_skso;
//				dbr=dbc.readRecord(myselect) ;
//				
//				if(dbr==null){
					dbr=dbc.newRecord(tabella);
//					dbr.put("n_cartella",cartella);
//					dbr.put("id_skso",new Integer(id_skso));
//				}

				while(n.hasMoreElements()){
					String elem=(String)n.nextElement();
					dbr.put(elem,h.get(elem));
				}
				String check = (String) dbr.get("checked");
				if(SALVASOLOSELEZIONATI  && !check.equals("S")){
					continue;
				}
				Integer progr = new Integer(i++);
				dbr.put(progressivo, progr);
				dbr.put(Costanti.N_CARTELLA,cartella);
				dbr.put(Costanti.CTS_ID_SKSO,new Integer(id_skso));
				dbc.writeRecord(dbr);
				LOG.info("SCRITTO su "+ tabella);
			}
			return i;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}
	}
	
	private void allineaDatiTipoCura(ISASConnection dbc, String nCartella, String idSkSo, ISASRecord dbr)
			throws Exception {
		String punto = ver + "allineaDatiUvi ";
		int tipoCura = ISASUtil.getValoreIntero(dbr, Costanti.CTS_TIPOCURA);

		if (tipoCura > 0) {
			int tipoCuraDb = getTipoCura(dbc, nCartella, idSkSo);
			if (tipoCura != tipoCuraDb) {
				if (tipoCura == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_PRESTAZIONALI)) {
					LOG.trace(punto + " CURE prestazionali ");
					rimuoviComponentiUvi(dbc, nCartella, idSkSo, dbr, punto);
					deleteDettagli(dbc, nCartella, idSkSo,
							ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE));
					deleteDettagli(dbc, nCartella, idSkSo, ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI));
					deleteDettagliObiettivi(dbc, nCartella, idSkSo);
					deleteEsitoResidenziale(dbc, nCartella, idSkSo);
				} else if (tipoCura == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)) {
					LOG.trace(punto + " CURE DOMICILIARI INTEGRALI RIMUOVO ");
					deleteEsitoResidenziale(dbc, nCartella, idSkSo);
					deleteDettagli(dbc, nCartella, idSkSo,
							ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_PRESTAZIONALI));
					deleteDettagli(dbc, nCartella, idSkSo, ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI));

				} else if (tipoCura == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI)) {
					LOG.trace(punto + " NON RIMUOVO NULLA RESIDENZIALI ");
					deleteOperatoreCoinvolto(dbc, nCartella, idSkSo);
					deleteDettagli(dbc, nCartella, idSkSo,
							ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_PRESTAZIONALI));
					deleteDettagli(dbc, nCartella, idSkSo,
							ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE));
				}
			}else {
				LOG.trace(punto + " Non è cambiato il tipo di cura, non effettuo nessun controllo di coerenza sui dati ");
			}
		}
	}

	private int getTipoCura(ISASConnection dbc, String nCartella, String idSkSo) {
		String punto = ver + "getTipoCura ";
		int tipoCuraDb =-1;
		
		String query = " SELECT tipocura FROM rm_skso_mmg WHERE n_cartella = " +nCartella +" AND id_skso = " + idSkSo;
		LOG.debug(punto + " query>" +query);
		try {
			ISASRecord dbrRmSksommg = dbc.readRecord(query);
			tipoCuraDb = ISASUtil.getValoreIntero(dbrRmSksommg, Costanti.CTS_TIPOCURA);
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare il tipo di cura ", e);
		}
		return tipoCuraDb;
	}


	private void deleteOperatoreCoinvolto(ISASConnection dbc, String nCartella, String idSkSo)
			throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String punto = ver + "deleteOperatoreCoinvolto ";
		LOG.debug(punto + " rimuovo i dati degli operatori coinvolti ");
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(Costanti.N_CARTELLA, nCartella);
		dati.put(Costanti.CTS_ID_SKSO, idSkSo);
		RMSkSOOpCoinvoltiEJB RMSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
		RMSkSOOpCoinvoltiEJB.deleteAll(dbc, dati);
		
	}

	private void deleteEsitoResidenziale(ISASConnection dbc, String nCartella, String idSkSo)
			throws Exception {
		String punto = ver + "deleteEsitoResidenziale ";
		LOG.debug(punto + " rimuovo i dati della richiesta ed eventuali preferenze espresse  ");

		RsaPreferenzeEJB rsaPreferenzeEJB = new RsaPreferenzeEJB();
		ISASRecord dbrRsaRichiesta =  rsaPreferenzeEJB.recuperaDbrRichiesta(dbc, nCartella, idSkSo, PreferenzeStruttureCtrl.CTS_ID_RICHIESTA);
		
		if(dbrRsaRichiesta!=null){
			LOG.debug(punto + " rimuovo il record ");
			dbc.deleteRecord(dbrRsaRichiesta);
		}
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(Costanti.N_CARTELLA, nCartella);
		dati.put(Costanti.CTS_ID_DOMANDA, idSkSo);
		dati.put(Costanti.CTS_ID_RICHIESTA, PreferenzeStruttureCtrl.CTS_ID_RICHIESTA);
		
		LOG.debug(punto + " rimuovo i le preferenze >>" + dati+"< ");
		rsaPreferenzeEJB.deleteRpPreferenze(dbc, dati);
		
//		IngressoStruttureSOEJB ingressoStruttureSo = new IngressoStruttureSOEJB();
//		ingressoStruttureSo.deleteAll(dbc, dati);
		
	}

	/**
	 * @param dbc
	 * @param nCartella
	 * @param idSkSo
	 * @param dbr
	 * @param punto
	 * @throws ISASMisuseException
	 * @throws DBMisuseException
	 * @throws DBSQLException
	 * @throws ISASPermissionDeniedException
	 * @throws DBRecordChangedException
	 */
	private void rimuoviComponentiUvi(ISASConnection dbc, String nCartella, String idSkSo, ISASRecord dbr, String punto)
			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException,
			DBRecordChangedException {
		LOG.trace(punto + " rimuovo evenutali componenti uvi presenti componenti ");
		dbr.put(CostantiSinssntW.CTS_COD_COMMISSIONE, "");
		dbr.put("pr_data_verbale_uvm", null);
		dbr.put("pr_num_verbale", "");
		dbr.put("presa_carico_livello" ,"");
		dbr.put("dt_presa_carico_livello", null);
		dbr.put("cod_case_manager", "");
		dbr.put("pr_valutazione","");
		dbr.put("pr_obiettivo", "");
		dbr.put("pr_piano_terapeutico", "");
		dbr.put("pr_revisione","");
		dbr.put("pr_data_revisione","");
		
		RMPuaUvmCommissioneEJB rmPuaUvmCommissioneEJB = new RMPuaUvmCommissioneEJB();
		rmPuaUvmCommissioneEJB.rimuoviAllComponenti(dbc, nCartella, idSkSo);
	}



	private void chiudiProrogheSospensioniAttive(ISASConnection dbc,
			String nCartella, String idSkSo, String dtChiusura) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException {
		String punto = ver + "chiudiProrogheSospensioniAttive ";
		aggiornaProroghe(dbc, nCartella, idSkSo, dtChiusura);
		aggiornaSospensioni(dbc, nCartella, idSkSo, dtChiusura);
	}

	private void aggiornaSospensioni(ISASConnection dbc, String nCartella,
			String idSkSo, String dtChiusura) throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException, DBRecordChangedException {
		String punto = ver + "aggiornaProroghe ";
		LOG.debug(punto + " inizio con la chiusura ");
		String query ="SELECT * FROM rm_skso_sospensioni WHERE n_cartella = " +
		nCartella +" AND id_skso = " +idSkSo+ " AND (   dt_sospensione_fine > " +
				formatDate(dbc, dtChiusura) +"  OR dt_sospensione_fine IS NULL ) ";
		LOG.trace(punto + " query >>" +query);
		ISASCursor crSospensioni = null;
		try {
			crSospensioni = dbc.startCursor(query);
			String isSospensioni, queryExec;
			while (crSospensioni.next()) {
				ISASRecord dbrRmSkSoProroghe = (ISASRecord) crSospensioni.getRecord();
				isSospensioni = ISASUtil.getValoreStringa(dbrRmSkSoProroghe, CostantiSinssntW.CTS_SO_ID_SOSPENSIONE);
				queryExec = query +" and id_sospensione  = "+ isSospensioni;
				LOG.trace(punto + " queryExec >>" +queryExec);
				ISASRecord dbrUp = dbc.readRecord(queryExec);
				if (dbrUp!=null){
					dbrUp.put(CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE, dtChiusura);
					dbc.writeRecord(dbrUp);
				}
			}
		} finally {
			close_dbcur_nothrow(punto, crSospensioni);
		}
	}

	private void aggiornaProroghe(ISASConnection dbc, String nCartella,
			String idSkSo, String dtChiusura)
			throws ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException, DBRecordChangedException {
		String punto = ver + "aggiornaProroghe ";
		LOG.debug(punto + " inizio con la chiusura ");
		String query ="select * from rm_skso_proroghe where n_cartella = " +
				nCartella +" and id_skso = " +idSkSo + " AND ( dt_proroga_fine > " +
				formatDate(dbc, dtChiusura)+ " or dt_proroga_fine is null)   ";
		LOG.trace(punto + " query >>" +query);
		
		ISASCursor crRmSkSoProroghe= null;
		try {
			crRmSkSoProroghe = dbc.startCursor(query);
			String idProroga, queryExec;
			while (crRmSkSoProroghe.next()) {
				ISASRecord dbrRmSkSoProroghe = (ISASRecord) crRmSkSoProroghe.getRecord();
				idProroga = ISASUtil.getValoreStringa(dbrRmSkSoProroghe, CostantiSinssntW.CTS_SO_ID_PROROGA);
				queryExec = query +" and id_proroga = "+ idProroga;
				LOG.trace(punto + " queryExec >>" +queryExec);
				ISASRecord dbrUp = dbc.readRecord(queryExec);
				if (dbrUp!=null){
					dbrUp.put(CostantiSinssntW.CTS_DT_PROROGA_FINE, dtChiusura);
					dbc.writeRecord(dbrUp);
				}
			}
		} finally {
			close_dbcur_nothrow(punto, crRmSkSoProroghe);
		}
	}

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "delete ";
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			dbc.startTransaction();
			String nCartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
			String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
			String query ="SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + nCartella + " and id_skso = " + idSkso;

			LOG.trace(punto + " query>>" + query + "]");
			ISASRecord dbrRmSkso = dbc.readRecord(query);
			dbc.deleteRecord(dbrRmSkso);
			
			deleteRecordRmSkSoMMG(dbc, nCartella, idSkso);
			
			deleteRecordOpCoinvolti(dbc, nCartella, idSkso);
			dbc.commitTransaction();
//			deleteAll(dbc, dbr, dbcur, false);

			dbc.close();
			super.close(dbc);
//			done = true;
		} catch (DBRecordChangedException e) {
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una delete() - " + e);
		} finally {
			logout_nothrow(punto, dbc);
//			if (!done) {
//				try {
//					if (dbcur != null)
//						dbcur.close();
//					dbc.close();
//					super.close(dbc);
//				} catch (Exception e2) {
//					System.out.println("RLSkPuacEJB.delete(): - Eccezione nella chiusura della connessione= " + e2);
//				}
//			}
		}
	}

	private void deleteRecordRmSkSoMMG(ISASConnection dbc, String nCartella,
			String idSkso) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException,
			DBRecordChangedException {
		String punto = ver + "deleteRecordRmSkSoMMG ";
		String query = "SELECT *   FROM rm_skso_mmg   where n_cartella = "
				+ nCartella + " and id_skso = " + idSkso;
		LOG.trace(punto + " query >>" + query);

		ISASRecord dbrRmSkSo = dbc.readRecord(query);

		if (dbrRmSkSo != null) {
			dbc.deleteRecord(dbrRmSkSo);
		}
	}

	private void deleteRecordOpCoinvolti(ISASConnection dbc, String nCartella,
			String idSkso) throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException, DBRecordChangedException {
		String punto = ver + "deleteRecordOpCoinvolti ";
		String query = "SELECT * FROM rm_skso_op_coinvolti where n_cartella = " +
				nCartella + " and id_skso = " + idSkso;
		LOG.trace(punto + " query dett>>"+ query);
		String query_dettaglio =""; 
		String tpOperatore =""; 
		ISASCursor dbrCursor = dbc.startCursor(query);
		while (dbrCursor.next()) {
			ISASRecord dbrSkSoOp = (ISASRecord) dbrCursor.getRecord();
			tpOperatore = ISASUtil.getValoreStringa(dbrSkSoOp, "tipo_operatore");
			if (dbrSkSoOp!=null && ISASUtil.valida(tpOperatore)){
				query_dettaglio = query + " and tipo_operatore = '" +tpOperatore+"'";
				LOG.trace(punto + " query dett>>"+ query_dettaglio);
				ISASRecord dbrSkSoOpCoinvolti = dbc.readRecord(query_dettaglio);
				if (dbrSkSoOpCoinvolti!=null){
					dbc.deleteRecord(dbrSkSoOpCoinvolti);
				}
			}
		}
	}

	// 29/12/09: cancella anche il record collegato su ASS_ANAGRAFICA ed eventualmente chiude il caso UVM
	public void deleteAlsoAssAna(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			deleteAll(dbc, dbr, dbcur, true);

			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			debugMessage("RLSkPuacEJB.deleteAlsoAssAna(): Eccezione= " + e);
			try {
				System.out.println("RLSkPuacEJB.deleteAlsoAssAna() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("RLSkPuacEJB.deleteAlsoAssAna(): Eccezione= " + e);
			try {
				System.out.println("RLSkPuacEJB.deleteAlsoAssAna() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB.deleteAlsoAssAna(): Eccezione= " + e);
			try {
				System.out.println("RLSkPuacEJB.deleteAlsoAssAna() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una deleteAlsoAssAna() - " + e);
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println("RLSkPuacEJB.deleteAlsoAssAna(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}

	private void deleteAll(ISASConnection dbc, ISASRecord dbr, ISASCursor dbcur, boolean alsoAssAna) throws Exception {
		// 21/05/09 m.
		Hashtable h = dbr.getHashtable();
		dbc.startTransaction();

		gest_scaleVal.cancPuauvmSchede(dbc, h, dbcur);

		// 29/12/09: cancellazione di tutti gli eventuali menbri della commissione UVM
		cancellaAllCompCommPUACUVM(dbc, dbr.get("n_cartella").toString(), dbr.get("pr_data").toString(), dbr.get("pr_progr").toString());

		// 29/12/09: cancellazione di tutti i dati del PAP
		cancAllRtPap(dbc, dbr.get("n_cartella").toString(), dbr.get("pr_data").toString(), dbr.get("pr_progr").toString());

		if (alsoAssAna) // cancellazione di ASS_ANAGRAFICA ed agende
			cancAssAnagra(dbc, h);
		else
			// pulizia dei campi relativi alla presa in carico su ASS_ANAGRAFICA
			pulisciAssAnagra(dbc, h);

		ISASRecord dbrCaso = gestore_casi.getCasoRifUvm(dbc, h);
		if (dbrCaso != null) {
			boolean isUVM = ((Boolean) gestore_casi.isCasoUvm(dbrCaso)).booleanValue();
			boolean isAttesa = ((Boolean) gestore_casi.isStatoAttesa(dbrCaso)).booleanValue();
			boolean isToscana = gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_UVM);

			// se caso ï¿½ UVM in ATTESA e gestisco segnalaz (=Toscana)  --> chiudo il caso aperto dalla SEGNALAZIONE
			if (isUVM && isAttesa && isToscana)
				chiudiCasoUVM(dbc, dbr);
		}

		dbc.deleteRecord(dbr);

		// 21/05/09 m.
		dbc.commitTransaction();
		if (dbcur != null)
			dbcur.close();
	}

	// Lettura schede puac chiuse (= con dtVerbaleUVM o con dtChiusura)
	public Vector query_skValPuacChiuse(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM rl_puauvm" + " WHERE n_cartella = " + (String) h.get("n_cartella") + " AND pr_data = "
					+ formatDate(dbc, (String) h.get("pr_data")) + " AND ((pr_data_verbale_uvm IS NOT NULL)"
					+ " OR (pr_data_chiusura IS NOT NULL))" + // 06/10/08
					" ORDER BY pr_progr DESC";

			mySystemOut("query_skValPuacChiuse=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			// decodifiche
			for (Enumeration en = vdbr.elements(); en.hasMoreElements();) {
				ISASRecord dbr_1 = (ISASRecord) en.nextElement();
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: query_skValPuacChiuse - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("RLSkPuacEJB: query_skValPuacChiuse - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query_skValPuacChiuse

	// 16/01/08: legge i dati da ASS_ANAGRAFICA e AGENDANT_INTERV.
	// 07/10/08: lettura solo da ASS_ANAGRAFICA
	// 02/03/09: solo rec NON ancora presi in carico
	public Hashtable query_getAssAnagrAge(myLogin mylogin, Hashtable h0) {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		Vector vdbr = null;

		Hashtable h_ret = new Hashtable();

		try {
			dbc = super.logIn(mylogin);
			String selAna = "";
			String strAssAnagrCognome = "";
			String strAssAnagrNome = "";
			String strAssAnagrDtNasc = "";
			String strAssAnagrComNasc = "";
			String strAssAnagrProgressivo = (String) h0.get("ass_angr_progressivo");
			String cart = (String) h0.get("n_cartella"); // 07/10/08

			// lettura da ASS_ANAGRAFICA
			// 10/04/05: se ho il progr si legge con quello, altrimenti si cerca per cognome, nome, luogo e dtNasc
			if ((strAssAnagrProgressivo != null) && (!strAssAnagrProgressivo.trim().equals(""))) {
				selAna = "SELECT * FROM ass_anagrafica" + " WHERE progressivo = " + strAssAnagrProgressivo;
			} else if ((cart != null) && (!cart.trim().equals(""))) { // 07/10/08: si cerca x n_cartella
				selAna = "SELECT * FROM ass_anagrafica" + " WHERE n_cartella = " + cart
						+ " AND data_reg IN (SELECT MAX(data_reg) data_registrazione" + " FROM ass_anagrafica a" + " WHERE a.n_cartella = "
						+ cart + ")";
			} else {
				strAssAnagrCognome = (String) h0.get("ass_anagr_cognome");
				strAssAnagrCognome = duplicateChar(strAssAnagrCognome, "'"); // 06/06/08
				strAssAnagrNome = (String) h0.get("ass_anagr_nome");
				strAssAnagrNome = duplicateChar(strAssAnagrNome, "'"); // 06/06/08
				strAssAnagrDtNasc = (String) h0.get("ass_anagr_data_nascita");
				strAssAnagrComNasc = (String) h0.get("ass_anagr_com_nascita");

				selAna = "SELECT *" + " FROM ass_anagrafica" + " WHERE cognome = '" + strAssAnagrCognome + "'" + " AND nome = '"
						+ strAssAnagrNome + "'" + " AND comune_nascita = '" + strAssAnagrComNasc + "'" + " AND data_nascita = "
						+ formatDate(dbc, strAssAnagrDtNasc) + " AND data_reg = (SELECT MAX(data_reg) data_registrazione"
						+ " FROM ass_anagrafica a" + " WHERE a.cognome = '" + strAssAnagrCognome + "'" + " AND a.nome = '"
						+ strAssAnagrNome + "'" + " AND a.comune_nascita = '" + strAssAnagrComNasc + "'" + " AND a.data_nascita = "
						+ formatDate(dbc, strAssAnagrDtNasc) + ")";
			}
			// 02/03/09 ----
			// 24/11/09	selAna += " AND esito_contatto = '6'" +

			//mod elisa b 11/02/11 : si aggiunge il filtro solo se
			//il metodo e' chiamato da RLSkPuac in questo caso e' presente
			//un campo nella hashtable)
			if ((h0.containsKey("filtro_presa_carico")) && (!h0.get("filtro_presa_carico").equals(""))) {
				selAna += " AND esito_contatto IN ('6', '15')" + " AND (presa_carico <> 'S' OR presa_carico IS NULL)";
			}
			// 02/03/09 ----

			mySystemOut("query_getAssAnagrAge: selAna=" + selAna);
			ISASRecord dbr_1 = dbc.readRecord(selAna);
			if (dbr_1 != null) {
				java.sql.Date dtRegSql = (java.sql.Date) dbr_1.get("data_reg");
				DataWI myDtReg = new DataWI(dtRegSql);
				h_ret.put("data_reg", (myDtReg != null ? myDtReg.getString(0) : ""));
				// 29/02/08
				java.sql.Date dtInvioSql = (java.sql.Date) dbr_1.get("invio_puac_data");
				DataWI myDtInvio = new DataWI(dtInvioSql);
				String dtInvioStr = myDtInvio.getString(0);
				h_ret.put("invio_puac_data", (dtInvioStr != null ? dtInvioStr : ""));

				// 24/12/08
				h_ret.put("ass_angr_progressivo", ((Integer) dbr_1.get("progressivo")).toString());

				// 29/09/10 -------------
				if (dbr_1.get("segn_data") != null) {
					java.sql.Date dtSegnSql = (java.sql.Date) dbr_1.get("segn_data");
					DataWI myDtSegn = new DataWI(dtSegnSql);
					String dtSegnStr = myDtSegn.getString(0);
					if (dtSegnStr != null)
						h_ret.put("segn_data", dtSegnStr);
				}
				if (dbr_1.get("stato_civile") != null)
					h_ret.put("stato_civile", dbr_1.get("stato_civile").toString());
				if (dbr_1.get("titolo_studio") != null)
					h_ret.put("titolo_studio", dbr_1.get("titolo_studio").toString());
				if (dbr_1.get("badante") != null)
					h_ret.put("badante", dbr_1.get("badante").toString());
				if (dbr_1.get("num_fam") != null)
					h_ret.put("num_fam", dbr_1.get("num_fam").toString());
				if (dbr_1.get("tipo_segnalazione") != null)
					h_ret.put("tipo_segnalazione", dbr_1.get("tipo_segnalazione").toString());
				if (dbr_1.get("richiedente") != null)
					h_ret.put("richiedente", dbr_1.get("richiedente").toString());
				// 29/09/10 -------------			

				//elisa b 27/01/11
				if (dbr_1.get("arrivato") != null)
					h_ret.put("arrivato", dbr_1.get("arrivato").toString());
				if (dbr_1.get("motivo") != null)
					h_ret.put("motivo", dbr_1.get("motivo").toString());
				if (dbr_1.get("progressivo") != null)
					h_ret.put("progressivo", dbr_1.get("progressivo").toString());
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return h_ret;
		} catch (Exception e1) {
			System.out.println("RLSkPuacEJB.query_getAssAnagrAge - Eccezione=[" + e1 + "]");
			/**
			System.out.println("***************************");
			e1.printStackTrace();
			System.out.println("***************************");
			 **/
			return (Hashtable) null;
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	// 26/02/08: inserimento su tabella PROGETTO di un record con i soli valori della chiave
	private void scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception {
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		mySystemOut("-->> insert: Inserito record su tabella PROGETTO");
	}

	// 24/10/08
	private ISASRecord getRecAssAna(ISASConnection mydbc, Hashtable h0) throws Exception {
		ISASRecord mydbr = null;
		String sel = "SELECT * FROM ass_anagrafica";

		Object progrAssAna = h0.get("ass_angr_progressivo");
		Object cart = (Object) h0.get("n_cartella");
		Object dtSkVal = (Object) h0.get("pr_data");
		Object progrPuac = (Object) h0.get("pr_progr");

		if (progrAssAna != null)
			sel += " WHERE progressivo = " + progrAssAna;
		else if ((cart != null) && (dtSkVal != null) && (progrPuac != null))
			sel += " WHERE n_cartella = " + cart + " AND pr_data = " + formatDate(mydbc, "" + dtSkVal) + " AND pr_progr = " + progrPuac
					+ " ORDER BY data_reg DESC";
		else
			return null;
		mySystemOut("getRecAssAna: sel=[" + sel + "]");

		mydbr = mydbc.readRecord(sel);
		return mydbr;
	}

	// 07/10/08: aggiornamento di ASS_ANAGRAFICA con key RL_PUAUVM e campi relativi alla presa carico
	private void aggiornaAssAna(ISASConnection mydbc, ISASRecord mydbr, Hashtable h0) throws Exception {
		if (mydbr != null) {
			mydbr.put("n_cartella", h0.get("n_cartella"));
			mydbr.put("pr_data", h0.get("pr_data"));
			mydbr.put("pr_progr", h0.get("pr_progr"));

			boolean isPresaCarico = (h0.get("pr_data_puac") != null);

			// 13/10/09	mydbr.put("presa_carico", (isPresaCarico?"S":"N"));
			// 13/10/09 --
			boolean isSkChiusa = ((h0.get("pr_data_chiusura") != null) && (!((String) h0.get("pr_data_chiusura")).trim().equals("")));
			mydbr.put("presa_carico", (isSkChiusa ? "C" : (isPresaCarico ? "S" : "N")));
			// 13/10/09 ---

			if (isPresaCarico) {
				// 13/10/09		mydbr.put("presa_carico_data", h0.get("pr_data_puac"));
				// 13/10/09  --
				mydbr.put("presa_carico_data", (isSkChiusa ? h0.get("pr_data_chiusura") : h0.get("pr_data_puac")));
				if (isSkChiusa) {
					String motChiu = (String) HASH_MAP_MOTCHIUS.get(h0.get("pr_motivo_chiusura"));
					mydbr.put("presa_carico_motivo", (motChiu != null ? motChiu : "9"));
				}
				// 13/10/09 --

				mydbr.put("presa_carico_oper", h0.get("pr_oper_ultmod"));

				mydbr.put("urgente", h0.get("pr_flag_urgente"));
			}
			mydbc.writeRecord(mydbr);
			mySystemOut("aggiornaAssAna: aggiornato progr=[" + mydbr.get("progressivo") + "]");
		}
	}

	// 11/03/08: inserimento su ASS_ANAGRAFICA per revisione
	// 24/10/08: anche per inserimento diretto da PUAC per segnalazione di MMG
	private int inserisciAssAnagrafica(ISASConnection mydbc, String strNCartella, String dtVerb, String dtRev, String oper, Hashtable h_1)
			throws Exception {
		ISASRecord dbrCart = getDbrDaCartella(mydbc, strNCartella);
		if (dbrCart == null) {
			String msg = "Attenzione: Non esiste il record sulla tabella CARTELLA!";
			throw new CariException(msg, -2);
		}

		ISASRecord dbrAnagC = getDbrDaAnagra_c(mydbc, strNCartella);
		if (dbrAnagC == null) {
			String msg = "Attenzione: Non esiste il record sulla tabella ANAGRA_C!";
			throw new CariException(msg, -2);
		}

		int intProgressivo = selectProgressivo(mydbc, "ASSOC_PROGR_ANAG");
		ISASRecord dbr = mydbc.newRecord("ass_anagrafica");

		fillUpAssAnagrDaCartella(dbr, dbrCart);
		fillUpAssAnagrDaAnagra_c(dbr, dbrAnagC);

		if ((dtVerb != null) && (dtRev != null)) // REVISIONE
			fillUpAssAnagrCampiCriticiRev(mydbc, dbr, strNCartella, dtVerb, dtRev, oper, "PUAC", h_1);
		else
			// INSERIMENTO DIRETTO per SEGNALAZ MMG
			fillUpAssAnagrCampiCriticiNew(mydbc, dbr, strNCartella, oper, "PUAC", h_1);

		dbr.put("progressivo", new Integer(intProgressivo));

		mydbc.writeRecord(dbr);
		mySystemOut("inserisciAssAnagrafica: inserito record con progr= " + intProgressivo);
		return intProgressivo;
	}

	private ISASRecord getDbrDaCartella(ISASConnection dbc, String strNCartella) throws SQLException {
		try {
			String strQuery = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + strNCartella;
			mySystemOut("getDbrDaCartella/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/getDbrDaCartella - " + e1);
		}
	}

	private ISASRecord getDbrDaAnagra_c(ISASConnection dbc, String strNCartella) throws SQLException {
		try {
			String strQuery = "SELECT *" + " FROM anagra_c" + " WHERE n_cartella = " + strNCartella + " AND data_variazione = ("
					+ " SELECT MAX(data_variazione) max_data_var" + " FROM anagra_c a" + " WHERE a.n_cartella = " + strNCartella + ")";
			mySystemOut("getDbrDaAnagra_c/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/getDbrDaAnagra_c - " + e1);
		}
	}

	private void fillUpAssAnagrDaCartella(ISASRecord dbr, ISASRecord dbrCart) throws SQLException {
		try {
			dbr.put("n_cartella", dbrCart.get("n_cartella")); // 30/03/09
			dbr.put("cognome", dbrCart.get("cognome"));
			dbr.put("nome", dbrCart.get("nome"));
			dbr.put("comune_nascita", dbrCart.get("cod_com_nasc"));
			dbr.put("data_nascita", dbrCart.get("data_nasc"));
			dbr.put("cod_fis", dbrCart.get("cod_fisc"));
			dbr.put("sesso", dbrCart.get("sesso"));
			dbr.put("nazionalita", dbrCart.get("nazionalita"));
			dbr.put("cittadinanza", dbrCart.get("cittadinanza"));
			dbr.put("cod_usl", dbrCart.get("cod_usl"));
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrDaCartella - " + e1);
		}
	}

	private void fillUpAssAnagrDaAnagra_c(ISASRecord dbr, ISASRecord dbrAnagC) throws SQLException {
		try {
			dbr.put("comune_res", dbrAnagC.get("citta"));
			dbr.put("indirizzo_res", dbrAnagC.get("indirizzo"));
			dbr.put("areadis_res", dbrAnagC.get("areadis"));
			dbr.put("comune_dom", dbrAnagC.get("dom_citta"));
			dbr.put("indirizzo_dom", dbrAnagC.get("dom_indiriz"));
			dbr.put("areadis_dom", dbrAnagC.get("dom_aeradis"));

			dbr.put("nome_campanello", dbrAnagC.get("nome_camp"));

			dbr.put("comune_rep", dbrAnagC.get("comune_rep"));
			dbr.put("indirizzo_rep", dbrAnagC.get("indirizzo_rep"));
			dbr.put("areadis_rep", dbrAnagC.get("areadis_rep"));

			dbr.put("str_tipo_doc", dbrAnagC.get("str_tipo_doc"));
			dbr.put("str_numero_doc", dbrAnagC.get("str_numero_doc"));
			dbr.put("str_scadenza_doc", dbrAnagC.get("str_scadenza_doc"));
			dbr.put("str_intestatario_doc", dbrAnagC.get("str_intestatario_doc"));

			dbr.put("telefono", dbrAnagC.get("telefono1"));

			dbr.put("mecodi", dbrAnagC.get("cod_med"));
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrDaAnagra_c - " + e1);
		}
	}

	private void fillUpAssAnagrCampiCriticiRev(ISASConnection dbc, ISASRecord dbr, String strNCartella, String dtVerb, String dtRev,
			String strCodOperatore, String strTipoOperatore, Hashtable h_1) throws SQLException {
		NumberDateFormat ndf = new NumberDateFormat();
		try {
			String strQueryOper = "SELECT *" + " FROM operatori" + " WHERE codice = '" + strCodOperatore + "'";
			mySystemOut("fillUpAssAnagrCampiCriticiRev/strQueryOper: " + strQueryOper);
			ISASRecord dbrOper = dbc.readRecord(strQueryOper);
			String cognOp = "";
			String nomeOp = "";
			String presidioOp = "";// 06/06/08
			if (dbrOper != null) {
				if (dbrOper.get("cognome") != null)
					cognOp = (String) dbrOper.get("cognome");
				if (dbrOper.get("nome") != null)
					nomeOp = (String) dbrOper.get("nome");
				if (dbrOper.get("cod_presidio") != null) // 06/06/08
					presidioOp = (String) dbrOper.get("cod_presidio");
			}
			dbr.put("segn_cognome", cognOp);
			dbr.put("segn_nome", nomeOp);

			// revisione
			dbr.put("inserita_autom", "R");
			// dtRegistrazione = dtVerbaleUVM
			dbr.put("data_reg", dtVerb);
			// dtFirmaIstanza = dtRevisione - 30 gg
			String dataR = dtRev.substring(8, 10) + dtRev.substring(5, 7) + dtRev.substring(0, 4);
			DataWI dtWIAppo = new DataWI(dataR);
			DataWI dtWIRev = dtWIAppo.aggiungiGg(-30);
			dbr.put("invio_puac_data", dtWIRev.getFormattedString2(1));

			dbr.put("esito_contatto", "6");
			dbr.put("presa_carico", "N");
			dbr.put("urgente", "N");
			String testo = "Operatore: " + strCodOperatore + " - " + cognOp + " " + nomeOp + " - Tipo: " + strTipoOperatore;
			dbr.put("arrivato_note", testo);
			// 06/06/08 ---
			dbr.put("sospesa_flag", "N");
			dbr.put("cod_operatore", strCodOperatore);
			dbr.put("cod_presidio", presidioOp);
			// 06/06/08 ---

			// 07/10/08 ----
			dbr.put("soc_cod", h_1.get("pr_soc_codice"));
			// 30/03/09	dbr.put("soc_data", h_1.get("pr_soc_data_visita"));
			dbr.put("soc_data", (java.sql.Date) null);
			dbr.put("soc_carico", "N");
			dbr.put("san_cod", h_1.get("pr_inf_codice"));
			dbr.put("san_data", (java.sql.Date) null);
			dbr.put("san_carico", "N");
			// 07/10/08 ----
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrCampiCriticiRev - " + e1);
		}
	}

	// 24/10/08
	private void fillUpAssAnagrCampiCriticiNew(ISASConnection dbc, ISASRecord dbr, String strNCartella, String strCodOperatore,
			String strTipoOperatore, Hashtable h_1) throws SQLException {
		NumberDateFormat ndf = new NumberDateFormat();
		try {
			// segnalante = MMG
			String codMed = (String) dbr.get("mecodi");
			String strQueryMed = "SELECT *" + " FROM medici" + " WHERE mecodi = '" + codMed + "'";
			mySystemOut("fillUpAssAnagrCampiCriticiNew/strQueryMed: " + strQueryMed);
			ISASRecord dbrMed = dbc.readRecord(strQueryMed);
			String cognM = "";
			String nomeM = "";
			if (dbrMed != null) {
				if (dbrMed.get("mecogn") != null)
					cognM = (String) dbrMed.get("mecogn");
				if (dbrMed.get("menome") != null)
					nomeM = (String) dbrMed.get("menome");
			}
			dbr.put("segn_cognome", cognM);
			dbr.put("segn_nome", nomeM);

			// inserimento automatico
			dbr.put("inserita_autom", "P");
			// dtRegistrazione = dtSkPuac
			dbr.put("data_reg", h_1.get("pr_data_puac"));
			// dtFirmaIstanza = dtAvvio (se valorizzata, altrimenti = dtSkPuac)
			if (h_1.get("pr_data_richiesta") != null)
				dbr.put("invio_puac_data", h_1.get("pr_data_richiesta"));
			else
				dbr.put("invio_puac_data", h_1.get("pr_data_puac"));

			dbr.put("esito_contatto", "6");

			/** 13/10/09
						dbr.put("presa_carico", "S");
						dbr.put("presa_carico_data", h_1.get("pr_data_puac"));
			**/
			// 13/10/09 --
			boolean isSkChiusa = ((h_1.get("pr_data_chiusura") != null) && (!((String) h_1.get("pr_data_chiusura")).trim().equals("")));
			dbr.put("presa_carico", (isSkChiusa ? "C" : "S"));
			dbr.put("presa_carico_data", (isSkChiusa ? h_1.get("pr_data_chiusura") : h_1.get("pr_data_puac")));
			if (isSkChiusa) {
				String motChiu = (String) HASH_MAP_MOTCHIUS.get(h_1.get("pr_motivo_chiusura"));
				dbr.put("presa_carico_motivo", (motChiu != null ? motChiu : "9"));
			}
			// 13/10/09 ---

			dbr.put("presa_carico_oper", strCodOperatore);
			dbr.put("urgente", h_1.get("pr_flag_urgente"));

			dbr.put("sospesa_flag", "N");
			// 12/12/08: verranno casomai rivalorizzati dall'agenda ---
			dbr.put("soc_carico", "N");
			dbr.put("san_carico", "N");
			// 12/12/08 ---

			dbr.put("cod_operatore", strCodOperatore);
			String presidioOp = (String) it.pisa.caribel.util.ISASUtil.getDecode(dbc, "operatori", "codice", strCodOperatore,
					"cod_presidio");
			dbr.put("cod_presidio", presidioOp);

			dbr.put("n_cartella", h_1.get("n_cartella"));
			dbr.put("pr_data", h_1.get("pr_data"));
			dbr.put("pr_progr", h_1.get("pr_progr"));
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrCampiCriticiNew - " + e1);
		}
	}

	private void updateAgendant_pua(ISASConnection dbc, ISASRecord dbr) throws Exception {
		String strOperatore = (String) dbr.get("presa_carico_oper");
		String strDtAppuntamento = "" + dbr.get("ag_data_app");
		String strOraAppuntamento = (String) dbr.get("ag_ora_app");
		String strPresaCarico = (String) dbr.get("presa_carico");

		try {
			String strQuery = "SELECT *" + " FROM agendant_pua" + " WHERE ag_cod_oper = '" + strOperatore + "'" + " AND ag_data_app = "
					+ formatDate(dbc, strDtAppuntamento) + " AND ag_ora_app = '" + strOraAppuntamento + "'";
			mySystemOut("updateAgendant_pua/strQuery: " + strQuery);
			ISASRecord dbrAge = dbc.readRecord(strQuery);
			if (strPresaCarico.equals("S"))
				dbrAge.put("ag_esito", new Integer(1));
			else if (strPresaCarico.equals("C"))
				dbrAge.put("ag_esito", new Integer(2));
			else
				dbrAge.put("ag_esito", new Integer(3));

			dbc.writeRecord(dbrAge);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("SocElecasiEJB/updateAgendant_pua: " + e);
		}
	}

	// 10/03/08: generazione num verbale x anno e x zona
	private int faiNumVerbale(ISASConnection dbc, String dtVerbale, String nVerbale, String codOper) throws Exception {
		int numVerb = 0;
		String zona = "";

		boolean giaVerbaliz = ((nVerbale != null) && (!nVerbale.trim().equals("")) && (!nVerbale.trim().equals("0")));
		if ((dtVerbale != null) && (!dtVerbale.trim().equals("")) && (!giaVerbaliz)) {
			String anno = dtVerbale.substring(0, 4);
			zona = (String) it.pisa.caribel.util.ISASUtil.getDecode(dbc, "operatori", "codice", codOper, "cod_zona");

			String key_ChiaviLibere = "VERBALEUVM_" + anno + "_" + zona;
			numVerb = selectProgressivo(dbc, key_ChiaviLibere);
		}
		return numVerb;
	}

	// 24/10/08: generazione num protocollo x anno e x zona
	private String faiNumProtocollo(ISASConnection dbc, String dtSkPuac, String numProto, String codOper) throws Exception {
		int numProtoMax = 0;
		String anno = "";
		String zona = "";

		boolean giaProtocollata = ((numProto != null) && (!numProto.trim().equals("")) && (!numProto.trim().equals("0")));
		if ((dtSkPuac != null) && (!dtSkPuac.trim().equals("")) && (!giaProtocollata)) {
			anno = dtSkPuac.substring(0, 4);
			zona = (String) it.pisa.caribel.util.ISASUtil.getDecode(dbc, "operatori", "codice", codOper, "cod_zona");

			String key_ChiaviLibere = "PUAUVM_NUMPROTO_" + anno + "_" + zona;
			numProtoMax = selectProgressivo(dbc, key_ChiaviLibere);
		}
		if (numProtoMax > 0)
			return (anno + "/" + numProtoMax);
		return null;
	}

	// 10/11/08
	private String decodPresidio(ISASConnection mydbc, String codPres) throws Exception {
		String desc = "";
		if ((codPres == null) || ("".equals(codPres)))
			return "";

		String sel = "SELECT p.* FROM presidi p" + " WHERE p.codpres = '" + codPres + "'" + " AND EXISTS (SELECT k1.* FROM conf k1"
				+ " WHERE p.codreg = k1.conf_txt" + " AND k1.conf_kproc = 'SINS'" + " AND k1.conf_key = 'codice_regione')"
				+ " AND EXISTS (SELECT k2.* FROM conf k2" + " WHERE p.codazsan = k2.conf_txt" + " AND k2.conf_kproc = 'SINS'"
				+ " AND k2.conf_key = 'codice_usl')";

		ISASRecord rec = mydbc.readRecord(sel);
		if ((rec != null) && (rec.get("despres") != null))
			desc = (String) rec.get("despres");
		return desc.trim();
	}

	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, String nCartella, String strDtSkVal) throws Exception {
		String dt = strDtSkVal;
		// 25/06/07  dt = dt.substring(0,2) + dt.substring(3,5) + dt.substring(6,10);
		dt = dt.substring(8, 10) + dt.substring(5, 7) + dt.substring(0, 4);
		DataWI dataWIApertura = new DataWI(dt);

		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" + " FROM progetto" + " WHERE n_cartella = " + nCartella
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
		// se data_apertura ï¿½ maggiore di data_chiusura restituisce 1
		// se data_apertura ï¿½ minore di data_chiusura restituisce 2
		// se data_apertura ï¿½ = di data_chiusura restituisce 0
		// se da errore -1
		if ((rit == 2) || (rit == 0))
			return true; // Non ammissibile
		else if (rit < 0) {
			throw new Exception("SocAssProgettoEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
			// Si ï¿½ verificato un errore nel metodo di confronto delle 2 date.
		} else
			// (rit == 1)
			return false; // Ammissibile
	}

	private boolean dtApeProgettoLTDtApeCartella(ISASConnection dbc, String nCartella, String strDtSkVal) throws Exception {
		String mySel = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + nCartella + " AND data_apertura > "
				+ formatDate(dbc, strDtSkVal);

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile
		else
			return true;
	}

	// ========================================== GESTIONE CASI x FLUSSI AD-RSA ============================

	private int gestione_caso(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov) throws Exception {
		mySystemOut("gestione_caso -- HASH: " + h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;
		int origine = -1;

		if (h.get("id_caso") != null && !h.get("id_caso").equals("-1")) // esiste un caso
		{
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(h.get("stato").toString());
			id_caso = Integer.parseInt(h.get("id_caso").toString());
			origine = Integer.parseInt(h.get("origine").toString());
		}

		// se sono in INSERT della scheda di valutazione e non esiste nessun caso oppure
		// esiste ma e' sanitario o concluso allora devo creare un caso UVM
		if (prov.equals("insert")
				&& (id_caso == -1 || (id_caso != -1 && (origine != gestore_casi.CASO_UVM || stato_caso == gestore_casi.STATO_CONCLU)))) {
			mySystemOut("gestione_caso() - caso DA CREARE");
			try {
				if (h.get("pr_data") == null)
					h.put("pr_data", dbr.get("pr_data"));

				if (!h.containsKey("dt_segnalazione") || (h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals("")))
					h.put("dt_segnalazione", h.get("pr_data_puac"));

				// 26/06/09 Elisa: per non far inserire il "motivo conclusione" nel record del caso al momento della
				// sua creazione
				if (h.containsKey("motivo"))
					h.put("motivo", new Integer(0));

				h.put("tipo_caso", new Integer(GestCasi.CASO_UVM));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_UVM));

				id_caso = gestore_casi.apriCasoUvm(dbc, h).intValue();
				mySystemOut("gestione_caso() - caso creato id_caso=[" + id_caso + "]");
				return id_caso;
			} catch (Exception e) {
				System.out.println("RLSkPuacEJB: gestione_caso() " + e);
				throw e;
			}
		} else
			return id_caso; // se sono in update dovrei avere gia' il caso! Lo ritorno e basta...
	}

	// 20/05/09 Elisa Croci
	/* 1) Il caso non esiste: creo il caso e la segnalazione
	 * 2) Il caso esiste ma e' chiuso: creo il caso e la segnalazione
	 * 3) Il caso e' attivo: aggiorno la segnalazione
	*/
	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov) throws NumberFormatException,
			ISASMisuseException, CariException {
		mySystemOut("gestione_segnalazione -- HASH: " + h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;
		int origine = -1;

		h.put("operZonaConf", (String) dbr.get("pr_oper_ultmod")); // 15/10/09

		Hashtable hCaso = new Hashtable();

		if (h.containsKey("id_caso") && (h.get("id_caso") != null && !h.get("id_caso").equals("-1"))) {
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(h.get("stato").toString());
			id_caso = Integer.parseInt(h.get("id_caso").toString());

			if (dbr.getHashtable().containsKey("origine"))
				origine = Integer.parseInt(dbr.get("origine").toString());
		}

		mySystemOut("gestione_segnalazione -- stato caso: " + stato_caso + " id_caso " + id_caso + " origine " + origine);

		// se sono in insert e il caso non esiste oppure
		// esiste MA e' sanitario o concluso, devo crearne uno!
		if (prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU || origine != GestCasi.CASO_UVM)) {
			mySystemOut("gestione_segnalazione -- INSERT ");

			// creo il caso UVM
			try {
				h.put("tipo_caso", new Integer(GestCasi.CASO_UVM));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_UVM));

				if (!h.containsKey("dt_segnalazione") || (h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals("")))
					h.put("dt_segnalazione", h.get("pr_data_puac"));

				/*if(!h.containsKey("dt_presa_carico") || (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals("")))
					h.put("dt_presa_carico", h.get("dt_segnalazione"));*/

				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
				// arriva la data del progetto, cosi' me la copio dal dbr!
				h.put("pr_data", dbr.get("pr_data"));

				// 26/06/09 Elisa: per non far inserire il "motivo conclusione" nel record del caso al momento della
				// sua creazione
				if (h.containsKey("motivo"))
					h.put("motivo", new Integer(0));

				ISASRecord rec_segn = gestore_segnalazioni.insert(dbc, h);

				if (rec_segn != null) {
					Enumeration en = rec_segn.getHashtable().keys();
					while (en.hasMoreElements()) {
						String chiave = en.nextElement().toString();
						dbr.put(chiave, rec_segn.get(chiave));
					}

					return Integer.parseInt(rec_segn.get("id_caso").toString());
				} else
					return -1;
			} catch (CariException e) // 17/11/09
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, insert -- " + e);
				throw e;
			} catch (DBRecordChangedException e) {
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} catch (ISASPermissionDeniedException e) {
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} catch (SQLException e) {
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} catch (Exception e) {
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
		}
		// il caso esiste e non e' concluso
		else if (id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU && stato_caso != -1)) {
			mySystemOut("gestione_segnalazione -- UPDATE ");

			// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione
			try {
				Enumeration e = dbr.getHashtable().keys();
				while (e.hasMoreElements()) {
					String chiave = e.nextElement().toString();

					if (!h.containsKey(chiave))
						h.put(chiave, dbr.get(chiave));
				}

				mySystemOut("gestione_segnalazione, DBR: " + dbr.getHashtable().toString());
				ISASRecord new_segnalazione = gestore_segnalazioni.update(dbc, h);

				if (new_segnalazione != null) {
					Enumeration en = new_segnalazione.getHashtable().keys();
					while (en.hasMoreElements()) {
						String chiave = en.nextElement().toString();
						dbr.put(chiave, new_segnalazione.get(chiave));
					}
				}

				return id_caso;
			} catch (CariException e) // 17/11/09
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, update -- " + e);
				throw e;
			} catch (Exception e) {
				System.out.println("RLSkPuacEJB gestione_segnalazione, update() -- " + e);
				return id_caso;
			}
		} else
			return id_caso;
	}

	private void gestione_presacarico_rivalutazione(ISASConnection dbc, ISASRecord dbr, Hashtable h) throws Exception, CariException {
		mySystemOut("gestione_presacarico() HASH -- " + h.toString() + " \n DBR == " + dbr.getHashtable().toString());
		int tempoT = -1;

		if (h.containsKey("tempo_t"))
			tempoT = Integer.parseInt(h.get("tempo_t").toString());
		else
			tempoT = Integer.parseInt(dbr.get("tempo_t").toString());

		h.put("operZonaConf", (String) dbr.get("pr_oper_ultmod")); // 15/10/09

		// 23/10/09: x EVENTO PRESACARICO ---
		if ((!h.containsKey("liv_isogravita")) && (dbr.get("liv_isogravita") != null))
			h.put("liv_isogravita", dbr.get("liv_isogravita"));
		// 23/10/09 ---

		try {
			// presa carico
			if (tempoT == 0) {
				mySystemOut("-- INSERISCO LA PRESA IN CARICO");

				if (!h.containsKey("dt_presa_carico") || (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals("")))
					h.put("dt_presa_carico", h.get("pr_data_verbale_uvm"));

				/** 27/12/10					
								Integer result = gestore_casi.presaCaricoCasoUvm(dbc, h);
								mySystemOut("gestore casi, PRESA CARICO UVM? " + result);
				**/

				int ubic = Integer.parseInt(ISASUtil.getValoreStringa(h, "ubicazione").toString());
				if (ubic != GestCasi.UBI_RTOSC) {
					if (!h.containsKey("dt_valutazione") || h.get("dt_valutazione") == null || h.get("dt_valutazione").equals(""))
						h.put("dt_valutazione", h.get("pr_data_verbale_uvm"));
				}

				// 27/12/10		ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);	
				// 27/12/10 ---
				ISASRecord rec_pc = null;
				if ((h.get("update_presacar") != null) && ((h.get("update_presacar").toString()).trim().equals("S")))
					rec_pc = gestore_presacarico.update(dbc, h);
				else {
					Integer result = gestore_casi.presaCaricoCasoUvm(dbc, h);
					mySystemOut("gestore casi, PRESA CARICO UVM? " + result);

					rec_pc = gestore_presacarico.insert(dbc, h);
				}
				// 27/12/10 ---

				if (rec_pc != null) {
					Enumeration en1 = rec_pc.getHashtable().keys();
					while (en1.hasMoreElements()) {
						String chiave = en1.nextElement().toString();
						dbr.put(chiave, rec_pc.get(chiave));
					}

					dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());

					// 27/12/10
					dbr.put("update_presacar", "S");
				}
			} else // rivalutazione perche' T > 0
			{
				mySystemOut("-- INSERISCO RIVALUTAZIONE");

				if (!h.containsKey("dt_rivalutazione") || h.get("dt_rivalutazione") == null || h.get("dt_rivalutazione").equals(""))
					h.put("dt_rivalutazione", h.get("pr_data_verbale_uvm"));

				// 27/12/10		gestore_casi.rivalutazCasoUvm(dbc, h);

				// 27/12/10		ISASRecord rec_riv = gestore_rivalutazioni.insert(dbc, h);
				// 27/12/10
				ISASRecord rec_riv = null;
				if ((h.get("update_rivalutazione") != null) && ((h.get("update_rivalutazione").toString()).trim().equals("S"))) {
					h.put("progr", "" + tempoT);
					rec_riv = gestore_rivalutazioni.update(dbc, h);
				} else {
					gestore_casi.rivalutazCasoUvm(dbc, h);

					rec_riv = gestore_rivalutazioni.insert(dbc, h);
				}
				// 27/12/10 ---

				if (rec_riv != null) {
					Enumeration en1 = rec_riv.getHashtable().keys();
					while (en1.hasMoreElements()) {
						String chiave = en1.nextElement().toString();
						dbr.put(chiave, rec_riv.get(chiave));
					}

					// 27/12/10
					dbr.put("update_rivalutazione", "S");
				}
			}
		} catch (CariException e) // 17/11/09
		{
			System.out.println("RLSkPuacEJB gestione_presacarico() -- " + e);
			throw e;
		} catch (NumberFormatException e) {
			System.out.println("RLSkPuacEJB gestione_presacarico() " + e);
			throw e;
		} catch (ISASMisuseException e) {
			System.out.println("RLSkPuacEJB gestione_presacarico() " + e);
			throw e;
		}
	}

	// 25/05/09 Elisa Croci
	// 24/12/10	private void prendi_presacarico_rivalutazione(ISASConnection dbc, int caso,ISASRecord dbr)
	private void prendi_presacarico_rivalutazione(ISASConnection dbc, int caso, ISASRecord dbr, Integer tempoT) {
		try {
			mySystemOut("prendi_presacarico_rivalutazione: tempo_t=[" + tempoT.toString() + "]");
			if (caso != -1) {
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				h.put("ubicazione", dbr.get("ubicazione"));

				// 24/12/10		if(Integer.parseInt(dbr.get("tempo_t").toString()) == 0)
				if ((tempoT.intValue()) == 0) {

					mySystemOut("prendi_presacarico_rivalutazione: PRENDO PRESA CARICO");
					ISASRecord res = gestore_presacarico.queryKey(dbc, h);

					if (res != null) {
						Enumeration e = res.getHashtable().keys();
						while (e.hasMoreElements()) {
							String chiave = e.nextElement().toString();
							dbr.put(chiave, res.get(chiave));
						}

						dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());

						// 27/12/10
						dbr.put("update_presacar", "S");
					}
				} else {
					mySystemOut("prendi_presacarico_rivalutazione: PRENDO RIVALUTAZIONE");
					h.put("pr_progr", dbr.get("pr_progr"));

					// 24/12/10: la chiave di RT/M_RIVALUTAZIONE ï¿½ "progr"
					h.put("progr", tempoT.toString());

					ISASRecord res = gestore_rivalutazioni.queryKey(dbc, h);

					if (res != null) {
						Enumeration e = res.getHashtable().keys();
						while (e.hasMoreElements()) {
							String chiave = e.nextElement().toString();
							dbr.put(chiave, res.get(chiave));
						}

						if (h.get("ubicazione").equals(Integer.toString(GestCasi.UBI_ALTRO)))// 24/12/10: solo su M_RIVALUTAZIONE
							dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());

						// 27/12/10
						dbr.put("update_rivalutazione", "S");
					}
				}

				// La presa in carico e la rivalutazione sono sempre in inserimento per ora,
				// quindi sugger
				if (!ISASUtil.getValoreStringa(h, "ubicazione").equals(Integer.toString(GestCasi.UBI_RTOSC))) {
					if (dbr.get("id_cartella") == null || dbr.get("id_cartella").equals(""))
						dbr.put("id_cartella", dbr.get("n_cartella"));
				}
			}
		} catch (ISASMisuseException e1) {
			System.out.println("RLSkPuacEJB prendi_presacarico_rivalutazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB prendi_presacarico_rivalutazione, fallimento! -- " + e);
		}
	}

	// 20/05/09 Elisa Croci
	// prendo la segnalazione relativa al caso a cui il contatto deve fare riferimento
	private boolean prendi_segnalazione(ISASConnection dbc, int caso, ISASRecord dbr) {
		try {
			/* prendo la segnalazione solo se il caso esiste e se sono in un contesto in cui si
				gestiscono le segnalazioni
			*/
			if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_UVM) && caso != -1) {
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
			System.out.println("RLSkPuacEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
			return false;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB prendi_segnalazione, fallimento! -- " + e);
			return false;
		}
	}

	// 20/05/09 Elisa Croci
	// dato un contatto, prendo il caso attivo se esiste altrimenti quello chiuso piu' recente!
	private int prendi_dati_caso(ISASConnection dbc, ISASRecord dbr) {
		mySystemOut(" prendi_dati_caso DBR == " + dbr.getHashtable().toString());
		Hashtable h = new Hashtable();

		try {
			h.put("n_cartella", dbr.get("n_cartella"));
			h.put("pr_data", dbr.get("pr_data"));

			mySystemOut("-- prendi dati caso: " + h.toString());

			ISASRecord rec = null;

			if (dbr.getHashtable().containsKey("tempo_t")) {
				if (dbr.get("tempo_t").toString().equals("0"))
					rec = gestore_casi.getCasoRifUvm(dbc, h);
				else
					rec = gestore_casi.getCasoRif(dbc, h);
			} else
				rec = gestore_casi.getCasoRifUvm(dbc, h);

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
			System.out.println("RLSkPuacEJB prendi_dati_caso, manca chiave primaria! -- " + e);
			return -1;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB prendi_dati_caso, fallimento! -- " + e);
			return -1;
		}
	}

	// ========================================== GESTIONE SCALE x FLUSSI AD-RSA ============================

	// 21/05/09 m.: lettura scale con dtMax per un certo tempoT dopo la conclusione
	// del CASO precedente
	public ISASRecord getScaleMaxPerT(myLogin mylogin, Hashtable h) throws Exception {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			mySystemOut("getScaleMaxPerT - h=[" + h.toString() + "]");

			// lettura dtConclusione CASO precedente
//			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
//			h.put("dtchius_casoprec", dtChiusCasoPrec);

			ISASRecord dbr = gest_scaleVal.getScaleMaxPerT(dbc, h);

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("RLSkPuacEJB.getScaleMaxPerT: e=" + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	// 21/05/09 m.: lettura dtConclusione CASO precedente
	public String getDtChiuSksoPrec(myLogin mylogin, Hashtable h) throws Exception {
		String dtChiusPrec = "";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
		String sql = "select max(pr_data_chiusura) pr_data_chiusura from rm_skso a where n_cartella = "+h.get(CostantiSinssntW.N_CARTELLA).toString()+
						" and pr_data_chiusura is not null";
		ISASRecord lastSksoChiu = dbc.readRecord(sql);

		if ((lastSksoChiu != null) && (lastSksoChiu.get("pr_data_chiusura") != null))
			dtChiusPrec = "" + lastSksoChiu.get("pr_data_chiusura");
		mySystemOut("getDtChiuCasoPrec - dtChiusPrec=[" + dtChiusPrec + "]");

		return dtChiusPrec;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			logout_nothrow("getDtChiuSksoPrec", dbc);
		}
	}
	// 11/06/09 m.
	public Integer query_getTempoTAllaData(myLogin mylogin, Hashtable h) throws Exception {
		boolean done = false;
		ISASConnection dbc = null;

		Integer ret = new Integer(-1);

		try {
			dbc = super.logIn(mylogin);

			mySystemOut(" query_getTempoTAllaData() == H per getCasoAllaData() == " + h.toString());
			ISASRecord dbr = (ISASRecord) gestore_casi.getCasoAllaData(dbc, h);

			if ((dbr != null) && (dbr.get("tempo_t") != null))
				ret = (Integer) dbr.get("tempo_t");

			dbc.close();
			super.close(dbc);
			done = true;
			return ret;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB.query_getTempoT(): " + e);
			throw e;
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
					return new Integer(-1);
				} catch (Exception e2) {
					System.out.println("RLSkPuacEJB.query_getTempoT(): " + e2);
					throw e2;
				}
			}
		}
	}

	// ===================================== FINE GESTIONE SCALE x FLUSSI AD-RSA ============================

	// 06/06/08
	public String duplicateChar(String s, String c) {
		if ((s == null) || (c == null))
			return s;
		String mys = new String(s);
		int p = 0;
		while (true) {
			int q = mys.indexOf(c, p);
			if (q < 0)
				return mys;
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	private void mySystemOut(String msg) {
		if (myDebug)
			System.out.println(nomeEJB + ": " + msg);
	}

	// 03/06/09 Elisa Croci
	private void cancellaAllCompCommPUACUVM(ISASConnection dbc, String n_cartella, String pr_data, String pr_progr) throws Exception {
		mySystemOut(" cancellaAllCompCommPUACUVM ");
		try {
			String sel = "SELECT * FROM puauvm_commissione WHERE " + " n_cartella = " + n_cartella + " AND pr_data = "
					+ formatDate(dbc, pr_data) + " AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllCompCommPUACUVM -- " + sel);
			ISASCursor dbcur = dbc.startCursor(sel);

			Vector vdbr = dbcur.getAllRecord();
			if ((vdbr != null) && (vdbr.size() > 0)) {
				for (int i = 0; i < vdbr.size(); i++) {
					ISASRecord dbrec = (ISASRecord) vdbr.get(i);
					if (dbrec != null) {
						String sel2 = sel + " AND pr_presenza = " + dbrec.get("pr_presenza");

						ISASRecord dbrD = dbc.readRecord(sel2);
						dbc.deleteRecord(dbrD);
					}
				}
			}

			if (dbcur != null)
				dbcur.close();

		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: cancellaAllCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private void scriviAllCompCommPUACUVM(ISASConnection dbc, Vector vCompComm, String codice_medico) throws Exception {
		mySystemOut(" scriviAllCompCommPUACUVM ");
		try {
			for (int i = 0; i < vCompComm.size(); i++) {
				Hashtable hash = (Hashtable) vCompComm.get(i);
				mySystemOut("scriviAllCompCommPUACUVM HASH " + i + " == " + hash.toString());
				scriviCompCommPUACUVM(dbc, hash, codice_medico, i);
			}
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: scriviAllCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private void scriviCompCommPUACUVM(ISASConnection dbc, Hashtable h, String codice_medico, int i) throws Exception {
		mySystemOut(" scriviCompCommPUACUVM -- H = " + h.toString());
		try {
			ISASRecord rec = dbc.newRecord("puauvm_commissione");

			Enumeration e = h.keys();
			while (e.hasMoreElements()) {
				String key = e.nextElement().toString();
				mySystemOut(" Chiave: " + key);
				rec.put(key, h.get(key));
			}

			rec.put("pr_presenza", new Integer(i));

			mySystemOut(" Scrivere REC: " + rec.getHashtable().toString());
			dbc.writeRecord(rec);
			mySystemOut(" Scritto!!! ");
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: scriviCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private void leggiCompCommPUACUVM(ISASConnection dbc, ISASRecord dbr, String n_cartella, String pr_data, String pr_progr,
			String codice_medico) throws Exception {
		mySystemOut("  leggiCompCommPUACUVM == pr_data " + pr_data + " n_cartella " + n_cartella + " pr_progr " + pr_progr);
		String select = "SELECT * FROM puauvm_commissione WHERE " + " pr_data = " + formatDate(dbc, pr_data) + " AND n_cartella = "
				+ n_cartella + " AND pr_progr = " + pr_progr + " ORDER BY pr_presenza ";

		mySystemOut("  leggiCompCommPUACUVM == " + select);
		try {
			ISASCursor dbcur = dbc.startCursor(select);
			Vector vdbr = dbcur.getAllRecord();
			Vector result = new Vector();

			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord rec = (ISASRecord) vdbr.get(i);
				if (rec.get("pr_tipo") != null && !rec.get("pr_tipo").equals("")) {
					String qualifica = rec.get("pr_tipo").toString();

					rec.put("desc_qualifica", hash_tp_oper.get(qualifica).toString());
					String cognome = "";
					String nome = "";

					if (qualifica.equals(codice_medico)) {
						cognome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "mecogn", "cognome");
						nome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "menome", "nome");
					} else {
						cognome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "cognome", "cognome");
						nome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "nome", "nome");
					}

					rec.put("cognome_op", cognome);
					rec.put("nome_op", nome);
				}

				result.add(rec);
			}

			if (dbcur != null)
				dbcur.close();

			for (int i = 0; i < result.size(); i++) {
				ISASRecord r = (ISASRecord) result.get(i);
				mySystemOut("leggiCompCommPUACUVM - RECORD " + i + " == " + r.getHashtable().toString());
			}

			dbr.put("griglia_componentiCommPUAUVM", result);
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: leggiCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private String getDescrChiaveConf(ISASConnection dbc, String chiave) {
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + " conf_kproc = 'SINS' " + "" + " AND conf_key = '" + chiave + "' ";

			System.out.println(" getDescrChiaveConf " + mysel);
			ISASRecord dbtxt = dbc.readRecord(mysel);

			if (dbtxt != null)
				return dbtxt.get("conf_txt").toString();
			else
				return null;
		} catch (Exception ex) {
			return null;
		}
	}

	private Hashtable faiHashTpOper() {
		try{
			return ManagerOperatore.getTipiOperatori(null);
		}catch(Exception ex){
			LOG.error("Si Ã¨ verificato un errore in "+this.getClass().getName()+".faiHashTpOper - Exception:"+ex);
			return null;
		}
	}

	// 13/10/09: mappa il motivo di chiusura skPuac con quello della skPuntoInsieme
	private static Hashtable faiHashMapMotChius() {
		Hashtable h_mapMC = new Hashtable();

		h_mapMC.put("0", "0"); // . - .
		h_mapMC.put("1", "2"); // Annullamento - Inappropriatezza dell'invio
		h_mapMC.put("2", "3"); // Rinuncia - Rinuncia
		h_mapMC.put("3", "9"); // Trasferimento - Altro
		h_mapMC.put("4", "1"); // Decesso - Deceduto/a
		h_mapMC.put("5", "9"); //  xxx - Altro
		return h_mapMC;
	}

	// 29/12/09
	private void cancAllRtPap(ISASConnection dbc, String n_cartella, String pr_data, String pr_progr) throws Exception {
		boolean done = false;
		ISASCursor dbcur = null;

		try {
			String sel = "SELECT * FROM rt_altro" + " WHERE n_cartella = " + n_cartella + " AND pr_data = " + formatDate(dbc, pr_data)
					+ " AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllRtPap - 1 -- " + sel);
			dbcur = dbc.startCursor(sel);

			Vector vdbr = dbcur.getAllRecord();
			if ((vdbr != null) && (vdbr.size() > 0)) {
				for (int i = 0; i < vdbr.size(); i++) {
					ISASRecord dbrec = (ISASRecord) vdbr.get(i);
					if (dbrec != null) {
						String sel2 = sel + " AND pr_num = " + dbrec.get("pr_num");

						ISASRecord dbrD = dbc.readRecord(sel2);
						dbc.deleteRecord(dbrD);
					}
				}
			}

			if (dbcur != null)
				dbcur.close();
			dbcur = null;

			String sel_1 = "SELECT * FROM rt_domdir" + " WHERE n_cartella = " + n_cartella + " AND pr_data = " + formatDate(dbc, pr_data)
					+ " AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllRtPap - 2 -- " + sel_1);
			dbcur = dbc.startCursor(sel_1);

			Vector vdbr_1 = dbcur.getAllRecord();
			if ((vdbr_1 != null) && (vdbr_1.size() > 0)) {
				for (int i = 0; i < vdbr_1.size(); i++) {
					ISASRecord dbrec = (ISASRecord) vdbr_1.get(i);
					if (dbrec != null) {
						String sel2 = sel_1 + " AND pr_num = " + dbrec.get("pr_num");

						ISASRecord dbrD = dbc.readRecord(sel2);
						dbc.deleteRecord(dbrD);
					}
				}
			}

			if (dbcur != null)
				dbcur.close();

			String sel_2 = "SELECT * FROM rt_pap" + " WHERE n_cartella = " + n_cartella + " AND pr_data = " + formatDate(dbc, pr_data)
					+ " AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllRtPap - 3 -- " + sel_2);
			ISASRecord dbr3 = dbc.readRecord(sel_2);
			if (dbr3 != null)
				dbc.deleteRecord(dbr3);

			done = true;
		} catch (Exception e) {
			System.out.println("RLSkPuacEJB: cancellaAllRtPap [" + e + "]");
			throw e;
		} finally {
			if (!done) {
				if (dbcur != null)
					dbcur.close();
			}
		}
	}

	// 29/12/09
	private void cancAssAnagra(ISASConnection dbc, Hashtable h0) throws Exception {
		ISASRecord recAssAna = getRecAssAna(dbc, h0);
		if (recAssAna != null) {
			// cancellazione agende collegate
			Integer progrAA = (Integer) recAssAna.get("progressivo");
			if (progrAA != null)
				deleteAgenda(dbc, progrAA.toString());

			dbc.deleteRecord(recAssAna);
		}
	}

	// 29/12/09
	private void pulisciAssAnagra(ISASConnection dbc, Hashtable h0) throws Exception {
		ISASRecord recAssAna = getRecAssAna(dbc, h0);
		if (recAssAna != null) {
			recAssAna.put("presa_carico", (String) "N");
			recAssAna.put("presa_carico_data", (java.sql.Date) null);
			recAssAna.put("presa_carico_motivo", (String) null);
			recAssAna.put("presa_carico_oper", (String) null);
			dbc.writeRecord(recAssAna);
		}
	}

	// 29/12/09
	private void chiudiCasoUVM(ISASConnection dbc, ISASRecord dbr) throws Exception {
		// devo chiudere anche il caso UVM
		DataWI dtOggi = new DataWI();
		Hashtable hDati = dbr.getHashtable();
		hDati.put("dt_conclusione", dtOggi.getFormattedString2(1));
		// 04/01/10 ----
		String motChiuFlux = getTabVociCodReg(dbc, "VALPCMCH", "1");
		hDati.put("motivo", motChiuFlux);
		// 04/01/10 ----
		hDati.put("operZonaConf", (String) dbr.get("pr_oper_ultmod"));
		mySystemOut("Dati per chiusura caso dovuta a cancellazione scheda PUAC: " + hDati.toString());
		gestore_casi.chiudiCaso(dbc, hDati);
	}

	// 30/12/09
	private void deleteAgenda(ISASConnection mydbc, String progr) throws Exception {
		boolean done = false;
		ISASCursor dbcur = null;
		ISASCursor dbcur_2 = null;

		try {
			// AGENDANT_PUA
			String mysel = "SELECT * FROM agendant_pua"
					+ " WHERE (ag_cod_tipo_appu='COL' OR ag_cod_tipo_appu='VI1' OR ag_cod_tipo_appu='VI2')" + " AND ag_num_scheda=" + progr;

			debugMessage("RLSkPuacEJB/deleteAgenda PUA - SELECT mysel: " + mysel);
			dbcur = mydbc.startCursor(mysel);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr_1 = (ISASRecord) dbcur.getRecord();
					String sel_2 = "SELECT * FROM agendant_pua" + " WHERE ag_cod_oper='" + ((String) dbr_1.get("ag_cod_oper")).trim() + "'"
							+ " AND ag_data_app=" + formatDate(mydbc, "" + dbr_1.get("ag_data_app")) + " AND ag_ora_app='"
							+ (String) dbr_1.get("ag_ora_app") + "'";

					ISASRecord dbr_2 = mydbc.readRecord(sel_2);
					mydbc.deleteRecord(dbr_2);
				}
			}
			dbcur.close();
			dbcur = null;

			// AGENDANT_INTERV
			String mysel_1 = "SELECT * FROM agendant_interv" + " WHERE num_scheda_pua =" + progr;

			debugMessage("RLSkPuacEJB/deleteAgenda INTERV - SELECT mysel_1: " + mysel_1);
			dbcur = mydbc.startCursor(mysel_1);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr_3 = (ISASRecord) dbcur.getRecord();
					String sel_4 = "SELECT * FROM agendant_interv" + " WHERE ag_oper_ref='" + ((String) dbr_3.get("ag_oper_ref")).trim()
							+ "'" + " AND ag_data=" + formatDate(mydbc, "" + dbr_3.get("ag_data")) + " AND ag_progr="
							+ (String) dbr_3.get("ag_progr");

					// AGENDANT_INTPRE ----
					String mysel_2 = "SELECT * FROM agendant_intpre" + " WHERE ap_oper_ref='" + ((String) dbr_3.get("ag_oper_ref")).trim()
							+ "'" + " AND ap_data=" + formatDate(mydbc, "" + dbr_3.get("ag_data")) + " AND ap_progr="
							+ (String) dbr_3.get("ag_progr");

					dbcur_2 = mydbc.startCursor(mysel_2);
					if ((dbcur_2 != null) && (dbcur_2.getDimension() > 0)) {
						while (dbcur.next()) {
							ISASRecord dbr_5 = (ISASRecord) dbcur_2.getRecord();
							String sel_6 = "SELECT * FROM agendant_intpre" + " WHERE ap_oper_ref='"
									+ ((String) dbr_5.get("ap_oper_ref")).trim() + "'" + " AND ap_data="
									+ formatDate(mydbc, "" + dbr_5.get("ap_data")) + " AND ap_progr=" + (String) dbr_5.get("ap_progr")
									+ " AND ap_prest_cod = '" + (String) dbr_5.get("ap_prest_cod") + "'";

							ISASRecord dbr_6 = mydbc.readRecord(sel_6);
							mydbc.deleteRecord(dbr_6);
						}
					}
					dbcur_2.close();
					dbcur_2 = null;
					// AGENDANT_INTPRE ----

					ISASRecord dbr_4 = mydbc.readRecord(sel_4);
					mydbc.deleteRecord(dbr_4);
				}
			}
			dbcur.close();

			done = true;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una deleteAgenda() ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					if (dbcur_2 != null)
						dbcur_2.close();
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 04/01/10
	private String getTabVociCodReg(ISASConnection dbc, String tbCod, String tbVal) throws Exception {
		String codReg = "99";
		String sel = "SELECT tab_codreg FROM tab_voci" + " WHERE tab_cod = '" + tbCod + "'" + " AND tab_val = '" + tbVal + "'";

		ISASRecord dbr1 = dbc.readRecord(sel);
		if ((dbr1 != null) && (dbr1.get("tab_codreg") != null))
			codReg = (String) dbr1.get("tab_codreg");
		return codReg;
	}

	// 28/12/10: verifica esistenza di SkPuac aperte dopo quella in oggetto
	public Boolean query_checkLastSkPuac(myLogin mylogin, Hashtable h0) {
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String) h0.get("n_cartella");
			String pr_progr = (String) h0.get("pr_progr");
			String dtSkPuac = (String) h0.get("pr_data_puac");

			String myselect = "SELECT * FROM rl_puauvm" + " WHERE n_cartella = " + cart + " AND pr_data_puac >= "
					+ formatDate(dbc, dtSkPuac) + " AND pr_progr <> " + pr_progr;

			debugMessage("RLSkPuacEJB.query_checkLastSkPuac - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(!risu));
		} catch (Exception e1) {
			System.out.println("RLSkPuacEJB.query_checkLastSkPuac - Eccezione=[" + e1 + "]");
			return (Boolean) null;
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	
	public String esisteProrogheSospensioniAttive(myLogin myLogin,Hashtable dati){
		String punto = ver + "esisteProrogheSospensioniAttive ";
		ISASConnection dbc = null;
		String attiveSospensione = CostantiSinssntW.CTS_NESSUNO_ATTIVI;
		String nCartella = ISASUtil.getValoreStringa(dati,CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		String prDataChiusura = ISASUtil.getValoreStringa(dati, CostantiSinssntW.PR_DATA_CHIUSURA);
		boolean prorogheAttive= false;
		boolean sospensioniAttive = false;
		try {
			dbc = super.logIn(myLogin);
			String query = "SELECT * FROM rm_skso_proroghe WHERE n_cartella = " +nCartella+ " AND id_skso = " +
					idSkso + " AND (" +formatDate(dbc, prDataChiusura)+ " < dt_proroga_fine  or dt_proroga_fine is null) ";
			LOG.debug(punto + " query>>" +query);
			ISASRecord dbr = dbc.readRecord(query);
			if (dbr != null) {
				prorogheAttive = true;
			}
			query = "SELECT * FROM rm_skso_sospensioni WHERE n_cartella = " +nCartella +" AND id_skso = " +
					idSkso + " AND (" + formatDate(dbc, prDataChiusura)+ 
					" < dt_sospensione_fine  or dt_sospensione_fine is null) ";
			LOG.debug(punto + " query>>" +query);
			ISASRecord dbrSospensioni = dbc.readRecord(query);
			if (dbrSospensioni!= null){
				sospensioniAttive = true;
			}
			
		}catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		
		if(prorogheAttive && sospensioniAttive){
			attiveSospensione = CostantiSinssntW.CTS_ATTIVI_PROROGHE_SOSPENSIONI;
		}else{
			if(prorogheAttive){
				attiveSospensione = CostantiSinssntW.CTS_ATTIVI_PROROGHE;
			}else {
				if(sospensioniAttive){
					attiveSospensione = CostantiSinssntW.CTS_ATTIVI_SOSPENSIONE;
				}
			}
		}
		
		LOG.debug(punto + " attiveSospensione>"+ attiveSospensione+"<");
		return attiveSospensione;
	}
	
	
	public ISASRecord esisteSovrapposizioneDtPrDataPuac(myLogin myLogin,Hashtable dati){
		String punto = ver + "esisteSovrapposizioneDtPrDataPuac ";
		LOG.info(punto + " inizio con dati>" + dati);
		ISASConnection dbc = null;
		String nCartella = ISASUtil.getValoreStringa(dati,CostantiSinssntW.N_CARTELLA);
		String prDataPuac = ISASUtil.getValoreStringa(dati,CostantiSinssntW.CTS_PR_DATA_PUAC);
		int idskso = ISASUtil.getValoreIntero(dati, CostantiSinssntW.CTS_ID_SKSO);
		boolean esclusoEstremo = ISASUtil.getvaloreBoolean(dati, ESCLUSO_ESTREMO);
		
		StringBuffer query = new StringBuffer();
		ISASRecord dbrRmSkso =null;
		try {
			dbc = super.logIn(myLogin);
			query.append("select * from rm_skso where n_cartella = ");
			query.append(nCartella);
			query.append(" and (  ( pr_data_puac < ");
			query.append(formatDate(dbc, prDataPuac));
			query.append(" and pr_data_chiusura >" );
			if (esclusoEstremo){
				query.append("= ");
			}
			query.append(formatDate(dbc, prDataPuac));
			query.append(" ) or  ( pr_data_puac < ");
			query.append(formatDate(dbc, prDataPuac));
			query.append(" and pr_data_chiusura is null )  )");
			if (idskso>0){
				query.append(" and id_skso <> " +idskso);
			}
			
			LOG.trace(punto + " query>>" +query);
			
			dbrRmSkso = dbc.readRecord(query.toString());
		}catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		
		LOG.debug(punto + " esisteSovrapposizione>"+ (dbrRmSkso!=null ? dbrRmSkso.getHashtable().toString(): " no dati") +"<");
		return dbrRmSkso;
	}
	
	// Inserimento su tabella richieste_chiusura
	public Boolean insertRichiestaChiusura(myLogin mylogin, Hashtable h) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		String punto = nomeEJB + "insertRichiestaChiusura ";
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : "no dati "));
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		boolean ret = false;
		try {
			dbc = super.logIn(mylogin);

			dbc.startTransaction(); 
			String n_cartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String id_skso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
			String zona_rich=ISASUtil.getValoreStringa(h, "cod_zona_richiedente");
			String data_richiesta = h.get("data_richiesta").toString();
			
			String sql = "select * from richieste_chiusura where n_cartella = "+n_cartella+
						" and id_skso = "+id_skso+
						" and cod_zona_richiedente = '"+zona_rich+"'"+
						" and data_richiesta = "+dbc.formatDbDate(data_richiesta);
			
			ISASRecord dbr_lett = dbc.readRecord(sql);
			if (dbr_lett!=null) throw new CariException(CostantiSinssntW.ESISTE_RICHIESTA_MSG);
			
			dbr = dbc.newRecord("richieste_chiusura");
						
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbc.writeRecord(dbr);
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			ret = true;
			
		}catch (CariException e) {
			LOG.trace(punto + " Errore generico " + e);
			throw e;
		}
		catch (Exception e) {
			LOG.trace(punto + " Errore generico " + e);
			e.printStackTrace();
			ret = false;
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una insert() - ", e);
		} finally {
			logout_nothrow(punto, dbc);		
		}
		return new Boolean(ret);
	}


	public ISASRecord recuperaRmSkSo(myLogin myLogin, Hashtable<String, String> dati) {
		String punto = ver + "recuperaRmSkSo ";
		LOG.info(punto + " inizio con dati>>" +dati);
		String nCartella =ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
		String nContatto =ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CONTATTO);
		String query = "select r.* from rm_skso r, skmedico m where r.n_cartella = m.n_cartella and r.id_skso = m.id_skso " +
				" and m.n_contatto = " + nContatto + " and r.n_cartella = " +nCartella;
		LOG.trace(punto + " query>"+ query);
		ISASConnection dbc = null;
		ISASRecord dbrRmSkso =null; 
		try {
			dbc = super.logIn(myLogin);
			dbrRmSkso = dbc.readRecord(query);
		} catch (Exception e) {
			LOG.error(punto + " Errore nel costruire query>>>"+ query);
		} finally{
			logout_nothrow(punto, dbc);
		}
		
		return dbrRmSkso;
	}


	//Verifico che per la cartella c'Ã¨ un PAI caricato
	public Boolean isPianificatoPAI(myLogin mylogin, Hashtable h) throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException, CariException {
		String punto = nomeEJB + "isPianificatoPAI ";
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : "no dati "));
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		boolean ret = false;
		try {
			dbc = super.logIn(mylogin);

			ret = isPianificatoPAITransactional(dbc, h);
			dbc.close();
			super.close(dbc);
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ punto + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(punto, dbc);		
		}
		return new Boolean(ret);
	}

	public boolean isPianificatoPAITransactional(ISASConnection dbc, Hashtable h) throws Exception {
		boolean ret;
		String n_cartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String id_skso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		String tipo_operatore =ISASUtil.getValoreStringa(h, CostantiSinssntW.TIPO_OPERATORE);
		String data_richiesta = h.get("data_richiesta").toString();

		String sql = "SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + n_cartella + " AND p.pr_data_chiusura IS NULL AND p.pr_data_PUAC <= "+dbc.formatDbDate(data_richiesta) + " AND ispianocongelato='S' ";

		ISASRecord dbr_lett = dbc.readRecord(sql);
		if (dbr_lett!=null){ 
//				sql = "SELECT p.* FROM rm_skso_op_coinvolti p WHERE p.n_cartella = " + n_cartella + " AND ID_SKSO = " + id_skso + " AND TIPO_OPERATORE = " + tipo_operatore + " ";
			String myselectconf="Select *  from conf where "+ "conf_kproc ='SINS' and conf_key = 'TIPDEF" + tipo_operatore + "'";
			ISASRecord tipDef = dbc.readRecord(myselectconf);
			
			String prestTipo = tipDef.get("conf_txt").toString();
			sql = "SELECT * FROM PAI p, prestaz pr WHERE p.n_cartella = " + n_cartella + " AND p.id_skso = " + id_skso + 
				  " AND p.prest_cod = pr.prest_cod AND pr.prest_tipo = "+ prestTipo;
		    ISASCursor dbgriglia = dbc.startCursor(sql);
		    Vector vdbg=dbgriglia.getAllRecord();
			if (vdbg.isEmpty()){ 
				ret = false;
			}else{
				ret = true;
			}
		}else
			ret = false;
		return ret;
	}
	
	//Duplica il PAI per il pianoAccessi
	public Boolean copiaPAIinPianoAccessi(myLogin mylogin, Hashtable h) throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException, CariException {
		String punto = nomeEJB + "isPianificatoPAI ";
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : "no dati "));
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		ISASCursor dbgriglia = null;
		boolean ret = false;
		try {
			
			//TODO VFR OOOOOOOOOO
			dbc = super.logIn(mylogin);

			dbc.startTransaction(); 
			String n_cartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//			String id_skso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//			String zona_rich=ISASUtil.getValoreStringa(h, "cod_zona_richiedente");
//			String data_richiesta = h.get("data_richiesta").toString();
			String paTipoOperatore = ISASUtil.getValoreStringa(h, CostantiSinssntW.TIPO_OPERATORE);
			String paData = ISASUtil.getValoreStringa(h, "pa_data");
			String cod_Op = ISASUtil.getValoreStringa(h, CostantiSinssntW.COD_OPERATORE);
//			N_PROGETTO
//			COD_OBBIETTIVO
//			N_INTERVENTO
			
			String myselectconf="Select *  from conf where "+
                    "conf_kproc ='SINS' and conf_key = 'TIPDEF" + paTipoOperatore + "'";
			ISASRecord tipDef = dbc.readRecord(myselectconf);
			
			String prestTipo = tipDef.get("conf_txt").toString();
			
			String sql = "SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + n_cartella + " AND p.pr_data_chiusura IS NULL " +
					     " AND ispianocongelato='S' ";
			//" AND p.pr_data_PUAC >= "+dbc.formatDbDate(data_richiesta) + 

			ISASRecord dbr_lett = dbc.readRecord(sql);
			if (dbr_lett != null) {
				String id_skso = dbr_lett.get("id_skso").toString();
				sql = "SELECT * FROM PAI p, prestaz pr WHERE p.n_cartella = " + n_cartella + " AND p.id_skso = " + id_skso + 
					  " AND p.prest_cod = pr.prest_cod AND pr.prest_tipo = "+ prestTipo;
//				"SELECT * FROM PAI p WHERE p.n_cartella = " + n_cartella + " AND p.id_skso = " + id_skso;
	            dbgriglia = dbc.startCursor(sql);
	            Vector vdbg=dbgriglia.getAllRecord();
	            PianoAssistEJB pa = new PianoAssistEJB();
	            Hashtable htPianoAcc = new Hashtable();
	            htPianoAcc.put(CostantiSinssntW.N_CARTELLA, n_cartella);
	            htPianoAcc.put("pa_data", paData);
	            htPianoAcc.put("pa_tipo_oper", paTipoOperatore);
	            htPianoAcc.put(CostantiSinssntW.N_PROGETTO, ISASUtil.getValoreStringa(h, CostantiSinssntW.N_PROGETTO));
	            htPianoAcc.put(CostantiSinssntW.COD_OBBIETTIVO, ISASUtil.getValoreStringa(h, CostantiSinssntW.COD_OBBIETTIVO));
	            htPianoAcc.put(CostantiSinssntW.N_INTERVENTO, ISASUtil.getValoreStringa(h, CostantiSinssntW.N_INTERVENTO));
	            for (Iterator iterator = vdbg.iterator(); iterator.hasNext();) {
					ISASRecord object = (ISASRecord) iterator.next();
					htPianoAcc.put("pi_data_inizio", object.get("pai_data_inizio"));
					htPianoAcc.put("pi_prest_cod", object.get("prest_cod"));
					htPianoAcc.put("pi_prest_qta", ISASUtil.getValoreStringa(object,"prest_qta"));
					htPianoAcc.put("pi_data_fine", object.get("pai_data_fine"));
					htPianoAcc.put("pi_freq", ISASUtil.getValoreStringa(object, "pai_freq"));
					htPianoAcc.put("pi_modalita", ISASUtil.getValoreStringa(object, "pai_modalita"));
					//imposto l'operatore esecurtore a quello definito nel pai/CP se presente 
					//altrimenti Ã¨ il referente del piano assistenziale
					if(((String)object.get("pai_cod_operatore")).isEmpty()){
						htPianoAcc.put("pi_op_esecutore", cod_Op);
					}else{
						htPianoAcc.put("pi_op_esecutore", (String)object.get("pai_cod_operatore"));
					}
					pa.insert_pianoAcc(mylogin, htPianoAcc);
				}
				ret = true;
			}				
			dbc.close();
			super.close(dbc);

		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ punto + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(punto, dbc);		
		}
		return new Boolean(ret);
	}
	
	//Verifico che per la cartella c'Ã¨ un PAI caricato
	public Date isPianificatoPAIDate(myLogin mylogin, Hashtable h) throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException, CariException {
		String punto = nomeEJB + "isPianificatoPAI ";
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : "no dati "));
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		Date ret = null;
		try {
			dbc = super.logIn(mylogin);

			dbc.startTransaction(); 
			String n_cartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String id_skso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
			String tipo_operatore =ISASUtil.getValoreStringa(h, CostantiSinssntW.TIPO_OPERATORE);
			String data_richiesta = h.get("data_richiesta").toString();

			String sql = "SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + n_cartella + " AND p.pr_data_chiusura IS NULL AND p.pr_data_PUAC <= "+dbc.formatDbDate(data_richiesta) + " AND ispianocongelato='S' ";

			ISASRecord dbr_lett = dbc.readRecord(sql);
			if (dbr_lett!=null){ 
//				sql = "SELECT p.* FROM rm_skso_op_coinvolti p WHERE p.n_cartella = " + n_cartella + " AND ID_SKSO = " + id_skso + " AND TIPO_OPERATORE = " + tipo_operatore + " ";
				String myselectconf="Select *  from conf where "+ "conf_kproc ='SINS' and conf_key = 'TIPDEF" + tipo_operatore + "'";
				ISASRecord tipDef = dbc.readRecord(myselectconf);
				
				String prestTipo = tipDef.get("conf_txt").toString();
				sql = "SELECT min(PAI_DATA_INIZIO) as PAI_DATA_INIZIO FROM PAI p, prestaz pr WHERE p.n_cartella = " + n_cartella + " AND p.id_skso = " + id_skso + 
					  " AND p.prest_cod = pr.prest_cod AND pr.prest_tipo = "+ prestTipo;
		        ISASCursor dbgriglia = dbc.startCursor(sql);
		        Vector<ISASRecord> vdbg=dbgriglia.getAllRecord();
				if (vdbg.isEmpty()){ 
					ret = null;
				}else{
					ret = (Date) vdbg.get(0).get("pai_data_inizio");
				}
			}else
				ret = null;
			dbc.close();
			super.close(dbc);
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ punto + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(punto, dbc);		
		}
		return ret;
	}
	
	public ISASRecord cambioPiano(myLogin mylogin, Hashtable htOldSKSO) throws SQLException{
		String punto = ver + "cambioPiano ";
		LOG.trace(punto + " inizio con dati>>" + (htOldSKSO != null ? htOldSKSO + "" : "no dati "));
		ISASConnection dbc = null;
		
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction(); // 26/02/08
			
			ISASRecord newDbr = cambioPiano(mylogin, htOldSKSO, dbc);
			dbc.commitTransaction();
			
			return newDbr;
		} catch (Exception e) {
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw newEjbException("Errore eseguendo rollback in "+ punto + ": " + e1.getMessage(), e1);
			}
			throw newEjbException("Errore eseguendo "+ punto + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(punto, dbc);
		}
	}

	public ISASRecord cambioPiano(myLogin mylogin, Hashtable htOldSKSO, ISASConnection dbc)
			throws Exception, ParseException, ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException, CariException, DBRecordChangedException, SQLException {
		String punto = ver + " cambioPiano conConnection ";
		LOG.debug(punto + "cambioPiano mL ");
		ISASRecord newDbr = cambiaPiano(dbc, htOldSKSO, mylogin);
		return newDbr;
	}

	public ISASRecord cambiaPiano(ISASConnection dbc, Hashtable htOldSKSO, myLogin mylogin)
			throws Exception, ParseException, ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException, CariException, DBRecordChangedException, SQLException {
		String punto = ver + "cambiaPiano dbc ";
		String id_skso = ISASUtil.getValoreStringa(htOldSKSO, CostantiSinssntW.CTS_ID_SKSO);
		String n_cartella = ISASUtil.getValoreStringa(htOldSKSO, CostantiSinssntW.N_CARTELLA);
		String dataChiusura = ISASUtil.getValoreStringa(htOldSKSO, CostantiSinssntW.DATA_CHIUSURA);
		
		//chiudo i PA alla data di chiusura
		PianoAssistEJB paEjb = new PianoAssistEJB();
		Hashtable dati = new Hashtable();
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
		dati.put(CostantiSinssntW.CTS_ID_SKSO, id_skso);
		dati.put(CostantiSinssntW.DATACHIUSURAPIANO, dataChiusura);
		String msg = paEjb.gestisci_chiusurePerSospensioneOProroga(mylogin, dbc, dati);
		
		//calcolo la nuova data di apertura della scheda
		Date lastClosed = UtilForBinding.getDateFromIsas(dataChiusura);
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastClosed);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Date nuovaData = cal.getTime();	
		Hashtable h = htOldSKSO;
		h.put(CostantiSinssntW.N_CARTELLA, n_cartella);
		
		//creo la nova scheda con la nuova data
		ISASRecord newDbr = execInsert(dbc, htOldSKSO, "execInsert", mylogin);
		int idSkSo = ISASUtil.getValoreIntero(newDbr, CostantiSinssntW.CTS_ID_SKSO);
//			copiaDati(newDbr, oldDbr);
		Enumeration en = h.keys();
		while (en.hasMoreElements()) {
			String chiave = en.nextElement().toString();
			newDbr.put(chiave, h.get(chiave));
		}
		newDbr.put(CostantiSinssntW.CTS_PR_DATA_PUAC, UtilForBinding.getValueForIsas(nuovaData));
		newDbr.getDBRecord().remove(CostantiSinssntW.CTS_PR_DATA_VERBALE_UVM);
		newDbr.getDBRecord().remove(CostantiSinssntW.CTS_DATA_PRESA_CARICO_SKSO);
		newDbr.getDBRecord().remove("ispianocongelato");
		newDbr.getDBRecord().remove("gridObiettivi");
		newDbr.getDBRecord().remove("gridPianoCP");
		newDbr.getDBRecord().remove("gridPianoPAI");
		newDbr.getDBRecord().remove("cod_commis_uvm");
		
		String dtInizio = UtilForBinding.getValueForIsas(nuovaData);
		newDbr.getDBRecord().put(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO, dtInizio);
		String dtFinePiano = recuperaDtFinePiano(dtInizio);
		newDbr.getDBRecord().put(CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE, dtFinePiano);
		
		htOldSKSO.put(CostantiSinssntW.CTS_ID_SKSO, id_skso);
//		ISASRecord oldDbr =selectSkValCorrente(mylogin, htOldSKSO);
		ISASRecord oldDbr =selectSkValCorrente(dbc, htOldSKSO);
		oldDbr.put(CostantiSinssntW.PR_DATA_CHIUSURA, dataChiusura);
		oldDbr.put(CostantiSinssntW.PR_MOTIVO_CHIUSURA, CostantiSinssntW.PR_MOTIVO_CHIUSURA_AMMINISTRATIVA);
		oldDbr.put("nuovaScheda", idSkSo);
		oldDbr.put("nuovaData", UtilForBinding.getValueForIsas(nuovaData));
		//aggiorno la vecchia scheda chiudendola per motivi amministrativi
		execUpdate(dbc, mylogin, oldDbr, null, null);
		//aggiorno la nuova scheda con i dati della vecchia
		execUpdate(dbc, mylogin, newDbr, null, null);
		
		//restituisco la nuova scheda
		String myselect = "SELECT * FROM rm_skso" + " WHERE n_cartella = " + n_cartella + " AND id_skso = " + idSkSo;
		LOG.trace(punto + " query " + myselect);
		newDbr = dbc.readRecord(myselect);
		newDbr.put("gridPianoCP", new Vector());
		newDbr.put("gridPianoPAI", new Vector());
		return newDbr;
	}

	private String recuperaDtFinePiano(String dtInizio) {
		String punto = ver + "recuperaDtFinePiano ";
		String dtFinePiano = "";
		try {
			Date dtFine = ManagerDate.aggiungiMesi(dtInizio, SegreteriaOrganizzativaFormCtrl.CTS_SO_MESI_DT_PERIODO_PIANO);
			dtFinePiano = UtilForBinding.getValueForIsas(dtFine);
		} catch (Exception e) {
			LOG.error(punto + " dt Fine piano>>", e);
		}
		return dtFinePiano;
	}

	public ISASRecord recuperaDataApertura(myLogin myLogin, Hashtable<String, String> prtDati) {
		String punto = ver + "recuperaDataApertura ";
		LOG.trace(punto + " inizio con dati>>" + (prtDati !=null ? prtDati+ "" : "no dati "));
		ISASConnection dbc = null;
		ISASRecord dbrRmSkso = null;
		String nCartella = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.CTS_ID_SKSO);
		String tipoOperatore = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.TIPO_OPERATORE);
		
		try {
			dbc = super.logIn(myLogin);
			Date prDataAttivazioneSo = null;
//			boolean schedaAperta = false;
			
			dbrRmSkso = selectSkSoCorrente( dbc, nCartella, idSkso);
			if (dbrRmSkso!=null && dbrRmSkso.get(CostantiSinssntW.CTS_PR_DATA_PUAC)!=null){
				prDataAttivazioneSo = (java.sql.Date) dbrRmSkso.get(CostantiSinssntW.CTS_PR_DATA_PUAC);
			}
			Date dataMaxUscita = null;
			if (ISASUtil.valida(tipoOperatore)){
				if (tipoOperatore.equals(GestTpOp.CTS_COD_INFERMIERE)){
					SkInfEJB skInfEJB = new SkInfEJB();
					ISASRecord dbrSkinf = skInfEJB.recuperaMaxSkinf(dbc, nCartella);
					if (dbrSkinf != null){
						if( dbrSkinf.get(CostantiSinssntW.SKI_DATA_USCITA)!=null){
							dataMaxUscita = (Date)dbrSkinf.get(CostantiSinssntW.SKI_DATA_USCITA);
						}
					}
				}else if(tipoOperatore.equals(GestTpOp.CTS_COD_MEDICO)){
					SkMedEJB skMedEJB = new SkMedEJB();
					ISASRecord dbrSkMed = skMedEJB.recuperaMaxSkmed(dbc, nCartella);
					if (dbrSkMed != null){
						if( dbrSkMed.get(CostantiSinssntW.SKMED_DATA_CHIUSURA)!=null){
							dataMaxUscita = (Date)dbrSkMed.get(CostantiSinssntW.SKMED_DATA_CHIUSURA);
						}
					}
				}else if (tipoOperatore.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)){
					SkFisioEJB skFisioEJB = new SkFisioEJB();
					ISASRecord dbrSkFis = skFisioEJB.recuperaMaxSkFisio(dbc, nCartella);
					if (dbrSkFis != null){
						if( dbrSkFis.get(CostantiSinssntW.SKF_DATA_CHIUSURA)!=null){
							dataMaxUscita = (Date)dbrSkFis.get(CostantiSinssntW.SKF_DATA_CHIUSURA);
						}
					}
				}else {
					SkFpgEJB skFpgEJB = new SkFpgEJB();
					ISASRecord dbrFpg = skFpgEJB.recuperaMaxSkFpg(dbc, nCartella, tipoOperatore);
					if (dbrFpg != null){
						if( dbrFpg.get(CostantiSinssntW.SKPG_DATA_CHIUSURA)!=null){
							dataMaxUscita = (Date)dbrFpg.get(CostantiSinssntW.SKPG_DATA_CHIUSURA);
						}
					}
				}
			}
			
//			dbrRmSkso.put(CostantiSinssntW.CTS_APERTURA_CONTATTO_SKSO, null);
			if (prDataAttivazioneSo!=null){
				if ( (dataMaxUscita ==null) || 
					 (dataMaxUscita !=null && dataMaxUscita.before(prDataAttivazioneSo)) ){
					dbrRmSkso.put(CostantiSinssntW.CTS_APERTURA_CONTATTO_SKSO_PROPOSTA, prDataAttivazioneSo);
				}
			}
		} catch (Exception e) {
			
		}finally {
			logout_nothrow(punto, dbc);
		}
		return dbrRmSkso;
	}

	public ISASRecord recuperaRmSksoMmg(ISASConnection dbc, String nCartella, String dataContatto)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "recuperaRmSksoMmg ";
		ISASRecord dbrRmSkSoMmg = null;
		if (ISASUtil.valida(nCartella) && ManagerDate.validaData(dataContatto)) {
			String query = "SELECT m.* FROM rm_skso_mmg m, rm_skso r WHERE m.n_cartella = " + nCartella
					+ " AND m.n_cartella = r.n_cartella AND m.id_skso = r.id_skso AND r.pr_data_puac <= "
					+ formatDate(dbc, dataContatto) + " AND data_fine >= " + formatDate(dbc, dataContatto);
			LOG.trace(punto + " query >>" + query);
			dbrRmSkSoMmg = dbc.readRecord(query);
		}
		return dbrRmSkSoMmg;
	}

	public void aggiornaValutazioneUVI(ISASConnection dbc, String nCartella, String idSkSo,
			ISASRecord dbrRmEsitiValutazioneUvi) {
		String punto = ver +"aggiornaValutazioneUVI ";
		
		LOG.debug(punto + "\n\n  DECIDERE COME FARE: SE MODIFICARE I DATI DI RMSKSO oppure leggere la tabella dello storico.\n\n");
//		ISASRecord dbrRmSkso = queryKey(dbc, nCartella, idSkSo);
//		dbrRmSkso.put(CostantiSinssntW.CTS_RMSKSO_PR_DATA_REVISIONE, ISASUtil.getValoreStringa(dbrRmEsitiValutazioneUvi, CostantiSinssntW.CTS_RMSKSO_PR_DATA_REVISIONE));
//		dbrRmSkso.put(CostantiSinssntW.CTS_RMSKSO_PR_DATA_REVISIONE, ISASUtil.getValoreStringa(dbrRmEsitiValutazioneUvi, CostantiSinssntW.CTS_RMSKSO_PR_DATA_REVISIONE));
		
	}
	
//	public ISASRecord queryKeyRmSkso(String nCartella, String idSkso,ISASConnection dbc)
//			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "queryKeyRmSkso ";
//		ISASRecord dbrRmSkso;
//		String query = "SELECT p.* FROM rm_skso p " +
//				" WHERE p.n_cartella = " + nCartella + " and p.id_skso = " + idSkso;					
//		LOG.debug(punto + " QUERY>>" + query+"<<");
//		dbrRmSkso = dbc.readRecord(query);
//		return dbrRmSkso;
//	}

	public String conclusioneIncludeAllScBisogni_(myLogin myLogin, Hashtable<String, String> dati) {
		String punto = ver + "conclusioneIncludeAllScBisogni ";
		String messaggio = "";
		ISASConnection dbc = null;
		String nCartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, Costanti.CTS_ID_SKSO);
		String prDataChiusura = ISASUtil.getValoreStringa(dati, CostantiSinssntW.PR_DATA_CHIUSURA);
		String label = ISASUtil.getValoreStringa(dati, Costanti.CTS_LABEL_MESSAGGIO);
		ISASCursor dbrCScalaBis = null;

		try {
			dbc = super.logIn(myLogin);
			String query = "select x.* from sc_bisogni x where n_cartella = " + nCartella + " and id_skso = " + idSkso
					+ " and data > " + formatDate(dbc, prDataChiusura) + " order by data desc ";
			LOG.debug(punto + " query>>" + query);
			dbrCScalaBis = dbc.startCursor(query);
			if (dbrCScalaBis.next()) {
				ISASRecord dbrScBisogni = dbrCScalaBis.getRecord();
				String data = ISASUtil.getValoreStringa(dbrScBisogni, "data");

				if (DataWI.validaData(data)) {
					String[] sostituire = new String[] { ManagerDate.formattaDataIta(prDataChiusura, "/"),
							ManagerDate.formattaDataIta(data, "/") };
					messaggio = Labels.getLabel(label, sostituire);
				} else {
					LOG.trace(punto + " Data non corretta ");
				}
			}

			
			
			
			
			
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati ", e);
		} finally {
			logout_nothrow(punto, dbrCScalaBis, dbc);
		}
		LOG.debug(punto + " messaggio>" + messaggio + "<");

		return messaggio;
	}

	public String query_controlloPresenzaAccessi(myLogin myLogin, Hashtable<String, String> dati) throws SQLException {
		String punto = ver + "query_controlloPresenzaAccessi ";
		LOG.debug(punto + " inizio con dati>>" +dati );
		String messaggio = "";
		ISASConnection dbc=null;
		ISASCursor dbcCursor = null;
		try {
			dbc=super.logIn(myLogin);
			
			String nCartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
			String dataConclusione = ISASUtil.getValoreStringa(dati, CostantiSinssntW.PR_DATA_CHIUSURA);

			String query = "SELECT INT_DATA_PREST, INT_TIPO_OPER FROM INTERV I WHERE I.INT_CARTELLA = " +nCartella +
					" AND I.INT_CONTATTO IS NOT NULL AND INT_DATA_PREST > " + formatDate(dbc, dataConclusione)+ 
					"  GROUP BY INT_DATA_PREST, INT_TIPO_OPER ORDER BY INT_DATA_PREST DESC ";
			LOG.trace(punto + " query interventi>" + query);

			dbcCursor = dbc.startCursor(query);
			String data, tipoOperatore = "", descTipoOperatore;
			
			String linee = "";
			int i = 0;
			while (dbcCursor.next() && i< CTS_NUMERO_ACCESSI_MOSTRARE ) {
				ISASRecord dbrInterv = (ISASRecord) dbcCursor.getRecord();
				data = ISASUtil.getValoreStringa(dbrInterv, "int_data_prest");
				tipoOperatore = ISASUtil.getValoreStringa(dbrInterv, "int_tipo_oper");
				if (ISASUtil.valida(tipoOperatore)) {
					descTipoOperatore = ManagerOperatore.decodificaTipoOperatore(dbc,tipoOperatore,null);
					if (ISASUtil.valida(descTipoOperatore)) {
						linee += (ISASUtil.valida(linee) ? "\n" : "");
						String[] sost = new String[] {ManagerDate.formattaDataIta(data, "/"), descTipoOperatore};
						linee +=Labels.getLabel(CostantiSinssntW.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO_DETTAGLIO, sost); 
					}   
				}
				i++;
			}
			messaggio = linee;
//			if (ISASUtil.valida(linee)){
//				String[] sost = new String[]{linee};
//				messaggio = Labels.getLabel(Costanti.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO, sost);
//			}
			LOG.debug(punto + "Dati che invio>>" +messaggio+"< ");
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controlloData()  ");
		}finally{
			logout_nothrow(punto, dbcCursor, dbc);
		}
		return messaggio;
	}

	public String statoSchedaSo(myLogin myLogin, Hashtable<String, String> dati) throws SQLException {
		String punto = ver + "statoSchedaSo ";
		LOG.debug(punto + " inizio con dati>>" + dati+"<");
		String messaggio = "";
		ISASConnection dbc=null;
		ISASCursor dbcCursor = null;
		try {
			dbc=super.logIn(myLogin);
			
			String nCartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
			String dataConclusione = ISASUtil.getValoreStringa(dati, CTS_DATA_ACCESSO);
			String query = "SELECT * FROM RM_SKSO WHERE N_CARTELLA IN (" +nCartella +") ";
			LOG.trace(punto + " query verifica presenza schesa so>" + query);
			ISASRecord dbrRmSkso = dbc.readRecord(query);
			if (dbrRmSkso != null) {
				LOG.trace(punto + "Schesa so presente, verifico se attiva");
				query = "SELECT n_cartella, data_inizio, data_fine, 'SCHEDA ATTIVA' AS esito FROM rm_skso_mmg "
						+ " WHERE n_cartella = " + nCartella + " AND data_inizio <=" + formatDate(dbc, dataConclusione)
						+ " AND data_fine >= " + formatDate(dbc, dataConclusione)
						+ " UNION SELECT n_cartella, dt_proroga_inizio AS data_inizio, "
						+ " dt_proroga_fine AS data_fine, 'SCHEDA PROROGATA' AS esito "
						+ " FROM rm_skso_proroghe WHERE n_cartella = " + nCartella + " AND dt_proroga_inizio <= "
						+ formatDate(dbc, dataConclusione)
						+ " AND ( dt_proroga_fine IS NOT NULL AND dt_proroga_fine >= "
						+ formatDate(dbc, dataConclusione) + " ) ";
				LOG.trace(punto + " query verifica presenza schesa so>" + query);
				ISASCursor dbcursor = null;
				int numero=0;
				try {
					dbcursor = dbc.startCursor(query);
					numero = dbcursor.getDimension();
					if (numero<=0){
						messaggio = Labels.getLabel(CTS_NO_SCHEDA_SO_PRESENTE_SCHEDA_SCADUTA);
					}
					LOG.debug(punto + " numero>>"+ numero+ " messaggio >>" +messaggio+"<");
				} finally {
					close_dbcur_nothrow(punto, dbcursor);
				}
			} else {
				LOG.trace(punto + " Non esiste la scheda so");
				messaggio = Labels.getLabel(CTS_NO_SCHEDA_SO_PRESENTE);
			}

			LOG.debug(punto + "Dati che invio>>" +messaggio+"< ");
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controlloData()  ");
		}finally{
			logout_nothrow(punto, dbcCursor, dbc);
		}
		return messaggio;
		
	}
	
	public String getVerificaPresenzaScale(myLogin mylogin, Hashtable<String, Object> dati) {
		String punto = ver + "getVerificaPresenzaScale ";
		String messaggio = "";
		ISASConnection dbc = null;
		String prDataChiusura = "";
		LOG.info(punto + " inizio con dati>>" + dati + "<");
		
		try {
			dbc = super.logIn(mylogin);
			prDataChiusura = ISASUtil.getValoreStringa(dati, CostantiSinssntW.PR_DATA_CHIUSURA);
			if (ManagerDate.validaData(prDataChiusura)) {
				String dettaglioScala ="";

				ISASRecord infoScala = gest_scaleVal.getScaleMaxPerT(dbc, dati);
				LOG.trace(punto + " dati scale >>" + infoScala.getHashtable() + "<");
				
				LOG.debug(punto + "Ci sono delle scale di valutazione compilate ");
				Vector<ISASRecord> scaleValutazioni = gest_scaleVal.getTabSchede(dbc, dati);
				LOG.debug(punto + " Ho recuperato le scale>>" + scaleValutazioni.size() + "<");
				ISASRecord dbrScala;
				String campoData, valDataCampo;
				String valNomeScala;
				String maxDataConclusione = "";
				
				for (int i = 0; i < scaleValutazioni.size(); i++) {
					dbrScala = scaleValutazioni.get(i);
					LOG.trace(punto + i + ") dati che esamino>>" + dbrScala.getHashtable() + "<");
					campoData = ISASUtil.getValoreStringa(dbrScala, "campo_data");
					valDataCampo = ISASUtil.getValoreStringa(infoScala, campoData);
					if (ManagerDate.confrontaDate(prDataChiusura, valDataCampo, false)) {
						valNomeScala = ISASUtil.getValoreStringa(dbrScala, "descrizione");
						dettaglioScala += (ISASUtil.valida(dettaglioScala) ? "\n" : "");
						dettaglioScala +=Labels.getLabel("so.messaggio.scale.compilata.scheda")+": "+ 
								valNomeScala +" "+ Labels.getLabel("so.messaggio.scale.compilata.data")+": "+ 
								ManagerDate.formattaDataIta(valDataCampo, "/");
						if (!ManagerDate.validaData(maxDataConclusione) || ManagerDate.confrontaDate(maxDataConclusione, valDataCampo, false)){
							maxDataConclusione = valDataCampo;
							LOG.trace(punto + i + ")Massima data>>"+ maxDataConclusione);
						}
						LOG.trace(punto + i + ") Data successiva alla scheda di conclusione ");
					} else {
						LOG.trace(punto + i + ") va bene ");
					}
				}
				if (ISASUtil.valida(dettaglioScala)) {
					String[] sostituire = new String[] { ManagerDate.formattaDataIta(prDataChiusura, "/"), dettaglioScala,
							ManagerDate.formattaDataIta(maxDataConclusione, "/") };
					messaggio = Labels.getLabel("so.messaggio.scale.conclusione", sostituire);
				}
			}
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati delle scale di valutazione ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		LOG.info(punto + " messaggio>>" + messaggio);
		return messaggio;
	}
	
}// END class

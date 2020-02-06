package it.caribel.app.sinssnt.bean.nuovi;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 30/09/2014 - //     scrittura nella tabella rm_skso_op_coinvolti
//
// ==========================================================================
 
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class RMSkSOOpCoinvoltiEJB extends RMSkSOOpCoinvoltiBaseEJB {
	public static String COD_OPERATORE = Costanti.COD_OPERATORE;
	public static String COD_PRESIDIO = Costanti.CTS_OPERATORE_COD_PRESIDO;

	public RMSkSOOpCoinvoltiEJB() {
	}
	private String ver = "20-";
    
	public Boolean dataFineCoerente(myLogin mylogin, Hashtable dati) throws SQLException {
		ISASConnection dbc = null;
		String punto = ver + "dataFineCoerente ";
		boolean dataCoerente=false;
		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
			String dataFine = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_OP_DATA_FINE_PIANO);
			String query = "select count(*) as numero from rm_skso_proroghe where n_cartella = " +
					nCartella +" and id_skso = " +idSkSo + " and dt_proroga_inizio <= " +
					formatDate(dbc, dataFine)+ " and " + formatDate(dbc, dataFine)+
							" <= dt_proroga_fine ";
			LOG.trace(punto + " query>" + query);
			ISASRecord dbrRmSksoProroghe = dbc.readRecord(query);  
			int numeroRecord = ISASUtil.getValoreIntero(dbrRmSksoProroghe, "numero");
			dataCoerente = (numeroRecord >=1);
			LOG.trace(punto + " numeroRecord>>"+numeroRecord +"< dataCoerente>"+dataCoerente+"<");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_terapia()  ");
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dataCoerente;
	}
	
//	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h) throws SQLException {
//		ISASConnection dbc = null;
//		String punto = ver + "query ";
//		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
//		try {
//			dbc = super.logIn(mylogin);
//			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//			String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//			
//			vdbr = recuperaDatiGriglia(dbc, nCartella, idSkSo);  
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			throw new SQLException("Errore eseguendo una query_terapia()  ");
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return vdbr;
//	}

//	private Vector<ISASRecord> recuperaDatiGriglia(ISASConnection dbc, String nCartella,
//			String idSkSo) throws ISASMisuseException, DBMisuseException,
//			DBSQLException, Exception {
//		String punto = ver + "recuperaDatiGriglia ";
//		ISASCursor dbcur = null;
//		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
//
//		try {
//			String myselect = recuperaQueryKey(nCartella, idSkSo, "", false);
//			LOG.debug(punto + " query>>" + myselect);
//			dbcur = dbc.startCursor(myselect);
//			vdbr = dbcur.getAllRecord();
//			String tipoOperatore, codDistretto, codOperatore, descr;
//			for (Enumeration e = vdbr.elements(); e.hasMoreElements();) {
//				ISASRecord dbr_1 = (ISASRecord) e.nextElement();
//				if (dbr_1 != null) {
//					tipoOperatore = ISASUtil.getValoreStringa(dbr_1,"tipo_operatore");
//					dbr_1.put("tipo_operatore_descr", ManagerOperatore.decodificaTipoOperatore(dbc, tipoOperatore,
//									CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE));
//					codDistretto = ISASUtil.getValoreStringa(dbr_1,"cod_distretto");
//					dbr_1.put("cod_distretto_descr", decodificaDistretto(dbc, codDistretto));
//					decodificaOperatore(dbc, dbr_1, true);
//					decodificaExtra(dbr_1, "extra");
//					decodificaExtra(dbr_1, "no_alert");
//				}
//			}
//		} finally {
//			close_dbcur_nothrow(punto, dbcur);
//		}
//		return vdbr;
//	}
	
//	private void decodificaExtra(ISASRecord dbr, String campo) {
//		String vCampo = ISASUtil.getValoreStringa(dbr, campo);
//		String descrCampo = "";
//			try {
//				descrCampo = CostantiSinssntW.getDecodificaSN(vCampo);
//				dbr.put(campo+"_descr", descrCampo );
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//	}

//	private void decodificaOperatore(ISASConnection dbc, ISASRecord dbrSkSoOpCoinvolti, boolean aggiungiOperatore) {
//		String punto = ver + "decodificaOperatore ";
//		String codOperatore = ISASUtil.getValoreStringa(dbrSkSoOpCoinvolti, "cod_operatore");
//		String tipoOperatore = ISASUtil.getValoreStringa(dbrSkSoOpCoinvolti, "tipo_operatore");
//		try {
//			if (tipoOperatore.equals(GestTpOp.CTS_COD_MMG)){
//				settaInfoMedico(dbc, dbrSkSoOpCoinvolti, codOperatore,"cod_operatore_descr");
//			}else {
//				settaInfoOperatori(dbc, dbrSkSoOpCoinvolti,codOperatore, "cod_operatore_descr", aggiungiOperatore);
//				codOperatore = ISASUtil.getValoreStringa(dbrSkSoOpCoinvolti, CostantiSinssntW.COD_OPERATORE_PC);
//				if (ISASUtil.valida(codOperatore)){
//					settaInfoOperatori(dbc, dbrSkSoOpCoinvolti, codOperatore,"cod_operatore_pc_descr", false);
//				}
//			}
//		} catch (Exception e) {
//			LOG.error(punto + " Errore nel decodifcare gli operatori >", e);
//		}
//	}

//	private void settaInfoMedico(ISASConnection dbc,
//			ISASRecord dbrSkSoOpCoinvolti, String codOperatore, String campoDescrMedico) throws ISASMisuseException, ISASPermissionDeniedException,
//			DBMisuseException, DBSQLException {
//		String query = "select * from medici where mecodi = '" +codOperatore + "' " ;
//		ISASRecord dbrOperatori = dbc.readRecord(query);
//		String cognomeNomeOperatore="";
//		if (dbrOperatori!=null){
//			cognomeNomeOperatore = ISASUtil.getValoreStringa(dbrOperatori, "mecogn");
//			cognomeNomeOperatore +=(ISASUtil.valida(cognomeNomeOperatore)?" ":"")+ISASUtil.getValoreStringa(dbrOperatori, "menome");
//		}
//		dbrSkSoOpCoinvolti.put(campoDescrMedico, cognomeNomeOperatore);
//		
//	}

//	private void settaInfoOperatori(ISASConnection dbc,
//			ISASRecord dbrSkSoOpCoinvolti, String codOperatore, String campoDescrOperatore, boolean aggiungiOperatore) throws ISASMisuseException, ISASPermissionDeniedException,
//			DBMisuseException, DBSQLException {
//		String cognomeNomeOperatore = "";
//		if (ISASUtil.valida(codOperatore)) {
//			String query = recuperaOperatore(codOperatore);
//			ISASRecord dbrOperatori = dbc.readRecord(query);
//			if (dbrOperatori != null) {
//				cognomeNomeOperatore = ISASUtil.getValoreStringa(dbrOperatori, "cognome");
//				cognomeNomeOperatore += (ISASUtil.valida(cognomeNomeOperatore) ? " " : "")
//						+ ISASUtil.getValoreStringa(dbrOperatori, "nome");
//			}
//		}else {
//			if (aggiungiOperatore){
//				cognomeNomeOperatore = ISASUtil.getValoreStringa(dbrSkSoOpCoinvolti, "op_cognome");
//				cognomeNomeOperatore += (ISASUtil.valida(cognomeNomeOperatore) ? " " : "")
//						+ ISASUtil.getValoreStringa(dbrSkSoOpCoinvolti, "op_nome");
//			}
//		}
//		dbrSkSoOpCoinvolti.put(campoDescrOperatore, cognomeNomeOperatore);
//	}

//	private String recuperaOperatore(String codOperatore) {
//		String punto = ver + "recuperaOperatore ";
////		String codOperatore = ISASUtil.getValoreStringa(dbrSkSoOpCoinvolti, "cod_operatore");
//		String query = "select * from operatori where codice = '" +codOperatore+"' ";
//		LOG.trace(punto + " query>>"+ query+"<<");
//		return query;
//	}
//
//	private Object decodificaDistretto(ISASConnection dbc, String cod_distretto) {
//		String punto = "decodificaDistretto ";
//		String distretto = "";
//		String query = "select des_distr from distretti where cod_distr = '" +cod_distretto+"' ";
//		LOG.trace(punto + " query>>"+ query+"<<");
//		ISASRecord dbrDistretti;
//		try {
//			dbrDistretti = dbc.readRecord(query);
//			distretto = ISASUtil.getValoreStringa(dbrDistretti, "des_distr");
//		} catch (Exception e) {
//			LOG.error(punto + " Errore nel decodifcare il distretto con codice >" +query, e);
//		}
//		
//		return distretto;
//	}

	/*
	private String decodificaTabVoci_(ISASConnection mydbc, String val, String codice) throws Exception{
		String strDescrizione = "";
		if ((val != null) && (!val.trim().equals(""))) {
			String selS = "SELECT tab_descrizione" +
			" FROM tab_voci" +
			" WHERE tab_cod = '" + codice + "'" +
			" AND tab_val = '" + val + "'";
			ISASRecord rec = mydbc.readRecord(selS);
			if (rec != null)
				strDescrizione = (String)rec.get("tab_descrizione");
			if (strDescrizione == null)
				strDescrizione = "";
			strDescrizione = ManagerOperatore.decodificaDescrizioneTipo(strDescrizione);
		}
		return strDescrizione;
	}*/

//	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException {
//		String punto = ver + "queryKey ";
//		ISASConnection dbc = null;
//		ISASRecord dbrRmSkSoOpCoinvolti = null;
//		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//		String tipoOperatore = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SO_TIPO_OPERATORE);
//
//		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
//		try {
//			if (ISASUtil.valida(tipoOperatore)) {
//				dbc = super.logIn(mylogin);
//				String myselect = recuperaQueryKey(nCartella, idSkSo, tipoOperatore, true);
//				LOG.trace(punto + " query>>" + myselect);
//				dbrRmSkSoOpCoinvolti = dbc.readRecord(myselect);
//				if (dbrRmSkSoOpCoinvolti!=null){
//					decodificaOperatore(dbc, dbrRmSkSoOpCoinvolti, false);
//				}
//			}
//
//		} catch (Exception e) {
//			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return dbrRmSkSoOpCoinvolti;
//	}



	public ISASRecord insert(myLogin mylogin, Hashtable dati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "insert ";
		String nCartella = null;
		String idSkSo = null;
		String tipoOperatore = "";
		ISASConnection dbc = null;
		ISASRecord dbrInsert = null;
		try {
			nCartella = ISASUtil.getValoreStringa(dati, "n_cartella");
			idSkSo = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
			tipoOperatore = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_SO_TIPO_OPERATORE);
		} catch (Exception e) {
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			String codOperatore = ISASUtil.getValoreStringa(dati, CostantiSinssntW.COD_OPERATORE);
			if (ISASUtil.valida(codOperatore)){
				dati.put(CostantiSinssntW.CTS_OPERATORE_COD_PRESIDO, ManagerOperatore.recuperaCodPresidioOperatore(dbc, codOperatore));
			}
			
			if (ISASUtil.valida(tipoOperatore) && tipoOperatore.equals(GestTpOp.CTS_COD_MMG)){
				LOG.trace(punto + " inserisco operatore MMG/PLS ");
				inserisciMMGPLS(dbc, nCartella, idSkSo, dati);
			}else {
				LOG.trace(punto + " inserisco operatore ALTRI OPERATORI ");
				dbrInsert = dbc.newRecord("rm_skso_op_coinvolti");
				Enumeration n = dati.keys();
				while (n.hasMoreElements()) {
					String e = (String) n.nextElement();
					dbrInsert.put(e, dati.get(e));
				}
				dbc.writeRecord(dbrInsert);
			}
			
			
			
			String query = recuperaQueryKey(nCartella, idSkSo, tipoOperatore, true);
			dbrInsert = dbc.readRecord(query);
			
		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrInsert;
	}

	public ISASRecord recuperaIdSksoDaAggiornare(myLogin mylogin, Hashtable<String, String> h)
			throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String punto = ver + "recuperaIdSksoDaAggiornare";
		ISASConnection dbc = null;
		ISASRecord dbrSkso = null;
		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);

			String query = "SELECT * FROM rm_skso WHERE n_cartella=" + nCartella + " and PR_DATA_CHIUSURA is null"
					+ " and pr_motivo_chiusura is null ";
//					"and vista_da_so is null and PV_DT_VISITA is null";
			LOG.trace(punto + " query>>" + query);
			dbrSkso = dbc.readRecord(query);

		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}

		return dbrSkso;
	}
	
	
	
	public Boolean updatePv(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String punto = ver + "updatePv";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);

			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
			String vistaDaSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO);
			
			String myselect = recuperaQueryKeyPV(nCartella, idSkSo);
			LOG.trace(punto + " query>>" + myselect);
			ISASRecord dbrSkso = dbc.readRecord(myselect);
			
			if (dbrSkso!=null){
			dbrSkso.put(CostantiSinssntW.CTS_PV_TIPO_OPERATORE, ISASUtil.getValoreStringa(h, CostantiSinssntW.TIPO_OPERATORE));
			dbrSkso.put(CostantiSinssntW.CTS_PV_COD_OPERATORE, ISASUtil.getValoreStringa(h, CostantiSinssntW.COD_OPERATORE));
			dbrSkso.put(CostantiSinssntW.CTS_PV_DT_VISITA, ISASUtil.getValoreStringa(h, CostantiSinssntW.DT_PRIMA_VISITA));
			dbrSkso.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, vistaDaSo);
			
			dbc.writeRecord(dbrSkso);
			}
			
			if (h.containsKey(CostantiSinssntW.DA_PRIMA_VISITA)){
				aggiornaPresaCarico(dbc,h);
			}

			return new Boolean(true);

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
	
	
	public void aggiornaPresaCarico(ISASConnection dbc, Hashtable h) throws Exception {
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String idSkSo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
		String tipo_operatore = ISASUtil.getValoreStringa(h, CostantiSinssntW.TIPO_OPERATORE);
		String cod_operatore =  ISASUtil.getValoreStringa(h, CostantiSinssntW.COD_OPERATORE);
		cod_operatore = ISASUtil.valida(cod_operatore) ? cod_operatore.trim() : "";
		String zona = ISASUtil.getDecode(dbc, "operatori", "codice", cod_operatore, "cod_zona");
		String dt_pc = ISASUtil.getValoreStringa(h,CostantiSinssntW.DT_PRESA_CARICO);
		
		String sql = "select * from rm_skso_op_coinvolti where n_cartella = "+nCartella+
					" and id_skso = "+idSkSo+
					" and tipo_operatore = "+tipo_operatore;
					if (ISASUtil.valida(zona)){
						sql += " and cod_zona = "+zona;
					}
					sql +=" and dt_presa_carico is null" +
					" and (cod_operatore is null or cod_operatore = '"+cod_operatore+"')"+
					" and exists ( select * from rm_skso where n_cartella = "+nCartella+
					" and id_skso = "+idSkSo+ " and data_presa_carico_skso is not null)";
		
		ISASRecord pc = dbc.readRecord(sql);
		if (pc!=null){
			pc.put(CostantiSinssntW.COD_OPERATORE, cod_operatore);
			pc.put(CostantiSinssntW.COD_OPERATORE_PC, cod_operatore);
			pc.put(CostantiSinssntW.DT_PRESA_CARICO, dt_pc);
			dbc.writeRecord(pc);			
		}		
	}



	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String punto = ver + "update";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);

			String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
			String tipoOperatore = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SO_TIPO_OPERATORE);
			String codOperatore = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.COD_OPERATORE);
			
			if (ISASUtil.valida(codOperatore)){
				dbr.put(CostantiSinssntW.CTS_OPERATORE_COD_PRESIDO, ManagerOperatore.recuperaCodPresidioOperatore(dbc, codOperatore));
			}
			
			dbc.writeRecord(dbr);

			String myselect = recuperaQueryKey(nCartella, idSkSo, tipoOperatore, true);
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
	
	public void deleteAll(ISASConnection dbc, Hashtable<String, String> dati) throws DBRecordChangedException,
			ISASPermissionDeniedException, Exception {
		String punto = ver + "deleteAll";
		LOG.info(punto + " rimuovo tutti " + dati);
		try {
			String nCartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(dati, Costanti.CTS_ID_SKSO);
			String tipoOperatore;
			ISASRecord dbrRmSkSoOpCoinvolti;
			Vector<ISASRecord> dbrSoOpCoinvolti = recuperaDatiGriglia(dbc, nCartella, idSkSo);
			for (Iterator iterator = dbrSoOpCoinvolti.iterator(); iterator.hasNext();) {
				ISASRecord isasRecord = (ISASRecord) iterator.next();
				tipoOperatore = ISASUtil.getValoreStringa(isasRecord, Costanti.CTS_SO_TIPO_OPERATORE);

				String myselect = recuperaQueryKey(nCartella, idSkSo, tipoOperatore, true);
				LOG.trace(punto + " query>>" + myselect);
				dbrRmSkSoOpCoinvolti = dbc.readRecord(myselect);
				dbc.deleteRecord(dbrRmSkSoOpCoinvolti);
			}
		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		}
	}
	
	
	public Vector<ISASRecord> inserisciMMGPLSOperatorePrimaVisita(myLogin myLogin, Hashtable<String, String> dati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "inserisciMMGPLSOperatorePrimaVisita ";
		LOG.trace(punto + " inizio con dati>>"+ dati);
		ISASConnection dbc = null;
		Vector<ISASRecord> v = new Vector<ISASRecord>();
		String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_ID_SKSO);
		
		try {
			dbc = super.logIn(myLogin);
			inserisciOperatoriFigure(dati, dbc, nCartella, idSkso);
			v = recuperaDatiGriglia(dbc, nCartella, idSkso);
			LOG.trace(punto + " dati che ho recuperato>>" + (v!=null ? v.size()+"": " no dati recuperati"));
		} catch (DBRecordChangedException e) {
			throw e;
		} catch (ISASPermissionDeniedException e) {
			throw e;
		} catch (Exception e) {
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		
		return v;
	}

	public void inserisciOperatoriFigure(Hashtable<String, String> dati,
			ISASConnection dbc, String nCartella, String idSkso)
			throws Exception {
		String inserireMMG = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_OP_INSERIRE_MMG);
		String inserirePV = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_OP_INSERIRE_PV);
		String inserireGenerico = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_OP_INSERIRE_GENERICO);
		String codOperatore = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_COD_OP_CORRENTE);
		ISASRecord dbrSkso = recuperaRmSksoMMG(dbc, nCartella, idSkso);
		String tipoOperatoreCorrente = recuperaTipoOperatoreCorrente(dbc, codOperatore);
		
		String query = "";
		ISASRecord dbrSksoOpCoinvolti =null;
		if (ISASUtil.valida(inserireMMG) && inserireMMG.equals(CostantiSinssntW.CTS_SI)){
			inserisciMMGPLS(dbc, nCartella, idSkso, dbrSkso.getHashtable());
		}
		
		if (ISASUtil.valida(inserirePV)&& inserirePV.equals(CostantiSinssntW.CTS_SI)){
			String tipoOperatorePrimaVisita = ISASUtil.getValoreStringa(dbrSkso, CostantiSinssntW.CTS_PV_TIPO_OPERATORE);
			if (ISASUtil.valida(tipoOperatorePrimaVisita)&& 
				ISASUtil.valida(tipoOperatoreCorrente) && tipoOperatorePrimaVisita.equals(tipoOperatoreCorrente)){
				query = recuperaQueryKey(nCartella, idSkso,tipoOperatorePrimaVisita, true);
				dbrSksoOpCoinvolti = dbc.readRecord(query);
				if (dbrSksoOpCoinvolti == null){
					inserisciOperatore(dbc, nCartella, idSkso, tipoOperatorePrimaVisita, dbrSkso.getHashtable(), true,codOperatore);
				}
			}
		}
		
		if (ISASUtil.valida(inserireGenerico) && inserireGenerico.equals(CostantiSinssntW.CTS_SI)){
			String tipoOperatoreDaInserire = dati.get(CostantiSinssntW.TIPO_OPERATORE);
			if (ISASUtil.valida(tipoOperatoreDaInserire)){
				query = recuperaQueryKey(nCartella, idSkso,tipoOperatoreDaInserire, true);
				dbrSksoOpCoinvolti = dbc.readRecord(query);
				dbrSkso.put("data_inizio", dati.get("data_inizio"));
				dbrSkso.put("data_fine", dati.get("data_fine"));
				//se non era definito un operatore lo inserisco altrimenti sovrascrivo 
				// quello giÃ  presente con i dati nuovi
				if (dbrSksoOpCoinvolti == null){
					inserisciOperatore(dbc, nCartella, idSkso, tipoOperatoreDaInserire, dbrSkso.getHashtable(), false, codOperatore);
				}else if(dati.containsKey("SOVRASCRIVI")){
					dbrSksoOpCoinvolti.getDBRecord().getHashtable().putAll(dati);
					dbrSksoOpCoinvolti.put("cod_operatore", codOperatore);
					updateOperatore(dbc, nCartella, idSkso, tipoOperatoreDaInserire, dbrSkso.getHashtable(), dbrSksoOpCoinvolti);
				}
			}
		}
	}



	public void inserisciMMGPLS(ISASConnection dbc, String nCartella, String idSkso, Hashtable dbrSkso)
			throws Exception {
		String query;
		ISASRecord dbrSksoOpCoinvolti;
		query = recuperaQueryKey(nCartella, idSkso,GestTpOp.CTS_COD_MMG ,  true);
		dbrSksoOpCoinvolti = dbc.readRecord(query);
		if (dbrSksoOpCoinvolti == null){
			inserisciOperatore(dbc, nCartella, idSkso, GestTpOp.CTS_COD_MMG, dbrSkso, false, null);
		}else {
			updateOperatore(dbc, nCartella, idSkso, GestTpOp.CTS_COD_MMG, dbrSkso, dbrSksoOpCoinvolti);
		}
		query = recuperaQueryKey(nCartella, idSkso,GestTpOp.CTS_COD_MEDICO ,  true);
		dbrSksoOpCoinvolti = dbc.readRecord(query);
		if (dbrSksoOpCoinvolti == null){
			inserisciOperatore(dbc, nCartella, idSkso, GestTpOp.CTS_COD_MEDICO, dbrSkso, false, null);
		}else {
			updateOperatore(dbc, nCartella, idSkso, GestTpOp.CTS_COD_MEDICO, dbrSkso, dbrSksoOpCoinvolti);
		}
	}
	
	public void rimuoviMMGPLS(ISASConnection dbc, String nCartella, String idSkso, Hashtable dbrSkso)
			throws Exception {
		String punto = ver + "rimuoviMMGPLS ";
		String query;
		ISASRecord dbrSksoOpCoinvolti;
		query = recuperaQueryKey(nCartella, idSkso,GestTpOp.CTS_COD_MMG ,  true);
		dbrSksoOpCoinvolti = dbc.readRecord(query);
		if (dbrSksoOpCoinvolti == null){
			LOG.debug(punto + " Record non presente: ");
		}else {
			LOG.trace(punto + " Rimouvo il record "+(dbrSksoOpCoinvolti!=null ? dbrSksoOpCoinvolti.getHashtable()+"":" no dati ")+"");
			dbc.deleteRecord(dbrSksoOpCoinvolti);
		}
		
		query = recuperaQueryKey(nCartella, idSkso,GestTpOp.CTS_COD_MEDICO ,  true);
		dbrSksoOpCoinvolti = dbc.readRecord(query);
		if (dbrSksoOpCoinvolti == null){
			LOG.debug(punto + " Record non presente ");
		}else {
			LOG.trace(punto + " Rimouvo il record "+(dbrSksoOpCoinvolti!=null ? dbrSksoOpCoinvolti.getHashtable()+"":" no dati ")+"");
			dbc.deleteRecord(dbrSksoOpCoinvolti);
		}
	}

	private void updateOperatore(ISASConnection dbc, String nCartella, String idSkso, String tipoOperatore,
			Hashtable dbrSkso, ISASRecord dbrRmSkSoOp) throws ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException, DBRecordChangedException {

		String punto = ver + "inserisciOperatore ";
		if (ISASUtil.valida(tipoOperatore)) {
			dbrRmSkSoOp.put("dt_inizio_piano", ISASUtil.getValoreStringa(dbrSkso, "data_inizio"));
			dbrRmSkSoOp.put("dt_fine_piano", ISASUtil.getValoreStringa(dbrSkso, "data_fine"));
			if (tipoOperatore.equals(GestTpOp.CTS_COD_MMG)) {
//				dbrRmSkSoOp.put("num_acces_set", ISASUtil.getValoreStringa(dbrSkso, "accessi_mmg"));
//				dbrRmSkSoOp.put("dt_presa_carico", ISASUtil.getValoreStringa(dbrSkso, "pr_mmg_data_richiesta"));
				inserisciDtPresaCarico(dbrSkso, dbrRmSkSoOp);
				dbrRmSkSoOp.put("cod_operatore", ISASUtil.getValoreStringa(dbrSkso, "cod_med"));
			}
//			TODO BOFFA da verificare: valore del campo letto dalla querykey(valore presente nel record) Ã¨  superiore alla grandezza del campo che si va a scrivere
			dbrRmSkSoOp.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, ISASUtil.getValoreStringa(dbrRmSkSoOp, CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO));

			LOG.trace(punto + " dati che aggiorno>>"
					+ (dbrRmSkSoOp != null ? dbrRmSkSoOp.getHashtable() + "" : " no dati "));
			dbc.writeRecord(dbrRmSkSoOp);
		}
	}



	private String recuperaTipoOperatoreCorrente(ISASConnection dbc, String codOperatore) {
		return  ManagerOperatore.caricaTipoOperatore(dbc, codOperatore);
	}

	private void inserisciOperatore(ISASConnection dbc, String nCartella,
			String idSkso, String tipoOperatore, Hashtable dbrSkso, boolean primaVisita, 
			String codOperatore) throws Exception{
		String punto = ver + "inserisciOperatore ";
		try {
		
		if (ISASUtil.valida(tipoOperatore)){
			ISASRecord dbrInsert = dbc.newRecord("rm_skso_op_coinvolti");
			dbrInsert.put("n_cartella", nCartella);
			dbrInsert.put("id_skso",idSkso);
			dbrInsert.put("tipo_operatore",tipoOperatore);
			dbrInsert.put("cod_zona", ISASUtil.getValoreStringa(dbrSkso, "cod_zona"));
//			dbrInsert.put("cod_presidio",ISASUtil.getValoreStringa(dbrSkso, CostantiSinssntW.CTS_OPERATORE_COD_PRESIDO));
			if (ISASUtil.valida(codOperatore)){
				dbrInsert.put(CostantiSinssntW.CTS_OPERATORE_COD_PRESIDO, ManagerOperatore.recuperaCodPresidioOperatore(dbc, codOperatore));
			}else {
				dbrInsert.put(CostantiSinssntW.CTS_OPERATORE_COD_PRESIDO, ISASUtil.getValoreStringa(dbrSkso, CostantiSinssntW.CTS_OPERATORE_COD_PRESIDO));
			}
			dbrInsert.put("cod_distretto",ISASUtil.getValoreStringa(dbrSkso, "cod_distretto"));
			dbrInsert.put("dt_inizio_piano",ISASUtil.getValoreStringa(dbrSkso, "data_inizio"));
			dbrInsert.put("dt_fine_piano",ISASUtil.getValoreStringa(dbrSkso, "data_fine"));
			dbrInsert.put("no_alert","S");
			if (tipoOperatore.equals(GestTpOp.CTS_COD_MMG)){
//				dbrInsert.put("num_acces_set",ISASUtil.getValoreStringa(dbrSkso, "accessi_mmg"));
				inserisciDtPresaCarico(dbrSkso, dbrInsert);
				dbrInsert.put("cod_operatore",ISASUtil.getValoreStringa(dbrSkso, "cod_med"));
			}else if (primaVisita) {
				dbrInsert.put("dt_presa_carico",ISASUtil.getValoreStringa(dbrSkso, "pv_dt_visita"));
				dbrInsert.put("cod_operatore",ISASUtil.getValoreStringa(dbrSkso, "pv_cod_operatore"));
			}else {
				if (ISASUtil.valida(codOperatore)){
					dbrInsert.put("cod_operatore",codOperatore);
				}
			}
			LOG.trace(punto + " dati che aggiorno>>" + (dbrInsert!=null ? dbrInsert.getHashtable()+"": " no dati "));
			dbc.writeRecord(dbrInsert);
		}
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	private void inserisciDtPresaCarico(Hashtable dbrSkso, ISASRecord dbrInsert) throws ISASMisuseException {
		String dtPresaCarico = ISASUtil.getValoreStringa(dbrSkso, "pr_mmg_data_richiesta");
		if (!ManagerDate.validaData(dtPresaCarico)){
			dtPresaCarico = ISASUtil.getValoreStringa(dbrSkso, "data_inizio");
		}
		if (ManagerDate.validaData(dtPresaCarico)){
			dbrInsert.put("dt_presa_carico",dtPresaCarico);
		}
	}

	public ISASRecord recuperaRmSksoMMG(ISASConnection dbc, String nCartella,
			String idSkso) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "recuperaRmSksoMMG ";
		String query = recuperaQueryKeySksoMMG(nCartella, idSkso);
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbrSksoMMg = dbc.readRecord(query);
		return dbrSksoMMg;
	}

//	public void verificaOperatoriPresenti(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
//		String punto = ver + "verificaOperatoriPresenti ";
//		String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
//		String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
//		String mmgPlsInserito = CostantiSinssntW.CTS_NO;
//		String query = recuperaQueryKey(nCartella, idSkso, GestTpOp.CTS_COD_MMG, true);
//		ISASRecord dbrRmSksoOpCoinvolti = dbc.readRecord(query);
//		
//		if (dbrRmSksoOpCoinvolti!=null){
//			mmgPlsInserito = CostantiSinssntW.CTS_SI;
//		}
//		
//		dbr.put(CostantiSinssntW.CTS_OP_COINVOLTI,mmgPlsInserito);
//		LOG.trace(punto + " mmgPlsInserito>>" +mmgPlsInserito);
//	}

//	public boolean verificaPresenzeAutorizzazioni(myLogin myLogin, Hashtable<String, String> prtDati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		String punto = ver + "verificaPresenzeAutorizzazioni ";
//		LOG.debug(punto + " Inizio con dati>"+prtDati);
//		boolean autorizzazioniPresenti= false;
//		ISASConnection dbc = null;
//		
//		try {
//			dbc = super.logIn(myLogin);
//			String nCartella = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.N_CARTELLA);
//			String idSkso = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.CTS_ID_SKSO);
//			String prDataPuac = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.CTS_PR_DATA_PUAC);
//			String prDataChiusura = ISASUtil.getValoreStringa(prtDati, CostantiSinssntW.PR_DATA_CHIUSURA);
//			SkmmgEJB skmmgEJB = new SkmmgEJB();
//			autorizzazioniPresenti =  skmmgEJB.queryKeyEsisteAutorizzazione(dbc, nCartella,idSkso, prDataPuac, prDataChiusura, "skmmg_adi");
//			LOG.debug(punto + " Esistono autorizzazioniAdi>"+autorizzazioniPresenti);
//			if (!autorizzazioniPresenti){
//				autorizzazioniPresenti =  skmmgEJB.queryKeyEsisteAutorizzazione(dbc, nCartella,idSkso, prDataPuac, prDataChiusura, "skmmg_adr");
//				LOG.debug(punto + " Esistono autorizzazioniAdr>"+autorizzazioniPresenti);
//				if (!autorizzazioniPresenti){
//					autorizzazioniPresenti =  skmmgEJB.queryKeyEsisteAutorizzazione(dbc, nCartella,idSkso, prDataPuac, prDataChiusura, "skmmg_adp");
//					LOG.debug(punto + " Esistono autorizzazioniAdp>"+autorizzazioniPresenti);
//				}
//			}
//		} catch (DBRecordChangedException e) {
//			throw e;
//		} catch (ISASPermissionDeniedException e) {
//			throw e;
//		} catch (Exception e) {
//			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
//		} finally {
//			logout_nothrow(punto, dbc);
//		}
//		return autorizzazioniPresenti;
//	}
	
	
	
	public Hashtable cercaOpCoinvolti(myLogin mylogin, Hashtable h) throws Exception {
		String nomeMetodo = ver + "cercaOpCoinvolti";
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		try {
			dbc = super.logIn(mylogin);
			String n_cartella   	 = ""+h.get("n_cartella");
			String tipo_operatore 	 = ""+h.get("tipo_operatore");			
			String codice_operatore  	= CaribelSessionManager.getInstance().getStringFromProfile("codice_operatore");
			String zona_operatore  		= CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
			String distr_operatore 		= CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");	
			boolean obbligoPV = ISASUtil.getvaloreBoolean(h, ManagerProfile.SO_OBB_CDI_PRIMA_VISITA);
			LinkedList<String> listaFonte = new LinkedList<String>();
			listaFonte.add(CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"");
			listaFonte.add(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"");
			String destinatariRichiesta = CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_OPERATORE;
			String myselect=ListaAttivitaEJB.recuperaQuery(dbc, n_cartella, "", "", 
					CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+ "," + CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA , 
					tipo_operatore, 0, 0, "", "", codice_operatore, zona_operatore, distr_operatore, "", 
					obbligoPV, false, true,0,"","", false, null, null, listaFonte, destinatariRichiesta,0);

			LOG.trace(nomeMetodo+" - myselect: "+myselect);
			System.out.println("myselect" + myselect);
			dbr=dbc.readRecord(myselect);
			if (dbr!=null)return dbr.getHashtable();
			else return null;
		} catch (Exception e) {
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public String recuperaCognomeNomeOperatore(ISASConnection dbc, String nCartella, String idSkSo, String tipoOperatore) {
		String punto = ver + "recuperaCognomeNomeOperatore ";
		String cognomeNomeOperatore = "";
		
		String query = recuperaQueryKey(nCartella, idSkSo, tipoOperatore, true);
		ISASRecord dbrRmSkSoOpCoinvolti;
		try {
			dbrRmSkSoOpCoinvolti = dbc.readRecord(query);
			if (dbrRmSkSoOpCoinvolti!=null){
				String codCodice = ISASUtil.getValoreStringa(dbrRmSkSoOpCoinvolti, "");
				if (ISASUtil.valida(codCodice)){
					cognomeNomeOperatore= ISASUtil.getDecode(dbc,"operatori","codice",codCodice, "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","cod_operatore_descr");
				}else {	
					cognomeNomeOperatore  = ISASUtil.getValoreStringa(dbrRmSkSoOpCoinvolti, "op_cognome");
					cognomeNomeOperatore += (ISASUtil.valida(cognomeNomeOperatore)?" ":"")+ISASUtil.getValoreStringa(dbrRmSkSoOpCoinvolti, "op_nome");
				}
			}
		} catch (Exception e) {
			LOG.error(punto + " Errore nella query >>" +query +"\n", e);
		}
		return cognomeNomeOperatore;
	}
	
}

package it.caribel.app.sinssnt.bean.nuovi;


import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
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
import java.util.Vector;
import org.zkoss.util.resource.Labels;

public class SegnalazioniEJB extends SegnalazioniBaseEJB  {
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

public SegnalazioniEJB() {}
private static final String ver = "9-";

	public ISASRecord queryKeyRecord(myLogin mylogin, Hashtable<String, String> h)
			throws SQLException {
		String punto = ver + "queryKeyRecord ";
		LOG.info(punto + " inizio con dati>>" + h);
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		try {
			dbc = super.logIn(mylogin);
			
			String query = recuperaQueryKey(dbc, h, true);
			LOG.trace(punto + " query>> " + query);
			dbr = dbc.readRecord(query);
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati:"+ e);
			throw new SQLException("Errore eseguendo una queryKey()  ");
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbr;
	}

	private ISASRecord decodificaDati(ISASConnection dbc, ISASRecord dbr, Hashtable<String, String> descrizioneTipoOperatore) throws ISASMisuseException {
		String tipoOperatore = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SEGNALAZIONE_TIPO_OPERATORE);
		if (ISASUtil.valida(tipoOperatore)){
			String tipoOperatoreCorrente = ISASUtil.getValoreStringa(descrizioneTipoOperatore, tipoOperatore);
			dbr.put(CostantiSinssntW.CTS_SEGNALAZIONE_TIPO_OPERATORE+"_descr", tipoOperatoreCorrente);
			String codOperatore = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SEGNALAZIONE_COD_OPERATORE);
			if(ISASUtil.valida(codOperatore)){
				String nomeOperatore = ManagerOperatore.cognomeNomeOperatore(dbc, tipoOperatore, codOperatore);
				dbr.put(CostantiSinssntW.CTS_SEGNALAZIONE_COD_OPERATORE +"_descr", nomeOperatore);
			}
			String vistaSo = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO);
			if(ISASUtil.valida(vistaSo)){
				String vistaSoDescr = recuperaDescrizione(vistaSo);
				dbr.put(CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO+"_descr", vistaSoDescr);
			}
		}
		return dbr;
	}

	private String recuperaDescrizione(String vistaSo) {
		String vistaSoDescr = "";
//		vistaSoDescr = Costanti.CTS_NO;
		vistaSoDescr = Labels.getLabel("elenco.segnalazione.stato.num_1");
		if (ISASUtil.valida(vistaSo)){
				vistaSoDescr = Labels.getLabel("elenco.segnalazione.stato.num_"+vistaSo);
		}
		return vistaSoDescr;
	}

	public void delete(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
			String punto = ver + "delete ";
			ISASConnection dbc = null;
			LOG.trace(punto + " rimozione dei dati ");
			try{
				dbc = super.logIn(mylogin);
				dbc.deleteRecord(dbr);
			}catch(DBRecordChangedException e){
				LOG.error(punto + " Errore nel recupera i dati ");
				throw e;
			}catch(ISASPermissionDeniedException e){
				LOG.error(punto + " Errore nel recupera i dati ");
				throw e;
			}catch(Exception e1){
				LOG.error(punto + " Errore nel recupera i dati ");
				throw new SQLException("DiagnosiEJB: Errore eseguendo una delete() - " + e1);
			}finally{
				logout_nothrow(punto, dbc);
			}
		}
	
	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException{
		String punto = ver + "queryKey ";
		LOG.info(punto + " inizio con dati>>" + h );
		ISASConnection dbc = null;
		ISASRecord dbr =null; 
		String pAttivo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_IA_PRINCIPIO_ATTIVO);
		String progressivo = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_IA_PROGRESSIVO);
		try{
			dbc = super.logIn(mylogin);
			if (ISASUtil.valida(progressivo) || ISASUtil.valida(pAttivo)){
				String query = recuperaQueryKey(dbc, h, true);
				LOG.trace(punto + " query>> "+ query);
				dbr = dbc.readRecord(query);
				if (dbr != null) {
//					recuperaIntolleranza(dbc, dbr);
//					decodificaIntolleranze(dbc,vet);
				}
			}else {
				LOG.trace(punto + " non eseguo le query ");
			}
		}catch(Exception e){
			System.out.println("DiagnosiEJB: queryKey - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			logout_nothrow(punto, dbc);
		}
		return dbr;
	}

	private void decodificaIntolleranze(ISASConnection dbc,
			Vector<ISASRecord> vet) throws Exception {
		for (int i = 0; i < vet.size(); i++) {
			ISASRecord dbr = (ISASRecord)vet.get(i);
			
			String codice = ISASUtil.getValoreStringa(dbr, "codice");
			String descrizione = util.getDecode(dbc, "principi_attivi", "codice", codice,
								      "descrizione", "descrizione");
			dbr.put("codice_descr", descrizione);
		}
		
	}
	
	private Vector<ISASRecord> decodificaOperatore(ISASConnection dbc,
			Vector<ISASRecord> vet, boolean leggoPerSo) throws Exception {
		String punto = ver + "decodificaOperatore ";
		Vector<ISASRecord> vetRest = new Vector<ISASRecord>();
		Hashtable<String, String> descrizioneTipoOperatore = ManagerOperatore.loadTipiOperatori();
		for (int i = 0; i < vet.size(); i++) {
			ISASRecord dbr = (ISASRecord)vet.get(i);
			//	RIMOSSO L'AGGIORNAMENTO AUTOMATICO
//			if (dbr !=null && leggoPerSo){
//				dbr = aggiornaRecordSo(dbc, dbr.getHashtable());
//			}
			dbr = decodificaDati(dbc, dbr, descrizioneTipoOperatore);
			LOG.trace(punto + " inizio con dati>>" + (dbr!=null ? dbr.getHashtable()+"": " no dati "));
			vetRest.add(dbr);
		}
		return vetRest;
	}
	
	public void aggiornaStatoVistaSo(myLogin mylogin, Hashtable<String, String> h) throws SQLException {
		String punto = ver + "aggiornaStatoVistaSo ";
		LOG.info(punto + " inizio con dati>>" + h);
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			aggiornaRecordSo(dbc, h);
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati:" + e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

	private ISASRecord aggiornaRecordSo(ISASConnection dbc, Hashtable dati) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException {
		String punto = ver + "aggiornaRecordSo ";
		LOG.trace(punto + " inizio con dati>>" + (dati!=null ? dati+"": " no dati "));
		
		String query = recuperaQueryKey(dbc, dati, true);
		ISASRecord dbrRmSegnalazione = dbc.readRecord(query);
		if(dbrRmSegnalazione!=null) {
			int vistaSo = ISASUtil.getValoreIntero(dati, CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO);
			dbrRmSegnalazione.put(CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO, vistaSo);
			dbc.writeRecord(dbrRmSegnalazione);
			query = recuperaQueryKey(dbc, dbrRmSegnalazione.getHashtable(), true);
			dbrRmSegnalazione = dbc.readRecord(query);
		}
		return dbrRmSegnalazione;
	}

	public Vector<ISASRecord> query(myLogin mylogin, Hashtable<String, Object> h) throws  SQLException{
		String punto = ver + "query";
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		LOG.trace(punto );
		String query = "";
		boolean leggoPerSo = false;
		
		try{
			dbc = super.logIn(mylogin);
			LOG.trace(punto + " recupero dati >"+h );
			leggoPerSo = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_SEGNALAZIONE_SONO_SO);
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA));
			query = recuperaQueryKey(dbc, dati, false);
			dbcur = dbc.startCursor(query);
			vdbr = (Vector<ISASRecord>)dbcur.getAllRecord();
			vdbr = decodificaOperatore(dbc, vdbr, leggoPerSo);
			dbc.close();
			super.close(dbc);

		}catch(Exception e){
			throw new SQLException("Errore eseguendo una query()  ",e);
		}finally{
			 logout_nothrow(punto, dbcur, dbc);
	   	}
		return vdbr;
	}

	@SuppressWarnings("rawtypes")
	public ISASRecord insert(myLogin mylogin, Hashtable h)
			throws Exception{
			String punto = ver + "insert ";
			LOG.info(punto + " inizio con dati>>" + h);
			ISASConnection dbc = null;
			ISASRecord dbr = null;
			boolean done = false;
			try{
				dbc = super.logIn(mylogin);
				dbc.startTransaction();
				
				dbr = insertSegnalazione(dbc, h);
				dbc.commitTransaction();
				done = true;
			}catch(DBRecordChangedException e){
				LOG.error(punto + " Errore in inserimento dei dati", e);
				throw e;
			}catch(ISASPermissionDeniedException e){
				LOG.error(punto + " Errore in inserimento dei dati", e);
				throw e;
			}catch(Exception e1){
				LOG.error(punto + " Errore in inserimento dei dati", e1);
				throw e1;
			}finally{
				if(!done)
					rollback_nothrow(punto, dbc);
				logout_nothrow(punto, dbc);
			}
			return dbr;
		}

		public ISASRecord update(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, ISASMisuseException, DBMisuseException, DBSQLException{
			String punto = ver + "update ";
			ISASConnection dbc = null;
			try{
				dbc = super.logIn(mylogin);
				dbc.startTransaction();
				dbc.writeRecord(dbr);
				String myselect = recuperaQueryKey(dbc, dbr.getHashtable(), true);
				dbr = dbc.readRecord(myselect);
				dbc.commitTransaction();
			}catch(DBRecordChangedException e){
				dbc.rollbackTransaction();
				LOG.error(punto + " Errore nel recuperare i dati ", e);
				throw e;
			}catch(ISASPermissionDeniedException e){
				dbc.rollbackTransaction();
				LOG.error(punto + " Errore nel recuperare i dati ", e);
				throw e;
			}catch(Exception e1){
				dbc.rollbackTransaction();
				LOG.error(punto + " Errore nel recuperare i dati ", e1);
				throw new SQLException("Errore eseguendo una update() - " + e1);
			}finally{
				logout_nothrow(punto, dbc);
			}
			return dbr;
		}

		public boolean esisteSegnalazione(ISASConnection dbc, String nCartella, String idSkso) {
			String punto = ver  +"esisteSegnalazione ";
			
			StringBuffer query = new StringBuffer();
			query.append("select count(*) numero from rm_segnalazioni where n_cartella = ");
			query.append(nCartella);
			query.append(" and id_skso = ");
			query.append(idSkso);
			
			LOG.debug(punto + " query>> "+ query);
			ISASRecord dbrSegnalazione = null;
			int numeroSegnalazioni = -1; 
			try {
				dbrSegnalazione = dbc.readRecord(query.toString());
				numeroSegnalazioni = ISASUtil.getValoreIntero(dbrSegnalazione, "numero");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return (numeroSegnalazioni > 0);
		} 
}
		

package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 03/03/2006 - EJB di connessione alla procedura SINS Tabella Skmmg
//
//
// ==========================================================================

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
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
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import com.itextpdf.text.log.Logger;
//import com.sun.enterprise.log.Log;

public class SkmmgEJB extends SINSSNTConnectionEJB {

	// 13/12/06 m.: sostituito la lettura da tabelle PROGETTO/SKPATOLOGIE
	// con quella dalla tabella DIAGNOSI nel metodo "query_patologie()"
	// + sostituito decod da tabella ICD9 a TAB_DIAGNOSI nel metodo
	// "CaricaPatologie()".

	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil(); 
	private GestCasi gestore_casi = new GestCasi();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();

	private String msgNoD = "Mancano i diritti per leggere il record";
	public static final String SKADI_DATA = "skadi_data";
	public static final String SKADI_OPERATORE = "skadi_operatore";
	public static final String SKADI_MMGPLS = "skadi_mmgpls";
	public static final String SKADI_APPROVA = "skadi_approva";
	public static final String SKADI_DATA_INIZIO = "skadi_data_inzio";
	public static final String SKADI_DATA_FINE = "skadi_data_fine";
	public static final String SKADI_FREQ_MENS = "skadi_freq_mens";
	public static final String SKADI_SPECIFICA = "skadi_specifica";
	public static final String SKADI_SPECIFICA_0 = "0";
	
	private static final String ver = "14-";

	public SkmmgEJB() {
	}
	
	public Vector queryAll_Adr(myLogin mylogin, Hashtable h)throws SQLException {
		String punto = ver + "queryAll_Adr ";
		LOG.debug(punto + " inizio con dati>>" + h + "<");
		ISASConnection dbc = null;
		ISASCursor dbrCursor = null;
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		Vector vdbr = new Vector();
		try {   
			dbc = super.logIn(mylogin);
			String query = queryAllAdr(nContatto, nCartella);
//			String mysel = "SELECT * FROM skmmg_adr WHERE n_cartella = "+
//					ISASUtil.getValoreStringa(h,CostantiSinssntW.N_CARTELLA) + " AND pr_data = "
//					+ formatDate(dbc, (String) h.get("pr_data"));
//			String myselCur = mysel + " ORDER BY skadr_data DESC";
			LOG.trace(punto + " query>>" +query);
			
			dbrCursor = dbc.startCursor(query);
			vdbr = dbrCursor.getAllRecord();
			decodificaDatiADR(dbc, vdbr);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryAll_Adr()  ");
		} finally {
			logout_nothrow(punto, dbrCursor, dbc);
		}
		LOG.debug(punto + " ho recuperato elementi>>" +(vdbr!=null ? vdbr.size()+"": " no dati "));
		
		return vdbr;
	}
	
	public ISASRecord queryKey_Adr(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKey_Adr ";
		LOG.info(punto + " inizio con dati>>"+ h);
		ISASConnection dbc = null;
		ISASRecord dbrSkMMGAdr = null;
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
//		String prData = ISASUtil.getValoreStringa(h, CostantiSinssntW.PR_DATA);
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		String skadrData = ISASUtil.getValoreStringa(h, CostantiSinssntW.MMG_ADI_SKADR_DATA);
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			if (ManagerDate.validaData(skadrData)){
				dbc = super.logIn(mylogin);
				dbrSkMMGAdr = queryKeyAdr(dbc, nCartella, nContatto, skadrData);
			}
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
			LOG.trace(punto + " dati recuperati >>>" +
				(dbrSkMMGAdr!=null ? dbrSkMMGAdr.getHashtable()+ "": "no dati "));
		return dbrSkMMGAdr;
	}
	

	public ISASRecord queryKeyAdr(ISASConnection dbc, String nCartella, String nContatto, String skadrData)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, Exception {
		String punto= ver + "queryKeyAdr "; 
		ISASRecord dbrSkMMGAdr;
		String myselect = queryKeyAdr(nCartella, nContatto, skadrData, dbc);
//		String myselect = " SELECT * FROM skmmg_adr WHERE n_cartella = " + nCartella + " AND pr_data = "
//			+ formatDate(dbc, prData) + " AND skadr_data = " + formatDate(dbc, skadrData);

		LOG.trace(punto + " query>>" + myselect);
		dbrSkMMGAdr = dbc.readRecord(myselect);
		decodificaDatiADR(dbrSkMMGAdr, dbc);
		return dbrSkMMGAdr;
	}
	
	
	public Vector queryAll_Adp(myLogin mylogin, Hashtable h)throws SQLException {
		String punto = ver + "queryAll_Adp ";
		LOG.debug(punto + " inizio con dati>>" + h + "<");
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		ISASConnection dbc = null;
		ISASCursor dbrCursor = null;
		Vector vdbr = new Vector();
		try {   
			dbc = super.logIn(mylogin);
			String myselCur = queryallAdp(nCartella, nContatto);
//			String mysel = "SELECT * FROM skmmg_adp WHERE n_cartella = "+
//					ISASUtil.getValoreStringa(h,CostantiSinssntW.N_CARTELLA) + " AND pr_data = "
//					+ formatDate(dbc, (String) h.get("pr_data"));
//			String myselCur = mysel + " ORDER BY skadp_data DESC";
			LOG.trace(punto + " query>>" +myselCur);
			
			dbrCursor = dbc.startCursor(myselCur);
			vdbr = dbrCursor.getAllRecord();
			decodificaDatiADP(dbc, vdbr);

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryAll_Adp()  ");
		} finally {
			logout_nothrow(punto, dbrCursor, dbc);
		}
		LOG.debug(punto + " ho recuperato elementi>>" +(vdbr!=null ? vdbr.size()+"": " no dati "));
		
		return vdbr;
	}
	
	private void decodificaDatiADP(ISASConnection dbc, Vector vdbr) throws ISASMisuseException, Exception {
		String punto = ver  + "decodificaDatiADP ";
		if (vdbr!=null){
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbrSkmmgAdp = (ISASRecord)vdbr.get(i);
				if (dbrSkmmgAdp!=null){
					decodificaDatiADP(dbrSkmmgAdp, dbc);
					LOG.debug(punto + "dati recuperati>>"+ 
							(dbrSkmmgAdp!=null ? dbrSkmmgAdp.getHashtable()+"": " no dati "));
				}
			}
		}	
	}

	private void decodificaDatiADR(ISASConnection dbc, Vector vdbr) throws ISASMisuseException, Exception {
		if (vdbr!=null){
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbrSkmmgAdr = (ISASRecord)vdbr.get(i);
				if (dbrSkmmgAdr!=null){
					decodificaDatiADR(dbrSkmmgAdr, dbc);
				}
			}
		}
	}

	public ISASRecord queryKey_Adp(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKey_Adp ";
		LOG.info(punto + " inizio con dati>>"+ h);
		ISASConnection dbc = null;
		ISASRecord dbrSkMMGAdp = null;
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
//		String prData = ISASUtil.getValoreStringa(h, CostantiSinssntW.PR_DATA);
		String skadpData = ISASUtil.getValoreStringa(h, CostantiSinssntW.MMG_ADI_SKADP_DATA);
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			if (ManagerDate.validaData(skadpData)){
				dbc = super.logIn(mylogin);
				dbrSkMMGAdp = queryKeyAdp(dbc, nContatto, skadpData, nCartella);
			}
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrSkMMGAdp;
	}

	public ISASRecord queryKeyAdp(ISASConnection dbc, String nContatto, String skadpData, String nCartella)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, Exception {
		ISASRecord dbrSkMMGAdp;
		String punto = ver + "queryKeyAdp ";
		String myselect = queryKeyAdp(nCartella, nContatto, skadpData, dbc);
//				String myselect = " SELECT * FROM skmmg_adp WHERE n_cartella = "
//					+ nCartella + " AND pr_data = "
//					+ formatDate(dbc, prData) + " AND skadp_data = " + formatDate(dbc, skadpData);

		LOG.trace(punto + " query>>" + myselect);
		dbrSkMMGAdp = dbc.readRecord(myselect);
		decodificaDatiADP(dbrSkMMGAdp, dbc);
		return dbrSkMMGAdp;
	}
	
	public Vector queryAll_Adi(myLogin mylogin, Hashtable h)throws SQLException {
		String punto = ver + "queryAll_Adi ";
		LOG.debug(punto + " inizio con dati>>" + h + "<");
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		String nCartella = ISASUtil.getValoreStringa(h,CostantiSinssntW.N_CARTELLA);
		ISASConnection dbc = null;
		ISASCursor dbrCursor = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {   
			dbc = super.logIn(mylogin);
			String mysel = queryAllAdi(nContatto, nCartella);
			LOG.trace(punto + " query>>" +mysel);
			
			dbrCursor = dbc.startCursor(mysel);
			vdbr = dbrCursor.getAllRecord();
			decodificaDatiADI(dbc, vdbr);

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKeyLast()  ");
		} finally {
			logout_nothrow(punto, dbrCursor, dbc);
		}
		LOG.debug(punto + " ho recuperato elementi>>" +(vdbr!=null ? vdbr.size()+"": " no dati "));
		
		return vdbr;
	}

	public String queryAllAdi(String nContatto, String nCartella) {
		String mysel = "SELECT * FROM skmmg_adi" + " WHERE n_cartella = "+
				nCartella + " AND n_contatto = " + nContatto;
		mysel = mysel + " ORDER BY skadi_data DESC";
		return mysel;
	}
	
	private void decodificaDatiADI(ISASConnection dbc, Vector vdbr) throws ISASMisuseException, Exception {
		String punto = ver  + "decodificaDatiADI ";
		if (vdbr!=null){
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbrSkmmgAdi = (ISASRecord)vdbr.get(i);
				if (dbrSkmmgAdi!=null){
					decodificaDatiADI(dbc, dbrSkmmgAdi);
					LOG.debug(punto + "dati recuperati>>"+ 
							(dbrSkmmgAdi!=null ? dbrSkmmgAdi.getHashtable()+"": " no dati "));
				}
			}
		}
	}

	public ISASRecord queryKey_Adi(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKey_Adi ";
		LOG.info(punto + " inizio con dati>>"+ h);
		ISASConnection dbc = null;
		ISASRecord dbrSkMMGAdi = null;
//		String prData = ISASUtil.getValoreStringa(h, CostantiSinssntW.PR_DATA);
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		String skadiData = ISASUtil.getValoreStringa(h, CostantiSinssntW.MMG_ADI_SKADI_DATA);
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA) ;
		
		try {
			if (ManagerDate.validaData(skadiData)){
				dbc = super.logIn(mylogin);
				dbrSkMMGAdi = queryKeyAdi(dbc, nContatto, skadiData, nCartella);
			}

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrSkMMGAdi;
	}
	
	
	public boolean queryKeyAdiEsiste(ISASConnection dbc, String nCartella,String nContatto, String prDataPuac, String prDataChiusura)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "queryKeyAdiEsiste ";
		boolean autorizzazioneAdiEsistente = false;

		String myselect = "SELECT * FROM skmmg_adi WHERE n_cartella=" + nCartella + " AND n_contatto = "
				+ nContatto + " AND ( skadi_data >= " + formatDate(dbc, prDataPuac)
				+ "   OR   skadi_data <= " + formatDate(dbc, prDataChiusura) + " ) ";
		LOG.trace(punto + " query>>" + myselect);
		ISASRecord dbrSkmmgAdi = dbc.readRecord(myselect);
		autorizzazioneAdiEsistente = (dbrSkmmgAdi != null);
		LOG.trace(punto + " esite>>" +autorizzazioneAdiEsistente);
		
		return autorizzazioneAdiEsistente;
	}
	
	
	public boolean queryKeyAdrEsiste(ISASConnection dbc, String nCartella, String nContatto, String prDataPuac, String prDataChiusura)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "queryKeyAdrEsiste ";
		boolean autorizzazioneAdrEsistente = false;

		String myselect = "SELECT * FROM skmmg_adr WHERE n_cartella=" + nCartella + " AND n_contatto = "
				+ nContatto+ " AND ( skadr_data >= " + formatDate(dbc, prDataPuac)
				+ "   OR   skadr_data <= " + formatDate(dbc, prDataChiusura) + " ) ";
		LOG.trace(punto + " query>>" + myselect);
		ISASRecord dbrSkmmgAdr = dbc.readRecord(myselect);
		autorizzazioneAdrEsistente = (dbrSkmmgAdr != null);
		LOG.trace(punto + " esite>>" + autorizzazioneAdrEsistente);

		return autorizzazioneAdrEsistente;
	}	
	
	
	public boolean queryKeyAdpEsiste(ISASConnection dbc, String nCartella,String nContatto, String prDataPuac, String prDataChiusura) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "queryKeyAdpEsiste ";
		boolean autorizzazioneAdrEsistente = false;

		String myselect = "SELECT * FROM skmmg_adp WHERE n_cartella=" + nCartella + " AND n_contatto = "
				+ nContatto + " AND ( skadp_data >= " + formatDate(dbc, prDataPuac)
				+ "   OR   skadp_data <= " + formatDate(dbc, prDataChiusura) + " ) ";
		LOG.trace(punto + " query>>" + myselect);
		ISASRecord dbrSkmmgAdp = dbc.readRecord(myselect);
		autorizzazioneAdrEsistente = (dbrSkmmgAdp != null);
		LOG.trace(punto + " esite>>" + autorizzazioneAdrEsistente);

		return autorizzazioneAdrEsistente;

	}
	
	

	public ISASRecord queryKeyAdi(ISASConnection dbc, String nContatto, String skadiData, String nCartella)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, Exception {
		ISASRecord dbrSkMMGAdi;
		String punto = ver + "queryKeyAdi";  
		String myselect = queryKeyAdi(nCartella, nContatto, skadiData, dbc);
//				String myselect = " SELECT * FROM skmmg_adi WHERE n_cartella = "
//					+ ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA) + " AND pr_data = "
//					+ formatDate(dbc, prData) + " AND skadi_data = " + formatDate(dbc, skadiData);

		LOG.trace(punto + " query>>" + myselect);
		dbrSkMMGAdi = dbc.readRecord(myselect);
		decodificaDatiADI(dbc, dbrSkMMGAdi);
		return dbrSkMMGAdi;
	}
	
	
	public void delete(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		public ISASRecord update_terapia(myLogin mylogin,Hashtable<String, Object> dati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
			String punto = ver + "delete ";
			LOG.debug(punto + " dati che esamino>>"+ (dbr!=null ? dbr.getHashtable()+"": " non dati "));
			Hashtable<String, Object>dati = convertiDati(dbr);
			delete(mylogin, dati);
	}
 
	private Hashtable<String, Object> convertiDati(ISASRecord dbr) {
		Hashtable<String, Object> datiConvertire = new Hashtable<String, Object>();
		datiConvertire.putAll(dbr.getHashtable());
		datiConvertire.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA));
		datiConvertire.put(CostantiSinssntW.PR_DATA, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.PR_DATA));
		return datiConvertire;
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException {
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = "";
		String data = "";
		ISASRecord dbr = null;
		// System.out.println("*****1"+h.toString());
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				cartella = (String) h.get("n_cartella");
				data = (String) h.get("pr_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.queryKey()-->MANCANO LE CHIAVI PRIMARIE" + ex);
			}
			String sel = "SELECT * FROM skmmg WHERE n_cartella=" + cartella
					+ " AND pr_data=" + formatDate(dbc, data);
			dbr = dbc.readRecord(sel);
			if (dbr != null) {
				dbr.put("griglia_adi", caricaGrigliaADI(dbc, h));
				dbr.put("griglia_adp", caricaGrigliaADP(dbc, h));
				CaricaPatologie(dbc, dbr);
			} else {
				/*
				 * Se non trovo niente con quella data devo andare a caricare il
				 * record con la data piï¿½ recente
				 */
				sel = "SELECT k.* FROM skmmg k WHERE k.n_cartella="
						+ cartella
						+ " AND k.pr_data IN (SELECT MAX(s.pr_data) FROM skmmg s "
						+ " WHERE s.n_cartella=k.n_cartella)";
				System.out.println("Skmmg=>QueryKey=>SELECT MAX:" + sel);
				dbr = dbc.readRecord(sel);
				if (dbr != null) {
					h.put("pr_data", "" + ((java.sql.Date) dbr.get("pr_data")));
					dbr.put("griglia_adi", caricaGrigliaADI(dbc, h));
					dbr.put("griglia_adp", caricaGrigliaADP(dbc, h));
					CaricaPatologie(dbc, dbr);
				}
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("eccezione permesso negato " + e);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 31/10/2006 Chiamato se la form ï¿½ richiamata da elenco autorizzazioni
	public ISASRecord queryKey_elenco(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException {
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = "";
		String data = "";
		ISASRecord dbr = null;
		System.out.println("*****1" + h.toString());
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				cartella = (String) h.get("n_cartella");
				data = (String) h.get("pr_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.queryKey()-->MANCANO LE CHIAVI PRIMARIE" + ex);
			}
			String sel = "SELECT * FROM skmmg WHERE n_cartella=" + cartella
					+ " AND pr_data=" + formatDate(dbc, data);
			dbr = dbc.readRecord(sel);
			if (dbr != null) {
				dbr.put("griglia_adi", caricaGrigliaADI(dbc, h));
				dbr.put("griglia_adp", caricaGrigliaADP(dbc, h));
				dbr.put("griglia_adr", caricaGrigliaADR(dbc, h)); // gb 15.12.08
				CaricaPatologie(dbc, dbr);
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("eccezione permesso negato " + e);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public ISASRecord query_insert(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException {
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = "";
		String data = "";
		ISASRecord dbr = null;
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				cartella = (String) h.get("n_cartella");
				data = (String) h.get("pr_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.query_insert()-->MANCANO LE CHIAVI PRIMARIE"
								+ ex);
			}
			String sel = "SELECT * FROM skmmg WHERE n_cartella=" + cartella
					+ " AND pr_data=" + formatDate(dbc, data);
			dbr = dbc.readRecord(sel);
			if (dbr != null) {
				dbr.put("griglia_adi", caricaGrigliaADI(dbc, h));
				dbr.put("griglia_adp", caricaGrigliaADP(dbc, h));
				CaricaPatologie(dbc, dbr);
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("eccezione permesso negato " + e);
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_insert()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public ISASRecord queryMMG(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = null;
		String dataVariazione = null;
		try {
			dbc = super.logIn(mylogin);
			try {
				cartella = (String) h.get("n_cartella");
				dataVariazione = (String) h.get("data_var");
			} catch (Exception eChiave) {
				throw new SQLException(
						"Errore eseguendo una ContribEJB.decodComAreaDis()."
								+ "Manca la chiave primaria " + eChiave);
			}
			String myselect = "SELECT a.cod_med,a.n_cartella "
					+ "FROM anagra_c a,cartella WHERE " + "a.n_cartella="
					+ cartella + " AND a.n_cartella=cartella.n_cartella"
					+ " AND a.data_variazione IN ";
			String parteIn = "SELECT MAX(data_variazione) from anagra_c WHERE "
					+ "anagra_c.n_cartella=a.n_cartella ";
			// 04/01/12
			String where = "";
			if ((dataVariazione != null) && (!dataVariazione.trim().equals("")))
			where = " AND data_variazione<="
					+ formatDate(dbc, dataVariazione);
					
			// cerco per primo il medico relativo al periodo della scheda
			String select = myselect + "(" + parteIn + where + ")";
System.out.println("SkmmgEJB.queryMMG() - select=["+select+"]");			
			ISASRecord dbr = dbc.readRecord(select);
			if (dbr == null) {
				// non l'ho trovato relativo al periodo della scheda , cerco il
				// massimo in assoluto
				select = myselect + "(" + parteIn + ")";
				dbr = dbc.readRecord(select);
			}
			if (dbr != null) {
				dbr.put("mecodi", (String) util.getObjectField(dbr, "cod_med",
						'S'));
				dbr
						.put(
								"mecogn",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbr, "cod_med", 'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mecogn"));
			}
			dbr.put("data_var", h.get("data_var"));
			// dbr.put("n_cartella", h.get("n_cartella"));
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			throw new SQLException("Errore eseguendo una SkmmgEJB.queryMMG() "
					+ e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out
							.println("Errore eseguendo una SkmmgEJB.queryMMG()"
									+ e1);
				}
			}
		}
	}

	public ISASRecord queryADI(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException, CariException {
		// richiamata quando vado in apri di un dettaglio ADI
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbrec = null;
		String cartella = null;
		String dataSK = null;
		String data = null;
		String messaggio = "";
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				cartella = (String) h.get("n_cartella");
				dataSK = (String) h.get("pr_data");
				data = (String) h.get("skadi_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.queryADI()-->MANCANO LE CHIAVI PRIMARIE" + ex);
			}

			String myselect = "SELECT * FROM skmmg_adi WHERE ";
			String mysel = "";
			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
					cartella);
			mysel = su.addWhere(mysel, su.REL_AND, "skadi_data", su.OP_EQ_NUM,
					formatDate(dbc, data));
			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
					formatDate(dbc, dataSK));
			myselect = myselect + mysel;

			dbrec = dbc.readRecord(myselect);
			if (dbrec != null) {
				dbrec
						.put(
								"desc_MMG",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbrec, "skadi_mmgpls",
														'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				dbrec.put("desc_operADI", util.getDecode(dbc, "operatori",
						"codice", (String) util.getObjectField(dbrec,
								"skadi_operatore", 'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));

				dbrec.put("griglia_adi", caricaGrigliaADI(dbc, dbrec
						.getHashtable())); // 07/11/07
			} else
				messaggio = "Operazione fallita.\n"
						+ "I dati richiesti sono stati rimossi da un altro utente";
			if (!messaggio.equals(""))
				throw new CariException(messaggio);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbrec;
		} catch (CariException ce) {
			ce.setISASRecord(dbrec);
			throw ce;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("eccezione permesso negato " + e);
			return null;
		} catch (Exception e) {
			throw new SQLException(
					"Errore eseguendo una ContribEJB.queryContrib() " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public ISASRecord queryADP(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException, CariException {
		// richiamata quando vado in apri di un dettaglio ADI
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbrec = null;
		String cartella = null;
		String dataSK = null;
		String data = null;
		String messaggio = "";
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				cartella = (String) h.get("n_cartella");
				dataSK = (String) h.get("pr_data");
				data = (String) h.get("skadp_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.queryADP()-->MANCANO LE CHIAVI PRIMARIE" + ex);
			}

			String myselect = "SELECT * FROM skmmg_adp WHERE ";
			String mysel = "";
			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
					cartella);
			mysel = su.addWhere(mysel, su.REL_AND, "skadp_data", su.OP_EQ_NUM,
					formatDate(dbc, data));
			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
					formatDate(dbc, dataSK));
			myselect = myselect + mysel;

			dbrec = dbc.readRecord(myselect);
			if (dbrec != null) {
				decodificaDatiADP(dbrec, dbc);

				dbrec.put("griglia_adp", caricaGrigliaADP(dbc, dbrec
						.getHashtable())); // 08/11/07
			} else
				messaggio = "Operazione fallita.\n"
						+ "I dati richiesti sono stati rimossi da un altro utente";
			if (!messaggio.equals(""))
				throw new CariException(messaggio);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbrec;
		} catch (CariException ce) {
			ce.setISASRecord(dbrec);
			throw ce;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("eccezione permesso negato " + e);
			return null;
		} catch (Exception e) {
			throw new SQLException(
					"Errore eseguendo una ContribEJB.queryContrib() " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public ISASRecord updateADI(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		String punto = ver + "updateADI ";
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = null;
//		String dataSK = null;
		String nContatto = null;
		String data = null;
		ServerUtility su = new ServerUtility();
		try {
			cartella = (String) dbr.get("n_cartella");
			nContatto = (String) dbr.get(CostantiSinssntW.N_CONTATTO);
			data = (String) dbr.get("skadi_data");
		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.updateADI()-->MANCANO LE CHIAVI PRIMARIE" + ex);
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.writeRecord(dbr);

//			String myselect = "SELECT * FROM skmmg_adi WHERE ";
//			String mysel = "";
//			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//					cartella);
//			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//					formatDate(dbc, dataSK));
//			mysel = su.addWhere(mysel, su.REL_AND, "skadi_data", su.OP_EQ_NUM,
//					formatDate(dbc, data));
//			myselect = myselect + mysel;
			String myselect = queryKeyAdi(cartella, nContatto, data, dbc);
			LOG.trace(punto + " query>>" +myselect);
			dbr = dbc.readRecord(myselect);
			// 07/11/07 ---
			if (dbr != null) {
				decodificaDatiADI(dbc, dbr);
				// 07/11/07 ---
				dbr.put("griglia_adi",
						caricaGrigliaADI(dbc, dbr.getHashtable()));
			}
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			System.out.println("SkmmgEJB.ISAS()-->"
					+ dbr.getHashtable().toString());
			ce.setISASRecord(dbr);
			throw ce;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.updateADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADI().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.updateADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADI().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SkmmgEJB.updateADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADI().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.updateADI()-->" + e);
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

	private void decodificaDatiADI(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException, Exception {
		if (dbr!= null){
			dbr.put("desc_MMG",
							util.getDecode(dbc,"medici","mecodi",(String) util.getObjectField(dbr, "skadi_mmgpls",'S'),
									"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))", "mmg_alias"));
			dbr.put("desc_operADI", util.getDecode(dbc, "operatori","codice", 
					(String) util.getObjectField(dbr,"skadi_operatore", 'S'),"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))", "oper_alias"));
		}
	}

	public ISASRecord updateADP(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = null;
//		String dataSK = null;
		String nContatto = null;
		String data = null;
		ServerUtility su = new ServerUtility();
		try {
			cartella = (String) dbr.get("n_cartella");
//			dataSK = (String) dbr.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CONTATTO);
			data = (String) dbr.get("skadp_data");
		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.updateADP()-->MANCANO LE CHIAVI PRIMARIE" + ex);
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.writeRecord(dbr);
			String myselect = queryKeyAdp(cartella, nContatto, data, dbc);
//			String myselect = "SELECT * FROM skmmg_adp WHERE ";
//			String mysel = "";
//			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//					cartella);
//			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//					formatDate(dbc, dataSK));
//			mysel = su.addWhere(mysel, su.REL_AND, "skadp_data", su.OP_EQ_NUM,
//					formatDate(dbc, data));
//			myselect = myselect + mysel;
			dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				decodificaDatiADP(dbr, dbc);
				dbr.put("griglia_adp", caricaGrigliaADP(dbc, dbr.getHashtable()));
			}

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			System.out.println("SkmmgEJB.ISAS()-->"
					+ dbr.getHashtable().toString());
			ce.setISASRecord(dbr);
			throw ce;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.updateADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADP().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.updateADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADP().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.updateADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADP().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.updateADP()-->" + e);
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

	private void decodificaDatiADP(ISASRecord dbr, ISASConnection dbc)
			throws ISASMisuseException, Exception {
		if (dbr!=null){
			dbr.put("desc_MMGADP",util.getDecode(dbc,"medici","mecodi",
					(String) util.getObjectField(dbr, "skadp_mmgpls",'S'),"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))","mmg_alias"));
			
			dbr.put("desc_operADP", util.getDecode(dbc, "operatori","codice", (String) util.getObjectField(dbr,"skadp_operatore", 'S'),
					"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","oper_alias"));
		}
	}

	public ISASRecord insertADI(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		// ,CariException{
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = null;
		String dataSK = null;
		String data = null;
		String messaggio = "";
		ServerUtility su = new ServerUtility();
		try {
			cartella = (String) h.get("n_cartella");
			dataSK = (String) h.get("pr_data");
			data = (String) h.get("skadi_data");
		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.insertADI()-->MANCANO LE CHIAVI PRIMARIE" + ex);
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			// Controllo che sia stata inserita la scheda MMG
			/*
			 * FACCIO TUTTO LATO CLIENT String
			 * sel="SELECT * FROM skmmg WHERE n_cartella="+cartella+
			 * " AND pr_data="+formatDate(dbc,dataSK); ISASRecord dbr =
			 * dbc.readRecord(sel); System.out.println("Select su SKMMG:"+sel);
			 * //Se non esiste la scheda vado ad inserirla if(dbr==null){
			 * System.out.println("RECORD NON ESISTE"); ISASRecord dbNuovo =
			 * dbc.newRecord("skmmg"); dbNuovo.put("n_cartella", cartella);
			 * dbNuovo.put("pr_data", dataSK);
			 * System.out.println("Vado a scrivere SKMMG");
			 * dbc.writeRecord(dbNuovo); }
			 */
			insertDbADI(dbc, h);
			// altrimenti se non esiste la scheda generale DBR rimane null
			// e dï¿½ errore quando si fa la put
			ISASRecord dbr = dbc.newRecord("skmmg_adi");
			dbr.put("n_cartella", cartella);
			dbr.put("pr_data", dataSK);
			dbr.put("griglia_adi", caricaGrigliaADI(dbc, h));
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			return dbr;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.insertADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.insertADI().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.insertADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.insertADI().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.insertADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.insertADI().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.insertADI()-->" + e);
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

	private void insertDbADI(ISASConnection dbc, Hashtable h) throws Exception {
		String messaggio = "";
		try {
			ISASRecord dbr = dbc.newRecord("skmmg_adi");
			System.out.println("SONO IN INSERTDBADI" + h.toString());
			ServerUtility su = new ServerUtility();
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}
			dbc.writeRecord(dbr);
		} catch (Exception ex) {
			throw new SQLException(
					"Errore eseguendo una SkmmgEJB.insertDbADI()- " + ex);
		}
	}

	private Vector caricaGrigliaADI(ISASConnection dbc, Hashtable h)
			throws Exception {
		String punto = ver + "caricaGrigliaADI ";
		Vector vdbg = new Vector();
		String cartella = null;
		String nContatto = null;
		String data = null;
		ServerUtility su = new ServerUtility();
		ISASRecord dbr = null;
		
		try {
			if (h.get("n_cartella") instanceof String)
				cartella = (String) h.get("n_cartella");
			else if (h.get("n_cartella") instanceof Integer)
				cartella = "" + (Integer) h.get("n_cartella");
//			if (h.get("pr_data") instanceof String)
//				data = (String) h.get("pr_data");
//			else if (h.get("pr_data") instanceof java.sql.Date)
//				data = "" + (java.sql.Date) h.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.caricaGrigliaADI()-->MANCANO LE CHIAVI PRIMARIE"
							+ ex);
		}
		try {
			System.out.println("Dentro caricaGrigliaADI");
//			String myselect = "SELECT * FROM skmmg_adi WHERE ";
//			String mysel = "";
//			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//					cartella);
//			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//					formatDate(dbc, data));
//			myselect = myselect + mysel + "ORDER BY skadi_data DESC";
			String myselect = queryAllAdi(nContatto, cartella);
			ISASCursor dbcur = dbc.startCursor(myselect);
			LOG.debug(punto + " query>" + myselect);
			dbr = dbc.readRecord(myselect);
			while (dbcur.next()) {
				ISASRecord dbrec = dbcur.getRecord();
				// MMG
				dbrec
						.put(
								"desc_MMGADI",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) dbrec
														.get("skadi_mmgpls"),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				vdbg.addElement(dbrec);
			}// end while
			dbcur.close();
			return vdbg;
		} catch (Exception ex) {
			throw new SQLException("SkmmgEJB.caricaGrigliaADI()-->" + ex);
		}
	}

	public ISASRecord insertADP(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		// ,CariException{
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = null;
		String dataSK = null;
		String data = null;
		String messaggio = "";
		ServerUtility su = new ServerUtility();
		try {
			cartella = (String) h.get("n_cartella");
			dataSK = (String) h.get("pr_data");
			data = (String) h.get("skadp_data");
		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.insertADP()-->MANCANO LE CHIAVI PRIMARIE" + ex);
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			/*
			 * FACCIO TUTTO LATO CLIENT //Controllo che sia stata inserita la
			 * scheda MMG String
			 * sel="SELECT * FROM skmmg WHERE n_cartella="+cartella+
			 * " AND pr_data="+formatDate(dbc,dataSK); ISASRecord dbr =
			 * dbc.readRecord(sel); //Se non esiste la scheda vado ad inserirla
			 * if(dbr==null){ ISASRecord dbNuovo = dbc.newRecord("skmmg");
			 * dbNuovo.put("n_cartella", cartella); dbNuovo.put("pr_data",
			 * dataSK); System.out.println("Vado a scrivere SKMMG");
			 * dbc.writeRecord(dbNuovo); }
			 */
			insertDbADP(dbc, h);
			// altrimenti se non esiste la scheda generale DBR rimane null
			// e dï¿½ errore quando si fa la put
			ISASRecord dbr = dbc.newRecord("skmmg_adi");
			dbr.put("n_cartella", cartella);
			dbr.put("pr_data", dataSK);
			dbr.put("griglia_adp", caricaGrigliaADP(dbc, h));
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			return dbr;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.insertADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.insertADP().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.insertADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.insertADP().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.insertADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.insertADP().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.insertADP()-->" + e);
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

	private void insertDbADP(ISASConnection dbc, Hashtable h) throws Exception {
		String messaggio = "";
		try {
			ISASRecord dbr = dbc.newRecord("skmmg_adp");
			ServerUtility su = new ServerUtility();
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}
			dbc.writeRecord(dbr);
		} catch (Exception ex) {
			throw new SQLException(
					"Errore eseguendo una SkmmgEJB.insertDbADP()- " + ex);
		}
	}

	private Vector caricaGrigliaADP(ISASConnection dbc, Hashtable h)
			throws Exception {
		Vector vdbg = new Vector();
		String cartella = null;
		String nContatto = null;
//		String data = null;
		ServerUtility su = new ServerUtility();
		ISASRecord dbr = null;
		try {
			if (h.get("n_cartella") instanceof String)
				cartella = (String) h.get("n_cartella");
			else if (h.get("n_cartella") instanceof Integer)
				cartella = "" + (Integer) h.get("n_cartella");
//			if (h.get("pr_data") instanceof String)
//				data = (String) h.get("pr_data");
//			else if (h.get("pr_data") instanceof java.sql.Date)
//				data = "" + (java.sql.Date) h.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO); 

		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.caricaGrigliaADP()-->MANCANO LE CHIAVI PRIMARIE"
							+ ex);
		}
		try {
			System.out.println("Dentro caricaGrigliaADP");
			String myselect = queryallAdp(cartella, nContatto);
//			String myselect = "SELECT * FROM skmmg_adp WHERE ";
//			String mysel = "";
//			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//					cartella);
//			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//					formatDate(dbc, data));
//			myselect = myselect + mysel + "ORDER BY skadp_data DESC";
			ISASCursor dbcur = dbc.startCursor(myselect);
			dbr = dbc.readRecord(myselect);
			while (dbcur.next()) {
				ISASRecord dbrec = dbcur.getRecord();
				// MMG
				dbrec
						.put(
								"desc_MMGADP",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbrec, "skadp_mmgpls",
														'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				vdbg.addElement(dbrec);
			}// end while
			dbcur.close();
			return vdbg;
		} catch (Exception ex) {
			throw new SQLException("SkmmgEJB.caricaGrigliaADP()-->" + ex);
		}
	}

	private String queryallAdp(String cartella, String nContatto) {
		String myselect = "SELECT * FROM skmmg_adp WHERE n_cartella = " + cartella +" and n_contatto = "+ nContatto 
				+"ORDER BY skadp_data DESC";
//		mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//				cartella);
//		mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//				formatDate(dbc, data));
//		myselect = myselect + mysel + "ORDER BY skadp_data DESC";
		return myselect;
	}

	private Vector caricaGrigliaADR(ISASConnection dbc, Hashtable h)
			throws Exception {
		String punto = ver + "caricaGrigliaADR ";
		Vector vdbg = new Vector();
		String cartella = null;
//		String data = null;
		ISASCursor dbcur = null;
		String nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
		try {
			if (h.get("n_cartella") instanceof String)
				cartella = (String) h.get("n_cartella");
			else if (h.get("n_cartella") instanceof Integer)
				cartella = "" + (Integer) h.get("n_cartella");
//			if (h.get("pr_data") instanceof String)
//				data = (String) h.get("pr_data");
//			else if (h.get("pr_data") instanceof java.sql.Date)
//				data = "" + (java.sql.Date) h.get("pr_data");

		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.caricaGrigliaADR()-->MANCANO LE CHIAVI PRIMARIE"
							+ ex);
		}
		try {
			String query = queryAllAdr(nContatto, cartella); 
//					"SELECT * FROM skmmg_adr WHERE ";
//			String mysel = "";
//			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//					cartella);
//			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//					formatDate(dbc, data));
//			myselect = myselect + mysel + "ORDER BY skadr_data DESC";
			LOG.trace(punto + " query>>"+ query);
			dbcur = dbc.startCursor(query);
			while (dbcur.next()) {
				ISASRecord dbrec = dbcur.getRecord();
				// MMG
				dbrec
						.put(
								"desc_MMGADR",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbrec, "skadr_mmgpls",
														'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				vdbg.addElement(dbrec);
			}// end while
			return vdbg;
		} catch (Exception ex) {
			throw new SQLException("SkmmgEJB.caricaGrigliaADR()-->" + ex);
		}finally{
			close_dbcur_nothrow(punto, dbcur);
		}
	}

	private String queryAllAdr(String nContatto, String cartella) {
		String query = " SELECT * FROM skmmg_adr WHERE n_cartella = " +cartella + 
				" AND n_contatto = "+ nContatto +" ORDER BY skadr_data DESC"; 
//		mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//				cartella);
//		mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//				formatDate(dbc, data));
//		myselect = myselect + mysel + "ORDER BY skadr_data DESC"
		return query;
	}

	public void deleteADI(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.deleteRecord(dbr);
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.deleteADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADI().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.deleteADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADI().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.deleteADI()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADI().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.deleteADI()-->" + e);
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

	public void deleteADP(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.deleteRecord(dbr);
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.deleteADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADP().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.deleteADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADP().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.deleteADP()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADP().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.deleteADP()-->" + e);
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

	public ISASRecord insert(myLogin mylogin, Hashtable hcom)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		String cartella = null;
		String data = null;
		String operatore = null;
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		try {
			cartella = (String) hcom.get("n_cartella");
			data = (String) hcom.get("pr_data");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"Errore: mancano elementi della chiave primaria");
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			String sel = "SELECT * FROM skmmg WHERE n_cartella=" + cartella
					+ " AND pr_data=" + formatDate(dbc, data);
			System.out.println("SEL in insert:" + sel);
			dbr = dbc.readRecord(sel);
			// Se non esiste la scheda vado ad inserirla
			if (dbr == null) {
				dbr = dbc.newRecord("skmmg");
				dbr.put("n_cartella", cartella);
				dbr.put("pr_data", data);
				System.out.println("Vado a scrivere SKMMG");
				// dbc.writeRecord(dbr);
			}
/** 05/05/11
			// -- Prendo i dati del caso per aggiornare l'eventuale presa in carico -- 
			Hashtable h = new Hashtable();
			h.put("n_cartella", cartella);
			h.put("pr_data", data);
			h.put("mmg_pr_data", data);
			
			
			Hashtable hp = getCasoFromProgetto(mylogin, h);
			h.put("pr_data",hp.get("pr_data"));
			h.put("id_caso",hp.get("id_caso"));
			h.put("skmmg_segnalatore",dbr.get("skmmg_segnalatore"));
			gestore_presacarico.aggRpPresaCarFromSkMMG(dbc,h);
**/			
			Enumeration nH = hcom.keys();
			while (nH.hasMoreElements()) {
				String e = (String) nH.nextElement();
				dbr.put(e, hcom.get(e));
			}
			Enumeration n = (dbr.getHashtable()).keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				if (hcom.get(e) != null)
					dbr.put(e, hcom.get(e));
				else
					dbr.put(e, "");
			}
			dbc.writeRecord(dbr);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e);
			}
			throw new SQLException("Errore eseguendo una insert() - " + e);
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

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		String datavar = (String) h.get("pr_data");
		try {
			dbc = super.logIn(mylogin);
			if (h.get("n_cartella") == null) {
				throw new SQLException(
						"Manca numero cartella in esecuzione query()");
			}
			StringBuffer myselect = new StringBuffer("SELECT pr_data "
					+ "FROM skmmg WHERE " + "n_cartella="
					+ (String) h.get("n_cartella"));
			if (h.get("pr_data") != null) {
				myselect.append(" and pr_data=" + formatDate(dbc, datavar));
			}
			ISASCursor dbcur = dbc.startCursor(myselect.toString());
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public void delete(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			System.out.println("****H:" + h.toString());
			ServerUtility su = new ServerUtility();
			String cartella = (String) h.get("n_cartella");
			String data = (String) h.get("pr_data");
			dbc.startTransaction();
			// cancello tutti i record su skmmg_adi
			String select = "SELECT * FROM skmmg_adi WHERE ";
			String sel = "";
			sel = su.addWhere(sel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
					cartella);
			sel = su.addWhere(sel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
					formatDate(dbc, data));
			select = select + sel;
			ISASCursor dbcur = dbc.startCursor(select);
			while (dbcur.next()) {
				ISASRecord dbdiag = (ISASRecord) dbcur.getRecord();
				String data2 = "" + (java.sql.Date) dbdiag.get("skadi_data");
				/**
				 * 26/10/07 m.: altrimenti aggiunge ogni volta " AND ..."
				 * select=select+" AND skadi_data="+formatDate(dbc,data2);
				 * ISASRecord dbrdel = dbc.readRecord(select);
				 **/
				ISASRecord dbrdel = dbc.readRecord(select + " AND skadi_data="
						+ formatDate(dbc, data2));
				if (dbrdel != null)
					dbc.deleteRecord(dbrdel);
			}
			// cancello tutti i record su skmmg_adp
			select = "SELECT * FROM skmmg_adp WHERE ";
			select += sel;
			dbcur = dbc.startCursor(select);
			while (dbcur.next()) {
				ISASRecord dbdiag = (ISASRecord) dbcur.getRecord();
				String data2 = "" + (java.sql.Date) dbdiag.get("skadp_data");
				/**
				 * 26/10/07 m.: altrimenti aggiunge ogni volta " AND ..."
				 * select=select+" AND skadp_data="+formatDate(dbc,data2);
				 * ISASRecord dbrdel = dbc.readRecord(select);
				 **/
				ISASRecord dbrdel = dbc.readRecord(select + " AND skadp_data="
						+ formatDate(dbc, data2));
				if (dbrdel != null)
					dbc.deleteRecord(dbrdel);
			}
			// cancello da skmmg
			select = "SELECT * FROM skmmg WHERE ";
			select += sel;
			ISASRecord dbr = dbc.readRecord(select);
			dbc.deleteRecord(dbr);
			if (dbcur != null)
				dbcur.close();
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una delete() - " + e);
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

	public ISASRecord query_patologie(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASRecord dbrec = null;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			/***
			 * 13/12/06 //CONTROLLO SUL CONF CHE LE PATOLOGIE DEL PROGETTO SIANO
			 * LEGATE //A ICD9 E NON A SVAMA_PAT String
			 * selConf="SELECT conf_txt FROM conf WHERE "+
			 * "conf_kproc='SINS' AND conf_key='tab_patologia'"; ISASRecord
			 * dbconf = dbc.readRecord(selConf);
			 * //System.out.println("SELCONF:"+
			 * dbconf.getHashtable().toString()); if (dbconf!=null){
			 * if(!((String)dbconf.get("conf_txt")).equals("SVAMA_PAT")){
			 * boolean entrato=false; //VA SU IDC9 E LE RIPORTO SU SKMMG //Se
			 * non trovo nessuna patologia nel progetto vado a prendere //
			 * quelle di SKPATOLOGIE con contatto MAX // (x ora ï¿½ una toppa x il
			 * Salvadori>=Andrea) String
			 * myselect="SELECT p.pr_patol1 cod1,p.pr_patol2 cod2,"+
			 * "p.pr_patol3 cod3,p.pr_patol4 cod4 FROM progetto p"+
			 * " WHERE p.n_cartella="+h.get("n_cartella")+
			 * " AND p.pr_data IN (SELECT MAX (pr_data) FROM progetto "+
			 * " WHERE p.n_cartella=n_cartella)";
			 * System.out.println("Skmmg=>query_patologie:"+myselect);
			 * dbrec=dbc.readRecord(myselect); //while (dbcur.next()){
			 * if(dbrec!=null) if(!((String)dbrec.get("cod1")).equals("") ||
			 * !((String)dbrec.get("cod2")).equals("") ||
			 * !((String)dbrec.get("cod3")).equals("") ||
			 * !((String)dbrec.get("cod4")).equals("")){
			 * System.out.println("Dentro"); entrato=true;
			 * 
			 * dbrec.put("diagnosi1",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)dbrec.get("cod1"),"diagnosi"));
			 * dbrec.put("diagnosi2",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)util.getObjectField(dbrec,"cod2",'S'),"diagnosi"));
			 * dbrec.put("diagnosi3",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)util.getObjectField(dbrec,"cod3",'S'),"diagnosi"));
			 * dbrec.put("diagnosi4",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)util.getObjectField(dbrec,"cod4",'S'),"diagnosi"));
			 * dbrec.put("n_cartella", h.get("n_cartella")); }
			 * if(entrato==false){ String mysel=
			 * "SELECT p.n_cartella,p.skpat_patol1 cod1,p.skpat_patol2 cod2,"+
			 * "p.skpat_patol3 cod3,p.skpat_patol4 cod4 FROM skpatologie p"+
			 * " WHERE p.n_cartella="+h.get("n_cartella")+
			 * " AND p.n_contatto IN (SELECT MAX (n_contatto) FROM skpatologie "
			 * + " WHERE p.n_cartella=n_cartella)";
			 * System.out.println("Skmmg=>query_skpatologie:"+mysel); ISASCursor
			 * dbcur=dbc.startCursor(mysel); while (dbcur.next()){
			 * dbrec=dbcur.getRecord();
			 * dbrec.put("diagnosi1",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)dbrec.get("cod1"),"diagnosi"));
			 * dbrec.put("diagnosi2",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)util.getObjectField(dbrec,"cod2",'S'),"diagnosi"));
			 * dbrec.put("diagnosi3",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)util.getObjectField(dbrec,"cod3",'S'),"diagnosi"));
			 * dbrec.put("diagnosi4",util.getDecode(dbc,"icd9","cd_diag",
			 * (String)util.getObjectField(dbrec,"cod4",'S'),"diagnosi"));
			 * dbrec.put("n_cartella", h.get("n_cartella")); } if(dbcur!=null)
			 * dbcur.close(); } } }
			 ***/
			// 13/12/06: lettura da DIAGNOSI
			String myselect = "SELECT d.diag1 cod1, d.diag2 cod2, d.diag3 cod3, d.diag4 cod4, d.diag5 cod5"
					+ " FROM diagnosi d"
					+ " WHERE d.n_cartella = "
					+ h.get("n_cartella")
					+ " AND d.data_diag IN (SELECT MAX (diagnosi.data_diag) FROM diagnosi"
					+ " WHERE diagnosi.n_cartella = d.n_cartella)";

			System.out.println("Skmmg=>query_patologie:" + myselect);
			dbrec = dbc.readRecord(myselect);

			if (dbrec != null) {
				decodificaDiagn(dbc, dbrec);
				dbrec.put("n_cartella", h.get("n_cartella"));
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbrec;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una SkmmgEJB.query_patologie()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 13/12/06
	private void decodificaDiagn(ISASConnection mydbc, ISASRecord mydbr)
			throws Exception {
		for (int k = 1; k < 6; k++) {
			String cod = (String) mydbr.get("cod" + k);
			String desc = util.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi",
					cod, "diagnosi");
			mydbr.put("diagnosi" + k, desc);
		}
	}// END decodificaDiagn

	public ISASRecord CaricaPatologie(ISASConnection dbc, ISASRecord dbr)
			throws Exception {
		boolean done = false;
		try {
			String w_codice = "";
			String w_descr = "";
			String w_select = "";
			ISASRecord w_dbr = null;
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				for (int i = 0; i <= 5; i++) {
					if (h1.get("skmmg_patol" + i) != null
							&& !((String) h1.get("skmmg_patol" + i)).equals("")) {
						w_codice = ((String) h1.get("skmmg_patol" + i)).trim();
						// 13/12/06 w_select =
						// "SELECT * FROM icd9 WHERE cd_diag='"+w_codice+"'";
						w_select = "SELECT * FROM tab_diagnosi WHERE cod_diagnosi = '"
								+ w_codice + "'";
						w_dbr = dbc.readRecord(w_select);
						if (w_dbr != null) {
							dbr.put("desc_patol" + i, w_dbr.get("diagnosi"));
						} else
							dbr.put("desc_patol" + i, "");
					} else
						dbr.put("desc_patol" + i, "");
				}
			}
			return dbr;
		} catch (Exception e) {
			throw new SQLException("Errore eseguendo una CaricaPatologie() - "
					+ e);
		}
	}

	public ISASRecord query_rsa(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		String codice = "";
		codice = (String) h.get("n_cartella");
		try {
			dbc = super.logIn(mylogin);

			/**
			 * 26/10/07: sostituito tabella PUNTEGGIO con ASS_PUNTEGGIO String
			 * myselect="SELECT * FROM punteggio "+ " WHERE n_cartella="+codice+
			 * " AND data IN (SELECT MAX (p.data)"+
			 * " FROM punteggio p WHERE n_cartella="+codice+" AND "+
			 * " n_contatto=p.n_contatto)";
			 **/
			// 26/10/07
			String myselect = "SELECT * FROM ass_punteggio"
					+ " WHERE n_cartella = " + codice
					+ " AND data IN (SELECT MAX(p.data)"
					+ " FROM ass_punteggio p" + " WHERE p.n_cartella = "
					+ codice + ")";

			System.out.println("Query_rsa su Skmmg " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			;
			if (dbr != null) {
				// 26/10/07 if(dbr.get("data_inizio_ric")!=null &&
				// !dbr.get("data_inizio_ric").equals(""))
				if (dbr.get("data_inizio_ric") != null)
					dbr.put("data_rsa", dbr.get("data_inizio_ric"));
			} else {
				// 26/10/07 dbr=dbc.newRecord("punteggio");
				dbr = dbc.newRecord("ass_punteggio");
				dbr.put("n_cartella", codice);
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_rsa()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public Hashtable verificaContatti(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		Hashtable p = new Hashtable();
		String cartella = (String) h.get("n_cartella");
		try {
			ISASConnection dbc = super.logIn(mylogin);
			p.put("soc", "" + contaContatti(dbc, cartella,
			// 26/10/07 "contatti", "data_contatto", "data_chiusura"));
					// 26/10/07: sostituito tabella CONTATTI con ASS_PROGETTO
					"ass_progetto", "ap_data_apertura", "ap_data_chiusura"));
			p.put("inf", ""
					+ contaContatti(dbc, cartella, "skinf",
							"ski_data_apertura", "ski_data_uscita"));
			p.put("fis", ""
					+ contaContatti(dbc, cartella, "skfis", "skf_data",
							"skf_data_chiusura"));
			p.put("med", ""
					+ contaContatti(dbc, cartella, "skmedico",
							"skm_data_apertura", "skm_data_chiusura"));
			dbc.close();
			super.close(dbc);
			done = true;
			return p;
		} catch (Exception e1) {
			debugMessage("" + e1);
			throw new SQLException("Errore eseguendo una verificaContatti() - "
					+ e1);
		}
	}

	private int contaContatti(ISASConnection dbc, String cartella, String tab,
			String data_ap, String data_ch) throws SQLException {
		try {
			String mysel = "SELECT COUNT(*) cont FROM " + tab
					+ " WHERE n_cartella=" + cartella + " AND " + data_ap
					+ " IS NOT NULL" + " AND " + data_ch + " IS NULL";
			ISASRecord dbr = dbc.readRecord(mysel);
			int conta = convNumDBToInt("cont", dbr);
			return conta;
		} catch (Exception e1) {
			debugMessage("" + e1);
			throw new SQLException("Errore eseguendo una contaContatti() - "
					+ e1);
		}
	}

	// 30/10/2006 metodo per tirare fuori le autorizzazioni in atto e quelle
	// ancora da effettuare
	// ma che hanno degli interventi giï¿½ inseriti.
	public Vector query_elencoAuto(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		System.out.println("HASH:" + h.toString());

		String data = (String) h.get("pr_data");
		String mmg = (String) h.get("mmgpls");
		String oper = (String) h.get("operatore");
		try {
			dbc = super.logIn(mylogin);
			DataWI dataGrad = new DataWI(data.substring(8, 10)
					+ data.substring(5, 7) + data.substring(0, 4));
			DataWI dataGrad2 = dataGrad.aggiungiMm(-4);
			String data2 = dataGrad2.getString(0, "/");
			data2 = data2.substring(6, 10) + "-" + data2.substring(3, 5) + "-"
					+ data2.substring(0, 2);
			// 1ï¿½
			System.out.println("****1");
			String myselect = "SELECT a.skadi_data_inzio data_inizio, a.skadi_data_fine data_fine, "
					+ "c.n_cartella, a.skadi_mmgpls cod_medico, 'ADI' desc_flag,"
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, a.pr_data "
					+ "FROM skmmg_adi a, cartella c, medici m WHERE "
					+ "a.n_cartella=c.n_cartella AND "
					+ "a.skadi_mmgpls=m.mecodi AND "
					+ "(a.skadi_data_fine IS NULL OR "
					+ "a.skadi_data_fine>"
					+ formatDate(dbc, data) + ")";
			System.out.println("****2");
			if (data != null && !data.equals(""))
				myselect += " AND a.skadi_data_inzio<=" + formatDate(dbc, data);
			System.out.println("****3");
			if (mmg != null && !mmg.equals(""))
				myselect += " AND a.skadi_mmgpls='" + mmg + "'";
			System.out.println("****4");
			if (oper != null && !oper.equals(""))
				myselect += " AND a.skadi_operatore='" + oper + "'";
			// 2ï¿½ Ora vado a prendere tutti i record da skmmg_adp
			myselect += " UNION SELECT a.skadp_data_inizio data_inizio, a.skadp_data_fine data_fine, "
					+ "c.n_cartella, a.skadp_mmgpls cod_medico, 'ADP' desc_flag,"
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, a.pr_data "
					+ "FROM skmmg_adp a, cartella c, medici m WHERE "
					+ "a.n_cartella=c.n_cartella AND "
					+ "a.skadp_mmgpls=m.mecodi AND "
					+ "(a.skadp_data_fine IS NULL OR "
					+ "a.skadp_data_fine>"
					+ formatDate(dbc, data) + ")";

			if (data != null && !data.equals(""))
				myselect += " AND a.skadp_data_inizio<="
						+ formatDate(dbc, data);
			if (mmg != null && !mmg.equals(""))
				myselect += " AND a.skadp_mmgpls='" + mmg + "'";
			if (oper != null && !oper.equals(""))
				myselect += " AND a.skadp_operatore='" + oper + "'";

			// 2ï¿½BIS ADR Ora vado a prendere tutti i record da skmmg_adr
			myselect += " UNION SELECT a.skadr_data_inizio data_inizio, a.skadr_data_fine data_fine, "
					+ "c.n_cartella, a.skadr_mmgpls cod_medico, 'ADR' desc_flag,"
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, a.pr_data "
					+ "FROM skmmg_adr a, cartella c, medici m WHERE "
					+ "a.n_cartella=c.n_cartella AND "
					+ "a.skadr_mmgpls=m.mecodi AND "
					+ "(a.skadr_data_fine IS NULL OR "
					+ "a.skadr_data_fine>"
					+ formatDate(dbc, data) + ")";

			if (data != null && !data.equals(""))
				myselect += " AND a.skadr_data_inizio<="
						+ formatDate(dbc, data);
			if (mmg != null && !mmg.equals(""))
				myselect += " AND a.skadr_mmgpls='" + mmg + "'";
			if (oper != null && !oper.equals(""))
				myselect += " AND a.skadr_operatore='" + oper + "'";
			// fine adr

			// 3ï¿½ Devo andare a prendere i record in intmmg che non hanno
			// inserito ancora
			// l'autorizzazione ADI
			myselect += " UNION SELECT to_date('','YYYY-MM-DD') data_inizio, to_date('','YYYY-MM-DD') data_fine, "
					+ "c.n_cartella, i.int_medico cod_medico, 'ADI' desc_flag,"
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, to_date('','YYYY-MM-DD') pr_data "
					+ "FROM intmmg i, cartella c, medici m, tabpipp t WHERE "
					+ "i.int_cartella=c.n_cartella AND "
					+ "i.int_medico=m.mecodi AND "
					+ "i.int_prestaz=t.pipp_codi AND "
					+ "i.int_tipo_pres=t.pipp_tipo AND "
					+ "i.int_tipo_pres='3' AND "
					+ "t.pipp_sottotipo='1' AND "
					+ "i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND "
					+ "i.int_data<="
					+ formatDate(dbc, data)
					+
					// 19/04/07 non deve tirar fuoir quelli con data_chiusura
					// inserita
					" AND c.data_chiusura IS NULL"
					+ " AND i.int_cartella "
					+ "NOT IN (SELECT DISTINCT n_cartella FROM skmmg_adi j WHERE "
					+ "(j.skadi_data_fine IS NULL "
					+ "OR j.skadi_data_fine>"
					+ formatDate(dbc, data)
					+ ") "
					+ "AND j.skadi_data_inzio<="
					+ formatDate(dbc, data) + ")";
			if (mmg != null && !mmg.equals(""))
				myselect += " AND i.int_medico='" + mmg + "'";
			// 03/01/2007 Su intmmg nn deve essere filtrato l'operatore
			// if(oper!=null && !oper.equals(""))
			// myselect+=" AND i.int_codoper='"+oper+"'";
			// 4ï¿½ Devo andare a prendere i record in intmmg che non hanno
			// inserito ancora
			// l'autorizzazione ADP
			myselect += " UNION SELECT to_date('','YYYY-MM-DD') data_inizio, to_date('','YYYY-MM-DD') data_fine, "
					+ "c.n_cartella, i.int_medico cod_medico, 'ADP' desc_flag,"
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, to_date('','YYYY-MM-DD') pr_data "
					+ "FROM intmmg i, cartella c, medici m, tabpipp t WHERE "
					+ "i.int_cartella=c.n_cartella AND "
					+ "i.int_medico=m.mecodi AND "
					+ "i.int_prestaz=t.pipp_codi AND "
					+ "i.int_tipo_pres=t.pipp_tipo AND "
					+ "i.int_tipo_pres='3' AND "
					+ "t.pipp_sottotipo='2' AND "
					+ "i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND "
					+ "i.int_data<="
					+ formatDate(dbc, data)
					+
					// 19/04/07 non deve tirar fuori quelli con data_chiusura
					// inserita
					" AND c.data_chiusura IS NULL"
					+ " AND i.int_cartella "
					+ "NOT IN (SELECT DISTINCT n_cartella FROM skmmg_adp j WHERE "
					+ "(j.skadp_data_fine IS NULL "
					+ "OR j.skadp_data_fine>"
					+ formatDate(dbc, data)
					+ ") "
					+ "AND j.skadp_data_inizio<=" + formatDate(dbc, data) + ")";
			if (mmg != null && !mmg.equals(""))
				myselect += " AND i.int_medico='" + mmg + "'";
			// if(oper!=null && !oper.equals(""))
			// myselect+=" AND i.int_codoper='"+oper+"'";

			// 4ï¿½BIS ADR Devo andare a prendere i record in intmmg che non hanno
			// inserito ancora
			// l'autorizzazione ADR
			myselect += " UNION SELECT to_date('','YYYY-MM-DD') data_inizio, to_date('','YYYY-MM-DD') data_fine, "
					+ "c.n_cartella, i.int_medico cod_medico, 'ADR' desc_flag,"
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, to_date('','YYYY-MM-DD') pr_data "
					+ "FROM intmmg i, cartella c, medici m, tabpipp t WHERE "
					+ "i.int_cartella=c.n_cartella AND "
					+ "i.int_medico=m.mecodi AND "
					+ "i.int_prestaz=t.pipp_codi AND "
					+ "i.int_tipo_pres=t.pipp_tipo AND "
					+ "i.int_tipo_pres='3' AND "
					+ "t.pipp_sottotipo='3' AND "
					+ "i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND "
					+ "i.int_data<="
					+ formatDate(dbc, data)
					+
					// 19/04/07 non deve tirar fuori quelli con data_chiusura
					// inserita
					" AND c.data_chiusura IS NULL"
					+ " AND i.int_cartella "
					+ "NOT IN (SELECT DISTINCT n_cartella FROM skmmg_adr j WHERE "
					+ "(j.skadr_data_fine IS NULL "
					+ "OR j.skadr_data_fine>"
					+ formatDate(dbc, data)
					+ ") "
					+ "AND j.skadr_data_inizio<=" + formatDate(dbc, data) + ")";
			if (mmg != null && !mmg.equals(""))
				myselect += " AND i.int_medico='" + mmg + "'";
			// fine ADR

			// 5ï¿½ ESISTONO INTERVENTI CHE NON RIENTRANO NEL PERIODO DI VALIDITA'
			// DELL'AUTORIZZAZIONE ADI MASSIMA
			myselect += " UNION SELECT to_date('','YYYY-MM-DD') data_inizio, "
					+ "to_date('','YYYY-MM-DD') data_fine, c.n_cartella, "
					+ "i.int_medico cod_medico, 'ADI' desc_flag, "
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, to_date('','YYYY-MM-DD') pr_data "
					+ "FROM intmmg i, cartella c, medici m, skmmg_adi a, tabpipp t "
					+ "WHERE i.int_cartella=c.n_cartella AND i.int_medico=m.mecodi "
					+ "AND i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND i.int_data<="
					+ formatDate(dbc, data)
					+ " AND i.int_prestaz=t.pipp_codi"
					+ " AND i.int_tipo_pres=t.pipp_tipo"
					+ " AND i.int_tipo_pres='3'"
					+ " AND t.pipp_sottotipo='1'"
					+ " AND i.int_cartella=a.n_cartella "
					+ " AND i.int_cartella NOT IN (SELECT(i.int_cartella)"
					+ " FROM intmmg i, cartella c, medici m, skmmg_adi a, tabpipp t"
					+ " WHERE i.int_cartella=c.n_cartella AND i.int_medico=m.mecodi"
					+ " AND i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND i.int_data<="
					+ formatDate(dbc, data)
					+ " AND i.int_prestaz=t.pipp_codi"
					+ " AND i.int_tipo_pres=t.pipp_tipo"
					+ " AND i.int_tipo_pres='3'"
					+ " AND t.pipp_sottotipo='1'"
					+ " AND i.int_cartella=a.n_cartella"
					+ " AND (i.int_data>a.skadi_data_inzio OR i.int_data<a.skadi_data_fine))";
			// " AND (i.int_data<a.skadi_data_inzio OR i.int_data>a.skadi_data_fine)";
			if (mmg != null && !mmg.equals(""))
				myselect += " AND i.int_medico='" + mmg + "'";
			// if(oper!=null && !oper.equals(""))
			// myselect+=" AND i.int_codoper='"+oper+"'";

			// 6ï¿½ ESISTONO INTERVENTI CHE NON RIENTRANO NEL PERIODO DI VALIDITA'
			// DELL'AUTORIZZAZIONE ADP
			myselect += " UNION SELECT to_date('','YYYY-MM-DD') data_inizio, "
					+ "to_date('','YYYY-MM-DD') data_fine, c.n_cartella, "
					+ "i.int_medico cod_medico, 'ADP' desc_flag, "
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, to_date('','YYYY-MM-DD') pr_data "
					+ "FROM intmmg i, cartella c, medici m, skmmg_adp a, tabpipp t "
					+ "WHERE i.int_cartella=c.n_cartella AND i.int_medico=m.mecodi "
					+ "AND i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND i.int_data<="
					+ formatDate(dbc, data)
					+ " AND i.int_prestaz=t.pipp_codi"
					+ " AND i.int_tipo_pres=t.pipp_tipo"
					+ " AND i.int_tipo_pres='3'"
					+ " AND t.pipp_sottotipo='2'"
					+ " AND i.int_cartella=a.n_cartella "
					+ " AND i.int_cartella NOT IN (SELECT(i.int_cartella)"
					+ " FROM intmmg i, cartella c, medici m, skmmg_adp a, tabpipp t"
					+ " WHERE i.int_cartella=c.n_cartella AND i.int_medico=m.mecodi"
					+ " AND i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND i.int_data<="
					+ formatDate(dbc, data)
					+ " AND i.int_prestaz=t.pipp_codi"
					+ " AND i.int_tipo_pres=t.pipp_tipo"
					+ " AND i.int_tipo_pres='3'"
					+ " AND t.pipp_sottotipo='2'"
					+ " AND i.int_cartella=a.n_cartella"
					+ " AND (i.int_data>a.skadp_data_inizio OR i.int_data<a.skadp_data_fine))";
			// " AND (i.int_data<a.skadp_data_inizio OR i.int_data>a.skadp_data_fine)";
			if (mmg != null && !mmg.equals(""))
				myselect += " AND i.int_medico='" + mmg + "'";
			// if(oper!=null && !oper.equals(""))
			// myselect+=" AND i.int_codoper='"+oper+"'";

			// 6ï¿½ BIS ESISTONO INTERVENTI CHE NON RIENTRANO NEL PERIODO DI
			// VALIDITA'
			// DELL'AUTORIZZAZIONE ADR
			myselect += " UNION SELECT to_date('','YYYY-MM-DD') data_inizio, "
					+ "to_date('','YYYY-MM-DD') data_fine, c.n_cartella, "
					+ "i.int_medico cod_medico, 'ADR' desc_flag, "
					+ "(nvl(trim(m.mecogn),'') || ' ' || nvl(trim(m.menome),'')) medico, "
					+ "(nvl(trim(c.cognome),'') || ' ' || nvl(trim(c.nome),'')) assistito, "
					+ "c.data_chiusura, c.motivo_chiusura, to_date('','YYYY-MM-DD') pr_data "
					+ "FROM intmmg i, cartella c, medici m, skmmg_adr a, tabpipp t "
					+ "WHERE i.int_cartella=c.n_cartella AND i.int_medico=m.mecodi "
					+ "AND i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND i.int_data<="
					+ formatDate(dbc, data)
					+ " AND i.int_prestaz=t.pipp_codi"
					+ " AND i.int_tipo_pres=t.pipp_tipo"
					+ " AND i.int_tipo_pres='3'"
					+ " AND t.pipp_sottotipo='3'"
					+ " AND i.int_cartella=a.n_cartella "
					+ " AND i.int_cartella NOT IN (SELECT(i.int_cartella)"
					+ " FROM intmmg i, cartella c, medici m, skmmg_adr  a, tabpipp t"
					+ " WHERE i.int_cartella=c.n_cartella AND i.int_medico=m.mecodi"
					+ " AND i.int_data>="
					+ formatDate(dbc, data2)
					+ " AND i.int_data<="
					+ formatDate(dbc, data)
					+ " AND i.int_prestaz=t.pipp_codi"
					+ " AND i.int_tipo_pres=t.pipp_tipo"
					+ " AND i.int_tipo_pres='3'"
					+ " AND t.pipp_sottotipo='3'"
					+ " AND i.int_cartella=a.n_cartella"
					+ " AND (i.int_data>a.skadr_data_inizio OR i.int_data<a.skadr_data_fine))";
			if (mmg != null && !mmg.equals(""))
				myselect += " AND i.int_medico='" + mmg + "'";
			// fine adr

			myselect += " ORDER BY 6,7";
			System.out.println("SELECT UNION:" + myselect);
			ISASCursor dbcur = dbc.startCursor(myselect.toString());
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 26/10/07: x JFrame in JCariContainer: legge il record con data MAX
	public ISASRecord getLastSkMmg(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null; // 07/08/07

		String cartella = (String) h.get("n_cartella");

		try {
			dbc = super.logIn(mylogin);   

//			String myselect = "SELECT k.* FROM skmmg k WHERE k.n_cartella="
//					+ cartella
//					+ " AND k.pr_data IN (SELECT MAX(s.pr_data) FROM skmmg s"
//					+ " WHERE s.n_cartella=k.n_cartella)";

//			String myselect = "SELECT k.skm_data_apertura  FROM skmedico k  WHERE k.n_cartella = " +
//					cartella + " AND k.skm_data_apertura IN ( SELECT MAX (s.skm_data_apertura)FROM skmedico s " +
//			"WHERE s.n_cartella = k.n_cartella )";
			
			
			String myselect = "SELECT k.skm_data_apertura  FROM skmedico k  WHERE k.n_cartella = " +
					cartella + " AND k.skm_data_apertura IN ( SELECT MAX (s.skm_data_apertura)FROM skmedico s " +
			"WHERE s.n_cartella = k.n_cartella )";
			
//			
			System.out.println("1-SkmmgEJB/getLastSkMmg: " + myselect);
//			 Leggo il record
			dbr = dbc.readRecord(myselect);

			// Si decodificano alcuni campi della maschera a video.
			if (dbr != null) {
				dbr.put("desc_oper", util.getDecode(dbc, "operatori", "codice",
						(String) util.getObjectField(dbr, "skmmg_operatore",
								'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));
				CaricaPatologie(dbc, dbr);
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		// 07/08/07 ---
		catch (ISASPermissionDeniedException e1) {
			System.out.println("SkmmgEJB.getLastSkMmg(): " + e1);
			throw new CariException(msgNoD, -2);
		}
		// 07/08/07 ---
		catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkmmgEJB.getLastSkMmg(): " + e);
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

	
	public Hashtable getLastSkMMG(myLogin mylogin, Hashtable h)
	throws SQLException, ISASPermissionDeniedException, CariException {
boolean done = false;
ISASConnection dbc = null;
ISASRecord dbr = null; // 07/08/07
Hashtable ret = null;

String cartella = (String) h.get("n_cartella");

try {
	dbc = super.logIn(mylogin);

//	String myselect = "SELECT k.* FROM skmmg k WHERE k.n_cartella="
//			+ cartella
//			+ " AND k.pr_data IN (SELECT MAX(s.pr_data) FROM skmmg s"
//			+ " WHERE s.n_cartella=k.n_cartella)";
//	String myselect = "SELECT k.skm_data_apertura as pr_data, k.* FROM skmedico k  WHERE k.n_cartella = " +
//			cartella + " AND k.skm_data_apertura IN ( SELECT MAX (s.skm_data_apertura)FROM skmedico s " +
//	"WHERE s.n_cartella = k.n_cartella )";
	
	
	SkInfEJB skinf = new SkInfEJB();
	dbr = skinf.getProgetto(dbc, cartella, null);
	
//	String myselect = "SELECT k.* FROM skmmg k WHERE k.n_cartella="
//			+ cartella
//			+ " AND k.pr_data IN (SELECT MAX(s.pr_data) FROM skmmg s"
//			+ " WHERE s.n_cartella=k.n_cartella)";


//	System.out.println("SkmmgEJB/getLastSkMmg: " + myselect);
//	// Leggo il record
//	dbr = dbc.readRecord(myselect);

	// Si decodificano alcuni campi della maschera a video.
	if (dbr != null) {
		dbr.put("desc_oper", util.getDecode(dbc, "operatori", "codice",
				(String) util.getObjectField(dbr, "skmmg_operatore",
						'S'),
				"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
				"oper_alias"));
		CaricaPatologie(dbc, dbr);
		decodificaSegnalatore(dbc,dbr);
		dbr.put("stato",new Integer(2));
		dbr.put("mmgpr_data",dbr.get("pr_data"));
		
		ret = dbr.getHashtable();
	}
	else {
		ret = new Hashtable();
		ret.put("n_cartella",cartella);
		ret.put("stato",new Integer(1));
		
	}
	
	dbc.close();
	super.close(dbc);
	done = true;
	return ret;
}
// 07/08/07 ---
catch (ISASPermissionDeniedException e1) {
	System.out.println("SkmmgEJB.getLastSkMmg(): " + e1);
	throw new CariException(msgNoD, -2);
}
// 07/08/07 ---
catch (Exception e) {
	e.printStackTrace();
	throw new SQLException("SkmmgEJB.getLastSkMmg(): " + e);
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

	
	
	
	// 26/10/07: caricamento della grid della frame "JFrameGridMMGSkmmg" ->
	// legge tutti i record tranne quello di data MAX
	public Vector query_loadGridMMGSkmmg(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException {
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");

		Vector vdbr = new Vector();

		try {
			dbc = super.logIn(mylogin);

			String myselect = "SELECT k.* FROM skmmg k"
					+ " WHERE k.n_cartella = "
					+ strNAssistito
					+ " AND k.pr_data NOT IN (SELECT MAX(s.pr_data) FROM skmmg s"
					+ " WHERE s.n_cartella = k.n_cartella)"
					+ " ORDER BY k.pr_data DESC";

			System.out.println("-->>query query_loadGridMMGSkmmg: " + myselect);

			ISASCursor dbcur = dbc.startCursor(myselect);
			vdbr = dbcur.getAllRecord();

			decodificaQueryInfo(dbc, vdbr);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"Errore eseguendo la query_loadGridMMGSkmmg()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 26/10/07
	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr)
			throws Exception {
		for (int i = 0; i < vdbr.size(); i++) {
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			dbr.put("desc_oper", util.getDecode(mydbc, "operatori", "codice",
					(String) util.getObjectField(dbr, "skmmg_operatore", 'S'),
					"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
					"oper_alias"));
			decodificaSegnalatore(mydbc, dbr);
			CaricaPatologie(mydbc, dbr);
		}
	}

	// 26/10/07
	private void decodificaSegnalatore(ISASConnection mydbc, ISASRecord dbr)
			throws Exception {
		String strCod = (String) dbr.get("skmmg_segnalatore");
		String strDesc = "";
		ISASRecord rec = null;

		if ((strCod != null) && (!strCod.trim().equals(""))) {
			String selS = "SELECT tab_descrizione descr_segnal"
					+ " FROM tab_voci" + " WHERE tab_cod = 'SEGNAL'"
					+ " AND tab_val = '" + strCod + "'";

			// System.out.println("-->>query decodificaSegnalatore: "+selS);
			rec = mydbc.readRecord(selS);
		}

		if ((rec != null) && (rec.get("descr_segnal") != null))
			strDesc = (String) rec.get("descr_segnal");

		dbr.put("descr_segnal", strDesc);
	}

	// 26/10/07
	public ISASRecord queryKey_solommg(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException, CariException {
		String punto = "queryKey_solommg ";
		LOG.info(punto + " dati che ricevo>>"+(h!=null ? h+"": " no dati "));
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = "";
		String data = "";
		ISASRecord dbr = null;

		try {
			dbc = super.logIn(mylogin);
			try {
				cartella = (String) h.get("n_cartella");
				data = (String) h.get("pr_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.queryKey_solommg()-->MANCANO LE CHIAVI PRIMARIE"
								+ ex);
			}

			String sel = "SELECT * FROM skmmg WHERE n_cartella=" + cartella
					+ " AND pr_data=" + formatDate(dbc, data);
			LOG.trace(punto + " query>"+ sel+"<<");
			// System.out.println("-->>queryKey_solommg: "+sel);
			dbr = dbc.readRecord(sel);
			if (dbr != null) {
				dbr.put("desc_oper", util.getDecode(dbc, "operatori", "codice",
						(String) util.getObjectField(dbr, "skmmg_operatore",
								'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));
				CaricaPatologie(dbc, dbr);
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (ISASPermissionDeniedException e1) {
			System.out.println("SkmmgEJB.queryKey_solommg(): " + e1);
			throw new CariException(msgNoD, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey_solommg()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 26/10/07
	public ISASRecord insert_solommg(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		String n_cartella = null;
		String data_apertura = null;
		ISASConnection dbc = null;

		try {
			n_cartella = (String) h.get("n_cartella");
			data_apertura = (String) h.get("pr_data");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"SkmmgEJB insert_solommg: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);

			// ctrl che non esista un altro record con la stessa data apertura
			if (esisteGiaRecord(dbc, n_cartella, data_apertura)) {
				String msg = "Impossibile inserire:\n scheda con data apertura gia' esistente";
				throw new CariException(msg, -2);
			}

			// ctrl dtApertura SkMMG >= dtApertura CARTELLA
			if (dtApeSkmmgLTDtApeCartella(dbc, n_cartella, data_apertura)) {
				String msg = "Impossibile inserire:\n la data apertura della scheda e' antecedente"
						+ "\n alla data di apertura dell'anagrafica";
				throw new CariException(msg, -2);
			}

			ISASRecord dbr = dbc.newRecord("skmmg");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM skmmg WHERE n_cartella="
					+ n_cartella + " AND pr_data="
					+ formatDate(dbc, data_apertura);
			dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				dbr.put("desc_oper", util.getDecode(dbc, "operatori", "codice",
						(String) util.getObjectField(dbr, "skmmg_operatore",
								'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));
				CaricaPatologie(dbc, dbr);
				
				// 05/05/11: solo x VCO
				h.put("operZonaConf", (String)dbr.get("skmmg_operatore"));
				if ((gestore_casi.isUbicazRegPiem(dbc, h)).booleanValue()) {			
					// -- Prendo i dati del caso per aggiornare l'eventuale presa in carico -- 
					h.put("n_cartella", n_cartella);
					h.put("pr_data", data_apertura);
					h.put("mmg_pr_data", data_apertura);
									
					Hashtable hp = getCasoFromProgetto(mylogin, h);
					if (hp!=null){
					h.put("pr_data",hp.get("pr_data"));
					h.put("id_caso",hp.get("id_caso"));
					h.put("skmmg_segnalatore",dbr.get("skmmg_segnalatore"));
					gestore_presacarico.aggRpPresaCarFromSkMMG(dbc,h);
					}
				}				
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - " + e1);
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

	// 26/10/07
	private boolean esisteGiaRecord(ISASConnection dbc, String strNAssistito,
			String strDtApeSkMmg) throws Exception {
		String myselect = "SELECT * FROM skmmg WHERE n_cartella="
				+ strNAssistito + " AND pr_data="
				+ formatDate(dbc, strDtApeSkMmg);

		ISASRecord dbr_1 = dbc.readRecord(myselect);
		return (dbr_1 != null);
	}

	// 26/10/07
	private boolean dtApeSkmmgLTDtApeCartella(ISASConnection dbc,
			String strNAssistito, String strDtApeSkMmg) throws Exception {
		String mySel = "SELECT *" + " FROM cartella" + " WHERE n_cartella = "
				+ strNAssistito + " AND data_apertura > "
				+ formatDate(dbc, strDtApeSkMmg);

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile
		else
			return true;
	}

	public ISASRecord update_solommg(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		String n_cartella = null;
		String data_apertura = null;

		try {
			data_apertura = (String) dbr.get("pr_data");
			n_cartella = (String) dbr.get("n_cartella");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"Skmmg update_solommg: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			
			// N.B.: per ora la dtSkMmg non ï¿½ modificabile, quindi i ctrl e gli
			// aggiornamenti non vengono eseguiti
// 28/11/11: da ora la dtSkMmg ï¿½ modificabile

			// ctrl variazione dtApertura scheda mmg
			String data_apertura_old = (String) dbr.get("pr_data_old");
			if ((data_apertura_old != null)
			&& (!data_apertura_old.trim().equals(data_apertura))) {
				String msg = "Impossibile aggiornare:\n la data apertura della scheda e' successiva"
						+ "\n alla data di apertura delle autorizzazioni";
				// ctrl dtApertura SkMMG <= dtAutorizz ADI
				if (dtApeSkMmgGTDtAutor(dbc, n_cartella, data_apertura,
						data_apertura_old, "adi")) {
					msg += " ADI";
					throw new CariException(msg, -2);
				}

				// ctrl dtApertura SkMMG <= dtAutorizz ADP
				if (dtApeSkMmgGTDtAutor(dbc, n_cartella, data_apertura,
						data_apertura_old, "adp")) {
					msg += " ADP";
					throw new CariException(msg, -2);
				}
				
				// 28/11/11: ctrl dtApertura SkMMG <= dtAutorizz ADR
				if (dtApeSkMmgGTDtAutor(dbc, n_cartella, data_apertura,
						data_apertura_old, "adr")) {
					msg += " ADR";
					throw new CariException(msg, -2);
				}

				// aggiornamento dtSkMmg sui record delle autoriz
				aggiornaDtSkMmg(dbc, n_cartella, data_apertura,
						data_apertura_old, "adi");
				aggiornaDtSkMmg(dbc, n_cartella, data_apertura,
						data_apertura_old, "adp");
				// 05/05/11
				aggiornaDtSkMmg(dbc, n_cartella, data_apertura,
						data_apertura_old, "adr");
			}

			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM skmmg WHERE n_cartella="
					+ n_cartella + " AND pr_data="
					+ formatDate(dbc, data_apertura);
			dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				dbr.put("desc_oper", util.getDecode(dbc, "operatori", "codice",
						(String) util.getObjectField(dbr, "skmmg_operatore",
								'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));
				CaricaPatologie(dbc, dbr);
				
				// 05/05/11: solo x VCO
				Hashtable h = new Hashtable();
				h.put("operZonaConf", (String)dbr.get("skmmg_operatore"));
				if ((gestore_casi.isUbicazRegPiem(dbc, h)).booleanValue()) {			
					// -- Prendo i dati del caso per aggiornare l'eventuale presa in carico -- 
					h.put("n_cartella", n_cartella);
					h.put("pr_data", data_apertura);
					h.put("mmg_pr_data", data_apertura);
									
					Hashtable hp = getCasoFromProgetto(mylogin, h);
					if (hp!=null){
					h.put("pr_data",hp.get("pr_data"));
					h.put("id_caso",hp.get("id_caso"));
					h.put("skmmg_segnalatore",dbr.get("skmmg_segnalatore"));
					gestore_presacarico.aggRpPresaCarFromSkMMG(dbc,h);
					}
				}				
			}
			
			
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una update() - " + e1);
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

	// 07/11/07
	private boolean dtApeSkMmgGTDtAutor(ISASConnection dbc,
			String strNAssistito, String strDtApeSkMmg_new,
			String strDtApeSkMmg_old, String tab) throws Exception {
		boolean trovato = false;

		String mySel = "SELECT *" + " FROM skmmg_" + tab
				+ " WHERE n_cartella = " + strNAssistito + " AND pr_data = "
				+ formatDate(dbc, strDtApeSkMmg_old) + " AND sk" + tab
				+ "_data < " + formatDate(dbc, strDtApeSkMmg_new);

		ISASCursor dbcur = dbc.startCursor(mySel);
		trovato = ((dbcur != null) && (dbcur.getDimension() > 0));
		dbcur.close();

		return trovato;
	}

	// 07/11/07
	private void aggiornaDtSkMmg(ISASConnection dbc, String strNAssistito,
			String strDtApeSkMmg_new, String strDtApeSkMmg_old, String tab)
			throws Exception {
		String nomeCampoDt = "sk" + tab + "_data";

		String mySel = "SELECT *" + " FROM skmmg_" + tab
				+ " WHERE n_cartella = " + strNAssistito + " AND pr_data = "
				+ formatDate(dbc, strDtApeSkMmg_old);

		ISASCursor dbcur = dbc.startCursor(mySel);
		if ((dbcur != null) && (dbcur.getDimension() > 0)) {
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String dtAutor = "" + dbr.get(nomeCampoDt);
				ISASRecord dbr_1 = dbc.readRecord(mySel + " AND " + nomeCampoDt
						+ " = " + formatDate(dbc, dtAutor));
				dbr_1.put("pr_data", strDtApeSkMmg_new);
				dbc.writeRecord(dbr_1);
			}
			dbcur.close();
		}
	}

	// 07/11/07: legge l'ultimo rec (per data scheda) e vi aggiunge gli isas
	// record per la griglia
	public ISASRecord queryKey_LastAdi(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		try {
			dbc = super.logIn(mylogin);

			String mysel = "SELECT * FROM skmmg_adi" + " WHERE n_cartella = "
					+ (String) h.get("n_cartella") + " AND pr_data = "
					+ formatDate(dbc, (String) h.get("pr_data"));

			String myselCur = mysel + " ORDER BY skadi_data DESC";

			System.out.println("SkmmgEJB: QueryKeyLast ADI - myselCur=["
					+ myselCur + "]");
			ISASCursor dbcur = dbc.startCursor(myselCur);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			for (int k = 0; k < vdbr.size(); k++) {
				ISASRecord dbr_2 = (ISASRecord) vdbr.elementAt(k);
				dbr_2
						.put(
								"desc_MMGADI",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) dbr_2
														.get("skadi_mmgpls"),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
			}

			if ((vdbr != null) && (vdbr.size() > 0)) {
				ISASRecord dbr_1 = (ISASRecord) vdbr.firstElement();
				String myselRec = mysel + " AND skadi_data = "
						+ formatDate(dbc, "" + dbr_1.get("skadi_data"));
				System.out.println("SkmmgEJB: QueryKeyLast ADI - myselRec=["
						+ myselRec + "]");

				dbr = dbc.readRecord(myselRec);
				if (dbr != null) {
					dbr
							.put(
									"desc_MMG",
									util
											.getDecode(
													dbc,
													"medici",
													"mecodi",
													(String) util
															.getObjectField(
																	dbr,
																	"skadi_mmgpls",
																	'S'),
													"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
													"mmg_alias"));

					dbr
							.put(
									"desc_operADI",
									util
											.getDecode(
													dbc,
													"operatori",
													"codice",
													(String) util
															.getObjectField(
																	dbr,
																	"skadi_operatore",
																	'S'),
													"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
													"oper_alias"));

					dbr.put("griglia_adi", (Vector) vdbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKeyLast()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 07/11/07
	public ISASRecord insert_Adi(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		String n_cartella = null;
		String nContatto = null;
//		String dtSkMmg = null;
		String dtAutorAdi = null;
		ISASConnection dbc = null;
		try {
			n_cartella = (String) h.get(Costanti.N_CARTELLA);
//			dtSkMmg = (String) h.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
			dtAutorAdi = (String) h.get(SKADI_DATA );
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"SKMMG insert_ADI: Errore: manca la chiave primaria");
		}
		try {
			dbc = super.logIn(mylogin);

			// ctrl che non esista un altro record con la stessa data apertura
			if (esisteGiaRecordAdi(dbc, n_cartella, nContatto, dtAutorAdi)) {
				String msg = "Impossibile inserire:\n Autorizzazione ADI con data proposta gia' esistente";
				throw new CariException(msg, -2);
			}

			ISASRecord dbr = insert_Adi(dbc, n_cartella, nContatto, dtAutorAdi, h);

			// 25/09/07
			if (dbr != null) {
				dbr
						.put(
								"desc_MMG",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbr, "skadi_mmgpls",
														'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				dbr.put("desc_operADI", util.getDecode(dbc, "operatori",
						"codice", (String) util.getObjectField(dbr,
								"skadi_operatore", 'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));

				dbr.put("griglia_adi",
						caricaGrigliaADI(dbc, dbr.getHashtable())); // 07/11/07
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - " + e1);
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

	public ISASRecord insert_Adi(ISASConnection dbc, String n_cartella, String nContatto, String dtAutorAdi,
			Hashtable h) throws Exception {
		String punto = ver + "insert_Adi ";   
		ISASRecord dbr = dbc.newRecord("skmmg_adi");
		Enumeration n = h.keys();
		while (n.hasMoreElements()) {
			String e = (String) n.nextElement();
			dbr.put(e, h.get(e));
		}

		dbc.writeRecord(dbr);
		String myselect = queryKeyAdi(n_cartella, nContatto, dtAutorAdi, dbc);
		LOG.debug(punto + " query>"+ myselect);
		
		dbr = dbc.readRecord(myselect);
		
		return dbr;
	}

	public String queryKeyAdi(String n_cartella, String nContatto, String dtAutorAdi, ISASConnection dbc) {
		String myselect = "SELECT * FROM skmmg_adi WHERE n_cartella="
				+ n_cartella + " AND n_contatto = " + nContatto
				+ " AND skadi_data = " + formatDate(dbc, dtAutorAdi);
		return myselect;
	}
	// 07/11/07
	private boolean esisteGiaRecordAdi(ISASConnection dbc,
			String strNAssistito, String nContatto, String strDtAutorAdi)
			throws Exception {
		String myselect = "SELECT * FROM skmmg_adi WHERE n_cartella="
				+ strNAssistito + " AND n_contatto = "+nContatto+ " AND skadi_data="
				+ formatDate(dbc, strDtAutorAdi);

		ISASRecord dbr_1 = dbc.readRecord(myselect);
		return (dbr_1 != null);
	}

	// 08/11/07: legge l'ultimo rec (per data scheda) e vi aggiunge gli isas
	// record per la griglia
	public ISASRecord queryKey_LastAdp(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		try {
			dbc = super.logIn(mylogin);

			String mysel = "SELECT * FROM skmmg_adp" + " WHERE n_cartella = "
					+ (String) h.get("n_cartella") + " AND pr_data = "
					+ formatDate(dbc, (String) h.get("pr_data"));

			String myselCur = mysel + " ORDER BY skadp_data DESC";

			System.out.println("SkmmgEJB: QueryKeyLast ADP - myselCur=["
					+ myselCur + "]");
			ISASCursor dbcur = dbc.startCursor(myselCur);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			for (int k = 0; k < vdbr.size(); k++) {
				ISASRecord dbr_2 = (ISASRecord) vdbr.elementAt(k);
				dbr_2
						.put(
								"desc_MMGADP",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) dbr_2
														.get("skadp_mmgpls"),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
			}

			if ((vdbr != null) && (vdbr.size() > 0)) {
				ISASRecord dbr_1 = (ISASRecord) vdbr.firstElement();
				String myselRec = mysel + " AND skadp_data = "
						+ formatDate(dbc, "" + dbr_1.get("skadp_data"));
				System.out.println("SkmmgEJB: QueryKeyLast ADP - myselRec=["
						+ myselRec + "]");

				dbr = dbc.readRecord(myselRec);
				if (dbr != null) {
					decodificaDatiADP(dbr, dbc);

					dbr.put("griglia_adp", (Vector) vdbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKeyLast()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 08/11/07
	public ISASRecord insert_Adp(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		String n_cartella = null;
//		String dtSkMmg = null;
		String nContatto = null;
		String dtAutorAdp = null;
		ISASConnection dbc = null;
		try {
			n_cartella = (String) h.get("n_cartella");
//			dtSkMmg = (String) h.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
			dtAutorAdp = (String) h.get("skadp_data");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"SKMMG insert_ADP: Errore: manca la chiave primaria");
		}
		try {
			dbc = super.logIn(mylogin);

			// ctrl che non esista un altro record con la stessa data apertura
			if (esisteGiaRecordAdp(dbc, n_cartella, nContatto, dtAutorAdp)) {
				String msg = "Impossibile inserire:\n Autorizzazione ADP con data proposta gia' esistente";
				throw new CariException(msg, -2);
			}

			ISASRecord dbr = dbc.newRecord("skmmg_adp");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			dbc.writeRecord(dbr);
			String myselect = queryKeyAdp(n_cartella, nContatto, dtAutorAdp, dbc);
			dbr = dbc.readRecord(myselect);
			System.out.println("select skmmg_adp insert: " + myselect);

			if (dbr != null) {
				decodificaDatiADP(dbr, dbc);

				dbr.put("griglia_adp",
						caricaGrigliaADP(dbc, dbr.getHashtable()));
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - " + e1);
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

	public String queryKeyAdp(String n_cartella, String nContatto, String dtAutorAdp, ISASConnection dbc) {
		String myselect = "SELECT * FROM skmmg_adp WHERE n_cartella="
				+ n_cartella + " AND n_contatto = " + nContatto
				+ " AND skadp_data = " + formatDate(dbc, dtAutorAdp);
		return myselect;
	}

	// 08/11/07
	private boolean esisteGiaRecordAdp(ISASConnection dbc,
			String strNAssistito, String nContatto, String strDtAutorAdp)
			throws Exception {
		String myselect = "SELECT * FROM skmmg_adp WHERE n_cartella="
				+ strNAssistito + " AND n_contatto =" + nContatto
				 + " AND skadp_data="
				+ formatDate(dbc, strDtAutorAdp);

		ISASRecord dbr_1 = dbc.readRecord(myselect);
		return (dbr_1 != null);
	}

	// gb 11/12/08: legge l'ultimo rec (per data scheda) e vi aggiunge gli isas
	// record per la griglia
	public ISASRecord queryKey_LastAdr(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		try {
			dbc = super.logIn(mylogin);

			String mysel = "SELECT * FROM skmmg_adr" + " WHERE n_cartella = "
					+ (String) h.get("n_cartella") + " AND pr_data = "
					+ formatDate(dbc, (String) h.get("pr_data"));

			String myselCur = mysel + " ORDER BY skadr_data DESC";

			System.out.println("SkmmgEJB: QueryKeyLast ADR - myselCur=["
					+ myselCur + "]");
			ISASCursor dbcur = dbc.startCursor(myselCur);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			for (int k = 0; k < vdbr.size(); k++) {
				ISASRecord dbr_2 = (ISASRecord) vdbr.elementAt(k);
				dbr_2
						.put(
								"desc_MMGADR",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) dbr_2
														.get("skadr_mmgpls"),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
			}

			if ((vdbr != null) && (vdbr.size() > 0)) {
				ISASRecord dbr_1 = (ISASRecord) vdbr.firstElement();
				String myselRec = mysel + " AND skadr_data = "
						+ formatDate(dbc, "" + dbr_1.get("skadr_data"));
				System.out.println("SkmmgEJB: QueryKeyLast ADR - myselRec=["
						+ myselRec + "]");

				dbr = dbc.readRecord(myselRec);
				if (dbr != null) {
					dbr
							.put(
									"desc_MMGADR",
									util
											.getDecode(
													dbc,
													"medici",
													"mecodi",
													(String) util
															.getObjectField(
																	dbr,
																	"skadr_mmgpls",
																	'S'),
													"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
													"mmg_alias"));

					dbr
							.put(
									"desc_operADR",
									util
											.getDecode(
													dbc,
													"operatori",
													"codice",
													(String) util
															.getObjectField(
																	dbr,
																	"skadr_operatore",
																	'S'),
													"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
													"oper_alias"));

					dbr.put("des_istituto", util.getDecode(dbc, "istituti",
							"ist_codice", (String) util.getObjectField(dbr,
									"skadr_istituto", 'S'),
							"nvl(trim(st_nome),'')", "isti_alias"));

					dbr.put("griglia_adr", (Vector) vdbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una SkmmgEJB.queryKeyLast()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// gb 11.12.08
	public ISASRecord queryADR(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException, CariException {
		// richiamata quando vado in apri di un dettaglio ADI
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbrec = null;
		String cartella = null;
		String dataSK = null;
		String data = null;
		String messaggio = "";
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
			try {
				cartella = (String) h.get("n_cartella");
				dataSK = (String) h.get("pr_data");
				data = (String) h.get("skadr_data");
			} catch (Exception ex) {
				throw new SQLException(
						"SkmmgEJB.queryADR()-->MANCANO LE CHIAVI PRIMARIE" + ex);
			}

			String myselect = "SELECT * FROM skmmg_adr WHERE ";
			String mysel = "";
			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
					cartella);
			mysel = su.addWhere(mysel, su.REL_AND, "skadr_data", su.OP_EQ_NUM,
					formatDate(dbc, data));
			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
					formatDate(dbc, dataSK));
			myselect = myselect + mysel;

			dbrec = dbc.readRecord(myselect);
			if (dbrec != null) {
				dbrec
						.put(
								"desc_MMGADR",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbrec, "skadr_mmgpls",
														'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				dbrec.put("desc_operADR", util.getDecode(dbc, "operatori",
						"codice", (String) util.getObjectField(dbrec,
								"skadr_operatore", 'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));
				dbrec.put("des_istituto", util.getDecode(dbc, "istituti",
						"ist_codice", (String) util.getObjectField(dbrec,
								"skadr_istituto", 'S'),
						"nvl(trim(st_nome),'')", "isti_alias"));

				dbrec.put("griglia_adr", caricaGrigliaADR(dbc, dbrec
						.getHashtable())); // 08/11/07
			} else
				messaggio = "Operazione fallita.\n"
						+ "I dati richiesti sono stati rimossi da un altro utente";
			if (!messaggio.equals(""))
				throw new CariException(messaggio);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbrec;
		} catch (CariException ce) {
			ce.setISASRecord(dbrec);
			throw ce;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("queryADR: eccezione permesso negato " + e);
			return null;
		} catch (Exception e) {
			throw new SQLException("Errore eseguendo una SkmmgEJB.queryADR() "
					+ e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// gb 11.12.08
	public ISASRecord insert_Adr(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		String n_cartella = null;
//		String dtSkMmg = null;
		String nContatto = null;
		String dtAutorAdr = null;
		ISASConnection dbc = null;
		try {
			n_cartella = (String) h.get("n_cartella");
//			dtSkMmg = (String) h.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO);
			dtAutorAdr = (String) h.get("skadr_data");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"SKMMG insert_ADR: Errore: manca la chiave primaria");
		}
		try {
			dbc = super.logIn(mylogin);

			// ctrl che non esista un altro record con la stessa data apertura
			if (esisteGiaRecordAdr(dbc, n_cartella, nContatto, dtAutorAdr)) {
				String msg = "Impossibile inserire:\n Autorizzazione ADR con data proposta gia' esistente";
				throw new CariException(msg, -2);
			}

			ISASRecord dbr = dbc.newRecord("skmmg_adr");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			dbc.writeRecord(dbr);
			String myselect = queryKeyAdr(n_cartella, nContatto, dtAutorAdr, dbc);
			dbr = dbc.readRecord(myselect);
			System.out.println("select skmmg_adr insert: " + myselect);

			if (dbr != null) {
				dbr
						.put(
								"desc_MMGADR",
								util
										.getDecode(
												dbc,
												"medici",
												"mecodi",
												(String) util.getObjectField(
														dbr, "skadr_mmgpls",
														'S'),
												"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
												"mmg_alias"));
				dbr.put("desc_operADR", util.getDecode(dbc, "operatori",
						"codice", (String) util.getObjectField(dbr,
								"skadr_operatore", 'S'),
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))",
						"oper_alias"));
				dbr.put("des_istituto", util.getDecode(dbc, "istituti",
						"ist_codice", (String) util.getObjectField(dbr,
								"skadr_istituto", 'S'),
						"nvl(trim(st_nome),'')", "isti_alias"));

				dbr.put("griglia_adr",
						caricaGrigliaADR(dbc, dbr.getHashtable()));
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("Errore eseguendo una insert_Adr() - " + e);
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert_Adr() - " + e1);
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

	public String queryKeyAdr(String n_cartella, String nContatto, String dtAutorAdr, ISASConnection dbc) {
		String myselect = "SELECT * FROM skmmg_adr WHERE n_cartella="
				+ n_cartella + " AND n_contatto = " + nContatto
				+ " AND skadr_data = " + formatDate(dbc, dtAutorAdr);
		return myselect;
	}

	// gb 11.12.08
	private boolean esisteGiaRecordAdr(ISASConnection dbc,
			String strNAssistito, String nContatto, String strDtAutorAdr)
			throws Exception {
		String myselect = "SELECT * FROM skmmg_adr WHERE n_cartella="
				+ strNAssistito + " AND n_contatto ="+ nContatto+ " AND skadr_data="
				+ formatDate(dbc, strDtAutorAdr);

		ISASRecord dbr_1 = dbc.readRecord(myselect);
		return (dbr_1 != null);
	}

	// gb 11.12.08
	public ISASRecord updateADR(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		String cartella = null;
//		String dataSK = null;
		String nContatto = null;
		String data = null;
		ServerUtility su = new ServerUtility();
		try {
			cartella = (String) dbr.get("n_cartella");
//			dataSK = (String) dbr.get("pr_data");
			nContatto = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CONTATTO);
			data = (String) dbr.get("skadr_data");
		} catch (Exception ex) {
			throw new SQLException(
					"SkmmgEJB.updateADR()-->MANCANO LE CHIAVI PRIMARIE" + ex);
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.writeRecord(dbr);
			String query = queryKeyAdr(cartella, nContatto, data, dbc);
//			String myselect = "SELECT * FROM skmmg_adr WHERE ";
//			String mysel = "";
//			mysel = su.addWhere(mysel, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
//					cartella);
//			mysel = su.addWhere(mysel, su.REL_AND, "pr_data", su.OP_EQ_NUM,
//					formatDate(dbc, dataSK));
//			mysel = su.addWhere(mysel, su.REL_AND, "skadr_data", su.OP_EQ_NUM,
//					formatDate(dbc, data));
//			myselect = myselect + mysel;
			dbr = dbc.readRecord(query);
			// 08/11/07 -----
			if (dbr != null) {
				decodificaDatiADR(dbr, dbc);
				dbr.put("griglia_adr", caricaGrigliaADR(dbc, dbr.getHashtable()));
			}

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			System.out.println("SkmmgEJB.ISAS()-->"
					+ dbr.getHashtable().toString());
			ce.setISASRecord(dbr);
			throw ce;
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.updateADR()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADP().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.updateADR()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADP().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.updateADR()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.updateADP().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.updateADR()-->" + e);
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

	private void decodificaDatiADR(ISASRecord dbr, ISASConnection dbc)
			throws ISASMisuseException, Exception {
		dbr.put("desc_MMGADR",util.getDecode(dbc,"medici","mecodi",(String) util.getObjectField(dbr, "skadr_mmgpls",'S'),
										"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))", "mmg_alias"));
		dbr.put("desc_operADR", util.getDecode(dbc, "operatori","codice", (String) util.getObjectField(dbr, "skadr_operatore", 'S'),
				"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","oper_alias"));
		// 08/11/07 ---
		dbr.put("des_istituto", util.getDecode(dbc, "istituti", "ist_codice", (String) util.getObjectField(dbr,
						"skadr_istituto", 'S'), "nvl(trim(st_nome),'')", "isti_alias"));
	}

	// gb 11.12.08
	public void deleteADR(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		String punto = ver + "deleteADR ";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.deleteRecord(dbr);
			dbc.commitTransaction();
		} catch (DBRecordChangedException e) {
			System.out.println("SkmmgEJB.deleteADR()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADR().rollback() - "
								+ e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkmmgEJB.deleteADR()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADR().rollback()- "
								+ e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SkmmgEJB.deleteADR()-->" + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException(
						"Errore eseguendo una SkmmgEJB.deleteADR().rollback()- "
								+ e);
			}
			throw new SQLException("SkmmgEJB.deleteADR()-->" + e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

	private int convNumDBToInt(String nomeCampo, ISASRecord mydbr)
			throws Exception {
		int numero = 0;
		Object numDB = (Object) mydbr.get(nomeCampo);
		if (numDB != null) {
			if (numDB.getClass().getName().endsWith("Double"))
				numero = ((Double) mydbr.get(nomeCampo)).intValue();
			else if (numDB.getClass().getName().endsWith("Integer"))
				numero = ((Integer) mydbr.get(nomeCampo)).intValue();
		}

		return numero;
	}// END convNumDBToInt
	// funzione solo per FLUSSI VCO
	public Hashtable getProgetto(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException {
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		boolean done = false;
		String select = null;
		Integer id_caso = null;
		Hashtable ret = null;
		Hashtable h_caso = new Hashtable();
		try {
			dbc = super.logIn(mylogin);
			
			pr_data = h.get("pr_data") != null ? (String) h.get("pr_data") : "";
			n_cartella = h.get("n_cartella") != null ? (String) h.get("n_cartella") : "";
			if (pr_data.equals("") || n_cartella.equals("")) {
				System.out.println("Errore: Mancano dati per select su Progetto");
				
				dbc.close();
				super.close(dbc);
				done = true;
				return null;
			} else {
				select = "select * from progetto where n_cartella = "+ n_cartella
						+ " and (("+ formatDate(dbc, pr_data)
						+ " between pr_data and pr_data_chiusura and pr_data_chiusura is not null) or ("
						+ formatDate(dbc, pr_data)+ " >= pr_data and pr_data_chiusura is null))";
						
				dbr = dbc.readRecord(select);
				System.out.println(" GetProgetto: SELECT 1: "+ select);
				if (dbr != null) // se il progetto esiste prendo id_caso attraverso pr_data e n_cartella
				{
					System.out.println(" GetProgetto: PROGETTO 1 ESISTENTE");

					ret = dbr.getHashtable();
	
					dbc.close();
					super.close(dbc);
					done = true;
					return ret;
				} else // cerco un progetto aperto dopo
				{
					// 06/05/11 ------------
					select = "SELECT * FROM progetto WHERE n_cartella = "+n_cartella
						+ " AND pr_data > " + formatDate(dbc, pr_data);
				
					dbr = dbc.readRecord(select);
					System.out.println(" GetCasoFromProgetto: Creazione Progetto :SELECT 2: "+ select);
					if (dbr != null) // se il progetto esiste prendo id_caso attraverso pr_data e n_cartella
					{
						System.out.println(" GetProgetto: PROGETTO 2 ESISTENTE");

						ret = dbr.getHashtable();
						
						dbc.close();
						super.close(dbc);
						done = true;
						return ret;
					}
				}
				dbc.close();
				super.close(dbc);
				done = true;
				return ret;
			}
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("Errore eseguendo una getCasoFromProgetto() - "
					+ e);
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una getCasoFromProgetto() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.rollbackTransaction();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}
					
					
					
					
					
	// funzione solo per FLUSSI VCO
	public Hashtable getCasoFromProgetto(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException {
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		boolean done = false;
		String select = null;
		Integer id_caso = null;
		Hashtable ret = null;
		Hashtable h_caso = new Hashtable();
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			pr_data = h.get("pr_data") != null ? (String) h.get("pr_data") : "";
			n_cartella = h.get("n_cartella") != null ? (String) h.get("n_cartella") : "";
			if (pr_data.equals("") || n_cartella.equals("")) {
				System.out.println("Errore: Mancano dati per select su Progetto");
				dbc.commitTransaction();
				dbc.close();
				super.close(dbc);
				done = true;
				return null;
			} else {
				select = "select * from progetto where n_cartella = "+ n_cartella
						+ " and (("+ formatDate(dbc, pr_data)
						+ " between pr_data and pr_data_chiusura and pr_data_chiusura is not null) or ("
						+ formatDate(dbc, pr_data)+ " >= pr_data and pr_data_chiusura is null))";
						
				dbr = dbc.readRecord(select);
				System.out.println(" GetCasoFromProgetto: Creazione Progetto :SELECT 1: "+ select);
				if (dbr != null) // se il progetto esiste prendo id_caso attraverso pr_data e n_cartella
				{
					System.out.println(" GetCasoFromProgetto: PROGETTO 1 ESISTENTE");

					ret = creaCasoFromProgetto(dbc, h_caso, dbr, pr_data);
					if (ret!=null)
						{
						String myselect = "select tipouvg from rp_presacarico where id_caso = "+ret.get("id_caso").toString();
						System.out.println("selectTipouvg:"+myselect);
						ISASRecord dbr1 = dbc.readRecord(myselect);	
						if (dbr1!=null)
						{
						ret.put("tipouvg",dbr1.get("tipouvg").toString());
						}
						}
					dbc.commitTransaction();
					dbc.close();
					super.close(dbc);
					done = true;
					return ret;
				} else // cerco un progetto aperto dopo
				{
					// 06/05/11 ------------
					select = "SELECT * FROM progetto WHERE n_cartella = "+n_cartella
						+ " AND pr_data > " + formatDate(dbc, pr_data);
				
					dbr = dbc.readRecord(select);
					System.out.println(" GetCasoFromProgetto: Creazione Progetto :SELECT 2: "+ select);
					if (dbr != null) // se il progetto esiste prendo id_caso attraverso pr_data e n_cartella
					{
						System.out.println(" GetCasoFromProgetto: PROGETTO 2 ESISTENTE");

						ret = creaCasoFromProgetto(dbc, h_caso, dbr, pr_data);
						if (ret!=null)
						{
						String myselect = "select tipouvg from rp_presacarico where id_caso = "+ret.get("id_caso").toString();
						System.out.println("selectTipouvg:"+myselect);
						ISASRecord dbr1 = dbc.readRecord(myselect);	
						if (dbr1!=null)
						{
						ret.put("tipouvg",dbr1.get("tipouvg").toString());
						}
						}
						dbc.commitTransaction();
						dbc.close();
						super.close(dbc);
						done = true;
						return ret;
					} else // creo il progetto
					{
					// 06/05/11 ------------
				
						System.out.println(" GetCasoFromProgetto: Creazione Progetto : ");
						ISASRecord dbr_new = dbc.newRecord("progetto");
						System.out.println(" GetCasoFromProgetto: Creazione Progetto :1 "+ dbr_new.getHashtable().toString());
						dbr_new.put("n_cartella", n_cartella);
						System.out.println(" GetCasoFromProgetto: Creazione Progetto :2 "+ n_cartella);
						dbr_new.put("pr_data", formatDBdate(pr_data));
						System.out.println(" GetCasoFromProgetto: Creazione Progetto :3 "+ pr_data);
						dbc.writeRecord(dbr_new);
						System.out.println(" GetCasoFromProgetto: Creazione Progetto :WRITE riuscita ");
					
						// creo il caso
						ret = creaCasoFromProgetto(dbc, h_caso, dbr_new, pr_data);
						if (ret!=null)
						{
						String myselect = "select tipouvg from rp_presacarico where id_caso = "+ret.get("id_caso").toString();
						System.out.println("selectTipouvg:"+myselect);
						ISASRecord dbr1 = dbc.readRecord(myselect);	
						if (dbr1!=null)
						{
						ret.put("tipouvg",dbr1.get("tipouvg").toString());
						}
						}
						dbc.commitTransaction();
						dbc.close();
						super.close(dbc);
						done = true;
						return ret;
					}
				}
			}
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("Errore eseguendo una getCasoFromProgetto() - "
					+ e);
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una getCasoFromProgetto() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.rollbackTransaction();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	private Hashtable creaCasoFromProgetto(ISASConnection dbc, Hashtable h_caso, ISASRecord dbr, String pr_data) throws Exception
	{
		Hashtable ret = null;
		ISASRecord db1,db2 = null;
		
		h_caso.put("pr_data", "" + dbr.get("pr_data"));
		h_caso.put("n_cartella", "" + dbr.get("n_cartella"));
		h_caso.put("origine", "" + GestCasi.CASO_SAN);
		System.out.println("SkmmgEJB.creaCasoFromProgetto(): GESTORE_CASI HASH : "+ h_caso.toString());

		// elisa b
		db1 = gestore_casi.getCasoRifOrigine(dbc, h_caso);
		db2 = gestore_casi.getCasoRif(dbc, h_caso);
		if ( db1 != null && !((Boolean)gestore_casi.isStatoConcluso(db1)).booleanValue())
			ret = db1.getHashtable();
		//simone 
		//DEVO CONTROLLARE che se ci sia o meno un caso complesso e in tal caso ritornare quello,
		// non ï¿½ possibile creare un caso san se ï¿½ presente un complesso
		
		
		else if (db2 != null && !((Boolean)gestore_casi.isStatoConcluso(db2)).booleanValue())
		ret = db2.getHashtable();
		else // creo il caso
		{
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso : ");
			h_caso.put("pr_data", "" + dbr.get("pr_data"));
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso :1 "+ h_caso.toString());
			h_caso.put("n_cartella", "" + dbr.get("n_cartella"));
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso :2 "+ h_caso.toString());
			h_caso.put("origine", "" + GestCasi.CASO_SAN);
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso :3 "+ h_caso.toString());
			h_caso.put("dt_presa_carico", formatDBdate(pr_data));
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso :3b "+ h_caso.toString());

			Integer id_caso = gestore_casi.apriCasoSan(dbc, h_caso);
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso :WRITE riuscita ");

			h_caso.put("id_caso", id_caso);
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso :4 "+ h_caso.toString());
				
			if (gestore_casi.getCaso(dbc, h_caso)!=null){
			ret = gestore_casi.getCaso(dbc, h_caso).getHashtable();
			System.out.println("SkmmgEJB.creaCasoFromProgetto(): Creazione Caso : Hash caso "+ ret.toString());
			}
		}
			
		
		return ret;
	}
	
	public Boolean esisteADIADR(myLogin mylogin, Hashtable h) throws SQLException
	{
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		String skadi_data = null;
		String skadr_data =null;
		String myselADI = null;
		String myselADR = null;
		ISASRecord dbr_adr = null;
		ISASRecord dbr_adi = null;
		boolean done = false;
		Boolean ret = new Boolean(false);
		try {
			n_cartella = h.get("n_cartella").toString();
			pr_data = h.get("pr_data").toString();
			skadi_data = h.get("skadi_data").toString();
			skadr_data = h.get("skadr_data").toString();
			dbc = super.logIn(mylogin);
			myselADI="select * from skmmg_adi where n_cartella = "+n_cartella
			+" and pr_data = "+formatDate(dbc, pr_data);
// 04/05/11	+" and skadi_data = "+formatDate(dbc, skadi_data);
			myselADR="select * from skmmg_adr where n_cartella = "+n_cartella
			+" and pr_data = "+formatDate(dbc, pr_data);
// 04/05/11	+" and skadr_data = "+formatDate(dbc, skadr_data);	

			 dbr_adi = dbc.readRecord(myselADI);
			 dbr_adr = dbc.readRecord(myselADR);
			
			if (dbr_adi!=null || dbr_adr!=null) ret = new Boolean(true);
			done = true;
			dbc.close();
			super.close(dbc);
			return ret;
			} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una getCasoFromProgetto() - " + e1);
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
		
	public Boolean esisteADPADR(myLogin mylogin, Hashtable h) throws SQLException
	{
	ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		String skadp_data = null;
		String skadr_data =null;
		String myselADP = null;
		String myselADR = null;
		ISASRecord dbr_adr = null;
		ISASRecord dbr_adp = null;
		boolean done = false;
		Boolean ret = new Boolean(false);
		try {
			n_cartella = h.get("n_cartella").toString();
			pr_data = h.get("pr_data").toString();
			skadp_data = h.get("skadp_data").toString();
			skadr_data = h.get("skadr_data").toString();
			dbc = super.logIn(mylogin);
			myselADP="select * from skmmg_adp where n_cartella = "+n_cartella
			+" and pr_data = "+formatDate(dbc, pr_data);
// 04/05/11	+" and skadp_data = "+formatDate(dbc, skadp_data);
			myselADR="select * from skmmg_adr where n_cartella = "+n_cartella
			+" and pr_data = "+formatDate(dbc, pr_data);
// 04/05/11	+" and skadr_data = "+formatDate(dbc, skadr_data);	

			 dbr_adp = dbc.readRecord(myselADP);
			 dbr_adr = dbc.readRecord(myselADR);
			
			if (dbr_adp!=null || dbr_adr!=null) ret = new Boolean(true);
			done = true;
			dbc.close();
			super.close(dbc);
			return ret;
			} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una getCasoFromProgetto() - " + e1);
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
		
	public Boolean esisteADPADI(myLogin mylogin, Hashtable h) throws SQLException
	{
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		String skadi_data = null;
		String skadp_data =null;
		String myselADI = null;
		String myselADP = null;
		ISASRecord dbr_adp = null;
		ISASRecord dbr_adi = null;
		boolean done = false;
		Boolean ret = new Boolean(false);
		try {
			n_cartella = h.get("n_cartella").toString();
			pr_data = h.get("pr_data").toString();
			skadi_data = h.get("skadi_data").toString();
			skadp_data = h.get("skadp_data").toString();
			dbc = super.logIn(mylogin);
			myselADI="select * from skmmg_adi where n_cartella = "+n_cartella
			+" and pr_data = "+formatDate(dbc, pr_data);
// 04/05/11	+" and skadi_data = "+formatDate(dbc, skadi_data);
			myselADP="select * from skmmg_adp where n_cartella = "+n_cartella
			+" and pr_data = "+formatDate(dbc, pr_data);
// 04/05/11	+" and skadp_data = "+formatDate(dbc, skadp_data);	

			 dbr_adi = dbc.readRecord(myselADI);
			 dbr_adp = dbc.readRecord(myselADP);
			
			if (dbr_adi!=null || dbr_adp!=null) ret = new Boolean(true);
			done = true;
			dbc.close();
			super.close(dbc);
			return ret;
			} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una getCasoFromProgetto() - " + e1);
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
	
	
	private String formatDBdate(String data) {
		if (!data.equals("") && data.indexOf("/") != -1)
			data = data.substring(6, 10) + "-" + data.substring(3, 5) + "-"
					+ data.substring(0, 2);
		return data;
	}
	
	/**
	 * elisa b 02/03/11
	 * @param mylogin
	 * @param h0
	 * @return
	 */
	public String getTipoSpecifica(myLogin mylogin, Hashtable h){
		boolean done = false;
		ISASConnection dbc = null;
		String  specifica = "";

		try {
			dbc = super.logIn(mylogin);

			String n_cartella = h.get("n_cartella").toString();

			String myselect = "SELECT r.pr_specifica FROM rv_puauvm r" +
							" WHERE r.n_cartella = " + n_cartella +
							" AND r.pr_data IN (" +
								" SELECT MAX(r1.pr_data) FROM rv_puauvm r1" +
								" WHERE r1.n_cartella = r.n_cartella" + ")" +
							 " AND r.pr_progr IN (" +
								" SELECT MAX(r1.pr_progr) FROM rv_puauvm r1" +
								" WHERE r1.n_cartella = r.n_cartella" +
								" AND r1.pr_data = r.pr_data" + ")";

			System.out.println("SkmmgEJB: getTipoSpecifica - myselect= " + myselect);

			ISASRecord dbr = dbc.readRecord(myselect);
			if((dbr != null) && (dbr.get("pr_specifica") != null))
				specifica = dbr.get("pr_specifica").toString();
			
			dbc.close();	
			super.close(dbc);
			done = true;

			return specifica;
			
		} catch(Exception e1){
			e1.printStackTrace();
			return null;
		} finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	
	
	/**
	 * elisa b 30/05/11: controlla che non ci siano altre autorizzazioni aperte o che 
	 * la data della nuova autorizzazione non si sovrapponga alle date 
	 * dell'autorizzazione precedente
	 * @param mylogin
	 * @param h
	 * @return
	 * @throws SQLException
	 */
	public Boolean checkAutorizzazione(myLogin mylogin, Hashtable h) throws SQLException{
	ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		String data_inizio = null;
		String data_fine = null;
		String tipo =null;
		String myselADP = null;
		String myselADR = null;
		String myselADI = null;
		ISASRecord dbr_adr = null;
		ISASRecord dbr_adp = null;
		ISASRecord dbr_adi = null;
		boolean done = false;
		Boolean ret = new Boolean(true);
		try {
			n_cartella = h.get("n_cartella").toString();
			pr_data = h.get("pr_data").toString();
			data_inizio = h.get("data_inizio").toString();
			tipo = h.get("tipo").toString();			
			
			dbc = super.logIn(mylogin);
			
			if(!tipo.equalsIgnoreCase("ADI")){			
				myselADI = selEsistenzaAutorizzazione(dbc, "adi", n_cartella, pr_data, data_inizio);
				System.out.println("A checkAutorizzazione " + myselADI);
				dbr_adi = dbc.readRecord(myselADI);

				if(dbr_adi != null)
					 return new Boolean(false);
				
			}
			if(!tipo.equalsIgnoreCase("ADP")){
				myselADP = selEsistenzaAutorizzazione(dbc, "adp", n_cartella, pr_data, data_inizio);
				System.out.println("B checkAutorizzazione " + myselADP);
				dbr_adp = dbc.readRecord(myselADP);

				if(dbr_adp != null) 				
					 return new Boolean(false);
				
				 
			}
			if(!tipo.equalsIgnoreCase("ADR")){
				myselADR = selEsistenzaAutorizzazione(dbc, "adr", n_cartella, pr_data, data_inizio);
				System.out.println("C checkAutorizzazione " + myselADR);
				dbr_adr = dbc.readRecord(myselADR);

				if(dbr_adr != null)
					 return new Boolean(false);
			}

			done = true;
			dbc.close();
			super.close(dbc);
			return ret;
		} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(
					"Errore eseguendo una checkAutorizzazione() - " + e1);
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

	
	
		public Boolean checkNumAdp(myLogin mylogin, Hashtable h) throws SQLException
	{
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		String n_cartella = null;
		String skadp_data =null;
		String skadp_freq =null;
		String myselADP = null;
		ISASRecord dbr_adp = null;
		
		boolean done = false;
		Boolean ret = new Boolean(true);
		try {
			n_cartella = h.get("n_cartella").toString();
			skadp_data = h.get("skadp_data").toString();
			skadp_freq = h.get("skadp_freq").toString();
			
			int soglia = 0;
			if (skadp_freq != null)
			{
			if (skadp_freq.equals("1"))
				soglia = 13;
				else if (skadp_freq.equals("2"))
				soglia = 26;
				else if (skadp_freq.equals("3"))
				soglia = 52;
			}
			dbc = super.logIn(mylogin);
			
			myselADP="select count(*) num from skmmg_adp where n_cartella = "+n_cartella
			+" and skadp_data between "+formatDate(dbc, "01-01-"+skadp_data.substring(0,4))+" and " +formatDate(dbc, "31-12-"+skadp_data.substring(0,4))
			+" and skadp_freq = "+skadp_freq;

			System.out.println("NUMERO ADP: "+myselADP);
			
			 dbr_adp = dbc.readRecord(myselADP);
			
			if (dbr_adp!=null) 
			{
			int num = util.getIntField(dbr_adp,"num");
						if (num > soglia)
						ret = new Boolean(false);
			}
			done = true;
			dbc.close();
			super.close(dbc);
			return ret;
			} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException(
					"Errore eseguendo una getCasoFromProgetto() - " + e1);
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
		
	/**
	 * elisa b 07/06/11: controlla l'esistenza di un'autorizzazione ADI attiva
	 * alla data di riferimento
	 * @param mylogin
	 * @param h
	 * @return
	 * @throws SQLException
	 */
	public Boolean esisteADI(myLogin mylogin, Hashtable h) throws SQLException{
	ISASConnection dbc = null;
		ISASRecord dbr = null;
		String pr_data = null;
		String n_cartella = null;
		String data_inizio = null;
		String data_fine = null;
		String myselADI = null;

		ISASRecord dbr_adi = null;
		boolean done = false;
		Boolean ret = new Boolean(false);
		try {
			n_cartella = h.get("n_cartella").toString();
			data_inizio = h.get("data_inizio").toString();
			
			dbc = super.logIn(mylogin);

			myselADI = "SELECT *" +
						" FROM skmmg_adi" +
						" WHERE n_cartella = " + n_cartella +						
						" AND (skadi_data_fine IS NULL " + 
							" OR skadi_data_fine >= " + formatDate(dbc, data_inizio) +
							" OR skadi_fine_effettiva = 'N'" +
						")";
			System.out.println("-esisteADI " + myselADI);
			dbr_adi = dbc.readRecord(myselADI);
			if(dbr_adi != null)
				 return new Boolean(true);
						
			done = true;
			dbc.close();
			super.close(dbc);
			return ret;
		} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(
					"Errore eseguendo una checkAutorizzazione() - " + e1);
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
		
	
	/**
	 * elisa b 20/06/11
	 * @param dbc
	 * @param tipo
	 * @param n_cartella
	 * @param pr_data
	 * @return
	 * @throws Exception
	 */
	private String selEsistenzaAutorizzazione(ISASConnection dbc, String tipo, 
			String n_cartella, String pr_data, String data_inizio) throws Exception{
		
		String campoDataInizio = "sk" + tipo + "_data_inizio";
		if(tipo.equalsIgnoreCase("adi"))
			campoDataInizio = "sk" + tipo + "_data_inzio";
		String campoDataFine = "sk" + tipo + "_data_fine";
		String campoDataFineEffettiva = "sk" + tipo + "_fine_effettiva";
		
		String mysel = "SELECT sk" + tipo + "_data_fine " +
		" FROM skmmg_" + tipo +
		" WHERE n_cartella = " + n_cartella +
		" AND pr_data = " + formatDate(dbc, pr_data) +	
		" AND " + campoDataInizio + " <= " + formatDate(dbc, data_inizio) +
		//elisa b 05/07/11: tolto il ctr su data_fine_effettiva
		" AND (" + campoDataFine + " IS NULL " +
			/*" OR (" +
				campoDataFine + " IS NOT NULL" + 									
				" AND " + campoDataFineEffettiva + " = 'N'" +
			")" +*/
			" OR (" +
				campoDataFine + " IS NOT NULL" + 
				" AND " + campoDataFine + " >= " + formatDate(dbc, data_inizio) +
				//" AND " + campoDataFineEffettiva + " = 'S'" +
			")" +
		")";
		

		
		return mysel;
	}

	public boolean queryKeyEsisteAutorizzazione(ISASConnection dbc, String nCartella, String idSkso, String prDataPuac,
			String prDataChiusura, String tabella) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "queryKeyEsisteAutorizzazione ";
		boolean autorizzazioneAdrEsistente = false;

		String query= "SELECT count(s.n_cartella) as numero FROM " +tabella+
				" s, skmedico m  WHERE s.n_cartella = " + nCartella+
				 " AND m.id_skso = " + idSkso+ " AND m.n_cartella = s.n_cartella AND s.n_contatto = m.n_contatto ";
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbrSkmmgAd = dbc.readRecord(query);
		int numero = ISASUtil.getValoreIntero(dbrSkmmgAd, "numero");
		autorizzazioneAdrEsistente = (numero>0);
		LOG.trace(punto + " valore recuperato>>" + numero+"<< autorizzazioneAdrEsistente>>"+autorizzazioneAdrEsistente+"<<");
		
		return autorizzazioneAdrEsistente;
	}

	public String controllaPeriodoInSoOProroga(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		String punto = ver + "controllaPeriodoInSoOProroga ";
		ISASConnection dbc = null;
		String messaggio = "";
		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, Costanti.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(h, Costanti.CTS_ID_SKSO);
			String dtInizio = ISASUtil.getValoreStringa(h, SKADI_DATA_INIZIO);
			String dtFine = ISASUtil.getValoreStringa(h, SKADI_DATA_FINE);

			
			ISASRecord dbrSo =esisteRmSkso(dbc, nCartella, idSkSo, dtInizio, dtFine); 
			if ( dbrSo == null){
				ISASRecord dbrProroga = esisteProroga(dbc, nCartella, idSkSo, dtInizio, dtFine);
				if( dbrProroga == null){
					dbrProroga = esisteProroga(dbc, nCartella, idSkSo, "", "");
					if( dbrProroga == null){
						dbrSo = esisteRmSkso(dbc, nCartella, idSkSo, "", "");
						String dtInizioRead = ISASUtil.getValoreStringa(dbrSo, CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO);
						String dtFineRead = ISASUtil.getValoreStringa(dbrSo, CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE);
						messaggio = faiMessaggio(dtInizioRead, dtFineRead);
					}else {
						String dtInizioRead = ISASUtil.getValoreStringa(dbrProroga, CostantiSinssntW.CTS_DT_PROROGA_INIZIO);
						String dtFineRead = ISASUtil.getValoreStringa(dbrProroga, CostantiSinssntW.CTS_DT_PROROGA_FINE);
						messaggio = faiMessaggio(dtInizioRead, dtFineRead);
					}
				}
			}

		} catch (CariException ce) {
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			throw new SQLException("Errore nella verifica RmSkso/Proroghe - " + e1);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return messaggio;
	}

	private ISASRecord esisteProroga(ISASConnection dbc, String nCartella, String idSkSo, String dtInizio, String dtFine) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "esisteProroga ";
		String query = "select * from rm_skso_proroghe where n_cartella = " +nCartella + " and id_skso = " +
				idSkSo ; 
		if (ManagerDate.validaData(dtInizio)) {
			query += " and dt_proroga_inizio <= " + dbc.formatDbDate(dtInizio);
		}
		if (ManagerDate.validaData(dtFine)) {
			query += " and dt_proroga_fine >= " + dbc.formatDbDate(dtFine);
		}
		
		ISASRecord dbrRmSkSoProroghe = dbc.readRecord(query);
		return dbrRmSkSoProroghe;
	}

	private String faiMessaggio(String dtInizioRead, String dtFineRead) {
		String messaggio;
		messaggio = Labels.getLabel(
				"autorizzazionemmg.msg.periodo",
				new String[] { ManagerDate.formattaDataIta(dtInizioRead),
						ManagerDate.formattaDataIta(dtFineRead) });
		return messaggio;
	}

	private ISASRecord esisteRmSkso(ISASConnection dbc, String nCartella, String idSkSo, String dtInizio, String dtFine)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "esisteRmSkso ";
		String query = "select * from rm_skso_mmg where n_cartella = " + nCartella + " and id_skso = " + idSkSo;
		if (ManagerDate.validaData(dtInizio)) {
			query += " and data_inizio <= " + dbc.formatDbDate(dtInizio);
		}
		if (ManagerDate.validaData(dtFine)) {
			query += " and data_fine >= " + dbc.formatDbDate(dtFine);
		}
		ISASRecord dbrRmSkSo = dbc.readRecord(query);

		return dbrRmSkSo;
	}
	
}


// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 04/10/2007 - gb
////bargi 08/11/2011 aggiunto controllo su data per stornare i contributi
////bargi 08/11/2011 aggiunto storno anche per RSA solo su fittizio
////bargi 08/11/2011 aggiunto aggiornamento data uscita 
// (1) Si controlla se la data di chiusura di una entit� della gerarchia del sinssnt viola
// qualche vincolo sulle date di apertura e chiusura delle entit� a pi� basso livello.
//
// (2) Se i controlli di cui al punto (1) sono passati, si procede alla chiusura di tutte
// le entit� che stanno sotto l'entit� di cui si � impostata la data di chiusura.
// Le sottoentit� saranno chiuse con la stessa data di chiusura.
// ============================================================================

package it.pisa.caribel.sinssnt.controlli;

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.calcoli.calcoloContoEconomico;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
// 27/04/10
// 30/04/10

public class CartCntrlEtChiusure extends SINSSNTConnectionEJB {

	final boolean forzaChiusura;
	private String cod_usl;
	private String data;
	private String motivo;
	
	/* elisa b 23/06/16: stato del piano assistenziale */
	private static String DA_INVIARE = "1";
	
    /**
     * @deprecated use CartCntrlEtChiusure(boolean forza)
 	*/
	public CartCntrlEtChiusure() {
		forzaChiusura = false;
	}

	public CartCntrlEtChiusure(boolean forza, String cod_usl, String data, String motivo) {
		forzaChiusura = forza;
		this.cod_usl = cod_usl;
		this.data = data;
		this.motivo = motivo;
	}
	
	private static final String mioNome = "1-CartCntrlEtChiusure.";
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	
	private GestCasi gestore_casi = new GestCasi(); // 27/04/10
	
	// 30/04/10 ---
	private EveUtils evUtl = new EveUtils();
	private calcoloContoEconomico cEco = new calcoloContoEconomico();
	it.pisa.caribel.sinssnt.rsa_routine.routineRsa rtRsa = new it.pisa.caribel.sinssnt.rsa_routine.routineRsa();
	
	private Hashtable h_Conf = null;
	private String[] arrKeyConf = new String[] {
			"ABILITAZ_CT", "ABILCT_SINS_AUTO",
			"ABILITAZ_RSA", "ADRSA_UBIC","ABILITAZ_FIT"
		};
	// 30/04/10 ---

	// 04/03/11 m.: aggiunto chiusura ADI/ADP/ADR da skValutazione
	// 	+ aggiunto gestione tabella skPuac secondo ubicazione
	//	+ aggiunto chiusura da "Risposte programmate" (per Veneto)
	// 30/04/10 m.: aggiunto aggiornamentoContoEconomico
	// 27/04/10 m.: aggiunto chiusura CASO 
	// 26/04/10 m.: aggiunto chiusura anche dei rec gi�  chiusi con dtChiusura CARTELLA, se questa � minore. 
	// 19/04/10 m.: aggiunto chiusura ASS_ANAGRAFICA (se segnalazione non ancora presa in carico)
	// 13/01/09 m.: nel metodo chiudoSchedaPuac() aggiunto pulizia eventuale
	// convocazione in sedutaUVM
	// 29/10/08 m.: aggiunto gestione agenda per AS --> tabella AGENDANT_PUA
	// 08/10/08 m.: aggiunto gestione SkPuac --> tabella PUAUVM

	/*
	 * gb 26/09/07 ******* //gb 24/09/07
	 * ***************************************************************** //
	 * Restituisce un messaggio appropriato se si verifica che la data di
	 * chiusura // della cartella � minore della data di chiusura di un progetto
	 * di Assistenza // Sociale (tabella 'ass_progetto'). // Se invece �
	 * maggiore o uguale ritorna la stringa vuota "". // public String
	 * checkDtChiusCartEDtChiusProgSociali(ISASConnection dbc, String cartella,
	 * String data_chiusura) throws Exception { ISASCursor dbcur = null; String
	 * msg = "";
	 * 
	 * if (data_chiusura!=null && !(data_chiusura.equals(""))) { String mySel =
	 * "SELECT *" + " FROM ass_progetto" + " WHERE n_cartella = " + cartella +
	 * " AND ap_data_chiusura > " + formatDate(dbc, data_chiusura) +
	 * " AND ap_data_chiusura IS NOT NULL";System.out.println(
	 * "CartCntrlEtChiusure / checkDtChiusCartEDtChiusProgSociali / mySel: " +
	 * mySel); dbcur = dbc.startCursor(mySel);
	 * 
	 * if ((dbcur != null) && (dbcur.getDimension() >0)) msg =
	 * "Attenzione esistono Progetti di Assistenza Sociale la cui data di chiusura � successiva alla data chiusura della Cartella."
	 * ; }
	 * 
	 * if (dbcur != null) dbcur.close(); return msg; } //
	 * ***********************
	 * *****************************************************
	 * 
	 * //gb 24/09/07
	 * ***************************************************************** //
	 * Restituisce un messaggio appropriato se si verifica che la data di
	 * chiusura // della cartella � minore della data di apertura di un progetto
	 * di Assistenza // Sociale (tabella 'ass_progetto'). // Se invece �
	 * maggiore o uguale ritorna la stringa vuota "". // public String
	 * checkDtChiusCartEDtApeProgSociali(ISASConnection dbc, String cartella,
	 * String data_chiusura) throws Exception { ISASCursor dbcur = null; String
	 * msg = "";
	 * 
	 * if (data_chiusura!=null && !(data_chiusura.equals(""))) { String mySel =
	 * "SELECT *" + " FROM ass_progetto" + " WHERE n_cartella = " + cartella +
	 * " AND ap_data_apertura > " + formatDate(dbc, data_chiusura) +
	 * " AND ap_data_chiusura IS NULL";System.out.println(
	 * "CartCntrlEtChiusure / checkDtChiusCartEDtApeProgSociali / mySel: " +
	 * mySel); dbcur = dbc.startCursor(mySel);
	 * 
	 * if ((dbcur != null) && (dbcur.getDimension() >0)) msg =
	 * "Attenzione esistono Progetti di Assistenza Sociale la cui data di apertura � successiva alla data chiusura della Cartella."
	 * ; }
	 * 
	 * if (dbcur != null) dbcur.close(); return msg; }gb 26/09/07: fine ******
	 */

	// chiamato da cartella x chiudere i contatti non sociale.
	private void chiudoContattiDaCart(ISASConnection dbc, String strNCartella, String strDtChiusura)
			throws SQLException {
		try {
			String[] ar_tableNames = new String[] { "skinf", "skmedico", "skfis", "skfpg", "skmedpal" };
			// String[] ar_fldDtApeNames = new String[] {"ski_data_apertura",
			// "skm_data_apertura", "skf_data", "skm_data_apertura"};
			String[] ar_fldDtChNames = new String[] { "ski_data_uscita", "skm_data_chiusura", "skf_data_chiusura",
					"skfpg_data_uscita", "skm_data_chiusura" };

			for (int i = 0; i < ar_tableNames.length; i++) {
				String[] ar_fldNames = new String[] { "n_cartella" };
				String[] ar_fldTypes = new String[] { "NUM" };
				String[] ar_fldValues = new String[] { strNCartella };
				
// 26/04/10		ISASCursor dbcur = getSelectRecords(dbc, ar_tableNames[i], ar_fldNames, ar_fldTypes, ar_fldValues);
				// 26/04/10 --
				String sel = "SELECT * FROM " + ar_tableNames[i] + 
				" WHERE n_cartella = " + strNCartella + 
				" AND ((" + ar_fldDtChNames[i] + " IS NULL) OR (" + ar_fldDtChNames[i] + " > " + formatDate(dbc, strDtChiusura) + "))";

				ISASCursor dbcur = dbc.startCursor(sel);
				// 26/04/10 --				
				
				Vector vdbr = dbcur.getAllRecord();
				dbcur.close();
				
				for (Enumeration senum = vdbr.elements(); senum.hasMoreElements();) {
					ISASRecord dbr = (ISASRecord) senum.nextElement();
					// String strDtApertura =
					// ((java.sql.Date)dbr.get(ar_fldDtApeNames[i])).toString();
					String strNContatto = ((Integer) dbr.get("n_contatto")).toString();
					// System.out.println(ar_fldDtApeNames[i]
					// +" "+strDtApertura);
					chiudoContatto(dbc, ar_tableNames[i], strNCartella, strNContatto, ar_fldDtChNames[i], strDtChiusura, true);
				}// fine for interno
			}// fine for esterno
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoContattiDaCart()  ", ex);
			}
		}
	}
	
	// 26/04/10
	private void chiudoContatto(ISASConnection dbc, String strTableName, String strNCartella, String strNContatto,
			String strFldDtChName, String strDtChiusura) throws Exception 
	{
		chiudoContatto(dbc, strTableName, strNCartella, strNContatto, strFldDtChName, strDtChiusura, false);
	}
	
	private void chiudoContatto(ISASConnection dbc, String strTableName, String strNCartella, String strNContatto,
			String strFldDtChName, String strDtChiusura, boolean forzaChiusura) throws Exception {
		try {
			String[] ar_fldNames = new String[] { "n_cartella", "n_contatto" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM" };
			String[] ar_fldValues = new String[] { strNCartella, strNContatto };
			String sel = getSelectQuery(dbc, strTableName, ar_fldNames, ar_fldTypes, ar_fldValues);

			ISASRecord dbrDett = dbc.readRecord(sel);
			if ((dbrDett.get(strFldDtChName) == null) 
			|| forzaChiusura) { // 26/04/10
				dbrDett.put(strFldDtChName, strDtChiusura);
				dbc.writeRecord(dbrDett);
			}
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoContatto()  ");
		}
	}

	// Metodo chiamato da cartella
	private void chiudoProgettiAssSocialeDaCart(ISASConnection dbc, String cartella, String data_chiusura,
			String motivo_chiusura, String strCodOperatore) throws Exception {
		ISASCursor dbcur = null;

		try {
			if (data_chiusura != null && !(data_chiusura.equals(""))) {
				String mySel = "SELECT * FROM ass_progetto" +
						" WHERE n_cartella = " + cartella +
						" AND ((ap_data_chiusura IS NULL) OR (ap_data_chiusura > " + formatDate(dbc, data_chiusura) + "))";
				LOG.info("CartCntrlEtChiusure / chiudoProgettiAssSocialeDaCart / mySel: " + mySel);
				dbcur = dbc.startCursor(mySel);

				Vector vdbr = dbcur.getAllRecord();
				for (Enumeration senum = vdbr.elements(); senum.hasMoreElements();) {
					ISASRecord dbr = (ISASRecord) senum.nextElement();
					String strNProgetto = ((Integer) dbr.get("n_progetto")).toString();
					chiudoProgettoAssSociale(dbc, cartella, strNProgetto, data_chiusura, motivo_chiusura,
							strCodOperatore, true);
				}// fine for
			}
			if (dbcur != null)
				dbcur.close();
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoProgettiAssSocialeDaCart()  ", ex);
			}
		}
	}

	// Metodo chiamato da scheda valutazione
	private void chiudoContattiOrProgAssSocialeDaSkVal(ISASConnection dbc, String strNCartella, String strDtSkVal,
			String strDtChiusura, String strMotivoChiusura, String strCodOperatore) throws Exception {
		ISASCursor dbcur = null;
		String[] ar_fldNames = null;
		String[] ar_fldTypes = null;
		String[] ar_fldValues = null;

		try {
			if (strDtChiusura != null && !(strDtChiusura.equals(""))) {
				ar_fldNames = new String[] { "n_cartella", "pr_data" };
				ar_fldTypes = new String[] { "NUM", "DTA" };
				ar_fldValues = new String[] { strNCartella, strDtSkVal };
				dbcur = getSelectRecords(dbc, "progetto_cont", ar_fldNames, ar_fldTypes, ar_fldValues);

				Vector vdbr = dbcur.getAllRecord();
				dbcur.close();
				
				for (Enumeration senum = vdbr.elements(); senum.hasMoreElements();) {
					ISASRecord dbr = (ISASRecord) senum.nextElement();
					String strNContOrProg = ((Integer) dbr.get("prc_n_contatto")).toString();
					String strTipoOperatore = (String) dbr.get("prc_tipo_op");
					if (strTipoOperatore.equals("01"))
						chiudoProgettoAssSociale(dbc, strNCartella, strNContOrProg, strDtChiusura, strMotivoChiusura,
								strCodOperatore);
					else if (strTipoOperatore.equals("02"))
						chiudoContatto(dbc, "skinf", strNCartella, strNContOrProg, "ski_data_uscita", strDtChiusura);
					else if (strTipoOperatore.equals("03"))
						chiudoContatto(dbc, "skmedico", strNCartella, strNContOrProg, "skm_data_chiusura",
								strDtChiusura);
					else if (strTipoOperatore.equals("04"))
						chiudoContatto(dbc, "skfis", strNCartella, strNContOrProg, "skf_data_chiusura", strDtChiusura);
					else if (strTipoOperatore.equals("52"))
						chiudoContatto(dbc, "skmedpal", strNCartella, strNContOrProg, "skm_data_chiusura",
								strDtChiusura);
				}// fine for
			}
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException(
					"CartCntrlEtChiusure, Errore eseguendo una chiudoContattiOrProgAssSocialeDaSkVal()  ");
		}
	}

	// 26/04/10
	private void chiudoProgettoAssSociale(ISASConnection dbc, String strNCartella, String strNProgetto,
			String data_chiusura, String motivo_chiusura, String strCodOperatore) throws Exception 
	{	
		chiudoProgettoAssSociale(dbc, strNCartella, strNProgetto,
									data_chiusura, motivo_chiusura, strCodOperatore, false);
	}
	
	// Metodo chiamato da:
	// chiudoProgettiAssSocialeDaCart e chiudoProgettiAssSocialeDaSkVal
	private void chiudoProgettoAssSociale(ISASConnection dbc, String strNCartella, String strNProgetto,
			String data_chiusura, String motivo_chiusura, String strCodOperatore, 
			boolean forzaChiusura) throws Exception {
		try {
			String sel = "SELECT * FROM ass_progetto" 
					+ " WHERE n_cartella = " + strNCartella
					+ " AND n_progetto = " + strNProgetto;
			LOG.info("CartCntrlEtChiusure / chiudoProgettoAssSociale / sel: " + sel);
			ISASRecord dbrDett = dbc.readRecord(sel);
			if (dbrDett != null) {
				if ((dbrDett.get("ap_data_chiusura") == null) 
				|| forzaChiusura) { // 26/04/10
					dbrDett.put("ap_data_chiusura", data_chiusura);
					dbrDett.put("motivo_chiusura", motivo_chiusura);
					dbrDett.put("ap_oper_ch", strCodOperatore);
					dbc.writeRecord(dbrDett);
				}
			}
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoProgettoAssSociale()  ");
		}
	}

	public void chiudoDaCartellaInGiu(ISASConnection dbc, String strNCartella, String strDtChCartella,
			String strMotivoChiusura, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
			
			//simone 24/03/15 non più necessaria
//			chiudoAssAnagrafica(dbc, strNCartella, strDtChCartella, strMotivoChiusura); // 19/04/10
//			chiudoSchedaValutazione(dbc, strNCartella, strDtChCartella, strMotivoChiusura);
//			chiudoSchedaPuac(dbc, new String[] { "n_cartella" }, new String[] { "NUM" }, new String[] { strNCartella },
//					strDtChCartella, strMotivoChiusura); // 08/10/08
//			chiudoProgettiAssSocialeDaCart(dbc, strNCartella, strDtChCartella, strMotivoChiusura, strCodOperatore);
			chiudoContattiDaCart(dbc, strNCartella, strDtChCartella);

			chiudoAutorizzazioneADI_ADP_ADR(dbc, strNCartella, strDtChCartella);

			String[] ar_fldNames = new String[] { "n_cartella" };
			String[] ar_fldTypes = new String[] { "NUM" };
			String[] ar_fldValues = new String[] { strNCartella };
			chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
			
			
			// 04/03/11: chiusura risposte programmate solo per veneto
			//chiudoRisposteProgr(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);

			chiudoObieOrInterv(dbc, "ass_obbiettivi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
			chiudoObieOrInterv(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
			chiudoVerifiche(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella, strCodOperatore);

			ar_fldNames = new String[] { "ag_cartella" };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
			rimuovoAgendaPuaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella); // 29/10/08
			
			//simone 24/03/15 non più necessaria
			// 27/04/10
//			chiudoCaso(dbc, new String[] {"n_cartella"}, new String[] {"NUM"}, new String[] {strNCartella},
//						strDtChCartella, strMotivoChiusura, strCodOperatore);
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaCartellaInGiu()  ", ex);
			}
		}
	}
	
	// 17/02/15 mv
	public void chiudoDaCartellaInGiuSoloSan(ISASConnection dbc, String strNCartella, String strDtChCartella,
			String strMotivoChiusura, String strCodOperatore) throws Exception 
	{
		String strSelect = "";
		ISASCursor dbcur = null;
		ISASRecord dbr = null;
		String strTipoOperatore = null;
		
		try {
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
			
			// 16/05/16 mv: per evitare che ISAS filtri contatti non della zona (e quindi non
			//	vengano chiusi), si utilizza direttamente UPDATE via SQL
			chiudoContattiDaCartSQL(dbc, strNCartella, strDtChCartella, strMotivoChiusura);
			
			chiudoAutorizzazioneADI_ADP_ADR(dbc, strNCartella, strDtChCartella);

			strSelect = "SELECT * FROM progetto_cont" 
					+ " WHERE n_cartella = " + strNCartella 
					+ " AND prc_tipo_op <> '01'";
			System.out.println("CartCntrlEtChiusure / chiudoDaCartellaInGiuSoloSan / strSelect: " + strSelect);
			dbcur = dbc.startCursor(strSelect);
	
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					dbr = dbcur.getRecord();
					strTipoOperatore = (String) dbr.get("prc_tipo_op");
					String[] ar_fldNames = new String[] { "n_cartella" };
					String[] ar_fldTypes = new String[] { "NUM" };
					String[] ar_fldValues = new String[] { strNCartella };
					chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
					chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
					
					ar_fldNames = new String[] { "ag_cartella" };
					rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella);
				}
			}
			if (dbcur != null)
				dbcur.close();
						
			// 27/04/10
			chiudoCaso(dbc, new String[] {"n_cartella"}, new String[] {"NUM"}, new String[] {strNCartella},
						strDtChCartella, strMotivoChiusura, strCodOperatore);
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaCartellaInGiu()  ");
		}
	}

	public void chiudoSkSO(myLogin mylogin, ISASConnection dbc, String strNCartella, String strDtChCartella, String strMotivoChiusura, boolean forzaChiusura) throws CariException {
		ISASCursor dbcur = null;
		String methodName = "chiudoSkSO";
		LOG.debug(methodName+": cartella= "+strDtChCartella+" data= "+strDtChCartella+" motivo="+strMotivoChiusura);
		try {
		RMSkSOEJB skso_ejb = new RMSkSOEJB();
		String sql = "select * from rm_skso where n_cartella = "+strNCartella+
					" and pr_data_chiusura is null";
		LOG.debug(methodName+": sql = "+sql);
			 dbcur = dbc.startCursor(sql);
			 if (dbcur!=null && dbcur.getDimension()>0){
				 while (dbcur.next()){
					 ISASRecord dbr = (ISASRecord)dbcur.getRecord();
					 if (forzaChiusura) dbr.put(Costanti.FORZA_CHIUSURA,"S");
					 dbr.put("pr_data_chiusura",strDtChCartella);
					 dbr.put("pr_motivo_chiusura","12");
					 LOG.debug(methodName+": chiudo dbr: "+dbr.getHashtable().toString());
					 skso_ejb.updatePerChiusura(mylogin, dbr);
				 }
			 }
		} catch(CariException ce){
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ce, dbc, cod_usl, data, motivo);
			}else{
				throw ce;
			}
		} catch(Exception e){
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(e, dbc, cod_usl, data, motivo);
			}else{
				throw new CariException("CartCntrlEtChiusure, Errore eseguendo una chiudoSkSO()  ", e);
			}
		}
		finally{
			close_dbcur_nothrow(methodName, dbcur);
		}
		
		
	}



	private void chiudoAutorizzazioneADI_ADP_ADR(ISASConnection dbc, String strNCartella, String strDtChCartella)
			throws SQLException {
		String punto = mioNome + ".chiudoAutorizzazioneADI_ADP_ADR ";
		ISASCursor dbcur = null;

		try {
			if (strDtChCartella != null && !(strDtChCartella.equals(""))) {
				String tabella = "skmmg_adi";
				String campoDataSettare = "skadi_data_fine";
				String campoDataDaControllare = "skadi_data";
				settaSkmmg(dbc, strNCartella, tabella, campoDataDaControllare, campoDataSettare, strDtChCartella);

				tabella = "skmmg_adp";
				campoDataSettare = "skadp_data_fine";
				campoDataDaControllare = "skadp_data";
				settaSkmmg(dbc, strNCartella, tabella, campoDataDaControllare, campoDataSettare, strDtChCartella);

				tabella = "skmmg_adr";
				campoDataSettare = "skadr_data_fine";
				campoDataDaControllare = "skadr_data";
				settaSkmmg(dbc, strNCartella, tabella, campoDataDaControllare, campoDataSettare, strDtChCartella);
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoProgettiAssSocialeDaCart()  ", ex);
			}
		}
	}

	private void settaSkmmg(ISASConnection dbc, String strNCartella, String tabella, String cmdControllare,
			String cmpSettareDtChCartella, String dtSettare) {
		String punto = mioNome + ".settaSkmmg ";
		String mySel = "SELECT * FROM " + tabella + 
			" WHERE n_cartella = '" + strNCartella + "'" +
			" AND ((" + cmpSettareDtChCartella + " IS NULL) OR (" + cmpSettareDtChCartella + " > " + formatDate(dbc, dtSettare) + "))";
		stampaQuery(punto, mySel);
		ISASCursor dbcur = null;
		try {
			dbcur = dbc.startCursor(mySel);
			String prData, skData, query;
			while (dbcur.next()) {
				ISASRecord skmmgAdi = dbcur.getRecord();
				prData = getValoreStringa(skmmgAdi, "pr_data");
				skData = getValoreStringa(skmmgAdi, cmdControllare);
				query = mySel + "  AND pr_data = " + formatDate(dbc, prData) + " AND " + cmdControllare + " = "
						+ formatDate(dbc, skData);

				stampaQuery(punto, query);

				ISASRecord recUpd = dbc.readRecord(query);
				if (recUpd != null) {
					recUpd.put(cmpSettareDtChCartella, dtSettare);
					dbc.writeRecord(recUpd);
					stampa(punto, "Record aggiornato ");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dbcur != null)
				try {
					dbcur.close();
				} catch (Exception e) {
					stampaEccezione(punto + "Chiusura cursore");
				}
		}

	}

	// gb 02/11/07 public void chiudoDaSkValInGiu(ISASConnection dbc, String
	// strNCartella, String strDtChSkVal, String strMotivoChiusura, String
	// strCodOperatore) throws Exception
	// gb 02/11/07 *******
	public void chiudoDaSkValInGiu(ISASConnection dbc, String strNCartella, String strDtSkVal, String strDtChSkVal,
			String strMotivoChiusura, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
		
			chiudoSchedaPuac(dbc, new String[] { "n_cartella", "pr_data" }, new String[] { "NUM", "DTA" },
					new String[] { strNCartella, strDtSkVal }, strDtChSkVal, strMotivoChiusura); // 08/10/08
			
			// 04/03/11
			chiudoAutorizzazioneADI_ADP_ADR(dbc, strNCartella, strDtChSkVal);					
					
			// gb 02/11/07 *******
			chiudoContattiOrProgAssSocialeDaSkVal(dbc, strNCartella, strDtSkVal, strDtChSkVal, strMotivoChiusura,
					strCodOperatore);
			/*
			 * gb 02/11/07 ******* chiudoContattiDaCart(dbc, strNCartella,
			 * strDtChSkVal); chiudoProgettiAssSocialeDaCart(dbc, strNCartella,
			 * strDtChSkVal, strMotivoChiusura, strCodOperatore);gb 02/11/07:
			 * fine ******
			 */
			String[] ar_fldNames = new String[] { "n_cartella" };
			String[] ar_fldTypes = new String[] { "NUM" };
			String[] ar_fldValues = new String[] { strNCartella };
			chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal);
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal);
			
			// 04/03/11: chiusura risposte programmate
			chiudoRisposteProgr(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal);

			chiudoObieOrInterv(dbc, "ass_obbiettivi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal);
			chiudoObieOrInterv(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal);
			chiudoVerifiche(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal, strCodOperatore);

			ar_fldNames = new String[] { "ag_cartella" };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal);
			rimuovoAgendaPuaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal); // 29/10/08
			
			// 27/04/10
			chiudoCaso(dbc, new String[] {"n_cartella", "pr_data"}, new String[] {"NUM", "DTA"}, new String[] {strNCartella, strDtSkVal},
						strDtChSkVal, strMotivoChiusura, strCodOperatore);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaSkValInGiu()  ");
		}
	}

	public void chiudoDaProgettoAssSocialeInGiu(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strDtChProgetto, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
		
			String[] ar_fldNames = new String[] { "n_cartella", "n_progetto", "pa_tipo_oper" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
			String[] ar_fldValues = new String[] { strNCartella, strNProgetto, "01" };
			chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChProgetto);
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChProgetto);

			ar_fldNames = new String[] { "n_cartella", "n_progetto" };
			ar_fldTypes = new String[] { "NUM", "NUM" };
			ar_fldValues = new String[] { strNCartella, strNProgetto };
			chiudoObieOrInterv(dbc, "ass_obbiettivi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChProgetto);
			chiudoObieOrInterv(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChProgetto);
			chiudoVerifiche(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChProgetto, strCodOperatore);

			ar_fldNames = new String[] { "ag_cartella", "ag_contatto", "ag_tipo_oper" };
			ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
			ar_fldValues = new String[] { strNCartella, strNProgetto, "01" };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChProgetto);
			// 29/10/08: se chiudo il contattoSociale, dato che � l'unico
			// correntemente aperto => rimuovo tutti i record con dt maggiore
			rimuovoAgendaPuaCaricata(dbc, new String[] { "ag_cartella" }, new String[] { "NUM" },
					new String[] { strNCartella }, strDtChProgetto);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new Exception("CartCntrlEtChiusure, Errore eseguendo una chiudoDaProgettoAssSocialeInGiu()  ");
		}
	}

	public void chiudoDaContattoInGiu(ISASConnection dbc, String strNCartella, String strNContatto,
			String strDtChContatto, String strTipoOperatore, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
		
			String[] ar_fldNames = new String[] { "n_cartella", "n_progetto", "pa_tipo_oper" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
			String[] ar_fldValues = new String[] { strNCartella, strNContatto, strTipoOperatore };
			chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChContatto);
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChContatto);

			ar_fldNames = new String[] { "ag_cartella", "ag_contatto", "ag_tipo_oper" };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChContatto);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaContattoInGiu()  ");
		}
	}

	public void chiudoDaObiettivoInGiu(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strDtChObiettivo, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
				
			String[] ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo", "pa_tipo_oper" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM", "STR", "STR" };
			String[] ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, "01" };
			chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChObiettivo);
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChObiettivo);

			ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo" };
			ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
			ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo };
			chiudoObieOrInterv(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, strDtChObiettivo);

			ar_fldNames = new String[] { "ag_cartella", "ag_contatto", "cod_obbiettivo", "ag_tipo_oper" };
			ar_fldTypes = new String[] { "NUM", "NUM", "STR", "STR" };
			ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, "01" };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChObiettivo);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaObiettivoInGiu()  ");
		}
	}

	public void chiudoDaInterventoInGiu(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strNIntervento, String strDtChIntervento, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
				
			String[] ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo", "n_intervento",
					"pa_tipo_oper" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM", "STR" };
			String[] ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento, "01" };
			chiudoPianiAssitenz(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChIntervento);
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChIntervento);

			ar_fldNames = new String[] { "ag_cartella", "ag_contatto", "cod_obbiettivo", "n_intervento", "ag_tipo_oper" };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChIntervento);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaInterventoInGiu()  ");
		}
	}

	public void chiudoDaPianoAssistInGiu(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strNIntervento, String strDtApePianoAssist, String strDtChPianoAssist,
			String strTipoOperatore, String strCodOperatore) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
				
			String[] ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo", "n_intervento",
					"pa_data", "pa_tipo_oper" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM", "DTA", "STR" };
			String[] ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento,
					strDtApePianoAssist, strTipoOperatore };
			chiudoPianiAccessi(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChPianoAssist);

			ar_fldNames = new String[] { "ag_cartella", "ag_contatto", "cod_obbiettivo", "n_intervento", "ag_tipo_oper" };
			ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM", "STR" };
			ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strTipoOperatore };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChPianoAssist);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaPianoAssistInGiu()  ");
		}
	}

	public void chiudoDaPianoAccessiInGiu(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strNIntervento, String strDtFinePianoAcc, String strTipoOperatore,
			String strPrestCod, String strCodOperEsec) throws Exception {
		try {
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperEsec, arrKeyConf);
				
			String[] ar_fldNames = new String[] { "ag_cartella", "ag_contatto", "cod_obbiettivo", "n_intervento",
					"ag_tipo_oper", "ap_prest_cod", "ag_oper_ref" };
			String[] ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM", "STR", "STR", "STR" };
			String[] ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento,
					strTipoOperatore, strPrestCod, strCodOperEsec };
			rimuovoAgendaCaricata(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtFinePianoAcc);
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaPianoAccessiInGiu()  ");
		}
	}

	private void chiudoPianiAssitenz(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura) throws Exception {
		try {
//26/04/10	ISASCursor dbcur = getSelectRecords(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues);

			// 26/04/10 --
			String mySelect = getSelectQuery(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues);
			mySelect += " AND ((pa_data_chiusura IS NULL) OR (pa_data_chiusura > " + formatDate(dbc, strDtChiusura) + "))";
			ISASCursor dbcur = dbc.startCursor(mySelect);
			// 26/04/10 --
			
			// Metto i record letti in un vector (un vector di record).
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				String strDataApertura = ((java.sql.Date) dbr.get("pa_data")).toString();
				String sel = "SELECT * FROM piano_assist" 
					+ " WHERE n_cartella = " + (Integer) dbr.get("n_cartella") 
					+ " AND n_progetto = " + (Integer) dbr.get("n_progetto")
					+ " AND cod_obbiettivo = '" + (String) dbr.get("cod_obbiettivo") + "'" 
					+ " AND n_intervento = " + (Integer) dbr.get("n_intervento") 
					+ " AND pa_tipo_oper = '" + (String) dbr.get("pa_tipo_oper") + "'" 
					+ " AND pa_data = " + formatDate(dbc, strDataApertura);
				LOG.info("CartCntrlEtChiusure / chiudoPianiAssitenz / sel: " + sel);
				ISASRecord dbrDett = dbc.readRecord(sel);
				dbrDett.put("pa_data_chiusura", strDtChiusura);
				
				/*elisa b 23/06/16: in caso di chiusura si congela il piano*/
				dbrDett.put("flag_stato", DA_INVIARE);
				
				dbc.writeRecord(dbrDett);
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoPianiAssitenz()  ", ex);
			}
		}
	}

	private void chiudoPianiAccessi(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura) throws Exception {
		try {
			String strSelect = getSelectQuery(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues);
			strSelect += " AND ((pi_data_fine IS NULL) OR (pi_data_fine > " + formatDate(dbc, strDtChiusura) + "))";
			LOG.info("CartCntrlEtChiusure / chiudoPianiAccessi / strSelect: " + strSelect);
			ISASCursor dbcur = dbc.startCursor(strSelect);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String strDataApertura = ((java.sql.Date) dbr.get("pa_data")).toString();
				String sel = "SELECT * FROM piano_accessi" 
						+ " WHERE n_cartella = " + (Integer) dbr.get("n_cartella") 
						+ " AND n_progetto = " + (Integer) dbr.get("n_progetto")
						+ " AND cod_obbiettivo = '" + (String) dbr.get("cod_obbiettivo") + "'" 
						+ " AND n_intervento = " + (Integer) dbr.get("n_intervento") 
						+ " AND pa_data = " + formatDate(dbc, strDataApertura)
						+ " AND pi_prog = " + (Integer) dbr.get("pi_prog") 
						+ " AND pa_tipo_oper = '" + (String) dbr.get("pa_tipo_oper") + "'";
				LOG.info("CartCntrlEtChiusure / chiudoPianiAccessi / sel: " + sel);
				ISASRecord dbrDett = dbc.readRecord(sel);
				if (dbrDett != null) {
					dbrDett.put("pi_data_fine", strDtChiusura);
					
					/*elisa b 23/06/16: in caso di chiusura si congela il piano*/
					dbrDett.put("flag_stato", DA_INVIARE);

					
					dbc.writeRecord(dbrDett);
				}
			}// fine for
			if (dbcur != null)
				dbcur.close();
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoPianiAccessi()  ", ex);
			}
		}
	}

	/*
	 * gb 26/09/07 ******* public void chiudoPianiAssitenz(String cartella,
	 * String data_chiusura, ISASConnection dbc) throws Exception { ISASCursor
	 * dbcur = null; String mySel = "SELECT *" + " FROM piano_assist" +
	 * " WHERE n_cartella = " + cartella + " AND pa_data_chiusura IS NULL ";
	 * System.out.println("CartCntrlEtChiusure / chiudoPianiAssitenz / mySel: "
	 * + mySel); dbcur = dbc.startCursor(mySel); Vector
	 * vdbr=dbcur.getAllRecord(); for(Enumeration
	 * senum=vdbr.elements();senum.hasMoreElements(); ) { ISASRecord
	 * dbr=(ISASRecord)senum.nextElement(); String
	 * strDataApertura=((java.sql.Date)dbr.get("pa_data")).toString(); String
	 * sel = "SELECT *" + " FROM piano_assist" + " WHERE n_cartella = " +
	 * cartella + " AND n_progetto = " + (Integer)dbr.get("n_progetto") +
	 * " AND cod_obbiettivo = '" + (String)dbr.get("cod_obbiettivo") + "'" +
	 * " AND n_intervento = " + (Integer)dbr.get("n_intervento") +
	 * " AND pa_tipo_oper = '" + (String)dbr.get("pa_tipo_oper") + "'" +
	 * " AND pa_data = " + formatDate(dbc, strDataApertura);
	 * System.out.println("CartCntrlEtChiusure / chiudoPianiAssitenz / sel: " +
	 * sel); ISASRecord dbrDett=dbc.readRecord(sel);
	 * dbrDett.put("pa_data_chiusura", data_chiusura); dbc.writeRecord(dbrDett);
	 * }//fine for if (dbcur != null) dbcur.close(); }gb 26/09/07. fine ******
	 */

	/*
	 * gb 28/09/07 ******* public void AggiornaDataPianointerv(String cartella,
	 * String data_chiusura, ISASConnection dbc) throws SQLException { try {
	 * String mysel = "SELECT *" + " FROM piano_accessi" +
	 * " WHERE n_cartella = " + cartella +
	 * " AND ( pi_data_fine is null OR pi_data_fine > " +
	 * formatDate(dbc,data_chiusura)+")"; //se pi_data_fine � valorizzata ma
	 * data > della data chiusura questa viene anticipata
	 * System.out.println("CartCntrlEtChiusure / AggiornaDataPianointerv / mysel: "
	 * + mysel); ISASCursor dbcur=dbc.startCursor(mysel); while (dbcur.next()) {
	 * ISASRecord dbr=dbcur.getRecord(); String strDataApertura =
	 * ((java.sql.Date)dbr.get("pa_data")).toString(); String sel = "SELECT *" +
	 * " FROM piano_accessi" + " WHERE n_cartella = " + cartella +
	 * " AND n_progetto = " + (Integer)dbr.get("n_progetto") +
	 * " AND cod_obbiettivo = '" + (String)dbr.get("cod_obbiettivo") + "'" +
	 * " AND n_intervento = " + (Integer)dbr.get("n_intervento") +
	 * " AND pa_data = " + formatDate(dbc, strDataApertura) + " AND pi_prog = "
	 * + (Integer)dbr.get("pi_prog") + " AND pa_tipo_oper= '" +
	 * (String)dbr.get("pa_tipo_oper") + "'";
	 * System.out.println("CartCntrlEtChiusure / AggiornaDataPianointerv / sel: "
	 * + sel); ISASRecord dbrDett=dbc.readRecord(sel); if(dbrDett!=null) {
	 * dbrDett.put("pi_data_fine", data_chiusura); dbc.writeRecord(dbrDett); }
	 * }//fine for if (dbcur != null) dbcur.close(); } catch (Exception ex) {
	 * System.out.println(ex); throw newSQLException(
	 * "CartCntrlEtChiusure, Errore eseguendo una AggiornaDataPianointerv()  ");
	 * } }gb 28/09/07: fine ******
	 */

	private void rimuovoAgendaCaricata(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		ISASCursor dbcur = null;
		try {
			String mysel = getSelectQuery(dbc, "agendant_interv, agendant_intpre", ar_fldNames, ar_fldTypes,
					ar_fldValues);
			mysel += " AND ag_data > " + formatDate(dbc, strDtChiusura) 
					+ " AND ag_stato = 0"
					+ " AND ag_data = ap_data" 
					+ " AND ag_progr = ap_progr" 
					+ " AND ag_oper_ref = ap_oper_ref"
					+ " ORDER BY ag_data";
			LOG.info("CartCntrlEtChiusure/rimuovoAgendaCaricata, mysel: " + mysel);
			dbcur = dbc.startCursor(mysel);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				cancellaAppuntam(dbr, dbc);
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una rimuovoAgendaCaricata()  ", ex);
			}
		} finally {
			close_dbcur_nothrow("rimuovoAgendaCaricata", dbcur);
		}
	}

	/*
	 * gb 28/09/07 ******* public void rimuovoAgendaCaricata(String
	 * cartella,String data_chiusura,ISASConnection dbc) throws
	 * DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	 * boolean done=false; ISASCursor dbcur=null; try{ String mysel="SELECT *" +
	 * " FROM agendant_interv, agendant_intpre"+ " WHERE ag_cartella = " +
	 * cartella + " AND ag_data > " + formatDate(dbc, data_chiusura) +
	 * " AND ag_stato = 0" + " AND ag_data = ap_data" +
	 * " AND ag_progr = ap_progr" + " AND ag_oper_ref = ap_oper_ref" +
	 * " ORDER BY ag_data";
	 * debugMessage("CartCntrlEtChiusure/rimuovoAgendaCaricata mysel: " +
	 * mysel); dbcur=dbc.startCursor(mysel); while (dbcur.next()) { ISASRecord
	 * dbr = dbcur.getRecord(); cancellaAppuntam(dbr, dbc); } if (dbcur != null)
	 * dbcur.close(); done=true; }catch(Exception e) {System.out.println(
	 * "CartCntrlEtChiusure, Errore in cancella rimuovoAgendaCaricata..."+e);
	 * throw newSQLException(
	 * "CartCntrlEtChiusure, Errore eseguendo rimuovoAgendaCaricata()  ");
	 * }finally{ if(!done){ try{ if (dbcur != null) dbcur.close();
	 * }catch(Exception e2){System.out.println(e2);} } } }gb 28/09/07: fine
	 * ******
	 */

	private void cancellaAppuntam(ISASRecord dbrec, ISASConnection dbc) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		try {

			String data = ((java.sql.Date) dbrec.get("ap_data")).toString();
			String selag = "SELECT * FROM agendant_intpre" 
					+ " WHERE ap_data = " + formatDate(dbc, data)
					+ " AND ap_progr = " + dbrec.get("ap_progr") 
					+ " AND ap_oper_ref = '" + (String) dbrec.get("ap_oper_ref") + "'" 
					+ " AND ap_prest_cod = '" + (String) dbrec.get("ap_prest_cod") + "'";
			debugMessage("CartCntrlEtChiusure/cancellaAppuntam selag(1): " + selag);
			ISASRecord dbag = dbc.readRecord(selag);
			if (dbag != null) {
				dbc.deleteRecord(dbag);
				dbag = null;
				// devo controllare se sono rimasti record su agenda_intpre se
				// non
				// ce ne sono occorre cancellare anche il record su
				// agenda_interv
				selag = "SELECT COUNT(*) tot FROM agendant_intpre" 
						+ " WHERE ap_data = " + formatDate(dbc, data)
						+ " AND ap_progr = " + dbrec.get("ap_progr") 
						+ " AND ap_oper_ref = '" + (String) dbrec.get("ap_oper_ref") + "'";
				debugMessage("CartCntrlEtChiusure/cancellaAppuntam selag(2): " + selag);
				dbag = dbc.readRecord(selag);

				int t = 0;
				if (dbag != null)
					t = util.getIntField(dbag, "tot");
				if (t == 0) {
					// cancello da agenda_interv
					selag = "SELECT * FROM agendant_interv" 
							+ " WHERE ag_data = " + formatDate(dbc, data)
							+ " AND ag_progr = " + dbrec.get("ag_progr") 
							+ " AND ag_oper_ref = '" + (String) dbrec.get("ag_oper_ref") + "'";
					debugMessage("CartCntrlEtChiusure/cancellaAppuntam selag(3): " + selag);
					dbag = dbc.readRecord(selag);
					dbc.deleteRecord(dbag);
				}
			}
		} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure, Errore in cancellaAppuntam..." + e);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo cancellaAppuntam()  ");
		}
	}

	// 29/10/08
	private void rimuovoAgendaPuaCaricata(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASCursor dbcur = null;
		String codOp = null;
		String dtApp = null;
		String oraApp = null;

		try {
			String mysel = getSelectQuery(dbc, "agendant_pua", ar_fldNames, ar_fldTypes, ar_fldValues);
			mysel += " AND ag_data_app > " + formatDate(dbc, strDtChiusura);
			LOG.info("CartCntrlEtChiusure/rimuovoAgendaPuaCaricata, mysel: " + mysel);

			dbcur = dbc.startCursor(mysel);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				codOp = (String) dbr.get("ag_cod_oper");
				dtApp = ((java.sql.Date) dbr.get("ag_data_app")).toString();
				oraApp = (String) dbr.get("ag_ora_app");

				String selag = "SELECT * FROM agendant_pua" 
						+ " WHERE ag_cod_oper = '" + codOp + "'"
						+ " AND ag_data_app = " + formatDate(dbc, dtApp) 
						+ " AND ag_ora_app = '" + oraApp + "'";

				ISASRecord dbag = dbc.readRecord(selag);
				if (dbag != null)
					dbc.deleteRecord(dbag);
			}

			if (dbcur != null)
				dbcur.close();

			done = true;
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una rimuovoAgendaPuaCaricata()  ", ex);
			}
		} finally {
			close_dbcur_nothrow("rimuovoAgendaPuaCaricata", dbcur);
		}	
	}

	// gb 27/09/07 *******
	// Controlla se le date apertura e chiusura delle entit� che stanno sotto
	// la cartella sono maggiori della data di chiusura impostata
	// nella cartella. Se si, ritorna un messaggio, altrimenti, come
	// messaggio, ritorna la stringa vuota.
	public String checkDtChDaCartGTDtApeDtCh(ISASConnection dbc, String strNCartella, String strDtChCartella, boolean forzaChiusura)
			throws Exception {
		String strTipoOperatore = "";
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;

		ar_fldNames = new String[] { "int_cartella" };
		ar_fldTypes = new String[] { "NUM" };
		ar_fldValues = new String[] { strNCartella };

		try{
			// Controllo Prestazioni erogate
			msg = checkDtGTDtInterv(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella, "");
			if (!msg.equals(""))
				return msg;
	
			ar_fldNames = new String[] { "n_cartella" };
	
			// Controllo Scheda valutazione
			msg = checkDtChGTDtApeEtDtCh(dbc, "progetto", ar_fldNames, ar_fldTypes, ar_fldValues, "pr_data",
					"pr_data_chiusura", strDtChCartella, "Scheda Valutazione", "");
			if (!msg.equals(""))
				return msg;
	
			// 08/10/08: Controllo Scheda PUAC
			// 04/03/11 --
			String nmTabSkPuac = evUtl.getNmTabSkPuac(dbc);		
			boolean esisteFldChiu = evUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_data_chiusura");
			if (esisteFldChiu) { // 04/03/11 --
				msg = checkDtChGTDtApeEtDtCh(dbc, nmTabSkPuac, ar_fldNames, ar_fldTypes, ar_fldValues, "pr_data_puac",
						"pr_data_chiusura", strDtChCartella, "Scheda PUAC", "");
				if (!msg.equals(""))
					return msg;
			}
			msg = checkDtChGTDtApeEtDtCh(dbc, nmTabSkPuac, ar_fldNames, ar_fldTypes, ar_fldValues, "pr_data_puac",
					"pr_data_verbale_uvm", strDtChCartella, "Scheda PUAC", "");
			if (!msg.equals(""))
				return msg;
			// 08/10/08 ---------------
	
	/**** 02/12/11: vorrebbero solo un msg di warning, ma la CariException fa solo msg bloccanti e quindi si toglie del tutto		
			// 04/03/11: controllo Autorizzazioni ADI/ADP/ADR --
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmmg_adi", ar_fldNames, ar_fldTypes, ar_fldValues, "skadi_data_inzio",
					"skadi_data_fine", strDtChCartella, "Autorizzazioni ADI", "");
			if (!msg.equals(""))
				return msg;
				
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmmg_adp", ar_fldNames, ar_fldTypes, ar_fldValues, "skadp_data_inizio",
					"skadp_data_fine", strDtChCartella, "Autorizzazioni ADP", "");
			if (!msg.equals(""))
				return msg;
	
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmmg_adr", ar_fldNames, ar_fldTypes, ar_fldValues, "skadr_data_inizio",
					"skadr_data_fine", strDtChCartella, "Autorizzazioni ADR", "");
			if (!msg.equals(""))
				return msg;			
			// 04/03/11: controllo Autorizzazioni ADI/ADP/ADR --
	****/
			
	//		// 04/03/11: controllo RisposteProgrammate --
	//		msg = checkDtChGTDtApeEtDtCh(dbc, "rv_puauvm_rispro", ar_fldNames, ar_fldTypes, ar_fldValues, "dt_ini",
	//				"dt_fine", strDtChCartella, "Risposte programmate", "");
	//		if (!msg.equals(""))
	//			return msg;
	//		// 04/03/11: controllo RisposteProgrammate --
			
			// Controllo Progetti/Contatti
			msg = checkDtChGTDtApeEtDtCh(dbc, "ass_progetto", ar_fldNames, ar_fldTypes, ar_fldValues, "ap_data_apertura",
					"ap_data_chiusura", strDtChCartella, "Progetti", "01");
			if (!msg.equals(""))
				return msg;
			msg = checkDtChGTDtApeEtDtCh(dbc, "skinf", ar_fldNames, ar_fldTypes, ar_fldValues, "ski_data_apertura",
					"ski_data_uscita", strDtChCartella, "Contatti", "02");
			if (!msg.equals(""))
				return msg;
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmedico", ar_fldNames, ar_fldTypes, ar_fldValues, "skm_data_apertura",
					"skm_data_chiusura", strDtChCartella, "Contatti", "03");
			if (!msg.equals(""))
				return msg;
			msg = checkDtChGTDtApeEtDtCh(dbc, "skfis", ar_fldNames, ar_fldTypes, ar_fldValues, "skf_data",
					"skf_data_chiusura", strDtChCartella, "Contatti", "04");
			if (!msg.equals(""))
				return msg;
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmedpal", ar_fldNames, ar_fldTypes, ar_fldValues, "skm_data_apertura",
					"skm_data_chiusura", strDtChCartella, "Contatti", "52");
			if (!msg.equals(""))
				return msg;
	
			// Controllo Piani assistenziali
			msg = checkDtChGTDtApeEtDtCh(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues, "pa_data",
					"pa_data_chiusura", strDtChCartella, "Piani Assistenziali", "");
			if (!msg.equals(""))
				return msg;
	
			// Controlli Piani accessi. (confronto solo con data apertura)
			msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues, "pi_data_inizio",
					"pi_data_fine", strDtChCartella, "Piani Accessi", "");
			if (!msg.equals(""))
				return msg;
	
			// Controlli Obiettivi. (solo assistenza sociale)
			msg = checkDtChGTDtApeEtDtCh(dbc, "ass_obbiettivi", ar_fldNames, ar_fldTypes, ar_fldValues, "ob_data_ins",
					"ob_data_ragg", strDtChCartella, "Obiettivi", "01");
			if (!msg.equals(""))
				return msg;
			// Controlli Interventi. (solo assistenza sociale)
			msg = checkDtChGTDtApeEtDtCh(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, "int_data_ins",
					"int_data_concl", strDtChCartella, "Interventi", "01");
			if (!msg.equals(""))
				return msg;
	
			/***
			 * 06/02/08 *** //Controllo Dettagli Interventi msg =
			 * checkDtChDettIntervDtCh(dbc, ar_fldNames, ar_fldTypes, ar_fldValues,
			 * strDtChCartella); if (!msg.equals("")) return msg; 06/02/08
			 ***/
	
			// Controlli Verifiche di progetto. (solo assistenza sociale)
			msg = checkDtChGTDtApeEtDtCh(dbc, "ass_verifica", ar_fldNames, ar_fldTypes, ar_fldValues, "ver_data",
					"ver_data_chiusa", strDtChCartella, "Verifiche", "01");
		}catch (Exception e){
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(e, dbc, cod_usl, data, motivo);
			}else{
				throw e;
			}
		}
		return msg;
	}

	// 17/02/15 mv: metodo ripreso dal precedente, ma NON considerando skPuac e contattoAS
	public String checkDtChDaCartGTDtApeDtChSoloSan(ISASConnection dbc, String strNCartella, String strDtChCartella)
			throws Exception {
		String strSelect = "";
		ISASCursor dbcur = null;
		ISASRecord dbr = null;
		String strTipoOperatore = "";
		String strNContatto = "";
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;
		
		try {
			ar_fldNames = new String[] { "n_cartella" };
			ar_fldTypes = new String[] { "NUM" };
			ar_fldValues = new String[] { strNCartella };
				
			strSelect = "SELECT * FROM progetto_cont" 
					+ " WHERE n_cartella = " + strNCartella 
					+ " AND prc_tipo_op <> '01'";
			System.out.println("CartCntrlEtChiusure / checkDtChDaCartGTDtApeDtChSoloSan / strSelect: " + strSelect);
			dbcur = dbc.startCursor(strSelect);
	
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				ar_fldTypes = new String[] { "NUM", "NUM" };
				while (dbcur.next()) {
					dbr = dbcur.getRecord();
					strTipoOperatore = (String) dbr.get("prc_tipo_op");
					strNContatto = ((Integer) dbr.get("prc_n_contatto")).toString();
					ar_fldValues = new String[] { strNCartella, strNContatto };
					// Controllo Prestazioni erogate
					ar_fldNames = new String[] { "int_cartella", "*" };
					msg = checkDtGTDtInterv(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella, strTipoOperatore);
					if (!msg.equals(""))
						break;
					
					// 01/09/16: anche INTMMG
					if (strTipoOperatore.equals("03") && isRegToscana(dbc)) {
						ar_fldNames = new String[] { "int_cartella"};
						msg = checkDtGTDtIntMmg(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChCartella, "");
						if (!msg.equals(""))
							break;
					}
					
					// Controllo Contatti				
					ar_fldNames = new String[] { "n_cartella", "n_contatto" };
					if (strTipoOperatore.equals("02"))
						msg = checkDtChGTDtApeEtDtCh(dbc, "skinf", ar_fldNames, ar_fldTypes, ar_fldValues,
								"ski_data_apertura", "ski_data_uscita", strDtChCartella, "Contatti", strTipoOperatore);
					else if (strTipoOperatore.equals("03"))
						msg = checkDtChGTDtApeEtDtCh(dbc, "skmedico", ar_fldNames, ar_fldTypes, ar_fldValues,
								"skm_data_apertura", "skm_data_chiusura", strDtChCartella, "Contatti", strTipoOperatore);
					else if (strTipoOperatore.equals("04"))
						msg = checkDtChGTDtApeEtDtCh(dbc, "skfis", ar_fldNames, ar_fldTypes, ar_fldValues,
								"skf_data", "skf_data_chiusura", strDtChCartella, "Contatti", strTipoOperatore);
					else if (strTipoOperatore.equals("52"))
						msg = checkDtChGTDtApeEtDtCh(dbc, "skmedpal", ar_fldNames, ar_fldTypes, ar_fldValues,
								"skm_data_apertura", "skm_data_chiusura", strDtChCartella, "Contatti", strTipoOperatore);
					if (!msg.equals(""))
						break;
	
					ar_fldNames = new String[] { "n_cartella", "n_progetto" };
					// Controllo Piani assistenziali
					msg = checkDtChGTDtApeEtDtCh(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues,
							"pa_data", "pa_data_chiusura", strDtChCartella, "Piani Assistenziali", strTipoOperatore);
					if (!msg.equals(""))
						break;
	
					// Controllo Piani accessi
					msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues,
							"pi_data_inizio", "pi_data_fine", strDtChCartella, "Piani Accessi", strTipoOperatore);
					if (!msg.equals(""))
						break;
				}
			}
			if (dbcur != null)
				dbcur.close();
	
			return msg;
		} catch (Exception ex) {
			System.out.println(ex);
			throw new Exception("CartCntrlEtChiusure, Errore eseguendo una checkDtChDaCartGTDtApeDtChSoloSan()  ");
		}
	}	
	
	// gb 27/09/07 *******
	// Controlla se le date apertura e chiusura delle entit� che stanno sotto
	// la scheda valutazione sono maggiori della data di chiusura impostata
	// nella scheda valutazione. Se si, ritorna un messaggio, altrimenti, come
	// messaggio, ritorna la stringa vuota.
	public String checkDtChDaSkValGTDtApeDtCh(ISASConnection dbc, String strNCartella, String strDtApeSkVal,
			String strDtChSkVal) throws Exception {
		String strSelect = "";
		ISASCursor dbcur = null;
		ISASRecord dbr = null;
		String strTipoOperatore = "";
		String strNContatto = "";
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;

		try {
/**** 02/12/11: vorrebbero solo un msg di warning, ma la CariException fa solo msg bloccanti e quindi si toglie del tutto		
			// 04/03/11: controllo Autorizzazioni ADI/ADP/ADR --
			ar_fldNames = new String[] { "n_cartella" };
			ar_fldTypes = new String[] { "NUM" };
			ar_fldValues = new String[] { strNCartella };
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmmg_adi", ar_fldNames, ar_fldTypes, ar_fldValues, "skadi_data_inzio",
					"skadi_data_fine", strDtChSkVal, "Autorizzazioni ADI", "");
			if (!msg.equals(""))
				return msg;
				
			msg = checkDtChGTDtApeEtDtCh(dbc, "skmmg_adp", ar_fldNames, ar_fldTypes, ar_fldValues, "skadp_data_inizio",
					"skadp_data_fine", strDtChSkVal, "Autorizzazioni ADP", "");
			if (!msg.equals(""))
				return msg;

			msg = checkDtChGTDtApeEtDtCh(dbc, "skmmg_adr", ar_fldNames, ar_fldTypes, ar_fldValues, "skadr_data_inizio",
					"skadr_data_fine", strDtChSkVal, "Autorizzazioni ADR", "");
			if (!msg.equals(""))
				return msg;			
			// 04/03/11: controllo Autorizzazioni ADI/ADP/ADR --	
****/			
		
			// 08/10/08: Controllo Scheda PUAC ---
			ar_fldNames = new String[] { "n_cartella", "pr_data" };
			ar_fldTypes = new String[] { "NUM", "DTA" };
			ar_fldValues = new String[] { strNCartella, strDtApeSkVal };
			// 04/03/11 --
			String nmTabSkPuac = evUtl.getNmTabSkPuac(dbc);		
			boolean esisteFldChiu = evUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_data_chiusura");
			if (esisteFldChiu) { // 04/03/11 --
				msg = checkDtChGTDtApeEtDtCh(dbc, nmTabSkPuac, ar_fldNames, ar_fldTypes, ar_fldValues, "pr_data_puac",
					"pr_data_chiusura", strDtChSkVal, "Scheda PUAC", "");
				if (!msg.equals(""))
					return msg;
			}
			msg = checkDtChGTDtApeEtDtCh(dbc, nmTabSkPuac, ar_fldNames, ar_fldTypes, ar_fldValues, "pr_data_puac",
					"pr_data_verbale_uvm", strDtChSkVal, "Scheda PUAC", "");
			if (!msg.equals(""))
				return msg;
			// 08/10/08 ------------------------------
			
			// 04/03/11: controllo RisposteProgrammate --
			msg = checkDtChGTDtApeEtDtCh(dbc, "rv_puauvm_rispro", ar_fldNames, ar_fldTypes, ar_fldValues, "dt_ini",
					"dt_fine", strDtChSkVal, "Risposte programmate", "");
			if (!msg.equals(""))
				return msg;
			// 04/03/11: controllo RisposteProgrammate --			
					
			strSelect = "SELECT *" + " FROM progetto_cont" + " WHERE n_cartella = " + strNCartella + " AND pr_data = "
					+ formatDate(dbc, strDtApeSkVal);
			LOG.info("CartCntrlEtChiusure / checkDtChDaSkValGTDtApeDtCh / strSelect: " + strSelect);
			dbcur = dbc.startCursor(strSelect);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				ar_fldTypes = new String[] { "NUM", "NUM" };
				while (dbcur.next()) {
					dbr = dbcur.getRecord();
					strTipoOperatore = (String) dbr.get("prc_tipo_op");
					strNContatto = ((Integer) dbr.get("prc_n_contatto")).toString();
					ar_fldValues = new String[] { strNCartella, strNContatto };
					// Controllo Prestazioni erogate
					ar_fldNames = new String[] { "int_cartella", "*" };
					msg = checkDtGTDtInterv(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChSkVal, strTipoOperatore);
					if (!msg.equals(""))
						break;

					// Controllo Progetti/Contatti
					if (strTipoOperatore.equals("01")) {
						ar_fldNames = new String[] { "n_cartella", "n_progetto" };
						msg = checkDtChGTDtApeEtDtCh(dbc, "ass_progetto", ar_fldNames, ar_fldTypes, ar_fldValues,
								"ap_data_apertura", "ap_data_chiusura", strDtChSkVal, "Progetti", strTipoOperatore);
					} else {
						ar_fldNames = new String[] { "n_cartella", "n_contatto" };
						if (strTipoOperatore.equals("02"))
							msg = checkDtChGTDtApeEtDtCh(dbc, "skinf", ar_fldNames, ar_fldTypes, ar_fldValues,
									"ski_data_apertura", "ski_data_uscita", strDtChSkVal, "Contatti", strTipoOperatore);
						else if (strTipoOperatore.equals("03"))
							msg = checkDtChGTDtApeEtDtCh(dbc, "skmedico", ar_fldNames, ar_fldTypes, ar_fldValues,
									"skm_data_apertura", "skm_data_chiusura", strDtChSkVal, "Contatti",
									strTipoOperatore);
						else if (strTipoOperatore.equals("04"))
							msg = checkDtChGTDtApeEtDtCh(dbc, "skfis", ar_fldNames, ar_fldTypes, ar_fldValues,
									"skf_data", "skf_data_chiusura", strDtChSkVal, "Contatti", strTipoOperatore);
						else if (strTipoOperatore.equals("52"))
							msg = checkDtChGTDtApeEtDtCh(dbc, "skmedpal", ar_fldNames, ar_fldTypes, ar_fldValues,
									"skm_data_apertura", "skm_data_chiusura", strDtChSkVal, "Contatti",
									strTipoOperatore);
					}
					if (!msg.equals(""))
						break;

					ar_fldNames = new String[] { "n_cartella", "n_progetto" };
					// Controllo Piani assistenziali
					msg = checkDtChGTDtApeEtDtCh(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues,
							"pa_data", "pa_data_chiusura", strDtChSkVal, "Piani Assistenziali", "");
					if (!msg.equals(""))
						break;

					// Controllo Piani accessi
					msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues,
							"pi_data_inizio", "pi_data_fine", strDtChSkVal, "Piani Accessi", "");
					if (!msg.equals(""))
						break;

					if (strTipoOperatore.equals("01")) {
						// Controllo Obiettivi
						msg = checkDtChGTDtApeEtDtCh(dbc, "ass_obbiettivi", ar_fldNames, ar_fldTypes, ar_fldValues,
								"ob_data_ins", "ob_data_ragg", strDtChSkVal, "Obiettivi", "01");
						if (!msg.equals(""))
							break;
						// Controllo Interventi
						msg = checkDtChGTDtApeEtDtCh(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues,
								"int_data_ins", "int_data_concl", strDtChSkVal, "Interventi", "01");
						if (!msg.equals(""))
							break;

						/***
						 * 06/02/08 *** //Controllo Dettagli Interventi msg =
						 * checkDtChDettIntervDtCh(dbc, ar_fldNames,
						 * ar_fldTypes, ar_fldValues, strDtChSkVal); if
						 * (!msg.equals("")) break; 06/02/08
						 ***/

						// Controllo Verifiche
						msg = checkDtChGTDtApeEtDtCh(dbc, "ass_verifica", ar_fldNames, ar_fldTypes, ar_fldValues,
								"ver_data", "ver_data_chiusa", strDtChSkVal, "Verifiche", "01");
						if (!msg.equals(""))
							break;
					}
				}
			}
			if (dbcur != null)
				dbcur.close();

			return msg;
		} catch (Exception ex) {
			LOG.error(ex);
			throw new Exception("CartCntrlEtChiusure, Errore eseguendo una checkDtChDaSkValGTDtApeDtCh()  ");
		}
	}

	// gb 27/09/07 *******
	// Controlla se le date apertura e chiusura delle entit� che stanno sotto
	// il contatto o il progetto sono maggiori della data di chiusura impostata
	// nel contatto o progetto. Se si, ritorna un messaggio, altrimenti, come
	// messaggio, ritorna la stringa vuota.
	public String checkDtChDaContProgGTDtApeDtCh(ISASConnection dbc, String strNCartella, String strNContOrProg,
			String strDtChContOrProg, String strTipoOperatore) throws Exception {
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;

		ar_fldTypes = new String[] { "NUM", "NUM" };
		ar_fldValues = new String[] { strNCartella, strNContOrProg };

		// Controllo Prestazioni erogate
		ar_fldNames = new String[] { "int_cartella", "*" };
		msg = checkDtGTDtInterv(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChContOrProg, strTipoOperatore);
		if (!msg.equals(""))
			return msg;

		// Controllo Piani assistenziali
		ar_fldNames = new String[] { "n_cartella", "n_progetto", "pa_tipo_oper" };
		ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
		ar_fldValues = new String[] { strNCartella, strNContOrProg, strTipoOperatore };
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues, "pa_data",
				"pa_data_chiusura", strDtChContOrProg, "Piani Assistenziali", "");
		if (!msg.equals(""))
			return msg;

		// Controllo Piani accessi
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues, "pi_data_inizio",
				"pi_data_fine", strDtChContOrProg, "Piani Accessi", "");
		if (!msg.equals(""))
			return msg;

		// Controllo Obiettivi/Interventi/Verifiche: solo per Assistenza Sociale
		/* BOFFA: NON CONTROLLO LA CHIUSURA DI ASS_OBBIETTIVI, ASS_INTERVENTI 
		if (strTipoOperatore.equals("01")) {
			ar_fldNames = new String[] { "n_cartella", "n_progetto" };
			ar_fldTypes = new String[] { "NUM", "NUM" };
			ar_fldValues = new String[] { strNCartella, strNContOrProg };
			// Controllo Obiettivi
			msg = checkDtChGTDtApeEtDtCh(dbc, "ass_obbiettivi", ar_fldNames, ar_fldTypes, ar_fldValues, "ob_data_ins",
					"ob_data_ragg", strDtChContOrProg, "Obiettivi", "01");
			if (!msg.equals(""))
				return msg;
			// Controllo Interventi
			msg = checkDtChGTDtApeEtDtCh(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, "int_data_ins",
					"int_data_concl", strDtChContOrProg, "Interventi", "01");
			if (!msg.equals(""))
				return msg;

			/ ***
			 * 06/02/08 *** //Controllo Dettagli Interventi msg =
			 * checkDtChDettIntervDtCh(dbc, ar_fldNames, ar_fldTypes,
			 * ar_fldValues, strDtChContOrProg); if (!msg.equals("")) return
			 * msg; 06/02/08
			 *** /

			// Controllo Verifiche
			// msg = checkDtChGTDtApeEtDtCh(dbc, "ass_verifica", ar_fldNames,
			// ar_fldTypes, ar_fldValues, "ver_data", "ver_data_chiusa",
			// strDtChContOrProg, "Verifiche", "01");
			// if (!msg.equals(""))
			// return msg;
		}
		*/
		return msg;
	}

	// gb 27/09/07 *******
	// Controlla se le date apertura e chiusura delle entit� che stanno sotto
	// l'obiettivo sono maggiori della data di chiusura impostata
	// nell'obiettivo. Se si, ritorna un messaggio, altrimenti, come
	// messaggio, ritorna la stringa vuota.
	public String checkDtChDaObieGTDtApeDtCh(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strDtChObiettivo) throws Exception {
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;

		ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
		ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo };

		// Controllo Prestazioni erogate
		ar_fldNames = new String[] { "int_cartella", "n_progetto", "cod_obbiettivo" };
		msg = checkDtGTDtInterv(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChObiettivo, "01");
		if (!msg.equals(""))
			return msg;

		// Controllo Piani assistenziali
		ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo", "pa_tipo_oper" };
		ar_fldTypes = new String[] { "NUM", "NUM", "STR", "STR" };
		ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, "01" };
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues, "pa_data",
				"pa_data_chiusura", strDtChObiettivo, "Piani Assistenziali", "");
		if (!msg.equals(""))
			return msg;

		// Controllo Piani accessi
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues, "pi_data_inizio",
				"pi_data_fine", strDtChObiettivo, "Piani Accessi", "");
		if (!msg.equals(""))
			return msg;

		// Controllo Interventi
		ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo" };
		ar_fldTypes = new String[] { "NUM", "NUM", "STR" };
		ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo };
		msg = checkDtChGTDtApeEtDtCh(dbc, "ass_interventi", ar_fldNames, ar_fldTypes, ar_fldValues, "int_data_ins",
				"int_data_concl", strDtChObiettivo, "Interventi", "01");
		if (!msg.equals(""))
			return msg;

		/***
		 * 06/02/08 *** //Controllo Dettagli Interventi msg =
		 * checkDtChDettIntervDtCh(dbc, ar_fldNames, ar_fldTypes, ar_fldValues,
		 * strDtChObiettivo); 06/02/08
		 ***/
		return msg;
	}

	// gb 27/09/07 *******
	// Controlla se le date apertura e chiusura delle entit� che stanno sotto
	// l'intervento sono maggiori della data di chiusura impostata
	// nell'intervento. Se si, ritorna un messaggio, altrimenti, come
	// messaggio, ritorna la stringa vuota.
	public String checkDtChDaIntervGTDtApeDtCh(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strNIntervento, String strDtChIntervento) throws Exception {
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;

		ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM" };
		ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento };

		// Controllo Prestazioni erogate
		ar_fldNames = new String[] { "int_cartella", "n_progetto", "cod_obbiettivo", "n_intervento" };
		msg = checkDtGTDtInterv(dbc, ar_fldNames, ar_fldTypes, ar_fldValues, strDtChIntervento, "01");
		if (!msg.equals(""))
			return msg;

		// Controllo Piani assistenziali
		ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo", "n_intervento", "pa_tipo_oper" };
		ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM", "STR" };
		ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento, "01" };
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_assist", ar_fldNames, ar_fldTypes, ar_fldValues, "pa_data",
				"pa_data_chiusura", strDtChIntervento, "Piani Assistenziali", "");
		if (!msg.equals(""))
			return msg;

		// Controllo Piani accessi
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues, "pi_data_inizio",
				"pi_data_fine", strDtChIntervento, "Piani Accessi", "");
		if (!msg.equals(""))
			return msg;

		// Controllo Dettagli Interventi
		/***
		 * 06/02/08 *** ar_fldNames = new String[] {"n_cartella", "n_progetto",
		 * "cod_obbiettivo", "n_intervento"}; ar_fldTypes = new String[] {"NUM",
		 * "NUM", "STR", "NUM"}; ar_fldValues = new String[] {strNCartella,
		 * strNProgetto, strCodObiettivo, strNIntervento}; msg =
		 * checkDtChDettIntervDtCh(dbc, ar_fldNames, ar_fldTypes, ar_fldValues,
		 * strDtChIntervento); 06/02/08
		 ***/
		return msg;
	}

	// gb 03/10/07 *******
	// Controlla se le date apertura delle entit� del dettaglio interventi
	// sono maggiori della data di chiusura impostata
	// Se si, ritorna un messaggio, altrimenti, come messaggio, ritorna la
	// stringa vuota.
	private String checkDtChDettIntervDtCh(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura) throws Exception {
		String msg = "";
		String[] arDettIntervTableNames = new String[] { "ass_coppie", "ass_minori", "ass_punteggio", "ass_contrib",
				"ass_asdodi", "ass_asdoed", "ass_malvio", "ass_ricoveri" };
		String[] arDettIntervMsgs = new String[] { "Assistenza Coppie", "Assistenza minori", "Lista attesa RSA",
				"Contributi", "Assist. domiciliare diretta", "Assist. domiciliare educativa", "Maltratt. e violenze",
				"Gestione RSA" };
		for (int i = 0; i < arDettIntervTableNames.length; i++) {
			msg = checkDtChGTDtApeEtDtCh(dbc, arDettIntervTableNames[i], ar_fldNames, ar_fldTypes, ar_fldValues,
					"data", "", strDtChiusura, arDettIntervMsgs[i], "");
			if (!msg.equals(""))
				return msg;
		}
		return msg;
	}

	// gb 27/09/07 *******
	// Controlla se le date apertura e chiusura delle entit� che stanno sotto
	// il piano assistenziale sono maggiori della data di chiusura impostata
	// nel piano assistenziale. Se si, ritorna un messaggio, altrimenti, come
	// messaggio, ritorna la stringa vuota.
	// In questo caso si controlla solo la data di apertura del piano_accessi
	public String checkDtChDaPianoAssGTDtApeDtCh(ISASConnection dbc, String strNCartella, String strNProgetto,
			String strCodObiettivo, String strNIntervento, String strDtApePianoAss, String strTipoOperatore,
			String strDtChPianoAss) throws Exception {
		String msg = "";
		String[] ar_fldNames = null;
		String[] ar_fldValues = null;
		String[] ar_fldTypes = null;

		ar_fldTypes = new String[] { "NUM", "NUM", "STR", "NUM", "DTA", "STR" };
		ar_fldValues = new String[] { strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strDtApePianoAss,
				strTipoOperatore };
		ar_fldNames = new String[] { "n_cartella", "n_progetto", "cod_obbiettivo", "n_intervento", "pa_data",
				"pa_tipo_oper" };

		// Controllo Piani accessi
		msg = checkDtChGTDtApeEtDtCh(dbc, "piano_accessi", ar_fldNames, ar_fldTypes, ar_fldValues, "pi_data_inizio",
				"pi_data_fine", strDtChPianoAss, "Piani Accessi", "");

		return msg;
	}

	private String checkDtChGTDtApeEtDtCh(ISASConnection dbc, String strNomeTabella, String[] ar_fldNames,
			String[] ar_fldTypes, String[] ar_fldValues, String strNomeFldDtApe, String strNomeFldDtCh,
			String strDtToCompare, String strEntity, String strTipoOper) throws Exception {
		ISASCursor dbcur = null;
		String strTipoOperDesc = "";
		String msg = "";
		String strCommonSelect = "";
		String strDtClausole = "";
		String strSelect = "";
		ISASRecord dbr = null;
		String methodName = "CartCntrlEtChiusure";

		try {
			// Controllo se esistono rec. con data apertura > data_confronto
			strCommonSelect = getSelectQuery(dbc, strNomeTabella, ar_fldNames, ar_fldTypes, ar_fldValues);
			strDtClausole = " AND " + strNomeFldDtApe + " > " + formatDate(dbc, strDtToCompare);
			strSelect = strCommonSelect + strDtClausole;
			LOG.info("CartCntrlEtChiusure / checkDtChGTDtApeEtDtCh (Apertura) / strSelect: " + strSelect);
			dbcur = dbc.startCursor(strSelect);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				dbcur.next();
				dbr = dbcur.getRecord();
				if (strEntity.equals("Piani Assistenziali") || strEntity.equals("Piani Accessi")) {
					strTipoOper = (String) dbr.get("pa_tipo_oper");
				}
				String dtapertura = ISASUtil.getValoreStringa(dbr,strNomeFldDtApe);
				if (!strEntity.equals("Scheda Valutazione")) {
					strTipoOperDesc = getTipoOperDesc(strTipoOper);
					msg = "Attenzione esistono " + strEntity + " di " + strTipoOperDesc
							+ " per la cartella " + dbr.get("n_cartella").toString() + ", \n" // 11/02/13
							+ " la cui data di apertura ("+dtapertura+") e' successiva alla data chiusura specificata.";
				} else
					msg = "Attenzione la data di apertura della Scheda Valutazione"
							+ " per la cartella " + dbr.get("n_cartella").toString() + ", \n" // 11/02/13
							+ " e' successiva alla data chiusura specificata.";
				if (dbcur != null)
					dbcur.close();
				return msg;
			}

			if (dbcur != null)
				dbcur.close();
			dbcur = null;

			// gb 25/09/07: Per il Piano Accessi si controllano solo le date di
			// apertura (inizio).
			if (strEntity.equals("Piani Accessi") || strNomeFldDtCh.equals("")) {
				return msg;
			}

			// Controllo se esistono rec. con data chiusura > data_confronto
			// 06/02/08: si devono controllare i dettagli intervento che
			// dipendono dal tipo
			if (strEntity.equals("Interventi")) {
				String critProg = "";
				String critObb = "";
				String critInterv = "";
				if (ar_fldValues.length > 1)
					critProg = " AND i.n_progetto = " + ar_fldValues[1];
				if (ar_fldValues.length > 2)
					critObb = " AND i.cod_obbiettivo = '" + ar_fldValues[2] + "'";
				if (ar_fldValues.length > 3)
					critInterv = " AND i.n_intervento = " + ar_fldValues[3];

				strSelect = "SELECT * FROM ass_interventi i, ass_interventi_dett d" + " WHERE i.n_cartella = "
						+ ar_fldValues[0] + critProg + critObb + critInterv + " AND i.n_cartella = d.n_cartella"
						+ " AND i.n_progetto = d.n_progetto" + " AND i.cod_obbiettivo = d.cod_obbiettivo"
						+ " AND i.n_intervento = d.n_intervento";

				strNomeFldDtCh = "d.data_concl";
			} else
				strSelect = strCommonSelect;

			strDtClausole = " AND " + strNomeFldDtCh + " > " + formatDate(dbc, strDtToCompare) + " AND "
					+ strNomeFldDtCh + " IS NOT NULL";

			strSelect += strDtClausole;
			LOG.info("CartCntrlEtChiusure / checkDtChGTDtApeEtDtCh (Chiusura) / strSelect: " + strSelect);
			dbcur = dbc.startCursor(strSelect);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				dbcur.next();
				dbr = dbcur.getRecord();
				if (strEntity.equals("Piani Assistenziali")) {
					strTipoOper = (String) dbr.get("pa_tipo_oper");
				}
				if (!strEntity.equals("Scheda Valutazione")) {
					strTipoOperDesc = getTipoOperDesc(strTipoOper);
					msg = "Attenzione esistono " + strEntity + " di " + strTipoOperDesc
							+ " per la cartella " + dbr.get("n_cartella").toString() + ", \n" // 11/02/13					
							+ " la cui data di chiusura e' successiva alla data chiusura specificata.";
				} else
					msg = "Attenzione la data di chiusura della Scheda Valutazione"
							+ " per la cartella " + dbr.get("n_cartella").toString() + ", \n" // 11/02/13
							+ " e' successiva alla data chiusura specificata.";
				if (dbcur != null)
					dbcur.close();
				return msg;
			}
			if (dbcur != null)
				dbcur.close();
			return msg;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(methodName, dbcur);
		}
//		catch (Exception ex) {
//			System.out.println(ex);
//			throw new Exception("CartCntrlEtChiusure, Errore eseguendo una checkDtChGTDtApeEtDtCh()  ");
//		}
	}

	/*
	 * gb 28/09/07 ******* public String checkDtChGTDtApeEtDtCh( String
	 * strNomeTabella, String strNomeFldDtApe, String strNomeFldDtCh, String
	 * strNCartella, String strDtToCompare, ISASConnection dbc, String
	 * strEntity, String strTipoOper) throws SQLException { ISASCursor dbcur =
	 * null; String strTipoOperDesc = ""; String msg = ""; ISASRecord dbr =
	 * null; try { String mySel = "SELECT *" + " FROM " + strNomeTabella +
	 * " WHERE n_cartella = " + strNCartella + " AND " + strNomeFldDtApe + " > "
	 * + formatDate(dbc, strDtToCompare) + " AND " + strNomeFldDtCh +
	 * " IS NULL";
	 * 
	 * System.out.println(
	 * "CartCntrlEtChiusure / checkDtChGTDtApeEtDtCh (Apertura) / mySel: " +
	 * mySel); dbcur = dbc.startCursor(mySel);
	 * 
	 * if ((dbcur != null) && (dbcur.getDimension() >0)) { if
	 * (strEntity.equals("Piani Assistenziali") ||
	 * strEntity.equals("Piani Accessi")) { dbcur.next(); dbr =
	 * dbcur.getRecord(); strTipoOper = (String)dbr.get("pa_tipo_oper"); } if
	 * (!strEntity.equals("Scheda Valutazione")) { strTipoOperDesc =
	 * getTipoOperDesc(strTipoOper); msg = "Attenzione esistono " + strEntity +
	 * " di " + strTipoOperDesc +
	 * " la cui data di apertura � successiva alla data chiusura specificata.";
	 * } else msg =
	 * "Attenzione la data di apertura della Scheda Valutazione � successiva alla data chiusura specificata."
	 * ; if (dbcur != null) dbcur.close(); return msg; }
	 * 
	 * if (dbcur != null) dbcur.close(); dbcur = null;
	 * 
	 * //gb 25/09/07: Per il Piano Accessi si controllano solo le date di
	 * apertura (inizio). if (strEntity.equals("Piani Accessi")) { return msg; }
	 * 
	 * mySel = "SELECT *" + " FROM " + strNomeTabella + " WHERE n_cartella = " +
	 * strNCartella + " AND " + strNomeFldDtCh + " > " + formatDate(dbc,
	 * strDtToCompare) + " AND " + strNomeFldDtCh + " IS NOT NULL";
	 * 
	 * System.out.println(
	 * "CartCntrlEtChiusure / checkDtChGTDtApeEtDtCh (Chiusura) / mySel: " +
	 * mySel); dbcur = dbc.startCursor(mySel);
	 * 
	 * if ((dbcur != null) && (dbcur.getDimension() >0)) { if
	 * (strEntity.equals("Piani Assistenziali")) { dbcur.next(); dbr =
	 * dbcur.getRecord(); strTipoOper = (String)dbr.get("pa_tipo_oper"); } if
	 * (!strEntity.equals("Scheda Valutazione")) { strTipoOperDesc =
	 * getTipoOperDesc(strTipoOper); msg = "Attenzione esistono " + strEntity +
	 * " di " + strTipoOperDesc +
	 * " la cui data di chiusura � successiva alla data chiusura specificata.";
	 * } else msg =
	 * "Attenzione la data di chiusura della Scheda Valutazione � successiva alla data chiusura specificata."
	 * ; if (dbcur != null) dbcur.close(); return msg; } if (dbcur != null)
	 * dbcur.close(); return msg; } catch (Exception ex) {
	 * System.out.println(ex); throw newSQLException(
	 * "CartCntrlEtChiusure, Errore eseguendo una checkDtChGTDtApeEtDtCh()  ");
	 * } }gb 28/09/07: fine ******
	 */

	private String getTipoOperDesc(String strTipoOper) {
		String strTipoOperDesc = "";

		if (strTipoOper.equals("01"))
			strTipoOperDesc = "Assistenza Sociale";
		else if (strTipoOper.equals("02"))
			strTipoOperDesc = "Infermieri";
		else if (strTipoOper.equals("03"))
			strTipoOperDesc = "Medici";
		else if (strTipoOper.equals("04"))
			strTipoOperDesc = "Fisioterapisti";
		else if (strTipoOper.equals("52"))
			strTipoOperDesc = "Oncologi";

		return strTipoOperDesc;
	}

	private String checkDtGTDtInterv(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtToCompare, String strTipoOperatore) throws Exception {
		String mySelect = "SELECT * FROM interv";
		String strWHERE_AND = " WHERE ";
		String msg = "";

		try {
			for (int i = 0; i < ar_fldNames.length; i++) {
				String strApice = (ar_fldTypes[i].equals("STR") ? "'" : "");
				if (ar_fldNames[i].equals("*")) {
					if (strTipoOperatore.equals("01"))
						ar_fldNames[i] = "n_progetto";
					else
						ar_fldNames[i] = "int_contatto";
				}
				mySelect += strWHERE_AND + ar_fldNames[i] + " = " + strApice + ar_fldValues[i] + strApice;
				strWHERE_AND = " AND ";
			}

			if (strTipoOperatore.equals("01"))
				mySelect += " AND int_contatto = 0";
			else if (!strTipoOperatore.equals(""))
				mySelect += " AND n_progetto = 0";

			if (!strTipoOperatore.equals(""))
				mySelect += " AND int_tipo_oper = '" + strTipoOperatore + "'";

			mySelect += " AND int_data_prest > " + formatDate(dbc, strDtToCompare) + " AND int_data_prest IS NOT NULL";

			LOG.info("CartCntrlEtChiusure/checkDtGTDtInterv, mySelect: " + mySelect);
			// Leggo i record
			ISASCursor dbcur = dbc.startCursor(mySelect);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				msg = "Attenzione, ci sono delle prestazioni erogate in data successiva alla data di chiusura impostata.";
				if (dbcur != null)
					dbcur.close();
				return msg;
			}
			if (dbcur != null)
				dbcur.close();
			return msg;
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una checkDtGTDtInterv()  ");
		}
	}

	private String checkDtChCartGTDtInterv(ISASConnection dbc, String strNCartella, String strDtToCompare)
			throws Exception {
		ISASCursor dbcur = null;
		String msg = "";
		try {
			String mySel = "SELECT *" + " FROM interv" + " WHERE int_cartella = " + strNCartella
					+ " AND int_data_prest > " + formatDate(dbc, strDtToCompare) + " AND int_data_prest IS NOT NULL";
			LOG.info("CartCntrlEtChiusure / checkDtChCartGTDtInterv / mySel: " + mySel);
			dbcur = dbc.startCursor(mySel);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				msg = "Attenzione, ci sono delle prestazioni erogate in data successiva alla data di chiusura impostata.";
				if (dbcur != null)
					dbcur.close();
				return msg;
			}
			if (dbcur != null)
				dbcur.close();
			return msg;
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una checkDtChCartGTDtInterv()  ");
		}
	}

	/*
	 * gb 26/09/07 ******* public void AggiornaData(String tabella, String
	 * cartella,String data_tabella,String data_chiusura,String
	 * data_ini_tab,ISASConnection dbc) throws SQLException{ try { String mysel
	 * = "SELECT * FROM " + tabella + " WHERE "+ "n_cartella =" + cartella ;
	 * debugMessage("CartCntrlEtChiusure/AggiornaData mysel: " + mysel);
	 * ISASCursor dbcur=dbc.startCursor(mysel); Vector
	 * vdbr=dbcur.getAllRecord(); for(Enumeration
	 * senum=vdbr.elements();senum.hasMoreElements(); ) { ISASRecord
	 * dbr=(ISASRecord)senum.nextElement(); String
	 * data_inizio=((java.sql.Date)dbr.get(data_ini_tab)).toString();
	 * System.out.println(data_ini_tab +" "+data_inizio); String sel =
	 * "SELECT * FROM " + tabella + " WHERE "+ "n_cartella = " + cartella +
	 * " AND "+ "n_contatto = " + (Integer)dbr.get("n_contatto") + " AND "+ //
	 * data_ini_tab + " = '" + data_inizio+"'"; data_ini_tab + " = " +
	 * formatDate(dbc,data_inizio);
	 * debugMessage("CartCntrlEtChiusure/AggiornaData sel: " + sel); ISASRecord
	 * dbrDett=dbc.readRecord(sel); if(dbrDett.get(data_tabella)==null)// &&
	 * (data_inizio!=null && !(data_inizio.equals(""))))
	 * dbrDett.put(data_tabella,data_chiusura); dbc.writeRecord(dbrDett);
	 * }//fine for dbcur.close(); } catch (Exception ex) {
	 * System.out.println(ex); throw new
	 * SQLException("CartCntrlEtChiusure, Errore eseguendo una AggiornaData()  "
	 * ); } }gb 26/09/07: fine ******
	 */

	/*
	 * gb 26/09/07 ******* public void AggiornaDataContsan(String
	 * cartella,String data_chiusura,ISASConnection dbc) throws SQLException{
	 * try { String mysel = "SELECT * FROM contsan WHERE "+ "n_cartella =" +
	 * cartella ; debugMessage("CartCntrlEtChiusure/AggiornaDataContsan mysel: "
	 * + mysel); ISASCursor dbcur=dbc.startCursor(mysel); Vector
	 * vdbr=dbcur.getAllRecord(); for(Enumeration
	 * senum=vdbr.elements();senum.hasMoreElements(); ) { ISASRecord
	 * dbr=(ISASRecord)senum.nextElement(); String sel =
	 * "SELECT * FROM contsan WHERE "+ "n_cartella = " + cartella + " AND "+
	 * "n_contatto = " + (Integer)dbr.get("n_contatto");
	 * debugMessage("CartCntrlEtChiusure/AggiornaDataContsan sel: " + sel);
	 * ISASRecord dbrDett=dbc.readRecord(sel);
	 * if(dbrDett.get("data_chius_medico")==null &&
	 * dbrDett.get("data_medico")!=null)
	 * dbrDett.put("data_chius_medico",data_chiusura);
	 * if(dbrDett.get("data_chius_infer")==null &&
	 * dbrDett.get("data_infer")!=null)
	 * dbrDett.put("data_chius_infer",data_chiusura);
	 * if(dbrDett.get("data_chius_sociale")==null &&
	 * dbrDett.get("data_sociale")!=null)
	 * dbrDett.put("data_chius_sociale",data_chiusura);
	 * if(dbrDett.get("data_chius_fisiot")==null &&
	 * dbrDett.get("data_fisiot")!=null)
	 * dbrDett.put("data_chius_fisiot",data_chiusura); dbc.writeRecord(dbrDett);
	 * }//fine for dbcur.close(); } catch (Exception ex) {
	 * System.out.println(ex); throw newSQLException(
	 * "CartCntrlEtChiusure, Errore eseguendo una AggiornaDataContsan()  "); } }
	 * gb 26/09/07: fine ******
	 */

	private void chiudoSchedaValutazione(ISASConnection dbc, String strNCartella, String strDtChiusura, String motivo)
			throws Exception {
		try {
			String[] ar_fldNames = new String[] { "n_cartella" };
			String[] ar_fldTypes = new String[] { "NUM" };
			String[] ar_fldValues = new String[] { strNCartella };
			String strSelect = getSelectQuery(dbc, "progetto", ar_fldNames, ar_fldTypes, ar_fldValues);
			strSelect += " AND ((pr_data_chiusura IS NULL) OR (pr_data_chiusura > " + formatDate(dbc, strDtChiusura) + "))";
			debugMessage("CartCntrlEtChiusure/chiudoSchedaValutazione strSelect: " + strSelect);
			ISASRecord dbr = dbc.readRecord(strSelect);
			if (dbr != null) {
				dbr.put("pr_data_chiusura", strDtChiusura);
				if (motivo != null) {
					if (motivo.equals("1")) // trasferimento
						dbr.put("pr_motivi_val_ch", "6");
					else if (motivo.equals("2")) // decesso
						dbr.put("pr_motivi_val_ch", "7");
				}
				dbc.writeRecord(dbr);
			}

		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoSchedaValutazione()  ", ex);
			}
		}
	}

	// 19/04/10: chiusura segnalazioni PuntoInsieme non ancora prese in carico
	private void chiudoAssAnagrafica(ISASConnection dbc, String strNCartella, String strDtChiusura, String motivo)
	throws Exception 
	{
		ISASCursor dbcur = null;
		try {
			String[] ar_fldNames = new String[] { "n_cartella" };
			String[] ar_fldTypes = new String[] { "NUM" };
			String[] ar_fldValues = new String[] { strNCartella };
			String strSelect = getSelectQuery(dbc, "ass_anagrafica", ar_fldNames, ar_fldTypes, ar_fldValues);
			String altriCrit = " AND ((chiusura_data IS NULL) OR (chiusura_data > " + formatDate(dbc, strDtChiusura) + "))" +
						" AND presa_carico = 'N'" +
						" AND soc_carico = 'N'" +
						" AND san_carico = 'N'";
			strSelect += altriCrit;			
			debugMessage("CartCntrlEtChiusure/chiudoAssAnagrafica strSelect x cartella: " + strSelect);
			dbcur = dbc.startCursor(strSelect);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr1 = dbcur.getRecord();
					String prog = "" + dbr1.get("progressivo");
					String strSelect2 = getSelectQuery(dbc, "ass_anagrafica", new String[] {"progressivo"}, ar_fldTypes, new String[] {prog});
					debugMessage("CartCntrlEtChiusure/chiudoAssAnagrafica strSelect x progressivo: " + strSelect2);
					ISASRecord dbr2 = dbc.readRecord(strSelect2);	
					dbr2.put("presa_carico_data", strDtChiusura);
					dbr2.put("presa_carico", "C");
					if (motivo != null) {
						if (motivo.equals("1")) // trasferimento --> rinuncia
							dbr2.put("presa_carico_motivo", "3");
						else if (motivo.equals("2")) // decesso
							dbr2.put("presa_carico_motivo", "1");
					}
					dbc.writeRecord(dbr2);
				}
			}
			if (dbcur != null)
				dbcur.close();				
		}catch (Exception ex){
			close_dbcur_nothrow("chiudoAssAnagrafica", dbcur);
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new Exception("CartCntrlEtChiusure, Errore eseguendo una chiudoAssAnagrafica()  ", ex);
			}
		}
	}	
	
	private String checkDtGTDtIntMmg(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtToCompare, String strTipoOperatore) throws Exception {
		String mySelect = "SELECT * FROM intmmg";
		String strWHERE_AND = " WHERE ";
		String msg = "";

		try {
			for (int i = 0; i < ar_fldNames.length; i++) {
				String strApice = (ar_fldTypes[i].equals("STR") ? "'" : "");
				mySelect += strWHERE_AND + ar_fldNames[i] + " = " + strApice + ar_fldValues[i] + strApice;
				strWHERE_AND = " AND ";
			}

			mySelect += " AND int_data > " + formatDate(dbc, strDtToCompare) + " AND int_data IS NOT NULL";

			System.out.println("CartCntrlEtChiusure/checkDtGTDtIntMmg, mySelect: " + mySelect);
			// Leggo i record
			ISASCursor dbcur = dbc.startCursor(mySelect);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				msg = "Attenzione, ci sono delle prestazioni erogate da MMG in data successiva alla data di chiusura impostata.";
				if (dbcur != null)
					dbcur.close();
				return msg;
			}
			if (dbcur != null)
				dbcur.close();
			return msg;
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una checkDtGTDtIntMmg()  ");
		}
	}
	
	// 27/04/10
	private void chiudoCaso(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura, String motivo, String strCodOperChiusura) throws Exception {
		ISASCursor  dbcur = null;
		try {
			String strSelect = getSelectQuery(dbc, "caso", ar_fldNames, ar_fldTypes, ar_fldValues);
			debugMessage("CartCntrlEtChiusure/chiudoCaso strSelect: " + strSelect);
			dbcur = dbc.startCursor(strSelect);
			
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr1 = dbcur.getRecord();
					String strNCartella =  "" + dbr1.get("n_cartella");
					String strDataApertura = "" + dbr1.get("pr_data");
// 12/09/12			gestore_casi.chiudiCasoProgetto(dbc, strNCartella, strDataApertura, strDtChiusura, strCodOperChiusura);
					gestore_casi.chiudiCasoProgetto(dbc, strNCartella, strDataApertura, strDtChiusura, motivo, strCodOperChiusura);					
				}
			}
			if (dbcur != null)
				dbcur.close();				
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoCaso()  ", ex);
			}
		} finally {
			close_dbcur_nothrow("chiudoCaso", dbcur);
		}
	}
	
	
	// 08/10/08
	private void chiudoSchedaPuac(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura, String motivo) throws Exception {
		try {
// 04/03/11	String strSelect = getSelectQuery(dbc, "puauvm", ar_fldNames, ar_fldTypes, ar_fldValues);
			// 04/03/11 --
			String nmTabSkPuac = evUtl.getNmTabSkPuac(dbc);
			String strSelect = getSelectQuery(dbc, nmTabSkPuac, ar_fldNames, ar_fldTypes, ar_fldValues);
			// 04/03/11 --
		
			strSelect += " AND ((pr_data_verbale_uvm IS NULL) OR (pr_data_verbale_uvm > " + formatDate(dbc, strDtChiusura) + "))";
			
			// 04/03/11 --
			boolean esisteFldChiu = evUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_data_chiusura");
			if (esisteFldChiu)// 04/03/11 --
				strSelect += " AND ((pr_data_chiusura IS NULL) OR (pr_data_chiusura > " + formatDate(dbc, strDtChiusura) + "))";
			
			debugMessage("CartCntrlEtChiusure/chiudoSchedaPuac strSelect: " + strSelect);
			ISASRecord dbr = dbc.readRecord(strSelect);
			if (dbr != null) {
				if (esisteFldChiu) {
					dbr.put("pr_data_chiusura", strDtChiusura);
					if (motivo != null) {
						if ((motivo.equals("1")) || (motivo.equals("6"))) // trasferimento
							dbr.put("pr_motivo_chiusura", "3");
						else if ((motivo.equals("2")) || (motivo.equals("7"))) // decesso
							dbr.put("pr_motivo_chiusura", "4");
					}
				}
				
				// 13/01/09: si ripulisce eventuale convocazione in sedutaUVM
				if ((dbr.get("pr_stato_convoc") != null) && (((String) dbr.get("pr_stato_convoc")).trim().equals("2"))) {
					dbr.put("pr_stato_convoc", "0");
					dbr.put("pr_data_seduta", (java.sql.Date) null);
					dbr.put("pr_cod_comm", (String) null);
					dbr.put("pr_sede", (String) null);
					dbr.put("pr_centro_soc", (String) null);
					dbr.put("pr_ora", new Integer(0));
					dbr.put("pr_ordine", new Integer(0));
				}
				// 13/01/09 ---

				dbc.writeRecord(dbr);
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoSchedaPuac()  ", ex);
			}
		}
	}

	/*
	 * gb 27/09/07 ******* public void aggiornaDataChiusSkVal(String
	 * cartella,String data_chiusura,ISASConnection dbc,String motivo) throws
	 * SQLException{ try { String mysel = "SELECT * FROM progetto WHERE "+
	 * "n_cartella =" + cartella + " and pr_data_chiusura is null";
	 * debugMessage("CartCntrlEtChiusure/aggiornaDataChiusSkVal mysel: " +
	 * mysel); ISASRecord dbr=dbc.readRecord(mysel); if (dbr!=null){
	 * dbr.put("pr_data_chiusura",data_chiusura); if(motivo!=null){
	 * if(motivo.equals("1")) dbr.put("pr_motivi_val_ch","6"); else
	 * if(motivo.equals("2")) dbr.put("pr_motivi_val_ch","7"); }
	 * dbc.writeRecord(dbr); }
	 * 
	 * } catch (Exception ex) { System.out.println(ex); throw newSQLException(
	 * "CartCntrlEtChiusure, Errore eseguendo una aggiornaDataChiusSkVal()  ");
	 * } }gb 27/09/07: fine ******
	 */

	private void chiudoObieOrInterv(ISASConnection dbc, String strTableName, String[] ar_fldNames,
			String[] ar_fldTypes, String[] ar_fldValues, String strDtChiusura) throws Exception {
		String strNCartella = "";
		String strNProgetto = "";
		String strCodObiettivo = "";
		String strNIntervento = "";
		String strClausolaInterv = "";
		String selectByKey = "";

// 26/04/10 	ISASCursor dbcur = getSelectRecords(dbc, strTableName, ar_fldNames, ar_fldTypes, ar_fldValues);
		// 26/04/10 --
		ISASCursor dbcur = null;
		boolean contoDaAgg = false; // 30/10/04
		boolean contoRsaDaAgg = false; // 30/10/04
		try{
			if (strTableName.equals("ass_interventi")) {
				dbcur = getSelectRecords(dbc, strTableName, ar_fldNames, ar_fldTypes, ar_fldValues);

				// 30/04/10: cntrl se abilitato alla gestione del contoEconomico da dentro il Sins (deve avere entrambe le abilitazioni)
				if (h_Conf != null) {
					if ((h_Conf.get("ABILITAZ_CT") != null) && (h_Conf.get("ABILCT_SINS_AUTO") != null))
						contoDaAgg = (((String)h_Conf.get("ABILITAZ_CT")).trim().equals("SI"))
						&& (((String)h_Conf.get("ABILCT_SINS_AUTO")).trim().equals("SI"));
				}
				if (h_Conf != null) {
					if (h_Conf.get("ABILITAZ_RSA") != null && h_Conf.get("ABILITAZ_FIT") != null )
						contoRsaDaAgg = (((String)h_Conf.get("ABILITAZ_RSA")).trim().equals("SI"))&& (((String)h_Conf.get("ABILITAZ_FIT")).trim().equals("SI"));
				}
			} else if (strTableName.equals("ass_obbiettivi")) {
				String mySelect = getSelectQuery(dbc, strTableName, ar_fldNames, ar_fldTypes, ar_fldValues);
				mySelect += " AND ((ob_data_ragg IS NULL) OR (ob_data_ragg > " + formatDate(dbc, strDtChiusura) + "))";
				dbcur = dbc.startCursor(mySelect);
			}
			// 26/04/10 --

			// Metto i record letti in un vector (un vector di record).
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strNCartella = ((Integer) dbr.get("n_cartella")).toString();
				strNProgetto = ((Integer) dbr.get("n_progetto")).toString();
				strCodObiettivo = (String) dbr.get("cod_obbiettivo");
				if (strTableName.equals("ass_interventi")) {
					strNIntervento = ((Integer) dbr.get("n_intervento")).toString();
					strClausolaInterv = " AND n_intervento = " + strNIntervento;
				}
				selectByKey = "SELECT * FROM " + strTableName 
						+ " WHERE n_cartella = " + strNCartella
						+ " AND n_progetto = " + strNProgetto 
						+ " AND cod_obbiettivo = '" + strCodObiettivo + "'"
						+ strClausolaInterv;
				LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv, selectByKey: " + selectByKey);
				ISASRecord dbrByKey = dbc.readRecord(selectByKey);
				if (strTableName.equals("ass_obbiettivi")) {
					/** 26/04/10			
				String strFlagRaggiunto = (String) dbrByKey.get("ob_raggiunto");
				if (strFlagRaggiunto.equals("N")) {
					 **/				
					// GB 6/6/2006 dbrByKey.put("ob_raggiunto", "S");
					// GB 6/6/2006 dbrByKey.put("ob_oper_ragg",
					// (String)h.get("ver_oper_chiusa"));
					dbrByKey.put("ob_data_ragg", strDtChiusura);
					dbc.writeRecord(dbrByKey);
					LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv Obiettivi: Fatta la writeRecord");
					// 26/04/10		}
				} else if (strTableName.equals("ass_interventi")) {
					/***
					 * 06/02/08 String strFlagConcluso = (String)
					 * dbrByKey.get("int_concluso"); if
					 * (strFlagConcluso.equals("N")) {
					 * dbrByKey.put("int_data_concl", strDtChiusura);
					 * 
					 * dbc.writeRecord(dbrByKey); System.out.println(
					 * "CartCntrlEtChiusure/chiudiObieOrInterv Interventi: Fatta la writeRecord"
					 * ); }
					 ***/

					// 06/02/08 m.: lettura dettaglio associato
					decodIntervento(dbc, dbrByKey);
					String strCodDettInt = (String)dbrByKey.get("dettaglio_intervento");
					if ((strCodDettInt != null) && !strCodDettInt.equals("")) {
						strCodDettInt = strCodDettInt.trim();
						if (strCodDettInt.equals("GENERICO") || strCodDettInt.equals("ASSDOMDI"))
							setChiusuraDettInterventoGener(dbc, dbrByKey, strDtChiusura);
						else if (strCodDettInt.equals("CONTRIB")) // 26/04/10 --- 
							setChiusuraDettInterventoContr(dbc, dbrByKey, strDtChiusura, contoDaAgg);
						else if (strCodDettInt.equals("RSA")) 
							setChiusuraDettInterventoRsa(dbc, dbrByKey, strDtChiusura,contoRsaDaAgg);						
						else // 26/04/10 --- 
							LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv Interventi: DettaglioInterv=["
									+ strCodDettInt + "] non ancora gestito!!");
					} else {
						LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv: Intervento senza Dettaglio");
						// 14/10/10: aggiorno solo se la data � > di dtChiusura
						int aggiornato = 0;
						aggiornato += setDtFineaDettInterv(dbc, dbrByKey, strDtChiusura, "int_data_prest", "int_data_ins");
						if (aggiornato > 0) {
							dbc.writeRecord(dbrByKey); 
							LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv Interventi: Fatta la writeRecord"); 
						}
					}
				}
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoObieOrInterv()  ", ex);
			}
		}
	}

	// 06/02/08: ripresi da SocAssPrestazEJB
	private void decodIntervento(ISASConnection dbc, ISASRecord dbr) throws SQLException {
		String strCdSettore = "";
		String strCdTipo = "";
		String strCdIntervento = "";
		String strCdConcat = "";
		try {
			strCdConcat = (String) dbr.get("int_cod_intervento");
			strCdSettore = strCdConcat.substring(0, 2);
			strCdTipo = strCdConcat.substring(2, 4);
			strCdIntervento = strCdConcat.substring(4);

			String strQuery = "SELECT * FROM tab_interventi" 
					+ " WHERE cod_settore_interv = '" + strCdSettore + "'" 
					+ " AND cod_tipo_interv = '" + strCdTipo + "'" 
					+ " AND cod_intervento = '" + strCdIntervento + "'";
			LOG.info("CartCntrlEtChiusure/decodIntervento/strQuery : " + strQuery);
			ISASRecord dbrTab = dbc.readRecord(strQuery);
			if (dbrTab != null)
				dbr.put("dettaglio_intervento", dbrTab.get("dettaglio_intervento"));
			else
				dbr.put("dettaglio_intervento", "");
		} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure.decodIntervento(): " + e);
			throw new SQLException("Errore eseguendo la decodIntervento()");
		}
	}

	private void setChiusuraDettInterventoGener(ISASConnection dbc, ISASRecord dbr, String dtChius) throws SQLException {
		try {
			String strNAssistito = "" + dbr.get("n_cartella");
			String strNProgetto = "" + dbr.get("n_progetto");
			String strCodObiettivo = "" + dbr.get("cod_obbiettivo");
			String strNIntervento = "" + dbr.get("n_intervento");

			String strQuery = "SELECT * FROM ass_interventi_dett" 
					+ " WHERE n_cartella = " + strNAssistito
					+ " AND n_progetto = " + strNProgetto 
					+ " AND cod_obbiettivo = '" + strCodObiettivo + "'"
					+ " AND n_intervento = " + strNIntervento;
			LOG.info("CartCntrlEtChiusure/setChiusuraDettInterventoGener/strQuery : " + strQuery);
			ISASRecord dbrDett = dbc.readRecord(strQuery);
			if (dbrDett != null) {
/** 26/04/10
				if (dbrDett.get("data_concl") == null) {
					dbrDett.put("data_concl", dtChius);
					dbc.writeRecord(dbrDett);
					System.out.println("CartCntrlEtChiusure/chiudiObieOrInterv DettInterventoGener: Fatta la writeRecord");
				}			
**/				
				// 26/04/10 ---
				int aggiornato = 0;
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "aut2_data_fine", "aut2_data_inizio");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "aut1_data_fine", "aut1_data_inizio");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "prop_data_fine", "prop_data_inizio");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "data_concl", "prop_data_inizio");				
				if (aggiornato > 0) {
					dbrDett.put("flag_livello", "3");
					dbrDett.put("flag_esito", "1");
					dbc.writeRecord(dbrDett);
					LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv DettInterventoGener: Fatta la writeRecord");
				}
				// 26/04/10 ---
			}
		} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure.setChiusuraDettInterventoGener(): " + e);
			throw new SQLException("Errore eseguendo la setChiusuraDettInterventoGener()");
		}
	}

	// 26/04/10
	private void setChiusuraDettInterventoContr(ISASConnection dbc, ISASRecord dbr, String dtChius,
												boolean contoDaAgg) throws SQLException {
		try {
			String strNAssistito = "" + dbr.get("n_cartella");
			String strNProgetto = "" + dbr.get("n_progetto");
			String strCodObiettivo = "" + dbr.get("cod_obbiettivo");
			String strNIntervento = "" + dbr.get("n_intervento");

			String strQuery = "SELECT * FROM ass_contrib" 
					+ " WHERE n_cartella = " + strNAssistito
					+ " AND n_progetto = " + strNProgetto 
					+ " AND cod_obbiettivo = '" + strCodObiettivo + "'"
					+ " AND n_intervento = " + strNIntervento;
			LOG.info("CartCntrlEtChiusure/setChiusuraDettInterventoContr/strQuery : " + strQuery);
			ISASRecord dbrDett = dbc.readRecord(strQuery);
			if (dbrDett != null) {
				// 30/04/10: aggiornamento contoEconomico: 1  (si sottrae l'attuale) 
				String messaggio = "";
				boolean storno=false;
				//bargi 08/11/2011 aggiunto controllo su data per stornare
				storno=isDettIntervDaAggiornare(dbc, dbrDett, dtChius, "data_fine_comm2", "data_inizio_comm2")
				||isDettIntervDaAggiornare(dbc, dbrDett, dtChius, "data_fine", "data_inizio")
				||isDettIntervDaAggiornare(dbc, dbrDett, dtChius, "data_fine_prop", "data_inizio_prop");			
				
				
				if (contoDaAgg&&storno) 
					messaggio = cEco.updateContribContoEcon(-1, dbc, dbrDett);
				// 30/04/10--
			
				// 26/04/10 ---
				int aggiornato = 0;
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "data_fine_comm2", "data_inizio_comm2");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "data_fine", "data_inizio");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "data_fine_prop", "data_inizio_prop");				
				if (aggiornato > 0) {
					dbrDett.put("flag_livello", "3");
					dbrDett.put("flag_esito", "1");
					dbc.writeRecord(dbrDett);
					LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv DettInterventoContrib: Fatta la writeRecord");
				}
				// 26/04/10 ---
				
				// 30/04/10: aggiornamento contoEconomico: 2 (si aggiunge quello con la nuova data di chiusura)
				if (contoDaAgg&&storno) //bargi 08/11/2011 aggiunto controllo su data per stornare
					messaggio = cEco.updateContribContoEcon(1, dbc, dbrDett);
				// 30/04/10--
			}
		} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure.setChiusuraDettInterventoContr(): " + e);
			throw new SQLException("Errore eseguendo la setChiusuraDettInterventoContr()");
		}
	}	
	
	// 26/04/10
	private void setChiusuraDettInterventoRsa(ISASConnection dbc, ISASRecord dbr, String dtChius,boolean contoDaAgg) throws SQLException {
		try {
			String strNAssistito = "" + dbr.get("n_cartella");
			String strNProgetto = "" + dbr.get("n_progetto");
			String strCodObiettivo = "" + dbr.get("cod_obbiettivo");
			String strNIntervento = "" + dbr.get("n_intervento");

			String strQuery = "SELECT * FROM ass_ricoveri" 
					+ " WHERE n_cartella = " + strNAssistito
					+ " AND n_progetto = " + strNProgetto 
					+ " AND cod_obbiettivo = '" + strCodObiettivo + "'"
					+ " AND n_intervento = " + strNIntervento;
			LOG.info("CartCntrlEtChiusure/setChiusuraDettInterventoRsa/strQuery : " + strQuery);
			ISASRecord dbrDett = dbc.readRecord(strQuery);
			if (dbrDett != null) {
				// 26/04/10 ---
				boolean storno=false;
				storno=isDettIntervDaAggiornare(dbc, dbrDett, dtChius,"data_uscita", "data_ingresso")
				||isDettIntervDaAggiornare(dbc, dbrDett, dtChius, "au_data_auto_fin", "au_data_auto_ini")
				||isDettIntervDaAggiornare(dbc, dbrDett, dtChius, "co_data_comm_fin", "co_data_comm_ini")
				||isDettIntervDaAggiornare(dbc, dbrDett, dtChius,"as_data_prop_fin", "as_data_prop_ini");			
				String messaggio = "";
				//bargi 08/11/2011 aggiunto storno anche per rsa
				if (contoDaAgg&&storno) {
					if ( !isIngresso(dbc,dbrDett)) {
						String pref="as_";
						String tipoImp="I";
						if(dbrDett.get("flag_livello")!=null) {
							if (((String) dbrDett.get("flag_livello")).equals("2"))pref="co_";
							if (((String) dbrDett.get("flag_livello")).equals("3")) pref="au_";
						}
							messaggio += rtRsa.aggiornaCalcoliRicoveri(dbc, dbrDett, -1, pref,tipoImp);
					}
				}
					
				int aggiornato = 0;
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "data_uscita", "data_ingresso");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "au_data_auto_fin", "au_data_auto_ini");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "co_data_comm_fin", "co_data_comm_ini");
				aggiornato += setDtFineaDettInterv(dbc, dbrDett, dtChius, "as_data_prop_fin", "as_data_prop_ini");				
				if (aggiornato > 0) {
					dbrDett.put("flag_livello", "3");
					dbrDett.put("flag_esito", "1");
					dbc.writeRecord(dbrDett);
					LOG.info("CartCntrlEtChiusure/chiudiObieOrInterv DettInterventoRsa: Fatta la writeRecord");
				}
				//bargi 08/11/2011
				if (contoDaAgg&&storno) {
					if ( !isIngresso(dbc,dbrDett)) {
						String pref="as_";
						String tipoImp="I";
						if(dbrDett.get("flag_livello")!=null) {
							if (((String) dbrDett.get("flag_livello")).equals("2"))pref="co_";
							if (((String) dbrDett.get("flag_livello")).equals("3")) pref="au_";
						}
							messaggio += rtRsa.aggiornaCalcoliRicoveri(dbc, dbrDett, 1, pref,tipoImp);
					}
				}
				// 26/04/10 ---
			}
		} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure.setChiusuraDettInterventoRsa(): " + e);
			throw new SQLException("Errore eseguendo la setChiusuraDettInterventoRsa()");
		}
	}		
		private boolean isIngresso(ISASConnection dbc,ISASRecord h) throws Exception{
			try {
			if  ((h.get("data_ingresso") != null && h.get("data_ingresso").toString().length() == 10 &&   
					                h.get("cod_istitu_ingresso")!= null && !h.get("cod_istitu_ingresso").toString().equals(""))
										) 
				return true;
		  return false;
		}catch (Exception e) {
				throw new SQLException("Errore eseguendo una RicoveriRsa.isIngresso()- " + e);
			}
		}
	// 26/04/10: aggiorno dtFine (prop, auto1, auto2) se:
	//	- esiste ed � > dtChius 
	//	- se non esiste ma esiste la dtIni relativa  (prop, auto1, auto2)
	private int setDtFineaDettInterv(ISASConnection dbc, ISASRecord dbr, String dtChius,
											String nmFldDtDaAgg, String nmFldDtDaCntrl) throws SQLException {
		try {
			boolean daAggiornare = false;
			daAggiornare=isDettIntervDaAggiornare(dbc, dbr, dtChius, nmFldDtDaAgg, nmFldDtDaCntrl);
			/*if (dbr.get(nmFldDtDaAgg) != null) {
				DataWI dtDaAgg_wi = new DataWI((java.sql.Date)dbr.get(nmFldDtDaAgg));
				String dtChius_ndf = ndf.formDate(dtChius, "aaaammgg");
				daAggiornare = (dtDaAgg_wi.isSuccessiva(dtChius_ndf));
			} else 
				daAggiornare = (dbr.get(nmFldDtDaCntrl) != null);
			*/
			if (daAggiornare)
				dbr.put(nmFldDtDaAgg, dtChius);
			return (daAggiornare?1:0);
		} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure.setDtChiusuraDettInterv(): " + e);
			throw new SQLException("Errore eseguendo la setDtChiusuraDettInterv()");
		}
	}
	private boolean isDettIntervDaAggiornare(ISASConnection dbc, ISASRecord dbr, String dtChius,
			String nmFldDtDaAgg, String nmFldDtDaCntrl) throws SQLException {
			try {
			boolean daAggiornare = false;
			if (dbr.get(nmFldDtDaAgg) != null) {
			DataWI dtDaAgg_wi = new DataWI((java.sql.Date)dbr.get(nmFldDtDaAgg));
			String dtChius_ndf = ndf.formDate(dtChius, "aaaammgg");
			daAggiornare = (dtDaAgg_wi.isSuccessiva(dtChius_ndf));
			} else 
			daAggiornare = (dbr.get(nmFldDtDaCntrl) != null);
			
			//if (daAggiornare)
			//dbr.put(nmFldDtDaAgg, dtChius);
			return daAggiornare;
			} catch (Exception e) {
			LOG.error("CartCntrlEtChiusure.setDtChiusuraDettInterv(): " + e);
			throw new SQLException("Errore eseguendo la setDtChiusuraDettInterv()");
			}
}
	/*
	 * gb 28/09/97 ******* public void chiudiObiettiviDaCartella(ISASConnection
	 * dbc, String strNCartella, String strDtChiusura) throws Exception { String
	 * selectByKey = ""; String myselect = "SELECT *" + " FROM ass_obbiettivi" +
	 * " WHERE n_cartella = " + strNCartella;
	 * 
	 * System.out.println(
	 * "CartCntrlEtChiusure/chiudiObiettiviDaCartella, myselect: " + myselect);
	 * // Leggo i record ISASCursor dbcur = dbc.startCursor(myselect);
	 * 
	 * // Metto i record letti in un vector (un vector di record). Vector
	 * vdbr=dbcur.getAllRecord();
	 * 
	 * String strCodObiettivo = ""; String strNProgetto = ""; for (int i=0;
	 * i<vdbr.size(); i++) { ISASRecord dbr = (ISASRecord) vdbr.get(i);
	 * strCodObiettivo = (String)dbr.get("cod_obbiettivo"); strNProgetto =
	 * ((Integer)dbr.get("n_progetto")).toString(); selectByKey = "SELECT *" +
	 * " FROM ass_obbiettivi" + " WHERE n_cartella = " + strNCartella +
	 * " AND n_progetto = " + strNProgetto + " AND cod_obbiettivo = '" +
	 * strCodObiettivo + "'";System.out.println(
	 * "CartCntrlEtChiusure/chiudiObiettiviDaCartella, selectByKey: " +
	 * selectByKey); ISASRecord dbrByKey = dbc.readRecord(selectByKey); String
	 * strFlagRaggiunto = (String) dbrByKey.get("ob_raggiunto"); if
	 * (strFlagRaggiunto.equals("N")) { //GB 6/6/2006
	 * dbrByKey.put("ob_raggiunto", "S"); //GB 6/6/2006
	 * dbrByKey.put("ob_oper_ragg", (String)h.get("ver_oper_chiusa"));
	 * dbrByKey.put("ob_data_ragg", strDtChiusura); dbc.writeRecord(dbrByKey);
	 * System.out.println(
	 * "CartCntrlEtChiusure/chiudiObiettiviDaCartella: Fatta la writeRecord"); }
	 * } }gb 28/09/97: fine ******
	 */

	/*
	 * gb 28/09/97 ******* public void chiudiInterventiDaCartella(ISASConnection
	 * dbc, String strNCartella, String strDtChiusura) throws Exception { String
	 * selectByKey = ""; String myselect = "SELECT *" + " FROM ass_interventi" +
	 * " WHERE n_cartella = " + strNCartella;System.out.println(
	 * "CartCntrlEtChiusure/chiudiInterventiDaCartella, myselect: " + myselect);
	 * 
	 * // Leggo i record ISASCursor dbcur = dbc.startCursor(myselect);
	 * 
	 * // Metto i record letti in un vector (un vector di record). Vector
	 * vdbr=dbcur.getAllRecord();
	 * 
	 * String strNProgetto = ""; String strCodObiettivo = ""; String
	 * strNIntervento = ""; for (int i=0; i<vdbr.size(); i++) { ISASRecord dbr =
	 * (ISASRecord) vdbr.get(i); strNProgetto =
	 * ((Integer)dbr.get("n_progetto")).toString(); strCodObiettivo =
	 * (String)dbr.get("cod_obbiettivo"); strNIntervento =
	 * ((Integer)dbr.get("n_intervento")).toString(); selectByKey = "SELECT *" +
	 * " FROM ass_interventi" + " WHERE n_cartella = " + strNCartella +
	 * " AND n_progetto = " + strNProgetto + " AND cod_obbiettivo = '" +
	 * strCodObiettivo + "'" + " AND n_intervento = " + strNIntervento;
	 * System.out
	 * .println("CartCntrlEtChiusure/chiudiInterventiDaCartella, selectByKey: "
	 * + selectByKey); ISASRecord dbrByKey = dbc.readRecord(selectByKey); //GB
	 * 6/6/2006 le linee che seguono sostituiscono quelle commentate. String
	 * strFlagConcluso = (String) dbrByKey.get("int_concluso"); if
	 * (strFlagConcluso.equals("N")) { dbrByKey.put("int_data_concl",
	 * strDtChiusura);
	 * 
	 * dbc.writeRecord(dbrByKey);System.out.println(
	 * "CartCntrlEtChiusure/chiudiInterventiDaCartella: Fatta la writeRecord");
	 * } } }gb 28/09/97: fine ******
	 */

	private void chiudoVerifiche(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes, String[] ar_fldValues,
			String strDtChiusura, String strCodOperatore) throws Exception {
		String strNCartella = "";
		String strNProgetto = "";
		String strNVerifica = "";
		String selectByKey = "";
		try{
	// 26/04/10	ISASCursor dbcur = getSelectRecords(dbc, "ass_verifica", ar_fldNames, ar_fldTypes, ar_fldValues);
	
			// 26/04/10 --
			String mySelect = getSelectQuery(dbc, "ass_verifica", ar_fldNames, ar_fldTypes, ar_fldValues);
			mySelect += " AND ((ver_data_chiusa IS NULL) OR (ver_data_chiusa > " + formatDate(dbc, strDtChiusura) + "))";
			ISASCursor dbcur = dbc.startCursor(mySelect);
			// 26/04/10 --		
			
			// Metto i record letti in un vector (un vector di record).
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
	
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strNCartella = ((Integer) dbr.get("n_cartella")).toString();
				strNProgetto = ((Integer) dbr.get("n_progetto")).toString();
				strNVerifica = ((Integer) dbr.get("n_verifica")).toString();
				selectByKey = "SELECT * FROM ass_verifica" 
						+ " WHERE n_cartella = " + strNCartella
						+ " AND n_progetto = " + strNProgetto 
						+ " AND n_verifica = " + strNVerifica;
				LOG.info("CartCntrlEtChiusure/chiudiVerifiche, selectByKey: " + selectByKey);
				ISASRecord dbrByKey = dbc.readRecord(selectByKey);
	/** 26/04/10
				String strFlagChiusa = (String) dbrByKey.get("ver_chiusa");
				if (strFlagChiusa.equals("N")) {
	**/			
					String strFlagDefinitiva = (String) dbrByKey.get("ver_definitiva");
					if (strFlagDefinitiva.equals("S")) {
						dbrByKey.put("ver_definitiva", "N");
						dbrByKey.put("ver_oper_def", (String) "");
						dbrByKey.put("ver_data_def", (java.sql.Date) null);
					}
					dbrByKey.put("ver_chiusa", "S");
					dbrByKey.put("ver_oper_chiusa", strCodOperatore);
					dbrByKey.put("ver_data_chiusa", strDtChiusura);
	
					dbc.writeRecord(dbrByKey);
					LOG.info("CartCntrlEtChiusure/chiudiVerifiche: Fatta la writeRecord");
	// 26/04/10	}
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoVerifiche()  ", ex);
			}
		}
	}

	// 04/03/11: a seconda della "Risposta programmata" terminata, chiude autorizzazioni/contatti
	public void chiudoDaRispProgrInGiu(ISASConnection dbc, String strNCartella, String strDtSkVal, String codRP,
			String strDtChRP, String strMotivoChiusura, String strCodOperatore) throws Exception {
		try {
			ISASCursor dbcur = null;
			String[] ar_fldNames = new String[] { "n_cartella" };
			String[] ar_fldTypes = new String[] { "NUM" };
			String[] ar_fldValues = new String[] { strNCartella };
		
			// 30/04/10
			if (h_Conf == null)
				h_Conf = evUtl.leggiConf(dbc, strCodOperatore, arrKeyConf);
		
			int tpRispProgr = Integer.parseInt(codRP);
			String strTableName = "";
			String nmFldDtIni = "";
			String nmFldDtFin = "";
			String strTipoOperatore = "";
			
			switch (tpRispProgr) {
				case 1: // mmg
// 2705/11			chiudoAutorizzazioneADI_ADP_ADR(dbc, strNCartella, strDtChRP);
					chiudoLastAutorADI_ADP_ADR(dbc, strNCartella, strDtChRP);
					break;
				
				case 2: // inf
					strTableName = "skinf";
					nmFldDtIni = "ski_data_apertura";
					nmFldDtFin = "ski_data_uscita";
					strTipoOperatore = "02";
					break;

				case 3: // riab
					strTableName = "skfis";
					nmFldDtIni = "skf_data";
					nmFldDtFin = "skf_data_chiusura";
					strTipoOperatore = "04";
					break;

				case 4: // med spec: medico
					strTableName = "skmedico";
					nmFldDtIni = "skm_data_apertura";
					nmFldDtFin = "skm_data_chiusura";
					strTipoOperatore = "03";
					break;

				case 5: // ass soc
					strTableName = "ass_progetto";
					nmFldDtIni = "ap_data_apertura";
					nmFldDtFin = "ap_data_chiusura";
					strTipoOperatore = "01";
					break;

				case 6: // ota
					LOG.info("CartCntrlEtChiusure/chiudoDaRispProgrInGiu: codRispProgr=OTA: NON si fa niente");
					break;					
				
				default:
					LOG.info("CartCntrlEtChiusure/chiudoDaRispProgrInGiu: codRispProgr NON definito");
			}
		
			// leggo e chiudo i contatti della tipologia associata alla risp progr, tranne i casi mmg e ota
			if ((tpRispProgr > 1) && (tpRispProgr < 6)) {
				String mySelect = getSelectQuery(dbc, strTableName, ar_fldNames, ar_fldTypes, ar_fldValues);
				mySelect += " AND " + nmFldDtIni + " < " + formatDate(dbc, strDtChRP);
				mySelect += " AND ((" + nmFldDtFin + " IS NULL) OR (" + nmFldDtFin + " > " + formatDate(dbc, strDtChRP) + "))";
				dbcur = dbc.startCursor(mySelect);
			
				Vector vdbr = dbcur.getAllRecord();
				dbcur.close();
				
				for (Enumeration senum = vdbr.elements(); senum.hasMoreElements();) {
					ISASRecord dbr_1 = (ISASRecord) senum.nextElement();

					if (tpRispProgr == 5) {// ass soc
						chiudoDaProgettoAssSocialeInGiu(dbc, strNCartella, ((Integer)dbr_1.get("n_progetto")).toString(),
														strDtChRP, strCodOperatore);
					
						chiudoProgettoAssSociale(dbc, strNCartella, ((Integer)dbr_1.get("n_progetto")).toString(), 
													strDtChRP, strMotivoChiusura, strCodOperatore, true);
					} else {
						chiudoDaContattoInGiu(dbc, strNCartella, ((Integer)dbr_1.get("n_contatto")).toString(),
												strDtChRP, strTipoOperatore, strCodOperatore);
					
						chiudoContatto(dbc, strTableName, strNCartella, ((Integer)dbr_1.get("n_contatto")).toString(), 
													nmFldDtFin, strDtChRP, true);
					}
				}
			}
		} catch (Exception ex) {
			LOG.error(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoDaRispProgrInGiu()  ");
		}
	}
	
	// 04/03/11
	private void chiudoRisposteProgr(ISASConnection dbc, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues, String strDtChiusura) throws Exception {
		try {
			String mySelect = getSelectQuery(dbc, "rv_puauvm_rispro", ar_fldNames, ar_fldTypes, ar_fldValues);
			mySelect += " AND ((dt_fine IS NULL) OR (dt_fine > " + formatDate(dbc, strDtChiusura) + "))";
			ISASCursor dbcur = dbc.startCursor(mySelect);
			
			// Metto i record letti in un vector (un vector di record).
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			
			for (int i = 0; i < vdbr.size(); i++) {
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				String strDataSkVal = ((java.sql.Date) dbr.get("pr_data")).toString();
				String sel = "SELECT * FROM rv_puauvm_rispro" 
					+ " WHERE n_cartella = " + (Integer) dbr.get("n_cartella") 
					+ " AND pr_data = " + formatDate(dbc, strDataSkVal)
					+ " AND pr_progr = " + (Integer) dbr.get("pr_progr")
					+ " AND risprocod = " + (Integer) dbr.get("risprocod");
				LOG.info("CartCntrlEtChiusure / chiudoRisposteProgr / sel: " + sel);
				ISASRecord dbrDett = dbc.readRecord(sel);
				dbrDett.put("dt_fine", strDtChiusura);
				dbc.writeRecord(dbrDett);
			}
		} catch (Exception ex) {
			if(forzaChiusura){
				loggaErroreInChiusuraForzata(ex, dbc, cod_usl, data, motivo);
			}else{
				throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoRisposteProgr()  ", ex);
			}
		}
	}
	
	// 27/05/11: chiusura con marcamento flag "effettiva" su ultima AUTORIZZAZIONE ADI/ADP/ADR
	private void chiudoLastAutorADI_ADP_ADR(ISASConnection dbc, String strNCartella, String strDtCh) throws Exception 
	{
		try {
			// cerco l'ultima autorizzazione tra ADI, ADP e ADR
			ISASRecord dbrLastAutor = (ISASRecord)getLastAutorADIADPADR(dbc, strNCartella);
			if (dbrLastAutor == null)
				return;
						
			// se gi� marcata come chiusura effettiva: non si fa niente
			if ((dbrLastAutor.get("fine_effettiva") != null) && (dbrLastAutor.get("fine_effettiva").toString().equals("S")))
				return;
			
			String nmFldDtChius = "skadi_data_fine";
			String nmFldDtEffettiva = "skadi_fine_effettiva";
			String nmFldDtRiesame = "skadi_data_riesame"; // 06/02/12
			if (dbrLastAutor.get("skadp_data") != null) { // ADP
				nmFldDtChius = "skadp_data_fine";
				nmFldDtEffettiva = "skadp_fine_effettiva";
				nmFldDtRiesame = "skadp_data_riesame"; // 06/02/12
			} else if (dbrLastAutor.get("skadr_data") != null) { //ADR
				nmFldDtChius = "skadr_data_fine";
				nmFldDtEffettiva = "skadr_fine_effettiva";
			} 
			
			dbrLastAutor.put(nmFldDtChius, strDtCh);
			dbrLastAutor.put(nmFldDtEffettiva, "S");
			dbrLastAutor.put(nmFldDtRiesame, ((java.sql.Date)null)); // 06/02/12
			dbc.writeRecord(dbrLastAutor);
		} catch (Exception ex) {
			LOG.error("CartCntrlEtChiusure, Errore eseguendo una chiudoLastAutorADI_ADP_ADR() - :"+ex);
			throw ex;
		}
	}
	
	// 27/05/11: ricerca ultima (= quella con dtInizio maggiore) AUTORIZZAZIONE ADI/ADP/ADR
	private ISASRecord getLastAutorADIADPADR(ISASConnection dbc, String strNCartella) throws Exception 
	{
		try {
			String dtIniMax = "01/01/1000";
			int tpAD = 0;
			String dtKey = "";
			
			String selMaxADI = getSelMaxAutorADIADPADR(dbc, "skmmg_adi", strNCartella, "skadi_data_inzio", dtIniMax);			
			ISASRecord dbrMax = dbc.readRecord(selMaxADI);
			if ((dbrMax != null) && (dbrMax.get("skadi_data_inzio") != null)) {
				dtIniMax = dbrMax.get("skadi_data_inzio").toString();
				dtKey = dbrMax.get("skadi_data").toString();
				tpAD = 1;
			}

			String selMaxADP = getSelMaxAutorADIADPADR(dbc, "skmmg_adp", strNCartella, "skadp_data_inizio", dtIniMax);
			dbrMax = dbc.readRecord(selMaxADP);
			if ((dbrMax != null) && (dbrMax.get("skadp_data_inizio") != null)) {
				dtIniMax = dbrMax.get("skadp_data_inizio").toString();
				dtKey = dbrMax.get("skadp_data").toString();
				tpAD = 2;
			}
				
			String selMaxADR = getSelMaxAutorADIADPADR(dbc, "skmmg_adr", strNCartella, "skadr_data_inizio", dtIniMax);
			dbrMax = dbc.readRecord(selMaxADR);
			if ((dbrMax != null) && (dbrMax.get("skadr_data_inizio") != null)) {
				dtIniMax = dbrMax.get("skadr_data_inizio").toString();
				dtKey = dbrMax.get("skadr_data").toString();
				tpAD = 3;
			}
			
			ISASRecord dbrToRet = null;
			String sel = null;
			switch (tpAD) {
				case 1:
					sel = getSelAutorADIADPADR(dbc, "skmmg_adi", strNCartella, "skadi_data", dtKey);
					break;
				case 2:
					sel = getSelAutorADIADPADR(dbc, "skmmg_adp", strNCartella, "skadp_data", dtKey);
					break;
				case 3:
					sel = getSelAutorADIADPADR(dbc, "skmmg_adr", strNCartella, "skadr_data", dtKey);
					break;	
				default:
					LOG.info("CartCntrlEtChiusure.getLastAutorADIADPADR(): nessuna autorizz ADI/ADP/ADR trovata.");
					break;
			}

			if (sel != null)
				dbrToRet = dbc.readRecord(sel);
						
			return (ISASRecord)dbrToRet;
		} catch (Exception ex) {
			LOG.error("CartCntrlEtChiusure, Errore eseguendo una getLastAutorADIADPADR() - :"+ex);
			throw ex;
		}
	}
	
	// 27/05/11
	private String getSelAutorADIADPADR(ISASConnection dbc, String nmTab, String strNCartella, 
											String nmFldData, String valData) throws Exception
	{
		return ("SELECT a.* FROM " + nmTab + " a" 
					+ " WHERE a.n_cartella = " + strNCartella
					+ " AND a." + nmFldData + " = " + formatDate(dbc, valData));
	}
	
	// 27/05/11
	private String getSelMaxAutorADIADPADR(ISASConnection dbc, String nmTab, String strNCartella, 
											String nmFldDataIni, String valDtM)
	{
		return ("SELECT a.* FROM " + nmTab + " a" 
					+ " WHERE a.n_cartella = " + strNCartella
					+ " AND a." + nmFldDataIni + " IN (SELECT MAX(b." + nmFldDataIni + ") FROM " + nmTab + " b"
							+ " WHERE b.n_cartella = a.n_cartella"
							+ " AND b." + nmFldDataIni + " >= " + formatDate(dbc, valDtM) + ")");
	}
	
	
	// 16/05/16 mv: per problemi ISAS si procede con l'aggiornamento tramite SQL
	private void chiudoContattiDaCartSQL(ISASConnection dbc, String strNCartella, String strDtChiusura, String strMotivoChiusura)
			throws SQLException {
		try {
			String[] ar_tableNames = new String[] {"skinf", "skmedico", "skfis", "skmedpal"};
			String[] ar_fldDtChNames = new String[] {"ski_data_uscita", "skm_data_chiusura", 
														"skf_data_chiusura", "skm_data_chiusura"};
			String[] ar_fldMotChNames = new String[] {"ski_dimissioni", "skm_motivo_chius", 
														"skf_motivo_chius",	"skm_motivo_chius"};

			for (int i=0; i<ar_tableNames.length; i++) {
				String sql = "UPDATE " + ar_tableNames[i] + " SET "
								+ ar_fldDtChNames[i] + " = " + formatDate(dbc, strDtChiusura) + ", "
								+ ar_fldMotChNames[i] + " = " + strMotivoChiusura
								+ " WHERE n_cartella = " + strNCartella
								+ " AND ((" + ar_fldDtChNames[i] + " IS NULL)"
									+ " OR (" + ar_fldDtChNames[i] + " > " + formatDate(dbc, strDtChiusura) + ")"
								+ ")";
				debugMessage("CartCntrlEtChiusure/chiudoContattiDaCartSQL - sql x cartella: " + sql);
				
				dbc.execSQL(sql);
			}
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("CartCntrlEtChiusure, Errore eseguendo una chiudoContattiDaCartSQL()  ");
		}
	}
	
	private ISASCursor getSelectRecords(ISASConnection dbc, String strTableName, String[] ar_fldNames,
			String[] ar_fldTypes, String[] ar_fldValues) throws Exception {
		String mySelect = "SELECT * FROM " + strTableName;
		String strWHERE_AND = " WHERE ";
		String strValue = "";
		for (int i = 0; i < ar_fldNames.length; i++) {
			String strApice = (ar_fldTypes[i].equals("STR") ? "'" : "");
			strValue = ((ar_fldTypes[i].equals("DTA")) ? formatDate(dbc, ar_fldValues[i]) : ar_fldValues[i]);
			mySelect += strWHERE_AND + ar_fldNames[i] + " = " + strApice + strValue + strApice;
			strWHERE_AND = " AND ";
		}

		LOG.info("CartCntrlEtChiusure/getSelectRecords, mySelect: " + mySelect);
		// Leggo i record
		ISASCursor dbcur = dbc.startCursor(mySelect);
		return dbcur;
	}

	private String getSelectQuery(ISASConnection dbc, String strTableName, String[] ar_fldNames, String[] ar_fldTypes,
			String[] ar_fldValues) throws Exception {
		String mySelect = "SELECT * FROM " + strTableName;
		String strWHERE_AND = " WHERE ";
		String strValue = "";
		for (int i = 0; i < ar_fldNames.length; i++) {
			String strApice = (ar_fldTypes[i].equals("STR") ? "'" : "");
			strValue = ((ar_fldTypes[i].equals("DTA")) ? formatDate(dbc, ar_fldValues[i]) : ar_fldValues[i]);
			mySelect += strWHERE_AND + ar_fldNames[i] + " = " + strApice + strValue + strApice;
			strWHERE_AND = " AND ";
		}
		LOG.info("CartCntrlEtChiusure/getSelectQuery, mySelect: " + mySelect);
		return mySelect;
	}
	
	

	/*
	 * gb 28/09/97 ******* public void chiudiVerificheDaCartella(ISASConnection
	 * dbc, String strNCartella, String strDtChiusura, String strCodOperatore)
	 * throws Exception { String selectByKey = ""; String myselect = "SELECT *"
	 * + " FROM ass_verifica" + " WHERE n_cartella = " + strNCartella;
	 * System.out
	 * .println("CartCntrlEtChiusure/chiudiVerificheDaCartella, myselect: " +
	 * myselect);
	 * 
	 * // Leggo i record ISASCursor dbcur = dbc.startCursor(myselect);
	 * 
	 * // Metto i record letti in un vector (un vector di record). Vector
	 * vdbr=dbcur.getAllRecord();
	 * 
	 * String strNProgetto = ""; String strNVerifica = ""; for (int i=0;
	 * i<vdbr.size(); i++) { ISASRecord dbr = (ISASRecord) vdbr.get(i);
	 * strNProgetto = ((Integer)dbr.get("n_progetto")).toString(); strNVerifica
	 * = ((Integer)dbr.get("n_verifica")).toString(); selectByKey = "SELECT *" +
	 * " FROM ass_verifica" + " WHERE n_cartella = " + strNCartella +
	 * " AND n_progetto = " + strNProgetto + " AND n_verifica = " +
	 * strNVerifica;System.out.println(
	 * "CartCntrlEtChiusure/chiudiVerificheDaCartella, selectByKey: " +
	 * selectByKey); ISASRecord dbrByKey = dbc.readRecord(selectByKey); String
	 * strFlagChiusa = (String) dbrByKey.get("ver_chiusa"); if
	 * (strFlagChiusa.equals("N")) { String strFlagDefinitiva = (String)
	 * dbrByKey.get("ver_definitiva"); if (strFlagDefinitiva.equals("S")) {
	 * dbrByKey.put("ver_definitiva", "N"); dbrByKey.put("ver_oper_def",
	 * (String)""); dbrByKey.put("ver_data_def", (java.sql.Date)null); }
	 * dbrByKey.put("ver_chiusa", "S"); dbrByKey.put("ver_oper_chiusa",
	 * strCodOperatore); dbrByKey.put("ver_data_chiusa", strDtChiusura);
	 * 
	 * dbc.writeRecord(dbrByKey);System.out.println(
	 * "CartCntrlEtChiusure/chiudiVerificheDaCartella: Fatta la writeRecord"); }
	 * } }gb 28/09/97: fine ******
	 */



	private void stampaQuery(String punto, String query) {
		LOG.info(punto + "Query>" + query + "<");

	}

	private void stampaEccezione(String messaggio) {
		stampa("", "\n\t Eccezione> " + messaggio + "<\n");
	}

	private void stampa(String punto, String messaggio) {
		LOG.info(punto + " " + messaggio);

	}

	private String getValoreStringa(ISASRecord dbr, String key) {
		String valoreLetto = "";

		try {
			valoreLetto = dbr.get(key) + "";
		} catch (Exception e) {
			stampaEccezione("Errore nella lettura della key>" + key + "<\nRecord>"
					+ (dbr != null ? dbr.getHashtable() + "" : " RECORD NON VALIDO ") + "<");
		}

		return valoreLetto;
	}
	
	private boolean isRegToscana(ISASConnection dbc) throws Exception
	{
		return ((Boolean)gestore_casi.isUbicazRegTosc(dbc, new Hashtable())).booleanValue();
		
	}
}
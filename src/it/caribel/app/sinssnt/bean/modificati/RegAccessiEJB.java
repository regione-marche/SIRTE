package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 14/04/2006 - EJB di connessione alla procedura SINS Tabella RegAccessi
//bargi 13/10/2008 aggiunto ADR
//gb 18.12.08: Riportate le modifiche di bargi 13.10.2008
//int
// ==========================================================================

import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.sinssnt.casi_adrsa.*; // 15/02/10

public class RegAccessiEJB extends SINSSNTConnectionEJB {
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	// varibaile per il totale della stampa
	int tot = 0;

	// 15/02/10 m. ------------------------------
	private GestCasi gestCaso = new GestCasi();
	private GestSegnalazione gestSegn = new GestSegnalazione();
	private GestPresaCarico gestPresaCar = new GestPresaCarico();
	private GestErogazione gestErog = new GestErogazione();
	private EveUtils eveUtl = new EveUtils();

	// 15/02/10 m. ------------------------------

	// 31/05/11
	private boolean isVeneto = false;
	
	
	public RegAccessiEJB() {
	}

	/*
	 * Devo andare a prendere quei record a cui � stata registrata
	 * un'autorizzazione in quel mese(di norma non dovrebbero esserci
	 * autorizzazioni sovrapposte); dopo aver fatto questo controllo vado a
	 * vedere se ci sono autorizzazioni scadute da uno o due mesi. Per quanto
	 * riguarda le date di inserimento in RSA e ricovero, devo tirar fuori quei
	 * record che le hanno nulle o con mese uguale a quello in esame.
	 */
	public ISASRecord queryKey(myLogin mylogin, Hashtable h)
			throws SQLException, ISASPermissionDeniedException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		// G.Brogi 16/05/06 aggiunto anno
		String anno = "";

		String mese = "";
		String medico = "";
		String tabella = "";
		String dataIni = "";
		String dataFine = "";
		// 31/05/11
		String fineEff = "";

		// Max, 2012/01/18
		ISASCursor dbcur = null, dbcur2 = null, dbcurInt = null;

		ISASRecord dbr = null;
		String strCodOperatore = ""; // gb 17/07/08
		String strTipo = ""; // gb 17/07/08
		try {
			dbc = super.logIn(mylogin);
			ServerUtility su = new ServerUtility();
		        if (LOG.isDebugEnabled())
		                LOG.debug("queryKey() input = " + h);
			try {
				// 31/05/11
				isVeneto = (gestCaso.isUbicazRegVeneto(dbc, new Hashtable())).booleanValue();
			
				// G.Brogi 16/05/06 aggiunto anno
				anno = (String) h.get("anno");

				mese = (String) h.get("mese");
				medico = (String) h.get("medico");
				strCodOperatore = (String) h.get("cod_oper"); // gb 17/07/08
				strTipo = (String) h.get("tipo"); // gb 17/07/08
			} catch (Exception ex) {
				throw newEjbException("errore in queryKey() leggendo le chiavi primarie: " + ex.getMessage(), ex);
			}
			if (((String) h.get("tipo")).equals("ADI")) {
				tabella = "adi";
				dataIni = "skadi_data_inzio";
				dataFine = "skadi_data_fine";
				// 31/05/11
				fineEff = "skadi_fine_effettiva";
			} else if (((String) h.get("tipo")).equals("ADP")) { // gb 18.12.08
				tabella = "adp";
				dataIni = "skadp_data_inizio";
				dataFine = "skadp_data_fine";
				// 31/05/11
				fineEff = "skadp_fine_effettiva";
			} else if (((String) h.get("tipo")).equals("RSA")) {
				tabella = "adr";
				dataIni = "skadr_data_inizio";
				dataFine = "skadr_data_fine";
				// 31/05/11
				fineEff = "skadr_fine_effettiva";
			}
			int intMese = Integer.parseInt(mese);
			// G.Brogi 16/05/06
			int intAnno = Integer.parseInt(anno);
			/*
			 * Arezzo ha richiesto che non siano mostrati i casi con data di
			 * decesso minore del mese in corso
			 */
			// G.Brogi 17/02/2010
			String mesesucc = "";
			if (intMese < 12) {
				if (intMese < 9)
					mesesucc = "01-0" + (intMese + 1) + "-";
				else
					mesesucc = "01-" + (intMese + 1) + "-";
			} else
				mesesucc = "01-01-";
			mesesucc += "" + (intAnno + (intMese < 12 ? 0 : 1));
			String meseincorso = "01-" + mese + "-" + anno;
			// G.Brogi 17/02/2010 fine

			String sel = "SELECT s.*, s."+ dataIni + " data_inizio,"
					+ " s." + dataFine + " data_fine,"
					// 31/05/11
					+ " s." + fineEff + " fine_effettiva," 
					+ " c.cognome, c.nome,c.cod_fisc, c.cod_reg,c.data_chiusura "
					+ " FROM skmmg_" + tabella + " s, cartella c WHERE "
					+ "s.sk" + tabella + "_mmgpls='" + medico  + "' AND "
					// G.Brogi 17/02/10
					+ " s." + dataIni + "<" + formatDate(dbc, mesesucc)
					+ " AND (s.sk" + tabella + "_data_fine IS NULL"
					+ " OR s.sk" + tabella + "_data_fine >="+ formatDate(dbc, meseincorso) + ")"
					// fine G.Brogi
					+ "AND s.n_cartella=c.n_cartella" + " AND s.sk" + tabella + "_data_ricovero IS NULL "
					// Arezzo
					+ " AND (c.data_chiusura IS NULL OR (c.data_chiusura IS NOT NULL AND "
					// G.Brogi 17/02/10
					+ " c.data_chiusura >=" + formatDate(dbc, meseincorso) + "))";
					// fine G.Brogi
			// Arezzo
			if (!((String) h.get("tipo")).equals("RSA"))
				sel += " AND s.sk" + tabella + "_data_rsa IS NULL ";
				
//18/05/11	sel += " ORDER BY s.n_cartella";
			sel += " ORDER BY c.cognome, c.nome, s.n_cartella";			
			System.out.println("Prima select:" + sel);

			dbcur = dbc.startCursor(sel);
			Vector vdb = dbcur.getAllRecord();
			dbr = dbc.newRecord("skmmg");
			dbr.put("medico", medico);
			dbr.put("mese", mese);
			// G.Brogi 16/05/06
			dbr.put("anno", anno);

			dbr.put("tipo", (String) h.get("tipo"));
			for (int i = 0; i < vdb.size(); i++) {
				ISASRecord dbrTab = (ISASRecord) vdb.elementAt(i);
				dbrTab.put("assistito", (String) util.getObjectField(dbrTab,
						"cognome", 'S')
						+ " "
						+ (String) util.getObjectField(dbrTab, "nome", 'S'));
				dbrTab.put("cod_reg", (String) util.getObjectField(dbrTab,
						"cod_reg", 'S'));
				dbrTab.put("cod_fisc", (String) util.getObjectField(dbrTab,
						"cod_fisc", 'S'));
				dbrTab.put("data_chiusura", (String) util.getObjectField(
						dbrTab, "data_chiusura", 'T'));
				dbrTab.put("accessi_prev", util.getObjectField(dbrTab, "sk"
						+ tabella + "_freq_mens", 'I'));
				dbrTab.put("data_ricovero", (String) util.getObjectField(
						dbrTab, "sk" + tabella + "_data_ricovero", 'T'));
				if (!((String) h.get("tipo")).equals("RSA")) // gb 18.12.08
					dbrTab.put("data_rsa", (String) util.getObjectField(dbrTab,
							"sk" + tabella + "_data_rsa", 'T'));
				// gb 18.12.08
				else {
					// 03/11/2008 devo andare a decodificare l'istitut
					dbrTab.put("istituto", util.getDecode(dbc, "istituti",
							"ist_codice", (String) util.getObjectField(dbrTab,
									"skadr_istituto", 'S'), "st_nome"));
				}
				// gb 18.12.08: fine
			}
			/*
			 * Vado a prendere quelli che hanno la data fine scaduta di uno o
			 * due mesi
			 */
			int mese1 = intMese - 1;
			int mese2 = intMese - 2;
			// G.Brogi il calcolo dei mesi precedenti va fatto in rapporto ai 12
			// mesi
			if (mese1 == 0)
				mese1 = 12;
			if (mese2 <= 0)
				mese2 = mese2 == -1 ? 11 : 12;
			// fine

			String lungMese1 = "";
			String lungMese2 = "";
			if (("" + mese1).length() == 1)
				lungMese1 = "0" + "" + mese1;
			else
				lungMese1 = "" + mese1;
			if (("" + mese2).length() == 1)
				lungMese2 = "0" + "" + mese2;
			else
				lungMese2 = "" + mese2;

			// G.Brogi - Calcolo dell'anno
			int anno1 = intAnno;
			int anno2 = intAnno;
			if (mese1 == 12) { // caso mese scelto 01 => mese1=12 e mese2=11
				anno1 = intAnno - 1;
				anno2 = intAnno - 2;
			} else if (mese2 == 12)// caso mese scelto 02=> mese1=01 e mese2=12
				anno2 = intAnno - 1;
			// fine

			sel = "SELECT s.*,s."
					+ dataIni
					+ " data_inizio, s."
					+ dataFine
					+ " data_fine,"
					// 31/05/11
					+ " s." + fineEff + " fine_effettiva," 
					+ " c.cognome, c.nome,c.cod_fisc, c.cod_reg, c.data_chiusura "
					+ " FROM skmmg_"
					+ tabella
					+ " s, cartella c WHERE "
					+ "s.sk"
					+ tabella
					+ "_mmgpls='"
					+ medico
					+ "' AND ("
					+ dbc.formatDbYear("s." + dataIni)
					+ "<'"
					+ anno
					+ "' OR ("
					+ dbc.formatDbYear("s." + dataIni)
					+ "='"
					+ anno
					+ "' AND "
					+ dbc.formatDbMonth("s." + dataIni)
					+ "<='"
					+ mese
					+ "')) AND (("
					+ "s.sk"
					+ tabella
					+ "_data_fine IS NOT NULL AND (("
					+ "("
					+ dbc.formatDbMonth("s.sk" + tabella + "_data_fine")
					+ "='"
					+ lungMese1
					+ "' AND "
					+ dbc.formatDbYear("s.sk" + tabella + "_data_fine")
					+ "='"
					+ anno1
					+ "')"
					+ "OR "
					+ "("
					+ dbc.formatDbMonth("s.sk" + tabella + "_data_fine")
					+ "='"
					+ lungMese2
					+ "' AND "
					+ dbc.formatDbYear("s.sk" + tabella + "_data_fine")
					+ "='"
					+ anno2
					+ "')"
					+ "))) OR "
					+ " (((s.sk"
					+ tabella
					+ "_data_ricovero IS NOT NULL AND ("
					+ "("
					+ dbc.formatDbMonth("s.sk" + tabella + "_data_ricovero")
					+ "='"
					+ mese
					+ "' AND "
					+ dbc.formatDbYear("s.sk" + tabella + "_data_ricovero")
					+ "='"
					+ anno
					+ "')"
					+ "))"
					+ " AND (c.data_chiusura IS NULL OR (c.data_chiusura IS NOT NULL AND "
					+
					// G.Brogi 17/02/10
					" c.data_chiusura >=" + formatDate(dbc, meseincorso) + "))";
			// fine G.Brogi

			if (!((String) h.get("tipo")).equals("RSA"))
				sel += " OR (s.sk" + tabella + "_data_rsa IS NOT NULL AND ("
						+ dbc.formatDbMonth("s.sk" + tabella + "_data_rsa")
						+ "='" + mese + "' AND "
						+ dbc.formatDbYear("s.sk" + tabella + "_data_rsa")
						+ "='" + anno + "'))";

			sel += ")))";
			sel += " AND s.n_cartella=c.n_cartella" 
// 18/05/11	+ " ORDER BY s.n_cartella";
			+ " ORDER BY c.cognome, c.nome, s.n_cartella";			
			
			LOG.debug("queryKey(): seconda select = " + sel);
			
			
			dbcur2 = dbc.startCursor(sel);
			while (dbcur2.next()) {
				ISASRecord dbrTab = dbcur2.getRecord();
				dbrTab.put("assistito", (String) util.getObjectField(dbrTab,
						"cognome", 'S')
						+ " "
						+ (String) util.getObjectField(dbrTab, "nome", 'S'));
				dbrTab.put("cod_reg", (String) util.getObjectField(dbrTab,
						"cod_reg", 'S'));
				dbrTab.put("cod_fisc", (String) util.getObjectField(dbrTab,
						"cod_fisc", 'S'));
				dbrTab.put("data_chiusura", (String) util.getObjectField(
						dbrTab, "data_chiusura", 'T'));
				dbrTab.put("accessi_prev", util.getObjectField(dbrTab, "sk"
						+ tabella + "_freq_mens", 'I'));

				String chiusura = (String) util.getObjectField(dbrTab, "sk"
						+ tabella + "_data_fine", 'T');
				String chiuso = "";
				if (!chiusura.equals("")) {
					int mese_chius = Integer.parseInt(chiusura.substring(3, 5));
					int anno_chius = Integer
							.parseInt(chiusura.substring(6, 10));
					if (anno_chius < intAnno)
						chiuso = "S";
					else if (anno_chius == intAnno) {
						if (mese_chius <= intMese)
							chiuso = "S";
						else
							chiuso = "N";
					}
				} else
					chiuso = "N";
				dbrTab.put("chiuso", chiuso);
				String ricovero = "N";
				String data_ric = (String) util.getObjectField(dbrTab, "sk"
						+ tabella + "_data_ricovero", 'T');
				dbrTab.put("data_ricovero", data_ric);
				if (!data_ric.equals(""))
					ricovero = "S";

				if (!((String) h.get("tipo")).equals("RSA")) { // gb 18.12.08
					String data_rsa = (String) util.getObjectField(dbrTab, "sk"
							+ tabella + "_data_rsa", 'T');
					dbrTab.put("data_rsa", data_rsa);
					if (!data_rsa.equals(""))
						ricovero = "S";
				} else {
					// 03/11/2008 devo andare a decodificare l'istitut
					dbrTab.put("istituto", util.getDecode(dbc, "istituti",
							"ist_codice", (String) util.getObjectField(dbrTab,
									"skadr_istituto", 'S'), "st_nome"));
				}

				dbrTab.put("ricovero", ricovero);
				vdb.addElement(dbrTab);
			}
			// Per ogni record devo andare a vedere se esistono gi� record
			// inseriti su
			// INTMMG

			// G.Brogi 16/05/06
			// String anno = (su.getTodayDate("dd/MM/yyyy")).substring(6,10);

			String strConfKey = "PIPP" + strTipo + "_"
					+ (String) h.get("metipo");// gb 18.12.08
			String strPar = selectConf(dbc, strConfKey, strCodOperatore);
			for (Enumeration senum = vdb.elements(); senum.hasMoreElements();) {
				ISASRecord dbrdes = (ISASRecord) senum.nextElement();
				// Sostituito l'interazione con la tabella CONF dal fatto che
				// abbiamo preso
				// il valore di conf_txt direttamente da CONF con il metodo
				// selectConf.
				// (Valori parametrici su zona in conf_txt).
				String selIntmmg = "SELECT i.* FROM intmmg i WHERE"
						+ " int_mese='" + mese + "' AND" + " int_medico='"
						+ medico + "' AND" + " int_anno='" + anno + "' AND"
						+ " int_cartella=" + (Integer) dbrdes.get("n_cartella")
						+ " AND int_tipo_pres='3'" + " AND int_prestaz = '"
						+ strPar + "'"
						+ " AND (int_exp='N' OR int_exp IS NULL)";
						
				// 27/05/11: leggo gli accessi nel periodo dell'autorizzazione (se specificato) -----------------------------		
				if ((dbrdes.get("data_inizio") != null) && (!dbrdes.get("data_inizio").toString().trim().equals("")))
					selIntmmg += " AND int_data >= " + formatDate(dbc, dbrdes.get("data_inizio").toString());

/*** 05/07/11: neanche in Veneto si deve considerare il flag fineEffettiva 					
				// 31/05/11: in VENETO si deve controllare solo se dtFineEffettiva
				String fineEffettiva = (String)dbrdes.get("fine_effettiva");
                if ((!isVeneto) || ((fineEffettiva != null) && (fineEffettiva.trim().equals("S")))) {											
					if ((dbrdes.get("data_fine") != null) && (!dbrdes.get("data_fine").toString().trim().equals("")))
						selIntmmg += " AND int_data <= " + formatDate(dbc, dbrdes.get("data_fine").toString());
				}
***/
				// 05/07/11
				if ((dbrdes.get("data_fine") != null) && (!dbrdes.get("data_fine").toString().trim().equals("")))
						selIntmmg += " AND int_data <= " + formatDate(dbc, dbrdes.get("data_fine").toString());
				// 27/05/11 -----------------------------
								
				LOG.debug("queryKey() query Intmmg = " + selIntmmg);
				dbcurInt = dbc.startCursor(selIntmmg);
				int i = 0;
				int tot_qta = 0;
				String valori = "";
				String rit_date = "";
				while (dbcurInt.next()) {
					ISASRecord dbInt = dbcurInt.getRecord();
					int qta = 0;
					String data = "" + (java.sql.Date) dbInt.get("int_data");
					if (dbInt.get("int_qta") != null)
						qta = ((Integer) dbInt.get("int_qta")).intValue();
					tot_qta += qta;
					if (i == 0) {
						valori += data.substring(8, 10) + "-" + qta;
						rit_date += data.substring(8, 10);
					} else {
						valori += "|" + data.substring(8, 10) + "-" + qta;
						rit_date += " - " + data.substring(8, 10);
					}
					i++;
				}
				// System.out.println("Valori: "+valori);
				dbrdes.put("valori", valori);
				dbrdes.put("date", rit_date);
				dbrdes.put("accessi_eff", "" + tot_qta);
			}
			if (vdb.size() > 0)
				dbr.put("tabella", vdb);

			return dbr;
		} catch (ISASPermissionDeniedException ex) {
			LOG.info("queryKey() - permesso negato: " + ex.getMessage(), ex);
			return null;
		} catch (Exception ex) {
			throw newEjbException("errore in queryKey() " + ex.getMessage(), ex);
		} finally {
		        close_dbcur_nothrow("queryKey", dbcurInt);
		        close_dbcur_nothrow("queryKey", dbcur2);
		        close_dbcur_nothrow("queryKey", dbcur);
		        logout_nothrow("queryKey", dbc);
		}
	}

	public ISASRecord query_residenza(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		String codice = "";
		String lastDate = "";// Data a cui si vuole la situazione
		codice = (String) h.get("n_cartella");
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM anagra_c " + " WHERE n_cartella="
					+ codice
					+ " AND data_variazione IN (SELECT MAX (data_variazione)"
					+ " FROM anagra_c WHERE n_cartella=" + codice + ")";
			LOG.debug("query_residenza() select = " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				// comune residenza
				if (dbr.get("citta") != null && !dbr.get("citta").equals("")) {
					String myselcomunires = "SELECT * FROM comuni"
							+ " WHERE codice='" + dbr.get("citta") + "'";
					ISASRecord dbrcomunires = dbc.readRecord(myselcomunires);
					if (dbrcomunires != null) {
						dbr.put("resreg", dbrcomunires.get("cod_reg"));
						dbr.put("resusl", dbrcomunires.get("cod_usl"));
					}
				}
			}
			return dbr;
		} catch (Exception e) {
			throw newEjbException("Errore in query_residenza(): " + e.getMessage(), e);
		} finally {
		        logout_nothrow("query_residenza", dbc);
		}
	} // fine query_residenza
   

	public ISASRecord controlloExport(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		String codice = "";
		codice = (String) h.get("n_cartella");
		ISASRecord dbr = null;
		String strCodOperatore = (String) h.get("cod_oper"); // gb 17/07/08
		String strTipo = (String) h.get("tipo"); // gb 17/07/08
		try {
			dbc = super.logIn(mylogin);
			/*
			 * String myselect= "SELECT DISTINCT i.* FROM intmmg i, conf WHERE"+
			 * " int_mese='"+(String)h.get("int_mese")+"' AND"+
			 * " int_medico='"+(String)h.get("int_medico")+"' AND"+
			 * " int_anno='"+(String)h.get("int_anno")+"' AND"+
			 * " int_cartella="+codice+ " AND int_tipo_pres='3' AND"+
			 * " conf_kproc='SINS' and conf_key='PIPP"
			 * +(String)h.get("tipo")+"' AND"+
			 * " conf_txt=int_prestaz AND int_exp='S'";
			 */
			// gb 17/07/08: Sostituito l'interazione con la tabella CONF dal
			// fatto che abbiamo preso
			// il valore di conf_txt direttamente da CONF con il metodo
			// selectConf.
			// (Valori parametrici su zona in conf_txt).
			// gb 18.12.08 String strConfKey = "PIPP" + strTipo;
			String strConfKey = "PIPP" + strTipo + "_"
					+ (String) h.get("metipo"); // gb 18.12.08
			String strPar = selectConf(dbc, strConfKey, strCodOperatore);

			String myselect = "SELECT DISTINCT i.* FROM intmmg i WHERE"
					+ " int_mese='" + (String) h.get("int_mese") + "' AND"
					+ " int_medico='" + (String) h.get("int_medico") + "' AND"
					+ " int_anno='" + (String) h.get("int_anno") + "' AND"
					+ " int_cartella=" + codice + " AND int_tipo_pres='3'"
					+ " AND int_prestaz = '" + strPar + "'"
					+ " AND int_exp='S'";
			// gb 17/07/08: fine

			LOG.debug("controlloExport() select = " + myselect);
			dbcur = dbc.startCursor(myselect);
			Vector vdb = dbcur.getAllRecord();
			String date = "";
			if (vdb.size() != 0) {
				for (int i = 0; i < vdb.size(); i++) {
					dbr = (ISASRecord) vdb.elementAt(i);
					// System.out.println("Date:"+dbr.getHashtable().toString());
					String d1 = "" + (java.sql.Date) dbr.get("int_data");
					d1 = d1.substring(8, 10) + "/" + d1.substring(5, 7) + "/"
							+ d1.substring(0, 4);
					if (i == 0)
						date = d1;
					else
						date += " - " + d1;
				}
				dbr.put("date_int", date);
				dbr.put("numero", "" + vdb.size());
			}
			return dbr;
		} catch (Exception e) {
			throw newEjbException("Errore in controlloExport(): " + e.getMessage(), e);
		} finally {
		        logout_nothrow("controlloExport", dbcur, dbc);
		}
	} // fine controlloExport

	public ISASRecord insert(myLogin mylogin, Hashtable h, Vector v)
			// gb 18.12.08 throws DBRecordChangedException,
			// ISASPermissionDeniedException, SQLException {
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException { // gb 18.12.08
		boolean done = false;
		// G.Brogi 16/05/06 aggiunto anno
		String anno = null;

		String mese = null;
		String medico = null;
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		ServerUtility su = new ServerUtility();
		// gb 18.12.08
		String mex = "Impossibile inserire la prestazione nei giorni:\n";

		String strCodOperatore = "";
		try {
			anno = (String) h.get("anno");
			mese = (String) h.get("mese");
			medico = (String) h.get("medico");
			strCodOperatore = (String) h.get("cod_oper");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try {
			dbc = super.logIn(mylogin);
			// G.Brogi 16/05/06
			// String anno = (su.getTodayDate("dd/MM/yyyy")).substring(6,10);
			System.out.println(">>>H:" + h.toString());
			/*
			 * vado ad inserire i record in intmmg tanti quanti sono le date
			 * salvate nella stringa 'valori'
			 */
			/*
			 * Prima di andare a inserire i record cancello tutto quello che ho
			 * gi� inserito in INTMMG con quelle chiavi, in modo da non
			 * duplicare niente
			 */

			// 15/02/10 m
			dbc.startTransaction();
			int risu = 0;

			// 31/05/11
			isVeneto = (gestCaso.isUbicazRegVeneto(dbc, new Hashtable())).booleanValue();
			
			// 15/02/10 m. CancellaRecord(dbc, h, v);
			// 15/02/10 m.: per verificare i record gi� presenti sul DB e NON inviare evento EROGAZIONE
			Hashtable h_recExist = CancellaRecord(dbc, h, v);
			System.out.println("----> RegAccessiEJB.insert(): h_recExist size=[" + h_recExist.size() + "]");

			for (Enumeration en = v.elements(); en.hasMoreElements();) {
				Hashtable hin = (Hashtable) en.nextElement();
				String valori = (String) hin.get("valori");
				System.out.println("----> RegAccessiEJB.insert(): valori =["+ valori + "]");

				if (valori != null && !valori.trim().equals("")) {
					// Vado a formare le date
					StringTokenizer val = new StringTokenizer(valori, "|");
					while (val.hasMoreElements()) {
						dbr = dbc.newRecord("intmmg");
						StringTokenizer date_acc = new StringTokenizer(val.nextToken(), "-");
						String date = date_acc.nextToken();
						if (date.length() == 1)
							date = "0" + date;
						System.out.println("Giorno:" + date);
						dbr.put("int_data", anno + "-" + mese + "-" + date);
						String acc = date_acc.nextToken();
						System.out.println("Quantit�:" + acc);
						dbr.put("int_qta", "" + acc);
						dbr.put("int_mese", mese);
						dbr.put("int_anno", anno);
						dbr.put("int_data_reg", su.getTodayDate("yyyy-MM-dd"));
						dbr.put("int_medico", medico);
						dbr.put("int_exp", "N");
						dbr.put("int_codoper", (String) h.get("cod_oper"));
						dbr.put("int_cartella", new Integer((String) hin.get("n_cartella")));
						// il tipo � sempre a 3 perch� sono sempre 'ACCESSI'
						dbr.put("int_tipo_pres", "3");
						dbr.put("int_importo", "");
						dbr.put("int_prestaz", "");
//						dbr.put("flag_sent", "0");
						// Devo prendere i dati dalla tabella TABPIPP
						// gb 17/07/08 ISASRecord dbPipp = PrendiDati(dbc,
						// (String)h.get("tipo"));
						// gb 17/07/08
						// gb 18.12.08 ISASRecord dbPipp = PrendiDati(dbc,
						// (String)h.get("tipo"), strCodOperatore);
						// gb 18.12.08
						// 29/10/2008 se esiste una data di fine validit� della
						// prestazione
						// minore della data intervento non devo inserire niente
						// 30/10/2008 Devo prendere la prestazione di default
						// anche in base
						// al tipo medico
						// ISASRecord dbPipp = PrendiDati(dbc,
						// (String)h.get("tipo"));
						ISASRecord dbPipp = PrendiDati(dbc, (String) h.get("tipo"), (String) h.get("metipo"),
								strCodOperatore,dbr.get("int_data").toString());
							String data_fine = "";
						// gb 18.12.08: fine
						if (dbPipp != null) {
							dbr.put("int_prestaz", (String) dbPipp.get("pipp_codi"));
							if (dbPipp.get("pipp_importo") != null)
								dbr.put("int_importo", ""
										+ (Double) dbPipp.get("pipp_importo"));
							// gb 18.12.08
							if (dbPipp.get("pipp_datafine") != null)
								data_fine = "" + dbPipp.get("pipp_datafine");
							// gb 18.12.08: fine
						}
						// Dati del panelKey
						dbr.put("mese", mese);
						// G.Brogi 16/05/06
						dbr.put("anno", anno);
						dbr.put("medico", medico);
						dbr.put("tipo", (String) h.get("tipo"));

						// 15/02/10 m.: chiave tabella INT_MMG
						Hashtable h_fromDbr = (Hashtable) dbr.getHashtable();
						String key_recExist = anno + "|" + mese + "|" + medico
								+ "|3|" + (String) hin.get("n_cartella") + "|"
								+ anno + "-" + mese + "-" + date + "|"
								+ h_fromDbr.get("int_prestaz");

						// gb 18.12.08
						if (data_fine != null && !data_fine.equals("")) {
							String data_intervento = anno + "-" + mese + "-"
									+ date;
							java.sql.Date dataInt = java.sql.Date.valueOf(data_intervento);
							java.sql.Date dataFine = java.sql.Date.valueOf(data_fine);
							System.out.println("----> data intervento=" + data_intervento);
							System.out.println("----> data fine validita'=" + data_fine);

							if (dataInt.before(dataFine)) {
//								if (!h_recExist.containsKey(key_recExist))
//									risu = gestCasoAndErogaz(dbc, h_fromDbr);
								if (!h_recExist.containsKey(key_recExist))
									dbr.put("flag_sent", CostantiSinssntW.FLAG_DA_INVIARE_I);
								else
									dbr.put("flag_sent", h_recExist.get(key_recExist).toString());
								dbc.writeRecord(dbr);

								// 15/02/10
							} else {
								mex += " " + date + " ";
							}
						} else {
							// gb 18.12.08: fine
//							if (!h_recExist.containsKey(key_recExist))
//								risu = gestCasoAndErogaz(dbc, h_fromDbr);
							if (!h_recExist.containsKey(key_recExist))
								dbr.put("flag_sent", CostantiSinssntW.FLAG_DA_INVIARE_I);
							else
								dbr.put("flag_sent", h_recExist.get(key_recExist).toString());
							dbc.writeRecord(dbr);

							// 15/02/10
						}
					}
				}// fine if valori
			}// end for
			// gb 18.12.08
			if (!mex.equals("Impossibile inserire la prestazione nei giorni:\n"))
				throw new CariException(mex);
			// gb 18.12.08: fine

			// 05/11/10 ---
			if (dbr == null) {
				dbr = dbc.newRecord("intmmg");
				// Dati del panelKey
				dbr.put("mese", mese);
				dbr.put("anno", anno);
				dbr.put("medico", medico);
				dbr.put("tipo", (String) h.get("tipo"));
			}

			// 15/02/10 m
			dbc.commitTransaction();
		        done = true;
			return dbr;
			// gb 18.12.08
		} catch (CariException ce) {
			ce.setISASRecord(dbr);
			throw ce;
			// gb 18.12.08: fine
		} catch (Exception e) {
			throw newEjbException("Errore in insert(): " + e.getMessage(), e);
		} finally {
		        // Max, 2012/01/18
			try {
			        if (!done)
				        dbc.rollbackTransaction();
			} catch (Throwable e2) {
				LOG.error("insert(): errore in ISASConnection.rollback(): " + e2.getMessage(), e2);
			}
		        logout_nothrow("insert", dbc);
		}
	}

	// gb 18.12.08 private ISASRecord PrendiDati(ISASConnection dbc, String
	// tipo, /*gb 17/07/08 */ String strCodOperatore)
	// gb 18.12.08
	private ISASRecord PrendiDati(ISASConnection dbc, String tipo,
			String metipo, /* gb 17/07/08 */String strCodOperatore, String int_data)
			throws SQLException {
		try {
			/*
			 * String sel =
			 * "SELECT pipp_codi, pipp_importo FROM tabpipp, conf WHERE "+
			 * "conf_kproc='SINS' and conf_key='PIPP"+tipo+"' AND "+
			 * "conf_txt=pipp_codi AND pipp_tipo='3'";
			 */
			// gb 17/07/08: Sostituito l'interazione con la tabella CONF dal
			// fatto che abbiamo preso
			// il valore di conf_txt direttamente da CONF con il metodo
			// selectConf.
			// (Valori parametrici su zona in conf_txt).
			// gb 18.12.08 String strConfKey = "PIPP"+tipo;
			String strConfKey = "PIPP" + tipo + "_" + metipo; // gb 18.12.08
			String strPar = selectConf(dbc, strConfKey, strCodOperatore);
			/*
			 * gb 18.12.08 String sel =
			 * "SELECT pipp_codi, pipp_importo FROM tabpipp WHERE "+
			 * "pipp_codi = '" + strPar + "'" + " AND pipp_tipo='3'";gb 18.12.08
			 */
			// gb 17/07/08: fine
			// gb 18.12.08
			/*
			 * 29/10/2008 prendo l'importo dalla nuova tabella degli importi
			 * delle pipp
			 */
			String sel = "SELECT t.pipp_codi, t.pipp_datafine, i.pipp_importo "
					+ "FROM tabpipp t, pipp_importi i "
					+ " WHERE t.pipp_codi = '" + strPar	+ "'"
					+ " AND t.pipp_tipo='3' "
					+
					// 29/10/2008
					" AND t.pipp_tipo=i.pipp_tipo AND t.pipp_codi=i.pipp_codi"
					+ " AND i.pipp_data IN (SELECT MAX(im.pipp_data) FROM pipp_importi im "
					+ " WHERE im.pipp_tipo=i.pipp_tipo AND im.pipp_codi=i.pipp_codi)"
					// Simone 15/03/2017 l'importo che deve essere preso è quello con data massima precedente alla data della prestazione, non massima in assoluto
					+ " AND im.pipp_data <= "+ dbc.formatDbDate(int_data)
					+")";
			// fine 29/10/2008
			// gb 18.12.08: fine
			System.out.println("PrendiDati: " + sel);
			ISASRecord dbr = dbc.readRecord(sel);
			return dbr;
		} catch (Exception e) {
			throw newEjbException("Errore in PrendiDati(): " + e.getMessage(), e);
		}
	}

	// 15/02/10 m. private void CancellaRecord(ISASConnection dbc, Hashtable h, Vector v)
	private Hashtable CancellaRecord(ISASConnection dbc, Hashtable h, Vector v)
			throws SQLException {
		ISASCursor dbcurInt = null;
		ServerUtility su = new ServerUtility();
		try {
			// 15/02/10 m.: per verificare i record gi� presenti sul DB e NON
			// inviare evento EROGAZIONE
			Hashtable h_rec = new Hashtable();
			String anno = (String) h.get("anno");
			String strCodOperatore = (String) h.get("cod_oper"); // gb 17/07/08
			String strTipo = (String) h.get("tipo"); // gb 17/07/08
			for (Enumeration senum = v.elements(); senum.hasMoreElements();) {
				Hashtable pi = (Hashtable) senum.nextElement();
				System.out.println("Hahs:" + pi.toString());
				/*
				 * String selIntmmg="SELECT i.* FROM intmmg i, conf WHERE"+
				 * " int_mese='"+(String)h.get("mese")+"' AND"+
				 * " int_medico='"+(String)h.get("medico")+"' AND"+
				 * " int_anno='"+anno+"' AND"+
				 * " int_cartella="+(String)pi.get("n_cartella")+
				 * " AND int_tipo_pres='3' AND"+
				 * " conf_kproc='SINS' and conf_key='PIPP"
				 * +(String)h.get("tipo")+"' AND"+
				 * " conf_txt=int_prestaz AND (int_exp='N' OR int_exp IS NULL)";
				 */
				// gb 17/07/08
				// gb 17/07/08: Sostituito l'interazione con la tabella CONF dal
				// fatto che abbiamo preso
				// il valore di conf_txt direttamente da CONF con il metodo
				// selectConf.
				// (Valori parametrici su zona in conf_txt).
				// gb 18.12.08 String strConfKey = "PIPP" + strTipo;
				String strConfKey = "PIPP" + strTipo + "_"
						+ (String) h.get("metipo"); // gb 18.12.08
				String strPar = selectConf(dbc, strConfKey, strCodOperatore);
				String selIntmmg = "SELECT i.* FROM intmmg i WHERE"
						+ " int_mese='" + (String) h.get("mese") + "' AND"
						+ " int_medico='" + (String) h.get("medico") + "' AND"
						+ " int_anno='" + anno + "' AND" + " int_cartella="
						+ (String) pi.get("n_cartella")
						+ " AND int_tipo_pres='3'" + " AND int_prestaz = '"
						+ strPar + "'"
						+ " AND (int_exp='N' OR int_exp IS NULL)";
						
				// 27/05/11: leggo gli accessi nel periodo dell'autorizzazione (se specificato) -----------------------------		
				if ((pi.get("data_inizio") != null) && (!pi.get("data_inizio").toString().trim().equals("")))
					selIntmmg += " AND int_data >= " + formatDate(dbc, pi.get("data_inizio").toString());

/*** 05/07/11: neanche in Veneto si deve considerare il flag fineEffettiva 						
				// 31/05/11: in VENETO si deve controllare solo se dtFineEffettiva
				String fineEffettiva = (String)pi.get("fine_effettiva");
				if ((!isVeneto) || ((fineEffettiva != null) && (fineEffettiva.trim().equals("S")))) {						
					if ((pi.get("data_fine") != null) && (!pi.get("data_fine").toString().trim().equals("")))
						selIntmmg += " AND int_data <= " + formatDate(dbc, pi.get("data_fine").toString());
				}
***/
				// 05/07/11
				if ((pi.get("data_fine") != null) && (!pi.get("data_fine").toString().trim().equals("")))
						selIntmmg += " AND int_data <= " + formatDate(dbc, pi.get("data_fine").toString());
				// 27/05/11 -----------------------------		
						
				// gb 17/07/08: fine
				System.out.println("***CancellaRecord-->" + selIntmmg);
				dbcurInt = dbc.startCursor(selIntmmg);
				while (dbcurInt.next()) {
					ISASRecord dbInt = dbcurInt.getRecord();
					String sel2 = "SELECT i.* FROM intmmg i WHERE"
							+ " int_mese='"
							+ (String) h.get("mese")
							+ "' AND"
							+ " int_medico='"
							+ (String) h.get("medico")
							+ "' AND"
							+ " int_anno='"
							+ anno
							+ "' AND"
							+ " int_cartella="
							+ (Integer) dbInt.get("int_cartella")
							+ " AND int_tipo_pres='3' AND"
							+ " (int_exp='N' OR int_exp IS NULL) AND"
							+ " int_prestaz ='"
							+ (String) dbInt.get("int_prestaz")
							+ "' AND "
							+ " int_data="
							+ formatDate(dbc, (String) util.getObjectField(
									dbInt, "int_data", 'T'));
					System.out.println("***CancellaRecord2-->" + sel2);
					ISASRecord dbCanc = dbc.readRecord(sel2);

					if (dbCanc != null) {
						 // 15/02/10 m.: chiave tabella INT_MMG ---
						String key_rec = anno + "|" + h.get("mese") + "|"
								+ h.get("medico") + "|3|"
								+ dbCanc.get("int_cartella") + "|"
								+ dbCanc.get("int_data") + "|"
								+ dbCanc.get("int_prestaz");
						h_rec.put(key_rec, dbCanc.get("flag_sent")!=null?dbCanc.get("flag_sent").toString():"0");
						// 15/02/10 m. --------------------------------------

						dbc.deleteRecord(dbCanc);
					}			
					
				}
			}
			if (dbcurInt != null)
				dbcurInt.close();

			// 15/02/10 m.
			return h_rec;
		} catch (Exception e) {
			throw newEjbException("Errore in CancellaRecord(): " + e.getMessage(), e);
		}
	}

	public ISASRecord query_controlloTipi(myLogin mylogin, Hashtable h)
			throws SQLException, CariException {
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		String myselect = "";
		String mex = "";
		String cartella = "";
		String medico = "";
		String data = "";
		String sottotipo = "";
		try {
			it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
			// System.out.println("query_controlloTipi:"+h.toString());
			dbc = super.logIn(mylogin);
			cartella = (String) h.get("int_cartella");
			medico = (String) h.get("int_medico");
			data = (String) h.get("int_data");
			sottotipo = (String) h.get("pipp_sottotipo");
			// controllo che nn siano gia' state fatte prestazioni di tipo PIPP
			// questo controllo va fatto per tutti i sottotipi (ADI ADP E ADR)
			myselect = "SELECT * FROM intmmg WHERE int_cartella=" + cartella
					+ " AND int_medico='" + medico + "'" + " AND int_data="
					+ formatDate(dbc, data) + " AND int_tipo_pres='1'";
			System.out.println("REGACCESSI select x Pipp: " + myselect);
			dbcur = dbc.startCursor(myselect);
			if (dbcur.next()) {
				mex += "Impossibile inserire la prestazione: \nesistono gia' prestazioni di tipo PIPP nello stesso giorno,\n "
						+ "allo stesso paziente e per lo stesso medico!";
				throw new CariException(mex);
			}
			if (sottotipo.equals("1") || sottotipo.equals("2")) {// caso ADI e
																	// ADP
				String tipo_dacercare = "";
				if (sottotipo.equals("1"))
					tipo_dacercare = "2";
				else if (sottotipo.equals("2"))
					tipo_dacercare = "1";
				// controllo che nn ci siano prestazioni con sottotipo diverso
				// da quello che si sta inserendo
				myselect = "SELECT * FROM intmmg, tabpipp WHERE int_cartella="
						+ cartella + " AND int_medico='" + medico + "'"
						+ " AND int_data=" + formatDate(dbc, data)
						+ " AND int_tipo_pres='3'"
						+ " AND pipp_tipo = int_tipo_pres"
						+ " AND pipp_codi = int_prestaz"
						+ " AND pipp_sottotipo='" + tipo_dacercare + "'";
				System.out
						.println("CASO ACCESSI: select x il controllo dei sottotipi diversi=>"
								+ myselect);
				dbcur = dbc.startCursor(myselect);
				if (dbcur.next()) {
					if (sottotipo.equals("1"))
						mex += "Impossibile inserire la prestazione: \nesistono gia' ACCESSI di tipo ADP nello stesso giorno,\n "
								+ "allo stesso paziente e per lo stesso medico!";
					else if (sottotipo.equals("2"))
						mex += "Impossibile inserire la prestazione: \nesistono gia' ACCESSI di tipo ADI nello stesso giorno,\n "
								+ "allo stesso paziente e per lo stesso medico!";
				}
			}
			ISASRecord dbr = dbc.newRecord("intmmg");
			dbr.put("int_cartella", cartella);
			dbr.put("int_medico", medico);
			dbr.put("int_data", data);
			dbr.put("pipp_sottotipo", sottotipo);
			if (!mex.equals(""))
				throw new CariException(mex);

			return dbr;
		} catch (CariException ce) {
			throw new CariException(mex, -1);
			// throw ce;
		} catch (Exception e) {
			throw newEjbException("Errore in query_controlloTipi(): " + e.getMessage(), e);
		} finally {
		        logout_nothrow("query_controlloTipi", dbcur, dbc);
		}
	}

	public byte[] query_report(String utente, String passwd, Hashtable par,
			mergeDocument doc) throws SQLException {

		LOG.debug("query_report(): inizio...");
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		ServerUtility su = new ServerUtility();
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			// compongo la select da eseguire
			String sel = "";
			String mese = (String) par.get("mese");
			String medico = (String) par.get("medico");
			String tipo = (String) par.get("tipo");
			// G.Brogi 16/05/06 l'anno arriva dal client
			String anno = (String) par.get("anno");
			// String anno = (su.getTodayDate("dd/MM/yyyy")).substring(6,10);
			
			// 15/06/11  ---
			String tabella = "";
			String dataIni = "";
			String dataFine = "";
			String fineEff = "";
			
			isVeneto = (gestCaso.isUbicazRegVeneto(dbc, new Hashtable())).booleanValue();
			// 15/06/11  ---
			
			
			sel = su
					.addWhere(sel, su.REL_AND, "i.int_mese", su.OP_EQ_STR, mese);
			sel = su
					.addWhere(sel, su.REL_AND, "i.int_anno", su.OP_EQ_STR, anno);
			sel = su.addWhere(sel, su.REL_AND, "i.int_medico", su.OP_EQ_STR,
					medico);
			sel = su.addWhere(sel, su.REL_AND, "i.int_tipo_pres", su.OP_EQ_STR,
					"3");
			String mysel = "SELECT i.int_data, i.int_qta, i.int_prestaz, t.pipp_des,"
					+ " c.n_cartella, c.cod_fisc, c.cod_reg,"
					+ "nvl(trim(c.cognome),'') ||' ' || nvl(trim(c.nome),'') assistito "
					+ "FROM intmmg i, tabpipp t,cartella c WHERE "
					+ "c.n_cartella=i.int_cartella AND "
					+ "i.int_prestaz=t.pipp_codi AND "
					+ "i.int_tipo_pres=t.pipp_tipo AND "
					+ "t.pipp_sottotipo='"
					+ tipo + "'";
			if (!sel.equals(""))
				mysel += " AND " + sel;
				
			// 15/06/11 m.  leggo gli accessi nel periodo dell'autorizzazione (se specificato) -----------------------------				
			if (tipo.equals("1")) {
				tabella = "adi";
				dataIni = "skadi_data_inzio";
				dataFine = "skadi_data_fine";
				fineEff = "skadi_fine_effettiva";
			} else if (tipo.equals("2")) { 
				tabella = "adp";
				dataIni = "skadp_data_inizio";
				dataFine = "skadp_data_fine";
				fineEff = "skadp_fine_effettiva";
			}
			
			String existsAuto = " AND EXISTS (SELECT a.* FROM skmmg_" + tabella + " a"
						+ " WHERE a.n_cartella = i.int_cartella"
						+ " AND ((a." + dataIni + " IS NULL) OR (a." + dataIni + " <= i.int_data))";
						
/*** 05/07/11: neanche in Veneto si deve considerare il flag fineEffettiva 	
			if (!isVeneto)
				existsAuto += " AND ((a." + dataFine + " IS NULL) OR (a." + dataFine + " >= i.int_data))";				
			else //  in VENETO si deve controllare solo se dtFineEffettiva
				existsAuto += " AND ((a." + dataFine + " IS NULL)"
									+ " OR (a." + dataFine + " >= i.int_data)"
									+ " OR (a." + fineEff + " <> 'S')"
									+ " OR (a." + fineEff + " IS NULL))";
									
			existsAuto += ")";
***/
			// 05/07/11 --
			existsAuto += " AND ((a." + dataFine + " IS NULL) OR (a." + dataFine + " >= i.int_data))";	
			existsAuto += ")";
			// 05/07/11 --
			
			mysel += existsAuto;				
			// 15/06/11 m.  leggo gli accessi nel periodo dell'autorizzazione (se specificato) -----------------------------
			
			mysel += " ORDER BY i.int_data, i.int_cartella";

			LOG.debug("query_report() select = " + mysel);

			dbcur = dbc.startCursor(mysel);
			if (((String) par.get("TYPE")).equals("application/vnd.ms-excel")) {
				if (dbcur == null) {
					doc.write("messaggio");
				} else {
					if (dbcur.getDimension() <= 0)
						doc.write("messaggio");
					else {
						doc.write("layout");
						while (dbcur.next()) {
							ISASRecord dbr = dbcur.getRecord();
							preparaBody(doc, dbc, dbr);
						}
						doc.write("fineTab");
					}
				}
			} else {
				preparaLayout(doc, dbc, par);
				doc.write("iniziotab");
				if (dbcur != null) {
					while (dbcur.next()) {
						ISASRecord dbr = dbcur.getRecord();
						preparaBody(doc, dbc, dbr);
					}
					Hashtable Htot = new Hashtable();
					doc.write("fineTab");
					Htot.put("#totale#", "" + tot);
					doc.writeSostituisci("totale", Htot);
				} else
					doc.write("messaggio");
			}
			doc.write("finale");
			doc.close();
			return doc.get();
		} catch (Exception e) {
			throw newEjbException("errore in query_report(): " + e.getMessage(), e);
		} finally {
		        logout_nothrow("query_report", dbcur, dbc);
		}
	} // End of query_report()

	private void preparaLayout(mergeDocument md, ISASConnection dbc,
			Hashtable par) {
		Hashtable hLayout = new Hashtable();
		try {
			String[] Vmesi = { "GENNAIO", "FEBBRAIO", "MARZO", "APRILE",
					"MAGGIO", "GIUGNO", "LUGLIO", "AGOSTO", "SETTEMBRE",
					"OTTOBRE", "NOVEMBRE", "DICEMBRE" };
			ISASUtil util = new ISASUtil();
			String mysel = "SELECT conf_txt FROM conf WHERE "
					+ "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			hLayout.put("#txt#", (String) dbtxt.get("conf_txt"));
			ServerUtility su = new ServerUtility();
			hLayout.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
			int mese = Integer.parseInt((String) par.get("mese"));
			hLayout.put("#mese#", Vmesi[mese - 1]);

			// G.Brogi 16/05/06 aggiunto anno
			hLayout.put("#anno#", (String) par.get("anno"));

			hLayout
					.put(
							"#medico#",
							par.get("medico")
									+ " "
									+ util
											.getDecode(
													dbc,
													"medici",
													"mecodi",
													par.get("medico"),
													"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))",
													"desc_med"));
			// gb 18.12.08 hLayout.put("#tipo#",
			// (((String)par.get("tipo")).equals("1")?"ADI":"ADP"));
			String tipo = ((String) par.get("tipo"));
			if (tipo.equals("1"))
				tipo = "ADI";
			else if (tipo.equals("2"))
				tipo = "ADP";
			else if (tipo.equals("3"))
				tipo = "ADR";
			hLayout.put("#tipo#", tipo);
		} catch (Exception ex) {
			hLayout.put("#txt#", "ragione_sociale");
		}
		md.writeSostituisci("layout", hLayout);
	}

	public void preparaBody(mergeDocument md, ISASConnection dbc, ISASRecord dbr)
			throws SQLException {
		try {
			Hashtable p = new Hashtable();
// 15/06/11 m tot++;
			// 15/06/11 m.: somma quantit� --
			int qta = 0;
			if (dbr.get("int_qta") != null)
				qta = ((Integer)dbr.get("int_qta")).intValue();
			tot += qta;
			// 15/06/11 m. --------------------
			
			String data = (String) util.getObjectField(dbr, "int_data", 'T');
			// if(!data.equals(""))
			// data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
			p.put("#data#", data);
			p.put("#assistito#", (Integer) dbr.get("n_cartella") + " "
					+ (String) dbr.get("assistito"));
			p.put("#c_f#", (String) util.getObjectField(dbr, "cod_fisc", 'S'));
			p.put("#c_s#", (String) util.getObjectField(dbr, "cod_reg", 'S'));
			p.put("#qta#", (String) util.getObjectField(dbr, "int_qta", 'I'));
			p.put("#prestaz#", (String) util.getObjectField(dbr, "int_prestaz",
					'S')
					+ " - "
					+ (String) util.getObjectField(dbr, "pipp_des", 'S'));
			md.writeSostituisci("tabella", p);
			return;
		} catch (Exception e) {
			throw newEjbException("Errore in preparaBody(): " + e.getMessage(), e);
		}
	}

	// gb 17/07/08
	// Se arriva il parametro strOperatore != "" allora significa che si deve
	// ottenere la zona dall'operatore e poi, dalla zona, ricavare il parametro
	// dalla stringa conf_txt di conf, per la data chiave 'strConfKey',
	// perch� si suppone che sia parametrica a secondo della zona (es.
	// #1=SI#2=NO#3=NO#4=NO)
	// Se invece il parametro strOperatore == "" allora si legge direttamente il
	// valore dal
	// campo conf_txt di conf.
	private String selectConf(ISASConnection dbc, String strConfKey,
			String strOperatore) throws SQLException {
		String ret = "";
		String strZona = "";
		String strZonaOper = "";
		try {
			if (!strOperatore.equals("")) {
				strZonaOper = getZonaFromOperatore(dbc, strOperatore);
				if (strZonaOper.trim().equals(""))// non esiste zona per l'oper
					System.out.println("!!!! RegAccessiEJB.selectConf: NON esiste " +
							"zona su OPERATORI per l'operatore=[" + strOperatore + "] !!!!");
			}

			String mysel = "SELECT conf_txt FROM conf WHERE "
					+ "conf_kproc='SINS' AND conf_key='" + strConfKey + "'";
			System.out.println("RegAccessiEJB/selectConf/mysel: " + mysel);
			ISASRecord dbConf = dbc.readRecord(mysel);
			if ((dbConf != null) && dbConf.get("conf_txt") != null) {
				if (strZonaOper.equals(""))
					ret = (String) dbConf.get("conf_txt");
				else {
					String strVal = (String) dbConf.get("conf_txt");
					ret = getValxZona(strVal, strZonaOper);
				}
			}
			return ret;
		} catch (Exception ex) {
			throw newEjbException("errore in selectConf(): " + ex.getMessage(), ex);
		}
	}

	// configurazione diversa per ogni zona -> si prevede una codifica
	// del tipo "#codZona1=xxxx#codZona2=yyyy....#codZonaN=zzzz".
	private String getValxZona(String val, String zonaOper) {
		String rit = "";

		// non esiste codifica per zona -> ritorno il valore cos� com'� letto,
		// visto che il valore � unico per tutte le zone.
		if (val.indexOf("#") == -1)
			return val;

		if ((zonaOper != null) && (!zonaOper.trim().equals(""))) {
			boolean trovato = false;
			String keyZona = zonaOper + "=";
			StringTokenizer strTkzZona = new StringTokenizer(val, "#");
			while ((strTkzZona.hasMoreTokens()) && (!trovato)) {
				String tkZona = strTkzZona.nextToken();
				int pos = tkZona.indexOf(keyZona);
				trovato = (pos != -1);
				if (trovato)
					rit = tkZona.substring(pos + zonaOper.length() + 1);
			}
		}

		if (rit.trim().equals("")) {
		   // non esiste codifica x la zona dell'oper
		   // (oppure oper senza zona!)
		   LOG.warn("getValxZona(): NON esiste codifica su CONF per la zona=[" + zonaOper + "]");
		}
		return rit;
	}

	private String getZonaFromOperatore(ISASConnection dbc, String strOperatore)
			throws SQLException {
		try {
			String ret = "";
			String strQuery = "SELECT cod_zona" + " FROM operatori"
					+ " WHERE codice = '" + strOperatore + "'";
		        LOG.debug("getZonaFromOperatore() select = " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			if ((dbr != null) && dbr.get("cod_zona") != null)
				ret = (String) dbr.get("cod_zona");
			return ret;
		} catch (Exception ex) {
			throw newEjbException("errore in getZonaFromOperatore(): " + ex.getMessage(), ex);
		}
	}

	// gb 17/07/08: fine

	// 15/02/10 m.: gestione CASO: se NON esiste nessun caso ATTIVO, si inserisce un caso SAN ---------------------------------
//	private int gestCasoAndErogaz(ISASConnection dbc, Hashtable h_fromDbr)
//			throws Exception, CariException {
//		try {
//			String cart = ((Object) h_fromDbr.get("int_cartella")).toString();
//			String datarif = ((Object) h_fromDbr.get("int_data")).toString();
//			String oper = ((Object) h_fromDbr.get("int_codoper")).toString();
//
//			Hashtable h_par = new Hashtable();
//			String pr_data = null;
//
//			// gestione progetto: ricerca ed eventualmente crea progetto
//			// contenente datarif
//			ISASRecord progetto_rif = getProgetto(dbc, cart, datarif);
//
//			if (progetto_rif != null)
//				pr_data = progetto_rif.get("pr_data").toString();
//
//			if (pr_data != null)
//				h_par.put("pr_data", pr_data);
//			else {
//				// 13/05/10
//				// throw new Exception("RegAccessiEJB.gestisciCaso() - non e' stato trovato un progetto");
//				LOG.warn("gestCasoAndErogaz() - non e' stato trovato un progetto attivo"
//								+ "\n\talla data " + datarif
//								+ ", quindi NON provo a comunicare EROGAZIONE");
//				return 0;
//			}
//
//			h_par.put("n_cartella", cart);
//			h_par.put("dt_segnalazione", datarif);
//			h_par.put("operZonaConf", oper);
//
//			// gestione caso [e segnalazione]
//			h_par.put("dtRif", datarif);
//// 26/10/12: per erogazione anche nei casi chiusi	ISASRecord caso = (ISASRecord)gestCaso.getCasoAttivoAllaData(dbc, h_par);
//			ISASRecord caso = (ISASRecord) gestCaso.getCasoAllaData(dbc, h_par);
//			Integer idCaso = null;
//
//			boolean isToscana = ((Boolean) gestCaso.isUbicazRegTosc(dbc, h_par))
//					.booleanValue();
//			// 21/10/10
//			boolean isPiemonte = ((Boolean) gestCaso
//					.isUbicazRegPiem(dbc, h_par)).booleanValue();
//
//			if (caso == null) { // NON esiste il caso
//				h_par.put("dt_presa_carico", datarif);
//
//				// 12/11/12 -----
//				String opAuto = getOperSkmmg(dbc, cart, datarif);
//				// 27/02/13: ricerca oper della zona di residenza dell'assistito ----
//				if (opAuto == null) {
//					System.out.println("*** RegAccessiEJB.gestCasoAndErogaz(): NON ESISTE oper su SKMMG per la cart=["+cart+"]");
//					opAuto = getOperZonaResAss(dbc, cart, datarif);
//					
//					if (opAuto == null) {
//						System.out.println("*** RegAccessiEJB.gestCasoAndErogaz(): NON ESISTE oper della zonaRes per la cart=["+cart+"]");
//						opAuto = "SASS1";
//					}
//				}
//				// 27/02/13 ----------------------					
//				h_par.put("oper_auto", opAuto);
//				// 12/11/12 -----
//				
//				gestSegn.settaDatiDefault(dbc, h_par);
//				gestSegn.settaDatiDefaultSan(dbc, h_par);
//
//				// se devo gestire l'evento segnalazione (solo in Toscana per
//				// ora) -
//				// ci pensa la segnalazione a creare il caso
//				if (isToscana) {
//					ISASRecord segn = (ISASRecord) gestSegn.insert(dbc, h_par);
//					if (segn != null) {
//						idCaso = new Integer(segn.get("id_caso").toString());
//
//						// gestione presa carico: fuori Toscana, non si fa la
//						// presa carico per casi SAN, ma solo UVM
//						h_par.put("id_caso", idCaso);
//						h_par.put("ubicazione", "" + GestCasi.UBI_RTOSC);
//
//						// valori default x presacarico ---
//						Hashtable h_Conf = eveUtl.leggiConf(dbc, oper);
//						String cod_reg = (String) h_Conf.get("ADRSA_REG_ERO");
//						String cod_usl = (String) h_Conf.get("ADRSA_ASL_ERO");
//						h_par.put("reg_ero", cod_reg);
//						h_par.put("asl_ero", cod_usl.substring(3));
//						h_par.put("tipo_percorso", "2"); // AD solo sanitaria
//						h_par.put("percorso_progettato", "1"); // AD diretta
//						// 12/11/12
//						h_par.put("zon_ero", segn.get("zona_segnalazione"));
//						// valori default x presacarico ---
//
//						ISASRecord presaCar = gestPresaCar.insert(dbc, h_par);
//						if (presaCar == null)
//							return -1;
//					} else
//						return -1;
//				} else if (isPiemonte) { // 21/10/10
//					h_par.put("dt_presa_carico", h_par.get("dt_segnalazione"));
//					idCaso = (Integer) gestCaso.apriCasoSan(dbc, h_par);
//				} else
//					return -1;
//			} else
//				idCaso = (Integer) caso.get("id_caso");
//
//			// gestione erogazione
//			h_par.put("int_cartella", cart);
//			h_par.put("int_data_prest", datarif);
//			// lettura da chiavi_libere del contatore accessi che serve come
//			// codice per l'evento EROGAZIONE
//			String anno = datarif.substring(0, 4);
//			Integer progr = (Integer) eveUtl.getProgrxAccessi(dbc, anno);
//			if (progr == null)
//				return -1;
//			h_par.put("int_anno", anno);
//			h_par.put("int_contatore", progr);
//			h_par.put("int_tipo_oper", "99"); // MMG
//			h_par.put("int_cod_oper", oper);
//
//			int risu = gestErog.insert(dbc, h_par);
//
//			if (risu <= 0)
//		                LOG.warn("gestCasoAndErogaz - NON segnalata EROGAZIONE!");
//			return risu;
//		} catch (Exception ex) {
//			throw newEjbException("errore in gestCasoAndErogaz(): " + ex.getMessage(), ex);
//		}
//	}

//	private ISASRecord getProgetto(ISASConnection dbc, String cartella,
//			String dataRif) throws Exception {
//		ISASRecord rec = null;
//
//		try {
//			String sel = " SELECT * FROM progetto WHERE n_cartella = "
//					+ cartella + " AND pr_data <= " + dbc.formatDbDate(dataRif)
//					+ " AND (pr_data_chiusura IS NULL "
//					+ " OR pr_data_chiusura >= " + dbc.formatDbDate(dataRif)
//					+ ")";
//
//			rec = dbc.readRecord(sel);
//			/***
//			 * 13/05/10: per problemi sulle date apertura, deciso di non
//			 * generare pi� la comunicazione dell'evento EROGAZIONE se non
//			 * esiste gi� il CASO (e quindi il PROGETTO) if (rec == null) { rec =
//			 * dbc.newRecord("progetto"); rec.put("n_cartella", new
//			 * Integer(cartella)); rec.put("pr_data", dataRif);
//			 * dbc.writeRecord(rec); rec = dbc.readRecord(sel); }
//			 ***/
//
//			return rec;
//		} catch (Exception ex) {
//			throw newEjbException("errore in getProgetto(): " + ex.getMessage(), ex);
//		}
//	}
//	
//	
//	// 12/11/12: ricerca dell'operatore che ha registrato l'ultima schedaMMG prima della dtRif
//	private String getOperSkmmg(ISASConnection dbc, String cart,
//									String dataRif) throws Exception 
//	{
//		String oper = null;
//		try {
//			String critDt = " AND b.pr_data <= " + dbc.formatDbDate(dataRif);
//		
//			String sel = "SELECT a.* FROM skmmg a" 
//				+ " WHERE a.n_cartella = " + cart
//				+ " AND a.pr_data IN (SELECT MAX(b.pr_data) FROM skmmg b"
//					+ " WHERE b.n_cartella = a.n_cartella";
//				
//			ISASRecord dbr = dbc.readRecord(sel + critDt + ")");
//			if (dbr == null);
//				dbr = dbc.readRecord(sel + ")");
//				
//			if ((dbr != null) && (dbr.get("skmmg_operatore") != null))
//				oper = dbr.get("skmmg_operatore").toString();
//			return oper;
//		} catch (Exception ex) {
//			throw newEjbException("errore in getOperSkmmg(): " + ex.getMessage(), ex);
//		}
//	}
//	
//	// 27/02/13: ricerca un qualsiasi oper della zona di residenza dell'assistito 
//	private String getOperZonaResAss(ISASConnection dbc, String cart,
//									String dataRif) throws Exception 
//	{
//		String oper = null;
//		try {
//			// lettura comune residenza su ANAGRA_C
//			String critDt = " AND b.data_variazione <= " + dbc.formatDbDate(dataRif);
//			
//			String sel = "SELECT a.* FROM anagra_c a" 
//				+ " WHERE a.n_cartella = " + cart
//				+ " AND a.data_variazione IN (SELECT MAX(b.data_variazione) FROM anagra_c b"
//					+ " WHERE b.n_cartella = a.n_cartella";
//				
//			ISASRecord dbrA = dbc.readRecord(sel + critDt + ")");
//			if (dbrA == null);
//				dbrA = dbc.readRecord(sel + ")");
//			
//			// lettura codice zona su COMUNI
//			if ((dbrA != null) && (dbrA.get("citta") != null)) {
//				sel = "SELECT * FROM comuni"
//					+ " WHERE codice = '" + (String)dbrA.get("citta") + "'";
//					
//				ISASRecord dbrC = dbc.readRecord(sel);
//				
//				// lettura codice su OPERATORI di un operatore
//				if ((dbrC != null) && (dbrC.get("cod_ist") != null)
//				&& (!dbrC.get("cod_ist").toString().trim().equals(""))) {
//					sel = "SELECT x.* FROM operatori x"
//						+ " WHERE x.cod_zona = '" + (String)dbrC.get("cod_ist") + "'"
//						+ " AND x.codice IN (SELECT MIN(y.codice) FROM operatori y"
//							+ " WHERE y.cod_zona = x.cod_zona)";
//						
//					ISASRecord dbrO = dbc.readRecord(sel);
//					
//					if ((dbrO != null) && (dbrO.get("codice") != null))
//						oper = dbrO.get("codice").toString();
//				}
//			}
//	
//			return oper;
//		} catch (Exception ex) {
//			throw newEjbException("errore in getOperZonaResAss(): " + ex.getMessage(), ex);
//		}
//	}		
	// 15/02/10 m.: gestione CASO ---------------------------------

	


	//public Boolean controlloEsistenzaSospensione(myLogin mylogin, Hashtable h) throws SQLException, CariException{
	/**
	 * mod elisa b 01/12/11
	 * La funzione restituisce un Hashtable con chiave un codice che indica l'esito
	 * dell'operazione e di valore un msg (o null)
	 * 0 : controllo superato
	 * 1 : controllo superato con warning
	 * 2 : controllo fallito
	 * 
	 * mod 02/01/12 aggiunta una chiave in conf che permette di rendere il controllo
	 * sempre non bloccante per alcune tipologie di operatori 
	 */
	public Hashtable controlloEsistenzaSospensione(myLogin mylogin, Hashtable h) throws SQLException, CariException{
		boolean done = false;
		ISASConnection dbc = null;
		boolean esito = true;
		String msg = null;
		ISASRecord dbrSosp = null;
		Hashtable res = new Hashtable();
		
		String codEsitoBloccante = "2";
		String codEsitoNonBloccante = "1";
		String msgBloccante = "\nNon e' possibile registrare accessi in tale periodo";
		
		try {
			dbc = super.logIn(mylogin);
			
			//elisa b 02/12/11	
			String tipoOpe = h.get("tipoOpe").toString();		
			boolean isCtrNonBloccante = isControlloNonBloccante(dbc, tipoOpe);
			
			String mese = h.get("mese").toString();
			String anno = h.get("anno").toString();
			String valori = h.get("valori").toString();
			String n_cartella = h.get("n_cartella").toString();	
			LOG.debug("controlloEsistenzaSospensione() parametri = " + h.toString());
			if (valori != null && !valori.trim().equals("")) {
				// Vado a verificare che le singole date non appartengano a un periodo di sospensione
				StringTokenizer val = new StringTokenizer(valori, "|");
				String data = null; //elisa b 01/12/11
				while ((val.hasMoreElements()) && (esito)) {
					StringTokenizer datiReg = new StringTokenizer(val.nextToken(), "-");
					String giorno = datiReg.nextToken();							
					if(giorno.length() == 1)
						giorno = "0" + giorno;
					/*String*/ data = anno + "-" + mese + "-" + giorno;
				
					// 27/05/11 ---
					dbrSosp = (ISASRecord)getSospensione(dbc, n_cartella, data);
										
					esito = (dbrSosp == null);
				}

				if (!esito) {
					String dtS_I = dbrSosp.get("prs_data_sospeso").toString();
					String dtS_F = null;
					if (dbrSosp.get("prs_data_riattiva") != null)
						dtS_F = dbrSosp.get("prs_data_riattiva").toString();
					
					/* 01/12/11 elisa b : si accettano accessi sugli estremi
					 * dell'intervallo (in questo caso si restituisce il codice
					 * 1 che il client interpreta come condizione non bloccante) */
					if(data.equals(dtS_I) || data.equals(dtS_F)) {	
						msg = "La data prestazione coincide con un estremo del " +
								"periodo di sospensione che va dal " + NumberDateFormat.getDateFromJDBC(dtS_I, NumberDateFormat.DATE_ITA_LONG) 
						+ (dtS_F!=null?(" al " + NumberDateFormat.getDateFromJDBC(dtS_F, NumberDateFormat.DATE_ITA_LONG)):", ancora attiva.");
						res.put(codEsitoNonBloccante, msg);
					} else {	
						//KO
						msg = "Esiste una sospensione dal " + NumberDateFormat.getDateFromJDBC(dtS_I, NumberDateFormat.DATE_ITA_LONG) 
							+ (dtS_F!=null?(" al " + NumberDateFormat.getDateFromJDBC(dtS_F, NumberDateFormat.DATE_ITA_LONG)):", ancora attiva.");
						// mod elisa b 02/01/12 res.put("2", msg);
						if(isCtrNonBloccante)
							res.put(codEsitoNonBloccante, msg);
						else
							res.put(codEsitoBloccante, msg + msgBloccante);						
					}
					
				}
				// 27/05/11 ---				
			}
			
			/*
			if(!esito)
				throw new CariException(msg);		
			return new Boolean(esito);*/
			
			if(esito)
				res.put("0", "");
			
			return res;
			
		} catch (Exception e) {
			throw newEjbException("errore in controlloEsistenzaSospensione(): " + e.getMessage(), e);
		} finally {
		        logout_nothrow("controlloEsistenzaSospensione", dbc);
		}
		
		
	}
	
	// 27/05/11: cerco record su PROGETTO_SOSP che contenga la dataPrestazione
	private ISASRecord getSospensione(ISASConnection dbc, String cart, String dtP) throws Exception
	{
		ISASCursor dbcurS = null;
		ISASRecord dbrS = null;
		try {
			String selS = "SELECT * FROM progetto_sosp"
							+ " WHERE n_cartella = " + cart
							+ " AND prs_data_sospeso <= " + formatDate(dbc, dtP)
							+ " AND ((prs_data_riattiva IS NULL)"
									+ " OR (prs_data_riattiva >= " + formatDate(dbc, dtP) + "))"
							+ " ORDER BY prs_data_sospeso";
			
			dbcurS = dbc.startCursor(selS);
			if ((dbcurS != null) && (dbcurS.getDimension() > 0)) {
				while (dbcurS.next())
					dbrS = (ISASRecord)dbcurS.getRecord();
			}
			return (ISASRecord)dbrS;
		} catch (Exception e) {
			throw newEjbException("errore in getSospensione(): " + e.getMessage(), e);
		} finally {
		        close_dbcur_nothrow("getSospensione", dbcurS);
		}
	}
	
	
	
/*** 27/05/11
	public Boolean controlloEsistenzaSospensione(myLogin mylogin, Hashtable h) throws SQLException, CariException{
		boolean done = false;
		ISASConnection dbc = null;
		boolean esito = true;
		String msg = null;
		
		try {
			dbc = super.logIn(mylogin);
			String mese = h.get("mese").toString();
			String anno = h.get("anno").toString();
			String valori = h.get("valori").toString();
			String n_cartella = h.get("n_cartella").toString();				
			if (valori != null && !valori.trim().equals("")) {
				//cerco eventuali periodi di sospensione 
				Hashtable hSosp = getPeriodiSospensione(dbc, n_cartella, mese, anno);
				//se non ci sono sospensioni l'esito e' positivo
				if(hSosp.size() > 0){		
					System.out.println("controlloEsistenzaSospensione hSosp ="+ hSosp.toString());
					if(hSosp.containsKey("sospAttiva")){
						//in caso di sospensione attiva (senza data di riattivazione)
						//l'esito e' negativo
						esito = false;
						msg = "Esite una sospensione in corso nel mese indicato.Impossibile registrare gli accessi";
					}else{
						// Vado a verificare che le singole date non appartengano
						//a un periodo di sospensione
						StringTokenizer val = new StringTokenizer(valori, "|");
						while (val.hasMoreElements()) {
							StringTokenizer datiReg = new StringTokenizer(val.nextToken(), "-");
							String giorno = datiReg.nextToken();							
							if(giorno.length() == 1)
								giorno = "0" + giorno;
							String data = anno + "-" + mese + "-" + giorno;
							Date dataP = Date.valueOf(data);
							
							Enumeration en = hSosp.elements();
							while(en.hasMoreElements()) {
								Hashtable hS = (Hashtable) en.nextElement();
								//Date inizio = (Date) hS.get("dt_sospeso");
								Date inizio = Date.valueOf(hS.get("dt_sospeso").toString());							
								Date fine = Date.valueOf(hS.get("dt_riattiva").toString());	
								if((dataP.compareTo(inizio) >= 0) && (dataP.compareTo(fine) <= 0)){
									//la data scelta appartiene a un periodo di sosp
									esito = false;
									DataWI dtI = new DataWI(inizio);
									DataWI dtF = new DataWI(fine);
									System.out.println("controlloEsistenzaSospensione KO " + dataP);
									msg = "Esiste una sospensione nel periodo " + dtI.getFormattedString(0)
										  + " - " + dtF.getFormattedString(0)+
										  " Non e' possibile registrare accessi in tale periodo";
									break;
								}
							}
							if(esito == false)
								break;
						}
					}
				}
			}
			
			done = true;
			dbc.close();
			super.close(dbc);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo controlloEsistenzaSospensione " + e);
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
		
		if(!esito)
			throw new CariException(msg);
		
		return new Boolean(esito);	
	}

	// Costruisce un Hashtable di hashtable, ognuna delle quali contiene un periodo di sospensione
	private Hashtable getPeriodiSospensione(ISASConnection dbc, String n_cartella, String mese,
			String anno) throws SQLException{
		Hashtable hS = new Hashtable();
		ISASCursor dbcur = null;
		
		String select = "SELECT prs_data_sospeso, prs_data_riattiva" +
						" FROM progetto_sosp" +
						" WHERE n_cartella = " + n_cartella +
						" AND( " +
							"(TO_CHAR(prs_data_sospeso, 'YYYY') = '" + anno +"'" +
							" AND TO_CHAR(prs_data_sospeso, 'MM') = '" + mese +"')" +
							" OR (prs_data_riattiva IS NULL)" +
						")" +
						" ORDER BY prs_data_sospeso ASC"	;
		
		try {
			dbcur = dbc.startCursor(select);
			System.out.println("getPeriodiSospensione " + select);
			int i = 0;
			String chiave = null;
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				Hashtable h = new Hashtable();
				h.put("dt_sospeso", dbr.get("prs_data_sospeso").toString());
				if(dbr.get("prs_data_riattiva") != null){
					h.put("dt_riattiva", dbr.get("prs_data_riattiva").toString());
					chiave = "" + i;
				}else
					chiave = "sospAttiva";
				
				hS.put(chiave,h);
				i++;
			}
			dbcur.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException();
		}
					
		return hS;
	}
***/	
	
	
	/**
	 * elisa b 09/06/11
	 * mod elisa b 02/01/12
	 * La funzione restituisce un Hashtable con chiave un codice che indica l'esito
	 * dell'operazione e di valore un msg (o null)
	 * 0 : controllo superato
	 * 1 : controllo superato con warning
	 * 2 : controllo fallito
	 */
	//public Boolean controlloMultiploEsistenzaSospensione(myLogin mylogin, Hashtable h) throws SQLException, CariException{
	public Hashtable controlloMultiploEsistenzaSospensione(myLogin mylogin, Hashtable par) throws SQLException, CariException{
		ISASConnection dbc = null;
		boolean esito = true;
		String msg = null;
		ISASRecord dbrSosp = null;
		String n_cartella = "";
		Hashtable res = new Hashtable();
		
		String codEsitoBloccante = "2";
		String codEsitoNonBloccante = "1";
		String msgBloccante = "\nNon e' possibile registrare accessi in tale periodo";
		
	        if (LOG.isDebugEnabled())
	                LOG.debug("controlloMultiploEsistenzaSospensione() parametri = " + par);
		
		try {
			dbc = super.logIn(mylogin);
			
			String tipoOpe = par.get("tipo_operatore").toString();		
			boolean isCtrNonBloccante = isControlloNonBloccante(dbc, tipoOpe);
			
			Hashtable h = (Hashtable) par.get("appuntamenti");//02/01/12
			Enumeration e = h.keys();
			while(e.hasMoreElements()){
				String data = (String)e.nextElement(); 				
				Hashtable hD = (Hashtable)h.get(data);
				Enumeration e1 = hD.keys();
				while((e1.hasMoreElements()) && (esito)) {
					
					String chiave = (String)e1.nextElement();							
					String[] elem = chiave.split("-");
					if(elem.length > 0){		
						n_cartella = elem[0];
						dbrSosp = (ISASRecord)getSospensione(dbc, n_cartella, data);
						esito = (dbrSosp == null);
						//System.out.println("controlloMultiploEsistenzaSospensione n_cartella " + n_cartella + " data " + data);
					}
				}

				if (!esito) {
					String dataFormattata = data.substring(6, 10) + "-" + data.substring(3, 5) + "-"
					+ data.substring(0, 2);
					String dtS_I = dbrSosp.get("prs_data_sospeso").toString();
					String dtS_F = null;
					if (dbrSosp.get("prs_data_riattiva") != null)
						dtS_F = dbrSosp.get("prs_data_riattiva").toString();
						
					LOG.debug("controlloMultiploEsistenzaSospensione1 " + dataFormattata);
					LOG.debug("controlloMultiploEsistenzaSospensione2 " + dtS_I);
					/* 02/01/12 elisa b : si accettano accessi sugli estremi
					 * dell'intervallo (in questo caso si restituisce il codice
					 * 1 che il client interpreta come condizione non bloccante) */
					if(dataFormattata.equals(dtS_I) || dataFormattata.equals(dtS_F)) {	
						msg = "La data prestazione coincide con un estremo del " +
								"periodo di sospensione che va dal " + NumberDateFormat.getDateFromJDBC(dtS_I, NumberDateFormat.DATE_ITA_LONG) 
						+ (dtS_F!=null?(" al " + NumberDateFormat.getDateFromJDBC(dtS_F, NumberDateFormat.DATE_ITA_LONG)):", ancora attiva.");
						res.put(codEsitoNonBloccante, msg);
					} else {	
						//KO
						msg = "Esiste una sospensione dal " + NumberDateFormat.getDateFromJDBC(dtS_I, NumberDateFormat.DATE_ITA_LONG) 
						+ (dtS_F!=null?(" al "+NumberDateFormat.getDateFromJDBC(dtS_F, NumberDateFormat.DATE_ITA_LONG)):", ancora attiva ")
						+ "per l'assistito " + n_cartella + " - " + ISASUtil.getDecode(dbc, "cartella",
								"n_cartella", n_cartella, "cognome");
						if(isCtrNonBloccante)
							res.put(codEsitoNonBloccante, msg);
						else
							res.put(codEsitoBloccante, msg + msgBloccante);		
					}
				}					
			}
			
			/*
			 if(!esito)
				throw new CariException(msg);
		
			return new Boolean(esito);*/			 
			
			if(esito)
				res.put("0", "");
			
			return res;			
			
		} catch (Exception e) {
			throw newEjbException("Errore in controlloMultiploEsistenzaSospensione(): " + e.getMessage(), e);
		} finally {
		        logout_nothrow("controlloMultiploEsistenzaSospensione", dbc);
		}				
	}
	
	/**
	 * elisa b 02/01/11
	 * Metodo che legge da conf gli eventuali tipologie di operatori per le quali
	 * il controllo sui periodi di sospensione in fase di inserimento di un accesso
	 * non e' mai bloccante e verifica se la tipologia passata come
	 * parametro rientra tra quelle.
	 * @param dbc
	 * @throws Exception
	 */
	private boolean isControlloNonBloccante(ISASConnection dbc, String tipoOpe) throws Exception{
		
		String myselect = "SELECT * FROM conf" +
		" WHERE conf_kproc = 'SINS'" +
		" AND conf_key = 'CTR_ACCESSI_SOSP'";
		
		ISASRecord dbr = dbc.readRecord(myselect);
		if (dbr != null) {
			String[] ope = ((String)dbr.get("conf_txt")).split("\\|");
			for (int i=0; i<ope.length; i++) {
				if(tipoOpe.equals(ope[i]))
					return true;
			}
		}
		
		return false;
	}
}

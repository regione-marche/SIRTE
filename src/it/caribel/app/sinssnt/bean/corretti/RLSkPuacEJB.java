package it.caribel.app.sinssnt.bean.corretti;


// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 25/10/06 - EJB di connessione alla procedura SINS Skvalutaz
//     scrittura nella tabella rl_puauvm
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;


import it.pisa.caribel.util.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*; //gb 02/11/06
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.casi_adrsa.GestRivalutazione;
import it.pisa.caribel.sinssnt.casi_adrsa.GestSegnalazione;
import it.caribel.app.sinssnt.bean.nuovi.ScaleVal;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.pisa.caribel.sinssnt.connection.*;


public class RLSkPuacEJB extends SINSSNTConnectionEJB
{
	public RLSkPuacEJB() {	}

	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	// 08/01/08
	private String msgNoDir = "Mancano i diritti per leggere il record";

	// 05/09 Elisa Croci
	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();
	private GestRivalutazione gestore_rivalutazioni = new GestRivalutazione();
	
	// 21/05/09 m.
	private boolean myDebug = true;
	private String nomeEJB = "RLSkPuacEJB";
	private ScaleVal gest_scaleVal = new ScaleVal();

	// 04/06/09 Elisa Croci
	private Hashtable hash_tp_oper = faiHashTpOper();
	private String codice_medico = "99";
	private String ver = "1-";

	// 13/10/09
	private final Hashtable HASH_MAP_MOTCHIUS = faiHashMapMotChius();

	private void putInfoFromCartellaIntoDbr(ISASConnection dbc, ISASRecord dbr, String strNAssistito) throws Exception
	{
		java.sql.Date dateDataApertura = null;
		java.sql.Date dateDataChiusura = null;

		String mySel = "SELECT data_apertura, data_chiusura" +
						" FROM cartella" +
						" WHERE n_cartella = " + strNAssistito;

		ISASRecord rec = dbc.readRecord(mySel);

		if (rec != null)
		{
			if (rec.get("data_apertura") != null)
			{
				dateDataApertura = (java.sql.Date) rec.get("data_apertura");
				dbr.put("data_apertura_cartella", dateDataApertura);
			}
			if (rec.get("data_chiusura") != null)
			{
				dateDataChiusura = (java.sql.Date) rec.get("data_chiusura");
				dbr.put("data_chiusura_cartella", dateDataChiusura);
			}
		}
	}

	// 08/01/08
	private void decodAllOper(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		decodOper(mydbc, mydbr, "pr_oper_ultmod", "ultmod");
		
		leggiMed(mydbc, mydbr);
		decodMed(mydbc, mydbr, "cod_med", "sk1");
	}
		
	// 08/01/08
	private void decodOper(ISASConnection mydbc, ISASRecord mydbr,
			String nomeCampo, String nomeDesc) throws Exception
	{
		decodOperAndMed(mydbc, mydbr, "operatori", "codice",
				"cognome", "nome",
				nomeCampo, nomeDesc);
	}

	// 08/01/08
	private void decodMed(ISASConnection mydbc, ISASRecord mydbr,
			String nomeCampo, String nomeDesc) throws Exception
	{
		decodOperAndMed(mydbc, mydbr, "medici", "mecodi",
				"mecogn", "menome",
				nomeCampo, nomeDesc);
	}

	// 08/01/08
	private void decodOperAndMed(ISASConnection mydbc, ISASRecord mydbr,
			String nomeTab, String nomeCampoCod,
			String nomeCampoCogn, String nomeCampoNome,
			String nomeCampo, String nomeDesc) throws Exception
	{
		String cod = (String)mydbr.get(nomeCampo);
		String nomeD = "decod_" + nomeDesc;
		String cognOpe = "";
		String nomeOpe = "";

		if ((cod != null) && (!cod.trim().equals(""))) {
			String sel = "SELECT NVL(" + nomeCampoCogn + ", '') cogn_ope," +
			" NVL(" + nomeCampoNome + ", '') nome_ope" +
			" FROM " + nomeTab +
			" WHERE " + nomeCampoCod + " = '" + cod + "'";

			ISASRecord dbr_1 = mydbc.readRecord(sel);
			if (dbr_1 != null){
				cognOpe = (String)dbr_1.get("cogn_ope");
				nomeOpe = (String)dbr_1.get("nome_ope");
			}
		}
		mydbr.put((nomeD + "_cogn_ope"), cognOpe);
		mydbr.put((nomeD + "_nome_ope"), nomeOpe);
		// 03/03/08: x la griglia -> unica descrizione
		mydbr.put((nomeDesc + "_desc"), cognOpe + " " + nomeOpe);
	}

	// 08/01/08
	private void leggiMed(ISASConnection mydbc, ISASRecord mydbr)throws Exception
	{
		String sel = "SELECT a.* FROM anagra_c a" +
		" WHERE a.n_cartella = " + mydbr.get("n_cartella") +
		" AND a.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
		" FROM anagra_c WHERE anagra_c.n_cartella = a.n_cartella)";

		ISASRecord dbr_1 = mydbc.readRecord(sel);
		if ((dbr_1 != null) && (((String)dbr_1.get("cod_med")) != null))
			mydbr.put("cod_med", (String)dbr_1.get("cod_med"));
	}

	// 04/03/09
	private void decodTabPap(ISASConnection mydbc, ISASRecord mydbr)throws Exception
	{
		String desc = util.getDecode(mydbc, "tab_pap", "codice", mydbr.get("pr_pianoint"), "descrizione");
		mydbr.put("desc_pianoint", desc);
	}


	public ISASRecord selectSkValCorrente(myLogin mylogin, Hashtable h) throws SQLException, CariException
	{
		mySystemOut("selectSkValCorrente == H == " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strDtProg = (String) h.get("pr_data");

		try
		{
			dbc = super.logIn(mylogin);

			/*** 02/03/09
			// si legge il record di progr max
		    String myselect = "SELECT p.* FROM rl_puauvm p" +
	                      " WHERE p.n_cartella = " + strNAssistito +
							" AND p.pr_data = " + formatDate(dbc, strDtProg) +
							" AND p.pr_progr IN (SELECT MAX(pr_progr) FROM rl_puauvm" +
									" WHERE rl_puauvm.n_cartella = p.n_cartella" +
									" AND rl_puauvm.pr_data = p.pr_data)";
			 ***/
			// 02/03/09: si legge il record attivo (non chiuso)
			String myselect =  "SELECT p.*" +
			" FROM rl_puauvm p" +
			" WHERE p.n_cartella = " + strNAssistito +
			" AND p.pr_data = " + formatDate(dbc, strDtProg) +
			" AND p.pr_data_verbale_uvm IS NULL" +
			" AND p.pr_data_chiusura IS NULL";

			mySystemOut("RLSkPuacEJB/selectSkValCorrente=[" + myselect + "]");
			// Leggo il record
			ISASRecord dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				// 08/01/08
				decodAllOper(dbc, dbr);

				// Si prendono data_apertura e data_chiusura dalla tabella 'cartella'
				putInfoFromCartellaIntoDbr(dbc, dbr, strNAssistito);

				// 04/03/09
				decodTabPap(dbc, dbr);

				// 27/10/08 ---
				ISASRecord dbrAssAna = getRecAssAna(dbc, (Hashtable)dbr.getHashtable());
				if ((dbrAssAna != null) && (dbrAssAna.get("progressivo") != null))
					dbr.put("ass_angr_progressivo", dbrAssAna.get("progressivo"));
				// 27/10/08 ---

				// 10/12/10
				Integer tempoT = (Integer)dbr.get("tempo_t");
				
				// 20/05/09 Elisa Croci
				int caso = prendi_dati_caso(dbc,dbr);
				
				String ubi = ISASUtil.getValoreStringa(h,"ubicazione");
                int ubic;
                if (ISASUtil.valida(ubi))  {
					ubic = Integer.parseInt(ubi);
					if(ubic == GestCasi.UBI_RTOSC)
						prendi_segnalazione(dbc,caso,dbr);

					if(ubic == GestCasi.UBI_RPIEM)
						prendi_segnalazione(dbc,caso,dbr);

					dbr.put("ubicazione",ubi);
                }
				
				// 03/06/09 Elisa Croci, lettura componenti commissione PUACUVM
				leggiCompCommPUACUVM(dbc, dbr, strNAssistito, strDtProg,
						 			dbr.get("pr_progr").toString(), codice_medico);
									
				// 07/11/10
				dbr.put("dt_rev_xlett", dbr.get("pr_data_revisione"));									
			}

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		// 08/01/08 ---
		catch(ISASPermissionDeniedException e){
			System.out.println("SkValutazEJB.selectSkValCorrente(): "+e);
			throw new CariException(msgNoDir, -2);
		}
		catch(Exception e) {
			System.out.println("RLSkPuacEJB/selectSkValCorrente(): "+e);
			throw new SQLException("Errore eseguendo la selectSkValCorrente() in RLSkPuacEJB");
		}
		finally {
			if(!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch(Exception e1)  {
					System.out.println(e1);
				}
			}
		}
	}


	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws  SQLException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		String strNAssistito = (String)h.get("n_cartella");
		String strDtProg = (String) h.get("pr_data");
		String strProgr = (String) h.get("pr_progr");

		try{
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM rl_puauvm" +
			" WHERE n_cartella = " + strNAssistito +
			" AND pr_data = " + formatDate(dbc, strDtProg) +
			" AND pr_progr = " + strProgr;

			mySystemOut("queryKey=[" + myselect + "]");
			ISASRecord dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				// 08/01/08
				decodAllOper(dbc, dbr);

				// 04/03/09
				decodTabPap(dbc, dbr);

				// Si prendono data_apertura e data_chiusura dalla tabella 'cartella'
				putInfoFromCartellaIntoDbr(dbc, dbr, strNAssistito);

				// 27/10/08 ---
				ISASRecord dbrAssAna = getRecAssAna(dbc, (Hashtable)dbr.getHashtable());
				if ((dbrAssAna != null) && (dbrAssAna.get("progressivo") != null))
					dbr.put("ass_angr_progressivo", dbrAssAna.get("progressivo"));
				// 27/10/08 ---

				// 10/12/10
				Integer tempoT = (Integer)dbr.get("tempo_t");				
				
				// 14/09/10 bysp
				int caso = prendi_dati_caso(dbc,dbr);
				
                String ubi = ISASUtil.getValoreStringa(h,"ubicazione");
                int ubic;
                if (ISASUtil.valida(ubi)) {
					ubic = Integer.parseInt(ubi);
					if(ubic == GestCasi.UBI_RTOSC)
						prendi_segnalazione(dbc,caso,dbr);

					if(ubic == GestCasi.UBI_RPIEM)
						prendi_segnalazione(dbc,caso,dbr);

					dbr.put("ubicazione",ubi);
                }
				
				// 03/06/09 Elisa Croci, lettura componenti commissione PUACUVM
				leggiCompCommPUACUVM(dbc, dbr, strNAssistito, strDtProg, strProgr, codice_medico);
				
				// 07/11/10
				dbr.put("dt_rev_xlett", dbr.get("pr_data_revisione"));
				// 10/12/10
				if (tempoT != null)
					dbr.put("tempo_t_xlett", tempoT.toString());					
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}
		// 08/01/08 ---
		catch(ISASPermissionDeniedException e){
			System.out.println("SkValutazEJB.queryKey(): "+e);
			throw new CariException(msgNoDir, -2);
		}
		catch(Exception e){
			System.out.println("RLSkPuacEJB: queryKey - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("RLSkPuacEJB: queryKey - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END queryKey



	public Vector query(myLogin mylogin, Hashtable h) throws  SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try{
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM rl_puauvm" +
			" WHERE n_cartella = " + (String)h.get("n_cartella") +
			" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data"));

			mySystemOut("query=[" + myselect+ "]");

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("RLSkPuacEJB: query - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("RLSkPuacEJB: query - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query


	// 22/02/08: aggiunto progr in key
	private int getProgressivo(ISASConnection mydbc, Hashtable h0) throws Exception
	{
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String myselect = "SELECT MAX(pr_progr) max_progr FROM rl_puauvm" +
		" WHERE n_cartella = " + (String)h0.get("n_cartella") +
		" AND pr_data = " + formatDate(mydbc, (String)h0.get("pr_data"));

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			intProgressivo = u.getIntField(dbr, "max_progr");

		intProgressivo++;
		return intProgressivo;
	}

	private boolean dtApeChiusPuac(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String strDataApeContatto = (String) h.get("pr_data_puac");
		if (strDataApeContatto == null) // 19/06/09: x chiamata da ElencoCasiPuac
			strDataApeContatto = (String)h.get("pr_data_richiesta");
		String pr_progetto = h.get("pr_data").toString();

		String mySel = "SELECT * FROM rl_puauvm WHERE n_cartella = " + strNCartella +
					" AND pr_data = " + formatDate(dbc, pr_progetto) +
					(h.get("pr_progr") != null ? " AND pr_progr <> " + h.get("pr_progr").toString() : "") +
					" AND ((pr_data_verbale_uvm >= " + formatDate(dbc, strDataApeContatto) +
					" AND pr_data_verbale_uvm IS NOT NULL) " +
						" OR (pr_data_chiusura >= " + formatDate(dbc, strDataApeContatto) +
							" AND pr_data_chiusura IS NOT NULL)) ";

		mySystemOut("dtApeChiusPuac -- " + mySel);
		ISASCursor dbcur;
		try
		{
			dbcur = dbc.startCursor(mySel);
			if ((dbcur != null) && (dbcur.getDimension() >0))
				return true;
			else
				return false;

		} catch (Exception e)
		{
			System.out.println("RLSkPuacEJB: dtApeChiusPuac - Eccezione= " + e);
			throw e;
		}
	}

	// 19/06/09: x chiamata da ElencoCasiPuac
	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException,
	ISASPermissionDeniedException,	SQLException, CariException
	{
		return insertConScale(mylogin, h, null);
	}

	// Inserimento su tabella rl_puauvm ed, eventualmente, PROGETTO
	public ISASRecord insertConScale(myLogin mylogin, Hashtable h, Vector vettSc) throws DBRecordChangedException,
	ISASPermissionDeniedException,	SQLException, CariException
	{
		mySystemOut("INSERT SCHEDA VALUTAZ PUAC: " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);

			dbc.startTransaction(); // 26/02/08

			String strNAssistito = (String)h.get("n_cartella");
			String strDtSkVal = (String)h.get("pr_data");// 26/02/08

			ISASRecord dbr = dbc.newRecord("rl_puauvm");

			// 22/02/08
			int intProgressivo = 1;
			if (strDtSkVal != null) // 26/02/08: la skVal esiste gia'
				intProgressivo = getProgressivo(dbc, h);
			else {// scrittura su PROGETTO
				if (h.get("pr_data_puac") != null)
					strDtSkVal = (String)h.get("pr_data_puac"); // con dtApertura = dtPresaCaricoPuac
				else if (h.get("pr_data_richiesta") != null)
					strDtSkVal = (String)h.get("pr_data_richiesta"); // con dtApertura = dtFirmaIstanza
				else {
					DataWI dtOggi = new DataWI();
					strDtSkVal = dtOggi.getFormattedString2(1); // con dtApertura = dtOdierna
				}

				// Controllo che data_ape sk_valutaz. fittizia sia >=
				// data chiusura di ultima sk_valutaz. chiusa pre-esistente.
				if (dtApeMinoreMaxDtChius(dbc, strNAssistito, strDtSkVal)) {
					String msg = "Attenzione: Data presa carico Puac antecedente a data chiusura di ultima Scheda valutazione chiusa!";
					throw new CariException(msg, -2);
				}

				// Controllo che data_ape sk_valutaz. fittizia sia >=
				//	data_apetura della tab. cartella.
				if (dtApeProgettoLTDtApeCartella(dbc, strNAssistito, strDtSkVal)) {
					String msg = "Attenzione: Data presa carico Puac e' antecedente alla data apertura dell'assistito!";
					throw new CariException(msg, -2);
				}

				scriviProgetto(dbc, strNAssistito, strDtSkVal);
				h.put("pr_data", strDtSkVal);
			}
			// 26/10/06 ----------------

			// 08/06/09 ----------------------------
			if(dtApeChiusPuac(dbc, h))
			{
				String msg = "Attenzione: Data apertura nuova scheda puac antecedente o uguale a data chiusura ultima scheda puac!";
				throw new CariException(msg, -2);
			}
			// --------------------------------------

			// 14/05/10
			String proven = (String)h.get("provenienza");

/*** 22/04/11: RME ha voluto campo testo libero			
			// 08/10/08: prelevo il max num protocollo e lo incremento sulla tabella CHIAVI_LIBERE
			// 24/10/08: se la dtSkPuac � valorizzata e non esiste gi� il num prot
			String numProto = faiNumProtocollo(dbc, (String)h.get("pr_data_puac"),
					(String)h.get("pr_protoc_domanda"), (String)h.get("pr_oper_ultmod"));
			if (numProto != null)
				dbr.put("pr_protoc_domanda", numProto);
			// -------------------------------------------------
***/

			// 07/10/08: se l'inserimento proviene da segnalazione su ASS_ANAGRAFICA
			// -> si deve aggiornare il rec con la chiave di RL_PUAUVM in modo da
			//	creare il legame tra la segnalazionePUA e la schedaPUAC
			// 24/10/08: se NON si proviene dall'elencoCasi (inserimento diretto su segnalazione MMG)
			// ->  si deve inserire un nuovo record su ASS_ANAGRAFICA
			ISASRecord dbrAssAna = getRecAssAna(dbc, h);
			int progrAssAna = 0;
			h.put("pr_progr", new Integer(intProgressivo));
			if (dbrAssAna != null)
				aggiornaAssAna(dbc, dbrAssAna, h);
			else
				progrAssAna = inserisciAssAnagrafica(dbc, strNAssistito, null, null, (String)h.get("pr_oper_ultmod"), h);

			Enumeration n = h.keys();
			while (n.hasMoreElements()){
				String e = (String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			// 22/02/08
			dbr.put("pr_progr", new Integer(intProgressivo));
			// 26/02/08
			dbr.put("pr_data", strDtSkVal);

			dbc.writeRecord(dbr);

			// 21/05/09 m.: scrittura su PUAUVM_SCHEDE
			if (vettSc != null)
				gest_scaleVal.scriviAllPuauvmSchede(dbc, strNAssistito, strDtSkVal, ""+intProgressivo, vettSc);

			String myselect = "SELECT * FROM rl_puauvm" +
			" WHERE n_cartella = " + strNAssistito +
			" AND pr_data = " + formatDate(dbc, strDtSkVal) +
			" AND pr_progr = " + intProgressivo;

			mySystemOut("RLSkPuacEJB/insert: " + myselect);
			dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				// 08/01/08
				decodAllOper(dbc, dbr);

				// 04/03/09
				decodTabPap(dbc, dbr);

				// 27/10/08 ---
				if (progrAssAna <= 0)
				{
					if ((dbrAssAna != null) && (dbrAssAna.get("progressivo") != null))
						progrAssAna = ((Integer)dbrAssAna.get("progressivo")).intValue();
				}

				if (progrAssAna > 0)
					dbr.put("ass_angr_progressivo", new Integer(progrAssAna));
				// 27/10/08 ---

				// 21/05/09 m. ------------------
				// lettura dtConclusione CASO precedente
				h.put("pr_data", strDtSkVal);
				String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
				String tempoT = (String)h.get("tempo_t");

				// letture scale max
			  	gest_scaleVal.getScaleMax(dbc, dbr, strNAssistito, strDtSkVal, ""+intProgressivo, dtChiusCasoPrec, "", tempoT);
				// 21/05/09 m. ------------------

			  	// 03/06/09 Elisa Croci, lettura componenti commissione PUACUVM
				leggiCompCommPUACUVM(dbc, dbr, h.get("n_cartella").toString(), h.get("pr_data").toString(),
									h.get("pr_progr").toString(), codice_medico);

				if (!"ELECASI".equals(proven)) { // 14/05/10: se non provengo da "ElencoCasi"
					// 27/05/09 E. Croci
					if(h.containsKey("pr_data_puac"))
					{
						mySystemOut("  SI pr_data_puac ");
						prendi_dati_caso(dbc, dbr);
						if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_UVM))
							gestione_segnalazione(dbc,dbr,h,"insert");
						else gestione_caso(dbc,dbr,h,"insert");

						dbr.put("ubicazione", ISASUtil.getValoreStringa(h,"ubicazione"));
						dbr.put("tempo_t", tempoT);
					}
					else mySystemOut("  NO pr_data_puac, salto tutto! ");

					// Chiusura amministrativa Scheda PUAC
					if(h.containsKey("pr_data_chiusura") && h.get("pr_data_chiusura") != null && !h.get("pr_data_chiusura").equals(""))
					{
						mySystemOut(" insert(): CHIUSURA AMMINISTRATIVA SCEHDA PUAC... " + h.get("pr_data_chiusura"));
						// devo chiudere solo il caso UVM
						Hashtable hDati = dbr.getHashtable();
						hDati.put("dt_conclusione", h.get("pr_data_chiusura"));
						// 04/01/10 ----
						String motChiu = (String)h.get("pr_motivo_chiusura");
						String motChiuFlux = getTabVociCodReg(dbc, "VALPCMCH", motChiu);
						hDati.put("motivo", motChiuFlux);
						// 04/01/10 ----
						hDati.put("operZonaConf", (String)dbr.get("pr_oper_ultmod")); // 15/10/09
						System.out.println("Dati per chiusura caso dovuta a chiusura scheda PUAC: " + hDati.toString());
						gestore_casi.chiudiCaso(dbc, hDati);
					}
				}
				
				// 07/11/10
				dbr.put("dt_rev_xlett", dbr.get("pr_data_revisione"));
				// 10/12/10
				dbr.put("tempo_t", tempoT);					
			}

			mySystemOut("RLSkPuacEJB: insert -- DBR restituito === " + dbr.getHashtable().toString());

			dbc.commitTransaction(); // 26/02/08

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}
		//gb 02/11/06
		catch(CariException ce)	{
			System.out.println("RLSkPuacEJB.insert(): Eccezione= " + ce);
			ce.setISASRecord(null);
			try{
				System.out.println("RLSkPuacEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}catch(DBRecordChangedException e){
			System.out.println("RLSkPuacEJB.insert(): Eccezione= " + e);
			try{
				System.out.println("RLSkPuacEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo la rollback() - " +  e1);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("RLSkPuacEJB.insert(): Eccezione= " + e);
			try{
				System.out.println("RLSkPuacEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo la rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e){
			System.out.println("RLSkPuacEJB.insert(): Eccezione= " + e);
			try{
				System.out.println("RLSkPuacEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo la rollback() - " +  e1);
			}
			throw new SQLException("Errore eseguendo una insert() - " +  e);
		}finally{
			if (!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("RLSkPuacEJB.insert(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END insert

	public ISASRecord update(myLogin mylogin, ISASRecord dbr, Vector vettSc, Vector vettCompComm)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean isSospUVM = false;

		try
		{
			dbc = super.logIn(mylogin);

			dbc.startTransaction();
			Hashtable h = (Hashtable)dbr.getHashtable();

			mySystemOut("update -- HASH: " + h.toString());

			if(dtApeChiusPuac(dbc, h))
			{
				String msg = "Attenzione: Data apertura nuova scheda puac antecedente o uguale a data chiusura ultima scheda puac!";
				throw new CariException(msg, -2);
			}

			String cart = (String)dbr.get("n_cartella");
			String oper = (String)dbr.get("pr_oper_ultmod");

			// 08/11/10 m: se c'� la sospensione del verbale si deve deconvocare
			if ((dbr.get("pr_sosp") != null) && (dbr.get("pr_sosp").toString().trim().equals("S"))) {
				isSospUVM = true;
				dbr.put("pr_stato_convoc", "0");
				dbr.put("pr_data_seduta", (java.sql.Date)null);
				dbr.put("pr_cod_comm", (String)null);
				dbr.put("pr_sede", (String)null);
				dbr.put("pr_centro_soc", (String)null);
				dbr.put("pr_ora", (Double)null);
				dbr.put("pr_ordine", (Integer)null);
			} else {	
/*** 22/04/11: RME ha voluto campo testo libero						
				// 08/10/08: prelevo il max num protocollo e lo incremento sulla tabella CHIAVI_LIBERE
				// 24/10/08: se la dtSkPuac � valorizzata e non esiste gi� il num prot
				String numProto = faiNumProtocollo(dbc, (String)dbr.get("pr_data_puac"),
						(String)dbr.get("pr_protoc_domanda"), oper);

				if (numProto != null)
					dbr.put("pr_protoc_domanda", numProto);
				// -----------------------------------------------
***/

				// 10/03/08: se aggiorno con dtVerbale e numVerbale non � gi� valorizzato -> lettura da CHIAVI_LIBERE
				int numVerbale = faiNumVerbale(dbc, (String)dbr.get("pr_data_verbale_uvm"),
						(String)dbr.get("pr_num_verbale"), oper);
				if (numVerbale > 0)
				{
					dbr.put("pr_num_verbale", new Integer(numVerbale));

					// 11/03/08: se aggiorno con dtRevisione e numVerbale non � gi� valorizzato -> inserimento su ASS_ANAGRAFICA
					// in modo da poter essere estratto nuovamente da "Elenco casi".
					String dtRevis = (String)dbr.get("pr_data_revisione");
					if ((dtRevis != null) && (!dtRevis.trim().equals("")))
						inserisciAssAnagrafica(dbc, cart, (String)dbr.get("pr_data_verbale_uvm"), dtRevis, oper, h);
				}
			}
			
			// 07/10/08: se l'inserimento proviene da segnalazione su ASS_ANAGRAFICA
			// -> si deve aggiornare il rec con la chiave di RL_PUAUVM in modo da
			//	creare il legame tra la segnalazionePUA e la schedaPUAC
			ISASRecord dbrAssAna = getRecAssAna(dbc, h);
			aggiornaAssAna(dbc, dbrAssAna, h);
			
			dbc.writeRecord(dbr);

			// 21/05/09 m.: scrittura su PUAUVM_SCHEDE
			if (vettSc != null)
				gest_scaleVal.scriviAllPuauvmSchede(dbc, (String)dbr.get("n_cartella"), (String)dbr.get("pr_data"),
									 	(String)dbr.get("pr_progr"), vettSc);

			if(vettCompComm != null)
			{
				mySystemOut("vettCompComm != null ");
				cancellaAllCompCommPUACUVM(dbc, dbr.get("n_cartella").toString(),
						                   dbr.get("pr_data").toString(),
						                   dbr.get("pr_progr").toString());
				scriviAllCompCommPUACUVM(dbc, vettCompComm, codice_medico);
			}

			String myselect = "SELECT * FROM rl_puauvm" +
							" WHERE n_cartella = " + (String)dbr.get("n_cartella") +
							" AND pr_data = " + formatDate(dbc, (String)dbr.get("pr_data")) +
							" AND pr_progr = " + (String)dbr.get("pr_progr");

			mySystemOut("update: " + myselect);

			dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				// 08/01/08
				decodAllOper(dbc, dbr);

				// 04/03/09
				decodTabPap(dbc, dbr);

				// 27/10/08 ---
				if ((dbrAssAna != null) && (dbrAssAna.get("progressivo") != null))
					dbr.put("ass_angr_progressivo", dbrAssAna.get("progressivo"));
				// 27/10/08 ---

				// 21/05/09 m. ------------------
				// lettura dtConclusione CASO precedente
				String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
				String tempoT = (String)h.get("tempo_t");

				// letture scale max
			  	gest_scaleVal.getScaleMax(dbc, dbr, (String)h.get("n_cartella"), (String)h.get("pr_data"),
										(String)h.get("pr_progr"), dtChiusCasoPrec, "", tempoT);
				// 21/05/09 m. ------------------

				// 03/06/09 Elisa Croci, lettura componenti commissione PUACUVM
				leggiCompCommPUACUVM(dbc, dbr, h.get("n_cartella").toString(), h.get("pr_data").toString(),
									h.get("pr_progr").toString(), codice_medico);

				dbr.put("ubicazione", ISASUtil.getValoreStringa(h,"ubicazione"));
				dbr.put("tempo_t", tempoT);				
				
				// 08/11/10 m: se c'� la sospensione del verbale NON si deve gestire segnalaz, presaCarico, ecc
				if (!isSospUVM) {
					// 21/05/09 Elisa Croci
					int caso = prendi_dati_caso(dbc, dbr);
					if(caso != -1 && dbr.get("origine").toString().equals(""+GestCasi.CASO_UVM))
					{
						// sono in update VERO
						mySystemOut(" update VERO ");
						if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_UVM))
							gestione_segnalazione(dbc, dbr, h, "update");
					}
					else
					{
						// la scheda e' stata inserita da "elenco segnalazioni"
						mySystemOut("  la scheda e' stata inserita da \"elenco segnalazioni\" ");
						if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_UVM)) {
							gestione_segnalazione(dbc, dbr, h, "insert");
							prendi_dati_caso(dbc, dbr);// 25/06/09 m
						} else // 20/04/10 m
							gestione_caso(dbc, dbr, h, "insert");
					}

	/* BYSP: 250711 - Non � necessaria la gestione presacarico per questa classe (viene chiamata solo da lazio abruzzo e sicilia)

	if(dbr.get("pr_data_verbale_uvm") != null && !dbr.get("pr_data_verbale_uvm").toString().equals("") 
					&& (!h.get("ubicazione").equals(""+GestCasi.UBI_RPIEM))
					&& (!h.get("ubicazione").equals(""+GestCasi.UBI_RLAZI)))
					{
						mySystemOut("update: data verbale == " + dbr.get("pr_data_verbale_uvm"));
						gestione_presacarico_rivalutazione(dbc,dbr,h);
					}
*/
					// Chiusura amministrativa Scheda PUAC
					if(h.containsKey("pr_data_chiusura") && h.get("pr_data_chiusura") != null && !h.get("pr_data_chiusura").equals(""))
					{
						mySystemOut(" update(): CHIUSURA AMMINISTRATIVA SCEHDA PUAC... " + h.get("pr_data_chiusura"));
						// devo chiudere solo il caso UVM
						Hashtable hDati = dbr.getHashtable();
						hDati.put("dt_conclusione", h.get("pr_data_chiusura"));
						// 04/01/10 ----
						String motChiu = (String)h.get("pr_motivo_chiusura");
						String motChiuFlux = getTabVociCodReg(dbc, "VALPCMCH", motChiu);
						hDati.put("motivo", motChiuFlux);
						// 04/01/10 ----
						hDati.put("operZonaConf", (String)dbr.get("pr_oper_ultmod")); // 15/10/09
						System.out.println("Dati per chiusura caso dovuta a chiusura scheda PUAC: " + hDati.toString());
						gestore_casi.chiudiCaso(dbc, hDati);
					}
				}
				
				// 07/11/10
				dbr.put("dt_rev_xlett", dbr.get("pr_data_revisione"));
				// 10/12/10
				dbr.put("tempo_t", tempoT);				
			}

			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}
		catch(CariException ce)
		{
			System.out.println("RLSkPuacEJB.update(): Eccezione= " + ce);
			ce.setISASRecord(null);
			try
			{
				System.out.println("RLSkPuacEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println("RLSkPuacEJB.update(): Eccezione= " + e);
			try
			{
				System.out.println("RLSkPuacEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e1);
			}
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("RLSkPuacEJB.update(): Eccezione= " + e);
			try
			{
				System.out.println("RLSkPuacEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}

			throw e;
		}
		catch(Exception e)
		{
			System.out.println("RLSkPuacEJB.update(): Eccezione= " + e);
			try
			{
				System.out.println("RLSkPuacEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new SQLException("Errore eseguendo una rollback() - " +  e1);
			}

			throw new SQLException("Errore eseguendo una update() - " +  e);
		}
		finally
		{
			if (!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2)  {	System.out.println("RLSkPuacEJB.update(): - Eccezione nella chiusura della connessione= " + e2);	}
			}
		}
	}// END update


	public void delete(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur=null;

		try {
			dbc = super.logIn(mylogin);

			deleteAll(dbc, dbr, dbcur, false);

			dbc.close();
			super.close(dbc);
			done=true;
		}
		catch(DBRecordChangedException e) {
            debugMessage("RLSkPuacEJB.delete(): Eccezione= " + e);
            try {
              System.out.println("RLSkPuacEJB.delete() => ROLLBACK");
              dbc.rollbackTransaction();
            } catch(Exception e1) {
              throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
            }
            throw e;
        }
        catch(ISASPermissionDeniedException e) {
            System.out.println("RLSkPuacEJB.delete(): Eccezione= " + e);
            try{
              System.out.println("RLSkPuacEJB.delete() => ROLLBACK");
              dbc.rollbackTransaction();
           	}catch(Exception e1){
              throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
            }
            throw e;
        }
        catch(Exception e){
            System.out.println("RLSkPuacEJB.delete(): Eccezione= " + e);
            try{
              System.out.println("RLSkPuacEJB.delete() => ROLLBACK");
              dbc.rollbackTransaction();
            }catch(Exception e1){
              throw new SQLException("Errore eseguendo una rollback() - " +  e1);
            }
            throw new SQLException("Errore eseguendo una delete() - " +  e);
        }
        finally{
            if (!done){
            	try {
	                if (dbcur != null)
	                	dbcur.close();
	                dbc.close();
	                super.close(dbc);
	            } catch(Exception e2){
    	            System.out.println("RLSkPuacEJB.delete(): - Eccezione nella chiusura della connessione= " + e2);
        	    }
            }
        }
	}

	// 29/12/09: cancella anche il record collegato su ASS_ANAGRAFICA ed eventualmente chiude il caso UVM
	public void deleteAlsoAssAna(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur=null;

		try	{
			dbc = super.logIn(mylogin);

			deleteAll(dbc, dbr, dbcur, true);

			dbc.close();
			super.close(dbc);
			done=true;
		}
		catch(DBRecordChangedException e) {
            debugMessage("RLSkPuacEJB.deleteAlsoAssAna(): Eccezione= " + e);
            try {
              System.out.println("RLSkPuacEJB.deleteAlsoAssAna() => ROLLBACK");
              dbc.rollbackTransaction();
            } catch(Exception e1) {
              throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
            }
            throw e;
        }
        catch(ISASPermissionDeniedException e) {
            System.out.println("RLSkPuacEJB.deleteAlsoAssAna(): Eccezione= " + e);
            try{
              System.out.println("RLSkPuacEJB.deleteAlsoAssAna() => ROLLBACK");
              dbc.rollbackTransaction();
           	}catch(Exception e1){
              throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
            }
            throw e;
        }
        catch(Exception e){
            System.out.println("RLSkPuacEJB.deleteAlsoAssAna(): Eccezione= " + e);
            try{
              System.out.println("RLSkPuacEJB.deleteAlsoAssAna() => ROLLBACK");
              dbc.rollbackTransaction();
            }catch(Exception e1){
              throw new SQLException("Errore eseguendo una rollback() - " +  e1);
            }
            throw new SQLException("Errore eseguendo una deleteAlsoAssAna() - " +  e);
        }
        finally{
            if (!done){
            	try {
	                if (dbcur != null)
	                	dbcur.close();
	                dbc.close();
	                super.close(dbc);
	            } catch(Exception e2){
    	            System.out.println("RLSkPuacEJB.deleteAlsoAssAna(): - Eccezione nella chiusura della connessione= " + e2);
        	    }
            }
        }
	}

	private void deleteAll(ISASConnection dbc, ISASRecord dbr, ISASCursor dbcur, boolean alsoAssAna) throws Exception
	{
		// 21/05/09 m.
		Hashtable h = dbr.getHashtable();
        dbc.startTransaction();

		gest_scaleVal.cancPuauvmSchede(dbc, h, dbcur);

		// 29/12/09: cancellazione di tutti gli eventuali menbri della commissione UVM
		cancellaAllCompCommPUACUVM(dbc, dbr.get("n_cartella").toString(),
						                   dbr.get("pr_data").toString(),
						                   dbr.get("pr_progr").toString());

		// 29/12/09: cancellazione di tutti i dati del PAP
		cancAllRtPap(dbc, dbr.get("n_cartella").toString(),
						                   dbr.get("pr_data").toString(),
						                   dbr.get("pr_progr").toString());

		if (alsoAssAna) // cancellazione di ASS_ANAGRAFICA ed agende
			cancAssAnagra(dbc, h);
		else // pulizia dei campi relativi alla presa in carico su ASS_ANAGRAFICA
			pulisciAssAnagra(dbc, h);


		ISASRecord dbrCaso = gestore_casi.getCasoRifUvm(dbc, h);
		if (dbrCaso != null) {
			boolean isUVM = ((Boolean)gestore_casi.isCasoUvm(dbrCaso)).booleanValue();
			boolean isAttesa = ((Boolean)gestore_casi.isStatoAttesa(dbrCaso)).booleanValue();
			boolean isToscana = gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_UVM);

			// se caso � UVM in ATTESA e gestisco segnalaz (=Toscana)  --> chiudo il caso aperto dalla SEGNALAZIONE
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
	public Vector query_skValPuacChiuse(myLogin mylogin, Hashtable h) throws  SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try
		{
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM rl_puauvm" +
			" WHERE n_cartella = " + (String)h.get("n_cartella") +
			" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data")) +
			" AND ((pr_data_verbale_uvm IS NOT NULL)" +
			" OR (pr_data_chiusura IS NOT NULL))" + // 06/10/08
			" ORDER BY pr_progr DESC";

			mySystemOut("query_skValPuacChiuse=[" + myselect+ "]");

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			// decodifiche
			for (Enumeration en = vdbr.elements(); en.hasMoreElements(); ){
				ISASRecord dbr_1 = (ISASRecord)en.nextElement();
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("RLSkPuacEJB: query_skValPuacChiuse - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("RLSkPuacEJB: query_skValPuacChiuse - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query_skValPuacChiuse


	// 16/01/08: legge i dati da ASS_ANAGRAFICA e AGENDANT_INTERV.
	// 07/10/08: lettura solo da ASS_ANAGRAFICA
	// 02/03/09: solo rec NON ancora presi in carico
	public Hashtable query_getAssAnagrAge(myLogin mylogin, Hashtable h0)
	{
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
			String strAssAnagrProgressivo = (String)h0.get("ass_angr_progressivo");
			String cart = (String)h0.get("n_cartella"); // 07/10/08

			// lettura da ASS_ANAGRAFICA
			// 10/04/05: se ho il progr si legge con quello, altrimenti si cerca per cognome, nome, luogo e dtNasc
			if ((strAssAnagrProgressivo != null) && (!strAssAnagrProgressivo.trim().equals(""))) {
				selAna = "SELECT * FROM ass_anagrafica" +
				" WHERE progressivo = " + strAssAnagrProgressivo;
			} else if ((cart != null) && (!cart.trim().equals(""))) { // 07/10/08: si cerca x n_cartella
				selAna = "SELECT * FROM ass_anagrafica" +
				" WHERE n_cartella = " + cart +
				" AND data_reg IN (SELECT MAX(data_reg) data_registrazione" +
				" FROM ass_anagrafica a" +
				" WHERE a.n_cartella = " + cart + ")";
			} else {
				strAssAnagrCognome = (String)h0.get("ass_anagr_cognome");
				strAssAnagrCognome = duplicateChar(strAssAnagrCognome, "'"); // 06/06/08
				strAssAnagrNome = (String)h0.get("ass_anagr_nome");
				strAssAnagrNome = duplicateChar(strAssAnagrNome, "'"); // 06/06/08
				strAssAnagrDtNasc = (String)h0.get("ass_anagr_data_nascita");
				strAssAnagrComNasc = (String)h0.get("ass_anagr_com_nascita");

				selAna = "SELECT *" +
				" FROM ass_anagrafica" +
				" WHERE cognome = '" + strAssAnagrCognome + "'" +
				" AND nome = '" + strAssAnagrNome + "'" +
				" AND comune_nascita = '" + strAssAnagrComNasc + "'" +
				" AND data_nascita = " + formatDate(dbc, strAssAnagrDtNasc) +
				" AND data_reg = (SELECT MAX(data_reg) data_registrazione" +
				" FROM ass_anagrafica a" +
				" WHERE a.cognome = '" + strAssAnagrCognome + "'" +
				" AND a.nome = '" + strAssAnagrNome + "'" +
				" AND a.comune_nascita = '" + strAssAnagrComNasc + "'" +
				" AND a.data_nascita = " + formatDate(dbc, strAssAnagrDtNasc) + ")";
			}
			// 02/03/09 ----
// 24/11/09	selAna += " AND esito_contatto = '6'" +
			
			//mod elisa b 11/02/11 : si aggiunge il filtro solo se
			//il metodo e' chiamato da RLSkPuac in questo caso e' presente
			//un campo nella hashtable)
			if((h0.containsKey("filtro_presa_carico")) && (!h0.get("filtro_presa_carico").equals(""))){
				selAna += " AND esito_contatto IN ('6', '15')" +
				" AND (presa_carico <> 'S' OR presa_carico IS NULL)";
			}
			// 02/03/09 ----

			mySystemOut("query_getAssAnagrAge: selAna=" + selAna);
			ISASRecord dbr_1 = dbc.readRecord(selAna);
			if (dbr_1 != null) {
				java.sql.Date dtRegSql = (java.sql.Date)dbr_1.get("data_reg");
				DataWI myDtReg = new DataWI(dtRegSql);
				h_ret.put("data_reg", (myDtReg!=null?myDtReg.getString(0):""));
				// 29/02/08
				java.sql.Date dtInvioSql = (java.sql.Date)dbr_1.get("invio_puac_data");
				DataWI myDtInvio = new DataWI(dtInvioSql);
				String dtInvioStr = myDtInvio.getString(0);
				h_ret.put("invio_puac_data", (dtInvioStr!=null?dtInvioStr:""));


				// 24/12/08
				h_ret.put("ass_angr_progressivo", ((Integer)dbr_1.get("progressivo")).toString());
				
				// 29/09/10 -------------
				if (dbr_1.get("segn_data") != null) {
					java.sql.Date dtSegnSql = (java.sql.Date)dbr_1.get("segn_data");
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
		} catch(Exception e1){
			System.out.println("RLSkPuacEJB.query_getAssAnagrAge - Eccezione=[" + e1 + "]");
			/**
			System.out.println("***************************");
			e1.printStackTrace();
			System.out.println("***************************");
			 **/
			return (Hashtable)null;
		} finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	// 26/02/08: inserimento su tabella PROGETTO di un record con i soli valori della chiave
	private void scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception
	{
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		mySystemOut("-->> insert: Inserito record su tabella PROGETTO");
	}


	// 24/10/08
	private ISASRecord getRecAssAna(ISASConnection mydbc, Hashtable h0) throws Exception
	{
		ISASRecord mydbr = null;
		String sel = "SELECT * FROM ass_anagrafica";

		Object progrAssAna = h0.get("ass_angr_progressivo");
		Object cart = (Object)h0.get("n_cartella");
		Object dtSkVal = (Object)h0.get("pr_data");
		Object progrPuac = (Object)h0.get("pr_progr");

		if (progrAssAna != null)
			sel += " WHERE progressivo = " + progrAssAna;
		else if ((cart != null) && (dtSkVal != null) && (progrPuac != null))
			sel += " WHERE n_cartella = " + cart +
			" AND pr_data = " + formatDate(mydbc, ""+dtSkVal) +
			" AND pr_progr = " + progrPuac +
			" ORDER BY data_reg DESC";
		else
			return null;
		mySystemOut("getRecAssAna: sel=["+sel+"]");

		mydbr = mydbc.readRecord(sel);
		return mydbr;
	}

	// 07/10/08: aggiornamento di ASS_ANAGRAFICA con key RL_PUAUVM e campi relativi alla presa carico
	private void aggiornaAssAna(ISASConnection mydbc, ISASRecord mydbr, Hashtable h0) throws Exception
	{
		if (mydbr != null) {
			mydbr.put("n_cartella", h0.get("n_cartella"));
			mydbr.put("pr_data", h0.get("pr_data"));
			mydbr.put("pr_progr", h0.get("pr_progr"));

			boolean isPresaCarico = (h0.get("pr_data_puac") != null);

// 13/10/09	mydbr.put("presa_carico", (isPresaCarico?"S":"N"));
			// 13/10/09 --
			boolean isSkChiusa = ((h0.get("pr_data_chiusura") != null) && (!((String)h0.get("pr_data_chiusura")).trim().equals("")));
			mydbr.put("presa_carico", (isSkChiusa?"C":(isPresaCarico?"S":"N")));
			// 13/10/09 ---

			if (isPresaCarico) {
// 13/10/09		mydbr.put("presa_carico_data", h0.get("pr_data_puac"));
				// 13/10/09  --
				mydbr.put("presa_carico_data", (isSkChiusa?h0.get("pr_data_chiusura"):h0.get("pr_data_puac")));
				if (isSkChiusa){
					String motChiu = (String)HASH_MAP_MOTCHIUS.get(h0.get("pr_motivo_chiusura"));
					mydbr.put("presa_carico_motivo", (motChiu!=null?motChiu:"9"));
				}
				// 13/10/09 --

				mydbr.put("presa_carico_oper", h0.get("pr_oper_ultmod"));

				mydbr.put("urgente", h0.get("pr_flag_urgente"));
			}
			mydbc.writeRecord(mydbr);
			mySystemOut("aggiornaAssAna: aggiornato progr=["+mydbr.get("progressivo")+"]");
		}
	}

	// 11/03/08: inserimento su ASS_ANAGRAFICA per revisione
	// 24/10/08: anche per inserimento diretto da PUAC per segnalazione di MMG
	private int inserisciAssAnagrafica(ISASConnection mydbc, String strNCartella,
			String dtVerb, String dtRev, String oper, Hashtable h_1) throws Exception
			{
		ISASRecord dbrCart = getDbrDaCartella(mydbc, strNCartella);
		if (dbrCart == null) {
			String msg = "Attenzione: Non esiste il record sulla tabella CARTELLA!";
			throw new CariException(msg, -2);
		}

		ISASRecord dbrAnagC = getDbrDaAnagra_c(mydbc, strNCartella);
		if (dbrAnagC == null)  {
			String msg = "Attenzione: Non esiste il record sulla tabella ANAGRA_C!";
			throw new CariException(msg, -2);
		}

		int intProgressivo = selectProgressivo(mydbc, "ASSOC_PROGR_ANAG");
		ISASRecord dbr = mydbc.newRecord("ass_anagrafica");

		fillUpAssAnagrDaCartella(dbr, dbrCart);
		fillUpAssAnagrDaAnagra_c(dbr, dbrAnagC);

		if ((dtVerb != null) && (dtRev != null)) // REVISIONE
			fillUpAssAnagrCampiCriticiRev(mydbc, dbr, strNCartella, dtVerb, dtRev, oper, "PUAC", h_1);
		else // INSERIMENTO DIRETTO per SEGNALAZ MMG
			fillUpAssAnagrCampiCriticiNew(mydbc, dbr, strNCartella, oper, "PUAC", h_1);

		dbr.put("progressivo",new Integer(intProgressivo));

		mydbc.writeRecord(dbr);
		mySystemOut("inserisciAssAnagrafica: inserito record con progr= " + intProgressivo);
		return intProgressivo;
			}

	private ISASRecord getDbrDaCartella(ISASConnection dbc, String strNCartella) throws SQLException
	{
		try {
			String strQuery = "SELECT *" +
			" FROM cartella" +
			" WHERE n_cartella = " + strNCartella;
			mySystemOut("getDbrDaCartella/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		}
		catch (Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/getDbrDaCartella - "+  e1);
		}
	}

	private ISASRecord getDbrDaAnagra_c(ISASConnection dbc, String strNCartella) throws SQLException
	{
		try {
			String strQuery = "SELECT *" +
			" FROM anagra_c" +
			" WHERE n_cartella = " + strNCartella +
			" AND data_variazione = (" +
			" SELECT MAX(data_variazione) max_data_var" +
			" FROM anagra_c a" +
			" WHERE a.n_cartella = " + strNCartella + ")";
			mySystemOut("getDbrDaAnagra_c/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		}
		catch (Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/getDbrDaAnagra_c - "+  e1);
		}
	}

	private void fillUpAssAnagrDaCartella(ISASRecord dbr, ISASRecord dbrCart) throws SQLException
	{
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
		}
		catch (Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrDaCartella - "+  e1);
		}
	}

	private void fillUpAssAnagrDaAnagra_c(ISASRecord dbr, ISASRecord dbrAnagC) throws SQLException
	{
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
		}
		catch (Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrDaAnagra_c - "+  e1);
		}
	}

	private void fillUpAssAnagrCampiCriticiRev(ISASConnection dbc, ISASRecord dbr,
			String strNCartella, String dtVerb, String dtRev,
			String strCodOperatore, String strTipoOperatore, Hashtable h_1)
	throws SQLException
	{
		NumberDateFormat ndf = new NumberDateFormat();
		try {
			String strQueryOper = "SELECT *" +
			" FROM operatori" +
			" WHERE codice = '" + strCodOperatore + "'";
			mySystemOut("fillUpAssAnagrCampiCriticiRev/strQueryOper: " + strQueryOper);
			ISASRecord dbrOper = dbc.readRecord(strQueryOper);
			String cognOp = "";
			String nomeOp = "";
			String presidioOp = "";// 06/06/08
			if (dbrOper != null) {
				if (dbrOper.get("cognome") != null)
					cognOp = (String)dbrOper.get("cognome");
				if (dbrOper.get("nome") != null)
					nomeOp = (String)dbrOper.get("nome");
				if (dbrOper.get("cod_presidio") != null) // 06/06/08
					presidioOp = (String)dbrOper.get("cod_presidio");
			}
			dbr.put("segn_cognome", cognOp);
			dbr.put("segn_nome", nomeOp);


			// revisione
			dbr.put("inserita_autom", "R");
			// dtRegistrazione = dtVerbaleUVM
			dbr.put("data_reg", dtVerb);
			// dtFirmaIstanza = dtRevisione - 30 gg
			String dataR = dtRev.substring(8,10) + dtRev.substring(5,7) +dtRev.substring(0,4);
			DataWI dtWIAppo = new DataWI(dataR);
			DataWI dtWIRev = dtWIAppo.aggiungiGg(-30);
			dbr.put("invio_puac_data", dtWIRev.getFormattedString2(1));

			dbr.put("esito_contatto", "6");
			dbr.put("presa_carico", "N");
			dbr.put("urgente", "N");
			String testo = "Operatore: " + strCodOperatore + " - " + cognOp + " " + nomeOp +
			" - Tipo: " + strTipoOperatore;
			dbr.put("arrivato_note", testo);
			// 06/06/08 ---
			dbr.put("sospesa_flag", "N");
			dbr.put("cod_operatore", strCodOperatore);
			dbr.put("cod_presidio", presidioOp);
			// 06/06/08 ---

			// 07/10/08 ----
			dbr.put("soc_cod", h_1.get("pr_soc_codice"));
			// 30/03/09	dbr.put("soc_data", h_1.get("pr_soc_data_visita"));
			dbr.put("soc_data", (java.sql.Date)null);
			dbr.put("soc_carico", "N");
			dbr.put("san_cod", h_1.get("pr_inf_codice"));
			dbr.put("san_data", (java.sql.Date)null);
			dbr.put("san_carico", "N");
			// 07/10/08 ----
		}
		catch (Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrCampiCriticiRev - "+  e1);
		}
	}

	// 24/10/08
	private void fillUpAssAnagrCampiCriticiNew(ISASConnection dbc, ISASRecord dbr,
			String strNCartella,
			String strCodOperatore, String strTipoOperatore, Hashtable h_1) throws SQLException
			{
		NumberDateFormat ndf = new NumberDateFormat();
		try {
			// segnalante = MMG
			String codMed = (String)dbr.get("mecodi");
			String strQueryMed = "SELECT *" +
			" FROM medici" +
			" WHERE mecodi = '" + codMed + "'";
			mySystemOut("fillUpAssAnagrCampiCriticiNew/strQueryMed: " + strQueryMed);
			ISASRecord dbrMed = dbc.readRecord(strQueryMed);
			String cognM = "";
			String nomeM = "";
			if (dbrMed != null) {
				if (dbrMed.get("mecogn") != null)
					cognM = (String)dbrMed.get("mecogn");
				if (dbrMed.get("menome") != null)
					nomeM = (String)dbrMed.get("menome");
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
			boolean isSkChiusa = ((h_1.get("pr_data_chiusura") != null) && (!((String)h_1.get("pr_data_chiusura")).trim().equals("")));
			dbr.put("presa_carico", (isSkChiusa?"C":"S"));
			dbr.put("presa_carico_data", (isSkChiusa?h_1.get("pr_data_chiusura"):h_1.get("pr_data_puac")));
			if (isSkChiusa){
				String motChiu = (String)HASH_MAP_MOTCHIUS.get(h_1.get("pr_motivo_chiusura"));
				dbr.put("presa_carico_motivo", (motChiu!=null?motChiu:"9"));
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
			String presidioOp = (String)it.pisa.caribel.util.ISASUtil.getDecode(dbc, "operatori", "codice", strCodOperatore, "cod_presidio");
			dbr.put("cod_presidio", presidioOp);

			dbr.put("n_cartella", h_1.get("n_cartella"));
			dbr.put("pr_data", h_1.get("pr_data"));
			dbr.put("pr_progr", h_1.get("pr_progr"));
		} catch (Exception e1){
			System.out.println(e1);
			throw new SQLException("RLSkPuacEJB/fillUpAssAnagrCampiCriticiNew - "+  e1);
		}
			}



	private void updateAgendant_pua(ISASConnection dbc, ISASRecord dbr) throws Exception
	{
		String strOperatore = (String) dbr.get("presa_carico_oper");
		String strDtAppuntamento = "" + dbr.get("ag_data_app");
		String strOraAppuntamento = (String) dbr.get("ag_ora_app");
		String strPresaCarico = (String) dbr.get("presa_carico");

		try
		{
			String strQuery = "SELECT *" +
			" FROM agendant_pua" +
			" WHERE ag_cod_oper = '" + strOperatore + "'" +
			" AND ag_data_app = " + formatDate(dbc, strDtAppuntamento) +
			" AND ag_ora_app = '" + strOraAppuntamento + "'";
			mySystemOut("updateAgendant_pua/strQuery: " + strQuery);
			ISASRecord dbrAge = dbc.readRecord(strQuery);
			if (strPresaCarico.equals("S"))
				dbrAge.put("ag_esito", new Integer(1));
			else if (strPresaCarico.equals("C"))
				dbrAge.put("ag_esito", new Integer(2));
			else
				dbrAge.put("ag_esito", new Integer(3));

			dbc.writeRecord(dbrAge);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Exception("SocElecasiEJB/updateAgendant_pua: " + e);
		}
	}

	// 10/03/08: generazione num verbale x anno e x zona
	private int faiNumVerbale(ISASConnection dbc, String dtVerbale,
			String nVerbale, String codOper)  throws Exception
	{
		int numVerb = 0;
		String zona = "";

		boolean giaVerbaliz = ((nVerbale != null) && (!nVerbale.trim().equals("")) && (!nVerbale.trim().equals("0")));
		if ((dtVerbale != null) && (!dtVerbale.trim().equals("")) && (!giaVerbaliz)) {
			String anno = dtVerbale.substring(0,4);
			zona = (String)it.pisa.caribel.util.ISASUtil.getDecode(dbc, "operatori", "codice", codOper, "cod_zona");

			String key_ChiaviLibere = "VERBALEUVM_" + anno + "_" + zona;
			numVerb = selectProgressivo(dbc, key_ChiaviLibere);
		}
		return numVerb;
	}

	// 24/10/08: generazione num protocollo x anno e x zona
	private String faiNumProtocollo(ISASConnection dbc, String dtSkPuac,
			String numProto, String codOper)  throws Exception
	{
		int numProtoMax = 0;
		String anno = "";
		String zona = "";

		boolean giaProtocollata = ((numProto != null) && (!numProto.trim().equals("")) && (!numProto.trim().equals("0")));
		if ((dtSkPuac != null) && (!dtSkPuac.trim().equals("")) && (!giaProtocollata)) {
			anno = dtSkPuac.substring(0,4);
			zona = (String)it.pisa.caribel.util.ISASUtil.getDecode(dbc, "operatori", "codice", codOper, "cod_zona");

			String key_ChiaviLibere = "PUAUVM_NUMPROTO_" + anno + "_" + zona;
			numProtoMax = selectProgressivo(dbc, key_ChiaviLibere);
		}
		if (numProtoMax > 0)
			return (anno+"/"+numProtoMax);
		return null;
	}




	// 10/11/08
	private String decodPresidio(ISASConnection mydbc, String codPres) throws Exception
	{
		String desc = "";
		if ((codPres == null) || ("".equals(codPres)))
			return "";

		String sel = "SELECT p.* FROM presidi p" +
		" WHERE p.codpres = '" + codPres + "'" +
		" AND EXISTS (SELECT k1.* FROM conf k1" +
		" WHERE p.codreg = k1.conf_txt" +
		" AND k1.conf_kproc = 'SINS'" +
		" AND k1.conf_key = 'codice_regione')" +
		" AND EXISTS (SELECT k2.* FROM conf k2" +
		" WHERE p.codazsan = k2.conf_txt" +
		" AND k2.conf_kproc = 'SINS'" +
		" AND k2.conf_key = 'codice_usl')";

		ISASRecord rec = mydbc.readRecord(sel);
		if ((rec != null) && (rec.get("despres") != null))
			desc = (String)rec.get("despres");
		return desc.trim();
	}


	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, String strNAssistito, String strDtSkVal)
	throws Exception
	{
		String dt = strDtSkVal;
		// 25/06/07  dt = dt.substring(0,2) + dt.substring(3,5) + dt.substring(6,10);
		dt=dt.substring(8,10) + dt.substring(5,7) + dt.substring(0,4);
		DataWI dataWIApertura = new DataWI(dt);

		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" +
		" FROM progetto" +
		" WHERE n_cartella = " + strNAssistito +
		" AND pr_data_chiusura IS NOT NULL";

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile

		if ((java.sql.Date)rec.get("max_data_chius") == null)
			return false; // Ammissibile

		dt = ((java.sql.Date)rec.get("max_data_chius")).toString();
		if (dt.equals(""))
			return false; // Ammissibile

		dt=dt.substring(0,4) + dt.substring(5,7) + dt.substring(8,10);
		String max_data_chiusura = dt;
		int rit = dataWIApertura.confrontaConDt(max_data_chiusura);
		// Codici ritornati da confrontaConDt:
		// se data_apertura � maggiore di data_chiusura restituisce 1
		// se data_apertura � minore di data_chiusura restituisce 2
		// se data_apertura � = di data_chiusura restituisce 0
		// se da errore -1
		if ((rit == 2) || (rit == 0))
			return true; // Non ammissibile
		else if (rit < 0)
		{
			throw new Exception("SocAssProgettoEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
			// Si � verificato un errore nel metodo di confronto delle 2 date.
		}
		else // (rit == 1)
			return false; // Ammissibile
	}

	private boolean dtApeProgettoLTDtApeCartella(ISASConnection dbc, String strNAssistito, String strDtSkVal) throws Exception
	{
		String mySel = "SELECT *" +
		" FROM cartella" +
		" WHERE n_cartella = " + strNAssistito +
		" AND data_apertura > " + formatDate(dbc, strDtSkVal);

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile
		else
			return true;
	}

// ========================================== GESTIONE CASI x FLUSSI AD-RSA ============================

	private int gestione_caso(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov) throws Exception
	{
		mySystemOut("gestione_caso -- HASH: " +  h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;
		int origine = -1;

		if(h.get("id_caso") != null && !h.get("id_caso").equals("-1")) // esiste un caso
		{
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(h.get("stato").toString());
			id_caso = Integer.parseInt(h.get("id_caso").toString());
			origine = Integer.parseInt(h.get("origine").toString());
		}

		// se sono in INSERT della scheda di valutazione e non esiste nessun caso oppure
		// esiste ma e' sanitario o concluso allora devo creare un caso UVM
		if(prov.equals("insert") && (id_caso == -1 || (id_caso != -1 && (origine != gestore_casi.CASO_UVM || stato_caso == gestore_casi.STATO_CONCLU))))
		{
			mySystemOut("gestione_caso() - caso DA CREARE");
			try{
				if (h.get("pr_data") == null)
					h.put("pr_data", dbr.get("pr_data"));

				if(!h.containsKey("dt_segnalazione") || (h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals("")))
					h.put("dt_segnalazione", h.get("pr_data_puac"));

				// 26/06/09 Elisa: per non far inserire il "motivo conclusione" nel record del caso al momento della
				// sua creazione
				if(h.containsKey("motivo"))
					h.put("motivo", new Integer(0));

				h.put("tipo_caso", new Integer(GestCasi.CASO_UVM));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_UVM));

				id_caso = gestore_casi.apriCasoUvm(dbc, h).intValue();
				mySystemOut("gestione_caso() - caso creato id_caso=["+id_caso+"]");
				return id_caso;
			}
			catch (Exception e)
			{
				System.out.println("RLSkPuacEJB: gestione_caso() " + e);
				throw e;
			}
		}
		else return id_caso; // se sono in update dovrei avere gia' il caso! Lo ritorno e basta...
	}

	// 20/05/09 Elisa Croci
	/* 1) Il caso non esiste: creo il caso e la segnalazione
	 * 2) Il caso esiste ma e' chiuso: creo il caso e la segnalazione
	 * 3) Il caso e' attivo: aggiorno la segnalazione
	*/
	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov)
	throws NumberFormatException, ISASMisuseException, CariException
	{
		mySystemOut("gestione_segnalazione -- HASH: " +  h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;
		int origine = -1;

		h.put("operZonaConf", (String)dbr.get("pr_oper_ultmod")); // 15/10/09

		Hashtable hCaso = new Hashtable();

		if(h.containsKey("id_caso") && (h.get("id_caso") != null && !h.get("id_caso").equals("-1")))
		{
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(h.get("stato").toString());
			id_caso = Integer.parseInt(h.get("id_caso").toString());

			if(dbr.getHashtable().containsKey("origine"))
				origine = Integer.parseInt(dbr.get("origine").toString());
		}

		mySystemOut("gestione_segnalazione -- stato caso: " + stato_caso + " id_caso " + id_caso + " origine " + origine);

		// se sono in insert e il caso non esiste oppure
		// esiste MA e' sanitario o concluso, devo crearne uno!
		if(prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU || origine != GestCasi.CASO_UVM))
		{
			mySystemOut("gestione_segnalazione -- INSERT ");

			// creo il caso UVM
			try
			{
				h.put("tipo_caso", new Integer(GestCasi.CASO_UVM));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_UVM));

				if(!h.containsKey("dt_segnalazione") || (h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals("")))
					h.put("dt_segnalazione", h.get("pr_data_puac"));

				/*if(!h.containsKey("dt_presa_carico") || (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals("")))
					h.put("dt_presa_carico", h.get("dt_segnalazione"));*/

				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
				// arriva la data del progetto, cosi' me la copio dal dbr!
				h.put("pr_data", dbr.get("pr_data"));

				// 26/06/09 Elisa: per non far inserire il "motivo conclusione" nel record del caso al momento della
				// sua creazione
				if(h.containsKey("motivo"))
					h.put("motivo", new Integer(0));

				ISASRecord rec_segn = gestore_segnalazioni.insert(dbc, h);

				if(rec_segn != null)
				{
					Enumeration en = rec_segn.getHashtable().keys();
					while(en.hasMoreElements())
					{
						String chiave = en.nextElement().toString();
						dbr.put(chiave, rec_segn.get(chiave));
					}

					return Integer.parseInt(rec_segn.get("id_caso").toString());
				}
				else return -1;
			}
			catch (CariException e) // 17/11/09
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, insert -- " + e);
				throw e;
			}
			catch (DBRecordChangedException e)
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
			catch (ISASPermissionDeniedException e)
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
			catch (SQLException e)
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
			catch (Exception e)
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
		}
		// il caso esiste e non e' concluso
		else if(id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU && stato_caso != -1))
		{
			mySystemOut("gestione_segnalazione -- UPDATE ");

			// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione
			try
			{
				Enumeration e = dbr.getHashtable().keys();
				while(e.hasMoreElements())
				{
					String chiave = e.nextElement().toString();

					if(!h.containsKey(chiave))
						h.put(chiave, dbr.get(chiave));
				}

				mySystemOut("gestione_segnalazione, DBR: " + dbr.getHashtable().toString());
				ISASRecord new_segnalazione = gestore_segnalazioni.update(dbc, h);

				if(new_segnalazione != null)
				{
					Enumeration en = new_segnalazione.getHashtable().keys();
					while(en.hasMoreElements())
					{
						String chiave = en.nextElement().toString();
						dbr.put(chiave, new_segnalazione.get(chiave));
					}
				}

				return id_caso;
			}
			catch (CariException e) // 17/11/09
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, update -- " + e);
				throw e;
			}
			catch (Exception e)
			{
				System.out.println("RLSkPuacEJB gestione_segnalazione, update() -- " + e);
				return id_caso;
			}
		}
		else return id_caso;
	}

	private void gestione_presacarico_rivalutazione(ISASConnection dbc, ISASRecord dbr, Hashtable h)
	throws Exception, CariException
	{
		mySystemOut("gestione_presacarico() HASH -- "+ h.toString() + " \n DBR == " + dbr.getHashtable().toString());
		int tempoT = -1;

		if(h.containsKey("tempo_t"))
			tempoT = Integer.parseInt(h.get("tempo_t").toString());
		else tempoT = Integer.parseInt(dbr.get("tempo_t").toString());

		h.put("operZonaConf", (String)dbr.get("pr_oper_ultmod")); // 15/10/09

		// 23/10/09: x EVENTO PRESACARICO ---
		if ((!h.containsKey("liv_isogravita")) && (dbr.get("liv_isogravita") != null))
			h.put("liv_isogravita", dbr.get("liv_isogravita"));
		// 23/10/09 ---

		try
		{
			// presa carico
			if(tempoT == 0)
			{
				mySystemOut("-- INSERISCO LA PRESA IN CARICO");

				if(!h.containsKey("dt_presa_carico") || (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals("")))
					h.put("dt_presa_carico", h.get("pr_data_verbale_uvm"));

/** 27/12/10					
				Integer result = gestore_casi.presaCaricoCasoUvm(dbc, h);
				mySystemOut("gestore casi, PRESA CARICO UVM? " + result);
**/
				
				int ubic = Integer.parseInt(ISASUtil.getValoreStringa(h,"ubicazione").toString());
				if(ubic != GestCasi.UBI_RTOSC)
				{
					if(!h.containsKey("dt_valutazione") || h.get("dt_valutazione") == null ||  h.get("dt_valutazione").equals(""))
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
							
				if(rec_pc != null)
				{
					Enumeration en1 = rec_pc.getHashtable().keys();
					while(en1.hasMoreElements())
					{
						String chiave = en1.nextElement().toString();
						dbr.put(chiave, rec_pc.get(chiave));
					}

					dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
					
					// 27/12/10
					dbr.put("update_presacar", "S");					
				}
			}
			else // rivalutazione perche' T > 0
			{
				mySystemOut("-- INSERISCO RIVALUTAZIONE");

				if(!h.containsKey("dt_rivalutazione") || h.get("dt_rivalutazione") == null || h.get("dt_rivalutazione").equals(""))
					h.put("dt_rivalutazione", h.get("pr_data_verbale_uvm"));

// 27/12/10		gestore_casi.rivalutazCasoUvm(dbc, h);
				
// 27/12/10		ISASRecord rec_riv = gestore_rivalutazioni.insert(dbc, h);
				// 27/12/10
				ISASRecord rec_riv = null;
				if ((h.get("update_rivalutazione") != null) && ((h.get("update_rivalutazione").toString()).trim().equals("S"))) {
					h.put("progr", ""+tempoT);
					rec_riv = gestore_rivalutazioni.update(dbc, h);
				} else {
					gestore_casi.rivalutazCasoUvm(dbc, h);	
				
					rec_riv = gestore_rivalutazioni.insert(dbc, h);
				}
				// 27/12/10 ---
				
				if(rec_riv != null)
				{
					Enumeration en1 = rec_riv.getHashtable().keys();
					while(en1.hasMoreElements())
					{
						String chiave = en1.nextElement().toString();
						dbr.put(chiave, rec_riv.get(chiave));
					}
					
					// 27/12/10
					dbr.put("update_rivalutazione", "S");						
				}
			}
		}
		catch (CariException e) // 17/11/09
		{
			System.out.println("RLSkPuacEJB gestione_presacarico() -- " + e);
			throw e;
		}
		catch (NumberFormatException e)
		{
			System.out.println("RLSkPuacEJB gestione_presacarico() " + e);
			throw e;
		}
		catch (ISASMisuseException e)
		{
			System.out.println("RLSkPuacEJB gestione_presacarico() " + e);
			throw e;
		}
	}

	// 25/05/09 Elisa Croci
// 24/12/10	private void prendi_presacarico_rivalutazione(ISASConnection dbc, int caso,ISASRecord dbr)
	private void prendi_presacarico_rivalutazione(ISASConnection dbc, int caso,ISASRecord dbr, Integer tempoT)
	{
		try
		{
			mySystemOut("prendi_presacarico_rivalutazione: tempo_t=["+tempoT.toString()+"]");	
			if(caso != -1)
			{
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				h.put("ubicazione", dbr.get("ubicazione"));

// 24/12/10		if(Integer.parseInt(dbr.get("tempo_t").toString()) == 0)
				if ((tempoT.intValue()) == 0)
				{

					mySystemOut("prendi_presacarico_rivalutazione: PRENDO PRESA CARICO");
					ISASRecord res = gestore_presacarico.queryKey(dbc, h);

					if(res != null)
					{
						Enumeration e = res.getHashtable().keys();
						while(e.hasMoreElements())
						{
							String chiave = e.nextElement().toString();
							dbr.put(chiave, res.get(chiave));
						}

						dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());
						
						// 27/12/10
						dbr.put("update_presacar", "S");
					}
				}
				else
				{
					mySystemOut("prendi_presacarico_rivalutazione: PRENDO RIVALUTAZIONE");
					h.put("pr_progr", dbr.get("pr_progr"));
					
					// 24/12/10: la chiave di RT/M_RIVALUTAZIONE � "progr"
					h.put("progr", tempoT.toString());
					
					ISASRecord res = gestore_rivalutazioni.queryKey(dbc, h);

					if(res != null)
					{
						Enumeration e = res.getHashtable().keys();
						while(e.hasMoreElements())
						{
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
				if(!ISASUtil.getValoreStringa(h,"ubicazione").equals(Integer.toString(GestCasi.UBI_RTOSC)))
				{
					if(dbr.get("id_cartella") == null || dbr.get("id_cartella").equals(""))
						dbr.put("id_cartella", dbr.get("n_cartella"));
				}
			}
		}
		catch (ISASMisuseException e1)
		{
			System.out.println("RLSkPuacEJB prendi_presacarico_rivalutazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
		}
		catch (Exception e)
		{
			System.out.println("RLSkPuacEJB prendi_presacarico_rivalutazione, fallimento! -- " + e);
		}
	}

	// 20/05/09 Elisa Croci
	// prendo la segnalazione relativa al caso a cui il contatto deve fare riferimento
	private boolean prendi_segnalazione(ISASConnection dbc, int caso,ISASRecord dbr)
	{
		try
		{
			/* prendo la segnalazione solo se il caso esiste e se sono in un contesto in cui si
		 		gestiscono le segnalazioni
		 	*/
			if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_UVM) && caso != -1)
			{
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				ISASRecord res = gestore_segnalazioni.queryKey(dbc, h);

				if(res != null)
				{
					Enumeration e = res.getHashtable().keys();
					while(e.hasMoreElements())
					{
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}
				}

				return true;
			}
			else return false;
		}
		catch (ISASMisuseException e1)
		{
			System.out.println("RLSkPuacEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
			return false;
		}
		catch (Exception e)
		{
			System.out.println("RLSkPuacEJB prendi_segnalazione, fallimento! -- " + e);
			return false;
		}
	}

	// 20/05/09 Elisa Croci
	// dato un contatto, prendo il caso attivo se esiste altrimenti quello chiuso piu' recente!
	private int prendi_dati_caso(ISASConnection dbc, ISASRecord dbr)
	{
		mySystemOut(" prendi_dati_caso DBR == " + dbr.getHashtable().toString());
		Hashtable h = new Hashtable();

		try
		{
			h.put("n_cartella", dbr.get("n_cartella"));
			h.put("pr_data",dbr.get("pr_data"));

			mySystemOut("-- prendi dati caso: " + h.toString());

			ISASRecord rec = null;

			if(dbr.getHashtable().containsKey("tempo_t"))
			{
				if(dbr.get("tempo_t").toString().equals("0"))
					rec = gestore_casi.getCasoRifUvm(dbc, h);
				else rec = gestore_casi.getCasoRif(dbc, h);
			}
			else
				rec = gestore_casi.getCasoRifUvm(dbc, h);

			if(rec != null)
			{
				Enumeration e = rec.getHashtable().keys();
				while(e.hasMoreElements())
				{
					String chiave = e.nextElement().toString();
					dbr.put(chiave, rec.get(chiave));
				}

				int caso = Integer.parseInt(dbr.get("id_caso").toString());
				return caso;
			}
			else return -1;
		}
		catch(ISASMisuseException e)
		{
			System.out.println("RLSkPuacEJB prendi_dati_caso, manca chiave primaria! -- " + e);
			return -1;
		}
		catch (Exception e)
		{
			System.out.println("RLSkPuacEJB prendi_dati_caso, fallimento! -- " + e);
			return -1;
		}
	}

// ========================================== GESTIONE SCALE x FLUSSI AD-RSA ============================

	// 21/05/09 m.: lettura scale con dtMax per un certo tempoT dopo la conclusione
	// del CASO precedente
	public ISASRecord getScaleMaxPerT(myLogin mylogin, Hashtable h) throws Exception
	{
		boolean done = false;
		ISASConnection dbc = null;
	  	try {
			dbc = super.logIn(mylogin);
			mySystemOut("getScaleMaxPerT - h=["+h.toString()+"]");

			// lettura dtConclusione CASO precedente
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			h.put("dtchius_casoprec", dtChiusCasoPrec);

			ISASRecord dbr = gest_scaleVal.getScaleMaxPerT(dbc, h);

		  	dbc.close();
	        super.close(dbc);
	        done = true;
			return dbr;
	    } catch (Exception e) {
	            e.printStackTrace();
	            throw new SQLException("RLSkPuacEJB.getScaleMaxPerT: e="+e);
	    } finally {
	        if (!done){
	            try{
	            	dbc.close();
	            	super.close(dbc);
	            }catch(Exception e2){System.out.println(e2);}
	        }
	    }
	}

	// 21/05/09 m.: lettura dtConclusione CASO precedente
	private String getDtChiuCasoPrec(ISASConnection dbc, Hashtable h) throws Exception
	{
		String dtChiusPrec = "";
		// 11/06/10
		h.put("orig_caso_chiuso", new Integer(gestore_casi.CASO_UVM));

		ISASRecord lastCasoChiu = (ISASRecord)gestore_casi.getLastCasoChiuso(dbc, h);

		if ((lastCasoChiu != null) && (lastCasoChiu.get("dt_conclusione") != null))
			dtChiusPrec = "" + lastCasoChiu.get("dt_conclusione");
		mySystemOut("getDtChiuCasoPrec - dtChiusPrec=["+dtChiusPrec+"]");

		return dtChiusPrec;
	}

	// 11/06/09 m.
	public Integer query_getTempoTAllaData(myLogin mylogin, Hashtable h) throws Exception
	{
		boolean done = false;
		ISASConnection dbc = null;

		Integer ret = new Integer(-1);

		try{
			dbc = super.logIn(mylogin);

			mySystemOut(" query_getTempoTAllaData() == H per getCasoAllaData() == " + h.toString());
			ISASRecord dbr = (ISASRecord)gestore_casi.getCasoAllaData(dbc, h);

			if ((dbr != null) && (dbr.get("tempo_t") != null))
				ret = (Integer)dbr.get("tempo_t");

			dbc.close();
			super.close(dbc);
			done = true;
			return ret;
		}
		catch(Exception e){
			System.out.println("RLSkPuacEJB.query_getTempoT(): " + e);
			throw e;
		} finally {
			if(!done){
				try	{
					dbc.close();
					super.close(dbc);
					return new Integer(-1);
				}catch(Exception e2){
					System.out.println("RLSkPuacEJB.query_getTempoT(): " + e2);
					throw e2;
				}
			}
		}
	}


// ===================================== FINE GESTIONE SCALE x FLUSSI AD-RSA ============================



	// 06/06/08
	public String duplicateChar(String s, String c) {
		if ((s == null) || (c == null)) return s;
		String mys = new String(s);
		int p = 0;
		while (true) {
			int q = mys.indexOf(c, p);
			if (q < 0) return mys;
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	private void mySystemOut(String msg)
  	{
		if (myDebug)
			System.out.println(nomeEJB + ": " + msg);
	}

	// 03/06/09 Elisa Croci
	private void cancellaAllCompCommPUACUVM(ISASConnection dbc, String n_cartella, String pr_data, String pr_progr) throws Exception
	{
		mySystemOut(" cancellaAllCompCommPUACUVM ");
		try
		{
			String sel = "SELECT * FROM puauvm_commissione WHERE " +
						" n_cartella = " + n_cartella +
						" AND pr_data = " + formatDate(dbc, pr_data) +
						" AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllCompCommPUACUVM -- " + sel);
			ISASCursor dbcur = dbc.startCursor(sel);

			Vector vdbr = dbcur.getAllRecord();
	        if ((vdbr != null) && (vdbr.size() > 0))
       		{
	        	for(int i = 0; i < vdbr.size(); i++)
	        	{
	            	ISASRecord dbrec = (ISASRecord)vdbr.get(i);
	                if (dbrec != null)
	                {
		                String sel2 = sel +
						" AND pr_presenza = " + dbrec.get("pr_presenza");

						ISASRecord dbrD = dbc.readRecord(sel2);
	                    dbc.deleteRecord(dbrD);
	                }
	            }
       		}

	        if (dbcur != null)	dbcur.close();

		}
		catch(Exception e)
		{
			System.out.println("RLSkPuacEJB: cancellaAllCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private void scriviAllCompCommPUACUVM(ISASConnection dbc, Vector vCompComm, String codice_medico) throws Exception
	{
		mySystemOut(" scriviAllCompCommPUACUVM ");
		try
		{
			for (int i = 0; i < vCompComm.size(); i++)
			{
				Hashtable hash = (Hashtable)vCompComm.get(i);
				mySystemOut("scriviAllCompCommPUACUVM HASH " + i + " == " + hash.toString());
				scriviCompCommPUACUVM(dbc, hash, codice_medico, i);
			}
		}
		catch(Exception e)
		{
			System.out.println("RLSkPuacEJB: scriviAllCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private void scriviCompCommPUACUVM(ISASConnection dbc, Hashtable h, String codice_medico, int i) throws Exception
	{
		mySystemOut(" scriviCompCommPUACUVM -- H = " + h.toString());
		try
		{
			ISASRecord rec = dbc.newRecord("puauvm_commissione");

			Enumeration e = h.keys();
			while (e.hasMoreElements())
			{
				String key = e.nextElement().toString();
				mySystemOut(" Chiave: " + key);
				rec.put(key, h.get(key));
			}

			rec.put("pr_presenza", new Integer(i));

			mySystemOut(" Scrivere REC: " + rec.getHashtable().toString());
			dbc.writeRecord(rec);
			mySystemOut(" Scritto!!! ");
		}
		catch(Exception e)
		{
			System.out.println("RLSkPuacEJB: scriviCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private void leggiCompCommPUACUVM(ISASConnection dbc, ISASRecord dbr, String n_cartella, String pr_data, String pr_progr, String codice_medico) throws Exception
	{
		mySystemOut("  leggiCompCommPUACUVM == pr_data " + pr_data + " n_cartella " + n_cartella + " pr_progr " + pr_progr);
		String select = "SELECT * FROM puauvm_commissione WHERE " +
					" pr_data = " + formatDate(dbc, pr_data) +
					" AND n_cartella = " + n_cartella +
					" AND pr_progr = " + pr_progr +
					" ORDER BY pr_presenza ";

		mySystemOut("  leggiCompCommPUACUVM == " + select);
		try
		{
			ISASCursor dbcur = dbc.startCursor(select);
			Vector vdbr = dbcur.getAllRecord();
			Vector result = new Vector();

			for(int i = 0; i < vdbr.size(); i++)
			{
				ISASRecord rec = (ISASRecord) vdbr.get(i);
				if(rec.get("pr_tipo") != null && !rec.get("pr_tipo").equals(""))
				{
					String qualifica = rec.get("pr_tipo").toString();

					rec.put("desc_qualifica", hash_tp_oper.get(qualifica).toString());
					String cognome = "";
					String nome = "";

					if(qualifica.equals(codice_medico))
					{
						cognome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "mecogn", "cognome");
						nome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "menome", "nome");
					}
					else
					{
						cognome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "cognome", "cognome");
						nome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "nome", "nome");
					}

					rec.put("cognome_op", cognome);
					rec.put("nome_op", nome);
				}

				result.add(rec);
			}

			if(dbcur != null) dbcur.close();

			for(int i = 0; i < result.size(); i++)
			{
				ISASRecord r = (ISASRecord) result.get(i);
				mySystemOut("leggiCompCommPUACUVM - RECORD " + i + " == " + r.getHashtable().toString());
			}

			dbr.put("griglia_componentiCommPUAUVM", result);
		}
		catch (Exception e)
		{
			System.out.println("RLSkPuacEJB: leggiCompCommPUACUVM [" + e + "]");
			throw e;
		}
	}

	private String getDescrChiaveConf(ISASConnection dbc, String chiave)
	{
		try
		{
			String mysel = "SELECT conf_txt FROM conf WHERE "+
							" conf_kproc = 'SINS' " + "" +
							" AND conf_key = '"+ chiave+"' ";

			System.out.println(" getDescrChiaveConf " + mysel);
			ISASRecord dbtxt = dbc.readRecord(mysel);

			if(dbtxt != null)
				return dbtxt.get("conf_txt").toString();
			else return null;
		}
		catch (Exception ex) { return null;	}
	}

	private Hashtable faiHashTpOper() {
		try{
			return ManagerOperatore.getTipiOperatori(null);
		}catch(Exception ex){
			LOG.error("Si è verificato un errore in "+this.getClass().getName()+".faiHashTpOper - Exception:"+ex);
			return null;
		}
	}
	
	// 13/10/09: mappa il motivo di chiusura skPuac con quello della skPuntoInsieme
	private static Hashtable faiHashMapMotChius()
    {
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
	private void cancAllRtPap(ISASConnection dbc, String n_cartella, String pr_data, String pr_progr) throws Exception
	{
		boolean done = false;
		ISASCursor dbcur = null;

		try	{
			String sel = "SELECT * FROM rt_altro" +
						" WHERE n_cartella = " + n_cartella +
						" AND pr_data = " + formatDate(dbc, pr_data) +
						" AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllRtPap - 1 -- " + sel);
			dbcur = dbc.startCursor(sel);

			Vector vdbr = dbcur.getAllRecord();
	        if ((vdbr != null) && (vdbr.size() > 0)) {
	        	for(int i = 0; i < vdbr.size(); i++) {
	            	ISASRecord dbrec = (ISASRecord)vdbr.get(i);
	                if (dbrec != null) {
		                String sel2 = sel +
						" AND pr_num = " + dbrec.get("pr_num");

						ISASRecord dbrD = dbc.readRecord(sel2);
	                    dbc.deleteRecord(dbrD);
	                }
	            }
       		}

	        if (dbcur != null)
				dbcur.close();
			dbcur = null;

			String sel_1 = "SELECT * FROM rt_domdir" +
						" WHERE n_cartella = " + n_cartella +
						" AND pr_data = " + formatDate(dbc, pr_data) +
						" AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllRtPap - 2 -- " + sel_1);
			dbcur = dbc.startCursor(sel_1);

			Vector vdbr_1 = dbcur.getAllRecord();
	        if ((vdbr_1 != null) && (vdbr_1.size() > 0)) {
	        	for(int i = 0; i < vdbr_1.size(); i++) {
	            	ISASRecord dbrec = (ISASRecord)vdbr_1.get(i);
	                if (dbrec != null) {
		                String sel2 = sel_1 +
						" AND pr_num = " + dbrec.get("pr_num");

						ISASRecord dbrD = dbc.readRecord(sel2);
	                    dbc.deleteRecord(dbrD);
	                }
	            }
       		}

	        if (dbcur != null)
				dbcur.close();

			String sel_2 = "SELECT * FROM rt_pap" +
						" WHERE n_cartella = " + n_cartella +
						" AND pr_data = " + formatDate(dbc, pr_data) +
						" AND pr_progr = " + pr_progr;

			mySystemOut(" cancellaAllRtPap - 3 -- " + sel_2);
			ISASRecord dbr3 = dbc.readRecord(sel_2);
			if (dbr3 != null)
				dbc.deleteRecord(dbr3);

			done = true;
		}
		catch(Exception e)
		{
			System.out.println("RLSkPuacEJB: cancellaAllRtPap [" + e + "]");
			throw e;
		}
		finally {
			if (!done) {
				if (dbcur != null)
					dbcur.close();
			}
		}
	}

	// 29/12/09
	private void cancAssAnagra(ISASConnection dbc, Hashtable h0) throws Exception
	{
		ISASRecord recAssAna = getRecAssAna(dbc, h0);
		if (recAssAna != null) {
			// cancellazione agende collegate
			Integer progrAA = (Integer)recAssAna.get("progressivo");
			if (progrAA != null)
				deleteAgenda(dbc, progrAA.toString());

			dbc.deleteRecord(recAssAna);
		}
	}

	// 29/12/09
	private void pulisciAssAnagra(ISASConnection dbc, Hashtable h0) throws Exception
	{
		ISASRecord recAssAna = getRecAssAna(dbc, h0);
		if (recAssAna != null) {
			recAssAna.put("presa_carico", (String)"N");
			recAssAna.put("presa_carico_data", (java.sql.Date)null);
			recAssAna.put("presa_carico_motivo", (String)null);
			recAssAna.put("presa_carico_oper", (String)null);
			dbc.writeRecord(recAssAna);
		}
	}



	// 29/12/09
	private void chiudiCasoUVM(ISASConnection dbc, ISASRecord dbr) throws Exception
	{
		// devo chiudere anche il caso UVM
		DataWI dtOggi = new DataWI();
		Hashtable hDati = dbr.getHashtable();
		hDati.put("dt_conclusione", dtOggi.getFormattedString2(1));
		// 04/01/10 ----
		String motChiuFlux = getTabVociCodReg(dbc, "VALPCMCH", "1");
		hDati.put("motivo", motChiuFlux);
		// 04/01/10 ----
		hDati.put("operZonaConf", (String)dbr.get("pr_oper_ultmod"));
		mySystemOut("Dati per chiusura caso dovuta a cancellazione scheda PUAC: " + hDati.toString());
		gestore_casi.chiudiCaso(dbc, hDati);
	}

	// 30/12/09
	private void deleteAgenda(ISASConnection mydbc, String progr) throws Exception
	{
		boolean done = false;
		ISASCursor dbcur = null;
		ISASCursor dbcur_2 = null;

		try {
			// AGENDANT_PUA
			String mysel ="SELECT * FROM agendant_pua"+
				" WHERE (ag_cod_tipo_appu='COL' OR ag_cod_tipo_appu='VI1' OR ag_cod_tipo_appu='VI2')" +
				" AND ag_num_scheda="+progr;

			debugMessage("RLSkPuacEJB/deleteAgenda PUA - SELECT mysel: "+mysel);
			dbcur = mydbc.startCursor(mysel);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr_1 = (ISASRecord)dbcur.getRecord();
					String sel_2 = "SELECT * FROM agendant_pua"+
									" WHERE ag_cod_oper='"+((String)dbr_1.get("ag_cod_oper")).trim()+"'" +
									" AND ag_data_app="+formatDate(mydbc,""+dbr_1.get("ag_data_app"))+
									" AND ag_ora_app='"+(String)dbr_1.get("ag_ora_app")+"'";

					ISASRecord dbr_2 = mydbc.readRecord(sel_2);
					mydbc.deleteRecord(dbr_2);
				}
			}
			dbcur.close();
			dbcur = null;

			// AGENDANT_INTERV
			String mysel_1 ="SELECT * FROM agendant_interv"+
				" WHERE num_scheda_pua ="+progr;

			debugMessage("RLSkPuacEJB/deleteAgenda INTERV - SELECT mysel_1: "+mysel_1);
			dbcur = mydbc.startCursor(mysel_1);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr_3 = (ISASRecord)dbcur.getRecord();
					String sel_4 = "SELECT * FROM agendant_interv"+
									" WHERE ag_oper_ref='"+((String)dbr_3.get("ag_oper_ref")).trim()+"'" +
									" AND ag_data="+formatDate(mydbc,""+dbr_3.get("ag_data"))+
									" AND ag_progr="+(String)dbr_3.get("ag_progr");

					// AGENDANT_INTPRE ----
					String mysel_2 ="SELECT * FROM agendant_intpre"+
								" WHERE ap_oper_ref='"+((String)dbr_3.get("ag_oper_ref")).trim()+"'" +
								" AND ap_data="+formatDate(mydbc,""+dbr_3.get("ag_data"))+
								" AND ap_progr="+(String)dbr_3.get("ag_progr");

					dbcur_2 = mydbc.startCursor(mysel_2);
					if ((dbcur_2 != null) && (dbcur_2.getDimension() > 0)) {
						while (dbcur.next()) {
							ISASRecord dbr_5 = (ISASRecord)dbcur_2.getRecord();
							String sel_6 = "SELECT * FROM agendant_intpre"+
									" WHERE ap_oper_ref='"+((String)dbr_5.get("ap_oper_ref")).trim()+"'" +
									" AND ap_data="+formatDate(mydbc,""+dbr_5.get("ap_data"))+
									" AND ap_progr="+(String)dbr_5.get("ap_progr") +
									" AND ap_prest_cod = '" + (String)dbr_5.get("ap_prest_cod") + "'";

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
		} catch(Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una deleteAgenda() ");
		}
		finally {
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					if (dbcur_2 != null)
						dbcur_2.close();
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	// 04/01/10
	private String getTabVociCodReg(ISASConnection dbc, String tbCod, String tbVal) throws Exception
	{
		String codReg = "99";
		String sel = "SELECT tab_codreg FROM tab_voci" +
					" WHERE tab_cod = '" + tbCod + "'" +
					" AND tab_val = '" + tbVal + "'";

		ISASRecord dbr1 = dbc.readRecord(sel);
		if ((dbr1 != null) && (dbr1.get("tab_codreg") != null))
			codReg = (String)dbr1.get("tab_codreg");
		return codReg;
	}

	// 28/12/10: verifica esistenza di SkPuac aperte dopo quella in oggetto
	public Boolean query_checkLastSkPuac(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String)h0.get("n_cartella");
			String pr_progr = (String)h0.get("pr_progr");
			String dtSkPuac = (String)h0.get("pr_data_puac");
			
			String myselect = "SELECT * FROM rl_puauvm" +
				" WHERE n_cartella = " + cart +
				" AND pr_data_puac >= " + formatDate(dbc, dtSkPuac) +
				" AND pr_progr <> " + pr_progr;

			debugMessage("RLSkPuacEJB.query_checkLastSkPuac - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();
			
			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(!risu));
		} catch(Exception e1){
			System.out.println("RLSkPuacEJB.query_checkLastSkPuac - Eccezione=[" + e1 + "]");
			return (Boolean)null;
		} finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}	
	
	
}// END class

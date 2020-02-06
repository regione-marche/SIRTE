package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 30/05/2000 - EJB di connessione alla procedura SINS Tabella subzona
//
// paolo ciampolini
//
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import org.zkoss.util.resource.Labels;

import it.pisa.caribel.util.*; //gb 02/11/06
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.NumberDateFormat;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*; //gb 02/11/06
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.casi_adrsa.GestSegnalazione;
import it.caribel.app.sinssnt.bean.nuovi.ScaleVal;
import it.pisa.caribel.sinssnt.connection.*;

public class SocAssProgettoEJB extends SINSSNTConnectionEJB
{
	// 06/06/07 m.: aggiunto "catch" e "throw" dell'eccezione ISASPermissionDenied
	//	anche sui metodi "queryProgettoCorrente()", "queryKey()".
	//	Nel metodo "query()": modificato "SELECT campo1, campo2,.." in "SELECT *"
	//	perche' vengano eseguiti i ctrl ISAS.

	// 26/10/06 m.: in seguito all'introduzione della scheda valutazione:
	// modificato metodi "queryProgettoCorrente()" e "query()" x aggiunta
	// 	join fra tabelle ASS_PROGETTO e PROGETTO_CONT;
	// + modificato metodo "insert()" x aggiunta scrittura su tabella PROGETTO_CONT e,
	// 	nel caso di non obbligatoriet� della preesistenza della scheda valutazione
	// 	(per es. reg toscana), scrittura su tabella PROGETTO con pr_data=ap_data_apertura;
	// + modificato metodo "delete()" x aggiunta cancellazione da tabella PROGETTO_CONT.

	private static final int CONST_CANC_PROGETTO_CONSENTITA = 0;
	private static final int CONST_EXIST_DIAGNOSI           = 1;
	private static final int CONST_EXIST_OBIETTIVI          = 2;
	private static final int CONST_EXIST_SEGNALAZIONI       = 3;
	private static final int CONST_EXIST_VERIFICHE          = 4;

	// 06/06/07
	private	String msgNoD = "Mancano i diritti per leggere il record";

	// Gestione flusso segnalazioni per la regione Toscana
	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();

	// 21/05/09 m.
	private ScaleVal gest_scaleVal = new ScaleVal();
	private boolean myDebug = true;
	private String nomeEJB = "SocAssProgettoEJB";

	public SocAssProgettoEJB() {}

	private void decodificaAssOperatore(ISASConnection mydbc, ISASRecord mydbr, String strNAssistito) throws Exception
	{
		String strCognome = "";
		String strNome = "";

		String selS = "SELECT cognome, nome FROM cartella" +
		" WHERE n_cartella = " + strNAssistito;

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
		{
			strCognome = (String)rec.get("cognome");
			strNome = (String)rec.get("nome");
		}

		mydbr.put("assCognome", strCognome);
		mydbr.put("assNome", strNome);
	}// END decodificaAssOperatore

	private void decodificaOperatore(ISASConnection mydbc, ISASRecord mydbr,
			String dbFldName, String dbName1, String dbName2) throws Exception
	{
		String strCodOperatore = (String) mydbr.get(dbFldName);
		String strCognome = "";
		String strNome= "";

		if (strCodOperatore == null)
		{
			mydbr.put(dbName1, "");
			mydbr.put(dbName2, "");
			return;
		}

		String selS = "SELECT cognome, nome FROM operatori " +
					" WHERE codice = '" + strCodOperatore + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
		{
			strCognome = (String)rec.get("cognome");
			strNome = (String)rec.get("nome");
		}

		mydbr.put(dbName1, strCognome);
		mydbr.put(dbName2, strNome);
	}// END decodificaOperatore

	private void decodificaComune(ISASConnection mydbc, ISASRecord mydbr,
			String dbFldName, String dbName) throws Exception
	{
		String strCodComune = (String) mydbr.get(dbFldName);
		String strDescrComune = "";

		if (strCodComune == null)
		{
			mydbr.put(dbName, "");
			return;
		}

		String selS = "SELECT descrizione FROM comuni" +
					" WHERE codice = '" + strCodComune + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
			strDescrComune = (String)rec.get("descrizione");

		mydbr.put(dbName, strDescrComune);
	}// END decodificaComune

	public ISASRecord queryProgettoCorrente(myLogin mylogin,Hashtable h) throws SQLException, ISASPermissionDeniedException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil(); //gb 05/11/08

		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String)h.get("pr_data");// 26/10/06

		try{
			if (strDtSkVal == null)
			{
				System.out.println("\nSocAssProgetto -->> queryProgettoCorrente: dataSkVal NULLA!!");
				done = true;
				return dbr;
			}

			dbc = super.logIn(mylogin);

			Hashtable htRegUsl= getHtRegUsl(dbc); //gb 05/11/08

			String myselect = "SELECT *" +
							" FROM ass_progetto" +
							" WHERE n_cartella = " + strNAssistito +
							" AND ap_data_chiusura IS NULL";

			mySystemOut("queryProgettoCorr SocAssProgettoEJB : " + myselect);
			dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				decodificaAssOperatore(dbc, dbr, strNAssistito);
				decodificaOperatore(dbc, dbr, "ap_oper_ap", "apeOpeCognome", "apeOpeNome");
				decodificaOperatore(dbc, dbr, "ap_oper_ch", "chiOpeCognome", "chiOpeNome");
				decodificaOperatore(dbc, dbr, "ap_ass_ref", "assSocRespOpeCognome", "assSocRespOpeNome");
				decodificaComune(dbc, dbr, "ap_ric_com_nasc", "ricComuneNascDescr");

				dbr.put("pr_data", strDtSkVal);// 26/10/06
				//gb 13/11/07: si carica un campo nascosto del client con lo stesso
				// valore di una combo dipendente e poi, nel client, si setta il valore
				// della combo al valore del campo nascosto
				dbr.put("ap_tipo_utente_hide",(String)dbr.get("ap_tipo_utente"));

				// 01/02/08
				boolean esisteInterv = query_esisteInterventi(dbc, strNAssistito, "" + dbr.get("n_progetto"));
				dbr.put("esiste_interv", (String)(esisteInterv?"S":"N"));

				//gb 05/11/08
				String strCodPresOp= (String)util.getObjectField(dbr,"ap_ass_ref_presidio",'S');
				if((strCodPresOp!=null) && !strCodPresOp.equals(""))
				{
					String strPresOp = getSedeOperatore(dbc, dbr, "ap_ass_ref_presidio", htRegUsl);
					dbr.put("ap_ass_ref_pres_descr", strPresOp);
				}

				// 24/12/08 ---
				ISASRecord dbrAssAna = getRecAssAna(dbc, (Hashtable)dbr.getHashtable());
				if ((dbrAssAna != null) && (dbrAssAna.get("progressivo") != null))
					dbr.put("ass_angr_progressivo", dbrAssAna.get("progressivo"));
				// 24/12/08 ---

				// 20/05/09 Elisa Croci
				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SOC))
				{
					// 15/06/09 Elisa Croci    ********************************************************
					if(h.containsKey("ubicazione") && h.get("ubicazione") != null)
						dbr.put("ubicazione", h.get("ubicazione"));
					if(h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
						dbr.put("update_segnalazione", h.get("update_segnalazione"));
					// *********************************************************************************

					int caso = prendi_dati_caso(dbc,dbr);
					prendi_segnalazione(dbc,caso,dbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("SocAssProgettoEJB.queryProgettoCorrente(): "+e);
			throw new CariException(msgNoD, -2);
		}
		catch(Exception e)
		{
			System.out.println("SocAssProgettoEJB.queryProgettoCorrente(): "+e);
			throw new SQLException("Errore eseguendo la queryProgettoCorrente()");
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1) {	System.out.println(e1);		}
			}
		}
	}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException, ISASPermissionDeniedException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");
		String strDtSkVal = (String)h.get("pr_data");// 26/10/06


		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil(); //gb 05/11/08

		try{
			// Ottengo la connessione al database
			dbc = super.logIn(mylogin);

			Hashtable htRegUsl= getHtRegUsl(dbc); //gb 05/11/08

			// Preparo la SELECT del record
			String myselect = "SELECT * FROM ass_progetto" +
							" WHERE n_cartella = " + strNAssistito +
							" AND n_progetto = " + strNProgetto;

			mySystemOut("queryKey SocAssProgettoEJB : " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);

			// Si decodificano alcuni campi della maschera a video.
			if (dbr != null)
			{
				decodificaAssOperatore(dbc, dbr, strNAssistito);
				decodificaOperatore(dbc, dbr, "ap_oper_ap", "apeOpeCognome", "apeOpeNome");
				decodificaOperatore(dbc, dbr, "ap_oper_ch", "chiOpeCognome", "chiOpeNome");
				decodificaOperatore(dbc, dbr, "ap_ass_ref", "assSocRespOpeCognome", "assSocRespOpeNome");
				decodificaComune(dbc, dbr, "ap_ric_com_nasc", "ricComuneNascDescr");

				dbr.put("pr_data", strDtSkVal);// 26/10/06
				//gb 13/11/07: si carica un campo nascosto del client con lo stesso
				// valore di una combo dipendente e poi, nel client, si setta il valore
				// della combo al valore del campo nascosto
				dbr.put("ap_tipo_utente_hide",(String)dbr.get("ap_tipo_utente"));


				// 01/02/08
				boolean esisteInterv = query_esisteInterventi(dbc, strNAssistito, strNProgetto);
				dbr.put("esiste_interv", (esisteInterv?"S":"N"));

				//gb 05/11/08
				String strCodPresOp= (String)util.getObjectField(dbr,"ap_ass_ref_presidio",'S');
				if((strCodPresOp!=null) && !strCodPresOp.equals(""))
				{
					String strPresOp = getSedeOperatore(dbc, dbr, "ap_ass_ref_presidio", htRegUsl);
					dbr.put("ap_ass_ref_pres_descr", strPresOp);
				}

				// 24/12/08 ---
				ISASRecord dbrAssAna = getRecAssAna(dbc, (Hashtable)dbr.getHashtable());
				if ((dbrAssAna != null) && (dbrAssAna.get("progressivo") != null))
					dbr.put("ass_angr_progressivo", dbrAssAna.get("progressivo"));
				// 24/12/08 ---

				// 20/05/09 Elisa Croci
				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SOC))
				{
					// 15/06/09 Elisa Croci    ********************************************************
					if(h.containsKey("ubicazione") && h.get("ubicazione") != null)
						dbr.put("ubicazione", h.get("ubicazione"));
					if(h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
						dbr.put("update_segnalazione", h.get("update_segnalazione"));
					// *********************************************************************************

					int caso = prendi_dati_caso(dbc,dbr);
					prendi_segnalazione(dbc,caso,dbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("SocAssProgettoEJB.queryKey(): "+e);
			throw new CariException(msgNoD, -2);
		}
		catch(Exception e)
		{
			System.out.println("SocAssProgettoEJB.query_key(): "+e);
			throw new SQLException("Errore eseguendo una queryKey()");
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1) {	System.out.println(e1);	}
			}
		}
	}

	public String duplicateChar(String s, String c)
	{
		if ((s == null) || (c == null)) return s;
		String mys = new String(s);
		int p = 0;

		while (true)
		{
			int q = mys.indexOf(c, p);
			if (q < 0) return mys;
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	private void decodificaQueryOperatore(ISASConnection mydbc, ISASRecord dbr,
			String dbFldNameCod, String dbName) throws Exception
	{
		String strCodOperatore = (String) dbr.get(dbFldNameCod);

		String strCognome = "";
		String strNome = "";

		if (strCodOperatore == null)
		{
			dbr.put(dbName, "");
			return;
		}

		String selS = "SELECT cognome, nome FROM operatori" +
				" WHERE codice = '" + strCodOperatore + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
		{
			strCognome = (String)rec.get("cognome");
			strNome = (String)rec.get("nome");
		}

		dbr.put(dbName, strCognome + " " + strNome);
	}

	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr) throws Exception
	{
		for (int i=0; i<vdbr.size(); i++)
		{
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			decodificaQueryOperatore(mydbc, dbr, "ap_oper_ap", "operatore_apertura");
			decodificaQueryOperatore(mydbc, dbr, "ap_oper_ch", "operatore_chiusura");
		}
	}



	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException, ISASPermissionDeniedException
	{
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String)h.get("pr_data");// 26/10/06

		Vector vdbr = new Vector();

		try
		{
			if (strDtSkVal == null)
			{
				System.out.println("\nSocAssProgetto -->> query: dataSkVal NULLA!!");
				done = true;
				return vdbr;
			}

			dbc=super.logIn(mylogin);

			String myselect = "SELECT ap.*" +
							" FROM ass_progetto ap," +
							" progetto_cont pc" + // 26/10/06
							" WHERE ap.n_cartella = " + strNAssistito +
							" AND ap.ap_data_chiusura IS NOT NULL" +
							// 26/10/06 : x estrarre solo quelli collegati ad una scheda valutaz
							" AND pc.prc_tipo_op = '01'" +
							" AND pc.n_cartella = ap.n_cartella" +
							" AND pc.pr_data = " + formatDate(dbc, strDtSkVal) +
							" AND pc.prc_n_contatto = ap.n_progetto" +
							// 26/10/06 --------------------------------------------------------
							" ORDER BY ap.ap_data_apertura, ap.ap_data_chiusura";

			mySystemOut("\nGB-->>query GridAssProgetto: "+myselect);

			ISASCursor dbcur=dbc.startCursor(myselect);
			vdbr=dbcur.getAllRecord();

			// Decodifica dei Cognomi e Nomi degli operatori e della descrizione
			// del grado di importanza in tutti gli ISASRecord del Vector
			decodificaQueryInfo(dbc, vdbr);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la query()  ");
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	private void decodificaMotivo(ISASConnection mydbc, ISASRecord dbr,	String dbFldNameCod, String dbName) throws Exception
	{
		String strCodMotivo = (String) dbr.get(dbFldNameCod);

		String strMotivoDecod = "";

		if (strCodMotivo == null)
		{
			dbr.put(dbName, "");
			return;
		}

		String selS = "SELECT descrizione FROM motivo" +
					" WHERE codice = '" + strCodMotivo + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
			strMotivoDecod = (String)rec.get("descrizione");

		dbr.put(dbName, strMotivoDecod);
	}

	private void decodificaProblema(ISASConnection mydbc, ISASRecord dbr, String dbFldNameCod, String dbName) throws Exception
	{
		String strCodProblema = (String) dbr.get(dbFldNameCod);

		String strProblemaDecod = "";

		if (strCodProblema == null)
		{
			dbr.put(dbName, "");
			return;
		}

		String selS = "SELECT descrizione FROM problema" +
					" WHERE codice = '" + strCodProblema + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
		{
			strProblemaDecod = (String)rec.get("descrizione");
		}
		dbr.put(dbName, strProblemaDecod);
	}

	// 14/11/07: aggiunto lettura indirizzo e comune domicilio e riunificato 4 select su cartella
	// 05/10/07: aggiunto lettura data e motivo chiusura cartella
	private void decodificaCartellaCognomeNome(ISASConnection mydbc, ISASRecord dbr, Hashtable htSesso) throws Exception
	{
		String strNCartella = ((Integer) dbr.get("n_cartella")).toString();
		String strCognome = "";
		String strNome = "";
		String strCodSesso = "";
		java.sql.Date dateDataNascita = null;
		String strCodComuneNascita = "";
		String strDescrComuneNascita = "";

		// 05/10/07 -----
		String mot_chius = "";
		java.sql.Date dtChius = null;
		// 05/10/07 -----

		// 14/11/07 ---
		String strCodComuneDom = "";
		String strDescrComuneDom = "";
		String strIndirizzoDom = "";
		// 14/11/07 ---

		// 08/10/0
		String codFisc = "";

		if (strNCartella.equals(""))
		{
			dbr.put("cognome", "");
			dbr.put("nome", "");
			dbr.put("sesso_decod", "");
			dbr.put("data_nascita", "");
			dbr.put("cod_comune_nascita", "");
			dbr.put("comune_nascita_decod", "");

			// 05/10/07 -----
			dbr.put("motivo_chiusura", "");
			dbr.put("data_chiusura", "");
			// 05/10/07 -----

			// 14/11/07 ---
			dbr.put("dom_citta", "");
			dbr.put("desc_dom_citta", "");
			dbr.put("dom_indiriz", "");
			// 14/11/07 ---

			// 08/10/08
			dbr.put("cod_fisc", "");

			return;
		}

		String selS = "SELECT c.cognome, c.nome," +
		" c.sesso," +
		" c.data_nasc," +
		" c.cod_com_nasc," +
		" c.motivo_chiusura, c.data_chiusura," + // 05/10/07
		" ac.dom_citta, ac.dom_indiriz," + // 14/11/07
		" c.cod_fisc" + // 08/10/08
		" FROM cartella c," +
		" anagra_c ac" + // 14/11/07
		" WHERE c.n_cartella = " + strNCartella +
		" AND ac.n_cartella = c.n_cartella" + // 14/11/07
		" AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
		" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
		{
			strCognome = ((String)rec.get("cognome")).trim();
			strNome = ((String)rec.get("nome")).trim();
			if (rec.get("sesso") != null)
				strCodSesso = ((String)rec.get("sesso")).trim();
			if (rec.get("data_nasc") != null)
				dateDataNascita = (java.sql.Date)rec.get("data_nasc");
			if (rec.get("cod_com_nasc") != null)
				strCodComuneNascita = (String) rec.get("cod_com_nasc");
			// 05/10/07 -----
			if (rec.get("motivo_chiusura") != null)
				mot_chius = "" + rec.get("motivo_chiusura");
			if (rec.get("data_chiusura") != null)
				dtChius = (java.sql.Date)rec.get("data_chiusura");
			// 05/10/07 -----
			// 14/11/07 ---
			if (rec.get("dom_citta") != null)
				strCodComuneDom = (String) rec.get("dom_citta");
			if (rec.get("dom_indiriz") != null)
				strIndirizzoDom = (String) rec.get("dom_indiriz");
			// 14/11/07 ---

			// 08/10/08
			if (rec.get("cod_fisc") != null)
				codFisc = (String) rec.get("cod_fisc");
		}

		dbr.put("cognome", strCognome);
		dbr.put("nome", strNome);
		dbr.put("sesso", strCodSesso);
		dbr.put("sesso_decod", (htSesso.get(strCodSesso)!=null?(String)htSesso.get(strCodSesso):""));
		dbr.put("data_nascita", (java.sql.Date)dateDataNascita);
		dbr.put("cod_comune_nascita", strCodComuneNascita);
		decodificaComune(mydbc, dbr, "cod_comune_nascita", "comune_nascita_decod");
		// 05/10/07 -----
		dbr.put("motivo_chiusura", mot_chius);
		dbr.put("data_chiusura", dtChius);
		// 05/10/07 -----

		// 14/11/07 -----
		dbr.put("dom_citta", strCodComuneDom);
		decodificaComune(mydbc, dbr, "dom_citta", "desc_dom_citta");
		dbr.put("dom_indiriz", strIndirizzoDom);
		// 14/11/07 -----

		// 08/10/08
		dbr.put("cod_fisc", codFisc);
	}

	private void decodificaQueryProgettiApertiInfo(ISASConnection mydbc, Vector vdbr) throws Exception
	{
		Hashtable htSesso = new Hashtable();
		htSesso.put("F","Femmina");
		htSesso.put("M","Maschio");

		it.pisa.caribel.util.ISASUtil utl = new it.pisa.caribel.util.ISASUtil();

		for (int i=0; i<vdbr.size(); i++)
		{
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			decodificaMotivo(mydbc, dbr, "ap_motivo", "motivo_decod");
			decodificaProblema(mydbc, dbr, "ap_problema", "problema_decod");
			decodificaCartellaCognomeNome(mydbc, dbr, htSesso);
			// 31/01/07
			dbr.put("desc_val_ap", (String)decodificaTabVoci(mydbc, (String)dbr.get("pr_motivo_val_ap"), "PRMOAP"));
			dbr.put("desc_val_ch", (String)decodificaTabVoci(mydbc, (String)dbr.get("pr_motivi_val_ch"), "PRMOCH"));
			// 06/12/07
			dbr.put("tipo_utente_decod", (String)utl.getDecode(mydbc, "tipute", "codice", (String)dbr.get("ap_tipo_utente"), "descrizione"));
		}
	}

	// 23/01/09 m.: eliminato cntrl su ASS_OPABIL
	// 31/01/07 m.: aggiunto lettura dataProg, dataChiusProg, motivo apertura e chiusura
	// da tabella PROGETTO (N.B.: si deve passare attraverso la join su PROGETTO_CONT).
	public Vector query_progettiAperti(myLogin mylogin, Hashtable h) throws  SQLException, ISASPermissionDeniedException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		String strDataDa = (String) h.get("da_data");
		String strDataA = (String) h.get("a_data");
		String strOperLoggato = (String) h.get("operatore_loggato");

		try{
			// Connessione al database
			dbc=super.logIn(mylogin);

			// Compongo la SELECT
			String myselect = "SELECT ap.*," +
			// 31/01/07 -----
			" p.pr_data," +
			" p.pr_data_chiusura," +
			" p.pr_motivo_val_ap," +
			" p.pr_motivi_val_ch," +
			// 31/01/07 -----
			" p.pr_data_carico" + // 11/10/07
			" FROM ass_progetto ap," +
			// 23/01/09					" ass_opabil ao," +
			// 31/01/07 -----
			" progetto p," +
			" progetto_cont pc" +
			// 31/01/07 -----
			/** 23/01/09
                      " WHERE ap.n_cartella = ao.n_cartella" +
                      " AND ap.n_progetto = ao.n_progetto" +
                      " AND ao.opabil_cod = '" + strOperLoggato + "'" +
			 **/
			/*** 14/04/08
                      " AND ap.ap_data_apertura <= " + formatDate(dbc, strDataA) +
                      " AND (ap.ap_data_chiusura IS NULL OR ap.ap_data_chiusura >= " + formatDate(dbc, strDataDa) + ")" +
			 ***/
			// 14/04/08: selezione solo dei progetti aperti nel periodo (x ridurre il num dei rec estratti) ----
			" WHERE ap.ap_data_apertura <= " + formatDate(dbc, strDataA) +
			" AND ap.ap_data_apertura >= " + formatDate(dbc, strDataDa) +
			// 14/04/08 ----------------
			// 31/01/07 -----
			" AND pc.prc_tipo_op = '01'" +
			" AND pc.n_cartella = ap.n_cartella" +
			" AND pc.prc_n_contatto = ap.n_progetto" +
			" AND p.n_cartella = pc.n_cartella" +
			" AND p.pr_data = pc.pr_data" +
			// 31/01/07 -----
			" ORDER BY ap.ap_data_apertura";

			mySystemOut("query_progettiAperti: "+myselect);

			// Leggo i record
			dbcur = dbc.startCursor(myselect, 200); // 14/04/08

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();

			// Decodifica del codice motivo e codice problema in tutti gli ISASRecord del Vector
			decodificaQueryProgettiApertiInfo(dbc, vdbr);
			mySystemOut("query_progettiAperti: Decodificati i campi degli ISASRecord del vettore");

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}
		catch(Exception e)  {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la query_progettiAperti()  ");
		}
		finally  {
			if(!done)
			{
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1)
				{System.out.println(e1);}
			}
		}
	}

	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException, ISASPermissionDeniedException {
		boolean done=false;
		ISASConnection dbc=null;
		String scr=" ";
		try{
			// Connessione al database
			dbc=super.logIn(mylogin);

			// Compongo la SELECT
			String myselect="SELECT * FROM ass_progetto";
			//controllo valore corretto di SEGN_DESCR
			scr=(String)(h.get("ap_motivo"));
			if (!(scr==null))
				if (!(scr.equals(" ")))
				{
					scr=duplicateChar(scr,"'");
					myselect=myselect+" WHERE ap_motivo like '"+scr+"%'";
				}

			myselect=myselect+" ORDER BY ap_motivo ";
			mySystemOut("queryPaginate() GridAssProgetto: "+myselect);

			// Leggo i record
			ISASCursor dbcur=dbc.startCursor(myselect);

			//Vector vdbr=dbcur.getAllRecord();
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			// Spacchetto un gruppo di record dentro in un vector
			Vector vdbr = dbcur.paginate(start, stop);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la queryPaginate()  ");
		}
		finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	private int getProgressivo(ISASConnection mydbc, String strNAssistito) throws Exception
	{
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String myselect="SELECT MAX(n_progetto) max_n_progetto" +
		" FROM ass_progetto" +
		" WHERE n_cartella = " + strNAssistito ;
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
		{
			intProgressivo = u.getIntField(dbr, "max_n_progetto");
		}
		intProgressivo++;
		return intProgressivo;
	}

	//gb 07/11/06 *****************************************************************
	private boolean dtApeContLEMaxDtContChius(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String strDataApeContatto = (String) h.get("ap_data_apertura");

		String mySel = "SELECT ap_data_chiusura" +
		" FROM ass_progetto" +
		" WHERE n_cartella = " + strNCartella +
		" AND ap_data_chiusura >= " + formatDate(dbc, strDataApeContatto) +
		" AND ap_data_chiusura IS NOT NULL";

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}
	// ****************************************************************************

	//gb 05/11/08
	private Hashtable getHtRegUsl(ISASConnection dbc)
	throws Exception
	{
		Hashtable ht = new Hashtable();
		String codRegione = "";
		String codUsl = "";
		ISASRecord dbr = null;
		try {
			String strSqlQuery = "SELECT conf_key, conf_txt" +
			" FROM conf" +
			" WHERE conf_kproc = 'SINS'" +
			" AND conf_key IN ('codice_regione', 'codice_usl')";
			mySystemOut("getHtRegUsl/strSqlQuery: "+strSqlQuery);
			ISASCursor dbcur = dbc.startCursor(strSqlQuery);
			if ((dbcur != null) && dbcur.getDimension()>0)
			{
				while (dbcur.next())
				{
					dbr = (ISASRecord) dbcur.getRecord();
					if (dbr != null)
					{
						String strConfKey = (String) dbr.get("conf_key");
						String strConfTxt = (String) dbr.get("conf_txt");
						if ((strConfKey != null) && strConfKey.equals("codice_regione"))
							codRegione = strConfTxt;
						else if ((strConfKey != null) && strConfKey.equals("codice_usl"))
							codUsl = strConfTxt;
					}
				}
			}
			ht.put("codice_regione", codRegione);
			ht.put("codice_usl", codUsl);
			return ht;
		} catch(Exception e) {
			System.out.println("getHtRegUsl: "+e);
			throw new Exception("Errore eseguendo una getHtRegUsl()  "+ e);
		}
	}

	private String getSedeOperatore(ISASConnection dbc, ISASRecord dbr, String strDbFldName,
			Hashtable htRegUsl)
	throws Exception
	{
		String strCodRegione = "";
		String strCodAzSan = "";
		try{
			strCodRegione = (String) htRegUsl.get("codice_regione");
			strCodAzSan = (String) htRegUsl.get("codice_usl");
			String strResult = "";
			String strCodPresidio = (String) dbr.get(strDbFldName);
			if ((strCodPresidio == null) || strCodPresidio.trim().equals(""))
				return "";
			String strQuery = "SELECT despres" +
			" FROM presidi" +
			" WHERE codpres = '" + strCodPresidio + "'" +
			" AND codreg = '" + strCodRegione + "'" +
			" AND codazsan = '" + strCodAzSan + "'";
			mySystemOut("getSedeOperatore/strQuery(2): " + strQuery);
			ISASRecord dbrPrs = dbc.readRecord(strQuery);
			if ((dbrPrs != null) && (dbrPrs.get("despres") != null))
				strResult = (String) dbrPrs.get("despres");
			return strResult;
		} catch(Exception e) {
			debugMessage("SocAssProgettoEJB: Exception in getSedeOperatore: "+e);
			throw new Exception("Errore eseguendo una getSedeOperatore()  "+ e);
		}
	}

	// Inserimento su tabelle ASS_PROGETTO, ASS_OPABIL, ASS_OPREF, PROGETTO_CONT ed, eventualmente, PROGETTO
	public ISASRecord insert(myLogin mylogin, Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
	CariException
	{
		boolean done=false;
		ISASConnection dbc=null;


		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String)h.get("pr_data");// 26/10/06

		if ((strNAssistito == null) || (strNAssistito.equals("")))
			throw new SQLException("Errore: manca il campo 'Assistito' (n. cartella) in chiave");


		try{
			dbc=super.logIn(mylogin);

			// Inizio la TRANSAZIONE
			dbc.startTransaction();

			// 21/12/11 m ---
			ISASRecord dbr = insertNew(dbc, h, strNAssistito, strDtSkVal);
			gestSegnalNew(dbc, dbr, h);
			// 21/12/11 m ---
			
			// Concludo la TRANSAZIONE
			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		//gb 02/11/06 **************
		catch(CariException ce){
			ce.setISASRecord(null);
			try{
				System.out.println("SocAssProgettoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}
		// *************************
		catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				System.out.println("SocAssProgettoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo la rollback() - " +  e1);
			}
			throw e;
		}
		catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				System.out.println("SocAssProgettoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo la rollback() - "+  e1);
			}
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
			try{
				System.out.println("SocAssProgettoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo la rollback() - " +  e1);
			}
			throw new SQLException("Errore eseguendo la insert() - "+  e);
		}
		finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	// 21/12/11 m
	public ISASRecord insertNew(ISASConnection dbc, Hashtable h, String strNAssistito, String strDtSkVal) throws Exception
	{
		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil(); //gb 05/11/08

		//Per la data di sistema
		NumberDateFormat ndf = new NumberDateFormat();
		
		Hashtable htRegUsl= getHtRegUsl(dbc); //gb 05/11/08

		int intProgressivo = getProgressivo(dbc, strNAssistito);
		Integer iProgressivo = new Integer(intProgressivo);
		String strNProgetto = iProgressivo.toString();

		mySystemOut("\nGB -->> SocAssProgettoEJB.insert: INIZIO.");

		//Controllo dati in input
		mySystemOut("\nGB Dati Input Insert -->> " + h.toString() + "\n");

		// Leggo la data di sistema
		String strDataSistema = ndf.getJdbcSystemDate();
		mySystemOut("\nGB Data di Sistema -->> " + strDataSistema + "\n");
		//  Setto la data di apertura nel caso non sia stata settata
		if(h.get("ap_data_apertura") == null)
			h.put("ap_data_apertura", strDataSistema);

		//gb 07/11/06 *************************
		if(dtApeContLEMaxDtContChius(dbc, h))
		{
			String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
			throw new CariException(msg, -2);
		}
		// ************************************

		// Si crea un template di nuovo record
		ISASRecord dbr=dbc.newRecord("ass_progetto");
		// Si riempie il nuovo record coi valori dei campi
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		//gb 05/11/08
		String strCodOper = (String) h.get("ap_ass_ref");
		String strCodPresidio = util.getDecode(dbc, "operatori", "codice", strCodOper,
				"cod_presidio", "");
		dbr.put("ap_ass_ref_presidio",strCodPresidio);
		//gb 05/11/08: fine

		// 24/12/08: x inserimento diretto da "RicercaAssistito" ----
		String proven = (String)h.get("provenienza");
		// se provengo da elecoCasi, invece, il record su ASS_ANAGRAFICA � gi� stato aggiornato alla presa in carico
		if (!"ELECASI".equals(proven)) {
			ISASRecord dbrAssAna = getRecAssAna(dbc, h);
			if (dbrAssAna != null)
				aggiornaAssAna(dbc, dbrAssAna, h);
		}
		// 24/12/08 ----

		// Si setta il campo 'n_progetto' col nuovo progressivo
		dbr.put("n_progetto",iProgressivo);

		// Si scrive il nuovo record
		dbc.writeRecord(dbr);

		// 26/10/06: scrittura su PROGETTO_CONT ed, eventualmente, PROGETTO -----
		if ((strDtSkVal == null) || ((strDtSkVal != null) && (strDtSkVal.trim().equals("")))) {
			strDtSkVal = (String)h.get("ap_data_apertura");
			//gb 02/11/06
			// Mettere controllo che data_ape sk_valutaz. fittizia sia >=
			// data chiusura di ultima sk_valutaz. chiusa pre-esistente.
			if (dtApeMinoreMaxDtChius(dbc, strNAssistito, strDtSkVal))
			{
				String msg = "Attenzione: Data apertura antecedente a data chiusura di ultima Scheda valutazione chiusa!";
				throw new CariException(msg, -2);
			}
			//gb 01/06/07: Controllo che la data di apertura del progetto (ap_data_apertura)
			//		sia >= data_apetura della tab. cartella.
			if (dtApeProgettoLTDtApeCartella(dbc, strNAssistito, strDtSkVal))
			{
				String msg = "Attenzione: Data apertura progetto e' antecedente alla data apertura dell'assistito!";
				throw new CariException(msg, -2);
			}
			scriviProgetto(dbc, strNAssistito, strDtSkVal);
		}
//		scriviProgettoCont(dbc, strNAssistito, strDtSkVal, "01", strNProgetto);


		// 26/10/06 -------------------------------------------------------------

		// Si crea la query per rileggere il record appena inserito
		String myselect="SELECT * FROM ass_progetto " +
		" WHERE n_cartella = " + strNAssistito + " " +
		" AND n_progetto = " + strNProgetto;
		// Si legge il record appena inserito
		dbr=dbc.readRecord(myselect);

		// Si decodificano i alcuni campi della maschera a video.
		if (dbr != null)
		{
			dbr.put("pr_data", strDtSkVal);
			decodificaAssOperatore(dbc, dbr, strNAssistito);
			//GB decodificaProgDescrizione(dbc, dbr, strNAssistito, strNProgetto);
			decodificaOperatore(dbc, dbr, "ap_oper_ap", "apeOpeCognome", "apeOpeNome");
			decodificaOperatore(dbc, dbr, "ap_oper_ch", "chiOpeCognome", "chiOpeNome");
			decodificaOperatore(dbc, dbr, "ap_ass_ref", "assSocRespOpeCognome", "assSocRespOpeNome");
			decodificaComune(dbc, dbr, "ap_ric_com_nasc", "ricComuneNascDescr");

			//gb 13/11/07: si carica un campo nascosto del client con lo stesso
			// valore di una combo dipendente e poi, nel client, si setta il valore
			// della combo al valore del campo nascosto
			dbr.put("ap_tipo_utente_hide",(String)dbr.get("ap_tipo_utente"));

			// 01/02/08
			dbr.put("esiste_interv", "N");
			//gb 05/11/08
			String strCodPresOp= (String)util.getObjectField(dbr,"ap_ass_ref_presidio",'S');
			if((strCodPresOp!=null) && !strCodPresOp.equals(""))
			{
				String strPresOp = getSedeOperatore(dbc, dbr, "ap_ass_ref_presidio", htRegUsl);
				dbr.put("ap_ass_ref_pres_descr", strPresOp);
			}

			// 21/05/09 m. ------------------
			// lettura dtConclusione CASO precedente
			h.put("pr_data", strDtSkVal);
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String)h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, strNAssistito, strDtSkVal,
									"", dtChiusCasoPrec, "", tempoT, "01");
			dbr.put("tempo_t", tempoT);
			// 21/05/09 m. ------------------



			// 15/06/09 Elisa Croci  ***************************************************************
			if(h.containsKey("ubicazione") && dbr != null)
				dbr.put("ubicazione", h.get("ubicazione"));
			if(h.containsKey("update_segnalazione") && dbr != null)
				dbr.put("update_segnalazione", h.get("update_segnalazione"));
			// *************************************************************************************
		}

		// Si crea un template di nuovo record ass_opabil
		ISASRecord dbrOpabil=dbc.newRecord("ass_opabil");
		mySystemOut("\nGB -->> SocAssProgettoEJB.insert: Inizio caricamento Rec. ass_opabil.");
		dbrOpabil.put("n_cartella",h.get("n_cartella"));
		dbrOpabil.put("n_progetto",iProgressivo);
		dbrOpabil.put("opabil_cod",h.get("ap_ass_ref"));
		dbrOpabil.put("opabil_liv","3");
		// Si scrive il nuovo record ass_opabil
		dbc.writeRecord(dbrOpabil);
		mySystemOut("\nGB -->> SocAssProgettoEJB.insert: Inserito Rec. ass_opabil.");

		// Si crea un template di nuovo record ass_opref
		ISASRecord dbrOpref=dbc.newRecord("ass_opref");
		mySystemOut("\nGB -->> SocAssProgettoEJB.insert: Inizio caricamento Rec. ass_opref.");
		dbrOpref.put("n_cartella",h.get("n_cartella"));
		dbrOpref.put("n_progetto",iProgressivo);
		dbrOpref.put("opref_cod",h.get("ap_ass_ref"));
		dbrOpref.put("opref_da",h.get("ap_ass_ref_da"));
		dbrOpref.put("opref_presidio", strCodPresidio); // 06/02/09
		// Si scrive il nuovo record ass_opref
		dbc.writeRecord(dbrOpref);
		mySystemOut("\nGB -->> SocAssProgettoEJB.insert: Inserito Rec. ass_opref.");	
		
		return dbr;
	}
	
	
	public void gestSegnalNew(ISASConnection dbc, ISASRecord dbr, Hashtable h) throws Exception
	{
		String proven = (String)h.get("provenienza");
		// 21/05/09 Elisa Croci
		if ((gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SOC))
		&& (!"ELECASI".equals(proven))) // 13/05/10: se non provengo da "ElencoCasi"
		{
			int idCaso = prendi_dati_caso(dbc, dbr);
			// if(idCaso != -1) Elisa Croci 26/06/09
				gestione_segnalazione(dbc, dbr, h, "insert");

			mySystemOut("gestSegnalNew() - Fatta gestione segnalazione per il caso: " + idCaso);

			String data_chiusura= "";
			if (dbr.get("ap_data_chiusura") != null)
				data_chiusura=((java.sql.Date)dbr.get("ap_data_chiusura")).toString();

/*** 26/03/10 m: la chiusura del contatto sociale non ha effetto sul CASO perch�:
- se SOC, � gi� chiuso;
- se SAN, vanno controllati solo i contatti sanitari;
- se UVM, va fatto da "JFrameSkVal" oppure "JFrameCaso".
			// 15/06/09 Elisa Croci      ***************************************************************
			if (data_chiusura != null && !data_chiusura.equals(""))
			{
				mySystemOut("Controllo contatto UNICO H == " + h.toString());
				boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true);
				if (idCaso != -1 && unico)
				{
					mySystemOut("Gestisco la chiusura del caso" );
					// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
					// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
					// sociale associato!
					if(dbr.get("origine") != null && !dbr.get("origine").equals(""))
					{
						int origine = Integer.parseInt(dbr.get("origine").toString());
						mySystemOut("Origine del caso: " + origine);
						if(origine == GestCasi.CASO_SAN)
						{
							Hashtable hCaso = new Hashtable();
							hCaso.put("n_cartella", h.get("n_cartella"));
							hCaso.put("pr_data", h.get("pr_data"));
							hCaso.put("id_caso", new Integer(idCaso));
							hCaso.put("dt_conclusione", dbr.get("ap_data_chiusura"));
							hCaso.put("motivo", "99");
							hCaso.put("operZonaConf", (String)dbr.get("ap_ass_ref")); // 15/10/09
							mySystemOut(" -- update(): Chiudi caso = HashCaso: " + hCaso.toString());
							Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
							mySystemOut("Ritorno di ChiudiCaso == " + r);
						}
					}
				}
			}
			// ****************************************************************************************
*** 26/03/10 m *****************/
		}	
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

	// 26/10/06: inserimento su tabella PROGETTO di un record con i soli valori della chiave
	private void scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception
	{
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		mySystemOut(" -->> insert: Inserito record su tabella PROGETTO");
	}

	// 26/10/06: inserimento su tabella PROGETTO_CONT
	private void scriviProgettoCont(ISASConnection mydbc, String numCart, String dtSkVal,
			String tpOper, String numProg) throws Exception
			{
		ISASRecord dbrPrgCont = mydbc.newRecord("progetto_cont");
		dbrPrgCont.put("n_cartella", numCart);
		dbrPrgCont.put("pr_data", dtSkVal);
		dbrPrgCont.put("prc_tipo_op", tpOper);
		dbrPrgCont.put("prc_n_contatto", new Integer(numProg));
		mydbc.writeRecord(dbrPrgCont);
		mySystemOut(" -->> insert: Inserito record su tabella PROGETTO_CONT");
			}

	//gb 07/11/06: ritorna true se esistono diagnosi per quel progetto (contatto)
	//             con data apertura diagnosi minore di data apertura contatto.
	//             ritorna false altrimenti.
	private boolean dtApeContGTMinDtApeDiagnosi(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");
		String strDataApeContatto = (String) h.get("ap_data_apertura");

		String mySel = "SELECT n_cartella" +
		" FROM ass_diagnosi" +
		" WHERE n_cartella = " + strNCartella +
		" AND n_progetto = " + strNProgetto +
		" AND ad_data_ins < " + formatDate(dbc, strDataApeContatto);

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}

	//gb 07/11/06: ritorna true se esistono segnalazioni per quel progetto (contatto)
	//             con data apertura segnalazione minore di data apertura contatto.
	//             ritorna false altrimenti.
	private boolean dtApeContGTMinDtApeSegnalazione(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");
		String strDataApeContatto = (String) h.get("ap_data_apertura");

		String mySel = "SELECT n_cartella" +
		" FROM ass_segnalazioni" +
		" WHERE n_cartella = " + strNCartella +
		" AND n_progetto = " + strNProgetto +
		" AND segn_data_ins < " + formatDate(dbc, strDataApeContatto);

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}

	//gb 07/11/06: ritorna true se esistono obiettivi per quel progetto (contatto)
	//             con data apertura obiettivo minore di data apertura contatto.
	//             ritorna false altrimenti.
	private boolean dtApeContGTMinDtApeObiettivo(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");
		String strDataApeContatto = (String) h.get("ap_data_apertura");

		String mySel = "SELECT n_cartella" +
		" FROM ass_obbiettivi" +
		" WHERE n_cartella = " + strNCartella +
		" AND n_progetto = " + strNProgetto +
		" AND ob_data_ins < " + formatDate(dbc, strDataApeContatto);

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}

	//gb 07/11/06: ritorna true se esistono verifiche progetto per quel progetto (contatto)
	//             con data apertura di verifica progetto minore di data apertura contatto.
	//             ritorna false altrimenti.
	private boolean dtApeContGTMinDtApeVerProgetto(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");
		String strDataApeContatto = (String) h.get("ap_data_apertura");

		String mySel = "SELECT n_cartella" +
		" FROM ass_verifica" +
		" WHERE n_cartella = " + strNCartella +
		" AND n_progetto = " + strNProgetto +
		" AND ver_data < " + formatDate(dbc, strDataApeContatto);

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}

	public ISASRecord update(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
	CariException
	{
		mySystemOut(" CONTATTO SOCIALE: update == " + dbr.getHashtable().toString());
		boolean done=false;
		String strNAssistito = null;
		String strNProgetto = null;
		ISASConnection dbc = null;
		//Per la data di sistema
		NumberDateFormat ndf = new NumberDateFormat();

		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil(); //gb 05/11/08

		// Controllo dei campi che compongono la chiave primaria
		try {
			strNAssistito=(String)dbr.get("n_cartella");
			strNProgetto=(String)dbr.get("n_progetto");
		}
		catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: mancano uno o pi� campi in chiave primaria");
		}

		try{
			// Ottengo la connessione al database
			dbc=super.logIn(mylogin);

			Hashtable htRegUsl= getHtRegUsl(dbc); //gb 05/11/08

			// 01/02/08
			String esisteInterv = (String)dbr.get("esiste_interv");

			//gb 07/11/06 ************************* Controlli sulle date **********
			Hashtable h = dbr.getHashtable();
			if(dtApeContLEMaxDtContChius(dbc, h))
			{
				String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
				throw new CariException(msg, -2);
			}

			if(dtApeContGTMinDtApeDiagnosi(dbc, h))
			{
				String msg = "Attenzione: Data apertura nuovo contatto successiva a data apertura di prima diagnosi!";
				throw new CariException(msg, -2);
			}
			if(dtApeContGTMinDtApeSegnalazione(dbc, h))
			{
				String msg = "Attenzione: Data apertura nuovo contatto successiva a data apertura di prima segnalazione!";
				throw new CariException(msg, -2);
			}
			if(dtApeContGTMinDtApeObiettivo(dbc, h))
			{
				String msg = "Attenzione: Data apertura nuovo contatto successiva a data apertura di primo obiettivo!";
				throw new CariException(msg, -2);
			}
			if(dtApeContGTMinDtApeVerProgetto(dbc, h))
			{
				String msg = "Attenzione: Data apertura nuovo contatto successiva a data apertura di prima verifica progetto!";
				throw new CariException(msg, -2);
			}
			
			// ************************************

			// Scrivo il record nella tabella
			dbc.writeRecord(dbr);

			// Si crea la query per rileggere il record appena aggiornato
			String myselect = "SELECT * FROM ass_progetto" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;
			// Si legge il record appena aggioranto
			dbr=dbc.readRecord(myselect);

			// Si decodificano i alcuni campi della maschera a video.
			if (dbr != null)
			{
				decodificaAssOperatore(dbc, dbr, strNAssistito);
				//GB decodificaProgDescrizione(dbc, dbr, strNAssistito, strNProgetto);
				decodificaOperatore(dbc, dbr, "ap_oper_ap", "apeOpeCognome", "apeOpeNome");
				decodificaOperatore(dbc, dbr, "ap_oper_ch", "chiOpeCognome", "chiOpeNome");
				decodificaOperatore(dbc, dbr, "ap_ass_ref", "assSocRespOpeCognome", "assSocRespOpeNome");
				decodificaComune(dbc, dbr, "ap_ric_com_nasc", "ricComuneNascDescr");

				//gb 13/11/07: si carica un campo nascosto del client con lo stesso
				// valore di una combo dipendente e poi, nel client, si setta il valore
				// della combo al valore del campo nascosto
				dbr.put("ap_tipo_utente_hide",(String)dbr.get("ap_tipo_utente"));

				// 01/02/08
				dbr.put("esiste_interv", (String)esisteInterv);

				//gb 05/11/08
				String strCodPresOp= (String)util.getObjectField(dbr,"ap_ass_ref_presidio",'S');
				if((strCodPresOp!=null) && !strCodPresOp.equals(""))
				{
					String strPresOp = getSedeOperatore(dbc, dbr, "ap_ass_ref_presidio", htRegUsl);
					dbr.put("ap_ass_ref_pres_descr", strPresOp);
				}

				// 21/05/09 m. ------------------
				// lettura dtConclusione CASO precedente
				String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
				String tempoT = (String)h.get("tempo_t");

				// letture scale max
			  	gest_scaleVal.getScaleMax(dbc, dbr, strNAssistito, (String)h.get("pr_data"),
										"", dtChiusCasoPrec, "", tempoT, "01");
				// 21/05/09 m. ------------------

				// 26/06/09 Elisa Croci: mi mancava la pr_data...
				dbr.put("pr_data", (String)h.get("pr_data"));
				
			  	// 21/05/09 Elisa Croci
				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SOC))
				{
					int idCaso = prendi_dati_caso(dbc, dbr);
					if(idCaso != -1)
						gestione_segnalazione(dbc, dbr, h, "update");
					else // 14/05/10
						gestione_segnalazione(dbc, dbr, h, "insert");

					mySystemOut("Fatta gestione segnalazione per il caso: " + idCaso);

					String data_chiusura= "";
					if (dbr.get("ap_data_chiusura") != null)
						data_chiusura=((java.sql.Date)dbr.get("ap_data_chiusura")).toString();

/*** 26/03/10 m: la chiusura del contatto sociale non ha effetto sul CASO perch�:
- se SOC, � gi� chiuso;
- se SAN, vanno controllati solo i contatti sanitari;
- se UVM, va fatto da "JFrameSkVal" oppure "JFrameCaso".

					// 15/06/09 Elisa Croci      ***************************************************************
					if (data_chiusura != null && !data_chiusura.equals(""))
					{
						mySystemOut("Controllo contatto UNICO H == " + h.toString());
						boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true);
						if (idCaso != -1 && unico)
						{
							mySystemOut("Gestisco la chiusura del caso" );
							// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
							// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
							// sociale associato!
							if(dbr.get("origine") != null && !dbr.get("origine").equals(""))
							{
								int origine = Integer.parseInt(dbr.get("origine").toString());
								mySystemOut("Origine del caso: " + origine);
								if(origine == GestCasi.CASO_SAN)
								{
									Hashtable hCaso = new Hashtable();
									hCaso.put("n_cartella", h.get("n_cartella"));
									hCaso.put("pr_data", h.get("pr_data"));
									hCaso.put("id_caso", new Integer(idCaso));
									hCaso.put("dt_conclusione", dbr.get("ap_data_chiusura"));
									hCaso.put("motivo", "99");
									hCaso.put("operZonaConf", (String)dbr.get("ap_ass_ref")); // 15/10/09
									mySystemOut(" -- update(): Chiudi caso = HashCaso: " + hCaso.toString());
									Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
									mySystemOut("Ritorno di ChiudiCaso == " + r);
								}
							}
						}
					}
					// ****************************************************************************************
*** 26/03/10 m *****************/
				}


				// 15/06/09 Elisa Croci  ***************************************************************
				if(h.containsKey("ubicazione") && dbr != null)
					dbr.put("ubicazione", h.get("ubicazione"));
				if(h.containsKey("update_segnalazione") && dbr != null)
					dbr.put("update_segnalazione", h.get("update_segnalazione"));
				// *************************************************************************************

				dbr.put("tempo_t", tempoT);
			}

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		//gb 07/11/06 **************
		catch(CariException ce){
			ce.setISASRecord(null);
			throw ce;
		}
		// *************************
		catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}
		catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}

		catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo la update() - "+  e1);
		}
		finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}

	}

	
	private void deleteDiagnosi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strNDiagnosi = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_diagnosi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut("deleteDiagnosi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strNDiagnosi = ((Integer) dbr.get("n_diagnosi")).toString();

				String myS =  "SELECT *" +
				" FROM ass_diagnosi" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND n_diagnosi = " + strNDiagnosi;
				mySystemOut("deleteDiagnosi (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteDiagnosi: Cancellato Rec. Diagnosi (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteDiagnosi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record delle diagnosi");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteDiagnosi - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteEventi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strCodObiettivo = null;
		String strNIntervento = null;
		String strNEvento = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_int_eventi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut("deleteEventi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strCodObiettivo = (String) dbr.get("cod_obbiettivo");
				strNIntervento = ((Integer) dbr.get("n_intervento")).toString();
				strNEvento = ((Integer) dbr.get("n_evento")).toString();

				String myS =  "SELECT *" +
				" FROM ass_int_eventi" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND n_intervento = " + strNIntervento +
				" AND n_evento = " + strNEvento +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'";

				mySystemOut("deleteEventi (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteEventi: Cancellato Rec. Eventi (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteEventi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record degli eventi");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteEventi - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteInterventi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strCodObiettivo = null;
		String strNIntervento = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_interventi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut("deleteInterventi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strCodObiettivo = (String) dbr.get("cod_obbiettivo");
				strNIntervento = ((Integer) dbr.get("n_intervento")).toString();

				String myS =  "SELECT *" +
				" FROM ass_interventi" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND n_intervento = " + strNIntervento +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'";

				mySystemOut("deleteInterventi (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteInterventi: Cancellato Rec. Interventi (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteInterventi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record degli interventi");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteInterventi - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteObiettivi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strCodObiettivo = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_obbiettivi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" deleteObiettivi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strCodObiettivo = (String) dbr.get("cod_obbiettivo");

				String myS =  "SELECT *" +
				" FROM ass_obbiettivi" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'";

				mySystemOut("deleteObiettivi (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteObiettivi: Cancellato Rec. Obiettivi (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteObiettivi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record degli obiettivi");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteObiettivi - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteSegnalazioni(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strSegnProgr = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_segnalazioni" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" deleteSegnalazioni - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr = dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strSegnProgr = ((Integer) dbr.get("segn_progr")).toString();

				String myS =  "SELECT *" +
				" FROM ass_segnalazioni" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND segn_progr = " + strSegnProgr;

				mySystemOut("deleteSegnalazioni (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteSegnalazioni: Cancellato Rec. Segnalazioni (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteSegnalazioni - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record delle segnalazioni");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteSegnalazioni - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteVerifiche(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strNVerifica = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_verifica" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" deleteVerifiche - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr = dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strNVerifica = ((Integer) dbr.get("n_verifica")).toString();

				String myS =  "SELECT *" +
				" FROM ass_verifica" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND n_verifica = " + strNVerifica;

				mySystemOut("deleteVerifiche (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteVerifiche: Cancellato Rec. Verifica (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteVerifiche - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record delle verifiche");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteVerifiche - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteOpabil(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strCodOpabil = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_opabil" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" deleteOpabil - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strCodOpabil = (String) dbr.get("opabil_cod");

				String myS =  "SELECT *" +
				" FROM ass_opabil" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND opabil_cod = '" + strCodOpabil + "'";

				mySystemOut("deleteOpabil (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteOpabil: Cancellato Rec. Opabil (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteOpabil - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record degli operatori abilitati");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteOpabil - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	private void deleteOpref(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean done = false;
		ISASCursor dbcur = null;
		String strCodOpref = null;
		String strDataOpref = null;

		try {

			String myselect = "SELECT *" +
			" FROM ass_opref" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut("deleteOpref - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			mySystemOut("GB-->>Fatta la startcursor");

			// Metto i record letti in un vector (un vector di ISASRecord).
			Vector vdbr=dbcur.getAllRecord();
			mySystemOut("GB-->>Creato il vettore");

			for( int i=0; i<vdbr.size(); i++ )
			{
				ISASRecord dbr = (ISASRecord) vdbr.get(i);
				strCodOpref = (String) dbr.get("opref_cod");
				strDataOpref=((java.sql.Date)dbr.get("opref_da")).toString();

				String myS =  "SELECT *" +
				" FROM ass_opref" +
				" WHERE n_cartella = " + strNAssistito +
				" AND n_progetto = " + strNProgetto +
				" AND opref_cod = '" + strCodOpref + "'" +
				" AND opref_da = " + formatDate(mydbc, strDataOpref);

				mySystemOut("deleteOpref (" + i + ") : "  + myS);
				ISASRecord rec = mydbc.readRecord(myS);
				// Cancellazione del Record ass_int_eventi
				mydbc.deleteRecord(rec);
				mySystemOut("deleteOpref: Cancellato Rec. Opref (" + i + ")");
			}

			dbcur.close();
			done = true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: deleteOpref - Eccezione= " + e);
			throw new SQLException("Errore eseguendo la delete dei record degli operatori referenti");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: deleteOpref - Eccezione nella chiusura del cursore = " + e1);
				}
			}
		}
			}

	public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		ISASConnection dbc=null;

		String strNAssistito=null;
		String strNProgetto=null;
		String strDtSkVal = null;// 26/10/06

		// Controllo dei campi che compongono la chiave primaria
		try {
			strNAssistito="" +dbr.get("n_cartella");
			strNProgetto=""+dbr.get("n_progetto");
			strDtSkVal = "" + dbr.get("pr_data");// 26/10/06
		}
		catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: mancano uno o pi� campi in chiave primaria");
		}

		try{
			dbc=super.logIn(mylogin);
			// Inizio la TRANSAZIONE
			dbc.startTransaction();

			deleteDiagnosi(dbc, strNAssistito, strNProgetto);
			deleteEventi(dbc, strNAssistito, strNProgetto);
			deleteInterventi(dbc, strNAssistito, strNProgetto);
			deleteObiettivi(dbc, strNAssistito, strNProgetto);
			deleteSegnalazioni(dbc, strNAssistito, strNProgetto);
			deleteVerifiche(dbc, strNAssistito, strNProgetto);
			deleteOpabil(dbc, strNAssistito, strNProgetto);
			deleteOpref(dbc, strNAssistito, strNProgetto);
			// 26/10/06 m.
			deleteProgettoCont(dbc, strNAssistito, strDtSkVal, "01", strNProgetto);
			// 10/10/07 -----
			deletePianoAssist(dbc, strNAssistito, strNProgetto);
			deletePianoAccessi(dbc, strNAssistito, strNProgetto);
			deletePianoVerifiche(dbc, strNAssistito, strNProgetto);
			// 10/10/07 -----

			dbc.deleteRecord(dbr);

			// Concludo la TRANSAZIONE
			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			done=true;
		}
		catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				System.out.println("SocAssProgettoEJB.delete() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo la rollback() - " +  e1);
			}
			throw e;
		}
		catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				System.out.println("SocAssProgettoEJB.delete() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo la rollback() - "+  e1);
			}
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
			try{
				System.out.println("SocAssProgettoEJB.delete() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo la rollback() - " +  e1);
			}
			throw new SQLException("Errore eseguendo la delete() - "+  e);
		}
		finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	// 26/10/06 m.
	private void deleteProgettoCont(ISASConnection mydbc, String numCart, String dtSkVal,
			String tpOper, String numProg) throws Exception
			{
		String myselect = "SELECT * FROM progetto_cont" +
		" WHERE n_cartella = " + numCart +
		" AND pr_data = " + formatDate(mydbc, dtSkVal) +
		" AND prc_tipo_op = '" + tpOper + "'" +
		" AND prc_n_contatto = " + numProg;

		ISASRecord dbr_pc = mydbc.readRecord(myselect);
		if (dbr_pc != null)
			mydbc.deleteRecord(dbr_pc);
		mySystemOut(" -->> delete: Eliminato record su tabella PROGETTO_CONT");
			}

	// 10/10/07
	private void deletePianoAssist(ISASConnection dbc, String cartella, String contatto) throws Exception
	{
		String strCodObiettivo = null;
		String strNIntervento = null;

		String myselect="SELECT * FROM piano_assist" +
		" WHERE pa_tipo_oper = '01'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto;
		mySystemOut("deletePianoAssist "+myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next()){
			ISASRecord dbr = dbcur.getRecord();
			strCodObiettivo = (String) dbr.get("cod_obbiettivo");
			strNIntervento = ((Integer) dbr.get("n_intervento")).toString();

			String sel = "SELECT * FROM piano_assist" +
			" WHERE pa_tipo_oper = '01'" +
			" AND n_cartella = " + cartella +
			" AND n_progetto = " + contatto +
			" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
			" AND n_intervento = " + strNIntervento +
			" AND pa_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("pa_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	// 10/10/07
	private void deletePianoAccessi(ISASConnection dbc, String cartella, String contatto) throws Exception
	{
		String strCodObiettivo = null;
		String strNIntervento = null;

		String myselect="SELECT * FROM piano_accessi" +
		" WHERE pa_tipo_oper = '01'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto;
		mySystemOut("deletePianoAccessi " + myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			strCodObiettivo = (String) dbr.get("cod_obbiettivo");
			strNIntervento = ((Integer) dbr.get("n_intervento")).toString();

			String sel = "SELECT * FROM piano_accessi" +
			" WHERE pa_tipo_oper = '01'" +
			" AND n_cartella = " + cartella +
			" AND n_progetto = " + contatto +
			" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
			" AND n_intervento = " + strNIntervento +
			" AND pa_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("pa_data"))) +
			" AND pi_prog = " + (Integer)dbr.get("pi_prog");
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	// 10/10/07
	private void deletePianoVerifiche(ISASConnection dbc, String cartella, String contatto)	throws Exception
	{
		String strCodObiettivo = null;
		String strNIntervento = null;

		String myselect="SELECT * FROM piano_verifica" +
		" WHERE pa_tipo_oper = '01'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto;
		mySystemOut("deletePianoVerifiche "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			strCodObiettivo = (String) dbr.get("cod_obbiettivo");
			strNIntervento = ((Integer) dbr.get("n_intervento")).toString();

			String sel = "SELECT * FROM piano_verifica" +
			" WHERE pa_tipo_oper = '01'" +
			" AND n_cartella = " + cartella +
			" AND n_progetto = " + contatto +
			" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
			" AND n_intervento = " + strNIntervento +
			" AND pa_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("pa_data"))) +
			" AND ve_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("ve_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}





	// Controlla se esiste almeno 1 diagnosi per un dato progetto.
	private boolean query_existDiagnosi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT n_diagnosi" +
			" FROM ass_diagnosi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" query_esisteDiagnosi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_existDiagnosi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo della esistenza di diagnosi  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_existDiagnosi - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	private boolean query_esisteDiagnosiChiuse(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT n_diagnosi" +
			" FROM ass_diagnosi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto +
			" AND ad_chiusa = 'S'";

			mySystemOut(" query_esisteDiagnosiChiuse - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_esisteDiagnosiChiuse - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo delle diagnosi chiuse  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_esisteDiagnosiChiuse - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	// Controlla se esiste almeno 1 segnalazione per un dato progetto.
	private boolean query_existSegnalazioni(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT segn_progr" +
			" FROM ass_segnalazioni" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" query_existSegnalazioni - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_existSegnalazioni - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo della esistenza di segnalazioni  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_existSegnalazioni - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	private boolean query_esisteSegnalazioniLette(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT segn_progr" +
			" FROM ass_segnalazioni" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto +
			" AND segn_letta = 'S'";

			mySystemOut(" query_esisteSegnalazioniLette - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_esisteSegnalazioniLette - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo delle segnalazioni lette  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_esisteSegnalazioniLette - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	private boolean query_existVerifiche(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT n_verifica" +
			" FROM ass_verifica" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut("query_existVerifiche - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_existVerifiche - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo della esistenza di verifiche  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_existVerifiche - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	private boolean query_esisteVerificheChiuse(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT n_verifica" +
			" FROM ass_verifica" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto +
			" AND ver_chiusa = 'S'";

			mySystemOut(" query_esisteVerificheChiuse - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_esisteVerificheChiuse - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo delle verifiche chiuse  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_esisteVerificheChiuse - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	private boolean query_esisteInterventi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT n_intervento" +
			" FROM ass_interventi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" query_esisteInterventi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);

			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_esisteInterventi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo di esistenza degli interventi");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_esisteInterventi - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	// Controlla se esiste almeno 1 obiettivo per un dato progetto.
	private boolean query_existObiettivi(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		boolean boolResCod = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT cod_obbiettivo" +
			" FROM ass_obbiettivi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto;

			mySystemOut(" query_existObiettivi - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));

			dbcur.close();
			done = true;

			return risu;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_existObiettivi - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo della esistenza degli obiettivi ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_existObiettivi - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	private boolean query_cancObiettiviConsentita(ISASConnection mydbc,
			String strNAssistito, String strNProgetto) throws SQLException
			{
		boolean risu = false;
		boolean done = false;
		boolean boolResCod = false;
		ISASCursor dbcur = null;

		try{

			String myselect = "SELECT cod_obbiettivo" +
			" FROM ass_obbiettivi" +
			" WHERE n_cartella = " + strNAssistito +
			" AND n_progetto = " + strNProgetto +
			" AND ob_raggiunto = 'S'";

			mySystemOut(" query_cancObiettiviConsentita - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();
			done = true;
			if (risu)
				return false;

			boolResCod = query_esisteInterventi(mydbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return false;

			return true;
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_cancObiettiviConsentita - Eccezione= " + e);
			throw new SQLException("Errore eseguendo il controllo degli obiettivi raggiunti e esist. interventi");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_cancObiettiviConsentita - Eccezione nella chiusura del cursore= " + e1);
				}
			}
		}
			}

	public Boolean query_cancProgettoConsentita(myLogin mylogin, Hashtable h) throws SQLException
	{
		boolean done = false;
		boolean boolResCod = false;

		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");

		try{
			dbc = super.logIn(mylogin);

			boolResCod = query_esisteDiagnosiChiuse(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Boolean(false);
			boolResCod = query_cancObiettiviConsentita(dbc, strNAssistito, strNProgetto);
			if (!boolResCod)
				return new Boolean(false);
			boolResCod = query_esisteSegnalazioniLette(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Boolean(false);
			boolResCod = query_esisteVerificheChiuse(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Boolean(false);

			dbc.close();
			super.close(dbc);
			done = true;

			return new Boolean(true);
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_cancProgettoConsentita - Eccezione= " + e);
			throw new SQLException("Errore eseguendo i controlli entit� chiuse  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_cancProgettoConsentita - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}


	public Integer query_cancProgettoConsentitaSemplice(myLogin mylogin, Hashtable h) throws SQLException
	{
		boolean done = false;
		boolean boolResCod = false;

		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strNProgetto = (String) h.get("n_progetto");

		try{
			dbc = super.logIn(mylogin);

			boolResCod = query_existDiagnosi(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Integer(CONST_EXIST_DIAGNOSI);
			boolResCod = query_existObiettivi(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Integer(CONST_EXIST_OBIETTIVI);
			boolResCod = query_existSegnalazioni(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Integer(CONST_EXIST_SEGNALAZIONI);
			boolResCod = query_existVerifiche(dbc, strNAssistito, strNProgetto);
			if (boolResCod)
				return new Integer(CONST_EXIST_VERIFICHE);

			dbc.close();
			super.close(dbc);
			done = true;

			return new Integer(CONST_CANC_PROGETTO_CONSENTITA);
		}catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_cancProgettoConsentitaSemplice - Eccezione= " + e);
			throw new SQLException("Errore eseguendo i controlli esistenza entit�  ");
		}finally{
			System.out.println("\nGBSocAssProgettoEJB: query_cancProgettoConsentitaSemplice/FINALLY\n");
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_cancProgettoConsentitaSemplice - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}

	//gb 15/01/08 *******
	// In caso di JFrameASAssProgetto in stato di INSERT, ritorna una Hashtable
	// riempita con i dati del 'bisogno' e del 'segnalante' presi dalla tabella ass_anagrafica.
	// 12/05/10: se mancano i diritti, semplicemente non carico il rec e non notifico nessun msg
	public Hashtable query_getDatiAssAnagrafica(myLogin mylogin, Hashtable h) throws Exception, CariException
	{
		boolean done = false;

		ISASConnection dbc = null;
		Hashtable htRes = null;

		try{
			dbc = super.logIn(mylogin);

			String strQuery = "";
			String strAssAnagrCognome = "";
			String strAssAnagrNome = "";
			String strAssAnagrDtNasc = "";
			String strAssAnagrComNasc = "";
			String strAssAnagrProgressivo = (String) h.get("ass_angr_progressivo");
			if ((strAssAnagrProgressivo == null) || strAssAnagrProgressivo.equals(""))
			{
				strAssAnagrCognome = (String) h.get("ass_anagr_cognome");
				strAssAnagrCognome = duplicateChar(strAssAnagrCognome, "'"); // 06/06/08
				strAssAnagrNome = (String) h.get("ass_anagr_nome");
				strAssAnagrNome = duplicateChar(strAssAnagrNome, "'"); // 06/06/08
				strAssAnagrDtNasc = (String) h.get("ass_anagr_data_nascita");
				strAssAnagrComNasc = (String) h.get("ass_anagr_com_nascita");

				strQuery = "SELECT *" +
				" FROM ass_anagrafica" +
				" WHERE cognome = '" + strAssAnagrCognome + "'" +
				" AND nome = '" + strAssAnagrNome + "'" +
				" AND comune_nascita = '" + strAssAnagrComNasc + "'" +
				" AND data_nascita = " + formatDate(dbc, strAssAnagrDtNasc) +
				" AND data_reg = (" +
					"SELECT MAX(data_reg) data_registrazione" +
					" FROM ass_anagrafica a" +
					" WHERE a.cognome = '" + strAssAnagrCognome + "'" +
					" AND a.nome = '" + strAssAnagrNome + "'" +
					" AND a.comune_nascita = '" + strAssAnagrComNasc + "'" +
					" AND a.data_nascita = " + formatDate(dbc, strAssAnagrDtNasc) +
// 24/11/09			" AND a.esito_contatto IN ('3', '4', '6')" + // 28/05/09 m.
					" AND a.esito_contatto IN ('3', '4', '6', '15')" +
				")";

			}
			else
			{
				strQuery = "SELECT *" +
				" FROM ass_anagrafica" +
				" WHERE progressivo = " + strAssAnagrProgressivo;
			}
			mySystemOut("query_getDatiAssAnagrafica/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			if (dbr != null)
			{
				decodificaComune(dbc, dbr, "segn_comune_nas", "segn_comune_nas_descr");
				decodificaComune(dbc, dbr, "segn_comune_res", "segn_comune_res_descr");

				// 24/12/08
				dbr.put("ass_angr_progressivo", ((Integer)dbr.get("progressivo")).toString());
			}

			dbc.close();
			super.close(dbc);
			done = true;

			htRes = new Hashtable();
			if (dbr == null)
				return htRes;
			else
			{
				htRes = (Hashtable)dbr.getHashtable();
				return htRes;
			}
		}
		// 27/08/08 ---
		catch(ISASPermissionDeniedException e){
			System.out.println("SocAssProgettoEJB.query_getDatiAssAnagrafica(): "+e);
// 12/05/10	throw new CariException(msgNoD, -2);
			// 12/05/10
			System.out.println("SocAssProgettoEJB.query_getDatiAssAnagrafica(): MANCANO DIRITTI su record ASS_ANAGRAFICA");
			return htRes;
		}
		// 27/08/08 ---
		catch(Exception e){
			System.out.println("SocAssProgettoEJB: query_getDatiAssAnagrafica - Eccezione= " + e);
			throw new Exception("Errore recupero dati da ass_anagrafica ");
		}finally{
			System.out.println("\nGBSocAssProgettoEJB: query_getDatiAssAnagrafica/FINALLY\n");
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SocAssProgettoEJB: query_getDatiAssAnagrafica - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}
	//gb 15/01/08: fine *******

	// 24/12/08
	// 12/05/10: se mancano i diritti, semplicemente non carico il rec, ma non notifico nessun msg
	private ISASRecord getRecAssAna(ISASConnection mydbc, Hashtable h0) throws Exception
	{
		ISASRecord mydbr = null;
		try {
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
		} catch(ISASPermissionDeniedException e){ // 12/05/10
			System.out.println("SocAssProgettoEJB.getRecAssAna(): MANCANO DIRITTI su record ASS_ANAGRAFICA");
			System.out.println("SocAssProgettoEJB.getRecAssAna(): "+e);
			return null;
		}
		return mydbr;
	}

	// 24/12/08: aggiornamento di ASS_ANAGRAFICA con campi relativi alla presa carico
	private void aggiornaAssAna(ISASConnection mydbc, ISASRecord mydbr, Hashtable h0) throws Exception
	{
		if (mydbr != null) {
			mydbr.put("n_cartella", h0.get("n_cartella"));

			mydbr.put("soc_carico", "S");
			mydbr.put("soc_data", h0.get("ap_data_apertura"));
			mydbr.put("soc_cod", h0.get("ap_ass_ref"));

			mydbc.writeRecord(mydbr);
			mySystemOut("aggiornaAssAna: aggiornato progr=["+mydbr.get("progressivo")+"]");
		}
	}




	// 03/04/06 m.: controlla se l'oper e' l'attuale oper referente del progetto
	public Boolean checkOperRef(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean isOperRef = false;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String)h0.get("n_cartella");
			String prog = (String)h0.get("n_progetto");
			String oper = (String)h0.get("cod_oper");
			String operRef = null;

			String myselect = "SELECT o.opref_cod FROM ass_opref o" +
			" WHERE o.n_cartella = " + cart +
			" AND o.n_progetto = " + prog +
			" AND o.opref_da IN (SELECT MAX(x.opref_da) FROM ass_opref x" +
			" WHERE x.n_cartella = o.n_cartella" +
			" AND x.n_progetto = o.n_progetto)";

			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr != null)
				operRef = (String)dbr.get("opref_cod");
			if (operRef != null)
				isOperRef = (oper.trim().equals(operRef.trim()));

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(isOperRef));
		} catch(Exception e1){
			System.out.println("SocAssProgettoEJB.checkOperRef - Eccezione=[" + e1 + "]");
			return (Boolean)null;
		} finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	// 03/04/06 m.: controlla se l'oper e' tra gli oper abilitati al progetto
	public Integer getLivAbilOper(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		int  livAbil = -1;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String)h0.get("n_cartella");
			String prog = (String)h0.get("n_progetto");
			String oper = (String)h0.get("cod_oper");


			String myselect = "SELECT o.opabil_liv FROM ass_opabil o" +
			" WHERE o.n_cartella = " + cart +
			" AND o.n_progetto = " + prog +
			" AND o.opabil_cod = '" + oper + "'";

			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				String livello = (String)dbr.get("opabil_liv");
				if ((livello != null) && (!livello.trim().equals("")))
					livAbil = Integer.parseInt(livello.trim());
			}
			dbc.close();
			super.close(dbc);
			done = true;

			return (new Integer(livAbil));
		} catch(Exception e1){
			System.out.println("SocAssProgettoEJB.getLivAbilOper - Eccezione=[" + e1 + "]");
			return (new Integer(-1));
		} finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

//	// 31/01/07
//	private String decodificaTabVoci(ISASConnection mydbc, String val, String codice) throws Exception
//	{
//		String strDescrizione = "";
//
//		if ((val != null) && (!val.trim().equals(""))) {
//			String selS = "SELECT tab_descrizione" +
//			" FROM tab_voci" +
//			" WHERE tab_cod = '" + codice + "'" +
//			" AND tab_val = '" + val + "'";
//
//			ISASRecord rec = mydbc.readRecord(selS);
//
//			if (rec != null)
//				strDescrizione = (String)rec.get("tab_descrizione");
//
//			if (strDescrizione == null)
//				strDescrizione = "";
//		}
//		return strDescrizione;
//	}

	// 13/09/07 m.: ctrl esistenza progetti successivi ad una certa data
	public Boolean query_checkProgSuccessivi(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String)h0.get("n_cartella");
			String dtRiferimento = (String)h0.get("dataRif");

			String myselect = "SELECT * FROM ass_progetto" +
			" WHERE n_cartella = " + cart +
			" AND ap_data_apertura > " + formatDate(dbc, dtRiferimento);

			mySystemOut("query_checkProgSuccessivi - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(risu));
		} catch(Exception e1){
			System.out.println("SocAssProgettoEJB.query_checkProgSuccessivi - Eccezione=[" + e1 + "]");
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

	// 11/01/08
	private String leggiConf(ISASConnection mydbc, String cod) throws Exception
	{
		String desc = "";

		String selConf = "SELECT conf_txt" +
		" FROM conf" +
		" WHERE conf_kproc = 'SINS'" +
		" AND conf_key = '" + cod + "'";

		ISASRecord dbrDec = mydbc.readRecord(selConf);
		if (dbrDec != null)
			if (dbrDec.get("conf_txt") != null)
				desc = (String)dbrDec.get("conf_txt");

		return desc;
	}// END leggiConf

	// 20/05/09 Elisa Croci
	/* 1) Il caso non esiste: creo il caso e la segnalazione
	 * 2) Il caso esiste ma e' chiuso: creo il caso e la segnalazione
	 * 3) Il caso e' attivo: aggiorno la segnalazione
	*/
	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov)
	throws NumberFormatException, ISASMisuseException, CariException, Exception
	{
		mySystemOut(" gestione_segnalazione -- HASH: " +  h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;

		h.put("operZonaConf", (String)dbr.get("ap_ass_ref")); // 15/10/09

		if(dbr.get("id_caso") != null && !dbr.get("id_caso").equals("-1"))
		{
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(dbr.get("stato").toString());
			id_caso = Integer.parseInt(dbr.get("id_caso").toString());
		}

		// se sono in insert e il caso non esiste oppure e' concluso, devo crearne uno!
		if(prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU
		|| stato_caso == GestCasi.STATO_ATTESA)) // 10/04/12
		{
			// 10/04/12: cntrl  se il CASO e la SEGNALAZIONE sono stati inseriti da Sins_PUA ---------
			if (esisteSegnAssAnag(dbc, dbr)) {
				mySystemOut(" gestione_segnalazione - trovato rec su ASS_ANAGRAFICA: NON invio evento segnalazione");
				return -1;
			}
			// 10/04/12 ---------
			
			// se il caso non esiste, non c'e' nemmeno la segnalazione, allora la creo!
			try
			{
				h.put("tipo_caso", new Integer(GestCasi.CASO_SOC));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_SOCIALE));

				if(h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals(""))
					h.put("dt_segnalazione", h.get("ap_data_apertura"));

				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
				// arriva la data del progetto, cosi' me la copio dal dbr!
				h.put("pr_data", dbr.get("pr_data"));

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
				System.out.println("SocAssProgettoEJB gestione_segnalazione, insert -- " + e);
				throw e;
			}
			catch (DBRecordChangedException e)
			{
				System.out.println("SocAssProgettoEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
			catch (ISASPermissionDeniedException e)
			{
				System.out.println("SocAssProgettoEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
			catch (SQLException e)
			{
				System.out.println("SocAssProgettoEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
			catch (Exception e)
			{
				System.out.println("SocAssProgettoEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			}
		}
// 29/03/10	else if(id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU &&  stato_caso != -1))
		else if(id_caso != -1 && (stato_caso != -1))
		{
			// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione e della presa in carico
			try
			{
				Enumeration e = dbr.getHashtable().keys();
				while(e.hasMoreElements())
				{
					String chiave = e.nextElement().toString();

					if(!h.containsKey(chiave))
						h.put(chiave, dbr.get(chiave));
				}

				mySystemOut(" gestione_segnalazione - UPDATE, H: " + h.toString());

				// 12/08/10 m ---------------
				if(dbr.get("origine") != null && !dbr.get("origine").equals("")) {
					int origine = Integer.parseInt(dbr.get("origine").toString());
					mySystemOut("gestione_segnalazione: Origine del caso "+id_caso+" =["+origine+"]");

					//  aggiorno solo se il caso nel frattempo non � diventato UVM, altrimenti prtono comunicazioni di EVENTI non previste
					if(origine == GestCasi.CASO_SOC) { // 12/08/10 m ---
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
					}
				}

				return id_caso;
			}
			catch (CariException e) // 17/11/09
			{
				System.out.println("SocAssProgettoEJB gestione_segnalazione, update -- " + e);
				throw e;
			}
			catch (Exception e)
			{
				System.out.println("SocAssProgettoEJB gestione_segnalazione, update() -- " + e);
				return id_caso;
			}
		}
		else return id_caso;
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
			if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SOC) && caso != -1)
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
			System.out.println("SocAssProgettoEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
			return false;
		}
		catch (Exception e)
		{
			System.out.println("SocAssProgettoEJB prendi_segnalazione, fallimento! -- " + e);
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

			ISASRecord rec = gestore_casi.getCasoRif(dbc, h);
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
			System.out.println("SocAssProgettoEJB prendi_dati_caso, manca chiave primaria! -- " + e);
			return -1;
		}
		catch (Exception e)
		{
			System.out.println("SocAssProgettoEJB prendi_dati_caso, fallimento! -- " + e);
			return -1;
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



	// 21/07/10 m.: riapertura progetto = aggiornamento a NULL dei campi
	// relativi alla chiusura sulle tabelle ASS_VERIIFICA e ASS_PROGETTO
	public ISASRecord riaperturaASPrg(myLogin mylogin,ISASRecord dbr)
				throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException
    {
		boolean done=false;
	    String strNAssistito=null;
    	String strNProgetto=null;
	    ISASConnection dbc=null;

    	//Per la data di sistema
	    NumberDateFormat ndf = new NumberDateFormat();
		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

    	// Controllo dei campi che compongono la chiave primaria
	    try {
    	    strNAssistito=(String)dbr.get("n_cartella");
        	strNProgetto=(String)dbr.get("n_progetto");
    	}
	    catch (Exception e){
    	    e.printStackTrace();
			throw new SQLException("Errore: mancano uno o pi� campi in chiave primaria");
	    }

    	try{
        	// Ottengo la connessione al database
	        dbc=super.logIn(mylogin);

    	    // Inizio la TRANSAZIONE
        	dbc.startTransaction();

			Hashtable htRegUsl= getHtRegUsl(dbc); //gb 05/11/08
			// 01/02/08
			String esisteInterv = (String)dbr.get("esiste_interv");
			Hashtable h = dbr.getHashtable();
			mySystemOut("SocAssProgettoEJB.riaperturaPrg(): H da DBR - h=["+((Hashtable)dbr.getHashtable()).toString()+"]");

	        // Leggo la data di sistema (formato 'yyyy-mm-gg')
    	    String strDataSistema = ndf.getJdbcSystemDate();

			String strOper = (String)dbr.get("oper_loggato");

	        // Leggo, se esiste, il record della verifica DEFINITIVA e CHIUSA dalla tabella ASS_VERIFICA
	        ISASRecord dbrVer = getDbrVer(dbc, strNAssistito, strNProgetto);
	        // Modifico i dati nel record di ASS_VERIFICA
			if (dbrVer != null) {
				dbrVer.put("ver_oper_chiusa", (String)null);
				dbrVer.put("ver_data_chiusa", (java.sql.Date)null);
				dbrVer.put("ver_chiusa", "N");

				// Setto la data e oper di Ultima Modifica.
				dbrVer.put("ver_data_ultmod", strDataSistema);
				dbrVer.put("ver_oper_ultmod", (String)strOper);

				// Riscrivo il record nella tabella ASS_VERIFICA
				dbc.writeRecord(dbrVer);
			}

			// pulisco MOTIVO CHIUSURA (campo non presente sulla maschera client JFrameASAssProgetto)
			dbr.put("motivo_chiusura", (String)null);

        	// Scrivo il record nella tabella ASS_PROGETTO
        	dbc.writeRecord(dbr);

	        // Si crea la query per rileggere il record appena aggiornato
	        String myselect = "SELECT * FROM ass_progetto" +
	                          " WHERE n_cartella = " + strNAssistito +
	                          " AND n_progetto = " + strNProgetto;

	        // Si legge il record appena aggioranto
	        dbr=dbc.readRecord(myselect);

	        // Si decodificano i alcuni campi della maschera a video (ripreso dal metodo "queryKey()").
	        if (dbr != null) {
   				decodificaAssOperatore(dbc, dbr, strNAssistito);
				//GB decodificaProgDescrizione(dbc, dbr, strNAssistito, strNProgetto);
				decodificaOperatore(dbc, dbr, "ap_oper_ap", "apeOpeCognome", "apeOpeNome");
				decodificaOperatore(dbc, dbr, "ap_ass_ref", "assSocRespOpeCognome", "assSocRespOpeNome");
				decodificaComune(dbc, dbr, "ap_ric_com_nasc", "ricComuneNascDescr");

				//gb 13/11/07: si carica un campo nascosto del client con lo stesso
				// valore di una combo dipendente e poi, nel client, si setta il valore
				// della combo al valore del campo nascosto
				dbr.put("ap_tipo_utente_hide",(String)dbr.get("ap_tipo_utente"));

				// 01/02/08
				dbr.put("esiste_interv", (String)esisteInterv);

				//gb 05/11/08
				String strCodPresOp= (String)util.getObjectField(dbr,"ap_ass_ref_presidio",'S');
				if((strCodPresOp!=null) && !strCodPresOp.equals(""))
				{
					String strPresOp = getSedeOperatore(dbc, dbr, "ap_ass_ref_presidio", htRegUsl);
					dbr.put("ap_ass_ref_pres_descr", strPresOp);
				}

				// 21/05/09 m. ------------------
				// lettura dtConclusione CASO precedente
				String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
				String tempoT = (String)h.get("tempo_t");

				// letture scale max
			  	gest_scaleVal.getScaleMax(dbc, dbr, strNAssistito, (String)h.get("pr_data"),
										"", dtChiusCasoPrec, "", tempoT, "01");
				// 21/05/09 m. ------------------

			  	// 21/05/09 Elisa Croci
				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SOC))
				{
					// 26/06/09 Elisa Croci: mi mancava la pr_data...
					dbr.put("pr_data", (String)h.get("pr_data"));

					int idCaso = prendi_dati_caso(dbc, dbr);
				}

				// 15/06/09 Elisa Croci  ***************************************************************
				if(h.containsKey("ubicazione") && dbr != null)
					dbr.put("ubicazione", h.get("ubicazione"));
				// *************************************************************************************

				dbr.put("tempo_t", tempoT);
	        }

	        // Concludo la TRANSAZIONE
    	    dbc.commitTransaction();

        	dbc.close();
	        super.close(dbc);
    	    done=true;
        	return dbr;
	    }
    	//gb 07/09/07 **************
	    catch(CariException ce){
    		ce.setISASRecord(null);
	      	throw ce;
	    }
    	//gb 07/09/07: fine **************
	    catch(DBRecordChangedException e){
    	    e.printStackTrace();
        	try{
	        	System.out.println("SocAssProgettoEJB.riaperturaPrg() => ROLLBACK");
			    dbc.rollbackTransaction();
	        }catch(Exception e1){
    		    throw new DBRecordChangedException("Errore eseguendo la rollback() - " +  e);
        	}
	        throw e;
    	}
    	catch(ISASPermissionDeniedException e){
        	e.printStackTrace();
        	try{
          		System.out.println("SocAssProgettoEJB.riaperturaPrg() => ROLLBACK");
          		dbc.rollbackTransaction();
        	}catch(Exception e1){
          		throw new ISASPermissionDeniedException("Errore eseguendo la rollback() - " +  e);
        	}
        	throw e;
    	}
    	catch(Exception e){
        	e.printStackTrace();
        	try{
          		System.out.println("SocAssProgettoEJB.riaperturaPrg() => ROLLBACK");
          		dbc.rollbackTransaction();
        	}catch(Exception e1){
          		throw new SQLException("Errore eseguendo una rollback() - " +  e1);
        	}
        	e.printStackTrace();
        	throw new SQLException("Errore eseguendo la riaperturaPrg() - " +  e);
    	}
    	finally{
   	    	if(!done){
   	        	try{
		  			dbc.close();
                  	super.close(dbc);
                }catch(Exception e2){
					System.out.println(e2);
				}
            }
    	}
	}

	// 21/07/10 m
	private ISASRecord getDbrVer(ISASConnection dbc, String cart, String prog) throws Exception
	{
		String sel = "SELECT * FROM ass_verifica" +
					" WHERE n_cartella = " + cart +
					" AND n_progetto = " + prog +
					" AND ver_definitiva = 'S'" +
					" AND ver_chiusa = 'S'";

		ISASRecord dbrV = dbc.readRecord(sel);
		return dbrV;
	}


public Vector CaricaBisogni(myLogin mylogin, Hashtable h1)
throws SQLException{
        ISASConnection dbc = null;
        ISASCursor dbcur=null;
        Vector v= new Vector();
        boolean done=false;
        try{
               dbc = super.logIn(mylogin);
               Hashtable h =new Hashtable();

                          String sel="SELECT tab_val, tab_descrizione "+
                          "FROM tab_voci where tab_cod='VAL1LIV' order by tab_val asc";
               System.out.println("CaricaBisogni su Frontespizio cartella sociale: "+sel);
               dbcur = dbc.startCursor(sel);


               while(dbcur.next()){
                  ISASRecord dbr = dbcur.getRecord();
                  if (dbr.get("tab_val")!=null && !((String)dbr.get("tab_val")).equals(""))
                  {

                    v.addElement(dbr);
//                          String val = (String)dbr.get("tab_val");
//                          String descrizione=(String)dbr.get("tab_descrizione");
//                          h.put((val.equals("#")?"0":val),descrizione);
//                          System.out.println("CaricaBisogni su Frontespizio cartella sociale: Aggiunti valori in hash: "+val+ " e "+descrizione);

                  }

                }
                dbcur.close();
                dbcur=null;
                dbc.close();
                super.close(dbc);
                done = true;
                return v;
	}catch(Exception e){
		System.out.println("************SocAssProgetto : exc="+e);
// 08/04/08 e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaBisogni()  ");
	}finally{
		if(!done){
			try{
				if (dbcur!=null)dbcur.close();

                                dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

	// 10/04/12: cntrl esistenza rec su ASS_ANAGRAFICA con invio AS
	private boolean esisteSegnAssAnag(ISASConnection dbc, ISASRecord dbr) throws Exception
	{
		String sel = "SELECT * FROM ass_anagrafica"
					+ " WHERE n_cartella = " + dbr.get("n_cartella").toString()
					+ " AND esito_contatto = '3'";
					
		ISASRecord recA = dbc.readRecord(sel);
		return (recA != null);
	}






	private void mySystemOut(String msg)
  	{
		if (myDebug)
			System.out.println(nomeEJB + ": " + msg);
	}

}

package it.caribel.app.sinssnt.bean;
		
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 25/10/06 - EJB di connessione alla procedura SINS Skvalutaz
//     scrittura nella tabella progetto
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.util.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*; //gb 02/11/06
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;  //gb 01/10/07
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils; // 28/02/12 m

import it.pisa.caribel.sinssnt.casi_adrsa.*; // Elisa Croci 11/06/09

public class SkValutazEJB extends SINSSNTConnectionEJB
{
	public SkValutazEJB() {}

	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	
	// 28/02/12 m
	it.pisa.caribel.sinssnt.casi_adrsa.EveUtils eveUtil = new it.pisa.caribel.sinssnt.casi_adrsa.EveUtils();
	private final String KEYCONF_PROCSQL = "CHIUSURA_PROCSQL";

	// 21/05/09 m.
	private ScaleVal gest_scaleVal = new ScaleVal();
	
	// 11/06/09
	private GestCasi gestore_casi = new GestCasi();

	// 08/10/07 --------------
	// 15/06/09 Elisa Croci: ho copiato questi valori anche in GestCasi, quindi se vengono modificati,
	// aggiornare anche GestCasi.
	String[] arrTabCont = {"ass_progetto", "skinf", "skmedico", "skfis", "skmedpal"};
	String[] arrFldCont = {"n_progetto", "n_contatto", "n_contatto", "n_contatto", "n_contatto"};
	String[] arrFldDtApe = {"ap_data_apertura", "ski_data_apertura", "skm_data_apertura", "skf_data", "skm_data_apertura"};
	String[] arrFldDtChiu = {"ap_data_chiusura", "ski_data_uscita", "skm_data_chiusura", "skf_data_chiusura", "skm_data_chiusura"};
	String[] arrTpOper = {"01", "02", "03", "04", "52"};
	// 08/10/07 --------------

	// 08/01/08
	private String msgNoDir = "Mancano i diritti per leggere il record";

	private void decodificaTabPrval(ISASConnection dbc, ISASRecord dbr) throws Exception
	{
		if (dbr.get("pr_tvcodice") == null){
			dbr.put("pr_tvdescr", "");
			return;
		}

		String tvCodice = ((Integer) dbr.get("pr_tvcodice")).toString();
		if (tvCodice.trim().equals(""))
		{
			dbr.put("pr_tvdescr", "");
			return;
		}
		String tvDescrizione = "";

		String mySel = "SELECT tv_descrizione" +
		" FROM tabprval" +
		" WHERE tv_codice = " + tvCodice;
		ISASRecord rec = dbc.readRecord(mySel);

		if (rec != null)
		{
			if (rec.get("tv_descrizione") != null)
				tvDescrizione = (String)rec.get("tv_descrizione");
		}
		dbr.put("pr_tvdescr", tvDescrizione);
	}

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

	public ISASRecord selectSkValCorrente(myLogin mylogin, Hashtable h) throws SQLException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");

		try
		{
			dbc = super.logIn(mylogin);
			
			String myselect = "SELECT * FROM progetto" +
						" WHERE n_cartella = " + strNAssistito +
						" AND pr_data_chiusura IS NULL";

			System.out.println("SkValutazEJB/selectSkValCorrente : " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				decodificaTabPrval(dbc, dbr);
				dbr.put("data_apertura_old", (java.sql.Date)dbr.get("pr_data"));
				putInfoFromCartellaIntoDbr(dbc, dbr, strNAssistito);
				leggiDiagnosi(dbc, dbr);
			}

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e){
			System.out.println("SkValutazEJB.selectSkValCorrente(): "+e);
			throw new CariException(msgNoDir, -2);
		}
		catch(Exception e)
		{
			System.out.println("SkValutazEJB/selectSkValCorrente(): "+e);
			throw new SQLException("Errore eseguendo la selectSkValCorrente() in SkValutazEJB");
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
				catch(Exception e1)
				{
					System.out.println(e1);
				}
			}
		}
	}

	//gb 26/02/07 m.: x DIAGNOSI --------------------------------------------------
	private void leggiDiagnosi(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		String cart = ((Integer)mydbr.get("n_cartella")).toString();
		Object dtApertura = (Object)mydbr.get("pr_data");
		Object dtChiusura = (Object)mydbr.get("pr_data_chiusura");

		Vector vdbr = new Vector();

		String critDtChius = "";
		if (dtChiusura != null)
			critDtChius = " AND data_diag <= " + formatDate(mydbc, dtChiusura.toString());

		String myselect = "SELECT * FROM diagnosi" +
						" WHERE n_cartella = " + cart +
						critDtChius +
						" ORDER BY data_diag DESC";

		System.out.println("SkValutaz/leggiDiagnosi - myselect=[" + myselect + "]");

		ISASRecord recD = mydbc.readRecord(myselect);

		if (recD != null) 
		{
			String dataIni = "";
			if (dtApertura != null)
				dataIni = dtApertura.toString();
		
			String dtIni = dataIni.substring(0,4) + dataIni.substring(5,7) + dataIni.substring(8,10);
			decodificaDiagn(mydbc, recD);
			decodificaOper(mydbc, recD);
			boolean isDataInContesto = checkData(recD, dtIni);
			costruisci5Rec(mydbc, recD, vdbr, (isDataInContesto?"C":"")+"0");
		}

		mydbr.put("diagn_associate", vdbr);			
	}// END leggiDiagnosi

	

	
	
	
	// Costruisce 5 record da quello letto: hanno tutti i campi del DB uguali, pi� le colonne fittizie del
	// codice e della descrizione, ognuno con i valori corrispondenti(rec1 con diag_1 e desc_diag_1, ecc).
	private void costruisci5Rec(ISASConnection mydbc, ISASRecord mydbr, Vector vett, String coloreCol) throws Exception
	{
		// aggiungo colonne fittizie al primo record
		mydbr.put("cod_alldiag", (String)mydbr.get("diag1"));		
		mydbr.put("desc_alldiag", (String)mydbr.get("desc_diag1"));
		mydbr.put("progr", "1");
		mydbr.put("dt_diag", (java.sql.Date)mydbr.get("data_diag"));
		mydbr.put("clr_column", "" + coloreCol);
		vett.addElement((ISASRecord)mydbr);

		// copio rec letto nei 4 nuovi record
		Hashtable h_1 = (Hashtable)mydbr.getHashtable();
		for (int j=2; j<6; j++) {
			ISASRecord dbr_i = mydbc.newRecord("diagnosi");
			copiaRec(h_1, dbr_i);	

			// aggiungo colonne fittizie agli altri 4 record
			dbr_i.put("cod_alldiag", (String)dbr_i.get("diag" + j));		
			dbr_i.put("desc_alldiag", (String)dbr_i.get("desc_diag" + j));
			dbr_i.put("progr", "" + j);
			dbr_i.put("dt_diag", "");
			vett.addElement((ISASRecord)dbr_i);
		}
	} // END costruisci6Rec

	private void copiaRec(Hashtable h_1, ISASRecord mydbr) throws Exception
	{
		Enumeration n_1 = h_1.keys();
		while (n_1.hasMoreElements()){
			String e = (String)n_1.nextElement();
			mydbr.put(e, h_1.get(e));
		}
	} // END copiaRec

	// 11/12/06: restituisce true se la data diagnosi e' >= della dataInizio del contesto
	private boolean checkData(ISASRecord mydbr, String dataI) throws Exception
	{
		DataWI dtDiag = new DataWI((java.sql.Date)mydbr.get("data_diag"));
		return dtDiag.isUguOSucc(dataI);
	} // END checkData

	private void decodificaDiagn(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		for (int k=1; k<6; k++){
			String cod = (String)mydbr.get("diag" + k);
			String desc = util.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi", cod, "diagnosi");
			mydbr.put("desc_diag" + k, desc);
		}
	}// END decodificaDiagn

	private void decodificaOper(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		String cod = (String)mydbr.get("cod_operatore");
		String desc = util.getDecode(mydbc, "operatori", "codice", cod,
				"nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nome_oper");
		mydbr.put("desc_oper", desc);
	}// END decodificaOper	
	//gb 26/02/07 m.: x DIAGNOSI -------------------------------------------------


	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws  SQLException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		String strNAssistito = (String)h.get("n_cartella");
		try{
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM progetto" +
			" WHERE n_cartella = " + strNAssistito +
			" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data"));

			System.out.println("SkValutazEJB: queryKey=[" + myselect + "]");
			ISASRecord dbr = dbc.readRecord(myselect);

			// Si decodificano alcuni campi della maschera a video.
			if (dbr != null)
			{
				decodificaTabPrval(dbc, dbr);
				dbr.put("data_apertura_old", (java.sql.Date)dbr.get("pr_data"));
			}
			// Si prendono data_apertura e data_chiusura dalla tabella 'cartella'
			if (dbr != null)
			{
				putInfoFromCartellaIntoDbr(dbc, dbr, strNAssistito);
			}

			if (dbr != null)
			{
				//gb 26/02/07 m.
				leggiDiagnosi(dbc, dbr);
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
			System.out.println("SkValutazEJB: queryKey - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SkValutazEJB: queryKey - Eccezione nella chiusura della connessione= " + e1);
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

			String myselect = "SELECT * FROM progetto" +
			" WHERE n_cartella = " + (String)h.get("n_cartella");

			System.out.println("SkValutazEJB: query=[" + myselect+ "]");

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("SkValutazEJB: query - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SkValutazEJB: query - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query


	//gb 02/11/06
	private boolean recordGiaPresente(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strDataAperturaNew = (String) h.get("pr_data");
		String strNCartella = (String) h.get("n_cartella");

		String mySel = "SELECT *" +
		" FROM progetto" +
		" WHERE n_cartella = " + strNCartella +
		" AND pr_data = " + formatDate(dbc, strDataAperturaNew);

		System.out.println("SkValutazEJB/recordGiaPresente = [" + mySel + "]");
		ISASRecord dbr = dbc.readRecord(mySel);

		if (dbr != null)
			return true; // record gi� presente in tabella
		else
			return false;
	}

	// 09/10/07 m.: aggiunto crit su skval diversa da quella in oggetto.
	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella = (String) h.get("n_cartella");
		String dt = (String) h.get("pr_data");
		//gb 04/10/07  dt = dt.substring(0,2) + dt.substring(3,5) + dt.substring(6,10);
		dt=dt.substring(8,10) + dt.substring(5,7) + dt.substring(0,4); //gb 04/10/07
		System.out.println("SkValutaz/dtApeMinoreMaxDtChius, data_apertura scheda(DDMMYYYY): " + dt);
		DataWI dataWIApertura = new DataWI(dt);

		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" +
		" FROM progetto" +
		" WHERE n_cartella = " + strNCartella +
		" AND pr_data <> " + formatDate(dbc, (String)h.get("pr_data")) + // 09/10/07 m.
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
		System.out.println("SkValutaz/dtApeMinoreMaxDtChius, massima data chiusura schede prec.(YYYYMMDD): " + dt);
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
			throw new Exception("SkValutaz/dtApeMinoreMaxDtChius: Errore in confronto date"); // Si � verificato un errore
		}
		else // (rit == 1)
			return false; // Ammissibile
	}


	public ISASRecord insert(myLogin mylogin, Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException,
	CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);

			//gb 02/11/06 *****************************************************
			if (recordGiaPresente(dbc, h))
			{
				String msg = "Attenzione: Record gi� presente!";
				throw new CariException(msg, -2);
			}

			if (dtApeMinoreMaxDtChius(dbc, h))
			{
				String msg = "Attenzione: Data antecedente a data chiusura di ultima Scheda valutazione chiusa!";
				throw new CariException(msg, -2);
			}
			// *****************************************************************
			
			ISASRecord dbr = dbc.newRecord("progetto");
			Enumeration n = h.keys();
			while (n.hasMoreElements()){
				String e = (String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM progetto" +
							" WHERE n_cartella = " + (String)h.get("n_cartella") +
							" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data"));
			
			System.out.println("SkValutazEJB/insert: " + myselect);
			dbr = dbc.readRecord(myselect);

			if (dbr != null)
			{
				// Elisa Croci 12/06/09 ******************************************
				Hashtable hash = dbr.getHashtable();
				String data_sospeso = null;
				if(hash.containsKey("pr_data_sospeso"))
					data_sospeso = hash.get("pr_data_sospeso").toString();
				String data_ripresa = null;
				if(hash.containsKey("pr_data_riattiva"))
					data_ripresa = hash.get("pr_data_riattiva").toString();
				
				System.out.println("data_sospeso == " + data_sospeso + " data_ripresa == " + data_ripresa);
				if(data_sospeso != null && !data_sospeso.trim().equals(""))
					SospendiCasoProgetto(dbc, h.get("n_cartella").toString(), h.get("pr_data").toString(), data_sospeso, data_ripresa);
				// ****************************************************************
				decodificaTabPrval(dbc, dbr);
				dbr.put("data_apertura_old", (java.sql.Date)dbr.get("pr_data"));// 08/01/08
			}

			if (dbr != null){
				leggiDiagnosi(dbc, dbr);
			}
			
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}
		catch(CariException ce)	
		{
			ce.setISASRecord(null);
			throw ce;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println("SkValutazEJB.insert(): Eccezione= " + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("SkValutazEJB.insert(): Eccezione= " + e);
			throw e;
		}
		catch(Exception e)
		{
			System.out.println("SkValutazEJB.insert(): Eccezione= " + e);
			throw new SQLException("Errore eseguendo una insert() - " +  e);
		}
		finally
		{
			if (!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2)
				{	
					System.out.println("SkValutazEJB.insert(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END insert


	private void cambiaDataAperturaAltreTabelle(ISASConnection dbc, String strNCartella,
						String strDataAperturaNew, String strDataAperturaOld) throws Exception
	{
		// 01/06/11 ---
		String[] arrNmTabToChange = new String[] {
			"progetto_cont", "progetto_psba", "progetto_sosp", "progetto_val", "puauvm", 
			"caso", "rt_segnalazione", "rt_presacarico", "rt_rivalutazione", "m_presacarico", 
			"m_rivalutazione", "caso_sospensione", "caso_ammidimi", "rt_pap", "rt_domdir",
			"rt_altro", "rp_pap", "rp_presacarico", "rp_rivalutazione", "rp_segnalazione",
			"rl_puauvm", "rl_presacarico", "rl_rivalutazione"
		};
	
		for (int k=0; k<arrNmTabToChange.length; k++)
			chngDtApeAltreTab(dbc, k, arrNmTabToChange[k], strNCartella, strDataAperturaNew, strDataAperturaOld);
		// 01/06/11 ---
		
/*** 01/06/11		
		// Aggiornamento pr_data in tabella progetto_cont
		String myUpd = "UPDATE progetto_cont SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - 1:" + myUpd);
		dbc.execSQL(myUpd);

		// Aggiornamento pr_data in tabella progetto_psba
		myUpd = "UPDATE progetto_psba SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - 2:" + myUpd);
		dbc.execSQL(myUpd);

		// 08/10/07: Aggiornamento pr_data in tabella progetto_sosp
		myUpd = "UPDATE progetto_sosp SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - 3:" + myUpd);
		dbc.execSQL(myUpd);

		// 08/10/07: Aggiornamento pr_data in tabella progetto_val
		myUpd = "UPDATE progetto_val SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - 4:" + myUpd);
		dbc.execSQL(myUpd);

		// 22/02/08: Aggiornamento pr_data in tabella puauvm
		myUpd = "UPDATE puauvm SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - 5:" + myUpd);
		dbc.execSQL(myUpd);
	
		// 12/06/09 Elisa Croci  ---------------------------------------------
		// AGGIORNAMENTO DATA PROGETTO NELLE TABELLE DEL FLUSSO ADRSA (CASI)
		myUpd = "UPDATE caso SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - caso:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE rt_segnalazione SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - rt_segnalazione:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE rt_presacarico SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - rt_presacarico:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE rt_rivalutazione SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - rt_rivalutazione:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE m_presacarico SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - m_presacarico:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE m_rivalutazione SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - m_rivalutazione:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE caso_sospensione SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - caso_sospensione:" + myUpd);
		dbc.execSQL(myUpd);
		
		myUpd = "UPDATE caso_ammidimi SET pr_data = " + formatDate(dbc,strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc,strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/cambiaDataAperturaAltreTabelle - caso_ammidimi:" + myUpd);
		dbc.execSQL(myUpd);
		// -----------------------------------------------------------------------
***/		
	}

	// 01/06/11
	private void chngDtApeAltreTab(ISASConnection dbc, int num, String nmTab,
			String strNCartella, String strDataAperturaNew, String strDataAperturaOld) throws Exception
	{
		// Aggiornamento pr_data in tabella 
		String myUpd = "UPDATE " + nmTab + " SET pr_data = " + formatDate(dbc, strDataAperturaNew) +
		" WHERE pr_data = " + formatDate(dbc, strDataAperturaOld) +
		" AND n_cartella = " + strNCartella;
		System.out.println("SkValutazEJB/chngDtApeAltreTab - " + num + ":" + myUpd);
		dbc.execSQL(myUpd);
	}
	
	
	
	
	
	private boolean dtApeMaggDteApeContatti(ISASConnection dbc, Hashtable h) throws Exception
	{
		// Ricerca tutti i record (n_cartella), tali che le date di apertura dei contatti
		// contenuti nella scheda valutazione, siano minori della data di apertura della
		// scheda valutazione stessa. Se tali contatti esistono si tratta di una situazione
		// non accettabile e il metodo ritorna 'true', se non esistono ritorna 'false'.

		// 08/10/07
		boolean trovato = false;
		int k = 0;
		while ((k < arrTabCont.length) && (!trovato)) 
		{
			trovato = existContApertiPrima(dbc, h, arrTabCont[k], arrFldCont[k], arrFldDtApe[k], arrTpOper[k]);
			k++;
		}
		
		return trovato;
	}

	// 08/10/07
	private boolean existContApertiPrima(ISASConnection dbc, Hashtable h,  
			String nomeTab, String nomeCampoCont, String nomeCampoDtApe, String tipoOp)throws Exception
	{
		String strNCartella = (String)h.get("n_cartella");
		String strDataAperturaNew = (String)h.get("pr_data");
		String strDataAperturaOld = (String)h.get("data_apertura_old");
		
		String mySel = "SELECT c.n_cartella" +
					" FROM progetto_cont c," +
					" " + nomeTab + " a" +	
					" WHERE c.n_cartella = " + strNCartella +
					" AND c.n_cartella = a.n_cartella" +
					" AND a." + nomeCampoDtApe + " < " + formatDate(dbc, strDataAperturaNew) +
					" AND c.prc_n_contatto = a." + nomeCampoCont +
					" AND c.pr_data = " + formatDate(dbc, strDataAperturaOld) +
					" AND c.prc_tipo_op = '" + tipoOp + "'";

		System.out.println("SkValutazEJB/existContApertiPrima: mySel =[" + mySel + "]");

		ISASCursor dbcur = dbc.startCursor(mySel);
		boolean contApertiPrima = ((dbcur != null) && (dbcur.getDimension() > 0));
		dbcur.close();
		// contApertiPrima=true: La data di apertura della Scheda valutazione � maggiore della
		// data di apertura di almeno un contatto (per ass. sociali, progetto).
		// contApertiPrima=false: La data di apertura della Scheda valutazione � minore o uguale della
		// data di apertura di tutti i contatti contenuti.
		System.out.println("SkValutazEJB/existContApertiPrima: contApertiPrima=" + contApertiPrima);
		return contApertiPrima;
	}


	public ISASRecord update(myLogin mylogin, ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		Hashtable h = null;
		boolean boolDtApeNewDiversaDtApeOld = false;

		try
		{
			dbc = super.logIn(mylogin);

			//gb 02/11/06 *****************************************************
			String strNCartella = (String)dbr.get("n_cartella");
			String strDataAperturaNew = (String) dbr.get("pr_data");
			String strDataAperturaOld = (String) dbr.get("data_apertura_old");
			h = dbr.getHashtable();
			if (!strDataAperturaNew.equals(strDataAperturaOld))
			{
				boolDtApeNewDiversaDtApeOld = true;
				if (recordGiaPresente(dbc, h))
				{
					String msg = "Attenzione: Record gi� presente!";
					throw new CariException(msg, -2);
				}

				if (dtApeMaggDteApeContatti(dbc, h))
				{
					String msg = "Attenzione: Data apertura scheda valutazione \n" +
					"maggiore di data apertura di primo contatto!";
					throw new CariException(msg, -2);
				}
			}

			if (dtApeMinoreMaxDtChius(dbc, h))
			{
				String msg = "Attenzione: Data antecedente a data chiusura di ultima Scheda valutazione chiusa!";
				throw new CariException(msg, -2);
			}
			// *****************************************************************

			//gb 01/10/07 *******
			dbc.startTransaction();
			String strDtChiusura = "";
			if (dbr.get("pr_data_chiusura")!=null && !((dbr.get("pr_data_chiusura")).toString()).equals(""))
			{
				strDtChiusura = (String)dbr.get("pr_data_chiusura");
				//gb 02/11/07			String strMotivoChiusura = (String)dbr.get("pr_motivi_val_ch");
				String strMotivoChiusura = (String)dbr.get("motivo_chius_contatti");
				// 05/10/12: su richiesta Lorenzo per nuovi motivi chiusura Veneto
				String strMotivoChiusuraProg =  (String)dbr.get("pr_motivi_val_ch");
				
				CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
				// Controlli data chiusura scheda valutazione con:
				// date prestazioni erogate della tabella interv.
				// date aper. e date chius. dei progetti e contatti.
				// date aper. e date chius. dei piani assitenziali.
				// date aper. dei piani accessi.
				// date aper. e date chius. degli obiettivi, interventi e verifiche x il progetto assist. sociale.
				String strMsgCheckDtCh = clCcec.checkDtChDaSkValGTDtApeDtCh(dbc, strNCartella, strDataAperturaOld, strDtChiusura);
				if(!strMsgCheckDtCh.equals(""))
					throw new CariException(strMsgCheckDtCh, -2);

				// 28/02/12 m: se configurato, si sostituisce con chiamata a storedProcedure SINS_CHIUSURA_PROGETTO_EJB
				Hashtable h_conf = eveUtil.leggiConf(dbc, (String)mylogin.getUser(), new String[]{KEYCONF_PROCSQL});
				if ((h_conf != null) && (h_conf.get(KEYCONF_PROCSQL) != null) 
				&& ((h_conf.get(KEYCONF_PROCSQL)).toString().equals("SI"))) {
					//28/02/12 m: chiamata a storedProcedure SINS_CHIUSURA_PROGETTO_EJB ---
					// parametri in ingresso: 
					//	p_cartella   VARCHAR, 
					//	p_data_str	VARCHAR,  
					//	p_motivo     VARCHAR
					// parametri restituiti: nm_tab_err OUT VARCHAR
					Vector vettParams = new Vector();
					vettParams.add(strNCartella);
					vettParams.add(strDtChiusura);
// 05/10/12			vettParams.add(strMotivoChiusura);
					vettParams.add(strMotivoChiusuraProg);
					
					int[] outTypes = new int[1];
					outTypes[0] = java.sql.Types.VARCHAR;
						
					Vector risu = dbc.callStoredFunction("sins_chiusura_progetto_ejb", vettParams, outTypes);
					System.out.println("********* SkValutazEJB: dopo chiamata alla STORED FUNCTION - risu=["+(risu!=null?risu.toString():"NULL")+"]******");												
					
					String msgErr = "Errore nella chiamata a stored procedure \"sins_chiusura_progetto_ejb\"";
					if ((risu == null) 
					|| ((risu != null) && (risu.elementAt(0) == null)))
						throw new CariException(msgErr, -2);
					if (!((String)risu.elementAt(0)).trim().equals("")) {
						String msgErrTab = "Errore nella chiusura della tabella: \"" 
							+ ((String)risu.elementAt(0)).trim() + "\"";
						throw new CariException(msgErrTab, -2);
					} else
						System.out.println("SkValutazEJB.update():  chiamata a stored procedure \"sins_chiusura_progetto_ejb\": OK");						
					// 28/02/12 m: chiamata a storedProcedure SINS_CHIUSURA_PROGETTO_EJB ---
				} else {					
					// Chiusure entit� che stanno sotto la scheda valutazione:
					// Contatti
					// Progetti di assist. sociale
					// Piani assistenziali
					// Piani accessi
					// Obiettivi, Interventi, Verifiche x il progetto assist. sociale.
					// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
					String strCodOperatore = (String)dbr.get("codice_operatore");
					//gb 02/11/07			clCcec.chiudoDaSkValInGiu(dbc, strNCartella, strDtChiusura, strMotivoChiusura, strCodOperatore);
					clCcec.chiudoDaSkValInGiu(dbc, strNCartella, strDataAperturaOld, strDtChiusura, strMotivoChiusura, strCodOperatore);
/** 27/04/10 sopostato nella classe "CartCntrlEtChiusure"
					// 11/06/09 Elisa Croci: se chiudo il progetto, concludo il caso associato, se esiste!
					gestore_casi.chiudiCasoProgetto(dbc, strNCartella, strDataAperturaOld, strDtChiusura, strCodOperatore);
**/				
					System.out.println("SkValutazEJB.update():  chiamata a \"it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure\": OK");	
				}
			}
			
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM progetto" +
			" WHERE n_cartella = " + (String)dbr.get("n_cartella") +
			" AND pr_data = " + formatDate(dbc, (String)dbr.get("pr_data"));
			System.out.println("SkValutazEJB/update: " + myselect);

			dbr = dbc.readRecord(myselect);

			// Si decodificano alcuni campi della maschera a video.
			if (dbr != null)
			{
				// Elisa Croci 12/06/09 ******************************************
				Hashtable hash = dbr.getHashtable();
				if(!hash.containsKey("pr_data_chiusura"))
				{
					String data_sospeso = null;
					if(hash.containsKey("pr_data_sospeso"))
						data_sospeso = hash.get("pr_data_sospeso").toString();
					String data_ripresa = null;
					if(hash.containsKey("pr_data_riattiva"))
						data_ripresa = hash.get("pr_data_riattiva").toString();
					
					System.out.println("data_sospeso == " + data_sospeso + " data_ripresa == " + data_ripresa);
					if(data_sospeso != null && !data_sospeso.trim().equals(""))
						SospendiCasoProgetto(dbc, h.get("n_cartella").toString(), h.get("pr_data").toString(), data_sospeso, data_ripresa);
				}
				// ****************************************************************
				
				decodificaTabPrval(dbc, dbr);
				dbr.put("data_apertura_old", (java.sql.Date)dbr.get("pr_data"));
			}

			if (dbr != null)
			{
				//gb 26/02/07 m.
				leggiDiagnosi(dbc, dbr);
			}

			if (boolDtApeNewDiversaDtApeOld)
				cambiaDataAperturaAltreTabelle(dbc, strNCartella, strDataAperturaNew, strDataAperturaOld);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		}
		//gb 02/11/06 **************
		catch(CariException ce){
			ce.setISASRecord(null);
			throw ce;
			// *************************
		}catch(DBRecordChangedException e){
			System.out.println("SkValutazEJB.update(): Eccezione= " + e);
			try
			{
				System.out.println("SkValutazEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("SkValutazEJB.update(): Eccezione= " + e);
			try{
				System.out.println("SkValutazEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e){
			System.out.println("SkValutazEJB.update(): Eccezione= " + e);
			try
			{
				System.out.println("SkValutazEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new SQLException("Errore eseguendo una rollback() - " +  e1);
			}
			throw new SQLException("Errore eseguendo una update() - " +  e);
		}finally
		{
			if (!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2)
				{
					System.out.println("SkValutazEJB.update(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END update


	private void cancProgetto(ISASConnection dbc, Hashtable h) throws Exception
	{
		String numCartella = ((Integer)h.get("n_cartella")).toString();
		String pr_data = ((java.sql.Date)h.get("pr_data")).toString();
		String myselect= "SELECT * FROM progetto" +
		" WHERE n_cartella = " + numCartella +
		" AND pr_data = " + formatDate(dbc, pr_data);

		//  debugMessage("ProgettoSinsEJB: cancProgetto - myselect=[" + myselect + "]");

		ISASRecord dbr = dbc.readRecord(myselect);
		if (dbr != null)
			dbc.deleteRecord(dbr);
	} // END cancProgetto

	private void cancProgettoCont(ISASConnection dbc, Hashtable h,ISASCursor dbcur)
	throws Exception
	{
		String numCartella = ((Integer)h.get("n_cartella")).toString();
		String pr_data = ((java.sql.Date)h.get("pr_data")).toString();
		String myselect= "SELECT * FROM progetto_cont" +
		" WHERE n_cartella = " + numCartella +
		" AND pr_data = " + formatDate(dbc, pr_data);

		//  debugMessage("ProgettoSinsEJB: cancProgetto - myselect=[" + myselect + "]");
		dbcur=dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				if (dbrec != null){
					String mysel="SELECT * FROM progetto_cont  "+
					" WHERE n_cartella = " + numCartella +
					" AND pr_data = " + formatDate(dbc,pr_data)+
					" AND prc_tipo_op='"+(String)dbrec.get("prc_tipo_op")+"'"+
					" AND prc_n_contatto="+(Integer)dbrec.get("prc_n_contatto");
					ISASRecord dbcont=dbc.readRecord(mysel);
					dbc.deleteRecord(dbcont);
				}
			}
		if (dbcur != null)
			dbcur.close();
	}

	private void cancProgettoPsba(ISASConnection dbc, Hashtable h,ISASCursor dbcur) throws Exception
	{
		String numCartella = ((Integer)h.get("n_cartella")).toString();
		String pr_data = ((java.sql.Date)h.get("pr_data")).toString();
		String myselect= "SELECT * FROM progetto_psba" +
		" WHERE n_cartella = " + numCartella +
		" AND pr_data = " + formatDate(dbc, pr_data);

		//  debugMessage("ProgettoSinsEJB: cancProgettoPsba - myselect=[" + myselect + "]");
		dbcur=dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				if (dbrec != null){
					String mysel="SELECT * FROM progetto_psba "+
					" WHERE n_cartella = " + numCartella +
					" AND pr_data = " + formatDate(dbc,pr_data)+
					" AND prba_data = " + formatDate(dbc,((java.sql.Date)dbrec.get("prba_data")).toString())+
					" AND prba_tbacodice="+(Integer)dbrec.get("prba_tbacodice");
					ISASRecord dbcont=dbc.readRecord(mysel);
					dbc.deleteRecord(dbcont);
				}
			}
		if (dbcur != null)
			dbcur.close();
	}

	private void cancStorVal(ISASConnection dbc, Hashtable h)
	throws Exception{
		String numCartella = (String)h.get("n_cartella").toString();
		String pr_data = (String)h.get("pr_data").toString();
		String myselect= "SELECT * FROM progetto_val" +
		" WHERE n_cartella = " + numCartella +
		" AND pr_data = " + formatDate(dbc, pr_data);

		debugMessage("ProgettoSinsEJB: cancStorVal - myselect=[" + myselect + "]");
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				if (dbrec != null){
					String mysel="SELECT * FROM progetto_val "+
					" WHERE n_cartella = " + numCartella +
					" AND pr_data = " + formatDate(dbc,pr_data)+
					" AND prv_data_valutaz="+formatDate(dbc,""+(java.sql.Date)dbrec.get("prv_data_valutaz"));
					ISASRecord dbcont=dbc.readRecord(mysel);
					dbc.deleteRecord(dbcont);
				}
			}
		if (dbcur != null)
			dbcur.close();
	}

	private void cancStorSosp(ISASConnection dbc, Hashtable h)
	throws Exception{
		String numCartella = (String)h.get("n_cartella").toString();
		String pr_data = (String)h.get("pr_data").toString();
		String myselect= "SELECT * FROM progetto_sosp" +
		" WHERE n_cartella = " + numCartella +
		" AND pr_data = " + formatDate(dbc, pr_data);

		//  debugMessage("ProgettoSinsEJB: cancStorSosp - myselect=[" + myselect + "]");
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				if (dbrec != null){
					String mysel="SELECT * FROM progetto_sosp "+
					" WHERE n_cartella = " + numCartella +
					" AND pr_data = " + formatDate(dbc,pr_data)+
					" AND prs_data_sospeso="+formatDate(dbc,""+(java.sql.Date)dbrec.get("prs_data_sospeso"));
					ISASRecord dbcont=dbc.readRecord(mysel);
					dbc.deleteRecord(dbcont);
				}
			}
		if (dbcur != null)
			dbcur.close();
	}

	// 22/02/08
	private void cancPuauvm(ISASConnection dbc, Hashtable h,ISASCursor dbcur)
	throws Exception{
		// 21/05/09 m. -----
		ISASCursor dbcur_1 = null;
		Hashtable h_1 = null;
		// 21/05/09 m. -----

		String numCartella = (String)h.get("n_cartella").toString();
		String pr_data = (String)h.get("pr_data").toString();
		String myselect= "SELECT * FROM puauvm" +
		" WHERE n_cartella = " + numCartella +
		" AND pr_data = " + formatDate(dbc, pr_data);

		//  debugMessage("ProgettoSinsEJB: cancPuauvm - myselect=[" + myselect + "]");
		dbcur=dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				if (dbrec != null){
					// 21/05/09 m. cancellazione PUAUVM_SCHEDE -----
					if (dbcur_1 != null)
						dbcur_1.close();
					dbcur_1 = null;
					h_1 = dbrec.getHashtable();
					gest_scaleVal.cancPuauvmSchede(dbc, h_1, dbcur_1);
					// 21/05/09 m. ----------------

					String mysel="SELECT * FROM puauvm"+
					" WHERE n_cartella = " + numCartella +
					" AND pr_data = " + formatDate(dbc,pr_data)+
					" AND pr_progr="+dbrec.get("pr_progr");
					ISASRecord dbcont=dbc.readRecord(mysel);						
					dbc.deleteRecord(dbcont);
				}
			}
		if (dbcur != null)
			dbcur.close();
	}

	/**
	 *	Esegue le seguenti operazioni:
	 *	1) cancellazione di tutti i record tabella progetto
	 *	2) cancellazione di tutti i record tabella progetto_cont
	 *	3) cancellazione di tutti i record tabella progetto_psba
	 *	4) cancellazione di tutti i record tabelle progetto_val
	 *   5) cancellazione di tutti i record tabelle progetto_sosp
	 *   6) cancellazione di tutti i record tabelle puauvm // 22/02/08
	 */
	public void delete(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur=null;
		boolean pV = true;
		try
		{
			Hashtable h = dbr.getHashtable();

			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			
			cancProgetto(dbc, h);
			System.out.println(">>>>>SkValutazEJB/delete: cancProgetto");
			cancProgettoCont(dbc, h,dbcur);
			System.out.println(">>>>>SkValutazEJB/delete: cancProgettoCont");
			cancProgettoPsba(dbc, h,dbcur);
			System.out.println(">>>>>SkValutazEJB/delete: cancProgettoPsba");
			cancStorVal(dbc, h);
			System.out.println(">>>>>SkValutazEJB/delete: cancStorVal");
			cancStorSosp(dbc, h);
			System.out.println(">>>>>SkValutazEJB/delete: cancStorSosp");
			cancPuauvm(dbc, h,dbcur); // 22/02/08
			System.out.println(">>>>>SkValutazEJB/delete: cancPuauvm");
			
			dbc.commitTransaction();

			if (dbcur != null)
				dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
		}
		catch(DBRecordChangedException e)
		{
			debugMessage("SkValutazEJB.delete(): Eccezione= " + e);
			try
			{
				System.out.println("SkValutazEJB.delete() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
			}
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("SkValutazEJB.delete(): Eccezione= " + e);
			try
			{
				System.out.println("SkValutazEJB.delete() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}
		catch(Exception e)
		{
			System.out.println("SkValutazEJB.delete(): Eccezione= " + e);
			try
			{
				System.out.println("SkValutazEJB.delete() => ROLLBACK");
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new SQLException("Errore eseguendo una rollback() - " +  e1);
			}
			throw new SQLException("Errore eseguendo una delete() - " +  e);
		}
		finally
		{
			if (!done)
			{
				try
				{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2)
				{
					System.out.println("SkValutazEJB.delete(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
			if (!pV) throw  new CariException("Fascicolo non vuoto: Non e' possibile cancellarlo!", 0);
			
		}
	}// END delete



	public ISASRecord esistenzaContatti(myLogin mylogin, Hashtable h) throws SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		try{
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM progetto_cont WHERE "+
			"n_cartella="+(String)h.get("n_cartella")+
			" AND pr_data="+formatDate(dbc,(String)h.get("pr_data"));
			System.out.println("SkValutazEJB/esistenzaContatti: " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}catch(Exception e){
			System.out.println("SkValutazEJB: esistenzaContatti - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una SkValutazEJB/esistenzaContatti()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SkValutazEJB/esistenzaContatti: - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END esistenzaContatti


	// Lettura schede valutaz chiuse
	public Vector query_skVal_chiuse(myLogin mylogin, Hashtable h) throws  SQLException	{
		String punto = "query_skVal_chiuse ";
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try{
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);

			String query = "select * from rm_skso where n_cartella = " +
			nCartella + " and pr_data_chiusura is not null";
			
			LOG.trace(punto +" query>> " +query+ "]");

			dbcur = dbc.startCursor(query);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			// decodifiche
			String prMotivoChiusura ="";
			for (Enumeration e = vdbr.elements(); e.hasMoreElements(); ){
				ISASRecord dbr_1 = (ISASRecord)e.nextElement();
				if (dbr_1 != null){
					prMotivoChiusura = ISASUtil.getValoreStringa(dbr_1, "pr_motivo_chiusura");
					dbr_1.put("desc_val_ap",
							(String)decodificaTabVoci(dbc, prMotivoChiusura, CostantiSinssntW.TAB_VAL_MOTIVO_CONCLUSIONE_FLUSSI_SIAD));
					 
				}
			}
			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("SkValutazEJB: query_skVal_chiuse - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SkValutazEJB: query_skVal_chiuse - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query_skVal_chiuse

	// controlla esistenza di scheda valutazione attiva
	public Hashtable query_esisteSkValAttiva(myLogin mylogin, Hashtable h) throws SQLException, CariException
	{
		Hashtable risu = new Hashtable();

		boolean done = false;
		ISASConnection dbc = null;

		String n_cartella = (String)h.get("n_cartella");

		try{
			dbc = super.logIn(mylogin);

			// 08/01/09	String myselect = "SELECT pr_data, pr_motivo_val_ap, pr_data_carico" +
			String myselect = "SELECT *" +			
			" FROM progetto" +
			" WHERE n_cartella = " + n_cartella +
			" AND pr_data_chiusura IS NULL";

			//	        System.out.println("SocAssDiagnosiEJB: query_esisteSkValAttiva - myselect=[" + myselect + "]");

			ISASRecord dbr = dbc.readRecord(myselect);

			risu.put("n_cartella", (String)n_cartella);
			if (dbr != null) {
				risu = dbr.getHashtable();
				risu.put("desc_val_ap",
						(String)decodificaTabVoci(dbc, (String)dbr.get("pr_motivo_val_ap"), "PRMOAP"));
				risu.put("pr_data",dbr.get("pr_data").toString());
				// simone 25/08/2014 non più necessaria la formattazione nel formato client x sinssnt_web2 
//				java.sql.Date dtSql = (java.sql.Date)dbr.get("pr_data");
//				DataWI myData = new DataWI(dtSql);
//				risu.put("pr_data", (myData!=null?myData.getFormattedString(0):""));
//				// 11/10/07 ---.
//				if (dbr.get("pr_data_carico") != null){
//					java.sql.Date dtSqlCarico = (java.sql.Date)dbr.get("pr_data_carico");
//					DataWI myDataCarico = new DataWI(dtSqlCarico);
//					risu.put("pr_data_carico", (myDataCarico!=null?myDataCarico.getFormattedString(0):""));
//				}
//				if (dbr.get("pr_data_valutaz") != null){
//					java.sql.Date dtSqlValut = (java.sql.Date)dbr.get("pr_data_valutaz");
//					DataWI myDataValut = new DataWI(dtSqlValut);
//					risu.put("pr_data_valutaz", (myDataValut!=null?myDataValut.getFormattedString(0):""));
//				}
				// 11/10/07 ---
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return risu;
		}// 08/01/08 ---
		catch(ISASPermissionDeniedException e){
			System.out.println("SkValutazEJB.query_esisteSkValAttiva(): "+e);
			// 08/01/09 throw new CariException(msgNoDir, -2);
			// 08/01/09 ---
			risu.put("retCode", new Integer(-2));
			return risu;
			// 08/01/09 ---
		}catch(Exception e){
			System.out.println("SkValutazEJB: query_esisteSkValAttiva - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("SkValutazEJB: query_esisteSkValAttiva - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END query_esisteSkValAttiva

//
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


	private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
		Hashtable htxt = new Hashtable();
		if (val_codice==null) return " ";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			return ((String)dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return " ";
		}
	}

	public ISASRecord query_massimaVal(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		ISASRecord dbr= null;
		try{
			dbc=super.logIn(mylogin);
			String sel="SELECT * FROM progetto_val p WHERE  "+
			"p.n_cartella="+(String)h.get("n_cartella")+" AND "+
			"p.pr_data="+formatDate(dbc,(String)h.get("pr_data"))+" and "+
			"p.prv_data_valutaz IN (SELECT MAX(a.prv_data_valutaz) FROM "+
			"progetto_val a WHERE p.pr_data=a.pr_data "+
			"AND p.n_cartella=a.n_cartella)";
			System.out.println("Progetto Sins=>Query_massima:"+sel);
			ISASCursor dbcur=dbc.startCursor(sel);
			while(dbcur.next()){
				dbr=dbcur.getRecord();
				dbr.put("desc_soggetto",decodifica("tabprval","tv_codice",(Integer)dbr.get("prv_tvcodice"),
						"tv_descrizione",dbc));
			}
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_massimaVal()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}


	// 09/10/07 m.: ctrl esistenza schede valutaz successive ad una certa data
	public Boolean query_checkSkValSuccessive(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String)h0.get("n_cartella");
			String dtRiferimento = (String)h0.get("dataRif");

			String myselect = "SELECT * FROM progetto" +
			" WHERE n_cartella = " + cart +
			" AND pr_data > " + formatDate(dbc, dtRiferimento);

			System.out.println("SkValutazEJB: query_checkSkValSuccessive - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(risu));
		} catch(Exception e1){
			System.out.println("SkValutazEJB.query_checkSkValSuccessive - Eccezione=[" + e1 + "]");
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

	// 10/10/07 m.: ctrl esistenza contatti non chiusi collegati ad una certa skVal
	public Boolean query_checkAllContChiusi(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean trovato = false;

		try 
		{
			dbc = super.logIn(mylogin);

			trovato = gestore_casi.query_checkAllContChiusi(dbc,h0);  // 15/06/09 Elisa Croci

			dbc.close();
			super.close(dbc);
			done = true;
			// se trovo anche 1 solo cont aperto -> ritorno false
			return (new Boolean(!trovato));
		} 
		catch(Exception e1)
		{
			System.out.println("SkValutazEJB.query_checkAllContChiusi - Eccezione=[" + e1 + "]");
			return (Boolean)null;
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
				catch(Exception e2)
				{   System.out.println("SkValutazEJB.query_checkAllContChiusi - Eccezione=[" + e2 + "]");   }
			}
		}
	}

	// 14/04/08 m.: ctrl esistenza di 1 solo contatto non chiuso collegato 
	//	ad una certa skVal
	public Boolean query_checkUnicoContAperto(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean trovato = false;

		try 
		{
			dbc = super.logIn(mylogin);
			
			trovato = gestore_casi.query_checkUnicoContAperto(dbc,h0);  // 15/06/09 Elisa Croci
			
			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(trovato));
		} 
		catch(Exception e1)
		{
			System.out.println("SkValutazEJB.query_checkUnicoContAperto - Eccezione=[" + e1 + "]");
			return (Boolean)null;
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
				catch(Exception e2){   System.out.println("SkValutazEJB.query_checkUnicoContAperto -- " + e2);   }
			}
		}
	}
	
	// 12/06/09 Elisa Croci
	private void SospendiCasoProgetto(ISASConnection dbc, String n_cartella, String pr_data, String dataInizioSosp, String dataFineSosp)
	{
		System.out.println("SkValutazEJB/SospendiCasoProgetto() CARTELLA " + n_cartella + ", DATA PROGETTO" + pr_data + ", DATA INIZIO SOSP " + dataInizioSosp);

		Hashtable h = new Hashtable();
		
		h.put("n_cartella", n_cartella);
		h.put("pr_data", pr_data);
		
		// nel progetto e' un campo di testo mentre nel caso e' una combobox, metto sempre la 
		// motivazione "altro"!
		h.put("motivo","9");  
		
		h.put("dt_inizio", dataInizioSosp);
		if(dataFineSosp != null)
			h.put("dt_fine", dataFineSosp);
		else h.put("dt_fine", "");
		
		try 
		{
			// prendo i casi non conclusi associati al progetto che voglio sospendere
			String mysel = "SELECT * FROM caso WHERE n_cartella = " + n_cartella +
						" AND pr_data = " + formatDate(dbc, pr_data) +
						" AND dt_conclusione IS NULL ";
		
			System.out.println("SkValutazEJB/SospendiCasoProgetto() CASO: " + mysel);
			ISASCursor cur = dbc.startCursor(mysel);
			Vector v = cur.getAllRecord();
			cur.close();
			// Tengo conto solo del caso UVM attivo, se esiste!
			for(int i = 0; i < v.size(); i++)
			{
				ISASRecord rec = (ISASRecord) v.get(i);
				
				if(rec != null)
				{
					int tipoCaso = Integer.parseInt(rec.get("origine").toString());
					int statoCaso = Integer.parseInt(rec.get("stato").toString());
					if(tipoCaso == GestCasi.CASO_UVM && statoCaso == GestCasi.STATO_ATTIVO)
					{
						String idcaso = rec.get("id_caso").toString();
						h.put("id_caso", idcaso);
						
						// prendo i casi non conclusi associati al progetto che voglio chiudere
						String select = "SELECT * FROM caso_sospensione WHERE n_cartella = " + n_cartella +
									" AND pr_data = " + formatDate(dbc, pr_data) +
									" AND id_caso = " + idcaso +
									" AND dt_inizio =  " + formatDate(dbc, dataInizioSosp);
						
						System.out.println("SkValutazEJB/SospendiCasoProgetto() SOSP: " + select);
						ISASRecord recCaso = dbc.readRecord(select);
						
						String selectProgr = " SELECT MAX(progr) progr FROM caso_sospensione WHERE" +
											" pr_data = " + formatDate(dbc, pr_data) +
											" AND id_caso = " + idcaso;
				
						System.out.println("SkValutazEJB/SospendiCasoProgetto() PROGR: " + selectProgr);
						ISASRecord recProgr = dbc.readRecord(selectProgr);
						if(recProgr == null)
							h.put("progr", new Integer(1));
						else
						{
							if(recCaso == null)
							{
								int newProgr = 1;
								if(recProgr.getHashtable().containsKey("progr") && recProgr.get("progr") != null)
								    newProgr = Integer.parseInt(recProgr.get("progr").toString()) + 1;
								
								System.out.println("NEW PROGR == " + newProgr);
								h.put("progr", new Integer(newProgr));
							}
							else 
								h.put("progr", recProgr.get("progr"));
						}
					
						if(recCaso == null) 
							recCaso = dbc.newRecord("caso_sospensione");
						
						System.out.println("H DELLA SOSPENSIONE: " + h.toString());
						Enumeration en = h.keys();
						while(en.hasMoreElements())
						{
							String ch = en.nextElement().toString();
							recCaso.put(ch, h.get(ch));
						}
							
						System.out.println("Record che aggiorno in SospendiCasoProgetto(): " + recCaso.getHashtable().toString());
						dbc.writeRecord(recCaso);
					}
				}
			}
		} 
		catch (Exception e) 
		{	System.out.println("SkValutazEJB/SospendiCasoProgetto " + e);	}
	}
	


	

// nuovi metodi per specchietto annuale riassuntivo degli accessi specialisti (x Veneto)

private void stampa(String msg) {
	System.out.println("SkValutazEJB: "+msg);		
}














private String getMese(int m) {
String ret = "";
switch (m){
case 1: ret = "Gennaio";break;
case 2: ret = "Febbraio";break;
case 3: ret = "Marzo";break;
case 4: ret = "Aprile";break;
case 5: ret = "Maggio";break;
case 6: ret = "Giugno";break;
case 7: ret = "Luglio";break;
case 8: ret = "Agosto";break;
case 9: ret = "Settembre";break;
case 10: ret = "Ottobre";break;
case 11: ret = "Novembre";break;
case 12: ret = "Dicembre";break;		
}
return ret;
}

private String getInizioMese(int m, String anno) {
String ret = "";
switch (m){
case 1: ret = "to_date('01-01-"+anno+"','dd-mm-yyyy')";break; 
case 2: ret = "to_date('01-02-"+anno+"','dd-mm-yyyy')";break;
case 3: ret = "to_date('01-03-"+anno+"','dd-mm-yyyy')";break;
case 4: ret = "to_date('01-04-"+anno+"','dd-mm-yyyy')";break;
case 5: ret = "to_date('01-05-"+anno+"','dd-mm-yyyy')";break;
case 6: ret = "to_date('01-06-"+anno+"','dd-mm-yyyy')";break;
case 7: ret = "to_date('01-07-"+anno+"','dd-mm-yyyy')";break;
case 8: ret = "to_date('01-08-"+anno+"','dd-mm-yyyy')";break;
case 9: ret = "to_date('01-09-"+anno+"','dd-mm-yyyy')";break;
case 10: ret = "to_date('01-10-"+anno+"','dd-mm-yyyy')";break;
case 11: ret = "to_date('01-11-"+anno+"','dd-mm-yyyy')";break;
case 12: ret = "to_date('01-12-"+anno+"','dd-mm-yyyy')";break;		
}
return ret;
}

private String getFineMese(int m, String anno) {
String ret = "";

switch (m){
case 1: ret = "to_date('31-01-"+anno+"','dd-mm-yyyy')";break;
case 2: ret = "to_date('"+((isBisestile(anno))?"29":"28")+"-02-"+anno+"','dd-mm-yyyy')";break;
case 3: ret = "to_date('31-03-"+anno+"','dd-mm-yyyy')";break;
case 4: ret = "to_date('30-04-"+anno+"','dd-mm-yyyy')";break;
case 5: ret = "to_date('31-05-"+anno+"','dd-mm-yyyy')";break;
case 6: ret = "to_date('30-06-"+anno+"','dd-mm-yyyy')";break;
case 7: ret = "to_date('31-07-"+anno+"','dd-mm-yyyy')";break;
case 8: ret = "to_date('31-08-"+anno+"','dd-mm-yyyy')";break;
case 9: ret = "to_date('30-09-"+anno+"','dd-mm-yyyy')";break;
case 10: ret = "to_date('31-10-"+anno+"','dd-mm-yyyy')";break;
case 11: ret = "to_date('30-11-"+anno+"','dd-mm-yyyy')";break;
case 12: ret = "to_date('31-12-"+anno+"','dd-mm-yyyy')";break;		
}
return ret;
}
private boolean isBisestile(String anno) {

if (anno.substring(2,4).equals("00")) return ((Integer.parseInt(anno.substring(0,2)) % 4) == 0);
else return ((Integer.parseInt(anno.substring(2,4)) % 4) == 0);

}






}// END class

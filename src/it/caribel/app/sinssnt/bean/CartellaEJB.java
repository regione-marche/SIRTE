package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 11/05/2000 - EJB di connessione alla procedura SINS Tabella Cartella
//
// paolo ciampolini
//
// completamente rifatto: 09/08/2002 Barbara Giannattasio
// ============================================================================

import it.caribel.app.common.ejb.CartellaBaseEJB;
import it.caribel.app.sinssnt.bean.nuovi.SegnalazioniEJB;
import it.caribel.app.sinssnt.comuni_nascita.ComuniNascita;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.zk.util.UtilForBinding;
import it.insielmercato.anagraficaHL7.AnagUtils;
import it.insielmercato.anagraficaHL7.AnagraConstants;
import it.insielmercato.anagraficaHL7.model.Esenzione;
import it.insielmercato.anagraficaHL7.model.Medico;
import it.insielmercato.anagraficaHL7.model.PatientResponse;
import it.insielmercato.anagraficaHL7.model.QueryResponse;
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
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class CartellaEJB extends CartellaBaseEJB  {

public CartellaEJB() {}


	@SuppressWarnings("rawtypes")
	public ISASRecord update(myLogin mylogin, ISASRecord dbr, Vector hv) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		boolean done = false;
		//String codice = null;
		// Jessy 14-01-2005
		// String data="";
		ISASConnection dbc = null;
		// System.out.println("in update cartella hashtable="+dbr.getHashtable().toString());

		// 28/04/10 --
		LOG.info("mylogin: user=[" + mylogin.getUser() + "] - pwd = " + mylogin.getPassword());
		// 28/04/10 --
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			// elisa b 17/04/12: gestione data scelta medico
			boolean infoMedicoMod = false;
			if ((dbr.get("info_medico_mod") != null) && (dbr.get("info_medico_mod").equals("S")))
				infoMedicoMod = true;

			ISASRecord dbrEx = ControlloEsistenza(dbc, dbr);
			if (dbrEx != null) {
				dbrEx.put("inserito", "S");
				dbr = dbrEx;
			} else {
				int max = 0;
				String mysel = "Select * from cartella where " + "n_cartella=" + dbr.get("n_cartella").toString();
				ISASRecord dbrmax = dbc.readRecord(mysel);
				if (dbrmax.get("nmax_contatti") != null) {
					String contatti = ((Integer) dbrmax.get("nmax_contatti")).toString();
					max = (new Integer(contatti)).intValue();
				} else
					max = 0;
				dbr.put("nmax_contatti", (new Integer(max)).toString());
				dbc.writeRecord(dbr);
				String data_chiusura = "";
				if (dbr.get("data_chiusura") != null && !((dbr.get("data_chiusura")).toString()).equals("1000-01-01"))
					data_chiusura = (String) dbr.get("data_chiusura").toString();
				String cartella = dbr.get("n_cartella").toString();
				String motivo_chiusura = (String) dbr.get("motivo_chiusura");
				
				if (data_chiusura != null && !(data_chiusura.equals(""))) {
					// SE LA DATA CHIUSURA E' PRESENTE VADO A INSERIRLA IN TUTTI
					// I CONATTI E SU CONTSAN il cod_usl viene utilizzato per il log in caso di chiusura forzata
					chiudiCartella(mylogin, dbr, dbc, data_chiusura, cartella, motivo_chiusura, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CODUSL));
				}

				// System.out.println("Ho scritto il record!");
				//codice = (String) dbr.get("n_cartella");
				String myselect = "Select * from cartella where " + "n_cartella=" + cartella;
				dbr = dbc.readRecord(myselect);
				// Jessy 14-01-2005
				// data=""+(java.sql.Date)dbr.get("data_apertura");

				// PER CAMPI DECODIFICA:
				// String w_codice="";
				// String w_descr="";
				// String w_select="";
				// ISASRecord w_dbr=null;
				decodificaDatiCartella(dbr, dbc);
				Enumeration enum2 = hv.elements();
				Hashtable htv = (Hashtable) enum2.nextElement();
				dbr.put("desc_area_res",
						decodifica("areadis", "codice", (String) htv.get("areadis"), "descrizione", dbc));
				dbr.put("desc_area_dom",
						decodifica("areadis", "codice", (String) htv.get("dom_areadis"), "descrizione", dbc));
				dbr.put("comresdescr", decodifica("comuni", "codice", (String) htv.get("citta"), "descrizione", dbc));
				dbr.put("comdomdescr",
						decodifica("comuni", "codice", (String) htv.get("dom_citta"), "descrizione", dbc));
				dbr.put("meddescr", decodifica("medici", "mecodi", (String) htv.get("cod_med"), "mecogn", dbc));
				// 15/11/07
				dbr.put("comreperibdescr",
						decodifica("comuni", "codice", (String) htv.get("comune_rep"), "descrizione", dbc));
				dbr.put("desc_area_reperib",
						decodifica("areadis", "codice", (String) htv.get("areadis_rep"), "descrizione", dbc));

				/*
				 * elisa b 17/04/12 : se dal client arriva l'informazione della
				 * modifica del medico e/o della data in cui il medico e'
				 * scelto, si deve aggiornare o inserire un record che ha per
				 * data variazione la data di scelta del medico ed,
				 * eventualmente, aggiornare il valore del medico e della data
				 * in record successivi.
				 */
				if (infoMedicoMod) {
					gestioneMedico(dbc, htv, cartella, (String) dbr.get("cod_operatore"));
				}

				String selan = "SELECT * FROM anagra_c" +
						" WHERE n_cartella=" + cartella + 
						" AND data_variazione = " + formatDate(dbc, (String) htv.get("data_variazione"));

				ISASRecord dbrAn = dbc.readRecord(selan);
				if (dbrAn != null) // aggiorno
					LOG.debug("aggiorno");
				else { // inserisco
					LOG.debug("Nuovo record");
					dbrAn = dbc.newRecord("anagra_c");
				}
				dbrAn.put("n_cartella", cartella);
				Enumeration n2 = htv.keys();
				while (n2.hasMoreElements()) {
					String elem2 = (String) n2.nextElement();

					dbrAn.put(elem2, htv.get(elem2));
				}
				dbc.writeRecord(dbrAn);
				// Jessy 14-01-2005
				// 02/08/2012 jessy la data variazione viene controllata da
				// client
				// 02/08/2012 jessyString data_var=ControlloDataMin(codice,
				// data, dbc);
				// dbr.put("data_variazione",data_var);
				LOG.debug("Rientro in update!!!");
				// Fine Jessy
				// Jessy 12-01-2005 deve essere sempre mostrato l'ultimo record
				// su anagra_c
				String myselect3 = "SELECT a.* FROM anagra_c a WHERE a.n_cartella = " + cartella
						+
						// Jessy
						// 14-01-2005" and data_variazione="+formatDate(dbc,(String)htv.get("data_variazione"));
						" and a.data_variazione IN (SELECT MAX (data_variazione) FROM anagra_c  "
						+ "WHERE n_cartella=a.n_cartella)";
				// System.out.println("leggo max anagra_c="+myselect3);
				dbrAn = dbc.readRecord(myselect3);
				// System.out.println("prima di put data variazione "+dbrAn);
				dbr.put("data_variazione", dbrAn.get("data_variazione"));
				// System.out.println("dopo..");
			}
			dbc.commitTransaction();
			done = true;
			// System.out.println("DBR in Update=>"+dbr.getHashtable().toString());
			return dbr;
		}
		// gb 24/09/07 **************
		catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		}
		// gb 24/09/07: fine **************
		catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			throw newEjbException("Errore eseguendo una update() - ", e1);
		} finally {
			if (!done) {
				rollback_nothrow("CartellaEJB.update", dbc);
			}
			logout_nothrow("CartellaEJB.update", dbc);
		}
	}


//	public void decodificaDatiCartella(ISASRecord dbr, ISASConnection dbc) throws ISASMisuseException, Exception {
//		if (dbr != null) {
//			Hashtable h1 = dbr.getHashtable();
//			if (h1.get("cod_com_nasc") != null && !((String) h1.get("cod_com_nasc")).equals("")) {
//				// Simone 26/03/13
//				// w_codice = (String)h1.get("cod_com_nasc");
//				// w_select =
//				// "SELECT * FROM comuni WHERE codice='"+w_codice+"'";
//				// w_dbr=dbc.readRecord(w_select);
//				// dbr.put("desc_com_nasc", w_dbr.get("descrizione"));
//				// dbr.put("xcf", w_dbr.get("cod_fis"));
//				dbr.put("desc_com_nasc", ComuniNascita.getDecodeComuneNascita(dbc, dbr.get("cod_com_nasc"),
//						dbr.get("data_nasc")));
//				dbr.put("xcf",
//						ComuniNascita.getCampo(dbc, "cod_fis", dbr.get("cod_com_nasc"), dbr.get("data_nasc")));
//
//			} else {
//				dbr.put("desc_com_nasc", "");
//				dbr.put("xcf", "");
//			}
//			decodificaOper(dbc, dbr, "cod_operatore", "desc_operat");
//			dbr.put("des_cittadin",
//					decodifica("cittadin", "cd_cittadin", (String) h1.get("cittadinanza"), "des_cittadin", dbc));
//			// 14/11/07: nazionalita
//			dbr.put("des_nazionalita",
//					decodifica("cittadin", "cd_cittadin", (String) h1.get("nazionalita"), "des_cittadin", dbc));
//			// distretto 21/09/04 bargi
//			// dbr.put("des_distr",decodifica("distretti","cod_distr",(Integer)h1.get("cod_distretto"),"des_distr",dbc));
//		}
//	}
	
	
	protected void chiudiCartella(myLogin mylogin, ISASRecord dbr, ISASConnection dbc, String data_chiusura,
			String cartella, String motivo_chiusura, String cod_usl) throws Exception, CariException,
			ISASMisuseException, DBMisuseException, DBSQLException {
		// gb 24/09/07
		// AggiornaData("contatti",cartella,"data_chiusura",data_chiusura,"data_contatto",dbc);
//		dbr.put(CostantiSinssntW.FORZA_CHIUSURA, "SI");
		LOG.info("Chiusura della cartella: "+cartella+ " con cod_usl: " + cod_usl + 
				" in data: "+ data_chiusura + " per: "+motivo_chiusura + 
				" forzata : "+dbr.getHashtable().containsKey(CostantiSinssntW.FORZA_CHIUSURA));
		CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure(dbr.getHashtable().containsKey(CostantiSinssntW.FORZA_CHIUSURA),
				cod_usl, data_chiusura, motivo_chiusura); // gb 25/09/07

		// gb 28/09/07: Controlli data chiusura cartella con:
		// date prestazioni erogate della tabella interv.
		// date aper. e date chius. della cheda valutazione.
		// date aper. e date chius. dei progetti e contatti.
		// date aper. e date chius. dei piani assitenziali.
		// date aper. dei piani accessi.
		// date aper. e date chius. degli obiettivi, interventi e verifiche.
		String strMsgCheckDtCh = clCcec.checkDtChDaCartGTDtApeDtCh(dbc, cartella, data_chiusura, dbr.getHashtable()
				.containsKey(CostantiSinssntW.FORZA_CHIUSURA));
		if (!strMsgCheckDtCh.equals("")){
			if(!dbr.getHashtable().containsKey(CostantiSinssntW.FORZA_CHIUSURA)){
				throw new CariException(strMsgCheckDtCh, -2);
			}else{
				loggaErroreInChiusuraForzata(new CariException(strMsgCheckDtCh, -2), dbc, cod_usl, data_chiusura, motivo_chiusura);
			}
		}
		// 28/02/12 m: se configurato, si sostituisce con chiamata a
		// storedProcedure SINS_CHIUSURA_ANAGRAFICA_EJB
		Hashtable h_conf = eveUtil.leggiConf(dbc, (String) mylogin.getUser(), new String[] { KEYCONF_PROCSQL });
		// System.out.println("************ CartellaEJB: h_conf="+h_conf.toString()+" ******");
		if ((h_conf != null) && (h_conf.get(KEYCONF_PROCSQL) != null)
				&& ((h_conf.get(KEYCONF_PROCSQL)).toString().equals("SI"))) {
			// 28/02/12 m: chiamata a storedProcedure
			// SINS_CHIUSURA_ANAGRAFICA_EJB ---
			// parametri in ingresso:
			// p_cartella VARCHAR,
			// p_data_str VARCHAR,
			// p_motivo VARCHAR
			// parametri restituiti: nm_tab_err OUT VARCHAR
			Vector<String> vettParams = new Vector<String>();
			vettParams.add(cartella);
			vettParams.add(data_chiusura);
			vettParams.add(motivo_chiusura);

			int[] outTypes = new int[1];
			outTypes[0] = java.sql.Types.VARCHAR;
			// System.out.println("******* CartellaEJB: vettParams="+vettParams.toString()+" ******");
			// System.out.println("********** CartellaEJB: chiamo la STORED FUNCTION ******");

			Vector risu = dbc.callStoredFunction("sins_chiusura_anagrafica_ejb", vettParams, outTypes);
			LOG.info("********* CartellaEJB: dopo chiamata alla STORED FUNCTION - risu=["
					+ (risu != null ? risu.toString() : "NULL") + "]******");

			String msgErr = "Errore nella chiamata a stored procedure \"sins_chiusura_anagrafica_ejb\"";
			if ((risu == null) || ((risu != null) && (risu.elementAt(0) == null)))
				throw new CariException(msgErr, -2);
			if (!((String) risu.elementAt(0)).trim().equals("")) {
				String msgErrTab = "Errore nella chiusura della tabella: \"" + ((String) risu.elementAt(0)).trim()
						+ "\"";
				throw new CariException(msgErrTab, -2);
			} else
				LOG.info("CartellaEJB.update():  chiamata a stored procedure \"sins_chiusura_anagrafica_ejb\": OK");
			// 28/02/12 m: chiamata a storedProcedure
			// SINS_CHIUSURA_ANAGRAFICA_EJB ---
		} else {
			// gb 01/10/07: Chiusure entit� che stanno sotto la cartella:
			// Scheda valutazione
			// Contatti
			// Progetti di assist. sociale
			// Piani assistenziali
			// Piani accessi
			// Obiettivi, Interventi, Verifiche
			// Rimozione record da agendant_interv e agendant_intpre con date
			// successive a data chiusura
			String strCodOperatore = (String) dbr.get("cod_operatore");
			clCcec.chiudoSkSO(mylogin, dbc, cartella, data_chiusura, motivo_chiusura, dbr.getHashtable().containsKey(CostantiSinssntW.FORZA_CHIUSURA));
			clCcec.chiudoDaCartellaInGiu(dbc, cartella, data_chiusura, motivo_chiusura, strCodOperatore);
			LOG.info("CartellaEJB.update():  chiamata a \"it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure\": OK");
		}
	}

	/**CJ 20/04/2007 Aggiunto il controllo anche su il codice fiscale, oltre al comune nascita, cognome,
	 * nome e data nascita perch� a Massa si sono presentati casi di omonimia e non riuscivano ad inserire
	 * il secondo nominativo
	 * @throws DBSQLException 
	 * @throws DBMisuseException 
	 * @throws ISASPermissionDeniedException 
	 * @throws ISASMisuseException 
	 */
	private ISASRecord ControlloEsistenza(ISASConnection dbc, ISASRecord dbr) throws SQLException, ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException {

		String datavar = "";
		if(dbr.get("data_nasc") instanceof String)
			datavar = (String) dbr.get("data_nasc");// ).toString();
		else
			datavar = UtilForBinding.getValueForIsas(new java.sql.Date(((Date)dbr.get("data_nasc")).getTime()));
		// Jessica 04/03/04 controllo che non esista una cartella con gli stessi dati
		String selCart = "Select * from cartella where ((";
		String scr = (String) (dbr.get("cognome"));
		scr = duplicateChar(scr, "'");
		selCart = selCart + " cognome='" + scr + "'";
		String scr1 = (String) (dbr.get("nome"));
		scr1 = duplicateChar(scr1, "'");
		selCart = selCart + " AND nome='" + scr1 + "'";
		String scr2 = formatDate(dbc, datavar);
		selCart = selCart + " AND data_nasc=" + scr2;
		String scr3 = (String) (dbr.get("cod_com_nasc"));
		selCart = selCart + " AND cod_com_nasc='" + scr3 + "' ";
		String scr4 = (String) (dbr.get("cod_fisc"));
		selCart = selCart + " AND cod_fisc='" + scr4 + "') ";
		// CONTROLLO SE ESITE LA CARTELLA TRAMITE IL CODICE REGIONALE
		// 13/10/2005 aggiunto il controllo che il codice regionale sia diverso
		// da tutti zero
		if (dbr.get("cod_reg") != null && !(((String) dbr.get("cod_reg")).equals(""))
				&& !(((String) dbr.get("cod_reg")).startsWith("0000")) 
				&& !(((String) dbr.get("cod_reg")).equals("0")))
			selCart = selCart + " OR (cod_reg='" + (String) dbr.get("cod_reg") + "'))";
		else
			selCart = selCart + ")";
		selCart = selCart + " AND n_cartella<>" + dbr.get("n_cartella").toString();
		LOG.debug("Controllo esistenza****" + selCart);
		dbr = dbc.readRecord(selCart);
		return dbr;

	}

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			// cancellazione dati anagrafici
			// System.out.println("prima di deleteAnagra");
			deleteAnagra(dbc, (Integer) dbr.get("n_cartella"));
			// System.out.println("dopo deleteAnagra");
			// cancellazione cartella
			String myselect = "SELECT * FROM cartella WHERE " + "n_cartella=" + (Integer) dbr.get("n_cartella");
			System.out.println("Select deleteCartella " + myselect);
			ISASRecord dbrc = dbc.readRecord(myselect);
			dbc.deleteRecord(dbrc);

			dbc.commitTransaction();
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			throw newEjbException("Errore eseguendo una delete() - " , e1);
		} finally {
			if (!done) {
				rollback_nothrow("CartellaEJB.delete", dbc);
			}
			logout_nothrow("CartellaEJB.delete", dbc);
		}
	}

public void deleteAnagra(ISASConnection dbc,Integer n_cartella)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	ISASCursor dbcur = null; // 01/02/13 mv
	
	try{
            System.out.println("In deleteAnagra");
	    String myselect="SELECT * FROM anagra_c WHERE "+
	        "n_cartella="+n_cartella.toString();
	    System.out.println("Select deleteAnagra "+myselect);

/* 01/02/13 mv
            ISASRecord dbr=dbc.readRecord(myselect);
   		  System.out.println(" dopo readRecord dbr:"+dbr.getHashtable().toString());
		  if (dbr!=null){
                  dbc.deleteRecord(dbr);
		  System.out.println("dopo deleteRecord deleteAnagra");
		  } else System.out.println("non esistono rec deleteAnagra");
*/		  
		
		// 01/02/13 mv: deve cancellare tutti i record su ANAGRA_C associati a n_cartella
		dbcur = dbc.startCursor(myselect);
		if (dbcur != null) {
			String sel_1 = "";
			while (dbcur.next()) {
				ISASRecord dbr = (ISASRecord)dbcur.getRecord();
				java.sql.Date dtVar = (java.sql.Date)dbr.get("data_variazione");
				sel_1 = myselect + " AND data_variazione = " + formatDate(dbc, dtVar.toString());
				ISASRecord dbr_1 = (ISASRecord)dbc.readRecord(sel_1);
				dbc.deleteRecord(dbr_1);
				LOG.debug(" - dopo deleteRecord");
			}
			dbcur.close();
		} else LOG.debug(" - non esistono rec");
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e1){
		e1.printStackTrace();
		throw newEjbException("Errore eseguendo una deleteAnagra() - ",  e1);
	} finally { // 01/02/13
		close_dbcur_nothrow("deleteAnagra", dbcur);
	}
}

	// 21/12/07 m.: lettura dati di ANAGRA_C (al momento servono solo zona, distretto di residenza e medico ma si legge tutto)
	public Hashtable query_getDatiFromAnagraC(myLogin mylogin, Hashtable h0) throws SQLException {
		ISASConnection dbc = null;

		// hashtable che ritorner� al client
		Hashtable h_ret = new Hashtable();

		String cart = (String)h0.get("n_cartella");
		try{
	    	dbc = super.logIn(mylogin);

			// 05/12/11 m. ---
			String dtRif = (String)h0.get("dt_rif");
			String crtiDtRif = "";
			if ((dtRif != null)  && (!dtRif.trim().equals("")))
				crtiDtRif = " AND anagra_c.data_variazione <= " + formatDate(dbc, dtRif);
			// 05/12/11  m. ---


			String sel = "SELECT a.* FROM anagra_c a" +
						" WHERE a.n_cartella = " + cart +
						" AND a.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
							" FROM anagra_c" +
							" WHERE anagra_c.n_cartella = a.n_cartella" +
							crtiDtRif +
						")";

			ISASRecord dbr1 = dbc.readRecord(sel);
			if (dbr1 != null) {
// 19/02/08		String codDistr = (String)ISASUtil.getDecode(dbc, "comuni", "codice", (String)dbr1.get("citta"), "cod_distretto");
				String codDistr = (String)ISASUtil.getDecode(dbc, "areadis", "codice", (String)dbr1.get("areadis"), "cod_distretto"); // 19/02/08
				String codZona = (String)ISASUtil.getDecode(dbc, "distretti", "cod_distr", codDistr, "cod_zona");
				decodMedico(dbc, dbr1);

				h_ret = (Hashtable)dbr1.getHashtable();
				h_ret.put("distretto", codDistr);
				h_ret.put("zona", codZona);
				h_ret.put("regione", dbr1.get("regione").toString());//elisa b 18/03/11
			}


           	return h_ret;
		}
		catch(Exception e) {
			e.printStackTrace();
			throw newEjbException("Errore eseguendo una query_getDatiFromAnagraC()", e);
		} finally {
			logout_nothrow("query_getDatiFromAnagraC", dbc);
		}
  	}

	private void decodMedico(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		String cod = (String)mydbr.get("cod_med");
		String cogn = "";
		String nome = "";

		if ((cod != null) && (!cod.trim().equals(""))){
			String sel = "SELECT NVL(mecogn, '') cogn_med," +
						" NVL(menome, '') nome_med" +
						" FROM medici WHERE mecodi = '" + cod + "'";

			ISASRecord dbr2 = mydbc.readRecord(sel);
			if (dbr2 != null) {
				cogn = (String)dbr2.get("cogn_med");
				nome = (String)dbr2.get("nome_med");
			}
		}
		mydbr.put("cod_med" , (cod!=null?cod:""));
		mydbr.put("cogn_med" , cogn);
		mydbr.put("nome_med" , nome);
	}

	/**
	 * elisa b 17/04/12
	 * se dal client arriva l'informazione della
	 * modifica del medico e/o della data in cui il medico e' stato
	 * scelto, si deve aggiornare o inserire un record che ha per
	 * data variazione la data di scelta del medico ed, eventualmente,
	 * aggiornare il valore del medico e della data in record con data variazione
	 * successiva.
	 * @param dbc
	 * @param h
	 */
	@SuppressWarnings("rawtypes")
	private void gestioneMedico(ISASConnection dbc, Hashtable h, String codice,
			String codOperatore) throws Exception {
		ISASCursor dbcur = null;
		try{
			String dtVariazione = (String) h.get("data_variazione");
			String dtRiferimento = (String) h.get("data_medico");
			String codMedico = (String) h.get("cod_med");
			DataWI dtRif = new DataWI(dtRiferimento.replaceAll("-", ""), 1);
			String sel = "SELECT * FROM anagra_c" + " WHERE n_cartella = " + codice
					+ " AND data_variazione = " + formatDate(dbc, dtRiferimento);

			ISASRecord dbrAn = dbc.readRecord(sel);
			if (dbrAn != null) // aggiorno
				LOG.info("aggiorno");
			else { // inserisco
				LOG.info("Nuovo record");
				dbrAn = dbc.newRecord("anagra_c");
				dbrAn.put("cod_operatore", codOperatore);
				LOG.info("gestioneMedico: inserito un record in data " + dtRiferimento);
			}
			dbrAn.put("n_cartella", codice);
			Enumeration n2 = h.keys();
			while (n2.hasMoreElements()) {
				String elem2 = (String) n2.nextElement();
				dbrAn.put(elem2, h.get(elem2));
			}
			dbrAn.put("data_medico",dtRif.getSqlDate());
			dbrAn.put("data_variazione",dtRif.getSqlDate());
			dbc.writeRecord(dbrAn);
			
			/* se esistono altri record con data variazione successiva alla data
			 * di riferimento, in essi si devono aggiornare i campi.
			 * Il record con data variazione passato come parametro
			 * viene gia' aggiornato dalla normale procedura*/
			sel = "SELECT * FROM anagra_c"
					+ " WHERE n_cartella = " + codice
					+ " AND data_variazione > " + formatDate(dbc, dtRiferimento)
					+ " AND data_variazione < " + formatDate(dbc, dtVariazione);
			dbcur = dbc.startCursor(sel);
			while(dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String dt = ""+ dbr.get("data_variazione");
				String selUpd = "SELECT * FROM anagra_c" + " WHERE n_cartella = " + codice
						+ " AND data_variazione = " + formatDate(dbc, dt);
				ISASRecord dbrU = dbc.readRecord(selUpd);
				dbrU.put("cod_med", codMedico);
				dbrU.put("data_medico",dtRif.getSqlDate());
				dbc.writeRecord(dbrU);
				LOG.info("Aggiornato un record in data successiva");
			}
			
			/* elisa b 11/03/15 : aggiornamento tabella segnalazione */	
			inserisciSegnalazione(dbc, codice, dtRiferimento, codMedico);

		} finally {
			if(dbcur != null)
				dbcur.close();
		}

	}
	
	/**
	 * elisa b 11/03/15
	 * Viene inserita una segnalazione in caso di variazione del medico
	 * @param dbc
	 * @param h
	 * @throws Exception 
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void inserisciSegnalazione(ISASConnection dbc, String nCartella, String dtRiferimento, String codMedico)
			throws Exception {
		SegnalazioniEJB seg = new SegnalazioniEJB();
	
		Hashtable hSegn = new Hashtable();
		hSegn.put("n_cartella", nCartella);
		Date dt = UtilForBinding.getDateFromIsas(dtRiferimento);
		hSegn.put("data_segnalazione", new java.sql.Date(dt.getTime()));
		hSegn.put("oggetto", "Variazione del medico");
		hSegn.put("segnalazione", "Assegnato il medico " + ISASUtil.getDecode(dbc, "medici", "mecodi", codMedico, "mecogn") +
			" in data " + UtilForBinding.getStringClientFromDate(dt));
		seg.insertSegnalazione(dbc, hSegn);
	}

	@SuppressWarnings({ "finally", "rawtypes" })
	public Boolean chiudiCartellaDaIntegrazione(myLogin mylogin, Hashtable arg) {
			boolean ret=false;
			String codUsl = null;
			Date data_chiusura = null;
			String motivo_chiusura = null;
			String cartella=null;
			ISASConnection dbc=null;
			String methodName= "chiudiCartellaDaIntegrazione";
			try {
				dbc=super.logIn(mylogin);
//				dbc.startTransaction();
				
				codUsl = (String)arg.get(CostantiSinssntW.CODUSL);
				try{data_chiusura = (Date) arg.get(CostantiSinssntW.DATA_CHIUSURA);
					}catch(ClassCastException e){
						data_chiusura = UtilForBinding.getDateFromIsas(arg.get(CostantiSinssntW.DATA_CHIUSURA).toString());
					}
				motivo_chiusura = (String)arg.get(CostantiSinssntW.MOTIVO_CHIUSURA);
				if(codUsl.isEmpty() || data_chiusura==null || motivo_chiusura.isEmpty()){
					Exception e = new CariException("Mancano i parametri per la chiusura codusl:"+codUsl +", "+
							"data chusura:"+data_chiusura + " motivo chiusura:"+motivo_chiusura+ "!");
					loggaErroreInChiusuraForzata(e, dbc, codUsl, UtilForBinding.getValueForIsas(data_chiusura), motivo_chiusura);
					return false;
				}
				
				String mysel="Select * from cartella where "+
						   "cod_usl = '"+(String)arg.get(CostantiSinssntW.CODUSL) +"' "; //AND data_chiusura IS NULL ";
				ISASRecord dbr=dbc.readRecord(mysel);
		        if(dbr!= null && dbr.get(CostantiSinssntW.N_CARTELLA) != null){
					cartella = ((Integer)dbr.get(CostantiSinssntW.N_CARTELLA)).toString();
					mysel = "Select * from cartella where "+
							   "n_cartella = "+ cartella +" ";
					dbr=dbc.readRecord(mysel);
					dbr.put(CostantiSinssntW.DATA_CHIUSURA, UtilForBinding.getValueForIsas(data_chiusura));
					dbr.put(CostantiSinssntW.MOTIVO_CHIUSURA, motivo_chiusura);
					dbr.put(CostantiSinssntW.FORZA_CHIUSURA, "SI");
		            dbc.writeRecord(dbr);
		            chiudiCartella(mylogin, dbr, dbc, UtilForBinding.getValueForIsas(data_chiusura), cartella, motivo_chiusura, codUsl);
				}
//		        dbc.commitTransaction();
		        ret = true;
			} catch(Exception e){
				loggaErroreInChiusuraForzata(e, dbc, codUsl, UtilForBinding.getValueForIsas(data_chiusura), motivo_chiusura);
				throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
			}finally {
				logout_nothrow(methodName, dbc);
				return ret;
			}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void gestioneAggiornamentiAnagrafici(myLogin mylogin, Hashtable hPar) throws Exception{
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			AnagUtils anagUtils = new AnagUtils();
			PatientResponse pR = (PatientResponse) hPar.get("PatientResponse");
			QueryResponse qR = pR.getQryResponse().get(0);
			List<Esenzione> lEsenzioni = qR.getEsenzioni();
			Medico med = qR.getMedicoCurante();
						
			GregorianCalendar gc = new GregorianCalendar();
			java.sql.Date dtOggi = new java.sql.Date(gc.getTimeInMillis());
			
			anagUtils.setNCartella(dbc, qR);
			int nCartella = qR.getAssistito().getnCartella();	
			
			if(med != null)	
				anagUtils.setCodMedico(dbc, qR);
			
			//aggiorna cartella
			String mysel = "SELECT * FROM cartella" +
					" WHERE n_cartella = " + nCartella;
			ISASRecord dbr = dbc.readRecord(mysel);
			dbr = anagUtils.creaIsasRecordCartella(dbc, qR, dbr);
			
			//anagra c
			String sel = "SELECT a.* FROM anagra_c a" +
					" WHERE a.n_cartella = " + nCartella +
					" AND a.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
						" FROM anagra_c" +
						" WHERE anagra_c.n_cartella = a.n_cartella" +
					")";

			ISASRecord dbrC = dbc.readRecord(sel);
			Hashtable<String, Object> hNew = anagUtils.creaDatiAnagraC(dbc, qR, dbrC.getHashtable());
			hNew.put("data_variazione", UtilForBinding.getValueForIsas(dtOggi));
			
			//gestione medico
			if(med != null){	
				Date dataRevoca = qR.getMedicoCurante().getDataRevoca();
				/* la cessazione viene comunicata con una data revoca per un assistito non attivo.
				 * In questo caso, quindi la presenza della data revoca ha un significato diverso */
				if(dataRevoca != null && !qR.getAssistito().isCertificato()){
					// caso di cessazione
					dataRevoca = null;
				}
						
				if(med.getCodice() == null){
					/* se il medico non e' presente s iinserisce */
					int progr = selectProgressivoSequence(dbc, AnagraConstants.SEQ_PROGRESSIVO_COD_MEDICO);
					anagUtils.inserisciMedico(dbc, hNew, String.valueOf(progr));
					String codMed = String.valueOf(progr);
					med.setCodice(codMed);
					hNew.put("cod_med",codMed);
				}
				String dtAssegnazioneNew = UtilForBinding.getValueForIsas(qR.getMedicoCurante().getDataAssegnazione());
				String dtAssegnazioneOld = UtilForBinding.getValueForIsas((Date)dbrC.get("data_medico"));
				if(!qR.getMedicoCurante().getCodice().equals(dbrC.get("cod_med"))
						|| (!dtAssegnazioneNew.equals("") && !dtAssegnazioneNew.equals(dtAssegnazioneOld))
						|| dataRevoca != null){
					//variazione medico
					dbr.put("info_medico_mod","S");
					hNew.put("data_variazione", UtilForBinding.getValueForIsas(qR.getMedicoCurante().getDataAssegnazione()));
				} 
			}
			
			/* creo un dbr con i campi di anagra_c in modo da confrontare due oggetti 
			 * con le stesse chiavi */
			ISASRecord dbrN = dbc.readRecord(sel);
			Enumeration<String> en = hNew.keys();
			Hashtable<String, Object> hNewAnagC = dbrN.getHashtable();
			while(en.hasMoreElements()){
				String key = en.nextElement();
				if(hNewAnagC.containsKey(key)){
					Object o = hNew.get(key);
					if(hNewAnagC.get(key) instanceof Date){
						/* in caso di campi inserisco un oggetto data anche nel nuovo isasRecord*/
						if (o instanceof String){
							Date d = UtilForBinding.getDateFromIsas((String)o);
							hNewAnagC.put(key, new java.sql.Date(d.getTime()));
						}
					}else
						hNewAnagC.put(key, o);
				}
			}
			if(med != null)
				hNewAnagC.put("data_medico", hNew.get("dt_scelta_medico"));
						
			/* confronto i nuovi e i vecchi valori per verificare se esistono dati modificati*/
			if(anagUtils.esisteVariazioneAnag(hNewAnagC, dbrC.getHashtable())
					|| dbr.get("data_chiusura") != null){				
				Vector<Hashtable<String, Object>> hv = new Vector<Hashtable<String,Object>>();
				//si imposta il parametro per forzare la chiusura
				if(dbr.get("data_chiusura") != null)
					dbr.put(CostantiSinssntW.FORZA_CHIUSURA, "SI");
				hv.add(hNew);
				update(mylogin, dbr, hv);
			}else
				LOG.info("Nessuna variazione");
			
			//Gestione esenzioni 
			if(lEsenzioni != null && lEsenzioni.size() >0){
				anagUtils.gestisciEsenzioniAssistito(dbc, qR.getAssistito().getCodiceUsl(), lEsenzioni);
			}

		}  finally {
			logout_nothrow("gestioneAggiornamentiAnagrafici", dbc);			
		}
	}
	
	public Hashtable<String, String> verificaStatoCartella(myLogin myLogin, Hashtable<String, String> dati) {
		String punto = ver + "verificaStatoCartella ";
		Hashtable<String, String> risultato = new Hashtable<String, String>();
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(myLogin);
			
			String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			risultato= statoCartella(dbc, nCartella);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logout_nothrow(punto, dbc);
		}
		return risultato;
	}

	private Hashtable<String, String> statoCartella(ISASConnection dbc,String nCartella) throws ISASMisuseException,
			ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver  + "statoCartella ";
		ISASRecord dbrCartella;
		
		Hashtable<String, String> risultato = new Hashtable<String, String>();
		String mysel = "select * from cartella where n_cartella = " + nCartella;
		risultato.put(CostantiSinssntW.CTS_CARTELLA_ATTIVA, "");
		dbrCartella = dbc.readRecord(mysel);
		String dataChiusura = ISASUtil.getValoreStringa(dbrCartella, "data_chiusura");
		if (ManagerDate.validaData(dataChiusura)) {
			risultato.put(CostantiSinssntW.CTS_CARTELLA_CHIUSA, dataChiusura);
		}
		LOG.trace(punto + " dati recuperati>>" +risultato);
		
		return risultato;
	}

}


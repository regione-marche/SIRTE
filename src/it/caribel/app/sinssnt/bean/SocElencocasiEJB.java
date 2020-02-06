package it.caribel.app.sinssnt.bean;
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

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.NumberDateFormat;
import it.pisa.caribel.sinssnt.connection.*;

// 20/09/11
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.swing2.util.utils;

public class SocElencocasiEJB extends SINSSNTConnectionEJB {

	// 11/06/07 m.: aggiunto, nel metodo "checkAssistito()", il "catch"
	// dell'ISASPermissioneDenied x segnalarlo latoClient;
	private static final String MIONOME = "17-SocElencocasiEJB.";

	public SocElencocasiEJB() {
	}

	// 20/09/11
	private EveUtils eveUtl = new EveUtils();
   
	// 06/12/07: valori per tipo oper
	private final String TP_OPER_AS = "01"; // Ass Sociale
	private final String TP_OPER_IP = "02"; // Infermiere
	private final String TP_OPER_PUAC = "00"; // PUAC
	private final String TP_OPER_PUACD = "DS"; // PUACD

	// 06/12/07: valori per "esito_contatto" attualmente definiti su TAB_VOCI con key=ESITOCON
	private final String APP_AS = "3"; // appuntamento con Ass Sociale
	private final String APP_IP = "4"; // appuntamento con Infermiere
	private final String APP_PUAC = "6"; // gestione PUAC
	private final String APP_PUAC_UVMD = "61"; // gestione PUAC UVMD 21/09/12
	private final String APP_PUAC_1 = "15"; // gestione PUAC x RomaE (24/11/09)
	private final String APP_UVMD = "61"; // GESTIONE DELL'uvmd 24/09/12

	// 08/01/08
	private String msg = "Mancano i diritti per leggere il record";

	// 24/08/09
	private final String AREADIS_COD_DEFAULT = "000000";
	private final String COMUNE_COD_DEFAULT = "000000";
	private final String MEDICO_COD_DEFAULT = "000000";
	private final String CONSTANTS_ABILITAZIONE_UVM = "ABIL_UVMD";

	private String TABELLA_GESTIONE_PUAUVMD ="puauvmd";

	private static final String CONSTANTS_ORDER_COMMUNE_NASC = "comune_nascita_descr";
	private static final String CONSTANTS_ORDER_COMMUNE_RES= "comune_res_descr";
	private static final String CONSTANTS_ORDER_COMMUNE_DOM= "comune_dom_descr";

	private String decodificaComune(ISASConnection mydbc, ISASRecord mydbr, String dbFldName, String dbName) throws Exception {
		String strCodComune = "";
		String strDescrComune = "";

		if (mydbr.get(dbFldName) != null)
			strCodComune = (String) mydbr.get(dbFldName);

		if ((strCodComune == null) || strCodComune.equals("")) {
			mydbr.put(dbName, "");
			return strDescrComune;
		}

		String selS = "SELECT descrizione FROM comuni" + " WHERE codice = '" + strCodComune + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
			if (rec.get("descrizione") != null)
				strDescrComune = (String) rec.get("descrizione");

		mydbr.put(dbName, strDescrComune);
		return strDescrComune;
	}// END decodificaComune

	private void decodificaTB_SINSS(ISASConnection mydbc, ISASRecord mydbr, String dbFldCodName, String dbFldTbCodVal, String dbDescrName)
			throws Exception {
		if ((mydbr.get(dbFldCodName) == null) || ((String) mydbr.get(dbFldCodName)).equals("")) {
			mydbr.put(dbDescrName, "");
			return;
		}

		String dbFldCodVal = (String) mydbr.get(dbFldCodName);
		String dbDescrVal = "";

		if (dbFldCodVal == null) {
			mydbr.put(dbDescrName, "");
			return;
		}

		// 13/11/07: sostituito TAB_SINSS con TAB_VOCI
		String selS = "SELECT tab_descrizione" + " FROM tab_voci" + " WHERE tab_cod = '" + dbFldTbCodVal + "'" + " AND tab_val = '"
				+ dbFldCodVal + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null) {
			if (rec.get("tab_descrizione") != null)
				dbDescrVal = (String) rec.get("tab_descrizione");
		}
		mydbr.put(dbDescrName, dbDescrVal);
	} // END decodificaTB_SINSS

	private void decodificaCaricoServ(ISASRecord dbr, String dbDescrName) throws Exception {
		Hashtable ht = new Hashtable();
		ht.put("0", "NO");
		ht.put("1", "Servizio Sociale");
		ht.put("2", "Servizio Sanitario");
		ht.put("3", "Entrambi");

		String codVal = (String) dbr.get("carico_serv");
		String descrVal = (String) ht.get(codVal);
		if (descrVal == null)
			descrVal = "";
		dbr.put(dbDescrName, descrVal);
	} // END decodificaCaricoServ

	// gb 27/12/07 *******
	private void decodificaTipoSegnalaz(ISASRecord dbr, String dbDescrName) throws Exception {
		try {
			Hashtable ht = new Hashtable();
			ht.put("0", "Primo contatto");
			ht.put("1", "Richiesta di segnalazione successiva non programmata");

			String codVal = (String) dbr.get("segn_tipo");
			if ((codVal == null) || codVal.equals("")) {
				dbr.put(dbDescrName, "");
				return;
			}
			String descrVal = (String) ht.get(codVal);
			if (descrVal == null)
				descrVal = "";
			dbr.put(dbDescrName, descrVal);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("SocElencocasiEJB/decodificaTipoSegnalaz: Errore!");
		}
	} // END decodificaTipoSegnalaz

	// gb 27/12/07: fine *******

	private String decodificaMedico(ISASConnection dbc, ISASRecord dbr, String dbFldName, String dbDescrName) throws Exception {
		String strCodMedico = "";
		String strDescrMedico = "";

		if (dbr.get(dbFldName) != null)
			strCodMedico = (String) dbr.get(dbFldName);

		if ((strCodMedico == null) || strCodMedico.equals("")) {
			dbr.put(dbDescrName, "");
			return strDescrMedico;
		}

		String selS = "SELECT mecogn" + " FROM medici" + " WHERE mecodi = '" + strCodMedico + "'";

		ISASRecord rec = dbc.readRecord(selS);

		if (rec != null)
			if (rec.get("mecogn") != null)
				strDescrMedico = (String) rec.get("mecogn");

		dbr.put(dbDescrName, strDescrMedico);
		return strDescrMedico;
	} // END decodificaMedico

	// gb 27/12/07 *******
	private String decodificaAreaDis(ISASConnection dbc, ISASRecord dbr, String dbFldName, String dbDescrName) throws Exception {
		try {
			String strCod = "";
			String strDescr = "";

			if (dbr.get(dbFldName) != null)
				strCod = (String) dbr.get(dbFldName);

			if ((strCod == null) || strCod.equals("")) {
				dbr.put(dbDescrName, "");
				return strDescr;
			}

			String selS = "SELECT descrizione" + " FROM areadis" + " WHERE codice = '" + strCod + "'";

			ISASRecord rec = dbc.readRecord(selS);

			if (rec != null)
				if (rec.get("descrizione") != null)
					strDescr = (String) rec.get("descrizione");

			dbr.put(dbDescrName, strDescr);
			return strDescr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("SocElencocasiEJB/decodificaAreaDis: Errore!");
		}
	} // END decodificaAreaDis

	// gb 27/12/07: fine *******

	// gb 27/12/07 *******
	private String decodificaCittadin(ISASConnection dbc, ISASRecord dbr, String dbFldName, String dbDescrName) throws Exception {
		String strCod = "";
		String strDescr = "";

		try {
			if (dbr.get(dbFldName) != null)
				strCod = (String) dbr.get(dbFldName);

			if ((strCod == null) || strCod.equals("")) {
				dbr.put(dbDescrName, "");
				return strDescr;
			}

			String selS = "SELECT des_cittadin" + " FROM cittadin" + " WHERE cd_cittadin = '" + strCod + "'";

			ISASRecord rec = dbc.readRecord(selS);

			if (rec != null)
				if (rec.get("des_cittadin") != null)
					strDescr = (String) rec.get("des_cittadin");

			dbr.put(dbDescrName, strDescr);
			return strDescr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("SocElencocasiEJB/decodificaCittadin: Errore!");
		}
	} // END decodificaCittadin

	// gb 27/12/07: fine *******

	// 06/06/08
	private String decodificaOper(ISASConnection dbc, ISASRecord dbr, String dbFldName, String dbDescrName) throws Exception {
		String strCodOper = "";
		String strDescrOper = "";

		if (dbr.get(dbFldName) != null)
			strCodOper = (String) dbr.get(dbFldName);

		if ((strCodOper == null) || strCodOper.equals("")) {
			dbr.put(dbDescrName, "");
			return strDescrOper;
		}

		String selS = "SELECT * FROM operatori" + " WHERE codice = '" + strCodOper + "'";

		ISASRecord rec = dbc.readRecord(selS);

		if (rec != null)
			strDescrOper = (rec.get("cognome") != null ? (String) rec.get("cognome") : "") + " "
					+ (rec.get("nome") != null ? (String) rec.get("nome") : "");

		dbr.put(dbDescrName, strDescrOper);
		return strDescrOper;
	} // END decodificaOper

	// 06/06/08
	private String decodificaSede(ISASConnection dbc, ISASRecord dbr, String dbFldName, String dbDescrName) throws Exception {
		String strCod = "";
		String strDescr = "";

		try {
			if (dbr.get(dbFldName) != null)
				strCod = (String) dbr.get(dbFldName);

			if ((strCod == null) || strCod.equals("")) {
				dbr.put(dbDescrName, "");
				return strDescr;
			}

			String selS = "SELECT p.* FROM presidi p" + " WHERE p.codpres = '" + strCod + "'" + " AND EXISTS (SELECT * FROM conf c1"
					+ " WHERE p.codreg = c1.conf_txt" + " AND c1.conf_kproc = 'SINS'" + " AND c1.conf_key = 'codice_regione')"
					+ " AND EXISTS (SELECT * FROM conf c2" + " WHERE p.codazsan = c2.conf_txt" + " AND c2.conf_kproc = 'SINS'"
					+ " AND c2.conf_key = 'codice_usl')";

			ISASRecord rec = dbc.readRecord(selS);

			if (rec != null)
				if (rec.get("despres") != null)
					strDescr = (String) rec.get("despres");

			dbr.put(dbDescrName, strDescr);
			return strDescr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("SocElencocasiEJB/decodificaSede: Errore!");
		}
	} // END decodificaSede

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		String strProgressivo = null;

		if (h.get("progressivo") == null) {
			throw new SQLException("ElencocasiEJB: Manca Numero Progressivo in esecuzione queryKey()");
		} else
			strProgressivo = (String) h.get("progressivo");

		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT *" + " FROM ass_anagrafica" + " WHERE progressivo = " + strProgressivo;
			System.out.println("ElencocasiEJB/queryKey : " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				decodificaComune(dbc, dbr, "comune_nascita", "comune_nascita_descr");
				decodificaComune(dbc, dbr, "comune_res", "comune_res_descr");
				decodificaComune(dbc, dbr, "comune_dom", "comune_dom_descr");
				decodificaComune(dbc, dbr, "segn_comune_nas", "segn_comune_nas_descr");
				decodificaComune(dbc, dbr, "segn_comune_res", "segn_comune_res_descr");

				decodificaTB_SINSS(dbc, dbr, "settore", "SETTORE", "settore_descr");
				decodificaTB_SINSS(dbc, dbr, "segn_rapporto", "SEGNRAPP", "segn_rapporto_descr");
				decodificaTB_SINSS(dbc, dbr, "arrivato", "ARRISERV", "arrivato_descr");
				decodificaTB_SINSS(dbc, dbr, "motivo", "MOTIVO", "motivo_descr");
				decodificaTB_SINSS(dbc, dbr, "esito_contatto", "ESITOCON", "esito_contatto_descr");

				decodificaCaricoServ(dbr, "carico_serv_descr");

				decodificaMedico(dbc, dbr, "mecodi", "mecodi_descr");

				// gb 27/12/07 *******
				decodificaTB_SINSS(dbc, dbr, "str_tipo_doc", "DOCSOGG", "str_tipo_doc_descr");
				decodificaTB_SINSS(dbc, dbr, "area_interv", "AREAINTE", "area_interv_descr");
				decodificaTB_SINSS(dbc, dbr, "rif_rapporto", "SEGNRAPP", "rif_rapporto_descr");
				decodificaTB_SINSS(dbc, dbr, "tipo_informazione", "TIPOINFO", "tipo_informazione_descr");
				decodificaTB_SINSS(dbc, dbr, "tipo_servizio", "TIPOSERV", "tipo_servizio_descr");

				decodificaCittadin(dbc, dbr, "nazionalita", "nazionalita_descr");
				decodificaCittadin(dbc, dbr, "cittadinanza", "cittadinanza_descr");

				decodificaAreaDis(dbc, dbr, "areadis_res", "areadis_res_descr");
				decodificaAreaDis(dbc, dbr, "areadis_dom", "areadis_dom_descr");
				decodificaAreaDis(dbc, dbr, "areadis_rep", "areadis_rep_descr");

				decodificaTipoSegnalaz(dbr, "segn_tipo_descr");

				decodificaComune(dbc, dbr, "rif_comune_res", "rif_comune_res_descr");
				decodificaComune(dbc, dbr, "comune_rep", "comune_rep_descr");
				// gb 27/12/07 *******

				// 06/06/08 -----
				decodificaOper(dbc, dbr, "cod_operatore", "desc_oper");
				decodificaSede(dbc, dbr, "cod_presidio", "desc_pres_oper");
				// 06/06/08 -----

				/***** 07/10/08
					dbr.put("ag_data_app", "");
					dbr.put("ag_ora_app", "A");
				*********/
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		// 08/01/08 ---
		catch (ISASPermissionDeniedException e) {
			System.out.println("ElencocasiEJB.queryKey(): " + e);
			throw new CariException(msg, -2);
		} catch (Exception e) {
			System.out.println("SocElencocasiEJB.queryKey(): " + e);
			throw new SQLException("Errore eseguendo la queryKey()");
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

	// 30/04/09
	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr, String tpOp, boolean abilitaGestioneUvmd) throws Exception {
		decodificaQueryInfo(mydbc, vdbr, tpOp, false, abilitaGestioneUvmd);
	}

	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr, String tpOp, boolean daQueryPaginate, boolean abilitaGestioneUvmd)
			throws Exception {
		String punto = MIONOME + "decodificaQueryInfo ";
		boolean isxPuac = tpOp.equals(this.TP_OPER_PUAC);
		String prefix = (tpOp.equals(this.TP_OPER_AS) ? "soc" : (tpOp.equals(this.TP_OPER_IP) ? "san" : ""));
		// 30/04/09 ---
		int dimVett = vdbr.size();
		if (daQueryPaginate)
			dimVett--;
		stampa(punto + " abilitazione>" + abilitaGestioneUvmd + "< vdbr.size()>" + vdbr.size() + "< daQueryPaginate>" + daQueryPaginate
				+ "<dimVett>" + dimVett + "<");

		for (int i = 0; i < dimVett; i++) {
			stampa(punto + "itero>" + i);
			ISASRecord dbr = (ISASRecord) vdbr.get(i);

			decodificaComune(mydbc, dbr, "comune_nascita", "comune_nascita_descr");
			decodificaComune(mydbc, dbr, "comune_res", "comune_res_descr");
			decodificaComune(mydbc, dbr, "comune_dom", "comune_dom_descr"); // 13/12/08

			decodificaTB_SINSS(mydbc, dbr, "settore", "SETTORE", "settore_descr");
			decodificaTB_SINSS(mydbc, dbr, "motivo", "MOTIVO", "motivo_descr");
			decodificaTB_SINSS(mydbc, dbr, "area_interv", "AREAINTE", "area_interv_descr");// 06/12/07

			// 19/11/08: x attribuz caso ad oper ---
			String nomaCampoOper = prefix + "_cod";
			dbr.put("oper_attr_old", dbr.get(nomaCampoOper));
			decodificaOper(mydbc, dbr, "oper_attr_old", "oper_attr_old_descr");
			// 19/11/08: x attribuz caso ad oper ---

			// x ottenere la colorazione delle righe ----
			String colRiga = "";
			boolean urg = ((dbr.get("urgente") != null) && (((String) dbr.get("urgente")).trim().equals("S")));
			if (isxPuac) { // elenco casi PUAC
				// 17/01/08: sono visualizzati solo nella JFrameGridASElencoCasiPUAC
				String segn_cogn = (String) dbr.get("segn_cognome");
				String segn_indi = (String) dbr.get("segn_indirizzo");
				dbr.put("segn_descr", (segn_cogn != null ? segn_cogn : "") + " - " + (segn_indi != null ? segn_indi : ""));

				boolean insAuto = ((dbr.get("inserita_autom") != null) && (((String) dbr.get("inserita_autom")).trim().equals("S")));
				boolean revis = ((dbr.get("inserita_autom") != null) && (((String) dbr.get("inserita_autom")).trim().equals("R"))); // 03/03/08

				if (urg)
					colRiga = "U";
				else if (insAuto)
					colRiga = "S";
				else if (revis)
					colRiga = "R";
			} else { // elenco casi x oper AS /INF
				// 24/11/09 boolean casoCmplx = ((dbr.get("esito_contatto") != null) && (((String)dbr.get("esito_contatto")).trim().equals(this.APP_PUAC)));
				boolean casoCmplx = ((dbr.get("esito_contatto") != null) && ((((String) dbr.get("esito_contatto")).trim()
						.equals(this.APP_PUAC)) || (((String) dbr.get("esito_contatto")).trim().equals(this.APP_PUAC_1))));

				if (urg)
					colRiga = "U";
				else if (casoCmplx)
					colRiga = "C";

				String nomeCampoDt = prefix + "_data";
				dbr.put("data_app", dbr.get(nomeCampoDt));
			}
			if (abilitaGestioneUvmd) {
				String valEsitoContatto = ISASUtil.getValoreStringa(dbr, "esito_contatto");
				if (ISASUtil.valida(valEsitoContatto) && valEsitoContatto.equalsIgnoreCase(APP_UVMD)) {
					dbr.put("uvmd", "D");
					if (urg){
						colRiga = "UD";
					}else{
						colRiga = "D";
					}
				}
			} else {
				stampa(punto + " disattivato il controllo ");
			}
			
			dbr.put("color_riga", colRiga);
			// x ottenere la colorazione delle righe ----
//			stampa(punto + "itero>" + (dbr != null ? dbr.getHashtable() + "" : " no dati") + "<");
		}
	}

	/********
		public Vector queryPaginate(myLogin mylogin, Hashtable h) throws SQLException 
		{
		    boolean done=false;
		    ISASConnection dbc=null;
		    ISASCursor dbcur = null;

		    try{
		      dbc=super.logIn(mylogin);
		      
				String myselect = faiSelectQuery(dbc, h);
		      System.out.println("ElencocasiEJB/queryPaginate: " + myselect);
		
		      dbcur = dbc.startCursor(myselect);
		      System.out.println("ElencocasiEJB/queryPaginate: Fatta la startcursor");
		
		      int start = Integer.parseInt((String)h.get("start"));
		      int stop = Integer.parseInt((String)h.get("stop"));
		      Vector vdbr = dbcur.paginate(start, stop);
		      dbcur.close();
		
		      // Decodifica comune_res, comune_nasc, settore e motivo
		      decodificaQueryInfo(dbc, vdbr);
		
		      dbc.close();
		      super.close(dbc);
		      done=true;
		      return vdbr;
		      }
		    catch(Exception e)
		      {
		      e.printStackTrace();
		      throw new SQLException("ElencocasiEJB: Errore eseguendo la queryPaginate()  ");
		      }
		    finally
		      {
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
	***********/

	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		String strProgressivo = null;
		ISASConnection dbc = null;

		try {
			strProgressivo = (String) h.get("progressivo");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			ISASRecord dbr = dbc.newRecord("ass_anagrafica");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}
			dbc.writeRecord(dbr);
			String myselect = "SELECT * FROM ass_anagrafica WHERE progressivo = " + strProgressivo;
			System.out.println("ElencocasiEJB/insert: " + myselect);
			dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		}

		catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("ElencocasiEJB: Errore eseguendo la insert() - " + e1);
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

	// gb 17/01/08: chiamato da JFrameASAssProgetto e da JFrameSkInf
	// Controlla se l'assistito � presente o no in ass_anagrafica
	// e se � gi� stato segnalato al PUAC per una valutazione successiva:
	// caso semplice -->caso complesso(=S) oppure revisione(=R)
	// Se � gi� stato segnalato al PUAC controlla lo stato del campo 'presa_carico'
	// e agisce di conseguenza.
	public Hashtable checkEsistenzaSegnalPUAC(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		Hashtable htResult = new Hashtable();
		try {
			dbc = super.logIn(mylogin);

			System.out.println("ElencocasiEJB/checkEsistenzaSegnalPUAC/h_in: " + h.toString());

			String strNCartella = (String) h.get("n_cartella");
			String strQueryCart = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + strNCartella;
			System.out.println("ElencocasiEJB/checkEsistenzaSegnalPUAC/strQueryCart: " + strQueryCart);
			ISASRecord dbrCart = dbc.readRecord(strQueryCart);
			if (dbrCart == null) {
				htResult.put("ret_code", "NoExistCart"); // 1. Rec. cartella non esistente
				htResult.put("n_cartella", strNCartella);
				return htResult;
			}

			// 12/03/08: ctrl non esistenza di schede PUAC aperte (=senza dtVerbaleUVM) ----
			// 06/10/08: per essere aperte devono anche NON avere dtChiusura
			String dtSkVal = (String) h.get("pr_data");

			// 20/09/11
			String nmTabSkPuac = eveUtl.getNmTabSkPuac(dbc);

			// si legge il record di progr max con dtVerbale NULL
			String selPuac = "SELECT p.* FROM " + nmTabSkPuac + " p" + " WHERE p.n_cartella = " + strNCartella + " AND p.pr_data = "
					+ formatDate(dbc, dtSkVal) + " AND p.pr_progr IN (SELECT MAX(pr_progr) FROM " + nmTabSkPuac + " b"
					+ " WHERE b.n_cartella = p.n_cartella" + " AND b.pr_data = p.pr_data)" + " AND p.pr_data_verbale_uvm IS NULL";

			// 20/09/11 -----
			if (eveUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_mmg_risposta_neg"))
				selPuac += " AND ((p.pr_mmg_risposta_neg <> 'S')" + " OR (p.pr_mmg_risposta_neg IS NULL))";

			if (eveUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_data_chiusura"))
				selPuac += " AND p.pr_data_chiusura IS NULL";// 06/10/08

			if (eveUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_data_avvio"))
				selPuac += " ORDER BY p.pr_data_avvio";
			else if (eveUtl.existsFldInTab(dbc, nmTabSkPuac, "pr_data_richiesta"))
				selPuac += " ORDER BY p.pr_data_richiesta";
			else
				selPuac += " ORDER BY p.pr_data_puac";
			// 20/09/11 -----

			System.out.println("ElencocasiEJB/checkEsistenzaSegnalPUAC/selPuac: " + selPuac);

			// 24/03/09 ISASRecord dbrPuac = dbc.readRecord(selPuac);

			// 24/03/09: in modo che ISAS elimini i record che non riguardano la zon
			ISASCursor dbcur_1 = dbc.startCursor(selPuac);

			while (dbcur_1.next()) {
				ISASRecord dbrPuac = dbcur_1.getRecord();
				// 24/03/09 ----------------
				if (dbrPuac != null) { // se esiste
					htResult.put("ret_code", "Puac_Aperta"); // 1BIS. scheda PUAC aperta
					String strDt = "";
					if (dbrPuac.get("pr_data_avvio") != null) {
						strDt = "" + dbrPuac.get("pr_data_avvio");
						strDt = strDt.substring(8, 10) + "/" + strDt.substring(5, 7) + "/" + strDt.substring(0, 4);
						htResult.put("pr_data_avvio", strDt);
					} else
						htResult.put("pr_data_avvio", "");
					return htResult;
				}
				// 12/03/08 ----------------------------------------------------------------------
			}
			dbcur_1.close();

			String strCognome = (String) dbrCart.get("cognome");
			strCognome = duplicateChar(strCognome, "'"); // 06/06/08
			String strNome = (String) dbrCart.get("nome");
			strNome = duplicateChar(strNome, "'"); // 06/06/08
			String strCodComNasc = (String) dbrCart.get("cod_com_nasc");
			String strDataNasc = "" + dbrCart.get("data_nasc");
			String strCodFisc = (String) dbrCart.get("cod_fisc");

			String strQueryAssAnag = "SELECT * FROM ass_anagrafica" + " WHERE cognome = '" + strCognome + "'" + " AND nome = '" + strNome
					+ "'" + " AND comune_nascita = '" + strCodComNasc
					+ "'"
					+ " AND data_nascita = "
					+ formatDate(dbc, strDataNasc)
					+ " AND cod_fis = '"
					+ strCodFisc
					+ "'"
					// 24/03/09 " AND inserita_autom IN ('S','R')" +
					// 24/11/09 " AND esito_contatto = '6'" +
					+ " AND esito_contatto IN ('6', '15')" + " AND data_reg = ("
					+ " SELECT MAX(data_reg) max_data_reg FROM ass_anagrafica a" + " WHERE a.cognome = '" + strCognome + "'"
					+ " AND a.nome = '" + strNome + "'" + " AND a.comune_nascita = '" + strCodComNasc + "'" + " AND a.data_nascita = "
					+ formatDate(dbc, strDataNasc) + " AND a.cod_fis = '" + strCodFisc + "'"
					// 24/03/09 " AND inserita_autom IN ('S','R')" +
					// 24/11/09 " AND esito_contatto = '6' )";
					+ " AND a.esito_contatto IN ('6', '15')" + ")";

			System.out.println("ElencocasiEJB/checkEsistenzaSegnalPUAC/strQueryAssAnag: " + strQueryAssAnag);

			// 24/03/09 ISASRecord dbrAssAnag = dbc.readRecord(strQueryAssAnag);

			// 24/03/09: in modo che ISAS elimini i record che non riguardano la zona ed il
			// controllo sul valore del campo "presa_carico" sia fatto sul record (se esiste) della zona giusta.
			ISASCursor dbcur_2 = dbc.startCursor(strQueryAssAnag);
			htResult.put("ret_code", "NoExistAssAnag"); // 2. Rec. ass_anagrafica non esistente

			while (dbcur_2.next()) {
				ISASRecord dbrAssAnag = dbcur_2.getRecord();
				// 24/03/09 ----------------
				if (dbrAssAnag == null) {
					htResult.put("ret_code", "NoExistAssAnag"); // 2. Rec. ass_anagrafica non esistente
				} else {
					if (dbrAssAnag.get("presa_carico") != null) {
						String strPresaCarico = (String) dbrAssAnag.get("presa_carico");
						if (strPresaCarico.equals("N")) {
							// 16/04/12: se segnalazione inserita per REVISIONE SUCCESSIVA da PUAC, si permette il nuovo inserimento
							if ((dbrAssAnag.get("inserita_autom") != null)
									&& (((String) dbrAssAnag.get("inserita_autom")).trim().equals("R")))
								htResult.put("ret_code", "NoExistAssAnag");
							else {
								htResult.put("ret_code", "PreCar_N"); // 3. presa_carico = 'N' in rec. ass_anagrafica.
								String strDt = "";
								if (dbrAssAnag.get("data_reg") != null) {
									strDt = "" + dbrAssAnag.get("data_reg");
									strDt = strDt.substring(8, 10) + "/" + strDt.substring(5, 7) + "/" + strDt.substring(0, 4);
									htResult.put("data_reg", strDt);
								} else
									htResult.put("data_reg", "");
							}
						} else if (strPresaCarico.equals("S")) {
							htResult.put("ret_code", "PreCar_S"); // 4. presa_carico = 'S' in rec. ass_anagrafica.
							String strDt = "";
							if (dbrAssAnag.get("presa_carico_data") != null) {
								strDt = "" + dbrAssAnag.get("presa_carico_data");
								strDt = strDt.substring(8, 10) + "/" + strDt.substring(5, 7) + "/" + strDt.substring(0, 4);
								htResult.put("presa_carico_data", strDt);
							} else
								htResult.put("presa_carico_data", "");
						} else if (strPresaCarico.equals("C")) {
							htResult.put("ret_code", "PreCar_C"); // 5. presa_carico = 'C' in rec. ass_anagrafica.
							String strDt = "";
							if (dbrAssAnag.get("presa_carico_data") != null) {
								strDt = "" + dbrAssAnag.get("presa_carico_data");
								strDt = strDt.substring(8, 10) + "/" + strDt.substring(5, 7) + "/" + strDt.substring(0, 4);
								htResult.put("presa_carico_data", strDt);
							} else
								htResult.put("presa_carico_data", "");
							decodificaTB_SINSS(dbc, dbrAssAnag, "presa_carico_motivo", "PUACHIUS", "presa_carico_motivo_descr");
							htResult.put("presa_carico_motivo_descr", (String) dbrAssAnag.get("presa_carico_motivo_descr"));
						} else
							htResult.put("ret_code", "PreCar_?"); // 6. presa_carico non valido in rec. ass_anagrafica.
					} else
						htResult.put("ret_code", "PreCar_?"); // 6. presa_carico non valido in rec. ass_anagrafica.
				}
			}
			dbcur_2.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return htResult;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		}

		catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("ElencocasiEJB: Errore eseguendo la checkEsistenzaSegnalPUAC() - " + e1);
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

	// 13/03/08: chiamato da JFrameASAssProgetto e JFrameSkInf
	// Inserisce un record in ass_anagrafica prendendo i dati dell'assistito da 'cartella' e 'anagra_c' e
	// settando il campo 'inserita_autom = 'S'', il campo 'presa_carico = 'N'' e esito_contatto = '6'.
	// In questo modo l'assistito viene segnalato al PUAC (caso semplice--> caso complesso)
	// Prima di chiamare questo metodo dal client si � lanciato un controllo su
	// ass_anagrafica: metodo EJB checkEsistenzaSegnalPUAC
	public ISASRecord insertRecPUAC(myLogin mylogin, Hashtable h0) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);

			ISASRecord dbrCart = getDbrDaCartella(dbc, h0);
			if (dbrCart == null) {
				String msg = "Attenzione: Non esiste il record sulla tabella CARTELLA!";
				throw new CariException(msg, -2);
			}
			ISASRecord dbrAnagC = getDbrDaAnagra_c(dbc, h0);
			if (dbrAnagC == null) {
				String msg = "Attenzione: Non esiste il record sulla tabella ANAGRA_C!";
				throw new CariException(msg, -2);
			}

			ISASRecord dbr = dbc.newRecord("ass_anagrafica");

			fillUpAssAnagrDaCartella(dbr, dbrCart);
			fillUpAssAnagrDaAnagra_c(dbr, dbrAnagC);

			fillUpAssAnagrCampiCritici(dbc, dbr, h0);

			int intProgressivo = selectProgressivo(dbc, "ASSOC_PROGR_ANAG");
			dbr.put("progressivo", new Integer(intProgressivo));
			System.out.println("ElencocasiEJB/insertRecPUAC/intProgressivo: " + intProgressivo);

			dbc.writeRecord(dbr);

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
		}

		catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("ElencocasiEJB: Errore eseguendo la insertRecPUAC() - " + e1);
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

	private ISASRecord getDbrDaCartella(ISASConnection dbc, Hashtable h) throws SQLException {
		try {
			String strQuery = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + (String) h.get("n_cartella");
			System.out.println("ElencocasiEJB/getDbrDaCartella/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("ElencocasiEJB/getDbrDaCartella - " + e1);
		}
	}

	private ISASRecord getDbrDaAnagra_c(ISASConnection dbc, Hashtable h) throws SQLException {
		try {
			String strQuery = "SELECT *" + " FROM anagra_c" + " WHERE n_cartella = " + (String) h.get("n_cartella")
					+ " AND data_variazione = (" + " SELECT MAX(data_variazione) max_data_var" + " FROM anagra_c a"
					+ " WHERE a.n_cartella = " + (String) h.get("n_cartella") + ")";
			System.out.println("ElencocasiEJB/getDbrDaAnagra_c/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			return dbr;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("ElencocasiEJB/getDbrDaAnagra_c - " + e1);
		}
	}

	private void fillUpAssAnagrDaCartella(ISASRecord dbr, ISASRecord dbrCart) throws SQLException {
		try {
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
			throw new SQLException("ElencocasiEJB/fillUpAssAnagrDaCartella - " + e1);
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
			throw new SQLException("ElencocasiEJB/fillUpAssAnagrDaAnagra_c - " + e1);
		}
	}

	private void fillUpAssAnagrCampiCritici(ISASConnection mydbc, ISASRecord dbr, Hashtable h) throws SQLException {
		try {
			dbr.put("data_reg", h.get("data_reg"));
			dbr.put("invio_puac_data", h.get("invio_puac_data"));
			dbr.put("urgente", h.get("urgente"));
			dbr.put("cod_operatore", h.get("cod_operatore"));
			dbr.put("cod_presidio", h.get("cod_presidio"));

			dbr.put("segn_cognome", h.get("segn_cognome"));
			dbr.put("segn_nome", h.get("segn_nome"));
			dbr.put("segn_comune_nas", h.get("segn_comune_nas"));
			dbr.put("segn_data_nas", h.get("segn_data_nas"));
			dbr.put("segn_indirizzo", h.get("segn_indirizzo"));
			dbr.put("segn_telefono", h.get("segn_telefono"));
			dbr.put("segn_comune_res", h.get("segn_comune_res"));
			dbr.put("segn_rapporto", h.get("segn_rapporto"));

			dbr.put("rif_cognome", h.get("rif_cognome"));
			dbr.put("rif_nome", h.get("rif_nome"));
			dbr.put("rif_indirizzo", h.get("rif_indirizzo"));
			dbr.put("rif_telefono", h.get("rif_telefono"));
			dbr.put("rif_rapporto", h.get("rif_rapporto"));

			dbr.put("bisogno_rich_ad", h.get("bisogno_rich_ad"));
			dbr.put("bisogno_rich_contrib", h.get("bisogno_rich_contrib"));
			dbr.put("bisogno_rich_rsa", h.get("bisogno_rich_rsa"));
			dbr.put("bisogno_rich_ric", h.get("bisogno_rich_ric"));
			dbr.put("bisogno_rich_altro", h.get("bisogno_rich_altro"));
			dbr.put("bisogno_rich_altro_spec", h.get("bisogno_rich_altro_spec"));
			dbr.put("arrivato_note", h.get("arrivato_note"));

			dbr.put("segn_tipo", "1");
			dbr.put("inserita_autom", "S");
			dbr.put("esito_contatto", "6");
			dbr.put("presa_carico", "N");
			// 06/06/08 ---
			dbr.put("sospesa_flag", "N");

			// 07/10/08: campi relativi all'oper che genera la richValutazSucc ---
			String tpOper = (String) h.get("tp_oper");
			String prefix = (tpOper.equals(this.TP_OPER_AS) ? "soc" : "san");
			dbr.put(prefix + "_cod", h.get("cod_operatore"));
			dbr.put(prefix + "_data", "" + h.get("data_reg"));
			dbr.put(prefix + "_carico", "S");
			// 07/10/08 ---

/*** 19/04/13: lettura di tutto il record su OPERATORI perch� serve anche il codZona oltre al codPresidio			
			// 10/11/08 ----
			String codPres = "";
			if ((h.get("cod_operatore") != null) && (!((String) h.get("cod_operatore")).trim().equals("")))
				codPres = (String) it.pisa.caribel.util.ISASUtil.getDecode(mydbc, "operatori", "codice", (String) h.get("cod_operatore"),
						"cod_presidio");
			dbr.put(prefix + "_cod_presidio", codPres);
			// 10/11/08 ----
***/			
			// 19/04/13 ----
			ISASRecord dbrOper = null;
			String codPres = "";
			if ((h.get("cod_operatore") != null) && (!((String) h.get("cod_operatore")).trim().equals("")))
				dbrOper = (ISASRecord)leggiOperatore(mydbc, (String) h.get("cod_operatore"));
			if ((dbrOper != null) && (dbrOper.get("cod_presidio") != null))
				codPres = (String)dbrOper.get("cod_presidio");
			dbr.put(prefix + "_cod_presidio", codPres);
			// 19/04/13 ----
			
			// 12/12/08: per l'altro tipo di operatore ---
			String prefixAltro = (tpOper.equals(this.TP_OPER_AS) ? "san" : "soc");
			dbr.put(prefixAltro + "_carico", "N");
			// 12/12/08: per l'altro tipo di operatore ---
			
			// 19/04/13 ---
			String zonaSegn = "";
			if ((dbrOper != null) && (dbrOper.get("cod_zona") != null))
				zonaSegn = (String)dbrOper.get("cod_zona");
			dbr.put("zona_segnalazione", zonaSegn);
			// 19/04/13 ---			

			// 09/12/08
			dbr.put("n_cartella", (String) h.get("n_cartella"));
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("ElencocasiEJB/fillUpAssAnagrCampiCritici - " + e1);
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
			System.out.println("SocElecasiEJB/updateAgendant_pua/strQuery: " + strQuery);
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

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		String strProgressivo = null;
		ISASConnection dbc = null;

		try {
			strProgressivo = (String) dbr.get("progressivo");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);

			dbc.startTransaction(); // gb 14/01/08

			/***** 11/09/06
			      //GB 28/06/06 Si setta il campo 'presa_carico' con 'S'
			      dbr.put("presa_carico","S");

			      String myselect = "SELECT *" +
			                        " FROM ass_anagrafica" +
			                        " WHERE progressivo = " + strProgressivo;
			      System.out.println("ElencocasiEJB/update: " + myselect);
			      ISASRecord dbr_2 = dbc.readRecord(myselect);

			      it.pisa.caribel.util.ServerUtility servUtil = new it.pisa.caribel.util.ServerUtility();
			      servUtil.mergeIsasRecord(dbr, dbr_2);
			******/

			/************* 07/10/08
			//gb 14/01/08 
				String strDtAppuntamento = null;
				String strOraAppuntamento = null;
				String strDtDataApp = (String)dbr.get("ag_data_app");
				if ((strDtDataApp != null) && (!strDtDataApp.trim().equals("")))
				{
				  strOraAppuntamento = (String) dbr.get("ag_ora_app");
				  updateAgendant_pua(dbc, dbr);
			 	}
			//gb 14/01/08: fine
			**************/

			// Scrivo il record nella tabella
			dbc.writeRecord(dbr);
			// Si crea la query per rileggere il record appena aggiornato
			String myselect = "SELECT *" + " FROM ass_anagrafica" + " WHERE progressivo = " + strProgressivo;
			// Si legge il record appena aggioranto
			dbr = dbc.readRecord(myselect);

			// Si decodificano i alcuni campi della maschera a video.
			if (dbr != null) {
				decodificaComune(dbc, dbr, "comune_nascita", "comune_nascita_descr");
				decodificaComune(dbc, dbr, "comune_res", "comune_res_descr");
				decodificaComune(dbc, dbr, "comune_dom", "comune_dom_descr");
				decodificaComune(dbc, dbr, "segn_comune_nas", "segn_comune_nas_descr");
				decodificaComune(dbc, dbr, "segn_comune_res", "segn_comune_res_descr");

				decodificaTB_SINSS(dbc, dbr, "settore", "SETTORE", "settore_descr");
				decodificaTB_SINSS(dbc, dbr, "segn_rapporto", "SEGNRAPP", "segn_rapporto_descr");
				decodificaTB_SINSS(dbc, dbr, "arrivato", "ARRISERV", "arrivato_descr");
				decodificaTB_SINSS(dbc, dbr, "motivo", "MOTIVO", "motivo_descr");
				decodificaTB_SINSS(dbc, dbr, "esito_contatto", "ESITOCON", "esito_contatto_descr");

				decodificaCaricoServ(dbr, "carico_serv_descr");

				decodificaMedico(dbc, dbr, "mecodi", "mecodi_descr");

				// gb 27/12/07
				decodificaTB_SINSS(dbc, dbr, "str_tipo_doc", "DOCSOGG", "str_tipo_doc_descr");
				decodificaTB_SINSS(dbc, dbr, "area_interv", "AREAINTE", "area_interv_descr");
				decodificaTB_SINSS(dbc, dbr, "rif_rapporto", "SEGNRAPP", "rif_rapporto_descr");
				decodificaTB_SINSS(dbc, dbr, "tipo_informazione", "TIPOINFO", "tipo_informazione_descr");
				decodificaTB_SINSS(dbc, dbr, "tipo_servizio", "TIPOSERV", "tipo_servizio_descr");

				decodificaCittadin(dbc, dbr, "nazionalita", "nazionalita_descr");
				decodificaCittadin(dbc, dbr, "cittadinanza", "cittadinanza_descr");

				decodificaAreaDis(dbc, dbr, "areadis_res", "areadis_res_descr");
				decodificaAreaDis(dbc, dbr, "areadis_dom", "areadis_dom_descr");
				decodificaAreaDis(dbc, dbr, "areadis_rep", "areadis_rep_descr");

				decodificaTipoSegnalaz(dbr, "segn_tipo_descr");

				decodificaComune(dbc, dbr, "rif_comune_res", "rif_comune_res_descr");
				decodificaComune(dbc, dbr, "comune_rep", "comune_rep_descr");
				// gb 27/12/07

				/************* 07/10/08
				//gb 14/01/08
					if ((strDtDataApp != null) && !strDtDataApp.equals(""))
					  {
					  dbr.put("ag_data_app", strDtDataApp);
					  dbr.put("ag_ora_app", strOraAppuntamento);
					  }
				//gb 14/01/08: fine
				**************/

			}

			dbc.commitTransaction(); // gb 14/01/08

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("ElencocasiEJB: Errore eseguendo la update() - " + e);
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

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		}

		catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		}

		catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - " + e1);
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

	// 28/08/06 m.: ctrl esistenza assistito su CARTELLA, progetto aperto su ASS_PROGETTI ed
	// abilitazione oper su ASS_OPABIL. Ritorna un'hashtable con le 3 chiavi "n_cartella",
	// "n_progetto" ed "abil", pi�, eventualemente, se l'assistito non esiste su CARTELLA
	// ma esiste su FASSI, i campi letti da FASSI.
	// 06/12/07 m.: aggiunto tipoOper
	public Hashtable checkAssistito(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		// hashtable che ritorner� al client
		Hashtable h_ret = new Hashtable();
		int n_cartella = -1;
		int n_progetto = -1;

		// gb 05/03/07
		String dataSkVal = "";
		String motivoSkVal = "";
		String dataSkValCarico = ""; // 11/10/07
		Hashtable htSkVal = new Hashtable();
		// gb 05/03/07

		// 06/12/07
		String tipoOper = (String) h.get("tipoOper");

		boolean abil = true;
		boolean esisteSuFassi = false;

		try {
			dbc = super.logIn(mylogin);

			// 1) ctrl esistenza su CARTELLA
			n_cartella = checkAssSuCartella(dbc, h);

			if (n_cartella > 0) {// esiste assistito
				// gb 05/03/07: prendo, se esiste, la data variazione (pr_data)
				// dal record della tabella 'progetto'.
				try { // 04/04/08
					checkInfoSkVal(dbc, n_cartella, htSkVal);
					dataSkVal = (String) htSkVal.get("data_apertura");
					motivoSkVal = (String) htSkVal.get("motivo");
					dataSkValCarico = (String) htSkVal.get("pr_data_carico");// 11/10/07
				} catch (ISASPermissionDeniedException e) { // 04/04/08
					System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab Progetto");
					dataSkVal = "31/12/9999";
				}

				if ((!dataSkVal.equals("")) && (!dataSkVal.equals("31/12/9999"))) {
					if ((tipoOper != null) && (!tipoOper.trim().equals(this.TP_OPER_PUAC))
							&& (!tipoOper.trim().equals(this.TP_OPER_PUACD))) { // 06/12/07: solo per ass soc e inferm
						try { // 11/06/07
							// 1.1) ctrl esistenza prog aperto su ASS_PROGETTI
							// 06/12/07: oppure esistenza cont aperto su SKINF
							n_progetto = checkPrgSuAssProgetti(dbc, n_cartella, tipoOper);
						} catch (ISASPermissionDeniedException e) {
							System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab AssProgetto/SkInf");
							n_progetto = -999;
						}

						if ((n_progetto > 0) // esiste prog aperto
								&& ((tipoOper != null) && (tipoOper.trim().equals(this.TP_OPER_AS)))) { // 06/12/07: solo per ass soc
							// 1.1.1) ctrl esistenza abil oper su ASS_OPERABIL
							abil = checkAbilSuAssOpAbil(dbc, h, n_cartella, n_progetto);
						}
					} else if ((tipoOper != null) && (tipoOper.trim().equals(this.TP_OPER_PUAC))) { // 08/04/08: solo per oper PUAC
						try {
							// ctrl esistenza skPuac su PUAUVM
							n_progetto = checkSkPuaSuPuauvm(dbc, n_cartella, dataSkVal);
						} catch (ISASPermissionDeniedException e) {
							System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab Puauvm");
							n_progetto = -999;
						}
					}else if ((tipoOper != null) && (tipoOper.trim().equals(this.TP_OPER_PUACD))) { // 08/04/08: solo per oper PUACD
						try {
							// ctrl esistenza skPuac su PUAUVMD disabili
							n_progetto = checkSkPuaSuPuauvmD(dbc, n_cartella, dataSkVal);
						} catch (ISASPermissionDeniedException e) {
							System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab Puauvm");
							n_progetto = -999;
						}
					}
				}
			} else {// non esiste assistito su CARTELLA
				// 2) ctrl esistenza su FASSI
				esisteSuFassi = checkAssSuFassi(dbc, h, h_ret);
			}
			// System.out.println("checkAssistito uscita 1");
			h_ret.put("n_cartella", new Integer(n_cartella));
			h_ret.put("n_progetto", new Integer(n_progetto));
			h_ret.put("data_skval", (String) dataSkVal); // gb 05/03/07
			h_ret.put("motivo_skval", (String) motivoSkVal); // gb 05/03/07
			h_ret.put("pr_data_carico", (String) dataSkValCarico); // 11/10/07
			h_ret.put("abil", new Boolean(abil));
			h_ret.put("esiste_fassi", new Boolean(esisteSuFassi));
			// System.out.println("checkAssistito uscita 2");

			dbc.close();
			super.close(dbc);
			done = true;

			return h_ret;
		} catch (Exception e) {
			System.out.println("SocElencocasiEJB.checkAssistito(): " + e);
			throw new SQLException("Errore eseguendo una checkAssistito()");
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
	}// END checkAssistito

	// 28/08/06 m.: ctrl esistenza assistito su CARTELLA
	private int checkAssSuCartella(ISASConnection mydbc, Hashtable h0) throws Exception {
		int nCart = -1;
		String strCognome = (String) h0.get("cognome");
		strCognome = duplicateChar(strCognome, "'"); // 06/06/08
		String strNome = (String) h0.get("nome");
		strNome = duplicateChar(strNome, "'"); // 06/06/08
		String strCodComNasc = (String) h0.get("comune_nascita");
		String strDataNasc = (String) h0.get("data_nascita");

		String myselect = "SELECT n_cartella" + " FROM cartella" + " WHERE cognome = '" + strCognome + "'" + " AND nome = '" + strNome
				+ "'" + " AND cod_com_nasc = '" + strCodComNasc + "'" + " AND data_nasc = " + formatDate(mydbc, strDataNasc);

		System.out.println("SocElencoCasiEJB/checkAssSuCartella: myselect: [" + myselect + "]");

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			nCart = ((Integer) dbr.get("n_cartella")).intValue();

		return nCart;
	}// END checkAssSuCartella

	// gb 05/03/07: prende la data 'pr_data' e il 'pr_motivo_val_ap' nel record
	// della tabella 'progetto'.
	private void checkInfoSkVal(ISASConnection mydbc, int n_cartella, Hashtable htSkVal) throws Exception {
		String dataSkVal = "";
		String motivoSkVal = "";
		String dataSkValCarico = ""; // 11/10/07

		String myselect = "SELECT *" + " FROM progetto" + " WHERE n_cartella = " + n_cartella + " AND pr_data_chiusura IS NULL";

		System.out.println("SocElencoCasiEJB/checkInfoSkVal: myselect: [" + myselect + "]");

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null) {
			dataSkVal = ((java.sql.Date) dbr.get("pr_data")).toString();
			dataSkVal = dataSkVal.substring(8, 10) + "/" + dataSkVal.substring(5, 7) + "/" + dataSkVal.substring(0, 4);
			motivoSkVal = (String) decodificaTabVoci(mydbc, (String) dbr.get("pr_motivo_val_ap"), "PRMOAP");
			// 11/10/07 --
			if (dbr.get("pr_data_carico") != null) {
				dataSkValCarico = ((java.sql.Date) dbr.get("pr_data_carico")).toString();
				dataSkValCarico = dataSkValCarico.substring(8, 10) + "/" + dataSkValCarico.substring(5, 7) + "/"
						+ dataSkValCarico.substring(0, 4);
			}
			// 11/10/07 --
			htSkVal.put("data_apertura", dataSkVal);
			htSkVal.put("motivo", motivoSkVal);
			htSkVal.put("pr_data_carico", dataSkValCarico); // 11/10/07
		} else {
			htSkVal.put("data_apertura", "");
			htSkVal.put("motivo", "");
			htSkVal.put("pr_data_carico", ""); // 11/10/07
		}
	} // END checkDataSkValSuProgetto

	// 28/08/06 m.: ctrl esistenza progetto aperto per l'assistito su ASS_PROGETTI
	// 06/12/07 m.: oppure ctrl esistenza contatto aperto per l'assistito su SKINF
	private int checkPrgSuAssProgetti(ISASConnection mydbc, int n_cartella, String tpOper) throws Exception {
		int nPrg = -1;
		String myselect = "";

		if ((tpOper != null) && (tpOper.trim().equals(this.TP_OPER_AS))) {
			myselect = "SELECT ap.*" + " FROM ass_progetto ap" + " WHERE ap.n_cartella = " + n_cartella
					+ " AND ap.ap_data_chiusura IS NULL";
		} else if ((tpOper != null) && (tpOper.trim().equals(this.TP_OPER_IP))) {
			// si usa un ALIAS per riportare il nome del campo a quello estratto dall'altra select
			myselect = "SELECT s.*," + " s.n_contatto n_progetto" + " FROM skinf s" + " WHERE s.n_cartella = " + n_cartella
					+ " AND s.ski_data_uscita IS NULL";
		}
		System.out.println("SocElencoCasiEJB/checkPrgSuAssProgetti-SkInf: myselect: [" + myselect + "]");

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			nPrg = ((Integer) dbr.get("n_progetto")).intValue();

		return nPrg;
	}// END checkPrgSuAssProgetti

	// 08/04/08: si cerca esistenza scheda Puac non conclusa (dtVerbale=null e parereMmg=N)
	// 06/10/08: anche SENZA dtChiusura
	private int checkSkPuaSuPuauvm(ISASConnection mydbc, int n_cartella, String stSkVal) throws Exception {
		int nPrg = -1;

		// 20/09/11
		String nmTabSkPuac = eveUtl.getNmTabSkPuac(mydbc);

		String myselect = "SELECT p.*" + " FROM " + nmTabSkPuac + " p" + " WHERE p.n_cartella = " + n_cartella + " AND p.pr_data = "
				+ formatDate(mydbc, stSkVal) + " AND p.pr_data_verbale_uvm IS NULL";

		// 20/09/11 -----
		if (eveUtl.existsFldInTab(mydbc, nmTabSkPuac, "pr_mmg_risposta_neg"))
			myselect += " AND ((p.pr_mmg_risposta_neg <> 'S')" + " OR (p.pr_mmg_risposta_neg IS NULL))";

		if (eveUtl.existsFldInTab(mydbc, nmTabSkPuac, "pr_data_chiusura"))
			myselect += " AND p.pr_data_chiusura IS NULL";// 06/10/08
		// 20/09/11 -----

		System.out.println("SocElencoCasiEJB/checkSkPuaSuPuauvm: myselect: [" + myselect + "]");

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			nPrg = ((Integer) dbr.get("pr_progr")).intValue();

		return nPrg;
	}// END checkSkPuaSuPuauvm
	// 08/04/08: si cerca esistenza scheda Puac non conclusa (dtVerbale=null e parereMmg=N)
	// 06/10/08: anche SENZA dtChiusura
	private int checkSkPuaSuPuauvmD(ISASConnection mydbc, int n_cartella, String stSkVal) throws Exception {
		int nPrg = -1;

		// 20/09/11
//		String nmTabSkPuac = eveUtl.getNmTabSkPuac(mydbc);
		String nmTabSkPuac = TABELLA_GESTIONE_PUAUVMD ;

		String myselect = "SELECT p.*" + " FROM " + nmTabSkPuac + " p" + " WHERE p.n_cartella = " + n_cartella + " AND p.pr_data = "
				+ formatDate(mydbc, stSkVal) + " AND p.pr_data_verbale_uvm IS NULL";

		// 20/09/11 -----
		if (eveUtl.existsFldInTab(mydbc, nmTabSkPuac, "pr_mmg_risposta_neg"))
			myselect += " AND ((p.pr_mmg_risposta_neg <> 'S')" + " OR (p.pr_mmg_risposta_neg IS NULL))";

		if (eveUtl.existsFldInTab(mydbc, nmTabSkPuac, "pr_data_chiusura"))
			myselect += " AND p.pr_data_chiusura IS NULL";// 06/10/08
		// 20/09/11 -----

		System.out.println("SocElencoCasiEJB/checkSkPuaSuPuauvm: myselect: [" + myselect + "]");

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			nPrg = ((Integer) dbr.get("pr_progr")).intValue();

		return nPrg;
	}// END checkSkPuaSuPuauvm

	// 28/08/06 m.: ctrl esistenza abilitazione oper per il prog aperto su ASS_OPABIL
	private boolean checkAbilSuAssOpAbil(ISASConnection mydbc, Hashtable h0, int n_cartella, int n_progetto) throws Exception {
		String oper = (String) h0.get("codice_operatore");

		String myselect = "SELECT o.opabil_liv" + " FROM ass_opabil o" + " WHERE o.n_cartella = " + n_cartella + " AND o.n_progetto = "
				+ n_progetto + " AND o.opabil_cod = '" + oper + "'";

		System.out.println("SocElencoCasiEJB/checkAbilSuAssOpAbil: myselect: [" + myselect + "]");

		ISASRecord dbr = mydbc.readRecord(myselect);

		return (dbr != null);
	}// END checkAbilSuAssOpAbil

	// 28/08/06 m.: ctrl esistenza assistito su FASSi
	private boolean checkAssSuFassi(ISASConnection mydbc, Hashtable h0, Hashtable h_ret) throws Exception {
		String strCognome = (String) h0.get("cognome");
		strCognome = duplicateChar(strCognome, "'"); // 06/06/08
		String strNome = (String) h0.get("nome");
		strNome = duplicateChar(strNome, "'"); // 06/06/08
		String strCodComNasc = (String) h0.get("comune_nascita");
		String strDataNasc = (String) h0.get("data_nascita");

		String myselect = "SELECT *" + " FROM fassi" + " WHERE cognome = '" + strCognome + "'" + " AND nome = '" + strNome + "'"
				+ " AND comunenas = '" + strCodComNasc + "'" + " AND datans = " + formatDate(mydbc, strDataNasc);

		System.out.println("SocElencoCasiEJB/checkAssSuFassi: myselect: [" + myselect + "]");

		ISASRecord dbrF = mydbc.readRecord(myselect);

		System.out.println("checkAssSuFassi subito dopo readRecord");
		if (dbrF != null) {
			// decodifiche
			String descComNas = decodificaComune(mydbc, dbrF, "comunenas", "comunenas_descr");
			String descComRes = decodificaComune(mydbc, dbrF, "comune_res", "comune_res_descr");
			String descComDom = decodificaComune(mydbc, dbrF, "comune_dom", "comune_dom_descr");
			String descMedico = decodificaMedico(mydbc, dbrF, "cod_medico", "cod_medico_descr");
			// 24/08/09
			String descAreaRes = decodificaAreaDis(mydbc, dbrF, "areadis_res", "areadis_res_descr");
			String descAreaDom = decodificaAreaDis(mydbc, dbrF, "areadis_dom", "areadis_dom_descr");

			// inserisco nell'hashtable di ritorno i valori
			h_ret.put("cognome", strCognome);
			h_ret.put("nome", strNome);
			h_ret.put("comunenas", checkCod((String) dbrF.get("comunenas"), descComNas, COMUNE_COD_DEFAULT));
			h_ret.put("datans", strDataNasc);

			// valori letti da FASSI
			System.out.println("checkAssSuFassi subito prima di leggere i dati da fassi 1");
			h_ret.put("sesso", (String) dbrF.get("sesso"));
			h_ret.put("afisc", (String) dbrF.get("afisc"));
			h_ret.put("comune_dom", checkCod((String) dbrF.get("comune_dom"), descComDom, COMUNE_COD_DEFAULT));
			h_ret.put("indirizzo_dom", (String) dbrF.get("indirizzo_dom"));
			h_ret.put("cap_dom", (String) dbrF.get("cap_dom"));
			h_ret.put("areadis_dom", (String) dbrF.get("areadis_dom"));
			h_ret.put("comune_res", checkCod((String) dbrF.get("comune_res"), descComRes, COMUNE_COD_DEFAULT));
			h_ret.put("indirizzo_res", (String) dbrF.get("indirizzo_res"));
			h_ret.put("cap_res", (String) dbrF.get("cap_res"));
			h_ret.put("areadis_res", (String) dbrF.get("areadis_res"));
			h_ret.put("codicereg", (String) dbrF.get("codicereg"));
			h_ret.put("flag", (String) dbrF.get("flag"));
			h_ret.put("cod_medico", checkCod((String) dbrF.get("cod_medico"), descMedico, MEDICO_COD_DEFAULT));
			// gb 29/05/07 h_ret.put("cod_usl",(String)dbrF.get("cod_usl"));
			// gb 29/05/07
			if (dbrF.get("cod_usl") == null)
				h_ret.put("cod_usl", "");
			else
				h_ret.put("cod_usl", "" + dbrF.get("cod_usl"));
			// gb 29/05/07: fine
			java.sql.Date dtDecesso = (java.sql.Date) dbrF.get("data_decesso");
			h_ret.put("data_decesso", (String) (dtDecesso != null ? dtDecesso.toString() : ""));
			h_ret.put("motivo_var", (String) dbrF.get("motivo_var"));
			h_ret.put("numtel1", (String) dbrF.get("numtel1"));
			h_ret.put("numtel2", (String) dbrF.get("numtel2"));
			// gb 29/05/07 h_ret.put("cittadinanza",(String)dbrF.get("cittadinanza"));
			// gb 29/05/07
			if (dbrF.get("cittadinanza") == null)
				h_ret.put("cittadinanza", "");
			else
				h_ret.put("cittadinanza", "" + dbrF.get("cittadinanza"));
			// gb 29/05/07: fine
			h_ret.put("comunenas_descr", (String) dbrF.get("comunenas_descr"));
			h_ret.put("comune_res_descr", (String) dbrF.get("comune_res_descr"));
			h_ret.put("comune_dom_descr", (String) dbrF.get("comune_dom_descr"));
			h_ret.put("cod_medico_descr", (String) dbrF.get("cod_medico_descr"));
			// 24/08/09
			h_ret.put("areadis_dom_descr", (String) dbrF.get("areadis_dom_descr"));
			h_ret.put("areadis_res_descr", (String) dbrF.get("areadis_res_descr"));

			System.out.println("checkAssSuFassi subito dopo aver letto i dati da fassi 1");
		}
		return (dbrF != null);
	}// END checkAssSuFassi

	// 28/08/06 m.: ctrl esistenza codici sulle tabelle di decodifica
	private String checkCod(String cod, String decod, String codDefault) throws Exception {
		if ((decod != null) && (!decod.trim().equals("")))
			return cod;
		return codDefault;
	} // END checkCod

	// 11/09/06 m.: ctrl esistenza progetto aperto e relativa abil oper
	// 06/12/07 m.: aggiunto tipoOper
	public Hashtable checkProgetto(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		String punto = MIONOME + "";
		
		stampa(punto + " dati che mi arrivano>"+h+ "<\n");
		// hashtable che ritorner� al client
		Hashtable h_ret = new Hashtable();
		int n_progetto = -1;
		// gb 05/03/07
		String dataSkVal = "";
		String motivoSkVal = "";
		String dataSkValCarico = ""; // 11/10/07
		Hashtable htSkVal = new Hashtable();
		// gb 05/03/07
		boolean abil = true;

		// 06/12/07
		String tipoOper = (String) h.get("tipoOper");

		try {
			dbc = super.logIn(mylogin);

			int n_cartella = Integer.parseInt((String) h.get("n_cartella"));

			// gb 05/03/07: prendo, se esiste, la data variazione (pr_data)
			// dal record della tabella 'progetto'.
			try { // 04/04/08
				checkInfoSkVal(dbc, n_cartella, htSkVal);
				dataSkVal = (String) htSkVal.get("data_apertura");
				motivoSkVal = (String) htSkVal.get("motivo");
				dataSkValCarico = (String) htSkVal.get("pr_data_carico");// 11/10/07
			} catch (ISASPermissionDeniedException e) { // 04/04/08
				System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab Progetto");
				dataSkVal = "31/12/9999";
			}

			if ((!dataSkVal.equals("")) && (!dataSkVal.equals("31/12/9999"))) {
				if ((tipoOper != null) && (!tipoOper.trim().equals(this.TP_OPER_PUAC)) 
						&& (!tipoOper.trim().equals(this.TP_OPER_PUACD) )) { // 06/12/07: solo per ass soc e inferm
					try { // 11/06/07
						// 1.1) ctrl esistenza prog aperto su ASS_PROGETTI
						// 06/12/07: oppure esistenza cont aperto su SKINF
						n_progetto = checkPrgSuAssProgetti(dbc, n_cartella, tipoOper);
					} catch (ISASPermissionDeniedException e) {
						System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI");
						n_progetto = -999;
					}

					if ((n_progetto > 0) // esiste prog aperto
							&& ((tipoOper != null) && (tipoOper.trim().equals(this.TP_OPER_AS)))) { // 06/12/07: solo per ass soc
						// 1.1.1) ctrl esistenza abil oper su ASS_OPERABIL
						abil = checkAbilSuAssOpAbil(dbc, h, n_cartella, n_progetto);
					}
				} else if ((tipoOper != null) && (tipoOper.trim().equals(this.TP_OPER_PUAC))) { // 08/04/08: solo per oper PUAC
					try {
						// ctrl esistenza skPuac su PUAUVM
						n_progetto = checkSkPuaSuPuauvm(dbc, n_cartella, dataSkVal);
					} catch (ISASPermissionDeniedException e) {
						System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab Puauvm");
						n_progetto = -999;
					}
				} else if ((tipoOper != null) && (tipoOper.trim().equals(this.TP_OPER_PUACD))) { // 08/04/08: solo per oper PUACD
					try {
						// ctrl esistenza skPuac su PUAUVMD
						n_progetto = checkSkPuaSuPuauvmD(dbc, n_cartella, dataSkVal);
					} catch (ISASPermissionDeniedException e) {
						System.out.println("SocElencocasiEJB.checkAssistito(): MANCANO I DIRITTI su record di tab Puauvm");
						n_progetto = -999;
					}
				}
			}

			h_ret.put("n_cartella", new Integer(n_cartella));
			h_ret.put("n_progetto", new Integer(n_progetto));
			h_ret.put("data_skval", dataSkVal); // gb 05/03/07
			h_ret.put("motivo_skval", motivoSkVal); // gb 05/03/07
			h_ret.put("pr_data_carico", dataSkValCarico); // 11/10/07
			h_ret.put("abil", new Boolean(abil));

			dbc.close();
			super.close(dbc);
			done = true;

			return h_ret;
		} catch (Exception e) {
			System.out.println("SocElencocasiEJB.checkProgetto(): " + e);
			throw new SQLException("Errore eseguendo una checkProgetto()");
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
	}// END checkProgetto

	// ============================================================================= //
	// ======================= ELENCO CASI ========================================= //
	// ============================================================================= //

	/**** 02/10/08
		// gestione SENZA agenda
		public Vector query(myLogin mylogin, Hashtable h) throws SQLException
	    {
		    boolean done=false;
		    ISASConnection dbc=null;
		    ISASCursor dbcur = null;
		
		    try{
		      dbc=super.logIn(mylogin);
		      
				String myselect = faiSelectQueryNoAge(dbc, h);
		      System.out.println("ElencocasiEJB/query: " + myselect);
		
		      dbcur = dbc.startCursor(myselect);
		      Vector vdbr=dbcur.getAllRecord();
		      dbcur.close();
		
		      // Decodifica comune_res, comune_nasc, settore e motivo
		      decodificaQueryInfo(dbc, vdbr, (String)h.get("tpOper"));
		
		      dbc.close();
		      super.close(dbc);
		      done=true;
		      return vdbr;
	      }
	    catch(Exception e)
	      {
	      e.printStackTrace();
	      throw new SQLException("ElencocasiEJB: Errore eseguendo la query()  ");
	      }
	    finally
	      {
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


		// 06/12/07: QUERY per chi non utilizza AGENDANT_PUA
		private String faiSelectQuery(ISASConnection mydbc, Hashtable h0) throws Exception
		{
		    String strCodSubzona = (String)h0.get("subzona");
		    String strCodSettore = (String)h0.get("settore");
		    String strTpOper = (String)h0.get("tpOper");// 06/12/07

		    System.out.println("ElencocasiEJB/queryPaginat: strCodSubzona=" + strCodSubzona +
		                        " / strCodSettore=" + strCodSettore);


			String myselect = "SELECT DISTINCT a.*" +
		                        " FROM ass_anagrafica a, comuni c" +
		                        " WHERE a.presa_carico = 'N'" +
								" AND ((a.sospesa_flag = 'N')" + // 16/05/08
									" OR (a.sospesa_flag IS NULL))"; // 06/06/08: x compatibilita rec precedenti
		
		    if ((strCodSubzona != null) && !(strCodSubzona.equals("")) && !(strCodSubzona.equals("TUTTI")))
		        myselect += " AND a.comune_res = c.codice" +
	                        " AND c.cod_subzona = '" + strCodSubzona + "'";

	      	if ((strCodSettore != null) && !(strCodSettore.equals("")) && !(strCodSettore.equals("TUTTI")))
		        myselect += " AND a.settore = '" + strCodSettore + "'";
		
			// 06/12/07
			if ((strTpOper != null) && (!strTpOper.trim().equals(""))) 
				myselect += " AND a.esito_contatto = '" + (strTpOper.equals(this.TP_OPER_AS)?this.APP_AS:this.APP_IP) + "'";
		
		    myselect += " ORDER BY a.data_reg, a.comune_res, a.settore";

			return myselect;
		}
	****/

	/***** 02/10/08
		// 11/12/07: gestione CON agenda
		public Vector query_eleCasiAge(myLogin mylogin, Hashtable h) throws SQLException
	    {
		    boolean done=false;
		    ISASConnection dbc=null;
		    ISASCursor dbcur = null;
		
		    try{
		      	dbc=super.logIn(mylogin);
		      
				String myselect = faiSelectQueryAge(dbc, h);
		      	System.out.println("ElencocasiEJB/query_eleCasiAge: " + myselect);
		
			      dbcur = dbc.startCursor(myselect);
			      Vector vdbr=dbcur.getAllRecord();
		    	  dbcur.close();
		
			      // Decodifica comune_res, comune_nasc, settore e motivo
			      decodificaQueryInfo(dbc, vdbr, (String)h.get("tpOper"));
		
		    	  dbc.close();
			      super.close(dbc);
			      done=true;
		    	  return vdbr;
	      	} catch(Exception e) {
		      e.printStackTrace();
	    	  throw new SQLException("ElencocasiEJB: Errore eseguendo la query_eleCasiAge()");
	      	} finally {
		      	if(!done) {
			  		try{
		          		if (dbcur != null)
		            		dbcur.close();
		          		dbc.close();
		          		super.close(dbc);
	          		}catch(Exception e1) {
						System.out.println(e1);
					}
	        	}
	    	}
	 	}

		// 11/12/07
		private String faiSelectQueryAge(ISASConnection mydbc, Hashtable h0) throws Exception
		{
		    String codOper = (String)h0.get("ag_cod_oper");
		    String dtIni = (String)h0.get("dt1");
		    String dtFin = (String)h0.get("dt2");

			String myselect = "SELECT DISTINCT a.*," +
									" b.ag_data_app," +
									" b.ag_ora_app," +
									" b.ag_esito," +
									" b.ag_urgente," +
									" b.ag_tipo_bisogno" +
		                        " FROM ass_anagrafica a," +
									" agendant_pua b" +
		                        " WHERE a.progressivo = b.ag_num_scheda" +
								" AND a.presa_carico = 'N'" +
								" AND ((a.sospesa_flag = 'N')" + // 16/05/08
									" OR (a.sospesa_flag IS NULL))" + // 06/06/08: x compatibilita rec precedenti
								" AND b.ag_cod_oper = '" + codOper + "'" +
								" AND b.ag_data_app >= " + formatDate(mydbc, dtIni) +
								" AND b.ag_data_app <= " + formatDate(mydbc, dtFin) +
								" AND b.ag_cod_tipo_appu = 'COL'" + // 03/03/08
								" ORDER BY b.ag_data_app, b.ag_ora_app";

			return myselect;
		}
	*****/

	// gestione SENZA agenda
	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		return query_eleCasiMix(mylogin, h, false, false);
	}

	// 11/12/07: gestione CON agenda
	public Vector query_eleCasiAge(myLogin mylogin, Hashtable h) throws SQLException {
		return query_eleCasiMix(mylogin, h, true, true);
	}

	// 03/10/08: gestione CON agenda solo per caso SEMPLICE
	public Vector query_eleCasiAge_S(myLogin mylogin, Hashtable h) throws SQLException {
		return query_eleCasiMix(mylogin, h, true, false);
	}

	// 03/10/08: gestione CON agenda solo per caso COMPLESSO
	public Vector query_eleCasiAge_C(myLogin mylogin, Hashtable h) throws SQLException {
		return query_eleCasiMix(mylogin, h, false, true);
	}

	// 03/10/08: gestione casi MISTI
	private Vector query_eleCasiMix(myLogin mylogin, Hashtable h, boolean isAgeSimple, boolean isAgeComplx) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		try {
			dbc = super.logIn(mylogin);

			// boolean abilitaGestioneUvmd = controllaAbilitazioneUvmD(dbc, codiceOperatore);
			String myselect = faiSelectQueryCasiMix(dbc, h, isAgeSimple, isAgeComplx);

			System.out.println("ElencocasiEJB/query_eleCasiMix: " + myselect);

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			boolean abilitaGestioneUvmd = false;

			// Decodifica comune_res, comune_nasc, settore e motivo
			decodificaQueryInfo(dbc, vdbr, (String) h.get("tpOper"), abilitaGestioneUvmd);

			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("ElencocasiEJB: Errore eseguendo la query_eleCasiMix()");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// versione con PAGINATE ---------------
	// 30/04/09: gestione SENZA agenda
	public Vector queryPag(myLogin mylogin, Hashtable h) throws SQLException {
		return queryPag_eleCasiMix(mylogin, h, false, false);
	}

	// 30/04/09: gestione CON agenda
	public Vector queryPag_eleCasiAge(myLogin mylogin, Hashtable h) throws SQLException {
		return queryPag_eleCasiMix(mylogin, h, true, true);
	}

	// 30/04/09: gestione CON agenda solo per caso SEMPLICE
	public Vector queryPag_eleCasiAge_S(myLogin mylogin, Hashtable h) throws SQLException {
		return queryPag_eleCasiMix(mylogin, h, true, false);
	}

	// 30/04/09: gestione CON agenda solo per caso COMPLESSO
	public Vector queryPag_eleCasiAge_C(myLogin mylogin, Hashtable h) throws SQLException {
		return queryPag_eleCasiMix(mylogin, h, false, true);
	}

	// 30/04/09: gestione casi MISTI
	private Vector queryPag_eleCasiMix(myLogin mylogin, Hashtable h, boolean isAgeSimple, boolean isAgeComplx) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String myselect = faiSelectQueryCasiMix(dbc, h, isAgeSimple, isAgeComplx);

			System.out.println("ElencocasiEJB/queryPag_eleCasiMix: " + myselect);

			// 11/05/10 dbcur = dbc.startCursor(myselect,200);
			// 11/05/10: richiesto da G.Carniani
			dbcur = dbc.startCursor(myselect);
			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			dbcur.close();

			boolean abilitaGestioneUvmd = false;// in questo caso non serve il controllo se abilitato uvmd
			// Decodifica comune_res, comune_nasc, settore e motivo
			decodificaQueryInfo(dbc, vdbr, (String) h.get("tpOper"), true, abilitaGestioneUvmd);

			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("ElencocasiEJB: Errore eseguendo la queryPag_eleCasiMix()");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// versione con PAGINATE ---------------

	// 03/10/08: QUERY per chi:
	// - NON utilizza AGENDA
	// - utilizza AGENDA
	// - utilizza AGENDA per i casi semplici(complessi) e NON per i casi complessi(semplici)
	// 02/10/08: aggiunto lettura anche dei casi complx (con eventuale vistoPuac=registrazione di keyPUAUVM e presa carico).
	// 13/11/08: aggiunto criteri su zona, distr e assegnamento ad operatore.
	// 11/03/09: aggiunto in lista campi estratti: indirizzo dom e res.
	// 16/04/10: aggiunto cntrl su esistenza campi ISAS su ASS_ANAGRAFICA
	private String faiSelectQueryCasiMix(ISASConnection mydbc, Hashtable h0, boolean isAgeSimple, boolean isAgeComplx) throws Exception {
		String strCodSubzona = (String) h0.get("subzona");
		String strCodSettore = (String) h0.get("settore");
		String codOper = (String) h0.get("ag_cod_oper");
		String dtIni = (String) h0.get("dt1");
		String dtFin = (String) h0.get("dt2");
		String strTpOper = (String) h0.get("tpOper");
		String vistoPuac = (String) h0.get("visto_puac");
		// 13/11/08 ---
		String codZona = (String) h0.get("zona");
		String codDistr = (String) h0.get("distr");
		String assegnati = (String) h0.get("assegnati");
		// 13/11/08 ---

		/*
			    System.out.println("ElencocasiEJB/faiSelectQueryCasiMix: strTpOper=["+ strTpOper +
									"] - strCodSubzona=[" + strCodSubzona + "] - strCodSettore=[" + strCodSettore +
									"] - codZona=[" + codZona + "] - codDistr=[" + codDistr + "] - assegnati=[" + assegnati + 
									"] - codOper=[" + codOper + "] - dtIni=[" + dtIni + "] - dtFin=[" + dtFin + 
									"] - isAgeSimple=[" + isAgeSimple + "] - isAgeComplx=[" + isAgeComplx + "]");
		*/

		// 16/04/10 ---
		ISASMetaInfo imt_assAna = new ISASMetaInfo(mydbc, "ass_anagrafica");
		boolean tabConISAS = imt_assAna.containsISASFields();
		// 16/04/10 ---

		String prefix = (strTpOper.equals(this.TP_OPER_AS) ? "soc" : "san");
		boolean conVistoPuac = ("SI".equals(vistoPuac));
		boolean conCodSubZona = ((strCodSubzona != null) && !(strCodSubzona.equals("")) && !(strCodSubzona.equals("TUTTI")));

		// 21/03/13: ripresa da metodo "faiSelectQueryPUAC()"
		String queryOrdinamento = creaQueryOrdinamento(h0);
		
		String sel = "SELECT" +
		// 16/04/10 " a.jisas_uid, a.jisas_gid, a.jisas_mask,"
				(tabConISAS ? " a.jisas_uid, a.jisas_gid, a.jisas_mask," : "") + // 1, 2, 3
				" a.urgente, a.soc_data, a.san_data," + // 4, 5, 6
				" a.progressivo, a.data_reg, a.cognome, a.nome," + // 7, 8, 9, 10
				" a.sesso, a.comune_nascita, a.data_nascita, a.comune_res, a.comune_dom," + // 11, 12, 13, 14, 15
				" a.settore, a.motivo, a.area_interv, a.esito_contatto," + // 16, 17, 18, 19
				" a.soc_cod, a.soc_carico," + // 20, 21
				" a.san_cod, a.san_carico," + // 22, 23
				" a.indirizzo_res, a.indirizzo_dom" + // 24, 25
				(ISASUtil.valida(queryOrdinamento)? ", " + queryOrdinamento :"")+ // 26, 27, 28
				" FROM ass_anagrafica a";

		String selSimple = new String(sel);
		String selComplx = new String(sel);

		// casi semplici
		if (isAgeSimple) // CON agenda
			selSimple += getCrit(false, conVistoPuac, strTpOper, prefix) + getCritAge(mydbc, prefix, codOper, dtIni, dtFin);
		else { // SENZA agenda
			selSimple += getTabNoAge(strTpOper, conCodSubZona) + getCrit(false, conVistoPuac, strTpOper, prefix);
			if (strTpOper.equals(this.TP_OPER_AS))
				selSimple += getCritNoAgeAS(conCodSubZona, strCodSubzona, strCodSettore);
			else if (strTpOper.equals(this.TP_OPER_IP))
				selSimple += getCritNoAgeIP(codZona, codDistr);
			selSimple += getCritNoAgeAttrib(prefix, codOper, assegnati);
		}

		// casi complessi
		if (isAgeComplx) // CON agenda
			selComplx += getCrit(true, conVistoPuac, strTpOper, prefix) + getCritAge(mydbc, prefix, codOper, dtIni, dtFin);
		else { // SENZA agenda
			selComplx += getTabNoAge(strTpOper, conCodSubZona) + getCrit(true, conVistoPuac, strTpOper, prefix);
			if (strTpOper.equals(this.TP_OPER_AS))
				selComplx += getCritNoAgeAS(conCodSubZona, strCodSubzona, strCodSettore);
			else if (strTpOper.equals(this.TP_OPER_IP))
				selComplx += getCritNoAgeIP(codZona, codDistr);
			selComplx += getCritNoAgeAttrib(prefix, codOper, assegnati);
		}

		String myselect = selSimple + " UNION " + selComplx;

		/** 16/04/10	
				// 30/04/09 ---
				if ((h0.get("order") != null) && (!((String)h0.get("order")).trim().equals("")))
					myselect += " ORDER BY " + getNumCampi(h0,); // N.B.: modificare metodo se variano colonne
				else {  // 30/04/09 ---
					myselect += " ORDER BY 4 DESC";
					if ((isAgeSimple) && (isAgeComplx)) // solo CON agenda
						myselect += ", "+(strTpOper.equals(this.TP_OPER_AS)?"5":"6");
					else if ((!isAgeSimple) && (!isAgeComplx)) // solo SENZA agenda
						myselect += ", 8, 14, 15, 16";
				}
		**/
		// 16/04/10
		if ((h0.get("order") != null) && (!((String) h0.get("order")).trim().equals("")))
			myselect += " ORDER BY " + getNumCampi(h0, tabConISAS); // N.B.: modificare metodo se variano colonne
		else {
			myselect += " ORDER BY " + (tabConISAS ? "4" : "1") + " DESC";
			if ((isAgeSimple) && (isAgeComplx)) // solo CON agenda
				myselect += ", " + (strTpOper.equals(this.TP_OPER_AS) ? (tabConISAS ? "5" : "2") : (tabConISAS ? "6" : "3"));
			else if ((!isAgeSimple) && (!isAgeComplx)) // solo SENZA agenda
				myselect += ", " + (tabConISAS ? "8" : "5") + ", " + (tabConISAS ? "14" : "11") + ", " + (tabConISAS ? "15" : "12") + ", "
						+ (tabConISAS ? "16" : "13");
		}

		return myselect;
	}

	// 13/11/08: tabelle da aggiungere nel caso SENZA agenda
	private String getTabNoAge(String tpOper, boolean conCodSubZona) throws SQLException {
		String selTab = "";

		if ((tpOper.equals(this.TP_OPER_AS)) && conCodSubZona)
			selTab = ", comuni c";
		else if (tpOper.equals(this.TP_OPER_IP))
			selTab = ", comuni c, distretti d, zone z";

		return selTab;
	}

	// 13/11/08: criteri su esito segnalazione
	private String getCrit(boolean isCasoCpmlx, boolean conVistoPuac, String tpOper, String prefix) throws SQLException {
		String selCrit = " WHERE a." + prefix + "_carico = 'N'" + " AND ((a.sospesa_flag <> 'S')" + " OR (a.sospesa_flag IS NULL))";

		// filtro esito
		if (!isCasoCpmlx) // caso semplice
			selCrit += " AND a.esito_contatto = '" + (tpOper.equals(this.TP_OPER_AS) ? this.APP_AS : this.APP_IP) + "'";
		else { // caso complesso
			selCrit += " AND ";
			if (conVistoPuac)
				selCrit += "(";
			// 24/11/08 selCrit += "a.esito_contatto = '" + this.APP_PUAC + "'";
			selCrit += "a.esito_contatto IN ('" + this.APP_PUAC + "', '" + this.APP_PUAC_1 + "')";
			if (conVistoPuac)
				selCrit += " AND a.pr_progr > 0 AND a.presa_carico = 'S')";
		}

		return selCrit;
	}

	// 13/11/08: criteri da aggiungere nel caso CON agenda
	private String getCritAge(ISASConnection mydbc, String prefix, String codOper, String dtIni, String dtFin) throws SQLException {
		// filtro cod operatore e date
		return (" AND a." + prefix + "_cod = '" + codOper + "'" + " AND a." + prefix + "_data >= " + formatDate(mydbc, dtIni) + " AND a."
				+ prefix + "_data <= " + formatDate(mydbc, dtFin));
	}

	// 13/11/08: criteri da aggiungere nel caso SENZA agenda per ass soc
	private String getCritNoAgeAS(boolean conCodSubZona, String strCodSubzona, String strCodSettore) throws SQLException {
		String selCrit = "";
		boolean conCodSettore = ((strCodSettore != null) && !(strCodSettore.equals("")) && !(strCodSettore.equals("TUTTI")));

		if (conCodSubZona) // filtro cod subzona
			selCrit += " AND a.comune_res = c.codice" + " AND c.cod_subzona = '" + strCodSubzona + "'";

		if (conCodSettore) // filtro cod settore
			selCrit += " AND a.settore = '" + strCodSettore + "'";

		return selCrit;
	}

	// 13/11/08: criteri da aggiungere nel caso SENZA agenda per inf
	private String getCritNoAgeIP(String codZona, String codDistr) throws SQLException {
		String selCrit = "";
		boolean conCodZona = ((codZona != null) && !(codZona.equals("")) && !(codZona.equals("TUTTO")));
		boolean conCodDistr = ((codDistr != null) && !(codDistr.equals("")) && !(codDistr.equals("TUTTO")));

		// filtro per comune dom e, se non esiste, per comune res
		selCrit += " AND ((a.comune_dom IS NOT NULL AND a.comune_dom = c.codice)"
				+ " OR (a.comune_dom IS NULL AND a.comune_res IS NOT NULL AND a.comune_res = c.codice))"
				+ " AND c.cod_distretto = d.cod_distr" + " AND d.cod_zona = z.codice_zona";

		if (conCodZona) // filtro cod zona
			selCrit += " AND z.codice_zona = '" + codZona + "'";

		if (conCodDistr) // filtro cod distretto
			selCrit += " AND d.cod_distr = '" + codDistr + "'";

		return selCrit;
	}

	// 13/11/08: criteri da aggiungere nel caso SENZA agenda con attribuzione dei casi
	private String getCritNoAgeAttrib(String prefix, String codOper, String assegnati) throws SQLException {
		String selCrit = "";

		// filtro casi attribuiti o non attribuiti
		if (assegnati != null) {
			if (assegnati.trim().equals("S"))
				selCrit += " AND a." + prefix + "_cod = '" + codOper + "'";
			else if (assegnati.trim().equals("N"))
				selCrit += " AND a." + prefix + "_cod IS NULL";
			else if (assegnati.trim().equals("S_ALL")) // 19/11/08: x attribuzione da parte di superutente
				selCrit += " AND a." + prefix + "_cod IS NOT NULL";
		}

		return selCrit;
	}

	// 30/04/09
	// 16/04/10 private String getNumCampi(Hashtable h0) throws SQLException
	private String getNumCampi(Hashtable h0, boolean tabConISAS) throws SQLException {
		String ordineNum = "";
		
		// 21/03/13 
		StringTokenizer strTkOrder = new StringTokenizer((String) h0.get("order"), ",");
		while (strTkOrder.hasMoreTokens()) {
			String ordineStr = (String)strTkOrder.nextToken();

			if (ordineStr.indexOf("progressivo") != -1)
				ordineNum += (tabConISAS ? "7" : "4");
			if (ordineStr.indexOf("data_reg") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "8" : "5");
			}
			if (ordineStr.indexOf("cognome") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "9" : "6");
			}
			if (ordineStr.indexOf("nome") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "10" : "7");
			}
			if (ordineStr.indexOf("sesso") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "11" : "8");
			}
			if (ordineStr.indexOf("data_nascita") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "13" : "10");
			}
			if (ordineStr.indexOf("indirizzo_res") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "24" : "21");
			}
			if (ordineStr.indexOf("indirizzo_dom") != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				ordineNum += (tabConISAS ? "25" : "22");
			}
			
			// 21/03/13 ---
			String posCom_1 = "26";
			String posCom_2 = "27";
			String posCom_3 = "28";
			if (ordineStr.indexOf(CONSTANTS_ORDER_COMMUNE_NASC) != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				String pos = ((ordineNum.indexOf(posCom_1) < 0)?posCom_1:((ordineNum.indexOf(posCom_2) < 0)?posCom_2:posCom_3));
				ordineNum += (tabConISAS ? pos : "" + (Integer.parseInt(pos) - 3));
			}
			if (ordineStr.indexOf(CONSTANTS_ORDER_COMMUNE_RES) != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				String pos = ((ordineNum.indexOf(posCom_1) < 0)?posCom_1:((ordineNum.indexOf(posCom_2) < 0)?posCom_2:posCom_3));
				ordineNum += (tabConISAS ? pos : "" + (Integer.parseInt(pos) - 3));
			}
			if (ordineStr.indexOf(CONSTANTS_ORDER_COMMUNE_DOM) != -1) {
				if (!ordineNum.trim().equals(""))
					ordineNum += ", ";
				String pos = ((ordineNum.indexOf(posCom_1) < 0)?posCom_1:((ordineNum.indexOf(posCom_2) < 0)?posCom_2:posCom_3));
				ordineNum += (tabConISAS ? pos : "" + (Integer.parseInt(pos) - 3));
			}
			// 21/03/13 ---
		}
		
		if (ordineNum.trim().equals(""))
			ordineNum = (tabConISAS ? "4" : "1") + " DESC";
		return ordineNum;
	}

	/**** 13/11/08
		private String faiSelectQueryAssAna(ISASConnection mydbc, String strTpOper, boolean isCasoCpmlx,
								String strCodSubzona, String strCodSettore, 
								String codOper, String dtIni, String dtFin, String vistoPuac) throws Exception
		{
			String prefix = (strTpOper.equals(this.TP_OPER_AS)?"soc":"san");

			boolean conVistoPuac = ("SI".equals(vistoPuac));

			// caso senza agenda
			boolean conCodSubZona = ((strCodSubzona != null) && !(strCodSubzona.equals("")) && !(strCodSubzona.equals("TUTTI")));
			boolean conCodSettore = ((strCodSettore != null) && !(strCodSettore.equals("")) && !(strCodSettore.equals("TUTTI")));
			// caso con agenda
			boolean conCodOper = ((codOper != null) && (!codOper.trim().equals("")));

			String myselect = "SELECT a.jisas_uid, a.jisas_gid, a.jisas_mask," + // 1,2,3
								" a.urgente, a.soc_data, a.san_data," + // 4,5,6
								" a.progressivo, a.data_reg, a.cognome, a.nome," + // 7,8,9,10
								" a.sesso, a.comune_nascita, a.data_nascita, a.comune_res, a.comune_dom," + // 11,12,13,14,15
								" a.settore, a.motivo, a.area_interv, a.esito_contatto" + // 16,17,18,19
		                        " FROM ass_anagrafica a" +
								(conCodSubZona?", comuni c":"") +
		                        " WHERE a." + prefix + "_carico = 'N'" + 
								" AND ((a.sospesa_flag <> 'S')" +
									" OR (a.sospesa_flag IS NULL))";
		
		    if (conCodSubZona) // filtro cod subzona
		        myselect += " AND a.comune_res = c.codice" +
	                        " AND c.cod_subzona = '" + strCodSubzona + "'";

	      	if (conCodSettore) // filtro cod settore
		        myselect += " AND a.settore = '" + strCodSettore + "'";

			// filtro esito
			if (!isCasoCpmlx)
				myselect += " AND a.esito_contatto = '" + (strTpOper.equals(this.TP_OPER_AS)?this.APP_AS:this.APP_IP) + "'";	
			else {// caso complesso
				myselect += " AND ";
				if (conVistoPuac)
					myselect += "(";
				myselect +=	"a.esito_contatto = '" + this.APP_PUAC + "'";
				if (conVistoPuac)
					myselect += " AND a.pr_progr > 0 AND a.presa_carico = 'S')";
			}

			if (conCodOper) // filtro cod operatore
				myselect += (" AND a." + prefix + "_cod = '" + codOper + "'" +
							" AND a." + prefix + "_data >= " + formatDate(mydbc, dtIni) +
							" AND a." + prefix + "_data <= " + formatDate(mydbc, dtFin));

			return myselect;
		}
	****/

	// 19/11/08: attribuzione di caso ad oper senza agenda da parte di superutente
	public ISASRecord attribuisciCasoOper(myLogin mylogin, Hashtable h0, Vector vettSel) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;// mi serve solo x restituire qualcosa al client
		ISASCursor dbcur = null;
		try {
			dbc = super.logIn(mylogin);

			String codOper = (String) h0.get("oper_attr_new");
			String tpOper = (String) h0.get("tpOper");
			String prefix = (tpOper.equals(this.TP_OPER_AS) ? "soc" : "san");

			String sel = "SELECT * FROM ass_anagrafica" + " WHERE progressivo = ";

			String progr = "";

			dbc.startTransaction();

			if (vettSel != null) {
				for (int k = 0; k < vettSel.size(); k++) {
					Hashtable h_ass = (Hashtable) vettSel.elementAt(k);
					progr = (String) h_ass.get("progressivo");

					dbr = dbc.readRecord(sel + progr);
					dbr.put(prefix + "_cod", codOper);
					dbc.writeRecord(dbr);
					// System.out.println("SocElencoCasiEJB: attribuisciCasoOper - aggiornato su ASS_ANAGRAFICA il progr=["+progr+"] con codOper=["+codOper+"]");
				}
			}

			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				System.out.println("SocElencoCasiEJB.attribuisciCasoOper() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + ce);
			}
			throw ce;
		} catch (DBRecordChangedException e) {
			System.out.println("SocElencoCasiEJB.attribuisciCasoOper(): Eccezione= " + e);
			try {
				System.out.println("SocElencoCasiEJB.attribuisciCasoOper() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SocElencoCasiEJB.attribuisciCasoOper(): Eccezione= " + e);
			try {
				System.out.println("SocElencoCasiEJB.attribuisciCasoOper() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			System.out.println("SocElencoCasiEJB.attribuisciCasoOper(): Eccezione= " + e);
			try {
				System.out.println("SocElencoCasiEJB.attribuisciCasoOper() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una attribuisciCasoOper() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println("SocElencoCasiEJB.attribuisciCasoOper(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}
	}// END attribuisciCasoOper

	// 17/12/07: gestione PUAC
	public Vector query_eleCasiPUAC(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		String codiceOperatore = ISASUtil.getValoreStringa(h, "codice_operatore");

		try {
			dbc = super.logIn(mylogin);
			boolean abilitaGestioneUvmd = controllaAbilitazioneUvmD(dbc, codiceOperatore);
			String myselect = faiSelectQueryPUAC(dbc, h, abilitaGestioneUvmd);
			System.out.println("ElencocasiEJB/query_eleCasiPUAC: " + myselect);

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			// Decodifica comune_res, comune_nasc, settore e motivo
			decodificaQueryInfo(dbc, vdbr, this.TP_OPER_PUAC, abilitaGestioneUvmd);

			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("ElencocasiEJB: Errore eseguendo la query()");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	// 17/12/07
	private String faiSelectQueryPUAC(ISASConnection mydbc, Hashtable h0, boolean abilitaGestioneUvmd) throws Exception {
		String tipo = h0.get("tipo") != null ? (String) h0.get("tipo") : "";
		String dtIni = h0.get("dt1") != null ? (String) h0.get("dt1") : "";
		String dtFin = h0.get("dt2") != null ? (String) h0.get("dt2") : "";
		String codiceOperatore = ISASUtil.getValoreStringa(h0, "zona_operatore");

		
		String queryOrdinamento = creaQueryOrdinamento(h0);
		
		String myselect = "SELECT DISTINCT a.* "
			+(ISASUtil.valida(queryOrdinamento)? ", " + queryOrdinamento :"")
			+ " FROM ass_anagrafica a" + " WHERE a.presa_carico = 'N'"
				+ " AND ((a.sospesa_flag <> 'S')"
				+ // 16/05/08
				" OR (a.sospesa_flag IS NULL))" + " AND a.invio_puac_data >= " + formatDate(mydbc, dtIni) + " AND a.invio_puac_data <= "
				+ formatDate(mydbc, dtFin) ;
				// 24/11/09 " AND a.esito_contatto = '" + this.APP_PUAC + "'";
				// gestione uvmd
//				" AND a.esito_contatto IN ('" + this.APP_PUAC + "', '" + this.APP_PUAC_1 + "' ";
//		if (abilitaGestioneUvmd) {
//			myselect += ", '" + APP_PUAC_UVMD + "' ";
//		}
//		myselect += ")";

		if (ISASUtil.valida(tipo) && tipo.equals("D")){
			myselect += " AND a.esito_contatto IN ('" + APP_PUAC_UVMD + "' )";
		}else {
			myselect += " AND a.esito_contatto IN ('" + this.APP_PUAC + "', '" + this.APP_PUAC_1 + "' ";
			if (abilitaGestioneUvmd) {
				myselect += ", '" + APP_PUAC_UVMD + "' ";
			}
			myselect += ")";
			if ((tipo != null) && (tipo.trim().equals("S")))// caso Semplice divenuto Complesso
				myselect += " AND a.inserita_autom = 'S'";
			else if ((tipo != null) && (tipo.trim().equals("C")))// caso Complesso direttamente da Segr Soc
				myselect += " AND ((a.inserita_autom IS NULL) OR (a.inserita_autom = 'N')" + " OR (a.inserita_autom = 'P'))"; // 14/05/08: schede dovuta al porting
			else if ((tipo != null) && (tipo.trim().equals("R")))// 03/03/08: caso Revisione
				myselect += " AND a.inserita_autom = 'R'";
		}
		
		/* elisa b 14/02/11: se presente il codice operatore, si prendono solo
		 * le segnalazioni appartenenti al distretto dell'operatore */
		if (h0.containsKey("disOpe") && (!h0.get("disOpe").toString().equals(""))) {
			myselect += " AND a.cod_presidio IN( " + " SELECT codpres FROM presidi" + " WHERE codreg = '"
					+ leggiConf(mydbc, "codice_regione") + "'" + " AND codazsan = '" + leggiConf(mydbc, "codice_usl") + "'"
					+ " AND coddistr = '" + h0.get("disOpe").toString() + "')";
		}
		myselect += " AND (NOT EXISTS (SELECT 1 FROM CARTELLA X WHERE X.N_CARTELLA = A.N_CARTELLA AND DATA_CHIUSURA IS NOT NULL ) ) ";
		// 30/04/09 ---
		if ((h0.get("order") != null) && (!((String) h0.get("order")).trim().equals("")))
			myselect += " ORDER BY " + h0.get("order");
		else
			// 30/04/09 ---
			myselect += " ORDER BY a.urgente DESC, a.invio_puac_data, a.cognome";

		return myselect;
	}

	private String creaQueryOrdinamento(Hashtable h0) {
		String punto = MIONOME + "creaQueryOrdinamento ";
		String query = "";
		String valOrdinamento = ISASUtil.getValoreStringa(h0, "order");
		
		if (ISASUtil.valida(valOrdinamento)){
			if (valOrdinamento.indexOf(CONSTANTS_ORDER_COMMUNE_NASC)>=0){
				query=" DECODE (a.comune_nascita, NULL, '', (select x.descrizione from comuni x " +
						" where x.codice = a.comune_nascita) ) as "+ CONSTANTS_ORDER_COMMUNE_NASC; 
			}
			if (valOrdinamento.indexOf(CONSTANTS_ORDER_COMMUNE_RES)>=0){
				query += (ISASUtil.valida(query)? ", ":"" );
				query +=" DECODE (a.comune_res, NULL, '', (select x.descrizione from comuni x " +
						" where x.codice = a.comune_res) ) as "+CONSTANTS_ORDER_COMMUNE_RES; 
			}
			if (valOrdinamento.indexOf(CONSTANTS_ORDER_COMMUNE_DOM)>=0){
				query += (ISASUtil.valida(query)? ", ":"" );
				query +=" DECODE (a.comune_dom, NULL, '', (select x.descrizione from comuni x " +
						" where x.codice = a.comune_dom) ) as " +CONSTANTS_ORDER_COMMUNE_DOM ; 
			}
		}

		stampa(punto + " Query>"+query );
		return query;
	}

	private boolean controllaAbilitazioneUvmD(ISASConnection dbc, String codiceOperatore) {
		String punto = MIONOME + "controllaAbilitazioneUvmD ";
		boolean gestioneUvmd = false;
		stampa(punto + " Gestione uvmd controllo abilitazione>" + gestioneUvmd + "<CONSTANTS_ABILITAZIONE_UVM>"+CONSTANTS_ABILITAZIONE_UVM+"<");
		try {
			Hashtable conf = eveUtl.leggiConf(dbc, codiceOperatore, new String[] { CONSTANTS_ABILITAZIONE_UVM });
			stampa(punto + "dati recuperati>" + (conf != null ? conf + "" : "no dati" + ""));
			String valoreletto = ISASUtil.getValoreStringa(conf, CONSTANTS_ABILITAZIONE_UVM);
			gestioneUvmd = (ISASUtil.valida(valoreletto) && valoreletto.equalsIgnoreCase("SI"));
			stampa(punto + "gestioneUvmd>"+valoreletto+"< valore attiv>"+gestioneUvmd+"<");
		} catch (Exception e) {
			stampa(punto + "\t Errore nel recuperare abilitazione per il codice operatore>" + codiceOperatore + "<");
			e.printStackTrace();
		}

		return gestioneUvmd;
	}

	// 30/04/09
	public Vector queryPag_eleCasiPUAC(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = MIONOME + "queryPag_eleCasiPUAC ";
		stampa(punto + " Inizio con dati>" + h + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);
			String codiceOperatore = ISASUtil.getValoreStringa(h, "codice_operatore");
			boolean abilitaGestioneUvmd = controllaAbilitazioneUvmD(dbc, codiceOperatore);
			String myselect = faiSelectQueryPUAC(dbc, h, abilitaGestioneUvmd);
			stampa(punto + " Query>" + myselect + "<");

			// 12/06/09 m. dbcur = dbc.startCursor(myselect,200);
			// 12/06/09 m.: richiesta di B.Giachi
			dbcur = dbc.startCursor(myselect);
			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			dbcur.close();
			stampa(punto + " procedo con la decodifica");
			// Decodifica comune_res, comune_nasc, settore e motivo
			decodificaQueryInfo(dbc, vdbr, this.TP_OPER_PUAC, true, abilitaGestioneUvmd);

			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryPag_eleCasiPUAC()");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

//	// gb 06/03/07
//	private String decodificaTabVoci(ISASConnection mydbc, String val, String codice) throws Exception {
//		String strDescrizione = "";
//
//		if ((val != null) && (!val.trim().equals(""))) {
//			String selS = "SELECT tab_descrizione" + " FROM tab_voci" + " WHERE tab_cod = '" + codice + "'" + " AND tab_val = '" + val
//					+ "'";
//
//			ISASRecord rec = mydbc.readRecord(selS);
//
//			if (rec != null)
//				strDescrizione = (String) rec.get("tab_descrizione");
//
//			if (strDescrizione == null)
//				strDescrizione = "";
//		}
//		return strDescrizione;
//	}

	// 11/01/08
	private String leggiConf(ISASConnection mydbc, String cod) throws Exception {
		String desc = "";

		String selConf = "SELECT conf_txt" + " FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND conf_key = '" + cod + "'";

		ISASRecord dbrDec = mydbc.readRecord(selConf);
		if (dbrDec != null)
			if (dbrDec.get("conf_txt") != null)
				desc = (String) dbrDec.get("conf_txt");

		return desc;
	}// END leggiConf

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

	// 19/04/13
	private ISASRecord leggiOperatore(ISASConnection dbc, String codOp) throws Exception
	{
		String sel = "SELECT * FROM operatori WHERE codice = '" + codOp.trim() + "'";
		return (ISASRecord)dbc.readRecord(sel);
	}
	
	
	
}

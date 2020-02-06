package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 31/10/06: EJB Scheda Medici ONCOLOGI
//
//	03/11/06 m.: aggiunto lettura dateMAX delle 6 scale presenti nel contatto.
//	22/11/06 m.: sostituito campi di SKPATOLOGIE con quelli della nuova tabella DIAGNOSI.
//	03/01/07 m.: corretto metodo "deleteAll()" aggiungendo cancellazione di SMEDPAL_REFERENTE.
// ==========================================================================


import java.util.*;
import java.sql.*;

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.casi_adrsa.GestSegnalazione;
import it.pisa.caribel.sinssnt.casi_adrsa.ScaleVal; // 15/07/09
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
//import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.exception.*; // 17/09/07
//import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure; //gb 01/10/07
//import it.caribel.app.sinssnt.bean.classiPerSkMedPalEJB.*;

public class SkMedPalEJB extends SINSSNTConnectionEJB {
	private static String nomeEJB = "1-SkMedPalEJB ";
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	ServerUtility su =new ServerUtility();

	// 17/09/07
	private String msgNoD = "Mancano i diritti per leggere il record";
	
	public SkMedPalEJB() {
	}

	private String[] arrTabTest = { "sc_braden", "sc_pfeiffer", "sc_kps", "sc_pap", "sc_tiq", "sc_nrs" };
	private String[] arrDtTest = { "skb_data", "data", "data", "data", "data", "data" };

	// 05/09 Elisa Croci
	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();
	// 15/07/09
	private ScaleVal gest_scaleVal = new ScaleVal();
	
	// 05/02/13
	private EveUtils eveUtl = new EveUtils();
	
	// 15/12/14 mv
	private final String KEYCONF_CHIUSURA_TUTTI_CONT = "ABL_SKMPAL_CHIUS";
	private final String TABVOCI_VAL_CHIUS_ALTRO = "9";	

	private boolean mydebug = true;
	private String ver = "3-"+ this.getClass().getName();

	@SuppressWarnings("rawtypes")
	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException, CariException {
		// 17/09/07
		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		String data_apertura = null;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;// 22/11/06 m.

		try {
			n_cartella = (String) h.get("n_cartella");
			n_contatto = (String) h.get("n_contatto");
			data_apertura = (String) h.get("skm_data_apertura");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkMedPal queryKey: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT m.* FROM skmedpal m WHERE" + " m.n_cartella=" + n_cartella + " AND m.n_contatto=" + n_contatto;// +
			// 13-05-05 Commento voluto da Andrea
			// " AND m.skm_data_apertura="+formatDate(dbc,data_apertura);

			printError("select query_key su skmedpal===" + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);

			String w_codice = "";
			String w_select = "";
			ISASRecord w_dbr = null;
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				System.out.println(nomeEJB + " - queryKey() - " + h1.toString());

				if (h1.get("cod_operatore") != null && !((String) h1.get("cod_operatore")).equals("")) {
					dbr.put("desc_operat", decodifica("operatori", "codice", h1.get("cod_operatore"),
							"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')", dbc));
				} else
					dbr.put("desc_operat", "");

				if (h1.get("skm_osp_dim") != null && !((String) h1.get("skm_osp_dim")).equals("")) {
					dbr.put("des_osp", decodifica("ospedali", "codosp", h1.get("skm_osp_dim"), "descosp", dbc));
				} else
					dbr.put("des_osp", "");

				if (h1.get("skm_uo_dim") != null && !((String) h1.get("skm_uo_dim")).equals("")) {
					dbr.put("des_rep", decodifica("reparti", "cd_rep", h1.get("skm_uo_dim"), "reparto", dbc));
				} else
					dbr.put("des_rep", "");

				if (h1.get("skm_medico") != null && !((String) h1.get("skm_medico")).equals("")) {
					w_codice = ((String) h1.get("skm_medico")).trim();
					w_select = "SELECT * FROM operatori WHERE codice='" + w_codice + "'";
					//w_select = "SELECT * FROM medici WHERE mecodi='" + w_codice + "'";
					w_dbr = dbc.readRecord(w_select);
					if(w_dbr!=null){
						dbr.put("desc_inf", w_dbr.get("cognome") + " " + w_dbr.get("nome"));
					}
				} else
					dbr.put("desc_inf", "");

				// Elisa 18/11/09 - medico di famiglia
				if (h1.get("skm_mmg") != null && !((String) h1.get("skm_mmg")).equals("")) {
					// se e' stato scelto, lo decodifico
					String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + dbr.get("skm_mmg"), "mecogn") + " "
							+ ISASUtil.getDecode(dbc, "medici", "mecodi", "" + dbr.get("skm_mmg"), "menome");
					System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
					dbr.put("mecogn", desc_mmg);
				} else {
					// propongo il medico indicato su anagra_c
					String codmed = ISASUtil.getDecode(dbc, "anagra_c", "n_cartella", n_cartella, "cod_med");
					System.out.println(nomeEJB + " codmed == " + codmed);
					if (codmed != null && !codmed.equals("")) {
						String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "mecogn") + " "
								+ ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "menome");
						System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
						dbr.put("skm_mmg", codmed);
						dbr.put("mecogn", desc_mmg);
					}
				}

				// ------------------------------------------------------------------------------

				/***
				 * 22/11/06 m. String
				 * pato="SELECT pat.* FROM skpatologie pat WHERE"+
				 * " pat.n_cartella="+n_cartella+
				 * " AND pat.n_contatto="+n_contatto; ISASRecord
				 * dbrpato=dbc.readRecord(pato); if (dbrpato!=null){ if
				 * (dbrpato.get("skpat_patol1")!=null &&
				 * !((String)dbrpato.get("skpat_patol1")).equals("")){
				 * dbr.put("skpat_patol1",dbrpato.get("skpat_patol1"));
				 * dbr.put("des1",
				 * decodifica("icd9","cd_diag",dbrpato.get("skpat_patol1"),
				 * "diagnosi",dbc)); }else dbr.put("des1",""); if
				 * (dbrpato.get("skpat_patol2")!=null &&
				 * !((String)dbrpato.get("skpat_patol2")).equals("")){
				 * dbr.put("skpat_patol2",dbrpato.get("skpat_patol2"));
				 * dbr.put("des2",
				 * decodifica("icd9","cd_diag",dbrpato.get("skpat_patol2"),
				 * "diagnosi",dbc)); }else dbr.put("des2",""); if
				 * (dbrpato.get("skpat_patol3")!=null &&
				 * !((String)dbrpato.get("skpat_patol3")).equals("")){
				 * dbr.put("skpat_patol3",dbrpato.get("skpat_patol3"));
				 * dbr.put("des3",
				 * decodifica("icd9","cd_diag",dbrpato.get("skpat_patol3"),
				 * "diagnosi",dbc)); }else dbr.put("des3",""); if
				 * (dbrpato.get("skpat_patol4")!=null &&
				 * !((String)dbrpato.get("skpat_patol4")).equals("")){
				 * dbr.put("skpat_patol4",dbrpato.get("skpat_patol4"));
				 * dbr.put("des4",
				 * decodifica("icd9","cd_diag",dbrpato.get("skpat_patol4"),
				 * "diagnosi",dbc)); }else dbr.put("des4","");
				 * dbr.put("skpat_conf_med",dbrpato.get("skpat_conf_med")); }
				 ***/

				// 22/11/06 m.
				leggiDiagnosi(dbc, dbr);

				// 05/02/13 
				dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));	
				
				/****
				 * 31/10/06 m.: XORA non utilizzano il progetto assistenziale,
				 * casomai poi SOSTITUIRE medprogass con nuova tabella
				 * *********************************** String myprest=
				 * "SELECT mepa_data,mepa_progetto FROM medprogass WHERE "+
				 * "n_cartella="+(String)h.get("n_cartella")+" and "+
				 * "n_contatto="+(String)h.get("n_contatto"); ISASCursor
				 * cur_ass=dbc.startCursor(myprest); Vector
				 * dbass=cur_ass.getAllRecord(); dbr.put("griglia_ass",dbass);
				 * //Jessy 26-06-05 cur_ass.close();
				 **************************************************************************************/

				String selg = "Select * from skmpal_terapia where " + " n_cartella=" + (String) h.get("n_cartella") + " and n_contatto="
						+ (String) h.get("n_contatto") + " ORDER BY skt_progr";
				ISASCursor dbgriglia = dbc.startCursor(selg);
				Vector vdbg = dbgriglia.getAllRecord();

				// Elisa 27/11/09
				if (vdbg != null && vdbg.size() > 0) {
					for (int i = 0; i < vdbg.size(); i++) {
						ISASRecord dbrTerap = (ISASRecord) vdbg.get(i);

						if (dbrTerap != null) {
							if (dbrTerap.get("skt_farmaco") != null && !dbrTerap.get("skt_farmaco").equals(""))
								dbrTerap.put("skt_farmaco_desc", ISASUtil.getDecode(dbc, "farmaci", "sf_codice", ""
										+ dbrTerap.get("skt_farmaco"), "sf_descrizione"));

							decodificaCampi(dbc, dbrTerap);
						}
					}
				}
				// -------------------------------------------------------------------------------------

				dbr.put("griglia", vdbg);
				dbgriglia.close();

				// 20/05/09 Elisa Croci
				if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
					// 15/06/09 Elisa Croci
					// ********************************************************
					if (h.containsKey("ubicazione") && h.get("ubicazione") != null)
						dbr.put("ubicazione", h.get("ubicazione"));
					if (h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
						dbr.put("update_segnalazione", h.get("update_segnalazione"));
					// *********************************************************************************

					int caso = prendi_dati_caso(dbc, dbr);
					if (prendi_segnalazione(dbc, caso, dbr))
						prendi_presacarico(dbc, caso, dbr);
				}

				//cv: ripristinare la riga sottostante
				if(h.containsKey("ubicazione") && h.get("ubicazione").toString().equals(Integer.toString(GestCasi.UBI_RTOSC))) decodePatologie(dbc,dbr); 
				
				System.out.println(" DBR RETURN == " + dbr.getHashtable().toString());
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e1) {
			System.out.println("SkMedPalEJB.queryKey(): " + e1);
			throw new CariException(msgNoD, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw newEjbException("Errore eseguendo una queryKey()  ", e);
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	private String decodificaOppio(ISASRecord dbrTerap) {
		String oppiodeForte = ISASUtil.getValoreStringa(dbrTerap, "skt_oppiode_forte");
		String decodeOppiodeForte = "NO";
		if (ISASUtil.valida(oppiodeForte) && oppiodeForte.equalsIgnoreCase("S")) {
			decodeOppiodeForte = "SI";
		}

		return decodeOppiodeForte;
	}

	private void decodificaCampi(ISASConnection dbc, ISASRecord dbrTerap) throws ISASMisuseException, Exception {
		String punto = nomeEJB + "decodificaCampi 99999";
		stampa(punto + "Inzio con dati ");
		// if(dbrTerap.get("skt_dosaggio") != null &&
		// !dbrTerap.get("skt_dosaggio").equals(""))
		// dbrTerap.put("skt_dosaggio_desc", ISASUtil.getDecode(dbc, "tab_voci",
		// "tab_cod", "tab_val", "CPDOSAGG",
		// ""+dbrTerap.get("skt_dosaggio") , "tab_descrizione"));
		if (dbrTerap != null) {
			try {
				// System.out.println(punto + "\n Ricevo>" +
				// dbrTerap.getHashtable() + "<\n");
				if (dbrTerap.get("skt_formulazione") != null && !dbrTerap.get("skt_formulazione").equals("")) {
					dbrTerap.put("skt_formulazione_descr", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "FORMFARM", ""
							+ dbrTerap.get("skt_formulazione"), "tab_descrizione"));
				}
				if (dbrTerap.get("skt_form_qta") != null && !dbrTerap.get("skt_form_qta").equals("")) {
					dbrTerap.put("skt_form_qta_descr", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "FORMQTA", ""
							+ dbrTerap.get("skt_form_qta"), "tab_descrizione"));
				}

				if (dbrTerap.get("skt_form_tempo") != null && !dbrTerap.get("skt_form_tempo").equals("")) {
					dbrTerap.put("skt_form_tempo_descr", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "FORMTEMP", ""
							+ dbrTerap.get("skt_form_tempo"), "tab_descrizione"));
				}

				if (dbrTerap.get("skt_form_somm") != null && !dbrTerap.get("skt_form_somm").equals("")) {
					dbrTerap.put("skt_form_somm_descr", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "FORMSOMM", ""
							+ dbrTerap.get("skt_form_somm"), "tab_descrizione"));
				}
			} catch (Exception e) {
				System.out.println(punto + "\n\n ECCEZIONE NELLA DECODIFICA DEI DATI>" + e.getMessage() + "<");
			}
			// System.out.println(punto + "\n Restituisco>" +
			// dbrTerap.getHashtable() + "<\n");
			String oppiodeForte = ISASUtil.getValoreStringa(dbrTerap, "skt_oppiode_forte");
			String decodeOppiodeForte = "NO";
			if (ISASUtil.valida(oppiodeForte) && oppiodeForte.equalsIgnoreCase("S")) {
				decodeOppiodeForte = "SI";
			}

			stampa(punto + " decodeOppiodeForte>" + decodeOppiodeForte + "<oppiodeForte>" + oppiodeForte + "<");
			dbrTerap.put("skt_oppiode_forte_dec", decodeOppiodeForte);
		} else {
			System.out.println(punto + "\n RECORD NON VALIDO dbrTerap ");
		}
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	// 17/09/07
	public ISASRecord getContattoMedPalCorrente(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException,
			CariException {
		String punto = nomeEJB + ".getContattoMedPalCorrente 99999";
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null; // 07/08/07

		String n_cartella = (String) h.get("n_cartella");
		String strDtSkVal = (String) h.get("pr_data");// 07/08/07

		try {
			// 07/08/07 ---
			if (strDtSkVal == null) {
				printError("\nSkMedPalEJB -->> getContattoMedPalCorrente: dataSkVal NULLA!!");
				done = true;
				return dbr;
			}
			// 07/08/07 ---

			// Ottengo la connessione al database
			dbc = super.logIn(mylogin);

			// Preparo la SELECT del record
			String myselect = "SELECT * FROM skmedpal" + " WHERE n_cartella = " + n_cartella + " AND skm_data_chiusura IS NULL";

			printError("SkMedPalEJB/getContattoMedPalCorrente: " + myselect);
			dbr = dbc.readRecord(myselect);

			String w_codice = "";
			String w_select = "";
			ISASRecord w_dbr = null;

			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				if (h1.get("cod_operatore") != null && !((String) h1.get("cod_operatore")).equals("")) {
					dbr.put("desc_operat", decodifica("operatori", "codice", h1.get("cod_operatore"),
							"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')", dbc));
				} else
					dbr.put("desc_operat", "");

				if (h1.get("skm_osp_dim") != null && !((String) h1.get("skm_osp_dim")).equals("")) {
					dbr.put("des_osp", decodifica("ospedali", "codosp", h1.get("skm_osp_dim"), "descosp", dbc));
				} else
					dbr.put("des_osp", "");

				if (h1.get("skm_uo_dim") != null && !((String) h1.get("skm_uo_dim")).equals("")) {
					dbr.put("des_rep", decodifica("reparti", "cd_rep", h1.get("skm_uo_dim"), "reparto", dbc));
				} else
					dbr.put("des_rep", "");

				if (h1.get("skm_medico") != null && !((String) h1.get("skm_medico")).equals("")) {
					w_codice = (String) h1.get("skm_medico");
					w_select = "SELECT * FROM operatori WHERE codice='" + w_codice + "'";
					w_dbr = dbc.readRecord(w_select);
					dbr.put("desc_inf", w_dbr.get("cognome") + " " + w_dbr.get("nome"));
				} else
					dbr.put("desc_inf", "");

				// Elisa 18/11/09 - medico di famiglia
				if (h1.get("skm_mmg") != null && !((String) h1.get("skm_mmg")).equals("")) {
					// se e' stato scelto, lo decodifico
					String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "mecogn") + " "
							+ ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "menome");
					System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
					dbr.put("mecogn", desc_mmg);
				} else {
					// propongo il medico indicato su anagra_c
					String codmed = ISASUtil.getDecode(dbc, "anagra_c", "n_cartella", n_cartella, "cod_med");
					System.out.println(nomeEJB + " codmed == " + codmed);
					if (codmed != null && !codmed.equals("")) {
						String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "mecogn") + " "
								+ ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "menome");
						System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
						dbr.put("skm_mmg", codmed);
						dbr.put("mecogn", desc_mmg);
					}
				}

				//elisa b 09/06/11
				if (h1.get("skm_farmaco_sedazione") != null && !((String) h1.get("skm_farmaco_sedazione")).equals("")) {
					dbr.put("des_farmaco_sedazione", decodifica("farmaci", "sf_codice", h1.get("skm_farmaco_sedazione"), "sf_descrizione",
							dbc));
				} else
					dbr.put("des_farmaco_sedazione", "");
				System.out.println("des_farmaco_sedazione " + dbr.get("des_farmaco_sedazione").toString());

				String farmacoTer = ISASUtil.getValoreStringa(h1, "skm_farmaco_ter");
				decodificaFarmacoTerapeutico(dbc, dbr, farmacoTer);

				// ------------------------------------------------------------------------------

				// 07/12/06 m.
				leggiDiagnosi(dbc, dbr);

				String selg = "Select * from skmpal_terapia where " + " n_cartella=" + n_cartella + " and n_contatto="
						+ dbr.get("n_contatto") + " ORDER BY skt_progr";
				stampa(punto + "\n Query da eseguire>" + selg + "<\n");
				ISASCursor dbgriglia = dbc.startCursor(selg);
				Vector vdbg = dbgriglia.getAllRecord();
				stampa(punto + "Dopo lettura ");
				// Elisa 27/11/09
				if (vdbg != null && vdbg.size() > 0) {
					for (int i = 0; i < vdbg.size(); i++) {
						ISASRecord dbrTerap = (ISASRecord) vdbg.get(i);

						if (dbrTerap != null) {
							if (dbrTerap.get("skt_farmaco") != null && !dbrTerap.get("skt_farmaco").equals(""))
								dbrTerap.put("skt_farmaco_desc", ISASUtil.getDecode(dbc, "farmaci", "sf_codice", ""
										+ dbrTerap.get("skt_farmaco"), "sf_descrizione"));

							if (dbrTerap.get("skt_dosaggio") != null && !dbrTerap.get("skt_dosaggio").equals(""))
								dbrTerap.put("skt_dosaggio_desc", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "CPDOSAGG", ""
										+ dbrTerap.get("skt_dosaggio"), "tab_descrizione"));
							decodificaCampi(dbc, dbrTerap);

						}
					}
				}
				// ----------------------------------------------------------------------------------------

				dbr.put("griglia", vdbg);

				// Jessy 26-06-05
				dbgriglia.close();

				dbr.put("pr_data", strDtSkVal);// 07/08/07
				
				// 05/02/13 
				dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));					

				// 05/09 Elisa Croci
				if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
					// 15/06/09 Elisa Croci
					// ********************************************************
					if (h.containsKey("ubicazione") && h.get("ubicazione") != null)
						dbr.put("ubicazione", h.get("ubicazione"));
					if (h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
						dbr.put("update_segnalazione", h.get("update_segnalazione"));
					// *********************************************************************************

					int caso = prendi_dati_caso(dbc, dbr);
					if (prendi_segnalazione(dbc, caso, dbr))
						prendi_presacarico(dbc, caso, dbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;
			// stampaRecord(punto, dbr);
			return dbr;
		}
		// 07/08/07 ---
		catch (ISASPermissionDeniedException e1) {
			System.out.println("SkMedPalEJB.getContattoMedPalCorrente(): " + e1);
			throw new CariException(msgNoD, -2);
		}
		// 07/08/07 ---
		catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkMedPalEJB.getContattoMedPalCorrente(): " + e);
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

	private void decodificaFarmacoTerapeutico(ISASConnection dbc, ISASRecord dbr, String farmacoTer) throws ISASMisuseException {

		String descrizioneFarmacoTerapeutico = "";
		if (ISASUtil.valida(farmacoTer)) {
			descrizioneFarmacoTerapeutico = decodifica("farmaci", "sf_codice", farmacoTer, "sf_descrizione", dbc) + "";
		}
		dbr.put("des_farmaco_ter", descrizioneFarmacoTerapeutico);
	}

	private void stampaRecord1(String punto, ISASRecord dbr) {
		if (dbr != null) {
			System.out.println(punto + " Record valido \ndati>" + dbr.getHashtable() + "<");
		} else {
			System.out.println(punto + " Il recodo non è valido");
		}

	}

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM skmedpal WHERE n_cartella=" + (String) h.get("n_cartella")
					+ " ORDER BY skm_data_apertura DESC ";
			ISASCursor dbcur = dbc.startCursor(myselect);
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

	// 11/12/14 mv: ulteriori richieste ASL10 per sinnst-hospice
	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  Exception {
		String nomeMetodo = "queryPaginate";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);

			LOG.info(nomeMetodo+" -  h INPUT["+h.toString()+"]");
			
			
			//Filtri per operatore loggato
			String codice_operatore 	= (String)h.get("skm_medico");

			//Filtri di ricerca
			String n_contatto 		    = (String)h.get("n_contatto");
			String n_cartella 		    = (String)h.get("n_cartella");

			String apertura_inizio 	    = (String)h.get("apertura_inizio");
			String apertura_fine 	    = (String)h.get("apertura_fine");
			String chiusura_inizio      = (String)h.get("chiusura_inizio");
			String chiusura_fine 	    = (String)h.get("chiusura_fine");
			String cognome 				= (String)h.get("cognome");
			String nome 				= (String)h.get("nome");
			
			String ordina_per 	= (String)h.get("ordina_per");
			String tipo_ordine 	= (String)h.get("tipo_ordine");
			
			String myselect = "";
			myselect = " SELECT s.*, c.cognome, c.nome, c.data_nasc "+
			" FROM skmedpal s, cartella c "+
			" WHERE s.n_cartella = c.n_cartella ";
			
			if(codice_operatore!=null && !codice_operatore.trim().equals(""))
				myselect+=" AND s.skm_medico = '"+codice_operatore+"'";
			
			if(n_contatto!=null && !n_contatto.trim().equals(""))
				myselect+=" AND s.n_contatto = "+n_contatto;
			
			if(n_cartella!=null && !n_cartella.trim().equals(""))
				myselect+=" AND s.n_cartella = "+n_cartella;
			
			if(apertura_inizio!=null && !apertura_inizio.trim().equals(""))
				myselect += " AND s.skm_data_apertura >= "+formatDate(dbc,apertura_inizio);
			if(apertura_fine!=null && !apertura_fine.trim().equals(""))
				myselect += " AND s.skm_data_apertura <= "+formatDate(dbc,apertura_fine);
			
			if(chiusura_inizio!=null && !chiusura_inizio.trim().equals(""))
				myselect += " AND s.skm_data_chiusura >= "+formatDate(dbc,chiusura_inizio);
			if(chiusura_fine!=null && !chiusura_fine.trim().equals(""))
				myselect += " AND s.skm_data_chiusura <= "+formatDate(dbc,chiusura_fine);
			
			//Cognome
			if(cognome!=null && !cognome.trim().equals("")){
				cognome=su.duplicateChar(cognome,"'");
				myselect += "AND c.cognome like '%"+cognome+"%'";

			}
			//Nome
			if(nome!=null && !nome.trim().equals("")){
				nome=su.duplicateChar(nome,"'");
				myselect += "AND c.nome like '%"+nome+"%'";
			}
					
			//ORDINAMENTO
			String ordine_modo = " DESC ";//DEFAULT
			String ordine_campo = " c.cognome "+ordine_modo+", c.nome "+ordine_modo;//DEFAULT
			if(tipo_ordine!=null && !tipo_ordine.trim().equals("")){
				ordine_modo = tipo_ordine;
			}
			if(ordina_per!=null && !ordina_per.trim().equals("")){
				if(ordina_per.equals("COGN"))
					ordine_campo = " c.cognome "+ordine_modo+", c.nome ";
				else if(ordina_per.equals("DTAPER"))
					ordine_campo = " s.skm_data_apertura ";
				else if(ordina_per.equals("DTCHIUS"))
					ordine_campo = " s.skm_data_chiusura ";
				else if(ordina_per.equals("MEDREF"))
					ordine_campo = " s.skm_medico ";
			}
			myselect += " ORDER BY "+ordine_campo+ ordine_modo;
			

			LOG.trace(nomeMetodo+" - myselect: "+myselect);

			ISASCursor dbcur=dbc.startCursor(myselect);
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);

			vdbr = decodificaVectorISASRecord(dbc, vdbr);
			
			LOG.info(nomeMetodo+" -  Metodo eseguito: vdbr.size=["+vdbr.size()+"]");
			
			return vdbr;			
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			logout_nothrow(nomeMetodo, dbc);
		}
	}	
	
	
	
	
	// 17/09/07: caricamento della grid della frame "JFrameGridSkMedPal"
	public Vector query_loadGridSkMedPal(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException {
		boolean done = false;
		ISASConnection dbc = null;

		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String) h.get("pr_data");// 26/10/06

		Vector vdbr = new Vector();

		try {
			// 26/10/06 ---
			if (strDtSkVal == null) {
				printError("\nSkMedPalEJB -->> query_loadGridSkMed: dataSkVal NULLA!!");
				done = true;
				return vdbr;
			}
			// 26/10/06 ---

			// Connessione al database
			dbc = super.logIn(mylogin);

			// Compongo la SELECT
			String myselect = "SELECT skm.*" + " FROM skmedpal skm," + " progetto_cont pc"
					+ // 26/10/06
					" WHERE skm.n_cartella = " + strNAssistito
					+ " AND skm.skm_data_chiusura IS NOT NULL"
					+
					// 26/10/06 : x estrarre solo quelli collegati ad una scheda
					// valutaz
					" AND pc.prc_tipo_op = '52'" + " AND pc.n_cartella = skm.n_cartella" + " AND pc.pr_data = "
					+ formatDate(dbc, strDtSkVal) + " AND pc.prc_n_contatto = skm.n_contatto" +
					// 26/10/06
					// --------------------------------------------------------
					" ORDER BY skm.skm_data_apertura, skm.skm_data_chiusura";

			printError("-->>query GridSkMedPal: " + myselect);

			// Leggo i record
			ISASCursor dbcur = dbc.startCursor(myselect);

			// Metto i record letti in un vector (un vector di ISASRecord).
			vdbr = dbcur.getAllRecord();

			// Decodifica dei Cognomi e Nomi degli operatori in tutti gli
			// ISASRecord del Vector
			decodificaQueryInfo(dbc, vdbr);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la query_loadGridSkMedPal()  ");
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

	// 17/09/07
	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr) throws Exception {
		for (int i = 0; i < vdbr.size(); i++) {
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			decodificaQueryOperatore(mydbc, dbr, "cod_operatore", "operatore_apertura");
			decodificaQueryOperatore(mydbc, dbr, "skm_medico", "operatore_referente");
		}
	}

	// 17/09/07
	private void decodificaQueryOperatore(ISASConnection mydbc, ISASRecord dbr, String dbFldNameCod, String dbName) throws Exception {
		String strCodOperatore = (String) dbr.get(dbFldNameCod);

		String strCognome = "";
		String strNome = "";

		if (strCodOperatore == null) {
			dbr.put(dbName, "");
			return;
		}
		String selS = "SELECT cognome, nome FROM operatori" + " WHERE codice = '" + strCodOperatore + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null) {
			if (rec.get("cognome") != null)
				strCognome = (String) rec.get("cognome");
			if (rec.get("nome") != null)
				strNome = (String) rec.get("nome");
		}
		dbr.put(dbName, strCognome + " " + strNome);
	}

	public Vector query_terapia(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select * from skmpal_terapia where " + " n_cartella=" + (String) h.get("n_cartella") + " and n_contatto="
					+ (String) h.get("n_contatto") + " ORDER BY skm_data_apertura,skt_progr DESC ";

			ISASCursor dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();

			// Elisa 27/11/09
			if (vdbr != null && vdbr.size() > 0) {
				for (int i = 0; i < vdbr.size(); i++) {
					ISASRecord dbrTerap = (ISASRecord) vdbr.get(i);

					if (dbrTerap != null) {
						if (dbrTerap.get("skt_farmaco") != null && !dbrTerap.get("skt_farmaco").equals(""))
							dbrTerap.put("skt_farmaco_desc", ISASUtil.getDecode(dbc, "farmaci", "sf_codice", ""
									+ dbrTerap.get("skt_farmaco"), "sf_descrizione"));

						if (dbrTerap.get("skt_dosaggio") != null && !dbrTerap.get("skt_dosaggio").equals(""))
							dbrTerap.put("skt_dosaggio_desc", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "CPDOSAGG", ""
									+ dbrTerap.get("skt_dosaggio"), "tab_descrizione"));
						decodificaCampi(dbc, dbrTerap);
					}
				}
			}
			// -------------------------------------------------------------------------------------

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_terapia()  ");
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

	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
			CariException // 17/09/07
	{
		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		String data_apertura = null;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;// 22/11/06 m.
		boolean caso_gestito = false;

		try {
			n_cartella = (String) h.get("n_cartella");
			n_contatto = (String) h.get("n_contatto");
			data_apertura = (String) h.get("skm_data_apertura");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkMedPal insert: Errore: manca la chiave primaria");
		}

		String strDtSkVal = (String) h.get("pr_data");// 17/09/07

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			// 31/10/14
			String data_chiusura = (String) h.get("skm_data_chiusura");
			
			// 07/08/07 *************************
			if (dtApeContLEMaxDtContChius(dbc, h)) {
				String msg = "Attenzione: Data apertura nuovo contatto antecedente o uguale a data chiusura di ultimo contatto chiuso!";
				throw new CariException(msg, -2);
			}

			// 13/10/14
			HSPSkMedPalEJB hspSk = new HSPSkMedPalEJB();
			if (!hspSk.checkDtApeNotInHSP(dbc, n_cartella, data_apertura, data_chiusura)) {
				String msg = "Attenzione: Data apertura contatto compresa in scheda Hospice!";
				throw new CariException(msg, -2);
			}
			
			
			// 07/08/07: si ottiene il nuovo progressivo (non si usa più
			// CONTSAN).
			int intProgressivo = getProgressivo(dbc, n_cartella);
			Integer iProgressivo = new Integer(intProgressivo);
			n_contatto = iProgressivo.toString();

			ISASRecord dbr = dbc.newRecord("skmedpal");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			// 07/08/07: Si setta il campo 'n_contatto' col nuovo progressivo
			dbr.put("n_contatto", iProgressivo);

			caso_gestito = (dbr.get("caso_gestito")!=null && dbr.get("caso_gestito").toString().equals("S"));

			
			dbc.writeRecord(dbr);

			// 26/02/16 mv: per evitare inserimenti concorrenti
			String msgErr1 = "Impossibile inserire: \nesiste un altro contatto cure palliative aperto";
			ISASUtil.checkUnicoContattoAperto(dbc, h, iProgressivo.toString(), "skmedpal", "skm_data_chiusura", msgErr1);
			
			
			// 07/08/07: scrittura su PROGETTO_CONT ed, eventualmente, PROGETTO
			// -----
			
			if ((strDtSkVal == null) || ((strDtSkVal != null) && (strDtSkVal.trim().equals(""))))
			{
				Hashtable h_pr = new Hashtable();
				h_pr.put("n_cartella",dbr.get("n_cartella").toString());
				h_pr.put("data_apertura",dbr.get("skm_data_apertura").toString());
				ISASRecord dbr_pr = cercaProgetto(dbc, h_pr);
				if (dbr_pr!=null)
					strDtSkVal = dbr_pr.get("pr_data").toString();
				
			}
			
			
			if (strDtSkVal == null) {
				strDtSkVal = (String) h.get("skm_data_apertura");
				// gb 02/11/06
				// Mettere controllo che data_ape sk_valutaz. fittizia sia >=
				// data chiusura di ultima sk_valutaz. chiusa pre-esistente.
				if (dtApeMinoreMaxDtChius(dbc, n_cartella, strDtSkVal)) {
					String msg = "Attenzione: Data apertura antecedente a data chiusura di ultima Scheda valutazione chiusa!";
					throw new CariException(msg, -2);
				}

				// gb 01/06/07: Controllo che la data di apertura del contatto
				// (skm_data_apertura)
				// sia >= data_apetura della tab. cartella.
				if (dtApeContattoLTDtApeCartella(dbc, n_cartella, strDtSkVal)) {
					String msg = "Attenzione: Data apertura contatto e' antecedente alla data apertura dell'assistito!";
					throw new CariException(msg, -2);
				}

				scriviProgetto(dbc, n_cartella, strDtSkVal);
			}

//			scriviProgettoCont(dbc, n_cartella, strDtSkVal, "52", n_contatto);
			// 07/08/07
			// -------------------------------------------------------------

			String myselect = "SELECT * FROM skmedpal WHERE n_cartella=" + n_cartella + " AND n_contatto=" + n_contatto
					+ " AND skm_data_apertura=" + formatDate(dbc, data_apertura);

			dbr = dbc.readRecord(myselect);


			if (dbr.get("skm_data_chiusura") != null)
				data_chiusura = ((java.sql.Date) dbr.get("skm_data_chiusura")).toString();
			String farmacoTer = ISASUtil.getValoreStringa(dbr, "skm_farmaco_ter");
			decodificaFarmacoTerapeutico(dbc, dbr, farmacoTer);

			String selref = "SELECT * FROM skmedpal_referente WHERE " + "n_cartella=" + n_cartella + " AND " + "n_contatto=" + n_contatto;
			// "ski_data_apertura="+formatDate(dbc,data_apertura)

			ISASRecord dbref = dbc.readRecord(selref);
			if (dbref == null) {
				if (dbr.get("skm_medico") != null && !((String) dbr.get("skm_medico")).equals("")) {
					String infref = (String) dbr.get("skm_medico");
					String data_ref = "";

					if (dbr.get("skm_medico_da") != null)
						data_ref = ((java.sql.Date) dbr.get("skm_medico_da")).toString();

					this.insertMedRef(dbc, infref, data_ref, data_apertura, n_cartella, n_contatto);
				}
			}

			/**
			 * 17/09/07: eliminato tabella CONTSAN String
			 * operatore=(String)dbr.get("cod_operatore"); String
			 * selcont="SELECT * FROM contsan where n_cartella="+n_cartella+
			 * " AND n_contatto="+n_contatto; ISASRecord
			 * dbcont=dbc.readRecord(selcont); if(dbcont!=null)
			 * this.updateContsan
			 * (dbc,data_apertura,data_chiusura,descr_medico,n_cartella
			 * ,n_contatto); else
			 * this.insertContsan(dbc,data_apertura,data_chiusura
			 * ,descr_medico,n_cartella,operatore);
			 * //this.updateContsan(dbc,data_apertura
			 * ,data_chiusura,descr_medico,n_cartella,n_contatto);
			 ***/

			// 22/11/06 m.
			leggiDiagnosi(dbc, dbr);

			// 07/08/07
			dbr.put("pr_data", strDtSkVal);

			// 15/07/09 m. ------------------
			// lettura dtConclusione CASO precedente
			h.put("pr_data", strDtSkVal);
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String) h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, strDtSkVal, "", dtChiusCasoPrec, "", tempoT, "52");
			dbr.put("tempo_t", tempoT);
			// 15/07/09 m. ------------------
			int idCaso = -1;
			// 21/05/09 Elisa Croci
			//Simone 130117 se ho giÃ  gestito il caso aggiornando la data di presa in carico e chiudendo il contatto, escludo questa parte di codice
			if (!caso_gestito) {

				if (gestore_segnalazioni.isSegnalDaGestire(dbc,
						GestCasi.CASO_SAN)) {
					idCaso = prendi_dati_caso(dbc, dbr);
					idCaso = gestione_segnalazione(dbc, dbr, h, "insert");
					// aggiornaDataMinimaCaso(dbc, h, idCaso, dbr);
					printError("Fatta gestione segnalazione per il caso: "
							+ idCaso);
				} else {
					h.put("dt_segnalazione", data_apertura);
					h.put("dt_presa_carico", data_apertura);
					// da rfc115 v9 ï¿½ richiesta anche la data di valutazione
					h.put("valutazione_data", data_apertura);
					idCaso = gestore_casi.apriCasoSan(dbc, h).intValue();
				}
				// 15/06/09 Elisa Croci
				// ***************************************************************
				if (data_chiusura != null && !data_chiusura.equals("")) {
					// 15/12/14 mv: richiesta ASL10 mail 02/12/14
					String confAbilChiuAll = leggiConf(dbc,
							KEYCONF_CHIUSURA_TUTTI_CONT);
					if ((confAbilChiuAll != null)
							&& (confAbilChiuAll.trim().equals("SI"))) {
						LOG.debug("Chiusura SkMedPal: abilitato a chiudere tutti i contatti");
						chiudiAll(dbc, n_cartella, data_chiusura,
								mylogin.getUser());
					} else {								
						printError("Controllo contatto UNICO SANITARIO H == "
								+ h.toString());
						boolean unico = gestore_casi
								.query_checkUnicoContAperto(dbc, h, true, true);
						if (idCaso != -1 && unico) {
							printError("Gestisco la chiusura del caso");
							// E' uguale ad S quando c'e' la possibilita' che ci
							// siano piu' contatti e questo e'
							// l'ultimo contatto aperto che stiamo chiudendo!
							// Quindi
							// devo chiudere, se esiste, il caso
							// sociale associato!
							int origine = -1;
							if (dbr.get("origine") != null
									&& !(dbr.get("origine").toString())
									.equals(""))
								origine = Integer.parseInt(dbr.get("origine")
										.toString());
							else if (h.get("origine") != null
									&& !(h.get("origine").toString())
									.equals(""))
								origine = Integer.parseInt(h.get("origine")
										.toString());
							if (origine != -1) {
								printError("Origine del caso: " + origine);
								if (origine == GestCasi.CASO_SAN) {
									Hashtable hCaso = new Hashtable();
									hCaso.put("n_cartella", h.get("n_cartella"));
									hCaso.put("pr_data", h.get("pr_data"));
									hCaso.put("id_caso", new Integer(idCaso));
									hCaso.put("dt_conclusione",
											dbr.get("skm_data_chiusura"));
									// 26/03/10							hCaso.put("motivo", "99");
									// 26/03/10 ----
									String motChiu = (String) h
											.get("skm_motivo_chius");
									String motChiuFlux = getTabVociCodReg(dbc,
											"MCHIUS", motChiu);
									hCaso.put("motivo", motChiuFlux);
									// 26/03/10 ----
									hCaso.put("operZonaConf",
											(String) dbr.get("cod_operatore")); // 15/10/09
									printError(" -- update(): Chiudi caso = HashCaso: "
											+ hCaso.toString());
									Integer r = gestore_casi.chiudiCaso(dbc,
											hCaso);
									printError("Ritorno di ChiudiCaso == " + r);
								}
							}
						}
					}
				}
			}
			// ****************************************************************************************

			// 15/06/09 Elisa Croci
			// ***************************************************************
			if (h.containsKey("ubicazione") && dbr != null)
				dbr.put("ubicazione", h.get("ubicazione"));
			if (h.containsKey("update_segnalazione") && dbr != null)
				dbr.put("update_segnalazione", h.get("update_segnalazione"));
			// *************************************************************************************

			// Elisa 18/11/09 - medico di famiglia
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				if (h1.get("skm_mmg") != null && !((String) h1.get("skm_mmg")).equals("")) {
					// se e' stato scelto, lo decodifico
					String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "mecogn") + " "
							+ ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "menome");
					System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
					dbr.put("mecogn", desc_mmg);
				} else {
					// propongo il medico indicato su anagra_c
					String codmed = ISASUtil.getDecode(dbc, "anagra_c", "n_cartella", n_cartella, "cod_med");
					System.out.println(nomeEJB + " codmed == " + codmed);
					if (codmed != null && !codmed.equals("")) {
						String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "mecogn") + " "
								+ ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "menome");
						System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
						dbr.put("skm_mmg", codmed);
						dbr.put("mecogn", desc_mmg);
					}
				}
			}
			// ------------------------------------------------------------------------------
			
			// 05/02/13 
			dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));
			if (h.containsKey("ubicazione") && h.get("ubicazione").toString().equals(Integer.toString(GestCasi.UBI_RTOSC))) decodePatologie(dbc,dbr); 
			
			printError("SkMedPalEJB: insert -- DBR restituito === " + dbr.getHashtable().toString());

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		// 17/09/07 **************
		catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				System.out.println("SkMedPalEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new CariException("Errore eseguendo la rollback() - " + e1);
			}
			throw ce;
		}
		// *************************
		catch (DBRecordChangedException e) {
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
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception ex) {
				throw new SQLException("Errore eseguendo una rollback() - " + ex);
			}
			throw new SQLException("Errore eseguendo una insert() - " + e1);
		} finally {
			if (!done) {
				try {
					if (dbcur != null) // 22/11/06 m.
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}

	}
	
//	private void aggiornaDataMinimaCaso(ISASConnection dbc, Hashtable h,int idCaso, ISASRecord dbr) throws Exception {
//		String punto = ver   + "aggiornaDataMinima ";
//		LOG.info(punto + " inizio con dati>" +(h!=null ? h + "": " no dati "));
//		if (idCaso > 0) {
//			LOG.debug(punto + " idCaso>" + idCaso);
//			
//			Hashtable h_dati = new Hashtable();
//			h_dati.put(GestCasi.CTS_ID_CASO, idCaso + "");
//			h_dati.put("n_cartella", h.get("n_cartella"));
//			h_dati.put("pr_data", h.get("pr_data"));
//			ISASRecord rec_caso = gestore_casi.getCaso(dbc, h_dati);
//			Integer origineCaso = (Integer)rec_caso.get("origine");
//			LOG.debug(punto + " origineCaso>" + origineCaso.toString());
//			if (origineCaso.intValue() != GestCasi.CASO_SAN)
//				return;
//			
//			String dtPresaCaricoOLD = ISASUtil.getValoreStringa(dbr, "dt_presa_carico");
//			String dtPresaCarico = ISASUtil.getValoreStringa(h, "skm_data_apertura");
//			h.put(GestCasi.CTS_TIPO_CASO, GestCasi.CASO_SAN+"");
//			h.put(GestCasi.CTS_PR_DATA_PRESA_CARICO_INSERIRE, dtPresaCarico);
//			h.put(GestCasi.CTS_ID_CASO, idCaso+"");
//			
//			h_dati.put(GestCasi.CTS_TIPO_CASO, GestCasi.CASO_SAN + "");
//			h_dati.put(GestCasi.CTS_PR_DATA_PRESA_CARICO_INSERIRE, dtPresaCarico);
//			
//			gestore_casi.aggiornaDataMinimaCaso(dbc, h_dati, dbr);
//			
//			String dtPresaCaricoNew = ISASUtil.getValoreStringa(dbr, "dt_presa_carico");
//			if (DataWI.validaData(dtPresaCaricoNew)){
//				if (DataWI.validaData(dtPresaCaricoOLD) && dtPresaCaricoOLD.equals(dtPresaCaricoNew)){
//					LOG.debug(punto + " stessa data presa carico: non aggiorno la presacarico");
//				}else {
//					ISASRecord dbrPresaCarico = gestore_presacarico.queryKey(dbc, h);
//					if (dbrPresaCarico!=null){
//						h.put("dt_presa_carico", dtPresaCaricoNew);
//						gestore_presacarico.update(dbc, h);  
//					}
//					ISASRecord dbrSegnalazione = gestore_segnalazioni.queryKey(dbc, h);
//					if (dbrSegnalazione!=null) {
//						h.put("dt_segnalazione", dtPresaCaricoNew);
//						gestore_segnalazioni.update(dbc, h);
//					}
//				}
//			}else {
//				LOG.debug(punto + " NON HO trovato una data minima, per cui lascio tutto inalterato.");
//			}
//		}else {
//			LOG.info(punto + " Non aggiorno il caso ");
//		}
//	}

	// 17/09/07
	private boolean dtApeContattoLTDtApeCartella(ISASConnection dbc, String strNAssistito, String strDtApeCont) throws Exception {
		String mySel = "SELECT *" + " FROM cartella" + " WHERE n_cartella = " + strNAssistito + " AND data_apertura > "
				+ formatDate(dbc, strDtApeCont);

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile
		else
			return true;
	}

	// 17/09/07
	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, String strNAssistito, String strDtSkVal) throws Exception {
		String dt = strDtSkVal;
		// 25/06/07 dt = dt.substring(0,2) + dt.substring(3,5) +
		// dt.substring(6,10);
		dt = dt.substring(8, 10) + dt.substring(5, 7) + dt.substring(0, 4);
		DataWI dataWIApertura = new DataWI(dt);

		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" + " FROM progetto" + " WHERE n_cartella = " + strNAssistito
				+ " AND pr_data_chiusura IS NOT NULL";

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile

		if ((java.sql.Date) rec.get("max_data_chius") == null)
			return false; // Ammissibile

		dt = ((java.sql.Date) rec.get("max_data_chius")).toString();
		if (dt.equals(""))
			return false; // Ammissibile

		dt = dt.substring(0, 4) + dt.substring(5, 7) + dt.substring(8, 10);
		String max_data_chiusura = dt;
		int rit = dataWIApertura.confrontaConDt(max_data_chiusura);
		// Codici ritornati da confrontaConDt:
		// se data_apertura è maggiore di data_chiusura restituisce 1
		// se data_apertura è minore di data_chiusura restituisce 2
		// se data_apertura è = di data_chiusura restituisce 0
		// se da errore -1
		if ((rit == 2) || (rit == 0))
			return true; // Non ammissibile
		else if (rit < 0) {
			throw new Exception("SkMedPalEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
			// Si è verificato un errore nel metodo di confronto delle 2 date.
		} else
			// (rit == 1)
			return false; // Ammissibile
	}

	// 17/09/07 m.: aggiunto crit su contatto diverso da quello in oggetto.
	private boolean dtApeContLEMaxDtContChius(ISASConnection dbc, Hashtable h) throws Exception {
		String strNCartella = (String) h.get("n_cartella");
		String strDataApeContatto = (String) h.get("skm_data_apertura");
		String strNContatto = (String) h.get("n_contatto"); // 13/09/07 m.

		String mySel = "SELECT skm_data_chiusura" + " FROM skmedpal" + " WHERE n_cartella = " + strNCartella
				+ (strNContatto != null ? " AND n_contatto <> " + strNContatto : "") + // 13/09/07 m.
				" AND skm_data_chiusura >= " + formatDate(dbc, strDataApeContatto) + " AND skm_data_chiusura IS NOT NULL";

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() > 0))
			return true;
		else
			return false;
	}

	// gb 12/09/07
	// *****************************************************************
	// Restituisce un messaggio appropriato se si verifica che le date di
	// apertura
	// e chiusura della scheda contatto oncologi non sono congrue con le
	// rispettive
	// date del piano assistenziale (tabella 'piano_assist').
	// Se invece sono congrue il metodo ritorna "" (stringa vuota).
	//	
	private String checkDateContEDatePianoAssist(ISASConnection dbc, Hashtable h) throws Exception {
		ISASCursor dbcur = null;
		String strNCartella = (String) h.get("n_cartella");
		String strNContatto = (String) h.get("n_contatto");
		String strDataApeContatto = (String) h.get("skm_data_apertura");
		String strDataChiuContatto = null;
		String msg = "";

		String mySel = "SELECT *" + " FROM piano_assist" + " WHERE n_cartella = " + strNCartella + " AND n_progetto = " + strNContatto
				+ " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper = '52'" + " AND pa_data < "
				+ formatDate(dbc, strDataApeContatto);
		printError("SkMedPalEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
		dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() > 0))
			msg = "Attenzione esistono Piani Assistenziali la cui data di apertura è antecedente della data apertura della scheda contatto.";
		else {
			dbcur = null;
			if (h.get("skm_data_chiusura") != null) {
				strDataChiuContatto = "" + h.get("skm_data_chiusura");

				if (strDataChiuContatto != null && !(strDataChiuContatto.equals(""))) {
					mySel = "SELECT *" + " FROM piano_assist" + " WHERE n_cartella = " + strNCartella + " AND n_progetto = " + strNContatto
							+ " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_tipo_oper = '52'"
							+ " AND pa_data_chiusura > " + formatDate(dbc, strDataChiuContatto) + " AND pa_data_chiusura IS NOT NULL ";
					printError("SkMedPalEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
					dbcur = dbc.startCursor(mySel);

					if ((dbcur != null) && (dbcur.getDimension() > 0))
						msg = "Attenzione esistono Piani Assistenziali la cui data di chiusura è successiva alla data chiusura della scheda contatto.";
				}
			}
		}

		if (dbcur != null)
			dbcur.close();
		return msg;
	}

	// 17/09/07: inserimento su tabella PROGETTO di un record con i soli valori
	// della chiave
	private void scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception {
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		printError("\n SkMedPalEJB -->> insert: Inserito record su tabella PROGETTO");
	}

	// 17/09/07: inserimento su tabella PROGETTO_CONT
	private void scriviProgettoCont_(ISASConnection mydbc, String numCart, String dtSkVal, String tpOper, String numProg) throws Exception {
		ISASRecord dbrPrgCont = mydbc.newRecord("progetto_cont");
		dbrPrgCont.put("n_cartella", numCart);
		dbrPrgCont.put("pr_data", dtSkVal);
		dbrPrgCont.put("prc_tipo_op", tpOper);
		dbrPrgCont.put("prc_n_contatto", new Integer(numProg));
		mydbc.writeRecord(dbrPrgCont);
		printError("\n SkMedPalEJB -->> insert: Inserito record su tabella PROGETTO_CONT");
	}

	// 17/09/07: ricava il nuovo progressivo per le operazioni di 'insert'.
	private int getProgressivo(ISASConnection mydbc, String strNAssistito) throws Exception {
		int intProgressivo = 0;

		String myselect = "SELECT MAX(n_contatto) max_n_contatto" + " FROM skmedpal" + " WHERE n_cartella = " + strNAssistito;
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			intProgressivo = ISASUtil.getIntField(dbr, "max_n_contatto");

		intProgressivo++;
		return intProgressivo;
	}

	
	private ISASRecord insertMedRef(ISASConnection dbc, String infref, String data_ref, String data_apertura, String n_cartella,
			String n_contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		try {
			ISASRecord dbref = dbc.newRecord("skmedpal_referente");
			dbref.put("n_cartella", n_cartella);
			dbref.put("n_contatto", n_contatto);
			dbref.put("skm_medico", infref);
			dbref.put("skm_medico_da", data_ref);
			dbc.writeRecord(dbref);

			return dbref;
		} catch (DBRecordChangedException e) {
			System.out.println("SkMedPalEJB.insertMedPalRef(): " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkMedPalEJB.insertMedPalRef(): " + e);
			throw e;
		} catch (Exception e1) {
			System.out.println("SkMedPalEJB.insertMedPalRef(): " + e1);
			throw new SQLException("Errore eseguendo una insertMedRef() - " + e1);
		}
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
			CariException // 17/09/07
	{
		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		ISASConnection dbc = null;
		String strDtSkVal = null;// 17/09/07
		boolean caso_gestito = false;

		try {
			strDtSkVal = (String) dbr.get("pr_data");// 17/09/07
			n_cartella = (String) dbr.get("n_cartella");
			n_contatto = (String) dbr.get("n_contatto");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkMedPal update: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			// 31/10/14
			String data_chiusura = (String)dbr.get("skm_data_chiusura");
			
			// 07/08/07 *************************
			Hashtable h = dbr.getHashtable();
			if (dtApeContLEMaxDtContChius(dbc, h)) {
				String msg = "Attenzione: Data apertura nuovo contatto antecedente o uguale a data chiusura di ultimo contatto chiuso!";
				throw new CariException(msg, -2);
			}

			// 13/10/14
			HSPSkMedPalEJB hspSk = new HSPSkMedPalEJB();
			String data_apertura = dbr.get("skm_data_apertura").toString();
			if (!hspSk.checkDtApeNotInHSP(dbc, n_cartella, data_apertura, data_chiusura)) {
				String msg = "Attenzione: Data apertura contatto compresa in scheda Hospice!";
				throw new CariException(msg, -2);
			}
			
			
			
			// gb 12/09/07 *************************
			String strMsgCheckDatePianoAssist = checkDateContEDatePianoAssist(dbc, h);
			if (!strMsgCheckDatePianoAssist.equals(""))
				throw new CariException(strMsgCheckDatePianoAssist, -2);

			caso_gestito = (dbr.get("caso_gestito")!=null && dbr.get("caso_gestito").toString().equals("S"));
			dbc.writeRecord(dbr);

			String myselect = "Select * from skmedpal where " + " n_cartella=" + n_cartella + " and n_contatto=" + n_contatto;

			dbr = dbc.readRecord(myselect);

			if (dbr.get("skm_data_chiusura") != null)
				data_chiusura = "" + (java.sql.Date) dbr.get("skm_data_chiusura");

//			String data_apertura = "" + (java.sql.Date) dbr.get("skm_data_apertura");

			// 17/09/07
			if (data_chiusura != null && !(data_chiusura.equals(""))) {
				// bargi 16/04/2007
				// gb 01/10/07: Controlli e chiusure entità sottostanti
				CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
				// date prestazioni erogate della tabella interv.
				// date aper. e date chius. dei piani assitenziali.
				// date aper. dei piani accessi.
				String strMsgCheckDtCh = clCcec.checkDtChDaContProgGTDtApeDtCh(dbc, n_cartella, n_contatto, data_chiusura, "52");
				if (!strMsgCheckDtCh.equals(""))
					throw new CariException(strMsgCheckDtCh, -2);

				// Chiusure entità che stanno sotto il contatto:
				// Piani assistenziali
				// Piani accessi
				// Rimozione record da agendant_interv e agendant_intpre con
				// date successive a data chiusura
				clCcec.chiudoDaContattoInGiu(dbc, n_cartella, n_contatto, data_chiusura, "52", (String) dbr.get("skf_operatore"));
				// gb 01/10/07: fine *******
				// chiudo piani assistenziali
				/*
				 * gb 01/10/07 ******* AggiornaData("piano_assist", n_cartella,
				 * n_contatto, "pa_data_chiusura", data_chiusura, "pa_data",
				 * dbc);
				 * 
				 * //rimuovo da agenda appuntamenti caricati per la cartella
				 * chiusa rimuovoAgendaCaricata(n_cartella, n_contatto,
				 * data_chiusura, dbc);gb 01/10/07: fine ******
				 */

				// 12/10/07 m. ---
				String skValDaChiudere = (String) h.get("skValDaChiudere");
				if ((skValDaChiudere != null) && (skValDaChiudere.trim().equals("S")))
					chiudiSkValutaz(dbc, n_cartella, strDtSkVal, data_chiusura);
			}

			String selref = "SELECT * FROM skmedpal_referente WHERE " + "n_cartella=" + n_cartella + " AND " + "n_contatto=" + n_contatto;

			ISASCursor dbcur = dbc.startCursor(selref);
			if (!dbcur.next()) {				
				if (dbr.get("skm_medico") != null && !((String) dbr.get("skm_medico")).equals("")) {
					String infref = (String) dbr.get("skm_medico");
					String data_ref = "";

					if (dbr.get("skm_medico_da") != null)
						if (dbr.get("skm_medico_da") instanceof String) {
							data_ref = (String) dbr.get("skm_medico_da");
						} else if (dbr.get("skm_medico_da") instanceof java.sql.Date) {
							data_ref = ((java.sql.Date) dbr.get("skm_medico_da")).toString();
						}

					this.insertMedRef(dbc, infref, data_ref, data_apertura, n_cartella, n_contatto);
				}
			}

			// Jessy 26-06-05
			dbcur.close();

			// 07/12/06 m.
			leggiDiagnosi(dbc, dbr);

			// 17/09/07: per rimandare indietro al client la data della scheda
			// valutazione
			dbr.put("pr_data", strDtSkVal);

			// 15/07/09 m. ------------------
			// lettura dtConclusione CASO precedente
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String) h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, (String) h.get("pr_data"), "", dtChiusCasoPrec, "", tempoT, "52");
			// 15/07/09 m. ------------------
			int idCaso = -1;
			// 21/05/09 Elisa Croci
			
			
			//Simone 130117 se ho giÃ  gestito il caso aggiornando la data di presa in carico e chiudendo il contatto, escludo questa parte di codice
			if (!caso_gestito) {
				if (gestore_segnalazioni.isSegnalDaGestire(dbc,
						GestCasi.CASO_SAN)) {
					idCaso = prendi_dati_caso(dbc, dbr);
					if (idCaso != -1){
						gestione_segnalazione(dbc, dbr, h, "update");
					} else
						// 14/05/10
						idCaso = gestione_segnalazione(dbc, dbr, h, "insert");
					// aggiornaDataMinimaCaso(dbc, h, idCaso, dbr);
					printError("Fatta gestione segnalazione per il caso: "
							+ idCaso);
				} else {
					h.put("dt_segnalazione", data_apertura);
					h.put("dt_presa_carico", data_apertura);
					// da rfc115 v9 ï¿½ richiesta anche la data di valutazione
					h.put("valutazione_data", data_apertura);
					h.put("origine", "" + GestCasi.CASO_SAN);
					idCaso = gestore_casi.getIdCasoOrigine(dbc, h).intValue();
				}
				// 15/06/09 Elisa Croci
				// ***************************************************************
				if (data_chiusura != null && !data_chiusura.equals("")) {
					// 15/12/14 mv: richiesta ASL10 mail 02/12/14
					String confAbilChiuAll = leggiConf(dbc,
							KEYCONF_CHIUSURA_TUTTI_CONT);
					if ((confAbilChiuAll != null)
							&& (confAbilChiuAll.trim().equals("SI"))) {
						LOG.debug("Chiusura SkMedPal: abilitato a chiudere tutti i contatti");
						chiudiAll(dbc, n_cartella, data_chiusura,
								mylogin.getUser());
					} else {
						printError("Controllo contatto UNICO SANITARIO H == "
								+ h.toString());
						boolean unico = gestore_casi
								.query_checkUnicoContAperto(dbc, h, true, true);
						if (idCaso != -1 && unico) {
							printError("Gestisco la chiusura del caso");
							// E' uguale ad S quando c'e' la possibilita' che ci
							// siano piu' contatti e questo e'
							// l'ultimo contatto aperto che stiamo chiudendo!
							// Quindi
							// devo chiudere, se esiste, il caso
							// sociale associato!
							int origine = -1;
							if (dbr.get("origine") != null
									&& !(dbr.get("origine").toString())
									.equals(""))
								origine = Integer.parseInt(dbr.get("origine")
										.toString());
							else if (h.get("origine") != null
									&& !(h.get("origine").toString())
									.equals(""))
								origine = Integer.parseInt(h.get("origine")
										.toString());
							if (origine != -1) {
								printError("Origine del caso: " + origine);
								if (origine == GestCasi.CASO_SAN) {
									Hashtable hCaso = new Hashtable();
									hCaso.put("n_cartella", h.get("n_cartella"));
									hCaso.put("pr_data", h.get("pr_data"));
									hCaso.put("id_caso", new Integer(idCaso));
									hCaso.put("dt_conclusione",
											dbr.get("skm_data_chiusura"));
									// 26/03/10							hCaso.put("motivo", "99");
									// 26/03/10 ----
									String motChiu = (String) h
											.get("skm_motivo_chius");
									String motChiuFlux = getTabVociCodReg(dbc,
											"MCHIUS", motChiu);
									hCaso.put("motivo", motChiuFlux);
									// 26/03/10 ----
									hCaso.put("operZonaConf",
											(String) dbr.get("cod_operatore")); // 15/10/09
									printError(" -- update(): Chiudi caso = HashCaso: "
											+ hCaso.toString());
									Integer r = gestore_casi.chiudiCaso(dbc,
											hCaso);
									printError("Ritorno di ChiudiCaso == " + r);
								}
							}
						}
					}
				}
			}
			// ****************************************************************************************

			// Elisa 18/11/09 - medico di famiglia
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				if (h1.get("skm_mmg") != null && !((String) h1.get("skm_mmg")).equals("")) {
					// se e' stato scelto, lo decodifico
					String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "mecogn") + " "
							+ ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "menome");
					System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
					dbr.put("mecogn", desc_mmg);
				} else {
					// propongo il medico indicato su anagra_c
					String codmed = ISASUtil.getDecode(dbc, "anagra_c", "n_cartella", n_cartella, "cod_med");
					System.out.println(nomeEJB + " codmed == " + codmed);
					if (codmed != null && !codmed.equals("")) {
						String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "mecogn") + " "
								+ ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "menome");
						System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
						dbr.put("skm_mmg", codmed);
						dbr.put("mecogn", desc_mmg);
					}
				}
			}
			// ------------------------------------------------------------------------------

			// 15/06/09 Elisa Croci
			// ***************************************************************
			if (h.containsKey("ubicazione") && dbr != null)
				dbr.put("ubicazione", h.get("ubicazione"));
			if (h.containsKey("update_segnalazione") && dbr != null)
				dbr.put("update_segnalazione", h.get("update_segnalazione"));
			// *************************************************************************************

			String farmacoTer = ISASUtil.getValoreStringa(dbr, "skm_farmaco_ter");
			decodificaFarmacoTerapeutico(dbc, dbr, farmacoTer);

			// 05/02/13 
			dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));
			if (h.containsKey("ubicazione") && h.get("ubicazione").toString().equals(Integer.toString(GestCasi.UBI_RTOSC))) decodePatologie(dbc,dbr); 
			
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				System.out.println("SkMedPalEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo la rollback() - " + ce);
			}
			throw ce;
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
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception ex) {
				throw new SQLException("Errore eseguendo una rollback() - " + ex);
			}

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

	public Object deleteAll(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
//			String cartella = (String) dbr.get("n_cartella");
//			String contatto = (String) dbr.get("n_contatto");
			
			//Carlo Volpicelli - 30/12/2016 - le due righe sopra davano errore di cast, le ho riscritte come segue
			String cartella = Integer.toString((Integer) dbr.get("n_cartella"));
			String contatto = Integer.toString((Integer) dbr.get("n_contatto"));

			String ris = VerificaInterv(dbc, dbr);
			if (ris.equals("N")) {
				dbc.deleteRecord(dbr);
				deleteLegameProgetto(dbc, cartella, contatto, "52");// 31/10/06
				// m.:
				// '52'=oncologo

				// 17/09/07 deleteContsan(dbc,data,cartella,contatto); //
				// eliminata tabella CONTSAN
				deleteAllSchede(dbc, cartella, contatto, "skmedpal_referente", "skm_medico_da");// 03/01/07
				// m.
				deleteAllSchede(dbc, cartella, contatto, "skmpal_diaria", "skd_data");
				deleteAllSchede(dbc, cartella, contatto, "skmpal_relcli", "skr_data");
				deleteAllSchede(dbc, cartella, contatto, "skmpal_terapia", "skt_progr");
				//Carlo Volpicelli - 10/03/2017
				deleteAllSchede(dbc, cartella, contatto, "skmpal_terapie_new", "id_terapia");
				deleteRelazioniCliniche(dbc, cartella, contatto, "skmpal_relcli_new", "tipo_operatore", "progr_inse", "progr_modi"); //modificare il nome della tabella
				// 17/09/07 ---
				deleteAllSchede(dbc, cartella, contatto, "skmpal_metastasi", "sks_progr");
				deleteAllSchede(dbc, cartella, contatto, "skmpal_sintomi", "sks_progr");
				deletePianoAssist(dbc, cartella, contatto);
				deletePianoAccessi(dbc, cartella, contatto);
				deletePianoVerifiche(dbc, cartella, contatto);
				// 28/07/16 Cancellazione rfc115 solo se è l'ultimo contatto
				gestore_casi.cancellaPC(dbc,dbr);
				
				// 17/09/07 ---
			}

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;

			if (ris.equals("N"))
				return new Integer(0);
			else
				return new Integer(1);
		} catch (DBRecordChangedException e) {
			System.out.println("SkMedPalEJB.delete1(): " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("SkMedPalEJB.delete2(): " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e1) {
			System.out.println("SkMedPalfEJB.delete3(): " + e1);
			try {
				dbc.rollbackTransaction();
			} catch (Exception ex) {
				throw new SQLException("Errore eseguendo una rollback() - " + ex);
			}

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

	// 17/09/07
	private void deletePianoAssist(ISASConnection dbc, String cartella, String contatto) throws Exception {
		String myselect = "SELECT * FROM piano_assist" + " WHERE pa_tipo_oper = '52'" + " AND n_cartella = " + cartella
				+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0";
		printError("deletePianoAssist " + myselect);

		ISASCursor dbcur = dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_assist" + " WHERE pa_tipo_oper = '52'" + " AND n_cartella = " + cartella
					+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pa_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	// 17/09/07
	private void deletePianoAccessi(ISASConnection dbc, String cartella, String contatto) throws Exception {
		String myselect = "SELECT * FROM piano_accessi" + " WHERE pa_tipo_oper = '52'" + " AND n_cartella = " + cartella
				+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0";
		printError("deletePianoAccessi " + myselect);

		ISASCursor dbcur = dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_accessi" + " WHERE pa_tipo_oper = '52'" + " AND n_cartella = " + cartella
					+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pa_data"))) + " AND pi_prog = " + (Integer) dbr.get("pi_prog");
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	// 17/09/07
	private void deletePianoVerifiche(ISASConnection dbc, String cartella, String contatto) throws Exception {
		String myselect = "SELECT * FROM piano_verifica" + " WHERE pa_tipo_oper = '52'" + " AND n_cartella = " + cartella
				+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0";
		printError("deletePianoVerifiche " + myselect);
		ISASCursor dbcur = dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_verifica" + " WHERE pa_tipo_oper = '52'" + " AND n_cartella = " + cartella
					+ " AND n_progetto = " + contatto + " AND cod_obbiettivo = '00000000'" + " AND n_intervento = 0" + " AND pa_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pa_data"))) + " AND ve_data = "
					+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("ve_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}

	private void deleteLegameProgetto(ISASConnection dbc, String cartella, String contatto, String figprof)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASCursor dbcur = null;
		try {
			String myselect = "SELECT * FROM progetto_cont WHERE " + " n_cartella=" + cartella + " AND prc_n_contatto=" + contatto
					+ " AND prc_tipo_op='" + figprof + "'";
			debugMessage("deleteLegameProgetto=>" + myselect);
			dbcur = dbc.startCursor(myselect);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String sel = "SELECT * FROM progetto_cont WHERE " + " n_cartella=" + cartella + " AND prc_n_contatto=" + contatto
						+ " AND prc_tipo_op='" + figprof + "'" + " AND pr_data = "
						+ formatDate(dbc, ("" + (java.sql.Date) dbr.get("pr_data")));
				ISASRecord dbr2 = dbc.readRecord(sel);
				dbc.deleteRecord(dbr2);
			}
			dbcur.close();
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una deleteLegameProgetto() - " + e1);
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	private void deleteAllSchede(ISASConnection dbc, String cartella, String contatto, String tabella, String dato)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		try {
			String myselect = "SELECT * FROM " + tabella + " WHERE " + " n_cartella=" + cartella + " AND n_contatto=" + contatto;

			ISASCursor dbcur = dbc.startCursor(myselect);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String sel = "SELECT * FROM " + tabella + " WHERE " + " n_cartella=" + cartella + " AND n_contatto=" + contatto;
				Object tipo_dato = dbr.get(dato);
				if (tipo_dato instanceof Integer)
					sel += " AND " + dato + " = " + (Integer) dbr.get(dato);
				else if (tipo_dato instanceof java.sql.Date)
					sel += " AND " + dato + " = " + formatDate(dbc, ("" + (java.sql.Date) dbr.get(dato)));
				ISASRecord dbr2 = dbc.readRecord(sel);
				dbc.deleteRecord(dbr2);
			}
			dbcur.close();
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una deleteSchede() - " + e1);
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
	
	//Carlo Volpicelli - 10/03/2017
	private void deleteRelazioniCliniche(ISASConnection dbc, String cartella, String contatto, String tabella, String tipo_operatore, String progr_inse, String progr_modi)
			throws Exception {
		boolean done = false;
		try {
			String myselect = "SELECT d.n_cartella, d.n_contatto, d.tipo_operatore," +
					" d.progr_inse,d.progr_modi FROM " + tabella + " d WHERE " + " n_cartella=" + cartella + " AND n_contatto=" + contatto;

			ISASCursor dbcur = dbc.startCursor(myselect);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String sel = "SELECT d.n_cartella, d.n_contatto, d.tipo_operatore," +
					" d.progr_inse,d.progr_modi FROM " + tabella + " d WHERE " + " n_cartella=" + cartella + " AND n_contatto=" + contatto;
				String dato1 = dbr.get(tipo_operatore).toString();				
					sel += " AND " + tipo_operatore + " = '" + dato1+"'";				
				
				String dato2 = dbr.get(progr_inse).toString();				
					sel += " AND " + progr_inse + " = " +dato2;
					
				String dato3 = dbr.get(progr_modi).toString();					
				sel += " AND " + progr_modi + " = " +dato3;
				
				ISASRecord dbr2 = dbc.readRecord(sel);
				dbc.deleteRecord(dbr2);
			}
			dbcur.close();
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una deleteRelazioniCliniche() - " + e1);
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

	public ISASRecord insert_terapia(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		System.out.println(nomeEJB + " insert_terapia - H == " + h.toString());

		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		ISASConnection dbc = null;

		try {
			n_cartella = (String) h.get("n_cartella");
			n_contatto = (String) h.get("n_contatto");
		} catch (Exception e) {
			System.out.println("SkMedPal insert: Errore: manca la chiave primaria -- " + e);
			throw new SQLException("SkMedPal insert: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			String myselectMAX = "Select MAX(skt_progr) progr from skmpal_terapia " + " where n_cartella=" + n_cartella + " and "
					+ " n_contatto=" + n_contatto;
			ISASRecord dbr_0 = dbc.readRecord(myselectMAX);
			int progr = 0;
			if (dbr_0 != null) {
				Integer k = (Integer) dbr_0.get("progr");
				if (k != null && !k.equals("")) {
					progr = k.intValue();
					progr = progr + 1;
				} else
					progr = 1;
			}

			ISASRecord dbr = dbc.newRecord("skmpal_terapia");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			dbr.put("skt_progr", new Integer(progr));
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM skmpal_terapia WHERE n_cartella=" + n_cartella + " AND n_contatto=" + n_contatto
					+ " AND skt_progr=" + progr;
			dbr = dbc.readRecord(myselect);

			// Elisa 18/11/09
			if (dbr != null) {
				if (dbr.get("skt_farmaco") != null && !dbr.get("skt_farmaco").equals(""))
					dbr.put("skt_farmaco_desc", ISASUtil.getDecode(dbc, "farmaci", "sf_codice", "" + dbr.get("skt_farmaco"),
							"sf_descrizione"));

				if (dbr.get("skt_dosaggio") != null && !dbr.get("skt_dosaggio").equals(""))
					dbr.put("skt_dosaggio_desc", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "CPDOSAGG", ""
							+ dbr.get("skt_dosaggio"), "tab_descrizione"));
				decodificaCampi(dbc, dbr);
			}
			// -------------------------------------------------------------------------------------

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
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			try {
				dbc.rollbackTransaction();
			} catch (Exception ex) {
				throw new SQLException("Errore eseguendo una rollback() - " + ex);
			}
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

	public ISASRecord salva_terapia(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		System.out.println(nomeEJB + " salva_terapia - H ==  " + h.toString());
		boolean done = false;
		String n_contatto = null;
		String n_cartella = null;
		String progr = null;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			n_cartella = (String) h.get("n_cartella");
			n_contatto = (String) h.get("n_contatto");
			progr = (String) h.get("skt_progr");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}

		try {
			String sel = "SELECT * FROM skmpal_terapia WHERE " + " n_cartella=" + n_cartella + " and skt_progr=" + progr
					+ " and n_contatto=" + n_contatto;
			ISASRecord dbr = dbc.readRecord(sel);

			if (dbr != null) {
				Enumeration n = h.keys();
				while (n.hasMoreElements()) {
					String e = (String) n.nextElement();
					dbr.put(e, h.get(e));
				}

				dbc.writeRecord(dbr);
			}

			String myselect = "Select * FROM skmpal_terapia WHERE " + " n_cartella=" + n_cartella + " and skt_progr=" + progr
					+ " and n_contatto=" + n_contatto;

			dbr = dbc.readRecord(myselect);

			// Elisa 18/11/09
			if (dbr != null) {
				if (dbr.get("skt_farmaco") != null && !dbr.get("skt_farmaco").equals(""))
					dbr.put("skt_farmaco_desc", ISASUtil.getDecode(dbc, "farmaci", "sf_codice", "" + dbr.get("skt_farmaco"),
							"sf_descrizione"));

				if (dbr.get("skt_dosaggio") != null && !dbr.get("skt_dosaggio").equals(""))
					dbr.put("skt_dosaggio_desc", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "CPDOSAGG", ""
							+ dbr.get("skt_dosaggio"), "tab_descrizione"));
				decodificaCampi(dbc, dbr);
			}
			// ------------------------------------------------------------------------------------

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

	public void delete_terapia(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String sel_del = "SELECT * FROM skmpal_terapia WHERE" + " n_cartella=" + (String) h.get("n_cartella") + " AND n_contatto="
					+ (String) h.get("n_contatto") + " AND skt_progr='" + (String) h.get("skt_progr") + "'";
			ISASRecord dbr = dbc.readRecord(sel_del);
			if (dbr != null)
				dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
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

	public ISASRecord query_salvataggio(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		ISASConnection dbc = null;
		try {
			n_cartella = (String) h.get("n_cartella");
			n_contatto = (String) h.get("n_contatto");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkMedPal queryKey: Errore: manca la chiave primaria");
		}
		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select n_cartella,n_contatto from skmedpal where " + " n_cartella=" + n_cartella + " and n_contatto="
					+ n_contatto;
			printError("select query_salvataggio su skmedpal===" + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_salvataggio()  ");
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

	public ISASRecord query_salvataggio2(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select * from skmedpal where " + "n_cartella=" + (String) h.get("n_cartella") + " and " + "n_contatto="
					+ (String) h.get("n_contatto") + " and " + "skm_data_apertura=" + formatDate(dbc, (String) h.get("skm_data_apertura"));

			printError("select query_salvataggio su skmedpal===" + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_salvataggio()  ");
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

	// Elisa 30/11/09
	public ISASRecord query_mmg_anagra(myLogin mylogin, Hashtable h) throws SQLException {
		ISASConnection dbc = null;
		ISASRecord dbtxt = null;
		boolean done = false;

		String cartella = null;

		try {
			cartella = h.get("n_cartella").toString();
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE query_mmg_anagra - manca cartella - " + e);
			throw new SQLException(nomeEJB + " ERRORE query_mmg_anagra - manca cartella - " + e);
		}

		try {
			dbc = super.logIn(mylogin);

			String mysel = " SELECT cod_med FROM anagra_c WHERE n_cartella = " + cartella
					+ " AND data_variazione = (SELECT MAX(c.data_variazione) " + " FROM anagra_c c WHERE c.n_cartella = " + cartella + ")";

			System.out.println(nomeEJB + " query_mmg_anagra() - sel == " + mysel);
			dbtxt = dbc.readRecord(mysel);
			if (dbtxt != null && dbtxt.get("cod_med") != null && !dbtxt.get("cod_med").equals("")) {
				String codmed = dbtxt.get("cod_med").toString();
				if (codmed != null && !codmed.equals("")) {
					String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "mecogn") + " "
							+ ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "menome");
					System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
					dbtxt.put("skm_mmg", codmed);
					dbtxt.put("mecogn", desc_mmg);
				}

				dbtxt.put("n_cartella", cartella);
			} else {
				dbtxt = dbc.newRecord("anagra_c");
				dbtxt.put("n_cartella", cartella);
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbtxt;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE query_mmg_anagra() - " + e);
			throw new SQLException(nomeEJB + " ERRORE query_mmg_anagra() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e) {
					System.out.println(nomeEJB + " ERRORE query_mmg_anagra() - " + e);
				}
			}
		}
	}

	/***
	 * 17/09/07: piano assistenziale scorporato dal contatto public Vector
	 * query_progass(myLogin mylogin,Hashtable h) throws SQLException { boolean
	 * done=false; ISASConnection dbc=null; Vector vdbr=new Vector(); try{
	 * dbc=super.logIn(mylogin); /**** 31/10/06 m.: XORA non utilizzano il
	 * progetto assistenziale, casomai poi SOSTITUIRE medprogass con nuova
	 * tabella ***************** String
	 * myselect="Select * from medprogass where n_cartella="+
	 * (String)h.get("n_cartella")+" and n_contatto="+
	 * (String)h.get("n_contatto")+" ORDER BY mepa_data"; ISASCursor
	 * dbcur=dbc.startCursor(myselect); vdbr=dbcur.getAllRecord();
	 * dbcur.close();
	 * 
	 * dbc.close(); super.close(dbc); done=true; return vdbr; }catch(Exception
	 * e){ e.printStackTrace(); throw new
	 * SQLException("Errore eseguendo una query()  "); }finally{ if(!done){ try{
	 * dbc.close(); super.close(dbc); }catch(Exception
	 * e1){System.out.println(e1);} } } }
	 ********************************************************************/

	/***
	 * 22/11/06 m. private ISASRecord insertPato(ISASConnection dbc,Hashtable h)
	 * throws DBRecordChangedException, ISASPermissionDeniedException,
	 * SQLException { boolean done=false; String n_cartella=null; String
	 * n_contatto=null; try { n_cartella=(String)h.get("n_cartella");
	 * n_contatto=(String)h.get("n_contatto"); }catch (Exception e){
	 * e.printStackTrace(); throw new
	 * SQLException("SkMedPal insert: Errore: manca la chiave primaria"); } try{
	 * ISASRecord dbr=dbc.newRecord("skpatologie"); //INSERISCO NELLA TABELLA
	 * DELLE PATOLOGIE DEL MEDICO dbr.put("n_cartella",n_cartella);
	 * dbr.put("n_contatto",n_contatto);
	 * dbr.put("cod_operatore",h.get("cod_operatore"));
	 * dbr.put("skpat_patol1",h.get("skpat_patol1"));
	 * dbr.put("skpat_patol2",h.get("skpat_patol2"));
	 * dbr.put("skpat_patol3",h.get("skpat_patol3"));
	 * dbr.put("skpat_patol4",h.get("skpat_patol4"));
	 * dbr.put("skpat_conf_med",h.get("skpat_conf_med")); dbc.writeRecord(dbr);
	 * String myselect="Select * from skpatologie where n_cartella="+n_cartella+
	 * " and " + " n_contatto="+n_contatto; dbr=dbc.readRecord(myselect); return
	 * dbr; }catch(Exception e){ e.printStackTrace(); throw new
	 * SQLException("Errore eseguendo una insertPato() e() - "+ e);
	 * 
	 * } }
	 * 
	 * private ISASRecord updatePato(ISASConnection dbc,ISASRecord dbr) throws
	 * DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	 * ISASRecord dbausi=null; String n_cartella=null; String n_contatto=null;
	 * try { if (dbr.get("n_cartella") instanceof String)
	 * n_cartella=(String)dbr.get("n_cartella"); else if (dbr.get("n_cartella")
	 * instanceof Integer) n_cartella=""+(Integer)dbr.get("n_cartella"); if
	 * (dbr.get("n_contatto") instanceof String)
	 * n_contatto=(String)dbr.get("n_contatto"); else if (dbr.get("n_contatto")
	 * instanceof Integer) n_contatto=""+(Integer)dbr.get("n_contatto"); }catch
	 * (Exception e){ e.printStackTrace(); throw new
	 * SQLException("SkPato update: Errore: manca la chiave primaria"); } try{
	 * String myselect="Select * from skpatologie where "+
	 * " n_cartella="+n_cartella+ " and n_contatto="+n_contatto;
	 * dbausi=dbc.readRecord(myselect); if(dbausi!=null){
	 * dbausi.put("cod_operatore",dbr.get("cod_operatore"));
	 * dbausi.put("skpat_patol1",dbr.get("skpat_patol1"));
	 * dbausi.put("skpat_patol2",dbr.get("skpat_patol2"));
	 * dbausi.put("skpat_patol3",dbr.get("skpat_patol3"));
	 * dbausi.put("skpat_patol4",dbr.get("skpat_patol4"));
	 * dbausi.put("skpat_conf_med",dbr.get("skpat_conf_med")); }else{
	 * dbausi=dbc.newRecord("skpatologie"); dbausi.put("n_cartella",n_cartella);
	 * dbausi.put("n_contatto",n_contatto);
	 * dbausi.put("cod_operatore",dbr.get("cod_operatore"));
	 * dbausi.put("skpat_patol1",dbr.get("skpat_patol1"));
	 * dbausi.put("skpat_patol2",dbr.get("skpat_patol2"));
	 * dbausi.put("skpat_patol3",dbr.get("skpat_patol3"));
	 * dbausi.put("skpat_patol4",dbr.get("skpat_patol4"));
	 * dbausi.put("skpat_conf_med",dbr.get("skpat_conf_med")); }
	 * dbc.writeRecord(dbausi); return dbausi; }catch(Exception e){
	 * e.printStackTrace(); throw new
	 * SQLException("Errore eseguendo una UpdatePato() e() - "+ e);
	 * 
	 * } }
	 ***/

	public ISASRecord query_controlloData(myLogin mylogin, Hashtable h) throws SQLException {
		String ritorno = "";
		String ritorno_max = "";
		ISASConnection dbc = null;
		ISASRecord dbtxt = null;
		boolean done = false;
		try {
			dbc = super.logIn(mylogin);
			String mysel = "SELECT MIN (int_data_prest) data " + ", MAX (int_data_prest) data_max" + " FROM interv WHERE "
					+ " int_cartella =" + (String) h.get("n_cartella") + " AND int_contatto =" + (String) h.get("n_contatto")
					+ " AND int_tipo_oper='52'";// 31/10/06
			// m.:
			// '52'=oncologo
			// debugMessage("Dentro VerificaContatti=>"+mysel);
			dbtxt = dbc.readRecord(mysel);
			if (dbtxt != null)
				if (dbtxt.get("data") != null) {
					ritorno = "" + ((java.sql.Date) dbtxt.get("data"));
					if (dbtxt.get("data_max") != null)
						ritorno_max = "" + ((java.sql.Date) dbtxt.get("data_max"));
				} else
					ritorno = "N";
			else
				ritorno = "N";
			dbtxt.put("trova_interv", ritorno);
			dbtxt.put("trova_interv_max", ritorno_max);
			dbtxt.put("n_cartella", (String) h.get("n_cartella"));
			dbtxt.put("n_contatto", (String) h.get("n_contatto"));
			dbc.close();
			super.close(dbc);
			done = true;
			return dbtxt;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controlloData()  ");
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

	private String VerificaInterv(ISASConnection dbc, ISASRecord dbr) throws SQLException {
		String ritorno = "";
		try {
			String mysel = "SELECT * FROM interv WHERE " + " int_cartella =" + dbr.get("n_cartella") + " AND int_contatto ="
					+ dbr.get("n_contatto") + " AND int_tipo_oper='52'";// 31/10/06.:
			// '52'=oncologo

			ISASRecord dbtxt = dbc.readRecord(mysel);
			if (dbtxt != null)
				ritorno = "S";
			else
				ritorno = "N";
			return ritorno;
		} catch (Exception ex) {
			return ritorno = "";
		}
	}

	/***
	 * 22/11/06 m. public ISASRecord query_patomed(myLogin mylogin,Hashtable h)
	 * throws SQLException { boolean done=false; ISASConnection dbc=null; try{
	 * dbc=super.logIn(mylogin); String
	 * myselect="Select * from skpatologie where "+
	 * "n_cartella="+(String)h.get("n_cartella")+" and "+
	 * "n_contatto="+(String)h.get("n_contatto"); ISASRecord
	 * dbr=dbc.readRecord(myselect); if (dbr!= null) { Hashtable h1 =
	 * dbr.getHashtable(); if(h1.get("skpat_patol1")!=null &&
	 * !(h1.get("skpat_patol1").equals(""))){ dbr.put("des1",
	 * decodifica("icd9","cd_diag",h1.get("skpat_patol1"), "diagnosi",dbc));
	 * }else dbr.put("des1",""); if(h1.get("skpat_patol2")!=null &&
	 * !(h1.get("skpat_patol2").equals(""))){ dbr.put("des2",
	 * decodifica("icd9","cd_diag",h1.get("skpat_patol2"), "diagnosi",dbc));
	 * }else dbr.put("des2",""); if(h1.get("skpat_patol3")!=null &&
	 * !(h1.get("skpat_patol3").equals(""))){ dbr.put("des3",
	 * decodifica("icd9","cd_diag",h1.get("skpat_patol3"), "diagnosi",dbc));
	 * }else dbr.put("des3",""); if(h1.get("skpat_patol4")!=null &&
	 * !(h1.get("skpat_patol4").equals(""))){ dbr.put("des4",
	 * decodifica("icd9","cd_diag",h1.get("skpat_patol4"), "diagnosi",dbc));
	 * }else dbr.put("des4",""); } dbc.close(); super.close(dbc); done=true;
	 * return dbr; }catch(Exception e){ e.printStackTrace(); throw new
	 * SQLException("Errore eseguendo una query_patomed()  "); }finally{
	 * if(!done){ try{ dbc.close(); super.close(dbc); }catch(Exception
	 * e1){System.out.println(e1);} } } }
	 * 
	 * public ISASRecord insert_patomed(myLogin mylogin,Hashtable h) throws
	 * DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	 * boolean done=false; String n_cartella=null; String n_contatto=null;
	 * String data_apertura=null; ISASConnection dbc=null; try {
	 * n_cartella=(String)h.get("n_cartella");
	 * n_contatto=(String)h.get("n_contatto"); }catch (Exception e){
	 * e.printStackTrace(); throw new
	 * SQLException("SkInf insert_pato: Errore: manca la chiave primaria"); }
	 * try{ dbc=super.logIn(mylogin); ISASRecord
	 * dbr=dbc.newRecord("skpatologie"); //INSERISCO NELLA TABELLA DELLE
	 * PATOLOGIE DELL'infermiere o del fisioterapista Enumeration n=h.keys();
	 * while(n.hasMoreElements()){ String e=(String)n.nextElement();
	 * dbr.put(e,h.get(e)); } dbc.writeRecord(dbr); String
	 * myselect="Select * from skpatologie where n_cartella="+n_cartella+" and "
	 * + " n_contatto="+n_contatto; dbr=dbc.readRecord(myselect); dbc.close();
	 * super.close(dbc); done=true; return dbr; }catch(Exception e){
	 * e.printStackTrace(); throw new
	 * SQLException("Errore eseguendo una insert_patomed()  "); }finally{
	 * if(!done){ try{ dbc.close(); super.close(dbc); }catch(Exception
	 * e1){System.out.println(e1);} } } } public ISASRecord
	 * update_patomed(myLogin mylogin,Hashtable h) throws
	 * DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	 * boolean done=false; String n_contatto=null; String n_cartella=null;
	 * String data_apertura=null; String progr=null; ISASConnection dbc=null;
	 * try { dbc=super.logIn(mylogin); n_cartella=(String)h.get("n_cartella");
	 * n_contatto=(String)h.get("n_contatto"); }catch (Exception e){
	 * e.printStackTrace(); throw new
	 * SQLException("Errore: manca la chiave primaria"); } try{ String
	 * sel="SELECT * FROM skpatologie WHERE "+ " n_cartella="+n_cartella+
	 * " and n_contatto="+n_contatto; ISASRecord dbr=dbc.readRecord(sel); if
	 * (dbr!=null){ dbr.put("n_cartella",n_cartella);
	 * dbr.put("n_contatto",n_contatto);
	 * dbr.put("cod_operatore",h.get("cod_operatore"));
	 * dbr.put("skpat_patol1",h.get("skpat_patol1"));
	 * dbr.put("skpat_patol2",h.get("skpat_patol2"));
	 * dbr.put("skpat_patol3",h.get("skpat_patol3"));
	 * dbr.put("skpat_patol4",h.get("skpat_patol4"));
	 * dbr.put("skpat_conf_med",h.get("skpat_conf_med")); }
	 * dbc.writeRecord(dbr); String myselect="Select * FROM skpatologie WHERE "+
	 * " n_cartella="+n_cartella+ " and n_contatto="+n_contatto; ISASRecord
	 * dbter=dbc.readRecord(myselect); dbc.close(); super.close(dbc); done=true;
	 * return dbter; }catch(DBRecordChangedException e){ e.printStackTrace();
	 * throw e; }catch(ISASPermissionDeniedException e){ e.printStackTrace();
	 * throw e; }catch(Exception e1){ System.out.println(e1); throw new
	 * SQLException("Errore eseguendo una update_patomed() - "+ e1); }finally{
	 * if(!done){ try{ dbc.close(); super.close(dbc); }catch(Exception
	 * e2){System.out.println(e2);} } } }
	 ***/

	private String decodifica(String tabella, String nome_cod, Object val_codice, String descrizione, ISASConnection dbc) {
		if (val_codice == null)
			return "codice " + val_codice + " non trovato";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE " + nome_cod + " ='" + val_codice.toString()
					+ "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			return ((String) dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return "codice " + val_codice + " non trovato";
		}
	}

	// 03/11/06 m.: lettura dell date max x test e scale ----------------------
	private void leggiDateMaxTest(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		for (int k = 0; k < arrTabTest.length; k++) {
			String chiave = "dtmax_" + arrTabTest[k];
			mydbr.put(chiave, (String) leggiDataMax(mydbc, mydbr, arrDtTest[k], arrTabTest[k]));
		}
	}

	private String leggiDataMax(ISASConnection mydbc, ISASRecord mydbr, String nomeData, String nomeTab) throws Exception {
		String dataMax = null;
		String numCart = "" + mydbr.get("n_cartella");
		Object dtApertura = (Object) mydbr.get("skm_data_apertura");
		Object dtChiusura = (Object) mydbr.get("skm_data_chiusura");

		String critDtApert = "";
		if (dtApertura != null)
			critDtApert = " AND " + nomeData + " >= " + formatDate(mydbc, dtApertura.toString());

		String critDtChius = "";
		if (dtChiusura != null)
			critDtChius = " AND " + nomeData + " <= " + formatDate(mydbc, dtChiusura.toString());

		String myselect = "SELECT " + nomeData + " FROM " + nomeTab + " WHERE n_cartella = " + numCart + critDtChius + critDtApert
				+ " ORDER BY " + nomeData + " DESC";

		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			if (dbr.get(nomeData) != null)
				dataMax = "" + dbr.get(nomeData);

		return dataMax;
	}

	public ISASRecord query_dateMaxTest(myLogin mylogin, Hashtable h0) throws SQLException {
		boolean done = false;
		String cartella = (String) h0.get("n_cartella");
		String dtApertura = (String) h0.get("skm_data_apertura");
		String dtChiusura = (String) h0.get("skm_data_chiusura");
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);

			ISASRecord dbr = dbc.newRecord("skmedpal");
			dbr.put("n_cartella", (String) cartella);
			dbr.put("skm_data_apertura", (String) dtApertura);
			dbr.put("skm_data_chiusura", (String) dtChiusura);
			leggiDateMaxTest(dbc, dbr);

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_dateMaxTest()  ");
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

	// 03/11/06 m.: lettura dell date max x test e scale ----------------------

	// 22/11/06 m.: x DIAGNOSI
	// --------------------------------------------------
	private void leggiDiagnosi(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String punto = ver + "leggiDiagnosi ";
		Vector vdbr = new Vector();
		DiagnosiEJB diagnosiEJB = new DiagnosiEJB();
		Hashtable dati = mydbr.getHashtable();
		ISASCursor dbcur = null;
		try {
			diagnosiEJB.leggiDiagnosi_interno(mydbc, dbcur, vdbr, dati, false);
		} finally{
			close_dbcur_nothrow(punto, dbcur);
		}
		mydbr.put("diagn_associate", vdbr);
		
		
//		String cart = ((Integer) mydbr.get("n_cartella")).toString();
//		Object dtApertura = (Object) mydbr.get("skm_data_apertura");
//		Object dtChiusura = (Object) mydbr.get("skm_data_chiusura");
//
//		Vector vdbr = new Vector();
//
//		String critDtChius = "";
//		if (dtChiusura != null)
//			critDtChius = " AND data_diag <= " + formatDate(mydbc, dtChiusura.toString());
//
//		String myselect = "SELECT * FROM diagnosi" + " WHERE n_cartella = " + cart + critDtChius + " ORDER BY data_diag DESC";
//
//		ISASRecord recD = mydbc.readRecord(myselect);
//
//		if (recD != null) {
//			String dataIni = "";
//			if (dtApertura != null)
//				dataIni = dtApertura.toString();
//			String dtIni = dataIni.substring(0, 4) + dataIni.substring(5, 7) + dataIni.substring(8, 10);
//			decodificaDiagn(mydbc, recD);
//			decodificaOper(mydbc, recD);
//			boolean isDataInContesto = checkData(recD, dtIni);
//			costruisci5Rec(mydbc, recD, vdbr, (isDataInContesto ? "C" : "") + "0");
//		}
//
//		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	// Costruisce 5 record da quello letto: hanno tutti i campi del DB uguali,
	// più le colonne fittizie del
	// codice e della descrizione, ognuno con i valori corrispondenti(rec1 con
	// diag_1 e desc_diag_1, ecc).
	private void costruisci5Rec(ISASConnection mydbc, ISASRecord mydbr, Vector vett, String coloreCol) throws Exception {
		// aggiungo colonne fittizie al primo record
		mydbr.put("cod_alldiag", (String) mydbr.get("diag1"));
		mydbr.put("desc_alldiag", (String) mydbr.get("desc_diag1"));
		mydbr.put("progr", "1");
		mydbr.put("dt_diag", (java.sql.Date) mydbr.get("data_diag"));
		mydbr.put("clr_column", coloreCol);
		vett.addElement((ISASRecord) mydbr);

		// copio rec letto nei 4 nuovi record
		Hashtable h_1 = (Hashtable) mydbr.getHashtable();
		for (int j = 2; j < 6; j++) {
			ISASRecord dbr_i = mydbc.newRecord("diagnosi");
			copiaRec(h_1, dbr_i);

			// aggiungo colonne fittizie agli altri 4 record
			dbr_i.put("cod_alldiag", (String) dbr_i.get("diag" + j));
			dbr_i.put("desc_alldiag", (String) dbr_i.get("desc_diag" + j));
			dbr_i.put("progr", "" + j);
			dbr_i.put("dt_diag", "");
			vett.addElement((ISASRecord) dbr_i);
		}
	} // END costruisci6Rec

	private void copiaRec(Hashtable h_1, ISASRecord mydbr) throws Exception {
		Enumeration n_1 = h_1.keys();
		while (n_1.hasMoreElements()) {
			String e = (String) n_1.nextElement();
			mydbr.put(e, h_1.get(e));
		}
	} // END copiaRec

	// 11/12/06: restituisce true se la data diagnosi e' >= della dataInizio del
	// contesto
	private boolean checkData(ISASRecord mydbr, String dataI) throws Exception {
		DataWI dtDiag = new DataWI((java.sql.Date) mydbr.get("data_diag"));
		return dtDiag.isUguOSucc(dataI);
	} // END checkData

	// 22/11/06m.: x DIAGNOSI -------------------------------------------------

	// ============== Decodifiche ==========================

	private void decodificaDiagn(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		for (int k = 1; k < 6; k++) {
			String cod = (String) mydbr.get("diag" + k);
			String desc = ISASUtil.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi", cod, "diagnosi");
			mydbr.put("desc_diag" + k, desc);
		}
	}// END decodificaDiagn

	private void decodificaOper(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String cod = (String) mydbr.get("cod_operatore");
		String desc = ISASUtil.getDecode(mydbc, "operatori", "codice", cod, "nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nome_oper");
		mydbr.put("desc_oper", desc);
	}// END decodificaOper

	// 17/09/07 m.: ctrl esistenza contatti successivi ad una certa data
	public Boolean query_checkContSuccessivi(myLogin mylogin, Hashtable h0) {
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String) h0.get("n_cartella");
			String dtRiferimento = (String) h0.get("dataRif");

			String myselect = "SELECT * FROM skmedpal" + " WHERE n_cartella = " + cart + " AND skm_data_apertura > "
					+ formatDate(dbc, dtRiferimento);

			printError("SkMedPalEJB: query_checkContSuccessivi - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(risu));
		} catch (Exception e1) {
			System.out.println("SkMedPalEJB.query_checkContSuccessivi - Eccezione=[" + e1 + "]");
			return (Boolean) null;
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	// 12/10/07: chiusura skValutazione -> aggiornamento dataChiusura
	private void chiudiSkValutaz(ISASConnection mydbc, String numCart, String dtSkVal, String data_chiusura) throws Exception {
		String mysel = "SELECT p.* FROM progetto p" + " WHERE p.n_cartella = " + numCart + " AND p.pr_data = " + formatDate(mydbc, dtSkVal);

		printError("SkMedPalEJB -->> chiudiSkValutaz: mysel=[" + mysel + "]");
		ISASRecord mydbr = mydbc.readRecord(mysel);
		if (mydbr != null) {
			mydbr.put("pr_data_chiusura", data_chiusura);
			mydbc.writeRecord(mydbr);
		}
	}

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

	// 20/05/09 Elisa Croci
	/*
	 * 1) Il caso non esiste: creo il caso e la segnalazione 2) Il caso esiste
	 * ma e' chiuso: creo il caso e la segnalazione 3) Il caso e' attivo:
	 * aggiorno la segnalazione
	 */
	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov)
	throws Exception, CariException 
	{
		printError("SkMedPalEJB: gestione_segnalazione -- HASH: " + h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;
		String tpGestione = "";
		
		try {

			h.put("operZonaConf", (String) dbr.get("cod_operatore")); // 15/10/09

			if (h.get("id_caso") != null && !h.get("id_caso").equals("-1")) {
				// il caso esiste, prendo l'id e il suo stato
				stato_caso = Integer.parseInt(h.get("stato").toString());
				id_caso = Integer.parseInt(h.get("id_caso").toString());
			}

			// se sono in insert e il caso non esiste oppure e' concluso, devo crearne uno!
			if (prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU)) {
				// se il caso non esiste, non c'e' nemmeno la segnalazione, allora la creo!
				tpGestione = "INSERT segn e pc";

				h.put("tipo_caso", new Integer(GestCasi.CASO_SAN));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_SANITARIO));

				if (h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals(""))
					h.put("dt_segnalazione", h.get("skm_data_apertura"));

				if (h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
					h.put("dt_presa_carico", h.get("dt_segnalazione"));

				// da rfc115 v9 è richiesta anche la data di valutazione
				if (h.get("valutazione_data") == null || h.get("valutazione_data").equals(""))
					h.put("valutazione_data", h.get("dt_segnalazione"));
				
				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
				// arriva la data del progetto, cosi' me la copio dal dbr!
				h.put("pr_data", dbr.get("pr_data"));

				ISASRecord rec_segn = gestore_segnalazioni.insert(dbc, h);

				if (rec_segn != null) {
					Enumeration en = rec_segn.getHashtable().keys();
					while (en.hasMoreElements()) {
						String chiave = en.nextElement().toString();
						dbr.put(chiave, rec_segn.get(chiave));
					}

					ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
					if (rec_pc != null) {
						Enumeration en1 = rec_pc.getHashtable().keys();
						while (en1.hasMoreElements()) {
							String chiave = en1.nextElement().toString();
							dbr.put(chiave, rec_pc.get(chiave));
						}

						dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
					}

					return Integer.parseInt(rec_segn.get("id_caso").toString());
				} else
					return -1;			
			}
		// 29/03/10	else if(id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU &&  stato_caso != -1))
			else if (id_caso != -1 && (stato_caso != -1)) 
			{
				// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione e della presa in carico
				tpGestione = "UPDATE segn e pc";
				
				// da rfc115 v9 è richiesta anche la data di valutazione
				if (dbr.get("valutazione_data") == null || dbr.get("valutazione_data").equals(""))
					dbr.put("valutazione_data", dbr.get("dt_segnalazione"));
				
				Enumeration e = dbr.getHashtable().keys();
				while (e.hasMoreElements()) {
					String chiave = e.nextElement().toString();

					if (!h.containsKey(chiave))
						h.put(chiave, dbr.get(chiave));
				}

				printError(" gestione_segnalazione - UPDATE, H: " + h.toString());

				// 12/08/10 m ---------------
				if (dbr.get("origine") != null && !dbr.get("origine").equals("")) {
					int origine = Integer.parseInt(dbr.get("origine").toString());
					printError("gestione_segnalazione: Origine del caso " + id_caso + " =[" + origine + "]");

					//  aggiorno solo se il caso nel frattempo non è diventato UVM, altrimenti prtono comunicazioni
					//	di EVENTI non previste 
					if (origine == GestCasi.CASO_SAN) { // 12/08/10 m ---		
						ISASRecord new_segnalazione = gestore_segnalazioni.update(dbc, h);

						if (new_segnalazione != null) {
							Enumeration en = new_segnalazione.getHashtable().keys();
							while (en.hasMoreElements()) {
								String chiave = en.nextElement().toString();
								dbr.put(chiave, new_segnalazione.get(chiave));
							}
						}

						// 29/03/12: aggiunto cntrl su esistenza rec, dato che il CASO e la SEGNALAZIONE potrebbero essere stati inseriti da Sins_PUA 
						//	(e quindi necessita update) , ma la PRESACARICO potrebbe dover essere in insert.
						if (!esistePresaCar(dbc, h)) {
							if(h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
								h.put("dt_presa_carico", h.get("skm_data_apertura"));
							if (h.get("valutazione_data") == null || h.get("valutazione_data").equals(""))
								h.put("valutazione_data", h.get("skm_data_apertura"));
							ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
							if(rec_pc != null)
							{
								gestore_casi.presaCaricoCaso(dbc, h);
							
								Enumeration en1 = rec_pc.getHashtable().keys();
								while(en1.hasMoreElements())
								{
									String chiave = en1.nextElement().toString();
									dbr.put(chiave, rec_pc.get(chiave));
								}
							
								dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
							}
						} else {						
							ISASRecord update_presacarico = gestore_presacarico.update(dbc, h);
							if (update_presacarico != null) {
								Enumeration en = update_presacarico.getHashtable().keys();
								while (en.hasMoreElements()) {
									String chiave = en.nextElement().toString();
									dbr.put(chiave, update_presacarico.get(chiave));
								}

								dbr.put("cod_usl", dbr.get("reg_ero").toString() + dbr.get("asl_ero").toString());
							}
						}
					}
				}
			} 
			
			return id_caso;
		}catch(Exception e)	{
			e.printStackTrace();
			LOG.info("SkMedPalEJB.gestione_segnalazione() - "+tpGestione+": eccezione=[" + e + "]");
			throw e;
		}
	}// END gestione_segnalazione

	// 25/05/09 Elisa Croci
	private void prendi_presacarico(ISASConnection dbc, int caso, ISASRecord dbr) {
		try {
			if (caso != -1) {
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				h.put("ubicazione", dbr.get("ubicazione"));

				ISASRecord res = gestore_presacarico.queryKey(dbc, h);

				if (res != null) {
					Enumeration e = res.getHashtable().keys();
					while (e.hasMoreElements()) {
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}

					dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());
				}
			}
		} catch (ISASMisuseException e1) {
			System.out.println("SkMedPalEJB prendi_presacarico, ERRORE REPERIMENTO CHIAVE! -- " + e1);
		} catch (Exception e) {
			System.out.println("SkMedPalEJB prendi_presacarico, fallimento! -- " + e);
		}
	}// END prendi_presacarico

	// 20/05/09 Elisa Croci
	// prendo la segnalazione relativa al caso a cui il contatto deve fare
	// riferimento
	private boolean prendi_segnalazione(ISASConnection dbc, int caso, ISASRecord dbr) {
		try {
			/*
			 * prendo la segnalazione solo se il caso esiste e se sono in un
			 * contesto in cui si gestiscono le segnalazioni
			 */
			if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN) && caso != -1) {
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				ISASRecord res = gestore_segnalazioni.queryKey(dbc, h);

				if (res != null) {
					Enumeration e = res.getHashtable().keys();
					while (e.hasMoreElements()) {
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}
				}

				return true;
			} else
				return false;
		} catch (ISASMisuseException e1) {
			System.out.println("SkMedPalEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
			return false;
		} catch (Exception e) {
			System.out.println("SkMedPalEJB prendi_segnalazione, fallimento! -- " + e);
			return false;
		}
	}// END prendi_segnalazione

	// 20/05/09 Elisa Croci
	// dato un contatto, prendo il caso attivo se esiste altrimenti quello
	// chiuso piu' recente!
	private int prendi_dati_caso(ISASConnection dbc, ISASRecord dbr) {
		Hashtable h = new Hashtable();

		try {
			h.put("n_cartella", dbr.get("n_cartella"));
			h.put("pr_data", dbr.get("pr_data"));

			printError("SkMedPalEJB -- prendi dati caso: " + h.toString());

			ISASRecord rec = gestore_casi.getCasoRif(dbc, h);
			if (rec != null) {
				Enumeration e = rec.getHashtable().keys();
				while (e.hasMoreElements()) {
					String chiave = e.nextElement().toString();
					dbr.put(chiave, rec.get(chiave));
				}

				int caso = Integer.parseInt(dbr.get("id_caso").toString());
				return caso;
			} else
				return -1;
		} catch (ISASMisuseException e) {
			System.out.println("SkMedPalEJB prendi_dati_caso, manca chiave primaria! -- " + e);
			return -1;
		} catch (Exception e) {
			System.out.println("SkMedPalEJB prendi_dati_caso, fallimento! -- " + e);
			return -1;
		}
	}// END prendi_dati_caso

	// 15/07/09 m.: lettura dtConclusione CASO precedente
	private String getDtChiuCasoPrec(ISASConnection dbc, Hashtable h) throws Exception {
		String dtChiusPrec = "";
		// 11/06/10
		h.put("orig_caso_chiuso", new Integer(gestore_casi.CASO_UVM));

		ISASRecord lastCasoChiu = (ISASRecord) gestore_casi.getLastCasoChiuso(dbc, h);

		if ((lastCasoChiu != null) && (lastCasoChiu.get("dt_conclusione") != null))
			dtChiusPrec = "" + lastCasoChiu.get("dt_conclusione");
		printError("getDtChiuCasoPrec - dtChiusPrec=[" + dtChiusPrec + "]");

		return dtChiusPrec;
	}

	// 26/03/10
	private String getTabVociCodReg(ISASConnection dbc, String tbCod, String tbVal) throws Exception {
		String codReg = "99";
		String sel = "SELECT tab_codreg FROM tab_voci" + " WHERE tab_cod = '" + tbCod + "'" + " AND tab_val = '" + tbVal + "'";

		ISASRecord dbr1 = dbc.readRecord(sel);
		if ((dbr1 != null) && (dbr1.get("tab_codreg") != null))
			codReg = (String) dbr1.get("tab_codreg");
		return codReg;
	}

	// 29/03/12: cntrl esistenza rec PRESACARICO
	private boolean esistePresaCar(ISASConnection dbc, Hashtable h) throws Exception
	{
		ISASRecord recPC = (ISASRecord)gestore_presacarico.queryKey(dbc, h);
		return (recPC != null);
	}	
	
	// 05/02/13
	private String decodPresidio(ISASConnection dbc, String codPres, String codOper) throws Exception
	{
		String ret = "";
		Hashtable h_conf = eveUtl.leggiConf(dbc, codOper, new String[]{"codice_regione", "codice_usl"});
			
		StringBuffer strBufSel = new StringBuffer("SELECT * FROM presidi WHERE codreg = '");
		strBufSel.append((String)h_conf.get("codice_regione"));
		strBufSel.append("' AND codazsan = '");
		strBufSel.append((String)h_conf.get("codice_usl"));
		strBufSel.append("' AND codpres = '");
		strBufSel.append(codPres);
		strBufSel.append("'");
			
		ISASRecord dbr = dbc.readRecord(strBufSel.toString());
		if ((dbr != null) && (dbr.get("despres") != null))
			ret = ((String)dbr.get("despres")).trim();
		return ret;
	}		
	
	

	private void decodePatologie(ISASConnection dbc, ISASRecord rec_pc) throws ISASMisuseException, Exception {
		String ss1,ss2,patologia;
		ss1 = (String)rec_pc.get("ss1");
		ss2 = (String)rec_pc.get("ss2");
		patologia = (String)rec_pc.get("patologia");
		if (ss1!=null)
			rec_pc.put("ss1_desc", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", ss1 , "diagnosi"));
		if (ss2!=null)
			rec_pc.put("ss2_desc", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", ss2 , "diagnosi"));
		if (patologia!=null)
			rec_pc.put("patologia_desc", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", patologia , "diagnosi"));
		}

	
	// 13/10/14
	public ISASRecord getDatiLastSkMedPal(myLogin mylogin, Hashtable h) throws  Exception 
	{
		String nomeMetodo = "getDatiLastSkMedPal";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			LOG.info(nomeMetodo+" - HASH INPUT: h=["+h.toString()+"]");
			
			String cart = (String)h.get("n_cartella");
			String dtRif = (String)h.get("data_rif");
			String critDtRif = "";
			if ((dtRif != null) && (!dtRif.trim().equals("")))
				critDtRif = " AND b.skm_data_apertura <= " + formatDate(dbc, dtRif);
			
			
			String myselect = "SELECT a.* FROM skmedpal a"
					+ " WHERE a.n_cartella = " + cart
					+ " AND a.skm_data_apertura IN (SELECT MAX(b.skm_data_apertura)"
						+ " FROM skmedpal b"
						+ " WHERE b.n_cartella = a.n_cartella"
						+ critDtRif
						+ ")";
			
			LOG.debug(nomeMetodo+" - myselect: "+myselect);
			
			ISASRecord dbr = dbc.readRecord(myselect);
					
			LOG.debug(nomeMetodo+" -  Metodo eseguito x: cart=["+cart+"] - dtRif=["+dtRif+"]");			
			return dbr;			
		}catch(Exception e){
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(),e);
		}finally{
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	private Vector decodificaVectorISASRecord(ISASConnection dbc,Vector vdbr)throws Exception{
		String nomeMetodo = "decodificaVectorISASRecord";
		try{
			for (int i =0;i<vdbr.size();i++ ) {
				Object obj = vdbr.get(i);
				if(obj instanceof ISASRecord){
					ISASRecord dbr = (ISASRecord)vdbr.get(i);	
					dbr = (ISASRecord)vdbr.elementAt(i);
					dbr = decodificaISASRecord(dbc, dbr);
				}
			}
			
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT["+vdbr.size()+"] OUTPUT["+vdbr.size()+"]");
			return vdbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	private ISASRecord decodificaISASRecord(ISASConnection dbc,ISASRecord dbr)throws Exception{
		String nomeMetodo = "decodificaISASRecord";
		try{

			if(dbr!=null){	
				Hashtable h1 = dbr.getHashtable();
				
				//Costruisco cognome_nome
				String cognome = (String)ISASUtil.getObjectField(dbr,"cognome",'S');
				if(cognome==null || cognome.equals("null"))
					cognome="";
				String nome = (String)ISASUtil.getObjectField(dbr,"nome",'S');
				if(nome==null || nome.equals("null"))
					nome="";
				String cognome_nome = cognome+" "+nome;
				dbr.put("cognome_nome",cognome_nome);				

				//Referente
				if (h1.get("skm_medico") != null && !((String) h1.get("skm_medico")).equals("")) {
					String w_codice = (String) h1.get("skm_medico");
					String w_select = "SELECT * FROM operatori WHERE codice='" + w_codice + "'";
					ISASRecord w_dbr = dbc.readRecord(w_select);
					dbr.put("skm_medico_descr", w_dbr.get("cognome") + " " + w_dbr.get("nome"));
				} else
					dbr.put("skm_medico_descr", "");																			
			}

			return dbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	// 15/12/14 mv
	private void chiudiAll(ISASConnection dbc, String cart, String dtChiu, String oper) throws Exception
	{
		CartCntrlEtChiusure cCEC = new CartCntrlEtChiusure();
		String strMsgCheckDtCh = cCEC.checkDtChDaCartGTDtApeDtCh(dbc, cart, dtChiu, false);
		if(!strMsgCheckDtCh.equals(""))
    		throw new CariException(strMsgCheckDtCh, -2);
		
		cCEC.chiudoDaCartellaInGiuSoloSan(dbc, cart, dtChiu, TABVOCI_VAL_CHIUS_ALTRO, oper);
		System.out.println("SkMedPalEJB.chiudiAll():  chiamata a \"it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure\": OK");
	}
	private ISASRecord cercaProgetto(ISASConnection dbc, Hashtable h) throws Exception
	{
		if ((h.get("n_cartella") == null) || (h.get("data_apertura") == null))
			return (ISASRecord)null;
			
		String cart = h.get("n_cartella").toString();		
		String dtApe = h.get("data_apertura").toString();
		
		String sel = "SELECT * FROM progetto"
					+ " WHERE n_cartella = " + cart
					+ " AND pr_data IN (SELECT MAX(a.pr_data) FROM progetto a"
						+ " WHERE a.n_cartella = " + cart
						+ " AND a.pr_data <= " + formatDate(dbc, dtApe) 
						+ " AND ((a.pr_data_chiusura IS NULL)"
							+ " OR (a.pr_data_chiusura >= " + formatDate(dbc, dtApe) + "))"
						+ ")";
		
		return (ISASRecord)dbc.readRecord(sel);
	}
	
	private void printError(String msg) {
		if (mydebug)
			System.out.println("JCariPanelRivalutazione: " + msg);
	}

	public ISASRecord riapri(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
	CariException // 17/09/07
{
boolean done = false;
String n_cartella = null;
String n_contatto = null;
ISASConnection dbc = null;
String strDtSkVal = null;// 17/09/07

try {
	strDtSkVal = (String) dbr.get("pr_data");// 17/09/07
	n_cartella = (String) dbr.get("n_cartella");
	n_contatto = (String) dbr.get("n_contatto");
} catch (Exception e) {
	e.printStackTrace();
	throw new SQLException("SkMedPal update: Errore: manca la chiave primaria");
}

try {
	dbc = super.logIn(mylogin);
	dbc.startTransaction();

	// 31/10/14
	String data_chiusura = (String)dbr.get("skm_data_chiusura");
	
	// 07/08/07 *************************
	Hashtable h = dbr.getHashtable();
	if (dtApeContLEMaxDtContChius(dbc, h)) {
		String msg = "Attenzione: Data apertura nuovo contatto antecedente o uguale a data chiusura di ultimo contatto chiuso!";
		throw new CariException(msg, -2);
	}

	// 13/10/14
	HSPSkMedPalEJB hspSk = new HSPSkMedPalEJB();
	String data_apertura = dbr.get("skm_data_apertura").toString();
	if (!hspSk.checkDtApeNotInHSP(dbc, n_cartella, data_apertura, data_chiusura)) {
		String msg = "Attenzione: Data apertura contatto compresa in scheda Hospice!";
		throw new CariException(msg, -2);
	}
	
	
	
	// gb 12/09/07 *************************
	String strMsgCheckDatePianoAssist = checkDateContEDatePianoAssist(dbc, h);
	if (!strMsgCheckDatePianoAssist.equals(""))
		throw new CariException(strMsgCheckDatePianoAssist, -2);

	dbc.writeRecord(dbr);

	String myselect = "Select * from skmedpal where " + " n_cartella=" + n_cartella + " and n_contatto=" + n_contatto;

	dbr = dbc.readRecord(myselect);

	if (dbr.get("skm_data_chiusura") != null)
		data_chiusura = "" + (java.sql.Date) dbr.get("skm_data_chiusura");



	String selref = "SELECT * FROM skmedpal_referente WHERE " + "n_cartella=" + n_cartella + " AND " + "n_contatto=" + n_contatto;

	ISASCursor dbcur = dbc.startCursor(selref);
	if (!dbcur.next()) {
		if (dbr.get("skm_medico") != null && !((String) dbr.get("skm_medico")).equals("")) {
			String infref = (String) dbr.get("skm_medico");
			String data_ref = "";

			if (dbr.get("skm_medico_da") != null)
				if (dbr.get("skm_medico_da") instanceof String) {
					data_ref = (String) dbr.get("skm_medico_da");
				} else if (dbr.get("skm_medico_da") instanceof java.sql.Date) {
					data_ref = ((java.sql.Date) dbr.get("skm_medico_da")).toString();
				}

			this.insertMedRef(dbc, infref, data_ref, data_apertura, n_cartella, n_contatto);
		}
	}

	// Jessy 26-06-05
	dbcur.close();

	// 07/12/06 m.
	leggiDiagnosi(dbc, dbr);

	// 17/09/07: per rimandare indietro al client la data della scheda
	// valutazione
	dbr.put("pr_data", strDtSkVal);

	// 15/07/09 m. ------------------
	// lettura dtConclusione CASO precedente
	String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
	String tempoT = (String) h.get("tempo_t");

	// letture scale max
	gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, (String) h.get("pr_data"), "", dtChiusCasoPrec, "", tempoT, "52");
	// 15/07/09 m. ------------------
	int idCaso = -1;

	
	if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN)) {
		h.put("data_apertura",h.get("skm_data_apertura").toString());
		SkInfEJB.checkRiapriPC(dbc,h);
	}
	
	// Elisa 18/11/09 - medico di famiglia
	if (dbr != null) {
		Hashtable h1 = dbr.getHashtable();
		if (h1.get("skm_mmg") != null && !((String) h1.get("skm_mmg")).equals("")) {
			// se e' stato scelto, lo decodifico
			String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "mecogn") + " "
					+ ISASUtil.getDecode(dbc, "medici", "mecodi", "" + h1.get("skm_mmg"), "menome");
			System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
			dbr.put("mecogn", desc_mmg);
		} else {
			// propongo il medico indicato su anagra_c
			String codmed = ISASUtil.getDecode(dbc, "anagra_c", "n_cartella", n_cartella, "cod_med");
			System.out.println(nomeEJB + " codmed == " + codmed);
			if (codmed != null && !codmed.equals("")) {
				String desc_mmg = ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "mecogn") + " "
						+ ISASUtil.getDecode(dbc, "medici", "mecodi", codmed, "menome");
				System.out.println(nomeEJB + " desc_mmg == " + desc_mmg);
				dbr.put("skm_mmg", codmed);
				dbr.put("mecogn", desc_mmg);
			}
		}
	}
	// ------------------------------------------------------------------------------

	// 15/06/09 Elisa Croci
	// ***************************************************************
	if (h.containsKey("ubicazione") && dbr != null)
		dbr.put("ubicazione", h.get("ubicazione"));
	if (h.containsKey("update_segnalazione") && dbr != null)
		dbr.put("update_segnalazione", h.get("update_segnalazione"));
	// *************************************************************************************

	String farmacoTer = ISASUtil.getValoreStringa(dbr, "skm_farmaco_ter");
	decodificaFarmacoTerapeutico(dbc, dbr, farmacoTer);

	// 05/02/13 
	dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));				
	if (h.get("ubicazione").toString().equals(Integer.toString(GestCasi.UBI_RTOSC))) decodePatologie(dbc,dbr); 
	
	dbc.commitTransaction();
	dbc.close();
	super.close(dbc);
	done = true;

	return dbr;
} catch (CariException ce) {
	ce.setISASRecord(null);
	try {
		System.out.println("SkMedPalEJB.update() => ROLLBACK");
		dbc.rollbackTransaction();
	} catch (Exception e1) {
		throw new SQLException("Errore eseguendo la rollback() - " + ce);
	}
	throw ce;
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
		throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e1);
	}

	throw e;
} catch (Exception e1) {
	e1.printStackTrace();
	try {
		dbc.rollbackTransaction();
	} catch (Exception ex) {
		throw new SQLException("Errore eseguendo una rollback() - " + ex);
	}

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
	
	/* --------------------------------------------------------------------------------------- */
	
	
	//Carlo Volpicelli. Resituisce il numero di contatto più alto.
	public Integer getMaxNContatto(myLogin mylogin, String nCartella) throws Exception 
	{
		Integer risultato = 0;
		
		boolean done=false;
    	ISASConnection dbc=null;
    	ISASCursor dbcur = null;
    	
    	try
    	{
    		dbc=super.logIn(mylogin);
    		String myselect = "SELECT MAX(n_contatto) max_n_contatto" + " FROM skmedpal" + " WHERE n_cartella = " + nCartella;
    		ISASRecord dbr = dbc.readRecord(myselect);
    		if (dbr != null)
    			risultato = ISASUtil.getIntField(dbr, "max_n_contatto");    
            
            dbc.close();
            super.close(dbc);
            done=true;
            //return dbr;
    	}
    	catch(Exception e)
    	{
    		System.out.println(e);
    		throw new SQLException("Errore eseguendo getMaxNCartella() ");
    	}
    	finally{
    		if(!done){
    			try{
    				if (dbcur != null)
    					dbcur.close();
    				dbc.close();
    				super.close(dbc);
    			}catch(Exception e1){System.out.println(e1);}
    		}
    	}
	
		return risultato;
	}
	
	/* ----------------------------------------------------------------------------------------- */
		
		
	private String getDataFormatoOracle(String data)
	{
		String risultato = null;
		
		//String a = "2017-01-05";
		String dd = data.substring(8, 10);
		String meseNumerico = data.substring(5, 7);
		String meseLetterale = "GEN";
		String yyyy = data.substring(0, 4);
		
		if(meseNumerico.equals("02"))
		{
			meseLetterale = "FEB";
		}
		else if(meseNumerico.equals("03"))
		{
			meseLetterale = "MAR";
		}
		else if(meseNumerico.equals("04"))
		{
			meseLetterale = "APR";
		}
		else if(meseNumerico.equals("05"))
		{
			meseLetterale = "MAG";
		}
		else if(meseNumerico.equals("06"))
		{
			meseLetterale = "GIU";
		}
		else if(meseNumerico.equals("07"))
		{
			meseLetterale = "LUG";
		}
		else if(meseNumerico.equals("08"))
		{
			meseLetterale = "AGO";
		}
		else if(meseNumerico.equals("09"))
		{
			meseLetterale = "SET";
		}
		else if(meseNumerico.equals("10"))
		{
			meseLetterale = "OTT";
		}
		else if(meseNumerico.equals("11"))
		{
			meseLetterale = "NOV";
		}
		else if(meseNumerico.equals("12"))
		{
			meseLetterale = "DIC";
		}
		
		risultato = dd+"-"+meseLetterale+"-"+yyyy;
		return risultato;
	}

}

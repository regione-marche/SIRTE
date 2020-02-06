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

//import it.caribel.app.sinssnt.bean.classiPerSkMedPalEJB.ManagerCodifiche;
//import it.caribel.app.sinssnt.bean.classiPerSkMedPalEJB.ManagerRfc191;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure; 
import it.pisa.caribel.sinssnt.rfc191.ManagerCodifiche;
import it.pisa.caribel.sinssnt.rfc191.ManagerRfc191;
import it.pisa.caribel.util.*;
//import it.pisa.caribel.sinssnt.connection.*;
//import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
//import it.pisa.caribel.sinssnt.rfc191.ManagerCodifiche;
//import it.pisa.caribel.sinssnt.rfc191.ManagerRfc191;
import it.pisa.caribel.exception.*; // 17/09/07

public class HSPSkMedPalEJB extends SINSSNTConnectionEJB {
	private static String nomeEJB = "1-HSPSkMedPalEJB ";
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	
	ManagerRfc191 rfc = new ManagerRfc191();
	ManagerCodifiche mc = new ManagerCodifiche(); 
	ServerUtility su =new ServerUtility();

	// 17/09/07
	private String msgNoD = "Mancano i diritti per leggere il record";

	// 13/10/14
	private final String TABVOCI_VAL_CHIUS_CONTATTI = "20";
	// 31/10/14
	private final String TABVOCI_TPCONT_DAYHOSP = "1";
	// 17/12/14
	private final String KEYCONF_ABIL_HSP_IN_SINS = "ABIL_HSP_IN_SINS";
	
	
	public HSPSkMedPalEJB() {
	}

	private String[] arrTabTest = { "sc_braden", "sc_pfeiffer", "sc_kps", "sc_pap", "sc_tiq", "sc_nrs" };
	private String[] arrDtTest = { "skb_data", "data", "data", "data", "data", "data" };

	private boolean mydebug = true;
	
	private ISASRecord decodificaISASRecord(ISASConnection dbc,ISASRecord dbr,boolean aggiungiDettagliScheda)throws Exception{
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
				
				//Tipo contatto
				String tipo_contatto = (String)ISASUtil.getObjectField(dbr,"skm_tipo_contatto",'S');
				String tipo_contatto_descr = ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "HSPCONTA", tipo_contatto, "tab_descrizione");
				dbr.put("skm_tipo_contatto_descr",tipo_contatto_descr);
				

				if(aggiungiDettagliScheda){
					
					if (h1.get("cod_operatore") != null && !((String) h1.get("cod_operatore")).equals("")) {
						dbr.put("desc_operat", decodifica("operatori", "codice", h1.get("cod_operatore"),
								"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')", dbc));
					} else
						dbr.put("desc_operat", "");
					
					if (h1.get("skm_ope_acc") != null && !((String) h1.get("skm_ope_acc")).equals("")) {
						dbr.put("skm_ope_acc_descr", decodifica("operatori", "codice", h1.get("skm_ope_acc"),
								"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')", dbc));
					} else
						dbr.put("skm_ope_acc_descr", "");

					//Ospedale di dimissione
					if (h1.get("skm_osp_dim") != null && !((String) h1.get("skm_osp_dim")).equals("")) {
						dbr.put("des_osp", decodifica("ospedali", "codosp", h1.get("skm_osp_dim"), "descosp", dbc));
					} else
						dbr.put("des_osp", "");

					if (h1.get("skm_uo_dim") != null && !((String) h1.get("skm_uo_dim")).equals("")) {
						dbr.put("des_rep", decodifica("reparti", "cd_rep", h1.get("skm_uo_dim"), "reparto", dbc));
					} else
						dbr.put("des_rep", "");
					
					//Ospedale di consulenza
					if (h1.get("skm_osp_cons") != null && !((String) h1.get("skm_osp_cons")).equals("")) {
						dbr.put("des_osp_cons", decodifica("ospedali", "codosp", h1.get("skm_osp_cons"), "descosp", dbc));
					} else
						dbr.put("des_osp_cons", "");

					if (h1.get("skm_uo_cons") != null && !((String) h1.get("skm_uo_cons")).equals("")) {
						dbr.put("des_rep_cons", decodifica("reparti", "cd_rep", h1.get("skm_uo_cons"), "reparto", dbc));
					} else
						dbr.put("des_rep_cons", "");

					//Referente
					if (h1.get("skm_medico") != null && !((String) h1.get("skm_medico")).equals("")) {
						String w_codice = (String) h1.get("skm_medico");
						String w_select = "SELECT * FROM operatori WHERE codice='" + w_codice + "'";
						ISASRecord w_dbr = dbc.readRecord(w_select);
						dbr.put("desc_inf", w_dbr.get("cognome") + " " + w_dbr.get("nome"));
					} else
						dbr.put("desc_inf", "");
					
					// Elisa 18/11/09 - medico di famiglia
					String n_cartella = ((Integer)h1.get("n_cartella")).toString();
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
					
					if (h1.get("skm_strut_erog") != null && !((String) h1.get("skm_strut_erog")).equals(""))
						dbr.put("skm_strut_erog_descr", decodifica("presidi", "codpres", h1.get("skm_strut_erog"), "despres",dbc));
					else
						dbr.put("skm_strut_erog_descr", "");
										
					
					if (h1.get("skm_ss1") != null && !((String) h1.get("skm_ss1")).equals(""))
						dbr.put("skm_ss1_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_ss1"), "diagnosi",dbc));
					else
						dbr.put("skm_ss1_descr", "");
					
					if (h1.get("skm_ss2") != null && !((String) h1.get("skm_ss2")).equals(""))
						dbr.put("skm_ss2_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_ss2"), "diagnosi",dbc));
					else
						dbr.put("skm_ss2_descr", "");
					
					
					if (h1.get("skm_mnc1") != null && !((String) h1.get("skm_mnc1")).equals(""))
						dbr.put("skm_mnc1_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_mnc1"), "diagnosi",dbc));
					else
						dbr.put("skm_mnc1_descr", "");
					
					
					if (h1.get("skm_mnc2") != null && !((String) h1.get("skm_mnc2")).equals(""))
						dbr.put("skm_mnc2_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_mnc2"), "diagnosi",dbc));
					else
						dbr.put("skm_mnc2_descr", "");
					
					if (h1.get("skm_patologia_resp") != null && !((String) h1.get("skm_patologia_resp")).equals(""))
						dbr.put("skm_patologia_resp_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_patologia_resp"), "diagnosi",dbc));
					else
						dbr.put("skm_patologia_resp_descr", "");
					
					if (h1.get("skm_ssp1") != null && !((String) h1.get("skm_ssp1")).equals(""))
						dbr.put("skm_ssp1_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_ssp1"), "diagnosi",dbc));
					else
						dbr.put("skm_ssp1_descr", "");
					
					if (h1.get("skm_ssp2") != null && !((String) h1.get("skm_ssp2")).equals(""))
						dbr.put("skm_ssp2_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_ssp2"), "diagnosi",dbc));
					else
						dbr.put("skm_ssp2_descr", "");
					
					if (h1.get("skm_sss1") != null && !((String) h1.get("skm_sss1")).equals(""))
						dbr.put("skm_sss1_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_sss1"), "diagnosi",dbc));
					else
						dbr.put("skm_sss1_descr", "");
					
					if (h1.get("skm_sss2") != null && !((String) h1.get("skm_sss2")).equals(""))
						dbr.put("skm_sss2_descr", decodifica("tab_diagnosi", "cod_diagnosi", h1.get("skm_sss2"), "diagnosi",dbc));
					else
						dbr.put("skm_sss2_descr", "");
					
				}
			}
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT[dbr,"+aggiungiDettagliScheda+"]");
			return dbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
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
					dbr = decodificaISASRecord(dbc, dbr,false);
				}
			}
			
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT["+vdbr.size()+"] OUTPUT["+vdbr.size()+"]");
			return vdbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException, CariException {
		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;// 22/11/06 m.

		try {
			n_cartella = (String) h.get("n_cartella");
			n_contatto = (String) h.get("n_contatto");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("SkMedPal queryKey: Errore: manca la chiave primaria");
		}

		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT m.* FROM hsp_scheda m WHERE" + " m.n_cartella=" + n_cartella + " AND m.n_contatto=" + n_contatto;// +
			// 13-05-05 Commento voluto da Andrea
			// " AND m.skm_data_apertura="+formatDate(dbc,data_apertura);

			printError("select query_key su hsp_scheda===" + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				System.out.println(nomeEJB + " - queryKey() - " + h1.toString());

				//serratore
				dbr = decodificaISASRecord(dbc, dbr,true);

				// 22/11/06 m.
				leggiDiagnosi(dbc, dbr);

				String selg = "Select * from hsp_terapia where " + " n_cartella=" + (String) h.get("n_cartella") + " and n_contatto="
						+ (String) h.get("n_contatto") + " ORDER BY skt_progr";
				ISASCursor dbgriglia = dbc.startCursor(selg);
				Vector vdbg = dbgriglia.getAllRecord();
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
				dbr.put("griglia", vdbg);
				dbgriglia.close();

				System.out.println(" DBR RETURN == " + dbr.getHashtable().toString());
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e1) {
			System.out.println("HSPSkMedPalEJB.queryKey(): " + e1);
			throw new CariException(msgNoD, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
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
			// Ottengo la connessione al database
			dbc = super.logIn(mylogin);

			// Preparo la SELECT del record
			String myselect = "SELECT * FROM hsp_scheda" + " WHERE n_cartella = " + n_cartella + " AND skm_data_chiusura IS NULL";

			printError("HSPSkMedPalEJB/getContattoMedPalCorrente: " + myselect);
			dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				//serratore
				dbr = decodificaISASRecord(dbc, dbr,true);
				
				// 07/12/06 m.
				leggiDiagnosi(dbc, dbr);

				String selg = "Select * from hsp_terapia where " + " n_cartella=" + n_cartella + " and n_contatto="
						+ dbr.get("n_contatto") + " ORDER BY skt_progr";
				stampa(punto + "\n Query da eseguire>" + selg + "<\n");
				
				
				ISASCursor dbgriglia = dbc.startCursor(selg);
				Vector vdbg = dbgriglia.getAllRecord();
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
				dbr.put("griglia", vdbg);
				dbgriglia.close();

				dbr.put("pr_data", strDtSkVal);// 07/08/07
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch (ISASPermissionDeniedException e1) {
			System.out.println("HSPSkMedPalEJB.getContattoMedPalCorrente(): " + e1);
			throw new CariException(msgNoD, -2);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("HSPSkMedPalEJB.getContattoMedPalCorrente(): " + e);
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
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM hsp_scheda WHERE n_cartella=" + (String) h.get("n_cartella")
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
	
	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  Exception {
		String nomeMetodo = "queryPaginate";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			//Filtri per operatore loggato
			String codice_operatore 	= (String)h.get("codice_operatore");
			String tipo_operatore 		= (String)h.get("tipo_operatore");
			//Filtri di ricerca
			String n_contatto 		    = (String)h.get("n_contatto");
			String n_cartella 		    = (String)h.get("n_cartella");
			String skm_tipo_contatto 	= (String)h.get("skm_tipo_contatto");
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
			" FROM HSP_SCHEDA s, CARTELLA c "+
			" WHERE s.n_cartella = c.n_cartella ";
			
			if(n_contatto!=null && !n_contatto.equals(""))
				myselect+=" and s.n_contatto = "+n_contatto;
			
			if(n_cartella!=null && !n_cartella.equals(""))
				myselect+=" and s.n_cartella = "+n_cartella;
			
			if(skm_tipo_contatto!=null && !skm_tipo_contatto.equals(""))
				myselect+=" and s.skm_tipo_contatto = '"+skm_tipo_contatto+"'";
			
			if(apertura_inizio!=null && !apertura_inizio.equals(""))
				myselect += " and s.skm_data_apertura >= "+formatDate(dbc,apertura_inizio);
			if(apertura_fine!=null && !apertura_fine.equals(""))
				myselect += " and s.skm_data_apertura <= "+formatDate(dbc,apertura_fine);
			
			if(chiusura_inizio!=null && !chiusura_inizio.equals(""))
				myselect += " and s.skm_data_chiusura >= "+formatDate(dbc,chiusura_inizio);
			if(chiusura_fine!=null && !chiusura_fine.equals(""))
				myselect += " and s.skm_data_chiusura <= "+formatDate(dbc,chiusura_fine);
			
			//Cognome
			if(cognome!=null && !cognome.equals("")){
				cognome=su.duplicateChar(cognome,"'");
				myselect += "AND c.cognome like '%"+cognome+"%'";

			}
			//Nome
			if(nome!=null && !nome.equals("")){
				nome=su.duplicateChar(nome,"'");
				myselect += "AND c.nome like '%"+nome+"%'";
			}
			
			
			//ORDINAMENTO
			String ordine_modo = " desc ";//DEFAULT
			String ordine_campo = " u.cognome "+ordine_modo+", u.nome "+ordine_modo;//DEFAULT
			if(tipo_ordine!=null && !tipo_ordine.equals("")){
				ordine_modo = tipo_ordine;
			}
			if(ordina_per!=null && !ordina_per.equals("")){
				if(ordina_per.equals("COGN"))
					ordine_campo = " c.cognome "+ordine_modo+", c.nome ";
				else if(ordina_per.equals("DTAPER"))
					ordine_campo = " s.skm_data_apertura ";
				else if(ordina_per.equals("DTCHIUS"))
					ordine_campo = " s.skm_data_chiusura ";
				else if(ordina_per.equals("TIPOCONT"))
					ordine_campo = " s.skm_tipo_contatto ";
			}
			myselect += " ORDER BY "+ordine_campo+ ordine_modo;
			

			LOG.trace(nomeMetodo+" - myselect: "+myselect);

			//ISASCursor dbcur=dbc.startCursor(myselect,200);//serratore per risolvere segnalazione 58909
			ISASCursor dbcur=dbc.startCursor(myselect);
			//Vector vdbr=dbcur.getAllRecord();
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);

			vdbr = decodificaVectorISASRecord(dbc, vdbr);
			
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT["+h.get("cognome")+", "+h.get("nome")+", "+h.get("id_scheda")+", "+h.get("n_cartella")+"...]");
			
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

		Vector vdbr = new Vector();

		try {
			// Connessione al database
			dbc = super.logIn(mylogin);

			// Compongo la SELECT			
			String myselect = "SELECT skm.*" + " FROM hsp_scheda skm"+
					" WHERE skm.n_cartella = " + strNAssistito+
					 " AND skm.skm_data_chiusura IS NOT NULL"+
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
			String myselect = "Select * from hsp_terapia where " + " n_cartella=" + (String) h.get("n_cartella") + " and n_contatto="
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

			// 07/08/07 *************************
			if (dtApeContLEMaxDtContChius(dbc, h)) {
				String msg = "Attenzione: Data apertura nuovo contatto antecedente o uguale a data chiusura di ultimo contatto chiuso!";
				throw new CariException(msg, -2);
			}

			// 07/08/07: si ottiene il nuovo progressivo (non si usa pi�
			// CONTSAN).
			int intProgressivo = getProgressivo(dbc, n_cartella);
			Integer iProgressivo = new Integer(intProgressivo);
			n_contatto = iProgressivo.toString();

			ISASRecord dbr = dbc.newRecord("hsp_scheda");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			// 07/08/07: Si setta il campo 'n_contatto' col nuovo progressivo
			dbr.put("n_contatto", iProgressivo);
			
			//serratore
			dbr.put("skm_id_percorso", rfc.newIdPercorsoPerRfc191(dbc,dbr));
			
			gestistiProgrAnnualePerPresidio(dbc,dbr);

			dbc.writeRecord(dbr);

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
			}


			String myselect = "SELECT * FROM hsp_scheda WHERE n_cartella=" + n_cartella + " AND n_contatto=" + n_contatto
					+ " AND skm_data_apertura=" + formatDate(dbc, data_apertura);

			dbr = dbc.readRecord(myselect);


			String selref = "SELECT * FROM hsp_referente WHERE " + "n_cartella=" + n_cartella + " AND " + "n_contatto=" + n_contatto;
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

			// 22/11/06 m.
			leggiDiagnosi(dbc, dbr);

			// 07/08/07
			dbr.put("pr_data", strDtSkVal);

			/*serratore
			// 15/07/09 m. ------------------
			// lettura dtConclusione CASO precedente
			h.put("pr_data", strDtSkVal);
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String) h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, strDtSkVal, "", dtChiusCasoPrec, "", tempoT, "52");
			dbr.put("tempo_t", tempoT);
			serratore*/
			
			//serratore
			dbr = decodificaISASRecord(dbc, dbr,true);
			
			// 13/10/14 mv: chiusura di tutti i contatti 
			chiudiAll(dbc, n_cartella, data_apertura, mylogin.getUser(), (String)dbr.get("skm_tipo_contatto"));
			
			//serratore - Gestione eventi per RFC191
			rfc.gestisciPresaCarico(dbc, dbr);			
			
			printError("HSPSkMedPalEJB: insert -- DBR restituito === " + dbr.getHashtable().toString());

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
				System.out.println("HSPSkMedPalEJB.insert() => ROLLBACK");
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

	// 17/09/07 m.: aggiunto crit su contatto diverso da quello in oggetto.
	private boolean dtApeContLEMaxDtContChius(ISASConnection dbc, Hashtable h) throws Exception {
		String strNCartella = (String) h.get("n_cartella");
		String strDataApeContatto = (String) h.get("skm_data_apertura");
		String strNContatto = (String) h.get("n_contatto"); // 13/09/07 m.

		String mySel = "SELECT skm_data_chiusura" + " FROM hsp_scheda" + " WHERE n_cartella = " + strNCartella
				+ (strNContatto != null ? " AND n_contatto <> " + strNContatto : "") + // 13/09/07 m.
				" AND skm_data_chiusura >= " + formatDate(dbc, strDataApeContatto) + " AND skm_data_chiusura IS NOT NULL";

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() > 0))
			return true;
		else
			return false;
	}

	// 17/09/07: ricava il nuovo progressivo per le operazioni di 'insert'.
	private int getProgressivo(ISASConnection mydbc, String strNAssistito) throws Exception {
		int intProgressivo = 0;

		String myselect = "SELECT MAX(n_contatto) max_n_contatto" + " FROM hsp_scheda" + " WHERE n_cartella = " + strNAssistito;
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
			intProgressivo = ISASUtil.getIntField(dbr, "max_n_contatto");

		intProgressivo++;
		return intProgressivo;
	}
	
	private int getProgrAnnualePerPresidio(ISASConnection mydbc,String anno, String strCodPresidio) throws Exception {
		int intProgressivo = 0;
		String myselect = "SELECT MAX(skm_pres_progr) max_skm_pres_progr" +
		" FROM hsp_scheda" + 
		" WHERE skm_strut_erog = '" + strCodPresidio+"'"+
		" AND skm_pres_anno = '"+anno+"'";
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null && dbr.get("max_skm_pres_progr")!=null ){
			String max_skm_pres_progr = (String)dbr.get("max_skm_pres_progr");
			if(max_skm_pres_progr!=null && !max_skm_pres_progr.equals(""))
				intProgressivo = Integer.parseInt((String)dbr.get("max_skm_pres_progr"));
		}	
		intProgressivo++;
		return intProgressivo;
	}
	
	private void gestistiProgrAnnualePerPresidio(ISASConnection dbc,ISASRecord dbr) {
		String nomeMetodo = "gestistiProgrAnnualePerPresidio";
		try{
			String skm_strut_erog = (String)dbr.get("skm_strut_erog");
			String skm_anno =mc.getAnnoFromStrSqlDate(((Object)ISASUtil.getObjectField(dbr,"skm_presacarico_data",'O')).toString());
			if(skm_strut_erog!=null && !skm_strut_erog.equals("") && !skm_anno.equals("")){
				//Solo se non � stato gia' valorizzato allora calcolo il progressivo
				if(dbr.get("skm_pres_progr")==null || ((String)dbr.get("skm_pres_progr")).equals("") ){
					dbr.put("skm_pres_anno", skm_anno);
					dbr.put("skm_pres_progr", ""+getProgrAnnualePerPresidio(dbc, skm_anno, skm_strut_erog));
				}
			}
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Exception: "+e);
		}
	}

	private ISASRecord insertMedRef(ISASConnection dbc, String infref, String data_ref, String data_apertura, String n_cartella,
			String n_contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		try {
			ISASRecord dbref = dbc.newRecord("hsp_referente");
			dbref.put("n_cartella", n_cartella);
			dbref.put("n_contatto", n_contatto);
			dbref.put("skm_medico", infref);
			dbref.put("skm_medico_da", data_ref);
			dbc.writeRecord(dbref);

			return dbref;
		} catch (DBRecordChangedException e) {
			System.out.println("HSPSkMedPalEJB.insertMedPalRef(): " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("HSPSkMedPalEJB.insertMedPalRef(): " + e);
			throw e;
		} catch (Exception e1) {
			System.out.println("HSPSkMedPalEJB.insertMedPalRef(): " + e1);
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

			// 07/08/07 *************************
			Hashtable h = dbr.getHashtable();
			if (dtApeContLEMaxDtContChius(dbc, h)) {
				String msg = "Attenzione: Data apertura nuovo contatto antecedente o uguale a data chiusura di ultimo contatto chiuso!";
				throw new CariException(msg, -2);
			}
			
			//serratore
			dbr.put("skm_id_percorso", rfc.newIdPercorsoPerRfc191(dbc,dbr));
			
			gestistiProgrAnnualePerPresidio(dbc,dbr);

			dbc.writeRecord(dbr);

			String myselect = "Select * from hsp_scheda where " + " n_cartella=" + n_cartella + " and n_contatto=" + n_contatto;

			dbr = dbc.readRecord(myselect);

			String data_apertura = "" + (java.sql.Date) dbr.get("skm_data_apertura");


			String selref = "SELECT * FROM hsp_referente WHERE " + "n_cartella=" + n_cartella + " AND " + "n_contatto=" + n_contatto;

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

			/*serratore
			// 15/07/09 m. ------------------
			// lettura dtConclusione CASO precedente
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String) h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, (String) h.get("pr_data"), "", dtChiusCasoPrec, "", tempoT, "52");
			serratore*/

			//serratore
			dbr = decodificaISASRecord(dbc, dbr,true);
			
			
			//serratore: Gestione eventi per RFC191
			ManagerRfc191 rfc = new ManagerRfc191();			
			String skm_data_chiusura = ((Object)ISASUtil.getObjectField(dbr,"skm_data_chiusura",'O')).toString();
			if(skm_data_chiusura==null || skm_data_chiusura.equals(""))
				rfc.gestisciPresaCarico(dbc, dbr);
			else 
				rfc.gestisciConclusione(dbc, dbr);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				System.out.println("HSPSkMedPalEJB.update() => ROLLBACK");
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
			System.out.println(e1);
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
			
			String cartella = (String) dbr.get("n_cartella");
			String contatto = (String) dbr.get("n_contatto");

			String ris = VerificaInterv(dbc, dbr);
			if (ris.equals("N")) {
				
				//############ INIZIO TRANSAZIONE ########################
				dbc.startTransaction();
				
				rfc.gestisciCancellazione(dbc, dbr);
				
				dbc.deleteRecord(dbr);
				//deleteAllSchede(dbc, cartella, contatto, "diagnosi");
				deleteAllSchede(dbc, cartella, contatto, "hsp_diaria");
				deleteAllSchede(dbc, cartella, contatto, "hsp_fam");
				deleteAllSchede(dbc, cartella, contatto, "hsp_metastasi");
				deleteAllSchede(dbc, cartella, contatto, "hsp_portatore");
				deleteAllSchede(dbc, cartella, contatto, "hsp_referente");
				deleteAllSchede(dbc, cartella, contatto, "hsp_relcli");
				deleteAllSchede(dbc, cartella, contatto, "hsp_ricoveri");
				deleteAllSchede(dbc, cartella, contatto, "hsp_sintomi");
				deleteAllSchede(dbc, cartella, contatto, "hsp_terapia");
				
				dbc.commitTransaction();
				//############ FINE TRANSAZIONE ########################
			}

			dbc.close();
			super.close(dbc);
			done = true;

			if (ris.equals("N"))
				return new Integer(0);
			else
				return new Integer(1);
		} catch (DBRecordChangedException e) {
			System.out.println("HSPSkMedPalEJB.delete1(): " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println("HSPSkMedPalEJB.delete2(): " + e);
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

	

	private void deleteAllSchede(ISASConnection dbc, String cartella, String contatto, String tabella)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		try {
			String myselect = "SELECT * FROM " + tabella + " WHERE " + " n_cartella=" + cartella + " AND n_contatto=" + contatto;
			
			ISASCursor dbcur=dbc.startCursor(myselect);
			while(dbcur.next()) {
				ISASRecord dbCorr=dbc.readRecord(myselect);
				dbc.deleteRecord(dbCorr);
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
			String myselectMAX = "Select MAX(skt_progr) progr from hsp_terapia " + " where n_cartella=" + n_cartella + " and "
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

			ISASRecord dbr = dbc.newRecord("hsp_terapia");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			dbr.put("skt_progr", new Integer(progr));
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM hsp_terapia WHERE n_cartella=" + n_cartella + " AND n_contatto=" + n_contatto
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
			String sel = "SELECT * FROM hsp_terapia WHERE " + " n_cartella=" + n_cartella + " and skt_progr=" + progr
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

			String myselect = "Select * FROM hsp_terapia WHERE " + " n_cartella=" + n_cartella + " and skt_progr=" + progr
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
			String sel_del = "SELECT * FROM hsp_terapia WHERE" + " n_cartella=" + (String) h.get("n_cartella") + " AND n_contatto="
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
			String myselect = "Select n_cartella,n_contatto from hsp_scheda where " + " n_cartella=" + n_cartella + " and n_contatto="
					+ n_contatto;
			printError("select query_salvataggio su hsp_scheda===" + myselect);
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
			String myselect = "Select * from hsp_scheda where " + "n_cartella=" + (String) h.get("n_cartella") + " and " + "n_contatto="
					+ (String) h.get("n_contatto") + " and " + "skm_data_apertura=" + formatDate(dbc, (String) h.get("skm_data_apertura"));

			printError("select query_salvataggio su hsp_scheda===" + myselect);
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

	public ISASRecord query_controlloData(myLogin mylogin, Hashtable h) throws SQLException {
		String ritorno = "";
		String ritorno_max = "";
		ISASConnection dbc = null;
		ISASRecord dbtxt = null;
		boolean done = false;
		try {
			dbc = super.logIn(mylogin);
			String mysel = "SELECT MIN (int_data_prest) data " + ", MAX (int_data_prest) data_max" + " FROM hsp_interv WHERE "
					+ " int_cartella =" + (String) h.get("n_cartella") + " AND int_contatto =" + (String) h.get("n_contatto");
					//serratore + " AND int_tipo_oper='52'";// 31/10/06
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
			String mysel = "SELECT * FROM hsp_interv WHERE " + " int_cartella =" + dbr.get("n_cartella") + " AND int_contatto ="
					+ dbr.get("n_contatto"); //serratore + " AND int_tipo_oper='52'";// 31/10/06.:
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

			ISASRecord dbr = dbc.newRecord("hsp_scheda");
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
		String cart = ((Integer) mydbr.get("n_cartella")).toString();
		Object dtApertura = (Object) mydbr.get("skm_data_apertura");
		Object dtChiusura = (Object) mydbr.get("skm_data_chiusura");

		Vector vdbr = new Vector();

		String critDtChius = "";
		if (dtChiusura != null)
			critDtChius = " AND data_diag <= " + formatDate(mydbc, dtChiusura.toString());

		String myselect = "SELECT * FROM diagnosi" + " WHERE n_cartella = " + cart + critDtChius + " ORDER BY data_diag DESC";

		ISASRecord recD = mydbc.readRecord(myselect);

		if (recD != null) {
			String dataIni = "";
			if (dtApertura != null)
				dataIni = dtApertura.toString();
			String dtIni = dataIni.substring(0, 4) + dataIni.substring(5, 7) + dataIni.substring(8, 10);
			decodificaDiagn(mydbc, recD);
			decodificaOper(mydbc, recD);
			boolean isDataInContesto = checkData(recD, dtIni);
			costruisci5Rec(mydbc, recD, vdbr, (isDataInContesto ? "C" : "") + "0");
		}

		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	// Costruisce 5 record da quello letto: hanno tutti i campi del DB uguali,
	// pi� le colonne fittizie del
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

			String myselect = "SELECT * FROM hsp_scheda" 
					+ " WHERE n_cartella = " + cart 
					+ " AND skm_data_apertura > " + formatDate(dbc, dtRiferimento);

			printError("HSPSkMedPalEJB: query_checkContSuccessivi - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done = true;

			return (new Boolean(risu));
		} catch (Exception e1) {
			System.out.println("HSPSkMedPalEJB.query_checkContSuccessivi - Eccezione=[" + e1 + "]");
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
	
	public Boolean checkEsisteSkHSPAperta(myLogin mylogin, Hashtable h0) throws Exception
	{
		String nomeMetodo = "checkEsisteSkHSPAperta";		
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			
			return (Boolean)checkEsisteSkHSPApertaDBC(dbc, h0);
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(), e);
		}finally{
			logout_nothrow(nomeMetodo, dbc);
		}
	}	
	
	// 17/02/15
	public Boolean checkEsisteSkHSPApertaDBC(ISASConnection dbc, Hashtable h0) throws Exception
	{
		String nomeMetodo = "checkEsisteSkHSPApertaDBC";		
		ISASCursor dbcur = null;
		boolean risu = false;
		
		try {
			// 17/12/14 mv
			boolean confAbilHSP = leggiAbilConf(dbc, KEYCONF_ABIL_HSP_IN_SINS);
			if (!confAbilHSP)
				return new Boolean(false);
			

			String cart = (String)h0.get("n_cartella");
			// 15/12/14
			String critNoDayHosp = "";
			String noDayHosp = (String)h0.get("noDayHosp");
			if ((noDayHosp != null) && (noDayHosp.trim().equals("S")))
				critNoDayHosp = " AND skm_tipo_contatto <> '" + TABVOCI_TPCONT_DAYHOSP + "'";

			String myselect = "SELECT * FROM hsp_scheda" 
					+ " WHERE n_cartella = " + cart 
					+ " AND skm_data_chiusura IS NULL"
					+ critNoDayHosp;

			LOG.debug(nomeMetodo+"- myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			return (new Boolean(risu));
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
		}
	}
	
	// 13/10/14 mv
	// 17/02/15 mv: chiusura dei soli contatti sanitari e casi (no parte SOC)
	private void chiudiAll(ISASConnection dbc, String cart, String dtApe, String oper, String tpCont) throws Exception
	{
		// 31/10/14: se DayHospital: non si chiude niente
		if (TABVOCI_TPCONT_DAYHOSP.equals(tpCont))
			return;
		
		
/** 31/10/14: si permette chiusura alla stessa data dell'apertura HSP
		// si calcola dtChiusura come (dtAperturaSkHSP - 1)		
		String dtChiu = getDtMenoUno(dtApe);
**/		
		CartCntrlEtChiusure cCEC = new CartCntrlEtChiusure();
		String strMsgCheckDtCh = cCEC.checkDtChDaCartGTDtApeDtChSoloSan(dbc, cart, dtApe);
		if(!strMsgCheckDtCh.equals(""))
    		throw new CariException(strMsgCheckDtCh, -2);
		
		cCEC.chiudoDaCartellaInGiuSoloSan(dbc, cart, dtApe, TABVOCI_VAL_CHIUS_CONTATTI, oper);
		System.out.println("HSPMedPalEJB.chiudiAll():  chiamata a \"it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure\": OK");
	}
	
	// 13/10/14
	private String getDtMenoUno(String dtIn) throws Exception
	{
		String nomeMetodo = "getDtMenoUno";
		String dtRet = "";
		
		NumberDateFormat ndf = new NumberDateFormat();
		String dtIn_1 = ndf.formDate(dtIn, "ggmmaaaa");
		DataWI dtAppo = new DataWI(dtIn_1);
		DataWI dtVMenoUno = dtAppo.aggiungiGg(-1);  
		dtRet = dtVMenoUno.getFormattedString2(1);
		
		LOG.debug(nomeMetodo+" - dtIn=["+ dtIn + "] - dtRet=[" + dtRet + "]");
		return dtRet;
	}
	
	// 13/10/14
	public ISASRecord getDatiLastSkHSP(myLogin mylogin, Hashtable h) throws  Exception 
	{
		String nomeMetodo = "getDatiLastSkHSP";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			LOG.info(nomeMetodo+" - HASH INPUT: h=["+h.toString()+"]");
			
			String cart = (String)h.get("n_cartella");
			String dtRif = (String)h.get("data_rif");
			String critDtRif = "";
			if ((dtRif != null) && (!dtRif.trim().equals("")))
				critDtRif = " AND b.skm_data_apertura <= " + formatDate(dbc, dtRif);
			
			
			String myselect = "SELECT a.* FROM hsp_scheda a"
					+ " WHERE a.n_cartella = " + cart
					+ " AND a.skm_data_apertura IN (SELECT MAX(b.skm_data_apertura)"
						+ " FROM hsp_scheda b"
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
	
	public boolean checkDtApeNotInHSP(ISASConnection dbc, String cart, String dtApe, String dtChiu) throws Exception 
	{
		String nomeMetodo = "checkDtApeNotInHSP";
		ISASCursor dbcur = null;
		boolean conDtChiu = ((dtChiu != null) && (!dtChiu.trim().equals("")));
		
		try { 
			// 17/12/14 mv
			boolean confAbilHSP = leggiAbilConf(dbc, KEYCONF_ABIL_HSP_IN_SINS);
			if (!confAbilHSP)
				return true;				
			
			
			String mySel_C = "SELECT * FROM hsp_scheda" 
					+ " WHERE n_cartella = " + cart
					+ " AND skm_data_apertura <= " + formatDate(dbc, dtApe)
					+ " AND ((skm_data_chiusura IS NULL)"
						+ " OR (skm_data_chiusura >= " + formatDate(dbc, dtApe) + ")"
					+ ")"
					+ " AND skm_tipo_contatto <> '" + TABVOCI_TPCONT_DAYHOSP + "'";
	
			dbcur = dbc.startCursor(mySel_C);
	
			if ((dbcur != null) && (dbcur.getDimension() > 0))
				return false;
			
			String mySel_A = "SELECT * FROM hsp_scheda" 
					+ " WHERE n_cartella = " + cart
					+ " AND skm_data_apertura IS NOT NULL"
					+ " AND skm_data_apertura >= " + formatDate(dbc, dtApe);
			
			if (conDtChiu)
				mySel_A += " AND skm_data_apertura < " + formatDate(dbc, dtChiu);
			
			mySel_A += " AND skm_tipo_contatto <> '" + TABVOCI_TPCONT_DAYHOSP + "'";
			
			dbcur = null;
			dbcur = dbc.startCursor(mySel_A);
	
			if ((dbcur != null) && (dbcur.getDimension() > 0))
				return false;
			
			return true;
		} finally {
			close_dbcur_nothrow(nomeMetodo, dbcur);
		}
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
		// se data_apertura � maggiore di data_chiusura restituisce 1
		// se data_apertura � minore di data_chiusura restituisce 2
		// se data_apertura � = di data_chiusura restituisce 0
		// se da errore -1
		if ((rit == 2) || (rit == 0))
			return true; // Non ammissibile
		else if (rit < 0) {
			throw new Exception("HSPSkMedPalEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
			// Si � verificato un errore nel metodo di confronto delle 2 date.
		} else
			// (rit == 1)
			return false; // Ammissibile
	}
	
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
		
	// 17/09/07: inserimento su tabella PROGETTO di un record con i soli valori
	// della chiave
	private void scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception {
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		printError("\n HSPSkMedPalEJB -->> insert: Inserito record su tabella PROGETTO");
	}
	
	// 17/12/14 
	private boolean leggiAbilConf(ISASConnection dbc, String keyConf) throws Exception
	{
		Hashtable hConf = getHashFromConf(dbc, null, new String[] {keyConf});
		String confTxt = (String)hConf.get(keyConf);
		return ((confTxt != null) && (confTxt.trim().equals("SI")));
	}
	
	
	
	private void printError(String msg) {
		if (mydebug)
			System.out.println("JCariPanelRivalutazione: " + msg);
	}
}

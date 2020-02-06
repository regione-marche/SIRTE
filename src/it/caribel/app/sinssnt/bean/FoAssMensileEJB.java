package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 30/10/2002 - EJB di connessione alla procedura SINSSNT Tabella FoAssMensile
//
// Jessica Caccavale
//
// ==========================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.math.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*; // fo merge
import it.pisa.caribel.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class FoAssMensileEJB extends SINSSNTConnectionEJB {
	String dom_res;
	String dr;
	private String MIONOME = "6-FoAssMensileEJB.";
	private static final String CODICE_TIPO_AREA_DISTRETTUALE = "A";
	private static final String CODICE_TIPO_COMUNE = "C";
	private static final String CODICE_TIPO_PRESIDIO = "P";//M.Minerba per Pistoia 21/02/2013
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO = "D";
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA = "R";
	private static final String CODICE_SANITARIO = "02";
	private static final String CODICE_SOCIALE = "01";;

	public static String getjdbcDate() {
		java.util.Date d = new java.util.Date();
		java.text.SimpleDateFormat local_dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
		return local_dateFormat.format(d);
	}

	public FoAssMensileEJB() {
	}

	public byte[] query_assmensile(String utente, String passwd, Hashtable par, mergeDocument doc) throws SQLException {

		boolean done = false;
		ISASConnection dbc = null;
		String mysel = "";
		String data_ini = "";
		String data_fine = "";
		String tipo = "";
		//DICHIARAZIONI DELLE HASHTABLE DA RIEMPIRE VUOTE
		Hashtable hfis = new Hashtable();
		Hashtable hmed = new Hashtable();
		Hashtable hinf = new Hashtable();
		Hashtable hass = new Hashtable();
		Hashtable stampa = new Hashtable();
		String  nmCampoPresidio = "";

		byte[] rit;
		boolean entrato = false;
		try {
			this.dom_res = (String) par.get("dom_res");
			if (this.dom_res != null) {
				if (this.dom_res.equals("R"))
					this.dr = "Residenza";
				else if (this.dom_res.equals("D"))
					this.dr = "Domicilio";
			}
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			data_ini = (String) par.get("data_inizio");
			data_fine = (String) par.get("data_fine");
			tipo = (String) par.get("tipo");

			//CARICO TUTTE LE HASHTABLE
			if (tipo.equals("C")) {
				//M.Minerba 21/12/2013 per Pistoia aggiunto il cod_presidio
				hfis = CaricaHash(dbc, "skfis", "skf_cod_presidio", "skf_data", "skf_data_chiusura", data_ini, data_fine, hfis, par);
				hmed = CaricaHash(dbc, "skmedico", "skm_cod_presidio", "skm_data_apertura", "skm_data_chiusura", data_ini, data_fine, hmed, par);
				hinf = CaricaHash(dbc, "skinf", "ski_cod_presidio", "ski_data_apertura", "ski_data_uscita", data_ini, data_fine, hinf, par);
				hass = CaricaHashSociale(dbc, data_ini, data_fine, hass, par);
			} else {
				hfis = CaricaHashInterv(dbc, "04", data_ini, data_fine, hfis, par);
				hmed = CaricaHashInterv(dbc, "03", data_ini, data_fine, hmed, par);
				hinf = CaricaHashInterv(dbc, "02", data_ini, data_fine, hinf, par);
				hass = CaricaHashInterv(dbc, "01", data_ini, data_fine, hass, par);
			}
			//RIEMPIO L'HASH PER LA STAMPA
			String dt1 = data_ini.substring(8, 10) + "/" + data_ini.substring(5, 7) + "/" + data_ini.substring(0, 4);
			String dt2 = data_fine.substring(8, 10) + "/" + data_fine.substring(5, 7) + "/" + data_fine.substring(0, 4);
			String periodo = "DAL " + dt1 + " AL " + dt2;
			//			preparaLayout(doc, dbc, periodo, tipo);
			preparaLayout(doc, dbc, periodo, tipo, par);
			doc.write("testa");

			InizializzaHash(data_ini, data_fine, hfis, hinf, hass, hmed, doc);

			doc.write("finetab");
			doc.write("finale");
			doc.close();
			rit = (byte[]) doc.get();
			//	System.out.println("byte[] restituito ");
			//String by= new String(rit);
			//System.out.println("Stringa del byte array   :"+by);
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

	private void InizializzaHash(String data_ini, String data_fine, Hashtable hfis, Hashtable hinf, Hashtable hass, Hashtable hmed,
			mergeDocument doc) {
		Hashtable has = new Hashtable();
		String[] Vmesi = { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre",
				"Novembre", "Dicembre" };
		String mi = data_ini.substring(5, 7);
		String ai = data_ini.substring(0, 4);
		String mf = data_fine.substring(5, 7);
		String af = data_fine.substring(0, 4);
		int mese_iniz = (new Integer(mi)).intValue();
		//System.out.println("Mese inizio="+mese_iniz);
		int anno_iniz = (new Integer(ai)).intValue();
		//System.out.println("Anno inizio="+anno_iniz);
		int mese_fine = (new Integer(mf)).intValue();
		//System.out.println("Mese fine="+mese_fine);
		int anno_fine = (new Integer(af)).intValue();
		//System.out.println("Anno fine="+anno_fine);
		int mese_ausi_iniz = mese_iniz;
		int mese_ausi_fine = 0;
		for (int i = anno_iniz; i <= anno_fine; i++) {
			if (i < anno_fine)
				mese_ausi_fine = 12;
			else
				mese_ausi_fine = mese_fine;
			for (int j = mese_ausi_iniz; j <= mese_ausi_fine; j++) {
				has.put("#mese#", Vmesi[j - 1] + " " + i);
				has.put("#meseEX#", Vmesi[j - 1] + "-" + i);
				if (hfis.get(Vmesi[j - 1] + i) != null)
					has.put("#mese_fis#", hfis.get(Vmesi[j - 1] + i));
				else
					has.put("#mese_fis#", "0");
				if (hmed.get(Vmesi[j - 1] + i) != null)
					has.put("#mese_med#", hmed.get(Vmesi[j - 1] + i));
				else
					has.put("#mese_med#", "0");
				if (hinf.get(Vmesi[j - 1] + i) != null)
					has.put("#mese_ip#", hinf.get(Vmesi[j - 1] + i));
				else
					has.put("#mese_ip#", "0");
				if (hass.get(Vmesi[j - 1] + i) != null)
					has.put("#mese_as#", hass.get(Vmesi[j - 1] + i));
				else
					has.put("#mese_as#", "0");
				doc.writeSostituisci("tabella", has);
			}
			mese_ausi_iniz = 1;
		}
		//return has;
	}
	//M.Minerba 21/02/2013 per Pistoia
	private Hashtable CaricaHash(ISASConnection dbc, String nometab, String nmCampoPresidio, String nome_data_ini, String nome_data_fine, String data_ini,
			String data_fine, Hashtable h, Hashtable par) throws SQLException {
		boolean done = false;
		ServerUtility su = new ServerUtility();

		try {
			String[] Vmesi = { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre",
					"Novembre", "Dicembre" };
			int anno_ini_arrivato = Integer.parseInt(data_ini.substring(0, 4));
			int mese_ini_arrivato = Integer.parseInt(data_ini.substring(5, 7));
			int gg_ini_arrivato = Integer.parseInt(data_ini.substring(8, 10));
			int anno_fine_arrivato = Integer.parseInt(data_fine.substring(0, 4));
			int mese_fine_arrivato = Integer.parseInt(data_fine.substring(5, 7));
			int gg_fine_arrivato = Integer.parseInt(data_fine.substring(8, 10));
			String data_ini_mese = "";
			String data_fine_mese = "";
			String mese_aggiornato = "";
			String segno_fine = "";
			String zero_gg = "";
			String zero_mm = "";
			int anno = 0;
			for (anno = anno_ini_arrivato; anno <= anno_fine_arrivato; anno++) {
				int mese_inizio = 0;
				int mese_fine = 0;
				if (anno_ini_arrivato == anno) {
					mese_inizio = mese_ini_arrivato;
				} else {
					mese_inizio = 1;
				}
				if (anno_fine_arrivato == anno) {
					mese_fine = mese_fine_arrivato;
				} else {
					mese_fine = 12;
				}
				for (int mese = mese_inizio; mese <= mese_fine; mese++) {
					//System.out.println("Anno dopo il for di mesi: "+anno);
					zero_gg = "";
					zero_mm = "";
					int mese_ok = mese;
					if (mese_ini_arrivato == mese && anno == anno_ini_arrivato) {
						if (gg_ini_arrivato < 10)
							zero_gg = "0";
						if (mese_ini_arrivato < 10)
							zero_mm = "0";
						data_ini_mese = zero_gg + gg_ini_arrivato + "/" + zero_mm + mese_ini_arrivato + "/" + anno_ini_arrivato;
						zero_gg = "";
						zero_mm = "";
					} else {
						//System.out.println("else mese_ini_arrivato=mese");
						if (mese < 10)
							zero_mm = "0";
						data_ini_mese = "01" + "/" + zero_mm + mese + "/" + anno;
					}
					if (mese_fine_arrivato == mese && anno == anno_fine_arrivato) {
						//System.out.println("mese_fine_arrivato=mese");
						segno_fine = "=";
						if (gg_fine_arrivato < 10)
							zero_gg = "0";
						if (mese_fine_arrivato < 10)
							zero_mm = "0";
						data_fine_mese = zero_gg + gg_fine_arrivato + "/" + zero_mm + mese_fine_arrivato + "/" + anno_fine_arrivato;
					} else {
						//System.out.println("else mese_fine_arrivato=mese");
						if (mese != 12) {
							mese_ok++;
							if (mese_ok < 10) {
								mese_aggiornato = "0" + mese_ok;
								data_fine_mese = "01" + "/" + mese_aggiornato + "/" + anno;
							} else
								data_fine_mese = "01" + "/" + mese_ok + "/" + anno;
						} else {
							int anno_incr = anno + 1;
							//System.out.println("anno incrementato"+anno_incr);
							data_fine_mese = "01" + "/01/" + anno_incr;
						}
					}
					System.out.println("****PRIMA DELLA SELECT " + par.toString());
					String selcount = "SELECT DISTINCT(t.n_cartella) conta FROM " + nometab + " t,anagra_c a,"
							+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
							+ " u WHERE " + nome_data_ini + "<" + segno_fine + formatDate(dbc, data_fine_mese) + " AND (" + nome_data_fine
							+ ">=" + formatDate(dbc, data_ini_mese) + " OR " + nome_data_fine + " IS NULL)"
							+ " AND t.n_cartella=a.n_cartella" + " AND a.data_variazione IN" + " (SELECT MAX (data_variazione)"
							+ " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";

					String ragg = (String) par.get("ragg");
					String pca = (String) par.get("pca");
					selcount = su.addWhere(selcount, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

					//              if (ragg!=null && ragg.equals("C")){
					//                    selcount += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
					//                            " AND u.codice=a.dom_citta)"+
					//                            " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
					//                            " AND u.codice=a.citta))";
					//              }else if (ragg!=null && ragg.equals("A")){
					//                selcount += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
					//                            " AND u.codice=a.dom_areadis)"+
					//                            " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
					//                            " AND u.codice=a.areadis))";
					//             }

					//Aggiunto Controllo Domicilio/Residenza (BYSP)
					if ((String) par.get("dom_res") == null) {
						if (ragg.equals("C"))
							selcount += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
									+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
						else if (ragg.equals("A"))
							selcount += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
									+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
						//Minerba per Pistoia 21/02/2013
						else if (ragg.equals("P"))
							selcount += "AND u.codice = t." + nmCampoPresidio;
						//fine Minerba per Pistoia 21/02/2013
					} else if (((String) par.get("dom_res")).equals("D")) {
						if (ragg.equals("C"))
							selcount += " AND u.codice=a.dom_citta";
						else if (ragg.equals("A"))
							selcount += " AND u.codice=a.dom_areadis";
						//Minerba per Pistoia 21/02/2013
						else if (ragg.equals("P"))
							selcount += "AND u.codice = t." + nmCampoPresidio;
						//fine Minerba per Pistoia 21/02/2013
					}

					else if (((String) par.get("dom_res")).equals("R")) {
						if (ragg.equals("C"))
							selcount += " AND u.codice=a.citta";
						else if (ragg.equals("A"))
							selcount += " AND u.codice=a.areadis";
						//Minerba per Pistoia 21/02/2013
						else if (ragg.equals("P"))
							selcount += "AND u.codice = t." + nmCampoPresidio;
						//fine Minerba per Pistoia 21/02/2013
					}

					selcount = su.addWhere(selcount, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
					selcount = su.addWhere(selcount, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
					selcount = su.addWhere(selcount, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

					System.out.println("Select Distinct su contatti:" + selcount);
					ISASCursor dbconta = dbc.startCursor(selcount);
					int i_conta = dbconta.getDimension();
					String conta = (new Integer(i_conta)).toString();
					int int_mese = (new Integer(mese)).intValue();
					h.put(Vmesi[int_mese - 1] + anno, conta);
					dbconta.close();
				}
			}
			//dbcur.close();
			done = true;
			return h;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

	private Hashtable CaricaHashSociale(ISASConnection dbc, String data_ini, String data_fine, Hashtable h, Hashtable par)
			throws SQLException {
		boolean done = false;
		ServerUtility su = new ServerUtility();

		try {
			String[] Vmesi = { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre",
					"Novembre", "Dicembre" };
			int anno_ini_arrivato = Integer.parseInt(data_ini.substring(0, 4));
			int mese_ini_arrivato = Integer.parseInt(data_ini.substring(5, 7));
			int gg_ini_arrivato = Integer.parseInt(data_ini.substring(8, 10));
			int anno_fine_arrivato = Integer.parseInt(data_fine.substring(0, 4));
			int mese_fine_arrivato = Integer.parseInt(data_fine.substring(5, 7));
			int gg_fine_arrivato = Integer.parseInt(data_fine.substring(8, 10));
			String data_ini_mese = "";
			String data_fine_mese = "";
			String mese_aggiornato = "";
			String segno_fine = "";
			String zero_gg = "";
			String zero_mm = "";
			int anno = 0;
			for (anno = anno_ini_arrivato; anno <= anno_fine_arrivato; anno++) {
				int mese_inizio = 0;
				int mese_fine = 0;
				if (anno_ini_arrivato == anno) {
					mese_inizio = mese_ini_arrivato;
				} else {
					mese_inizio = 1;
				}
				if (anno_fine_arrivato == anno) {
					mese_fine = mese_fine_arrivato;
				} else {
					mese_fine = 12;
				}
				for (int mese = mese_inizio; mese <= mese_fine; mese++) {
					//System.out.println("Anno dopo il for di mesi: "+anno);
					zero_gg = "";
					zero_mm = "";
					int mese_ok = mese;
					if (mese_ini_arrivato == mese && anno == anno_ini_arrivato) {
						if (gg_ini_arrivato < 10)
							zero_gg = "0";
						if (mese_ini_arrivato < 10)
							zero_mm = "0";
						data_ini_mese = zero_gg + gg_ini_arrivato + "/" + zero_mm + mese_ini_arrivato + "/" + anno_ini_arrivato;
						zero_gg = "";
						zero_mm = "";
					} else {
						//System.out.println("else mese_ini_arrivato=mese");
						if (mese < 10)
							zero_mm = "0";
						data_ini_mese = "01" + "/" + zero_mm + mese + "/" + anno;
					}
					if (mese_fine_arrivato == mese && anno == anno_fine_arrivato) {
						//System.out.println("mese_fine_arrivato=mese");
						segno_fine = "=";
						if (gg_fine_arrivato < 10)
							zero_gg = "0";
						if (mese_fine_arrivato < 10)
							zero_mm = "0";
						data_fine_mese = zero_gg + gg_fine_arrivato + "/" + zero_mm + mese_fine_arrivato + "/" + anno_fine_arrivato;
					} else {
						//System.out.println("else mese_fine_arrivato=mese");
						if (mese != 12) {
							mese_ok++;
							if (mese_ok < 10) {
								mese_aggiornato = "0" + mese_ok;
								data_fine_mese = "01" + "/" + mese_aggiornato + "/" + anno;
							} else
								data_fine_mese = "01" + "/" + mese_ok + "/" + anno;
						} else {
							int anno_incr = anno + 1;
							//System.out.println("anno incrementato"+anno_incr);
							data_fine_mese = "01" + "/01/" + anno_incr;
						}
					}
					String selcount = "SELECT DISTINCT(t.n_cartella) conta FROM ass_progetto t,anagra_c a, "
							+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
							+ " u  WHERE " + "ap_data_apertura<" + segno_fine + formatDate(dbc, data_fine_mese) + " AND ("
							+ "ap_data_chiusura>=" + formatDate(dbc, data_ini_mese) + " OR " + "ap_data_chiusura IS NULL )"
							+ " AND t.n_cartella=a.n_cartella" + " AND a.data_variazione IN" + " (SELECT MAX (data_variazione)"
							+ " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";
					String ragg = (String) par.get("ragg");
					String pca = (String) par.get("pca");

					selcount = su.addWhere(selcount, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

					//                  if (ragg!=null && ragg.equals("C")){
					//                        selcount += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
					//                                " AND u.codice=a.dom_citta)"+
					//                                " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
					//                                " AND u.codice=a.citta))";
					//                  }else if (ragg!=null && ragg.equals("A")){
					//                    selcount += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
					//                                " AND u.codice=a.dom_areadis)"+
					//                                " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
					//                                " AND u.codice=a.areadis))";
					//                 }

					//Aggiunto Controllo Domicilio/Residenza (BYSP)
					if ((String) par.get("dom_res") == null) {
						if (ragg.equals("C"))
							selcount += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
									+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
						else if (ragg.equals("A"))
							selcount += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
									+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
						//M.Minerba 21/02/2013 per Pistoia
						else if (ragg.equals("P"))
							selcount += " AND u.codice=t.ap_ass_ref_presidio";
						//fine M.Minerba 21/02/2013 per Pistoia
					} else if (((String) par.get("dom_res")).equals("D")) {
						if (ragg.equals("C"))
							selcount += " AND u.codice=a.dom_citta";
						else if (ragg.equals("A"))
							selcount += " AND u.codice=a.dom_areadis";
						//M.Minerba 21/02/2013 per Pistoia
						else if (ragg.equals("P"))
							selcount += " AND u.codice=t.ap_ass_ref_presidio";
						//fine M.Minerba 21/02/2013 per Pistoia
					}

					else if (((String) par.get("dom_res")).equals("R")) {
						if (ragg.equals("C"))
							selcount += " AND u.codice=a.citta";
						else if (ragg.equals("A"))
							selcount += " AND u.codice=a.areadis";
						//M.Minerba 21/02/2013 per Pistoia
						else if (ragg.equals("P"))
							selcount += " AND u.codice=t.ap_ass_ref_presidio";
						//fine M.Minerba 21/02/2013 per Pistoia
					}

					selcount = su.addWhere(selcount, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
					selcount = su.addWhere(selcount, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
					selcount = su.addWhere(selcount, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

					System.out.println("Select Distinct su ass_progetto:" + selcount);
					ISASCursor dbconta = dbc.startCursor(selcount);
					int i_conta = dbconta.getDimension();
					String conta = (new Integer(i_conta)).toString();
					int int_mese = (new Integer(mese)).intValue();
					h.put(Vmesi[int_mese - 1] + anno, conta);
					dbconta.close();
				}
			}
			//dbcur.close();
			done = true;
			return h;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

	private Hashtable CaricaHashInterv(ISASConnection dbc, String oper, String data_ini, String data_fine, Hashtable h, Hashtable par)
			throws SQLException {
		boolean done = false;
		ServerUtility su = new ServerUtility();

		try {
			String anno_ini_arrivato = data_ini.substring(0, 4);
			String mese_ini_arrivato = data_ini.substring(5, 7);
			String gg_ini_arrivato = data_ini.substring(8, 10);
			String anno_fine_arrivato = data_fine.substring(0, 4);
			String mese_fine_arrivato = data_fine.substring(5, 7);
			String gg_fine_arrivato = data_fine.substring(8, 10);
			String[] Vmesi = { "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno", "Luglio", "Agosto", "Settembre", "Ottobre",
					"Novembre", "Dicembre" };
			String myselect = "SELECT DISTINCT " + dbc.formatDbMonth("int_data_prest") + " mese ," + dbc.formatDbYear("int_data_prest")
					+ " anno " + "FROM interv,"
					+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n") + " u WHERE "
					+ "int_data_prest<=" + formatDate(dbc, data_fine) + " AND " + "int_data_prest>=" + formatDate(dbc, data_ini) + " AND "
					+ " int_tipo_oper='" + oper + "'";
			String ragg = (String) par.get("ragg");
			String pca = (String) par.get("pca");

			myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
			// per coerenza con la scelta solo contatto/progetto aperto
			// anche nella scelta almeno un accesso non e' previsto che il terzo livello
			// sia il presidio

			if (this.dom_res == null) {
				if (ragg.equals("A")) {
					myselect += " AND int_cod_areadis=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND int_cod_comune=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune", su.OP_EQ_STR, pca);
				}
				//M.Minerba per Pistoia 25/02/2013
				else if (ragg.equals("P")) {
					myselect += " AND int_cod_presidio_sk=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
				}//fine M.Minerba per Pistoia 25/02/2013
			} else if (this.dom_res.equals("D")) {
				if (ragg.equals("A")) {
					myselect += " AND int_cod_areadis=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND int_cod_comune=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune", su.OP_EQ_STR, pca);
				}
				//M.Minerba per Pistoia 25/02/2013
				else if (ragg.equals("P")) {
					myselect += " AND int_cod_presidio_sk=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
				}//fine M.Minerba per Pistoia 25/02/2013
			} else if (this.dom_res.equals("R")) {
				if (ragg.equals("A")) {
					myselect += " AND int_cod_res_areadis=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_res_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND int_cod_res_comune=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_res_comune", su.OP_EQ_STR, pca);
				}
				//M.Minerba per Pistoia 25/02/2013
				else if (ragg.equals("P")) {
					myselect += " AND int_cod_presidio_sk=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
				}//fine M.Minerba per Pistoia 25/02/2013
			}

			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));

			myselect += " ORDER BY anno,mese";
			System.out.println("Select CaricaHash" + myselect);
			ISASCursor dbcur = dbc.startCursor(myselect);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				//System.out.println("ISAS="+(dbr.getHashtable()).toString());
				String meseletto = (String) dbr.get("mese");
				String annoletto = (String) dbr.get("anno");
				String data_ini_mese = "";
				String data_fine_mese = "";
				String mese_aggiornato = "";
				int mese_ok = Integer.parseInt(meseletto);
				String segno_fine = "";
				//Questo controllo serve nel caso in cui mi venga scelta una data inizio periodo
				//che non coincide con la data inizio mese e una data fine periodo che non
				//coincide con la fine del mese
				if (anno_ini_arrivato.equals(annoletto)) {
					if (mese_ini_arrivato.equals(meseletto))
						data_ini_mese = gg_ini_arrivato + "/" + mese_ini_arrivato + "/" + anno_ini_arrivato;
					else
						data_ini_mese = "01" + "/" + meseletto + "/" + annoletto;
				} else
					data_ini_mese = "01" + "/" + meseletto + "/" + annoletto;
				if (anno_fine_arrivato.equals(annoletto)) {
					if (mese_fine_arrivato.equals(meseletto)) {
						segno_fine = "=";
						data_fine_mese = gg_fine_arrivato + "/" + mese_fine_arrivato + "/" + anno_fine_arrivato;
					} else {
						if (!meseletto.equals("12")) {
							mese_ok++;
							if (mese_ok < 10) {
								mese_aggiornato = "0" + mese_ok;
								data_fine_mese = "01" + "/" + mese_aggiornato + "/" + annoletto;
							} else
								data_fine_mese = "01" + "/" + mese_ok + "/" + annoletto;
						} else
							data_fine_mese = "01" + "/01/" + annoletto;
					}
				} else {
					if (!meseletto.equals("12")) {
						mese_ok++;
						if (mese_ok < 10) {
							mese_aggiornato = "0" + mese_ok;
							data_fine_mese = "01" + "/" + mese_aggiornato + "/" + annoletto;
						} else
							data_fine_mese = "01" + "/" + mese_ok + "/" + annoletto;
					} else
						data_fine_mese = "01" + "/01/" + annoletto;
				}
				//System.out.println("Data inizio mese"+data_ini_mese);
				//System.out.println("Data fine mese"+data_fine_mese);
				String selcount = "SELECT DISTINCT(t.int_cartella) conta FROM interv t, "
						+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
						+ " u WHERE "
						+ //,"+nometab+" WHERE "+
						"int_data_prest<" + segno_fine + formatDate(dbc, data_fine_mese) + " AND" + " int_data_prest>="
						+ formatDate(dbc, data_ini_mese) + " AND " + " int_tipo_oper='" + oper + "'";
				myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
				 // per coerenza con la scelta solo contatto/progetto aperto
				// anche nella scelta almeno un accesso non e' previsto che il terzo livello
				// sia il presidio
				if (this.dom_res == null) {
					if (ragg.equals("A")) {
						myselect += " AND int_cod_areadis=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis", su.OP_EQ_STR, pca);
					} else if (ragg.equals("C")) {
						myselect += " AND int_cod_comune=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune", su.OP_EQ_STR, pca);
					}
					//M.Minerba per Pistoia 25/02/2013
					else if (ragg.equals("P")) {
						myselect += " AND int_cod_presidio_sk=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
					}//fine M.Minerba per Pistoia 25/02/2013
				} else if (this.dom_res.equals("D")) {
					if (ragg.equals("A")) {
						myselect += " AND int_cod_areadis=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis", su.OP_EQ_STR, pca);
					} else if (ragg.equals("C")) {
						myselect += " AND int_cod_comune=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune", su.OP_EQ_STR, pca);
					}
					//M.Minerba per Pistoia 25/02/2013
					else if (ragg.equals("P")) {
						myselect += " AND int_cod_presidio_sk=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
					}//fine M.Minerba per Pistoia 25/02/2013
				} else if (this.dom_res.equals("R")) {
					if (ragg.equals("A")) {
						myselect += " AND int_cod_res_areadis=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_res_areadis", su.OP_EQ_STR, pca);
					} else if (ragg.equals("C")) {
						myselect += " AND int_cod_res_comune=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_res_comune", su.OP_EQ_STR, pca);
					}
					//M.Minerba per Pistoia 25/02/2013
					else if (ragg.equals("P")) {
						myselect += " AND int_cod_presidio_sk=u.codice ";
						myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
					}//fine M.Minerba per Pistoia 25/02/2013
				}

				myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
				myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));

				System.out.println("Select Distnct su interv:" + selcount);
				ISASCursor dbconta = dbc.startCursor(selcount);
				int i_conta = dbconta.getDimension();
				String conta = (new Integer(i_conta)).toString();
				System.out.println("Quante cartelle?: " + conta);
				int int_mese = (new Integer(meseletto)).intValue();
				//System.out.println("Conta="+conta);
				h.put(Vmesi[int_mese - 1] + annoletto, conta);
				System.out.println("HASH IN SELECT COUNT:" + h.toString());
				dbconta.close();
			}
			dbcur.close();
			done = true;
			return h;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

	private void preparaLayout(mergeDocument doc, ISASConnection dbc, String periodo, String tipo, Hashtable par) {
		Hashtable htxt = new Hashtable();
		String punto = MIONOME + "preparaLayout ";
		stampa(punto + "Inizio con dati>" + par + "<\n");

		ServerUtility su = new ServerUtility();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		String ora = currentTime();
		htxt.put("#ora#", ora);
		htxt.put("#periodo#", periodo);
		String linea = "";
		String valore = "";
		//	tipo=C&
		//		ragg=A&&dom_res=R&socsan=01
		//		distretto=1  pca=1 		zona=1
		String codZona = ISASUtil.getValoreStringa(par, "zona");
		if (ISASUtil.valida(codZona)) {
			String descrZona = recuperaDescrizioneZona(dbc, codZona);
			if (ISASUtil.valida(descrZona)) {
				linea += "Zona: " + descrZona + " ";
			}
		}
		String codDistretto = ISASUtil.getValoreStringa(par, "distretto");
		if (ISASUtil.valida(codDistretto)) {
			String descrDistretto = recuperaDescrizioneDistretto(dbc, codDistretto);
			if (ISASUtil.valida(descrDistretto)) {
				linea += "Distretto: " + descrDistretto + " ";
				;
			}
		}

		String raggruppamento = ISASUtil.getValoreStringa(par, "ragg");
		if (ISASUtil.valida(raggruppamento)) {
			if (raggruppamento.equals(CODICE_TIPO_COMUNE)) {
				valore = "Comune ";
				String codComune = ISASUtil.getValoreStringa(par, "pca");
				if (ISASUtil.valida(codComune)) {
					String descrComune = recuperaDescrizioneComune(dbc, codComune);
					if (ISASUtil.valida(descrComune)) {
						valore += descrComune + " ";
					}
				}
			} else if (raggruppamento.equals(CODICE_TIPO_AREA_DISTRETTUALE)) {
				valore = "Area distrettuale";
				String codAreaDis = ISASUtil.getValoreStringa(par, "pca");
				if (ISASUtil.valida(codAreaDis)) {
					String descrAreaDistret = recuperaDescrizioneAreaDistrettuale(dbc, codAreaDis);
					if (ISASUtil.valida(descrAreaDistret)) {
						valore += ": " + descrAreaDistret + " ";
					}
				}
			}
			//M.Minerba 21/02/2013 per Pistoia
			else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
				valore = "Presidio";
				String codPresidio = ISASUtil.getValoreStringa(par, "pca");
				if (ISASUtil.valida(codPresidio)) {
					String descrPresidio = recuperaDescrizionePresidio(dbc, codPresidio);
					if (ISASUtil.valida(descrPresidio)) {
						valore += ": " + descrPresidio + " ";
					}
				}
			}//fine M.Minerba 21/02/2013 per Pistoia
			linea += "Raggruppamento: " + valore;
		}
		String linea1 = "";
		String tipologiaUbicazione = ISASUtil.getValoreStringa(par, "dom_res");
		if (ISASUtil.valida(tipologiaUbicazione)) {
			if (tipologiaUbicazione.equals(CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA)) {
				valore = "Residenza";
			} else if (tipologiaUbicazione.equals(CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO)) {
				valore = "Domicilio";
			}
			linea1 += "Tipologia di Ubicazione: " + valore;
		}
		String socialeSan = ISASUtil.getValoreStringa(par, "socsan");
		if (ISASUtil.valida(socialeSan)) {
			if (socialeSan.equals(CODICE_SOCIALE)) {
				valore = "Sociale ";
			} else if (socialeSan.equals(CODICE_SANITARIO)) {
				valore = "Sanitario";
			}
			if (ISASUtil.valida(tipologiaUbicazione)) {
				linea1 += ", " + valore;
			} else {
				linea1 += "Tipologia di Ubicazione: " + valore;
			}
		}
		htxt.put("#filtri#", linea);
		htxt.put("#filtri1#", linea1);

		if (tipo.equals("C"))
			htxt.put("#titolo#", "(SOLO CONTATTO O PROGETTO APERTO)");
		else
			htxt.put("#titolo#", "(ALMENO UN INTERVENTO)");

		doc.writeSostituisci("layout", htxt);
	}
	//M.Minerba 21/02/2013 per Pistoia 
	private String recuperaDescrizionePresidio(ISASConnection dbc, String codPresidio) {
		String descrizionePresidio = "";
		String query = "SELECT * FROM presidi WHERE codpres = '" + codPresidio + "' ";
		ISASRecord dbrPresidio = getRecord(dbc, query);
		descrizionePresidio = ISASUtil.getValoreStringa(dbrPresidio, "despres");
		return descrizionePresidio;
	}//fine M.Minerba 21/02/2013 per Pistoia
	
	private String recuperaDescrizioneAreaDistrettuale(ISASConnection dbc, String codAreaDis) {
		String descrizioneComune = "";
		String query = "SELECT * FROM areadis WHERE codice = '" + codAreaDis + "' ";
		ISASRecord dbrComune = getRecord(dbc, query);
		descrizioneComune = ISASUtil.getValoreStringa(dbrComune, "descrizione");
		return descrizioneComune;
	}

	private String recuperaDescrizioneComune(ISASConnection dbc, String codComune) {
		String descrizioneComune = "";
		String query = " SELECT * FROM comuni  WHERE codice = '" + codComune + "' ";
		ISASRecord dbrDistretto = getRecord(dbc, query);
		descrizioneComune = ISASUtil.getValoreStringa(dbrDistretto, "descrizione");
		return descrizioneComune;
	}

	private String recuperaDescrizioneDistretto(ISASConnection dbc, String codDistretto) {
		String descrizioneDistretto = "";
		String query = " Select * from distretti WHERE cod_distr = '" + codDistretto + "' ";
		ISASRecord dbrDistretto = getRecord(dbc, query);
		descrizioneDistretto = ISASUtil.getValoreStringa(dbrDistretto, "des_distr");
		return descrizioneDistretto;
	}

	private String recuperaDescrizioneZona(ISASConnection dbc, String codZona) {
		String descrizioneZona = "";
		String query = " select * from zone where codice_zona = '" + codZona + "' ";
		ISASRecord dbrZone = getRecord(dbc, query);
		descrizioneZona = ISASUtil.getValoreStringa(dbrZone, "descrizione_zona");
		return descrizioneZona;
	}

	private ISASRecord getRecord(ISASConnection dbc, String query) {
		String punto = MIONOME + ".getRecord ";
		ISASRecord dbr = null;
		try {
			System.out.println(punto + "Query da eseguire> " + query + "<");
			dbr = dbc.readRecord(query);
		} catch (Exception ex) {
			System.out.println(punto + "Errore nel leggere il record con query>" + query + "<");
		}
		return dbr;
	}

	private void preparaLayout(mergeDocument doc, ISASConnection dbc, String periodo, String tipo) {
		Hashtable htxt = new Hashtable();
		String punto = MIONOME + "preparaLayout ";
		stampa(punto + "");
		ServerUtility su = new ServerUtility();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		String ora = currentTime();
		htxt.put("#ora#", ora);
		htxt.put("#periodo#", periodo);

		//	tipo=C&ragg=A&pca=1&distretto=1&dom_res=R&socsan=01&zona=1
		//	String raggruppamento = ISASUtil.getValoreStringa(par, key)

		if (tipo.equals("C"))
			htxt.put("#titolo#", "(SOLO CONTATTO O PROGETTO APERTO)");
		else
			htxt.put("#titolo#", "(ALMENO UN INTERVENTO)");
		doc.writeSostituisci("layout", htxt);
	}

	public String currentTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(" HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(cal.getTime());
	}

	public byte[] query_asspaz(String utente, String passwd, Hashtable par, mergeDocument doc) throws SQLException {
		String punto = MIONOME + "query_asspaz ";
		stampa(punto + "Inizio con dati>" + par + "<");

		boolean done = false;
		ISASConnection dbc = null;
		int fisio = 0;
		int inf = 0;
		int med = 0;
		int ass = 0;
		int tot = 0;
		Hashtable h = new Hashtable();
		ISASRecord dbr = null;
		String data_ini = "";
		String data_fine = "";
		String tipo = "";

		byte[] rit;
		boolean entrato = false;
		try {

			this.dom_res = (String) par.get("dom_res");
			if (this.dom_res != null) {
				if (this.dom_res.equals("R"))
					this.dr = "Residenza";
				else if (this.dom_res.equals("D"))
					this.dr = "Domicilio";
			}

			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			data_ini = (String) par.get("data_inizio");
			data_fine = (String) par.get("data_fine");
			tipo = (String) par.get("tipo");

			//RIEMPIO L'HASH PER LA STAMPA
			data_ini = data_ini.substring(8, 10) + "/" + data_ini.substring(5, 7) + "/" + data_ini.substring(0, 4);
			data_fine = data_fine.substring(8, 10) + "/" + data_fine.substring(5, 7) + "/" + data_fine.substring(0, 4);
			String periodo = "DAL " + data_ini + " AL " + data_fine;
			h.put("#periodo#", periodo);
			preparaLayout(doc, dbc, periodo, tipo, par);
			doc.write("testa");
			if (tipo.equals("C")) {
				fisio = CaricaIsas(dbc, "skfis", "skf_cod_presidio", "skf_data", "skf_data_chiusura", data_ini, data_fine, par);
				med = CaricaIsas(dbc, "skmedico", "skm_cod_presidio", "skm_data_apertura", "skm_data_chiusura", data_ini, data_fine, par);
				inf = CaricaIsas(dbc, "skinf", "ski_cod_presidio", "ski_data_apertura", "ski_data_uscita", data_ini, data_fine, par);
				ass = CaricaIsasSociale(dbc, data_ini, data_fine, par);
			} else {
				fisio = CaricaIsasInterv(dbc, "04", data_ini, data_fine, par);
				med = CaricaIsasInterv(dbc, "03", data_ini, data_fine, par);
				inf = CaricaIsasInterv(dbc, "02", data_ini, data_fine, par);
				ass = CaricaIsasInterv(dbc, "01", data_ini, data_fine, par);
			}

			//SCRIVO SECONDA RIGA
			h.put("#per_fis#", (new Integer(fisio)).toString());
			h.put("#per_ip#", (new Integer(inf)).toString());
			h.put("#per_as#", (new Integer(ass)).toString());
			h.put("#per_med#", (new Integer(med)).toString());
			tot = fisio + med + inf + ass;
			h.put("#totali#", (new Integer(tot)).toString());
			doc.writeSostituisci("tabella", h);

			doc.write("finetab");
			doc.write("finale");
			doc.close();
			//riprendo il bytearray
			rit = (byte[]) doc.get();
			//riprendo l'array di byte
			//	System.out.println("byte[] restituito ");
			//String by= new String(rit);
			//System.out.println("Stringa del byte array   :"+by);
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

	private void stampa(String messaggio) {
		System.out.println(messaggio);

	}

	private int CaricaIsas(ISASConnection dbc, String nometab, String nmCampoPresidio, String nome_data_ini, String nome_data_fine, String data_ini,
			String data_fine, Hashtable par) throws SQLException {
		boolean done = false;
		ServerUtility su = new ServerUtility();

		try {
			String myselect = "SELECT DISTINCT(t.n_cartella) conta FROM " + nometab + " t, anagra_c a,"
					+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n") + " u WHERE "
					+ nome_data_ini + "<=" + formatDate(dbc, data_fine) + " AND (" + nome_data_fine + " is null OR " + nome_data_fine
					+ ">=" + formatDate(dbc, data_ini) + " )" + " AND t.n_cartella=a.n_cartella" + " AND a.data_variazione IN"
					+ " (SELECT MAX (data_variazione)" + " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";
			String ragg = (String) par.get("ragg");
			String pca = (String) par.get("pca");
			myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

			//          if (ragg!=null && ragg.equals("C")){
			//                myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
			//                        " AND u.codice=a.dom_citta)"+
			//                        " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
			//                        " AND u.codice=a.citta))";
			//          }else if (ragg!=null && ragg.equals("A")){
			//            myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
			//                        " AND u.codice=a.dom_areadis)"+
			//                        " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
			//                        " AND u.codice=a.areadis))";
			//         }

			//Aggiunto Controllo Domicilio/Residenza (BYSP)
			if ((String) par.get("dom_res") == null) {
				if (ragg.equals("C"))
					myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
							+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
				else if (ragg.equals("A"))
					myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
							+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
				//M.Minerba 21/02/2013 per Pistoia 
				else if (ragg.equals("P"))
				       myselect += " AND u.codice = t." + nmCampoPresidio;
				//fine M.Minerba 21/02/2013 per Pistoia 
			} else if (((String) par.get("dom_res")).equals("D")) {
				if (ragg.equals("C"))
					myselect += " AND u.codice=a.dom_citta";
				else if (ragg.equals("A"))
					myselect += " AND u.codice=a.dom_areadis";
				//M.Minerba 21/02/2013 per Pistoia 
				else if (ragg.equals("P"))
				       myselect += " AND u.codice = t." + nmCampoPresidio;
				//fine M.Minerba 21/02/2013 per Pistoia 
			}

			else if (((String) par.get("dom_res")).equals("R")) {
				if (ragg.equals("C"))
					myselect += " AND u.codice=a.citta";
				else if (ragg.equals("A"))
					myselect += " AND u.codice=a.areadis";
				//M.Minerba 21/02/2013 per Pistoia 
				else if (ragg.equals("P"))
				       myselect += " AND u.codice = t." + nmCampoPresidio;
				//fine M.Minerba 21/02/2013 per Pistoia 
			}

			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
			myselect = su.addWhere(myselect, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

			ISASCursor dbcur = dbc.startCursor(myselect);
			int conta = dbcur.getDimension();
			System.out.println("Select CaricaIsas" + myselect);
			done = true;
			//return dbr;
			return conta;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

	private int CaricaIsasSociale(ISASConnection dbc, String data_ini, String data_fine, Hashtable par) throws SQLException {
		boolean done = false;
		ServerUtility su = new ServerUtility();

		try {
			String myselect = "SELECT DISTINCT(t.n_cartella) conta FROM ass_progetto t," + ""
					+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
					+ " u,anagra_c a" + " WHERE ap_data_apertura<=" + formatDate(dbc, data_fine) + " AND ("
					+ "ap_data_chiusura is null OR " + "ap_data_chiusura>=" + formatDate(dbc, data_ini) + " )"
					+ " AND t.n_cartella=a.n_cartella" + " AND a.data_variazione IN" + " (SELECT MAX (data_variazione)"
					+ " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";
			String ragg = (String) par.get("ragg");
			String pca = (String) par.get("pca");

			myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

			//                  if (ragg!=null && ragg.equals("C")){
			//                        myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
			//                                " AND u.codice=a.dom_citta)"+
			//                                " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
			//                                " AND u.codice=a.citta))";
			//                  }else if (ragg!=null && ragg.equals("A")){
			//                    myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
			//                                " AND u.codice=a.dom_areadis)"+
			//                                " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
			//                                " AND u.codice=a.areadis))";
			//                 }
			//Aggiunto Controllo Domicilio/Residenza (BYSP)
			if ((String) par.get("dom_res") == null) {
				if (ragg.equals("C"))
					myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
							+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
				else if (ragg.equals("A"))
					myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
							+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
				//M.Minerba 21/02/2013 per Pistoia
				else if (ragg.equals("P"))
					myselect += " AND u.codice=t.ap_ass_ref_presidio";
				//fine M.Minerba 21/02/2013 per Pistoia
			} else if (((String) par.get("dom_res")).equals("D")) {
				if (ragg.equals("C"))
					myselect += " AND u.codice=a.dom_citta";
				else if (ragg.equals("A"))
					myselect += " AND u.codice=a.dom_areadis";
				//M.Minerba 21/02/2013 per Pistoia
				else if (ragg.equals("P"))
					myselect += " AND u.codice=t.ap_ass_ref_presidio";
				//fine M.Minerba 21/02/2013 per Pistoia
			}

			else if (((String) par.get("dom_res")).equals("R")) {
				if (ragg.equals("C"))
					myselect += " AND u.codice=a.citta";
				else if (ragg.equals("A"))
					myselect += " AND u.codice=a.areadis";
				//M.Minerba 21/02/2013 per Pistoia
				else if (ragg.equals("P"))
					myselect += " AND u.codice=t.ap_ass_ref_presidio";
				//fine M.Minerba 21/02/2013 per Pistoia
			}

			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
			myselect = su.addWhere(myselect, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

			ISASCursor dbcur = dbc.startCursor(myselect);
			int conta = dbcur.getDimension();
			System.out.println("Select CaricaIsas" + myselect);
			done = true;
			//return dbr;
			return conta;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una ass_progetto()  ");
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

	private int CaricaIsasInterv(ISASConnection dbc, String oper, String data_ini, String data_fine, Hashtable par) throws SQLException {
		boolean done = false;
		ServerUtility su = new ServerUtility();

		try {
			String myselect = "SELECT DISTINCT(int_cartella) conta FROM interv,"
					+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n") + " u"
					+ " WHERE int_data_prest<=" + formatDate(dbc, data_fine) + " AND " + " int_data_prest>=" + formatDate(dbc, data_ini)
					+ " AND " + " int_tipo_oper='" + oper + "'";

			String ragg = (String) par.get("ragg");
			String pca = (String) par.get("pca");

			myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
			// per coerenza con la scelta solo contatto/progetto aperto
			// anche nella scelta almeno un accesso non e' previsto che il terzo livello
			// sia il presidio
			if (this.dom_res == null) {
				if (ragg.equals("A")) {
					myselect += " AND int_cod_areadis=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND int_cod_comune=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune", su.OP_EQ_STR, pca);
				}
				//M.Minerba per Pistoia 25/02/2013
				else if (ragg.equals("P")) {
					myselect += " AND int_cod_presidio_sk=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
				}//fine M.Minerba per Pistoia 25/02/2013
			} else if (this.dom_res.equals("D")) {
				if (ragg.equals("A")) {
					myselect += " AND int_cod_areadis=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND int_cod_comune=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune", su.OP_EQ_STR, pca);
				}
				//M.Minerba per Pistoia 25/02/2013
				else if (ragg.equals("P")) {
					myselect += " AND int_cod_presidio_sk=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
				}//fine M.Minerba per Pistoia 25/02/2013
			} else if (this.dom_res.equals("R")) {
				if (ragg.equals("A")) {
					myselect += " AND int_cod_res_areadis=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_res_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND int_cod_res_comune=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_res_comune", su.OP_EQ_STR, pca);
				}
				//M.Minerba per Pistoia 25/02/2013
				else if (ragg.equals("P")) {
					myselect += " AND int_cod_presidio_sk=u.codice ";
					myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk", su.OP_EQ_STR, pca);
				}//fine M.Minerba per Pistoia 25/02/2013
			}

			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));

			ISASCursor dbcur = dbc.startCursor(myselect);
			int conta = dbcur.getDimension();
			System.out.println("Select CaricaIsasInterv" + myselect);
			//dbcur.close();
			done = true;
			//return dbr;
			return conta;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatori()  ");
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

} // End of FoEleSoc class

package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//16/06/2010 bargi anomalia caso nessuna divisione ma zona selezionata
// 06/02/2002 - EJB di connessione alla procedura SINS Tabella FoInfElenco
//
//Jessica Caccavale/Giulia Brogi
//
// 22/10/2007 - bargi [ONCOLOGO]
// ==========================================================================


import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.ndo.*;
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

public class FoInfElencoEJB extends SINSSNTConnectionEJB {

	private static final String mioNome = "2-FoInfElencoEJB.";
	String dom_res;
	String dr;
	public static String getjdbcDate() {
		java.util.Date d = new java.util.Date();
		java.text.SimpleDateFormat local_dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
		return local_dateFormat.format(d);
	}

	private boolean dividiEta = false;// indica se e' richiesta la divisione per
	// fasce eta'
	int tot_f = 0;
	int tot_m = 0;
	Hashtable h_fasceEta = new Hashtable();
	Hashtable hColonne = new Hashtable();
	Hashtable h_1 = new Hashtable();
	Hashtable h_2 = new Hashtable();
	Hashtable h_3 = new Hashtable();
	Hashtable h_4 = new Hashtable();
	Hashtable h_5 = new Hashtable();
	Hashtable h_6 = new Hashtable();
	// colonne
	Hashtable HDescCol = new Hashtable();

	private String codice_usl = "";
	private String codice_regione = "";

	private void caricaHashFasce(Hashtable par) {
		String fasce = (String) par.get("eta");
		if ((fasce != null) && (!fasce.trim().equals(""))) {
			StringTokenizer strTkzFasce = new StringTokenizer(fasce, "|");
			while (strTkzFasce.hasMoreTokens()) {
				String singolaFascia = (String) strTkzFasce.nextToken();
				int pos = singolaFascia.indexOf("-");
				h_fasceEta.put(singolaFascia.substring(0, pos), singolaFascia.substring(pos + 1));
			}
		}
	}

	/**
	 * restituisce un parametro data come stringa nel formato gg/mm/aaaa
	 */
	private String getStringDate(Hashtable par, String k) {
		try {
			String s = (String) par.get(k);
			s = s.substring(8, 10) + "/" + s.substring(5, 7) + "/" + s.substring(0, 4);
			return s;
		} catch (Exception e) {
			debugMessage("getStringDate(" + par + ", " + k + "): " + e);
			return "";
		}
	}

	public int ConvertData(String dataold, String datanew) {
		// inizializzazione della variabile eta
		int eta = 0;
		int tempeta = 0;

		// preparazione primo array
		int[] datavecchia = new int[3];
		Integer giorno = new Integer(dataold.substring(8, 10));
		datavecchia[0] = giorno.intValue();
		Integer mese = new Integer(dataold.substring(5, 7));
		datavecchia[1] = mese.intValue();
		Integer anno = new Integer(dataold.substring(0, 4));
		datavecchia[2] = anno.intValue();

		// preparazione secondo array

		int[] datanuova = new int[3];
		Integer day = new Integer(datanew.substring(8, 10));
		datanuova[0] = day.intValue();
		Integer mounth = new Integer(datanew.substring(5, 7));
		datanuova[1] = mounth.intValue();
		Integer year = new Integer(datanew.substring(0, 4));
		datanuova[2] = year.intValue();

		tempeta = datanuova[2] - datavecchia[2];
		// confronto mese
		if (datanuova[1] < datavecchia[1])
			tempeta = tempeta - 1; // anni non ancora compiuti
		else if (datanuova[1] == datavecchia[1])
			if (datanuova[0] < datavecchia[0]) // confronto giorno
				tempeta = tempeta - 1; // anni non ancora compiuti
		eta = tempeta;
		return eta;
	}

	public FoInfElencoEJB() {
	}

	private String getFiguraProfessionale(String tipo) {
		String fp = "";
		try {
			if (tipo.equals("00"))
				fp = "TUTTE LE FIGURE PROFESSIONALI";
			else if (tipo.equals("01"))
				fp = "ASSISTENTE SOCIALE";
			else if (tipo.equals("02"))
				fp = "INFERMIERE";
			else if (tipo.equals("03"))
				fp = "MEDICO";
			else if (tipo.equals("04"))
				fp = "FISIOTERAPISTA";
			else if (tipo.equals("52"))
				fp = "ONCOLOGO";
			else
				fp = "FIGURA PROFESSIONALE NON VALIDA";
		} catch (Exception e) {
			fp = "FIGURA PROFESSIONALE ERRATA";
		}
		return fp;
	}

	private String getSelectElenco(ISASConnection dbc, Hashtable par) {
		String punto = mioNome + ".getSelectElenco ";
		stampa(punto, par);

		String from = "";
		String where = "";
		String motivo = (String) par.get("motivo");
		System.out.println("MOTIVO:" + motivo);
		String tipo = (String) par.get("figprof");
		System.out.println("figprof:" + tipo);

		if (tipo.trim().equals("01"))
			if (!motivo.equals("") && !motivo.equals("TUTTO")) {
				// gb 25/06/07 from="contatti co,";
				from = "ass_progetto co,"; // gb 25/06/07
				where = " AND r.n_cartella = co.n_cartella" +
				// gb 25/06/07 " AND r.n_contatto = co.n_contatto"+
						" AND r.n_contatto = co.n_progetto" + // gb 25/06/07
//						" AND co.motivo = '" + motivo + "'";
						" AND co.ap_motivo = '" + motivo + "'";
			}
		if (tipo.equals("02")) {
			System.out.println("Sono negli infemrieri");
			if (!motivo.equals("") && !motivo.equals("TUTTO")) {
				System.out.println("Motivo diverso");
				from = "skinf sk,";
				where = " AND r.n_cartella=sk.n_cartella AND r.n_contatto=sk.n_contatto" + " AND sk.ski_motivo='"
						+ motivo + "'";
			}
		}
		boolean stampaTerr = true;
		if (par.get("terr") != null && ((String) par.get("terr")).equals("0|0|0"))
			stampaTerr = false;

		String s = "SELECT DISTINCT c.n_cartella,c.data_nasc,c.sesso," + " u.cod_zona,u.des_zona, "
				+ " u.des_distretto,u.cod_distretto," + " u.codice ,u.descrizione " + "FROM contsan_n r, cartella c, "
				+ from + // "operatori o,"+
				"anagra_c a,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
		try {
			String w = getSelectParteWhereElenco(dbc, par);
			if (!w.equals(""))
				s = s + " WHERE " + w + where;
		} catch (Exception e) {
			debugMessage("FoInfElencoEJB.getSelectElenco(): " + e);
			e.printStackTrace();
		}
		s = s + " ORDER BY u.des_zona, u.des_distretto,u.descrizione";
		stampaQuery(punto, s);
		// debugMessage("FoInfElencoEJB.getSelectElenco(): " + s);
		return s;
	}

	/**
	 * restituisce la parte where della select valorizzata secondo i parametri
	 * di ingresso.
	 */
	private String getSelectParteWhereElenco(ISASConnection dbc, Hashtable par) {
		String punto = mioNome + ".getSelectParteWhereElenco ";
		ServerUtility su = new ServerUtility();

		String figprof = "";
		if (!par.get("figprof").equals("00"))
			figprof = (String) par.get("figprof");
		String s = su.addWhere("", su.REL_AND, "r.tipo_operatore", su.OP_EQ_STR, figprof);

		String scr = (String) par.get("ragg");// RAGGRUPPAMENTO
		s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);

		s = su.addWhere(s, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		s = su.addWhere(s, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		s = su.addWhere(s, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));

//		if (scr.equals("C"))
//			s += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
//					+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
//		else if (scr.equals("A"))
//			s += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" + " AND u.codice=a.dom_areadis)"
//					+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') " + " AND u.codice=a.areadis))";
        //Aggiunto Controllo Domicilio/Residenza (BYSP)
        if(this.dom_res == null)
        {
                if (scr.equals("C"))
                  s += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                        " AND u.codice=a.dom_citta)"+
        		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                        " AND u.codice=a.citta))";
                else if (scr.equals("A"))
                  s += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                        " AND u.codice=a.dom_areadis)"+
        		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                        " AND u.codice=a.areadis))";
                //M.Minerba 25/02/2013 per Pistoia 
                else if (scr.equals("P"))
                    s += " AND u.codice=r.cod_presidio";
                //fine M.Minerba 25/02/2013 per Pistoia 
        }
        else if (this.dom_res.equals("D"))
                                  {
                                   if (scr.equals("C"))
                  s += " AND u.codice=a.dom_citta";
                                    else if (scr.equals("A"))
                  s += " AND u.codice=a.dom_areadis";
                                 //M.Minerba 25/02/2013 per Pistoia 
                                    else if (scr.equals("P"))
                 s += " AND u.codice=r.cod_presidio";
                                    //fine M.Minerba 25/02/2013 per Pistoia 
                                  }

        else if (this.dom_res.equals("R"))
                        {
                        if (scr.equals("C"))
                  s += " AND u.codice=a.citta";
                else if (scr.equals("A"))
                  s += " AND u.codice=a.areadis";
                      //M.Minerba 25/02/2013 per Pistoia 
                else if (scr.equals("P"))
                    s += " AND u.codice=r.cod_presidio";
                //fine M.Minerba 25/02/2013 per Pistoia 
                        }
		/*
		 * s = su.addWhere(s, su.REL_AND, "r.data_inizio", su.OP_GE_NUM,
		 * formatDate(dbc, (String)par.get("data_inizio")));
		 */
		s = su.addWhere(s, su.REL_AND, "r.data_inizio", su.OP_LE_NUM, formatDate(dbc, (String) par.get("data_fine")));

		if (!s.equals(""))
			s += " AND ";
		s += " (r.data_fine is null "
				+
				// OR "r.data_fine = "+formatDate(dbc, "1000-01-01")+
				" OR r.data_fine >= " + formatDate(dbc, (String) par.get("data_inizio"))
				+ ") "
				+
				// " AND r.operatore = o.codice "+
				" AND r.n_cartella = c.n_cartella AND r.n_cartella = a.n_cartella "
				+ " AND a.data_variazione IN (SELECT MAX(data_variazione) "
				+ " FROM anagra_c WHERE a.n_cartella = anagra_c.n_cartella)";
		// Controllo se hanno scelto un motivo
		stampaQuery(punto, s);
		return s;
	}

	private void preparaLayout(ISASConnection dbc, Hashtable par, NDOContainer cnt) {
		String txt = "";
		boolean done = false;
		String titolo = "";
		ISASCursor dbconf = null;
		ISASUtil ut = new ISASUtil();
		try {
			String mysel = "SELECT conf_txt,conf_key FROM conf WHERE "
					+ "conf_kproc='SINS' AND (conf_key='codice_regione'" + " OR conf_key='ragione_sociale'"
					+ " OR conf_key='codice_usl')";
			debugMessage("FoSingolaProfEJB.preparaLayout(): " + mysel);

			dbconf = dbc.startCursor(mysel);
			while (dbconf.next()) {
				ISASRecord dbtxt = dbconf.getRecord();
				if (((String) dbtxt.get("conf_key")).equals("ragione_sociale"))
					txt = (String) dbtxt.get("conf_txt");
				if (((String) dbtxt.get("conf_key")).equals("codice_regione"))
					codice_regione = (String) dbtxt.get("conf_txt");
				if (((String) dbtxt.get("conf_key")).equals("codice_usl"))
					codice_usl = (String) dbtxt.get("conf_txt");
			}
			dbconf.close();
			cnt.setFooter(txt);

			Vector vtitoli = new Vector();

			String tipoStampa = (String) par.get("terr");
			StringTokenizer st = new StringTokenizer(tipoStampa, "|");

			String sZona = st.nextToken();
			String sDis = st.nextToken();
			String sCom = st.nextToken();
			String sAss = (String) par.get("ass");

			String subTitolo = "";

			if (sZona.equals("1")) {
				vtitoli.add("Zona");
				subTitolo = "Zona: " + DecodificaZona(dbc, (String) par.get("zona"));
			} else
				subTitolo = "Zona: Nessuna divisione";

			if (sDis.equals("1")) {
				vtitoli.add("Distretto");
				subTitolo = subTitolo + " - Distretto: " + DecodificaDistretto(dbc, (String) par.get("distretto"));
			} else
				subTitolo = subTitolo + " - Distretto: Nessuna divisione";

			String ragg = (String) par.get("ragg");
			String tipopca = "";
//			if (ragg.equals("A")) {
//				tipopca = " Area distrettuale ";
//			} else if (ragg.equals("C")) {
//				tipopca = " Comune ";
//			} else if (ragg.equals("P"))
//				tipopca = " Presidio ";
            if(this.dom_res==null)
            {
            	if (ragg.equals("A")) 
                    tipopca = " Area distrettuale ";
                 else if (ragg.equals("C")) 
                        tipopca = " Comune ";
                 else if (ragg.equals("P"))
                        tipopca = " Presidio ";

            }else if (this.dom_res.equals("D"))
            {
            	if (ragg.equals("A")) 
                    tipopca = " Area distrettuale di Domicilio ";
                 else if (ragg.equals("C")) 
                        tipopca = " Comune di Domicilio ";
                 else if (ragg.equals("P"))
                     tipopca = " Presidio ";
            }else if (this.dom_res.equals("R"))
                {
                	if (ragg.equals("A")) 
                        tipopca = " Area distrettuale di Residenza ";
                     else if (ragg.equals("C")) 
                            tipopca = " Comune di Residenza ";
                     else if (ragg.equals("P"))
                         tipopca = " Presidio ";
                } 	
			if (sCom.equals("1")) {
				vtitoli.add(tipopca);
				subTitolo = subTitolo + " - " + tipopca + ": " + DecodificaLiv3(dbc, (String) par.get("pca"), ragg);
			} else
				subTitolo = subTitolo + " - " + tipopca + " Nessuna divisione";

			cnt.setGroupTitles(vtitoli);

			titolo = "RIEPILOGO ASSISTITI PER FASCE DI ETA' - " + "DA DATA: " + getStringDate(par, "data_inizio")
					+ " - " + "A DATA: " + getStringDate(par, "data_fine");
			String tipo = (String) par.get("figprof");
			titolo += " - FIGURA PROFESSIONALE: " + getFiguraProfessionale(tipo);
			String motivo = (String) par.get("motivo");
			if (tipo.equals("01")) {
				if (!motivo.equals("") && !motivo.equals("TUTTO"))
					titolo += " - MOTIVO ASSISTENTE SOCIALE: "
							+ ut.getDecode(dbc, "motivo", "codice", motivo, "descrizione");
			} else if (tipo.equals("02")) {
				if (!motivo.equals("") && !motivo.equals("TUTTO"))
					titolo += " - MOTIVO INFERMIERE: " + ut.getDecode(dbc, "motivo_s", "codice", motivo, "descrizione");
			}
			cnt.setHeader(titolo);

			cnt.setSubTitle(subTitolo);
			done = true;
		} catch (Exception ex) {
			// cnt.setHeader(titolo);
		} finally {
			if (!done) {
				try {
					if (dbconf != null)
						dbconf.close();
				} catch (Exception e1) {
					System.out.println("FoSingolaProfEJB.preparaLayout(): " + e1);
				}
			}
		}
	}

	public byte[] query_infelenco(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = mioNome + "query_infelenco ";
		stampaInizio(punto, par);
		boolean done = false;
		ISASConnection dbc = null;
		byte[] jessy;
		boolean primo = true;
		try {
			this.dom_res=(String)par.get("dom_res");
			if (this.dom_res != null)
			{
			if (this.dom_res.equals("R")) this.dr="Residenza";
			else if (this.dom_res.equals("D")) this.dr="Domicilio";
				}
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			NDOContainer cnt = new NDOContainer();
			NDOUtil unt = new NDOUtil();

			caricaHashFasce(par);
			dividiEta = (((String) par.get("eta") != null) && (!((String) par.get("eta")).trim().equals("")));
			caricaHashColonne(par);

			preparaLayout(dbc, par, cnt);

			ISASCursor dbcur = dbc.startCursor(getSelectElenco(dbc, par));
			if (dbcur.getDimension() <= 0) {
				debugMessage("FoSingolaProfEJB.query_singprof(): vuoto");
				cnt.setSubTitle("NESSUNA INFORMAZIONE REPERITA");
			} else {
				mkElencoBody(dbcur, cnt, unt, par);
			}

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return StampaNDO(cnt, par);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_elenco infermieri()  ");
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

	private byte[] StampaNDO(NDOContainer cnt, Hashtable par) throws Exception {
		try {
			if (cnt == null)
				System.out.println("FoInfElencoEJB/StampaNDO/cnt � nullo");
			else
				System.out.println("FoInfElencoEJB/StampaNDO/cnt non � nullo");
			cnt.colSort();
			System.out.println("FoInfElencoEJB/StampaNDO/ dopo: 'cnt.colSort()'");
			cnt.rowSort();
			System.out.println("FoInfElencoEJB/StampaNDO/ dopo: 'cnt.rowSort()'");
			NDOPrinter prt = new NDOPrinter();

			String tipoStampa = (String) par.get("terr");
			System.out.println("FoInfElencoEJB/StampaNDO/tipoStampa: " + tipoStampa);
			StringTokenizer st = new StringTokenizer(tipoStampa, "|");

			String sZona = st.nextToken();
			System.out.println("FoInfElencoEJB/StampaNDO/sZona: " + sZona);
			String sDis = st.nextToken();
			System.out.println("FoInfElencoEJB/StampaNDO/sDis: " + sDis);
			String sCom = st.nextToken();
			System.out.println("FoInfElencoEJB/StampaNDO/sCom: " + sCom);
			int iZona = Integer.parseInt(sZona);
			int iDis = Integer.parseInt(sDis);
			int iCom = Integer.parseInt(sCom);

			int iLivello = (iZona + iDis + iCom) - 1;
			System.out.println("FoInfElencoEJB/StampaNDO/iLivello: " + iLivello);
			if (iLivello >= 0)
				prt.addContainer(cnt, true, false, iLivello, (dividiEta ? 1 : 0), true, false);
			else
				prt.addContainer(cnt, true, false, 0, (dividiEta ? 1 : 0), false, false);

			System.out.println("FoInfElencoEJB/StampaNDO/prima di 'return prt.getDocument(1);'");
			String formato=(String)par.get("formato");
			return prt.getDocument(Integer.parseInt(formato));
		} catch (Exception e) {
			debugMessage("FoSingolaProfEJB.StampaNDO(): " + e);
			throw new SQLException("Errore eseguendo StampaNDO()");
		}
	}

	// private void mkElencoBody(ISASConnection dbc, ISASCursor dbcur,
	// Hashtable par, mergeDocument doc) throws Exception {
	private void mkElencoBody(ISASCursor dbcur, NDOContainer cnt, NDOUtil unt, Hashtable par) throws Exception {
		try {
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				ElaboraDati(dbr, cnt, unt, par);
			}
			cnt.calculate();
		} catch (Exception e) {
			debugMessage("FoInfElencoEJB.mkElencoBody(): " + e);
			throw new SQLException("Errore eseguendo mkElencoBody()");
		}
	}

	private void ElaboraDati(ISASRecord dbr, NDOContainer cnt, NDOUtil unt, Hashtable par) throws Exception {
		try {
			String tipoStampa = (String) par.get("terr");
			StringTokenizer st = new StringTokenizer(tipoStampa, "|");
			/*
			 * se trovo 0 vuol dire Nessuna divisione-->non devo stampare il
			 * livello se trovo 1 vuol dire che lo devo stampare la prima
			 * posizione � la zona la seconda posizione � il distretto la terza
			 * posizione � il comune/Areadis
			 */
			String sZona = st.nextToken();
			String sDis = st.nextToken();
			String sCom = st.nextToken();

			Vector vRiga = new Vector();// carico in un vettore gli eventuali
			// elementi di riga
			Vector vDecRiga = new Vector();// carico in un vettore le
			// descrizioni
			if (sZona.equals("1")) {
				vRiga = Aggiungi(vRiga, "cod_zona", dbr);
				vDecRiga = Aggiungi(vDecRiga, "des_zona", dbr);
			}
			if (sDis.equals("1")) {
				vRiga = Aggiungi(vRiga, "cod_distretto", dbr);
				vDecRiga = Aggiungi(vDecRiga, "des_distretto", dbr);
			}
			if (sCom.equals("1")) {
				vRiga = Aggiungi(vRiga, "codice", dbr);
				vDecRiga = Aggiungi(vDecRiga, "descrizione", dbr);
			}

			caricaNDO(vRiga, vDecRiga, dbr, unt, cnt, par);

		} catch (Exception e) {
			debugMessage("FoInfElencoEJB.ElaboraDati(): " + e);
			throw new SQLException("Errore eseguendo ElaboraDati()");
		}
	}

	private boolean caricaNDO(Vector vCod, Vector vDesc, ISASRecord dbr, NDOUtil unt, NDOContainer cnt, Hashtable par)
			throws SQLException {
		try {
			String fasciaEta = "NON_SPEC";
			if (dividiEta) {
				String dataNascita = "";
				if (dbr.get("data_nasc") != null)
					dataNascita = ((java.sql.Date) dbr.get("data_nasc")).toString();
				String dataFine = (String) par.get("data_fine");

				NumberDateFormat ut = new NumberDateFormat();
				int eta = -1;
				if (!dataNascita.equals("") && dataNascita.length() == 10)
					eta = ut.getAge(dataNascita, dataFine);
				else {
					System.out.println("****CARTELLA " + dbr.get("n_cartella") + ": DATA NASCITA NON VALIDA");
					return false;
				}
				fasciaEta = calcolaFascia(eta);
			}
			String sex = (String) dbr.get("sesso");
			/*
			 * debugMessage("SEX??--->"+sex); if(sex.equals("F")){ tot_f++;
			 * debugMessage("tot F:"+tot_f); }else if(sex.equals("M")){ tot_m++;
			 * debugMessage("tot M:"+tot_m); }
			 */
			if (vCod.size() == 0) {
				cnt.put(unt.mkPar("TOT"), (dividiEta ? unt.mkPar(sex, fasciaEta) : unt.mkPar(sex)), new Integer(1));
				cnt.put(unt.mkPar("TOT"), unt.mkPar("TF", "Z"), new Integer(1));
				cnt.put(unt.mkPar("TOT"), unt.mkPar(sex, "TS"), new Integer(1));
			}else if (vCod.size() == 1) {//bargi 16/06/2010 schiantava aggiunto controllo pb NESSUNA DIVISIONE
				cnt.put(unt.mkPar("" + vCod.elementAt(0)),
						(dividiEta ? unt.mkPar(sex, fasciaEta) : unt.mkPar(sex)), new Integer(1));

				cnt.put(unt.mkPar("" + vCod.elementAt(0)), unt.mkPar(
						"TF", "Z"), new Integer(1));
				if (dividiEta) {
					cnt.put(unt.mkPar("" + vCod.elementAt(0)), unt
							.mkPar(sex, "TS"), new Integer(1));
				} 
			}else if (vCod.size() == 2) {//bargi 16/06/2010 schiantava aggiunto controllo pb NESSUNA DIVISIONE
				cnt.put(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1)),
						(dividiEta ? unt.mkPar(sex, fasciaEta) : unt.mkPar(sex)), new Integer(1));

				cnt.put(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1)), unt.mkPar(
						"TF", "Z"), new Integer(1));
				if (dividiEta) {
					cnt.put(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1)), unt
							.mkPar(sex, "TS"), new Integer(1));
				}
			}else {
				cnt.put(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), "" + vCod.elementAt(2)),
						(dividiEta ? unt.mkPar(sex, fasciaEta) : unt.mkPar(sex)), new Integer(1));

				cnt.put(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), "" + vCod.elementAt(2)), unt.mkPar(
						"TF", "Z"), new Integer(1));
				if (dividiEta) {
					cnt.put(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), "" + vCod.elementAt(2)), unt
							.mkPar(sex, "TS"), new Integer(1));
				}
			}
			if (dividiEta) {
				settaTitoliColonne2(cnt, unt, sex, fasciaEta);
			} else
				settaTitoliColonne(cnt, unt, sex);

			if (dividiEta) {
				settaTitoliColonne2(cnt, unt, "TF", "Z");
				settaTitoliColonne2(cnt, unt, sex, "TS");
			} else {
				settaTitoliColonne(cnt, unt, "TF");
				settaTitoliColonne(cnt, unt, "TS");
			}
			settaTitoliRiga(vDesc, vCod, cnt, unt);
			return true;
		} catch (Exception e) {
			debugMessage("FoInfElencoEJB.caricaNDO(): " + e);
			throw new SQLException("Errore eseguendo caricaNDO()");
		}
	}

	private void settaTitoliColonne2(NDOContainer cnt, NDOUtil unt, String keyLiv1, String keyLiv2) {
		if (HDescCol.containsKey(keyLiv1)) {
			if (!HDescCol.containsKey(keyLiv1 + "|" + keyLiv2)) {
				cnt.setColTitle(unt.mkPar(keyLiv1, keyLiv2), (String) hColonne.get(keyLiv2));
				HDescCol.put(keyLiv1 + "|" + keyLiv2, "");
			}
		} else {
			cnt.setColTitle(unt.mkPar(keyLiv1), (String) hColonne.get(keyLiv1));
			cnt.setColTitle(unt.mkPar(keyLiv1, keyLiv2), (String) hColonne.get(keyLiv2));
			HDescCol.put(keyLiv1, "");
			HDescCol.put(keyLiv1 + "|" + keyLiv2, "");
		}
	}

	private void settaTitoliColonne(NDOContainer cnt, NDOUtil unt, String chiave) {
		/*
		 * Controllo che non sia inserito nella hashtable HDescCol Se lo trovo
		 * nella Hashtable vuol dire che per quella chiave ho gi� inserito la
		 * descrizione
		 */
		if (!HDescCol.containsKey(chiave)) {
			cnt.setColTitle(unt.mkPar(chiave), (String) hColonne.get(chiave));
		}
		HDescCol.put(chiave, "");
	}

	private void settaTitoliRiga(Vector vDesc, Vector vCod, NDOContainer cnt, NDOUtil unt) throws Exception {
		try {
			int dim = vCod.size();
			Vector vChiave = new Vector();

			for (int i = 0; i < vCod.size(); i++) {
				if (i == 0)
					vChiave.add(i, "" + vCod.elementAt(i));
				else {
					vChiave.add(i, ("" + vChiave.elementAt(i - 1)) + "|" + vCod.elementAt(i));
				}
			}

			if (dim == 0)
				cnt.setRowTitle(unt.mkPar("TOT"), "Totali generali");
			else {
				if (!h_1.containsKey("" + vChiave.elementAt(0))) {
					/*
					 * Se non ho la descrizione per il livello pi� alto
					 * certamente non cel'ho per i sottolivelli
					 */
					cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0)), "" + vDesc.elementAt(0));
					h_1.put("" + vChiave.elementAt(0), "" + vDesc.elementAt(0));
					if (dim > 1) {
						cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1)), ""
								+ vDesc.elementAt(1));
						h_2.put("" + vChiave.elementAt(1), "" + vDesc.elementAt(1));
						if (dim > 2) {
							cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
									+ vCod.elementAt(2)), "" + vDesc.elementAt(2));
							h_3.put("" + vChiave.elementAt(2), "" + vDesc.elementAt(2));
							if (dim > 3) {
								cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
										+ vCod.elementAt(2), "" + vCod.elementAt(3)), "" + vDesc.elementAt(3));
								h_4.put("" + vChiave.elementAt(3), "" + vDesc.elementAt(3));
								if (dim > 4) {
									cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
											+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4)), ""
											+ vDesc.elementAt(4));
									h_5.put("" + vChiave.elementAt(4), "" + vDesc.elementAt(4));
									if (dim > 5) {
										cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
												+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4), ""
												+ vCod.elementAt(5)), "" + vDesc.elementAt(5));
										h_6.put("" + vChiave.elementAt(5), "" + vDesc.elementAt(5));
									}
								}
							}
						}
					}// fine dim
				} else {
					if (dim > 1 && !h_2.containsKey("" + vChiave.elementAt(1))) {
						/* controllo i livelli inferiori */
						cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1)), ""
								+ vDesc.elementAt(1));
						h_2.put("" + vChiave.elementAt(1), "" + vDesc.elementAt(1));
						if (dim > 2) {
							cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
									+ vCod.elementAt(2)), "" + vDesc.elementAt(2));
							h_3.put("" + vChiave.elementAt(2), "" + vDesc.elementAt(2));
							if (dim > 3) {
								cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
										+ vCod.elementAt(2), "" + vCod.elementAt(3)), "" + vDesc.elementAt(3));
								h_4.put("" + vChiave.elementAt(3), "" + vDesc.elementAt(3));
								if (dim > 4) {
									cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
											+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4)), ""
											+ vDesc.elementAt(4));
									h_5.put("" + vChiave.elementAt(4), "" + vDesc.elementAt(4));
									if (dim > 5) {
										cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
												+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4), ""
												+ vCod.elementAt(5)), "" + vDesc.elementAt(5));
										h_6.put("" + vChiave.elementAt(5), "" + vDesc.elementAt(5));
									}
								}
							}
						}// fine dim
					} else {
						if (dim > 2 && !h_3.containsKey("" + vChiave.elementAt(2))) {
							cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
									+ vCod.elementAt(2)), "" + vDesc.elementAt(2));
							h_3.put("" + vChiave.elementAt(2), "" + vDesc.elementAt(2));
							if (dim > 3) {
								cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
										+ vCod.elementAt(2), "" + vCod.elementAt(3)), "" + vDesc.elementAt(3));
								h_4.put("" + vChiave.elementAt(3), "" + vDesc.elementAt(3));
								if (dim > 4) {
									cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
											+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4)), ""
											+ vDesc.elementAt(4));
									h_5.put("" + vChiave.elementAt(4), "" + vDesc.elementAt(4));
									if (dim > 5) {
										cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
												+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4), ""
												+ vCod.elementAt(5)), "" + vDesc.elementAt(5));
										h_6.put("" + vChiave.elementAt(5), "" + vDesc.elementAt(5));
									}
								}
							}// fine dim
						} else {
							if (dim > 3 && !h_4.containsKey("" + vChiave.elementAt(3))) {
								cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
										+ vCod.elementAt(2), "" + vCod.elementAt(3)), "" + vDesc.elementAt(3));
								h_4.put("" + vChiave.elementAt(3), "" + vDesc.elementAt(3));
								if (dim > 4) {
									cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
											+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4)), ""
											+ vDesc.elementAt(4));
									h_5.put("" + vChiave.elementAt(4), "" + vDesc.elementAt(4));
									if (dim > 5) {
										cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
												+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4), ""
												+ vCod.elementAt(5)), "" + vDesc.elementAt(5));
										h_6.put("" + vChiave.elementAt(5), "" + vDesc.elementAt(5));
									}
								}// fine dim
							} else {
								if (dim > 4 && !h_5.containsKey("" + vChiave.elementAt(4))) {
									cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
											+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4)), ""
											+ vDesc.elementAt(4));
									h_5.put("" + vChiave.elementAt(4), "" + vDesc.elementAt(4));
									if (dim > 5) {
										cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
												+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4), ""
												+ vCod.elementAt(5)), "" + vDesc.elementAt(5));
										h_6.put("" + vChiave.elementAt(5), "" + vDesc.elementAt(5));
									}// fine dim
								} else {
									if (dim > 5 && !h_6.containsKey("" + vChiave.elementAt(5))) {
										cnt.setRowTitle(unt.mkPar("" + vCod.elementAt(0), "" + vCod.elementAt(1), ""
												+ vCod.elementAt(2), "" + vCod.elementAt(3), "" + vCod.elementAt(4), ""
												+ vCod.elementAt(5)), "" + vDesc.elementAt(5));
										h_6.put("" + vChiave.elementAt(5), "" + vDesc.elementAt(5));
									}// fine dim
								}
							}
						}
					}
				}
			}// fine if dim=0
		} catch (Exception e) {
			debugMessage("FoInfElencoEJB.settaTitoliRiga: " + e);
			throw new Exception("Errore eseguendo settaTitoliRiga()");
		}
	}

	private String calcolaFascia(int eta) {
		boolean trovata = false;
		String retF = "NON_SPEC";
		Enumeration enuK = h_fasceEta.keys();

		while ((enuK.hasMoreElements()) && (!trovata)) {
			String kFascia = (String) enuK.nextElement();
			String vFascia = (String) h_fasceEta.get(kFascia);
			int iniFascia = Integer.parseInt(kFascia);
			int finFascia = Integer.parseInt(vFascia);
			trovata = ((eta >= iniFascia) && (eta <= finFascia));
			if (trovata)
				retF = kFascia;
		}
		return retF;
	}

	private void aggiungiFasceEta() {
		Enumeration enuK = h_fasceEta.keys();
		while (enuK.hasMoreElements()) {
			String kFascia = (String) enuK.nextElement();
			String vFascia = (String) h_fasceEta.get(kFascia);
			String codFascia = kFascia;
			// hColonne.put(kFascia, kFascia + "-" + vFascia);
			while (codFascia.length() < 3) {
				codFascia = " " + codFascia;
			}
			if (Integer.parseInt(vFascia) != 999)
				hColonne.put(kFascia, codFascia + "-" + vFascia);
			else
				hColonne.put(kFascia, codFascia + " e oltre");
		}
		// aggiungo fascia eta x valori "non specificati"
		hColonne.put("NON_SPEC", "NON SPECIF");
	}

	private Vector Aggiungi(Vector v, String campo, ISASRecord dbr) throws Exception {
		try {
			if (dbr.get(campo) != null)
				v.add("" + dbr.get(campo));
			return v;
		} catch (Exception e) {
			debugMessage("FoInfElencoEJB.Aggiungi(): " + e);
			throw new SQLException("Errore eseguendo Aggiungi()");
		}
	}

	private void caricaHashColonne(Hashtable par) {
		hColonne.put("F", "Femmine");
		hColonne.put("M", "Maschi");
		hColonne.put("TF", "Tot. Assis.");
		hColonne.put("TS", "Tot. Fasce");
		hColonne.put("Z", " ");
		if (dividiEta)
			aggiungiFasceEta();
	}

	private String DecodificaLiv3(ISASConnection dbc, String pca, String ragg) {
		String ret = "";
		try {
			if (pca != null) {
				if (pca.equals("") && !ragg.equals("A"))
					ret = " TUTTI";
				else if (pca.equals("") && ragg.equals("A"))
					ret = " TUTTE";
				else {
					ISASRecord dbr = null;
					String sel = "";
					if (ragg.equals("P")) {
						sel = "SELECT despres des FROM presidi WHERE" + " codpres='" + pca + "' AND codreg='"
								+ codice_regione + "' AND codazsan='" + codice_usl + "'";
						dbr = dbc.readRecord(sel);
					} else if (ragg.equals("C")) {
						sel = "SELECT descrizione des FROM comuni WHERE" + " codice='" + pca + "'";
						dbr = dbc.readRecord(sel);
					} else if (ragg.equals("A")) {
						sel = "SELECT descrizione des FROM areadis WHERE" + " codice='" + pca + "'";
						dbr = dbc.readRecord(sel);
					}
					
					if (dbr != null && dbr.get("des") != null)
						ret = (String) dbr.get("des");
				}
			}
		} catch (Exception ex) {
		}
		return ret;
	}

	private String DecodificaZona(ISASConnection dbc, String zona) {
		String ret = "";
		try {
			if (zona != null) {
				if (zona.equals(""))
					ret = "TUTTE";
				else {
					String sel = "SELECT descrizione_zona FROM zone WHERE" + " codice_zona='" + zona + "'";
					ISASRecord dbr = dbc.readRecord(sel);
					if (dbr != null && dbr.get("descrizione_zona") != null)
						ret = (String) dbr.get("descrizione_zona");
				}
			}
		} catch (Exception ex) {
		}
		return ret;
	}

	private String DecodificaDistretto(ISASConnection dbc, String distr) {
		String ret = "";
		try {
			if (distr != null) {
				if (distr.equals(""))
					ret = "TUTTI";
				else {
					String sel = "SELECT des_distr FROM distretti WHERE" + " cod_distr='" + distr + "'";
					ISASRecord dbr = dbc.readRecord(sel);
					if (dbr != null && dbr.get("des_distr") != null)
						ret = (String) dbr.get("des_distr");
				}
			}
		} catch (Exception ex) {
		}
		return ret;
	}

	// utility gsp

	private void stampaQuery(String punto, String query) {
		stampa(punto, "Query>" + query + "<");
	}

	private void stampa(String punto, String messaggio) {
		System.out.println(punto + "   " + messaggio);
	}

	private void stampa(String punto, Hashtable p) {
		System.out.println(punto + "\n Dati Hashtable>" + p + "<\n");
	}

	private void stampaNelDoc(String punto, String sezione, Hashtable p) {
		System.out.println(punto + "\nSezione da stampare>" + sezione + "<\n Dati>" + p + "<\n");
	}

	private void stampaInizio(String punto, Hashtable par) {
		stampa(punto, "Dati che ricevo>" + par + "<\n");
	}

} // End of FoInfElenco class

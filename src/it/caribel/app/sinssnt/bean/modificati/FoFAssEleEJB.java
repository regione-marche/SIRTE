package it.caribel.app.sinssnt.bean.modificati;
/**
 * CARIBEL S.r.l. - SINSS: produzione elenco assistiti per fisioterapisti
 *
 * 18/09/2003 - Francesco Greco
 */

import java.sql.*;
import java.util.*;

import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class FoFAssEleEJB extends SINSSNTConnectionEJB {

	public FoFAssEleEJB() {
	}

	String ragg = "";
	String dom_res;
	String dr;
	// hash per calcolare il numero totale di assistiti
	Hashtable hContaGeneraleS = new Hashtable();
	Hashtable hContaGeneraleA = new Hashtable();

	Hashtable hContaAssistitiA = new Hashtable();
	int totaleFis = 0;
	
	private static String UBI_LAZIO = "4";//elisa b  

	/**
	 * restituisce un parametro data come stringa nel formato gg/mm/aaaa
	 */
	private String getStringDate(Hashtable par, String k) {
		try {
			String s = (String) par.get(k);
			s = s.substring(8, 10) + "/" + s.substring(5, 7) + "/"
					+ s.substring(0, 4);
			return s;
		} catch (Exception e) {
			debugMessage("getStringDate(" + par + ", " + k + "): " + e);
			return "";
		}
	}

	/**
	 * restituisce un parametro come stringa
	 */
	private String getStringField(ISASRecord dbr, String f) {
		try {
			return (dbr.get(f)).toString();
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	/**
	 * restituisce un campo data come stringa
	 */
	private String getDateField(ISASRecord dbr, String f) {
		try {
			if (dbr.get(f) == null)
				return "";
			String d = ((java.sql.Date) dbr.get(f)).toString();
			d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/"
					+ d.substring(0, 4);
			return d;
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	private String getComune(ISASConnection dbc, String codice)
			throws Exception {
		String comune = "";
		try {
			if (!codice.equals("")) {
				String sel = "SELECT descrizione FROM comuni "
						+ "WHERE codice = '" + codice + "'";
				// debugMessage("FoEleSocEJB.getRagioneSociale(): "+sel);
				ISASRecord dbcom = dbc.readRecord(sel);
				comune = (String) dbcom.get("descrizione");
			}
		} catch (Exception e) {
			debugMessage("getComune(" + dbc + ", " + codice + "): " + e);
			return "";
		}
		return comune;
	}

	private String getMotivoUscita(ISASRecord dbr) {
		try {
			switch (new Integer(((String) dbr.get("skf_motivo_chius")))
					.intValue()) {
			case 1:
				return "DIMESSO";
			case 2:
				return "SOSPESO";
			case 3:
				return "RICOVERO OSPEDALIERO";
			case 4:
				return "R.S.A.";
			case 5:
				return "TRASFERITO";
			default:
				return " ";
			}
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * restituisce la select per la stampa sintetica.
	 */

	private String getSelectSintetica(ISASConnection dbc, Hashtable par) {
		String s = "SELECT DISTINCT c.n_cartella, "
				+ "c.cognome,c.nome,c.data_nasc," + "u.cod_zona,"
				+ "u.cod_distretto" + " as cod_distretto,"
				+ "u.des_zona descrizione_zona ,";

		if (par.get("oper") != null && par.get("oper").equals("SI"))
			s = s + "op.cognome cognome_oper,op.nome nome_oper,op.dipend_conv,";// 29-09-2003
																	// aggiunta
																	// divisione
																	// per
																	// operatore

		s = s
				+ "u.codice,u.descrizione,u.des_distretto des_distr"
				+ " FROM cartella c,anagra_c a,skfis skf,"
				+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc"
						: "ubicazioni_n") + " u";

		if (par.get("oper") != null && par.get("oper").equals("SI"))
			s = s + ",operatori op";
		try {
			String w = getSelectParteWhereSint(dbc, par);
			if (!w.equals(""))
				s = s + " WHERE " + w;
			
			/* elisa b 14/02/11 : x il lazio, si aggiunge il fitro sulla tipologia
			 * operatori */
			if(inLazio(dbc) && par.containsKey("tipoOperatore") && 
					(!par.get("tipoOperatore").toString().equals(""))){
				if (!w.equals(""))
					s = s + " AND op.tipo = " + par.get("tipoOperatore").toString();
				else
					s = s + " WHERE op.tipo = " + par.get("tipoOperatore").toString();
			}
				
		} catch (Exception e) {
			debugMessage("FoFAssEleEJB.getSelectSintetica(): " + e);
			e.printStackTrace();
		}
		s = s + " ORDER BY ";
		// 29-09-2003 aggiunta divisione per operatore
		if (par.get("oper") != null && par.get("oper").equals("SI"))
			s = s + " op.cognome,op.nome, ";

		s = s + " descrizione_zona,des_distr,descrizione,"
				+ "c.cognome,c.nome,c.n_cartella";
		debugMessage("FoFAssEleEJB.getSelectSintetica(): " + s);
		return s;
	}

	/**
	 * restituisce la parte where della select valorizzata secondo i parametri
	 * di ingresso.
	 */
	private String getSelectParteWhereSint(ISASConnection dbc, Hashtable par) {
		ServerUtility su = new ServerUtility();

		String myselect = "";

		String data_fine = "";
		// controllo data fine
		if (par.get("data_fine") != null) {
			data_fine = (String) (par.get("data_fine"));
			myselect = myselect + " skf.skf_data<="
					+ formatDate(dbc, data_fine);
		}

		// controllo data inizio
		if (par.get("data_inizio") != null) {
			String scr = (String) (par.get("data_inizio"));
			myselect = myselect
					+ " AND (skf.skf_data_chiusura is null OR skf.skf_data_chiusura>="
					+ formatDate(dbc, scr) + ")";
		}

		String livello=(String)(par.get("motivo"));	
	    if (livello!=null && !livello.equals("-1"))
	    	myselect=myselect+" AND skf.skf_motivo='"+livello+"'";
	    
		// 29-09-2003 aggiunta divisione per operatore
		if (par.get("oper") != null && par.get("oper").equals("SI"))
			myselect = myselect + " AND skf.skf_operatore = op.codice ";

		String cod_inizio = "";
		// controllo codice inizio operatore
		if (par.get("codice_inizio") != null) {
			cod_inizio = (String) (par.get("codice_inizio"));
			if (!cod_inizio.equals(""))
				//myselect = myselect + " AND skf.skf_operatore>='" + cod_inizio
					//	+ "'";
				myselect = myselect + " AND skf.skf_operatore='" + cod_inizio
				+ "'";
		}

		String cod_fine = "";
		// controllo codice fine operatore
		if (par.get("codice_fine") != null) {
			cod_fine = (String) (par.get("codice_fine"));
			if (!cod_fine.equals(""))
				myselect = myselect + " AND skf.skf_operatore<='" + cod_fine
						+ "'";
		}

		/*ragg = (String) par.get("ragg");
		myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,
				ragg);

		// if (ragg!=null && ragg.equals("C"))
		// myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
		// " AND u.codice=a.dom_citta)"+
		// " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
		// " AND u.codice=a.citta))";
		// else if (ragg!=null && ragg.equals("A"))
		// myselect +=
		// " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
		// " AND u.codice=a.dom_areadis)"+
		// " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
		// " AND u.codice=a.areadis))";

		// Aggiunto Controllo Domicilio/Residenza (BYSP)
		if ((String) par.get("dom_res") == null) {
			if (ragg.equals("C"))
				myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"
						+ " AND u.codice=a.dom_citta)"
						+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "
						+ " AND u.codice=a.citta))";
			else if (ragg.equals("A"))
				myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"
						+ " AND u.codice=a.dom_areadis)"
						+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "
						+ " AND u.codice=a.areadis))";
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.dom_citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.dom_areadis";
		}

		else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.areadis";
		}

		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));*/

		String condWhere = getFiltroUbicazione(par, su);
        myselect=myselect+ " AND " + condWhere;
        
		myselect += " AND a.n_cartella=c.n_cartella"
				+ " AND skf.n_cartella=a.n_cartella"
				+ " AND skf.n_cartella=c.n_cartella"
				+ " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"
				+ " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";

		return myselect;
	}

	/**
	 * restituisce la select per la stampa analitica.
	 */
	private String getSelectAnalitica(ISASConnection dbc, Hashtable par) {
		String s = "SELECT c.n_cartella, "
				+ "c.cognome,c.nome,c.data_nasc,"
				+ "a.dom_citta,a.dom_indiriz,a.indirizzo,a.citta,a.nome_camp, a.telefono1, "
				+ "skf.skf_data, skf.skf_data_chiusura, skf.skf_motivo_chius,"
				+ "skf.skf_operatore,mecogn, menome, ";

		if (par.get("oper") != null && par.get("oper").equals("SI"))
			s = s + "op.cognome cognome_oper,op.nome nome_oper, op.dipend_conv,";// 29-09-2003 aggiunta divisione per operatore
				s = s + "u.cod_zona,"
				+ "u.cod_distretto"
				+ " as cod_distretto,"
				+ "u.des_zona descrizione_zona ,"
				+ "u.codice,u.descrizione,u.des_distretto des_distr"
				+ " FROM cartella c,anagra_c a, medici m, skfis skf,"
				+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc"
						: "ubicazioni_n") + " u";

		if (par.get("oper") != null && par.get("oper").equals("SI"))
			s = s + ",operatori op";
		try {
			String w = getSelectParteWhereAna(dbc, par);
			if (!w.equals(""))
				s = s + " WHERE " + w;
			/* elisa b 14/02/11 : x il lazio, si aggiunge il fitro sulla tipologia
			 * operatori */
			if(inLazio(dbc) && par.containsKey("tipoOperatore") && 
					(!par.get("tipoOperatore").toString().equals(""))){
				if (!w.equals(""))
					s = s + " AND op.tipo = " + par.get("tipoOperatore").toString();
				else
					s = s + " WHERE op.tipo = " + par.get("tipoOperatore").toString();
			}
			
		} catch (Exception e) {
			debugMessage("FoFAssEleEJB.getSelectAnalitica(): " + e);
			e.printStackTrace();
		}
		s = s + " ORDER BY ";
		// 29-09-2003 aggiunta divisione per operatore
		if (par.get("oper") != null && par.get("oper").equals("SI"))
			s = s + " op.cognome,op.nome, ";

		s = s + " descrizione_zona,des_distr,descrizione,"
				+ "c.cognome,c.nome,c.n_cartella";
		debugMessage("FoFAssEleEJB.getSelectAnalitica(): " + s);
		return s;
	}

	/**
	 * restituisce la parte where della select valorizzata secondo i parametri
	 * di ingresso.
	 */
	private String getSelectParteWhereAna(ISASConnection dbc, Hashtable par) {
		ServerUtility su = new ServerUtility();

		String myselect = "";

		String data_fine = "";
		// controllo data fine
		if (par.get("data_fine") != null) {
			data_fine = (String) (par.get("data_fine"));
			myselect = myselect + " skf.skf_data<="
					+ formatDate(dbc, data_fine);
		}

		// controllo data inizio
		if (par.get("data_inizio") != null) {
			String scr = (String) (par.get("data_inizio"));
			myselect = myselect
					+ " AND (skf.skf_data_chiusura is null OR skf.skf_data_chiusura>="
					+ formatDate(dbc, scr) + ")";
		}
		
		String livello=(String)(par.get("motivo"));
	    if (livello!=null && !livello.equals("-1"))
	 	   myselect=myselect+" AND skf.skf_motivo='"+livello+"'";
	    
		// 29-09-2003 aggiunta divisione per operatore
		if (par.get("oper") != null && par.get("oper").equals("SI"))
			myselect = myselect + " AND skf.skf_operatore = op.codice ";

		String cod_inizio = "";
		// controllo codice inizio operatore
		if (par.get("codice_inizio") != null) {
			cod_inizio = (String) (par.get("codice_inizio"));
			if (!cod_inizio.equals(""))
				//myselect = myselect + " AND skf.skf_operatore>='" + cod_inizio
					//	+ "'";
				myselect = myselect + " AND skf.skf_operatore='" + cod_inizio
				+ "'";
		}

		String cod_fine = "";
		// controllo codice fine operatore
		if (par.get("codice_fine") != null) {
			cod_fine = (String) (par.get("codice_fine"));
			if (!cod_fine.equals(""))
				myselect = myselect + " AND skf.skf_operatore<='" + cod_fine
						+ "'";
		}

		/*ragg = (String) par.get("ragg");
		myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,
				ragg);

		// if (ragg!=null && ragg.equals("C"))
		// myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
		// " AND u.codice=a.dom_citta)"+
		// " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
		// " AND u.codice=a.citta))";
		// else if (ragg!=null && ragg.equals("A"))
		// myselect +=
		// " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
		// " AND u.codice=a.dom_areadis)"+
		// " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
		// " AND u.codice=a.areadis))";

		// Aggiunto Controllo Domicilio/Residenza (BYSP)
		if ((String) par.get("dom_res") == null) {
			if (ragg.equals("C"))
				myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"
						+ " AND u.codice=a.dom_citta)"
						+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "
						+ " AND u.codice=a.citta))";
			else if (ragg.equals("A"))
				myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"
						+ " AND u.codice=a.dom_areadis)"
						+ " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "
						+ " AND u.codice=a.areadis))";
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.dom_citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.dom_areadis";
		}

		else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				myselect += " AND u.codice=a.citta";
			else if (ragg.equals("A"))
				myselect += " AND u.codice=a.areadis";
		}
		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));*/
		
		String condWhere = getFiltroUbicazione(par, su);
        myselect=myselect+ " AND " + condWhere;

		myselect += " AND a.n_cartella=c.n_cartella"
				+ " AND m.mecodi=a.cod_med"
				+ " AND skf.n_cartella=a.n_cartella"
				+ " AND skf.n_cartella=c.n_cartella"
				+ " AND a.data_variazione IN (SELECT MAX (anagra_c.data_variazione)"
				+ " FROM anagra_c WHERE anagra_c.n_cartella=c.n_cartella)";

		return myselect;
	}

	/**
	 * stampa sintetica-analitica: sezione layout del documento
	 */
	private void mkLayout(ISASConnection dbc, Hashtable par, mergeDocument doc) {

		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();

		ht.put("#txt#", getConfStringField(dbc, "SINS", "ragione_sociale",
				"conf_txt"));
		ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
		ht.put("#data_fine#", getStringDate(par, "data_fine"));
		ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

		doc.writeSostituisci("layout", ht);
	}
	
	public String getFiltroUbicazione(Hashtable<String, String> par, ServerUtility su) {

		String condWhere = "";
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));
		String raggruppamento = (String) par.get("ragg");
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, raggruppamento);
		String dom_res = ISASUtil.getValoreStringa(par, "dom_res");
		if (!ISASUtil.valida(dom_res)) {
			if (raggruppamento.equals("C")) {
				condWhere += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
						+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
			} else if (raggruppamento.equals("A")) {
				condWhere += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"
						+ " AND u.codice=a.dom_areadis)" + " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "
						+ " AND u.codice=a.areadis))";
			} else if (raggruppamento.equals("P")) {
				condWhere += " AND u.codice  = skf.skf_cod_presidio ";
			}
		} else if (dom_res.equals("D")) {
			if (raggruppamento.equals("C")) {
				condWhere += " AND u.codice=a.dom_citta";
			} else if (raggruppamento.equals("A")) {
				condWhere += " AND u.codice=a.dom_areadis";
			} else if (raggruppamento.equals("P")) {
				condWhere += " AND u.codice  = skf.skf_cod_presidio ";
			}
		} else if (dom_res.equals("R")) {
			if (raggruppamento.equals("C")) {
				condWhere += " AND u.codice=a.citta";
			} else if (raggruppamento.equals("A")) {
				condWhere += " AND u.codice=a.areadis";
			} else if (raggruppamento.equals("P")) {
				condWhere += " AND u.codice  = skf.skf_cod_presidio ";
			}
		}
		return condWhere;
	}


	/**
	 * stampa sintetica: sezione coordinate geografiche
	 */
	private void mkCoordinate(mergeDocument doc, Hashtable par,
			String cur_zona, String cur_dist, String cur_comu) {

		Hashtable ht = new Hashtable();
		ht.put("#descrizione_zona#", cur_zona);
		ht.put("#des_distr#", cur_dist);

		String ragg = faiRaggruppamento((String) par.get("ragg"));
		ht.put("#tipologia#", ragg);
		ht.put("#descrizione#", cur_comu);
		doc.writeSostituisci("zona", ht);
	}

	/**
	 * stampa sintetica: sezione nuovo operatore 29-09-2003 aggiunta divisione
	 * per operatore
	 */
	private void mkTaglioOperatore(mergeDocument doc, Hashtable par,
			String cur_oper) {

		Hashtable ht = new Hashtable();
		ht.put("#descrizione_operatore#", cur_oper);
		doc.writeSostituisci("operatore", ht);
	}

	/**
	 * stampa analitica: sezione totalemedico 29-09-2003 aggiunta divisione per
	 * operatore
	 */
	private void mkTotaleMed(mergeDocument doc) {
		Hashtable ht = new Hashtable();
		ht.put("#descrizioneTotMed#",
				" Totale n. assistiti per Fisioterapista: ");
		// ht.put("#totaleMed#", " "+tot);
		ht.put("#totaleMed#", " " + totaleFis);
		totaleFis = 0;
		doc.writeSostituisci("totaleMed", ht);
	}

	/**
	 * stampa sintetica: sezione totalemedico 29-09-2003 aggiunta divisione per
	 * operatore
	 */
	private void mkTotaleMed(mergeDocument doc, int tot) {
		Hashtable ht = new Hashtable();
		ht.put("#descrizioneTotMed#",
				" Totale n. assistiti per Fisioterapista: ");
		ht.put("#totaleMed#", " " + tot);
		doc.writeSostituisci("totaleMed", ht);
	}

	/**
	 * stampa sintetica: sezione pagina nuova 29-09-2003 aggiunta divisione per
	 * operatore
	 */
	private void mkPaginaNuova(mergeDocument doc) {
		doc.write("taglia");
	}

	/**
	 * stampa sintetica: sezione inizio tabella
	 */
	private void mkIniziaTabella(mergeDocument doc, ISASRecord dbr) {
		Hashtable ht = new Hashtable();
		if (this.dom_res == null) {

			ht.put("#dom_res#", "Domicilio");
			doc.writeSostituisci("iniziotab", ht);
		} else {

			ht.put("#dom_res#", this.dr);
			doc.writeSostituisci("iniziotab", ht);
		}

	}

	/**
	 * stampa sintetica: sezione riga tabella
	 */
	private void mkSinteticaRigaTabella(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc) {

		Hashtable ht = new Hashtable();
		try {
			ht.put("#assistito#", (String) dbr.get("cognome") + " "
					+ (String) dbr.get("nome"));
			ht.put("#data_nasc#", getDateField(dbr, "data_nasc"));
			if (dbr.get("n_cartella") == null)
				ht.put("#cartella#", " ");
			else
				ht.put("#cartella#", ((Integer) dbr.get("n_cartella"))
						.toString());

			hContaGeneraleS.put(((Integer) dbr.get("n_cartella")).toString(),
					"");
		} catch (Exception e) {
			ht.put("#assistito#", "*** ERRORE ***");
			ht.put("#data_nasc#", " ");
			ht.put("#cartella#", " ");
		}
		doc.writeSostituisci("tabella", ht);
	}

	/**
	 * stampa sintetica: sezione fine tabella
	 */
	private void mkFineTabella(mergeDocument doc, int conta) {
		doc.write("finetab");

		Hashtable ht = new Hashtable();
		ht.put("#descrizione#", " Totale n. assistiti: ");
		ht.put("#totale#", " " + conta);
		doc.writeSostituisci("totale", ht);// //COMMENTATO IL 04-07-03
	}

	/**
	 * stampa analitica: sezione fine tabella
	 */
	private void mkFineTabellaA(mergeDocument doc) {
		doc.write("finetab");

		Hashtable ht = new Hashtable();
		ht.put("#descrizione#", " Totale n. assistiti: ");
		// ht.put("#totale#", " "+conta);
		ht.put("#totale#", " " + hContaAssistitiA.size());
		totaleFis = totaleFis + hContaAssistitiA.size();
		hContaAssistitiA.clear();
		doc.writeSostituisci("totale", ht);// //COMMENTATO IL 04-07-03
	}

	/**
	 * stampa sintetica: corpo
	 */
	private void mkSinteticaBody(ISASConnection dbc, ISASCursor dbcur,
			Hashtable par, mergeDocument doc) throws Exception {

		boolean first_time = true;
		// 29-09-2003 aggiunta divisione per operatore
		String old_oper = "*", cur_oper = "";

		String old_zona = "*", cur_zona = "";
		String old_dist = "*", cur_dist = "";
		String old_comu = "*", cur_comu = "";
		int conta = 0;
		int contaTot = 0;
		int contaAssMed = 0;
		try {
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				// 29-09-2003 aggiunta divisione per operatore
				if (par.get("oper") != null && par.get("oper").equals("SI")) {
					String tipo_operatore=(String)dbr.get("dipend_conv");
                    String tipo_op="";
                    if (tipo_operatore!=null && !tipo_operatore.equals("")){
                  	  if (tipo_operatore.equals("D"))
                  		  tipo_op="Operatore dipendente";
                  	  else if (tipo_operatore.equals("C"))
                  		  tipo_op="Operatore convenzionato";
                    }  
					cur_oper = (String) dbr.get("cognome_oper") + " "
							+ (String) dbr.get("nome_oper") + " " + tipo_op;
				}
				cur_zona = (String) dbr.get("descrizione_zona");
				cur_dist = (String) dbr.get("des_distr");
				cur_comu = (String) dbr.get("descrizione");
				if ((par.get("oper") != null && par.get("oper").equals("SI") && !old_oper
						.equals(cur_oper))
						|| ((!old_zona.equals(cur_zona))
								|| (!old_dist.equals(cur_dist)) || (!old_comu
								.equals(cur_comu)))) {
					/*
					 * if (first_time) { first_time = ! first_time; } else {
					 */
					if (!first_time) {
						// chiudi tabella precedente
						mkFineTabella(doc, conta);
						conta = 0;
					}
					// 29-09-2003 aggiunta divisione per operatore
					if (par.get("oper") != null && par.get("oper").equals("SI")
							&& !old_oper.equals(cur_oper)) {
						if (!first_time) {
							// System.out.println("pagina nuova");
							mkTotaleMed(doc, contaAssMed);
							mkPaginaNuova(doc);
							contaAssMed = 0;
						}
						mkTaglioOperatore(doc, par, cur_oper);
						// intestazione cambio coordinate
						mkCoordinate(doc, par, cur_zona, cur_dist, cur_comu);
					} else if ((!old_zona.equals(cur_zona))
							|| (!old_dist.equals(cur_dist))
							|| (!old_comu.equals(cur_comu))) {
						// intestazione cambio coordinate
						mkCoordinate(doc, par, cur_zona, cur_dist, cur_comu);
					}
					// apri tabella con muovo operatore
					mkIniziaTabella(doc, dbr);
				}
				// 29-09-2003 aggiunta divisione per operatore
				old_oper = cur_oper;
				old_zona = cur_zona;
				old_dist = cur_dist;
				old_comu = cur_comu;

				if (first_time) {
					first_time = !first_time;
				}
				// stampa riga
				mkSinteticaRigaTabella(dbc, dbr, doc);
				conta++;
				contaAssMed++;
				contaTot++;
			}
			if (!first_time) {
				// chiudi tabella precedente
				mkFineTabella(doc, conta);
				if (par.get("oper") != null && par.get("oper").equals("SI"))
					mkTotaleMed(doc, contaAssMed);
				Hashtable htot = new Hashtable();
				htot.put("#descrizione#", " Totale generale assistiti: ");
				htot.put("#totale#", " " + hContaGeneraleS.size());
				doc.writeSostituisci("totale", htot);
			}
		} catch (Exception e) {
			debugMessage("FoFAssEleEJB.mkSinteticaBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkSinteticaBody()");
		}
	}

	/**
	 * stampa sintetica: entry point
	 */
	public byte[] query_fissint(String utente, String passwd, Hashtable par,
			mergeDocument doc) throws SQLException {

		ISASConnection dbc = null;
		boolean done = false;
		try {

			this.dom_res = (String) par.get("dom_res");
			if (this.dom_res != null) {
				if (this.dom_res.equals("R"))
					this.dr = "Residenza";
				else if (this.dom_res.equals("D"))
					this.dr = "Domicilio";
			}

			myLogin lg = new myLogin();
			String selectedLanguage = (String)par.get(FileMaker.printParamLang);
			lg.put(utente,passwd,selectedLanguage);
			
			//lg.put(utente, passwd);
			dbc = super.logIn(lg);
			ISASCursor dbcur = dbc.startCursor(getSelectSintetica(dbc, par));
			mkLayout(dbc, par, doc);
			if (dbcur == null) {
				doc.write("messaggio");
				doc.write("finale");
				debugMessage("FoFAssEleEJB.query_fissint(): "
						+ "cursore nullo.");
			} else {
				if (dbcur.getDimension() <= 0) {
					doc.write("messaggio");
				} else {
					mkSinteticaBody(dbc, dbcur, par, doc);
				}
				doc.write("finale");
			}
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;

			doc.close();
			byte[] rit = (byte[]) doc.get();
			// debugMessage("FoFAssEleEJB.query_infesint(): documento ["+
			// (new String(rit))+"]");
			return rit;
		} catch (Exception e) {
			debugMessage("FoFAssEleEJB.query_fissint(): " + e);
			throw new SQLException("Errore eseguendo query_fissint()");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					debugMessage("FoFAssEleEJB.query_fissint(): " + e1);
				}
			}
		}
	}

	/**
	 * stampa analitica: sezione riga tabella
	 */
	private void mkAnaliticaRigaTabella(ISASConnection dbc, ISASRecord dbr,
			mergeDocument doc, Hashtable par) {

		Hashtable ht1 = new Hashtable();
		Hashtable ht2 = new Hashtable();
		Hashtable ht3 = new Hashtable();
		String n_cartella = "";
		String nome_camp = "";
		try {
			if (hContaAssistitiA.containsKey(((Integer) dbr.get("n_cartella"))
					.toString()) == false) {
				hContaAssistitiA.put(((Integer) dbr.get("n_cartella"))
						.toString(), "");

				ht1.put("#assistito#", (String) dbr.get("cognome") + " "
						+ (String) dbr.get("nome"));
				ht1.put("#data_nasc#", getDateField(dbr, "data_nasc"));
				try {
					n_cartella = ((Integer) dbr.get("n_cartella")).toString();
				} catch (Exception e) {
					n_cartella = "";
				}
				ht1.put("#cartella#", n_cartella);

				if (this.dom_res == null) {
					ht2.put("#indirizzo#", (String) dbr.get("dom_indiriz"));
					ht2.put("#citta#", getComune(dbc, (String) dbr
							.get("dom_citta")));
				} else {
					if (this.dom_res.equals("R")) {
						ht2.put("#indirizzo#", (String) dbr.get("indirizzo"));
						ht2.put("#citta#", getComune(dbc, (String) dbr
								.get("citta")));
					} else if (this.dom_res.equals("D")) {
						ht2.put("#indirizzo#", (String) dbr.get("dom_indiriz"));
						ht2.put("#citta#", getComune(dbc, (String) dbr
								.get("dom_citta")));
					}
				}
				// ht2.put("#indirizzo#",
				// (String)dbr.get("dom_indiriz"));
				// ht2.put("#citta#",
				// getComune(dbc,(String)dbr.get("dom_citta")));
				ht2.put("#telefono1#", (String) dbr.get("telefono1"));

				if (!((String) dbr.get("nome_camp")).equals(""))
					nome_camp = "(" + (String) dbr.get("nome_camp") + ")";
				ht2.put("#nome_camp#", nome_camp);

				ht2.put("#medico#", (String) dbr.get("mecogn") + " "
						+ (String) dbr.get("menome"));
				// ***
			} else {
				ht1.put("#assistito#", "");
				ht1.put("#data_nasc#", "");
				ht1.put("#cartella#", "");
				ht2.put("#indirizzo#", "");
				ht2.put("#citta#", "");
				ht2.put("#telefono1#", "");
				ht2.put("#nome_camp#", "");
				ht2.put("#medico#", "");
			}

			ht3.put("#skf_data#", getDateField(dbr, "skf_data"));
			ht3.put("#skf_data_chiusura#", getDateField(dbr,
					"skf_data_chiusura"));
			ht3.put("#skf_motivo_chius#", getMotivoUscita(dbr));
			// ***

			hContaGeneraleA.put(((Integer) dbr.get("n_cartella")).toString(),
					"");
		} catch (Exception e) {
			ht1.put("#assistito#", "*** ERRORE ***");
			ht1.put("#data_nasc#", " ");
			ht1.put("#cartella#", n_cartella);
			ht2.put("#indirizzo#", "*** ERRORE ***");
			ht2.put("#citta#", " ");
			ht2.put("#nome_camp#", " ");
			ht2.put("#telefono1#", " ");
			ht2.put("#medico#", " ");

			ht3.put("#skf_data#", " ");
			ht3.put("#skf_data_chiusura#", " ");
			ht3.put("#skf_motivo_chius#", " ");
		}
		doc.writeSostituisci("assistito1", ht1);
		doc.writeSostituisci("contatti", ht3);
		// mkAnaliticaContatti(dbc, n_cartella, doc, par);
		doc.writeSostituisci("assistito2", ht2);
	}

	/**
	 * stampa analitica: sezione blocco ripetitivo "CONTATTI" riga tabella
	 */
	private void mkAnaliticaContatti(ISASConnection dbc, String n_cartella,
			mergeDocument doc, Hashtable par) {
		Hashtable ht = new Hashtable();
		try {
			ISASCursor dbcur = dbc.startCursor(getSelectContatti(dbc, par,
					n_cartella));

			if (dbcur == null) {
				ht.put("#skf_data#", " ");
				ht.put("#skf_data_chiusura#", " ");
				ht.put("#skf_motivo_chius#", " ");
				doc.writeSostituisci("contatti", ht);
			} else {
				while (dbcur.next()) {
					ISASRecord dbinf = dbcur.getRecord();
					ht.put("#skf_data#", getDateField(dbinf, "data_inizio"));
					ht.put("#skf_data_chiusura#", getDateField(dbinf,
							"data_fine"));
					ht.put("#skf_motivo_chius#", getMotivoUscita(dbinf));
					doc.writeSostituisci("contatti", ht);
				}
			}
			dbcur.close();
		} catch (Exception e) {
			ht.put("#skf_data#", "*** ERRORE ***");
			ht.put("#skf_data_chiusura#", " ");
			ht.put("#skf_motivo_chius#", " ");
			doc.writeSostituisci("contatti", ht);
		}
	}

	/**
	 * stampa analitica: corpo
	 */
	private void mkAnaliticaBody(ISASConnection dbc, ISASCursor dbcur,
			Hashtable par, mergeDocument doc) throws Exception {

		boolean first_time = true;
		// 29-09-2003 aggiunta divisione per operatore
		String old_oper = "*", cur_oper = "";

		String old_zona = "*", cur_zona = "";
		String old_dist = "*", cur_dist = "";
		String old_comu = "*", cur_comu = "";
		// int conta = 0;
		// int contaTot = 0;
		// int contaAssMed=0;
		try {
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				// 29-09-2003 aggiunta divisione per operatore
				if (par.get("oper") != null && par.get("oper").equals("SI")) {
					String tipo_operatore=(String)dbr.get("dipend_conv");
                    String tipo_op="";
                    if (tipo_operatore!=null && !tipo_operatore.equals("")){
                  	  if (tipo_operatore.equals("D"))
                  		  tipo_op="Operatore dipendente";
                  	  else if (tipo_operatore.equals("C"))
                  		  tipo_op="Operatore convenzionato";
                    }  
					cur_oper = (String) dbr.get("cognome_oper") + " "
							+ (String) dbr.get("nome_oper") + " " + tipo_op;
				}
				cur_zona = (String) dbr.get("descrizione_zona");
				cur_dist = (String) dbr.get("des_distr");
				cur_comu = (String) dbr.get("descrizione");
				boolean flag = (!old_zona.equals(cur_zona))
						|| (!old_dist.equals(cur_dist))
						|| (!old_comu.equals(cur_comu));
				// 29-09-2003 aggiunta divisione per operatore
				if ((par.get("oper") != null && par.get("oper").equals("SI") && !old_oper
						.equals(cur_oper))
						|| (flag)) {
					if (!first_time) {
						mkFineTabellaA(doc);
						// conta = 0;
					}
					// 29-09-2003 aggiunta divisione per operatore
					if (par.get("oper") != null && par.get("oper").equals("SI")
							&& !old_oper.equals(cur_oper)) {
						if (!first_time) {
							mkTotaleMed(doc);
							mkPaginaNuova(doc);
							// contaAssMed=0;
						}
						mkTaglioOperatore(doc, par, cur_oper);
						mkCoordinate(doc, par, cur_zona, cur_dist, cur_comu);
					} else if (flag) {
						mkCoordinate(doc, par, cur_zona, cur_dist, cur_comu);
					}
					mkIniziaTabella(doc, dbr);
				}
				// 29-09-2003 aggiunta divisione per operatore
				old_oper = cur_oper;

				old_zona = cur_zona;
				old_dist = cur_dist;
				old_comu = cur_comu;
				if (first_time) {
					first_time = !first_time;
				}

				// stampa riga
				mkAnaliticaRigaTabella(dbc, dbr, doc, par);
				// conta++;
				// contaTot++;
				// contaAssMed++;
			}
			if (!first_time) {
				// chiudi tabella precedente
				mkFineTabellaA(doc);
				if (par.get("oper") != null && par.get("oper").equals("SI"))
					mkTotaleMed(doc);
				Hashtable htot = new Hashtable();
				htot.put("#descrizione#", " Totale generale assistiti: ");
				htot.put("#totale#", " " + hContaGeneraleA.size());
				doc.writeSostituisci("totale", htot);
			}
		} catch (Exception e) {
			debugMessage("FoFAssEleEJB.mkSinteticaBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkSinteticaBody()");
		}
	}

	/**
	 * stampa analitica: entry point
	 */
	public byte[] query_fisio(String utente, String passwd, Hashtable par,
			mergeDocument doc) throws SQLException {

		ISASConnection dbc = null;
		boolean done = false;
		try {

			this.dom_res = (String) par.get("dom_res");
			if (this.dom_res != null) {
				if (this.dom_res.equals("R"))
					this.dr = "Residenza";
				else if (this.dom_res.equals("D"))
					this.dr = "Domicilio";
			}
			System.out.println(this.dr + " " + this.dom_res);

			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			ISASCursor dbcur = dbc.startCursor(getSelectAnalitica(dbc, par));
			mkLayout(dbc, par, doc);
			if (dbcur == null) {
				doc.write("messaggio");
				doc.write("finale");
				debugMessage("FoFAssEleEJB.query_fisio(): " + "cursore nullo.");
			} else {
				if (dbcur.getDimension() <= 0) {
					doc.write("messaggio");
				} else {
					mkAnaliticaBody(dbc, dbcur, par, doc);
				}
				doc.write("finale");
			}
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;

			doc.close();
			byte[] rit = (byte[]) doc.get();
			// debugMessage("FoFAssEleEJB.query_fisio(): documento ["+
			// (new String(rit))+"]");
			return rit;
		} catch (Exception e) {
			debugMessage("FoFAssEleEJB.query_inferef(): " + e);
			throw new SQLException("Errore eseguendo query_fisio()");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					debugMessage("FoFAssEleEJB.query_fisio(): " + e1);
				}
			}
		}
	}

	private String getSelectContatti(ISASConnection dbc, Hashtable par,
			String n_cartella) {

		String selConta = "";
		String dt_ini = (String) par.get("data_inizio");
		String dt_fine = (String) par.get("data_fine");

		selConta = "SELECT skf_data data_inizio, "
				+ "skf_data_chiusura data_fine,skf_motivo_chius FROM skfis s "
				+ "WHERE s.n_cartella = " + n_cartella + " AND s.skf_data <= "
				+ formatDate(dbc, dt_fine)
				+ " AND (s.skf_data_chiusura IS NULL"
				+ " OR s.skf_data_chiusura = " + formatDate(dbc, "1000-01-01")
				+ " OR s.skf_data_chiusura >=" + formatDate(dbc, dt_ini) + ")";

		// System.out.println("Select Contatti"+selConta);
		return selConta;
	}

	private String faiRaggruppamento(String tipo) {
		String raggruppa = "";
		if (this.dom_res == null) {
			if (tipo.trim().equals("A"))
				raggruppa = "Area Distr.";
			else if (tipo.trim().equals("C"))
				raggruppa = "Comune";
			else
				raggruppa = "TIPO NON VALIDO";
		} else if (this.dom_res.equals("D")) {
			if (tipo.trim().equals("A"))
				raggruppa = "Area Distr. di Domicilio";
			else if (tipo.trim().equals("C"))
				raggruppa = "Comune di Domicilio";
			else
				raggruppa = "TIPO NON VALIDO";
		} else if (this.dom_res.equals("R")) {
			if (tipo.trim().equals("A"))
				raggruppa = "Area Distr. di Residenza";
			else if (tipo.trim().equals("C"))
				raggruppa = "Comune di Residenza";
			else
				raggruppa = "TIPO NON VALIDO";
		}
		// if(tipo.trim().equals("C"))
		// raggruppa=" Comune ";
		// else if(tipo.equals("A"))
		// raggruppa=" Area ";

		return raggruppa;
	}

	private boolean inLazio(ISASConnection dbc) throws Exception {
		String ubicazione = "";

		String select = "SELECT conf_txt" + " FROM conf"
				+ " WHERE conf_kproc = 'SINS'" + " AND conf_key = 'ADRSA_UBIC'";

		ISASRecord dbr = dbc.readRecord(select);
		if ((dbr != null) && (!dbr.get("conf_txt").equals("")))
			ubicazione = dbr.get("conf_txt").toString();

		return ubicazione.equals(UBI_LAZIO);
	}

} // End of FoFAssEle class

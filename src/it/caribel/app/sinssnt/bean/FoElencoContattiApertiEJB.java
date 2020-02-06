package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
//
// elisa b 03/10/11
// ==========================================================================

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Hashtable;

public class FoElencoContattiApertiEJB extends SINSSNTConnectionEJB {
	
	private String dtInizio = null; 
	private String dtFine = null; 

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
	


	public FoElencoContattiApertiEJB() {
	}

	
	private void mkLayout(ISASConnection dbc, Hashtable par, mergeDocument doc) throws Exception {

		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String conPiano = (String) par.get("con_piano"); 
		String complessita = (String) par.get("compl_ass"); 

		ht.put("#txt#", getConfStringField(dbc, "SINS", "ragione_sociale",
				"conf_txt"));

		ht.put("#d1#", getStringDate(par, "d1"));
		ht.put("#d2#", getStringDate(par, "d2"));
		ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

		if (conPiano.equals("S"))
			ht.put("#tipo#", "PER PRESENZA DI PIANO");
		else
			ht.put("#tipo#", "");
		
		if (!complessita.equals(""))
			ht.put("#filtri#", "Livello di complessita' : " + ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
					"COMPLASS", complessita,"tab_descrizione"));
		else
			ht.put("#filtri#", "");

		doc.writeSostituisci("layout", ht);
	}

	
	
	/**
	 * 
	 * @param dbc
	 * @param dbr
	 * @param cartella
	 * @throws Exception
	 */
	private void recuperaDatiAssistito(ISASConnection dbc, ISASRecord dbr,
			String cartella) throws Exception{
		ISASRecord dbrinfoAss = null;

		String query = "SELECT cognome, nome"
				+ " FROM cartella"
				+ " WHERE n_cartella = " + cartella;
	
		dbrinfoAss = dbc.readRecord(query);
		if(dbrinfoAss != null){
			dbr.put("nome", dbrinfoAss.get("nome").toString());
			dbr.put("cognome", dbrinfoAss.get("cognome").toString());
		}
		
		//System.out.println("recuperaDatiAssistito " + query);
	}
	
	
	
	
	
	private void mkBody(mergeDocument md, ISASCursor dbcur, Hashtable par,
			ISASConnection dbc) throws Exception {
		String zona = "";
		String distretto = "";
		String areadis = "";
		String zonaOld = "";
		String distrettoOld = "";
		String areadisOld = "";
		
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			
			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();
			
			if((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld))
					|| (!areadis.equals(areadisOld))){
				if(!zonaOld.equals(""))
					md.write("finetab");
				
				Hashtable hZ = new Hashtable();
				hZ.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
				hZ.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
				hZ.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
				md.writeSostituisci("zona", hZ);
				
				zonaOld = zona;
				distrettoOld = distretto;
				areadisOld = areadis;
				
				md.write("iniziotab");
			}		
			
			String cartella = dbr.get("n_cartella").toString();
			recuperaDatiAssistito(dbc, dbr, cartella);
			Integer tipo = (Integer) dbr.get("tipo");			

			Hashtable htAss = new Hashtable();
			htAss.put("#n_cartella#", cartella);
			htAss.put("#cognome#", "");
			htAss.put("#nome#", "");
			htAss.put("#complessita#", "");
			if(dbr.get("cognome") != null)
				htAss.put("#cognome#", dbr.get("cognome").toString());
			if(dbr.get("nome") != null)
				htAss.put("#nome#", dbr.get("nome").toString());
			htAss.put("#data_apertura#", getDateField(dbr, "data_apertura"));					
			if(dbr.get("skpa_complessita") != null)
				htAss.put("#complessita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
						"COMPLASS", (String) dbr.get("skpa_complessita"),"tab_descrizione"));
			htAss.put("#tipo_contatto#", getTipoContatto(tipo));
			md.writeSostituisci("tabella", htAss);

		}
		
		md.write("finetab");
	}
	
	/**
	 * In caso di stampa su foglio excel si deve stamapre un'unica tabella in 
	 * cui le informazioni su zona/distretto e area dis sono scritte su 3 
	 *  colonne aggiuntive
	 * @param md
	 * @param dbcur
	 * @param par
	 * @param dbc
	 * @throws Exception
	 */
	private void mkBodyFoglioCalcolo(mergeDocument md, ISASCursor dbcur, Hashtable par,
			ISASConnection dbc) throws Exception {
		
		md.write("iniziotab");
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();	
			
			String zona = dbr.get("cod_zona").toString();
			String distretto = dbr.get("cod_distretto").toString();
			String areadis = dbr.get("codice").toString();
			
			String cartella = dbr.get("n_cartella").toString();
			recuperaDatiAssistito(dbc, dbr, cartella);
			Integer tipo = (Integer) dbr.get("tipo");			

			Hashtable htAss = new Hashtable();
			htAss.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
			htAss.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
			htAss.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
			htAss.put("#n_cartella#", cartella);
			htAss.put("#cognome#", "");
			htAss.put("#nome#", "");
			htAss.put("#complessita#", "");
			if(dbr.get("cognome") != null)
				htAss.put("#cognome#", dbr.get("cognome").toString());
			if(dbr.get("nome") != null)
				htAss.put("#nome#", dbr.get("nome").toString());
			htAss.put("#data_apertura#", getDateField(dbr, "data_apertura"));					
			if(dbr.get("skpa_complessita") != null)
				htAss.put("#complessita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
						"COMPLASS", (String) dbr.get("skpa_complessita"),"tab_descrizione"));
			htAss.put("#tipo_contatto#", getTipoContatto(tipo));
			md.writeSostituisci("tabella", htAss);

		}
		
		md.write("finetab");
	}

	/**
	 * Decodifica il tipo di contatto
	 * @param tipo
	 * @return
	 */
	private String getTipoContatto(Integer tipo){
		String tipoContatto = "";
		switch (tipo.intValue()) {
		case 1:
			tipoContatto = "Sociale";
			break;
		case 2:
			tipoContatto = "Infermieristico";
			break;
		case 3:
			tipoContatto = "Riabilitativo";
			break;
		case 4:
			tipoContatto = "Medico";
			break;
		case 5:
			tipoContatto = "Medico Palliativista";
			break;

		default:
			break;
		}
		
		return tipoContatto;
	}
	

	
	
	/**
	 * Select 
	 * @param dbc
	 * @param par
	 * @return
	 */
	private String getSelect(ISASConnection dbc, Hashtable par) {
		ServerUtility su = new ServerUtility();
		String condComplessita = "";
		String orderComplessita = "";
		String condData = "";
		String joinPiano = "";
		String campoCompessita = "";
		String complessita = (String) par.get("compl_ass"); 
		String conPiano = (String) par.get("con_piano"); 
		String campiUbic = ",u.cod_zona, u.cod_distretto," +
				" u.des_zona descrizione_zona, u.codice";
		String tabelle = ",ubicazioni_n u, anagra_c ac";
		String condAnagraC = " AND ac.n_cartella = t.n_cartella" +
				" AND ac.data_variazione IN (" +				
				" SELECT MAX (data_variazione) FROM anagra_c" +
				" WHERE ac.n_cartella=anagra_c.n_cartella) ";
		String condUbicazioni = " AND u.codice = ac.areadis"; 
				
		String condPiano = "";
		if ((complessita != null) && (!complessita.equals("")))  {
			condComplessita = " AND p.skpa_complessita = '" + complessita + "'";
			
		}
		
		if(conPiano.equals("S")){
			joinPiano = " JOIN piano_assist p ON p.n_cartella = t.n_cartella";			
			condPiano = " AND (p.pa_data_chiusura IS NULL" +
						" OR p.pa_data_chiusura >= " + formatDate(dbc, dtFine) + 
					")";
			campoCompessita = ", skpa_complessita";
			orderComplessita = "skpa_complessita,";
		}
		
		//contatto sociale
		if(conPiano.equals("S"))
			condData = " AND p.pa_data <= t.ap_data_apertura";
		String selAssProgetto = "SELECT t.n_cartella, ap_data_apertura AS data_apertura," +
				"ap_ass_ref AS operatore, 1 AS tipo" + campoCompessita +	
				campiUbic +
				" FROM ass_progetto t" + joinPiano +
				tabelle +
				" WHERE ap_data_apertura <= " + formatDate(dbc, dtFine) +
				" AND (ap_data_chiusura IS NULL" +
					" OR ap_data_chiusura >= " + formatDate(dbc, dtFine) + 
				")" +
				condAnagraC + 
				condUbicazioni +
				condComplessita +
				condData +
				condPiano;
		selAssProgetto = su.addWhere(selAssProgetto, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		selAssProgetto = su.addWhere(selAssProgetto, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		selAssProgetto = su.addWhere(selAssProgetto, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));
		
		//contatto infermieristico
		if(conPiano.equals("S"))
			condData = " AND p.pa_data <= t.ski_data_apertura";
		String selSkInf = "SELECT t.n_cartella, ski_data_apertura AS data_apertura," +
				"ski_infermiere AS operatore, 2 AS tipo" + campoCompessita +
				campiUbic +
				" FROM skinf t" + joinPiano +
				tabelle +
				" WHERE ski_data_apertura <= " + formatDate(dbc, dtFine) +
				" AND (ski_data_uscita IS NULL" +
					" OR ski_data_uscita >= " + formatDate(dbc, dtFine) + 
				")" +
				condAnagraC + 
				condUbicazioni +
				condComplessita +
				condData +
				condPiano;
		selSkInf = su.addWhere(selSkInf, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		selSkInf = su.addWhere(selSkInf, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		selSkInf = su.addWhere(selSkInf, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));
		
		//contatto fisioterapico
		if(conPiano.equals("S"))
			condData = " AND p.pa_data <= t.skf_data";
		String selSkFis = "SELECT t.n_cartella, skf_data AS data_apertura," +
				"skf_fisiot AS operatore, 3 AS tipo" + campoCompessita +
				campiUbic +
				" FROM skfis t" + joinPiano +
				tabelle +
				" WHERE skf_data <= " + formatDate(dbc, dtFine) +
				" AND (skf_data_chiusura IS NULL" +
					" OR skf_data_chiusura >= " + formatDate(dbc, dtFine) + 
				")" +
				condAnagraC + 
				condUbicazioni +
				condComplessita +
				condData +
				condPiano;
		selSkFis = su.addWhere(selSkFis, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		selSkFis = su.addWhere(selSkFis, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		selSkFis = su.addWhere(selSkFis, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));
		
		//contatto medico
		if(conPiano.equals("S"))
			condData = " AND p.pa_data <= t.skm_data_apertura";
		String selSkMed = "SELECT t.n_cartella, skm_data_apertura AS data_apertura," +
				"skm_medico AS operatore, 4 AS tipo" + campoCompessita +
				campiUbic +
				" FROM skmedpal t" + joinPiano +
				tabelle +
				" WHERE skm_data_apertura <= " + formatDate(dbc, dtFine) +
				" AND (skm_data_chiusura IS NULL" +
					" OR skm_data_chiusura >= " + formatDate(dbc, dtFine) + 
				")" +
				condAnagraC + 
				condUbicazioni +
				condComplessita +
				condData +
				condPiano;
		selSkMed = su.addWhere(selSkMed, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		selSkMed = su.addWhere(selSkMed, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		selSkMed = su.addWhere(selSkMed, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));
		
		//contatto medico cure palliative
		if(conPiano.equals("S"))
			condData = " AND p.pa_data <= t.skm_data_apertura";
		String selSkMedPal = "SELECT t.n_cartella, skm_data_apertura AS data_apertura," +
				"skm_medico AS operatore, 5 AS tipo" + campoCompessita +
				campiUbic +
				" FROM skmedico t" + joinPiano +
				tabelle +
				" WHERE skm_data_apertura <= " + formatDate(dbc, dtFine) +
				" AND (skm_data_chiusura IS NULL" +
					" OR skm_data_chiusura >= " + formatDate(dbc, dtFine) + 
				")" +
				condAnagraC + 
				condUbicazioni +
				condComplessita +
				condData +
				condPiano;
		selSkMedPal = su.addWhere(selSkMedPal, su.REL_AND, "u.cod_zona",
				su.OP_EQ_STR, (String) par.get("zona"));
		selSkMedPal = su.addWhere(selSkMedPal, su.REL_AND, "u.cod_distretto",
				su.OP_EQ_STR, (String) par.get("distretto"));
		selSkMedPal = su.addWhere(selSkMedPal, su.REL_AND, "u.codice", su.OP_EQ_STR,
				(String) par.get("pca"));		

		String select = selAssProgetto +
						" UNION (" +
						selSkInf +
						") UNION (" +
						selSkFis +
						") UNION (" +
						selSkMed +
						") UNION (" +
						selSkMedPal +")" +
						" ORDER BY cod_zona,cod_distretto,codice," + orderComplessita +" data_apertura";
		
		debugMessage("FoElencoContattiApertiEJB.getSelectConclusi: " + select);
		
		return select;
	}

	

	public byte[] query_report(String utente, String passwd, Hashtable par,
			mergeDocument eve) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		dtInizio = (String) par.get("d1"); 
		dtFine = (String) par.get("d2"); 
		String type = (String) par.get("TYPE"); 
		
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			ISASCursor dbcur = null;

			dbcur = dbc.startCursor(getSelect(dbc, par));
			
			mkLayout(dbc, par, eve);

			if (dbcur == null) {
				eve.write("messaggio");
				eve.write("finale");
				System.out.println("FoElencoContattiApertiEJB.query_IntervOpe(): "
						+ "cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					eve.write("messaggio");
					eve.write("finale");
				} else {
					if(type.equalsIgnoreCase("PDF"))
						mkBody(eve, dbcur, par, dbc);			
					else
						mkBodyFoglioCalcolo(eve, dbcur, par, dbc);
					eve.write("finale");
				}
			} 
			dbcur.close();
			eve.close();

			dbc.close();
			super.close(dbc);
			done = true;
			//System.out.println(eve.get());
			return eve.get(); // restituisco il bytearray
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoElencoContattiApertiEJB: "+ e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out
							.println("FoElencoContattiApertiEJB.query_intervope(): "
									+ e1);
				}
			}
		}
	} 
	
	

} // End of FoIntervOpe class

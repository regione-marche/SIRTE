package it.caribel.app.sinssnt.bean;

//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// elisa b 29/09/11
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

public class FoElencoPianiAssistEJB extends SINSSNTConnectionEJB {

	private static final String MIONOME = "FoElencoPianiAssistEJB.";

	public FoElencoPianiAssistEJB() {
	}

	private void preparaLayout(mergeDocument doc, ISASConnection dbc,
			String tipo) throws Exception {
		Hashtable htxt = new Hashtable();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE "
					+ "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);

			String percorso = doc.getPath();
			if (percorso != null) {
				htxt.put("#percorso#", percorso);
			}

			htxt.put("#tipoPiani#", tipo);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		
		if(tipo.equals("A"))
			htxt.put("#tipo#", "ATTIVI");
		else 
			htxt.put("#tipo#", "CONCLUSI");

		doc.writeSostituisci("layout", htxt);
	}

	
	private void preparaBody(mergeDocument doc, ISASCursor dbcur,
			Hashtable par, ISASConnection dbc) throws Exception{
		
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
					doc.write("finetab");
				
				Hashtable hZ = new Hashtable();
				hZ.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
				hZ.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
				hZ.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
				doc.writeSostituisci("zona", hZ);
				
				zonaOld = zona;
				distrettoOld = distretto;
				areadisOld = areadis;
				
				doc.write("iniziotab");
			}		
			
			Hashtable h = new Hashtable();
			String cart = " ";
			h.put("#pa_operatore#", ISASUtil.getDecode(dbc, "operatori", "codice",
					dbr.get("pa_operatore").toString(), "nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nomeope"));					
			if (dbr.get("n_cartella") != null)
				cart = ((Integer) dbr.get("n_cartella")).toString();
			h.put("#n_cartella#", cart);

			String data = " ";

			if (dbr.get("pa_data") != null) {
				data = ((java.sql.Date) dbr.get("pa_data")).toString();
				data = data.substring(8, 10) + "/"
						+ data.substring(5, 7) + "/"
						+ data.substring(0, 4);
			}
			h.put("#pa_data#", data);				
			data = "";
			if (dbr.get("pa_data_chiusura") != null) {
				data = ((java.sql.Date) dbr.get("pa_data_chiusura")).toString();
				data = data.substring(8, 10) + "/"
						+ data.substring(5, 7) + "/"
						+ data.substring(0, 4);
			}
			h.put("#pa_data_chiusura#", data);
			
			h.put("#nome#", dbr.get("nome").toString());
			h.put("#cognome#", dbr.get("cognome").toString());

			String complessita = "";
			if (dbr.get("skpa_complessita") != null
					&& !((String) dbr.get("skpa_complessita")).equals("")) {
				complessita = ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
								"COMPLASS", (String) dbr.get("skpa_complessita"),
								"tab_descrizione");
			}
			h.put("#complessita#", complessita);
			//System.out.println("doc.writeSostituisci tabella");
			doc.writeSostituisci("tabella", h);
		}
		
		doc.write("finetab");
	}
	
	/**
	 * In caso di stampa su foglio excel si deve stamapre un'unica tabella in 
	 * cui le informazioni su zona/distretto e area dis sono scritte su 3 
	 *  colonne aggiuntive
	 * @param doc
	 * @param dbcur
	 * @param par
	 * @param dbc
	 * @throws Exception
	 */
	private void preparaBodyFoglioCalcolo(mergeDocument doc, ISASCursor dbcur,
			Hashtable par, ISASConnection dbc) throws Exception{
		
		String zona = "";
		String distretto = "";
		String areadis = "";
		
		doc.write("iniziotab");
		
		while (dbcur.next()) {
			
			ISASRecord dbr = dbcur.getRecord();
			
			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();
			
			Hashtable h = new Hashtable();
			h.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
			h.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
			h.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
			String cart = " ";
			h.put("#pa_operatore#", ISASUtil.getDecode(dbc, "operatori", "codice",
					dbr.get("pa_operatore").toString(), "nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nomeope"));					
			if (dbr.get("n_cartella") != null)
				cart = ((Integer) dbr.get("n_cartella")).toString();
			h.put("#n_cartella#", cart);

			String data = " ";

			if (dbr.get("pa_data") != null) {
				data = ((java.sql.Date) dbr.get("pa_data")).toString();
				data = data.substring(8, 10) + "/"
						+ data.substring(5, 7) + "/"
						+ data.substring(0, 4);
			}
			h.put("#pa_data#", data);				
			data = "";
			if (dbr.get("pa_data_chiusura") != null) {
				data = ((java.sql.Date) dbr.get("pa_data_chiusura")).toString();
				data = data.substring(8, 10) + "/"
						+ data.substring(5, 7) + "/"
						+ data.substring(0, 4);
			}
			h.put("#pa_data_chiusura#", data);
			
			h.put("#nome#", dbr.get("nome").toString());
			h.put("#cognome#", dbr.get("cognome").toString());

			String complessita = "";
			if (dbr.get("skpa_complessita") != null
					&& !((String) dbr.get("skpa_complessita")).equals("")) {
				complessita = ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
								"COMPLASS", (String) dbr.get("skpa_complessita"),
								"tab_descrizione");
			}
			h.put("#complessita#", complessita);
			//System.out.println("doc.writeSostituisci tabella");
			doc.writeSostituisci("tabella", h);
		}
		
		doc.write("finetab");
	}




	public byte[] query_report(String utente, String passwd, Hashtable par,
			mergeDocument doc) throws SQLException {
		String punto = MIONOME + "query_report ";
		ServerUtility su = new ServerUtility();
		boolean done = false;
		ISASConnection dbc = null;
		byte[] rit;
		stampa(punto + "Inizio con dati>" + par + "< utente>" + utente + "<\n");
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			String dtInizio = (String) par.get("d1"); 
			String dtFine = (String) par.get("d2"); 
			String tipoPiano = (String) par.get("tipo_piano"); 
			String complessita = (String) par.get("compl_ass"); 
			String type = (String) par.get("TYPE"); 
			String select = "";

			select = "SELECT p.*, nome, cognome," +
					" u.cod_zona, u.cod_distretto," +
					" u.des_zona descrizione_zona, u.codice" +
					" FROM piano_assist p LEFT JOIN cartella c" +					
					" ON p.n_cartella = c.n_cartella," +
					" ubicazioni_n u, anagra_c ac" +
					" WHERE pa_data <= " + formatDate(dbc, dtFine) +
					" AND ac.n_cartella = c.n_cartella" +
					" AND ac.data_variazione IN (" +
					" SELECT MAX (data_variazione) FROM anagra_c" +
						" WHERE ac.n_cartella=anagra_c.n_cartella) " +			
					" AND u.codice = ac.areadis"; 
			
			if(tipoPiano.equals("A")){
				//piani attivi
				select += " AND (pa_data_chiusura IS NULL" +
						" OR pa_data_chiusura >= " + formatDate(dbc, dtFine) + ")";
				
			}else{
				//piani chiusi
				select += " AND pa_data_chiusura >= " + formatDate(dbc, dtInizio) +
				" AND pa_data_chiusura <= " + formatDate(dbc, dtFine);
			}
			
			if((complessita != null ) && (!complessita.equals("")))
				select += " AND skpa_complessita = '" + complessita + "'";
			
			select = su.addWhere(select, su.REL_AND, "u.cod_zona",
					su.OP_EQ_STR, (String) par.get("zona"));
			select = su.addWhere(select, su.REL_AND, "u.cod_distretto",
					su.OP_EQ_STR, (String) par.get("distretto"));
			select = su.addWhere(select, su.REL_AND, "u.codice", su.OP_EQ_STR,
					(String) par.get("pca"));

			select += " ORDER BY cod_zona,u.cod_distretto,u.codice,skpa_complessita, pa_data_chiusura";

			debugMessage("FoPianoAssist/query_pianoassist-->select: " + select);
			ISASCursor dbcur = dbc.startCursor(select);

			preparaLayout(doc, dbc, tipoPiano);

			if (dbcur == null) {
				doc.write("messaggio");
				doc.write("finale");
			} else {
				if (dbcur.getDimension() <= 0) {
					doc.write("messaggio");
					doc.write("finale");
				} else {
					if(type.equalsIgnoreCase("PDF"))
						preparaBody(doc, dbcur, par, dbc);
					else
						preparaBodyFoglioCalcolo(doc, dbcur, par, dbc);
					doc.write("finale");
				} 
			}

			doc.close();

			rit = (byte[]) doc.get();

			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_assprogass()  ");
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
	

} 

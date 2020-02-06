package it.caribel.app.sinssnt.bean.nuovi;


import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.gprs2.FileMaker;
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

public class FoReportEstrRUGEJB extends SINSSNTConnectionEJB {

	public FoReportEstrRUGEJB() {
	}
	private String ver = "50-";
	private String CTS_TIPOCURA = "tipocura";
	
	public byte[] query_stampa(String utente, String passwd, Hashtable<String, String> par, mergeDocument eve)
			throws SQLException {
		String punto = ver + "query_stampa "; 
		LOG.info(punto + " inizio dati>>" + par + "<<");
		ISASConnection dbc = null;
		ISASCursor dbcCur = null;
		try {
			myLogin lg = new myLogin();
			String selectedLanguage = (String)par.get(FileMaker.printParamLang);
			lg.put(utente,passwd,selectedLanguage);
			//lg.put(utente, passwd);
			dbc = super.logIn(lg);
			
			String query = recuperaQuery(dbc, par);
			LOG.trace(punto + " query>>" + query);
			dbcCur = dbc.startCursor(query);
			if (dbcCur == null || (dbcCur != null && dbcCur.getDimension() == 0)) {
				preparaLayout(eve, dbc, par);
				eve.write("messaggio");
				eve.write("finale");
			} else {
				String dadata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
				String adata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);
				preparaLayout(eve, dbc, par);
				preparaBody(eve, dbcCur, dbc, par, dadata, adata);
				eve.write("finale");
			}
			eve.close();
//			LOG.trace(punto + " html generato>>\n\n" + new String(eve.get()) + "\n\n");
			return eve.get();
		} catch (Exception e) {
			LOG.error(punto + " Errore nella stampa ", e);
			throw new SQLException("FoListaAttivitaEJB.query_attivita(): " + e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}


	private void preparaLayout(mergeDocument md, ISASConnection dbc, Hashtable<String, String> par) {
		Hashtable<String, String> htxt = new Hashtable<String, String>();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));

			String dadata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
			String adata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);
			String zona = par.get("zona").toString();
			String distretto = par.get("distretto").toString();
			String sede = par.get("pca").toString();
			String periodo = "";
			if (ManagerDate.validaData(dadata)) {
				periodo = "Dal " + ManagerDate.formattaDataIta(dadata, "/");
			}
			if (ManagerDate.validaData(adata)) {
				periodo += (ISASUtil.valida(periodo) ? " Al " : "Fino al ") + ManagerDate.formattaDataIta(adata, "/");
			}
			htxt.put("#periodo#", periodo);
			if (zona!=null && !zona.equals(""))
				htxt.put("#zona#", "Zona: " + ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
			else
				htxt.put("#zona#","");
			if (distretto!=null && !distretto.equals(""))
				htxt.put("#distretto#", "Distretto: " + ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
			else
				htxt.put("#distretto#","");
			if (sede!=null && !sede.equals(""))
				htxt.put("#sede#", "Sede: " + ISASUtil.getDecode(dbc, "ubicazioni_n", "codice", sede, "descrizione"));
			else
				htxt.put("#sede#","");
		} catch (Exception ex) {

		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		md.writeSostituisci("layout", htxt);
	}

	
	@SuppressWarnings("unchecked")
	private void preparaBody(mergeDocument md, ISASCursor dbcCursor, ISASConnection dbc, Hashtable<String, String> par, String dataInizio, String dataFine) {
		String punto = ver + "preparaBody ";
		LOG.info(punto + " procedo ");
		@SuppressWarnings("rawtypes")
		Hashtable h = new Hashtable();
		try {
			md.write("iniziotab");
			while (dbcCursor.next()) {	
				ISASRecord dbr = dbcCursor.getRecord();
				String query_rug =" SELECT r.rug_punt FROM rugiii_hc r, sc_bisogni b"+
								  " WHERE r.n_cartella =" +dbr.get("n_cartella")+
								  " AND b.n_cartella =" +dbr.get("n_cartella")+
								  " AND r.n_cartella = b.n_cartella"+
								  " AND r.data = b.data"+
								  " AND b.valutazione<> '1'"+
								  " AND b.id_skso=" +dbr.get("id_skso")+
								  " AND r.data IN (SELECT MAX(r1.data)"+
								  " FROM rugiii_hc r1 WHERE r1.n_cartella=r.n_cartella "+
								  " AND r1.n_cartella =" +dbr.get("n_cartella")+
								  " AND r1.data <=" + formatDate(dbc, dataFine)+
								  " AND r1.data >=" + formatDate(dbc, dataInizio)+
								  ")";
				ISASRecord dbr_rug = dbc.readRecord(query_rug);
				if (dbr_rug!=null){
					if (dbr_rug.get("rug_punt")!=null && !dbr_rug.get("rug_punt").equals(""))
						h.put("#ultimo_punt#", ((Integer)dbr.get("rug_punt")).toString());
					else h.put("#ultimo_punt#","");
				}else h.put("#ultimo_punt#","");
		        h.put("#cartella#", ((Integer)dbr.get("n_cartella")).toString());
		        h.put("#cod_zona#", ((String)dbr.get("cod_zona")).toString());
		        h.put("#cod_distr#", ((String)dbr.get("cod_distretto")).toString());
		        h.put("#cod_sede#", ((String)dbr.get("codice")).toString());
		        h.put("#des_zona#", ((String)dbr.get("des_zona")).toString());
		        h.put("#des_distr#", ((String)dbr.get("des_distretto")).toString());
		        h.put("#des_sede#", ((String)dbr.get("descrizione")).toString());
		        h.put("#cognome#", ((String)dbr.get("cognome")).toString());
		        h.put("#nome#", ((String)dbr.get("nome")).toString());
		        h.put("#data_nasc#",((String)dbr.get("dt_nascita")).toString());
		        if (dbr.get("sess").equals("M"))
		        	h.put("#sesso#","Maschio");
		        else if (dbr.get("sess").equals("M"))
		        	h.put("#sesso#","Femmina");
		        else
		        	h.put("#sesso#","");
		        h.put("#cod_fiscale#", ((String)dbr.get("cod_fisca")).toString());
		        if (dbr.get("dt_accett")!=null && !dbr.get("dt_accett").equals(""))
			        h.put("#data_accett#",((String)dbr.get("dt_accett")).toString());
		        else 
		        	h.put("#data_accett#","");
		        if (dbr.get("dt_attivaz")!=null && !dbr.get("dt_attivaz").equals(""))
			        h.put("#data_attiv#",((String)dbr.get("dt_attivaz")).toString());
		        else 
		        	h.put("#data_attiv#","");
		        if (dbr.get("dt_iniz_piano")!=null && !dbr.get("dt_iniz_piano").equals(""))
			        h.put("#data_inizio_p#",((String)dbr.get("dt_iniz_piano")).toString());
		        else 
		        	h.put("#data_inizio_p#","");
		        if (dbr.get("dt_fine_piano")!=null && !dbr.get("dt_fine_piano").equals(""))
			        h.put("#data_fine_p#",((String)dbr.get("dt_fine_piano")).toString());
		        else 
		        	h.put("#data_fine_p#","");
		        if (dbr.get("dt_chiusura")!=null && !dbr.get("dt_chiusura").equals(""))
			        h.put("#data_chiusura#",((String)dbr.get("dt_chiusura")).toString());
		        else 
		        	h.put("#data_chiusura#","");
		        if (dbr.get("cod_med")!=null && !dbr.get("cod_med").equals("")){		        	
			        h.put("#cod_medico#",((String)dbr.get("cod_med")).toString());
			        h.put("#medico#", ISASUtil.getDecode(dbc, "medici", "mecodi", dbr.get("cod_med"), "mecogn") + " " + ISASUtil.getDecode(dbc, "medici", "mecodi", dbr.get("cod_med"), "menome"));
		        }else {
		        	h.put("#cod_medico#","");
		        	h.put("#medico#","");
		        }
		        if (dbr.get("classe_rug1")!=null && !dbr.get("classe_rug1").equals(""))
		        	h.put("#classe_rug#",((Integer)dbr.get("classe_rug1")).toString());
		        else
		        	h.put("#classe_rug#","");
		        if (dbr.get("categoria_rug1")!=null && !dbr.get("categoria_rug1").equals(""))
		        	h.put("#cat_rug#",((String)dbr.get("categoria_rug1")).toString());
		        else
		        	h.put("#cat_rug#","");

		        if (dbr.get("tipocura")!=null && !dbr.get("tipocura").equals(""))
		        	h.put("#intensita#", ISASUtil.getDecode(dbc,"tab_voci","tab_cod","tab_val",CostantiSinssntW.TAB_VAL_TIPOCURA,dbr.get("tipocura"),"tab_descrizione"));
		        else
		        	h.put("#intensita#","");
		        if (dbr.get("presa_carico_livello")!=null && !dbr.get("presa_carico_livello").equals(""))
		        	h.put("#livello#", ISASUtil.getDecode(dbc,"tab_voci","tab_cod","tab_val",CostantiSinssntW.TAB_VAL_LIVELLO_PRESA_CARICO,dbr.get("presa_carico_livello"),"tab_descrizione"));
		        else
		        	h.put("#livello#","");
				md.writeSostituisci("tabella", h);
				
							}
			md.write("fineTabella");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		md.write("tabella_fine_din");
	}

	
	private String recuperaQuery(ISASConnection dbc, Hashtable<String, String> par) {
		String punto = ver + "recuperaQuery ";
		ServerUtility su = new ServerUtility();
		LOG.debug(punto + "Inizio con dati>>" + par);
		String dataInizio = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
		String dataFine = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);
		
		
		String query =" SELECT s.n_cartella n_cartella, a.cod_med, a.cod_med AS cod_med_desc,"+
					  " s.id_skso id_skso,"+
					  " NVL (TO_CHAR (data_nasc, 'DD/MM/YYYY'), '') dt_nascita, sesso sess,"+
					  " cod_fisc cod_fisca, u.cod_zona, u.des_zona, u.des_distretto,"+
					  " u.cod_distretto AS cod_distretto, u.codice, u.descrizione,"+
					  " NVL (TO_CHAR (s.pr_data_puac, 'DD/MM/YYYY'), '') dt_accett,"+
					  " c.cognome cognome, c.nome nome,"+
					  " NVL (TO_CHAR (m.data_inizio, 'DD/MM/YYYY'), '') dt_iniz_piano,"+
					  " NVL (TO_CHAR (m.data_fine, 'DD/MM/YYYY'), '') dt_fine_piano,"+
					  " NVL (TO_CHAR (data_presa_carico_skso, 'DD/MM/YYYY'), '') dt_attivaz,"+
					  " NVL (TO_CHAR (s.pr_data_chiusura, 'DD/MM/YYYY'), '') dt_chiusura,"+
					  " s.flag_sent, m.tipocura tipocura,r.RUG_PUNT classe_rug1,r.RUG_PUNT_STR categoria_rug1, s.presa_carico_livello"+
					  " FROM rm_skso s, rm_skso_mmg m, cartella c, anagra_c a, ubicazioni_n u,"+
					  " rugiii_hc r,sc_bisogni b"+
					  " WHERE s.n_cartella = m.n_cartella"+
					  " AND s.id_skso = m.id_skso"+
					  " AND c.n_cartella = m.n_cartella"+
					  " AND s.pr_data_puac <="+ formatDate(dbc, dataFine)+
					  " AND ((s.pr_data_chiusura IS NOT NULL"+
					  " AND s.pr_data_chiusura >=" + formatDate(dbc, dataInizio)+
					  ")"+
					  " OR s.pr_data_chiusura IS NULL"+
					  ")"+
					  " AND m.tipocura = '1'";
			if (par.get("zona")!=null && !par.get("zona").equals(""))
				query = su.addWhere(query, su.REL_AND, "u.cod_zona",
						su.OP_EQ_STR, (String) par.get("zona"));
			if (par.get("distretto")!=null && !par.get("distretto").equals(""))
				query = su.addWhere(query, su.REL_AND, "u.cod_distretto",
						su.OP_EQ_STR, (String) par.get("distretto"));
			if (par.get("pca")!=null && !par.get("pca").equals(""))
				query = su.addWhere(query, su.REL_AND, "u.codice",
						su.OP_EQ_STR, (String) par.get("pca"));	

			query +=" AND u.tipo = 'P'"+
					" AND u.codice = s.cod_presidio"+
					" AND s.n_cartella = a.n_cartella"+    
					" AND s.data_presa_carico_skso IS NOT NULL"+    
					" AND r.n_cartella=b.n_cartella"+
					" AND r.data=b.data"+
					" AND b.n_cartella=s.n_cartella"+
					" AND s.id_skso=b.id_skso"+
					" AND b.valutazione='1'"+   
					" AND a.data_variazione IN ("+
					" SELECT MAX (ac.data_variazione)"+
					" FROM anagra_c ac"+
					" WHERE ac.n_cartella = a.n_cartella"+
					" AND ac.data_variazione <="+ formatDate(dbc, dataFine)+")"+
					" ORDER BY u.des_zona, des_distretto, descrizione, cognome, nome";

		LOG.debug(punto + " query>> " + query);
   
		return query.toString();
	}

}


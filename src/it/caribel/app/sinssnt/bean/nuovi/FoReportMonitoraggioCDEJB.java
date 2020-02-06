package it.caribel.app.sinssnt.bean.nuovi;

import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;

public class FoReportMonitoraggioCDEJB extends SINSSNTConnectionEJB {

	public FoReportMonitoraggioCDEJB() {
	}

	private String ver = "30-"+ this.getClass().getName()+". ";
	private String CTS_TIPOCURA = "tipocura";
	private String CTS_SCHEDASENZA_SO = "senza_so";
	private String CTS_SCHEDASENZA_SO_NO = "senza_so_N";
	private String CTS_SCHEDASENZA_SO_SI = "con_so";
	
	private String CTS_ADP = "adp";
	private String CTS_ARD = "ard";
	private String CTS_AID = "aid";
	private String CTS_VSD = "vsd";
	private String CTS_NUMERO = "numero";
	private String CTS_LIVELLO = "livello";
	private String CTS_LIVELLO_1 = "01";
	private String CTS_LIVELLO_2 = "02";
	private String CTS_LIVELLO_3 = "03";
	private String CTS_LIVELLO_NO_LIVELLO = "";
	private String CTS_S = "S";

	int totaleNumCp = 0, totaleNumAdp = 0, totaleNumArd = 0, totaleNumAid = 0, totaleNumVsd = 0, totaleNumCdi1 = 0,
			totaleNumCdi2 = 0, totaleNumCdi=0, totaleSchedeSenzaSo=0;
	int totaleNumCdi3 = 0, totaleNumCdiNL = 0;
	int numCp = 0, numAdp = 0, numArd = 0, numAid = 0, numVsd = 0, numCdi1 = 0, numCdi2 = 0, numCdi3 = 0, numCdiNL = 0,
			numCdi=0, numSchedeSenzaSo = 0 ;
	

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
			String query = recuperaQuery(dbc, par, true);
			LOG.trace(punto + " query>> \n" + query+"\n");
			preparaLayout(eve, dbc, par);
			dbcCur = dbc.startCursor(query);
			stampaPrestazioniAccessi(par, eve, dbc, dbcCur, true);
			query = recuperaQuery(dbc, par, false);
			LOG.trace(punto + " query Accessi>>\n" + query+"\n");
			dbcCur = dbc.startCursor(query);
			stampaPrestazioniAccessi(par, eve, dbc, dbcCur, false);
			eve.write("finale");
			eve.close();
			//			LOG.trace(punto + " html generato>>\n\n" + new String(eve.get()) + "\n\n");
			return eve.get();
		} catch (Exception e) {
			LOG.error(punto + " Errore nella stampa ", e);
			throw new SQLException("FoListaAttivitaEJB.query_attivita(): " + e);
		} finally {
			logout_nothrow(punto, dbcCur, dbc);
		}
	}

	public void stampaPrestazioniAccessi(Hashtable<String, String> par, mergeDocument eve, ISASConnection dbc,
			ISASCursor dbcCur, boolean stampaPrestazioni) throws ISASMisuseException, ISASPermissionDeniedException,
			DBMisuseException, DBSQLException, Exception {
		if (dbcCur == null || (dbcCur != null && dbcCur.getDimension() == 0)) {
			Hashtable<String, String> dati = new Hashtable<String, String>();
			String descInfo = "";
			if (stampaPrestazioni) {
				descInfo = Labels.getLabel("st.monitoraggio.cure.domiciliare.prestazioni");
			} else {
				descInfo = Labels.getLabel("st.monitoraggio.cure.domiciliare.accessi");
			}
			dati.put("#info#", descInfo);
			eve.writeSostituisci("messaggio", dati);
		} else {
			stampaPrestazioni(eve, dbc, dbcCur, stampaPrestazioni);
		}
	}

	public void stampaPrestazioni(mergeDocument eve, ISASConnection dbc, ISASCursor dbrCur, boolean stampaPrestazioni)
			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException, Exception {
		String punto = ver + "stampaPrestazioni ";

		Hashtable<String, String> dati = new Hashtable<String, String>();
		String titoloTabella = "";
		if (stampaPrestazioni) {
			titoloTabella = "PRESTAZIONI PER FIGURA PROFESSIONALE";
		} else {
			titoloTabella = "ACCESSI PER FIGURA PROFESSIONALE";
			eve.write("saltopagina");
		}
		dati.put("#titolo_tabella#", titoloTabella);
		eve.writeSostituisci("tabella_intestazione", dati);

		ISASRecord dbrDati = null;
		numCp = 0;
		numCdi = 0;
		numAdp = 0;
		numArd = 0;
		numAid = 0;
		numVsd = 0;
		numCdi1 = 0;
		numCdi2 = 0;
		numCdi3 = 0;
		numCdiNL = 0;
		numSchedeSenzaSo = 0 ;
		String tipoCura = "";
		String descPrestaz = "", descPrestazOld = "";
		String tipoFiguraProfessionale = "", tipoFiguraProfessionaleOld = "";
		boolean stampaTotale = false;
		if (stampaPrestazioni) {
			while (dbrCur.next()) {
				stampaTotale = true;
				dbrDati = (ISASRecord) dbrCur.getRecord();
				descPrestaz = ISASUtil.getValoreStringa(dbrDati, "pre_des_prest");
				tipoCura = ISASUtil.getValoreStringa(dbrDati, CTS_TIPOCURA);
				tipoFiguraProfessionale = ISASUtil.getValoreStringa(dbrDati, "int_tipo_oper");

				if (!(ISASUtil.valida(tipoFiguraProfessionaleOld))) {
					tipoFiguraProfessionaleOld = tipoFiguraProfessionale;
					stampaFiguraProfessionale(eve,dbc, tipoFiguraProfessionale, stampaPrestazioni);
				}
				if (!tipoFiguraProfessionaleOld.equals(tipoFiguraProfessionale)) {
					stampaDatiTotali(eve, stampaPrestazioni, tipoCura);
					stampaFiguraProfessionale(eve,dbc, tipoFiguraProfessionale, stampaPrestazioni);
					tipoFiguraProfessionaleOld = tipoFiguraProfessionale;
				}
				if (!ISASUtil.valida(descPrestazOld)) {
					descPrestazOld = descPrestaz;
				}
				if (!(descPrestazOld.equalsIgnoreCase(descPrestaz))) {
					stampaDatiCorpo(eve, descPrestazOld, tipoCura);
					descPrestazOld = descPrestaz;
				}
				aggiornaDatiCalcolo(dbrDati, tipoCura);
			}
			if (ISASUtil.valida(descPrestaz) && stampaPrestazioni) {
				stampaDatiCorpo(eve, descPrestaz, tipoCura);
			}
			if (stampaTotale) {
				stampaDatiTotali(eve, stampaPrestazioni, tipoCura);
			}
		} else {
			String descFiguraProf = "";String tipoCuraOld="";
			while (dbrCur.next()) {
				dbrDati = (ISASRecord) dbrCur.getRecord();
				tipoFiguraProfessionale = ISASUtil.getValoreStringa(dbrDati, "int_tipo_oper");
				tipoCura = ISASUtil.getValoreStringa(dbrDati, CTS_TIPOCURA);
				if (!(ISASUtil.valida(tipoFiguraProfessionaleOld))) {
					descFiguraProf = stampaFiguraProfessionale(eve,dbc, tipoFiguraProfessionale, stampaPrestazioni);
					tipoFiguraProfessionaleOld = tipoFiguraProfessionale;
				}
				if (!(ISASUtil.valida(tipoCuraOld))){
					tipoCuraOld = tipoCura;
				}

				if (!tipoFiguraProfessionaleOld.equals(tipoFiguraProfessionale) || (!tipoCuraOld.equals(tipoCura))) {
					stampaDatiCorpo(eve, descFiguraProf, tipoCura);
					descFiguraProf = stampaFiguraProfessionale(eve,dbc, tipoFiguraProfessionaleOld, stampaPrestazioni);
					if(!tipoFiguraProfessionaleOld.equals(tipoFiguraProfessionale)){
						tipoFiguraProfessionaleOld = tipoFiguraProfessionale;
					}
					if(!tipoCuraOld.equals(tipoCura)){
						tipoCuraOld = tipoCura;
					}
				}
				aggiornaDatiCalcolo(dbrDati, tipoCura);
			}
			stampaDatiCorpo(eve, descFiguraProf, tipoCura);
		}
		eve.write("tabella_fine");
	}

	private void aggiornaDatiCalcolo(ISASRecord dbrDati, String tipoCura) {
		String punto = ver +"aggiornaDatiCalcolo ";
		if (isTipoCurePrestazionali(tipoCura)) {
			LOG.trace(punto + " nella prestazioni il livello NON DEVE ESSERE CONTEGGIATO ");
			numCp += recuperaValore(dbrDati, CTS_TIPOCURA, Costanti.CTS_COD_CURE_PRESTAZIONALI);
			numAdp += recuperaValore(dbrDati, CTS_ADP, CTS_S);
			numArd += recuperaValore(dbrDati, CTS_ARD, CTS_S);
			numAid += recuperaValore(dbrDati, CTS_AID, CTS_S);
			numVsd += recuperaValore(dbrDati, CTS_VSD, CTS_S);
			numSchedeSenzaSo = numVsd += recuperaValore(dbrDati, CTS_SCHEDASENZA_SO, CTS_SCHEDASENZA_SO_NO);
		}else if (isTipoCureDomiciliare(tipoCura)) {
			LOG.trace(punto + " caso domiciliare ");
				numCdi += recuperaValore(dbrDati, CTS_TIPOCURA, Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE);
				numCdi1 += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_1);
				numCdi2 += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_2);
				numCdi3 += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_3);
				numCdiNL += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_NO_LIVELLO);
				numSchedeSenzaSo = numVsd += recuperaValore(dbrDati, CTS_SCHEDASENZA_SO, CTS_SCHEDASENZA_SO_NO);
				LOG.debug( " Aggiorno i contatori ");
		}else {
			LOG.trace(punto + " tipo cura non gestito>>" + tipoCura+"<<");
		}
		
	}

	public void calcolaLivello_(ISASRecord dbrDati, String tipoCura) {
		String punto = ver + "calcolaLivello ";

		if (isTipoCurePrestazionali(tipoCura)) {
			LOG.trace(punto + " nella prestazioni il livello NON DEVE ESSERE CONTEGGIATO ");
		} if (isTipoCureDomiciliare(tipoCura)) {
			numCdi1 += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_1);
			numCdi2 += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_2);
			numCdi3 += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_3);
			numCdiNL += recuperaValore(dbrDati, CTS_LIVELLO, CTS_LIVELLO_NO_LIVELLO);
			LOG.debug(punto + " Aggiorno i contatori ");
		}else {
			LOG.trace(punto + " caso non gestito>>" + tipoCura+"<<<");
		}
	}

	private String stampaFiguraProfessionale(mergeDocument eve,ISASConnection dbc, String tipoFiguraProfessionale,
			boolean stampaPrestazione) throws Exception {
		String descFiguraProfessionale = "";
		if (ISASUtil.valida(tipoFiguraProfessionale)) {
			Hashtable<String, String> dati = new Hashtable<String, String>();
			descFiguraProfessionale = recuperaFiguraProfessionale(dbc,tipoFiguraProfessionale);

			descFiguraProfessionale = (descFiguraProfessionale != null ? descFiguraProfessionale : "Non decodifica: "
					+ tipoFiguraProfessionale);

			dati.put("#figura_professionale#", descFiguraProfessionale);
			String figProfessione = "";
			if (stampaPrestazione) {
				figProfessione = Labels.getLabel("st.monitoraggio.cure.domiciliare.prestazioni.fig.prof");
			} else {
				figProfessione = Labels.getLabel("st.monitoraggio.cure.domiciliare.accessi.fig.prof");
			}
			dati.put("#tipo_figura_professionale#", figProfessione);

			eve.writeSostituisci("tabella_corpo_figura", dati);
		}
		return descFiguraProfessionale;
	}

	public String recuperaFiguraProfessionale(ISASConnection dbc,String tipoFiguraProfessionale)throws Exception {
		String descrizione = ManagerOperatore.decodificaTipoOperatore(dbc,tipoFiguraProfessionale,null);
		return descrizione;
	}

	public void stampaDatiTotali(mergeDocument eve, boolean stampaPrestazioni, String tipoCura) {
		String punto = ver + "stampaDatiTotali ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
//		int totale = totaleNumCp+ totaleNumCdi1 + totaleNumCdi2 + totaleNumCdi3 + totaleNumCdiNL;
		int totale = totaleNumCp+ totaleNumCdi;
		
		int totaleConSchedeSo = totale + totaleSchedeSenzaSo;
		
				String tipoTotale = "";
		if (stampaPrestazioni) {
			tipoTotale = Labels.getLabel("st.monitoraggio.cure.domiciliare.totali.prestazioni");
		} else {
			tipoTotale = Labels.getLabel("st.monitoraggio.cure.domiciliare.totali.accessi");
		}
		dati.put("#desc_prestazione#", tipoTotale);
		dati.put("#cp#", totaleNumCp + "");

		dati.put("#adp#", totaleNumAdp + "");
		dati.put("#ard#", totaleNumArd + "");
		dati.put("#aid#", totaleNumAid + "");
		dati.put("#vsd#", totaleNumVsd + "");
	
		dati.put("#cdi#", totaleNumCdi+ "");
		
		dati.put("#cdi_1_l#", totaleNumCdi1 + "");
		dati.put("#cdi_2_l#", totaleNumCdi2 + "");
		dati.put("#cdi_3_l#", totaleNumCdi3 + "");
		dati.put("#cdi_non_l#", totaleNumCdiNL + "");
		
		dati.put("#senza_so#", totaleSchedeSenzaSo+"");
		dati.put("#totale_cp_cdi_s_so#", totaleConSchedeSo+"");
		
		dati.put("#totale#", totale + "");
		LOG.debug(punto + " stampo la prestazione >>" + dati);
		eve.writeSostituisci("tabella_corpo_dati", dati);

		totaleNumCp = 0;
		totaleNumAdp = 0;
		totaleNumArd = 0;
		totaleNumAid = 0;
		totaleNumVsd = 0;
		totaleNumCdi1 = 0;
		totaleNumCdi2 = 0;
		totaleNumCdi3 = 0;
		totaleNumCdiNL = 0;
		totaleSchedeSenzaSo = 0;

		numCp = 0;
		numCdi = 0;
		numAdp = 0;
		numArd = 0;
		numAid = 0;
		numVsd = 0;
		numCdi1 = 0;
		numCdi2 = 0;
		numCdi3 = 0;
		numCdiNL = 0;
		numSchedeSenzaSo = 0;
	}

	public void stampaDatiCorpo(mergeDocument eve, String descPrestaz, String tipoCura) {
		String punto = ver + "stampaDatiCorpo ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
//		int totale = numCdi1 + numCdi2 + numCdi3 + numCdiNL + numCp;
		
		int totale = numCp + numCdi;
		
		int totaleConSchedeSo = totale + numSchedeSenzaSo;
		
		dati.put("#desc_prestazione#", descPrestaz);
		dati.put("#cp#", numCp + "");
		dati.put("#adp#", numAdp + "");
		dati.put("#ard#", numArd + "");
		dati.put("#aid#", numAid + "");
		dati.put("#vsd#", numVsd + "");
		dati.put("#cdi_1_l#", numCdi1 + "");   
		dati.put("#cdi_2_l#", numCdi2 + "");
		dati.put("#cdi_3_l#", numCdi3 + "");
		dati.put("#cdi_non_l#", numCdiNL + "");

		dati.put("#cdi#", numCdi + "");
		
		dati.put("#senza_so#", numSchedeSenzaSo+"");
		dati.put("#totale_cp_cdi_s_so#", totaleConSchedeSo+"");

		dati.put("#totale#", totale + "");
		LOG.debug(punto + " stampo la prestazione >>" + descPrestaz);
		eve.writeSostituisci("tabella_corpo_dati", dati);

		totaleNumCp += numCp;
		totaleNumCdi += numCdi;
		totaleNumAdp += numAdp;
		totaleNumArd += numArd;
		totaleNumAid += numAid;
		totaleNumVsd += numVsd;
		totaleNumCdi1 += numCdi1;
		totaleNumCdi2 += numCdi2;
		totaleNumCdi3 += numCdi3;
		totaleNumCdiNL += numCdiNL;
		totaleSchedeSenzaSo +=numSchedeSenzaSo;

		numCp = 0;
		numCdi = 0;
		numAdp = 0;
		numArd = 0;
		numAid = 0;
		numVsd = 0;
		numCdi1 = 0;
		numCdi2 = 0;
		numCdi3 = 0;
		numCdiNL = 0;
		numSchedeSenzaSo =0;
	}

	private boolean isTipoCurePrestazionali(String tipoCura) {
		return ISASUtil.valida(tipoCura) && tipoCura.equals(Costanti.CTS_COD_CURE_PRESTAZIONALI);
	}

	private boolean isTipoCureDomiciliare(String tipoCura) {
		return ISASUtil.valida(tipoCura) && tipoCura.equals(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE);
	}
	
	private int recuperaValore(ISASRecord dbrDati, String keyCampo, String valore) {
		return recuperaValore(dbrDati, keyCampo, valore, false);
	}

	private int recuperaValore(ISASRecord dbrDati, String keyCampo, String valore, boolean valoreValido) {
		int numero = 0;
		String valCampo = ISASUtil.getValoreStringa(dbrDati, keyCampo);
		int valNumero = ISASUtil.getValoreIntero(dbrDati, CTS_NUMERO);
		if (valNumero > 0 && ((valCampo.equalsIgnoreCase(valore)) || valoreValido)) {
			numero = valNumero;
		}
		return numero;
	}

	private void preparaLayout(mergeDocument eve, ISASConnection dbc, Hashtable<String, String> par) {
		String punto = ver + "preparaLayout ";
		LOG.debug(punto + " inizio ");

		Hashtable<String, String> htxt = new Hashtable<String, String>();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));

			String dadata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
			String adata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);
			String periodo = "";
			if (ManagerDate.validaData(dadata)) {
				periodo = Labels.getLabel("st.monitoraggio.cure.domiciliare.dal") + ": "
						+ ManagerDate.formattaDataIta(dadata, "/");
			}
			if (ManagerDate.validaData(adata)) {
				periodo += " " + Labels.getLabel("st.monitoraggio.cure.domiciliare.al") + ": "
						+ ManagerDate.formattaDataIta(adata, "/");
			}
			htxt.put("#periodo#", periodo);

			String ragg = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_RAGGRUPPAMENTO);
			String zona = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_ZONE);
			String distretto = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DISTRETTO);
			String pca = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_PCA);
			String filtro1 = "";
			if (ISASUtil.valida(zona)) {
				filtro1 = Labels.getLabel("generic.zona") + ": "
						+ decodifica("zone", "codice_zona", zona, "descrizione_zona", dbc);
			}

			if (ISASUtil.valida(distretto)) {
				filtro1 += ", " + Labels.getLabel("generic.distretto") + ": "
						+ decodifica("distretti", "cod_distr", distretto, "des_distr", dbc);
			}

			if (ISASUtil.valida(ragg) && ISASUtil.valida(pca)) {
				if (ragg.equalsIgnoreCase("P")) {
					filtro1 += ", " + Labels.getLabel("PanelUbicazione.presidioCombo") + ": "
							+ decodifica("presidi", "codpres", pca, "despres", dbc);
				}
			}
			htxt.put("#filtro1#", filtro1);

			String tipoPrestazione = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_TIPO_PRESTAZIONE);
			String figuraProfessionale = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_FIGURA_PROFESSIONALE);

			String filtro2 = Labels.getLabel("operatori.tipoOperatore") + ": ";
			if (ISASUtil.valida(figuraProfessionale)) {
				String descrizione = recuperaFiguraProfessionale(dbc,figuraProfessionale);
				filtro2 += descrizione + ", ";
			} else {
				filtro2 += " --   ";
			}

			filtro2 += Labels.getLabel("generic.tipo_prestazione") + ": ";
			if (ISASUtil.valida(tipoPrestazione)) {
				if (tipoPrestazione.equals("D")) {
					filtro2 += Labels.getLabel("accessiPrestazioni.prestazioniForm.tipoPrestazione.domiciliare");
				}
			} else {
				filtro2 += Labels.getLabel("accessiPrestazioni.prestazioniForm.tipoPrestazione.nonDomiciliare");
			}
			htxt.put("#filtro2#", filtro2);

		} catch (Exception ex) {

		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		eve.writeSostituisci("layout", htxt);

	}

	private String decodifica(String tabella, String nome_cod, Object val_codice, String descrizione, ISASConnection dbc) {

		if (val_codice == null)
			return " ";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE " + nome_cod + " ='"
					+ val_codice.toString() + "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			if (dbtxt == null || dbtxt.get("descrizione") == null)
				return " ";
			return ((String) dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return " ";
		}
	}

	private String recuperaQuery(ISASConnection dbc, Hashtable<String, String> par, boolean prestazioni) {
		String punto = ver + "recuperaQuery ";
		LOG.debug(punto + " inizio ");
		ServerUtility su = new ServerUtility();

		StringBuffer query = new StringBuffer();
		String dtInizio = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
		String dtFine = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);

		String tipoPrestazione = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_TIPO_PRESTAZIONE);
		String figuraProfessionale = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_FIGURA_PROFESSIONALE);

		String tabella = ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc"
				: "ubicazioni_n") + " u";

		String condWhere = getFiltroUbicazione(par, su);

		query.append("SELECT int_tipo_oper ");
		if (prestazioni) {
			query.append(", pre_des_prest");
		}
		query.append(", adp, ard, aid, vsd,livello," + CTS_TIPOCURA+", " +CTS_SCHEDASENZA_SO 
				+ " , COUNT (*) AS numero FROM ( SELECT v.int_tipo_oper ");

		if (prestazioni) {
			query.append(", p.pre_des_prest ");
		}

		query.append(", g.adp as " + CTS_ADP + ", g.ard as " + CTS_ARD + ",g.aid as " + CTS_AID);
		query.append(", g.vsd as " + CTS_VSD + ", r.presa_carico_livello as " + CTS_LIVELLO + ", g.tipocura as "
				+ CTS_TIPOCURA +", '"+ CTS_SCHEDASENZA_SO_SI+ "' as " + CTS_SCHEDASENZA_SO);
		query.append(" FROM rm_skso r, rm_skso_mmg g, interv v, ");
		if (prestazioni) {
			query.append("intpre p, ");
		}
		query.append(" anagra_c a, ");
		query.append(tabella);
		query.append(" WHERE r.n_cartella = a.n_cartella ");
		query.append(" AND A.DATA_VARIAZIONE IN (SELECT MAX(x.DATA_VARIAZIONE) FROM anagra_c x where x.n_cartella = r.n_cartella ");
		query.append(" AND x.data_variazione <= " + formatDate(dbc, dtFine) + ") ");

		query.append(" AND v.int_data_prest <= " + formatDate(dbc, dtFine));
		query.append(" and r.n_cartella = v.int_cartella AND r.n_cartella = g.n_cartella AND r.id_skso = g.id_skso ");
		if (prestazioni) {
			query.append(" AND v.int_anno = p.pre_anno AND v.int_contatore = p.pre_contatore ");
		}
		query.append(" AND r.pr_data_puac >= ");
		query.append(formatDate(dbc, dtInizio));
		query.append(" AND r.pr_data_puac <= ");
		query.append(formatDate(dbc, dtFine));
		query.append(" AND v.int_data_prest >= r.pr_data_puac ");
		query.append(" AND ( r.pr_data_chiusura IS NULL OR (r.pr_data_chiusura IS NOT NULL ");
		query.append(" AND v.int_data_prest <= r.pr_data_chiusura ) ) ");

		if (ISASUtil.valida(tipoPrestazione)) {
			query.append(" AND v.int_ambdom = '" + tipoPrestazione + "' ");
		} else {
			//			PRESTAZIONI NON DOMICILIARI 
			query.append(" AND v.int_ambdom <> '" + CostantiSinssntW.CTS_STP_REPORT_TIPO_PRESTAZIONE_DOMICILIARE + "' ");
		}

		if (ISASUtil.valida(figuraProfessionale)) {
			query.append(" AND v.int_tipo_oper = '" + figuraProfessionale + "' ");
		}
		query.append(" AND " + condWhere);
		
		String queryContatti = recuperaQueryContatti(dbc, dtInizio, dtFine, figuraProfessionale, tipoPrestazione, tabella, 
				condWhere, prestazioni);
		
		if (ISASUtil.valida(queryContatti)) {
			query.append(" UNION " + queryContatti);
		}

		if (prestazioni && !ISASUtil.valida(figuraProfessionale)
				|| (ISASUtil.valida(figuraProfessionale) && (figuraProfessionale.equals(GestTpOp.CTS_COD_MMG)))) {
			query.append(" UNION ");
			query.append("SELECT '" + GestTpOp.CTS_COD_MMG + "' as int_tipo_oper ");
			if (prestazioni) {
				query.append(", p.pipp_des as pre_des_prest, ");
				query.append(" g.adp as " + CTS_ADP + ", g.ard as " + CTS_ARD + ",g.aid as " + CTS_AID);
				query.append(", g.vsd as " + CTS_VSD + ", r.presa_carico_livello as " + CTS_LIVELLO
						+ ", g.tipocura as " + CTS_TIPOCURA +", '"+ CTS_SCHEDASENZA_SO_SI+ "' as " + CTS_SCHEDASENZA_SO);
			}
			query.append(" FROM rm_skso r, rm_skso_mmg g, ");
			query.append(" intmmg m, tabpipp p, ");
			query.append(" anagra_c a, ");
			query.append(tabella);
			query.append(" WHERE r.n_cartella = a.n_cartella ");
			query.append(" AND A.DATA_VARIAZIONE IN (SELECT MAX(x.DATA_VARIAZIONE) FROM anagra_c x where x.n_cartella = r.n_cartella ");
			query.append(" AND x.data_variazione <= " + formatDate(dbc, dtFine) + ") ");
			query.append(" AND m.int_data  <= " + formatDate(dbc, dtFine));
			query.append(" and r.n_cartella = m.int_cartella AND r.n_cartella = g.n_cartella AND r.id_skso = g.id_skso ");
			query.append(" AND m.int_prestaz = p.pipp_codi AND m.int_tipo_pres = p.pipp_tipo ");
			query.append(" AND r.pr_data_puac >= ");
			query.append(formatDate(dbc, dtInizio));
			query.append(" AND r.pr_data_puac <= ");
			query.append(formatDate(dbc, dtFine));
			query.append(" AND m.int_prestaz = p.pipp_codi AND m.int_tipo_pres = p.pipp_tipo ");
			query.append(" AND m.int_data >= r.pr_data_puac ");
			query.append(" AND ( r.pr_data_chiusura IS NULL OR (r.pr_data_chiusura IS NOT NULL ");
			query.append(" AND m.int_data <= r.pr_data_chiusura ) ) ");
			query.append(" AND " + condWhere);

		} else {
			LOG.trace(punto + " non recupero le prestazioni dei medici ");
		}

		query.append(" ) GROUP BY int_tipo_oper ");
		if (prestazioni) {
			query.append(", pre_des_prest ");
		}
		query.append(", adp, ard, aid, vsd, livello, tipocura, "+CTS_SCHEDASENZA_SO);
		query.append(" ORDER BY int_tipo_oper");
		if (prestazioni) {
			query.append(", pre_des_prest ");
		}
		query.append(", "+ CTS_TIPOCURA+", "+CTS_SCHEDASENZA_SO);

		return query.toString();
	}

	private String recuperaQueryContatti(ISASConnection dbc, String dtInizio, String dtFine,
			String figuraProfessionale, String tipoPrestazione, String tabella, String condWhere, boolean prestazioni) {
		String punto = ver + "recuperaQueryContatti ";
		String tabellaContatto = "skinf";
		String dbTipoCura ="ski_motivo";
		String dtAperturaContatto = "ski_data_apertura";
		String dtChiusuraContatto = "ski_data_uscita";
		String queryContattoInf = recuperaQueryContatto(dbc, dtInizio, dtFine, figuraProfessionale, tipoPrestazione,
				tabella, condWhere, dbTipoCura, prestazioni, tabellaContatto, dtAperturaContatto, dtChiusuraContatto);		
		StringBuffer query = new StringBuffer(queryContattoInf);

		tabellaContatto = "skmedico";
		dbTipoCura ="skm_motivo";
		dtAperturaContatto = "skm_data_apertura";
		dtChiusuraContatto = "skm_data_chiusura";
		String queryContattoMed = recuperaQueryContatto(dbc, dtInizio, dtFine, figuraProfessionale, tipoPrestazione,
				tabella, condWhere, dbTipoCura, prestazioni, tabellaContatto, dtAperturaContatto, dtChiusuraContatto);		

		query.append(" UNION "+ queryContattoMed);
		
		tabellaContatto = "skfis";
		dbTipoCura ="skf_motivo";
		dtAperturaContatto = "skf_data";
		dtChiusuraContatto = "skf_data_chiusura";
		String queryContattoFis = recuperaQueryContatto(dbc, dtInizio, dtFine, figuraProfessionale, tipoPrestazione,
				tabella, condWhere, dbTipoCura, prestazioni, tabellaContatto, dtAperturaContatto, dtChiusuraContatto);		

		query.append(" UNION "+ queryContattoFis);
		
		tabellaContatto = "skfpg";
		dbTipoCura ="skfpg_motivo";
		dtAperturaContatto = "skfpg_data_apertura";
		dtChiusuraContatto = "skfpg_data_uscita";
		String queryContattoGen = recuperaQueryContatto(dbc, dtInizio, dtFine, figuraProfessionale, tipoPrestazione,
				tabella, condWhere, dbTipoCura, prestazioni, tabellaContatto, dtAperturaContatto, dtChiusuraContatto);		

		query.append(" UNION "+ queryContattoGen);

		LOG.trace(punto + " query>>" + query );
		
		return query.toString();
	}

	private String recuperaQueryContatto(ISASConnection dbc, String dtInizio, String dtFine,
			String figuraProfessionale, String tipoPrestazione, String tabella, 
			String condWhere, String dbTipoCura, boolean prestazioni, String tabellaContatto, String dtAperturaContatto, 
			String dtChiusuraContatto) {
		
		StringBuffer query = new StringBuffer(" SELECT v.int_tipo_oper ");

		if (prestazioni) {
			query.append(", p.pre_des_prest ");
		}

		query.append(", null as " + CTS_ADP + ", null as " + CTS_ARD + ",null as " + CTS_AID);
		query.append(", null as " + CTS_VSD + ", null as " + CTS_LIVELLO + ", " +dbTipoCura+" as "
				+ CTS_TIPOCURA+", '"+ CTS_SCHEDASENZA_SO_NO+ "' as " + CTS_SCHEDASENZA_SO);
		query.append(" FROM " +tabellaContatto + " r, interv v, ");
		if (prestazioni) {
			query.append("intpre p, ");
		}
		query.append(" anagra_c a, ");
		query.append(tabella);
		query.append(" WHERE r.n_cartella = a.n_cartella ");
		query.append(" AND NOT exists (SELECT 1 FROM rm_skso x WHERE x.n_cartella = r.n_cartella ");
		query.append(" AND x.pr_data_puac >= " +formatDate(dbc, dtInizio));
		query.append(" AND x.pr_data_puac <= " +formatDate(dbc, dtFine)+ " ) ");

		query.append(" AND A.DATA_VARIAZIONE IN (SELECT MAX(x.DATA_VARIAZIONE) FROM anagra_c x where x.n_cartella = r.n_cartella ");
		query.append(" AND x.data_variazione <= " + formatDate(dbc, dtFine) + ") ");

		query.append(" AND v.int_data_prest <= " + formatDate(dbc, dtFine));
		query.append(" and r.n_cartella = v.int_cartella ");
		if (prestazioni) {
			query.append(" AND v.int_anno = p.pre_anno AND v.int_contatore = p.pre_contatore ");
		}
		query.append(" AND r." +dtAperturaContatto+ " >= ");
		query.append(formatDate(dbc, dtInizio));
		query.append(" AND r." +dtAperturaContatto+" <= ");
		query.append(formatDate(dbc, dtFine));
		query.append(" AND v.int_data_prest >= r." +dtAperturaContatto+ " ");
		query.append(" AND ( r." +dtChiusuraContatto+ " IS NULL OR (r." +dtChiusuraContatto+" IS NOT NULL ");
		query.append(" AND v.int_data_prest <= r." +dtChiusuraContatto+ " ) ) ");

		if (ISASUtil.valida(tipoPrestazione)) {
			query.append(" AND v.int_ambdom = '" + tipoPrestazione + "' ");
		} else {
			//			PRESTAZIONI NON DOMICILIARI 
			query.append(" AND v.int_ambdom <> '" + CostantiSinssntW.CTS_STP_REPORT_TIPO_PRESTAZIONE_DOMICILIARE + "' ");
		}

		
		
		
		if (ISASUtil.valida(figuraProfessionale)) {
			query.append(" AND v.int_tipo_oper = '" + figuraProfessionale + "' ");
		}
		query.append(" AND " + condWhere);

		return query.toString();
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
				condWhere += " AND u.codice  = v.int_codpres ";
			}
		} else if (dom_res.equals("D")) {
			if (raggruppamento.equals("C")) {
				condWhere += " AND u.codice=a.dom_citta";
			} else if (raggruppamento.equals("A")) {
				condWhere += " AND u.codice=a.dom_areadis";
			} else if (raggruppamento.equals("P")) {
				condWhere += " AND u.codice  = v.int_codpres ";
			}
		} else if (dom_res.equals("R")) {
			if (raggruppamento.equals("C")) {
				condWhere += " AND u.codice=a.citta";
			} else if (raggruppamento.equals("A")) {
				condWhere += " AND u.codice=a.areadis";
			} else if (raggruppamento.equals("P")) {
				condWhere += " AND u.codice  = v.int_codpres ";
			}
		}
		return condWhere;
	}

}

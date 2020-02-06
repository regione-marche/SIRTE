package it.caribel.app.sinssnt.bean.nuovi;

import it.caribel.app.common.ejb.IntolleranzeAllergieEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.IntInfEJB;
import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.sinssnt.bean.nuovi.FiltroStampaRA.InformazioneColonna;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
public class FoReportElencoAssistitiEJB extends SINSSNTConnectionEJB {

	public FoReportElencoAssistitiEJB() {
	}
	private String ver = "60-";
	private Hashtable<String, String> datiIntensita = new Hashtable<String, String>();
	private Hashtable<String, String> datiTipoUtenza = new Hashtable<String, String>();
	private Hashtable<String, String> datiLivello = new Hashtable<String, String>();
	private static final String CTS_DT_PROROGA_CARTELLA ="dt_proroga_att";
	
	public static final String CTS_CP_ADP = "cp_adp";
	public static final String CTS_CP_ARD = "cp_ard";
	public static final String CTS_CP_AID = "cp_aid";
	public static final String CTS_CP_VSD = "cp_vsd";
	
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
			FiltroStampaRA filtroStampaRA = recuperaFiltroStampaRA(dbc,par);
			String query = recuperaQuery(dbc, par, filtroStampaRA);
			LOG.info(punto + " query>>" + query);
			dbcCur = dbc.startCursor(query);
			if (dbcCur == null || (dbcCur != null && dbcCur.getDimension() == 0)) {
				preparaLayout(eve, dbc, par);
				eve.write("messaggio");
				eve.write("finale");
			} else {
				preparaLayout(eve, dbc, par);
				caricaDatiPerStampa(dbc, filtroStampaRA);
				preparaBody(eve, dbcCur, dbc, par, filtroStampaRA);
				aggiungiConteggiStampa(eve, dbc, filtroStampaRA, query);
				eve.write("finale");
			}
			eve.close();
//			LOG.debug(punto + " html generato>>\n\n" + new String(eve.get()) + "\n\n");
			return eve.get();
		} catch (Exception e) {
			LOG.error(punto + " Errore nella stampa ", e);
			throw new SQLException("FoListaAttivitaEJB.query_attivita(): " + e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

	private void aggiungiConteggiStampa(mergeDocument eve, ISASConnection dbc, FiltroStampaRA filtroStampaRA, String query) {
		String punto = ver + "aggiungiConteggiStampa ";
		LOG.debug(punto + " Conteggio stampa ");
		String campo = "";
		String label = "";
		Vector<CampiDettaglio> campoLabel = new Vector<CampiDettaglio>();
		
		campo = CostantiSinssntW.N_CARTELLA;
		label = Labels.getLabel("report.elenco.assistiti.ncartelle.distinte");
		LOG.debug(punto + " condSelect>>" + campo+" "+label );
		CampiDettaglio campiDettaglio = new CampiDettaglio(campo, label, true, CampiDettaglio.CTS_TIPO_DECODIFICA, true);
		campoLabel.add(campiDettaglio);
		InformazioneColonna informazioneColonna;
		
		if (filtroStampaRA.isIntensita()) {
			informazioneColonna = filtroStampaRA.getInformazioneColonna(CostantiSinssntW.CTS_STP_REPORT_ASS_INTENSITA);
			
			Vector<String> labels = (Vector<String>)informazioneColonna.getEtichettaColonna();
			Vector<String> nomeCampi = (Vector<String>)informazioneColonna.getNomeCampo();
			campo = nomeCampi.get(0);
			label = labels.get(0);
			
			campiDettaglio =  new CampiDettaglio(campo, label, false, CampiDettaglio.CTS_TIPO_DECODIFICA_INTENSITA,false);
			campiDettaglio.setLabels(labels);
			campiDettaglio.setnomeCampi(nomeCampi);
			campoLabel.add(campiDettaglio);
			
//			for (int i = 0; i < nomeCampi.size(); i++) {
//				campo = nomeCampi.get(i);
//				label = labels.get(i);
//				if (i==0 ){/* devo stampare intensità assistenziale */
//					campiDettaglio =  new CampiDettaglio(campo, label, false, CampiDettaglio.CTS_TIPO_DECODIFICA_INTENSITA,false, labels, nomeCampi);
//					campoLabel.add(campiDettaglio);
//				}else {
//					LOG.debug(punto + " condSelect>>" + campo+" "+label );
//					/* caso 1 metto una riga vuota */
//					campiDettaglio =  new CampiDettaglio(campo, label, true, CampiDettaglio.CTS_TIPO_DECODIFICA_DATA, true);
//					campoLabel.add(campiDettaglio);
//				}
//			}
			
		}
		if (filtroStampaRA.isTipoUte()) {
			informazioneColonna = filtroStampaRA.getInformazioneColonna(CostantiSinssntW.CTS_STP_REPORT_ASS_TIPO_UTE);
			campo = filtroStampaRA.getNomeCampo(informazioneColonna);
			label = filtroStampaRA.getLabel(informazioneColonna);
			LOG.debug(punto + " condSelect>>" + campo+" "+label );
			campiDettaglio = new CampiDettaglio(campo, label, false, CampiDettaglio.CTS_TIPO_DECODIFICA_TIPO_UTENZA, false);
			campoLabel.add(campiDettaglio);
		}
		if (filtroStampaRA.isLivello()) {
			informazioneColonna = filtroStampaRA.getInformazioneColonna(CostantiSinssntW.CTS_STP_REPORT_ASS_LIVELLO );
			campo = filtroStampaRA.getNomeCampo(informazioneColonna);
			label = filtroStampaRA.getLabel(informazioneColonna);
			LOG.debug(punto + " condSelect>>" + campo+" "+label );
			campiDettaglio =  new CampiDettaglio(campo, label, false, CampiDettaglio.CTS_TIPO_DECODIFICA_LIVELLO, false);
			campoLabel.add(campiDettaglio);
		}
		
		if (filtroStampaRA.isAttive() || filtroStampaRA.isApertePeriodo()){
			campo = "dt_accett";
			label = Labels.getLabel("report.elenco.numero.assistiti.attivi");
			LOG.debug(punto + " condSelect>>" + campo+" "+label);
			campiDettaglio = new CampiDettaglio(campo, label, true, CampiDettaglio.CTS_TIPO_DECODIFICA_DATA, false);
			campoLabel.add(campiDettaglio);
		}
		
		if (filtroStampaRA.isConcluse() || filtroStampaRA.isChiusaPeriodo()){
			campo = "dt_chiusura";
			label = Labels.getLabel("report.elenco.numero.assistiti.concluse");
			LOG.debug(punto + " condSelect>>" + campo+" "+label);
			campiDettaglio = new CampiDettaglio(campo, label, true, CampiDettaglio.CTS_TIPO_DECODIFICA_DATA, false);
			campoLabel.add(campiDettaglio);
		}

		campo = "";
		label = Labels.getLabel("report.elenco.assistiti.numero.assistiti");
		LOG.debug(punto + " condSelect>>" + campo+" "+label );
		campiDettaglio = new CampiDettaglio(campo, label, true, CampiDettaglio.CTS_TIPO_DECODIFICA, false);
		campoLabel.add(campiDettaglio);
		stampaRiepilogoDati(dbc, eve, campoLabel, query);
	}

	private void stampaRiepilogoDati(ISASConnection dbc, mergeDocument eve, Vector<CampiDettaglio> campoLabel,
			String query) {
		eve.write("saltopagina");
		eve.write("tab_riepilogo");
		CampiDettaglio campiDettaglio;
		for (int i = 0; i < campoLabel.size(); i++) {
			campiDettaglio = campoLabel.get(i);
			
			recuperaNumeroElementi(dbc, query, campiDettaglio, eve);
		}
		eve.write("tab_riepilogo_fine");
	}

	private void recuperaNumeroElementi(ISASConnection dbc, String query, CampiDettaglio campiDettaglio, mergeDocument eve) {
			String punto = ver + "recuperaNumeroElementi ";
			StringBuffer queryConteggio = new StringBuffer();
			
			ISASCursor dbrConteggio =null;
			try {
				Hashtable<String, String> prtDati = new Hashtable<String, String>();
				String label;
				String numero;
				
			if (campiDettaglio.isDettaglio()) {
//				if (ISASUtil.valida(campiDettaglio.getCampoDb())) {
//					if (campiDettaglio.getTipoDecodifica()== CampiDettaglio.CTS_TIPO_DECODIFICA_DATA){
//						queryConteggio.append("select ");
//						queryConteggio.append(campiDettaglio.getCampoDb());
//						queryConteggio.append(" from ( ");
//					}else {
//						queryConteggio.append("select count(*) as numero from ( ");
//					}
//					queryConteggio.append(query);
//					queryConteggio.append(" ) ");
//					if (campiDettaglio.getTipoDecodifica()== CampiDettaglio.CTS_TIPO_DECODIFICA_DATA){
//						queryConteggio.append(" WHERE " + campiDettaglio.getCampoDb() +" IS NOT NULL ");
//					}else {
//						queryConteggio.append(" GROUP BY " + campiDettaglio.getCampoDb());
//					}
//				} else {
////					se voglio il numero dei record esaminati 
//					queryConteggio.append(query);
//				}
//
//				LOG.debug(punto + " query>>" + queryConteggio);
//				dbrConteggio = dbc.startCursor(queryConteggio.toString());
//				label = campiDettaglio.getLabel();
//				numero = "0";
//				if (dbrConteggio!=null){
//					numero = dbrConteggio.getAllRecord().size()+"";
//				}
				label = campiDettaglio.getLabel();
				numero = recuperaConteggio(dbc, campiDettaglio.getCampoDb(), campiDettaglio.getTipoDecodifica(), query);
				LOG.debug(punto + " campoDb>" + campiDettaglio.getCampoDb() + " " + label + "= " + numero);
				prtDati.put("#label#", label);
				prtDati.put("#numero#", numero + "");
				if (campiDettaglio.isRigaDettaglioTab()){
					eve.writeSostituisci("tab_riepilogo_righe", prtDati);
				}else {
					eve.writeSostituisci("tab_riepilogo_righe_intestazioni", prtDati);
				}
			} else {
				queryConteggio.append(" SELECT ");
				queryConteggio.append(campiDettaglio.getCampoDb());
				queryConteggio.append(" as label, count(*) as numero ");
				queryConteggio.append(" FROM (");
				queryConteggio.append(query);
				queryConteggio.append(" ) GROUP BY ");
				queryConteggio.append(campiDettaglio.getCampoDb());
				queryConteggio.append(" order by 1 asc ");
				LOG.debug(punto + " query>>" + queryConteggio);
				dbrConteggio = dbc.startCursor(queryConteggio.toString());
				boolean stampataIntestazione =false ;
				Vector<String> labels = campiDettaglio.getLabels();
				Vector<String> nomeCampi = campiDettaglio.getNomeCampi();
				boolean stampareDettPrestazionale;
				while (dbrConteggio.next()) {
					ISASRecord dbrDettaglio = dbrConteggio.getRecord();
					label = ISASUtil.getValoreStringa(dbrDettaglio, "label");
					
					stampareDettPrestazionale =  (ISASUtil.valida(label)&& label.equals(Costanti.CTS_COD_CURE_PRESTAZIONALI)&& labels!=null);
					
					numero = ISASUtil.getValoreStringa(dbrDettaglio, "numero");
					LOG.debug(punto + " campoDb>" + campiDettaglio.getCampoDb() + " " + label + "= " + numero+" stampareDettPrestazionale>"+stampareDettPrestazionale);
					
					if (!stampataIntestazione){
						prtDati.put("#label#", campiDettaglio.getLabel());
						prtDati.put("#numero#", "");
						eve.writeSostituisci("tab_riepilogo_righe_intestazioni", prtDati);
						stampataIntestazione = true;
					}
					
					label = decodificaLabelDettaglio(label, campiDettaglio);
					prtDati.put("#label#", label);
					prtDati.put("#numero#", numero + "");
					eve.writeSostituisci("tab_riepilogo_righe", prtDati);
					stampataIntestazione = true;
					
					if (stampareDettPrestazionale){
						recuperaDettagliCurePrestazionali(dbc, labels, nomeCampi, query, eve);
					}
					
				}
			}
			} catch (ISASMisuseException e) {
				e.printStackTrace();
			} catch (ISASPermissionDeniedException e) {
				e.printStackTrace();
			} catch (DBMisuseException e) {
				e.printStackTrace();
			} catch (DBSQLException e) {
				e.printStackTrace();
			}finally{
				close_dbcur_nothrow(punto, dbrConteggio);
			}
		}

	private void recuperaDettagliCurePrestazionali(ISASConnection dbc, Vector<String> labels, Vector<String> nomeCampi,
			String query, mergeDocument eve) throws ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException {
		String punto = ver + "recuperaDettagliCurePrestazionali ";

		String label, campo, numero;
		Hashtable<String, String> prtDati = new Hashtable<String, String>();
		for (int i = 1; i < nomeCampi.size(); i++) {
			campo = nomeCampi.get(i);
			label = labels.get(i);
			numero = recuperaConteggio(dbc, campo, CampiDettaglio.CTS_TIPO_DECODIFICA_DATA, query);
			LOG.debug(punto + " campoDb>" + campo + " " + label + "= " + numero);
			prtDati.put("#label#", label);
			prtDati.put("#numero#", numero + "");
			if (true) {
				eve.writeSostituisci("tab_riepilogo_righe", prtDati);
			}
		}

	}

	private String recuperaConteggio(ISASConnection dbc, String campoDb, int tipoDecodifica, String query)
			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException {
		String punto = ver + "recuperaConteggio ";
		StringBuffer queryConteggio = new StringBuffer();
		String numero = "0";
		ISASCursor dbrConteggio = null;
		try {

			if (ISASUtil.valida(campoDb)) {
				if (tipoDecodifica == CampiDettaglio.CTS_TIPO_DECODIFICA_DATA) {
					queryConteggio.append("select ");
					queryConteggio.append(campoDb);
					queryConteggio.append(" from ( ");
				} else {
					queryConteggio.append("select count(*) as numero from ( ");
				}
				queryConteggio.append(query);
				queryConteggio.append(" ) ");
				if (tipoDecodifica == CampiDettaglio.CTS_TIPO_DECODIFICA_DATA) {
					queryConteggio.append(" WHERE " + campoDb + " IS NOT NULL ");
				} else {
					queryConteggio.append(" GROUP BY " + campoDb);
				}
			} else {
				//			se voglio il numero dei record esaminati 
				queryConteggio.append(query);
			}

			LOG.debug(punto + " query>>" + queryConteggio);
			dbrConteggio = dbc.startCursor(queryConteggio.toString());

			numero = "0";
			if (dbrConteggio != null) {
				numero = dbrConteggio.getAllRecord().size() + "";
			}
		} finally {
			close_dbcur_nothrow(punto, dbrConteggio);
		}

		return numero;
	}


	private String decodificaLabelDettaglio(String valoreCampo, CampiDettaglio campiDettaglio) {
		String label ="";
		switch (campiDettaglio.getTipoDecodifica()) {
		case CampiDettaglio.CTS_TIPO_DECODIFICA_INTENSITA:
			label = ISASUtil.getValoreStringa(datiIntensita, valoreCampo);
			break;
		case CampiDettaglio.CTS_TIPO_DECODIFICA_LIVELLO:
			label = ISASUtil.getValoreStringa(datiLivello, valoreCampo);
			break;	
		case CampiDettaglio.CTS_TIPO_DECODIFICA_TIPO_UTENZA:
			label = ISASUtil.getValoreStringa(datiTipoUtenza, valoreCampo);
			break;	
		default:
			break;
		}
		return label;
	}
 
	private void caricaDatiPerStampa(ISASConnection dbc, FiltroStampaRA filtroStampaRA) {
		String punto = ver + "caricaDatiPerStampa ";
		LOG.debug(punto + " carico i dati ");
		if (filtroStampaRA.isIntensita()) {
			recuperaIntensitaAssistenziale(dbc);
		}
		if (filtroStampaRA.isTipoUte()) {
			recuperaDatiTipoUtenza(dbc);
		}
		if (filtroStampaRA.isLivello()) {
			recuperaDatiLivello(dbc);
		}
	}

	private void recuperaDatiTipoUtenza(ISASConnection dbc) {
		String punto = ver + "recuperaIntensitaAssistenziale ";
		TiputeSEJB tiputeSEJB = new TiputeSEJB();
		try {
			Vector<ISASRecord> dbrElementi = tiputeSEJB.recuperaDatiQuery(new Hashtable<String, String>(), dbc);
			String key, value;
			for (int i = 0; i < dbrElementi.size(); i++) {
				ISASRecord dbr = (ISASRecord) dbrElementi.get(i);
				key = ISASUtil.getValoreStringa(dbr, "codice");
				value = ISASUtil.getValoreStringa(dbr, "descrizione");
				datiTipoUtenza.put(key, value);
			}
			LOG.debug(punto + " datiTipoUtenza>>" + datiTipoUtenza + "<");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void recuperaIntensitaAssistenziale(ISASConnection dbc) {
		String punto = ver + "recuperaDatiTipoUtenza ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put("tab_cod", CostantiSinssntW.TAB_VAL_TIPOCURA);

		datiIntensita = recupeDatiTabVoci(dbc, dati);
		LOG.debug(punto + " datiIntensita>>" +datiIntensita);
	}

	
	private void recuperaDatiLivello(ISASConnection dbc) {
		String punto = ver + "recuperaDatiLivello ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put("tab_cod", CostantiSinssntW.TAB_VAL_LIVELLO_PRESA_CARICO);

		datiLivello= recupeDatiTabVoci(dbc, dati);
		LOG.debug(punto + " datiLivello>>" +datiLivello);

	}


	private Hashtable<String, String>  recupeDatiTabVoci(ISASConnection dbc, Hashtable<String, String> dati) {
		String punto = ver + "recupeDatiTabVoci ";
		Hashtable<String, String> datiCaricati = new Hashtable<String, String>();
		TabVociEJB tabVoci = new TabVociEJB();
		try {
			Vector<ISASRecord> dbrElementi =(Vector<ISASRecord>) tabVoci.recuperaDatiQuery(dati, dbc);
			String key, value;
			for (int i = 0; i < dbrElementi.size(); i++) {
				ISASRecord dbr = (ISASRecord) dbrElementi.get(i);
				key = ISASUtil.getValoreStringa(dbr, "tab_val");
				value = ISASUtil.getValoreStringa(dbr, "tab_descrizione");
				datiCaricati.put(key, value);
			}
			LOG.debug(punto + " datiCaricati>>" + datiCaricati + "<");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return datiCaricati;
	}

	private void preparaLayout(mergeDocument md, ISASConnection dbc, Hashtable<String, String> par) {
		Hashtable<String, String> htxt = new Hashtable<String, String>();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));

			String dadata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
			String adata = "" + par.get(CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);
			String periodo = "";
			if (ManagerDate.validaData(dadata)) {
				periodo = "Dal " + ManagerDate.formattaDataIta(dadata, "/");
			}
			if (ManagerDate.validaData(adata)) {
				periodo += (ISASUtil.valida(periodo) ? " Al " : "Fino al ") + ManagerDate.formattaDataIta(adata, "/");
			}
			htxt.put("#periodo#", periodo);
		} catch (Exception ex) {

		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		md.writeSostituisci("layout", htxt);
	}

	private void preparaBody(mergeDocument md, ISASCursor dbcCursor, ISASConnection dbc, Hashtable<String, String> par,
			FiltroStampaRA filtroStampaRA) {
		String punto = ver + "preparaBody ";
		LOG.info(punto + " procedo ");
		inizializzaTabella(md, filtroStampaRA);
		Vector<InformazioneColonna> elementiDaStampare = filtroStampaRA.getElementiDaStampare();
		try {
			int assistito = 0;
			while (dbcCursor.next()) {
				if (assistito>0){
					md.write("saltopagina");
				}
				md.write("tabella_corpo_intestazione");
				ISASRecord dbrDati = (ISASRecord) dbcCursor.getRecord();
				String nomeCampo, label;
				for (int i = 0; i < elementiDaStampare.size(); i++) {
					InformazioneColonna informazioneColonna = elementiDaStampare.get(i);
					Vector<String> datiColonna = informazioneColonna.getNomeCampo();
					Vector<String> etichetteColonne = informazioneColonna.getEtichettaColonna();
					for (int j = 0; j < datiColonna.size(); j++) {
						nomeCampo = datiColonna.get(j);
						label = etichetteColonne.get(j);
						inserisciDatiCorpo(md, dbrDati, nomeCampo, filtroStampaRA, dbc, label);
					}
				}
				md.write("tabella_corpo_fine");
				assistito++;
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		md.write("tabella_fine_din");
	}

	private void inserisciDatiCorpo(mergeDocument md, ISASRecord dbrDati, String key, FiltroStampaRA filtroStampaRA,
			ISASConnection dbc, String label) throws ISASMisuseException {
		Hashtable<String, String> dati = new Hashtable<String, String>();
		if (ISASUtil.valida(key)) {
			String valoreDaVisualizzare = ISASUtil.getValoreStringa(dbrDati, key);
			if (filtroStampaRA.isIntensita() && key.equalsIgnoreCase(FiltroStampaRA.C_INTENSITA)) {
				valoreDaVisualizzare = ISASUtil.getValoreStringa(datiIntensita, valoreDaVisualizzare);
			} else if (filtroStampaRA.isTipoUte() && key.equalsIgnoreCase(FiltroStampaRA.C_TIPO_UTE)) {
				valoreDaVisualizzare = ISASUtil.getValoreStringa(datiTipoUtenza, valoreDaVisualizzare);
			} else if (filtroStampaRA.isPai() && key.equals(CostantiSinssntW.CTS_STP_REPORT_ASS_PAI)) {
				valoreDaVisualizzare = recuperaDatiPai(dbc, dbrDati);
			} else if (filtroStampaRA.isDiario() && key.equals(CostantiSinssntW.CTS_STP_REPORT_ASS_DIARIO)) {
				valoreDaVisualizzare = recuperaDatiDiario(dbc, dbrDati);
			} else if (filtroStampaRA.isSegnalazione() && key.equals(CostantiSinssntW.CTS_STP_REPORT_ASS_SEGNALAZIONE)) {
				valoreDaVisualizzare = recuperaDatiSegnalazione(dbc, dbrDati);
			} else if (filtroStampaRA.isIntolleranze() && key.equals(CostantiSinssntW.CTS_STP_REPORT_ASS_INTOLLERANZE)) {
				valoreDaVisualizzare = recuperaDatiIntolleranze(dbc, dbrDati);
			} else if (filtroStampaRA.isSospese() && key.equals(CostantiSinssntW.CTS_STP_REPORT_ASS_SOSPESE)) {
				valoreDaVisualizzare = recuperaDatiSospensioni(dbc, dbrDati);
			} else if (filtroStampaRA.isProroghe() && key.equals(CostantiSinssntW.CTS_STP_REPORT_ASS_ATTIVE)) {
				valoreDaVisualizzare = recuperaDatiProroghe(dbc, dbrDati);
				if (ManagerDate.validaData(valoreDaVisualizzare)){
					dbrDati.put(CTS_DT_PROROGA_CARTELLA , valoreDaVisualizzare);
				}
			} else if (filtroStampaRA.isLivello() && key.equals(FiltroStampaRA.C_LIVELLO)) {
				valoreDaVisualizzare = ISASUtil.getValoreStringa(datiLivello, valoreDaVisualizzare);
			}else if (filtroStampaRA.isRiepiloAccessi() && key.equals(FiltroStampaRA.C_ACCESSI_EFFETTUATI)) {
				valoreDaVisualizzare = recuperaDatiAccessiEffettuati(dbc, dbrDati, filtroStampaRA);
			}else if (filtroStampaRA.isRiepiloAccessi() && key.equals(FiltroStampaRA.C_ACCESSI_EFFETTUATI_SIAD)) {
				dbrDati.put(CostantiSinssntW.CTS_ACCESSI_FLUSSI_SIAD_INVIATO, new Boolean(true));
				valoreDaVisualizzare = recuperaDatiAccessiEffettuati(dbc, dbrDati, filtroStampaRA);
//				riazzero il valore 
				dbrDati.put(CostantiSinssntW.CTS_ACCESSI_FLUSSI_SIAD_INVIATO, new Boolean(false));
			}else if (key.equals(FiltroStampaRA.C_COD_MEDICO_DESC)){
				valoreDaVisualizzare = recuperaInfoMedico(dbc, valoreDaVisualizzare);
			}else if (key.equals(FiltroStampaRA.C_FLUSSI_SIAD)){
				valoreDaVisualizzare = decodificaStatoFlussoSiad(dbc, valoreDaVisualizzare);
			}else if (key.equals(FiltroStampaRA.C_ADP) || key.equals(FiltroStampaRA.C_AID) || 
					key.equals(FiltroStampaRA.C_ARD) || key.equals(FiltroStampaRA.C_VSD)){
				if (ISASUtil.valida(valoreDaVisualizzare)){
					valoreDaVisualizzare = Labels.getLabel(Costanti.CTS_LABEL_SI);
				}
			}
			dati.put("#label#", label);
			recuperaDati(dati, valoreDaVisualizzare);
			md.writeSostituisci("tabella_corpo_dati", dati);
		}
	}

	private String decodificaStatoFlussoSiad(ISASConnection dbc, String valoreDaVisualizzare) {
		String[] sost= new  String[]{valoreDaVisualizzare};
		String decodifica = Labels.getLabel(CostantiSinssntW.STP_ASSISTITI_FLUSSO_SIAD_NON_INVIATO, sost);
		if (ISASUtil.valida(valoreDaVisualizzare)){
			if (valoreDaVisualizzare.equals(CostantiSinssntW.FLAG_DA_INVIARE_V) || 
					valoreDaVisualizzare.equals(CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO)){
				decodifica = Labels.getLabel(CostantiSinssntW.STP_ASSISTITI_FLUSSO_SIAD_INVIATO, sost);
			}
		}
		return decodifica;
	}

	private String recuperaInfoMedico(ISASConnection dbc, String codMedico) {
		String punto = ver + "recuperaInfoMedico ";
		String infoMedico = "";
		if (ISASUtil.valida(codMedico)){
			try {
				infoMedico = ISASUtil.getDecode(dbc, "medici", "mecodi", codMedico,
							"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))","mmg_alias");
			} catch (Exception e) {
				LOG.error(punto + " Errore nel recuperare info medico con codice>>" +codMedico, e);
			}
		}
		return infoMedico;
	}

	private String recuperaDatiAccessiEffettuati(ISASConnection dbc, ISASRecord dbrDati, FiltroStampaRA filtroStampaRA) {
		String punto = ver + "recuperaDatiAccessiEffettuati ";
		IntInfEJB intInfEJB = new IntInfEJB();
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);
		Hashtable<String, Object> prtDati = new Hashtable<String, Object>();
		String dtProroga = ISASUtil.getValoreStringa(dbrDati, CTS_DT_PROROGA_CARTELLA);
		if(!ManagerDate.validaData(dtProroga) && !filtroStampaRA.isProroghe()) {
			dtProroga = recuperaDatiProroghe(dbc, dbrDati);
		}
		
		String dtFinePeriodo = "";
		if (ManagerDate.validaData(dtProroga)){
			dtFinePeriodo = dtProroga;
		}else {
			dtFinePeriodo = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.DATA_FINE_PIANO);
		}
		prtDati.put(CostantiSinssntW.CTS_INT_CARTELLA, nCartella);
		prtDati.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO,ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.DATA_INIZIO_PIANO));
		prtDati.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE,dtFinePeriodo);
		prtDati.put(CostantiSinssntW.CTS_INT_CONTATTO,"");
		prtDati.put(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA, new Boolean(filtroStampaRA.isSO()));
		prtDati.put(CostantiSinssntW.CTS_ACCESSI_FLUSSI_SIAD_INVIATO, ISASUtil.getvaloreBoolean(dbrDati.getHashtable(), CostantiSinssntW.CTS_ACCESSI_FLUSSI_SIAD_INVIATO));
		
		int numeroAccessi = 0;
		try {
			numeroAccessi= intInfEJB.recuperaNumeroAccessi(dbc, prtDati);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(punto + "Errore nel recuperare gli accessi ", e);
		}
		String result = ""+(numeroAccessi>0 ? numeroAccessi+"":0);
		
		return result;
	}

	private String recuperaDatiProroghe(ISASConnection dbc, ISASRecord dbrDati) {
		RMSkSOSKSoProrogheEJB rmSkSOSKSoProrogheEJB = new RMSkSOSKSoProrogheEJB();
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.ID_SKSO);

		boolean esisteProrogha = rmSkSOSKSoProrogheEJB.esisteProroghe(dbc, nCartella, idSkso);
		return (esisteProrogha ? "X" : "");
	}

	private String recuperaDatiSospensioni(ISASConnection dbc, ISASRecord dbrDati) {
		RMSkSOSKSoSospensioniEJB rmSkSOSKSoSospensioniEJB = new RMSkSOSKSoSospensioniEJB();
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.ID_SKSO);

		boolean esisteSospensione = rmSkSOSKSoSospensioniEJB.esisteSospensione(dbc, nCartella, idSkso);
		return (esisteSospensione ? "X" : "");
	}

	private String recuperaDatiIntolleranze(ISASConnection dbc, ISASRecord dbrDati) {
		IntolleranzeAllergieEJB intolleranzeAllergieEJB = new IntolleranzeAllergieEJB("RM_INTOLLERANZE_ALLERGIE");
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);

		boolean esisteIntolleranzaAllergia = intolleranzeAllergieEJB.esisteIntolleranzeAllergie(dbc, nCartella);
		return (esisteIntolleranzaAllergia ? "X" : "");
	}

	private String recuperaDatiSegnalazione(ISASConnection dbc, ISASRecord dbrDati) {
		SegnalazioniEJB segnalazioniEJB = new SegnalazioniEJB();
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.ID_SKSO);

		boolean esisteSegnalazione = segnalazioniEJB.esisteSegnalazione(dbc, nCartella, idSkso);
		return (esisteSegnalazione ? "X" : "");
	}

	private String recuperaDatiDiario(ISASConnection dbc, ISASRecord dbrDati) {
		RMDiarioEJB rmDiarioEJB = new RMDiarioEJB();
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.ID_SKSO);

		boolean esisteDiario = rmDiarioEJB.esisteDiario(dbc, nCartella, idSkso);
		return (esisteDiario ? "X" : "");
	}

	private String recuperaDatiPai(ISASConnection dbc, ISASRecord dbrDati) {
		PAIEJB paiejb = new PAIEJB();
		String nCartella = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dbrDati, FiltroStampaRA.ID_SKSO);
		boolean esistePai = paiejb.esistePai(dbc, nCartella, idSkso);

		return (esistePai ? "X" : "");
	}

	private void inizializzaTabella(mergeDocument md, FiltroStampaRA filtroStampaRA) {
		md.write("tabella_ini_din");
		Vector<InformazioneColonna> elementiDaStampare = filtroStampaRA.getElementiDaStampare();

		for (int i = 0; i < elementiDaStampare.size(); i++) {
			InformazioneColonna informazioneColonna = elementiDaStampare.get(i);
			Vector<String> etichetteColonne = informazioneColonna.getEtichettaColonna();
			for (int j = 0; j < etichetteColonne.size(); j++) {
				intestazioneTabella(md, etichetteColonne.get(j));
			}
		}
		md.write("tabella_fine_colonna_din");
	}

	private void intestazioneTabella(mergeDocument md, String colonna) {
		String punto = ver + "intestazioneTabella ";
		LOG.debug(punto + " colonna da stampare >>" + colonna);

		Hashtable<String, String> datiTabella = new Hashtable<String, String>();
		datiTabella.put("#label#", colonna);
		md.writeSostituisci("tabella_colonna_din", datiTabella);
	}

	private void recuperaDati(Hashtable<String, String> dati, String valore) {
		dati.put("#valore#", valore);
	}

	private FiltroStampaRA recuperaFiltroStampaRA(ISASConnection dbc, Hashtable<String, String> par)throws Exception {
		FiltroStampaRA filtroStampaRA = new FiltroStampaRA(dbc, par);
		return filtroStampaRA;
	}

	private String recuperaQuery(ISASConnection dbc, Hashtable<String, String> par, FiltroStampaRA filtroStampaRA) {
		String punto = ver + "recuperaQuery ";
		ServerUtility su = new ServerUtility();
		LOG.debug(punto + "Inizio con dati>>" + par);
		String dataInizio = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO);
		String dataFine = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DATA_FINE);
		String codMedico = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_COD_MEDICO);
		String zona = "";
		String distretto = "";
		String comune = "";
		String raggruppamento = (String) par.get("ragg");

		zona = " u.cod_zona,u.des_zona, ";
		distretto = " u.des_distretto," + "u.cod_distretto" + " as cod_distretto, ";
		comune = " u.codice ,u.descrizione ";
		String tabella = ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc"
				: "ubicazioni_n") + " u";
		String condSelect = "";

		StringBuffer query = new StringBuffer();
		query.append("select s.n_cartella " + FiltroStampaRA.N_CARTELLA);
		query.append(", a."+FiltroStampaRA.C_COD_MEDICO +", a.cod_med as cod_med_desc ");
		query.append(", s.id_skso " + FiltroStampaRA.ID_SKSO);
		query.append(", NVL(to_char(data_nasc ,'DD/MM/YYYY'),'') " + FiltroStampaRA.C_DATA_NASCITA);
		query.append(", sesso " + FiltroStampaRA.C_SESSO);
		query.append(", cod_fisc " + FiltroStampaRA.C_CODICE_FISCALE);
		query.append(", "+ zona + distretto + comune);
		query.append(", NVL(to_char(s.pr_data_puac ,'DD/MM/YYYY'),'') " + FiltroStampaRA.DATA_ACCETTAZIONE);
		query.append(", c.cognome " + FiltroStampaRA.COGNOME);
		query.append(", c.nome " + FiltroStampaRA.NOME);
		query.append(", NVL(to_char(m.data_inizio ,'DD/MM/YYYY'),'') " + FiltroStampaRA.DATA_INIZIO_PIANO);
		query.append(", NVL(to_char(m.data_fine ,'DD/MM/YYYY'),'') " + FiltroStampaRA.DATA_FINE_PIANO);
		query.append(", NVL(to_char(data_presa_carico_skso ,'DD/MM/YYYY'),'') " + FiltroStampaRA.DATA_ATTIVAZIONE);
		query.append(", NVL(to_char(s.pr_data_chiusura ,'DD/MM/YYYY'),'') " + FiltroStampaRA.C_DATA_CHIUSURA);
		query.append(", s.flag_sent ");

		if (filtroStampaRA.isIntensita()) {
			condSelect = filtroStampaRA.getSelect(CostantiSinssntW.CTS_STP_REPORT_ASS_INTENSITA);
			LOG.debug(punto + " condSelect>>" + condSelect);
			query.append(", " + condSelect);
		}
		if (filtroStampaRA.isTipoUte()) {
			condSelect = filtroStampaRA.getSelect(CostantiSinssntW.CTS_STP_REPORT_ASS_TIPO_UTE);
			LOG.debug(punto + " condSelect>>" + condSelect);
			query.append(", " + condSelect);
		}
		if (filtroStampaRA.isLivello()) {
			condSelect = filtroStampaRA.getSelect(CostantiSinssntW.CTS_STP_REPORT_ASS_LIVELLO);
			LOG.debug(punto + " condSelect>>" + condSelect);
			query.append(", " + condSelect);
		}
		if(filtroStampaRA.isRivalutazione()){
			condSelect = filtroStampaRA.getSelect(CostantiSinssntW.CTS_STP_REPORT_ASS_RIVALUTAZIONE);
			LOG.debug(punto + " condSelect>>" + condSelect);
			query.append(", " + condSelect);
		}
		 
		query.append(" from rm_skso s,rm_skso_mmg m,cartella c, anagra_c a, ");
		query.append(tabella);
		query.append(" where s.N_CARTELLA=m.N_CARTELLA and s.ID_SKSO=m.ID_SKSO");
		query.append(" and c.n_cartella=m.n_cartella ");
		
		if ((filtroStampaRA.isAttive() || filtroStampaRA.isConcluse())){
			if (filtroStampaRA.isAttive()) {
				if (ManagerDate.validaData(dataFine)) {
					query.append(" AND s.pr_data_puac <= " + formatDate(dbc, dataFine)) ;
				}
				if (ManagerDate.validaData(dataInizio)) {
					query.append(" AND ( ( s.pr_data_chiusura IS NOT NULL AND s.pr_data_chiusura >= ");
					query.append(formatDate(dbc, dataInizio));
					query.append(" )  OR s.pr_data_chiusura IS NULL  ) ");
				} else {
					query.append(" and ( s.pr_data_chiusura is null ) ");
				}
			}
			if(filtroStampaRA.isConcluse()){
				query.append(" and ( s.pr_data_chiusura is not null ) ");
			}
		}else {
			LOG.debug(punto + " non metto la condizione sulla data in quanto una scheda o e attiva oppure e conclusa.");
		}

		if (filtroStampaRA.isChiusaPeriodo()) {
			if(ManagerDate.validaData(dataInizio)){
				query.append(" AND s.pr_data_chiusura >= ");
				query.append(dbc.formatDbDate(dataInizio));
			}
			if (ManagerDate.validaData(dataFine)){
				query.append(" AND s.pr_data_chiusura <= " );
				query.append(dbc.formatDbDate(dataFine));
			}
//			metto questa condizione, nel caso in cui non fossere valorizzati le date inizio, poco probabile in quanto sono obbligatorie 
			//query.append(" AND s.pr_data_chiusura is not null ");
		}
		
		if (filtroStampaRA.isApertePeriodo()) {
			if (ManagerDate.validaData(dataFine)){
				query.append(" AND s.pr_data_puac <= ");
				query.append(formatDate(dbc, dataFine)); 
			}
			
			if (ManagerDate.validaData(dataInizio)){
				query.append(" AND s.pr_data_puac >= ");
				query.append(formatDate(dbc, dataInizio));
			}
			
			query.append(" AND (s.pr_data_chiusura IS NULL ");
			if (ManagerDate.validaData(dataInizio)){
				query.append(" OR s.pr_data_chiusura >= ");
				query.append(formatDate(dbc, dataInizio));
			}
			query.append(")"); 
		}

		if (filtroStampaRA.isTipoUte()) {
			String tipoUtenza = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_ASS_COD_TIPO_UTE);
			if (ISASUtil.valida(tipoUtenza)) {
				query.append(" AND m.tipo_ute = '" + tipoUtenza + "' ");
			}
		}
		if (filtroStampaRA.isIntensita()) {
			String tipoCura = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_ASS_COD_TIPOCURA);
			if (ISASUtil.valida(tipoCura)) {
				query.append(" AND m.tipocura = '" + tipoCura + "' ");
			}
		}
		String condPrest = "";

		if (filtroStampaRA.isAdp()) {
			condPrest = " m.adp = '" + Costanti.CTS_S + "' ";
		}
		if (filtroStampaRA.isAid()) {
			condPrest += (ISASUtil.valida(condPrest) ? " AND " : "") + " m.aid = '" + Costanti.CTS_S + "' ";
		}
		if (filtroStampaRA.isArd()) {
			condPrest += (ISASUtil.valida(condPrest) ? " AND " : "") + " m.ard = '" + Costanti.CTS_S + "' ";
		}
		if (filtroStampaRA.isVsd()) {
			condPrest += (ISASUtil.valida(condPrest) ? " AND " : "") + " m.vsd = '" + Costanti.CTS_S + "' ";
		}
		if (ISASUtil.valida(condPrest)) {
			query.append(" AND ( " + condPrest + " ) ");
		}

		if (filtroStampaRA.isLivello()) {
			String codLivello = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_ASS_COD_LIVELLO);
			if (ISASUtil.valida(codLivello)) {
				query.append(" AND s.presa_carico_livello = '" + codLivello + "' ");
			}
		}

		if (filtroStampaRA.isFinePiano()) {
			if (ManagerDate.validaData(dataInizio) || ManagerDate.validaData(dataFine)){
				if (ManagerDate.validaData(dataInizio)){
					query.append(" AND ( m.data_fine >= ");
					query.append(dbc.formatDbDate(dataInizio));
				}
				if (ManagerDate.validaData(dataFine)){
					query.append(" AND m.data_fine <= ");
					query.append(dbc.formatDbDate(dataFine) +" ) ");
				}
			}else {
				query.append(" AND m.data_fine is not null ");
			}
		}
		
		if (filtroStampaRA.isRivalutazione()) {
			if (ManagerDate.validaData(dataInizio) || ManagerDate.validaData(dataFine)){
				if (ManagerDate.validaData(dataInizio)){
					query.append(" AND s.pr_data_revisione >= ");
					query.append(dbc.formatDbDate(dataInizio));
				}
				if (ManagerDate.validaData(dataFine)){
					query.append(" AND s.pr_data_revisione <= ");
					query.append(dbc.formatDbDate(dataFine));
				}
			}else {
				query.append(" AND s.pr_data_revisione is not null ");
			}	
		}
		
		if (filtroStampaRA.isFlussiSiadInviato()){
			query.append(" AND s.flag_sent in ( "+ CostantiSinssntW.FLAG_DA_INVIARE_V+", "+ CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO +")");
		}
		
		if (filtroStampaRA.isFlussiSiadNonInviato()){
			query.append(" AND s.flag_sent not in ( "+ CostantiSinssntW.FLAG_DA_INVIARE_V+", "+ CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO +")");
		}
		
		String condWhere = "";
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));
		raggruppamento = (String) par.get("ragg");
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, raggruppamento);
		String dom_res = ISASUtil.getValoreStringa(par, "dom_res");
		// Aggiunto Controllo Domicilio/Residenza (BYSP)
		if (!ISASUtil.valida(dom_res)) {
			if (raggruppamento.equals("C")){
				condWhere += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
						+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
			} else if (raggruppamento.equals("A")){
				condWhere += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"
						+ " AND u.codice=a.dom_areadis)" + " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "
						+ " AND u.codice=a.areadis))";
			} else if (raggruppamento.equals("P")){
				condWhere += " AND u.codice  = s.cod_presidio ";
			}
		} else if (dom_res.equals("D")) {
			if (raggruppamento.equals("C")){
				condWhere += " AND u.codice=a.dom_citta";
			} else if (raggruppamento.equals("A")){
				condWhere += " AND u.codice=a.dom_areadis";
			}else if (raggruppamento.equals("P")){
				condWhere += " AND u.codice  = s.cod_presidio ";
			}
		}

		else if (dom_res.equals("R")) {
			if (raggruppamento.equals("C")){
				condWhere += " AND u.codice=a.citta";
			}else if (raggruppamento.equals("A")){
				condWhere += " AND u.codice=a.areadis";
			}else if (raggruppamento.equals("P")){
				condWhere += " AND u.codice  = s.cod_presidio ";
			}
		}
		if (ISASUtil.valida(codMedico)){
			query.append(" AND a.cod_med = '" +codMedico+ "' ");
		}
		query.append(" AND " + condWhere);

		query.append(" AND s.n_cartella=a.n_cartella AND a.data_variazione IN (SELECT MAX(ac.data_variazione)");
		query.append(" FROM anagra_c ac WHERE ac.n_cartella=a.n_cartella ");
		
		if (ManagerDate.validaData(dataFine)) {
			query.append("AND ac.data_variazione<= ");
			query.append(formatDate(dbc, dataFine));
		}else {
			query.append(" AND (M.DATA_FINE IS NULL OR (M.DATA_FINE IS NOT NULL AND AC.DATA_VARIAZIONE <= M.DATA_FINE ) ) ");
		}

		query.append(" ) ");
		query.append(" ORDER BY u.des_zona, des_distretto, descrizione, cognome, nome  ");
//				"s.pr_data_puac
		LOG.debug(punto + " query>> " + query);
   
		return query.toString();
	}

}

class CampiDettaglio {
	String campoDb;
	String label;
	int tipoDecodifica = -1;
	boolean dettaglio;
	boolean rigaDettaglioTab = true;
	public static final int CTS_TIPO_DECODIFICA = -1;
	public static final int CTS_TIPO_DECODIFICA_LIVELLO=1;
	public static final int CTS_TIPO_DECODIFICA_INTENSITA=2;
	public static final int CTS_TIPO_DECODIFICA_TIPO_UTENZA=3;
	public static final int CTS_TIPO_DECODIFICA_DATA = 4;
	
	Vector<String> labels = null;
	Vector<String> nomeCampi = null;
	

	public String getLabel() {
		return label;
	}

	public void setnomeCampi(Vector<String> nomeCampi) {
		this.nomeCampi = nomeCampi;
	}

	public void setLabels(Vector<String> labels) {
		this.labels = labels;
		
	}

	public boolean isRigaDettaglioTab() {
		return rigaDettaglioTab;
	}

	public boolean isDettaglio() {
		return dettaglio;
	}

	public CampiDettaglio(String campoDb, String label, boolean dettaglio, int tipoDecodifica, boolean rigaDettaglioTab) {
		super();
		this.campoDb = campoDb;
		this.label = label;
		this.dettaglio = dettaglio;
		this.tipoDecodifica = tipoDecodifica;
		this.rigaDettaglioTab= rigaDettaglioTab;
	}

	public String getCampoDb() {
		return campoDb;
	}

	public int getTipoDecodifica() {
		return tipoDecodifica;
	}

	public Vector<String> getLabels() {
		return labels;
	}

	public Vector<String> getNomeCampi() {
		return nomeCampi;
	}

}

package it.caribel.app.sinssnt.bean.nuovi;

import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.util.ISASUtil;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
public class FiltroStampaRA {

	private boolean apertePeriodo = false;
	private boolean chiusaPeriodo = false;
	private boolean pai = false;
	private boolean diario = false;
	private boolean intolleranze = false;
	private boolean tipoUte = false;
	private boolean segnalazione = false;
	private boolean intensita = false;
	private boolean adp = false;
	private boolean ard= false;
	private boolean aid= false;
	private boolean vsd= false;
	private boolean attive = false;
	private boolean concluse = false;
	private boolean sospese = false;
	private boolean proroghe = false;
	private boolean finePiano = false;
	private boolean rivalutazione = false;
	private boolean livello = false;
	private boolean riepiloAccessi = false;
	private boolean flussiSiadInviato = false;
	private boolean flussiSiadNonInviato = false;
	private boolean isSO = false;
	private String tipoOperatore = "";
	
	private Vector<InformazioneColonna> elementiDaStampare = new Vector<InformazioneColonna>();
	/* proprietà attive, posizione che contiene informazioni colonna */
	private Hashtable<String, Integer> nomeColonna = new Hashtable<String, Integer>();
	
	public static final String C_ADP = "c_adp";
	public static final String C_ARD = "c_ard";
	public static final String C_AID = "c_aid";
	public static final String C_VSD = "c_vsd";

	public static final String C_TIPO_UTE = "tipo_ute";
	public static final String C_INTENSITA = "tipocura";
	public static final String LIVELLO = "livello";
	public static final String C_DATA_CHIUSURA = "dt_chiusura";
	public static final String C_COD_MEDICO = "cod_med";
	public static final String C_COD_MEDICO_DESC = "cod_med_desc";
	public static final String C_FLUSSI_SIAD = "flag_sent";
	public static final String DATA_ACCETTAZIONE = "dt_accett";
	public static final String DATA_ATTIVAZIONE = "dt_attivaz";
	public static final String DATA_INIZIO_PIANO = "dt_iniz_piano";
	public static final String DATA_FINE_PIANO = "dt_fine_piano";
	public static final String COGNOME = "cognome";
	public static final String N_CARTELLA = "n_cartella";
	public static final String ID_SKSO = "id_skso";
	public static final String NOME = "nome";
	public static final String PR_OBIETTIVO = "pr_obiettivo";
	public static final String PR_DATA_CHIUSURA = "pr_data_chiusura";
	public static final String VAL_DIARIO_ = "diario";
	public static final String C_PAI = "c_pai";
	public static final String C_DIARIO = "c_diario";
	public static final String C_LIVELLO = "livello";
	public static final String C_ACCESSI_EFFETTUATI = "acc_effettuati";
	public static final String C_ACCESSI_EFFETTUATI_SIAD = "acc_effettuati_siad";

	public static final String C_ZONA = "des_zona";
	public static final String C_DISTRETTO = "des_distretto";
	public static final String C_RAGGRUPPAMENTO = "descrizione";
	public static final String C_DATA_NASCITA = "dt_nascita";
	public static final String C_CODICE_FISCALE = "cod_fisca";
	public static final String C_SESSO = "sess";

	public FiltroStampaRA(ISASConnection dbc, Hashtable<String, String> par)throws Exception {
		campiDefault(par);
		Vector<String> nomeCampo = new Vector<String>();
		Vector<String> campoDb = new Vector<String>();
		Vector<String> labels = new Vector<String>();
		apertePeriodo = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_APERTE);
		chiusaPeriodo = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_CHIUSE);
		flussiSiadInviato = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_FLAG_SIAD_CHECK_INVIATI);
		flussiSiadNonInviato = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_FLAG_SIAD_CHECK_NO_INVIATI);

		isSO = ISASUtil.getvaloreBoolean(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
		tipoOperatore = ISASUtil.getValoreStringa(par, CostantiSinssntW.TIPO_OPERATORE);
		
		pai = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_PAI);
		if (pai) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>(); 

			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_PAI);
			campoDb.add("");
			labels.add("report.elenco.assistiti.pai");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_PAI, elementiDaStampare.size());
		}

		diario = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_DIARIO);
		if (diario) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_DIARIO);
			campoDb.add("");
			labels.add("report.elenco.assistiti.diario");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_DIARIO, elementiDaStampare.size());
		}

		segnalazione = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_SEGNALAZIONE);
		if (segnalazione) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_SEGNALAZIONE);
			campoDb.add("");
			labels.add("report.elenco.assistiti.segnalazione");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_SEGNALAZIONE, elementiDaStampare.size());
		}

		intolleranze = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_INTOLLERANZE);
		if (intolleranze) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_INTOLLERANZE);
			campoDb.add("");
			labels.add("report.elenco.assistiti.intolleranze");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_INTOLLERANZE, elementiDaStampare.size());
		}

		adp = ISASUtil.getvaloreBoolean(par, FoReportElencoAssistitiEJB.CTS_CP_ADP);
		aid = ISASUtil.getvaloreBoolean(par, FoReportElencoAssistitiEJB.CTS_CP_AID);
		ard = ISASUtil.getvaloreBoolean(par, FoReportElencoAssistitiEJB.CTS_CP_ARD);
		vsd = ISASUtil.getvaloreBoolean(par, FoReportElencoAssistitiEJB.CTS_CP_VSD);
		
		intensita = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_INTENSITA);
		if (intensita) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			
			nomeCampo.add(C_INTENSITA);
			campoDb.add("m.tipocura");
			labels.add("RichiestaMMG.principale.intensita_ass");
			
			String tipoCura = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_ASS_COD_TIPOCURA);
			if (!ISASUtil.valida(tipoCura)
					|| ((ISASUtil.valida(tipoCura) && tipoCura.equals(Costanti.CTS_COD_CURE_PRESTAZIONALI)))) {
			
				nomeCampo.add(C_ADP);
				campoDb.add("m.adp");
				labels.add("RichiestaMMG.principale.adp");

				nomeCampo.add(C_ARD);
				campoDb.add("m.ard");
				labels.add("RichiestaMMG.principale.ard");

				nomeCampo.add(C_AID);
				campoDb.add("m.aid");
				labels.add("RichiestaMMG.principale.aid");

				nomeCampo.add(C_VSD);
				campoDb.add("m.vsd");
				labels.add("RichiestaMMG.principale.vsd");
			}

			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_INTENSITA, elementiDaStampare.size());
		}

		tipoUte = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_TIPO_UTE);
		if (tipoUte) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(C_TIPO_UTE);
			campoDb.add("m.tipo_ute");
			labels.add("SchedaInfForm.principale.tipoUtenza");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_TIPO_UTE, elementiDaStampare.size());
		}
		livello = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_LIVELLO); 
		if (livello) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(C_LIVELLO);
			campoDb.add("s.presa_carico_livello");
			labels.add("menu.segreteria.organizzativa.scheda.dati.uvi.presa.carico.livello");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_LIVELLO, elementiDaStampare.size());
		}
		riepiloAccessi = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_ACCESSI_EFFETTUATI);
		if(riepiloAccessi){
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(C_ACCESSI_EFFETTUATI);
			
			String tipoOperatoreAccesso  = "";
			String label = Labels.getLabel("report.elenco.assistiti.accessi.effettuati");
			if (ISASUtil.valida(getTipoOperatore())){
				tipoOperatoreAccesso = ManagerOperatore.decodificaTipoOperatoreGeneric(dbc,getTipoOperatore());
			}else {
				tipoOperatoreAccesso = Labels.getLabel("menu.stampe.riepilogo_assistiti.operatori.tutti");
			}
			
			label +=" "+Labels.getLabel("menu.stampe.riepilogo_assistiti.operatori.per")+" " +tipoOperatoreAccesso;
			labels.add(label);
			campoDb.add("");
			
			nomeCampo.add(C_ACCESSI_EFFETTUATI_SIAD);
			campoDb.add("");
			label +=" "+Labels.getLabel("menu.stampe.riepilogo_assistiti.operatori.siad");
			labels.add(label);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_ACCESSI_EFFETTUATI_SIAD, elementiDaStampare.size());
			
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			
		}

		attive = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_ATTIVE);
		concluse = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_CONCLUSE);
		sospese = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_SOSPESE);
		if (sospese ) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();   
			labels = new Vector<String>();
			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_SOSPESE);   
			campoDb.add("");
			labels.add("report.elenco.assistiti.sospese");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_SOSPESE, elementiDaStampare.size());
		}
		
		proroghe = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_PROROGHE);
		if (proroghe ) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_PROROGHE);
			campoDb.add("");
			labels.add("report.elenco.assistiti.proroghe");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_PROROGHE, elementiDaStampare.size());
		}
		finePiano = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_FINE_PIANO);
		rivalutazione = ISASUtil.getvaloreBoolean(par, CostantiSinssntW.CTS_STP_REPORT_ASS_RIVALUTAZIONE);
		if (rivalutazione) {
			nomeCampo = new Vector<String>();
			campoDb = new Vector<String>();
			labels = new Vector<String>();
			nomeCampo.add(CostantiSinssntW.CTS_STP_REPORT_ASS_RIVALUTAZIONE);
			campoDb.add("NVL(to_char(s.pr_data_revisione ,'DD/MM/YYYY'),'')");
			labels.add("menu.segreteria.organizzativa.scheda.dati.uvi.data.prevista");
			aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
			nomeColonna.put(CostantiSinssntW.CTS_STP_REPORT_ASS_RIVALUTAZIONE, elementiDaStampare.size());
		}
		
	}

	public boolean isFlussiSiadInviato() {
		return flussiSiadInviato;
	}

	public boolean isFlussiSiadNonInviato() {
		return flussiSiadNonInviato;
	}

	private void aggiungiInformazioneColonna(Vector<String> nomeCampo, Vector<String> campoDb, Vector<String> labels) {

		InformazioneColonna informazioneColonna = new InformazioneColonna();
		informazioneColonna.setNomeCampo(nomeCampo);
		informazioneColonna.setCampoSqlWhere(campoDb);

		Vector<String> descrizioniLabel = new Vector<String>();
		Object objLabel = null;
		for (int i = 0; i < labels.size(); i++) {
			objLabel = Labels.getLabel(labels.get(i));
			if (objLabel != null){
				descrizioniLabel.add(objLabel.toString());
			}else {
				descrizioniLabel.add(labels.get(i));
			}
		}
		informazioneColonna.setEtichettaColonna(descrizioniLabel);
		elementiDaStampare.add(informazioneColonna);
	}

	private void campiDefault(Hashtable<String, String> par) {

		Vector<String> nomeCampo = new Vector<String>();
		Vector<String> campoDb = new Vector<String>();
		Vector<String> labels = new Vector<String>();
		nomeCampo.add(C_ZONA);
		campoDb = new Vector<String>();
		labels.add("generic.zona");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_DISTRETTO);
		campoDb = new Vector<String>();
		labels.add("generic.distretto");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_RAGGRUPPAMENTO);
		String raggruppamento = ISASUtil.getValoreStringa(par, "ragg");
		if (ISASUtil.valida(raggruppamento)) {
			if (raggruppamento.equals("C"))
				labels.add("PanelUbicazione.comune");
			else if (raggruppamento.equals("A"))
				labels.add("PanelUbicazione.area_dis");
			else if (raggruppamento.equals("P")) {
				labels.add("PanelUbicazione.presidio");
			}
		} else {
			labels.add("PanelUbicazione.presidio");
		}

		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(DATA_ACCETTAZIONE);
		labels.add("menu.segreteria.organizzativa.scheda.uvm.data.accettazione");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(N_CARTELLA);
		labels.add("fassiGrid.grid.cartella");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(COGNOME);
		labels.add("fassiGrid.grid.cognome");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(NOME);
		labels.add("fassiGrid.grid.nome");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		
		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_DATA_NASCITA);
		labels.add("fassiGrid.grid.nascita.data");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		
		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_SESSO);
		labels.add("fassiGrid.grid.sesso");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		
		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_CODICE_FISCALE);
		labels.add("cartellaForm.codiceFiscale");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(DATA_ATTIVAZIONE);
		labels.add("menu.segreteria.organizzativa.scheda.uvm.data.presa.carico");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(DATA_INIZIO_PIANO);
		labels.add("RichiestaMMG.principale.data_inizio");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(DATA_FINE_PIANO);
		labels.add("RichiestaMMG.principale.data_fine");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		
		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_DATA_CHIUSURA);
		campoDb.add("NVL(to_char(s.pr_data_chiusura ,'DD/MM/YYYY'),'')");
		labels.add("menu.segreteria.organizzativa.scheda.uvm.data.chiusura");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		
		
		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_COD_MEDICO);
		nomeCampo.add(C_COD_MEDICO_DESC);
		campoDb.add("a.cod_med");
		campoDb.add("a.cod_med as cod_med_desc");
		labels.add("menu.segreteria.organizzativa.scheda.uvm.codice.medicommg");
		labels.add("menu.segreteria.organizzativa.scheda.uvm.codice.medicommg.descri");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);
		
		nomeCampo = new Vector<String>();
		campoDb = new Vector<String>();
		labels = new Vector<String>();
		nomeCampo.add(C_FLUSSI_SIAD);
		campoDb.add("s.flag_sent");
		labels.add("menu.segreteria.organizzativa.scheda.uvm.flag.flussi.siad");
		aggiungiInformazioneColonna(nomeCampo, campoDb, labels);

	}

	public Vector<InformazioneColonna> getElementiDaStampare() {
		return elementiDaStampare;
	}

	public boolean isChiusaPeriodo() {
		return chiusaPeriodo;
	}
	

	public boolean isApertePeriodo() {
		return apertePeriodo;
	}

	public boolean isPai() {
		return pai;
	}

	public boolean isDiario() {
		return diario;
	}

	public boolean isIntolleranze() {
		return intolleranze;
	}

	public boolean isTipoUte() {
		return tipoUte;
	}

	public boolean isSegnalazione() {
		return segnalazione;
	}

	public boolean isIntensita() {
		return intensita;
	}

	public boolean isAttive() {
		return attive;
	}

	public boolean isConcluse() {
		return concluse;
	}

	public boolean isSospese() {
		return sospese;
	}

	public boolean isProroghe() {
		return proroghe;
	}

	public boolean isFinePiano() {
		return finePiano;
	}

	public boolean isRivalutazione() {
		return rivalutazione;
	}

	class InformazioneColonna {
		private Vector<String> nomeCampo = new Vector<String>();
		private Vector<String> etichettaColonna = new Vector<String>();
		private Vector<String> campoSqlSelect = new Vector<String>();
		private Vector<String> campoSqlWhere = new Vector<String>();

		public Vector<String> getNomeCampo() {
			return nomeCampo;
		}

		public void setNomeCampo(Vector<String> nomeCampo) {
			this.nomeCampo = nomeCampo;
		}

		public Vector<String> getEtichettaColonna() {
			return etichettaColonna;
		}

		public void setEtichettaColonna(Vector<String> etichettaColonna) {
			this.etichettaColonna = etichettaColonna;
		}

		public Vector<String> getCampoSqlSelect() {
			return campoSqlSelect;
		}

		public void setCampoSqlSelect(Vector<String> campoSqlSelect) {
			this.campoSqlSelect = campoSqlSelect;
		}

		public Vector<String> getCampoSqlWhere() {
			return campoSqlWhere;
		}

		public void setCampoSqlWhere(Vector<String> campoSqlWhere) {
			this.campoSqlWhere = campoSqlWhere;
		}

	}

	public String getSelect(String keyProprieta) {
		String select = "";
//		int posizione = ISASUtil.getValoreIntero(nomeColonna, keyProprieta);
		InformazioneColonna informazioneColonna = getInformazioneColonna(keyProprieta);
		try {
			if (informazioneColonna !=null) {
				Vector<String> elementiColonna = informazioneColonna.getNomeCampo();
				Vector<String> elementiColonneDb = informazioneColonna.getCampoSqlWhere();
				for (int i = 0; i < elementiColonna.size(); i++) {
					select += (ISASUtil.valida(select) ? ", " : " ") + elementiColonneDb.get(i) + " "
							+ elementiColonna.get(i);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return select;
	}

	public String getNomeCampo(InformazioneColonna informazioneColonna) {
		String nomeCampo = "";
		if (informazioneColonna !=null) {
			Vector<String> elementiColonna = informazioneColonna.getNomeCampo();
			for (int i = 0; i < elementiColonna.size(); i++) {
				nomeCampo = elementiColonna.get(i);
			}
		}
		return nomeCampo;
	}
	
	
	public String getLabel(InformazioneColonna informazioneColonna) {
		String label = "";
		if (informazioneColonna !=null) {
			Vector<String> elementiColonna = informazioneColonna.getEtichettaColonna();
			for (int i = 0; i < elementiColonna.size(); i++) {
				label = elementiColonna.get(i);
			}
		}
		return label;
	}
	
	public InformazioneColonna getInformazioneColonna(String keyProprieta) {
		InformazioneColonna informazioneColonna =null;
		
		int posizione = ISASUtil.getValoreIntero(nomeColonna, keyProprieta);
		try {
			if (posizione > 0) {
				informazioneColonna = (InformazioneColonna) elementiDaStampare.get(posizione - 1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return informazioneColonna;
	}

	
	public boolean isLivello() {
		return livello;
	}

	public boolean isRiepiloAccessi() {
		return riepiloAccessi;
	}

	public boolean isSO() {
		return isSO;
	}

	public String getTipoOperatore() {
		return tipoOperatore;
	}

	public boolean isAdp() {
		return adp;
	}

	public boolean isArd() {
		return ard;
	}

	public boolean isAid() {
		return aid;
	}

	public boolean isVsd() {
		return vsd;
	}
}

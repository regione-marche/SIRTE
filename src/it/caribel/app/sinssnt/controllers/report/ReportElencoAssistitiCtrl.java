package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.nuovi.FoReportElencoAssistitiEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SegreteriaOrganizzativaFormCtrl;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.DatiStampaRichiesti;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.pisa.caribel.util.ISASUtil;

import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

public class ReportElencoAssistitiCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;

	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup modStampa;

	protected Radio pdf;
	protected Radio html;

	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelTextbox cod_med;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup res_dom;

	protected Radio presidio;
	protected Radio comune;
	protected Radio area_dis;
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	private CaribelCombobox tipocura;
	private CaribelCheckbox adp;
	private CaribelCheckbox ard;
	private CaribelCheckbox aid;
	private CaribelCheckbox vsd;
	
	private CaribelCombobox cbx_utenza;
	private CaribelCombobox cbx_presa_carico_livello;

	private CaribelCheckbox chiusePeriodo;
	private CaribelCheckbox apertePeriodo;
	private CaribelCheckbox pai;
	private CaribelCheckbox diario;
	private CaribelCheckbox intolleranze;
	private CaribelCheckbox tipo_ute;
	private CaribelCheckbox segnalazione;
	private CaribelCheckbox intensita;
	private CaribelCheckbox attive;
	private CaribelCheckbox concluse;
	private CaribelCheckbox sospese;
	private CaribelCheckbox proroghe;
	private CaribelCheckbox fine_piano;
	private CaribelCheckbox rivalutazione;
	private CaribelCheckbox livello;
	private CaribelCheckbox flussiSIADInviato;
	private CaribelCheckbox flussiSIADNonInviato;
	private CaribelCheckbox accessiEffettuati;

	private int numeroFiltriImpostati = 0;
	private String ver = "10-";
	private int CTS_NUMERO_FILTRI_MINIMO = 3;

	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.setVisibleRaggruppamento(false);
			c.setVisibleUbicazione(false);    
			c.settaRaggr("P");
			doPopulateCombobox();
			

			tipocura.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					onChangeTipoIntensita();
				}});

			apertePeriodo.addEventListener(Events.ON_CHECK, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					controlloPeriodo();
					return;
				}});
			
			chiusePeriodo.addEventListener(Events.ON_CHECK, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					controlloPeriodo();
					return;
				}});
			
			attive.addEventListener(Events.ON_CHECK, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					controllaSchedeAttiveConcluse();
					return;
				}});
			concluse.addEventListener(Events.ON_CHECK, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					controllaSchedeAttiveConcluse();
					return;
				}});
			controllaSchedeAttiveConcluse();
			controlloPeriodo();
			
		} catch (Exception e) {
			doShowException(e);
		}
	}

	protected void controllaSchedeAttiveConcluse() {
		String punto = ver + "controllaSchedeAttiveConcluse ";
		logger.debug(punto + " allineo il periodo ");
		
		concluse.setDisabled(false);
		attive.setDisabled(false);
		if (attive.isChecked()){
			concluse.setDisabled(true);
			concluse.setChecked(false);
		}else if (concluse.isChecked()){
			attive.setChecked(false);
			attive.setDisabled(true);
		}
		
	}

	protected void controlloPeriodo() {
		String punto = ver + "controlloPeriodo ";
		logger.debug(punto + " allineo il periodo ");
		dadata.setRequired(false);
		adata.setRequired(false);
		if (apertePeriodo.isChecked() || chiusePeriodo.isChecked()){
			dadata.setRequired(true);
			adata.setRequired(true);
		}
		concluse.setDisabled(false);
		attive.setDisabled(false);
		if(apertePeriodo.isChecked() || chiusePeriodo.isChecked()){
			concluse.setDisabled(true);
			concluse.setChecked(false);
			attive.setDisabled(true);
			attive.setChecked(false);
		}
	}

	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + "");
		String linea = CaribelComboRepository.stampaCompbo(tipocura);
		logger.debug(punto + " combo caricata >>" + linea + "<<");
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(tipocura);
		SegreteriaOrganizzativaFormCtrl segreteriaOrganizzativaFormCtrl = new SegreteriaOrganizzativaFormCtrl();
		segreteriaOrganizzativaFormCtrl.caricaTipoUtenza(cbx_utenza);
		caricaLivello(cbx_presa_carico_livello);
		CaribelComboRepository.addComboValueEmpty(tipocura);
		CaribelComboRepository.addComboValueEmpty(cbx_utenza);
		CaribelComboRepository.addComboValueEmpty(cbx_presa_carico_livello);
	}

	public void caricaLivello(CaribelCombobox tipocura) throws Exception {
		Hashtable<String, String> tipocura_hash = new Hashtable<String, String>();
		tipocura_hash.put("tab_cod", CostantiSinssntW.TAB_VAL_LIVELLO_PRESA_CARICO);
		CaribelComboRepository.comboPreLoad("liv_pres_cari", new TabVociEJB(), "query", tipocura_hash, tipocura, null,
				"tab_val", "tab_descrizione", false);

	}

	public void doStampa() {
		String punto = ver + "doStampa ";
		String messaggio = "";
		if ((ManagerDate.validaData(dadata)) && (ManagerDate.validaData(adata))) {
			int numeroGiorni = ManagerDate.getNumeroGiorniData(dadata, adata);
			logger.trace(punto + " data inserita numeroGiorni>" + numeroGiorni + "<<");
			if (numeroGiorni > 365) {
				logger.trace(punto + " data inserita ");
				messaggio = Labels.getLabel("report.elenco.assistiti.superioreAnno", new String[] {});
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
						Messagebox.EXCLAMATION);
			}
		}

		if (ISASUtil.valida(messaggio)){
			return;
		}
		
		try {
			String ejb = "SINS_FOREPELEASS";
			String metodo = "query_stampa";
			String report = "rep_elenco_assistiti_so_";
			String servlet = ""; 
			report += "an";

			Hashtable<String, Object> parametri = impostaFiltro();

			if (numeroFiltriImpostati < CTS_NUMERO_FILTRI_MINIMO ) {
				numeroFiltriImpostati = 0;
				logger.trace(punto + " numero minimo di filtri da impostare ");
				messaggio = Labels.getLabel("report.elenco.assistiti.numero.minimo.filtri", new String[] {});
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
						Messagebox.EXCLAMATION);
			}
			if (ISASUtil.valida(messaggio)){
				return;
			}
			
			DatiStampaRichiesti datiStampaRichiesti = new DatiStampaRichiesti(metodo, report, ejb, parametri);

			logger.trace(punto + " dati >>" + parametri + "<<");
			String parametriUrl = datiStampaRichiesti.recuperaParametriStampa();
			String type = "";
			String file = "";  
			if (pdf.isChecked()) {
				file = report + ".fo";
				type = "&TYPE=PDF";
			} else if (html.isSelected()) {
				file = report + ".html";
				type = "&TYPE=application/vnd.ms-excel";
			}
//			else if (rtf.isSelected()) {
//				logger.info(punto + " da definire ");
//				file = report ;
//				type = "&TYPE=application/vnd.ms-excel";
//			}

			servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=" + ejb + "&USER="
					+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
					+ CaribelSessionManager.getInstance().getMyLogin().getPassword() + "&METHOD=" + metodo + "&REPORT="
					+ file + type + parametriUrl;

			logger.trace(punto + " url recuperata>>\n" + servlet);

//			Executions.getCurrent().createComponents(DatiStampaCtrl.CTS_FILE_ZUL, self, dati);
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);

		} catch (Exception e) {
			doShowException(e);
		}
	}

	private Hashtable<String, Object> impostaFiltro() throws Exception {
		String punto = ver + "impostaFiltro ";
		logger.info(punto + " inizio per il recupero dei filtri ");
		Hashtable<String, Object> parametri = new Hashtable<String, Object>();
		Hashtable<String, String> datiStampa = new Hashtable<String, String>();

		String ragg = raggruppamento.getSelectedItem().getValue();
		String zone = zona.getSelectedItem().getValue();
		String distr = "";
		String codMedico = "";
		String pca = "";
		if (zone.equals("TUTTO"))
			zone = "";
		if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals("")
				|| distretto.getValue().equals("NODIV") || distretto.getValue().equals("NESDIV")) {
			distr = "";
		} else {
			distr = distretto.getSelectedItem().getValue();
		}

		if (presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals("")
				|| presidio_comune_area.getValue().equals("NODIV") || presidio_comune_area.getValue().equals("NESDIV")) {
			pca = "";
		} else {
			pca = presidio_comune_area.getSelectedItem().getValue();
		}

		if (cod_med!=null && cod_med.getValue()!=null){
			numeroFiltriImpostati +=2;
			codMedico = cod_med.getValue();
		}
		
		String terr = "";

		if (zone.equals("NESDIV")) {
			terr = "0|";
			zone = "";
		} else
			terr = "1|";
		if (distretto.equals("NESDIV") || distr.equals("")) {
			terr = terr + "0|";
			distr = "";
		} else
			terr = terr + "1|";
		if (pca.equals("NESDIV") || pca.equals("")) {
			terr = terr + "0|";
			pca = "";
		} else
			terr = terr + "1";

		String socsan = soc_san.getSelectedItem().getValue();
		String tipo_ubi = res_dom.getSelectedItem().getValue();

		parametri.put(CostantiSinssntW.CTS_STP_REPORT_RAGGRUPPAMENTO, ragg);
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ZONE, zone);
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_DISTRETTO, distr);
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_PCA, pca);
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_SOCSAN, socsan);
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_TIPO_UBI, tipo_ubi);
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_TERR, terr);
	    parametri.put(CostantiSinssntW.CTS_STP_REPORT_COD_MEDICO, codMedico);
		
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO, dadata.getValueForIsas());
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_DATA_FINE, adata.getValueForIsas());

		if (ISASUtil.valida(pca)){
			numeroFiltriImpostati++;
		}
		
		if (ManagerDate.validaData(dadata)){
			numeroFiltriImpostati++;
		}
		
		if (ManagerDate.validaData(adata)){
			numeroFiltriImpostati++;
		}
		
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_APERTE, new Boolean(apertePeriodo.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_CHIUSE, new Boolean(chiusePeriodo.isChecked()));
		
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_PAI, new Boolean(pai.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_DIARIO, new Boolean(diario.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_INTOLLERANZE, new Boolean(intolleranze.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_TIPO_UTE, new Boolean(tipo_ute.isChecked()));
		
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_INTENSITA, new Boolean(intensita.isChecked()));
		if (intensita.isChecked()&& (tipocura!=null && ISASUtil.valida(tipocura.getValue()) )){
			numeroFiltriImpostati++;
		}
		if (tipo_ute.isChecked()&& (cbx_utenza!=null && ISASUtil.valida(cbx_utenza.getValue()))){
			numeroFiltriImpostati++;
		}
		if (livello.isChecked() && (cbx_presa_carico_livello !=null && ISASUtil.valida(cbx_presa_carico_livello.getValue()))){
			numeroFiltriImpostati++;
		}
		if (fine_piano.isChecked()){
			numeroFiltriImpostati++;
		}
		if (rivalutazione.isChecked()){
			numeroFiltriImpostati++;
		}
		if (attive.isChecked()){
			numeroFiltriImpostati++;
		}
		if (concluse.isChecked()){
			numeroFiltriImpostati++;
		}
		if (flussiSIADInviato.isChecked()){
			numeroFiltriImpostati++;
		}
		if (flussiSIADNonInviato.isChecked()){
			numeroFiltriImpostati++;
		}
		
		if (adp.isChecked() || aid.isChecked() || ard.isChecked() || vsd.isChecked()) {
			numeroFiltriImpostati++;
		}
		
		parametri.put(FoReportElencoAssistitiEJB.CTS_CP_ADP, new Boolean(adp.isChecked()));
		parametri.put(FoReportElencoAssistitiEJB.CTS_CP_AID, new Boolean(aid.isChecked()));
		parametri.put(FoReportElencoAssistitiEJB.CTS_CP_ARD, new Boolean(ard.isChecked()));
		parametri.put(FoReportElencoAssistitiEJB.CTS_CP_VSD, new Boolean(vsd.isChecked()));
		
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_LIVELLO, new Boolean(livello.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_ACCESSI_EFFETTUATI, new Boolean(accessiEffettuati.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_SEGNALAZIONE, new Boolean(segnalazione.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_ATTIVE, new Boolean(attive.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_CONCLUSE, new Boolean(concluse.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_SOSPESE, new Boolean(sospese.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_PROROGHE, new Boolean(proroghe.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_FINE_PIANO, new Boolean(fine_piano.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_RIVALUTAZIONE, new Boolean(rivalutazione.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_COD_TIPOCURA, tipocura.getSelectedValue());
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_COD_TIPO_UTE, cbx_utenza.getSelectedValue());
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_ASS_COD_LIVELLO, cbx_presa_carico_livello.getSelectedValue());
		parametri.put(CostantiSinssntW.TIPO_OPERATORE, UtilForContainer.getTipoOperatorerContainer());
		parametri.put(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA, UtilForContainer.isSegregeriaOrganizzativa());
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_FLAG_SIAD_CHECK_INVIATI, new Boolean(flussiSIADInviato.isChecked()));
		parametri.put(CostantiSinssntW.CTS_STP_REPORT_FLAG_SIAD_CHECK_NO_INVIATI, new Boolean(flussiSIADNonInviato.isChecked()));
		
		String codice_operatore = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
		String zona_operatore = CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
		String distr_operatore = CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");
		parametri.put("codice_operatore", codice_operatore);
		parametri.put("zona_operatore", zona_operatore);
		parametri.put("distr_operatore", distr_operatore);

		parametri.putAll(datiStampa);

		return parametri;
	}
	
	
	public void onChangeTipoIntensita() {
		String punto = ver + "onChangeTipoIntensita() ";
		
		String tipoCura =tipocura.getSelectedValue();
		boolean disattivare =true;
		if(ISASUtil.valida(tipoCura)&& tipoCura.equals(Costanti.CTS_COD_CURE_PRESTAZIONALI)){
			disattivare = false;
			
		}

		logger.debug(punto + " disattivare>>"+disattivare+"< ");
		aid.setDisabled(disattivare);
		ard.setDisabled(disattivare);
		adp.setDisabled(disattivare);
		vsd.setDisabled(disattivare);
		
		aid.setSelectedValue(false);
		ard.setSelectedValue(false);
		adp.setSelectedValue(false);
		vsd.setSelectedValue(false);
	}
	

	public void onChangeIntensita() {
		String punto = ver + "onChangeIntensita() ";
		logger.info(punto + " inizio");
		tipocura.setDisabled(!intensita.isChecked());
		if (!intensita.isChecked()) {
			tipocura.setSelectedIndex(-1);
		}
	}

	public void onChangeFlussoInviato(){
		String punto = ver + "onChangeFlussoInviato() ";
		logger.info(punto + " inizio");
		if (flussiSIADInviato.isChecked()) {
			flussiSIADNonInviato.setChecked(false);
		}
	}
	
	public void onChangeFlussoNonInviato(){
		String punto = ver + "onChangeFlussoNonInviato() ";
		logger.info(punto + " inizio");
		if (flussiSIADNonInviato.isChecked()) {
			flussiSIADInviato.setChecked(false);
		}
	}
	
	
	public void onChangeTipoUtenza() {
		String punto = ver + "onChangeTipoUtenza() ";
		logger.info(punto + " inizio");

		cbx_utenza.setDisabled(!tipo_ute.isChecked());
		if (!tipo_ute.isChecked()) {
			cbx_utenza.setSelectedIndex(-1);
		}
	}

	public void onChangeLivello() {
		String punto = ver + "onChangeLivello() ";
		logger.info(punto + " inizio");

		cbx_presa_carico_livello.setDisabled(!livello.isChecked());
		if (!livello.isChecked()) {
			cbx_presa_carico_livello.setSelectedIndex(-1);
		}
	}
	
//	public void onChangeFlussiSiad() {
//		String punto = ver + "onChangeFlussiSiad() ";
//		logger.info(punto + " inizio");
//
//		cbx_flussi_siad.setDisabled(!flussiSIAD.isChecked());
//		if (!flussiSIAD.isChecked()) {
//			cbx_flussi_siad.setSelectedIndex(-1);
//		}
//	}	

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
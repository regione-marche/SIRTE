package it.caribel.app.sinssnt.controllers.accessi_effettuati;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.CartellaBaseEJB;
import it.caribel.app.sinssnt.bean.IntInfEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOSKSoProrogheEJB;
import it.caribel.app.sinssnt.controllers.contattoMedico.ContattoMedicoFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.util.ISASUtil;

import java.util.Date;
import java.util.HashMap;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.Window.Mode;

public class AccessiEffettuatiGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	public static String CTS_PROVENIENZA_OPERAZIONE = "cts_prov";
	public static String CTS_PROVENIENZA_FROM_OPERAZIONE = "m_opr";
	private String myKeyPermission = ChiaviISASSinssntWeb.RIEPILO_ACCESSI;
	private IntInfEJB myEJB = new IntInfEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/accessi_effettuati/accessiEffettuatiGrid.zul";
	private CaribelTextbox key_cartella;
	private CaribelTextbox n_cartella;
	private CaribelTextbox key_contatto;
	private CaribelTextbox key_tipo;
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	private CaribelCombobox figure_professionali;
	private String ver = "15-";
	private Label presidio_comune_areadis;
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup res_dom;
	private CaribelTextbox cod_operatore;
	private AbstractComponent caribelSearchOperatore;
	private AbstractComponent assistito;
	private Label lbx_assistito;
	private Hlayout hOperatoreSearch;
	private Hlayout hUbicazione;
	private Hlayout hAssistiti;
	boolean formMenuOperazione = false;
	private int CTS_NUMERO_PARAMETRI_MINIMO = 3;
	public static final String CTS_TITOLO_FORM = "tit_form";

	
	public void doAfterCompose(Component comp) throws Exception {
		String punto = ver + "doAfterCompose ";
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		String provendienza = ISASUtil.getValoreStringa(arg, CTS_PROVENIENZA_OPERAZIONE);
		hUbicazione.setVisible(false);
		hOperatoreSearch.setVisible(false);
		hAssistiti.setVisible(false);
		
		if (ISASUtil.valida(provendienza) && provendienza.equalsIgnoreCase(CTS_PROVENIENZA_FROM_OPERAZIONE)){
			impostaFormDaOperazione();

			doPopolaCombo();
		}else if(super.caribelSearchCtrl!=null){
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get("textToSearch");
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				key_cartella.setText(textToSearch);
				super.hParameters.put(key_cartella.getDb_name(), textToSearch);
				//tb_filter2.setText(textToSearch);
				//super.hParameters.put(tb_filter2.getDb_name(), textToSearch);
				doRefresh();
			}
		}else{
			String nCartella = ISASUtil.getValoreStringa((HashMap<String, String>)arg,CostantiSinssntW.N_CARTELLA);
			String nContatto = ISASUtil.getValoreStringa((HashMap<String, String>)arg,CostantiSinssntW.N_CONTATTO);
			String idSkso = ISASUtil.getValoreStringa((HashMap<String, String>)arg,CostantiSinssntW.CTS_ID_SKSO);
			String tipo = UtilForContainer.getTipoOperatorerContainer();
//			String tipo = ISASUtil.getValoreStringa((HashMap<String, String>)arg,ContattoMedicoFormCtrl.CTS_TIPO_OPERATORE);
			boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();
			
			if(!ISASUtil.valida(nCartella)){
				nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"";
			}
			String dataInizio = "";
			String dataFine = "";
			if (isSegreteriaOrganizzativa){
				logger.trace(punto + " recupero date SO ");
				dataInizio = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO)+"";
				idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO)+"";
				
				RMSkSOSKSoProrogheEJB rmSkSOSKSoProrogheEJB = new RMSkSOSKSoProrogheEJB();
				String dtMaxProroga = rmSkSOSKSoProrogheEJB.getMaxDataProrogaML(CaribelSessionManager.getInstance().getMyLogin(), nCartella, idSkso);
				if (ManagerDate.validaData(dtMaxProroga)){
					dataFine = dtMaxProroga;
				}else {
					dataFine = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE)+"";
				}
			}else {
				logger.trace(punto + " recupero data da contatti ");
				Object obj = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO);
				dataInizio = (obj!=null? obj+"":"");
				obj = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE);
				dataFine = (obj !=null ? obj+"":"");
			}
			
//			if  (ISASUtil.valida(nCartella)&& ISASUtil.valida(nContatto)&& ISASUtil.valida(tipo)){
//				logger.trace(punto + " dati presenti ");
//			}else {
//				logger.trace(punto + " recupero dati da container ");
			nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"";
				
				logger.trace(punto + " apertura modale ");
				((Window)self).setSizable(false);
				((Window)self).setClosable(false);
				((Window)self).setSclass("");
				((Window)self).setMode(Mode.EMBEDDED);
				((Window)self).setPosition("");
				 String titolo = recuperaTitoloForm();
				((Window)self).setTitle(titolo);
				((Window)self).setVflex("true");
//			}

			if (ISASUtil.valida(provendienza) && provendienza.equalsIgnoreCase(CTS_PROVENIENZA_FROM_OPERAZIONE)){
				logger.trace(punto + " non imposto il campo data ");
			}else {
				impostaCampoData(dataInizio, dataFine);			
			}
				
		    doPopolaCombo();
			key_cartella.setText(nCartella);
			key_contatto.setText(nContatto);
			key_tipo.setText(tipo);
			
			if (isSegreteriaOrganizzativa){
				logger.trace(punto + "NON IMPOSTO IL TIPO >>" +tipo+"<");
				figure_professionali.setDisabled(false);
			}else {
				logger.trace(punto + "imposto il tipo>>" +tipo+"<");
				figure_professionali.setSelectedValue(tipo);
				figure_professionali.setDisabled(true);
			}
			
			super.hParameters.put(key_cartella.getDb_name(), nCartella);
			super.hParameters.put(key_contatto.getDb_name(),nContatto);
			super.hParameters.put(key_tipo.getDb_name(), tipo);
			super.hParameters.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO, dataInizio);
			super.hParameters.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE, dataFine);
			
			super.hParameters.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, dataInizio);
			super.hParameters.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, dataFine);
			
			super.hParameters.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
			super.hParameters.put(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA, new Boolean(isSegreteriaOrganizzativa));
			
			doRefreshNoAlert();
		}
    }
	
	private void impostaFormDaOperazione() {
		String punto = ver + "impostaFormDaOperazione ";
		logger.trace(punto + " attivo ubicazione e operatore ");
		hUbicazione.setVisible(true);
		hOperatoreSearch.setVisible(true);
		hAssistiti.setVisible(true);
		
		doCaricaComboDistretti();
		CaribelSearchCtrl operatoreSearch = (CaribelSearchCtrl) caribelSearchOperatore.getAttribute(MY_CTRL_KEY);
		operatoreSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, figure_professionali);
		operatoreSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zona);
		operatoreSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto);
		operatoreSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_PRESIDIO, presidio_comune_area);
		formMenuOperazione = true;
		
		CaribelSearchCtrl assistitoSearch = (CaribelSearchCtrl) assistito.getAttribute(MY_CTRL_KEY);
		
		impostaFiltroAssistito(assistitoSearch);
		
		
		dettaglioForm();
	}

	private void impostaFiltroAssistito(CaribelSearchCtrl assistito) {
		String punto = ver + "impostaFiltroAssistito ";
		boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();

		String labelAssistito = Labels.getLabel("accessi.effettuati.ricerca.assistito.su.contatti");
		lbx_assistito.setValue(labelAssistito);
		
		assistito.putLinkedSearchObjects(CartellaBaseEJB.CTS_COD_ZONA, zona);
		if (isSegreteriaOrganizzativa){
			assistito.putLinkedSearchObjects(CartellaBaseEJB.CTS_TIPO_OPERATORE_CONTATTO, CartellaBaseEJB.CTS_TIPO_SEGRETERIA_ORGANIZZATIVA);
		}else {
			String tipoOperatore = "";
			try {
				tipoOperatore = UtilForContainer.getTipoOperatorerContainer();
			} catch (Exception e) {
				logger.error(punto + " Errore nel recuperare il tipo di operatore collegato", e);
			}
			if (ISASUtil.valida(tipoOperatore)){
				assistito.putLinkedSearchObjects(CartellaBaseEJB.CTS_TIPO_OPERATORE_CONTATTO, tipoOperatore);
			}
		}
	}

	private void dettaglioForm() {
		String punto = ver + "dettaglioform ";
		logger.trace(punto + " apertura modale ");
		((Window)self).setClosable(true);
		((Window)self).setMode(Mode.EMBEDDED);
		((Window)self).setHflex("550px");
		((Window)self).setSizable(true);
		String titolo = ISASUtil.getValoreStringa(arg, CTS_TITOLO_FORM ); 
		((Window)self).setTitle(titolo);
	}

	private void doCaricaComboDistretti() {
		String punto = ver + "doCaricaComboDistretti ";
		logger.debug(punto + " Inizio>>" );
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.setDistrettiVoceTutti(false);
			c.doInitPanel();
			String codZona =getProfile().getStringFromProfile("zona_operatore"); 
			if (ISASUtil.valida(codZona)){
				zona.setValue(codZona);
			}
			c.setDistrettoFirst();
			distretto.setRequired(true);
			Events.sendEvent(Events.ON_SELECT, distretto, null);
			c.setVisibleZona(false);
			c.settaRaggruppamento("P");
			c.setVisibleRaggruppamento(false);
			c.setVisibleUbicazione(false);
			c.setDbNameZona(CostantiSinssntW.CTS_OP_COINVOLTI_DB_NAME_ZONA);    
			c.setDbNameDistretto(CostantiSinssntW.CTS_OP_COINVOLTI_DB_NAME_DISTRETTO);
			c.setDbNamePresidioComuneArea(CostantiSinssntW.CTS_DB_NAME_PRESIDI);
			presidio_comune_areadis.setValue(Labels.getLabel("PanelUbicazione.presidio.sede")+":");
			c.setPresidioComuneAreaDisabilita(false);
		}catch(Exception e){
			doShowException(e);
		}
	}



	public void onChange$figure_professionali(Event e){
	String punto = ver + "";
	logger.trace(punto + " ");
		doCerca();
	}
	
	private void impostaCampoData(String dtInizio, String dtFine) {
		String punto = ver  + "impostaCampoData ";
		if (ManagerDate.validaData(dtInizio)){
				Date dtPeriodoInizio = ManagerDate.getDate(dtInizio);
				dadata.setConstraint("after"+ UtilForComponents.formatDateforDatebox(dtPeriodoInizio));
				dadata.setValue(dtPeriodoInizio);
		}
		logger.trace(punto + "dtInizio>>"+dtInizio+"<");
		if (ManagerDate.validaData(dtFine)){
				Date dtPeriodoFine = ManagerDate.getDate(dtFine);
				adata.setConstraint("before"+ UtilForComponents.formatDateforDatebox(dtPeriodoFine));
				adata.setValue(dtPeriodoFine);
		}
		logger.trace(punto + "dtFine>>"+dtFine+"<");
	}

	private void doPopolaCombo() throws Exception {
		String punto = ver + "doPopolaCombo ";
		logger.trace(punto + " tipi operatore ");
		ManagerOperatore.loadTipiOperatori(figure_professionali, true);
	}

	public String recuperaTitoloForm() {
		String titolo = Labels.getLabel("accessi.effettuati.formTitle.dal");
		String prDataPuac = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_PR_DATA_PUAC)+"";
		titolo +=" "+ManagerDate.formattaDataIta(prDataPuac, "/");
		String prDataChiusura = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.PR_DATA_CHIUSURA)+"";
		if (ManagerDate.validaData(prDataChiusura)){
			titolo+=" " + Labels.getLabel("accessi.effettuati.formTitle.al");
			titolo += " " + ManagerDate.formattaDataIta(prDataChiusura, "/");
		}
		return titolo;
	}
	
	public void doStampa() {	
		String punto = ver  + "doStampa ";
		logger.trace(punto + ver  + " inizio dati ");
		HashMap<String, Object> map = new HashMap<String, Object>();

		if (formMenuOperazione && !parametriSufficienti()){
			logger.trace(punto + " Numero parametri non sufficienti per eseguire la stampa ");
			String messaggio = Labels.getLabel("accessi.effettuati.numero.parametri.insufficienti");
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
					Messagebox.EXCLAMATION);
			return ;
		}
		map = recuperaDatiPerRicerca();

		Executions.getCurrent().createComponents(ReportEAccessiAssistiti.myPathFormZul, self, map);
	}

	public HashMap<String, Object> recuperaDatiPerRicerca() {
		HashMap<String, Object> map = new HashMap<String, Object>();

		if (formMenuOperazione) {
			key_cartella.setValue(n_cartella.getValue());
			map.put(CostantiSinssntW.CTS_STP_REPORT_ZONE, zona.getValue());
			map.put(CostantiSinssntW.CTS_STP_REPORT_DISTRETTO, distretto.getSelectedValue());
			map.put(CostantiSinssntW.CTS_STP_REPORT_PCA, presidio_comune_area.getSelectedValue());
			map.put(CostantiSinssntW.COD_OPERATORE, cod_operatore.getValue());

		}

		map.put(CostantiSinssntW.N_CARTELLA, key_cartella.getValue());
		map.put(CostantiSinssntW.N_CONTATTO, key_contatto.getValue());

		map.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, dadata.getValueForIsas());
		map.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, adata.getValueForIsas());
		map.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO,
				ISASUtil.getValoreStringa(super.hParameters, CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO));
		map.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE,
				ISASUtil.getValoreStringa(super.hParameters, CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE));
		map.put(ContattoMedicoFormCtrl.CTS_TIPO_OPERATORE, figure_professionali.getSelectedValue());
		map.put(CostantiSinssntW.CTS_ID_SKSO, ISASUtil.getValoreStringa(super.hParameters, CostantiSinssntW.CTS_ID_SKSO));

		return map;
	}
	
	private boolean parametriSufficienti() {
		String punto = ver + "parametriSufficienti ";
		int numeroParametri = 0; 
		boolean parametriSufficienti = false;
		
		if (ManagerDate.validaData(dadata)){
			numeroParametri++;
		}
		if (ManagerDate.validaData(adata)){
			numeroParametri++;
		}
		if (ISASUtil.valida(n_cartella.getValue())){
			numeroParametri++;
		}
		
		if (ISASUtil.valida(presidio_comune_area.getValue())){
			numeroParametri++;
		}

		if (ISASUtil.valida(cod_operatore.getValue())){
			numeroParametri++;
		}
		
		if (ISASUtil.valida(figure_professionali.getValue())){
			numeroParametri++;
		}
		
		parametriSufficienti =numeroParametri >= CTS_NUMERO_PARAMETRI_MINIMO ;
		logger.trace(punto + " parametriSufficienti>" +parametriSufficienti);
		
		return parametriSufficienti;
	}




	public void doCerca(){
		String punto = ver + "doCerca ";
		try{
			UtilForComponents.testRequiredFields(self);
			
			if (formMenuOperazione && !parametriSufficienti()){
				logger.trace(punto + " Numero parametri non sufficienti per eseguire la stampa ");
				String messaggio = Labels.getLabel("accessi.effettuati.numero.parametri.insufficienti");
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
						Messagebox.EXCLAMATION);
				return ;
			}
			super.hParameters.putAll(recuperaDatiPerRicerca());
			
			super.hParameters.put(key_cartella.getDb_name(), key_cartella.getValue().toUpperCase());
			super.hParameters.put(key_contatto.getDb_name(), key_contatto.getValue().toUpperCase());
			
			super.hParameters.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, dadata.getValueForIsas());
			super.hParameters.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, adata.getValueForIsas());
			super.hParameters.put(CostantiSinssntW.CTS_TIPO_OPERATORE, figure_professionali.getSelectedValue());
			
			
			doRefresh();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doPulisciRicerca() {
		try {
			setDefault();
		} catch (Exception e) {
			logger.error(this.getClass().getName()+": Impossibile inizializzare il reparto, rivolgersi all'assistenza");
		}
	}
	
private void setDefault() throws Exception{		
		
		if(caribellb.getItemCount()>0){
			caribellb.getItems().clear(); //.jCariTable1.deleteAll();
		}
		
		key_cartella.setValue("");
		key_contatto.setValue("");
		key_tipo.setValue("");
	}
public void doNuovo() {		
	Messagebox.show(
			Labels.getLabel("exception.NotYetImplementedException.msg"),
			Labels.getLabel("messagebox.attention"),
			Messagebox.OK,
			Messagebox.INFORMATION);
	
}
public void doApri(){		
	Messagebox.show(
			Labels.getLabel("exception.NotYetImplementedException.msg"),
			Labels.getLabel("messagebox.attention"),
			Messagebox.OK,
			Messagebox.INFORMATION);
}
}

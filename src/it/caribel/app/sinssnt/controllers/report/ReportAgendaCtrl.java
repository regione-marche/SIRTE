package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.OperatoriEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.login.ManagerProfileBase;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.util.UtilitiDate;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.operatori.GestTpOp;

import java.text.ParseException;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Radio;

public class ReportAgendaCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;

	private final int WAIT = 0;

	private final int STAMPA = 4;
	
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	protected CaribelCheckbox planningSettimanale;

	protected CaribelRadiogroup modalitaStampa;
	
	protected Component operatoreCS;
	protected CaribelTextbox codope;
	protected CaribelTextbox codope_es;
	protected CaribelCombobox tipoOperatore;
	
	protected CaribelTextbox cod_ass;
	
	protected CaribelTextbox cod_pre;
	
	protected CaribelRadiogroup tipo;
	
	protected CaribelCheckbox mmg;
	
	protected CaribelRadiogroup ordin;
	
	protected CaribelCheckbox sintetica;
	
	protected Radio an;
	protected Radio sin;
	
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;

	protected CaribelTextbox JCariTextFieldTipo;

	protected Component panel_ubicazione;
	protected CaribelSearchCtrl operatoreCSC;
	
	protected String keyPermission = ChiaviISASSinssntWeb.ST_AGVIS; //"ST_AGVIS";
	protected String modificaOperatore ="AGEMODOP";

	private boolean gl_boolCodOperBloccato = false;
	private PanelUbicazioneCtrl c;

	private int stato;

	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			initCombo();
			operatoreCSC = (CaribelSearchCtrl) operatoreCS.getAttribute(MY_CTRL_KEY);
			operatoreCSC.putLinkedSearchObjects("tipo", tipoOperatore);
//			operatoreCSC.putLinkedSearchObjects(JCariTextFieldTipo.getDb_name(), JCariTextFieldTipo);
			setStato(0);
			c.settaRaggruppamentoNoUbic("P");
			c.setVisibleRaggruppamento(false);
			
			checkDatiDaArg();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	private void checkDatiDaArg() throws WrongValueException, ParseException {
		if(arg.containsKey("giorni")){
			Vector v = (Vector) arg.get("giorni");
			dadata.setValue(UtilitiDate.getGCalData((String) v.get(0)).getTime());
			adata.setValue(UtilitiDate.getGCalData((String) v.get(6)).getTime());
		}	
		if(arg.containsKey("referente")){
			modalitaStampa.setSelectedIndex(1);
			Events.sendEvent(Events.ON_CHECK, modalitaStampa, modalitaStampa.getSelectedValue());
			codope.setValue((String) arg.get("referente"));
			Events.sendEvent(Events.ON_CHANGE, codope, codope.getValue());			
		}	
		if(arg.containsKey("tipo_operatore")){
			tipoOperatore.setSelectedValue((String) arg.get("tipo_operatore"));
		}	
		if(arg.containsKey("tipoPrestazioni")){
			tipo.setSelectedValue((String) arg.get("tipoPrestazioni"));
		}
		if(arg.containsKey("pres")){
			distretto.setSelectedValue(ManagerProfile.getDistrettoOperatore(getProfile()));
			Events.sendEvent(Events.ON_SELECT, distretto, ManagerProfile.getDistrettoOperatore(getProfile()));
			presidio_comune_area.setSelectedValue((String) arg.get("pres"));
		}
	}

	private void setStato(int state) {
		if (state == this.WAIT) {
			if (!getProfile().getIsasUser().canIUse(ChiaviISASSinssntWeb.AGEMODOP, ChiaviISASSinssntWeb.CONS)) {
				codope.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
				Events.sendEvent(Events.ON_CHANGE, codope, codope.getValue());
//				this.ExecSelectOperatore();
				gl_boolCodOperBloccato = true;
			}

			if (!getProfile().getIsasUser().canIUse(keyPermission, ChiaviISASSinssntWeb.FIGPROF)) {
				tipoOperatore.setDisabled(true);
				tipoOperatore.setSelectedValue(getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE));
			} else {
				tipoOperatore.setDisabled(false);
				tipoOperatore.setSelectedValue(getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE));
			}

			// gb 21/09/07 *******
			if (gl_boolCodOperBloccato) {
//				abilitaPresidio(false);
				zona.setDisabled(true);
				zona.setReadonly(true);
				abilitaOperatore(false);
				modalitaStampa.setSelectedValue("OP");
				modalitaStampa.setDisabled(true);
			}else {
				String scelta = modalitaStampa.getSelectedValue();
				if (scelta.equals("PR")) {
					abilitaPresidio(true);
					abilitaOperatore(false);
				} else {
					abilitaPresidio(false);
					abilitaOperatore(true);
				}
			}
			dadata.setFocus(true);
		}
		
		stato = state;
	}
	
	public void onCheck$modalitaStampa() throws Exception {
		String scelta = modalitaStampa.getSelectedValue();
		if (scelta.equals("PR")) {
			abilitaPresidio(true);
			abilitaOperatore(false);
		} else {
			abilitaPresidio(false);
			abilitaOperatore(true);
		}
	}
	
	void abilitaOperatore(boolean abil) {
		operatoreCSC.setReadonly(!abil);
	}

	void abilitaPresidio(boolean abil) {
		// gb 21/09/07 *******
		UtilForBinding.setComponentReadOnly(panel_ubicazione, !abil);
		zona.setSelectedIndex(0);
		Events.sendEvent(Events.ON_SELECT, zona, null);
	}
	
	public void doStampa() {
		try {

			String punto = getClass().getName() + "doStampa ";
			logger.info(punto + " Procedo con la stampa ");
			if (planningSettimanale.isChecked()) {
				logger.info(punto + " selezionato la stampa con planning ");
				//				String dataInizio = dadata.getValueForIsas();
				if (dadata.getValue()==null) {
					UtilForUI.standardExclamation(Labels.getLabel("agenda.stampa.msg.erroreNodatainizio"));
					//					new cariInfoDialog(null, "Manca la data di definizione di inizio della settimana", "Attenzione!").show();
					return;
				}
				stato = this.STAMPA; // gb 21/09/07
			} else {
				logger.info(punto + " Stampo la stampa con altre tipologie di stampa");
				// controllo la regolarità delle date
				if (dadata.getValue()==null || adata.getValue()==null || dadata.getValue().after(adata.getValue())){
					UtilForUI.standardExclamation(Labels.getLabel("agenda.stampa.msg.dateIncoerenti"));
					return;
				}else{
					stato = this.STAMPA; // gb 21/09/07
				}
			}

			if (stato == this.STAMPA) {
				// recupero parametri
				String user = CaribelSessionManager.getInstance().getMyLogin().getUser();
				String passwd = CaribelSessionManager.getInstance().getMyLogin().getPassword();
				String data_inizio = dadata.getValueForIsas();
				String data_fine = adata.getValueForIsas();

				String servlet = "";
				String figprof = "";
				String codice_operatore = codope.getValue();
				if (codice_operatore.equals(""))
					figprof = tipoOperatore.getSelectedValue();

				//				String zona = jPanelUbicazione1.getComboZone();
				//				String distretto = jPanelUbicazione1.getComboDistretti();
				//				String codice_presidio = jPanelUbicazione1.getComboTerzoLivello();

				String zone = zona.getSelectedItem().getValue();
				String distrettoV="";
				String codice_presidio="";
				if (zone.equals("TUTTO"))
					zone="";
				if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals(""))
					distrettoV="";
				else 
					distrettoV = distretto.getSelectedItem().getValue();			

				if(presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals(""))
					codice_presidio="";
				else  
					codice_presidio=presidio_comune_area.getSelectedItem().getValue();
				
				String ass = cod_ass.getValue();
				String pre = cod_pre.getValue();
				String tp = tipo.getSelectedValue();
				String mod = modalitaStampa.getSelectedValue();
				String med ="";
				if (mmg.isChecked())
					med = "M";
				String sin = "";
				if (sintetica.isChecked())
					sin ="S";
				String ord = ordin.getSelectedValue();
				
				String esecutore = codope_es.getValue();

				// gb 21/09/07: fine *******

				// la tipologia di operatore la prendo in considerazione solo
				// nel caso in cui l'operatore non è definito e nel caso in cui
				// scelgo la stampa per presidio

				String metodo = "query_agenda";
				String report = "agenda_giorn.fo";
				
				if (sintetica.isChecked() && planningSettimanale.isChecked()){					
					metodo = "query_agenda_plan";
					report = "agenda_giorn_plan_sin.fo";
				}
				if (sintetica.isChecked() && !planningSettimanale.isChecked())
					report = "agenda_giorn_sin.fo";
				if (!sintetica.isChecked() && planningSettimanale.isChecked()) {
					logger.info(punto + "\n stampa con tipologia PLANNING");
					metodo = "query_agenda_plan";
					report = "agenda_giorn_plan.fo";
				}

				servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+ "?EJB=SINS_FOAGENDAGIORN&USER=" + user + "&ID509=" + passwd + "&METHOD=" + metodo
						+ "&codope=" + codice_operatore + "&data_inizio="
						+ data_inizio
						//					+ (jCheckBox1.isSelected() ? "" : "&data_fine=" + data_fine)
						+ "&data_fine=" + data_fine + "&codazsan=" + getProfile().getStringFromProfile(ManagerProfile.CODICE_USL) + "&codreg="
						+ getProfile().getStringFromProfile(ManagerProfile.CODICE_REGIONE) + "&REPORT=" + report + "&figprof=" + figprof + "&codpres="
						+ codice_presidio + "&distretto=" + distrettoV + "&zona=" + zone + "&ass=" + ass + "&pre=" + pre + "&tp=" + tp + "&med=" + med + "&sin=" + sin + "&ord=" + ord + "&mod=" + mod + "&esec=" + esecutore;

				logger.info("percorso servlet FoAgendaGiorn \n" + servlet);

				it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo() {
		try {
			CaribelComboRepository.addComboItem(tipoOperatore, "00", 								Labels.getLabel(ManagerOperatore.keyLabelTipoOperatore+"tutti"));
			CaribelComboRepository.addComboItem(tipoOperatore, GestTpOp.CTS_COD_ASSISTENTE_SOCIALE, Labels.getLabel(ManagerOperatore.keyLabelTipoOperatore+GestTpOp.CTS_COD_ASSISTENTE_SOCIALE));       
			CaribelComboRepository.addComboItem(tipoOperatore, GestTpOp.CTS_COD_INFERMIERE, 		Labels.getLabel(ManagerOperatore.keyLabelTipoOperatore+GestTpOp.CTS_COD_INFERMIERE));    
			CaribelComboRepository.addComboItem(tipoOperatore, GestTpOp.CTS_COD_MEDICO,				Labels.getLabel(ManagerOperatore.keyLabelTipoOperatore+GestTpOp.CTS_COD_MEDICO));        
			CaribelComboRepository.addComboItem(tipoOperatore, GestTpOp.CTS_COD_FISIOTERAPISTA, 	Labels.getLabel(ManagerOperatore.keyLabelTipoOperatore+GestTpOp.CTS_COD_FISIOTERAPISTA));
				
		}catch(Exception e){
			doShowException(e);
		}
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
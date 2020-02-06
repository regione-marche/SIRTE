package it.caribel.app.sinssnt.controllers.relazione_clinica_medico;

import it.caribel.app.sinssnt.bean.modificati.SkmClinicaEJB;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.util.Vector;

public class RelazioneClinicaMedicaGridCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = "MEDRECLI";   
	private SkmClinicaEJB myEJB = new SkmClinicaEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/relazione_clinica_medico/relazioneClinicaMedicoGrid.zul";

	private CaribelTextbox key_cartella;
	private CaribelTextbox key_contatto;

	private CaribelTextbox JLabelCognomeOp;
	private CaribelTextbox JLabelAssistito;
	private String ver = "2-";
	
	String nCartella = "";
	String nContatto="";
	private CaribelTextbox keyCartella;
	private CaribelTextbox keyContatto;
	String cognomeNomeAssistito = "";

	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query_clinica");
			
			super.setMethodNameForInsert("insert_clinica");
			super.setMethodNameForUpdate("update_relazione_clinica");
			super.setMethodNameForDelete("delete_relazione_clinica");
			super.setMethodNameForQueryKey("queryKeyRelazioneClinica");

			if (arg.get("n_cartella") == null) {
				CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
				if (containerCorr instanceof ContainerMedicoCtrl) {
					nCartella = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) containerCorr).hashChiaveValore,
							CostantiSinssntW.N_CARTELLA);
					nContatto = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) containerCorr).hashChiaveValore,
							CostantiSinssntW.N_CONTATTO);
					
					cognomeNomeAssistito = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) containerCorr).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_COGNOME);
					cognomeNomeAssistito += (ISASUtil.valida(cognomeNomeAssistito)?" ": "") + 
								ISASUtil.getValoreStringa(((ContainerMedicoCtrl) containerCorr).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
					
				} else {
					throw new Exception("Reperimento chiavi Diagnosi non riuscito!");
				}
				key_cartella.setText(nCartella);
				key_contatto.setText(nContatto);
				
				JLabelAssistito.setValue(cognomeNomeAssistito);
				JLabelCognomeOp.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
				
				doLoadGrid();
			} else {

			}
		} catch (Exception e) {
			doShowException(e);
		}
	}

	@Override
	protected void doLoadGrid() throws Exception {
		String punto = ver + "doLoadGrid";
		logger.trace(punto + " carica griglia ");
		if (ISASUtil.valida(key_cartella.getValue()) && ISASUtil.valida(key_contatto.getValue())) {
			hParameters.put(key_cartella.getDb_name(), key_cartella.getValue());
			hParameters.put(key_contatto.getDb_name(), key_contatto.getValue());

			hParameters.putAll(getOtherParametersString());
			Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
			clb.getItems().clear();
			clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
		} else {
			logger.debug(" \n Non effettuo il caricamento della griglia ");
		}
	}

	@Override
	protected boolean doValidateForm() {
		keyCartella.setValue(key_cartella.getValue());
		keyContatto.setValue(key_contatto.getValue());
//		boolean periodoConforme = ManagerDate.controllaPeriodo(self, skt_data_inizio, skt_data_fine, "sktdatainizio", "sktdatafine");
//
//		return periodoConforme;
		return true;
	}

	//	public void doAfterCompose(Component comp) throws Exception {
	//		String punto = ver + "doAfterCompose ";
	//		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
	//		super.doAfterCompose(comp);
	//		if (super.caribelSearchCtrl != null) {
	//			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
	//			UtilForBinding.bindDataToComponent(logger, super.caribelSearchCtrl.getLinkedParameterForQuery(), self);
	//			String textToSearch = (String) arg.get("textToSearch");
	//			if (textToSearch != null && !textToSearch.trim().equals("")) {
	//				textToSearch = textToSearch.toUpperCase();
	//				key_cartella.setText(textToSearch);
	//				super.hParameters.put(key_cartella.getDb_name(), textToSearch);
	//				doRefresh();
	//			}
	//		} else {
	//			String nCartella = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CARTELLA);
	//			String nContatto = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CONTATTO);
	//			String assistito = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CTS_COGNOME_NOME_ASSISTITO);
	//			logger.debug(punto + " dati che ricevo>>" + arg);
	//			JLabelCognomeOp.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
	//
	//			JLabelAssistito.setText(assistito);
	//			key_cartella.setText(nCartella);
	//			key_contatto.setText(nContatto);
	//			doCerca();
	//		}
	//	}
	//
	//	protected boolean doValidateForm() {
	//		CaribelDatebox smri_datadimis = (CaribelDatebox)self.getParent().getFellow("smri_datadimis");
	//		Date dataDimiss = smri_datadimis.getValue();
	//			  
	//		CaribelDatebox smes_data = (CaribelDatebox)self.getFellow("smes_data");
	//		Date dataEsame = smes_data.getValue();
	//			
	//		boolean isConclusione = dataDimiss!=null;
	//		
	//		// ctrl data esame < data fine intervento
	//        if (isConclusione)
	//		    if (dataEsame.after(dataDimiss)) {
	//			    Label lbl_smes_data = (Label)self.getFellow("lbl_smes_data");
	//			    Label lbl_smri_datadimis = (Label)self.getParent().getFellow("lbl_smri_datadimis");	    
	//			    smes_data.setErrorMessage(Labels.getLabel("common.msg.NoOrderDate.1maggioreDi0",
	//			    				new String[] {lbl_smes_data.getValue(),lbl_smri_datadimis.getValue()}));
	//			    return false;
	//		    }
	//		return true;
	//	}	
	//	
	//	public void doCerca() {
	//		try {
	//			UtilForComponents.testRequiredFields(self);
	//
	//			String punto = ver + "doCaricaGriglia ";
	//			String nCartella = key_cartella.getText();
	//			String nContatto = key_contatto.getText();
	//			super.hParameters.put(key_cartella.getDb_name(), nCartella);
	//			super.hParameters.put(key_contatto.getDb_name(), nContatto);
	//
	//			logger.trace(punto + " dati che devo esaminare>>" + (super.hParameters != null ? super.hParameters + "" : " no dati "));
	//			doRefresh();
	//		} catch (Exception e) {
	//			doShowException(e);
	//		}
	//	}
	//
	//	public void doPulisciRicerca() {
	//		try {
	//			setDefault();
	//		} catch (Exception e) {
	//			logger.error(this.getClass().getName() + ": Impossibile inizializzare il reparto, rivolgersi all'assistenza");
	//		}
	//	}
	//
	//	private void setDefault() throws Exception {
	//		if (caribellb.getItemCount() > 0) {
	//			caribellb.getItems().clear(); //.jCariTable1.deleteAll();
	//		}
	//
	//		key_cartella.setValue("");
	//		key_contatto.setValue("");
	//		//		key_tipo.setValue("");
	//	}
	//
	//	@Override
	//	protected void doStampa() {
	//
	//	}

}

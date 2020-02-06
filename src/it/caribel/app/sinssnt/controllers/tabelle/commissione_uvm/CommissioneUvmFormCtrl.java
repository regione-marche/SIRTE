package it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.bean.modificati.CommissUVMEJB;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridFormCtrl;
import it.caribel.zk.util.UtilForComponents;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

public class CommissioneUvmFormCtrl extends CommissioneUvmFormBaseCtrl {

		private static final long serialVersionUID = 1L;

	
//		private CaribelTextbox codice;
//		private CaribelTextbox codice_nascosto;
//		private CaribelTextbox descrizione;
//		private CaribelTextbox cb_zona;
//		private CaribelTextbox cb_distr;
//		private CaribelTextbox cb_pres;
//		private CaribelCombobox giorno;
//		
//		protected CaribelCombobox zona;
//		protected CaribelCombobox distretto;
//		protected CaribelCombobox presidio_comune_area;
//		
//		protected CaribelRadiogroup res_dom;
//		protected CaribelRadiogroup soc_san;
//		protected CaribelRadiogroup raggruppamento;
//		protected Label lbl_ubicazione;
//		protected Label presidio_comune_areadis;
//		protected Hlayout riga_raggruppamento;
//		private Component datiDettagli;
		
		public static String myKeyPermission = "A_COMUVM";
		private CommissUVMEJB myEJB = new CommissUVMEJB();
		private String ver = "3-";
		@Override
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				descrizione.setFocus(true);
				
				/*String strZona = cb_zona.getText();
				zona.setText(strZona);
				String strDistr = cb_distr.getText();
				distretto.setText(strDistr);
				String strPres = cb_pres.getText();
				presidio_comune_area.setText(strPres);*/
				Component p = self.getFellow("panel_ubicazione");
				PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
				
				caricaComboGiorno(giorno);
				c.doInitPanel(false);
				String zona_operatore = CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
				zona.setSelectedValue(zona_operatore);
				zona.setReadonly(true);
				c.settaRaggruppamento("P");
				res_dom.setVisible(false);
				raggruppamento.setVisible(false);
				soc_san.setVisible(false);
				lbl_ubicazione.setVisible(false);
				presidio_comune_area.setVisible(false);
				presidio_comune_areadis.setVisible(false);
				riga_raggruppamento.setVisible(false);
				zona.setDisabled(true);
				c.onSelect$zona(null);
				distretto.setSelectedIndex(0);
				//c.settaRaggrContatti("CA");
				zona.setRequired(true);
				distretto.setRequired(true);
				//presidio_comune_area.setRequired(true);
				String cod=codice.getValue();
				if (cod!=null && !cod.equals(""))
					codice_nascosto.setText(cod);
				
				distretto.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
//						settaDataPresaCarico();
						settaPresidio();
						return;
					}});
				
				if(dbrFromList!=null){
					hParameters.put("cm_cod_comm", dbrFromList.get("cm_cod_comm").toString());
					
					doQueryKeySuEJB();
					
					//doOtherQuery();
					doWriteBeanToComponents();
					setPannelloUbicazione();
					codice.setReadonly(true);
					descrizione.setFocus(true);
					
					doFreezeForm();//Rifaccio il doFreezeForm perchè è stato impostato il pannello ubicazione
					
				}else{
					codice.setReadonly(false);
				}
				
			}catch(Exception e){
				doShowException(e);
			}
		}


		protected void settaPresidio() {
			String punto = ver + "settaPresidio ";
			if (distretto!=null && distretto.getValue()!=null){
				logger.trace(punto + " imposto il distretto >>" +distretto.getSelectedValue()+ "< ");
				try{
					CaribelTextbox distretto_nascosto = (CaribelTextbox)datiDettagli.getFellowIfAny("distretto_nascosto", true);
					distretto_nascosto.setText(distretto.getSelectedValue());
				}catch(Exception e){
					logger.trace(punto + " Errore nell'impostare il distretto ", e);
				}
			}
		}

//		private void setPannelloUbicazione() {
//			zona.setSelectedValue(cb_zona.getValue());
//			Events.sendEvent(Events.ON_SELECT, zona, cb_zona.getValue());
//			distretto.setSelectedValue(cb_distr.getValue());
//			Events.sendEvent(Events.ON_SELECT, distretto, cb_distr.getValue());
//			presidio_comune_area.setSelectedValue(cb_pres.getValue());
//		}
//		
		
		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}

		protected boolean doSaveForm() throws Exception{
			if(!isSavable()){
				Messagebox.show(
						Labels.getLabel("permissions.insufficient.on.doSaveForm"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.ERROR);
				return false;
			}
			
			UtilForComponents.testRequiredFields(self);
			
			if(!doValidateForm())
				return false;

	    	Vector<Hashtable<String, String>> vettGriDettagli = new Vector<Hashtable<String, String>>();
	    	
	    	//griglia dettagli
	    	Component dettagli = self.getFellow("datiDettagli");
	    	CaribelGridFormCtrl ctrlDettagli = (CaribelGridFormCtrl)dettagli.getAttribute(MY_CTRL_KEY);
	    	vettGriDettagli = ctrlDettagli.getDataFromGrid();
	    	String strZona = zona.getSelectedValue();
			cb_zona.setText(strZona);
			String strDistr =distretto.getSelectedValue();
			cb_distr.setText(strDistr);
			String strPres = presidio_comune_area.getSelectedValue();
			cb_pres.setText(strPres);
			
			doWriteComponentsToBean();
			if(this.currentIsasRecord!=null){//caso UPDATE
	    		Object [] par = new Object[2];
	    		par[0]= this.currentIsasRecord;
	    		par[1]= vettGriDettagli;
	    		this.currentIsasRecord =updateSuEJB(this.currentBean,par);
	    	}else{//sto facendo un insert
	    		Object [] par = new Object[2];
	    		par[0]= this.hParameters;
	    		par[1]= vettGriDettagli;
	    		this.currentIsasRecord = insertSuEJB(this.currentBean,par);
			}
//			zona.setSelectedValue(cb_zona.getValue());
//			distretto.setSelectedValue(cb_distr.getValue());
//			presidio_comune_area.setSelectedValue(cb_pres.getValue());
			doWriteBeanToComponents();
			setPannelloUbicazione();
			if(this.caribelGrid!=null){
				//REFRESH SULLA LISTA
				this.caribelGrid.doRefreshNoAlert();
			}
			if(this.caribelContainerCtrl!=null){
				//REFRESH SUL CONTAINER
//				caribelContainerCtrl.doRefreshOnSave(compChiamante);
			}
			riCaricaComboCommissioni(strDistr);
			doFreezeForm();		
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
			return true;
		}
		
		protected void doDeleteForm() throws Exception{
			try{
				if(!isDeletable()){
					Messagebox.show(
							Labels.getLabel("permissions.insufficient.on.doDeleteForm"),
							Labels.getLabel("messagebox.attention"),
							Messagebox.OK,
							Messagebox.ERROR);
					return;
				}
				//griglia dettagli
		    	Vector<Hashtable<String, String>> vettGriDettagli = new Vector<Hashtable<String, String>>();
		    	Component dettagli = self.getFellow("datiDettagli");
		    	CaribelGridFormCtrl ctrlDettagli = (CaribelGridFormCtrl)dettagli.getAttribute(MY_CTRL_KEY);
		    	vettGriDettagli = ctrlDettagli.getDataFromGrid();
		    	
				//myEJB.delete(CaribelSessionManager.getInstance().getMyLogin(), currentIsasRecord.getHashtable(), vettGriDettagli);
				deleteSuEJB(currentBean, currentIsasRecord); //in questo caso non va bene!
				doFreezeForm();
				if(caribelGrid!=null){
					//REFRESH SULLA LISTA
					caribelGrid.doRefresh();
				}
				if(caribelContainerCtrl!=null){
					caribelContainerCtrl.doRefreshOnDelete(compChiamante);
				}
				compChiamante.detach();
				Clients.showNotification(Labels.getLabel("form.delete.ok.notification"),"info",compChiamante,"middle_center",2500);
			}catch (Exception e){
				doShowException(e);
			}
		}
//		public void caricaComboGiorno (CaribelCombobox cbx)throws Exception {
//			cbx.clear();
//			CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("generic.giorno1"));
//			CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("generic.giorno2"));
//			CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("generic.giorno3"));
//			CaribelComboRepository.addComboItem(cbx, "4", Labels.getLabel("generic.giorno4"));
//			CaribelComboRepository.addComboItem(cbx, "5", Labels.getLabel("generic.giorno5"));
//			
//			cbx.setValue(Labels.getLabel("generic.giorno1"));
//			
//		}
		
//		private void doOtherQuery() throws Exception {
//			if(currentIsasRecord != null && currentIsasRecord.get("cod_zona")!= null && !((String) currentIsasRecord.get("cod_zona")).isEmpty()){
//				Hashtable<String, Object> h = new Hashtable<String, Object>();
//				h.put("cod_zona", currentIsasRecord.get("cod_zona"));				
//				
//				
//			}
//			if(currentIsasRecord != null && currentIsasRecord.get("presidio_pas")!= null && !((String) currentIsasRecord.get("presidio_pas")).isEmpty()){
//				Hashtable<String, Object> h = new Hashtable<String, Object>();
//				h.put("codpres", currentIsasRecord.get("presidio_pas"));
//				ISASRecord pres = invokeSuEJB(new PresidiEJB(), h, "queryKey");
//				if(pres != null && !((String) pres.get("despres")).isEmpty()){					
//					currentIsasRecord.put("des_presP", pres.get("despres"));
//				}
//				
//			}
//		}
		
//		public void riCaricaComboCommissioni(String distrettoOperatore) throws Exception {
//			String punto = ver + "caricaComboCommissioni ";
//			String nomeCombo = nomeCombo(distrettoOperatore);
//			logger.trace(punto + " svuoto la combo>" +nomeCombo);
//			CaribelComboRepository.unload(nomeCombo);
//		}
		
//		private String nomeCombo(String distrettoOperatore) {
//			return CostantiSinssntW.CTS_COD_DISTRETTO_UVM+"_"+distrettoOperatore;
//		}


//		public void caricaComboCommissioni(CaribelCombobox cbx_commissioni, Hashtable<String, String> dati,
//				String distrettoOperatore) throws Exception {
//			String punto = ver + "caricaComboCommissioni ";
//			String nomeCombo = nomeCombo(distrettoOperatore);
//			logger.trace(punto + " nomecombo>>"+nomeCombo);
//			CaribelComboRepository.comboPreLoad(nomeCombo,  new CommissUVMEJB(), "query", dati, cbx_commissioni, null,
//				"cm_cod_comm", "cm_descr", true);
//		}

}

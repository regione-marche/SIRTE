package it.caribel.app.sinssnt.controllers.pianoAssistenziale;

import it.caribel.app.common.controllers.prestazioni.PrestazioniGridCtrl;
import it.caribel.app.common.ejb.OperatoriEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Toolbarbutton;

public class PianoAccessiGridCRUDCtrl extends CaribelGridCRUDCtrl {
	private static final long serialVersionUID = 1L;

	private CaribelTextbox JCariTextFieldTipoOper;
	private CaribelTextbox JCariTextFieldNProgetto;
	private CaribelTextbox JCariTextFieldCodObiettivo;
	private CaribelDatebox JCariDateTextFieldPianoAss;
	private CaribelTextbox JCariTextFieldNIntervento;
	private Component prestazione;
	public static String myKeyPermission = "PIANOACC";
	private PianoAssistEJB myEJB = new PianoAssistEJB();
	private CaribelTextbox cartella;

	private CaribelDatebox pi_data_inizio;
	private CaribelDatebox pi_data_fine;

	private CaribelTextbox pi_op_esecutore;
	private CaribelCombobox pi_op_esec_desc;
	private CaribelCombobox pi_freq;
	
	private CaribelListbox tablePrestazioni;
//	private Button btn_confermaSelezione;
	private Toolbarbutton btn_agenda;
	
	private CaribelTextbox codPrestazione;
	private CaribelTextbox descPrestazione;
	
	private Component operatore;
	private CaribelSearchCtrl operatoreEsecCtrl;
	CaribelSearchCtrl cbsPrestazone;
	private CaribelTextbox pi_prest_cod;
	private CaribelCombobox pi_prest_desc;
	private CaribelTextbox pi_pianificato;
	private CaribelIntbox pi_quantita;

	private String ver = "3-";
	
	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQueryKey("queryKey_pianoAcc");
			super.setMethodNameForQuery("query_pianoAcc");
			super.setMethodNameForDelete("delete_pianoAcc");
			super.setMethodNameForInsert("insert_pianoAcc");
			super.setMethodNameForUpdate("update_pianoAcc");

			cbsPrestazone = (CaribelSearchCtrl) prestazione.getAttribute(MY_CTRL_KEY);
			
			pi_prest_cod.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>(){
				public void onEvent(Event event){
					abilitaTabellaPrestazione();
				}
			});
			
			pi_prest_cod.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					abilitaTabellaPrestazione();
					return;
				}
			});
			
//			pi_prest_cod.addEventListener(CaribelSearchCtrl.on_b, new EventListener<Event>(){
//				public void onEvent(Event event){
//					abilitaTabellaPrestazione();
//				}
//			});
			
			clb.setItemRenderer(new PianoAccessiGridItemRenderer());
			operatoreEsecCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
			operatoreEsecCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, UtilForContainer.getTipoOperatorerContainer());
			operatoreEsecCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
			
			gestisciTipoOperatore();
			   
			JCariTextFieldTipoOper = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldTipoOper");    
			JCariTextFieldNProgetto = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldNProgetto");   
			JCariTextFieldCodObiettivo = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldCodObiettivo");
			JCariDateTextFieldPianoAss = (CaribelDatebox) self.getParent().getFellow("JCariDateTextFieldPianoAss");
			JCariTextFieldNIntervento = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldNIntervento"); 
			parkSetting.put("pi_op_esecutore",  ((CaribelTextbox) self.getParent().getFellow("cod_operatore")).getValue());
			parkSetting.put("pi_op_esec_desc", ((CaribelCombobox) self.getParent().getFellow("desc_operatore")).getValue());
			UtilForComponents.disableListBox(tablePrestazioni, true);

		} catch (Exception e) {
			doShowException(e);
		}
	}

	/**
	 * @throws Exception
	 */
	private void gestisciTipoOperatore() throws Exception {
		String tipoOperatore = UtilForContainer.getTipoOperatorerContainer(); 

		operatoreEsecCtrl.putLinkedSearchObjects("tipo_op_lock", ISASUtil.valida(tipoOperatore));
		OperatoriEJB operatoriEJB = new OperatoriEJB();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(OperatoriEJB.CTS_TIPO_OPERATORE, tipoOperatore);
		String intTipoOper = operatoriEJB.recuperaIntTipoOperatore(CaribelSessionManager.getInstance().getMyLogin(), dati);
		
		cbsPrestazone.putLinkedSearchObjects(PrestazioniGridCtrl.CTS_FIGURE_PROFESSIONALI, intTipoOper);
		cbsPrestazone.putLinkedSearchObjects(PrestazioniGridCtrl.CTS_BLOCCA_COMBO_PRESTAZIONI, ISASUtil.valida(intTipoOper));
	}

	protected void abilitaTabellaPrestazione() {
		String punto = ver + "abilitaTabellaPrestazione ";
		boolean abilitazionePrestazione = (pi_prest_cod !=null && ISASUtil.valida(pi_prest_cod.getValue()));
		logger.trace(punto + " abilitazione>>" +abilitazionePrestazione+"<");
		UtilForComponents.disableListBox(tablePrestazioni,abilitazionePrestazione );
	}

//	@Override
//	protected void doLoadGrid() throws Exception {
//		hParameters.putAll(getOtherParametersString());
//		// REFRESH SULLA LISTA
//		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//		CaribelListModel<ISASRecord> clm = new CaribelListModel<ISASRecord>(vDbr);
//		clm.setMultiple(true);
//		clb.setModel(clm);
//	}
	
	@Override
	protected Map<String,String> getOtherParametersString() {
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put(JCariTextFieldTipoOper.getDb_name(), JCariTextFieldTipoOper.getValue());
		ret.put(cartella.getDb_name(), cartella.getValue().toString());
		ret.put(JCariTextFieldNProgetto.getDb_name(), JCariTextFieldNProgetto.getValue());
		ret.put(JCariTextFieldCodObiettivo.getDb_name(), JCariTextFieldCodObiettivo.getValue());
		ret.put(JCariTextFieldNIntervento.getDb_name(), JCariTextFieldNIntervento.getValue());
		ret.put(JCariDateTextFieldPianoAss.getDb_name(), JCariDateTextFieldPianoAss.getValueForIsas());
		return ret;
	}

	@Override
	protected boolean doValidateForm() {
		Integer quant = pi_quantita.getValue();
		if (quant == null){
			pi_quantita.setValue(new Integer(1));
		}
		if(!ManagerDate.isPeriodoValido(pi_data_inizio, pi_data_fine)){
	      	  UtilForUI.standardExclamation(Labels.getLabel("common.msg.NoOrderDate.data1maggioreUgualeDi0", new String[]{Labels.getLabel("pianoAssistenziale.pianoAccessi.dataInizio"), Labels.getLabel("pianoAssistenziale.pianoAccessi.dataFine"), }));
	          return false;
       }
		return true;
	}
	
	protected void deleteSuEJB(Object myEJB,ISASRecord myDbr)throws Exception {
		Hashtable<String,String> temp = UtilForBinding.getHashtableFromComponent(myForm);
		temp.putAll(getOtherParametersString());
		myDbr = (ISASRecord)CaribelClass.isasInvoke(myEJB, "delete_pianoAcc", temp);
	}
	

	public void onSelect$tablePrestazioni(Event event){
		String punto = "onSelect$tablePrestazioni Eve";
		if (super.stato_corr.equals(CaribelGridCRUDCtrl.STATO_INSERT)){
			int prestazioniSelezionate = tablePrestazioni.getSelectedCount();
			boolean prestazioniObligatoria = (prestazioniSelezionate<=0);
			logger.trace(punto + " prestazioniSelezionate>"+prestazioniSelezionate+"< prestazioniObligatoria>"+prestazioniObligatoria+"<");
			abilitazionePrestazione(prestazioniObligatoria, true);
		}else {
			logger.trace(punto + " NON SONO IN INSERT ");
		}
	}

	private void abilitazionePrestazione(boolean abilitazione, boolean svuotaCampi) {
		cbsPrestazone.setRequired(abilitazione);	
		cbsPrestazone.setReadonly(!abilitazione);
		if (svuotaCampi){
			pi_prest_cod.setValue("");
			pi_prest_desc.setValue("");
		}
	}
	
	public void onClick$btn_formgrid_new() {
		super.onClick$btn_formgrid_new();
		pi_op_esecutore.setValue(((CaribelTextbox) self.getParent().getFellow("cod_operatore")).getValue());
		pi_op_esec_desc.setValue(((CaribelCombobox) self.getParent().getFellow("desc_operatore")).getValue());
	}
	
	@Override
	protected void executeInsert()throws Exception{
		String punto = ver + "executeInsert ";
		logger.trace(punto );
//		((PianoAssistenzialeFormCtrl) self.getParent().getSpaceOwner().getAttribute(MY_CTRL_KEY)).salvaAccesso();
		((PianoAssistenzialeFormCtrl) self.getParent().getSpaceOwner().getAttribute(MY_CTRL_KEY)).doSaveForm();
		doLoadGrid();
		Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",self,"middle_center",2500);
	}
	
//	@Override
//	protected void executeUpdate()throws Exception{
//		String punto = ver + "executeUpdate ";
//		logger.trace(punto );
////		((PianoAssistenzialeFormCtrl) self.getParent().getSpaceOwner().getAttribute(MY_CTRL_KEY)).salvaAccesso();
//		((PianoAssistenzialeFormCtrl) self.getParent().getSpaceOwner().getAttribute(MY_CTRL_KEY)).doSaveForm();
//		doLoadGrid();
//		Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",self,"middle_center",2500);
//	}
	
	protected void notEditable() {
		if(stato_corr.equals(STATO_UPDATE)){
			UtilForBinding.setComponentReadOnly(myForm, true);
//			pi_data_inizio.setReadonly(true);
			boolean dataFineReadOnly = pi_data_fine.getValue()!=null && (pi_data_fine.getValue()).before(new Date());
			if(pi_pianificato.getValue() == null || !pi_pianificato.getValue().equals("S")){
				UtilForBinding.setComponentReadOnly(pi_data_fine, false);
				UtilForBinding.setComponentReadOnly(pi_freq, false);
				operatoreEsecCtrl.setReadonly(false);
				pi_quantita.setReadonly(false);
			}else{
				btn_agenda.setDisabled(false);
				pi_data_fine.setRequired(pi_data_inizio!=null);
				UtilForBinding.setComponentReadOnly(pi_freq, pi_freq.getSelectedIndex()>-1);
			}
			pi_data_fine.setReadonly(dataFineReadOnly);
		}else{
			btn_agenda.setDisabled(true);
			pi_data_fine.setRequired(false);
		}
	}
	
	@Override
	protected void afterSetStatoInsert() {
		UtilForComponents.disableListBox(tablePrestazioni, false);
		codPrestazione.setReadonly(false);
		descPrestazione.setReadonly(false);
//		tablePrestazioni.setCheckmark(true);
		cbsPrestazone.setRequired(true);
		tablePrestazioni.clearSelection();
		tablePrestazioni.invalidate();
		UtilForComponents.disableListBox(tablePrestazioni, false);
		tablePrestazioni.setDisabled(false);
		abilitazionePrestazione(true, true);
//		btn_confermaSelezione.setDisabled(false);
		if (JCariDateTextFieldPianoAss.getValue() != null){
			pi_data_inizio.setConstraint("after " + UtilForComponents.formatDateforDatebox(JCariDateTextFieldPianoAss.getValue()));
			pi_data_inizio.invalidate();
		}
	}
	
	@Override
	protected void afterSetStatoWait() {
		tablePrestazioni.clearSelection();
//		UtilForComponents.disableListBox(tablePrestazioni, true);
		codPrestazione.setReadonly(true); 
		descPrestazione.setReadonly(true);
//		cbsPrestazone.setRequired(true);
//		tablePrestazioni.setCheckmark(false);
		tablePrestazioni.invalidate();
		UtilForComponents.disableListBox(tablePrestazioni, true);
		tablePrestazioni.setDisabled(false);
		abilitazionePrestazione(false, true);
//		btn_confermaSelezione.setDisabled(true);
	}

	@Override
	protected void afterSetStatoUpdate() {
		UtilForComponents.disableListBox(tablePrestazioni, false);
//		tablePrestazioni.setCheckmark(true);
//		codPrestazione.setReadonly(true); 
//		descPrestazione.setReadonly(true);		
		tablePrestazioni.clearSelection();
		cbsPrestazone.setRequired(true);
		tablePrestazioni.invalidate();
		UtilForComponents.disableListBox(tablePrestazioni, true);
		tablePrestazioni.setDisabled(true);
		abilitazionePrestazione(false, false);
//		btn_confermaSelezione.setDisabled(false);
	}
	
	public void setStatoAcc(String stato) throws Exception{
		setStato(stato);
		doLoadGrid();
	}
}

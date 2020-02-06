package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.controllers.agenda.CostantiAgenda;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;

public class PAIPianSettFormCtrl extends CaribelFormCtrl {

	private static final long serialVersionUID = 1L;

	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/precaricamentoAgendaForm.zul";

	private CaribelListbox clbPrecaricamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected Component cs_operatore;
	protected CaribelSearchCtrl operatoreCSCtrl;
	protected Checkbox checkBoxNotificaPresaCarico;
	protected CaribelTextbox operatore_cod;
	protected CaribelCombobox operatore_descr;
	protected Component panel_ubicazione;
	protected PanelUbicazioneCtrl panel_ubicazione_ctrl;
	protected CaribelDatebox tmp_dataInizio;

	protected Button bottoneOperatore;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void doInitForm() {
		try {
//			super.initCaribelFormCtrl(myEJB, myKeyPermission);
//			clbPrecaricamento.setItemRenderer(clsnm);
			clbPrecaricamento.setItemRenderer(new PAIGridItemRenderer());
			if(arg.containsKey(CostantiSinssntW.PIANIFICAZIONE_PAI)){
				clbPrecaricamento.setModel(new CaribelListModel((Vector<?>) arg.get(CostantiSinssntW.PIANIFICAZIONE_PAI)));
			}else{
				clbPrecaricamento.setModel(new CaribelListModel((Vector<?>) CostantiAgenda.getHashtablePianificazione(CostantiAgenda.getPianificazioneVuota())));
			}
			
			
			inizializzaPannelloUbicazione();
			if(arg.containsKey(CostantiSinssntW.COD_OPERATORE)){
				operatore_cod.setValue((String) arg.get(CostantiSinssntW.COD_OPERATORE));
				Events.sendEvent(Events.ON_CHANGE, operatore_cod, operatore_cod.getValue());
			}
			operatoreCSCtrl = (CaribelSearchCtrl) cs_operatore.getAttribute(MY_CTRL_KEY);
			if(tmp_dataInizio != null){
				operatoreCSCtrl.putLinkedComponent("dataInizioPrestazione", tmp_dataInizio);
			}
			if(arg.containsKey(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI)){
				operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, arg.get(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI));
				operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zona);
				operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto);
				operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_PRESIDIO, presidio_comune_area);
			}else{
				operatoreCSCtrl.setReadonly(true);
				checkBoxNotificaPresaCarico.setDisabled(true);
				bottoneOperatore.setDisabled(true);
			}
			
			doFreezeForm();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void inizializzaPannelloUbicazione() {

		distretto.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
			public void onEvent(Event event){
				operatore_cod.setValue(null);
				Events.sendEvent(Events.ON_CHANGE, operatore_cod, null);
//				onChangeDistretto();
			}
		});	
		
		presidio_comune_area.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
			public void onEvent(Event event){
//				onChangeUbicazione();
				operatore_cod.setValue(null);
				Events.sendEvent(Events.ON_CHANGE, operatore_cod, null);
			}
		});
		
//		Component p = self.getFellow("panel_ubicazione");
//		PanelUbicazioneCtrl panel_ubicazione_ctrl = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
		panel_ubicazione_ctrl = (PanelUbicazioneCtrl) panel_ubicazione.getAttribute(MY_CTRL_KEY);
		panel_ubicazione_ctrl.doInitPanel();			
		panel_ubicazione_ctrl.settaRaggruppamento("P"); //NoUbic("P");
		panel_ubicazione_ctrl.setVisibleRaggruppamento(false);
		panel_ubicazione_ctrl.setVisibleUbicazione(false);
		String codZona =getProfile().getStringFromProfile("zona_operatore"); 
		if (ISASUtil.valida(codZona)){
			zona.setValue(codZona);
		}
		panel_ubicazione_ctrl.setVisibleZona(false);
		try {
			String distretto_operatore = getProfile().getStringFromProfile(ManagerProfile.DISTRETTO_OPERATORE);
			String distrettoPresaCarico = (String) arg.get(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO);
			String distrettoDaSettare = (distrettoPresaCarico != null && !distrettoPresaCarico.isEmpty()) ? distrettoPresaCarico : distretto_operatore;
			if (distretto !=null && distrettoDaSettare != null && !distrettoDaSettare.isEmpty()){
				distretto.setSelectedValue(distrettoDaSettare);
//		 		distretto.setDisabled(true);
//				panel_ubicazione_ctrl.onSelect$zona(null);
				panel_ubicazione_ctrl.onSelect$distretto(null);
				String sede = arg.containsKey(CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO) ? (String) arg.get(CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO):getProfile().getStringFromProfile(ManagerProfile.PRES_OPERATORE);
				presidio_comune_area.setSelectedValue(sede);
			}
			//				onChangeTipoOperatore();
		} catch (Exception e) {
			logger.trace("inizializzaPannelloUbicazione:Errore nel recuperare i dati ", e);
		}
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}
	
	@Override
	public void onClose(Event event) {
		super.onClose(event);
	}
	
	@Override
	protected boolean doSaveForm() throws Exception {
		UtilForBinding.getDataFromGrid(clbPrecaricamento);
		PrestazioniPAICaribelGridFormCtrl ctrl = ((PrestazioniPAICaribelGridFormCtrl) this.getForm().getParent().getAttribute(MY_CTRL_KEY));
		if(ctrl==null){
			return false;
		}
		Hashtable<String, Object> h = new Hashtable<String, Object>();
		h.put("pai_cod_operatore", operatore_cod.getValue());
		h.put(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE, operatore_descr.getValue());
		if(tmp_dataInizio.getValue()!=null){
			h.put("dataInizioPrestazione", tmp_dataInizio.getValue());
		}
//		boolean ret = ctrl.updatePrestazioni(getDataFromGrid(), operatore_cod.getValue(), operatore_descr.getValue());
		boolean ret = ctrl.updatePrestazioni(getDataFromGrid(), h);
		if(checkBoxNotificaPresaCarico.isChecked()){
			Hashtable<String, Object> comps = operatoreCSCtrl.getLinkedSearchObjects();
			Object obj = comps.get(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI);
			if(obj instanceof String){
				((SegreteriaOrganizzativaFormCtrl) arg.get("segreteriaCtrl")).notificaPresaCaricoOperatore(obj.toString(), operatore_cod.getValue());
			} else if(obj instanceof CaribelCombobox){
				((SegreteriaOrganizzativaFormCtrl) arg.get("segreteriaCtrl")).notificaPresaCaricoOperatore(((CaribelCombobox) obj).getValue(), operatore_cod.getValue());
			}
		}
		if(ret){
			doFreezeForm();
		}
		return ret;
	}
	
	public void onClick$btn_save() {
	}
	
	public void onClick$btn_transfer() {
		try {
			doSaveForm();
			self.detach();
		} catch (Exception e) {
			doShowException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public Vector<Hashtable<String, String>> getDataFromGrid() {
			Vector<Hashtable<String, String>> vettore=new Vector<Hashtable<String, String>>();
	
			//controllo che la caritable non sia vuota
			if(this.clbPrecaricamento.getModel().getSize() != 0){
				Hashtable<String, String> h = null;
		    	String dbName = "";
		    	String valCorr = "";
		    	Hashtable<String, ?> data = null;
		    	for (int i = 0; i < clbPrecaricamento.getItemCount(); i++) {
			    	Object myData = clbPrecaricamento.getModel().getElementAt(i);
			    	h = new Hashtable<String, String>();
			    	if(myData instanceof ISASRecord){
			    		data = ((ISASRecord) myData).getHashtable();
			    	}else if(myData instanceof Hashtable){
			    		data = (Hashtable<String, ?>)myData;
			    	}
					
			    	Enumeration<String> enumKeysData = data.keys();
			    	while (enumKeysData.hasMoreElements()){
		  				dbName = (String)enumKeysData.nextElement();
						if(data.get(dbName)==null)
							valCorr = "";
						else if(data.get(dbName) instanceof java.sql.Date){
							valCorr=UtilForBinding.getValueForIsas((java.sql.Date)data.get(dbName));
						}else if(data.get(dbName) instanceof java.util.Date){
							valCorr=UtilForBinding.getValueForIsas((java.util.Date)data.get(dbName));
						}else if(data.get(dbName) instanceof java.lang.Integer){
							valCorr =((java.lang.Integer)data.get(dbName)).toString();
						}else if(data.get(dbName) instanceof java.lang.Double){
							valCorr = ((java.lang.Double)data.get(dbName)).toString();
							valCorr = ((String)valCorr).replace(".", ",");
						}else if(data.get(dbName) instanceof BigDecimal){
							valCorr = ((BigDecimal)data.get(dbName)).toString();
							valCorr = ((String)valCorr).replace(".", ",");
						}else{
							valCorr= (String)data.get(dbName);
						}
						
						h.put(dbName, valCorr);
					}
			    	try {
			    		h.putAll(UtilForBinding.getHashtableFromComponent(clbPrecaricamento.getItemAtIndex(i)));
			    	} catch (Exception e) {
			    		e.printStackTrace();
			    	}
					vettore.addElement(h);
				}
			}
			return vettore;
		}
	
	public void onRicercaOperatori(Event event) throws Exception{
		try{
			Map<String, Object> params = new Hashtable<String, Object>();
//			params.put(CostantiSinssntW.PIANIFICAZIONE_PAI, CostantiSinssntWAgenda.getHashtablePianificazione(pattern));
			
			params.put("segreteriaCtrl", "");
//			params.put("caribelContainerCtrl", this.caribelContainerCtrl);
			params.put("caribelSearchCtrl", operatoreCSCtrl);
			params.put(CostantiSinssntW.TIPO_OPERATORE, getTipoOperatore());
			if(arg.containsKey(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI)){
				params.put(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, arg.get(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI));
			}
			params.put(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO, distretto.getSelectedValue());
			params.put(CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO, presidio_comune_area.getSelectedValue());
			Executions.getCurrent().createComponents(RicercaOperatoriGridCtrl.myPathFormZul, getForm(), params );
		}catch(Exception e){
			doShowException(e);
		}
	}

	protected String getTipoOperatore() throws Exception {
		if(arg.containsKey(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI)){
			return (String) arg.get(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI);
		}else{
			return UtilForContainer.getTipoOperatorerContainer();
		}
	}
	
}

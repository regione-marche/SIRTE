package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

//import it.caribel.app.sins_sm.controllers.sm_psicoFarmaci.SM_PsicoFarmaciSearchCtrl;
//import it.caribel.app.sins_sm.controllers.sm_tabPre.SM_TabPreSearchCtrl;
import it.caribel.app.sinssnt.controllers.agenda.CostantiAgenda;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class PrestazioniPAICaribelGridFormCtrl extends PrestazioniPAICaribelGridFormBaseCtrl {

	private static final long serialVersionUID = 6570378279356476533L;
//	private CaribelRadiogroup tipoPrestazioneFarmaco;
//	private Vlayout sceltaPrestazioneFarmaco;
//	private CaribelTextbox cod_prestazione;
//	private CaribelTextbox pre_des_dett;
//	private Component prestazione;
//	private CaribelSearchCtrl prestazioneCtrl;
//	private CaribelDecimalbox pre_importo;
//	private CaribelIntbox sp_quantita;
//	private CaribelTextbox pre_note;
//	private CaribelTextbox tipo_prestazione;
//	private CaribelCombobox tipoCura;

	protected boolean doValidateForm(){
//		if(cod_prestazione==null){
//			cod_prestazione = (CaribelTextbox) sceltaPrestazioneFarmaco.getFellowIfAny("cod_prestazione", true);
//		}
//		return cod_prestazione.getValue() != null && !cod_prestazione.getValue().isEmpty(); 
		return true;
	}
	
	protected void doInitGridForm(){
		clb.setItemRenderer(new PAIGridItemRenderer());
//		prestazioneCtrl = (CaribelSearchCtrl) prestazione.getAttribute(MY_CTRL_KEY);
//		prestazioneCtrl.putLinkedComponent("pre_importo", pre_importo);
//		prestazioneCtrl.putLinkedComponent("pre_numero", sp_quantita);
//		prestazioneCtrl.putLinkedComponent("pre_des_dett", pre_des_dett);
//		prestazioneCtrl.putLinkedComponent("pre_note", pre_note);
//		prestazioneCtrl.setMethodNameForQueryKey("query_ximporto");
//		prestazioneCtrl.putLinkedSearchObjects("tipo_oper", "05");
//		prestazioneCtrl.putLinkedSearchObjects("provenienza", "interv");
		if(tipoCura == null){
			tipoCura = (CaribelCombobox) self.getParent().getFellowIfAny("tipocura", true);
		}
//		prestazioneCtrl.putLinkedSearchObjects("prest_tipo", tipo_prestazione);
	}
	/* Portata sopra 
	public void executeDelete() throws Exception{
		int riga = clb.getSelectedIndex();
		if (riga < 0)
			UtilForUI.doAlertSelectOneRow();
		/*01/12/2006 Controllo che non sia l'ultima prestazione* /
		else {
//			if (clb.getItemCount() == 1) {
//				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.impossibileCancelareUltimaPrestazione"));
//				return;
//			}
			try{
//				invokeGenericSuEJB(new IntpreEJB(), (Hashtable<?,?>) clb.getSelectedItem().getAttribute("ht_from_grid"), "delete");
//				clb.removeItemAt(riga);
//				((CaribelListModel) clb.getModel()).remove(riga);
//				for (Iterator iterator = clb.getSelectedItems().iterator(); iterator.hasNext();) {
//					clb.getModel()
//					Listitem type = (Listitem) iterator.next();
//					((CaribelListModel) clb.getModel()).remove(clb.getIndexOfItem(type));
//				}
				Vector<Integer> itemsId = new Vector();
			       for (Listitem item : clb.getSelectedItems()) {
			    	   itemsId.add(clb.getIndexOfItem(item));
			       }
			       Collections.sort(itemsId);
			       Collections.reverse(itemsId);
			       CaribelListModel clm = (CaribelListModel) clb.getModel();
			       for (Integer id : itemsId) {
						clm.remove(id.intValue());
//						clb.removeItemAt(id);
//						((CaribelListModel) clb.getModel()).remove(id);
    	           }
			       clm.setMultiple(clb.isMultiple());
			       clb.setModel(clm);
			       clb.invalidate();
			} catch(Exception e){
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.cancellazioneFallita"));
			}
		}
	}
	*/
	protected void notEditable() {
//		pre_des_dett.setReadonly(true);
		if(clb.getSelectedIndex()>-1){
//			prestazioneCtrl.setReadonly(true);
		}
	}
	
//	public void executeUpdate() throws Exception{
//		boolean save = doSaveInterv();
//	}

	public void onClick$btn_formgrid_new() {
//		super.onClick$btn_formgrid_new();
		onAggiungiPrestazioni();
	}

	public void onAggiungiPrestazioni() {
		try{
			this.clb.setSelectedIndex(-1);
//			UtilForBinding.resetForm(myForm,this.parkSetting);
//			this.setStato(STATO_INSERT);
			Map<String, Object> params = new Hashtable<String, Object>();
			params.put("prestazioni", this.clb.getModel());
			params.put("segreteriaCtrl", ((Component)clb.getSpaceOwner()).getAttribute(MY_CTRL_KEY));
			Window panelUbic = (Window) self.getParent().getSpaceOwner().getFellowIfAny("ubicazioneO",true);
			if(panelUbic!=null){
				params.put(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO, ((CaribelCombobox) panelUbic.getFellowIfAny("distretto", true)).getSelectedValue());
				params.put(CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO, ((CaribelCombobox) panelUbic.getFellowIfAny("presidio_comune_area", true)).getSelectedValue());
			}
			Executions.getCurrent().createComponents(AggiungiPrestazioniFormCtrl.myPathFormZul, (Component)clb.getSpaceOwner(), params );
		}catch(Exception e){
			doShowException(e);
		}
	}

	public void caricaPrestazioni(HashMap<String, Object> par) {
		CaribelListModel clm = (CaribelListModel) clb.getModel();
		Hashtable<String, Object> ht = null;
		Hashtable searchParams = new Hashtable();
		for (Iterator iterator = ((Collection) par.get("prestazioni")).iterator(); iterator.hasNext();) {
			ht = (Hashtable<String, Object>) iterator.next();
			ht.put(CostantiSinssntW.CTS_TIPOCURA, tipoCura.getSelectedValue());
			searchParams.put("prest_cod", ((String)ht.get("prest_cod")).trim());
			if(clm.columnsContains(searchParams)==-1){
				clm.add(ht);
			}
		}
		Clients.showNotification(Labels.getLabel("segreteria.organizzativa.msg.pretazioniAggiunte"), "info", null, "after_center", 2500);
//		clm.addAll((Collection) par.get("prestazioni"));
//		clb.setModel(clm);
	}
	
	@Override
	public void onDoubleClickedItem(Event event) throws Exception {
		try{
		}catch(Exception e){
			doShowException(e);
		}
	}
//
//	private boolean doSaveInterv() throws Exception {
//		AccessiFormCtrl accessi = (AccessiFormCtrl) self.getParent().getSpaceOwner().getAttribute(MY_CTRL_KEY);
//		return accessi.doSaveForm();
//	}
	protected void afterSetStatoWait() {
		btn_formgrid_edit.setVisible(false);
		btn_formgrid_delete.setVisible(false);
	}


	protected void afterSetStatoUpdate() {
		btn_formgrid_edit.setVisible(false);
		btn_formgrid_delete.setVisible(false);
	}


	protected void afterSetStatoInsert() {
		btn_formgrid_edit.setVisible(false);
	}

	public Vector<Hashtable<String, String>> getDataFromGrid() {
		Vector<Hashtable<String, String>> vettore=new Vector<Hashtable<String, String>>();

		//controllo che la caritable non sia vuota
		if(this.clb.getModel().getSize() != 0){
//			boolean primo = true;
			Hashtable<String, String> h = null;
			String dbName = "";
			String valCorr = "";
			Hashtable<String, ?> data = null;
			//				for (Iterator<Object> iterator2 = ((CaribelListModel<Object>) this.clb.getModel()).iterator(); iterator2.hasNext();) {
			//		    	Object myData = iterator2.next();
			for (int i = 0; i < clb.getItemCount(); i++) {
				Object myData = clb.getModel().getElementAt(i);
				h = new Hashtable<String, String>();
				if(myData instanceof ISASRecord){
					data = ((ISASRecord) myData).getHashtable();
				}else if(myData instanceof Hashtable){
					data = (Hashtable<String, ?>)myData;
				}

				Enumeration<String> enumKeysData = data.keys();
				//			    	for (Object header : this.clb.getListhead().getChildren()) {
				//						dbName = ((CaribelListheader)header).getDb_name();
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
					//			    		if(primo ){
					h.putAll(UtilForBinding.getHashtableFromComponent(clb.getItemAtIndex(i)));
					//			    			primo = false;
					//			    		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				vettore.addElement(h);
			}
		}
		return vettore;
	}
	
	public void onOpenPianificazione(Event event) throws Exception{
		try{
			String pattern;
			if(clb.getSelectedItem()==null){
				pattern = (String) ((Hashtable<String, String>)event.getTarget().getParent().getAttribute("ht_from_grid")).get(CostantiSinssntW.PIANIFICAZIONE_PAI);
			}
			
			pattern = (String) ((Hashtable<String, String>) clb.getSelectedItem().getAttribute("ht_from_grid")).get(CostantiSinssntW.PIANIFICAZIONE_PAI);
			
			Map<String, Object> params = new Hashtable<String, Object>();
			params.put(CostantiSinssntW.PIANIFICAZIONE_PAI, CostantiAgenda.getHashtablePianificazione(pattern));
			
	    	String figuraProfessionale = "";
	    	String cod_operatore = "";
	    	for (Iterator iterator = clb.getSelectedItems().iterator(); iterator.hasNext();) {
	    		Listitem type = (Listitem) iterator.next();
	    		Hashtable h = (Hashtable) type.getAttribute("ht_from_grid");
//	    		int count = 0; //gb 07/08/07
	    		String newFigProf = ISASUtil.getValoreStringa(h, "cod_fig_prof");
	    		if(figuraProfessionale.isEmpty() || figuraProfessionale.equals(newFigProf)){
	    			figuraProfessionale = newFigProf;
	    		}else{
	    			figuraProfessionale = "";
	    			break;
	    		}
	    		String newCodOperatore = ISASUtil.getValoreStringa(h, "pai_cod_operatore");
	    		if(cod_operatore != null && (cod_operatore.isEmpty()|| cod_operatore.equals(newCodOperatore))){
	    			cod_operatore = newCodOperatore;
	    		}else{
	    			cod_operatore = null;
	    		}
	    	}
			if(!figuraProfessionale.isEmpty()){
				params.put(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, figuraProfessionale); 
				if(cod_operatore!=null){
					params.put(CostantiSinssntW.COD_OPERATORE, cod_operatore);
				}
			}
			
			params.put("segreteriaCtrl", ((Component)((Component)clb.getSpaceOwner()).getParent().getSpaceOwner()).getAttribute(MY_CTRL_KEY));
			Window panelUbic = (Window) self.getParent().getSpaceOwner().getFellowIfAny("ubicazioneO",true);
			if(panelUbic!=null){
				params.put(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO, ((CaribelCombobox) panelUbic.getFellowIfAny("distretto", true)).getSelectedValue());
				params.put(CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO, ((CaribelCombobox) panelUbic.getFellowIfAny("presidio_comune_area", true)).getSelectedValue());
				
			}
			Executions.getCurrent().createComponents(PAIPianSettFormCtrl.myPathFormZul, (Component)clb.getSpaceOwner(), params );
		}catch(Exception e){
			doShowException(e);
		}
	}



	public boolean updatePrestazioni(Vector<Hashtable<String, String>> dataFromGrid, Hashtable parametri) {
//		boolean gridReload=false;
		String pattern = CostantiAgenda.getStringPianificazione(dataFromGrid);
		CaribelListModel clm = (CaribelListModel) clb.getModel();
		Hashtable data = new Hashtable();
		CopyOnWriteArrayList selezionati = new CopyOnWriteArrayList(clb.getSelectedItems());
		String operatore_cod = (String) parametri.get("pai_cod_operatore");
		String operatore_desc = (String) parametri.get(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE);
		Date dataInizioPrestazione =  (Date) parametri.get("dataInizioPrestazione");
		for (Iterator<Listitem> iterator = selezionati.iterator(); iterator.hasNext();){//Listitem item : clb.getSelectedItems()) {
			Listitem item = iterator.next();
			Object myData = UtilForBinding.estraiDatiDallaRigaI(clb, clb.getIndexOfItem(item));
	    	if(myData instanceof ISASRecord){
	    		data = ((ISASRecord) myData).getHashtable();//((ISASRecord) myData).getDBRecord().getHashtable();
	    	}else if(myData instanceof Hashtable){
	    		data = (Hashtable<String, ?>)myData;
	    	}
	    	data.put(CostantiSinssntW.PIANIFICAZIONE_PAI, pattern);
			((Hashtable<String, String>) item.getAttribute("ht_from_grid")).put(CostantiSinssntW.PIANIFICAZIONE_PAI, pattern);
			if(dataInizioPrestazione != null){
				data.put("pai_data_inizio", dataInizioPrestazione);
				((Hashtable<String, Object>) item.getAttribute("ht_from_grid")).put("pai_data_inizio", dataInizioPrestazione);
			}
			if(operatore_cod != null && operatore_desc != null){
				data.put("pai_cod_operatore", operatore_cod);
				data.put(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE, operatore_desc);
				((Hashtable<String, String>) item.getAttribute("ht_from_grid")).put("pai_cod_operatore", operatore_cod);
				((Hashtable<String, String>) item.getAttribute("ht_from_grid")).put(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE, operatore_desc);
			}
			clm.set(clb.getIndexOfItem(item), data);// TODO VFR righe multiple
			item.invalidate();
			clb.renderItem(item);
//			gridReload = true;
		}
		
		return true;
	}
}

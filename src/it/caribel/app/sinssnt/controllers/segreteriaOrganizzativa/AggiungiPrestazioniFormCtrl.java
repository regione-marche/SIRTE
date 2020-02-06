package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.controllers.agenda.CostantiAgenda;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.FiltroCodiceDescrizione;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.impl.InputElement;

public class AggiungiPrestazioniFormCtrl extends PAIPianSettFormCtrl {

	private static final long serialVersionUID = 1L;

	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/aggiungiPrestazioni.zul";

	private CaribelCombobox pai_figProf;
	private CaribelDatebox pai_data_inizio;
	private CaribelDatebox pai_data_fine;
	private CaribelCombobox pai_freq;
	private CaribelIntbox pai_prest_qta;

	public static String myKeyPermission = "PIANOACC";
	private PianoAssistEJB myEJB = new PianoAssistEJB();

	private CaribelListbox tablePrestazioni;
	private Button btn_confermaSelezione;

	private CaribelTextbox codPrestazione;
	private CaribelTextbox filtroDescrizione;
	
	private Component operatore;

	private CaribelListModel<ISASRecord> modelloPrestazioni = new CaribelListModel<ISASRecord>();
	protected Predicate filtroPrestazioni;

	private Vector<String> vettPrestazioni = new Vector<String>();

	@SuppressWarnings("unchecked")
	@Override
	public void doInitForm() {
		try {
			super.doInitForm();
			filtroPrestazioni = new FiltroCodiceDescrizione(null, filtroDescrizione, tablePrestazioni, "prest_cod", "prest_des");
			((FiltroCodiceDescrizione)filtroPrestazioni).setMantieniSelezione(true);
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
//			super.setMethodNameForQueryKey("queryKey_pianoAcc");
//			super.setMethodNameForQuery("query_pianoAcc");
//			super.setMethodNameForDelete("delete_pianoAcc");
//			super.setMethodNameForInsert("insert_pianoAcc");
//			super.setMethodNameForUpdate("update_pianoAcc");

//			operatoreEsecCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
//			operatoreEsecCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, UtilForContainer.getTipoOperatorerContainer());
//			operatoreEsecCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
//			
//			JCariTextFieldTipoOper = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldTipoOper");    
//			JCariTextFieldNProgetto = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldNProgetto");   
//			JCariTextFieldCodObiettivo = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldCodObiettivo");
//			JCariDateTextFieldPianoAss = (CaribelDatebox) self.getParent().getFellow("JCariDateTextFieldPianoAss");
//			JCariTextFieldNIntervento = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldNIntervento"); 
//			parkSetting.put("pi_op_esecutore",  ((CaribelTextbox) self.getParent().getFellow("cod_operatore")).getValue());
//			parkSetting.put("pi_op_esec_desc", ((CaribelCombobox) self.getParent().getFellow("desc_operatore")).getValue());
//			UtilForComponents.disableListBox(tablePrestazioni, true);
			caricaCombo();
			if(arg.containsKey("prestazioni")){
				for (Iterator iterator = ((CaribelListModel) arg.get("prestazioni")).iterator(); iterator.hasNext();) {
					Object type = iterator.next();
					Hashtable<String, Object> ht = null;
					if(type instanceof ISASRecord){
						ht = ((ISASRecord) type).getHashtable();
					}else if(type instanceof Hashtable){
						ht = (Hashtable)type;
					}
					vettPrestazioni.add((String) ht.get("prest_cod"));
				}
			}
			Date start = ((CaribelDatebox)((Component)self.getParent().getParent().getSpaceOwner()).getFellowIfAny("data_inizio", true)).getValue();
			Date stop = ((CaribelDatebox)((Component)self.getParent().getParent().getSpaceOwner()).getFellowIfAny("data_fine", true)).getValue();
			if(start!=null && stop !=null){
				String startDate = UtilForComponents.formatDateforDatebox(start);
				String endDate   = UtilForComponents.formatDateforDatebox(stop);
				String constraint = "between " + startDate +" and " + endDate +" ";
				pai_data_inizio.setConstraint(constraint);
				pai_data_fine.setConstraint(constraint);
				CaribelDatebox o = (CaribelDatebox) ((Component)self.getParent().getParent().getSpaceOwner()).getFellowIfAny("data_presa_carico_skso", true); 
				if(o!=null && o.getValue()==null){
					pai_data_inizio.setValue(start);
				}
				pai_data_fine.setValue(stop);
			}
//			doLoadGrid();
			filtroDescrizione.addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public void onEvent(Event event){
					try{
						if(event.getName().equals(Events.ON_CHANGING)){
							((InputElement) event.getTarget()).setRawValue(((InputEvent)event).getValue().toUpperCase());
							Set prestazioni = new TreeSet<String>();
							for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
								Listitem litem = (Listitem) iterator.next();
								String cod_prest = (String)((CaribelListModel) tablePrestazioni.getModel()).getFromRow(litem, "prest_cod");
								prestazioni.add(cod_prest);
							}
							Collection col = getPrestazioni();
							CaribelListModel mod = new CaribelListModel(col);
							tablePrestazioni.setModel(mod);

							Set tmp = new HashSet(tablePrestazioni.getSelectedItems());

							for (Iterator iterator = prestazioni.iterator(); iterator.hasNext();) {
								String codice = (String) iterator.next();
								if (codice != null) {
									//ricerco il codice nella griglia
									Hashtable hTrova = new Hashtable();
									hTrova.put("prest_cod", codice);
									int riga = mod.columnsContains(hTrova);
									if (riga != -1) {
										logger.trace("Selezione della prestazione: "+codice);
										Object o = mod.remove(riga);
										mod.add(0, o);
										tmp.add(tablePrestazioni.getItemAtIndex(0));
									}
								}
							}//fine for
							mod.setMultiple(true);
							tablePrestazioni.setSelectedItems(tmp);
//							tablePrestazioni.invalidate();
						}
					}catch(Exception e){
						doShowException(e);
					}
				}});
			// sovrascrivo i vincoli per il caribel search dell'operatore
			operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, pai_figProf);
			operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zona);
			operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto);
			operatoreCSCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_PRESIDIO, presidio_comune_area);
			
			operatore_cod.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if (event.getName().equals(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH)&&operatoreCSCtrl.getCurrentRecord()!=null) {
						Date dataInPre = (Date) operatoreCSCtrl.getCurrentRecord().get("dataInizioPrestazione");
						if(dataInPre!= null ){
							pai_data_inizio.setValue(dataInPre);
						}
					}
				}
			});
			doFreezeForm();
		} catch (Exception e) {
			doShowException(e);
		}
	}
	
	@Override
	protected String getTipoOperatore() throws Exception {
		return pai_figProf.getSelectedValue();
	}
	
	private void caricaCombo() throws Exception {
		//Caricamento combo frequenza accessi
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
		h_xCBdaTabBase.put("FREQAC",   pai_freq);

		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val", "tab_descrizione", true);
		pai_freq.setSelectedValue(3);
		//Caricamento combo tipi operatori
		String opCaricati = ManagerOperatore.loadTipiOperatori(pai_figProf, CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE, true);
		logger.trace("Aggiungi prestazioni: operatori caricati\n"+opCaricati);
		pai_figProf.setSelectedValue(UtilForContainer.getTipoOperatorerContainer());
	}

	@SuppressWarnings("unchecked")
	public Collection getPrestazioni() {
		return CollectionUtils.select(modelloPrestazioni != null ? modelloPrestazioni : CollectionUtils.EMPTY_COLLECTION, filtroPrestazioni);
	}

	protected void doLoadGrid() throws Exception {
		hParameters.putAll(getOtherParametersString());
			//griglia prestazioni
			Hashtable h = new Hashtable();
			h.put("referente",pai_figProf.getSelectedValue());
			h.put("PianoAssistenziale","SI");
			Vector<ISASRecord> vDbr;
			if(!pai_figProf.getSelectedValue().isEmpty()){
				vDbr = (Vector<ISASRecord>) invokeGenericSuEJB(new AgendaEJB(), h, "CaricaTabellaPrestazioni");
//			Vector griglia=new Vector();
				if (vDbr!=null){
					if(vDbr.size()<=0){
						UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
					}
//				else griglia=(Vector)o;
				}
			}else{
				vDbr = new Vector<ISASRecord>();
			}
				
			CaribelListModel<ISASRecord> modelTable = new CaribelListModel<ISASRecord>(vDbr);
			modelTable.setMultiple(true);
			Hashtable<String, String> searchParams = new Hashtable<String, String>();
			int i=-1;
			for (Iterator<String> iterator = vettPrestazioni.iterator(); iterator.hasNext();) {
				String codPrest = iterator.next();
				searchParams.put("prest_cod", codPrest.trim());
				i=modelTable.columnsContains(searchParams);
				if(i!=-1){
					modelTable.remove(i);
				}
			}
			modelloPrestazioni = modelTable;
			tablePrestazioni.setModel(modelTable);
			tablePrestazioni.invalidate();tablePrestazioni.getHeight();
//		
//		// REFRESH SULLA LISTA
//		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//		tablePrestazioni.getItems().clear();
//		modelloPrestazioni = new CaribelListModel<ISASRecord>(vDbr);
//		tablePrestazioni.setModel(modelloPrestazioni);
	}
	
	public void onChange$pai_figProf(Event evt) throws Exception{
		doLoadGrid();
		operatore_cod.setValue(null);
		operatore_descr.setValue(null);
		boolean editable = pai_figProf.getSelectedValue()!=null && !pai_figProf.getSelectedValue().isEmpty();
		operatoreCSCtrl.setReadonly(!editable);
		checkBoxNotificaPresaCarico.setDisabled(!editable);
		bottoneOperatore.setDisabled(!editable);
	}
	
	protected Map<String,String> getOtherParametersString() {
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put(pai_figProf.getDb_name(), pai_figProf.getValue());
		ret.put("cod_fig_prof", pai_figProf.getSelectedValue());
		ret.put(pai_data_inizio.getDb_name(), pai_data_inizio.getValueForIsas());
		ret.put(pai_data_fine.getDb_name(), pai_data_fine.getValueForIsas());
		ret.put(pai_freq.getDb_name(), pai_freq.getSelectedValue());
		ret.put(pai_prest_qta.getDb_name(), pai_prest_qta.getValue().toString());
		if(operatore_cod != null && !operatore_cod.getValue().isEmpty()){
			ret.put(operatore_cod.getDb_name(), operatore_cod.getValue());
			ret.put(operatore_descr.getDb_name(), operatore_descr.getValue());
		}
		return ret;
	}
	
	public void onConfermaSelezione(Event event) throws Exception{
		int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
		if(num==0){
			Clients.showNotification(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"), "info", tablePrestazioni, "top_center", 2500);
			return;
		}
		if(doValidateForm()){
			HashMap<String, Object> par = new HashMap<String, Object>();
			par.putAll(getOtherParametersString());
			Vector<Hashtable<String, Object>> prestazioni = new Vector<Hashtable<String, Object>>();
			for (Iterator<Listitem> iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
				Listitem type = iterator.next();
				Hashtable<String, Object> ht = (Hashtable<String, Object>) type.getAttribute("ht_from_grid");
				ht.putAll(getOtherParametersString());
				String pattern = CostantiAgenda.getStringPianificazione(getDataFromGrid());
				ht.put(CostantiSinssntW.PIANIFICAZIONE_PAI, pattern);
				prestazioni.add(ht);
			}
			par.put("prestazioni", prestazioni);
			
			((PrestazioniPAICaribelGridFormCtrl) self.getParent().getAttribute(MY_CTRL_KEY)).caricaPrestazioni(par);
			tablePrestazioni.clearSelection();
		}
		doFreezeForm();
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return UtilForComponents.testRequiredFieldsNoCariException(self);
	}
	
	@Override
	public void onClick$btn_save() {
	}
	
	@Override
	public void onClick$btn_delete() {
	}
	
	@Override
	public void onClick$btn_print() {
	}
	
	@Override
	public void onClick$btn_search() {
	}
	
	@Override
	public void onClick$btn_undo() {
	}
}

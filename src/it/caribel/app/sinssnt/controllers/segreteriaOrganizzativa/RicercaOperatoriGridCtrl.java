package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.AgendaModOperatoreEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.Listitem;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class RicercaOperatoriGridCtrl extends PAIPianSettFormCtrl {

	private static final long serialVersionUID = 1L;

	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/ricercaOperatoriPerCarico.zul";

	private CaribelDatebox data_inizio;
	public static String myKeyPermission = "PIANOACC";
	private PianoAssistEJB myEJB = new PianoAssistEJB();

	private CaribelListbox caribellbOperatori;
//	private Button btn_confermaSelezione;

//	private CaribelListModel<ISASRecord> modelloOperatori = new CaribelListModel<ISASRecord>();

	private CaribelSearchCtrl caribelSearchCtrl;
	private CaribelListheader num_prelievi;
	@Override
	public void doInitForm() {
		try {
			super.doInitForm();
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			
			CaribelDatebox start;
			start= ((CaribelDatebox)((Component)self.getParent().getParent().getSpaceOwner()).getFellowIfAny("data_inizio", true));
			Date startDate;
			if(start !=null && start.getValue()!=null){
				startDate = start.getValue();
				//setto la data inizio con la data inizio piano
				if(startDate!=null){
					data_inizio.setValue(startDate);
				}
//				String startDate = UtilForComponents.formatDateforDatebox(start);
				CaribelDatebox o = (CaribelDatebox) ((Component)self.getParent().getParent().getSpaceOwner()).getFellowIfAny("data_presa_carico_skso", true); 
				if(o!=null && o.getValue()!=null){//se è stata presa in carico la scheda SO inizio da quella data 
					data_inizio.setValue(o.getValue());
				}
			}
			start = ((CaribelDatebox)((Component)self.getParent()).getFellowIfAny("pai_data_inizio", true));
			if(start !=null && start.getValue()!=null){//se è definita la data per la prestazione prendo quella
				startDate = start.getValue();
				data_inizio.setValue(startDate);
			}
			if(data_inizio.getValue()==null){
				data_inizio.setValue(new Date());
			}
			this.caribelSearchCtrl = (CaribelSearchCtrl)arg.get("caribelSearchCtrl");			
			this.ricercaOperatori();
			boolean prelieviVisible = arg.get("tipo_operatore")!=null&&arg.get("tipo_operatore").equals(GestTpOp.CTS_COD_INFERMIERE);
			num_prelievi.setVisible(prelieviVisible);
			doFreezeForm();
		} catch (Exception e) {
			doShowException(e);
		}
	}
	
	protected Map<String,String> getOtherParametersString() {
		Hashtable<String, String> ret = new Hashtable<String, String>();
//		ret.put(pai_figProf.getDb_name(), pai_figProf.getValue());
//		ret.put("cod_fig_prof", pai_figProf.getSelectedValue());
//		ret.put(pai_data_inizio.getDb_name(), pai_data_inizio.getValueForIsas());
//		ret.put(pai_data_fine.getDb_name(), pai_data_fine.getValueForIsas());
//		ret.put(pai_freq.getDb_name(), pai_freq.getSelectedValue());
//		ret.put(pai_prest_qta.getDb_name(), pai_prest_qta.getValue().toString());
//		if(operatore_cod != null && !operatore_cod.getValue().isEmpty()){
//			ret.put(operatore_cod.getDb_name(), operatore_cod.getValue());
//			ret.put(operatore_descr.getDb_name(), operatore_descr.getValue());
//		}
		return ret;
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return UtilForComponents.testRequiredFieldsNoCariException(self);
	}

	public void onPrec(ForwardEvent event) throws Exception{
		precSett();
	}
	
	public void onSucc(ForwardEvent event) throws Exception{
		succSett();
	}
	
	protected void succSett() throws Exception {
		logger.trace("giornata Succesiva");
		data_inizio.setValue(DateUtils.addDays(data_inizio.getValue(), 1));
	    this.ricercaOperatori();
	}

	protected void precSett() throws Exception {
		logger.trace("giornata Precedente");
		data_inizio.setValue(DateUtils.addDays(data_inizio.getValue(), -1));
	    this.ricercaOperatori();
	}
	
	private void ricercaOperatori() throws Exception {
		Vector giorni = new Vector(7);
		Hashtable par = new Hashtable();
		String stDistretto=distretto.getSelectedValue(); //jPanelAgendaUbicazione.getComboDistretti();
		if(stDistretto.trim().equals("TUTTO"))
			stDistretto="";
		String stZona=zona.getSelectedValue(); //jPanelAgendaUbicazione.getComboZone();
		if(stZona.trim().equals("TUTTO"))
			stZona="";
		String stPresidio=presidio_comune_area.getSelectedValue(); //jPanelAgendaUbicazione.getComboTerzoLivello();
		if(stPresidio.trim().equals("TUTTI"))
			stPresidio="";
		for (int i = 0; i < 7; i++) {
			giorni.add(data_inizio.getValueForIsas());
		}
		par.put("distretto",stDistretto);
		par.put("zona",stZona);
		par.put("presidio",stPresidio);
		par.put("tipo_operatore", (String) arg.get("tipo_operatore"));
		par.put("giorni", giorni);
		CaribelListModel clm = new CaribelListModel((new AgendaModOperatoreEJB()).CaricaTabellaOperatoriNew(getProfile().getMyLogin(), par));
		caribellbOperatori.setModel(clm);
		caribellbOperatori.invalidate();
	}
	
	public void onDoubleClickedItem(Event event) throws Exception {
		try{
			Listitem item = this.caribellbOperatori.getSelectedItem();
			ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");
			if(this.caribelSearchCtrl !=null){
				Hashtable<String, Object> h = new Hashtable<String, Object>();
				h.put("dataInizioPrestazione", data_inizio.getValueForIsas());
				this.caribelSearchCtrl.updateFromGrid(dbrFromGrid, h);
				self.detach();
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void onChange$data_inizio(Event event) throws Exception {
		 this.ricercaOperatori();
	}
	
	public void onChange$presidio_comune_area(Event event) throws Exception {
		 this.ricercaOperatori();
	}
	
	@Override
	protected boolean doSaveForm() throws Exception {
		return false;
	}
}

package it.caribel.app.sinssnt.controllers.palliat_terapia;

import it.caribel.app.sinssnt.bean.nuovi.SkMPalTerapieNewEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfileBase;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.app.sinssnt.util.UtilForContainerGen;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelBandbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.generic_controllers.CaribelTimeTablesCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;
import org.zkoss.zul.Window.Mode;

public class TerapiaGridCRUDCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	public static String myKeyPermission = "";
	private SkMPalTerapieNewEJB myEJB = null;	
	public static final String myPathFormZul ="/web/ui/sinssnt/contatto_palliativista/terapiaForm.zul";	
	
	public static final String ARGS_ID_MY_WINDOW 		= "ARGS_ID_MY_WINDOW";
	public static final String ARGS_NOME_TABELLA		= "NOME_TABELLA";
	public static final String ARGS_STRING_N_CARTELLA 	= "STRING_N_CARTELLA";
	
	public static final String CADENZA_CICL = "cicl";
	public static final String CADENZA_OCCO = "occo";
	public static final String CADENZA_MENS = "mens";
	public static final String CADENZA_SETT = "sett";
	public static final String CADENZA_GIOR = "gior";

	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	
	private CaribelCombobox cbx_frequenza;

	String nCartella = "";
	String nContatto = "";
	
	private CaribelRadiogroup statoTerapiaFilter;
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	private CaribelIntbox frequenza_gg;
	private CaribelTextbox note;
	private CaribelBandbox my_bandbox;
	private CaribelIntbox somministrazione;
	private CaribelDatebox dataInizio;
	private AbstractComponent operatoreSearch;
	private CaribelTextbox codOperatore;
	private CaribelCombobox codOperatoreDesc;

	private static final String ver = "9-";

	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		try 
		{
			myEJB = new SkMPalTerapieNewEJB();

			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			//doPopulateCombobox();
			if (arg.get(ARGS_STRING_N_CARTELLA) == null) {
				nCartella = UtilForContainerGen.getCartellaCorr();
				if(nCartella == null || nCartella.trim().equals(""))
					throw new Exception("Reperimento codice Assistito non riuscito!");
			}else {
				nCartella = ISASUtil.getValoreStringa(arg, ARGS_STRING_N_CARTELLA);
				//cv
				n_cartella.setValue(Integer.parseInt(nCartella));
			}

			//cv
			String s = UtilForContainer.getNcontatto();
			if (s != null)
			{
				nContatto = s;						
				n_contatto.setValue(Integer.parseInt(nContatto));
			}				

			logger.trace(punto + " nCartella>>"+ nCartella+"<");
			//key_cartella.setText(nCartella);

			//Recupero eventuale ID_MY_WINDOW passato dal container (utile in caso di piu diari nello stesso container)
			if(arg.get(ARGS_ID_MY_WINDOW)!=null && !(""+arg.get(ARGS_ID_MY_WINDOW)).trim().equals("")){
				self.setId((String)arg.get(ARGS_ID_MY_WINDOW));
			}

			String modal = ISASUtil.getValoreStringa(arg, "modale");
			if(ISASUtil.valida(modal)&& modal.equals("SI"))
			{
				logger.trace(punto + " apertura modale ");
				((Window)self).setMode(Mode.MODAL);
				((Window)self).setClosable(true);
			}else {
				logger.trace(punto + " apertura NON modale ");
			}
			
			caricaComboBox();
			appendEvent();
			doLoadGrid();

		} catch (Exception e) {
			doShowException(e);
		}
	}
	
	
	/* --------------------------------------------------------------------------------------- */
	
	private void caricaComboBox() {
		cbx_frequenza.clear();
		CaribelComboRepository.addComboItem(cbx_frequenza, "", "");
		CaribelComboRepository.addComboItem(cbx_frequenza, CADENZA_GIOR, Labels.getLabel("interventi.terapia.farmaci.cadenza."+CADENZA_GIOR));
		CaribelComboRepository.addComboItem(cbx_frequenza, CADENZA_SETT, Labels.getLabel("interventi.terapia.farmaci.cadenza."+CADENZA_SETT));
		CaribelComboRepository.addComboItem(cbx_frequenza, CADENZA_MENS, Labels.getLabel("interventi.terapia.farmaci.cadenza."+CADENZA_MENS));
		CaribelComboRepository.addComboItem(cbx_frequenza, CADENZA_OCCO, Labels.getLabel("interventi.terapia.farmaci.cadenza."+CADENZA_OCCO));
		CaribelComboRepository.addComboItem(cbx_frequenza, CADENZA_CICL, Labels.getLabel("interventi.terapia.farmaci.cadenza."+CADENZA_CICL));
	}
	
	/* ---------------------------------------------------------------------------------------- */
	
	public void onLoadGrid(){
		try{
			doLoadGrid();
		}catch (Exception e){
			doShowException(e);
		}
	}
	
	/* ---------------------------------------------------------------------------------------- */
	
	private void appendEvent() {
		my_bandbox.addEventListener(CaribelTimeTablesCtrl.ON_UPDATE_TIME_TABLES, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				String [] times = my_bandbox.getText().split(CaribelTimeTablesCtrl.sep);
				somministrazione.setRawValue(times.length);
			}});
		
		cbx_frequenza.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				impostaCadenza();
				disabilitaCadenza();
				}});
		
		dadata.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doLoadGrid();
				}});
		
		adata.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doLoadGrid();
				}});	
	}
	
	/* -------------------------------------------------------------------------------------- */
	
	private void disabilitaCadenza(){
		try{
			note.setRequired(false);
			String valore = cbx_frequenza.getSelectedValue();
			if(ISASUtil.valida(valore)){
				if(valore.equals(CADENZA_GIOR)){
					frequenza_gg.setReadonly(true);
				}else if(valore.equals(CADENZA_SETT)){
					frequenza_gg.setReadonly(true);
				}else if(valore.equals(CADENZA_MENS)){
					frequenza_gg.setReadonly(true);
				}else if(valore.equals(CADENZA_OCCO)){
					if(this.isEditable()){
						frequenza_gg.setReadonly(false);
						note.setRequired(true);
					}
				}else if(valore.equals(CADENZA_CICL)){
					if(this.isEditable())
						frequenza_gg.setReadonly(false);
				}
			}else{
				frequenza_gg.setReadonly(true);
			}
		}catch (Exception e){
			doShowException(e);
		}
	}
	
	/* -------------------------------------------------------------------------------------- */
	
	private void impostaCadenza(){
		try{
			String valore = cbx_frequenza.getSelectedValue();
			if(ISASUtil.valida(valore)){
				if(valore.equals(CADENZA_GIOR)){
					frequenza_gg.setValue(1);
				}else if(valore.equals(CADENZA_SETT)){
					frequenza_gg.setValue(7);
				}else if(valore.equals(CADENZA_MENS)){
					frequenza_gg.setValue(30);
				}else if(valore.equals(CADENZA_OCCO)){
					frequenza_gg.setValue(1);
				}else if(valore.equals(CADENZA_CICL)){
					frequenza_gg.setRawValue(null);
				}
			}else{
				frequenza_gg.setRawValue(null);
			}
		}catch (Exception e){
			doShowException(e);
		}
	}
	
	/* -------------------------------------------------------------------------------------- */
	
	@Override
	protected void afterSetStatoInsert() {
		String punto = ver + "afterSetStatoInsert ";
		impostaDataOra();
		impostaInfoOperatore();
		disabilitaInfoOperatore();
	}
	
	private void impostaDataOra() {
		if (!ManagerDate.validaData(dataInizio)) {
			dataInizio.setValue(procdate.getDate());
		}
	}

	private void impostaInfoOperatore() {
		String op_sess = getProfile().getStringFromProfile(ManagerProfileBase.CODICE_OPERATORE);
		String cognomeNomeOperatore = getProfile().getStringFromProfile(ManagerProfileBase.COGNOME_OPERATORE);
		codOperatore.setValue(op_sess);
		codOperatoreDesc.setSelectedValue(cognomeNomeOperatore);
	}

	private void disabilitaInfoOperatore() {
		CaribelSearchCtrl operatore = (CaribelSearchCtrl) operatoreSearch.getAttribute(MY_CTRL_KEY);
		operatore.setReadonly(true);
		codOperatoreDesc.setDisabled(true);
	}
	
	/* ---------------------------------------------------------------------------------------- */

	@Override
	public void onClick$btn_formgrid_new() {
		try{
			this.clb.setSelectedIndex(-1);
			UtilForBinding.resetForm(myForm,this.parkSetting);
			this.setStato(STATO_INSERT);
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Override
	protected void notEditable() {
		disabilitaInfoOperatore();
		disabilitaCadenza();
		my_bandbox.setReadonly(true);
	}

	@Override
	protected void afterSetStatoUpdate() {
		super.afterSetStatoUpdate();
	}

	@Override
	protected void afterSetStatoWait() {
		String punto = ver + "afterSetStatoWait ";
		super.afterSetStatoWait();
		logger.trace(punto + " afterStatoWait ");
	}


	protected void doLoadGrid() throws Exception {
		hParameters.putAll(getOtherParametersString());
		// REFRESH SULLA LISTA
		//Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
		
		myLogin ml = CaribelSessionManager.getInstance().getMyLogin();
		Vector<ISASRecord> vDbr = myEJB.query(ml, hParameters);
		clb.getItems().clear();
		clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
	}

	@Override
	protected boolean doValidateForm() 
	{
		String punto = ver + "doValidateForm ";
		logger.debug(punto + "inzio ");
		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		dati.put("n_cartella", n_cartella.getValue()+"");
		//cv
		dati.put("n_contatto", n_contatto.getValue());
		
		return true;
	}

	@Override
	protected Map<String,String> getOtherParametersString() 
	{
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put("n_cartella", nCartella);
		ret.put("n_contatto", nContatto);
		//ret.put("id_terapia", id_terapia.getValue().toString());
		
		ret.put("stato_terapia", statoTerapiaFilter.getSelectedValue());
		ret.put("da_data", dadata.getValueForIsas());
		ret.put("a_data", adata.getValueForIsas());
		
		return ret;
		
	}

	

}	
		
		

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

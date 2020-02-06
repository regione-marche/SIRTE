package it.caribel.app.sinssnt.controllers.relazione_clinica_palliat;

import it.caribel.app.common.controllers.report.ReportLauncherJasper;
import it.caribel.app.sinssnt.bean.modificati.SkMPalClinicaEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMDiarioEJB;
import it.caribel.app.sinssnt.bean.nuovi.SkMPalClinicaNewEJB;
import it.caribel.app.sinssnt.controllers.ContainerFisioterapicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerGenericoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerInfermieristicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.report.jasper_report.JRDataSourceOfIsasRecords;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.generic_controllers.interfaces.AskDataInput;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

public class RelazioneClinicaGridCtrl extends CaribelGridCRUDCtrl implements AskDataInput {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = ChiaviISASSinssntWeb.RMDIARIO;
	private SkMPalClinicaNewEJB myEJB = new SkMPalClinicaNewEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/relazione_clinica_palliat/relClinicaGrid.zul";
	public static final String myPathFormZulStorico = "/web/ui/sinssnt/relazione_clinica_palliat/relClinicaGridStorico.zul";
	
	private CaribelDatebox data_relcli;
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	private CaribelTextbox op_inse;
	private CaribelCombobox cbx_tipo_operatore;
	
	private Vector<ISASRecord> vDbr;
	
	public Button btn_storico;
	
	//Chiavi
	String n_cartella 		= "";
	String n_contatto 		= "";
	String progr_inse 		= "";
	String progr_modi 		= "";
	String tipo_operatore 	= "";
	
	String id_skso          = "";
	
	Object data_apertura_contatto;
	private AbstractComponent operatoreCS;
	
	private boolean fromBtnPrintAll = false;

	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			
			populateCombobox();
			
			CaribelSearchCtrl operatoreCSearch = (CaribelSearchCtrl) operatoreCS.getAttribute(MY_CTRL_KEY);
			operatoreCSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, cbx_tipo_operatore);
			operatoreCSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, ManagerProfile.getZonaOperatore(getProfile()));
			operatoreCSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, ManagerProfile.getDistrettoOperatore(getProfile()));
			
			CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();

			if(containerCorr instanceof ContainerPuacCtrl){
				//La segreteria organizzativa deve visualizzare il diario in sola lettura
				setDiarioReadOnly(self);
			}
			
			//Recupero la data di apertura del contatto corrente, per poi verificare che il diario
			//non venga inserito con data antecedente a ques'ultima
			if(containerCorr instanceof ContainerMedicoCtrl){
				this.data_apertura_contatto = (Object)((containerCorr).hashChiaveValore).get("skm_data_apertura");
			}else if(containerCorr instanceof ContainerGenericoCtrl){
				this.data_apertura_contatto = (Object)((containerCorr).hashChiaveValore).get("skfpg_data_apertura");
			}else if(containerCorr instanceof ContainerInfermieristicoCtrl){
				this.data_apertura_contatto = (Object)((containerCorr).hashChiaveValore).get(CostantiSinssntW.SKI_DATA_APERTURA);
			}else if(containerCorr instanceof ContainerFisioterapicoCtrl){
				this.data_apertura_contatto = (Object)((containerCorr).hashChiaveValore).get(CostantiSinssntW.SKF_DATA);
			}else if(containerCorr instanceof ContainerPuacCtrl){
				this.data_apertura_contatto = (Object)((containerCorr).hashChiaveValore).get(CostantiSinssntW.CTS_PR_DATA_PUAC);
			}
			

			if(arg.get(CostantiSinssntW.N_CARTELLA)!=null && !(""+arg.get(CostantiSinssntW.N_CARTELLA)).trim().equals("")){
				n_cartella = (String)arg.get(CostantiSinssntW.N_CARTELLA);
			}else{
				n_cartella = ISASUtil.getValoreStringa((containerCorr).hashChiaveValore,CostantiSinssntW.N_CARTELLA);
			}
			if(arg.get(CostantiSinssntW.N_CONTATTO)!=null && !(""+arg.get(CostantiSinssntW.N_CONTATTO)).trim().equals("")){
				n_contatto = (String)arg.get(CostantiSinssntW.N_CONTATTO);
			}else{
				n_contatto = ISASUtil.getValoreStringa((containerCorr).hashChiaveValore,CostantiSinssntW.N_CONTATTO);
			}
			if(arg.get(CostantiSinssntW.CTS_ID_SKSO)!=null && !(""+arg.get(CostantiSinssntW.CTS_ID_SKSO)).trim().equals("")){
				id_skso = (String)arg.get(CostantiSinssntW.CTS_ID_SKSO);
			}else{
				id_skso = ISASUtil.getValoreStringa((containerCorr).hashChiaveValore,CostantiSinssntW.CTS_ID_SKSO);
			}
			if(arg.get("tipo_operatore")!=null && !(""+arg.get("tipo_operatore")).trim().equals("")){
				tipo_operatore = (String)arg.get("tipo_operatore");
			}else{
				tipo_operatore = UtilForContainer.getTipoOperatorerContainer();
			}
			
			if(arg.get("progr_inse")!=null && !(""+arg.get("progr_inse")).trim().equals("")){
				//si sta aprendo lo storico delle revisioni
				progr_inse = (String)arg.get("progr_inse");
//			}else{
//				op_inse.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
//				op_inse_descr.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
			}
			
//			cbx_tipo_operatore.setSelectedValue(tipo_operatore);
			
			doLoadGrid();

		}catch (Exception e){
			doShowException(e);
		}
	}
	
	public void onClick$btn_refresh() {
		try{
			doLoadGrid();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void onClick$btn_storico() {
		try{
			String dbr_n_cartella 		= ""+currentIsasRecord.get(CostantiSinssntW.N_CARTELLA);
			String dbr_n_contatto 		= ""+currentIsasRecord.get(CostantiSinssntW.N_CONTATTO);
			String dbr_progr_inse 		= ""+currentIsasRecord.get("progr_inse");
			String dbr_tipo_operatore 	= ""+currentIsasRecord.get("tipo_operatore");
			if(!dbr_progr_inse.equals("null") && !dbr_progr_inse.trim().equals("")){
				Hashtable<String, String> map = new Hashtable<String, String>();
				map.put(CostantiSinssntW.N_CARTELLA, 	dbr_n_cartella);
				map.put(CostantiSinssntW.N_CONTATTO, 	dbr_n_contatto);
				map.put("progr_inse", 			dbr_progr_inse);
				map.put("tipo_operatore", 		dbr_tipo_operatore);
				map.put("progr_inse",			dbr_progr_inse);
				Component comp = Executions.createComponents(myPathFormZulStorico,Path.getComponent("/main"),map);
				((Window)comp).doModal();
				((Window)comp).setTitle(Labels.getLabel("diario.dati_storici.titolo"));
				((Window)comp).getFellow("btn_storico").setVisible(false);
				setDiarioReadOnly(comp);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Override
	protected boolean esisteRecord(Hashtable<String,String> h)throws Exception{
		return false;
	}
	
	public void setDiarioReadOnly(Component comp){
		((Window)comp).getFellow("menu_horiz").setVisible(false);
		UtilForBinding.setComponentReadOnly(((Window)comp).getFellow("myForm"), true);
	}
	
	@Override
	protected Hashtable<String, String> getOtherParametersString() {
		Hashtable<String, String> h = new Hashtable<String, String>();
		h.put(CostantiSinssntW.N_CARTELLA, n_cartella);
		h.put(CostantiSinssntW.N_CONTATTO, n_contatto);
		h.put("tipo_operatore", tipo_operatore);
		h.put(CostantiSinssntW.CTS_ID_SKSO, id_skso);
		return h;
	}
	
	protected void executeOpen()throws Exception{
		btn_storico.setDisabled(false);
		super.executeOpen();
		
		//Verifico che sia il mio op_inse
		boolean isMyDiario = false;
		String op_inse = (String)this.currentIsasRecord.get("op_inse");
		String op_sess = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
		if(op_inse!=null && op_inse.equals(op_sess))
			isMyDiario = true;
		
		//Verifico che sia il mio tipo_operatore
		boolean isMyTipoOperatore = false;
		String tipo_operatore = (String)this.currentIsasRecord.get("tipo_operatore");
		String tipo_operatore_corr = UtilForContainer.getTipoOperatorerContainer();
		if(tipo_operatore!=null && tipo_operatore.equals(tipo_operatore_corr))
			isMyTipoOperatore = true;
		
		super.btn_formgrid_edit.setVisible(isMyDiario && isMyTipoOperatore && isEditable());
		super.btn_formgrid_delete.setVisible(isMyDiario && isDeletable());
	}
	
	protected void notEditable() {
		if(super.stato_corr.equals(STATO_UPDATE)){
			data_relcli.setReadonly(true);
		}else{
			data_relcli.setReadonly(false);
		}
	}
	
	protected void afterSetStatoInsert() {
		data_relcli.setToDay();
	}
	
	@Override
	protected void doLoadGrid() throws Exception {
		hParameters.clear();
		hParameters.put(dadata.getDb_name(), dadata.getValueForIsas());
		hParameters.put(adata.getDb_name(), adata.getValueForIsas());
		hParameters.put(op_inse.getDb_name(), op_inse.getText());
		hParameters.put("progr_inse",progr_inse);
		hParameters.putAll(getOtherParametersString());
		//in fase di ricerca sovrascrivo il tipo operatore settato da getOtherParametersString
		hParameters.put("tipo_operatore", cbx_tipo_operatore.getSelectedValue());
		this.vDbr = querySuEJB(this.currentBean, this.hParameters);
		clb.getItems().clear();
		clb.setModel(new CaribelListModel<ISASRecord>(this.vDbr));
	}

	@Override
	protected boolean doValidateForm() {
		try{
			if(this.data_apertura_contatto !=null){
				Date dtAperturaCont = null;
				if(this.data_apertura_contatto instanceof String)
					dtAperturaCont = UtilForBinding.getDateFromIsas(this.data_apertura_contatto.toString());
				else
					dtAperturaCont = (Date)this.data_apertura_contatto;
				if(data_relcli.getValue().before(dtAperturaCont)){
					UtilForUI.standardExclamation(Labels.getLabel("diario.msg.data_non_valida"));
					return false;
				}
			}
			return true;

		}catch (Exception e){
			doShowException(e);
			return false;
		}
	}
	
	private void populateCombobox()throws Exception{	
		ManagerOperatore.loadTipiOperatori(cbx_tipo_operatore, CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE,true);
	}
	
	@Override
	public void onDoubleClickedItem(Event event) throws Exception { 
		logger.debug("Doppio click su lista diario, non devo fare nulla, basta executeOpen()");
	}
	
	@Override
	protected void doStampaRigaSel()throws Exception{
		try{
			fromBtnPrintAll=false;
			ReportLauncherJasper.chiediTipo(this,self);
		}catch (Exception e){
			doShowException(e);
		}
	}
	
	public void onClick$btn_print_all() {
		try{
			fromBtnPrintAll=true;
			ReportLauncherJasper.chiediTipo(this,self);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Override
	public void setDataInput(String data, String methodName) throws Exception{
		Component pancombo = self.getFellowIfAny("pannelloCombo");
		if(data==null || data.trim().equals("")){
			pancombo.detach();
			ReportLauncherJasper.chiediTipo(this,self);
			return;
		}
		pancombo.detach();
		if(fromBtnPrintAll)
			doPrintAll(data);
		else
			doPrintOne(data);
	}

	private void doPrintOne(String tipoFile) throws Exception  {
		Listitem item = clb.getSelectedItem();
		Hashtable<String, ?> htFromGrid = (Hashtable<String, ?>) item.getAttribute("ht_from_grid");
		hParameters = (Hashtable<String, Object>) UtilForBinding.getHashtableForEJBFromHashtable(htFromGrid);
		this.currentIsasRecord = queryKeySuEJB(this.currentBean,hParameters);
		
		Vector<ISASRecord> vDbr2 = new Vector<ISASRecord>();
		vDbr2.add(queryKeySuEJB(this.currentBean,hParameters));
		
		avviaStampa(vDbr2,tipoFile);
	}

	private void doPrintAll(String tipoFile) throws Exception  {
		Vector<ISASRecord> vDbr2 = new Vector<ISASRecord>();
		if(this.vDbr!=null && this.vDbr.size()>0){
			for(int i=0;i<this.vDbr.size();i++){
				hParameters = (Hashtable<String, Object>) UtilForBinding.getHashtableForEJBFromHashtable(this.vDbr.get(i).getHashtable());
				vDbr2.add(queryKeySuEJB(this.currentBean,hParameters));
			}	
			avviaStampa(vDbr2,tipoFile);
		}
	}
	
	private void avviaStampa(Vector<ISASRecord> vDbr,String tipoFile)throws Exception{
		JRDataSourceOfIsasRecords myDS = new JRDataSourceOfIsasRecords();
		myDS.setvDbr(vDbr);
		
		Hashtable<String, String> h = new Hashtable<String, String>();
		String assistito = UtilForContainer.getCognomeNomeAssistito();
		h.put("titolo", Labels.getLabel("diario.print.title")+" "+assistito);

		ReportLauncherJasper.launchReport("ReportDiari.jasper", tipoFile,myDS,h);
	}
	
	
}

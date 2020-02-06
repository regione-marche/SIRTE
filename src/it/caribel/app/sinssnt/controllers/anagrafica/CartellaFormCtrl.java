package it.caribel.app.sinssnt.controllers.anagrafica;

import it.caribel.app.common.ejb.Anagra_cEJB;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.common.ejb.RegionEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.common.ejb.TabuslEJB;
import it.caribel.app.sinssnt.controllers.tabelle.comuni.ComuniSearchCtrl;
import it.caribel.app.sinssnt.util.ChiaviIsasBase;
import it.caribel.app.sinssnt.util.ComboModelRepository;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForDevelopment;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.swing2.util.CodiceFiscale;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Window;


public class CartellaFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = 1L;

	protected Window cartellaForm;
	
	private CaribelIntbox n_cartella;

	public static String myKeyPermission = ChiaviIsasBase.CARTELLA;
    
	private CartellaEJB myEJB = new  CartellaEJB();
	private Anagra_cEJB myEJB2 = new Anagra_cEJB();
	
	private CaribelCombobox cbx_motivoChiusura;
	private CaribelCombobox cbx_religione;
	private CaribelCombobox cbx_localizzazione;

	private Vector<Hashtable<String, String>> vt;

	private CaribelTextbox cod_operatore;
	private CaribelTextbox desc_operat;
	private CaribelDatebox data_apertura;
	private CaribelDatebox data_chiusura;
	private Button btn_anagra_sto;
	private CaribelTextbox cod_regionale;
	
	private CaribelDatebox data_variazione;

	private Component anagra_c;
	private Component panel_area;
	
	private Tabbox panneli_anagrac;
	private Tab tab_domicilio;
	private Tab tab_residenza;
	private Tab tab_reperibilita;
	private Tab tab_stranieri;
	private Tab tab_esenzioni;
	private Tab tab_team;
	private Tabpanel panel_domicilio;
	private Tabpanel panel_residenza;
	private Tabpanel panel_reperibilita;
	private Tabpanel panel_stranieri;
	private Tabpanel panel_esenzioni;
	private Tabpanel panel_team;

	private CaribelCombobox cbx_regione;

	private CaribelCombobox cbx_usl;
	private CaribelCombobox cbx_documento;

	private CaribelListbox clbEsenzioni;

	//campi reperibilita
	private CaribelTextbox indirizzo_rep   ;
	private CaribelTextbox nome_camp       ;
	private CaribelTextbox comune_rep      ;
	private CaribelCombobox comreperibdescr;
	private CaribelTextbox rep_cap         ;
	private CaribelTextbox provincia_rep   ;
	private CaribelTextbox telefono1_rep   ;
	private CaribelTextbox telefono2_rep   ;
	private CaribelTextbox areadis_rep     ;
	private CaribelCombobox desc_com_rep   ;
	//campi domicilio
	private CaribelTextbox indirizzo_dom    ;
	private CaribelTextbox dom_citta        ;
	private CaribelCombobox comdomdescr     ;
	private CaribelTextbox provincia_dom    ;
	private CaribelTextbox telefono1_dom    ;
	private CaribelTextbox telefono2_dom    ;
	private CaribelTextbox cod_areadis_dom  ;
	private CaribelCombobox desc_areadis_dom;
	//campi residenza
	protected CaribelTextbox indirizzo_res;
	private CaribelTextbox cap_res;
	protected CaribelTextbox cod_citta_res;
	private CaribelCombobox comresdescr;
	private CaribelTextbox provincia_res;
	protected CaribelTextbox cod_areadis_res;
	protected CaribelCombobox desc_areadis_res;

	private CaribelDatebox data_nasc;
	private AbstractComponent comuneNascitaSearch;
	private AbstractComponent comuneDomicilioSearch;
	private AbstractComponent comuneResidenzaSearch;
	private AbstractComponent comuneRepSearch;	

	private CaribelTextbox cod_fiscale;
	private CaribelTextbox cognome;
	private CaribelTextbox nome;
	private CaribelTextbox cod_com_nasc;
	private CaribelTextbox codComXCF = new CaribelTextbox();

	private CaribelRadiogroup sesso;

	private Button prova;
	private Label prova_placeholder;

	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			visualizzaBtnPrintQRCode();
			doPopulateCombobox();
			
			//definisco le dipendenze dalle caribelSearch
			CaribelSearchCtrl comuneNascita = (CaribelSearchCtrl) comuneNascitaSearch.getAttribute(MY_CTRL_KEY);
			comuneNascita.putLinkedSearchObjects("data_nasc", data_nasc);
			comuneNascita.putLinkedComponent("cod_fis", codComXCF);
			CaribelSearchCtrl comuneDomicilio = (CaribelSearchCtrl) comuneDomicilioSearch.getAttribute(MY_CTRL_KEY);
			comuneDomicilio.putLinkedComponent("cod_pro", provincia_dom);
			CaribelSearchCtrl comuneResidenza = (CaribelSearchCtrl) comuneResidenzaSearch.getAttribute(MY_CTRL_KEY);
			comuneResidenza.putLinkedComponent("cod_pro", provincia_res);
			comuneResidenza.putLinkedComponent("cod_cap", cap_res);
			comuneResidenza.putLinkedComponent("cod_reg", cbx_regione);
			comuneResidenza.putLinkedComponent("cod_usl", cbx_usl);
			CaribelSearchCtrl comuneReperibilita = (CaribelSearchCtrl) comuneRepSearch.getAttribute(MY_CTRL_KEY);
			comuneReperibilita.putLinkedComponent("cod_pro", provincia_rep);
			comuneReperibilita.putLinkedComponent("cod_cap", rep_cap);
						
			cod_citta_res.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if(event.getData()!=null && event.getData() instanceof ComuniSearchCtrl){
						if(cod_citta_res.getRawText()==null || cod_citta_res.getRawText().equals("")){
							//Ripulisco il campo area
							cod_areadis_res.setRawValue("");
							desc_areadis_res.setRawValue("");
						}
						CaribelSearchCtrl comuneResidenza = (ComuniSearchCtrl)event.getData();
						if(comuneResidenza!=null && comuneResidenza.getCurrentRecord()!=null){
							String cod_reg = (String)comuneResidenza.getCurrentRecord().get("cod_reg");
							cbx_regione.setSelectedValue(cod_reg);
							loadComboBoxUsl(cbx_usl,cbx_regione,false);
						}
					}
				}});
			dom_citta.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if(event.getData()!=null && event.getData() instanceof ComuniSearchCtrl){
						if(dom_citta.getRawText()==null || dom_citta.getRawText().equals("")){
							//Ripulisco il campo area
							cod_areadis_dom.setRawValue("");
							desc_areadis_dom.setRawValue("");
						}
					}
				}});
			comune_rep.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if(event.getData()!=null && event.getData() instanceof ComuniSearchCtrl){
						if(comune_rep.getRawText()==null || comune_rep.getRawText().equals("")){
							//Ripulisco il campo area
							areadis_rep.setRawValue("");
							desc_com_rep.setRawValue("");
						}
					}
				}});
			
			if(dbrFromList!=null && dbrFromList.get("n_cartella")!=null) {
				hParameters.put("n_cartella", ((Integer)dbrFromList.get("n_cartella")).toString());
				hParameters.put("cod_usl", ((String)dbrFromList.get("cod_usl")).toString());
				
				doQueryKeySuEJB();
				doSelectLastValueSuEJB2();
				doPopulateEsenzioni();
				doWriteBeanToComponents();
				n_cartella.setReadonly(true);
			}else if(arg.get("n_cartella")!=null){
				hParameters.put("n_cartella", (arg.get("n_cartella")).toString());
				doQueryKeySuEJB();
				hParameters.put("cod_usl", (String)currentIsasRecord.get("cod_usl"));
				doSelectLastValueSuEJB2();
				doPopulateEsenzioni();
				doWriteBeanToComponents();
				n_cartella.setReadonly(true);
			}
			else{
				n_cartella.setReadonly(false);
			}
			//Carico le combo che dipendono da altre
			loadComboBoxUsl(cbx_usl,cbx_regione,false);
			 
			if(this.isInUpdate())
				notEditable();
			
			//Se sono in insert seleziono il pannelo della residenza
			if(this.isInInsert()){
				panneli_anagrac.setSelectedPanel(panel_residenza);
				cod_operatore.setText(getProfile().getStringFromProfile("codice_operatore"));
				desc_operat.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
				data_variazione.setValue(new Date());//(procdate.getDate()); la SINSSNTGprsServlet non gira sotto Liferay
				data_apertura.setValue(new Date());//(procdate.getDate()); la SINSSNTGprsServlet non gira sotto Liferay
			}
			if (UtilForDevelopment.isDevelopMode())
			{
				prova.setVisible(true);
				prova_placeholder.setVisible(false);
			}
			doFreezeForm();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doPopulateEsenzioni() throws Exception {
//		hParameters.put("cod_usl", value)
		Vector<ISASRecord> esenzioni = (Vector<ISASRecord>)CaribelClass.isasInvoke(myEJB, "query_esenzioni", hParameters);
		if(esenzioni!= null && !esenzioni.isEmpty()){
			clbEsenzioni.setModel(new CaribelListModel<ISASRecord>(esenzioni));
		}
	}
	
	@SuppressWarnings("unchecked")
	private void doSelectLastValueSuEJB2() throws Exception {
		ISASRecord tmp = invokeSuEJB(myEJB2, hParameters, "selectLastValue");
		@SuppressWarnings("rawtypes")
		Hashtable ht = new Hashtable();
		ht.putAll(currentIsasRecord.getHashtable());
		if (tmp!=null){
			ht.putAll(tmp.getHashtable());
		}
		currentIsasRecord.putHashtable(ht);
	}

	public void doPopulateCombobox() throws Exception{
		doPopulateMotivoChiusura();
		doPopulateReligione();
		CaribelComboRepository.populateCombobox(cbx_localizzazione, ComboModelRepository.LOCALIZZAZIONE, false);
		caricaComboBoxReg();
		cbx_regione.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
			public void onEvent(Event event)throws Exception{
				loadComboBoxUsl(cbx_usl,cbx_regione,true);
				}
			});
		initComboBoxdaTabVoci();
	}

	private void initComboBoxdaTabVoci() throws Exception {
	      
			Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
			Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
			
			Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
			h_xAllCB.put("combo_tipostru_V", "S"); // x notificare che serve 1 riga vuota
			h_xAllCB.put("combo_tipostru_C", "S"); // x notificare che serve anche il codReg
			
			h_xCBdaTabBase.put("DOCSOGG", cbx_documento);
			h_xLabdaTabBase.put("DOCSOGG", (Label) cbx_documento.getPreviousSibling());

			CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val", "tab_descrizione", false);
	}
	
	public void doPopulateMotivoChiusura(){
		cbx_motivoChiusura.clear();
		CaribelComboRepository.addComboItem(cbx_motivoChiusura, "0", "");
		CaribelComboRepository.addComboItem(cbx_motivoChiusura, "1", Labels.getLabel("cartellaForm.comboMotivoChiusura.trasferimento"));
		CaribelComboRepository.addComboItem(cbx_motivoChiusura, "2", Labels.getLabel("cartellaForm.comboMotivoChiusura.decesso"));
		cbx_motivoChiusura.setSelectedValue("0");
	}

	public void doPopulateReligione(){
		cbx_religione.clear();
		CaribelComboRepository.addComboItem(cbx_religione, "0", Labels.getLabel("cartellaForm.comboReligione.nonRilevata"));
		CaribelComboRepository.addComboItem(cbx_religione, "1", Labels.getLabel("cartellaForm.comboReligione.ateo"));
		CaribelComboRepository.addComboItem(cbx_religione, "2", Labels.getLabel("cartellaForm.comboReligione.cristiana"));
		CaribelComboRepository.addComboItem(cbx_religione, "3", Labels.getLabel("cartellaForm.comboReligione.musulmana"));
		CaribelComboRepository.addComboItem(cbx_religione, "4", Labels.getLabel("cartellaForm.comboReligione.ebraica"));
		CaribelComboRepository.addComboItem(cbx_religione, "5", Labels.getLabel("cartellaForm.comboReligione.altro"));
		CaribelComboRepository.addComboItem(cbx_religione, "7", Labels.getLabel("cartellaForm.comboReligione.testimoniGeova"));
		cbx_religione.setSelectedValue("0");
	}
	
	// 21/12/04: x caricamento comboBox da tabella REGION
	private void caricaComboBoxReg(){
		try{
//			comboPreLoad.load("combo_region", "query",
//							profile.getParameter("region"),
//							ctcxRegion, this.jCariComboBoxRegion,
//							"cd_reg", "region");
			CaribelComboRepository.comboPreLoad("combo_region", new RegionEJB(), "query", new Hashtable<String, String>(), cbx_regione, null, "cd_reg", "region", false);
			
		}catch(Exception e){
			logger.error("caricamento combo REGION fallito! - Eccez=" + e);
		}
	}	
	
	private void loadComboBoxUsl(CaribelCombobox cbx, CaribelCombobox cbxDip, boolean isReload)throws Exception {			
		String str_reg_val = (String)cbxDip.getSelectedValue();
		String str_usl_val ="";
		try{
			str_usl_val = (String)cbx.getSelectedValue();
		}catch(WrongValueException ex){}
		
		if(isReload)
			cbx.clear();
		if(str_reg_val !=null && !str_reg_val.equals("")&& !str_reg_val.equals("TUTTO")){
			Hashtable<String, String> h = new Hashtable<String, String>();
			h.put("cd_reg", str_reg_val);
			CaribelComboRepository.comboPreLoad("combo_usl_per_reg_"+str_reg_val, new TabuslEJB(), "queryCombo", h, cbx_usl, null, "codice_usl", "desusl", false);
		}
		if(!isReload)
			cbx.setSelectedValue(str_usl_val);
	}
	
	
	private String getCalcoloCodiceFiscale()	{
		String cognomeStr = cognome.getValue();
		if (cognomeStr.trim().compareTo("")==0)
			return null;
		String nomeStr = nome.getValue();
		if (nomeStr.trim().compareTo("")==0)
			return null;
		String comune  = cod_com_nasc.getValue();
		if (comune.trim().compareTo("")==0)
			return null;
		comune = codComXCF.getValue();
		if (comune.trim().compareTo("")==0)
			return null;
		if (data_nasc.getValue()==null) {
			return null;
		}
		Calendar c =  Calendar.getInstance(TimeZone.getDefault());
		c.setTime(data_nasc.getValue());
		boolean sessoBl = sesso.getSelectedValue().equals("F");
		String cfc = (new CodiceFiscale(cognomeStr, nomeStr, c.get(Calendar.YEAR)+"", (c.get(Calendar.MONTH)+1)+"", c.get(Calendar.DAY_OF_MONTH)+"",comune, sessoBl)).getCodice();
		return cfc;
	}
	
	
	public boolean doSaveForm() throws Exception{
		if(!isSavable()){
			Messagebox.show(
					Labels.getLabel("permissions.insufficient.on.doSaveForm"),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
			return false;
		}
		
		//Solo in fase di INSERT manuale, prima del testRequiredFields,
		//eseguo il seguente travaso di dati
    	if(this.currentIsasRecord==null){
			setDatiDomicilioFromResidenza();
			setDatiReperibilitaFromDomicilio();
		}
		
		UtilForComponents.testRequiredFields(self);
		
		if(!doValidateForm())
			return false;
		
		if(codiceFiscaleIsValido()){
			doSaveFormPasso2();
			return true;
		}else{
			Messagebox.show(Labels.getLabel("cartellaForm.msg.calcoloCF.verifica.ko"), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.NO+Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())){
								doSaveFormPasso2();
								return;
							}
						}
					});
			return false;
		}
	}	
	
	private void doSaveFormPasso2() throws Exception{		
		doWriteComponentsToBean();
		if(this.currentIsasRecord!=null){
			Object [] par = new Object[2];
    		par[0]= this.currentIsasRecord;
    		par[1]= vt;
    		this.currentIsasRecord = (ISASRecord)CaribelClass.isasInvoke(this.currentBean, "update", par);//insertSuEJB(this.currentBean,par);
    		//			this.currentIsasRecord = updateSuEJB(this.currentBean,this.currentIsasRecord);
		}else{
			this.hParameters.put("cod_operatore", getProfile().getStringFromProfile("codice_operatore"));
			this.hParameters.put("desc_operatore", getProfile().getStringFromProfile("cognome_operatore" ));
			Object [] par = new Object[2];
    		par[0]= this.hParameters;
    		par[1]= vt;
    		this.currentIsasRecord = (ISASRecord)CaribelClass.isasInvoke(this.currentBean, "insert", par);//insertSuEJB(this.currentBean,par);
			hParameters.put("n_cartella", currentIsasRecord.get("n_cartella").toString());
			//this.currentIsasRecord = insertSuEJB(this.currentBean,this.hParameters);
		}
		doSelectLastValueSuEJB2();
		doWriteBeanToComponents();
//		if(this.caribelGrid!=null){
//			//REFRESH SULLA LISTA
//			this.caribelGrid.doRefreshNoAlert();
//		}
		if(this.caribelContainerCtrl!=null){
			//REFRESH SUL CONTAINER
			caribelContainerCtrl.doRefreshOnSave(compChiamante);
		}
		if(this.currentIsasRecord!=null)
			notEditable();
		doFreezeForm();		
		Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
	}
	
	private boolean codiceFiscaleIsValido()throws Exception {
		if(this.currentIsasRecord==null){
			//Se sono in INSERT verifico che il codice fiscale 
			//inserito sia uguale a quello calcolato automaticamente
			String cfCalcolato = getCalcoloCodiceFiscale();
			if(cfCalcolato == null){
				throw new Exception(Labels.getLabel("cartellaForm.msg.calcoloCF.errore"));
			}
			if(!cod_fiscale.getValue().equalsIgnoreCase(cfCalcolato))
				return false;
		}
		return true;
	}

	/**
	 * Alcuni campi devono essere resi non editabili in base a configurazioni particolari
	 */
	@Override
	protected void notEditable() {
		try{
			checkAndDisableEditWhenBloccato();
			disableEditWhenConf();
			UtilForBinding.setComponentReadOnly(panel_team,true);//Team sempre disabilitato
		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		vt = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> h=null;
		h = new Hashtable<String, String>();
		/*CJ 01/02/2006 Devo controllare che se mi inseriscano la data chiusura
              deve essere specificato anche il motivo chiusura altrimenti non
              faccio fare niente
		 */
		if (data_chiusura.getValue() != null){
			//CJ Controllo che la data chiusura sia maggiore della data apertura
//			int ris = this.ControlloData( sm_data_inizio,sm_data_fine);
			if (data_apertura.getValue().after(data_chiusura.getValue())){
				UtilForUI.standardExclamation(Labels.getLabel("cartellaForm.msg.dateIncongruenti"));
				return false;
			}
			//CJ oltre alla data chiusura deve essere specificato il motivo chiusura
			if (cbx_motivoChiusura.getSelectedIndex()==-1  ||
					cbx_motivoChiusura.getSelectedValue().equals("") ||
					cbx_motivoChiusura.getSelectedValue().equals(".")){
				UtilForUI.standardExclamation(Labels.getLabel("cartellaForm.msg.mancaMotivoChiusura"));
				cbx_motivoChiusura.focus();
				return false;
			}
		}

//		if (data_variazione==null){
//			h.put("data_variazione","");
//			UtilForUI.standardExclamation(Labels.getLabel("cartellaForm.msg.mancaDataVariazione"));
//			data_variazione.focus();
//			return false;
//		}else{
////			String datavar=(String)JCariDateTextFieldDataVar.getUnmaskedText();
////			datavar = datavar.substring(6, 10)+"-"+datavar.substring(3, 5)+"-"+datavar.substring(0, 2);// data in JDBC
//			h.put("data_variazione",data_variazione.getValueForIsas());
//		}
		
		h.putAll(UtilForBinding.getHashtableFromComponent(anagra_c));
		h.put(data_variazione.getDb_name(), data_variazione.getValueForIsas());
//		//Se trovo qualche campo fra comuni, aree distrettuali e medico vuoto
//		//vado ad inserire il codice '000000'
//		if (JCariTextFieldCodComDom.getUnmaskedText().equals(""))
//			JCariTextFieldCodComDom.setUnmaskedText("000000");
//		/*24/11/2006 Faccio la decodifica in modo da presentare
//              in automatico la regione e usl d'iscrizione, anche quando
//              vengono messi tutti 000000
//		 */
//		if (JCariTextFieldCodComRes.getUnmaskedText().equals("")){
//			JCariTextFieldCodComRes.setUnmaskedText("000000");
//			this.ExecSelectComuni(forGridComRes);
//		}
//		if (JCariTextFieldAreaDom.getUnmaskedText().equals(""))
//			JCariTextFieldAreaDom.setUnmaskedText("000000");
//		if (JCariTextFieldAreaRes.getUnmaskedText().equals(""))
//			JCariTextFieldAreaRes.setUnmaskedText("000000");
//		if (JCariTextFieldCodMedico.getUnmaskedText().equals(""))
//			JCariTextFieldCodMedico.setUnmaskedText("000000");
//
//		// 21/12/04: ctrl comboBox region e tabusl
//		if ((!checkComboBox(this.jCariComboBoxRegion))
//				|| (!checkComboBox(this.jCariComboBoxUslProve)))
//			return;
//
//		//residenza
//		h.put("citta",(String)JCariTextFieldCodComRes.getUnmaskedText());
//		h.put("indirizzo",(String)JCariTextFieldIndirizzoRes.getUnmaskedText());
//		h.put("cap",(String)JCariTextFieldCapRes.getUnmaskedText());
//		h.put("areadis",(String)JCariTextFieldAreaRes.getUnmaskedText());
//		h.put("prov",(String)JCariTextFieldProvRes.getUnmaskedText());
//		h.put("localita",(String)JCariTextFieldLocalita.getUnmaskedText());
//		if (JCariComboBoxReligione.getSelectedIndex()!=-1){
//			String cod=(String)JCariComboBoxReligione.getMyvalue();
//			h.put("religione",cod);
//		}else h.put("religione","");
//		// 21/12/04
//		h.put("regione", this.jCariComboBoxRegion.getMyvalue());
//		h.put("usl", this.jCariComboBoxUslProve.getMyvalue());
//
//		//domicilio
//		h.put("dom_citta", (String)JCariTextFieldCodComDom.getUnmaskedText());
//		h.put("dom_prov", (String)JCariTextFieldProvDom.getUnmaskedText());
//		h.put("dom_areadis",(String)JCariTextFieldAreaDom.getUnmaskedText());
//		h.put("dom_indiriz",(String)JCariTextFieldIndirizzoDom.getUnmaskedText());
//		h.put("dom_localita",(String)JCariTextFieldDomLocalita.getUnmaskedText());
//		h.put("cod_med",(String)JCariTextFieldCodMedico.getUnmaskedText());
//		h.put("nome_camp",(String)JCariTextFieldCampan.getUnmaskedText());
//		h.put("telefono1",(String)JCariTextFieldTel1.getUnmaskedText());
//		h.put("telefono2",(String)JCariTextFieldTel2.getUnmaskedText());
//		if (JCariComboBoxlocalizzazione.getSelectedIndex()!=-1){
//			String cod2=(String)JCariComboBoxlocalizzazione.getMyvalue();
//			h.put("localizzazione",cod2);
//		}else h.put("localizzazione","");
//
//		// 23/11/09: TEAM
//		h.put("team_numero",(String)jCariTextFieldNumTeam.getUnmaskedText());
//		String dtScadTeam = "";
//		if (!jCariDateTextFieldScadTeam.IsVoid()) {
//			dtScadTeam = jCariDateTextFieldScadTeam.getUnmaskedText();
//			dtScadTeam = dtScadTeam.substring(6, 10)+"-"+dtScadTeam.substring(3, 5)+"-"+dtScadTeam.substring(0, 2);// data in JDBC
//		}
//		h.put("team_dt_scad", dtScadTeam);
//		// 28/04/2011 reperibilita
//		h.put("indirizzo_rep",(String)JCariTextFieldIndirizzoReperib.getUnmaskedText());
//		h.put("comune_rep",(String)JCariTextFieldCodComReperib.getUnmaskedText());
//		h.put("areadis_rep",(String)JCariTextFieldAreaReperib.getUnmaskedText());

		//elisa b 25/02/13
		h.put("cod_ope_loggato",getProfile().getStringFromProfile("codice_operatore"));


		vt.addElement(h);
		return true;
	}
	
	public void onClick$btn_anagra_sto() {
		try{
			Hashtable<String, String> h = new Hashtable<String, String>();
			h.put("n_cartella", n_cartella.getRawText());
			Executions.getCurrent().createComponents(AnagraStoricoCtrl.myPathFormZul, self, h);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void onClick$prova() {
		try{
			Hashtable h = (Hashtable)hParameters.clone();
			h.put("data_chiusura",data_chiusura.getValueForIsas());
			h.put("motivo_chiusura",cbx_motivoChiusura.getSelectedValue());
			invokeGenericSuEJB(myEJB,h,"chiudiCartellaDaIntegrazione");
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	// G.Brogi 26/04/11 test sulla disabilitazione di alcuni campi
    // se presente la chiave ANAGMODI con la funzione "MODI" vuol dire che l'operatore NON
    // puo' modificare i dati anagrafici.
    // Per lasciare tutto abilitato ad ADMIN (che è abilitato per tutte le chiavi isas)
    // si testa una funzione fittizia "ADMIN" che non possiede nessuno e a cui lui risulterà
    // comunque abilitato.
    private boolean canIUseAnagModi(){
        boolean bret = false;
        boolean anagmodi_modi   = getProfile().getIsasUser().canIUse(ChiaviIsasBase.ANAGRAFICA_MODI,"MODI");
        boolean anagmodi_admin  = getProfile().getIsasUser().canIUse(ChiaviIsasBase.ANAGRAFICA_MODI,"ADMIN");
        // la funzione ritorna un booleano secondo lo schema seguente:
        //  anagmodi_modi         anagmodi_admin    !(anagmodi_modi && !anagmodi_admin)
        //  F                       F                   T
        //  T                       F                   F
        //  T                       T                   T
        //  F                       T                   T
        bret = !(anagmodi_modi && !anagmodi_admin);
        return bret;
    }
    
    // 23/01/13 mv: ulteriore chiave ISAS per la sola chiusura dell'anagrafica (campi data e motivo chiusura)
    // Deve essere utilizzata solo nel caso di apposita configurazione.
    private boolean canICloseAnag(){
        String confCntrlChiu = getProfile().getStringFromProfile("ctrl_chiu_anag");
        boolean isChiuDaControl = ((confCntrlChiu != null) && (confCntrlChiu.trim().equals("SI")));
        boolean abilCloseAnag = getProfile().getIsasUser().canIUse(ChiaviIsasBase.ANAGRAFICA_CLOSE);
        if (!isChiuDaControl)
            return canIUseAnagModi();
        return (abilCloseAnag && canIUseAnagModi());
    }
    
    private void disableEditWhenConf(){
    	if (!canIUseAnagModi()){
    		UtilForBinding.setComponentReadOnly(panel_area,true);
    		UtilForBinding.setComponentReadOnly(panel_reperibilita,false);
    		UtilForBinding.setComponentReadOnly(panel_stranieri,false);
    		UtilForBinding.setComponentReadOnly(panel_team,false);
    		btn_anagra_sto.setDisabled(false);
    		data_apertura.setDisabled(false);
    		data_variazione.setDisabled(false);
        } else if (!canICloseAnag()){ //23/01/13 mv: ulteriore cntrl ISAS per chiusura
            abilFldChiusura(false);
        }
    }
    
    // 23/01/13 mv: abilitazione campi chiusura (data e motivo)
    private void abilFldChiusura(boolean tof){
    	tof = !tof;//faccio il negato rispetto al vecchio
    	data_chiusura.setDisabled(tof);
    	cbx_motivoChiusura.setDisabled(tof);
    }
    
    //gb 02/04/07: Disabilito l'edit di alcuni campi e disabilito il sesso
    //  se il contenuto del campo jCariTextFieldBloccato è uguale a 'S',
    //  cioè se il campo 'bloccato' nella tabella 'cartella' è 'S'
    //  cognome, nome, codice fiscale, data di nascita, comune di nascita,
    //  sesso, codice sanitario regionale
    private void checkAndDisableEditWhenBloccato()throws Exception{
    	String strBloccato = "";
    	if(currentIsasRecord!=null && currentIsasRecord.get("bloccato")!=null)
    		strBloccato = (String)currentIsasRecord.get("bloccato");
    	CaribelSearchCtrl comuneNascita = (CaribelSearchCtrl) comuneNascitaSearch.getAttribute(MY_CTRL_KEY);
    	if (strBloccato.equals("S")){
    		cod_regionale.setReadonly(true);
    		cod_fiscale.setReadonly(true);
    		cognome.setReadonly(true);
    		nome.setReadonly(true);
    		comuneNascita.setReadonly(true);
    		data_nasc.setReadonly(true);
    		sesso.setDisabled(true);
    	}else{
    		cod_regionale.setReadonly(false);
    		cod_fiscale.setReadonly(false);
    		cognome.setReadonly(false);
    		nome.setReadonly(false);
    		comuneNascita.setReadonly(false);
    		data_nasc.setReadonly(false);
    		sesso.setDisabled(false);
    	}
    }

    private void setDatiReperibilitaFromDomicilio() {
    	//Solo in fase di INSERT manuale, se i campi reperibilita sono tutti vuoti
    	//allora li valorizzo con quelli del domicilio
    	if(this.currentIsasRecord==null){
    		String str_indirizzo_rep    = indirizzo_rep.getRawText(); 
    		String str_nome_camp        = nome_camp.getRawText();     
    		String str_comune_rep       = comune_rep.getRawText();    
    		String str_comreperibdescr  = comreperibdescr.getRawText();
    		String str_rep_cap          = rep_cap.getRawText();       
    		String str_provincia_rep    = provincia_rep.getRawText(); 
    		String str_telefono1_rep    = telefono1_rep.getRawText(); 
    		String str_telefono2_rep    = telefono2_rep.getRawText(); 
    		String str_areadis_rep      = areadis_rep.getRawText();   
    		String str_desc_com_rep     = desc_com_rep.getRawText();

    		if (!ISASUtil.valida(str_indirizzo_rep  ) &&
    				!ISASUtil.valida(str_nome_camp      ) &&
    				!ISASUtil.valida(str_comune_rep     ) &&
    				!ISASUtil.valida(str_comreperibdescr) &&
    				!ISASUtil.valida(str_rep_cap        ) &&
    				!ISASUtil.valida(str_provincia_rep  ) &&
    				!ISASUtil.valida(str_telefono1_rep  ) &&
    				!ISASUtil.valida(str_telefono2_rep  ) &&
    				!ISASUtil.valida(str_areadis_rep    ) &&
    				!ISASUtil.valida(str_desc_com_rep   )
    				){
    			String str_indirizzo_dom     = indirizzo_dom.getRawText();     
    			String str_dom_citta         = dom_citta.getRawText();         
    			String str_comdomdescr       = comdomdescr.getRawText();      
    			String str_provincia_dom     = provincia_dom.getRawText();     
    			String str_telefono1_dom     = telefono1_dom.getRawText();     
    			String str_telefono2_dom     = telefono2_dom.getRawText();     
    			String str_cod_areadis_dom   = cod_areadis_dom.getRawText();   
    			String str_desc_areadis_dom  = desc_areadis_dom.getRawText();

    			//Riporto i valori del domicilio
    			indirizzo_rep.setText(str_indirizzo_dom);
    			comune_rep.setText(str_dom_citta);
    			comreperibdescr.setText(str_comdomdescr);
    			//rep_cap.setText(???????????);
    			provincia_rep.setText(str_provincia_dom);
    			telefono1_rep.setText(str_telefono1_dom);
    			telefono2_rep.setText(str_telefono2_dom);
    			areadis_rep.setText(str_cod_areadis_dom);
    			desc_com_rep.setText(str_desc_areadis_dom);
    		}
    	}
    }

    private void setDatiDomicilioFromResidenza() {
    	//Solo in fase di INSERT manuale, se i campi domicilio sono tutti vuoti
    	//allora li valorizzo con quelli della residenza
    	if(this.currentIsasRecord==null){
    		String str_indirizzo_dom     = indirizzo_dom.getRawText();     
    		String str_dom_citta         = dom_citta.getRawText();         
    		String str_comdomdescr       = comdomdescr.getRawText();      
    		String str_provincia_dom     = provincia_dom.getRawText();     
    		String str_telefono1_dom     = telefono1_dom.getRawText();     
    		String str_telefono2_dom     = telefono2_dom.getRawText();     
    		String str_cod_areadis_dom   = cod_areadis_dom.getRawText();   
    		String str_desc_areadis_dom  = desc_areadis_dom.getRawText(); 

    		if (!ISASUtil.valida(str_indirizzo_dom     ) &&
    				!ISASUtil.valida(str_dom_citta         ) &&
    				!ISASUtil.valida(str_comdomdescr       ) &&
    				!ISASUtil.valida(str_provincia_dom     ) &&
    				!ISASUtil.valida(str_telefono1_dom     ) &&
    				!ISASUtil.valida(str_telefono2_dom     ) &&
    				!ISASUtil.valida(str_cod_areadis_dom   ) &&
    				!ISASUtil.valida(str_desc_areadis_dom  )
    				){
    			String str_indirizzo_res     = indirizzo_res.getRawText(); 
    			String str_cap_res           = cap_res.getRawText(); 
    			String str_cod_citta_res     = cod_citta_res.getRawText(); 
    			String str_comresdescr       = comresdescr.getRawText(); 
    			String str_provincia_res     = provincia_res.getRawText(); 
    			String str_cod_areadis_res   = cod_areadis_res.getRawText(); 
    			String str_desc_areadis_res  = desc_areadis_res.getRawText(); 

    			//Riporto i valori della residenza
    			indirizzo_dom.setText(str_indirizzo_res);    
    			dom_citta.setText(str_cod_citta_res);            
    			comdomdescr.setText(str_comresdescr);
    			provincia_dom.setText(str_provincia_res);
    			cod_areadis_dom.setText(str_cod_areadis_res);
    			desc_areadis_dom.setText(str_desc_areadis_res);
    		}
    	}
    }
    
    private void visualizzaBtnPrintQRCode(){
    	 String valConf = getProfile().getStringFromProfile("abil_qrc");
    	 boolean abilitaQrc = (ISASUtil.valida(valConf)&& valConf.equalsIgnoreCase("SI"));
    	 if(abilitaQrc){
    		 super.btn_print_qrc.setVisible(true);
    	 }
    }
    
    protected void doStampaQRCode(){
    	try{
    		String cartella=this.n_cartella.getRawText();
    		String u = "/AsterdroidServlet/AsterdroidServlet" +
    				"?EJB=ASTERDROIDQRC" +
    				"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
    				"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
    				"&METHOD=print&n_cartella=" +cartella;
    		it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
    	}catch(Exception e){
    		doShowException(e);
    	}
    }
	
}

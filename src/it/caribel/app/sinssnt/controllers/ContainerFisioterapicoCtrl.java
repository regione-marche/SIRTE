package it.caribel.app.sinssnt.controllers;

import it.caribel.app.sinssnt.bean.RLPresaCaricoEJB;
import it.caribel.app.sinssnt.bean.SkValutazEJB;
import it.caribel.app.sinssnt.controllers.interfacce.IContainerFisioterapicoCtrl;
import it.caribel.app.sinssnt.controllers.intolleranzeAllergie.IntolleranzeAllergieGridCRUDCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerLogout;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.app.sinssnt.util.UtilForContainerGen;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.ButtonMenuContainer;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Tab;

public class ContainerFisioterapicoCtrl extends ContainerSinssntCtrl implements IContainerFisioterapicoCtrl {
	
	private static final long serialVersionUID = -6272002050610951859L;

	public static final String myPathZul = "/web/ui/containerFisioterapico.zul"; 
	
	//Hashtable<String, String> hIdUrizul = new Hashtable<String, String>();
	
	public static final String CTS_IS_SK_VAL_CHIUSA = "isSkValChiusa"; 
	private String CTS_INTOLLERANZE_ALLERGIE = "intolleranzeAllergieForm";
	public static final String CTS_SEGNALAZIONI = ContainerMedicoCtrl.CTS_SEGNALAZIONI;
	public static final String CTS_RIEPILOGO_ACCESSI = "riepilogoAccessiForm";
	private String fonte = "";
	private boolean aprireSegnalazione = false;

	    
	Tab menuLeftTabContatto;

	private static final String ver ="2-";
	
	public ComponentInfo doBeforeCompose(Page page, Component parent,ComponentInfo compInfo){
		try{
			ISASUser iu =CaribelSessionManager.getInstance().getIsasUser();
			if(iu!=null && !iu.canIUse(ChiaviISASSinssntWeb.SKFISIO)){
				(new ManagerLogout()).doLogout();
				return null;
			}else
				return super.doBeforeCompose(page, parent, compInfo);
		}catch(Exception ex){
			UtilForUI.standardExclamation(Labels.getLabel("exception.generic.error.msg"));
			return null;
		}
	}
	
	protected void initContainer(){	
		super.initContainer();
		List<Component> listComp = UtilForComponents.getAllChildren(menu_left); 
		for (Component corrComp : listComp){
			if(corrComp instanceof ButtonMenuContainer){
				String idForm = ((ButtonMenuContainer)corrComp).getIdForm();
				String pathZulForm = ((ButtonMenuContainer)corrComp).getPathZulForm();
				hIdUrizul.put(idForm, pathZulForm);
				super.hashMenuLeft.put(idForm, ((ButtonMenuContainer)corrComp));
			}	
		}
	}
		
	public void doAfterCompose(Component comp) throws Exception {
		String punto = ver + "doAfterCompose ";
		super.doAfterCompose(comp);
		initContainer();
		Selectors.wireEventListeners(comp, this);//Per collegare gli eventi sul menu di destra dopo l'initContainer
		//Recupero il codice assistito passato dall'altro container
		String n_cartella = Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA);
		
		fonte = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE);
		aprireSegnalazione = (ISASUtil.valida(fonte) && fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI+"")); 
		logger.debug(punto + " stampa >>"+fonte+"<<");
		
		if(n_cartella!=null && !n_cartella.trim().equals("")){
	        UtilForContainer.restartContainerFromFassi(n_cartella);
	    }else{
	    	UtilForContainer.restartContainerFromListaAttivita();
	    }
		UtilForContainer.refreshToolBar();
		if(Executions.getCurrent().getSession().hasAttribute(CostantiSinssntW.FIRST_ACCESS)){
			UtilForUI.doTestVersionBrowser();
			Executions.getCurrent().getSession().removeAttribute(CostantiSinssntW.FIRST_ACCESS);
		}
	}
	
	public void gestisciChiaviCorr(Component comp){
		String punto = "gestisciChiaviCorr ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				Object n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawValue();
				if(n_cartella!=null)
					hashChiaveValore.put("n_cartella", n_cartella);
				Object cod_com_nasc = ((CaribelTextbox)comp.query("#cod_com_nasc")).getRawValue();
				if(cod_com_nasc!=null)
					hashChiaveValore.put(CostantiSinssntW.COD_COM_NASC, cod_com_nasc);
				Object desc_com_nasc = ((CaribelCombobox) comp.query("#desc_com_nasc")).getRawValue();
				if (desc_com_nasc != null)
					hashChiaveValore.put(Costanti.DESC_COM_NASC, desc_com_nasc);   
				Object dataNasc = ((CaribelDatebox)comp.query("#data_nasc")).getRawValue();
				if(dataNasc!=null)
					hashChiaveValore.put("data_nasc", dataNasc);
				Object dataCart = ((CaribelDatebox)comp.query("#data_apertura")).getRawValue();
				if(dataCart!=null)
					hashChiaveValore.put("data_apertura", dataCart);
				Object dataCartChius = ((CaribelDatebox)comp.query("#data_chiusura")).getRawValue();
				if(dataCartChius!=null)
					hashChiaveValore.put("data_chiusura", dataCartChius);
				Object codiceFiscale = ((CaribelTextbox)comp.query("#cod_fiscale")).getRawValue();
				if(codiceFiscale!=null)
					hashChiaveValore.put(Costanti.ASSISTITO_COD_FISC, codiceFiscale);
			}else if(comp.getId().equals("contattoFisioForm")){
				addChiaveValore("skf_data_chiusura", "#skf_data_chiusura", comp);
				hashChiaveValore.put(CostantiSinssntW.TIPO_OPERATORE, GestTpOp.CTS_COD_FISIOTERAPISTA);
//				addChiaveValore(CostantiSinssntW.N_CONTATTO, "#n_contatto", comp);
//				addChiaveValore(CostantiSinssntW.CTS_ID_SKSO, "#id_skso", comp);
				addChiaveValore(CostantiSinssntW.SKF_DATA, "#skf_data", comp);
				
//				addChiaveValore(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, "#skf_data", comp);
//				addChiaveValore(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, "#skf_data_chiusura", comp);
				
				
				aggiungiValoreContainer(CostantiSinssntW.N_CONTATTO, "#n_contatto", comp);
				aggiungiValoreContainer(CostantiSinssntW.CTS_ID_SKSO, "#id_skso", comp);
				
				aggiungiValoreContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, "#skf_data", comp);
				aggiungiValoreContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, "#skf_data_chiusura", comp);
				aggiungiValoreContainer(CostantiSinssntW.COD_OPERATORE_REF, "#skf_fisio", comp);

				
				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				logger.debug(punto + "diario ");
			}else if(comp.getId().equals("fassiGrid")){
				hashChiaveValore.clear();
			}
		}
	}
	
	public void gestisciTopMenu(Component comp){
		String punto = ver+ "gestisciTopMenu ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				String n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawText();
				String cognome = ((CaribelTextbox)comp.query("#cognome")).getRawText();
				String nome = ((CaribelTextbox)comp.query("#nome")).getRawText();
				String desc_com_nasc = ((CaribelCombobox)comp.query("#desc_com_nasc")).getRawText();
				String data_nasc = ((CaribelDatebox)comp.query("#data_nasc")).getRawText();
				String dataApertura =((CaribelDatebox)comp.query("#data_apertura")).getRawText();

				String testo = 	Labels.getLabel("caribelContainer.cartella.numero")+": "+
								n_cartella+" "+Labels.getLabel("caribelContainer.cartella.assistito")+" "+
								cognome +" "+
								nome+
								" "+Labels.getLabel("caribelContainer.cartella.data_nascita")+" "+data_nasc+
								" "+Labels.getLabel("caribelContainer.cartella.luogo_nascita")+" "+desc_com_nasc;
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_COGNOME, cognome);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_NOME, nome);
				
				String dataChiusura="";
				String valoreMotivoChiusura = "";
				try {
					dataChiusura=((CaribelDatebox)comp.query("#data_chiusura")).getRawText();
					valoreMotivoChiusura = "";
					if (ManagerDate.validaData(dataChiusura)){
						if (comp.query("#cbx_motivoChiusura")!=null && ((CaribelCombobox)comp.query("#cbx_motivoChiusura")).getRawText() !=null){
							valoreMotivoChiusura = ((CaribelCombobox) comp.query("#cbx_motivoChiusura")).getRawText();
						}
					}
				} catch (Exception e) {
					logger.error(punto + " Errore nel recuperare la chiusura cartella ");
				}
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_CARTELLA_APERTA, dataApertura);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA, dataChiusura);
				if (desc_com_nasc != null){
					hashChiaveValore.put("desc_com_nasc", desc_com_nasc);
				}
				
				testo = ContainerInfermieristicoCtrl.gestioneChiusuraCartella(testo, dataChiusura, valoreMotivoChiusura);
				addItemMenuTop(comp,testo, (ManagerDate.validaData(dataChiusura) ? CostantiSinssntW.CTS_ROW_RED:""));
			}else if(comp.getId().equals("contattoFisioForm")){
				String n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawText();
				String skf_data = ((CaribelDatebox)comp.query("#skf_data")).getRawText();
				String testo = "";

				if(n_contatto==null || n_contatto.equals(""))
					testo = Labels.getLabel("caribelContainer.contatto.in_fase_di_ins");
				else
					testo = Labels.getLabel("caribelContainer.contatto.numero")+": "+n_contatto+
							" "+Labels.getLabel("caribelContainer.contatto.data_apertura")+" "+skf_data;

				String skfDataChiusura = ((CaribelDatebox)comp.query("#skf_data_chiusura")).getRawText();
				if(ManagerDate.validaData(skfDataChiusura)){
					testo+=" " +Labels.getLabel("SchedaFisioForm.dimissioni.datachiusura")+": "+ skfDataChiusura;
				}
				addItemMenuTop(comp,testo, (ManagerDate.validaData(skfDataChiusura) ? CostantiSinssntW.CTS_ROW_RED:""));
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				logger.debug(punto + " diario>>");
			}
		}
	}
	
	public void gestisciLeftMenu(){
		//Disabilito tutto
		disableAllButtonMenuLeft(true);
		
		//Abilito pulsanti sempre attivi
		if(hashChiaveValore.get(CostantiSinssntW.N_CARTELLA)!=null){
			//Abilito Contatto
			disableButtonMenuLeft("contattoFisioForm",false);
			disableButtonMenuLeft("contattoFisioGridSto",false); 
			//Abilito il bottone di AsterView
			disableButtonMenuLeft("asterview", !conAsterView);
			//Abilito il bottone dei documenti
			disableButtonMenuLeft("documenti", false);
		}
		if(hashChiaveValore.get(CostantiSinssntW.N_CONTATTO)!=null && !hashChiaveValore.get(CostantiSinssntW.N_CONTATTO).equals("0")){
			menuLeftTabContatto.setSelected(true);
			//abilita accessi
			disableButtonMenuLeft("medicoAccessiForm",false);
			disableButtonMenuLeft("pianoAssistForm",false);
			//Abilito Diario
			disableButtonMenuLeft(CostantiSinssntW.CTS_DIARIO , false);
			disableButtonMenuLeft(CTS_INTOLLERANZE_ALLERGIE,false);
			//Abilito Segnalazioni
			disableButtonMenuLeft(CTS_SEGNALAZIONI,false);
			disableButtonMenuLeft(CTS_RIEPILOGO_ACCESSI,false);
		}		
		gestisciPersonalizzazioni();
	}
	
	private void gestisciPersonalizzazioni() {
		if (ManagerProfile.isConfigurazioneMarche(getProfile())){
			/*disabilitati i pulsanti dei flussi*/
			hashMenuLeft.get("flussiSiadForm").setVisible(false);
			hashMenuLeft.get("flussiStoForm").setVisible(false);
			hashMenuLeft.get("pianoAssistForm").setVisible(false);
		}
	}
	
	private void gestisciApertureAutomatiche(Component comp){
		String punto = ver  + "gestisciApertureAutomatiche ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				//Ho aperto una Cartella, allora provo ad aprire anche il relativo Contatto, se non e' stato ancora aperto
				String n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawText();	
				if(super.hashIdComponent.get("contattoFisioForm")==null &&  n_cartella!=null && !n_cartella.equals("")){
					recuperaDaSchedaValutazione(n_cartella);
					HashMap<String, Object> mapParam = new HashMap<String, Object>();
					mapParam.put("n_cartella", n_cartella);
					Component contattoFisioForm = createComponent(hIdUrizul.get("contattoFisioForm"), mapParam);
					showComponent(contattoFisioForm);
				}
			} else if(comp.getId().equals("contattoFisioForm") && aprireSegnalazione){
				logger.trace(punto + " dati CostantiSinssntW FISIO FORM ");
				aprireSegnalazione = false;
				String nCartella = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA);
				HashMap<String, Object> mapParam = new HashMap<String, Object>();
				mapParam.put(CostantiSinssntW.N_CARTELLA, nCartella);
				Component sntSegnalazioni = createComponent(hIdUrizul.get(CTS_SEGNALAZIONI), mapParam);
				showComponent(sntSegnalazioni);
			} 
		}
	}
	
	private void recuperaDaSchedaValutazione(String n_cartella) {
		/*  Carica i dati della scheda di valutazione */
		SkValutazEJB skValutazEJB = new SkValutazEJB();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		try {
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
			Hashtable<String, String> datiRicevuti = skValutazEJB.query_esisteSkValAttiva(CaribelSessionManager.getInstance().getMyLogin(),	dati);

			if(datiRicevuti.containsKey(CostantiSinssntW.PR_DATA)){
				hashChiaveValore.put(CostantiSinssntW.PR_DATA, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.PR_DATA));
			}
			hashChiaveValore.put("desc_val_ap", ISASUtil.getValoreStringa(datiRicevuti, "desc_val_ap"));
			String prDataCarico=ISASUtil.getValoreStringa(datiRicevuti, "pr_data_carico"); 
			if (ISASUtil.valida(prDataCarico)){
				hashChiaveValore.put("pr_data_carico", prDataCarico);
			}
			String prDataValutaz = ISASUtil.getValoreStringa(datiRicevuti, "pr_data_valutaz");
			if (ISASUtil.valida(prDataValutaz)){
				hashChiaveValore.put("pr_data_valutaz", prDataValutaz);
			}
			hashChiaveValore.put("isSkValChiuso", new Boolean(false));
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (CariException e) {
			e.printStackTrace();
		}
	}

	private void gestisciRefreshGrid(Component comp){
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
		}
	}
			
	
	
	@Override
	public void onShowComponent(Event evt) {
		Component comp = ((Component)evt.getData());
		gestisciChiaviCorr(comp);
		gestisciTopMenu(comp);
		gestisciApertureAutomatiche(comp);
		gestisciRefreshGrid(comp);
		gestisciLeftMenu();
	}

	@Listen("onClick=#btn_segnalazioniForm")
	public void btn_segnalazioniForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI),false,null);
			}else{
				super.showComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_riepilogoAccessiForm")
	public void btn_riepilogoAccessiForm() {
		String punto = ver + "btn_riepilogoAccessiForm ";
		logger.trace(punto + " inizio ");
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI),false,null);
			}else{
				removeComponentsFrom(CTS_RIEPILOGO_ACCESSI);
				super.showComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_contattoFisioForm")
	public void btn_contattoFisioForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("contattoFisioForm",hIdUrizul.get("contattoFisioForm"),true,null);
			}else{
				removeComponentsFrom("contattoFisioForm");
				super.showComponent("contattoFisioForm",hIdUrizul.get("contattoFisioForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_contattoFisioGridSto")
	public void btn_contattoFisioGridSto() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "N");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("contattoFisioGridSto",hIdUrizul.get("contattoFisioGridSto"),true,mapParam);
			}else{
				removeComponentsFrom("contattoFisioGridSto");
				super.showComponent("contattoFisioGridSto",hIdUrizul.get("contattoFisioGridSto"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_intolleranzeAllergieForm")
	public void btn_intolleranzeAllergieForm() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_NOME_TABELLA, "RM_INTOLLERANZE_ALLERGIE");
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_STRING_N_CARTELLA, UtilForContainerGen.getCartellaCorr());
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_ID_MY_WINDOW, CTS_INTOLLERANZE_ALLERGIE);
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_INTOLLERANZE_ALLERGIE,IntolleranzeAllergieGridCRUDCtrl.myPathFormZul,false,mapParam);
			}else{
				super.showComponent(CTS_INTOLLERANZE_ALLERGIE,IntolleranzeAllergieGridCRUDCtrl.myPathFormZul,mapParam);
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_diarioForm")
	public void btn_diarioForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO),false,null);
			}else{
				super.showComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_contattoFisioGridAltri")
	public void btn_contattoFisioGridAltri() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "S");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("contattoFisioGridSto",hIdUrizul.get("contattoFisioGridSto"),true,mapParam);
			}else{
				removeComponentsFrom("contattoFisioGridSto");
				super.showComponent("contattoFisioGridSto",hIdUrizul.get("contattoFisioGridSto"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	@Listen("onClick=#btn_cartellaForm")
	public void btn_cartellaForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("cartellaForm",hIdUrizul.get("cartellaForm"),false,null);
			}else{
				super.showComponent("cartellaForm",hIdUrizul.get("cartellaForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_medicoAccessiForm")
	public void btn_medicoAccessiForm() {
		HashMap <String, Object> argForZul = new HashMap<String, Object>();
		argForZul.put("provAccessiPrestazioni", 1+"");
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("accessiPrestazioniForm",hIdUrizul.get("medicoAccessiForm"),true,argForZul);
			}else{
				removeComponentsFrom("accessiPrestazioniForm");
				super.showComponent("accessiPrestazioniForm",hIdUrizul.get("medicoAccessiForm"),argForZul);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_fisioPianoAssForm")
	public void btn_fisioPianoAssForm() {		
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("pianoAssistForm",hIdUrizul.get("pianoAssistForm"),true,null);
			}else{
				removeComponentsFrom("pianoAssistForm");
				super.showComponent("pianoAssistForm",hIdUrizul.get("pianoAssistForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
		
	@Listen("onClick=#btn_fisioFlussiSiadForm")
	public void btn_fisioFlussiSiadForm() {
		try {
			if (caricaCaso()){
				if(formNeedApprovalForSave()){			
					executeApprovalForSaveBeforShowComponent("flussiSiadForm",hIdUrizul.get("flussiSiadForm"),true,null);
				}else{
					removeComponentsFrom("flussiSiadForm");
					super.showComponent("flussiSiadForm",hIdUrizul.get("flussiSiadForm"));
				}
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_fisioFlussiStoForm")
	public void btn_fisioFlussiStoForm() {		
		try {
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("flussiStoForm",hIdUrizul.get("flussiStoForm"),true,null);
			}else{
				removeComponentsFrom("flussiStoForm");
				super.showComponent("flussiStoForm",hIdUrizul.get("flussiStoForm"));
			}
			
		}catch(Exception e){
			doShowException(e);
		}
	}	

	@Override
	public void doRefreshOnSave(Component comp) {
		gestisciChiaviCorr(comp);
		gestisciTopMenu(comp);
		gestisciLeftMenu();
	}
	
	@Override
	public void doRefreshOnDelete(Component comp) {
		if(comp!=null && comp.getId()!=null || !comp.getId().equals("")){
			if(comp.getId().equals("cartellaForm")){
				removeComponentsFrom(comp.getId());
				super.showComponent("fassiGrid",hIdUrizul.get("fassiGrid"));
				
			}else if(comp.getId().equals("contattoFisioForm")){
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent("cartellaForm",hIdUrizul.get("cartellaForm"));
				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				//Diario 
				removeComponentsFrom(comp.getId());
				super.showComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO));
			}else if(comp.getId().equals(CTS_INTOLLERANZE_ALLERGIE)){
				// Relazione medica  
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_INTOLLERANZE_ALLERGIE,hIdUrizul.get(CTS_INTOLLERANZE_ALLERGIE));
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI));
			}else if(comp.getId().equals(CTS_RIEPILOGO_ACCESSI)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI));
			}
			gestisciLeftMenu();
		}
	}
	
	
	public void removeComponentsFrom(String idComp){
		if(idComp!=null && !idComp.equals("")){
			if(idComp.equals("cartellaForm") || idComp.equals("fassiGrid")){
				hashChiaveValore.remove("n_cartella");
				hashChiaveValore.remove("n_contatto");
				hashChiaveValore.remove(CostantiSinssntW.CTS_ID_SKSO);
				hashChiaveValore.remove("skf_data");
				hashChiaveValore.remove("smri_progr");
				hashChiaveValore.remove("smp_data");
				super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get("contattoFisioForm"));
				super.removeComponent(hashIdComponent.get("flussiSiadForm"));
				super.removeComponent(hashIdComponent.get("contattoFisioGridSto"));
				super.removeComponent(hashIdComponent.get(CostantiSinssntW.CTS_DIARIO));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				
			}else if(idComp.equals("contattoFisioForm") || idComp.equals("contattoFisioGridSto")){
				hashChiaveValore.remove("n_contatto");
				hashChiaveValore.remove(CostantiSinssntW.CTS_ID_SKSO);
				hashChiaveValore.remove("skf_data");
				hashChiaveValore.remove("smri_progr");
				hashChiaveValore.remove("smp_data");
				//super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get("contattoFisioForm"));
				super.removeComponent(hashIdComponent.get("flussiSiadForm"));
				super.removeComponent(hashIdComponent.get("contattoFisioGridSto"));
				super.removeComponent(hashIdComponent.get(CostantiSinssntW.CTS_DIARIO));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
			}else if (idComp.equals(CTS_RIEPILOGO_ACCESSI)){
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
			}
		}
	}
	
	public boolean isSkValChiusa() {
		boolean isSkValChiusa = ISASUtil.getvaloreBoolean(hashChiaveValore, CTS_IS_SK_VAL_CHIUSA);

		if (isSkValChiusa) {
			isSkValChiusa = ISASUtil.getvaloreBoolean(hashChiaveValore, CTS_IS_SK_VAL_CHIUSA);
		}
		return isSkValChiusa;
	}   
	
	   private boolean caricaCaso() throws Exception
	   {
	       
	       // 05/04/13 ---

	       Hashtable h_skval = (Hashtable)hashChiaveValore.clone();
	       h_skval.put("tipouvg","1");

	       Hashtable h_caso = new Hashtable();
	       h_caso.put(CostantiSinssntW.N_CARTELLA,h_skval.get(CostantiSinssntW.N_CARTELLA));

	       if (h_skval.containsKey(CostantiSinssntW.PR_DATA) && h_skval.get(CostantiSinssntW.PR_DATA)!= null)
	           h_caso.put(CostantiSinssntW.PR_DATA, h_skval.get(CostantiSinssntW.PR_DATA));
	      
	     
	       h_caso.put("chiamante", CostantiSinssntW.CASO_SAN);
	       h_caso.put("tipouvg","1");

	       
	       Hashtable nuovoCaso = null;
	   
	        
	       Hashtable datiCaso = (Hashtable)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_caso, "selCaso");

	       if ((datiCaso != null) && (datiCaso.size() > 0)) {
	           if (datiCaso.get("id_caso") != null)
	               hashChiaveValore.put("id_caso", ((Integer)datiCaso.get("id_caso")).toString());

	           if (datiCaso.get("dt_conclusione") != null)
	        	   hashChiaveValore.put("dt_conclusione", datiCaso.get("dt_conclusione").toString());

	           if (datiCaso.get("dt_segnalazione") != null)
	        	   hashChiaveValore.put("dt_segnalazione", datiCaso.get("dt_segnalazione").toString());

	           if (datiCaso.get("pr_data") != null) {
	               String dtSkVal = datiCaso.get("pr_data").toString();               
	               hashChiaveValore.put("pr_data", dtSkVal);             
	           }
	          
	       } else
	       {
	           nuovoCaso = (Hashtable)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_caso, "creaCaso");

	           if ((nuovoCaso != null) && (nuovoCaso.size() > 0))
	           {
	               if (nuovoCaso.get("id_caso") != null)
	            	   hashChiaveValore.put("id_caso", ((Integer)nuovoCaso.get("id_caso")).toString());

	               if (nuovoCaso.get("dt_conclusione") != null)
	            	   hashChiaveValore.put("dt_conclusione", nuovoCaso.get("dt_conclusione").toString());

	               if (nuovoCaso.get("dt_segnalazione") != null)
	            	   hashChiaveValore.put("dt_segnalazione", nuovoCaso.get("dt_segnalazione").toString());

	      
	           if (nuovoCaso.get("pr_data") != null) {
	               String dtSkVal = nuovoCaso.get("pr_data").toString();
	               hashChiaveValore.put("pr_data", dtSkVal);
	           }
	       
	              logger.debug("JCariContainerINFCartella.caricaCaso(): CASO creato da zero!!!");
	           }else
	           {
	        	   logger.debug("JCariContainerINFCartella.caricaCaso(): creazione nuovo CASO non riuscita!!!");
	               return false;
	           }
	       }
	       return true;
	   }
}

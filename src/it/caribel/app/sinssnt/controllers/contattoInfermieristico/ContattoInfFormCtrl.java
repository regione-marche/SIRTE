package it.caribel.app.sinssnt.controllers.contattoInfermieristico;
//==========================================================================
//CARIBEL S.r.l.
//
//18/08/2014 --------Controller Scheda Infermieri----
//Simone Puntoni
//==========================================================================

import it.caribel.app.common.controllers.ospedali.OspedaliSearchCtrl;
import it.caribel.app.common.controllers.reparti.RepartiSearchCtrl;
import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.RtValutazSanEJB;
import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.sinssnt.bean.corretti.RLSkPuacEJB;
import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.controllers.ContainerInfermieristicoCtrl;
import it.caribel.app.sinssnt.controllers.accessi_effettuati.AccessiEffettuatiGridCtrl;
import it.caribel.app.sinssnt.controllers.contatto.CaribelContattoFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.riepilogo_ausili_protesica.RiepilogoAusiliProtesicaGridCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalePanelCtr;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.storico_referente.StoricoInfermiereReferenteGridCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.diagnosi.DiagnosiFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;

public class ContattoInfFormCtrl extends CaribelContattoFormCtrl {

	private static final long serialVersionUID = 1L;
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/contatto_infermieristico/contatto_inf.zul";

	private String myKeyPermission = ChiaviISASSinssntWeb.SKINF;
	private SkInfEJB myEJB = new SkInfEJB();
	protected Window contattoInfForm;

//	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	private CaribelDatebox pr_data;
	
//	private Component cs_operatore_referente;
	private Component cs_presidio;
	private CaribelTextbox ski_motivo_txt;
	private CaribelDatebox ski_data_apertura;
	private CaribelDatebox ski_data_uscita;
	private CaribelTextbox txt_motchiusura;
	private CaribelTextbox ski_infermiere;
	private CaribelTextbox ski_operatore;
	private CaribelCombobox desc_inf;
	private CaribelTextbox ski_cod_presidio;
	private CaribelCombobox desc_presidio;
	//boolean primaVisita = false;
	private CaribelCombobox cbx_provenienza; // provenienza
	private CaribelCombobox cbx_motivo; // motivo
	private CaribelCombobox cbx_utenza; // tipo utenza
	private CaribelCombobox cbx_motchiusura; // motivo chiusura
	private CaribelCombobox cbx_tipologia;
	private CaribelCombobox cbx_modalita;
	private Label label_prima_btn_accessi;
	private Label label_modalita_tipologia;
	private Label label_segnalazione;
	private Label label_dimis_progr;
	private Label insteadof_btn_skUVT;

	private Component sezione_ospedale;
	
	//private CaribelIntbox id_skso;
	private OspedaliSearchCtrl ospedaliSearch;
	private RepartiSearchCtrl repartiSearch;

	private Tabpanels tabpanels_contatto_inf;

	private Tabs tabs_contatto_inf;
	private Tab presacarico_tab;
	private Tab segnalazione_tab;
	private Tab scaleval_tab;
	private Tab ambulatorio_tab;
	private Tab pa_tab;
	
	
	String id_skso_url = null;
//	String id_skso_hash = null;
//	String id_skso_temp = null;
	String skso_fonte = null;
	String nContattoURL = null;
	String nCartellaURL = null;
	
	private static final int RISPOSTACHIUSURACONTATTO = 1;
	
	private Button btn_accessi_;
	private Button btn_storico;
	private Button btn_riapri;
	private Button btn_rug_svama;
	private Button btn_segnalazione;
	private Button btn_skUVT;
	private Button btn_rfc115_valutazione;
	private Button btn_scheda_so;

	private Tabpanel presacarico;
	private Tabpanel segnalazione;
	private Tabpanel ambulatorio;
	private Tabpanel tabpanel_pa;
	private Component panel_scale;

	// private CaribelRadiogroup ski_les_dec;
	// private CaribelRadiogroup ski_trasm_sk;
	private CaribelRadiogroup ski_dimis_progr;

	private Vector<Hashtable<String, String>> vettOper = new Vector<Hashtable<String, String>>();
	private CaribelDatebox ski_infermiere_da;
	private CaribelTextbox desc_operat;
	private CaribelDatebox dt_presa_carico;
	private CaribelCombobox ski_tipologia;
	private Button btn_protesica;
	private int check_save_step = 0;
	private Label label_dimis_progr_instead;
	private Component patologieGrid;
	protected CaribelListbox tablePrestazioni;
	private Component storico_operatore_ref = null;
	private CaribelTextbox ski_descr_contatto;
	private CaribelTextbox ski_anamnesi_1;
	private CaribelTextbox ski_anamnesi_2;
	private Label btn_protesica_placeholder;
	private Component contattoMedicoPatologia;
	private ISASRecord skso_op_coinvolti;
	private RMSkSOOpCoinvoltiEJB op_CoinvoltiEJB;
	private String ver ="14-";
	private boolean state_before_in_insert = false; 
	boolean messaggioAvvisoCartellaChiusa = true;
	
	public static final String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final String CTS_DATA_APERTURA = "ski_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "ski_data_chiusura";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";

	public void doInitForm() {
		try {
			logger.debug("inizio ");
			super.doInitForm();
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForDelete("deleteAll");
			
			doMakeControl();
			doPopulateCombobox();
			
			if (Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO)!=null)
				id_skso_url = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE)!=null)
				skso_fonte = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE);
			
			if (Executions.getCurrent().getParameter(CostantiSinssntW.N_CONTATTO)!=null)
				nContattoURL = Executions.getCurrent().getParameter(CostantiSinssntW.N_CONTATTO);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA)!=null)
				nCartellaURL = Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA);
			
			
			
			//########################### Recupero chiavi del contatto ###################################################################
			String nCartella 	= "";
			String nContatto 	= "";
			String prData 		= "";
//			Object dtCartellaChiusa =  UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
//			if (ManagerDate.validaData(dtCartellaChiusa+"")){
//				hParameters.put(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA, dtCartellaChiusa);
//			}
			if (ISASUtil.valida(nContattoURL)){
				nContatto = nContattoURL;
			}
			if (ISASUtil.valida(nCartellaURL)){
				nCartella = nCartellaURL;
			}
			
			if (ISASUtil.valida((String)arg.get(CostantiSinssntW.N_CARTELLA)) && ISASUtil.valida((String)arg.get(CostantiSinssntW.N_CONTATTO)) && ISASUtil.valida((String)arg.get(CostantiSinssntW.PR_DATA))) {
				//Arrivo da una chiamata esplicita per cui ho i parametri in arg
				nCartella 	= (String)arg.get(CostantiSinssntW.N_CARTELLA);
				nContatto 	= (String)arg.get(CostantiSinssntW.N_CONTATTO);
				prData 		= (String)arg.get(CostantiSinssntW.PR_DATA);
			}else if (dbrFromList != null) {
				//arrivo da una griglia di contatti (es. storico)				
				nCartella 	= ((Integer)dbrFromList.get(CostantiSinssntW.N_CARTELLA)).toString();
				nContatto 	= ((Integer)dbrFromList.get(CostantiSinssntW.N_CONTATTO)).toString();
				prData 		= UtilForBinding.getValueForIsas((Date) dbrFromList.get("ski_data_apertura"));
			}else if (super.caribelContainerCtrl != null
					&& super.caribelContainerCtrl.hashChiaveValore != null
					&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA) != null && !(ISASUtil.valida(nContatto))) {
				//arrivo da nuovo contatto o da qualunque altro posto
				nCartella = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.N_CARTELLA);
				nContatto = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.N_CONTATTO);
				if (super.caribelContainerCtrl.hashChiaveValore.containsKey(CostantiSinssntW.PR_DATA))
					prData = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.PR_DATA);
				if (ISASUtil.valida(prData) && !ISASUtil.valida(nContatto)) {
					//Recupero il contatto piu recente
					pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
					nContatto = recuperaNContatto(nCartella, pr_data.getValueForIsas());
				}
			}
			//########################### Fine Recupero chiavi del contatto ##############################################################
			
			//Imposto le chiavi trovate
			if(ISASUtil.valida(nCartella)){
				n_cartella.setValue(new Integer(nCartella));
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
//				super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CARTELLA, nCartella);
			}
			if(ISASUtil.valida(nContatto)){
				n_contatto.setValue(new Integer(nContatto));
				hParameters.put(CostantiSinssntW.N_CONTATTO, nContatto);
//				super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, nContatto);
			}
			if(ISASUtil.valida(prData)){
				pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
				hParameters.put(CostantiSinssntW.PR_DATA, prData);
			}
			String dtCartellaChiusa =  ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
			//Eseguo la lettura da DB o mi preparo per l'insert
			if(ISASUtil.valida(nCartella) && 
					(ISASUtil.valida(nContatto) || ManagerDate.validaData(dtCartellaChiusa)) ){
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(nContatto) ){
				//sono in inserimento					
				this.n_contatto.setValue(new Integer(0));
				this.ski_operatore.setValue(getProfile().getIsasUser().getKUser());
				this.desc_operat.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
				caricaPatologie();
			}
			
			doMakeControlAfterRead();
			doFreezeForm();
//			gestionePannelloScale();
			gestioneListaAttivita(false);
			gestionePianiAssistenziali();
			gestioneRichiestaChiusura(false);
		} catch (Exception ex){
			
			if (ex instanceof InvocationTargetException &&((InvocationTargetException) ex).getTargetException().getMessage().equals(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI)){
			gestioneRichiestaChiusura(true);
			}
			else{
			doShowException(ex);
			}
			try{
				this.setReadOnly(true);
				this.currentIsasRecord=null;
			}catch (Exception ex2) {
				doShowException(ex2);
			}
		}
	}
	

	private void gestionePianiAssistenziali() {
		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			if(n_contatto.getValue()!= null && n_contatto.getValue()>0){//verifico che esista un contatto salvato
				pa_tab.setDisabled(false);
				tabpanel_pa.getLinkedTab().setDisabled(false);
				if(tabpanel_pa.getFirstChild()==null){
					HashMap<String, Object> map = new HashMap<String, Object>();			
					map.put("caribelContainerCtrl", this.caribelContainerCtrl);
					map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
					map.put(CostantiSinssntW.SKI_DATA_APERTURA, ski_data_apertura.getValueForIsas());
					if(ski_data_uscita.getValue()!=null){
						map.put("stato", 3);
					}
					Executions.getCurrent().createComponents("/web/ui/sinssnt/piano_assistenziale/pianoAssistenzialeGrid.zul", tabpanel_pa, map);
				}
			}
		}
	}

	private void gestioneListaAttivita(boolean fromDoSaveForm) throws Exception {
		final Hashtable h = new Hashtable();
		
		if (id_skso_url==null) {
			if (!fromDoSaveForm){
				Hashtable h_opCoinvolti = ricercaOpCoinvolti();
				if (h_opCoinvolti!=null){
					skso_fonte = h_opCoinvolti.get("fonte").toString();
					id_skso_url = h_opCoinvolti.get("id_richiesta").toString();
					
				}
			}
		}
		if (skso_fonte != null && this.skso_fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"")){
		op_CoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
		

			h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getText());
			h.put(CostantiSinssntW.CTS_ID_SKSO,id_skso_url);
			h.put(CostantiSinssntW.CTS_FONTE,skso_fonte);
			h.put(CostantiSinssntW.TIPO_OPERATORE,CostantiSinssntW.TIPO_OPERATORE_INFERMIERE);			
			skso_op_coinvolti = invokeSuEJB(op_CoinvoltiEJB, h , "queryKey");

			
			if ((skso_op_coinvolti !=null) && (skso_op_coinvolti.get("dt_presa_carico")!=null)){
				Messagebox.show(
						Labels.getLabel("Contatti.SchedaSO.presa_carico_effettuata",new String[]{
								UtilForBinding.getStringClientFromDate((Date) skso_op_coinvolti.get("dt_presa_carico")),
								skso_op_coinvolti.get("cod_operatore").toString()}),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.EXCLAMATION);
				return;
			}
				
			if (this.isInUpdate() && !fromDoSaveForm && (id_skso.getText().equals(id_skso_url) || id_skso.getText().equals(""))){
				//contatto già legato alla scheda so, visualizza messaggio di conferma.
				if (id_skso.getText().equals(id_skso_url)){
					scriviPresaCarico(h, true);
				}
				else{
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_esistente",new String[]{ski_data_apertura.getText(),desc_operat.getText()}), 
						Labels.getLabel("messagebox.attention"),
						Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
						new EventListener<Event>() {
							public void onEvent(Event event)throws Exception {
								if (Messagebox.ON_YES.equals(event.getName())){
									id_skso.setValue(new Integer(id_skso_url));
									recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
									scriviPresaCarico(h,true);
								}
								if (Messagebox.ON_NO.equals(event.getName())){
									id_skso_url=null;
									skso_fonte=null;
									Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
									Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
									return;			
								}						
							}							
						});
					}
				}
			else if (this.isInUpdate()&& !fromDoSaveForm && id_skso!=null && !id_skso.getText().equals("") && !id_skso.getText().equals(id_skso_url)){
				//contatto già legato ad un'altra scheda so, visualizza messaggio di errore.
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_utilizzato",new String[]{ski_data_apertura.getText(),desc_operat.getText()}), 
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.EXCLAMATION);
				id_skso_url=null;
				skso_fonte=null;
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				return;								
				
			} else if (state_before_in_insert && fromDoSaveForm){
				scriviPresaCarico(h,false);
				recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
				id_skso_url=null;
				skso_fonte=null;
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				return;
			}
			
			
			
		}
		else if (skso_fonte != null && skso_fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"")){
			 
			if (this.isInUpdate() && !fromDoSaveForm && (id_skso.getText().equals(id_skso_url) || id_skso.getText().equals("")))
				scriviPrimaVisita(false);
			
			else if (this.isInUpdate() && !fromDoSaveForm && !id_skso.getText().equals(id_skso_url)){
				Messagebox.show(
						Labels.getLabel("Contatti.SchedaSO.contatto_in_uso_altra_skso"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.EXCLAMATION);
				id_skso_url=null;
				skso_fonte=null;
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				return;
			}
			
			if(state_before_in_insert && fromDoSaveForm){
				scriviPrimaVisita(false);				
			}
		}else {
			if(fromDoSaveForm){
				scriviPrimaVisita(true);
			}
		}
//		if (isInInsert()&&!fromDoSaveForm && id_skso_url!=null){
		if (!fromDoSaveForm && (id_skso_url!=null || (id_skso!=null && id_skso.getValue()!=null) )) {
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
		} 
	}  

	

	private boolean scriviPrimaVisita(boolean fromDoSaveFormNofonte) throws Exception{
		
		String data_pc = ski_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(ski_infermiere.getText())){
			 //VFR se entro nel contatto e salvo non voglio che mi sovrascriva l'operatore, non so come si deve comportare con la prima visita e quindi quello lo lascio uguale
			if(!fromDoSaveFormNofonte){ 
				ski_infermiere.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
				ski_infermiere_da.setValue(Calendar.getInstance().getTime());
			}
			data_pc = ski_infermiere_da.getValueForIsas();
		}
		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
		return scriviPrimaVisita(data_pc, this.n_cartella.getValue().toString(), this.id_skso_url, true);		
		}

	


	protected void scriviPresaCarico(Hashtable h, boolean saveForm) throws Exception  {
		String punto = ver  + "scriviPresaCarico ";
		String data_pc = ski_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(ski_infermiere.getText())){
			ski_infermiere.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			ski_infermiere_da.setValue(Calendar.getInstance().getTime());
			data_pc = ski_infermiere_da.getValueForIsas();
		}
		skso_op_coinvolti.put("dt_presa_carico", data_pc);
		skso_op_coinvolti.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, CostantiSinssntW.CTS_FLAG_STATO_FATTA);
		skso_op_coinvolti.put(CostantiSinssntW.COD_OPERATORE_PC, getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		try{
		updateSuEJB(op_CoinvoltiEJB, skso_op_coinvolti);
		// effettuo anche la eventuale prima visita
		scriviPrimaVisita(data_pc, this.n_cartella.getValue().toString(), this.id_skso_url, false);
		if (saveForm) {
			id_skso_url=null;
			skso_fonte=null;
			Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
			Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);			
			doSaveForm();
		}
		}catch (Exception e){
			String id_skso = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO);
			ISASRecord skso_op_coinvolti_new = invokeSuEJB(op_CoinvoltiEJB, h , "queryKey");
			Messagebox.show(
					Labels.getLabel("Contatti.SchedaSO.presa_carico_effettuata",new String[]{
							UtilForBinding.getStringClientFromDate((Date) skso_op_coinvolti_new.get("dt_presa_carico")),
							skso_op_coinvolti_new.get("cod_operatore").toString()}),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.EXCLAMATION);
			return;
		}
		
		Clients.showNotification(Labels.getLabel("Contatti.SchedaSO.presa_carico_successo"));
		
		
//		Messagebox.show(
//				Labels.getLabel("Contatti.SchedaSO.presa_carico_successo"),
//				Labels.getLabel("messagebox.attention"),
//				Messagebox.OK,
//				Messagebox.EXCLAMATION);
		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
		
		return;
	}


//	private void recuperaDatiSettaggio_() throws CariException {
//		String punto = ver + "recuperaDatiSettaggio ";
//		if (this.currentIsasRecord == null && !isDataValida(ski_infermiere_da)) {
//			ski_infermiere_da.setValue(ski_data_apertura.getValue());
//		}
//		
//		recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
//		
//		Hashtable<String,String> datiDaInviare = new Hashtable<String,String>();
//		String idSkSo = (id_skso.getValue() !=null ? id_skso.getValue()+"":"");
//		datiDaInviare.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
//		datiDaInviare.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
//		logger.debug(punto + " dati che invio>>"+ datiDaInviare);
////		if (ISASUtil.valida(id_skso_url)){
//			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
//			ISASRecord dbrValutazione = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(), datiDaInviare);
//			String codRichiedente = ISASUtil.getValoreStringa(dbrValutazione, "richiedente");
//			String tipoUte = ISASUtil.getValoreStringa(dbrValutazione, "tipo_ute");
////			String prMotivo = ISASUtil.getValoreStringa(dbrValutazione, "pr_motivo");
////			String prMotivo = ISASUtil.getValoreStringa(dbrValutazione, "pr_motivo");
//			String tipocure = ISASUtil.getValoreStringa(dbrValutazione, "tipocura");
//			
//			logger.debug(punto + " dati codRichiedente>"+codRichiedente+"<codRichiedente>"+codRichiedente+"< tipocure>"+ tipocure);
//			cbx_provenienza.setSelectedValue(codRichiedente.equals("")?"9":codRichiedente);
//			cbx_utenza.setSelectedValue(tipoUte);
//			cbx_motivo.setSelectedValue(tipocure);
//			cbx_provenienza.setRequired(!cbx_provenienza.getSelectedValue().equals(""));
//			cbx_utenza.setRequired(!tipoUte.equals(""));
//			cbx_provenienza.setDisabled(true);
//			cbx_utenza.setDisabled(true);
//			if (ISASUtil.valida(tipocure)){
//				cbx_motivo.setDisabled(true);
//			}else {
//				cbx_motivo.setDisabled(false);
//			}
//			
//			if (!this.ski_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
//			this.ski_descr_contatto.setValue(ski_descr_contatto.getValue()+CostantiSinssntW.DA_SCHEDA_SO);
//			
////		}
//	}



	private void doMakeControlAfterRead() throws WrongValueException, Exception {
		if (getProfile().getStringFromProfile("ctrl_skinf_siad").equals(CostantiSinssntW.SI) && 
				isInUpdate() && cbx_motivo.isRequired())
		{
			if (cbx_motivo.getSelectedValue().equals(""))
			cbx_motivo.setDisabled(false);
			else cbx_motivo.setDisabled(true);
		}
		else cbx_motivo.setDisabled(false);
			
		//setto se prima valutazione o rivalutazione (x Toscana, RFC115)
		if (ManagerProfile.isConfigurazioneToscana(getProfile()) && 
				getProfile().getStringFromProfile("rfc115_6").equals(CostantiSinssntW.SI))
			settaValutazione();
			
		// simone 14/08/14 i metodi seguenti sono stati spostati nella gestisciLeftMenu del containerInf
//		checkMotivoxFlussi();
//        checkEsistePresaCar();
//        checkEsistePianoAssist();
//        faiMsgNoGestFlussi();
        
		
		
		
		//gestisco personalizzazioni
		gestisciPersonalizzazioni();
		
		// gestisco l'abilitazione/disabilitazione dei vari componenti, a seconda dello stato della maschera
		abilitazioniMaschera();
		if (panel_scale != null){ 
			panel_scale.detach();
		}
	}



	private void gestisciPersonalizzazioni() throws Exception {
		 //elisa b 26/01/11: aggiungo il pannello solo se non siamo in lazio
        //mod elisa b 16/03/11 aggiunta personalizzazione x abruzzo
       if(ManagerProfile.isConfigurazioneMolise(getProfile()) ||
    		   ManagerProfile.isConfigurazioneMarche(getProfile()) ||
    		   ManagerProfile.isConfigurazioneAbruzzo(getProfile()) 
    		   ){
           
            	Hashtable htAssAnagrafica = new Hashtable();
          
            	htAssAnagrafica.put("ass_anagr_cognome",(String)super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ASSISTITO_COGNOME));
            	htAssAnagrafica.put("ass_anagr_nome",(String)super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ASSISTITO_NOME));
            	htAssAnagrafica.put("ass_anagr_data_nascita", super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.DATA_NASC));
            	htAssAnagrafica.put("ass_anagr_com_nascita", (String)super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.COD_COM_NASC));

            	htAssAnagrafica.put("n_cartella", (String)n_cartella.getText());

              // 07/10/08: lettura solo da ASS_ANAGRAFICA
              // 02/03/09: solo rec NON presi in carico
               Hashtable<String,String> htAssAnagrResult = null;
			try {
				htAssAnagrResult = (Hashtable<String, String>)invokeGenericSuEJB(new RLSkPuacEJB(), htAssAnagrafica, "query_getAssAnagrAge");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

               if ((htAssAnagrResult != null) && (!htAssAnagrResult.isEmpty())){
                 if(isInInsert()){
                    logger.debug("++++ htAssAnagrResult NON NULLA");
                    String segnalante = (String)htAssAnagrResult.get("arrivato");
                    if ((segnalante != null) && (!segnalante.trim().equals(""))){
                      cbx_provenienza.setValue(segnalante);
                      System.out.println("segnalante " + segnalante);
                   }
                }else{
                	cbx_provenienza.setDisabled(true);
                	cbx_provenienza.setRequired(false);
                }
              }
               
            
          }else if (ManagerProfile.isConfigurazioneToscana(getProfile())){
          // TODO: inserire gestione pannello ambulatorio e scale di valutazione, solo per toscana
        	 
        	  
        	  
   
        }else if (ManagerProfile.isConfigurazioneLazio(getProfile())){ // equivalente di getDatiUV_RL
        	
                //29/03/11 si va a leggere anche il valore della tipologia
                Hashtable<String,String> ht = new Hashtable<String,String>();

                ht.put("n_cartella",n_cartella.getText());
                ht.put("pr_data",this.caribelContainerCtrl.hashChiaveValore.get("pr_data").toString());
                if(!ski_data_apertura.getText().isEmpty())
                    ht.put("data_apertura",ski_data_apertura.getText());
                if(!ski_data_uscita.getText().isEmpty())
                    ht.put("data_chiusura",ski_data_uscita.getText());

                  Hashtable<String,String> hPUA = null;
				try {
					hPUA = (Hashtable<String, String>)invokeGenericSuEJB(myEJB, ht, "getDatiRLPuaUvm");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                 System.out.println("++++ hPUA NON NULLA " + hPUA.toString());
                
                 if(!hPUA.get("tipologia").toString().equals("")){
                    cbx_tipologia.setValue(hPUA.get("tipologia").toString());
                    
                     cbx_tipologia.setDisabled(true);                     
                }
                 if(!hPUA.get("motivo").toString().equals("")){
                	 ski_motivo_txt.setText(hPUA.get("motivo").toString());
                	 ski_motivo_txt.setDisabled(true);
                   
                }
                if(!hPUA.get("segnalante").toString().equals("")){
                     cbx_provenienza.setValue(hPUA.get("segnalante").toString());
                     cbx_provenienza.setDisabled(true);
                     cbx_provenienza.setRequired(false);
                }
        }
		
	}


	private void onChange$cbx_modalita(ForwardEvent e){
		 if (cbx_modalita.getSelectedIndex() == 1)
			  btn_rfc115_valutazione.setDisabled(true);
			   else   btn_rfc115_valutazione.setDisabled(false);
	}

//	private void onSchedaSO() throws Exception{
//		Hashtable h = new Hashtable();
//		h.put("n_cartella", n_cartella.getValue().toString());
//		if (id_skso.getValue()!=null)
//			{
//				String id_sk = id_skso.getValue().toString();
//				h.put(CostantiSinssntW.CTS_ID_SKSO, id_sk);
//				h.put(CostantiSinssntW.ACTION, new Integer(CostantiSinssntW.CONSULTA_RICHIESTA));
//				h.put("caribelContainerCtrl", super.caribelContainerCtrl);
//				temp_container_hash = (Hashtable) super.caribelContainerCtrl.hashChiaveValore.clone();
//				 sofc = Executions.getCurrent().createComponents(SegreteriaOrganizzativaFormCtrl.CTS_FILE_ZUL, self, h);
//				 sofc.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
//						public void onEvent(Event event) throws Exception{
//							onCloseSchedaSO();
//						}
//					});		
//				 
//			}
//		else{
//		ISASRecord dbr = (ISASRecord) invokeGenericSuEJB(new RMSkSOEJB(), h,"selectSkValCorrente");
//		if (dbr != null && dbr.get(CostantiSinssntW.CTS_ID_SKSO)!=null){
//			String id_sk = dbr.get(CostantiSinssntW.CTS_ID_SKSO).toString();
//			h.put(CostantiSinssntW.CTS_ID_SKSO, id_sk);
//			h.put(CostantiSinssntW.ACTION, new Integer(CostantiSinssntW.CONSULTA_RICHIESTA));
//			h.put("caribelContainerCtrl", super.caribelContainerCtrl);
//			temp_container_hash = (Hashtable) super.caribelContainerCtrl.hashChiaveValore.clone();
//			 sofc = Executions.getCurrent().createComponents(SegreteriaOrganizzativaFormCtrl.CTS_FILE_ZUL, self, h);
//			 sofc.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
//					public void onEvent(Event event) throws Exception{
//						onCloseSchedaSO();
//					}
//				});		
//			 
//		}
//				else Messagebox.show(
//				Labels.getLabel("SchedaInfForm.msg.no_scheda_so_attiva"),
//				Labels.getLabel("messagebox.attention"),
//				Messagebox.OK,
//				Messagebox.EXCLAMATION);  
//          return;
//	}
//	}
//	
//	private void onCloseSchedaSO(){
//		super.caribelContainerCtrl.hashChiaveValore.clear();
//		super.caribelContainerCtrl.hashChiaveValore.putAll(temp_container_hash);
//		
//	}
	public boolean isContattoInfChiuso()
	{
		return ski_data_uscita.getText() != null && !ski_data_uscita.getText().isEmpty();
	}
	
	public boolean isContattoInfAperto()
	{
		return ski_data_apertura.getText()!= null && !ski_data_apertura.getText().isEmpty();
	}

	



	private void settaValutazione() {
		 btn_rfc115_valutazione.setVisible(true);
		 btn_rfc115_valutazione.setLabel(Labels.getLabel("SchedaInfForm.button.PrimaValutazione"));
		 Hashtable<String,String> h = new Hashtable<String,String>();
         h.put("n_cartella",this.n_cartella.getValue().toString());
         String dtSkVal = ISASUtil.getValoreStringa(
					super.caribelContainerCtrl.hashChiaveValore,
					CostantiSinssntW.PR_DATA);
         if (dtSkVal!=null)
              h.put("pr_data",dtSkVal);
         else h.put("pr_data","");
         

         try {
			h = (Hashtable<String, String>)invokeGenericSuEJB(new RtValutazSanEJB(), h, "getCasoRif");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         btn_rfc115_valutazione.setAttribute("origine", h.get("origine").toString());
         btn_rfc115_valutazione.setAttribute("tempo_t", h.get("tempo_t").toString());
         
         
           if (checkPrimaValutazione() && (this.dt_presa_carico!=null && this.dt_presa_carico.getValue()!=null))
           btn_rfc115_valutazione.setLabel(Labels.getLabel("SchedaInfForm.button.Rivalutazione"));
	}



	private boolean checkPrimaValutazione() {
		Hashtable<String,String> h = new Hashtable<String,String>();
		h.put("n_cartella",n_cartella.getValue().toString());
		boolean esisteCasoAttivo = (this.dt_presa_carico!=null && this.dt_presa_carico.getValue()!=null);
        if (!esisteCasoAttivo) h.put("dt_presa_carico", ski_data_apertura.getValueForIsas());
        try {
			return ((Boolean)invokeGenericSuEJB(new RtValutazSanEJB(), h, "query_PrimaValutazione")).booleanValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}



	private String recuperaNContatto(String nCartella, String prData)
			throws Exception {
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.PR_DATA, prData);

		ISASRecord dbrSkMed = null;
//		try {
			dbrSkMed = invokeSuEJB(myEJB, dati, "getContattoInfCorrente");
//		} catch (Exception e) {
//			doShowException(e);
//		}
		String nContatto = ISASUtil.getValoreStringa(dbrSkMed,
				CostantiSinssntW.N_CONTATTO);

		return nContatto;
	}

	private void loadTipologia(CaribelCombobox cbx) throws Exception {
		CaribelComboRepository.addComboItem(cbx, "1",
				Labels.getLabel("SchedaFisioForm.combo.tipologia.occasionale"));
		CaribelComboRepository.addComboItem(cbx, "2",
				Labels.getLabel("SchedaFisioForm.combo.tipologia.apc"));
		CaribelComboRepository.addComboItem(cbx, "3",
				Labels.getLabel("SchedaFisioForm.combo.tipologia.uvd"));
	}

	

	private void doMakeControl() {
		if (ManagerProfile.isAbilitazione(getProfile(),
				ManagerProfile.ABILITA_SCALA)) {
			// TODO METTERE AMBULATORIO , PRESA CARICO E
			// PANNELLO SEGNALAZIONE SOLO PER LA TOSCANA.
			
		}else {
			//nascondo pannello scale
			tabpanels_contatto_inf.removeChild(panel_scale);
			tabs_contatto_inf.removeChild(scaleval_tab);
		}

		// nascondo componenti opzionali
		cbx_modalita.setVisible(false);
		cbx_modalita.setRequired(false);
		cbx_tipologia.setVisible(false);
		cbx_tipologia.setRequired(false);
		label_prima_btn_accessi.setVisible(false);
		label_segnalazione.setVisible(true);
		btn_segnalazione.setVisible(false);
		ski_motivo_txt.setVisible(false);
		ski_motivo_txt.setRequired(false);
		btn_rfc115_valutazione.setVisible(false);
		
		label_dimis_progr.setVisible(false);
		label_dimis_progr_instead.setVisible(true);
		ski_dimis_progr.setVisible(false);
		cbx_motchiusura.setVisible(false);
		btn_scheda_so.setVisible(false);
		
		btn_rug_svama.setLabel(Labels.getLabel("SchedaInfForm.buttonSvama"));
		btn_skUVT.setVisible(getProfile().getStringFromProfile(
				"abl_skuvt_skpuac").equals(CostantiSinssntW.SI));
		insteadof_btn_skUVT.setVisible(!getProfile().getStringFromProfile(
				"abl_skuvt_skpuac").equals(CostantiSinssntW.SI));

		if (ManagerProfile.isConfigurazioneMarche(getProfile())) {
			label_modalita_tipologia.setValue("");
			label_prima_btn_accessi.setVisible(true);
			label_prima_btn_accessi.setValue("");
			//btn_rug_svama.setLabel(Labels.getLabel("SchedaInfForm.buttonRUG"));
			btn_rug_svama.setVisible(false);
			//rimuovo i pannelli x toscana
			tabpanels_contatto_inf.removeChild(ambulatorio);
			tabpanels_contatto_inf.removeChild(segnalazione);
			//tabpanels_contatto_inf.removeChild(scaleval);
			tabpanels_contatto_inf.removeChild(presacarico);
			// rimuovo le tab x toscana
			tabs_contatto_inf.removeChild(presacarico_tab);
			tabs_contatto_inf.removeChild(segnalazione_tab);
			//tabs_contatto_inf.removeChild(scaleval_tab);
			tabs_contatto_inf.removeChild(ambulatorio_tab);
			//rimuovo i componenti non desiderati
//			cbx_motivo.setRequired(false);	
			cbx_motivo.setVisible(true);
			//	VFR segnalazione mail di David 22/03/201 punto 2 rimosso per evitare che si possa scrivere liberamente nel campo e al salvataggio si perde l'informazione 
			//cbx_motivo.setReadonly(false);cbx_motivo.invalidate();	
			sezione_ospedale.setVisible(false);
			btn_protesica.setVisible(false);
			btn_protesica_placeholder.setVisible(true);
			ski_descr_contatto.setRequired(false);
			UtilForBinding.setComponentReadOnly(contattoMedicoPatologia,true);
			ski_anamnesi_2.setDisabled(true);
			ski_anamnesi_1.setDisabled(true);
			btn_scheda_so.setVisible(true);
			
			
		} else if (ManagerProfile.isConfigurazioneMolise(getProfile())) {
			label_modalita_tipologia.setValue("");
			label_prima_btn_accessi.setVisible(true);
			label_prima_btn_accessi.setValue("");
			//rimuovo i pannelli x toscana
			tabpanels_contatto_inf.removeChild(ambulatorio);
			tabpanels_contatto_inf.removeChild(segnalazione);
			//tabpanels_contatto_inf.removeChild(scaleval);
			tabpanels_contatto_inf.removeChild(presacarico);
			// rimuovo le tab x toscana
			tabs_contatto_inf.removeChild(presacarico_tab);
			tabs_contatto_inf.removeChild(segnalazione_tab);
			//tabs_contatto_inf.removeChild(scaleval_tab);
			tabs_contatto_inf.removeChild(ambulatorio_tab);
			//rimuovo i componenti non desiderati da Regione Marche  
			//(da rimuovere quando sarà sistemata la configurazione adesso MARCHE=MOLISE
//			cbx_motivo.setRequired(false);			
			sezione_ospedale.setVisible(false);
			btn_protesica.setVisible(false);
			btn_protesica_placeholder.setVisible(true);
			ski_descr_contatto.setRequired(false);
			UtilForBinding.setComponentReadOnly(contattoMedicoPatologia,true);
			ski_anamnesi_2.setDisabled(true);
			ski_anamnesi_1.setDisabled(true);

		} else if (ManagerProfile.isConfigurazioneLazio(getProfile())) {
			cbx_tipologia.setVisible(true);
			cbx_tipologia.setRequired(true);
			ski_motivo_txt.setVisible(true);
			ski_motivo_txt.setRequired(true);
			cbx_motivo.setVisible(false);
//			cbx_motivo.setRequired(false);
			label_modalita_tipologia.setValue(Labels.getLabel("SchedaInfForm.principale.Tipologia"));
			//rimuovo i pannelli x toscana
			tabpanels_contatto_inf.removeChild(ambulatorio);
			tabpanels_contatto_inf.removeChild(segnalazione);
			//tabpanels_contatto_inf.removeChild(scaleval);
			tabpanels_contatto_inf.removeChild(presacarico);
			// rimuovo le tab x toscana
			tabs_contatto_inf.removeChild(presacarico_tab);
			tabs_contatto_inf.removeChild(segnalazione_tab);
			//tabs_contatto_inf.removeChild(scaleval_tab);
			tabs_contatto_inf.removeChild(ambulatorio_tab);

		} else if (ManagerProfile.isConfigurazioneToscana(getProfile())) {
			cbx_modalita.setVisible(true);
			cbx_modalita.setRequired(true);
			label_segnalazione.setVisible(false);
			btn_segnalazione.setVisible(true);
			label_dimis_progr.setVisible(true);
			label_dimis_progr_instead.setVisible(false);
			ski_dimis_progr.setVisible(true);
			cbx_motchiusura.setVisible(true);
			// scaleval.setVisible(true);
			label_modalita_tipologia.setValue(Labels
					.getLabel("SchedaInfForm.principale.Modalita"));
			
			
			
		} else if (ManagerProfile.isConfigurazioneAbruzzo(getProfile())) {
			label_modalita_tipologia.setValue("");
			label_prima_btn_accessi.setVisible(true);
			label_prima_btn_accessi.setValue("");
			//rimuovo i pannelli x toscana
			tabpanels_contatto_inf.removeChild(ambulatorio);
			tabpanels_contatto_inf.removeChild(segnalazione);
			//tabpanels_contatto_inf.removeChild(scaleval);
			tabpanels_contatto_inf.removeChild(presacarico);
			// rimuovo le tab x toscana
			tabs_contatto_inf.removeChild(presacarico_tab);
			tabs_contatto_inf.removeChild(segnalazione_tab);
			//tabs_contatto_inf.removeChild(scaleval_tab);
			tabs_contatto_inf.removeChild(ambulatorio_tab);

		}
		btn_scheda_so.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			public void onEvent(Event event) throws Exception{
				onSchedaSO();
			}
		});		

	}

	public void onAccessi(ForwardEvent e) throws Exception {

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("n_cartella", n_cartella.getValue().toString());
		map.put("n_contatto", n_contatto.getValue().toString());
		
		
//		ski_data_apertura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
//		ski_data_uscita
		
		String dtContattoApertura = "";
		if(ManagerDate.validaData(ski_data_apertura)){
			dtContattoApertura = ski_data_apertura.getValueForIsas();
		}
		String dtContattoChiusura = "";
		if(ManagerDate.validaData(ski_data_uscita)){
			dtContattoChiusura = ski_data_uscita.getValueForIsas();
		}
		
		map.put(CostantiSinssntW.CTS_DT_CONTATTO_APERTURA, dtContattoApertura);
		map.put(CostantiSinssntW.CTS_DT_CONTATTO_CHIUSURA, dtContattoChiusura);
		
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_INFERMIERE);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		if (vettOper != null && vettOper.size() > 0) {
			map.put("vettOper", vettOper);
		}
		Executions.getCurrent().createComponents(
				AccessiEffettuatiGridCtrl.myPathFormZul, self, map);
		
		
	}

	public void onProtesica(ForwardEvent e) throws Exception {

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("n_cartella", n_cartella.getValue().toString());
		map.put("n_contatto", n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_INFERMIERE);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		if (vettOper != null && vettOper.size() > 0) {
			map.put("vettOper", vettOper);
		}
		Executions.getCurrent().createComponents(
				RiepilogoAusiliProtesicaGridCtrl.myPathFormZul, self, map);

	}

	public void onRiapri(ForwardEvent e) throws Exception {

		// ctrl skValutazione aperta		
			boolean isSkValChiusa = ((Boolean)this.caribelContainerCtrl.hashChiaveValore.get("isSkValChiuso")).booleanValue();
			if (isSkValChiusa)
			{
				Messagebox.show(
	    				Labels.getLabel("SchedaInfForm.msg.ValutazioneChiusa"),
	    				Labels.getLabel("messagebox.attention"),
	    				Messagebox.OK,
	    				Messagebox.EXCLAMATION);  
	              return;
			}
		

		// ctrl non esistenza di altri contatti successivi
		if (!getProfile().getStringFromProfile("abil_newcont_inf").equals("SI"))
		{
			if (checkContattiSuccessivi(false))
			{
				Messagebox.show(
	    				Labels.getLabel("SchedaInfForm.msg.contattiSuccessiviPresenti"),
	    				Labels.getLabel("messagebox.attention"),
	    				Messagebox.OK,
	    				Messagebox.EXCLAMATION);  
	              return;
				
			}
		}

		// ripulisco campi chiusura
		this.ski_data_uscita.setText(null);
		//this.ski_data_uscita.setValue(null);
		this.cbx_motchiusura.setSelectedIndex(-1);
		this.txt_motchiusura.setValue("");
		
		
		// aggiorno il record
        boolean risu = doSaveForm();

        this.setReadOnly(false);
        ski_operatore.setDisabled(true);
        desc_operat.setDisabled(true);
    	
        
//        doMakeControlAfterRead();
//		
//        panel_scale.detach();
//		doFreezeForm();
//		gestionePannelloScale();

	}

	// 14/04/08
	private boolean checkContattiSuccessivi(boolean soloAperti) throws Exception
	{
		Hashtable<String,String> h_ctrl = new Hashtable<String,String>();
		h_ctrl.put("n_cartella", (String)this.n_cartella.getText().trim());
		h_ctrl.put("dataRif", (String)this.ski_data_apertura.getText());
		h_ctrl.put("soloAperti", (soloAperti?"S":"N"));
		return ((Boolean)invokeGenericSuEJB(myEJB, h_ctrl, "query_checkContSuccessivi")).booleanValue();
	}

	public void onStorico(ForwardEvent e) throws Exception {
		
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);

		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerInfermieristicoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerInfermieristicoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerInfermieristicoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}

		map.put(StoricoInfermiereReferenteGridCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
		map.put("zul_chiamante", (ContattoInfFormCtrl)this);//Utile per l'aggiornamento da finestra modale
		//TODO non permettere la riapertura se la finestra è gia aperta
		if (storico_operatore_ref == null) {
			storico_operatore_ref = Executions.getCurrent().createComponents(StoricoInfermiereReferenteGridCtrl.myPathFormZul, self, map);
			storico_operatore_ref = null;
		} 
	}


	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put(CTS_DATA_APERTURA, ski_data_apertura.getValue());
		if (ski_data_uscita != null)
			map.put(CTS_DATA_CHIUSURA, ski_data_uscita.getValue());
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		if (vettOper != null && vettOper.size() > 0) {
			map.put("vettOper", vettOper);
		}
		try {
			patologieGrid = Executions.getCurrent().createComponents(
					DiagnosiFormCtrl.myPathFormZul, self, map);
			patologieGrid.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public void onEvent(Event event) throws Exception{
					caricaPatologie();
					doFreezeForm();
				}
			});
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
 
	
	private void caricaPatologie() throws Exception {
		CaribelListModel lm = new CaribelListModel();
		Hashtable h = new Hashtable();
		h.put("data_apertura", ski_data_apertura.getValueForIsas());
		if (ski_data_uscita != null)
			h.put("data_chiusura", ski_data_uscita.getValueForIsas());
		h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
//		lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryLastDiagContesto"));
		lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryStoricoDiag"));
		tablePrestazioni.setModel(lm);
	}
	
	private void doPopulateCombobox() throws Exception {
		// caricamento combobox cbx_modalita (solo x toscana)
		CaribelComboRepository.addComboItem(cbx_modalita, "1",
				Labels.getLabel("SchedaInfForm.combo.toscana.modalita.1"));
		CaribelComboRepository.addComboItem(cbx_modalita, "2",
				Labels.getLabel("SchedaInfForm.combo.toscana.modalita.2"));
		loadTipologia(ski_tipologia);
		
		
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
		
		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SEGNALANTE, cbx_provenienza);
		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_ICHIUS, cbx_motchiusura);
//		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_MOTIVO, cbx_motivo);
		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
				"tab_descrizione", false);

		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(cbx_motivo);
		
//		Hashtable<String, String> mot_chiu = new Hashtable<String, String>();
//		mot_chiu.put("tab_cod", "ICHIUS");
//		CaribelComboRepository.comboPreLoad("contatto_inf_chiusura",
//				new TabVociEJB(), "query", mot_chiu, cbx_motchiusura, null,
//				"tab_val", "tab_descrizione", false);

		CaribelComboRepository.comboPreLoad("contatto_inf_tiputes",
				new TiputeSEJB(), "query", new Hashtable<String, String>(), cbx_utenza, null,
				"codice", "descrizione", false);
//		Hashtable<String, String> segnala = new Hashtable<String, String>();
//		segnala.put("contesto", CostantiSinssntW.TIPO_OPERATORE_INFERMIERE);
//		CaribelComboRepository.comboPreLoad("contatto_inf_segnala",
//				new SegnalaEJB(), "query_contesto", segnala, cbx_provenienza,
//				null, "codice", "descrizione", false);

//		Hashtable<String, String> motivo = new Hashtable<String, String>();
//		motivo.put("tab_cod", "MOTIVO");
//		CaribelComboRepository.comboPreLoad("contatto_inf_motivo",
//				new TabVociEJB(), "query", motivo, cbx_motivo,
//				null, "tab_val", "tab_descrizione", false);
	}

	private void abilitazioniMaschera() throws WrongValueException, Exception {
		boolean contattoDisabilitato = false;
		boolean abilitaBottoneRiapri = abilitaBottoneRiapri();
		
		verificaStatoCartella();
		
		if (isInInsert()){
			if(ManagerProfile.getTipoOperatore(getProfile()).equals(UtilForContainer.getTipoOperatorerContainer())){
				ski_infermiere.setValue(ManagerProfile.getCodiceOperatore(getProfile()));
				desc_inf.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
			}
			String codPresidio = getProfile().getStringFromProfile(
					ManagerProfile.PRES_OPERATORE);
			ski_cod_presidio.setValue(codPresidio);
			desc_presidio.setValue(ManagerDecod.decodPresidi(getProfile(),
					codPresidio));
//			btn_accessi.setDisabled(true);
			btn_storico.setDisabled(true);
			btn_rug_svama.setDisabled(true);
			ski_data_uscita.setDisabled(true);
			cbx_motchiusura.setDisabled(true);
			btn_segnalazione.setDisabled(true);
			btn_protesica.setDisabled(true);
			boolean proporreDtScheda = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_PROPORRE_DT_SCHEDA);
			Date dataAccettazioneSkSo = recuperaDataAccettazioneSkSo(n_cartella, id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
			if (ManagerDate.validaData(dataAccettazioneSkSo)) {
				ski_data_apertura.setValue(dataAccettazioneSkSo);
				ski_infermiere_da.setValue(dataAccettazioneSkSo);
			} else {
				if (proporreDtScheda) {
					if (!anagraficaChiusa(this.hParameters, ski_data_apertura, ski_infermiere_da)){
//						ski_data_apertura.setConstraint("after "+ UtilForComponents.formatDateforDatebox(pr_data.getValue()));
						ski_data_apertura.setValue(procdate.getDate());
						ski_infermiere_da.setValue(procdate.getDate());
					}
				}
			}
		}
		if (isInUpdate()){
//			btn_accessi.setDisabled(false);
			btn_storico.setDisabled(false);
			try {
				contattoDisabilitato = id_skso.getValue()>0;
			} catch (Exception e) {
				
			}
//			ski_infermiere.setDisabled(true);
//			ski_infermiere_da.setDisabled(true);
			((CaribelSearchCtrl) cs_operatore_referente.getAttribute(MY_CTRL_KEY)).setReadonly(true);
//			((CaribelSearchCtrl) cs_presidio.getAttribute(MY_CTRL_KEY)).setReadonly(true);
			
			if (this.ski_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
			{
				cbx_provenienza.setDisabled(true);				
				cbx_utenza.setDisabled(true);
//				cbx_motivo.setDisabled(true);
//				cbx_motivo.setRequired(false);
				contattoDisabilitato= true;
				cbx_provenienza.setRequired(false);				
				cbx_utenza.setRequired(false);
			}
		}
		
		btn_riapri.setDisabled( (isInInsert() || (isInUpdate() && !isContattoInfChiuso() && abilitaBottoneRiapri ) ));
		boolean abilChiu = ((getProfile().getIsasUser().canIUse(ManagerProfile.CLOSE_SCHEDA_INF,"MODI"))
				&& (!isContattoInfChiuso()) && (isContattoInfAperto()));
		
		this.ski_data_uscita.setDisabled(!abilChiu);
		this.cbx_motchiusura.setDisabled(!abilChiu);
		this.txt_motchiusura.setDisabled(!abilChiu);
		
		if (isContattoInfChiuso()){
			this.setReadOnly(true);
			btn_riapri.setDisabled(false);
//			btn_accessi.setDisabled(false);
			btn_rug_svama.setDisabled(false);
			btn_segnalazione.setDisabled(false);
			btn_protesica.setDisabled(false);
			contattoDisabilitato= true;
			
			boolean abilRiap = (getProfile().getIsasUser().canIUse(ManagerProfile.REOPEN_SCHEDA_INF,"MODI")) ;
			this.btn_riapri.setVisible(abilRiap);
			this.btn_riapri.setDisabled(!abilitaBottoneRiapri);
			
		}
		//passaggio di parametri a container per successivi controlli e abilitazioni pulsanti sinistra
		if (!isInInsert())
		super.caribelContainerCtrl.hashChiaveValore.put(
				CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		super.caribelContainerCtrl.hashChiaveValore.put(
				"contattoInfInUpdate", new Boolean(isInUpdate())
				);
		super.caribelContainerCtrl.hashChiaveValore.put(
				"contattoInfInInsert", new Boolean(isInInsert())
				);
		super.caribelContainerCtrl.hashChiaveValore.put(
				"contattoInfReadOnly", new Boolean(isReadOnly())
				);
		if (cbx_motivo.isRequired()){
		super.caribelContainerCtrl.hashChiaveValore.put(
				"cbx_motivo", cbx_motivo.getSelectedValue());
		}
		super.caribelContainerCtrl.hashChiaveValore.put(
				"ski_data_apertura", ski_data_apertura.getValueForIsas());
		super.caribelContainerCtrl.hashChiaveValore.put(
				"ski_data_uscita", ski_data_uscita.getValueForIsas());
		
		cbx_motivo.setDisabled(contattoDisabilitato);
		
		tabpanel_scale.getLinkedTab().setDisabled(isInInsert());
		impostaCollegamentoSo(btn_scheda_so, this.currentIsasRecord);
		
		settaDatiPerRiepilogoAccessi();
	}
	
	public static boolean anagraficaChiusa(Hashtable<String, Object> hParameters, CaribelDatebox skiDataApertura,
			CaribelDatebox skiReferente) {
		String dtCartellaChiusa = ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
		String dtCartellaApertura = ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_APERTA);
		boolean anagraficaChiusa = false;
		if (ManagerDate.validaData(dtCartellaChiusa)){
			anagraficaChiusa = true;
			if (ManagerDate.validaData(dtCartellaApertura)){
				skiDataApertura.setValue(ManagerDate.getDate(dtCartellaApertura));
				if (skiReferente!=null){
					skiReferente.setValue(ManagerDate.getDate(dtCartellaApertura));
				}
			}
		}
		return anagraficaChiusa;
	}


	private void settaDatiPerRiepilogoAccessi() {
		String dtContattoApertura = "";
		if(ManagerDate.validaData(ski_data_apertura)){
			dtContattoApertura = ski_data_apertura.getValueForIsas();
		}
		String dtContattoChiusura = "";
		if(ManagerDate.validaData(ski_data_uscita)){
			dtContattoChiusura = ski_data_uscita.getValueForIsas();
		}
		UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO, dtContattoApertura);
		UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE, dtContattoChiusura);
	}

	private boolean abilitaBottoneRiapri() {
		String punto = ver + "abilitaBottoneRiapri ";
		boolean abilitaBottoneRiapri = false;
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		logger.trace(punto + " dati>>" +dati);
		int nMaxContatto = myEJB.recuperaMaxContatto(CaribelSessionManager.getInstance().getMyLogin(), dati);
		
		abilitaBottoneRiapri = ( (n_contatto.getValue() !=null)  && (n_contatto.getValue().intValue() == nMaxContatto));
		
		logger.trace(punto + " abilitareBottone>>" + abilitaBottoneRiapri +
				" \nn_contatto.getValue().intValue()==  nMaxContatto>"+nMaxContatto +"<");
		return abilitaBottoneRiapri;
	}


	private void verificaStatoCartella() {
		String punto = ver + "verificaStatoCartella ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		logger.trace(punto + " controllo per cartella>");
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		CartellaEJB cartellaEjb = new CartellaEJB();
		Hashtable<String, String> statoCartella = cartellaEjb.verificaStatoCartella(CaribelSessionManager.getInstance()
				.getMyLogin(), dati);
		String messaggio = "";

		String dataChiusura = ISASUtil.getValoreStringa(statoCartella, CostantiSinssntW.CTS_CARTELLA_CHIUSA);

		if (ManagerDate.validaData(dataChiusura)) {
			logger.trace(punto + " cartella chiusa>"+dataChiusura+"<");
			if (messaggioAvvisoCartellaChiusa && isInInsert()){
				String dtFIne = ManagerDate.formattaDataIta(dataChiusura, "/");
				String[] lables = new String[] { dtFIne };
				messaggio = Labels.getLabel("Cartella.stato.chiusa", lables);
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
				messaggioAvvisoCartellaChiusa = false;
			}
			
			Date dtChiusura = ManagerDate.getDate(dataChiusura);
			ski_data_apertura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
			ski_data_uscita.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
		}
		if (isInUpdate()){
			ski_data_apertura.setReadonly(true);
		}
	}

	@Override
	public boolean doValidateForm() throws Exception {
		
		boolean canSave = false;
		
		
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		//		myEJB.insert(CaribelSessionManager.getInstance().getMyLogin(), dati);
		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITAZIONE_RFC115_6)) {
			/* TODO IMPLEMENTEARE */
		}
		
		if (this.ski_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO)){
			ski_descr_contatto.setValue(ski_data_apertura.getText()+CostantiSinssntW.DA_SCHEDA_SO);
		}
		else ski_descr_contatto.setValue(ski_data_apertura.getText());
				
		switch(check_save_step){
		case 0: if(!controlloDatiSalvataggio()) return false;
		case 1: settaDati(); canSave = true; check_save_step = 0;break;
		}		
		return canSave;
}
	
	private void settaDati() {
		if (this.currentIsasRecord == null && !isDataValida(ski_infermiere_da)) {
			ski_infermiere_da.setValue(ski_data_apertura.getValue());
		}
		ski_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		desc_operat.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
		
	}

	
	private boolean isDataValida(CaribelDatebox data) {
		boolean dataValida = false;
		Date dt = data.getValue();
		dataValida = (dt != null);
		logger.trace(" data valida>>" + dt + "<< dataValida>" + dataValida + "<<");
		return dataValida;
	}
	private boolean controlloDataAperturaInferioreContatto() {
		boolean controlloOk = true;
		
		if (pr_data.getValue()!=null) {
			try {
				logger.trace(" data>>" + pr_data.getValue().toString() + "<<");
				Date dataApertura = ski_data_apertura.getValue();
				if (DateUtils.truncate(pr_data.getValue(), Calendar.DATE).after(DateUtils.truncate(dataApertura, Calendar.DATE))) {
					UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.aperto"));
					controlloOk = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return controlloOk;
	}
	
	private boolean onRfc115Valutazione()
	{
		/* TODO: inserire il codice per richiamare le frame di prima valutazione e rivalutazione per
		 * RFC115 x Toscana.
		 * Attenzione: i parametri per chiamare prima valutazione o rivalutazione sono all'interno del pulsante
		 * btn_rfc115_valutazione
		 *  void jCariButton1_actionPerformed(ActionEvent e) {
------------- VECCHIO METODO -----------------


 void jCariButton1_actionPerformed(ActionEvent e) {

  boolean presa_carico_uvm = false;
                try{
                if (JPanelDettPresaCarico.jCariComboBoxTipoPercorso.getMyvalue().equals("1"))
                presa_carico_uvm = true;
                }catch (Exception ex){ex.printStackTrace();}
    if (presa_carico_uvm) {
    new cariInfoDialog(this,"Non è necessario inserire valutazioni per i casi UVM!", "Attenzione!").show();
     new JFrameRtValutazSan(this,(JInternalFrame)this.myContainer,this.JCariTextFieldCodice.getUnmaskedText(),
		  this.JCariDateTextFieldDataApertura.getUnmaskedText(),this.JLabelAssistito.getText(), "",
                  this.jCariDateTextFieldChiusura.getUnmaskedText(),this.jCariDateTextFieldSkVal.getUnmaskedText(),
                  new Integer(1),"consulta",(this.tempo_t.equals("0")));
                  return;
    }
  Hashtable h = new Hashtable();
    h.put("n_cartella",JCariTextFieldCodice.getUnmaskedText());
        if (!jCariDateTextFieldSkVal.IsVoid())
    h.put("pr_data",jCariDateTextFieldSkVal.getUnmaskedText());
    Hashtable caso = db.getCasoRif(h);
    if (!checkPrimaValutazione())this.tempo_t = "0";
    else
    {
    this.tempo_t = Integer.toString(Integer.parseInt(caso.get("tempo_t").toString()));
    if (this.tempo_t.equals("0")) this.tempo_t = "1";
    }
  new JFrameRtValutazSan(this,(JInternalFrame)this.myContainer,this.JCariTextFieldCodice.getUnmaskedText(),
		  this.JCariDateTextFieldDataApertura.getUnmaskedText(),this.JLabelAssistito.getText(), JCariTextFieldCodInfRef.getUnmaskedText(),
                  this.jCariDateTextFieldChiusura.getUnmaskedText(),this.jCariDateTextFieldSkVal.getUnmaskedText(),
                  new Integer(1),(this.stato != this.CONSULTA)?this.tempo_t:"consulta",(this.tempo_t.equals("0")));
  }
  
		*/
		return true;
		
	}
	
	private boolean controlloDatiSalvataggio() throws Exception {
		boolean canSave = true;
		logger.debug(" controllo dati salvataggio ");

		
		
		canSave = controlloDataAperturaInferioreContatto();
		if (!canSave) 
			return false;
		
		boolean isChiusura = ski_data_uscita.getValue() != null;
		boolean isMotivoChiusuraInserito = isMotivoChiusuraInserito();

		if (isChiusura) {
			canSave = ManagerDate.controllaPeriodo(self, ski_data_apertura, ski_data_uscita, "lb_ski_data_apertura", "lb_ski_data_uscita");
			if (!canSave) return false;
			
			if (!isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg"));
				if (ManagerProfile.isConfigurazioneToscana(getProfile()))
				cbx_motchiusura.focus();
				else txt_motchiusura.focus();
				ski_data_uscita.setValue(null);
				canSave = false;
				return false;
			}
			return messaggioConfermaChiusura();			
		} else {
			logger.debug(" contatto non chiuso ");
			if (isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.no.data"));
				if (ManagerProfile.isConfigurazioneToscana(getProfile()))
					cbx_motchiusura.focus();
					else txt_motchiusura.focus();
				
				canSave = false;
				return false;
			}
		}
		logger.debug("fine");
		return canSave;
	}
	private boolean messaggioConfermaChiusura() throws Exception {
		//non è più prevista la chiusura del contatto in automatico.
//		if (id_skso_url != null){
//			settaSaveStep(RISPOSTACHIUSURACONTATTO);
//			doSaveForm();
//			return false;
//		}
		Messagebox.show(Labels.getLabel("contatto.conferma.chiusura.msg", new String[] {}),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception{
						if (Messagebox.ON_YES.equals(event.getName())) {
							settaSaveStep(RISPOSTACHIUSURACONTATTO);
							doSaveForm();
						}
					}
				});
		return false;
	}
	private void settaSaveStep(int step) throws Exception {
		check_save_step = step;
	}

	private boolean isMotivoChiusuraInserito() {
		boolean motivoChiusuraInserito = true;
		if (ManagerProfile.isConfigurazioneToscana(getProfile()))
		{
		if (cbx_motchiusura.getSelectedIndex() == -1 || !ISASUtil.valida(cbx_motchiusura.getSelectedItem().getLabel())
				|| !cbx_motchiusura.getSelectedItem().getLabel().equals(CostantiSinssntW.VALORE_COMBO_DEFAULT)) {
			cbx_motchiusura.focus();
			motivoChiusuraInserito = false;
		}
		}
		else if (txt_motchiusura.getValue().trim().equals(""))
			motivoChiusuraInserito = false;
		return motivoChiusuraInserito;
	}


	private Hashtable<String, String> selectControlloData(String nCartella,String nContatto) throws SQLException {
		Hashtable<String, String> datiRicevuti = new Hashtable<String, String>();
		Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
		controlloInterv.put(Costanti.N_CARTELLA, nCartella);
		controlloInterv.put(CostantiSinssntW.N_CONTATTO, nContatto);
		controlloInterv.put(ManagerProfile.TIPO_OPERATORE, ManagerProfile.getTipoOperatore(getProfile()));
		//		super.select("query_controlloData",profile.getParameter("skmed"),t);
		ISASRecord dbrInterv = myEJB.query_controlloData(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);

		datiRicevuti.put(CTS_TROVA_INVERV, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV));
		datiRicevuti.put(CTS_TROVA_INVERV_MAX, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV_MAX));
		logger.debug( " dati recuperati>>" + (datiRicevuti != null ? datiRicevuti + "" : " no dati") + "");

		return datiRicevuti;
	}
	
	public void onBlurDataApertura(Event event) throws Exception {
		if (ski_infermiere_da.getValue()==null || this.isInInsert()) ski_infermiere_da.setValue(ski_data_apertura.getValue());
		
	}


	@Override
	protected boolean doSaveForm() throws Exception {			
		String punto = ver + "doSaveForm ";
		state_before_in_insert = isInInsert();
		if (state_before_in_insert && id_skso_url!=null)
			{this.id_skso.setValue(new Integer(id_skso_url));
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
			}
		boolean ret = false;
		try {
			
			if (!ManagerDate.controllaPeriodo(self, ski_data_apertura, ski_data_uscita, "lb_ski_data_apertura","lb_ski_data_uscita")){
				logger.trace(punto + " data Non corretta ");
				return false;
			}
			
			
			if (isInUpdate()){
				String dataContatto =ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "ski_data_apertura");
				String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "ski_data_uscita");

				if (okControlliChiusura(ski_data_apertura, ski_data_uscita,dataContatto, dataCorrenteChiu, n_cartella.getText(), n_contatto.getText())){
					logger.trace(punto + " Controlli chiusura ok");
				}else {
					logger.trace(punto + " Controlli chiusura NON andati a buon fine!!!");
					return false;
				}
			}
			
			ret = super.doSaveForm();
		}catch (Exception e) {
			doShowException(e);
			return false;
		}
		if (ret){
		gestioneListaAttivita(true);
		doMakeControlAfterRead();
		doFreezeForm();
//		gestionePannelloScale();
		gestionePianiAssistenziali();
		}
		return ret;
		
	}
	
	public  boolean okControlliChiusura(CaribelDatebox dataApertura, CaribelDatebox dataUscita,
			String dataContatto, String dataCorrenteChiu,String nCartella, String nContatto) throws Exception {
		String punto =  "okControlliChiusura ";
		boolean controlliChiusuraOK = false;
		logger.debug(punto + "Controllo chiusura  ");
		
		String data_corrente = dataApertura.getText();
		String data_corrente_chiu = dataUscita.getText();
		
//		String dataCorrenteChiu = "";
//		if (isInUpdate()) {
//			dataContatto = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "ski_data_apertura");
//			dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "ski_data_uscita");

			if (!data_corrente.equals(dataContatto) || !data_corrente_chiu.equals(dataCorrenteChiu)) {
				//entra qui quando Ã¨ cambiata la data apertura contatto o la data chiusura contatto
				Hashtable<String, String> datiRicevuti = selectControlloData(nCartella, nContatto);
				//System.out.println("Controllodata=>"+trovaInterv.getUnmaskedText());
				String trovaInterv = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV);
				String trovaIntervMax = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV_MAX);
				String msg = "";
				if (!(trovaInterv.equals("N"))) {//ci sono degli interventi controllo le date
					String data = trovaInterv;
					String datastr = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
					java.sql.Date dtscad = java.sql.Date.valueOf(data);
					data = data_corrente;
					data = data.substring(6, 10) + "-" + data.substring(3, 5) + "-" + data.substring(0, 2);
					java.sql.Date corrente = java.sql.Date.valueOf(data);
					if (corrente.after(dtscad)) {
						String[] sost = new String[]{datastr};
						msg = Labels.getLabel(CostantiSinssntW.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_PRIMA_APERTURA, sost);
	//					new it.pisa.caribel.swing2.cariInfoDialog(null, "Impossibile effettuare il salvataggio.\nSono presenti interventi con "
	//							+ "data minore della data apertura inserita.\n" + "Massima data valida " + datastr, "Attenzione!").show();
						//					return count=-1;
					}
					if (!ISASUtil.valida(msg)){
						//controllo la data chiusura
						if (ManagerDate.validaData(data_corrente_chiu)) {
							String data_max = trovaIntervMax;
							String datastr_chiu = data_max.substring(8, 10) + "/" + data_max.substring(5, 7) + "/" + data_max.substring(0, 4);
							java.sql.Date dtscad_chiu = java.sql.Date.valueOf(data_max);
							data_max = data_corrente_chiu;
							data_max = data_max.substring(6, 10) + "-" + data_max.substring(3, 5) + "-" + data_max.substring(0, 2);
							java.sql.Date corrente_chiu = java.sql.Date.valueOf(data_max);
							if (dtscad_chiu.after(corrente_chiu)) {
								String[] sost = new String[]{datastr_chiu};
								msg = Labels.getLabel(CostantiSinssntW.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_DOPO_CONCLUSIONE, sost);
		//						new it.pisa.caribel.swing2.cariInfoDialog(null,
		//								"Impossibile effettuare il salvataggio.\nSono presenti interventi con"
		//										+ " data maggiore della data chiusura inserita.\n" + "Minima data valida " + datastr_chiu,
		//								"Attenzione!").show();
								//						return count=-1;
							}
						}
					}
					
					if (ISASUtil.valida(msg)){
						logger.trace(punto + " Ci sono messaggi>>" + msg );
						Messagebox.show(msg,Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
					}else {
						controlliChiusuraOK = true;
					}
					
					//se c'Ã¨ qualcosa che non va, qui non arriva altrimenti deve fare update
					//				count=db.Update(t);
					//				setTipoUVG();
				} else {
					controlliChiusuraOK = true;
				}
//			}
		}else {
			controlliChiusuraOK = true;
			logger.trace(punto + " Non effettuo il controllo per gli accessi ");
		}
		
		return controlliChiusuraOK;
	}


	public void settaInfermiereReferente(String codice, String descrizione, Date dataDa) throws Exception {
		ski_infermiere.setValue(codice);
		desc_inf.setValue(descrizione);
		ski_infermiere_da.setValue(dataDa);
	}

	
	protected void gestionePannelloScale() throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (ManagerProfile.isConfigurazioneMarche((getProfile()))
				|| ManagerProfile.isConfigurazioneToscana((getProfile()))) {
			if (tabpanel_scale.getFirstChild() != null){
				//panel_scale.detach();
				tabpanel_scale.getFirstChild().detach();
			}
			/*else {
				scalPnlCtr.settaDtApeAllScale(UtilForBinding
						.getValueForIsas(ski_data_apertura.getValue()));
				scalPnlCtr.settaDtApeSkVal(UtilForBinding
						.getValueForIsas(pr_data.getValue()));
				scalPnlCtr.settaNCart(String.valueOf(n_cartella.getValue()));
				scalPnlCtr.getTempoT();
			}*/
			
			if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITA_SCALA)) {
				map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
				// map.put("contatto", n_contatto.getValue().toString());
				if (pr_data != null)
					map.put("pr_data", pr_data.getValueForIsas());
				map.put("chiamante", String.valueOf(CostantiSinssntW.CASO_SAN));
				// hPar.put("stato", ""+this.stato);

				// dt ape skInf
				if (ski_data_apertura != null)
					map.put("data_ap", ski_data_apertura.getValueForIsas());
				else if (pr_data != null) // dt ape skVal
					map.put("data_ap", pr_data.getValueForIsas());
				// dt chius skInf
				if (ski_data_uscita != null)
					map.put("data_ch", ski_data_uscita.getValueForIsas());

				map.put("tipo_op", GestTpOp.CTS_COD_INFERMIERE);
				boolean conTempoT = true;
				boolean tuttiCampi = true;
				map.put("tutti_campi", String.valueOf(tuttiCampi));
				map.put("con_tempo_t", String.valueOf(conTempoT));
				map.put("caribelContainerCtrl", super.caribelContainerCtrl);
				if (id_skso.getValue()!=null)
				map.put(CostantiSinssntW.CTS_ID_SKSO, id_skso.getValue().toString());
				

				panel_scale = Executions.getCurrent().createComponents(
						ScalePanelCtr.myPathFormZul, tabpanel_scale, map);
			}			
		}
	}
	
	public void doStampa() {
		try{
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
			dati.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
			dati.put("data_ap", ski_data_apertura.getText());
			dati.put("data_chiu", ski_data_uscita.getText());
			dati.put("assistito", UtilForContainer.getCognomeNomeAssistito());
			dati.put("operatore", ManagerProfile.getCognomeNomeOperatore(getProfile()));
			Executions.getCurrent().createComponents(StampaContattoInfCtrl.CTS_FILE_ZUL, self, dati);
		}catch(Exception ex){
			ex.printStackTrace();
			doShowException(ex);
		}
	}
	@Override
	protected void doDeleteForm() throws Exception {
		Object obj; 
		try {
			obj = myEJB.deleteAll(CaribelSessionManager.getInstance().getMyLogin(), this.currentIsasRecord);
		} catch (Exception ex) {
			logger.error("Errore nella cancellazione del contatto." + ex);
			throw ex;
		}
		if(((Integer)obj).intValue()==1){
			UtilForUI.standardExclamation("Il contatto non può essere cancellato:" +
					"\nesistono accessi collegati.");
			return;
		}else{
			//caribelContainerCtrl.hashChiaveValore.remove(key)
			caribelContainerCtrl.doRefreshOnDelete(contattoInfForm);	

			
		}
	}
	
}

package it.caribel.app.sinssnt.controllers.contattoFisioterapico;


import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkFisioEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.controllers.ContainerFisioterapicoCtrl;
import it.caribel.app.sinssnt.controllers.accessi_effettuati.AccessiEffettuatiGridCtrl;
import it.caribel.app.sinssnt.controllers.contatto.CaribelContattoFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.riepilogo_ausili_protesica.RiepilogoAusiliProtesicaGridCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalePanelCtr;
import it.caribel.app.sinssnt.controllers.scheda_fim.FimFormCtrl;
import it.caribel.app.sinssnt.controllers.scheda_fim.FimGridCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.storico_referente.StoricoFisioReferenteGridCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.diagnosi.DiagnosiFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
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
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
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
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;

public class ContattoFisioFormCtrl<K> extends CaribelContattoFormCtrl {

	private static final long serialVersionUID = 1L;

	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/contatto_fisioterapico/contatto_fisio.zul";
//	private String myKeyPermission = "SKFISIO";
	public static final String myKeyPermission = ChiaviISASSinssntWeb.SKFISIO;
	public static final String myKeyPermissionStorico = "RBFISREF";
	private SkFisioEJB myEJB = new SkFisioEJB();
	private RMSkSOEJB SoEJB = new RMSkSOEJB();
	protected Window contattoFisioForm;
	private Component cs_presidio;
//	private Component cs_operatore_referente;
	boolean rispostaChiusuraContato = false;

//	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	
	private CaribelTextbox cod_operatore;
	private CaribelTextbox desc_operat;
	private Component contattoMedicoPatologia;
	String nContattoURL = null;
	String nCartellaURL = null;
	
	private Component storicoOperatori = null;
	private Component storico = null;
	private Component protesica = null;
	private Component diagnosi = null;
	private Component fim = null;
		
	private CaribelDatebox skf_data_chiusura;
	private CaribelDatebox skf_data;
	boolean messaggioAvvisoCartellaChiusa = true;
//	private CaribelDatebox pr_data;
	
	private CaribelTextbox skf_fisio;
	private CaribelCombobox skf_fisio_desc;
	private CaribelDatebox skf_fisiot_da;
	private CaribelTextbox cod_presidio;
	private CaribelCombobox presidio_descr;
	private CaribelTextbox skf_descr_contatto;
//	private CaribelIntbox skf_nucfam_num;
	
	//private CaribelTextbox str_cod;
	private CaribelCombobox cbx_riabilitazione; // tipo riabilitazione
	private CaribelCombobox cbx_provenienza; //provenienza
	private CaribelCombobox cbx_medico; //medico proponente
	private CaribelCombobox cbx_motivo; //motivo
	private CaribelCombobox cbx_utenza; // tipo utenza
	private CaribelCombobox cbx_tipologia; //tipologia
	private CaribelCombobox cbx_generali; // condizioni generali
	private CaribelCombobox cbx_cognitive; //condizioni cognitive
	private CaribelCombobox cbx_deambula; // Deambulazione
	private CaribelCombobox cbx_autonomia; //Autonomia
	private CaribelCombobox cbx_tipo; // tipo disabilità
	private CaribelCombobox cbx_decorso; //decorso
	private CaribelCombobox cbx_tempo; // tempo di trattamento
	private CaribelCombobox cbx_giver; //care giver
	private CaribelCombobox cbx_motchiusura; // motivo chiusura
	private Label label_tipologia;
	
//	private Tabpanel tabpanel_scale;
	private Component panel_scale;
	private Tabpanels tabpanels;
	private Tab scaleval_tab_fis;
	private Tabs tabs;	
	private Tabpanel tabpanel_progetto2;
	private Tabpanel tabpanel_condizione;
	private Tab tab_progetto2;
	private Tab tab_condizione;
	private Tab pa_tab;
	private Tabpanel tabpanel_pa;

	
//	private Component sofc = null;
//	private Hashtable temp_container_hash = null;

	
//	private CaribelIntbox id_skso;
	
	private ISASRecord skso_op_coinvolti;
	private RMSkSOOpCoinvoltiEJB op_CoinvoltiEJB;
	
	
//	private Button btn_limitaA; 
//	private Button btn_accessi;
	private Button btn_protesica;
	private Button btn_storico;
	private Button btn_riapri;
	private Button btn_patologie;
	private Button btn_fim;
	private Button btn_sorg;
	private Button btn_scheda_so;
	
	private Vector<Hashtable<String, String>> vettOper = new Vector<Hashtable<String, String>>();
	public static final  String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final  String CTS_DATA_APERTURA = "skf_data";
	public static final  String CTS_DATA_CHIUSURA = "skf_data_chiusura";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";
	public static final String CTS_DATA_CART = "dataCart";
	public static final String CTS_PR_DATA = "pr_data";
	public static final String CTS_ZUL_CHIAMANTE = "zul_chiamante";
	
	private int check_save_step = 0;

	protected CaribelListbox tablePrestazioni;

	private Hlayout corporee;
	private Hlayout ambientali;

	private boolean state_before_in_insert;

	private static final String ver = "1-";

	private static final int RISPOSTACHIUSURACONTATTO = 1;
	
	
	
	public void doInitForm() {
		try {
			logger.debug("inizio ");
			super.doInitForm();
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			
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
			
			cod_operatore.setValue(getProfile().getStringFromProfile("codice_operatore"));
			desc_operat.setValue(getProfile().getStringFromProfile("cognome_operatore"));
				
			//########################### Recupero chiavi del contatto ###################################################################
			String nCartella 	= "";
			String nContatto 	= "";
			
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
			}else if (dbrFromList != null) {
				//arrivo da una griglia di contatti (es. storico)				
				nCartella 	= ((Integer)dbrFromList.get(CostantiSinssntW.N_CARTELLA)).toString();
				nContatto 	= ((Integer)dbrFromList.get(CostantiSinssntW.N_CONTATTO)).toString();
			}else if (super.caribelContainerCtrl != null
					&& super.caribelContainerCtrl.hashChiaveValore != null
					&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA) != null && !(ISASUtil.valida(nContatto))) {
				//arrivo da nuovo contatto o da qualunque altro posto
				nCartella = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.N_CARTELLA);
				nContatto = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.N_CONTATTO);
				if (!ISASUtil.valida(nContatto)) {
					//Recupero il contatto piu recente
					nContatto = recuperaNContatto(nCartella);
				}
				super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, nContatto);
				//13/10/2014 recupero motivo, tipo utenza e numero familiari 
				//dalla segreteria organizzativa
//				recuperaDatiSO(nCartella);
				n_cartella.setValue(new Integer(nCartella));
				recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
			}
			//########################### Fine Recupero chiavi del contatto ##############################################################
			
			//Imposto le chiavi trovate
			if(ISASUtil.valida(nCartella)){
				n_cartella.setValue(new Integer(nCartella));
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
				super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CARTELLA, nCartella);
			}
			if(ISASUtil.valida(nContatto)){
				n_contatto.setValue(new Integer(nContatto));
				hParameters.put(CostantiSinssntW.N_CONTATTO, nContatto);
				super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, nContatto);
			}
			
			String dtCartellaChiusa =  ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
			//Eseguo la lettura da DB o mi preparo per l'insert
			if(ISASUtil.valida(nCartella) && 
					(ISASUtil.valida(nContatto) || ManagerDate.validaData(dtCartellaChiusa)) ) {
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(nContatto)){
				//sono in inserimento					
				this.n_contatto.setValue(new Integer(0));
				this.skf_fisio.setValue(getProfile().getIsasUser().getKUser());
				this.skf_fisio_desc.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
				caricaPatologie();
			}
			
			
			abilitazioneMaschera();
			doFreezeForm();
//			gestionePannelloScale();
			gestioneListaAttivita(false);
			gestionePianiAssistenziali();
			gestioneRichiestaChiusura(false);
		}  catch (Exception ex){
			
			if (ex instanceof InvocationTargetException &&((InvocationTargetException) ex).getTargetException().getMessage().equals(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI)){
			gestioneRichiestaChiusura(true);
			}
			else{
			doShowException(ex);
			}
			try{
				this.setReadOnly(true);
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
					map.put(CostantiSinssntW.SKF_DATA, skf_data.getValueForIsas());
					if(skf_data_chiusura.getValue()!=null){
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
		if (id_skso_url==null) return;
		if (skso_fonte != null && this.skso_fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"")){
		op_CoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
		

			h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getText());
			h.put(CostantiSinssntW.CTS_ID_SKSO,id_skso_url);
			h.put(CostantiSinssntW.CTS_FONTE,skso_fonte);
			h.put(CostantiSinssntW.TIPO_OPERATORE,CostantiSinssntW.TIPO_OPERATORE_FISIOTERAPISTA);			
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
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_esistente",new String[]{skf_data.getText(),desc_operat.getText()}), 
						Labels.getLabel("messagebox.attention"),
						Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
						new EventListener<Event>() {
							public void onEvent(Event event)throws Exception {
								if (Messagebox.ON_YES.equals(event.getName())){
									id_skso.setValue(new Integer(id_skso_url));
									recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
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
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_utilizzato",new String[]{skf_data.getText(),desc_operat.getText()}), 
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
				recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
				id_skso_url=null;
				skso_fonte=null;
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				return;
			}
			
			
		}else if (skso_fonte != null && skso_fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"")){
			 
			if (this.isInUpdate() && !fromDoSaveForm && (id_skso.getText().equals(id_skso_url) || id_skso.getText().equals("")))
				scriviPrimaVisita();
			
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
				scriviPrimaVisita();				
			}
		}else {
			if(fromDoSaveForm){
				scriviPrimaVisita();
			}
		}
		
		if (isInInsert()&&!fromDoSaveForm && id_skso_url!=null)  {
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
		}
		
	}

	private boolean scriviPrimaVisita() throws Exception{		
		String data_pc = skf_data.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(skf_fisio.getText())){
			skf_fisio.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skf_fisiot_da.setValue(Calendar.getInstance().getTime());
			data_pc = skf_fisiot_da.getValueForIsas();
		}		
		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
		return scriviPrimaVisita(data_pc, this.n_cartella.getValue().toString(), this.id_skso_url, true);		
	}


	protected void chiudiSchedaEPianoAss(Hashtable h) throws Exception {
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(Calendar.getInstance().getTime());
		cal.add(Calendar.DAY_OF_YEAR, -1);
		Date data_chiusura = cal.getTime();
		
		this.skf_data_chiusura.setValue(data_chiusura);
		this.cbx_motchiusura.setSelectedValue("9");
		h.put("n_progetto",n_contatto.getText());
		h.put("pa_tipo_oper",GestTpOp.CTS_COD_FISIOTERAPISTA);
		doSaveForm();
		Integer piano_assist=(Integer)invokeGenericSuEJB(new PianoAssistEJB(), h, "chiudi_piani");
		this.currentIsasRecord=null;
		this.caribelContainerCtrl.hashChiaveValore.remove(CostantiSinssntW.N_CONTATTO);
		this.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO,h.get(CostantiSinssntW.CTS_ID_SKSO));		
		((ContainerFisioterapicoCtrl)this.caribelContainerCtrl).btn_contattoFisioForm();
	}



	


	
	protected void scriviPresaCarico(Hashtable h, boolean saveForm) throws Exception  {
		String data_pc = skf_data.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(skf_fisio.getText())){
			skf_fisio.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skf_fisiot_da.setValue(Calendar.getInstance().getTime());
			data_pc = skf_fisiot_da.getValueForIsas();
		}
		skso_op_coinvolti.put("dt_presa_carico", data_pc);
		skso_op_coinvolti.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, CostantiSinssntW.CTS_FLAG_STATO_FATTA);
		skso_op_coinvolti.put(CostantiSinssntW.COD_OPERATORE_PC, getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		try{
		updateSuEJB(op_CoinvoltiEJB, skso_op_coinvolti);
		// aggiorno eventuale scala bisogni con l'id_skso
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


	
	
	private String recuperaNContatto(String nCartella) throws ISASPermissionDeniedException, SQLException, CariException {
		String nContatto = "";
		String punto = "recuperaNContatto ";
		logger.debug(punto + " recuperare nContatto>>" + nCartella);
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		//dati.put(CTS_PR_DATA, prData);

		ISASRecord dbrSkFis = myEJB.getContattoFisCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
		nContatto = ISASUtil.getValoreStringa(dbrSkFis, CostantiSinssntW.N_CONTATTO);
		logger.debug(punto + " contatto recuperato>>" + nContatto);

		return nContatto;
	}
	
//	private void recuperaDatiSO(String nCartella) throws ISASPermissionDeniedException, SQLException, CariException {
//		String punto = "recuperaDatiSO ";
//		logger.debug(punto + " recuperare recuperaDatiSO>>" + nCartella);
//		Hashtable<String, String> dati = new Hashtable<String, String>();
//		Hashtable<String, String> datiRicevuti = new Hashtable<String, String>();
//		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
//		
//		ISASRecord dbrSO = SoEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
////		cbx_motivo.setSelectedValue(ISASUtil.getValoreStringa(dbrSO, "pr_motivo"));
////		cbx_motivo.setDisabled(true);
//		
//		cbx_motivo.setSelectedValue(ISASUtil.getValoreStringa(dbrSO, "tipocura"));
//		cbx_motivo.setDisabled(true);
//
//////		String prMotivo = ISASUtil.getValoreStringa(dbrValutazione, "pr_motivo");
////		String tipocure = ISASUtil.getValoreStringa(dbrValutazione, "tipocura");
//		
//		cbx_utenza.setSelectedValue(ISASUtil.getValoreStringa(dbrSO, "tipo_ute"));
//		cbx_utenza.setDisabled(true);
//		int numero = ISASUtil.getIntField(dbrSO, "num_fam");
//		skf_nucfam_num.setValue(new Integer(ISASUtil.getIntField(dbrSO, "num_fam")));
//		logger.debug(punto + " dati SO recuperati>>" );	
//	}
	private void loadTipologia(CaribelCombobox cbx)throws Exception {
		logger.debug(" dentro loadTipologia ");
		CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("SchedaFisioForm.combo.tipologia.occasionale"));
		CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("SchedaFisioForm.combo.tipologia.apc"));
		CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("SchedaFisioForm.combo.tipologia.uvd"));
	}
	private void loadUtenza(CaribelCombobox cbx)throws Exception {
		logger.debug(" dentro loadUtenza ");
		cbx.clear();
		Hashtable h = new Hashtable();
		CaribelComboRepository.comboPreLoad("f_tipo_utente", new TiputeSEJB(), "query", h, cbx, null, "codice", "descrizione", false);
	}

	private void settaDati() {
		if (this.currentIsasRecord == null && !isDataValida(skf_data)) {
			skf_fisiot_da.setValue(skf_data.getValue());
		}
		//deve essere salvato sempre l'ultimo operatore che modifica qcs
		cod_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		desc_operat.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
	}


	private boolean controlloAperturaInferioreContatto() {
		boolean controlloOk = true;
		
		//if (pr_data.getValue()!=null) {
			try {
				Date dataApertura = skf_data.getValue();
				if (DateUtils.truncate(skf_data.getValue(), Calendar.DATE).after(DateUtils.truncate(dataApertura, Calendar.DATE))) {
				//if (DateUtils.truncate(pr_data.getValue(), Calendar.DATE).after(DateUtils.truncate(dataApertura, Calendar.DATE))) {
					UtilForUI.standardExclamation(Labels.getLabel("SchedaFisioForm.dimissioni.motchiusura.msg.aperto"));
					controlloOk = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		//}
		return controlloOk;
	}
	
	private void settaRisposta(boolean b) {
		rispostaChiusuraContato = b;
	}

	private boolean isDataValida(CaribelDatebox data) {
		String punto = "isDataValida ";
		boolean dataValida = false;
		Date dt = data.getValue();
		dataValida = (dt != null);
		logger.trace(punto + " data valida>>" + dt + "<< dataValida>" + dataValida + "<<");
		return dataValida;
	}

	public boolean isContattoFisChiuso()
	{
		return skf_data_chiusura.getText() != null && !skf_data_chiusura.getText().isEmpty();
	}
	
	public boolean isContattoFisAperto()
	{
		return skf_data.getText()!= null && !skf_data.getText().isEmpty();
	}
	
	private boolean isMotivoChiusuraInserito() {
		boolean motivoChiusuraInserito = false;
		if (cbx_motchiusura.getSelectedIndex() != -1 && !(cbx_motchiusura.getSelectedValue()).equals(".")&& !(cbx_motchiusura.getSelectedValue()).equals("")) {
			cbx_motchiusura.focus();
			motivoChiusuraInserito = true;
		}
		return motivoChiusuraInserito;
	}

	private void doMakeControl() {
		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITA_SCALA)) {
			//		TODO METTERE LA PRESA CARICO E PANNELLO SEGNALAZIONE SOLO PER LA TOSCANA.
		}
		UtilForBinding.setComponentReadOnly(contattoMedicoPatologia,true);
		btn_scheda_so.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			public void onEvent(Event event) throws Exception{
				onSchedaSO();
			}
		});		

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
			caribelContainerCtrl.doRefreshOnDelete(contattoFisioForm);	

			
		}
	}
	public void onAccessi(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onAccessi ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, GestTpOp.CTS_COD_FISIOTERAPISTA);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		if (vettOper != null && vettOper.size() > 0) {
			map.put("vettOper", vettOper);
		}
		storicoOperatori = Executions.getCurrent().createComponents(AccessiEffettuatiGridCtrl.myPathFormZul, self, map);		
	}

	public void onProtesica(ForwardEvent e) throws Exception {
		String punto = ".onProtesica ";
		logger.debug(punto + "inizio ");   

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, GestTpOp.CTS_COD_FISIOTERAPISTA);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		protesica = Executions.getCurrent().createComponents(RiepilogoAusiliProtesicaGridCtrl.myPathFormZul, self, map);
	}

	
	public void onClick$btn_riapri(Event event) throws Exception	{
		try{
			doRiapriContatto();
		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	public void doRiapriContatto() throws Exception {
		String punto = ".doRiapriContatto";
		
		Object isSkValChiusaObj = UtilForContainer.getObjectFromMyContainer(ContainerFisioterapicoCtrl.CTS_IS_SK_VAL_CHIUSA); 
		if(isSkValChiusaObj !=null){
			// ctrl skValutazione aperta
			boolean isSkValChiusa = ISASUtil.getvaloreBoolean(isSkValChiusaObj);
			if(isSkValChiusa){
				UtilForUI.standardExclamation("Il contatto non può essere riaperto: la scheda valutazione è chiusa.");
				return ;
			}
		}	
		// ctrl non esistenza di altri contatti successivi
		Hashtable h_ctrl = new Hashtable();
		h_ctrl.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		h_ctrl.put("dataRif", (String)skf_data.getText());

		Object obj; 
		try {
			obj = myEJB.query_checkContSuccessivi(CaribelSessionManager.getInstance().getMyLogin(), h_ctrl);
		} catch (Exception ex) {
			logger.error("Errore nel controllo contatti successivi." + ex);
			throw ex;
		}
		if(((Boolean)obj).booleanValue()){
			UtilForUI.standardExclamation("Il contatto non può essere riaperto:" +
					"\nesistono altri contatti in date successive.");
			return;
		}
		skf_data_chiusura.setText(null);
		cbx_motchiusura.setSelectedIndex(-1);
		
		// aggiorno il record
        boolean risu = doSaveForm();
        this.setReadOnly(false);
	}

	public void onStorico(ForwardEvent e) throws Exception {
		String punto = ".onStorico ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, GestTpOp.CTS_COD_FISIOTERAPISTA);

		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerFisioterapicoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerFisioterapicoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerFisioterapicoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}

		map.put(StoricoFisioReferenteGridCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
		map.put(CTS_ZUL_CHIAMANTE, (ContattoFisioFormCtrl) this);//Utile per l'aggiornamento da finestra modale
		if (storico == null) {
			storico = Executions.getCurrent().createComponents(StoricoFisioReferenteGridCtrl.myPathFormZul, self, map);
			logger.trace(punto + " Chiudo storico fisioterapista referente già aperto ");
			storico = null;
		} else {
			logger.trace(punto + " storico fisioterapista referente già aperto ");
		}
	}

	public void settaFisioReferente(String codice, String descrizione, Date dataDa) throws Exception {
		String punto =  ".settaFisioReferente ";
		logger.debug(punto + "inizio codice>>" + codice + "<< descrizione>>" + descrizione + "<< data>>" + dataDa);
		skf_fisio.setValue(codice);
		skf_fisio_desc.setValue(descrizione);
		skf_fisiot_da.setValue(dataDa);
	}
	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put(CTS_DATA_APERTURA, skf_data.getValue());
		if(skf_data_chiusura!=null)
			map.put(CTS_DATA_CHIUSURA, skf_data_chiusura.getValue());
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		if (vettOper != null && vettOper.size() > 0) {
			map.put("vettOper", vettOper);
		}
		try {
			diagnosi = Executions.getCurrent().createComponents(
					DiagnosiFormCtrl.myPathFormZul, self, map);
			diagnosi.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
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
		h.put("data_apertura", skf_data.getValueForIsas());
		if (skf_data_chiusura != null)
			h.put("data_chiusura", skf_data_chiusura.getValueForIsas());
		h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
//		lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryLastDiagContesto"));
		lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryStoricoDiag"));
		tablePrestazioni.setModel(lm);
	}
	
	public void onFIM(ForwardEvent e) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue());
		map.put("fim_spr_data", skf_data.getValueForIsas().toString());
		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerFisioterapicoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerFisioterapicoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerFisioterapicoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}
		map.put(FimFormCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		logger.debug("MAP:"+map.toString());
		fim = Executions.getCurrent().createComponents(FimGridCtrl.myPathGridZul, self, map);

	}
	

	private void doPopulateCombobox() throws Exception {
		String punto = "doPopulateCombobox \n";
		String codProvenienza = "FPROVE";
		String[] combo;
		logger.debug(punto + "");
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

		if (ManagerProfile.isConfigurazioneAbruzzo(getProfile()) || ManagerProfile.isConfigurazioneMolise(getProfile()) || 
			ManagerProfile.isConfigurazioneLazio(getProfile())) {
			codProvenienza = "FMRICH";
		}
		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
		h_xAllCB.put("combo_tipostru_V", "S"); // x notificare che serve 1 riga vuota
		h_xAllCB.put("combo_tipostru_C", "S"); // x notificare che serve anche il codReg
		if (ManagerProfile.isConfigurazioneAbruzzo(getProfile()) || ManagerProfile.isConfigurazioneMolise(getProfile()) 
				|| ManagerProfile.isConfigurazioneMarche(getProfile())) {
			combo=new String[]{"FTIPRI",codProvenienza,"FMEDPR","FCONDG",
					"FCONDC","FDEAMB","FAUTON","FDECPA","FDISAB",
					"FTEMPO","FCAREG","FCHIUS", "MOTIVO"};
//			h_xCBdaTabBase.put("MOTIVO", cbx_motivo);
			h_xCBdaTabBase.put("FMRICH", cbx_provenienza);
		}else if (ManagerProfile.isConfigurazioneLazio(getProfile())) {
			combo=new String[]{"FTIPRI",codProvenienza,"FMEDPR","FCONDG",
					"FCONDC","FDEAMB","FAUTON","FDECPA","FDISAB",
					"FTEMPO","FCAREG","FCHIUS"};
			h_xCBdaTabBase.put("FMRICH", cbx_provenienza);
		} else {
				combo=new String[]{"FTIPRI",codProvenienza,"FMEDPR","FCONDG",
						"FCONDC","FDEAMB","FAUTON","FDECPA","FDISAB",
						"FTEMPO","FCAREG","FCHIUS"};
				h_xCBdaTabBase.put("FPROVE", cbx_provenienza);
		}
		h_xCBdaTabBase.put("FTIPRI",cbx_riabilitazione);
		h_xCBdaTabBase.put("FMEDPR",cbx_medico);
		if(!ManagerProfile.isConfigurazioneMarche(getProfile())){
			h_xCBdaTabBase.put("FCONDG",cbx_generali);
			h_xCBdaTabBase.put("FCONDC",cbx_cognitive);
			h_xCBdaTabBase.put("FDEAMB",cbx_deambula);
			h_xCBdaTabBase.put("FAUTON",cbx_autonomia);
			h_xCBdaTabBase.put("FDISAB",cbx_tipo);
			h_xCBdaTabBase.put("FCAREG",cbx_giver);
		}
		h_xCBdaTabBase.put("FDECPA",cbx_decorso);
		h_xCBdaTabBase.put("FTEMPO",cbx_tempo);
		h_xCBdaTabBase.put("FCHIUS",cbx_motchiusura);
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(cbx_motivo);
		
		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
				"tab_descrizione", false);

		loadTipologia(cbx_tipologia);
		loadUtenza(cbx_utenza);
	}
	
	private void abilitazioneMaschera() throws WrongValueException, Exception {
		String punto = ver  + "abilitazioneMaschera ";
		logger.trace(punto);
		
		verificaStatoCartella();
		
		if (isInInsert()){
			String nCartella = (String) hParameters.get("n_cartella");
			n_cartella.setText(nCartella);
			n_contatto.setText("0");
			if(ManagerProfile.getTipoOperatore(getProfile()).equals(UtilForContainer.getTipoOperatorerContainer())){
				skf_fisio.setValue(getProfile().getStringFromProfile(
					ManagerProfile.CODICE_OPERATORE));
				skf_fisio_desc.setValue(getProfile().getStringFromProfile(
					ManagerProfile.COGNOME_OPERATORE)
					+ " "
					+ getProfile().getStringFromProfile(
							ManagerProfile.NOME_OPERATORE));
			}
			String codPresidio = getProfile().getStringFromProfile(
					ManagerProfile.PRES_OPERATORE);
			cod_presidio.setValue(codPresidio);
			presidio_descr.setValue(ManagerDecod.decodPresidi(getProfile(),
					codPresidio));
			logger.trace(" aggiorno>>" + "<< " + "\ndesc>>" + "<");
			boolean proporreDtScheda = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_PROPORRE_DT_SCHEDA);
			Date dataAccettazioneSkSo = recuperaDataAccettazioneSkSo(n_cartella, id_skso,cbx_provenienza, cbx_utenza, 
					cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
			if (ManagerDate.validaData(dataAccettazioneSkSo)) {
				skf_data.setValue(dataAccettazioneSkSo);
				skf_fisiot_da.setValue(dataAccettazioneSkSo);
			} else {
				if (proporreDtScheda) {
					if (!ContattoInfFormCtrl.anagraficaChiusa(this.hParameters, skf_data, skf_fisiot_da)){
						skf_data.setValue(procdate.getDate());
						skf_fisiot_da.setValue(procdate.getDate());
					}
				}
			}
		}
//		btn_accessi.setDisabled(isInInsert());
		//10/10/2014btn_protesica.setDisabled(isInInsert());
		btn_protesica.setVisible(false);
		btn_riapri.setDisabled(isInInsert() || (isInUpdate()&&!isContattoFisChiuso()));
		btn_patologie.setDisabled(isInInsert());
		btn_storico.setDisabled(isInInsert());
		if(!ManagerProfile.isConfigurazioneMarche(getProfile()))
			btn_fim.setDisabled(isInInsert());
		skf_data_chiusura.setDisabled(isInInsert());
		if(isInInsert())
			cbx_motchiusura.setSelectedIndex(-1);
		cbx_motchiusura.setDisabled(isInInsert());		
		cbx_tipologia.setVisible(false);
		label_tipologia.setVisible(false);
		
		tabpanel_scale.getLinkedTab().setDisabled(isInInsert());
		
		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			//corporee.setVisible(false);
			//ambientali.setVisible(false);
			tabs.removeChild(tab_progetto2);
			tabpanels.removeChild(tabpanel_progetto2);
			tabs.removeChild(tab_condizione);
			tabpanels.removeChild(tabpanel_condizione);			
		}
		if (isInUpdate()){
			skf_fisiot_da.setDisabled(true);
			((CaribelSearchCtrl) cs_operatore_referente.getAttribute(MY_CTRL_KEY)).setReadonly(true);
			if (this.skf_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
			{
				cbx_provenienza.setDisabled(true);
				cbx_utenza.setDisabled(true);
//				cbx_motivo.setDisabled(true);
//				cbx_motivo.setRequired(false);
				cbx_provenienza.setRequired(false);				
				cbx_utenza.setRequired(false);
			}
		}
		
		boolean abilChiu = ((getProfile().getIsasUser().canIUse(ManagerProfile.CLOSE_SCHEDA_FIS,"MODI"))
				&& (!isContattoFisChiuso()) && (isContattoFisAperto()));
		
		this.skf_data_chiusura.setDisabled(!abilChiu);
		this.cbx_motchiusura.setDisabled(!abilChiu);
		
		if (isContattoFisChiuso()){
			this.setReadOnly(true);
			btn_riapri.setDisabled(false);
//			btn_accessi.setDisabled(false);
			//btn_rug_svama.setDisabled(false);
			//btn_segnalazione.setDisabled(false);
			btn_protesica.setDisabled(false);
			boolean abilRiap = (getProfile().getIsasUser().canIUse(ManagerProfile.REOPEN_SCHEDA_FIS,"MODI"));
			this.btn_riapri.setVisible(abilRiap);
			this.btn_riapri.setDisabled(false);
		}
		recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
//		recuperaDatiSO(n_cartella.getValue()+"");
		impostaCollegamentoSo(btn_scheda_so, this.currentIsasRecord);
	}
	
	
	private void verificaStatoCartella() {
		String punto = ver + "verificaStatoCartella ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		logger.trace(punto + " controllo per cartella>");
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		CartellaEJB cartellaEJB = new CartellaEJB();
		
		Hashtable<String, String> statoCartella = cartellaEJB.verificaStatoCartella(CaribelSessionManager.getInstance()
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
			skf_data.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
			skf_data_chiusura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
		}
		skf_data.setReadonly(isInUpdate());
	}

	private boolean controlloDatiSalvataggio() throws Exception {
		boolean canSave = true;
		logger.debug(" controllo dati salvataggio ");

		
		canSave = controlloAperturaInferioreContatto();
		if (!canSave) 
			return false;
		
		boolean isChiusura = skf_data_chiusura.getValue() != null;
		boolean isMotivoChiusuraInserito = isMotivoChiusuraInserito();

		if (isChiusura) {
			canSave = ManagerDate.controllaPeriodo(self, skf_data, skf_data_chiusura, "lb_skf_data", "lb_skf_data_chiusura");
			if (!canSave) return false;
			
			if (!isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("SchedaFisioForm.dimissioni.motchiusura.msg"));
				cbx_motchiusura.focus();
				skf_data_chiusura.setValue(null);
				canSave = false;
				return false;
			}
			return messaggioConfermaChiusura();			
		} else {
			logger.debug(" contatto non chiuso ");
			if (isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("SchedaFisioForm.dimissioni.motchiusura.msg.no.data"));
				cbx_motchiusura.focus();
				canSave = false;
				return false;
			}
		}
		if (this.skf_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO)){
			skf_descr_contatto.setValue(skf_data.getText()+CostantiSinssntW.DA_SCHEDA_SO);
		}
		else skf_descr_contatto.setValue(skf_data.getText());		
	
		logger.debug("fine");
		return canSave;
	}
	private boolean messaggioConfermaChiusura() throws Exception {
//		if (id_skso_hash !=null || id_skso != null){
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

	
	private boolean doUpdateRecord() throws SQLException {
		String punto = "doInsertRecord ";
		logger.trace(punto + " sono in insert ");
		String data_corrente = skf_data.getText();		
		cod_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		desc_operat.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
		
		String data_corrente_chiu = skf_data_chiusura.getText();
		String dataContatto = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skf_data");
		String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skf_data_chiusura");
		if (!data_corrente.equals(dataContatto) || !data_corrente_chiu.equals(dataCorrenteChiu)) {
			//entra qui quando è cambiata la data apertura contatto o la data chiusura contatto
			Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
			Hashtable<String, String> datiRicevuti = selectControlloData();
			//System.out.println("Controllodata=>"+trovaInterv.getUnmaskedText());
			String trovaInterv = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV);
			String trovaIntervMax = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV_MAX);
			if (!(trovaInterv.equals("N"))) {//ci sono degli interventi controllo le date
				String data = trovaInterv;
				String datastr = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
				java.sql.Date dtscad = java.sql.Date.valueOf(data);
				data = data_corrente;
				data = data.substring(6, 10) + "-" + data.substring(3, 5) + "-" + data.substring(0, 2);
				java.sql.Date corrente = java.sql.Date.valueOf(data);
				if (corrente.after(dtscad)) {
					new it.pisa.caribel.swing2.cariInfoDialog(null, "Impossibile effettuare il salvataggio.\nSono presenti interventi con "
							+ "data minore della data apertura inserita.\n" + "Massima data valida " + datastr, "Attenzione!").show();
					return false;
				}
				//controllo la data chiusura
				if (!data_corrente_chiu.equals("__/__/____")) {
					String data_max = trovaIntervMax;
					String datastr_chiu = data_max.substring(8, 10) + "/" + data_max.substring(5, 7) + "/" + data_max.substring(0, 4);
					java.sql.Date dtscad_chiu = java.sql.Date.valueOf(data_max);
					data_max = data_corrente_chiu;
					data_max = data_max.substring(6, 10) + "-" + data_max.substring(3, 5) + "-" + data_max.substring(0, 2);
					java.sql.Date corrente_chiu = java.sql.Date.valueOf(data_max);
					if (dtscad_chiu.after(corrente_chiu)) {
						new it.pisa.caribel.swing2.cariInfoDialog(null,
								"Impossibile effettuare il salvataggio.\nSono presenti interventi con"
										+ " data maggiore della data chiusura inserita.\n" + "Minima data valida " + datastr_chiu,
								"Attenzione!").show();
						return false;
					}
				}
			}
		}
		return true;	
	}
	
	public boolean doValidateForm() throws Exception {
		if (skf_fisiot_da.getValue()==null) skf_fisiot_da.setValue(skf_data.getValue());
		String punto = "doValidateForm ";
		boolean canSave = true;
		switch(check_save_step){
			case 0: if(!controlloDatiSalvataggio()) return false;
			case 1: settaDati(); canSave = true; check_save_step = 0;abilitazioneMaschera();break;
		}
		return canSave;
	}

	
	
	private Hashtable<String, String> selectControlloData() throws SQLException {
		String punto = "selectControlloData ";
		Hashtable<String, String> datiRicevuti = new Hashtable<String, String>();
		Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
		controlloInterv.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		controlloInterv.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
		ISASRecord dbrInterv = myEJB.query_controlloData(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);
		datiRicevuti.put(CTS_TROVA_INVERV, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV));
		datiRicevuti.put(CTS_TROVA_INVERV_MAX, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV_MAX));
		logger.debug(punto + " dati recuperati>>" + (datiRicevuti != null ? datiRicevuti + "" : " no dati") + "");

		return datiRicevuti;
	}
	
	
	public void onBlurDataApertura(Event event) throws Exception {
		if (skf_fisiot_da.getValue()==null || isInInsert()) skf_fisiot_da.setValue(skf_data.getValue());
		
	}
	@Override
	protected boolean doSaveForm() throws Exception {
		String punto = ver + "doSaveForm ";
		state_before_in_insert = isInInsert();
		if (state_before_in_insert && id_skso_url!=null)
			{this.id_skso.setValue(new Integer(id_skso_url));
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
			}
		boolean ret = false;
		try {
			if (!ManagerDate.controllaPeriodo(self, skf_data, skf_data_chiusura, "lb_skf_data","lb_skf_data_chiusura")){
				logger.trace(punto + " data Non corretta ");
				return false;
			}
			
			if (isInUpdate()){
				String dataContatto =ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skf_data");
				String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skf_data_chiusura");

				ContattoInfFormCtrl cont = new ContattoInfFormCtrl();
				if (cont.okControlliChiusura(skf_data, skf_data_chiusura,dataContatto, dataCorrenteChiu, n_cartella.getText(), n_contatto.getText())){
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
		abilitazioneMaschera();
		doFreezeForm();
//		gestionePannelloScale();
		gestionePianiAssistenziali();
		}
		return ret;
	}
		
	 



	public void doStampa() {
		try{
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
			dati.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());			
			dati.put("data_ap", skf_data.getText());
			dati.put("data_chiu", skf_data_chiusura.getText());
			dati.put("assistito", UtilForContainer.getCognomeNomeAssistito());
			dati.put("operatore", ManagerProfile.getCognomeNomeOperatore(getProfile()));
			Executions.getCurrent().createComponents(StampaContattoFisioCtrl.CTS_FILE_ZUL, self, dati);
		}catch(Exception ex){
			ex.printStackTrace();
			doShowException(ex);
		}
	}
	
	protected void gestionePannelloScale() throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (ManagerProfile.isConfigurazioneMarche((getProfile()))
				|| ManagerProfile.isConfigurazioneToscana((getProfile()))) {
			if (tabpanel_scale.getFirstChild() != null){
				//panel_scale.detach();
				tabpanel_scale.getFirstChild().detach();
			}
			if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITA_SCALA)) {
				map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
//				if (pr_data != null)
//					map.put("pr_data", pr_data.getValueForIsas());
				map.put("chiamante", String.valueOf(CostantiSinssntW.CASO_SAN));
				if (skf_data != null)
					map.put("data_ap", skf_data.getValueForIsas());
//				else if (pr_data != null) // dt ape skVal
//					map.put("data_ap", pr_data.getValueForIsas());
				if (skf_data_chiusura != null)
					map.put("data_ch", skf_data_chiusura.getValueForIsas());

				map.put("tipo_op", GestTpOp.CTS_COD_FISIOTERAPISTA);
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
//	private void onSchedaSO() throws Exception{
//		Hashtable h = new Hashtable();
//		h.put("n_cartella", n_cartella.getValue().toString());
//		ISASRecord dbr = (ISASRecord) invokeGenericSuEJB(new RMSkSOEJB(), h,"selectSkValCorrente");
//		if (dbr != null && dbr.get(CostantiSinssntW.CTS_ID_SKSO)!=null){
//			String id_skso = dbr.get(CostantiSinssntW.CTS_ID_SKSO).toString();
//			h.put(CostantiSinssntW.CTS_ID_SKSO, id_skso);
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
//		else Messagebox.show(
//				Labels.getLabel("SchedaInfForm.msg.no_scheda_so_attiva"),
//				Labels.getLabel("messagebox.attention"),
//				Messagebox.OK,
//				Messagebox.EXCLAMATION);  
//          return;
//	}
	
	
	private void recuperaDatiSettaggio_() throws CariException {
		String punto = "recuperaDatiSettaggio ";
		Hashtable<String,String> datiDaInviare = new Hashtable<String,String>();
		String idSkSo = (id_skso.getValue() !=null ? id_skso.getValue()+"":"");
		datiDaInviare.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
		datiDaInviare.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
		
		recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,skf_descr_contatto,  skf_data, skf_fisiot_da);
		
		logger.debug(punto + " dati che invio>>"+ datiDaInviare);
//		if (ISASUtil.valida(idSksoV)){
			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
			ISASRecord dbrValutazione = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(), datiDaInviare);
			String codRichiedente = ISASUtil.getValoreStringa(dbrValutazione, "richiedente");
			String tipoUte = ISASUtil.getValoreStringa(dbrValutazione, "tipo_ute");
//			String prMotivo = ISASUtil.getValoreStringa(dbrValutazione, "pr_motivo");
//			String prMotivo = ISASUtil.getValoreStringa(dbrValutazione, "pr_motivo");
			String tipocure = ISASUtil.getValoreStringa(dbrValutazione, "tipocura");
			
			logger.debug(punto + " dati codRichiedente>"+codRichiedente+"<codRichiedente>"+codRichiedente+"< prMotivo>"+tipocure);
			cbx_provenienza.setSelectedValue(codRichiedente.equals("")?"9":codRichiedente);
			cbx_utenza.setSelectedValue(tipoUte);
			cbx_motivo.setSelectedValue(tipocure);
			cbx_provenienza.setRequired(!codRichiedente.equals(""));
			cbx_utenza.setRequired(!tipoUte.equals(""));
			
			if (ISASUtil.valida(tipocure)){
				cbx_motivo.setDisabled(true);
			}else {
				cbx_motivo.setDisabled(false);
			}
			
			cbx_provenienza.setDisabled(true);
			cbx_utenza.setDisabled(true);
			if (!this.skf_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
			this.skf_descr_contatto.setValue(skf_descr_contatto.getValue()+CostantiSinssntW.DA_SCHEDA_SO);
			
//		}
	}
	
	
//	private void onCloseSchedaSO(){
//		super.caribelContainerCtrl.hashChiaveValore.clear();
//		super.caribelContainerCtrl.hashChiaveValore.putAll(temp_container_hash);
//		
//	}
	
	@Override
	protected void notEditable() {
		cod_operatore.setReadonly(true);
        desc_operat.setReadonly(true);
	}
}

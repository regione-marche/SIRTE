package it.caribel.app.sinssnt.controllers.contattoGenerico;
//==========================================================================
//CARIBEL S.r.l.
//
//27/10/2014 --------Controller per Contatto Generico----
//Simone Puntoni
//==========================================================================

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.RtValutazSanEJB;
import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.sinssnt.bean.corretti.RLSkPuacEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.SkFpgEJB;
import it.caribel.app.sinssnt.controllers.ContainerGenericoCtrl;
import it.caribel.app.sinssnt.controllers.accessi_effettuati.AccessiEffettuatiGridCtrl;
import it.caribel.app.sinssnt.controllers.contatto.CaribelContattoFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.riepilogo_ausili_protesica.RiepilogoAusiliProtesicaGridCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalePanelCtr;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.storico_referente.StoricoGenericoReferenteGridCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.diagnosi.DiagnosiFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.ManagerOperatore;
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

public class ContattoGenFormCtrl extends CaribelContattoFormCtrl {

	private static final long serialVersionUID = 1L;
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/contatto_generico/contatto_gen.zul";
	
	private String myKeyPermission = ChiaviISASSinssntWeb.CONTATTO_GENERICO+"tipo_operatore";//vedi doInitForm()
	private SkFpgEJB myEJB = new SkFpgEJB();
	protected Window contattoGenForm;
	
	private String myTipoOpInstance = "";

//	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	private CaribelDatebox pr_data;


	private CaribelTextbox skfpg_motivo_txt;
	private CaribelDatebox skfpg_data_apertura;

	private CaribelDatebox skfpg_data_uscita;
	private CaribelTextbox txt_motchiusura;
	private CaribelTextbox skfpg_referente;
	private CaribelTextbox skfpg_operatore;
	private CaribelTextbox skfpg_tipo_operatore;
	private CaribelCombobox desc_skfpg_referente;
	private CaribelTextbox skfpg_cod_presidio;
	private CaribelCombobox desc_presidio;
	boolean messaggioAvvisoCartellaChiusa = true;
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
	
//	private Component sofc = null;
//	private Hashtable temp_container_hash = null;

	private Component sezione_ospedale;
	
//	private OspedaliSearchCtrl ospedaliSearch;
//	private RepartiSearchCtrl repartiSearch;

	private Tabpanels tabpanels_contatto_inf;
	private Tabs tabs_contatto_inf;
	private Tab presacarico_tab;
	private Tab segnalazione_tab;
	private Tab scaleval_tab;
	private Tab ambulatorio_tab;
	private Tab pa_tab;
	
//	private Component cs_operatore_referente;
	String id_skso_url = null;
	String skso_fonte = null;
	String nContattoURL = null;
	String nCartellaURL = null;	
	
	private static final int RISPOSTACHIUSURACONTATTO = 1;
	
	
	private Button btn_accessi;
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
//	private Tabpanel tabpanel_scale;	
	private Tabpanel tabpanel_pa;
	private Component panel_scale;

	// private CaribelRadiogroup ski_les_dec;
	// private CaribelRadiogroup ski_trasm_sk;
	private CaribelRadiogroup skfpg_dimis_progr;

	private Vector<Hashtable<String, String>> vettOper = new Vector<Hashtable<String, String>>();
	private CaribelDatebox skfpg_referente_da;
	private CaribelTextbox desc_operat;
	private CaribelDatebox dt_presa_carico;
	private CaribelCombobox skfpg_tipologia;
	private Button btn_protesica;
	private int check_save_step = 0;
	private Label label_dimis_progr_instead;
	private Component patologieGrid;
	protected CaribelListbox tablePrestazioni;
	private Component storico_operatore_ref = null;
	private CaribelTextbox skfpg_descr_contatto;
	private CaribelTextbox skfpg_anamnesi_1;
	private CaribelTextbox skfpg_anamnesi_2;
	private Label btn_protesica_placeholder;
	private Component contattoMedicoPatologia;
	private ISASRecord skso_op_coinvolti;
	private RMSkSOOpCoinvoltiEJB op_CoinvoltiEJB;
	private String ver ="4-";
	private boolean state_before_in_insert = false;
	
	public static final String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final String CTS_DATA_APERTURA = "skfpg_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "skfpg_data_chiusura";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";

	private Component cs_presidio;
	
	public void doInitForm() {
		try {
		 	logger.debug("inizio ");
			this.myTipoOpInstance = UtilForContainer.getTipoOperatorerContainer();
			contattoGenForm.setTitle(getLabelScheda(this.myTipoOpInstance));
			super.doInitForm();
			myKeyPermission = ChiaviISASSinssntWeb.CONTATTO_GENERICO+this.myTipoOpInstance;
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForDelete("deleteAll");
			
			doMakeControl();
			doPopulateCombobox();
			
			if (Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO)!=null)
				id_skso_url = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE)!=null)
				skso_fonte = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE);
			
			//Imposto subito il tipo operatore essendo generico
			this.skfpg_tipo_operatore.setValue(this.myTipoOpInstance);
			hParameters.put("skfpg_tipo_operatore", skfpg_tipo_operatore.getText());

			
			if (Executions.getCurrent().getParameter(CostantiSinssntW.N_CONTATTO)!=null)
				nContattoURL = Executions.getCurrent().getParameter(CostantiSinssntW.N_CONTATTO);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA)!=null)
				nCartellaURL = Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA);
			
			//########################### Recupero chiavi del contatto ###################################################################
			String nCartella 	= "";
			String nContatto 	= "";
			String prData 		= "";
			
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
					nContatto = recuperaNContatto(nCartella,skfpg_tipo_operatore.getText(),pr_data.getValueForIsas());
				}
			}
			//########################### Fine Recupero chiavi del contatto ##############################################################
			
			//Imposto le chiavi trovate
			if(ISASUtil.valida(nCartella)){
				n_cartella.setValue(new Integer(nCartella));
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
			}
			if(ISASUtil.valida(nContatto)){
				n_contatto.setValue(new Integer(nContatto));
				hParameters.put(CostantiSinssntW.N_CONTATTO, nContatto);
			}
			if(ISASUtil.valida(prData)){
				pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
				hParameters.put(CostantiSinssntW.PR_DATA, prData);
			}
			
			String dtCartellaChiusa =  ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
			//Eseguo la lettura da DB o mi preparo per l'insert
			if(ISASUtil.valida(nCartella) && 
					(ISASUtil.valida(nContatto)|| ManagerDate.validaData(dtCartellaChiusa)) ) {
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(nContatto)){
				//sono in inserimento					
				this.n_contatto.setValue(new Integer(0));
				this.skfpg_operatore.setValue(getProfile().getIsasUser().getKUser());
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
			}catch (Exception ex2) {
				doShowException(ex2);
			}
		}
	}

	private void gestionePianiAssistenziali() throws Exception, Exception {
		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			if(n_contatto.getValue()!= null && n_contatto.getValue()>0){//verifico che esista un contatto salvato
				pa_tab.setDisabled(false);
				tabpanel_pa.getLinkedTab().setDisabled(false);
				if(tabpanel_pa.getFirstChild()==null){
					HashMap<String, Object> map = new HashMap<String, Object>();			
					map.put("caribelContainerCtrl", this.caribelContainerCtrl);
					map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
					if(this.myTipoOpInstance.equals(GestTpOp.CTS_COD_ASSISTENTE_SOCIALE)){
						map.put(CostantiSinssntW.N_PROGETTO, n_contatto.getValue().toString());	
					}
					map.put(CostantiSinssntW.SKFPG_DATA_APERTURA, skfpg_data_apertura.getValueForIsas());
					if(skfpg_data_uscita.getValue()!=null){
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
			h.put(CostantiSinssntW.TIPO_OPERATORE,this.myTipoOpInstance);			
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
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_esistente",new String[]{skfpg_data_apertura.getText(),desc_operat.getText()}), 
						Labels.getLabel("messagebox.attention"),
						Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
						new EventListener<Event>() {
							public void onEvent(Event event)throws Exception {
								if (Messagebox.ON_YES.equals(event.getName())){
									id_skso.setValue(new Integer(id_skso_url));
//									recuperaDatiSettaggio();
									recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
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
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_utilizzato",new String[]{skfpg_data_apertura.getText(),desc_operat.getText()}), 
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
				recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
				id_skso_url=null;
				skso_fonte=null;
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				return;
			}
			
			
			
		} else if (skso_fonte != null && skso_fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"")){
			 
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
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
		}
	}
//		if (isInInsert()&&!fromDoSaveForm)  recuperaDatiSettaggio();
//	}
	protected void scriviPresaCarico(Hashtable h, boolean saveForm) throws Exception  {
		String punto = ver  + "scriviPresaCarico ";
		String data_pc = skfpg_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(skfpg_referente.getText())){
			skfpg_referente.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skfpg_referente_da.setValue(Calendar.getInstance().getTime());
			data_pc = skfpg_referente_da.getValueForIsas();
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



	private boolean scriviPrimaVisita() throws Exception{
		String data_pc = skfpg_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(skfpg_referente.getText())){
			skfpg_referente.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skfpg_referente_da.setValue(Calendar.getInstance().getTime());
			data_pc = skfpg_referente_da.getValueForIsas();
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
		
		this.skfpg_data_uscita.setValue(data_chiusura);
		this.txt_motchiusura.setValue(Labels.getLabel("Contatti.SchedaSO.chiusura_per_pc_da_lista_attiv"));
		h.put("n_progetto",n_contatto.getText());
		h.put("pa_tipo_oper",this.myTipoOpInstance);
		doSaveForm();
		Integer piano_assist=(Integer)invokeGenericSuEJB(new PianoAssistEJB(), h, "chiudi_piani");
		this.currentIsasRecord=null;
		this.caribelContainerCtrl.hashChiaveValore.remove(CostantiSinssntW.N_CONTATTO);
		this.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO,h.get(CostantiSinssntW.CTS_ID_SKSO));		
		((ContainerGenericoCtrl)this.caribelContainerCtrl).btn_contattoGenForm();
	}


//	private void recuperaDatiSettaggio_() throws CariException {
//		String punto = ver + "recuperaDatiSettaggio ";
//		
//		recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
//		if (this.currentIsasRecord == null && !isDataValida(skfpg_referente_da)) {
//			skfpg_referente_da.setValue(skfpg_data_apertura.getValue());
//		}
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
//			logger.debug(punto + " dati codRichiedente>"+codRichiedente+"<codRichiedente>"+codRichiedente+"< tipocure>"+tipocure);
//			cbx_provenienza.setSelectedValue(codRichiedente.equals("")?"9":codRichiedente);
//			cbx_utenza.setSelectedValue(tipoUte);
////			cbx_motivo.setSelectedValue(prMotivo);
//			cbx_motivo.setSelectedValue(tipocure);
////			cbx_motivo.setDisabled(true);
//			
//			if (ISASUtil.valida(tipocure)){
//				cbx_motivo.setDisabled(true);
//			}else {
//				cbx_motivo.setDisabled(false);
//			}
//			
//			cbx_provenienza.setRequired(!codRichiedente.equals(""));
//			cbx_utenza.setRequired(!tipoUte.equals(""));
//			
//			cbx_provenienza.setDisabled(true);
//			cbx_utenza.setDisabled(true);
//			if (!this.skfpg_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
//			this.skfpg_descr_contatto.setValue(skfpg_descr_contatto.getValue()+CostantiSinssntW.DA_SCHEDA_SO);
////		}
//	}



	private void doMakeControlAfterRead() throws WrongValueException, Exception {
//		if (getProfile().getStringFromProfile("ctrl_skinf_siad").equals(CostantiSinssntW.SI) && 
//				isInUpdate() && cbx_motivo.isRequired())
//		{
//			if (cbx_motivo.getSelectedValue().equals(""))
//			cbx_motivo.setDisabled(false);
//			else cbx_motivo.setDisabled(true);
//		}
//		else cbx_motivo.setDisabled(false);
			
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
        	 
        	  
        	  
        	  //JTabbedPane1.add(JPanelAmbulatorio, "JPanelAmbulatorio");spostato elisa b 11/02/11
          // 05/06/09 m.: aggiungo pannello scale valutaz
//          if (jPanScale == null)
//              gest_pannello_scale();
//          else {
//              jPanScale.settaDtApeAllScale((String)JCariDateTextFieldDataApertura.getUnmaskedText());
//              jPanScale.settaDtApeSkVal((String)jCariDateTextFieldSkVal.getUnmaskedText());
//              jPanScale.settaNCart((String)JCariTextFieldCodice.getUnmaskedText()); // 31/03/10
//              jPanScale.getTempoT();
//          }
        	  
        	// 14/05/10: quando si proviene da "JFElencoCasi" ---
//              if (h_Ass.get("daEleCasi") != null) {
//                  daEleCasi = ((Boolean)h_Ass.get("daEleCasi")).booleanValue();
//                  h_Ass.remove("daEleCasi");
//      System.out.println("--- JFSkInf: daEleCasi=["+(daEleCasi?"S":"N")+"]");
//                  // 13/02/13: ZONA EROGAZIONE x FLUSSI RFC115 ---
//                  if (JPanelDettPresaCarico != null)
//                      JPanelDettPresaCarico.impostaComboZonaE();
//                  // 13/02/13 ---
//              }
//
//              // 14/05/10 ---
//      		
//      		JPanelDettSegn.settaStatoSegnalazione(stato, dtSkVal, numCart);
//  			if(stato == CONSULTA)
//  				JPanelDettPresaCarico.settaStatoPresaCaricoStorico(this.JCariDateTextFieldDataApertura.getUnmaskedText(), numCart);
//  				else
//  				JPanelDettPresaCarico.settaStatoPresaCarico(stato, dtSkVal, numCart);

        }else if (ManagerProfile.isConfigurazioneLazio(getProfile())){ // equivalente di getDatiUV_RL
        	
                //29/03/11 si va a leggere anche il valore della tipologia
                Hashtable<String,String> ht = new Hashtable<String,String>();

                ht.put("n_cartella",n_cartella.getText());
                ht.put("pr_data",this.caribelContainerCtrl.hashChiaveValore.get("pr_data").toString());
                if(!skfpg_data_apertura.getText().isEmpty())
                    ht.put("data_apertura",skfpg_data_apertura.getText());
                if(!skfpg_data_uscita.getText().isEmpty())
                    ht.put("data_chiusura",skfpg_data_uscita.getText());

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
                	 skfpg_motivo_txt.setText(hPUA.get("motivo").toString());
                	 skfpg_motivo_txt.setDisabled(true);
                   
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
		return skfpg_data_uscita.getText() != null && !skfpg_data_uscita.getText().isEmpty();
	}
	
	public boolean isContattoInfAperto()
	{
		return skfpg_data_apertura.getText()!= null && !skfpg_data_apertura.getText().isEmpty();
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
        if (!esisteCasoAttivo) h.put("dt_presa_carico", skfpg_data_apertura.getValueForIsas());
        try {
			return ((Boolean)invokeGenericSuEJB(new RtValutazSanEJB(), h, "query_PrimaValutazione")).booleanValue();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}



	private String recuperaNContatto(String nCartella,String tipo_operatore, String prData)
			throws Exception {
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put("skfpg_tipo_operatore", tipo_operatore);
		dati.put(CostantiSinssntW.PR_DATA, prData);

		ISASRecord dbrSkMed = null;
//		try {
			dbrSkMed = invokeSuEJB(myEJB, dati, "getContattoGenCorrente");
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
		skfpg_motivo_txt.setVisible(false);
		skfpg_motivo_txt.setRequired(false);
		btn_rfc115_valutazione.setVisible(false);
		
		label_dimis_progr.setVisible(false);
		label_dimis_progr_instead.setVisible(true);
		skfpg_dimis_progr.setVisible(false);
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
			cbx_motivo.setReadonly(false);
			cbx_motivo.invalidate();	
			sezione_ospedale.setVisible(false);
			btn_protesica.setVisible(false);
			btn_protesica_placeholder.setVisible(true);
			skfpg_descr_contatto.setRequired(false);
			UtilForBinding.setComponentReadOnly(contattoMedicoPatologia,true);
			skfpg_anamnesi_2.setDisabled(true);
			skfpg_anamnesi_1.setDisabled(true);
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
			cbx_motivo.setRequired(false);			
			sezione_ospedale.setVisible(false);
			btn_protesica.setVisible(false);
			btn_protesica_placeholder.setVisible(true);
			skfpg_descr_contatto.setRequired(false);
			UtilForBinding.setComponentReadOnly(contattoMedicoPatologia,true);
			skfpg_anamnesi_2.setDisabled(true);
			skfpg_anamnesi_1.setDisabled(true);

		} else if (ManagerProfile.isConfigurazioneLazio(getProfile())) {
			cbx_tipologia.setVisible(true);
			cbx_tipologia.setRequired(true);
			skfpg_motivo_txt.setVisible(true);
			skfpg_motivo_txt.setRequired(true);
			cbx_motivo.setVisible(false);
			cbx_motivo.setRequired(false);
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
			skfpg_dimis_progr.setVisible(true);
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
		map.put(CTS_TIPO_OPERATORE, this.myTipoOpInstance);
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
		map.put(CTS_TIPO_OPERATORE, this.myTipoOpInstance);
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
		this.skfpg_data_uscita.setText(null);
		//this.skfpg_data_uscita.setValue(null);
		this.cbx_motchiusura.setSelectedIndex(-1);
		this.txt_motchiusura.setValue("");
		
		
		// aggiorno il record
        boolean risu = doSaveForm();

        this.setReadOnly(false);
        skfpg_operatore.setDisabled(true);
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
		h_ctrl.put("dataRif", (String)this.skfpg_data_apertura.getText());
		h_ctrl.put("soloAperti", (soloAperti?"S":"N"));
		return ((Boolean)invokeGenericSuEJB(myEJB, h_ctrl, "query_checkContSuccessivi")).booleanValue();
	}

	public void onStorico(ForwardEvent e) throws Exception {
		
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, skfpg_tipo_operatore.getValue());

		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerGenericoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerGenericoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerGenericoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}

		map.put(StoricoGenericoReferenteGridCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
		map.put("zul_chiamante", (ContattoGenFormCtrl)this);//Utile per l'aggiornamento da finestra modale
		map.put("skfpg_tipo_operatore",skfpg_tipo_operatore.getText());
		//TODO non permettere la riapertura se la finestra è gia aperta
		if (storico_operatore_ref == null) {
			storico_operatore_ref = Executions.getCurrent().createComponents(StoricoGenericoReferenteGridCtrl.myPathFormZul, self, map);
			storico_operatore_ref = null;
		} 
	}


	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put(CTS_DATA_APERTURA, skfpg_data_apertura.getValue());
		if (skfpg_data_uscita != null)
			map.put(CTS_DATA_CHIUSURA, skfpg_data_uscita.getValue());
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
		h.put("data_apertura", skfpg_data_apertura.getValueForIsas());
		if (skfpg_data_uscita != null)
			h.put("data_chiusura", skfpg_data_uscita.getValueForIsas());
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
		loadTipologia(skfpg_tipologia);
		
		
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
		
		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SEGNALANTE, cbx_provenienza);
		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_ICHIUS, cbx_motchiusura);
//		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_MOTIVO, cbx_motivo);
		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
				"tab_descrizione", false);
		CaribelComboRepository.comboPreLoad("contatto_inf_tiputes",
				new TiputeSEJB(), "query", new Hashtable<String, String>(), cbx_utenza, null,
				"codice", "descrizione", false);
		
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(cbx_motivo);
		
		String linea = CaribelComboRepository.stampaCompbo(cbx_motchiusura);
		logger.trace(" combo motChiugen>>\n" +linea);
		
	}

	private void abilitazioniMaschera() throws WrongValueException, Exception {
		boolean contattoDisabilitato = false;
		String punto = ver + "abilitazioneMaschera ";
		logger.trace(punto);
		boolean abilitaBottoneRiapri = abilitaBottoneRiapri();
		
		verificaStatoCartella();
		
		if (isInInsert()){
			if(ManagerProfile.getTipoOperatore(getProfile()).equals(UtilForContainer.getTipoOperatorerContainer())){
				skfpg_referente.setValue(getProfile().getStringFromProfile(
					ManagerProfile.CODICE_OPERATORE));
				desc_skfpg_referente.setValue(getProfile().getStringFromProfile(
					ManagerProfile.COGNOME_OPERATORE)
					+ " "
					+ getProfile().getStringFromProfile(
							ManagerProfile.NOME_OPERATORE));
			}
			String codPresidio = getProfile().getStringFromProfile(
					ManagerProfile.PRES_OPERATORE);
			skfpg_cod_presidio.setValue(codPresidio);
			desc_presidio.setValue(ManagerDecod.decodPresidi(getProfile(),
					codPresidio));
			btn_accessi.setDisabled(true);
			btn_storico.setDisabled(true);
			btn_rug_svama.setDisabled(true);
			skfpg_data_uscita.setDisabled(true);
			cbx_motchiusura.setDisabled(true);
			btn_segnalazione.setDisabled(true);
			btn_protesica.setDisabled(true);
			boolean proporreDtScheda = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_PROPORRE_DT_SCHEDA);
			Date dataAccettazioneSkSo = recuperaDataAccettazioneSkSo(n_cartella, id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
			if (ManagerDate.validaData(dataAccettazioneSkSo)) {
				skfpg_data_apertura.setValue(dataAccettazioneSkSo);
				skfpg_referente_da.setValue(dataAccettazioneSkSo);
			} else {
				if (proporreDtScheda) {
					if (!ContattoInfFormCtrl.anagraficaChiusa(this.hParameters, skfpg_data_apertura, skfpg_referente_da)){
						skfpg_data_apertura.setValue(procdate.getDate());
						skfpg_referente_da.setValue(procdate.getDate());
					}
				}
			}
		}
		if (isInUpdate()){
			try {
				contattoDisabilitato = id_skso.getValue()>0;
			} catch (Exception e) {
			}
			btn_accessi.setDisabled(false);
			btn_storico.setDisabled(false);
			((CaribelSearchCtrl) cs_operatore_referente.getAttribute(MY_CTRL_KEY)).setReadonly(true);
//			((CaribelSearchCtrl) cs_presidio.getAttribute(MY_CTRL_KEY)).setReadonly(true);

			
			if (this.skfpg_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
			{
				cbx_provenienza.setDisabled(true);
				cbx_utenza.setDisabled(true);
//				cbx_motivo.setDisabled(true);
				cbx_provenienza.setRequired(false);				
				cbx_utenza.setRequired(false);
//				cbx_motivo.setRequired(false);
			}
			
		}
		
		btn_riapri.setDisabled(isInInsert() || (isInUpdate() && !isContattoInfChiuso() && abilitaBottoneRiapri)  );
		
		
		boolean abilChiu = ((getProfile().getIsasUser().canIUse(ManagerProfile.CLOSE_SCHEDA_INF,"MODI"))
				&& (!isContattoInfChiuso()) && (isContattoInfAperto()));
		
		this.skfpg_data_uscita.setDisabled(!abilChiu);
		this.cbx_motchiusura.setDisabled(!abilChiu);
		this.txt_motchiusura.setDisabled(!abilChiu);
		
		if (isContattoInfChiuso()){
			this.setReadOnly(true);
			btn_riapri.setDisabled(false);
			btn_accessi.setDisabled(false);
			btn_rug_svama.setDisabled(false);
			btn_segnalazione.setDisabled(false);
			btn_protesica.setDisabled(false);
			contattoDisabilitato= true;
			
			boolean abilRiap = (getProfile().getIsasUser().canIUse(ManagerProfile.REOPEN_SCHEDA_INF,"MODI"));
			this.btn_riapri.setVisible(abilRiap);
			this.btn_riapri.setDisabled(!abilitaBottoneRiapri);
			
		}
		//passaggio di parametri a container per successivi controlli e abilitazioni pulsanti sinistra
//		recuperaDatiSettaggio();
		recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
		
		if (!isInInsert())
		super.caribelContainerCtrl.hashChiaveValore.put(
				CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		super.caribelContainerCtrl.hashChiaveValore.put(
				"contattoInfInUpdate", new Boolean(isInUpdate())
				);
		super.caribelContainerCtrl.hashChiaveValore.put(
				"contattoGenInInsert", new Boolean(isInInsert())
				);
		super.caribelContainerCtrl.hashChiaveValore.put(
				"contattoGenReadOnly", new Boolean(isReadOnly())
				);
		if (cbx_motivo.isRequired()){
		super.caribelContainerCtrl.hashChiaveValore.put(
				"cbx_motivo", cbx_motivo.getSelectedValue());
		}
		super.caribelContainerCtrl.hashChiaveValore.put(
				"skfpg_data_apertura", skfpg_data_apertura.getValueForIsas());
		super.caribelContainerCtrl.hashChiaveValore.put(
				"skfpg_data_uscita", skfpg_data_uscita.getValueForIsas());
		
		tabpanel_scale.getLinkedTab().setDisabled(isInInsert());
		impostaCollegamentoSo(btn_scheda_so, this.currentIsasRecord);
	
	}

	private boolean abilitaBottoneRiapri() throws Exception {
		String punto = ver + "abilitaBottoneRiapri ";
		boolean abilitaBottoneRiapri = false;
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		dati.put(CostantiSinssntW.CTS_SKFPG_TIPO_OPERATORE, this.myTipoOpInstance);
		
		logger.trace(punto + " dati>>" +dati);
		int nMaxContatto = myEJB.recuperaMaxContatto(CaribelSessionManager.getInstance().getMyLogin(), dati);
		
		abilitaBottoneRiapri = ( (n_contatto.getValue()!=null && 
				(n_contatto.getValue().intValue() == nMaxContatto)));
		logger.trace(punto + " abilitareBottone>>" + abilitaBottoneRiapri +"<< nMaxContatto>"+nMaxContatto +"<");
		
		return abilitaBottoneRiapri;
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
			skfpg_data_apertura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
			skfpg_data_uscita.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
		}
		skfpg_data_apertura.setReadonly(isInUpdate());
	}

	@Override
	public boolean doValidateForm() throws Exception {
		
		boolean canSave = false;
		
		
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		//		myEJB.insert(CaribelSessionManager.getInstance().getMyLogin(), dati);
		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITAZIONE_RFC115_6)) {
			/* TODO IMPLEMENTEARE */
		}
		
		if (this.skfpg_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO)){
			skfpg_descr_contatto.setValue(skfpg_data_apertura.getText()+CostantiSinssntW.DA_SCHEDA_SO);
		}
		else skfpg_descr_contatto.setValue(skfpg_data_apertura.getText());
		
						
		switch(check_save_step){
		case 0: if(!controlloDatiSalvataggio()) return false;
		case 1: settaDati(); canSave = true; check_save_step = 0;break;
		}		
		return canSave;
}
	
	private void settaDati() throws Exception {
		if (this.currentIsasRecord == null && !isDataValida(skfpg_referente_da)) {
			skfpg_referente_da.setValue(skfpg_data_apertura.getValue());
		}
		skfpg_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		skfpg_tipo_operatore.setText(this.myTipoOpInstance);
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
				Date dataApertura = skfpg_data_apertura.getValue();
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
		
		boolean isChiusura = skfpg_data_uscita.getValue() != null;
		boolean isMotivoChiusuraInserito = isMotivoChiusuraInserito();

		if (isChiusura) {
			canSave = ManagerDate.controllaPeriodo(self, skfpg_data_apertura, skfpg_data_uscita, "lb_skfpg_data_apertura", "lb_skfpg_data_uscita");
			if (!canSave) return false;
			
			if (!isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg"));
				if (ManagerProfile.isConfigurazioneToscana(getProfile()))
				cbx_motchiusura.focus();
				else txt_motchiusura.focus();
				skfpg_data_uscita.setValue(null);			
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
//		if (id_skso_hash !=null || id_skso_temp != null){
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


	private Hashtable<String, String> selectControlloData() throws SQLException {
		Hashtable<String, String> datiRicevuti = new Hashtable<String, String>();
		Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
		controlloInterv.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		controlloInterv.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
		controlloInterv.put("skfpg_tipo_operatore", skfpg_tipo_operatore.getText());
		
		//		super.select("query_controlloData",profile.getParameter("skmed"),t);
		ISASRecord dbrInterv = myEJB.query_controlloData(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);

		datiRicevuti.put(CTS_TROVA_INVERV, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV));
		datiRicevuti.put(CTS_TROVA_INVERV_MAX, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV_MAX));
		logger.debug( " dati recuperati>>" + (datiRicevuti != null ? datiRicevuti + "" : " no dati") + "");

		return datiRicevuti;
	}
	
	public void onBlurDataApertura(Event event) throws Exception {
		if (skfpg_referente_da.getValue()==null || this.isInInsert()) skfpg_referente_da.setValue(skfpg_data_apertura.getValue());
		
	}


	@Override
	protected boolean doSaveForm() throws Exception {			
		String punto = ver + "doSaveForm ";
		state_before_in_insert = isInInsert();
		if (state_before_in_insert && id_skso_url!=null)
			{this.id_skso.setValue(new Integer(id_skso_url));
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skfpg_data_apertura, skfpg_referente_da);
			}
		boolean ret = false;
		try {
			if (!ManagerDate.controllaPeriodo(self, skfpg_data_apertura, skfpg_data_uscita, "lb_skfpg_data_apertura","lb_skfpg_data_uscita")){
				logger.trace(punto + " data Non corretta ");
				return false;
			}
			if (isInUpdate()){
				String dataContatto =ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skfpg_data_apertura");
				String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skfpg_data_uscita");

				ContattoInfFormCtrl cont = new ContattoInfFormCtrl();
				if (cont.okControlliChiusura(skfpg_data_apertura, skfpg_data_uscita,dataContatto, dataCorrenteChiu, n_cartella.getText(), n_contatto.getText())){
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
	public void settaInfermiereReferente(String codice, String descrizione, Date dataDa) throws Exception {
		skfpg_referente.setValue(codice);
		desc_skfpg_referente.setValue(descrizione);
		skfpg_referente_da.setValue(dataDa);
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
						.getValueForIsas(skfpg_data_apertura.getValue()));
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
				if (skfpg_data_apertura != null)
					map.put("data_ap", skfpg_data_apertura.getValueForIsas());
				else if (pr_data != null) // dt ape skVal
					map.put("data_ap", pr_data.getValueForIsas());
				// dt chius skInf
				if (skfpg_data_uscita != null)
					map.put("data_ch", skfpg_data_uscita.getValueForIsas());

				map.put("tipo_op",this.myTipoOpInstance);
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
			dati.put("data_ap", skfpg_data_apertura.getText());
			dati.put("data_chiu", skfpg_data_uscita.getText());
			dati.put("assistito", UtilForContainer.getCognomeNomeAssistito());
			dati.put("operatore", ManagerProfile.getCognomeNomeOperatore(getProfile()));
			Executions.getCurrent().createComponents(StampaContattoGenCtrl.CTS_FILE_ZUL, self, dati);
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
			caribelContainerCtrl.doRefreshOnDelete(contattoGenForm);	

			
		}
	}
	
	public static String getLabelScheda(String tipo_oper) throws Exception{
		String labelScheda = null;
		Hashtable<String, String> tipiOp = ManagerOperatore.getTipiOperatori(CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE);
		String descrTipoOperatore = tipiOp.get(tipo_oper);
		if(descrTipoOperatore!=null && !descrTipoOperatore.trim().equals("")){
			labelScheda = Labels.getLabel("menu.toolbar.scheda")+" "+descrTipoOperatore;
		}
		return labelScheda;
	}
	
}

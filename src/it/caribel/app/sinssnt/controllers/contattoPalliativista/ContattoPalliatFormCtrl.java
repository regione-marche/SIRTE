package it.caribel.app.sinssnt.controllers.contattoPalliativista;


import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.common.ejb.OperatoriEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.RtValutazSanEJB;
import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.sinssnt.bean.corretti.RLSkPuacEJB;
import it.caribel.app.sinssnt.bean.modificati.IntervEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkMedPalEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.controllers.ContainerGenericoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerInfermieristicoCtrl;
import it.caribel.app.sinssnt.controllers.accessi_effettuati.AccessiEffettuatiGridCtrl;
import it.caribel.app.sinssnt.controllers.contatto.CaribelContattoFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.riepilogo_ausili_protesica.RiepilogoAusiliProtesicaGridCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalePanelCtr;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.storico_referente.StoricoPalliativistaReferenteGridCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.diagnosi.DiagnosiFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.app.sinssnt.util.UtilForContainerGen;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
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
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

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
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;
//import it.caribel.app.sinssnt.controllers.storico_referente.StoricoGenericoReferenteGridCtrl;
//import it.caribel.app.sinssnt.controllers.storico_referente.StoricoMedicoPalliatReferenteGridCtrl;

public class ContattoPalliatFormCtrl extends CaribelContattoFormCtrl {

	private static final long serialVersionUID = 1L;
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/contatto_palliativista/contatto_palliat.zul";

	private String myKeyPermission = ChiaviISASSinssntWeb.SKMEDPAL;//vedi doInitForm()
	//private SkFpgEJB myEJB = new SkFpgEJB();
	private SkMedPalEJB myEJB = new SkMedPalEJB();
	protected Window contattoPalliatForm;

	private String myTipoOpInstance = "";

	//	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	private CaribelDatebox pr_data;

	private CaribelTextbox skfpg_motivo_txt;
	private CaribelDatebox skm_data_apertura;

	private CaribelDatebox skfpg_data_uscita;    
	private CaribelTextbox txt_motchiusura;
	private CaribelTextbox skfpg_referente;
	private CaribelTextbox skfpg_operatore;
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
	public static Tab scaleval_tab;
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
	public static final String CTS_DATA_APERTURA = "skm_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "skm_data_chiusura";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";

	private Component cs_presidio;

	//Carlo Volpicelli	
	public static final String CTS_ZUL_CHIAMANTE = "zul_chiamante";

	private Component cs_operatore;  //caribel search dell'operatore
	private CaribelSearchCtrl operatoreSearchCtrl;  
	private CaribelCombobox operatore;
	private CaribelTextbox tb_filter3; //campo codice del caribel search dell'operatore

	//anamnesi
	private CaribelDatebox skm_segnala_data;
	private CaribelDatebox skm_medico_da;
	private CaribelCombobox skm_segnala;
	private CaribelRadiogroup skm_sedazione; 
	private CaribelTextbox skm_descr_contatto;
	private Component cs_medicoReferente;
	private Component cs_skm_cod_presidio;
	private CaribelCombobox skm_attivazione;
	private CaribelCombobox desc_medico;
	private CaribelTextbox skm_anamnesi;
	private CaribelTextbox codMedicoReferente;
	private CaribelTextbox cod_presidio;
	private CaribelTextbox ospedale_dimiss;
	private CaribelTextbox reparto_dimiss;
	private CaribelDatebox skm_data_dimiss;
	private Component cs_ospedali;
	private Component cs_reparti;

	//situazione iniziale
	private CaribelCombobox skm_pvisita;
	private CaribelDatebox skm_presacarico_data;
	private CaribelDatebox skm_pvisita_data;
	private CaribelTextbox id_cod_medicoCurante;
	private CaribelCheckbox skm_np_diag_altro;
	private CaribelTextbox skm_np_diag_altro_descr;
	private CaribelRadiogroup skm_lesioni_altro;
	private CaribelTextbox skm_lesioni_altro_descr;
	private Radio lesioniAltroSi;
	private Radio lesioniAltroNo;
	//private CaribelRadiogroup skm_presacarico;

	//segnalazione
	private CaribelCombobox stato_civile;
	private CaribelCombobox titolo_studio;
	private CaribelCombobox richiedente;
	private CaribelCombobox zona_segnalazione;
	private CaribelCombobox continuita_osp_territ;

	//conclusione
	private CaribelDatebox skm_data_chiusura;
	private CaribelCombobox skm_motivo_chius;
	private CaribelCombobox skm_deceduto;
	private CaribelCombobox skm_presenti;
	//private Button btn_riapri;

	//Situazione familiare
	private CaribelCombobox grado_consapevolezza_iniziale;
	private CaribelCombobox grado_consapevolezza_ultSett;
	private CaribelCombobox grado_consapevolezza;
	private CaribelRadiogroup quanti_consapevoli;
	private Radio consapevoli_tutti;
	private Radio consapevoli_tutti_eccetto;
	private CaribelTextbox familiari_consapevoli;
	private CaribelCombobox skm_badante_it;

	private CaribelCheckbox skp_sondino;

	private static int insert = 0;

	public void doInitForm() 
	{
		try 
		{
			super.doInitForm();
			myKeyPermission = ChiaviISASSinssntWeb.SKMEDPAL;
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			this.myTipoOpInstance = UtilForContainer.getTipoOperatorerContainer();
			
			if (Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO)!=null)
				id_skso_url = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE)!=null)
				skso_fonte = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.N_CONTATTO)!=null)
				nContattoURL = Executions.getCurrent().getParameter(CostantiSinssntW.N_CONTATTO);
			if (Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA)!=null)
				nCartellaURL = Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA);

			String nCartella 	= "";
			String nContatto 	= "";			

			if (ISASUtil.valida(nContattoURL)){
				nContatto = nContattoURL;
			}
			if (ISASUtil.valida(nCartellaURL)){
				nCartella = nCartellaURL;
			}

			//cv: carico il codice, la descrizione e il tipo dell'operatore che sta inserendo i dati
			String codOperatore = (String) CaribelSessionManager.getInstance().getParamFromProfile(ManagerProfile.CODICE_OPERATORE);
			operatoreSearchCtrl = ((CaribelSearchCtrl)cs_operatore.getAttribute(MY_CTRL_KEY)); //ottengo il controller del componente cs_operatore
			tb_filter3.setValue(codOperatore);
			Events.sendEvent(Events.ON_CHANGE, tb_filter3, null);

			loadCombo(skm_attivazione, skm_segnala, skm_pvisita, stato_civile, titolo_studio, 
					richiedente, skm_motivo_chius, skm_deceduto, skm_presenti, grado_consapevolezza_iniziale,
					grado_consapevolezza_ultSett, grado_consapevolezza, skm_badante_it);

			//gestisciFamiliariConsapevoli_(); //se è selezionato "tutti eccetto" nel panel familiari nel tab situazione familiare, allora la textbox per indicare i familiari è abilitata

			if (ISASUtil.valida((String)arg.get(CostantiSinssntW.N_CARTELLA)) && ISASUtil.valida((String)arg.get(CostantiSinssntW.N_CONTATTO))) 
			{
				insert = 0;
				//Arrivo da una chiamata esplicita per cui ho i parametri in arg
				nCartella 	= (String)arg.get(CostantiSinssntW.N_CARTELLA);
				nContatto 	= (String)arg.get(CostantiSinssntW.N_CONTATTO);
			}
			else if (dbrFromList != null) 
			{
				insert = 0;

				//arrivo da una griglia di contatti (es. storico), quindi caso UPDATE				
				nCartella 	= ((Integer)dbrFromList.get(CostantiSinssntW.N_CARTELLA)).toString();
				nContatto 	= ((Integer)dbrFromList.get(CostantiSinssntW.N_CONTATTO)).toString();

				hParameters.put("n_cartella", nCartella);
				hParameters.put("n_contatto", nContatto);

				doQueryKeySuEJB();					
				doWriteBeanToComponents();

				Events.sendEvent(Events.ON_CHANGE, id_cod_medicoCurante, null);

				if(currentIsasRecord != null)
				{
					//abilito il pulsante "riapri" nel panel "conclusioni" nel caso sia presente una data di chiusura
					if(currentIsasRecord.get("skm_data_chiusura") != null)
					{
						btn_riapri.setDisabled(false);
					}			

					//se la combobox "motivo" nel panel "conclusione", le combo "luogo decesso" e 
					//"familiari presenti" vengono disabilitate
					String motivo = skm_motivo_chius.getValue();

					if(!motivo.equals("Decesso"))
					{
						UtilForBinding.setComponentReadOnly(skm_deceduto, true);
						UtilForBinding.setComponentReadOnly(skm_presenti, true);
					}

					UtilForBinding.setComponentReadOnly(cs_medicoReferente, true);
				}

			}
			else if (super.caribelContainerCtrl != null
					&& super.caribelContainerCtrl.hashChiaveValore != null
					&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA) != null 
					&& !(ISASUtil.valida(nContatto))) 
			{
				//arrivo da nuovo contatto o da qualunque altro posto

				if(skm_lesioni_altro.getSelectedValue().equals("S"))
					skm_lesioni_altro_descr.setReadonly(false);
				else
					skm_lesioni_altro_descr.setReadonly(true);

				System.out.println("Arrivo da nuovo contatto o qualsiasi altro posto");
				nCartella = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.N_CARTELLA);
				nContatto = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,CostantiSinssntW.N_CONTATTO);
				if (!ISASUtil.valida(nContatto)) 
				{
					//Recupero il contatto piu recente
					nContatto = recuperaNContatto(nCartella);
					super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, nContatto);
				}   
			}
			

			//Imposto le chiavi trovate
			if(ISASUtil.valida(nCartella)){
				n_cartella.setValue(new Integer(nCartella));
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
			}
			if(ISASUtil.valida(nContatto)){
				n_contatto.setValue(new Integer(nContatto));
				hParameters.put(CostantiSinssntW.N_CONTATTO, nContatto);
			}

			String dtCartellaChiusa =  ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
			//Eseguo la lettura da DB o mi preparo per l'insert
			if(ISASUtil.valida(nCartella) && 
					(ISASUtil.valida(nContatto) || ManagerDate.validaData(dtCartellaChiusa)) ) 
			{
				doQueryKeySuEJB();				
				doWriteBeanToComponents();

				if(skm_data_apertura.getValue()==null)
					insert = 1;
				else
					insert = 0;
				
				if(insert==1)
				{
					OperatoriEJB op = new OperatoriEJB();					                   

					//imposto automaticamente il medico referente in base all'operatore loggato
					codMedicoReferente.setValue(codOperatore);                                 
					Events.sendEvent("onChange", codMedicoReferente, null);
					
					myLogin ml = CaribelSessionManager.getInstance().getMyLogin();             
					Hashtable ht = new Hashtable();                                            
					ht.put("codice", codOperatore);                                            
					ISASRecord dbr = op.queryKey(ml, ht);                                      
					Hashtable ht1 = new Hashtable();                                           
					ht1 = dbr.getHashtable(); 
					Object o = ht1.get("cod_presidio");
					String codicePresidio = "";                                                
					if(null!=dbr && o!=null)                              
					{                                                                          
						codicePresidio = dbr.get("cod_presidio").toString();                   
						cod_presidio.setValue(codicePresidio);                                 
						Events.sendEvent("onChange", cod_presidio, null);                      
					}		
				}

				gestisciFamiliariConsapevoli_(); //se è selezionato "tutti eccetto" nel panel familiari nel tab situazione familiare, allora la textbox per indicare i familiari è abilitata

				if(skm_lesioni_altro.getSelectedValue().equals("S"))
					skm_lesioni_altro_descr.setReadonly(false);
				else
					skm_lesioni_altro_descr.setReadonly(true);

				if(skm_np_diag_altro.isChecked())
					skm_np_diag_altro_descr.setReadonly(false);
				else
					skm_np_diag_altro_descr.setReadonly(true);

				if(null != skm_data_chiusura.getValue())
				{
					this.setReadOnly(true);
				}


				//se la combobox "motivo" nel panel "conclusione", le combo "luogo decesso" e 
				//"familiari presenti" vengono disabilitate
				String motivo = skm_motivo_chius.getValue();

				if(!motivo.equals("Decesso"))
				{
					UtilForBinding.setComponentReadOnly(skm_deceduto, true);
					UtilForBinding.setComponentReadOnly(skm_presenti, true);
				}

				//abilito il pulsante "riapri" nel panel "conclusione" nel caso sia presente la data 
				//chiusura 
				if(currentIsasRecord!=null)
				{
					if(currentIsasRecord.get("skm_data_chiusura") != null)
					{
						btn_riapri.setDisabled(false);
					}
				}			
				
				Events.sendEvent(Events.ON_CHANGE, codMedicoReferente, null);
				Events.sendEvent(Events.ON_CHANGE, cod_presidio, null);
				Events.sendEvent(Events.ON_CHANGE, ospedale_dimiss, null);
				Events.sendEvent(Events.ON_CHANGE, reparto_dimiss, null);
				Events.sendEvent(Events.ON_CHANGE, id_cod_medicoCurante, null);

			}else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(nContatto))
			{
				insert=1;
				//sono in inserimento					
				this.n_contatto.setValue(new Integer(0));
				tb_filter3.setValue(getProfile().getIsasUser().getKUser());
				caricaPatologie();
			}
			
			//gestioneListaAttivita(false);
			//gestioneRichiestaChiusura(false);
			
		}catch (Exception ex){
			doShowException(ex);
		}
	}

	//Carlo Volpicelli
	private void loadCombo(CaribelCombobox cbx1, CaribelCombobox cbx2, CaribelCombobox cbx3,
			CaribelCombobox cbx4, CaribelCombobox cbx5, CaribelCombobox cbx6,
			CaribelCombobox cbx7, CaribelCombobox cbx8, CaribelCombobox cbx9,
			CaribelCombobox cbx10, CaribelCombobox cbx11, CaribelCombobox cbx12,
			CaribelCombobox cbx13)
	{
		try{
			Hashtable h_xCBdaTabBase = new Hashtable(); // x le Combobox da caricare
			Hashtable h_xLabdaTabBase = new Hashtable(); // x le Label da caricare
			h_xCBdaTabBase.put("SPATTI", cbx1);  //il primo parametro è il nome che identifica la prima  da caricare, contenuto nella colonna tab_cod della vista tab_voci; il secondo parametro è la combobox da popolare.

			h_xCBdaTabBase.put("CPSEGNAL", cbx2); //

			h_xCBdaTabBase.put("CPVISITA", cbx3);
			//h_xCBdaTabBase.put("STACIV", cbx4);  non presente in tab_voci
			h_xCBdaTabBase.put("FMRICH", cbx6);
			h_xCBdaTabBase.put("MCHIUS", cbx7);
			h_xCBdaTabBase.put("LUODEC", cbx8);
			h_xCBdaTabBase.put("CPPRESEN", cbx9);
			h_xCBdaTabBase.put("CPCONSAP", cbx10);
			//h_xCBdaTabBase.put("CPCONSAP", cbx11);

			Hashtable<String, Object> h = new Hashtable();
			CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h, 
					h_xCBdaTabBase, h_xLabdaTabBase, "tab_val", "tab_descrizione", false);
		}catch(Exception e){
			doShowException(e);
		}

		//combo stato civile 
		cbx4.clear();
		CaribelComboRepository.addComboItem(cbx4, " ", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.statoCivile.0"));
		CaribelComboRepository.addComboItem(cbx4, "1", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.statoCivile.1"));
		CaribelComboRepository.addComboItem(cbx4, "2", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.statoCivile.2"));
		CaribelComboRepository.addComboItem(cbx4, "3", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.statoCivile.3"));
		CaribelComboRepository.addComboItem(cbx4, "4", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.statoCivile.4"));
		cbx4.setSelectedValue(" ");

		//combo titolo studio
		cbx5.clear();
		CaribelComboRepository.addComboItem(cbx5, " ", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.0"));
		CaribelComboRepository.addComboItem(cbx5, "1", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.1"));
		CaribelComboRepository.addComboItem(cbx5, "2", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.2"));
		CaribelComboRepository.addComboItem(cbx5, "3", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.3"));
		CaribelComboRepository.addComboItem(cbx5, "4", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.4"));
		CaribelComboRepository.addComboItem(cbx5, "5", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.5"));
		CaribelComboRepository.addComboItem(cbx5, "6", Labels.getLabel("schedaPalliat.segnalazionePanel.combo.titoloStudio.6"));
		cbx5.setSelectedValue(" ");

		//combo grado consapevolezza paziente ultima settimana
		cbx11.clear();
		CaribelComboRepository.addComboItem(cbx11, "0", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.0"));
		CaribelComboRepository.addComboItem(cbx11, "1", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.1"));
		CaribelComboRepository.addComboItem(cbx11, "2", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.2"));
		CaribelComboRepository.addComboItem(cbx11, "3", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.3"));
		CaribelComboRepository.addComboItem(cbx11, "4", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.4"));
		CaribelComboRepository.addComboItem(cbx11, "5", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.5"));
		CaribelComboRepository.addComboItem(cbx11, "6", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezza.6"));

		//combo grado consapevolezza familiari
		cbx12.clear();
		CaribelComboRepository.addComboItem(cbx12, "0", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezzaF.0"));
		CaribelComboRepository.addComboItem(cbx12, "1", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezzaF.1"));
		CaribelComboRepository.addComboItem(cbx12, "2", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezzaF.2"));
		CaribelComboRepository.addComboItem(cbx12, "3", Labels.getLabel("schedaPalliat.familiariPanel.combo.gradoConsapevolezzaF.3"));

		//combo grado conoscenza italiano
		cbx13.clear();
		CaribelComboRepository.addComboItem(cbx13, "0", Labels.getLabel("schedaPalliat.badantePanel.combo.conoscenzaItaliano.0"));
		CaribelComboRepository.addComboItem(cbx13, "1", Labels.getLabel("schedaPalliat.badantePanel.combo.conoscenzaItaliano.1"));
		CaribelComboRepository.addComboItem(cbx13, "2", Labels.getLabel("schedaPalliat.badantePanel.combo.conoscenzaItaliano.2"));
		CaribelComboRepository.addComboItem(cbx13, "3", Labels.getLabel("schedaPalliat.badantePanel.combo.conoscenzaItaliano.3"));
		CaribelComboRepository.addComboItem(cbx13, "4", Labels.getLabel("schedaPalliat.badantePanel.combo.conoscenzaItaliano.4"));

		
		try {
			loadUtenza(cbx_utenza);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
			try {
				quadroSanitarioMMGCtrl.caricaTipoCura(cbx_motivo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	/* ----------------------------------------------------------------------------------------- */
	
	private void loadUtenza(CaribelCombobox cbx)throws Exception {
		logger.debug(" dentro loadUtenza ");
		cbx.clear();
		Hashtable h = new Hashtable();
		CaribelComboRepository.comboPreLoad("f_tipo_utente", new TiputeSEJB(), "query", h, cbx, null, "codice", "descrizione", false);
	}

	/* ----------------------------------------------------------------------------------------- */
	
	//Carlo Volpicelli
	//panel conclusioni: le due combo "luogo del decesso" e "familiari presenti" vengono abilitate
	//solo se la combo "motivo" è settata su "decesso"
	public void onChange$skm_motivo_chius(Event event)
	{
		String motivo = skm_motivo_chius.getValue();

		if(motivo.equals("Decesso"))
		{
			UtilForBinding.setComponentReadOnly(skm_deceduto, false);
			UtilForBinding.setComponentReadOnly(skm_presenti, false);
		}else
		{
			UtilForBinding.setComponentReadOnly(skm_deceduto, true);
			UtilForBinding.setComponentReadOnly(skm_presenti, true);
		}
	}

	/* ---------------------------------------------------------------------------------------- */


	//Carlo Volpicelli - all'impostazione della data apertura nel panel anamnesi, vengono automaticamente
	//settati i valori della data presa carico e data prima visita nel panel "situazione iniziale", 
	//con valori pari alla data di apertura del panel anamnesi
	public void onChange$skm_data_apertura()
	{
		Date d = skm_data_apertura.getValue();
		skm_presacarico_data.setValue(d);
		skm_pvisita_data.setValue(d);
	}

	/* ---------------------------------------------------------------------------------------- */


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
					map.put(CostantiSinssntW.SKFPG_DATA_APERTURA, skm_data_apertura.getValueForIsas());
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
					Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_esistente",new String[]{skm_data_apertura.getText(),desc_operat.getText()}), 
							Labels.getLabel("messagebox.attention"),
							Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
							new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())){
								id_skso.setValue(new Integer(id_skso_url));
								//									recuperaDatiSettaggio();
								recuperaDatiSettaggio(id_skso, skm_segnala, cbx_utenza, cbx_motivo, skm_descr_contatto,skm_data_apertura, skm_medico_da);
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
				Messagebox.show(Labels.getLabel("Contatti.presacarico.avviso_contatto_utilizzato",new String[]{skm_data_apertura.getText(),desc_operat.getText()}), 
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
				recuperaDatiSettaggio(id_skso, skm_segnala, cbx_utenza, cbx_motivo, skm_descr_contatto,skm_data_apertura, skm_medico_da);
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
		}
		if (isInInsert()&&!fromDoSaveForm && id_skso_url!=null)  {
			recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
		}
	}
	
	
	protected void scriviPresaCarico(Hashtable h, boolean saveForm) throws Exception  {
		String data_pc = skm_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(codMedicoReferente.getText())){
			codMedicoReferente.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skm_medico_da.setValue(Calendar.getInstance().getTime());
			data_pc = skm_medico_da.getValueForIsas();
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



	private boolean scriviPrimaVisita() throws Exception{
		String data_pc = skm_data_apertura.getValueForIsas();
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
			if(!skm_data_apertura.getText().isEmpty())
				ht.put("data_apertura",skm_data_apertura.getText());
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
		return skm_data_apertura.getText()!= null && !skm_data_apertura.getText().isEmpty();
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
		if (!esisteCasoAttivo) h.put("dt_presa_carico", skm_data_apertura.getValueForIsas());
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


	//Carlo Volpicelli, 22/12/2016. Recupera il numero di contatto più alto per numero di cartella
	private String recuperaNContatto(String nCartella) throws Exception 
	{
		String risultato;

		myLogin ml = CaribelSessionManager.getInstance().getMyLogin();
		risultato = myEJB.getMaxNContatto(ml, nCartella).toString();

		return risultato;
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
		h_ctrl.put("dataRif", (String)this.skm_data_apertura.getText());
		h_ctrl.put("soloAperti", (soloAperti?"S":"N"));
		return ((Boolean)invokeGenericSuEJB(myEJB, h_ctrl, "query_checkContSuccessivi")).booleanValue();
	}

	public void onStorico(ForwardEvent e) throws Exception
	{	
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO_CURE_PALLIATIVE);

		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerInfermieristicoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerInfermieristicoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerInfermieristicoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}

		map.put(StoricoPalliativistaReferenteGridCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
		map.put("zul_chiamante", (ContattoPalliatFormCtrl)this);//Utile per l'aggiornamento da finestra modale
		//TODO non permettere la riapertura se la finestra è gia aperta
		if (storico_operatore_ref == null) {
			storico_operatore_ref = Executions.getCurrent().createComponents(StoricoPalliativistaReferenteGridCtrl.myPathFormZul, self, map);
			storico_operatore_ref = null;
		} 

	}


	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		//map.put(CTS_DATA_APERTURA, skfpg_data_apertura.getValue());
		map.put(CTS_DATA_APERTURA, skm_data_apertura.getValue());
		if (skm_data_dimiss != null)
			map.put(CTS_DATA_CHIUSURA, skm_data_dimiss.getValue()); //si riferisce a skm_data_dimiss o skm_data_chiusura?
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
		h.put("data_apertura", skm_data_apertura.getValueForIsas());
		if (skm_data_chiusura != null)
			h.put("data_chiusura", skm_data_chiusura.getValueForIsas());
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
			Date dataAccettazioneSkSo = recuperaDataAccettazioneSkSo(n_cartella, id_skso, cbx_provenienza, cbx_utenza, cbx_motivo, skfpg_descr_contatto,skm_data_apertura, skfpg_referente_da);
			if (ManagerDate.validaData(dataAccettazioneSkSo)) {
				skm_data_apertura.setValue(dataAccettazioneSkSo);
				skfpg_referente_da.setValue(dataAccettazioneSkSo);
			} else {
				if (proporreDtScheda) {
					if (!ContattoInfFormCtrl.anagraficaChiusa(this.hParameters, skm_data_apertura, skfpg_referente_da)){
						skm_data_apertura.setValue(procdate.getDate());
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
		recuperaDatiSettaggio(id_skso, skm_segnala, cbx_utenza, cbx_motivo, skm_descr_contatto,skm_data_apertura, skm_medico_da);

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
				"skfpg_data_apertura", skm_data_apertura.getValueForIsas());
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
		// FIXME Recuperare l'ultimo contatto delle cure palliative
		//		int nMaxContatto = myEJB.recuperaMaxContatto(CaribelSessionManager.getInstance().getMyLogin(), dati);
		//		
		//		abilitaBottoneRiapri = ( (n_contatto.getValue()!=null && 
		//				(n_contatto.getValue().intValue() == nMaxContatto)));
		//		logger.trace(punto + " abilitareBottone>>" + abilitaBottoneRiapri +"<< nMaxContatto>"+nMaxContatto +"<");
		//		
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
			skm_data_apertura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
			skfpg_data_uscita.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
		}
		skm_data_apertura.setReadonly(isInUpdate());
	}

	@Override
	public boolean doValidateForm() throws Exception {

		//		boolean canSave = false;
		//		
		//		
		//		
		//		Hashtable<String, String> dati = new Hashtable<String, String>();
		//		//		myEJB.insert(CaribelSessionManager.getInstance().getMyLogin(), dati);
		//		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITAZIONE_RFC115_6)) {
		//			/* TODO IMPLEMENTEARE */
		//		}
		//		
		//		if (this.skfpg_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO)){
		//			skfpg_descr_contatto.setValue(skfpg_data_apertura.getText()+CostantiSinssntW.DA_SCHEDA_SO);
		//		}
		//		else skfpg_descr_contatto.setValue(skfpg_data_apertura.getText());
		//		
		//						
		//		switch(check_save_step){
		//		case 0: if(!controlloDatiSalvataggio()) return false;
		//		case 1: settaDati(); canSave = true; check_save_step = 0;break;
		//		}		
		//		return canSave;

		return true;
	}

	//	private void settaDati() throws Exception {
	//		if (this.currentIsasRecord == null && !isDataValida(skfpg_referente_da)) {
	//			skfpg_referente_da.setValue(skfpg_data_apertura.getValue());
	//		}
	//		skfpg_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
	//		skfpg_tipo_operatore.setText(this.myTipoOpInstance);
	//		desc_operat.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
	//	}


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
				Date dataApertura = skm_data_apertura.getValue();
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
			canSave = ManagerDate.controllaPeriodo(self, skm_data_apertura, skfpg_data_uscita, "lb_skfpg_data_apertura", "lb_skfpg_data_uscita");
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
		controlloInterv.put("skfpg_tipo_operatore", this.myTipoOpInstance);

		//		super.select("query_controlloData",profile.getParameter("skmed"),t);
		ISASRecord dbrInterv = myEJB.query_controlloData(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);

		datiRicevuti.put(CTS_TROVA_INVERV, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV));
		datiRicevuti.put(CTS_TROVA_INVERV_MAX, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV_MAX));
		logger.debug( " dati recuperati>>" + (datiRicevuti != null ? datiRicevuti + "" : " no dati") + "");

		return datiRicevuti;
	}

	public void onBlurDataApertura(Event event) throws Exception {
		if (skfpg_referente_da.getValue()==null || this.isInInsert()) skfpg_referente_da.setValue(skm_data_apertura.getValue());

	}


	@Override
	protected boolean doSaveForm() throws Exception 
	{	
		//10/05/2017
		state_before_in_insert = isInInsert();
		if (state_before_in_insert && id_skso_url!=null)
		{
			this.id_skso.setValue(new Integer(id_skso_url));
			//recuperaDatiSettaggio(id_skso, skm_segnala, cbx_utenza, cbx_motivo,skm_descr_contatto,  skm_data, skf_medico_da);
			recuperaDatiSettaggio(id_skso, skm_segnala, cbx_utenza, cbx_motivo, skm_descr_contatto,skm_data_apertura, skm_medico_da);
		}
	
		if(null!=skm_data_chiusura.getValue())
		{
			this.setReadOnly(true);
			btn_riapri.setDisabled(false);

			super.btn_delete.setDisabled(true);
			super.btn_save.setDisabled(true);
			super.btn_undo.setDisabled(true);
			btn_riapri.setDisabled(false);
		}else
		{
			btn_delete.setDisabled(false);
			btn_save.setDisabled(false);
			btn_undo.setDisabled(false);
			btn_riapri.setDisabled(true);
		}

		if(insert==1)
		{
			skm_medico_da.setValue(skm_data_apertura.getValue());
		}
		
		boolean b = false;
		if(super.doSaveForm())
		{
			b = true;
			insert = 0;
			caribelContainerCtrl.doRefreshOnSave(contattoPalliatForm);
		}
		
		//10/05/2017
		if(b)
		{
			gestioneListaAttivita(true);				
			doFreezeForm();
//				gestionePannelloScale();
			//gestionePianiAssistenziali();
		}				

		return b;		

	}
	

	//Carlo Volpicelli
	public void settaMedicoPalliatReferente(String codice, Date dataDa) throws Exception 
	{
		codMedicoReferente.setValue(codice);
		Events.sendEvent(Events.ON_CHANGE, codMedicoReferente, null);
		skm_medico_da.setValue(dataDa);
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
				if (skm_data_apertura != null)
					map.put("data_ap", skm_data_apertura.getValueForIsas());
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

	//	public void doStampa() {
	//		try{
	//			Hashtable<String, String> dati = new Hashtable<String, String>();
	//			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
	//			dati.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
	//			dati.put("data_ap", skfpg_data_apertura.getText());
	//			dati.put("data_chiu", skfpg_data_uscita.getText());
	//			dati.put("assistito", UtilForContainer.getCognomeNomeAssistito());
	//			dati.put("operatore", ManagerProfile.getCognomeNomeOperatore(getProfile()));
	//			Executions.getCurrent().createComponents(StampaContattoGenCtrl.CTS_FILE_ZUL, self, dati);
	//		}catch(Exception ex){
	//			ex.printStackTrace();
	//			doShowException(ex);
	//		}
	//	}
	@Override
	protected void doDeleteForm() throws Exception {
		Object obj; 

		try {
			obj = myEJB.deleteAll(CaribelSessionManager.getInstance().getMyLogin(), this.currentIsasRecord);
			//doInitForm();
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
			caribelContainerCtrl.doRefreshOnDelete(contattoPalliatForm);
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


	public void onCheck$consapevoli_tutti()
	{
		gestisciFamiliariConsapevoli();
	}

	public void onCheck$consapevoli_tutti_eccetto()
	{
		gestisciFamiliariConsapevoli();
	}

	private void gestisciFamiliariConsapevoli()
	{
		String s = quanti_consapevoli.getSelectedValue();
		if(s.equals("1"))
			familiari_consapevoli.setReadonly(false);
		else
		{
			familiari_consapevoli.setReadonly(true);
			familiari_consapevoli.setValue(null);
		}			
	}

	private void gestisciFamiliariConsapevoli_()  ///FIXARE
	{
		String s = quanti_consapevoli.getSelectedValue();
		if(s.equals("1"))
			familiari_consapevoli.setReadonly(false);
		else if(s.equals("0"))
			familiari_consapevoli.setReadonly(true);
	}

	/* ----------------------------------------------------------------------------------- */

	public void onCheck$skm_np_diag_altro()
	{
		if(skm_np_diag_altro.isChecked())
			skm_np_diag_altro_descr.setReadonly(false);
		else 
		{
			skm_np_diag_altro_descr.setReadonly(true);
			skm_np_diag_altro_descr.setValue(null);
		}

	}

	/* ----------------------------------------------------------------------------------- */

	public void onCheck$lesioniAltroSi()
	{
		if(lesioniAltroSi.isChecked())
			skm_lesioni_altro_descr.setReadonly(false);
		else
			skm_lesioni_altro_descr.setReadonly(true);
	}

	public void onCheck$lesioniAltroNo()
	{
		if(lesioniAltroNo.isChecked())
			skm_lesioni_altro_descr.setReadonly(true);
		else
			skm_lesioni_altro_descr.setReadonly(false);
	}

	/* ------------------------------------------------------------------------------------ */

	public void onClick$btn_riapri() throws Exception
	{		
		this.setReadOnly(false);

		btn_delete.setDisabled(false);
		btn_save.setDisabled(false);
		btn_undo.setDisabled(false);

		//gestione della disattivazione/attivazione del campo skm_lesioni_altro_descr nel panel situazioneIniziale/lesioni
		if(skm_lesioni_altro.getSelectedValue().equals("S"))
			skm_lesioni_altro_descr.setReadonly(false);
		else		
			skm_lesioni_altro_descr.setReadonly(true);

		//gestione della disattivazione/attivazione del campo skm_np_diag_altro_descrr nel panel situazioneIniziale/neoplasia
		if(skm_np_diag_altro.isChecked())
			skm_np_diag_altro_descr.setReadonly(false);
		else
			skm_np_diag_altro_descr.setReadonly(true);

		//situazioneFamiliare/familiari, gestione textbox in base al check tutti e tutti eccetto
		if(consapevoli_tutti_eccetto.isChecked())
			familiari_consapevoli.setReadonly(false);
		else
			familiari_consapevoli.setReadonly(true);
	}

	/* -------------------------------------------------------------------------------------------- */

	public void onClick$btn_scheda_so()
	{
		try {
			onSchedaSO();
		} catch (Exception e) 
		{			
			doShowException(e);
		}
	}
	
	/* -------------------------------------------------------------------------------------------- */
	
	public void onChange$skm_data_chiusura() throws Exception
	{
		Date dataChiusura = skm_data_chiusura.getValue();
		Date dataApertura = skm_data_apertura.getValue();
		boolean b = false; 	//viene valorizzata a true quando la data di chiusura è antecedente a quella di apertura,
						   	//allo scopo di non far eseguire il controllo dell'esistenza di accessi con data successiva
							//a quella di chiusura

		//se la data di chiusura è antecedente alla data di apertura, la chiusura viene impedita.
		if(dataChiusura.before(dataApertura) && dataChiusura!=null)
		{
			Messagebox.show(Labels.getLabel("contattoPalliat.valoreDataChiusuraNonValidaBefore"), 
					Labels.getLabel("contattoPalliat.attenzione = ATTENZIONE!"), 
					Messagebox.OK, 
					Messagebox.EXCLAMATION);

			skm_data_chiusura.setValue(null);
			
			b = true;
		}

		if(!b && dataChiusura!=null)
		{
			//se esistono accessi con data successiva alla data di chiusura, la chiusura viene impedita.
			IntervEJB intEJB = new IntervEJB();
			myLogin ml = CaribelSessionManager.getInstance().getMyLogin();
			Hashtable h = new Hashtable();
			h.put("int_tipo_prest", UtilForContainerGen.getTipoOperatorerContainer());
			h.put("n_cartella", n_cartella.getValue().toString());
			h.put("n_contatto", n_contatto.getValue().toString());
			Vector v = null;
			try {				
				v = intEJB.queryAccessi(ml, h);
				for(int i=0; i<v.size(); i++)
				{
					ISASRecord dbr = (ISASRecord) v.get(i);
					Date dataAccesso = null;
					try 
					{
						dataAccesso = (Date) dbr.get("int_data_prest");
					} catch (ISASMisuseException e) 
					{				
						doShowException(e);
					}

					if(dataChiusura.before(dataAccesso))
					{
						Messagebox.show(Labels.getLabel("contattoPalliat.dataChiusuraNonValidaAccessiEsistenti"), 
								Labels.getLabel("contattoPalliat.attenzione"), 
								Messagebox.OK, 
								Messagebox.EXCLAMATION);

						skm_data_chiusura.setValue(null);

						break;
					}				
				}	
				
			}catch(Exception e1)
			{
				doShowException(e1);
			}			
		}
		
	}

	/* -------------------------------------------------------------------------------------------- */
}




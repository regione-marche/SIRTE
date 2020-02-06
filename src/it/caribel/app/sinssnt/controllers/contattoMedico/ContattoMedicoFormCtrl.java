package it.caribel.app.sinssnt.controllers.contattoMedico;

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkMedEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.SCBisogniEJB;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.accessi_effettuati.AccessiEffettuatiGridCtrl;
import it.caribel.app.sinssnt.controllers.contatto.CaribelContattoFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.riepilogo_ausili_protesica.RiepilogoAusiliProtesicaGridCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalePanelCtr;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SegreteriaOrganizzativaFormCtrl;
import it.caribel.app.sinssnt.controllers.storico_referente.StoricoMedicoReferenteGridCtrl;
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

public class ContattoMedicoFormCtrl extends CaribelContattoFormCtrl {

	private static final long serialVersionUID = 1L;

	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/contatto_medico/contatto_medico.zul";
	/*
	 *  ho reso pubblica in quanto è la stessa
	 *  chiave che viene usata nel contatto delle terapie 
	 */
//	public static final String myKeyPermission = "SKMED";
	public static final String myKeyPermission = ChiaviISASSinssntWeb.CONTATTO_MEDICO;

	private static final int RISPOSTACHIUSURACONTATTO = 1;
	private SkMedEJB myEJB = new SkMedEJB();
	protected Window sntContattoMedico;

	boolean rispostaChiusuraContatto = false;
	boolean messaggioAvvisoCartellaChiusa = true;

	private CaribelCombobox cbxMotivoChius; // motivo chiusura
	private CaribelCombobox cbxBoxSegn; //Segnalante 
	private CaribelCombobox cbxMotivo; // motivo: cambiato in intensità assistenziale
	private CaribelCombobox cbxTipute; // tipoUtente  

	private Label label_defInizioPer;
	private Label label_defFinePer;
	
//	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;

	private CaribelTextbox cod_operatore;
	private CaribelTextbox desc_operat;
	private CaribelTextbox skm_descr_contatto;

	private Component protesica = null;
	private Component storico_operatore_ref = null;
	private Component accessi;
	private Component diagnosi;
	private Component idMenuTerapia;
	private Component contattoMedicoTerapia;
	
	private int check_save_step = 0;
	
	private CaribelCombobox skm_medico_descr;
	private CaribelTextbox skm_medico;
	
	private Tabpanel panello_terapia;
	
	private Tabpanels tabpanels;
	private Tabs tab_ana;
	private Tab scaleval_tab;
	private Tab pa_tab;
//	private Tabpanel tabpanel_scale;
	private Tabpanel tabpanel_pa;

	private ScalePanelCtr scalPnlCtr;// = new ScalePanelCtr(); 
	private Component panel_scale;
//	private Component cs_operatore_referente;
	private Component cs_presidio_;
	//	private Button btn_limitaA; 
	private Button btn_accessi_;
	private Button btn_protesica;
	private Button btn_riapri;
	private Button btn_storico;
	private Button btn_patologie;
	private Button btn_scheda_so;
	private CaribelDatebox skm_data_apertura;
	private CaribelDatebox skm_data_chiusura;
	private CaribelDatebox skm_medico_da;
//	private CaribelDatebox pr_data;

//	private CaribelTextbox skm_medico;
//	private CaribelCombobox skm_medico_descr;
	private CaribelTextbox skm_cod_presidio;
	private CaribelCombobox presidio_descr;

//	private Component sofc = null;
//	private Hashtable temp_container_hash = null;

	
	private Vector<Hashtable<String, String>> vettOper = new Vector<Hashtable<String, String>>();
	private String ver = "43-" + this.getClass().getName() + "\n ";

	protected CaribelListbox tablePrestazioni;
	
	//per gestione presa in carico da lista attività
	private ISASRecord skso_op_coinvolti;
	private RMSkSOOpCoinvoltiEJB op_CoinvoltiEJB;
	private boolean gestione_lista_attivita = true;
	
	String id_skso_url = null;
	String skso_fonte = null;
//	private CaribelIntbox id_skso;
	String nContattoURL = null;
	String nCartellaURL = null;
	
	private boolean state_before_in_insert;
	
	

	public static final String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";
	//	TODO VERIFICARE IL CORRETTO FUNZIONAMENTO: pr_data 
	public static final String CTS_DATA_CART = "dataCart";
	public static final String CTS_ZUL_CHIAMANTE = "zul_chiamante";
	public static final String CTS_DATA_RIF = "dataRif";

	public static final String CTS_DATA_APERTURA = "skm_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "skm_data_chiusura";
	

	@Override
	public boolean doValidateForm() throws Exception {
		String punto = ver + "doSaveForm ";
		boolean canSave = true;
		Hashtable<String, String> dati = new Hashtable<String, String>();
		//		myEJB.insert(CaribelSessionManager.getInstance().getMyLogin(), dati);
		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITAZIONE_RFC115_6)) {
			/* TODO IMPLEMENTEARE */
		}
		
		
		
		if (this.skm_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO)){
			skm_descr_contatto.setValue(skm_data_apertura.getText()+CostantiSinssntW.DA_SCHEDA_SO);
		}
		else skm_descr_contatto.setValue(skm_data_apertura.getText());
		
		
		switch(check_save_step){
		case 0: if(!controlloDatiSalvataggio()) return false;
		case 1: settaDati(); canSave = true; settaSaveStep(0);break;
		}		
		return canSave;
	}

	private void settaDati() throws SQLException {
		if (this.currentIsasRecord == null && !isDataValida(skm_medico_da)) {
			skm_medico_da.setValue(skm_data_apertura.getValue());
		}
		//Jessy 11/05 deve essere salvato sempre l'ultimo operatore che modifica qcs
		cod_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		desc_operat.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
		
		
		/*
		 * String prData = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.PR_DATA);
		if (ISASUtil.valida(prData)) {
			try {
				pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
			} catch (WrongValueException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		 */
		
		if (this.currentIsasRecord == null) {
			doInsertRecord();
		} else {
			doUpdateRecord();
		}
	}

	private void settaSaveStep(int step) throws Exception {
		check_save_step = step;
	}
	private boolean controlloDatiSalvataggio() throws Exception {
		boolean canSave = true;
		String punto = ver + "controlloDatiSalvataggio ";
		logger.debug(punto + " controllo dati salvataggio ");

		canSave = controlloDataAperturaInferioreContatto();
		if (!canSave) {
			return canSave;    
		}
		Date dataApertura = skm_data_apertura.getValue();
		Date dataChiusura = skm_data_chiusura.getValue();
		boolean isChiusura = dataChiusura != null;
		boolean isMotivoChiusuraInserito = isMotivoChiusuraInserito();

		if (isChiusura) {
			canSave = ManagerDate.controllaPeriodo(self, skm_data_apertura, skm_data_chiusura, "lb_skm_data_apertura", "lb_skm_data_chiusura");
//			if (dataApertura.after(dataChiusura)) {
//				Label lblSmesData = (Label) self.getFellow("lb_skm_data_apertura");
//				Label lblSkmChiusura = (Label) self.getFellow("lb_skm_data_chiusura");
//				skm_data_chiusura.setErrorMessage(Labels.getLabel("common.msg.NoOrderDate.1maggioreDi0",
//						new String[] { lblSmesData.getValue(), lblSkmChiusura.getValue() }));
//				canSave = false;
//				return canSave;
//			} else {
//				logger.debug(punto + "da ok ");
//			}
			
			if (canSave){
				logger.debug(punto + "da ok ");
			}else {
				logger.debug(punto + "periodo non valido>>");
				return canSave;
			}
			
			if (!isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg"));
				cbxMotivoChius.focus();
				skm_data_chiusura.setValue(null);				
				canSave = false;
				return canSave;
			}
			if (isDataValida(skm_data_chiusura)) {
				//				int i = UtilForUI.standardYesOrNo(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.contatto",
				//							new String[]{""}), null );
				//				canSave =(i == 1);//no
				canSave = messaggioConfermaChiusura();
//				return messaggioConfermaChiusura();
			}
			if (!canSave) {
				return canSave;
			}
		} else {
			logger.debug(punto + " contatto non chiuso ");
			if (isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.no.data"));
				cbxMotivoChius.focus();
				canSave = false;
				return canSave;
			}
		}
		logger.debug(punto + " fine ");
		return canSave;
	}

	private boolean controlloDataAperturaInferioreContatto() {
		String punto = ver + "controlloDataAperturaInferioreContatto ";
		boolean controlloOk = true;
		String prData = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.PR_DATA);
		logger.trace(punto + " dati letti>>" + super.caribelContainerCtrl.hashChiaveValore + "<<<");

		if (ISASUtil.valida(prData) && isDataValida(skm_data_apertura)) {
			try {
//				Date skVal = new Date();
//				SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
//				try {
//					skVal = format1.parse(prData);
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				logger.trace(punto + " data>>" + prData + "<<");
//				Date dataApertura = skm_data_apertura.getValue();
//				if (skVal.after(dataApertura)) {
//					UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.aperto"));
//					controlloOk = false;
//				}
				
				/*if (pr_data.getValue()!=null) {
					try {
						logger.trace(" data>>" + pr_data.getValue().toString() + "<<");
						Date dataApertura = skm_data_apertura.getValue();
						if (DateUtils.truncate(skm_data_apertura.getValue(), Calendar.DATE).after(DateUtils.truncate(dataApertura, Calendar.DATE))) {
							UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.aperto"));
							controlloOk = false;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				*/
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return controlloOk;
	}
	
	
	private boolean messaggioConfermaChiusura() throws Exception {
		rispostaChiusuraContatto = false;
//		if (id_skso_hash !=null || id_skso != null){
//			settaSaveStep(RISPOSTACHIUSURACONTATTO);
//			doSaveForm();
//			return false;
//		}
		Messagebox.show(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.contatto", new String[] {}),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							settaSaveStep(RISPOSTACHIUSURACONTATTO);
							boolean ret = doSaveForm();
							if (ret)doMakeControlAfterRead();
						}
					}
				});
		return rispostaChiusuraContatto;
	}

	private void doMakeControlAfterRead() throws WrongValueException, Exception {
		
		/*
		if (getProfile().getStringFromProfile("ctrl_skinf_siad").equals(CostantiSinssntW.SI) && 
				isInUpdate() && cbx_motivo.isVisible())
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
		
		 */
		// gestisco l'abilitazione/disabilitazione dei vari componenti, a seconda dello stato della maschera
		abilitazioneMaschera();
		if (panel_scale != null){ 
			panel_scale.detach();
		}
	}
 

	private boolean isDataValida(CaribelDatebox data) {
		String punto = ver + "isDataValida ";
		boolean dataValida = false;
		Date dt = data.getValue();
		dataValida = (dt != null);
		logger.trace(punto + " data valida>>" + dt + "<< dataValida>" + dataValida + "<<");
		return dataValida;
	}   

	private boolean isMotivoChiusuraInserito() {
		boolean motivoChiusuraInserito = false;
		if (cbxMotivoChius.getSelectedValue()!=null && !cbxMotivoChius.getSelectedValue().equals("") 
				&& (cbxMotivoChius.getSelectedItem()!=null)
				&& !cbxMotivoChius.getSelectedItem().getLabel().equals(CostantiSinssntW.VALORE_COMBO_DEFAULT)) {
			cbxMotivoChius.focus();
			motivoChiusuraInserito = true;
		}
		logger.trace(ver + "isMotivoChiusuraInserito "+motivoChiusuraInserito );
		return motivoChiusuraInserito;
	}

	private void doUpdateRecord() throws SQLException {
		String punto = ver + "doInsertRecord ";
		logger.trace(punto + " sono in insert ");

		String data_corrente = skm_data_apertura.getText();
		//ilaria controllo anche la data chiusura
		String data_corrente_chiu = skm_data_chiusura.getText();
		String dataContatto = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skm_data_apertura");
		String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skm_data_chiusura");
		if (!data_corrente.equals(dataContatto) || !data_corrente_chiu.equals(dataCorrenteChiu)) {
			//entra qui quando è cambiata la data apertura contatto o la data chiusura contatto
			Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
			Hashtable<String, String> datiRicevuti = selectControlloData();
			//System.out.println("Controllodata=>"+trovaInterv.getUnmaskedText());
			String trovaInterv = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV);
			String trovaIntervMax = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV_MAX);
			if (!(trovaInterv.equals("N"))) {//ci sono degli interventi controllo le date
				String data = trovaInterv;
				if (ManagerDate.validaData(data)){
					String datastr = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
					java.sql.Date dtscad = java.sql.Date.valueOf(data);
					data = data_corrente;
					data = data.substring(6, 10) + "-" + data.substring(3, 5) + "-" + data.substring(0, 2);
					java.sql.Date corrente = java.sql.Date.valueOf(data);
					if (corrente.after(dtscad)) {
						new it.pisa.caribel.swing2.cariInfoDialog(null, "Impossibile effettuare il salvataggio.\nSono presenti interventi con "
								+ "data minore della data apertura inserita.\n" + "Massima data valida " + datastr, "Attenzione!").show();
						//					return count=-1;
					}
				}
				//controllo la data chiusura
				if (!data_corrente_chiu.equals("__/__/____")) {
					String data_max = trovaIntervMax;
					String datastr_chiu = data_max.substring(8, 10) + "/" + data_max.substring(5, 7) + "/" + data_max.substring(0, 4);
					java.sql.Date dtscad_chiu = java.sql.Date.valueOf(data_max);
					if (ManagerDate.validaData(data_corrente_chiu)){
						data_max = data_corrente_chiu;
						data_max = data_max.substring(6, 10) + "-" + data_max.substring(3, 5) + "-" + data_max.substring(0, 2);
						java.sql.Date corrente_chiu = java.sql.Date.valueOf(data_max);
						if (dtscad_chiu.after(corrente_chiu)) {
							new it.pisa.caribel.swing2.cariInfoDialog(null,
									"Impossibile effettuare il salvataggio.\nSono presenti interventi con"
											+ " data maggiore della data chiusura inserita.\n" + "Minima data valida " + datastr_chiu,
									"Attenzione!").show();
							//						return count=-1;
						}
					}
				}
				//se c'è qualcosa che non va, qui non arriva altrimenti deve fare update
				//				count=db.Update(t);
				//				setTipoUVG();

			} else {
				//				count=db.Update(t);
				//               setTipoUVG();
			}
		}
	}

	private Hashtable<String, String> selectControlloData() throws SQLException {
		String punto = ver + "selectControlloData ";
		Hashtable<String, String> datiRicevuti = new Hashtable<String, String>();
		Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
		controlloInterv.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		controlloInterv.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
		//		super.select("query_controlloData",profile.getParameter("skmed"),t);
		ISASRecord dbrInterv = myEJB.query_controlloData(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);

		datiRicevuti.put(CTS_TROVA_INVERV, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV));
		datiRicevuti.put(CTS_TROVA_INVERV_MAX, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV_MAX));
		logger.debug(punto + " dati recuperati>>" + (datiRicevuti != null ? datiRicevuti + "" : " no dati") + "");

		return datiRicevuti;
	}

	private void doInsertRecord() {
		String punto = ver + "doUpdateRecord ";
		logger.trace(punto + " sono in update ");
	}

	public void doInitForm() {
		String punto = this.getClass().getName() + ".doInitForm \n";
		logger.debug(punto + "inizio ");
		try {
			super.doInitForm();
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			doMakeControl();
			doPopulateCombobox();
			btn_protesica.setVisible(false);

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
					super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, nContatto);
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
			
			String dtCartellaChiusa =  ISASUtil.getValoreStringa(hParameters, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
			//Eseguo la lettura da DB o mi preparo per l'insert
			if(ISASUtil.valida(nCartella) && 
					(ISASUtil.valida(nContatto) || ManagerDate.validaData(dtCartellaChiusa)) ) {
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(nContatto)){
				//sono in inserimento					
				this.n_contatto.setValue(new Integer(0));
				cod_operatore.setValue(getProfile().getIsasUser().getKUser());
				desc_operat.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
				caricaPatologie();
			}
			
			abilitazioneMaschera();
			
			doFreezeForm();
//			gestionePannelloScale();
			gestioneListaAttivita(false);
			gestionePianiAssistenziali();
			gestioneRichiestaChiusura(false);
		}  catch (Exception ex){
			
			if (ex instanceof InvocationTargetException && ((InvocationTargetException) ex).getTargetException().getMessage().equals(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI)){
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
					map.put(CostantiSinssntW.CTS_SKM_DATA_APERTURA, skm_data_apertura.getValueForIsas());
					if(skm_data_chiusura.getValue()!=null){
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
			h.put(CostantiSinssntW.TIPO_OPERATORE,CostantiSinssntW.TIPO_OPERATORE_MEDICO);			
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
									recuperaDatiSettaggio(id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
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
				recuperaDatiSettaggio(id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
				id_skso_url=null;
				skso_fonte=null;
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
				Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				return;
			}
		}else {
			if(fromDoSaveForm){
				scriviPrimaVisita();
			}
		}
		
		if (isInInsert()&&!fromDoSaveForm && id_skso_url!=null)  {
			recuperaDatiSettaggio(id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
		}

	}


	private boolean scriviPrimaVisita() throws Exception{
		
		String data_pc = skm_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(skm_medico.getText())){
			skm_medico.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skm_medico_da.setValue(Calendar.getInstance().getTime());
			data_pc = skm_medico_da.getValueForIsas();
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
		
		this.skm_data_chiusura.setValue(data_chiusura);
		
		this.cbxMotivoChius.setSelectedValue("9");
		h.put("n_progetto",n_contatto.getText());
		h.put("pa_tipo_oper",CostantiSinssntW.TIPO_OPERATORE_MEDICO);
		gestione_lista_attivita=false;
		doSaveForm();
		Integer piano_assist=(Integer)invokeGenericSuEJB(new PianoAssistEJB(), h, "chiudi_piani");
		this.currentIsasRecord=null;
		this.caribelContainerCtrl.hashChiaveValore.remove(CostantiSinssntW.N_CONTATTO);
		this.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO,h.get(CostantiSinssntW.CTS_ID_SKSO));		
		((ContainerMedicoCtrl)this.caribelContainerCtrl).btn_sntContattoMedico();
	}



	protected void scriviPresaCarico(Hashtable h, boolean saveForm) throws Exception  {
		String punto = ver  + "scriviPresaCarico ";
		String data_pc = skm_data_apertura.getValueForIsas();
		if (!getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE).equals(skm_medico.getText())){
			skm_medico.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skm_medico_da.setValue(Calendar.getInstance().getTime());
			data_pc = skm_medico_da.getValueForIsas();
		}
		skso_op_coinvolti.put("dt_presa_carico", data_pc);
		skso_op_coinvolti.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, CostantiSinssntW.CTS_FLAG_STATO_FATTA);
		skso_op_coinvolti.put(CostantiSinssntW.COD_OPERATORE_PC, getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		try{
		updateSuEJB(op_CoinvoltiEJB, skso_op_coinvolti);
		invokeGenericSuEJB(new SCBisogniEJB(), h, "updateIdSkso");
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



//	protected void scriviPresaCarico(Hashtable h) throws Exception  {
//		String punto = ver + "pr_motivo ";
//		skso_op_coinvolti.put("dt_presa_carico", skm_data_apertura.getValueForIsas());
//		if (skso_op_coinvolti.get("cod_operatore")==null){
//		skso_op_coinvolti.put("cod_operatore", cod_operatore.getText());
//		}
//		try{
//		updateSuEJB(op_CoinvoltiEJB, skso_op_coinvolti);
//		
//		Hashtable datiDaInviare = new Hashtable();
//		datiDaInviare.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
//		datiDaInviare.put(CostantiSinssntW.CTS_ID_SKSO, (id_skso_url));
//		if (ISASUtil.valida(id_skso_url)){
//			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
//			ISASRecord dbrValutazione = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(), datiDaInviare);
//			String codRichiedente = ISASUtil.getValoreStringa(dbrValutazione, "richiedente");
//			String tipoUte = ISASUtil.getValoreStringa(dbrValutazione, "tipo_ute");
//			String prMotivo = ISASUtil.getValoreStringa(dbrValutazione, "pr_motivo");
//			logger.debug(punto + " dati codRichiedente>"+codRichiedente+"<codRichiedente>"+codRichiedente+"< prMotivo>"+prMotivo);
//			cbxBoxSegn.setSelectedValue(codRichiedente);
//			cbxTipute.setSelectedValue(tipoUte);
//			cbxMotivo.setSelectedValue(prMotivo);
//		}
//		}catch (Exception e){
//			String id_skso = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_SKSO);
//			ISASRecord skso_op_coinvolti_new = invokeSuEJB(op_CoinvoltiEJB, h , "queryKey");
//			Messagebox.show(
//					Labels.getLabel("Contatti.SchedaSO.presa_carico_effettuata",new String[]{
//							UtilForBinding.getStringClientFromDate((Date) skso_op_coinvolti_new.get("dt_presa_carico")),
//							skso_op_coinvolti_new.get("cod_operatore").toString()}),
//					Labels.getLabel("messagebox.attention"),
//					Messagebox.OK,
//					Messagebox.EXCLAMATION);
//			return;
//		}
//		Messagebox.show(
//				Labels.getLabel("Contatti.SchedaSO.presa_carico_successo"),
//				Labels.getLabel("messagebox.attention"),
//				Messagebox.OK,
//				Messagebox.EXCLAMATION);
//		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
//		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_SKSO);
//		
//		return;
//	}
//
//	

//	private String recuperaNContatto(String nCartella, String prData) throws ISASPermissionDeniedException, SQLException, CariException {
	private String recuperaNContatto(String nCartella) throws ISASPermissionDeniedException, SQLException, CariException {
		String nContatto = "";
		String punto = ver + "recuperaNContatto ";
		logger.debug(punto + " recuperare nContatto>>" + nCartella);
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
//		dati.put(CostantiSinssntW.PR_DATA, prData);

		ISASRecord dbrSkMed = myEJB.getContattoMedCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
		nContatto = ISASUtil.getValoreStringa(dbrSkMed, CostantiSinssntW.N_CONTATTO);
		logger.debug(punto + " contatto recuperato>>" + nContatto);

		return nContatto;
	}

	public void doStampa() {
		try{
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
			dati.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
			dati.put("data_ap", skm_data_apertura.getText());
			dati.put("data_chiu", skm_data_chiusura.getText());
			dati.put("assistito", UtilForContainer.getCognomeNomeAssistito());
			dati.put("operatore", ManagerProfile.getCognomeNomeOperatore(getProfile()));
			Executions.getCurrent().createComponents(StampaContattoMedicoCtrl.CTS_FILE_ZUL, self, dati);
		}catch(Exception ex){
			doShowException(ex);
		}
	}

	private void doMakeControl() {
		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITA_SCALA)) {
			//		TODO METTERE LA PRESA CARICO E PANNELLO SEGNALAZIONE SOLO PER LA TOSCANA.
		}
		
		
		if (ManagerProfile.isAbilitazione(getProfile(),
				ManagerProfile.ABILITA_SCALA)) {
			// TODO METTERE AMBULATORIO , PRESA CARICO E
			// PANNELLO SEGNALAZIONE SOLO PER LA TOSCANA.
			
		}else {
			//nascondo pannello scale
			tabpanels.removeChild(panel_scale);
			tabpanels.removeChild(scaleval_tab);
		}
		
		btn_scheda_so.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			public void onEvent(Event event) throws Exception{
				onSchedaSO();
			}
		});		

		
	}

	private void abilitazioneMaschera() throws WrongValueException, Exception {
		String punto = ver + "abilitazioneMaschera ";
		logger.trace(punto);
		boolean insert = false;
		
		verificaStatoCartella();
		
		if (super.currentIsasRecord != null) {
			insert = false;
		} else {
			SegreteriaOrganizzativaFormCtrl.settaTipoUtenza(cbxTipute, null);
			boolean proporreDtScheda = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_PROPORRE_DT_SCHEDA);
			Date dataAccettazioneSkSo = recuperaDataAccettazioneSkSo(n_cartella, id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
			if (ManagerDate.validaData(dataAccettazioneSkSo)) {
				skm_data_apertura.setValue(dataAccettazioneSkSo);
			} else {
				if (proporreDtScheda) {
					if (!ContattoInfFormCtrl.anagraficaChiusa(this.hParameters, skm_data_apertura, null)){
						skm_data_apertura.setValue(procdate.getDate());
					}
				}
			}
			skm_medico_da.setValue(procdate.getDate());
			String nCartella = (String) arg.get("n_cartella");
			if (n_cartella.getValue()==null)
			n_cartella.setText(nCartella);
			n_contatto.setText("0");
			insert = true;
			if(ManagerProfile.getTipoOperatore(getProfile()).equals(UtilForContainer.getTipoOperatorerContainer())){
				skm_medico.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
				skm_medico_descr.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
			}
			String codPresidio = getProfile().getStringFromProfile(ManagerProfile.PRES_OPERATORE);
			skm_cod_presidio.setValue(codPresidio);
			presidio_descr.setValue(ManagerDecod.decodPresidi(getProfile(), codPresidio));
			String skm_data_chiusura = ISASUtil.getValoreStringa(this.currentIsasRecord, "skm_data_chiusura");
//			if (ManagerDate.validaData(skm_data_chiusura)){
//				statoConsultazione  = true;
//			}
			logger.trace(ver + " aggiorno>>" + "<< " + "\ndesc>>" + "<");
			//			cbxMotivoChius.setSelectedValue("0");
		}
		
		panello_terapia.getLinkedTab().setDisabled(insert);
		tabpanel_scale.getLinkedTab().setDisabled(insert);
		logger.trace(ver + " sono in inserimento:" + insert+" statoConsultazione ");
//		btn_accessi.setDisabled(insert);
		btn_protesica.setDisabled(insert);
		btn_riapri.setDisabled(true); // sia in inser/update deve essere sempre disabilitato
		btn_storico.setDisabled(insert);
		btn_patologie.setDisabled(insert);
		skm_data_chiusura.setDisabled(insert);
		cbxMotivoChius.setDisabled(insert);
		label_defInizioPer.setVisible(!insert);
		label_defFinePer.setVisible(!insert);
		
//		boolean contattoDisabilitato_ = false;
		
		if (isContattoInfChiuso()){
			logger.trace(ver + " sono in  statoConsultazione ");
			this.setReadOnly(true);
//			btn_accessi.setDisabled(false);
			btn_protesica.setDisabled(false);
			btn_storico.setDisabled(true);
//			contattoDisabilitato= true;
			
			boolean abilRiap = (getProfile().getIsasUser().canIUse(ManagerProfile.REOPEN_SCHEDA_MED,"MODI"));
			this.btn_riapri.setVisible(abilRiap);
			this.btn_riapri.setDisabled(false);	
			
		}else {
//			try {
//				contattoDisabilitato = id_skso.getValue()>0;
//			} catch (Exception e) {
//			}
			logger.trace(ver + "NON sono in Consultazione ");
		}
		
		if (isInUpdate()){
			
//			btn_accessi.setDisabled(false);
			btn_storico.setDisabled(false);
			((CaribelSearchCtrl) cs_operatore_referente.getAttribute(MY_CTRL_KEY)).setReadonly(true);
//			((CaribelSearchCtrl) cs_presidio.getAttribute(MY_CTRL_KEY)).setReadonly(true);
			
			if (this.skm_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
			{
//				cbxMotivo.setDisabled(true);				
//				cbxMotivo.setRequired(false);				
				cbxTipute.setDisabled(true);
				cbxBoxSegn.setDisabled(true);
				cbxTipute.setRequired(false);
				cbxBoxSegn.setRequired(false);
			}
		}
		recuperaDatiSettaggio(id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
//		cbxMotivo.setDisabled(contattoDisabilitato);
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
			skm_data_apertura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
			skm_data_chiusura.setConstraint("before "+ UtilForComponents.formatDateforDatebox(dtChiusura));
		}
		skm_data_apertura.setReadonly(isInUpdate());
	}

	public boolean isContattoInfChiuso(){
		return ManagerDate.validaData(skm_data_chiusura);
	}

	public void onAccessi(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onAccessi ";
		logger.debug(punto + "inizio ");
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("n_cartella", n_cartella.getValue().toString());
		map.put("n_contatto", n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);

		accessi = Executions.getCurrent().createComponents(AccessiEffettuatiGridCtrl.myPathFormZul, self, map);
	}

	public void onProtesica(ForwardEvent e) throws Exception {
		String punto = ver + ".onProtesica ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);
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
		String punto = ver + "doRiapriContatto() ";
		
		Object isSkValChiusaObj = UtilForContainer.getObjectFromMyContainer(ContainerMedicoCtrl.CTS_IS_SK_VAL_CHIUSA); 
		if(isSkValChiusaObj !=null){
			// ctrl skValutazione aperta
			boolean isSkValChiusa = ISASUtil.getvaloreBoolean(isSkValChiusaObj);
			if(isSkValChiusa){
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.msg.errore.riapertura"));
				return ;
			}
		}	
//		ctrl non esistenza di altri contatti successivi
		Hashtable hCtrl = new Hashtable();
		hCtrl.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.N_CARTELLA));
		hCtrl.put(CTS_DATA_RIF, (skm_data_apertura !=null ? skm_data_apertura.getValueForIsas(): ""));
		logger.trace(punto + " dati che invio >>" + hCtrl + "<<");
		
		Object contattiSuccessivi =myEJB.query_checkContSuccessivi(CaribelSessionManager.getInstance().getMyLogin(), hCtrl); 
		if (ISASUtil.getvaloreBoolean(contattiSuccessivi)) {
			UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.msg.errore.no.riapertura"));
			return;
		}
			
		skm_data_chiusura.setValue(null);
		cbxMotivoChius.setSelectedIndex(-1);
		//super.doSaveForm();
		
		// aggiorno il record
        boolean risu = doSaveForm();
//        TODO BOFFA SETTARE I CASI DELL'ESITO DEL SALVATAGGIO
//    	if (risu > 0){
//			if (this.myContainer != null) {
//				// chiudo e riapro il contatto per resettare tutti i livelli
//				this.myContainer.invocaMetodo("concludiCont");
//			}
//		} else if (risu != -2){
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"Si è verificato un errore in fase di aggiornamento/inserimento","Attenzione!").show();
//			setStato(this.CONSULTA);
//		}
        
        if (risu){
        this.setReadOnly(false);
        cod_operatore.setDisabled(true);
        desc_operat.setDisabled(true);
        
        doMakeControlAfterRead();
//        if (panel_scale != null){ 
//			panel_scale.detach();
//		}
		doFreezeForm();
//		gestionePannelloScale();
		gestionePianiAssistenziali();
        }
	}

	public void onStorico(ForwardEvent e) throws Exception {
		String punto = ver + ".onStorico ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
//		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);
		map.put(CTS_TIPO_OPERATORE, GestTpOp.CTS_COD_MEDICO);

		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerMedicoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerMedicoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}

		map.put(StoricoMedicoReferenteGridCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
		map.put(CTS_ZUL_CHIAMANTE, (ContattoMedicoFormCtrl) this);//Utile per l'aggiornamento da finestra modale
		//TODO non permettere la riapertura se la finestra è gia aperta
		if (storico_operatore_ref == null) {
			storico_operatore_ref = Executions.getCurrent().createComponents(StoricoMedicoReferenteGridCtrl.myPathFormZul, self, map);
			logger.trace(punto + " Chiudo storico Medico Referente già aperto ");
			storico_operatore_ref = null;
		} else {
			logger.trace(punto + " storico Medico Referente già aperto ");
		}
	}

	public void settaMedicoReferente(String codice, String descrizione, Date dataDa) throws Exception {
		String punto = ver + ".onPatologie ";
		logger.debug(punto + "inizio codice>>" + codice + "<< descrizione>>" + descrizione + "<< data>>" + dataDa);
		skm_medico.setValue(codice);
		skm_medico_descr.setValue(descrizione);
		skm_medico_da.setValue(dataDa);
	}


	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put(CTS_DATA_APERTURA, skm_data_apertura.getValue());
		if(skm_data_chiusura!=null)
			map.put(CTS_DATA_CHIUSURA, skm_data_chiusura.getValue());
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
//					if (panel_scale != null){ 
//						panel_scale.detach();
//					}
					doFreezeForm();
//					gestionePannelloScale();
					gestionePianiAssistenziali();
				}
			});
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	private void caricaPatologie() throws Exception {
		String punto = ver + "caricaPatologie ";
		logger.debug(punto + "inizio ");
		CaribelListModel lm = new CaribelListModel();
		Hashtable h = new Hashtable();
		if (ManagerDate.validaData(skm_data_apertura)){
			h.put("data_apertura", skm_data_apertura.getValueForIsas());
		}
		if (skm_data_chiusura != null)
			h.put("data_chiusura", skm_data_chiusura.getValueForIsas());
		h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
//		lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryLastDiagContesto"));
		lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryStoricoDiag"));
		tablePrestazioni.setModel(lm);
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
				if (skm_data_apertura != null){
					map.put("pr_data", skm_data_apertura.getValueForIsas());
					CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
					if (caribelContainerCtrl != null) {
						(caribelContainerCtrl.hashChiaveValore).put(CostantiSinssntW.PR_DATA, skm_data_apertura.getValueForIsas());
					}					
				}
				map.put("chiamante", String.valueOf(CostantiSinssntW.CASO_SAN));
				// hPar.put("stato", ""+this.stato);

				if (skm_data_apertura != null){
					map.put("data_ap", skm_data_apertura.getValueForIsas());
				}
				map.put("tipo_op", GestTpOp.CTS_COD_MEDICO);
				
//				if (ski_data_apertura != null)
//					map.put("data_ap", ski_data_apertura.getValueForIsas());
//				else if (pr_data != null) // dt ape skVal
//					map.put("data_ap", pr_data.getValueForIsas());
//				// dt chius skInf
//				if (ski_data_uscita != null)
//					map.put("data_ch", ski_data_uscita.getValueForIsas());

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
	
	
	private void loadTipologiaUtente(CaribelCombobox cbx) throws Exception {
		cbx.clear();
		Hashtable h = new Hashtable();
		// comboPreLoad.load("TIPUTES","query",profile.getParameter("tiputes"),cTipo,jCariComboBoxTipute, "codice", "descrizione");
		CaribelComboRepository.comboPreLoad("f_tipo_utente", new TiputeSEJB(), "query", h, cbx, null, "codice", "descrizione", false);
	}

	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + "");
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
		h_xAllCB.put("combo_tipostru_V", "S"); // x notificare che serve 1 riga vuota
		h_xAllCB.put("combo_tipostru_C", "S"); // x notificare che serve anche il codReg
		if (ManagerProfile.isConfigurazioneAbruzzo(getProfile()) || ManagerProfile.isConfigurazioneMolise(getProfile())
				|| ManagerProfile.isConfigurazioneMarche(getProfile())) {
			//			combo=new String[]{"MCHIUS","SAOADI","FMRICH", "MOTIVO"};
			h_xCBdaTabBase.put("MCHIUS", cbxMotivoChius);
			//			h_xCBdaTabBase.put("SAOADI",cbo_FMRICH);  // tipo cura non presente per questa versione 
			h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SEGNALANTE, cbxBoxSegn);
//			h_xCBdaTabBase.put("MOTIVO", cbxMotivo);
			
		} else {
			if (ManagerProfile.isConfigurazioneLazio(getProfile())) {
				//				combo=new String[]{"MCHIUS","SAOADI","FMRICH"};
				h_xCBdaTabBase.put("MCHIUS", cbxMotivoChius);
				//				h_xCBdaTabBase.put("SAOADI",combo_FMRICH);TODO INDIVIDUARE:jCariComboBoxTipocura  hCombo.put("SAOADI",jCariComboBoxTipocura);
				h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SEGNALANTE, cbxBoxSegn);
			} else {
				//				combo=new String[]{"MCHIUS","SAOADI"};
				h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SEGNALANTE, cbxBoxSegn);
				//				h_xCBdaTabBase.put("SAOADI",combo_FMRICH);
			}
		}
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(cbxMotivo);
		
		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
				"tab_descrizione", false);
		loadTipologiaUtente(cbxTipute);
		String linea = CaribelComboRepository.stampaCompbo(cbxMotivoChius);
		logger.trace(punto + " motivo chiusura >>" +linea);
	}
	public void onBlurDataApertura(Event event) throws Exception {
		if (skm_medico_da.getValue()==null || isInInsert()) skm_medico_da.setValue(skm_data_apertura.getValue());
		
	}

	@Override
	protected boolean doSaveForm() throws Exception {
		String punto = ver + "doSaveForm ";
		state_before_in_insert = isInInsert();
		if (state_before_in_insert && id_skso_url != null) {
			this.id_skso.setValue(new Integer(id_skso_url));
			recuperaDatiSettaggio(id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto, skm_data_apertura, skm_medico_da);
		}
		boolean ret = false;
		try {
			
			if (!ManagerDate.controllaPeriodo(self, skm_data_apertura, skm_data_chiusura, "lb_skm_data_apertura","lb_skm_data_chiusura")){
				logger.trace(punto + " data Non corretta ");
				return false;
			}
			
			if (isInUpdate()){
				String dataContatto =ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skm_data_apertura");
				String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skm_data_chiusura");

				ContattoInfFormCtrl cont = new ContattoInfFormCtrl();
				if (cont.okControlliChiusura(skm_data_apertura, skm_data_chiusura,dataContatto, dataCorrenteChiu,
						n_cartella.getText(), n_contatto.getText())){
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
//	private void onCloseSchedaSO(){
//		super.caribelContainerCtrl.hashChiaveValore.clear();
//		super.caribelContainerCtrl.hashChiaveValore.putAll(temp_container_hash);
//		
//	}
	
//	private void recuperaDatiSettaggio(CaribelIntbox id_skso, CaribelCombobox cbxBoxSegn,
//			CaribelCombobox cbxTipute, CaribelCombobox cbxMotivo,
//			CaribelTextbox skm_descr_contatto) throws CariException {
////	private void recuperaDatiSettaggio(id_skso,cbxBoxSegn, cbxTipute,cbxMotivo, skm_descr_contatto) throws CariException {
//		String punto = ver + "recuperaDatiSettaggio ";
//		Hashtable<String,String> datiDaInviare = new Hashtable<String,String>();
//		String idSkSo = (id_skso.getValue() !=null ? id_skso.getValue()+"":"");
//		datiDaInviare.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
//		datiDaInviare.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
//		logger.debug(punto + " dati che invio>>" + datiDaInviare);
//		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
//		ISASRecord dbrValutazione = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(),
//				datiDaInviare);
//		idSkSo = ISASUtil.getValoreStringa(dbrValutazione, CostantiSinssntW.CTS_ID_SKSO);
//		if (ISASUtil.valida(idSkSo)){
//			
//			String codRichiedente = ISASUtil.getValoreStringa(dbrValutazione, "richiedente");
//			String tipoUte = ISASUtil.getValoreStringa(dbrValutazione, "tipo_ute");
//			String tipocure = ISASUtil.getValoreStringa(dbrValutazione, "tipocura");
//			logger.debug(punto + " dati codRichiedente>" + codRichiedente + "<codRichiedente>" + codRichiedente
//					+ "< tipocure>" + tipocure);
//			cbxBoxSegn.setSelectedValue(codRichiedente.equals("") ? "9" : codRichiedente);
//			cbxTipute.setSelectedValue(tipoUte);
//			// cbxMotivo.setSelectedValue(prMotivo);
//			cbxMotivo.setSelectedValue(tipocure);
//			cbxBoxSegn.setRequired(!codRichiedente.equals(""));
//			cbxTipute.setRequired(!tipoUte.equals(""));
//	
//			if (ISASUtil.valida(tipocure)){
//				cbxMotivo.setDisabled(true);
//			}else {
//				cbxMotivo.setDisabled(false);
//			}
//			cbxTipute.setDisabled(true);
//			cbxBoxSegn.setDisabled(true);
//		}
//		
//		if (!skm_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
//			skm_descr_contatto.setValue(skm_descr_contatto.getValue() + CostantiSinssntW.DA_SCHEDA_SO);
//		// }
//	}
	
//	private void recuperaDatiSettaggio() throws CariException {
//		String punto = ver + "recuperaDatiSettaggio ";
//
//		Hashtable<String,String> datiDaInviare = new Hashtable<String,String>();
//		String idSkSo = (id_skso.getValue() !=null ? id_skso.getValue()+"":"");
//		datiDaInviare.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
//		datiDaInviare.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
//		logger.debug(punto + " dati che invio>>" + datiDaInviare);
//		// if (ISASUtil.valida(id_skso_url)){
//		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
//		ISASRecord dbrValutazione = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(),
//				datiDaInviare);
//		idSkSo = ISASUtil.getValoreStringa(dbrValutazione, CostantiSinssntW.CTS_ID_SKSO);
//		if (ISASUtil.valida(idSkSo)){
//			
//			String codRichiedente = ISASUtil.getValoreStringa(dbrValutazione, "richiedente");
//			String tipoUte = ISASUtil.getValoreStringa(dbrValutazione, "tipo_ute");
//			String tipocure = ISASUtil.getValoreStringa(dbrValutazione, "tipocura");
//			logger.debug(punto + " dati codRichiedente>" + codRichiedente + "<codRichiedente>" + codRichiedente
//					+ "< tipocure>" + tipocure);
//			cbxBoxSegn.setSelectedValue(codRichiedente.equals("") ? "9" : codRichiedente);
//			cbxTipute.setSelectedValue(tipoUte);
//			// cbxMotivo.setSelectedValue(prMotivo);
//			cbxMotivo.setSelectedValue(tipocure);
//			cbxBoxSegn.setRequired(!codRichiedente.equals(""));
//			cbxTipute.setRequired(!tipoUte.equals(""));
//	
//			if (ISASUtil.valida(tipocure)){
//				cbxMotivo.setDisabled(true);
//			}else {
//				cbxMotivo.setDisabled(false);
//			}
//			cbxTipute.setDisabled(true);
//			cbxBoxSegn.setDisabled(true);
//		}
//		
//		if (!this.skm_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
//			this.skm_descr_contatto.setValue(skm_descr_contatto.getValue() + CostantiSinssntW.DA_SCHEDA_SO);
//		// }
//	}
	
	
}
package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.sins_pht.util.CostantiPHT;
import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.modificati.SkMedEJB;
import it.caribel.app.sinssnt.bean.nuovi.EsitiValutazioniUviEJB;
import it.caribel.app.sinssnt.bean.nuovi.ListaAttivitaEJB;
import it.caribel.app.sinssnt.bean.nuovi.PAI_FSE_EJB;
import it.caribel.app.sinssnt.bean.nuovi.RMPuaUvmCommissioneEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOBaseEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.RmRichiesteMMGEJB;
import it.caribel.app.sinssnt.bean.nuovi.SCBisogniEJB;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.autorizzazioni.AutorizzazioniMMGAdiFormCtrl;
import it.caribel.app.sinssnt.controllers.autorizzazioni.AutorizzazioniMMGAdpFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.lista_attivita.AttribuzioneDistrettoPht2Ctrl;
import it.caribel.app.sinssnt.controllers.lista_attivita.ListaAttivitaGridCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.login.ManagerProfileBase;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalePanelCtr;
import it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm.CommissioneUvmFormCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.diagnosi.DiagnosiFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviIsasBase;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.DatiStampaCtrl;
import it.caribel.app.sinssnt.util.DatiStampaRichiesti;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.generic_controllers.interfaces.AskDataInput;
import it.caribel.zk.generic_controllers.interfaces.AskDatiInput;
import it.caribel.zk.generic_controllers.interfaces.AskSignDocInput;
import it.caribel.zk.generic_controllers.interfaces.SegreteriaOrganizzativa;
import it.caribel.zk.generic_controllers.messagebox.PannelloDataFormCtrl;
import it.caribel.zk.generic_controllers.messagebox.PannelloSignDocFormCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.dateutility;
import it.pisa.caribel.util.procdate;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

public class SegreteriaOrganizzativaFormCtrl extends SegreteriaOrganizzativaFormBaseCtrl implements SegreteriaOrganizzativa,
			AskDataInput, AskDatiInput, AskSignDocInput {

	private static final long serialVersionUID = 1L;
	public static final int CTS_SO_MESI_DT_PERIODO_PIANO = 12;
	public static final String myKeyPermission = ChiaviIsasBase.SEGRETERIA_ORGANIZZATIVA;
	QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();

	/* convertita a partire da JFrameRASkPuac.java */ 
	private RMSkSOEJB myEJB = new RMSkSOEJB();
	private ListaAttivitaEJB myEJB_att = new ListaAttivitaEJB();
	private SCBisogniEJB bisogni = new SCBisogniEJB();
 	private static final String ver = "293-SegreteriaOrganizzativaFormCtrl ";
 	protected Button trasmettiDistretto;
 	private ListaAttivitaGridCtrl attivita = new ListaAttivitaGridCtrl();

 	
 	private String docPaiPdfEncodedBase64 = "";//Variabile d'appoggio per il documento PAI da caricare su FSE 
 	private String idAsrEmpi="";//Variabile d'appoggio per il codice AsrEmpi necessario al PAI da caricare su FSE 
 	
	@Override
	protected void notEditable() {
		String punto = ver + "notEditable ";
		super.notEditable();
		boolean isPresaCaricoSkSo = ManagerDate.validaData(data_presa_carico_skso); 
		
		data_presa_carico_skso.setReadonly(isPresaCaricoSkSo);
		logger.trace(punto + " attivare idSkso>>" +isPresaCaricoSkSo+"<");
		tipocura.setDisabled(isPresaCaricoSkSo);
		tipoFrequenza.setDisabled(isPresaCaricoSkSo || quadroSanitarioMMGCtrl.isCureResidenziali(this.self));
		boolean presenteVerbaleUvi = true;
		if (!isPresaCaricoSkSo ){
			if (ManagerDate.validaData(data_inizio) && ManagerDate.validaData(data_fine)){
//				if (tipocura.getSelectedItem() != null && 
//						tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
				if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
//					Component presentiUvi = self.getFellowIfAny("SOPresentiUvi", true);
//					CaribelDatebox dataVerbaleUvi= (CaribelDatebox) self.getFellowIfAny("pr_data_verbale_uvm",  true);
					CaribelDatebox  dataVerbaleUvi = (CaribelDatebox) SOPresentiUvi.getFellowIfAny("pr_data_verbale_uvm", true);
					presenteVerbaleUvi = !(ManagerDate.validaData(dataVerbaleUvi));
				}else {
					presenteVerbaleUvi =false;
				}
			}
		}else {
			logger.trace(punto + " disabilito il periodo ");
			data_inizio.setReadonly(true);
			data_fine.setReadonly(true);
			accessi_mmg.setReadonly(true);
		}
//		data_presa_carico_skso.setDisabled(isPresaCaricoSkSo);
		gestioneDataPresaCarico();
		if (insertMode()) {
			data_presa_carico_skso.setDisabled(true);
		}
//		btn_delete.setDisabled(isPresaCaricoSkSo);
	}

	@Override
	public boolean doSaveForm() throws Exception {
		String punto = ver + "doSaveForm() ";
		if (!isSavable()) {
			Messagebox.show(Labels.getLabel("permissions.insufficient.on.doSaveForm"), Labels.getLabel("messagebox.attention"),
					Messagebox.OK, Messagebox.ERROR);
			return false;
		}
		UtilForComponents.testRequiredFields(self);

		if (check_save_step_PrDataPuac == RISPOSTASCHEDASO_PR_DATA_PUAC_INTERSEZIONE_NO) {
			if (!esistonoSovraposizioneDtPrDataPuac()){
				return false;   
			}
		}else {
			logger.trace(punto + " posso salvare check_save_step_PrDataPuac");
		}
		
		if (check_save_step_PresaCaricoSkso == RISPOSTASCHEDASO_DATA_PRESA_CARICO_SKSO_NO) {
			if (!controlloCoerenzaSchedaAttivazione()){
				return false;   
			}
		}else {
			logger.trace(punto + " posso salvare check_save_step_PresaCaricoSkso");
		}
		
		if (check_save_stepConc == RISPOSTACHIUSURACONTATTO_NO) {
			if (!isConclusioneOK()) 
				return false;   
		}else {
			logger.trace(punto + " posso salvare");
		}
		
		if (!doValidateForm())
			return false;

		if (check_save_step == RISPOSTACHIUSURACONTATTO_NO) {
			logger.trace(punto + " non posso effettuare il salvataggio ");
			return false;
		}else {
			logger.trace(punto + " RPOCEDO CON IL salvataggio ");
		}

		if (check_save_stepConc == RISPOSTACHIUSURACONTATTO_NO) {
			logger.trace(punto + " non posso effettuare il salvataggio ");
			if (!isConclusioneOK()){
				return false;
			}
		}else {
			logger.trace(punto + " PROCEDO CON IL salvataggio ");
		}

		if (check_save_Conclusione == RISPOSTACHIUSURACONTATTO_NO) {
			logger.trace(punto + " controllo la presenza di PC/PV da effettuare");
			if (!isConclusionePresente()){
				return false;
			}
		}else {
			logger.trace(punto + " PROCEDO CON IL salvataggio ");
		}
		
		if (conclusionePresente() && !isAllScBisogni(pr_data_chiusura.getValueForIsas(), CTS_MESSAGGIO_CONCLUSIONE_SO)){
			logger.trace(punto + " controllo presenza della conclusione sia dentro le scale bisogni");
			return false;
		}else {   
			logger.trace(punto + " PROCEDO CON IL salvataggio ");
		}
		
		if (check_save_stepMsgControlloPresaCarico == RISPOSTACHIUSURACONTATTO_NO) {
			logger.trace(punto + " non posso effettuare il salvataggio ");
			if (!isMessaggioControlloPresaCarico()){
				return false;
			}
		}else {
			logger.trace(punto + " PROCEDO CON IL salvataggio ");
		}
		
		if (check_save_stepMsgPeriodoSupAnno == RISPOSTACHIUSURACONTATTO_NO) {
			logger.trace(punto + " peridoSuperiore anno");
			if (!isMessaggioPeriodoSuperioreAnno()){
				return false;
			}
		}else {
			logger.trace(punto + " PROCEDO CON IL salvataggio ");
		}

		if (check_save_stepMsgPrimaVisita == RISPOSTACHIUSURACONTATTO_NO) {
			logger.trace(punto + " non posso effettuare il salvataggio ");
			if (!verificaPrimaVisita()){
				return false;
			}
		}else {
			logger.trace(punto + " PROCEDO CON IL salvataggio ");
		}		
		
		if (check_save_stepMsgAvvisoPresaCarico == RISPOSTACHIUSURACONTATTO_NO ) {
			settaSaveMsgAvvisoPresaCarico(RISPOSTACHIUSURACONTATTO);
			if (!msgAvvisoPresaCarico()){
				return false;
			}
		}
		
		boolean curePrestazionali = quadroSanitarioMMGCtrl.isCurePrestazionali(this.self);
		boolean salvareDati = true;
		if (curePrestazionali){
			salvareDati = quadroSanitarioMMGCtrl.controlloDatiSanitari(this.self, "dati_scheda_uvm");
			if (!salvareDati){
				return salvareDati;
			}
		}

		boolean cureResidenziali = quadroSanitarioMMGCtrl.isCureResidenziali(this.self);
		if (cureResidenziali && isIdSksoPresente()){
			doSalvaRichiestaIstituti();
		}
		
		CaribelGridStateCtrl paiCtrl;
		CaribelListbox listaPai;
		if(quadroSanitarioMMGCtrl.isCureDomiciliari(this.self) || quadroSanitarioMMGCtrl.isCureResidenziali(this.self)){
			paiCtrl = (CaribelGridStateCtrl) SOPai.getAttribute(MY_CTRL_KEY);
			listaPai = caribellb2;
		}else{
			paiCtrl = (CaribelGridStateCtrl) SOCP.getAttribute(MY_CTRL_KEY);
			listaPai = listaPrestazioniCP;
		}
		
		doWriteComponentsToBean();
		addCodDistrettoSede();
		
		Vector<Hashtable<String, String>> vettorePAI = UtilForBinding.getDataFromGrid(listaPai); 
//		paiCtrl.getDataFromGrid(); // new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> vettoreObiettivi = UtilForBinding.getDataFromGrid(clbobiettivi);

		boolean insertMode = insertMode();
		if (insertMode()){
			if (this.currentIsasRecord !=null){
				this.hParameters.putAll(this.currentIsasRecord.getHashtable());
			}
			logger.debug(punto + " sto facendo un insert con dati>>"+ this.hParameters+"");
			Object[] par = new Object[1];
			par[0] = this.hParameters;
			this.currentIsasRecord = insertSuEJB(this.currentBean, par);
		} else {
			logger.debug(punto + " Sono in UPDATE ");
			Object[] par = new Object[3];
			//    		par[0]= this.currentIsasRecord.getHashtable();
			par[0] = this.currentIsasRecord;
			par[1] = vettorePAI;
			par[2] = vettoreObiettivi;
			this.currentIsasRecord = updateSuEJB(this.currentBean, par);
		}
		String idSkso = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_ID_SKSO);
		super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
		if (this.caribelGrid != null) {
			//REFRESH SULLA LISTA
			this.caribelGrid.doRefreshNoAlert();
		}
		if (this.caribelContainerCtrl != null) {
			//REFRESH SUL CONTAINER
			caribelContainerCtrl.doRefreshOnSave(compChiamante);
		}
		doWriteBeanToComponents();
		if (insertMode && Executions.getCurrent().getAttribute(CostantiSinssntW.CTS_ID_RICH)!=null && !Executions.getCurrent().getAttribute(CostantiSinssntW.CTS_ID_RICH).toString().equals("")){
			gestisciRichiestaMMG(true);
		}
		chiestaLaData = false;   
		abilitazioneMaschera();
		Clients.showNotification(Labels.getLabel("form.save.ok.notification"), "info", btn_save, "after_center", 2500);
		eseguiOperazioni();
		abilitaComboCommissione();

		aggiuntiBottoneStampaPai();
		aggiungiBottoneCongela();
		aggiungiBottoneCambioPiano();
		
		abilitazioniPianoCongelato();
		
		messaggioIsPatologieInserite = false;
		return true;
	}

	private boolean isAllScBisogni(String dtChiusura, String label) throws Exception {
		String punto = ver + "isAllScBisogni ";
		boolean conclusioneinAllScBisogni = false;
		String messaggio = "";

		if (ManagerDate.validaData(dtChiusura)) {
			logger.trace(punto + " Ci sono le condizioni per la conclusione  ");
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(Costanti.N_CARTELLA, n_cartella.getText());
			dati.put(Costanti.CTS_ID_SKSO, id_skso.getValue() + "");
			dati.put(CostantiSinssntW.PR_DATA_CHIUSURA, dtChiusura);
			dati.put(Costanti.CTS_LABEL_MESSAGGIO, label);
			
			messaggio = verificaPresenzaScaleNelPeriodo();
//			messaggio = myEJB.conclusioneIncludeAllScBisogni(CaribelSessionManager.getInstance().getMyLogin(), dati);
			if (ISASUtil.valida(messaggio)) {
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
						Messagebox.EXCLAMATION);
				return false;
			} else {
				// la conclusione include tutte le scale di 
				conclusioneinAllScBisogni = true;
			}
		} else {
			conclusioneinAllScBisogni = true;
		}
		return conclusioneinAllScBisogni;
	}
	
	
	private void addCodDistrettoSede() {
		String punto = ver +"addCodDistrettoSede ";
		try {
			Component p = ubicazioneO.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
			String codDistretto = c.getDistrettoValue();
			String codSede = c.getPresidioComuneAreaValue();
			this.currentIsasRecord.put(CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO, codDistretto);
			this.currentIsasRecord.put(CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO, codSede);
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare il distretto e/o sede ", e);
		}
	}

	private void eseguiOperazioni() throws Exception {
//		if (tabpanel_scale.getFirstChild() != null){
//			tabpanel_scale.getFirstChild().detach();
//		}
		doFreezeForm();
//		gestionePannelloScale();
		settaControlli();
		doCaricaOpCoinvolti();
		doCaricaPreferenzeIstituti(true);
		doCaricaIngressoIstituti();
		doCaricaGriglia();
		notEditable();
		tabpanel_scale.getTabbox().setSelectedIndex(0);
		verificaIsPatologieInserite();
	}

	private boolean msgAvvisoPresaCarico() {
		String punto = ver+ "msgAvvisoPresaCarico ";
		boolean messaggioD= true;
			
			if (isPianoCongelato.isChecked()&& !ManagerDate.validaData(data_presa_carico_skso)){
				try {
					String lblDtInizio= Labels.getLabel("RichiestaMMG.principale.data_inizio");
					String lblDtFine = Labels.getLabel("RichiestaMMG.principale.data_fine");
					String[] lables = new String[] {lblDtInizio, lblDtFine};
					
					String messaggio = Labels.getLabel("so.conferma.chiusura.msg.mancata.presa.carico", lables);
					
					Clients.showNotification(messaggio,"info", self, "middle_center",CostantiSinssntW.INT_TIME_OUT);
					settaSaveMsgAvvisoPresaCarico(RISPOSTACHIUSURACONTATTO);
//					Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"),
//					Messagebox.OK, Messagebox.EXCLAMATION,  new EventListener<Event>() {
//						public void onEvent(Event event) throws Exception{
//							settaSaveMsgAvvisoPresaCarico(RISPOSTACHIUSURACONTATTO);
//						}
//					});
//					return false;
				} catch (Exception e) {
					logger.error(punto + " Errore nel comporre il messaggio ");
				}
//			}else {
//				logger.trace(punto + " data inserita ");
//				settaSaveMsgAvvisoPresaCarico(RISPOSTACHIUSURACONTATTO_NO);
			}else {
				messaggioFigureCoinvolte();
			}
//		}
		return messaggioD;
	 
	}

	private void messaggioFigureCoinvolte() {
		String punto = ver + "messaggioFigureCoinvolte ";
		logger.trace(punto + " verifico ");
		if (quadroSanitarioMMGCtrl.isCureDomiciliari(self) && (!insertMode())){ 
			if (griglia_op_coinvolti!=null && griglia_op_coinvolti.getItemCount() <=0){
				String messaggio = Labels.getLabel("so.conferma.chiusura.msg.mancata.presenza.figura.coinvolte"); 
				Clients.showNotification(messaggio,"info", self, "middle_center",CostantiSinssntW.INT_TIME_OUT);
			}
		}
		 
	}

	private boolean verificaPrimaVisita() throws Exception {
		String punto = ver + "isMessaggioPresaCarico ";
		boolean inseritaPrimaVisita= true;
		
		if( tipoOperatoreSelezionato() && (!ISASUtil.valida(pv_cod_operatore.getValue()))  ){
			logger.trace(punto + " tipo operatore inserito, ma non inserito il codice operatore ");
				String messaggio = Labels.getLabel("menu.segreteria.organizzativa.scheda.msg.prima.visita", new String[]{});
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
//				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION, new EventListener<Event>() {
//							public void onEvent(Event event) throws Exception{
//								settaSavePrimaVisita(RISPOSTACHIUSURACONTATTO_NO);
////								doSaveForm();
//							}
//						});
				return false;
//			}else {
//				logger.trace(punto + " non ci tipi operatori selezionati ");
//				settaSavePrimaVisita(RISPOSTACHIUSURACONTATTO);
//				doSaveForm();
			}
		return inseritaPrimaVisita;
	}

	
	private boolean preferenzeRsaModificate() throws Exception {
		String punto = ver + "preferenzeModificate ";
		boolean datiMofificati = true;
		
		
		Component prefStrutture = self.getFellowIfAny("SOPreferenzeStruttura", true);
		if(prefStrutture!=null){
			PreferenzeStruttureCtrl preferenzeCtrl = (PreferenzeStruttureCtrl) prefStrutture.getAttribute(MY_CTRL_KEY);
			datiMofificati = !preferenzeCtrl.getModificatiDati();
			if (datiMofificati){
				logger.trace(punto + " Non ci sono dati modificati per le preferenze ");
			}else {	
				logger.trace(punto + " data inserita ");
				preferenze_strutture.getLinkedTab().setSelected(true);
				String messaggio = Labels.getLabel("so.conferma.messaggio.preferenze.rsa.salvare", new String[]{});
				Messagebox.show(messaggio,
						Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception{
						if (Messagebox.ON_YES.equals(event.getName())) {
							settaSaveMessaggioPreferenzeStrutture(RISPOSTACHIUSURACONTATTO);
							doSaveForm();
						}
					}
				});
				return false;
			}
		}
		return datiMofificati;
	}
	
	protected void settaSaveMessaggioPreferenzeStrutture(int risposta) {
		check_save_PreferenzeStrutture = risposta;
	}

	private void settaControlli() {
		settaSaveStep(RISPOSTACHIUSURACONTATTO_NO);
		settaSaveStepConcl(RISPOSTACHIUSURACONTATTO_NO);
//		settaSaveMessaggioPresaCarico(RISPOSTACHIUSURACONTATTO_NO);
		settaSaveMessaggioPeriodoSuperioreAnno(RISPOSTACHIUSURACONTATTO_NO);
		settaSaveMsgAvvisoPresaCarico(RISPOSTACHIUSURACONTATTO_NO);
		settaSaveMsgControlloPresaCarico(RISPOSTACHIUSURACONTATTO_NO);
	}
	
	public void doCaricaOpCoinvolti() throws Exception {
		Component opCoinvolti = self.getFellowIfAny("SOOperatoriCoinvolti", true);
		if(opCoinvolti!=null){
			SOOperatoreCoinvoltiCtrl opCoinvoltiCtrl = (SOOperatoreCoinvoltiCtrl) opCoinvolti.getAttribute(MY_CTRL_KEY);
			opCoinvoltiCtrl.doPublicLoadGrid(n_cartella.getText(), id_skso.getText());
		}
	}
	 
	private void doSalvaRichiestaIstituti() throws Exception {
		Component preferenzeStrutture = self.getFellowIfAny("SOPreferenzeStruttura", true);
		if(preferenzeStrutture!=null){
			PreferenzeStruttureCtrl preferenzeStruttureCtrl= (PreferenzeStruttureCtrl) preferenzeStrutture.getAttribute(MY_CTRL_KEY);
			preferenzeStruttureCtrl.doSaveForm(n_cartella.getText(), id_skso.getText());
		}
	}
	
	private boolean isConclusioneOK() throws Exception {
		String punto = ver + "isConclusioneOK ";
		boolean conclusioneOk= true;
		
		if(pr_motivo_chiusura.getSelectedItem()!=null && ISASUtil.valida(pr_motivo_chiusura.getSelectedValue())){
			logger.trace(punto + " dati selezionati ");
			if(!ManagerDate.validaData(pr_data_chiusura)){
				pr_data_chiusura.setErrorMessage(Labels.getLabel("menu.segreteria.organizzativa.scheda.uvm.data.chiusura.errore"));
				conclusioneOk = false;
				return conclusioneOk;
			}
		}
		
		if(ManagerDate.validaData(pr_data_chiusura)){
			conclusioneOk = controllaCoerenzaDtConclusione();
			if (!conclusioneOk){
				return conclusioneOk;
			}
			
			if (controlloPresenzaAccessi(pr_data_chiusura.getValueForIsas(), CostantiSinssntW.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO)){
				logger.trace(punto + "Ci sono degli accessi per cui non si può salvare!");
				return false;
			}
			
			if(pr_motivo_chiusura.getSelectedItem()==null || !ISASUtil.valida(pr_motivo_chiusura.getSelectedValue())){
				pr_motivo_chiusura.setErrorMessage(Labels.getLabel("menu.so.scheda.uvm.motivo.chiusure.errore"));
				conclusioneOk = false;   
				return conclusioneOk;
			}
			if (check_save_step ==0){
				conclusioneOk = (esistonoSospensioniProroghe());
			}
			if (conclusioneOk){
				Messagebox.show(Labels.getLabel("so.conferma.chiusura.msg", new String[] {}),
						Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
							public void onEvent(Event event) throws Exception{
								if (Messagebox.ON_YES.equals(event.getName())) {
									settaSaveStepConcl(RISPOSTACHIUSURACONTATTO);
									doSaveForm();
								}
							}
						});
				return false;
			}
		}else {
			logger.trace(punto + " posso salvare ");
			settaSaveStep(RISPOSTACHIUSURACONTATTO);
			settaSaveStepConcl(RISPOSTACHIUSURACONTATTO);
		}
		return conclusioneOk;
	}

	private String verificaPresenzaScaleNelPeriodo() {
		String messaggio = "";
		HashMap<String, Object> map = recuperaDatiPerScalaValutazione();
		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		dati.putAll(map);
		dati.put(CostantiSinssntW.PR_DATA_CHIUSURA, pr_data_chiusura.getValueForIsas());
		
		messaggio = myEJB.getVerificaPresenzaScale(CaribelSessionManager.getInstance().getMyLogin(),dati);
		return messaggio;
	}
	
	private boolean controlloPresenzaAccessi(String dataChiusura, String testoLabel) throws SQLException {
		String punto = ver + "controlloPresenzaAccessi ";
		boolean presenzaAccessi = false;
		Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
		controlloInterv.put(Costanti.N_CARTELLA, n_cartella.getText());
		controlloInterv.put(CostantiSinssntW.PR_DATA_CHIUSURA, dataChiusura);
		String msg = "";
		String messaggio = myEJB.query_controlloPresenzaAccessi(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);
		if (ISASUtil.valida(messaggio)){
			String[] sost = new String[]{messaggio};
			msg = Labels.getLabel(testoLabel, sost);
			Messagebox.show(msg,Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
		}
		
		presenzaAccessi = ISASUtil.valida(msg);
		logger.trace(punto + " Ci sono presenzaAccessi>>"+ msg+"<\n"+ presenzaAccessi);
		
		return presenzaAccessi;
	}


	private boolean isConclusionePresente() throws Exception {
		String punto = ver + "isConclusionePresente ";
		boolean figCoinvolteTuttePreseCarico = false;
		boolean primaVisitaEffettuata = false;
		String messaggio = "";
		
		if(conclusionePresente()){
			logger.trace(punto + " verificare la presenza di PC/PV ");
			figCoinvolteTuttePreseCarico = isPresaCaricoEffettuate();			
			primaVisitaEffettuata = isPrimaVisitaEffettuata();
			
			if (!figCoinvolteTuttePreseCarico || !primaVisitaEffettuata){
				messaggio = "";
				if (!figCoinvolteTuttePreseCarico){
					messaggio = Labels.getLabel("so.conferma.chiusura.so.msg.presa.carico");
				}
				if (!primaVisitaEffettuata){
					messaggio += (ISASUtil.valida(messaggio)?", ":"")+Labels.getLabel("so.conferma.chiusura.so.msg.prima.visita");
				}
				
				if (ISASUtil.valida(messaggio)){
					String msg= Labels.getLabel("so.conferma.chiusura.so.msg.conclusione", new String[] {messaggio})  ;
					Messagebox.show(msg,
							Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
						public void onEvent(Event event) throws Exception{
							if (Messagebox.ON_YES.equals(event.getName())) {
								settaSaveConclusione(RISPOSTACHIUSURACONTATTO);
								doSaveForm();
							}
						}
					});
					return false;
				}
			}
		}else{
			logger.trace(punto + " Non inserita la conclusione");
		}
		
		return (!ISASUtil.valida(messaggio));
	}

	public boolean conclusionePresente() {
		return pr_motivo_chiusura.getSelectedItem()!=null 
				&& ISASUtil.valida(pr_motivo_chiusura.getSelectedValue())
				&& ManagerDate.validaData(pr_data_chiusura);
	}
	
	
	private boolean isPrimaVisitaEffettuata() {
		boolean primaVisitaEffettuata = false;
		primaVisitaEffettuata = ManagerDate.validaData(pv_dt_visita);
		return primaVisitaEffettuata;
	}

	private boolean isPresaCaricoEffettuate() {
		String punto = ver + "isPresaCaricoEffettuate ";
		boolean presaCaricoEffettuate = false;
		if (griglia_op_coinvolti!=null){
			CaribelListModel clm = (CaribelListModel) griglia_op_coinvolti.getModel();
			int numRighe = clm.getSize();
			ISASRecord dbrSksoOpCoinvolti;
			int i=0;
			presaCaricoEffettuate = true;
			while(presaCaricoEffettuate && i<numRighe ){
				dbrSksoOpCoinvolti = (ISASRecord)clm.get(i);
				if(dbrSksoOpCoinvolti!=null){
					logger.trace(punto + " Inizio con dati "+ dbrSksoOpCoinvolti.getHashtable()+"<");
					String dtPresaCarico = ISASUtil.getValoreStringa(dbrSksoOpCoinvolti, "dt_presa_carico");
					presaCaricoEffettuate = ManagerDate.validaData(dtPresaCarico);
				}
				i++;
			}
		}
		return presaCaricoEffettuate;
	}

	private boolean controllaCoerenzaDtConclusione() {
		String punto = ver + "controllaCoerenzaDtConclusione ";
		boolean conclusioneOk = true;
		try {
			if ((ManagerDate.validaData(data_presa_carico_skso))){
				conclusioneOk = ManagerDate.controllaPeriodo(self, data_presa_carico_skso, pr_data_chiusura,"lb_data_presa_carico_skso", "lb_pr_data_chiusura");
				logger.trace(punto + " controllo la data presa carico skso>>"+ conclusioneOk);
			}
			if (conclusioneOk && (ManagerDate.validaData(pr_data_puac))){
				conclusioneOk = ManagerDate.controllaPeriodo(self, pr_data_puac, pr_data_chiusura,"lb_pr_data_puac", "lb_pr_data_chiusura");
				logger.trace(punto + " controllo la data presa carico skso>>"+ conclusioneOk);
			}
		} catch (Exception e) {
			logger.trace(punto + " Errore nel recuperare la data presa carico");
		}
		return conclusioneOk;
	}

	@Override
	public void settaCampiDataObligatoriPresaData() {
		String punto = ver + "settaCampiDataObligatoriPresaData ";
		logger.trace(punto + " setto la data del verbale uvm ");
		CaribelDatebox dataVerbaleUvi= (CaribelDatebox) self.getFellowIfAny("pr_data_verbale_uvm",  true);
		if (dataVerbaleUvi!=null){
			dataVerbaleUvi.setRequired(false);
		}
		if (ManagerDate.validaData(data_presa_carico_skso)){
			if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
				dataVerbaleUvi.setRequired(true);
			}
		}
	}
	
	private boolean isMessaggioControlloPresaCarico() throws Exception {
		String punto = ver + "isMessaggioControlloPresaCarico ";
		CaribelDatebox dataVerbaleUvi= (CaribelDatebox) self.getFellowIfAny("pr_data_verbale_uvm",  true);
		boolean messaggioPresaCarico= true;
			if (ManagerDate.validaData(data_presa_carico_skso)){
				if (ManagerDate.validaData(data_inizio) && ManagerDate.validaData(data_fine)){
					boolean presenteVerbaleUvi = false;
//					if (tipocura.getSelectedItem() != null && 
//							tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
					if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
						dataVerbaleUvi.setRequired(true);
						presenteVerbaleUvi = !(ManagerDate.validaData(dataVerbaleUvi));
					}
					if (presenteVerbaleUvi){
						logger.trace(punto + " data uvi non presente ");
						Messagebox.show(Labels.getLabel("so.conferma.chiusura.msg.data.uvi.non.presente"),
						Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
						return false;
					}
				}else {
					logger.trace(punto + " date non valida ");
					Messagebox.show(Labels.getLabel("so.conferma.chiusura.msg.data.uvi.periodo"),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
					logger.trace("\n VERIFICARE: \n"+ punto);
					try {
						if (!ManagerDate.validaData(data_inizio)){
//							data_inizio.getf
							logger.trace(punto + "VERIFICARE  seleziono dt inizo ");
							data_inizio.setFocus(true);
						}else if (!ManagerDate.validaData(data_fine)){
							logger.trace(punto + " seleziono dt fine  ");
							data_fine.setFocus(true);
						}
					} catch (Exception e) {
						logger.error(punto + " Errore nel settare il focus ");
//						e.printStackTrace();
					}
					return false;
				}
			}
			return messaggioPresaCarico;
	}
	
	private boolean isMessaggioControlloCoerenzaDate() throws Exception {
		String punto = ver + "isMessaggioControlloCoerenzaDate ";
		boolean messaggioCoerenzaPeriodo = true;
		if( (ManagerDate.validaData(data_inizio)&& !data_inizio.isDisabled() ) ){
			messaggioCoerenzaPeriodo = ManagerDate.controllaPeriodo(self, pr_data_puac, data_inizio,"lb_pr_data_puac","lb_dataInizio");
		}
		if (messaggioCoerenzaPeriodo && 
				(ManagerDate.validaData(data_presa_carico_skso)&& !data_presa_carico_skso.isDisabled() ) ){
			messaggioCoerenzaPeriodo = ManagerDate.controllaPeriodo(self, pr_data_puac, data_presa_carico_skso,"lb_pr_data_puac","lb_data_presa_carico_skso");
			logger.trace(punto + " controllo la data presa carico skso>>"+ messaggioCoerenzaPeriodo);
		}
		
		try {
			CaribelDatebox  dataVerbaleUvi = (CaribelDatebox) SOPresentiUvi.getFellowIfAny("pr_data_verbale_uvm", true);
			if (messaggioCoerenzaPeriodo && 
					(ManagerDate.validaData(dataVerbaleUvi)&& !dataVerbaleUvi.isDisabled() ) ){
				messaggioCoerenzaPeriodo = ManagerDate.controllaPeriodo(self, pr_data_puac, dataVerbaleUvi,"lb_pr_data_puac","lb_pr_data_verbale_uvm");
				logger.trace(punto + " controllo la data del verbale UVI >>"+ messaggioCoerenzaPeriodo);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		logger.trace(punto + " controllo coerenza periodo>>" + messaggioCoerenzaPeriodo +"< ");
		return messaggioCoerenzaPeriodo;
	}
	
	private boolean isMessaggioPeriodoSuperioreAnno() throws Exception {
		String punto = ver + "isMessaggioPeriodoSuperioreAnno ";
		boolean messaggioPeriodoSuperioreAnno= true;
		if( (ManagerDate.validaData(data_inizio)&& !data_inizio.isDisabled() )&& 
			(ManagerDate.validaData(data_fine)&& !data_fine.isDisabled() ) ){
			int numeroGiorni = ManagerDate.getNumeroGiorniData(data_inizio, data_fine);
			
			logger.trace(punto + " data inserita numeroGiorni>"+numeroGiorni+"<<");
			if (numeroGiorni>365){
				logger.trace(punto + " data inserita ");
				String messaggio = Labels.getLabel("so.conferma.periodo.superioreAnno", new String[]{});
					
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"),
						Messagebox.OK, Messagebox.EXCLAMATION);
//				settaSaveMessaggioPeriodoSuperioreAnno(RISPOSTACHIUSURACONTATTO_NO);
//				Messagebox.show(messaggio,
//							Labels.getLabel("messagebox.attention"), Messagebox., Messagebox.QUESTION, new EventListener<Event>() {
//								public void onEvent(Event event) throws Exception{
//										settaSaveMessaggioPeriodoSuperioreAnno(RISPOSTACHIUSURACONTATTO_NO);
//								}
//							});
					return false;
//			}else {
//					logger.trace(punto + " differenza gg>>"+numeroGiorni+"< ");
//					settaSaveMessaggioPeriodoSuperioreAnno(RISPOSTACHIUSURACONTATTO);
//					doSaveForm();
			}
		}
//		else {
//			logger.trace(punto + " Non c'Ã¨ il periodo  ");
//			settaSaveMessaggioPeriodoSuperioreAnno(RISPOSTACHIUSURACONTATTO);
//			doSaveForm();
//		}
		return messaggioPeriodoSuperioreAnno;
	}
	
	private void settaSaveStep(int step) {
		check_save_step = step;
	}
	
	private void settaSaveStepPrDataPuac(int step) {
		check_save_step_PrDataPuac = step;
	}
	
	private void settaSaveStepDataPresaCaricoSkso(int step) {
		check_save_step_PresaCaricoSkso = step;
	}
	private void settaSaveStepConcl(int step) {
		check_save_stepConc = step;
	}
	private void settaSaveConclusione(int step) {
		check_save_Conclusione = step;
	}
	
	private void settaSaveMessaggioPeriodoSuperioreAnno(int step) {
		check_save_stepMsgPeriodoSupAnno = step;
	}

	private void settaSaveMsgAvvisoPresaCarico(int step) {
		check_save_stepMsgAvvisoPresaCarico = step;
	}
	
//	private void settaSaveMessaggioPresaCarico(int step) {
//		check_save_stepMsgPresaCarico = step;
//	}

	private void settaSaveMsgControlloPresaCarico(int step) {
		check_save_stepMsgControlloPresaCarico = step;
	}

//	private void settaSaveMsgMMGPLS(int step) {
//		check_save_stepMsgMMGPLSInserire= step;
//	}
	
	
	private boolean esistonoSospensioniProroghe() throws Exception {
			String punto = ver + "esistonoSospensioniProroghe ";
			boolean esisteSovrapposizione = false;
			Hashtable dati = new Hashtable();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
			dati.put(CostantiSinssntW.CTS_ID_SKSO, id_skso.getValue()+"");
			dati.put(CostantiSinssntW.PR_DATA_CHIUSURA, pr_data_chiusura.getValueForIsas());
			
			String attiveSospensioni = myEJB.esisteProrogheSospensioniAttive(CaribelSessionManager.getInstance().getMyLogin(), dati); 
			if (ISASUtil.valida(attiveSospensioni)){
					Messagebox.show(Labels.getLabel(attiveSospensioni, new String[] {}),
							Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
								public void onEvent(Event event) throws Exception{
									if (Messagebox.ON_YES.equals(event.getName())) {
										settaSaveStep(RISPOSTACHIUSURACONTATTO);
										doSaveForm();
									}
								}
							});
					return false;
			}else {
				logger.trace(punto + " non ci sono proroghe e sospensioni>>"+attiveSospensioni+"< ");
				settaSaveStep(RISPOSTACHIUSURACONTATTO);
				doSaveForm();
			}
			return esisteSovrapposizione;
		}
	
	private boolean esistonoSovraposizioneDtPrDataPuac() throws Exception {
		String punto = ver + "esistonoSovraposizioneDtPrDataPuac ";
		boolean esisteSovrapposizioneDate = true;
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		dati.put(CostantiSinssntW.CTS_PR_DATA_PUAC, pr_data_puac.getValueForIsas());
		
		ISASRecord dbrRmSkso = myEJB.esisteSovrapposizioneDtPrDataPuac(CaribelSessionManager.getInstance().getMyLogin(), dati); 
		String messaggio = "";
		
		if (dbrRmSkso !=null){
				String prDtConclusioneDb = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.PR_DATA_CHIUSURA);
				String prDataPuacDb = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_PR_DATA_PUAC);
				String idSkso = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_ID_SKSO);
				String[] sostituire; 
				if (ManagerDate.validaData(prDtConclusioneDb)){
					sostituire = new String[]{ManagerDate.formattaDataIta(pr_data_puac.getValueForIsas(),"/"),
							idSkso,
							ManagerDate.formattaDataIta(prDataPuacDb,"/"),
							ManagerDate.formattaDataIta(prDtConclusioneDb,"/")};
					messaggio = Labels.getLabel("segreteria.organizzativa.pr_data_puac.esiste.scheda.conclusa", sostituire);
				}else {
					sostituire = new String[]{ManagerDate.formattaDataIta(pr_data_puac.getValueForIsas(),"/"),
							idSkso,
							ManagerDate.formattaDataIta(prDataPuacDb,"/")};
					messaggio = Labels.getLabel("segreteria.organizzativa.pr_data_puac.esiste.scheda.aperta", sostituire);
				}
				pr_data_puac.setFocus(true);
				Messagebox.show(messaggio,Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.ERROR);
				settaSaveStepPrDataPuac(RISPOSTASCHEDASO_PR_DATA_PUAC_INTERSEZIONE_NO);
				esisteSovrapposizioneDate = false;
		}else {
			logger.trace(punto + " non ci sono intersezioni con la data>>"+dati+"< ");
			settaSaveStepPrDataPuac(RISPOSTASCHEDASO_PR_DATA_PUAC_INTERSEZIONE);
		}
		return esisteSovrapposizioneDate;
	}

	
	
	protected boolean controlloCoerenzaSchedaAttivazione() {
		String punto = ver + "controlloCoerenzaSchedaAttivazione";
		logger.trace(punto + "controllo data ");
		boolean esisteSovrapposizioneDataPresaCarico = true;
		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		dati.put(CostantiSinssntW.CTS_PR_DATA_PUAC, data_presa_carico_skso.getValueForIsas());
		dati.put(CostantiSinssntW.CTS_ID_SKSO, id_skso.getValue() + "");
		dati.put(RMSkSOEJB.ESCLUSO_ESTREMO, new Boolean(true));

		boolean dataPresaCaricoOk = ManagerDate.controllaPeriodo(self, pr_data_puac, data_presa_carico_skso,
				"lb_pr_data_puac", "lb_data_presa_carico_skso");
		if (dataPresaCaricoOk) {
			ISASRecord dbrRmSkso = myEJB.esisteSovrapposizioneDtPrDataPuac(CaribelSessionManager.getInstance()
					.getMyLogin(), dati);
			String messaggio = "";
			if (dbrRmSkso != null) {
				String prDtConclusioneDb = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.PR_DATA_CHIUSURA);
				String idSkso = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_ID_SKSO);
				String[] sostituire;
				if (ManagerDate.validaData(prDtConclusioneDb)) {
					sostituire = new String[] {
							ManagerDate.formattaDataIta(data_presa_carico_skso.getValueForIsas(), "/"), idSkso,
							ManagerDate.formattaDataIta(prDtConclusioneDb, "/") };
					messaggio = Labels.getLabel("segreteria.organizzativa.data_presa_carico_skso.no.attivazione",
							sostituire);
				}

				data_presa_carico_skso.setFocus(true);
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.ERROR);
				settaSaveStepDataPresaCaricoSkso(RISPOSTASCHEDASO_DATA_PRESA_CARICO_SKSO_NO);
				esisteSovrapposizioneDataPresaCarico = false;
			} else {
				logger.trace(punto + " non ci sono intersezioni con la data>>" + dati + "< ");
				settaSaveStepDataPresaCaricoSkso(RISPOSTASCHEDASO_DATA_PRESA_CARICO_SKSO_OK);
			}
		} else {
			esisteSovrapposizioneDataPresaCarico = false;
		}
		return esisteSovrapposizioneDataPresaCarico;
	}	
	
	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = ver + this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");
		boolean possoProseguire = true;
		try {
			UtilForComponents.testRequiredFields(self);
		} catch (Exception e2) {
			possoProseguire = false;
			logger.trace(punto + " ci sono dei dati da salvare ");
		}
		if (!possoProseguire){
			logger.trace(punto + "ERRORE non posso proseguire ci sono dei dati obbligatori nella frame che vanno salvati ");
			return ;
		}
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		Date dtApertura = pr_mmg_data_richiesta.getValue();
		if (ManagerDate.validaData(pr_mmg_data_richiesta)){
			logger.trace(punto + " uso la dt della prmmg");
			dtApertura = pr_mmg_data_richiesta.getValue();
		}else {
			dtApertura = pr_data_puac.getValue(); 
		}
		map.put(CostantiSinssntW.DATA_APERTURA, dtApertura);
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
					CaribelListModel lm = new CaribelListModel();
					Hashtable h = new Hashtable();
					h.put("data_apertura", pr_mmg_data_richiesta.getValueForIsas());
					h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
					String dtApertura = pr_mmg_data_richiesta.getValueForIsas();
					if (ManagerDate.validaData(pr_mmg_data_richiesta)){
						logger.trace("sss uso la dt della prmmg");
					}else {
						dtApertura = pr_data_puac.getValueForIsas(); 
					}
					h.put(CostantiSinssntW.DATA_APERTURA, dtApertura);
//					lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryLastDiagContesto"));
					lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryStoricoDiag"));
					tablePrestazioni.setModel(lm);
					doFreezeForm();
				}
			});
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	public void onAggiornaPiano(ForwardEvent e) throws Exception {
		logger.debug(" Aggiorna piano inizio ");
		boolean abilitatoAggiornaPiano = true;
		if(!abilitatoAggiornaPiano){
			Clients.showNotification(Labels.getLabel("segreteria.organizzativa.msg.noAggiornaPiano"));
			return;
		}
		isPianoCongelato.setChecked(false);
		//doSaveForm();
		if(!adp.isChecked()){
			UtilForBinding.setComponentReadOnly(tipoFrequenza, false);
			accessi_mmg.setReadonly(false);
		}
		
		if(!ard.isChecked()){
			ard.setDisabled(false);
		}
		if(!aid.isChecked()){
			aid.setDisabled(false);
		}
		if(!vsd.isChecked()){
			vsd.setDisabled(false);
		}
		UtilForBinding.setComponentReadOnly(SOCP, false);
		boolean notSavableORReadOnly = !isSavable() || isReadOnly();
//		btn_congelaCP.setDisabled(isPianoCongelato.isChecked()|| notSavableORReadOnly);
//		gestioneBottoneCongela(!isPianoCongelato.isChecked() && (isSavable() && !isReadOnly()));
		gestioneBottoneCongela();
		if (btn_aggiornaPiano!=null){
			btn_aggiornaPiano.setDisabled(!isPianoCongelato.isChecked() || notSavableORReadOnly);
		}

//		CaribelGridStateCtrl paiCtrl;
//		if(tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
//			paiCtrl = (CaribelGridStateCtrl) SOPai.getAttribute(MY_CTRL_KEY);
//		}else{
//			paiCtrl = (CaribelGridStateCtrl) SOCP.getAttribute(MY_CTRL_KEY);
//		}		
	}
	
	protected boolean doCambioPiano(String dataCambioPiano){
		String punto = ver + "doCambioPiano ";
		try {
			if (quadroSanitarioMMGCtrl.isCurePrestazionali(self)) {
				onAggiornaPiano(null);
			} else if (quadroSanitarioMMGCtrl.isCureDomiciliari(self)
					|| quadroSanitarioMMGCtrl.isCureResidenziali(self)) {
				if (!isAllScBisogni(dataCambioPiano, CTS_MESSAGGIO_CAMBIA_PIANO )) {
					logger.trace(punto + " controllo presenza della conclusione sia dentro le scale bisogni");
					return false;
				} else {
					logger.trace(punto + " PROCEDO CON IL salvataggio ");
				}
				
				if (controlloPresenzaAccessi(dataCambioPiano, CTS_MESSAGGIO_CAMBIA_PIANO_ACCESSI_DOPO)){
					logger.trace(punto + "Ci sono degli accessi per cui non si può salvare!");
					return false;
				}else {
					logger.trace(punto + " PROCEDO CON IL salvataggio ");
				}
				currentIsasRecord.put(CostantiSinssntW.DATA_CHIUSURA, dataCambioPiano);
				currentIsasRecord = invokeSuEJB(myEJB, currentIsasRecord.getHashtable(), "cambioPiano");
				doWriteBeanToComponents();
				String nCartella = ISASUtil.getValoreStringa(currentIsasRecord,Costanti.N_CARTELLA);
				String idSkso = ISASUtil.getValoreStringa(currentIsasRecord,Costanti.CTS_ID_SKSO);
				ricaricaSO(nCartella,idSkso);
				tabpanel_scale.getTabbox().setSelectedPanel(tabpanel_scale);//mi porto in scale per inserire la valutazione
			}
			
		} catch (Exception e) {
			doShowException(e);
			return false;
		}
		return true;
	}

	
	public void esitoValutazioneRicaricaSO(String nCartella, String idSkso){
		String punto = ver + "esitoValutazioneRicaricaSO ";
		logger.trace(punto + " Ricarico i dati>" +nCartella+ " idSkso>>" + idSkso);
		try {
			Component pandata = self.getFellowIfAny("Esitivalutazione");
			pandata.detach();
			ricaricaSO(nCartella, idSkso);
		} catch (ISASMisuseException e) {
			e.printStackTrace();
			logger.error(punto + " Errore nel ricarica i dati ");
		}
		
	}
	
	private void ricaricaSO(String nCartella, String idSkso) throws ISASMisuseException {
		arg.put(CostantiSinssntW.N_CARTELLA, nCartella);
		arg.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
//		dati_scheda_pai.getTabbox().setSelectedIndex(0);
		griglia_uvi.setModel(new CaribelListModel());
		griglia_op_coinvolti.setModel(new CaribelListModel());
		doInitForm();
		this.caribelContainerCtrl.showComponent(this.getForm());
	}
	
	public void onChangeAblMedicoAltro(){
		String punto = ver +"onChangeAblMedicoAltro ";
		logger.debug(punto + "inizio ");
	}
	
	@Override
	public void onChangeDataRevisione(){
		String punto = ver +"onChangeDataRevisione ";
		logger.debug(punto + "inizio ");
		
		calcolaNuovaRevisione(pr_data_revisione, cbx_pr_revisione, pr_data_verbale_uvm);
	}

	public static void calcolaNuovaRevisione(CaribelDatebox prDataRevisione, CaribelCombobox cbxPrRevisione, CaribelDatebox dataIniziale) {
//		String punto = "";
		prDataRevisione.setRequired(false);
		prDataRevisione.setReadonly(false);
			String dataNew = "";
			int codRevisione = -1;
			if (cbxPrRevisione !=null && cbxPrRevisione.getSelectedValue()!=null){
				try {
					codRevisione = Integer.parseInt(cbxPrRevisione.getSelectedValue());
				} catch (Exception e) {
				}
			}
			if(codRevisione ==0 || codRevisione == 5){
				if (codRevisione == 0){
					prDataRevisione.setText("");
					prDataRevisione.setReadonly(true);
				}else if (codRevisione == 5){
					prDataRevisione.setReadonly(false);
					prDataRevisione.setRequired(true);
				}
			}else {
				String dtOld = dataIniziale.getValueForIsas();
				dataNew = calcolaDataRevisione(codRevisione, dtOld);
				if (ManagerDate.validaData(dataNew)){
					prDataRevisione.setText(dataNew);
					prDataRevisione.setReadonly(true);
				}else {
//					logger.trace(punto + " resetto la data ");
					prDataRevisione.setText("");
					prDataRevisione.setReadonly(false);
				}
			}
	}
	
	public void onChangetipoFrequenza(){
		String punto = ver + "onChangetipoFrequenza ";
		String valoreCombo = tipoFrequenza.getSelectedValue(); 
		logger.trace(punto + " cambio tipo frequenza ");
		int numeroAccessi = 0;
		if (ISASUtil.valida(valoreCombo)){
			int frequenza = Integer.parseInt(valoreCombo); 	
//			if (tipocura.getSelectedItem() != null ){
			if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
//				if ( tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
				numeroAccessi = AutorizzazioniMMGAdiFormCtrl.recuperaFequenzaAdi(frequenza);
			}
			if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)){
				numeroAccessi = AutorizzazioniMMGAdpFormCtrl.recuperaFrequenzaAdp(frequenza);
			}
//			}
		}
		logger.trace(punto + "numero accessi recuperato>>"+ numeroAccessi+"<<");
		accessi_mmg.setValue(numeroAccessi);
		Events.sendEvent(Events.ON_CHANGE, accessi_mmg, null);
		quadroSanitarioMMGCtrl.controlliSuAdp();
	}
	
	@Override 
	public void onChangeIntensitaAssistenziale(){
		String punto = ver +"onChangeIntensitaAssistenziale ";
		logger.debug(punto + "inizio ");
		abilitaObligatorioOperatorePrimaVisita();
		gestioneMessaggioDatiRimossi();
		
		abilitareOperatoriCoinvolti();
		settaCampiDataObligatoriPresaData();
		attivareVerbaleUvi();
		quadroSanitarioMMGCtrl.caricoFrequenza(tipocura, tipoFrequenza, resettaNumeroAccessi,accessi_mmg);
		quadroSanitarioMMGCtrl.settaDatiObbligatori(this.self, ManagerDate.validaData(data_presa_carico_skso) );
		logger.trace(punto + " combo frequenza \n" +CaribelComboRepository.stampaCompbo(tipoFrequenza));
		dati_cure_prestazionali.getLinkedTab().setVisible(isIdSksoPresente()&& quadroSanitarioMMGCtrl.isCurePrestazionali(self));	
	}

	public void gestioneMessaggioDatiRimossi() {
		String punto = ver + "gestioneMessaggioDatiRimossi ";
		int tipoCuraOld = ISASUtil.getValoreIntero(this.currentIsasRecord, Costanti.CTS_TIPOCURA);

		if (!insertMode() && tipoCuraOld >0 && !(tipoCuraOld == ISASUtil.getValoreIntero(tipocura.getSelectedValue())) ) {

			boolean componentiUviNonPresenti = false;
			boolean paiObiettiviPresenti = false;
			boolean paiPresente = false;
			boolean esitoResidenziale = false;
			boolean operatoriCoinvolti = false;
			
			if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)) {
				logger.trace(punto + " Ho impostato le cure PRESTAZIONALI ");
				componentiUviNonPresenti = isComponentiUviInseriti();
				paiObiettiviPresenti = isPaiObiettiviInseriti(tipoCuraOld);
				paiPresente = isPaiInseriti(tipoCuraOld);
				esitoResidenziale = isPaiEsitoResidenziale(tipoCuraOld);
			} else if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)) {
				logger.trace(punto + " Cure residenziali ");
				paiPresente = isPaiInseriti(tipoCuraOld);
				esitoResidenziale = isPaiEsitoResidenziale(tipoCuraOld);
			} else if (quadroSanitarioMMGCtrl.isCureResidenziali(this.self)) {
				paiPresente = isPaiInseriti(tipoCuraOld);
				operatoriCoinvolti = isOperatoriCoinvolti();
			}

			String infoPannelli = "";
			String pannello = recuperaDescrizione(componentiUviNonPresenti,
					"menu.segreteria.organizzativa.scheda.commissioniUVI");
			if (ISASUtil.valida(pannello)){
				infoPannelli += (ISASUtil.valida(infoPannelli) ? ", " : "")+pannello;
			}
			
			pannello = recuperaDescrizione(paiObiettiviPresenti, "menu.segreteria.organizzativa.scheda.paiObiettivi");
			if (ISASUtil.valida(pannello)){
				infoPannelli += (ISASUtil.valida(infoPannelli) ? ", " : "")+pannello;
			}
			
			pannello = recuperaDescrizione(operatoriCoinvolti, "menu.segreteria.organizzativa.scheda.operatori.coinvolti");
			if (ISASUtil.valida(pannello)){
				infoPannelli += (ISASUtil.valida(infoPannelli) ? ", " : "")+pannello;
			}
			pannello="";
			if (paiPresente){
				if (tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)
						|| tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI)) {
					pannello = recuperaDescrizione(paiPresente, "menu.segreteria.organizzativa.scheda.pai");
				} else if (tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_PRESTAZIONALI)) {
					pannello = recuperaDescrizione(paiPresente,
							"menu.segreteria.organizzativa.scheda.curePrestazionali");
				}
			}
			
			if (ISASUtil.valida(pannello)){
				infoPannelli += (ISASUtil.valida(infoPannelli) ? ", " : "")+pannello;
			}
			pannello = recuperaDescrizione(esitoResidenziale,
							"menu.segreteria.organizzativa.scheda.preferenze.struttura");
			if (ISASUtil.valida(pannello)){
				infoPannelli += (ISASUtil.valida(infoPannelli) ? ", " : "")+pannello;
			}
			
			if (ISASUtil.valida(infoPannelli)) {
				String messaggioDaMostrare = "";
				String[] dati = new String[]{Labels.getLabel("RichiestaMMG.principale.intensita_ass"), infoPannelli};
				messaggioDaMostrare = Labels.getLabel("segreteria.organizzativa.scheda.messaggio.rimossi", dati);
				
				Messagebox.show(messaggioDaMostrare,
						Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception{
						if (Messagebox.ON_NO.equals(event.getName())) {
							ripristinaTipoCura();
						}
					}
				});
			} else {
				logger.trace(punto + " non ci sono record presenti ");
			}
		}else {
			logger.trace(punto + " Non effettuo il controllo se dati presenti ");
		}
	}
	
	private boolean isOperatoriCoinvolti() {
		String punto = ver + "isOperatoriCoinvolti ";
		boolean recordPresenti = false;
		try {
			Component soOperatoriCoinvolti = self.getFellowIfAny("SOOperatoriCoinvolti", true);
			if (soOperatoriCoinvolti != null) {
				CaribelListbox grigliaOpCoinvolti = (CaribelListbox) soOperatoriCoinvolti.getFellowIfAny(
						"griglia_op_coinvolti", true);
				recordPresenti = (grigliaOpCoinvolti!=null && grigliaOpCoinvolti.getItemCount()>0);
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare i dati>>" ,e);
		}
		return recordPresenti;
	}

	protected void ripristinaTipoCura() {
		String punto = ver + "ripristinaTipoCura ";
		String tipoCura = ISASUtil.getValoreStringa(this.currentIsasRecord, Costanti.CTS_TIPOCURA);
		logger.trace(punto + " tipoCura>>"+tipoCura+"<");
		if (ISASUtil.valida(tipoCura) && !tipoCura.equals(tipocura.getSelectedValue())){
			tipocura.setSelectedValue(tipoCura+"");
			logger.trace(punto + " ripristino il tipo di cura >>" + tipoCura);
			Events.sendEvent(Events.ON_CHANGE, tipocura, null);
		}
	}

	private String recuperaDescrizione(boolean datiPresenti, String label) {
		String descrizione = "";
		if (datiPresenti){
			
			descrizione = Labels.getLabel(label);
		}
		return descrizione;
	}

	private boolean isPaiEsitoResidenziale(int tipoCuraOld) {
		String punto = ver + "isPaiEsitoResidenziale ";
		boolean esitoResidenziale = false;
		try { 
			if (tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI)) {
				Combobox cbxTipoist = (Combobox)self.getFellowIfAny("SOPreferenzeStruttura").getFellow("cbxTipoist");
				Combobox cbxModulo = (Combobox)self.getFellowIfAny("SOPreferenzeStruttura").getFellow("cbxModulo");
				
				esitoResidenziale = (ISASUtil.valida(cbxTipoist.getSelectedItem().getValue().toString())||
						ISASUtil.valida(cbxModulo.getSelectedItem().getValue().toString()));
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare i dati>>", e);
		}
		logger.debug(punto + " Ho inserito obiettivi per tipo cura>>" + tipoCuraOld + " esitoResidenziale>" + esitoResidenziale
				+ "<");

		return esitoResidenziale;
	}  

	private boolean isPaiInseriti(int tipoCuraOld) {
		String punto = ver + "isPaiInseriti ";
		boolean paiInserito = false;
		try { 
			if (tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)
					|| tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI)) {
				if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)){
					logger.trace(punto + " Solo in questo caso effettuo la verifica del pai ");
					paiInserito = ((clbobiettivi) != null && clbobiettivi.getItemCount() > 0);
				}
			} else if (tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_PRESTAZIONALI)) {
				paiInserito = ((listaPrestazioniCP) != null && listaPrestazioniCP.getItemCount() > 0);
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare i dati>>", e);
		}
		logger.debug(punto + " Ho inserito obiettivi per tipo cura>>" + tipoCuraOld + " paiInserito>" + paiInserito
				+ "<");
		return paiInserito;
	}

	private boolean isPaiObiettiviInseriti(int tipoCuraOld) {
		String punto = ver + "isPaiObiettiviInseriti ";
		boolean paiObiettiviInseriti = false;
		try {
			if (tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)||
				tipoCuraOld == ISASUtil.getValoreIntero(Costanti.CTS_COD_CURE_RESIDENZIALI)) {
				paiObiettiviInseriti = ((clbobiettivi)!=null && clbobiettivi.getItemCount()>0);
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare i dati>>" ,e);
		}
		
		logger.debug(punto + " Ho inserito obiettivi per tipo cura>>" + tipoCuraOld+" paiObiettivi>" + paiObiettiviInseriti+ "<");
		return paiObiettiviInseriti;
	}
	
	
	public boolean isComponentiFiguraCoinvolteMMGPls() {
		String punto = ver + "isComponentiFiguraCoinvolteMMGPls ";
		boolean medicoPresente = false;

		try {
			Component soOperatoriCoinvolti = self.getFellowIfAny("SOOperatoriCoinvolti", true);
			if (soOperatoriCoinvolti != null) {
				CaribelListbox grigliaOpCoinvolti = (CaribelListbox) soOperatoriCoinvolti.getFellowIfAny(
						"griglia_op_coinvolti", true);
				Hashtable dati;
				int k = 0;
				while (k < grigliaOpCoinvolti.getItemCount() && !medicoPresente) {
					dati = new Hashtable();
					Object itemGrid = grigliaOpCoinvolti.getModel().getElementAt(k);
					if (itemGrid instanceof ISASRecord) {
						dati = ((ISASRecord) itemGrid).getHashtable();
					} else if (itemGrid instanceof Hashtable) {
						dati = (Hashtable<String, Object>) itemGrid;
					}
					String tipoOperatore = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_SO_TIPO_OPERATORE);
					medicoPresente = (ISASUtil.valida(tipoOperatore) && (tipoOperatore.trim().equals(
							GestTpOp.CTS_COD_MMG) || tipoOperatore.trim().equals(GestTpOp.CTS_COD_MEDICO)));
					k++;
				}
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel verificare la presenza degli operatore ", e);
		}
		logger.trace(punto + " medico presente " + medicoPresente + "< ");
		return medicoPresente;
	}
	
	@Override
	public void attivareVerbaleUvi() {
		String punto = ver + "attivareVerbaleUvi ";
		attivareVerbaleUvi = true;
		
//		if (tipocura.getSelectedItem() != null && 
//				tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
		if ( (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self) || quadroSanitarioMMGCtrl.isCureResidenziali(this.self))
			  &&  isIdSksoPresente()) {  
			logger.debug(punto + " non sono in insert");
			attivareVerbaleUvi = false;
		}
		logger.trace(punto + " attivare>>"+attivareVerbaleUvi+"<");
		
//		dati_scheda_verbale_uvm.getLinkedTab().setDisabled(attivareVerbaleUvi);
		dati_scheda_verbale_uvm.getLinkedTab().setVisible((!attivareVerbaleUvi));
		
		boolean attivarePai = verificaSeAttivarePai();
		boolean disattivareCureResidenziali = false ;
		if (quadroSanitarioMMGCtrl.isCureResidenziali(this.self) && this.isIdSksoPresente() && attivarePai){
			disattivareCureResidenziali = true;
		}
		/* attivo sempre */
		dati_scheda_pai_obiettivi.getLinkedTab().setVisible(attivarePai);
		dati_scheda_pai.getLinkedTab().setVisible(attivarePai);
		impostaLivelloAttivazione();
		gestioneBottoneCongela(); 
//		gestioneBottoneCongela(attivarePai || disattivareCureResidenziali);
		gestioneBottoneCongela();
		preferenze_strutture.getLinkedTab().setVisible(disattivareCureResidenziali);

		boolean attivareIngressoStruttura = !(quadroSanitarioMMGCtrl.isCureResidenziali(self) && ManagerDate.validaData(data_presa_carico_skso));
		ingresso_struttura.getLinkedTab().setDisabled(attivareIngressoStruttura);
		ingresso_struttura.getLinkedTab().setVisible(!attivareIngressoStruttura);
		abilitazionePreferenzeStrutture();
	}

    @Override
	public void doInitForm() {
		String punto = ver + this.getClass().getName() + ".doInitForm \n";
		logger.debug(punto + "inizio ");

		try {
			settaController();
			inizioForm = true;
			 messaggioIsPatologieInserite = false;
			/* disabilito il CASE MANAGER 
			case_manager_mmg.setVisible(false);
			case_manager_mmg_pai.setVisible(false);
			 * */
			String fonte = getParameters(CostantiSinssntW.CTS_FONTE);
			idRichiesta = getParameters(CostantiSinssntW.CTS_ID_RICHIESTA);
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQueryKey("selectSkValCorrente");
			hParameters.put(CostantiSinssntW.CTS_SONO_IN_SO, CostantiSinssntW.CTS_SI);
			hParameters.put(CostantiSinssntW.CTS_FONTE, fonte);
			hParameters.put(CostantiSinssntW.CTS_ID_RICHIESTA, idRichiesta);
			
			Object dtCartellaChiusa =  UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
			if (ManagerDate.validaData(dtCartellaChiusa+"")){
				hParameters.put(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA, dtCartellaChiusa);
			}
			
			doMakeControl();
			doPopulateCombobox();
			//aggiungiBottoni();
//			doCaricaComboDistretti();
			
			SOPresentiUvi = self.getFellowIfAny("SOPresentiUvi", true);
			
			CaribelSearchCtrl medicoSearch = (CaribelSearchCtrl) medicoReferenteSearch.getAttribute(MY_CTRL_KEY);
			medicoSearch.putLinkedComponent("metel_amb", metel_amb);
			medicoSearch.putLinkedComponent("metel_cell", metel_cell);
			
			CaribelSearchCtrl operatorePrimaVisita = (CaribelSearchCtrl) so_cod_op_prima_visita.getAttribute(MY_CTRL_KEY);
			operatorePrimaVisita.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, cbx_pv_tp_operatore);
			operatorePrimaVisita.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
			
			CaribelSearchCtrl caseManagerSearch= (CaribelSearchCtrl) searchCaseManager.getAttribute(MY_CTRL_KEY);
			caseManagerSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
			caseManagerSearch.putLinkedComponent("telefono1", telefono1);
			
			
			pr_data_verbale_uvm.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
//					settaDataPresaCarico();
					settaCampiDataObligatoriPresaData();
					return;
				}});
			
			data_presa_carico_skso.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if (controlloCoerenzaSchedaAttivazione()){
						settaCampiDataObligatoriPresaData();
					}else {
						logger.debug(" data presa carico ok");
					}
					return;
				}
			});

			
			accessi_mmg.addEventListener(Events.ON_CHANGE,new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					verificaAccessiMMGPls();
					return;
				}
			});
			
			pr_data_chiusura.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					controllaCoerenzaDtConclusione();
				}});
			
			/* disabilito il CASE MANAGER
			case_manager_mmg_pai.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					attivareSearchManager(true);
					return;
				}});
			 */
			
			
			croniche_avanzate.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					settaTipoUtenza(cbx_utenza, croniche_avanzate);
					return;
				}});
			
			logger.trace(punto + " dati disponibili>>" + (arg != null ? arg.toString() : "no dati ") + "<<");
			String nCartella = ISASUtil.getValoreStringa(arg, CostantiSinssntW.N_CARTELLA);
			String idSkSo = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_ID_SKSO);
			String idRich = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_ID_RICH);
			
//			data_fine.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
//				public void onEvent(Event event) throws Exception {
////					notEditable();
//					return;
//				}});

			data_fine.addEventListener(Events.ON_BLUR, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
//					notEditable();
					aggiungiMesi(data_inizio, data_fine);
//					aggiungiFineAnno();
					return;
				}});
			
			pr_data_puac.addEventListener(Events.ON_BLUR, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					inserisciDataInizioPiano();
					return;
				}

				});
			
			data_inizio.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
//					notEditable();
					aggiungiMesi(data_inizio, data_fine);
					try {
						doCaricaPreferenzeIstituti(false);
					} catch (Exception e) {
						logger.error(" Imposta le date  ");
					}
//					aggiungiFineAnno();
				}
				});			
			
			data_inizio.addEventListener(Events.ON_BLUR, new EventListener<Event>(){
				public void onEvent(Event event){
//					notEditable();
					aggiungiMesi(data_inizio, data_fine);
					try {
						doCaricaPreferenzeIstituti(false);
					} catch (Exception e) {
						logger.error(" Imposta le date  ");
					}
//					aggiungiFineAnno();
				}
				});
			
			
			cod_med.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>(){
				public void onEvent(Event event){
					// abilitaScritturaMedicoAltro();
				}
				});			
			
			if(caribellb2 == null){
				caribellb2 = (CaribelListbox) SOPai.getFellowIfAny("caribellb2", true);
			}
			
			if(listaPrestazioniCP == null){
				listaPrestazioniCP = (CaribelListbox) SOCP.getFellowIfAny("caribellb2", true);
			}
			
			String action= ISASUtil.getValoreStringa(arg, CostantiSinssntW.ACTION);
			if(ISASUtil.valida(action) && action.equals(CostantiSinssntW.CONSULTA_RICHIESTA+"")){
				logger.trace(punto + " setto lo stato di consultazione della maschera ");
				soloConsultazione = true;
			}
			
			if (!ISASUtil.valida(idRich)){
			 idRich = getParameters(CostantiSinssntW.CTS_ID_RICH);
			}
			if(ISASUtil.valida(idRich)){
				this.hParameters.put(CostantiSinssntW.CTS_ID_RICH, idRich);
				id_rich.setValue(new Integer(idRich));
			}
			
			if (ISASUtil.valida(nCartella) && 
					(ISASUtil.valida(idSkSo) || ManagerDate.validaData(dtCartellaChiusa+"")) ) {
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
				hParameters.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
				
				doQueryKeySuEJB();
				
				doWriteBeanToComponents();
			} else if (dbrFromList != null) {
				//sono stato invocato da una griglia con il contatto (es:storico contatti)
				nCartella =ISASUtil.getValoreStringa(dbrFromList, CostantiSinssntW.N_CARTELLA);
				idSkSo = ISASUtil.getValoreStringa(dbrFromList, CostantiSinssntW.CTS_ID_SKSO);
				idRich = ISASUtil.getValoreStringa(dbrFromList, CostantiSinssntW.CTS_ID_RICH);
				super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
				hParameters.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
				hParameters.put(CostantiSinssntW.CTS_ID_RICH, idRich);
				
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			} else if (super.caribelContainerCtrl != null && super.caribelContainerCtrl.hashChiaveValore != null
					&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA) != null) {
				//arrivo da nuovo contatto o da qualunque altro posto
				String cart = (String) hParameters.get(CostantiSinssntW.N_CARTELLA);
				if (cart == null) {
					//se non ho il codice cartella lo prendo dal container.
					nCartella = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.N_CARTELLA);
//					idSkSo = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.CTS_ID_SKSO);
//					idRich = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.CTS_ID_RICH);
					Object objSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
					this.hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella.toString());
					if (objSkSo!=null){
						this.hParameters.put(CostantiSinssntW.CTS_ID_SKSO, objSkSo+"");
						idSkSo = objSkSo+"";
					}
				} 
   
				if (ISASUtil.valida(nCartella) ){//&& (ISASUtil.valida(idSkSo) || ISASUtil.valida(idRich))) {
					doQueryKeySuEJB();
					doWriteBeanToComponents();
				} else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(idSkSo)) {
					this.n_cartella.setText(nCartella + "");
//					this.richMMG_n_cartella.setText(nCartella+"");
					if(ISASUtil.valida(idRich)){
						this.id_rich.setText(idRich + "");
//						this.richMMG_id_rich.setText(idRich+"");   
					}   
				}
			}
			
			if (ISASUtil.valida(fonte) && ISASUtil.getValoreIntero(fonte) == CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA){
				logger.trace(punto + " Imposto idSchedaPht2>>"+ idRichiesta);
				id_scheda_pht2.setValue(idRichiesta);
				Events.sendEvent(Events.ON_CHANGE, data_inizio, null);
			}else  if (ISASUtil.valida(fonte) && ISASUtil.getValoreIntero(fonte) == CostantiSinssntW.CTS_TIPO_FONTE_PUA){
				logger.trace(punto + " Imposto idProgressivoPua>>"+ idRichiesta);
				id_progressivo_pua.setValue(idRichiesta);
				Events.sendEvent(Events.ON_CHANGE, data_inizio, null);
			}
			
			if (pr_data_chiusura!=null && caribelContainerCtrl !=null)
			super.caribelContainerCtrl.hashChiaveValore.put(CostantiSinssntW.PR_DATA_CHIUSURA,pr_data_chiusura.getValueForIsas());
			
//			recuperaOggetti();
			aggiungiBottoni();
			abilitazioneMaschera();
//			if (panel_scale != null){ 
//				panel_scale.detach();
//			}
			notEditable();
			if(griglia==null){
				griglia = (CaribelListbox) SOPresentiUvi.getFellowIfAny("griglia_uvi", true);
			}
			
			if (griglia_op_coinvolti == null){
				griglia_op_coinvolti = (CaribelListbox)SOOperatoriCoinvolti.getFellowIfAny("griglia_op_coinvolti", true);
			}
			
			if (lista_istituti_preferenze == null){
				lista_istituti_preferenze = (CaribelListbox)SOPreferenzeStruttura.getFellowIfAny("lista_istituti_preferenze", true);
			}
			
//			if(btn_aggiungi== null){
//				btn_aggiungi = (Button) SOPai.getFellowIfAny("btn_aggiungi", true);
//			}
			
			settaDatiOperatore();
			gestisciRichiestaMMG(false);
//			gestisciMMGPLS();
			inizioForm = false;
			settaValoreComboCommissione();
			abilitaComboCommissione();
			settaValoreFrequenza();
			
			doCaricaComboDistrettiO();

			//serve  per impostare correttamente la combo del distretto 
			doCaricaComboDistretti();
			abilitazionePreferenzeStrutture();
			doFreezeForm();
//			gestionePannelloScale();
			//impostaStatoCommissioneUVI(CaribelGridStateCtrl.STATO_WAIT);
			
			if (this.mascheraReadOnly|| soloConsultazione){
				settaGrigliaUvi(false);
				this.setReadOnly(true);
			}
			abilitaPulsantiScala();
		}
		catch (Exception ex){ 
//			logger.info(punto  + "\n\n BOFFA commento in quanto schianta \n\n ");
			String msg = Labels.getLabel(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI);
			if ( (ex instanceof InvocationTargetException) &&
				((InvocationTargetException) ex).getTargetException().getMessage().equals(msg) && !arg.containsKey(CostantiSinssntW.ACTION)){
			gestioneRichiestaChiusura();
			}
			else{
			doShowException(ex);
			}
//			doCaricaComboDistrettiO();
			try{
				this.setReadOnly(true);
			}catch (Exception ex2) {
				doShowException(ex2);
			}
		}
	}
	
	@Override
	public void settaController() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		rimuoviComponente("SOPresentiUvi");
		map.put("caribelContainerCtrl", this);
		map.put(CTRL_SO_PRESENTI_UVI, "it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SOPresentiUviCtrl");
		Component corrComp = Executions.createComponents(
				"~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_presenti_uvi.zul",
				self.getFellowIfAny("dati_presenti_uvi_h"), map);
		SOPresentiUvi = corrComp.getFellowIfAny("SOPresentiUvi");

		rimuoviComponente("SOOperatoriCoinvolti");
		map.put("caribelContainerCtrl", this);
		map.put(CTRL_SO_OP_COINVOLTI,
				"it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SOOperatoreCoinvoltiCtrl");
		corrComp = Executions.createComponents(
				"~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_operatori_coinvolti.zul",
				self.getFellowIfAny("dati_scheda_operatori_coinvolti"), map);
		SOOperatoriCoinvolti = corrComp.getFellowIfAny("SOOperatoriCoinvolti");

		rimuoviComponente("SOProroghe");
		map = new HashMap<String, Object>();
		map.put("caribelContainerCtrl", this);
		map.put(CTRL_SO_PROROGHE, "it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SOProrogheCtrl");
		Executions.createComponents(
				"~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_Proroghe.zul",
				self.getFellowIfAny("dati_scheda_so_proroghe"), map);

		rimuoviComponente("SOSospensione");
		map = new HashMap<String, Object>();
		map.put("caribelContainerCtrl", this);
		map.put(CTRL_SO_SOSPENSIONI, "it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SOSospensioneCtrl");
		Executions.createComponents(
				"~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_Sospensione.zul",
				self.getFellowIfAny("dati_scheda_sospensione"), map);
		
		rimuoviComponente("elencoBisogni");
		rimuoviComponente("isPianoCongelato");
		rimuoviComponente("btn_cambiaPiano");
		rimuoviComponente("btn_calcola");
		rimuoviComponente("SOPai");
		rimuoviComponente("SOCP");
		
		map = new HashMap<String, Object>();
		map.put("caribelContainerCtrl", this);
		map.put(CTRL_SO_PAI_PRESTAZIONI, "it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.PrestazioniPAICaribelGridFormCtrl");
		Component corrCompPiano = Executions.createComponents(
				"~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_pai.zul",
				self.getFellowIfAny("dati_scheda_pai"), map);
		SOPai = corrCompPiano.getFellowIfAny("SOPai");
		
		
		map = new HashMap<String, Object>();
		map.put("caribelContainerCtrl", this);
		map.put(CTRL_SO_PAI_PRESTAZIONI, "it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.PrestazioniPAICaribelGridFormCtrl");
		Component corrCompPianoCP = Executions.createComponents(
				"~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_curePrestazionali.zul",
				self.getFellowIfAny("dati_cure_prestazionali"), map);
		SOCP = corrCompPianoCP.getFellowIfAny("SOCP");
		
		recuperaOggetti();
	}

	private void rimuoviComponente(String nomeComponente) {
		String punto = ver + "rimuoviComponente ";
		Component componente = self.getFellowIfAny(nomeComponente);
		if (componente != null) {
			logger.trace(punto + " rimuovo oggetto>" +nomeComponente);
			componente.detach();
		}
	}
	
	@Override
	public void aggiungiBottoni() {
		String punto = ver + "aggiungiBottoni ";
		logger.debug(punto + " inizio ");
		try {
			resettaToolBar();
			aggiungiBottoneSO();
			aggiuntiBottoneStampaPai();
			aggiungiBottonStampaPHT2();
			aggiungiBottoneCongela();
			aggiungiBottoneCambioPiano();
			aggiungiBottonTrasmissioneDistretto();
		} catch (Exception x) {
			x.printStackTrace();
			logger.trace(punto + " Errore nell'inserire il menu ", x);
		}
	}

	private void resettaToolBar() {
		String punto = ver + "resettaToolBar ";
		if (btn_printDatiSo!= null){
			logger.trace(punto + " invalido la toolbar ");
			btn_printDatiSo.invalidate();
		}
		btn_printDatiSo = new Toolbarbutton();		
	}

	private void aggiungiBottoneCongela() {
		String punto = ver + "aggiungiBottoneCongela ";
		String idBottone = "btn_congela";
		if (!insertMode()) {
			btn_congela = (Button) this.self.getFellowIfAny(idBottone);
			if (btn_congela == null) {
				btn_congela = new Button();
				btn_congela.setId(idBottone);
				// btn_congela.setId("btn_congela");
				btn_congela.setStyle("color: #0983C5");
				btn_congela.setImage("/web/img/attivarePiano.png");
				btn_congela.setLabel(Labels.getLabel("segreteria.organizzativa.scheda.pai.btn.congelaPiano"));
				btn_printDatiSo.getParent().insertBefore(btn_congela, btn_printDatiSo);
				btn_congela.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					public void onEvent(Event event) {
						try {
							onCongelaPiano(null);
						} catch (Exception e) {
							doShowException(e);
						}
					}
				});
			} else {
				logger.trace(punto + " bottone gia' " + idBottone + " presente!!!");
			}
		}
		boolean attivarePai = verificaSeAttivarePai();
//		gestioneBottoneCongela(isPianoCongelato.isChecked() && attivarePai);
		gestioneBottoneCongela();
	}
	
	protected void doPrintDatiSo(Event event) {
		String punto = ver + "doPrintDatiSo ";
		try {

			String ejb = "SINS_FOSO";
			String metodo = "stampaSo";
			String report = "dettaglio_scheda_so";
			Hashtable<String, Object> parametri = new Hashtable<String, Object>();
			parametri.put(Costanti.N_CARTELLA, n_cartella.getText());
			parametri.put(Costanti.CTS_ID_SKSO, id_skso.getText());
			parametri.put(CostantiSinssntW.CTS_TIPOCURA, tipocura.getSelectedValue() + "");

			Component tabPanelScale = self.getFellow("tabpanel_scale");
			ScalePanelCtr tabPanelScaleCtrl = (ScalePanelCtr) (tabPanelScale.getFirstChild().getAttribute(MY_CTRL_KEY));
			Hashtable<String, String> hParScala = tabPanelScaleCtrl.getDatiFromScale();
			logger.trace(punto + " dati recuperati>" + hParScala);
			parametri.putAll(hParScala);
			DatiStampaRichiesti datiStampaRichiesti = new DatiStampaRichiesti(metodo, report, ejb, parametri, true);
			datiStampaRichiesti.setTitoloMaschera(((Window) self).getTitle());
			datiStampaRichiesti.setFormatoStampaPdf(true);
			datiStampaRichiesti.setFormatoStampaFoglioCalcolo(false);
			
			Hashtable<String, Object> dati = new Hashtable<String, Object>();
			dati.put(CostantiSinssntW.CTS_STAMPA_BEAN, datiStampaRichiesti);

			logger.trace(punto + " dati >>" + parametri + "<<");
			Executions.getCurrent().createComponents(DatiStampaCtrl.CTS_FILE_ZUL, self, dati);

		} catch (Exception ex) {
			doShowException(ex);
		}
	}

	protected void verificaAccessiMMGPls() {
		String punto = ver + "verificaAccessiMMGPls ";
		int numeroAccessi= -1;
		try {
			numeroAccessi = accessi_mmg.getValue().intValue();
			if (numeroAccessi == 0 && !insertMode()) {
				int numAccessiIndb = ISASUtil.getValoreIntero(this.currentIsasRecord, CostantiSinssntW.CTS_ACCESSI_MMG);
				if (numAccessiIndb > 0) {
					if (isComponentiFiguraCoinvolteMMGPls()) {
						String messaggio = Labels.getLabel("menu.segreteria.so.operatori.coinvolti.rimossi");
						Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
								Messagebox.EXCLAMATION);
					} else {
						logger.trace(punto + "Non sono presenti MMG o medici ");
					}
				}
			}
		} catch (Exception e) {
			logger.trace(punto + " Errore in recupero accessi", e);
		}
		logger.trace(punto + "numero accessi recuperato>>" + numeroAccessi + "<<");
	}

	private void gestioneRichiestaChiusura() {
		try{
			this.setReadOnly(true);
			ISASRecord dbr = (ISASRecord)invokeGenericSuEJB(myEJB, hParameters, "selectZonaSkValCorrente");
			if (dbr!=null){   
				final String zona_cod = ISASUtil.getValoreStringa(dbr, "zona_cod");
				final String codDistretto = ISASUtil.getValoreStringa(dbr, Costanti.CTS_L_ASSISTITO_COD_DISTRETTO);
				final String id_skso = ISASUtil.getValoreStringa(dbr, Costanti.CTS_ID_SKSO);
				
			String zona = dbr.get("zona_desc").toString();
			Messagebox.show(Labels.getLabel("so.conferma.richiesta.chiusura",new String[]{zona}), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())){
								String msg = inviaRichiestaChiusura(zona_cod,id_skso, codDistretto);
								Clients.showNotification(msg.equals("")?Labels.getLabel("so.richiesta.chiusura.successo"):msg);									
							}
							if (Messagebox.ON_NO.equals(event.getName())){
								return;			
							}						
						}							
					});
				}
			}catch (Exception ex2) {
				doShowException(ex2);
			}
	}

	protected String inviaRichiestaChiusura(String zona_cod, String id_skso, String codDistretto) {
		String ret = "";
		Hashtable<String,String> h = (Hashtable)hParameters.clone();
		try{
			h.put("esito_richiesta", CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA);
			h.put("cod_zona_richiedente", ManagerProfile.getZonaOperatore(getProfile()));
			h.put("cod_zona_presacarico", zona_cod);
			h.put(CostantiSinssntW.CTS_COD_DISTRETTO_PRESA_CARICO, codDistretto);
			h.put("cod_operatore_richiedente",getProfile().getIsasUser().getKUser());
			h.put("data_richiesta", UtilForBinding.getValueForIsas(new Date()));
			h.put("id_skso",id_skso);
			boolean res = ((Boolean)invokeGenericSuEJB(myEJB, h, "insertRichiestaChiusura")).booleanValue();
			if (res) return ret; else return Labels.getLabel("so.richiesta.chiusura.errore");
		}catch (InvocationTargetException e){
			if (((InvocationTargetException) e).getTargetException() instanceof CariException)
				ret = ((InvocationTargetException) e).getTargetException().getMessage();
		}catch (Exception e){
			e.printStackTrace();
			ret = Labels.getLabel("so.richiesta.chiusura.errore");
		}
		return ret;
	}

	@Override
	public void gestisciMMGPLS() throws Exception {
		String punto = ver + "gestisciMMGPLS ";
		logger.trace(punto + " inizio potrebbe essere cambiata ");
		
		try {
			if (tipocura.getSelectedItem() != null && 
					(accessi_mmg !=null && accessi_mmg.getValue()>0)  
					&& !soloConsultazione ){
//				if (tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
				if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
					if (mmgpls_op_coinvolti_pr !=null && ISASUtil.valida(mmgpls_op_coinvolti_pr.getValue())
								&& (!mmgpls_op_coinvolti_pr.getValue().equals(CostantiSinssntW.CTS_SI))
								&& (cod_med!=null && ISASUtil.valida(cod_med.getText()))
						&& (vista_da_so !=null && ISASUtil.valida(vista_da_so.getValue())  
						&& vista_da_so.getValue().equals(CostantiSinssntW.CTS_FLAG_STATO_VISTA))	){
						inserisciMMGPLS();
					}
				}else if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)) {
					if (mmgpls_op_coinvolti_pr !=null && ISASUtil.valida(mmgpls_op_coinvolti_pr.getValue())
								&& (!mmgpls_op_coinvolti_pr.getValue().equals(CostantiSinssntW.CTS_SI))
								&& (cod_med!=null && ISASUtil.valida(cod_med.getText()))){
						inserisciMMGPLS();
					}
				}else if (quadroSanitarioMMGCtrl.isCureResidenziali(this.self)){
					logger.trace(punto + " CASO RESIDENZIALE ");
				}
			}
		} catch (Exception e) {
			logger.trace(punto + " Errore nel recuperare i dati MMG");
		}
	}
	
	private void inserisciMMGPLS() {
		String punto = ver + "inserisciMMGPLS ";
		String nCartella = ISASUtil.getValoreStringa(this.hParameters, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(this.hParameters, CostantiSinssntW.CTS_ID_SKSO);
		
		if ( ISASUtil.valida(nCartella)&& ISASUtil.valida(idSkso)) {
			RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, nCartella + "");
			dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso + "");
			dati.put(CostantiSinssntW.CTS_OP_INSERIRE_MMG, CostantiSinssntW.CTS_SI);
			dati.put(CostantiSinssntW.DT_PRESA_CARICO, CostantiSinssntW.CTS_SI);
			Vector<ISASRecord> griglia = new Vector<ISASRecord>();
			try {
				griglia = rmSkSOOpCoinvoltiEJB.inserisciMMGPLSOperatorePrimaVisita(CaribelSessionManager.getInstance().getMyLogin(), dati);
			} catch (DBRecordChangedException e) {
				e.printStackTrace();
			} catch (ISASPermissionDeniedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			griglia_op_coinvolti.getItems().clear();
			griglia_op_coinvolti.setModel(new CaribelListModel<ISASRecord>(griglia));
//			settaSaveMsgMMGPLS(RISPOSTACHIUSURACONTATTO_NO);
		}
	}

	public static void aggiungiMesi(CaribelDatebox dtInizio, CaribelDatebox dtFine){
		String punto = ver + "aggiungiMesi ";
//		logger.trace(punto + "Inizio>>");
		if(!ManagerDate.validaData(dtFine) && ManagerDate.validaData(dtInizio)){
			String dataInizio = dtInizio.getValueForIsas();
			dateutility dtDateutility = new dateutility();
			Date vdtFine = dtDateutility.getDataNMesi(CTS_SO_MESI_DT_PERIODO_PIANO, dataInizio);
//			logger.trace(punto + " data>>" +vdtFine);
			dtFine.setValue(vdtFine);
		}
	}
	
	private void inserisciDataInizioPiano() {
		String punto = ver + "inserisciDataInizioPiano ";
		logger.trace(punto + "Inizio ");
		if(ManagerDate.validaData(pr_data_puac) && !ManagerDate.validaData(data_inizio)){
			data_inizio.setValue(pr_data_puac.getValue());
			Events.sendEvent(Events.ON_BLUR, data_inizio, null);
		}
	}
	
	private void aggiungiFineAnno_(){
		String punto = ver + "aggiungiFineAnno ";
		logger.trace(punto + "Inizio>>");
		if(!ManagerDate.validaData(data_fine) && ManagerDate.validaData(data_inizio)){
			String dataInizio = data_inizio.getValueForIsas();
			dateutility dtDateutility = new dateutility();
			Date dtFine = dtDateutility.getFineAnno(dataInizio);
			logger.trace(punto + " data>>" +dtFine);
			data_fine.setValue(dtFine);
		}
	}
	
	private void settaValoreComboCommissione() {
		String punto = ver + "settaValoreComboCommissione ";
		if (cbx_commissioni!=null){
			codCommUviOld = cbx_commissioni.getValue();
		}
		logger.trace(punto + " valore vecchio>>"+ codCommUviOld+"<");
	}

	private void gestisciRichiestaMMG(boolean afterInsert) throws Exception {
		String idRich = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_RICH);
		String fonte =  getParameters(CostantiSinssntW.CTS_FONTE);
		
		if (fonte!=null && fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG)
				&& idRich!=null && this.pr_data_chiusura.getValue()==null)
		{
			Hashtable h = new Hashtable();
			h.put("id_rich",idRich);
			h.put("n_cartella",this.n_cartella.getText());
			
			
			final ISASRecord rich_mmg = (ISASRecord)invokeGenericSuEJB(new RmRichiesteMMGEJB(), h, "queryKey");
			//controllo che la richiesta non sia ancora effettivamente in attesa
			if (rich_mmg!=null && rich_mmg.get("stato")!=null && rich_mmg.get("stato").toString().equals(String.valueOf(CostantiSinssntW.STATO_ATTESA))){
			if (afterInsert){
				aggiornaRichiestaMMG(rich_mmg,RmRichiesteMMGEJB.STATO_RICH_MMG_ATTIVATA);
	        	Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_RICH);
	    		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
				
			}else if (!this.insertMode()){
				Messagebox.show(Labels.getLabel("so.conferma.archivia.prendicarico.msg"),Labels.getLabel("messagebox.attention"),
						   new Messagebox.Button[] {Messagebox.Button.YES, Messagebox.Button.NO, Messagebox.Button.CANCEL},
						       new String[]{Labels.getLabel("RichiestaMMG.principale.btn_conferma"),
					"Archivia","Annulla"},Messagebox.QUESTION, Messagebox.Button.CANCEL, new EventListener<Messagebox.ClickEvent>() { 
						           public void onEvent(Messagebox.ClickEvent event) throws Exception {
						        	   switch (event.getButton()) {
						                case YES: {
						                	aggiornaRichiestaMMG(rich_mmg,RmRichiesteMMGEJB.STATO_RICH_MMG_CONFERMA);
						                	Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_RICH);
						            		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
						            		break;
						                }
						                case NO:{
						                	aggiornaRichiestaMMG(rich_mmg,RmRichiesteMMGEJB.STATO_RICH_MMG_ARCHIVIATO);
						                	Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_ID_RICH);
						            		Executions.getCurrent().removeAttribute(CostantiSinssntW.CTS_FONTE);
						            		break;
						                }
						                default: break;
						                }
						           }
						       });
			}
		}
		}
		else return;
	}

	protected void aggiornaRichiestaMMG(ISASRecord rich_mmg, String modalita) throws Exception {
		rich_mmg.put("id_scheda_so",this.id_skso.getText());
		rich_mmg.put("stato",modalita);
		updateSuEJB(new RmRichiesteMMGEJB(), rich_mmg);
		
	}

	public void onChangeTipoCommissione(Event event) throws WrongValueException, Exception{
		String punto = ver +"onChangeTipoCommissione ";
		logger.debug(punto + "sbianco il codice operatore ");
//		boolean selezionataCommissione = tipoComboCommissione();
		impostaStatoCommissioneUVI(CaribelGridStateCtrl.STATO_WAIT);
		
			if (check_save_rimozione_componenti_uvi == RISPOSTACHIUSURACONTATTO_NO){
				if (!isRimuoviComponenti()){
					return;
				}
			}
			
			Hashtable<String, String> datiDaInserire = new Hashtable<String, String>();
			datiDaInserire.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
			datiDaInserire.put(CostantiSinssntW.CTS_ID_SKSO, id_skso.getValue()+"");
			datiDaInserire.put(CostantiSinssntW.CTS_COD_COMMISSIONE, cbx_commissioni.getSelectedValue());
			
			RMPuaUvmCommissioneEJB rmPuaUvmCommissioneEJB = new RMPuaUvmCommissioneEJB();
			boolean inserimentoOk = rmPuaUvmCommissioneEJB.inserisciComponentiCommissione(CaribelSessionManager.getInstance().getMyLogin(), datiDaInserire);
			if (inserimentoOk){
				logger.trace(punto + " dati inseriti correttamente: ricarico la griglia ");
//				Component presentiUvi = self.getFellowIfAny("SOPresentiUvi", true);
				if(SOPresentiUvi!=null){
					SOPresentiUviBaseCtrl presentiUviCtrl = (SOPresentiUviBaseCtrl) SOPresentiUvi.getAttribute(MY_CTRL_KEY);
					presentiUviCtrl.doPublicLoadGrid(n_cartella.getText(), id_skso.getText(), this.mascheraReadOnly);
				}
				impostaStatoCommissioneUVI(CaribelGridStateCtrl.STATO_WAIT);
				boolean riletturaRmSkso = false;  // Non aggiorno il codice della commissione 
				if (riletturaRmSkso){
				doQueryKeySuEJB();
				doWriteBeanToComponents();
				
				eseguiOperazioni();
				}
			}else {
				logger.trace(punto + "Errore nell'inserire i componenti NON ricarico la griglia ");
			}
			settaRimozioneComponenti(RISPOSTACHIUSURACONTATTO_NO);
			settaValoreComboCommissione();
	}
	
	protected void impostaStatoCommissioneUVI(String stato) {
		if(SOPresentiUvi!=null){ 
			SOPresentiUviBaseCtrl presentiUviBaseCtrl = (SOPresentiUviBaseCtrl) SOPresentiUvi.getAttribute(MY_CTRL_KEY);
			presentiUviBaseCtrl.impostaStato(stato);
		} 
	}

	private boolean isRimuoviComponenti() {
		String punto = ver + "isRimuoviComponenti ";
		boolean rimuoviComponenti = true;
		
		Component soPresentiUvi = self.getFellowIfAny("SOPresentiUvi", true);
		if(soPresentiUvi!=null){
	        griglia_uvi = (CaribelListbox) soPresentiUvi.getFellowIfAny("griglia_uvi", true);
			if (griglia_uvi != null && griglia_uvi.getItems().size()>0) {
				logger.trace(punto + " griglia ha dei dati  ");
				String messaggio = Labels.getLabel("so.conferma.chiusura.msg.rimuovi.componenti.uvi");
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"),
						Messagebox.YES + Messagebox.NO, Messagebox.QUESTION,
						new EventListener<Event>() {
							public void onEvent(Event event) throws Exception {
								if (Messagebox.ON_YES.equals(event.getName())) {
									settaRimozioneComponenti(RISPOSTACHIUSURACONTATTO);
									onChangeTipoCommissione(event);
								}else if(Messagebox.ON_NO.equals(event.getName())){
									logger.trace("Caso NO: Setto il codice commissione>>"+ codCommUviOld+"<<");
									cbx_commissioni.setValue(codCommUviOld);
									return;
								}
							}
						});
				return false;
			}else {
				logger.trace(punto + " non ci sono dati da cancellare ... proseguo!!!");
			}
		}else {
			logger.trace(punto + "Non ho recuperato la griglia !!!");
		}
		return rimuoviComponenti;
	}

	private void settaRimozioneComponenti(int risposta) {
		check_save_rimozione_componenti_uvi = risposta;
	}

	private boolean tipoComboCommissione() {
		boolean tipoOperatoreSelezionato = false;

		tipoOperatoreSelezionato = (cbx_commissioni != null
				&& cbx_commissioni.getSelectedIndex() > 0 && ISASUtil
				.valida(cbx_commissioni.getSelectedValue()));

		return tipoOperatoreSelezionato;
	}
	
	private void abilitaScritturaOperatoreCognomeNomeAltro() {
		String punto= ver + "";
		SOPresentiUviCtrl so = new SOPresentiUviCtrl();
		so.abilitaScritturaOperatoreCognomeNomeAltro();
	}


	@Override
	public void abilitazioneMaschera() throws WrongValueException, Exception {
		boolean insert = false;
		String punto = ver + "abilitazioneMaschera ";
		logger.trace(punto);
		
		verificaStatoCartella();
		
		if (insertMode()) {   
			insert = true;
			boolean proporreDtScheda = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_PROPORRE_DT_SCHEDA);
			if(proporreDtScheda){
				if (!ContattoInfFormCtrl.anagraficaChiusa(this.hParameters, pr_data_puac, null)){
					pr_data_puac.setValue(procdate.getDate());
				}
			}
		}
		
		btn_patologie.setDisabled(insert);
		btn_delete.setVisible(false);
		if (insert){
			logger.trace(punto +" cancellazione disabilitata  ");
			btn_delete.setVisible(false);
			settaTipoUtenza(cbx_utenza, croniche_avanzate);			
		}else {
			if (!ManagerDate.validaData(data_presa_carico_skso)){
				logger.trace(punto +" abilito la cancellazione ");
				btn_delete.setVisible(true);
			}else {
				logger.trace(punto +" esiste la data puac disabilita la cancellazione ");
			}
			pr_data_puac.setReadonly(true);
		}
		
		abilitareOperatoriCoinvolti();
//		dati_scheda_verbale_uvm.getLinkedTab().setDisabled(insert);
		dati_scheda_verbale_uvm.getLinkedTab().setVisible(!insert);
//		dati_scheda_so_proroghe.getLinkedTab().setDisabled(insert);
		dati_scheda_so_proroghe.getLinkedTab().setVisible(!insert);
//		dati_scheda_sospensione.getLinkedTab().setDisabled(insert);
		dati_scheda_sospensione.getLinkedTab().setVisible(!insert);
//		dati_scheda_conclusione.getLinkedTab().setDisabled(insert);
		dati_scheda_conclusione.getLinkedTab().setVisible(!insert);
		tabpanel_scale.getLinkedTab().setDisabled(insert);
		tabpanel_scale.getLinkedTab().setVisible(!insert);
//		dati_cure_prestazionali.getLinkedTab().setDisabled(insert);
		dati_cure_prestazionali.getLinkedTab().setVisible(!insert);
		
		logger.trace(ver + " sono in inserimento>>" + insert + "<<");

		boolean isRichiestaMMGPresente = isRichiestaMMG();
		logger.trace(punto + " richiesta mmg presente>>"+ isRichiestaMMGPresente+"<<");
		
		attivareVerbaleUvi();
		controlloMedici();
		// abilitaScritturaMedicoAltro();
		abilitaScritturaOperatoreCognomeNomeAltro();
		Events.sendEvent(Events.ON_CHECK, sitfam, null);
		Events.sendEvent(Events.ON_CHECK, flag_trasporto, null);
		abilitaOperatorePrimaVisita(false);
		abilitazionePrimaVisita();
		settaValoreComboCommissione();
		abilitaComboCommissione();
		abilitazioneProrogheSospensione();
		abilitaObligatorioOperatorePrimaVisita();
		gestisciMMGPLS();
		settaValoreFrequenza();

		gestioneSchedaPai();
		quadroSanitarioMMGCtrl.eseguiOperazioniIniziali(this.self, ManagerDate.validaData(data_presa_carico_skso));
//		verificaObiettivi();
//		abilitazioniPianoCongelato();
		verificaIsPatologieInserite();
		gestioneDataPresaCarico();
		onChangeDataRevisione();
		verificaDatiUvi();
		
		recuperaInfoScheda();
		resettaNumeroAccessi = false;
		onChangeIntensitaAssistenziale();
		resettaNumeroAccessi = true;
		settaValoreFrequenza();
//		doCaricaPreferenzeIstituti();
		
		impostaDatiArg();
		aggiungiBottonStampaPHT2();
		aggiungiBottonTrasmissioneDistretto();
		
		abilitazionePreferenzeStrutture();
		settaStatoScheda();
		if (isContattoChiuso()){
			logger.trace(ver + " sono in  statoConsultazione ");
			this.setReadOnly(true);
			soloConsultazione = true;
			this.mascheraReadOnly = true;
		}else {
			logger.trace(ver + "NON sono in Consultazione ");
		}
		abilitazioniPianoCongelato();
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
			Date dtChiusura = ManagerDate.getDate(dataChiusura);
			if (isInInsert()) {
				logger.trace(punto + " cartella chiusa>" + dataChiusura + "<");
				if (messaggioAvvisoCartellaChiusa) {
					String dtFIne = ManagerDate.formattaDataIta(dataChiusura, "/");
					String[] lables = new String[] { dtFIne };
					messaggio = Labels.getLabel("Cartella.stato.chiusa", lables);
					Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
							Messagebox.EXCLAMATION);
					messaggioAvvisoCartellaChiusa = false;
				}
				pr_data_puac.setConstraint("before " + UtilForComponents.formatDateforDatebox(dtChiusura));
			}
			pr_data_chiusura.setConstraint("before " + UtilForComponents.formatDateforDatebox(dtChiusura));
		}
	}

	@Override
	public void verificaIsPatologieInserite() {
		String punto = ver + "verificaIsPatologieInserite ";
		logger.trace(punto + " inizio ");
//		if(!quadroSanitarioMMGCtrl.isCurePrestazionali(self) && (!isInInsert())){
		if ((quadroSanitarioMMGCtrl.isCureDomiciliari(self) || quadroSanitarioMMGCtrl.isCureResidenziali(self))
				&& (!isInInsert())) {
			if(tablePrestazioni == null){
				tablePrestazioni = (CaribelListbox) self.getFellowIfAny("tablePrestazioni", true);
			}
			
			if ((tablePrestazioni !=null && tablePrestazioni.getItemCount()<=0 )){
				logger.trace(punto + "controllare presenza patologie ");
				String messaggio = Labels.getLabel("so.messaggio.patologie.mancanti");
				patologie.getLinkedTab().setSelected(true);
				if (!messaggioIsPatologieInserite){
					messaggioIsPatologieInserite = true;
					Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
				}
			}
		}else {
			logger.trace(punto + "tipo curo Prestazione: NON effettuo il controllo se le patologie sono state inserite ");
		}
	}

	public void onCalcolaPrestazioni(ForwardEvent e) throws Exception {
		if(data_inizio.getValue()==null||data_fine.getValue()==null){
			UtilForUI.standardYesOrNo(Labels.getLabel("segreteria.organizzativa.msg.dataInizioOFineAssente"),
					new EventListener<Event>(){
						public void onEvent(Event event) throws Exception{
							if (Messagebox.ON_YES.equals(event.getName())){	
								caricaBisogni(true);								
							}					
						}
					});
		}else{
			caricaBisogni(true);
		}
	}
	
	public void onEsitoValutazioneUvi(ForwardEvent e) throws Exception {
		String punto = ver + this.getClass().getName() + ".onEsitoValutazioneUvi ";
		logger.debug(punto + "inizio ");
		boolean possoProseguire = true;
		try {
			UtilForComponents.testRequiredFields(self);
		} catch (Exception e2) {
			possoProseguire = false;
			logger.trace(punto + " ci sono dei dati da salvare ");
		}
		if (!possoProseguire){
			logger.trace(punto + "ERRORE non posso proseguire ci sono dei dati obbligatori nella frame che vanno salvati ");
			return ;
		}
		logger.trace(punto + " esito valutazione ");
		if (id_skso.getValue()!=null){
			String id_sk = id_skso.getValue().toString();
			Hashtable<String, Object> dati = new Hashtable<String, Object>(); 
			dati.put(CostantiSinssntW.CTS_ID_SKSO, id_sk);
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
			dati.put("caribelContainerCtrl", super.caribelContainerCtrl);
			Component sofc = Executions.getCurrent().createComponents(EsitiValutazioniUviCtrl.myPathFormZul, self, dati);

			sofc.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
					public void onEvent(Event event) throws Exception{
						logger.trace(" Events.ON_CLOSE ");
						modificaDatiEsitoValutazione();
					}
				});
		}else {
			logger.trace(punto + " Non ho un idskso valido>>" + id_skso.getValue()+"<");
		}
	}
	
	protected void modificaDatiEsitoValutazione() throws ISASMisuseException {
		String punto = ver + "modificaDatiEsitoValutazione ";
		logger.trace(punto + " riallineoDati");
		Object esitoValutazioneRicaricaSo  = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_RICARICA_SO);
		boolean ricaricareSchedaSo = ISASUtil.getvaloreBoolean(esitoValutazioneRicaricaSo);
		
		if (ricaricareSchedaSo){
			logger.debug(punto + "Carico ID skso della nuova scheda di valutazione ");
			String nCartella = ISASUtil.getValoreStringa(currentIsasRecord,CostantiSinssntW.N_CARTELLA);
			Object idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_ID_SKSO_NEW);
			if (idSkso!=null) {
				logger.trace(punto + " Ricarico la scheda SO con cartella:" + nCartella+ "< idSksoNew>"+ idSkso+"<");
				ricaricaSO(nCartella,idSkso+"");
			}
		}else {
			logger.debug(punto + " carico la data dell'ultima valutazione ");
			Hashtable<String, Object> dati = new Hashtable<String, Object>();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
			dati.put(CostantiSinssntW.CTS_ID_SKSO, id_skso.getValue());
			EsitiValutazioniUviEJB esitiValutazioniUviEJB = new EsitiValutazioniUviEJB();
			ISASRecord dbrEsitiUltimaValutazioneUvi = esitiValutazioniUviEJB.recuperaUltimaValutazione(CaribelSessionManager.getInstance().getMyLogin(), dati);
			String dtUltimaValutazioneUvi = ISASUtil.getValoreStringa(dbrEsitiUltimaValutazioneUvi, CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE);
			dt_prossima_valutazione.setValue(ManagerDate.getDate(dtUltimaValutazioneUvi));
		}
		
	}

	
	@SuppressWarnings("unchecked")
//	@Override
	public void caricaPrestazioniCP(boolean caricaPrestazioni) throws Exception {
		if(id_skso.getValue()!=null){
			CaribelListModel modelPrestazioni = new CaribelListModel();
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			h.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
			h.put("soloBisogni", true);
			if(data_inizio.getValue()!=null){
				h.put("data_inizio", data_inizio.getValue());
			}else{
				h.put("data_inizio", pr_data_puac.getValue());
			}
			if(data_fine.getValue()!=null){
				h.put("data_fine", data_fine.getValue());
			}else{
				h.put("data_fine", new Date());
			}
			Vector<String> listaBisogni = new Vector<String>();
			if(adp.isChecked()){
				listaBisogni.add("CP_ADP");
			}
			if(ard.isChecked()){
				listaBisogni.add("CP_ARD");
			}
			if(aid.isChecked()){
				listaBisogni.add("CP_AID");
			}
			if(vsd.isChecked()){
				listaBisogni.add("CP_VSD");
			}
			
			if (caricaPrestazioni){
				h.put("listaBisogni", listaBisogni);
				h.put(CostantiSinssntW.CTS_TIPOCURA, tipocura.getSelectedValue());
				modelPrestazioni.addAll(bisogni.getPrestazioni(CaribelSessionManager.getInstance().getMyLogin(), h));
				modelPrestazioni.setMultiple(listaPrestazioniCP.isMultiple());
				listaPrestazioniCP.setModel(modelPrestazioni);
			}
			listaPrestazioniCP.setItemRenderer(new PAIGridItemRenderer());
			listaPrestazioniCP.invalidate(); 
		}
		listaPrestazioniCP.setDisabled(isPianoCongelato.isChecked());
		UtilForComponents.disableListBox(listaPrestazioniCP, isPianoCongelato.isChecked()|| !isSavable());
		UtilForBinding.setComponentReadOnly(SOCP, isPianoCongelato.isChecked()|| !isSavable() || isReadOnly());
		
		
		boolean attivarePai = false;
		if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)){
			attivarePai = true;
		}else {
			attivarePai = verificaSeAttivarePai();
		}
//		gestioneBottoneCongela(!(isPianoCongelato.isChecked()|| !isSavable() || isReadOnly()) && attivarePai);
		gestioneBottoneCongela();
	}
	
	public void onCongelaPiano(ForwardEvent e) throws Exception {
		try{
			String punto = ver + "onCongelaPiano ";
			logger.trace(punto + "onCongelaPiano ");
			boolean pianoCongelabile =  verificaSePianoCongelabile(); 
			if(pianoCongelabile){
				UtilForUI.standardYesOrNo(Labels.getLabel("segreteria.organizzativa.msg.congelaPiano"),
					new EventListener<Event>(){
						public void onEvent(Event event) throws Exception{
							if (Messagebox.ON_YES.equals(event.getName())){	
								doCongelaPiano();
								doArchiviazionePAISuFSE();
							}					
						}
				});
			}else {
				logger.trace(punto + " piano NON CONGELABILE ");
			}
		}catch(Exception ex){
			doShowException(ex);
		}
	}

	private String verificaScalaBisogni(String messaggio) {
		String punto = ver + "verificaScalaBisogni ";
		boolean ret = false;
		boolean nonObbligatorioScalaValutazioneBisogni = ManagerProfileBase.isAbilitazione(getProfile(),
				ManagerProfileBase.SO_NO_OBBL_SCALA_VAL_BISOGNI);

		if (nonObbligatorioScalaValutazioneBisogni) {
			logger.debug(punto + " Non obbligatorio la scala  ");
		} else {
			boolean ablInsDataValutazioneBisogni = ManagerProfile.isAbilitazione(getProfile(),
					ManagerProfile.ABL_INS_DATA_VALUTAZIONE_BISOGNI);

			try {
				Hashtable<String, Object> dati = new Hashtable<String, Object>();
				dati.putAll((Hashtable) hParameters.clone());
				dati.put(CostantiSinssntW.CTS_PR_DATA_VERBALE_UVM, pr_data_verbale_uvm.getValueForIsas());
				dati.put(ManagerProfile.ABL_INS_DATA_VALUTAZIONE_BISOGNI, new Boolean(ablInsDataValutazioneBisogni));
				ret = ((Boolean) invokeGenericSuEJB(new SCBisogniEJB(), dati, "getConvalida")).booleanValue();

			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!ablInsDataValutazioneBisogni) {
				if (!ret) {
					messaggio += (ISASUtil.valida(messaggio) ? ", " : "")
							+ Labels.getLabel("so.congela.pai.necessaria.convalida_uvi");
				}
			}
		}
		
		return messaggio;
	}
	
	private boolean verificaSePianoCongelabile() {
		String punto = ver + "verificaSePianoCongelabile ";
		logger.trace(punto + " inizio ");
		boolean pianoCongelabile = false;
		String messaggio = ""; 
		boolean casoCurePrestazionali = quadroSanitarioMMGCtrl.isCurePrestazionali(this.self);
		if (casoCurePrestazionali){
			pianoCongelabile = true;
			logger.trace(punto + " sono nella cure prestazionali ... NON EFFETTUO I CONTROLLI ");
		}else {
//			messaggio = verificaPresenzaMedicoDistretto();
//			boolean mancaMedicoDistretto = ISASUtil.valida(messaggio);
			messaggio = verificaPresenzaMedico();
			boolean mancaMedicoDiDistretto = ISASUtil.valida(messaggio);
			messaggio = verificaDatiPai(messaggio);    
			boolean attivareMascheraDatiSchedaVerbale = ISASUtil.valida(messaggio);
			messaggio = verificaDatiPaiObiettivi(messaggio);
			boolean attivareMascheraPaiObiettivi = ISASUtil.valida(messaggio);
			messaggio = verificaScalaBisogni(messaggio);
			
			if (messaggio !=null && ISASUtil.valida(messaggio)){
				String msg = Labels.getLabel("so.conferma.chiusura.pai.necessario.msg", new String[]{messaggio}); 
				Messagebox.show(msg, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.ERROR);
				
//				if (mancaMedicoDistretto) {
//					logger.trace(punto + " Necessario inserire il medico di distretto ");
//				} else {
				if (mancaMedicoDiDistretto) {
					logger.trace(punto + " Necessario inserire un medico ");
					dati_scheda_operatori_coinvolti.getLinkedTab().setSelected(true);
					//						dati_scheda_uvm.getLinkedTab().setSelected(true);
				} else {
					if (attivareMascheraDatiSchedaVerbale) {
						logger.trace(punto + " datiSchedaVerale ");
						dati_scheda_verbale_uvm.getLinkedTab().setSelected(true);
					} else {
						if (attivareMascheraPaiObiettivi) {
							logger.trace(punto + " SSSZ attivo OBBIETTIVI ");
							dati_scheda_pai_obiettivi.getLinkedTab().setSelected(true);
						}
					}
				}
//				}
				
			}else {
				pianoCongelabile = true;
			}   
		}
		return pianoCongelabile;
	}

//	private String verificaPresenzaMedicoDistretto() {
//		String punto = ver + "verificaPresenzaMedico ";
//		String messaggio = "";
//		boolean medicoDistrettualePresente = isMedicoDistrettualePresente();
//
//		if (!medicoDistrettualePresente) {
//			messaggio += (ISASUtil.valida(messaggio) ? ", " : "") + Labels.getLabel("so.inserire.medico.distretto.msg");
//			logger.trace(punto + " codice medico >>" + messaggio);
//		}
//		return messaggio;
//	}

	private boolean isMedicoDistrettualePresente() {
		boolean medicoPresente = false;
		if (SOOperatoriCoinvolti != null) {
			SOOperatoreCoinvoltiCtrl presentiUviBaseCtrl = (SOOperatoreCoinvoltiCtrl) SOOperatoriCoinvolti.getAttribute(MY_CTRL_KEY);
			medicoPresente = presentiUviBaseCtrl.isMedicoDistrettoPresente(true);
		}
		return medicoPresente;
	}

	private String verificaPresenzaMedico() {
		String messaggio ="";
		String punto = ver + "verificaPresenzaMedico ";
		int medicoPresenteInUvi = isMedicoPresenteInUvi();
		boolean medicoDistrettualePresente = isMedicoDistrettualePresente();
		
		if (!medicoDistrettualePresente && (medicoPresenteInUvi <0 || medicoPresenteInUvi == SOPresentiUviBaseCtrl.CTS_MANCA_CODICE_MEDICO) ) {
			messaggio = Labels.getLabel("so.inserire.medico.distretto.msg");
			logger.trace(punto + " codice medico >>" + messaggio);
		}
		return messaggio;
	}

	private int isMedicoPresenteInUvi() {
		int medicoPresente = -1;
		if (SOPresentiUvi != null) {
			SOPresentiUviBaseCtrl presentiUviBaseCtrl = (SOPresentiUviBaseCtrl) SOPresentiUvi.getAttribute(MY_CTRL_KEY);
			medicoPresente = presentiUviBaseCtrl.isComponentiUviInseritoMedico(GestTpOp.CTS_COD_MEDICO);
		}
		return medicoPresente;
	}

	public String verificaDatiPaiObiettivi(String messaggio) {
		String punto = ver + "verificaDatiPaiObiettivi ";
		logger.trace(punto + " verifico i DATI-PAI ");
		
		if (quadroSanitarioMMGCtrl.isCureResidenziali(self)) {
			logger.trace(punto + " non si rende obbligatorio inserimento del valore del case Manager e del livello attivazione  ");
		} else {
			String codCaseManager = cod_case_manager.getText();
			if (!ISASUtil.valida(codCaseManager)) {
				messaggio = aggiungiCampo(messaggio, "lbx_case_manager");
			}
			if (!((cbx_presa_carico_livello.getSelectedItem() != null) && (ISASUtil.valida(cbx_presa_carico_livello
					.getSelectedValue())))) {
				messaggio = aggiungiCampo(messaggio, "lbx_presacarico_livello");
				logger.trace(punto + " LIVELLO PRESA CARICO >>" + messaggio);
			}
		}
//		String prObiettivo = pr_obiettivo.getText();
//		if (!ISASUtil.valida(prObiettivo)) {
//			messaggio = aggiungiCampo(messaggio, "lbx_obiettivi");
//			logger.trace(punto + " LIVELLO PRESA CARICO >>" + messaggio);
//		}
		if (!(((cbx_pr_revisione.getSelectedItem() != null) && (ISASUtil.valida(cbx_pr_revisione.getSelectedValue()))))) {
			messaggio = aggiungiCampo(messaggio, "lbx_valutazione_successiva");
			logger.trace(punto + " valutazione successive >>" + messaggio);
		}
		return messaggio;
	}

	private String aggiungiCampo(String messaggio, String label) {
		String msg = messaggio + (ISASUtil.valida(messaggio) ? ", ":"")+ManagerDecod.recuperaDescrizioneLabel(self, label); 
		return msg; 
	}

	public String verificaDatiPai(String messaggio) {
		String punto = ver + "verificaDatiPai ";
		
		if (!ManagerDate.validaData(pr_data_verbale_uvm)){
			messaggio = aggiungiCampo(messaggio, "lb_pr_data_verbale_uvm");
			logger.trace(punto + " valorizzare data valutazione >>"+messaggio);
		}
		if(!isComponentiUviInseriti()){
			messaggio += (ISASUtil.valida(messaggio) ? ", ": "")+ Labels.getLabel("so.conferma.chiusura.pai.necessario.componenti.uvi");
			logger.trace(punto + " Componenti UVI non inseriti >>"+messaggio);
		}
		if (ISASUtil.valida(messaggio)){
			dati_scheda_verbale_uvm.getLinkedTab().setSelected(true);
		}
		
		return messaggio;
	}

	protected void doCongelaPiano() throws Exception {
		String punto = ver + "doCongelaPiano ";
//		isPianoCongelato.setValue("S");
		logger.trace(punto + " inizio con dati ");
		
		isPianoCongelato.setChecked(true);
		CaribelListbox op_coinv = (CaribelListbox) dati_scheda_operatori_coinvolti.getFirstChild().getFellowIfAny("griglia_op_coinvolti", true);
		boolean erroreSalvataggio = false;
		try {
			doSaveForm();
		} catch (Exception e) {
			erroreSalvataggio = true;
		}
		if (erroreSalvataggio){
			logger.debug(punto + " Errore nel salvataggio della scheda: non proseguo  ");
			return;
		}
		
		doCaricaPresentiUVI();
		CaribelGridStateCtrl paiCtrl = null;
//		if(tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
//		NEL CASO DI CURE PRESTAZIONIALI NON SI INSERISCONO LE FIGURE COINVOLTE
		if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
			paiCtrl = (CaribelGridStateCtrl) SOPai.getAttribute(MY_CTRL_KEY);
		}else if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)){
			paiCtrl = (CaribelGridStateCtrl) SOCP.getAttribute(MY_CTRL_KEY);
		}
		if (paiCtrl != null) {
			Vector<Hashtable<String, String>> vettorePiano = paiCtrl.getDataFromGrid();
			//		Vector<ISASRecord> vettorePAI = (Vector<ISASRecord>) currentIsasRecord.get("gridPianoPAI");
			Hashtable<String, String> newFig;
			Hashtable<String, Hashtable<String, String>> figure = new Hashtable<String, Hashtable<String, String>>();
			for (Hashtable<String, String> iterable_element : vettorePiano) {
				String figuraProfessionale = (String) iterable_element.get("cod_fig_prof");
				//			String cod_prest = (String) iterable_element.get("prest_cod");
				String datafine = (String) iterable_element.get("pai_data_fine");
				String datainizio = (String) iterable_element.get("pai_data_inizio");
				newFig = (Hashtable<String, String>) figure.get(figuraProfessionale);
				if (newFig == null) {
					newFig = new Hashtable<String, String>();
				}
				if (newFig.get("data_inizio") == null || datainizio.compareTo(newFig.get("data_inizio")) < 0) {
					newFig.put("data_inizio", datainizio);
				}
				if (newFig.get("data_fine") == null || datafine.compareTo(newFig.get("data_fine")) > 0) {
					newFig.put("data_fine", datafine);
				}
				newFig.put("tipo_operatore", figuraProfessionale);
				figure.put(figuraProfessionale, newFig);
			}
		
			Hashtable<String, String> dati ;
			Vector<ISASRecord> griglia = new Vector<ISASRecord>();
			RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
			for (String chiave : figure.keySet()) {
				dati = figure.get(chiave);
				griglia = inserisciOperatore(dati, griglia, rmSkSOOpCoinvoltiEJB);
			}
			
			griglia_op_coinvolti.getItems().clear();
			griglia_op_coinvolti.setModel(new CaribelListModel<ISASRecord>(griglia));
			
			if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)) {
//				if(isMedicoPresenteInUvi()){
//					inserisciAutorizzazioneMMG();
//				}
				if (op_coinv != null && !(op_coinv.getItemCount() > 0)) {
					UtilForUI.standardInfo(Labels.getLabel("segreteria.organizzativa.msg.congelaPianoNOFigure"));
				}
			}
		}
		
		if(ISASUtil.getValoreIntero(id_scheda_pht2.getValue())>0){
			Hashtable<String, String> dati ;
			dati = new Hashtable<String, String>();
			dati.put(Costanti.N_CARTELLA, n_cartella.getValue());
			dati.put(Costanti.CTS_ID_RICHIESTA, id_scheda_pht2.getValue());
			dati.put(CostantiPHT.dbNameStatoScheda, CostantiPHT.statoValutato);
			logger.trace(punto + " dati rercuperati>" +dati);
			RMSkSOBaseEJB rmSkSOBaseEJB = new RMSkSOBaseEJB();
			boolean archiviazioneOk = rmSkSOBaseEJB.modificaStatoPht2Generale(CaribelSessionManager.getInstance().getMyLogin(),dati);
			if (archiviazioneOk){
				logger.trace(punto + " ho archiviato correttamente la scheda pht2 ");
				UtilForUI.standardInfo(Labels.getLabel("so.scheda_pht2.inserito.stato.valutato"));
			}else {
				UtilForUI.standardInfo(Labels.getLabel("so.scheda_pht2.no.inserito.stato.valutato"));
			}
		}
	}
	
	@Override
	protected void impostaDataAttivazione() {
		String punto = ver + "impostaDataAttivazione ";
		if (!chiestaLaData) {
			if (!ManagerDate.validaData(data_presa_carico_skso)) {
				String lbxDataInizioPiano = ManagerDecod.recuperaDescrizioneLabel(self, "lb_dataInizio");
				String lbxDataAttivazione = ManagerDecod.recuperaDescrizioneLabel(self, "lb_data_presa_carico_skso");
				String messaggio = Labels.getLabel("segreteria.organizzativa.msg.imposta.data.attivazione",
						new String[] { lbxDataAttivazione, lbxDataInizioPiano });
				UtilForUI.standardYesOrNo(messaggio, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							if (ManagerDate.validaData(data_inizio) && !ManagerDate.validaData(data_presa_carico_skso)) {
								data_presa_carico_skso.setValue(data_inizio.getValue());
								doSaveForm();
								inserisciAutorizzazioneMMG();
							}
						}
					}
				});
			} else {
				logger.trace(punto + " \n non IMPORTO I DATI ");
			}
		}
		chiestaLaData = true;
		logger.trace(punto + " \n chiestaLaData>" + chiestaLaData + "<");
	}
	

	private void inserisciAutorizzazioneMMG() {
		String punto = ver + "inserisciAutorizzazioneMMG ";
		if (ManagerDate.validaData(data_presa_carico_skso)){
			SkMedEJB skMedEJB = new SkMedEJB();
			Hashtable<String, String> datiContattiMedici = new Hashtable<String, String>();
			datiContattiMedici.put(Costanti.N_CARTELLA, n_cartella.getText());
			datiContattiMedici.put(Costanti.CTS_ID_SKSO, id_skso.getText());
			skMedEJB.inserisciAutorizzazioneMMG(CaribelSessionManager.getInstance().getMyLogin(), datiContattiMedici);
			try {
				super.doCaricaOpCoinvolti();
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(punto + " Errore nel ricarica i componenti delle figure coinvolte ");
			}
		}else {
			logger.trace(punto + " non ho effettuato l'attivazione ");
		}
	}

	private Vector<ISASRecord> inserisciOperatore(Hashtable<String, String> dati,
			Vector<ISASRecord> griglia, RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB) {
		logger.trace("inserisciOperatore dati che esamino>>" + dati+"<<");
		String nCartella = ISASUtil.getValoreStringa(this.hParameters, CostantiSinssntW.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(this.hParameters, CostantiSinssntW.CTS_ID_SKSO);
		String tipoOperatore = ISASUtil.getValoreStringa(dati, "tipo_operatore");
		if (ISASUtil.valida(nCartella)&& ISASUtil.valida(idSkso)) {
			dati.put(CostantiSinssntW.N_CARTELLA, nCartella + "");
			dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso + "");
			dati.put(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO, data_inizio.getValueForIsas());
			if(ISASUtil.valida(tipoOperatore) && tipoOperatore.equals(GestTpOp.CTS_COD_MMG)){
				dati.put(CostantiSinssntW.CTS_OP_INSERIRE_MMG, CostantiSinssntW.CTS_SI);
			}else {
				dati.put(CostantiSinssntW.CTS_OP_INSERIRE_GENERICO, CostantiSinssntW.CTS_SI);
			}
			try {
				griglia = rmSkSOOpCoinvoltiEJB.inserisciMMGPLSOperatorePrimaVisita(CaribelSessionManager.getInstance().getMyLogin(), dati);
			} catch (DBRecordChangedException e) {
				e.printStackTrace();
			} catch (ISASPermissionDeniedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return griglia;
	}
	
	public boolean notificaPresaCaricoOperatore(String figuraProfessionale, String cod_operatore) {
		logger.trace("Richiesto l'inserimento dell'operatore " + cod_operatore+" per la figura professionale "+ figuraProfessionale);
		Hashtable<String, String> dati = new Hashtable<String, String>();
		
		//recupero la griglia delle prestazioni
		CaribelGridStateCtrl paiCtrl = null;
//		if(tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
		if (quadroSanitarioMMGCtrl.isCureDomiciliari(this.self)){
			paiCtrl = (CaribelGridStateCtrl) SOPai.getAttribute(MY_CTRL_KEY);
		}else if (quadroSanitarioMMGCtrl.isCurePrestazionali(this.self)){
			paiCtrl = (CaribelGridStateCtrl) SOCP.getAttribute(MY_CTRL_KEY);
		}
		
		if (paiCtrl != null) {
			Vector<Hashtable<String, String>> vettorePiano = paiCtrl.getDataFromGrid();
			//memorizzo la dataa inizio piu piccola e la data fine più grande per la figura professionale richiesta
			for (Hashtable<String, String> iterable_element : vettorePiano) {
				String datainizio = (String) iterable_element.get("pai_data_inizio");
				String datafine = (String) iterable_element.get("pai_data_fine");
				String figProf = (String) iterable_element.get("cod_fig_prof");
				if (figProf.equals(figuraProfessionale)) {
					if (dati.get("data_inizio") == null || datainizio.compareTo(dati.get("data_inizio")) < 0) {
						dati.put("data_inizio", datainizio);
					}
					if (dati.get("data_fine") == null || datafine.compareTo(dati.get("data_fine")) > 0) {
						dati.put("data_fine", datafine);
					}
				} else {
					continue;
				}
			}
			//salvo la figura professionale
			dati.put("tipo_operatore", figuraProfessionale);
			dati.put(CostantiSinssntW.CTS_COD_OP_CORRENTE, cod_operatore);
			dati.put("SOVRASCRIVI", "SI");
			Vector<ISASRecord> griglia = new Vector<ISASRecord>();
			//inserisco l'operatore tra le figure professionali coinvolte 
			griglia = inserisciOperatore(dati, griglia, new RMSkSOOpCoinvoltiEJB());
			
			//aggiorno la griglia degli operatori coinvolti
			griglia_op_coinvolti.getItems().clear();
			griglia_op_coinvolti.setModel(new CaribelListModel<ISASRecord>(griglia));
		}
		
		return true;
	}

	
	public void doUndoForm() throws Exception{		
		String punto = ver + "doUndoForm ";
		int idRichiesta = -1;
		try {
			idRichiesta = id_rich.getValue(); 
		} catch (Exception e) {
			logger.trace(punto + " dati da salvare >>" );
		}
		
		if (idRichiesta >0){
			super.doUndoForm(false);
		}else {
			super.doUndoForm(true);
		}
	}
	
	
	public void gestioneDataPresaCarico() {
		String punto = ver + "gestioneDataPresaCarico ";
		logger.trace(punto + " controllo se impostare la data ");
		boolean disabilitareDataAttivazioneSkso = true; 
		if (isPianoCongelato.isChecked()){
			disabilitareDataAttivazioneSkso = false;
			if (!ManagerDate.validaData(data_presa_carico_skso)) {
				impostaDataAttivazione();
			}else {
				logger.trace(punto + " data impostata ");
			}
		}
		data_presa_carico_skso.setDisabled(disabilitareDataAttivazioneSkso);
	}
	
	
	public static void settaTipoUtenza(CaribelCombobox cbx_utenza, CaribelCheckbox croniche_avanzate) {
		String punto = ver +"settaTipoUtenza ";
		try {
			if (croniche_avanzate !=null && croniche_avanzate.isChecked()) {
//				logger.trace(punto + " imposto il tipo malato terminale>>"+CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE+"<<");
				cbx_utenza.setSelectedValue(CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE);
			} else {
					UtilForContainer containerAssistito = new UtilForContainer();
					String data_nasc = containerAssistito
							.getObjectFromMyContainer("data_nasc") + "";
//					logger.trace("Data nascita = " + data_nasc);
					Date d1 = new GregorianCalendar(Integer.parseInt(data_nasc
							.substring(0, 4)), (Integer.parseInt(data_nasc
							.substring(5, 7))) - 1, Integer.parseInt(data_nasc
							.substring(8, 10))).getTime();
//					logger.trace("Data nascita = " + d1.toString());
					Date today = new Date();
//					logger.trace("Data oggi = " + today.toString());
					long diff = today.getTime() - d1.getTime();
					long annidiff = (diff / (1000 * 60 * 60 * 24)) / 365;
//					logger.trace(punto + " dati>>" + annidiff + "<");
					if (annidiff >= 65) {
						cbx_utenza
								.setSelectedValue(CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO);
					} else if (annidiff >= 18) {
						cbx_utenza
								.setSelectedValue(CostantiSinssntW.CTS_TIPO_UTENTE_ADULTO);
					} else {
						cbx_utenza
								.setSelectedValue(CostantiSinssntW.CTS_TIPO_UTENTE_MINORE);
					}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

 
	private void caricaComboCommissioni() throws Exception {
		String punto = ver + "caricaComboCommissioni ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		Component p = self.getFellow("panel_ubicazione");
		String distrettoOperatore = ManagerProfile.getDistrettoOperatore(getProfile());

		dati.put(CostantiSinssntW.CTS_COD_DISTRETTO_UVM, distrettoOperatore);
//		String nomeCombo = CostantiSinssntW.CTS_COD_DISTRETTO_UVM+"_"+distrettoOperatore;
//		logger.trace(punto + " nomecombo>>"+nomeCombo);
//		CaribelComboRepository.comboPreLoad(nomeCombo,  new CommissUVMEJB(), "query", dati, cbx_commissioni, null,
//			"cm_cod_comm", "cm_descr", true);
		CommissioneUvmFormCtrl commissioneUvmFormCtrl = new CommissioneUvmFormCtrl();
		commissioneUvmFormCtrl.caricaComboCommissioni(cbx_commissioni, dati, distrettoOperatore);
	}

	public void onChangeTipoOperatoreIncaricato(){
		String punto = ver + "onChangeTipoOperatoreIncaricato ";
		logger.debug(punto + "inizio  ");
		abilitaOperatorePrimaVisita(true);
	}

	public void doStampa() {
		String punto = ver + "doStampa ";
		try{
			String ejb    = "SINS_FOPAI";
			String metodo = "stampaPai";
			String report = "elencoPaiAssistito";
			Hashtable<String, Object> parametri = getParametriPerStampaPai();
			
			DatiStampaRichiesti datiStampaRichiesti = new DatiStampaRichiesti(metodo, report, ejb, parametri);
			datiStampaRichiesti.setTitoloMaschera(((Window)self).getTitle());
			
			Hashtable<String , Object> dati = new Hashtable<String, Object>();
			dati.put(CostantiSinssntW.CTS_STAMPA_BEAN, datiStampaRichiesti);
			
			logger.trace(punto + " dati >>" + parametri+"<<");
			
			Executions.getCurrent().createComponents(DatiStampaCtrl.CTS_FILE_ZUL, self,dati);
			
			
			//Test solo per provare
			if(false){
				doArchiviazionePAISuFSE();
			}
			
			
		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	private Hashtable<String, Object> getParametriPerStampaPai(){
		String punto = ver + "getParametriPerStampaPai ";
		
		Hashtable<String, Object> parametri = new Hashtable<String, Object>();
		parametri.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		parametri.put(CostantiSinssntW.CTS_ID_SKSO, id_skso.getText());
		parametri.put(CostantiSinssntW.CTS_TIPOCURA, tipocura.getSelectedValue()+"");
		
		Component tabPanelScale = self.getFellow("tabpanel_scale");
		ScalePanelCtr tabPanelScaleCtrl = (ScalePanelCtr) (tabPanelScale.getFirstChild().getAttribute(MY_CTRL_KEY));
		Hashtable<String, String> hParScala = tabPanelScaleCtrl.getDatiFromScale();
		logger.trace(punto + " dati recuperati>" + hParScala);
		parametri.putAll(hParScala);
		
		return parametri;
	}

	private HashMap<String, Object> recuperaDatiPerScalaValutazione_() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (pr_data_puac != null){
			map.put("pr_data", pr_data_puac.getValueForIsas());
			CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
			if (caribelContainerCtrl != null) {
				(caribelContainerCtrl.hashChiaveValore).put(CostantiSinssntW.PR_DATA, pr_data_puac.getValueForIsas());
			}					
		}
		map.put("chiamante", String.valueOf(CostantiSinssntW.CASO_SAN));
		if (pr_data_puac != null){
			map.put("data_ap", pr_data_puac.getValueForIsas());
			map.put("data_ap", pr_data_puac.getValueForIsas());
		}

		map.put("tipo_op", "00");
		boolean conTempoT = true;
		boolean tuttiCampi = true;
		map.put("tutti_campi", String.valueOf(tuttiCampi));
		map.put("con_tempo_t", String.valueOf(conTempoT));
		map.put("caribelContainerCtrl", super.caribelContainerCtrl);
		map.put(Costanti.N_CARTELLA, n_cartella.getText());
		
		if(id_skso !=null && (id_skso.getValue()!=null) && (id_skso.getValue() >0)){
			map.put(Costanti.CTS_ID_SKSO,id_skso.getValue()+"");
		}
		
		return map;
	}
	
	
	@Override
	public boolean doValidateForm() throws Exception {
		String punto = ver + "doSaveForm ";
		boolean canSave = true;
		logger.debug(punto + " salvataggio >>>");
		String dataAperturaCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.DATA_APERTURA)+"";
		
		
		String nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"";
		n_cartella.setValue(nCartella);    
//		richMMG_n_cartella.setValue(nCartella);
//		/if (!insertForm()){
		if(id_skso !=null && (id_skso.getValue()!=null) && (id_skso.getValue() >0)){
			logger.trace(punto + " ho idskso>>"+ id_skso.getValue());
		}else {
			logger.trace(punto + " vedo se esiste nel container ");
			Object objIdSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
			if (objIdSkSo!=null){
				id_skso.setText(objIdSkSo+"");
			}
			logger.trace(punto + " settato con>>"+ objIdSkSo+"<");
		}
		
		if (!ManagerDate.confrontaDate(dataAperturaCartella, pr_data_puac.getValueForIsas())){
			String lbprDataPuac = ManagerDecod.recuperaDescrizioneLabel(self, "lb_pr_data_puac"); 
			String[] lables = new String[] {lbprDataPuac};
			String messaggio = Labels.getLabel("so.conferma.chiusura.msg.apertura.scheda", lables);
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.QUESTION);
			canSave = false;
			return canSave;
		}
		
		if (!isMessaggioControlloCoerenzaDate()){
			return false;
		}
		
		canSave = ManagerDate.controllaPeriodo(self, data_inizio, data_fine, "lb_dataInizio","lb_dataFine");

		return canSave;
	}

	private boolean insertForm() {
		String punto = ver + "insertForm ";
		boolean sonoInsert = false;
		String recFittizio = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_RECORD_FITTIZIO);
		if (this.currentIsasRecord == null || ISASUtil.valida(recFittizio)) {
			sonoInsert = true;
		}
		logger.trace(punto + " sono in insert >>"+sonoInsert+"<< ");
		return sonoInsert;
	}

	protected void doFreezeForm() throws Exception {
		if (tabpanel_scale.getFirstChild() != null){
			tabpanel_scale.getFirstChild().detach();
		}
		super.doFreezeForm();
		gestionePannelloScale();
	}

	public void onCheck$selTutti(Event event){
		int numRighe = clbobiettivi.getItemCount();
		boolean selez = selTutti.isChecked();
		Listitem item;
		for(int k=0; k<numRighe; k++){
			item = clbobiettivi.getItemAtIndex(k);
			for (Iterator<Component> iterator = item.getChildren().iterator(); iterator.hasNext();) {
				Component type = iterator.next();
				if(type instanceof Listcell) {
					Listcell new_name = (Listcell) type;
					if(new_name.getFirstChild() instanceof Checkbox){
						Checkbox check = (Checkbox)new_name.getFirstChild();
						check.setChecked(selez);
						break;
					}
				}
			}
		}
		if(selez && numRighe>0){
			clbobiettivi.selectAll();
		}else{
			clbobiettivi.clearSelection();
		}
	}
	
	public void onCheckCell(ForwardEvent event) throws Exception {
		Component comp = event.getOrigin().getTarget();
		Set tmp = new HashSet(clbobiettivi.getSelectedItems());
		int count = tmp.size();
		if(((Checkbox) comp).isChecked()){
			tmp.add((Listitem) comp.getParent().getParent());
		}else{
			tmp.remove((Listitem) comp.getParent().getParent());
			selTutti.setChecked(false);
		}
		count = tmp.size();
//		clbobiettivi.clearSelection();
//		clbobiettivi.setSelectedItems(tmp);
	}
	
	
	@Override
	public void onClick$btn_undo() {
		String punto = ver + " onClick$btn_undo ";
		logger.trace(punto + " ricarico ");
		super.onClick$btn_undo();
	}
	
	public void onClickedCell(ForwardEvent event) throws Exception {
//		Component comp = event.getOrigin().getTarget();
//		int x = clbobiettivi.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
//		mostraAssistito(x);
	}

	public void setCambioPiano() throws Exception {
		doCambioPiano("");
	}
		
	@Override
	public void setDataInput(String data, String methodName) throws Exception {
		Component pandata = self.getFellowIfAny("pannelloData");
		//se non è stata inserita una data non faccio nulla
		if(data!=null && !data.isEmpty() ){
			Date dataInizioNuovePrestazioni = UtilForBinding.getDateFromIsas(data);
			String msg = dataValidaSKSO(dataInizioNuovePrestazioni);
			//se la data impostata non è in un periodo valido stampo il messaggio di errore
			if(!msg.isEmpty()){
				UtilForUI.standardExclamation(msg);
				return;
			}
			pandata.detach(); 
			if(methodName == null){
				doCambioPiano(data);
			} else if(methodName.equals(ADD_PREST_CP)){
				doCaricaPrestazioniCPSpecificheDa(checkPrest, dataInizioNuovePrestazioni);
				checkPrest = null;
			}
			return;
		}
	}
	
	private String dataValidaSKSO(Date dataInizioNuovePrestazioni) {
		Date dataInizio;
		Date dataFine = data_fine.getValue();
		try {
			Component sospensione = self.getFellowIfAny("SOSospensione", true);
			if(sospensione!=null){
				SOSospensioneCtrl sospensioneCtrl = (SOSospensioneCtrl) sospensione.getAttribute(MY_CTRL_KEY);
				Vector<Hashtable<String, String>> sospensioni = sospensioneCtrl.getDataFromGrid();
				//ciclo sulle sospensioni per essere sicuro che non è in un perodo sospeso
				for (Hashtable<String, String> sosp : sospensioni) {
					 dataInizio = UtilForBinding.getDateFromIsas(sosp.get("dt_sospensione_inizio"));
					 dataFine = UtilForBinding.getDateFromIsas(sosp.get("dt_sospensione_fine"));
						//se la data è compresa in una sospensione restituisco il messaggio di errore
						if(dataInizioNuovePrestazioni.after(dataInizio)&& dataInizioNuovePrestazioni.before(dataFine)){
							return Labels.getLabel("segreteria.organizzativa.msg.dataCambioNonConformeXSospensione", new String[]{UtilForBinding.getValueForIsas(dataInizio), UtilForBinding.getValueForIsas(dataFine)});
						}
				}
			}
			
			//se è nel periodo di validità della scheda restituisco true, ho già escluso eventuali sosensioni
			if(!(data_inizio.getValue().after(dataInizioNuovePrestazioni) || dataInizioNuovePrestazioni.after(data_fine.getValue()))){
				return "";
			}
			
			Component proroghe = self.getFellowIfAny("SOProroghe", true);
			if(proroghe!=null){
				SOProrogheCtrl prorogheCtrl = (SOProrogheCtrl) proroghe.getAttribute(MY_CTRL_KEY);
				Vector<Hashtable<String, String>> sospensioni = prorogheCtrl.getDataFromGrid();
				//ciclo sulle prorghe per essere sicuro che è in un periodo attivato
				for (Hashtable<String, String> sosp : sospensioni) {
					 dataInizio = UtilForBinding.getDateFromIsas(sosp.get("dt_proroga_inizio"));
					 dataFine = UtilForBinding.getDateFromIsas(sosp.get("dt_proroga_fine"));
					 	//se la data è compresa in una proroga restituisco un messaggio vuoto
						if(!(dataInizio.after(dataInizioNuovePrestazioni) || dataInizioNuovePrestazioni.after(dataFine))){
							return "";
						}
				}			
			}
		} catch (ParseException e) {
			doShowException(e);
		}
		//se sono arrivato qui vuol dire che non è in un periodo valido
		return Labels.getLabel("segreteria.organizzativa.msg.dataCambioNonConforme", new String[]{data_inizio.getValueForIsas(), data_fine.getValueForIsas(), UtilForBinding.getValueForIsas(dataFine)});
	}

	public void onCheck$adp(Event event){
		caricaPrestazioniCPSpecifiche(adp);
	}
	
	public void onCheck$ard(Event event){
		caricaPrestazioniCPSpecifiche(ard);
	}
	
	public void onCheck$aid(Event event){
		caricaPrestazioniCPSpecifiche(aid);
	}
	
	public void onCheck$vsd(Event event){
		caricaPrestazioniCPSpecifiche(vsd);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void caricaPrestazioniCPSpecifiche(Checkbox checkboxIntensita) {
			if(id_skso.getValue()!=null){
				if(data_presa_carico_skso.getValue()!=null && checkboxIntensita.isChecked()){
					Hashtable ht = new Hashtable();
					String mess=Labels.getLabel("segreteria.organizzativa.msg.aggiungiPrestazioniCP");
					ht.put("mess", mess);
					ht.put("titolo", Labels.getLabel("messagebox.attention"));
					ht.put("height", "150px");
					ht.put(PannelloDataFormCtrl.METHODNAME, ADD_PREST_CP);
					checkPrest = checkboxIntensita;
					Component comp = Executions.getCurrent().createComponents(PannelloDataFormCtrl.myZul, self, ht);
					comp.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
						public void onEvent(Event event) throws Exception{
							annullaAggiuntaPrestazioni();
						}
					});
					return;
				}else{
					doCaricaPrestazioniCPSpecificheDa(checkboxIntensita, null);
				}
			}
	}

	protected void annullaAggiuntaPrestazioni() {
		checkPrest.setChecked(false);
		checkPrest = null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doCaricaPrestazioniCPSpecificheDa(Checkbox checkboxIntensita, Date nuovaDataInizio) {
		try {
			CaribelListModel modelPrestazioni = (CaribelListModel) listaPrestazioniCP.getModel();
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			h.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
			h.put("soloBisogni", true);
			if(nuovaDataInizio != null){
				h.put("data_inizio", nuovaDataInizio);
			}else if(data_inizio.getValue()!=null){
				h.put("data_inizio", data_inizio.getValue());
			}else{
				h.put("data_inizio", pr_data_puac.getValue());
			}
			if(data_fine.getValue()!=null){
				h.put("data_fine", data_fine.getValue());
			}else{
				h.put("data_fine", new Date());
			}
			Vector<String> listaBisogni = new Vector<String>();
			if(checkboxIntensita.equals(adp)){
				listaBisogni.add("CP_ADP");
			}
			if(checkboxIntensita.equals(ard)){
				listaBisogni.add("CP_ARD");
			}
			if(checkboxIntensita.equals(aid)){
				listaBisogni.add("CP_AID");
			}
			if(checkboxIntensita.equals(vsd)){
				listaBisogni.add("CP_VSD");
			}

			h.put("listaBisogni", listaBisogni);
			h.put(CostantiSinssntW.CTS_TIPOCURA, tipocura.getSelectedValue());
			Vector<ISASRecord> prestazioni = bisogni.getPrestazioni(CaribelSessionManager.getInstance().getMyLogin(), h);
			boolean mod = false;
			String prest = "";
			ISASRecord isasRecord = null;
			Hashtable<String, Object> h1 = new Hashtable<String, Object>();
			if(checkboxIntensita.isChecked()){
				for (Iterator iterator = prestazioni.iterator(); iterator.hasNext();) {
					isasRecord = (ISASRecord) iterator.next();
					prest = (String) isasRecord.get("prest_cod");
					h1.put("prest_cod", prest);
					if(modelPrestazioni.columnsContains(h1)==-1){
						modelPrestazioni.add(isasRecord);
					}
				}
				//					mod = modelPrestazioni.addAll(prestazioni);
			}else{
				int i=0;
				for (Iterator iterator = prestazioni.iterator(); iterator.hasNext();) {
					isasRecord = (ISASRecord) iterator.next();
					prest = (String) isasRecord.get("prest_cod");
					h1.put("prest_cod", isasRecord.get("prest_cod"));
					i = modelPrestazioni.columnsContains(h1);
					if(i>-1){
						modelPrestazioni.remove(i);
					}
				}
			}
			listaPrestazioniCP.getItems().clear();
//			listaPrestazioniCP.setItemRenderer(new PAIGridItemRenderer());
			modelPrestazioni.setMultiple(listaPrestazioniCP.isMultiple());
			listaPrestazioniCP.setModel(modelPrestazioni);
			listaPrestazioniCP.invalidate(); 
		} catch (SQLException e) {
			logger.error("Errore nel caricamento delle prestazioni");
		} catch (ISASMisuseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void onUpdateProrogheSospensioni(Event e) {
		settaStatoScheda();	
	}

	private void settaStatoScheda() {
		String punto = ver + "settaStatoScheda";
		String msgStatoScheda= "";
		if (insertMode()){
			msgStatoScheda= Labels.getLabel(CostantiSinssntW.CTS_SKSO_STATO_NUOVA);
		}else {
			if (this.currentIsasRecord!=null && this.currentIsasRecord.getHashtable().containsKey(CostantiSinssntW.CTS_SKSO_STATO)){
				try {
					statoSchedaSO = myEJB.verificaStatoScheda(CaribelSessionManager.getInstance().getMyLogin(), currentIsasRecord);
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error(punto + " Errore >>" +e);
				}
				msgStatoScheda = Labels.getLabel(statoSchedaSO);
			}
		}
		lbx_stato_scheda.setValue(msgStatoScheda);
	}
	
	@Override
	protected void setBtnPrintDisabled(boolean disabled){
		super.setBtnPrintDisabled(disabled);
		
		if(stampaPht2!=null){
			stampaPht2.setDisabled(disabled);
			if(stampaPht2.getImage()!=null)
				stampaPht2.setImage("~./zul/img/print24x24"+(disabled?"-dis":"" )+".png");
		}
		if(stampaSO!=null){
			stampaSO.setDisabled(disabled);
			if(stampaSO.getImage()!=null)
				stampaSO.setImage("~./zul/img/print24x24"+(disabled?"-dis":"" )+".png");
		}
		if(stampaSoPai!=null){
			stampaSoPai.setDisabled(disabled);
			if(stampaSoPai.getImage()!=null)
				stampaSoPai.setImage("~./zul/img/print24x24"+(disabled?"-dis":"" )+".png");
		}
	}
	
	
	
	
	public void aggiungiBottonTrasmissioneDistretto() {
		String punto = ver + "aggiungiBottonTrasmissioneDistretto ";
		logger.debug(punto + " inizio ");
		try {
			String fonte = getParameters(Costanti.CTS_FONTE);
			if (ISASUtil.valida(fonte) && ISASUtil.getValoreIntero(fonte) == Costanti.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA) {
				Object objST = self.getFellowIfAny("trasmettiDistretto");
				if (objST== null){
					trasmettiDistretto = new Button();
					trasmettiDistretto.setId("trasmettiDistretto");					
					trasmettiDistretto.setStyle("color: #0983C5");
					trasmettiDistretto.setImage("~./zul/img/rightarrow_g.png");
					trasmettiDistretto.setLabel(Labels.getLabel("segreteria.organizzativa.trasmetti.distretto"));
					//stampaPht2.getParent().insertBefore(trasmettiDistretto,stampaPht2);
					btn_printDatiSo.getParent().insertBefore(trasmettiDistretto, btn_printDatiSo);
					stampaPht2.getParent().invalidate();
					trasmettiDistretto.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
						public void onEvent(Event event) {
							try {
								doTrasmettiDistretto(event);
							} catch (Exception e) {
								doShowException(e);
							}
						}
					});
				
				}
			} else {
				logger.trace(punto + " non inserisco il bottone PHT2 ");
			}
			
			if (!insertMode() && (trasmettiDistretto!=null)){
				trasmettiDistretto.setDisabled(true);
			}
		} catch (Exception x) {
			x.printStackTrace();
			logger.trace(punto + " Errore nell'inserire il menu ", x);
		}
	}
	

	
	public void doTrasmettiDistretto(Event event) {
		String punto = ver + "doTrasmettiDistretto ";
		try {
			Hashtable<String, Object> dati = new Hashtable<String, Object>();
			String distretto_ass= ManagerProfileBase.getDistrettoOperatore(getProfile());
			Component p = ubicazioneO.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
			String codDistrettoAppartenenza = c.getDistrettoValue();
			
			String codZonaAppartenenza = zona.getValue();
			String codPresidioAppartenenza = c.getPresidioComuneAreaValue();
			
			String assistito = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.ASSISTITO_COGNOME).toString() +" " + UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.ASSISTITO_NOME).toString();
			int nCartellaPht2 = ISASUtil.getValoreIntero(arg.get("n_cartella") + "");
			String idSchedaPht2 = (String)arg.get("id_richiesta");
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_ZONA_APPARTENENZA, codZonaAppartenenza);
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_DISTRETTO_APPARTENENZA, codDistrettoAppartenenza);
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_SEDE_APPARTENENZA, codPresidioAppartenenza);
			dati.put(AttribuzioneDistrettoPht2Ctrl.METHODNAME, "caricaSchedaPHT2");
			
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_INFO_ASSISTITO,assistito);
			dati.put(Costanti.N_CARTELLA, nCartellaPht2);
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_ID_SCHEDA_PHT2, id_scheda_pht2.getValue());
			
			Executions.getCurrent().createComponents(AttribuzioneDistrettoPht2Ctrl.myPathZul, self, dati);
			
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	public void setDatiInput(Hashtable<String, String> dati) throws Exception {
		  String punto = ver + "setDatiInput ";
		  logger.trace(punto + " inizio con dati >>" +dati +"< ");
		  
		  Component pandata = self.getFellowIfAny("attribuzioneDistrettoA");
		  if(pandata != null){
			  //attivita.caricaSchedaPHT2(dati);
			  caricaSchedaPHT2(dati);
		  }
		  if(pandata != null){
			  UtilForContainer.restartContainerFromListaAttivita();
		  }
		  logger.trace(punto + " Chiudo il pannello ");

		 }
	
	public  void caricaSchedaPHT2(Hashtable<String, String> dati) {
		String punto = ver + "caricaSchedaPHT2 ";
		logger.debug(punto + "inizio con dati >" + dati);
		int fonte = CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA;
		String n_cartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
		String id_richiesta = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_ID_SCHEDA_PHT2);

		String parametri = CostantiSinssntW.CTS_FONTE + "=" + fonte + "&" + CostantiSinssntW.N_CARTELLA + "="
				+ n_cartella;
		parametri += "&" + CostantiSinssntW.CTS_ID_RICHIESTA + "=" + id_richiesta;
		logger.trace(punto + " parametri>>" +parametri);

		int operazioneDaEseguire = ISASUtil.getValoreIntero(dati, AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE);
		if (operazioneDaEseguire > 0) {
			switch (operazioneDaEseguire) {
			case AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE_CARICA_ASSISTITO:
				String assistito = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_INFO_ASSISTITO);
				String messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.carica.richiesta.msg", new String[]{assistito});
				Clients.showNotification(messaggio,"info", self, "middle_center",CostantiSinssntW.INT_TIME_OUT);
				Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul + "?" + parametri);
				break;
			case AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE_SPOSTA_ASSITITO:
				spostaUtentePht2(dati);
				break;
			case AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE_CANCELLA:
				Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul + "?" + parametri);
				break;
			default:
				break;
			}
		}else {
			//doCerca();
		}
		
	}

	private void spostaUtentePht2(Hashtable<String, String> dati) {
		String punto = ver + "spostaUtentePht2 ";
		logger.trace(punto + " inizio con dati >>" +dati +"< ");
		boolean aggiornamentoOk = false;
		try {
			aggiornamentoOk = myEJB_att.spostaUtentePht2(CaribelSessionManager.getInstance().getMyLogin(), dati);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String assistito = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_INFO_ASSISTITO);
		String messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg.ko", new String[]{assistito});
		if (aggiornamentoOk){
			messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg.ok", new String[]{assistito});
		}
		Clients.showNotification(messaggio,"info", self, "middle_center",CostantiSinssntW.INT_TIME_OUT);
		//doCerca();
	}

	private boolean isAbilInoltroFSE() throws Exception {
		String confTxt = ManagerProfile.getValue(getProfile(), ManagerProfile.ABILIT_INVIO_FSE);
		boolean abilInoltroFSE = ((confTxt != null) && (confTxt.trim().equals("SI")));
		return abilInoltroFSE;
	}
	
	private void doArchiviazionePAISuFSE() throws Exception{
		String punto = ver + "doArchiviazionePAISuFSE ";
		logger.trace(punto + " inizio ");
		if(quadroSanitarioMMGCtrl.isCureDomiciliari(self)){
			if (isAbilInoltroFSE()) {
				UtilForUI.standardYesOrNo(Labels.getLabel("segreteria.organizzativa.msg.inviaPianoSuFSE"),
						new EventListener<Event>(){
							public void onEvent(Event event) throws Exception{
								if (Messagebox.ON_YES.equals(event.getName())){	
									eseguiArchiviazionePaiSuFse();
								}					
							}
					});
			}
		}
	}
	
	private void eseguiArchiviazionePaiSuFse() throws Exception {
		String punto = ver + "eseguiArchiviazionePaiSuFse ";
		logger.trace(punto + " inizio ");
		PAI_FSE_EJB paiFseEjb = new PAI_FSE_EJB();
		myLogin mylogin= CaribelSessionManager.getInstance().getMyLogin();
		//Genero il documento PAI
		Hashtable<String, Object> parametri = getParametriPerStampaPai();		
		this.docPaiPdfEncodedBase64 = paiFseEjb.getDocumentPAI(mylogin, parametri);
		//Recupero codice assistito
		this.idAsrEmpi = paiFseEjb.getAsrEmpi(mylogin, n_cartella.getText());
		//Richiedo OTP per firma del documento (questa vale per pochi secondi, per questo ho fatto tutti i precedenti recuperi)
		Hashtable<String, String> ht = new Hashtable<String, String>();
		ht.put(PannelloSignDocFormCtrl.LABEL_FOR_TITLE, Labels.getLabel("segreteria.organizzativa.scheda.pai.sign_for_fse.title"));
		ht.put(PannelloSignDocFormCtrl.LABEL_FOR_MESSAGE, Labels.getLabel("segreteria.organizzativa.scheda.pai.sign_for_fse.msg"));
		ht.put(PannelloSignDocFormCtrl.LABEL_FOR_SIGNER, Labels.getLabel("segreteria.organizzativa.scheda.pai.sign_for_fse.signer"));
		ht.put(PannelloSignDocFormCtrl.LABEL_FOR_PIN, Labels.getLabel("segreteria.organizzativa.scheda.pai.sign_for_fse.pin"));
		ht.put(PannelloSignDocFormCtrl.LABEL_FOR_OTP, Labels.getLabel("segreteria.organizzativa.scheda.pai.sign_for_fse.otp"));
		Executions.getCurrent().createComponents(PannelloSignDocFormCtrl.myZul, self, ht);
	}
	
	@Override
	public void setSignDocInput(Hashtable<String, String> dati) throws Exception {
		String punto = ver + "setSignDocInput ";
		logger.trace(punto + " inizio ");
		PAI_FSE_EJB paiFseEjb = new PAI_FSE_EJB();
		myLogin mylogin= CaribelSessionManager.getInstance().getMyLogin();
		//Recupero SIGNER, PIN e OTP inseriti dall'operatore
		String signer	= dati.get(PannelloSignDocFormCtrl.SIGNER_VALUE);
		String pin_code	= dati.get(PannelloSignDocFormCtrl.PIN_VALUE);
		String otp_code	= dati.get(PannelloSignDocFormCtrl.OTP_VALUE);
		//Invoco il servizio di firma digitale remota
		String docPdfEncodedBase64Signed = paiFseEjb.getDocumentPAISigned(mylogin, this.idAsrEmpi, this.docPaiPdfEncodedBase64,signer,pin_code,otp_code);
		//Carico su tabella di frontiera del XDSEngine
		if(docPdfEncodedBase64Signed!=null && !docPdfEncodedBase64Signed.equals(""))
			paiFseEjb.caricaSuTabellaDiFrontiera(mylogin, this.idAsrEmpi,signer,docPdfEncodedBase64Signed);
		else
			UtilForUI.standardExclamation(Labels.getLabel("segreteria.organizzativa.scheda.pai.sign_for_fse.ko"));
	}
	

}
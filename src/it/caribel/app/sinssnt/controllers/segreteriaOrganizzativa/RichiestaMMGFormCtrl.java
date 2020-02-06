package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.common.ejb.DistrettiEJB;
import it.caribel.app.common.ejb.ParentEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RmRichiesteMMGEJB;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.autorizzazioni.AutorizzazioniMMGAdiFormCtrl;
import it.caribel.app.sinssnt.controllers.autorizzazioni.AutorizzazioniMMGAdpFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.intolleranzeAllergie.IntolleranzeAllergieGridCRUDCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.tabelle.diagnosi.DiagnosiFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
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
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Window;

public class RichiestaMMGFormCtrl extends CaribelFormCtrl  {

	/**
	 */
	private static final long serialVersionUID = 1L;

	RmRichiesteMMGEJB myEJB = new RmRichiesteMMGEJB();
	
	public static String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/richiesta_mmg.zul";
	QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
	private String myKeyPermission = ChiaviISASSinssntWeb.RICH_MMG;
	private CaribelIntbox n_cartella;
	private CaribelIntbox id_rich;
	private CaribelIntbox id_scheda_so;
	
	private CaribelTextbox stato_rich;
	private CaribelCombobox richiedente;
	private CaribelCombobox tipocura;
	private CaribelCombobox tipoFrequenza;
	private CaribelCombobox cbx_grado_parentela;
	private CaribelCombobox autosufficienza;
	private CaribelCombobox cod_distretto;
//	private CaribelTextbox cognome_caregiver;
//	private CaribelTextbox telefono_caregiver;
//	private CaribelTextbox nome_caregiver;         
	private CaribelIntbox accessi_mmg;
	
	private CaribelDatebox data_inizio;
	private CaribelDatebox data_fine;
	
	private CaribelDatebox data_richiesta;
	private CaribelDatebox data_diag;
	private Date dataDiag;
	private Component patologieGrid;
	private Vector<Hashtable<String, String>> vettOper = new Vector<Hashtable<String, String>>();
	protected CaribelListbox tablePrestazioni;
	private Window richiestaMMGForm;
	private Window intolleranzeallergie;
	
	private CaribelCheckbox flag_trasporto;
	private CaribelCheckbox flag_non_auto;
	private CaribelCheckbox flag_piano_alto;
	private CaribelCheckbox flag_traporto_altro;
	private CaribelTextbox trasporto_altro;
	private CaribelTextbox medico_desc;
	private CaribelTextbox cod_med;
	ISASRecord anagra = null;
	
	private Button btn_conferma;
	private Button btn_archivia;
	private Button btn_presacarico;
	
	private CaribelTextbox pato1;
	private CaribelTextbox pato2;
	private CaribelTextbox pato3;
	private CaribelTextbox pato4;
	private CaribelTextbox pato5;
	
	private AbstractComponent patologiadet1;
	private CaribelRadiogroup sitfam;

	private String ver ="16- ";
	private Tab principale_tab;
	private Tabpanel quadro_into_alle;

	public void doInitForm() {
		try {

			logger.debug("inizio ");
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			
			//Appendo il pannello per intolleranze allergie
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_NOME_TABELLA, "RM_INTOLLERANZE_ALLERGIE");
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_STRING_N_CARTELLA, UtilForContainerGen.getCartellaCorr());
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_ID_MY_WINDOW, "intolleranzeallergie");
			if(intolleranzeallergie==null){
				intolleranzeallergie = (Window)Executions.getCurrent().createComponents(IntolleranzeAllergieGridCRUDCtrl.myPathFormZul, quadro_into_alle, mapParam);
				intolleranzeallergie.setBorder("none");
				intolleranzeallergie.setTitle("");
			}
			
			doMakeControl();
			doPopulateCombobox();
			data_fine.addEventListener(Events.ON_BLUR, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					SegreteriaOrganizzativaFormCtrl.aggiungiMesi(data_inizio, data_fine);
					return;
				}
			});
			
			pato1.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					gestionePatologia();
					return;
				}
			});
			pato2.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					gestionePatologia();
					return;
				}
			});
			pato3.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					gestionePatologia();
					return;
				}
			});
			pato4.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					gestionePatologia();
					return;
				}
			});
			pato5.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					gestionePatologia();
					return;
				}
			});
			
			data_inizio.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
					SegreteriaOrganizzativaFormCtrl.aggiungiMesi(data_inizio, data_fine);
				}
				});			
			
			data_inizio.addEventListener(Events.ON_BLUR, new EventListener<Event>(){
				public void onEvent(Event event){
					SegreteriaOrganizzativaFormCtrl.aggiungiMesi(data_inizio, data_fine);
				}
				});
			
			String nCartella = (String)arg.get(CostantiSinssntW.N_CARTELLA);
			String idRich = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_ID_RICH);
			if (ISASUtil.valida(nCartella))n_cartella.setValue(new Integer(nCartella));			
			if (dbrFromList != null || ( ISASUtil.valida(nCartella) && ISASUtil.valida(idRich))) {
				if (dbrFromList!=null){
					// sono stato invocato da una griglia
					nCartella = ((Integer) dbrFromList.get(CostantiSinssntW.N_CARTELLA)).toString();
					idRich = dbrFromList.get("id_rich").toString();
				}
				hParameters.put(CostantiSinssntW.N_CARTELLA,  nCartella);
				hParameters.put(CostantiSinssntW.CTS_ID_RICH, idRich);
				
				n_cartella.setValue(new Integer(hParameters.get(CostantiSinssntW.N_CARTELLA).toString()));
				id_rich.setValue(new Integer(hParameters.get(CostantiSinssntW.CTS_ID_RICH).toString()));
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}
			doMakeControlAfterRead();
			doFreezeForm();
			if (isInInsert()){
				richiestaMMGForm.setTitle(Labels.getLabel("richiesta_mmg.title.in_inserimento"));
				if (anagra!=null && anagra.get("cod_distretto")!=null){
					String codice = anagra.get("cod_distretto").toString();
						if (!codice.equals(ManagerProfile.getDistrettoOperatore(getProfile())))
							Clients.showNotification(Labels.getLabel("notifica.distretto.operatore.diverso"),"info",cod_distretto,
									"top_center",CostantiSinssntW.INT_TIME_OUT);
					}
			}
			else richiestaMMGForm.setTitle(Labels.getLabel("richiesta_mmg.title.in_update",new String[]{data_richiesta.getRawText()})); 
			
		} catch (Exception e) {
			doShowException(e);
		}
		settaValoreFrequenza();
	}

	protected void gestionePatologia() {
		boolean obbligatorioData = false;
		boolean casoCureDomiciliari = quadroSanitarioMMGCtrl.isCureDomiciliari(this.self);
		boolean casoResidenziale = quadroSanitarioMMGCtrl.isCureResidenziali(this.self);

		boolean patologiaInserita = esistePatologia(pato1) || esistePatologia(pato2) || esistePatologia(pato3)
				|| esistePatologia(pato4) || esistePatologia(pato5);
		obbligatorioData = patologiaInserita || casoCureDomiciliari || casoResidenziale;
		
		data_diag.setRequired(obbligatorioData);
		pato1.setRequired(casoCureDomiciliari || casoResidenziale);
	}

	private boolean esistePatologia(CaribelTextbox patologia) {
		return (patologia != null && ISASUtil.valida(patologia.getText()));
	}

	@Override
	protected void doDeleteForm() throws Exception {
		super.doDeleteForm();
		if (caribelContainerCtrl!=null && caribelContainerCtrl instanceof ContainerPuacCtrl){
			((ContainerPuacCtrl)caribelContainerCtrl).btn_ElencoRichiesteMMGForm();
		}
	}

	@Override
	public void doUndoForm() throws Exception {
		super.doUndoForm();
		this.principale_tab.setSelected(true);
	}

	private void doPopulateCombobox() throws Exception {
	
		Hashtable<String, String> richiedente_hash = new Hashtable<String, String>();
		richiedente_hash.put("tab_cod", CostantiSinssntW.TAB_VAL_SEGNALANTE);
		CaribelComboRepository.comboPreLoad("rich_mmg_richiedente",
				new TabVociEJB(), "query", richiedente_hash, richiedente, null,
				"tab_val", "tab_descrizione", false);

		Hashtable<String, String> autosufficienza_hash = new Hashtable<String, String>();
		autosufficienza_hash.put("tab_cod",CostantiSinssntW.TAB_VAL_AUTOSUFFICIENTE);
		CaribelComboRepository.comboPreLoad("rich_mmg_autosufficienza",
				new TabVociEJB(), "query", autosufficienza_hash, autosufficienza, null,
				"tab_val", "tab_descrizione", false);
		
		Hashtable<String, String> distetto_hash = new Hashtable<String, String>();
		CaribelComboRepository.comboPreLoad("rich_mmg_distretto",
				new DistrettiEJB(), "query", distetto_hash, cod_distretto, null,
				"cod_distr", "des_distr", false);
		
		CaribelComboRepository.comboPreLoad("grado_parentela",  new ParentEJB(), "query", new Hashtable<String, String>(), cbx_grado_parentela, null,
				"codice", "descrizione", true);
		
		quadroSanitarioMMGCtrl.caricoFrequenza(tipocura,tipoFrequenza, false, accessi_mmg);
	}

	
	private void settaValoreFrequenza() {
		String punto = ver + "caricoFrequenza ";
		logger.trace(punto + " carico la combo Frequenza ");
		quadroSanitarioMMGCtrl.caricoFrequenza(tipocura,tipoFrequenza,false, accessi_mmg);
		if (tipoFrequenza !=null && tipoFrequenza.getItemCount()>= 0){
			int frequenza = ISASUtil.getValoreIntero(this.currentIsasRecord, "frequenza");
			if (frequenza>=0){
				tipoFrequenza.setSelectedValue(frequenza+"");
			}
		}else {
			logger.trace(punto + " Non carico la combo Frequenza: intensita non selezionata ");
		}
	}

	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = ver  +this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		
		
		logger.trace(punto + " effettuo il controllo che i dati obligatori sono stati inseriti ");
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
		map.put(CostantiSinssntW.DATA_APERTURA, data_richiesta.getValue());
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
					h.put("data_apertura", data_richiesta.getValueForIsas());
					h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
					lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryStoricoDiag"));
					tablePrestazioni.setModel(lm);
					doFreezeForm();
				}
			});
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	private void doMakeControl() {
		
		flag_traporto_altro.setDisabled(true);
		flag_piano_alto.setDisabled(true);
		flag_non_auto.setDisabled(true);
		trasporto_altro.setDisabled(true);
		
		if (flag_trasporto.isChecked()){
			flag_traporto_altro.setDisabled(false);
			flag_piano_alto.setDisabled(false);
			flag_non_auto.setDisabled(false);
			trasporto_altro.setDisabled(false);
		}

		btn_archivia.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			public void onEvent(Event event) throws Exception{
				onClickBtnArchivia();
			}
		});			

		btn_conferma.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			public void onEvent(Event event){
				onClickBtnConferma();
			}
		});			
		
		btn_presacarico.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
			public void onEvent(Event event) throws Exception{
				onClickBtnPresacarico();
			}
		});
	}
	private void doMakeControlAfterRead() throws WrongValueException, Exception {

		//gestisco personalizzazioni
		gestisciPersonalizzazioni();
		
		// gestisco l'abilitazione/disabilitazione dei vari componenti, a seconda dello stato della maschera
		abilitazioniMaschera();
	}



	private void abilitazioniMaschera() throws Exception {
//		btn_patologie.setDisabled(true);
		if (isInInsert()){
			
			boolean proporreDtScheda = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_PROPORRE_DT_SCHEDA);
			if (proporreDtScheda){
				if (!ContattoInfFormCtrl.anagraficaChiusa(this.hParameters, data_richiesta, data_diag)){
					data_richiesta.setValue(procdate.getDate());
					data_diag.setValue(procdate.getDate());
				}
			}
			
			Hashtable h = new Hashtable();
			h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
			anagra = (ISASRecord)invokeGenericSuEJB(myEJB, h, "getInfoAnagrafica");
			if (anagra!=null && anagra.get("cod_distretto")!=null){
				String codice = anagra.get("cod_distretto").toString();
					cod_distretto.setSelectedValue(codice);
				}
			if (anagra.get("medico_desc")!=null){
				String codice = anagra.get("medico_desc").toString();
					medico_desc.setValue(codice);
				}
			if (anagra.get("cod_med")!=null){
				String codice = anagra.get("cod_med").toString();
					cod_med.setValue(codice);
				}
					
			btn_presacarico.setDisabled(true);
			btn_conferma.setDisabled(true);
			btn_archivia.setDisabled(true);
			trasporto_altro.setDisabled(true);
		} else if (isInUpdate() && stato_rich.getValue().equals("0")){
			btn_presacarico.setDisabled(false);
			btn_conferma.setDisabled(false);
			btn_archivia.setDisabled(false);
			trasporto_altro.setDisabled(false);
//			btn_patologie.setDisabled(false);
		}
		else if(isInUpdate() && stato_rich.getValue().equals("3")){
			this.setReadOnly(true);
		}else{	
			btn_presacarico.setDisabled(true);
			btn_conferma.setDisabled(true);
			btn_archivia.setDisabled(true);
			trasporto_altro.setDisabled(true);
		}
		Events.sendEvent(Events.ON_CHECK, sitfam, null);
		Events.sendEvent(Events.ON_CHECK, flag_trasporto, null);
		settaValoreFrequenza();
		quadroSanitarioMMGCtrl.eseguiOperazioniIniziali(this.self, false);
//		boolean casoCurePrestazionali = quadroSanitarioMMGCtrl.isCurePrestazionali(self);
		gestioneCampiCurePrestazionali();
		
		quadroSanitarioMMGCtrl.settaDatiObbligatori(this.self,false);
//		onSelectSitFam();	
	}

	private void gestisciPersonalizzazioni() throws Exception {		
	}


	@Override
	protected boolean doValidateForm() throws Exception {
		String punto = ver + "doValidateForm ";
		boolean canSave = true;
		logger.debug(punto + " salvataggio >>>");
		String dataAperturaCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.DATA_APERTURA)+"";
		
		if (!ManagerDate.confrontaDate(dataAperturaCartella, data_richiesta.getValueForIsas())){
			String lbprDataPuac = ManagerDecod.recuperaDescrizioneLabel(self, "lb_data_richiesta"); 
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
	
	private boolean isMessaggioControlloCoerenzaDate() throws Exception {
		String punto = ver + "isMessaggioControlloCoerenzaDate ";
		boolean messaggioCoerenzaPeriodo = true;
		if( (ManagerDate.validaData(data_inizio)&& !data_inizio.isDisabled() ) ){
			messaggioCoerenzaPeriodo = ManagerDate.controllaPeriodo(self, data_richiesta, data_inizio,"lb_data_richiesta","lb_dataInizio");
		}
		
		logger.trace(punto + " controllo coerenza periodo>>" + messaggioCoerenzaPeriodo +"< ");
		return messaggioCoerenzaPeriodo;
	}
	
	private void onClickBtnArchivia() throws Exception{
	
	if (CaribelSessionManager.getInstance().getIsasUser().canIUse(ChiaviISASSinssntWeb.ARCH_RIC,ChiaviISASSinssntWeb.INSE)){
		Messagebox.show(Labels.getLabel("RichiestaMMG.principale.conferma_annullamento"),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {	
							stato_rich.setValue(RmRichiesteMMGEJB.STATO_RICH_MMG_ARCHIVIATO);
							doSaveForm();
							doMakeControlAfterRead();
						}
					}
				});
	}else {
		UtilForUI.standardExclamation(Labels.getLabel("common.msg.noPermessi"));
        return;
	}
}
	
	public void onChangetipoFrequenza(){
		String punto = ver + "onChangetipoFrequenza ";
		String valoreCombo = tipoFrequenza.getSelectedValue(); 
		logger.trace(punto + " cambio tipo frequenza ");
		int numeroAccessi = 0;
		if (ISASUtil.valida(valoreCombo)){
			int frequenza = Integer.parseInt(valoreCombo); 	
			if (tipocura.getSelectedItem() != null ){
				if ( tipocura.getSelectedValue().equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
					numeroAccessi = AutorizzazioniMMGAdiFormCtrl.recuperaFequenzaAdi(frequenza);
				}else {
					numeroAccessi = AutorizzazioniMMGAdpFormCtrl.recuperaFrequenzaAdp(frequenza);
				}
			}
		}
		logger.trace(punto + "numero accessi recuperato>>"+ numeroAccessi+"<<");
		accessi_mmg.setValue(numeroAccessi);
		quadroSanitarioMMGCtrl.controlliSuAdp();
	}
	
	
	public void onChangeIntensitaAssistenziale(){
		String punto = ver +"onChangeIntensitaAssistenziale ";
		logger.debug(punto + "inizio ");
//		boolean casoCurePrestazionali = quadroSanitarioMMGCtrl.isCurePrestazionali(this.self);
		
		gestioneCampiCurePrestazionali();

		quadroSanitarioMMGCtrl.caricoFrequenza(tipocura, tipoFrequenza, true, accessi_mmg);
		quadroSanitarioMMGCtrl.settaDatiObbligatori(this.self,false);
		gestionePatologia();
		
	}

	public void gestioneCampiCurePrestazionali() {
		String punto = ver + "gestioneCampiCurePrestazionali ";
		boolean casoCurePrestazionali = quadroSanitarioMMGCtrl.isCurePrestazionali(this.self);
		boolean casoCureDomiciliari = quadroSanitarioMMGCtrl.isCureDomiciliari(this.self);
		boolean casoResidenziale = quadroSanitarioMMGCtrl.isCureResidenziali(this.self);
		
		logger.trace(punto + " curePrestazione>>" +casoCurePrestazionali);
		CaribelSearchCtrl patologie1 = (CaribelSearchCtrl) patologiadet1.getAttribute(MY_CTRL_KEY);
		patologie1.setRequired(casoCureDomiciliari);
		
//		if (casoCurePrestazionali){
//			if (ManagerDate.validaData(data_diag)){
//				dataDiag = data_diag.getValue();
//			}
//			data_diag.setValue(null);
//		}
		if (casoCureDomiciliari) {
			if(!ManagerDate.validaData(data_diag) && ManagerDate.validaData(dataDiag)){
				data_diag.setValue(dataDiag);
			}
		}
		data_diag.setRequired(casoCureDomiciliari|| casoResidenziale);
	}

	
	private void onClickBtnConferma(){
		if (CaribelSessionManager.getInstance().getIsasUser().canIUse(ChiaviISASSinssntWeb.CONF_RIC,ChiaviISASSinssntWeb.INSE)){
			
		
		Messagebox.show(Labels.getLabel("RichiestaMMG.principale.conferma_proroga"),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							Integer current_so = getCurrentSkSo();
							if (current_so!=null){
								id_scheda_so.setValue(current_so);
								stato_rich.setValue(RmRichiesteMMGEJB.STATO_RICH_MMG_CONFERMA);
								doSaveForm();
								doMakeControlAfterRead();
							return;
							}
							else  {
								Messagebox.show(
					    				Labels.getLabel("RichiestaMMG.principale.conferma_no_sk_attiva"),
					    				Labels.getLabel("messagebox.attention"),
					    				Messagebox.OK,
					    				Messagebox.EXCLAMATION);  
					              return;
							}
							}
					}
				});
		}else {
		UtilForUI.standardExclamation(Labels.getLabel("common.msg.noPermessi"));
        return;
	}
	}
	protected Integer getCurrentSkSo() throws Exception {
		
		ISASRecord current_id = (ISASRecord) invokeGenericSuEJB(new RMSkSOEJB(), (Hashtable)hParameters.clone(), "selectSkValCorrente");
		if (current_id!=null) return (Integer)current_id.get("id_skso");
		return null;
		
	}



	private void onClickBtnPresacarico() throws Exception{
		if (CaribelSessionManager.getInstance().getIsasUser().canIUse(ChiaviISASSinssntWeb.PC_RIC,ChiaviISASSinssntWeb.INSE)){
			
		if (esistePresaCarico()){
			Messagebox.show(Labels.getLabel("RichiestaMMG.principale.esiste_pc_attiva"),
					Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
						public void onEvent(Event event) throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())) {									
								doOpenSchedaSO(CostantiSinssntW.CAMBIA_PIANO);														
							}
						}
					});
		}
		else{
		Messagebox.show(Labels.getLabel("RichiestaMMG.principale.conferma_presacarico"),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {	
//							stato_rich.setValue("1");
//							doSaveForm();
							doMakeControlAfterRead();
							doOpenSchedaSO(CostantiSinssntW.PRESA_CARICO_RICHIESTA);														
						}
					}
				});
		}
	}else {
				UtilForUI.standardExclamation(Labels.getLabel("common.msg.noPermessi"));
          return;
	}
		

}

	private boolean esistePresaCarico() throws Exception {
		Hashtable h = new Hashtable();
		h.put("n_cartella", n_cartella.getValue().toString());
		ISASRecord dbr = (ISASRecord) invokeGenericSuEJB(new RMSkSOEJB(), h,"selectSkValCorrente");
		return (dbr != null && dbr.get("data_presa_carico_skso") != null);
	}

	protected void doOpenSchedaSO(int action) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put(CostantiSinssntW.CTS_ID_RICH, id_rich.getValue().toString());
		if (action != CostantiSinssntW.PRESA_CARICO_RICHIESTA){
			if (id_scheda_so!=null && id_scheda_so.getValue()!=null && !(id_scheda_so.getValue().intValue()==0)){
				map.put(CostantiSinssntW.CTS_ID_SKSO, id_scheda_so.getValue().toString());
			}
		}
		map.put(CostantiSinssntW.ACTION, new Integer(action));
		//caribelContainerCtrl.removeComponentsFrom(ContainerPuacCtrl.CTS_RICHIESTE_MMG);
		caribelContainerCtrl.removeComponentsFrom(ContainerPuacCtrl.CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA);
		caribelContainerCtrl.showComponent(ContainerPuacCtrl.CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA,SegreteriaOrganizzativaFormCtrl.CTS_FILE_ZUL, map);
	}


	@Override
	protected boolean doSaveForm() throws Exception {
		String punto = ver + "doSaveForm ";
		logger.trace(punto + " peridoSuperiore anno");
		if (!isMessaggioPeriodoSuperioreAnno()) {
			return false;
		}
		boolean curePrestazionali = quadroSanitarioMMGCtrl.isCurePrestazionali(this.self);
		boolean salvareDati = true;
		if (curePrestazionali){
			salvareDati = quadroSanitarioMMGCtrl.controlloDatiSanitari(this.self, "principale");
		}
		if (salvareDati){
			salvareDati = super.doSaveForm();
			doMakeControlAfterRead();
		}
		return salvareDati;
	}	
	
	
	private boolean isMessaggioPeriodoSuperioreAnno() throws Exception {
		String punto = ver + "isMessaggioPeriodoSuperioreAnno ";
		boolean messaggioPeriodoSuperioreAnno = true;
		if ((ManagerDate.validaData(data_inizio) && !data_inizio.isDisabled())
				&& (ManagerDate.validaData(data_fine) && !data_fine.isDisabled())) {
			int numeroGiorni = ManagerDate.getNumeroGiorniData(data_inizio, data_fine);

			logger.trace(punto + " data inserita numeroGiorni>" + numeroGiorni + "<<");
			if (numeroGiorni > 365) {
				logger.trace(punto + " data inserita ");
				String messaggio = Labels.getLabel("so.conferma.periodo.superioreAnno", new String[] {});

				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
						Messagebox.EXCLAMATION);
				return false;
			}
		}
		return messaggioPeriodoSuperioreAnno;
	}
	
	
	protected void doStampa(){
		String cart = n_cartella.getText();
		String rich = id_rich.getText();		
		
		String u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
				"?EJB=SINS_FORICHMMG"+
				"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
				"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
				"&METHOD=stampa"+
				"&n_cartella="+cart+
				"&id_rich="+rich+
				"&REPORT=richiesta_mmg.fo";
		it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
	}
}

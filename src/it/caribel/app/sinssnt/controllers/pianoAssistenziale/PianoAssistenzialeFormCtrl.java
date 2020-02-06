package it.caribel.app.sinssnt.controllers.pianoAssistenziale;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.controllers.CaribelAggiornaCtrl;
import it.caribel.app.sinssnt.controllers.agenda.AgendaPianSettFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.util.CompareHashtable;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.util.ISASUtil;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JInternalFrame;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

public class PianoAssistenzialeFormCtrl extends CaribelFormCtrl implements CaribelAggiornaCtrl{

	private static final long serialVersionUID = 1L;
	
	private static String IN_ELABORAZIONE = "0",
			DA_INVIARE = "1";


	private CaribelTextbox 	cod_operatore;   
	private CaribelCombobox 	desc_operatore;

	private String myKeyPermission = "PIANOASS";
    private final String pAccMyKeyPermission = "PIANOACC";
    private final String pVerMyKeyPermission = "PIANOVER";
	private PianoAssistEJB myEJB = new PianoAssistEJB();
	
	private CaribelListbox tablePrestazioni;
	private CaribelListbox grigliaAccessi;
	private CaribelListbox grigliaVerifiche;
	
	private Component operatore;
//	private Button btn_annullaSelezione;
	private Button btn_confermaSelezione_;
	
	/* elisa b 21/06/16*/
	private Button btn_aggiornaPianoAss;
	private Button btn_congelaPianoAss;
	private CaribelTextbox flagStato;
	private CaribelTextbox pa_motivo_chiusura;
	private Tabpanel areaInterv;
	private Tabpanel areaPianoAccessi;

	private CaribelSearchCtrl operatoreCtrl;
	
	protected String pathGridZul = "/web/ui/sinssnt/interventi/accessiGrid.zul";

	private ISASUser iu;

	private CaribelCombobox pi_freq;
	private CaribelCombobox skpa_complessita;

	private String gl_strTipoOperatore;

	private Component jPanelProgetto;
	private Component jPanelCompl;

	private String nomeLivLoad;

	private String gl_strUltimaDataChiusuraPianoPrec;

	private CaribelDatebox jCariDateTextFieldHiddenProg;

	private CaribelTextbox cartella;
	private CaribelTextbox JCariTextFieldNProgetto;
	private CaribelTextbox JCariTextFieldCodObiettivo;
	private CaribelTextbox JCariTextFieldNIntervento;
	private CaribelTextbox JCariTextFieldTipoOper;
	private CaribelDatebox JCariDateTextFieldPianoAss;
	
	//TODO VARIABILI

	private final int WAIT = 0;
    private final int INSERT = 1;
    private final int UPDATE_DELETE = 2;
    private final int CONSULTA = 3;

	private String[] gl_arrExistingDtVar;

	private Component protocoliProcedure;
	private Component principaleForm;
	private CaribelDatebox pa_data_chiusura;

	private int statoPianoAss;

    private boolean gl_booCaricoPrestazSelezionate = false;

	private CaribelDatebox pi_data_fine;

	private int statoPianoAcc;

	private int statoPianoVer;

	private Window dett;
	private Window verifiche;
	private CaribelDatebox pi_data_inizio;

	private Component prestazione;

	private CaribelTextbox pi_prest_cod;
	private CaribelCombobox pi_prest_desc;
	private CaribelTextbox JCariTextFieldProgHide;
	private CaribelIntbox pi_quantita;
	private CaribelTextbox pi_op_esecutore;
	private CaribelTextbox JCariTextFieldIndHide;
	
	private CompareHashtable hAccessi;
	private CompareHashtable hVerifiche;

	private CaribelDatebox int_data;

	private String nomeLivInfo = "PIANO_ASSIST";

	private PianoAccessiGridCRUDCtrl dettCtrl;
	private CaribelGridStateCtrl verificheCtrl;


	private Toolbarbutton btn_agenda;

	private Hashtable<String, String> newHtAccessi = new Hashtable<String, String>();

	private Tab 	 tabProtocolliProcedure;
	private Tabpanel panelProtocolliProcedure;
	private Tab 	 tabAccessi;
	private Tabpanel panelAccessi;
	
	private CaribelTextbox codPrestazione;
	private CaribelTextbox descPrestazione;
	
	private CaribelListModel modelloPrestazioni = new CaribelListModel();


	private boolean showSaveMessage = true;


	private Component pianificaGrid;


	private String ver = "1-";

	private boolean vengoDaPianificazionePai = false;
	
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			super.setMethodNameForQueryKey("queryKey_pianoAss");
			super.setMethodNameForQuery("query_pianoAss");
			super.setMethodNameForDelete("delete_pianoAss");
			super.setMethodNameForInsert("insert_all");
			super.setMethodNameForUpdate("update_pianoAss");
			//super.setMsgDeleteQuestion(Labels.getLabel("pianoAssistenziale.msg.cancellazione"));
			
			iu = CaribelSessionManager.getInstance().getIsasUser();	
			operatoreCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
			operatoreCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, UtilForContainer.getTipoOperatorerContainer());
			operatoreCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
			
			pi_freq =  (CaribelCombobox) self.getFellowIfAny("datiPiano",  true).getFellowIfAny("pi_freq",  true);
			
			caricaCombo();
			
			gl_strTipoOperatore = UtilForContainer.getTipoOperatorerContainer();
			//gb 20/09/07: Questi elementi sono visibili solo per gli infermieri
	        if (!gl_strTipoOperatore.equals("02")||ManagerProfile.isConfigurazioneMarche(getProfile())){
	        	jPanelProgetto.setVisible(false);
	          }
	        //gb 20/09/07: fine *******
	        logger.info("-->JFrameASPianoAsist/gl_strTipoOperatore: " + gl_strTipoOperatore);
	        
	        jPanelCompl.setVisible(ManagerProfile.isConfigurazioneLazio(getProfile()));

			dett = (Window) self.getFellowIfAny("datiPiano", true);
			dettCtrl= (PianoAccessiGridCRUDCtrl) dett.getAttribute(MY_CTRL_KEY);
//			if(tablePrestazioni==null){
				tablePrestazioni = (CaribelListbox) dett.getFellowIfAny("tablePrestazioni",  true);
//			}
//			if(btn_confermaSelezione==null){
//				btn_confermaSelezione = (Button) dett.getFellowIfAny("btn_confermaSelezione",  true);
//				btn_confermaSelezione.addEventListener(Events.ON_CLICK,  new EventListener<Event>() {
//															public void onEvent(Event event) throws Exception {
//																onConfermaSelezione(event);
//																return;
//															}});
//			}
	        
			if(btn_agenda==null){
				btn_agenda = (Toolbarbutton) dett.getFellowIfAny("btn_agenda",  true);
				btn_agenda.addEventListener(Events.ON_CLICK,  new EventListener<Event>() {
															public void onEvent(Event event) throws Exception {
																onOpenAgenda(event);
																return;
															}});
			}
			
			verifiche = (Window) self.getFellowIfAny("datiVerifiche", true);
			verificheCtrl= (CaribelGridStateCtrl) verifiche.getAttribute(MY_CTRL_KEY);

			if(int_data==null){
				int_data = (CaribelDatebox) verifiche.getFellowIfAny("int_data",  true);
//				UtilForComponents.linkDatebox(JCariDateTextFieldPianoAss, int_data);
			}
			
			grigliaAccessi   =  (CaribelListbox) dett.getFellowIfAny(CaribelGridStateCtrl.GRIGLIA);
			grigliaVerifiche =  (CaribelListbox) verifiche.getFellowIfAny(CaribelGridStateCtrl.GRIGLIA);
			grigliaAccessi.addEventListener(Events.ON_SELECT,  new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					hAccessi  = new CompareHashtable(UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true), null, false, true));
					return;
				}});			
	        
	        pi_data_inizio = (CaribelDatebox) dett.getFellowIfAny("pi_data_inizio", true); 
	        prestazione = dett.getFellowIfAny("prestazione", true);
			pi_data_fine =  (CaribelDatebox) dett.getFellowIfAny("pi_data_fine");
			pi_quantita = (CaribelIntbox) dett.getFellowIfAny("pi_quantita");
			pi_op_esecutore = (CaribelTextbox) dett.getFellowIfAny("pi_op_esecutore");
			pi_prest_cod= (CaribelTextbox) dett.getFellowIfAny("pi_prest_cod");
			pi_prest_desc= (CaribelCombobox) dett.getFellowIfAny("pi_prest_desc");
			JCariTextFieldProgHide=(CaribelTextbox) dett.getFellowIfAny("JCariTextFieldProgHide");
//			JCariTextFieldIndHide=(CaribelTextbox) dett.getFellowIfAny("JCariTextFieldIndHide");
	        
			if(codPrestazione==null)
				codPrestazione= (CaribelTextbox) dett.getFellowIfAny("codPrestazione");
			if(descPrestazione==null)
				descPrestazione= (CaribelTextbox) dett.getFellowIfAny("descPrestazione");

			loadDati2nd("PIANO_ASSIST");
			UtilForComponents.linkDatebox(JCariDateTextFieldPianoAss, pi_data_inizio);
			//la griglia viene disabilitata elemento per elemnto, devo farlo per forza qui dopo che è stata caricata.
			UtilForComponents.disableListBox(tablePrestazioni, true);
			
			if(ManagerProfile.isConfigurazioneMarche(getProfile())){
				tabProtocolliProcedure.setVisible(false);
				panelProtocolliProcedure.setVisible(false);
			}

			codPrestazione.addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public void onEvent(Event event){
					try{
						if(event.getName().equals(Events.ON_CHANGING)){
							((InputElement) event.getTarget()).setRawValue(((InputEvent)event).getValue().toUpperCase());
							Collection sele = modelloPrestazioni.getSelection();
							Collection sel2 = tablePrestazioni.getSelectedItems();
							Set prestazioni = new TreeSet<String>();
							for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
								Listitem litem = (Listitem) iterator.next();
								String cod_prest = (String)((CaribelListModel) tablePrestazioni.getModel()).getFromRow(litem, "pre_cod_prest");
								prestazioni.add(cod_prest);
							}
							Collection col = getPrestazioni();
							CaribelListModel mod = new CaribelListModel(col);
							tablePrestazioni.setModel(mod);

							Set tmp = new HashSet(tablePrestazioni.getSelectedItems());

							for (Iterator iterator = prestazioni.iterator(); iterator.hasNext();) {
								String codice = (String) iterator.next();
								if (codice != null) {
									//ricerco il codice nella griglia
									Hashtable hTrova = new Hashtable();
									hTrova.put("pre_cod_prest", codice);
									int riga = mod.columnsContains(hTrova);
									if (riga != -1) {
										logger.trace("Selezione della prestazione: "+codice);
										Object o = mod.remove(riga);
										mod.add(0, o);
										tmp.add(tablePrestazioni.getItemAtIndex(0));
									}
								}
							}//fine for
							mod.setMultiple(true);
							tablePrestazioni.setSelectedItems(tmp);
							tablePrestazioni.invalidate();
						}
						
					}catch(Exception e){
						doShowException(e);
					}
				}});
			descPrestazione.addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public void onEvent(Event event){
					try{
						if(event.getName().equals(Events.ON_CHANGING)){
							((InputElement) event.getTarget()).setRawValue(((InputEvent)event).getValue().toUpperCase());
							Collection sele = modelloPrestazioni.getSelection();
							Collection sel2 = tablePrestazioni.getSelectedItems();
							Set prestazioni = new TreeSet<String>();
							for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
								Listitem litem = (Listitem) iterator.next();
								String cod_prest = (String)((CaribelListModel) tablePrestazioni.getModel()).getFromRow(litem, "prest_cod");
								prestazioni.add(cod_prest);
							}
							Collection col = getPrestazioni();
							CaribelListModel mod = new CaribelListModel(col);
							tablePrestazioni.setModel(mod);

							Set tmp = new HashSet(tablePrestazioni.getSelectedItems());

							for (Iterator iterator = prestazioni.iterator(); iterator.hasNext();) {
								String codice = (String) iterator.next();
								if (codice != null) {
									//ricerco il codice nella griglia
									Hashtable hTrova = new Hashtable();
									hTrova.put("prest_cod", codice);
									int riga = mod.columnsContains(hTrova);
									if (riga != -1) {
										logger.trace("Selezione della prestazione: "+codice);
										Object o = mod.remove(riga);
										mod.add(0, o);
										tmp.add(tablePrestazioni.getItemAtIndex(0));
									}
								}
							}//fine for
							mod.setMultiple(true);
							tablePrestazioni.setSelectedItems(tmp);
//							tablePrestazioni.invalidate();
						}
					}catch(Exception e){
						doShowException(e);
					}
				}});
			
//			doFreezeForm();
			
			/* elisa b */
			/* elisa b */
			boolean isModificabile = 
					flagStato.getValue().equals("") || //elisa b 05/08/16 (caso insert nuovo piano)
					flagStato.getValue().equals(IN_ELABORAZIONE);			
			gestioneBottoniAggiornaCongela(isModificabile);

			
		}catch(Exception e){
			doShowException(e);
		}
	}
	

	private void caricaCombo() throws Exception {
		
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();

		h_xCBdaTabBase.put("FREQAC",   pi_freq);
		h_xCBdaTabBase.put("COMPLASS", skpa_complessita);

		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val", "tab_descrizione", false);
	}

	public Boolean loadDati2nd(String nomeLivelloLoad) throws Exception {
		this.nomeLivLoad = nomeLivelloLoad;// 25/01/07

		Hashtable h_pianoAss = (Hashtable) this.caribelContainerCtrl.hashChiaveValore.get("h_pianoAss");
		int ret = 0;
		int statoAp = 0;
		
		String numCart, numProg, codObiet, numInterv;
		String dataPiano;
		if(dbrFromList!=null){
			 numCart = ((Integer)dbrFromList.get(CostantiSinssntW.N_CARTELLA)).toString();   
			 numProg = ((Integer)dbrFromList.get(CostantiSinssntW.N_PROGETTO)).toString();                 
			 codObiet = (String)dbrFromList.get(CostantiSinssntW.COD_OBBIETTIVO);            
			 numInterv = ((Integer)dbrFromList.get(CostantiSinssntW.N_INTERVENTO)).toString();      
			 dataPiano = UtilForBinding.getValueForIsas((Date) dbrFromList.get("pa_data"));
		}else{
			numCart = (String)h_pianoAss.get(CostantiSinssntW.N_CARTELLA);
			numProg = (String)h_pianoAss.get(CostantiSinssntW.N_PROGETTO);              
			codObiet = (String)h_pianoAss.get(CostantiSinssntW.COD_OBBIETTIVO);         
			numInterv = (String)h_pianoAss.get(CostantiSinssntW.N_INTERVENTO);
			dataPiano = (String)h_pianoAss.get("pa_data");
		}
		//gb 08/08/07 *******
		gl_strUltimaDataChiusuraPianoPrec = (String)h_pianoAss.get("ultima_data_chiusura");
		//gb 08/08/07: fine *******
		//gb 14/11/06 *
		String strDtApeIntervento = (String)h_pianoAss.get("int_data_ins");
		if (strDtApeIntervento != null)
			jCariDateTextFieldHiddenProg.setValue(UtilForBinding.getDateFromIsas(strDtApeIntervento));
		// ************

		String tpOper = UtilForContainer.getTipoOperatorerContainer(); //(String)h_pianoAss.get("pa_tipo_oper");
		Integer iStatoAp = (Integer)h_pianoAss.get("stato");
		if (iStatoAp != null)
			statoAp = iStatoAp.intValue();

		if (numCart == null) {
			logger.info("JFrameASPianoAssist.loadDati2nd - CARTELLA NON VALIDA!!");
			return new Boolean(false);
		}
		cartella.setValue(numCart);

		//gb 10/08/07 *******
		if (numProg == null){
			if (gl_strTipoOperatore.equals("01"))
				logger.info("JFrameASPianoAssist.loadDati2nd - PROGETTO NON VALIDO!!");
			else
				logger.info("JFrameASPianoAssist.loadDati2nd - CONTATTO NON VALIDO!!");
			return new Boolean(false);
		}else
			JCariTextFieldNProgetto.setValue(numProg);
		//gb 10/08/07: fine *******

		if (codObiet == null){
			logger.info("JFrameASPianoAssist.loadDati2nd - OBIETTIVO NON VALIDO!!");
			return new Boolean(false);
		}
		
		JCariTextFieldCodObiettivo.setValue(codObiet);

		if (numInterv == null){
			logger.info("JFrameASPianoAssist.loadDati2nd - INTERVENTO NON VALIDO!!");
			return new Boolean(false);
		}
		JCariTextFieldNIntervento.setValue(numInterv);

		if (tpOper == null) {
			logger.info("JFrameASPianoAssist.loadDati2nd - TIPO OPER NON VALIDO!!");
			return new Boolean(false);
		}
		JCariTextFieldTipoOper.setValue(tpOper);

		if (statoAp != this.INSERT){
			if (dataPiano == null) {
				logger.info("JFrameASPianoAssist.loadDati2nd - DATA PIANO ASSIST. NON VALIDO!!");
				return new Boolean(false);
			}
			UtilForBinding.setDateforCaribelDatebox(JCariDateTextFieldPianoAss, dataPiano);
			doWriteComponentsToBean();
			ret = execSelectPianoAss();
			doWriteBeanToComponents();
			doFreezeForm();
			if(pa_data_chiusura.getValue()!=null){
//				this.setReadOnly(true);
			}
		} else {
			Date newDate = new Date();
			if(gl_strUltimaDataChiusuraPianoPrec!= null && !gl_strUltimaDataChiusuraPianoPrec.isEmpty()){
				Date lastCosed = UtilForBinding.getDateFromIsas(gl_strUltimaDataChiusuraPianoPrec);
				Calendar cal = Calendar.getInstance();
				cal.setTime(lastCosed);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				JCariDateTextFieldPianoAss.setConstraint("no future, after "+ UtilForComponents.formatDateforDatebox(cal.getTime()));
				JCariDateTextFieldPianoAss.invalidate();
			}
			JCariDateTextFieldPianoAss.setValue(newDate);
			//gb 20/09/07: Solo per infermieri
			if (gl_strTipoOperatore.equals("02"))
				setDefaultChkBoxInfermieri();
			//gb 20/09/07: fine *******
		}
		gl_arrExistingDtVar = (String[])h_pianoAss.get("arrCodEsistenti");

//		//gb 31/08/07 ---
//		Hashtable h_datiAss = (Hashtable)this.myContainer.invocaMetodo("getDatiAssistito");
//		this.JCariTextFieldAss.setText((String)h_datiAss.get("cognome")+" "+(String)h_datiAss.get("nome"));
//		logger.info("loadDati2nd / h_datiAss: " + h_datiAss.toString());
//		//gb 31/08/07 ---

		 //gb 31/08/07
		if (tpOper.equals("01")){
			if(opeLoggatoIsOpeAbilitato())
				setStatoPianoAss(statoAp);
			else
				setStatoPianoAss(CONSULTA);
		}
		else //gb 31/08/07
			setStatoPianoAss(statoAp); //gb 31/08/07

		boolean isPianificatoPAI = false;
		Date nextDate = checkPianificatoPAI(numCart);
		Date aperturaContatto = jCariDateTextFieldHiddenProg.getValue();
		isPianificatoPAI = nextDate!=null;
		if(nextDate == null || (aperturaContatto!=null && aperturaContatto.after(nextDate))){
			nextDate = aperturaContatto;
		}
		if(isPianificatoPAI && isInInsert()){
			Date lastClosed = null;
			if(gl_strUltimaDataChiusuraPianoPrec!= null && !gl_strUltimaDataChiusuraPianoPrec.isEmpty()){
				lastClosed = UtilForBinding.getDateFromIsas(gl_strUltimaDataChiusuraPianoPrec);
			}
			if(lastClosed!=null && lastClosed.after(nextDate)){
				Calendar cal = Calendar.getInstance();
				cal.setTime(lastClosed);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				lastClosed = cal.getTime();
			}else{
				lastClosed = nextDate;
			}
			JCariDateTextFieldPianoAss.setValue(lastClosed);
//			String 
//			cod_operatore.setValue((String) this.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.COD_OPERATORE_REF));
//			Events.sendEvent(Events.ON_CHANGE, cod_operatore, null);
			setOperatoreReferente();

			vengoDaPianificazionePai = true;
			doSaveForm();
			salvaPianoAccessi(currentIsasRecord);
			vengoDaPianificazionePai = false;
			execSelectPianoAss();
			tabAccessi.getTabbox().setSelectedTab(tabAccessi);
			doWriteBeanToComponents();
//			doQueryKeySuEJB();
		}
		
//		cHPianoAss = new compareHashtable(ctcPianoAss.getHashtableKeyValue());
		
		logger.info("-->loadDati2nd/gl_strUltimaDataChiusura: " + gl_strUltimaDataChiusuraPianoPrec);
		//gb 07/08/07 *******
		this.ExecSelectGrigliaPrestazioni();
		doFreezeForm();
//		settaRendererTabellaGrigliaPrestazioni();
//		settaRendererTabella();
		//gb 07/08/07: fine *******

		return new Boolean(ret >= 0);
	}


	private void setOperatoreReferente() {
		String codiceOperatore = (String) this.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.COD_OPERATORE_REF);
		if(codiceOperatore != null && !codiceOperatore.isEmpty()){
			cod_operatore.setValue(codiceOperatore);
			Events.sendEvent(Events.ON_CHANGE, cod_operatore, codiceOperatore);
		}else{
			cod_operatore.setValue(getProfile().getStringFromProfile("codice_operatore"));
			desc_operatore.setValue(getProfile().getStringFromProfile("cognome_operatore") + " " + getProfile().getStringFromProfile("nome_operatore"));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void salvaPianoAccessi(ISASRecord currentIsasRecord) throws Exception {
		Hashtable h = new Hashtable();
		h.putAll(currentIsasRecord.getHashtable());
		h.put(CostantiSinssntW.TIPO_OPERATORE, UtilForContainer.getTipoOperatorerContainer());
		h.put("pa_data", JCariDateTextFieldPianoAss.getValueForIsas());
		h.put(CostantiSinssntW.COD_OPERATORE, cod_operatore.getValue());
		invokeGenericSuEJB(new RMSkSOEJB(), h, "copiaPAIinPianoAccessi");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Date checkPianificatoPAI(String numCart) throws Exception {
		if(this.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.CTS_ID_SKSO)!=null){
			Hashtable h = new Hashtable();
			h.put(CostantiSinssntW.N_CARTELLA, numCart);
			h.put(CostantiSinssntW.CTS_ID_SKSO, this.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.CTS_ID_SKSO));
			h.put(CostantiSinssntW.TIPO_OPERATORE, UtilForContainer.getTipoOperatorerContainer());
			h.put("data_richiesta", UtilForBinding.getValueForIsas(new Date()));
			return (Date) invokeGenericSuEJB(new RMSkSOEJB(), h, "isPianificatoPAIDate");
		}else{
			return null;
		}
	}

	//gb 07/08/07: *******
	public int ExecSelectGrigliaPrestazioni() throws Exception{
		//griglia prestazioni
		Hashtable h = new Hashtable();
		h.put("referente",gl_strTipoOperatore);
		h.put("PianoAssistenziale","SI");
		Object o= invokeGenericSuEJB(new AgendaEJB(), h, "CaricaTabellaPrestazioni");
		Vector griglia=new Vector();
		if (o!=null){
			if(((Vector)o).size()<=0){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
				return -1;
			}
			else griglia=(Vector)o;
		}
		CaribelListModel modelTable = new CaribelListModel(griglia);
		modelTable.setMultiple(true);
		modelloPrestazioni = modelTable;
		tablePrestazioni.setModel(modelTable);
		tablePrestazioni.invalidate();
//		Enumeration en=griglia.elements();
//		while(en.hasMoreElements())
//		{
//			ISASRecord is =(ISASRecord)en.nextElement();
//			Hashtable ht=is.getHashtable();
//			//logger.info("carico pannello in hash=="+ht.toString());
//			caricaTabPrestazioni(ht);
//		}
//		tablePrestazioni.setSizedByContent(true);
//		tablePrestazioni.invalidate();
		return 1;
	}

	//gb 07/08/07: *******
	private int caricaTabPrestazioni(Hashtable hash){
		int count=0;
		StringBuffer st = new StringBuffer();
		String chiavi = "##";//cariObjectTableModel1.getColumnDB("##");
		StringTokenizer rowChiavi = new StringTokenizer(chiavi,"##");
		while(rowChiavi.hasMoreTokens()){
			String rowItem = rowChiavi.nextToken();
			if(hash.containsKey(rowItem)){
				if(((String)hash.get(rowItem)).equals(""))
					st.append(" ");
				else
					st.append((String) hash.get(rowItem));
			}else {
				//la tabella non contiene la chiave
				st.append(" ");
			}
			if (rowChiavi.hasMoreTokens())
				st.append("##");
		}//end while
//		cariObjectTableModel1.addRow(st.toString());
		//logger.info("carico su griglia:"+st.toString());
		return 1;
	}//end caricaPrest
	//gb 07/08/07: fine *******<
	
	// set stato "principale"
	private void setStatoPianoAss(int state) throws Exception{
		logger.info("setStatoPianoAss, state: " + state);
		if(pa_data_chiusura.getValue() != null){
			state = this.CONSULTA;
		}
		if (state == this.INSERT) {
			btn_print.setDisabled(true);
			doCheckPermission();

			setOperatoreReferente();

			//  	            this.JCariDateTextFieldPianoAss.setValue(getProfile().getStringFromProfile("data_sistema"));
			this.JCariDateTextFieldPianoAss.focus();

			// avendo posto il salvataggio in comune a seguito di modifiche dei campi
			UtilForBinding.setComponentReadOnly(principaleForm, !iu.canIUse(myKeyPermission,"INSE"));

			//gb 02/08/07 *******
			if(pa_data_chiusura.getValue() == null){
				pa_data_chiusura.setDisabled(false);
			}else{
				pa_data_chiusura.setDisabled(true);
			}
			//gb 02/08/07: fine *******

		}

		if (state==this.UPDATE_DELETE) {
			btn_print.setDisabled(false);
			// 27/04/11 ---
			try {
				btn_save.setDisabled(!iu.canIUse(myKeyPermission, "MODI")
						&& iu.canIWrite(currentIsasRecord));
			} catch (Exception e) {
				logger.error("ERRORE " + e);
				btn_save.setDisabled(!false);
			}

			try {
				btn_delete.setDisabled(!iu.canIUse(myKeyPermission, "CANC")
						&& iu.canIDelete(currentIsasRecord));
			} catch (Exception e) {
				logger.error("ERRORE " + e);
				btn_delete.setDisabled(!false);
			}
			// 27/04/11 ---

			this.JCariDateTextFieldPianoAss.setDisabled(!false);
			cod_operatore.focus();

			// avendo posto il salvataggio in comune a seguito di modifiche dei campi
			UtilForBinding.setComponentReadOnly(principaleForm, !iu.canIUse(myKeyPermission,"MODI"));

			//gb 31/08/07 *******
			if (!ManagerProfile.isConfigurazioneLazio(getProfile())) { // 27/04/11
				operatoreCtrl.setReadonly(true); 
			}
			//gb 31/08/07: fine *******

			//gb 02/08/07 *******
			if(pa_data_chiusura.getValue() == null){
				pa_data_chiusura.setDisabled(false);
			}else{
				pa_data_chiusura.setDisabled(true);
			}
			//gb 02/08/07: fine *******
		}

		if (state==this.CONSULTA){
			logger.info("state == CONSULTA");
			this.setReadOnly(true);
			// 17/11/08 ---
//			btn_print.setDisabled(!true);
//			btn_save.setDisabled(!false);
//			btn_delete.setDisabled(!false);
//			btn_undo.setDisabled(!false);
			logger.info("setStatoPianoAss/ JCariDateTextFieldPianoAss.isDisabled(): " + JCariDateTextFieldPianoAss.isDisabled());
		}

		statoPianoAss = state;
		// set stato "secondari"

		//gb 14/11/07 il metodo setDefaultPianoAcc() non lo chiamo se
		//            provengo in qualche modo dal metodo caricaPrestazioniSelezionate()
		if (!gl_booCaricoPrestazSelezionate) //gb 14/11/07
			setDefaultPianoAcc();

		logger.info("setStatoPianoAss (2)/ JCariDateTextFieldPianoAss.isDisabled(): " + JCariDateTextFieldPianoAss.isDisabled());
		setDefaultPianoVer();
		logger.info("setStatoPianoAss (3)/ JCariDateTextFieldPianoAss.isDisabled(): " + JCariDateTextFieldPianoAss.isDisabled());
	}

    private void setDefaultPianoAcc()
	{
//        utl.setDefault(this.jPanelDettAcc);
//        this.jCariComboBoxFreq.setSelectedIndex(-1);
//        rigaApertaPianoAcc = -1;
        setStatoPianoAcc(this.WAIT);
//        cHPianoAcc = new compareHashtable(ctcPianoAcc_xCmp.getHashtableKeyValue());
    }

    private void setStatoPianoAcc(int state){
    	logger.info("setStatoPianoAcc=="+state);
    	int state_old=state;
    	//TODO verificare se va qui o nel controller della griglia
//    	this.JCariActionButtonAgenda.setEnabled(false);
////    	this.JActionButtonCalendar.setEnabled(false);
//    	pi_data_fine.setRequired(false);
//    	String abil = getProfile().getStringFromProfile("abil_agen");
//    	boolean isAbilxAgenda = ((iu.canIUse("AGENDA"))
//    			&& ((abil != null) && (abil.trim().equals("SI")))); // 23/10/04
//    	if (statoPianoAss == this.CONSULTA) {
//    		state = this.CONSULTA;
//    		logger.info("setStatoPianoAcc==CONSULTA");
//    	}
//    	if (state == this.WAIT){
//    		if(pa_data_chiusura.getValue()!=null){
//    			this.newActionPAcc.setEnabled(false);
//    			btn_confermaSelezione.setDisabled(true); //gb 09/08/07
//    		}else{
//    			this.newActionPAcc.setEnabled(iu.canIUse(pAccKeyPermission,"INSE"));
//    			btn_confermaSelezione.setDisabled(false); //gb 09/08/07
//    		}
//    		this.openActionPAcc.setEnabled(iu.canIUse(pAccKeyPermission,"CONS"));
//    		this.saveActionPAcc.setEnabled(false);
//    		this.deleteActionPAcc.setEnabled(false);
//    		this.undoActionPAcc.setEnabled(false); //gb 02/08/07
//
//    		this.JCariActionButtonAgenda.setEnabled(false); //gb 03/08/07
//    		this.JActionButtonCalendar.setEnabled(false); //gb 06/08/07
//
//    		this.jCariComboBoxFreq.setSelectedIndex(-1);
//    		rigaApertaPianoAcc = -1;
//    	}
//
//    	if (state == this.INSERT){
//    		utl.Enable(this.jPanelDettAcc,true);
//    		enablePanelOperatore(true);
//    		this.newActionPAcc.setEnabled(false);
//    		this.openActionPAcc.setEnabled(false);
//    		this.deleteActionPAcc.setEnabled(false);
//    		this.undoActionPAcc.setEnabled(true);
//    		this.JCariDateTextFieldDataIni.requestFocus();
//    		// avendo posto il salvataggio in comune a seguito di modifiche dei campi
//    		utl.Enable(jPanelPianoAcc, (iu.canIUse(pAccKeyPermission,"INSE")));
//    		if(pa_data_chiusura.getValue()!=null){
//    			saveActionPAcc.setEnabled(false);
//    			btn_confermaSelezione.setEnabled(false);
//    		}else{
//    			saveActionPAcc.setEnabled(iu.canIUse(pAccKeyPermission,"INSE"));
//    			btn_confermaSelezione.setEnabled(true);
//    		}
//    		this.JCariActionButtonAgenda.setEnabled(false);//2012-08-07      	isAbilxAgenda);
//    		this.JActionButtonCalendar.setEnabled(isAbilxAgenda);
//    		this.JCariTextFieldCodOpEsecRif1.setValue(this.JCariTextFieldCodOpEsecRif.getValue());
//    		this.JCariTextFieldDescOpEsecRif1.setValue(this.JCariTextFieldDescOpEsecRif.getValue());
//    		if (!(this.JCariTextFieldDescOpEsecRif1.getValue().equals("")))
//    			this.JCariTextFieldDescOpEsecRif1.setCaretPosition(1);
//    	}
//
//    	if (state == this.UPDATE_DELETE){
//    		utl.Enable(this.jPanelDettAcc,!isAbilxAgenda);//bargi 11/06/2012 non è modificabile
//    		this.newActionPAcc.setEnabled(false);
//    		this.openActionPAcc.setEnabled(false);
//    		this.undoActionPAcc.setEnabled(true);
//    		if(pa_data_chiusura.getValue()!=null){
//    			saveActionPAcc.setEnabled(false);
//    			deleteActionPAcc.setEnabled(false);
//    			btn_confermaSelezione.setEnabled(false);
//    			utl.Enable(jPanelPianoAcc, false);
//    		}else{
//    			try {
//    				this.saveActionPAcc.setEnabled(iu.canIUse(pAccKeyPermission, "MODI")
//    						&& iu.canIWrite((ISASRecord) ctcPianoAcc.getDBRecordForUpdate()));
//    			} catch (Exception e) {
//    				logger.info("ERRORE " + e);
//    				this.saveActionPAcc.setEnabled(false);
//    			}
//    			try {
//    				this.deleteActionPAcc.setEnabled(iu.canIUse(pAccKeyPermission, "CANC")
//    						&& iu.canIDelete((ISASRecord) ctcPianoAcc.getDBRecordForUpdate()));
//    			} catch (Exception e) {
//    				logger.error("ERRORE " + e);
//    				this.deleteActionPAcc.setEnabled(false);
//    			}
//    			utl.Enable(jPanelPianoAcc, (iu.canIUse(pAccKeyPermission,"MODI")));
//    			btn_confermaSelezione.setEnabled(!isAbilxAgenda&&iu.canIUse(pAccKeyPermission,"MODI"));
//    		}
//    		//gb 03/08/07: fine *******
//
//    		jCariTablePianoAcc.setEnabled(true); //gb 09/08/07
//    		//  this.JCariDateTextFieldDataIni.requestFocus();
//    		//          jPanelDettAcc
//    		utl.Enable(jPanelDettAcc, (iu.canIUse(pAccKeyPermission,"MODI"))&&!isAbilxAgenda);
//    		this.JCariActionButtonAgenda.setEnabled(isAbilxAgenda); //gb 03/08/07
//    		this.JActionButtonCalendar.setEnabled(isAbilxAgenda); //gb 06/08/07
//    		// avendo posto il salvataggio in comune a seguito di modifiche dei campi
//    		enablePanelOperatore(false);
//    		pi_data_fine.requestFocus();
//    		if(JCariDateTextFieldDataFin.IsVoid()) {//TODO bargi 09/04/2013
//    			pi_data_fine.setEnabled(true);
//    			if(isAbilxAgenda)pi_data_fine.setValid(true);
//    		}else if(dt.confrontaDate(JCariDateTextFieldDataFin.getValue(), data_odierna)==1) {//dt1>dt2
//    			pi_data_fine.setEnabled(true);
//    			pi_data_fine.setEditable(true);
//    		}
//    	}
//    	if (state == this.CONSULTA) {
//    		utl.Enable(this.jPanelPianoAcc,true);
//    		utl.Enable(this.jPanelDettAcc,false);
//    		this.newActionPAcc.setEnabled(false);
//    		this.openActionPAcc.setEnabled(iu.canIUse(pAccKeyPermission,"CONS"));
//    		this.saveActionPAcc.setEnabled(false);
//    		this.deleteActionPAcc.setEnabled(false);
//    		this.undoActionPAcc.setEnabled(true);
//    		utl.Enable(JPanelPrestaz, false);
//    		// JCariDateTextFieldPianoAss.enable(false);
//    		JCariDateTextFieldPianoAss.setEnabled(false);
//    		if(state_old==UPDATE_DELETE) {
//    			this.JCariActionButtonAgenda.setEnabled(isAbilxAgenda); //gb 03/08/07
//    			this.JActionButtonCalendar.setEnabled(isAbilxAgenda); //gb 06/08/07
//    		}
//    	}

    	statoPianoAcc = state;
    }

    private void setDefaultPianoVer()
	{
//        utl.setDefault(this.jPanelDettVer);
        setStatoPianoVer(this.WAIT);
//        cHPianoVer = new compareHashtable(ctcPianoVer_xCmp.getHashtableKeyValue());
    }

    private void setStatoPianoVer(int state){
        if (statoPianoAss == this.CONSULTA)
            state = this.CONSULTA;
    	//TODO verificare se va qui o nel controller della griglia

//        if (state == this.WAIT){
//        	utl.Enable(this.jPanelDettVer,false);
//
//        	//gb 08/08/07 *******
//        	if(pa_data_chiusura.getValue()!=null){
//        		this.newActionPVer.setDisabled(!false);
//        	}else{
//        		this.newActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPAcc,"INSE"));
//        	}
//        	//gb 08/08/07: fine *******
//
//        	//gb 08/08/07            this.newActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"INSE"));
//        	this.openActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"CONS"));
//        	this.saveActionPVer.setDisabled(!false);
//        	this.deleteActionPVer.setDisabled(!false);
//        	this.undoActionPVer.setDisabled(!false); //gb 02/08/07
//
//        	rigaApertaPianoVer = -1;
//        }
//
//        if (state == this.INSERT){
//        	utl.Enable(this.jPanelDettVer,true);
//
//        	this.newActionPVer.setDisabled(!false);
//        	this.openActionPVer.setDisabled(!false);
//        	//gb 08/08/07	        this.saveActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"INSE"));
//        	this.deleteActionPVer.setDisabled(!false);
//        	this.undoActionPVer.setDisabled(!true); //gb 08/08/07
//
//        	this.JCariDateTextFieldPianoVer.setDisabled(!true);
//        	this.JCariDateTextFieldPianoVer.setEditable(true);
//        	this.JCariDateTextFieldPianoVer.requestFocus();
//
//        	// avendo posto il salvataggio in comune a seguito di modifiche dei campi
//        	utl.Enable(jPanelVerifica, (iu.canIUse(this.nomeFinestraPVer,"INSE")));
//
//        	//gb 08/08/07 *******
//        	if(pa_data_chiusura.getValue()!=null){
//        		saveActionPVer.setDisabled(!false);
//        	}else{
//        		saveActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPAcc,"INSE"));
//        	}
//        	//gb 08/08/07: fine *******
//        }
//
//        if (state == this.UPDATE_DELETE){
//        	utl.Enable(this.jPanelDettVer,true);
//
//        	this.newActionPVer.setDisabled(!false);
//        	this.openActionPVer.setDisabled(!false);
//        	//gb 08/08/07	        this.saveActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"MODI"));
//        	//gb 08/08/07	        this.deleteActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"CANC"));
//        	this.undoActionPVer.setDisabled(!true); //gb 08/08/07
//
//        	//gb 08/08/07 *******
//        	if(pa_data_chiusura.getValue()!=null){
//        		saveActionPVer.setDisabled(!false);
//        		deleteActionPVer.setDisabled(!false);
//        		utl.Enable(jPanelVerifica, false);
//        	}else{
//        		/** 27/04/11
//	              saveActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPAcc,"MODI"));
//                  deleteActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPAcc,"CANC"));
//        		 **/
//        		// 27/04/11 ---
//        		try {
//        			this.saveActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer, "MODI")
//        					&& iu.canIWrite((ISASRecord) ctcPianoVer.getDBRecordForUpdate()));
//        		} catch (Exception e) {
//        			logger.info("ERRORE " + e);
//        			this.saveActionPVer.setDisabled(!false);
//        		}
//
//        		try {
//        			this.deleteActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"CANC")
//        					&& iu.canIDelete((ISASRecord) ctcPianoVer.getDBRecordForUpdate()));
//        		} catch (Exception e) {
//        			logger.info("ERRORE " + e);
//        			this.deleteActionPVer.setDisabled(!false);
//        		}
//        		// 27/04/11 ---
//
//        		utl.Enable(jPanelVerifica, (iu.canIUse(this.nomeFinestraPVer,"MODI")));
//        	}
//        	//gb 08/08/07: fine *******
//
//        	this.JCariDateTextFieldPianoVer.setDisabled(!false);
//        	this.JCariDateTextFieldPianoVer.setEditable(false);
//        	this.jCariTextPaneVerTesto.requestFocus();
//
//        	// avendo posto il salvataggio in comune a seguito di modifiche dei campi
//        	//gb 08/08/07            utl.Enable(jPanelVerifica, (iu.canIUse(this.nomeFinestraPVer,"MODI")));
//        }
//
//        if (state == this.CONSULTA) {
//        	utl.Enable(this.jPanelVerifica,true);
//        	utl.Enable(this.jPanelDettVer,false);
//        	// 17/11/08: x abilitare scroll ---
//        	utl.Enable(jScrollPaneVerTesto,true);
//        	jCariTextPaneVerTesto.setEditable(false);
//        	// 17/11/08 ---
//
//        	this.newActionPVer.setDisabled(!false);
//        	this.openActionPVer.setDisabled(!iu.canIUse(this.nomeFinestraPVer,"CONS"));
//        	this.saveActionPVer.setDisabled(!false);
//        	this.deleteActionPVer.setDisabled(!false);
//        	this.undoActionPVer.setDisabled(!false); //gb 08/08/07
//        }
//
        statoPianoVer = state;
    }
    
	private boolean opeLoggatoIsOpeAbilitato() {
		// TODO vedere se mettere il metodo nel container o lasciarlo fuori
		return true;
	}

	private void setDefaultChkBoxInfermieri() {
  		Hashtable h1 = UtilForBinding.getComponentsForBinding(protocoliProcedure);
		Object corrValue = "";
		String corrDBName = "";
		Component corrComp;
		for (Enumeration<String> e = h1.keys(); e.hasMoreElements();) {			
				corrDBName = e.nextElement();
				corrComp = (Component) h1.get(corrDBName);
				if(corrComp instanceof CaribelCheckbox){
					((CaribelCheckbox) corrComp).setChecked(false);
				}
		}
	}

	// ======= Gestione PIANO ASSISTENZIALE =======
    public int execSelectPianoAss() throws Exception{
    	this.setMethodNameForQueryKey("queryKey_pianoAss");
    	hParameters.put("pa_tipo_oper", JCariTextFieldTipoOper.getValue());
    	hParameters.put("n_cartella", cartella.getValue());
    	hParameters.put("n_progetto", JCariTextFieldNProgetto.getValue());
    	hParameters.put("cod_obbiettivo", JCariTextFieldCodObiettivo.getValue());
    	hParameters.put("n_intervento", JCariTextFieldNIntervento.getValue());
    	hParameters.put("pa_data", JCariDateTextFieldPianoAss.getValueForIsas());
    	doQueryKeySuEJB();
//        int count = db.selectAllPianoAss(ctcPianoAss);
		if (currentIsasRecord == null){// count <= 0) {
			UtilForUI.standardExclamation(Labels.getLabel("grid.search.no.rows"));
			return -1;
		}
//gb 20/09/07: Solo per infermieri
		//VFR non dovrebbe servire perchè insito nel framework
//        if (gl_strTipoOperatore.equals("02"))
//          SelectCheck();
//gb 20/09/07: fine *******
        return 1;
	}

	@Override
	protected boolean doValidateForm() throws Exception {
        // ha senso solo quando sono in INSERT, in UPDATE è sempre true
		if (!checkAllDatiPAss())
            return false;

        if (dtVarEsistente()){
        	UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.recordEsistente"));
//            new it.pisa.caribel.swing2.cariInfoDialog(null,"Record esistente." +
//                          " Cambiare la data!","Attenzione!").show();
            return false;
        }

        if (!isDtGTUltimaDtChiusPianoPrec()){
        	UtilForComponents.selectMyTabpanel(JCariDateTextFieldPianoAss);
        	((InputElement)JCariDateTextFieldPianoAss).setErrorMessage(""); //Labels.getLabel("pianoAssistenziale.msg.dataAperturaSuccessivaChiusura", new String[]{gl_strUltimaDataChiusuraPianoPrec})
        	JCariDateTextFieldPianoAss.focus();
        	UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.coerenzaDatePAeChiusura"));
//            new it.pisa.caribel.swing2.cariInfoDialog(null,"La data di apertura del Piano assistenziale" +
//                      " deve essere successiva alla data di chiusura del Piano assistenziale precedente" +
//                          " Cambiare la data!","Attenzione!").show();
            return false;
        }
		
		return true;
	}
	private boolean checkAllDatiPAss(){

        //gb 14/11/06
        // se la data definizione piano assistenziale è < di data apertura intervento return false.
        if (dtApePianoAssMinoreDtApeIntervento())
          return false;

        // ctrl data piano assist < data inizio accesso e data verifica
        if (!checkDateAccVer())
            return false;

        return true;
	}

	private boolean dtApePianoAssMinoreDtApeIntervento(){

        // cntrl che la data del 1° argomento sia minore o uguale alla
        // data del 2° argomento
//      if (jCariDateTextFieldHiddenProg.getValue() != null && !(jCariDateTextFieldHiddenProg.getValue().before(JCariDateTextFieldPianoAss.getValue()) || jCariDateTextFieldHiddenProg.getValue().equals(JCariDateTextFieldPianoAss.getValue()))){
		
		if(!ManagerDate.isPeriodoValido(jCariDateTextFieldHiddenProg, JCariDateTextFieldPianoAss)){
	      	  UtilForUI.standardExclamation(Labels.getLabel("common.msg.NoOrderDate.data1maggioreUgualeDi0", new String[]{Labels.getLabel("pianoAssistenziale.aperturaScheda"), Labels.getLabel("pianoAssistenziale.definizionePianoAssistenziale"), }));
	          return true;
         }
        return false;    
	}
//
//    private boolean checkAllDatiPVer(){
//        if (!ctcPianoVer.checkValidity())
//            return false;
//        if (controlloPresenzaVer()){
//            new cariInfoDialog(null,"Data verifica già inserita!","Errore!").show();
//            return false;
//        }
//
//        // Giulia controllo lunghezza JCariTextPane 13/04/05
//        String descr2 = this.jCariTextPaneVerTesto.getText();
//        if (descr2.length()>2000) {
//            descr2 = descr2.substring(0,2000);
//            jCariTextPaneVerTesto.setText(descr2);
//        }
//        // fine controllo lunghezza
//
//        // ctrl data verifica >= data piano assist
//        if (!checkDateVer())
//            return false;
//        return true;
//    }
//
//    private boolean checkAllDatiPAcc(){
//        if((this.jCariTextFieldCodPrestaz.getValue()).equals("")){
//          new cariInfoDialog(null,"Codice Prestazione obbligatorio!","Attenzione!").show();
//          jCariTextFieldCodPrestaz.requestFocus();
//          return false;
//        }
//
//        String quant = this.JCariTextFieldQuantita.getValue();
//        if (quant.trim().equals(""))
//            JCariTextFieldQuantita.setText("1");
//
//        if (!ctcPianoAcc.checkValidity())
//            return false;
//        // ctrl comboBoxFreq selezionata
//        if (jCariComboBoxFreq.getSelectedIndex() < 0) {
//            new cariInfoDialog(null,"Selezionare una voce dalla lista.","Attenzione!").show();
//            jCariComboBoxFreq.requestFocus();
//            return false;
//        }
//        if (controlloPresenzaPrest()){
//            new cariInfoDialog(null,"Prestazione già inserita!","Attenzione!").show();
//            return false;
//        }
//
//        // ctrl data inizio piano accessi >= data piano assist
//        if (!checkDateAcc())
//            return false;
//        return true;
//    }
//
    private boolean dtVarEsistente(){
        String selDtVar = JCariDateTextFieldPianoAss.getValueForIsas();
        boolean trovato = false;
        if (gl_arrExistingDtVar != null){
          int j = 0;
          while ((j<gl_arrExistingDtVar.length) && (!trovato)){
            trovato = (gl_arrExistingDtVar[j].trim().equals(selDtVar.trim()));
            j++;
          }
        }
        return trovato;
    }
    
    private boolean checkDateAccVer(){
        if (!checkDateAcc())
            return false;
        if (!checkDateVer())
            return false;
        return true;
    }

   	private boolean checkDateAcc(){
//        if (pi_data_inizio.getValue() != null && !(JCariDateTextFieldPianoAss.getValue().before(pi_data_inizio.getValue()) || JCariDateTextFieldPianoAss.getValue().equals(pi_data_inizio.getValue()))){
   		
      	 if (!ManagerDate.isPeriodoValido(JCariDateTextFieldPianoAss, pi_data_inizio)){ 
      		 UtilForUI.standardExclamation(Labels.getLabel("common.msg.NoOrderDate.data1maggioreUgualeDi0", new String[]{Labels.getLabel("pianoAssistenziale.definizionePianoAssistenziale"), Labels.getLabel("pianoAssistenziale.inizio") }));
      		 return false;
        }
        return true;
    }

	private boolean checkDateVer(){
//		if (int_data.getValue() != null && !(JCariDateTextFieldPianoAss.getValue().before(int_data.getValue())||JCariDateTextFieldPianoAss.getValue().equals(int_data.getValue()))){
		if (!ManagerDate.isPeriodoValido(int_data, JCariDateTextFieldPianoAss)){
			UtilForUI.standardExclamation(Labels.getLabel("common.msg.NoOrderDate.data1maggioreUgualeDi0", new String[]{Labels.getLabel("pianoAssistenziale.definizionePianoAssistenziale"), Labels.getLabel("pianoAssistenziale.verifica") }));
			return false;
		}
		return true;
    }
    
    private boolean isDtGTUltimaDtChiusPianoPrec() throws ParseException{
    if ((gl_strUltimaDataChiusuraPianoPrec != null) && (!gl_strUltimaDataChiusuraPianoPrec.trim().equals(""))){
//      String dt1=JCariDateTextFieldPianoAss.getValueForIsas();;// Data Apertura Piano Assistenziale
//      String dt2=gl_strUltimaDataChiusuraPianoPrec;// Ultima data chiusura del Piano assistenziale precedente
//      dt2=dt2.substring(8,10)+dt2.substring(5,7)+dt2.substring(0,4);
//      DataWI dataINIZIO=new DataWI(dt2);
//      dt1=dt1.substring(6,10)+dt1.substring(3,5)+dt1.substring(0,2);
      Date dataChiusuraUltimoPiano = UtilForBinding.getDateFromIsas(gl_strUltimaDataChiusuraPianoPrec);
//      int rit=dataINIZIO.confrontaConDt(dt1);
//      if(rit==0 || rit==1){//uguale o minore
      if(!JCariDateTextFieldPianoAss.getValue().after(dataChiusuraUltimoPiano)){
//    	  UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.dataAperturaSuccessivaChiusura"));
//    	  new cariInfoDialog(null,"Inserire una data di apertura successiva alla data di chiusura del piano precedente!","Attenzione!").show();
        return false;
        }else
        return true;
      }else
      return true;
    }

	  //caricamento prestazioni selezionate nella griglia accanto
	public void onConfermaSelezione(Event event) throws Exception{
		String punto = ver  + "onConfermaSelezione ";
		logger.trace(punto + " Inizio ");

		/*
		if(!UtilForComponents.testRequiredFieldsNoCariException(self)){
			return;
		}
		CaribelSearchCtrl cbsPrestazone = (CaribelSearchCtrl) prestazione.getAttribute(MY_CTRL_KEY);
		cbsPrestazone.setRequired(false);
		if(!UtilForComponents.testRequiredFieldsNoCariException(dett.getFellowIfAny("myForm", true))){
			cbsPrestazone.setRequired(true);
			return;
		}
		int ret =this.controlloPrestazioniSelezionate();
		if(ret==-2)return;
		else if(ret==-1){
			UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionarePrestazione"));
			return;
		}else if(statoPianoAcc==this.UPDATE_DELETE && ret>1){
			UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionareUnaPrestazione"));
			return;
		}
		//System.out.println("controllo ok");
		ret=0;
		ret =this.caricaPrestazioniSelezionate();
		if(ret==-1){
			UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.noPrestazioniOperatore"));
			return;
		}
		else if (ret==-2){
			UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionarePrestazione"));
			return;
		}else if(ret==-3) return;
		cbsPrestazone.setRequired(true);
		dettCtrl.setStatoAcc(PianoAccessiGridCRUDCtrl.STATO_WAIT);
		if(showSaveMessage){
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",self,"middle_center",2500);
		}else{
			showSaveMessage = true;
		}
		
		*/
		
		//	      this.jCariTable1.setColumnSizes();
	}
	
	@SuppressWarnings("unchecked")
	public void onOpenAgenda(Event event) throws Exception{
		  Hashtable hAgenda=new Hashtable();
//		  String st="NONE";
		  Date today = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);
		  int st = INSERT;
		   if(pa_data_chiusura.getValue()!=null){//se il piano assistenziale è chiuso devo solo poter consultare 
			   hAgenda.put("updAppuntam","false");//TODO bargi 09/04/2013
			   st=CONSULTA;
		   } else {
			  if(pi_data_fine.getValue() == null || !(today).after(pi_data_fine.getValue())){ //JCariDateTextFieldDataFin.IsVoid()) {
				   hAgenda.put("updAppuntam","true");
				   st=UPDATE_DELETE; //st="UPDATE_DELETE";
			  }else  if(!pi_data_fine.isReadonly()){
				  hAgenda.put("updAppuntam","true");
				  st=UPDATE_DELETE; //st="UPDATE_DELETE";
			  }else{
				  hAgenda.put("updAppuntam","false");
				  st=CONSULTA;
			  }
		   }

//		  if(grigliaAccessi.getSelectedCount()==1){
//		      hAgenda.put("prestazioni", pi_prest_cod.getValue());
//		      hAgenda.put("progressivi",JCariTextFieldProgHide.getValue());
//		      hAgenda.put("pi_prest_cod",pi_prest_cod.getValue());
//		      hAgenda.put("as_prog",this.JCariTextFieldProgHide.getValue());
//		  }else{
			  String prestazioni="";
			  String progressivi="";
			  String pianificati="";
			  String tipo_freq = null;
			  String newFreq = null;
			  String minDate = "9999-99-99";
			  String oldDataFine = "9999-99-99";
			  String pianificato = "";
			  for (Iterator iterator = grigliaAccessi.getSelectedItems().iterator(); iterator.hasNext();) {
				Listitem type = (Listitem) iterator.next();
				Hashtable h = (Hashtable) type.getAttribute("ht_from_grid");
				int count = 0; //gb 07/08/07
				String dataFine = ISASUtil.getValoreStringa(h, "pi_data_fine");
				String cod_prest = (String) h.get("pi_prest_cod");
				String progr = h.get("pi_prog").toString();
				pianificato=(String) h.get(CostantiSinssntW.PIANIFICAZIONE_PAI);
				if(!prestazioni.equals("")){
					prestazioni+="-";
					progressivi+="-";
					pianificati+="-";
				}
				prestazioni+=cod_prest.trim();
				progressivi+=progr;
				pianificati+=pianificato;
				newFreq = (String) h.get("frequenza");
				if(!(oldDataFine.equals("9999-99-99")||oldDataFine.equals(dataFine))){
					//sto cercando di pianificare date fini diverse non lo posso fare
					Clients.showNotification(Labels.getLabel("pianoAssistenziale.msg.richiestePrestazioniConStessaDataFine"));
					return;
				}
				oldDataFine = dataFine;
				
				if(newFreq == null || newFreq.isEmpty()){
					Clients.showNotification(Labels.getLabel("pianoAssistenziale.msg.mancataFrequenza"));
					return;
				}
				if(tipo_freq!=null){
					if(!tipo_freq.equals(newFreq)){
						Clients.showNotification(Labels.getLabel("pianoAssistenziale.msg.richiestePrestazioniConStessaFrequenza"));
						return;
					}
				}else{
					tipo_freq = newFreq;
				}
				if(minDate.compareTo(ISASUtil.getValoreStringa(h, "pi_data_inizio"))>0){
					minDate = ISASUtil.getValoreStringa(h, "pi_data_inizio");
				}
				if(!h.containsKey("pi_pianificato") || !h.get("pi_pianificato").equals("S")){
					hAgenda.put("minDate", minDate);
				}
			}
		    hAgenda.put("prestazioni", prestazioni);
		    hAgenda.put("progressivi", progressivi);
		    hAgenda.put("pianificati", pianificati);
//		    String dataCalcoloPianificazione = UtilForBinding.getValueForIsas(new Date());
		    Date dataCalcoloPianificazione = new Date();
		    if(dataCalcoloPianificazione.before(UtilForBinding.getDateFromIsas(minDate))){
		    	dataCalcoloPianificazione = UtilForBinding.getDateFromIsas(minDate);
		    }
		    hAgenda.put("startPianificazione", UtilForBinding.getValueForIsas(dataCalcoloPianificazione));
//		  }
	      caricoHashAgenda(hAgenda);
	      hAgenda.put("statoPianoAcc",st);
	      if(pianificaGrid != null){
	    	  pianificaGrid.setParent(null);
	    	  pianificaGrid.detach();
	    	  pianificaGrid = null;
	      }
	      pianificaGrid = Executions.getCurrent().createComponents(AgendaPianSettFormCtrl.myPathZul, self, hAgenda);
//	      new JFrameAgendaPianSett(this, this.myContainer, hAgenda, st, gl_strTipoOperatore); //gb 06/08/07

	}

	public int caricaPrestazione() throws Exception{

		//gb 31/08/07 *******
		if( pi_quantita.getValue()==null)
			pi_quantita.setValue(1);
		String prestazioni="";
		String progressivi="";
		int count = 0; //gb 07/08/07
		boolean tuttoOk = false;
		boolean trovato=false;
		if(!controlloPiano())return -3;
		trovato=true ;
		String cod_prest = pi_prest_cod.getValue();
		if(!prestazioni.equals(""))
			prestazioni+="-";
		prestazioni+=cod_prest;
		gl_booCaricoPrestazSelezionate = true;
		// si setta questa variabile booleana per non far gestire
		// l'agenda all'interno del metodo execUpdatePianoAcc.
		tuttoOk = ((Boolean)saveDati(false)).booleanValue();
		if (tuttoOk){
			if(!progressivi.equals(""))
				progressivi+="-";
			//gb 14/11/07 ******* Devo controllare il caso del primo inserimento in assoluto
			//   	                    prima ancora di aver salvato il Piano assistenziale col bottone
			//   	                    'Salva' generale.
			String strProgressivo = (String)JCariTextFieldProgHide.getValue();
			if ((strProgressivo == null) || strProgressivo.equals(""))
				strProgressivo = "1";
			progressivi += strProgressivo;
		}

		if(trovato){
			if (tuttoOk)
				salvaAgenda(prestazioni,progressivi,true);
			setDefaultPianoAcc();
			return 1;
		}else
			return -2;
	}
	
	public int caricaPrestazioniSelezionate() throws Exception{

		if( pi_quantita.getValue()==null)
			pi_quantita.setValue(1);
		String prestazioni="";
		String progressivi="";
		int count = 0; //gb 07/08/07
		boolean tuttoOk = false;
		//		cariObjectTableModel modello=(cariObjectTableModel) this.jCariTableGrigliaPrestaz.getModel();
		CaribelListModel modello = (CaribelListModel) tablePrestazioni.getModel();
		if(modello.getSize()==0)return -1;
		boolean trovato=false;
		//boolean salvaAgenda=false;
		//gb 11.04.08 (da bargi 10.04.08)
		if(!controlloPiano())return -3;

		for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			Listitem litem = (Listitem) iterator.next();
//			litem.setSelected(false);
			trovato=true ;
			String cod_prest = (String) modello.getFromRow(litem, "prest_cod");
			String desc_prest = (String) modello.getFromRow(litem, "prest_des");

			if(!prestazioni.equals("")){
				prestazioni+="-";
			}
			prestazioni+=cod_prest.trim();
			pi_prest_cod.setValue(cod_prest);
			pi_prest_desc.setValue(desc_prest);

			gl_booCaricoPrestazSelezionate = true;
			// si setta questa variabile booleana per non far gestire
			// l'agenda all'interno del metodo execUpdatePianoAcc.
			tuttoOk = ((Boolean)saveDati(false)).booleanValue();
			if (tuttoOk){
				if(!progressivi.equals("")){
					progressivi+="-";
				}
				//gb 14/11/07 ******* Devo controllare il caso del primo inserimento in assoluto
				//                  prima ancora di aver salvato il Piano assistenziale col bottone
				//                  'Salva' generale.
				String strProgressivo = (String)JCariTextFieldProgHide.getValue();
				if ((strProgressivo == null) || strProgressivo.equals("")){
					strProgressivo = "1";
				}
				progressivi += strProgressivo;
        		JCariTextFieldProgHide.setValue(null);
			}  // end if
		} // end for
		tablePrestazioni.clearSelection();
		
		if(trovato){
			if (tuttoOk){
				salvaAgenda(prestazioni,progressivi,true);
				UtilForBinding.resetForm(dett.getFellowIfAny("myForm",  true),this.parkSetting);
				doFreezeForm();
			}
			setDefaultPianoAcc();
			return 1;
		}else
			return -2;
	}

	private void salvaAgenda(String prestazioni, String progressivi, boolean chiedoconferma){
		statoPianoAcc = getStatoInteger(dettCtrl.getStato_corr());
		String abil = getProfile().getStringFromProfile("abil_agen");
		boolean isAbilxAgenda = ((iu.canIUse("AGENDA"))
				&& ((abil != null) && (abil.trim().equals("SI"))));
		if(isAbilxAgenda){
			Hashtable hAgenda=new Hashtable();
			hAgenda.put("prestazioni",prestazioni);
			hAgenda.put("progressivi",progressivi);
			StringTokenizer st=new StringTokenizer(prestazioni,"-");
			if(st.countTokens()==1){
				String prest=st.nextToken();
				hAgenda.put("pi_prest_cod",prest);
				hAgenda.put("prestazioni",prest);
				hAgenda.put("as_prog",progressivi);
			}
			hAgenda.put("minDate", pi_data_inizio.getValueForIsas());
			hAgenda.put("updAppuntam","true");

			logger.info("salvaAgenda / statoPianoAcc: " + statoPianoAcc);

			caricoHashAgenda(hAgenda);
			hAgenda.put("statoPianoAcc",statoPianoAcc);
			
			Executions.getCurrent().createComponents(AgendaPianSettFormCtrl.myPathZul, self, hAgenda);
			showSaveMessage  = false;
//			if(statoPianoAcc==this.UPDATE_DELETE){
//				//TODO
////				new JFrameAgendaPianSett(this, this.myContainer, hAgenda, "UPDATE_DELETE", gl_strTipoOperatore);
//				Executions.getCurrent().createComponents(AgendaPianSettFormCtrl.myPathZul, self, hAgenda);
//			}
//			else if(statoPianoAcc==this.INSERT ){
//				//TODO
////				logger.info("Chiamata a JFrameAgendaPianSett / INSERT: " + this.myContainer.toString());
////				new JFrameAgendaPianSett(this, this.myContainer, hAgenda, "INSERT", gl_strTipoOperatore);
//				Executions.getCurrent().createComponents(AgendaPianSettFormCtrl.myPathZul, self, hAgenda);
//			}
		}
		setDefaultPianoAcc();
		setStatoPianoAcc(this.WAIT); //gb 06/08/07
	}
	
	private int getStatoInteger(String stato_corr) {
		if(stato_corr.equals(CaribelGridStateCtrl.STATO_WAIT))
			return WAIT;
		if(stato_corr.equals(CaribelGridStateCtrl.STATO_INSERT))
			return INSERT;
		if(stato_corr.equals(CaribelGridStateCtrl.STATO_UPDATE))
			return UPDATE_DELETE;
		return CONSULTA;
	}

	private void caricoHashAgenda(Hashtable<String, String> h){
		String pi_prest_cod_old = "";
		if(grigliaAccessi.getSelectedIndex()>-1){
			pi_prest_cod_old = (String) ((CaribelListModel) grigliaAccessi.getModel()).getFromRow(grigliaAccessi.getSelectedIndex(), "pi_prest_cod");
		}
		h.put("pi_prest_cod_old", pi_prest_cod_old);
		h.put("n_cartella", cartella.getValue());
		h.put("n_progetto", JCariTextFieldNProgetto.getValue()); //gb 06/08/07
		h.put("cod_obbiettivo", JCariTextFieldCodObiettivo.getValue()); //gb 06/08/07
		h.put("n_intervento", JCariTextFieldNIntervento.getValue()); //gb 06/08/07
		h.put("as_op_referente", pi_op_esecutore.getValue());
		h.put("as_tipo_op_referente", gl_strTipoOperatore); //gb 03/09/07
		h.put("esecutore", pi_op_esecutore.getValue());
		//h.put("as_prog",JCariTextFieldProgHide.getValue());
		//gb 07/08/07         h.put("as_tipo_oper",tipoOperatore);//caso assistenti sociali
		h.put("as_tipo_oper", gl_strTipoOperatore); //gb 07/08/07
		h.put("pa_data",JCariDateTextFieldPianoAss.getValueForIsas()); //gb 07/08/07
		// h.put("pi_prest_cod",jCariTextFieldCodPrestaz.getValue());
		h.put("pi_data_inizio",pi_data_inizio.getValueForIsas());
		h.put("pi_data_fine",pi_data_fine.getValueForIsas());
		if( pi_quantita.getValue()==null)
			pi_quantita.setValue(1);

		h.put("quantita",pi_quantita.getValue().toString());
		h.put("ap_alert",""+pi_freq.getSelectedValue());
		//gb 06/08/07: Attenzione le seguenti 2 istruzioni sono state commentate perché
		//             nell'EJB non c'è mai un get per i DBName 'assistito' e 'indirizzo'
		//             e nelle 3 tabelle relative all'AGENDA, non compaiono tali campi.
		//             Servono quando si chiama la form dell'AGENDA 'JFrameAgendaPianSett'.
		//             Da vedere quando sarà il momento.
		h.put("assistito",(String) caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ASSISTITO_COGNOME)+" "+
						  (String) caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ASSISTITO_NOME));
		h.put("indirizzo", JCariTextFieldIndHide.getValue());
	}

	public Boolean saveDati(boolean chiudi) throws Exception{
	        if (statoPianoAss == CONSULTA)
	            return new Boolean(true);

	//gb 02/08/07 *******
	          if(pa_data_chiusura.getValue()!=null){
//	            int comp=ndf.dateCompare(this.JCariDateTextFieldPianoAss.getValue(),this.JCariDateTextFieldDataChiusura.getValue());
//	            if(JCariDateTextFieldPianoAss.getValue().after(pa_data_chiusura.getValue())){
	        	if (!ManagerDate.isPeriodoValido(JCariDateTextFieldPianoAss, pa_data_chiusura)){  
	            	UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.incoerenzaDateProgetto"));
	              return new Boolean(true);
	              }
	            }
	//gb 20/09/07: solo per infermieri
//	          if (gl_strTipoOperatore.equals("02"))
//	            valorizzoCheck();
	        // salvo solo se e' stato cambiato qualcosa
	        if (currentIsasRecord!=null && (tH != null && !tH.isModified(UtilForBinding.getHashtableFromComponent(self, null, false, true)))
	        && (hAccessi != null && !hAccessi.isModified(UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true), null, false, true)))
	        && (hVerifiche != null && !hVerifiche.isModified(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true), null, false, true))))
	            return new Boolean(true);
	        boolean canIModiPAss = false;
	        boolean canIModiPAcc = false;
	        boolean canIModiPVer = false;
	        try {
	            canIModiPAss = (iu.canIUse(myKeyPermission, "MODI")
	                            && iu.canIWrite(currentIsasRecord));
	        } catch (Exception e) {
	            logger.info("ERRORE 1" + e);
	           canIModiPAss = false;
	        }
	        try {
	            canIModiPAcc = (iu.canIUse(pAccMyKeyPermission, "MODI")
	                            && iu.canIWrite(currentIsasRecord));
	         } catch (Exception e) {
	            logger.info("ERRORE 2" + e);
	            canIModiPAcc = false;
	        }
	        try {
	            canIModiPVer = (iu.canIUse(pVerMyKeyPermission, "MODI")
	                            && iu.canIWrite(currentIsasRecord));
	        } catch (Exception e) {
	            logger.info("ERRORE 3" + e);
	            canIModiPVer = false;
	        }

	        // se modificato piano assist e NON ho diritti di scrittura
	        if ((tH != null && tH.isModified(UtilForBinding.getHashtableFromComponent(self)))
	            && (((statoPianoAss == this.INSERT) && (!iu.canIUse(myKeyPermission,"INSE")))
	            	|| ((statoPianoAss == this.UPDATE_DELETE) && (!canIModiPAss)))
	        )
	            return new Boolean(true);
	        
	        //VFR eliminato da quando è disabilitato il salvataggio in caso di modifica o inserimento degli accessi e delle verifiche
	        // se modificato piano accessi e NON ho diritti di scrittura
	        if ((hAccessi != null && hAccessi.isModified(UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true))))
	            && (((statoPianoAcc == this.INSERT) && (!iu.canIUse(pAccMyKeyPermission,"INSE")))
	                || ((statoPianoAcc == this.UPDATE_DELETE) && (!canIModiPAcc)))
	        )
	            return new Boolean(true);
	        // se modificato piano verifica e NON ho diritti di scrittura
	        if ((hVerifiche != null && !hVerifiche.isModified(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true))))
	            && (((statoPianoVer == this.INSERT) && (!iu.canIUse(pVerMyKeyPermission,"INSE")))
	            	|| ((statoPianoVer == this.UPDATE_DELETE) && (!canIModiPVer)))
	        )
	            return new Boolean(true);
	        // 27/04/11 -------


	        if(pa_data_chiusura.getValue()!=null && !chiudi){
		        UtilForUI.standardYesOrNo(Labels.getLabel("pianoAssistenziale.msg.chiusuraPiano"),new EventListener<Event>(){
					public void onEvent(Event event) throws Exception{
						if (Messagebox.ON_YES.equals(event.getName())){
							boolean tuttoOk = saveDati(true);
							if(caribelGrid!=null){
								//REFRESH SULLA LISTA
								caribelGrid.doRefreshNoAlert();
							}
					        if(tuttoOk){
								doCheckPermission();
								doFreezeForm();		
								Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
					        }
						}
					}
				});
	            return new Boolean(false);
	        }

	        // 07/04/11
	        if (ManagerProfile.isConfigurazioneLazio(getProfile()))
	            setComplessita(false);
	        boolean isInserimento = (statoPianoAss == this.INSERT);
	        int risu = 0;
//	        JActionButtonSalva.requestFocus();
	        // se sono in INSERT x pianoAss ed ho modificato qualcosa, devo comunque salvare:
	        // inserisco la testata e quello che è cambiato -> chiamerà il metodo "insertAllPiano()".
	        newHtAccessi = UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true));
	        newHtAccessi.put(JCariTextFieldTipoOper.getDb_name(), JCariTextFieldTipoOper.getValue());
	        newHtAccessi.put(cartella.getDb_name(), cartella.getValue().toString());
	        newHtAccessi.put(JCariTextFieldNProgetto.getDb_name(), JCariTextFieldNProgetto.getValue());
	        newHtAccessi.put(JCariTextFieldCodObiettivo.getDb_name(), JCariTextFieldCodObiettivo.getValue());
	        newHtAccessi.put(JCariTextFieldNIntervento.getDb_name(), JCariTextFieldNIntervento.getValue());
	        newHtAccessi.put(JCariDateTextFieldPianoAss.getDb_name(), JCariDateTextFieldPianoAss.getValueForIsas());
	        if (isInserimento){
//	        	doWriteComponentsToBean();
//	        	currentIsasRecord = insertSuEJB(myEJB, this.hParameters);
	            risu = this.execUpdatePianoAss();
	        }else {
	            if ((risu >= 0) && (hAccessi != null && hAccessi.isModified(newHtAccessi))){
	            	String methodName = "";
	            	ISASRecord nuovaPrestazione;
	            	if(newHtAccessi.get("pi_prog")!=null && !newHtAccessi.get("pi_prog").isEmpty()){
//	            		ISASRecord tmp = 
	            		methodName = "update_pianoAcc";
	            		nuovaPrestazione = (ISASRecord) invokeGenericSuEJB(myEJB, newHtAccessi, methodName);
	            	}else{
	            		methodName = "insert_pianoAcc";
	            		nuovaPrestazione = (ISASRecord) invokeGenericSuEJB(myEJB, newHtAccessi, methodName);
	            	}
	            	UtilForBinding.bindDataToComponent(logger, nuovaPrestazione, dett.getFellowIfAny("myForm",  true));
	            	if(methodName == "insert_pianoAcc"){
//	            		JCariTextFieldProgHide.setValue(null);
	            	}
//	                risu = this.execUpdatePianoAcc();
	            }
	            if ((risu >= 0) && (hVerifiche != null && hVerifiche.isModified(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true)))))
	            	invokeGenericSuEJB(myEJB, UtilForBinding.getHashtableFromComponent(grigliaVerifiche), "update_pianoVer");
//	                risu = this.execUpdatePianoVer();
	            // deve essere l'ultimo ad essere eseguito perché il setStatoPianoAss
	            // chiama il setDefault x pianoAcc e pianoVer
	            if ((risu >= 0) && (tH != null && tH.isModified(UtilForBinding.getHashtableFromComponent(self))))
	            	risu = this.execUpdatePianoAss();
//	            	updateSuEJB(myEJB, (ISASRecord) currentBean);
	        }
	        // aggiorno la statusBar
	        if (risu >= 0){//il fallimento delle operazioni da DB è gestito come eccezione se sono qu va tutto ok
	              setInfo(this.nomeLivInfo);// 25/01/07
	              //VFR non dovrebbero servire non sono gestiti ulteriori pulsanti
//	              if (isInserimento)
//	                  this.myContainer.setAbilPulsanti("PIANO_ASSIST");
//	               ctcPianoAss.notifyGenericEvent("piano_assist_INSE");
	        }else{
	        	return false;
	        }
//	        doWriteBeanToComponents();JCariTextFieldProgHide.getValue();
            setStatoPianoAss(UPDATE_DELETE);
            doFreezeForm();
	        return true;
	    }

	public int execUpdatePianoAss() throws Exception
	{
		int count=0;

        if(!doValidateForm()){
        	return -1;
        }
        
        doWriteComponentsToBean();
        if (this.statoPianoAss == UPDATE_DELETE){
//            count = db.updatePianoAss(ctcPianoAss);
        	currentIsasRecord = updateSuEJB(myEJB, currentIsasRecord);
        	currentIsasRecord = queryKeySuEJB(myEJB, hParameters);
        }else if (this.statoPianoAss == INSERT) {
            // se sono in INSERT x pianoAss, posso essere al max in INSERT per pianoAcc e pianoVer
            // e, quindi, faccio un inserimento complessivo spedendo le 3 hashtable.
//          count = db.insertPianoAss(ctcPianoAss);
            count  = insertAllPiano();
        }
        doWriteBeanToComponents();
       doFreezeForm();

        return count;
	}


    // Spedisce la ctc di pianoAss ed 1 vetttore contenente alla posizione 0
    // l'hashtable dalla ctc di pianoVer ed alla posizione 1 l'hashtable dalla
    // ctc di pianoAcc. Le 2 hashtable sono non null solo se i rispettivi campi
    // sono stati valorizzati.
    private int insertAllPiano() throws Exception
    {
        Vector vettAccVer = new Vector();
        Hashtable<String, String> h_pianoAcc = null;
        Hashtable<String, String> h_pianoVer = null;

        // se ho modificato x insert del piano verifica
	        // se modificato piano verifica e NON ho diritti di scrittura
        if (hVerifiche != null && hVerifiche.isModified(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true)))){
//        if (cHPianoVer != null && cHPianoVer.isModified(ctcPianoVer_xCmp)) {
//            if (!checkAllDatiPVer())
//                return -1;
            h_pianoVer = UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true));//ctcPianoVer.getHashtableKeyValue();
	        	
        }
            vettAccVer.addElement(h_pianoVer);
//        vettAccVer.addElement(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true)));
//
//        // se ho modificato x insert del piano accessi
//        if (cHPianoAcc != null && cHPianoAcc.isModified(ctcPianoAcc_xCmp)){
//      		if (!checkAllDatiPAcc())
//                return -1;
          if (hAccessi != null && 
        	hAccessi.isModified(UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true)))){
           h_pianoAcc = newHtAccessi;
        	  // h_ pianoAcc = dettCtrl.getDataFromGrid(); //.getHashtableKeyValue();
          }
          vettAccVer.addElement(h_pianoAcc);
//        }
          doWriteComponentsToBean();
        int count = 0;
        //db.insertAllPiano(ctcPianoAss, vettAccVer);
		Object[] par = new Object[2];
		par[0] = this.hParameters;
   		par[1] = vettAccVer;
		//    		par[2]= vettoreComponentiCommissione;
		this.currentIsasRecord = insertSuEJB(this.currentBean, par);
		setStatoPianoAss(UPDATE_DELETE);
		doWriteBeanToComponents();
        return count;
    }


	public Boolean setInfo(String nomeLivelloInfo){
        this.nomeLivInfo = nomeLivelloInfo;// 25/01/07

        String numCart = cartella.getValue();
        String numProg = JCariTextFieldNProgetto.getValue();
        String codObiet = JCariTextFieldCodObiettivo.getValue();
        String numInterv = JCariTextFieldNIntervento.getValue();
        String tpOper = JCariTextFieldTipoOper.getValue();

        String dataPiano = JCariDateTextFieldPianoAss.getValueForIsas();
        String operRef = cod_operatore.getValue() +
                " " + desc_operatore.getValue();

        String msgxChiusura = " - Data chiusura: " + pa_data_chiusura.getValue();

        Hashtable h_dati = new Hashtable();
        h_dati.put("n_cartella", numCart);
        h_dati.put("n_progetto", numProg);
        h_dati.put("n_contatto", numProg); //gb 10/08/07
        h_dati.put("cod_obbiettivo", codObiet);
        h_dati.put("n_intervento", numInterv);
        h_dati.put("pa_tipo_oper", tpOper);
        h_dati.put("pa_data", dataPiano);

        h_dati.put("stato", new Integer(this.statoPianoAss));

        //FIXME che fa??????
//        h_dati.put("parMtdDaInvocare", new Object[]{this.myContainer.getNome2ndFrame(this.getClass().getName())});

        String info = "PIANO ASSISTENZIALE: Definito il: " + dataPiano +
                    " - Referente: " + operRef + msgxChiusura;

        // setto informazioni nella barra di stato
        caribelContainerCtrl.hashChiaveValore.put(this.nomeLivInfo, h_dati);// 25/01/07
        return new Boolean(true);
    }
	
    private void setComplessita(boolean forzaSetting) throws Exception{
    	// se già valorizzato: non si fa niente
    	if ((skpa_complessita.getSelectedIndex() != -1)
    			&& (!forzaSetting))
    		return;

    	String tipoUV = getTipoUV();
    	String complAss = "-1";
    	if (tipoUV.trim().equals("1")) // tipoUV = OCCASIONALI
    		complAss = "01"; // prestazionale
    	else
    		complAss = calcCompl();
    	if (Integer.parseInt(complAss) > 0)
    		skpa_complessita.setValue(complAss);
    	else
    		skpa_complessita.setSelectedIndex(-1);
    }

    private String calcCompl() throws Exception{
        String risu = "0";
        Hashtable h_d = new Hashtable();
        h_d.put("n_cartella", cartella.getValue().trim());
        h_d.put("n_progetto", JCariTextFieldNProgetto.getValue().trim());
        h_d.put("cod_obbiettivo", JCariTextFieldCodObiettivo.getValue().trim());
        h_d.put("n_intervento", JCariTextFieldNIntervento.getValue().trim());
        h_d.put("pa_data", JCariDateTextFieldPianoAss.getValue());
        h_d.put("pa_tipo_oper", JCariTextFieldTipoOper.getValue().trim());

        double nAccSett = (Double) invokeGenericSuEJB(myEJB, h_d, "query_FreqMaxAcc");

        if ((nAccSett > 0) && (nAccSett < 3))
            risu = "02"; // bassa
        else if ((nAccSett >= 3) && (nAccSett < 7))
            risu = "03"; // media
        else if ((nAccSett >= 7) && (nAccSett < 10))
            risu = "04"; // alta
        else if (nAccSett >= 10)
            risu = "05"; // alta++

        return risu;
    }

    // 07/04/11
    private String getTipoUV() throws Exception{
        String ret = "";

        Hashtable hPUA = (Hashtable) invokeGenericSuEJB(new SkInfEJB(), this.caribelContainerCtrl.hashChiaveValore, "getDatiRLPuaUvm");// db.getDatiRLPuaUvm(ht);
        if ((hPUA != null)  && (hPUA.get("tipologia") != null))
            ret = hPUA.get("tipologia").toString();
        return ret;
    }
    
	private boolean controlloPiano() {

//		int comp=NumberDateFormat.dateCompare(this.JCariDateTextFieldPianoAss.getValue(),UtilForBinding..pi_data_inizio.getValue());
		if(pi_data_inizio.getValue() != null && DateUtils.truncate(JCariDateTextFieldPianoAss.getValue(), Calendar.DAY_OF_MONTH).after(DateUtils.truncate(pi_data_inizio.getValue(), Calendar.DAY_OF_MONTH))){
			UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.coerenzaDate"));
			return false;
		}
		if(pi_freq.getSelectedIndex()==-1){
			UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.mancataFrequenza"));
			return false;
		}
		//		    04/01/2007 Controllo che la data di chiusura sia maggiore della data di apertura
		if(pi_data_fine.getValue() != null) {
			if(pi_data_inizio.getValue().after(pi_data_fine.getValue())){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.coerenzaDatePrestazione"));
				pi_data_fine.setValue(null);
				pi_data_fine.focus();
				return false;
			}
		}
		return true;
	}

	//gb 07/08/07 *******
	@SuppressWarnings("rawtypes")
	public int controlloPrestazioniSelezionate(){
		CaribelListModel modello = (CaribelListModel) tablePrestazioni.getModel();
		int numSelez=0;
		if(modello.getSize()==0) return -1; //.getRowCount()==0)return -1;
		boolean trovatoDoppio=false;
		for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			numSelez++;
			Listitem litem = (Listitem) iterator.next();
			/*Controllo se e' gia' presente nella griglia un record con lo stesso codice prestazione
	                  e non concluso in tal caso non permetto inseriemnto
			 */
			String cod_prest = (String) modello.getFromRow(litem, "prest_cod");
			int isPres=isPresente(cod_prest);
			if(isPres==-2) {
				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.prestazionePresente", new String[]{cod_prest}));
				litem.setSelected(false); //modello.setValueAt(new Boolean(false),k,col);
				trovatoDoppio=true;
//				return -2;
			}
		}
		if(trovatoDoppio)
			return -2;
		else
			return numSelez;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int isPresente(String cod_prest) {
		Hashtable hNuovo=new Hashtable() ;
		hNuovo.put("pi_prest_cod",cod_prest);
		hNuovo.put("pi_op_esecutore",((CaribelTextbox) dett.getFellowIfAny("pi_op_esecutore", true)).getValue());
		logger.info("controlo se presente"+hNuovo.toString());
		int num_riga=  ((CaribelListModel) grigliaAccessi.getModel()).columnsContains(hNuovo);
		if (num_riga!=-1){
			String data = (String) ((CaribelListModel) grigliaAccessi.getModel()).getFromRow(num_riga, "pi_data_fine");
			logger.info("controllo se presente ha data fine="+data);
			if(data == null || data.isEmpty())
				return -2;
		}
		return 1;
	}

	public boolean salvaAccesso() throws Exception {
        int ret =this.isPresente(pi_prest_cod.getValue());
        if(ret==-2) {
			UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.prestazionePresente", new String[]{pi_prest_cod.getValue()}));
        	return false;
        }
        ret=0;
        ret =this.caricaPrestazione();
        if(ret==-1){
        	UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.noPrestazioniOperatore"));
        	return false;
        }
        else if (ret==-2){
        	UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionarePrestazione"));
          return false;
        }else if(ret==-3){
        	return false;
        }
        return true;
    }
	
	protected void doFreezeForm() throws Exception{
		tH = new CompareHashtable(UtilForBinding.getHashtableFromComponent(self, null, false, true));
		hAccessi  = new CompareHashtable(UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true), null, false, true));
		hVerifiche = new CompareHashtable(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true), null, false, true));
		doFreezeListBox();
	}
	
	protected boolean doSaveForm() throws Exception{
		boolean tuttoOk = false;
		if(!isSavable()){
			Messagebox.show(
					Labels.getLabel("permissions.insufficient.on.doSaveForm"),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
			return false;
		}	
		UtilForComponents.testRequiredFields(self);
		if(!doValidateForm())
			return false;
		doWriteComponentsToBean();

		if ((tablePrestazioni !=null) && (tablePrestazioni.getSelectedCount()>0)){
//			if(!UtilForComponents.testRequiredFieldsNoCariException(self)){
//				return;
//			}
//			CaribelSearchCtrl cbsPrestazone = (CaribelSearchCtrl) prestazione.getAttribute(MY_CTRL_KEY);
//			cbsPrestazone.setRequired(false);
//			if(!UtilForComponents.testRequiredFieldsNoCariException(dett.getFellowIfAny("myForm", true))){
//				cbsPrestazone.setRequired(true);
//				return;
//			}
			
			int ret =this.controlloPrestazioniSelezionate();
			if(ret==-2){
//				return;
				return false;
			}
			else if(ret==-1){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionarePrestazione"));
//				return;
				return false;
			}else if(statoPianoAcc==this.UPDATE_DELETE && ret>1){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionareUnaPrestazione"));
//				return;
				return false;
			}
			//System.out.println("controllo ok");
			ret=0;
			ret =this.caricaPrestazioniSelezionate();
			if(ret==-1){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.noPrestazioniOperatore"));
//				return;
				return false;
			}
			else if (ret==-2){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAsssitenziale.msg.selezionarePrestazione"));
//				return;
				return false;
			}else if(ret==-3) {
				return false;
//				return;
			}
//			cbsPrestazone.setRequired(true);
			dettCtrl.setStatoAcc(PianoAccessiGridCRUDCtrl.STATO_WAIT);
			if(showSaveMessage){
				Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",self,"middle_center",2500);
			}else{
				showSaveMessage = true;
			}
		}else {
			gl_booCaricoPrestazSelezionate = false;
			/* elisa b 05/08/16: commentato per ripristinare versione in prod */
			if(pi_prest_cod.getValue().isEmpty() && !vengoDaPianificazionePai ){
//				codPrestazione.setErrorMessage(Labels.getLabel("component.required"));
				Clients.showNotification(Labels.getLabel("pianoAsssitenziale.msg.selezionarePrestazione"));
				return false;
			}
			tuttoOk = ((Boolean)saveDati(false)).booleanValue();
			
			if(this.caribelGrid!=null){
				//REFRESH SULLA LISTA
				this.caribelGrid.doRefreshNoAlert();
			}
			
			if(tuttoOk){
				String cod_prest = pi_prest_cod.getValue();
				if(cod_prest != null && !cod_prest.isEmpty()){
					String strProgressivo = (String)JCariTextFieldProgHide.getValue();
					if ((strProgressivo == null) || strProgressivo.equals(""))
						strProgressivo = "1";
					salvaAgenda(cod_prest,strProgressivo,true);
				}
				doCheckPermission();
				doFreezeForm();		
				Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
			}
		}
		
		return tuttoOk;
	}
	
	
	
	
	
	
	
	
	protected boolean doSaveFormOld() throws Exception{
		if(!isSavable()){
			Messagebox.show(
					Labels.getLabel("permissions.insufficient.on.doSaveForm"),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
			return false;
		}	
		
		UtilForComponents.testRequiredFields(self);
		
		if(!doValidateForm())
			return false;

		doWriteComponentsToBean();
		
        gl_booCaricoPrestazSelezionate = false;
        // si setta questa variabile booleana per far gestire
        // l'agenda all'interno del metodo execUpdatePianoAcc.
//        gl_strCodicePrestaz = this.jCariTextFieldCodPrestaz.getUnmaskedText();
//        gl_strProgressPrestaz = this.JCariTextFieldProgHide.getUnmaskedText();
//        gl_boolChiedoConfermaInAgenda = true;
        boolean tuttoOk = ((Boolean)saveDati(false)).booleanValue();
		
//		doWriteComponentsToBean();
//		 Vector vettAccVer = new Vector();
//		 vettAccVer.add(null);
//		 vettAccVer.add(null);
//		 
//		if(this.currentIsasRecord!=null){
//			Object[] par = new Object[2];
//			par[0] = this.currentIsasRecord.getHashtable();
//	   		par[1]= vettAccVer;
//			//    		par[2]= vettoreComponentiCommissione;
//			this.currentIsasRecord = insertSuEJB(this.currentBean, par);
////			this.currentIsasRecord = updateSuEJB(this.currentBean,this.currentIsasRecord);
//		}else{
//			Object[] par = new Object[2];
//			par[0] = this.hParameters;
//	   		par[1]= vettAccVer;
//			//    		par[2]= vettoreComponentiCommissione;
//			this.currentIsasRecord = insertSuEJB(this.currentBean, par);
////			this.currentIsasRecord = insertSuEJB(this.currentBean,this.hParameters);
//		}
//		doWriteBeanToComponents();
		if(this.caribelGrid!=null){
			//REFRESH SULLA LISTA
			this.caribelGrid.doRefreshNoAlert();
		}
//		if(this.caribelContainerCtrl!=null){
//			//REFRESH SUL CONTAINER
//			caribelContainerCtrl.doRefreshOnSave(compChiamante);
//		}
        if(tuttoOk){
			doCheckPermission();
			doFreezeForm();		
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
        }
		return tuttoOk;
	}
	
	protected void doDeleteForm() throws Exception{
		try{
			if(!isDeletable()){
				Messagebox.show(
						Labels.getLabel("permissions.insufficient.on.doDeleteForm"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.ERROR);
				return;
			}
			
			/* elisa b 22/06/16: se il piano e' stato comunicato a una qualche
			 * societa' di servizi non puo' essere cancellato */
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, cartella.getText());
			dati.put(CostantiSinssntW.N_PROGETTO, JCariTextFieldNProgetto.getValue());
			dati.put(CostantiSinssntW.N_INTERVENTO, JCariTextFieldNIntervento.getValue());
			dati.put(CostantiSinssntW.COD_OBBIETTIVO, JCariTextFieldCodObiettivo.getValue());
			dati.put(CostantiSinssntW.TIPO_OPERATORE, UtilForContainer.getTipoOperatorerContainer());
			dati.put("pa_data", JCariDateTextFieldPianoAss.getValueForIsas());
			Boolean isPianoInviato = myEJB.isPianoAssistInviato(CaribelSessionManager.getInstance().getMyLogin(), dati);
			if(isPianoInviato){
				Messagebox.show(
						Labels.getLabel("pianoAssistenziale.msg.impossibile.cancellare"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.ERROR);
				return;
			}
			
			execDeleteForm();
//			UtilForUI.standardYesOrNo(Labels.getLabel("pianoAssistenziale.msg.cancellazione"),  new EventListener<Event>() {
//				public void onEvent(Event event) throws Exception {
//					if (Messagebox.ON_YES.equals(event.getName())){
//					}
//					return;
//			}});
			}catch (Exception e){
			doShowException(e);
		}
	}
	
	protected void execDeleteForm() throws Exception{
		try{
//			int count = db.deletePianoAss(ctcPianoAss); //gb 10/09/07
			
//			deleteSuEJB(currentBean, currentIsasRecord.getHashtable());
			Object o = invokeGenericSuEJB(currentBean, UtilForBinding.getHashtableForEJBFromHashtable(currentIsasRecord.getHashtable()), "delete_pianoAss");
			doFreezeForm();
			if(caribelGrid!=null){
				//REFRESH SULLA LISTA
				caribelGrid.doRefresh();
			}
			if(caribelContainerCtrl!=null){
				caribelContainerCtrl.doRefreshOnDelete(compChiamante);
			}
			compChiamante.detach();
			Clients.showNotification(Labels.getLabel("form.delete.ok.notification"),"info",compChiamante,"middle_center",2500);
//		        if (count > 0){
//		            if (this.myContainer != null)
//		                this.myContainer.invocaMetodo("deleteJFPianoAssist");
//		            else
//		                myDispose();
//
//		           // 25/03/13
//		           ctcPianoAss.notifyGenericEvent("piano_assist_DEL");
//		        } else
//		            setStatoPianoAss(CONSULTA);
		}catch (Exception e){
			doShowException(e);
		}
	}
	
	public void onChangeDatePA(Event event) throws Exception{
		if (JCariDateTextFieldPianoAss.getValue() != null) pi_data_inizio.setConstraint("after " + UtilForComponents.formatDateforDatebox(JCariDateTextFieldPianoAss.getValue()));
	}
	
	
	private Predicate filtroPrestazioni = new Predicate() {
		public boolean evaluate(Object object) {
			Hashtable prestazione;
			if (!(object instanceof Hashtable)) {
				if(!(object instanceof ISASRecord)){
					return false;
				}else{
					prestazione = ((ISASRecord) object).getHashtable();
				}
			}else{
				prestazione = (Hashtable) object;
			}
			String valoreFiltroPrestazioneCodice = codPrestazione.getValue()
					.toUpperCase();
			String valoreFiltroPrestazioneDescrizione = descPrestazione.getValue()
					.toUpperCase();
			String codicePrestazione = (String) prestazione.get("prest_cod");
			String descrizionePrestazione = (String) prestazione.get("prest_des");
			
			boolean selezionata = modelloPrestazioni.isSelected(object);
			boolean isCodice = false;
			boolean isDescrizione = false;
			for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
				selezionata = false;
				Listitem litem = (Listitem) iterator.next();
				Object itemGrid = litem.getAttribute("ht_from_grid");
				if (itemGrid instanceof Hashtable) {
					String cod_prest = (String) ((Hashtable) itemGrid).get("prest_cod");
					if(cod_prest.equals(codicePrestazione)){
						selezionata = true;
						break;
					}
				}
			}
			
			isCodice = (codicePrestazione != null && codicePrestazione.contains(valoreFiltroPrestazioneCodice));
			isDescrizione = (descrizionePrestazione != null && descrizionePrestazione.contains(valoreFiltroPrestazioneDescrizione));
			boolean ret = isCodice && isDescrizione || selezionata;
			return ret;
		}
	};

	@SuppressWarnings("unchecked")
	public Collection getPrestazioni() {
		return CollectionUtils.select(modelloPrestazioni != null ? modelloPrestazioni : CollectionUtils.EMPTY_COLLECTION, filtroPrestazioni);
	}

	@Override
	public boolean aggiornaDatiEsterni() {
		try{
			dettCtrl.setStatoAcc(PianoAccessiGridCRUDCtrl.STATO_WAIT);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * elisa b: 21/06/16
	 * Metodo che consente la modifica del piano e lo rende non disponibile
	 * per essere acquisisto dalle aziende di servizi tramite ws
	 * @param e
	 * @throws Exception
	 */
	public void onAggiornaPianoAss(ForwardEvent e) throws Exception {
		logger.debug("onModificaPianoAss");
	
		aggiornaFlagStatoPianoAssistenziale(IN_ELABORAZIONE);
	}		
	
	/**
	 * elisa b 
	 * Metodo che setta il campo flag_stato in modo da rendere il piano
	 * disponibile per essere acquisisto dalle aziende di servizi tramite ws
	 * 
	 * @param e
	 * @throws Exception
	 */
	public void onCongelaPianoAss(ForwardEvent e) throws Exception {
		logger.debug("onCongelaPianoAss");
		String messaggio = Labels.getLabel("pianoAssistenziale.msg.conferma.congelamento");
		Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
				Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							if(isModified())
								//se ci sono dati modificati si salvano
								doSaveForm();
							aggiornaFlagStatoPianoAssistenziale(DA_INVIARE);
						}
					}
				});
			
	}
	
	/**
	 * elisa b
	 * Metodo che aggiorna il campo flag_stato sul piano
	 * @param stato
	 * @throws Exception
	 */
	private void aggiornaFlagStatoPianoAssistenziale(String stato) throws Exception {
		boolean isModificabile = stato.equals(IN_ELABORAZIONE);
		
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, cartella.getText());
		dati.put(CostantiSinssntW.N_PROGETTO, JCariTextFieldNProgetto.getValue());
		dati.put(CostantiSinssntW.N_INTERVENTO, JCariTextFieldNIntervento.getValue());
		dati.put(CostantiSinssntW.COD_OBBIETTIVO, JCariTextFieldCodObiettivo.getValue());
		dati.put(CostantiSinssntW.TIPO_OPERATORE, UtilForContainer.getTipoOperatorerContainer());
		dati.put("pa_data", JCariDateTextFieldPianoAss.getValueForIsas());
		dati.put("flag_stato", stato);
		
		myEJB.aggiornaFlagStatoPianoAssistenziale(CaribelSessionManager.getInstance().getMyLogin(), dati);
	
		gestioneBottoniAggiornaCongela(isModificabile);
	}
	
	private void gestioneBottoniAggiornaCongela(boolean isModificabile){
		btn_congelaPianoAss.setDisabled(!isModificabile||currentIsasRecord == null);
		btn_aggiornaPianoAss.setDisabled(isModificabile);
		UtilForBinding.setComponentReadOnly(areaInterv, !isModificabile);
		UtilForBinding.setComponentReadOnly(areaPianoAccessi, !isModificabile);
		
		//data e motivo chiusura devono essere sempre editabili
		if(!isModificabile){
			pa_data_chiusura.setReadonly(false);
			pa_motivo_chiusura.setReadonly(false);
		}
	}
}

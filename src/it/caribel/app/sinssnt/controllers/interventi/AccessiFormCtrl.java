package it.caribel.app.sinssnt.controllers.interventi;

import it.caribel.app.common.ejb.UnitaFunzEJB;
import it.caribel.app.sinssnt.bean.IntpreEJB;
import it.caribel.app.sinssnt.bean.modificati.IntervEJB;
import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.util.CompareHashtable;
import it.caribel.util.FiltroCodiceDescrizione;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelDecimalbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelTimebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.CaribelComparator;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.swing2.JCariTextField;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

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
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

public class AccessiFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = 1L;
	
	private CaribelIntbox 	int_contatore;
	private CaribelTextbox 	int_anno;
	private CaribelTextbox 	cod_operatore;
	private CaribelCombobox 	desc_operatore;

	private String myKeyPermission = ChiaviISASSinssntWeb.INTERV;
	private IntervEJB myEJB = new IntervEJB();

	private CaribelCombobox cbx_contatto;
	private CaribelTextbox n_cartella;
	private Button btn_apriCartella;
	private CaribelRadiogroup tipoPrestazione;
	private CaribelCombobox cognomeAss;
	
	private CaribelListbox tablePrestazioni;
	private CaribelListbox caribellb2;
	
	private CaribelTextbox JCariTextFieldRegione;
	private CaribelTextbox JCariTextFieldUsl;
	private CaribelTextbox JCariTextFieldTipo_oper;
	
	private Component unitaFunzionale;
	private CaribelCombobox cbx_unitaFunzionale;
	private Label labelUnitaF;
	
	private Component operatore;
//	private Button btn_annullaSelezione;
	private Button btn_confermaSelezione;

	private CaribelTextbox JCariTextFieldTipoServizio;
	
	private CaribelTextbox cod_presidio;
	private CaribelCombobox desc_presidio;
	private CaribelTextbox tipo_prestazione;

	private CaribelTextbox jCariTextFieldCombo;
	private CaribelTextbox JCariTextFieldTipoAccesso;
	
	private CaribelDatebox dateCartIni;
	private CaribelDatebox dateCartFin;
	
	private String vecchiadata;
	
	private CaribelTextbox jCariTextFieldTipoOperConstraint;
	private Label lbl_contatto;
	private Label lbx_accessi_note;
	private Label lbx_accessi_operatore;
	private CaribelIntbox int_tempo;
	
	private Date data_oggi = procdate.getDate();
	private CaribelDatebox int_data;
	private Component assistito;
	private Component presidio;
	CaribelSearchCtrl assistitoCtrl;
	CaribelSearchCtrl operatoreCtrl;
	CaribelSearchCtrl presidioCtrl;
	//	private CaribelCombobox qualOperatore;
	private CaribelTextbox qualOperatore;
	private CaribelTextbox qualificaOperatore;
	
	protected String pathGridZul = "/web/ui/sinssnt/interventi/accessiGrid.zul";

	private ISASUser iu;

	private Button btn_formgrid_update;

	private Label lbl_noBtnApriCartella;

	private CaribelTextbox codPrestazione;
	private CaribelTextbox descPrestazione;
	private CaribelListModel modelloPrestazioni = new CaribelListModel();
	private Hashtable datiFormPrimaRichiestaSO = null;
	
	private Predicate filtroPrestazioni;

	public void doInitForm() {
		try {
			currentIsasRecord = null;
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			iu = CaribelSessionManager.getInstance().getIsasUser();

			this.setMethodNameForDelete("deleteAll");
			
			if(n_cartella.getEventListeners(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH)!= null && !(n_cartella.getEventListeners(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH).iterator().hasNext())){
				n_cartella.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						ExecSelectCartella();
						verificaDataInSchedaSo(false, 0);
						return;
					}});
			}
			operatoreCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
//			operatoreCtrl.setMethodNameForQuery("query_operatoreQual");
//			operatoreCtrl.setMethodNameForQueryKey("query_operatoreQual");
			operatoreCtrl.putLinkedComponent("cod_presidio", cod_presidio);
			operatoreCtrl.putLinkedComponent("des_presidio", desc_presidio);
			operatoreCtrl.putLinkedComponent("tipo", JCariTextFieldTipo_oper);
			operatoreCtrl.putLinkedComponent("int_tipo_prest", tipo_prestazione);
			operatoreCtrl.putLinkedComponent("des_qual", qualOperatore);
			operatoreCtrl.putLinkedComponent("cod_qualif", qualificaOperatore);
			if(!(UtilForContainer.getContainerCorr() instanceof ContainerPuacCtrl)){
				operatoreCtrl.putLinkedSearchObjects("tipo", JCariTextFieldTipo_oper);
				operatoreCtrl.putLinkedSearchObjects("tipo_oper_constraint", jCariTextFieldTipoOperConstraint);
				operatoreCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, jCariTextFieldTipoOperConstraint);
			}
			operatoreCtrl.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
			
			presidioCtrl = (CaribelSearchCtrl) presidio.getAttribute(MY_CTRL_KEY);
			if(cod_operatore.getEventListeners(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH) != null && !(cod_operatore.getEventListeners(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH).iterator().hasNext())){
				cod_operatore.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						ExecSelectOperatori(false);
						Events.sendEvent(Events.ON_CHANGE, cod_presidio, cod_presidio.getValue());
						if(cod_operatore.getValue()==null || cod_operatore.getValue().isEmpty()){
							JCariTextFieldTipo_oper.setValue(null);
							tipo_prestazione.setValue(null);
							qualOperatore.setValue(null);
							qualificaOperatore.setValue(null);
							Events.sendEvent(Events.ON_CHANGE, cod_prestazione, null);
							
						}
						return;
					}});
			}
			String title;
			String iProv = (String) arg.get("provAccessiPrestazioni");
			if(iProv!=null && ((String) iProv).equals("1")){
				// 14/05/08 m.: x modifica SINS 14/03/08
				PROVENIENZA = "ACCES";
				//14/03/2008 OPERATORIOK =OperatoriAmmessiInterventi;
				OPERATORIOK = getProfile().getStringFromProfile(ManagerProfile.OP_ACC_GEN);
				logger.debug("operatori generali:" + OPERATORIOK);

				title = Labels.getLabel("accessiPrestazioni.formTitle");
				lbx_accessi_note.setValue(Labels.getLabel("accessiPrestazioni.note"));
				lbx_accessi_operatore.setValue(Labels.getLabel("accessiPrestazioni.operatore"));

				cbx_contatto.setVisible(true);
			} else {
				// 14/05/08 m.: x modifica SINS 14/03/08
				PROVENIENZA = "OCCAS";
				//14/03/2008 OPERATORIOK =OperatoriAmmessiOccasionali ;
				OPERATORIOK = getProfile().getStringFromProfile(ManagerProfile.OP_ACC_OCC);
				logger.debug("operatori occasionali:" + OPERATORIOK);

				title = Labels.getLabel("accessiPrestazioni.formOccasionaliTitle");
				
				lbx_accessi_note.setValue(Labels.getLabel("accessiPrestazioni.note.accessi.occasionali"));
				lbx_accessi_operatore.setValue(Labels.getLabel("accessiPrestazioni.operatore.accessi.occasionali"));
				
				cbx_contatto.setVisible(false);
				cbx_contatto.setDisabled(true); //gb 29/10/07
				lbl_contatto.setVisible(false);
			}
			
			if(getForm() instanceof Window){
				((Window) getForm()).setTitle(title);
			}

			// 26/04/11: solo per accessi STANDARD di tutti: INF, FIS, MED ed ONC
			btn_apriCartella.setVisible(iProv==null|| ((String) iProv).equals("1"));
			
			String strTipoOperatore = "";
//			int tipoOperatore = 0;// 28/09/07
			String tipoOp = UtilForContainer.getTipoOperatorerContainer();//(String) arg.get("tipoOp");

			if(UtilForContainer.getContainerCorr() instanceof ContainerPuacCtrl){
				tipoOp = null;//lo valorizzo al momento di scelta dell'operatore
			}
			
			//inizializzo il progressivo a 0 se inserisco è giusto se è una modifica verrà sovrascritto
			int_contatore.setValue(0);		
			btn_search.setVisible(true);
			if((arg.get("mode") !=null && arg.get("mode").equals("overlapped"))||caribelContainerCtrl==null){
				((Window)getForm()).setMode("overlapped");
				((Window)getForm()).setPosition("center");
				((Window)getForm()).setClosable(true);
				((Window)getForm()).setSizable(true);
				((Window)getForm()).setMaximizable(true);
				((Window)getForm()).setMinimizable(false);
				((Window)getForm()).setWidth("96%");  
				((Window)getForm()).setHeight("93%");
				((Window)getForm()).setVflex("");
			}
			tipoPrestazione.setSelectedValue("D");
			
			Window dett = (Window) self.getFellowIfAny("datiDettagli", true);
			if(tablePrestazioni==null){
				tablePrestazioni = (CaribelListbox) dett.getFellowIfAny("tablePrestazioni",  true);
			}
			
			if(btn_confermaSelezione==null){
				btn_confermaSelezione = (Button) dett.getFellowIfAny("btn_confermaSelezione",  true);
			}
			if(cod_prestazione==null){
				cod_prestazione = (CaribelTextbox) dett.getFellowIfAny("cod_prestazione",  true);
			}

			if(desc_prestazione==null){
				desc_prestazione = (CaribelCombobox) dett.getFellowIfAny("desc_prestazione",  true);
			}
			
			if(sp_quantita==null){
				sp_quantita = (CaribelIntbox) dett.getFellowIfAny("sp_quantita",  true);
			}
			
			if(pre_tempo==null){
				pre_tempo = (CaribelTextbox) dett.getFellowIfAny("pre_tempo",  true);
			}
			
			if(pre_note==null){
				pre_note = (CaribelTextbox) dett.getFellowIfAny("pre_note",  true);
			}
			
			if(pre_importo==null){
				pre_importo = (CaribelDecimalbox) dett.getFellowIfAny("pre_importo",  true);
			}
			
			if(pre_des_dett==null){
				pre_des_dett = (CaribelTextbox) dett.getFellowIfAny("pre_des_dett",  true);
			}
			
			if(caribellb2 == null){
            	caribellb2 = (CaribelListbox) self.getFellowIfAny("datiDettagli").getFellowIfAny("caribellb2", true);
            }
            caribellb2.clearSelection();
            tablePrestazioni.clearSelection();
			caribellb2.setModel(new CaribelListModel());
            
            if(myForm == null){
            	myForm = self.getFellowIfAny("datiDettagli").getFellowIfAny("myForm", true);
            }
//            if(btn_formgrid_update == null){
//            	btn_formgrid_update = (Button) self.getFellowIfAny("datiDettagli").getFellowIfAny("btn_formgrid_update", true);
//            	btn_formgrid_update.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
//    				public void onEvent(Event event) throws Exception {
//    					doSaveForm();
//    				}});
//            }
			//spostato prima dell'initform per averlo valorizzato nel setState insert) 
			if(tipoOp!=null){
				strTipoOperatore = (String) tipoOp;
			}

			//gb 28/05/07 ***
			if (strTipoOperatore.length() < 2 && !strTipoOperatore.equals(""))
				strTipoOperatore = "0" + strTipoOperatore;
			gl_strTipoOperatore = strTipoOperatore;
			//gb 28/05/07: fine ***
			
			initForm();

			if(arg.get("int_contatore")!=null){
				hParameters.put("int_contatore", (String)arg.get("int_contatore"));
				hParameters.put("int_anno", (String)arg.get("int_anno"));
				this.presidioFlussiSPR = (String)arg.get("presidioSpr");
				this.prestazioneFlussiSPR = (String)arg.get("prestazioneSpr");
				doQueryKeySuEJB();
				doWriteBeanToComponents();
				
			}else if(dbrFromList!=null){
//				if(this.caribelContainerCtrl!=null){
				
//					((Window)self).setMode(Mode.OVERLAPPED);
//					((Window)getForm()).setPosition("center");
//					((Window)getForm()).setClosable(true);
//					((Window)getForm()).setSizable(true);
//					((Window)getForm()).setWidth("96%");  
//					((Window)getForm()).setHeight("93%");
					//this.caribelContainerCtrl.showComponent(comp);
//				}
				hParameters.put("int_contatore", dbrFromList.get("int_contatore"));
				hParameters.put("int_anno", dbrFromList.get("int_anno"));
				this.presidioFlussiSPR = (String) dbrFromList.get("presidioSpr");
				this.prestazioneFlussiSPR = (String) dbrFromList.get("prestazioneSpr");
				tipoOp = (String) dbrFromList.get("int_tipo_oper");
				if(iProv==null){
					PROVENIENZA =((Integer) dbrFromList.get("int_contatto"))==0?"OCCAS":"ACCES";
				}
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}else if(caribelContainerCtrl!=null && 
					super.caribelContainerCtrl.hashChiaveValore.containsKey("n_cartella") && 
					super.caribelContainerCtrl.hashChiaveValore.containsKey("n_contatto")){
//				h_DatiAss.put("n_cartella", super.caribelContainerCtrl.hashChiaveValore.get("n_cartella"));
//				h_DatiAss.put("n_contatto", super.caribelContainerCtrl.hashChiaveValore.get("n_contatto"));
//				faiAssistitoDefinito();
//				//se sono all'interno di un contatto precarico i valori di cartella e contatto con quelli correnti
				n_cartella.setValue(super.caribelContainerCtrl.hashChiaveValore.get("n_cartella").toString());
				Events.sendEvent(Events.ON_CHANGE, (Component)n_cartella, null);
//				cbx_sc_contatto.setSelectedValue(super.caribelContainerCtrl.hashChiaveValore.get("n_contatto"));
				int_contatore.setValue(0);

				setStato(this.INSERT);
				
				//VFR requisito indicato da Barbara nella mail del 26/04/2017
				if(ManagerProfile.isConfigurazioneMarche(getProfile())){
				}else{
					int_data.setValue(procdate.getDate());
				}
				
				btn_apriCartella.setVisible(false);
				lbl_noBtnApriCartella.setVisible(true);
				ExecSelectCartella();
//				caricaDatiContatti();
			}else{
				if(int_data.getValue()==null){
					int_data.setFocus(true);
				}else{
					n_cartella.setFocus(true);
				}
				if(n_cartella.getValue()!=null && !n_cartella.getValue().isEmpty()){
					Events.sendEvent(Events.ON_CHANGE, (Component)n_cartella, null);
					//int_contatore.setValue(0); VFR eliminato il reset dell'accesso se carico un accesso non riuscivo ad aggirnare una prestazione, non credo serva resettare? 
				}
			}
			
			ExecSelect();
			notEditable();
			if(currentIsasRecord!=null){
				if(n_cartella!=null){
					Events.sendEvent(Events.ON_CHANGE, n_cartella, null);
				}
				caricaDatiContatti();
				if(presidioFlussiSPR != null){
					logger.debug(" Dati che mi presidioSpr>>"+presidioFlussiSPR+"< prestazioneSpr>"+prestazioneFlussiSPR+"<\n ");
					popoladatiFlussoSpr();
				}
				SettaTipoPrest();
			}
			Calendar cal = Calendar.getInstance();
			cal.setTime(data_oggi);
//			int_anno.setValue(new Integer(cal.get(Calendar.YEAR)).toString()); //VFR rimossoAnno
			if(btn_confermaSelezione.getEventListeners(Events.ON_CLICK) != null && !(btn_confermaSelezione.getEventListeners(Events.ON_CLICK).iterator().hasNext())){
				btn_confermaSelezione.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						onConfermaSelezione(event);
						return;
					}});
			}
			
			
			if(codPrestazione==null)
				codPrestazione= (CaribelTextbox) dett.getFellowIfAny("codPrestazione");
			if(descPrestazione==null)
				descPrestazione= (CaribelTextbox) dett.getFellowIfAny("descPrestazione");
			codPrestazione.addEventListener(Events.ON_OK, new EventListener<Event>() {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public void onEvent(Event event){
					try{
						if(event.getName().equals(Events.ON_OK)){
							Collection col = getPrestazioni();
							CaribelListModel mod = new CaribelListModel(col);
							mod.setMultiple(true);
							tablePrestazioni.setModel(mod);
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
			
			filtroPrestazioni = new FiltroCodiceDescrizione(null, descPrestazione, tablePrestazioni, "pre_cod_prest", "prest_des");
			((FiltroCodiceDescrizione)filtroPrestazioni).setMantieniSelezione(true);
			
			if(ManagerProfile.isConfigurazioneMarche(getProfile())){
				btn_apriCartella.setVisible(false);
				lbl_noBtnApriCartella.setVisible(true);
			}
			
			doFreezeForm();
		}catch(Exception e){
			doShowException(e);
		}
	}


	protected String getPathGridZul(){
		return pathGridZul;
	}
	
	protected Map<String, Object> getParametersForGrid(){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("tipo", gl_strTipoOperatore);
		params.put("prov", PROVENIENZA);
		params.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		if(int_anno.getValue()!= null ){
			params.put("anno", int_anno.getValue());
		}
		return params;
	}
	
	//-------------------------------------------------------------------------------------------------
	// Form di inserimento di interventi e prestazioni.
	// INSERIMENTO: si inseriscono i dati dell'intervento e di una prestazione==>viene fatta una
	// insert sul database (tabelle interv e intpre); i successivi inserimenti di prestazioni
	// comportano delle insert solo su intpre;
	// il contatore dell'intervento e' generato automaticamente al salvataggio;
	//
	// MODIFICA: i dati sono modificabili solo da parte di chi ha effettuato l'inserimento;
	//
	// GESTIONE GENERALE: la chiusura nel pannello delle prestazioni //termina l'inserimento/modifica
	// delle prestazioni e della griglia;non provoca lo sbiancamento di tutti i campi per permettere
	// di inserire altri interventi sulla stessa cartella, nella stessa data, nello stesso presidio

	// 13/03/2008 Gli operatori che possono inserire accessi vengono letti dal configuratore
	// attraverso la chiave OP_ACC_GEN (operatori abilitati all'inserimento degli accessi generali) e
	// OP_ACC_OCC (operatori abilitati all'inserimento degli accessi occasionali)
	//-------------------------------------------------------------------------------------------------

		private final int WAIT = 0;
		private final int INSERT = 1;
		private final int UPDATE_DELETE = 2;
		private final int CONSULTA = 3;

		// 04/11/10
		private final String nomeFinestraChgOp = ChiaviISASSinssntWeb.INTCHGOP;
		private static final String MIONOME = "13-JFrameInterv.";

		private CaribelTextbox JCariTextFieldChiaveCombo = null;

		private String presidioFlussiSPR = "";
		private String prestazioneFlussiSPR = "";

		private boolean modi_op = false;
		//----stato della form ->=0 attesa, =1 insert, =2 update/delete
		private int stato = 0;
		int numRiga = 0;
		boolean insertDett = false;
		boolean salvato = false;
		String tipo = "";
		String conta = "";
		boolean cancellato = false;
//		ProgettoSinsTableCellRenderer renderer;
		JCariTextField unita = new JCariTextField();
		boolean update_prestaz = false;
		private int indiceCombo = -1;

		//gb 28/05/07: valorizzato col tipo operatore proveniente dal costruttore
		private String gl_strTipoOperatore = ""; //gb 28/05/07

		/*mi carica tutto quello che riguarda l'assistito
		   E' stato fatto per fare un unico accesso al db
		   mi restituisce un hashtable con 5 chiavi

		   chiave :n_cartella       valore :cognome nome assistito
		   chiave :"contatti"       valore :vettore di ISASREcord con tutti i contatti .Viene scorso lato client
		                                       per riempire la combo
		   chiave :"pianoInterv"    valore : hashtable che ha come chiave il numero contatto e come valore
		                                       il vettore di ISASrecord con le prestazioni*/
		private Hashtable hDati = new Hashtable();

		private String OPERATORIOK = "";
		private String PROVENIENZA = "";

		private Hashtable hTipo = new Hashtable();
		/*ilaria :variabile valorizzata al caricamento della frame
		se vale true vuol dire che si deve valorizzare con i valori di default
		*/
		boolean valoriDef = false;
		boolean valoriDef_unifun = false;

		// 31/05/11
		boolean durataObbl = false;

		private Component myForm;
		private CaribelTimebox int_ora_in;
		private CaribelTimebox int_ora_out;
		private CaribelTextbox cod_prestazione;
		private Hashtable h_cartella;

		private CaribelCombobox desc_prestazione;

		private CaribelIntbox sp_quantita;

		private CaribelDecimalbox pre_importo;

		private CaribelTextbox pre_tempo;
		private CaribelTextbox pre_des_dett;
		private CaribelTextbox pre_note;

		private CompareHashtable Hgriglia = null;
		private CompareHashtable Hop = null;

		private Vlayout hopVLayout;

		private CaribelTextbox contatto;

		private String ver = "9-";

		private static final int CTS_SO_PRESENTE = 0;
		private int check_save_SO_PRESENTE = CTS_SO_PRESENTE;

		private int provenienza = 0;
		private int CTS_SAVE_FORM = 1;
		private int CTS_BOTTONE = 2;
		private static final int CTS_SO_PRESENTE_SAVE = 1;
		private static final String CTS_LABEL_SO_PROCEDERE = "accessi.no.scheda.so.proseguire";
		

		public int ExecSelect() throws Exception {
//			int count = db.SelectAll(interv);
//			logger.debug("HASH->" + interv.getHashtableKeyValue().toString());
//			if (count < 0) {
//				setDefault();
//				setStato(this.WAIT);
//				return count;
//			}
//			if (count > 0) {
			if(currentIsasRecord!=null){
				/*Controllo che sia un intervento effettuato da personale no asl
				*/
				if (PROVENIENZA.equals("ACCES") && this.JCariTextFieldTipoAccesso.getValue().equals("ESTERNO")) {
//					setDefault();
					setStato(this.WAIT);
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaProvenienzaTipoAccesso.specialista"));
					return 1;
				} else if (PROVENIENZA.equals("OCCAS") && this.JCariTextFieldTipoAccesso.getValue().equals("INTERNO")) {
//					setDefault();
					setStato(this.WAIT);
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaProvenienzaTipoAccesso.tipoSpecialistico"));
					return 1;
				}
				salvato = false;
				tipo = this.tipo_prestazione.getValue();
				logger.debug("TIPO PRESTAZIONE--> '" + tipo + "'");
				ExecSelectCartella();
				logger.debug("TIPO PRESTAZIONE Dopo exec cartella--> '" + this.tipo_prestazione.getValue().trim() + "'");
				String chiave_combo = JCariTextFieldChiaveCombo.getValue();
				//SettaTipoPrest();
				update_prestaz = false;
				doFreezeForm();
//				tH = new compareHashtable(t.getHashtableKeyValue());
//				Hop = new compareHashtable(tabop.getHashtableKeyValue());
//				Hgriglia = new compareHashtable(forGriglia.getHashtableKeyValue());
				setStato(this.UPDATE_DELETE);
				/*Attenzione questo lo faccio perchè la
				cbx_contatto.setMyvalue(chiave_combo);
				schianta.
				*/
				if (PROVENIENZA.equals("ACCES")) {
					logger.debug("TIPO SERVIZIO-->" + this.JCariTextFieldTipoServizio.getValue());
					for (int i = 0; i < cbx_contatto.getItemCount(); i++) {
						cbx_contatto.getItemAtIndex(i);
//						if (cbx_contatto.getMyvalues(i).equals(chiave_combo)) {
//							cbx_contatto.setSelectedIndex(i);
//							break;
//						}
					}
					cbx_contatto.setSelectedValue(chiave_combo);cbx_contatto.getSelectedValue();
				}

				CaricaGrigliaPrestaz();
				logger.debug("TIPO PRESTAZIONE Dopo CaricaGrigliaPrestaz--> '" + this.tipo_prestazione.getValue() + "'");

			} else {
//				if (!iu.canIUse(myKeyPermission, "INSE")) {
//					new it.pisa.caribel.swing2.cariInfoDialog(null, "Attenzione!", "Nessun record trovato!").show();
////					setDefault();
//					setStato(this.WAIT);
//					return count;
//				} else {
//					int i = new it.pisa.caribel.swing2.cariYesNoDialog(null, "Nessun record trovato!\nVuoi Inserire un Record?", "Attenzione!")
//							.show();
//					if (i == 1) {
//						setDefault();
//						setStato(this.WAIT);
//						return count;
//					} else {
//						salvato = false;
//						int_contatore.setUnmaskedText("0");
//						setStato(this.INSERT);
//						//insertPrest();
//						tH = new compareHashtable(t.getHashtableKeyValue());
//					}
//				}
			}
			if (caribellb2.getItemCount()>0) //this.jCariTable1.getRowCount() > 0)
				insertDett = true;
			else
				insertDett = false;
			return currentIsasRecord!=null? 1 : 0 ;
		}

		private void insertPrest() throws Exception {
			Hashtable h = new Hashtable();
			//J16-03 se il tipo è 01 gli passo anche il tipo servizio che mi differenzia il
			//tipo di prestazione: TServ=1=>TPrest=01 o TServ=2=>TPrest=01B
			//tipo_oper=getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE);

			String tipo_oper = JCariTextFieldTipo_oper.getValue();
			if (tipo_oper.equals("01")) {
				h.put("tipo_servizio", tipoServizio());
			}
			h.put("cod_oper", cod_operatore.getValue());
			if (!tipo_oper.equals("")) {
				h = (Hashtable) invokeGenericSuEJB(myEJB, h, "prestDef");// db.PrestazDefault(h);
				this.tipo_prestazione.setValue((String) h.get("tipo_prest"));
			}
		}	

		private String tipoServizio() {
			String tipoServ = "";
			String tipo_oper = JCariTextFieldTipo_oper.getValue();
			if (tipo_oper.equals("01")) {
				if (jCariTextFieldCombo.isVisible()) {
					String descr = jCariTextFieldCombo.getValue();
					int car = descr.length() - 1;
					tipoServ = descr.substring(car);
				} else if (cbx_contatto.isVisible() && (cbx_contatto.getSelectedIndex() != -1)) {
					String descr = (String) cbx_contatto.getSelectedValue();
					int car = descr.length() - 1;
					tipoServ = descr.substring(car);
				}
			}
			logger.debug("\n-->> In tipoServizio: tipoServ = " + tipoServ);
			return tipoServ;
		}

		//FUNZIONE PER PRENDERE LA DATA DI SISTEMA NEL FORMATO DESIDERATO
		public static String getjdbcDate() {
			java.util.Date d = procdate.getDate();
			java.text.SimpleDateFormat local_dateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy");
			return local_dateFormat.format(d);
		}	

		private int ControlloData1() {
			String data = this.int_data.getText();
			String oggi = this.getjdbcDate();
			String anno = data.substring(6, 10);
			String anno_oggi = oggi.substring(6, 10);
			int anno1 = (new Integer(anno)).intValue();
			int anno2 = (new Integer(anno_oggi)).intValue();
			//logger.debug("Anno1 :"+anno1+" Anno2: "+anno2);
			if (anno1 == anno2 || anno1 == anno2 - 1 || anno1 == anno2 - 2)
				return 1;
			else {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.validaControlloData1"));
				this.int_data.focus();
				return -1;
			}
		}

		public int ExecSelectOperatori(boolean resend) throws Exception {
			/*
			  mi vado a salvare il tipo prestazione relativo all'operatore
			*/
			String codpres = cod_presidio.getValue();
			String despres = desc_presidio.getValue();

			String tipo = tipo_prestazione.getValue();
			String tipoOperatore = JCariTextFieldTipo_oper.getValue();

			if(!(UtilForContainer.getContainerCorr() instanceof ContainerPuacCtrl)){
				// 10/01/08 ---
				String tpOper = (getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE).equals("05") ? "05" : gl_strTipoOperatore);
				JCariTextFieldTipo_oper.setValue(tpOper);
				jCariTextFieldTipoOperConstraint.setValue(gl_strTipoOperatore); //gb 28/05/07
				// 10/01/08 ---
				logger.debug("::::::: INTERV JCariTextFieldTipo_oper=[" + tipoOperatore + "] - gl_strTipoOperatore=[" + gl_strTipoOperatore + "]");
			}else{
				gl_strTipoOperatore = tipoOperatore;
			}
			
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			int count=0;
			if (resend) {
				Events.sendEvent(Events.ON_CHANGE, cod_operatore, cod_operatore.getValue());
			}

			if(cod_operatore.getValue()==null || cod_operatore.getValue().isEmpty()){
//			if (count <= 0) {
//				/*se ho inserto delle prestazioni devo riassegnare il tipo prestazione che mi è stato
//				sbiancato dalla select*/
//				if (cariStringTableModel1.getRowCount() > 0 || !this.JCariTextFieldPrestaz_1.getUnmaskedText().equals("")) {
				if(caribellb2.getItemCount()>0 || cod_prestazione.getValue() != null){
					tipo_prestazione.setValue(tipo);
					JCariTextFieldTipo_oper.setValue(tipoOperatore);
				} else {
					tipo_prestazione.setValue(null);
					JCariTextFieldTipo_oper.setValue(null);
				}
				//sbianco la tabella
				modelloPrestazioni = new CaribelListModel();
				tablePrestazioni.setModel(modelloPrestazioni);
			} else {
				count=1;
//				operatoreCtrl.setReadonly(true);
				//controllo che il tipo operatore sia del tipo scelto
				if (OPERATORIOK.indexOf(JCariTextFieldTipo_oper.getValue().trim()) == -1) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.operatoreNonCoerente"));
					cod_operatore.setValue(null);
					if (resend){
						Events.sendEvent(Events.ON_CHANGE, cod_operatore, cod_operatore.getValue());
					}
					tipo_prestazione.setValue(tipo);
					JCariTextFieldTipo_oper.setValue(tipoOperatore);
					cod_presidio.setValue(codpres);
					Events.sendEvent(Events.ON_CHANGE, cod_presidio, cod_presidio.getValue());
				}
				
				//se ci sono delle prestazioni controllo che il tipo sia lo stesso altrimenti lo impedisco
				if (caribellb2.getItemCount() > 0 || (cod_prestazione.getValue() != null && !cod_prestazione.getValue().isEmpty())) {
					boolean operatoreOk = true;
					//controllo il tipo operatore se è cambiato non lo faccio cambiare
					if (!tipoOperatore.equals(JCariTextFieldTipo_oper.getValue())) {
						operatoreOk = false;
					} else {/*il tipo oepratore è lo stesso controllo allora il tipo prestazione
					                          Ad esempio per assistente sociale posso avere lo stesso tipo operatore,
					                          ma la prestazione differente perche dipende dal tipo servizio che c'è sul contatto
					                    */
						if (!tipo.equals(tipo_prestazione.getValue()))
							operatoreOk = false;
					}
					if (!operatoreOk) {
						UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.operatoreNonCompatible"));
						cod_operatore.setValue(null);
						if (resend){
							Events.sendEvent(Events.ON_CHANGE, cod_operatore, cod_operatore.getValue());
						}
						tipo_prestazione.setValue(tipo);
						JCariTextFieldTipo_oper.setValue(tipoOperatore);
						cod_presidio.setValue(codpres);
						Events.sendEvent(Events.ON_CHANGE, cod_presidio, cod_presidio.getValue());
					} else {
						CaricaGrigliaPrestaz();
						CaricaHDati();
					}
				} else {
					CaricaGrigliaPrestaz();
					CaricaHDati();
					
					//ricarico il piano interventi
					if (stato == INSERT) {
						logger.debug("onChangeContatto-->>: Chiamo CaricaPianoInterventi");
						CaricaPianoInterventi();

						// 18/03/11
						if ((PROVENIENZA.equals("ACCES")) && (gl_strTipoOperatore.equals(CostantiSinssntW.TIPO_OPERATORE_INFERMIERE)))
							caricaModalita();
					}
				}

				// 26/04/11: nel caso di ingresso da JCCXXXCartella con oper non dello stesso tipo dell'accesso e
				// che quindi viene scelto successivamente
				setComboCont();
			}
			return count;
		}

		public void CaricaHDati() throws Exception {//QUESTA FUNZIONE VA AL SERVER
			//verifica se deve andare a caricarsi i dati per quell'assistito
			boolean esegui = true;
			if (hDati != null && hDati.get(this.n_cartella.getValue()) != null) { /*se entro qui vuol dire che i dati per quella cartella sono già stati caricati
			           controllo se sono di quel tipo di operatore
			           */
				String tipoOperatore = this.JCariTextFieldTipo_oper.getValue();
				if (hDati.get("tipo_oper") != null && ((String) hDati.get("tipo_oper")).equals(tipoOperatore)) {//se entro qui vuol dire che i dati per quell'operatore sono stati caricati
					//non devo ricaricare i dati
					esegui = false;
				}
			}

			if (cod_operatore.getValue() == null || cod_operatore.getValue().isEmpty())
				esegui = false;
			if (n_cartella.getValue() == null || n_cartella.getValue().isEmpty())
				esegui = false;
			if (esegui) {
				sbiancaCombo();
				hDati =  (Hashtable) invokeGenericSuEJB(myEJB, h_cartella, "query_allcartella");
				caricaDatiContatti();
			}

		}

		public int ExecUpdate() throws Exception {
			//02/11/06 Riproporre sempre l'ultimo anno inserito
			String anno = (String) int_anno.getValue();
			if (this.stato == INSERT || modi_op)
				if (ControlloData1() < 0)
					return -1;
			int count = 0;
			count = this.SalvaDettagli();
			if (count == -1)
				this.insertDett = false;
			else {
				if (Hop != null && Hop.isModified(UtilForBinding.getHashtableFromComponent(hopVLayout)))
					modi_op = true;

				if (PROVENIENZA.equals("ACCES")) {
					String data2 = (String) this.cbx_contatto.getValue();
					StringTokenizer tok = new StringTokenizer(data2, ":");
					data2 = tok.nextToken();
					int ris = this.ControlloData(data2, UtilForBinding.getStringClientFromDate(int_data.getValue()));
					if (ris == -1) {
						int_data.focus();
						return -1;
					}
				}
				if ((Hgriglia != null && Hgriglia.isModified(UtilForBinding.getHashtableFromComponent(caribellb2))) || !insertDett) {
					if (numRiga == -1)
						insertDett = true;
					this.ExecInsertGrid();
				}
				this.Salvataggio();
			}
			int_anno.setValue(anno);
			return count;
		}

		private int SalvaDettagli() {
			String nuovo_valore = cod_prestazione.getValue().trim();
			numRiga = caribellb2.getSelectedIndex();
			if (caribellb2.getItemCount()> 0) {
				Hashtable hNuovo = new Hashtable();
				hNuovo.put("pre_cod_prest", nuovo_valore);
				int riga = ((CaribelListModel) caribellb2.getModel()).columnsContains(hNuovo);
				if (riga != -1)//-1 non trova nulla
				{
					if (numRiga != riga)//se numRiga=riga=>sono in modifica
					{
						UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneGiaInserita"));
//						cod_prestazione.setValue(null);
						Events.sendEvent(Events.ON_CHANGE, cod_prestazione, null);
						return -1;
					} else {
						conta = "1";
						return 0;
					}//fine else
				} else {//fine riga!=-1
					conta = "0";
					numRiga = -1;
				}
			}//if esterno al for!
			else
				numRiga = -1;
			return 0;
		}

		//-------- gestione decodifica cartella --------
		public void ExecSelectCartella() throws Exception {
			h_cartella = new Hashtable();
			h_cartella.put("n_cartella", this.n_cartella.getValue());
			h_cartella.put("tipo_oper", JCariTextFieldTipo_oper.getValue());
			
			controlloZonaOperatore();
			
			hDati.clear();
			sbiancaCombo();
			cognomeAss.setValue(null);
			// 10/02/11 ---
			logger.debug("XXXXX INTERV JCariTextFieldTipo_oper=[" + JCariTextFieldTipo_oper.getValue()
					+ "] - gl_strTipoOperatore=[" + gl_strTipoOperatore + "]");
			String appoTpOper = JCariTextFieldTipo_oper.getValue();
			JCariTextFieldTipo_oper.setValue(gl_strTipoOperatore);
			// logger.debug("YYYYY INTERV JCariTextFieldTipo_oper=["+JCariTextFieldTipo_oper.getValue()+"] - gl_strTipoOperatore=["+gl_strTipoOperatore+"]");
			// 10/02/11 ---

			hDati = (Hashtable) invokeGenericSuEJB(myEJB, h_cartella, "query_allcartella");//db.SelectCartella(cartella);
			h_cartella = (Hashtable) invokeGenericSuEJB(myEJB, h_cartella, "query_anagra");// db.select_anagra(h_cartella);
			// 10/02/11 ---
			JCariTextFieldTipo_oper.setValue(appoTpOper);
			// 10/02/11 ---

			if (hDati.get(this.n_cartella.getValue()) == null
					|| hDati.get(this.n_cartella.getValue()).equals("")) {
//				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noContatti"));
//				new it.pisa.caribel.swing2.cariInfoDialog(null, "Codice Cartella: Non Esiste Decodifica", "Attenzione!").show();
				n_cartella.setValue(null);cognomeAss.setValue(null);
				cbx_contatto.setSelectedIndex(-1);
				// 01/09/08 m. ---
				this.dateCartIni.setValue(null);
				this.dateCartFin.setValue(null);
				// 01/09/08 m. ---
			} else {
				String cognome = (String) hDati.get(this.n_cartella.getValue());
				cognomeAss.setValue(cognome);

				// 01/09/08 m. ---
				String dtApeCart = (String) hDati.get("data_apertura");
				if (dtApeCart != null)
					this.dateCartIni.setValue(UtilForBinding.getDateFromIsas(dtApeCart));
				else
					this.dateCartIni.setValue(null);
				String dtChiusCart = (String) hDati.get("data_chiusura");
				if (dtChiusCart != null)
					this.dateCartFin.setValue(UtilForBinding.getDateFromIsas(dtChiusCart));
				else
					this.dateCartFin.setValue(null);
				// 01/09/08 m. ---

				caricaDatiContatti();
			}
		}

		public void caricaDatiContatti() throws Exception {
			//carico la combo dei contatti solo se mi hanno inserito la data
			if(cod_operatore.getValue()==null || cod_operatore.getValue().isEmpty())
				return;
			if (n_cartella.getValue()==null||n_cartella.getValue().isEmpty())
				return;
			if (int_data.getValue()==null)
				return;
			caricaComboContatti();
		}

		private void caricaComboContatti() throws Exception {
			//  logger.debug("HASH DATI-->>"+ hDati.toString() );
			cbx_contatto.clear();
			int k = 0;
			if (hDati.get("contatti") != null) {
				Vector v = new Vector();
				v = (Vector) hDati.get("contatti");
				if (PROVENIENZA.equals("ACCES") && v.size() == 0) {
					cbx_contatto.setSelectedIndex(-1);
					tipoPrestazione.setDisabled(false);
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noContatti"));
				} else {
					String[] myValue = new String[v.size()];
					//    int k=0;
					for (int i = 0; i <= v.size() - 1; i++) {
						try {
							it.pisa.caribel.isas2.ISASRecord r = (it.pisa.caribel.isas2.ISASRecord) v.elementAt(i);
							/*devo inserirlo nella combo solo se il contatto è aperto nella data di intervento */
							//confronto la data apertura con la data della prestazione
							if (!DateUtils.truncate((java.sql.Date) r.get("data_contatto"), Calendar.DAY_OF_MONTH).after(int_data.getValue())){
								if (r.get("data_chiusura") != null) {
									if (!int_data.getValue().after(((java.sql.Date) r.get("data_chiusura")))) {
										String elementoDec = "" + r.get("descrizione");
										String valore = "" + r.get("kcombo");
										CaribelComboRepository.addComboItem(cbx_contatto, valore, elementoDec);
										k++;
									}
								} else {
									String elementoDec = "" + r.get("descrizione");
									String valore = "" + r.get("kcombo");
									CaribelComboRepository.addComboItem(cbx_contatto, valore, elementoDec);
									k++;
								}
							}
						} catch (Exception e) {
							cbx_contatto.clear();
							tipoPrestazione.setDisabled(false);
							UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.erroreCaricamentoContatti"));
							break;
						}
					}//fine for
					if (k != 0) {
						if (PROVENIENZA.equals("ACCES")) {
							if (k == 1)
								cbx_contatto.setSelectedIndex(0);
								onChangeContatto(null);
						} else {
							UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.kContattiAperti", new String[]{k+""}));
						}
					} else {
						if (PROVENIENZA.equals("ACCES")) {
							//non ho trovato contatti
							cbx_contatto.clear(); 
							tipoPrestazione.setDisabled(false);
							UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noContatti"));
						}
					}
				}//fine size=0
			} else {
				if (PROVENIENZA.equals("ACCES"))
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noContatti"));
			}
//			cbx_contatto.setRows(k);
		}
//
//		//-------- gestione decodifica tipo prestazioni --------
//		public int ExecSelectPrestaz() {
//			String punto = MIONOME + "ExecSelectPrestaz ";
//			String tipo = tipo_prestazione.getValue();
//			logger.debug(punto + " tipo operatore>"+tipo+"<");
//			int count = db.SelectPrestaz(prest);
//			if (count <= 0) {
//				this.JCariTextFieldPrestaz_1.setUnmaskedText("");
//				new it.pisa.caribel.swing2.cariInfoDialog(null, "Codice Prestazione: non esiste decodifica", "Attenzione!").show();
//				JCariTextFieldPrestDec.setText("");
//				this.JCariTextFieldPrestDec.setEditable(true);
//				this.JCariTextFieldPrestDec.setDisabled(false);
//				this.JCariTextFieldPrest_num.setText("");
//				this.JCariTextPaneNote.setText("");
//				this.jCariTextPaneDesDett.setText("");
//				this.cariCurrencyTextFieldImporto.putItValue("");
//			} else {
//				if (tipo.length() == 2)
//					if (tipo.substring(0, 1).equals("0"))
//						tipo = tipo.substring(1, 2);
//				if (!tipo.equals(this.tipo_prestazione.getValue())) {
//					new it.pisa.caribel.swing2.cariInfoDialog(null, "Il tipo di prestazione non e' ammesso", "Attenzione!").show();
//					this.JCariTextFieldPrestaz_1.setText("");
//					JCariTextFieldPrestDec.setText("");
//					this.JCariTextFieldPrestDec.setEditable(true);
//					this.JCariTextFieldPrestDec.setDisabled(false);
//					this.JCariTextFieldPrest_num.setText("");
//					this.JCariTextPaneNote.setText("");
//					this.jCariTextPaneDesDett.setText("");
//					this.cariCurrencyTextFieldImporto.putItValue("");
//				}
//				this.JCariTextFieldPrestDec.setEditable(false);
//				this.JCariTextFieldPrestDec.setDisabled(true);
//				if (!(JCariTextFieldPrestDec.getUnmaskedText().equals("")))
//					JCariTextFieldPrestDec.setCaretPosition(1);
//			}
//			tipo_prestazione.setUnmaskedText(tipo);
//			return count;
//		}
//
		private int controllaDataPrest(String data_inizio, String data_fine) {// se data_inizio è maggiore di data_fine restituisce 1
			// se data_inizio è minore di data_fine restituisce 2
			// se data_inizio è = di data_fine restituisce 0
			// se da errore -1
			if (data_inizio.length() != 10 && data_fine.length() != 10)
				return -1;
			data_inizio = data_inizio.substring(0, 2) + data_inizio.substring(3, 5) + data_inizio.substring(6, 10);
			DataWI dataINIZIO = new DataWI(data_inizio);
			data_fine = data_fine.substring(6, 10) + data_fine.substring(3, 5) + data_fine.substring(0, 2);
			int rit = dataINIZIO.confrontaConDt(data_fine);
			return rit;
		}

		public int ControlloData(String dataold, String datanew) {
			// controlla se dataold < datanew

			int[] datavecchia = new int[3];

			Integer giorno = new Integer(dataold.substring(0, 2));
			datavecchia[0] = giorno.intValue();
			Integer mese = new Integer(dataold.substring(3, 5));
			datavecchia[1] = mese.intValue();
			Integer anno = new Integer(dataold.substring(6, 10));
			datavecchia[2] = anno.intValue();

			//preparazione secondo array

			int[] datanuova = new int[3];

			Integer day = new Integer(datanew.substring(0, 2));
			datanuova[0] = day.intValue();
			Integer mounth = new Integer(datanew.substring(3, 5));
			datanuova[1] = mounth.intValue();
			Integer year = new Integer(datanew.substring(6, 10));
			datanuova[2] = year.intValue();

			//confronto anno

			if (datanuova[2] < datavecchia[2]) {
				UtilForUI.standardExclamation("Inserire data prestazione successiva\nalla data apertura del contatto!");
				return -1;
			} else if (datanuova[2] == datavecchia[2])

				//confronto mes
				if (datavecchia[1] > datanuova[1]) {
					UtilForUI.standardExclamation("data contatto e data prestazione non compatibili! ");
					logger.debug("data contatto e data prestazione non compatibili!");
					return -1;
				} else if (datanuova[1] == datavecchia[1])
					//confronto giorno
					if (datanuova[0] < datavecchia[0]) {
						//new cariInfoDialog(null,"intervallo non valido,invertire le date ","Attenzione!").show();
						UtilForUI.standardExclamation("data contatto e data prestazione non compatibili");
						logger.debug("intervallo non valido, invertire le date");
						return -1;
					}

			return 1;

		}

		private void ExecInsertGrid() throws Exception {
			//inserimento dati dai controlli alla griglia
			int count = 0;
			StringBuffer st = new StringBuffer();
			// inserimento di un nuovo record nella cariStringTableModel1
			String codice = cod_prestazione.getValue();
			if (codice.equals(""))
				codice = " ";
			String descrizione = desc_prestazione.getSelectedValue();
			if (descrizione.equals(""))
				descrizione = " ";
			String numero;
			if (sp_quantita.getValue() == null) {
				numero = "1";
				sp_quantita.setValue(1);
			}else{
				numero = (new Integer(sp_quantita.getValue())).toString();
			}
			String tempo = pre_tempo.getValue()!=null ? pre_tempo.getValue() : " ";
			if (tempo.equals(""))
				tempo = " ";
			String note = pre_note.getValue()!=null ? pre_note.getValue() : " ";
			if (note.equals(""))
				note = " ";
			String importo = pre_importo.getValue()!=null ? (pre_importo.getValue()).toString() : " ";

			String dett = pre_des_dett.getValue()!=null ? pre_des_dett.getValue() : " ";
			if (dett.equals(""))
				dett = " ";

			st.append(codice + "##" + descrizione + "##" + numero + "##" + tempo + "##" + note + "##" + importo + "##" + dett);
			//TODO verificare l'insert
			CaribelListModel modelloGriglia = (CaribelListModel) caribellb2.getModel();
			Hashtable hDati = UtilForBinding.getHashtableFromComponent(myForm);
//			hDati.put("pre_cartella", n_cartella.getValue());
//			hDati.put("pre_contatto", contatto.getValue());
			if (numRiga != -1) {
				modelloGriglia.remove(numRiga);
				modelloGriglia.add(numRiga, hDati);
			} else{
				modelloGriglia.add(hDati);
			}
			
//			if (numRiga == -1) {
//				
//				if (cariStringTableModel1.getRowCount() == 0)
//					cariStringTableModel1.insertRowAt(st.toString(), 0);
//				else
//					cariStringTableModel1.insertRowAt(st.toString(), cariStringTableModel1.getRowCount());
//			} else {
//				cariStringTableModel1.removeRow(numRiga);
//				cariStringTableModel1.insertRowAt(st.toString(), numRiga);
//			}
		}

		private void Salvataggio() throws Exception {
			doWriteComponentsToBean();
//			if(this.currentIsasRecord!=null){
//				this.currentIsasRecord = updateSuEJB(this.currentBean,this.currentIsasRecord);
//			}else{
//				this.currentIsasRecord = insertSuEJB(this.currentBean,this.hParameters);
//			}
			int count = 0;
//			String cognome = this.JCariTextFieldDesOper.getUnmaskedText();
			String cognome = desc_operatore.getValue();
//			String desQual = this.JCariTextFieldDesOperqual.getUnmaskedText();
			String desQual = qualOperatore.getValue();
			String despres = desc_presidio.getValue();
			Hashtable h;
			// se viene dato il messaggio della scheda SO, va in default e sbianca il form.
			if (check_save_SO_PRESENTE == CTS_SO_PRESENTE_SAVE && datiFormPrimaRichiestaSO!=null){
				h= datiFormPrimaRichiestaSO;
			}else {
				h = UtilForBinding.getHashtableFromComponent(myForm);
			}
			h.put("pre_anno", int_anno.getValue());
			h.put("pre_contatore", Integer.toString(int_contatore.getValue()));
//			h.put("pre_cartella", n_cartella.getValue());
//			h.put("pre_contatto", contatto.getValue());
			
			if(((String) h.get("pre_numero")).isEmpty()){
				h.put("pre_numero", "1");
			}
			
			if ((numRiga == -1 && !insertDett) || modi_op || this.stato == INSERT) {
				if (!modi_op || stato == INSERT){
//					count = db.Insert(t);
//					doWriteBeanToComponents();
					this.hParameters.putAll(h);
					this.currentIsasRecord = insertSuEJB(this.currentBean,this.hParameters);
					
				}else if (stato != INSERT) {
					update_prestaz = true;
					//SE SONO IN UPDATE SU INTERV DEVO CONTROLLARE SE HO CAMBIATO QCS ANCHE NELLA GRIGLIA
					if (Hgriglia != null && Hgriglia.isModified(UtilForBinding.getHashtableFromComponent(caribellb2, null, false, true)));
						if (conta.equals("0") || cancellato == true) {
							invokeGenericSuEJB(new IntpreEJB(), h, "insert");
//							count = db.InsertPrestaz(prestaz);
//							u.setDefault(this.JPanelPrestazioni);
						} else {
							invokeGenericSuEJB(new IntpreEJB(), h, "salva");
//							count = db.Salva(prestaz);
						}
//					count = db.Update(t);
					this.currentIsasRecord = invokeSuEJB(myEJB, this.currentIsasRecord.getHashtable(), "update_interv"); //updateSuEJB(this.currentBean,this.currentIsasRecord);
				}
				this.insertDett = true;
//				if (count == 1)
					this.setStato(UPDATE_DELETE);
			} else if ((conta.equals("0") || cancellato == true)) { //&&currentIsasRecord==null) {
				invokeGenericSuEJB(new IntpreEJB(), h, "insert");
//				count = db.InsertPrestaz(prestaz);
//				u.setDefault(this.JPanelPrestazioni);
			} else {
				invokeGenericSuEJB(new IntpreEJB(), h, "salva");
//				count = db.Salva(prestaz);
				this.insertDett = true;
//				u.setDefault(this.JPanelPrestazioni);
//				u.Enable(jPanelInterv, false);
//				//        this.JCariTextFieldDesOper.setText(getProfile().getStringFromProfile(ManagerProfile.COGNOME_OPERATORE));
//				this.JCariTextFieldDesOper.setText(cognome);
//				this.JCariTextFieldDesOperqual.setText(desQual);
//				this.JCariTextFieldPresidio.setText(desPres);
			}
//			this.JCariTextPaneNote.setText("");
//			this.jCariTextPaneDesDett.setText("");
//			this.cariCurrencyTextFieldImporto.putItValue("");
//			u.Enable(jPanelInterv, false);
//			JCariTextFieldCodpresidio.setEditable(false);
//			// this.JCariTextFieldDesOper.setText(getProfile().getStringFromProfile(ManagerProfile.COGNOME_OPERATORE));
//			this.JCariTextFieldDesOper.setText(cognome);
//			this.JCariTextFieldDesOperqual.setText(desQual);
//			this.JCariTextFieldPresidio.setText(desPres);
//			if (!(this.JCariTextFieldDesOper.getUnmaskedText().equals("")))
//				this.JCariTextFieldDesOper.setCaretPosition(1);
//			if (!(this.JCariTextFieldDesOperqual.getUnmaskedText().equals("")))
//				this.JCariTextFieldDesOperqual.setCaretPosition(1);
//			if (!(this.JCariTextFieldPresidio.getUnmaskedText().equals("")))
//				this.JCariTextFieldPresidio.setCaretPosition(1);

			hParameters.put("int_contatore", currentIsasRecord.get("int_contatore"));
			hParameters.put("int_anno", int_anno.getValue());
			doQueryKeySuEJB();
//			int sel = caribellb2.getSelectedIndex();
			doWriteBeanToComponents();
			ExecSelectCartella();
//			caribellb2.invalidate();
//			caribellb2.setSelectedIndex(sel);
		}

		private Hashtable CaricaPerProfile(Vector Vprestaz) {
//			tablePrestazioni.setModel(new CaribelListModel<ISASRecord>(Vprestaz));
			//VFR TODO
			// Hashtable hTipo=new Hashtable();
			//Ho un unico elemento che è un hashtable
			Hashtable h = (Hashtable) Vprestaz.elementAt(0);
			Enumeration enTp = h.keys();
			modelloPrestazioni = new CaribelListModel();
			while (enTp.hasMoreElements()) {
				String tipo = "" + enTp.nextElement();
				if (hTipo.get(tipo) != null) {
					modelloPrestazioni = (CaribelListModel) hTipo.get(tipo);
					continue;
				} else {
					modelloPrestazioni = new CaribelListModel();
				}
				//prendo la hashtable con i dati delle prestazioni
				Hashtable hCod = (Hashtable) h.get(tipo);
				Enumeration enCod = hCod.keys();
				int i = 0;
				// logger.debug(e);
				while (enCod.hasMoreElements()) {
					String cod_prestaz = "" + enCod.nextElement();
					Hashtable hDati = (Hashtable) hCod.get(cod_prestaz);
					hDati.put("pre_cod_prest", cod_prestaz);
					hDati.put("pre_numero", "1");
					hDati.put("pre_des_prest", hDati.get("prest_des"));
					modelloPrestazioni.add(hDati);
//					modelTable.insertRowAt(riga, ++i);
				}//fine while interno
				/*cancello l'eventuale riga vuota.Attenzione se faccio una deleteall sulla tabella ho problemi
				perchè mi cancella tutte le righe del modello --> quindi anche quelle  caricate in memoria
				*/

				Hashtable hNuovo = new Hashtable();
				hNuovo.put("pre_cod_prest", "");
//				int num_riga = modelTable.columnsContains(hNuovo);
//				if (num_riga != -1)
//					modelTable.removeRow(num_riga);
//				//--------------------------
				hTipo.put(tipo, modelloPrestazioni);
			}//fine while esterno
			// ordina x Descrizione ASC
			CaribelComparator cmpr = new CaribelComparator("prest_des");
			cmpr.setAscendingOrder(true);
			modelloPrestazioni.sort(cmpr, true);
			tablePrestazioni.setModel(modelloPrestazioni);
//			((CaribelListModel)tablePrestazioni.getModel()).sort(cmpr, true);
//			tablePrestazioni.invalidate();
//			((CaribelListModel)tablePrestazioni.getModel()).setMultiple(true);
			modelloPrestazioni.setMultiple(true);
			return hTipo;
		}

		private void CaricaGrigliaPrestaz() {
//			CaribelListModel<ISASRecord> modello_ = new CaribelListModel<ISASRecord>();

			String tipoPrest = tipo_prestazione.getValue();
			logger.debug("TIPO PRESTAZIONE Dentro CaricaGrigliaPrest-->'" + tipoPrest + "'");
			if (tipoPrest.equals("")) {//assegno il modello vuoto
				logger.debug("MODELLO Dentro 1");
				modelloPrestazioni = new CaribelListModel();
				tablePrestazioni.setModel(modelloPrestazioni);
				return;
			}
			if (hTipo != null && hTipo.get(tipoPrest) != null) {
				modelloPrestazioni = (CaribelListModel) hTipo.get(tipoPrest);
				logger.debug("MODELLO Dentro CaricaGrigliaPrest-->'" + modelloPrestazioni + "'");
				CaribelComparator cmpr = new CaribelComparator("prest_des");
				cmpr.setAscendingOrder(true);
				modelloPrestazioni.sort(cmpr, true);
				tablePrestazioni.setModel(modelloPrestazioni);
//				((CaribelListModel)tablePrestazioni.getModel()).setMultiple(true);
				modelloPrestazioni.setMultiple(true);
			} else {
				logger.debug("MODELLO Dentro 2");
				modelloPrestazioni = new CaribelListModel();
				tablePrestazioni.setModel(modelloPrestazioni);
			}
		}

		private void CaricaPianoInterventi() {
			logger.debug("-->>In CaricaPianoInterventi");

			if (!PROVENIENZA.equals("ACCES"))
				return;
			if (this.cbx_contatto.getSelectedIndex() != -1 && !cod_operatore.getValue().equals("")
					&& !("" + this.cbx_contatto.getValue()).equals("") && !this.n_cartella.getValue().equals("")
					&& !this.int_data.getValueForIsas().equals("")) {
				//CaricaGrigliaPrestaz() ;
				deseleziona();
				CaribelListModel modello = (CaribelListModel) tablePrestazioni.getModel();
				Hashtable hPiano = (Hashtable) hDati.get("pianoInterv");
				//vado a a prendere il contatto
				String cod = this.cbx_contatto.getSelectedValue();
				StringTokenizer tok = new StringTokenizer(cod, "#");
				tok.nextToken();
				cod = tok.nextToken();
				contatto.setValue(cod);
				if (hPiano.get(cod) != null) {//per quel contatto esiste un piano interventi
					Vector vettPrest = (Vector) hPiano.get(cod);
					for (int i = 0; i < vettPrest.size(); i++) {
						try {
							ISASRecord dbrec = (ISASRecord) vettPrest.elementAt(i);
							//vado a controllare che quel piano sia valido per la data prestazione inserita
							String data_apertura = ((java.sql.Date) dbrec.get("pi_data_inizio")).toString();
							data_apertura = data_apertura.substring(8, 10) + "/" + data_apertura.substring(5, 7) + "/"
									+ data_apertura.substring(0, 4);
							String data_chiusura = null;
							if (dbrec.get("pi_data_fine") != null)
								data_chiusura = ((java.sql.Date) dbrec.get("pi_data_fine")).toString();
							if (data_chiusura != null)
								data_chiusura = data_chiusura.substring(8, 10) + "/" + data_chiusura.substring(5, 7) + "/"
										+ data_chiusura.substring(0, 4);

							String data_prest = (String) int_data.getText();
							//confronto la data apertura con la data della prestazione
							int rit = controllaDataPrest(data_apertura, data_prest);
							if (rit == 2 || rit == 0) {
								if (data_chiusura == null
										|| (controllaDataPrest(data_prest, data_chiusura) == 2 || controllaDataPrest(data_prest, data_chiusura) == 0)) {
									if (dbrec.get("pi_prest_cod") != null) {
										//ricerco il codice nella griglia
										String codice = (String) dbrec.get("pi_prest_cod");
										Hashtable hTrova = new Hashtable();
										hTrova.put("pre_cod_prest", codice);
										int riga = modello.columnsContains(hTrova);
										if (riga != -1) {
											logger.debug("Selezione della prestazione: "+codice + " come da piano: "+cod);
											int quant = 1;
											if (dbrec.get("pi_prest_qta") != null)
												quant = ((Integer) dbrec.get("pi_prest_qta")).intValue();
											//                                             logger.debug("QUANTI-->"+ quant)         ;
											((Hashtable)modello.get(riga)).put("pre_numero", quant+"");
											Object o = modello.remove(riga);
											modello.add(0, o);
											Set tmp = new HashSet(tablePrestazioni.getSelectedItems());
											tmp.add(tablePrestazioni.getItemAtIndex(0));
											tablePrestazioni.setSelectedItems(tmp);
//											modello.getSelection().add(modello.get(riga));
//											int col_quan = modello.getColumnLocationByName("pre_numero");
//											modello.setValueAt("" + quant, riga, col_quan);
											//                                             logger.debug("QUANTI-->"+ modello.getValueAt(riga,col_quan) );
											//devo mettere la quantità

										}
									}
								}//fine controllo chiusura
							}//fine controllo apertura
						} catch (Exception e) {
							logger.error("ERRORE PIANO INTERVENTI-->" + e);
							doShowException(e);
						}
					}//fine for
				}
				/*devo ordinare prima per una colonnae poi per l'altra perchè
				altrimenti mi ordina lecheck delle volte in cima e delle volte in fondo*/
//				JCariTableGrigliaPrestaz.sortRows(2, true);
//				JCariTableGrigliaPrestaz.sortRows(0, false);

				//JCariTableGrigliaPrestaz.so
				
			}//fine test su campi != stringa vuota
		}

		private void initForm() throws Exception {
			//String parDaConf_ = getProfile().getStringFromProfile(ManagerProfile.ACCE_DURATA_OBBL); //getProfile().getStringFromProfile("acce_durata_obbl");
			this.durataObbl = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ACCE_DURATA_OBBL); //((parDaConf != null) && (parDaConf.trim().equals("SI")));
			// 31/05/11 --

			String tipo_oper = getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE);

			int_tempo.setRequired(durataObbl);
			//gb 28/05/07 *******
			if (!tipo_oper.equals("05"))
				//gb 28/05/07: naturalmente in gl_strTipoOperatore c'è quello ritornato
				//              dalla form di scelta tipo operatore e settata nel costruttore.
				tipo_oper = gl_strTipoOperatore;

			//-----------caricamento della griglia delle prestazioni-----
			Vector vett = (Vector) getProfile().getParamFromProfile("tipiPresInterv");
			if (vett == null) {
				Hashtable<String, Object> h = new Hashtable<String, Object>();
				h.put("tipo_oper", "05");
				vett = (Vector) invokeGenericSuEJB(myEJB, h, "CaricaPrest"); //db.CaricaPrest(h);
				getProfile().getProfile().put("tipiPresInterv", vett);
			}
			
			hTipo = CaricaPerProfile(vett);

//			t.buildClientTable(this.JPanelMain, "interv");
//			t.addComponent(this.JCariTextPaneNote, "pre_note");
//			t.addComponent(this.JCariTextFieldRegione, "codreg");
//			t.addComponent(this.JCariTextFieldUsl, "codazsan");
//			t.addComponent(JCariTextFieldDesOperqual, "desc_qualif");
//			t.addComponent(JCariTextFieldTipo_oper, "int_tipo_oper");
//			//tabella interv per la SelectAll
//			interv.buildClientTable(this.JPanelMain, "interv");
//			interv.addComponent(this.JCariTextFieldPrestaz_1, "pres_cod_pres", 1);
//			interv.addComponent(this.JCariTextFieldPrest_num, "pre_numero", 1);
//			interv.addComponent(this.JCariTextPaneNote, "pre_note", 1);
//			interv.addComponent(this.cariCurrencyTextFieldImporto, "pre_importo", 1);
//			interv.addComponent(this.jCariTable1, "griglia");
//			interv.addComponent(this.tipo_prestazione, "int_tipo_prest");
//			interv.addComponent(this.JCariTextFieldOperatore, "int_cod_oper");
//			interv.addComponent(JCariTextFieldDesOperqual, "desc_qualif");
//			interv.addComponent(JCariTextFieldQual_oper, "int_qual_oper");
//			interv.addComponent(JCariTextFieldTipo_oper, "int_tipo_oper");
//			interv.addComponent(JCariTextFieldTipoServizio, "tipo_servizio");
//			//tabella operatori ------
//			operatori.addComponent(JCariTextFieldOperatore, "codice");
//			operatori.addComponent(JCariTextFieldDesOper, "cognome");
//			operatori.addComponent(JCariTextFieldDesOperqual, "desc_qualif");
//			operatori.addComponent(JCariTextFieldQual_oper, "cod_qualif");
//			operatori.addComponent(tipo_prestazione, "int_tipo_prest");
//			operatori.addComponent(JCariTextFieldCodpresidio, "cod_presidio");
//			operatori.addComponent(JCariTextFieldPresidio, "des_presidio");
//			operatori.addComponent(JCariTextFieldTipo_oper, "tipo");
//			//gb 28/05/07: l'istruz. sotto serve per passare il vincolo alla queryKey e query dell'EJB
//			//              OperatoriEJB che il tipo operatore non sia comunque '01'
//			operatori.addComponent(jCariTextFieldTipoOperConstraint, "tipo_oper_constraint"); //gb 28/05/07
//
//			//tabella cartella ------
//			cartella.addComponent(n_cartella, "n_cartella");
//			cartella.addComponent(JCariTextFieldTipo_oper, "tipo_oper");
//
//			//tabella continterv ------
//			continterv.addComponent(int_contatore, "int_contatore");
//			continterv.addComponent(this.n_cartella, "int_cartella");
//			continterv.addComponent(this.n_cartellaDec, "cognome");
//			//tabella prestazioni ------
//			prestaz.addComponent(JCariTextFieldPrestaz_1, "pre_cod_prest");
//			prestaz.addComponent(JCariTextFieldPrestDec, "pre_des_prest");
//			prestaz.addComponent(this.JCariTextPaneNote, "pre_note");
//			prestaz.addComponent(this.JCariTextFieldPrest_num, "pre_numero");
//			prestaz.addComponent(this.cariCurrencyTextFieldImporto, "pre_importo");
//			prestaz.addComponent(this.jCariTextFieldTempoPrest, "pre_tempo");
//			prestaz.addComponent(this.JCariTextFieldAnno, "pre_anno");
//			prestaz.addComponent(int_contatore, "pre_contatore");
//			//tabella prest -----
//			prest.addComponent(JCariTextFieldPrestaz_1, "prest_cod");
//			prest.addComponent(JCariTextFieldPrestDec, "prest_des");
//			prest.addComponent(this.jCariTextFieldTempoPrest, "prest_tempo");
//			prest.addComponent(this.cariCurrencyTextFieldImporto, "pre_importo");
//			prest.addComponent(this.tipo_prestazione, "tipo");
//			prest.addComponent(this.jCariTextPaneDesDett, "prest_des_dett");
//			//tabella presidi ------
//			presidi.buildClientTable(this.JPanelMain, "presidi");
//			presidi.addComponent(this.JCariTextFieldCodpresidio, "codpres");
//			presidi.addComponent(this.JCariTextFieldPresidio, "despres");
//
//			//caritablecom per il controllo delle modifiche sull'intervento
//			//ad opera dell'operatore che l'ha inserito
//			tabop.addComponent(n_cartella, "cartella");
//			tabop.addComponent(this.int_data, "data");
//			tabop.addComponent(this.JCariTextFieldOraInizio, "inizio");
//			tabop.addComponent(this.JCariTextFieldOraFine, "fine");
//			tabop.addComponent(this.JCariTextFieldTempo, "tempo");
//			tabop.addComponent(this.JCariTextFieldTempoGO, "tempogo");
//			tabop.addComponent(this.JCariTextFieldCodpresidio, "presidio");
//			tabop.addComponent(this.cbx_contatto, "combo");
//			tabop.addComponent(this.jCariTextPaneNote1, "note");
//			tabop.addComponent(this.tipoPrestazione, "tipo");
//			tabop.addComponent(this.cbx_unitaFunzionale, "combounita");
//			tabop.addComponent(this.JCariTextFieldOperatore, "operatore");
//
//			forGriglia.addComponent(this.JCariTextFieldPrestaz_1, "prest");
//			forGriglia.addComponent(this.JCariTextFieldPrest_num, "num");
//			forGriglia.addComponent(this.jCariTextPaneDesDett, "dett");
//			forGriglia.addComponent(this.JCariTextPaneNote, "note");
//			forGriglia.addComponent(this.cariCurrencyTextFieldImporto, "importo");

			JCariTextFieldRegione.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_REGIONE));
			JCariTextFieldUsl.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_USL));
			JCariTextFieldTipo_oper.setValue(tipo_oper);
			
			//Registrazione Listeners FIXME
//			this.n_cartella.addJCariTextListeners(this);
//			this.JCariTextFieldPrestaz_1.addJCariTextListeners(this);
//			this.JCariTextFieldCodpresidio.addJCariTextListeners(this);
//			this.JCariTextFieldOraInizio.addJCariTextListeners(this);
//			this.JCariTextFieldOraFine.addJCariTextListeners(this);
//			this.JCariTextFieldTempo.addJCariTextListeners(this);
//			this.jCariTable1.addJCariTableListeners(this);
//			this.JCariTextFieldArea.addJCariTextListeners(this);
//			this.JCariTextFieldOperatore.addJCariTextListeners(this);
			

			Hashtable<String, String> params = new Hashtable<String, String>();
			params.put("descrizione", " ");
            CaribelComboRepository.comboPreLoad("combo_inttabuf", new UnitaFunzEJB(), "query", params, cbx_unitaFunzionale, labelUnitaF, "codice", "descrizione", false);


			if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.DEFAULT_ACCESSI))
				valoriDef = true;
			if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABIL_ACC_UNIFUN))
				valoriDef_unifun = true;
			if (valoriDef_unifun) {
				Hashtable<String, String> uxoper = new Hashtable<String, String>();
				uxoper.put("codice", cod_operatore.getValue());
				ISASRecord res = invokeSuEJB(new UnitaFunzEJB(), uxoper, "query_unita_oper");
				String unita = (String) res.get("unita_funz");
				logger.debug("Unità dell'operatore=>" + unita);
				cbx_unitaFunzionale.setSelectedValue(unita);
			}
			
			// 02/07/07
			cbx_unitaFunzionale.setRequired(valoriDef_unifun);

			/*il campo operatore deve essere abilitato solo e soltanto se l'operatore che è entrato è
			di tipo amministrativo--> tipo=05
			*/
			// 04/11/10
			//FIXME verificare che con l'operatore amministrativo il controllo sia corretto.
//			CaribelSearchCtrl operatoreCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
			if ((tipo_oper.equals("05") && (currentIsasRecord == null || iu.canIWrite(currentIsasRecord))) || 
					iu.canIUse(this.nomeFinestraChgOp, "MODI")){
		        operatoreCtrl.setReadonly(false);
			} else {
		        operatoreCtrl.setReadonly(true);
			}
			this.setStato(this.WAIT);
			if(currentIsasRecord==null){
				this.setStato(this.INSERT);
			}else{
				this.setStato(this.UPDATE_DELETE);
			}

		}

		private void popoladatiFlussoSpr() {
			String punto = MIONOME + "popoladatiFlussoSpr ";
			try{
				String abilGestFlussiSpr = getProfile().getStringFromProfile(ManagerProfile.ABL_GST_SPR);
				boolean abltFlussiSPR = ((abilGestFlussiSpr != null) && (abilGestFlussiSpr.trim().equals("SI")));

				if (abltFlussiSPR){
					logger.debug(punto + " abilitazione flussi spr ");
					String user = iu.getDescUser();
					logger.debug(punto + " user>"+user+"< presidio>"+presidioFlussiSPR+"< prestazione>"+prestazioneFlussiSPR+"<");
					cod_presidio.setValue(presidioFlussiSPR);
					//TODO verificare se devo lanciare l'evento per la ricerca del presidio
					//Events.sendEvent(Events.ON_CHANGE, cod_presidio, null);

					
					OPERATORIOK = getProfile().getStringFromProfile(ManagerProfile.OP_ACC_GEN);
					logger.debug("operatori generali:" + getProfile().getStringFromProfile(ManagerProfile.OP_ACC_GEN));

					cod_operatore.setValue(user);
					ExecSelectOperatori(true);
					if (presidioFlussiSPR!= null && !presidioFlussiSPR.isEmpty()){
						cod_presidio.setValue(presidioFlussiSPR);
						Events.sendEvent(Events.ON_CHANGE, cod_presidio, null);
//						ExecSelectPresidi();
					}
				}else {
					logger.debug(punto + "\n non si ha l'abilitazione flussi spr \n");
				}

			}catch (Exception e) {
				logger.error(punto + " Errore nel recuperare per popolare: operatore, presidio, operatore ");
			}
		}

//		private void setDefault() {
//			String tpOper = this.JCariTextFieldTipo_oper.getValue();// 10/01/08
//			this.sbiancaTutto();
//			this.tipo_prestazione.setValue(tipo);
//			this.jCariTable1.deleteAll();
//			this.jCariTextFieldCombo.setVisible(false);
//			this.jCariTextFieldCombo.setValue("");
//			if (PROVENIENZA.equals("ACCES"))
//				this.cbx_contatto.setVisible(true);
//			cariStringComboBoxModelContatti.setItems(new String[] { " " });
//			setDefaultDettagli();
//			modi_op = false;
//			this.JCariTextFieldTipo_oper.setUnmaskedText(tpOper); // 10/01/08
//		}
//
//		private void sbiancaTutto() {
//			u.setDefault(this.JPanelPrestazioni);
//			this.JCariTextFieldDesOper.setText("");
//			this.JCariTextFieldDesOperqual.setText("");
//			this.n_cartella.setText("");
//			this.n_cartellaDec.setText("");
//			this.JCariTextFieldCodpresidio.setText("");
//			this.JCariTextFieldPresidio.setText("");
//			this.JCariTextFieldOraFine.setText("");
//			this.JCariTextFieldOraInizio.setText("");
//			this.JCariTextFieldTempo.setText("");
//			this.JCariTextFieldTempoGO.setText("");
//			this.jCariTextPaneNote1.setText("");
//			this.int_data.setText("__/__/____");
//			sbiancaCombo();
//		}
//
//		private void setDefaultDettagli() {
//			String data_prest = this.int_data.getText();
//			u.setDefault(this.JPanelArea);
//			this.int_data.setText(data_prest);
//		}
//
		private void setStato(int state) throws Exception {
			if (state == this.WAIT) {
//				u.Enable(JPanelKey, true);
//				u.Enable(JPanelArea, false);
//				this.printAction.setDisabled(true);
//				this.newAction.setEnabled(iu.canIUse(myKeyPermission, "INSE"));
//				this.openAction.setEnabled(iu.canIUse(myKeyPermission, "CONS"));
//				this.saveAction.setDisabled(true);
//				this.deleteAction.setDisabled(true);
				btn_confermaSelezione.setDisabled(true);
//				int_anno.setValue(procdate.getAnno()); //VFR rimossoAnno
				JCariTextFieldTipoServizio.setValue("");
				//ilaria
				if (valoriDef) {
					tipoPrestazione.setSelectedValue("D");
				} else {
					tipoPrestazione.setSelectedValue("N");
				}
				if (valoriDef_unifun) {
					cbx_unitaFunzionale.setVisible(true);
					labelUnitaF.setVisible(true);
				} else {
					cbx_unitaFunzionale.setVisible(false);
					labelUnitaF.setVisible(false);
				}
				unitaFunzionale.setVisible(valoriDef_unifun);
				
				int_contatore.setValue(0);
				deseleziona();
				modelloPrestazioni = new CaribelListModel();
				tablePrestazioni.setModel(modelloPrestazioni);
				int_contatore.focus();
			}
			if (state == this.INSERT) {
				this.setReadOnly(!(iu.canIUse(myKeyPermission, "INSE")));
//				u.Enable(JPanelKey, false);
//				u.Enable(this.jPanelInterv, true);
//				u.Enable(JPanelArea, true);
				btn_apriCartella.setDisabled(true);
//				this.printAction.setDisabled(true);
//				u.Enable(this.JPanelPrestazioni, true);
//				this.openAction.setDisabled(true);
//				this.newAction.setDisabled(true);
//				this.saveAction.setEnabled(iu.canIUse(myKeyPermission, "INSE"));
				btn_confermaSelezione.setDisabled(!iu.canIUse(myKeyPermission, "INSE"));
				
				presidioCtrl.setReadonly(false);
//				this.JCariTextFieldPrestaz_1.setEditable(true);
//				this.JButtonBorderedPrestaz.setEnabled(true && iu.canIUse(myKeyPermission, "INSE"));
				this.insertDett = false;
//				CaribelSearchCtrl operatoreCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
				operatoreCtrl.setReadonly(false);
				if ((OPERATORIOK.indexOf(getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE)) != -1)
						&& (getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE).trim().equals(gl_strTipoOperatore))) {
					cod_operatore.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
					desc_operatore.setValue(getProfile().getStringFromProfile(ManagerProfile.COGNOME_OPERATORE));
					qualOperatore.setValue(getProfile().getStringFromProfile(ManagerProfile.QUAL_OPERATORE));
					ExecSelectOperatori(true);
				} else
					this.int_data.focus();
//				this.JCariActionButton4.setDisabled(true);
//				this.JCariActionButton3.setDisabled(true);
//				this.JCariActionButton5.setDisabled(true);
				this.JCariTextFieldTipoServizio.setValue("");

				if ((getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE).equals("05")) || (iu.canIUse(this.nomeFinestraChgOp, "MODI"))) // 04/11/10
				{
					operatoreCtrl.setReadonly(false);
				} else {
					operatoreCtrl.setReadonly(true);
				}
			}

			if (state == this.UPDATE_DELETE) {
//				this.printAction.setDisabled(false);
//
//				//Giulia 29/01/2003
				this.setReadOnly(!(iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord)));
//				UtilForBinding.setComponentReadOnly(myForm, !(iu.canIUse(myKeyPermission, "CONS") && iu.canIWrite(currentIsasRecord)));
//				try {
//					this.JCariActionButton5.setEnabled(iu.canIUse(myKeyPermission, "MODI")
//							&& profile.getISASUser().canIDelete((ISASRecord) interv.getDBRecordForUpdate()));
//				} catch (Exception e) {
//					logger.error("ERRORE " + e);
//					this.JCariActionButton5.setDisabled(true);
//				}
//				this.JCariActionButton4.setEnabled(iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord));
//				this.JCariActionButton3.setEnabled(iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord));
//				this.JCariActionButton1.setEnabled(iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord));
//				this.JCariActionButton2.setEnabled(iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord));
//				u.Enable(JPanelKey, false);
//				this.newAction.setDisabled(true);
//				this.openAction.setDisabled(true);
//				this.saveAction.setEnabled(true && iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord));
//				JCariActionButtonConferma.setEnabled(true && iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord));
//				try {
//					this.deleteAction.setEnabled(true && iu.canIUse(myKeyPermission, "CANC")
//							&& profile.getISASUser().canIDelete((ISASRecord) interv.getDBRecordForUpdate()));
//				} catch (Exception e) {
//					logger.error("ERRORE " + e);
//					this.JCariActionButton5.setDisabled(true);
//				}
//				if (JCariActionButton1.isEnabled()) {
//					this.JCariTextFieldCodpresidio.setEditable(true);
//					this.JCariTextFieldCodpresidio.setDisabled(false);
//					this.JCariTextFieldPresidio.setEditable(true);
//					this.JCariTextFieldPresidio.setDisabled(false);
//					this.int_data.requestFocus();
//				}
//				this.JCariTextFieldPrestDec.setEditable(true);
//				this.JCariTextFieldPrestDec.setDisabled(false);
//				if (iu.canIUse(myKeyPermission, "MODI") && iu.canIWrite(currentIsasRecord))
//					u.Enable(this.tipoPrestazione, true);
//				else
//					u.Enable(this.tipoPrestazione, false);
//
				if ((getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE).equals("05") && iu.canIWrite(currentIsasRecord))
						|| (iu.canIUse(this.nomeFinestraChgOp, "MODI"))) // 04/11/10
				{
					operatoreCtrl.setReadonly(false);
//					this.JCariTextFieldOperatore.setDisabled(false);
//					this.JCariTextFieldOperatore.setEditable(true);
//					this.JCariTextFieldDesOper.setDisabled(false);
//					this.JCariTextFieldDesOper.setEditable(true);
//					this.JButtonBorderedOperatore.setDisabled(false);
//					this.JCariTextFieldOperatore.requestFocus();
				} else {
					operatoreCtrl.setReadonly(true);
//					this.JCariTextFieldOperatore.setDisabled(true);
//					this.JCariTextFieldOperatore.setEditable(false);
//					this.JCariTextFieldDesOper.setDisabled(true);
//					this.JCariTextFieldDesOper.setEditable(false);
//					this.JButtonBorderedOperatore.setDisabled(true);
				}
			}
			stato = state;
		}


//		// x apertura da JCariContainerXXXCartella
//		public JFrameInterv(Integer iProv, Integer tipoOp, JDesktopPane dp, String numCart, String numContatto) {
//			this(iProv, tipoOp);
//			String punto =MIONOME + "JFrameInterv ";
//			logger.debug(punto + " \n\n Integer iProv, Integer tipoOp, JDesktopPane dp, String numCart, String numContatto)");
//			this.n_cartella.setUnmaskedText(numCart);
//			this.cbx_contatto.getSelectedValue().setUnmaskedText(numContatto);
//
//			int_contatore.setUnmaskedText("0");
//
//			setStato(this.INSERT);
//
//			int_data.setText(procdate.getitaDate());
//			btn_apriCartella.setVisible(false);
//
//			ExecSelectCartella();
//			caricaDatiContatti();
//			logger.debug(punto + " \n\n fINE ESECUZIONE ");
//			setModal(dp);
//		}
//
//
//
//
//		public JFrameInterv(Integer iProv, Integer tipoOp, JDesktopPane dp, String numCart, String numContatto,
//				String presidioSpr, String prestazioneSpr) {
//			this(iProv, tipoOp);
//			String punto =MIONOME + "JFrameInterv ";
//			logger.debug(punto + " \n\n Integer iProv, Integer tipoOp, JDesktopPane dp, String numCart, String numContatto)");
//			this.n_cartella.setUnmaskedText(numCart);
//			this.cbx_contatto.getSelectedValue().setUnmaskedText(numContatto);
//
//			int_contatore.setUnmaskedText("0");
//
//			setStato(this.INSERT);
//
//			int_data.setText(procdate.getitaDate());
//			btn_apriCartella.setVisible(false);
//
//			ExecSelectCartella();
//			caricaDatiContatti();
//			logger.debug(punto + " \n\n fINE ESECUZIONE ");
//			this.presidioFlussiSPR = presidioSpr;
//			this.prestazioneFlussiSPR = prestazioneSpr;
//			logger.debug(punto + " dati che mi presidioSpr>>"+presidioSpr+"< prestazioneSpr>"+prestazioneSpr+"<\n ");
//			popoladatiFlussoSpr();
//			setModal(dp);
//		}
//
//
//
//		//gb 28/05/07	public JFrameInterv(int Prov){
//		// 28/09/07        public JFrameInterv(int Prov, int tipoOperatore){ //gb 28/05/07
//		public JFrameInterv(Integer iProv, Integer tipoOp) {
//			enableEvents(AWTEvent.WINDOW_EVENT_MASK);
//			try {
//				if (profile.getParameter("vco_tempogo") != null && profile.getParameter("vco_tempogo").equals("SI"))
//					this.vco_tempogo = true;
//				int Prov = iProv.intValue();// 28/09/07
//				int tipoOperatore = tipoOp.intValue();// 28/09/07
//
//				if (Prov == 1)
//					PROVENIENZA = "ACCES";
//
//				else
//					PROVENIENZA = "OCCAS";
//
//				//gb 28/05/07 ***
//				String strTipoOperatore = "" + tipoOperatore;
//				if (strTipoOperatore.length() < 2)
//					strTipoOperatore = "0" + strTipoOperatore;
//				gl_strTipoOperatore = strTipoOperatore;
//				//gb 28/05/07: fine ***
//
//				jbInit();
//				this.jCariTable1.setColumnHidden("Tempo");
//				this.jCariTable1.setColumnHidden("prest_des_dett");
//				this.cariStringTableModel1.removeRow(0);
//				String title = "";
//				if (Prov == 1) {
//					// 14/05/08 m.: x modifica SINS 14/03/08
//					PROVENIENZA = "ACCES";
//					//14/03/2008 OPERATORIOK =OperatoriAmmessiInterventi;
//					OPERATORIOK = getProfile().getStringFromProfile(ManagerProfile.OP_ACC_GEN);
//					logger.debug("operatori generali:" + getProfile().getStringFromProfile(ManagerProfile.OP_ACC_GEN));
//
//					title = "Accessi";
//					cbx_contatto.setVisible(true);
//				} else {
//					// 14/05/08 m.: x modifica SINS 14/03/08
//					PROVENIENZA = "OCCAS";
//					//14/03/2008 OPERATORIOK =OperatoriAmmessiOccasionali ;
//					OPERATORIOK = getProfile().getStringFromProfile("op_acc_occ");
//					logger.debug("operatori occasionali:" + getProfile().getStringFromProfile("op_acc_occ"));
//
//					title = "Accessi occasionali - Specialisti";
//					JCariTextFieldTempo.setValid(false);
//					cbx_contatto.setVisible(false);
//					cbx_contatto.setValid(false); //gb 29/10/07
//					JLabelContatto.setVisible(false);
//				}
//				setTitle(title);
//
//				// 26/04/11: solo per accessi STANDARD di tutti: INF, FIS, MED ed ONC
//				this.btn_apriCartella.setVisible(Prov == 1);
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//
//		public JFrameInterv() {
//			enableEvents(AWTEvent.WINDOW_EVENT_MASK);
//			try {
//				if (profile.getParameter("vco_tempogo") != null && profile.getParameter("vco_tempogo").equals("SI"))
//					this.vco_tempogo = true;
//				jbInit();
//				this.jCariTable1.setColumnHidden("Tempo");
//				this.jCariTable1.setColumnHidden("prest_des_dett");
//				this.cariStringTableModel1.removeRow(0);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		private void jbInit() throws Exception {
//			// 28/01/08: x apertura modale
//			JCariActionButton3.setIcon(undoIcon);
//			JCariActionButton4.setIcon(openIcon);
//			JCariActionButton5.setIcon(deleteIcon);
//
//			JCariTextFieldOperatore.setDisabled(true);
//			JCariTextFieldOperatore.setEditable(false);
//			
//			jCariTextFieldTempoPrest.setDBName("pre_tempo");
//			jCariTextFieldTempoPrest.setTableName("interv");
//			jCariTextFieldTempoPrest.setVisible(false);
//
//			jCariTextFieldDiff.setDBName("differenza");
//			jCariTextFieldDiff.setTableName("prestaz");
//			jCariTextFieldDiff.setVisible(false);
//
//			JCariActionButtonConferma.setBackground(new Color(255, 198, 140));
//			JCariActionButtonConferma.setBorder(BorderFactory.createRaisedBevelBorder());
//			JCariActionButtonConferma.addActionListener(new java.awt.event.ActionListener() {
//				public void actionPerformed(ActionEvent e) {
//					JCariActionButtonConferma_actionPerformed(e);
//				}
//			});
//			JCariActionButtonConferma.setText("Conferma selezione");
//			
//			InitForm();
//		}
//
//		javax.swing.JMenuBar JMenuBar1 = new javax.swing.JMenuBar();
//		javax.swing.JMenu JMenu1 = new javax.swing.JMenu();
//		it.pisa.caribel.swing2.JCariActionMenuItem JActionMenuItemNew = new it.pisa.caribel.swing2.JCariActionMenuItem();
//		it.pisa.caribel.swing2.JCariActionMenuItem JActionMenuItemApri = new it.pisa.caribel.swing2.JCariActionMenuItem();
//		it.pisa.caribel.swing2.JCariActionMenuItem JActionMenuItemRimuovi = new it.pisa.caribel.swing2.JCariActionMenuItem();
//		it.pisa.caribel.swing2.JCariActionMenuItem JActionMenuItemAnnulla = new it.pisa.caribel.swing2.JCariActionMenuItem();
//		it.pisa.caribel.swing2.JCariActionMenuItem JActionMenuItemStampa = new it.pisa.caribel.swing2.JCariActionMenuItem();
//		it.pisa.caribel.swing2.JCariActionMenuItem JActionMenuItemExit = new it.pisa.caribel.swing2.JCariActionMenuItem();
//		it.pisa.caribel.swing2.border.cariEtchedBorder etchedBorder1 = new it.pisa.caribel.swing2.border.cariEtchedBorder();
//		it.pisa.caribel.swing2.border.cariEmptyBorder emptyBorder1 = new it.pisa.caribel.swing2.border.cariEmptyBorder();
//		it.pisa.caribel.swing2.cariImageIcon cariImageIcon1 = new it.pisa.caribel.swing2.cariImageIcon();
//		it.pisa.caribel.swing2.border.cariEtchedBorder cariEtchedBorder1 = new it.pisa.caribel.swing2.border.cariEtchedBorder();
//		it.pisa.caribel.swing2.cariImageIcon cariImageIconQuery = new it.pisa.caribel.swing2.cariImageIcon();
//		it.pisa.caribel.swing2.cariImageIcon cariImageIcondettagli = new it.pisa.caribel.swing2.cariImageIcon();
//		javax.swing.JToolBar JToolBar1 = new javax.swing.JToolBar();
//		it.pisa.caribel.swing2.JCariActionButton JActionButtonExit = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariActionButton JActionButtonNew = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariActionButton JActionButtonApri = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariActionButton JActionButtonRimuovi = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariActionButton JActionButtonAnnulla = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariActionButton JActionButtonStampa = new it.pisa.caribel.swing2.JCariActionButton();
//		javax.swing.JScrollPane JScrollPane1 = new javax.swing.JScrollPane();
//		javax.swing.JPanel JPanelMain = new javax.swing.JPanel();
//		javax.swing.JPanel JPanelKey = new javax.swing.JPanel();
//		javax.swing.JLabel JLabelAnno = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldAnno = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldContatore = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariTextField n_cartellaDec = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JButtonBordered JButtonBorderedCartella = new it.pisa.caribel.swing2.JButtonBordered();
//		javax.swing.JLabel JLabelCodOper = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JButtonBordered JButtonBorderedContatore = new it.pisa.caribel.swing2.JButtonBordered();
//		javax.swing.JPanel JPanelArea = new javax.swing.JPanel();
//		javax.swing.JLabel JLabelDataPrest = new javax.swing.JLabel();
//		javax.swing.JLabel JLabelOperatore = new javax.swing.JLabel();
//		javax.swing.JLabel JLabelTipoOper = new javax.swing.JLabel();
//		javax.swing.JLabel JLabelOraInizio = new javax.swing.JLabel();
//		javax.swing.JLabel JLabelOraFine = new javax.swing.JLabel();
//		javax.swing.JLabel JLabelTempo = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldOraInizio = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldTempo = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldOraFine = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldDesOperqual = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldCodpresidio = new it.pisa.caribel.swing2.JCariTextField();
//		javax.swing.JPanel JPanelPrestazioni = new javax.swing.JPanel();
//		javax.swing.JLabel JLabelPrestaz = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldPrestaz_1 = new it.pisa.caribel.swing2.JCariTextField();
//		javax.swing.JLabel JLabelTempo_prest = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldPrest_num = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JButtonBordered JButtonBorderedPrestaz = new it.pisa.caribel.swing2.JButtonBordered();
//		javax.swing.JScrollPane JScrollPane2 = new javax.swing.JScrollPane();
//		it.pisa.caribel.swing2.JCariTextPane JCariTextPaneNote = new it.pisa.caribel.swing2.JCariTextPane();
//		javax.swing.JLabel JLabel3 = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JCariActionButton JCariActionButton1 = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldPrestDec = new it.pisa.caribel.swing2.JCariTextField();
//		it.pisa.caribel.swing2.JCariActionButton JCariActionButton2 = new it.pisa.caribel.swing2.JCariActionButton();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldPre_cont = new it.pisa.caribel.swing2.JCariTextField();
//		javax.swing.JLabel JLabel1 = new javax.swing.JLabel();
//		it.pisa.caribel.swing2.JButtonBordered JButtonBorderedPresidi = new it.pisa.caribel.swing2.JButtonBordered();
//		it.pisa.caribel.swing2.JCariTextField JCariTextFieldPresidio = new it.pisa.caribel.swing2.JCariTextField();
//
//		it.pisa.caribel.swing2.cariBasicAction exitPrestAction = new it.pisa.caribel.swing2.cariBasicAction();
//		JCariTextField JCariTextFieldQual_oper = new JCariTextField();
//		JCariTextField JCariTextFieldOperatore = new JCariTextField();
//		JCariTextField jCariTextFieldTempoPrest = new JCariTextField();
//
//		class SymAction implements java.awt.event.ActionListener {
//			public void actionPerformed(java.awt.event.ActionEvent event) {
//				Object object = event.getSource();
//				if (object == newAction)
//					newAction_actionPerformed(event);
//				else if (object == openAction)
//					openAction_actionPerformed(event);
//				else if (object == saveAction)
//					saveAction_actionPerformed(event);
//				else if (object == undoAction)
//					undoAction_actionPerformed(event);
//				else if (object == exitAction)
//					exitAction_actionPerformed(event);
//				else if (object == JCariTextFieldAnno)
//					JCariTextFieldAnno_actionPerformed(event);
//				else if (object == JButtonBorderedCartella)
//					JButtonBorderedCartella_actionPerformed(event);
//				else if (object == JButtonBorderedContatore)
//					JButtonBorderedContatore_actionPerformed(event);
//				else if (object == JButtonBorderedPrestaz)
//					JButtonBorderedPrestaz_actionPerformed(event);
//				else if (object == JButtonBorderedPresidi)
//					JButtonBorderedPresidi_actionPerformed(event);
//				else if (object == JCariTextFieldContatore)
//					JCariTextFieldContatore_actionPerformed(event);
//				else if (object == printAction)
//					printAction_actionPerformed(event);
//				else if (object == exitPrestAction)
//					exitPrestAction_actionPerformed(event);
//			}
//		}
//
//		void newAction_actionPerformed(java.awt.event.ActionEvent event) {
//			salvato = false;
//			JActionButtonNew.requestFocus();
//			tH = new compareHashtable(t.getHashtableKeyValue());
//			if (this.JCariTextFieldAnno.getUnmaskedText().trim().equals("")) {
//				new cariInfoDialog(null, "Campo non valido: inserire l'anno", "Errore!").show();
//				return;
//			}
//			setStato(this.INSERT);
//			int_contatore.setText("0");
//			if ((getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE).equals("05")) || (iu.canIUse(this.nomeFinestraChgOp, "MODI"))) // 04/11/10
//				JCariTextFieldOperatore.requestFocus();
//			Hop = null;
//
//		}
//
//		void openAction_actionPerformed(java.awt.event.ActionEvent event) {
//			int count = 0;
//			if (!(this.JCariTextFieldAnno.getUnmaskedText().equals("") || int_contatore.equals(""))) {
//				count = ExecSelect();
//			} else {
//				new cariInfoDialog(null, "Campo non valido: modificare il dato", "Errore!").show();
//				if (this.n_cartella.getUnmaskedText().equals(""))
//					this.n_cartella.requestFocus();
//				else
//					int_contatore.requestFocus();
//				return;
//			}
//			SettaTipoPrest();
//			numRiga = -1;
//		}
//
		private int controlloSalvataggio(int daDoveProvengo) throws Exception {//se non puo salvare ritorna -1
			String punto = ver  + "controlloSalvataggio ";
//			logger.debug("-->controlloSalvataggio/t.getHashtableKeyValue(): " + t.getHashtableKeyValue());
			logger.debug("-->controlloSalvataggio/cbx_contatto.getValid(): " + cbx_contatto.isRequired());
			
//			if(!verificaDataInSchedaSo()){
//				logger.debug(punto + " Non salvo in quanto la scheda SO è SCADUTA, OPPURE NON ESISTE ");
//				return -1;
//			}
			
			if (check_save_SO_PRESENTE == CTS_SO_PRESENTE) {
				if (!verificaDataInSchedaSo(true, daDoveProvengo)) {
					logger.debug(punto + " Non salvo in quanto la scheda SO è SCADUTA, OPPURE NON ESISTE ");
					return -1;
				}
			} else {
				logger.trace(punto + " posso salvare");
			}
			
			
			if(!PROVENIENZA.equals("ACCES")){
				cbx_contatto.setRequired(false);
			}else{
				cbx_contatto.setRequired(true);
			}
			
			if((int_anno.getValue().isEmpty() || currentIsasRecord==null) && int_data.getValue()!=null){
				Calendar cal = Calendar.getInstance();
				cal.setTime(int_data.getValue());
				int_anno.setValue(new Integer(cal.get(Calendar.YEAR)).toString());
			}
			
			if(stato == INSERT){
				UtilForComponents.testRequiredFields(self);
			}
			//02/11/2006 Controllo che l'anno in chiave sia lo stesso di quello della data prestazione
			String annoData = int_data.getValueForIsas().substring(0, 4);
			if (!int_anno.getValue().equals(annoData)) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.annoIncongruente"));
				return -1;
			}
			if (OPERATORIOK.indexOf(JCariTextFieldTipo_oper.getValue().trim()) == -1) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.operatoreNonAbilitatoInsermento"));
				return -1;
			}
			if (PROVENIENZA.equals("ACCES")	&& cbx_contatto.getSelectedIndex() == -1 ) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.contattoObbligatorio"));
				return -1;
			}
			if (PROVENIENZA.equals("ACCES") && JCariTextFieldTipo_oper.getValue().equals("01")) {
				String tipoServ = tipoServizio();
				if (!tipoServ.equals(this.JCariTextFieldTipoServizio.getText())) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.incoerenzaContattoTipoServizio"));
					return -1;
				}
			}

			if (cod_operatore.getValue()==null) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.operatoreObbligatorio"));
				cod_operatore.focus();
				return -1;
			}
			if (cbx_unitaFunzionale.isVisible() && cbx_unitaFunzionale.getSelectedIndex() == -1 || (// (""+cbx_unitaFunzionale.getSelectedItem()).trim().equals(".") ||
					("" + cbx_unitaFunzionale.getSelectedItem()).equals(""))) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.unitafunzionaleObbligatoria"));
				return -1;
			}
			if (cod_presidio.getValue()== null) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.presidioObbligatorio"));
				cod_presidio.focus();
				return -1;
			}
			//ilaria :controllo se è stato scelto il tipo prestazione e che non sia su nessuno
			if (this.tipoPrestazione.getSelectedValue().equals("N")) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.tipoPrestazioneObbligatoria"));
				tipoPrestazione.focus();
				return -1;
			}
			if (!ControllaDataPresta())
				return -1;

			// 01/09/08 m.
			if (!checkDtPrestCartella())
				return -1;
			return 1;
		}

		public boolean doSaveForm() throws Exception {
			String punto = "doSaveForm ";
			if (stato == INSERT){
				if ((cod_prestazione.getValue()==null || cod_prestazione.getValue().isEmpty()) && caribellb2.getItemCount() == 0) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
					return false;
				}
			}
			if (controlloSalvataggio(CTS_SAVE_FORM) == -1)
				return false;
			
			
			
			if (check_save_SO_PRESENTE == CTS_SO_PRESENTE) {
				if (!verificaDataInSchedaSo(true, CTS_SAVE_FORM)) {
					logger.debug(punto + " Non salvo in quanto la scheda SO è SCADUTA ");
					return false;
				}
			} else {
				logger.trace(punto + " posso salvare");
			}
			
			
			//RILEGGO HOP PER VEDERE SE CI SONO DELLE MODIFICHE NELLA PARTE SUPERIORE
			//DELLA FORM
			if ((Hop != null && Hop.isModified(UtilForBinding.getHashtableFromComponent(hopVLayout, null, false, true))) && this.stato != INSERT)
				modi_op = true;

			if (modi_op) {
				if (update_prestaz == true) {
					if (cod_prestazione.getValue()==null || cod_prestazione.getValue().isEmpty()) {
						UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
						cod_prestazione.focus();
						return false;
					}
				} else {
					if ((cod_prestazione.getValue()==null || cod_prestazione.getValue().isEmpty()) && caribellb2.getItemCount() == 0) {
						UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
						cod_prestazione.focus();
						return false;
					}
				}
			} else if (caribellb2.getItemCount() == 0 && cod_prestazione.getValue().isEmpty()) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
				cod_prestazione.focus();
				return false;
			}
			if (PROVENIENZA.equals("ACCES")) {
				String cod = this.cbx_contatto.getSelectedValue();
				StringTokenizer tok = new StringTokenizer(cod, "#");
				tok.nextToken();
				cod = tok.nextToken();
				contatto.setValue(cod);
			} else {
				//sono nel caso di accessi occasionali devo mettere il contatto a 0
				contatto.setValue("0");
			}

            //elisa b 27/09/11 (x Lazio) : impedire la registrazione se la presa in carico non e' attiva in quella data
            if (ManagerProfile.isConfigurazioneLazio(getProfile())) {
              //mod elisa b 22/11/11 : il controllo si effettua solo se l'accesso non e' occasionale
              if (PROVENIENZA.equals("ACCES")) {
                  Hashtable hP = new Hashtable();
                  //valori è una stringa del tipo giorno-qta| (il metodo nasce x la reg accessi MMG)
                  String dtP = int_data.getValueForIsas();
                  hP.put("data_prest", dtP);
                  hP.put("n_cartella", n_cartella.getText());
                  try {
                      if (!(Boolean) invokeGenericSuEJB(myEJB, hP, "controlloEsistenzaPresaInCarico"))//db.controlloEsistenzaPresaInCarico(hP))
                        return false;
                  } catch (Exception e) {
                      if (e instanceof CariException)
                    	  UtilForUI.standardExclamation(e.getMessage());
//                          new it.pisa.caribel.swing2.cariInfoDialog(null, e.getMessage(), "Attenzione!").show();
                      return false;
                  }
              }
            }
            
			this.ExecUpdate();
			doFreezeForm();
			settaSaveMessaggioSo(CTS_SO_PRESENTE);
//			u.Enable(this.JPanelPrestazioni, true);
//			this.JButtonBorderedPrestaz.setDisabled(false);
//			this.printAction.setDisabled(false);
//			Hgriglia = new compareHashtable(forGriglia.getHashtableKeyValue());
//			JCariTextFieldPrestDec.setDisabled(false);
//			JCariTextFieldPrestDec.setEditable(true);
//			JCariTextFieldPrestaz_1.setDisabled(false);
//			JCariTextFieldPrestaz_1.setEditable(true);
//			JButtonBorderedPrestaz.setDisabled(false);
			numRiga = -1;
			this.JCariTextFieldTipoServizio.setValue(tipoServizio());
			salvato = true;
			UtilForBinding.setComponentReadOnly(hopVLayout, true);
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
			return true;
		}
//
//		void undoAction_actionPerformed(java.awt.event.ActionEvent event) {
//			int NumRighe = jCariTable1.getRowCount();
//			if (NumRighe == 0 && stato == this.UPDATE_DELETE) {
//				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
//				JCariTextFieldPrestaz_1.requestFocus();
//				return;
//			}
//			//  this.setStato(this.WAIT);
//			this.setDefault();
//			this.n_cartellaDec.setEditable(true);
//			this.n_cartellaDec.setDisabled(false);
//			salvato = true;
//			this.setStato(this.WAIT);
//			JCariTextFieldPrestaz_1.setEditable(true);
//			JCariTextFieldPrestaz_1.setDisabled(false);
//		}
//
//		void exitAction_actionPerformed(java.awt.event.ActionEvent event) {
//			int NumRighe = jCariTable1.getRowCount();
//			if (NumRighe == 0 && stato == this.UPDATE_DELETE) {
//				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
//				JCariTextFieldPrestaz_1.requestFocus();
//				return;
//			}
//			if (salvato != false && !this.JCariTextFieldPrestaz_1.getUnmaskedText().equals("")) {
//				if (iu.canIWrite(currentIsasRecord)) {
//					int i = new it.pisa.caribel.swing2.cariYesNoDialog(null, "Ci sono delle informazioni non salvate! \nVuoi salvare?",
//							"Attenzione!").show();
//					if (i == 0)
//						saveAction_actionPerformed(event);
//				} else
//					myDispose();
//			}
//			if (salvato == false) {
//				if (tH != null && tH.isModified(t))//.getHashTableForSelect()))
//				{
//					if (iu.canIWrite(currentIsasRecord)) {
//						int i = new it.pisa.caribel.swing2.cariYesNoDialog(null, "Ci sono delle informazioni non salvate! \nVuoi salvare?",
//								"Attenzione!").show();
//						if (i == 0)
//							saveAction_actionPerformed(event);
//						else
//							myDispose();
//					} else
//						myDispose();
//					if (salvato)
//						myDispose();
//				} else
//					myDispose();
//			} else
//				myDispose();
//		}
//
//		public void onUpdate(it.pisa.caribel.swing2.event.JCariTableEvent e) {
//
//			String tipoOper = JCariTextFieldTipo_oper.getValue();
//			if (e.getTableName().equals("cartella")) {
//				this.cartella.putDBRecordUpdate(e.getResult());
//				try {
//					this.setSelected(true);
//				} catch (Exception e1) {
//					logger.error(e1);
//				}
//				JCariTextFieldTipo_oper.setUnmaskedText(tipoOper);
//				this.ExecSelectCartella();
//			} else if (e.getTableName().equals("prestaz")) {
//				String tipo = tipo_prestazione.getValue();
//				this.prest.putDBRecordUpdate(e.getResult());
//				this.tipo_prestazione.setUnmaskedText(tipo);
//				try {
//					this.setSelected(true);
//				} catch (Exception e1) {
//					logger.error(e1);
//				}
//				this.ExecSelectPrestaz();
//			} else if (e.getTableName().equals("continterv")) {
//
//				this.continterv.putDBRecordUpdate(e.getResult());
//			logger.debug.setSelected(true);
//					logger.debug(this.JCariTextFieldAnno.getUnmaskedText());
//				} catch (Exception e1) {
//					logger.error(e1);
//				}
//				if (!this.n_cartella.getUnmaskedText().equals(""))
//					ExecSelect();
//			} else if (e.getTableName().equals("operatori")) {
//				String tipo = tipo_prestazione.getValue();
//				String tpOper = JCariTextFieldTipo_oper.getValue();// 10/01/08
//				this.operatori.putDBRecordUpdate(e.getResult());
//				tipo_prestazione.setUnmaskedText(tipo);
//				//             logger.debug(operatori.getHashtableKeyValue().toString()) ;
//				JCariTextFieldTipo_oper.setUnmaskedText(tpOper); // 10/01/08
//				try {
//					this.setSelected(true);
//				} catch (Exception e1) {
//					logger.error(e1);
//				}
//				this.ExecSelectOperatori();
//			} else if (e.getTableName().equals("presidi")) {
//				this.presidi.putDBRecordUpdate(e.getResult());
//				try {
//					this.setSelected(true);
//				} catch (Exception e1) {
//					logger.error(e1);
//				}
//			}
//		}
//
//		void JCariTextFieldAnno_actionPerformed(java.awt.event.ActionEvent event) {
//			if (!(this.JCariTextFieldAnno.getUnmaskedText().equals("") || int_contatore.getUnmaskedText().equals(""))) {
//				ExecSelect();
//			} else {
//				new cariInfoDialog(null, "Campo non valido: modificare il dato", "Errore!").show();
//				if (this.JCariTextFieldAnno.getUnmaskedText().equals(""))
//					this.JCariTextFieldAnno.requestFocus();
//				else
//					int_contatore.requestFocus();
//				return;
//			}
//			SettaTipoPrest();
//			numRiga = -1;
//		}
//
//		public void onTextModified(it.pisa.caribel.swing2.event.JCariTextModifiedEvent e) {
//			logger.debug("onTextModified:cartella inizio...");
//			if (e.getSource() == this.n_cartella) {
//				if (!((String) this.n_cartella.getUnmaskedText()).equals("")) {
//					cbx_contatto.getSelectedValue().setText("");
//					this.ExecSelectCartella();
//				} else {
//					this.n_cartellaDec.setText("");
//					this.n_cartellaDec.setEditable(true);
//					this.n_cartellaDec.setDisabled(false);
//					//devo svuotare anche la combo contatti
//					this.jCariTextFieldCombo.setValue("");
//					this.jCariTextFieldCombo.setVisible(false);
//					if (PROVENIENZA.equals("ACCES"))
//						this.cbx_contatto.setVisible(true);
//					String tipoServizio = this.JCariTextFieldTipoServizio.getText();
//					sbiancaCombo();
//					this.JCariTextFieldTipoServizio.setText(tipoServizio);
//				}
//			} else if (e.getSource() == this.JCariTextFieldPrestaz_1) {
//				if (this.JCariTextFieldOperatore.getUnmaskedText().equals("")) {
//					new it.pisa.caribel.swing2.cariInfoDialog(null, "La prestazione dipende dal tipo operatore." + "\n Inserire l'operatore! ",
//							"Attenzione!").show();
//					this.JCariTextFieldPrestaz_1.setUnmaskedText("");
//				} else {
//					if (!((String) this.JCariTextFieldPrestaz_1.getUnmaskedText()).equals("")) {
//						int rit = ControlloPrestazione();
//						if (rit == -1)
//							return;
//						else {
//							this.ExecSelectPrestaz();
//						}
//					} else {
//						this.JCariTextFieldPrestDec.setText("");
//						this.JCariTextFieldPrestDec.setEditable(true);
//						this.JCariTextFieldPrestDec.setDisabled(false);
//						this.JCariTextFieldPrest_num.setText("");
//						this.JCariTextPaneNote.setText("");
//						this.cariCurrencyTextFieldImporto.putItValue("");
//					}
//				}
//			} else if (e.getSource() == this.JCariTextFieldOperatore) {
//				if (!((String) this.JCariTextFieldOperatore.getUnmaskedText()).equals("")) {
//					this.ExecSelectOperatori();
//				} else {
//					this.JCariTextFieldOperatore.setUnmaskedText("");
//					JCariTextFieldOperatore.setText("");
//					JCariTextFieldQual_oper.setText("");
//					JCariTextFieldDesOperqual.setUnmaskedText("");
//					this.JCariTextFieldDesOper.setUnmaskedText("");
//					this.JCariTextFieldDesOper.setEditable(true);
//					this.JCariTextFieldDesOper.setDisabled(false);
//					/*se ho inserto delle prestazioni non devo sbiancare il tipo*/
//					if (cariStringTableModel1.getRowCount() <= 0 && this.JCariTextFieldPrestaz_1.getUnmaskedText().equals(""))
//						tipo_prestazione.setUnmaskedText("");
//					JCariTableGrigliaPrestaz.setModel(new CaribelListModel());
//				}
//			} else if (e.getSource() == this.JCariTextFieldCodpresidio) {
//				if (!((String) this.JCariTextFieldCodpresidio.getUnmaskedText()).equals(""))
//					this.ExecSelectPresidi();
//				else {
//					this.JCariTextFieldPresidio.setText("");
//					this.JCariTextFieldPresidio.setEditable(true);
//					this.JCariTextFieldPresidio.setDisabled(false);
//				}
//			} else if (e.getSource() == this.JCariTextFieldOraInizio) {
//				if (!((String) this.JCariTextFieldOraInizio.getUnmaskedText()).equals("")) {
//					String ora = this.JCariTextFieldOraInizio.getUnmaskedText();
//					if (ora.length() == 2)
//						this.JCariTextFieldOraInizio.setText(ora + "_00");
//					if (ora.length() == 4)
//						this.JCariTextFieldOraInizio.setText(ora + "0");
//					int ini = this.ControlloOra(this.JCariTextFieldOraInizio.getUnmaskedText(), "inizio");
//					if (ini == -1) {
//						this.JCariTextFieldOraInizio.setText("");
//						this.JCariTextFieldOraInizio.requestFocus();
//					} else {
//						if (!this.JCariTextFieldOraFine.getUnmaskedText().equals(""))
//							CalcoloTempo((String) this.JCariTextFieldOraInizio.getUnmaskedText(), (String) this.JCariTextFieldOraFine
//									.getUnmaskedText());
//					}
//				}
//			} else if (e.getSource() == this.JCariTextFieldOraFine) {
//				if (!((String) this.JCariTextFieldOraFine.getUnmaskedText()).equals("")) {
//					String ora = this.JCariTextFieldOraFine.getUnmaskedText();
//					if (ora.length() == 2)
//						this.JCariTextFieldOraFine.setText(ora + "_00");
//					if (ora.length() == 4)
//						this.JCariTextFieldOraFine.setText(ora + "0");
//					int fine = this.ControlloOra(this.JCariTextFieldOraFine.getUnmaskedText(), "fine");
//					if (fine == -1) {
//						this.JCariTextFieldOraFine.setText("");
//						this.JCariTextFieldOraFine.requestFocus();
//					} else if (!((String) this.JCariTextFieldOraInizio.getUnmaskedText()).equals(""))
//						CalcoloTempo((String) this.JCariTextFieldOraInizio.getUnmaskedText(), (String) this.JCariTextFieldOraFine
//								.getUnmaskedText());
//				}
//			} else if (e.getSource() == this.JCariTextFieldTempo) {
//				if (!((String) this.JCariTextFieldTempo.getUnmaskedText()).equals("")) {
//					/*                int tempo=this.ControlloOra(this.JCariTextFieldTempo.getUnmaskedText(),"tempo");
//					                if (tempo==-1){
//					                    this.JCariTextFieldTempo.setText("");
//					                    this.JCariTextFieldTempo.requestFocus();
//					                }*/
//				}
//			}
//			logger.debug("onTextModified:fine...");
//		}

		//J16-03 Prima di andare a inserire una prestazione, se il tipo operatore è sociale
		//devo controllare che mi abbiano scelto la cartella e il contatto altrimenti non so
		//che tipo prestazione andare a prendere
		private int ControlloPrestazione() {
			String tipo_oper = JCariTextFieldTipo_oper.getValue();
			if (tipo_oper.equals("01")) {
				if (n_cartella.getValue()!=null && n_cartella.getValue().equals("")) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.mancanzaCartella"));
					Events.sendEvent(Events.ON_CHANGE, cod_prestazione, null);
					n_cartella.focus();
					return -1;
				}
				if (cbx_contatto.isVisible() && (cbx_contatto.getSelectedIndex() == -1)) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.mancanzaContatto"));
					Events.sendEvent(Events.ON_CHANGE, cod_prestazione, null);
					return -1;
				}
			}
			return 1;
		}

		public void onCalcolaDurata(Event evt)
		{
			int tipo= Integer.parseInt((String)evt.getData());
			int_tempo.setText("");
			if ((int_ora_in.getValue() == null ) || (int_ora_out.getValue()==null))
				return;

			if (int_ora_in.getValue().after(int_ora_out.getValue())) {
				if (tipo == 1) {
					int_ora_in.setText("");
					int_ora_in.setFocus(true);
				} else {
					int_ora_out.setText("");
					int_ora_out.setFocus(true);
				}
				UtilForUI.standardExclamation(Labels.getLabel("common.msg.orari.incongruenti"));
			} else {
				int durata = (int) ((int_ora_out.getValue().getTime() - int_ora_in.getValue().getTime())/ (60 * 1000));
				int_tempo.setText("" + durata);
			}
		}

//		void JButtonBorderedPrestaz_actionPerformed(java.awt.event.ActionEvent event) {
//			if (this.JCariTextFieldOperatore.getUnmaskedText().equals("")) {
//				new it.pisa.caribel.swing2.cariInfoDialog(null, "La prestazione dipende dal tipo operatore." + "\n Inserire l'operatore! ",
//						"Attenzione!").show();
//				this.JCariTextFieldPrestaz_1.getUnmaskedText();
//			} else {
//				int rit = ControlloPrestazione();
//				if (rit == -1)
//					return;
//				else {
//					// this.insertPrest();
//					String tipo = tipo_prestazione.getValue();
//					if (tipo.equals("")) {
//						new it.pisa.caribel.swing2.cariInfoDialog(null, "Non è possibile inserire interventi\n"
//								+ "Manca nel configuratore il tipo prestazione associato al tipo operatore", "Attenzione!").show();
//						return;
//					}
//					//                new JFrameGridPrestaz(this,false,tipo,this.JCariTextFieldPrestDec.getUnmaskedText().trim(),"interv");
//					// 10/01/08: apertura modale
//					new JFrameGridPrestaz(this, (JCariTableListener) this, tipo, this.JCariTextFieldPrestDec.getUnmaskedText().trim(), "interv");
//				}
//			}
//		}



//		void JCariTextFieldContatore_actionPerformed(java.awt.event.ActionEvent event) {
//			if (!(this.JCariTextFieldAnno.getUnmaskedText().equals("") || int_contatore.getUnmaskedText().equals(""))) {
//				ExecSelect();
//			} else {
//				new cariInfoDialog(null, "Campo non valido: modificare il dato", "Errore!").show();
//				if (this.JCariTextFieldAnno.getUnmaskedText().equals(""))
//					this.JCariTextFieldAnno.requestFocus();
//				else
//					int_contatore.requestFocus();
//				return;
//			}
//			SettaTipoPrest();
//			numRiga = -1;
//		}
//

		protected void doStampa(){
			if (!testValidKey())
				return;

			//recupero parametri
			String user = CaribelSessionManager.getInstance().getMyLogin().getUser();
			String passwd = CaribelSessionManager.getInstance().getMyLogin().getPassword();
			String anno = int_anno.getValue();
			String data = int_data.getValueForIsas();
//			data = data.substring(6, 10) + "-" + data.substring(3, 5) + "-" + data.substring(0, 2);
			String contatore = int_contatore.getValue().toString();
			String operatore = cod_operatore.getValue();
			String tipo = qualOperatore.getValue();
			String tipo_prest = this.tipo_prestazione.getValue();
			//invocazione alla servlet
			String servlet = "";
			servlet = getProfile().getStringFromProfile("fop") + "?EJB=SINS_FOINTERV&USER=" + user + "&WORD=" + passwd + "&METHOD=query_interv&anno=" + anno
					+ "&data=" + data + "&contatore=" + contatore + "&operatore=" + operatore + "&tipo=" + tipo + "&tipo_prest=" + tipo_prest
					+ "&REPORT=interv.fo";
			logger.debug("percorso servlet FoInterv " + servlet);
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
		}
		
		//---------testa la validita delle chiavi sul PanelKey
		private boolean testValidKey() {
			boolean b = !(int_anno.getValue()!= null && !int_anno.getValue().isEmpty());
			if (b) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.assenzaDati"));
				int_anno.focus();
			} else {
				b = int_contatore.getValue()== null;
				if (b) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.assenzaDati"));
					int_contatore.focus();
				} else {
				}
			}
			return !b;
		}
//
//		void exitPrestAction_actionPerformed(java.awt.event.ActionEvent event) {
//			int NumRighe = jCariTable1.getRowCount();
//			if (NumRighe == 0) {
//				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.prestazioneObbligatoria"));
//				JCariTextFieldPrestaz_1.requestFocus();
//				return;
//			}
//			//this.JCariTextFieldCodpresidio.setText("");
//			//this.JCariTextFieldPresidio.setText("");
//			/*   this.JCariTextFieldPresidio.setEditable(true);
//			   this.JCariTextFieldPresidio.setDisabled(false);
//			  */this.JCariTextFieldCodpresidio.setEditable(true);
//			this.JCariTextFieldCodpresidio.setDisabled(false);
//			this.JCariTextFieldOraInizio.setText("");
//			this.JCariTextFieldOraFine.setText("");
//			this.JCariTextFieldTempo.setText("");
//			this.JCariTextFieldTempoGO.setText("");
//			int_contatore.setText("0");
//			this.jCariTable1.deleteAll();
//			this.jCariTextPaneNote1.setText("");
//			this.cariCurrencyTextFieldImporto.putItValue("");
//			cbx_contatto.getSelectedValue().setText("");
//			String oper = this.JCariTextFieldOperatore.getUnmaskedText();
//			String cognoper = this.JCariTextFieldDesOper.getUnmaskedText();
//			String qual = this.JCariTextFieldQual_oper.getUnmaskedText();
//			String codpres = this.JCariTextFieldCodpresidio.getUnmaskedText();
//			String despres = this.JCariTextFieldPresidio.getUnmaskedText();
//			this.setStato(INSERT);
//			logger.debug("\n-->>In exitPrestAction_actionPerformed: Chiamo CaricaPianoInterventi");
//			CaricaPianoInterventi();
//			this.JCariTextFieldOperatore.setUnmaskedText(oper);
//			this.JCariTextFieldDesOper.setUnmaskedText(cognoper);
//			this.JCariTextFieldQual_oper.setUnmaskedText(qual);
//			this.JCariTextFieldCodpresidio.setUnmaskedText(codpres);
//			this.JCariTextFieldPresidio.setUnmaskedText(despres);
//			if (!this.JCariTextFieldPresidio.getUnmaskedText().equals("")) {
//				this.JCariTextFieldPresidio.setEditable(false);
//				this.JCariTextFieldPresidio.setDisabled(true);
//			}
//			// 02/07/07 ---
//			if (cariStringComboBoxModelContatti.getSize() == 1)
//				this.cbx_contatto.setSelectedIndex(0);
//			else
//				this.cbx_contatto.setSelectedIndex(-1);
//			// 02/07/07 ---
//		}
//
//		void JCariActionButton4_actionPerformed(ActionEvent e) {
//			//int riga=this.jCariTable1.getSelectedRow();
//			numRiga = this.jCariTable1.getSelectedRow();
//			if (numRiga < 0) {
//				new cariInfoDialog(null, "Selezionare una riga della tabella!", "Attenzione!").show();
//				return;
//			}
//			this.JCariTextFieldPrestaz_1.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 0));
//			this.jCariTextFieldTempoPrest.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 3));
//			this.JCariTextFieldPrestDec.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 1));
//			this.JCariTextFieldPrest_num.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 2));
//			this.JCariTextPaneNote.setText((String) this.cariStringTableModel1.getValueAt(numRiga, 4));
//			this.jCariTextPaneDesDett.setText((String) this.cariStringTableModel1.getValueAt(numRiga, 6));
//			String importo = (String) this.cariStringTableModel1.getValueAt(numRiga, 5);
//			if (!importo.equals(" "))
//				this.cariCurrencyTextFieldImporto.putItValue(importo);
//			else
//				cariCurrencyTextFieldImporto.putItValue("");
//			this.insertDett = false;
//			//NON PUO ESSERE CAMBIATO IL CODICE PRESTAZIONE
//			DisabilitaPrest();
//			cancellato = false;//viene aperta una riga quindi nella tabella esistono
//		}
//
//		private void DisabilitaPrest() {
//			//funzione che serve a disabilitare la prestazione nel caso in cui loro ne
//			//aprono una dalla griglia,possono cambiare solo l'importo,il numero e le note
//			JCariTextFieldPrestaz_1.setDisabled(true);
//			JCariTextFieldPrestDec.setDisabled(true);
//			JButtonBorderedPrestaz.setDisabled(true);
//		}
//
//		void JCariActionButton3_actionPerformed(ActionEvent e) {
//			this.JCariTextFieldPrestaz_1.setUnmaskedText("");
//			this.JCariTextFieldPrestDec.setUnmaskedText("");
//			this.JCariTextFieldPrest_num.setUnmaskedText("");
//			this.JCariTextPaneNote.setText("");
//			this.cariCurrencyTextFieldImporto.putItValue("");
//			jCariTextPaneDesDett.setText("");
//			JCariTextFieldPrestDec.setDisabled(false);
//			JCariTextFieldPrestaz_1.setDisabled(false);
//			JButtonBorderedPrestaz.setDisabled(false);
//		}
//
//		private long ttime = 0;
////		JCariActionButton JCariActionButton5 = new it.pisa.caribel.swing2.JCariActionButton();
////		JCariTextField jCariTextFieldDiff = new JCariTextField();
////		//  JCariTextField jCariTextFieldComune = new JCariTextField();
////		JCariTextField JCariTextFieldNome = new JCariTextField();
////		JLabel JLabelContatto = new javax.swing.JLabel();
////		cariStringComboBoxModel cariStringComboBoxModelContatti = new cariStringComboBoxModel();
////		JScrollPane jScrollPane3 = new JScrollPane();
////		JCariTextPane jCariTextPaneDesDett = new JCariTextPane();
////		JLabel jLabel2 = new JLabel();
////		JLabel JLabelPrestaz1 = new javax.swing.JLabel();
////		cariCurrencyTextField cariCurrencyTextFieldImporto = new cariCurrencyTextField();
////		JCariTextField JCariTextFieldArea = new it.pisa.caribel.swing2.JCariTextField();
////		JCariTextField JCariTextFieldCod_comune = new it.pisa.caribel.swing2.JCariTextField();
////		JLabel JLabelUni = new javax.swing.JLabel();
////		cariStringComboBoxModel cariStringComboBoxModel2 = new cariStringComboBoxModel();
////		XYLayout xYLayout5 = new XYLayout();
////		JCariRadioButton jCariRadioButtonNessuno = new JCariRadioButton();
////		JCariTextField JCariTextFieldChiaveCombo = new it.pisa.caribel.swing2.JCariTextField();
////		cariObjectTableModel cariObjectTableModel1 = new cariObjectTableModel();
////		JPanel JPanelPrestaz = new javax.swing.JPanel();
////		JCariTable JCariTableGrigliaPrestaz = new it.pisa.caribel.swing2.JCariTable();
////		JScrollPane JScrollPane4 = new javax.swing.JScrollPane();
////		XYLayout xYLayout6 = new XYLayout();
////		JCariActionButton JCariActionButtonConferma = new it.pisa.caribel.swing2.JCariActionButton();
////		JButtonBordered JButtonBorderedOperatore = new it.pisa.caribel.swing2.JButtonBordered();
////		JCariTextField JCariTextFieldDesOper = new it.pisa.caribel.swing2.JCariTextField();
////		JCariActionButton JCariActionButtonDeseleziona = new it.pisa.caribel.swing2.JCariActionButton();
////		JCariTextField jCariTextFieldTipoOperConstraint = new JCariTextField();
////		// 01/09/08 ---
//
//		// 01/09/08 ---
//
//		void jCariTable1_mouseClicked(MouseEvent e) {
//			long diff = System.currentTimeMillis() - ttime;
//			if (diff <= 500) {
//				if (this.JCariTextFieldPrestaz_1.getUnmaskedText().equals("")) {
//					numRiga = this.jCariTable1.getSelectedRow();
//					this.JCariTextFieldPrestaz_1.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 0));
//					this.jCariTextFieldTempoPrest.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 3));
//					this.JCariTextFieldPrestDec.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 1));
//					this.JCariTextFieldPrest_num.setUnmaskedText((String) this.cariStringTableModel1.getValueAt(numRiga, 2));
//					this.JCariTextPaneNote.setText((String) this.cariStringTableModel1.getValueAt(numRiga, 4));
//					this.jCariTextPaneDesDett.setText((String) this.cariStringTableModel1.getValueAt(numRiga, 6));
//					String importo = (String) this.cariStringTableModel1.getValueAt(numRiga, 5);
//					if (!importo.equals(" "))
//						this.cariCurrencyTextFieldImporto.putItValue((String) this.cariStringTableModel1.getValueAt(numRiga, 5));
//					else
//						cariCurrencyTextFieldImporto.putItValue("");
//					this.insertDett = false;
//					cancellato = false;//viene aperta una riga quindi nella tabella esistono
//					DisabilitaPrest();
//				} else
//					new it.pisa.caribel.swing2.cariInfoDialog(this, "Per accedere alle informazioni del campo premere ANNULLA\n",
//							"Impossible eseguire l'operazione").show();
//			}
//			ttime = System.currentTimeMillis();
//		}
//
//		void JCariActionButton5_actionPerformed(ActionEvent e) {
//			spostato in farmaciPrestazioni
//		}

		public void sbiancaCombo() {

			cbx_contatto.clear();
			if (PROVENIENZA.equals("ACCES")){
				this.cbx_contatto.setVisible(true);
				lbl_contatto.setVisible(true);
			}
			cbx_contatto.setSelectedIndex(-1);
			jCariTextFieldCombo.setVisible(false);
			jCariTextFieldCombo.setValue("");
			tipoPrestazione.setDisabled(false);
		}

		public boolean ControllaDataPresta() {

			String data_oggi = it.pisa.caribel.util.procdate.getitaDate();
			String data_prest = UtilForBinding.getStringClientFromDate(int_data.getValue());
			if (int_data.getValue()!=null) {
				int rit = controllaDataPrest(data_oggi, data_prest);
				if (rit == 2) {
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaDataPrestazione"));
					int_data.setValue(null);
					int_data.focus();
					return false;
				}
			}
			return true;
		}

		private void SettaTipoPrest() {
			if (!isReadOnly()) {
				String tipo_oper = JCariTextFieldTipo_oper.getValue();
				String tipoServ = tipoServizio();
				if (tipo_oper.equals("01")) {
					if (tipoServ.equals("2")) {
						tipoPrestazione.setSelectedValue("A");
						tipoPrestazione.setDisabled(true);
					} else {
						tipoPrestazione.setSelectedValue("D");// 02/07/07
						tipoPrestazione.setDisabled(false);
					}
				} else {
					tipoPrestazione.setDisabled(false);
				}
				this.JCariTextFieldTipoServizio.setText(tipoServ);
			} else
				tipoPrestazione.setDisabled(true);
		}

		public void onChangeContatto(ForwardEvent e) throws Exception{
			/*ilaria se il tipo operatore 01 devo controllare il tipo servizio
			e disabilitarlo
			*/
			logger.debug("In onChangeContatto");
			if (PROVENIENZA.equals("OCCAS"))
				return;
			if (this.cbx_contatto.getSelectedIndex() == -1)
				return;
			if (cbx_contatto.getSelectedItem() != null && cbx_contatto.getSelectedValue().isEmpty())
				return;
			String cod = this.cbx_contatto.getSelectedValue();
			StringTokenizer tok = new StringTokenizer(cod, "#");
			tok.nextToken();
			cod = tok.nextToken();
			contatto.setValue(cod);
			String tipo_oper = JCariTextFieldTipo_oper.getValue();
			if (tipo_oper.equals("01")){
				/*se sono in inserimento posso mettere un contatto di tipo servizio 1
				inserire delle prestazioni e poi cambiare il contatto inserendo uno con tipo servizio 2
				Controllo che se esistono delle prestazioni non posso cambiare il tipo servizio
				*/
				if (!(cod_prestazione !=null || caribellb2.getItemCount() > 0)) {
					String tipoServ = tipoServizio();
					if (!tipoServ.equals(this.JCariTextFieldTipoServizio.getText())) {
						UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaContattoTipoServizio"));
						return;
					}
				}
			}
			SettaTipoPrest();
			//   this.JCariTextFieldTipoServizio.setText(tipoServizio()) ;
			indiceCombo = this.cbx_contatto.getSelectedIndex();
			if (tipo_oper.equals("01")) {
				insertPrest();
			}
			//CJ 30/03/06 E' stato spostato fuori perchè come era prima non mi caricava
			//per bene i contatti degli infermieri.
			CaricaGrigliaPrestaz();
			logger.debug("onChangeContatto-->>stato" + stato);
			// 05/03/14 mv  SEGN.55276 if (stato != UPDATE_DELETE) {
			if (stato == INSERT) {
				logger.debug("onChangeContatto-->>: Chiamo CaricaPianoInterventi");
				CaricaPianoInterventi();

				// 18/03/11
				if ((PROVENIENZA.equals("ACCES")) && (gl_strTipoOperatore.equals(CostantiSinssntW.TIPO_OPERATORE_INFERMIERE)))
					caricaModalita();
			}
		}

		public void onConfermaSelezione(Event event) throws Exception{
			try{
			if (controlloSalvataggio(CTS_BOTTONE) == -1)
				return;
			int ret = this.passaPrestazioni();
			if (ret == -1) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniOperatore"));
				return;
			} else if (ret == -2) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
				return;
			}

			//elisa b 27/09/11 (x Lazio) : impedire la registrazione se la presa in carico non e' attiva in quella data
			if (ManagerProfile.isConfigurazioneLazio(getProfile())){//profile.getParameter("adrsa_ubic").equals("" + JMainmdi.UBI_RLAZI)) {
				//mod elisa b 22/11/11 : il controllo si effettua solo se l'accesso non e' occasionale
				if (PROVENIENZA.equals("ACCES")) {
					Hashtable hP = new Hashtable();
					//valori è una stringa del tipo giorno-qta| (il metodo nasce x la reg accessi MMG)
					String dtP = int_data.getValueForIsas();
					hP.put("data_prest", dtP);
					hP.put("n_cartella", n_cartella.getValue());
					try {
						if (!(Boolean) invokeGenericSuEJB(myEJB, hP, "controlloEsistenzaPresaInCarico"))
							return;
					} catch (Exception ex) {
						if (ex instanceof CariException)
							UtilForUI.standardExclamation(ex.getMessage());
						return;
					}
				}
			}

			Vector vh = new Vector();
			this.PreparaVettoreHash(vh);
			if (PROVENIENZA.equals("OCCAS")) {
				this.cbx_contatto.setValue("0");
				contatto.setValue("0");
			} else {
				String cod = this.cbx_contatto.getSelectedValue();
				StringTokenizer tok = new StringTokenizer(cod, "#");
				tok.nextToken();
				cod = tok.nextToken();
//				this.cbx_contatto.setSelectedValue(cod);
				contatto.setValue(cod);
			}
			doWriteComponentsToBean();
			if (stato == this.UPDATE_DELETE) {
				currentIsasRecord.put("vettore", vh);
				currentIsasRecord = (ISASRecord) invokeGenericSuEJB(myEJB, currentIsasRecord.getHashtable(), "updateVector");
//				db.UpdateVector(t, vh);
			} else {
				hParameters.put("vettore", vh);
				int cont = (Integer) invokeGenericSuEJB(myEJB, hParameters, "insertVector");
				Hashtable<String, String> h = new Hashtable<String, String>();
				h.put("int_anno", int_anno.getValue());
				h.put("int_contatore", cont+"");
				currentIsasRecord = queryKeySuEJB(myEJB, h);
//				int cont = db.InsertVector(t, vh);
				int_contatore.setValue(cont);
				setStato(this.UPDATE_DELETE);
			}
			doWriteBeanToComponents();
			ExecSelectCartella();
//			Date dtApeCart = dateCartIni.getValue();
//			Date dtChiusCart = dateCartFin.getValue();
//			doWriteBeanToComponents();
//			
//			dateCartIni.setValue(dtApeCart);
//			dateCartFin.setValue(dtChiusCart);
			this.insertDett = true;
// TODO
//			u.setDefault(this.JPanelPrestazioni);
//			u.Enable(jPanelInterv, false);
//			u.Enable(this.JPanelPrestazioni, true);
//			this.JButtonBorderedPrestaz.setDisabled(false);
//			this.printAction.setDisabled(false);
//			Hgriglia = new compareHashtable(forGriglia.getHashtableKeyValue());
//			JCariTextFieldPrestDec.setDisabled(false);
//			JCariTextFieldPrestDec.setEditable(true);
//			JCariTextFieldPrestaz_1.setDisabled(false);
//			JCariTextFieldPrestaz_1.setEditable(true);
//			JButtonBorderedPrestaz.setDisabled(false);
			numRiga = -1;
			deseleziona();
			this.JCariTextFieldTipoServizio.setText(tipoServizio());
			salvato = true;
			doFreezeForm();
			UtilForBinding.setComponentReadOnly(hopVLayout, true);
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info",btn_save,"after_center",2500);
			settaSaveMessaggioSo(CTS_SO_PRESENTE);
			}catch (Exception e ){
				doShowException(e);
			}
		}

		public int PreparaVettoreHash(Vector vh) {
			Hashtable h1 = null;
			String valore = "";
			String codice = "";
			for (int k = 0; k < caribellb2.getItemCount(); k++) {
				h1 = new Hashtable();
				Object itemGrid = caribellb2.getModel().getElementAt(k);
//				for (Iterator iterator = caribellb2.getItems().iterator(); iterator.hasNext();) {
////					Object object = (Object) iterator.next();
//		        	Object itemGrid = ((Listitem) iterator.next()).getAttribute("ht_from_grid");
		            if(itemGrid instanceof ISASRecord){
		    			h1 = ((ISASRecord) itemGrid).getHashtable();
		    		}else if(itemGrid instanceof Hashtable){
		    			h1 = (Hashtable<String, Object>) itemGrid;
		    		}
//				}
//				for (int j = 0; j < cariStringTableModel1.getColumnCount(); j++) {
//					codice = (String) cariStringTableModel1.getColumnDB(j);
//					valore = (String) cariStringTableModel1.getValueAt(k, j);
//					if (valore != null && (!valore.trim().equals(""))) {
//						h.put(codice, valore);
//					}
//				}//end for interno
				vh.addElement(h1);
			}//end for esterno
			return 1;
		}

//		//TODO
//		private void checkprestaz(boolean sel, int num_riga) {
//			String selezione = " ";
//			if (sel)
//				selezione = "X";
//			cariObjectTableModel modello = (cariObjectTableModel) this.JCariTableGrigliaPrestaz.getModel();
//			//  modello.setValueAt(selezione,num_riga,modello.getColumnLocationByName("pr_sel"));
//			int col = modello.getColumnLocationByName("pr_sel");
//			modello.setValueAt(new Boolean(sel), num_riga, col);
//		}
//
		private void deseleziona() {
//			cariObjectTableModel modello = (cariObjectTableModel) this.JCariTableGrigliaPrestaz.getModel();
//			for (int k = 0; k < modello.getSize(); k++) {
//				int col = modello.getColumnLocationByName("pr_sel");
//				boolean valore = ((Boolean) modello.getValueAt(k, col)).booleanValue();
//				if (valore) {
//					checkprestaz(false, k);
//					int col_quan = modello.getColumnLocationByName("pre_numero");
//					modello.setValueAt("1", k, col_quan);
//				}
//			}
			//FIXME VFR TEST
			tablePrestazioni.clearSelection();
		}

		public int passaPrestazioni() {
			ListModel<Object> modello = tablePrestazioni.getModel();
			CaribelListModel modelloGriglia = (CaribelListModel) caribellb2.getModel();
//			cariObjectTableModel modello = (cariObjectTableModel) this.JCariTableGrigliaPrestaz.getModel();
			if (modello.getSize() == 0)
				return -1;
			boolean trovato = false;
//			for (int k = 0; k < modello.getSize(); k++) {
//				int col = modello.getColumnLocationByName("pr_sel");
//				boolean valore = ((Boolean) modello.getValueAt(k, col)).booleanValue();
//				if (valore) {
//					trovato = true;
//					int colonnaCodice = modello.getColumnLocationByName("pre_cod_prest");
//					String cod_prest = (String) modello.getValueAt(k, colonnaCodice);
//					/*Controllo se e' gia' presente nella griglia un record con lo stesso codice prestazione
//					ed eventualmente devo aumentare la quantità
//					*/
//					Hashtable hNuovo = new Hashtable();
//					hNuovo.put("pre_cod_prest", cod_prest);
//					int num_riga = cariStringTableModel1.columnsContains(hNuovo);
//					//int quantita=1;
			for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
				trovato = true;
				Listitem litem = (Listitem) iterator.next();
				Object itemGrid = litem.getAttribute("ht_from_grid");
				if (itemGrid instanceof Hashtable) {
					String cod_prest = (String) ((Hashtable) itemGrid).get("pre_cod_prest");
					Hashtable hNuovo = new Hashtable();
					hNuovo.put("pre_cod_prest", cod_prest);
//					/*Controllo se e' gia' presente nella griglia un record con lo stesso codice prestazione
//					ed eventualmente devo aumentare la quantità
//					*/
					int num_riga = modelloGriglia.columnsContains(hNuovo);
					int quantita = Integer.parseInt((String) ((Hashtable) itemGrid).get("pre_numero"));
					String note = " ";
					if (num_riga != -1) { //l'ho inserito devo aumentare la quantità
						//aggiorno tutto
						Object o = modelloGriglia.getFromRow(num_riga, "pre_numero");
						int quant_vecchia;
						if(o instanceof Integer){
							quant_vecchia = ((Integer) o).intValue();
						}else{
							quant_vecchia = Integer.parseInt((String)  modelloGriglia.getFromRow(num_riga, "pre_numero"));
						}
						//                     int quant_nuova=Integer.parseInt((String)modello.getValueAt(k, Col_quantita_nuova));
						quantita = quant_vecchia + quantita;
//						int Col_note = cariStringTableModel1.getColumnLocationByName("pre_note");
						o = modelloGriglia.getFromRow(num_riga, "pre_note");
						note = (o != null ? (String) o : " ");
					}
					Hashtable<String, Object> hDati = (Hashtable<String, Object>) itemGrid;
					hDati.put("pre_numero", quantita+"");
					hDati.put("pre_note", note+"");
//					hDati.put("pre_cartella", n_cartella.getValue());
//					hDati.put("pre_contatto", contatto.getValue());
//					String riga = cod_prest
//							+ "##"
//							+ (!(((String) modello.getValueAt(k, modello.getColumnLocationByName("pre_des_prest"))).equals("")) ? (String) modello
//									.getValueAt(k, modello.getColumnLocationByName("pre_des_prest"))
//									: " ")
//							+ "##"
//							+ quantita
//							+ "##"
//							+ (!(((String) modello.getValueAt(k, modello.getColumnLocationByName("pre_tempo"))).equals("")) ? (String) modello
//									.getValueAt(k, modello.getColumnLocationByName("pre_tempo")) : " ")
//							+ "##"
//							+ note
//							+ "##"
//							+ (!(((String) modello.getValueAt(k, modello.getColumnLocationByName("pre_importo"))).equals("")) ? (String) modello
//									.getValueAt(k, modello.getColumnLocationByName("pre_importo"))
//									: " ")
//							+ "##"
//							+ (!(((String) modello.getValueAt(k, modello.getColumnLocationByName("prest_des_dett"))).equals("")) ? (String) modello
//									.getValueAt(k, modello.getColumnLocationByName("prest_des_dett"))
//									: " ");
					if (num_riga != -1) {
						modelloGriglia.remove(num_riga);
						modelloGriglia.add(num_riga, hDati);
					} else
						modelloGriglia.add(hDati);
				}
			}
			if (trovato)
				return 1;
			else
				return -2;
		}

		public void onDeseleziona(ForwardEvent e) throws Exception{
			deseleziona();
		}

		// 01/09/08 m. ctrl dtPrestaz >= dtApertura cartella e <= dtChiusura cartella
		private boolean checkDtPrestCartella() {
//			String data_prest = (String) int_data.getValueForIsas();
//			String dtApeCart = (String) dateCartIni.getValueForIsas();
//			String dtChiusCart = (String) dateCartFin.getValueForIsas();
			Date data_prest = int_data.getValue();
			Date dtApeCart = dateCartIni.getValue();
			Date dtChiusCart = dateCartFin.getValue();

//			int rit = controllaDataPrest(data_prest, dtApeCart);
//			if (rit == 2) {
			if(dtApeCart.after(data_prest)){
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaDataApertura"));
				int_data.focus();
				return false;
			}

//			rit = controllaDataPrest(dtChiusCart, data_prest);
//			if (rit == 2) {
			if(dtChiusCart!=null && data_prest.after(dtChiusCart)){
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaDataChiusura"));
				int_data.focus();
				return false;
			}

			return true;
		}

		private void onApriCartella() {
			String numCartella = this.n_cartella.getValue();
			String numContatto = this.cbx_contatto.getSelectedValue();
			String descContatto = this.cognomeAss.getValue(); //elisa b 08/04/11

			HashMap<String, String> param = new HashMap<String, String>();
			param.put("cartella", numCartella);
			param.put("contatto", numContatto);
			param.put("nome", descContatto);

			Component comp;
			String zulCartellaOp = "";
			if (gl_strTipoOperatore.equals(CostantiSinssntW.TIPO_OPERATORE_INFERMIERE))
				zulCartellaOp = "/web/ui/sinssnt/contatto_infermieristico/contatto_inf.zul";
			else if (gl_strTipoOperatore.equals((CostantiSinssntW.TIPO_OPERATORE_FISIOTERAPISTA)))
				zulCartellaOp ="/web/ui/sinssnt/contatto_fisioterapico/contatto_fisio.zul";
			else if (gl_strTipoOperatore.equals((CostantiSinssntW.TIPO_OPERATORE_MEDICO)))
				zulCartellaOp = "/web/ui/sinssnt/contatto_medico/contatto_medico.zul";
			else if (gl_strTipoOperatore.equals(CostantiSinssntW.TIPO_OPERATORE_MEDICO_CURE_PALLIATIVE))
				zulCartellaOp = "/web/ui/sinssnt/contatto_palliative/contatto_palliativemedico.zul";
			
			comp =  Executions.getCurrent().createComponents(zulCartellaOp, self, param);
		}

		private void caricaModalita() throws Exception {
			String modalita = "D";
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			h.put("n_cartella", n_cartella.getValue());
			h.put("n_contatto", contatto.getValue());
			modalita = (String) invokeGenericSuEJB(new SkInfEJB(), h, "getModalita");
			if (modalita!= null && modalita.equals("2"))
				tipoPrestazione.setSelectedValue("A");
			else
				tipoPrestazione.setSelectedValue("D");
			if (n_cartella != null && cbx_contatto.getSelectedValue()!=null)
				btn_apriCartella.setDisabled(false);
			else
				btn_apriCartella.setDisabled(true);

		}

		// 26/04/11
		private void setComboCont() throws Exception {
			if (cbx_contatto.getSelectedIndex() != -1)
				return;
			caricaDatiContatti();
		}

		//perdita del focus sulla data
		public void onAggiornaData(Event event) throws Exception {
			String punto = "onAggiornaData ";
			logger.trace(punto + " verifica data");
			if(int_data.getValue()!=null){
				String data = this.int_data.getText();
				String anno = data.substring(6, 10);
				if(isInUpdate() && !anno.equals(int_anno.getValue())){
					UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.coerenzaAnno", new String[]{int_anno.getValue()}));
				}
				if(vecchiadata != null){
					if(!vecchiadata.equals(int_data.getValueForIsas())){
						vecchiadata = int_data.getValueForIsas();
						if(!vecchiadata.isEmpty()){
							caricaDatiContatti();
						}else{
							sbiancaCombo();
						}
					}
				}else{
					caricaDatiContatti();
				}
				verificaDataInSchedaSo(false,0);
			}else{
				sbiancaCombo();
			}
			vecchiadata = int_data.getValueForIsas();
		}
		
		
		private boolean verificaDataInSchedaSo(boolean bloccareSalvataggio, int daDoveProvengo) {
			String punto = ver +"verificaDataInSchedaSo ";
			boolean datiSchedaSoOk= true;
			
			if (cbx_contatto != null && cbx_contatto.getItemCount() > 0) {
				if (ManagerDate.validaData(int_data)) {
					Hashtable<String, String> dati = new Hashtable<String, String>();
					dati.put(CostantiSinssntW.N_CARTELLA, this.n_cartella.getValue());
					dati.put(RMSkSOEJB.CTS_DATA_ACCESSO, int_data.getValueForIsas());
					RMSkSOEJB rmSkSo = new RMSkSOEJB();
					String messaggio = "";
					try {
						datiFormPrimaRichiestaSO = UtilForBinding.getHashtableFromComponent(myForm);
						logger.trace(punto + " data inserita, DADOVE PROVENGO>"+daDoveProvengo+
								"\n setto datiFormPrimaRichiestaSO>"+datiFormPrimaRichiestaSO+"\n");
						
						messaggio = rmSkSo.statoSchedaSo(CaribelSessionManager.getInstance().getMyLogin(), dati);
						if (ISASUtil.valida(messaggio)) {
							provenienza  = daDoveProvengo;
//							String messaggio = 
//									Labels.getLabel("so.conferma.chiusura.msg.presa.carico", new String[]{Labels.getLabel("RichiestaMMG.principale.intensita_ass")} );
							if (bloccareSalvataggio){
								messaggio +="\n"+ Labels.getLabel(CTS_LABEL_SO_PROCEDERE);
								Messagebox.show(messaggio,
										Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {

											public void onEvent(Event event) throws Exception{
												if (Messagebox.ON_YES.equals(event.getName())) {
													settaSaveMessaggioSo(CTS_SO_PRESENTE_SAVE);
													if (provenienza == CTS_SAVE_FORM){
														doSaveForm();
													}else {
														if (provenienza == CTS_BOTTONE){
															onConfermaSelezione(null);
														}
													}
												}
											}
										});
									return false;
							}else {
								Clients.showNotification(messaggio, "info", self, "after_center", CostantiSinssntW.INT_TIME_OUT);
							}
						} else {
							logger.trace(punto + " la data rientra nella scheda so");
						}
					} catch (Exception e) {
						logger.error(punto + " Errore nel recupera ", e);
					}
				} else {
					logger.trace(punto + " Non verifico la presenza di scheda so ");
				}
			} else {
				logger.debug(punto + " Non verifico lo stato della scheda SO: NON HA CONTATTO");
			}
			return datiSchedaSoOk;
		}

		protected void settaSaveMessaggioSo(int step) {
			check_save_SO_PRESENTE = step;
		}

		@Override
		protected void notEditable() {
			//Chiave gestita automaticamente quindi non editabili
			int_contatore.setReadonly(true);
			int_anno.setReadonly(true);
//			cod_operatore.setReadonly(true);
//			desc_operatore.setReadonly(true);
			qualOperatore.setReadonly(true);
			if(caribelContainerCtrl!=null && 
			   super.caribelContainerCtrl.hashChiaveValore.get("n_cartella") != null && 
			   super.caribelContainerCtrl.hashChiaveValore.get("n_contatto") != null){
				
				assistitoCtrl = (CaribelSearchCtrl) assistito.getAttribute(MY_CTRL_KEY);
				assistitoCtrl.setReadonly(true);
//		        cbx_contatto.setDisabled(true);
			}
		}

		@Override
		protected boolean doValidateForm() throws Exception {
			// TODO Auto-generated method stub
			return false;
		}
		
		protected void doFreezeForm() throws Exception{
			tH = new CompareHashtable(UtilForBinding.getHashtableFromComponent(self, null, false, true));
			Hgriglia  = new CompareHashtable(UtilForBinding.getHashtableFromComponent(caribellb2, null, false, true));
			Hop = new CompareHashtable(UtilForBinding.getHashtableFromComponent(hopVLayout, null, false, true));
			doFreezeListBox();
		}
		
		protected void deleteSuEJB(Object myEJB,ISASRecord myDbr)throws Exception {
			CaribelClass.isasInvoke(myEJB, "deleteAll", UtilForBinding.getHashtableFromComponent(self));
			myDbr = null;
		}
		
//		private Predicate filtroPrestazioni = new Predicate() {
//			public boolean evaluate(Object object) {
//				Hashtable prestazione;
//				if (!(object instanceof Hashtable)) {
//					if(!(object instanceof ISASRecord)){
//						return false;
//					}else{
//						prestazione = ((ISASRecord) object).getHashtable();
//					}
//				}else{
//					prestazione = (Hashtable) object;
//				}
//				String valoreFiltroPrestazioneCodice = codPrestazione.getValue().toUpperCase();
//				String valoreFiltroPrestazioneDescrizione = descPrestazione.getValue().toUpperCase();
//				int filtroPrestazione = valoreFiltroPrestazioneDescrizione.indexOf("%");
//				String codicePrestazione = (String) prestazione.get("pre_cod_prest");
//				String descrizionePrestazione = (String) prestazione.get("prest_des");
//				
//				Set sel = modelloPrestazioni.getSelection();
//				boolean selezionata = modelloPrestazioni.isSelected(object);
//				boolean isCodice = false;
//				boolean isDescrizione = false;
//				for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
//					selezionata = false;
//					Listitem litem = (Listitem) iterator.next();
//					Object itemGrid = litem.getAttribute("ht_from_grid");
//					if (itemGrid instanceof Hashtable) {
//						String cod_prest = (String) ((Hashtable) itemGrid).get("pre_cod_prest");
//						if(cod_prest.equals(codicePrestazione)){
//							selezionata = true;
//							break;
//						}
//					}
//				}
//				
//				isCodice = (codicePrestazione != null && codicePrestazione.contains(valoreFiltroPrestazioneCodice));
//				valoreFiltroPrestazioneDescrizione = valoreFiltroPrestazioneDescrizione.replaceFirst("%", "");
//				switch (filtroPrestazione) {
//				case -1:
//					isDescrizione = (descrizionePrestazione != null && descrizionePrestazione.equals(valoreFiltroPrestazioneDescrizione));
//					break;
//				case 0:
//					if(valoreFiltroPrestazioneDescrizione.endsWith("%")){
//						valoreFiltroPrestazioneDescrizione = valoreFiltroPrestazioneDescrizione.replaceFirst("%", "");
//						isDescrizione = (descrizionePrestazione != null && descrizionePrestazione.contains(valoreFiltroPrestazioneDescrizione));
//					}else{
//						isDescrizione = (descrizionePrestazione != null && descrizionePrestazione.endsWith(valoreFiltroPrestazioneDescrizione));
//					}
//					break;
//				default:
//					isDescrizione = (descrizionePrestazione != null && descrizionePrestazione.startsWith(valoreFiltroPrestazioneDescrizione));
//					break;
//				}
//				
//				boolean ret = isCodice && isDescrizione || selezionata;
//				return ret;
//			}
//		};

		@SuppressWarnings("unchecked")
		public Collection getPrestazioni() {
			return CollectionUtils.select(modelloPrestazioni != null ? modelloPrestazioni : CollectionUtils.EMPTY_COLLECTION, filtroPrestazioni);
		}
		
		private void controlloZonaOperatore() {
			boolean res = true;
			try{
				Hashtable h = new Hashtable();
				h.put("n_cartella",n_cartella.getValue());
				if (!n_cartella.getValue().equals("")){
					ISASRecord dbr = (ISASRecord)invokeGenericSuEJB(new RMSkSOEJB(), h, "selectZonaSkValCorrente");
					if (dbr!=null){
						final String distretto_cod = dbr.get("cod_distretto_verbale").toString();
						final String zona_cod = dbr.get("zona_cod").toString();
						final String id_skso = dbr.get("id_skso").toString();						
						String zona = dbr.get("gid").toString();
						String zona_desc = dbr.get("zona_desc").toString();
						if(!getProfile().getIsasUser().isInGroup(new Integer(zona))){				
					Messagebox.show(Labels.getLabel("accessi.eccezione.altra_zona",new String[]{zona_desc}), 
							Labels.getLabel("messagebox.attention"), Messagebox.OK,Messagebox.EXCLAMATION
							);
						res = false;
						}
					}
				}
			
				}catch (Exception ex2) {
					doShowException(ex2);
				}
			if (!res)
			 {
				this.n_cartella.setText("");
				this.cognomeAss.setText("");
			}
		}
			
		
		
}

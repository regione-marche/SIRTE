package it.caribel.app.sinssnt.controllers.agenda.agendaMultiOperatore;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.OperatoriEJB;
import it.caribel.app.common.ejb.PresidiEJB;
import it.caribel.app.common.ejb.PresidiNewEJB;
import it.caribel.app.sinssnt.bean.AgendaEditEJB;
import it.caribel.app.sinssnt.bean.AgendaModOperatoreEJB;
import it.caribel.app.sinssnt.bean.AgendaRegistraEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.controllers.agenda.AgendaGridItemRenderer;
import it.caribel.app.sinssnt.controllers.agenda.AgendaNuovoFormCtrl;
import it.caribel.app.sinssnt.controllers.agenda.AgendaRegistraNewFormCtrl;
import it.caribel.app.sinssnt.controllers.agenda.AgendaRegistraPopUpNewFormCtrl;
import it.caribel.app.sinssnt.controllers.agenda.CostantiAgenda;
import it.caribel.app.sinssnt.controllers.diario.DiarioGridCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.login.ManagerProfileBase;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForGridRenderer;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.util.ISASUtil;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.LayoutRegion;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AgendaMultiOperatoreRegistraFormCtrl extends AgendaRegistraNewFormCtrl{

	private static final long serialVersionUID = -9185932534805159010L;

	private static final long DOUBLE_CLICK_THRESHOLD = 600;


	public static String myPathZul = "/web/ui/sinssnt/agenda/agendaRegistraNewForm.zul";

	DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	protected AgendaEJB myEJB = new AgendaEJB();
	protected AgendaEditEJB myEJBEdit = new AgendaEditEJB();
	protected AgendaModOperatoreEJB modEJB = new AgendaModOperatoreEJB();
	
	protected String myKeyPermission = "AGENDA";
	protected String myKeyPermissionREG = "AG_REG";
	protected String myKeyPermissionSPO = "AG_SPO";
	protected String myKeyPermissionEdit = ChiaviISASSinssntWeb.AG_SPO;
	protected String myKeyPermissionSpostaOp = ChiaviISASSinssntWeb.AG_OPE;

//	private String ver = "2-"+this.getClass().getName()+". ";

	protected CaribelCombobox cbx_tipo_operatore;
	protected CaribelCombobox cbx_sede;
	protected CaribelCombobox cbx_operatore;
	protected CaribelCombobox cbx_operatore_esec;
	
	protected CaribelCombobox cbx_sedeCO;

	protected North filtriAgenda;
	protected LayoutRegion pannelloCambiaOperatore;
	protected Component filtriAgendaChiuso;
	protected Label filtriAgendaSintesi;

	protected Datebox fakeDate;

	protected java.util.Calendar calend = Calendar.getInstance();

	private Checkbox cb_mostraKmTempi;

	private Intbox numeroSettimane = new Intbox(1);

	protected static final String APRIDIARIO 	= "onApriDiario";
	protected static final String STAMPASCHEDA 	= "onStampaScheda";
	
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			iu = CaribelSessionManager.getInstance().getIsasUser();
			caribellbAgendaAssistito.setItemRenderer(new AgendaMultiOperatoreGridItemRenderer());

			populateCombobox();
			
			inizializzaCambiaOperatore();
			btn_print.setDisabled(!CaribelSessionManager.getInstance().getIsasUser().canIUse(ChiaviISASSinssntWeb.ST_AGVIS));
//			giornoSelezionato = new Date();
//			calendario.setValue(new Date());
			fakeDate.setValue(new Date());
		    
		    calend.setTime(fakeDate.getValue());
		    hParameters.put("figprof", cbx_tipo_operatore.getSelectedValue());
		    hParameters.put("presidio_op", cbx_sede.getSelectedValue());
		    hParameters.put("referente", cbx_operatore.getSelectedValue());
		    hParameters.put("esecutivo", cbx_operatore_esec.getSelectedValue());
		    hParameters.put("tipo_operatore", cbx_tipo_operatore.getSelectedValue());
		    hParameters.put("mese", mesi[calend.get(Calendar.MONTH)]);
		    hParameters.put("anno", calend.get(Calendar.YEAR));
			hParameters.put("giorni", getVettoreSettimana());
			hashDaPassare.put("giorni", getVettoreSettimana());
			hashDaPassare.putAll(hParameters);
			execSelect();
			
//			doAggiornaAgenda();
			
			filtriAgenda.addEventListener(Events.ON_OPEN,  new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
						filtriAgenda.setVisible(filtriAgenda.isOpen());
						return;
				}});
//			filtriAgenda.setOpen(true);
			filtriAgendaSintesi.setValue(Labels.getLabel("agenda.filtriAgendaSintesi", new String [] {cbx_tipo_operatore.getValue(), cbx_sede.getValue(), cbx_operatore.getValue(), cbx_operatore_esec.getValue()}));
			
			self.addEventListener(org.zkoss.zk.ui.event.Events.ON_AFTER_SIZE, new EventListener() {
				public void onEvent(Event event) throws Exception {
					if(event instanceof AfterSizeEvent){
						try{
							caribellbAgendaAssistito.invalidate();
						}catch(Exception e){
							doShowException(e);
						}
					}
				}
			});
//			initForm();
//			doFreezeForm();
		}catch(Exception e){
			UtilForBinding.setComponentReadOnly(self, true);
			doShowException(e);
		}
	}
	
	private void populateCombobox() throws Exception {
		//tipo operatore
		ManagerOperatore.loadTipiOperatori(cbx_tipo_operatore, null, false);
		String tipoOperatore = UtilForContainer.getTipoOperatorerContainer();
		if(tipoOperatore== null || tipoOperatore.isEmpty()){
			cbx_tipo_operatore.setSelectedValue(getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE));
		}else{
			cbx_tipo_operatore.setSelectedValue(tipoOperatore);
			UtilForBinding.setComponentReadOnly(cbx_tipo_operatore, true);
		}
		
		//sedi
		boolean conVoceTutti = false;
		Hashtable<String, Object> h = new Hashtable<String, Object>();	
		String strDISTR = ManagerProfileBase.getDistrettoOperatore(getProfile());							
		h.put("cod_distr",strDISTR);
        if (!conVoceTutti){
            h.put("senzaTutti", "S");
        }
        h.put("data_rif", UtilForUI.getDateForUI(fakeDate.getValue()));
		String tipi_ass = new PresidiNewEJB().getConfTipoAssPresidi(getProfile().getMyLogin());
		h.put("attivita_pres",tipi_ass);
		CaribelComboRepository.comboPreLoad("combo_presidio_"+strDISTR+ (conVoceTutti ? "" : "_NoTutti"), new PresidiNewEJB(), "query",h, cbx_sede, null, "codpres", "despres", false);
		cbx_sede.setSelectedValue(ManagerProfileBase.getPresidioOperatore(getProfile()));
		//imposto la sede per il cambi operatore
		CaribelComboRepository.comboPreLoad("combo_presidio_"+strDISTR+ (conVoceTutti ? "" : "_NoTutti"), new PresidiNewEJB(), "query",h, cbx_sedeCO, null, "codpres", "despres", false);
		cbx_sedeCO.setSelectedValue(ManagerProfileBase.getPresidioOperatore(getProfile()));
		
		
		h.put("presidio_op", cbx_sede.getSelectedValue());
		h.put("figprof", cbx_tipo_operatore.getSelectedValue());
		//operatori
		CaribelComboRepository.comboPreLoad("combo_operatori_"+strDISTR+"_"+cbx_sede.getSelectedValue()+"_"+cbx_tipo_operatore.getSelectedValue(), new OperatoriEJB(), "query",h, cbx_operatore,      null, "codice", "cognomeNome", true);
		h.put("siOperatoreFittizio", "false");
		CaribelComboRepository.comboPreLoad("combo_operatori_esec_"+strDISTR+"_"+cbx_sede.getSelectedValue()+"_"+cbx_tipo_operatore.getSelectedValue(), new OperatoriEJB(), "query",h, cbx_operatore_esec, null, "codice", "cognomeNome", true);
		h.remove("siOperatoreFittizio");
		cbx_operatore.setSelectedValue(ManagerProfile.getCodiceOperatore(getProfile()));
		cbx_operatore_esec.setSelectedValue(ManagerProfile.getCodiceOperatore(getProfile()));

		//se non ho la chiave isas APRIAGGR disabilito la possibilità d modificare gli operatori
		boolean cbxEditabili = iu.canIUse(ChiaviISASSinssntWeb.APRIAGGR,"CONS");
		UtilForBinding.setComponentReadOnly(cbx_tipo_operatore, !cbxEditabili);
		UtilForBinding.setComponentReadOnly(cbx_sede, !cbxEditabili);
		UtilForBinding.setComponentReadOnly(cbx_operatore, !cbxEditabili);
		UtilForBinding.setComponentReadOnly(cbx_operatore_esec, !cbxEditabili);
	}
	
	private Vector getVettoreSettimana() {
		Calendar c = Calendar.getInstance(); 
		c.setTime(fakeDate.getValue()); 
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		String dataIni=df.format(c.getTime()); //dt.getDataNGiorni(ultimogg, 1);
		Vector settimana=getSettimanaDa(dataIni,"gg-mm-aaaa");
		return settimana;
	}
	
	public void oldDoInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			super.setMethodNameForQuery("query_agenda");
			super.setMethodNameForInsert("insert_agenda");
			super.setMethodNameForUpdate("update_agenda");
			super.noModificheNelFuturo = true;
			hashDaPassare.putAll(arg);
//			hashDaPassare.put("esecutore", value)
			caribellbAgendaAssistito.setItemRenderer(new AgendaGridItemRenderer());
			
			selTutti.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					if (selTutti.isChecked())
						selezionaTuttiAssistiti(true);
					else 
						selezionaTuttiAssistiti(false);
				}});

			caribellbOperatori.setModel(new CaribelListModel());
			HProgToMove.clear();

            task = new ClickTask();//timer
            timer = new java.util.Timer();//timer
            
			execSelect();
			
			
			inizializzaCambiaOperatore();
			String referente = Labels.getLabel("agenda.operatore") + ": " + arg.get("referente") + " " + arg.get("referente_nome") + " ";
			String esecutore = ((String)arg.get("referente")).equals((String)arg.get("esecutivo")) ? "" : Labels.getLabel("agenda.operatoreEsecutore") + ": " + arg.get("esecutivo") + " " + arg.get("esecutivo_nome") + " ";
			if(this.spaceOwner instanceof Window){
				((Window)this.spaceOwner).setTitle(((Window)this.spaceOwner).getTitle() + " " + referente + " - " + esecutore) ;
			}
			doFreezeForm();
			iu = CaribelSessionManager.getInstance().getIsasUser();	
		}catch(Exception e){
			doShowException(e);
		}
	}

	protected void inizializzaCambiaOperatore() {
		c = (PanelUbicazioneCtrl)panel_ubicazione.getAttribute(MY_CTRL_KEY);
		c.doInitPanel();			
		c.settaRaggruppamento("P"); //NoUbic("P");
		c.setVisibleRaggruppamento(false);
		c.setVisibleUbicazione(false);
		c.setVisibleZona(false);
//		c.setVisibleDistretto(false);
		c.setDistrettoDisabilita(true);
		distretto.setSelectedValue(ManagerProfileBase.getDistrettoOperatore(getProfile()));
		Events.sendEvent(Events.ON_CHANGE, distretto, ManagerProfileBase.getDistrettoOperatore(getProfile()));
		distretto.addEventListener(Events.ON_CHANGE, new CambioOperatore());
		c.setVisiblePresidioComuneAreaDis(false);
//		presidio_comune_area.addEventListener(Events.ON_CHANGE, new CambioOperatore());
		cbx_sedeCO.addEventListener(Events.ON_CHANGE, new CambioOperatore());
		pannelloCambiaOperatore.addEventListener(Events.ON_OPEN, new CambioOperatore());
	}
	
	class CambioOperatore implements EventListener<Event>{
		
		@Override
		public void onEvent(Event event) throws Exception{
			onCaricaOperatori(event);
		}
	}

	protected void mostraAssistito(int i) {
		
	        int x = i!=-1? i: caribellbAgendaAssistito.getSelectedIndex();
//	        int y=  JCariTableAgenda.getColumnLocationByName("assistito");
	        if(x>-1){
	        	String ass= ((CaribelListModel) caribellbAgendaAssistito.getModel()).getFromRow(x, "assistito") + " - " + 
	        			((CaribelListModel) caribellbAgendaAssistito.getModel()).getFromRow(x, "indirizzo");
	        	//	        if(x%2==0)ass=(String)JCariTableAgenda.getValueAt(x,y) +" - "+(String)JCariTableAgenda.getValueAt(x+1,y);
	        	//	        else ass=(String)JCariTableAgenda.getValueAt(x-1,y) +" - "+(String)JCariTableAgenda.getValueAt(x,y);
	        	riepilogoAltroAssistito.setValue(ass);
	        }
	}

	protected void executeSelect() throws ISASMisuseException {
		if(caribellbAgendaAssistito.getSelectedIndex()>-1){
			Object o = caribellbAgendaAssistito.getModel().getElementAt(caribellbAgendaAssistito.getSelectedIndex());
			String val = "";
			if(o instanceof ISASRecord){
				val = (String) ((ISASRecord) o).get("valore");
			}else if(o instanceof Hashtable){
				val = (String) ((Hashtable) o).get("valore");
			}
			riepilogoAltroAssistito.setValue(val);
		}
	}
	
	public void onDoubleClickedCell(ForwardEvent event) throws Exception {
//        task.cancel();
		if(!checkOperatoreEsecutore()){
			return;
		}		
		setCellaSelez(-1,-1);
		
		Component comp = event.getOrigin().getTarget();
		Object o = comp.getAttribute("dbName", true);
		modelloAgenda = (CaribelListModel) caribellbAgendaAssistito.getModel();
		x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
		
		setAssistitoRitorno(((Hashtable) ((Listitem) comp.getParent()).getAttribute(UtilForGridRenderer.HT_FROM_GRID)).get("ag_cartella"));

		stat=0;
		if(((String)o).length()!=1) return; //	if(y<2)return;
		y = Integer.parseInt((String)o);
		logger.trace("Apro il dettaglio di:"+ x +" - "+y);
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		String statoSullaSelezione = ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(x),"stato"+y);

		if(statoSullaSelezione.equals("1")){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoIncompleto"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"L'accesso, con stato incompleto, è chiuso! \n Puoi solo visionare l'elenco delle prestazioni!","Attenzione scelta non valida!").show();
			stat=1;
		}else if(statoSullaSelezione.equals("2")){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoChiuso"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"L'accesso, con stato completo, è chiuso! \n Puoi solo visionare l'elenco delle prestazioni!","Attenzione scelta non valida!").show();
			stat=2;
		}
		String valore = ((String) modelloAgenda.getFromRow(x,(String) o));
		logger.trace("in mouseclick valor="+valore);
//		Hashtable<String, Object> h = new Hashtable<String, Object>();
		if (valore == null || valore.trim().equals("")||statoSullaSelezione.equals("")){
			UtilForUI.standardYesOrNo(Labels.getLabel("agendaSposta.msg.nuovoIntervento", new String[]{key_data[x][y]}), new EventListener<Event>(){
				public void onEvent(Event event) throws Exception{
					if (Messagebox.ON_YES.equals(event.getName())){	
						apriPopup(modelloAgenda, x, stat, y,"INSERT", "00");
					}
				}
			} );
			return;
		}
		apriPopup(modelloAgenda, x, stat, y,"UPDATE", ISASUtil.getValoreStringa((Hashtable<String,Object>)modelloAgenda.get(x),"progr"+y));

	}
	
	public void onCheckCell(ForwardEvent event) throws Exception {
		Component comp = event.getOrigin().getTarget();
		Set tmp = new HashSet(caribellbAgendaAssistito.getSelectedItems());
//		int count = tmp.size();
		if(((Checkbox) comp).isChecked()){
			tmp.add((Listitem) comp.getParent().getParent());
		}else{
			tmp.remove((Listitem) comp.getParent().getParent());
			selTutti.setChecked(false);
		}
//		count = tmp.size();
		caribellbAgendaAssistito.clearSelection();
		caribellbAgendaAssistito.setSelectedItems(tmp);
		onSelect$caribellbAgenda(null);
	}
	
	public void onClickedCell(ForwardEvent event) throws Exception {
//		componentClicked = event.getOrigin().getTarget();
		componentClicked = event.getOrigin().getTarget();
		doSingleclick();
//		timer = new Timer();
//		timer.schedule(new ClickTask(), DOUBLE_CLICK_THRESHOLD);
	}
	public void doSingleclick() throws Exception {
		int x = caribellbAgendaAssistito.getIndexOfItem((Listitem) componentClicked.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
		mostraAssistito(x);
		Object o = componentClicked.getAttribute("dbName", true);
		//considero il click solo se è stata cliccata una cella della settimana (cioè con dbname associato e compreso tra (0-6)
		if(o== null || ((String)o).length()!=1) return; //	if(y<2)return;
		y = Integer.parseInt((String)o);
		x = caribellbAgendaAssistito.getIndexOfItem((Listitem) componentClicked.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
		modelloAgenda = (CaribelListModel) caribellbAgendaAssistito.getModel();
		String statoSullaSelezione = ISASUtil.getValoreStringa((Hashtable<String,Object>)modelloAgenda.get(x),"stato"+y);
		setCellaSelez(x,y+4);
		if(ySel!=-1){
			//JCariTableAgenda.clearSelection();
			setCellaSelez(-1,-1);
			return;
		}

		if(x==xOld && y==yOld){//se riclicco su cella selezionata la deseleziono
			//JCariTableAgenda.clearSelection();
			setCellaSelez(-1,-1);
			xOld=-1;yOld=-1;
			xDrag=-1;yDrag=-1;
			checkCancellabile();
			return;
		}else{
			xOld=x;
			yOld=y;
		}

		//se non ho il diritto di spostare gli appuntamenti non effettuo la selezione del singolo giorno
		if(!iu.canIUse(ChiaviISASSinssntWeb.AG_SPO,"CONS")){
			return;
		}
		
		if(xDrag==-1 && yDrag==-1){
			if(!statoSullaSelezione.equals("0")&& !statoSullaSelezione.equals("9"))/*[PUA]*/{
				if(statoSullaSelezione.equals("")){
//					UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.nonEsisteAppuntamento"));
					setCellaSelez(-1,-1);
					return;
				}
				UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.statoInterventoNonSpostabile"));
				setCellaSelez(-1,-1);
				checkCancellabile();
				return;
			}
			xDrag=x;
			yDrag=y;
			hDa=new Hashtable();
			if(!spostoDa(xDrag,yDrag,hDa)){
				UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.sceltaNonValida"));
				xDrag=-1;yDrag=-1;
			}
			//seleziono cella da spostare
		}else{
			setCellaSelez(-1,-1);
			xOld=-1;yOld=-1;
			hA=new Hashtable();
			if(!spostoA(x,y,hA)){
				UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.sceltaNonValida"));
				return;
			}
			xDrag=-1;
			yDrag=-1;
			String message=null;
			String strCtrlDa = (String)hDa.get("ag_cartella")+"/"+
					(String)hDa.get("ag_contatto")+"/"+
					(String)hDa.get("cod_obbiettivo")+"/"+
					(String)hDa.get("n_intervento");
			String strCtrlA  = (String)hA.get("ag_cartella")+"/"+
					(String)hA.get("ag_contatto")+"/"+
					(String)hA.get("cod_obbiettivo")+"/"+
					(String)hA.get("n_intervento");
			Map params = new HashMap();
			params.put("width", 500);
			if(strCtrlDa.equals(strCtrlA)){
				message=Labels.getLabel("agendaSposta.msg.copiareOSpostareAppuntamento", new String[]{Labels.getLabel("agendaSposta.msg.spostare"),
						costruisciMsg(hDa), costruisciMsg(hA)});
				//UtilForUI.standardExclamation();
				Messagebox.show(message, Labels.getLabel("messagebox.attention"),  new Messagebox.Button[] {Messagebox.Button.CANCEL, Messagebox.Button.YES, Messagebox.Button.NO}, new String[]{"Cancel","Copia", "Sposta"}, Messagebox.QUESTION, Messagebox.Button.CANCEL, new EventListener<Messagebox.ClickEvent>() {
					public void onEvent(Messagebox.ClickEvent event) throws Exception {
						if(event.getButton()!=null){
							execUpdate(event.getButton().ordinal(),"CELL",hDa,hA);
						}
					}},params);
			}else{
				message=Labels.getLabel("agendaSposta.msg.copiareOSpostareAppuntamento", new String[]{"", costruisciMsg(hDa), costruisciMsg(hA)});
				Messagebox.show(message, Labels.getLabel("messagebox.attention"),  new Messagebox.Button[] {Messagebox.Button.CANCEL, Messagebox.Button.YES}, new String[]{"Cancel","Copia"}, Messagebox.QUESTION, Messagebox.Button.CANCEL, new EventListener<Messagebox.ClickEvent>() {
					public void onEvent(Messagebox.ClickEvent event) throws Exception {
						execUpdate(event.getButton().ordinal(),"CELL",hDa,hA);
					}},params);  
			}
			xDrag=-1;yDrag=-1;
		}
		checkCancellabile();
	}
	
	
	
	private void checkCancellabile() {
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
//		String statoSullaRiga = ;
		setBtnDeleteDisabled(xDrag==-1&&yDrag==-1 || !ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(xDrag),"stato"+yDrag).equals("0"));
//		setBtnDeleteDisabled(xDrag==-1&&yDrag==-1 || !key_stato[xDrag][yDrag].equals("0"));
	}

	public void execUpdate(int selection,String operaz,Hashtable hDa,Hashtable hA) throws Exception{
		logger.debug("execUpdate hash="+hashDaPassare.toString());

		if (selection==1) return;
		Hashtable hAgg=new Hashtable();
		hAgg.put("operaz",operaz);
		hAgg.put("hDa",hDa);
		hAgg.put("hA",hA);
		hAgg.put("referente",hashDaPassare.get("referente"));
		//System.out.println("INVIO...."+hAgg.toString());
		Object o=null;
		if (selection==2){//COPIO
			o=invokeGenericSuEJB(myEJBEdit, hAgg, "copio_appunt");//db.copioAppunt(hAgg);
		}else if (selection==3){//sposto
			o=invokeGenericSuEJB(myEJBEdit, hAgg, "sposto_appunt");//db.spostoAppunt(hAgg);
		}
		if (o!=null){
			if(((String)o).equals("")){
				 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.noPianificazione"));
//				new it.pisa.caribel.swing2.cariInfoDialog(null,"Non esistono prestazioni pianificate per le quali caricare l'agenda","Attenzione!").show();
				return;
			}
		}
		aggiornaTabella();
	}

	private String costruisciMsg(Hashtable h) throws ParseException{
//        String msg= "Num Cartella/Contatto/Obiettivo/Intervento="+(String)h.get("ag_cartella")+"/"+(String)h.get("ag_contatto")+"/"+(String)h.get("cod_obbiettivo")+"/"+(String)h.get("n_intervento")+
//        " del:"+formattaData3((String)h.get("ag_data"),1)+" "+((((String)h.get("ag_orario")).trim()).equals("0")?"Mattina":"Pomeriggio");
        String msg = Labels.getLabel("agendaSposta.msg.costruisciMsg", new String[]{(String)h.get("ag_cartella"),
        																			Labels.getLabel("agenda.fasce."+h.get("ag_orario")),
        																			df.format(UtilForBinding.getDateFromIsas((String) h.get("ag_data")))});
        return msg;
    }
	
	public boolean spostoDa(int x,int y,Hashtable hDa){
		boolean moveAccordato=false;
		String valore = ((String) modelloAgenda.getFromRow(x,y+""));
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		String statoSullaSelezione = ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(x),"stato"+y);
		if (valore.equals("")||statoSullaSelezione.equals("")){
			moveAccordato=false;
		}else{
			moveAccordato=true;
		}
		String st=statoSullaSelezione;
		if(st.equals(""))st="0";
		hDa.put("ag_stato",st);
		hDa.put("ag_data",key_data[x][y]);
		hDa.put("referente",(String)hashDaPassare.get("referente"));
		hDa.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		hDa.put("esecutivo",(String)hashDaPassare.get("esecutivo"));
		hDa.put("ag_contatto",modelloAgenda.getFromRow(x,"ag_contatto").toString());
		//gb 14/09/07 *******
		hDa.put("cod_obbiettivo",(String)modelloAgenda.getFromRow(x,"cod_obbiettivo"));
		hDa.put("n_intervento",modelloAgenda.getFromRow(x,"n_intervento").toString());
		//gb 14/09/07: fine *******
		hDa.put("ag_cartella", modelloAgenda.getFromRow(x,"ag_cartella").toString());
//		String orario=(String)modelloAgenda.getFromRow(x,"matt_pom");
//		Object o = modelloAgenda.getFromRow(x,"ag_orario");
//		hDa.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));
		hDa.put("ag_orario", ""+x%CostantiAgenda.NUMFASCIEORARIE);
		hDa.put("ag_progr", modelloAgenda.getFromRow(x,"ag_progr")+"");
		logger.trace("Caricata hashtable hDa..."+hDa.toString());
		return moveAccordato;
	}

	public boolean spostoA(int x,int y,Hashtable hA){
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		String statoSullaSelezione = ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(x),"stato"+y);
		String st=statoSullaSelezione;
		if(st.equals(""))st="0";
		hA.put("ag_stato",st);
		hA.put("ag_data",key_data[x][y]);
		hA.put("referente",(String)hashDaPassare.get("referente"));
		hA.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		hA.put("esecutivo",(String)hashDaPassare.get("esecutivo"));
		hA.put("ag_contatto",modelloAgenda.getFromRow(x,"ag_contatto").toString());
		//gb 14/09/07 *******
		hA.put("cod_obbiettivo",(String)modelloAgenda.getFromRow(x,"cod_obbiettivo"));
		hA.put("n_intervento",modelloAgenda.getFromRow(x,"n_intervento").toString());
		//gb 14/09/07: fine *******
		hA.put("ag_cartella",modelloAgenda.getFromRow(x,"ag_cartella").toString());
//		String orario=(String)modelloAgenda.getFromRow(x,"matt_pom");
//		hA.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));
//		Object o = modelloAgenda.getFromRow(x,"ag_orario");
		hA.put("ag_orario", ""+x%CostantiAgenda.NUMFASCIEORARIE);
		logger.trace("Caricata hashtable hA..."+hA.toString());
		return true;
	}

	private void setCellaSelez(int x, int y) {
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		int numRighe=clm.getSize();
		Listitem item;
		String newStyle;
		for(int i=0;i<numRighe;i++){
			item = caribellbAgendaAssistito.getItemAtIndex(i);
			int k=0;
			for (Iterator<Component> iterator = item.getChildren().iterator(); iterator.hasNext();) {
				HtmlBasedComponent e = (HtmlBasedComponent) iterator.next();
				boolean primeDueFasce = (i%CostantiAgenda.NUMFASCIEORARIE==0 );//|| i%CostantiAgenda.NUMFASCIEORARIE==1);
//			if(item.getChildren().size()>=y){
//				HtmlBasedComponent e = (HtmlBasedComponent) item.getChildren().get(y+2);
				if(e.getSclass()!=null){
					String parkStyle = e.getSclass();
					parkStyle = parkStyle.replaceAll(classForRequired, "");
					parkStyle = parkStyle.replaceAll(classForMove, "");
					parkStyle = parkStyle.replaceAll("  ", " ");
					if(i==x && ((k==y && primeDueFasce)|| (k==y-2 && !primeDueFasce))){//i==X la riga è quella giusta 
//						if(parkStyle.trim().length()>0){
							newStyle = classForMove + " " + parkStyle;
//						} else{
//							newStyle = classForMove;
//						}
					} else {
						newStyle = parkStyle;
					}
				}else{
					newStyle = classForRequired;
				}
				e.setSclass(newStyle);
				k++;
			}
		}
//		checkCancellabile();
	}

	private void apriPopup(CaribelListModel model, int x, int stat, int y, String stato, String progr) {
		Hashtable<String, Object> h = new Hashtable<String, Object>();
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		String statoSullaSelezione = ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(x),"stato"+y);

        String st=statoSullaSelezione;
        if(st.equals(""))st="0";
        h.putAll((Hashtable) model.getElementAt(x));
        h.put("ag_stato",st);
		h.put("stato", stato);
		h.put("ag_progr", progr);
		h.put("ag_data",key_data[x][y]);
		h.put("referente",(String) model.getFromRow(x,"ag_oper_ref"));
		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		h.put("esecutivo",(String)hashDaPassare.get("esecutivo"));
		h.put("ag_contatto",((Integer)model.getFromRow(x,"ag_contatto")).toString());
		h.put("cod_obbiettivo",(String)model.getFromRow(x,"cod_obbiettivo"));
		h.put("n_intervento",((Integer)model.getFromRow(x,"n_intervento")).toString());
		h.put("ag_cartella",((Integer)model.getFromRow(x,"ag_cartella")).toString());
		h.put("assistito",(String)model.getFromRow(x,"assistito"));
		h.put("ag_orario",x%CostantiAgenda.NUMFASCIEORARIE);//(String)model.getFromRow(x,"matt_pom"));
		h.put("stato_interv", stat);
		
		logger.trace("Call PopUp..."+h.toString());

	    Executions.getCurrent().createComponents(AgendaRegistraPopUpNewFormCtrl.myPathZul, self, h);
	    if(stat==1){
	    	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoIncompleto"));
	    }else if(stat==2){
	    	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoChiuso"));
	    }
	}
	
	boolean isPresente(String nuova_prest,String stringa){
		StringTokenizer st_new=new StringTokenizer(nuova_prest,"-");
		while(st_new.hasMoreTokens()){
			String tok_new=(st_new.nextToken()).trim();
			StringTokenizer st=new StringTokenizer(stringa,"-");
			while(st.hasMoreTokens()){
				String tok=(st.nextToken()).trim();
				if(tok_new.equals(tok))
					return true;
			}
		}
		return false;
	}
	
	public int preparaVettoreHash(Vector<Vector<String>> vh){
		vh.add(0,vMat);
		vh.add(1,vPom);
		return 1;
	}
	public int preparaVettoreHashRemove(Vector<Vector<String>> vh){
		vh.add(0,vMat_rm);
		vh.add(1,vPom_rm);
		return 1;
	}
	
	@Override
	protected void settaHeaderTabella(){
		//Modifico le label degli header delle colonne
		caribellbAgendaAssistito.getHeads();
		Listhead testa = caribellbAgendaAssistito.getListhead();
		String dbName;
		Vector v=(Vector) hashDaPassare.get("giorni");
		if(v==null){
			//FIXME per nuova agenda
			return;
		}
		String data;
		boolean disable;
		String labelHeader = "";
		CaribelListheader header = null;
		String numPrestazioni = "0";
		String numPrelievi = "0";
		for (Object figlio : testa.getChildren()) {
			header = ((CaribelListheader)figlio);
			dbName = header.getDb_name();
			numPrestazioni = "0";
			numPrelievi = "0";
			try {
				int i = Integer.parseInt(dbName);
				header.setSortAscending(new AgendaComparator(true, i));
				header.setSortDescending(new AgendaComparator(false, i));

				data = (String) v.get(i);
				if(key_contatori != null){
					numPrestazioni 	= key_contatori[0][i];
					numPrelievi 	= key_contatori[1][i];
					if(numPrestazioni==null) numPrestazioni = "0";
					if(numPrelievi==null) numPrelievi = "0";
				}
				labelHeader = Labels.getLabel("agendaPianSett.sett.header", new String[]{Labels.getLabel("agendaPianSett.sett."+i), data.substring(0, 2), numPrestazioni+"", numPrelievi+""});
				header.setLabel(labelHeader);
				header.setTooltiptext(Labels.getLabel("agendaPianSett.sett.tooltiptext"));
				// disabilitazione header nel futuro
				if(UtilForBinding.getDateFromIsas(data.substring(6,10)+"-"+data.substring(3,5)+"-"+data.substring(0,2)).after(new Date())){
					disable=true;
				}else{
					disable=false;
				}
				InputElement ie = ((InputElement) caribellbAgendaAssistito.getFellowIfAny("kmIntBox"+i,  true));
				if(ie!=null){
					ie.setReadonly(disable);
				}
				ie = ((InputElement) caribellbAgendaAssistito.getFellowIfAny("mmIntBox"+i,  true));
				if(ie!=null){
					ie.setReadonly(disable);
				}
				Checkbox check = (Checkbox) header.getFellowIfAny("jCheckBox"+(i+1));
				if(check!= null){
					check.setChecked(false);
				}
			} catch (NumberFormatException e) {
			} catch (ParseException e) {
				doShowException(e);
			}
		}
		selTutti.setChecked(false);
		//finemodifica headers
	}

//	@Override
//	protected void ricercaKmMm() throws Exception {
//		boolean referenteImpostato = !ISASUtil.getValoreStringa(hashDaPassare, "referente").isEmpty();
//		headers1.setVisible(referenteImpostato && cb_mostraKmTempi.isChecked());
//		headers2.setVisible(referenteImpostato && cb_mostraKmTempi.isChecked());
//		if(referenteImpostato){
//			super.ricercaKmMm();
//		}
//	}

	
//	public void onColoraColonna(Checkbox box, int col){
//		String punto = ver + "onColoraColonna ";
//		
//		if (isColonnaVuota(((CaribelListheader)box.getParent()).getDb_name())){
//			// new it.pisa.caribel.swing2.cariInfoDialog(null,"La giornata selezionata non contiene prestazioni!","Attenzione!").show();
//			box.setChecked(false); //setSelected(false);
//			return;
//		}
//		Vector v=(Vector)hashDaPassare.get("giorni");
//		//logger.info("_coloraColonna vettore giorni che arriva:"+v);
//		int  ind=col-3;
//		String index=""+ind;
//		String w_data=(String)v.elementAt(ind);
//		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
////		if(noModificheNelFuturo  && ControlloData(w_data,dataOdierna)<0){
////			if(firsttime){
////				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.noRegistrazioneFutura"));
//////				new it.pisa.caribel.swing2.cariInfoDialog(null,"Non si possono registrare prestazioni con data successiva a oggi.","Attenzione!").show();
////				firsttime=false;
////			} 
////			box.setChecked(false);
////			return;
////		}else{
////			firsttime=true;
////		}
//
//		if(box.isChecked()){
//			int numRighe=clm.getSize();
//			boolean proseguo=false;
//			for(int i=0;i<numRighe;i++){
//				String key_statoVal = key_stato[i][col-3];
//				if(key_statoVal.equals("0")||key_statoVal.equals("3")||key_statoVal.equals("9")){
//					proseguo=true;
//					String data=(String)v.elementAt(ind);
//					String cartella=clm.getFromRow(i, "ag_cartella").toString()+"-"+
//					clm.getFromRow(i, "ag_contatto").toString()+"-"+
//					(String)clm.getFromRow(i, "cod_obbiettivo")+"-"+
//					clm.getFromRow(i, "n_intervento").toString()+"-"+
//					(String)clm.getFromRow(i, "matt_pom");
//					Hashtable hC=new Hashtable();
//					Vector vP=new Vector();
//					logger.trace("CARICO HProgToMove"+HProgToMove.toString());
//					if(HProgToMove.containsKey(data)){
//						hC=(Hashtable)HProgToMove.get(data);
//					}
//					if(hC.containsKey(cartella)){
//						vP=(Vector)hC.get((cartella));
//					}
//					vP.add(key_progr[i][col-3]);
//					hC.put(cartella,vP);
//					logger.trace("dopo  hC"+hC.toString());
//					logger.trace("dopo  vP"+vP.toString());
//					HProgToMove.put(data,hC);
//					logger.trace(punto +" HO CARICATO HProgToMove"+HProgToMove.toString());
//				}
//			}
//
//			//?? da togliere se si vuole spostare anche se ci sono prestazioni registrate
//			if(proseguo==false){
//				// new it.pisa.caribel.swing2.cariInfoDialog(null,"Nella giornata selezionata non esistono prestazioni da registrare","Attenzione!").show();
//				box.setChecked(false);
//				return;
//			}
//			logger.trace("inserisco in reg_interv: index=["+index+"]["+(String)v.elementAt(ind)+"]");
//			reg_interv.put(index,(String)v.elementAt(ind));
//		}
//		else{
//			String data=(String)reg_interv.get(index);
//			logger.trace("rimuovo "+index+" data="+data);
//			try{//potrebbe non essere presente in quanto lo stato non ha permesso il suo caricamento
//				reg_interv.remove(index);
//			}catch(Exception e){
//			}
//			try{//potrebbe non essere presente in quanto lo stato non ha permesso il suo caricamento
//				this.HProgToMove.remove(data);
//			}catch(Exception e2){
//			}
//		}
//		cambiaStile(box.isChecked(), col);
//		caribellbAgendaAssistito.invalidate();
//	}
	
	private void cambiaStile(boolean checked, int col) {
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		int numRighe=clm.getSize();
		Listitem item;
		String newStyle;
		for(int i=0;i<numRighe;i++){
			item = caribellbAgendaAssistito.getItemAtIndex(i);
			if(item.getChildren().size()>=col){
				HtmlBasedComponent e;
				if(i%CostantiAgenda.NUMFASCIEORARIE==0 || i% CostantiAgenda.NUMFASCIEORARIE==1){
					e = (HtmlBasedComponent) item.getChildren().get(col);
				}else{
					e = (HtmlBasedComponent) item.getChildren().get(col-1);
				}
				if(e.getSclass()!=null){
					String parkStyle = e.getSclass();
					parkStyle = parkStyle.replaceAll(classForRequired, "");
					parkStyle = parkStyle.replaceAll(classForAssistito, "");
					parkStyle = parkStyle.replaceAll("  ", " ");
					if(checked){
						if(parkStyle.trim().length()>0){
							newStyle = parkStyle+" "+classForRequired;
						} else{
							newStyle = classForRequired;
						}
						if(item.isSelected()){
							newStyle = newStyle + " " + classForAssistito;
						}
					} else {
						newStyle = parkStyle;
					}
				}else{
					newStyle = classForRequired;
				}
				e.setSclass(newStyle);
			}
		}
	}

	public void onSelect$caribellbAgenda(Event event){
		if(jCheckBox1.isChecked())
			onColoraColonna(jCheckBox1, 4);
		if(jCheckBox2.isChecked())
			onColoraColonna(jCheckBox2, 5);
		if(jCheckBox3.isChecked())
			onColoraColonna(jCheckBox3, 6);
		if(jCheckBox4.isChecked())
			onColoraColonna(jCheckBox4, 7);
		if(jCheckBox5.isChecked())
			onColoraColonna(jCheckBox5, 8);
		if(jCheckBox6.isChecked())
			onColoraColonna(jCheckBox6, 9);
		if(jCheckBox7.isChecked())
			onColoraColonna(jCheckBox7, 10);
	}

	boolean registra() throws Exception{
		Hashtable h=new Hashtable();
		//h.put("giorni",reg_interv);
		h.put("appuntamenti",HProgToMove);
		h.put("referente",(String)hashDaPassare.get("referente"));
		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		h.put("esecutivo",(String)hashDaPassare.get("esecutivo"));

		logger.trace("REGISTRO... "+h.toString());

		if(HProgToMove.isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.appuntamentNonValidi"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"Appuntamenti non validi","Attenzione").show();
			reg_interv.clear();
			return false;
		}else{
			for (Iterator iterator = HProgToMove.keySet().iterator(); iterator.hasNext();) {
				Object dataPre = (Object) iterator.next();
				if(ControlloData((String)dataPre,dataOdierna)<0){
					UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.noRegistrazioneFutura"));
					return false;
				}
			}
		}
		Hashtable<String, Object> tempiKm = new Hashtable<String, Object>();
		tempiKm.putAll(UtilForBinding.getHashtableFromComponent(headers1));
		tempiKm.putAll(UtilForBinding.getHashtableFromComponent(headers2));
		h.put("tempiKm", tempiKm);
		h.put("giorni", hashDaPassare.get("giorni"));
		if(HProgToMove.get("ag_cartella")!=null){
			setAssistitoRitorno(HProgToMove.get("ag_cartella"));
		}
		doWriteComponentsToBean();
		Object o = invokeGenericSuEJB(new AgendaRegistraEJB(), h, "registra_prestaz"); //db.registraPrest(h);
		h.clear();
		if (o==null){
			UtilForUI.standardExclamation("Operazione non riuscita");
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"Operazione non riuscita","Attenzione!").show();
			return false;
		}
//		setDefault();
		HProgToMove.clear();
		xDrag=-1;
		yDrag=-1;
		ySel=-1;
		reg_interv.clear();
		jCheckBox1.setChecked(false);
		jCheckBox2.setChecked(false);
		jCheckBox3.setChecked(false);
		jCheckBox4.setChecked(false);
		jCheckBox5.setChecked(false);
		jCheckBox6.setChecked(false);
		jCheckBox7.setChecked(false);
		selTutti.setChecked(false);
		
		aggiornaTabella();
		return true;
	}
	
	public String MetodoGenerico_doubleClickAction(Vector vett) throws Exception{
		Vector settimana =(Vector)vett.get(0);
		logger.debug("settimanaSelezionata arrivata al padre=="+settimana.toString());
		boolean isCaricato=((Boolean)vett.get(3)).booleanValue();
		if(isCaricato==false){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.settimanaNonCaricata"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"La settimana selezionata non è ancora stata caricata in agenda!","Attenzione!").show();
			return "true";
		}else{//posso aggiornare
			//  logger.info("MetodoGenerico_doubleClickAction carico in hash la settimana-->"+settimana);
			hashDaPassare.put("giorni",settimana);
			hashDaPassare.put("mese",(String)vett.get(1));
			hashDaPassare.put("anno",(String)vett.get(2));
			this.aggiornaTabella();
		}
		return "true";
	}

	public void onBlurMMKM(Event blur) throws Exception{
		logger.trace(blur);
		Event event = blur;
		if(event instanceof ForwardEvent){
			event = ((ForwardEvent) event).getOrigin();
		}
		String idColonna = ((CaribelIntbox) event.getTarget()).getDb_name().substring(2);
		if(!reg_interv.containsKey(idColonna)){
					
		}
		return;
	}
	
	protected boolean cambioOperatore() throws Exception {
		Hashtable h=new Hashtable();
		h.put("appuntamenti",HProgToMove);
		h.put("referente",(String)hashDaPassare.get("referente"));
		h.put("giorni",hashDaPassare.get("giorni"));
		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		h.put("referente_new",(String)hashDaPassare.get("referente_new"));
		//System.out.println("call cambiaOperatore"+h.toString());
		if(HProgToMove.isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agendaModOperatore.msg.appuntamentiNonValidi"));
			reg_interv.clear();
			return false;
		}
		logger.trace("Eseguo spostamento "+h.toString());
		Object o;
		try {
			o = invokeGenericSuEJB(modEJB, h, "cambia_operatore");
		} catch (Exception e) {
			UtilForUI.standardExclamation(Labels.getLabel("common.msg.ko.notification"));
			logger.error("Errore nel cambiamento dell'operatore: " + e.getMessage());
			e.printStackTrace();
			return false;
		} 
//		Object o=db.cambiaOperatore(h);
		hashDaPassare.remove("referente_new");
		h.clear();
		HProgToMove.clear();
		reg_interv.clear();
		aggiornaTabella();
		return true;
	}
	
	protected boolean controlloSelezioneOp(){
//		cariObjectTableModel modello=(cariObjectTableModel) this.JCariTableOperatore.getModel();
		//System.out.println("controlloSelezioneOp"+modello.getRowCount());
		int numSelez=0;
		if(caribellbOperatori.getItemCount()==-1)return false;
		CaribelListModel modello = ((CaribelListModel)caribellbOperatori.getModel());
		if(caribellbOperatori.getSelectedIndex()==-1){
			hashDaPassare.remove("referente_new");
			UtilForUI.standardExclamation(Labels.getLabel("common.selezionareOperatore"));
			return false;
		}else{
			String cod_oper = (String) modello.getFromRow(caribellbOperatori.getSelectedIndex(), "cod_oper");
			hashDaPassare.put("referente_new", cod_oper);
		}
		return true;
	}	

	protected boolean controlloSelezioneCheck() throws Exception{
		return controlloSelezioneCheck(true);
	}
	
	protected boolean controlloSelezioneCheck(boolean controllaEsecutore) throws Exception{
		int n_day=getDaySelezionati();
		if(n_day==0){
			UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.selezionaGiorni"));
			return false;
		}
		int n_cart=getCartelleSelezionate();
		if(n_cart==0){
			UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.selezionaAssistiti"));
			return false;
		}
		if(cbx_operatore.getSelectedIndex()<0 || cbx_operatore.getSelectedValue().isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.selezionaReferente"));
			return false;
		}
		if(controllaEsecutore){
			if(cbx_operatore_esec.getSelectedIndex()<0 || cbx_operatore_esec.getSelectedValue().isEmpty()){
				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.selezionaEsecutore"));
				return false;
			}
		}
		return true;
	}

	public void onConfermaSelezione(Event event) throws Exception{
		if(controlloSelezioneOp() && controlloSelezioneCheck(false)){
			if(cambioOperatore()){
				onCaricaOperatori(event);
			}
		}
		return;
	}
	
	public void onCaricaOperatori(Event event) throws Exception{
	      String ragg= raggruppamento.getSelectedValue();//panel_ubicazione.getRaggruppamento();
	      String stDistretto=distretto.getSelectedValue(); //jPanelAgendaUbicazione.getComboDistretti();
	      if(stDistretto.trim().equals("TUTTO"))
	        stDistretto="";
	      String stZona=zona.getSelectedValue(); //jPanelAgendaUbicazione.getComboZone();
	      if(stZona.trim().equals("TUTTO"))
	        stZona="";
	      String stPresidio=cbx_sedeCO.getSelectedValue(); //presidio_comune_area.getSelectedValue(); //jPanelAgendaUbicazione.getComboTerzoLivello();
	      if(stPresidio.trim().equals("TUTTI"))
	        stPresidio="";
	      Vector griglia=new Vector();
	      Hashtable h=new Hashtable();
	      h.put("ragg",ragg);
	      h.put("distretto",stDistretto);
	      h.put("zona",stZona);
	      h.put("presidio",stPresidio);
	      h.put("tipo_operatore",(String) hashDaPassare.get("tipo_operatore"));
	      h.put("giorni", hashDaPassare.get("giorni"));
	      //System.out.println("Caricaoperatori==>"+h.toString());
	      Object o= modEJB.CaricaTabellaOperatoriNew(CaribelSessionManager.getInstance().getMyLogin(), h); //invokeGenericSuEJB(modEJB, h, "CaricaTabellaOperatoriNew");//db.CaricaTabellaOperatori(h);
	      if (o!=null){
	    	  if(((Vector)o).size()<=0){
	    		  	Clients.showNotification(Labels.getLabel("common.noOperatori"), "info", self, "middle_center", 2500);
//	  				UtilForUI.standardExclamation(Labels.getLabel("common.noOperatori"));
	  				griglia = new Vector();
//	    		  return;
	    	  }else griglia=(Vector)o;
	      }
	      caribellbOperatori.setModel(new CaribelListModel<ISASRecord>(griglia));
	      caribellbOperatori.invalidate();
	}
	
	class ClickTask extends java.util.TimerTask{
		public void run(){
			singleClick();
		}
	}
    private void singleClick()
    {
        logger.debug("Single click");
        try {
			doSingleclick();
		} catch (Exception e) {
			doShowException(e);
		}
    }
    
    public void onCalendar(Event event) throws Exception{
//    	Calendar myCalendar = Calendar.getInstance();
//    	org.zkoss.zul.Calendar calendarietto = (org.zkoss.zul.Calendar)((ForwardEvent) event).getOrigin().getTarget();
//    	Datebox calendarietto = (Datebox) ((ForwardEvent) event).getOrigin().getTarget();

    	Object date = fakeDate.getValue();
//    	calendarietto.setValue(null);
    	if(date!=null){
    		String message=null;
    		Map params = new HashMap();
    		params.put("width", 500);
    		Date dataSelezionata = (Date) date;
    		Calendar c = Calendar.getInstance(); 
    		c.setTime(dataSelezionata); 
    		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
    		String dataIni=df.format(c.getTime()); //dt.getDataNGiorni(ultimogg, 1);
    		Vector settimana=getSettimanaDa(dataIni,"gg-mm-aaaa");
    		hashDaPassare.put("giorni", settimana);
//    		Hashtable h = new Hashtable<String, Object>();
//    		h.put("referente",(String)hashDaPassare.get("referente"));
//    		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
//    		h.put("giorni", settimana);

    		Vector caricata = myEJB.query_agenda(CaribelSessionManager.getInstance().getMyLogin(), hashDaPassare );
    		
    		if(xDrag==-1 && yDrag==-1 && ySel==-1) {
    			hashDaPassare.put("giorni",settimana);
    		    hashDaPassare.put("mese", mesi[dt.getMese(dataIni)-1]);
    		    hashDaPassare.put("anno",""+dt.getAnno(dataIni));
    		     this.aggiornaTabella();
      			if(caricata.isEmpty()){
     				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.settimanaNonCaricata"));
      			}
//    			UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.noSelezione"));
    			return;
    		}else{
    			if(caricata.isEmpty()){
    				UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.settimanaNonCaricata"));
    				return;
    			}
    			if(xDrag!=-1 && yDrag!=-1 ){
    				setCellaSelez(-1,-1);
    				xOld=-1;yOld=-1;
    				hA.put("ag_data",formattaData(df.format(dataSelezionata)));

    				message=Labels.getLabel("agendaSposta.msg.copiareOSpostareAppuntamento", new String[]{Labels.getLabel("agendaSposta.msg.spostare"),
    						costruisciMsg(hDa), df.format(dataSelezionata)});
    				Messagebox.show(message, Labels.getLabel("messagebox.attention"),  new Messagebox.Button[] {Messagebox.Button.CANCEL, Messagebox.Button.YES, Messagebox.Button.NO}, new String[]{"Cancel","Copia", "Sposta"}, Messagebox.QUESTION, Messagebox.Button.CANCEL, new EventListener<Messagebox.ClickEvent>() {
    					public void onEvent(Messagebox.ClickEvent event) throws Exception {
    						if(event.getButton()!=null){
    							execUpdate(event.getButton().ordinal(),"CELLDAY",hDa,hA);
    						}
    					}},params);
    				xDrag=-1;yDrag=-1;
    			}else if(ySel!=-1 || (jCheckBox1.isChecked()||jCheckBox2.isChecked()||jCheckBox3.isChecked()||jCheckBox4.isChecked()||jCheckBox5.isChecked()||jCheckBox6.isChecked()||jCheckBox7.isChecked())){
    				String giornoDa= df.format(UtilForBinding.getDateFromIsas(key_data[1][ySel]));

    				hA2.put("ag_data",formattaData(df.format(dataSelezionata)));
    				hDa2.put("ag_data",formattaData(giornoDa));

    				message=Labels.getLabel("agendaSposta.msg.copiareOSpostareGiorno", new String[]{Labels.getLabel("agendaSposta.msg.spostare"),
    						giornoDa, df.format(dataSelezionata)});
    				Messagebox.show(message, Labels.getLabel("messagebox.attention"),  new Messagebox.Button[] {Messagebox.Button.CANCEL, Messagebox.Button.YES, Messagebox.Button.NO}, new String[]{"Cancel","Copia", "Sposta"}, Messagebox.QUESTION, Messagebox.Button.CANCEL, new EventListener<Messagebox.ClickEvent>() {
    					public void onEvent(Messagebox.ClickEvent event) throws Exception {
    						if(event.getButton()!=null){
    							execUpdate(event.getButton().ordinal(),"COLUMN",hDa2,hA2);
    						}
    					}},params);
    				ySel=-1;
    			}
    		}
    	}
    }
    
	public void onChange$cbx_tipo_operatore(Event evt) throws Exception{
//		if(cbx_tipo_operatore.get
		logger.trace(Thread.currentThread().getStackTrace()[1].getMethodName());
		hashDaPassare.put("figprof", cbx_tipo_operatore.getSelectedValue());
		hashDaPassare.put("tipo_operatore", cbx_tipo_operatore.getSelectedValue());
		cbx_operatore.clear();
		CaribelComboRepository.comboPreLoad("combo_operatori_"+ManagerProfileBase.getDistrettoOperatore(getProfile())+"_"+cbx_sede.getSelectedValue()+"_"+cbx_tipo_operatore.getSelectedValue(), new OperatoriEJB(), "query",hashDaPassare, cbx_operatore,      null, "codice", "cognomeNome", true);
		hashDaPassare.put("siOperatoreFittizio", "false");
		cbx_operatore_esec.clear();
		CaribelComboRepository.comboPreLoad("combo_operatori_"+ManagerProfileBase.getDistrettoOperatore(getProfile())+"_"+cbx_sede.getSelectedValue()+"_"+cbx_tipo_operatore.getSelectedValue(), new OperatoriEJB(), "query",hashDaPassare, cbx_operatore_esec, null, "codice", "cognomeNome", true);
		hashDaPassare.remove("siOperatoreFittizio");
	}

	public void onChange$cbx_sede(Event evt) throws Exception{
		logger.trace(Thread.currentThread().getStackTrace()[1].getMethodName());
		hashDaPassare.put("presidio_op", cbx_sede.getSelectedValue());
		cbx_operatore.clear();
		CaribelComboRepository.comboPreLoad("combo_operatori_"+ManagerProfileBase.getDistrettoOperatore(getProfile())+"_"+cbx_sede.getSelectedValue()+"_"+cbx_tipo_operatore.getSelectedValue(), new OperatoriEJB(), "query",hashDaPassare, cbx_operatore,      null, "codice", "cognomeNome", true);
		hashDaPassare.put("siOperatoreFittizio", "false");
		cbx_operatore_esec.clear();
		CaribelComboRepository.comboPreLoad("combo_operatori_"+ManagerProfileBase.getDistrettoOperatore(getProfile())+"_"+cbx_sede.getSelectedValue()+"_"+cbx_tipo_operatore.getSelectedValue(), new OperatoriEJB(), "query",hashDaPassare, cbx_operatore_esec, null, "codice", "cognomeNome", true);
		hashDaPassare.remove("siOperatoreFittizio");
	}

	public void onChange$cbx_operatore(Event evt){
		logger.trace(Thread.currentThread().getStackTrace()[1].getMethodName());
		hashDaPassare.put("referente", cbx_operatore.getSelectedValue());
	}

	public void onChange$cbx_operatore_esec(Event evt){
		logger.trace(Thread.currentThread().getStackTrace()[1].getMethodName());
		hashDaPassare.put("esecutivo", cbx_operatore_esec.getSelectedValue());
	}
	
	public void onClick$btn_filtri(){
		filtriAgenda.setOpen(!filtriAgenda.isOpen());
		filtriAgenda.setVisible(filtriAgenda.isOpen());
		filtriAgendaChiuso.setVisible(!filtriAgenda.isOpen());
	}
	
	public void onOpen$filtriAgenda(Event evt){
		filtriAgenda.setVisible(filtriAgenda.isOpen());
		filtriAgendaChiuso.setVisible(!filtriAgenda.isOpen());
		filtriAgendaSintesi.setValue(Labels.getLabel("agenda.filtriAgendaSintesi", new String [] {cbx_tipo_operatore.getValue(), cbx_sede.getValue(), cbx_operatore.getValue(), cbx_operatore_esec.getValue()}));
	}
	
	public void onCloseAgenda(Event evt){
		filtriAgenda.setVisible(filtriAgenda.isOpen());
		filtriAgendaChiuso.setVisible(!filtriAgenda.isOpen());
	}
	
	public void onNuovo(Event event) throws Exception{
		if(!checkOperatoreEsecutore()){
			return;
		}
		hashDaPassare.clear();
	    Date dataNuova = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(fakeDate.getValue()); 
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	    if(jCheckBox1.isChecked()){
//	    	c.add(Calendar.DATE, 0);
	    }else if(jCheckBox2.isChecked()){
	    	c.add(Calendar.DATE, 1);
	    }else if(jCheckBox3.isChecked()){
	    	c.add(Calendar.DATE, 2);
	    }else if(jCheckBox4.isChecked()){
	    	c.add(Calendar.DATE, 3);
	    }else if(jCheckBox5.isChecked()){
	    	c.add(Calendar.DATE, 4);
	    }else if(jCheckBox6.isChecked()){
	    	c.add(Calendar.DATE, 5);
	    }else if(jCheckBox7.isChecked()){
	    	c.add(Calendar.DATE, 6);
	    }else {
	    	c.setTime(fakeDate.getValue());
	    }
	    dataNuova = c.getTime();
	    calend.setTime(dataNuova);
	    	
		hashDaPassare.putAll(UtilForBinding.getHashtableFromComponent(filtriAgenda));
		hashDaPassare.put("mese", mesi[calend.get(Calendar.MONTH)]);
		hashDaPassare.put("anno", calend.get(Calendar.YEAR));
		hashDaPassare.put("giorni", getVettoreSettimana());
		hashDaPassare.put("giorno", calend.getTime());
	    hashDaPassare.put("chiamante", this);
    	logger.info("Apro Nuovo Assistito"+hashDaPassare.toString());
		Executions.getCurrent().createComponents(AgendaNuovoFormCtrl.myPathZul, self, hashDaPassare);
//		daSalvare=new Hashtable();
	}
	
	protected int getPadd() {
		return 1;
	}
	
	public void onClick$btn_update() throws Exception{
		if(!UtilForComponents.testRequiredFieldsNoCariException(filtriAgenda)){
			return;
		}
		hashDaPassare.put("figprof", cbx_tipo_operatore.getSelectedValue());
		hashDaPassare.put("presidio_op", cbx_sede.getSelectedValue());
		if(!cbx_operatore.getSelectedValue().isEmpty()){
			hashDaPassare.put("referente", cbx_operatore.getSelectedValue());
		}else{
			hashDaPassare.remove("referente");
		}
		if(!cbx_operatore_esec.getSelectedValue().isEmpty()){
			hashDaPassare.put("esecutivo", cbx_operatore_esec.getSelectedValue());
		}else{
			hashDaPassare.remove("esecutivo");
		}
		hashDaPassare.put("tipo_operatore", cbx_tipo_operatore.getSelectedValue());
		execSelect();
		
	}
	
	public void onClick$btn_week() throws Exception{
//		calendario.setVisible(true);
		fakeDate.open();
//		fakeDate.setVisible(true);
	}
	public void onSelect$calendario() throws Exception{
//		calendario.setVisible(false);
		
		if(!UtilForComponents.testRequiredFieldsNoCariException(filtriAgenda)){
			return;
		}
		hParameters.put("figprof", cbx_tipo_operatore.getSelectedValue());
		hParameters.put("presidio_op", cbx_sede.getSelectedValue());
		if(!cbx_operatore.getSelectedValue().isEmpty()){
			hParameters.put("referente", cbx_operatore.getSelectedValue());
		}else{
			hParameters.remove("referente");
		}
		if(!cbx_operatore_esec.getSelectedValue().isEmpty()){
			hParameters.put("esecutivo", cbx_operatore_esec.getSelectedValue());
		}else{
			hParameters.remove("esecutivo");
		}
		hParameters.put("tipo_operatore", cbx_tipo_operatore.getSelectedValue());
		execSelect();
		
	}
	
	@Override
	protected int execSelect() throws Exception {
		hashDaPassare.clear();
	    calend.setTime(fakeDate.getValue());
		hashDaPassare.putAll(UtilForBinding.getHashtableFromComponent(filtriAgenda));
		hashDaPassare.put("mese", mesi[calend.get(Calendar.MONTH)]);
		hashDaPassare.put("anno", calend.get(Calendar.YEAR));
		hashDaPassare.put("giorni", getVettoreSettimana());
		
		int ret = super.execSelect();
		boolean referenteImpostato = !ISASUtil.getValoreStringa(hashDaPassare, "referente").isEmpty();
		headers1.setVisible(referenteImpostato && cb_mostraKmTempi.isChecked());
		headers2.setVisible(referenteImpostato && cb_mostraKmTempi.isChecked());
		if(referenteImpostato){
			super.ricercaKmMm();
		}
		if(pannelloCambiaOperatore.isOpen()){
			onCaricaOperatori(null);
		}
		return ret;
	}

	@Override
	protected void aggiornaTabella() throws Exception{
//		Vector vSettAttiva=(Vector)hashDaPassare.get("giorni");
//		fakeDate.setValue(df.parse(vSettAttiva.firstElement().toString()));
		this.execSelect();
		vaiAllUltimaSelezione();
	}
	
	protected void succSett() throws Exception {
		logger.trace("settimana Succesiva");
		calend.setTime(fakeDate.getValue());
		calend.add(Calendar.DATE, 7);
		Date nuovaData = calend.getTime();
		fakeDate.setValue(nuovaData);
		super.succSett();
	}

	protected void precSett() throws Exception {
		logger.trace("settimana Precedente");
		calend.setTime(fakeDate.getValue());
		calend.add(Calendar.DATE, -7);
		Date nuovaData = calend.getTime();
		fakeDate.setValue(nuovaData);
		super.precSett();
	}
	
	@Override
	protected void doStampa() {
		if(!CaribelSessionManager.getInstance().getIsasUser().canIUse(ChiaviISASSinssntWeb.ST_AGVIS)){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.noDirittoStampa"));
			return;
		}
		Executions.createComponents("/web/ui/report/reportAgenda.zul", self, hashDaPassare);
	}
	
	@Override
	protected void notEditable() {
		boolean ricercaGruppo = iu.canIUse(ChiaviISASSinssntWeb.APRIAGGR,"CONS");
		UtilForBinding.setComponentReadOnly(cbx_tipo_operatore, !ricercaGruppo);
		UtilForBinding.setComponentReadOnly(cbx_sede, !ricercaGruppo);
		UtilForBinding.setComponentReadOnly(cbx_operatore, !ricercaGruppo);
		UtilForBinding.setComponentReadOnly(cbx_operatore_esec, !iu.canIUse(ChiaviISASSinssntWeb.AG_OPE,"CONS"));
		super.notEditable();
		
	}
	
	public void onCarica(Event event) throws Exception{
		if(!checkOperatoreReferente()){
			return;
		}
		Vector settimanaSelezionata = getVettoreSettimana();
//		if (cal.getSelectedDates() == null || cal.getSelectedDates().isEmpty()){//settimanaSelezionata.isEmpty()){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimana"));
//			//	    	 new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana!","Attenzione!").show();
//			return;
//		}else if (cod_operatore.getValue()==null){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
//			//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
//			cod_operatore.focus();
//			return;
//		}
		
		//CARICA SETTIMANE
		Integer num_sett = numeroSettimane.getValue();
		if(num_sett == null){
			num_sett= 1;
		}
//		int num_sett=Integer.parseInt(st);
//		String giornoSel=(String)settimanaSelezionata.firstElement();
//		String gg=giornoSel.substring(0,2);
//		if(gg.substring(0,1).equals("0"))
//			gg=gg.substring(1);
//
//		String mm=giornoSel.substring(3,5);
//		if(mm.substring(0,1).equals("0"))
//			mm=mm.substring(1);
//		String aaaa=giornoSel.substring(6);
//		GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(aaaa), Integer.parseInt(mm)-1, Integer.parseInt(gg));
//		gc.add(gc.DATE,num_sett*7);
//		String data_ultima=getData(gc);
//		DataWI dt=new DataWI(data_ultima,1);
//		GregorianCalendar gc2 = new GregorianCalendar(Integer.parseInt(aaaa), Integer.parseInt(mm)-1, Integer.parseInt(gg));
//		String data_successiva=getData(gc2);
//		int giorno=1;
		Vector v_giorni=new Vector();
		Vector v_settimane=new Vector();
		
		v_settimane.add(settimanaSelezionata);//aggiungo la settimana selezionata
		for (int i = 1; i < num_sett; i++) { //ciclo e aggiungo una settimana ogni settimana > 1 
			Calendar c = Calendar.getInstance(); 
			c.setTime(fakeDate.getValue()); 
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			c.add(Calendar.WEEK_OF_YEAR, i);
			String dataIni=df.format(c.getTime()); //dt.getDataNGiorni(ultimogg, 1);
			Vector settimana=getSettimanaDa(dataIni,"gg-mm-aaaa");
			v_settimane.add(settimana);
		}
		
//		while(dt.isSuccessiva(data_successiva)){
//			if (giorno==8){
//				v_settimane.addElement(v_giorni);
//				v_giorni=new Vector();
//				giorno=1;
//			}
//			v_giorni.addElement(formattaData(data_successiva));
//			giorno++;
//			gc2.add(gc2.DATE,1);
//			data_successiva=getData(gc2);
//		}
//		v_settimane.addElement(v_giorni);

//		settimanaSelezionata.clear();
//		settimanaSelezionata=v_settimane;
		
		hashDaPassare.clear();
	    calend.setTime(fakeDate.getValue());
		hashDaPassare.putAll(UtilForBinding.getHashtableFromComponent(filtriAgenda));
		hashDaPassare.put("mese", mesi[calend.get(Calendar.MONTH)]);
		hashDaPassare.put("anno", calend.get(Calendar.YEAR));
		hashDaPassare.put("giorni", v_settimane);
		hashDaPassare.put("giorno", calend.getTime());
		
		//System.out.println("CARICO in agenda la SETTIMANA "+daSalvare.toString());
		this.execCarica();
	}
	private boolean checkOperatoreReferente() {
		boolean ret = false;
		if (cbx_operatore.getSelectedValue()==null || cbx_operatore.getSelectedValue().isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
			//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			cbx_operatore.focus();
			return false;
		}else{
			ret=true;
		}
		return ret;
	}

	private boolean checkOperatoreEsecutore() {
		boolean ret = false;
		if (cbx_operatore_esec.getSelectedValue()==null || cbx_operatore_esec.getSelectedValue().isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaEsecutivo"));
			//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			cbx_operatore_esec.focus();
			return false;
		}else{
			ret=true;
		}
		return ret;
	}
	
	public int execCarica() throws Exception{
        Object o = invokeGenericSuEJB(new AgendaEJB(), hashDaPassare, "carica_agenda"); //db.selezCaricati(h);
        Vector griglia=new Vector();
        if (o==null){
        	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.errore")); //new it.pisa.caribel.swing2.cariInfoDialog(null,"Si è verificato un errore contattare l'Assistenza","Attenzione!").show();
          return -1;
        }else{
			HashMap<String, String>	params = new HashMap<String, String>();
            params.put("width", "500px");
        	UtilForUI.standardInfo((String)o, params);
        }
//        hashDaPassare=new Hashtable();
        this.execSelect();
        return 1;
    }
	
	@Override
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
			
			deleteSuEJB(currentBean, currentIsasRecord);
			doFreezeForm();
//			if(caribelGrid!=null){
//				//REFRESH SULLA LISTA
//				caribelGrid.doRefresh();
//			}
//			if(caribelContainerCtrl!=null){
//				caribelContainerCtrl.doRefreshOnDelete(compChiamante);
//			}
//			compChiamante.detach();
			aggiornaTabella();
			Clients.showNotification(Labels.getLabel("form.delete.ok.notification"),"info",compChiamante,"middle_center",2500);
		}catch (Exception e){
			doShowException(e);
		}
	}
	
	@Override
	protected void deleteSuEJB(Object myEJB, ISASRecord myDbr) throws Exception {
		if(xDrag==-1 || yDrag==-1){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaGiornoCancellare"));
			return;
		}
		myEJBEdit.cancella_intervCella(CaribelSessionManager.getInstance().getMyLogin(), hDa);
	}
	
	public boolean isDeletable() throws ISASMisuseException{
		ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
		boolean canIUse = true;
		boolean canIDelete = true;
		if(keyPermission!=null && !keyPermission.equals("")){
			canIUse = iu.canIUse(keyPermission,"CANC");
		}
		canIDelete = xDrag!=-1 && yDrag!=-1; //currentIsasRecord != null && iu.canIDelete(currentIsasRecord);
		return canIUse && canIDelete;
	}

	public boolean isSavable() throws ISASMisuseException{
		return iu.canIUse(ChiaviISASSinssntWeb.AG_REG,"CONS");
	}
	
	public void onApriDiario(Event evt){
		Hashtable data = (Hashtable) evt.getData();
		Map<String, Object> params = new Hashtable<String, Object>();
		params.put("mode", "overlapped");
		params.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(data, "ag_cartella"));
		params.put(CostantiSinssntW.N_CONTATTO, ISASUtil.getValoreStringa(data, "ag_contatto"));
		params.put(CostantiSinssntW.CTS_ID_SKSO, "-1");
		params.put("tipo_operatore", cbx_tipo_operatore.getSelectedValue());
		Executions.createComponents(DiarioGridCtrl.myPathFormZul, self, params );
//		apriDiario((String) evt.getData());
	}

	public void onStampaScheda(Event evt){
//		stampaScheda((String) evt.getData());
	}

	//apro una finestra popup con il diario dell'assistito
	public void apriDiario(String n_cartella) {

		return;	
	}

	//stampo la scheda cartella assistito
	public void stampaScheda(String n_cartella) {
		// TODO Auto-generated method stub
		return;
	}

	@Override
	protected int getColonnaArray(int colonnaSelezionata) {
		return colonnaSelezionata - 4;
	}
}

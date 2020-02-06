package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.DataWI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelArray;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AgendaFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "AGENDA";
	private AgendaEJB myEJB = new AgendaEJB();

	private CaribelTextbox 	cod_operatore;
	private CaribelCombobox 	desc_operatore;
	private CaribelTextbox 	cod_operatore_esec;
	private CaribelCombobox 	desc_operatore_esec;
	
	private Component operatore;
	private Component operatoreEsec;
	CaribelSearchCtrl operatoreCtrl;
	CaribelSearchCtrl operatoreEsecCtrl;

	private CaribelTextbox tipo_operatore;
	private CaribelTextbox qualificaOperatore;
	private CaribelIntbox JCariTextFieldNumSett;

//	protected String pathGridZul = "/web/ui/sinssnt/interventi/accessiGrid.zul";

	private ISASUser iu;

	private final int WAIT = 0;
	private final int INSERT = 1;
	private final int UPDATE_DELETE = 2;
	private final int CONSULTA = 3;

	private TestComposer cal;

	private Vector settimanaCaricata;
	private	Vector settimanaSelezionata = new Vector();

	private Hashtable daSalvare;

	private Component registra;

	private Textbox tbx;
	private Listbox caricate;
	private CaribelTextbox tipo_operatoretmp;

	protected String[] mesi = { "Gennaio", "Febbraio", "Marzo", "Aprile",
            "Maggio", "Giugno", "Luglio", "Agosto",
            "Settembre", "Ottobre", "Novembre", "Dicembre"
              };

	protected Button btn_registra;
	protected Button btn_carica;
	protected Button btn_sposta;
	protected Button btn_cambia;
	
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			iu = CaribelSessionManager.getInstance().getIsasUser();

			this.setMethodNameForDelete("deleteAll");
			String tipoOperatoreContainer = UtilForContainer.getTipoOperatorerContainer();
			tipo_operatore.setValue(tipoOperatoreContainer);
			tipo_operatoretmp.setValue(tipoOperatoreContainer);
			operatoreCtrl = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
			operatoreCtrl.putLinkedSearchObjects("figprof", tipo_operatore.getValue());
			operatoreCtrl.putLinkedSearchObjects("tipo_op", tipo_operatore.getValue());
			operatoreCtrl.putLinkedSearchObjects("zona_op", getProfile().getStringFromProfile("zona_operatore"));
			operatoreCtrl.setReadonly(!iu.canIUse(ChiaviISASSinssntWeb.APRIAGGR,"CONS"));
			operatoreCtrl.putLinkedComponent("tipo", tipo_operatoretmp);
			operatoreEsecCtrl = (CaribelSearchCtrl) operatoreEsec.getAttribute(MY_CTRL_KEY);
			operatoreEsecCtrl.putLinkedSearchObjects("figprof", tipo_operatoretmp);
			operatoreEsecCtrl.putLinkedSearchObjects("tipo_op", tipo_operatoretmp);
			operatoreEsecCtrl.putLinkedSearchObjects("zona_op", getProfile().getStringFromProfile("zona_operatore"));
			if(tipoOperatoreContainer.isEmpty() || tipoOperatoreContainer.equals(getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE))){
				operatoreCtrl.putLinkedSearchObjects("tipo_op_lock", !tipoOperatoreContainer.isEmpty());
				operatoreEsecCtrl.putLinkedSearchObjects("tipo_op_lock", !tipoOperatoreContainer.isEmpty());
				String operatoreCorrente = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
				cod_operatore.setValue(operatoreCorrente);
				Events.sendEvent(Events.ON_CHANGE, cod_operatore, operatoreCorrente);
				cod_operatore_esec.setValue(operatoreCorrente);
				Events.sendEvent(Events.ON_CHANGE, cod_operatore_esec, operatoreCorrente);
			}
			cod_operatore.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					execSelect();
					if(tipo_operatoretmp.getValue()!=null && operatoreEsecCtrl.getCurrentRecord() != null && !operatoreEsecCtrl.getCurrentRecord().get("tipo").equals(tipo_operatoretmp.getValue())){
						cod_operatore_esec.setValue(null);
						Events.sendEvent(Events.ON_CHANGE, cod_operatore_esec, null);
					}
					return;
				}});
			//((Toolbarbutton) btn_registra).setDisabled(true);
			btn_registra.setDisabled(!iu.canIUse(ChiaviISASSinssntWeb.AG_REG,"CONS"));
			btn_carica.setDisabled(!iu.canIUse(ChiaviISASSinssntWeb.AG_CAR,"INSE"));
            btn_sposta.setDisabled(!iu.canIUse(ChiaviISASSinssntWeb.AG_SPO,"CONS"));
            btn_cambia.setDisabled(!iu.canIUse(ChiaviISASSinssntWeb.AG_OPE,"CONS"));

            if((arg.get("mode") !=null && arg.get("mode").equals("overlapped"))||caribelContainerCtrl==null){
				((Window)getForm()).setMode("overlapped");
				((Window)getForm()).setPosition("center");
				((Window)getForm()).setClosable(true);
				((Window)getForm()).setSizable(true);
				((Window)getForm()).setMaximizable(true);
				((Window)getForm()).setMinimizable(false);
//				((Window)getForm()).setWidth("96%");  
//				((Window)getForm()).setHeight("93%");
			}
			
			initForm();
//			doFreezeForm();
		}catch(Exception e){
			doShowException(e);
		}
	}

	private void initForm() throws Exception {
		execSelect();
		onCalendar(null);
	}
	
	List<List<String>> getNumberOfWeeks(int year, int month) {
        List<List<String>> weekdates = new ArrayList<List<String>>();
        List<String> dates;
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, 1);
        while (c.get(Calendar.MONTH) == month) {
                dates = new ArrayList<String>();
              while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                c.add(Calendar.DAY_OF_MONTH, -1);
              }
              dates.add(format.format(c.getTime()));
              c.add(Calendar.DAY_OF_MONTH, 6);
              dates.add(format.format(c.getTime()));
              weekdates.add(dates);
              c.add(Calendar.DAY_OF_MONTH, 1);
        }
        logger.debug(weekdates);
        return weekdates;
      }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int execSelect() throws Exception{
        Hashtable h=new Hashtable();
        h.put("referente",cod_operatore.getValue());
        h.put("tipo_operatore", tipo_operatoretmp.getValue());
        Object o = invokeGenericSuEJB(new AgendaEJB(), h, "query_caricati"); //db.selezCaricati(h);
        Vector griglia=new Vector();
        if (o!=null){
          if(((Vector)o).size()>0)
              griglia=(Vector)o;
        }else{
        	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.errore")); //new it.pisa.caribel.swing2.cariInfoDialog(null,"Si è verificato un errore contattare l'Assistenza","Attenzione!").show();
          return -1;
        }
        settimanaCaricata=new Vector();
        settimanaCaricata=(Vector)griglia.clone();
        //System.out.println("carico in profile settimanaCaricata"+settimanaCaricata.toString());
        //FIXME verificare la lettura
        profile.putParameter(cod_operatore.getValue()+"_settimanaCaricata",(Vector)settimanaCaricata.clone());
       // System.out.println("nel profile:"+((Vector)profile.getObject("settimanaCaricata")).toString());
        onCalendar(null);
        return 1;
    }

	public int execCarica() throws Exception{
        Object o = invokeGenericSuEJB(new AgendaEJB(), daSalvare, "carica_agenda"); //db.selezCaricati(h);
//        Object o=db.caricaAgenda(daSalvare);
        Vector griglia=new Vector();
        if (o==null){
        	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.errore")); //new it.pisa.caribel.swing2.cariInfoDialog(null,"Si è verificato un errore contattare l'Assistenza","Attenzione!").show();
          return -1;
        }else{
			HashMap<String, String>	params = new HashMap<String, String>();
            params.put("width", "500px");
        	UtilForUI.standardInfo((String)o, params);
//            new it.pisa.caribel.swing2.cariInfoDialog(null,(String)o,"Attenzione!").show();
        }
        daSalvare=new Hashtable();
//        settimanaSelezionata=new Vector();
        this.execSelect();
//        this.drawMese();
        return 1;
    }
	
	  /**
	   * Trasferisce il vettore giorni selezionati nella
	   * hashtable daSalvare, che contiene, oltre ai giorni,
	   * i due operatori
	   */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void prepareHashtable()
	  {
	    // Inserisce anno e progr offerta nella hashtable daSalvare
	    daSalvare=new Hashtable();
	    this.daSalvare.put("referente", cod_operatore.getValue());
	    this.daSalvare.put("referente_nome", desc_operatore.getValue());
	    this.daSalvare.put("tipo_operatore", tipo_operatoretmp.getValue());

	    this.daSalvare.put("esecutivo", cod_operatore_esec.getValue());
	    this.daSalvare.put("esecutivo_nome", desc_operatore_esec.getValue());
	    // Aggiunge il vector di date alla hashtable daSalvare
	    this.daSalvare.put("giorni", settimanaSelezionata);
	    
	    java.util.Calendar calend = Calendar.getInstance();
	    calend.setTime(cal.getValue());
	    this.daSalvare.put("mese", mesi[calend.get(Calendar.MONTH)]);
	    this.daSalvare.put("anno", calend.get(Calendar.YEAR));

	  }
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return false;
	}
	
	public void onCarica(Event event) throws Exception{
		settimanaSelezionata = getVettoreSettimana();
		if (cal.getSelectedDates() == null || cal.getSelectedDates().isEmpty()){//settimanaSelezionata.isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimana"));
			//	    	 new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana!","Attenzione!").show();
			return;
		}else if (cod_operatore.getValue()==null){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
			//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			cod_operatore.focus();
			return;
		}
		
		//CARICA SETTIMANE
		Integer num_sett = JCariTextFieldNumSett.getValue();
		if(num_sett == null){
			num_sett= 1;
		}
//		int num_sett=Integer.parseInt(st);
		String giornoSel=(String)settimanaSelezionata.firstElement();
		String gg=giornoSel.substring(0,2);
		if(gg.substring(0,1).equals("0"))
			gg=gg.substring(1);

		String mm=giornoSel.substring(3,5);
		if(mm.substring(0,1).equals("0"))
			mm=mm.substring(1);
		String aaaa=giornoSel.substring(6);
		GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(aaaa), Integer.parseInt(mm)-1, Integer.parseInt(gg));
		gc.add(gc.DATE,num_sett*7);
		String data_ultima=getData(gc);
		DataWI dt=new DataWI(data_ultima,1);
		GregorianCalendar gc2 = new GregorianCalendar(Integer.parseInt(aaaa), Integer.parseInt(mm)-1, Integer.parseInt(gg));
		String data_successiva=getData(gc2);
		int giorno=1;
		Vector v_giorni=new Vector();
		Vector v_settimane=new Vector();
		while(dt.isSuccessiva(data_successiva)){
			if (giorno==8){
				v_settimane.addElement(v_giorni);
				v_giorni=new Vector();
				giorno=1;
			}
			v_giorni.addElement(formattaData(data_successiva));
			giorno++;
			gc2.add(gc2.DATE,1);
			data_successiva=getData(gc2);
		}
		v_settimane.addElement(v_giorni);

		settimanaSelezionata.clear();
		settimanaSelezionata=v_settimane;
		prepareHashtable();
		//System.out.println("CARICO in agenda la SETTIMANA "+daSalvare.toString());
		this.execCarica();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Vector getVettoreSettimana() {
		Vector sett = new Vector();
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
		cal.onMySelect(null);
			
		for (Iterator iterator = cal.getSelectedDates().iterator(); iterator.hasNext();) {
			Date data = (Date) iterator.next();
			sett.add(df.format(data));
		}
		return sett;
	}

	String getData(GregorianCalendar gc){
		int dd_i=gc.get(java.util.Calendar.DAY_OF_MONTH);
		int mm_i=gc.get(java.util.Calendar.MONTH)+1;
		int aaaa=gc.get(java.util.Calendar.YEAR);
		String dd=""+dd_i;
		String mm=""+mm_i;
		if(dd_i<=9) dd="0"+dd_i;
		if(mm_i<=9) mm="0"+mm_i;
		String data=aaaa+mm+dd;
		return data;
	}
	 
	 private String formattaData(String campo){
		 //restituisce la data nel formato gg-mm-aaaa
		 String dataret="00-00-00000";
		 String sep="-";
		 if (campo!=null && !campo.equals("")){
			 String dataLetta=campo;
			 if (dataLetta.length()==8){
				 String aaaa=dataLetta.substring(0,4);
				 String mm=dataLetta.substring(4,6);
				 String gg=dataLetta.substring(6,8);
				 dataret=gg+sep+mm+sep+aaaa;
			 }
		 }
		 //System.out.println("DATA FORMATTATA="+dataret);
		 return dataret;
	 }
	 
	 public void onRegistra(Event event) throws Exception{
		 settimanaSelezionata = getVettoreSettimana();
		 if (cal.getSelectedDates().isEmpty()){//settimanaSelezionata.isEmpty()){
			 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimana"));
			 //	    	 new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana!","Attenzione!").show();
			 return;
		 }else if (cod_operatore.getValue()==null || cod_operatore.getValue().isEmpty()){
			 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
			 //	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			 cod_operatore.focus();
			 return;
		 }else if (cod_operatore_esec.getValue()==null || cod_operatore_esec.getValue().isEmpty()){
			 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaEsecutivo"));
			 //	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			 cod_operatore_esec.focus();
			 return;
		 }
		 //FIXME
		 //			else if (!isSettSel(back_selezionato)){
		 //				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimanaCaricata"));
		 ////		        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana già caricata!","Attenzione!").show();
		 //		        return;
		 //		     }
		 prepareHashtable();
		 //System.out.println("Apro la SETTIMANA "+daSalvare.toString());
		 //		     JFrameAgendaRegistra jfgr=new JFrameAgendaRegistra(this,daSalvare);
		 registra = Executions.getCurrent().createComponents(AgendaRegistraFormCtrl.myPathZul, self, daSalvare);
		 daSalvare=new Hashtable();
		 //		     settimanaSelezionata=new Vector();
		 //		     this.drawMese();
	 }
	 
	 public void onRegistraNew(Event event) throws Exception{
		 settimanaSelezionata = getVettoreSettimana();
		 if (cal.getSelectedDates().isEmpty()){//settimanaSelezionata.isEmpty()){
			 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimana"));
			 //	    	 new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana!","Attenzione!").show();
			 return;
		 }else if (cod_operatore.getValue()==null || cod_operatore.getValue().isEmpty()){
			 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
			 //	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			 cod_operatore.focus();
			 return;
		 }else if (cod_operatore_esec.getValue()==null || cod_operatore_esec.getValue().isEmpty()){
			 UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaEsecutivo"));
			 //	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
			 cod_operatore_esec.focus();
			 return;
		 }
		 //FIXME
		 //			else if (!isSettSel(back_selezionato)){
		 //				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimanaCaricata"));
		 ////		        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana già caricata!","Attenzione!").show();
		 //		        return;
		 //		     }
		 prepareHashtable();
		 //System.out.println("Apro la SETTIMANA "+daSalvare.toString());
		 //		     JFrameAgendaRegistra jfgr=new JFrameAgendaRegistra(this,daSalvare);
		 registra = Executions.getCurrent().createComponents(AgendaRegistraNewFormCtrl.myPathZul, self, daSalvare);
		 daSalvare=new Hashtable();
		 //		     settimanaSelezionata=new Vector();
		 //		     this.drawMese();
	 }
		
		public void onSposta(Event event) throws Exception{
			settimanaSelezionata = getVettoreSettimana();
			if (cal.getSelectedDates().isEmpty()){//settimanaSelezionata.isEmpty()){
				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimana"));
				//	    	 new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana!","Attenzione!").show();
				return;
			}else if (cod_operatore.getValue()==null){
				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
				//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
				cod_operatore.focus();
				return;
			}
			prepareHashtable();
			registra = Executions.getCurrent().createComponents(AgendaSpostaFormCtrl.myPathZul, self, daSalvare);
			daSalvare=new Hashtable();
		}

		public void onNuovo(Event event) throws Exception{
			if (cod_operatore_esec.getValue()==null || cod_operatore_esec.getValue().isEmpty()){
				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaEsecutivo"));
				//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
				cod_operatore_esec.focus();
				return;
			}
			settimanaSelezionata = getVettoreSettimana();
    	    prepareHashtable();
	    	logger.info("Apro Nuovo Assistito"+daSalvare.toString());
//	    	    JFrameAgendaNuovoAssistito jfgr=new JFrameAgendaNuovoAssistito(this,daSalvare);
//	    	    daSalvare=new Hashtable();
//	    	    settimanaSelezionata=new Vector();
//	    	    this.drawMese();
//			settimanaSelezionata = getVettoreSettimana();
//			prepareHashtable();
			registra = Executions.getCurrent().createComponents(AgendaNuovoFormCtrl.myPathZul, self, daSalvare);
			daSalvare=new Hashtable();
		}

		public void onHelp(Event event) throws Exception{
			HashMap<String, String>	params = new HashMap<String, String>();
            params.put("width", "600px");
			Messagebox.show(Labels.getLabel("agenda.msg.help"), Labels.getLabel("messagebox.info"), new Messagebox.Button[] {Messagebox.Button.OK}, null, Messagebox.INFORMATION, null, null, params);		
		}
		
		public void onCambia(Event event) throws Exception{
			settimanaSelezionata = getVettoreSettimana();
			if (cal.getSelectedDates().isEmpty()){//settimanaSelezionata.isEmpty()){
				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimana"));
				//	    	 new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana!","Attenzione!").show();
				return;
			}else if (cod_operatore.getValue()==null){
				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaReferente"));
				//	        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre inserire l\'operatore referente","Attenzione!").show();
				cod_operatore.focus();
				return;
			}
			 //FIXME
			 //			else if (!isSettSel(back_selezionato)){
			 //				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.selezionaSettimanaCaricata"));
			 ////		        new it.pisa.caribel.swing2.cariInfoDialog(null,"Occorre selezionare una settimana già caricata!","Attenzione!").show();
			 //		        return;
			 //		     }
			prepareHashtable();
			//FIXME VFR Spostamento operatore non ancora completato
			registra = Executions.getCurrent().createComponents(AgendaModOperatoreFormCtrl.myPathZul, self, daSalvare);
			daSalvare=new Hashtable();
		}
		
		public void onCalendar(Event event) throws Exception{
			Calendar myCalendar = Calendar.getInstance();
			myCalendar.setTime(cal.getValue());
			int year = myCalendar.get(Calendar.YEAR);
			int mese = myCalendar.get(Calendar.MONTH);
			myCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			List settimane = getNumberOfWeeks(year, mese);
			Boolean presente = false;
			tbx.setValue("");
			ArrayList<String> lista = new ArrayList(settimane.size());
			for (Iterator iterator = settimane.iterator(); iterator.hasNext();) {
				List settimana = (List) iterator.next();
				presente = settimanaCaricata.contains(settimana.get(0));
				if(presente){
					tbx.setValue(tbx.getValue()+ UtilForUI.getDateForUI(format.parse((String) settimana.get(0))) + "-" + UtilForUI.getDateForUI(format.parse((String) settimana.get(1))) + "\n");
					lista.add(UtilForUI.getDateForUI(format.parse((String) settimana.get(0))) + "-" + UtilForUI.getDateForUI(format.parse((String) settimana.get(1))));
				}
			}
			ListModelArray<String> model = new ListModelArray<String>(lista);
			caricate.setModel(model);
		}
}

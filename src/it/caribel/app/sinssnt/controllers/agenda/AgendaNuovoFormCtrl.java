package it.caribel.app.sinssnt.controllers.agenda;


import it.caribel.app.sinssnt.bean.AgendaEditEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.CaribelComparator;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.procdate;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Calendar;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

@SuppressWarnings({"rawtypes","unchecked"})
public class AgendaNuovoFormCtrl extends AgendaGenericPopupFormCtrl {

	private static final long serialVersionUID = 5616844654492008468L;
	public static final String myPathZul = "/web/ui/sinssnt/agenda/agendaNuovoForm.zul";
	protected String keyPermission = "AG_SPO";


	private int stato_form=0;
	protected Hashtable hashDapassare =new Hashtable();
	
	protected int ultimo_y=0;
	protected String dataOdierna=procdate.getitaDate();
	private Label lbl_selezionaPrestazioniErogate;
	private Listbox tablePrestazioni;
	private Button btn_confermaSelezione;

	private AgendaEJB agendaEJB = new AgendaEJB();
	private AgendaEditEJB agendaEditEJB = new AgendaEditEJB();

	private Component assistito;
	protected CaribelTextbox n_cartella;
//	protected CaribelDatebox int_data;
	protected CaribelCombobox cbx_contatto;
	private CaribelSearchCtrl assistitoCtrl;
	private CaribelRadiogroup ag_orario;
	private Hashtable<String,Object> prestazioniDaPiano = new Hashtable<String, Object>();
	protected Bandbox my_bandbox;
	protected Calendar mydate;
	protected CaribelForwardComposer ctrl = null;// = (CaribelForwardComposer) parent.getAttribute(MY_CTRL_KEY);
	
	@Override
	public void doInitForm() {
		try {
			super.doInitForm();
			
			int numeroFasce = CostantiAgendaBase.NUMFASCIEORARIE;
			List<Component> fasce =  ag_orario.getChildren();
//			for (int i = 0; i < fasce.size(); i++) {
			if(fasce!=null && fasce.size()>0){
				fasce.removeAll(fasce);
			}
//			(Component iterable_element : ag_orario.getChildren()) {
//				ag_orario.removeChild(iterable_element);	
//			}
			for (int i = 0; i < numeroFasce; i++) {
				Radio radio = new Radio(Labels.getLabel("agenda.fasce."+i));
				radio.setValue(i+"");
				ag_orario.appendChild(radio);
			}
			ag_orario.setSelectedIndex(0);
			CaribelListModel emptyModel = new CaribelListModel();
			emptyModel.setMultiple(true);
			tablePrestazioni.setModel(emptyModel);

			logger.info("AgendaNuovoFormCtrl/Inizializzazione arg in input: " + arg.toString());
			assistitoCtrl = ((CaribelSearchCtrl) assistito.getAttribute(MY_CTRL_KEY));
			setDefault();
			Object o = arg.get("stato_interv");
			stato_form = o!= null ? (Integer) o : 0;
			if (stato_form!=0){
//				lbl_selezionaPrestazioniErogate.setValue(Labels.getLabel("agendaRegistraPopup.prestazioniErogate"));
				UtilForComponents.disableListBox(tablePrestazioni, true);
				UtilForComponents.disableListBox(tableGrigliaPrestazioni, true);
				btn_confermaSelezione.setDisabled(true);
			}else{
				UtilForComponents.disableListBox(tablePrestazioni, false);
				UtilForComponents.disableListBox(tableGrigliaPrestazioni, false);
				btn_confermaSelezione.setDisabled(false);
			}
			hashDapassare.putAll(arg);
			if (stato_form!=0){
				this.setReadOnly(true);
//				lbl_selezionaPrestazioniErogate.setValue(Labels.getLabel("agendaRegistraPopup.prestazioniErogate"));
			} else {
				doCheckPermission();
			}
			execSelectPrestazioni();

			n_cartella.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					caricaCombo();
					return;
				}});
			
			if(arg.containsKey("chiamante")){
				ctrl = (CaribelForwardComposer) arg.get("chiamante");
			}
		} catch (Exception e) {
			doShowException(e);
		}

	}
	
	public void onChange$cbx_contatto(Event e) throws Exception{
		execSelectPiano();
		execSelectPrestazioni();
//		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
		my_bandbox.setDisabled(cbx_contatto.getSelectedIndex()>0);
		cbx_contatto.setDisabled(cbx_contatto.getSelectedIndex()==0);
//		}
	}

	private void setDefault() {
		cbx_contatto.clear();
//		this.jCariTablePrestaz.deleteAll();
		abilitaAss(true);
		n_cartella.setValue(null);
		Events.sendEvent(Events.ON_CHANGE, n_cartella, "");
		if(arg.containsKey("giorno")){
//			int_data.setValue((Date) arg.get("giorno"));
			mydate.setValue((Date) arg.get("giorno"));
			my_bandbox.setValue(UtilForBinding.getStringClientFromDate((Date) arg.get("giorno")));
		}else{
//			int_data.setValue(null);
			my_bandbox.setValue("");
		}
	}

	private void abilitaAss(boolean abilita) {
//		int_data.setDisabled(!abilita);
//		int_data.setReadonly(!abilita);
		my_bandbox.setDisabled(!abilita);
		assistitoCtrl.setReadonly(!abilita);
	}

	private int execSelectPrestazioni() throws Exception{
//		CaribelListModel newModel = new CaribelListModel();
		modelloPrestazioni = new CaribelListModel();
		modelloPrestazioni.setMultiple(true);
		tableGrigliaPrestazioni.setModel(modelloPrestazioni);
		//griglia prestazioni
		Hashtable h = new Hashtable();
		//h.put("tipo_oper",profile.getParameter("tipo_operatore"));
		Vector griglia=new Vector();
		h.put("referente", hashDapassare.get("referente"));
		h.put("JFrameAgendaNuovoAssistito","JFrameAgendaNuovoAssistito");

		Object obj=profile.getObject((String)hashDapassare.get("tipo_operatore")+"_prestazioniRefer"); 
		//profile.getObject((String)hashDapassare.get("referente")+"_prestazioniRefer");
		if(obj != null){
			griglia=(Vector)obj;
		}else{
			Object o= invokeGenericSuEJB(agendaEJB, h, "CaricaTabellaPrestazioni");//db.CaricaPrest(h);
			if (o!=null){
				if(((Vector)o).size()<=0){
					UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
					//	                new it.pisa.caribel.swing2.cariInfoDialog(null,"Non esistono prestazioni!","Attenzione!").show();
					return -1;
				}
				else griglia=(Vector)o;
			}
			//logger.info("REGISTR put profile"+(String)hashDapassare.get("referente")+"_prestazioniRefer");
			profile.putParameter((String)hashDapassare.get("tipo_operatore")+"_prestazioniRefer",griglia);
		}
		CaribelComparator comp = new CaribelComparator("prest_des");
		comp.setAscendingOrder(true);
		Set<Hashtable> daPiano = new TreeSet<Hashtable>(comp);

		Enumeration en=griglia.elements();
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			Hashtable ht=is.getHashtable();
			String codicePrestazione = (String) ht.get("prest_cod");
			//logger.info("carico pannello in hash=="+ht.toString());
			if(!prestazioniDaPiano.containsKey(codicePrestazione)){
				caricaTabPrestazioni(ht);
			}else{
				//				prestazioniDaPiano.put(codicePrestazione,ht);
				daPiano.add(ht);
			}
		}
		modelloPrestazioni.addAll(0, daPiano);
		modelloPrestazioni.setSelection(daPiano);

		//	        this.JCariTableGrigliaPrestaz.setColumnSizes();
		return 1;
	}
	
	private int execSelectPiano() throws Exception {
		if(prestazioniDaPiano.isEmpty()){
			prestazioniDaPiano= new Hashtable<String, Object>();
			Hashtable h = new Hashtable<String, String>();		
			h.put("pa_tipo_oper", arg.get("tipo_operatore"));
			h.put("n_cartella", n_cartella.getValue());
			
			String valCombo=cbx_contatto.getSelectedValue();
			StringTokenizer str=new StringTokenizer(valCombo,"#");
//			int k=str.countTokens();
			while (str.hasMoreTokens()) {
				h.put("n_progetto",str.nextToken());
				h.put("cod_obbiettivo",str.nextToken());
				h.put("n_intervento",str.nextToken());
			}
//			if(ManagerProfile.isConfigurazioneMarche(getProfile())){
				h.put("data_agenda", UtilForBinding.getValueForIsas(UtilForBinding.getDateFromClient(my_bandbox.getValue().substring(0, 10))));
//			}else{
//				h.put("data_agenda", int_data.getValueForIsas());
//			}
			Object o= new PianoAssistEJB().query_pianoAcc(CaribelSessionManager.getInstance().getMyLogin(), h );
			Vector griglia=new Vector();
			if (o!=null){
				if(((Vector)o).size()<=0){
					//				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
					return -1;
				}
				else griglia=(Vector)o;}
			Enumeration en=griglia.elements();
//			int i=0;
			while(en.hasMoreElements()){
				ISASRecord is =(ISASRecord)en.nextElement();
				h=is.getHashtable();
				String codice = (String) h.get("pi_prest_cod");
				prestazioniDaPiano.put(codice, codice);		
			}
		}
		return 0;
	}
	
	
	//carico tabella prestaz
	private int caricaTabPrestazioni(Hashtable hash){
		// TODO VFR controllare il cambio
//		((CaribelListModel) tableGrigliaPrestazioni.getModel()).add(hash);
		modelloPrestazioni.add(hash);
		
		//		StringBuffer st = new StringBuffer();
		//		String chiavi = cariObjectTableModel1.getColumnDB(sepChar);
		//		StringTokenizer rowChiavi = new StringTokenizer(chiavi,sepChar);
		//		while(rowChiavi.hasMoreTokens()){
		//			String rowItem = rowChiavi.nextToken();
		//			if(hash.containsKey(rowItem)){
		//				if(((String)hash.get(rowItem)).equals(""))
		//					st.append(" ");
		//				else
		//					st.append((String) hash.get(rowItem));
		//			}else {
		//				//la tabella non contiene la chiave
		//				st.append(" ");
		//			}
		//			if (rowChiavi.hasMoreTokens())
		//				st.append(sepChar);
		//		}//end while
		//		cariObjectTableModel1.addRow(st.toString());
		//		//logger.info("carico su griglia:"+st.toString());
		return 1;
	}//end caricaPrest

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}
	public int ControlloData (String dataold,String datanew)
	{
		// controlla se dataold < datanew
		//preparazione primo array
		int[] datavecchia= new int[3];

		Integer giorno = new Integer(dataold.substring(0,2));
		datavecchia[0]= giorno.intValue();
		Integer mese = new Integer(dataold.substring(3,5));
		datavecchia[1]= mese.intValue();
		Integer anno = new Integer(dataold.substring(6,10));
		datavecchia[2]= anno.intValue();
		//preparazione secondo array
		int[] datanuova= new int[3];

		Integer day = new Integer(datanew.substring(0,2));
		datanuova[0]= day.intValue();
		Integer mounth = new Integer(datanew.substring(3,5));
		datanuova[1]= mounth.intValue();
		Integer year = new Integer(datanew.substring(6,10));
		datanuova[2]= year.intValue();

		//confronto anno

		if (datanuova[2] < datavecchia[2])
		{
			//new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
			return -1;
		}
		else
			if (datanuova[2] == datavecchia[2])

				//confronto mes
				if (datavecchia[1] > datanuova[1])
				{
					//  new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
					return -1;
				}
				else
					if (datanuova[1] == datavecchia[1])
						//confronto giorno
						if (datanuova[0] < datavecchia[0])
						{
							//  new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
							return -1;
						}
		return 1;

	}
	public void onConfermaSelezione(Event event) throws Exception{
		int ret =this.caricaPrestazioniSelezionate();
		if(ret==-1){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniOperatore"));
			return;
		}
		else if (ret==-2){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
			return;
		}
		tableGrigliaPrestazioni.clearSelection();
		tablePrestazioni.invalidate();
	}
	public int caricaPrestazioniSelezionate(){
		ListModel<Object> modello = tableGrigliaPrestazioni.getModel();
		CaribelListModel modelloGriglia = (CaribelListModel) tablePrestazioni.getModel();
		if (modello.getSize() == 0)
			return -1;
		boolean trovato=false;
		//		for(int k=0; k< modello.getRowCount(); k++){
		//			int col=modello.getColumnLocationByName("pr_sel");
		//			boolean valore = ((Boolean)modello.getValueAt(k, col)).booleanValue();
		//			if(valore){
		//				modello.setValueAt(new Boolean(false),k,col);
		//				//(jCariTablePrestaz.getModel()).setValueAt(new Boolean(b) ,i,0);
		for (Iterator iterator = tableGrigliaPrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			trovato=true ;
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			if (itemGrid instanceof Hashtable) {
				//				int colonnaCodice=modello.getColumnLocationByName("prest_cod");
				//				String cod_prest = (String)modello.getValueAt(k, colonnaCodice);
				String cod_prest = (String) ((Hashtable) itemGrid).get("prest_cod");
				/*Controllo se e' gia' presente nella griglia un record con lo stesso codice prestazione
	                  ed eventualmente devo aumentare la quantità
				 */
				Hashtable hNuovo=new Hashtable() ;
				hNuovo.put("ap_prest_cod",cod_prest.trim());
				//				int num_riga=cariObjectTableModelPrestaz.columnsContains(hNuovo);
				int num_riga = modelloGriglia.columnsContains(hNuovo);

				//int quantita=1;
				//int Col_quantita_nuova=modello.getColumnLocationByName("pre_numero");
				int quantita=1;//Integer.parseInt((String)modello.getValueAt(k, Col_quantita_nuova));
				String frequenza=" ";
				String ap_alert=" ";
				if (num_riga!=-1){
					//l'ho inserito devo aumentare la quantità
					//aggiorno tutto
					//					int Col_quantita_vecchia=//cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_qta");
					//					String qta=(String)cariObjectTableModelPrestaz.getValueAt(num_riga, Col_quantita_vecchia);
					//					if((qta.trim()).equals(""))qta="1";
					int quant_vecchia= (Integer) modelloGriglia.getFromRow(num_riga, "ap_prest_qta"); //Integer.parseInt(qta);
					quantita=quant_vecchia+quantita;
					frequenza=(String) modelloGriglia.getFromRow(num_riga, "frequenza");//cariObjectTableModelPrestaz.getValueAt(num_riga,cariObjectTableModelPrestaz.getColumnLocationByName("frequenza"));
					ap_alert=(String) modelloGriglia.getFromRow(num_riga, "ap_alert");//cariObjectTableModelPrestaz.getValueAt(num_riga,cariObjectTableModelPrestaz.getColumnLocationByName("frequenza"));
				}
				//  Questo perché se la frequenza è una stringa vuota allora la concatenazione che segue
				//  produrrebbe una riga con due separatori contigui.
				if (frequenza.equals(""))
					frequenza=" ";
				//gb 18/09/07: fine *******
				Hashtable<String, Object> hDati = (Hashtable<String, Object>) itemGrid;
				hDati.put("ap_prest_qta", quantita);
				hDati.put("frequenza", frequenza);
				hDati.put("ap_alert", ap_alert);
				hDati.put("ap_prest_cod", cod_prest);
				hDati.put("ap_prest_desc", (String) ((Hashtable) itemGrid).get("prest_des"));
				
//				String  riga="true"+"##"+cod_prest+"##"+
//						(!(((String)modello.getValueAt(k,modello.getColumnLocationByName("prest_des"))).equals(""))?
//								(String)modello.getValueAt(k,modello.getColumnLocationByName("prest_des")):" ")+
//								"##"+frequenza+"##"+quantita+"##"+"0##";
				if (num_riga!=-1){
					modelloGriglia.remove(num_riga);
					modelloGriglia.add(num_riga, hDati);
					//					cariObjectTableModelPrestaz.removeRow(num_riga);
					//					cariObjectTableModelPrestaz.insertRowAt(riga,num_riga);
					modelloGriglia.addToSelection(modelloGriglia.get(num_riga));
				}else{
					modelloGriglia.add(hDati);
					modelloGriglia.addToSelection(modelloGriglia.get(modelloGriglia.size()-1));
					//					cariObjectTableModelPrestaz.insertRowAt(riga,cariObjectTableModelPrestaz.getRowCount());
				}
			}
		}
		if(trovato)
			return 1;
		else
			return -2;
	}
	
	@Override
	protected boolean doSaveForm() throws Exception {
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
		
		
		int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
		if(num==0){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
			return false;
		}
		Vector v=new Vector();
		Vector q=new Vector();
		Vector f=new Vector();
//		int col_qta=cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_qta");
//		int col_pr=cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_cod");
//		
		for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			v.addElement((String) ((Hashtable)itemGrid).get("ap_prest_cod"));
//				v.addElement((String)cariObjectTableModelPrestaz.getValueAt(i,col_pr));
				Integer qta = (Integer) ((Hashtable)itemGrid).get("ap_prest_qta");//((String)cariObjectTableModelPrestaz.getValueAt(i,col_qta));
				if(qta == null)q.addElement("1");
				else q.addElement(qta.toString());
				f.addElement(((Hashtable)itemGrid).get("ap_alert"));
				//logger.info("carico nel vettore="+check.JCariTextFieldCodPrest.getUnmaskedText());
		}
		//logger.info("vettore="+v.toString());

	       if(v.size()==0){
	   			self.detach();
	     		return false;
	       }
		
		this.hashDapassare.put("prestazioni",v);
		this.hashDapassare.put("quantita",q);
        this.hashDapassare.put("frequenza",f);

		hashDapassare.put("ag_stato","0");
//		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			hashDapassare.put("ag_data", my_bandbox.getValue());
//		}else{
//			hashDapassare.put("ag_data", int_data.getValueForIsas());
//		}
		String valCombo=cbx_contatto.getSelectedValue();
		StringTokenizer str=new StringTokenizer(valCombo,"#");
		int k=str.countTokens();
		while (str.hasMoreTokens()) {
			hashDapassare.put("ag_contatto",str.nextToken());
			hashDapassare.put("cod_obbiettivo",str.nextToken());
			hashDapassare.put("n_intervento",str.nextToken());
		}
		hashDapassare.put("ag_cartella", n_cartella.getValue());
		hashDapassare.put("ag_orario", ag_orario.getSelectedValue().toString());
		logger.info("Registro hashtable="+hashDapassare.toString());
		
		Object o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_intervCella"); //db.registraPrest(hashDapassare);
//		((AgendaRegistraFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
		self.detach();
		
	    //alla chiusura del registra aggiorno la griglia sottostante
//		Component parent = self.getParent();
		if(ctrl!=null){
			if(ctrl instanceof AgendaRegistraNewFormCtrl){
				try {
					((AgendaRegistraNewFormCtrl) ctrl).execSelect();
				} catch (Exception e) {
					doShowException(e);
				}
			}
		}

		return true;
	}
	
	public void onCaricaCombo(Event event) throws Exception {
			caricaCombo();
	}
	
	private void caricaCombo() throws Exception {
		if(n_cartella.getValue()==null || n_cartella.getValue().isEmpty() || 
				(//int_data.getValue()==null && 
						my_bandbox.getValue().isEmpty())) {
			logger.info("valorizzare data e cartella");
			return;
		}
		logger.info("eseguo caricamento combo");

		cbx_contatto.clear();//cariStringComboBoxModelContatti.removeAllElements();
		cbx_contatto.setDisabled(false);
		Hashtable h = new Hashtable();
		Vector griglia = new Vector();
		h.put("tipo_operatore", hashDapassare.get("tipo_operatore"));
//		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			h.put("ag_data", my_bandbox.getValue());
//		}else{
//			h.put("ag_data", int_data.getValueForIsas());
//		}
		h.put("ag_cartella", n_cartella.getValue());
		Object o = invokeGenericSuEJB(agendaEditEJB, h, "combo_contatti");//db.caricaContatti(h);
		if (o != null) {
			if (((Vector) o).size()> 0) {
				griglia = (Vector) o;
				caricaComboContatti(griglia);
				abilitaAss(false);
				return;
			}
		}
		UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.noContatti"));
		//new it.pisa.caribel.swing2.cariInfoDialog(null,	"Non esistono contatti !", "Attenzione!").show();
		caricaComboContatti(new Vector());
		abilitaAss(false);
	}
	
	private void caricaComboContatti(Vector v){
		//  logger.info("HASH DATI-->>"+ hDati.toString() );
		int k=1;
//		String[] myValue = new String[v.size()+1];
//		myValue[0]="0#"+ "00000000"+"#" +"0";//"0";
//		cariStringComboBoxModelContatti.add(0,"Accesso Occasionale") ;
		CaribelComboRepository.addComboItem(cbx_contatto, "0#"+ "00000000"+"#" +"0", Labels.getLabel("agenda.contatto.accessoOccasionale"));
		if(v.size()>0)
			for(int i=1; i<=v.size(); i++) {
				try{
					it.pisa.caribel.isas2.ISASRecord r = (it.pisa.caribel.isas2.ISASRecord) v.elementAt(i-1);
					String elementoDec = "" + r.get("descr");
					logger.info("caricaComboContatti r==>"+r.getHashtable().toString());
					String valore = "" + r.get("n_contatto")+"#"+ r.get("cod_obbiettivo")+"#" +r.get("n_intervento");
//					cariStringComboBoxModelContatti.add(k,elementoDec) ;
					logger.info("VALORE-->"+k+" "+valore +" "+elementoDec);
//					myValue[k] = valore;
//					k++;
					CaribelComboRepository.addComboItem(cbx_contatto, valore, elementoDec);
				} catch(Exception e) {
					cbx_contatto.clear();//cariStringComboBoxModelContatti.removeAllElements();
					UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.errore.caricamentoContatti"));
//					new it.pisa.caribel.swing2.cariInfoDialog(null,"Errore nel caricamento dei contatti!","Attenzione!" ).show();
					break;}
			}//fine for
//		jCariComboBoxContatto.setMyvalues(myValue);
//		jCariComboBoxContatto.setMaximumRowCount(k);
	}

}

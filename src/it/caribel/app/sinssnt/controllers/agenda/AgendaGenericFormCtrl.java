package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.sinssnt.bean.AgendaRegistraEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForGridRenderer;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.dateutility;
import it.pisa.caribel.util.procdate;

import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;

@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
public class AgendaGenericFormCtrl extends CaribelFormCtrl{

	private CaribelTextbox 	cod_operatore;
	private CaribelCombobox 	desc_operatore;

	protected String myKeyPermission = "";

	protected AgendaEJB myEJB = new AgendaEJB();
	protected ISASUser iu;

	protected final int WAIT = 0;
    protected final int INSERT = 1;
    protected final int UPDATE_DELETE = 2;
    protected final int CONSULTA = 3;

    protected Hashtable hashDaPassare=new Hashtable();

	protected CaribelListbox caribellbAgendaAssistito;
	public CaribelListModel modelloAgenda;
	public int x, y, stat;
	protected CaribelTextbox riepilogoAltroAssistito;

	protected String statoPian;
	Vector<String> vMat = new Vector<String>();
	Vector<String> vPom = new Vector<String>();
	Vector<String> vMat_rm = new Vector<String>();
	Vector<String> vPom_rm = new Vector<String>();

	protected String[][] key_data;
	protected String[][] key_progr;
	protected String[][] key_stato;

	protected Hashtable HProgToMove=new Hashtable();
	protected Hashtable reg_interv=new Hashtable();

	protected String dataOdierna=procdate.getitaDate();
	protected boolean firsttime=true;

	protected Checkbox jCheckBox1;
	protected Checkbox jCheckBox2;
	protected Checkbox jCheckBox3;
	protected Checkbox jCheckBox4;
	protected Checkbox jCheckBox5;
	protected Checkbox jCheckBox6;
	protected Checkbox jCheckBox7;
	protected Checkbox jCheckBox12;
    
	protected Checkbox selTutti;
	
	protected Checkbox jCheckBoxInteraSettimana;
    
	protected static final String classForRequired = "selectedDayClass";
	protected static final String classForAssistito = "selectedAssistitoClass";
	protected static final String classForMove = "selectedToMoveClass";
	
	protected String[] mesi = { "Gennaio", "Febbraio", "Marzo", "Aprile",
            "Maggio", "Giugno", "Luglio", "Agosto",
            "Settembre", "Ottobre", "Novembre", "Dicembre"
              };
	protected dateutility dt=new dateutility();
	protected Label meseAnno;
	protected boolean noModificheNelFuturo = false;
	protected boolean alertNoPianificazone = false;
	protected int ySel = -1;
	protected Object assistitoRitorno;
	protected String[][] key_contatori;

	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query_agenda");
			super.setMethodNameForInsert("insert_agenda");
			super.setMethodNameForUpdate("update_agenda");
			
			hashDaPassare.putAll(arg);
//			hashDaPassare.put("esecutore", value)
			caribellbAgendaAssistito.setItemRenderer(new AgendaGridItemRenderer());
			
			caribellbAgendaAssistito.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					mostraAssistito();
				}});

			if(selTutti!= null){
				selTutti.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (selTutti.isChecked())
							selezionaTuttiAssistiti(true);
						else 
							selezionaTuttiAssistiti(false);
					}});
			}
			
			execSelect();
			
			doFreezeForm();
			iu = CaribelSessionManager.getInstance().getIsasUser();	
			riepilogoAltroAssistito.focus();
		}catch(Exception e){
			doShowException(e);
		}
	}

	protected void mostraAssistito() {
		mostraAssistito(-1);
	}
	protected void mostraAssistito(int i) {

		int x = i!=-1? i: caribellbAgendaAssistito.getSelectedIndex();
		//        int y=  JCariTableAgenda.getColumnLocationByName("assistito");
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
		Component comp = event.getOrigin().getTarget();
		Object o = comp.getAttribute("dbName", true);
		CaribelListModel model = (CaribelListModel) caribellbAgendaAssistito.getModel();
		int x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
//		int y = JCariTableAgenda.getSelectedColumn();

		setAssistitoRitorno(((Hashtable) ((Listitem) comp.getParent()).getAttribute(UtilForGridRenderer.HT_FROM_GRID)).get("ag_cartella"));

		int stat=0;
		if(((String)o).length()!=1) return; //	if(y<2)return;
		int y = Integer.parseInt((String)o);
		logger.info("Apro il dettaglio di:"+ x +" - "+y);
		if(key_stato[x][y].equals("1")){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoIncompleto"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"L'accesso, con stato incompleto, è chiuso! \n Puoi solo visionare l'elenco delle prestazioni!","Attenzione scelta non valida!").show();
			stat=1;
		}else if(key_stato[x][y].equals("2")){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoChiuso"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"L'accesso, con stato completo, è chiuso! \n Puoi solo visionare l'elenco delle prestazioni!","Attenzione scelta non valida!").show();
			stat=2;
		}
		String valore = ((String) model.getFromRow(x,(String) o));
		logger.trace("in mouseclick valor="+valore);
		Hashtable<String, Object> h = new Hashtable<String, Object>();
		if (valore == null || valore.trim().equals("")||key_stato[x][y].equals("")){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.cellaNoPrestazioni"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"La cella selezionata non contiene prestazioni!","Attenzione scelta non valida!").show();
			return;
		}
		h.put("ag_progr",key_progr[x][y]);
		h.put("ag_data",key_data[x][y]);
		h.put("referente",(String)hashDaPassare.get("referente"));
		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		h.put("esecutivo",(String)hashDaPassare.get("esecutivo"));
		h.put("ag_contatto",((Integer)model.getFromRow(x,"ag_contatto")).toString());
//		gb 17/09/07 *******
		h.put("cod_obbiettivo",(String)model.getFromRow(x,"cod_obbiettivo"));
		h.put("n_intervento",((Integer)model.getFromRow(x,"n_intervento")).toString());
//		gb 17/09/07: fine *******
		h.put("ag_cartella",((Integer)model.getFromRow(x,"ag_cartella")).toString());
		h.put("assistito",(String)model.getFromRow(x,"assistito"));
		h.put("ag_orario",(String)model.getFromRow(x,"matt_pom"));
		h.put("stato_interv", stat);
		logger.trace("Call PopUp..."+h.toString());

	    Executions.getCurrent().createComponents(AgendaRegistraPopUpFormCtrl.myPathZul, self, h);
	    if(stat==1){
	    	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoIncompleto"));
	    }else if(stat==2){
	    	UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoChiuso"));
	    }
//		JFrameAgendaRegistraPopUp jfgop=new JFrameAgendaRegistraPopUp(this,h,stat);
	}
	
	public void onCheckCell(ForwardEvent event) throws Exception {
		Component comp = event.getOrigin().getTarget();
		Set tmp = new HashSet(caribellbAgendaAssistito.getSelectedItems());
		int count = tmp.size();
		if(((Checkbox) comp).isChecked()){
			tmp.add((Listitem) comp.getParent().getParent());
		}else{
			tmp.remove((Listitem) comp.getParent().getParent());
			selTutti.setChecked(false);
		}
		count = tmp.size();
		caribellbAgendaAssistito.clearSelection();
		caribellbAgendaAssistito.setSelectedItems(tmp);
		onSelect$caribellbAgenda(null);
	}
	
	public void onClickedCell(ForwardEvent event) throws Exception {
		Component comp = event.getOrigin().getTarget();
		int x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
		mostraAssistito(x);
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
	
	protected int execSelect() throws Exception {
		meseAnno.setValue(hashDaPassare.get("mese") + " - " + hashDaPassare.get("anno"));
		caribellbAgendaAssistito.setModel(new CaribelListModel());
		
		int count = 0;
		logger.info("JFrameAgendaPianSett **** HASH che invio per selezAgenda..."+hashDaPassare.toString());
		Object o= invokeGenericSuEJB(myEJB, hashDaPassare, "query_agenda"); //db.selezAgenda(hashDaPassare);
		Vector griglia=new Vector();
		if(o==null){
			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.errore"));
			return -1;
		}else{
			if(((Vector)o).size()<=0 && alertNoPianificazone ){
				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.noPianificazione"));
				return -1;
			}else{
				griglia=(Vector)o;
				count=griglia.size();
			}
		}
		
		int totrighe=0;
		if(griglia!=null){ 
			totrighe=griglia.size();
		}
		key_data=new String[totrighe*CostantiAgenda.NUMFASCIEORARIE][7];
		key_progr=new String[totrighe*CostantiAgenda.NUMFASCIEORARIE][7];
		key_stato=new String[totrighe*CostantiAgenda.NUMFASCIEORARIE][7];
		key_contatori = new String[2][7];
		Enumeration en=griglia.elements();
		String chiave_new="";
		String chiave_old="";
		int riga = 0;
		Hashtable h;
		Hashtable[] righeAssistito = new Hashtable[CostantiAgenda.NUMFASCIEORARIE];
		boolean first = true;
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			h=is.getHashtable();
			//gb 30/08/07          chiave_new=((Integer)h.get("n_cartella")).toString()+"-"+((Integer)h.get("n_contatto")).toString()+"-"+formDate(h.get("as_data"),"gg/mm/aaaa");
			chiave_new = ((Integer)h.get("ag_cartella")).toString() + "-" +
					((Integer)h.get("ag_contatto")).toString() + "-" +
					(String)h.get("cod_obbiettivo") + "-" +
					((Integer)h.get("n_intervento")).toString();
			riga = ((Integer)h.get("ag_orario")).intValue();
			if (chiave_new.equals(chiave_old)){
				righeAssistito[riga] = h;
			}else{
				caricaRighe(righeAssistito);
				righeAssistito = new Hashtable[CostantiAgenda.NUMFASCIEORARIE];
				righeAssistito[riga] = h;
				chiave_old=chiave_new;
			}
			if(first){
				for (int i = 0; i < 7; i++) {
					key_contatori[0][i] = is.get("numprestazioni"+i)+"";
					key_contatori[1][i] = is.get("numprelievi"+i)+"";
				}
				first=false;
			}
		}
		caricaRighe(righeAssistito);
		
		((CaribelListModel) caribellbAgendaAssistito.getModel()).setMultiple(true);
		settaHeaderTabella();

		return count;
	}

	protected void settaHeaderTabella() {
		//Modifico le label degli header delle colonne
		caribellbAgendaAssistito.getHeads();
		Listhead testa = caribellbAgendaAssistito.getListhead();
		String dbName, titolo;
		Vector v=(Vector) hashDaPassare.get("giorni");
		
		for (Object header : testa.getChildren()) {
			dbName = ((CaribelListheader)header).getDb_name();
			
			try {
				int i = Integer.parseInt(dbName);
				((CaribelListheader)header).setLabel(Labels.getLabel("agendaPianSett.sett."+i)+" "+((String) v.get(i)).substring(0, 2));
			} catch (NumberFormatException e) {
			}
		}
		//finemodifica headers
	}

	private void caricaRighe(Hashtable[] righeAssistito) {
		boolean found = false;
		Hashtable perRigheVuote = null;
		//cerco se è valorizzata almeno una riga
		int i = 0;
		for (; i < righeAssistito.length && !found; i++) {
			found = found || righeAssistito[i]!=null;
		}
		if(!found){
			//se non ho da visualizzare nulla ritorno
			return;
		}
//		int presente[] = new int[7];
		Hashtable presente = new Hashtable();
		Hashtable h;
		for (int k = 0; k < righeAssistito.length; k++) {
			h = righeAssistito[k];
			for (int j = 0; j < 7; j++) {
				if(h!=null && h.containsKey(j+"")){
					presente.put("o"+j, 1);
				}
			}
		}
		
		//mi salvo una riga sicuramente piena come template per generare le righe vuote 
		perRigheVuote = righeAssistito[--i];
		perRigheVuote.put("presente", presente);
		//ciclo su tutte le fasce orarie
		for (i = 0; i < righeAssistito.length; i++) {
			h = righeAssistito[i];
			if(h == null){
				//carico la riga vuota
				caricaRigaVuota(perRigheVuote, caribellbAgendaAssistito.getModel().getSize());

			}else{
				h.putAll(presente);
				//carico una riga nella griglia con i valori di h
				caricaRiga(h);
			}
			
		}
			
	}

	protected	String formDate(Object dataI,String formato){
		return formDate(dataI,formato,false);//false =>	aaaammgg
	}
	
	protected	String formDate(Object dataI,String formato,boolean it){
		String giorno="00";
		String mese="00";
		String anno="0000";
		String data="";
		if(dataI!=null){
			if(dataI instanceof java.sql.Date) data=((java.sql.Date)dataI).toString();
			else if(dataI instanceof String) data=(String)dataI;
		}
		//it=false =>	aaaammgg
		//it=true	=> ggmmaaaa
		if(data!=null	&& !data.equals("")){

			if(data.charAt(2)=='-' ||data.charAt(2)=='/')//gg-mm-aaaa
			{
				giorno=data.substring(0,2);
				mese=data.substring(3,5);
				anno=data.substring(6,10);
			}else	 if(data.charAt(4)=='-'	||data.charAt(4)=='/')//aaaa-mm-gg
			{
				giorno=data.substring(8,10);
				mese=data.substring(5,7);
				anno=data.substring(0,4);
			}else	//ggmmaaaa aaaammgg
			{	if(it==false)//aaaammgg
			{
				giorno=data.substring(6,8);
				mese=data.substring(4,6);
				anno=data.substring(0,4);
			}
			else//ggmmaaaa
			{
				giorno=data.substring(0,2);
				mese=data.substring(2,4);
				anno=data.substring(4,8);
			}
			}
		}
		if(formato.equals("gg-mm-aaaa"))
			return giorno+"-"+mese+"-"+anno;
		else if(formato.equals("aaaa-mm-gg"))
			return anno+"-"+mese+"-"+giorno;
		else if(formato.equals("ggmmaaaa"))
			return giorno+mese+anno;
		else if(formato.equals("aaaammgg"))
			return anno+mese+giorno;
		else if(formato.equals("gg/mm/aaaa"))
			return giorno+"/"+mese+"/"+anno;
		else if(formato.equals("aaaa/mm/gg"))
			return anno+"/"+mese+"/"+giorno;
		else return data;
	}
	
	protected void casoRigaVuota(Hashtable ht){
		int val=((Integer)ht.get("ag_orario")).intValue();
		int riga=0;
		int numRighe=0;
		if(val==0){
			ht.put("ag_orario",new Integer(1));
			//   riga=1;
			numRighe=caribellbAgendaAssistito.getItemCount(); //this.cariStringTableModelAgenda.getNumRows();
			riga=numRighe;
			caricaRigaVuota(ht,numRighe);
			for (int k=0;k<7;k++){
				key_progr[numRighe][k]="";
				key_data[numRighe][k]=key_data[numRighe-1][k];
				key_stato[numRighe][k]="";
			}
		}else{
			ht.put("ag_orario",new Integer(0));
			numRighe=this.caribellbAgendaAssistito.getItemCount()-1;
//			Listitem st1=caribellbAgendaAssistito.getItemAtIndex(numRighe);
//			Object o = ((CaribelListModel) caribellbAgendaAssistito.getModel()).get(numRighe);
//			caribellbAgendaAssistito.removeItemAt(numRighe);
//			((CaribelListModel) caribellbAgendaAssistito.getModel()).remove(numRighe);
			caricaRigaVuota(ht,numRighe);
//			caricaRiga(ht);
//			((CaribelListModel) caribellbAgendaAssistito.getModel()).add(numRighe+1, o);
			for (int k=0;k<7;k++){
				key_progr[numRighe+1][k]=key_progr[numRighe][k];
				key_data[numRighe+1][k]=key_data[numRighe][k];
				key_stato[numRighe+1][k]=key_stato[numRighe][k];
				key_progr[numRighe][k]="";
				key_data[numRighe][k]=key_data[numRighe+1][k];//PROVA
				key_stato[numRighe][k]="";
			}
		}
	}

	protected void casoRigheVuote(Hashtable ht){
		int val=((Integer)ht.get("ag_orario")).intValue();
		int riga=0;
		int numRighe=0;
		if(val==0){
			ht.put("ag_orario",new Integer(1));
			//   riga=1;
			numRighe=caribellbAgendaAssistito.getItemCount(); //this.cariStringTableModelAgenda.getNumRows();
			riga=numRighe;
			caricaRigaVuota(ht,numRighe);
			for (int k=0;k<7;k++){
				key_progr[numRighe][k]="";
				key_data[numRighe][k]=key_data[numRighe-1][k];
				key_stato[numRighe][k]="";
			}
		}else{ 
			ht.put("ag_orario",new Integer(0));
			numRighe=this.caribellbAgendaAssistito.getItemCount()-1;
//			Listitem st1=caribellbAgendaAssistito.getItemAtIndex(numRighe);
//			Object o = ((CaribelListModel) caribellbAgendaAssistito.getModel()).get(numRighe);
//			caribellbAgendaAssistito.removeItemAt(numRighe);
//			((CaribelListModel) caribellbAgendaAssistito.getModel()).remove(numRighe);
			caricaRigaVuota(ht,numRighe);
//			caricaRiga(ht);
//			((CaribelListModel) caribellbAgendaAssistito.getModel()).add(numRighe+1, o);
			for (int k=0;k<7;k++){
				key_progr[numRighe+1][k]=key_progr[numRighe][k];
				key_data[numRighe+1][k]=key_data[numRighe][k];
				key_stato[numRighe+1][k]=key_stato[numRighe][k];
				key_progr[numRighe][k]="";
				key_data[numRighe][k]=key_data[numRighe+1][k];//PROVA
				key_stato[numRighe][k]="";
			}
		}
	}
	
	protected int caricaRigaVuota(Hashtable hash,int riga){
		//inserimento dati dai controlli alla griglia
		int count=0;
		Hashtable nuovaRiga = new Hashtable<String, String>();

		Enumeration keys = hash.keys();
		while(keys.hasMoreElements()){//rowChiavi.hasMoreTokens()){
			String rowItem = (String) keys.nextElement(); //rowChiavi.nextToken();
			if(hash.contains("ag_cartella") && hash.contains("ag_contatto") && hash.contains("assistito")){
				nuovaRiga.put(rowItem, ((Integer)hash.get("ag_cartella")).intValue()+"/"+
						((Integer)hash.get("ag_contatto")).intValue()+" -"+
						(String) hash.get("assistito"));
			}
			if(rowItem.equals("matt_pom")){
				int val=riga%CostantiAgenda.NUMFASCIEORARIE;//((Integer)hash.get("ag_orario")).intValue();
				nuovaRiga.put(rowItem,  Labels.getLabel("agenda.fasce."+val));
				nuovaRiga.put("ag_orario",  new Integer(val));
			}else if(rowItem.equals("valore")){
				nuovaRiga.put(rowItem, hash.get("assistito")+
						" cartella= "+hash.get("n_cartella")+
						"; contatto= "+hash.get("n_contatto")+
						"; obiettivo= "+hash.get("cod_obbiettivo")+ //gb 30/08/07
						"; intervento= "+hash.get("n_intervento")+ //gb 30/08/07
						"; piano del "+formDate(hash.get("as_data"),"gg/mm/aaaa"));
			}
//			if(hash.containsKey(rowItem)){
			if(rowItem.equals("assistito")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("indirizzo")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("ag_cartella")){
					nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("ag_contatto")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("cod_obbiettivo")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("n_intervento")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("x_assist")){
				nuovaRiga.put(rowItem, "false");
			}else if(rowItem.equals("ag_oper_ref")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("ag_oper_ref_desc")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("presente")){
				nuovaRiga.putAll((Hashtable) hash.get(rowItem));
			}
		}//end while
		int val=riga%CostantiAgenda.NUMFASCIEORARIE;//((Integer)hash.get("ag_orario")).intValue();
		nuovaRiga.put("matt_pom",  Labels.getLabel("agenda.fasce."+val));
		nuovaRiga.put("ag_orario",  new Integer(val));
//		caribellbAgenda.insertRowAt(st.toString(),riga);
		
		((CaribelListModel) caribellbAgendaAssistito.getModel()).add(riga, nuovaRiga);
		Vector v=(Vector)hashDaPassare.get("giorni");
		//aggiorno gli array dello stato
		for (int k=0;k<7;k++){
			key_progr[riga][k]="";
			if(nuovaRiga.get("data"+k)!=null && !((String)nuovaRiga.get("data"+k)).equals("")){
				key_data[riga][k]=(String)nuovaRiga.get("data"+k);
			}else{
				key_data[riga][k]=formattaData((String)v.get(k));
			}
			key_stato[riga][k]="";
		}
		return 1;
	}//end caricaRigaVuota

	protected int caricaRiga(Hashtable hash){
		//inserimento dati dai controlli alla griglia
		int count=0;
		hash.put("valore", hash.get("assistito")+
				" cartella= "+hash.get("n_cartella")+
				"; contatto= "+hash.get("n_contatto")+
				"; obiettivo= "+hash.get("cod_obbiettivo")+ //gb 30/08/07
				"; intervento= "+hash.get("n_intervento")+ //gb 30/08/07
				"; piano del "+formDate(hash.get("as_data"),"gg/mm/aaaa"));

		if(hash.containsKey("ag_orario")){
			int val=((Integer)hash.get("ag_orario")).intValue();
			hash.put("matt_pom", Labels.getLabel("agenda.fasce."+val));
		}
		
		((CaribelListModel) caribellbAgendaAssistito.getModel()).add(hash);
		int riga=((CaribelListModel) caribellbAgendaAssistito.getModel()).getSize()-1;
		Vector v=(Vector)hashDaPassare.get("giorni");
		for (int k=0;k<7;k++){
			if(hash.get("progr"+k)!=null && !((String)hash.get("progr"+k)).equals(""))
				key_progr[riga][k]=(String)hash.get("progr"+k);
			else key_progr[riga][k]="";
			if(hash.get("data"+k)!=null && !((String)hash.get("data"+k)).equals(""))
				key_data[riga][k]=(String)hash.get("data"+k);
			else key_data[riga][k]=formattaData((String)v.get(k));//prova
			if(hash.get("stato"+k)!=null && !((String)hash.get("stato"+k)).equals(""))
				key_stato[riga][k]=(String)hash.get("stato"+k);
			else key_stato[riga][k]="";
		}
		return 1;
	}//end caricaRiga
	
	protected String formattaData(String campo) {
		//restituisce la data nel formato aaaa-mm-gg
		String dataret="0000-00-00";
		String sep="-";
		if (campo!=null && !campo.equals("")) {
			String dataLetta=campo;
			if (dataLetta.length()==10) {
				String aaaa=dataLetta.substring(6,10);
				String mm=dataLetta.substring(3,5);
				String gg=dataLetta.substring(0,2);
				dataret=aaaa+sep+mm+sep+gg;
			}
		}
		//System.out.println("DATA FORMATTATA="+dataret);
		return dataret;
	}
	
//	protected void doFreezeForm() throws Exception{
//		tH = new CompareHashtable(UtilForBinding.getHashtableFromComponent(self));
////		hAccessi  = new CompareHashtable(UtilForBinding.getHashtableFromComponent(dett.getFellowIfAny("myForm",  true)));
////		hVerifiche = new CompareHashtable(UtilForBinding.getHashtableFromComponent(verifiche.getFellowIfAny("myForm",  true)));
//		doFreezeListBox();
//	}

	@Override
	protected boolean doValidateForm() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected boolean doSaveForm() throws Exception{
			if (controlloSelezioneCheck()){
				return registra();
			}else{
				return false;
			}
	}

	//QUI0910
	protected void aggiornaTabella() throws Exception{
//	       this.setDefault();
//		this.JCariTableAgenda.deleteAll();
		this.execSelect();
		vaiAllUltimaSelezione();
//		settaRenderer();
//		JCariTableAgenda.setColumnHidden("cartella");
//		JCariTableAgenda.setColumnHidden("contatto");
////		gb 07/11/07 *******
//		JCariTableAgenda.setColumnHidden("obiettivo");
//		JCariTableAgenda.setColumnHidden("intervento");

	}
	
	public void onColoraColonna(Checkbox box, int col){
		String colonna = ((CaribelListheader)box.getParent()).getDb_name();
		if (isColonnaVuota(colonna)){
			// new it.pisa.caribel.swing2.cariInfoDialog(null,"La giornata selezionata non contiene prestazioni!","Attenzione!").show();
			box.setChecked(false); //setSelected(false);
			return;
		}
		Vector v=(Vector)hashDaPassare.get("giorni");
		//logger.info("_coloraColonna vettore giorni che arriva:"+v);
		int  ind=Integer.parseInt(colonna); //getColonnaArray(col);
		String index=""+ind;
//		String w_data=(String)v.elementAt(ind);
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
//		if(noModificheNelFuturo  && ControlloData(w_data,dataOdierna)<0){
//			if(firsttime){
//				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.noRegistrazioneFutura"));
////				new it.pisa.caribel.swing2.cariInfoDialog(null,"Non si possono registrare prestazioni con data successiva a oggi.","Attenzione!").show();
//				firsttime=false;
//			} 
//			box.setChecked(false);
//			return;
//		}else{
//			firsttime=true;
//		}

		if(box.isChecked()){
			ySel=ind;
			int numRighe=clm.getSize();
			boolean proseguo=false;
			for(int i=0;i<numRighe;i++){
				String key_statoVal = key_stato[i][ind];
				String statoSullaRiga = ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(i),"stato"+ind);
				if(!key_statoVal.equals(statoSullaRiga)){
					System.out.println("Trovato stato: "+ key_statoVal +" sul key e stato:"+ statoSullaRiga +" sul listmodel");
				}
				key_statoVal=statoSullaRiga;
				if(key_statoVal.equals("0")||key_statoVal.equals("3")||key_statoVal.equals("9")){
					proseguo=true;
					String data=(String)v.elementAt(ind);
					String cartella=clm.getFromRow(i, "ag_cartella").toString()+"-"+
					clm.getFromRow(i, "ag_contatto").toString()+"-"+
					(String)clm.getFromRow(i, "cod_obbiettivo")+"-"+
					clm.getFromRow(i, "n_intervento").toString()+"-"+
					(String)clm.getFromRow(i, "matt_pom");
					Hashtable hC=new Hashtable();
					Vector vP=new Vector();
					logger.trace("CARICO HProgToMove"+HProgToMove.toString());
					if(HProgToMove.containsKey(data)){
						hC=(Hashtable)HProgToMove.get(data);
					}
					if(hC.containsKey(cartella)){
						vP=(Vector)hC.get((cartella));
					}
					String key_progrValue = key_progr[i][ind];
					String progrSullaRiga = ISASUtil.getValoreStringa((Hashtable<String,Object>)clm.get(i),"progr"+ind);
					if(!key_progrValue.equals(progrSullaRiga)){
						System.out.println("Trovato progr: "+ key_progrValue +" sul key e progr:"+ progrSullaRiga +" sul listmodel");
					}
//					vP.add(key_progr[i][ind]);
					vP.add(progrSullaRiga);

					hC.put(cartella,vP);
					logger.trace("dopo  hC"+hC.toString());
					logger.trace("dopo  vP"+vP.toString());
					HProgToMove.put(data,hC);
					logger.trace("HO CARICATO HProgToMove"+HProgToMove.toString());
				}
			}

			//?? da togliere se si vuole spostare anche se ci sono prestazioni registrate
			if(proseguo==false){
				// new it.pisa.caribel.swing2.cariInfoDialog(null,"Nella giornata selezionata non esistono prestazioni da registrare","Attenzione!").show();
				box.setChecked(false);
				return;
			}
			logger.trace("inserisco in reg_interv: index=["+index+"]["+(String)v.elementAt(ind)+"]");
			reg_interv.put(index,(String)v.elementAt(ind));
		}
		else{
			//
			if(jCheckBox1.isChecked()){
				ySel=1;
			}else if(jCheckBox2.isChecked()){
				ySel=2;
			}else if(jCheckBox3.isChecked()){
				ySel=3;
			}else if(jCheckBox4.isChecked()){
				ySel=4;
			}else if(jCheckBox5.isChecked()){
				ySel=5;
			}else if(jCheckBox6.isChecked()){
				ySel=6;
			}else if(jCheckBox7.isChecked()){
				ySel=7;
			}else{
				ySel=-1;
			}
			String data=(String)reg_interv.get(index);
			logger.trace("rimuovo "+index+" data="+data);
			try{//potrebbe non essere presente in quanto lo stato non ha permesso il suo caricamento
				reg_interv.remove(index);
			}catch(Exception e){
			}
			try{//potrebbe non essere presente in quanto lo stato non ha permesso il suo caricamento
				this.HProgToMove.remove(data);
			}catch(Exception e2){
			}
		}
		cambiaStile(box.isChecked(), ind);
		caribellbAgendaAssistito.invalidate();
	}
	
	private void cambiaStile(boolean checked, int col) {
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		int numRighe=clm.getSize();
		Listitem item;
		String newStyle;
		int colonna = col;
		for(int i=0;i<numRighe;i++){
			item = caribellbAgendaAssistito.getItemAtIndex(i);
			if(item.getChildren().size()>1){
//				if(item.getChildren().size()==10){
//					colonna=col-1;
//				}else if(item.getChildren().size()==9){
//					colonna=col-2;
//				}else{
//					colonna=col;
//				}
				colonna=item.getChildren().size()-7+col;
				HtmlBasedComponent e = (HtmlBasedComponent) item.getChildren().get(colonna);
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
			onColoraColonna(jCheckBox1, 3+getPadd());
		if(jCheckBox2.isChecked())
			onColoraColonna(jCheckBox2, 4+getPadd());
		if(jCheckBox3.isChecked())
			onColoraColonna(jCheckBox3, 5+getPadd());
		if(jCheckBox4.isChecked())
			onColoraColonna(jCheckBox4, 6+getPadd());
		if(jCheckBox5.isChecked())
			onColoraColonna(jCheckBox5, 7+getPadd());
		if(jCheckBox6.isChecked())
			onColoraColonna(jCheckBox6, 8+getPadd());
		if(jCheckBox7.isChecked())
			onColoraColonna(jCheckBox7, 9+getPadd());
	}
	
	public int ControlloData (String dataold,String datanew){
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
		if (datanuova[2] < datavecchia[2]){
			return -1;
		}else
			if (datanuova[2] == datavecchia[2])
				//confronto mes
				if (datavecchia[1] > datanuova[1]){
					return -1;
				}else if (datanuova[1] == datavecchia[1])
						//confronto giorno
						if (datanuova[0] < datavecchia[0]){
							return -1;
						}
		return 1;
	}

	boolean isColonnaVuota(String chiave){
		//TODO passare il db_name?????
		for(int i=0; i<caribellbAgendaAssistito.getItemCount();i++){
//			((CaribelListModel)caribellbAgenda.getModel()).getFromRow(row, chiave)
			String w = (String) ((CaribelListModel)caribellbAgendaAssistito.getModel()).getFromRow(i, chiave);//(String) cariObjectTableModelAgenda.getElementAt(i,col);
//			w=w.trim();
			if(w!=null && !w.trim().equals(""))return false;
		}
		return true;
	}
	
	public void onCheck$jCheckBoxInteraSettimana(Event e){
		if(jCheckBoxInteraSettimana.isChecked()){
			jCheckBox1.setChecked(true);
			jCheckBox2.setChecked(true);
			jCheckBox3.setChecked(true);
			jCheckBox4.setChecked(true);
			jCheckBox5.setChecked(true);
			jCheckBox6.setChecked(true);
			jCheckBox7.setChecked(true);
		}else{
			jCheckBox1.setChecked(false);
			jCheckBox2.setChecked(false);
			jCheckBox3.setChecked(false);
			jCheckBox4.setChecked(false);
			jCheckBox5.setChecked(false);
			jCheckBox6.setChecked(false);
			jCheckBox7.setChecked(false);
		}
		onColoraColonna(jCheckBox1, 3+getPadd());
		onColoraColonna(jCheckBox2, 4+getPadd());
		onColoraColonna(jCheckBox3, 5+getPadd());
		onColoraColonna(jCheckBox4, 6+getPadd());
		onColoraColonna(jCheckBox5, 7+getPadd());
		onColoraColonna(jCheckBox6, 8+getPadd());
		onColoraColonna(jCheckBox7, 9+getPadd());
	}
	
	public void onCheck$jCheckBox1(Event e) {
		onColoraColonna(jCheckBox1, 3+getPadd());
	}
	public void onCheck$jCheckBox2(Event e) {
		onColoraColonna(jCheckBox2, 4+getPadd());
	}
	public void onCheck$jCheckBox3(Event e) {
		onColoraColonna(jCheckBox3, 5+getPadd());
	}
	public void onCheck$jCheckBox4(Event e) {
		onColoraColonna(jCheckBox4, 6+getPadd());
	}
	public void onCheck$jCheckBox5(Event e) {
		onColoraColonna(jCheckBox5, 7+getPadd());
	}
	public void onCheck$jCheckBox6(Event e) {
		onColoraColonna(jCheckBox6, 8+getPadd());
	}
	public void onCheck$jCheckBox7(Event e) {
		onColoraColonna(jCheckBox7, 9+getPadd());
	}

	public void onCheck$selTutti(Event e) {
		if (selTutti.isChecked())
			selezionaTuttiAssistiti(true);
		else selezionaTuttiAssistiti(false);
	}
	
	public void selezionaTuttiAssistiti(boolean selez){
		//		caribellbAgendaAssistito.getModel(
		//	CaribelListModel modello = (CaribelListModel) caribellbAgendaAssistito.getModel();
		int numRighe = caribellbAgendaAssistito.getItemCount();
		Listitem item;
		for(int k=0; k<numRighe; k++){
			item = caribellbAgendaAssistito.getItemAtIndex(k);
			for (Iterator iterator = item.getChildren().iterator(); iterator.hasNext();) {
				Object type = (Object) iterator.next();
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
			caribellbAgendaAssistito.selectAll();
		}else{
			caribellbAgendaAssistito.clearSelection();
		}
		onSelect$caribellbAgenda(null);
	}
	
	public int getCartelleSelezionate(){
		CaribelListModel modello=(CaribelListModel) caribellbAgendaAssistito.getModel();

		int numRighe = caribellbAgendaAssistito.getItemCount();
		Listitem item;
		for(int k=0; k<numRighe; k++){
			item = caribellbAgendaAssistito.getItemAtIndex(k);
			boolean valore = item.isSelected();//((Boolean)modello.getValueAt(k, col)).booleanValue();
			logger.info("valore==>"+valore);
			Enumeration en=HProgToMove.keys();
			String cartella=modello.getFromRow(k,"ag_cartella")+"-"+
					modello.getFromRow(k,"ag_contatto")+"-"+
					modello.getFromRow(k,"cod_obbiettivo")+"-"+
					modello.getFromRow(k,"n_intervento")+"-"+
					modello.getFromRow(k,"matt_pom");
			if(!valore){
				logger.info("RIGA NON SELEZIONATA ["+cartella+"]");
				while(en.hasMoreElements()){
					String key=(String)en.nextElement();
					logger.info("key==>"+key);
					Hashtable hC=(Hashtable)HProgToMove.get(key);
					logger.info("hC"+hC.toString());
					if(hC.containsKey(cartella)){
						hC.remove(cartella);
						if(hC.isEmpty()){
							HProgToMove.remove(key);
						}else{
							HProgToMove.put(key,hC);
						}
						logger.info("hC dopo rimozione:"+hC.toString());
						logger.info("HProgToMove dopo rimozione:"+HProgToMove.toString());
					}
				}
			}else{
				if(assistitoRitorno == null){
					setAssistitoRitorno(modello.getFromRow(k,"ag_cartella"));
				}
			}
		}
		logger.info("hash HProgToMove finale--->"+HProgToMove.toString());
		return caribellbAgendaAssistito.getSelectedCount();
	}

	protected boolean controlloSelezioneCheck() throws Exception{
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
		return registra();
	}

	public int getDaySelezionati(){
		logger.info("day selezionati hashtable reg_interv==>"+reg_interv.toString());
		if(reg_interv.isEmpty())return 0;
		return reg_interv.size();
	}

	boolean registra() throws Exception{
		Hashtable h=new Hashtable();
		//h.put("giorni",reg_interv);
		h.put("appuntamenti",HProgToMove);
		h.put("referente",(String)hashDaPassare.get("referente"));
		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
		h.put("esecutivo",(String)hashDaPassare.get("esecutivo"));

		logger.info("REGISTRO... "+h.toString());

		if(HProgToMove.isEmpty()){
			UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.appuntamentNonValidi"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"Appuntamenti non validi","Attenzione").show();
			reg_interv.clear();
			return false;
		}else{
			
			for (Iterator<String> iterator = HProgToMove.values().iterator(); iterator.hasNext();) {
				String dataPre = (String) iterator.next();
				if(ControlloData(dataPre,dataOdierna)<0){
					UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.noRegistrazioneFutura"));
					return false;
				}
			}
		
		}
		Object o = invokeGenericSuEJB(new AgendaRegistraEJB(), h, "registra_prestaz"); //db.registraPrest(h);
		h.clear();
		if (o==null){
			UtilForUI.standardExclamation("Operazione non riuscita");
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"Operazione non riuscita","Attenzione!").show();
			return false;
		}
//		setDefault();
		HProgToMove.clear();
		reg_interv.clear();
		aggiornaTabella();
		return true;
	}
	
	public String MetodoGenerico_doubleClickAction(Vector vett) throws Exception{
		Vector settimana =(Vector)vett.get(0);
		logger.info("settimanaSelezionata arrivata al padre=="+settimana.toString());
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
	
	public void aggiorna() throws Exception{
		execSelect();
		vaiAllUltimaSelezione();
	}

	protected void vaiAllUltimaSelezione() {
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		for (int i = 0; i < caribellbAgendaAssistito.getModel().getSize(); i++) {
			Object cartella = clm.getFromRow(i, "ag_cartella");
			if(assistitoRitorno!= null && cartella != null && cartella.equals(assistitoRitorno)){
				Clients.scrollIntoView(caribellbAgendaAssistito.getItemAtIndex(clm.getSize()-2));
				Clients.scrollIntoView(caribellbAgendaAssistito.getItemAtIndex(i));
				assistitoRitorno= null;
				return;
			}
		}
	}
	
	public void onPrec(ForwardEvent event) throws Exception{
		precSett();
	}
	
	public void onSucc(ForwardEvent event) throws Exception{
		succSett();
	}
	
	protected void succSett() throws Exception {
		logger.trace("settimana Succesiva");
		Vector vSettAttiva=(Vector)hashDaPassare.get("giorni");
		String primogg=vSettAttiva.firstElement().toString();
		String ultimogg=vSettAttiva.lastElement().toString();
		logger.info("succSett ["+primogg+" ]["+ultimogg+"]");
		String dataIni=dt.getDataNGiorni(ultimogg, 1);
		Vector settimana=getSettimanaDa(dataIni,"gg-mm-aaaa");
		hashDaPassare.put("giorni",settimana);
	    hashDaPassare.put("mese", mesi[dt.getMese(dataIni)-1]);
	    hashDaPassare.put("anno",""+dt.getAnno(dataIni));
	     this.aggiornaTabella();
	}

	protected void precSett() throws Exception {
		Vector vSettAttiva=(Vector)hashDaPassare.get("giorni");
		String primogg=vSettAttiva.firstElement().toString();
		String ultimogg=vSettAttiva.lastElement().toString();
		logger.info("precSett ["+primogg+" ]["+ultimogg+"]");
		String dataIni=dt.getDataNGiorni(primogg,-7);
		Vector settimana=getSettimanaDa(dataIni,"gg-mm-aaaa");
		hashDaPassare.put("giorni",settimana);
	    hashDaPassare.put("mese",mesi[dt.getMese(dataIni)-1]);
	    hashDaPassare.put("anno",""+dt.getAnno(dataIni));
	     this.aggiornaTabella();
	}
	
	protected	Vector getSettimanaDa(String giornoSel,String formato){

		GregorianCalendar gc =dt.getGreg(giornoSel);
		Vector v =new Vector();
		v.addElement(dt.getData(gc,formato));
		for(int i=0;i<6;i++)
		{
			gc.add(GregorianCalendar.DATE,1);
			v.addElement(dt.getData(gc,formato));
		}
		logger.info("giornisettimana="+v.toString());
		return v;
	}

	protected void setAssistitoRitorno(Object object) {
		assistitoRitorno = object;
	}
	
	protected int getColonnaArray(int colonnaSelezionata){
		return colonnaSelezionata - 3;
	}
	
	protected int getPadd() {
		return 0;
	}

}

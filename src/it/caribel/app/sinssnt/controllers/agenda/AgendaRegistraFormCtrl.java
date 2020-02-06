package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.sinssnt.bean.AgendaRegistraEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;

import java.text.ParseException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
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
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.impl.InputElement;

public class AgendaRegistraFormCtrl extends AgendaGenericFormCtrl{

	private static final long serialVersionUID = -9185932534805159010L;

	public static String myPathZul = "/web/ui/sinssnt/agenda/agendaRegistraForm.zul";

	private CaribelTextbox 	cod_operatore;
	private CaribelCombobox 	desc_operatore;

	protected String myKeyPermission = "AG_REG";
	protected AgendaEJB myEJB = new AgendaEJB();
	
//	protected String pathGridZul = "/web/ui/sinssnt/interventi/accessiGrid.zul";

//	private final int WAIT = 0;
//    private final int INSERT = 1;
//    private final int UPDATE_DELETE = 2;
//    private final int CONSULTA = 3;

//    Hashtable hashDaPassare=new Hashtable();

//	private CaribelListbox caribellbAgendaAssistito;
//	private CaribelListbox caribellbAgendaAssistito;


//	private Label riepilogoAssistito;

//	private CaribelTextbox riepilogoAltroAssistito;

	protected String statoPian;
	Vector<String> vMat = new Vector<String>();
	Vector<String> vPom = new Vector<String>();
	Vector<String> vMat_rm = new Vector<String>();
	Vector<String> vPom_rm = new Vector<String>();




	private String data_input;

//	private String[][] key_data;
//
//	private String[][] key_progr;
//
//	private String[][] key_stato;

//	private Hashtable HProgToMove=new Hashtable();
//	private Hashtable reg_interv=new Hashtable();

//	String dataOdierna=procdate.getitaDate();
//	boolean firsttime=true;


	private Component headers1;
	private Component headers2;

//	private Checkbox jCheckBoxInteraSettimana;

    
//	private static final String classForRequired = "selectedDayClass";
//	private static final String classForAssistito = "selectedAssistitoClass";
	
	public void doInitForm() {
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

			execSelect();
			ricercaKmMm();
			
			doFreezeForm();
			iu = CaribelSessionManager.getInstance().getIsasUser();	
		}catch(Exception e){
			doShowException(e);
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
		Component comp = event.getOrigin().getTarget();
		Object o = comp.getAttribute("dbName", true);
		CaribelListModel model = (CaribelListModel) caribellbAgendaAssistito.getModel();
		int x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
//		int y = JCariTableAgenda.getSelectedColumn();
		
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
//		Object o = comp.getAttribute("dbName", true);
//		if(((String)o).length()!=1) return; //	if(y<2)return;
//		int y = Integer.parseInt((String)o);
		int x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
		mostraAssistito(x);
//		modelloAgenda = (CaribelListModel) caribellbAgendaAssistito.getModel();
//		setCellaSelez(x,y+3);
//        if(ySel!=-1){
//            //JCariTableAgenda.clearSelection();
//            setCellaSelez(-1,-1);
//            return;
//            }
//
//           if(x==xOld && y==yOld){//se riclicco su cella selezionata la deseleziono
//            //JCariTableAgenda.clearSelection();
//            setCellaSelez(-1,-1);
//            xOld=-1;yOld=-1;
//            xDrag=-1;yDrag=-1;
//            return;
//           }else{
//            xOld=x;
//            yOld=y;
//           }
	}
	
//	protected void executeAddRemove(String y) throws ISASMisuseException {
//		int x = caribellbAgendaAssistito.getSelectedIndex();
//		int gg = -3;
//		try {
//			gg = Integer.parseInt(y);
//			gg = gg+2;
//			
//		} catch (Exception e) {
//			doShowException(e);
//		}
//		CaribelListModel clmAssistito = (CaribelListModel) caribellbAgendaAssistito.getModel();
////		String y = caribellbAgendaAssistito.getSelectedColumn();
//		if(x>-1){
//			String valore = (String)clmAssistito.getFromRow(x, y);
//			logger.debug("inserico la prestazione in cella valore==>["+valore+"]");
//
//			//String nuova_prest=((String)hashDaPassare.get("pi_prest_cod")).trim();
//			String nuova_prest=((String)hashDaPassare.get("prestazioni")).trim();
//			logger.debug("inserico la prestazione in cella prestaz==>["+nuova_prest+"]");
//			//gb 05/09/07                if((x==0 || x==1) && y!=0)
//			if((x==0 || x==1) && !y.isEmpty() && !y.equals("assistito")){ //gb 05/09/07
//				if (valore == null)
//					clmAssistito.setValueAt(nuova_prest, x, y);
//				else{
//					if(!isPresente(nuova_prest,valore)){
//						clmAssistito.setValueAt(valore+"-"+nuova_prest, x, y);
//					}else{
//						if(statoPian.equals("UPDATE_DELETE"))//{
//							UtilForUI.standardExclamation(Labels.getLabel("agendaPianSett.msg.prestazioneRimossa", new String[]{nuova_prest}));
////							new it.pisa.caribel.swing2.cariInfoDialog(null,"La Prestazione: ["+nuova_prest+"] verrà rimossa","Attenzione!").show();
//						int inizio=valore.indexOf(nuova_prest);
//						//logger.debug("INIZIO =="+inizio);
//						int fine=inizio+nuova_prest.length();
//						//logger.debug("FINE =="+fine);
//						if (inizio!=0)inizio=inizio-1;
//						StringBuffer bf=new StringBuffer(valore);
//						bf.replace(inizio,fine,"");
//						String w_bf=bf.toString();
//						if(w_bf.startsWith("-"))
//							w_bf=w_bf.substring(1);
//						clmAssistito.setValueAt(w_bf, x, y);
//						//devo controllare se presente e rimuoverlo!
//
//						if (x==0){
//							if(vMat.contains("M"+gg))vMat.remove("M"+gg);
//
//						}else if (x==1){
//							if(vPom.contains("P"+gg))vPom.remove("P"+gg);
//
//						}
//						if(statoPian.equals("UPDATE_DELETE")){
//							if (x==0)
//								vMat_rm.add("M"+gg);
//							else if (x==1)
//								vPom_rm.add("P"+gg);
//						}
//						hashDaPassare.put("updAppuntam","false");
//						caribellbAgendaAssistito.setModel(clmAssistito);
//						caribellbAgendaAssistito.invalidate();
//						return;
//					}
//				}
//				//logger.debug("aggiungo--> M"+y+" P"+y);
//				if (x==0){
//					vMat.add("M"+gg);
//					//logger.debug("aggiungo--> M"+y);
//				}else if (x==1){
//					vPom.add("P"+gg);
//					//logger.debug("aggiungo--> P"+gg);
//				}hashDaPassare.put("updAppuntam","false");
//			}
//		}
//		caribellbAgendaAssistito.setModel(clmAssistito);
//		caribellbAgendaAssistito.invalidate();
//	}	

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
	
	@SuppressWarnings("rawtypes")
//	protected int execSelect() throws Exception {
//		meseAnno.setValue(hashDaPassare.get("mese") + " - " + hashDaPassare.get("anno"));
//		caribellbAgendaAssistito.setModel(new CaribelListModel());
//		settaHeaderTabella();
//		
//		int count = 0;
//		logger.info("JFrameAgendaPianSett **** HASH che invio per selezAgenda..."+hashDaPassare.toString());
//		Object o= invokeGenericSuEJB(myEJB, hashDaPassare, "query_agenda"); //db.selezAgenda(hashDaPassare);
//		Vector griglia=new Vector();
//		if(o==null){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.errore"));
//			return -1;
//		}else{
//			if(((Vector)o).size()<=0){
//				UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.noPianificazione"));
//				return -1;
//			}else{
//				griglia=(Vector)o;
//			}
//		}
//		
//		int totrighe=0;
//		if(griglia!=null){ 
//			totrighe=griglia.size();
//		}
//		key_data=new String[totrighe*2][7];
//		key_progr=new String[totrighe*2][7];
//		key_stato=new String[totrighe*2][7];
//		
//		Enumeration en=griglia.elements();
//		String chiave_new="";
//		String chiave_old="";
//		boolean w_due=false;
//		boolean w_prima=true;
//		int riga=0;
//		int numRighe=0;
//		Hashtable ht=new Hashtable();
//
//		while(en.hasMoreElements()){
//			ISASRecord is =(ISASRecord)en.nextElement();
//			Hashtable h=is.getHashtable();
//			//gb 30/08/07          chiave_new=((Integer)h.get("n_cartella")).toString()+"-"+((Integer)h.get("n_contatto")).toString()+"-"+formDate(h.get("as_data"),"gg/mm/aaaa");
//			chiave_new = ((Integer)h.get("ag_cartella")).toString() + "-" +
//					((Integer)h.get("ag_contatto")).toString() + "-" +
//					(String)h.get("cod_obbiettivo") + "-" +
//					((Integer)h.get("n_intervento")).toString();
//			if (w_prima==true){
//				caricaRiga(h);
//				ht=(Hashtable) h.clone();
//				w_prima=false;
//				w_due=false;
//				chiave_old=chiave_new;
//				//devo controllare se l'assistito a cui
//				//sto inserendo le prestazioni è gia presente
//				//nella pianificazioni in modo da i
//				//onserire eventualm le righe vuote
//			}else{
//				if (chiave_new.equals(chiave_old)){
//					caricaRiga(h);
//					w_due=true;
//				}else{
//					chiave_old=chiave_new;
//					//devo controllare se l'assistito a cui sto inserendo le prestazioni è gia presente
//					//nella pianificazioni in modo da inserire eventualmente le righe vuote
//					if (w_due!=true){
//						casoRigaVuota(ht);
//						w_due=true;
//					}
//					ht=h;
//					caricaRiga(h);
//					w_due=false;
//				}
//			}
//		}
//
//		if (w_due!=true){
//			casoRigaVuota(ht);
//		}
//
//		((CaribelListModel) caribellbAgendaAssistito.getModel()).setMultiple(true);
//		
//		return count;
//	}

	@Override
	protected void settaHeaderTabella(){
		//Modifico le label degli header delle colonne
		caribellbAgendaAssistito.getHeads();
		Listhead testa = caribellbAgendaAssistito.getListhead();
		String dbName, titolo;
		Vector v=(Vector) hashDaPassare.get("giorni");
		String data;
		boolean disable;
		for (Object header : testa.getChildren()) {
			dbName = ((CaribelListheader)header).getDb_name();

			try {
				int i = Integer.parseInt(dbName);
				data = (String) v.get(i);
				((CaribelListheader)header).setLabel(Labels.getLabel("agendaPianSett.sett."+i)+" "+data.substring(0, 2));
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
			} catch (NumberFormatException e) {
			} catch (ParseException e) {
				doShowException(e);
			}
		}
		//finemodifica headers
	}

	private void ricercaKmMm() throws Exception {
		//TODO VFR spostare da qui e separare per evitare la cancellazione in caso di aggiornamento dal popup
		Hashtable pippo = (Hashtable<String, Object>) invokeGenericSuEJB(myEJB, hashDaPassare, "query_tempiKm");
		//doWriteBeanToComponents();
		UtilForBinding.bindDataToComponent(logger, pippo, headers1);
		UtilForBinding.bindDataToComponent(logger, pippo, headers2);
		doFreezeForm();
	}


	  private boolean ISEmpty(Vector v){
		  Enumeration en=v.elements();
	      int elem=0;
	      while(en.hasMoreElements()){
	      	Vector vt=(Vector)en.nextElement();
	      	elem+=vt.size();
	      }
	      if(elem==0)return true;
	      else return false;   
	  }

//	public void setDataInput(String data_input) {
//		this.data_input = data_input;
//		doSuperSaveForm();
//	}
	
	public void onColoraColonna(Checkbox box, int col){
		if (isColonnaVuota(((CaribelListheader)box.getParent()).getDb_name())){
			// new it.pisa.caribel.swing2.cariInfoDialog(null,"La giornata selezionata non contiene prestazioni!","Attenzione!").show();
			box.setChecked(false); //setSelected(false);
			return;
		}
		Vector v=(Vector)hashDaPassare.get("giorni");
		//logger.info("_coloraColonna vettore giorni che arriva:"+v);
		int  ind=col-3;
		String index=""+ind;
		String w_data=(String)v.elementAt(ind);
		CaribelListModel clm = (CaribelListModel) caribellbAgendaAssistito.getModel();
		if(noModificheNelFuturo  && ControlloData(w_data,dataOdierna)<0){
			if(firsttime){
				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.noRegistrazioneFutura"));
//				new it.pisa.caribel.swing2.cariInfoDialog(null,"Non si possono registrare prestazioni con data successiva a oggi.","Attenzione!").show();
				firsttime=false;
			} 
			box.setChecked(false);
			return;
		}else{
			firsttime=true;
		}

		if(box.isChecked()){
			int numRighe=clm.getSize();
			boolean proseguo=false;
			for(int i=0;i<numRighe;i++){
				String key_statoVal = key_stato[i][col-3];
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
					logger.info("CARICO HProgToMove"+HProgToMove.toString());
					if(HProgToMove.containsKey(data)){
						hC=(Hashtable)HProgToMove.get(data);
					}
					if(hC.containsKey(cartella)){
						vP=(Vector)hC.get((cartella));
					}
					vP.add(key_progr[i][col-3]);
					hC.put(cartella,vP);
					logger.info("dopo  hC"+hC.toString());
					logger.info("dopo  vP"+vP.toString());
					HProgToMove.put(data,hC);
					logger.info("HO CARICATO HProgToMove"+HProgToMove.toString());
				}
			}

			//?? da togliere se si vuole spostare anche se ci sono prestazioni registrate
			if(proseguo==false){
				// new it.pisa.caribel.swing2.cariInfoDialog(null,"Nella giornata selezionata non esistono prestazioni da registrare","Attenzione!").show();
				box.setChecked(false);
				return;
			}
			logger.info("inserisco in reg_interv: index=["+index+"]["+(String)v.elementAt(ind)+"]");
			reg_interv.put(index,(String)v.elementAt(ind));
		}
		else{
			String data=(String)reg_interv.get(index);
			logger.info("rimuovo "+index+" data="+data);
			try{//potrebbe non essere presente in quanto lo stato non ha permesso il suo caricamento
				reg_interv.remove(index);
			}catch(Exception e){
			}
			try{//potrebbe non essere presente in quanto lo stato non ha permesso il suo caricamento
				this.HProgToMove.remove(data);
			}catch(Exception e2){
			}
		}
		cambiaStile(box.isChecked(), col);
		caribellbAgendaAssistito.invalidate();
	}
	
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
			onColoraColonna(jCheckBox1, 3);
		if(jCheckBox2.isChecked())
			onColoraColonna(jCheckBox2, 4);
		if(jCheckBox3.isChecked())
			onColoraColonna(jCheckBox3, 5);
		if(jCheckBox4.isChecked())
			onColoraColonna(jCheckBox4, 6);
		if(jCheckBox5.isChecked())
			onColoraColonna(jCheckBox5, 7);
		if(jCheckBox6.isChecked())
			onColoraColonna(jCheckBox6, 8);
		if(jCheckBox7.isChecked())
			onColoraColonna(jCheckBox7, 9);
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
		}
		Hashtable<String, Object> tempiKm = new Hashtable<String, Object>();
		tempiKm.putAll(UtilForBinding.getHashtableFromComponent(headers1));
		tempiKm.putAll(UtilForBinding.getHashtableFromComponent(headers2));
		h.put("tempiKm", tempiKm);
		h.put("giorni", hashDaPassare.get("giorni"));
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
	
	protected void aggiornaTabella() throws Exception{
		this.execSelect();
		ricercaKmMm();
	}
	
}

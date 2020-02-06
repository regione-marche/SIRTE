package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.bean.AgendaEditEJB;
import it.caribel.app.sinssnt.bean.AgendaModOperatoreEJB;
import it.caribel.app.sinssnt.bean.AgendaRegistraEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForGridRenderer;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;

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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AgendaRegistraNewFormCtrl extends AgendaGenericFormCtrl{

	private static final long serialVersionUID = -9185932534805159010L;

	private static final long DOUBLE_CLICK_THRESHOLD = 600;

	public static String myPathZul = "/web/ui/sinssnt/agenda/agendaRegistraNewForm.zul";
	private String ver = "2-"+this.getClass().getName()+". ";
	private CaribelTextbox 	cod_operatore;
	private CaribelCombobox 	desc_operatore;

	DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
	protected String myKeyPermission = "AG_REG";
	protected AgendaEJB myEJB = new AgendaEJB();

	protected String statoPian;
	protected Vector<String> vMat = new Vector<String>();
	protected Vector<String> vPom = new Vector<String>();
	protected Vector<String> vMat_rm = new Vector<String>();
	protected Vector<String> vPom_rm = new Vector<String>();

	protected Component headers1;
	protected Component headers2;
	
	protected AgendaEditEJB myEJBEdit = new AgendaEditEJB();
	protected AgendaModOperatoreEJB modEJB = new AgendaModOperatoreEJB();

	protected String myKeyPermissionEdit = ChiaviISASSinssntWeb.AG_SPO;
	protected String myKeyPermissionSpostaOp = ChiaviISASSinssntWeb.AG_OPE;

	protected int xOld = -1, yOld = -1;
	protected int xDrag= -1, yDrag = -1;
	protected Hashtable hDa = new Hashtable();
    protected Hashtable hA = new Hashtable();
	
	protected CaribelListbox caribellbOperatori;
//	protected Hashtable HProgToMove=new Hashtable();
	protected Component panel_ubicazione;
	protected PanelUbicazioneCtrl c;
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	
	protected java.util.Timer timer;
	protected java.util.TimerTask task;
	protected boolean clickValido = true;

	
	protected Component componentClicked;

	protected Hashtable hA2 = new Hashtable();

	protected Hashtable hDa2 = new Hashtable();

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

			caribellbOperatori.setModel(new CaribelListModel());
			HProgToMove.clear();

            task = new ClickTask();//timer
            timer = new java.util.Timer();//timer
            
			execSelect();
			ricercaKmMm();
			
			c = (PanelUbicazioneCtrl)panel_ubicazione.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();			
			c.settaRaggruppamento("P"); //NoUbic("P");
			c.setVisibleRaggruppamento(false);
			c.setVisibleUbicazione(false);
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
	
//	protected void doFreezeForm() throws Exception{
//		tH = new CompareHashtable(UtilForBinding.getHashtableFromComponent(caribellbAgendaAssistito, null, false, true));
//		doFreezeListBox();
//	}

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
//        task = new ClickTask();
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
		if(key_stato[x][y].equals("1")){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoIncompleto"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"L'accesso, con stato incompleto, è chiuso! \n Puoi solo visionare l'elenco delle prestazioni!","Attenzione scelta non valida!").show();
			stat=1;
		}else if(key_stato[x][y].equals("2")){
//			UtilForUI.standardExclamation(Labels.getLabel("agenda.msg.accessoChiuso"));
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"L'accesso, con stato completo, è chiuso! \n Puoi solo visionare l'elenco delle prestazioni!","Attenzione scelta non valida!").show();
			stat=2;
		}
		String valore = ((String) modelloAgenda.getFromRow(x,(String) o));
		logger.trace("in mouseclick valor="+valore);
//		Hashtable<String, Object> h = new Hashtable<String, Object>();
		if (valore == null || valore.trim().equals("")||key_stato[x][y].equals("")){
			UtilForUI.standardYesOrNo(Labels.getLabel("agendaSposta.msg.nuovoIntervento", new String[]{key_data[x][y]}), new EventListener<Event>(){
				public void onEvent(Event event) throws Exception{
					if (Messagebox.ON_YES.equals(event.getName())){	
						apriPopup(modelloAgenda, x, stat, y,"INSERT", "00");
					}
				}
			} );
			return;
		}
		apriPopup(modelloAgenda, x, stat, y,"UPDATE", key_progr[x][y]);

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
//		timer.schedule(task, DOUBLE_CLICK_THRESHOLD);
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
		setCellaSelez(x,y+3);
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
			return;
		}else{
			xOld=x;
			yOld=y;
		}

		if(xDrag==-1 && yDrag==-1){
			if(!key_stato[x][y].equals("0")&& !key_stato[x][y].equals("9"))/*[PUA]*/{
				if(key_stato[x][y].equals("")){
//					UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.nonEsisteAppuntamento"));
					setCellaSelez(-1,-1);
					return;
				}
				UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.statoInterventoNonSpostabile"));
				setCellaSelez(-1,-1);
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
		if (valore.equals("")||key_stato[x][y].equals("")){
			moveAccordato=false;
		}else{
			moveAccordato=true;
		}
		String st=key_stato[x][y];
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
		String orario=(String)modelloAgenda.getFromRow(x,"matt_pom");//FIXME decodificare orario
		Object o = modelloAgenda.getFromRow(x,"ag_orario");
//		hDa.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));
		hDa.put("ag_orario", ""+x%CostantiAgenda.NUMFASCIEORARIE);
		hDa.put("ag_progr", modelloAgenda.getFromRow(x,"ag_progr")+"");

		logger.trace("Caricata hashtable hDa..."+hDa.toString());
		return moveAccordato;
	}

	public boolean spostoA(int x,int y,Hashtable hA){
		String st=key_stato[x][y];
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
		String orario=(String)modelloAgenda.getFromRow(x,"matt_pom");
//		hA.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));
		Object o = modelloAgenda.getFromRow(x,"ag_orario");
		hA.put("ag_orario", ""+x%CostantiAgenda.NUMFASCIEORARIE);//FIXME decodificare orario?
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
				boolean primeDueFasce = (i%CostantiAgenda.NUMFASCIEORARIE==0 || i%CostantiAgenda.NUMFASCIEORARIE==1);
//			if(item.getChildren().size()>=y){
//				HtmlBasedComponent e = (HtmlBasedComponent) item.getChildren().get(y+2);
				if(e.getSclass()!=null){
					String parkStyle = e.getSclass();
					parkStyle = parkStyle.replaceAll(classForRequired, "");
					parkStyle = parkStyle.replaceAll(classForMove, "");
					parkStyle = parkStyle.replaceAll("  ", " ");
					if(i==x && ((k==y && primeDueFasce)|| (k==y-1 && !primeDueFasce))){//i==X la riga è quella giusta 
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
	}

	private void apriPopup(CaribelListModel model, int x, int stat, int y, String stato, String progr) {
		Hashtable<String, Object> h = new Hashtable<String, Object>();
        String st=key_stato[x][y];
        if(st.equals(""))st="0";
        h.put("ag_stato",st);
		h.put("stato", stato);
		h.put("ag_progr", progr);
		h.put("ag_data",key_data[x][y]);
		h.put("referente",(String)hashDaPassare.get("referente"));
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

	protected void ricercaKmMm() throws Exception {
		//TODO VFR spostare da qui e separare per evitare la cancellazione in caso di aggiornamento dal popup
		Hashtable pippo = (Hashtable<String, Object>) invokeGenericSuEJB(myEJB, hashDaPassare, "query_tempiKm");
		//doWriteBeanToComponents();
		UtilForBinding.bindDataToComponent(logger, pippo, headers1);
		UtilForBinding.bindDataToComponent(logger, pippo, headers2);
		doFreezeForm();
	}

	
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
		reg_interv.clear();
		jCheckBox1.setChecked(false);
		jCheckBox2.setChecked(false);
		jCheckBox3.setChecked(false);
		jCheckBox4.setChecked(false);
		jCheckBox5.setChecked(false);
		jCheckBox6.setChecked(false);
		jCheckBox7.setChecked(false);
		selTutti.setChecked(false);
		xDrag=-1;
		yDrag=-1;
		ySel=-1;
		
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
	
	protected void aggiornaTabella() throws Exception{
		super.aggiornaTabella();
		ricercaKmMm();
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
		return true;
	}

	public void onConfermaSelezione(Event event) throws Exception{
		if(controlloSelezioneOp() && controlloSelezioneCheck()){
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
	      String stPresidio=presidio_comune_area.getSelectedValue(); //jPanelAgendaUbicazione.getComboTerzoLivello();
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
	      Object o= invokeGenericSuEJB(modEJB, h, "CaricaTabellaOperatoriNew");//db.CaricaTabellaOperatori(h);
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
	
	class ClickTask extends java.util.TimerTask
	{
		public void run()
		{
				singleClick();
				task = new ClickTask();
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
    	Datebox calendarietto = (Datebox) ((ForwardEvent) event).getOrigin().getTarget();

    	Object date = calendarietto.getValue();
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
    		Hashtable h = new Hashtable<String, Object>();
    		h.put("referente",(String)hashDaPassare.get("referente"));
    		h.put("tipo_operatore",(String)hashDaPassare.get("tipo_operatore"));
    		h.put("giorni", settimana);

    		Vector caricata = myEJB.query_agenda(CaribelSessionManager.getInstance().getMyLogin(), h );
    		if(caricata.isEmpty()){
    			UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.settimanaNonCaricata"));
    			return;
    		}
    		
    		if(xDrag==-1 && yDrag==-1 && ySel==-1) {
    			hashDaPassare.put("giorni",settimana);
    		    hashDaPassare.put("mese", mesi[dt.getMese(dataIni)-1]);
    		    hashDaPassare.put("anno",""+dt.getAnno(dataIni));
    		     this.aggiornaTabella();
    			
//    			UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.noSelezione"));
    			return;
    		}else{
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
}

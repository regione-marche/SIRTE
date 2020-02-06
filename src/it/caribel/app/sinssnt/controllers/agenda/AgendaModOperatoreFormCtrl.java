package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.bean.AgendaModOperatoreEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.util.CaribelSessionManager;
import it.caribel.util.CompareHashtable;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;

import java.util.Hashtable;
import java.util.Iterator;
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
import org.zkoss.zul.Listitem;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AgendaModOperatoreFormCtrl extends AgendaGenericFormCtrl{

	private static final long serialVersionUID = -439915547098798881L;

	public static String myPathZul = "/web/ui/sinssnt/agenda/agendaModOperatoreForm.zul";

	protected AgendaEJB myEJB = new AgendaEJB();
	protected AgendaModOperatoreEJB modEJB = new AgendaModOperatoreEJB();
	
	protected String myKeyPermission = ChiaviISASSinssntWeb.AG_OPE;

	protected CaribelListbox caribellbOperatori;
	protected Component panel_ubicazione;
	protected PanelUbicazioneCtrl c;

	private int xOld = -1, yOld = -1;
	private int xDrag= -1, yDrag = -1;
    private Hashtable hDa = new Hashtable();
    private Hashtable hA = new Hashtable();

	private CaribelRadiogroup raggruppamento;
	private CaribelCombobox zona;
	private CaribelCombobox distretto;
	private CaribelCombobox presidio_comune_area;
	
    
    
    
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			super.setMethodNameForQuery("query_agenda");
			super.setMethodNameForInsert("insert_agenda");
			super.setMethodNameForUpdate("update_agenda");
			
			caribellbOperatori.setModel(new CaribelListModel());
			caribellbAgendaAssistito.setModel(new CaribelListModel());
			HProgToMove.clear();
			
			hashDaPassare.putAll(arg);
//			hashDaPassare.put("esecutore", value)
			caribellbAgendaAssistito.setItemRenderer(new AgendaGridItemRenderer());
			
			caribellbAgendaAssistito.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					mostraAssistito();
				}});

			execSelect();

			c = (PanelUbicazioneCtrl)panel_ubicazione.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();			
			c.settaRaggruppamento("P"); //NoUbic("P");
			c.setVisibleRaggruppamento(false);
			c.setVisibleUbicazione(false);
			caribellbAgendaAssistito.invalidate();
			doFreezeForm();
			iu = CaribelSessionManager.getInstance().getIsasUser();	
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	protected void doFreezeForm() throws Exception{
		tH = new CompareHashtable(UtilForBinding.getHashtableFromComponent(caribellbAgendaAssistito, null, false, true));
		doFreezeListBox();
	}

	protected void mostraAssistito() {
		
	        int x = caribellbAgendaAssistito.getSelectedIndex();
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
		logger.info("Eseguo spostamento "+h.toString());
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
	
	public void execUpdate(int selection,String operaz,Hashtable hDa,Hashtable hA) throws Exception{
		logger.info("execUpdate hash="+hashDaPassare.toString());

		if (selection==1) return;
		Hashtable hAgg=new Hashtable();
		hAgg.put("operaz",operaz);
		hAgg.put("hDa",hDa);
		hAgg.put("hA",hA);
		hAgg.put("referente",hashDaPassare.get("referente"));
		//System.out.println("INVIO...."+hAgg.toString());
		Object o=null;
		if (selection==2){//COPIO
			o=invokeGenericSuEJB(myEJB, hAgg, "copio_appunt");//db.copioAppunt(hAgg);
		}else if (selection==3){//sposto
			o=invokeGenericSuEJB(myEJB, hAgg, "sposto_appunt");//db.spostoAppunt(hAgg);
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

	private String costruisciMsg(Hashtable h){
//        String msg= "Num Cartella/Contatto/Obiettivo/Intervento="+(String)h.get("ag_cartella")+"/"+(String)h.get("ag_contatto")+"/"+(String)h.get("cod_obbiettivo")+"/"+(String)h.get("n_intervento")+
//        " del:"+formattaData3((String)h.get("ag_data"),1)+" "+((((String)h.get("ag_orario")).trim()).equals("0")?"Mattina":"Pomeriggio");
        String msg = Labels.getLabel("agendaSposta.msg.costruisciMsg", new String[]{(String)h.get("ag_cartella"),
        																			(String)h.get("ag_contatto"),
        																			(String)h.get("cod_obbiettivo"),
        																			(String)h.get("n_intervento"),
        																			formattaData3((String)h.get("ag_data"),1),
        																			((((String)h.get("ag_orario")).trim()).equals("0")?"Mattina":"Pomeriggio")});
        return msg;
    }
	
	private String formattaData3(String campo,int f){
		//restituisce la data nel formato gg-mm-aaaa
		//arriva aaaa-mm-gg
		String dataret=null;
		if(f==1){//rit gg-mm-aaaa
			dataret="00-00-0000";
			if (campo!=null && !campo.equals("")){
				String dataLetta=campo;
				if (dataLetta.length()==10){
					dataret=dataLetta.substring(8)+"-"+dataLetta.substring(5,7)
							+"-"+dataLetta.substring(0,4);
				}
			}
		}else{//rit aaaammgg
			dataret="00000000";
			if (campo!=null && !campo.equals("")){
				String dataLetta=campo;
				if (dataLetta.length()==10){
					dataret=dataLetta.substring(0,4)+dataLetta.substring(5,7)+dataLetta.substring(8);;
				}
			}
		}
		return dataret;
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
		String orario=(String)modelloAgenda.getFromRow(x,"matt_pom");
		hDa.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));

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
		hA.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));
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
			for (Iterator iterator = item.getChildren().iterator(); iterator.hasNext();) {
				HtmlBasedComponent e = (HtmlBasedComponent) iterator.next();
				
//			if(item.getChildren().size()>=y){
//				HtmlBasedComponent e = (HtmlBasedComponent) item.getChildren().get(y+2);
				if(e.getSclass()!=null){
					String parkStyle = e.getSclass();
					parkStyle = parkStyle.replaceAll(classForRequired, "");
					parkStyle = parkStyle.replaceAll(classForMove, "");
					parkStyle = parkStyle.replaceAll("  ", " ");
					if(i==x && k==y){
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
		h.put("ag_orario",(String)model.getFromRow(x,"matt_pom"));
		h.put("stato_interv", stat);
		
	    Executions.getCurrent().createComponents(AgendaModificaPopUpFormCtrl.myPathZul, self, h);
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
	protected boolean doSaveForm() throws Exception{
			if(controlloSelezioneOp() && controlloSelezioneCheck()){
				return cambioOperatore();
			}
			return false;
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
		doSaveForm();
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
	      //System.out.println("Caricaoperatori==>"+h.toString());
	      Object o= invokeGenericSuEJB(modEJB, h, "CaricaTabellaOperatori");//db.CaricaTabellaOperatori(h);
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

	public void onDoubleClickedCell(ForwardEvent event) throws Exception {
	}

}

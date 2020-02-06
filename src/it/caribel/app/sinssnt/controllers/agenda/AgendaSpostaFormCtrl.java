package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.sinssnt.bean.AgendaEditEJB;
import it.caribel.app.sinssnt.bean.AgendaRegistraEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.util.UtilForUI;

import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AgendaSpostaFormCtrl extends AgendaGenericFormCtrl{

	private static final long serialVersionUID = -439915547098798881L;

	public static String myPathZul = "/web/ui/sinssnt/agenda/agendaSpostaForm.zul";

	protected AgendaEditEJB myEJB = new AgendaEditEJB();
	protected String myKeyPermission = ChiaviISASSinssntWeb.AG_SPO;

	private int xOld = -1, yOld = -1;
	private int xDrag= -1, yDrag = -1;
    private Hashtable hDa = new Hashtable();
    private Hashtable hA = new Hashtable();
	
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			super.setMethodNameForQuery("query_agenda");
			super.setMethodNameForInsert("insert_agenda");
			super.setMethodNameForUpdate("update_agenda");
			
			hashDaPassare.putAll(arg);
//			hashDaPassare.put("esecutore", value)
			caribellbAgendaAssistito.setItemRenderer(new AgendaGridItemRenderer());
			
//			caribellbAgendaAssistito.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
//				public void onEvent(Event event) throws Exception {
//					mostraAssistito();
//				}});

			execSelect();
			
			doFreezeForm();
			iu = CaribelSessionManager.getInstance().getIsasUser();	
		}catch(Exception e){
			doShowException(e);
		}
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

	public void onDoubleClickedCell(ForwardEvent event) throws Exception {
		Component comp = event.getOrigin().getTarget();
		Object o = comp.getAttribute("dbName", true);
		modelloAgenda = (CaribelListModel) caribellbAgendaAssistito.getModel();
		x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
		
		stat=0;
		if(((String)o).length()!=1) return; //	if(y<2)return;
		y = Integer.parseInt((String)o);
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
		String valore = ((String) modelloAgenda.getFromRow(x,(String) o));
		//System.out.println("in mouseclick valor="+valore);
		if (valore == null || valore.trim().equals("")||key_stato[x][y].equals("")){
			UtilForUI.standardYesOrNo(Labels.getLabel("agendaSposta.msg.nuovoIntervento", new String[]{key_data[x][y]}), new EventListener<Event>(){
				public void onEvent(Event event) throws Exception{
					if (Messagebox.ON_YES.equals(event.getName())){	
						apriPopup(modelloAgenda, x, stat, y,"INSERT", "0");
					}
				}
			} );
			return;
		}
		apriPopup(modelloAgenda, x, stat, y,"UPDATE", key_progr[x][y]);
	}

	public void onClickedCell(ForwardEvent event) throws Exception {
		mostraAssistito();
		Component comp = event.getOrigin().getTarget();
		Object o = comp.getAttribute("dbName", true);
		if(((String)o).length()!=1) return; //	if(y<2)return;
		y = Integer.parseInt((String)o);
		x = caribellbAgendaAssistito.getIndexOfItem((Listitem) comp.getParent()); //caribellbAgendaAssistito.getSelectedIndex(); //JCariTableAgenda.getSelectedRow();
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
                	   UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.nonEsisteAppuntamento"));
//                	   new it.pisa.caribel.swing2.cariInfoDialog(null,"Scelta non valida non esiste appuntamento.","Attenzione!").show();
                	   setCellaSelez(-1,-1);
                	   return;
                   }
            	   UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.statoInterventoNonSpostabile"));
//                   new it.pisa.caribel.swing2.cariInfoDialog(null,"Lo stato dell'intervento non permette di effetuare spostamenti.","Attenzione!").show();
                   setCellaSelez(-1,-1);
                   return;
                 }
                 xDrag=x;
                 yDrag=y;
                 hDa=new Hashtable();
                 if(!spostoDa(xDrag,yDrag,hDa)){
              	   UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.sceltaNonValida"));
//                   new it.pisa.caribel.swing2.cariInfoDialog(null,"La scelta effettuata non risulta valida","Attenzione!").show();
                   xDrag=-1;yDrag=-1;
                 }
             //seleziono cella da spostare
           }else{
              setCellaSelez(-1,-1);
              xOld=-1;yOld=-1;
              hA=new Hashtable();
               if(!spostoA(x,y,hA)){
            	   UtilForUI.standardExclamation(Labels.getLabel("agendaSposta.msg.sceltaNonValida"));
//                 new it.pisa.caribel.swing2.cariInfoDialog(null,"La scelta effettuata non risulta valida","Attenzione!").show();
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
//                Object[] options = { "Cancel","Copia", "Sposta"};
//                selection=chiedoConfermaSpostamento(message,options);
//                execUpdate(selection,"CELL",hDa,hA);
              }else{
                  message=Labels.getLabel("agendaSposta.msg.copiareOSpostareAppuntamento", new String[]{"", costruisciMsg(hDa), costruisciMsg(hA)});
                  Messagebox.show(message, Labels.getLabel("messagebox.attention"),  new Messagebox.Button[] {Messagebox.Button.CANCEL, Messagebox.Button.YES}, new String[]{"Cancel","Copia"}, Messagebox.QUESTION, Messagebox.Button.CANCEL, new EventListener<Messagebox.ClickEvent>() {
                	  											public void onEvent(Messagebox.ClickEvent event) throws Exception {
                	  													execUpdate(event.getButton().ordinal(),"CELL",hDa,hA);
                	  											}},params);  
//                message="Vuoi copiare l'appuntamento:\n"+
//                costruisciMsg(hDa)+"\n"+
//                "al giorno:\n"+
//                costruisciMsg(hA);
//                Object[] options = { "Cancel","Copia"};
//                selection=chiedoConfermaSpostamento(message,options);
//                execUpdate(selection,"CELL",hDa,hA);
              }
              xDrag=-1;yDrag=-1;
           }
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
        																			Labels.getLabel("agenda.fasce."+h.get("ag_orario"))});
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
		String orario=(String)modelloAgenda.getFromRow(x,"matt_pom");//FIXME decodificare orario
		Object o = modelloAgenda.getFromRow(x,"ag_orario");
//		hDa.put("ag_orario", ((orario.trim()).equals("M")?"0":"1"));
		hDa.put("ag_orario", ""+x%CostantiAgenda.NUMFASCIEORARIE);
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
	
//	private void caricaPerAgenda(){
//		logger.info("caricaPeragenda");
//		for(int x=0;x<caribellbAgendaAssistito.getItemCount();x++){ // JCariTableAgendaAssistito.getRowCount()
//			for(int y=0;y<=6;y++){
//				String valore = (String)((CaribelListModel) caribellbAgendaAssistito.getModel()).getFromRow(x, String.valueOf(y));
//				String nuova_prest=((String)hashDaPassare.get("prestazioni")).trim();
//				logger.info("caricaPeragenda val "+valore);
//				logger.info("caricaPeragenda prest "+nuova_prest);
//				logger.info("caricaPeragenda x,y="+x+","+y);
//				if(isPresente(nuova_prest,valore)){
//					if (x==0){
//						vMat_rm.add("M"+y);
//						vMat.add("M"+y);
//					}else if (x==1){
//						vPom_rm.add("P"+y);
//						vPom.add("P"+y);
//					}
//				}
//			}
//		}
//	}
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
		if(ControlloData(w_data,dataOdierna)<0){
			if(firsttime){
				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistra.msg.noRegistrazioneFutura"));
//				new it.pisa.caribel.swing2.cariInfoDialog(null,"Non si possono registrare prestazioni con data successiva a oggi.","Attenzione!").show();
				firsttime=false;
			} box.setChecked(false);
			return;
		}else firsttime=true;

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
				HtmlBasedComponent e = (HtmlBasedComponent) item.getChildren().get(col);
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
	
	void jCheckBoxInteraSettimana_actionPerformed(ActionEvent e) {
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
	}

	public void onCheck$jCheckBox1(Event e) {
		onColoraColonna(jCheckBox1, 3);
	}
	public void onCheck$jCheckBox2(Event e) {
		onColoraColonna(jCheckBox2, 4);
	}
	public void onCheck$jCheckBox3(Event e) {
		onColoraColonna(jCheckBox3, 5);
	}
	public void onCheck$jCheckBox4(Event e) {
		onColoraColonna(jCheckBox4, 6);
	}
	public void onCheck$jCheckBox5(Event e) {
		onColoraColonna(jCheckBox5, 7);
	}
	public void onCheck$jCheckBox6(Event e) {
		onColoraColonna(jCheckBox6, 8);
	}
	public void onChange$jCheckBox7(Event e) {
		onColoraColonna(jCheckBox7, 9);
	}

	public int getCartelleSelezionate(){
		CaribelListModel modello=(CaribelListModel) caribellbAgendaAssistito.getModel();
//		int numSelez=0;
		//TODO
//		int col_cart=modello.getColumnLocationByName("ag_cartella");
//		int col_cont=modello.getColumnLocationByName("ag_contatto");
////		gb 17/09/07 *******
//		int col_obie=modello.getColumnLocationByName("cod_obbiettivo");
//		int col_inte=modello.getColumnLocationByName("n_intervento");
////		gb 17/09/07: fine *******
//		int col_mp=modello.getColumnLocationByName("matt_pom");
//		int col=modello.getColumnLocationByName("x_assist");
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
			}
		}
		logger.info("hash HProgToMove finale--->"+HProgToMove.toString());
		return caribellbAgendaAssistito.getSelectedCount();
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

}

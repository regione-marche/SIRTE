package it.caribel.app.sinssnt.controllers.pianoAssistenziale;


import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.generic_controllers.CaribelGridItemRenderer;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForGridRenderer;
import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ComponentsCtrl;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;


public class PianoAccessiGridItemRenderer extends CaribelGridItemRenderer {

	protected boolean addedHeadCal = false;      
	protected CaribelListheader colCal = null;    

	public void render(Listitem item, Object myData, int index) throws Exception {
		CaribelDatebox temp = new CaribelDatebox();
		ComponentsCtrl.applyForward(item, "onDoubleClick=onDoubleClickedItem");

		final CaribelListbox listbox = (CaribelListbox)item.getParent();
		Listhead listhead = listbox.getListhead();

		appendHeadForButtons(listbox);
		
		Hashtable<String, ?> data = UtilForGridRenderer.getDataSetAttribute(item, myData);

		if(listhead != null){
			boolean column_editable = false;
			String column_type = "";
			String dbName = "";
			String valCorr = "";
			for (Object header : listhead.getChildren()) {
				dbName = ((CaribelListheader)header).getDb_name();
				column_editable = ((CaribelListheader)header).isColumn_editable();
				column_type = ((CaribelListheader)header).getColumn_type();
				
				valCorr = UtilForGridRenderer.getValoreCorrente(temp, data, dbName);
					
				if(column_editable && !column_type.equals("")){
					UtilForGridRenderer.appendEditableComponent(item, data, (CaribelListheader)header, valCorr,this.eventListener);
				}else{
					Listcell lc = new Listcell(valCorr);
					if(header instanceof Listheader && ((Listheader) header).getMaxlength()>0 && valCorr.length()>((Listheader) header).getMaxlength()){
						lc.setTooltiptext(valCorr);
					}
					if(this.addedHeadDel && header instanceof CaribelListheader && header.equals(listbox.getListhead().getAttribute("colDel"))){
						UtilForGridRenderer.appendBtnFormGridDelete(item);
					}else if(this.addedHeadEdt && header instanceof CaribelListheader && header.equals(listbox.getListhead().getAttribute("colEdt"))){
						UtilForGridRenderer.appendBtnFormGridEdit(item);
					}else if(this.addedHeadCal && header instanceof CaribelListheader && header.equals(listbox.getListhead().getAttribute("colCal"))){
						boolean flag = data.containsKey("pi_pianificato") && data.get("pi_pianificato").equals("S");
						appendBtnAgenda(item, flag);
					}else{	
						item.appendChild(lc);
					}
				}
			}			
		}
	}
	
	@Override
	protected void appendHeadForButtons(CaribelListbox listbox){
		try{
			super.appendHeadForButtons(listbox);

			addedHeadCal = listbox.getListhead().hasAttribute("agendable");
			if(!this.addedHeadCal){
				colCal = new CaribelListheader();
				colCal.setLabel("");
				colCal.setWidth("30px");
				listbox.getListhead().setAttribute("agendable", true);
				listbox.getListhead().setAttribute("colCal", colCal);
				listbox.getListhead().appendChild(colCal);
				listbox.getListhead().invalidate();
				this.addedHeadCal = true;
			}

		}catch(Exception ex){
			logger.warn("NON  E' STATO POSSIBILE APPENDERE LE COLONNE DI DELETE ed EDIT ALLA GRIGLIA id: "+listbox.getId());
		}
	}
	
	protected void appendBtnAgenda(Listitem item, boolean flag){
		//Appendo il pulsante di cancellazione
		final Listbox listbox = (Listbox)item.getParent();						
		Button btn = new Button();
		btn.setWidth("30px");
		String image = "";
		if(flag){
			image = "/web/img/AgCalendSpg.png";
		}else{
			String pattern = (String) ((Hashtable)item.getAttribute("ht_from_grid")).get(CostantiSinssntW.PIANIFICAZIONE_PAI);
			if(pattern == null || pattern.indexOf("S")==-1){
				image = "/web/img/AgCalend.png";
			}else{
				image = "/web/img/AgCalendSps.png";
			}
		}
		btn.setImage(image);
		btn.setTooltiptext(Labels.getLabel("pianoAssistenziale.pianoAccessi.btn_agenda"));
		btn.setStyle("background-color: Transparent; background-repeat:no-repeat; border: none; cursor:pointer; overflow: hidden; padding-left: 0px;");
		btn.addEventListener("onClick", new EventListener<Event>() {
            public void onEvent(Event event) {
//            	int index = ((Listitem)((Listcell)event.getTarget().getParent()).getParent()).getIndex();
//            	listbox.setSelectedIndex(index);
//            	Listitem item = ((Listitem)((Listcell)event.getTarget().getParent()).getParent());
//            	listbox.addItemToSelection(item);
            	if(listbox.isMultiple()){
                	Listitem item = ((Listitem)((Listcell)event.getTarget().getParent()).getParent());
                	listbox.addItemToSelection(item);	
            	}else{
            		int index = ((Listitem)((Listcell)event.getTarget().getParent()).getParent()).getIndex();
            		listbox.setSelectedIndex(index);
            	}
            	Events.sendEvent(Events.ON_SELECT, listbox, null);
            	Window myWin = (Window)((Window)listbox.getSpaceOwner()).getParent().getSpaceOwner();
            	Events.sendEvent("onOpenAgenda",myWin,null); 
            }
        });
		UtilForComponents.disableImageButton((btn), item.isDisabled());
		btn.setDisabled(item.isDisabled());
		Listcell cell = new Listcell();
		cell.appendChild(btn);
		item.appendChild(cell);
	}
}
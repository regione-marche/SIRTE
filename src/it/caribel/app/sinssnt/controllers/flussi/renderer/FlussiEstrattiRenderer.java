package it.caribel.app.sinssnt.controllers.flussi.renderer;


import it.caribel.app.sinssnt.util.Costanti;
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


public class FlussiEstrattiRenderer extends CaribelGridItemRenderer {

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
				if (dbName.equals("convalida")) valCorr = getDescrConvalida(valCorr);
				
				
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
					}else 
					if(((CaribelListheader)header).hasAttribute("download")){
//						boolean flag = data.containsKey("download");
						appendBtnDonwload(item);
					}else{	
						item.appendChild(lc);
					}
				}
			}			
		}
	}
	
	private String getDescrConvalida(String valCorr) {
		if (valCorr.equals(Costanti.ESTRAZIONE_CONVALIDATA)) return CostantiSinssntW.CONVALIDA_OK;
		else if (valCorr.equals(Costanti.ESTRAZIONE_SCARTATA)) return CostantiSinssntW.CONVALIDA_KO;
		else return CostantiSinssntW.CONVALIDA_WAIT	;		
	}

	protected void appendBtnDonwload(Listitem item){
		//Appendo il pulsante di scarico zip
		String image1;
		image1 = "/web/img/zip-icon.png";
		
		
		final Listbox listbox = (Listbox)item.getParent();						
		Button btn1 = new Button();
		btn1.setWidth("30px");
		btn1.setImage(image1);
		btn1.setTooltiptext(Labels.getLabel("flussi.siad.tooltip.zip"));
		btn1.setStyle("background-color: Transparent; background-repeat:no-repeat; border: none; cursor:pointer; overflow: hidden; padding-left: 0px;");
		btn1.addEventListener("onClick", new EventListener<Event>() {
            public void onEvent(Event event) {
            		int index = ((Listitem)((Listcell)event.getTarget().getParent()).getParent()).getIndex();
            		listbox.setSelectedIndex(index);
               	Events.sendEvent(Events.ON_SELECT, listbox, CostantiSinssntW.FORMATO_ZIP);
//            	Window myWin = (Window)((Window)listbox.getSpaceOwner()).getParent().getSpaceOwner();
//            	Events.sendEvent("onOpenAgenda",myWin,null); 
            }
        });
		UtilForComponents.disableImageButton((btn1), item.isDisabled());
		btn1.setDisabled(item.isDisabled());
		Listcell cell1 = new Listcell();
		cell1.appendChild(btn1);
			
		
		
		item.appendChild(cell1);
	
	
	}
}
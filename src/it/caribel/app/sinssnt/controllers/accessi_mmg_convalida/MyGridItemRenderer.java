package it.caribel.app.sinssnt.controllers.accessi_mmg_convalida;


import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.generic_controllers.CaribelGridItemRenderer;
import it.caribel.zk.util.UtilForGridRenderer;
import it.pisa.caribel.util.ISASUtil;

import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.sys.ComponentsCtrl;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;

public class MyGridItemRenderer extends CaribelGridItemRenderer{

	public static String legendaH = "Prestazione caricata";
	public static String legendaG = "Prestazione da caricare";
	public static String legendaF = "Prestazione rimossa";
	public static String legendaC = "Manca autorizzazione";
	public static String legendaD = "Autor. fuori range";
	public static String legendaE = "Prestazione duplicata";
	public static String legendaA = "Non esiste l'assistito";
	public static String legendaB = "Non esiste la prestazione";
	
	
	@Override
	public void render(Listitem item, Object myData, int index) throws Exception {
		CaribelDatebox temp = new CaribelDatebox();
		ComponentsCtrl.applyForward(item, "onDoubleClick=onDoubleClickedItem");

		final CaribelListbox listbox = (CaribelListbox)item.getParent();
		Listhead listhead = listbox.getListhead();
		
		Hashtable<String, ?> data = UtilForGridRenderer.getDataSetAttribute(item, myData);

		impostaColoreAndLegenda(item, data, index);
		
		if(listhead != null){
			String dbName = "";
			String valCorr = "";
			for (Object header : listhead.getChildren()) {
				dbName = ((CaribelListheader)header).getDb_name();
				valCorr = UtilForGridRenderer.getValoreCorrente(temp, data, dbName);	
				Listcell lc = new Listcell(valCorr);
				
				String caricato = ISASUtil.getValoreStringa(data, "flag_caricato");
				String ripeti_prest = ISASUtil.getValoreStringa(data, "flag_err_ripetuta");
				String prest = ISASUtil.getValoreStringa(data, "flag_err_prestazione");				
				if(header instanceof CaribelListheader &&
						((CaribelListheader)header).getId()!=null &&
						((CaribelListheader)header).getId().equals("colDel")){
					Button btn = UtilForGridRenderer.appendBtnDeleteRow(item); 
					if (caricato.trim().equals("S") && (!ripeti_prest.equals("0") || prest.equals("5"))){
						btn.setVisible(true); 
						btn.setTooltiptext(Labels.getLabel("accessi_mmg_convalida.btn_rimuovi.tooltiptext"));
					 }else{
//						 btn.setTooltiptext(Labels.getLabel("accessi_mmg_convalida.btn_rimuovi.alert"));
//						 UtilForComponents.disableImageButton(btn, true);
//						 btn.setDisabled(true);
						 btn.setVisible(false);
					 }
					//item.setDisabled(true);
				}else{	
					item.appendChild(lc);
				}
			}			
		}
	}
	
	
	private void impostaColoreAndLegenda(Listitem item, Hashtable<String, ?> data, int index){
		String caricato = ISASUtil.getValoreStringa(data, "flag_caricato");
		String ripeti_prest = ISASUtil.getValoreStringa(data, "flag_err_ripetuta");
		if(caricato.equals("S") && (ripeti_prest.equals("") || ripeti_prest.equals("0"))){
			item.setClass("z-listbox-row-white");
			item.setCheckable(false);
			return;
		}
		if(caricato.equals("C")){
			item.setClass("z-listbox-row-gray");
			item.setTooltiptext(legendaF);
			item.setCheckable(false);
			return;
		}
		String ass = ISASUtil.getValoreStringa(data, "flag_err_assistito");
		if (ass!=null && !ass.equals("0")){
			item.setClass("z-listbox-row-pink");
			item.setTooltiptext(legendaA);
			item.setCheckable(false);
			return;
		}		
		String prest = ISASUtil.getValoreStringa(data, "flag_err_prestazione");
		if (prest!=null && !prest.equals("0") && !prest.equals("5")){
			item.setClass("z-listbox-row-celeste");
			item.setTooltiptext(legendaB);
			item.setCheckable(false);
			return;
		}else if (prest!=null && prest.equals("5")){//bargi 22/01/2016
			item.setClass("z-listbox-row-yellow");
			item.setTooltiptext(legendaE);
			item.setCheckable(false);
			return;
		}
		
		if (!ripeti_prest.equals("") && !ripeti_prest.equals("0")){
			item.setClass("z-listbox-row-yellow");
			item.setTooltiptext(legendaE);
			item.setCheckable(false);
			return;
		}
		String auto = ISASUtil.getValoreStringa(data, "flag_err_autorizzazioni");
		if (!auto.trim().equals("")&& !auto.equals("0") ){
            if(auto.equals("1")){
	            item.setClass("z-listbox-row-red");
				item.setTooltiptext(legendaC);
				return;
            }else {
	            item.setClass("z-listbox-row-orange");
				item.setTooltiptext(legendaD);
				return;
            }
		}
		if(caricato.equals("N") || caricato.equals("")){
			item.setClass("z-listbox-row-verdino");
			item.setTooltiptext(legendaG);
			item.setCheckable(true);
			return;
		}				
	};
	
	
	
}
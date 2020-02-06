package it.caribel.app.sinssnt.controllers.tabelle.interventiSIL;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.modificati.ConsultazioneSILEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class InterventiSILSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private ConsultazioneSILEJB myEJB = new ConsultazioneSILEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/interventiSIL/interventiSILGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("n_cartella", "des_cartella", null);
		super.doAfterCompose(comp);
	}
	
}

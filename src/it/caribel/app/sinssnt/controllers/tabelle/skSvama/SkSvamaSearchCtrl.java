package it.caribel.app.sinssnt.controllers.tabelle.skSvama;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.SkSvamaEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class SkSvamaSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private SkSvamaEJB myEJB = new SkSvamaEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/skSvama/skSvamaGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("n_cartella", "des_cartella", null);
		super.doAfterCompose(comp);
	}
	
}

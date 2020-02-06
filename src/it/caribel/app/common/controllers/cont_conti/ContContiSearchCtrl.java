package it.caribel.app.common.controllers.cont_conti;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.ContiEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ContContiSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private ContiEJB myEJB = new ContiEJB();
	private String myPathGridZul = "/web/ui/common/cont_conti/contContiGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("eco_codice", "eco_descri", null);
		super.doAfterCompose(comp);
	}
	
}

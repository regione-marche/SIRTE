package it.caribel.app.sinssnt.controllers.tabelle.ele_segnala_ospedale;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.modificati.DimissOspEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class EleSegnalaOspedaleSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private DimissOspEJB myEJB = new DimissOspEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/ele_segnala_ospedale/eleSegnalaOspedaleGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("cod_ospedale", "desc_ospedale", null);
		super.doAfterCompose(comp);
	}
	
}

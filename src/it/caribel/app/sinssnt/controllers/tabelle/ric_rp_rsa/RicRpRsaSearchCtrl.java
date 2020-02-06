package it.caribel.app.sinssnt.controllers.tabelle.ric_rp_rsa;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.modificati.GestRpRsaEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class RicRpRsaSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private GestRpRsaEJB myEJB = new GestRpRsaEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/ric_rp_rsa/ricRpRsaGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("n_cartella", "cognome", null);
		super.doAfterCompose(comp);
	}
	
}

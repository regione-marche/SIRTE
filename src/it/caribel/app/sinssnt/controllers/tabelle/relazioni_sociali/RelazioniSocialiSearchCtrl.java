package it.caribel.app.sinssnt.controllers.tabelle.relazioni_sociali;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.RpRelazioneSocialeEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class RelazioniSocialiSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RpRelazioneSocialeEJB myEJB = new RpRelazioneSocialeEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/relazioni_sociali/relazioniSocialiGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("n_cartella", "cognome", null);
		super.doAfterCompose(comp);
	}
	
}

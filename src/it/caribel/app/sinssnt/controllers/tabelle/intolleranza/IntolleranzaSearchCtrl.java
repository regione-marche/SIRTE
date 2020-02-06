package it.caribel.app.sinssnt.controllers.tabelle.intolleranza;

import it.caribel.app.common.ejb.OperatoriEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class IntolleranzaSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private OperatoriEJB myEJB = new OperatoriEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/intolleranze/intolleranzeGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione","");
		super.doAfterCompose(comp);
	}
}

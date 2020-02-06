package it.caribel.app.sinssnt.controllers.tabelle.piano_intervPAP;

import it.caribel.app.sinssnt.bean.TabPapEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class PianoIntervPAPSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private TabPapEJB myEJB = new TabPapEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/piano_intervPAP/pianoIntervPAPGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione",null);
		super.doAfterCompose(comp);
	}
}

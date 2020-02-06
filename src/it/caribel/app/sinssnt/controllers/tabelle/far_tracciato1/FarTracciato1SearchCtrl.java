package it.caribel.app.sinssnt.controllers.tabelle.far_tracciato1;

import it.caribel.app.sinssnt.bean.SINSFarTracc1EJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class FarTracciato1SearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private SINSFarTracc1EJB myEJB = new SINSFarTracc1EJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/far_tracciato1/farTracciato1Grid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("n_cartella", "cognome",null);
		super.doAfterCompose(comp);
	}
}

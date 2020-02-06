package it.caribel.app.common.controllers.contributi;

import it.caribel.app.sinssnt.bean.SussidiEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ContributiSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private SussidiEJB myEJB = new SussidiEJB();
	private String myPathGridZul = "/web/ui/common/contributi/contributiGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice_suss", "descrizione_suss", null);
		super.doAfterCompose(comp);
	}
}

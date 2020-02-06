package it.caribel.app.common.controllers.prestaz_bisogni;

import org.zkoss.zk.ui.Component;

import it.caribel.app.common.ejb.MediciEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

public class PrestazBisogniSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private MediciEJB myEJB = new MediciEJB();
	private String myPathGridZul = "/web/ui/common/prestaz_bisogni/prestazBisogniGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		//super.setColumnsNameForDecod("mecodi", "mecogn", "menome");
		super.doAfterCompose(comp);
	}
}

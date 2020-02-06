package it.caribel.app.sinssnt.controllers.tabelle.prog_aperti;

import it.caribel.app.sinssnt.bean.SocAssProgettoEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class AsProgettiApertiSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private SocAssProgettoEJB myEJB = new SocAssProgettoEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/prog_aperti/asProgettiApertiGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("da_data", "a_data",null);
		super.doAfterCompose(comp);
	}
}

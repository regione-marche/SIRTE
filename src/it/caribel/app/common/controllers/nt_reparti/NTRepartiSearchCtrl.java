package it.caribel.app.common.controllers.nt_reparti;

import it.caribel.app.common.ejb.RepartiEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class NTRepartiSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RepartiEJB myEJB = new RepartiEJB();
	private String myPathGridZul = "/web/ui/common/nt_reparti/nt_repartiGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cd_rep", "reparto", null);
		super.doAfterCompose(comp);
	}
}

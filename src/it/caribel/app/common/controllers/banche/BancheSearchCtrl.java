package it.caribel.app.common.controllers.banche;

import it.caribel.app.sinssnt.bean.BancheEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class BancheSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private BancheEJB myEJB = new BancheEJB();
	private String myPathGridZul = "/web/ui/common/banche/bancheGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("ban_codice_abi", "ban_cab_sport", null);
		super.doAfterCompose(comp);
	}
}

package it.caribel.app.common.controllers.statoProf;

import it.caribel.app.sinssnt.bean.PosproEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class StatoProfSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private PosproEJB myEJB = new PosproEJB();
	private String myPathGridZul = "/web/ui/common/statoProf/statoProfGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

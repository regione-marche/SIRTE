package it.caribel.app.common.controllers.soctab;

import it.caribel.app.sinssnt.bean.SSoctabEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class SoctabSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private SSoctabEJB myEJB = new SSoctabEJB();
	private String myPathGridZul = "/web/ui/common/soctab/soctabGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

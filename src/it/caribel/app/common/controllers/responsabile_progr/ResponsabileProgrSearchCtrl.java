package it.caribel.app.common.controllers.responsabile_progr;

import it.caribel.app.sinssnt.bean.RespargEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ResponsabileProgrSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RespargEJB myEJB = new RespargEJB();
	private String myPathGridZul = "/web/ui/common/responsabile_progr/responsabilePROGGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

package it.caribel.app.common.controllers.ruoli_operatore;

import it.caribel.app.sinssnt.bean.RuoliOperatoreEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class RuoliOperatoreSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RuoliOperatoreEJB myEJB = new RuoliOperatoreEJB();
	private String myPathGridZul = "/web/ui/common/ruoli_operatore/ruoliOperatoreGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("kuser", "descr", null);
		super.doAfterCompose(comp);
	}
}

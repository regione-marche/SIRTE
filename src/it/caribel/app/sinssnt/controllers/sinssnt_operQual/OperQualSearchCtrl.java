package it.caribel.app.sinssnt.controllers.sinssnt_operQual;

import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class OperQualSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private OperqualEJB myEJB = new OperqualEJB();
	private String myPathGridZul = "/web/ui/sinssnt/sinssnt_operQual/operQualGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cod_qualif", "desc_qualif", null);
		super.doAfterCompose(comp);
	}
}

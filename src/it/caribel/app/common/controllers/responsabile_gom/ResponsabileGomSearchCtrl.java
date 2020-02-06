package it.caribel.app.common.controllers.responsabile_gom;

import it.caribel.app.sinssnt.bean.ResgomEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ResponsabileGomSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private ResgomEJB myEJB = new ResgomEJB();
	private String myPathGridZul = "/web/ui/common/responsabile_gom/responsabileGOMGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

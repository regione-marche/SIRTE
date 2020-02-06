package it.caribel.app.common.controllers.grado_parent;

import it.caribel.app.common.ejb.ParentEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class GradoParentSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private ParentEJB myEJB = new ParentEJB();
	private String myPathGridZul = "/web/ui/common/grado_parent/gradoParentGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

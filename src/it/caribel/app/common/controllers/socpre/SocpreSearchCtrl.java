package it.caribel.app.common.controllers.socpre;

import it.caribel.app.sinssnt.bean.SSoctpreEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class SocpreSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private SSoctpreEJB myEJB = new SSoctpreEJB();
	private String myPathGridZul = "/web/ui/common/socpre/socpreGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

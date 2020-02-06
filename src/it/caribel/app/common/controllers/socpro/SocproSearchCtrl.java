package it.caribel.app.common.controllers.socpro;

import it.caribel.app.sinssnt.bean.SSoctproEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class SocproSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private SSoctproEJB myEJB = new SSoctproEJB();
	private String myPathGridZul = "/web/ui/common/socpro/socproGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

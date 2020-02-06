package it.caribel.app.common.controllers.pensioni;

import it.caribel.app.sinssnt.bean.PensioniEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class PensioniSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private PensioniEJB myEJB = new PensioniEJB();
	private String myPathGridZul = "/web/ui/common/pensioni/pensioniGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("pe_codice", "pe_descri", null);
		super.doAfterCompose(comp);
	}
}

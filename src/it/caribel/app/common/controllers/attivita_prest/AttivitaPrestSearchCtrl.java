package it.caribel.app.common.controllers.attivita_prest;

import it.caribel.app.sinssnt.bean.SocAssAttPrestEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class AttivitaPrestSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private SocAssAttPrestEJB myEJB = new SocAssAttPrestEJB();
	private String myPathGridZul = "/web/ui/common/attivita_prest/attivitaPrestGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cod_attivita", "des_attivita", null);
		super.doAfterCompose(comp);
	}
}

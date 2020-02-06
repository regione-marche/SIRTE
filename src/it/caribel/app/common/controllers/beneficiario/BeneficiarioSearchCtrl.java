package it.caribel.app.common.controllers.beneficiario;

import org.zkoss.zk.ui.Component;

import it.caribel.app.sinssnt.bean.modificati.BeneficiarioEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

public class BeneficiarioSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private BeneficiarioEJB myEJB = new BeneficiarioEJB();
	private String myPathGridZul = "/web/ui/common/beneficiario/beneficiarioGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("b_codice", "b_cognome", null);
		super.doAfterCompose(comp);
	}
}

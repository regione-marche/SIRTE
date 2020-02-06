package it.caribel.app.common.controllers.cont_centro_costo;

import it.caribel.app.sinssnt.bean.CentriCostoEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ContCentroCostoSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private CentriCostoEJB myEJB = new CentriCostoEJB();
	private String myPathGridZul = "/web/ui/common/cont_centro_costo/contCentroCostoGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

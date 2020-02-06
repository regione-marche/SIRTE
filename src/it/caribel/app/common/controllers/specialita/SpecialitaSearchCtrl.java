package it.caribel.app.common.controllers.specialita;

import org.zkoss.zk.ui.Component;

import it.caribel.app.sinssnt.bean.MedspecEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

public class SpecialitaSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private MedspecEJB myEJB = new MedspecEJB();
	private String myPathGridZul = "/web/ui/common/specialita/specialitaGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}
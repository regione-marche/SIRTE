package it.caribel.app.common.controllers.bisogni_ass;

import org.zkoss.zk.ui.Component;

import it.caribel.app.sinssnt.bean.BisAssEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

public class BisogniAssSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private BisAssEJB myEJB = new BisAssEJB();
	private String myPathGridZul = "/web/ui/common/bisogni_ass/bisogniAssGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("tba_codice", "tba_descrizione", null);
		super.doAfterCompose(comp);
	}
}

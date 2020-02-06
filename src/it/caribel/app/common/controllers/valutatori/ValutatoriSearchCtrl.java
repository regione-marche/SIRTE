package it.caribel.app.common.controllers.valutatori;

import it.caribel.app.sinssnt.bean.TabPrvalEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ValutatoriSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private TabPrvalEJB myEJB = new TabPrvalEJB();
	private String myPathGridZul = "/web/ui/common/valutatori/valutatoriGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("tv_codice", "tv_descrizione", null);
		super.doAfterCompose(comp);
	}
}

package it.caribel.app.common.controllers.branca;

import org.zkoss.zk.ui.Component;

import it.caribel.app.sinssnt.bean.modificati.BrancaEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

public class BrancaSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private BrancaEJB myEJB = new BrancaEJB();
	private String myPathGridZul = "/web/ui/common/branca/brancaGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

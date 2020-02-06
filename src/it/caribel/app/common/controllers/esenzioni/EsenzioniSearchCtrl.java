package it.caribel.app.common.controllers.esenzioni;

import it.caribel.app.sinssnt.bean.EsenzioniEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class EsenzioniSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private EsenzioniEJB myEJB = new EsenzioniEJB();
	private String myPathGridZul = "/web/ui/common/esenzioni/esenzioniGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cod_esenzione", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

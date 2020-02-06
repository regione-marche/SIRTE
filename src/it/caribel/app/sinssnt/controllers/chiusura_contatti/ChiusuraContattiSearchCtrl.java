package it.caribel.app.sinssnt.controllers.chiusura_contatti;

import it.caribel.app.sinssnt.bean.modificati.ChiudiContattoEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ChiusuraContattiSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private ChiudiContattoEJB myEJB = new ChiudiContattoEJB();
	private String myPathGridZul = "/web/ui/sinssnt/chiusura_contatti/chiusuraContatti.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cod_diagnosi", "diagnosi","diagnosi1");
		super.doAfterCompose(comp);
	}
}

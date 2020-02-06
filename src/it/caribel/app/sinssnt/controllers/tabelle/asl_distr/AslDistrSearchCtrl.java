package it.caribel.app.sinssnt.controllers.tabelle.asl_distr;

import it.caribel.app.sinssnt.bean.RLAslDistrettiEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class AslDistrSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private RLAslDistrettiEJB myEJB = new RLAslDistrettiEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/asl_distr/aslDistrGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice_asl_distr", "descrizione",null);
		super.doAfterCompose(comp);
	}
}

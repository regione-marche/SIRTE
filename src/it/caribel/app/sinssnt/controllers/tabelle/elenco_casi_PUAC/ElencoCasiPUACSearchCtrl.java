package it.caribel.app.sinssnt.controllers.tabelle.elenco_casi_PUAC;

import it.caribel.app.sinssnt.bean.SocElencocasiEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class ElencoCasiPUACSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private SocElencocasiEJB myEJB = new SocElencocasiEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/elenco_casi_PUAC/elencoCasiPUACGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("da_data", "a_data",null);
		super.doAfterCompose(comp);
	}
}

package it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm;

import it.caribel.app.sinssnt.bean.modificati.CommissUVMEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class CommissioneUvmSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private CommissUVMEJB myEJB = new CommissUVMEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/commissione_uvm/commissioneUvmGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cm_cod_comm", "cm_descr",null);
		super.doAfterCompose(comp);
	}
}

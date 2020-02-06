package it.caribel.app.sinssnt.controllers.tabelle.diagnosi;

import java.util.Hashtable;

import it.caribel.app.common.ejb.TabDiagnosiEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class DiagnosiSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private TabDiagnosiEJB myEJB = new TabDiagnosiEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/diagnosi/diagnosiGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("cod_diagnosi", "diagnosi","diagnosi1");
		super.doAfterCompose(comp);
	}
	
	@Override
	public Hashtable<String, String> getOtherParameterForQuery(){
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put("order", "diagnosi asc");
		return ret;
	}
}

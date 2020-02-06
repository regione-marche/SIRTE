package it.caribel.app.sinssnt.controllers.interventi;

import it.caribel.app.sinssnt.bean.modificati.IntervEJB;
import it.caribel.app.sinssnt.controllers.tabelle.operatori.OperatoriGridCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class OperatoriQualSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private IntervEJB myEJB = new IntervEJB();
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,OperatoriGridCtrl.myPathZul,1);
		super.setColumnsNameForDecod("codice", "cognome", "nome");
		super.doAfterCompose(comp);
		setMethodNameForQueryKey("query_operatoreQual");
	}
}

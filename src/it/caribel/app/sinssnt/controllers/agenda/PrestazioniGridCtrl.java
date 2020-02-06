package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.common.controllers.prestazioni.PrestazioniFormCtrl;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;

import org.zkoss.zk.ui.Component;

public class PrestazioniGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	private AgendaEJB myEJB = new AgendaEJB();
	private String myKeyPermission = PrestazioniFormCtrl.myKeyPermission;
	private String myPathFormZul = "/web/ui/common/prestazioni/prestazioniForm.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		setMethodNameForQuery("CaricaTabellaPrestazioni");
		doCerca();
    }
	
	public void doStampa() {		
		
	}
	
	public void doCerca(){		
		try{
			super.hParameters.put("referente", UtilForContainer.getTipoOperatorerContainer());
			doRefresh();
		}catch(Exception e){
			doShowException(e);
		}
	}
}

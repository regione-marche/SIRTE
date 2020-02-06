package it.caribel.app.common.controllers.tipoAssistito;

import it.caribel.app.sinssnt.bean.RsaTipoAssistitoEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class TipoAssistitoSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RsaTipoAssistitoEJB myEJB = new RsaTipoAssistitoEJB();
	private String myPathGridZul = "/web/ui/common/tipoAssistito/tipoAssistitoGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione", null);
		super.doAfterCompose(comp);
	}
}

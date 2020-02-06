package it.caribel.app.sinssnt.controllers.tabelle.tipo_assistito;

import it.caribel.app.sinssnt.bean.RsaTipoAssistitoEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class TipoAssistitoSearchCtrl extends CaribelSearchCtrl {
	
	private static final long serialVersionUID = 1L;
	
	private RsaTipoAssistitoEJB myEJB = new RsaTipoAssistitoEJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/tipo_assistito/tipoAssistitoGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("codice", "descrizione",null);
		super.doAfterCompose(comp);
	}
}

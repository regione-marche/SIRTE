package it.caribel.app.sinssnt.controllers.tabelle.tipoIstituto;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.RsaTipoIstitutoSts11EJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class TipoIstitutoSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RsaTipoIstitutoSts11EJB myEJB = new RsaTipoIstitutoSts11EJB();
	private String myPathGridZul = "/web/ui/sinssnt/tabelle/tipoIstituto/tipoIstitutoGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("tar_codice", "tar_descri", null);
		super.doAfterCompose(comp);
	}
	
}

package it.caribel.app.common.controllers.sussidi_filtrati;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.RsaContiEJB;
import it.caribel.app.sinssnt.bean.RsaCentroCostiEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class SussidiFiltratiSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RsaContiEJB myEJB = new RsaContiEJB();
	private String myPathGridZul = "/web/ui/common/sussidi_filtrati/sussidiFiltratiGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("codice_suss", "descrizione_suss", null);
		super.doAfterCompose(comp);
	}
	
}

package it.caribel.app.common.controllers.rsa_conti;

import java.util.Hashtable;


import it.caribel.app.sinssnt.bean.RsaContiEJB;
import it.caribel.app.sinssnt.bean.RsaCentroCostiEJB;

import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class RsaContiSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RsaContiEJB myEJB = new RsaContiEJB();
	private String myPathGridZul = "/web/ui/common/rsa_conti/rsaContiGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("eco_codice", "eco_descri", null);
		super.doAfterCompose(comp);
	}
	
}

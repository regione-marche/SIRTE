package it.caribel.app.common.controllers.rsa_motDim;

import it.caribel.app.sinssnt.bean.RsaMotiEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class RsaMotDimSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RsaMotiEJB myEJB = new RsaMotiEJB();
	private String myPathGridZul = "/web/ui/common/rsa_motDim/rsaMotDimGrid.zul";
		
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		super.setColumnsNameForDecod("md_codice", "md_descri", null);
		super.doAfterCompose(comp);
	}
}

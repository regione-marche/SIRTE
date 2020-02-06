package it.caribel.app.common.controllers.tipoTariffa;

import it.caribel.app.sinssnt.bean.RsaTipoTariffaEJB;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;

import org.zkoss.zk.ui.Component;

public class TipoTariffaSearchCtrl extends CaribelSearchCtrl {

	private static final long serialVersionUID = 1L;
	
	private RsaTipoTariffaEJB myEJB = new RsaTipoTariffaEJB();
	private String myPathGridZul = "/web/ui/common/tipoTariffa/tipoTariffaGrid.zul";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelSearchCtrl(myEJB,myPathGridZul,1);
		
		super.setColumnsNameForDecod("tar_codice", "tar_descri", null);
		super.doAfterCompose(comp);
	}
	
}

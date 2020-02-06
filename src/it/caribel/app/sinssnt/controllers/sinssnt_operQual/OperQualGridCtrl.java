package it.caribel.app.sinssnt.controllers.sinssnt_operQual;

import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.zk.ui.Component;

public class OperQualGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "OPERQUAL";
	private OperqualEJB myEJB = new OperqualEJB();
	private String myPathFormZul = "/web/ui/sinssnt/sinssnt_operQual/operQualForm.zul";
	
	private CaribelTextbox tb_filter1;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		if(super.caribelSearchCtrl!=null){
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get("textToSearch");
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				tb_filter1.setText(textToSearch);
				super.hParameters.put(tb_filter1.getDb_name(), textToSearch);
				doRefresh();
			}
		}
    }
	
	public void doStampa() {		

	}
	
	public void doCerca() {		
		super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getValue().toUpperCase());
		doRefresh();
	}
}

package it.caribel.app.common.controllers.specialita;

import it.caribel.app.sinssnt.bean.MedspecEJB;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

public class SpecialitaGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = SpecialitaFormCtrl.myKeyPermission;
	private MedspecEJB myEJB = new MedspecEJB();
	private String myPathFormZul = "/web/ui/common/specialita/specialitaForm.zul";
	
	private CaribelTextbox tb_filter1;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		
		super.doAfterCompose(comp);
		tb_filter1.setFocus(true);
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
	
	public void doCerca(){		
		super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getValue().toUpperCase());
		doRefresh();
	}	
	public void doPulisciRicerca() {
		try {
			setDefault();
		} catch (Exception e) {
			logger.error(this.getClass().getName()+": Impossibile inizializzare il reparto, rivolgersi all'assistenza");
		}
	}
	
private void setDefault() throws Exception{		
		
		if(caribellb.getItemCount()>0){
			caribellb.getItems().clear(); //.jCariTable1.deleteAll();
		}
		
		tb_filter1.setValue("");
		
	}

}

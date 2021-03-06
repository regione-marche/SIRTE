
package it.caribel.app.sinssnt.controllers.tabelle.relazioni_sociali;

import java.util.Hashtable;

import it.caribel.app.sinssnt.bean.RpRelazioneSocialeEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

public class RelazioniSocialiGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	private Hashtable<String, Object> parkSetting;
	private String myKeyPermission = "G_RPRELS";
	private RpRelazioneSocialeEJB myEJB = new RpRelazioneSocialeEJB();
	private String myPathFormZul = "/web/ui/sinssnt/tabelle/relazioni_sociali/relazioniSocialiForm.zul";
	
	
	protected CaribelTextbox tb_filter1;
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);		
		super.doAfterCompose(comp);
		
		if(super.caribelSearchCtrl!=null){
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get("textToSearch");
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				tb_filter1.setValue(textToSearch);				
				super.hParameters.put(tb_filter1.getDb_name(), textToSearch);
				
				doRefresh();
			}
		}
    }
	
	public void doStampa() {		
		
	}
	
	public void doCerca(){		
		try{
			UtilForComponents.testRequiredFields(self);

			
				super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getValue().toUpperCase());			
				super.hParameters.put(dadata.getDb_name(), dadata.getValueForIsas());
				super.hParameters.put(adata.getDb_name(), adata.getValueForIsas());
				doRefresh();
			
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doPulisciRicerca() {
		try {
			setDefault();
			UtilForBinding.resetForm(self,this.parkSetting);
			this.hParameters.clear();
		} catch (Exception e) {
			logger.error(this.getClass().getName()+": Impossibile inizializzare il reparto, rivolgersi all'assistenza");
		}
	}
	
private void setDefault() throws Exception{		
		
		if(caribellb.getItemCount()>0){
			caribellb.getItems().clear(); //.jCariTable1.deleteAll();
		}
		
		tb_filter1.setValue("");
		dadata.setValue(null);		
		adata.setValue(null);
		
		
	}
public void doNuovo() {		
	Messagebox.show(
			Labels.getLabel("exception.NotYetImplementedException.msg"),
			Labels.getLabel("messagebox.attention"),
			Messagebox.OK,
			Messagebox.INFORMATION);
	
}
public void doApri(){		
	Messagebox.show(
			Labels.getLabel("exception.NotYetImplementedException.msg"),
			Labels.getLabel("messagebox.attention"),
			Messagebox.OK,
			Messagebox.INFORMATION);
}

}

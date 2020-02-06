package it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm;

import it.caribel.app.sinssnt.bean.modificati.CommissUVMEJB;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

public class CommissioneUvmGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "";
	private CommissUVMEJB myEJB = new CommissUVMEJB();
	private String myPathFormZul = "/web/ui/sinssnt/tabelle/commissione_uvm/commissioneUvmForm.zul";
	
	private CaribelTextbox tb_filter1;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);		
		((Window)self).setTitle(getTitoloForm());
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
	public void doPulisciRicerca() {
		try {
			setDefault();
		} catch (Exception e) {
			logger.error(this.getClass().getName()+": Impossibile inizializzare l'operatore, rivolgersi all'assistenza");
		}
	}
	
private void setDefault() throws Exception{		
		
		if(caribellb.getItemCount()>0){
			caribellb.getItems().clear(); //.jCariTable1.deleteAll();
		}
		
		tb_filter1.setValue("");
		
	}


private String getTitoloForm(){
	  String ret = "";
	  String labelTitle = Labels.getLabel("commissioneUvm.formTitle");
	 
	        String confUVM = getProfile().getStringFromProfile("titolo_uvm");
	        String UVM = ((confUVM!=null && !confUVM.trim().equals("NO"))?confUVM:"UV");
	        ret = labelTitle +" "+UVM;
	        return ret.toUpperCase();
	 }
}

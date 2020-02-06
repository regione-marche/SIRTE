package it.caribel.app.sinssnt.controllers.tabelle.asl_distr;

import it.caribel.app.sinssnt.bean.RLAslDistrettiEJB;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

public class AslDistrGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "ASLDIST";
	private RLAslDistrettiEJB myEJB = new RLAslDistrettiEJB();
	private String myPathFormZul = "/web/ui/sinssnt/tabelle/asl_distr/aslDistrForm.zul";
	
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

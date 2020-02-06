package it.caribel.app.sinssnt.controllers.tabelle.prog_aperti;

import it.caribel.app.sinssnt.bean.SocAssProgettoEJB;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

public class AsProgettiApertiGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "A_RICPRG";
	private SocAssProgettoEJB myEJB = new SocAssProgettoEJB();
	private String myPathFormZul = "/web/ui/sinssnt/tabelle/prog_aperti/asProgettiApertiForm.zul";
	
	
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
				dadata.setText(textToSearch);
				super.hParameters.put(dadata.getDb_name(), textToSearch);
				
				doRefresh();
			}
		}
    }
	
	public void doStampa() {		
		
	}
	
	public void doCerca() {
		if ((dadata.getValueForIsas()!=null && !dadata.getValueForIsas().equals(""))
				&& (adata.getValueForIsas()!=null && !adata.getValueForIsas().equals("")) 
				)
		{
		
		super.hParameters.put(dadata.getDb_name(), dadata.getValueForIsas());
		super.hParameters.put(adata.getDb_name(), adata.getValueForIsas());
		
		doRefresh();
		}
		else
			Messagebox.show(
					Labels.getLabel("exception.filtriDateObbligatori.msg"),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.INFORMATION);
		
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

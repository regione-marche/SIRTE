package it.caribel.app.sinssnt.controllers.tabelle.far_tracciato1;

import it.caribel.app.sinssnt.bean.SINSFarTracc1EJB;
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

public class FarTracciato1GridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "FAR_TRAC";
	private SINSFarTracc1EJB myEJB = new SINSFarTracc1EJB();
	private String myPathFormZul = "/web/ui/sinssnt/tabelle/far_tracciato1/farTracciato1Form.zul";
	
	private CaribelIntbox tb_filter1;
	private CaribelTextbox tb_filter2;
	private CaribelTextbox tb_filter3;
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	private CaribelRadiogroup tb_filter6;
	protected Radio am_dim;
	
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
		
		if (tb_filter1.getText()!=null && !tb_filter1.getText().equals("") 
				|| (tb_filter2.getValue()!=null && !tb_filter2.getValue().equals(""))
				|| (tb_filter3.getValue()!=null && !tb_filter3.getValue().equals(""))
				|| (dadata.getValueForIsas()!=null && !dadata.getValueForIsas().equals(""))
				|| (adata.getValueForIsas()!=null && !adata.getValueForIsas().equals("")) 
				)
		{
		super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getText());
		super.hParameters.put(tb_filter2.getDb_name(), tb_filter2.getValue().toUpperCase());
		super.hParameters.put(tb_filter3.getDb_name(), tb_filter3.getValue().toUpperCase());
		super.hParameters.put(dadata.getDb_name(), dadata.getValueForIsas());
		super.hParameters.put(adata.getDb_name(), adata.getValueForIsas());
		super.hParameters.put(tb_filter6.getDb_name(), tb_filter6.getSelectedItem().getValue());
		doRefresh();
		}
		else
			Messagebox.show(
					Labels.getLabel("exception.filtriObbligatori.msg"),
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
		
		tb_filter1.setText("");
		tb_filter2.setValue("");
		tb_filter3.setValue("");
		dadata.setValue(null);		
		adata.setValue(null);
		am_dim.setChecked(true);
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

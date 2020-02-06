package it.caribel.app.common.controllers.tipoTariffa;

import it.caribel.app.rsa.bean.modificati.RsaOrganizzazioneEJB;
import it.caribel.app.rsa.bean.modificati.RsaTipoIstitutoEJB;
import it.caribel.app.sinssnt.bean.RsaTipoTariffaEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;

public class TipoTariffaGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	private Hashtable<String, Object> parkSetting;
	private String myKeyPermission = "";
	private RsaTipoTariffaEJB myEJB = new RsaTipoTariffaEJB();
	private String myPathFormZul = "/web/ui/common/tipoTariffa/tipoTariffaForm.zul";
	
	protected CaribelCombobox tb_filter1;
	protected CaribelCombobox tb_filter2;
	private CaribelTextbox tb_filter3;
	private CaribelTextbox tb_filter4;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		caricaComboIstituti();
		caricaComboOrganizzazione();
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

			if(tb_filter1.getValue()!=null && !tb_filter1.getValue().equals(""))
				super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getSelectedItem().getValue());
			if(tb_filter2.getValue()!=null && !tb_filter2.getValue().equals(""))
			super.hParameters.put(tb_filter2.getDb_name(), tb_filter2.getSelectedItem().getValue());
			super.hParameters.put(tb_filter3.getDb_name(), tb_filter3.getValue().toUpperCase());
			super.hParameters.put(tb_filter4.getDb_name(), tb_filter4.getValue().toUpperCase());
			
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
		tb_filter2.setValue("");
		tb_filter3.setValue("");
		tb_filter4.setValue("");
		
		
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

public void caricaComboOrganizzazione() {
	try {
		tb_filter2.clear();
		tb_filter2.setDisabled(false);
		Hashtable<String, Object> h = new Hashtable();
		Hashtable h1 = new Hashtable(); // x le comboBox da caricare		
		h1.put("ORGMOD1", tb_filter2);
		
		CaribelComboRepository.comboPreLoadAll(new RsaOrganizzazioneEJB(), "query_combo", h, h1, new Hashtable(), "codice", "descrizione", false);

	}catch(Exception e){
		doShowException(e);
	}

}
public void caricaComboIstituti() {
	try {
		tb_filter1.clear();
		tb_filter1.setDisabled(false);
		Hashtable<String, Object> h = new Hashtable();
		Hashtable h1 = new Hashtable(); // x le comboBox da caricare	
		h1.put("TIPOIST1", tb_filter1);
		

		CaribelComboRepository.comboPreLoadAll(new RsaTipoIstitutoEJB(), "query_combo", h, h1, new Hashtable(), "codice", "descrizione", false);
	}catch(Exception e){
		e.printStackTrace();
		doShowException(e);
	}

}
}

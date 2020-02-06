package it.caribel.app.common.controllers.prestaz_bisogni;

import java.util.Hashtable;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.nuovi.PrestazBisogniEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Label;

public class PrestazBisogniGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = PrestazBisogniFormCtrl.myKeyPermission;
	private PrestazBisogniEJB myEJB = new PrestazBisogniEJB();
	private String myPathFormZul = "/web/ui/common/prestaz_bisogni/prestazBisogniForm.zul";
	
	private CaribelCombobox pai_figProf;
	private CaribelRadiogroup tb_filter2;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		caricaCombo();
		if(super.caribelSearchCtrl!=null){
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get("textToSearch");
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				pai_figProf.setText(textToSearch);
				super.hParameters.put(pai_figProf.getDb_name(), textToSearch);
				//tb_filter2.setText(textToSearch);
				//super.hParameters.put(tb_filter2.getDb_name(), textToSearch);
				doRefresh();
			}
		}
    }
	
	public void doStampa() {		
		
	}
	
	public void doCerca(){		
		try{
			//pai_figProf.getSelectedValue();
			super.hParameters.put(pai_figProf.getDb_name(), pai_figProf.getSelectedValue());
			super.hParameters.put(tb_filter2.getDb_name(), tb_filter2.getSelectedItem().getValue());
			doRefresh();
		}catch(Exception e){
			doShowException(e);
		}
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
		
		pai_figProf.setValue("");
		
		
	}

	private void caricaCombo() throws Exception {
	
	//Caricamento combo tipi operatori
		String opCaricati = ManagerOperatore.loadTipiOperatori(pai_figProf, Costanti.TAB_VAL_SO_TIPO_OPERATORE, true);
		logger.trace("Aggiungi prestazioni: operatori caricati\n"+opCaricati);
		pai_figProf.setSelectedValue(UtilForContainer.getTipoOperatorerContainer());
}

}

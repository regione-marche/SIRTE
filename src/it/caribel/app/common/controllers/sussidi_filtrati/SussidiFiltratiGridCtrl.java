package it.caribel.app.common.controllers.sussidi_filtrati;

import java.util.Hashtable;

import it.caribel.app.sinssnt.bean.ContribEJB;
import it.caribel.app.sinssnt.bean.SussidiEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.swing2.util.comboPreLoad;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

public class SussidiFiltratiGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	private Hashtable<String, Object> parkSetting;
	private String myKeyPermission = "";
	private SussidiEJB myEJB = new SussidiEJB();
	private String myPathFormZul = "/web/ui/common/sussidi_filtrati/sussidiFiltratiForm.zul";
	
	
	protected CaribelTextbox tb_filter1;
	protected CaribelIntbox tb_filter2;
	private CaribelRadiogroup tb_filter3;
	private CaribelTextbox tb_filter4;
	protected CaribelTextbox tb_filter5;
	protected CaribelTextbox tb_filter6;
	private CaribelTextbox tb_filter7;	
	private CaribelCombobox tb_filter8;
	private CaribelTextbox tb_filter9;
	private CaribelRadiogroup tb_filter10;
	protected Radio filtro_senza;
	protected Radio soc;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.setMethodNameForQueryPaginate("queryPaginateFiltri");

		super.doAfterCompose(comp);
		caricaComboCosto();
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
			
			super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getValue().toUpperCase());
			super.hParameters.put(tb_filter2.getDb_name(), tb_filter2.getText());
			super.hParameters.put(tb_filter3.getDb_name(), tb_filter3.getSelectedItem().getValue());
			super.hParameters.put(tb_filter4.getDb_name(), tb_filter4.getValue().toUpperCase());
			super.hParameters.put(tb_filter5.getDb_name(), tb_filter5.getValue().toUpperCase());			
			super.hParameters.put(tb_filter6.getDb_name(), tb_filter6.getValue().toUpperCase());	
			super.hParameters.put(tb_filter7.getDb_name(), tb_filter7.getValue().toUpperCase());	
			if(tb_filter8.getValue()!=null && !tb_filter8.getValue().equals(""))
			super.hParameters.put(tb_filter8.getDb_name(), tb_filter8.getSelectedItem().getValue());
			super.hParameters.put(tb_filter9.getDb_name(), tb_filter9.getValue().toUpperCase());
			super.hParameters.put(tb_filter10.getDb_name(), tb_filter10.getSelectedItem().getValue());
			
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
		tb_filter2.setValue(0);
		filtro_senza.setSelected(true);
		soc.setSelected(true);		
		tb_filter4.setValue("");
		tb_filter5.setValue("");
		tb_filter6.setValue("");
		tb_filter7.setValue("");	
		tb_filter8.setValue("");
		tb_filter9.setValue("");
		
		
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

public void caricaComboCosto() {
	try {
		tb_filter8.clear();
		tb_filter8.setDisabled(false);
		Hashtable<String, Object> h = new Hashtable();
		String strCodOper =profile.getParameter("codice_operatore");					
		//h.put("operatore",strCodOper);
		h.put("codice_regione","codreg");
		h.put("codice_usl","codazsan");
		
		CaribelComboRepository.comboPreLoad("combo_centroCosto"+strCodOper, new ContribEJB(), "query_loadCmbBoxCentriCosto",h, tb_filter8, null, "codice", "descrizione", false);
		
		
	}catch(Exception e){
		e.printStackTrace();
		doShowException(e);
	}

}

}

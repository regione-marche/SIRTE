package it.caribel.app.sinssnt.controllers.tabelle.elenco_casi_PUAC;

import java.util.Hashtable;

import it.caribel.app.sinssnt.bean.RsaTipoIstitutoSts11EJB;
import it.caribel.app.sinssnt.bean.SocElencocasiEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.profile2.profile;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import it.pisa.caribel.util.ISASUtil;

public class ElencoCasiPUACGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "A_PUACAS";
	private SocElencocasiEJB myEJB = new SocElencocasiEJB();
	private String myPathFormZul = "/web/ui/sinssnt/tabelle/elenco_casi_PUAC/elencoCasiPUACForm.zul";
	
	private boolean abilitazioneUvmd = false;
	private String CONSTANTS_CODICE_UVMD="D";
	 private String CONSTANTS_TESTO_UVMD="Uvmd";
	
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	protected CaribelCombobox tb_filter1;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.setMethodNameForQueryPaginate("queryPag_eleCasiPUAC");
		super.doAfterCompose(comp);
		String valAbilUvmd = profile.getParameter("abil_uvmd");
   	  	abilitazioneUvmd = (ISASUtil.valida(valAbilUvmd) && valAbilUvmd.equalsIgnoreCase("SI"));
		caricaComboTipo(tb_filter1);
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
				&& (adata.getValueForIsas()!=null && !adata.getValueForIsas().equals("") && (tb_filter1.getValue()!=null && !tb_filter1.getValue().equals(""))) 
				)
		{
		super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getSelectedItem().getValue());
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

public void caricaComboTipo(CaribelCombobox cbx) {
	cbx.clear();
	if (abilitazioneUvmd){
	CaribelComboRepository.addComboItem(cbx, "T", Labels.getLabel("elenco_casiPUAC.tipo1"));
	CaribelComboRepository.addComboItem(cbx, "C", Labels.getLabel("elenco_casiPUAC.tipo2"));
	CaribelComboRepository.addComboItem(cbx, "S", Labels.getLabel("elenco_casiPUAC.tipo3"));
	CaribelComboRepository.addComboItem(cbx, "R", Labels.getLabel("elenco_casiPUAC.tipo4"));
	CaribelComboRepository.addComboItem(cbx, CONSTANTS_CODICE_UVMD, CONSTANTS_TESTO_UVMD);
	}
	else{
		CaribelComboRepository.addComboItem(cbx, "T", Labels.getLabel("elenco_casiPUAC.tipo1"));
		CaribelComboRepository.addComboItem(cbx, "C", Labels.getLabel("elenco_casiPUAC.tipo2"));
		CaribelComboRepository.addComboItem(cbx, "S", Labels.getLabel("elenco_casiPUAC.tipo3"));
		CaribelComboRepository.addComboItem(cbx, "R", Labels.getLabel("elenco_casiPUAC.tipo4"));
	}
	cbx.setSelectedValue("T");
	

}
}

package it.caribel.app.sinssnt.controllers.report.common;

import it.caribel.app.common.ejb.AreaDisEJB;
import it.caribel.app.common.ejb.ComuniEJB;
import it.caribel.app.common.ejb.DistrettiEJB;
import it.caribel.app.common.ejb.PresidiEJB;
import it.caribel.app.common.ejb.ZoneEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.pisa.caribel.isas2.ISASUser;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;

public class PanelUbicazioneNesDivCtrl extends CaribelForwardComposer {
	private static final long serialVersionUID = 1L;
	
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected Radio presidio;
	protected Radio comune;
	protected Radio area_dis;
	protected CaribelRadiogroup res_dom;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup raggruppamento;
	protected Radio sociale;
	protected Radio sanitaria;
	protected Radio residenza;
	protected Radio domicilio;
	protected Label presidio_comune_areadis;
	protected Label lbl_ubicazione;
	private String lblPresidio = Labels.getLabel("PanelUbicazione.presidio")+":";
	private String lblComune = Labels.getLabel("PanelUbicazione.comune")+":";
	private String lblArea = Labels.getLabel("PanelUbicazione.area_dis")+":";
	protected Hlayout riga_raggruppamento;
	protected Hlayout riga_ubicazione;
	private String cod_oper =  getProfile().getStringFromProfile("codice_operatore");
	private String tipo_oper =  getProfile().getStringFromProfile("tipo_operatore");
	
	
	CaribelSessionManager profile = CaribelSessionManager.getInstance();
	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
	
	private boolean conVoceTutti = true;
	
	public void doInitPanel(){
		try {
			
			// Workaround per evitare problemi di visualizzazione Disabled anzichè Enabled
			zona.setDisabled(false);
			res_dom.setDisabled(true);
			soc_san.setDisabled(true);			
			
			presidio_comune_areadis.setValue(this.lblPresidio);
			
			initCombo();
		}catch(Exception e){
			doShowException(e);
		}
	}	
		

	
	public void onSelect$zona(Event event){
		try{
			if (!zona.getSelectedItem().getValue().equals("TUTTO")){
				distretto.clear();
				distretto.setDisabled(false);
				Hashtable<String, Object> h = new Hashtable();	
				
				String strZONA =zona.getSelectedValue();
				h.put("rec_vuoto", "S");				
				h.put("cod_zona",strZONA);
				CaribelComboRepository.comboPreLoad("combo_distr"+strZONA, new DistrettiEJB(), "queryCombo_NesDiv",h, distretto, null, "cod_distr", "des_distr", false);
				if(distretto.getItemCount()>0){				
					distretto.setSelectedValue("TUTTI");
					distretto.getSelectedItem().setValue("");
				}	
			}
		
			if (distretto.getSelectedItem().getValue().equals("TUTTI") || distretto.getSelectedItem().getValue().equals("")){				
				presidio_comune_area.clear();
				presidio_comune_area.setSelectedValue("TUTTI");
				CaribelComboRepository.addComboItem(presidio_comune_area, "", Labels.getLabel("ReportElencoStr.tutti"));
				CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
			
			}
			else{
				Hashtable<String, Object> h = new Hashtable();
				String strDISTR =distretto.getSelectedValue();							
				h.put("cod_distr",strDISTR);
				if (presidio.isChecked())
					CaribelComboRepository.comboPreLoad("combo_presidio"+strDISTR, new PresidiEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codpres", "despres", false);
				if (comune.isChecked())
					CaribelComboRepository.comboPreLoad("combo_comune"+strDISTR, new ComuniEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codice", "descrizione", false);
				if (area_dis.isChecked())
					CaribelComboRepository.comboPreLoad("combo_area_dis"+strDISTR, new AreaDisEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codice", "descrizione", false);
			}
			
			if (zona.getSelectedItem().getValue().equals("NESDIV")){
				
				distretto.clear();
				presidio_comune_area.clear();
				CaribelComboRepository.addComboItem(distretto, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				presidio_comune_area.setSelectedValue("NESSUNA DIVISIONE");
				distretto.setSelectedValue("NESSUNA DIVISIONE");
				distretto.setDisabled(true);
				presidio_comune_area.setDisabled(true);
			}
			if (zona.getSelectedItem().getValue().equals("TUTTO")){
				distretto.clear();
				presidio_comune_area.clear();				
				CaribelComboRepository.addComboItem(distretto, "", Labels.getLabel("ReportElencoStr.tutti"));
				CaribelComboRepository.addComboItem(distretto, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				CaribelComboRepository.addComboItem(presidio_comune_area, "", Labels.getLabel("ReportElencoStr.tutti"));
				CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				presidio_comune_area.setSelectedValue("TUTTI");
				distretto.setSelectedValue("TUTTI");
				distretto.setDisabled(false);
				presidio_comune_area.setDisabled(false);
			}
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void onSelect$distretto(Event event)
	{
		try{
			if (!distretto.getSelectedItem().getValue().equals("TUTTO")){				
				presidio_comune_area.clear();
				presidio_comune_area.setDisabled(false);
				Hashtable<String, Object> h = new Hashtable();	
				
				String strDISTR =distretto.getSelectedValue();							
				h.put("cod_distr",strDISTR);
				if (presidio.isChecked())
					CaribelComboRepository.comboPreLoad("combo_presidio"+strDISTR, new PresidiEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codpres", "despres", false);
				if (comune.isChecked())
					CaribelComboRepository.comboPreLoad("combo_comune"+strDISTR, new ComuniEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codice", "descrizione", false);
				if (area_dis.isChecked())
					CaribelComboRepository.comboPreLoad("combo_area_dis"+strDISTR, new AreaDisEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codice", "descrizione", false);
				
				if(presidio_comune_area.getItemCount()>0){				
					presidio_comune_area.setSelectedValue("TUTTO");
					presidio_comune_area.getSelectedItem().setValue("");
				}	
			}
			
			if (distretto.getSelectedItem().getValue().equals("NODIV") || distretto.getSelectedItem().getValue().equals("NESDIV")){				
				presidio_comune_area.clear();				
				CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				presidio_comune_area.setSelectedValue("NESSUNA DIVISIONE");				
				presidio_comune_area.setDisabled(true);
			}
			
		}catch(Exception e){
			doShowException(e);
		}
	}

	public void initCombo() {
		try {
			zona.clear();
			zona.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			
			String metodo = "query" + (conVoceTutti ? "Combo" : "_combo");
			String keyCombo = "combo_zona"+ cod_oper + (conVoceTutti ? "" : "NoTutti");
//			forzo a ricaricare le zone sempre, in quanto c'è isas_gid sulle zone.
			CaribelComboRepository.comboPreLoad("", new ZoneEJB(), "queryCombo_NesDiv", h, zona, null, "codice_zona", "descrizione_zona", false);
			if(zona.getItemCount()>0){
				zona.setSelectedIndex(0);
				presidio_comune_area.setSelectedValue("TUTTI");
				distretto.setSelectedValue("TUTTI");
			}
			
			
			String strZONA =zona.getSelectedValue();
				
			h.put("cod_zona",strZONA);
			h.put("rec_vuoto", "S");
			
			distretto.setSelectedValue("TUTTI");
			if (zona.getSelectedItem().getValue().equals("TUTTO")){
				CaribelComboRepository.addComboItem(distretto, "", Labels.getLabel("ReportElencoStr.tutti"));
				CaribelComboRepository.addComboItem(distretto, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				CaribelComboRepository.addComboItem(presidio_comune_area, "", Labels.getLabel("ReportElencoStr.tutti"));
				CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));
				//distretto.setSelectedValue("TUTTI");
			
			}
			
				
		}catch(Exception e){
			doShowException(e);
		}
	
	}
	
	public void onCheck$raggruppamento() throws Exception {
		Hashtable<String, Object> h = new Hashtable();	
		String strDISTR =distretto.getSelectedValue();							
		h.put("cod_distr",strDISTR);
		if(!presidio.isChecked() && !comune.isChecked())
			presidio_comune_areadis.setValue(this.lblArea);
		
		else if(!presidio.isChecked() && !area_dis.isChecked())
			presidio_comune_areadis.setValue(this.lblComune);
		
		else 
			presidio_comune_areadis.setValue(this.lblPresidio);

		
		
		if(comune.isChecked()){
			if (!iu.canIUse("U_SOCSAN")) {	
				soc_san.setSelectedValue(tipo_oper);
				soc_san.setDisabled(true);
				sociale.setDisabled(true);
				sanitaria.setDisabled(true);
				res_dom.setDisabled(false);
			} else {						
				soc_san.setDisabled(false);
				res_dom.setDisabled(false);
				sociale.setDisabled(false);
				sanitaria.setDisabled(false);
				
			}
			
			CaribelComboRepository.comboPreLoad("combo_comune"+strDISTR, new ComuniEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codice", "descrizione", false);
			presidio_comune_area.setSelectedValue("TUTTI");
		}
		else if (presidio.isChecked()){
			res_dom.setDisabled(true);
			soc_san.setDisabled(true);
		    domicilio.setChecked(true);
		    sanitaria.setChecked(true);
			CaribelComboRepository.comboPreLoad("combo_presidio"+strDISTR, new PresidiEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codpres", "despres", false);
			presidio_comune_area.setSelectedValue("TUTTI");
		}
		else if (area_dis.isChecked()){
			if (!iu.canIUse("U_SOCSAN")) {						
				soc_san.setSelectedValue(tipo_oper);
				soc_san.setDisabled(true);
				sociale.setDisabled(true);
				sanitaria.setDisabled(true);
				res_dom.setDisabled(false);
			} else {						
				soc_san.setDisabled(false);
				res_dom.setDisabled(false);
				sociale.setDisabled(false);
				sanitaria.setDisabled(false);
				
			}
			
			CaribelComboRepository.comboPreLoad("combo_area_dis"+strDISTR, new AreaDisEJB(), "queryComboxDistretti_NesDiv",h, presidio_comune_area, null, "codice", "descrizione", false);
			presidio_comune_area.setSelectedValue("TUTTI");
		}
		
		if (zona.getSelectedItem().getValue().equals("TUTTO")){			
			
			CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));			
		
		}
		
		if (distretto.getSelectedItem().getValue().equals("TUTTI") || distretto.getSelectedItem().getValue().equals("") ){			
			presidio_comune_area.clear();
			presidio_comune_area.setSelectedValue("TUTTI");
			CaribelComboRepository.addComboItem(presidio_comune_area, "", Labels.getLabel("ReportElencoStr.tutti"));
			CaribelComboRepository.addComboItem(presidio_comune_area, "NODIV", Labels.getLabel("ReportElencoStr.nodiv"));			
		
		}
		
		
	}
	
	
	public void setLabelPresidio(String label){
		this.lblPresidio = label;
		presidio_comune_areadis.setValue(this.lblPresidio); 
	}
	
	public void setLabelArea(String label){
		this.lblArea = label;
		presidio_comune_areadis.setValue(this.lblArea); 
	}
	
	public void setLabelComune(String label){
		this.lblComune = label;
		presidio_comune_areadis.setValue(this.lblComune); 
	}
	
	public void setVisibleRaggruppamento(boolean visible){
		riga_raggruppamento.setVisible(visible);
	}
	
	public void settaRaggruppamento(String valori) {
		String raggr = raggruppamento.getSelectedItem().getValue();
		raggr = valori;

		if (raggr.indexOf("P") == -1){
			presidio.setVisible(false);
			setLabelComune(lblComune);
			comune.setChecked(true);
			res_dom.setDisabled(false);
			soc_san.setDisabled(false);	
		}

		if (raggr.indexOf("C") == -1){
			comune.setVisible(false);			
		}
		if (raggr.indexOf("A") == -1){
			area_dis.setVisible(false);
		}
		
		}
		
		public void settaRaggruppamentoNoUbic(String valori) {
			String raggr = raggruppamento.getSelectedItem().getValue();
			raggr = valori;

			if (raggr.indexOf("P") == -1){
				presidio.setVisible(false);
				setLabelComune(lblComune);
				comune.setChecked(true);
				res_dom.setDisabled(false);
				soc_san.setDisabled(false);	
			}

			if (raggr.indexOf("C") == -1){
				comune.setVisible(false);			
			}
			if (raggr.indexOf("A") == -1){
				area_dis.setVisible(false);
			}
			if (raggr.indexOf("N") == -1){
				riga_ubicazione.setVisible(false);
				res_dom.setVisible(false);
				soc_san.setVisible(false);
				lbl_ubicazione.setVisible(false);
			}
		

	}
	
	

}
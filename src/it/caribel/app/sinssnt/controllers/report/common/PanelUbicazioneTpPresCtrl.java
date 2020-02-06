package it.caribel.app.sinssnt.controllers.report.common;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.pisa.caribel.isas2.ISASUser;

public class PanelUbicazioneTpPresCtrl extends CaribelForwardComposer {
	private static final long serialVersionUID = 1L;
	protected CaribelRadiogroup tp_presidio;
	protected Radio contatto;
	protected Radio accesso;
	
	
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
	
	private String lblPresidioContatto = Labels.getLabel("PanelUbicazioneTpPres.presidio_contatto")+":";
	private String lblPresidioAccesso = Labels.getLabel("PanelUbicazioneTpPres.presidio_accesso")+":";
	
	protected Hlayout riga_raggruppamento;
	protected Hlayout riga_ubicazione;
	
	
	CaribelSessionManager profile = CaribelSessionManager.getInstance();
	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
	

	public void doInitPanel(){
		try {
			
			Component p1 = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c1 = (PanelUbicazioneCtrl)p1.getAttribute(MY_CTRL_KEY);
			c1.doInitPanel();
			
		}catch(Exception e){
			doShowException(e);
		}
	}	
		

	
	public void onCheck$tp_presidio(Event event){
		try{
			if (contatto.isChecked())
				presidio_comune_areadis.setValue(this.lblPresidioContatto);
			else if(accesso.isChecked() )
				presidio_comune_areadis.setValue(this.lblPresidioAccesso);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	public void setLabelPresidioContatto(String label){
		this.lblPresidioContatto = label;
		presidio_comune_areadis.setValue(this.lblPresidioContatto); 
	}
	
	public void setLabelPresidioAccesso(String label){
		this.lblPresidioAccesso = label;
		presidio_comune_areadis.setValue(this.lblPresidioAccesso); 
	}
	
	public void settaRaggruppamentoNoUbic(String valori) {
		String raggr = raggruppamento.getSelectedItem().getValue();
		raggr = valori;

		if (raggr.indexOf("P") == -1){
			presidio.setVisible(false);			
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
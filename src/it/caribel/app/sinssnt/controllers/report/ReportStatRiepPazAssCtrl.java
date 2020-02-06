package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ReportStatRiepPazAssCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	
	
	protected Radio pdf;
	protected Radio html;
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup res_dom;
	
	protected Radio presidio;
	protected Radio comune;
	protected Radio area_dis;
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	
	
	protected CaribelRadiogroup tp_conteggio;
	
	protected Radio prog_ap;
	protected Radio acc;
	
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String data_inizio=dadata.getValueForIsas();
		    String data_fine=adata.getValueForIsas();
		    String tipo = tp_conteggio.getSelectedItem().getValue();

		    String ragg=raggruppamento.getSelectedItem().getValue();
		    String zone=zona.getSelectedItem().getValue();
		    String distr="";
		    String pca="";
		    if (zone.equals("TUTTO"))
				zone="";
			if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals("") || distretto.getValue().equals("NODIV") || distretto.getValue().equals("NESDIV"))
				distr="";
			else 
				distr = distretto.getSelectedItem().getValue();			
			
			if(presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals("") || presidio_comune_area.getValue().equals("NODIV") || presidio_comune_area.getValue().equals("NESDIV"))
	              pca="";
			else  
				pca=presidio_comune_area.getSelectedItem().getValue();
			
		    String socsan = soc_san.getSelectedItem().getValue();
		    String tipo_ubi=res_dom.getSelectedItem().getValue();
		    
		    String fStampa = formatoStampa.getSelectedItem().getValue();
			String report = "ass_mensile";
			String type = "";

			if (fStampa.equals("P")) {				
				report += ".fo";
				type = "PDF";
			} else{				
				report += ".html";
				type = "application/vnd.ms-excel";
			}

		    String servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
		    		"?EJB=SINS_FOASSMENSILE"+
		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
		    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
		    		"&METHOD=query_assmensile&data_inizio="+data_inizio+"&dom_res="+tipo_ubi+"&socsan="+socsan+
		            "&data_fine="+data_fine+"&tipo="+tipo+
		            "&ragg="+ragg+"&pca="+pca+"&distretto="+distr+"&zona="+zone
		            +"&TYPE=" + type + "&REPORT=" + report ;
		    
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
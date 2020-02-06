package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ReportInfOspedaleDimCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	
	protected Radio html;
	protected Radio pdf;
	
	
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
	
	protected CaribelTextbox osp_dim;
	protected CaribelTextbox rep_dim;
	
	private String tipoOpe="";
	
	public void doInitForm() {
		try {
			String tipoOpe = (String)arg.get("tipoOpe");
			if(tipoOpe!=null){
				this.tipoOpe=tipoOpe;
			}
			
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			dadata.setFocus(true);
			initCombo();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			String formato = "";
							
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			
			String dom_res = res_dom.getSelectedItem().getValue();
			String socsan = soc_san.getSelectedItem().getValue();
			String ospdim=osp_dim.getValue();
            String repdim=rep_dim.getValue();			
			
			
			if (distretto.getValue().equals("TUTTO") || distretto.getValue().equals(""))
				distr="";
			else 
				distr = distretto.getSelectedItem().getValue();			
			
			if(presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals(""))
	              pca="";
			else  
				pca=presidio_comune_area.getSelectedItem().getValue();
			
			if (zone.equals("TUTTO"))
				zone="";
			if (distr.equals("TUTTO"))
				distr="";
			 String tp = formatoStampa.getSelectedItem().getValue();
	         String report = "ele_ospdim.fo";
	         String type = "PDF";
	            if (tp.equals("2"))
	            {
	                report = "ele_ospdim.html";
	                type = "application/vnd.ms-excel";
	            }

	            String u="/SINSSNTFoServlet/SINSSNTFoServlet"+
						"?EJB=SINS_FORIEPOSPDIM"+
						"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
						"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
	            		"&METHOD=query_ospdim"+
	            		"&data_inizio="+(dadata.getValueForIsas())+
						"&data_fine="+(adata.getValueForIsas())+
	                    "&tipoope="+this.tipoOpe+
	                    "&dom_res="+dom_res+"&socsan="+socsan+
	                    "&ragg="+raggr+"&zona="+zone+"&distretto="+distr+"&pca="+pca+ 
	                    "&reparto="+repdim+"&ospedale="+ospdim+"&TYPE=" + type + "&REPORT=" + report;

			String metodo="";
			
				
           
            String TYPE="";
			if (pdf.isSelected()) {
				TYPE = "PDF";
				
					report="ele_infassan.fo";
			}
				else 
					report="ele_infassint.fo";
			
			if (html.isSelected()) {	
				TYPE="application/vnd.ms-excel";
				
					report="ele_infassan.html";
			}
				else 
					report="ele_infassint.html";
			
			
			
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo() {
		try {
			
		}catch(Exception e){
			doShowException(e);
		}
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
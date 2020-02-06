package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ReportInfMMGContattiApCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup divMed;
	//protected CaribelRadiogroup tipoAss;
	protected CaribelRadiogroup modalitaStampa;
	protected Radio html;
	protected Radio pdf;
	protected Radio an;
	protected Radio sin;
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup res_dom;
	
	protected Radio presidio;
	protected Radio comune;
	protected Radio area_dis;
	
	protected CaribelCombobox motivo;
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	protected CaribelTextbox cod_med;
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			
			initCombo();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			String formato = "";
			String report="";			
			String type = formatoStampa.getSelectedItem().getValue();
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			String divisione_med = divMed.getSelectedItem().getValue();
			//String tipo_ass = tipoAss.getSelectedItem().getValue();
			String dom_res = res_dom.getSelectedItem().getValue();
			String socsan = soc_san.getSelectedItem().getValue();
			String mot ="";			
			if (motivo.getValue()!=null && !motivo.getValue().equals(""))
				mot=motivo.getSelectedItem().getValue();
			else mot="-1";
			
			if (divisione_med.equals("S"))
				divisione_med="SI";
			if (divisione_med.equals("N"))
				divisione_med="NO";
			
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
			
			String metodo="";
			if (sin.isSelected())
				metodo="query_elencoSint";
			else 
				metodo="query_elencoAss";
           
			String TYPE="";
			if (pdf.isSelected()) {
				TYPE = "PDF";
				if (an.isSelected())
					report="inf_mass.fo";
				else if (sin.isSelected())
					report="inf_msint.fo";
			}
			if (html.isSelected()) {
				TYPE="application/vnd.ms-excel";
				if (an.isSelected())
					report="inf_mass.html";
				else if (sin.isSelected())
					report="inf_msint.html";
			}
			
			String u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOMASSELE"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
					"&METHOD="+metodo+
					"&codice_inizio="+(cod_med.getValue())+					
					"&data_inizio="+(dadata.getValueForIsas())+
					"&data_fine="+(adata.getValueForIsas())+
					"&pca="+pca+	
					"&distretto="+distr +
					"&ragg="+raggr+	
					"&dom_res="+dom_res+	
					"&socsan="+socsan+						
					"&formato="+formato+					
					//"&ass="+tipo_ass +
					"&zona="+zone+			
					"&REPORT="+report+
					"&TYPE="+TYPE+
					"&op="+divisione_med+
					"&motivo="+mot;
					
					
					
			
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo() {
		try {
			QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
			quadroSanitarioMMGCtrl.caricaTipoCura(motivo);
		}catch(Exception e){
			doShowException(e);
		}
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
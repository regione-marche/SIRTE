package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ReportAltroContattiApCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup divOp;
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
	
	
	
	public void doInitForm() {
		try {
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
			
			String report="";						
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			String divisione_op = divOp.getSelectedItem().getValue();
			String dom_res = res_dom.getSelectedItem().getValue();
			String socsan = soc_san.getSelectedItem().getValue();
			String mot ="";			
			if (motivo.getValue()!=null && !motivo.getValue().equals(""))
				mot=motivo.getSelectedItem().getValue();
			else mot="-1";
			
			if (divisione_op.equals("S"))
				divisione_op="SI";
			else if (divisione_op.equals("N"))
				divisione_op="NO";
			
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
				metodo="query_altrosint";
			else 
				metodo="query_altro";
           
            String TYPE="";
			if (pdf.isSelected()) {
				TYPE = "PDF";
				if (an.isSelected())
					report="ele_altroassan.fo";
				else if (sin.isSelected())
					report="ele_altroassint.fo";
			}
			if (html.isSelected()) {	
				TYPE="application/vnd.ms-excel";
				if (an.isSelected())
					report="ele_altroassan.html";
				else if (sin.isSelected())
					report="ele_altroassint.html";
			}
			;
			String u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOALTROELEASS"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
					"&METHOD="+metodo+
					"&data_inizio="+(dadata.getValueForIsas())+
					"&data_fine="+(adata.getValueForIsas())+
					"&pca="+pca+	
					"&distretto="+distr +
					"&ragg="+raggr+	
					"&dom_res="+dom_res+	
					"&socsan="+socsan+	
					"&zona="+zone+			
					"&REPORT="+report+
					"&TYPE="+TYPE+
					"&oper="+divisione_op+
					"&motivo="+mot;
					
					
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, u);
			
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
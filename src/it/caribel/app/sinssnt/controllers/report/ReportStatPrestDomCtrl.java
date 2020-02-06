package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ReportStatPrestDomCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup ass_adi;	
	
	protected Radio html;
	protected Radio pdf;
	
	protected Radio ass_adi_si;
	protected Radio ass_adi_no;
	
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
		    String data_fine=this.adata.getValueForIsas();

		    String tipo = ass_adi.getSelectedItem().getValue();
		    if (tipo.equals("S")) tipo = "ADI";
		    else tipo = "NONADI";

		
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

		    String formato = formatoStampa.getSelectedItem().getValue();
		    String report = "prestdom.fo";
		            String TYPE = "PDF";
		            if (formato.equals("2"))
		            {

		            report = "prestdom.html";
		            TYPE="application/vnd.ms-excel";
		            }

		   
		    String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOPRESTDOM"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
					"&METHOD=query_elencoPrest&data_inizio="+data_inizio+
		            "&data_fine="+data_fine+"&REPORT="+report+ "&TYPE=" + TYPE+"&tipo="+tipo +"&dom_res="+tipo_ubi+"&socsan="+socsan+
		            "&ragg="+ragg+"&pca="+pca+"&distretto="+distr+"&zona="+zone;
		
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
		   
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo (CaribelCombobox cbx)throws Exception {
		
	}
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
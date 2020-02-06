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

public class ReportCurePalAssistitiCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup divMedPal;
	
	protected CaribelRadiogroup modalitaStampa;
	
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
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	protected CaribelTextbox codope;
	protected CaribelTextbox codope1;
	
	protected Radio si;
	protected Radio no;
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggruppamento("CA");
			codope.setFocus(true);
			initCombo();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			 	
			String codice_inizio=codope.getValue();
			String codice_fine=codope1.getValue();	          
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr="";
			String pca="";
			if (zone.equals("TUTTO"))
				zone="";
			if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals(""))
				distr="";
			else 
				distr = distretto.getSelectedItem().getValue();			
			
			if(presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals(""))
	              pca="";
			else  
				pca=presidio_comune_area.getSelectedItem().getValue();
			
	        
	        String data_inizio= dadata.getValueForIsas();
	        String data_fine=adata.getValueForIsas();
	       
			String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi=res_dom.getSelectedItem().getValue();
	        //invocazione alla servlet
	        String servlet="";

	        if (an.isSelected())
	            servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
						"?EJB=SINS_FOMCPELEASS"+
						"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
						"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+	           
						"&METHOD=query_inf&codice_inizio="+codice_inizio+"&codice_fine="+codice_fine+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                    "&pca="+pca+"&distretto="+distr+"&ragg="+raggr+"&dom_res="+tipo_ubi+"&socsan="+socsan+	                    
						"&zona="+zone+"&REPORT=ele_mcpassan.fo";
	            else
	                servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
							"?EJB=SINS_FOMCPELEASS"+
							"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
							"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+	 
							"&METHOD=query_infsint&codice_inizio="+codice_inizio+"&codice_fine="+codice_fine+
							"&data_inizio="+data_inizio+"&data_fine="+data_fine+
							"&pca="+pca+"&distretto="+distr+"&ragg="+raggr+"&dom_res="+tipo_ubi+"&socsan="+socsan+	                    
							"&zona="+zone+"&REPORT=ele_mcpasssint.fo";
	            
	            if (si.isSelected())
	               servlet=servlet+"&oper=SI";
	            else servlet=servlet+"&oper=NO";
	            
	            
			
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
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
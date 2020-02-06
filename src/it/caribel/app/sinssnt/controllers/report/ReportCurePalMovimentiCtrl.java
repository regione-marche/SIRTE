package it.caribel.app.sinssnt.controllers.report;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;

import it.caribel.app.sinssnt.bean.MotivoSEJB;
import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneNesDivCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.composite_components.CaribelTextbox;

public class ReportCurePalMovimentiCtrl extends CaribelFormCtrl {
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
	
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c = (PanelUbicazioneNesDivCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggruppamento("CA");
			initCombo();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String ragg = raggruppamento.getSelectedItem().getValue();
	        String zone = zona.getSelectedItem().getValue();
	        String distr = "";
	        String pca = "";
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
			
	        String terr="";
	        
	        if (zone.equals("NESDIV")) {
	          terr="0|";
	          zone="";
	        } else
	          terr="1|";
	        if (distr.equals("NODIV") || distr.equals("NESDIV")) {
	          terr=terr+"0|";
	          distr="";
	        } else
	          terr=terr+"1|";
	        if (pca.equals("NODIV") || pca.equals("NESDIV")) {
	          terr=terr+"0";
	          pca="";
	        } else
	          terr=terr+"1";
	        String data_inizio= dadata.getValueForIsas();
	        String data_fine=adata.getValueForIsas();
	        String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi=res_dom.getSelectedItem().getValue();
			String formato="1";
			String TYPE="PDF";
			
			if (html.isSelected()) {
	        TYPE="application/vnd.ms-excel";
	        formato="2";
	      }
	        //invocazione alla servlet
	        String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FORIEPMOVMEDPAL"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
	                "&METHOD=query_riepmov"+
	                "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                "&zona="+zone+"&distretto="+distr+"&pca="+pca+
	                "&ragg="+ragg+"&terr="+terr+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                "&formato="+formato+"&TYPE="+TYPE+"&REPORT=x.fo" 
	                ;
	        
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
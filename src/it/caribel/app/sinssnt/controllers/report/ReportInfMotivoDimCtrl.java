package it.caribel.app.sinssnt.controllers.report;

import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

public class ReportInfMotivoDimCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup tipoAss;
	
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
	

	protected CaribelCombobox motivo_dimissione;
	
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	
	
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
			
			String report="";						
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			
			String tipo_ass = tipoAss.getSelectedItem().getValue();
			String dom_res = res_dom.getSelectedItem().getValue();
			String socsan = soc_san.getSelectedItem().getValue();
			String mot="";
			if (motivo_dimissione.getValue()!=null && !motivo_dimissione.getValue().equals(""))
				mot=motivo_dimissione.getSelectedItem().getValue();
			else mot="";
			
			String terr="";
			 if(zone.equals("NESDIV"))
	          {
	                  terr="0|";
	                  zone="";
	          }
	          else
	                  terr="1|";
	          if(distr.equals("NESDIV"))
	          {
	                  terr=terr+"0|";
	                  distr="";
	          }
	          else
	                  terr=terr+"1|";
	          if(pca.equals("NESDIV"))
	          {
	                  terr=terr+"0|";
	                  pca="";
	          }
	          else
	                  terr=terr+"1|";
	          if(mot.equals("NESDIV"))
	          {
	                  terr=terr+"0";
	                  mot="";
	          }
	          else
	                  terr=terr+"1";

			
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
			String metodo="query_inf";
			String TYPE="";
            
			if (pdf.isSelected()) {						
					report="ele_infmotivo.fo";				
					TYPE = "PDF";
			}
			if (html.isSelected()) {				
					report="ele_infmotivo.html";
					TYPE = "application/vnd.ms-excel";
			}
			
			String u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOINFELEMOT"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
					"&METHOD="+metodo+	
					"&ragg="+raggr+	
					"&pca="+pca+	
					"&distretto="+distr +
					"&zona="+zone+	
					"&dom_res="+dom_res+
					"&socsan="+socsan+	
					"&terr=" + terr +
					"&data_inizio="+(dadata.getValueForIsas())+
					"&data_fine="+(adata.getValueForIsas())+
					"&ass="+tipo_ass +
					"&motivo="+mot+
					"&REPORT="+report+
					"&TYPE="+TYPE;
			
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo() {
		try {
			motivo_dimissione.clear();
			motivo_dimissione.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			h.put("tab_cod","ICHIUS");
			CaribelComboRepository.comboPreLoad("combo_motivo_dim", new TabVociEJB(), "query",h, motivo_dimissione, null, "tab_val", "tab_descrizione", false);
			
			//if(motivo_dimissione.getItemCount()>0){
				//motivo_dimissione.setSelectedIndex(0);
			//}
		}catch(Exception e){
			e.printStackTrace();
			doShowException(e);
		}
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
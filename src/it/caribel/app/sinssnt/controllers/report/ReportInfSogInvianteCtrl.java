package it.caribel.app.sinssnt.controllers.report;

import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;


import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

public class ReportInfSogInvianteCtrl extends CaribelFormCtrl {
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
	

	protected CaribelCombobox soggetto_inviante;
	
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
			
			String inviato="";
			if (soggetto_inviante.getValue()!=null && !soggetto_inviante.getValue().equals(""))
				inviato=soggetto_inviante.getSelectedItem().getValue();
			else inviato="";
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
	          if(inviato.equals("NESDIV"))
	          {
	                  terr=terr+"0";
	                  inviato="";
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
					report="ele_infinviante.fo";				
					TYPE = "PDF";
			}
			if (html.isSelected()) {				
					report="ele_infinviante.html";
					TYPE = "application/vnd.ms-excel";
			}
			
			String u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOINFELEINV"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
					"&METHOD="+metodo+	
					"&ragg="+raggr+	
					"&pca="+pca+	
					"&distretto="+distr +
					"&zona="+zone+	
					"&terr=" + terr +
					"&dom_res="+dom_res+
					"&socsan="+socsan+						
					"&data_inizio="+(dadata.getValueForIsas())+
					"&data_fine="+(adata.getValueForIsas())+
					"&ass="+tipo_ass +
					"&inviato="+inviato+
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
			/*soggetto_inviante.clear();
			soggetto_inviante.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			
			Hashtable h_xCBdaTabBase = new Hashtable(); // x le comboBox da caricare
//			
			h_xCBdaTabBase.put("SEGNALA", soggetto_inviante);
			
			CaribelComboRepository.comboPreLoadAll(new SkInfEJB(), "query_Allcombo", h, h_xCBdaTabBase, new Hashtable(), "codice", "descrizione", false);*/
			Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
			Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
			Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
			
			h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SEGNALANTE, soggetto_inviante);
			CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
					"tab_descrizione", false);
			
		}catch(Exception e){
			doShowException(e);
		}
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
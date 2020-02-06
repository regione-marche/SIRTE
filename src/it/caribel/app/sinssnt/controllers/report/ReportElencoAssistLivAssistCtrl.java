package it.caribel.app.sinssnt.controllers.report;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;

import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneNesDivCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.util.resource.Labels;

public class ReportElencoAssistLivAssistCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	CaribelSessionManager profile = CaribelSessionManager.getInstance();
	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup modalitaStampa;
	
	protected Radio pdf;
	protected Radio html;
	protected Radio sin;
	protected Radio an;
	
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
	protected CaribelCombobox liv_ass;
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c = (PanelUbicazioneNesDivCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();			
			initCombo(liv_ass);
			presidio.setSelected(true);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String d1 = dadata.getValueForIsas();				
			String d2 = adata.getValueForIsas();		
			
			String modStampa = modalitaStampa.getSelectedItem().getValue();	

			String formato = formatoStampa.getSelectedItem().getValue();	
			String tpFormato = (formato.equals("1") ? "PDF" : "application/vnd.ms-excel");
			String report = "elePianiAssistLivAss";
			
			if (modStampa.equals("0")) {
				report += "Sint";
			}
			report += (formato.equals("1") ? ".fo" : ".html");
			
			String liv_assist = liv_ass.getSelectedItem().getValue();

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
	        
	        String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi=res_dom.getSelectedItem().getValue();

			String servlet ="";
                      
             if (profile.getStringFromProfile("ELEASSLIV")!=null && profile.getStringFromProfile("ELEASSLIV").equals("S")){
                    servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
        		    		"?EJB=SINS_FOELENCOLIVASS"+
        		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
        		    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
                    		"&METHOD=query_report"+
                    		"&d1=" + d1 + "&d2=" + d2 + "&liv_ass=" + liv_assist
                    		+ "&zona=" + zone + "&distretto=" + distr + "&dom_res=" + tipo_ubi + "&socsan=" + socsan + "&pca="
                    		+ pca + "&ragg=" + ragg + "&terr=" + terr + "&TYPE=" + tpFormato + "&REPORT=" + report;
			
                        }
               else{
                    servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
        		    		"?EJB=SINS_FOELEASSTLIVCOMP"+
        		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
        		    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
                    		"&METHOD=query_report"
                    		+ "&d1=" + d1 + "&d2=" + d2 + "&liv_ass=" + liv_assist
                    		+ "&zona=" + zone + "&distretto=" + distr + "&dom_res=" + tipo_ubi + "&socsan=" + socsan + "&pca="
                    		+ pca + "&ragg=" + ragg + "&terr=" + terr + "&TYPE=" + tpFormato + "&REPORT=" + report;
               }
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo(CaribelCombobox cbx) {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "0", Labels.getLabel("generic.livello_ass1"));
		CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("generic.livello_ass2"));
		CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("generic.livello_ass3"));
		CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("generic.livello_ass4"));
		
		cbx.setSelectedValue("0");
	
	}
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
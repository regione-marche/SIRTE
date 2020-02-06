package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import java.text.SimpleDateFormat;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

public class ReportCurePalGiorniAssCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	
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
	String tabella="";
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggruppamento("CA");
			initCombo();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			 	
				          
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
			String formato = formatoStampa.getSelectedItem().getValue();
            String TYPE=(formato.equals("PDF")?"PDF":"application/vnd.ms-excel");
            String metodo = "query_report";
            String report = (formato.equals("PDF")?"conta_giorni.fo":"conta_giorni.html");
	        //invocazione alla servlet
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dt1=sdf.format(dadata.getValue());
            String dt2=sdf.format(adata.getValue());
            it.pisa.caribel.util.NumberDateFormat ndf = new
                                it.pisa.caribel.util.NumberDateFormat();
            int periodovalido = ndf.IsLessThanOneYear(dt1,dt2);
            if(periodovalido==1){
            	Messagebox.show(
            			Labels.getLabel("exception.LunghezzaPeriodoException.msg"),
        				Labels.getLabel("messagebox.attention"),
        				Messagebox.OK,
        				Messagebox.INFORMATION);
             
            }else {
	        String servlet= "/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOCONTAGIORNI"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+      
                    "&METHOD="+metodo+"&d1="+data_inizio+"&d2="+data_fine+
                    "&TYPE="+TYPE+
                    "&tabella="+tabella+
                    "&dom_res="+tipo_ubi+"&socsan="+socsan+
                    "&ragg="+raggr+"&pca="+pca+"&distretto="+distr+"&zona="+zone+
                    "&REPORT="+report;
	        		
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
            }
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
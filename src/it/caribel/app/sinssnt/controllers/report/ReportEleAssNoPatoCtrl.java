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

public class ReportEleAssNoPatoCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	
	
	protected Radio pdf;
	protected Radio html;
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggr("A");
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			
			String d1=dadata.getValueForIsas();               
            String d2 = adata.getValueForIsas();   
           

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
 			
            String form=formatoStampa.getSelectedItem().getValue();
            String report=(form.equals("PDF")?"ass_pato.fo":"ass_pato.html");
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
            String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
   				 			"?EJB=SINS_FOASSNOPATO"+
   				 			"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
   				 			"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
   				 			"&METHOD=query_senzaPato&TYPE="+form+"&d1="+d1+"&d2="+d2+"&REPORT="+report+
   				 			"&pca="+pca+"&distretto="+distr+"&zona="+zone;
		
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
            }
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
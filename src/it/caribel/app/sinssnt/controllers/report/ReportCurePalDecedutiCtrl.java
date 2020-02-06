package it.caribel.app.sinssnt.controllers.report;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
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
import org.zkoss.util.resource.Labels;

public class ReportCurePalDecedutiCtrl extends CaribelFormCtrl {
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
	        String d1= dadata.getValueForIsas();
	        String d2=adata.getValueForIsas();
	        String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi=res_dom.getSelectedItem().getValue();
			String formato = formatoStampa.getSelectedItem().getValue();
			String TYPE=(formato.equals("PDF")?"PDF":"application/vnd.ms-excel");
            String metodo = "query_report";
            String report = (formato.equals("PDF")?"riep_decessi.fo":"riep_decessi.html");
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
	        //invocazione alla servlet
	        String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FORIEPDECESSI"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
	                "&METHOD="+metodo+
	                "&d1="+d1+"&d2="+d2+
	                "&zona="+zone+"&distretto="+distr+"&pca="+pca+
	                "&ragg="+ragg+"&terr="+terr+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                "&TYPE="+TYPE+"&REPORT="+report; 
	                
	        
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
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

public class ReportCartelleChiuseCtrl extends CaribelFormCtrl {
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
	
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	
	protected CaribelCombobox mot_ch;
	
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c = (PanelUbicazioneNesDivCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggruppamento("CA");
			initCombo(mot_ch);
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			String data1=dadata.getValueForIsas();               
            String data2 = adata.getValueForIsas(); 
            String raggr = raggruppamento.getSelectedItem().getValue();
            String zone = zona.getSelectedItem().getValue();
 	        String distr = "";
 	        String pca = "";
 	        if (zone.equals("TUTTO"))
 				zone="";
 			if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals("") || distretto.getValue().equals("NODIV") || distretto.getValue().equals("NESDIV") || distretto.getValue().equals("NESSUNA DIVISIONE"))
 				distr="";
 			else 
 				distr = distretto.getSelectedItem().getValue();			
 			
 			if(presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals("") || presidio_comune_area.getValue().equals("NODIV") || presidio_comune_area.getValue().equals("NESDIV") || presidio_comune_area.getValue().equals("NESSUNA DIVISIONE"))
 	              pca="";
 			else  
 				pca=presidio_comune_area.getSelectedItem().getValue();
 			
 	        String terr="";
		           
 	        if(zone.equals("NESDIV")){
 	        	terr="0|";
		        zone="";
		    }else
		        terr="1|";
		        if(distretto.equals("NESDIV") || distr.equals("")){
		         	terr=terr+"0|";
		            distr="";
		         }else
		            terr=terr+"1|";
		       if(pca.equals("NESDIV") || pca.equals("")){
		                    terr=terr+"0|";
		                    pca="";
		            }else
		                    terr=terr+"1";

		       String formato = formatoStampa.getSelectedItem().getValue();
		       String mot = "";
		       if (!mot_ch.getValue().equals(""))
		    	   mot = mot_ch.getSelectedItem().getValue();
		       String socsan = soc_san.getSelectedItem().getValue();
			   String tipo_ubi=res_dom.getSelectedItem().getValue();
		       String TYPE=(formato.equals("PDF")?"PDF":"application/vnd.ms-excel");
		       String report=(formato.equals("PDF")?"cart_chiuse.fo":"cart_chiuse.html");
		       String servlet = "";
		            servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
				 			  "?EJB=SINS_FOCARTCHIUSE"+
				 			  "&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
				 			  "&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
		                      "&METHOD=query_chiuse" +
		                      "&TYPE=" + TYPE + "&data1=" + data1 +
		                      "&data2=" + data2 +"&motivo="+mot+
		                      "&pca="+pca+"&distretto="+distr+"&zona="+zone+"&dom_res="+tipo_ubi+"&socsan="+socsan+
		                      "&ragg="+raggr+"&terr="+terr+
		                      "&REPORT="+report;
		            
		            
		
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo(CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "", "");
		CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("generic.motivo_chiusura1"));
		CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("generic.motivo_chiusura2"));
		
		cbx.setSelectedValue("");
	
	}
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
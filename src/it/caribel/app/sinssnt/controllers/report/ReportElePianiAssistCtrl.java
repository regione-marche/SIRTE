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

public class ReportElePianiAssistCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup pres_piano;
	
	
	protected Radio pdf;
	protected Radio html;
	protected Radio att;
	protected Radio ch;
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	
	protected CaribelCombobox compl_ass;
	
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggr("A");
			initCombo();
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String d1=dadata.getValueForIsas();               
            String d2 = adata.getValueForIsas();   
            
            String formato = formatoStampa.getSelectedItem().getValue();
            String tpFormato = (formato.equals("1")?"PDF":"application/vnd.ms-excel");
            String report = (formato.equals("1")?"elencoPianiAssist.fo":"elencoPianiAssist.html");

            String tipo_piano = pres_piano.getSelectedItem().getValue();
            String comp_ass="";
            if (!compl_ass.getValue().equals(""))
           	 comp_ass = compl_ass.getSelectedItem().getValue();

            
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
 	        
                 if(zone.equals("TUTTO"))
                 {
                         terr="0|0|0";
                         zone="";
                         distr="";
                         pca="";
                 }else{
                       if(distr.equals("TUTTO"))
                       {
                              terr=terr+"1|0|0";
                              distr="";
                              pca="";
                       }else{
                             if(pca.equals("TUTTO"))
                             {
                                 terr=terr+"1|1|0";
                                 pca="";
                             }
                             else
                                 terr=terr+"1|1|1";
                       }
                 }

	    String servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
				 			  "?EJB=SINS_FOELEPIAASS"+
                              "&METHOD=query_report" +
                              "&TYPE=" + tpFormato+"&d1="+d1+"&d2=" + d2
                              + "&tipo_piano=" + tipo_piano
                              + "&compl_ass=" + comp_ass
                              +"&REPORT=" + report
                              + "&zona=" + zone + "&distretto=" + distr + "&pca=" + pca+
                              "&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
     	    				 "&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword();
     	    		

			 
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo() {
		try {
			compl_ass.clear();
			compl_ass.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			
			Hashtable h_xCBdaTabBase = new Hashtable(); // x le comboBox da caricare
//			
			h_xCBdaTabBase.put("COMPLASS", compl_ass);
			
			CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h, h_xCBdaTabBase, new Hashtable(), "tab_val", "tab_descrizione", false);
		}catch(Exception e){
			doShowException(e);
		}
	
	}
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
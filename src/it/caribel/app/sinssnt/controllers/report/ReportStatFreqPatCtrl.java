package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForUI;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;

public class ReportStatFreqPatCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;	
	
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
	

	protected CaribelCombobox tipo_op;
	
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	protected CaribelTextbox coddiag;
	protected CaribelTextbox coddiag1;
	protected CaribelIntbox frequenza;
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			
			c.doInitPanel();
			c.settaRaggruppamento("CA");
			dadata.setFocus(true);
			frequenza.setText("0");
			initCombo(tipo_op);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String tipo = tipo_op.getSelectedItem().getValue();		
		    
		    String data_inizio=dadata.getValueForIsas();
		    String data_fine=adata.getValueForIsas();
		    String val= frequenza.getText();
		    String cod1=coddiag.getValue();
		    String cod2=coddiag1.getValue();
		    String ragg=raggruppamento.getSelectedItem().getValue();
		    String zone=zona.getSelectedItem().getValue();
		    String distr="";
		    String pca="";
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
			
			 String socsan = soc_san.getSelectedItem().getValue();
			 String tipo_ubi=res_dom.getSelectedItem().getValue();
		    

		    String tp = formatoStampa.getSelectedItem().getValue();
		    String report = "freqpat.fo";
		    String type = "PDF";
		    if (tp.equals("2"))
		    {
		        report = "freqpat.html";
		        type = "application/vnd.ms-excel";
		    }
		    if (tipo.equals("")){
	        	 UtilForUI.standardExclamation(Labels.getLabel("ReportStatFreqPat.tipo_op.msg"));
	        	
		    }
		    else{
		    String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOFREQPAT"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
		    		"&METHOD=query_freqpat&data_inizio="+data_inizio+
		            "&data_fine="+data_fine+"&val="+val+"&cod1="+cod1+"&cod2="+cod2+
		            "&ragg="+ragg+"&pca="+pca+"&distretto="+distr+"&dom_res="+tipo_ubi+"&socsan="+socsan+
		            "&zona="+zone+"&tipo="+tipo+"&TYPE=" + type + "&REPORT=" + report;
			
		  
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
		    }
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo (CaribelCombobox cbx)throws Exception {
		cbx.clear();		
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.operatore2"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.operatore3"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.operatore4"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.operatore5"));
		//CaribelComboRepository.addComboItem(cbx, "52", Labels.getLabel("generic.operatore6"));
		
		cbx.setSelectedValue("02");
	
	}
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component; 
import org.zkoss.zul.Radio;

public class ReportEleAssAccessiCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	private String ver = "3- ";
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup tipoStampa;	
	
	protected Radio html;
	protected Radio pdf;	
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
	
	protected CaribelCombobox tipo_op;
	protected CaribelCombobox motivo;
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			
			c.doInitPanel();
			dadata.setFocus(true);
			initCombo(tipo_op);
			abilitazioneMaschera();
		}catch(Exception e){
			doShowException(e);
		}
	}
	private void abilitazioneMaschera() {
		String punto = ver + "abilitazioneMaschera ";
		String tipoOperatore = ManagerProfile.getTipoOperatore(getProfile());
		tipo_op.setSelectedValue(tipoOperatore);
		if (UtilForContainer.isSegregeriaOrganizzativa()) {
			logger.trace(punto + " posso modificare la combo delle figure professionali ");
		} else {
			tipo_op.setReadonly(true);
			tipo_op.setDisabled(true);
		}
	}
	public void doStampa() {
		try {
			String figprof=tipo_op.getSelectedItem().getValue();
			String mot ="";			
			if (motivo.getValue()!=null && !motivo.getValue().equals(""))
				mot=motivo.getSelectedItem().getValue();
			else mot="-1";
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

            String anafo = "ele_iass.fo";
            String sintfo = "ele_isint.fo";
            if (tp.equals("2"))
            {
                anafo = "ele_iass.html";
                sintfo = "ele_isint.html";

            }

            String tpFormato = (tp.equals("1") ? "PDF" : "application/vnd.ms-excel");
            String data_inizio= dadata.getValueForIsas();            
            String data_fine=adata.getValueForIsas();   
            
            String servlet="";
            String tipocura = "TUTTO";
            String piem = "N";
           
            if(ragg.equals("P"))
            {
            if (an.isSelected())
                servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
    					"?EJB=SINS_FOIASSELE"+
    					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
    					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
    					"&METHOD=query_inferef"+
    					"&data_inizio="+data_inizio+"&data_fine="+data_fine+"&ragg="+ragg+"&piem="+piem+
    					"&distretto="+distr+"&figprof="+figprof+"&tc="+tipocura+"&tp="+tp+
    					"&zona="+zone+"&pca="+pca+"&REPORT="+anafo + "&TYPE=" + tpFormato+"&motivo="+mot;
            	else
            		servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
        					"?EJB=SINS_FOIASSELE"+
        					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
        					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
        					"&METHOD=query_infesint"+
        					"&data_inizio="+data_inizio+"&data_fine="+data_fine+
        					"&distretto="+distr+"&figprof="+figprof+"&ragg="+ragg+"&tc="+tipocura+"&piem="+piem+"&tp="+tp+
        					"&zona="+zone+"&pca="+pca+"&REPORT="+sintfo + "&TYPE=" + tpFormato+"&motivo="+mot;
            	}else
            {
            	if (an.isSelected())
            		servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
        					"?EJB=SINS_FOIASSELE"+
        					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
        					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
        					"&METHOD=query_inferef"+
        					"&data_inizio="+data_inizio+"&data_fine="+data_fine+"&ragg="+ragg+"&tc="+tipocura+"&piem="+piem+
        					"&distretto="+distr+"&figprof="+figprof+"&dom_res="+tipo_ubi+"&socsan="+socsan+"&tp="+tp+
        					"&zona="+zone+"&pca="+pca+"&REPORT="+anafo + "&TYPE=" + tpFormato+"&motivo="+mot;
            	else
            		servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
        					"?EJB=SINS_FOIASSELE"+
        					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
        					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
        					"&METHOD=query_infesint"+
        					"&data_inizio="+data_inizio+"&data_fine="+data_fine+"&tc="+tipocura+"&piem="+piem+
        					"&distretto="+distr+"&figprof="+figprof+"&ragg="+ragg+"&dom_res="+tipo_ubi+"&socsan="+socsan+"&tp="+tp+
        					"&zona="+zone+"&pca="+pca+"&REPORT="+sintfo + "&TYPE=" + tpFormato+"&motivo="+mot;
            	}
            
            
            
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
		   
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	public void initCombo (CaribelCombobox cbx)throws Exception {
		/*cbx.clear();		
		CaribelComboRepository.addComboItem(cbx, "00", Labels.getLabel("generic.operatore1"));
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.operatore2"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.operatore3"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.operatore4"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.operatore5"));
		//CaribelComboRepository.addComboItem(cbx, "52", Labels.getLabel("generic.operatore6"));
		
		cbx.setSelectedValue("02");*/
		cbx.clear();		
		ManagerOperatore.loadTipiOperatori(cbx, true);
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(motivo);
	
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
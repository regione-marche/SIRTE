package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Radio;

public class ReportElencoPrestazioniCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	private String ver = "3- ";
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
	protected CaribelTextbox codope;
	//protected CaribelTextbox codope1;
	
	
	protected CaribelCombobox tipo_op;
	protected CaribelCombobox qual_op;
	protected CaribelCombobox motivo;
	
	//protected CaribelRadiogroup tp_prest;
	
	protected Radio dom;
	protected Radio am;
	protected Radio en;
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			dadata.setFocus(true);
			initComboTipoOp(tipo_op);
			abilitazioneMaschera();
			initComboQualificaOp(qual_op);
			initComboLivello();
			
			
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
			
	     String codice_inizio=codope.getValue();
	     //String codice_fine=codope1.getValue();
	     String figprof=tipo_op.getSelectedItem().getValue();
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
			String formato = formatoStampa.getSelectedItem().getValue();
	        String tpFormato = (formato.equals("1") ? "PDF" : "application/vnd.ms-excel");

	        String filefo="";
	        String piem = "N";
	        
	        filefo=formato.equals("1") ? "inf_eleprest.fo" : "inf_eleprest.html";
	           
	        String tipo_prest = "";

	        String tipocura = "";
	          
	        String data_inizio=dadata.getValueForIsas();
	        String data_fine=adata.getValueForIsas();
	        String mot ="";			
			if (motivo.getValue()!=null && !motivo.getValue().equals(""))
				mot=motivo.getSelectedItem().getValue();
			else mot="-1";
	        //String tipo_accert=tp_prest.getSelectedItem().getValue();
	        
	        String servlet="";
	        String qualifica="";
	        if(tipo_op.getSelectedItem().getValue().equals("00"))
		    	   qualifica="";
		       else
		    	   qualifica=qual_op.getSelectedItem().getValue();
	        if(ragg.equals("P"))
	           servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
			    		"?EJB=SINS_FOIPREST"+
			    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
			    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
			    		"&METHOD=query_elencoPrest" +
			    		"&codice_inizio="+codice_inizio+//"&codice_fine="+codice_fine+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+"&figprof="+figprof+"&tc="+tipocura+"&tprest="+tipo_prest+"&piem="+piem+
	                    "&distretto="+distr+"&ragg="+ragg+
	                    //"&tipo_accert="+tipo_accert+
	                    "&qualifica="+qualifica+
	                    "&zona="+zone+"&pca="+pca+"&REPORT="+filefo + "&TYPE=" + tpFormato+"&motivo="+mot;
	         else
	         	servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
			    		"?EJB=SINS_FOIPREST"+
			    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
			    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
			    		"&METHOD=query_elencoPrest" +
			    		"&codice_inizio="+codice_inizio+//"&codice_fine="+codice_fine+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+"&figprof="+figprof+"&tc="+tipocura+"&tprest="+tipo_prest+"&piem="+piem+
	                    "&distretto="+distr+"&ragg="+ragg+
	                    //"&tipo_accert="+tipo_accert+
	                    "&qualifica="+qualifica+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                    "&zona="+zone+"&pca="+pca+"&REPORT="+filefo + "&TYPE=" + tpFormato+"&motivo="+mot;
			
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initComboTipoOp (CaribelCombobox cbx)throws Exception {
		/*cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "00", Labels.getLabel("generic.operatore1"));
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.operatore2"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.operatore3"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.operatore4"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.operatore5"));
		//CaribelComboRepository.addComboItem(cbx, "52", Labels.getLabel("generic.operatore6"));
		CaribelComboRepository.addComboItem(cbx, "98", Labels.getLabel("generic.operatore7"));
		
		cbx.setSelectedValue("02");*/
		cbx.clear();		
		ManagerOperatore.loadTipiOperatori(cbx, true);
	
	}
	
	public void initComboQualificaOp (CaribelCombobox qual)throws Exception {
		qual_op.clear();
		qual_op.setDisabled(false);
		Hashtable<String, Object> h = new Hashtable();
		String oper =tipo_op.getSelectedValue();							
		h.put("tipo_oper",oper);
		
		CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);
		qual_op.setSelectedValue("TUTTE");
	}
	
	
	public void onSelect$tipo_op(Event event){
		try{
			
			Hashtable<String, Object> h = new Hashtable();
			
			 if (tipo_op.getSelectedItem().getValue().equals("01")){				 
				 qual_op.clear();
				 initComboQualificaOp(qual_op);
			 }
	         else if (tipo_op.getSelectedItem().getValue().equals("02")){
	            	
	            	qual_op.clear();
	            	initComboQualificaOp(qual_op);
	         }
	         else if (tipo_op.getSelectedItem().getValue().equals("00")){	            	
	        	
	        	 qual_op.clear();
	        	 qual_op.setSelectedValue("");
	        	 qual_op.setDisabled(true);
	         }
	         else {
	        	 
	        	 qual_op.clear();
	        	 initComboQualificaOp(qual_op);
	        	
	         }
	       
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initComboLivello ()throws Exception {	
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(motivo);
	}
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
package it.caribel.app.sinssnt.controllers.report;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.app.sinssnt.bean.modificati.BrancaEJB;
import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneTpPresCtrl;
import it.caribel.app.sinssnt.controllers.report.common.PanelFasciaEtaCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;

import java.util.*;

import org.zkoss.util.resource.Labels;

public class ReportEleAccessiOpCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	CaribelSessionManager profile = CaribelSessionManager.getInstance();
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup tipoPrest;
	protected CaribelRadiogroup salto_pagina;
	
	
	
	protected Radio pdf;
	protected Radio html;
	protected Radio dom;
	protected Radio amb;
	protected Radio en;
	protected Radio no_salto;
	protected Radio si_salto;
	
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup res_dom;
	protected CaribelRadiogroup tp_presidio;
	protected Radio contatto;
	protected Radio accesso;
	
	protected Radio presidio;
	protected Radio comune;
	protected Radio area_dis;
	

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	protected CaribelCombobox tipo_op;
	protected CaribelCombobox qual_op;
	
	protected CaribelTextbox codope;
	
	public void doInitForm() {
		try {
			Component p1 = self.getFellow("panel_ubicazioneTpPres");
			PanelUbicazioneTpPresCtrl c1 = (PanelUbicazioneTpPresCtrl)p1.getAttribute(MY_CTRL_KEY);
			c1.doInitPanel();
			c1.settaRaggruppamentoNoUbic("PCA");
			
			
			initComboTipoOp(tipo_op);			
			initComboQualificaOp(qual_op);
			presidio.setSelected(true);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {

			String codice_inizio=codope.getValue();
			String data_inizio=dadata.getValueForIsas();    	            
            String data_fine=adata.getValueForIsas();    
            String tipo_accert=tipoPrest.getSelectedItem().getValue();
            //invocazione alla servlet
            String servlet="";
            String qualifica="";
	        if(tipo_op.getSelectedItem().getValue().equals("00"))
		    	   qualifica="";
		       else
		    	   qualifica=qual_op.getSelectedItem().getValue();
            String figprof=tipo_op.getSelectedItem().getValue();
            String cambio=salto_pagina.getSelectedItem().getValue();
            if(cambio.equals("S") && codice_inizio.equals("")){
            	 UtilForUI.standardExclamation(Labels.getLabel("ReportEleAccessiOp.tipo_op.msg"));
            }
            String tp = (!(profile.getStringFromProfile("vco_tempof").equals("NO"))?profile.getStringFromProfile("vco_tempof"):"0");


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
				
				String socsan = soc_san.getSelectedItem().getValue();
				String tipo_ubi=res_dom.getSelectedItem().getValue();

            String tipoPresidio = tp_presidio.getSelectedItem().getValue();

        String formato = formatoStampa.getSelectedItem().getValue();
        String report = (!(profile.getStringFromProfile("vco_tempof").equals("NO"))?"ele_intervope_vco.":"ele_intervope.");
        String TYPE ="";
        if (formato.equals("0")) {
          report += "fo";
          TYPE = "PDF";
        } else if (formato.equals("1")) {
          report += "html";
          TYPE = "application/vnd.ms-excel";
        } else {
          report += "html";
          TYPE = "application/vnd.ms-excel";
        }
        if(cambio.equals("S") && codice_inizio.equals("")){
       	 UtilForUI.standardExclamation(Labels.getLabel("ReportEleAccessiOp.tipo_op.msg"));
       }else{
            servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
		    		"?EJB=SINS_FOINTERVOPE"+
		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+	               
            		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
            		"&METHOD=query_intervope&codope="+codice_inizio+
            		"&data_inizio="+data_inizio+"&data_fine="+data_fine+"&tipo_accert="+tipo_accert+
            		"&zona=" + zone + "&distretto=" + distr + "&pca=" + pca +
            		"&tppres=" + tipoPresidio +"&qualifica=" + qualifica +
            		"&ragg="+raggr+"&dom_res="+tipo_ubi+"&socsan="+socsan+"&tp="+tp+
            		"&REPORT="+ report + "&TYPE=" + TYPE + 
            		"&figprof="+figprof+"&cambio="+cambio;
	          
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
       }
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initComboTipoOp (CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "00", Labels.getLabel("generic.operatore1"));
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.operatore2"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.operatore3"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.operatore4"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.operatore5"));
		//CaribelComboRepository.addComboItem(cbx, "52", Labels.getLabel("generic.operatore6"));
		CaribelComboRepository.addComboItem(cbx, "98", Labels.getLabel("generic.operatore7"));
		cbx.setSelectedValue("02");
	
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
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
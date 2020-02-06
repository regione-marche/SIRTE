package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.app.sinssnt.bean.modificati.BrancaEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Radio;

public class ReportOreAssCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup tipoPrest;
	protected CaribelRadiogroup cont_su;
	
	protected Radio pdf;
	protected Radio html;	
	protected Radio dom;
	protected Radio amb;
	protected Radio en;
	protected Radio acc;
	protected Radio prest;
	protected Radio ass;
	
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
	protected CaribelCombobox qual_op;
	protected CaribelCombobox branca;
	protected CaribelCombobox tp_aggr;
	
	protected CaribelTextbox codope;
	
	public void doInitForm() {
		try {
			Component p1 = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c1 = (PanelUbicazioneCtrl)p1.getAttribute(MY_CTRL_KEY);
			c1.doInitPanel();			
			
			dadata.setFocus(true);
			initComboTipoOp(tipo_op);
			initComboBranca();
			initComboQualificaOp(qual_op);
			initComboTpAggr(tp_aggr);
			presidio.setSelected(true);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			  String figprof=tipo_op.getSelectedItem().getValue();
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
	            
	            String data_inizio= dadata.getValueForIsas();    
	            String data_fine=adata.getValueForIsas();    
	            
	            String op=codope.getValue();
	            String branc="";
	            String tipopre="";
	           // if (branc.equals(""))
	            //	 branc="";
	            
	            if(branca.getValue().equals("TUTTE"))
	            	branc="";
	            else if (branca.getValue().equals(""))
	            	branc="";
	            else if (!branca.getValue().equals("") && !branca.getValue().equals("TUTTE")){
	                String codice=branca.getValue();
	                StringTokenizer st = new StringTokenizer(codice,"|");
	                tipopre=st.nextToken();
	                //branc=st.nextToken() ;
	                
	               
	            }
	            if(branc.trim().equals("TUT"))
	              branc="";
	            if(tipopre.trim().equals("TUT") || branc.trim().equals("") )
	              tipopre="";

	            String tipo=tp_aggr.getSelectedItem().getValue();
	            
	            String tipo_prest=tipoPrest.getSelectedItem().getValue();
	            String cont=cont_su.getSelectedItem().getValue();
	            String ore =cont_su.getSelectedItem().getValue();
	            //invocazione alla servlet
	            String servlet="";
	            String qualifica="";
		        if(tipo_op.getSelectedItem().getValue().equals("00"))
			    	   qualifica="";
			       else
			    	   qualifica=qual_op.getSelectedItem().getValue();
	            String formato="1";
	            String tipoform="PDF";
	            if (html.isSelected())
	            {
	                        tipoform="application/vnd.ms-excel";
	                        formato="0";
	            }
	          if(raggr.equals("P"))
	            servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
    		    		"?EJB=SINS_FOOREASS"+
    		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+               
	          			"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
	          			"&METHOD=query_oreass"+
	          			"&figprof="+figprof+"&TYPE="+tipoform+"&tipo_prest="+tipo_prest+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                    "&distretto="+distr+"&branca="+branc+"&ragg="+raggr+
	                    "&tipopre="+tipopre+"&tipo="+tipo+"&formato="+formato+"&qualifica="+qualifica+
	                    "&pca="+pca+"&zona="+zone+"&ore="+ore+"&op="+op+"&REPORT=x.fo";
	         else
	            servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
    		    		"?EJB=SINS_FOOREASS"+
    		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+               
	          			"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
	          			"&METHOD=query_oreass"+
	          			"&figprof="+figprof+"&TYPE="+tipoform+"&tipo_prest="+tipo_prest+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                    "&distretto="+distr+"&branca="+branc+"&ragg="+raggr+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                    "&tipopre="+tipopre+"&tipo="+tipo+"&formato="+formato+"&qualifica="+qualifica+
	                    "&pca="+pca+"&zona="+zone+"&ore="+ore+"&op="+op+"&REPORT=x.fo";
	          
			  
	        
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
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
	
	public void initComboTpAggr (CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "Z", Labels.getLabel("generic.tp_aggregazione1"));
		CaribelComboRepository.addComboItem(cbx, "D", Labels.getLabel("generic.tp_aggregazione2"));
		CaribelComboRepository.addComboItem(cbx, "C", Labels.getLabel("generic.tp_aggregazione4"));
		CaribelComboRepository.addComboItem(cbx, "B", Labels.getLabel("generic.tp_aggregazione4"));
		CaribelComboRepository.addComboItem(cbx, "O", Labels.getLabel("generic.tp_aggregazione5"));
		
		cbx.setSelectedValue("Z");
	
	}
	
	public void initComboBranca ()throws Exception {
		branca.clear();
		branca.setDisabled(false);
		
		Hashtable<String, Object> h = new Hashtable();
		
        CaribelComboRepository.comboPreLoad("combo_branca", new BrancaEJB(), "query_combo",h, branca, null, "cod_branca", "descrizione", false);
        	
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
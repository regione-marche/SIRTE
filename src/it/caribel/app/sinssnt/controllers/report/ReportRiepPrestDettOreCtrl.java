package it.caribel.app.sinssnt.controllers.report;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.app.sinssnt.bean.TiputeEJB;
import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneNesDivCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForBinding;
import java.util.*;

import org.zkoss.util.resource.Labels;

public class ReportRiepPrestDettOreCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup tipoPrest;
	
	protected Radio pdf;
	protected Radio html;	
	protected Radio dom;
	protected Radio amb;
	protected Radio en;
	
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
	protected CaribelCombobox tp_utente;	

	
	public void doInitForm() {
		try {
			Component p1 = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c1 = (PanelUbicazioneNesDivCtrl)p1.getAttribute(MY_CTRL_KEY);
			c1.doInitPanel();			
			
			dadata.setFocus(true);
			initComboTipoOp(tipo_op);			
			initComboQualificaOp(qual_op);
			initComboTpUtente();
			presidio.setSelected(true);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
						
			String metodo="query_dettore";
			
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr="";
			String pca="";
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
				
				String figprof=tipo_op.getSelectedItem().getValue();

				String data_inizio= dadata.getValueForIsas();    
	            String data_fine=adata.getValueForIsas();    
	            String ute="";
	           
	          if(tp_utente.getSelectedItem().getValue().equals("TUTTO"))  
	        	  ute="";
	          else
	        	  ute=tp_utente.getSelectedItem().getValue();
	          

	          String tipopz=tipoPrest.getSelectedItem().getValue();

	          String terr="";
	          
	          if(zone.equals("NESDIV"))
	          {
	                  terr="0|";
	                  zone="";
	          }
	          else
	                  terr="1|";
	          if(distretto.equals("NESDIV") || distr.equals(""))
	          {
	                  terr=terr+"0|";
	                  distr="";
	          }
	          else
	                  terr=terr+"1|";
	          if(pca.equals("NESDIV") || pca.equals(""))
	          {
	                  terr=terr+"0|";
	                  pca="";
	          }
	          else
	                  terr=terr+"1|";
	          if(ute.equals("NESDIV"))
	          {
	                  terr=terr+"0|";
	                  ute="";
	          }
	          else
	                  terr=terr+"1|";

	         String formato="1";
	         String TYPE="PDF";
	         String report="dettoreprest.fo";
	         String socsan = soc_san.getSelectedItem().getValue();
			 String tipo_ubi=res_dom.getSelectedItem().getValue();
	          
			 if (html.isSelected())
	          {
	                  TYPE="application/vnd.ms-excel";
	                  formato="0";
	                  report="dettoreprest.html";
	          }

	            String  servlet ;
	            String qualifica="";
		        if(tipo_op.getSelectedItem().getValue().equals("00"))
			    	   qualifica="";
			       else
			    	   qualifica=qual_op.getSelectedItem().getValue();
	            if (raggr.equals("P"))
	                servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
	    		    		"?EJB=SINS_FODETTOREPREST"+
	    		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+               
		          			"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
		          			"&METHOD=" +metodo+"&TYPE="+TYPE+
	                        "&formato=" + formato+"&figprof="+figprof+
	                        "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                        "&ragg="+raggr+"&pca="+pca+
	                        "&distretto="+distr+"&zona="+zone+
	                        "&terr=" + terr +"&ute=" + ute +
	                        "&tipopz=" + tipopz+
	                        "&qualifica=" + qualifica+
	                        "&REPORT="+report;
	           else
	               servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+
	    		    		"?EJB=SINS_FODETTOREPREST"+
	    		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+               
		          			"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
		          			"&METHOD=" +metodo+"&TYPE="+TYPE+
	                        "&formato=" + formato+"&figprof="+figprof+
	                        "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                        "&ragg="+raggr+"&pca="+pca+
	                        "&distretto="+distr+"&zona="+zone+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                        "&terr=" + terr +"&ute=" + ute +
	                        "&tipopz=" + tipopz+
	                        "&qualifica=" + qualifica+
	                        "&REPORT="+report;
	            
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initComboTipoOp (CaribelCombobox cbx)throws Exception {
		cbx.clear();		
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.operatore2"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.operatore3"));
		
		cbx.setSelectedValue("01");
	
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
	
	public void initComboTpUtente ()throws Exception {
		tp_utente.clear();
		tp_utente.setDisabled(false);
		
		Hashtable<String, Object> h = new Hashtable();
		
        CaribelComboRepository.comboPreLoad("combo_tiputestsoc", new TiputeEJB(), "queryComboNESDIV",h, tp_utente, null, "codice", "descrizione", false);
        tp_utente.setSelectedValue("TUTTE");	
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
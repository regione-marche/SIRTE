package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;

public class ReportStatAccessiDomCtrl extends CaribelFormCtrl {
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
	

	protected CaribelCombobox motivo;
	protected CaribelCombobox tipo_op;
	protected CaribelCombobox qual_op;
	
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();		
			dadata.setFocus(true);
			initComboTipoOp(tipo_op);
			initComboQualifica();
			initComboMotivo();
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String figprof=tipo_op.getSelectedItem().getValue();
			String ragg = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			if (distretto.getValue().equals("TUTTO") || distretto.getValue().equals(""))
				distr="";
			else 
				distr = distretto.getSelectedItem().getValue();			
			
			if(presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals(""))
	              pca="";
			else  
				pca=presidio_comune_area.getSelectedItem().getValue();
			
			if (zone.equals("TUTTO"))
				zone="";
			if (distr.equals("TUTTO"))
				distr="";
			
	       String data_inizio= dadata.getValueForIsas();
	       String data_fine=adata.getValueForIsas();
	       String mot="";
	       String qualifica="";

	       String report="fostatdomiciliare";
	       if (!motivo.getValue().equals("")){	
	    	   if(figprof.equals("01") || figprof.equals("02") )
	    		   mot=motivo.getSelectedItem().getValue();	
	       }
	       if (motivo.getValue().equals("")){	
	    	   if(figprof.equals("02") )
	    		   mot="TUTTO";
	       }
	       if(tipo_op.getSelectedItem().getValue().equals("00"))
	    	   qualifica="";
	       else
	    	   qualifica=qual_op.getSelectedItem().getValue();	
	       String unifun="";
	            
	       String fStampa = formatoStampa.getSelectedItem().getValue();
 
	       String type = "";

	       if (fStampa.equals("P")) {	       
	       	report += ".fo";
	      	type = "PDF";
	      	} else{
	      		report += ".html";
	            type = "application/vnd.ms-excel";
	          }

	        String servlet="";
	        String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi=res_dom.getSelectedItem().getValue();	           
	        if(ragg.equals("P"))
	            servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
						"?EJB=SINS_FOSTATDOMICILIARE"+
						"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
						"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+	            
						"&METHOD=query_statdom&"+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                    "&distretto="+distr+"&figprof="+figprof+"&ragg="+ragg+
	                    "&unifun="+unifun+"&motivo="+mot+"&qualifica="+qualifica+
	                    "&pca="+pca+"&zona="+zone+"&TYPE=" + type + "&REPORT="+report;
	         else
	            servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
						"?EJB=SINS_FOSTATDOMICILIARE"+
						"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
						"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
						"&METHOD=query_statdom&"+
	                    "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                    "&distretto="+distr+"&figprof="+figprof+"&ragg="+ragg+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                    "&unifun="+unifun+"&motivo="+mot+"&qualifica="+qualifica+
	                    "&pca="+pca+"&zona="+zone+"&TYPE=" + type + "&REPORT="+report;

	            
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initComboQualifica() {
		try {
			qual_op.clear();
			qual_op.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			String oper =tipo_op.getSelectedValue();							
			h.put("tipo_oper",oper);
			
			CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);
			qual_op.setSelectedValue("TUTTE");
		}catch(Exception e){
			doShowException(e);
		}
	
	}
	
	public void initComboMotivo() {
		try {
			/*motivo.clear();
			motivo.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			if (tipo_op.getValue().equals("01"))
				CaribelComboRepository.comboPreLoad("combo_motivo", new MotivoEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);
			else
				CaribelComboRepository.comboPreLoad("combo_motivos", new MotivoSEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);
			motivo.setSelectedValue("TUTTI");*/
			Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
			Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
			Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
			h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_MOTIVO, motivo);
			CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
					"tab_descrizione", false);
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
	
	public void onSelect$tipo_op(Event event){
		try{
			System.out.println("tipo_op.getSelectedItem().getValue()" + tipo_op.getSelectedItem().getValue());
			Hashtable<String, Object> h = new Hashtable();	
			String oper =tipo_op.getSelectedValue();							
			h.put("tipo_oper",oper);
			if (tipo_op.getSelectedItem().getValue().equals("00")){
				motivo.clear();				
				qual_op.clear();
				
				motivo.setDisabled(true);
				qual_op.setDisabled(true);
				
			}
			if (!tipo_op.getSelectedItem().getValue().equals("01")&&!tipo_op.getSelectedItem().getValue().equals("02")&&!tipo_op.getSelectedItem().getValue().equals("00")){
				qual_op.clear();
				qual_op.setDisabled(false);
				CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);

				motivo.setDisabled(true);
			}
			if (tipo_op.getSelectedItem().getValue().equals("01")){
				qual_op.clear();
				qual_op.setDisabled(false);
				//motivo.clear();
				motivo.setDisabled(false);
				//CaribelComboRepository.comboPreLoad("combo_motivo", new MotivoEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);*/
				Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
				Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
				Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
				h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_MOTIVO, motivo);
				CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
						"tab_descrizione", false);
				CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);
				
			}
			if (tipo_op.getSelectedItem().getValue().equals("02")){
				qual_op.clear();
				qual_op.setDisabled(false);
				//motivo.clear();
				motivo.setDisabled(false);
				//CaribelComboRepository.comboPreLoad("combo_motivos", new MotivoSEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);*/
				Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
				Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
				Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
				h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_MOTIVO, motivo);
				CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
						"tab_descrizione", false);
				CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);
				
			}
			if (!tipo_op.getSelectedItem().getValue().equals("00"))
				qual_op.setSelectedValue("TUTTE");
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
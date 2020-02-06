package it.caribel.app.sinssnt.controllers.report;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneNesDivCtrl;
import it.caribel.app.sinssnt.controllers.report.common.PanelFasciaEtaCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.util.resource.Labels;

public class ReportRiepAssEtaCtrl extends CaribelFormCtrl {
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
	
	protected CaribelCombobox tipo_op;
	protected CaribelCombobox motivo;
	
	
	public void doInitForm() {
		try {
			Component p1 = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c1 = (PanelUbicazioneNesDivCtrl)p1.getAttribute(MY_CTRL_KEY);
			c1.doInitPanel();
			
			String[] arrTestiLabel = new String[]{
					Labels.getLabel("PanelSingleFasciaEta.1"), 
					Labels.getLabel("PanelSingleFasciaEta.2"),
					Labels.getLabel("PanelSingleFasciaEta.3"),
					Labels.getLabel("PanelSingleFasciaEta.4")
			};

			Component p3 = self.getFellow("panel_fascia_eta");
			
			if(p3.getChildren().size()==0){//Necessario in caso di undo
				PanelFasciaEtaCtrl c3 = (PanelFasciaEtaCtrl)p3.getAttribute(MY_CTRL_KEY);
				c3.generaPannello(arrTestiLabel);
			}
			
			dadata.setFocus(true);
			initComboTipoOp(tipo_op);
			initComboMotivo(motivo);
			presidio.setSelected(true);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {

			Component p1 = self.getFellow("panel_fascia_eta");
			PanelFasciaEtaCtrl c1 = (PanelFasciaEtaCtrl)p1.getAttribute(MY_CTRL_KEY);
			if (!c1.checkFasceEta())
                return;
			
			String figprof=tipo_op.getSelectedItem().getValue();
	        String mot="";
	        if(figprof.equals("01") || figprof.equals("02")){
			      if (motivo.getSelectedValue().equals(""))
			    	  mot = "TUTTO";
			      else
			    	  mot=motivo.getSelectedItem().getValue();
			      }
		
	        String eta = c1.getValFasce();
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
	      String formato = "1";
	      String TYPE = "PDF";

	      if (html.isSelected()) {
	        TYPE="application/vnd.ms-excel";
	        formato="2";
	      }
	        
	      String socsan = soc_san.getSelectedItem().getValue();
		  String tipo_ubi=res_dom.getSelectedItem().getValue();
	      String data_inizio= dadata.getValueForIsas();    
	      String data_fine=adata.getValueForIsas();    
	      
	      String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
		    		"?EJB=SINS_FOINFELENCO"+
		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
		    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
		    		"&METHOD=query_infelenco&"+
	                "&data_inizio="+data_inizio+"&data_fine="+data_fine+
	                "&distretto="+distr+"&figprof="+figprof+"&ragg="+ragg+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                "&motivo="+mot+"&eta="+eta+"&terr=" + terr +"&TYPE=" + TYPE + "&formato=" + formato +	                
	                "&pca="+pca+"&zona="+zone+"&REPORT=x.fo";
	        
	       
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
		
		cbx.setSelectedValue("02");
	
	}
	
	public void initComboMotivo (CaribelCombobox mot)throws Exception {
		/*motivo.clear();
		motivo.setDisabled(false);
		motivo.setSelectedValue("TUTTI");
		Hashtable<String, Object> h = new Hashtable();
		System.out.println("tipo_op.getSelectedItem().getValue()"+ tipo_op.getSelectedItem().getValue());
		 if (tipo_op.getSelectedItem().getValue().equals("01"))
			 CaribelComboRepository.comboPreLoad("combo_motivo", new MotivoEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);  		 
         else if (tipo_op.getSelectedItem().getValue().equals("02")){
            CaribelComboRepository.comboPreLoad("combo_motivos", new MotivoSEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);
		 	motivo.setSelectedValue("TUTTI");
		 }
         else {
        	 motivo.setSelectedValue("");
        	 motivo.setDisabled(true);
         }*/
//		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
//		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
//		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
//		h_xCBdaTabBase.put(Costanti.TAB_VAL_MOTIVO, motivo);
//		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
//				"tab_descrizione", false);
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(motivo);
	
	}
	
	public void onSelect$tipo_op(Event event){
		
		/*
		 * try{
		 
			//motivo.clear();
			motivo.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			//motivo.setSelectedValue("TUTTI");
			/*System.out.println("tipo_op.getSelectedItem111().getValue()"+ tipo_op.getSelectedItem().getValue());
			 if (tipo_op.getSelectedItem().getValue().equals("01")){
				 CaribelComboRepository.comboPreLoad("combo_motivo", new MotivoEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);  
				 motivo.setSelectedValue("TUTTI");
				
			 }
	         else if (tipo_op.getSelectedItem().getValue().equals("02")){
	            	CaribelComboRepository.comboPreLoad("combo_motivos", new MotivoSEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);
	            	motivo.setSelectedValue("TUTTI");
	            	
	         }* /
			if (tipo_op.getSelectedItem().getValue().equals("01")){
				 //CaribelComboRepository.comboPreLoad("combo_motivo", new MotivoEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);  
				 //motivo.setSelectedValue("TUTTI");
				 Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
					Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
					Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
					h_xCBdaTabBase.put(Costanti.TAB_VAL_MOTIVO, motivo);
					CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
							"tab_descrizione", false);
				
			 }
	         else if (tipo_op.getSelectedItem().getValue().equals("02")){
	            	//CaribelComboRepository.comboPreLoad("combo_motivos", new MotivoSEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);
	            	//motivo.setSelectedValue("TUTTI");
	        	 Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
	     		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
	     		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
	     		h_xCBdaTabBase.put(Costanti.TAB_VAL_MOTIVO, motivo);
	     		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
	     				"tab_descrizione", false);
	            
	         }
	         else if (tipo_op.getSelectedItem().getValue().equals("00")){	            	
	        	 motivo.setSelectedValue("");
	        	 motivo.setDisabled(true);
	        	
	         }
	         else {
	        	 motivo.setSelectedValue("");
	        	 motivo.setDisabled(true);	       	
	        	
	         }
	       
		}catch(Exception e){
			doShowException(e);
		}
		*/
	}
	
	

	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
package it.caribel.app.sinssnt.controllers.report;

import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;

import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneNesDivCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.QuadroSanitarioMMGCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.composite_components.CaribelTextbox;
import org.zkoss.util.resource.Labels;

public class ReportStatRiepPrestazioniCtrl extends CaribelFormCtrl {
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
	protected CaribelCombobox qual_op;
	protected CaribelCombobox motivo;
	protected CaribelCombobox tp_accesso;
	
	protected CaribelRadiogroup tp_prest;
	protected CaribelTextbox cod_ass;
	protected Radio dom;
	protected Radio am;
	protected Radio en;
	
	private static final String VALORE_ENTRAMBI = "EN";
	private static final String VALORE_ACCESSI_PRESTAZIONE = "AP";
	private static final String VALORE_ACCESSI_OCCASIONALI = "AO";
	
	boolean valoriDef_unifun=false;
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c = (PanelUbicazioneNesDivCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			dadata.setFocus(true);
			initComboTipoOp(tipo_op);
			initComboQualificaOp(qual_op);
			initComboMotivo(motivo);
			initComboTpAccesso(tp_accesso);
			presidio.setSelected(true);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String data_inizio=dadata.getValueForIsas();
		    String data_fine=adata.getValueForIsas();
		    String tipo_prest=tp_prest.getSelectedItem().getValue();
		    String combo="";
		    String report="";
		    String servlet="";
		    
		    String ragg=raggruppamento.getSelectedItem().getValue();
		    String zone=zona.getSelectedItem().getValue();
		    String distr="";
		    String pca="";
		    System.out.println("DIST" + distretto.getValue());
		    System.out.println("DIST1" + presidio_comune_area.getValue());
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
			
		   System.out.println("DISTR" + distr);
		   System.out.println("DISTR" + pca);
		    String terr = "";
		    if(zone.equals("NESDIV")){
		            terr="0|";
		            zone="";
		    }else
		            terr="1|";
		    if(distr.equals("NESDIV") || distr.equals("")){
		            terr=terr+"0|";
		            distr="";
		    }else
		            terr=terr+"1|";
		    if(pca.equals("NESDIV") || pca.equals("")){
		            terr=terr+"0|";
		            pca="";
		    }else
		            terr=terr+"1";

			String tipoAccesso = tp_accesso.getSelectedItem().getValue();

		    String tipo_st="";
		    String mot="";
		    String qualifica="";
		    String cartella=cod_ass.getText();
		    tipo_st=formatoStampa.getSelectedItem().getValue();
		  System.out.println("MOTIVO" + motivo.getSelectedValue());
		    String tipo=tipo_op.getSelectedItem().getValue();
		    if (tipo.equals("-1") || tipo.equals("08"))
		      tipo="";
		    else 
//		    	if(tipo.equals("01") || tipo.equals("02")) {
//		      if (motivo.getSelectedValue().equals(""))
//		    	  mot = "TUTTO";
//		      else
//		    	  mot=motivo.getSelectedItem().getValue();
//		      }
		      if (tipo.equals("-1"))
		       tipo="";
		       else {
		    	   if(qual_op.getValue()!=null && !qual_op.getValue().equals("") && !qual_op.getValue().equals("TUTTE"))
		    		   qualifica=qual_op.getSelectedItem().getValue();
		    	   else if(qual_op.getValue().equals("TUTTE"))
		    		   qualifica="TUTTO";
		    	   else 
		    		   qualifica="";
		       }
		 mot ="";
		 if (motivo!=null && motivo.getSelectedItem()!=null){
			 mot=motivo.getSelectedItem().getValue();      
		 }
		      
		String socsan = soc_san.getSelectedItem().getValue();
		String tipo_ubi=res_dom.getSelectedItem().getValue();
		String unifun="";
		
		    if (ragg.equals("P"))
		    servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
		    		"?EJB=SINS_FOSTATPREST"+
		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
		    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
		            "&METHOD=query_statprest&TYPE="+tipo_st+
		            "&dataini="+data_inizio+"&datafine="+data_fine+"&cartella="+cartella+
		            "&tipo_prest="+tipo_prest+"&tipo_accesso="+tipoAccesso+
					"&tipo="+tipo+"&ragg="+ragg+"&pca="+pca+"&distretto="+distr+
					"&zona="+zone+"&terr="+terr+"&unifun="+unifun+"&motivo="+mot+"&qualifica="+qualifica+"&REPORT=x.fo";

		    else   servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
		    		"?EJB=SINS_FOSTATPREST"+
		    		"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
		    		"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
		            "&METHOD=query_statprest&TYPE="+tipo_st+
		            "&dataini="+data_inizio+"&datafine="+data_fine+"&dom_res="+tipo_ubi+"&socsan="+socsan+"&cartella="+cartella+
		            "&tipo_prest="+tipo_prest+"&tipo_accesso="+tipoAccesso+
					"&tipo="+tipo+"&ragg="+ragg+"&pca="+pca+"&distretto="+distr+
					"&zona="+zone+"&terr="+terr+"&unifun="+unifun+"&motivo="+mot+"&qualifica="+qualifica+"&REPORT=x.fo";
		
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
	
	public void initComboQualificaOp (CaribelCombobox qual)throws Exception {
		qual_op.clear();
		qual_op.setDisabled(false);
		Hashtable<String, Object> h = new Hashtable();
		String oper =tipo_op.getSelectedValue();							
		h.put("tipo_oper",oper);
		
		CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);
		qual_op.setSelectedValue("TUTTE");
		//ManagerOperatore.loadTipiOperatori(qual_op,true);	
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
//         Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
//		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
//		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
//		h_xCBdaTabBase.put(Costanti.TAB_VAL_MOTIVO, motivo);
//		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
//				"tab_descrizione", false);
		
		QuadroSanitarioMMGCtrl quadroSanitarioMMGCtrl = new QuadroSanitarioMMGCtrl();
		quadroSanitarioMMGCtrl.caricaTipoCura(motivo);
	
	}
	
	public void onSelect$tipo_op(Event event){
//		try{
//			motivo.clear();
//			motivo.setDisabled(false);
//			Hashtable<String, Object> h = new Hashtable();
//			motivo.setSelectedValue("TUTTI");
//			System.out.println("tipo_op.getSelectedItem111().getValue()"+ tipo_op.getSelectedItem().getValue());
//			 if (tipo_op.getSelectedItem().getValue().equals("01")){
//				 //CaribelComboRepository.comboPreLoad("combo_motivo", new MotivoEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);  
//				 //motivo.setSelectedValue("TUTTI");
//				 Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
//					Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
//					Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
//					h_xCBdaTabBase.put(Costanti.TAB_VAL_MOTIVO, motivo);
//					CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
//							"tab_descrizione", false);
//				 qual_op.clear();
//				 initComboQualificaOp(qual_op);
//			 }
//	         else if (tipo_op.getSelectedItem().getValue().equals("02")){
//	            	//CaribelComboRepository.comboPreLoad("combo_motivos", new MotivoSEJB(), "queryTutte",h, motivo, null, "codice", "descrizione", false);
//	            	//motivo.setSelectedValue("TUTTI");
//	        	 Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
//	     		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
//	     		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
//	     		h_xCBdaTabBase.put(Costanti.TAB_VAL_MOTIVO, motivo);
//	     		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
//	     				"tab_descrizione", false);
//	            	qual_op.clear();
//	            	initComboQualificaOp(qual_op);
//	         }
//	         else if (tipo_op.getSelectedItem().getValue().equals("00")){	            	
//	        	 motivo.setSelectedValue("");
//	        	 motivo.setDisabled(true);
//	        	 qual_op.clear();
//	        	 qual_op.setSelectedValue("");
//	        	 qual_op.setDisabled(true);
//	         }
//	         else {
//	        	 motivo.setSelectedValue("");
//	        	 motivo.setDisabled(true);	
//	        	 qual_op.clear();
//	        	 initComboQualificaOp(qual_op);
//	        	
//	         }
//	       
//		}catch(Exception e){
//			doShowException(e);
//		}
		
	}
	
	public void initComboTpAccesso (CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, VALORE_ENTRAMBI, Labels.getLabel("generic.accessi1"));
		CaribelComboRepository.addComboItem(cbx, VALORE_ACCESSI_PRESTAZIONE, Labels.getLabel("generic.accessi2"));
		CaribelComboRepository.addComboItem(cbx, VALORE_ACCESSI_OCCASIONALI, Labels.getLabel("generic.accessi3"));
		
		cbx.setSelectedValue(VALORE_ENTRAMBI);
	
	}

	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
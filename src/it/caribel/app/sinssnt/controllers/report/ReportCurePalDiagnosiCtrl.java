package it.caribel.app.sinssnt.controllers.report;

import java.text.SimpleDateFormat;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;

import it.caribel.app.sinssnt.bean.MotivoSEJB;
import it.caribel.app.sinssnt.controllers.report.common.PanelUbicazioneNesDivCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.composite_components.CaribelTextbox;
import org.zkoss.util.resource.Labels;

public class ReportCurePalDiagnosiCtrl extends CaribelFormCtrl {
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
	
	
	protected CaribelCombobox metastasi;
	protected CaribelCombobox sintomo;
	protected CaribelTextbox cod_diagnosi;
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione_nesdiv");
			PanelUbicazioneNesDivCtrl c = (PanelUbicazioneNesDivCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			c.settaRaggruppamento("CA");
			dadata.setFocus(true);
			initComboMeta(metastasi);
			initComboSintomo(sintomo);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
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
	        String data_inizio= dadata.getValueForIsas();
	        String data_fine=adata.getValueForIsas();
	        String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi=res_dom.getSelectedItem().getValue();
			String formato = formatoStampa.getSelectedItem().getValue();
            String TYPE=(formato.equals("PDF")?"PDF":"application/vnd.ms-excel");
            String metodo = "query_report";
            String report = (formato.equals("PDF")?"conta_diagnosi.fo":"conta_diagnosi.html");
            String diag = cod_diagnosi.getText();
            String met ="";
			if (metastasi.getValue()!=null && !metastasi.getValue().equals(""))
				met=metastasi.getSelectedItem().getValue();
			else met="";
			
			String sint ="";
			if (sintomo.getValue()!=null && !sintomo.getValue().equals(""))
				sint=sintomo.getSelectedItem().getValue();
			else sint="";
           
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String dt1=sdf.format(dadata.getValue());
            String dt2=sdf.format(adata.getValue());
            it.pisa.caribel.util.NumberDateFormat ndf = new
                                it.pisa.caribel.util.NumberDateFormat();
            int periodovalido = ndf.IsLessThanOneYear(dt1,dt2);
            if(periodovalido==1){
            	Messagebox.show(
            			Labels.getLabel("exception.LunghezzaPeriodoException.msg"),
        				Labels.getLabel("messagebox.attention"),
        				Messagebox.OK,
        				Messagebox.INFORMATION);
             
            }else {
	        //invocazione alla servlet
	        String servlet="/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOCONTADIAGNOSI"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+ 
	                "&METHOD="+metodo+
	                "&d1="+data_inizio+"&d2="+data_fine+
	                "&diag="+diag+"&met="+met+"&sint="+sint+
	                "&zona="+zone+"&distretto="+distr+"&pca="+pca+
	                "&ragg="+ragg+"&terr="+terr+"&dom_res="+tipo_ubi+"&socsan="+socsan+
	                "&TYPE="+TYPE+"&REPORT="+report; 
	                
	        
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
            }
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initComboMeta (CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, " ", Labels.getLabel("generic.meta1"));
		CaribelComboRepository.addComboItem(cbx, "sks_linfonodi", Labels.getLabel("generic.meta2"));
		CaribelComboRepository.addComboItem(cbx, "sks_polmonare", Labels.getLabel("generic.meta3"));
		CaribelComboRepository.addComboItem(cbx, "sks_pleura", Labels.getLabel("generic.meta4"));
		CaribelComboRepository.addComboItem(cbx, "sks_peritoneo", Labels.getLabel("generic.meta5"));
		CaribelComboRepository.addComboItem(cbx, "sks_fegato", Labels.getLabel("generic.meta6"));
		CaribelComboRepository.addComboItem(cbx, "sks_rene", Labels.getLabel("generic.meta7"));
		CaribelComboRepository.addComboItem(cbx, "sks_encefalo", Labels.getLabel("generic.meta8"));
		CaribelComboRepository.addComboItem(cbx, "sks_urinari", Labels.getLabel("generic.meta9"));
		CaribelComboRepository.addComboItem(cbx, "sks_pelle", Labels.getLabel("generic.meta10"));
		CaribelComboRepository.addComboItem(cbx, "sks_ossa", Labels.getLabel("generic.meta11"));
		CaribelComboRepository.addComboItem(cbx, "sks_loc_avanzate", Labels.getLabel("generic.meta12"));
		CaribelComboRepository.addComboItem(cbx, "sks_altro", Labels.getLabel("generic.meta13"));
		cbx.setSelectedValue("");
	
	}
	
	public void initComboSintomo (CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, " ", Labels.getLabel("generic.sintomi1"));
		CaribelComboRepository.addComboItem(cbx, "sks_dolore", Labels.getLabel("generic.sintomi2"));
		CaribelComboRepository.addComboItem(cbx, "sks_vomito", Labels.getLabel("generic.sintomi3"));
		CaribelComboRepository.addComboItem(cbx, "sks_nausea", Labels.getLabel("generic.sintomi4"));
		CaribelComboRepository.addComboItem(cbx, "sks_febbre", Labels.getLabel("generic.sintomi5"));
		CaribelComboRepository.addComboItem(cbx, "sks_astenia", Labels.getLabel("generic.sintomi6"));
		CaribelComboRepository.addComboItem(cbx, "sks_anemia", Labels.getLabel("generic.sintomi7"));
		CaribelComboRepository.addComboItem(cbx, "sks_dispnea", Labels.getLabel("generic.sintomi8"));
		CaribelComboRepository.addComboItem(cbx, "sks_edemi", Labels.getLabel("generic.sintomi9"));
		CaribelComboRepository.addComboItem(cbx, "sks_micosi", Labels.getLabel("generic.sintomi10"));
		CaribelComboRepository.addComboItem(cbx, "sks_ascite", Labels.getLabel("generic.sintomi11"));
		CaribelComboRepository.addComboItem(cbx, "sks_calo", Labels.getLabel("generic.sintomi12"));
		CaribelComboRepository.addComboItem(cbx, "sks_cachessia", Labels.getLabel("generic.sintomi13"));
		CaribelComboRepository.addComboItem(cbx, "sks_stipsi", Labels.getLabel("generic.sintomi14"));
		CaribelComboRepository.addComboItem(cbx, "sks_inappetenza", Labels.getLabel("generic.sintomi15"));
		CaribelComboRepository.addComboItem(cbx, "sks_ittero", Labels.getLabel("generic.sintomi16"));
		CaribelComboRepository.addComboItem(cbx, "sks_diarrea", Labels.getLabel("generic.sintomi17"));
		CaribelComboRepository.addComboItem(cbx, "sks_disuria", Labels.getLabel("generic.sintomi18"));
		CaribelComboRepository.addComboItem(cbx, "sks_disidratazione", Labels.getLabel("generic.sintomi19"));
		CaribelComboRepository.addComboItem(cbx, "sks_insonnia", Labels.getLabel("generic.sintomi20"));
		CaribelComboRepository.addComboItem(cbx, "sks_confusione", Labels.getLabel("generic.sintomi21"));
		CaribelComboRepository.addComboItem(cbx, "sks_depressione", Labels.getLabel("generic.sintomi22"));
		CaribelComboRepository.addComboItem(cbx, "sks_lesioni_decubito", Labels.getLabel("generic.sintomi23"));
		CaribelComboRepository.addComboItem(cbx, "sks_ansia", Labels.getLabel("generic.sintomi24"));
		CaribelComboRepository.addComboItem(cbx, "sks_vertigine", Labels.getLabel("generic.sintomi25"));
		CaribelComboRepository.addComboItem(cbx, "sks_afasia", Labels.getLabel("generic.sintomi26"));
		CaribelComboRepository.addComboItem(cbx, "sks_disfagia", Labels.getLabel("generic.sintomi27"));
		CaribelComboRepository.addComboItem(cbx, "sks_altro", Labels.getLabel("generic.sintomi28"));
		
		cbx.setSelectedValue("");
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
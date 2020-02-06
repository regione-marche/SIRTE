package it.caribel.app.sinssnt.controllers.report;

import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Radio;
import it.caribel.zk.composite_components.CaribelRadiogroup;

import it.caribel.app.sinssnt.bean.TiputeSEJB;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForUI;



public class ReportInfRiepAttUtDimCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	protected CaribelRadiogroup formatoStampa;	
	protected CaribelRadiogroup modalitaStampa;
	
	protected Radio html;
	protected Radio pdf;
	protected Radio an;
	protected Radio sin;
	
	
	protected CaribelRadiogroup raggruppamento;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelRadiogroup soc_san;
	protected CaribelRadiogroup res_dom;
	
	protected Radio presidio;
	protected Radio comune;
	protected Radio area_dis;
	

	protected CaribelCombobox tipo_utente;
	protected CaribelCombobox fascia_eta;
	
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	
	protected CaribelCombobox damese;
	protected CaribelCombobox amese;
	protected CaribelIntbox anno;
	protected CaribelRadiogroup radio_assistiti;
	
	
	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			
			initCombo();
			loadDaMese(damese);
			loadAMese(amese);
			loadFasciaEta(fascia_eta);
			//anno.setText(it.pisa.caribel.util.procdate.getAnno());
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			
			String meseIni =damese.getSelectedItem().getValue();
	        String meseFine =amese.getSelectedItem().getValue();		        
	        String metodo="query_utedim";
			String raggr = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			String tipo_ubi = res_dom.getSelectedItem().getValue();
			String socsan = soc_san.getSelectedItem().getValue();
			String ass=radio_assistiti.getSelectedItem().getValue();
	        String eta="";
	        String ute=tipo_utente.getSelectedItem().getValue();
			String tp = formatoStampa.getSelectedItem().getValue();
			if (eta.equals("TUTTI"))
				eta="";
			
			if (ute.equals("TUTTO"))
				ute="";
			
			String terr="";
			
			if(zone.equals("NESDIV"))
	          {
	                  terr="0|";
	                  zone="";
	          }
	          else
	                  terr="1|";
	          if(distr.equals("NESDIV"))
	          {
	                  terr=terr+"0|";
	                  distr="";
	          }
	          else
	                  terr=terr+"1|";
	          if(raggr.equals("NESDIV"))
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
	          if(eta.equals("NESDIV"))
	          {
	                  terr=terr+"0|";
	                  eta="";
	          }
	          else
	                  terr=terr+"1|";
	          if(ass.equals("NO"))
	                  terr=terr+"0";
	          else
	                  terr=terr+"1";

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
	         String formato="1";
	         String TYPE="PDF";
	         if (html.isSelected())
	          {
	                  TYPE="application/vnd.ms-excel";
	                  formato="0";
	          }
	         String  u ="";
	         int mese_inizio = Integer.parseInt(meseIni);
	         int mese_fine = Integer.parseInt(meseFine);
	         if (mese_fine<mese_inizio)
	        	 UtilForUI.standardExclamation(Labels.getLabel("ReportInfRiepAttUtDim.incoerenzaMesi.msg"));
	         else{
	           u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
						"?EJB=SINS_FOTIPOUTEDIMIS"+
						"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
						"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
						"&METHOD=" + metodo+
						"&TYPE="+TYPE+
						"&formato=" + formato+
						"&an=" + anno.getText()+
						"&me_ini=" + meseIni + "&me_fine=" + meseFine +
						"&ragg="+raggr+"&pca="+pca+"&dom_res="+tipo_ubi+"&socsan="+socsan+
						"&distretto="+distr+"&zona="+zone+
						"&terr=" + terr +"&ute=" + ute +
						"&eta=" + eta +"&ass="+ass+
						"&REPORT=x.fo";
			
			
	         			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
	         			}
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void initCombo() {
		try {
			tipo_utente.clear();
			tipo_utente.setDisabled(false);
			Hashtable<String, Object> h = new Hashtable();
			
			CaribelComboRepository.comboPreLoad("combo_tiputest", new TiputeSEJB(), "queryCombo",h, tipo_utente, null, "codice", "descrizione", false);
			if(tipo_utente.getItemCount()>0){
				tipo_utente.setSelectedIndex(0);
			}
			
			
			
			
		}catch(Exception e){
			doShowException(e);
		}
	
	}
	
	private void loadDaMese(CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.mese1"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.mese2"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.mese3"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.mese4"));
		CaribelComboRepository.addComboItem(cbx, "05", Labels.getLabel("generic.mese5"));
		CaribelComboRepository.addComboItem(cbx, "06", Labels.getLabel("generic.mese6"));
		CaribelComboRepository.addComboItem(cbx, "07", Labels.getLabel("generic.mese7"));
		CaribelComboRepository.addComboItem(cbx, "08", Labels.getLabel("generic.mese8"));
		CaribelComboRepository.addComboItem(cbx, "09", Labels.getLabel("generic.mese9"));
		CaribelComboRepository.addComboItem(cbx, "10", Labels.getLabel("generic.mese10"));
		CaribelComboRepository.addComboItem(cbx, "11", Labels.getLabel("generic.mese11"));
		CaribelComboRepository.addComboItem(cbx, "12", Labels.getLabel("generic.mese12"));
		
		cbx.setSelectedValue("01");
	}
	
	private void loadAMese(CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.mese1"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.mese2"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.mese3"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.mese4"));
		CaribelComboRepository.addComboItem(cbx, "05", Labels.getLabel("generic.mese5"));
		CaribelComboRepository.addComboItem(cbx, "06", Labels.getLabel("generic.mese6"));
		CaribelComboRepository.addComboItem(cbx, "07", Labels.getLabel("generic.mese7"));
		CaribelComboRepository.addComboItem(cbx, "08", Labels.getLabel("generic.mese8"));
		CaribelComboRepository.addComboItem(cbx, "09", Labels.getLabel("generic.mese9"));
		CaribelComboRepository.addComboItem(cbx, "10", Labels.getLabel("generic.mese10"));
		CaribelComboRepository.addComboItem(cbx, "11", Labels.getLabel("generic.mese11"));
		CaribelComboRepository.addComboItem(cbx, "12", Labels.getLabel("generic.mese12"));
		
		cbx.setSelectedValue("01");
	}
	
	private void loadFasciaEta(CaribelCombobox cbx)throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "TUTTI", Labels.getLabel("generic.fascia_eta1"));
		CaribelComboRepository.addComboItem(cbx, "NESDIV", Labels.getLabel("generic.fascia_eta2"));
		CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("generic.fascia_eta3"));
		CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("generic.fascia_eta4"));
		CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("generic.fascia_eta5"));
		
		
		cbx.setSelectedValue("TUTTI");
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
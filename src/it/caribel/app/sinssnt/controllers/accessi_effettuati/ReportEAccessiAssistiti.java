package it.caribel.app.sinssnt.controllers.accessi_effettuati;

import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.contattoMedico.ContattoMedicoFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.DatiStampaCtrl;
import it.caribel.app.sinssnt.util.DatiStampaRichiesti;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.util.ISASUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Window;

public class ReportEAccessiAssistiti extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;

	public static final String myPathFormZul = "/web/ui/sinssnt/accessi_effettuati/reportEAccessiAssistiti.zul";
	
	protected CaribelRadiogroup formatoStampa;
	
	
	protected Radio pdf;
	protected Radio html;
	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	String keyCartella = "";
	String keyContatto = "";
	String keyTipo = "";

	private static final String ver ="3-";
	
	public void doInitForm() {
		try {
			
			keyCartella = ISASUtil.getValoreStringa((HashMap<String, String>)arg,CostantiSinssntW.N_CARTELLA);
			Object obj = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CONTATTO);
			keyContatto = (obj!=null? obj+"": "");
			keyTipo = ISASUtil.getValoreStringa((HashMap<String, String>)arg, ContattoMedicoFormCtrl.CTS_TIPO_OPERATORE );
			impostaCampoData();
			
			if ((UtilForContainer.getContainerCorr() != null)
					&& (UtilForContainer.getContainerCorr() instanceof ContainerPuacCtrl)){
				logger.trace(" NON PASSO IL TIPO DI OPERATORE");
			}else {
//				super.hParameters.put(key_tipo.getDb_name(), key_tipo.getValue().toUpperCase());
//				keyTipo = ManagerProfile.getTipoOperatore(getProfile());
				logger.trace(" NON PASSO IL TIPO OPERATORE impostato>" +keyTipo);
			}
		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	private void impostaCampoData() {
		String punto = ver  + "impostaCampoData ";
		String dtInizio = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO);
		if (!ManagerDate.validaData(dtInizio)){
			dtInizio = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO);
		}
		
		if (ManagerDate.validaData(dtInizio)){
				Date dtPeriodoInizio = ManagerDate.getDate(dtInizio);
				dadata.setConstraint("after"+ UtilForComponents.formatDateforDatebox(dtPeriodoInizio));
				String inizioInserita = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO);
				Date dtInseritaInizio = ManagerDate.getDate(inizioInserita);
				if (ManagerDate.validaData(dtInseritaInizio)){
					dadata.setValue(dtInseritaInizio);
				}
		}
		logger.trace(punto + "dtInizio>>"+dtInizio+"<");
		String dtFine = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE);
		
		if (!ManagerDate.validaData(dtFine)){
			dtFine = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE);
		}
		
		if (ManagerDate.validaData(dtFine)){
				Date dtPeriodoFine = ManagerDate.getDate(dtFine);
				adata.setConstraint("before"+ UtilForComponents.formatDateforDatebox(dtPeriodoFine));
				
				String inseritaFine = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE);
				Date dtInseritaFine = ManagerDate.getDate(inseritaFine);
				if (ManagerDate.validaData(dtInseritaFine)){
					adata.setValue(dtInseritaFine);
				}
		}
		logger.trace(punto + "dtFine>>"+dtFine+"<");
	}

	public void doStampa() {
		String punto = ver +"doStampa ";
		try {
			String data_inizio= dadata.getValueForIsas();
			String data_fine=adata.getValueForIsas();
			
            if (ManagerDate.validaData(dadata)&& ManagerDate.validaData(adata)){
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
	            	return ;
	            }
            }
            	boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();
            	String ejb = "SINS_FOELEACCASS";
    			String metodo = "query_report";
    			String report = "ele_acc_ass";
    			String codice_operatore = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);

    			String zona_operatore = CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
    			String distr_operatore = CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");

    			Hashtable<String, Object> datiFiltro = new Hashtable<String, Object>();
    			
    			datiFiltro.putAll(this.arg);
    			if (ManagerDate.validaData(data_inizio)){
    				datiFiltro.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO,data_inizio);
    			}else {
    				datiFiltro.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO,ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO));
    			}
    			
    			if (ManagerDate.validaData(data_fine)){
    				datiFiltro.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE,data_fine);
    			}else {
    				datiFiltro.put(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE));
    			}

    			datiFiltro.put(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA ,new Boolean(isSegreteriaOrganizzativa));	
    			datiFiltro.put("tipo",keyTipo);
    			datiFiltro.put(CostantiSinssntW.CTS_ID_SKSO, ISASUtil.getValoreStringa(this.arg, CostantiSinssntW.CTS_ID_SKSO));
    			datiFiltro.put(CostantiSinssntW.CTS_INT_CONTATTO,keyContatto);
    			datiFiltro.put(CostantiSinssntW.CTS_INT_CARTELLA,keyCartella);
    			
    			Hashtable<String, Object> parametri = new Hashtable<String, Object>();
    			parametri.put("codice_operatore", codice_operatore);
    			parametri.put("zona_operatore", zona_operatore);
    			parametri.put("distr_operatore", distr_operatore);
    			parametri.putAll(datiFiltro);
    			logger.trace(punto + " dati >>" + parametri + "<<");

    			String parametriStampa = recuperaParametriStampa(parametri);
    			String type = "";
    			String file = "";  
    			if (pdf.isChecked()) {
    				file = report + ".fo";
    				type = "&TYPE=PDF";
    			} else if (html.isSelected()) {
    				file = report + ".html";
    				type = "&TYPE=application/vnd.ms-excel";
    			}

    			String u = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=" + ejb + "&USER="
    					+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
    					+ CaribelSessionManager.getInstance().getMyLogin().getPassword() + "&METHOD=" + metodo + "&REPORT="
    					+ file + type + parametriStampa;

    			logger.trace(punto + " url recuperata>>\n" + u);
    			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);

            	
		}catch(Exception e){
			doShowException(e);
		}
	}

	private String recuperaParametriStampa(Hashtable<String, Object> map) {
		String parametri = "";
		for (Iterator<String> keys = map.keySet().iterator(); keys.hasNext();) {
			String key = (String) keys.next();
			if (ISASUtil.valida(key)) {
				parametri += "&" + key + "=" + ISASUtil.getValoreStringa(map, key);
			}
		}
		return parametri;
	}



	public void initCombo() {
		try {
			
		}catch(Exception e){
			doShowException(e);
		}
	
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
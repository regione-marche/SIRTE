package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.pisa.caribel.util.ISASUtil;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;

public class ReportMonitoraggioCureDomiciliariCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;

	public static String PATH_ZUL = "/web/ui/report/reportMonitoraggioCureDomiciliari.zul";
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
	//	protected CaribelTextbox codope;
	//	protected CaribelTextbox codope1;

	protected CaribelCombobox tipo_op;
	//	protected CaribelCombobox qual_op;

	protected CaribelRadiogroup tp_prest;

	protected Radio dom;
	protected Radio am;
	protected Radio en;

	private int numeroFiltriImpostati = 0;
	private int CTS_NUMERO_FILTRI_MINIMO = 1;

	private String ver = "3- ";

	public void doInitForm() {
		try {
			//			codope.setFocus(true);
			initComboTipoOp(tipo_op);
			settaDatiUbicazione();
			abilitazioneMaschera();
		} catch (Exception e) {
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

	private void settaDatiUbicazione() {
		String punto = ver + "settaDatiUbicazione ";
		Component p = self.getFellow("panel_ubicazione");
		PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
		c.doInitPanel();
		c.setVisibleRaggruppamento(false);
		c.setVisibleUbicazione(false);
		c.settaRaggr("P");
	}

	public void doStampa() {
		String punto = ver + "doStampa ";
		String messaggio = "";
		//		if ((ManagerDate.validaData(dadata)) && (ManagerDate.validaData(adata))) {
		//			int numeroGiorni = ManagerDate.getNumeroGiorniData(dadata, adata);
		//			logger.trace(punto + " data inserita numeroGiorni>" + numeroGiorni + "<<");
		//			if (numeroGiorni > 365) {
		//				logger.trace(punto + " data inserita ");
		//				messaggio = Labels.getLabel("report.elenco.assistiti.superioreAnno", new String[] {});
		//				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
		//						Messagebox.EXCLAMATION);
		//			}
		//		}
		//		if (ISASUtil.valida(messaggio)) {
		//			return;
		//		}

		try {
			String ejb = "SINS_FOREPMONICD";
			String metodo = "query_stampa";
			String report = "rep_monitoraggio_cure";
			String servlet = "";

			String parametriUrl = impostaDatiStampa();
			logger.trace(punto + " dati >>" + parametriUrl + "<<");

			if (numeroFiltriImpostati < CTS_NUMERO_FILTRI_MINIMO) {
				numeroFiltriImpostati = 0;
				logger.trace(punto + " numero minimo di filtri da impostare ");
				messaggio = Labels.getLabel("report.elenco.assistiti.numero.minimo.filtri", new String[] {});
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
						Messagebox.EXCLAMATION);
			}
			if (ISASUtil.valida(messaggio)) {

				return;
			}

			String type = "";
			String file = "";
			if (pdf.isChecked()) {
				file = report + ".fo";
				type = "&TYPE=PDF";
			} else if (html.isSelected()) {
				file = report + ".html";
				type = "&TYPE=application/vnd.ms-excel";
			}

			servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=" + ejb + "&USER="
					+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
					+ CaribelSessionManager.getInstance().getMyLogin().getPassword() + "&METHOD=" + metodo + "&REPORT="
					+ file + type + parametriUrl;

			logger.trace(punto + " url recuperata>>\n" + servlet);

			//			Executions.getCurrent().createComponents(DatiStampaCtrl.CTS_FILE_ZUL, self, dati);
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);

		} catch (Exception e) {
			doShowException(e);
		}
	}

	private String impostaDatiStampa() {
		String punto = ver + "impostaDatiStampa ";
		StringBuffer parametriStampa = new StringBuffer();
		logger.info(punto + " inizio per il recupero dei filtri ");

		String ragg = raggruppamento.getSelectedItem().getValue();
		String zone = zona.getSelectedItem().getValue();
		String distr = "";
		String pca = "";
		if (zone.equals("TUTTO"))
			zone = "";
		if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals("")
				|| distretto.getValue().equals("NODIV") || distretto.getValue().equals("NESDIV")) {
			distr = "";
		} else {
			distr = distretto.getSelectedItem().getValue();
		}

		if (presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals("")
				|| presidio_comune_area.getValue().equals("NODIV") || presidio_comune_area.getValue().equals("NESDIV")) {
			pca = "";
		} else {
			pca = presidio_comune_area.getSelectedItem().getValue();
		}

		String terr = "";

		if (zone.equals("NESDIV")) {
			terr = "0|";
			zone = "";
		} else
			terr = "1|";
		if (distretto.equals("NESDIV") || distr.equals("")) {
			terr = terr + "0|";
			distr = "";
		} else
			terr = terr + "1|";
		if (pca.equals("NESDIV") || pca.equals("")) {
			terr = terr + "0|";
			pca = "";
		} else
			terr = terr + "1";

		String socsan = soc_san.getSelectedItem().getValue();
		String tipo_ubi = res_dom.getSelectedItem().getValue();

		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_RAGGRUPPAMENTO + "=" + ragg);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_ZONE + "=" + zone);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_DISTRETTO + "=" + distr);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_PCA + "=" + pca);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_SOCSAN + "=" + socsan);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_TIPO_UBI + "=" + tipo_ubi);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_TERR + "=" + terr);
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_DATA_INIZIO + "=" + dadata.getValueForIsas());
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_DATA_FINE + "=" + adata.getValueForIsas());
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_TIPO_PRESTAZIONE + "=" + tp_prest.getSelectedValue());
		parametriStampa.append("&" + CostantiSinssntW.CTS_STP_REPORT_FIGURA_PROFESSIONALE + "=" + tipo_op.getSelectedValue());

		if (ISASUtil.valida(pca)) {
			numeroFiltriImpostati++;
		}

		if (ManagerDate.validaData(dadata)) {
			numeroFiltriImpostati++;
		}

		if (ManagerDate.validaData(adata)) {
			numeroFiltriImpostati++;
		}

		parametriStampa.append("&" + ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA + "="
				+ (UtilForContainer.isSegregeriaOrganizzativa() ? Costanti.CTS_S : Costanti.CTS_N));
		String codice_operatore = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
		String zona_operatore = CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
		String distr_operatore = CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");
		parametriStampa.append("&" + "codice_operatore" + "=" + codice_operatore);
		parametriStampa.append("&" + "zona_operatore" + "=" + zona_operatore);
		parametriStampa.append("&" + "distr_operatore" + "=" + distr_operatore);

		return parametriStampa.toString();
	}

	public void initComboTipoOp(CaribelCombobox cbx) throws Exception {
		cbx.clear();
		String punto = ver + "doPopolaCombo ";
		logger.trace(punto + " tipi operatore ");
		ManagerOperatore.loadTipiOperatori(cbx, true);
	}

	//	public void initComboQualificaOp (CaribelCombobox qual)throws Exception {
	//		qual_op.clear();
	//		qual_op.setDisabled(false);
	//		Hashtable<String, Object> h = new Hashtable();
	//		String oper =tipo_op.getSelectedValue();							
	//		h.put("tipo_oper",oper);
	//		
	//		CaribelComboRepository.comboPreLoad("combo_qual"+oper, new OperqualEJB(), "queryTutteQualifiche",h, qual_op, null, "qual_oper", "desc_qualif", false);
	//		qual_op.setSelectedValue("TUTTE");
	//	}

	//	
	//	public void onSelect$tipo_op(Event event){
	//		try{
	//			
	//			Hashtable<String, Object> h = new Hashtable();
	//			
	//			 if (tipo_op.getSelectedItem().getValue().equals("01")){				 
	//				 qual_op.clear();
	//				 initComboQualificaOp(qual_op);
	//			 }
	//	         else if (tipo_op.getSelectedItem().getValue().equals("02")){
	//	            	
	//	            	qual_op.clear();
	//	            	initComboQualificaOp(qual_op);
	//	         }
	//	         else if (tipo_op.getSelectedItem().getValue().equals("00")){	            	
	//	        	
	//	        	 qual_op.clear();
	//	        	 qual_op.setSelectedValue("");
	//	        	 qual_op.setDisabled(true);
	//	         }
	//	         else {
	//	        	 
	//	        	 qual_op.clear();
	//	        	 initComboQualificaOp(qual_op);
	//	        	
	//	         }
	//	       
	//		}catch(Exception e){
	//			doShowException(e);
	//		}
	//	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
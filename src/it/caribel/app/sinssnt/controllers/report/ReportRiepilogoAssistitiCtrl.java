package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Radio;

public class ReportRiepilogoAssistitiCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;

	protected CaribelRadiogroup formatoStampa;
	protected CaribelRadiogroup modStampa;

	protected Radio pdf;
	protected Radio html;
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

	protected CaribelDatebox dadata;
	protected CaribelDatebox adata;
	protected CaribelTextbox codope;
	protected CaribelTextbox codope1;

	protected CaribelCombobox tipo_op;

	public void doInitForm() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel();
			codope.setFocus(true);
			initComboTipoOp(tipo_op);

		} catch (Exception e) {
			doShowException(e);
		}
	}

	public void doStampa() {
		try {

			String formato = formatoStampa.getSelectedItem().getValue();
			String report_an = "ele_iass_2.fo";
			String report_si = "ele_isint_2.fo";
			String TYPE = "PDF";
			if (formato.equals("2")) {
				report_an = "ele_iass_2.html";
				report_si = "ele_isint_2.html";
				TYPE = "application/vnd.ms-excel";
			}

			String op_ini = codope.getValue();
			String op_fine = codope1.getValue();
			String figprof = tipo_op.getSelectedItem().getValue();
			String ragg = raggruppamento.getSelectedItem().getValue();
			String zone = zona.getSelectedItem().getValue();
			String distr = "";
			String pca = "";
			if (zone.equals("TUTTO"))
				zone = "";
			if (distretto.getValue().equals("TUTTI") || distretto.getValue().equals("")
					|| distretto.getValue().equals("NODIV") || distretto.getValue().equals("NESDIV"))
				distr = "";
			else
				distr = distretto.getSelectedItem().getValue();

			if (presidio_comune_area.getValue().equals("TUTTI") || presidio_comune_area.getValue().equals("")
					|| presidio_comune_area.getValue().equals("NODIV")
					|| presidio_comune_area.getValue().equals("NESDIV"))
				pca = "";
			else
				pca = presidio_comune_area.getSelectedItem().getValue();
			String socsan = soc_san.getSelectedItem().getValue();
			String tipo_ubi = res_dom.getSelectedItem().getValue();

			String data_inizio = dadata.getValueForIsas();
			String data_fine = adata.getValueForIsas();

			String servlet = "";
			if (ragg.equals("P")) {
				if (an.isSelected())
					servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=SINS_FOIASSELE2" + "&USER="
							+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
							+ CaribelSessionManager.getInstance().getMyLogin().getPassword()
							+ "&METHOD=query_inferef_2&op_ini=" + op_ini + "&op_fine=" + op_fine + "&data_inizio="
							+ data_inizio + "&data_fine=" + data_fine + "&distretto=" + distr + "&figprof=" + figprof
							+ "&ragg=" + ragg + "&pca=" + pca + "&zona=" + zone + "&REPORT=" + report_an + "&TYPE="
							+ TYPE;
				else
					servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=SINS_FOIASSELE2" + "&USER="
							+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
							+ CaribelSessionManager.getInstance().getMyLogin().getPassword()
							+ "&METHOD=query_infesint_2&op_ini=" + op_ini + "&op_fine=" + op_fine + "&data_inizio="
							+ data_inizio + "&data_fine=" + data_fine + "&distretto=" + distr + "&figprof=" + figprof
							+ "&ragg=" + ragg + "&pca=" + pca + "&zona=" + zone + "&REPORT=" + report_si + "&TYPE="
							+ TYPE;
			} else {
				if (an.isSelected())
					servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=SINS_FOIASSELE2" + "&USER="
							+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
							+ CaribelSessionManager.getInstance().getMyLogin().getPassword()
							+ "&METHOD=query_inferef_2&op_ini=" + op_ini + "&op_fine=" + op_fine + "&data_inizio="
							+ data_inizio + "&data_fine=" + data_fine + "&distretto=" + distr + "&figprof=" + figprof
							+ "&ragg=" + ragg + "&dom_res=" + tipo_ubi + "&socsan=" + socsan + "&pca=" + pca + "&zona="
							+ zone + "&REPORT=" + report_an + "&TYPE=" + TYPE;
				else
					servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=SINS_FOIASSELE2" + "&USER="
							+ CaribelSessionManager.getInstance().getMyLogin().getUser() + "&ID509="
							+ CaribelSessionManager.getInstance().getMyLogin().getPassword()
							+ "&METHOD=query_infesint_2&op_ini=" + op_ini + "&op_fine=" + op_fine + "&data_inizio="
							+ data_inizio + "&data_fine=" + data_fine + "&distretto=" + distr + "&figprof=" + figprof
							+ "&ragg=" + ragg + "&dom_res=" + tipo_ubi + "&socsan=" + socsan + "&pca=" + pca + "&zona="
							+ zone + "&REPORT=" + report_si + "&TYPE=" + TYPE;
			}

			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);

			// self.detach();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	public void initComboTipoOp(CaribelCombobox cbx) throws Exception {
		cbx.clear();
		CaribelComboRepository.addComboItem(cbx, "01", Labels.getLabel("generic.operatore2"));
		CaribelComboRepository.addComboItem(cbx, "02", Labels.getLabel("generic.operatore3"));
		CaribelComboRepository.addComboItem(cbx, "03", Labels.getLabel("generic.operatore4"));
		CaribelComboRepository.addComboItem(cbx, "04", Labels.getLabel("generic.operatore5"));

		cbx.setSelectedValue("02");

	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}

}
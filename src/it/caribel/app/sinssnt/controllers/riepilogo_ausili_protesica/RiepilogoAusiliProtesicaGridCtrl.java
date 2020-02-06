package it.caribel.app.sinssnt.controllers.riepilogo_ausili_protesica;

import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.controllers.contattoMedico.ContattoMedicoFormCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.util.ISASUtil;
import java.util.HashMap;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.Messagebox;

public class RiepilogoAusiliProtesicaGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;

	private String myKeyPermission = "INTIaaaa";
	private SkInfEJB myEJB = new SkInfEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/riepilogo_ausili_protesica/riepilogoAusiliProtesicaGrid.zul";

	private CaribelTextbox key_cartella;
	private CaribelTextbox key_contatto;
	private CaribelRadiogroup tipo;

	private String ver = "2-";

	public void doAfterCompose(Component comp) throws Exception {
		String punto = ver + "doAfterCompose ";
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		if (super.caribelSearchCtrl != null) {
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger, super.caribelSearchCtrl.getLinkedParameterForQuery(), self);
			String textToSearch = (String) arg.get("textToSearch");
			if (textToSearch != null && !textToSearch.trim().equals("")) {
				textToSearch = textToSearch.toUpperCase();
				key_cartella.setText(textToSearch);
				super.hParameters.put(key_cartella.getDb_name(), textToSearch);
				doRefresh();
			}
		} else {
			String nCartella = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CARTELLA);
			String nContatto = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CONTATTO);
			logger.debug(punto + " dati che ricevo>>" +arg);
			key_cartella.setText(nCartella);
			key_contatto.setText(nContatto);
			doCaricaGriglia();
		}
	}

	
	
	public void onRicerca(ForwardEvent e) throws Exception {
		String punto = ver + ".onRicerca ";
		logger.debug(punto + "Ricarica i dati  ");
		doCaricaGriglia();
	}

	
	private void doCaricaGriglia() {
		String punto = ver + "doCaricaGriglia ";
		String nCartella= key_cartella.getText();
		String nContatto = key_contatto.getText();
		String tipoRadio = tipo.getSelectedValue();
		super.hParameters.put(key_cartella.getDb_name(), nCartella);
		super.hParameters.put(key_contatto.getDb_name(), nContatto);
		super.hParameters.put(tipo.getDb_name(), tipoRadio);

		logger.trace(punto + " dati che devo esaminare>>"+ (super.hParameters!=null ? super.hParameters + "": " no dati "));
		doRefresh();
	}

	public void doCerca() {
		try {
			UtilForComponents.testRequiredFields(self);

			super.hParameters.put(key_cartella.getDb_name(), key_cartella.getValue().toUpperCase());
			super.hParameters.put(key_contatto.getDb_name(), key_contatto.getValue().toUpperCase());
			//			super.hParameters.put(key_tipo.getDb_name(), key_tipo.getValue().toUpperCase());
			doRefresh();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	public void doPulisciRicerca() {
		try {
			setDefault();
		} catch (Exception e) {
			logger.error(this.getClass().getName() + ": Impossibile inizializzare il reparto, rivolgersi all'assistenza");
		}
	}

	private void setDefault() throws Exception {

		if (caribellb.getItemCount() > 0) {
			caribellb.getItems().clear(); //.jCariTable1.deleteAll();
		}

		key_cartella.setValue("");
		key_contatto.setValue("");
		//		key_tipo.setValue("");
	}

	public void doNuovo() {
		Messagebox.show(Labels.getLabel("exception.NotYetImplementedException.msg"), Labels.getLabel("messagebox.attention"),
				Messagebox.OK, Messagebox.INFORMATION);

	}

	public void doApri() {
		Messagebox.show(Labels.getLabel("exception.NotYetImplementedException.msg"), Labels.getLabel("messagebox.attention"),
				Messagebox.OK, Messagebox.INFORMATION);
	}

	@Override
	protected void doStampa() {

	}
}

package it.caribel.app.sinssnt.controllers.storico_referente;

import it.caribel.app.sinssnt.bean.nuovi.SkFpgRefEJB;
import it.caribel.app.sinssnt.controllers.contattoGenerico.ContattoGenFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Listitem;

public class StoricoGenericoReferenteGridCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = ChiaviISASSinssntWeb.CONTATTO_GENERICO_REFERENTE;
	private Object myEJB = new SkFpgRefEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/storico_referente/storicoGenericoReferenteGrid.zul";
	private CaribelTextbox key_cartella;
	private CaribelTextbox key_contatto;
	private CaribelTextbox key_tipo_operatore;
	private CaribelTextbox keyCartella;
	private CaribelTextbox keyContatto;
	private CaribelTextbox keyTipoOperatore;
	private CaribelTextbox JLabelCognomeOp;
	private CaribelTextbox JLabelAssistito;
	private String ver = "11-";
	private CaribelDatebox skfpg_referente_da;

	private AbstractComponent skfpgReferente;
	Object chiamante = null;
	public static final String CTS_COGNOME_NOME_ASSISTITO = "cgn_nome_assistito";

	@Override
	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		try {
			if (arg.get("zul_chiamante") != null) {
				chiamante = arg.get("zul_chiamante");
			}

			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			JLabelCognomeOp.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
			String assistito = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CTS_COGNOME_NOME_ASSISTITO);
			JLabelAssistito.setText(assistito);

			CaribelSearchCtrl genericoReferente = (CaribelSearchCtrl) skfpgReferente.getAttribute(MY_CTRL_KEY);
			genericoReferente.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, ManagerProfile.getTipoOperatore(getProfile()));
			genericoReferente.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, ManagerProfile.getZonaOperatore(getProfile()));
			genericoReferente.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, ManagerProfile.getDistrettoOperatore(getProfile()));
			
			key_cartella.setReadonly(true);
			key_contatto.setReadonly(true);
			key_tipo_operatore.setReadonly(true);

			doInitStoricoInf();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void doInitStoricoInf() throws Exception {
			//infermiereReferenteW.setTitle(Labels.getLabel("riepilogo.storico.infermiere.referente.formTitle"));
		if (arg.get(CostantiSinssntW.N_CARTELLA) != null) {
			key_cartella.setText(ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CARTELLA));
			key_contatto.setText(ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CONTATTO));
			key_tipo_operatore.setText(ISASUtil.getValoreStringa((HashMap<String, String>) arg, "skfpg_tipo_operatore"));
			hParameters.put(key_cartella.getDb_name(), key_cartella.getValue());
			hParameters.put(key_contatto.getDb_name(), key_contatto.getValue());
			hParameters.put(key_tipo_operatore.getDb_name(), key_tipo_operatore.getValue());
			doLoadGrid();
		}
	}
	
	
	@Override
	protected void afterSetStatoInsert(){
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " inizio ");
		skfpg_referente_da.setValue(procdate.getDate());
		logger.trace(punto + " data da impostare>>");
	}

	public void onClose(Event event) {
		String punto = ver + "onClose ";
		logger.debug(punto + " event>>");
		try {

			String codice = "";
			String descrizione = "";
			Date dataDa = null;

			int ultimo = ((CaribelListModel<Object>) this.clb.getModel()).size() - 1;
			if (ultimo >= 0) {
				Object myData = ((CaribelListModel<Object>) this.clb.getModel()).get(ultimo);
				Hashtable<String, ?> data = new Hashtable<String, String>();
				if (myData instanceof ISASRecord) {
					data = ((ISASRecord) myData).getHashtable();
				} else if (myData instanceof Hashtable) {
					data = (Hashtable<String, ?>) myData;
				}
				logger.debug(punto + " dati>" + data + "<");
				//				if (storicoContattoMedico){
				codice = ISASUtil.getValoreStringa(data, "skfpg_referente");
				descrizione = ISASUtil.getValoreStringa(data, "descop");
				String dataDaVal = ISASUtil.getValoreStringa(data, "skfpg_referente_da");
				java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
				if (ISASUtil.valida(dataDaVal) && dataDaVal.length() >= 10) {
					dataDa = dateFormat.parse(dataDaVal);
				}
				((ContattoGenFormCtrl) chiamante).settaInfermiereReferente(codice, descrizione, dataDa);
			}
			logger.debug(punto + " codice>>" + codice + "<< descrizione>>" + descrizione + "<<");
			self.detach();

		} catch (Exception e) {
			doShowException(e);
		}
	}

	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		keyCartella.setText(key_cartella.getValue());
		keyContatto.setText(key_contatto.getValue());
		keyTipoOperatore.setText(key_tipo_operatore.getValue());
		if (this.currentIsasRecord != null) {
			try {
				/* per mantenere la compatibilita con la versione applet */
				this.currentIsasRecord.put("data_vecchia", ISASUtil.getValoreStringa(this.currentIsasRecord, "skfpg_referente_da"));
				this.currentIsasRecord.put("n_cartella", key_cartella.getValue());
				this.currentIsasRecord.put("n_contatto", key_contatto.getValue());
				this.currentIsasRecord.put("skfpg_tipo_operatore", key_tipo_operatore.getValue());				
			} catch (ISASMisuseException e) {
				logger.error(punto + " in salvataggio dei dati ");
			}
		}
		logger.debug(punto + " dati che invio>>" + (this.currentIsasRecord != null ? this.currentIsasRecord + "" : " no dati "));
		return true;
	}

	@Override
	protected void executeOpen() throws Exception {
		int ultimo = ((CaribelListModel<Object>) this.clb.getModel()).size() - 1;
		if (this.clb.getSelectedIndex() < ultimo) {
			UtilForUI.standardExclamation(Labels.getLabel("storico.contatto.medico.msg.modificare.ultimo"));
			return;
		}
		Listitem item = clb.getSelectedItem();
		Hashtable<String, ?> htFromGrid = (Hashtable<String, ?>) item.getAttribute("ht_from_grid");
		hParameters = (Hashtable<String, Object>) UtilForBinding.getHashtableForEJBFromHashtable(htFromGrid);
		this.currentIsasRecord = queryKeySuEJB(this.currentBean, hParameters);
		UtilForBinding.bindDataToComponent(logger, currentIsasRecord, myForm);
	}
	@Override
	protected void executeDelete() throws Exception {
		if (this.clb.getItemCount()==1)
			UtilForUI.standardExclamation(Labels.getLabel("storico.referente.ultimo.nodelete"));
		else
		super.executeDelete();
	}
	
}

package it.caribel.app.sinssnt.controllers.storico_referente;

import it.caribel.app.sinssnt.bean.modificati.FisRefEJB;
import it.caribel.app.sinssnt.controllers.contattoFisioterapico.ContattoFisioFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
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
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

public class StoricoFisioReferenteGridCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = "FISREF";
	private Object myEJB = new FisRefEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/storico_referente/storicoFisioReferenteGrid.zul";

	private CaribelTextbox key_cartella;
	private CaribelTextbox key_contatto;
	private CaribelTextbox keyCartella;
	private CaribelTextbox keyContatto;
	private CaribelTextbox JLabelCognomeOp;
	private CaribelTextbox JLabelAssistito;
	private AbstractComponent fisioReferenteSearch;
	private Window fisioReferente;
	private CaribelDatebox skf_fisiot_da;
	
	private String ver = "1-";

	Object chiamante = null;
	public static final String CTS_COGNOME_NOME_ASSISTITO = "cgn_nome_assistito";

	@Override
	protected void doInitGridForm() {
		String punto = "doInitGridForm ";
		try {
			if (arg.get(ContattoFisioFormCtrl.CTS_ZUL_CHIAMANTE) != null) {
				chiamante = arg.get(ContattoFisioFormCtrl.CTS_ZUL_CHIAMANTE);
			}
			
			CaribelSearchCtrl fisioReferente = (CaribelSearchCtrl) fisioReferenteSearch.getAttribute(MY_CTRL_KEY);
			fisioReferente.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, GestTpOp.CTS_COD_FISIOTERAPISTA);
			fisioReferente.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, ManagerProfile.getZonaOperatore(getProfile()));
			fisioReferente.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, ManagerProfile.getDistrettoOperatore(getProfile()));
			
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			JLabelCognomeOp.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
			String assistito = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CTS_COGNOME_NOME_ASSISTITO);
			JLabelAssistito.setText(assistito);

			key_cartella.setReadonly(true);
			key_contatto.setReadonly(true);
			doInitContatto();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	
	private void doInitContatto() throws Exception {
		String punto = "doInitContatto ";
		logger.debug(punto + " Setto i dati per il fisioterapista ");
		fisioReferente.setTitle(Labels.getLabel("riepilogo.storico.fisio.referente.formTitle"));

		if (arg.get(CostantiSinssntW.N_CARTELLA) != null) {
			key_cartella.setText(ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CARTELLA));
			key_contatto.setText(ISASUtil.getValoreStringa((HashMap<String, String>) arg, CostantiSinssntW.N_CONTATTO));
			hParameters.put(key_cartella.getDb_name(), key_cartella.getValue());
			hParameters.put(key_contatto.getDb_name(), key_contatto.getValue());
			doLoadGrid();
		}
	}

	
	@Override
	protected void afterSetStatoInsert(){
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " inizio ");
		skf_fisiot_da.setValue(procdate.getDate());
		logger.trace(punto + " data da impostare>>");
	}
	
	public void onClose(Event event) {
		String punto = "onClose ";
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
				logger.debug(punto + " dati>"+ data+"<");
				codice= ISASUtil.getValoreStringa(data, "skf_fisiot");
				descrizione= ISASUtil.getValoreStringa(data, "descop");  
				String dataDaVal = ISASUtil.getValoreStringa(data, "skf_fisiot_da");
				java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
				if (ISASUtil.valida(dataDaVal)&& dataDaVal.length()>=10){
					dataDa = dateFormat.parse(dataDaVal);
				}
				((ContattoFisioFormCtrl) chiamante).settaFisioReferente(codice, descrizione, dataDa);
			}
			logger.debug(punto + " codice>>"+codice + "<< descrizione>>"+ descrizione+"<<");
			self.detach();

		} catch (Exception e) {
			doShowException(e);
		}
	}

	@Override
	protected boolean doValidateForm() {
		String punto = "doValidateForm ";
		keyCartella.setText(key_cartella.getValue());
		keyContatto.setText(key_contatto.getValue());
		if (this.currentIsasRecord != null) {
			try {
				/* per mantenere la compatibilita con la versione applet */
				this.currentIsasRecord.put("data_vecchia", ISASUtil.getValoreStringa(this.currentIsasRecord, "skf_fisiot_da"));
				this.currentIsasRecord.put("n_cartella", key_cartella.getValue());
				this.currentIsasRecord.put("n_contatto", key_contatto.getValue());
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

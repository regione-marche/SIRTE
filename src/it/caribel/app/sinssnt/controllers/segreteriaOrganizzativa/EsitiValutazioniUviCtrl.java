package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.nuovi.EsitiValutazioniUviEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

public class EsitiValutazioniUviCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = ChiaviISASSinssntWeb.ESITO_VALUTAZIONI_UVI;
	private EsitiValutazioniUviEJB myEJB = new EsitiValutazioniUviEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/esitiValutazioniUvi.zul";
	Object objNCartella = "";
	Object objIdSkSo = "";
	private CaribelIntbox keyCartella;
	private CaribelRadiogroup esito_valutazione;
	private CaribelIntbox keyIdSkSo;
	private CaribelIntbox keyIdEsitoVisita;
	private static String ver = "14-";
	private CaribelCombobox cbx_pr_revisione;
	private CaribelDatebox dt_prossima_valutazione;
	private CaribelDatebox dt_valutato;
	private CaribelDatebox dt_precedente_valutazione;
	private Label lbx_prossima_valutazione;
	private Label lbx_dtPrecedenteValutazione;
	private boolean effettuaCambioPiano = false;

	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		logger.trace(punto + " inizio con dati ");
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			super.setMethodNameForInsert("insert");
			super.setMethodNameForUpdate("update");
			super.setMethodNameForQueryKey("queryKey");
			super.setMethodNameForDelete("delete");

			esito_valutazione.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					settaDatiValutazione(true);
					return;
				}
			});

			doPopulateCombobox();
			leggiDatiContainer();
			doLoadGrid();
			abilitaMaschera();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	protected void settaDatiValutazione(boolean resettaDati) throws Exception {
		String punto = ver + this.getClass().getName() + ".settaDatiValutazione ";
		logger.debug(punto + "inizio ");
		String esitiValutazione = esito_valutazione.getSelectedItem().getValue();
		logger.trace(punto + " esitoValutazione>>" + esitiValutazione);
		if (ISASUtil.valida(esitiValutazione)) {
			if (esitiValutazione.equals(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_CAMBIA_PIANO)) {
				dt_prossima_valutazione.setDisabled(true);
				cbx_pr_revisione.setDisabled(true);
				dt_prossima_valutazione.setValue(null);
				if (resettaDati) {
					cbx_pr_revisione.setSelectedIndex(-1);
					cbx_pr_revisione.setReadonly(false);
					Events.sendEvent(Events.ON_CHANGE, cbx_pr_revisione, null);
				}
			}
			if (esitiValutazione.equals(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_CONFERMA)) {
				dt_prossima_valutazione.setDisabled(false);
				cbx_pr_revisione.setDisabled(false);
				cbx_pr_revisione.setRequired(true);
			}
		}
	}

	public void onChangeDataRevisione() {
		String punto = ver + "onChangeDataRevisione ";
		logger.debug(punto + "inizio ");
		SegreteriaOrganizzativaFormCtrl.calcolaNuovaRevisione(dt_prossima_valutazione, cbx_pr_revisione,
				dt_precedente_valutazione);
	}

	private void leggiDatiContainer() throws Exception {
		CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
		if (containerCorr != null) {
			objNCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA);
			objIdSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
		} else {
			throw new Exception("Reperimento chiavi Proroghe non riuscito!");
		}
		if (objNCartella != null) {
			keyCartella.setValue(Integer.parseInt(objNCartella + ""));
		}
		if (objIdSkSo != null && ISASUtil.valida(objIdSkSo + "")) {
			keyIdSkSo.setValue(Integer.parseInt(objIdSkSo + ""));
		}
	}

	private void abilitaMaschera() throws Exception {
		String punto = ver + "";
		logger.debug(punto + " dati ");
		settaEffettuaCambioPiano(false);
		settaDatiValutazione(false);
	}

	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + " carico combo ");
		SegreteriaOrganizzativaFormCtrl.loadComboRevisione(cbx_pr_revisione);
	}

	@Override
	protected void doLoadGrid() throws Exception {
		try {
			if ((keyCartella.getValue() != null) && (keyCartella.getValue() > 0) && (keyIdSkSo.getValue() != null)
					&& (keyIdSkSo.getValue() > 0)) {
				hParameters.putAll(getOtherParametersString());
				this.hParameters.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue() + "");
				this.hParameters.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue() + "");
				Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
				clb.getItems().clear();
				clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
			} else {
				logger.debug(" \n Non effettuo il caricamento della griglia ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void executeInsert() throws Exception {
		String punto = ver + "executeInsert ";
		logger.trace(punto + " effettuare cambio piano>>" + effettuaCambioPiano);
		super.executeInsert();
		if (effettuaCambioPiano) {
			gestisciCambioPiano();
		}
	}

	@Override
	protected void executeUpdate() throws Exception {
		String punto = ver + "executeUpdate ";
		logger.trace(punto + " effettuare cambio piano>>" + effettuaCambioPiano);
		super.executeUpdate();
		if (effettuaCambioPiano) {
			gestisciCambioPiano();
		}
	}

	public void gestisciCambioPiano() {
		String punto = ver + "gestisciCambioPiano ";
		logger.trace(punto + " effettuare cambio piano>>" + effettuaCambioPiano);

		String idSksoNew = ISASUtil
				.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_ESITO_VALUTAZIONE_ID_SKSO_NEW);
		if (ISASUtil.valida(idSksoNew)) {
			UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_ID_SKSO_NEW, idSksoNew);
			UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_RICARICA_SO, new Boolean(true));
			logger.trace(punto + " Utente quando chiude la finestra ricarico la nuova SO CON ID:" + idSksoNew+"<");
//			Component comp = self.getFellowIfAny("Esitivalutazione");
//			Window win = (Window)comp;
//			win.detach();
//			Window win = ((Window)getForm());
		}
	}

	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		boolean possoSalvare = true;
		possoSalvare = ManagerDate.controllaPeriodo(self, dt_precedente_valutazione, dt_prossima_valutazione,
				lbx_dtPrecedenteValutazione, lbx_prossima_valutazione);

		if (possoSalvare && !effettuaCambioPiano) {
			String esitiValutazione = esito_valutazione.getSelectedItem().getValue();
			logger.trace(punto + " esitoValutazione>>" + esitiValutazione);
			if (ISASUtil.valida(esitiValutazione)) {
				if (esitiValutazione.equals(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_CAMBIA_PIANO)) {
					logger.trace(punto + " griglia ha dei dati  ");
					String dtValutazione = ManagerDate.formattaDataIta(dt_valutato.getValueForIsas(), "/");
					String[] lables = new String[] { dtValutazione };
					String messaggio = Labels.getLabel("esiti.valutazioni.uvi.messaggio.cambio.piano", lables);
					Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
							Messagebox.QUESTION, new EventListener<Event>() {
								public void onEvent(Event event) throws Exception {
									if (Messagebox.ON_YES.equals(event.getName())) {
										logger.trace("Procedo con il cambio piano");
										settaEffettuaCambioPiano(true);
										if (isInInsert()) {
											executeInsert();
										} else {
											executeUpdate();
										}
									} else if (Messagebox.ON_NO.equals(event.getName())) {
										logger.trace("Non effettuo il cambio piano!! ");
										return;
									}
								}
							});
					return false;
				} else {
					logger.trace(punto + " Effetto una conferma ");
				}
			}
		}
		return possoSalvare;
	}

	protected void settaEffettuaCambioPiano(boolean effettuare) {
		this.effettuaCambioPiano = effettuare;
	}

	protected void afterSetStatoInsert() {
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " verifico la data ");
		try {
			leggiDatiContainer();
			dt_valutato.setValue(procdate.getDate());
			impostaDatiUltimaValutazione();
			settaDatiValutazione(false);
		} catch (Exception e) {
			logger.error(punto + " Errore date non recuperate correttamente ");
		}
	}

	protected void afterSetStatoUpdate() {
		String punto = ver + "afterSetStatoUpdate ";
		logger.trace(punto + " verifico la data ");
		try {
			boolean possoModificareDati = verificaSePossoModificareDati();
			abilitaDatiModificabili(possoModificareDati);
			settaDatiValutazione(false);
		} catch (Exception e) {
			logger.error(punto + " Errore date non recuperate correttamente ");
		}
		onChangeDataRevisione();
	}

	@Override
	protected void executeDelete() throws Exception {
		String punto = ver + "executeDelete ";
		executeOpen();
		logger.trace(punto + " verifico la data ");
		boolean procedoConRimozione = false;
		try {
			procedoConRimozione = verificaSePossoModificareDati();
		} catch (Exception e) {
			logger.error(punto + " Errore date non recuperate correttamente ");
		}

		if (!procedoConRimozione) {
			logger.trace(punto + " Non posso rimuovere i dati della valutazione: non è l'ultima ");
			String messaggio = Labels.getLabel("esiti.valutazioni.uvi.messaggio.no.cancellare.dati");
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
		} else {
			logger.trace(punto + " procedo con la rimozione della valutazione  ");
			super.executeDelete();
		}
	}

	private void abilitaDatiModificabili(boolean modificareDati) {
		if (!modificareDati) {
			String[] lables = new String[] {};
			String messaggio = Labels.getLabel("esiti.valutazioni.uvi.messaggio.no.modificare.dati", lables);
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.QUESTION);

		}
		esito_valutazione.setDisabled(!modificareDati);
		dt_valutato.setDisabled(!modificareDati);
		cbx_pr_revisione.setDisabled(!modificareDati);
		dt_prossima_valutazione.setDisabled(!modificareDati);
	}

	private boolean verificaSePossoModificareDati() {
		String punto = ver + "verificaSePossoModificareDati ";
		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		dati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue());
		dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue());
		int idEsitoVisita = keyIdEsitoVisita.getValue();
		dati.put(CostantiSinssntW.CTS_ID_ESITO_VALUTAZIONE, idEsitoVisita);
		boolean possoModificareDati = true;
		try {
			if (idEsitoVisita > 0) {
				possoModificareDati = !(myEJB.esistoSchedeSuccessive(CaribelSessionManager.getInstance().getMyLogin(),
						dati));
			}
			logger.trace(punto + " idEsitoVisita>>" + idEsitoVisita + "<<");
		} catch (Exception e) {
			logger.error(punto + " Esistono degli errore nel verificare se ci sono schede successive.");
			e.printStackTrace();
		}
		logger.trace(punto + " possoModificareDati>>" + possoModificareDati + "<");
		return possoModificareDati;

	}

	private void impostaDatiUltimaValutazione() {
		String punto = ver + "impostaDatiUltimaValutazione ";

		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		dati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue());
		dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue());
		try {
			Hashtable<String, String> datiValutazioneUvi = myEJB.recuperaDatiUltimaValutazioneUvi(CaribelSessionManager
					.getInstance().getMyLogin(), dati);
			logger.trace(punto + " dati ricevuti >>" + datiValutazioneUvi);
			String dtUltimaValutazione = ISASUtil.getValoreStringa(datiValutazioneUvi,
					CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE);
			String sceltaRevisione = ISASUtil.getValoreStringa(datiValutazioneUvi, CostantiSinssntW.CTS_PR_REVISIONE);
			if (ManagerDate.validaData(dtUltimaValutazione)) {
				dt_precedente_valutazione.setValue(ManagerDate.getDate(dtUltimaValutazione));
			}
			cbx_pr_revisione.setSelectedValue(sceltaRevisione);
			Events.sendEvent(Events.ON_CHANGE, cbx_pr_revisione, null);
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected void notEditable() {
		String punto = ver + "notEditable ";
		logger.trace(punto + " Inizio ");
	}

	public static Hashtable<String, String> recuperaDecodificaEsito() {
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_CONFERMA,
				Labels.getLabel("esiti.valutazioni.uvi.esito.valutazione.conferma"));
		dati.put(CostantiSinssntW.CTS_ESITO_VALUTAZIONE_CAMBIA_PIANO,
				Labels.getLabel("esiti.valutazioni.uvi.esito.valutazione.cambia.piano"));
		return dati;
	}

	public static Hashtable<String, String> recuperaDecodificaRevisione() {
		Hashtable<String, String> dati = new Hashtable<String, String>();

		CaribelCombobox cbxComboRevisione = new CaribelCombobox();
		try {
			SegreteriaOrganizzativaFormCtrl.loadComboRevisione(cbxComboRevisione);
			String key, value = "";
			for (Comboitem item : cbxComboRevisione.getItems()) {
				if (item != null) {
					key = item.getValue();
					value = item.getLabel();
					dati.put(key, value);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dati;
	}
}

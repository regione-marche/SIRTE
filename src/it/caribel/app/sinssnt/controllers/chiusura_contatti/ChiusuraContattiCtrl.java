package it.caribel.app.sinssnt.controllers.chiusura_contatti;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.modificati.ChiudiContattoEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSortPaginateCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Toolbarbutton;

public class ChiusuraContattiCtrl extends CaribelFormCtrl {

	protected CaribelListbox caribellb2;
	protected Paging pagCaribellb;
	protected Toolbarbutton btn_sort_paginate;
	protected transient CaribelSortPaginateCtrl caribelSortPaginateCtrl;
	private int _selectedIndex = -1;
	private int _firstResult = -1;
	private int _maxResult = -1;

	private static final long serialVersionUID = 1L;
	private final int UPDATE_DELETE = 2;
	private int stato = 0;
	public static final String myKeyPermission = ChiaviISASSinssntWeb.CHISURA_CONTATTI_ATTIVI;
	private ChiudiContattoEJB myEJB = new ChiudiContattoEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/chiusura_contatti/chiusuraContatti.zul";

	protected CaribelRadiogroup senza_accessi;
	protected Radio da30;
	protected Radio da60;
	protected Radio da90;
	protected Radio da120;
	protected Radio da180;
	protected Radio daoggi;
	private CaribelDatebox data_rif;
	private CaribelCombobox cbx_tipoOper;
	private CaribelTextbox cod_operatore;
	private CaribelCombobox desc_operatore;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;
	private CaribelCombobox mot_ch;
	protected CaribelRadiogroup dt_chiu;
	private CaribelDatebox alla_data;
	protected Radio ultimo;
	protected Radio alla_dt;
	private Button btn_save;
	private Vector vettSelez = null;
	private boolean daSelez = false;
	private Button btn_confermaSelezione;
	ServerUtility su = new ServerUtility();
	private String ver = "17-ChiusuraContattiCtrl ";
	private int CONTINUARE_SALVATAGGIO_BLOCCA = 0;
	private int CONTINUARE_SALVATAGGIO = 1;
	private int check_save_stepMsgAvvisoPresaCarico = CONTINUARE_SALVATAGGIO_BLOCCA;
	Vector vElementiSelezionati = new Vector();
	
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQueryPaginate("query");
			cod_operatore.setValue(ManagerProfile.getCodiceOperatore(getProfile()));
			desc_operatore.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
			c.doInitPanel(false, false);
			zona.setSelectedValue(ManagerProfile.getZonaOperatore(getProfile()));
			c.settaRaggrContatti("CA");
			Events.sendEvent(Events.ON_SELECT, distretto, null);
			distretto.setRequired(true);

			doPopolateComboBox();
			String periodo = senza_accessi.getSelectedValue();
			String data_oggi = su.getTodayDate("dd/MM/yyyy");
			DataWI dt = new DataWI(data_oggi.substring(0, 2) + data_oggi.substring(3, 5) + data_oggi.substring(6, 10));
			dt = dt.aggiungiGg(-(Integer.parseInt(periodo)));
			String dataPeriodo = dt.getString(0, "/");
			data_rif.setText(dataPeriodo);
			dt_chiu.setDisabled(true);
			mot_ch.setDisabled(true);
			alla_data.setDisabled(true);
			if (caribellb2 == null) {
				caribellb2 = (CaribelListbox) self.getFellowIfAny("datiDettagli").getFellowIfAny("caribellb2", true);
			}
			abilitazioneMaschera();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void abilitazioneMaschera() {
		String punto = ver + "abilitazioneMaschera ";
		String codTipoOperatore = ManagerProfile.getTipoOperatore(getProfile());
		logger.trace(punto + " codOperatore collegato>>" + codTipoOperatore);
		if (ISASUtil.valida(codTipoOperatore)) {
			cbx_tipoOper.setSelectedValue(codTipoOperatore);
		} else {
			cbx_tipoOper.setSelectedIndex(0);
		}
		String codZona = ManagerProfile.getZonaOperatore(getProfile());
		String codDistretto = ManagerProfile.getDistrettoOperatore(getProfile());
		String codPresidio = ManagerProfile.getPresidioOperatore(getProfile());
		if (ISASUtil.valida(codZona)) {
			zona.setSelectedValue(codZona);
			Events.sendEvent(Events.ON_SELECT, zona, null);
		} else {
			zona.setSelectedIndex(0);
		}
		if (ISASUtil.valida(codDistretto)) {
			distretto.setSelectedValue(codDistretto);
			Events.sendEvent(Events.ON_SELECT, distretto, null);
		} else {
			distretto.setSelectedIndex(0);
		}
		if (ISASUtil.valida(codPresidio)) {
			presidio_comune_area.setSelectedValue(codPresidio);
		} else {
			presidio_comune_area.setSelectedIndex(0);
		}

		cod_operatore.setValue(ManagerProfile.getCodiceOperatore(getProfile()));
		desc_operatore.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
		settaSaveConclusione(CONTINUARE_SALVATAGGIO_BLOCCA);
	}

	private void doPopolateComboBox() throws Exception {
		String punto = ver + "doPopolateComboBox ";
		String opCaricati = ManagerOperatore.loadTipiOperatori(cbx_tipoOper,
				CostantiSinssntW.TAB_VAL_TIPO_OPERATORE_CHIUSURA);
		logger.trace(punto + " chiave: " + CostantiSinssntW.TAB_VAL_TIPO_OPERATORE_CHIUSURA + "< op caricati >>"
				+ opCaricati);

		caricaComboMot();
	}

	public void doStampa() {

	}

	public void onClick$btn_open() {
		try {
			effettuaRicerca();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void effettuaRicerca() {
		super.hParameters.put(senza_accessi.getDb_name(), senza_accessi.getSelectedItem().getValue());
		if (data_rif.getValueForIsas() != null && !data_rif.getValueForIsas().equals("")) {
			super.hParameters.put(data_rif.getDb_name(), data_rif.getValueForIsas());
			super.hParameters.put(cbx_tipoOper.getDb_name(), cbx_tipoOper.getSelectedValue());
			super.hParameters.put(cod_operatore.getDb_name(), cod_operatore.getValue().toUpperCase());

			if (!zona.getSelectedItem().getValue().equals("TUTTO") && !zona.getSelectedItem().getValue().equals(""))
				super.hParameters.put(zona.getDb_name(), zona.getSelectedItem().getValue());
			if (!distretto.getSelectedItem().getValue().equals("TUTTO")
					&& !distretto.getSelectedItem().getValue().equals(""))
				super.hParameters.put(distretto.getDb_name(), distretto.getSelectedItem().getValue());
			if (presidio_comune_area.getSelectedItem() != null
					&& !presidio_comune_area.getSelectedItem().equals(""))
				super.hParameters.put(presidio_comune_area.getDb_name(), presidio_comune_area.getSelectedItem()
						.getValue());

			doRefresh();

			dt_chiu.setDisabled(false);
			mot_ch.setDisabled(false);
			alla_data.setDisabled(true);
			stato = UPDATE_DELETE;

			disabilitaCampi(true);
			settaSaveConclusione(CONTINUARE_SALVATAGGIO_BLOCCA);
		} else {
			Messagebox.show(Labels.getLabel("exception.dataObbligatoria.msg"),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);

		}
	}

	private void disabilitaCampi(boolean disabilita) {
		senza_accessi.setDisabled(disabilita);
		data_rif.setDisabled(disabilita);
		cbx_tipoOper.setDisabled(disabilita);
		cod_operatore.setDisabled(disabilita);
		desc_operatore.setDisabled(disabilita);
		zona.setDisabled(disabilita);
		distretto.setDisabled(disabilita);
		presidio_comune_area.setDisabled(disabilita);
		onCheck$dt_chiu();
	}

	public void onClick$btn_undo() {
		doPulisciRicerca();

	}

	public void doCerca() {
	}

	public void doPulisciRicerca() {
		try {
			setDefault();
			disabilitaCampi(false);
			abilitazioneMaschera();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(this.getClass().getName()
					+ ": Impossibile inizializzare l'operatore, rivolgersi all'assistenza");
		}
	}

	private void setDefault() throws Exception {

		if (caribellb2.getItemCount() > 0) {
			caribellb2.getItems().clear(); //.jCariTable1.deleteAll();
			caribellb2.setModel(new CaribelListModel<Object>(new Vector<Object>()));
		}

		caribellb2.getItems().clear();
		CaribelListModel<ISASRecord> myModel = new CaribelListModel<ISASRecord>(new Vector<ISASRecord>());
		if (caribellb2.isMultiple() && !myModel.isMultiple()) {
			myModel.setMultiple(true);
		}
		caribellb2.setModel(myModel);
		caribellb2.invalidate();
		
		da30.setSelected(true);
		String periodo = senza_accessi.getSelectedValue();
		String data_oggi = su.getTodayDate("dd/MM/yyyy");
		DataWI dt = new DataWI(data_oggi.substring(0, 2) + data_oggi.substring(3, 5) + data_oggi.substring(6, 10));
		dt = dt.aggiungiGg(-(Integer.parseInt(periodo)));
		String dataPeriodo = dt.getString(0, "/");
		data_rif.setText(dataPeriodo);

		//		cbx_tipoOper.setSelectedValue(ManagerProfile.getTipoOperatore(getProfile()));

		mot_ch.setValue("");
		ultimo.setSelected(true);
		alla_data.setValue(null);
		dt_chiu.setDisabled(true);
		mot_ch.setDisabled(true);
		alla_data.setDisabled(true);
		alla_data.setRequired(false);

		abilitazioneMaschera();

	}

	private void caricaComboMot() {

		try {
			Hashtable<String, String> mot_chiu = new Hashtable<String, String>();
			mot_chiu.put("tab_cod", CostantiSinssntW.TAB_VAL_ICHIUS);
			CaribelComboRepository.comboPreLoad("contatto_chiusura", new TabVociEJB(), "query", mot_chiu, mot_ch, null,
					"tab_val", "tab_descrizione", false);
		} catch (Exception e) {
			e.printStackTrace();
			doShowException(e);
		}
	}

	public void onCheck$senza_accessi() throws Exception {

		try {
			String periodo = senza_accessi.getSelectedValue();
			String data_oggi = su.getTodayDate("dd/MM/yyyy");
			DataWI dt = new DataWI(data_oggi.substring(0, 2) + data_oggi.substring(3, 5) + data_oggi.substring(6, 10));
			dt = dt.aggiungiGg(-(Integer.parseInt(periodo)));
			String dataPeriodo = dt.getString(0, "/");
			data_rif.setText(dataPeriodo);
			settaSaveConclusione(CONTINUARE_SALVATAGGIO_BLOCCA);
		} catch (Exception e) {
			doShowException(e);
		}
	}

	public void onCheck$dt_chiu(){
		try {
			if (ultimo.isChecked()) {
				alla_data.setDisabled(true);
				alla_data.setText("");
				alla_data.setRequired(false);
			} else {
				alla_data.setDisabled(false);
				alla_data.setRequired(true);
			}

			if (alla_dt.isChecked()) {
				alla_data.setDisabled(false);
				alla_data.setRequired(true);
			} else {
				alla_data.setDisabled(true);
				alla_data.setText("");
				alla_data.setRequired(false);
			}
		} catch (Exception e) {
			doShowException(e);
		}
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return false;
	}

	public void doRefresh() {
		if (pagCaribellb != null) {
			//Recupero la stringa per la clausola ORDER BY
			if (this.caribelSortPaginateCtrl != null) {
				String strOrderBy = "";
				strOrderBy = this.caribelSortPaginateCtrl.getStrOrderBy();
				if (strOrderBy != null && !strOrderBy.isEmpty())
					hParameters.put("ordinamento", strOrderBy);
			}
			//Resetto il paging affinche ricominci dalla prima pagina
			//if(this._firstResult==-1){
			this._firstResult = 0;
			this._maxResult = pagCaribellb.getPageSize();
			//}
			doQueryPaginate(_firstResult, _maxResult);
			if (_selectedIndex != -1 && caribellb2.getItemCount() > _selectedIndex)
				caribellb2.setSelectedIndex(_selectedIndex);
		} else {
			doQuery();
			if (_selectedIndex != -1 && caribellb2.getItemCount() > _selectedIndex)
				caribellb2.setSelectedIndex(_selectedIndex);
		}

	}

	private void doQueryPaginate(int firstResult, int maxResult) {
		try {
			int dimension = executeQueryPaginate(firstResult, maxResult);
			if (dimension == 0)
				UtilForUI.doNotificationNoRows(self);
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void doQuery() {
		try {
			int count = executeQuery();
			if (count == 0) {
				UtilForUI.doNotificationNoRows(self);
			} else
				Clients.showNotification(Labels.getLabel("grid.search.total.rows", new String[] { "" + count }),
						"info", self, "middle_center", 2500);
		} catch (Exception e) {
			e.printStackTrace();
			doShowException(e);
		}
	}

	private int executeQueryPaginate(int firstResult, int maxResult) throws Exception {
		this._firstResult = firstResult;
		this._maxResult = maxResult;

		hParameters.put("start", "" + firstResult);
		hParameters.put("stop", "" + maxResult);
		Vector<Object> vDbr = queryPaginateSuEJB(currentBean, hParameters);
		int posUltimo = vDbr.size() - 1;
		@SuppressWarnings("rawtypes")
		Hashtable ultimo = (Hashtable) vDbr.get(posUltimo);
		//	int currentRecord = ((Integer)ultimo.get("currentRecord")).intValue();
		int dimension = ((Integer) ultimo.get("dimension")).intValue();
		vDbr.remove(posUltimo);

		pagCaribellb.setTotalSize(dimension);

		caribellb2.getItems().clear();
		caribellb2.setModel(new CaribelListModel<Object>(vDbr));

		return dimension;
	}

	private int executeQuery() throws Exception {
		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean,this.hParameters);
		caribellb2.getItems().clear();
		CaribelListModel<ISASRecord> myModel = new CaribelListModel<ISASRecord>(vDbr);
		if (caribellb2.isMultiple() && !myModel.isMultiple()) {
			myModel.setMultiple(true);
		}
		caribellb2.setModel(myModel);
		caribellb2.invalidate();
		return vDbr.size();
		//		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//		caribellb2.getItems().clear();
//		caribellb2.setModel(new CaribelListModel<ISASRecord>(vDbr));
//		return vDbr.size();
	}

	public void onClick$btn_save() {
		settaSaveConclusione(CONTINUARE_SALVATAGGIO_BLOCCA);
		try {
			this.execUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void execUpdate() throws Exception {
		String punto = ver + "execUpdate ";
		UtilForComponents.testRequiredFields(self);
		
		if (caribellb2 != null && caribellb2.getModel().getSize() == 0) {
			String msg = Labels.getLabel("chiusura_contatti.msg.riga.no.selezionati");
			UtilForUI.standardExclamation(msg);
			return;
		}
		vettSelez = PreparaVettoreHash();
		if (vettSelez == null ) {
			if (check_save_stepMsgAvvisoPresaCarico != CONTINUARE_SALVATAGGIO_BLOCCA ){
				UtilForUI.standardExclamation(Labels.getLabel("chiusura_contatti.msg.riga"));
			}
			return;
		} else if (vettSelez.size() == 0)
			return;

//		btn_save.setDisabled(true);
		String dataChius = alla_data.getText();
		String tipo_operatore = cbx_tipoOper.getSelectedValue();
		String cod_oper = cod_operatore.getValue();
		int count = 0;
		if (stato == UPDATE_DELETE)
			try {
				doWriteComponentsToBean();
				Object[] par = new Object[2];
				par[0] = this.hParameters;
				par[1] = vettSelez;
				Hashtable<String, String> h = new Hashtable<String, String>();
				h.put(ChiudiContattoEJB.CTS_TIPO_OPERATORE, tipo_operatore);
				h.put("cod_oper_profile", cod_oper);

				if (mot_ch.getSelectedValue() != null && !mot_ch.getSelectedValue().equals("")) {
					h.put(ChiudiContattoEJB.CTS_MOTIVO_CHIUSURA, mot_ch.getSelectedValue());
					if (ultimo.isChecked()) {
						logger.trace(punto + " data chiusura a ultimo intervento ");
					} else {
						if (alla_data.getValue() != null && !alla_data.getValue().equals("")) {
							logger.trace(punto + " ho la data procedo con aggiornamento dati ");
							h.put("cod_oper_profile", cod_oper);
							h.put(ChiudiContattoEJB.CTS_DATA_CHIUSURA, alla_data.getValueForIsas());
						} else {
							UtilForUI.standardExclamation(Labels.getLabel("exception.FieldRequiredException.msg"));
							return;
						}
					}
					aggiornaDati(h);
				} else {
					UtilForUI.standardExclamation(Labels.getLabel("exception.FieldRequiredException.msg"));
				}

				//ISASRecord dbr = myEJB.chiudiAllCont(CaribelSessionManager.getInstance().getMyLogin(),h, vettSelez);
				//doWriteBeanToComponents();

			} catch (Exception e) {
				logger.error(punto + "Aggiornamento FALLITO", e);
				doShowException(e);
				return;
			}
		if (count == 0) {
			logger.trace(punto + " il salvataggio e' andato a buon fine .... ricarico ");
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"), "info", btn_save, "after_center", CostantiSinssntW.INT_TIME_OUT);
			effettuaRicerca();
		} else {
			logger.trace(punto + " il salvataggio NON è ANDATO A BUON FINE ");
		}
		alla_data.setText(dataChius);

	}

	private void aggiornaDati(Hashtable<String, String> h) throws Exception {
		String punto = ver + "aggiornaDati ";
		h.put(ChiudiContattoEJB.CTS_COD_OPERATORE_CHIUSURA, ManagerProfile.getCodiceOperatore(getProfile()));
		logger.trace(punto + " dati che invio>>" + h);
		this.currentIsasRecord = (ISASRecord) CaribelClass.isasInvoke(this.currentBean, "chiudiAllCont", h, vettSelez);//insertSuEJB(this.currentBean,par);
//		doFreezeForm();
//		onClick$btn_open();
		Clients.showNotification(Labels.getLabel("form.save.ok.notification"), "info", btn_save, "after_center", 2500);
//		if (ultimo.isChecked()) {
//			alla_data.setDisabled(true);
//			alla_data.setRequired(false);
//		} else {
//			alla_data.setDisabled(false);
//			alla_data.setRequired(true);
//		}
//		effettuaRicerca();
	}

	private Vector PreparaVettoreHash() {
		String punto = ver + "PreparaVettoreHash ";
		it.pisa.caribel.util.NumberDateFormat su = new it.pisa.caribel.util.NumberDateFormat();
		String messaggio = "";
		int contaRifiutati = 0;
		String dataSel = dt_chiu.getSelectedValue();
		if (check_save_stepMsgAvvisoPresaCarico == CONTINUARE_SALVATAGGIO_BLOCCA) {
			Vector vSel = new Vector();
			for (Iterator iterator = caribellb2.getSelectedItems().iterator(); iterator.hasNext();) {
				Listitem litem = (Listitem) iterator.next();
				Object itemGrid = litem.getAttribute("ht_from_grid");
				String colData = (String) ((Hashtable) itemGrid).get("data_prest");
				boolean valore = true;
				Hashtable h_sel = new Hashtable();
				String cart = ((Integer) ((Hashtable) itemGrid).get("n_cartella")).toString();
				String cont = ((Integer) ((Hashtable) itemGrid).get("n_contatto")).toString();

				String dataChiusura = "";
				if (valore) {
					//cart = (String) ((Hashtable) itemGrid).get(colCa);
					//cont = (String) ((Hashtable) itemGrid).get(colCo);
					String dataPrest = (String) ((Hashtable) itemGrid).get("data_prest");
					String dataContatto = (String) ((Hashtable) itemGrid).get("data_contatto").toString();
					/*Inserisco la data chiusura
					se hanno scelto come chiusura l'ultima data intervento controllo
					che ci sia per quelli selezionati: posso non averla perchè per quel contatto
					non sono stati inseriti interventi,Eventualmente tolgo la selezione e creo il messaggio
					*/
					String assistito = ISASUtil.getValoreStringa(((Hashtable) itemGrid), "assistito");
					if (dataSel.equals("INT")) { //hanno scelto la data intervento
						dataChiusura = dataPrest;
						if (dataChiusura.equals("")) {
//							messaggio = messaggio + "Assistito: " + (String) ((Hashtable) itemGrid).get("assistito")
//									+ " - data apertura contatto: " + dataContatto + "\n";
							
							messaggio += (ISASUtil.valida(messaggio) ? "\n" : "")
									+ Labels.getLabel("chiusura_contatti.msg.riga.assistito", new String[] { assistito,
											ManagerDate.formattaDataIta(dataContatto) });
							//(String) ((Hashtable) itemGrid).get("data_contatto")+"\n";
							valore = false;
							// modelTable.setValueAt(new Boolean(false), iterator, colS)
							contaRifiutati++;
						}
					} else {
						dataChiusura = alla_data.getText();
						if (su.dateCompare(dataChiusura, dataPrest) == 2 || !ManagerDate.confrontaDate(dataContatto, dataChiusura)) {
							messaggio += (ISASUtil.valida(messaggio) ? "\n" : "")
									+ Labels.getLabel("chiusura_contatti.msg.riga.assistito", new String[] { assistito,
											ManagerDate.formattaDataIta(dataContatto) });
							//						messaggio = messaggio + "Assistito: " + (String) ((Hashtable) itemGrid).get("assistito")
							//								+ " - data apertura contatto: " + dataContatto + "\n";
							valore = false;
							contaRifiutati++;
						}
					}
				}
				if (valore) {
					h_sel.put("n_cartella", cart);
					h_sel.put("n_contatto", cont);
					h_sel.put("data_chiusura", dataChiusura);
					String tipo_operatore = cbx_tipoOper.getSelectedValue();
					h_sel.put(ChiudiContattoEJB.CTS_TIPO_OPERATORE, tipo_operatore);

					vSel.addElement((Hashtable) h_sel);
				}
			}
			vElementiSelezionati = vSel;
		}
		
		if (vElementiSelezionati!=null && vElementiSelezionati.size()>0 && !ISASUtil.valida(messaggio)){
			logger.trace(punto + " NON CI SONO ERRORI, PROCEDO AL SALVATAGGIO ");
			return vElementiSelezionati;
		}else {
			logger.trace(punto + " Presenza di errori sento se l'utente vuole proseguire con quelli selezionati ");
			if (check_save_stepMsgAvvisoPresaCarico == CONTINUARE_SALVATAGGIO ){
				logger.trace(punto + " in presenza di erorri, si vuole proseguire  ");
				return vElementiSelezionati;
			}else {
				logger.debug(punto + " non proseguo ");
				eseguiVerifica(messaggio, contaRifiutati, dataSel);
				return null;
			}
		
		
		}
		
	}

	protected void settaSaveConclusione(int stato) {
		check_save_stepMsgAvvisoPresaCarico = stato;
	}

	private boolean eseguiVerifica(String messaggio, int contaRifiutati, String dataSel) {
		boolean attesa = false;
		if (ISASUtil.valida(messaggio)) {
			if (dataSel.equals("INT")) {
				if (contaRifiutati < 10) {
					messaggio = Labels.getLabel("chiusura_contatti.msg.riga.no.intervento", new String[] { messaggio });
					//					messaggio = " Sono stati scelti i seguenti contatti per i quali non esiste intervento\n"
					//							+ messaggio + " questi contatti sono stati deselezionati e pertanto non verranno chiusi."
					//							+ "\n VUOI CONTINUARE?";
				} else {
					messaggio = Labels.getLabel("chiusura_contatti.msg.riga.rifiutati", new String[] { contaRifiutati
							+ "" });
					//					messaggio = " Sono stati scelti " + contaRifiutati
					//							+ " contatti per i quali non esiste intervento\n"
					//							+ " questi contatti sono stati deselezionati e pertanto non verranno chiusi"
					//							+ "\n VUOI CONTINUARE?";
				}
				
				if (vElementiSelezionati!=null && vElementiSelezionati.size()>0){
					messaggio += Labels.getLabel("chiusura_contatti.msg.riga.domanda");
					Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
							Messagebox.QUESTION, new EventListener<Event>() {
								public void onEvent(Event event) throws Exception {
									if (Messagebox.ON_YES.equals(event.getName())) {
										settaSaveConclusione(CONTINUARE_SALVATAGGIO);
										execUpdate();
									}
								}
							});
				}else {
					UtilForUI.standardExclamation(messaggio);
				}
				return false;
				//int i=new it.pisa.caribel.swing2.cariYesNoDialog(null,messaggio,"Attenzione!" ).show();
//				if (i == 1) { //restituisco un il vettore che non è nullo ma  dimensione 0
//					Vector vRet = new Vector();
//					return vRet;
//				}
			} else {//caso in cui hanno scelto la data chiusura
				if (contaRifiutati < 10) {
					messaggio = Labels.getLabel("chiusura_contatti.msg.riga.prestazione.maggiore",
							new String[] { messaggio });
					//					messaggio = " Sono stati scelti i seguenti contatti per i quali \n"
					//							+ "la data ultima prestazione è maggiore della data chiusura selezionata\n" + messaggio
					//							+ " questi contatti sono stati deselezionati e pertanto non verranno chiusi."
					//							+ "\n VUOI CONTINUARE?";
				} else {
					messaggio = Labels.getLabel("chiusura_contatti.msg.riga.dt.prestazione.maggiore",
							new String[] { contaRifiutati + "" });
					//					messaggio = " Sono stati scelti " + contaRifiutati + " contatti per i quali \n"
					//							+ "la data ultima prestazione è maggiore della data chiusura selezionata\n"
					//							+ " questi contatti sono stati deselezionati e pertanto non verranno chiusi"
					//							+ "\n VUOI CONTINUARE?";
				}
//				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
//						Messagebox.QUESTION, new EventListener<Event>() {
//							public void onEvent(Event event) throws Exception {
//								if (Messagebox.ON_YES.equals(event.getName())) {
//									settaSaveConclusione(CONTINUARE_SALVATAGGIO);
//									execUpdate();
//								}
//							}
//						});
				if (vElementiSelezionati!=null && vElementiSelezionati.size()>0){
					messaggio += Labels.getLabel("chiusura_contatti.msg.riga.domanda");
					Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
							Messagebox.QUESTION, new EventListener<Event>() {
								public void onEvent(Event event) throws Exception {
									if (Messagebox.ON_YES.equals(event.getName())) {
										settaSaveConclusione(CONTINUARE_SALVATAGGIO);
										execUpdate();
									}
								}
							});
				}else {
					UtilForUI.standardExclamation(messaggio);
				}
				return false;
//				return vRet;
//				int i = UtilForUI.standardYesOrNo(messaggio, null);
//				//int i=new it.pisa.caribel.swing2.cariYesNoDialog(null,messaggio,"Attenzione!" ).show();
//				if (i == 1) { //selezionaAllCB(false);//deseleziono tutto
//								//restituisco un il vettore che non è nullo ma  dimensione 0
//					Vector vRet = new Vector();
//					return vRet;
//				}
			}
		}
//		if (vSel.size() > 0)
//			return vSel;
//		else
//			return null;
		return attesa;
	}

	public void onSelezioneAll(Event e) {
		daSelez = !daSelez;
		//selezionaAllCB(daSelez);
		btn_confermaSelezione.setLabel((daSelez ? "Deseleziona" : "Seleziona") + " tutti");
	}

	/*private void selezionaAllCB(boolean tof)
	{
		for (int k=0; k<modelTable.getRowCount(); k++) {
	        int col = modelTable.getColumnLocationByName("sel");
	        modelTable.setValueAt(new Boolean(tof), k, col);
	    }
	}*/
}

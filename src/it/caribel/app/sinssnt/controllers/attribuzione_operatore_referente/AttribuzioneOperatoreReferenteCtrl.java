package it.caribel.app.sinssnt.controllers.attribuzione_operatore_referente;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneDuplicatoCtrl;
import it.caribel.app.sinssnt.bean.modificati.AttrNuovoOperReferEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelClass;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.generic_controllers.CaribelSortPaginateCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Toolbarbutton;

public class AttribuzioneOperatoreReferenteCtrl extends CaribelFormCtrl {

	protected CaribelListbox caribellb2;
	protected Paging pagcaribellb2;
	protected Toolbarbutton btn_sort_paginate;
	protected transient CaribelSortPaginateCtrl caribelSortPaginateCtrl;
	private int _selectedIndex = -1;
	private int _firstResult = -1;
	private int _maxResult = -1;
	private static final long serialVersionUID = 1L;
	private final int UPDATE_DELETE = 2;
	private int stato = 0;
	
	public static final String myKeyPermission = ChiaviISASSinssntWeb.ATTRIBUZIONE_OPERATORE_REFERENTE;
	public static final String myPathFormZul = "/web/ui/sinssnt/attribuzione_operatore_referente/attribuzione_operatore_referente.zul";

	private CaribelCombobox cbx_tipoOperRef;
	private CaribelCombobox cbx_tipoOperNewRef;
	private CaribelTextbox cod_operatore;
	private CaribelRadiogroup tipo;
	private CaribelCombobox desc_operatore;
	private CaribelTextbox cod_operatoreRef;
	private CaribelCombobox desc_operatoreRef;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox zonaDp;
	protected CaribelCombobox distrettoDp;
	protected CaribelCombobox presidio_comune_area;
	protected CaribelCombobox presidio_comune_areaDp;
	private CaribelDatebox data_attribuzione;
	private CaribelDatebox data_riferimento;	
	private CaribelListheader pi_data_inizio;
	private CaribelListheader pi_data_fine;
	private CaribelListheader n_contatto;
	private CaribelListheader pa_data;
	private CaribelListheader data_apertura_contatto;
	private Label lbl_data_attribuzione;
	private Label lbx_note;
	private Radio ref;
	private Radio pian;
	private Radio agenda;
	private Button btn_save;
	private Vector vettSelez = null;
	private boolean daSelez = false;
	private Button btn_confermaSelezione;
	ServerUtility su = new ServerUtility();
	private String ver = "13-AttribuzioneOperatoreReferenteCtrl. ";
	private int CONTINUARE_SALVATAGGIO_BLOCCA = 0;
	private int CONTINUARE_SALVATAGGIO = 1;
	private int check_save_stepMsgAvvisoPresaCarico = CONTINUARE_SALVATAGGIO_BLOCCA;
	Vector vElementiSelezionati = new Vector();
	private AbstractComponent nuovoOperatoreReferenteSearch;
	private AbstractComponent attualeOperatoreReferenteSearch;
	
	
	private AttrNuovoOperReferEJB myEJB = new AttrNuovoOperReferEJB();
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQueryPaginate("query");
			cod_operatore.setValue(ManagerProfile.getCodiceOperatore(getProfile()));
			desc_operatore.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
			impostaDatiUbicazione();
			data_riferimento.setDisabled(true);
			pi_data_inizio.setVisible(false);
			pi_data_fine.setVisible(false);
			
			doPopolateComboBox();

			CaribelSearchCtrl attualeOpSearch = (CaribelSearchCtrl) attualeOperatoreReferenteSearch
					.getAttribute(MY_CTRL_KEY);
			attualeOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, cbx_tipoOperRef);
			attualeOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zona);
			attualeOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto);
			attualeOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_PRESIDIO, presidio_comune_area);

			CaribelSearchCtrl nuovoOpSearch = (CaribelSearchCtrl) nuovoOperatoreReferenteSearch
					.getAttribute(MY_CTRL_KEY);
			nuovoOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, cbx_tipoOperRef);
			nuovoOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zonaDp);
			nuovoOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distrettoDp);
			nuovoOpSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_PRESIDIO, presidio_comune_areaDp);

			cbx_tipoOperRef.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					allineaTipoOperatore();
					return;
				}
			});

			distretto.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
				public void onEvent(Event event) {
					onChangeDistretto();
				}
			});

			zona.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
				public void onEvent(Event event) {
					onChangeZona();
				}
			});
			//			data_attribuzione.setDisabled(true);
			if (caribellb2 == null) {
				caribellb2 = (CaribelListbox) self.getFellowIfAny("datiDettagli").getFellowIfAny("caribellb2", true);
			}
			abilitazioneMaschera();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	protected void onChangeZona() {
		String punto = ver + "onChangeZona ";
		String codZona = zona.getSelectedValue();
		logger.trace(punto + " codZona>>" + codZona);
		zonaDp.setSelectedValue(codZona);
		zonaDp.setDisabled(true);
		sbiancaOperatori(true);
	}

	private void sbiancaOperatori(boolean ancheReferente) {
		cod_operatore.setValue("");
		desc_operatore.setSelectedValue("");
		if (ancheReferente) {
			cod_operatoreRef.setValue("");
			desc_operatoreRef.setSelectedValue("");
		}
	}

	protected void onChangeDistretto() {
		String punto = ver + "onChangeDistretto ";
		String codDistretto = distretto.getSelectedValue();
		logger.trace(punto + " codDistretto>>" + codDistretto);
		distrettoDp.setSelectedValue(codDistretto);
		distrettoDp.setDisabled(true);
		Events.sendEvent(Events.ON_SELECT, distrettoDp, null);
		
		sbiancaOperatori(true);
	}

	protected void allineaTipoOperatore() {
		String punto = ver + "allineaTipoOperatore ";
		String codOpRef = cbx_tipoOperRef.getSelectedValue();
		logger.trace(punto + " codOpRef>" + codOpRef);
		cbx_tipoOperNewRef.setSelectedValue(codOpRef);
		cbx_tipoOperNewRef.setDisabled(true);
		sbiancaOperatori(true);
	}

	private void impostaDatiUbicazione() {
		String punto = ver + "impostaDatiUbicazione ";
		Component p = self.getFellow("panel_ubicazione");
		PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
		c.doInitPanel(false, false);
		String zonaOperatore = ManagerProfile.getZonaOperatore(getProfile());
		logger.trace(punto + " zona operatore >>" + zonaOperatore);
		zona.setSelectedValue(zonaOperatore);
		c.settaRaggrContatti("CA");
		Events.sendEvent(Events.ON_SELECT, distretto, null);
		distretto.setRequired(true);

		Component pDp = self.getFellow("panel_ubicazioneDp");
		PanelUbicazioneDuplicatoCtrl cDp = (PanelUbicazioneDuplicatoCtrl) pDp.getAttribute(MY_CTRL_KEY);
		cDp.doInitPanel(false, false);
		zonaDp.setSelectedValue(zonaOperatore);
		cDp.settaRaggrContatti("CA");
		Events.sendEvent(Events.ON_SELECT, distrettoDp, null);
		distrettoDp.setRequired(true);
		cDp.setPresidioComuneAreaFirst();
	}

	private void abilitazioneMaschera() {
		String punto = ver + "abilitazioneMaschera ";
		String codTipoOperatore = ManagerProfile.getTipoOperatore(getProfile());
		logger.trace(punto + " codOperatore collegato>>" + codTipoOperatore);
		if (ISASUtil.valida(codTipoOperatore)) {
			cbx_tipoOperRef.setSelectedValue(codTipoOperatore);
		} else {
			cbx_tipoOperRef.setSelectedIndex(0);
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
			distrettoDp.setSelectedValue(codDistretto);
		} else {
			distretto.setSelectedIndex(0);
			distrettoDp.setSelectedIndex(0);
		}
		Events.sendEvent(Events.ON_SELECT, distretto, null);
		Events.sendEvent(Events.ON_SELECT, distrettoDp, null);
		
		if (ISASUtil.valida(codPresidio)) {
			presidio_comune_area.setSelectedValue(codPresidio);
		} else {
			presidio_comune_area.setSelectedIndex(0);
		}

		cod_operatore.setValue(ManagerProfile.getCodiceOperatore(getProfile()));
		desc_operatore.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
		settaSaveConclusione(CONTINUARE_SALVATAGGIO_BLOCCA);
		allineaTipoOperatore();
		disabilitaCampiNuovoOperatore();
		controlloCampiNuovoReferente();
		try {
			onCheck$tipo();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doPopolateComboBox() throws Exception {
		String punto = ver + "doPopolateComboBox ";
		String opCaricati = ManagerOperatore.loadTipiOperatori(cbx_tipoOperRef,
				CostantiSinssntW.TAB_VAL_TIPO_OPERATORE_REFERENTE);
		ManagerOperatore.loadTipiOperatori(cbx_tipoOperNewRef, CostantiSinssntW.TAB_VAL_TIPO_OPERATORE_REFERENTE);
		logger.trace(punto + " chiave: " + CostantiSinssntW.TAB_VAL_TIPO_OPERATORE_CHIUSURA + "< op caricati >>"
				+ opCaricati);
	}

	public void doStampa() {

	}

	public void onClick$btn_open() {
		try {
			effettuaRicerca();
			String tipologia = tipo.getSelectedValue();
			if (tipologia.equals("0"))
				data_attribuzione.setRequired(false);
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void effettuaRicerca() throws Exception {
		UtilForComponents.testRequiredFields(self);
		super.hParameters.put(cbx_tipoOperRef.getDb_name(), cbx_tipoOperRef.getSelectedValue());
		super.hParameters.put(cod_operatore.getDb_name(), cod_operatore.getValue().toUpperCase());
		super.hParameters.put(tipo.getDb_name(), tipo.getSelectedValue());
		if (!zona.getSelectedItem().getValue().equals("TUTTO") && !zona.getSelectedItem().getValue().equals(""))
			super.hParameters.put(zona.getDb_name(), zona.getSelectedItem().getValue());
		if (!distretto.getSelectedItem().getValue().equals("TUTTO")
				&& !distretto.getSelectedItem().getValue().equals(""))
			super.hParameters.put(distretto.getDb_name(), distretto.getSelectedItem().getValue());
		if (presidio_comune_area.getSelectedItem() != null && !presidio_comune_area.getSelectedItem().equals(""))
			super.hParameters.put(presidio_comune_area.getDb_name(), presidio_comune_area.getSelectedItem().getValue());
		if (data_riferimento.getValue()!=null && !data_riferimento.getValue().equals(""))
			super.hParameters.put(data_riferimento.getDb_name(), data_riferimento.getText());
		if (data_attribuzione.getValue()!=null && !data_attribuzione.getValue().equals(""))
			super.hParameters.put(data_attribuzione.getDb_name(), data_attribuzione.getText());
		doRefresh();
		
		stato = UPDATE_DELETE;
		disabilitaCampi(true);
		settaSaveConclusione(CONTINUARE_SALVATAGGIO_BLOCCA);
	}

	private void disabilitaCampiNuovoOperatore() {
		zonaDp.setDisabled(true);
		distrettoDp.setDisabled(true);
		cbx_tipoOperNewRef.setDisabled(true); // sempre disabilitata 
	}

	private void disabilitaCampi(boolean disabilita) {
		cbx_tipoOperRef.setDisabled(disabilita);
		cbx_tipoOperNewRef.setDisabled(true); // sempre disabilitata
		cod_operatore.setDisabled(disabilita);
		desc_operatore.setDisabled(disabilita);
		zona.setDisabled(disabilita);
		distretto.setDisabled(disabilita);
		presidio_comune_area.setDisabled(disabilita);
		data_riferimento.setDisabled(disabilita);
		tipo.setDisabled(disabilita);
		disabilitaCampiNuovoOperatore();
		
		controlloCampiNuovoReferente();
	}

	private void controlloCampiNuovoReferente() {
		CaribelSearchCtrl nuovoOpSearch = (CaribelSearchCtrl) nuovoOperatoreReferenteSearch
				.getAttribute(MY_CTRL_KEY);
		if (caribellb2!=null && caribellb2.getModel().getSize()>0){
			data_attribuzione.setRequired(true);
			cod_operatoreRef.setRequired(true);
			nuovoOpSearch.setRequired(true);
		}else {
			cod_operatoreRef.setRequired(false);
			data_attribuzione.setRequired(false);
			nuovoOpSearch.setRequired(false);
		}
	}

	public void onClick$btn_undo() {
		doPulisciRicerca();
		lbl_data_attribuzione.setValue("Data attribuzione");
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
//		caribellb2.setCheckmark(true);
//		caribellb2.setMultiple(true);
		
		caribellb2.getItems().clear();
		CaribelListModel<ISASRecord> myModel = new CaribelListModel<ISASRecord>(new Vector<ISASRecord>());
		if (caribellb2.isMultiple() && !myModel.isMultiple()) {
			myModel.setMultiple(true);
		}
		caribellb2.setModel(myModel);
		caribellb2.invalidate();
		

		data_attribuzione.setValue(null);
		data_riferimento.setValue(null);
		data_riferimento.setDisabled(true);
		data_riferimento.setRequired(false);
		pi_data_inizio.setVisible(false);
		pi_data_fine.setVisible(false);
		data_apertura_contatto.setVisible(true);
		n_contatto.setVisible(true);
		lbl_data_attribuzione.setVisible(true);
		data_attribuzione.setVisible(true);
		data_attribuzione.setRequired(true);
		lbl_data_attribuzione.setValue("Data attribuzione");
		ref.setSelected(true);
		abilitazioneMaschera();

	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return false;
	}

	public void doRefresh() {
		if (pagcaribellb2 != null) {
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
			this._maxResult = pagcaribellb2.getPageSize();
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
		
		hParameters.put("start", ""+firstResult);
		hParameters.put("stop", ""+maxResult);
		Vector<Object> vDbr = queryPaginateSuEJB(currentBean, hParameters);
		int posUltimo = vDbr.size()-1;
		int dimension = 0;
		if(posUltimo!=-1){
			@SuppressWarnings("rawtypes")
			Hashtable ultimo = (Hashtable)vDbr.get(posUltimo);
			//int currentRecord = ((Integer)ultimo.get("currentRecord")).intValue();
			dimension = ((Integer)ultimo.get("dimension")).intValue();
			vDbr.remove(posUltimo);
		}

		pagcaribellb2.setTotalSize(dimension);

		caribellb2.getItems().clear();		
		CaribelListModel<Object> myModel = new CaribelListModel<Object>(vDbr);
		if (caribellb2.isMultiple() && !myModel.isMultiple()) {
			myModel.setMultiple(true);
		}
		caribellb2.setModel(myModel);
		caribellb2.invalidate();
		
		return dimension;		
	}

	private int executeQuery() throws Exception {
//		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//		caribellb2.getItems().clear();
//		caribellb2.setModel(new CaribelListModel<ISASRecord>(vDbr));
//		return vDbr.size();
		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean,this.hParameters);
		caribellb2.getItems().clear();
		CaribelListModel<ISASRecord> myModel = new CaribelListModel<ISASRecord>(vDbr);
		if (caribellb2.isMultiple() && !myModel.isMultiple()) {
			myModel.setMultiple(true);
		}
		caribellb2.setModel(myModel);
		caribellb2.invalidate();
		return vDbr.size();
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
		if (caribellb2 != null && caribellb2.getModel().getSize() == 0) {
			String msg = Labels.getLabel("attribuzione.operatore.referente.msg.riga.no.selezionati");
			UtilForUI.standardExclamation(msg);
			return;
		}
		String tipologia=tipo.getSelectedValue();
		if(!tipologia.equals("0")){
			data_attribuzione.setRequired(false);
			if (!ManagerDate.validaData(data_attribuzione)) {
				String msg = Labels.getLabel("attribuzione.operatore.referente.msg.no.data.attivazione");
				UtilForUI.standardExclamation(msg);
				return;
			}
		}
		
		String codOperNewRef = cod_operatoreRef.getValue();
		if (!ISASUtil.valida(codOperNewRef)) {
			String descOperatore = lbx_note.getValue();
			String msg = Labels.getLabel("attribuzione.operatore.referente.msg.no.operatore.attribuito", 
						new String[]{descOperatore});
			UtilForUI.standardExclamation(msg);
			return;
		}
		String codOperRef = cod_operatore.getValue();
		if (ISASUtil.valida(codOperNewRef) && ISASUtil.valida(codOperRef)  && codOperRef.equals(codOperNewRef)) {
			String msg = Labels.getLabel("attribuzione.operatore.referente.msg.stesso.operatore");
			UtilForUI.standardExclamation(msg);
			return;
		}
		
		vettSelez = PreparaVettoreHash();
		if (vettSelez == null) {
			if (check_save_stepMsgAvvisoPresaCarico != CONTINUARE_SALVATAGGIO_BLOCCA) {
				UtilForUI.standardExclamation(Labels.getLabel("attribuzione.operatore.referente.msg.riga"));
			}
			return;
		} else if (vettSelez.size() == 0)
			return;

		//		btn_save.setDisabled(true);
		//		String dataChius = data_attribuzione.getText();
		//		String tipo_operatore = cbx_tipoOperRef.getSelectedValue();
		//		String cod_oper = cod_operatore.getValue();
		int count = 0;
		if (stato == UPDATE_DELETE)
			try {
				doWriteComponentsToBean();
				Object[] par = new Object[2];
				par[0] = this.hParameters;
				par[1] = vettSelez;
				//				Hashtable<String, String> h = new Hashtable<String, String>();
				//				h.put(ChiudiContattoEJB.CTS_TIPO_OPERATORE, tipo_operatore);
				//				h.put("cod_oper_profile", cod_oper);
				if (!tipologia.equals("0")){
					if (data_attribuzione.getValue() != null && !data_attribuzione.getValue().equals("")) {
						logger.trace(punto + " ho la data procedo con aggiornamento dati ");
						//					h.put("cod_oper_profile", cod_oper);
						//					h.put(ChiudiContattoEJB.CTS_DATA_CHIUSURA, data_attribuzione.getValueForIsas());
					} else {
						UtilForUI.standardExclamation(Labels.getLabel("exception.FieldRequiredException.msg"));
						return;
					}
				}
				aggiornaDati(this.hParameters);
			} catch (Exception e) {
				logger.error(punto + "Aggiornamento FALLITO", e);
				doShowException(e);
				return;
			}
		logger.trace(punto + " count>>" + count);
		if (count == 0) {
			//			btn_save.setDisabled(false);
			logger.trace(punto + " il salvataggio e' andato a buon fine .... ricarico ");
			Clients.showNotification(Labels.getLabel("form.save.ok.notification"), "info", btn_save, "after_center",
					CostantiSinssntW.INT_TIME_OUT);

			effettuaRicerca();
			
		} else {
			logger.trace(punto + " il salvataggio e' andato a buon fine .... ricarico ");
						//			// rimuovo le righe selezionate dalla griglia
			//			for (int j = 0; j < vettSelez.size(); j++) {
			//				Hashtable h_sel = (Hashtable) vettSelez.elementAt(j);
			//				//int rigaSel = (int)modelTable.columnsContains(h_sel);
			//				int rigaSel = ((CaribelListModel) caribellb2.getModel()).columnsContains(h_sel);
			//				if (rigaSel >= 0)
			//					caribellb2.removeItemAt(rigaSel);
			//			}
		}
		//		data_attribuzione.setText(dataChius);

	}

	private void aggiornaDati(Hashtable<String, Object> h) throws Exception {
		String punto = ver + "aggiornaDati ";
		logger.trace(punto + " dati che invio>>" + h);
		String tipologia = tipo.getSelectedValue();
		if(tipologia.equals("R"))
			this.currentIsasRecord = (ISASRecord) CaribelClass
				.isasInvoke(this.currentBean, "cambiaOperatore", h, vettSelez);//insertSuEJB(this.currentBean,par);
		else if (tipologia.equals("A"))
			this.currentIsasRecord = (ISASRecord) CaribelClass
			.isasInvoke(this.currentBean, "trasferisciPianificazioneAgenda", h, vettSelez);
		else if (tipologia.equals("0"))
			this.currentIsasRecord = (ISASRecord) CaribelClass
			.isasInvoke(this.currentBean, "trasferisciPianificazione", h, vettSelez);
		
		Clients.showNotification(Labels.getLabel("form.save.ok.notification"), "info", btn_save, "after_center", 2500);
	}

	
	private Vector PreparaVettoreHash() {
		String punto = ver + "PreparaVettoreHash ";
		it.pisa.caribel.util.NumberDateFormat su = new it.pisa.caribel.util.NumberDateFormat();
		String messaggio = "";
		int contaRifiutati = 0;
		//		String dataSel = dt_chiu.getSelectedValue();
		String dataAttribuzione = data_attribuzione.getText();
		String msgAntecedente = "";
		if (check_save_stepMsgAvvisoPresaCarico == CONTINUARE_SALVATAGGIO_BLOCCA) {
			Vector vSel = new Vector();
			for (Iterator iterator = caribellb2.getSelectedItems().iterator(); iterator.hasNext();) {
				Listitem litem = (Listitem) iterator.next();
				Object itemGrid = litem.getAttribute("ht_from_grid");
				boolean valore = true;
				Hashtable h_sel = new Hashtable();
				String tipologia = tipo.getSelectedValue();
				String cart = ((Integer) ((Hashtable) itemGrid).get("n_cartella")).toString();
				String obbiettivo ="";
				String interv ="";
				String progetto ="";
				String pa_data ="";
				if (tipologia.equals("0")){
					 obbiettivo = ((String) ((Hashtable) itemGrid).get("cod_obbiettivo")).toString();
					 interv = ((Integer) ((Hashtable) itemGrid).get("n_intervento")).toString();
					 progetto = ((Integer) ((Hashtable) itemGrid).get("n_progetto")).toString();
					 pa_data = ISASUtil.getValoreStringa(((Hashtable) itemGrid), "data");
				}				
				
				String cont = "";
				if (tipologia.equals("R"))
					cont = ((Integer) ((Hashtable) itemGrid).get("n_contatto")).toString();
				// sel##n_cartella##assistito##n_contatto##data_apertura_contatto
				String assistito = ISASUtil.getValoreStringa(((Hashtable) itemGrid), "assistito");

				if (valore) {
					if (tipologia.equals("R")){
					String dataContatto = (String) ((Hashtable) itemGrid).get("data_apertura_contatto").toString();
					//					int intRetCod = su.dateCompare(dataAttribuzione, dataContatto);
					boolean dateCorrette = ManagerDate.confrontaDate(dataContatto, dataAttribuzione);
					if (dateCorrette) {
						logger.trace(punto + " periodo corretto ");
					} else {
						valore = false;

						msgAntecedente += (ISASUtil.valida(msgAntecedente) ? "\n" : "")
								+ Labels.getLabel("attribuzione.operatore.referente.msg.riga.assistito", new String[] {
										assistito, ManagerDate.formattaDataIta(dataContatto) });
						//						msgAntecedente += "La data attribuzione risulta antecedente alla data apertura contatto"
						//								+ "\ndella riga: N. Cartella: " + cart + " N. Contatto: " + cont;
						logger.trace(punto + " msg>>" + msgAntecedente);
						//						new cariInfoDialog(null, msgAntecedente, "Attenzione!").show();
						//						return null;
						//					} else {
						//						msgControlloDate += "Errore inatteso nel controllo date" + " della riga: N. Cartella: " + cart
						//								+ " N. Contatto: " + cont;
						//						logger.trace(punto + " msg>>" + msgControlloDate);
						//						//						new cariInfoDialog(null, msg, "Attenzione!").show();
						//						//						return null;
					}
					}

					if (valore) {
						h_sel.put("n_cartella", cart);
						h_sel.put("cod_obbiettivo", obbiettivo);
						h_sel.put("n_intervento", interv);
						h_sel.put("n_progetto", progetto);
						h_sel.put("data", pa_data);
						if (tipologia.equals("R"))
							h_sel.put("n_contatto", cont);
						if (tipologia.equals("0"))
							data_attribuzione.setText(data_riferimento.getText());
						if (!tipologia.equals("R")){
							h_sel.put("data_inizio", data_riferimento.getText());
							if (data_attribuzione.getText()!=null && !data_attribuzione.getText().equals(""))
								h_sel.put("data_fine", data_attribuzione.getText());
							else 
								h_sel.put("data_fine", "");
						}
						
						logger.trace(punto + " dati che aggiungo>>" + h_sel);
						vSel.addElement((Hashtable) h_sel);
					} else {
						logger.trace(punto + " NON AGGIUNGO IL RECORD ");
					}
				}
			}
			vElementiSelezionati = vSel;
		}

		if (vElementiSelezionati != null && vElementiSelezionati.size() > 0 && !ISASUtil.valida(msgAntecedente)) {
			logger.trace(punto + " NON CI SONO ERRORI, PROCEDO AL SALVATAGGIO ");
			return vElementiSelezionati;
		} else {
			logger.trace(punto + " Presenza di errori sento se l'utente vuole proseguire con quelli selezionati ");
			if (check_save_stepMsgAvvisoPresaCarico == CONTINUARE_SALVATAGGIO) {
				logger.trace(punto + " in presenza di erorri, si vuole proseguire  ");
				return vElementiSelezionati;
			} else {
				logger.debug(punto + " non proseguo ");
				eseguiVerifica(msgAntecedente, contaRifiutati, dataAttribuzione);
				return null;
			}
		}
	}

	protected void settaSaveConclusione(int stato) {
		check_save_stepMsgAvvisoPresaCarico = stato;
	}

	private boolean eseguiVerifica(String msgAntecedente, int contaRifiutati, String dataSel) {
		boolean attesa = false;
		String messaggio = "";
		String tipologia = tipo.getSelectedValue();
		if(tipologia.equals("R")){
		if (ISASUtil.valida(msgAntecedente)) {
			if (ISASUtil.valida(msgAntecedente)) {
				messaggio = Labels.getLabel("attribuzione.operatore.referente.msg.errori.data.antecendente",
						new String[] {dataSel, msgAntecedente });
				//				if (ISASUtil.valida(msgControlloDate)) {
				//					messaggio += Labels.getLabel("attribuzione.operatore.referente.msg.errori.data.errata",
				//							new String[] { contaRifiutati + "" });
				//				}
			}
			if (vElementiSelezionati!=null && vElementiSelezionati.size()>0){
				messaggio += Labels.getLabel("attribuzione.operatore.referente.msg.riga.domanda");
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
			
			
//			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
//					Messagebox.QUESTION, new EventListener<Event>() {
//						public void onEvent(Event event) throws Exception {
//							if (Messagebox.ON_YES.equals(event.getName())) {
//								settaSaveConclusione(CONTINUARE_SALVATAGGIO);
//								execUpdate();
//							}
//						}
//					});
			return false;
		}
	}
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
	
	public void onCheck$tipo() throws Exception {
		String punto = ver + "onCheck$tipo ";
		try {
			String tipologia = tipo.getSelectedValue();
			lbx_note.setValue("");
			if (tipologia.equals("R")){
				data_riferimento.setDisabled(true);
				data_riferimento.setValue(null);
				data_riferimento.setRequired(false);
				pi_data_inizio.setVisible(false);
				pi_data_fine.setVisible(false);
				data_apertura_contatto.setVisible(true);
				n_contatto.setVisible(true);
				lbl_data_attribuzione.setVisible(true);
				data_attribuzione.setVisible(true);
				lbl_data_attribuzione.setValue(Labels.getLabel("attribuzione.operatore.referente.data.attribuzione"));
				data_attribuzione.setRequired(true);
				caribellb2.getItems().clear();
				lbx_note.setValue(Labels.getLabel("attribuzione.operatore.referente.note.referente"));
			}else if (tipologia.equals("A")){
				data_riferimento.setDisabled(false);
				data_riferimento.setRequired(true);
				pi_data_inizio.setVisible(false);
				pi_data_fine.setVisible(false);
				data_apertura_contatto.setVisible(false);
				n_contatto.setVisible(false);
				lbl_data_attribuzione.setVisible(true);
				lbl_data_attribuzione.setValue(Labels.getLabel("attribuzione.operatore.referente.data.attribuzione.fine"));
				data_attribuzione.setVisible(true);
				data_attribuzione.setRequired(true);
				caribellb2.getItems().clear();
				lbx_note.setValue(Labels.getLabel("attribuzione.operatore.referente.note.agenda"));
			}else if (tipologia.equals("0")){
				data_riferimento.setDisabled(false);
				data_riferimento.setRequired(true);
				pi_data_inizio.setVisible(true);
				lbl_data_attribuzione.setVisible(false);
				data_attribuzione.setVisible(false);
				pi_data_fine.setVisible(true);
				data_apertura_contatto.setVisible(false);
				n_contatto.setVisible(false);				
				caribellb2.getItems().clear();
				data_attribuzione.setRequired(false);
				lbx_note.setValue(Labels.getLabel("attribuzione.operatore.referente.note.pianificato"));
			}
		} catch (Exception e) {
			doShowException(e);
		}
	}
}

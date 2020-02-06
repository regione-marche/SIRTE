package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.ejb.DistrettiEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

public class SOOperatoreCoinvoltiCtrl extends SOOperatoreCoinvoltiBaseCtrl{

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = SegreteriaOrganizzativaFormCtrl.myKeyPermission;
	private RMSkSOOpCoinvoltiEJB myEJB = new RMSkSOOpCoinvoltiEJB();
//	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/schedaSO_operatori_coinvolti.zul";
//	Object objNCartella = "";
//	Object objIdSkSo = "";
//	private AbstractComponent cs_ski_infermiere;
//	
//	private CaribelListbox griglia_op_coinvolti;
//	
//	private CaribelIntbox keyCartella;
//	private CaribelIntbox keyIdSkSo;
//	
//	CaribelTextbox cod_operatore;
//	CaribelCombobox cod_operatore_descr;
//	
//	private String ver = "41-";
//	private CaribelDatebox dt_inizio_piano;
//	private CaribelDatebox dt_fine_piano;
//	private CaribelDatebox dt_presa_carico;
//	private CaribelDatebox dt_chiusura;
//	private CaribelCombobox cbx_tipo_operatore;
//	private CaribelCombobox cbx_cod_distretto;
//	private Label presidio_comune_areadis;
////	private CaribelIntbox num_acces_set;
//	protected CaribelRadiogroup raggruppamento;
//	protected CaribelCombobox zona;
//	protected CaribelCombobox distretto;
//	protected CaribelCombobox presidio_comune_area;
//	protected CaribelRadiogroup soc_san;
//	protected CaribelRadiogroup res_dom;
//	
//	private CaribelTextbox op_operatore_cognome;
//	private CaribelTextbox op_operatore_nome;
//	
//	boolean controlloMmgOperatorePrimaVisita = false;
//	
	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			super.setMethodNameForInsert("insert");
			super.setMethodNameForUpdate("update");
			super.setMethodNameForQueryKey("queryKey");
			super.setMethodNameForDelete("delete");

			doPopulateCombobox();
			doCaricaComboDistretti();
			
			CaribelSearchCtrl medicoSearch = (CaribelSearchCtrl) cs_ski_infermiere.getAttribute(MY_CTRL_KEY);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, cbx_tipo_operatore);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zona);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_PRESIDIO, presidio_comune_area);
			
			distretto.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
					onChangeDistretto();
				}
			});	
			
			if(griglia_op_coinvolti== null){
				griglia_op_coinvolti = (CaribelListbox) self.getFellowIfAny("griglia_op_coinvolti", true);
			}
			
			presidio_comune_area.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
					onChangeTipoOperatore();
				}
			});
			
			cod_operatore.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>(){
				public void onEvent(Event event){
					abilitaScritturaOperatoreCognomeNomeReferente();
				}
				});
			
			op_operatore_cognome.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					abilitaScritturaOperatoreCognomeNomeReferente();
					return;
				}});
			
			op_operatore_nome.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
					abilitaScritturaOperatoreCognomeNomeReferente();
				}
				});
			
			if (arg.get("n_cartella") == null) {
				CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
				if (containerCorr !=null) {
					objNCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA);
					objIdSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
				} else {
					//throw new Exception("Reperimento chiavi Diagnosi non riuscito!");
				}
				if (objNCartella!=null){
					keyCartella.setValue(Integer.parseInt(objNCartella+""));
				}
				if (objIdSkSo!=null && ISASUtil.valida(objIdSkSo+"")){
					keyIdSkSo.setValue(Integer.parseInt(objIdSkSo+""));
				}
				doLoadGrid();
			} else {

			}
			abilitaMaschera();
		} catch (Exception e) {
			doShowException(e);
		}
	}
	
	private void abilitaScritturaOperatoreCognomeNomeReferente() {
		String punto = ver + "abilitaScritturaOperatoreCognomeNomeReferente ";
		logger.trace(punto + " ");
		String codOperatore= "";
		
		CaribelSearchCtrl operatoreSearch = (CaribelSearchCtrl) cs_ski_infermiere.getAttribute(MY_CTRL_KEY);
		boolean readOnlyOperatore = false;
		try {
			codOperatore = cod_operatore.getText();
		} catch (Exception e2) {
		}
		
		boolean abilitareScritturaOperatoreReferente = ISASUtil.valida(codOperatore);
		try {
			if (abilitareScritturaOperatoreReferente) {
				logger.trace(punto + " abilitareScritturaOperatoreReferente>>" + abilitareScritturaOperatoreReferente);
				op_operatore_cognome.setReadonly(abilitareScritturaOperatoreReferente);
				op_operatore_nome.setReadonly(abilitareScritturaOperatoreReferente);
				if (abilitareScritturaOperatoreReferente) {
					op_operatore_cognome.setValue("");
					op_operatore_nome.setValue("");
				}
			} else {
				if (ISASUtil.valida(op_operatore_cognome.getValue()) && ISASUtil.valida(op_operatore_nome.getValue())){ 
					readOnlyOperatore = true;
				}
			}
		} catch (Exception e) {
		}
		logger.trace(punto + "readOnlyOperatore>"+readOnlyOperatore+"<\nabilitareScritturaOperatoreReferente>"+abilitareScritturaOperatoreReferente+"<\n");
		operatoreSearch.setReadonly(readOnlyOperatore);
		op_operatore_cognome.setReadonly(abilitareScritturaOperatoreReferente);
		op_operatore_nome.setReadonly(abilitareScritturaOperatoreReferente);
	}
	
	private void onChangeDistretto() {
		Component p = self.getFellow("panel_ubicazione");
		PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
		c.setPresidioComuneAreaFirst();
		onChangeTipoOperatore();
	}
	
	
//	@Override
//	protected void executeOpen()throws Exception{
//		String punto = ver + "executeOpen ";
//		String tipoOperatore = ISASUtil.getValoreStringa(this.currentIsasRecord, "tipo_operatore");
//		boolean procedoConModifica = true;
//		if (ISASUtil.valida(tipoOperatore)&& tipoOperatore.equals(GestTpOp.CTS_COD_MMG)){
//			procedoConModifica = !verificaPresenzaAutorizzazioni(); 
//		}
//		if(procedoConModifica){
//			logger.trace(punto + " procedo con la modifica dell'operatore ");
//			super.executeOpen();
//		}else {
//			logger.trace(punto + " NON POSSO MODIFICARE OPERATORE: IN QUANTO ESISTONO AUTORIZZAZIONI INSERITE ");
//			Messagebox.show(Labels.getLabel("segreteria.organizzativa.scheda.opc.msg.no.update"),
//					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
//		}
//	}
//	
	
	@Override
	protected void executeDelete()throws Exception{
		String punto = ver + "executeDelete ";
		executeOpen();
		String tipoOperatore = ISASUtil.getValoreStringa(this.currentIsasRecord, "tipo_operatore");
		boolean procedoConRimozione = true;
		String dtAttivazione = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.DT_PRESA_CARICO);
		
		if (ManagerDate.validaData(dtAttivazione)){
			procedoConRimozione = false;
			String messaggio = Labels.getLabel("so.scheda.opc.msg.no.data.attivazione.presente.delete");
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
		}else {
			if (ISASUtil.valida(tipoOperatore)&& tipoOperatore.equals(GestTpOp.CTS_COD_MMG)){
				procedoConRimozione = !verificaPresenzaAutorizzazioni();
				String messaggio = "";
				if (procedoConRimozione){
					logger.trace(punto + " NON si RIMUOVE L'OPERATORE  ");
					messaggio = Labels.getLabel("segreteria.organizzativa.scheda.opc.msg.no.delete.mmg");
				}else {
					logger.trace(punto + " NON RIMUOVO L'OPERATORE: ESISTONO AUTORIZZAZIONI ");
					messaggio = Labels.getLabel("segreteria.organizzativa.scheda.opc.msg.no.delete");
				}
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
				return;
			}
		}
		
		if(procedoConRimozione){
			logger.trace(punto + " procedo con la rimozione dell'operatore ");
			super.executeDelete();
		}
	}
	
//	@Override
//	protected void executeUpdate()throws Exception{
//		String punto = ver + "executeDelete ";
//		logger.debug(punto + " verifica dati ");
//		
//		if(!tipoOperatorePresente()){
//			super.executeUpdate();
//		}
//	}
//	
//	@Override
//	protected void executeInsert()throws Exception{
//		String punto = ver + "executeInsert ";
//		logger.debug(punto + " verifica dati ");
//		
//		if(!tipoOperatorePresente()){
//			super.executeUpdate();
//		}
//	}
	
	private boolean tipoOperatorePresente() {
		String punto = ver + "tipoOperatorePresente ";
		boolean trovato = false;
		String codFigura = cbx_tipo_operatore.getSelectedValue();
		
		int rigaSelezionata = griglia_op_coinvolti.getSelectedIndex();;
		boolean statoUpdate = (this.stato_corr ==CaribelGridStateCtrl.STATO_UPDATE);
		
		Vector<Hashtable<String, String>> vettoreObiettivi = UtilForBinding.getDataFromGrid(griglia_op_coinvolti);
		if (ISASUtil.valida(codFigura) && vettoreObiettivi!=null && vettoreObiettivi.size()>0){
				codFigura = codFigura.trim();
				int i = 0;
				
				while (i< vettoreObiettivi.size() && !trovato) {
					Hashtable<String, String> dbrOpCoinv = (Hashtable<String, String>) vettoreObiettivi.get(i);
					String tipoOp = ISASUtil.getValoreStringa(dbrOpCoinv, "tipo_operatore");
					tipoOp = tipoOp.trim();
					logger.trace(punto + " tipoOp>>"+tipoOp+"<codFigura>"+codFigura+"<");
					if(ISASUtil.valida(tipoOp) && tipoOp.equals(codFigura) && !(statoUpdate && rigaSelezionata==i)){
						trovato = true;
					}
					i++;
				}
			}
		if (trovato){
			logger.debug(punto + " operatore già presente ");
			String operatore = cbx_tipo_operatore.getValue();
			String messaggio = Labels.getLabel("so.scheda.opc.msg.no.operatore.gia.presente", new String[]{operatore});
			
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
		}
		
		return trovato;
	}

	private boolean verificaPresenzaAutorizzazioni() {
		String punto = ver + "verificaPresenzaAutorizzazioni ";
		String nCartella = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.N_CARTELLA);
		String idSkso= ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_ID_SKSO);
		String prDataPuac = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_PR_DATA_PUAC)+"";
		String prDataChiusura = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.PR_DATA_CHIUSURA) +"";
		
		Hashtable<String, String> prtDati = new Hashtable<String, String>();
		prtDati.put(CostantiSinssntW.N_CARTELLA,nCartella);
		prtDati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
		prtDati.put(CostantiSinssntW.CTS_PR_DATA_PUAC, prDataPuac);
		prtDati.put(CostantiSinssntW.PR_DATA_CHIUSURA, prDataChiusura);
		
		logger.debug(punto + " dati che invio>>" +prtDati +"<");
		
		boolean autorizzazioniPresenti = false;
		try {
			autorizzazioniPresenti = myEJB.verificaPresenzeAutorizzazioni(CaribelSessionManager.getInstance().getMyLogin(), prtDati);
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.debug(punto + " esito>>"+ autorizzazioniPresenti+"<");
		
		return autorizzazioniPresenti;
	}

	@Override
	protected void afterSetStatoUpdate() {
		String punto = ver + "afterSetStatoUpdate ";
		boolean readOnlyOperatore = false;
		
		impostaDistretto(false);
		
		String dtAttivazione = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.DT_PRESA_CARICO);
		if(ManagerDate.validaData(dtAttivazione)){
			String messaggio = Labels.getLabel("so.scheda.opc.msg.no.data.attivazione.presente.modi");
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
			UtilForBinding.setComponentReadOnly(myForm, true);
			UtilForComponents.disableListBox(clb,false);
			super.btn_formgrid_update.setVisible(false);
		}else {
			CaribelSearchCtrl operatoreSearch = (CaribelSearchCtrl) cs_ski_infermiere.getAttribute(MY_CTRL_KEY);
			if(cbx_tipo_operatore.getSelectedValue().equals(GestTpOp.CTS_COD_MMG)){
				readOnlyOperatore= true;
			}
			logger.trace(punto + " readOnly>>"+readOnlyOperatore+"<");
			operatoreSearch.setReadonly(readOnlyOperatore);
			gestisciMMG();
//			gestisciReferenteOperatore(readOnlyOperatore);
			abilitaScritturaOperatoreCognomeNomeReferente();
		}
		
	}
	
	private void impostaDistretto(boolean datiOperatore) {
		String punto = ver + "impostaDistretto ";
		if (distretto !=null){
//			distretto.setSelectedIndex(0);   
			String codDistretto = "";
			String codSede= "";
			if (datiOperatore){
				codDistretto =  ManagerProfile.getDistrettoOperatore(getProfile());
				codSede= ManagerProfile.getPresidioOperatore(getProfile());
			}else {
				codDistretto = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_SO_DB_NAME_DISTRETTO);
				codSede= ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_SO_DB_NAME_PRESIDIO);
				
			}
			logger.debug(punto + " datiOperatore>"+ datiOperatore+"< codDistretto>>" +codDistretto+"< codSede>"+ codSede+"<");
			if (ISASUtil.valida(codDistretto)){
				distretto.setSelectedValue(codDistretto);
			}else {
				distretto.setSelectedIndex(0);
			}
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			Events.sendEvent(Events.ON_SELECT, this.distretto, null);
			
			if(ISASUtil.valida(codSede)){
				c.setPresidioComuneArea(codSede);
			}
		}
	}

	private void gestisciReferenteOperatore_(boolean readOnlyOperatore) {
		String punto= ver + "gestisciReferenteOperatore ";
		boolean readOnly = false;
		
		if(cod_operatore!=null && ISASUtil.valida(cod_operatore.getValue())){
//			op_operatore_cognome.setValue("");
//			op_operatore_nome.setValue("");
			readOnly = true;
		}
		
		logger.trace(punto + " readOnly>>" + readOnly+"<");
		op_operatore_cognome.setReadonly(readOnly);
		op_operatore_nome.setReadonly(readOnly);
		
	}

	@Override
	protected void afterSetStatoInsert(){
		String punto = ver + "afterSetStatoInsert ";
		String codZona =getProfile().getStringFromProfile("zona_operatore"); 
		if (ISASUtil.valida(codZona)){
			zona.setValue(codZona);
		}
		try {
			impostaDistretto(true);
//			if (distretto !=null){
//				distretto.setSelectedIndex(0);
//				Component p = self.getFellow("panel_ubicazione");
//				PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
//				c.onSelect$distretto(null);
//				Events.sendEvent(Events.ON_SELECT, this.distretto, null);
//			}
			
//			if (!controlloMmgOperatorePrimaVisita) {
//				inserireMMGPlsOperatorePrimaVisita();
//				controlloMmgOperatorePrimaVisita = true;
//				this.setStato(this.STATO_WAIT);
//			} else {
				onChangeTipoOperatore();
				CaribelDatebox dataInizio = (CaribelDatebox) self.getParent()
						.getFellow(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO);
				if (dataInizio != null && dataInizio.getValue() != null) {
					dt_inizio_piano.setValue(dataInizio.getValue());
				}
				CaribelDatebox dataFine = (CaribelDatebox) self.getParent()
						.getFellow(CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE);
				if (dataFine != null && dataFine.getValue() != null) {
					dt_fine_piano.setValue(dataFine.getValue());
				}
				gestisciMMG();
//			}
		} catch (Exception e) {
			logger.trace(punto + "Errore nel recuperare i dati ", e);
		}
	}
	
	
	private void gestisciMMG() {
		String punto = ver + "gestisciMMG ";
		boolean readOnly = false; 
		if(cbx_tipo_operatore.getSelectedValue().equals(GestTpOp.CTS_COD_MMG)){
			super.btn_formgrid_update.setVisible(false);
			readOnly = true;
		}
		logger.trace(punto + " readOnly>>"+readOnly);
		dt_inizio_piano.setReadonly(readOnly);
		dt_fine_piano.setReadonly(readOnly);
//		num_acces_set.setReadonly(readOnly);
		cbx_tipo_operatore.setDisabled(readOnly);
		distretto.setDisabled(readOnly);
		presidio_comune_area.setDisabled(readOnly);
		
	}

	private void doCaricaComboDistretti() {
		try {
			Component p = self.getFellow("panel_ubicazione");
			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
			c.setDistrettiVoceTutti(false);
			c.doInitPanel();
			String codZona =getProfile().getStringFromProfile("zona_operatore"); 
			if (ISASUtil.valida(codZona)){
				zona.setValue(codZona);
			}
			c.setDistrettoFirst();
			distretto.setRequired(true);
			c.setVisibleZona(false);
			c.settaRaggruppamento("P");
			c.setVisibleRaggruppamento(false);
			c.setVisibleUbicazione(false);
			c.setDbNameZona(Costanti.CTS_OP_COINVOLTI_DB_NAME_ZONA);  
			c.setDbNameDistretto(Costanti.CTS_OP_COINVOLTI_DB_NAME_DISTRETTO);
			c.setDbNamePresidioComuneArea(Costanti.CTS_DB_NAME_PRESIDI);
			presidio_comune_areadis.setValue(Labels.getLabel("PanelUbicazione.presidio.sede")+":");
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Override
	protected void notEditable() {
		super.notEditable();
		dt_chiusura.setReadonly(true);
		dt_presa_carico.setReadonly(true);
	}
	private void abilitaMaschera() {
		dt_presa_carico.setReadonly(true);
		dt_chiusura.setReadonly(true);
		onChangeTipoOperatore();
	}

	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + "");
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
//		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE, cbx_tipo_operatore);
		String opCaricati = ManagerOperatore.loadTipiOperatori(cbx_tipo_operatore, CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE);
		logger.trace(punto + " op caricati>>"+ opCaricati);
		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
				"tab_descrizione", false);
		
//		caricaComboDistretti();
		
	}

	private void caricaComboDistretti() {
		try{
			CaribelComboRepository.comboPreLoad("query_distretti", new DistrettiEJB(), "query", 
					new Hashtable<String, String>(), cbx_cod_distretto, null, "cod_distr", "des_distr", false);
			
		}catch(Exception e){
			logger.error("caricamento combo REGION fallito! - Eccez=" + e);
		}
		
	}

	@Override
	protected void doLoadGrid() throws Exception {
		try {
			if ( (keyCartella.getValue()!=null) && (keyCartella.getValue() > 0) && (keyIdSkSo.getValue()!=null)  && (keyIdSkSo.getValue() > 0) ) {
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

	
	private boolean tipoOperatoreSelezionato() {
		boolean tipoOperatoreSelezionato = false;
		
		tipoOperatoreSelezionato = (cbx_tipo_operatore!=null && 
									cbx_tipo_operatore.getSelectedIndex()>=0 && 
									ISASUtil.valida(cbx_tipo_operatore.getSelectedValue())) ;
		
		return tipoOperatoreSelezionato;
	}
	
	public void onChangeTipoOperatore(){
		String punto = ver +"onChangeTipoOperatore ";
		logger.debug(punto + "sbianco il codice operatore ");
		
		boolean selezionatoOperatore = tipoOperatoreSelezionato();
		
		boolean abilitareRicercaOperatore= !selezionatoOperatore;
		CaribelSearchCtrl operatoreSearch = (CaribelSearchCtrl) cs_ski_infermiere.getAttribute(MY_CTRL_KEY);
		operatoreSearch.setReadonly(abilitareRicercaOperatore);
		op_operatore_cognome.setValue("");
		op_operatore_nome.setValue("");
		op_operatore_cognome.setReadonly(abilitareRicercaOperatore);
		op_operatore_nome.setReadonly(abilitareRicercaOperatore);
		
		logger.debug(punto + "abilitareRicercaOperatore>>"+abilitareRicercaOperatore+"<");
		cod_operatore.setValue("");
		cod_operatore_descr.setValue("");
		
		if (selezionatoOperatore){ 
			if(cbx_tipo_operatore.getSelectedValue().equals(GestTpOp.CTS_COD_MMG)){
				logger.trace(punto + " caso mmg " );
				CaribelDatebox dataAccettazione = (CaribelDatebox)self.getParent().getFellow(CostantiSinssntW.CTS_PR_DATA_PUAC);
				if (dataAccettazione!=null && dataAccettazione.getValue()!=null){
					dt_presa_carico.setValue(dataAccettazione.getValue());	
				}
				CaribelIntbox accessiMmg = (CaribelIntbox)self.getParent().getFellow(CostantiSinssntW.CTS_ACCESSI_MMG);
//				if (accessiMmg!=null && accessiMmg.getValue()!=null){
//					num_acces_set.setValue(accessiMmg.getValue());	
//				}
				operatoreSearch.setReadonly(true);
				CaribelTextbox cod_med = (CaribelTextbox) self.getParent().getFellowIfAny("cod_med");
				CaribelCombobox medico_desc=(CaribelCombobox)self.getParent().getFellowIfAny("medico_desc");
				String codMedico = "";
				String descrMedico = "";
				if (ISASUtil.valida(cod_med.getValue())){
					codMedico = cod_med.getValue();
					descrMedico = medico_desc.getValue();
				}else {
					CaribelTextbox pr_mmg_altro = (CaribelTextbox) self.getParent().getFellowIfAny("pr_mmg_altro");
					descrMedico = pr_mmg_altro.getValue();
				}
				cod_operatore.setValue(codMedico);
				cod_operatore_descr.setValue(descrMedico);
			}else {
				if (tipoOperatorePrimaVisita()){
					logger.trace(punto + " operatore appartiene alla prima visita porto i dati ");
					CaribelTextbox pv_cod_operatore = (CaribelTextbox) self.getParent().getFellowIfAny("pv_cod_operatore");
					CaribelCombobox pv_cod_operatore_descr=(CaribelCombobox)self.getParent().getFellowIfAny("pv_cod_operatore_descr");
					CaribelDatebox dt_pv_dt_visita =(CaribelDatebox)self.getParent().getFellowIfAny("pv_dt_visita");
					cod_operatore.setValue(pv_cod_operatore.getValue());
					cod_operatore_descr.setValue(pv_cod_operatore_descr.getValue());
					if (ManagerDate.validaData(dt_pv_dt_visita)){
						dt_presa_carico.setValue(dt_pv_dt_visita.getValue());
					}
					Events.sendEvent(Events.ON_CHANGE, cod_operatore, null);
				}
			}
		}else {
			dt_presa_carico.setValue(null);	
//			num_acces_set.setValue(0);
//			cod_operatore.setValue("");
//			cod_operatore_descr.setValue("");
		}	
		gestisciMMG();
		logger.debug(punto + "abilitareRicercaOperatore>>"+abilitareRicercaOperatore+"<");
	}
	
	private boolean tipoOperatorePrimaVisita() {
		String punto = ver + "tipoOperatorePrimaVisita ";
		logger.debug(punto + " inizio ");
		boolean operatorePrimaVisitaSelezionato = false;
		String tipoOperatore = cbx_tipo_operatore.getSelectedValue();
		if (ISASUtil.valida(tipoOperatore)) {
			CaribelCombobox cbx_pv_tp_operatore = (CaribelCombobox) self.getParent().getFellowIfAny("cbx_pv_tp_operatore");
			String codTipoOperatoreSelezionato = cbx_pv_tp_operatore.getSelectedValue();
			if (ISASUtil.valida(codTipoOperatoreSelezionato)&& codTipoOperatoreSelezionato.equals(tipoOperatore)){
				operatorePrimaVisitaSelezionato = true;
			}
		}
		logger.trace(punto + " tipoOperatore>>"+tipoOperatore+"< e' operatorePrimaVisita>"+operatorePrimaVisitaSelezionato+"<");
		return operatorePrimaVisitaSelezionato;
	}

	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";         
		
		if(tipoOperatorePresente()){
			return false;
		}
		
		boolean periodoConforme = true;
		keyCartella.setText(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"");
		CaribelIntbox IdSkSo = (CaribelIntbox) self.getParent().getFellow(CostantiSinssntW.CTS_ID_SKSO);
		keyIdSkSo.setValue(IdSkSo.getValue());
		/* controllo che il periodo entri nel piano */
		CaribelDatebox data_inizio = (CaribelDatebox) self.getParent().getFellowIfAny("data_inizio");
		CaribelDatebox data_fine = (CaribelDatebox) self.getParent().getFellowIfAny("data_fine");
		Label lb_dataInizio = (Label) self.getParent().getFellowIfAny("lb_dataInizio");
//		Label lb_dataFine = (Label) self.getParent().getFellowIfAny("lb_dataFine");
		
		
		
		Label lb_dtInizioPiano = (Label)self.getFellowIfAny("lb_dtInizioPiano");
		Label lb_dtFinePiano = (Label)self.getFellowIfAny("lb_dtFinePiano");
		
		periodoConforme = ManagerDate.controllaPeriodo(self, data_inizio, dt_inizio_piano, lb_dataInizio, lb_dtInizioPiano);
		logger.trace(punto + "\ndata inizio >"+data_inizio.getValueForIsas()+"<inizioPianoOp>>"+dt_inizio_piano.getValueForIsas()+"< ="+periodoConforme);
		
		if (periodoConforme){
			periodoConforme = ManagerDate.controllaPeriodo(self, dt_inizio_piano, dt_fine_piano, lb_dtInizioPiano, lb_dtFinePiano);
		}
		logger.trace(punto + "\ndt_inizio_piano >"+dt_inizio_piano.getValueForIsas()+"<dt_fine_piano>>"+dt_fine_piano.getValueForIsas()+"< ="+periodoConforme);
		     
		if (periodoConforme){
//			periodoConforme = !(ManagerDate.controllaPeriodo(self, data_fine, dt_fine_piano, lb_dtFinePiano, lb_dataFine);
			if (ManagerDate.validaData(data_fine) && ManagerDate.validaData(dt_fine_piano)) {
				Date dataApertura = DateUtils.truncate(data_fine.getValue(), Calendar.DAY_OF_MONTH);
				Date dataChiusura = DateUtils.truncate(dt_fine_piano.getValue(), Calendar.DAY_OF_MONTH);
				if (dataChiusura != null) {
					if (dataApertura.equals(dataChiusura)){
						periodoConforme= true;
					}else if (dataApertura.before(dataChiusura) ) {
						periodoConforme = false;
					}
				} else {
					periodoConforme= true;
				}
			}			
			
			logger.trace(punto + " dt_fine_piano >"+dt_fine_piano.getValueForIsas()+"< data_fine>>"+data_fine.getValueForIsas()
					+"<\n Verificare se ci sono delle proroghe "+periodoConforme);
			if (!periodoConforme){
				periodoConforme = verificaCoerenzaDataFine();
				if (!periodoConforme){ 
//					Messagebox.show(Labels.getLabel("menu.segreteria.organizzativa.scheda.operatori.dt.fine.supera"), Labels.getLabel("messagebox.attention"),
//							Messagebox.OK, Messagebox.ERROR);
					dt_fine_piano.setErrorMessage(Labels.getLabel("menu.segreteria.organizzativa.scheda.operatori.dt.fine.supera"));
				}else {
					dt_fine_piano.setErrorMessage(null);
				}
			}
			logger.trace(punto + " dt_fine_piano >"+dt_fine_piano.getValueForIsas()+"< data_fine>>"+data_fine.getValueForIsas()
				+"< opro="+periodoConforme);
		}
		
		if (periodoConforme){
			periodoConforme = ManagerDate.controllaPeriodo(self, dt_presa_carico, dt_chiusura, "lb_dtPresaCarico","lb_dtChiusura");
		}
		logger.trace(punto + "ho processato>>"+ periodoConforme+"<");
		
		return periodoConforme;
	}
	
	private boolean verificaCoerenzaDataFine() {
		String punto = ver + "verificaCoerenzaDataFine ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		boolean dataCoerente = false;
		try{
			dati.put(CostantiSinssntW.N_CARTELLA,keyCartella.getValue()+"");
			dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue()+"");
			dati.put(CostantiSinssntW.CTS_OP_DATA_FINE_PIANO, dt_fine_piano.getValueForIsas()+"");
			
			dataCoerente = ((Boolean)invokeGenericSuEJB(myEJB, dati, "dataFineCoerente")).booleanValue();
		}catch(Exception e){
			logger.error(punto + " Errore nel recuperare i dati ");
		}

		return dataCoerente;
	}

	public void doPublicLoadGrid(String nCartella, String idSkso) throws Exception {
		String punto = ver + "doPublicLoadGrid ";
		logger.trace(punto + " dati>>" +nCartella+"<< idSkso>>"+ idSkso+"<<");
		keyCartella.setText(nCartella);
		keyIdSkSo.setText(idSkso);
		
		doLoadGrid();
	}

	public boolean isMedicoDistrettoPresente(boolean conMedicoInserito) {
		String punto = ver + "isComponentiUviInseritoMedico ";
		boolean medicoPresente = false;
		try {
			if (clb != null) {
				Iterator<Listitem> iterator = clb.getItems().iterator();
				while (iterator.hasNext() && !medicoPresente) {
					Listitem type = (Listitem) iterator.next();
					Hashtable<String, Object> htFromGrid = (Hashtable<String, Object>) type
							.getAttribute("ht_from_grid");
					String prTipo = ISASUtil.getValoreStringa(htFromGrid, Costanti.TIPO_OPERATORE);

					if (ISASUtil.valida(prTipo) && prTipo.trim().equals(GestTpOp.CTS_COD_MEDICO)) {
						if (conMedicoInserito){
							String prOperatore = ISASUtil.getValoreStringa(htFromGrid, Costanti.COD_OPERATORE);
							medicoPresente = ISASUtil.valida(prOperatore);
						}else {
							logger.trace(punto + " mi interessa sapere se è stato inserito il medico, o meno "); 
							medicoPresente = true; 
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare i dati>>", e);
		}
		return medicoPresente;
	}
	
}

package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.nuovi.RMPuaUvmCommissioneEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Messagebox;

public class SOPresentiUviCtrl extends SOPresentiUviBaseCtrl {

	private static final long serialVersionUID = 1L;
//	private String myKeyPermission = SegreteriaOrganizzativaFormCtrl.myKeyPermission;
	private RMPuaUvmCommissioneEJB myEJB = new RMPuaUvmCommissioneEJB();
//	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/schedaSO_presenti_uvi.zul";
//	Object objNCartella = "";
//	Object objIdSkSo = "";
//
//	private AbstractComponent cs_pr_operatore;
//	
//	private CaribelIntbox keyCartella;
//	private CaribelIntbox keyIdSkSo;
//	private CaribelIntbox keyIdPrPresenza;
//	private CaribelTextbox pr_operatore_cognome;
//	private CaribelTextbox pr_operatore_nome;
//	private CaribelCombobox cbx_pr_tipo;
//	protected Hlayout riga_operatore_searc;
//	private CaribelRadiogroup pr_partecipa;
//	private CaribelRadiogroup pr_responsabile;
//	private Checkbox isPianoCongelato;
//
//	private CaribelTextbox pr_operatore;
//	private CaribelCombobox pr_operatore_descr;
//	private String ver = "22-";
//	private boolean controlloOperatoriInserimentoUvi= false;
//	private boolean msgAvvisoDtUvi = false;
//	private CaribelDatebox pr_data_verbale_uvm;
//	private int caricamentoGriglia = 0;
	
	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			super.setMethodNameForInsert("insert");
			super.setMethodNameForUpdate("update");
			super.setMethodNameForQueryKey("queryKey");
			super.setMethodNameForDelete("delete");

			doPopulateCombobox();
			
			CaribelSearchCtrl medicoSearch = (CaribelSearchCtrl) cs_pr_operatore.getAttribute(MY_CTRL_KEY);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, cbx_pr_tipo);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, getProfile().getStringFromProfile("zona_operatore"));
			
			pr_operatore.addEventListener(CaribelSearchCtrl.ON_UPDATE_CARIBEL_SEARCH, new EventListener<Event>(){
				public void onEvent(Event event){
					abilitaScritturaOperatoreCognomeNomeAltro();
				}
				});
			
			pr_operatore_cognome.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					abilitaSceltaOperatore();
					return;
				}});
			
			pr_operatore_nome.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
					abilitaSceltaOperatore();
				}
				});
			
			try {
				pr_data_verbale_uvm = ((CaribelDatebox)((Component)self.getParent().getSpaceOwner()).getFellowIfAny("pr_data_verbale_uvm", true));
			} catch (Exception e) {
				logger.error(punto + " Errore nel recupera la data ", e);
			}
			
			if (arg.get("n_cartella") == null) {
				CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
				if (containerCorr !=null) {
					objNCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA);
					objIdSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
				} else {
					//throw new Exception("Reperimento chiavi Proroghe non riuscito!");
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

//	public void abilitaScritturaOperatoreCognomeNomeAltro() {
//		String punto = ver + "abilitaScritturaOperatoreCognomeNomeAltro ";
//		logger.trace(punto + "dati >>");
//		String prOperatore = "";
//		try {
//			prOperatore = pr_operatore.getText();
//		} catch (Exception e2) {
//		}
//		
//		boolean abilitareScritturaMedicoAltro = ISASUtil.valida(prOperatore);
//		try {
//			logger.trace(punto + " abilitare operatore altro>>"+ abilitareScritturaMedicoAltro);
//			pr_operatore_nome.setReadonly(abilitareScritturaMedicoAltro);
//			pr_operatore_cognome.setReadonly(abilitareScritturaMedicoAltro);
//			if(abilitareScritturaMedicoAltro){
//				pr_operatore_nome.setValue("");
//				pr_operatore_cognome.setValue("");
//			}
//		} catch (Exception e) {
////			e.printStackTrace();
//		}
//	}

	private void abilitaMascheraDettaglio() {
		String punto = ver + "abilitaMascheraDettaglio ";
		logger.trace(punto + " verifica dati dettaglio ");

		pr_operatore_nome.setReadonly(false);
		pr_operatore_cognome.setReadonly(false);
		if (ISASUtil.valida(pr_operatore.getValue())){
			logger.trace(punto + "disabilito il cognome e nome: ho il codice operatore");
			pr_operatore_nome.setValue("");
			pr_operatore_cognome.setValue("");
			pr_operatore_nome.setReadonly(true);
			pr_operatore_cognome.setReadonly(true);
		}
		pr_partecipa.setSelectedValue(CostantiSinssntW.CTS_N);
		pr_responsabile.setSelectedValue(CostantiSinssntW.CTS_N);
	}
	
	
	public void onChangeTipoPresentiUvi(){
		 onChangeTipoPresentiUvi(true);
	}
	
	public void onChangeTipoPresentiUvi(boolean inserimento){
		String punto = ver +"onChangeTipoPresentiUvi ";
		logger.debug(punto + "sbianco il codice operatore ");
		CaribelSearchCtrl medicoSearch = (CaribelSearchCtrl) cs_pr_operatore.getAttribute(MY_CTRL_KEY);
		
		if (cbx_pr_tipo !=null && cbx_pr_tipo.getSelectedValue().equals(GestTpOp.CTS_CAREGIVER)){
			pr_operatore.setReadonly(true);
			pr_operatore_descr.setReadonly(true);
			CaribelTextbox nome_caregiver = (CaribelTextbox)self.getParent().getFellow("nome_caregiver");
			CaribelTextbox cognome_caregiver = (CaribelTextbox)self.getParent().getFellow("cognome_caregiver");
			if (ISASUtil.valida(pr_operatore_nome.getValue()) && ISASUtil.valida(pr_operatore_cognome.getValue())){
				logger.trace(punto + " dati validi ");
			}else {
				logger.trace(punto + " recupero i data dai presenti ");
				pr_operatore_nome.setValue(nome_caregiver.getValue());
				pr_operatore_cognome.setValue(cognome_caregiver.getValue());
			}
			medicoSearch.setReadonly(true);
			pr_operatore.setValue("");
			pr_operatore_descr.setValue("");
		}else if (cbx_pr_tipo !=null && cbx_pr_tipo.getSelectedValue().equals(GestTpOp.CTS_COD_MMG)){
			pr_operatore.setReadonly(true);
			pr_operatore_descr.setReadonly(true);
			pr_operatore_nome.setValue("");
			pr_operatore_cognome.setValue("");
			pr_operatore_nome.setReadonly(true);
			pr_operatore_cognome.setReadonly(true);
			CaribelTextbox cod_med = (CaribelTextbox)self.getParent().getFellowIfAny("cod_med");
			if (ISASUtil.valida(cod_med.getValue())){
				CaribelCombobox medico_desc = (CaribelCombobox)self.getParent().getFellowIfAny("medico_desc");
				pr_operatore.setValue(cod_med.getValue());
				pr_operatore_descr.setValue(medico_desc.getValue());
			}else {
				try {
					CaribelTextbox pr_mmg_altro = (CaribelTextbox) self.getPage().getFellowIfAny("pr_mmg_altro");
					pr_operatore_cognome.setValue(pr_mmg_altro.getValue());
				} catch (Exception e) {
				}
			}
			medicoSearch.setReadonly(true);
		}else {
			if (inserimento){
				pr_operatore.setValue("");
				pr_operatore_descr.setValue("");
				pr_operatore_cognome.setValue("");
				pr_operatore_nome.setValue("");
				pr_operatore.setReadonly(false);
				pr_operatore_descr.setReadonly(false);
				medicoSearch.setReadonly(false);
				pr_operatore_nome.setReadonly(false);
				pr_operatore_cognome.setReadonly(false);
			}
		}
	}

	@Override
	protected void afterSetStatoInsert(){
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + "inizio ");
		
		boolean abilitareCBCommissioneUvi = abilitazioneCBVommissioneUvi();
		try {
			if (abilitareCBCommissioneUvi){
				this.setStato(CaribelGridStateCtrl.STATO_WAIT);
			}else {
				if (!controlloOperatoriInserimentoUvi) {
					if (inserireOperatoreUvi()){
						controlloOperatoriInserimentoUvi = true;
					}else {
						logger.trace(punto + " NON HO INSERITO OPERATORI ");
					}
					this.setStato(CaribelGridStateCtrl.STATO_WAIT);
				} else {
					logger.trace(punto + " prove ");
					keyIdPrPresenza.setValue(-1);
					try {   
						onChangeTipoPresentiUvi(true);
					} catch (Exception e) {
					}
					pr_partecipa.setSelectedValue(CostantiSinssntW.CTS_N);
					pr_responsabile.setSelectedValue(CostantiSinssntW.CTS_N);
				}
			}
		} catch (Exception e) {
			logger.error(punto + " Errore nel recuperare i dati ", e);
		} 
	}

//	private boolean abilitazioneCBVommissioneUvi() {
//		if(isPianoCongelato == null){
//			isPianoCongelato = (Checkbox) self.getParent().getFellowIfAny("isPianoCongelato", true);
//		}
//	return (isPianoCongelato.isChecked());
//	}

	private boolean inserireOperatoreUvi() {
		String punto = ver + "inserireOperatoreUvi ";
		logger.trace(punto + " inizio ");
		
		keyCartella.setText(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"");
		CaribelIntbox IdSkSo = (CaribelIntbox) self.getParent().getFellow(CostantiSinssntW.CTS_ID_SKSO);
		keyIdSkSo.setValue(IdSkSo.getValue());
		boolean presenzaMedico= verificaPresenzaMedico();
		if ( (keyCartella.getValue()!=null) && (keyCartella.getValue() > 0) && (keyIdSkSo.getValue()!=null)  && (keyIdSkSo.getValue() > 0) 
				&& presenzaMedico ) {
			RMPuaUvmCommissioneEJB rmPuaUvmCommissioneEJB = new RMPuaUvmCommissioneEJB();
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue() + "");
			dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue() + "");
			Vector<ISASRecord> griglia = new Vector<ISASRecord>();
			try {
				griglia = rmPuaUvmCommissioneEJB.inserisciOperatoriUvi(CaribelSessionManager.getInstance().getMyLogin(), dati);
				gestisciDataVerbaleUVM(griglia);
			} catch (DBRecordChangedException e) {
				e.printStackTrace();
			} catch (ISASPermissionDeniedException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			clb.getItems().clear();
			clb.setModel(new CaribelListModel<ISASRecord>(griglia));
		}else {
			logger.trace(punto + " Non si è inserito gli operatori presenzaMedico>>"+presenzaMedico+"<");
		}
		return presenzaMedico;
	}

	private boolean verificaPresenzaMedico() {
		String punto = ver + "verificaPresenzaMedico ";

		String tipoCura = getTipoCura();
		boolean medicoPresente = true;
		if (ISASUtil.valida(tipoCura) && tipoCura.equalsIgnoreCase(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)) {
			int presenteMedicoMMG = isComponentiUviInseritoMedico(GestTpOp.CTS_COD_MMG);
			if (presenteMedicoMMG <0 || presenteMedicoMMG == SOPresentiUviBaseCtrl.CTS_MANCA_CODICE_MMG) {
				String codMedico = "";
				try {
					codMedico = ((CaribelTextbox) (self.getParent().getFellowIfAny("cod_med"))).getText();
				} catch (Exception x) {
					logger.error(punto + " Errore nel recuperare il codice del medico >>" + x);
				}
				medicoPresente = ISASUtil.valida(codMedico);
			}
			logger.trace(punto + " codice medico >>" + medicoPresente);
		}
		
		if (!medicoPresente){
			String msg = Labels.getLabel("so.inserire.medico.msg"); 
			Messagebox.show(msg, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.ERROR);
		}
		
		return medicoPresente;
	}

	private String getTipoCura() {
		String punto = ver + "getTipoCura ";
		String tipoCura = "";
		try {
			tipoCura = ((CaribelCombobox)(self.getParent().getFellowIfAny("tipocura"))).getSelectedValue();
		} catch (Exception x) {
			logger.error(punto + " Errore nel recuperare il tipoCura>>" + x);
		}
		
		return tipoCura;
	}

	@Override
	protected void afterSetStatoUpdate() {
		String punto = ver + "afterSetStatoUpdate ";
		logger.trace(punto + " prove ");
		
		boolean abilitareCBCommissioneUvi = abilitazioneCBVommissioneUvi();
		try {
			if (abilitareCBCommissioneUvi){
				disabilitaGriglia();		
			}else {
				onChangeTipoPresentiUvi(false);
				abilitaMascheraDettaglio();
			}
		} catch (Exception e) {
		}
	}

	public void disabilitaGriglia() throws ISASMisuseException {
		String punto = ver + "disabilitaGriglia "; 
		logger.trace(punto + " disabilito griglia ");
		this.setStato(CaribelGridStateCtrl.STATO_WAIT);
		super.btn_formgrid_new.setVisible(false);
		super.btn_formgrid_delete.setVisible(false);
		super.btn_formgrid_edit.setVisible(false);
		clb.setShowBtnDeleteInRow(false);
		clb.setShowBtnEditInRow(false);
		logger.trace(punto  + " disabilito tuttto ");
	}

	@Override
	protected void executeInsert() throws Exception {
		String punto = ver + "executeInsert ";
		if(possoInserireDati()){
			super.executeInsert();
		}else {
			logger.trace(punto + " figura già presente ");
		}
	}

	@Override
	protected void executeUpdate() throws Exception {
		String punto = ver + "executeUpdate ";
		if(possoInserireDati()){
			super.executeUpdate();
		}else {
			logger.trace(punto + " figura già presente ");
		}
	}

	private boolean possoInserireDati() {
		String punto = ver + "possoInserireDati ";
		boolean possoInserireDati= true;
		logger.trace(punto + " Verifica la presenza nella tabella della figura mmg o caregiver ");
		String codFiguraSelezionato = cbx_pr_tipo.getSelectedValue();
		if (ISASUtil.valida(codFiguraSelezionato)	
				&& ( codFiguraSelezionato.equals(GestTpOp.CTS_CAREGIVER)  || 
					 codFiguraSelezionato.equals(GestTpOp.CTS_COD_MMG) ) ) {
			RMPuaUvmCommissioneEJB rmPuaUvmCommissioneEJB = new RMPuaUvmCommissioneEJB();
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue()+"");
			dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue()+"");
			dati.put(CostantiSinssntW.CTS_PR_PRESENZA, keyIdPrPresenza.getValue()+"");
			dati.put(CostantiSinssntW.CTS_PR_TIPO, codFiguraSelezionato);
			try {
//				dbrPuauvmCommissioni = rmPuaUvmCommissioneEJB.queryKeyFigure(CaribelSessionManager.getInstance().getMyLogin(), dati);
				possoInserireDati = rmPuaUvmCommissioneEJB.queryKeyFigure(CaribelSessionManager.getInstance().getMyLogin(), dati);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if (!possoInserireDati){
				String tipoUvi = Labels.getLabel("segreteria.organizzativa.scheda.presenti.uvi.tipo");
				String descrizioneTipo = cbx_pr_tipo.getSelectedItem().getLabel();
				String messaggio = Labels.getLabel("segreteria.organizzativa.scheda.presenti.figura.presente", new String[]{tipoUvi, descrizioneTipo});;
				Messagebox.show(messaggio,
				Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);
			}
			logger.trace(punto + " dbrPuauvmCommissioni>>>"+ possoInserireDati+"<" );
			
		}else {
			logger.trace(punto + " figura prof>>"+ codFiguraSelezionato+ "< tipo non tra quelli evidenziati:>"+GestTpOp.CTS_CAREGIVER+","+
					GestTpOp.CTS_COD_MMG+"<");
		}
		logger.trace(punto + " possoInserire>>"+ possoInserireDati+"<" );
		return possoInserireDati;
	}



	private void abilitaSceltaOperatore() {
		String punto = ver + "abilitaSceltaOperatore ";
		String cognome= pr_operatore_cognome.getText();
		String nome = pr_operatore_nome.getText();
		
		boolean abilitareLaRicerca = (ISASUtil.valida(cognome)|| ISASUtil.valida(nome));
		logger.trace(punto + " abilitare>>" +abilitareLaRicerca);
//		pr_operatore.setReadonly(abilitareLaRicerca);
//		pr_operatore_descr.setReadonly(abilitareLaRicerca);
		CaribelSearchCtrl medicoSearch = (CaribelSearchCtrl) cs_pr_operatore.getAttribute(MY_CTRL_KEY);
		medicoSearch.setReadonly(abilitareLaRicerca);
	}
	
	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + " inizio ");
		String opCaricati = ManagerOperatore.loadTipiOperatori(cbx_pr_tipo, CostantiSinssntW.TAB_VAL_OPERATORI_UVI);
		if (cbx_pr_tipo!=null){
			logger.debug(punto + " aggiungo caregiver ");
			String codice = GestTpOp.CTS_CAREGIVER;
			String descrizione = GestTpOp.CTS_CAREGIVER_DESCRIZIONE;
			CaribelComboRepository.addComboItem(cbx_pr_tipo, codice, descrizione);
		}
		logger.trace(punto + " opcaricati \n"+ opCaricati);
	}

//	@Override
//	protected void doLoadGrid() throws Exception {
//		try {
//			if ( (keyCartella.getValue()!=null) && (keyCartella.getValue() > 0) && (keyIdSkSo.getValue()!=null)  && (keyIdSkSo.getValue() > 0) ) {
//				hParameters.putAll(getOtherParametersString());
//				this.hParameters.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue() + "");
//				this.hParameters.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue() + "");
//				Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//				clb.getItems().clear();
//				clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
//				
//				gestisciDataVerbaleUVM(vDbr);
//			} else {
//				logger.debug(" \n Non effettuo il caricamento della griglia ");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	private void gestisciDataVerbaleUVM(Vector<ISASRecord> vDbr) {
//		if(caricamentoGriglia>0 && !msgAvvisoDtUvi){
//			caricaDataCommissioneUvi(vDbr);
//		}
//		caricamentoGriglia++;
//	}

//	private void caricaDataCommissioneUvi(Vector<ISASRecord> vDbr) {
//		String punto = ver + "caricaDataCommissioneUvi ";
//		Date dataValutazioneBisogni = null;
//		SCBisogniEJB scBisogniEJB = new SCBisogniEJB();
//		Hashtable<String, Object> prtDati = new Hashtable<String, Object>();
//		prtDati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getValue() +"");
//		prtDati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue()+"");
//		try {
//			ISASRecord dbrScalaBisogni = scBisogniEJB.getScalaBisogni(CaribelSessionManager.getInstance().getMyLogin(), prtDati);
//			if (dbrScalaBisogni !=null){
//				dataValutazioneBisogni =(Date)dbrScalaBisogni.get("data");
//			}
//		} catch (Exception e) {
//			logger.error(punto + " Errore nel recupero dei dati ");
//		}
//		
//		
//		if( ManagerDate.validaData(dataValutazioneBisogni) && !ManagerDate.validaData(pr_data_verbale_uvm) && vDbr!=null 
//					&& vDbr.size()>0 && !msgAvvisoDtUvi){
//			msgAvvisoDtUvi = true;
//			final CaribelDatebox prDataVerbaleUvm = pr_data_verbale_uvm;
//			final Date dtValutazioneBisogni = dataValutazioneBisogni;
//			String[] sostituire = new String[]{ManagerDate.formattaDataIta(dataValutazioneBisogni,"/"), 
//						Labels.getLabel("menu.segreteria.organizzativa.scheda.verbale.uvm.data.valutazione")};
//			String messaggio = Labels.getLabel("menu.segreteria.organizzativa.scheda.verbale.uvm.msg.dt.commissione", sostituire);
//			Messagebox.show(messaggio,
//					Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
//						public void onEvent(Event event) throws Exception{
//							if (Messagebox.ON_YES.equals(event.getName())) {
//								prDataVerbaleUvm.setValue(dtValutazioneBisogni);
//							}
//						}
//					});
//		}
//	}

	@Override
	protected boolean doValidateForm() {
		String punto= ver + "doValidateForm ";
		boolean periodoConforme = true;
		keyCartella.setText(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"");
//		keyIdSkSo.setText(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO)+"");
//		JCariDateTextFieldPianoAss = (CaribelDatebox) self.getParent().getFellow("JCariDateTextFieldPianoAss");
		CaribelIntbox IdSkSo = (CaribelIntbox) self.getParent().getFellow(CostantiSinssntW.CTS_ID_SKSO);
		keyIdSkSo.setValue(IdSkSo.getValue());
		String prOperatore = pr_operatore.getText();
		String prOperatoreCognome = pr_operatore_cognome.getText();
		String prOperatoreNome = pr_operatore_nome.getText();
		if (ISASUtil.valida(prOperatore) ||(ISASUtil.valida(prOperatoreCognome) && ISASUtil.valida(prOperatoreNome))){
			logger.trace(punto + " posso salvare ");
		}else {
			if (ISASUtil.valida(prOperatoreCognome) || ISASUtil.valida(prOperatoreNome)){
				UtilForUI.standardExclamation(Labels.getLabel("segreteria.organizzativa.scheda.presenti.operatore.necessario.non.codificato"));
			}else {
				if (!ISASUtil.valida(prOperatore)){
					UtilForUI.standardExclamation(Labels.getLabel("segreteria.organizzativa.scheda.presenti.operatore.necessario"));
				}
			}
			periodoConforme = false;
		}
		return periodoConforme;
	}
	
//	public void doPublicLoadGrid(String nCartella, String idSkso, boolean readOnly) throws Exception {
//		String punto = ver + "doPublicLoadGrid ";
//		logger.trace(punto + " dati>>" +nCartella+"<< idSkso>>"+ idSkso+"<<");
//		keyCartella.setText(nCartella);
//		keyIdSkSo.setText(idSkso);
//		
//		doLoadGrid();
//		boolean abilitareCBCommissioneUvi = abilitazioneCBVommissioneUvi();
//		if (abilitareCBCommissioneUvi || readOnly){
//			disabilitaGriglia();
//		}
//	}
	
}

package it.caribel.app.sinssnt.controllers.segnalazione;

import it.caribel.app.sinssnt.bean.nuovi.SegnalazioniEJB;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.Window.Mode;

public class SegnalazioneGridCRUDCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	public static String myKeyPermission = ChiaviISASSinssntWeb.CTS_SEGNALAZIONI;
	public static final String CTS_POSSO_MODIFICARE_TUTTE_SEGNALAZIONI = "mod_ot_segnal";
	private SegnalazioniEJB myEJB = new SegnalazioniEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/segnalazione/segnalazioneForm.zul";
	private CaribelIntbox key_cartella;
	private CaribelIntbox idskso;
	private CaribelTextbox tipoOperatore;
	private CaribelTextbox codOperatore;
//	private CaribelTextbox vista_so;
	private CaribelDatebox key_data;
	String nCartella = "";
	String valIdSkso = "";
	boolean isContainerSO = false;
	private Button btn_container_so;
	private Hlayout container_so;
	private Hlayout segnalazioni_contatti;
	
	private static final String ver = "21-";
	private boolean possoModificareSegnalazioni = true;

	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			doPopulateCombobox();

			recuperaDati();
			
			String modal = ISASUtil.getValoreStringa(arg, CostantiSinssntW.MODALE);
			if (ISASUtil.valida(modal) && modal.equals(CostantiSinssntW.CTS_SI)) {
				logger.trace(punto + " apertura modale ");
				((Window) self).setMode(Mode.MODAL);
				((Window) self).setClosable(true);
			} else {
				logger.trace(punto + " apertura NON modale ");
			}
			doLoadGrid();
			super.btn_formgrid_new.setVisible(possoModificareSegnalazioni);
			clb.setShowBtnDeleteInRow(possoModificareSegnalazioni);
			clb.setShowBtnEditInRow(possoModificareSegnalazioni);
			
			btn_container_so.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
				public void onEvent(Event event)throws Exception {
					cambioStatoSegnalazione();
				}
			});
			
			
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void cambioStatoSegnalazione() {
		String punto = ver + "cambioStatoSegnalazione ";
		logger.trace(punto + " Inizio ");
		try {
			Hashtable dati = this.currentIsasRecord.getHashtable();
			int vistaSo = ISASUtil.getValoreIntero(dati, CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO);

			int nuoValore = CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO_INSERITA_UPDATE;
			if (vistaSo >0 && vistaSo == CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO_INSERITA_UPDATE){
				nuoValore = CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO_VISTA;
			}
			
			dati.put(CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO, nuoValore+"");
			myEJB.aggiornaStatoVistaSo(CaribelSessionManager.getInstance().getMyLogin(), dati);
			logger.trace(punto + " disabilito il bottone ");
			btn_container_so.setDisabled(true);
			doLoadGrid();
			
			try {
				this.setStato(CaribelGridStateCtrl.STATO_WAIT);
				impostaCollegamentoBottone(false);
			} catch (ISASMisuseException e) {
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			logger.error(punto + " Errore nell'aggiornare lo stato ");
		}
	}

	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		super.btn_formgrid_new.setVisible(possoModificareSegnalazioni);
		if (isContainerSO){
			super.btn_formgrid_delete.setVisible(possoModificareSegnalazioni);
			super.btn_formgrid_edit.setVisible(possoModificareSegnalazioni);
		}
		clb.setShowBtnDeleteInRow(possoModificareSegnalazioni);
		clb.setShowBtnEditInRow(possoModificareSegnalazioni);
		container_so.setVisible(false);
		segnalazioni_contatti.setVisible(false);
		if (isContainerSO){
			container_so.setVisible(true);
			impostaCollegamentoBottone(false);
		}else {
			segnalazioni_contatti.setVisible(true);
		}
	}
	
	private void impostaCollegamentoBottone(boolean abilitareBottone) {
		String punto = ver + "impostaCollegamentoBottone ";
		String label ="";// Labels.getLabel("segnalazione.bnt.cambia.stato.senza.stato");
		logger.trace(punto + " label>>" +label);
//		boolean abilitareBottone =true;
		
		btn_container_so.setDisabled(true);
		btn_container_so.setVisible(false);
		if (abilitareBottone && this.currentIsasRecord!=null) {
			int vistaSo = ISASUtil.getValoreIntero(this.currentIsasRecord, CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO);
			String vistaSoDa,vistaSoA;
			abilitareBottone = true;
			
			vistaSoDa = "elenco.segnalazione.stato.num_"+vistaSo;
			if (vistaSo>0 && vistaSo == CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO_INSERITA_UPDATE){
				vistaSoA ="elenco.segnalazione.stato.num_"+ CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO_VISTA;
			}else {
				vistaSoA = "elenco.segnalazione.stato.num_"+CostantiSinssntW.CTS_SEGNALAZIONE_VISTA_DA_SO_INSERITA_UPDATE;
			}
			
			String[] statiVistaSo = new String[] { Labels.getLabel(vistaSoDa), Labels.getLabel(vistaSoA) };
			label = Labels.getLabel("segnalazione.bnt.cambia.stato.con.dati", statiVistaSo);
			btn_container_so.setDisabled(!abilitareBottone);
			btn_container_so.setVisible(abilitareBottone);
			btn_container_so.setLabel(label);
		}
	}

	public void recuperaDati() throws Exception {
		String punto = ver + "recuperaDati ";
		logger.trace(punto + " inizio con dati>>"+ arg +"<\narg<>>" + this.hParameters+"<");
		
		if (arg.get("n_cartella") == null) {
			nCartella = UtilForContainer.getCartellaCorr();
			if (nCartella == null || nCartella.trim().equals(""))
				throw new Exception("Reperimento codice Assistito non riuscito!");
		} else {
			nCartella = ISASUtil.getValoreStringa(arg, CostantiSinssntW.N_CARTELLA);
		}
		logger.trace(punto + " nCartella>>" + nCartella + "<");
		if (!ISASUtil.valida(nCartella)) {
			nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA) + "";
		}
		key_cartella.setText(nCartella);

		if (arg.get(CostantiSinssntW.CTS_ID_SKSO) != null) {
			valIdSkso = ISASUtil.getValoreStringa(arg, CostantiSinssntW.CTS_ID_SKSO);
		}
		logger.trace(punto + " idSkso>>" + valIdSkso + "<");
		if (!ISASUtil.valida(valIdSkso)) {
			Object obj = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
			if (obj != null) {
				valIdSkso = obj + "";
			}
		}
		if (ISASUtil.valida(valIdSkso)) {
			idskso.setText(valIdSkso);
		}

		if ((UtilForContainer.getContainerCorr() != null)
				&& (UtilForContainer.getContainerCorr() instanceof ContainerPuacCtrl)) {
			isContainerSO = true;
			possoModificareSegnalazioni = false;
		}else {
			possoModificareSegnalazioni = true;
		}
	}

	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + "");
//		cbx_tipo_operatore.clear();
//		ManagerOperatore.loadTipiOperatori(cbx_tipo_operatore, false);
	}

	@Override
	public void onClick$btn_formgrid_new() {
		try {
			this.clb.setSelectedIndex(-1);
			UtilForBinding.resetForm(myForm, this.parkSetting);
			this.setStato(STATO_INSERT);
		} catch (Exception e) {
			doShowException(e);
		}
	}

	@Override 
	protected void executeDelete() throws Exception {
		String punto = ver + "executeDelete ";
		logger.trace(punto + " inizio ");
		if (!possoModificareSegnalazioni) {
			Messagebox.show(Labels.getLabel("segnalazione.messaggio.no.diritti.cancellazione"),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.ERROR);

			return;
		} else {
			executeOpen();
			boolean possoModificare = possoModificareDati();
			if (!possoModificare) {
				Messagebox.show(Labels.getLabel("segnalazione.messaggio.altro.operatore.inserito"),
						Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.ERROR);
				return;
			}
		}
		super.executeDelete();
	}
	
	@Override
	protected void afterSetStatoInsert() {
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " inizio ");
		if (possoModificareSegnalazioni) {
			CaribelDatebox dataContattoMedico = (CaribelDatebox) ((Window) self.getSpaceOwner()).getParent()
					.getFellowIfAny(CostantiSinssntW.CTS_SKM_DATA_APERTURA);
			if (dataContattoMedico == null) {
				logger.trace(punto + " non ho il conttato medico, recupero della SO ");
				CaribelDatebox dataContattoSO = (CaribelDatebox) ((Window) self.getSpaceOwner()).getParent()
						.getFellowIfAny(CostantiSinssntW.CTS_PR_DATA_PUAC);
				CaribelIntbox idSkSo = (CaribelIntbox) ((Window) self.getSpaceOwner()).getParent().getFellowIfAny(
						CostantiSinssntW.CTS_ID_SKSO);
				if (dataContattoSO != null && (idSkSo != null && idSkSo.getValue() != null && idSkSo.getValue() >= 0)) {
					logger.trace(punto + "Inserisco la data della SO");
					key_data.setValue(dataContattoSO.getValue());
				} else {
					CaribelDatebox dataRichiestaMMGPLS = (CaribelDatebox) ((Window) self.getSpaceOwner()).getParent()
							.getFellowIfAny(CostantiSinssntW.RICH_MMG_DATA_RICHIESTA);
					if (dataRichiestaMMGPLS != null) {
						logger.trace(punto + "Inserisco la data delle richiesta mmg ");
						key_data.setValue(dataRichiestaMMGPLS.getValue());
					}
				}
			} else {
				logger.trace(punto + "Inserisco la data della SO");
				key_data.setValue(dataContattoMedico.getValue());
			}
			verificaData();
		}else {
			logger.trace(punto + " NON POSSO MODIFICARE LA SEGNALAZIONE: SOLO VISUALIZZARE");
			try {
				this.setStato(CaribelGridStateCtrl.STATO_WAIT);
				impostaCollegamentoBottone(false);
			} catch (ISASMisuseException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void afterSetStatoUpdate() {
		String punto = ver + "afterSetStatoUpdate ";
		if (possoModificareSegnalazioni){
			boolean possoModificare = possoModificareDati();
			if (!possoModificare){
				logger.trace(punto +" posso modificare i dati ");
				Messagebox.show(
						Labels.getLabel("segnalazione.messaggio.altro.operatore.inserito"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.ERROR);
				
				try {
					setStato(STATO_WAIT);
				} catch (ISASMisuseException e) {
					e.printStackTrace();
				}
				return ;
			}
			super.afterSetStatoUpdate();
		}else {
			logger.trace(punto + " NON POSSO MODIFICARE LA SEGNALAZIONE: SOLO VISUALIZZARE");
			try {
				this.setStato(CaribelGridStateCtrl.STATO_WAIT);
				impostaCollegamentoBottone(true);
			} catch (ISASMisuseException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean possoModificareDati() {
		boolean possoModificare = false;
		String tipoOperatore = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_SEGNALAZIONE_TIPO_OPERATORE);
		String codOperatore = ISASUtil.getValoreStringa(this.currentIsasRecord, CostantiSinssntW.CTS_SEGNALAZIONE_COD_OPERATORE);
		if (ISASUtil.valida(tipoOperatore) && ISASUtil.valida(codOperatore)){
			String myTipo = getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE)+"";
			String myCodOperatore =getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
			possoModificare = (tipoOperatore.equals(myTipo) && codOperatore.equals(myCodOperatore));
		}
		return possoModificare;
	}

	@Override
	protected void afterSetStatoWait() {
		String punto = ver + "afterSetStatoWait ";
		super.afterSetStatoWait();
		logger.trace(punto + " afterStatoWait ");
	}

	private void verificaData() {
		String punto = ver + "verificaData ";
		ISASRecord dbrSegnalazione = null;
		if (ManagerDate.validaData(key_data)) {
			logger.trace(punto + " verifico se è stata già inserita una segnalazione con questa data  ");
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, key_cartella.getValue() + "");
			dati.put("data_diag", key_data.getValueForIsas());
			try {
				dbrSegnalazione = myEJB.queryKey(CaribelSessionManager.getInstance().getMyLogin(), dati);
			} catch (SQLException e) {
				logger.error(punto + " Errore nel recupera le diagnosi ", e);
			}
		} else {
			logger.trace(punto + " Data non presente uso quella attuale");
			key_data.setValue(procdate.getDate());
		}
		if (dbrSegnalazione == null) {
			logger.trace(punto + " Non esiste diagnosi in questa data, usa tale data ");
		} else {
			logger.trace(punto + " esiste gia un record con la stessa data, propongo quella di oggi ");
			key_data.setValue(procdate.getDate());
		}
	}

	@Override
	protected void doLoadGrid() throws Exception {
		String punto = ver + "doLoadGrid ";
		hParameters.putAll(getOtherParametersString());
		hParameters.put(CostantiSinssntW.CTS_SEGNALAZIONE_SONO_SO, new Boolean(isContainerSO));
		super.doLoadGrid();
		logger.trace(punto + " possoModificare>>" +possoModificareSegnalazioni+"<");
	}

	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		logger.debug(punto + "inzio ");
		if (ISASUtil.valida(valIdSkso)){
			idskso.setText(valIdSkso);
		}
		
//		if(!isContainerSO){
//			vista_so.setText(Costanti.CTS_SEGNALAZIONE_VISTA_DA_SO_INSERITA_UPDATE);
//		}
		tipoOperatore.setText(getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE));
		codOperatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		
		return true;
	}

	@Override
	protected Map<String, String> getOtherParametersString() {
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put("n_cartella", nCartella);
		return ret;
	}

}
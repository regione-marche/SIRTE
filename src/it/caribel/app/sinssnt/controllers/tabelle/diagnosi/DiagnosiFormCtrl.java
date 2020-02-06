package it.caribel.app.sinssnt.controllers.tabelle.diagnosi;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.time.DateUtils;
import org.zkoss.idom.Item;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;
import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.sinssnt.controllers.ContainerFisioterapicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerGenericoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerInfermieristicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.contattoFisioterapico.ContattoFisioFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoGenerico.ContattoGenFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoInfermieristico.ContattoInfFormCtrl;
import it.caribel.app.sinssnt.controllers.contattoMedico.ContattoMedicoFormCtrl;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

public class DiagnosiFormCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;

	public static String myKeyPermission = "DIAGNOSI";
	private DiagnosiEJB myEJB = new DiagnosiEJB();

	public static final String myPathFormZul = "/web/ui/sinssnt/tabelle/diagnosi/diagnosiForm.zul";

	private CaribelIntbox key_cartella;
	private CaribelDatebox key_data_apertura;
	private CaribelDatebox key_data_chiusura;
	private CaribelDatebox key_data;
	
	private CaribelTextbox operatore;
	private CaribelTextbox desc_oper;

	String nCartella = "";
	Date dtApertura = null;
	Date dtChiusura = null;

	private static final String ver = "5-";

	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("queryStoricoDiag");

			// Chiave gestita automaticamente quindi non editabili
			key_cartella.setReadonly(true);
			if (arg.get(Costanti.N_CARTELLA) != null) {
				CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
				if (containerCorr instanceof ContainerFisioterapicoCtrl) {
					nCartella = ""+arg.get(Costanti.N_CARTELLA);
					dtApertura = (Date) arg.get(ContattoFisioFormCtrl.CTS_DATA_APERTURA);
					dtChiusura = (Date) arg.get(ContattoFisioFormCtrl.CTS_DATA_CHIUSURA);
				}else if (containerCorr instanceof ContainerMedicoCtrl){
					nCartella = ((Integer) arg.get(Costanti.N_CARTELLA)).toString();
					dtApertura = (Date) arg.get(ContattoMedicoFormCtrl.CTS_DATA_APERTURA);
					if (arg.get(ContattoMedicoFormCtrl.CTS_DATA_CHIUSURA)!=null){
						dtChiusura = (java.sql.Date) arg.get(ContattoMedicoFormCtrl.CTS_DATA_CHIUSURA);
					}
				}else if (containerCorr instanceof ContainerInfermieristicoCtrl){
					nCartella = ""+arg.get(Costanti.N_CARTELLA);
					dtApertura = (Date) arg.get(ContattoInfFormCtrl.CTS_DATA_APERTURA);
					if (arg.get(ContattoInfFormCtrl.CTS_DATA_CHIUSURA)!=null){
						dtChiusura = (java.sql.Date) arg.get(ContattoInfFormCtrl.CTS_DATA_CHIUSURA);
					}
				}else if (containerCorr instanceof ContainerGenericoCtrl){
					nCartella = ""+arg.get(Costanti.N_CARTELLA);
					dtApertura = (Date) arg.get(ContattoGenFormCtrl.CTS_DATA_APERTURA);
					if (arg.get(ContattoGenFormCtrl.CTS_DATA_CHIUSURA)!=null){
						dtChiusura = (java.sql.Date) arg.get(ContattoGenFormCtrl.CTS_DATA_CHIUSURA);
					}
				}
				else {
					nCartella = ""+arg.get(Costanti.N_CARTELLA);
					dtApertura = (Date) arg.get(Costanti.DATA_APERTURA);
					if (arg.get(Costanti.DATA_CHIUSURA)!=null){
						dtChiusura = (java.sql.Date) arg.get(Costanti.DATA_CHIUSURA);
					}
				}
			}
			key_cartella.setText(nCartella);
			key_data_apertura.setValue(dtApertura);
			key_data_chiusura.setValue(dtChiusura);
			doLoadGrid();
			key_data_apertura.setReadonly(true);
			key_data_chiusura.setDisabled(true);

		} catch (Exception e) {
			doShowException(e);
		}
	}
	
	public void onClick$btn_formgrid_copy() {
		try{
			if(this.clb.getSelectedIndex()==-1){
				UtilForUI.doAlertSelectOneRow();
				return;
			}
			key_data.setValue(null);
			setStato(STATO_INSERT);
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Override
	public void onClick$btn_formgrid_new() {
		try{
			this.clb.setSelectedIndex(-1);
			UtilForBinding.resetForm(myForm,this.parkSetting);
			this.setStato(STATO_INSERT);
			operatore.setText(getProfile().getStringFromProfile("codice_operatore"));
			desc_oper.setText(ManagerDecod.getCognomeNomeOperatore(getProfile()));
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	@Override
	protected void afterSetStatoInsert(){
		String punto= ver + "afterSetStatoInsert ";
		logger.trace(punto + " disabilitare il pulsante duplicare>>");
//		logger.trace(punto + ">> " + ((Window) self.getAttributes())+"< ");
		CaribelDatebox dataContattoMedico = (CaribelDatebox) ((Window) self.getSpaceOwner()).getParent().getFellowIfAny(Costanti.CTS_SKM_DATA_APERTURA);
		if (dataContattoMedico == null){
			logger.trace(punto + " non ho il conttato medico, recupero della SO ");
			CaribelDatebox dataContattoSO = (CaribelDatebox) ((Window) self.getSpaceOwner()).getParent().getFellowIfAny(Costanti.CTS_PR_DATA_PUAC);
			CaribelIntbox idSkSo = (CaribelIntbox) ((Window) self.getSpaceOwner()).getParent().getFellowIfAny(Costanti.CTS_ID_SKSO);
			if (dataContattoSO != null && (idSkSo !=null && idSkSo.getValue()!=null && idSkSo.getValue()>=0)){
				logger.trace(punto + "Inserisco la data della SO");
				key_data.setValue(dataContattoSO.getValue());
			}else {
				CaribelDatebox dataRichiestaMMGPLS = (CaribelDatebox)((Window)self.getSpaceOwner()).getParent().getFellowIfAny(Costanti.RICH_MMG_DATA_RICHIESTA);
				if(dataRichiestaMMGPLS !=null){
					logger.trace(punto + "Inserisco la data delle richiesta mmg ");
					key_data.setValue(dataRichiestaMMGPLS.getValue());
				}
			}
		}else {
			logger.trace(punto + "Inserisco la data della SO");
			key_data.setValue(dataContattoMedico.getValue());
		}
		verificaData();
	}
	
	@Override
	protected void afterSetStatoUpdate() {
		super.afterSetStatoUpdate();
	}

	@Override
	protected void afterSetStatoWait() {
		String punto = ver + "afterSetStatoWait ";
		super.afterSetStatoWait();
		logger.trace(punto + " afterStatoWait ");
	}

	private void verificaData() {
		String punto = ver  + "verificaData ";
		ISASRecord dbrDiagnosi = null;
		if (ManagerDate.validaData(key_data)) {
			logger.trace(punto + " verifico se è stata già inserita patologia con questa data  ");
			Hashtable dati = new Hashtable();
			dati.put(Costanti.N_CARTELLA, key_cartella.getValue()+"");
			dati.put("data_diag", key_data.getValueForIsas());
			DiagnosiEJB diagnosiEJB = new DiagnosiEJB();
			try {
				 dbrDiagnosi = diagnosiEJB.queryKey(CaribelSessionManager.getInstance().getMyLogin(), dati);
			} catch (SQLException e) {
				logger.error(punto + " Errore nel recupera le diagnosi ", e);
			}
		}else {
			logger.trace(punto + " Data non presente uso quella attuale");
			key_data.setValue(procdate.getDate());
		}
		if (dbrDiagnosi==null){
			logger.trace(punto + " Non esiste diagnosi in questa data, usa tale data ");
		}else {
			logger.trace(punto + " esiste gia un record con la stessa data, propongo quella di oggi ");
			key_data.setValue(procdate.getDate());
		}
	}

	protected void notEditable() {
		operatore.setDisabled(true);
		desc_oper.setDisabled(true);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl#doLoadGrid()
	 * Sovrascrivo il metodo perche la query ha bisogno dei campi dtApertura e
	 * dtChiusura che non fanno parte dell'ISASRecord
	 */
	@Override
	protected void doLoadGrid() throws Exception {
		hParameters.putAll(getOtherParametersString());
		// REFRESH SULLA LISTA
		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
		clb.getItems().clear();
		clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
	}

	@Override
	protected boolean doValidateForm() {
		logger.debug("doValidateForm");
		Date dataApertura = key_data.getValue();
		Date dataApertura2 = key_data_apertura.getValue();
		if(dataApertura!=null && dataApertura2!=null){
			if (DateUtils.truncate(dataApertura2, Calendar.DATE).after(DateUtils.truncate(dataApertura, Calendar.DATE))) {
				UtilForUI.standardExclamation(Labels.getLabel("diagnosi.msg.data"));
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected Map<String,String> getOtherParametersString() {
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put("n_cartella", nCartella);
		ret.put(key_data_apertura.getDb_name(), key_data_apertura.getValueForIsas());
		ret.put(key_data_chiusura.getDb_name(), key_data_chiusura.getValueForIsas());
		return ret;
	}

}

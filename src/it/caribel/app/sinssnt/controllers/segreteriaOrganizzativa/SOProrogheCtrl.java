package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOSKSoProrogheEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.dateutility;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;

public class SOProrogheCtrl extends SOProrogheBaseCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = SegreteriaOrganizzativaFormCtrl.myKeyPermission;
	private RMSkSOSKSoProrogheEJB myEJB = new RMSkSOSKSoProrogheEJB();
//	public static final String myPathFormZul = "~./ui/sinssnt/segreteriaOrganizzativa/schedaSO_Proroghe.zul";
	Object objNCartella = "";
	Object objIdSkSo = "";

	private CaribelIntbox keyCartella; 
	private CaribelIntbox keyIdSkSo;
	private CaribelIntbox keyIdProroga;
	
	private static String ver = "9-";
	private CaribelDatebox dt_proroga_inizio;
	private CaribelDatebox dt_proroga_fine;
	private CaribelDatebox dt_proroga_uvi;
	private CaribelCombobox cbx_pr_revisione;
	
	protected void doInitGridForm() {
		String punto = ver +"doInitGridForm ";
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			super.setMethodNameForInsert("insert");
			super.setMethodNameForUpdate("update");
			super.setMethodNameForQueryKey("queryKey");
			super.setMethodNameForDelete("delete");

			doPopulateCombobox();
			
			dt_proroga_inizio.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) {
					aggiungiMesi(dt_proroga_inizio, dt_proroga_fine);
				}
			});		
			
			dt_proroga_inizio.addEventListener(Events.ON_BLUR, new EventListener<Event>() {
				public void onEvent(Event event) {
					aggiungiMesi(dt_proroga_inizio, dt_proroga_fine);
					calcolaDtFineUvi(dt_proroga_inizio);
				}
			});		
			
			dt_proroga_fine.addEventListener(Events.ON_BLUR, new EventListener<Event>() {
				public void onEvent(Event event) {
					aggiungiMesi(dt_proroga_inizio, dt_proroga_fine);
					calcolaDtFineUvi(dt_proroga_inizio);
				}
			});
			
			
			try {
				cbx_pr_revisione = ((CaribelCombobox)((Component)self.getParent().getSpaceOwner()).getFellowIfAny("cbx_pr_revisione", true));
			} catch (Exception e) {
				logger.error(punto + " Errore nel recupera la data ", e);
			}
			
			if (arg.get("n_cartella") == null) {
				leggiDatiContainer();
				doLoadGrid();
			} else {

			}
			abilitaMaschera();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	
	public  void aggiungiMesi(CaribelDatebox dtInizio, CaribelDatebox dtFine){
		String punto = ver + "aggiungiMesi ";
		logger.trace(punto + "Inizio>>");
		if(!ManagerDate.validaData(dtFine) && ManagerDate.validaData(dtInizio)){
			int mesi = 12;
			String dataInizio = dtInizio.getValueForIsas();
			dateutility dtDateutility = new dateutility();
			Date vdtFine = dtDateutility.getDataNMesi(mesi, dataInizio);
//			logger.trace(punto + " data>>" +vdtFine);
			dtFine.setValue(vdtFine);
		}
	}
	
	
	private void leggiDatiContainer() throws Exception {
		CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
		if (containerCorr !=null) {
			objNCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA);
			objIdSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
		} else {
			throw new Exception("Reperimento chiavi Proroghe non riuscito!");
		}
		if (objNCartella!=null){
			keyCartella.setValue(Integer.parseInt(objNCartella+""));
		}
		if (objIdSkSo!=null && ISASUtil.valida(objIdSkSo+"")){
			keyIdSkSo.setValue(Integer.parseInt(objIdSkSo+""));
		}
	}

	private void abilitaMaschera() {
		String punto = ver + "";
		logger.debug(punto + " dati ");
		
//		dt_presa_carico.setReadonly(true);
//		dt_chiusura.setReadonly(true);
	}

	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + "");
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
				
				Events.sendEvent("onUpdateProrogheSospensioni", (Component) self.getParent().getSpaceOwner(), null);

			} else {
				logger.debug(" \n Non effettuo il caricamento della griglia ");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		boolean possoSalvare = true;
		keyCartella.setText(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"");
		CaribelIntbox IdSkSo = (CaribelIntbox) self.getParent().getFellow(CostantiSinssntW.CTS_ID_SKSO);
		keyIdSkSo.setValue(IdSkSo.getValue());
		
		String ggDaProroga= getProfile().getStringFromProfile(ManagerProfile.GG_DA_PROROGA);
 		if(!ggDaProroga.equals(CostantiSinssntW.NO)){
			//Verifico che la proroga inizi entro x giorni da oggi dove x Ã¨ cnfigurato in (ManagerProfile.GG_DA_PROROGA)
			int gg_da_proroga = Integer.parseInt(ggDaProroga);
			Calendar cal = Calendar.getInstance();
			cal.setTime(dt_proroga_inizio.getValue());
			cal.add(Calendar.DAY_OF_MONTH, -gg_da_proroga);
			possoSalvare = cal.getTime().before(new Date());
			if(!possoSalvare){
				UtilForUI.standardExclamation(Labels.getLabel("segreteria.organizzativa.scheda.proroghe.messaggio.limiteGGEcceduto", new String[]{gg_da_proroga+""}));
				return false;
			}
		}		
		CaribelDatebox data_fine = (CaribelDatebox) self.getParent().getFellowIfAny("data_fine");
		Label lb_dataFine = (Label)self.getParent().getFellowIfAny("lb_dataFine");
		Label lb_dtProrogaInizio = (Label)self.getFellowIfAny("lb_dtProrogaInizio");
		possoSalvare = ManagerDate.controllaPeriodo(self, data_fine, dt_proroga_inizio, lb_dataFine,lb_dtProrogaInizio);
		
		if (possoSalvare){
			possoSalvare = ManagerDate.controllaPeriodo(self, dt_proroga_inizio, dt_proroga_fine, "lb_dtProrogaInizio","lb_dtProrogaFine");
		}
		logger.trace(punto + " posso salvare>>"+ possoSalvare);
		if (possoSalvare){
			possoSalvare = (!esisteSovrapposizione());

			if (!possoSalvare) {
				UtilForUI.standardExclamation(Labels.getLabel("segreteria.organizzativa.scheda.proroghe.messaggio.sovrapposizione"));
//			return false;
			}
		}
		return possoSalvare;
	}

	private boolean esisteSovrapposizione() {
		boolean esisteSovrapposizione = false;
		Hashtable dati = new Hashtable();
		dati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getText());
		dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue()+"");
		if (keyIdProroga.getValue()!=null){
			dati.put(CostantiSinssntW.CTS_SO_ID_PROROGA, keyIdProroga.getValue()+"");
		}
		dati.put(CostantiSinssntW.CTS_DT_PROROGA_INIZIO, dt_proroga_inizio.getValueForIsas());
		dati.put(CostantiSinssntW.CTS_DT_PROROGA_FINE, dt_proroga_fine.getValueForIsas());
		 
		esisteSovrapposizione = myEJB.esisteSovrapposizione(CaribelSessionManager.getInstance().getMyLogin(), dati); 
		return esisteSovrapposizione;
	}

	public void doPublicLoadGrid(String nCartella, String idSkso) throws Exception {
		String punto = ver + "doPublicLoadGrid ";
		logger.trace(punto + " dati>>" +nCartella+"<< idSkso>>"+ idSkso+"<<");
		keyCartella.setText(nCartella);
		keyIdSkSo.setText(idSkso);
		
		doLoadGrid();
	}
	
	protected void afterSetStatoInsert() {
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " verifico la data ");
		try {
			if (clb != null && clb.getModel() != null && clb.getModel().getSize() > 0) {
				String dataFine = recuperaDataFine();
				logger.trace(punto + " datafine>>" + dataFine);
				Date dtFine = ManagerDate.getDate(dataFine);
				Calendar cal = Calendar.getInstance();
				cal.setTime(dtFine);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				dt_proroga_inizio.setValue(cal.getTime());
			} else {
				CaribelDatebox data_fine = (CaribelDatebox) self.getParent().getFellow("data_fine");
				if (!ManagerDate.validaData(data_fine)) {
					UtilForUI.standardExclamation(Labels
							.getLabel("segreteria.organizzativa.scheda.proroghe.msg.data.fine.periodo"));
					try {
						setStato(STATO_WAIT);
					} catch (ISASMisuseException e) {
						doShowException(e);
					}
				} else {
					logger.debug(punto + " data valida >>" + data_fine.getValue() + "<");
					Calendar cal = Calendar.getInstance();
					cal.setTime(data_fine.getValue());
					cal.add(Calendar.DAY_OF_MONTH, 1);
					dt_proroga_inizio.setConstraint("after " + UtilForComponents.formatDateforDatebox(cal.getTime()));
					dt_proroga_inizio.setValue(cal.getTime());
				}
			}
			aggiungiMesi(dt_proroga_inizio, dt_proroga_fine);
			calcolaDtFineUvi(dt_proroga_inizio);
		} catch (Exception e) {
			logger.error(punto + " Errore date non recuperate correttamente ");
		}
	}

	private void calcolaDtFineUvi(CaribelDatebox dtProrogaInizio) {
		String punto = ver + "calcolaDtFineUvi ";
		logger.trace(punto + " Inizio con dati ");
		if (ManagerDate.validaData(dtProrogaInizio) && cbx_pr_revisione!=null ){
			int codRevisione = 0 ;
			String dtInizio = dtProrogaInizio.getValueForIsas();
			try {
				codRevisione = Integer.parseInt(cbx_pr_revisione.getSelectedValue());
			} catch (Exception e) {
			}
			String dataNew = SegreteriaOrganizzativaFormCtrl.calcolaDataRevisione(codRevisione, dtInizio);
			if (ManagerDate.validaData(dataNew)){
				dt_proroga_uvi.setValue(ManagerDate.getDate(dataNew));
			}else {
				dt_proroga_uvi.setValue(dt_proroga_fine.getValue());
			}
		}
	}

	private String recuperaDataFine() {
		String punto = ver+ "recuperaDataFine ";
		String maxDataFine = "";
		int ultimo = ((CaribelListModel<Object>) this.clb.getModel()).size() - 1;
		if (ultimo >= 0) {
			CaribelListModel list =(CaribelListModel)clb.getModel();
			for (int i = 0; i < list.size(); i++) {
				Object myData = ((CaribelListModel<Object>) this.clb.getModel()).get(i);
				Hashtable<String, ?> data = new Hashtable<String, String>();
				if (myData instanceof ISASRecord) {
					data = ((ISASRecord) myData).getHashtable();
				} else if (myData instanceof Hashtable) {
					data = (Hashtable<String, ?>) myData;
				}
				logger.debug(punto + " dati>" + data + "<");
				String dataFine = ISASUtil.getValoreStringa(data, "dt_proroga_fine");
				if (ManagerDate.validaData(maxDataFine) && ManagerDate.validaData(dataFine) && ManagerDate.confrontaDate(maxDataFine, dataFine)){
					maxDataFine = dataFine;
				}else {
					maxDataFine = dataFine;
				}
			}
		}
		return maxDataFine;
	}

	protected void notEditable() {
		//se le date sono state impostate ho generato i piani e aggiornato le agende quindi posso solo cambiare le note
		UtilForBinding.setComponentReadOnly(dt_proroga_inizio, dt_proroga_inizio.getValue()!=null);
		UtilForBinding.setComponentReadOnly(dt_proroga_fine, dt_proroga_fine.getValue()!=null);
	}
	
}

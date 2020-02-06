package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOSKSoSospensioniEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Messagebox;

public class SOSospensioneCtrl extends SOSospensioneBaseCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = SegreteriaOrganizzativaFormCtrl.myKeyPermission;
	private RMSkSOSKSoSospensioniEJB myEJB = new RMSkSOSKSoSospensioniEJB();
//	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/schedaSO_Sospensione.zul";
//	Object objNCartella = "";
//	Object objIdSkSo = "";

//	private CaribelIntbox keyCartella;
//	private CaribelIntbox keyIdSkSo;
//	
//	private CaribelDatebox dt_sospensione_inizio;
//	private CaribelDatebox dt_sospensione_fine;
//	
//	private CaribelCombobox cbx_motivo_sospensione;
//	
	private String ver = "3-";
	private CaribelIntbox keyIdSospensione;
	
	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			super.setMethodNameForInsert("insert");
			super.setMethodNameForUpdate("update");
			super.setMethodNameForQueryKey("queryKey");
			super.setMethodNameForDelete("delete");

			doPopulateCombobox();
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

	private void abilitaMaschera() {
//		dt_presa_carico.setReadonly(true);
//		dt_chiusura.setReadonly(true);
	}

//	private void doPopulateCombobox() throws Exception {
//		String punto = ver + "doPopulateCombobox \n";
//		logger.debug(punto + "");
//		
//		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
//		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare
//
//		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
//		h_xCBdaTabBase.put(CostantiSinssntW.TAB_VAL_SO_MOTIVO_SOSPENSIONE, cbx_motivo_sospensione);
//		
//		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
//				"tab_descrizione", false);
//
//		//		caricaComboDistretti();
//		
//	}

//	private void caricaComboDistretti() {
//		try{
//			CaribelComboRepository.comboPreLoad("query_distretti", new DistrettiEJB(), "query", 
//					new Hashtable<String, String>(), cbx_cod_distretto, null, "cod_distr", "des_distr", false);
//			
//		}catch(Exception e){
//			logger.error("caricamento combo REGION fallito! - Eccez=" + e);
//		}
//		
//	}

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
//		keyIdSkSo.setText(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO)+"");
//		JCariDateTextFieldPianoAss = (CaribelDatebox) self.getParent().getFellow("JCariDateTextFieldPianoAss");
		
		possoSalvare = controllaDataInizioCoerente();
		logger.trace(punto + " posso controllaDataInizioCoerente>>"+ possoSalvare);
		if (!possoSalvare) {
			return false;
		}		
		
		CaribelIntbox IdSkSo = (CaribelIntbox) self.getParent().getFellow(CostantiSinssntW.CTS_ID_SKSO);
		keyIdSkSo.setValue(IdSkSo.getValue());
		possoSalvare = ManagerDate.controllaPeriodo(self, dt_sospensione_inizio,dt_sospensione_fine , "lb_dt_sospensione_inizio","lb_dt_sospensione_fine");
		possoSalvare = (!esisteSovrapposizione());
		logger.trace(punto + " posso salvare>>"+ possoSalvare);
		if (!possoSalvare) {
			UtilForUI.standardExclamation(Labels.getLabel("segreteria.organizzativa.scheda.sospensioni.messaggio.sovrapposizione"));
			return false;
		}			
		return possoSalvare;
	}

	private boolean controllaDataInizioCoerente() {
		String punto = ver + "controllaDataInizioCoerente ";
		boolean dtInizioCoeerente = true;
		
		CaribelDatebox dtPresaCarico = (CaribelDatebox) self.getParent().getFellow(Costanti.CTS_PR_DATA_PUAC);
		if (!ManagerDate.controllaPeriodo(dtPresaCarico.getValue(), dt_sospensione_inizio.getValue())){
			String[] sost = new String[]{ManagerDate.formattaDataIta(dtPresaCarico.getValueForIsas(), "/")};
			String messaggio = Labels.getLabel("segreteria.organizzativa.scheda.sospensioni.messaggio.dt.sospensione.maggiore.apertura.scheda",sost);
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.QUESTION);
			dtInizioCoeerente = false;
		}
		if (dtInizioCoeerente) {
			CaribelDatebox dataInizioPiano = (CaribelDatebox) self.getParent().getFellow(
					Costanti.CTS_SKSO_MMG_DATA_INIZIO);
			if (!ManagerDate.controllaPeriodo(dataInizioPiano.getValue(), dt_sospensione_inizio.getValue())) {
				String[] sost = new String[] { ManagerDate.formattaDataIta(dataInizioPiano.getValueForIsas(), "/") };
				String messaggio = Labels
						.getLabel(
								"so.scheda.sospensioni.messaggio.dt.sospensione.maggiore.inizio.piano",
								sost);
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.QUESTION);
				dtInizioCoeerente = false;
			}
		}
		
		logger.trace(punto + " dtInizioCoeerente >>" + dtInizioCoeerente+"<");
		
		return dtInizioCoeerente;
	}

	private boolean esisteSovrapposizione() {
		boolean esisteSovrapposizione = false;
		Hashtable dati = new Hashtable();
		dati.put(CostantiSinssntW.N_CARTELLA, keyCartella.getText());
		dati.put(CostantiSinssntW.CTS_ID_SKSO, keyIdSkSo.getValue()+"");
		if (keyIdSospensione.getValue()!=null){
			dati.put(CostantiSinssntW.CTS_SO_ID_SOSPENSIONE, keyIdSospensione.getValue()+"");
		}
		dati.put(CostantiSinssntW.CTS_DT_SOSPENSIONE_INIZIO, dt_sospensione_inizio.getValueForIsas());
		dati.put(CostantiSinssntW.CTS_DT_SOSPENSIONE_FINE, dt_sospensione_fine.getValueForIsas());
		
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

	public void leggiDatiContainer() {
		CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
		if (containerCorr !=null) {
			objNCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA);
			objIdSkSo = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO);
		} else {
		//	throw new Exception("Reperimento chiavi Proroghe non riuscito!");
		}
		if (objNCartella!=null){
			keyCartella.setValue(Integer.parseInt(objNCartella+""));
		}
		if (objIdSkSo!=null && ISASUtil.valida(objIdSkSo+"")){
			keyIdSkSo.setValue(Integer.parseInt(objIdSkSo+""));
		}
	}
	protected void notEditable() {
		//se le date sono state impostate ho generato i piani e aggiornato le agende quindi posso solo cambiare le note
		UtilForBinding.setComponentReadOnly(dt_sospensione_inizio, dt_sospensione_inizio.getValue()!=null);
		UtilForBinding.setComponentReadOnly(dt_sospensione_fine, dt_sospensione_fine.getValue()!=null);
	}
}

package it.caribel.app.sinssnt.controllers.contattoMedico;

import it.caribel.app.sinssnt.bean.modificati.SkMedEJB;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import java.util.Vector;

public class DatiTerapiaCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;
	private String myKeyPermission = ContattoMedicoFormCtrl.myKeyPermission;
	private SkMedEJB myEJB = new SkMedEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/contatto_medico/contatto_medico_terapia.zul";
	String nCartella = "";
	String nContatto = "";

	private CaribelTextbox keyCartella;
	private CaribelTextbox keyContatto;
	
	private CaribelDatebox skt_data_inizio;
	private CaribelDatebox skt_data_fine;
	
	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query_terapia");
			super.setMethodNameForInsert("insert_terapia");
			super.setMethodNameForUpdate("update_terapia");
			super.setMethodNameForQueryKey("queryKeyTerapia");
			super.setMethodNameForDelete("delete_terapia");

			if (arg.get("n_cartella") == null) {
				CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
				if (containerCorr instanceof ContainerMedicoCtrl) {
					nCartella = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) containerCorr).hashChiaveValore,
							CostantiSinssntW.N_CARTELLA);
					nContatto = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) containerCorr).hashChiaveValore,
							CostantiSinssntW.N_CONTATTO);
				} else {
					throw new Exception("Reperimento chiavi Diagnosi non riuscito!");
				}
				doLoadGrid();
			} else {

			}
		} catch (Exception e) {
			doShowException(e);
		}
	}

	@Override
	protected void doLoadGrid() throws Exception {
		 
		if (ISASUtil.valida(keyCartella.getValue()) && ISASUtil.valida(keyContatto.getValue())) {
			hParameters.putAll(getOtherParametersString());
			this.hParameters.put(CostantiSinssntW.N_CARTELLA,keyCartella.getValue()+"");
			this.hParameters.put(CostantiSinssntW.N_CONTATTO,keyContatto.getValue()+"");
			Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
			clb.getItems().clear();
			clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
		}else {
			logger.debug(" \n Non effettuo il caricamento della griglia ");
		}
	}

	@Override
	protected boolean doValidateForm() {
		keyCartella.setValue(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"");
		keyContatto.setValue(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CONTATTO)+"");
		boolean periodoConforme = ManagerDate.controllaPeriodo(self, skt_data_inizio, skt_data_fine, "sktdatainizio","sktdatafine");
				
		return periodoConforme;
	}
	
}

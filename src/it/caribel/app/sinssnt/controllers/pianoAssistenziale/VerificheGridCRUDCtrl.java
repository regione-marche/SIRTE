package it.caribel.app.sinssnt.controllers.pianoAssistenziale;

import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;

public class VerificheGridCRUDCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;

	private CaribelTextbox JCariTextFieldTipoOper;
	private CaribelTextbox JCariTextFieldNProgetto;
	private CaribelTextbox JCariTextFieldCodObiettivo;
	private CaribelDatebox JCariDateTextFieldPianoAss;
	private CaribelTextbox JCariTextFieldNIntervento;

	public static String myKeyPermission = "PIANOVER";
	private PianoAssistEJB myEJB = new PianoAssistEJB();

	private CaribelTextbox cartella;

	private CaribelDatebox int_data;

//	public static final String myPathFormZul = "/web/ui/sinssnt/tabelle/diagnosi/diagnosiForm.zul";

//	private CaribelIntbox key_cartella;
//	private CaribelDatebox key_data_apertura;
//	private CaribelDatebox key_data_chiusura;
//	private CaribelDatebox key_data;
//	
//	private CaribelTextbox operatore;
//	private CaribelTextbox desc_oper;
//	
//	private Button btn_duplica;
//
//	String nCartella = "";
//	Date dtApertura = null;
//	Date dtChiusura = null;

	protected void doInitGridForm() {
		try {
			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQueryKey("queryKey_pianoVer");
			super.setMethodNameForQuery("query_pianoVer");
			super.setMethodNameForDelete("delete_pianoVer");
			super.setMethodNameForInsert("insert_pianoVer");
			super.setMethodNameForUpdate("update_pianoVer");

			JCariTextFieldTipoOper = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldTipoOper");    
			JCariTextFieldNProgetto = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldNProgetto");   
			JCariTextFieldCodObiettivo = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldCodObiettivo");
			JCariDateTextFieldPianoAss = (CaribelDatebox) self.getParent().getFellow("JCariDateTextFieldPianoAss");
			JCariTextFieldNIntervento = (CaribelTextbox) self.getParent().getFellow("JCariTextFieldNIntervento"); 
			
			// Chiave gestita automaticamente quindi non editabili
//			key_cartella.setReadonly(true);
//			if (arg.get("n_cartella") != null) {
//				CaribelContainerCtrl containerCorr = UtilForContainer
//						.getContainerCorr();
//				if (containerCorr instanceof ContainerFisioterapicoCtrl) {
//					nCartella = ((Integer) arg.get(Costanti.N_CARTELLA)).toString();
//					dtApertura = (Date) arg.get(ContattoFisioFormCtrl.CTS_DATA_APERTURA);
//					dtChiusura = (Date) arg.get(ContattoFisioFormCtrl.CTS_DATA_CHIUSURA);
//				}else if (containerCorr instanceof ContainerMedicoCtrl){
//					nCartella = ((Integer) arg.get(Costanti.N_CARTELLA)).toString();
//					dtApertura = (Date) arg.get(ContattoMedicoFormCtrl.CTS_DATA_APERTURA);
//					if (arg.get(ContattoMedicoFormCtrl.CTS_DATA_CHIUSURA)!=null){
//						dtChiusura = (java.sql.Date) arg.get(ContattoMedicoFormCtrl.CTS_DATA_CHIUSURA);
//					}
//				}else if (containerCorr instanceof ContainerInfermieristicoCtrl){
//						nCartella = ((Integer) arg.get(Costanti.N_CARTELLA)).toString();
//						dtApertura = (Date) arg.get(ContattoInfFormCtrl.CTS_DATA_APERTURA);
//						if (arg.get(ContattoInfFormCtrl.CTS_DATA_CHIUSURA)!=null){
//							dtChiusura = (java.sql.Date) arg.get(ContattoInfFormCtrl.CTS_DATA_CHIUSURA);
//						}
//					}
//					else {
//						throw new Exception(
//								"Reperimento chiavi Diagnosi non riuscito!");
//					}
//				}
//				key_cartella.setText(nCartella);
//				key_data_apertura.setValue(dtApertura);
//				key_data_chiusura.setValue(dtChiusura);
//				doLoadGrid();
//				key_data_apertura.setReadonly(true);
//				key_data_chiusura.setDisabled(true);
				
		} catch (Exception e) {
			doShowException(e);
		}
	}

//	@Override
//	protected void doLoadGrid() throws Exception {
//		hParameters.putAll(getOtherParametersString());
//		// REFRESH SULLA LISTA
//		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//		clb.getItems().clear();
//		clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
//	}
	
	@Override
	protected Map<String,String> getOtherParametersString() {
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put(JCariTextFieldTipoOper.getDb_name(), JCariTextFieldTipoOper.getValue());
		ret.put(cartella.getDb_name(), cartella.getValue().toString());
		ret.put(JCariTextFieldNProgetto.getDb_name(), JCariTextFieldNProgetto.getValue());
		ret.put(JCariTextFieldCodObiettivo.getDb_name(), JCariTextFieldCodObiettivo.getValue());
		ret.put(JCariTextFieldNIntervento.getDb_name(), JCariTextFieldNIntervento.getValue());
		ret.put(JCariDateTextFieldPianoAss.getDb_name(), JCariDateTextFieldPianoAss.getValueForIsas());
		return ret;
	}

	@Override
	protected boolean doValidateForm() {
		try {
			if(!UtilForComponents.testRequiredFieldsNoCariException(((Component)self.getParent().getSpaceOwner()))){
				return false;
			}
		} catch (Exception e) {
			doShowException(e);
			return false;
		}
		if(JCariDateTextFieldPianoAss.getValue()==null){
			UtilForUI.standardInfo(Labels.getLabel("common.msg.NoOrderDate.data1maggioreUgualeDi0"));
			return false;
		}
		if(!int_data.getValue().before(JCariDateTextFieldPianoAss.getValue()))
			return true;
		else {
			UtilForUI.standardExclamation(Labels.getLabel("common.msg.NoOrderDate.data1maggioreUgualeDi0", new String[]{"definizione piano assistenziale", "verifica" }));
			return false;
		}
	}

}

package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.IntpreEJB;
//import it.caribel.app.sinssnt.bean.modificati.CommissUVMEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDecimalbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForUI;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Vlayout;


public class SOVerbaleUvmGridFormCtrl extends CaribelGridFormCtrl {

	private static final long serialVersionUID = 6570378279356476533L;
	private Vlayout sceltaPrestazioneFarmaco;
	private CaribelTextbox cod_prestazione;
	private CaribelTextbox pre_des_dett;
	private Component prestazione;
	private AbstractComponent searchCaseManager;
	private CaribelDecimalbox pre_importo;
	private CaribelIntbox sp_quantita;
	private CaribelTextbox pre_note;
	private CaribelTextbox tipo_prestazione;
	private CaribelCombobox cbx_pr_cod_comm;
	private String ver = "4-";
	
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	
	protected boolean doValidateForm(){
		boolean valida = true;
		
//		if(cod_prestazione==null){
//			cod_prestazione = (CaribelTextbox) sceltaPrestazioneFarmaco.getFellowIfAny("cod_prestazione", true);
//		}
//		return cod_prestazione.getValue() != null && !cod_prestazione.getValue().isEmpty(); 
		return valida;
	}

	protected void doInitGridForm(){
		String punto = ver  + "doInitGridForm ";
		logger.trace(punto );
		try {
			loadComboBoxCommissioneUvm(cbx_pr_cod_comm);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		doCaricaComboDistretti();
		abilitaMaschera();
//		if(tipo_prestazione == null){
//			tipo_prestazione = (CaribelTextbox) self.getParent().getFellowIfAny("tipo_prestazione", true);
//		}
//		prestazioneCtrl.putLinkedSearchObjects("prest_tipo", tipo_prestazione);
	}
	
	private void abilitaMaschera() {
		String punto = ver + "abilitaMaschera ";
		CaribelSearchCtrl careManagerSearch = (CaribelSearchCtrl) searchCaseManager.getAttribute(MY_CTRL_KEY);
		boolean abilitareSeachCaseManager = false;
		try {
			CaribelCheckbox ceckBox= (CaribelCheckbox) self.getParent().getFellowIfAny("case_manager_mmg", true);
			abilitareSeachCaseManager = ceckBox.isChecked();
		} catch (Exception e) {
			e.printStackTrace();
		}
		logger.trace(punto + " checcato >>" +abilitareSeachCaseManager+"<<");
		careManagerSearch.setReadonly(true);
	}

//	private void doCaricaComboDistretti() {
//		try {
//			Component p = self.getFellow("panel_ubicazione");
//			PanelUbicazioneCtrl c = (PanelUbicazioneCtrl)p.getAttribute(MY_CTRL_KEY);
//			c.setDistrettiVoceTutti(false);
//			c.doInitPanel();
//			String codZona =getProfile().getStringFromProfile("zona_operatore"); 
//			if (ISASUtil.valida(codZona)){
//				zona.setValue(codZona);
//			}
//			if (distretto !=null){
//				distretto.setSelectedIndex(0);
//			}
//			c.setVisibleZona(false);
//			c.setVisiblePresidioComuneAreaDis(false);
//			c.setVisibleRaggruppamento(false);
//			c.setVisibleUbicazione(false);
//			c.setDbNameZona(Costanti.CTS_SO_VERBALE_DB_NAME_ZONA);
//			c.setDbNameDistretto(Costanti.CTS_SO_VERBALE_DB_NAME_DISTRETTO);
//		}catch(Exception e){
//			doShowException(e);
//		}
//	}

	private void loadComboBoxCommissioneUvm(CaribelCombobox cbx) throws Exception {
		cbx.clear();
//		CaribelComboRepository.comboPreLoad("query_comuvm", new CommissUVMEJB(), "query", new Hashtable(), cbx, null, "cm_cod_comm",
//				"cm_descr", false);
	}

	public void executeDelete() throws Exception{
		int riga = clb.getSelectedIndex();
		if (riga < 0)
			UtilForUI.doAlertSelectOneRow();
		/*01/12/2006 Controllo che non sia l'ultima prestazione*/
		else {
			if (clb.getItemCount() == 1) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.impossibileCancelareUltimaPrestazione"));
				return;
			}
			int count = (Integer) invokeGenericSuEJB(new IntpreEJB(), (Hashtable<?,?>) clb.getSelectedItem().getAttribute("ht_from_grid"), "delete");
//			int count = db.DeletePres(prestaz);
			if (count != -1) {
				clb.removeItemAt(riga);
//				this.cariStringTableModel1.removeRow(riga);
//				this.JCariTextFieldPrestaz_1.setText("");
//				JCariTextFieldPrestDec.setText("");
//				this.JCariTextFieldPrestDec.setEditable(true);
//				this.JCariTextFieldPrestDec.setDisabled(false);
//				this.JCariTextFieldPrest_num.setText("");
//				this.JCariTextPaneNote.setText("");
//				this.jCariTextPaneDesDett.setText("");
//				this.cariCurrencyTextFieldImporto.putItValue("");
			} else {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.cancellazioneFallita"));
			}
//				new cariInfoDialog(null, "Cancellazione non eseguita!", "Errore!").show();
			//			cancellato = true;
//			JCariTextFieldPrestDec.setDisabled(false);
//			JCariTextFieldPrestDec.setEditable(true);
//			JCariTextFieldPrestaz_1.setDisabled(false);
//			JCariTextFieldPrestaz_1.setEditable(true);
//			JButtonBorderedPrestaz.setDisabled(false);
			/*controllo se non ci sono più prestazioni devo sbiancare
			il campo tipo servizio nascosto. in modo che la combo venga ricaricata
			*/
			//questo caso non si dovrebbe poter verificare se era l'ultima prestazione ho gia impedito che venisse cancellata
//			if (clb.getItemCount() == 0) {
//				this.JCariTextFieldTipoServizio.setValue("");
//				//    caricaComboContatti();
//			}
		}
	}
	
	protected void notEditable() {
		String punto = ver + "notEditable ";
		logger.trace(punto + "");
		
//		pre_des_dett.setReadonly(true);
//		if(clb.getSelectedIndex()>-1){
//			prestazioneCtrl.setReadonly(true);
//		}
		
	}
}

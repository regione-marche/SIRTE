package it.caribel.app.sinssnt.controllers.interventi;

//import it.caribel.app.sins_sm.controllers.sm_psicoFarmaci.SM_PsicoFarmaciSearchCtrl;
//import it.caribel.app.sins_sm.controllers.sm_tabPre.SM_TabPreSearchCtrl;
import it.caribel.app.sinssnt.bean.IntpreEJB;
import it.caribel.zk.composite_components.CaribelDecimalbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForUI;

import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Vlayout;


public class PrestazioniCaribelGridFormCtrl extends CaribelGridFormCtrl {

	private static final long serialVersionUID = 6570378279356476533L;
//	private CaribelRadiogroup tipoPrestazioneFarmaco;
	private Vlayout sceltaPrestazioneFarmaco;
	private CaribelTextbox cod_prestazione;
	private CaribelTextbox pre_des_dett;
	private Component prestazione;
	private CaribelSearchCtrl prestazioneCtrl;
	private CaribelDecimalbox pre_importo;
	private CaribelIntbox sp_quantita;
	private CaribelTextbox pre_note;
	private CaribelTextbox tipo_prestazione;

	protected boolean doValidateForm(){
		if(cod_prestazione==null){
			cod_prestazione = (CaribelTextbox) sceltaPrestazioneFarmaco.getFellowIfAny("cod_prestazione", true);
		}
		return cod_prestazione.getValue() != null && !cod_prestazione.getValue().isEmpty(); 
	}

	protected void doInitGridForm(){
		prestazioneCtrl = (CaribelSearchCtrl) prestazione.getAttribute(MY_CTRL_KEY);
		prestazioneCtrl.putLinkedComponent("pre_importo", pre_importo);
		prestazioneCtrl.putLinkedComponent("pre_numero", sp_quantita);
		prestazioneCtrl.putLinkedComponent("prest_des_dett", pre_des_dett);
		prestazioneCtrl.putLinkedComponent("pre_note", pre_note);
		prestazioneCtrl.setMethodNameForQueryKey("query_ximporto");
		prestazioneCtrl.putLinkedSearchObjects("tipo_oper", "05");
		prestazioneCtrl.putLinkedSearchObjects("provenienza", "interv");
		if(tipo_prestazione == null){
			tipo_prestazione = (CaribelTextbox) self.getParent().getFellowIfAny("tipo_prestazione", true);
		}
		prestazioneCtrl.putLinkedSearchObjects("prest_tipo", tipo_prestazione);
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
			try{
				invokeGenericSuEJB(new IntpreEJB(), (Hashtable<?,?>) clb.getSelectedItem().getAttribute("ht_from_grid"), "delete");
				clb.removeItemAt(riga);
			} catch(Exception e){
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.cancellazioneFallita"));
			}
//				new cariInfoDialog(null, "Cancellazione non eseguita!", "Errore!").show();
			//TODO verificare la notifica
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
		pre_des_dett.setReadonly(true);
		if(clb.getSelectedIndex()>-1){
			prestazioneCtrl.setReadonly(true);
		}
	}
	
	public void executeUpdate() throws Exception{
		boolean save = doSaveInterv();
	}
	public void executeInsert() throws Exception{
		boolean save = doSaveInterv();
	}

	private boolean doSaveInterv() throws Exception {
		AccessiFormCtrl accessi = (AccessiFormCtrl) self.getParent().getSpaceOwner().getAttribute(MY_CTRL_KEY);
		return accessi.doSaveForm();
	}
	
	
}

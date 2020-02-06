package it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm;

import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridFormCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Event;

import it.caribel.zk.generic_controllers.CaribelSearchCtrl;



public class GrigliaOperatoriCtrl extends CaribelGridFormCtrl{

	private static final long serialVersionUID = 1L;
	private AbstractComponent operatoreSearch;
	CaribelTextbox codice_nascosto;
	CaribelTextbox descrizione_qualifica;
	CaribelTextbox cod_operatore;	
	CaribelCombobox desc_operatore;
	CaribelTextbox zona_nascosta;
	CaribelTextbox distretto_nascosto;
	private CaribelCombobox qualifica;
	@Override
	protected void doInitGridForm() {
		try{
			
			populateCombobox();
			CaribelSearchCtrl operatore = (CaribelSearchCtrl) operatoreSearch.getAttribute(MY_CTRL_KEY);
			operatore.putLinkedSearchObjects("tipo", qualifica);
			operatore.putLinkedComponent("tipo", qualifica);
			operatore.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, zona_nascosta);
			operatore.putLinkedComponent(CostantiSinssntW.CTS_OPERATORE_ZONA, zona_nascosta);
			operatore.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto_nascosto);
			operatore.putLinkedComponent(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, distretto_nascosto);
			
		}catch(Exception e){
			doShowException(e);
		}
	}

	public void onSelect$qualifica(Event event){
		String desc_qualifica=qualifica.getText();
		descrizione_qualifica.setText(desc_qualifica);
		cod_operatore.setText("");
		desc_operatore.setText("");
		try{
		}catch(Exception e){
			doShowException(e);
		}	
	}
	protected boolean doValidateForm() {
		return true;
	}	
	@Override
	public void onClick$btn_formgrid_new() {
		try{
			CaribelTextbox codice = (CaribelTextbox)self.getParent().getSpaceOwner().getFellow("codice");
			String strCodice = codice.getText();
			
			CaribelCombobox zona1 = (CaribelCombobox)self.getParent().getSpaceOwner().getFellow("zona");
			String strZona = zona1.getSelectedValue();
			
			CaribelCombobox distr = (CaribelCombobox)self.getParent().getSpaceOwner().getFellow("distretto");
			String strDistr = distr.getSelectedValue();
			
			super.onClick$btn_formgrid_new();
			
			codice_nascosto.setText(strCodice);
			codice.setReadonly(true);
			
			zona_nascosta.setText(strZona);
			zona1.setReadonly(true);
			
			distretto_nascosto.setText(strDistr);
			distr.setReadonly(true);
			//qualifica.setSelectedValue("01");
			
			
			
			
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void onClick$btn_formgrid_edit() {
		try{
			CaribelTextbox codice = (CaribelTextbox)self.getParent().getSpaceOwner().getFellow("codice");
			String strCodice = codice.getText();
			
			CaribelCombobox zona1 = (CaribelCombobox)self.getParent().getSpaceOwner().getFellow("zona");
			String strZona = zona1.getSelectedValue();
			
			CaribelCombobox distr = (CaribelCombobox)self.getParent().getSpaceOwner().getFellow("distretto");
			String strDistr = distr.getSelectedValue();
			
			super.onClick$btn_formgrid_edit();
			
			codice_nascosto.setText(strCodice);
			codice.setReadonly(true);
			
			zona_nascosta.setText(strZona);
			zona1.setReadonly(true);
			
			distretto_nascosto.setText(strDistr);
			distr.setReadonly(true);
			//qualifica.setSelectedValue("01");
			
			
			
			
			
		}catch(Exception e){
			doShowException(e);
		}
	}
	private void populateCombobox()throws Exception {
		ManagerOperatore.loadTipiOperatori(qualifica,true);		
		//qualifica.setSelectedValue("01");
		
	}
}
	
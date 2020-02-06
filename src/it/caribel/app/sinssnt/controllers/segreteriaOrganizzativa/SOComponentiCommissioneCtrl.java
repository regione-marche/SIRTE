package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.zk.generic_controllers.CaribelGridFormCtrl;

public class SOComponentiCommissioneCtrl extends CaribelGridFormCtrl{

	private static final long serialVersionUID = 1L;
	
//	private AbstractComponent caribel_search_presidi;
	private String ver = "1-";
	
	@Override
	protected void doInitGridForm() {
		String punto = ver +"doInitGridForm ";
		logger.trace(punto );
//		CaribelSearchCtrl c1 = (CaribelSearchCtrl)caribel_search_presidi.getAttribute(MY_CTRL_KEY);
//		c1.putLinkedSearchObjects("codreg", getProfile().getStringFromProfile("codice_regione"));
//		c1.putLinkedSearchObjects("codazsan", getProfile().getStringFromProfile("codice_usl"));
	}

	@SuppressWarnings("unchecked")
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		logger.trace(punto + " inizio con dati ");
		boolean valido= true;
//		if(clb.getItemCount()>0){
//			for (Iterator<Listitem> iterator = clb.getItems().iterator(); iterator.hasNext();) {
//				Listitem type = (Listitem) iterator.next();
//				Hashtable<String, Object> htFromGrid = (Hashtable<String, Object>) type.getAttribute("ht_from_grid");			
//				if(htFromGrid.get("presidio").equals(cod_presidio.getRawValue())){
//					UtilForUI.standardExclamation("Impossibile inserire. Record con uguale " + Labels.getLabel("generic.codice"));
//					return false;
//				}
//			}
//		}
		return valido;
	}	
}
	
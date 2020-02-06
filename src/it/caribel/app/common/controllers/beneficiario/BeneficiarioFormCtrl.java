package it.caribel.app.common.controllers.beneficiario;

import it.caribel.app.sinssnt.bean.modificati.BeneficiarioEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Window;

public class BeneficiarioFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;

		
		private CaribelIntbox codice;		
		private CaribelCombobox tipo_pag;		
		private CaribelTextbox cognome;
		protected Window Beneficiari;
		public static String myKeyPermission = "BENEFIC";
		private BeneficiarioEJB myEJB = new BeneficiarioEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				codice.setFocus(true);
				caricaComboTipoPag(tipo_pag);
				if(dbrFromList!=null){
					hParameters.put("b_codice", dbrFromList.get("b_codice"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					codice.setReadonly(true);
					cognome.setFocus(true);
				}else{
					codice.setReadonly(false);
				}
				Beneficiari.doModal();
			}catch(Exception e){
				doShowException(e);
			}
		}
		
		public void caricaComboTipoPag (CaribelCombobox cbx)throws Exception {
			cbx.clear();
			CaribelComboRepository.addComboItem(cbx, "0", Labels.getLabel("generic.tipo_pag1"));
			CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("generic.tipo_pag2"));
			CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("generic.tipo_pag3"));
			CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("generic.tipo_pag4"));
			CaribelComboRepository.addComboItem(cbx, "4", Labels.getLabel("generic.tipo_pag5"));
			
			cbx.setValue(Labels.getLabel("generic.tipo_pag1"));
			
		}

		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}
		
	}

package it.caribel.app.sinssnt.controllers.sinssnt_operQual;

import it.caribel.app.common.ejb.OperqualEJB;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import org.zkoss.zul.Window;

public class OperQualFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;

		protected Window smOperQual;
		
		private CaribelTextbox codice;

		private String myKeyPermission = "OPERQUAL";
		private OperqualEJB myEJB = new OperqualEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				
				if(dbrFromList!=null){
					hParameters.put("cod_qualif", (String)dbrFromList.get("cod_qualif"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					codice.setReadonly(true);
				}else{
					codice.setReadonly(false);
				}
				smOperQual.doModal();
			}catch(Exception e){
				doShowException(e);
			}
		}

		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}
	}

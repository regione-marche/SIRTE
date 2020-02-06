package it.caribel.app.common.controllers.soctab;

import it.caribel.app.sinssnt.bean.SSoctabEJB;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelGridFormCtrl;
import it.caribel.zk.util.UtilForComponents;
import it.pisa.caribel.isas2.ISASRecord;

import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

public class SoctabFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;
		
		private CaribelTextbox codice;
		
		public static String myKeyPermission = "SSOCTAB";
		private SSoctabEJB myEJB = new SSoctabEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				
				if(dbrFromList!=null){
					hParameters.put("codice", ""+dbrFromList.get("codice"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					codice.setReadonly(true);
				}else{
					codice.setReadonly(false);
				}
				
			}catch(Exception e){
				doShowException(e);
			}
		}
		
		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}

		
}

package it.caribel.app.common.controllers.pensioni;

import it.caribel.app.sinssnt.bean.PensioniEJB;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelIntbox;
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

public class PensioniFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;
		
		private CaribelTextbox cod;
		private CaribelTextbox descrizione;
		
		public static String myKeyPermission = "PENSIONI";
		private PensioniEJB myEJB = new PensioniEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				
				if(dbrFromList!=null){
					hParameters.put("pe_codice", (String)dbrFromList.get("pe_codice"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					cod.setReadonly(true);
					descrizione.setFocus(true);
				}else{
					cod.setReadonly(false);
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

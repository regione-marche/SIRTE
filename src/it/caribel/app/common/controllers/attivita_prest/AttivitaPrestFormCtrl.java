package it.caribel.app.common.controllers.attivita_prest;

import it.caribel.app.sinssnt.bean.SocAssAttPrestEJB;
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

public class AttivitaPrestFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;
		
		private CaribelIntbox codice;
		private CaribelTextbox descrizione;
		
		public static String myKeyPermission = "A_ASATPR";
		private SocAssAttPrestEJB myEJB = new SocAssAttPrestEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				codice.setFocus(true);
				if(dbrFromList!=null){
					hParameters.put("cod_attivita", ""+dbrFromList.get("cod_attivita"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					codice.setReadonly(true);
					descrizione.setFocus(true);
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

package it.caribel.app.common.controllers.banche;

import java.util.Hashtable;

import it.caribel.app.sinssnt.bean.BancheEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

public class BancheFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;
		protected Window Banche;
	
		private CaribelTextbox codice_abi;
		private CaribelTextbox codice_cab;	
		private CaribelTextbox indirizzo_ban;
		

		public static String myKeyPermission = "BANCHE";
		private BancheEJB myEJB = new BancheEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);	
				codice_abi.setFocus(true);
				if(dbrFromList!=null){
					hParameters.put("ban_codice_abi", (String)dbrFromList.get("ban_codice_abi"));
					hParameters.put("ban_cab_sport", (String)dbrFromList.get("ban_cab_sport"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					codice_abi.setReadonly(true);
					codice_cab.setReadonly(true);
					indirizzo_ban.setFocus(true);
				}else{
					codice_abi.setReadonly(false);
					codice_cab.setReadonly(false);
				}
				Banche.doModal();
			}catch(Exception e){
				doShowException(e);
			}
		}
		
		

		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}
		
	}

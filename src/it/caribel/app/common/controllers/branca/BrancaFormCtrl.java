package it.caribel.app.common.controllers.branca;

import java.util.Hashtable;

import it.caribel.app.sinssnt.bean.modificati.BrancaEJB;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

public class BrancaFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;

		
		private CaribelTextbox codice;	
		private CaribelTextbox codice_pre;
		private CaribelTextbox descrizione;
		
		protected Window Branca;
		public static String myKeyPermission = "BRANCA";
		private BrancaEJB myEJB = new BrancaEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);	
				codice_pre.setFocus(true);
				if(dbrFromList!=null){
					hParameters.put("codice", dbrFromList.get("codice"));
					hParameters.put("cod_tippre", dbrFromList.get("cod_tippre"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					codice.setReadonly(true);
					codice_pre.setReadonly(true);
					descrizione.setFocus(true);
				}else{
					codice.setReadonly(false);
					codice_pre.setReadonly(false);
				}
				Branca.doModal();
			}catch(Exception e){
				doShowException(e);
			}
		}
		
		

		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}
		
	}

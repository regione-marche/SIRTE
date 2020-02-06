package it.caribel.app.common.controllers.prestaz_bisogni;

import java.util.Hashtable;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.nuovi.PrestazBisogniEJB;
import it.caribel.app.sinssnt.util.ComboModelRepository;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class PrestazBisogniFormCtrl extends CaribelFormCtrl {

		private static final long serialVersionUID = 1L;

		protected Window PrestazBisogni;
		
		private CaribelIntbox id;
		private CaribelIntbox codice_nascosto;
		
		
		private CaribelCombobox frequenza;
		private CaribelCombobox bisogno;

		public static String myKeyPermission = "PRE_BI";
		private PrestazBisogniEJB myEJB = new PrestazBisogniEJB();
		
		public void doInitForm() {
			try {
				super.initCaribelFormCtrl(myEJB,myKeyPermission);
				id.setFocus(true);
				caricaCombo();
				caricaComboBisogno(bisogno);
				String cod=id.getText();
				if (cod!=null && !cod.equals(""))
					codice_nascosto.setText(cod);
				if(dbrFromList!=null){
					hParameters.put("id", (Integer)dbrFromList.get("id"));
					doQueryKeySuEJB();
					doWriteBeanToComponents();
					id.setReadonly(true);
					
				}else{
					id.setReadonly(false);
				}
				PrestazBisogni.doModal();
			}catch(Exception e){
				doShowException(e);
			}
		}
		
		
	
		
		
		


		@Override
		protected boolean doValidateForm() throws Exception {
			return true;
		}
		
		private void caricaCombo() throws Exception {
			//Caricamento combo frequenza accessi
			Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
			Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

			Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
			h_xCBdaTabBase.put("FREQAC",   frequenza);

			CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val", "tab_descrizione", true);
			frequenza.setSelectedValue(3);
			
		}
		
		public void caricaComboBisogno (CaribelCombobox cbx)throws Exception {
			CaribelComboRepository.populateCombobox(bisogno, ComboModelRepository.BISOGNO, false);
			
		}

		
	}

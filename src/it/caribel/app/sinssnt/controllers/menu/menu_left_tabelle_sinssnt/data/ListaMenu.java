package it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle_sinssnt.data;

import it.caribel.app.sinssnt.controllers.tabelle.operatori.*;
import it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle.data.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import org.zkoss.util.resource.Labels;


public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {
		MenuItem asl_distr = new MenuItem (Labels.getLabel("menu.tabelle.asl_distr"));
		asl_distr.setPathZul("/web/ui/sinssnt/tabelle/asl_distr/aslDistrGrid.zul");
  
		
		MenuItem commissione_uvm = new MenuItem (Labels.getLabel("menu.tabelle.commissione_uvm"));
		commissione_uvm.setPathZul("/web/ui/sinssnt/tabelle/commissione_uvm/commissioneUvmGrid.zul");
  
		MenuItem farmaci = new MenuItem (Labels.getLabel("menu.tabelle.farmaci"));
		farmaci.setPathZul("/web/ui/sinssnt/tabelle/farmaci/farmaciGrid.zul");
		
		//MenuItem interventiSIL = new MenuItem (Labels.getLabel("menu.tabelle.interventiSIL"));
		//interventiSIL.setPathZul("/web/ui/sinssnt/tabelle/interventiSIL/interventiSILGrid.zul");
  
		MenuItem operatori = new MenuItem (Labels.getLabel("menu.tabelle.operatori"));	
		operatori.setKeyPermission(OperatoriFormCtrl.myKeyPermission);
		operatori.setPathZul(OperatoriGridCtrl.myPathZul); 
		
		MenuItem piano_intervPAP = new MenuItem (Labels.getLabel("menu.tabelle.piano_intervPAP"));
		piano_intervPAP.setPathZul("/web/ui/sinssnt/tabelle/piano_intervPAP/pianoIntervPAPGrid.zul");
  
		//MenuItem progetti = new MenuItem (Labels.getLabel("menu.tabelle.progetti_aperti"));
		//progetti.setPathZul("/web/ui/sinssnt/tabelle/prog_aperti/asProgettiApertiGrid.zul");
		
		//MenuItem relazioni_sociali = new MenuItem (Labels.getLabel("menu.tabelle.relazioni_sociali"));
		//relazioni_sociali.setPathZul("/web/ui/sinssnt/tabelle/relazioni_sociali/relazioniSocialiGrid.zul");
		
		//MenuItem ricerca_rsa = new MenuItem (Labels.getLabel("menu.tabelle.ricerca_rsa"));
		//ricerca_rsa.setPathZul("/web/ui/sinssnt/tabelle/ric_rp_rsa/ricRpRsaGrid.zul");
				
		//MenuItem scheda_svama = new MenuItem (Labels.getLabel("menu.tabelle.scheda_svama"));
		//scheda_svama.setPathZul("/web/ui/sinssnt/tabelle/skSvama/skSvamaGrid.zul");
		
		//MenuItem ele_segnalazioni = new MenuItem (Labels.getLabel("menu.tabelle.ele_segnalazioni"));
		//ele_segnalazioni.setPathZul("/web/ui/sinssnt/tabelle/elenco_casi_PUAC/elencoCasiPUACGrid.zul");
		
		MenuItem ele_segnalazioni_ospedale = new MenuItem (Labels.getLabel("menu.tabelle.ele_segnalazioni_ospedale"));
		ele_segnalazioni_ospedale.setPathZul("/web/ui/sinssnt/tabelle/ele_segnala_ospedale/eleSegnalaOspedaleGrid.zul");
		
		//MenuItem tipo_assistiti = new MenuItem (Labels.getLabel("menu.tabelle.tipo_assistiti"));
		//tipo_assistiti.setPathZul("/web/ui/sinssnt/tabelle/tipo_assistito/tipoAssistitoGrid.zul");

		//MenuItem tipo_istituto = new MenuItem (Labels.getLabel("menu.tabelle.tipo_istituto"));
		//tipo_istituto.setPathZul("/web/ui/sinssnt/tabelle/tipoIstituto/tipoIstitutoGrid.zul");

		MenuItem far_tracciato1 = new MenuItem (Labels.getLabel("menu.tabelle.far_tracciato1"));
		far_tracciato1.setPathZul("/web/ui/sinssnt/tabelle/far_tracciato1/farTracciato1Grid.zul");

		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
				new MenuTreeNode(asl_distr),
//				new MenuTreeNode(commissione_uvm),
				new MenuTreeNode(farmaci),
				//new MenuTreeNode(interventiSIL),
				new MenuTreeNode(operatori),
				new MenuTreeNode(piano_intervPAP),
				//new MenuTreeNode(progetti),
				//new MenuTreeNode(relazioni_sociali),
				//new MenuTreeNode(ricerca_rsa),
				//new MenuTreeNode(scheda_svama),
				//new MenuTreeNode(ele_segnalazioni),
				new MenuTreeNode(ele_segnalazioni_ospedale),
				//new MenuTreeNode(tipo_assistiti),
				//new MenuTreeNode(tipo_istituto),
				new MenuTreeNode(far_tracciato1),
				

		});
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

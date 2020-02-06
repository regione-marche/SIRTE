package it.caribel.app.sinssnt.controllers.menu.menu_left_puac.data;

import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import org.zkoss.util.resource.Labels;



public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {
		
		MenuItem puac_ele_segn = new MenuItem (Labels.getLabel("menu.puac.elenco.segnalazioni"));	
		MenuItem pua_ele_val_pap = new MenuItem (Labels.getLabel("menu.puac.elenco.schede.valutazione.pap"));
		
		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
				new MenuTreeNode(puac_ele_segn),
				new MenuTreeNode(pua_ele_val_pap)				
			
		});
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

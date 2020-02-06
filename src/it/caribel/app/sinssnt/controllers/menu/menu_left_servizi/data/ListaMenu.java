package it.caribel.app.sinssnt.controllers.menu.menu_left_servizi.data;

import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import org.zkoss.util.resource.Labels;



public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {
		
		
		MenuItem servizi_tipol = new MenuItem (Labels.getLabel("menu.servizi.tipologia.servizi"));
		MenuItem servizi_gest = new MenuItem (Labels.getLabel("menu.servizi.gestione.servizi"));		
		
		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
				new MenuTreeNode(servizi_tipol),
				new MenuTreeNode(servizi_gest)				
			
		});
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

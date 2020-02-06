package it.caribel.app.sinssnt.controllers.menu.menu_left_flussi.data;

import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import org.zkoss.util.resource.Labels;



public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {	
		
		MenuItem flussi_sili = new MenuItem (Labels.getLabel("menu.flussi.estrazioni.sili"));
		
		
		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
				new MenuTreeNode(flussi_sili)			
			
		});
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

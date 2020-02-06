package it.caribel.app.sinssnt.controllers.menu.menu_left_stampe2.data;

import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import org.zkoss.util.resource.Labels;



public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {
		
		
		MenuItem stampe2_sociale = new MenuItem (Labels.getLabel("menu.stampe2.sociale"));
		MenuItem stampe2_ass_motivo = new MenuItem (Labels.getLabel("menu.stampe2.elenco.assistiti.assegnazione.motivo"));
		MenuItem stampe2_prest_ass = new MenuItem (Labels.getLabel("menu.stampe2.prestazioni.assegnate.assistito.pai"));
		
		
		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
				new MenuTreeNode(stampe2_sociale),
				new MenuTreeNode(stampe2_ass_motivo),
				new MenuTreeNode(stampe2_prest_ass)
				
			
		});
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

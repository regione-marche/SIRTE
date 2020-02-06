package it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle;

import it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle.data.ListaMenu;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.AdvancedTreeModel;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuComposer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Tree;



public class ComposerMenuLeft extends MenuComposer {

	private static final long serialVersionUID = 4231564509395483264L;
	
	@Wire
	private Tree menu_tree_tabelle;

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		initTree(menu_tree_tabelle, new AdvancedTreeModel(new ListaMenu().getRoot()));
	}

	
}
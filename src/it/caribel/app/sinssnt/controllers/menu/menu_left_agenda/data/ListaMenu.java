package it.caribel.app.sinssnt.controllers.menu.menu_left_agenda.data;

import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;

import org.zkoss.util.resource.Labels;



public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {	
		
//		MenuItem agenda_prest_nuova = new MenuItem (Labels.getLabel("menu.agenda.prestazioni")+"NUOVO");
		MenuItem agenda_prest = new MenuItem (Labels.getLabel("menu.agenda.prestazioni"));
		MenuItem agenda_stampa = new MenuItem (Labels.getLabel("menu.agenda.stampa.agenda"));		

		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
//				new MenuTreeNode(agenda_prest_nuova),
				new MenuTreeNode(agenda_prest),
				new MenuTreeNode(agenda_stampa)				
			
		});
		
		
		try {
			//FIXME rimuovere la doppia visualizzazione post test.
			if(ManagerProfile.isConfigurazioneMarche(CaribelSessionManager.getInstance())){
				agenda_prest.setKeyPermission("AGENDA");
				agenda_prest.setPathZul("/web/ui/sinssnt/agenda/agendaMultiOperatore/agendaForm.zul");
			}else{
				agenda_prest.setKeyPermission("AGENDA");
				agenda_prest.setPathZul("/web/ui/sinssnt/agenda/agendaForm.zul");
			}
//				agenda_prest_nuova.setKeyPermission("AGENDA");
//				agenda_prest_nuova.setPathZul("/web/ui/sinssnt/agenda/agendaMultiOperatore/agendaForm.zul");
//				agenda_prest.setKeyPermission("AGENDA");
//				agenda_prest.setPathZul("/web/ui/sinssnt/agenda/agendaForm.zul");
			agenda_stampa.setKeyPermission("ST_AGVIS");
			agenda_stampa.setPathZul("/web/ui/report/reportAgenda.zul");
//			Hashtable<String, String> argForZul = new Hashtable<String, String>();
//			argForZul.put("provAccessiPrestazioni", 1+"");
//			argForZul.put("mode", "overlapped");
//			agenda_prest.setArgForZul(argForZul);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

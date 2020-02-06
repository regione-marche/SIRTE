package it.caribel.app.sinssnt.controllers;

import it.caribel.app.sinssnt.util.Costanti;

import it.caribel.zk.util.UtilForUI;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;

public class ContainerNoProfiloCtrl extends ContainerSinssntCtrl {

	private static final long serialVersionUID = 1L;
	
	public static final String myPathZul = "/web/ui/containerNoProfilo.zul";
		
	public void doAfterCompose(Component comp) throws Exception {	
		super.doAfterCompose(comp);
		if(Executions.getCurrent().getSession().hasAttribute(Costanti.FIRST_ACCESS)){
			UtilForUI.doTestVersionBrowser();
			Executions.getCurrent().getSession().removeAttribute(Costanti.FIRST_ACCESS);
		}
	}


	@Override
	public void doRefreshOnSave(Component comp) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void doRefreshOnDelete(Component comp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShowComponent(Event evt) {
		// TODO Auto-generated method stub
		
	}
	
	
}

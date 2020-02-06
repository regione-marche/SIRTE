package it.caribel.app.sinssnt.controllers.login;

import it.caribel.util.CaribelSessionManager;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

  
public class LoginCohesionCtrl extends SelectorComposer<Window> {

	private static final long serialVersionUID = 2785400018169401382L;

	private ManagerProfile mp = new ManagerProfile();
	
	private ManagerAccessoFromCohesion 		mac = new ManagerAccessoFromCohesion();


	public ComponentInfo doBeforeCompose(Page page, Component parent,ComponentInfo compInfo){
		try{
			if (CaribelSessionManager.getInstance().isAuthenticated()) {
				Executions.getCurrent().sendRedirect("/main.zul");
				return null;
			}else{
				mac.gestisciAccessoDaSSOMarche(CaribelSessionManager.getInstance());
				if(CaribelSessionManager.getInstance().isAuthenticated()){
					if(mp.caricaProfile())
						Executions.getCurrent().sendRedirect("/main.zul");
				}
				return null;
			}
		}catch(Exception ex){
			Clients.alert(Labels.getLabel("login.automatico.error"), Labels.getLabel("messagebox.attention"), "z-msgbox z-msgbox-error");
			return null;
		}
	}
}

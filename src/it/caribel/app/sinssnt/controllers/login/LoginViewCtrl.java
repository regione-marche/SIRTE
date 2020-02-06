package it.caribel.app.sinssnt.controllers.login;

import it.caribel.util.CaribelSessionManager;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

  
public class LoginViewCtrl extends SelectorComposer<Window> {

	private static final long serialVersionUID = 5730426085235946339L;
	private ManagerProfile mp = new ManagerProfile();
	
	private ManagerAccessoFromCohesion 		mac = new ManagerAccessoFromCohesion();
	private ManagerAccessoFromAster 		maa = new ManagerAccessoFromAster();
	private ManagerAccessoFromDevelopment 	mad = new ManagerAccessoFromDevelopment();

	@Wire
	private Textbox nameTxb, passwordTxb;

	@Wire
	private Label mesgLbl;
	

	public ComponentInfo doBeforeCompose(Page page, Component parent,ComponentInfo compInfo){
		if (CaribelSessionManager.getInstance().isAuthenticated()) {
			Executions.getCurrent().sendRedirect("/main.zul");
			return null;
		}else{
			doLoginAutomatica();
			if(CaribelSessionManager.getInstance().isAuthenticated()){
				if(mp.caricaProfile())
					Executions.getCurrent().sendRedirect("/main.zul");
				return null;
			}else{
				//Non è riuscita l'autenticazione automatica, allora mostro la form di login
				return super.doBeforeCompose(page, parent, compInfo);
			}
		}
	}

	@Override
	public void doAfterCompose(Window comp) throws Exception {
		super.doAfterCompose(comp);	
		if(nameTxb!=null){
			nameTxb.setFocus(true);
		}
	}

	@Listen("onOK=#passwordTxb")
	public void onOK() {
		doLogin();
	}

	@Listen("onClick=#confirmBtn")
	public void confirm() {
		doLogin();
	}

	private void doLogin() {
		CaribelSessionManager mgmt = CaribelSessionManager.getInstance();
		ManagerLogin ml = new ManagerLogin();
		ml.login(nameTxb.getValue(), passwordTxb.getValue(),mgmt);
		if (mgmt.isAuthenticated()){
			if(mp.caricaProfile())
				Executions.getCurrent().sendRedirect("/main.zul");
		} else {			
			//mesgLbl.setValue(Labels.getLabel("login.wrong.user_or_pwd"));
			Clients.showNotification(Labels.getLabel("login.wrong.user_or_pwd"),"warning",getSelf(),"end_center",2500);
		}
	}

	private void doLoginAutomatica(){
		try{
			CaribelSessionManager mgmt = CaribelSessionManager.getInstance();
			if(maa.isAccessoFromAster()){
				maa.gestisciAccessoDaAsterLogin(mgmt);
			}else{
				mad.gestisciAccessoDaDevelopment(mgmt);
			}
		}catch(Exception ex){
			Clients.alert(Labels.getLabel("login.automatico.error"), Labels.getLabel("messagebox.attention"), "z-msgbox z-msgbox-error");
		}
	}

}

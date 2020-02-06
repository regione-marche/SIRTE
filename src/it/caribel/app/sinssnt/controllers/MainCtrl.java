package it.caribel.app.sinssnt.controllers;

import java.util.Enumeration;
import java.util.Hashtable;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.pisa.caribel.isas2.ISASUser;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;



public class MainCtrl extends CaribelForwardComposer {

	private static final long serialVersionUID = -3050804164944763910L;
	
	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
	
	public void doAfterCompose(Component comp) throws Exception {
		try{
			super.doAfterCompose(comp);	
	//		UtilForUI.doTestVersionBrowser();
			Executions.getCurrent().getSession().setAttribute(Costanti.FIRST_ACCESS, "true");
	        apriContainerDaPermessiOperatore();
	    	return;
		}catch (Exception e) {
			doShowException(e);
		}
    }
	
	private void apriContainerDaPermessiOperatore()throws Exception {
		if(iu.canIUse(ChiaviISASSinssntWeb.A_OPPUAC))
			Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul);
		else if(iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_MEDICO))
			Executions.getCurrent().sendRedirect(ContainerMedicoCtrl.myPathZul);
		else if(iu.canIUse(ChiaviISASSinssntWeb.SKINF))
			Executions.getCurrent().sendRedirect(ContainerInfermieristicoCtrl.myPathZul);
		else if(iu.canIUse(ChiaviISASSinssntWeb.SKFISIO))
			Executions.getCurrent().sendRedirect(ContainerFisioterapicoCtrl.myPathZul);
		else if(iu.canIUse(ChiaviISASSinssntWeb.SKMEDPAL))
			Executions.getCurrent().sendRedirect(ContainerPalliativistaCtrl.myPathZul);
		else if(redirecSuContainerGenerico()){
			logger.debug("Ho rediretto su container generico");
		}else
			Executions.getCurrent().sendRedirect(ContainerNoProfiloCtrl.myPathZul);
	}
	
	
	private boolean redirecSuContainerGenerico() throws Exception {
//		String punto = "redirecSuContainerGenerico ";
		Hashtable<String, String> tipiOp = ManagerOperatore.getTipiOperatori(Costanti.TAB_VAL_SO_TIPO_OPERATORE);
		Enumeration<String> n = tipiOp.keys();
//		logger.trace(punto + " canUseOperatore>>" + iu.getFuncs()+"<");
		while (n.hasMoreElements()){
			String tipo_operatore = (String)n.nextElement();
//			logger.trace(punto + " tipo operatore >>" + tipo_operatore+"<");
			if(iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_GENERICO+tipo_operatore)){
				Executions.getCurrent().sendRedirect(ContainerGenericoCtrl.myPathZul +"?"+ContainerGenericoCtrl.parameter_tipo_op+"="+tipo_operatore);
				return true;
			}
		}
		return false;
	}
}

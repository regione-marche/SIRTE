package it.caribel.app.sinssnt.bean.nuovi;

import java.util.Hashtable;
import java.util.Vector;

import it.caribel.app.common.ejb.AccessiMmgEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.millewin.manager_web.ManagerCaricamento;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.millewin.manager_web.InsertDBManagerMarcheWA;

public class AccessiMmgWebAppEJB extends AccessiMmgEJB {

	public AccessiMmgWebAppEJB() {}
	
	public void insertPrestazErogate(myLogin mylogin,Hashtable<String,String> h,Vector<String> listaPrest) throws  Exception {
		String nomeMetodo = "insertPrestazErogate";
		ISASConnection dbc=null;
		boolean done = false;
		try{
			dbc=super.logIn(mylogin);
			//############ INIZIO TRANSAZIONE ########################
			dbc.startTransaction();
			
			@SuppressWarnings("rawtypes")
			Hashtable hRitorno = new Hashtable();
			int ubicazione = InsertDBManagerMarcheWA.UBI_RMARCHE;
			
			boolean ctrlAuto = false;
			String ctr_auto_web= ManagerProfile.getValue(CaribelSessionManager.getInstance(), ManagerProfile.CTR_AUTO_WEB);
			if(ctr_auto_web!=null && !ctr_auto_web.equals("") && ctr_auto_web.equals("SI"))
				ctrlAuto = true;
			
			boolean ctrlSosp = false;
			String ctr_sosp_web= ManagerProfile.getValue(CaribelSessionManager.getInstance(), ManagerProfile.CTR_SOSP_WEB);
			if(ctr_sosp_web!=null && !ctr_sosp_web.equals("") && ctr_sosp_web.equals("SI"))
				ctrlSosp = true;
			
			boolean ancheSuINTMMG = false;
			String forza_intmmg_web= ManagerProfile.getValue(CaribelSessionManager.getInstance(), ManagerProfile.FORZA_INTMMG_WEB);
			if(forza_intmmg_web!=null && !forza_intmmg_web.equals("") && forza_intmmg_web.equals("SI"))
				ancheSuINTMMG = true;
			
			InsertDBManagerMarcheWA insDBManager = new InsertDBManagerMarcheWA(dbc, hRitorno, ubicazione, ctrlAuto, ctrlSosp,ancheSuINTMMG);
			ManagerCaricamento mc = new ManagerCaricamento(insDBManager,this);
			mc.eseguiCaricamento(dbc,h,listaPrest);

			dbc.commitTransaction();
			done=true;
			//############ FINE TRANSAZIONE ########################
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			if (!done)
				rollback_nothrow(nomeMetodo, dbc);
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	

}

	

package it.pisa.caribel.sinssnt.millewin.manager_web;

import java.util.Hashtable;

import it.caribel.app.sinssnt.millewin.manager_web.InsertDBManagerMarche;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.isas2.ISASConnection;

public class InsertDBManagerMarcheWA extends InsertDBManagerMarche{
	
	@SuppressWarnings("rawtypes")
	public InsertDBManagerMarcheWA(ISASConnection mydbc, Hashtable myHRitorno,
			int myUbicazione, boolean myCtrlAuto, boolean myCtrlSosp, boolean myCaricaSuINTMMG) throws Exception {
		super(mydbc, myHRitorno,myUbicazione, myCtrlAuto, myCtrlSosp, myCaricaSuINTMMG);
		
		//Reimposto il vero codice operatore anziche SAMM1
		super.cod_operatore=CaribelSessionManager.getInstance().getStringFromProfile("codice_operatore");
	}
	
	

}

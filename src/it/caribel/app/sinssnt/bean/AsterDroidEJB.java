package it.caribel.app.sinssnt.bean;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;

public class AsterDroidEJB extends SINSSNTConnectionEJB  {

public AsterDroidEJB() {}

public byte[] checkuser(String utente, String passwd, Hashtable par) throws Exception {
	ISASConnection dbc = null;
	String x_tipo = "";
	String x_tdes = "";
	String x_qual = "";

	try {
		myLogin lg = new myLogin();
		lg.put(utente, passwd);
		dbc = super.logIn(lg);
		String s = "SELECT * FROM OPERATORI WHERE codice = '"+utente+"'";
		ISASRecord dbr = dbc.readRecord(s);
		if (dbr != null) {
			x_tipo = (String)dbr.get("tipo");
			x_tdes = getTipo(x_tipo);
			x_qual = (String)dbr.get("cod_qualif");
		}
	} catch(Exception e) {
		x_tipo = "";
		x_tdes = "";
		x_qual = "";
		System.out.println("AsterDroidEJB.checkuser("+utente+"): "+e);
	}finally{
		try{
			if (dbc != null) dbc.close();
			super.close(dbc);
		} catch(Exception e1) {
			System.out.println("AsterDroidEJB.checkuser("+utente+"): ERRORE "+e1);
			e1.printStackTrace();
		}
	}
	String v = utente+"|"+x_tipo+"|"+x_tdes+"|"+x_qual;
	return v.getBytes();
}

public byte[] getservices(String utente, String passwd, Hashtable par) throws Exception {
	ISASConnection dbc = null;
	ISASCursor dbcur = null;
	String v = "";
	try {
		myLogin lg = new myLogin();
		//lg.put(utente, passwd);
		lg.put("GUEST", "GUEST");
		dbc = super.logIn(lg);
		String s = "SELECT * FROM OPERATORI WHERE codice = '"+utente+"'";
		ISASRecord dbr = dbc.readRecord(s);
		if (dbr != null) {
			String t1 = (String)dbr.get("tipo");
			String t2 = getTipo(t1);
			String t3 = getTIPDEF(dbc, t1);
			//if (t1.substring(0,1).equals("0")) t1 = t1.substring(1,2);
			s = "SELECT * FROM prestaz WHERE prest_tipo = '"+t3+"' ORDER BY prest_cod";
			System.out.println("AsterDroidEJB.getservices("+utente+"): "+s);
			dbcur = dbc.startCursor(s);
			if (dbcur != null){
				while (dbcur.next()){
					ISASRecord p = dbcur.getRecord();
					v += (String)p.get("prest_cod")+"|"+(String)p.get("prest_des")+"|"+t2 +"\n";
				}
				dbcur.close();
			}
		}
	} catch(Exception e) {
		System.out.println("AsterDroidEJB.getservices("+utente+"): "+e);
		v = "";
	}finally{
		try{
			if (dbcur != null) dbcur.close();
			if (dbc != null) dbc.close();
			super.close(dbc);
		} catch(Exception e1) {
			System.out.println("AsterDroidEJB.getservices("+utente+"): ERRORE "+e1);
			e1.printStackTrace();
		}
	}
	return v.getBytes();
}

protected String getTIPDEF(ISASConnection dbc, String t1) {
	String t2 = t1;
	try {
		if (t2.substring(0,1).equals("0")) t2 = t2.substring(1,2);
		String s = "SELECT * FROM conf WHERE conf_kproc = 'SINS' AND conf_key = 'TIPDEF" + t1 + "'";
		ISASRecord dbr = dbc.readRecord(s);
		if (dbr != null && dbr.get("conf_txt") != null) 
			t2 = (String) dbr.get("conf_txt");
	} catch(Exception ex) {
		System.out.println("AsterDroidEJB.getTIPDEF("+t1+"): "+ex);
	}
	System.out.println("AsterDroidEJB.getTIPDEF("+t1+"): "+t2);
	return t2;
}

protected String getTipo(String t1) {
	String t2 = "";
	if (t1.equals("01"))		t2 = "ASSISTENTE SOCIALE";
	else if (t1.equals("02"))	t2 = "INFERMIERE";
	else if (t1.equals("03"))	t2 = "MEDICO";
	else if (t1.equals("04"))	t2 = "FISIOTERAPISTA";
	else if (t1.equals("05"))	t2 = "AMMINISTRATIVO";
	else if (t1.equals("06"))	t2 = "OSTETRICO";
	else if (t1.equals("07"))	t2 = "NEUROPSICHIATRA";
	else if (t1.equals("08"))	t2 = "GINECOLOGO";
	else if (t1.equals("09"))	t2 = "PSICOLOGO";
	else if (t1.equals("10"))	t2 = "PEDIATRA";
	else if (t1.equals("11"))	t2 = "DIETISTA";
	else if (t1.equals("12"))	t2 = "EDUCATORE";
	else if (t1.equals("13"))	t2 = "FISIOKINESITERAPISTA";
	else if (t1.equals("14"))	t2 = "LOGOPEDISTA";
	else if (t1.equals("15"))	t2 = "NEUROPSICHIATRA INFANTILE";
	else if (t1.equals("16"))	t2 = "OTA/OSA";
	else if (t1.equals("17"))	t2 = "CONSULENTE";
	else if (t1.equals("18"))	t2 = "NEUROPSICOMOTICISTA";
	else if (t1.equals("98"))	t2 = "MEDICO SPECIALISTA";
	else if (t1.equals("99"))	t2 = "MMG";
	else 				t2 = "***";
	return t2;
}

}

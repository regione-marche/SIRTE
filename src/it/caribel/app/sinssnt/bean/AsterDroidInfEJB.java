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

public class AsterDroidInfEJB extends SINSSNTConnectionEJB  {

public AsterDroidInfEJB() {}

private String mkJSONValue(Object o) {
	StringBuffer s = new StringBuffer(1024);
	if (o instanceof java.lang.String) {
		String p = (String)o;
		s.append("\"");
		for (int i = 0; i < p.length(); i++) {
			if ((p.charAt(i) == '"')||(p.charAt(i) == '\\')) s.append("\\");
			s.append(p.charAt(i));
		}
		s.append("\"");
	} else {
		s.append(o.toString());
	}
	return s.toString();
}

private String mkJSONObject(ISASRecord dbr) {
	StringBuffer s = new StringBuffer(1024);
	try {
		if (dbr == null) {
			s.append("\"error\":\"null\"");
		} else {
			Hashtable ht = dbr.getHashtable();
			String sep = "";
			for(Enumeration e = ht.keys(); e.hasMoreElements();) {
				String k = (String)e.nextElement();
				s.append(sep+"\""+k+"\":"+mkJSONValue(ht.get(k)));
				if (sep.equals("")) sep = ", ";
			}
		}
	} catch(Exception ex) {
		s.append("\"error\":"+mkJSONValue(ex.toString()));
	}
	return "{"+s+"}";
}

private String getAssistitoPrivate(ISASConnection dbc, String id) throws Exception {
	String s = "SELECT c.*, a.* FROM cartella c, anagra_c a WHERE c.n_cartella = "+id+
		" AND c.n_cartella = a.n_cartella AND a.data_variazione = "+
		"(SELECT MAX(data_variazione) FROM anagra_c a1 WHERE a1.n_cartella = a.n_cartella)";
	return mkJSONObject(dbc.readRecord(s));
}

private String getContattoPrivate(ISASConnection dbc, String id, String contatto) throws Exception {
	String s = "SELECT s.* FROM skinf s WHERE s.n_cartella = "+id+" AND s.n_contatto = "+contatto;
	return mkJSONObject(dbc.readRecord(s));
}

private String getElencoContattiPrivate(ISASConnection dbc, String id) throws Exception {
	ISASCursor dbcur = null;
	StringBuffer v = new StringBuffer(1024);
	String s = "SELECT s.* FROM skinf s WHERE s.n_cartella = "+id;
	dbcur = dbc.startCursor(s);
	if (dbcur != null){
		try {
			String sep = "";
			while (dbcur.next()){
				ISASRecord r = dbcur.getRecord();
				v.append(sep+mkJSONObject(r));
				if (sep.equals("")) sep = ", ";
			}
		} finally {
			dbcur.close();
		}
	}
	return "["+v+"]";
}

private byte[] getRouteMethod(String entry, String utente, String passwd, Hashtable par) throws Exception {
	ISASConnection dbc = null;
	String v = "";
	try {
		myLogin lg = new myLogin();
		lg.put(utente, passwd);
		dbc = super.logIn(lg);
		if (entry.equals("getAssistito")) v = getAssistitoPrivate(dbc, (String)par.get("ID"));
		else if (entry.equals("getContatto")) v = getContattoPrivate(dbc, (String)par.get("ID"), (String)par.get("CONTATTO"));
		else if (entry.equals("getElencoContatti")) v = getElencoContattiPrivate(dbc, (String)par.get("ID"));
	} catch(Exception e) {
		System.out.println("AsterDroidInfEJB.getRouteMethod("+entry+","+utente+","+par+"): "+e);
		e.printStackTrace();
		v = "{\"error\":"+mkJSONValue(e.toString())+"}";
	}finally{
		try{
			if (dbc != null) dbc.close();
			super.close(dbc);
		} catch(Exception e1) {
			System.out.println("AsterDroidInfEJB.getRouteMethod("+entry+","+utente+","+par+"): ERRORE "+e1);
			e1.printStackTrace();
		}
	}
	return v.getBytes();
}

public byte[] getAssistito(String utente, String passwd, Hashtable par) throws Exception {
	return getRouteMethod("getAssistito", utente, passwd, par);
}

public byte[] getContatto(String utente, String passwd, Hashtable par) throws Exception {
	return getRouteMethod("getContatto", utente, passwd, par);
}

public byte[] getElencoContatti(String utente, String passwd, Hashtable par) throws Exception {
	return getRouteMethod("getElencoContatti", utente, passwd, par);
}

}

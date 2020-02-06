package it.caribel.app.sinssnt.bean.modificati;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//Scheda Ricoveri
//
//Elisa 19/11/09
//==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.ISASUtil;

public class SkMPalRicoEJB extends SINSSNTConnectionEJB {
	private static String nomeEJB = "SkMPalRicoEJB ";

	public SkMPalRicoEJB() {
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException 
	{		
		System.out.println(nomeEJB + " queryKey() - H == " + h.toString());
		//Carlo Volpicelli - col codice commentato sotto, non funzionava
		boolean done=false;
		ISASConnection dbc=null;
	        try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skmpal_ricoveri where "+
	                                " n_cartella="+(String)h.get("n_cartella")+                                
	                                " AND skr_progr="+(String)h.get("skr_progr");
			System.out.println("QueryKey SkMPalRicoEJB:"+myselect);
			
			ISASRecord dbr=dbc.readRecord(myselect);
			decodificaTipoRicovero(dbc, dbr);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
//		boolean done = false;
//		ISASConnection dbc = null;
//
//		String cartella = null;
//		String progr = null;
//
//		try {
//			cartella = h.get("n_cartella").toString();
//			progr = h.get("skr_progr").toString();
//		} catch (Exception e) {
//			System.out.println(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
//			throw new SQLException(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
//		}
//
//		try {
//			dbc = super.logIn(mylogin);
//
//			String myselect = "SELECT * FROM skmpal_ricoveri WHERE " + " n_cartella = " + cartella
//					+ " AND skr_progr = " + progr;
//
//			System.out.println(nomeEJB + " QueryKey() - " + myselect);
//			ISASRecord dbr = dbc.readRecord(myselect);
//
//			decodificaTipoRicovero(dbc, dbr);
//
//			dbc.close();
//			super.close(dbc);
//			done = true;
//
//			return dbr;
//		} catch (Exception e) {
//			System.out.println(nomeEJB + " ERRORE queryKey() - " + e);
//			throw new SQLException(nomeEJB + " ERRORE queryKey() - " + e);
//		} finally {
//			if (!done) {
//				try {
//					dbc.close();
//					super.close(dbc);
//				} catch (Exception e) {
//					System.out.println(nomeEJB + " ERRORE queryKey() - " + e);
//				}
//			}
//		}
	}

	private void decodificaTipoRicovero(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException, Exception {
		String punto = nomeEJB + ".decodificaTipoRicovero ";
		if (dbr != null) {
			String tipo = dbr.get("skr_tipo").toString();
			if (tipo.equals("0")) {
				dbr.put("skr_tipo_desc", "   ");
			} else if (tipo.equals("1")) {
				dbr.put("skr_tipo_desc", "Ricovero");
			} else if (tipo.equals("2")) {
				dbr.put("skr_tipo_desc", "Intervento 118");
			} else {
				dbr.put("skr_tipo_desc", "Pronto Soccorso");
			}
			tipo = dbr.get("skr_agente_ric").toString();
			dbr.put("", "");

			if (dbr.get("skr_agente_ric") != null && !dbr.get("skr_agente_ric").equals("")) {
				dbr.put("skr_agente_ric_desc", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "SKMAGRIC", ""
						+ dbr.get("skr_agente_ric"), "tab_descrizione"));
			}

			stampa(punto + " Dati che restituisco>" + dbr.getHashtable() + "<");
		} else {
			stampa(punto + "\n RECORD NON CORRETTO \n");
		}
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);

	}

	// 25/09/07: legge l'ultimo rec (per data e progr) e vi aggiunge gli isas
	// record per la griglia
	public ISASRecord queryKeyLast(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;

		try {
			dbc = super.logIn(mylogin);

			String mysel = "SELECT * FROM skmpal_ricoveri" 
			+ " WHERE n_cartella = " + (String) h.get("n_cartella");

			String myselCur = mysel + " ORDER BY skr_data DESC, skr_progr DESC";

			System.out.println("SkMPalRicoEJB: QueryKeyLast - myselCur=[" + myselCur + "]");
			ISASCursor dbcur = dbc.startCursor(myselCur);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			if (vdbr != null && vdbr.size() > 0) {
				for (int i = 0; i < vdbr.size(); i++) {
					ISASRecord dbrX = (ISASRecord) vdbr.get(i);

					decodificaTipoRicovero(dbc, dbrX);
					// if (dbrX != null) {
					// String tipo = dbrX.get("skr_tipo").toString();
					// if (tipo.equals("1"))
					// dbrX.put("skr_tipo_desc", "Ricovero");
					// else if (tipo.equals("2"))
					// dbrX.put("skr_tipo_desc", "Intervento 118");
					// else
					// dbrX.put("skr_tipo_desc", "Pronto Soccorso");
					// }
				}

				ISASRecord dbr_1 = (ISASRecord) vdbr.firstElement();
				String myselRec = mysel + " AND skr_progr = " + dbr_1.get("skr_progr");
				System.out.println(nomeEJB + " QueryKeyLast - myselRec=[" + myselRec + "]");

				dbr = dbc.readRecord(myselRec);

				if (dbr != null) {
					// String tipo = dbr.get("skr_tipo").toString();
					// if (tipo.equals("1"))
					// dbr.put("skr_tipo_desc", "Ricovero");
					// else if (tipo.equals("2"))
					// dbr.put("skr_tipo_desc", "Intervento 118");
					// else
					// dbr.put("skr_tipo_desc", "Pronto Soccorso");

					decodificaTipoRicovero(dbc, dbr);
					dbr.put("griglia", (Vector) vdbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKeyLast()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		System.out.println(nomeEJB + " query() - H == " + h.toString());

		boolean done = false;
		ISASConnection dbc = null;

		String cartella = null;


		try {
			cartella = h.get("n_cartella").toString();
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
			throw new SQLException(nomeEJB + " ERRORE queryKey() MANCA CHIAVE - " + e);
		}

		try {
			dbc = super.logIn(mylogin);

			String myselect = "SELECT * FROM skmpal_ricoveri WHERE " 
					+ "n_cartella = " + cartella
					+ " ORDER BY skr_data DESC ";

			System.out.println(nomeEJB + " query skmpal_ricoveri() -  " + myselect);

			ISASCursor dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;

			return vdbr;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE query() - " + e);
			throw new SQLException(nomeEJB + " ERRORE query() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e) {
					System.out.println(nomeEJB + " ERRORE query() - " + e);
				}
			}
		}
	}

	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		System.out.println(nomeEJB + " insert() - H == " + h.toString());

		boolean done = false;
		String n_cartella = null;

		int progr = 0;
		ISASConnection dbc = null;

		try {
			n_cartella = (String) h.get("n_cartella");
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE insert() - MANCA CHIAVE - " + e);
			throw new SQLException(nomeEJB + " ERRORE insert() - MANCA CHIAVE - " + e);
		}

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			ISASRecord dbr = dbc.newRecord("skmpal_ricoveri");

			String selProg = "SELECT MAX(skr_progr) progr FROM skmpal_ricoveri WHERE " + "n_cartella = " + n_cartella;
			ISASRecord dbrProg = dbc.readRecord(selProg);

			if (dbrProg.get("progr") != null)
				progr = Integer.parseInt("" + dbrProg.get("progr"));

			progr++;

			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}

			dbr.put("skr_progr", "" + progr);
			System.out.println("REC CHE SCRIVO == " + dbr.getHashtable().toString());
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM skmpal_ricoveri WHERE n_cartella = " + n_cartella 
					+ " AND skr_progr = " + progr;

			dbr = dbc.readRecord(myselect);
			System.out.println(nomeEJB + " insert() - select - " + myselect);

			if (dbr != null)
				dbr.put("griglia", (Vector) leggiTuttiRec(dbc, n_cartella));

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (DBRecordChangedException e) {
			System.out.println(nomeEJB + " ERRORE insert() - " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(nomeEJB + " Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " ERRORE insert() - " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException(nomeEJB + " Errore eseguendo una rollback() - " + e1);
			}

			throw e;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE insert() - " + e);
			try {
				dbc.rollbackTransaction();
			} catch (Exception ex) {
				throw new SQLException("Errore eseguendo una rollback() - " + ex);
			}

			throw new SQLException(nomeEJB + " ERRORE insert() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e) {
					System.out.println(nomeEJB + " ERRORE insert() - " + e);
				}
			}
		}
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		String cartella = null;
		String progr = null;

		try {
			cartella = dbr.get("n_cartella").toString();
			progr = "" + dbr.get("skr_progr");
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE update - MANCA CHIAVE " + e);
			throw new SQLException(nomeEJB + " ERRORE update - MANCA CHIAVE " + e);
		}
		try {
			dbc = super.logIn(mylogin);
			dbc.writeRecord(dbr);

			String myselect = "SELECT * FROM skmpal_ricoveri WHERE " + "n_cartella = " + cartella
					+ " AND skr_progr = " + progr;
			dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				// String tipo = dbr.get("skr_tipo").toString();
				// if (tipo.equals("1"))
				// dbr.put("skr_tipo_desc", "Ricovero");
				// else if (tipo.equals("2"))
				// dbr.put("skr_tipo_desc", "Intervento 118");
				// else
				// dbr.put("skr_tipo_desc", "Pronto Soccorso");
				decodificaTipoRicovero(dbc, dbr);

				dbr.put("griglia", (Vector) leggiTuttiRec(dbc, cartella));
			}
			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (DBRecordChangedException e) {
			System.out.println(nomeEJB + " ERRORE update() - " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " ERRORE update() - " + e);
			throw e;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE update() - " + e);
			throw new SQLException(nomeEJB + " ERRORE update() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e) {
					System.out.println(nomeEJB + " ERRORE update() - " + e);
				}
			}
		}
	}

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			System.out.println(nomeEJB + " ERRORE delete() - " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " ERRORE delete() - " + e);
			throw e;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE delete() - " + e);
			throw new SQLException(nomeEJB + " ERRORE delete() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e) {
					System.out.println(nomeEJB + " ERRORE delete() - " + e);
				}
			}
		}
	}

	private Vector leggiTuttiRec(ISASConnection mydbc, String cart) throws Exception {
		String myselect = "SELECT * FROM skmpal_ricoveri" 
				+ " WHERE n_cartella = " + cart
				+ " ORDER BY skr_data DESC, skr_progr DESC";

		ISASCursor dbcur = mydbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();

		dbcur.close();

		for (int i = 0; i < vdbr.size(); i++) {
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			if (dbr != null) {
				decodificaTipoRicovero(mydbc, dbr);
				// String tipo = dbr.get("skr_tipo").toString();
				// if (tipo.equals("1"))
				// dbr.put("skr_tipo_desc", "Ricovero");
				// else if (tipo.equals("2"))
				// dbr.put("skr_tipo_desc", "Intervento 118");
				// else
				// dbr.put("skr_tipo_desc", "Pronto Soccorso");
			}
		}

		return vdbr;
	}
}

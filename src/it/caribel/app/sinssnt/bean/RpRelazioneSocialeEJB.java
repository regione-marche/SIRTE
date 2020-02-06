package it.caribel.app.sinssnt.bean;
//============================================================================
// CARIBEL S.r.l.
//----------------------------------------------------------------------------
//
// ---- EJB di gestione - SINS_PAI ----
//clacolo del piano assistenziale individualizzato OMEGNA
//============================================================================

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.sinssnt.connection.*;

public class RpRelazioneSocialeEJB extends SINSSNTConnectionEJB {

	public RpRelazioneSocialeEJB() {
	}

	private String msg = "Mancano i diritti per leggere il record";
	private String nomeEJB = "RelazioneSocialeEJB";
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();

	public ISASRecord queryKey(myLogin mylogin, Hashtable h)
			throws SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			debugMessage(nomeEJB + " queryKey h=" + h.toString());
			ISASRecord dbr = leggiDati(dbc, h);
			
			//decodifica del comune
			if((dbr != null) && (dbr.get("citta") != null))
				dbr.put("des_citta", ISASUtil.getDecode(dbc, "comuni",
					"codice", dbr.get("citta"), "descrizione"));
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + ".queryKey(): ");
			e.printStackTrace();
			throw new CariException(msg, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
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
public ISASRecord queryKeyLast(myLogin mylogin, Hashtable h)
			throws SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			debugMessage(nomeEJB + " queryKeyLast h=" + h.toString());
			String cartella = (String)h.get("n_cartella").toString();
			String 	myselect = "select r.* from rp_relazione_sociale r"
					+ " WHERE n_cartella = " + cartella + " and data_ins in (select max(data_ins) from rp_relazione_sociale where n_cartella = "+ cartella + ")" ;
	
			ISASRecord dbr = dbc.readRecord(myselect);
			
			//decodifica del comune
			if((dbr != null) && (dbr.get("citta") != null))
				dbr.put("des_citta", ISASUtil.getDecode(dbc, "comuni",
					"codice", dbr.get("citta"), "descrizione"));
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + ".queryKey(): ");
			e.printStackTrace();
			throw new CariException(msg, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
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
	
	
	private ISASRecord leggiDati(ISASConnection dbc, Hashtable h)
			throws Exception {
		String progr;
		String myselect = "";
		String cartella = "";
		String stato = "";
		String data = "";
		try {
			data = h.get("data_ins")!=null?h.get("data_ins").toString():"";
			progr = h.get("progr")!=null?h.get("progr").toString():"1";
			cartella = h.get("n_cartella")!=null?h.get("n_cartella").toString():"";
			myselect = "SELECT r.* from rp_relazione_sociale r"
					+ " WHERE n_cartella = " + cartella 
					+ " AND data_ins = " + dbc.formatDbDate(data)
					+ " AND progr = " + progr;

			System.out.println("QueryKey su rp_relazione_sociale: " + myselect);
			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr != null) {// decodifiche varie
				String cart = (String) ISASUtil.getObjectField(dbr,
						"n_cartella", 'I');
				// debugMessage("cartella="+cart);
				if ((cart != null) && !cart.equals("")) {
					String strCogn = ISASUtil.getDecode(dbc, "cartella",
							"n_cartella", cart, "cognome");
					String strNome = ISASUtil.getDecode(dbc, "cartella",
							"n_cartella", cart, "nome");
					dbr.put("des_cartella", strCogn.trim() + " " + strNome.trim());
				}
			}
			return dbr;
		} catch (Exception e) {
			System.out.println("Errore eseguendo una leggiDati: ");
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una leggiDati: ");
			// return null;
		}
	}

	public String duplicateChar(String s, String c) {
		if ((s == null) || (c == null))
			return s;
		if (s.equals(""))
			return s;
		String mys = new String(s);
		int p = 0;
		while (true) {
			int q = mys.indexOf(c, p);
			if (q < 0)
				return mys;
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException,
			CariException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);

			String myselect = getSelect(dbc, h);
			ISASCursor dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
		
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw new CariException(msg, -2);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ");
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

	private String getSelect(ISASConnection dbc, Hashtable h) {

		String myselect = "SELECT r.* from rp_relazione_sociale r"
				+ " WHERE n_cartella = " + h.get("n_cartella")
				//+ " AND data_ins = " + dbc.formatDbDate(h.get("data_ins").toString())
				+ " ORDER BY data_ins DESC";

		return myselect;
	}

	
	public Vector queryPaginate(myLogin mylogin, Hashtable h)
			throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = getSelect(dbc, h);

			System.out.println("QueryPaginate su rp_relazione_sociale: "+ myselect);
			ISASCursor dbcur = dbc.startCursor(myselect, 200);

			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryPaginate()  ");
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

	public ISASRecord insert(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {

		boolean done = false;
		debugMessage("INSERT h=" + h.toString());
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);

			ISASRecord dba = dbc.newRecord("rp_relazione_sociale");
			this.setISAS(dbc, "rp_relazione_sociale", dba, h);
			
			//calcolo il progressivo
			int progr = getProgressivo(dbc, h.get("n_cartella").toString());
			dba.put("progr", ""+progr);
			System.out.println(nomeEJB + " insert VADO A SCRIVERE: " + dba.getHashtable().toString());
			dbc.writeRecord(dba);
			
			h.put("progr", ""+progr);
			ISASRecord dbret = leggiDati(dbc, h);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbret;
		} catch (CariException ce) {
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException(e.getMessage());
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		debugMessage("UPDDATE dbr=" + dbr.getHashtable().toString());
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			dbc.writeRecord(dbr);
			dbc.commitTransaction();

			// lettura record
			ISASRecord dbret = leggiDati(dbc, dbr.getHashtable());

			dbc.close();
			super.close(dbc);
			done = true;
			return dbret;

		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException(
						"Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - "
						+ e1);
			}
			throw new SQLException("Errore eseguendo una update() - " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	// public void deleteAll(myLogin mylogin,ISASRecord dbr)
	public void deleteAll(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			debugMessage("deleteAll " + h);
			DeleteRow(dbc, h);
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			throw new SQLException("Errore eseguendo una delete() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}

	private void DeleteRow(ISASConnection dbc, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		try {
			String sel = "SELECT * FROM rp_relazione_sociale"
					+ " WHERE n_cartella = '" + h.get("n_cartella") + "'"
					+ " AND dtinse="
					+ formatDate(dbc, h.get("dtinse").toString());
			ISASRecord dbr = dbc.readRecord(sel);
			dbc.deleteRecord(dbr);

		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una DeleteRow() - " + e);
		}
	}

	private ISASRecord setISAS(ISASConnection dbc, String nome_tab,
			ISASRecord dbred, Hashtable strutt) throws Exception {
		try {
			ISASMetaInfo mt = new ISASMetaInfo(dbc, nome_tab);
			Enumeration en = mt.getCampi();
			while (en.hasMoreElements()) {
				String campo = en.nextElement().toString();
				String tipo = mt.getType(campo);
				campo = campo.toLowerCase();
				if (tipo.equalsIgnoreCase("NUMBER")
						|| tipo.equalsIgnoreCase("INTEGER")) {
					if (campo.equals("n_cartella") && strutt.get(campo) == null)
						dbred.put(campo, strutt.get(campo));
					else
						dbred.put(campo, strutt.get(campo) != null ? strutt
								.get(campo) : new Integer(0));
				} else if (tipo.equalsIgnoreCase("DATE")) {
					if (!getVal(strutt.get(campo)).equals(""))
						dbred.put(campo, ndf.formDate(
								getVal(strutt.get(campo)), "aaaa-mm-gg"));
				} else
					dbred.put(campo, getVal(strutt.get(campo)));
			}
			return dbred;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	
	private int getProgressivo(ISASConnection mydbc, String n_cartella) throws Exception {
		int intProgressivo = 0;

		String myselect = "SELECT MAX(progr) max_progr" 
				+ " FROM rp_relazione_sociale"
				+ " WHERE n_cartella = " + n_cartella ; 

		ISASRecord dbr = mydbc.readRecord(myselect);
		if ((dbr != null) && (dbr.get("max_progr") != null))
			intProgressivo = ISASUtil.getIntField(dbr, "max_progr");

		intProgressivo++;
		return intProgressivo;
	}

	
	private static String getVal(Object o) {
		if (o == null)
			return "";
		else if (o.toString().trim().equals("null"))
			return "";
		else
			return o.toString().trim();
	}

}

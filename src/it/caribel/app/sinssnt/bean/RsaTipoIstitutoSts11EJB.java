package it.caribel.app.sinssnt.bean;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//
// 02/07/2009 - EJB di connessione alla procedura SINS Tabella rsa_tipo_tariffa
//
// Elisa Croci
//
//==========================================================================

import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class RsaTipoIstitutoSts11EJB extends SINSSNTConnectionEJB {
	private static String nomeEJB = "2-RsaTipoIstitutoSts11EJB.";
	private static final String MIONOME = "7-RsaTipoIstitutoSts11EJB.";

	public RsaTipoIstitutoSts11EJB() {
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException, Exception {
		System.out.println(nomeEJB + " queryKey -- H == " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			ISASRecord dbr = selectTipoTariffa(dbc, h);

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(nomeEJB + " Errore eseguendo una queryKey()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + " QueryKey(): " + e1);
				}
			}
		}
	} // fine query key

	//	public Vector query_combo_orgddd(myLogin mylogin, Hashtable h) throws SQLException {
	//		boolean done = false;
	//		String punto = MIONOME + "query_combo_org ddd";
	//		ISASConnection dbc = null;
	//		stampa(punto + "dati che ricevo>" + h + "\n");
	//		try {
	//			dbc = super.logIn(mylogin);
	//			String myselect = "SELECT org_codice codice,  org_descri descrizione FROM rsa_organizzazione ORDER BY org_descri";
	//			stampa(punto + "Query: " + myselect);
	//			ISASCursor 
	//			Vector vdbr = dbc.readRecord(myselect);
	//			dbc.close();
	//			super.close(dbc);
	//			done = true;
	//			return dbr;
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//			throw new SQLException("Errore eseguendo una queryKey()  ");
	//		} finally {
	//			if (!done) {
	//				try {
	//					dbc.close();
	//					super.close(dbc);
	//				} catch (Exception e1) {
	//					System.out.println(e1);
	//				}
	//			}
	//		}
	//	}

	public Vector query_combo_org(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		String punto = MIONOME + "query_combo_org ";
		stampa(punto + "inzio con dati>" + h + "\n");

		Hashtable res = new Hashtable();
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		Vector vdbrAll = new Vector();

		try {
			dbc = super.logIn(mylogin);
			String myselect = " SELECT org_codice codice,  org_descri descrizione FROM rsa_organizzazione ORDER BY org_descri ";
			stampa(punto + " Query>" + myselect + "\n");

			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			stampa(punto + " record letti>" + vdbr.size() + "<\n ");
			stampa(punto + "dati recuperati >" + (vdbr != null ? vdbr.size() + "" : " non dati "));
			if (vdbr != null) {
				ISASRecord dbr = dbc.newRecord("rsa_organizzazione");
				dbr.put("codice", "");
				dbr.put("descrizione", ".");
				vdbrAll.add(dbr);
				for (int i = 0; i < vdbr.size(); i++) {
					dbr = (ISASRecord) vdbr.get(i);
					vdbrAll.add(dbr);
				}
				stampa(punto + " record restituisco>" + vdbrAll.size() + "<\n ");
			}

			if (dbcur != null)
				dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbrAll;
		} catch (Exception e) {
			System.out.println("Errore eseguendo RSA ORGANIZZAZIONE: query_combo " + e);
			throw new SQLException("Errore eseguendo RSA ORGANIZZAZIONE: query_combo ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println("Errore eseguendo RSAORGMOD -- combo " + e2);
				}
			}
		}
	}

	public Vector query_combo_ist(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		String punto = MIONOME + "query_combo_ist ";
		stampa(punto + "\n Dati che ricevo>" + h + "\n");

		Hashtable res = new Hashtable();
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		Vector vdbrAll = new Vector();
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT codice,  descrizione FROM rsa_tipo_istituto ORDER BY descrizione";
			stampa(punto + " query>" + myselect + "\n");
			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			stampa(punto + "dati recuperati >" + (vdbr != null ? vdbr.size() + "" : " non dati "));
			if (dbcur != null)
				dbcur.close();

			if (vdbr != null) {
				ISASRecord dbr = dbc.newRecord("rsa_tipo_istituto");
				dbr.put("codice", "");
				dbr.put("descrizione", ".");
				vdbrAll.add(dbr);
				for (int i = 0; i < vdbr.size(); i++) {
					dbr = (ISASRecord) vdbr.get(i);
					vdbrAll.add(dbr);
				}
				stampa(punto + " record restituisco>" + vdbrAll.size() + "<\n ");
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return vdbrAll;
		} catch (Exception e) {
			System.out.println(nomeEJB + " Errore eseguendo query_combo() [" + e + "]");
			throw new SQLException(nomeEJB + " Errore eseguendo query_combo() ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(nomeEJB + "  Errore eseguendo Rquery_combo() " + e2);
				}
			}
		}
	}// query_combo

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	private ISASRecord selectTipoTariffa(ISASConnection dbc, Hashtable h) throws SQLException, Exception {
		String codice = "";

		try {
			codice = h.get("tar_codice").toString();
		} catch (Exception e) {
			System.out.println(nomeEJB + " selectTipoTariffa() -- manca codice! [" + e + "] ");
			throw e;
		}

		String myselect = "SELECT * FROM rsa_tipo_tariffa WHERE  tar_codice = '" + codice + "' ";
		System.out.println(nomeEJB + " QueryKey == " + myselect);

		ISASRecord dbr = dbc.readRecord(myselect);

		if (dbr != null) {
			// decodifica tipo istituto
			if (dbr.get("tar_tipoist") != null && !dbr.get("tar_tipoist").toString().equals("")) {
				String desc_tipoist = ISASUtil.getDecode(dbc, "rsa_tipo_istituto", "codice", dbr.get("tar_tipoist").toString(),
						"descrizione");

				dbr.put("desc_tipoist", desc_tipoist);
			}

			// decod tipo organizzazione
			if (dbr.get("tar_org_mod") != null && !dbr.get("tar_org_mod").toString().equals("")) {
				String desc_tiporg = ISASUtil.getDecode(dbc, "rsa_organizzazione", "org_codice", dbr.get("tar_org_mod").toString(),
						"org_descri");

				dbr.put("desc_tiporg", desc_tiporg);
			}

			// decod tipo assistito
			if (dbr.get("cod_tipo_assistito") != null && !dbr.get("cod_tipo_assistito").toString().equals("")) {
				String desc_tipass = ISASUtil.getDecode(dbc, "rsa_tipo_assistito", "codice", dbr.get("cod_tipo_assistito").toString(),
						"descrizione");

				dbr.put("desc_tipass", desc_tipass);
			}
		}

		return dbr;
	}

	public String duplicateChar(String s, String c) {
		if ((s == null) || (c == null))
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

	public Vector queryPaginate(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = MIONOME + "queryPaginate ";
		stampa(punto + " queryPaginate - Hash: " + h.toString());
		boolean done = false;
		String scr = null;
		ISASConnection dbc = null;
		String descrizione = ISASUtil.getValoreStringa(h, "descrizione");
		String tipoIstituto = ISASUtil.getValoreStringa(h, "tar_tipoist");
		String modulo = ISASUtil.getValoreStringa(h, "tar_org_mod");
		try {
			dbc = super.logIn(mylogin);

			String myselect = "select  i.st_nome as tar_descri, i.ist_codice as tar_codice, rsa.tipoist as tar_tipoist, rsa.org_mod as tar_org_mod, rsa.codice_ist_sts11 "
					+ " from rsa_tipologia_istituto rsa, istituti i where i.ist_codice = rsa.codice_ist ";

			//			and i.st_nome like 'CAS%'
			//			and rsa.tipoist = '1'
			//			and rsa.org_mod = '2'

			if (ISASUtil.valida(tipoIstituto)) {
				myselect += " and rsa.tipoist = '" + tipoIstituto + "'";
			}

			if (ISASUtil.valida(modulo)) {
				myselect += " and rsa.org_mod = '" + modulo + "' ";
			}

			if (ISASUtil.valida(descrizione)) {
				myselect += "and i.st_nome like '" + descrizione + "%'";
			}
			myselect += "order by i.st_nome  ";
			System.out.println(nomeEJB + " QueryPaginate() - select == " + myselect);
			ISASCursor dbcur = dbc.startCursor(myselect);

			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);

			if (vdbr != null && vdbr.size() > 0) {
				for (int i = 0; i < vdbr.size() - 1; i++) {
					ISASRecord r = (ISASRecord) vdbr.get(i);
					if (r != null) {
						// decodifica tipo istituto
						if (r.get("tar_tipoist") != null && !r.get("tar_tipoist").toString().equals("")) {
							String desc_tipoist = ISASUtil.getDecode(dbc, "rsa_tipo_istituto", "codice", r.get("tar_tipoist").toString(),
									"descrizione");
							r.put("desc_tipoist", desc_tipoist);
						}
						// decod tipo organizzazione
						if (r.get("tar_org_mod") != null && !r.get("tar_org_mod").toString().equals("")) {
							String desc_tiporg = ISASUtil.getDecode(dbc, "rsa_organizzazione", "org_codice", r.get("tar_org_mod")
									.toString(), "org_descri");
							r.put("desc_tiporg", desc_tiporg);
						}
					}
				}
			}

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			stampa(punto + "\n dati che invio>" + (vdbr != null ? vdbr.size() + "" : "non dati "));
			return vdbr;
		} catch (Exception e) {
			System.out.println(nomeEJB + e);
			throw new SQLException(nomeEJB + "Errore eseguendo una QueryPaginate()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + e1);
				}
			}
		}
	} // fine paginate

	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		System.out.println(nomeEJB + " - insert(), HASH: " + h.toString());

		boolean done = false;
		String codice = null;
		ISASConnection dbc = null;

		try {
			codice = h.get("tar_codice").toString();
		} catch (Exception e) {
			System.out.println(nomeEJB + e);
			throw new SQLException(nomeEJB + " - insert() ==  manca il codice");
		}

		try {
			dbc = super.logIn(mylogin);
			ISASRecord dbr = dbc.newRecord("rsa_tipo_tariffa");

			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = n.nextElement().toString();
				dbr.put(e, h.get(e));
			}

			dbc.writeRecord(dbr);

			dbr = selectTipoTariffa(dbc, h);

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (DBRecordChangedException e) {
			System.out.println(nomeEJB + " insert() " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " insert() " + e);
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(nomeEJB + " insert() " + e2);
				}
			}
		}
	} // fine insert

	public ISASRecord update(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		System.out.println(nomeEJB + " update() - DBR == " + dbr.getHashtable().toString());

		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			dbc.writeRecord(dbr);

			dbr = selectTipoTariffa(dbc, dbr.getHashtable());

			dbc.close();
			super.close(dbc);
			done = true;

			return dbr;
		} catch (DBRecordChangedException e) {
			System.out.println(nomeEJB + " - update() - " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " - update() - " + e);
			throw e;
		} catch (Exception e1) {
			System.out.println(nomeEJB + " - update() - " + e1);
			throw new SQLException("Errore eseguendo una update() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(nomeEJB + " - update() - " + e2);
				}
			}
		}
	} // fine update

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		System.out.println(nomeEJB + " delete() - DBR == " + dbr.getHashtable().toString());

		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);

			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			System.out.println(nomeEJB + " delete() - " + e);
			throw e;
		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " delete() - " + e);
			throw e;
		} catch (Exception e1) {
			System.out.println(nomeEJB + " delete() - " + e1);
			throw new SQLException(nomeEJB + " delete() - " + e1);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(nomeEJB + " delete() - " + e2);
				}
			}
		}
	} // fine delete
}

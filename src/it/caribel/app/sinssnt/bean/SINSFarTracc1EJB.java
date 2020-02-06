package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 30/04/2012 - EJB di gestione flussi far molise
//
// ============================================================================

import it.caribel.app.sinssnt.util.Costanti;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASCursorLock;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

public class SINSFarTracc1EJB extends SINSSNTConnectionEJB {

	public SINSFarTracc1EJB() {
	}

	private static final String MIONOME = "43-SINSFarTracc1EJB.";
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	/* boffa inizio */

	public ISASRecord query_keyCartella(myLogin mylogin, Hashtable h) throws SQLException, CariException {
		String punto = MIONOME + "query_keyCartella ";
		boolean done = false;
		ISASConnection dbc = null;
		String mex = "";
		ISASRecord dbr = null;
		stampa(punto + "Inizio con dati>" + h + "\n");
		try {
			dbc = super.logIn(mylogin);
			String nCartella = ISASUtil.getValoreStringa(h, "n_cartella");

			String myselect = " select * from cartella where n_cartella = " + nCartella;
			stampa(punto + " Query>" + myselect + "\n");
			dbr = dbc.readRecord(myselect);

			if (dbr != null) {
				stampa(punto + "Dati recuperati sono: " + dbr.getHashtable());

				myselect = "select data_variazione ,regione, usl, team_numero,citta from anagra_C c where n_cartella = " + nCartella
						+ " and data_variazione in ( select max(data_variazione) from anagra_c where n_cartella = c.n_cartella ) ";
				stampa(punto + " Query>" + myselect + "\n");
				ISASRecord dbrAnagraC = dbc.readRecord(myselect);

				dbr.put("regione", ISASUtil.getValoreStringa(dbrAnagraC, "regione"));
				dbr.put("usl", ISASUtil.getValoreStringa(dbrAnagraC, "usl"));
				dbr.put("team_numero", ISASUtil.getValoreStringa(dbrAnagraC, "team_numero"));

				dbr.put("annonascita", recuperaAnno(ISASUtil.getValoreStringa(dbr, "data_nasc")));

				String codCittadinanza = ISASUtil.getValoreStringa(dbr, "cittadinanza");
				dbr.put("cittadinanza_des", recuperaCittadinanza(dbc, codCittadinanza));

				String codComune = ISASUtil.getValoreStringa(dbrAnagraC, "citta");
				dbr.put("citta", codComune);
				dbr.put("citta_des", recuperaDescrizioneComune(dbc, codComune));

			} else {
				stampa(punto + "non ho recuperato informazioni");
			}

			stampa(punto + "\n dati che invio>" + (dbr != null ? dbr.getHashtable() + "" : " \n non ho recuperato dati "));
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(dbr);
			throw ce;
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
			System.out.println("nella finally");
		}
	}

	private Object recuperaAnno(String data) {
		String punto = MIONOME + "recuperaAnno ";
		String anno = "";
		if (ISASUtil.valida(data)) {
			try {
				anno = data.substring(0, 4);
			} catch (Exception e) {
				stampa(punto + "\n Errore nel recuperare anno");
				e.printStackTrace();
			}
		}
		return anno;
	}

	public Vector queryPaginate(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = MIONOME + "queryPaginate ";
		stampa(punto + " Inizio con dati>" + h + "<\n");
		boolean done = false;
		String scr = " ";
		String scr1 = " ";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);

			String myselect = " SELECT data_nasc, cognome, nome, tracc1_cod, ft.n_cartella, ft.tipo, ft.fl_trasmissione, ft.dt_trasmissione, ft.ammis_data, ft.dimiss_data"
					+ " FROM FAR_TRACCIATO1 FT, CARTELLA C WHERE FT.N_CARTELLA =  C.N_CARTELLA ";

			String cognome = ISASUtil.getValoreStringa(h, "cognome");
			String nome = ISASUtil.getValoreStringa(h, "nome");
			String dtInizio = ISASUtil.getValoreStringa(h, "data_inizio");
			String dtFine = ISASUtil.getValoreStringa(h, "data_fine");
			String tipo = ISASUtil.getValoreStringa(h, "tipo");
			String nCartella = ISASUtil.getValoreStringa(h, "n_cartella");
			//			data_inizio=2012-01-01, cognome=A, start=0, stop=12, nome=C, data_fine=2012-02-02
			if (ISASUtil.valida(cognome)) {
				myselect += " AND UPPER(C.COGNOME) LIKE '" + cognome + "%' ";
			}

			if (ISASUtil.valida(nCartella)) {
				myselect += " AND C.N_CARTELLA = " + nCartella + " ";
			}

			if (ISASUtil.valida(nome)) {
				myselect += " AND UPPER(C.NOME) LIKE '" + nome + "%' ";
			}
			if (ISASUtil.valida(dtInizio) && dtInizio.length() >= 10) {
				if (tipo.equalsIgnoreCase("A")) {
					myselect += " AND FT.AMMIS_DATA >= " + dbc.formatDbDate(dtInizio);
				} else {
					if (tipo.equalsIgnoreCase("D")) {
						myselect += " AND FT.DIMISS_DATA >= " + dbc.formatDbDate(dtInizio);
					} else {
						myselect += " AND ( FT.AMMIS_DATA >= " + dbc.formatDbDate(dtInizio) + " OR FT.DIMISS_DATA >= "
								+ dbc.formatDbDate(dtInizio) + " ) ";
					}
				}
			}

			if (ISASUtil.valida(dtFine) && dtFine.length() >= 10) {
				if (tipo.equalsIgnoreCase("A")) {
					myselect += " AND FT.AMMIS_DATA <= " + dbc.formatDbDate(dtFine);
				} else {
					if (tipo.equalsIgnoreCase("D")) {
						myselect += " AND FT.DIMISS_DATA <= " + dbc.formatDbDate(dtFine);
					} else {
						myselect += " AND ( FT.AMMIS_DATA <= " + dbc.formatDbDate(dtFine) + " OR FT.DIMISS_DATA <= "
								+ dbc.formatDbDate(dtFine) + " ) ";
					}
				}
			}

			if (ISASUtil.valida(tipo)) {
				if (tipo.equalsIgnoreCase("A")) {
					myselect += " AND FT.AMMIS_DATA IS NOT NULL ";
				} else {
					if (tipo.equalsIgnoreCase("D")) {
						myselect += " AND FT.DIMISS_DATA IS NOT NULL ";
					}
				}
			}
			stampa(punto + " Query>" + myselect + "\n");
			ISASCursor dbcur = dbc.startCursor(myselect);
			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			//			if ((vdbr != null) && (vdbr.size() > 0))
			//				for (int i = 0; i < vdbr.size() - 1; i++) {
			//					ISASRecord dbr = (ISASRecord) vdbr.elementAt(i);
			//					dbr.put("desc_nasc", util.getDecode(dbc, "comuni", "codice", (String) util.getObjectField(dbr, "cod_com_nasc", 'S'),
			//							"descrizione"));
			//					dbr.put("desc_res", util.getDecode(dbc, "comuni", "codice", (String) util.getObjectField(dbr, "citta", 'S'),
			//							"descrizione"));
			//				}
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
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

	//	public ISASRecord insert(myLogin mylogin, Hashtable h, Vector hv) throws DBRecordChangedException, ISASPermissionDeniedException,
	public ISASRecord insert(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = MIONOME + "insert ";
		boolean done = false;
		ISASConnection dbc = null;
		stampa(punto + " dati che ricevo>" + (h != null ? "dati che ricevo>" + h : " non ho dati") + "\n");
		//		stampa(punto + "dimensione vettore>" + (hv != null ? " vettore>" + hv.size() : " non ho dati") + "\n");

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			//=========================================================
			// Su chiavi_libere la chiave � del tipo   TRACC1FAR + anno (server)
			//=========================================================
// da problemi di generazione per anno 
			//			String anno = getAnno();
//			String chiave = "TRACC1FAR" + anno;
			String chiave = "TRACC1FAR";
			Integer tracc1Cod = new Integer(selectProgressivo(dbc, chiave));
			dbc.commitTransaction();
			stampa(punto + " chiave>" + chiave + "< progressivo>" + tracc1Cod + "<\n");

			ISASRecord dbr = dbc.newRecord("far_tracciato1");
			dbr.put("tracc1_cod", tracc1Cod);
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}
			aggiornaDatiDaAster(dbc, dbr);
			dbc.writeRecord(dbr);
			String myselect = " select * from far_tracciato1 where tracc1_cod = " + tracc1Cod;
			dbr = dbc.readRecord(myselect);

			recuperaDecodificheFarTracciato1(dbc, dbr);
			Vector tracciato2 = recuperaElementiTracciato2(dbc, tracc1Cod + "");
			dbr.put("griglia_tracc2", tracciato2);

			dbc.close();
			super.close(dbc);
			done = true;
			stampa(punto + "\n FINE ESECUZIONE \n" + "dati che invio>" + dbr.getHashtable() + "\n");

			return dbr;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una insert() - " + e);
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

	private void recuperaDecodificheFarTracciato1(ISASConnection dbc, ISASRecord dbr) {
		String punto = MIONOME + "recuperaDecodificheFarTracciato1 ";
		stampa(punto + " \n recupera decodifiche ");
		try {

			String nCartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
			recuperaInfoCartella(dbc, nCartella, dbr);

			String codCittadinanza = ISASUtil.getValoreStringa(dbr, "cittadinanza_aster");
			dbr.put("des_cittadin", recuperaCittadinanza(dbc, codCittadinanza));
			String codComune = ISASUtil.getValoreStringa(dbr, "res_comune");
			dbr.put("des_comune", recuperaDescrizioneComune(dbc, codComune));

			String codStatoEstero = ISASUtil.getValoreStringa(dbr, "res_statoestero_aster");
			dbr.put("des_statoestero", recuperaCittadinanza(dbc, codStatoEstero));

			String codiceIstituto = ISASUtil.getValoreStringa(dbr, "erog_codstrutaster");
			dbr.put("des_erog_codstrutaster", recuperaDescrizioneIstituto(dbc, codiceIstituto));
			String codIstSts11 = ISASUtil.getValoreStringa(dbr, "erog_codstrutsts11");
			//			dbr.put("des_erog_codstrutsts11", recuperaCodiceStsIstituto(dbr, codIstSts11));
			dbr.put("uls", ISASUtil.getValoreStringa(dbr, "erog_asl"));

		} catch (ISASMisuseException e) {
			stampa(punto + "\n Errore nella decodifica \n");
			e.printStackTrace();
		}
		stampa(punto + "\n fine descrizione tracciato 1 ");

	}

	private void recuperaInfoCartella(ISASConnection dbc, String nCartella, ISASRecord dbr) {
		String punto = MIONOME + "recuperaInfoCartella ";

		if (ISASUtil.valida(nCartella)) {
			try {
				String query = "select trim(cognome) || ' ' ||  trim(nome) co_nome  ,data_nasc, sesso from cartella where  n_cartella = "
						+ nCartella;
				stampaQuery(punto, query);
				ISASRecord dbrNCartella = getRecord(dbc, query);

				dbr.put("co_nome", ISASUtil.getValoreStringa(dbrNCartella, "co_nome"));
				dbr.put("annonascita", dbrNCartella.get("data_nasc"));
				dbr.put("sesso", ISASUtil.getValoreStringa(dbrNCartella, "sesso"));
			} catch (ISASMisuseException e) {
				e.printStackTrace();
			}
		} else {
			stampa(punto + "\n Non recupero codice della cartella ");
		}
	}

	private String getAnno() {
		Calendar calendar = new GregorianCalendar();
		String anno = calendar.get(Calendar.YEAR) + "";
		return anno;
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws SQLException, CariException {
		String punto = MIONOME + "queryKey ";
		boolean done = false;
		ISASConnection dbc = null;
		String mex = "";
		String codice = "";
		ISASRecord dbr = null;
		stampa(punto + "Inizio con dati>" + h + "\n");
		try {
			dbc = super.logIn(mylogin);
			String codTracciato1 = ISASUtil.getValoreStringa(h, "tracc1_cod");

			String myselect = "SELECT * FROM FAR_TRACCIATO1 WHERE TRACC1_COD = " + codTracciato1;
			stampa(punto + " Query>" + myselect + "\n");
			dbr = dbc.readRecord(myselect);
			String w_codice = "";
			String w_descr = "";
			String w_select = "";
			ISASRecord w_dbr = null;
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				stampa(punto + "Dati recuperati sono: " + h1);
				recuperaDecodificheFarTracciato1(dbc, dbr);
				Vector tracciato2 = recuperaElementiTracciato2(dbc, codTracciato1);
				dbr.put("griglia_tracc2", tracciato2);
			} else {
				stampa(punto + "non ho recuperato informazioni");
			}

			if (!mex.equals(""))
				throw new CariException(mex);
			stampa(punto + "\n dati che invio>" + (dbr != null ? dbr.getHashtable() + "" : " \n non ho recuperato dati "));
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(dbr);
			throw ce;
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
			System.out.println("nella finally");
		}
	}

	private Vector recuperaElementiTracciato2(ISASConnection dbc, String tracc1Cod) {
		String punto = MIONOME + "recuperaElementiTracciato2 ";
		String query = "select * from far_tracciato2 where tracc1_cod = " + tracc1Cod;
		stampaQuery(punto, query);
		Vector elementiTracciato2 = null;
		ISASCursor dbrCursor = null;
		try {
			dbrCursor = dbc.startCursor(query);
			elementiTracciato2 = dbrCursor.getAllRecord();
			dbrCursor.close();

			recuperaDescrizioneTracciato2(dbc, elementiTracciato2);

		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		} finally {
			if (dbrCursor == null) {
				try {
					dbrCursor.close();
				} catch (DBSQLException e) {
					e.printStackTrace();
				} catch (DBMisuseException e) {
					e.printStackTrace();
				} catch (ISASMisuseException e) {
					e.printStackTrace();
				}
			}
		}
		stampa(punto + "ho recuperato elementi>"
				+ ((elementiTracciato2 != null) ? elementiTracciato2.size() + "" : " non ho recuperato dati"));

		return elementiTracciato2;
	}

	private void recuperaDescrizioneTracciato2(ISASConnection dbc, Vector elementiTracciato2) {
		String punto = MIONOME + "recuperaDescrizioneTracciato2 ";
		stampa(punto + " \n inizio con descrizione ");
		LinkedList listaElementiComboBox = new LinkedList();

		listaElementiComboBox.add("tipotrasm");
		listaElementiComboBox.add("valut_tipo");
		listaElementiComboBox.add("valut_vitaquo");
		listaElementiComboBox.add("valut_mobil");
		listaElementiComboBox.add("valut_cognit");
		listaElementiComboBox.add("valut_discomp");

		listaElementiComboBox.add("valut_tratspec1");
		listaElementiComboBox.add("valut_tratspec2");
		listaElementiComboBox.add("valut_tratspec3");
		listaElementiComboBox.add("valut_tratspec4");
		listaElementiComboBox.add("valut_tratspec5");
		listaElementiComboBox.add("valut_tratspec6");
		listaElementiComboBox.add("valut_tratspec7");
		listaElementiComboBox.add("valut_tratspec8");

		listaElementiComboBox.add("valut_sociale");
		listaElementiComboBox.add("valut_finaz");
		LinkedList listaElementi = new LinkedList();

		listaElementi.add("TIPTRASM");
		listaElementi.add("TIPOVALU");
		listaElementi.add("VITAQUOD");
		listaElementi.add("MOBILITA");
		listaElementi.add("COGNITIV");
		listaElementi.add("DISTCOMP");

		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");
		listaElementi.add("TRATSPEC");

		listaElementi.add("FRAGSOCI");
		listaElementi.add("FRAGFINA");

		for (int i = 0; i < elementiTracciato2.size(); i++) {
			ISASRecord dbrTracciato2 = (ISASRecord) elementiTracciato2.get(i);
			stampa(punto + "\n Inizio la decodifica >" + dbrTracciato2.getHashtable());
			String colonnaDb, valoreNelDb, descrizione, codiceTabellaFar;
			if (dbrTracciato2 != null) {
				for (int j = 0; j < listaElementiComboBox.size(); j++) {
					try {
						colonnaDb = listaElementiComboBox.get(j) + "";
						valoreNelDb = ISASUtil.getValoreStringa(dbrTracciato2, colonnaDb);
						if (ISASUtil.valida(valoreNelDb)) {
							codiceTabellaFar = listaElementi.get(j) + "";
							descrizione = recuperaDescrzioneCampo(dbc, codiceTabellaFar, valoreNelDb);
							dbrTracciato2.put(colonnaDb + "_des", descrizione);
						}
					} catch (ISASMisuseException e) {
						e.printStackTrace();
					}
				}
			}
		}
		stampa(punto + " \n Fine ");

	}

	private String recuperaDescrzioneCampo(ISASConnection dbc, String codiceTabellaFar, String valoreNelDb) {
		String punto = MIONOME + "recuperaDescrzioneCampo ";
		String descrizione = "";

		String query = " select * from far_decod where far_cod = '" + codiceTabellaFar + "' and far_val = '" + valoreNelDb + "'";
		ISASRecord dbrFarDecode = getRecord(dbc, query);
		descrizione = ISASUtil.getValoreStringa(dbrFarDecode, "far_descrizione");
		stampa(punto + " cod far>" + codiceTabellaFar + "< valoreDb>" + valoreNelDb + "< desc>" + descrizione + "<" + "query>" + query
				+ "<\n");
		return descrizione;
	}

	private Object recuperaCodiceStsIstituto(ISASRecord dbr, String codiceIstituto) {
		String descrizione = codiceIstituto;
		//		TODO VEDERE COME RECUPERARE  LA DECODIFICA DELL'STS11. la tabella � rsa_tipologia_istituto
		return descrizione;
	}

	private Object recuperaDescrizioneIstituto(ISASConnection dbc, String codiceIstituto) {
		String punto = MIONOME + "recuperaDescrizioneComune ";
		String descrizione = "";
		if (ISASUtil.valida(codiceIstituto)) {
			String query = "select * from istituti where ist_codice = '" + codiceIstituto + "' ";
			stampaQuery(punto, query);
			ISASRecord dbrComune = getRecord(dbc, query);
			descrizione = ISASUtil.getValoreStringa(dbrComune, "st_nome");
		} else {
			stampa(punto + "\n Non recupero codice Comune ");
		}
		return descrizione;
	}

	private Object recuperaDescrizioneComune(ISASConnection dbc, String codComune) {
		String punto = MIONOME + "recuperaDescrizioneComune ";
		String descrizione = "";
		if (ISASUtil.valida(codComune)) {
			String query = "SELECT * FROM COMUNI WHERE CODICE ='" + codComune + "' ";
			stampaQuery(punto, query);
			ISASRecord dbrComune = getRecord(dbc, query);
			descrizione = ISASUtil.getValoreStringa(dbrComune, "descrizione");
		} else {
			stampa(punto + "\n Non recupero codice Comune ");
		}
		return descrizione;
	}

	private String recuperaCittadinanza(ISASConnection dbc, String codCittadinanza) {
		String punto = MIONOME + "recuperaCittadinanza ";
		String descrizione = "";
		if (ISASUtil.valida(codCittadinanza)) {
			String query = "SELECT * FROM cittadin WHERE cd_cittadin= '" + codCittadinanza + "' ";
			stampaQuery(punto, query);
			ISASRecord dbrCittadinanza = getRecord(dbc, query);
			descrizione = ISASUtil.getValoreStringa(dbrCittadinanza, "des_cittadin");
		} else {
			stampa(punto + "\n Non recupero codice cittadinanza");
		}
		return descrizione;
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr, Vector hv) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		String punto = MIONOME + "update ";
		boolean done = false;
		ISASConnection dbc = null;
		stampa(punto + " dati che ricevo>" + (dbr != null ? "dati che ricevo>" + dbr.getHashtable() : " non ho dati") + "\n");
		stampa(punto + "dimensione vettore>" + (hv != null ? " vettore>" + hv.size() : " non ho dati") + "\n");

		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			String tracc1Cod = ISASUtil.getValoreStringa(dbr, "tracc1_cod");
			stampa(punto + "\n dati che aggiorno>" + dbr.getHashtable() + "\n");
			dbr.put("fl_trasmissione", "S");// tutte le volte che salvo, sono pronto per l'invio

			aggiornaDatiDaAster(dbc, dbr);
			stampa(punto + "\n dati che salvo>" + dbr.getHashtable() + "\n");
			dbc.writeRecord(dbr);
			insertFarTracciato2(dbc, hv, tracc1Cod, dbr);

			String myselect = "SELECT * FROM FAR_TRACCIATO1 WHERE TRACC1_COD = " + tracc1Cod;
			stampa(punto + "Query>" + myselect + "\n");
			dbr = dbc.readRecord(myselect);
			recuperaDecodificheFarTracciato1(dbc, dbr);
			Vector tracciato2 = recuperaElementiTracciato2(dbc, tracc1Cod);
			dbr.put("griglia_tracc2", tracciato2);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			stampa(punto + "\n FINE ESECUZIONE \n" + "dati che invio>" + dbr.getHashtable() + "\n");
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				dbc.rollbackTransaction();
			} catch (Exception p) {
				throw new SQLException("DEBUG TRACCIATO1:Errore eseguendo una rollback()" + p);
			}
			throw ce;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception p) {
				throw new SQLException("DEBUG TRACCIATO1:Errore eseguendo una rollback()" + p);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception t) {
				throw new SQLException("DEBUG TRACCIATO1:Errore eseguendo una rollback()" + t);
			}
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception k) {
				throw new SQLException("DEBUG TRACCIATO1:Errore eseguendo una rollback()" + k);
			}
			throw new SQLException("Errore eseguendo una update() - " + e1);
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

	private void aggiornaDatiDaAster(ISASConnection dbc, ISASRecord dbr) {
		String punto = MIONOME + "aggiornaDatiDaAster ";
		stampa(punto + "\n aggiornare i dati");
		try {
			String codiceCittadinanza = ISASUtil.getValoreStringa(dbr, "cittadinanza_aster");
			dbr.put("cittadinanza", recuperaCodiceIsoAlpha(dbc, codiceCittadinanza));
			codiceCittadinanza = ISASUtil.getValoreStringa(dbr, "res_statoestero_aster");
			dbr.put("res_statoestero", recuperaCodiceIsoAlpha(dbc, codiceCittadinanza));

		} catch (ISASMisuseException e) {
			stampa(punto + " Errone nel recupera la codifica aster");
			e.printStackTrace();
		}
	}

	private Object recuperaCodiceIsoAlpha(ISASConnection dbc, String codiceCittadinanza) {
		String punto = MIONOME + "recuperaCodiceIsoAlpha ";
		String descrizione = "";
		String query = "select * from naz_istat_iso3166 where cod_istat = '" + codiceCittadinanza + "' ";
		stampaQuery(punto, query);
		ISASRecord dbrNazIstat = getRecord(dbc, query);
		stampa(punto + "\n dati>" + (dbrNazIstat != null ? dbrNazIstat.getHashtable() + "" : "no dati"));

		descrizione = ISASUtil.getValoreStringa(dbrNazIstat, "iso3166_a2");
		stampa(punto + "\n restituisco>" + descrizione + "<\\n");
		return descrizione;
	}

	private void insertFarTracciato2(ISASConnection dbc, Vector hv, String tracc1Cod, ISASRecord dbrTracciato1) {
		String punto = MIONOME + "insertFarTracciato2 ";
		Hashtable datiTracciato2 = new Hashtable();
		rimuoviTracciato2Esistenti(dbc, tracc1Cod);

		stampa(punto + "Elementi che processo> " + (hv != null ? hv.size() + "" : "NON INSERISCO DATI") + " \n");
		if (hv != null && hv.size() > 0) {
			ISASRecord dbrTracciato2 = null;
			for (int i = 0; i < hv.size(); i++) {
				try {
					datiTracciato2 = (Hashtable) hv.get(i);
					if (datiTracciato2.size() > 0) {
						stampa(punto + " Esamino il vettore: " + i + "\n con dati>" + datiTracciato2 + "\n");
						dbrTracciato2 = dbc.newRecord("far_tracciato2");
						String key, value;

						Iterator dati = datiTracciato2.keySet().iterator();
						while (dati.hasNext()) {
							key = (String) dati.next();
							value = (String) datiTracciato2.get(key);
							dbrTracciato2.put(key, value);
						}
						dbrTracciato2.put("tracc1_cod", tracc1Cod);
						dbrTracciato2.put("tracc2_cod", i + "");

						dbrTracciato2.put("erog_asl", ISASUtil.getValoreStringa(dbrTracciato1, "erog_asl"));
						dbrTracciato2.put("erog_regione", ISASUtil.getValoreStringa(dbrTracciato1, "erog_regione"));
						dbrTracciato2.put("fl_trasmissione", ISASUtil.getValoreStringa(dbrTracciato1, "fl_trasmissione"));
						try {
							dbrTracciato2.put("ammis_data", dbrTracciato1.get("ammis_data"));
						} catch (Exception e) {
							e.printStackTrace();
						}

						stampa(punto + "\n dati che aggiorno" + dbrTracciato2 + "\n");
						dbc.writeRecord(dbrTracciato2);
					} else {
						stampa(punto + " Non ho dati inseriti nel vettore da inserire");
					}
				} catch (ISASMisuseException e) {
					e.printStackTrace();
				} catch (DBSQLException e) {
					e.printStackTrace();
				} catch (DBRecordChangedException e) {
					e.printStackTrace();
				} catch (DBMisuseException e) {
					e.printStackTrace();
				} catch (ISASPermissionDeniedException e) {
					e.printStackTrace();
				}
			}
		} else {
			stampa(punto + "\n non ci sono elementi del tracciato 2 da aggiornare\n");
		}
	}

	private void rimuoviTracciato2Esistenti(ISASConnection dbc, String tracc1Cod) {
		String punto = MIONOME + "rimuoviTracciato2Esistenti ";
		stampa(punto + " rimozione tracciato2 con codice>" + tracc1Cod + "\n");
		String query = "SELECT * FROM FAR_TRACCIATO2 WHERE TRACC1_COD = " + tracc1Cod;
		stampaQuery(punto, query);
		ISASCursor dbrCursor = null;
		try {
			String codTracciato2 = "";
			dbrCursor = dbc.startCursor(query);
			while (dbrCursor.next()) {
				ISASRecord dbrTracc2 = dbrCursor.getRecord();
				codTracciato2 = ISASUtil.getValoreStringa(dbrTracc2, "tracc2_cod");
				stampa(punto + "\n cancello record tracc1_cod>" + ISASUtil.getValoreStringa(dbrTracc2, "TRACC1_COD") + "<tracc2>"
						+ ISASUtil.getValoreStringa(dbrTracc2, "tracc2_cod"));
				ISASRecord dbrTracciato2 = recuperaRecord(dbc, tracc1Cod, codTracciato2);
				if (dbrTracciato2 != null) {
					dbc.deleteRecord(dbrTracciato2);
				}
			}
			dbrCursor.close();
		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
		} finally {
			if (dbrCursor != null) {
				try {
					dbrCursor.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void stampaQuery(String punto, String query) {
		System.out.println(punto + " Query>" + query + "<\n");
	}

	private ISASRecord recuperaRecord(ISASConnection dbc, String tracc1Cod, String tracc2Cod) {
		String punto = MIONOME + "recuperaRecord ";
		String query = " SELECT * FROM FAR_TRACCIATO2 WHERE TRACC1_COD = " + tracc1Cod + " AND TRACC2_COD = " + tracc2Cod;
		stampa(punto + " Query>" + query);
		ISASRecord dbrTracciato2 = getRecord(dbc, query);
		return dbrTracciato2;
	}

	private ISASRecord getRecord(ISASConnection dbc, String query) {
		String punto = MIONOME + ".getRecord ";
		ISASRecord dbr = null;
		try {
			dbr = dbc.readRecord(query);
		} catch (Exception ex) {
			stampa(punto + "Errore nel leggere il record con query>" + query + "<");
		}
		return dbr;
	}

	/* boffa fine */

	public ISASRecord loadandinsertmedico(myLogin mylogin, Hashtable h) throws SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		String mex = "";
		String codice = "";
		ISASRecord dbr = null;
		/*
		        it.pisa.caribel.fassi.InterrogazioneAnagrafica ia =
		          it.pisa.caribel.fassi.InterrogazioneAnagraficaFactory.getRealClass();
		*/
		Object ia = null;
		try {

			dbc = super.logIn(mylogin);
			// Inserisce, se non esiste, il medico nel caso WS!
			dbr = check_medico(dbc, h, ia != null);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(dbr);
			throw ce;
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
			System.out.println("nella finally");
		}

	}

	/**
	 * Verifica l'esistenza del medico; se non c'e' lo inserisce.
	 * Per inserirlo o ha gia' tutti i dati nella hashtable, oppure
	 * invoca il metodo getMedico su InterrogazioneAnagrafica
	 *
	 * @param mylogin
	 * @param datiMedico
	 * @return
	 * @throws SQLException
	 */
	public ISASRecord check_medico(ISASConnection ic, Hashtable datiMedico, boolean insert) throws SQLException {
		Object codiceMedico = datiMedico.get("cod_med");
		if (codiceMedico == null) {
			System.out.println("check_medico: " + "cod_med a null; provo cod_medico...");
			codiceMedico = datiMedico.get("cod_medico");
			if (codiceMedico == null)
				codiceMedico = datiMedico.get("mecodi");
		}
		System.out.println("check_medico(" + codiceMedico + ")");
		try {
			//
			// Test dell'esistenza del medico sul DB
			// In pratica esegue una queryKey
			boolean existMedico = false;
			ISASRecord dbr = null;
			try {
				String myselect = "Select medici.* from medici where " + "mecodi='" + codiceMedico + "'";
				dbr = ic.readRecord(myselect);
				existMedico = (dbr != null);
			} catch (Exception exx) {
				exx.printStackTrace();
			}

			//
			// Fine della queryKey: se esiste gi~E non fa nulla
			// altrimenti esegue una insert sulla tabella medici
			//
			if (insert && !existMedico) {
				System.out.println("Il medico " + codiceMedico + " non esiste: lo creo!");
				raccogliDatiMedicoMancanti(datiMedico);
				ISASRecord dbrNuovo = ic.newRecord("medici");
				Enumeration n = datiMedico.keys();
				while (n.hasMoreElements()) {
					String e = (String) n.nextElement();
					dbrNuovo.put(e, datiMedico.get(e));
				}
				ic.writeRecord(dbrNuovo);
				String myselect = "Select * from medici where " + "mecodi='" + codiceMedico + "'";
				dbrNuovo = ic.readRecord(myselect);
				return dbrNuovo;

			} else
				return dbr;
		} catch (Exception dd) {
			dd.printStackTrace();
		} finally {
		}
		return null;
	}

	/**
	 * Questo metodo verifica se nella hasthable passata ci sono
	 * gia' i dati necessari. Se ci sono non fa nulla; altrimenti
	 * li prova a recuperare invocando getMedico da InterrogazioneAnagrafica
	 *
	 * @param datiMedico
	 */
	private void raccogliDatiMedicoMancanti(Hashtable datiMedico) {
		/*
		        it.pisa.caribel.fassi.InterrogazioneAnagrafica ia =
		          it.pisa.caribel.fassi.InterrogazioneAnagraficaFactory.getRealClass();
		        if (ia!=null){
		                String nome= (String) datiMedico.get( "menome" );
		                String cognome= (String) datiMedico.get( "mecogn" );
		                if (datiMedico.get(ia.FLD_ST_MEDICO_CODICE)!=null)
		                     datiMedico.put("mecodi",
		                                 datiMedico.get(ia.FLD_ST_MEDICO_CODICE));
		                else
		                if ( datiMedico.get(ia.FLD_OBJ_ANAG_CODICE_MEDICO)!=null)
		                     datiMedico.put("mecodi",
		                                 datiMedico.get(ia.FLD_OBJ_ANAG_CODICE_MEDICO));
		                if (nome!=null && cognome!=null &&
		                 !nome.trim().equals("") && !cognome.trim().equals("")) {
		                        datiMedico.put( "menome", nome);
		                        datiMedico.put( "mecogn", cognome);
		                }else{
		                        Hashtable nuovoMedico = null;
		                        try{
		                            nuovoMedico = (Hashtable) ia.getMedico(
		                               datiMedico.get("mecodi") );
		                        }catch(Exception xc){
		                            xc.printStackTrace();
		                          }
		                                System.out.println("da "+datiMedico.get("mecodi")
		+"nuovoMedic : "+nuovoMedico);
		                                if (nuovoMedico.get( ia.FLD_ST_MEDICO_CODICE )
		                                        !=null)
		                                 datiMedico.put( "mecodi",
		                                   nuovoMedico.get( ia.FLD_ST_MEDICO_CODICE ));
		                                if (nuovoMedico.get( ia.FLD_ST_MEDICO_COGNOME )
		                                        !=null)
		                                        datiMedico.put( "mecogn",
		                                        nuovoMedico.get(
		                                                 ia.FLD_ST_MEDICO_COGNOME ));
		                                else
		                                        System.out.println("WARNING: campo '"+
		                                                ia.FLD_ST_MEDICO_COGNOME+
		                                                "' non reperito!");
		                                if (nuovoMedico.get( ia.FLD_ST_MEDICO_NOME )
		                                        !=null)
		                                        datiMedico.put( "menome",
		                                        nuovoMedico.get(
		                                         ia.FLD_ST_MEDICO_NOME ));
		                                else
		                                        System.out.println("WARNING: campo '"+
		                                                ia.FLD_ST_MEDICO_NOME+
		                                                "' non reperito!");
		                        }
		                }
		*/
	}

	//Fine Ivan

	public ISASRecord queryKeyOLD(myLogin mylogin, Hashtable h) throws SQLException, CariException {
		boolean done = false;
		ISASConnection dbc = null;
		String mex = "";
		String codice = "";
		ISASRecord dbr = null;
		try {
			dbc = super.logIn(mylogin);
			if (h.get("n_cartella") instanceof String)
				codice = (String) h.get("n_cartella");
			else if (h.get("n_cartella") instanceof Integer)
				codice = "" + (Integer) h.get("n_cartella");
			String myselect = "Select * from cartella where " + "n_cartella=" + codice;
			dbr = dbc.readRecord(myselect);
			String w_codice = "";
			String w_descr = "";
			String w_select = "";
			ISASRecord w_dbr = null;
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				//System.out.println("La Hash complessiva e': "+h1.toString());
				//comune nascita
				try {
					if (h1.get("cod_com_nasc") != null && !((String) h1.get("cod_com_nasc")).equals("")) {
						w_codice = (String) h1.get("cod_com_nasc");
						w_select = "SELECT * FROM comuni WHERE codice='" + w_codice + "'";
						w_dbr = dbc.readRecord(w_select);
						dbr.put("desc_com_nasc", w_dbr.get("descrizione"));
						dbr.put("xcf", w_dbr.get("cod_fis"));
					} else {
						dbr.put("desc_com_nasc", "");
						dbr.put("xcf", "");
					}
				} catch (Exception e) {
					dbr.put("cod_com_nasc", "");
					mex += "Comune nascita: " + w_codice + " � stato cancellato\n";
				}
				//operatore
				try {
					if (h1.get("cod_operatore") != null && !((String) h1.get("cod_operatore")).equals("")) {
						w_codice = (String) h1.get("cod_operatore");
						w_select = "SELECT * FROM operatori WHERE codice='" + w_codice + "'";
						w_dbr = dbc.readRecord(w_select);
						dbr.put("desc_operat", w_dbr.get("cognome") + " " + w_dbr.get("nome"));
					} else
						dbr.put("desc_operat", "");
				} catch (Exception e) {
					dbr.put("cod_operatore", "");
					mex += "Codice operatore: " + w_codice + " � stato cancellato\n";
				}
				//cittadinanza
				try {
					if (h1.get("cittadinanza") != null && !((String) h1.get("cittadinanza")).equals("")) {
						w_codice = (String) h1.get("cittadinanza");
						w_select = "SELECT * FROM cittadin WHERE cd_cittadin= '" + w_codice + "'";
						w_dbr = dbc.readRecord(w_select);
						dbr.put("des_cittadin", w_dbr.get("des_cittadin"));
					} else
						dbr.put("des_cittadin", "");
				} catch (Exception e) {
					dbr.put("cittadinanza", "");
					mex += "Codice cittadinanza: " + w_codice + " � stato concellato\n";
				}

				// 14/11/07: nazionalita
				try {
					if (h1.get("nazionalita") != null && !((String) h1.get("nazionalita")).equals("")) {
						w_codice = (String) h1.get("nazionalita");
						w_select = "SELECT * FROM cittadin WHERE cd_cittadin= '" + w_codice + "'";
						w_dbr = dbc.readRecord(w_select);
						if (w_dbr.get("des_cittadin") != null)
							dbr.put("des_nazionalita", w_dbr.get("des_cittadin"));
						else
							dbr.put("des_nazionalita", "");
					} else
						dbr.put("des_nazionalita", "");
				} catch (Exception e) {
					dbr.put("nazionalita", "");
					mex += "Codice nazionalita: " + w_codice + " � stato concellato\n";
				}

				//distretto 21/09/04 bargi
				/*  if (h1.get("cod_distretto")!=null && !((Integer)h1.get("cod_distretto")).equals(""))
				  {
				      Integer w_codice_i  = (Integer)h1.get("cod_distretto");
				      w_select = "SELECT * FROM distretti WHERE cod_distr="+w_codice_i.intValue();
				      w_dbr=dbc.readRecord(w_select);
				      dbr.put("des_distr", w_dbr.get("des_distr"));
				  }else dbr.put("des_distr", "");*/
				//caricamento griglia
				//Il codice usl deve essere un VARCHAR ***02/02/04
				if (h1.get("cod_usl") != null && !(h1.get("cod_usl")).equals("")) {
					String selg = "Select es_data_inizio,es_data_fine,cod_esenzione FROM anagra_esenzioni WHERE " + "cod_usl='"
							+ h1.get("cod_usl") + "'";
					//System.out.println("***QUERY CARICAMENTO GRIGLIA->"+selg);
					ISASCursor dbgriglia = dbc.startCursor(selg);
					Vector vdbg = dbgriglia.getAllRecord();
					for (Enumeration senum = vdbg.elements(); senum.hasMoreElements();) {
						//System.out.println("***ENUMERATION->");
						ISASRecord dbrdes = (ISASRecord) senum.nextElement();
						//System.out.println("***ESENZIONE->"+dbrdes.get("cod_esenzione"));
						if (dbrdes.get("cod_esenzione") != null && !(dbrdes.get("cod_esenzione")).equals("")) {
							String seldes = "SELECT descrizione FROM esenzioni WHERE" + " cod_esenzione='" + dbrdes.get("cod_esenzione")
									+ "'";
							//System.out.println("***QUERY DESCRIZIONE->"+seldes);
							ISASRecord des = dbc.readRecord(seldes);
							// 21/03/11 m.						  dbrdes.put("descrizione", des.get("descrizione"));
							// 21/03/11 m. ---
							if ((des != null) && (des.get("descrizione") != null))
								dbrdes.put("descrizione", des.get("descrizione"));
							else
								dbrdes.put("descrizione", "");
							// 21/03/11 m. ---
						} else
							dbrdes.put("descrizione", "");
					}
					dbr.put("griglia", vdbg);
				}//Hashtable ausi=dbr.getHashtable();
			}
			if (!mex.equals(""))
				throw new CariException(mex);

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (CariException ce) {
			ce.setISASRecord(dbr);
			throw ce;
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
			System.out.println("nella finally");
		}
	}

	/**CJ 20/04/2007 Aggiunto il controllo anche su il codice fiscale, oltre al comune nascita, cognome,
	 * nome e data nascita perch� a Massa si sono presentati casi di omonimia e non riuscivano ad inserire
	 * il secondo nominativo
	 */
	public ISASRecord query_controlloDaFassi(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		System.out.println("Hash:" + h.toString());
		String datavar = (String) h.get("data_nasc");
		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select * from cartella where (";
			String scr = (String) (h.get("cognome"));
			scr = duplicateChar(scr, "'");
			myselect = myselect + " cognome='" + scr + "'";
			String scr1 = (String) (h.get("nome"));
			scr1 = duplicateChar(scr1, "'");
			myselect = myselect + " AND nome='" + scr1 + "'";
			String scr2 = formatDate(dbc, datavar);
			myselect = myselect + " AND data_nasc=" + scr2;
			String scr3 = (String) (h.get("cod_com_nasc"));
			myselect = myselect + " AND cod_com_nasc='" + scr3 + "'";
			/*20/04/07*/
			String scr4 = (String) (h.get("cod_fisc"));
			myselect = myselect + " AND cod_fisc='" + scr4 + "')";

			//CONTROLLO SE ESITE LA CARTELLA TRAMITE IL CODICE REGIONALE
			//13/10/2005 aggiunto il controllo che il codice regionale sia diverso
			//da tutti zero
			System.out.println("cod_Reg:" + h.get("cod_reg") + "]");

			if (h.get("cod_reg") != null && !(((String) h.get("cod_reg")).equals(""))) {
				if (!(((String) h.get("cod_reg")).startsWith("0000")) && !(((String) h.get("cod_reg")).equals("0"))) {
					myselect = myselect + " OR (cod_reg='" + h.get("cod_reg") + "')";
				}
			}

			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				System.out.println("Cartella=>ControlloDaFassi: Vado a chiamare la queryKey");
				dbr = queryKeyOLD(mylogin, dbr.getHashtable());
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controllo()  ");
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

	public ISASRecord query_decodifica(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select cartella.*,anagra_c.citta from cartella,anagra_c where " + "cartella.n_cartella="
					+ (String) h.get("n_cartella") + " AND " + "cartella.n_cartella=anagra_c.n_cartella";
			ISASRecord dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
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

	/**CJ 20/04/2007 Aggiunto il controllo anche su il codice fiscale, oltre al comune nascita, cognome,
	 * nome e data nascita perch� a Massa si sono presentati casi di omonimia e non riuscivano ad inserire
	 * il secondo nominativo
	 */
	public ISASRecord query_controllo(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		System.out.println("Hash:" + h.toString());
		String datavar = (String) h.get("data_nasc");
		try {
			dbc = super.logIn(mylogin);
			String myselect = "Select * from cartella where (";
			String scr = (String) (h.get("cognome"));
			scr = duplicateChar(scr, "'");
			myselect = myselect + " cognome='" + scr + "'";
			String scr1 = (String) (h.get("nome"));
			scr1 = duplicateChar(scr1, "'");
			myselect = myselect + " AND nome='" + scr1 + "'";
			String scr2 = formatDate(dbc, datavar);
			myselect = myselect + " AND data_nasc=" + scr2;
			String scr3 = (String) (h.get("cod_com_nasc"));
			myselect = myselect + " AND cod_com_nasc='" + scr3 + "'";
			String scr4 = (String) (h.get("cod_fisc"));
			myselect = myselect + " AND cod_fisc='" + scr4 + "')";

			//CONTROLLO SE ESITE LA CARTELLA TRAMITE IL CODICE REGIONALE
			//13/10/2005 aggiunto il controllo che il codice regionale sia diverso
			//da tutti zero
			System.out.println("cod_Reg:" + h.get("cod_reg") + "]");

			if (h.get("cod_reg") != null && !(((String) h.get("cod_reg")).equals(""))) {
				System.out.println("***1");
				if (!(((String) h.get("cod_reg")).startsWith("0000")) && !(((String) h.get("cod_reg")).equals("0"))) {
					System.out.println("***2");
					myselect = myselect + " OR (cod_reg='" + h.get("cod_reg") + "')";
				}
			}
			ISASRecord dbr = dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controllo()  ");
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
		boolean done = false;
		String scr = " ";
		String scr1 = " ";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			boolean dato_presente = false;
			String myselect = " Select * from cartella ";

			//controllo valore corretto cognome
			scr = (String) (h.get("cognome"));
			if (!(scr == null))
				if (!(scr.equals(" "))) {
					scr = duplicateChar(scr, "'");
					myselect = myselect + " where cognome like '" + scr + "%'";
				}
			//Controllo esistenza nome
			scr1 = (String) (h.get("nome"));
			if (!(scr1 == null))
				if (!(scr1.equals(" ")))
					myselect = myselect + " and nome like '" + scr1 + "%'";

			myselect = myselect + " ORDER BY cod_operatore, cognome, nome,data_apertura, data_chiusura";
			//System.out.println("Query su cartella: "+myselect);
			ISASCursor dbcur = dbc.startCursor(myselect.toString());
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
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

	public Vector query_esenzioni(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		String scr = " ";
		Vector vdbg = null;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			boolean dato_presente = false;
			scr = (String) h.get("cod_usl");
			//Il codice usl deve essere un VARCHAR ***02/02/04
			if (scr != null && !(scr.equals(""))) {
				String selg = "Select es_data_inizio,es_data_fine,cod_esenzione FROM anagra_esenzioni WHERE " + "cod_usl='" + scr + "'";
				System.out.println("***QUERY CARICAMENTO GRIGLIA ESENZIONI->" + selg);
				ISASCursor dbgriglia = dbc.startCursor(selg);
				vdbg = dbgriglia.getAllRecord();
				for (Enumeration senum = vdbg.elements(); senum.hasMoreElements();) {
					System.out.println("***ENUMERATION->");
					ISASRecord dbrdes = (ISASRecord) senum.nextElement();
					System.out.println("***ESENZIONE->" + dbrdes.get("cod_esenzione"));
					if (dbrdes.get("cod_esenzione") != null && !(dbrdes.get("cod_esenzione")).equals("")) {
						String seldes = "SELECT descrizione FROM esenzioni WHERE" + " cod_esenzione='" + dbrdes.get("cod_esenzione") + "'";
						System.out.println("***QUERY DESCRIZIONE->" + seldes);
						ISASRecord des = dbc.readRecord(seldes);
						dbrdes.put("descrizione", des.get("descrizione"));
					} else
						dbrdes.put("descrizione", "");
				}
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbg;
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

	/*
	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
	        String scr=" ";
		String scr1=" ";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			boolean dato_presente = false;
			String myselect= " Select * from cartella ";

	                //controllo valore corretto cognome

	                scr=(String)(h.get("cognome"));
		        if (!(scr==null))
	                  if (!(scr.equals(" ")))
	                   {
			    scr=duplicateChar(scr,"'");
	                    myselect=myselect+" where cognome like '"+scr+"%'";
	                   }

	                //Controllo esistenza nome
	                scr1=(String)(h.get("nome"));
	                 if (!(scr1==null))
	                  if (!(scr1.equals(" ")))
	                   myselect=myselect+" and nome like '"+scr1+"%'";

	                myselect=myselect+" ORDER BY cod_operatore, cognome, nome,data_apertura, data_chiusura";
			System.out.println("Query su cartella: "+myselect);
			ISASCursor dbcur=dbc.startCursor(myselect,200);
			//Vector vdbr=dbcur.getAllRecord();
	                int start = Integer.parseInt((String)h.get("start"));
	                int stop = Integer.parseInt((String)h.get("stop"));
	                Vector vdbr = dbcur.paginate(start, stop);
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ");
	   	}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}*/

	//CJ 13/12/2006 presi i dati anche da anagra_c
	public Vector queryPaginate_old(myLogin mylogin, Hashtable h) throws SQLException {
		//   it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
		boolean done = false;
		String scr = " ";
		String scr1 = " ";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			boolean dato_presente = false;
			String myselect = " Select c.*, a.citta, a.indirizzo" + " FROM cartella c,anagra_c a" + " WHERE c.n_cartella=a.n_cartella AND "
					+ " a.data_variazione IN (SELECT MAX(an.data_variazione)" + " FROM anagra_c an WHERE an.n_cartella=c.n_cartella)";

			//controllo valore corretto cognome

			scr = (String) (h.get("cognome"));
			if (!(scr == null))
				if (!(scr.equals(" "))) {
					scr = duplicateChar(scr, "'");
					myselect = myselect + " and cognome like '" + scr + "%'";
				}

			//Controllo esistenza nome
			scr1 = (String) (h.get("nome"));
			if (!(scr1 == null))
				if (!(scr1.equals(" ")))
					myselect = myselect + " and nome like '" + scr1 + "%'";

			myselect = myselect + " ORDER BY c.cod_operatore, cognome, nome,data_apertura, data_chiusura";
			System.out.println("Query su cartella: " + myselect);
			ISASCursor dbcur = dbc.startCursor(myselect, 200);
			//Vector vdbr=dbcur.getAllRecord();
			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			if ((vdbr != null) && (vdbr.size() > 0))
				for (int i = 0; i < vdbr.size() - 1; i++) {
					ISASRecord dbr = (ISASRecord) vdbr.elementAt(i);
					dbr.put("desc_nasc", util.getDecode(dbc, "comuni", "codice", (String) util.getObjectField(dbr, "cod_com_nasc", 'S'),
							"descrizione"));
					dbr.put("desc_res", util.getDecode(dbc, "comuni", "codice", (String) util.getObjectField(dbr, "citta", 'S'),
							"descrizione"));
				}
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
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

	public ISASRecord insertOLd(myLogin mylogin, Hashtable h, Vector hv) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		String punto = MIONOME + "insert ";
		stampa(punto + "\n dati che ricevo >");
		boolean done = false;
		String codice = null;
		//Jessy 14-01-2005
		String data = "";
		ISASConnection dbc = null;
		//System.out.println("in insert hashtable h="+h.toString());
		//System.out.println("in insert vettore hv="+hv.toString());
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			int numero_cartelle = selectProgressivo(dbc, "CARTELLA");

			/*                // Inserisce un nuovo record in tabella
					// util_contatti  per la cartella inserita
					ISASRecord dbr_ut_cn=dbc.newRecord("util_contatto");
					dbr_ut_cn.put("num_cartella", new Integer(numero_cartelle));
					int numero_contatti = 0;
					dbr_ut_cn.put("num_contatto", new Integer(numero_contatti));
					dbc.writeRecord(dbr_ut_cn);
			*/

			ISASRecord dbr = dbc.newRecord("cartella");
			Enumeration n = h.keys();
			while (n.hasMoreElements()) {
				String e = (String) n.nextElement();
				dbr.put(e, h.get(e));
			}
			dbr.put("n_cartella", new Integer(numero_cartelle));
			//Inserisce nmax_contatti a zero in inserimento
			dbr.put("nmax_contatti", new Integer(0));
			dbc.writeRecord(dbr);
			String myselect = "Select * from cartella where " + "n_cartella=" + numero_cartelle;
			dbr = dbc.readRecord(myselect);
			//Jessy 14-01-05 tiro fuori la data per controllare che sia uguale
			//alla minima data presente su anagra_c
			data = "" + (java.sql.Date) dbr.get("data_apertura");

			//PER CAMPI DECODIFICA:
			String w_codice = "";
			String w_descr = "";
			String w_select = "";
			ISASRecord w_dbr = null;
			if (dbr != null) {
				Hashtable h1 = dbr.getHashtable();
				System.out.println("*****La Hash complessiva e: " + h1.toString());
				//comune nascita
				if (h1.get("cod_com_nasc") != null && !((String) h1.get("cod_com_nasc")).equals("")) {
					w_codice = (String) h1.get("cod_com_nasc");
					w_select = "SELECT * FROM comuni WHERE codice='" + w_codice + "'";
					w_dbr = dbc.readRecord(w_select);
					dbr.put("desc_com_nasc", w_dbr.get("descrizione"));
					dbr.put("xcf", w_dbr.get("cod_fis"));
				} else {
					dbr.put("desc_com_nasc", "");
					dbr.put("xcf", "");
				}
				//operatore
				dbr.put("desc_operat", decodifica("operatori", "codice", (String) h1.get("cod_operatore"), "descrizione", dbc));
				//cittadinanza
				dbr.put("des_cittadin", decodifica("cittadin", "cd_cittadin", (String) h1.get("cittadinanza"), "des_cittadin", dbc));
				// 14/11/07: nazionalita
				dbr.put("des_nazionalita", decodifica("cittadin", "cd_cittadin", (String) h1.get("nazionalita"), "des_cittadin", dbc));
				//distretto 21/09/04 bargi
				// dbr.put("des_distr",decodifica("distretti","cod_distr",(Integer)h1.get("cod_distretto"),"des_distr",dbc));
			}
			//FINE
			//******* ANAGRA
			//***INSERIMENTO di anagra_c

			//System.out.println("vettore :"+hv.toString());
			Enumeration enum2 = hv.elements();
			Hashtable htv = (Hashtable) enum2.nextElement();
			dbr.put("desc_area_res", decodifica("areadis", "codice", (String) htv.get("areadis"), "descrizione", dbc));
			dbr.put("desc_area_dom", decodifica("areadis", "codice", (String) htv.get("dom_areadis"), "descrizione", dbc));
			dbr.put("comresdescr", decodifica("comuni", "codice", (String) htv.get("citta"), "descrizione", dbc));
			dbr.put("comdomdescr", decodifica("comuni", "codice", (String) htv.get("dom_citta"), "descrizione", dbc));
			dbr.put("meddescr", decodifica("medici", "mecodi", (String) htv.get("cod_med"), "mecogn", dbc));
			// 15/11/07
			dbr.put("comreperibdescr", decodifica("comuni", "codice", (String) htv.get("comune_rep"), "descrizione", dbc));
			dbr.put("desc_area_reperib", decodifica("areadis", "codice", (String) htv.get("areadis_rep"), "descrizione", dbc));

			String selan = "SELECT * FROM anagra_c WHERE n_cartella=" + numero_cartelle + " and data_variazione="
					+ formatDate(dbc, (String) htv.get("data_variazione"));
			ISASRecord dbrAn = dbc.readRecord(selan);
			//ISASRecord dbrAn=dbc.newRecord("anagra_c");

			if (dbrAn != null) //aggiorno
			{
				System.out.println("aggiorno");
			} else //inserisco
			{
				dbrAn = dbc.newRecord("anagra_c");

			}
			dbrAn.put("n_cartella", "" + numero_cartelle);
			Enumeration n2 = htv.keys();
			while (n2.hasMoreElements()) {
				String elem2 = (String) n2.nextElement();
				dbrAn.put(elem2, htv.get(elem2));
			}
			dbc.writeRecord(dbrAn);
			//Jessy 14-01-2005
			String data_var = ControlloDataMin("" + new Integer(numero_cartelle), data, dbc);
			dbr.put("data_variazione", data_var);
			//Fine Jessy
			String myselect3 = "SELECT * FROM anagra_c WHERE n_cartella=" + numero_cartelle + " and data_variazione="
					+ formatDate(dbc, (String) htv.get("data_variazione"));
			//                System.out.println("prima di readRecord "+myselect);

			dbrAn = dbc.readRecord(myselect3);
			//System.out.println("*****L'isas che ritorna: "+dbr.getHashtable().toString());

			//********* ANAGRA
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new SQLException("Errore eseguendo una rollback() - " + e1);
			}
			throw new SQLException("Errore eseguendo una insert() - " + e);
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

	//Jessy 14-01-2005
	private String ControlloDataMin(String cartella, String data, ISASConnection dbc) throws SQLException {
		try {
			//System.out.println("Dentro controllodataMin");
			String mysel = "SELECT MIN(data_variazione) data_min FROM anagra_c WHERE " + "n_cartella =" + cartella;
			ISASRecord dbr = dbc.readRecord(mysel);
			String data_min = "" + (java.sql.Date) dbr.get("data_min");
			//System.out.println("Data minima: "+data_min);
			//System.out.println("Data che arriva: "+data);
			data_min = data_min.substring(8, 10) + data_min.substring(5, 7) + data_min.substring(0, 4);
			DataWI dataINIZIO = new DataWI(data_min);
			String data1 = data.substring(0, 4) + data.substring(5, 7) + data.substring(8, 10);
			int rit = dataINIZIO.confrontaConDt(data1);
			System.out.println("Sono uguali?" + rit);
			if (rit != 0) {
				String sel = "SELECT * FROM anagra_c WHERE " + "n_cartella=" + cartella + " AND data_variazione="
						+ formatDate(dbc, "" + (java.sql.Date) dbr.get("data_min"));
				String sel1 = "SELECT * FROM anagra_c WHERE " + "n_cartella=" + cartella + " AND data_variazione=" + formatDate(dbc, data);
				ISASRecord dbrData = dbc.readRecord(sel1);
				if (dbrData == null) {
					//System.out.println("Select=>"+sel);
					ISASRecord dbrAn = dbc.readRecord(sel);
					ISASRecord dbr2 = dbc.newRecord("anagra_c");
					Enumeration n = dbrAn.getHashtable().keys();
					while (n.hasMoreElements()) {
						String e = (String) n.nextElement();
						dbr2.put(e, dbrAn.get(e));
					}
					dbc.deleteRecord(dbrAn);
					dbr2.put("data_variazione", data);
					dbc.writeRecord(dbr2);
				}
			}
			return data;
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una ControlloDataMin()  ");
		}
	}

	public ISASRecord update_old(myLogin mylogin, ISASRecord dbr, Vector hv) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
		boolean done = false;
		String codice = null;
		//Jessy 14-01-2005
		String data = "";
		ISASConnection dbc = null;
		//System.out.println("in update cartella hashtable="+dbr.getHashtable().toString());

		// 28/04/10 --		
		System.out.println("************ CartellaEJB: mylogin: user=[" + mylogin.getUser() + "] - pwd=[" + mylogin.getPassword()
				+ "] ****************");
		// 28/04/10 --
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();

			//elisa b 17/04/12: gestione data scelta medico
			boolean infoMedicoMod = false;
			if ((dbr.get("info_medico_mod") != null) && (dbr.get("info_medico_mod").equals("S")))
				infoMedicoMod = true;

			ISASRecord dbrEx = ControlloEsistenza(dbc, dbr);
			if (dbrEx != null) {
				dbrEx.put("inserito", "S");
				dbr = dbrEx;
			} else {
				int max = 0;
				String mysel = "Select * from cartella where " + "n_cartella=" + (String) dbr.get("n_cartella");
				ISASRecord dbrmax = dbc.readRecord(mysel);
				if (dbrmax.get("nmax_contatti") != null) {
					String contatti = ((Integer) dbrmax.get("nmax_contatti")).toString();
					max = (new Integer(contatti)).intValue();
				} else
					max = 0;
				dbr.put("nmax_contatti", (new Integer(max)).toString());
				dbc.writeRecord(dbr);
				String data_chiusura = "";
				if (dbr.get("data_chiusura") != null && !((dbr.get("data_chiusura")).toString()).equals("1000-01-01"))
					data_chiusura = (String) dbr.get("data_chiusura").toString();
				String cartella = (String) dbr.get("n_cartella");
				String motivo_chiusura = (String) dbr.get("motivo_chiusura");
				;
				if (data_chiusura != null && !(data_chiusura.equals(""))) {
					//SE LA DATA CHIUSURA E' PRESENTE VADO A INSERIRLA IN TUTTI I CONATTI E SU CONTSAN
					//gb 24/09/07                AggiornaData("contatti",cartella,"data_chiusura",data_chiusura,"data_contatto",dbc);
					CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure(); //gb 25/09/07

					//gb 28/09/07: Controlli data chiusura cartella con:
					// date prestazioni erogate della tabella interv.
					// date aper. e date chius. della cheda valutazione.
					// date aper. e date chius. dei progetti e contatti.
					// date aper. e date chius. dei piani assitenziali.
					// date aper. dei piani accessi.
					// date aper. e date chius. degli obiettivi, interventi e verifiche.
					String strMsgCheckDtCh = clCcec.checkDtChDaCartGTDtApeDtCh(dbc, cartella, data_chiusura, dbr.getHashtable().containsKey(Costanti.FORZA_CHIUSURA));
					if (!strMsgCheckDtCh.equals(""))
						throw new CariException(strMsgCheckDtCh, -2);
				}

				//System.out.println("Ho scritto il record!");
				codice = (String) dbr.get("n_cartella");
				String myselect = "Select * from cartella where " + "n_cartella=" + codice;
				dbr = dbc.readRecord(myselect);
				//Jessy 14-01-2005
				data = "" + (java.sql.Date) dbr.get("data_apertura");

				//PER CAMPI DECODIFICA:
				String w_codice = "";
				String w_descr = "";
				String w_select = "";
				ISASRecord w_dbr = null;
				if (dbr != null) {
					Hashtable h1 = dbr.getHashtable();
					if (h1.get("cod_com_nasc") != null && !((String) h1.get("cod_com_nasc")).equals("")) {
						w_codice = (String) h1.get("cod_com_nasc");
						w_select = "SELECT * FROM comuni WHERE codice='" + w_codice + "'";
						w_dbr = dbc.readRecord(w_select);
						dbr.put("desc_com_nasc", w_dbr.get("descrizione"));
						dbr.put("xcf", w_dbr.get("cod_fis"));
					} else {
						dbr.put("desc_com_nasc", "");
						dbr.put("xcf", "");
					}
					dbr.put("desc_operat", decodifica("operatori", "codice", (String) h1.get("cod_operatore"), "descrizione", dbc));
					dbr.put("des_cittadin", decodifica("cittadin", "cd_cittadin", (String) h1.get("cittadinanza"), "des_cittadin", dbc));
					// 14/11/07: nazionalita
					dbr.put("des_nazionalita", decodifica("cittadin", "cd_cittadin", (String) h1.get("nazionalita"), "des_cittadin", dbc));
					//distretto 21/09/04 bargi
					//dbr.put("des_distr",decodifica("distretti","cod_distr",(Integer)h1.get("cod_distretto"),"des_distr",dbc));
				}
				Enumeration enum2 = hv.elements();
				Hashtable htv = (Hashtable) enum2.nextElement();
				dbr.put("desc_area_res", decodifica("areadis", "codice", (String) htv.get("areadis"), "descrizione", dbc));
				dbr.put("desc_area_dom", decodifica("areadis", "codice", (String) htv.get("dom_areadis"), "descrizione", dbc));
				dbr.put("comresdescr", decodifica("comuni", "codice", (String) htv.get("citta"), "descrizione", dbc));
				dbr.put("comdomdescr", decodifica("comuni", "codice", (String) htv.get("dom_citta"), "descrizione", dbc));
				dbr.put("meddescr", decodifica("medici", "mecodi", (String) htv.get("cod_med"), "mecogn", dbc));
				// 15/11/07
				dbr.put("comreperibdescr", decodifica("comuni", "codice", (String) htv.get("comune_rep"), "descrizione", dbc));
				dbr.put("desc_area_reperib", decodifica("areadis", "codice", (String) htv.get("areadis_rep"), "descrizione", dbc));

				/* elisa b 17/04/12 : se dal client arriva l'informazione della
				 * modifica del medico e/o della data in cui il medico e'
				 * scelto, si deve aggiornare o inserire un record che ha per
				 * data variazione la data di scelta del medico ed, eventualmente,
				 * aggiornare il valore del medico e della data in record
				 * successivi.*/
				if (infoMedicoMod) {
					gestioneMedico(dbc, htv, codice, (String) dbr.get("cod_operatore"));
				}

				String selan = "SELECT * FROM anagra_c WHERE n_cartella=" + codice + " and data_variazione="
						+ formatDate(dbc, (String) htv.get("data_variazione"));

				ISASRecord dbrAn = dbc.readRecord(selan);
				if (dbrAn != null) //aggiorno
					System.out.println("aggiorno");
				else { //inserisco
					System.out.println("Nuovo record");
					dbrAn = dbc.newRecord("anagra_c");
				}
				dbrAn.put("n_cartella", codice);
				Enumeration n2 = htv.keys();
				while (n2.hasMoreElements()) {
					String elem2 = (String) n2.nextElement();

					dbrAn.put(elem2, htv.get(elem2));
				}
				dbc.writeRecord(dbrAn);
				//Jessy 14-01-2005
				String data_var = ControlloDataMin(codice, data, dbc);
				//dbr.put("data_variazione",data_var);
				System.out.println("Rientro in update!!!");
				//Fine Jessy
				//Jessy 12-01-2005 deve essere sempre mostrato l'ultimo record su anagra_c
				String myselect3 = "SELECT a.* FROM anagra_c a WHERE a.n_cartella=" + codice +
				//Jessy 14-01-2005" and data_variazione="+formatDate(dbc,(String)htv.get("data_variazione"));
						" and a.data_variazione IN (SELECT MAX (data_variazione) FROM anagra_c  " + "WHERE n_cartella=a.n_cartella)";
				//  System.out.println("leggo max anagra_c="+myselect3);
				dbrAn = dbc.readRecord(myselect3);
				//  System.out.println("prima di put data variazione "+dbrAn);
				dbr.put("data_variazione", dbrAn.get("data_variazione"));
				//  System.out.println("dopo..");
			}
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			//System.out.println("DBR in Update=>"+dbr.getHashtable().toString());
			return dbr;
		}
		//gb 24/09/07 **************
		catch (CariException ce) {
			ce.setISASRecord(null);
			try {
				dbc.rollbackTransaction();
			} catch (Exception p) {
				throw new SQLException("DEBUG CARTELLAEJB:Errore eseguendo una rollback()" + p);
			}
			throw ce;
		}
		//gb 24/09/07: fine **************
		catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception p) {
				throw new SQLException("DEBUG CARTELLAEJB:Errore eseguendo una rollback()" + p);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception t) {
				throw new SQLException("DEBUG CARTELLAEJB:Errore eseguendo una rollback()" + t);
			}
			throw e;
		} catch (Exception e1) {
			e1.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception k) {
				throw new SQLException("DEBUG CARTELLAEJB:Errore eseguendo una rollback()" + k);
			}
			throw new SQLException("Errore eseguendo una update() - " + e1);
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

	/*gb 26/09/07 *******
	//gb 24/09/07 *****************************************************************
	private void chiudoProgettiAssSociale(String cartella, String data_chiusura, String motivo_chiusura, String strCodOperatore, ISASConnection dbc) throws Exception
	   {
	   ISASCursor dbcur = null;
	   String msg = "";

	   if (data_chiusura!=null && !(data_chiusura.equals("")))
	    {
	    String mySel = "SELECT *" +
	                 " FROM ass_progetto" +
	                 " WHERE n_cartella = " + cartella +
			 " AND ap_data_chiusura IS NULL";
	    System.out.println("CartellaEJB / chiudiProgettoAssSociale / mySel: " + mySel);
	    dbcur = dbc.startCursor(mySel);

	    Vector vdbr=dbcur.getAllRecord();
	    for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
		{
		ISASRecord dbr=(ISASRecord)senum.nextElement();
		String sel = "SELECT *" +
				" FROM ass_progetto" +
				" WHERE n_cartella = " + cartella +
				" AND n_progetto = " + (Integer)dbr.get("n_progetto");
		System.out.println("CartellaEJB / chiudiProgettoAssSociale / sel: " + sel);
		ISASRecord dbrDett=dbc.readRecord(sel);
		dbrDett.put("ap_data_chiusura", data_chiusura);
		dbrDett.put("motivo_chiusura", motivo_chiusura);
		dbrDett.put("ap_oper_ch", strCodOperatore);
		dbc.writeRecord(dbrDett);
		}//fine for
	    }
	  if (dbcur != null)
	    dbcur.close();
	  }
	// ****************************************************************************

	//gb 24/09/07 *****************************************************************
	private void chiudoPianiAssitenz(String cartella, String data_chiusura, ISASConnection dbc) throws Exception
	   {
	    ISASCursor dbcur = null;
	    String mySel = "SELECT *" +
	                 " FROM piano_assist" +
	                 " WHERE n_cartella = " + cartella +
			 " AND pa_data_chiusura IS NULL ";
	    System.out.println("CartellaEJB / chiudoPianiAssitenz / mySel: " + mySel);
	    dbcur = dbc.startCursor(mySel);
	    Vector vdbr=dbcur.getAllRecord();
	    for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
		{
		ISASRecord dbr=(ISASRecord)senum.nextElement();
		String strDataApertura=((java.sql.Date)dbr.get("pa_data")).toString();
		String sel = "SELECT *" +
				" FROM piano_assist" +
				" WHERE n_cartella = " + cartella +
				" AND n_progetto = " + (Integer)dbr.get("n_progetto") +
				" AND cod_obbiettivo = '" + (String)dbr.get("cod_obbiettivo") + "'" +
				" AND n_intervento = " + (Integer)dbr.get("n_intervento") +
				" AND pa_tipo_oper = '" + (String)dbr.get("pa_tipo_oper") + "'" +
				" AND pa_data = " + formatDate(dbc, strDataApertura);
		System.out.println("CartellaEJB / chiudoPianiAssitenz / sel: " + sel);
		ISASRecord dbrDett=dbc.readRecord(sel);
		dbrDett.put("pa_data_chiusura", data_chiusura);
		dbc.writeRecord(dbrDett);
		}//fine for
	   if (dbcur != null)
	     dbcur.close();
	   }
	// ****************************************************************************

	//gb 24/09/07 *****************************************************************
	private void AggiornaDataPianointerv(String cartella, String data_chiusura, ISASConnection dbc)
		throws  SQLException
	   {
	        try {
			String mysel = "SELECT *" +
					" FROM piano_accessi" +
					" WHERE n_cartella = " + cartella +
			    		" AND ( pi_data_fine is null OR pi_data_fine > " + formatDate(dbc,data_chiusura)+")";
			//se pi_data_fine � valorizzata ma data > della data chiusura questa viene anticipata
			ISASCursor dbcur=dbc.startCursor(mysel);
			while (dbcur.next())
			  {
	                  ISASRecord dbr=dbcur.getRecord();
	                  String strDataApertura = ((java.sql.Date)dbr.get("pa_data")).toString();
	                  String sel = "SELECT *" +
					" FROM piano_accessi" +
					" WHERE n_cartella = " + cartella +
					" AND n_progetto = " + (Integer)dbr.get("n_progetto") +
					" AND cod_obbiettivo = '" + (String)dbr.get("cod_obbiettivo") + "'" +
					" AND n_intervento = " + (Integer)dbr.get("n_intervento") +
					" AND pa_data = " + formatDate(dbc, strDataApertura) +
					" AND pi_prog = " + (Integer)dbr.get("pi_prog") + 
					" AND pa_tipo_oper= '" + (String)dbr.get("pa_tipo_oper") + "'";
			  System.out.println("CartellaEJB / AggiornaDataPianointerv / sel: " + sel);
	                  ISASRecord dbrDett=dbc.readRecord(sel);
	                  if(dbrDett!=null)
			    {
	                    dbrDett.put("pi_data_fine", data_chiusura);
			    dbc.writeRecord(dbrDett);
			    }
			  }//fine for
	   		if (dbcur != null)
			   dbcur.close();
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("CartellaEJB, Errore eseguendo una AggiornaDataPianointerv()  ");
		}
	   }
	// ****************************************************************************

	//gb 24/09/07 *****************************************************************
	private void rimuovoAgendaCaricata(String cartella,String data_chiusura,ISASConnection dbc)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	   {
	    boolean done=false;
	    ISASCursor dbcur=null;
	    try{
		String mysel="SELECT *" +
			" FROM agendant_interv, agendant_intpre"+
			" WHERE ag_data > " + formatDate(dbc, data_chiusura) +
			" AND ag_cartella = " + cartella +
			" AND ag_stato = 0" + //cancello solo appunt con stato a 0
			" AND ag_data = ap_data" +
			" AND ag_progr = ap_progr" +
			" AND ag_oper_ref = ap_oper_ref" +
			" ORDER BY ag_data";
		debugMessage("CartellaEJB/rimuovoAgendaCaricata mysel: " + mysel);
		dbcur=dbc.startCursor(mysel);
		while (dbcur.next())
	     	   {
		   ISASRecord dbr = dbcur.getRecord();
		   cancellaAppuntam(dbr, dbc);
		   }
		 if (dbcur != null) dbcur.close();
		 done=true;
		}catch(Exception e)
			{
			System.out.println("Errore in cancella agenda_intpre..."+e);
			throw new SQLException("Errore eseguendo rimuovoAgendaCaricata()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
	                 			dbcur.close();
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	   }
	// ****************************************************************************

	//gb 24/09/07 *****************************************************************
	private void cancellaAppuntam(ISASRecord dbrec,ISASConnection dbc)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	   {
	    try{

		String data = ((java.sql.Date)dbrec.get("ap_data")).toString();
		String selag =	"SELECT *" +
				" FROM agendant_intpre"+
				" WHERE ap_data = " + formatDate(dbc,data) +
				" AND ap_progr = " + dbrec.get("ap_progr") +
				" AND ap_oper_ref = '" + (String)dbrec.get("ap_oper_ref") + "'" +
				" AND ap_prest_cod = '" + (String)dbrec.get("ap_prest_cod") + "'";
		debugMessage("CartellaEJB/cancellaAppuntam selag(1): " + selag);
		ISASRecord dbag = dbc.readRecord(selag);
		if(dbag!=null)
		   {
		   dbc.deleteRecord(dbag);
		   dbag=null;
	          //devo controllare se sono rimasti record su agenda_intpre se non
	          //ce ne sono occorre cancellare anche il record su agenda_interv
	           selag="SELECT COUNT(*) tot" +
			" FROM agendant_intpre" +
			" WHERE ap_data = " + formatDate(dbc,data) +
			" AND ap_progr = " + dbrec.get("ap_progr") +
			" AND ap_oper_ref = '" + (String)dbrec.get("ap_oper_ref") + "'";
		   debugMessage("CartellaEJB/cancellaAppuntam selag(2): " + selag);
	           dbag=dbc.readRecord(selag);

		   int t=0;
		   if(dbag!=null) t=util.getIntField(dbag,"tot");
		   if(t==0)
			{
	             //cancello da agenda_interv
			selag = "SELECT *" +
				" FROM agendant_interv"+
				" WHERE ag_data = " + formatDate(dbc,data) +
				" AND ag_progr = " + dbrec.get("ag_progr") +
				" AND ag_oper_ref = '" + (String)dbrec.get("ag_oper_ref") + "'";
			debugMessage("CartellaEJB/cancellaAppuntam selag(3): " + selag);
			dbag=dbc.readRecord(selag);
			dbc.deleteRecord(dbag);
			}
		   }
		}catch(Exception e){
			System.out.println("CartellaEJB, Errore in cancellaAppuntam..."+e);
			throw new SQLException("CartellaEJB, Errore eseguendo cancellaAppuntam()  ");
		}
	   }
	// ****************************************************************************
	*gb 26/09/07: fine *******/

	/**CJ 20/04/2007 Aggiunto il controllo anche su il codice fiscale, oltre al comune nascita, cognome,
	 * nome e data nascita perch� a Massa si sono presentati casi di omonimia e non riuscivano ad inserire
	 * il secondo nominativo
	 */
	private ISASRecord ControlloEsistenza(ISASConnection dbc, ISASRecord dbr) throws SQLException {
		try {
			String datavar = (String) dbr.get("data_nasc");//).toString();
			//Jessica 04/03/04 controllo che non esista una cartella con gli stessi dati
			String selCart = "Select * from cartella where ((";
			String scr = (String) (dbr.get("cognome"));
			scr = duplicateChar(scr, "'");
			selCart = selCart + " cognome='" + scr + "'";
			String scr1 = (String) (dbr.get("nome"));
			scr1 = duplicateChar(scr1, "'");
			selCart = selCart + " AND nome='" + scr1 + "'";
			String scr2 = formatDate(dbc, datavar);
			selCart = selCart + " AND data_nasc=" + scr2;
			String scr3 = (String) (dbr.get("cod_com_nasc"));
			selCart = selCart + " AND cod_com_nasc='" + scr3 + "' ";
			String scr4 = (String) (dbr.get("cod_fisc"));
			selCart = selCart + " AND cod_fisc='" + scr4 + "') ";
			//CONTROLLO SE ESITE LA CARTELLA TRAMITE IL CODICE REGIONALE
			//13/10/2005 aggiunto il controllo che il codice regionale sia diverso
			//da tutti zero
			if (dbr.get("cod_reg") != null && !(((String) dbr.get("cod_reg")).equals(""))
					&& !(((String) dbr.get("cod_reg")).startsWith("0000")) && !(((String) dbr.get("cod_reg")).equals("0")))
				selCart = selCart + " OR (cod_reg='" + (String) dbr.get("cod_reg") + "'))";
			else
				selCart = selCart + ")";
			selCart = selCart + " AND n_cartella<>" + (String) dbr.get("n_cartella");
			System.out.println("Controllo esistenza****" + selCart);
			dbr = dbc.readRecord(selCart);
			return dbr;
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una ControlloEsistenza()  ");
		}
	}

	public void delete(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			//cancellazione dati anagrafici
			//System.out.println("prima di deleteAnagra");
			deleteAnagra(dbc, (Integer) dbr.get("n_cartella"));
			//System.out.println("dopo deleteAnagra");
			//cancellazione cartella
			String myselect = "SELECT * FROM cartella WHERE " + "n_cartella=" + (Integer) dbr.get("n_cartella");
			System.out.println("Select deleteCartella " + myselect);
			ISASRecord dbrc = dbc.readRecord(myselect);
			dbc.deleteRecord(dbrc);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e2) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e2);
			}
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			try {
				dbc.rollbackTransaction();
			} catch (Exception e3) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e3);
			}
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

	public void deleteAnagra(ISASConnection dbc, Integer n_cartella) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		try {
			System.out.println("In deleteAnagra");
			String myselect = "SELECT * FROM anagra_c WHERE " + "n_cartella=" + n_cartella;
			System.out.println("Select deleteAnagra " + myselect);

			ISASRecord dbr = dbc.readRecord(myselect);
			System.out.println(" dopo readRecord dbr:" + dbr.getHashtable().toString());
			if (dbr != null) {
				dbc.deleteRecord(dbr);
				System.out.println("dopo deleteRecord deleteAnagra");
			} else
				System.out.println("non esistono rec deleteAnagra");
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una deleteAnagra() - " + e1);
		}
	}

	// 21/12/07 m.: lettura dati di ANAGRA_C (al momento servono solo zona, distretto di residenza e medico ma si legge tutto)
	public Hashtable query_getDatiFromAnagraC(myLogin mylogin, Hashtable h0) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		// hashtable che ritorner� al client
		Hashtable h_ret = new Hashtable();

		String cart = (String) h0.get("n_cartella");
		try {
			dbc = super.logIn(mylogin);

			// 05/12/11 m. ---
			String dtRif = (String) h0.get("dt_rif");
			String crtiDtRif = "";
			if ((dtRif != null) && (!dtRif.trim().equals("")))
				crtiDtRif = " AND anagra_c.data_variazione <= " + formatDate(dbc, dtRif);
			// 05/12/11  m. ---

			String sel = "SELECT a.* FROM anagra_c a" + " WHERE a.n_cartella = " + cart
					+ " AND a.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" + " FROM anagra_c"
					+ " WHERE anagra_c.n_cartella = a.n_cartella" + crtiDtRif + ")";

			ISASRecord dbr1 = dbc.readRecord(sel);
			if (dbr1 != null) {
				// 19/02/08		String codDistr = (String)ISASUtil.getDecode(dbc, "comuni", "codice", (String)dbr1.get("citta"), "cod_distretto");
				String codDistr = (String) ISASUtil.getDecode(dbc, "areadis", "codice", (String) dbr1.get("areadis"), "cod_distretto"); // 19/02/08
				String codZona = (String) ISASUtil.getDecode(dbc, "distretti", "cod_distr", codDistr, "cod_zona");
				decodMedico(dbc, dbr1);

				h_ret = (Hashtable) dbr1.getHashtable();
				h_ret.put("distretto", codDistr);
				h_ret.put("zona", codZona);
				h_ret.put("regione", dbr1.get("regione").toString());//elisa b 18/03/11
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return h_ret;
		} catch (Exception e) {
			System.out.println("CartellaEJB.query_getDatiFromAnagraC(): " + e);
			throw new SQLException("Errore eseguendo una query()");
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

	private void decodMedico(ISASConnection mydbc, ISASRecord mydbr) throws Exception {
		String cod = (String) mydbr.get("cod_med");
		String cogn = "";
		String nome = "";

		if ((cod != null) && (!cod.trim().equals(""))) {
			String sel = "SELECT NVL(mecogn, '') cogn_med," + " NVL(menome, '') nome_med" + " FROM medici WHERE mecodi = '" + cod + "'";

			ISASRecord dbr2 = mydbc.readRecord(sel);
			if (dbr2 != null) {
				cogn = (String) dbr2.get("cogn_med");
				nome = (String) dbr2.get("nome_med");
			}
		}
		mydbr.put("cod_med", (cod != null ? cod : ""));
		mydbr.put("cogn_med", cogn);
		mydbr.put("nome_med", nome);
	}

	private int selectProgressivoOLD(ISASConnection dbc, String chiave_tabella) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {

		// Attenzione: prima di invocare il metodo assicurarsi che sia stata  invocata
		// una start Transaction sulla connessione!

		boolean done = false;
		String codice = null;
		int i = 10;
		int next_value = 2;
		int numass = 0;
		boolean aggiungi_assistito = false;
		try {
			//                dbc.startTransaction();
			String myselect = "Select * from chiavi_libere where " + "nome_chiave='" + chiave_tabella + "'";
			ISASCursorLock isascur = dbc.startCursorLock(myselect);

			if (!isascur.next()) {
				ISASRecord dbr_chiavilib = dbc.newRecord("chiavi_libere");
				dbr_chiavilib.put("nome_chiave", chiave_tabella);
				dbr_chiavilib.put("val_libero", new Integer(next_value));
				dbc.writeRecord(dbr_chiavilib);
			} else {
				ISASRecord isasr = isascur.getRecord();
				next_value = 1 + ((Integer) isasr.get("val_libero")).intValue();
				isasr.put("val_libero", new Integer(next_value));
				isascur.writeRecord(isasr);
				isascur.close();
			}
			numass = next_value - 1;
			//                System.out.println("DBG: progressivo : "+numass);
			//                dbc.commitTransaction();
			done = true;
			return numass;
		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " + e1);
			}
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - " + e);
			}
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una selectProgressivo()  ");
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

	private String decodifica(String tabella, String nome_cod, Object val_codice, String descrizione, ISASConnection dbc) {
		Hashtable htxt = new Hashtable();
		if (val_codice == null)
			return " ";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE " + nome_cod + " ='" + val_codice.toString()
					+ "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			return ((String) dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return " ";
		}
	}

	/**
	 * elisa b 17/04/12
	 * se dal client arriva l'informazione della
	 * modifica del medico e/o della data in cui il medico e' stato
	 * scelto, si deve aggiornare o inserire un record che ha per
	 * data variazione la data di scelta del medico ed, eventualmente,
	 * aggiornare il valore del medico e della data in record con data variazione
	 * successiva.
	 * @param dbc
	 * @param h
	 */
	private void gestioneMedico(ISASConnection dbc, Hashtable h, String codice, String codOperatore) throws Exception {
		ISASCursor dbcur = null;
		try {
			String dtVariazione = (String) h.get("data_variazione");
			String dtRiferimento = (String) h.get("data_medico");
			String codMedico = (String) h.get("cod_med");
			DataWI dtRif = new DataWI(dtRiferimento.replaceAll("-", ""), 1);
			String sel = "SELECT * FROM anagra_c" + " WHERE n_cartella = " + codice + " AND data_variazione = "
					+ formatDate(dbc, dtRiferimento);

			ISASRecord dbrAn = dbc.readRecord(sel);
			if (dbrAn != null) // aggiorno
				System.out.println("aggiorno");
			else { // inserisco
				System.out.println("Nuovo record");
				dbrAn = dbc.newRecord("anagra_c");
				dbrAn.put("cod_operatore", codOperatore);
			}
			dbrAn.put("n_cartella", codice);
			Enumeration n2 = h.keys();
			while (n2.hasMoreElements()) {
				String elem2 = (String) n2.nextElement();
				dbrAn.put(elem2, h.get(elem2));
			}
			dbrAn.put("data_medico", dtRif.getSqlDate());
			dbrAn.put("data_variazione", dtRif.getSqlDate());
			dbc.writeRecord(dbrAn);
			System.out.println("Inserito un record in data " + dtRiferimento);

			/* se esistono altri record con data variazione successiva alla data 
			 * di riferimento, in essi si devono aggiornare i campi.
			 * Il record con data variazione passato come parametro
			 * viene gia' aggiornato dalla normale procedura*/
			sel = "SELECT * FROM anagra_c" + " WHERE n_cartella = " + codice + " AND data_variazione > " + formatDate(dbc, dtRiferimento)
					+ " AND data_variazione < " + formatDate(dbc, dtVariazione);
			dbcur = dbc.startCursor(sel);
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				String dt = (String) dbr.get("data_variazione");
				String selUpd = "SELECT * FROM anagra_c" + " WHERE n_cartella = " + codice + " AND data_variazione = "
						+ formatDate(dbc, dt);
				ISASRecord dbrU = dbc.readRecord(selUpd);
				dbrU.put("cod_med", codMedico);
				dbrU.put("data_medico", dtRif.getSqlDate());
				dbc.writeRecord(dbrU);
				System.out.println("Aggiornato un record in data successiva");
			}

		} finally {
			if (dbcur != null)
				dbcur.close();
		}

	}

	/*gb 26/09/07 *******
	private void AggiornaData(String tabella, String cartella,String data_tabella,String data_chiusura,String data_ini_tab,ISASConnection dbc)
	throws  SQLException{
	        try {
			String mysel = "SELECT * FROM " + tabella + " WHERE "+
				"n_cartella =" + cartella ;
			ISASCursor dbcur=dbc.startCursor(mysel);
			Vector vdbr=dbcur.getAllRecord();
	                for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
	                {
	                  ISASRecord dbr=(ISASRecord)senum.nextElement();
	                  String data_inizio=((java.sql.Date)dbr.get(data_ini_tab)).toString();
	                  System.out.println(data_ini_tab +" "+data_inizio);
	                  String sel = "SELECT * FROM " + tabella + " WHERE "+
				       "n_cartella = " + cartella + " AND "+
	                               "n_contatto = " + (Integer)dbr.get("n_contatto") + " AND "+
	//                               data_ini_tab + " = '" + data_inizio+"'";
	                               data_ini_tab + " = " + formatDate(dbc,data_inizio);
	                  ISASRecord dbrDett=dbc.readRecord(sel);
	                  if(dbrDett.get(data_tabella)==null)// && (data_inizio!=null && !(data_inizio.equals(""))))
	                    dbrDett.put(data_tabella,data_chiusura);
	                  dbc.writeRecord(dbrDett);
	                }//fine for
	                dbcur.close();
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}

	private void AggiornaDataContsan(String cartella,String data_chiusura,ISASConnection dbc)
	throws  SQLException{
	        try {
			String mysel = "SELECT * FROM contsan WHERE "+
				"n_cartella =" + cartella ;
			ISASCursor dbcur=dbc.startCursor(mysel);
			Vector vdbr=dbcur.getAllRecord();
	                for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
	                {
	                  ISASRecord dbr=(ISASRecord)senum.nextElement();
	                  String sel = "SELECT * FROM contsan WHERE "+
				       "n_cartella = " + cartella + " AND "+
	                               "n_contatto = " + (Integer)dbr.get("n_contatto");
	                  ISASRecord dbrDett=dbc.readRecord(sel);
	                  if(dbrDett.get("data_chius_medico")==null && dbrDett.get("data_medico")!=null)
	                    dbrDett.put("data_chius_medico",data_chiusura);
	                  if(dbrDett.get("data_chius_infer")==null && dbrDett.get("data_infer")!=null)
	                    dbrDett.put("data_chius_infer",data_chiusura);
	                  if(dbrDett.get("data_chius_sociale")==null && dbrDett.get("data_sociale")!=null)
	                    dbrDett.put("data_chius_sociale",data_chiusura);
	                  if(dbrDett.get("data_chius_fisiot")==null && dbrDett.get("data_fisiot")!=null)
	                    dbrDett.put("data_chius_fisiot",data_chiusura);
	                  dbc.writeRecord(dbrDett);
	                }//fine for
	                dbcur.close();
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}
	private void AggiornaDataProgetto(String cartella,String data_chiusura,ISASConnection dbc,String motivo)
	throws  SQLException{
	        try {
			String mysel = "SELECT * FROM progetto WHERE "+
				"n_cartella =" + cartella +
	                        " and pr_data_chiusura is null";
			ISASRecord dbr=dbc.readRecord(mysel);
	                if (dbr!=null){
	                  dbr.put("pr_data_chiusura",data_chiusura);
	                  if(motivo!=null){
	                   if(motivo.equals("1"))
	                     dbr.put("pr_motivi_val_ch","6");
	                   else if(motivo.equals("2"))
	                     dbr.put("pr_motivi_val_ch","7");
	                  }
	                  dbc.writeRecord(dbr);
	                   }

		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}
	*gb 26/09/07: fine *******/

}

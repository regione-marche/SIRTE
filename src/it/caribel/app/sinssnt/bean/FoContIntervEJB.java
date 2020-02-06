package it.caribel.app.sinssnt.bean;

//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 29/01/2002 - EJB di connessione alla procedura SINS Tabella FoContInterv
//
// Giulia Brogi
//
// ==========================================================================

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
// fo merge

public class FoContIntervEJB extends SINSSNTConnectionEJB {

	public static String getjdbcDate() {
		java.util.Date d = new java.util.Date();
		java.text.SimpleDateFormat local_dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
		return local_dateFormat.format(d);
	}

	private String MIONOME = "15-FoContIntervEJB.";

	public FoContIntervEJB() {  
	}

	public byte[] query_continterv(String utente, String passwd, Hashtable par, mergeDocument doc) throws SQLException {
		String punto = MIONOME + "query_continterv ";
		stampa(punto + "Inizio con dati>" + par + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		byte[] rit;
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			String myselect = "";
			String cartella = "";
			//			dtInizio=/__/&dtFine=/__/&codOperatore=
			String codiceOperatore = ISASUtil.getValoreStringa(par, "codOperatore");
			String dtInizio = ISASUtil.getValoreStringa(par, "dtInizio");
			String dtFine = ISASUtil.getValoreStringa(par, "dtFine");
			String anno = "";
			if (par.get("cartella") != null)
				if (!((String) par.get("cartella")).equals(""))
					cartella = (String) par.get("cartella");

			if (par.get("anno") != null)
				if (!((String) par.get("anno")).equals(""))
					anno = (String) par.get("anno");

			myselect = "select o.codice , o.cognome opcognome, o.nome opnome,  a.int_anno, a.int_cartella,a.int_contatore,b.cognome,b.nome,"
					+ "a.int_data_prest, o.cognome opcogn from interv a, cartella b,operatori o "
					+ " where a.int_cartella = b.n_cartella "
					+ " and a.int_cartella=" + cartella + " and o.codice=a.int_cod_oper";

			if (!anno.equals(""))
				myselect = myselect + " and a.int_anno='" + (String) par.get("anno") + "' ";

			if (ISASUtil.valida(codiceOperatore)) {
				myselect += " AND int_cod_oper ='" + codiceOperatore + "' ";
			}

			if (ISASUtil.valida(dtInizio) && dtInizio.length() >= 10) {
				myselect += " AND int_data_prest >= " + formatDate(dbc, dtInizio);
			}
			if (ISASUtil.valida(dtFine) && dtFine.length() >= 10) {
				myselect += " AND int_data_prest <= " + formatDate(dbc, dtFine);
			}

			//			myselect = myselect + " order by a.int_anno,b.cognome,b.nome," + "a.int_contatore";
			myselect = myselect + " ORDER BY a.int_data_prest, o.cognome, o.nome, o.codice ";
			stampa(punto + " Query>" + myselect);
			ISASCursor dbcur = dbc.startCursor(myselect);

			String cognomeNomeOperatore = "";

			boolean entrato = false;
			TreeMap dettaglioOperatoriAccessi = new TreeMap();
			//			doc.write("layout");
			preparaLayout(doc, dbc, par);

			while (dbcur.next()) {
				Hashtable htab = new Hashtable();
				ISASRecord dbr = dbcur.getRecord();
				if (!entrato) {
					//					stampaDoc(punto, "tabellaIntestazione", null);
					doc.write("tabellaIntestazione");
					entrato = true;
				}

				cognomeNomeOperatore = ISASUtil.getValoreStringa(dbr, "opcognome");
				cognomeNomeOperatore += (ISASUtil.valida(cognomeNomeOperatore) ? " " : "") + ISASUtil.getValoreStringa(dbr, "opnome");
				codiceOperatore = ISASUtil.getValoreStringa(dbr, "codice");

				htab.put("#contatore#", ((Integer) dbr.get("int_contatore")).toString());

				String giorno = ((java.sql.Date) dbr.get("int_data_prest")).toString();
				giorno = giorno.substring(8, 10) + "/" + giorno.substring(5, 7) + "/" + giorno.substring(0, 4);
				htab.put("#data_interv#", giorno);
				settaDatiOperatore(dettaglioOperatoriAccessi, cognomeNomeOperatore, codiceOperatore);

				htab.put("#operatore#", cognomeNomeOperatore);
				//				if (dbr.get("opcogn") != null) {
				//					if (!((String) dbr.get("opcogn")).equals("")) {
				//						htab.put("#operatore#", (String) dbr.get("opcogn"));
				//					} else
				//						htab.put("#operatore#", " ");
				//				} else
				//					htab.put("#operatore#", " ");

				//				stampaDoc(punto, "tabellaCorpo", htab);
				doc.writeSostituisci("tabellaCorpo", htab);
				//				doc.writeSostituisci("totale", htab);

			}//end while	   

			if (!entrato)
				doc.write("messaggio");
			else {
				//				stampaDoc(punto, "tabellaFine", null);
				doc.write("tabellaFine");
				stampa(punto + "\n Stampare il dettaglio degli accessi \ndettaglioOperatoriAccessi>" + dettaglioOperatoriAccessi.size()
						+ "<");
				if (dettaglioOperatoriAccessi.size() > 0) {
					doc.write("dettaglioIntestazione");
					Iterator it = dettaglioOperatoriAccessi.entrySet().iterator();
					InformazioniOperatore informazioniOperatore = null;
					Hashtable prtDati = new Hashtable();
					int totaleAccessi = 0;
					while (it.hasNext()) {
						Map.Entry et = (Map.Entry) it.next();
						informazioniOperatore = (InformazioniOperatore) et.getValue();
						prtDati.put("#codiceOperatore#", informazioniOperatore.getCodiceOperatore());
						prtDati.put("#cognomeNome#", informazioniOperatore.getCognomeNome());
						prtDati.put("#numeroAccessi#", informazioniOperatore.getNumeroAccessi() + "");
						totaleAccessi+=informazioniOperatore.getNumeroAccessi();
						doc.writeSostituisci("dettaglioCorpo", prtDati);
					}
					prtDati.put("#totale#", totaleAccessi+"");
					doc.writeSostituisci("dettaglioCorpoTotale", prtDati);
					doc.write("dettaglioFine");
				}
				doc.write("finetab");
			}
			doc.write("finale");
			doc.close();
			//riprendo il bytearray
			rit = (byte[]) doc.get();
			//riprendo l'array di byte
			stampa(punto + "\n FINE ESECUZIONE \n");
			//			String by = new String(rit);
			//			System.out.println("Stringa del byte array   :" + by);
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_continterv()  ");
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

	}//end query_continterv

	private void preparaLayout(mergeDocument doc, ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "preparaLayout ";

		Hashtable prtDati = new Hashtable();
		String query = "SELECT conf_txt from conf " + "WHERE conf_kproc='SINS' AND" + " conf_key='ragione_sociale'";
		stampaQuery(punto, query);
		ISASRecord dbconf = getRecord(dbc, query);
		prtDati.put("#conf_txt#", ISASUtil.getValoreStringa(dbconf, "conf_txt"));

		prtDati.put("#anno#", ISASUtil.getValoreStringa(par, "anno"));
		String nCartella = ISASUtil.getValoreStringa(par, "cartella");
		prtDati.put("#int_cartella#", nCartella);

		query = " select cognome, nome from cartella where n_cartella = " + nCartella;
		stampaQuery(punto, query);
		ISASRecord dbrCartella = getRecord(dbc, query);
		String cognomeNome = ISASUtil.getValoreStringa(dbrCartella, "cognome");
		cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "") + ISASUtil.getValoreStringa(dbrCartella, "nome");
		prtDati.put("#nome_cartella#", cognomeNome);
		String filtri = "";
		String codOperatore = ISASUtil.getValoreStringa(par, "codOperatore");
		if (ISASUtil.valida(codOperatore)) {
			query = "SELECT * FROM OPERATORI WHERE CODICE = '" + codOperatore + "'";
			ISASRecord dbrOperatore = getRecord(dbc, query);
			cognomeNome = ISASUtil.getValoreStringa(dbrOperatore, "cognome");
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "") + ISASUtil.getValoreStringa(dbrOperatore, "nome");
			filtri += "Operatore: " + codOperatore + " - " + cognomeNome;
		} else {
			stampa(punto + "\n NON DECODIFICO OPERATORE\n ");
		}

		String dtInizio = ISASUtil.getValoreStringa(par, "dtInizio");

		if (ISASUtil.valida(dtInizio) && dtInizio.trim().length() >= 10) {
			filtri += (ISASUtil.valida(filtri) ? " " : "") + "Periodo dal: " + dtInizio;
		}

		String dtFine = ISASUtil.getValoreStringa(par, "dtFine");
		if (ISASUtil.valida(dtFine) && dtFine.trim().length() >= 10) {
			filtri += (ISASUtil.valida(filtri) ? " " : "");
			filtri += ((ISASUtil.valida(dtInizio) && dtInizio.trim().length() >= 10) ? "al: " : "Fino a: ") + dtFine;
		}
		prtDati.put("#filtri_ricerca#", filtri);

		//		stampaDoc(punto, "layout", prtDati);
		doc.writeSostituisci("layout", prtDati);
		//		hconf.put("#anno#", (String) dbr.get("int_anno"));
		//		String cart = "";
		//		if (dbr.get("cognome") != null)
		//			if (!((String) dbr.get("cognome")).equals("")) {
		//				cart = (String) dbr.get("cognome");
		//				if (dbr.get("nome") != null)
		//					if (!((String) dbr.get("nome")).equals(""))
		//						cart = cart + " " + (String) dbr.get("nome");
		//				hconf.put("#nome_cartella#", cart);
		//			} else
		//				hconf.put("#nome_cartella#", " ");
		//		else
		//			hconf.put("#nome_cartella#", " ");
		//
		//		hconf.put("#int_cartella#", ((Integer) dbr.get("int_cartella")).toString());
		//		//					hconf.put("#filtri_ricerca#", "");
		//		doc.writeSostituisci("before", hconf);

	}

	private void stampaQuery(String punto, String query) {
		System.out.println(punto + " Query>" + query + "<\n");
	}

	private void stampaDoc_(String punto, String sezione, Hashtable prtDati) {
		System.out.println(punto + "\n Sezione da stampare>" + sezione + "\n" + (prtDati != null ? "Dati>" + prtDati : ""));

	}

	private ISASRecord getRecord(ISASConnection dbc, String query) {
		String punto = MIONOME + "getRecord ";
		ISASRecord dbrRecord = null;
		stampa(punto + "\n Query>" + query + "<");
		try {
			dbrRecord = dbc.readRecord(query);
		} catch (Exception e) {
			stampa(punto + "\n Errore in recupero record>" + query + "<\n");
			e.printStackTrace();
		}

		return dbrRecord;
	}

	private void settaDatiOperatore(TreeMap dettaglioOperatoriAccessi, String cognomeNome, String codiceOperatore) {
		String punto = MIONOME + "settaDatiOperatore ";

		//		stampa(punto + " Inserisco codOperatore>" + codiceOperatore + "< Cognome>" + cognomeNome + "<\n");
		int numeroAccessi = 1;
		if (ISASUtil.valida(codiceOperatore) && ISASUtil.valida(cognomeNome)) {
			InformazioniOperatore informazioniOperatore = null;
			if (dettaglioOperatoriAccessi.containsKey(cognomeNome)) {
				//				stampa(punto + "\t setto un operatore gi� preesistente");
				informazioniOperatore = (InformazioniOperatore) dettaglioOperatoriAccessi.get(cognomeNome);
				numeroAccessi = informazioniOperatore.getNumeroAccessi() + 1;
				informazioniOperatore.setNumeroAccessi(numeroAccessi);
			} else {
				//				stampa(punto + "\t inserisco un dipendente non esistente");
				informazioniOperatore = new InformazioniOperatore(codiceOperatore, cognomeNome, numeroAccessi);
			}
			dettaglioOperatoriAccessi.put(cognomeNome, informazioniOperatore);
		}
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

} // End of FoContInterv class

class InformazioniOperatore {
	String codiceOperatore = "";
	String cognomeNome = "";
	int numeroAccessi = 0;

	public InformazioniOperatore(String codiceOperatore, String cognomeNome, int numeroAccessi) {
		super();
		this.codiceOperatore = codiceOperatore;
		this.cognomeNome = cognomeNome;
		this.numeroAccessi = numeroAccessi;
	}

	public String getCodiceOperatore() {
		return codiceOperatore;
	}

	public void setCodiceOperatore(String codiceOperatore) {
		this.codiceOperatore = codiceOperatore;
	}

	public String getCognomeNome() {
		return cognomeNome;
	}

	public void setCognomeNome(String cognomeNome) {
		this.cognomeNome = cognomeNome;
	}

	public int getNumeroAccessi() {
		return numeroAccessi;
	}

	public void setNumeroAccessi(int numeroAccessi) {
		this.numeroAccessi = numeroAccessi;
	}

}
package it.caribel.app.sinssnt.bean;
// ============================================================================

// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// ============================================================================

import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class FoInfEleInvSegMotivoEJB extends SINSSNTConnectionEJB {

	public FoInfEleInvSegMotivoEJB() {
	}

	String dom_res;
	String dr;
	private String MIONOME = "49-FoInfEleInvSegMotivoEJB.";
	private String sezioneDoc = "";
	private static final String CODICE_TIPO_AREA_DISTRETTUALE = "A";
	private static final String CODICE_TIPO_COMUNE = "C";
	private static final String CODICE_TIPO_PRESIDIO = "P";//M.Minerba per Pistoia 04/02/2013
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO = "D";
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA = "R";
	private boolean nessunaDivisione = false;
	private static final int STAMPA_ZONA = 1;
	private static final int STAMPA_DISTRETTO = 2;
	private static final int STAMPA_COMUNE = 3;

	public byte[] query_inf(String utente, String passwd, Hashtable par, mergeDocument doc) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		String punto = MIONOME + ".query_inf ";
		stampa(punto + "\n Inizio con dati>" + par + "<\n");

		ISASCursor dbcur = null;
		try {

			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			String query = getQuery(dbc, par);
			stampaQuery(punto, query);
			dbcur = dbc.startCursor(query);
			stampa(punto + "ho recuperato elementi:" + (dbcur != null ? dbcur.getDimension() + "" : " NON ho recuperato nulla "));

			preparaLayout(doc, dbc, par);

			if (dbcur == null) {
				doc.write("messaggio");
				doc.write("finale");
			} else {
				if (dbcur.getDimension() <= 0) {
					doc.write("messaggio");
					doc.write("finale");
				} else {
					preparaBody(doc, dbcur, par, dbc);
					stampaDoc("finale", null);
					doc.write("finale");
				}
			}
			dbcur.close();
			doc.close();
			stampa(punto + " \n Fine esecuzione programma\n");
			dbc.close();
			super.close(dbc);
			done = true;
			return (byte[]) doc.get();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(punto + "Errore eseguendo una query_elencoAss()  ");

		} finally {
			if (!done) {
				if (dbcur != null) {
					try {
						dbcur.close();
					} catch (DBSQLException e) {
						e.printStackTrace();
					} catch (DBMisuseException e) {
						e.printStackTrace();
					} catch (ISASMisuseException e) {
						e.printStackTrace();
					}
				}
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	private void preparaBody(mergeDocument doc, ISASCursor dbcur, Hashtable par, ISASConnection dbc) {
		String punto = MIONOME + "preparaBody ";
		String zona = "";
		String distretto = "";
		String areadis = "";
		String zonaOld = "";
		String distrettoOld = "";
		String areadisOld = "";
		String assistito = "";
		int totAreaDisAssistiti = 0;

		int totDistrettoAssistiti = 0;
		int totZonaAssistiti = 0;
		int totGeneraleAssistiti = 0;

		Hashtable prtDati = new Hashtable();
		Hashtable numeroAssistiti = new Hashtable();
		try {

			String raggruppamento, descrizione = "", descrPace = "";
			String descrizioneEtichetta = "";
			Hashtable hZ = new Hashtable();

			raggruppamento = ISASUtil.getValoreStringa(par, "ragg");
			if (ISASUtil.valida(raggruppamento)) {
				if (raggruppamento.equals(CODICE_TIPO_COMUNE)) {
					descrPace = "Comune ";
				} else if (raggruppamento.equals(CODICE_TIPO_AREA_DISTRETTUALE)) {

					descrPace = "Area distrettuale";			
					//M.Minerba per Pistoia 04/02/2013
				}else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {

					descrPace = "Presidio";					
				}
			}

			hZ.put("#pace#", descrPace);
			stampaDoc("iniziotabConclusioni-html", hZ);
			doc.writeSostituisci("iniziotabConclusioni-html", hZ);
			String codMotivo = "";
			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				//				stampa(punto + "dati che esamino>" + (dbr != null ? dbr.getHashtable() + "" : "Non ho dati"));
				zona = ISASUtil.getValoreStringa(dbr, "cod_zona");
				distretto = ISASUtil.getValoreStringa(dbr, "cod_distretto");
				areadis = ISASUtil.getValoreStringa(dbr, "codice");

				if ((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld)) || (!areadis.equals(areadisOld))) {
					if (!zonaOld.equals("")) {
						stampaDoc("finetabConclusioni", null);
						doc.write("finetabConclusioni");
						if ((!areadis.equals(areadisOld))) {
							totAreaDisAssistiti = numeroAssistiti.size();
							descrizioneEtichetta = descrPace + ": " + descrizione;
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#numero_assist#", totAreaDisAssistiti + "");
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", descrizione);
							stampaParziali(doc, prtDati, hZ, STAMPA_COMUNE);
							stampaParzialiHtml(doc, prtDati, hZ);

							totDistrettoAssistiti += totAreaDisAssistiti;
							totAreaDisAssistiti = 0;
							numeroAssistiti.clear();
						}

						if ((!distretto.equals(distrettoOld))) {
							descrizioneEtichetta = "Distretto: " + ISASUtil.getValoreStringa(hZ, "#des_distr#");
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#numero_assist#", totDistrettoAssistiti + "");
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#des_distr#"));
							stampaParziali(doc, prtDati, hZ, STAMPA_DISTRETTO);
							totZonaAssistiti += totDistrettoAssistiti;
							totDistrettoAssistiti = 0;
						}

						if ((!zona.equals(zonaOld))) {
							descrizioneEtichetta = "Zona: " + ISASUtil.getValoreStringa(hZ, "#descrizione_zona#");
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#numero_assist#", totZonaAssistiti + "");
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#descrizione_zona#"));
							stampaParziali(doc, prtDati, hZ, STAMPA_ZONA);
							totGeneraleAssistiti += totZonaAssistiti;
							totZonaAssistiti = 0;
						}
					}

					hZ.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
					hZ.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));

					raggruppamento = ISASUtil.getValoreStringa(dbr, "tipo");
					descrizione = "";
					descrPace = "";
					if (ISASUtil.valida(raggruppamento)) {
						String codice = ISASUtil.getValoreStringa(dbr, "codice");
						if (raggruppamento.equals(CODICE_TIPO_COMUNE)) {
							descrPace = "Comune ";
							if (ISASUtil.valida(codice)) {
								descrizione = recuperaDescrizioneComune(dbc, codice);
							}
						} else if (raggruppamento.equals(CODICE_TIPO_AREA_DISTRETTUALE)) {
							descrPace = "Area distrettuale ";
							if (ISASUtil.valida(codice)) {
								descrizione = recuperaDescrizioneAreaDistrettuale(dbc, codice);
							}
						}
						//M.Minerba per Pistoia 04/02/2013
						else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
							descrPace = "Presidio ";
							if (ISASUtil.valida(codice)) {
								descrizione = recuperaDescrizionePresidio(dbc, codice);
							}
						}
					}

					hZ.put("#descrizione#", descrizione);
					hZ.put("#pace#", descrPace);

					stampaDoc("zona", null);
					if (!nessunaDivisione) {
						doc.writeSostituisci("zona", (Hashtable) hZ.clone());
					}
					zonaOld = zona;
					distrettoOld = distretto;
					areadisOld = areadis;
					stampaDoc("iniziotabConclusioni", null);
					doc.write("iniziotabConclusioni");
				}
				String cartella = ISASUtil.getValoreStringa(dbr, "n_cartella");

				prtDati.putAll(hZ);

				prtDati.put("#n_cartella#", cartella);
				assistito = ISASUtil.getValoreStringa(dbr, "cognome");
				assistito += (ISASUtil.valida(assistito) ? " " : "") + ISASUtil.getValoreStringa(dbr, "nome");
				prtDati.put("#assistito#", assistito);
				prtDati.put("#dt_nascita#", getDateField(dbr, "data_nasc"));
				prtDati.put("#dt_apertura#", getDateField(dbr, "ski_data_apertura"));
				prtDati.put("#descr_conta#", ISASUtil.getValoreStringa(dbr, "ski_descr_contatto"));
				prtDati.put("#dt_uscita#", getDateField(dbr, "ski_data_uscita"));
				codMotivo = ISASUtil.getValoreStringa(dbr, "ski_motivo");
				prtDati.put("#motivo#", recuperaDescrizioneModalita(dbc, codMotivo));

				stampaDoc("tabellaConclusioni", prtDati);
				doc.writeSostituisci("tabellaConclusioni", prtDati);
				numeroAssistiti.put(cartella, "");
			}
			//			doc.write("finetabConclusioni");

			stampaDoc("finetabConclusioni", null);
			doc.write("finetabConclusioni");
			stampaDoc("finetabConclusioni-hmtl", null);
			doc.write("finetabConclusioni-hmtl");

			totAreaDisAssistiti = numeroAssistiti.size();
			//			stampa(punto + "numero assistit>" + totAreaDisAssistiti + " <\n " + numeroAssistiti + "\n");
			descrizioneEtichetta = descrPace + ": " + descrizione;
			prtDati.put("#descrPace#", descrizioneEtichetta);
			prtDati.put("#numero_assist#", totAreaDisAssistiti + "");
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", descrizione);
			stampaParziali(doc, prtDati, hZ, STAMPA_COMUNE);
			stampaParzialiHtml(doc, prtDati, hZ);

			totDistrettoAssistiti += totAreaDisAssistiti;
			descrizioneEtichetta = "Distretto: " + ISASUtil.getValoreStringa(hZ, "#des_distr#");
			prtDati.put("#descrPace#", descrizioneEtichetta);
			prtDati.put("#numero_assist#", totDistrettoAssistiti + "");
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#des_distr#"));
			stampaParziali(doc, prtDati, hZ, STAMPA_DISTRETTO);
			totZonaAssistiti += totDistrettoAssistiti;

			descrizioneEtichetta = "Zona: " + ISASUtil.getValoreStringa(hZ, "#descrizione_zona#");
			prtDati.put("#descrPace#", descrizioneEtichetta);
			prtDati.put("#numero_assist#", totZonaAssistiti + "");
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#descrizione_zona#"));
			stampaParziali(doc, prtDati, hZ, STAMPA_ZONA);

			totGeneraleAssistiti += totZonaAssistiti;
			prtDati.put("#numero_assisti#", totGeneraleAssistiti + "");
			doc.writeSostituisci("totale_Sintetico", prtDati);

		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		stampa(punto + " fine esecuzione ");

	}

	private void stampaDoc(String sezione, Hashtable hz) {
		//		System.out.println("sezione>" + sezione);
		sezioneDoc += sezione + ", ";
		//		System.out.println("sezione>" + sezione + "\n " + (hz != null ? "Dati>" + hz + "<" : ""));
	}

	private void stampaParzialiHtml(mergeDocument doc, Hashtable prtDati, Hashtable zone) {
		stampaParziale(doc, prtDati, zone, true, 0);
	}

	private void stampaParziali(mergeDocument doc, Hashtable prtDati, Hashtable zone, int tipoStampa) {
		stampaParziale(doc, prtDati, zone, false, tipoStampa);
	}

	//	private void stampaParziale(mergeDocument doc, Hashtable prtDati, Hashtable zone, boolean html) {
	private void stampaParziale(mergeDocument doc, Hashtable prtDati, Hashtable zone, boolean html, int tipoStampa) {
		String punto = MIONOME + "stampaParziale ";
		String sezione = "";
		boolean stampaSeparatore = false;
		if (!nessunaDivisione) {
			if (html) {
				sezione = "_html";
			}
			prtDati.putAll(zone);

			prtDati.put("#descrZona#", "");
			prtDati.put("#numero_assist_zona#", "");
			prtDati.put("#descrDistr#", "");
			prtDati.put("#numero_assist_distr#", "");
			prtDati.put("#descrPace_sint#", "");
			prtDati.put("#numero_assist_pace#", "");
			prtDati.put("#descrizioneZonaDistretto#", "");

			switch (tipoStampa) {
			case STAMPA_ZONA:
				//				prtDati.put("#descrZona#", "Totale della zona: " + ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
				prtDati.put("#descrizioneZonaDistretto#", "Totale della zona: " + ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
				prtDati.put("#numero_assist_zona#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				stampaSeparatore = true;
				break;
			case STAMPA_DISTRETTO:
				prtDati
						.put("#descrizioneZonaDistretto#", "Totale del distretto: "
								+ ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
				prtDati.put("#numero_assist_distr#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				prtDati.put("#numero_assist_pace#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				stampaSeparatore = true;
				break;
			case STAMPA_COMUNE:
				prtDati.put("#descrPace_sint#", "Totale " + ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
				prtDati.put("#numero_assist_pace#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				break;
			default:
				break;
			}
			stampaDoc("totali" + sezione, prtDati);
			doc.writeSostituisci("totali" + sezione, (Hashtable) prtDati.clone());
			if (stampaSeparatore) {
				doc.write("separatore");
			}
		}
	}

	private void stampaQuery(String punto, String query) {
		System.out.println(punto + " Query>" + query);
	}

	private String getQuery(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getFiltro ";
		ServerUtility su = new ServerUtility();

		String dtInizio = ISASUtil.getValoreStringa(par, "data_inizio");
		String dataFine = ISASUtil.getValoreStringa(par, "data_fine");
		String zona = ISASUtil.getValoreStringa(par, "zona");
		String distretto = ISASUtil.getValoreStringa(par, "distretto");
		String pca = ISASUtil.getValoreStringa(par, "pca");
		String motivo = ISASUtil.getValoreStringa(par, "inviato");
		String condWhere = "";

		String tipoStampa = (String) par.get("terr");
		StringTokenizer st = new StringTokenizer(tipoStampa, "|");
		String sZona = st.nextToken();
		String sDis = st.nextToken();
		String sCom = st.nextToken();

		String selZona = "";
		String selDistretto = "";
		String selComune = "";

		if (sZona.equals("1")) {
			selZona = " u.cod_zona,u.des_zona descrizione_zona, ";
		} else {
			nessunaDivisione = true;
			selZona = " 'NESSUNA DIVISIONE' cod_zona,'NESSUNA DIVISIONE' descrizione_zona, ";
		}

		if (sDis.equals("1"))
			selDistretto = " u.des_distretto," + "u.cod_distretto" + " as cod_distretto, ";
		else
			selDistretto = " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto, ";

		if (sCom.equals("1"))
			selComune = " u.codice ,u.descrizione ,";
		else
			selComune = " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione, ";

		StringBuffer query = new StringBuffer();

		query
				.append(" SELECT   c.data_nasc, c.n_cartella, sk.ski_motivo,sk.ski_data_uscita, sk.ski_data_apertura, sk.ski_descr_contatto, ");
		//		query.append(" c.nome, c.cognome, u.cod_zona, u.des_zona descrizione_zona , u.cod_distretto, u.codice, u.tipo ");
		query.append(" c.nome, c.cognome, " + selZona + selDistretto + selComune + " u.tipo ");
		query.append(" FROM skinf sk, cartella c, "
				+ ((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n")
				+ "  u, anagra_c ac WHERE sk.n_cartella = ac.n_cartella ");
		condWhere = " AND ac.n_cartella = c.n_cartella  AND ac.n_cartella = c.n_cartella ";
		if (ISASUtil.valida(motivo) && (!motivo.equals("0"))) {
			query.append(" AND sk.ski_motivo = '");
			query.append(motivo);
			query.append("' ");
		}
		if (ISASUtil.valida(dtInizio) && dtInizio.length() >= 10) {
			query.append(" AND sk.ski_data_apertura >= " + formatDate(dbc, dtInizio));
		}

		if (ISASUtil.valida(dataFine) && dataFine.length() >= 10) {
			query.append(" AND sk.ski_data_apertura <= " + formatDate(dbc, dataFine));
		}
		query.append(" AND ac.data_variazione IN (SELECT MAX (data_variazione) FROM anagra_c WHERE ac.n_cartella = anagra_c.n_cartella ");
		if (ISASUtil.valida(dataFine) && dataFine.length() >= 10) {
			query.append(" AND data_variazione <= " + formatDate(dbc, dataFine));
		}
		query.append(" ) ");

		String ragg = ISASUtil.getValoreStringa(par, "ragg");
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, zona);
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, distretto);
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, pca);
		query.append(condWhere);
		if ((String) par.get("dom_res") == null) {
			if (ragg.equals("C")) {
				query.append(" AND (( (ac.dom_citta IS NOT NULL OR ac.dom_citta <> '') AND u.codice=ac.dom_citta) ");
				query.append(" OR ( (ac.dom_citta IS NULL OR ac.dom_citta = '') " + " AND u.codice=ac.citta))");
			} else if (ragg.equals("A")) {
				query.append(" AND (( (ac.dom_areadis IS NOT NULL OR ac.dom_areadis <> '')" + " AND u.codice=ac.dom_areadis)");
				query.append(" OR ( (ac.dom_areadis IS NULL OR ac.dom_areadis = '')  AND u.codice=ac.areadis)) ");
			}
			//M.Minerba per Pistoia 04/02/2013
			else if (ragg.equals("P")) {
				query.append(" AND u.codice=sk.ski_cod_presidio ");
			}
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				query.append(" AND u.codice=ac.dom_citta ");
			else if (ragg.equals("A"))
				query.append(" AND u.codice=ac.dom_areadis ");
			//M.Minerba per Pistoia 04/023/2013
			else if (ragg.equals("P"))
				query.append(" AND u.codice=sk.ski_cod_presidio ");
		} else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				query.append(" AND u.codice=ac.citta");
			else if (ragg.equals("A"))
				query.append(" AND u.codice=ac.areadis ");
			//M.Minerba per Pistoia 04/023/2013
			else if (ragg.equals("P"))
				query.append(" AND u.codice=sk.ski_cod_presidio ");
		}
		query.append(" ORDER BY cod_zona,u.cod_distretto,u.codice, cognome, nome, sk.ski_data_apertura");

		return query.toString();
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	private String getDateField(ISASRecord dbr, String f) {
		try {
			if (dbr.get(f) == null)
				return "";
			String d = ((java.sql.Date) dbr.get(f)).toString();
			d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/" + d.substring(0, 4);
			return d;
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	public Vector caricaDati(ISASConnection dbc, ISASRecord dbr, Vector vDati) throws SQLException {
		try {

			Hashtable tab = new Hashtable();
			int cartella = 0;
			if (dbr.get("n_cartella") != null)
				cartella = ((Integer) dbr.get("n_cartella")).intValue();
			tab.put("#cartella#", "" + cartella);

			String cognome = "";
			if (dbr.get("cognome") != null && !((String) dbr.get("cognome")).equals(""))
				cognome = (String) dbr.get("cognome");

			String nome = "";
			if (dbr.get("nome") != null && !((String) dbr.get("nome")).equals(""))
				nome = (String) dbr.get("nome");
			tab.put("#assistito#", cognome.trim() + " " + nome.trim());

			/* tab.put("#comune_nasc#",decodifica("comuni","codice",
			                   (String)dbr.get("cod_com_nasc"),"descrizione",dbc));
			 */

			String data_u = "";
			if (dbr.get("ski_data_uscita") != null) {
				data_u = "" + ((java.sql.Date) dbr.get("ski_data_uscita"));
				data_u = data_u.substring(8, 10) + "/" + data_u.substring(5, 7) + "/" + data_u.substring(0, 4);
				tab.put("#ski_data_uscita#", data_u);
			} else
				tab.put("#ski_data_uscita#", "");
			String data_e = "";
			if (dbr.get("ski_data_apertura") != null) {
				data_e = "" + ((java.sql.Date) dbr.get("ski_data_apertura"));
				data_e = data_e.substring(8, 10) + "/" + data_e.substring(5, 7) + "/" + data_e.substring(0, 4);
				tab.put("#ski_data_apertura#", data_e);
			} else
				tab.put("#ski_data_apertura#", "");
			String data_nasc = "";

			if (dbr.get("data_nasc") != null) {
				data_nasc = ((java.sql.Date) dbr.get("data_nasc")).toString();
				if (data_nasc.length() == 10)
					data_nasc = data_nasc.substring(8, 10) + "/" + data_nasc.substring(5, 7) + "/" + data_nasc.substring(0, 4);
			}

			tab.put("#data_nasc#", data_nasc);
			vDati.add(tab);
			return vDati;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una caricaDati()");
		}
	}

	private void preparaLayout(mergeDocument md, ISASConnection dbc, Hashtable par) {
		Hashtable htxt = new Hashtable();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

		String data = ISASUtil.getValoreStringa(par, "data_inizio");
		htxt.put("#d1#", recuperaDataItaliano(data));
		data = ISASUtil.getValoreStringa(par, "data_fine");
		htxt.put("#d2#", recuperaDataItaliano(data));

		String codModalita = ISASUtil.getValoreStringa(par, "inviato");
		String descrModalita = "";
		if (ISASUtil.valida(codModalita)) {
			descrModalita = recuperaDescrizioneModalita(dbc, codModalita);
			if (ISASUtil.valida(descrModalita)) {
				descrModalita = ": " + descrModalita;
			}
		}
		htxt.put("#tipologia#", descrModalita);

		String linea = "";
		String valore = "";
		if (!nessunaDivisione) {
			String codZona = ISASUtil.getValoreStringa(par, "zona");
			if (ISASUtil.valida(codZona)) {
				String descrZona = recuperaDescrizioneZona(dbc, codZona);
				if (ISASUtil.valida(descrZona)) {
					linea += "Zona: " + descrZona + " ";
				}
			}
			String codDistretto = ISASUtil.getValoreStringa(par, "distretto");
			if (ISASUtil.valida(codDistretto)) {
				String descrDistretto = recuperaDescrizioneDistretto(dbc, codDistretto);
				if (ISASUtil.valida(descrDistretto)) {
					linea += "Distretto: " + descrDistretto + " ";
				}
			}

			String raggruppamento = ISASUtil.getValoreStringa(par, "ragg");
			if (ISASUtil.valida(raggruppamento)) {
				if (raggruppamento.equals(CODICE_TIPO_COMUNE)) {
					valore = "Comune ";
					String codComune = ISASUtil.getValoreStringa(par, "pca");
					if (ISASUtil.valida(codComune)) {
						String descrComune = recuperaDescrizioneComune(dbc, codComune);
						if (ISASUtil.valida(descrComune)) {
							valore += descrComune + " ";
						}
					}
				} else if (raggruppamento.equals(CODICE_TIPO_AREA_DISTRETTUALE)) {
					valore = "Area distrettuale";
					String codAreaDis = ISASUtil.getValoreStringa(par, "pca");
					if (ISASUtil.valida(codAreaDis)) {
						String descrAreaDistret = recuperaDescrizioneAreaDistrettuale(dbc, codAreaDis);
						if (ISASUtil.valida(descrAreaDistret)) {
							valore += ": " + descrAreaDistret + " ";
						}
					}
				}
				//M.Minerba per Pistoia 04/02/2013
				else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
					valore = "Presidio";
					String codPresidio = ISASUtil.getValoreStringa(par, "pca");
					if (ISASUtil.valida(codPresidio)) {
						String descrPresidio = recuperaDescrizionePresidio(dbc, codPresidio);
						if (ISASUtil.valida(descrPresidio)) {
							valore += ": " + descrPresidio + " ";
						}
					}
				}
				linea += "Raggruppamento: " + valore;
			}
		} else {
			linea = "NESSUNA DIVISIONE";
		}
		htxt.put("#filtri#", linea);
		stampaDoc("layout", htxt);
		md.writeSostituisci("layout", htxt);
	}

	private String recuperaDescrizioneModalita(ISASConnection dbc, String codModalita) {
		String query = "select * from motivo_s where codice = '" + codModalita + "' ";
		String descrizione = "";
		ISASRecord dbrMotivoS = getRecord(dbc, query);
		if (dbrMotivoS != null) {
			descrizione = ISASUtil.getValoreStringa(dbrMotivoS, "descrizione");
		}

		return descrizione;
	}

	private String recuperaDescrizioneDistretto(ISASConnection dbc, String codDistretto) {
		String descrizioneDistretto = "";
		String query = " Select * from distretti WHERE cod_distr = '" + codDistretto + "' ";
		ISASRecord dbrDistretto = getRecord(dbc, query);
		descrizioneDistretto = ISASUtil.getValoreStringa(dbrDistretto, "des_distr");
		return descrizioneDistretto;
	}

	private String recuperaDescrizioneAreaDistrettuale(ISASConnection dbc, String codAreaDis) {
		String descrizioneComune = "";
		String query = "SELECT * FROM areadis WHERE codice = '" + codAreaDis + "' ";
		ISASRecord dbrComune = getRecord(dbc, query);
		descrizioneComune = ISASUtil.getValoreStringa(dbrComune, "descrizione");
		return descrizioneComune;
	}

	private String recuperaDescrizioneComune(ISASConnection dbc, String codComune) {
		String descrizioneComune = "";
		String query = " SELECT * FROM comuni  WHERE codice = '" + codComune + "' ";
		ISASRecord dbrDistretto = getRecord(dbc, query);
		descrizioneComune = ISASUtil.getValoreStringa(dbrDistretto, "descrizione");
		return descrizioneComune;
	}
	//M.Minerba per Pistoia 04/02/2013
	private String recuperaDescrizionePresidio(ISASConnection dbc, String codPresidio) {
		String descrizionePresidio = "";
		String query = " SELECT * FROM presidi  WHERE codpres = '" + codPresidio + "' ";
		ISASRecord dbrPresidio = getRecord(dbc, query);
		descrizionePresidio = ISASUtil.getValoreStringa(dbrPresidio, "despres");
		return descrizionePresidio;
	}

	private String recuperaDescrizioneZona(ISASConnection dbc, String codZona) {
		String descrizioneZona = "";
		String query = " select * from zone where codice_zona = '" + codZona + "' ";
		ISASRecord dbrZone = getRecord(dbc, query);
		descrizioneZona = ISASUtil.getValoreStringa(dbrZone, "descrizione_zona");
		return descrizioneZona;
	}

	private ISASRecord getRecord(ISASConnection dbc, String query) {
		String punto = MIONOME + "getRecord ";
		ISASRecord dbrRecord = null;
		//		stampa(punto + "\n Query>" + query + "<");
		try {
			dbrRecord = dbc.readRecord(query);
		} catch (Exception e) {
			e.printStackTrace();
			stampa(punto + "\n Errore in recupero record>" + query + "<\n");
		}

		return dbrRecord;
	}

	private Object recuperaDataItaliano(String data) {
		String punto = MIONOME + "recuperaDataItaliano ";
		String dataFormat = "";
		try {
			if (ISASUtil.valida(data) && data.length() >= 10) {
				dataFormat = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
			}
		} catch (Exception e) {
			e.printStackTrace();
			stampa(punto + " Errore nel recuperare le data>" + data + "<");
		}
		return dataFormat;
	}

}

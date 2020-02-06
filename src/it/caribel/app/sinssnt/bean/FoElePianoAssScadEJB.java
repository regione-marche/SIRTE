package it.caribel.app.sinssnt.bean;

//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// elisa b 29/09/11
// ==========================================================================

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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class FoElePianoAssScadEJB extends SINSSNTConnectionEJB {

	private static final String MIONOME = "48-FoElePianoAssScadEJB.";
	private static final String PIANI_IN_SCADENZA = "I";
	private static final String PIANI_SCADUTI = "S";

	private static final String CODICE_TIPO_AREA_DISTRETTUALE = "A";
	private static final String CODICE_TIPO_COMUNE = "C";
	private static final String CODICE_TIPO_PRESIDIO = "P";//M.Minerba 25/02/2013
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO = "D";
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA = "R";
	private boolean nessunaDivisione = false;
	private static final int STAMPA_ZONA = 1;
	private static final int STAMPA_DISTRETTO = 2;
	private static final int STAMPA_COMUNE = 3;

	public FoElePianoAssScadEJB() {
	}

	private void preparaLayout(mergeDocument doc, ISASConnection dbc, Hashtable par) throws Exception {
		Hashtable htxt = new Hashtable();
		String tipoPiano = ISASUtil.getValoreStringa(par, "tipo_piano");
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);

			String percorso = doc.getPath();
			if (percorso != null) {
				htxt.put("#percorso#", percorso);
			}
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

		String data = ISASUtil.getValoreStringa(par, "d1");
		htxt.put("#d1#", recuperaDataItaliano(data));
		data = ISASUtil.getValoreStringa(par, "d2");
		htxt.put("#d2#", recuperaDataItaliano(data));

		if (tipoPiano.equals(PIANI_IN_SCADENZA))
			htxt.put("#tipologia#", " IN SCADENZA ");
		else
			htxt.put("#tipologia#", " SCADUTI ");

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
				//M.Minerba 25/02/2013 per Pistoia
				else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
					valore = "Presidio";
					String codPresidio = ISASUtil.getValoreStringa(par, "pca");
					if (ISASUtil.valida(codPresidio)) {
						String descrPresidio = recuperaDescrizionePresidio(dbc, codPresidio);
						if (ISASUtil.valida(descrPresidio)) {
							valore += ": " + descrPresidio + " ";
						}
					}
				}//fine M.Minerba 25/02/2013 per Pistoia
				linea += "Raggruppamento: " + valore;
			}
		} else {
			linea = "NESSUNA DIVISIONE";
		}
		htxt.put("#filtri#", linea);
		doc.writeSostituisci("layout", htxt);
	}
	//M.Minerba 25/02/2013 per Pistoia
	private String recuperaDescrizionePresidio(ISASConnection dbc, String codPresidio) {
		String descrizionePresidio = "";
		String query = "SELECT * FROM presidi WHERE codpres = '" + codPresidio + "' ";
		ISASRecord dbrPresidio = getRecord(dbc, query);
		descrizionePresidio = ISASUtil.getValoreStringa(dbrPresidio, "despres");
		return descrizionePresidio;
	}//fine M.Minerba 25/02/2013 per Pistoia
	
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

	private String recuperaDescrizioneDistretto(ISASConnection dbc, String codDistretto) {
		String descrizioneDistretto = "";
		String query = " Select * from distretti WHERE cod_distr = '" + codDistretto + "' ";
		ISASRecord dbrDistretto = getRecord(dbc, query);
		descrizioneDistretto = ISASUtil.getValoreStringa(dbrDistretto, "des_distr");
		return descrizioneDistretto;
	}

	private String recuperaDescrizioneZona(ISASConnection dbc, String codZona) {
		String descrizioneZona = "";
		String query = " select * from zone where codice_zona = '" + codZona + "' ";
		ISASRecord dbrZone = getRecord(dbc, query);
		descrizioneZona = ISASUtil.getValoreStringa(dbrZone, "descrizione_zona");
		return descrizioneZona;
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
		int totAreaDisPianiAssistiti = 0;
		int totAreaDisAssistiti = 0;

		int totDistrettoPianiAssistiti = 0;
		int totDistrettoAssistiti = 0;

		int totZonaPianiAssistiti = 0;
		int totZonaAssistiti = 0;

		Hashtable prtDati = new Hashtable();
		Hashtable numeroAssistiti = new Hashtable();
		int numeroPianiPerAreaDis = 0;
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
				}
				//M.Minerba 25/02/2013 per Pistoia
				else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {

					descrPace = "Presidio";
				}
			}//fine M.Minerba 25/02/2013 per Pistoia

			hZ.put("#pace#", descrPace);
			doc.writeSostituisci("iniziotabConclusioni-html", hZ);

			while (dbcur.next()) {
				ISASRecord dbr = dbcur.getRecord();
				//				stampa(punto + "dati che esamino>" + (dbr != null ? dbr.getHashtable() + "" : "Non ho dati"));
				zona = ISASUtil.getValoreStringa(dbr, "cod_zona");
				distretto = ISASUtil.getValoreStringa(dbr, "cod_distretto");
				areadis = ISASUtil.getValoreStringa(dbr, "codice");

				if ((!zona.equals(zonaOld)) || (!distretto.equals(distrettoOld)) || (!areadis.equals(areadisOld))) {
					if (!zonaOld.equals("")) {
						doc.write("finetabConclusioni");
						if ((!areadis.equals(areadisOld))) {

							totAreaDisPianiAssistiti = numeroPianiPerAreaDis;
							totAreaDisAssistiti = numeroAssistiti.size();

							descrizioneEtichetta = descrPace + ": " + descrizione;
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#tot_piani_ass#", totAreaDisPianiAssistiti + "");
							prtDati.put("#numero_assist#", totAreaDisAssistiti + "");
							//							doc.writeSostituisci("totali", prtDati);
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", descrizione);
							stampaParziali(doc, prtDati, hZ, STAMPA_COMUNE);
							stampaParzialiHtml(doc, prtDati, hZ);

							totDistrettoPianiAssistiti += totAreaDisPianiAssistiti;
							totDistrettoAssistiti += totAreaDisAssistiti;

							totAreaDisPianiAssistiti = 0;
							totAreaDisAssistiti = 0;
							numeroPianiPerAreaDis = 0;
							numeroAssistiti.clear();
						}

						if ((!distretto.equals(distrettoOld))) {
							descrizioneEtichetta = "Distretto: " + ISASUtil.getValoreStringa(hZ, "#des_distr#");
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#tot_piani_ass#", totDistrettoPianiAssistiti + "");
							prtDati.put("#numero_assist#", totDistrettoAssistiti + "");
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#des_distr#"));
							stampaParziali(doc, prtDati, hZ, STAMPA_DISTRETTO);
							//							doc.writeSostituisci("totali", prtDati);

							totZonaPianiAssistiti += totDistrettoPianiAssistiti;
							totZonaAssistiti += totDistrettoAssistiti;

							totDistrettoAssistiti = 0;
							totDistrettoPianiAssistiti = 0;
						}

						if ((!zona.equals(zonaOld))) {
							descrizioneEtichetta = "Zona: " + ISASUtil.getValoreStringa(hZ, "#descrizione_zona#");
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#tot_piani_ass#", totZonaPianiAssistiti + "");
							prtDati.put("#numero_assist#", totZonaAssistiti + "");
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#descrizione_zona#"));
							stampaParziali(doc, prtDati, hZ, STAMPA_ZONA);
							//							doc.writeSostituisci("totali", prtDati);
							totZonaPianiAssistiti = 0;
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
						//M.Minerba 25/02/2013 per Pistoia 
						else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
							descrPace = "Presidio ";
							if (ISASUtil.valida(codice)) {
								descrizione = recuperaDescrizionePresidio(dbc, codice);
							}
						}//fine M.Minerba 25/02/2013 per Pistoia 
					}

					hZ.put("#descrizione#", descrizione);
					hZ.put("#pace#", descrPace);
					if (!nessunaDivisione) {
						doc.writeSostituisci("zona", (Hashtable) hZ.clone());
					}
					zonaOld = zona;
					distrettoOld = distretto;
					areadisOld = areadis;

					doc.write("iniziotabConclusioni");
				}
				String cartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
				Hashtable htAss = new Hashtable();

				htAss.putAll(hZ);

				htAss.put("#n_cartella#", cartella);
				assistito = ISASUtil.getValoreStringa(dbr, "cognome");
				assistito += (ISASUtil.valida(assistito) ? " " : "") + ISASUtil.getValoreStringa(dbr, "nome");
				htAss.put("#assistito#", assistito);
				htAss.put("#dt_inizio#", getDateField(dbr, "pa_data"));
				htAss.put("#dt_fine#", getDateField(dbr, "pa_data_chiusura"));

				prtDati = recuperaAccessiPrestazioneAssistito(dbc, dbr, htAss, doc);

				numeroAssistiti.put(cartella, "");
				numeroPianiPerAreaDis++;

			}
			//			doc.write("finetabConclusioni");

			doc.write("finetabConclusioni");
			doc.write("finetabConclusioni-hmtl");

			totAreaDisPianiAssistiti = numeroPianiPerAreaDis;
			totAreaDisAssistiti = numeroAssistiti.size();

			descrizioneEtichetta = descrPace + ": " + descrizione;
			prtDati.put("#descrPace#", descrizioneEtichetta);
			prtDati.put("#tot_piani_ass#", totAreaDisPianiAssistiti + "");
			prtDati.put("#numero_assist#", totAreaDisAssistiti + "");
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", descrizione);
			stampaParziali(doc, prtDati, hZ, STAMPA_COMUNE);
			stampaParzialiHtml(doc, prtDati, hZ);

			//			doc.writeSostituisci("totali", prtDati);
			totDistrettoPianiAssistiti += totAreaDisPianiAssistiti;
			totDistrettoAssistiti += totAreaDisAssistiti;
			descrizioneEtichetta = "Distretto: " + ISASUtil.getValoreStringa(hZ, "#des_distr#");
			prtDati.put("#descrPace#", descrizioneEtichetta);
			prtDati.put("#tot_piani_ass#", totDistrettoPianiAssistiti + "");
			prtDati.put("#numero_assist#", totDistrettoAssistiti + "");
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#des_distr#"));
			stampaParziali(doc, prtDati, hZ, STAMPA_DISTRETTO);
			//			doc.writeSostituisci("totali", prtDati);

			totZonaPianiAssistiti += totDistrettoPianiAssistiti;
			totZonaAssistiti += totDistrettoAssistiti;

			descrizioneEtichetta = "Zona: " + ISASUtil.getValoreStringa(hZ, "#descrizione_zona#");
			prtDati.put("#descrPace#", descrizioneEtichetta);
			prtDati.put("#tot_piani_ass#", totZonaPianiAssistiti + "");
			prtDati.put("#numero_assist#", totZonaAssistiti + "");
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#descrizione_zona#"));
			stampaParziali(doc, prtDati, hZ, STAMPA_ZONA);
			//			doc.writeSostituisci("totali", prtDati);

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

	private void stampaParzialiHtml(mergeDocument doc, Hashtable prtDati, Hashtable zone) {
		stampaParziale(doc, prtDati, zone, true, 0);
	}

	private void stampaParziali(mergeDocument doc, Hashtable prtDati, Hashtable zone, int tipoStampa) {
		stampaParziale(doc, prtDati, zone, false, tipoStampa);
	}

	private void stampaParziale(mergeDocument doc, Hashtable prtDati, Hashtable zone, boolean html, int tipoStampa) {
		String punto = MIONOME + "stampaParziale ";
		String sezione = "";
		boolean stampaSeparatore = false;
		if (!nessunaDivisione) {
			if (html) {
				sezione = "_html";
			}
			prtDati.putAll(zone);

			prtDati.put("#descrPace_sint#", "");
			prtDati.put("#tot_piani_ass_pace#", "");
			prtDati.put("#numero_assist_pace#", "");

			prtDati.put("#tot_piani_ass_distr#", "");
			prtDati.put("#numero_assist_distr#", "");

			prtDati.put("#tot_piani_ass_zona#", "");
			prtDati.put("#numero_assist_zona#", "");
			prtDati.put("#descrizioneZonaDistretto#", "");

			switch (tipoStampa) {
			case STAMPA_ZONA:
				//				prtDati.put("#descrizioneZonaDistretto#", "Totale della zona: " + ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
//				prtDati.put("#descrizioneZonaDistretto#", ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
				prtDati.put("#descrizioneZonaDistretto#", ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
				prtDati.put("#tot_piani_ass_zona#", ISASUtil.getValoreStringa(prtDati, "#tot_piani_ass#"));
				prtDati.put("#numero_assist_zona#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				stampaSeparatore = true;
				break;
			case STAMPA_DISTRETTO:
				//				prtDati.put("#descrizioneZonaDistretto#", "Totale del distretto: "
				//								+ ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
//				prtDati.put("#descrizioneZonaDistretto#", ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
				prtDati.put("#descrizioneZonaDistretto#", ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
				prtDati.put("#tot_piani_ass_distr#", ISASUtil.getValoreStringa(prtDati, "#tot_piani_ass#"));
				prtDati.put("#numero_assist_distr#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				stampaSeparatore = true;
				break;
			case STAMPA_COMUNE:
				//				prtDati.put("#descrPace_sint#", "Totale " + ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
				prtDati.put("#descrPace_sint#", ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
				prtDati.put("#numero_assist_pace#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
				prtDati.put("#tot_piani_ass_pace#", ISASUtil.getValoreStringa(prtDati, "#tot_piani_ass#"));
				break;
			default:
				break;
			}
			//			stampaDoc("totali" + sezione, prtDati);
			doc.writeSostituisci("totali" + sezione, (Hashtable) prtDati.clone());
			if (stampaSeparatore) {
				doc.write("separatore");
			}
		} else {
//			if (tipoStampa == STAMPA_ZONA) {
//				prtDati.put("#descrizioneZonaDistretto#", ISASUtil.getValoreStringa(prtDati, "#descrPace_html#"));
//				prtDati.put("#tot_piani_ass_zona#", ISASUtil.getValoreStringa(prtDati, "#tot_piani_ass#"));
//				prtDati.put("#numero_assist_zona#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
//			}
		}

	}

	private void stampaParziale_(mergeDocument doc, Hashtable prtDati, Hashtable zone, boolean html) {
		String punto = MIONOME + "stampaParziale ";
		String sezione = "";
		if (html) {
			sezione = "_html";
		}
		prtDati.putAll(zone);
		doc.writeSostituisci("totali" + sezione, (Hashtable) prtDati.clone());
	}

	private Hashtable recuperaAccessiPrestazioneAssistito(ISASConnection dbc, ISASRecord dbr, Hashtable htAss, mergeDocument doc) {
		String punto = MIONOME + "recuperaAccessiPrestazioneAssistito ";
		Hashtable prtDati = new Hashtable();

		String query = getFiltroAccessiPrestazione(dbc, dbr);
		stampaQuery(punto, query);

		try {
			ISASCursor dbrCurPianoAccessi = dbc.startCursor(query);
			stampa(punto + "\n Elementi recuperati: "
					+ (dbrCurPianoAccessi != null ? dbrCurPianoAccessi.getDimension() + "" : " Non ho dati "));
			int prestazioniProcessate = 0;

			while (dbrCurPianoAccessi.next()) {
				ISASRecord dbrPianoAccessi = dbrCurPianoAccessi.getRecord();
				prtDati.put("#n_cartella#", "");
				prtDati.put("#assistito#", "");
				prtDati.put("#dt_inizio#", "");
				prtDati.put("#dt_fine#", "");
				prtDati.put("#descrizione_zona#", "");
				prtDati.put("#des_distr#", "");
				prtDati.put("#descrizione#", "");

				prtDati.put("#n_cartella_html#", ISASUtil.getValoreStringa(htAss, "#n_cartella#"));
				prtDati.put("#assistito_html#", ISASUtil.getValoreStringa(htAss, "#assistito#"));
				prtDati.put("#dt_inizio_html#", ISASUtil.getValoreStringa(htAss, "#dt_inizio#"));
				prtDati.put("#dt_fine_html#", ISASUtil.getValoreStringa(htAss, "#dt_fine#"));
				prtDati.put("#descrizione_zona_html#", ISASUtil.getValoreStringa(htAss, "#descrizione_zona#"));
				prtDati.put("#des_distr_html#", ISASUtil.getValoreStringa(htAss, "#des_distr#"));
				prtDati.put("#descrizione_html#", ISASUtil.getValoreStringa(htAss, "#descrizione#"));

				if (prestazioniProcessate == 0) {
					prtDati.putAll(htAss);
				}
				prestazioniProcessate++;

				String codPrestazione = ISASUtil.getValoreStringa(dbrPianoAccessi, "pi_prest_cod");
				String codOperatore = ISASUtil.getValoreStringa(dbrPianoAccessi, "pi_op_esecutore");
				String codFrequenza = ISASUtil.getValoreStringa(dbrPianoAccessi, "pi_freq");

				prtDati.put("#codice#", codPrestazione);
				prtDati.put("#prestazione#", recuperaDescrizionePrestazione(dbc, codPrestazione));
				prtDati.put("#op_esecutore#", recuperaDescrzioneOperatore(dbc, codOperatore));
				prtDati.put("#dt_inizio_pretaz#", getDateField(dbrPianoAccessi, "pi_data_inizio"));
				prtDati.put("#frequenza_pretaz#", recuperaDescrizioneFrequenza(dbc, codFrequenza));
				prtDati.put("#dt_fine_pretaz#", getDateField(dbrPianoAccessi, "pi_data_fine"));
				doc.writeSostituisci("tabellaConclusioni", prtDati);
			}
			// non ho recuperato prestazioni associate all'assistito 
			if (prestazioniProcessate == 0) {
				prtDati.putAll(htAss);

				prtDati.put("#n_cartella_html#", ISASUtil.getValoreStringa(htAss, "#n_cartella#"));
				prtDati.put("#assistito_html#", ISASUtil.getValoreStringa(htAss, "#assistito#"));
				prtDati.put("#dt_inizio_html#", ISASUtil.getValoreStringa(htAss, "#dt_inizio#"));
				prtDati.put("#dt_fine_html#", ISASUtil.getValoreStringa(htAss, "#dt_fine#"));
				prtDati.put("#descrizione_zona_html#", ISASUtil.getValoreStringa(htAss, "#descrizione_zona#"));
				prtDati.put("#des_distr_html#", ISASUtil.getValoreStringa(htAss, "#des_distr#"));
				prtDati.put("#descrizione_html#", ISASUtil.getValoreStringa(htAss, "#descrizione#"));

				prtDati.put("#codice#", "");
				prtDati.put("#prestazione#", "");
				prtDati.put("#op_esecutore#", "");
				prtDati.put("#dt_inizio_pretaz#", "");
				prtDati.put("#frequenza_pretaz#", "");
				prtDati.put("#dt_fine_pretaz#", "");
				doc.writeSostituisci("tabellaConclusioni", prtDati);
			}

		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		}
		return prtDati;
	}

	private Object recuperaDescrizionePrestazione(ISASConnection dbc, String codPrestazione) {
		String descrizione = "";
		String query = "select * from prestaz where prest_cod = '" + codPrestazione + "' ";
		ISASRecord dbrPrestaz = getRecord(dbc, query);
		descrizione = ISASUtil.getValoreStringa(dbrPrestaz, "prest_des");
		return descrizione;
	}

	private Object recuperaDescrzioneOperatore(ISASConnection dbc, String codOperatore) {
		String descrizione = "";
		String query = "select * from operatori where codice = '" + codOperatore + "' ";
		ISASRecord dbrOperatori = getRecord(dbc, query);
		descrizione = ISASUtil.getValoreStringa(dbrOperatori, "cognome");
		descrizione += (ISASUtil.valida(descrizione) ? " " : "") + ISASUtil.getValoreStringa(dbrOperatori, "nome");

		return descrizione;
	}

	private Object recuperaDescrizioneFrequenza(ISASConnection dbc, String codFrequenza) {
		String descrizione = "";
		String query = " select * from tab_voci where tab_cod = 'FREQAC' AND TAB_VAL = '" + codFrequenza + "' ";
		ISASRecord dbrTabVoci = getRecord(dbc, query);
		descrizione = ISASUtil.getValoreStringa(dbrTabVoci, "tab_descrizione");

		return descrizione;
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

	private String getFiltroAccessiPrestazione(ISASConnection dbc, ISASRecord dbr) {
		String punto = MIONOME + "getFiltroAccessiPrestazione ";
		String operatore = ISASUtil.getValoreStringa(dbr, "pa_tipo_oper");
		String nCartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
		String nProgetto = ISASUtil.getValoreStringa(dbr, "n_progetto");
		String codObbiettivo = ISASUtil.getValoreStringa(dbr, "cod_obbiettivo");
		String nIntervento = ISASUtil.getValoreStringa(dbr, "n_intervento");
		String data = ISASUtil.getValoreStringa(dbr, "pa_data");

		String query = " SELECT * FROM piano_accessi WHERE pa_tipo_oper = '" + operatore + "' AND n_cartella = " + nCartella
				+ " AND n_progetto = " + nProgetto + " AND cod_obbiettivo = '" + codObbiettivo + "' AND n_intervento = " + nIntervento
				+ " AND pa_data = " + formatDate(dbc, data) + " ORDER BY pi_prog ";

		return query;
	}

	//	private String getDateField(ISASRecord dbr, String f) {
	//		try {
	//			if (dbr.get(f) == null)
	//				return "";
	//			String d = ((java.sql.Date) dbr.get(f)).toString();
	//			d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/" + d.substring(0, 4);
	//			return d;
	//		} catch (Exception e) {
	//			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
	//			return "";
	//		}
	//	}

	private String getDateField(ISASRecord dbr, String f) {
		String data = "";
		try {
			if (dbr.get(f) == null)
				return "";
			String d = ((java.sql.Date) dbr.get(f)).toString();
			data = recuperaDataItaliano(d);
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
		}
		return data;
	}

	private String recuperaDataItaliano(String data) {
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

	/**
	 * In caso di stampa su foglio excel si deve stamapre un'unica tabella in 
	 * cui le informazioni su zona/distretto e area dis sono scritte su 3 
	 *  colonne aggiuntive
	 * @param doc
	 * @param dbcur
	 * @param par
	 * @param dbc
	 * @throws Exception
	 */
	private void preparaBodyFoglioCalcolo(mergeDocument doc, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {

		String zona = "";
		String distretto = "";
		String areadis = "";

		doc.write("iniziotab");

		while (dbcur.next()) {

			ISASRecord dbr = dbcur.getRecord();

			zona = dbr.get("cod_zona").toString();
			distretto = dbr.get("cod_distretto").toString();
			areadis = dbr.get("codice").toString();

			Hashtable h = new Hashtable();
			h.put("#descrizione_zona#", ISASUtil.getDecode(dbc, "zone", "codice_zona", zona, "descrizione_zona"));
			h.put("#des_distr#", ISASUtil.getDecode(dbc, "distretti", "cod_distr", distretto, "des_distr"));
			h.put("#descrizione#", ISASUtil.getDecode(dbc, "areadis", "codice", areadis, "descrizione"));
			String cart = " ";
			h.put("#pa_operatore#", ISASUtil.getDecode(dbc, "operatori", "codice", dbr.get("pa_operatore").toString(),
					"nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nomeope"));
			if (dbr.get("n_cartella") != null)
				cart = ((Integer) dbr.get("n_cartella")).toString();
			h.put("#n_cartella#", cart);

			String data = " ";

			if (dbr.get("pa_data") != null) {
				data = ((java.sql.Date) dbr.get("pa_data")).toString();
				data = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
			}
			h.put("#pa_data#", data);
			data = "";
			if (dbr.get("pa_data_chiusura") != null) {
				data = ((java.sql.Date) dbr.get("pa_data_chiusura")).toString();
				data = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
			}
			h.put("#pa_data_chiusura#", data);

			h.put("#nome#", dbr.get("nome").toString());
			h.put("#cognome#", dbr.get("cognome").toString());

			String complessita = "";
			if (dbr.get("skpa_complessita") != null && !((String) dbr.get("skpa_complessita")).equals("")) {
				complessita = ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", "COMPLASS", (String) dbr.get("skpa_complessita"),
						"tab_descrizione");
			}
			h.put("#complessita#", complessita);
			//System.out.println("doc.writeSostituisci tabella");
			doc.writeSostituisci("tabella", h);
		}

		doc.write("finetab");
	}

	public byte[] query_report(String utente, String passwd, Hashtable par, mergeDocument doc) throws SQLException {
		String punto = MIONOME + "query_report ";
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		byte[] rit;
		stampa(punto + "Inizio con dati>" + par + "< utente>" + utente + "<\n");
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			settaDataFine(par);

			String query = getFiltro(dbc, par);

			stampaQuery(punto, query);
			dbcur = dbc.startCursor(query);
			preparaLayout(doc, dbc, par);
			stampa(punto + "ho recuperato elementi:" + (dbcur != null ? dbcur.getDimension() + "" : " NON ho recuperato nulla "));
			if (dbcur == null) {
				doc.write("messaggio");
				doc.write("finale");
			} else {
				if (dbcur.getDimension() <= 0) {
					doc.write("messaggio");
					doc.write("finale");
				} else {
					preparaBody(doc, dbcur, par, dbc);
					doc.write("finale");
				}
			}
			dbcur.close();
			doc.close();
			stampa(punto + " \n Fine esecuzione programma\n");
			rit = (byte[]) doc.get();
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_assprogass()  ");
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

	private void settaDataFine(Hashtable par) {
		String punto = MIONOME + "settaDataFine ";
		String dtInizio = ISASUtil.getValoreStringa(par, "d1");

		String tipoPiano = ISASUtil.getValoreStringa(par, "tipo_piano");
		String valNumGiorni = ISASUtil.getValoreStringa(par, "numGiorni");
		int numGiorni = getIntero(valNumGiorni);

		String dataFine = "";
		if (ISASUtil.valida(tipoPiano) && tipoPiano.equalsIgnoreCase(PIANI_IN_SCADENZA)) {
			dataFine = aggiungiGiorno(dtInizio, numGiorni);
			par.put("d2", dataFine);
		} else {
			stampa(punto + "\n non calcolo la data fine, in quanto mi � arrivata dal client");
		}
	}

	private void stampaQuery(String punto, String query) {
		System.out.println(punto + " Query>" + query + "");

	}

	private String getFiltro(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getFiltro ";
		ServerUtility su = new ServerUtility();

		String dtInizio = ISASUtil.getValoreStringa(par, "d1");
		String dataFine = ISASUtil.getValoreStringa(par, "d2");
		String zona = ISASUtil.getValoreStringa(par, "zona");
		String distretto = ISASUtil.getValoreStringa(par, "distretto");
		String pca = ISASUtil.getValoreStringa(par, "pca");
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
        
		//		query.append(" SELECT p.*, nome, cognome, u.cod_zona, u.cod_distretto, u.des_zona descrizione_zona, u.codice, u.tipo");
		query.append(" SELECT p.*, nome, cognome, " + selZona + selDistretto + selComune + " u.tipo");
		//M.Minerba 25/02/2013 aggiunta della vista contsan
		query.append(" FROM piano_assist p , cartella c, contsan_n r, ");
		query.append(((par.get("socsan") != null && par.get("socsan").equals("01")) ? "ubicazioni_n_soc" : "ubicazioni_n"));
		//M.Minerba 25/02/2013 aggiunta della join r.n_cartella=p.n_cartella, p.pa_tipo_oper=r.tipo_operatore, p.n_progetto=r.n_contatto
		query.append(" u, anagra_c ac WHERE ac.n_cartella = c.n_cartella AND r.n_cartella=p.n_cartella");
		query.append(" AND p.pa_tipo_oper=r.tipo_operatore AND p.n_progetto=r.n_contatto");
		query.append(" AND ac.data_variazione IN ( SELECT MAX (data_variazione) FROM anagra_c WHERE ac.n_cartella=anagra_c.n_cartella) ");
		condWhere = " and  ac.n_cartella = p.n_cartella ";

		if (ISASUtil.valida(dtInizio) && dtInizio.length() >= 10) {
			query.append(" AND p.pa_data_chiusura >= " + formatDate(dbc, dtInizio));
		}

		if (ISASUtil.valida(dataFine) && dataFine.length() >= 10) {
			query.append(" AND p.pa_data_chiusura <= " + formatDate(dbc, dataFine));
		}

		//		String ragg = (String) par.get("ragg");
		//		condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
		//		if (ragg != null && ragg.equals("C")) {
		//			condWhere += " AND (( (ac.dom_citta IS NOT NULL OR ac.dom_citta <> '')" + " AND u.codice=ac.dom_citta)"
		//					+ " OR ( (ac.dom_citta IS NULL OR ac.dom_citta = '') " + " AND u.codice=ac.citta))";
		//		} else if (ragg != null && ragg.equals("A")) {
		//			condWhere += " AND (( (ac.dom_areadis IS NOT NULL OR ac.dom_areadis <> '')" + " AND u.codice=ac.dom_areadis)"
		//					+ " OR ( (ac.dom_areadis IS NULL OR ac.dom_areadis = '') " + " AND u.codice=ac.areadis))";
		//		}
		//
		//		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, zona);
		//		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, distretto);
		//		condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, pca);
		//
		//		query.append(condWhere);

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
			//M.Minerba 25/02/2013 per Pistoia
			else if (ragg.equals("P")) {
				query.append(" AND u.codice=r.cod_presidio");
			}//fine M.Minerba 25/02/2013 per Pistoia
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				query.append(" AND u.codice=ac.dom_citta ");
			else if (ragg.equals("A"))
				query.append(" AND u.codice=ac.dom_areadis ");
			//M.Minerba 25/02/2013 per Pistoia
			else if (ragg.equals("P")) {
				query.append(" AND u.codice=r.cod_presidio");
			}//fine M.Minerba 25/02/2013 per Pistoia
		} else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				query.append(" AND u.codice=ac.citta");
			else if (ragg.equals("A"))
				query.append(" AND u.codice=ac.areadis ");
			//M.Minerba 25/02/2013 per Pistoia
			else if (ragg.equals("P")) {
				query.append(" AND u.codice=r.cod_presidio");
			}//fine M.Minerba 25/02/2013 per Pistoia
		}
		query.append(" ORDER BY cod_zona,u.cod_distretto,u.codice, cognome, nome, pa_data_chiusura");

		stampaQuery(punto, query.toString());

		return query.toString();
	}

	//	private String recuperaDataFine(String dtInizio, String numGiorni) {
	//		String punto = MIONOME + "";
	//		String dataFine = "";
	//
	//		try {
	//			Calendar calDataInizioPeriodo = recuperaGiorno(dtInizio);
	//
	//			dataFine = "";
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//			stampa(punto + "");
	//		}
	//		return dataFine;
	//	}

	private String aggiungiGiorno(String dataInizio, int numeroGiorniDaAggiungere) {
		String punto = MIONOME + "aggiungiGiorno ";
		String dataJdbc = "";

		int giorno = -1, mese = -1, anno = -1;

		int pos = dataInizio.indexOf("-");
		if (pos > 0) {
			anno = getIntero(dataInizio.substring(0, 4));
		}
		String val = dataInizio.substring(pos + 1);
		pos = val.indexOf("-");
		if (pos > 0) {
			mese = getIntero(val.substring(0, pos));
			giorno = getIntero(val.substring(pos + 1));
		}

		stampa(punto + "Dati recuperati giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<");
		mese = (mese > 0 ? mese - 1 : mese);

		stampa(punto + " Dati che setto giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<");
		Calendar calender = new GregorianCalendar(anno, mese, giorno);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		giorno = calender.get(Calendar.DAY_OF_MONTH);

		stampa(punto + " Giorno inizio calendar >" + giorno + "< mese>" + mese + "< anno>" + anno + "<");
		calender = new GregorianCalendar(anno, mese, giorno + numeroGiorniDaAggiungere);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		giorno = calender.get(Calendar.DAY_OF_MONTH);
		mese = mese + 1;

		String valMese = formatta(mese);
		String valGiorno = formatta(giorno);
		stampa(punto + " Giorno dopo che ho aggiunto>" + numeroGiorniDaAggiungere + " giorno" + formatta(giorno) + "< mese>"
				+ formatta(mese) + "< anno>" + anno + "<");
		dataJdbc = anno + "-" + valMese + "-" + valGiorno;
		stampa(punto + " \ndata>" + dataJdbc + "\n");

		return dataJdbc;
	}

	private static String formatta(int numero) {
		String descrizione = "" + numero;
		if (numero <= 9) {
			descrizione = "0" + numero;
		}
		return descrizione;
	}

	private static int getIntero(String numero) {
		int valore = -1;

		try {
			valore = Integer.parseInt(numero);
		} catch (Exception e) {
		}
		return valore;
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

}

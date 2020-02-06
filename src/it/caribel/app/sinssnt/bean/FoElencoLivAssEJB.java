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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

public class FoElencoLivAssEJB extends SINSSNTConnectionEJB {
	private static final String MIONOME = "43-FoElencoLivAssEJB.";
	private static final String CODICE_TIPO_AREA_DISTRETTUALE = "A";
	private static final String CODICE_TIPO_COMUNE = "C";
	private static final String CODICE_TIPO_PRESIDIO = "P";//M.Minerba 28/02/2013
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_DOMICILIO = "D";
	private static final String CODICE_TIPOLOGIA_UBICAZIONE_RESIDENZA = "R";

	private Hashtable decodificaLivello = new Hashtable();
	private static final int STAMPA_ZONA = 1;
	private static final int STAMPA_DISTRETTO = 2;
	private static final int STAMPA_COMUNE = 3;
	private boolean nessunaDivisione = false;

	public FoElencoLivAssEJB() {
	}

	private void preparaLayout(mergeDocument doc, ISASConnection dbc, Hashtable par) throws Exception {
		Hashtable htxt = new Hashtable();

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
				//M.Minerba 28/02/2013 per Pistoia
				else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
					valore = "Presidio";
					String codPresidio = ISASUtil.getValoreStringa(par, "pca");
					if (ISASUtil.valida(codPresidio)) {
						String descrPresidio = recuperaDescrizionePresidio(dbc, codPresidio);
						if (ISASUtil.valida(descrPresidio)) {
							valore += ": " + descrPresidio + " ";
						}
					}
				}//fine M.Minerba 28/02/2013 per Pistoia
				linea += "Raggruppamento: " + valore;
			}
		} else {
			linea = "NESSUNA DIVISIONE";
		}
		htxt.put("#filtri#", linea);

		String livello = ISASUtil.getValoreStringa(par, "liv_ass");
		String filtro1 = "";
		if (ISASUtil.valida(livello)) {
			filtro1 = ISASUtil.getValoreStringa(decodificaLivello, livello);
			if (ISASUtil.valida(filtro1)) {
				filtro1 = "Livello assistenziale: " + filtro1;
			}
		}

		htxt.put("#filtri1#", filtro1);

		doc.writeSostituisci("layout", htxt);
	}
	//M.Minerba 28/02/2013 per Pistoia
	private String recuperaDescrizionePresidio(ISASConnection dbc, String codPresidio) {
		String descrizionePresidio = "";
		String query = "SELECT * FROM presidi WHERE codpres = '" + codPresidio + "' ";
		ISASRecord dbrPresidio = getRecord(dbc, query);
		descrizionePresidio = ISASUtil.getValoreStringa(dbrPresidio, "despres");
		return descrizionePresidio;
	}//fine M.Minerba 28/02/2013 per Pistoia

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
		int totAreaDisAssistiti = 0;

		int totDistrettoAssistiti = 0;

		int totZonaAssistiti = 0;

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
				}//M.Minerba 28/02/2013 per Pistoia
				else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
					descrPace = "Presidio";
				}//fine M.Minerba 28/02/2013 per Pistoia
			}

			hZ.put("#pace#", descrPace);
			doc.writeSostituisci("iniziotabConclusioni-html", hZ);
			Hashtable paceTotaleLivello = new Hashtable();
			Hashtable distrettoTotaleLivello = new Hashtable();
			Hashtable zonaTotaleLivello = new Hashtable();
			Hashtable totaleGenerale = new Hashtable();
			inizializzaTotaleLivello(paceTotaleLivello);

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
							incrementaLivello(paceTotaleLivello, distrettoTotaleLivello);

							totAreaDisAssistiti = numeroAssistiti.size();
							descrizioneEtichetta = descrPace + ": " + descrizione;
							prtDati.put("#descrPace#", descrizioneEtichetta);
							prtDati.put("#numero_assist#", totAreaDisAssistiti + "");
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", descrizione);
							stampaParziali(doc, prtDati, paceTotaleLivello, hZ, STAMPA_COMUNE);
							stampaParzialiHtml(doc, prtDati, paceTotaleLivello, hZ);
							totDistrettoAssistiti += totAreaDisAssistiti;
							totAreaDisAssistiti = 0;
							numeroAssistiti.clear();
							paceTotaleLivello.clear();
						}

						if ((!distretto.equals(distrettoOld))) {

							incrementaLivello(distrettoTotaleLivello, zonaTotaleLivello);
							descrizioneEtichetta = "Distretto: " + ISASUtil.getValoreStringa(hZ, "#des_distr#");
							prtDati.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#des_distr#"));
							prtDati.put("#numero_assist#", totDistrettoAssistiti + "");
							stampaParziali(doc, prtDati, distrettoTotaleLivello, hZ, STAMPA_DISTRETTO);
							totZonaAssistiti += totDistrettoAssistiti;
							totDistrettoAssistiti = 0;
							distrettoTotaleLivello.clear();
						}

						if ((!zona.equals(zonaOld))) {
							descrizioneEtichetta = "Zona: " + ISASUtil.getValoreStringa(hZ, "#descrizione_zona#");
							prtDati.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace#", descrizioneEtichetta);
							hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#descrizione_zona#"));
							prtDati.put("#numero_assist#", totZonaAssistiti + "");

							stampaParziali(doc, prtDati, zonaTotaleLivello, hZ, STAMPA_ZONA);

							totZonaAssistiti = 0;
							incrementaLivello(zonaTotaleLivello, totaleGenerale);
							zonaTotaleLivello.clear();
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
						//M.Minerba 28/02/2013 per Pistoia
						else if (raggruppamento.equals(CODICE_TIPO_PRESIDIO)) {
							descrPace = "Presidio ";
							if (ISASUtil.valida(codice)) {
								descrizione = recuperaDescrizionePresidio(dbc, codice);
							}
						}//fine M.Minerba 28/02/2013 per Pistoia
					}

					hZ.put("#descrizione#", descrizione);
					hZ.put("#pace#", descrPace);
					if (!nessunaDivisione) {
						doc.writeSostituisci("zona", (Hashtable) hZ.clone());
					}
					//					doc.writeSostituisci("zona", (Hashtable) hZ.clone());
					zonaOld = zona;
					distrettoOld = distretto;
					areadisOld = areadis;
					doc.write("iniziotabConclusioni");
				}
				String cartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
				Hashtable htAss = new Hashtable();
				htAss.putAll(hZ);

				htAss.put("#n_cartella#", cartella);
				String select_ass ="select * from cartella where n_cartella = '" + cartella + "' ";
				System.out.println("SELECT_ASS" + select_ass);
				ISASRecord dbrAss = dbc.readRecord(select_ass);
				if (dbrAss!=null){
				
					System.out.println ("COGNOME" + ISASUtil.getValoreStringa(dbr, "cognome") );
					prtDati.put("#assistito#", ISASUtil.getValoreStringa(dbrAss, "cognome") + " " + ISASUtil.getValoreStringa(dbrAss, "nome"));
					prtDati.put("#dt_nascita#", getDateField(dbrAss, "data_nasc"));
				}
					htAss.put("#dt_scala#", getDateField(dbr, "sku_data"));
				
				prtDati.putAll(htAss);
				
				decodificaDati(dbc, dbr, prtDati);
				String codLivello = ISASUtil.getValoreStringa(dbr, "livello");
				//				stampaDoc("tabellaConclusioni", prtDati);
				doc.writeSostituisci("tabellaConclusioni", prtDati);
				numeroAssistiti.put(cartella, "");

				paceTotaleLivello = incrementaLivello(paceTotaleLivello, codLivello);
				//				numeroPianiPerAreaDis++;

			}
			//			doc.write("finetabConclusioni");

			doc.write("finetabConclusioni");
			doc.write("finetabConclusioni-hmtl");

			totAreaDisAssistiti = numeroAssistiti.size();
			incrementaLivello(paceTotaleLivello, distrettoTotaleLivello);

			descrizioneEtichetta = descrPace + ": " + descrizione;
			prtDati.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", descrizione);
			prtDati.put("#numero_assist#", totAreaDisAssistiti + "");
			stampaParziali(doc, prtDati, paceTotaleLivello, hZ, STAMPA_COMUNE);
			stampaParzialiHtml(doc, prtDati, paceTotaleLivello, hZ);
			incrementaLivello(distrettoTotaleLivello, zonaTotaleLivello);
			totDistrettoAssistiti += totAreaDisAssistiti;
			descrizioneEtichetta = "Distretto: " + ISASUtil.getValoreStringa(hZ, "#des_distr#");
			prtDati.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#des_distr#"));
			prtDati.put("#numero_assist#", totDistrettoAssistiti + "");

			stampaParziali(doc, prtDati, distrettoTotaleLivello, hZ, STAMPA_DISTRETTO);
			totZonaAssistiti += totDistrettoAssistiti;
			incrementaLivello(zonaTotaleLivello, totaleGenerale);
			descrizioneEtichetta = "Zona: " + ISASUtil.getValoreStringa(hZ, "#descrizione_zona#");
			prtDati.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace#", descrizioneEtichetta);
			hZ.put("#descrPace_html#", ISASUtil.getValoreStringa(hZ, "#descrizione_zona#"));
			prtDati.put("#numero_assist#", totZonaAssistiti + "");

			stampaParziali(doc, prtDati, zonaTotaleLivello, hZ, STAMPA_ZONA);

			stampaTotaleGenerale(doc, totaleGenerale);

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

	private void stampaTotaleGenerale(mergeDocument doc, Hashtable totale) {
		String punto = MIONOME + "stampaParziali";

		Iterator itr = totale.keySet().iterator();
		int numeroAssistitoLivello = 0;
		String descrizioneLivello = "";
		Hashtable prtDati = new Hashtable();
		doc.write("totale_Sintetico");
		int numeroAssistitiGenerale = 0;
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String value = totale.get(key) + "";
			numeroAssistitoLivello = getIntero(value);
			if (numeroAssistitoLivello > 0) {
				descrizioneLivello = ISASUtil.getValoreStringa(decodificaLivello, key + "");
				prtDati.put("#descrizione_livello#", descrizioneLivello);
				prtDati.put("#numero_assist#", numeroAssistitoLivello + "");
				doc.writeSostituisci("totale_Sintetico_dett", prtDati);
			} else {
				stampa(punto + "\n Non stampato valore key>" + key + "=" + value + "<");
			}
			numeroAssistitiGenerale += numeroAssistitoLivello;
		}

		prtDati.put("#descrizione_livello#", "Totale Assistiti: ");
		prtDati.put("#numero_assist#", numeroAssistitiGenerale + "");
		doc.writeSostituisci("totale_Sintetico_dett", prtDati);

		doc.write("totale_Sintetico_fine");

	}

	private void incrementaLivello(Hashtable totaleLivello, Hashtable hashDaAggiungere) {
		String punto = MIONOME + "incrementaLivello ";
		//		stampa(punto + "Inizio con dati>" + totaleLivello + "\n " + "devo aggiungeere a>" + hashDaAggiungere + "\n");
		Iterator it = totaleLivello.keySet().iterator();
		String valoreLivello = "";
		int numValoreLivello = 0;
		String valoreLivelloPrecedente = "";
		int numValoreLivelloPrecedente = 0;
		while (it.hasNext()) {
			String codLivello = (String) it.next();
			valoreLivello = totaleLivello.get(codLivello) + "";
			numValoreLivello = getIntero(valoreLivello);
			//			stampa(punto + "codLivello>" + codLivello + "< valoreLivello>" + valoreLivello + "");
			if (numValoreLivello > 0) {
				valoreLivelloPrecedente = ISASUtil.getValoreStringa(hashDaAggiungere, codLivello);
				numValoreLivelloPrecedente = getIntero(valoreLivelloPrecedente);
				if (numValoreLivelloPrecedente > 0) {
					numValoreLivello += numValoreLivelloPrecedente;
				}
				hashDaAggiungere.put(codLivello, numValoreLivello + "");
			} else {
				//				stampa(punto + "Il numValoreLivello � " + numValoreLivello + "\n ");
			}
		}
		//		stampa(punto + "Inizio con dati>" + totaleLivello + "\n " + "nuovi valori>" + hashDaAggiungere + "\n");
	}

	private void inizializzaTotaleLivello(Hashtable paceTotaleLivello) {
		String punto = MIONOME + "inizializzaTotaleLivello ";
		Iterator it = decodificaLivello.keySet().iterator();
		while (it.hasNext()) {
			String codLivello = (String) it.next();
			paceTotaleLivello.put(codLivello, "0");
		}
		stampa(punto + "dati che ho inizializzato>" + paceTotaleLivello);
	}

	private Hashtable incrementaLivello(Hashtable paceTotaleLivello, String codLivello) {
		int numero = 1;

		if (ISASUtil.valida(codLivello)) {
			String val = ISASUtil.getValoreStringa(paceTotaleLivello, codLivello);
			numero = getIntero(val);
			if (numero > 0) {
				numero += 1;
			} else {
				numero = 1;
			}
		}
		paceTotaleLivello.put(codLivello, numero + "");

		return paceTotaleLivello;
	}

	private void stampaParzialiHtml(mergeDocument doc, Hashtable prtDati, Hashtable paceTotaleLivello, Hashtable datiZona) {
		parziali(doc, prtDati, paceTotaleLivello, datiZona, true, 0);
	}

	private void stampaParziali(mergeDocument doc, Hashtable prtDati, Hashtable totaleLivello, Hashtable datiZona, int tipoStampa) {
		parziali(doc, prtDati, totaleLivello, datiZona, false, tipoStampa);
	}

	private void parziali(mergeDocument doc, Hashtable prtDati, Hashtable totaleLivello, Hashtable datiZona, boolean html, int tipoStampa) {
		String punto = MIONOME + "stampaParziali";
		prtDati.put("#descrPace_sintFo#", "");
		prtDati.put("#descrizione_livello#", "");
		prtDati.put("#numero_assist_live_pace#", "");
		prtDati.put("#numero_assist_pace#", "");
		prtDati.put("#numero_assist_distre#", "");
		prtDati.put("#numero_assist_zona#", "");
		if (!nessunaDivisione) {

			if (!html) {
				settaDatiDaStampare(prtDati, tipoStampa);
				doc.writeSostituisci("totali", prtDati);
			}
			Iterator itr = totaleLivello.keySet().iterator();
			int numeroAssistitoLivello = 0;
			String descrizioneLivello = "";
			prtDati = new Hashtable();
			while (itr.hasNext()) {
				prtDati.put("#descrPace_sintFo#", "");
				prtDati.put("#descrizione_livello#", "");
				prtDati.put("#numero_assist_live_pace#", "");
				prtDati.put("#numero_assist_pace#", "");
				prtDati.put("#numero_assist_distre#", "");
				prtDati.put("#numero_assist_zona#", "");

				String key = (String) itr.next();
				String value = totaleLivello.get(key) + "";
				numeroAssistitoLivello = getIntero(value);
				if (numeroAssistitoLivello > 0) {
					descrizioneLivello = ISASUtil.getValoreStringa(decodificaLivello, key + "");
					prtDati.putAll(datiZona);
					settaDatiDaStampare(prtDati, tipoStampa);
					prtDati.put("#descrizione_livello#", descrizioneLivello);
					prtDati.put("#numero_assist_live_pace#", numeroAssistitoLivello + "");
					prtDati.put("#numero_assist#", numeroAssistitoLivello + "");
					//				stampaDoc(punto, "totali_dettaglio" + (html ? "_html" : ""), prtDati);
					doc.writeSostituisci("totali_dettaglio" + (html ? "_html" : ""), prtDati);
				} else {
					//				stampa(punto + "\n Non stampao valore key>" + key + "=" + value + "<");
				}
			}
			if (!html) {
				doc.write("totali_dettaglio_fine");
			}
		}
	}

	private void settaDatiDaStampare(Hashtable prtDati, int tipoStampa) {

		switch (tipoStampa) {
		case STAMPA_ZONA:
			prtDati.put("#descrPace_sintFo#", ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
			prtDati.put("#numero_assist_zona#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
			prtDati.put("#numero_assist_distre#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
			break;
		case STAMPA_DISTRETTO:
			prtDati.put("#descrPace_sintFo#", ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
			prtDati.put("#numero_assist_distre#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
			prtDati.put("#numero_assist_pace#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
			break;
		case STAMPA_COMUNE:
			prtDati.put("#descrPace_sintFo#", ISASUtil.getValoreStringa(prtDati, "#descrPace#"));
			prtDati.put("#numero_assist_pace#", ISASUtil.getValoreStringa(prtDati, "#numero_assist#"));
			break;
		default:
			break;
		}
	}

	private void stampaDoc(String punto, String sezione, Hashtable dati) {
		System.out.println(punto + " sezione>" + sezione + "< " + (dati != null ? "\n dati>" + dati : ""));

	}

/*
	private void decodificaDati(ISASConnection dbc, ISASRecord dbr, Hashtable prtDati) {
		String codLivello = ISASUtil.getValoreStringa(dbr, "sku_livello");
		String descrLivello = "";
		if (ISASUtil.valida(codLivello)) {
			descrLivello = ISASUtil.getValoreStringa(decodificaLivello, codLivello);
		}
		prtDati.put("#liv_intensita#", descrLivello);
		System.out.println("LIVELLO" + descrLivello);
		String codPatPrev = ISASUtil.getValoreStringa(dbr, "pat_prev");
		String descrPatPrev = "";
		if (ISASUtil.valida(codPatPrev)) {
			String query = " select diagnosi from tab_diagnosi where cod_diagnosi = '" + codPatPrev + "' ";
			ISASRecord dbrTabDiagnosi = getRecord(dbc, query);
			descrPatPrev = ISASUtil.getValoreStringa(dbrTabDiagnosi, "diagnosi");
		}
		prtDati.put("#prevalente#", descrPatPrev);

	}
	*/
	
	private void decodificaDati(ISASConnection dbc, ISASRecord dbr, Hashtable prtDati) {
		String codLivello = ISASUtil.getValoreStringa(dbr, "sku_livello");
		String descrLivello = "";
		if (ISASUtil.valida(codLivello)) {
			descrLivello = ISASUtil.getValoreStringa(decodificaLivello, codLivello);
		}
		prtDati.put("#liv_intensita#", descrLivello);
		
		try {
			
			String query = " SELECT diag1 FROM diagnosi d WHERE d.n_cartella = " + dbr.get("n_cartella")+ 
					       " AND d.data_diag IN (SELECT MAX (a.data_diag) " +
					       " FROM diagnosi a WHERE a.n_cartella = d.n_cartella" +
					       " AND a.data_diag <= " + formatDate(dbc,dbr.get("sku_data").toString()) + ")";
                    
			System.out.println("QUERY" +query);
			
					String diagPrinc = "";
					ISASRecord dbrDiag = (ISASRecord)dbc.readRecord(query);
					if ((dbrDiag != null) && (dbrDiag.get("diag1") != null))
						diagPrinc = dbrDiag.get("diag1").toString();
					else
						diagPrinc ="";
	    
	     
			prtDati.put("#prevalente#",ISASUtil.getDecode(dbc,"tab_diagnosi","cod_diagnosi",
					diagPrinc,"diagnosi"));
		
		
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

	private Object recuperaDescrzioneOperatore(ISASConnection dbc, String codOperatore) {
		String descrizione = "";
		String query = "select * from operatori where codice = '" + codOperatore + "' ";
		ISASRecord dbrOperatori = getRecord(dbc, query);
		descrizione = ISASUtil.getValoreStringa(dbrOperatori, "cognome");
		descrizione += (ISASUtil.valida(descrizione) ? " " : "") + ISASUtil.getValoreStringa(dbrOperatori, "nome");

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
			//M.Minerba 28/02/2013 per Pistoia
			decodificaLivello.put("0", "NEGATIVO");
			decodificaLivello.put("1", "BASSA INTENSITA'");
			decodificaLivello.put("2", "MEDIO INTENSITA'");
			decodificaLivello.put("3", "ALTA INTENSITA'");

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
		String livello = ISASUtil.getValoreStringa(par, "liv_ass");
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
		
		query.append(" SELECT DISTINCT s.n_cartella, sku_data, sku_data_fine, sku_livello, ");
		query.append(selZona + selDistretto + selComune + " u.tipo ");
		query.append(" FROM skuvt s, anagra_c a, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u");
		query.append(" WHERE sku_data<="+formatDate(dbc,dataFine)+
				                  " AND( sku_data_fine>="+formatDate(dbc,dtInizio)+" OR sku_data_fine IS NULL)"+
				                  " AND sku_adi='S'" +	
				                  " AND sku_livello='"+livello+"'" +				                 		 			
				                  " AND a.data_variazione IN"+
				                  " (SELECT MAX (data_variazione)"+
				                  " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella");
		
		query.append(") ");

		condWhere =  " AND s.n_cartella = a.n_cartella";

		String ragg = ISASUtil.getValoreStringa(par, "ragg");
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, zona);
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, distretto);
		condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, pca);
		query.append(condWhere);
		if ((String) par.get("dom_res") == null) {
			if (ragg.equals("C")) {
				query.append(" AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                        " AND u.codice=a.dom_citta)"+
        		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                        " AND u.codice=a.citta))");
			} else if (ragg.equals("A")) {
				query.append(" AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                        " AND u.codice=a.dom_areadis)"+
        		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                        " AND u.codice=a.areadis))");
			}
			else if (ragg.equals("P")) {
				query.append(" AND EXISTS (SELECT * FROM skinf c "+
                 		" WHERE c.n_cartella = s.n_cartella "+
                 		" AND c.ski_data_apertura <= s.sku_data "+
                 		" AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                 		"AND c.ski_cod_presidio = u.codice)");
			}
			
		} else if (((String) par.get("dom_res")).equals("D")) {
			if (ragg.equals("C"))
				query.append(" AND u.codice=a.dom_citta ");
			else if (ragg.equals("A"))
				query.append(" AND u.codice=a.dom_areadis ");
			else if (ragg.equals("P"))
				query.append(" AND EXISTS (SELECT * FROM skinf c "+
                         " WHERE c.n_cartella = s.n_cartella "+
                         " AND c.ski_data_apertura <= s.sku_data "+
                         " AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                         " AND c.ski_cod_presidio = u.codice) ");
			
		} else if (((String) par.get("dom_res")).equals("R")) {
			if (ragg.equals("C"))
				query.append(" AND u.codice=a.citta");
			else if (ragg.equals("A"))
				query.append(" AND u.codice=a.areadis ");
			else if (ragg.equals("P"))
				query.append(" AND EXISTS (SELECT * FROM skinf c "+
                     		" WHERE c.n_cartella = s.n_cartella "+
                     		" AND c.ski_data_apertura <= s.sku_data "+
                     		" AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                     		"AND c.ski_cod_presidio = u.codice) ");
		}
		
		
		
		
         


		query.append(" ORDER BY s.n_cartella, sku_data, sku_data_fine  ");
		
		
		
				        System.out.println("Select CaricaHash:PrestDom"+query);
		

		return query.toString();
		
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

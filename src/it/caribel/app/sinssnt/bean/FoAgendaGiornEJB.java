package it.caribel.app.sinssnt.bean;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// 06/02/2007 - EJB di connessione alla procedura SINS Agenda
// bargi
//Stampa dell'agenda per operatore oppure per presidio la stampa avviene per giorno
// ==========================================================================

import it.caribel.app.common.ejb.ConfEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ManagerDate;
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
import it.pisa.caribel.sinssnt.controlli.CaricaAgendaPrestazioni;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.NumberDateFormat;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.dateutility;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import org.zkoss.util.resource.Labels;

public class FoAgendaGiornEJB extends SINSSNTConnectionEJB {
	private static final String MIONOME = "277-FoAgendaGiornEJB.";
private CaricaAgendaPrestazioni cag=new CaricaAgendaPrestazioni(); 
dateutility dt =new dateutility();
NumberDateFormat ndf=new NumberDateFormat();
private String CTS_NO_DATI = " ----- ";
	public FoAgendaGiornEJB() {
	}
	boolean conf_note=false;

	public byte[] query_agenda(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_agenda ";
		stampa(punto + "\t Inizio con dati >" + par + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		ConfEJB confEjb = new ConfEJB();
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			conf_note= confEjb.getAbilitazioneConf(dbc,utente,  "ST_NOTE_AG");
			
			ISASCursor dbcur = null;
			if (par.get("sin").equals("S"))
				dbcur = dbc.startCursor(getSelectSintetica(dbc, par));
			else
				dbcur = dbc.startCursor(getSelect(dbc, par));
			mkLayout(dbc, par, eve);

			if (dbcur == null) {
				eve.write("messaggio");
				eve.write("finale");
				System.out.println("FoAgendaGiornEJB.query_agenda(): cursore non valido");
			} else {
				if (dbcur.getDimension() <= 0) {
					eve.write("messaggio");
					eve.write("finale");
				} else {
					if (par.get("sin").equals("S"))
						mkBodySin(eve, dbcur, par, dbc);
					else
						mkBody(eve, dbcur, par, dbc);
//					mkFineTabella(eve);
					eve.write("finale");
				} // fine if dbcur.getDimension()  
			} // fine if dbcur
			dbcur.close();
			eve.close();
			//			System.out.println("FoAgendaGiornEJB.query_agenda(): DEBUG " + "documento restituito [" + (new String(eve.get())) + "]");

			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get(); // restituisco il bytearray
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoAgendaGiornEJB.query_agenda(): " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("FoAgendaGiornEJB.query_agenda(): " + e1);
				}
			}
		}
	} // End of query_agenda() method

	public byte[] query_agenda_plan(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_agenda_plan ";
		stampa(punto + "\t Inizio con dati >" + par + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		ConfEJB confEjb = new ConfEJB();
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			conf_note= confEjb.getAbilitazioneConf(dbc,utente,  "ST_NOTE_AG");
			impostaDataInizio(par);
			String dataI= ISASUtil.getValoreStringa(par, "data_inizio");
			String dataF= ISASUtil.getValoreStringa(par, "data_fine");
			
			Vector vSettimane=cag.caricaSettimane(dataI, dataF);
			//Hashtable dati = new Hashtable();
			Hashtable dati = mkLayoutPlan(dbc, par, eve);
			
				
			if(vSettimane.size()>0) {
				Enumeration sett=vSettimane.elements();
				int settim=0;
				boolean tagliato = false;
				while(sett.hasMoreElements())
				{				
					settim++;
					Vector settimana=new Vector();
					settimana =(Vector)sett.nextElement();
					if (tagliato) {
						eve.write("taglia");
					}
					Hashtable datiAggiornati = new Hashtable();			
					mkLayoutPlanSettimanale(settimana, datiAggiornati);
					dati.putAll(datiAggiornati);
					par.put("dataIniziSettimana", ISASUtil.getValoreStringa(datiAggiornati, "dataIniziSettimana"));
					par.put("dataFineSettimana", ISASUtil.getValoreStringa(datiAggiornati, "dataFineSettimana"));
					ISASCursor dbcur = dbc.startCursor(getSelectPlanOperatori(dbc, par));
					esaminaDatiCorrenti(eve, dbc, dbcur, par, dati);
					dbcur.close();
					tagliato = true;
				}
			}else {
				String periodo = "dal " + ndf.formDate(ISASUtil.getValoreStringa(par, "dataIniziSettimana"),"gg/mm/aaaa");
				periodo += " al " + ndf.formDate(	ISASUtil.getValoreStringa(par, "dataFineSettimana"),"gg/mm/aaaa");				
				dati.put("#periodoInfostmp#", periodo);				
				//stampaDoc(punto, "messaggio", dati);
				eve.writeSostituisci("messaggio", dati);
				eve.write("finale");
			}
			eve.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoAgendaGiornEJB.query_agenda(): " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("FoAgendaGiornEJB.query_agenda(): " + e1);
				}
			}
		}
	} // End of query_agenda() method
/*
 
	public byte[] query_agenda_plan(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_agenda_plan ";
		stampa(punto + "\t Inizio con dati >" + par + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			stampa(punto + "\n INIZIO \n");
			impostaDataInizio(par);
			Hashtable dati = mkLayoutPlan(dbc, par, eve);
			//			stampa(punto + "\t Dati da processare >" + dati + "<\n Dati che analizzo>" + par + "<");
			int numeroSettimanaInizio = getvaloreInt(ISASUtil.getValoreStringa(dati, "#numSett#"));
			int numeroSettimanaFine = getvaloreInt(ISASUtil.getValoreStringa(dati, "#numSettFine#"));
			int annoCorrente = getvaloreInt(ISASUtil.getValoreStringa(dati, "#anno#"));
			int annoFine = getvaloreInt(ISASUtil.getValoreStringa(dati, "#annoFine#"));
			//			stampa(punto + "numeroSettimanaInizio>" + numeroSettimanaInizio + "< numeroSettimanaFine>" + numeroSettimanaFine
			//					+ "< annoCorrente>" + annoCorrente + "<\nannoFine>" + annoFine + "<");
			if (numeroSettimanaFine == 0) {
				numeroSettimanaFine = numeroSettimanaInizio;
			}
			if (annoFine == 0) {
				annoFine = annoCorrente;
			}
			//			stampa(punto + "numeroSettimanaInizio>" + numeroSettimanaInizio + "< numeroSettimanaFine>" + numeroSettimanaFine
			//					+ "< annoCorrente>" + annoCorrente + "< annoFine>" + annoFine + "<\n");
			int numeroSettimaneAttuale = numeroSettimanaInizio;
			stampa(punto + "\n numero Settimane>" + numeroSettimaneAttuale + "<" + "numeroSettimaneAttuale>" + numeroSettimaneAttuale
					+ "<numeroSettimanaFine>" + numeroSettimanaFine + "<annoCorrente>" + annoCorrente + "<annoFine>" + annoFine + "<"
					+ "numeroSettimaneAttuale <= numeroSettimanaFine>" + (numeroSettimaneAttuale <= numeroSettimanaFine)
					+ "<(annoCorrente <= annoFine)>" + (annoCorrente <= annoFine) + "< (numeroSettimaneAttuale!=numeroSettimanaFine)>"
					+ (numeroSettimaneAttuale != numeroSettimanaFine) + "<");
			boolean tagliato = false;
			stampa(punto + " IF annoFine=" + annoFine + " annoCorrente=" + annoCorrente);
			//if ((((numeroSettimaneAttuale == numeroSettimanaFine) || (numeroSettimaneAttuale != numeroSettimanaFine)) && (annoCorrente <= annoFine))) {
			if (annoCorrente <= annoFine) {
				int kkkk = 0;
				stampa(punto + " INIZIO WHILE annoCorrente=" + annoCorrente + " annoFine=" + annoFine +
					" numeroSettimaneAttuale=" + numeroSettimaneAttuale + " numeroSettimanaFine=" + numeroSettimanaFine);
				//				boolean entrare = (numeroSettimaneAttuale == numeroSettimanaFine) && (annoCorrente == annoFine);
				//				stampa(punto + " devo entrare>" + entrare + "<\n");
				//while (((numeroSettimaneAttuale <= numeroSettimanaFine) || (numeroSettimaneAttuale != numeroSettimanaFine))
				if ((annoCorrente == annoFine) && (numeroSettimaneAttuale > numeroSettimanaFine)){
					annoFine++;	// RB 19/12/2012
				}
				while (((annoCorrente < annoFine) && (numeroSettimaneAttuale != numeroSettimanaFine)) || 	// RB 19/12/2012
						((annoCorrente == annoFine) && (numeroSettimaneAttuale <= numeroSettimanaFine))){
						
						
//						(numeroSettimaneAttuale <= numeroSettimanaFine) && (annoCorrente < annoFine) || (numeroSettimaneAttuale == numeroSettimanaFine))
//						&& (annoCorrente <= annoFine)) {
					//					entrare = false;
					//				while (numeroSettimaneAttuale <= numeroSettimanaFine || (annoCorrente <= annoFine) && kkkk < 2) {
					//&& (numeroSettimaneAttuale != numeroSettimanaFine)) {
					kkkk++;
					stampa(punto + kkkk + " \nnumeroSettimaneAttuale>" + numeroSettimaneAttuale + "<numeroSettimanaFine>"
							+ numeroSettimanaFine + "annoCorrente>" + annoCorrente + "<annoFine>" + annoFine+
							"(annoCorrente < annoFine)>" +(annoCorrente < annoFine)+"< (numeroSettimaneAttuale != numeroSettimanaFine)>" +
							(numeroSettimaneAttuale != numeroSettimanaFine) + "((annoCorrente == annoFine)>" +
							(annoCorrente == annoFine) +" && (numeroSettimaneAttuale <= numeroSettimanaFine))>" +
							(numeroSettimaneAttuale <= numeroSettimanaFine)+"<");
					if (tagliato) {
						eve.write("taglia");
					}
					//				stampa(punto + " numeroSettimanaAttuale>" + numeroSettimaneAttuale + "<\n annoCorrente>" + annoCorrente + "<");
					Hashtable datiAggiornati = new Hashtable();
					annoCorrente = mkLayoutPlanSettimanale(numeroSettimaneAttuale, annoCorrente, datiAggiornati);
					stampa(punto + kkkk + " annoCorrente>" + annoCorrente + "<annoFine>" + annoFine + "");
					dati.putAll(datiAggiornati);
					//				stampa(punto + "\nDati Recuperati>" + datiAggiornati + "<\n");
					par.put("dataIniziSettimana", ISASUtil.getValoreStringa(datiAggiornati, "dataIniziSettimana"));
					par.put("dataFineSettimana", ISASUtil.getValoreStringa(datiAggiornati, "dataFineSettimana"));
					par.put("primoGiornoSettimana", ISASUtil.getValoreStringa(datiAggiornati, "primoGiornoSettimana"));
					par.put("ultimoGiornoSettimana", ISASUtil.getValoreStringa(datiAggiornati, "ultimoGiornoSettimana"));

					par.put("dataFineSettIta", ISASUtil.getValoreStringa(datiAggiornati, "dataFineSettIta"));
					par.put("dataIniziSettIta", ISASUtil.getValoreStringa(datiAggiornati, "dataIniziSettIta"));

					ISASCursor dbcur = dbc.startCursor(getSelectPlanOperatori(dbc, par));
					esaminaDatiCorrenti(eve, dbc, dbcur, par, dati);
					dbcur.close();
					//				mkBodyPlan(eve, dbcur, par, dbc, dati);
					//				mkFineTabella(eve);
					//				eve.write("taglia");
					numeroSettimaneAttuale++;
					if (numeroSettimaneAttuale > 52) {
						numeroSettimaneAttuale = numeroSettimaneAttuale / 52;
						annoCorrente++; 	// RB 19/12/2012
						dati.put("#anno#", annoCorrente+"");
					}
					tagliato = true;
					stampa(punto + kkkk + "\t Fine settimana attuale>" + numeroSettimaneAttuale + "<numeroSettimanaFine>"
							+ numeroSettimanaFine + "\n (numeroSettimaneAttuale != numeroSettimanaFine)>"
							+ (numeroSettimaneAttuale != numeroSettimanaFine) + "<annoCorrente>" + annoCorrente + "<");
				}
			} else {
				String periodo = "dal " + ISASUtil.getValoreStringa(par, "dataIniziSettIta");
				periodo += " al " + ISASUtil.getValoreStringa(par, "dataFineSettIta");
				dati.put("#periodoInfostmp#", periodo);
				stampaDoc(punto, "messaggio", dati);
				eve.writeSostituisci("messaggio", dati);
				stampa(punto + "\t MESSAGGIO NON STAMPO NULL");

				eve.write("finale");
			}
			//			eve.write("finale");
			eve.close();
			//			stampa(punto + "FoAgendaGiornEJB.query_agenda(): DEBUG " + "documento restituito \n\n>" + (new String(eve.get())) + "<\n\n");
			stampa(punto + "\t FINE ESECUZIONE ");
			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoAgendaGiornEJB.query_agenda(): " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("FoAgendaGiornEJB.query_agenda(): " + e1);
				}
			}
		}
	} // End of query_agenda() method
 */
	private void esaminaDatiCorrenti(mergeDocument eve, ISASConnection dbc, ISASCursor dbcur, Hashtable par, Hashtable dati) {
		String punto = MIONOME + "esaminaDatiCorrenti ";
		stampa(punto + "\tInizio con dati>" + par + "<\n");
		try {
			if (dbcur == null) {
				dati = mkLayoutPlan(dbc, par, eve);
				//				par.put("primoGiornoSettimana", ISASUtil.getValoreStringa(dati, "primoGiornoSettimana"));
				//				par.put("ultimoGiornoSettimana", ISASUtil.getValoreStringa(datiAggiornati, "ultimoGiornoSettimana"));

				String periodo = "dal " + ndf.formDate(ISASUtil.getValoreStringa(par, "dataIniziSettimana"),"gg/mm/aaaa");
				periodo += " al " + ndf.formDate(	ISASUtil.getValoreStringa(par, "dataFineSettimana"),"gg/mm/aaaa");
				dati.put("#periodoInfostmp#", periodo);
				stampaDoc(punto, "messaggio", dati);
				eve.writeSostituisci("messaggio", dati);
				stampa(punto + "\t MESSAGGIO \n");
			} else {
				if (dbcur.getDimension() <= 0) {
					dati = mkLayoutPlan(dbc, par, eve);

					String periodo = "dal " + ndf.formDate(ISASUtil.getValoreStringa(par, "dataIniziSettimana"),"gg/mm/aaaa");
					periodo += " al " + ndf.formDate(	ISASUtil.getValoreStringa(par, "dataFineSettimana"),"gg/mm/aaaa");
					
					dati.put("#periodoInfostmp#", periodo);

					stampaDoc(punto, "messaggio", dati);
					eve.writeSostituisci("messaggio", dati);
				} else {
					
					stampaDoc(punto, "stampaInfo", dati);
					eve.writeSostituisci("stampaInfo", (Hashtable) dati.clone());
					if (par.get("sin").equals("S"))
						mkBodyPlanSin(eve, dbcur, par, dbc, dati);
					else
						mkBodyPlan(eve, dbcur, par, dbc, dati);
				}
			}
			eve.write("finale");

		} catch (Exception e) {
			stampa(punto + "\t  Errore nel processare i dati");
		}
	}
/*
	public byte[] query_agenda_planOLD(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_agenda_plan ";
		stampa(punto + "\t Inizio con dati >" + par + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);
			impostaDataInizio(par);
			ISASCursor dbcur = dbc.startCursor(getSelectPlan(dbc, par));

			if (dbcur == null) {
				Hashtable dati = mkLayoutPlan(dbc, par, eve);
				eve.writeSostituisci("messaggio", dati);
				eve.write("finale");
				stampa(punto + "\t MESSAGGIO \n");
			} else {
				if (dbcur.getDimension() <= 0) {
					Hashtable dati = mkLayoutPlan(dbc, par, eve);
					eve.writeSostituisci("messaggio", dati);
					eve.write("finale");
				} else {
					//					stampaPiuSettimane(dbc, dbcur, eve, par);
					//					stampaPiuSettimane(dbc, dbcur, eve, par, dati);
					//					mkBodyPlan(eve, dbcur, par, dbc, dati);
					//					mkFineTabella(eve);
					eve.write("finale");
				} // fine if dbcur.getDimension()
			} // fine if dbcur

			dbcur.close();
			eve.close();
			//			stampa(punto + "FoAgendaGiornEJB.query_agenda(): DEBUG " + "documento restituito \n\n>" + (new String(eve.get())) + "<\n\n");
			stampa(punto + "\t FINE ESECUZIONE\n");
			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("FoAgendaGiornEJB.query_agenda(): " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("FoAgendaGiornEJB.query_agenda(): " + e1);
				}
			}
		}
	} // End of query_agenda() method
*/
	//	private void stampaPiuSettimane(ISASConnection dbc, ISASCursor dbcur, mergeDocument eve, Hashtable par, Hashtable dati)
	//			throws Exception {
	/*private void stampaPiuSettimane(ISASConnection dbc, ISASCursor dbcur, mergeDocument eve, Hashtable par) throws Exception {
		String punto = MIONOME + "stampaPiuSettimane ";
		Hashtable dati = mkLayoutPlan(dbc, par, eve);
		//		stampa(punto + "\t Dati da processare >" + dati + "<\n Dati che analizzo>" + par + "<");
		int numeroSettimanaInizio = getvaloreInt(ISASUtil.getValoreStringa(dati, "#numSett#"));
		int numeroSettimanaFine = getvaloreInt(ISASUtil.getValoreStringa(dati, "#numSettFine#"));
		int annoCorrente = getvaloreInt(ISASUtil.getValoreStringa(dati, "#anno#"));
		int annoFine = getvaloreInt(ISASUtil.getValoreStringa(dati, "#annoFine#"));
		//		stampa(punto + "numeroSettimanaInizio>" + numeroSettimanaInizio + "< numeroSettimanaFine>" + numeroSettimanaFine
		//				+ "< annoCorrente>" + annoCorrente + "<\nannoFine>" + annoFine + "<");
		if (numeroSettimanaFine == 0) {
			numeroSettimanaFine = numeroSettimanaInizio;
		}

		//		stampa(punto + "numeroSettimanaInizio>" + numeroSettimanaInizio + "< numeroSettimanaFine>" + numeroSettimanaFine
		//				+ "< annoCorrente>" + annoCorrente + "<\n");
		int numeroSettimaneAttuale = numeroSettimanaInizio;

		while (numeroSettimaneAttuale <= numeroSettimanaFine && ((annoCorrente == annoFine) || (annoCorrente - 1 == annoFine))) {
			//			stampa(punto + " numeroSettimanaAttuale>" + numeroSettimaneAttuale + "<\n ");
			Hashtable datiAggiornati = new Hashtable();
			annoCorrente = mkLayoutPlanSettimanale(numeroSettimaneAttuale, annoCorrente, datiAggiornati);
			dati.putAll(datiAggiornati);
			mkBodyPlan(eve, dbcur, par, dbc, dati);
			mkFineTabella(eve);
			eve.write("taglia");
			if (numeroSettimaneAttuale >= 52) {
				numeroSettimaneAttuale = numeroSettimaneAttuale / 52;
			}
			numeroSettimaneAttuale++;
		}

	}
*/
	private void  mkLayoutPlanSettimanale(Vector vSett, Hashtable datiGiornalieri) {
		String punto = MIONOME + "mkLayoutPlanSettimanale ";
		String dtI=(String)vSett.get(0);
		String dtF=(String)vSett.get(6);
		debugMessage(punto+" settimana="+dtI+" - "+dtF);
		
		String mese=dt.getMeseSt(dtF);
		String anno=""+dt.getAnno(dtF);		
		GregorianCalendar gc=dt.getGreg(dtF);
		int numeroSettimana=gc.get(Calendar.WEEK_OF_YEAR);
		datiGiornalieri.put("#numSett#", numeroSettimana + "");
		datiGiornalieri.put("#anno#", anno + "");
		datiGiornalieri.put("dataIniziSettimana", ndf.formDate(dtI, "aaaa-mm-gg"));
		datiGiornalieri.put("#lungg#", ndf.formDate(dtI, "gg/mm/aaaa"));
		datiGiornalieri.put("#margg#", ndf.formDate((String)vSett.get(1), "gg/mm/aaaa"));
		datiGiornalieri.put("#mergg#", ndf.formDate((String)vSett.get(2), "gg/mm/aaaa"));
		datiGiornalieri.put("#giogg#", ndf.formDate((String)vSett.get(3), "gg/mm/aaaa"));
		datiGiornalieri.put("#vengg#", ndf.formDate((String)vSett.get(4), "gg/mm/aaaa"));
		datiGiornalieri.put("#sabgg#", ndf.formDate((String)vSett.get(5), "gg/mm/aaaa"));
		datiGiornalieri.put("#domgg#", ndf.formDate((String)vSett.get(6), "gg/mm/aaaa"));
		datiGiornalieri.put("dataFineSettimana", ndf.formDate(dtF, "aaaa-mm-gg"));
	}
/*
 
	private int mkLayoutPlanSettimanale(int numeroSettimana, int annoCorrente, Hashtable datiGiornalieri) {
		String punto = MIONOME + "mkLayoutPlanSettimanale ";
		stampa(punto + "Numerosettimana>" + numeroSettimana + "< annoCorrente>" + annoCorrente + "<");
		Calendar cal = getPrimoGiornoSettimana(numeroSettimana, annoCorrente);
		datiGiornalieri.put("#numSett#", numeroSettimana + "");
		datiGiornalieri.put("dataIniziSettimana", getDataDbc(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal
				.get(Calendar.DAY_OF_MONTH)));
		//bargi	datiGiornalieri.put("primoGiornoSettimana", cal.get(Calendar.DAY_OF_MONTH) + "");
		datiGiornalieri.put("dataIniziSettIta", getDataIta(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal
				.get(Calendar.DAY_OF_MONTH)));

		datiGiornalieri.put("#lungg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		cal = giornoSuccessivo(cal);
		datiGiornalieri.put("#margg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

		cal = giornoSuccessivo(cal);
		datiGiornalieri.put("#mergg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		cal = giornoSuccessivo(cal);
		datiGiornalieri.put("#giogg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		cal = giornoSuccessivo(cal);
		datiGiornalieri.put("#vengg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		cal = giornoSuccessivo(cal);
		datiGiornalieri.put("#sabgg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		cal = giornoSuccessivo(cal);
		datiGiornalieri.put("#domgg#", cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

		//bargi	datiGiornalieri.put("ultimoGiornoSettimana", cal.get(Calendar.DAY_OF_MONTH) + "");
		datiGiornalieri.put("dataFineSettimana", getDataDbc(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal
				.get(Calendar.DAY_OF_MONTH)));
		datiGiornalieri.put("dataFineSettIta", getDataIta(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), cal
				.get(Calendar.DAY_OF_MONTH)));

		//		stampa(punto + "dati processati>" + datiGiornalieri + "<\nNumerosettimana>" + numeroSettimana + "< annoCorrente>" + annoCorrente
		//				+ "<");
		return cal.get(Calendar.YEAR);
	}

 */
	private Calendar giornoSuccessivo(Calendar cal) {
		String punto = MIONOME + "giornoSuccessivo ";
		String linea = "";
		int mese;
		int anno;
		int giorno = cal.get(Calendar.DAY_OF_MONTH);
		cal.set(Calendar.DAY_OF_MONTH, giorno + 1);
		int giorn = cal.get(Calendar.DAY_OF_MONTH);
		anno = cal.get(Calendar.YEAR);
		mese = cal.get(Calendar.MONTH);
		//		stampa(punto + "\t giorono>" + cal.get(Calendar.DAY_OF_MONTH) + " mese>" + cal.get(Calendar.MONTH) + "< anno>"
		//				+ cal.get(Calendar.YEAR) + "<");

		//	        linea = "\t giorono>" + cal.get(Calendar.DAY_OF_MONTH) + " mese>" + cal.get(Calendar.MONTH) + "< anno>"
		//	                + cal.get(Calendar.YEAR) + "<";
		//	        stampa(punto + "\t linea>" + linea + "<\n\n");
		return cal;

	}

	private int mkLayoutPlanSettimanaleOLD(int numeroSettimana, int annoCorrente, Hashtable datiGiornalieri) {
		String punto = MIONOME + "mkLayoutPlanSettimanale ";
		stampa(punto + "\t Numerosettimana>" + numeroSettimana + "< annoCorrente>" + annoCorrente + "<\n\n");
		//		Hashtable ht = new Hashtable();

		Calendar cal = getPrimoGiornoSettimana(numeroSettimana, annoCorrente);
		int anno, mese;
		anno = cal.get(Calendar.YEAR);
		int meseConteggio = cal.get(Calendar.MONTH);
		int annoConteggio = cal.get(Calendar.MONTH);

		int inizioGiornoSettimana = cal.get(Calendar.DATE);
		Calendar calender = new GregorianCalendar(annoConteggio, meseConteggio, inizioGiornoSettimana);
		mese = calender.get(Calendar.MONTH);

		int lun = calender.get(Calendar.DAY_OF_MONTH);
		//		stampa(punto + "giorno>" + calender.get(Calendar.DAY_OF_MONTH));
		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#lungg#", lun + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));
		calender = new GregorianCalendar(annoConteggio, meseConteggio, ++inizioGiornoSettimana);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		int mar = calender.get(Calendar.DAY_OF_MONTH);

		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#margg#", mar + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);

		calender = new GregorianCalendar(annoConteggio, meseConteggio, ++inizioGiornoSettimana);
		int mer = calender.get(Calendar.DAY_OF_MONTH);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#mergg#", mer + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));

		calender = new GregorianCalendar(annoConteggio, meseConteggio, ++inizioGiornoSettimana);
		int gio = calender.get(Calendar.DAY_OF_MONTH);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#giogg#", gio + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));

		calender = new GregorianCalendar(annoConteggio, meseConteggio, ++inizioGiornoSettimana);
		int ven = calender.get(Calendar.DAY_OF_MONTH);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);

		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#vengg#", ven + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));

		calender = new GregorianCalendar(annoConteggio, meseConteggio, ++inizioGiornoSettimana);
		int sab = calender.get(Calendar.DAY_OF_MONTH);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		stampa(punto + "\t mese>" + mese + "<\n");
		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#sabgg#", sab + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));;

		calender = new GregorianCalendar(annoConteggio, meseConteggio, ++inizioGiornoSettimana);
		int dom = calender.get(Calendar.DAY_OF_MONTH);
		anno = calender.get(Calendar.YEAR);
		mese = calender.get(Calendar.MONTH);
		stampa(punto + "\t mese>" + mese + "<\n");
		stampa(punto + " anno>" + anno + "< mese>" + mese + "<\nannoConteggio>" + annoConteggio + "<");
		datiGiornalieri.put("#domgg#", dom + "/" + (mese + 1) + "/" + anno);// recuperaAnno(anno));
		stampa(punto + "\t dati da processare>" + datiGiornalieri + "<\n anno>" + anno + "<\n");

		return anno;
	}

	private String recuperaAnno(int anno) {
		String valoreAnno = anno + "";
		if (valoreAnno.trim().length() >= 4) {
			valoreAnno = valoreAnno.substring(2);
		}
		return valoreAnno;
	}

	private int getvaloreInt(String valoreStringa) {
		String punto = MIONOME + "getvaloreInt ";
		int numero = 0;
		try {
			numero = Integer.parseInt(valoreStringa);
		} catch (Exception e) {
			stampa(punto + "\t Errore nel recuperare le informazioni \n " + e.getMessage() + "<\nErrore nel convertire>" + valoreStringa
					+ "<\n");
		}

		return numero;
	}

	private void impostaDataInizio(Hashtable par) {
		String punto = MIONOME + "impostaDataInizio\t";
		stampa(punto + " Inizio con dati>" + par + "<");
		String data = ISASUtil.getValoreStringa(par, "data_inizio");
		String dataInizioSett=cag.getFirstDayWeek(data, 6);
		String dataFineSett=cag.getLastDayWeek(data, 6);
		par.put("dataIniziSettimana",dataInizioSett );
		par.put("dataFineSettimana",dataFineSett);
	}
	
	/*
		
		int giorno = -1, mese = -1, anno = -1;
		int pos = data.indexOf("-");
		if (pos > 0) {
			anno = getIntero(data.substring(0, 4));
		}
		String val = data.substring(pos + 1);
		pos = val.indexOf("-");
		if (pos > 0) {
			mese = getIntero(val.substring(0, pos));
			giorno = getIntero(val.substring(pos + 1));
		}

		//		stampa(punto + "\t giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<\n");
		mese = (mese <= 0 ? 0 : mese - 1);
		//		stampa(punto + "\t dopo giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<\n");
		//		int numeroSettimana = NumSettYear(anno, mese, giorno);
		//		System.out.println("stamp>" + getIntervalloSettimana(numeroSettimana, anno) + "<");
		Calendar gc = NumSettYear(anno, mese, giorno);
		int numeroSettimana = gc.get(Calendar.WEEK_OF_YEAR);
		anno = gc.get(Calendar.YEAR);
		//		stampa(punto + " anno>" + anno + "<");

		Calendar cal = getPrimoGiornoSettimana(numeroSettimana, anno);
		par.put("dataIniziSettimana", getDataDbc(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), (cal.get(Calendar.DATE))));
		//bargi par.put("primoGiornoSettimana", getValdb(cal.get(Calendar.DATE)));
		cal.add(Calendar.DATE, 6);

		par.put("dataFineSettimana", getDataDbc(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), (cal.get(Calendar.DATE))));
		//bargi par.put("ultimoGiornoSettimana", getValdb(cal.get(Calendar.DATE)));
		scriviLog.scrivi(punto + "Fine >" + par + "<");

	}*/
/*
 private void impostaDataInizio(Hashtable par) {
		String punto = MIONOME + "impostaDataInizio\t";
		stampa(punto + " Inizio con dati>" + par + "<");
		String data = ISASUtil.getValoreStringa(par, "data_inizio");
		int giorno = -1, mese = -1, anno = -1;
		int pos = data.indexOf("-");
		if (pos > 0) {
			anno = getIntero(data.substring(0, 4));
		}
		String val = data.substring(pos + 1);
		pos = val.indexOf("-");
		if (pos > 0) {
			mese = getIntero(val.substring(0, pos));
			giorno = getIntero(val.substring(pos + 1));
		}

		//		stampa(punto + "\t giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<\n");
		mese = (mese <= 0 ? 0 : mese - 1);
		//		stampa(punto + "\t dopo giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<\n");
		//		int numeroSettimana = NumSettYear(anno, mese, giorno);
		//		System.out.println("stamp>" + getIntervalloSettimana(numeroSettimana, anno) + "<");
		Calendar gc = NumSettYear(anno, mese, giorno);
		int numeroSettimana = gc.get(Calendar.WEEK_OF_YEAR);
		anno = gc.get(Calendar.YEAR);
		//		stampa(punto + " anno>" + anno + "<");

		Calendar cal = getPrimoGiornoSettimana(numeroSettimana, anno);
		par.put("dataIniziSettimana", getDataDbc(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), (cal.get(Calendar.DATE))));
		//bargi par.put("primoGiornoSettimana", getValdb(cal.get(Calendar.DATE)));
		cal.add(Calendar.DATE, 6);

		par.put("dataFineSettimana", getDataDbc(cal.get(Calendar.YEAR), (cal.get(Calendar.MONTH) + 1), (cal.get(Calendar.DATE))));
		//bargi par.put("ultimoGiornoSettimana", getValdb(cal.get(Calendar.DATE)));
		scriviLog.scrivi(punto + "Fine >" + par + "<");

	}
 */
	private int recuperaAnnoSettimana(int numeroSettimana, int anno) {
		String punto = MIONOME + "";
		int valAnno = anno;

		return valAnno;
	}

	private String getDataDbc(int anno, int mese, int giorno) {
		String linedata = "";
		if (anno > 0 && mese > 0 && giorno > 0) {
			linedata = anno + "-" + (mese > 9 ? mese + "" : "0" + mese) + "-" + (giorno > 9 ? giorno + "" : "0" + giorno);
		}
		return linedata;
	}

	private String getDataIta(int anno, int mese, int giorno) {
		String linedata = "";
		if (anno > 0 && mese > 0 && giorno > 0) {
			linedata = (giorno > 9 ? giorno + "" : "0" + giorno) + "/" + (mese > 9 ? mese + "" : "0" + mese) + "/" + anno;
		}
		return linedata;
	}

	private Object getValdb(int i) {
		String giornoDb = i + "";
		if (i <= 9) {
			giornoDb = "0" + i;
		}
		return giornoDb;
	}

	private void stampaQuery(String punto, String s) {
		System.out.println(punto + "\tQuery>" + s);
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	/**
	* stampa sintetica-analitica: sezione layout del documento
	*/
	private void mkLayout(ISASConnection dbc, Hashtable par, mergeDocument doc) throws SQLException {

		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String tipo = "";

		ht.put("#txt#", getConfStringField(dbc, "SINS", "ragione_sociale", "conf_txt"));
		ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
		ht.put("#data_fine#", getStringDate(par, "data_fine"));
		ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		/*
		try{
				String sel = "SELECT cognome,nome,tipo FROM operatori "+
					"WHERE codice = '"+(String)par.get("codope")+"'";
				debugMessage("FoEleSocEJB.getOperatore(): "+sel);
				ISASRecord dbcom = dbc.readRecord(sel);
				cognome=(String)dbcom.get("cognome");
				nome=(String)dbcom.get("nome");
				tipo=(String)dbcom.get("tipo");
		} catch(Exception e) {
			debugMessage("getOperatore("+dbc+", "+cognome+"): "+e);
		}
			ht.put("#operatore#", cognome + " " + nome);

		        if(tipo.equals("") && !par.get("figprof").equals("00"))
		              tipo=(String)par.get("figprof");
			ht.put("#tipo_ope#",  getFiguraProfessionale(tipo));*/
		//if(!par.get("figprof").equals("00"))
		tipo = (String) par.get("figprof");
		String presidio = "";
		if (!par.get("codpres").equals("")) {
			presidio = "PRESIDIO: " + getDescPresidio(dbc, par);
		}
		ht.put("#tipo_figprof#", getFiguraProfessionale(tipo));
		ht.put("#presidio#", presidio);
		doc.writeSostituisci("layout", ht);
	}
/*
	private Hashtable mkRecuperaDati(ISASConnection dbc, Hashtable par) throws SQLException {
		String punto = MIONOME + "mkLayoutPlan ";
		stampa(punto + "\t DATI INIZIO>" + par + "<\n ");
		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String tipo = "";

		ht.put("#infoAsl#", getConfStringField(dbc, "SINS", "ragione_sociale", "conf_txt"));
		ht.put("#info_settimana#", getStringDate(par, "data_fine"));
		String infoDate = su.getTodayDate("dd/MM/yyyy");
		Date dd = new Date();
		infoDate += "   " + dd.getHours() + ":" + dd.getMinutes() + ":" + dd.getSeconds();
		ht.put("#stampato_il#", infoDate);
		tipo = (String) par.get("figprof");
		String presidio = "";
		if (!par.get("codpres").equals("")) {
			presidio = "/PRESIDIO: " + getDescPresidio(dbc, par);
		}
		String zona = ISASUtil.getValoreStringa(par, "zona");
		if (!par.get("codpres").equals("")) {
			presidio = "/Zona: " + zona;
		}
		String tipoAgenda = getFiguraProfessionale(tipo);
		ht.put("#tipo_figprof#", tipoAgenda);
		ht.put("#presidio#", presidio);

		if (ISASUtil.valida(tipoAgenda)) {
			tipoAgenda = "Agenda " + tipoAgenda;
		}

		String data = ISASUtil.getValoreStringa(par, "data_inizio");
		int giorno = -1, mese = -1, anno = -1;
		int pos = data.indexOf("-");
		if (pos > 0) {
			anno = getIntero(data.substring(0, 4));
		}
		String val = data.substring(pos + 1);
		pos = val.indexOf("-");
		if (pos > 0) {
			mese = getIntero(val.substring(0, pos));
			giorno = getIntero(val.substring(pos + 1));
		}
		//		stampa(punto + "\t giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + "<\n");
		mese = (mese <= 0 ? 0 : mese - 1);
		//		int numeroSettimana = NumSettYear(anno, mese, giorno);
		//		System.out.println("stamp>" + getIntervalloSettimana(numeroSettimana, anno) + "<");

		Calendar gc = NumSettYear(anno, mese, giorno);
		int numeroSettimana = gc.get(Calendar.WEEK_OF_YEAR);
		anno = gc.get(Calendar.YEAR);

		String meseString = getMese(mese);
		ht.put("#mese#", meseString);
		ht.put("#anno#", anno + "");
		ht.put("#numSett#", numeroSettimana + "");

		ht.put("#tipo_agenda#", tipoAgenda);
		//		ht.put("#zona#", ISASUtil.getValoreStringa(par, "zona"));
		//		stampaDoc(punto, "layout", (Hashtable) ht.clone());
		//		doc.writeSostituisci("layout", (Hashtable) ht.clone());

		//		Calendar cal = getPrimoGiornoSettimana(numeroSettimana, anno);
		//		int inizioGiornoSettimana = cal.get(Calendar.DATE);
		//		Calendar calender = new GregorianCalendar(anno, mese, inizioGiornoSettimana);
		//		int lun = calender.get(Calendar.DAY_OF_MONTH);
		//		System.out.println("giorno>" + calender.get(Calendar.DAY_OF_MONTH));
		//		ht.put("#lungg#", lun + "");
		//
		//		calender = new GregorianCalendar(anno, mese, ++inizioGiornoSettimana);
		//		int mar = calender.get(Calendar.DAY_OF_MONTH);
		//		ht.put("#margg#", mar + "");
		//
		//		calender = new GregorianCalendar(anno, mese, ++inizioGiornoSettimana);
		//		int mer = calender.get(Calendar.DAY_OF_MONTH);
		//		ht.put("#mergg#", mer + "");
		//
		//		calender = new GregorianCalendar(anno, mese, ++inizioGiornoSettimana);
		//		int gio = calender.get(Calendar.DAY_OF_MONTH);
		//		ht.put("#giogg#", gio + "");
		//		calender = new GregorianCalendar(anno, mese, ++inizioGiornoSettimana);
		//		int ven = calender.get(Calendar.DAY_OF_MONTH);
		//		ht.put("#vengg#", ven + "");
		//		calender = new GregorianCalendar(anno, mese, ++inizioGiornoSettimana);
		//		int sab = calender.get(Calendar.DAY_OF_MONTH);
		//		ht.put("#sabgg#", sab + "");
		//		calender = new GregorianCalendar(anno, mese, ++inizioGiornoSettimana);
		//
		//		int dom = calender.get(Calendar.DAY_OF_MONTH);
		//		ht.put("#domgg#", dom + "");

		ht.put("#numSettFine#", "");
		ht.put("#annoFine#", "");
		String dataFine = ISASUtil.getValoreStringa(par, "data_fine");
		if (ISASUtil.valida(dataFine)) {
			giorno = -1;
			mese = -1;
			anno = -1;
			pos = dataFine.indexOf("-");
			if (pos > 0) {
				anno = getIntero(dataFine.substring(0, 4));
			}
			val = dataFine.substring(pos + 1);
			pos = val.indexOf("-");
			if (pos > 0) {
				mese = getIntero(val.substring(0, pos));
				giorno = getIntero(val.substring(pos + 1));
			}
			Calendar gcs = NumSettYear(anno, mese, giorno);
			int numeroSettimanaFine = gcs.get(Calendar.WEEK_OF_YEAR);
			anno = gc.get(Calendar.YEAR);
			ht.put("#numSettFine#", numeroSettimanaFine + "");
			ht.put("#annoFine#", anno + "");
		} else {
			stampa(punto + "\t NON CALCOLO LA DATA DI FINE ");
		}
		return ht;
	}
*/
	private Hashtable mkLayoutPlan(ISASConnection dbc, Hashtable par, mergeDocument doc) throws SQLException {
		String punto = MIONOME + "mkLayoutPlan ";
		stampa(punto + "\t DATI INIZIO>" + par + "<\n ");
		ServerUtility su = new ServerUtility();
		Hashtable ht = new Hashtable();
		String tipo = "";
		ht.put("#infoAsl#", getConfStringField(dbc, "SINS", "ragione_sociale", "conf_txt"));
		ht.put("#info_settimana#", getStringDate(par, "data_fine"));
		String infoDate = su.getTodayDate("dd/MM/yyyy");
		Date dd = new Date();
		infoDate += "   " + dd.getHours() + ":" + dd.getMinutes() + ":" + dd.getSeconds();
		ht.put("#stampato_il#", infoDate);
		tipo = (String) par.get("figprof");
		String presidio = "";
		if (!par.get("codpres").equals("")) {
			presidio = "/PRESIDIO: " + getDescPresidio(dbc, par);
		}
		String zona = ISASUtil.getValoreStringa(par, "zona");
		if (!par.get("zona").equals("")) {
			presidio = "/Zona: " + zona;
		}
		String tipoAgenda = getFiguraProfessionale(tipo);
		ht.put("#tipo_figprof#", tipoAgenda);
		ht.put("#presidio#", presidio);

		if (ISASUtil.valida(tipoAgenda)) {
			tipoAgenda = "Agenda " + tipoAgenda;
		}
		String data = ISASUtil.getValoreStringa(par, "data_inizio");
		ht.put("#periodoInfostmp#", getIntervalloSettimana(data));
//bargi ht.put("#periodoInfostmp#", getIntervalloSettimana(numeroSettimana, anno) + "");
		
		String dtF=cag.getLastDayWeek(data, 6);
		String mese=dt.getMeseSt(dtF);
		String anno=""+dt.getAnno(dtF);		
		//String meseString = getMese(mese);
		ht.put("#mese#", mese);
		ht.put("#anno#", anno);
		GregorianCalendar gc=dt.getGreg(dtF);
		int numeroSettimana=gc.get(Calendar.WEEK_OF_YEAR);
		ht.put("#numSett#", numeroSettimana + "");
		ht.put("#tipo_agenda#", tipoAgenda);
		stampaDoc(punto, "layout", (Hashtable) ht.clone());
		doc.writeSostituisci("layout", (Hashtable) ht.clone());
		ht.put("#numSettFine#", "");
		ht.put("#annoFine#", "");
		String dataFine = ISASUtil.getValoreStringa(par, "data_fine");
		String dtF2=cag.getLastDayWeek(dataFine, 6);
		stampa(punto + "par>" + par + "< dataFine>" + dataFine + "<");

		GregorianCalendar gc2=dt.getGreg(dtF);
		int numeroSettimanaFine=gc2.get(Calendar.WEEK_OF_YEAR);
		ht.put("#numSettFine#", numeroSettimanaFine + "");
		ht.put("#annoFine#", anno + "");
		stampa(punto + " restituisco>" + ht + "<\n");
		return ht;
	}
	
	
	

	/**   
	* Crea un testo contenente gli estremi di una particolare coppia 
	* (settimana, anno).
	* @param _settimana e' il numero della settimana dell'anno
	* @param _anno e' il numero dell'anno in cui "esaminare" la settimana
	* @return una stringa del tipo "da 25/11/2002 a 1/12/2002"
	* rappresentante gli estremi della settimana passata come parametro.
	* Come primo giorno della settimana voglio considerare Lunedi' (Monday).
	* Ad esempio, per la settimana 48, dell'anno 2002, 
	* esce "da 25/11/2002 a 1/12/2002" (25/11=Lunedi' 1/12=domenica).
	*/
	private Calendar getPrimoGiornoSettimana(int _settimana, int _anno) {
		Calendar cal = Calendar.getInstance();
		String punto = MIONOME + "getPrimoGiornoSettimana ";
		stampa(punto + "\t dati che mi arrivano>" + _settimana + "< anno>" + _anno + "<");
		//Configuro la settimana e l'anno passati
		cal.set(Calendar.YEAR, _anno);
		cal.set(Calendar.WEEK_OF_YEAR, _settimana);

		//Cambio la data di "cal" con il primo giorno della settimana voluta
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		//		risultato.append("da " + cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
		//
		//		//Cambio la data di "cal" con l'ultimo giorno della settimana voluta
		//		cal.add(Calendar.DATE, 6);

		return cal;
	}

	/**
	* Crea un testo contenente gli estremi di una particolare coppia 
	* (settimana, anno).
	* @param _settimana e' il numero della settimana dell'anno
	* @param _anno e' il numero dell'anno in cui "esaminare" la settimana
	* @return una stringa del tipo "da 25/11/2002 a 1/12/2002"
	* rappresentante gli estremi della settimana passata come parametro.
	* Come primo giorno della settimana voglio considerare Lunedi' (Monday).
	* Ad esempio, per la settimana 48, dell'anno 2002, 
	* esce "da 25/11/2002 a 1/12/2002" (25/11=Lunedi' 1/12=domenica).
	*/
/*	public static String getIntervalloSettimana(int _settimana, int _anno) {
		StringBuffer risultato = new StringBuffer("");
		Calendar cal = Calendar.getInstance();

		//Configuro la settimana e l'anno passati
		cal.set(Calendar.YEAR, _anno);
		cal.set(Calendar.WEEK_OF_YEAR, _settimana);

		//Cambio la data di "cal" con il primo giorno della 
		//settimana voluta
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		risultato.append("da " + cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

		//Cambio la data di "cal" con l'ultimo giorno della 
		//settimana voluta
		cal.add(Calendar.DATE, 6);
		risultato.append(" a " + cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));

		return risultato.toString();
	}*/
	private  String getIntervalloSettimana(String dataI) {
		String dataInizioSett=cag.getFirstDayWeek(dataI, 6);
		String dataFineSett=cag.getLastDayWeek(dataI, 6);
		StringBuffer risultato = new StringBuffer("");
		risultato.append("da "+dataInizioSett);
		risultato.append(" a " +dataFineSett);
		return risultato.toString();
	}/*
	private String getMese(int mese) {
		String punto = MIONOME + "getMese ";
		mese += 1;
		Hashtable ht = new Hashtable();
		ht.put("1", "Gennaio");
		ht.put("2", "Febbraio");
		ht.put("3", "Marzo");
		ht.put("4", "Aprile");
		ht.put("5", "Maggio");
		ht.put("6", "Giugno");
		ht.put("7", "Luglio");
		ht.put("8", "Agosto");
		ht.put("9", "Settembre");
		ht.put("10", "Ottobre");
		ht.put("11", "Novembre");
		ht.put("12", "Dicembre");
		String str = "";
		try {
			str = (String) ht.get(mese + "");
		} catch (Exception e) {
			stampa(punto + "Errore nel recuperare il mese");
		}

		return str;
	}
*/
	private static int getIntero(String numero) {
		int valore = -1;

		try {
			valore = Integer.parseInt(numero);
		} catch (Exception e) {
		}
		return valore;
	}

	/**
	* stampa sintetica: sezione riga tabella
	*/
	private void faiRigaAssistito(ISASConnection dbc, ISASRecord dbr, mergeDocument doc, Hashtable par) {
		String int_contatto = "";
		Hashtable ht = new Hashtable();
		try {
			ht.put("#assistito#", (String) dbr.get("assistito"));
			ht.put("#indirizzo#", (String) dbr.get("indirizzo"));
			String telefono =recuperaTelefono(dbr);
			ht.put("#telefono#", telefono);
			ht.put("#data#", getDateField(dbr, "ag_data"));
			ht.put("#nome_campanello#", recuperaNomeCampanello(dbr));
			if (par.get("med").equals("M"))
				ht.put("#mmg_dati#", recuperaDatiMMG(dbc, dbr));			
			else
				ht.put("#mmg_dati#", "");
			int eta=0;
            //calcolo eta'
            String mydate="";
            String data_stampa=this.getjdbcDate();
            if (dbr.get("data_nasc")!=null)
            {
                mydate=((java.sql.Date)dbr.get("data_nasc")).toString();
                if (!mydate.equals(""))
                  eta=this.ConvertData(mydate, data_stampa);
                else eta=0;
            }
            ht.put("#eta#","Età: "+eta);
		} catch (Exception e) {
			ht.put("#assistito#", "*** ERRORE ***");
			ht.put("#indirizzo#", " ");
			ht.put("#data#", " ");
			ht.put("#eta#", " ");
		}
		
			doc.writeSostituisci("rigaAssistito", ht);
		
	}
	
	private void faiRigaAssistitoSin(ISASConnection dbc, ISASRecord dbr, mergeDocument doc, Hashtable par) {
		String int_contatto = "";
		Hashtable ht = new Hashtable();
		try {
			ht.put("#assistito#", (String) dbr.get("assistito"));
			ht.put("#indirizzo#", (String) dbr.get("indirizzo"));
			String telefono =recuperaTelefono(dbr);
			ht.put("#telefono#", telefono);
			ht.put("#data#", getDateField(dbr, "ag_data"));
			ht.put("#nome_campanello#", recuperaNomeCampanello(dbr));
			if (par.get("med").equals("M"))
				ht.put("#mmg_dati#", recuperaDatiMMG(dbc, dbr));
			else
				ht.put("#mmg_dati#", "");
			int eta=0;
            //calcolo eta'
            String mydate="";
            String data_stampa=this.getjdbcDate();
            if (dbr.get("data_nasc")!=null)
            {
                mydate=((java.sql.Date)dbr.get("data_nasc")).toString();
                if (!mydate.equals(""))
                  eta=this.ConvertData(mydate, data_stampa);
                else eta=0;
            }
            ht.put("#eta#","Età: "+eta);
            
            String selectFrequenza = " select pi_freq from piano_accessi"+
					 " where n_cartella =" + dbr.get("ag_cartella").toString()+
					 " and pa_tipo_oper = '" + (String) dbr.get("ag_tipo_oper") + "'"+
					 " and cod_obbiettivo = '" + (String) dbr.get("cod_obbiettivo") + "'"+
					 " and pi_data_inizio <= "+ formatDate(dbc, dbr.get("ag_data").toString())+
					 //" and pi_data_fine >= "+ formatDate(dbc, dbr.get("ag_data").toString())+
					 " AND (pi_data_fine >= "+ formatDate(dbc, dbr.get("ag_data").toString()) +
					 " OR PI_DATA_FINE is null)"+
					 " and pi_prest_cod = '" + (String) dbr.get("ap_prest_cod") + "'"+
					// " and pa_data = " + formatDate(dbc, dbr.get("ag_data").toString())+
					 " and n_intervento=" + dbr.get("n_intervento").toString();

            ISASRecord dbr_freq = dbc.readRecord(selectFrequenza);
            if (dbr_freq!=null){
            	if (dbr_freq.get("pi_freq")!=null && !dbr_freq.get("pi_freq").equals(""))
            		ht.put("#frequenza#", ISASUtil.getDecode(dbc,"tab_voci", "tab_cod", "tab_val", "FREQAC",dbr_freq.get("pi_freq"), "tab_descrizione"));
            	else 
            		ht.put("#frequenza#","");
            }else ht.put("#frequenza#","");
            	

            ht.put("#op_ref#",  getDescOperatori(dbc, (String) dbr.get("ag_oper_ref")));
            
            ht.put("#quant#", dbr.get("numeroprestazioni"));
            
		} catch (Exception e) {
			e.printStackTrace();
			ht.put("#assistito#", "*** ERRORE ***");
			ht.put("#indirizzo#", " ");
			ht.put("#data#", " ");
			ht.put("#eta#", " ");
		}
		if (par.get("mod").equals("OP"))
			doc.writeSostituisci("rigaAssistito", ht);
		if (par.get("mod").equals("PR"))
			doc.writeSostituisci("rigaAssistito_op", ht);
	}

	private Object recuperaDatiMMG(ISASConnection dbc, ISASRecord dbr) {
		String datiMMG ="";
		String codMedico = ISASUtil.getValoreStringa(dbr, "cod_med");
		if (ISASUtil.valida(codMedico)) {
			// " -- mecogn, menome,metel_casa, metel_cell
			String query = "select * from medici where mecodi = '" + codMedico + "' ";
			try {
				ISASRecord dbrMedici = dbc.readRecord(query);
				String testo = Labels.getLabel("operatore.stampe");
				datiMMG = ISASUtil.getValoreStringa(dbrMedici, "mecogn") + " " + ISASUtil.getValoreStringa(dbrMedici, "menome");
				if (ISASUtil.valida(datiMMG)) {
					datiMMG = testo+": " + datiMMG;
					String tel = ISASUtil.getValoreStringa(dbrMedici, "metel_casa");
					String cel = ISASUtil.getValoreStringa(dbrMedici, "metel_cell");
					if (ISASUtil.valida(tel)&& ISASUtil.valida(cel)){
						tel+=ISASUtil.valida(cel) ? ", ":"";
					}
					tel+=cel;
					if (ISASUtil.valida(tel)){
						testo = Labels.getLabel("cartella.domicilio.telefono");
						datiMMG+= testo+": "+tel;
					}
				}else {
					datiMMG = testo+": "+ CTS_NO_DATI;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return datiMMG;
	}
	

	private Object recuperaNomeCampanello(ISASRecord dbr) {
		String nomeCampanello = ISASUtil.getValoreStringa(dbr, "nome_camp");
		String testo = Labels.getLabel("cartella.reperibilita.nomeCampanello");
		nomeCampanello = testo +": "+ (ISASUtil.valida(nomeCampanello) ?  nomeCampanello : CTS_NO_DATI);
		return nomeCampanello;
	}

	private String recuperaTelefono(ISASRecord dbr) {
		 String telefono = ISASUtil.getValoreStringa(dbr, "telefono1");
		 telefono += (ISASUtil.valida(telefono)? " - " : "") + ISASUtil.getValoreStringa(dbr, "telefono2");

		 //telefono = "Telefono: "+ (ISASUtil.valida(telefono) ? telefono : CTS_NO_DATI );
		 telefono = (ISASUtil.valida(telefono) ? telefono : CTS_NO_DATI );
		 
		return telefono;
	}

	private void faiRigaAssistitoPlan(ISASConnection dbc, ISASRecord dbr, mergeDocument doc, Hashtable par) {
		String punto = MIONOME + "faiRigaAssistitoPlan ";
		Hashtable ht = new Hashtable();
		try {
			//bargi 08/03/2013
			String note= ISASUtil.getValoreStringa(dbr, "note");
			//			anagra_c.indirizzo, anagra_c.citta, anagra_c.cap,
			//			dom_indiriz, anagra_c.dom_citta, anagra_c.dom_localita
			String cart= ISASUtil.getValoreStringa(dbr, "ag_cartella");
			String indirizzo = "";

			indirizzo = ISASUtil.getValoreStringa(dbr, "dom_indiriz");
			//			indirizzo += (ISASUtil.valida(indirizzo) ? " " : "");
			//			indirizzo += ISASUtil.getValoreStringa(dbr, "dom_citta");
			//			indirizzo += (ISASUtil.valida(indirizzo) ? " " : "");
			//			indirizzo += ISASUtil.getValoreStringa(dbr, "dom_localita");
			if (!ISASUtil.valida(indirizzo)) {
				indirizzo = ISASUtil.getValoreStringa(dbr, "indirizzo");
				//				indirizzo += (ISASUtil.valida(indirizzo) ? " " : "");
				//				indirizzo += ISASUtil.getValoreStringa(dbr, "cap");
				//				indirizzo += (ISASUtil.valida(indirizzo) ? " " : "");
				//				indirizzo += ISASUtil.getValoreStringa(dbr, "citta");
			}

//			String telefono = ISASUtil.getValoreStringa(dbr, "telefono1");
//			String tele2 = ISASUtil.getValoreStringa(dbr, "telefono2");
//			if (ISASUtil.valida(telefono) && ISASUtil.valida(tele2)) {
//				telefono += " - ";
//			}
//			telefono += tele2;
//
//			telefono = (ISASUtil.valida(telefono) ? "Telefono: " + telefono : "");

			String assistito = ISASUtil.getValoreStringa(dbr, "ass_cogn");
			assistito += ISASUtil.valida(assistito) ? " " : "";
			assistito += ISASUtil.getValoreStringa(dbr, "ass_nome");

//			String nomeCampanello = ISASUtil.getValoreStringa(dbr, "nome_camp");
//			if (ISASUtil.valida(nomeCampanello)) {
//				nomeCampanello = "Campanello: " + nomeCampanello;
//			}

			String citta = ISASUtil.getValoreStringa(dbr, "comune_descr");
			ht.put("#assistito#",cart+" "+ assistito);
			ht.put("#indirizzo#", indirizzo);
//			ht.put("#telefono#", telefono);
			String telefono =recuperaTelefono(dbr);
			ht.put("#telefono#", telefono);
			
			ht.put("#campanello#", recuperaNomeCampanello(dbr));
			
			if (par.get("med").equals("M"))
				ht.put("#mmg_dati#", recuperaDatiMMG(dbc, dbr));			
			else
				ht.put("#mmg_dati#", "");
			
//			ht.put("#campanello#", nomeCampanello);
			ht.put("#citta#", citta);
			ht.put("#note#", note);

		} catch (Exception e) {
			ht.put("#assistito#", "");
			ht.put("#indirizzo#", "");
			ht.put("#telefono#", "");
			ht.put("#campanello#", "");
			ht.put("#citta#", "");
			ht.put("#note#", "");
		}

		stampaDoc(punto, "rigaAssistito", ht);
		doc.writeSostituisci("rigaAssistito", ht);
		
		
	}
	
	private void faiRigaAssistitoPlanSin(ISASConnection dbc, ISASRecord dbr, mergeDocument doc, Hashtable par) {
		String punto = MIONOME + "faiRigaAssistitoPlanSin ";
		Hashtable ht = new Hashtable();
		try {
			//bargi 08/03/2013
			String note= ISASUtil.getValoreStringa(dbr, "note");
			String cart= ISASUtil.getValoreStringa(dbr, "ag_cartella");
			String data_nascita= ISASUtil.getDateField(dbr, "data_nasc");
			String indirizzo = "";

			indirizzo = ISASUtil.getValoreStringa(dbr, "dom_indiriz");
			
			if (!ISASUtil.valida(indirizzo)) {
				indirizzo = ISASUtil.getValoreStringa(dbr, "indirizzo");
			}

			String assistito = ISASUtil.getValoreStringa(dbr, "ass_cogn");
			assistito += ISASUtil.valida(assistito) ? " " : "";
			assistito += ISASUtil.getValoreStringa(dbr, "ass_nome");

			String citta = ISASUtil.getValoreStringa(dbr, "comune_descr");
			ht.put("#assistito#",cart+" "+ assistito);
			ht.put("#data_nascita#",data_nascita);
			ht.put("#indirizzo#", indirizzo);
			String telefono =recuperaTelefono(dbr);
			ht.put("#telefono#", telefono);
			
			ht.put("#campanello#", recuperaNomeCampanello(dbr));
			if (par.get("med").equals("M"))
				ht.put("#mmg_dati#", recuperaDatiMMG(dbc, dbr));
			else ht.put("#mmg_dati#", "");

			ht.put("#citta#", citta);
			ht.put("#note#", note);

		} catch (Exception e) {
			ht.put("#assistito#", "");
			ht.put("#indirizzo#", "");
			ht.put("#telefono#", "");
			ht.put("#campanello#", "");
			ht.put("#citta#", "");
			ht.put("#note#", "");
		}

		stampaDoc(punto, "rigaAssistito", ht);
		doc.writeSostituisci("rigaAssistito", ht);
	}

	/**
	* stampa sintetica: sezione riga tabella
	*/
	private void faiRigaIntervento(ISASRecord dbr, mergeDocument doc) {
		Hashtable ht = new Hashtable();
		try {
			if (((String) dbr.get("ap_stato")).equals("1")) {
				ht.put("#check#", "x");
			} else
				ht.put("#check#", "");
			ht.put("#prestazione#", (String) dbr.get("prestazione"));
			ht.put("#quantita#", (Integer) dbr.get("ap_prest_qta"));
			if (((Integer) dbr.get("ag_orario")).intValue() == 0)
				ht.put("#matt_pom#", "M");
			else
				ht.put("#matt_pom#", "P");

		} catch (Exception e) {
			ht.put("#check#", " ");
			ht.put("#prestazione#", " ");
			ht.put("#quantita#", " ");
			ht.put("#matt_pom#", " ");
		}

		doc.writeSostituisci("prestazione", ht);
	}
	
	/*private void faiRigaInterventoSin(ISASConnection dbc, ISASRecord dbr, mergeDocument doc, Hashtable par) {
		Hashtable ht = new Hashtable();
		try {
			String selectFrequenza = " select pi_freq from piano_accessi"+
									 " where n_cartella =" + dbr.get("ag_cartella").toString()+
									 " and pa_tipo_oper = '" + (String) dbr.get("ag_tipo_oper") + "'"+
									 " and cod_obbiettivo = '" + (String) dbr.get("cod_obbiettivo") + "'"+
									 " and pa_data = " + formatDate(dbc, dbr.get("ag_data").toString())+
									 " and n_intervento=" + dbr.get("n_intervento").toString();
			
			ISASRecord dbr_freq = dbc.readRecord(selectFrequenza);
			if (dbr_freq!=null)
				ht.put("#frequenza#", ISASUtil.getDecode(dbc,"tab_voci", "tab_cod", "tab_val", "FREQAC",dbr_freq.get("pi_freq"), "tab_descrizione"));
			
			ht.put("#op_ref#",  getDescOperatori(dbc, (String) dbr.get("ag_oper_ref")));
			ht.put("#quant#",   (Integer) dbr.get("pres"));
		
		} catch (Exception e) {
			
			ht.put("#quantita#", " ");
			ht.put("#op_ref#", " ");
			
		}
		if (par.get("mod").equals("OP"))
			doc.writeSostituisci("prestazione", ht);
		else if (par.get("mod").equals("PR"))
			doc.writeSostituisci("prestazione_op", ht);
	}*/

	/**
	* stampa sintetica: sezione inizio tabella
	*/
	private void mkInizioTabella(ISASConnection dbc, mergeDocument doc, ISASRecord dbr) throws SQLException {
		Hashtable ht = new Hashtable();
		String cognome = "";
		String nome = "";
		String tipo_oper = "";
		try {
			if (dbr.get("ag_tipo_oper") != null)
				tipo_oper = (String) dbr.get("ag_tipo_oper");
			
			
			ht.put("#tipo_ope#", getFiguraProfessionale(tipo_oper));
			ht.put("#operatore#", (String) dbr.get("ag_oper_ref") + " - " + getDescOperatori(dbc, (String) dbr.get("ag_oper_ref")));

		} catch (Exception e) {
			ht.put("#tipo_ope#", "*** ERRORE ***");
			ht.put("#operatore#", " ");
		}
		
		doc.writeSostituisci("operatore", ht);
		doc.write("iniziotab");
	}
	
	private void mkInizioTabellaSin(ISASConnection dbc, mergeDocument doc, ISASRecord dbr, Hashtable par) throws SQLException {
		Hashtable ht = new Hashtable();
		String cognome = "";
		String nome = "";
		String tipo_oper = "";
		try {
			if (dbr.get("ag_tipo_oper") != null)
				tipo_oper = (String) dbr.get("ag_tipo_oper");
			String cartella = dbr.get("ag_cartella").toString();
			String data = dbr.get("ag_data").toString();
			Calendar data_giorno = this.recuperaCalendar(dbr.get("ag_data").toString());
			
			int giorno = data_giorno.get(Calendar.DAY_OF_WEEK);
			if (giorno == 1)
				ht.put("#giorno#", "Domenica: " + getDateField(dbr, "ag_data"));
			else if (giorno == 2)
				ht.put("#giorno#", "Lunedì: " + getDateField(dbr, "ag_data"));
			else if (giorno == 3)
				ht.put("#giorno#", "Martedì: " + getDateField(dbr, "ag_data"));
			else if (giorno == 4)
				ht.put("#giorno#", "Mercoledì: " + getDateField(dbr, "ag_data"));
			else if (giorno == 5)
				ht.put("#giorno#", "Giovedì: " + getDateField(dbr, "ag_data"));
			else if (giorno == 6)
				ht.put("#giorno#", "Venerdì: " + getDateField(dbr, "ag_data"));
			else if (giorno == 7)
				ht.put("#giorno#", "Sabato: " + getDateField(dbr, "ag_data"));
			ht.put("#tipo_ope#", getFiguraProfessionale(tipo_oper));
			ht.put("#operatore#", (String) dbr.get("ag_oper_ref") + " - " + getDescOperatori(dbc, (String) dbr.get("ag_oper_ref")));
			ht.put("#sede#", dbr.get("despres").toString());
			
			ht.put("#num_pres#", dbr.get("numeroprestazioni"));
			String selectPrelievi = " select conf_txt from conf where conf_kproc='SINS' and conf_key='PRESTPREL'";
			ISASRecord dbr_prelievi = dbc.readRecord(selectPrelievi);
			String prelievi = (String) dbr_prelievi.get("conf_txt");																		
			String query_prel = getSelectSinteticaPrelievi(dbc, par,prelievi, data, cartella);
			ISASRecord dbr_prel = dbc.readRecord(query_prel);
			if (dbr_prel!=null){
				if (dbr_prel.get("numeroprelievi")!=null && !dbr_prel.get("numeroprelievi").equals(""))
					ht.put("#prel#", dbr_prel.get("numeroprelievi"));
				else
					ht.put("#prel#", "0");
			}else ht.put("#prel#", "0");
			
			
		} catch (Exception e) {
			ht.put("#tipo_ope#", "*** ERRORE ***");
			ht.put("#operatore#", " ");
		}
		if (par.get("mod").equals("OP"))
			doc.writeSostituisci("operatore", ht);
		else if (par.get("mod").equals("PR"))
			doc.writeSostituisci("sede", ht);
		
		if (par.get("mod").equals("OP"))
			doc.write("iniziotab");
		else if (par.get("mod").equals("PR"))
			doc.write("iniziotab_op");
	}
	
	/*
	private Calendar NumSettYear(int aaaa, int mm, int gg) {
		String punto = MIONOME + "NumSettYear \t";
		Calendar giorno = Calendar.getInstance();//
		giorno.set(Calendar.DAY_OF_MONTH, gg);
		giorno.set(Calendar.MONTH, mm);
		giorno.set(Calendar.YEAR, aaaa);
		int NumSettYear = giorno.get(Calendar.WEEK_OF_YEAR);
		int anno = giorno.get(Calendar.YEAR);

		Calendar cal = Calendar.getInstance();
		stampa(punto + "\t dati in ingresso: " + gg + "/" + mm + "/" + aaaa + " > num.settimana=" + NumSettYear + " anno=" + anno);
		//Configuro la settimana e l'anno passati
		cal.set(Calendar.YEAR, anno);
		cal.set(Calendar.WEEK_OF_YEAR, NumSettYear);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		stampa(punto + " inizio while = [" + giorno.get(Calendar.DAY_OF_MONTH) + "/" + 
			giorno.get(Calendar.MONTH) + "/" + giorno.get(Calendar.YEAR) + "] [" +
			cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" +
			cal.get(Calendar.YEAR) + "]\n");
		int differenzaGiorno = 0;
		while (cal.after(giorno) && differenzaGiorno <= 7) {//|| inizioGiorno.equals(fineGiorno)) {
			System.out.println(punto + " Giorno>" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + 
				cal.get(Calendar.YEAR) + " differenzaGiorno=" + differenzaGiorno + "<");
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
			differenzaGiorno++;
		}
		System.out.println(punto + " Giorno>" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + 
			cal.get(Calendar.YEAR) + " differenzaFinale=" + differenzaGiorno + "<");
		if (differenzaGiorno > 7) {
			anno = anno - 1;
		}
		//		stampa("differenzaGiorni>" + differenzaGiorno + " danno>" + anno + "<\n");
		cal.set(Calendar.YEAR, anno);
		cal.set(Calendar.WEEK_OF_YEAR, NumSettYear);
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		stampa(punto + " Primo giorno Recuperato>" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/"
				+ cal.get(Calendar.YEAR) + "<" + "numerosettimana>" + cal.get(Calendar.WEEK_OF_YEAR) + "<");
		return cal;
	}*/
/*
	private Calendar NumSettYearold(int aaaa, int mm, int gg) {
		String punto = MIONOME + "NumSettYear ";
		stampa(punto + "dati che mi arrivano anno>" + aaaa + "< mm>" + mm + "< gg>" + gg + "<");

		GregorianCalendar giorno = new GregorianCalendar(aaaa, mm, gg);
		int NumSettYear = giorno.get(Calendar.WEEK_OF_YEAR);
		stampa(punto + "WEEK_OF_YEAR erog: " + NumSettYear + "<");
		int anno = giorno.get(Calendar.YEAR);
		stampa("YEAR erog: " + anno + "<");

		Calendar cal = Calendar.getInstance();
		stampa(punto + "\t dati che mi arrivano>" + NumSettYear + "< anno>" + anno + "<");
		//Configuro la settimana e l'anno passati
		cal.set(Calendar.YEAR, anno);
		cal.set(Calendar.WEEK_OF_YEAR, NumSettYear);

		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		int differenzaGiorno = 0;
		while (cal.after(giorno) && differenzaGiorno <= 7) {//|| giorno.equals(fineGiorno)) {
			System.out.println(punto + " giorno>" + cal.get(Calendar.DAY_OF_MONTH) + "<" + "<mese>" + cal.get(Calendar.MONTH)
					+ "< differenzaGiorno>" + differenzaGiorno + "<");
			cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
			differenzaGiorno++;
		}
		stampa("differenzaGiorni>" + differenzaGiorno + "<anno>" + anno + "<");
		if (differenzaGiorno > 7) {
			anno = anno - 1;
		}

		cal.set(Calendar.WEEK_OF_YEAR, NumSettYear);
		cal.set(Calendar.YEAR, anno);

		stampa(" fine differenzaGiorni>" + differenzaGiorno + " danno>" + anno + "<" + "annoYear>" + cal.get(Calendar.YEAR) + " sett>"
				+ cal.get(Calendar.WEEK_OF_YEAR) + "<");

		return cal;
	}
*/
	private void mkInizioTabellaPlan(ISASConnection dbc, mergeDocument doc, ISASRecord dbr, Hashtable dati) throws SQLException {
		String punto = MIONOME + "mkInizioTabellaPlan ";
		//		stampa(punto + " Dati Che mi arrivano>" + dati + "<");
		Hashtable ht = new Hashtable();
		String tipo_oper = "";
		try {
			if (dbr.get("ag_tipo_oper") != null)
				tipo_oper = (String) dbr.get("ag_tipo_oper");

			ht.put("#tipo_ope#", getFiguraProfessionale(tipo_oper));
			ht.put("#operatore#", (String) dbr.get("ag_oper_ref") + " - " + getDescOperatori(dbc, (String) dbr.get("ag_oper_ref")));

			String numeroSettimana = ISASUtil.getValoreStringa(dati, "#numSett#");
		//	String mese = ISASUtil.getValoreStringa(dati, "#mese#");

			String anno = ISASUtil.getValoreStringa(dati, "#anno#");
			//			anno = recuperaAnnoSettimana(numeroSettimana, anno);

			//			String infoSettimana = "Settimana nr. " + numeroSettimana + " " + mese;//+ " " + anno;

			String infoSettimana = "Settimana nr. " + numeroSettimana + " del " + anno;

			ht.put("#info_settimana#", infoSettimana);
			ht.put("#tipo_figprof#", ISASUtil.getValoreStringa(dati, "#tipo_figprof#"));
			ht.put("#presidio#", ISASUtil.getValoreStringa(dati, "#presidio#"));
			ht.put("#stampato_il#", ISASUtil.getValoreStringa(dati, "#stampato_il#"));

		//	bargi ht.put("#zona#", ISASUtil.getValoreStringa(dati, "#zona#"));
			ht.put("#zona#", ISASUtil.getValoreStringa(dati, "#presidio#"));
			//			#tipo_ope#/#presidio# #zona_operatore#   #stampato_il# 
		//bargi 08/03/2013 	ht.put("#operatore#", ISASUtil.getValoreStringa(dbr, "cognome"));
			ht.put("#operatore#",ISASUtil.getValoreStringa(dbr, "ag_oper_ref") + " - " + ISASUtil.getValoreStringa(dbr, "cognome"));

			ht.put("#lungg#", ISASUtil.getValoreStringa(dati, "#lungg#"));
			ht.put("#margg#", ISASUtil.getValoreStringa(dati, "#margg#"));
			ht.put("#mergg#", ISASUtil.getValoreStringa(dati, "#mergg#"));
			ht.put("#giogg#", ISASUtil.getValoreStringa(dati, "#giogg#"));
			ht.put("#vengg#", ISASUtil.getValoreStringa(dati, "#vengg#"));
			ht.put("#sabgg#", ISASUtil.getValoreStringa(dati, "#sabgg#"));
			ht.put("#domgg#", ISASUtil.getValoreStringa(dati, "#domgg#"));

		} catch (Exception e) {
			ht.put("#tipo_ope#", "*** ERRORE ***");
			ht.put("#operatore#", " ");
		}

		//		#info_settimana# #tipo_ope#/#presidio#   #stampato_il# 	#zona# - #operatore#

		stampaDoc(punto, "operatore", ht);
		doc.writeSostituisci("operatore", ht);
	}
	
	private void mkInizioTabellaPlanSin(ISASConnection dbc, mergeDocument doc, ISASRecord dbr, Hashtable dati, Hashtable par) throws SQLException {
		String punto = MIONOME + "mkInizioTabellaPlanSin ";
		
		Hashtable ht = new Hashtable();
		String tipo_oper = "";
		try {
			if (dbr.get("ag_tipo_oper") != null)
				tipo_oper = (String) dbr.get("ag_tipo_oper");

			ht.put("#tipo_ope#", getFiguraProfessionale(tipo_oper));
			ht.put("#operatore#", (String) dbr.get("ag_oper_ref") + " - " + getDescOperatori(dbc, (String) dbr.get("ag_oper_ref")));

			String numeroSettimana = ISASUtil.getValoreStringa(dati, "#numSett#");

			String anno = ISASUtil.getValoreStringa(dati, "#anno#");
			
			String periodo =  ndf.formDate(ISASUtil.getValoreStringa(par, "dataIniziSettimana"),"gg/mm/aaaa");
			periodo += " - " + ndf.formDate(	ISASUtil.getValoreStringa(par, "dataFineSettimana"),"gg/mm/aaaa");				
			ht.put("#info_settimana#", "Settimana " + periodo);
			//ht.put("#info_settimana#", infoSettimana);
			ht.put("#tipo_figprof#", ISASUtil.getValoreStringa(dati, "#tipo_figprof#"));
			ht.put("#presidio#", ISASUtil.getValoreStringa(dati, "#presidio#"));
			ht.put("#stampato_il#", ISASUtil.getValoreStringa(dati, "#stampato_il#"));

		//	bargi ht.put("#zona#", ISASUtil.getValoreStringa(dati, "#zona#"));
			ht.put("#zona#", ISASUtil.getValoreStringa(dati, "#presidio#"));
			//			#tipo_ope#/#presidio# #zona_operatore#   #stampato_il# 
		//bargi 08/03/2013 	ht.put("#operatore#", ISASUtil.getValoreStringa(dbr, "cognome"));
			ht.put("#operatore#",ISASUtil.getValoreStringa(dbr, "ag_oper_ref") + " - " + ISASUtil.getValoreStringa(dbr, "cognome"));

			ht.put("#lungg#", ISASUtil.getValoreStringa(dati, "#lungg#"));
			ht.put("#margg#", ISASUtil.getValoreStringa(dati, "#margg#"));
			ht.put("#mergg#", ISASUtil.getValoreStringa(dati, "#mergg#"));
			ht.put("#giogg#", ISASUtil.getValoreStringa(dati, "#giogg#"));
			ht.put("#vengg#", ISASUtil.getValoreStringa(dati, "#vengg#"));
			ht.put("#sabgg#", ISASUtil.getValoreStringa(dati, "#sabgg#"));
			ht.put("#domgg#", ISASUtil.getValoreStringa(dati, "#domgg#"));

		} catch (Exception e) {
			ht.put("#tipo_ope#", "*** ERRORE ***");
			ht.put("#operatore#", " ");
		}

		//		#info_settimana# #tipo_ope#/#presidio#   #stampato_il# 	#zona# - #operatore#

		stampaDoc(punto, "operatore", ht);
		doc.writeSostituisci("operatore", ht);
	}

	//	private String recuperaAnnoSettimana(String numeroSettimana, String anno) {
	//
	//		String valAnno = "";
	//
	//		Calendar cal = Calendar.getInstance();
	//
	//		//Configuro la settimana e l'anno passati
	//		cal.set(Calendar.YEAR, recuperaNumero(anno));
	//		cal.set(Calendar.WEEK_OF_YEAR, recuperaNumero(numeroSettimana));
	//
	//		//Cambio la data di "cal" con il primo giorno della 
	//		//settimana voluta
	//		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
	//		cal.add(Calendar.DATE, 6);
	//		//		risultato.append(" a " + cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));
	//		valAnno = cal.get(Calendar.YEAR) + "";
	//
	//		return valAnno;
	//	}
/*
	private int recuperaNumero(String anno) {
		String punto = MIONOME + "recuperaNumero ";
		int _anno = 0;
		try {
			_anno = Integer.parseInt(anno);
		} catch (Exception e) {
			stampa(punto + " Errore nel calcolo dell'anno>" + anno + "<");
		}
		return _anno;
	}
*/
	private void stampaDoc(String punto, String sezione) {
		//		stampaDoc(punto, sezione, null);
	}

	private void stampaDoc(String punto, String sezione, Hashtable dati) {
		//		String line = punto + " sezione>" + sezione + "<";
		//		if (dati != null) {
		//			line += "\tDati>" + dati + "<";
		//		}
		//		System.out.println(line);
	}
/*
	private void stampaDoc1(String punto, String sezione, Hashtable dati) {
		String line = punto + " sezione>" + sezione + "<";
		if (dati != null) {
			line += "\tDati>" + dati + "<";
		}
		System.out.println(line);
	}
*/
	/**
	* stampa sintetica: sezione fine tabella
	*/
	private void mkFineTabella(mergeDocument doc) {
		String punto = MIONOME + "mkFineTabella ";
		stampaDoc(punto, "finetab", new Hashtable());
		doc.write("finetab");

	}
	private String getNotePiano(ISASConnection dbc,ISASRecord dbr)throws Exception {
		String note="";
		if(dbr.get("st_data")!=null && !dbr.get("st_data").toString().equals("")) {
			String sel="select pa_progetto from piano_assist where "+
			" pa_tipo_oper='"+dbr.get("ag_tipo_oper")+"'"+
			" and n_cartella="+dbr.get("ag_cartella")+
			" and n_progetto="+dbr.get("ag_contatto")+
			" and cod_obbiettivo='"+dbr.get("cod_obbiettivo").toString().trim()+"'"+
			" and n_intervento="+dbr.get("n_intervento")+
			" and  pa_data="+dbc.formatDbDate(dbr.get("st_data").toString());
			ISASRecord dbrL=dbc.readRecord(sel);
			if(dbrL!=null) {
				if(dbrL.get("pa_progetto")!=null)
				note=dbrL.get("pa_progetto").toString();
			}
		}
		return note;
	}
	private void mkBodyPlan(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc, Hashtable dati) throws Exception {
		String punto = MIONOME + "mkBodyPlan ";
		String oldCart = "", cartAss = "";
		String old_oper = "", cur_oper = "";
		String cur_data = "";
//		boolean conf_note=getConf(dbc, "ST_NOTE_AG");
		stampa(punto + " Inizio con dati>" + par + "< dati Aggiornati>" + dati + "<");
		 //Inizio con dati>{dataFineSettimana=2012-12-09, dataIniziSettimana=2012-12-03, figprof=, primoGiornoSettimana=3, dataFineSettIta=09/12/2012, distretto=, codazsan=108, ultimoGiornoSettimana=9, codope=, data_inizio=2012-12-04, codpres=, data_fine=2012-12-08, codreg=090, zona=, dataIniziSettIta=03/12/2012}< dati Aggiornati>{#presidio#=, #tipo_figprof#=FIGURA PROFESSIONALE NON VALIDA, #mese#=Dicembre, dataFineSettIta=09/12/2012, #sabgg#=8/12/2012, #annoFine#=2012, #margg#=4/12/2012, #numSett#=49, #numSettFine#=49, #tipo_agenda#=Agenda FIGURA PROFESSIONALE NON VALIDA, #anno#=2012, #periodoInfostmp#=da 3/12/2012 a 9/12/2012, dataIniziSettIta=03/12/2012, #vengg#=7/12/2012, #mergg#=5/12/2012, dataFineSettimana=2012-12-09, dataIniziSettimana=2012-12-03, #stampato_il#=08/01/2013   7:57:55, #lungg#=3/12/2012, ultimoGiornoSettimana=9, #infoAsl#=A.U.S.L. N. 8 AREZZO, primoGiornoSettimana=3, #giogg#=6/12/2012, #info_settimana#=08/12/2012, #domgg#=9/12/2012}<
		try {
			Hashtable dettaglioGiornaliero = new Hashtable();
			int inizioGiornoSettimana = getIntero(ISASUtil.getValoreStringa(par, "primoGiornoSettimana"));
			//			int fineGiornoSettimana = inizioGiornoSettimana + 6;
			String oldData = "";
			String prestazione = "";
			int progressivoInserimento = 0;
			//			stampa(punto + "inizioGiornisettimaan>" + inizioGiornoSettimana + "< finegiornosettina>" + fineGiornoSettimana + "<");
				//			int giornoStampato = inizioGiornoSettimana;
			Calendar giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));

			Calendar fineGiornoSettimana = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataFineSettimana"));
			//			fineGiornoSettimana.set(Calendar.DAY_OF_MONTH, fineGiornoSettimana.get(Calendar.DAY_OF_MONTH) + 1);

			//			int giornoCorrente = 0;
			Calendar giornoCorrente = null;
			String mattinaPomeriggio = "";
			boolean aggiuntaPrestazione = false;
			while (dbcur.next()) {
				aggiuntaPrestazione = false;
				ISASRecord dbr = dbcur.getRecord();
				cartAss = ISASUtil.getValoreStringa(dbr, "ag_cartella");
				prestazione = ISASUtil.getValoreStringa(dbr, "prestazione");
				cur_oper = ISASUtil.getValoreStringa(dbr, "codice");
				cur_data = getDateField(dbr, "ag_data");
				String note="";
				if(conf_note) note=getNotePiano(dbc,dbr);
				dbr.put("note",note);
				mattinaPomeriggio = ISASUtil.getValoreStringa(dbr, "ag_orario");
				//				giornoCorrente = getGiorno(cur_data);
				giornoCorrente = recuperaCalendarFormatoItaliano(cur_data);
				//				stampa(punto + "\t attuale>" + (attuale++) + "/" + numeroRighe + "\t\t operatore>" + cur_oper + "< cur_data>" + cur_data
				//						+ "< cartAss>" + cartAss + "< prestazione>" + prestazione + "<\ngiornistampati>" + giorniStampati
				//						+ "<giornoStampato>" + lineCal(giornoStampato) + "< oldCart>" + oldCart + "<giornoCorrente>" + giornoCorrente
				//						+ "<fineGiornoSettimana>" + lineCal(fineGiornoSettimana) + "<");

				if (!ISASUtil.valida(oldData)) {
					oldData = cur_data;
				}

				if (!ISASUtil.valida(oldCart)) {
					oldCart = cartAss;
					oldData = cur_data;
				}
				if (!ISASUtil.valida(old_oper)) {
					mkInizioTabellaPlan(dbc, md, dbr, dati);
					faiRigaAssistitoPlan(dbc,dbr, md, par);
					old_oper = cur_oper;
				}

				if (ISASUtil.valida(old_oper) && ISASUtil.valida(cur_oper) && (!(cur_oper.equals(old_oper)))) {
					//					int giornoOldCorrente = getGiorno(oldData);
					Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
					//					while (giornoStampato <= fineGiornoSettimana) {
					while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
						//						stampa(punto + "giornoStampato>" + lineCal(giornoStampato) + "< giornoOldCorrente>" + lineCal(giornoOldCorrente)
						//								+ "<");
						//						if (giornoStampato == giornoOldCorrente) {
						//						if (giornoStampato.get(Calendar.DAY_OF_MONTH) == giornoOldCorrente) {
						if (giornoStampato.equals(giornoOldCorrente)) {
							faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero, oldCart,
									oldData);
							//							dettaglioGiornaliero.clear();
						} else {
							faidettaglioGiornaliero(dbc, md, new Hashtable(), null, null);
						}
						//						giornoStampato += 1;
						giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);

						//						giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
						//						stampa(punto + " dopo  giornoStampato>" + lineCal(giornoStampato) + "<");
					}
					fineRigaAssistitoPlan(md);
					faiRigaAssistitoPlanFine(md);
					//					giornoStampato = inizioGiornoSettimana;
					//					giornoStampato.set(Calendar.DAY_OF_MONTH, inizioGiornoSettimana);
					giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
					stampaDoc(punto, "taglia", new Hashtable());
					md.write("taglia");
					mkInizioTabellaPlan(dbc, md, dbr, dati);
					faiRigaAssistitoPlan(dbc,dbr, md,par);
					oldData = cur_data;
				}
				//				stampa(punto + "Cartella>" + cartAss + "< oldCart>" + oldCart + "< \n " + "cur_data>" + cur_data + "< oldData>" + oldData
				//						+ "<\ngiornoStampato>" + lineCal(giornoStampato) + "<");

				if (ISASUtil.valida(cartAss) && ISASUtil.valida(oldCart) && cartAss.equals(oldCart)) {//riga assistito
					//					stampa(punto + "sssssssssstessa cartella>" + oldCart + "<aggiungo prestazioni>" + prestazione + "<cur_data>" + cur_data
					//							+ "< oldData>" + oldData + "<oper>" + cur_oper + "< old_oper>" + old_oper + "<\n");
					//					dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
					if (ISASUtil.valida(cur_data) && ISASUtil.valida(oldData) && cur_data.equals(oldData)) {
						aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
								aggiuntaPrestazione);
						//						dettaglioGiornaliero.put((++progressivoInserimento) + "", prestazione);
						//						stampa(punto + " stessa data> aggiungo prestazionea>" + dettaglioGiornaliero + "<");
					} else {
						//						stampa(punto + " data diversa scarico le prestazioni >" + dettaglioGiornaliero + "<\n" + "old_oper>" + old_oper
						//								+ "< cur_oper>" + cur_oper + "<\n");
						//						int giornoOldCorrente = getGiorno(oldData);
						Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
						//						while (giornoStampato.get(Calendar.DAY_OF_MONTH) <= giornoOldCorrente) {
						while (giornoStampato.before(giornoOldCorrente) || giornoStampato.equals(giornoOldCorrente)) {
							//							stampa(punto + "giornoStampato>" + lineCal(giornoStampato) + "< giornoOldCorrente>"
							//									+ lineCal(giornoOldCorrente) + "<");
							//							if (giornoStampato.get(Calendar.DAY_OF_MONTH) == giornoOldCorrente) {
							if (giornoStampato.equals(giornoOldCorrente)) {
								faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero,cartAss,
										oldData);
								//								dettaglioGiornaliero = new Hashtable();
							} else {
								//								faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
								faidettaglioGiornaliero(dbc, md, new Hashtable(),  null, null);
							}
							//							giornoStampato += 1;
							giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
							//							stampa(punto + " data diversa scarico le prestazioni >" + lineCal(giornoStampato) + "<");
						}
						oldData = cur_data;
						aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
								aggiuntaPrestazione);
						//						dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
						//						stampa(punto + " data diversa aggiungo prestazionea>" + dettaglioGiornaliero + "<");
					}
				} else {
					if (old_oper.equals(cur_oper)) {
						//					stampa(punto + " cartelle diverse cur_cart>" + cur_data + "< old_data>" + oldData + "<old_oper>" + old_oper
						//							+ "< cur_oper>" + cur_oper + "<\n");

						//					stampa(punto + "cartelle diverse>" + cartAss + "< oldCart>" + oldCart + "<oper>" + cur_oper + "< old_oper>" + old_oper
						//							+ "<curdata>" + cur_data + "<\n");
						//					if (ISASUtil.valida(ol) cartAss.equals(oldCart))
						//					int giornoOldCorrente = getGiorno(oldData);
						Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
						//					stampa(punto + "\t cartella diversa scarico i dati scarico dati> " + dettaglioGiornaliero + "<\n"
						//							+ "giornoOldCorrente>" + lineCal(giornoOldCorrente) + "<fineGiornoSettimana>" + lineCal(fineGiornoSettimana)
						//							+ "<\n" + "giornoStampato>" + lineCal(giornoStampato) + "<oldData>" + oldData);
						//					while (giornoStampato <= fineGiornoSettimana) {
						while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
							//						stampa(punto + "giornoStampato>" + lineCal(giornoStampato) + "< giornoOldCorrente>" + lineCal(giornoOldCorrente)
							//								+ "<");
							//						if (giornoStampato.get(Calendar.DAY_OF_MONTH) == giornoOldCorrente) {
							if (giornoStampato.equals(giornoOldCorrente)) {
								faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero, oldCart,
										oldData);
								//							dettaglioGiornaliero = new Hashtable();
							} else {
								faidettaglioGiornaliero(dbc, md, new Hashtable(),  null, null);
							}
							//						giornoStampato += 1;
							giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
							//						stampa(punto + "\t cartella diversa scarico i dati scarico dati fineGiornoSettimana>"
							//								+ lineCal(fineGiornoSettimana) + "<\n" + "giornoStampato>" + lineCal(giornoStampato) + "<oldData>"
							//								+ oldData);
						}

						//					giornoStampato = inizioGiornoSettimana;
						giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
						//					stampa(punto + "\t cartella diversa scarico azzero giornoStampato>" + lineCal(giornoStampato) + "<oldData>" + oldData);
						fineRigaAssistitoPlan(md);
						faiRigaAssistitoPlan(dbc,dbr, md,par);
						oldData = cur_data;
						progressivoInserimento = 0;
					}
				}
				aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione, aggiuntaPrestazione);
				//				dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
				old_oper = cur_oper;
				oldCart = cartAss;
				//				stampa(punto + "\t aggiungo la prestazione al giorno>" + cur_data + "< dettaglioGiornaliero>" + dettaglioGiornaliero
				//						+ "<\n" + "giornoStampato.get(Calendar.DAY_OF_MONTH)>" + lineCal(giornoStampato) + "<");

			}//end while
			//			while (giornoStampato <= giornoCorrente) {
			//			while (giornoStampato <= fineGiornoSettimana) {
			while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
				//				stampa(punto + "giornoStampato>" + lineCal(giornoStampato) + "<");
				//				if (giornoStampato.get(Calendar.DAY_OF_MONTH) == giornoCorrente) {
				if (giornoStampato.equals(giornoCorrente)) {
					faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero, cartAss, cur_data);
				} else {
					faidettaglioGiornaliero(dbc, md, new Hashtable(),  null, null);
				}
				//				giornoStampato += 1;
				giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
				//				stampa(punto + "giornoStampato>" + lineCal(giornoStampato) + "<");
			}
			fineRigaAssistitoPlan(md);
			faiRigaAssistitoPlanFine(md);

		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.mkBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkBody()");
		}
	}
	
	private void mkBodyPlanSin(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc, Hashtable dati) throws Exception {
		String punto = MIONOME + "mkBodyPlanSin ";
		String oldCart = "", cartAss = "";
		String old_oper = "", cur_oper = "";
		String cur_data = "";
		
		stampa(punto + " Inizio con dati>" + par + "< dati Aggiornati>" + dati + "<");
		try {
			Hashtable dettaglioGiornaliero = new Hashtable();
			int inizioGiornoSettimana = getIntero(ISASUtil.getValoreStringa(par, "primoGiornoSettimana"));
			
			String oldData = "";
			String prestazione = "";
			int progressivoInserimento = 0;
			
			Calendar giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));

			Calendar fineGiornoSettimana = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataFineSettimana"));
			
			Calendar giornoCorrente = null;
			String mattinaPomeriggio = "";
			boolean aggiuntaPrestazione = false;
			while (dbcur.next()) {
				aggiuntaPrestazione = false;
				ISASRecord dbr = dbcur.getRecord();
				cartAss = ISASUtil.getValoreStringa(dbr, "ag_cartella");
				prestazione = ISASUtil.getValoreStringa(dbr, "prestazione");
				cur_oper = ISASUtil.getValoreStringa(dbr, "codice");
				cur_data = getDateField(dbr, "ag_data");
				String note="";
				if(conf_note) note=getNotePiano(dbc,dbr);
				dbr.put("note",note);
				mattinaPomeriggio = ISASUtil.getValoreStringa(dbr, "ag_orario");
				
				giornoCorrente = recuperaCalendarFormatoItaliano(cur_data);

				if (!ISASUtil.valida(oldData)) {
					oldData = cur_data;
				}

				if (!ISASUtil.valida(oldCart)) {
					oldCart = cartAss;
					oldData = cur_data;
				}
				if (!ISASUtil.valida(old_oper)) {
					mkInizioTabellaPlanSin(dbc, md, dbr, dati, par);
					faiRigaAssistitoPlanSin(dbc,dbr, md, par);
					old_oper = cur_oper;
				}

				if (ISASUtil.valida(old_oper) && ISASUtil.valida(cur_oper) && (!(cur_oper.equals(old_oper)))) {
					
					Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
					
					while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
						if (giornoStampato.equals(giornoOldCorrente)) {
							faidettaglioGiornalieroSin(dbc, md, dettaglioGiornaliero, oldCart,
									oldData);
						} else {
							faidettaglioGiornalieroSin(dbc, md, new Hashtable(), null, null);
						}
						
						giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
						
					}
					fineRigaAssistitoPlan(md);
					faiRigaAssistitoPlanFine(md);
					
					giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
					stampaDoc(punto, "taglia", new Hashtable());
					md.write("taglia");
					mkInizioTabellaPlanSin(dbc, md, dbr, dati,par);
					faiRigaAssistitoPlanSin(dbc,dbr, md, par);
					oldData = cur_data;
				}
				

				if (ISASUtil.valida(cartAss) && ISASUtil.valida(oldCart) && cartAss.equals(oldCart)) {//riga assistito
					
					if (ISASUtil.valida(cur_data) && ISASUtil.valida(oldData) && cur_data.equals(oldData)) {
						aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
								aggiuntaPrestazione);
						
					} else {
						Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
						
						while (giornoStampato.before(giornoOldCorrente) || giornoStampato.equals(giornoOldCorrente)) {
							
							if (giornoStampato.equals(giornoOldCorrente)) {
								faidettaglioGiornalieroSin(dbc, md, dettaglioGiornaliero,cartAss,
										oldData);
							} else {
								faidettaglioGiornalieroSin(dbc, md, new Hashtable(),  null, null);
							}
							
							giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
							
						}
						oldData = cur_data;
						aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
								aggiuntaPrestazione);
						
					}
				} else {
					if (old_oper.equals(cur_oper)) {						
						Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
						
						while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
							
							if (giornoStampato.equals(giornoOldCorrente)) {
								faidettaglioGiornalieroSin(dbc, md, dettaglioGiornaliero, oldCart,
										oldData);								
							} else {
								faidettaglioGiornalieroSin(dbc, md, new Hashtable(),  null, null);
							}
							
							giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
							
						}

						giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
					
						fineRigaAssistitoPlan(md);
						faiRigaAssistitoPlanSin(dbc,dbr, md, par);
						oldData = cur_data;
						progressivoInserimento = 0;
					}
				}
				aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione, aggiuntaPrestazione);
				
				old_oper = cur_oper;
				oldCart = cartAss;
				
			}//end while
			
			while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
				
				if (giornoStampato.equals(giornoCorrente)) {
					faidettaglioGiornalieroSin(dbc, md, dettaglioGiornaliero, cartAss, cur_data);
				} else {
					faidettaglioGiornalieroSin(dbc, md, new Hashtable(),  null, null);
				}
				
				giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
				
			}
			fineRigaAssistitoPlan(md);
			faiRigaAssistitoPlanFine(md);

		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.mkBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkBody()");
		}
	}

	private Calendar recuperaCalendar(String dataInizio) {
		String punto = MIONOME + "recuperaCalendar ";
		Calendar calenderInizio = null;
		try {
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
			//			stampa(punto + "data>" + giorno + "/" + mese + "/" + anno + "<");
			mese = (mese > 0 ? mese - 1 : mese);

			calenderInizio = new GregorianCalendar(anno, mese, giorno);
			//			stampa(punto + "giorno>" + calenderInizio.get(Calendar.DAY_OF_MONTH) + "<mese>" + calenderInizio.get(Calendar.MONTH) + "<"
			//					+ calenderInizio.get(Calendar.YEAR) + "<dataInizio>" + dataInizio + "<");

		} catch (Exception e) {
			System.out.println("Errore nel recuperare il calendar \n\ndata da processare >" + dataInizio + "<\n\n");
		}
		return calenderInizio;
	}
	/*private void mkBodyPlanbargi(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc, Hashtable dati) throws Exception {
		String punto = MIONOME + "mkBodyPlan ";
		String oldCart = "", cartAss = "";
		String old_oper = "", cur_oper = "";
		String cur_data = "";
		stampa(punto + " Inizio con dati>" + par + "< dati Aggiornati>" + dati + "<");
	    try {
			Hashtable dettaglioGiornaliero = new Hashtable();
			String oldData = "";
			String prestazione = "";
			int progressivoInserimento = 0;
			//Calendar giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
			//Calendar fineGiornoSettimana = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataFineSettimana"));			
			//Calendar giornoCorrente = null;
			boolean aggiuntaPrestazione = false;
			String dataIniSett=ISASUtil.getValoreStringa(dati, "dataIniziSettimana");
			String dataFinSett=ISASUtil.getValoreStringa(dati, "dataFineSettimana");
			debugMessage("apro primo while");
			while (dbcur.next()) {
				aggiuntaPrestazione = false;
				ISASRecord dbr = dbcur.getRecord();
				cartAss = ISASUtil.getValoreStringa(dbr, "ag_cartella");
				prestazione = ISASUtil.getValoreStringa(dbr, "prestazione");
				cur_oper = ISASUtil.getValoreStringa(dbr, "codice");
				cur_data = getDateField(dbr, "ag_data");
			//	giornoCorrente = recuperaCalendarFormatoItaliano(cur_data);
				
					
				if (!ISASUtil.valida(oldData)) {
					oldData = cur_data;
				}

				if (!ISASUtil.valida(oldCart)) {
					oldCart = cartAss;
					oldData = cur_data;
				}
				if (!ISASUtil.valida(old_oper)) {
					mkInizioTabellaPlan(dbc, md, dbr, dati);
					faiRigaAssistitoPlan(dbr, md);
					old_oper = cur_oper;
				}

				if (ISASUtil.valida(old_oper) && ISASUtil.valida(cur_oper) && (!(cur_oper.equals(old_oper)))) {
					//Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
			        
						while (dt.confrontaDate(cur_data, dataFinSett)!=1) {
								//giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {

							debugMessage("apro second while");
								if (dt.confrontaDate(cur_data, oldData)==0) {
									//	if (giornoStampato.equals(giornoOldCorrente)) {
							faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero,  oldCart,
									oldData);
							//							dettaglioGiornaliero.clear();
						} else {
							faidettaglioGiornaliero(dbc, md, new Hashtable(),null, null);
						}
						//						giornoStampato += 1;
						//????? giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);

						//						giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
						//						stampa(punto + " dopo  giornoStampato>" + lineCal(giornoStampato) + "<");
					}

						debugMessage("chiuso secondo while");
					fineRigaAssistitoPlan(md);
					faiRigaAssistitoPlanFine(md);
					//giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
					//stampaDoc(punto, "taglia", new Hashtable());
					md.write("taglia");
					mkInizioTabellaPlan(dbc, md, dbr, dati);
					faiRigaAssistitoPlan(dbr, md);
					oldData = cur_data;
				}
							if (ISASUtil.valida(cartAss) && ISASUtil.valida(oldCart) && cartAss.equals(oldCart)) {//riga assistito
							if (ISASUtil.valida(cur_data) && ISASUtil.valida(oldData) && cur_data.equals(oldData)) {
						aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
								aggiuntaPrestazione);
						} else {
							//Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
							int stop=0;
							while (dt.confrontaDate(cur_data, oldData)!=1) {
								stop++;
						//	while (giornoStampato.before(giornoOldCorrente) || giornoStampato.equals(giornoOldCorrente)) {
if(stop>500)return;
								debugMessage("apro terzo while");
								if (dt.confrontaDate(cur_data, oldData)==0) {
								//if (giornoStampato.equals(giornoOldCorrente)) {
								faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero, cartAss,
										oldData);
							} else {
								faidettaglioGiornaliero(dbc, md, new Hashtable(),  null, null);
							}
							//????giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
						}

							debugMessage("chiuso terzo while");
						oldData = cur_data;
						aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
								aggiuntaPrestazione);
					}
				} else {
					if (old_oper.equals(cur_oper)) {
				//		Calendar giornoOldCorrente = recuperaCalendarFormatoItaliano(oldData);
					//	while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {
							while (dt.confrontaDate(cur_data, dataFinSett)!=1) {
							debugMessage("apro quarto while");
						//	if (giornoStampato.equals(giornoOldCorrente)) {
								if (dt.confrontaDate(cur_data, oldData)==0) {
								faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero,  oldCart,
										oldData);
							} else {
								faidettaglioGiornaliero(dbc, md, new Hashtable(),  null, null);
							}
							//?????giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
							
						}

						debugMessage("chiuso quarto while");
					//	giornoStampato = recuperaCalendar(ISASUtil.getValoreStringa(dati, "dataIniziSettimana"));
						fineRigaAssistitoPlan(md);
						faiRigaAssistitoPlan(dbr, md);
						oldData = cur_data;
						progressivoInserimento = 0;
					}
				}
				aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione, aggiuntaPrestazione);
				old_oper = cur_oper;
				oldCart = cartAss;
			}//end while
			debugMessage("chiuso primo while");
	while (giornoStampato.before(fineGiornoSettimana) || giornoStampato.equals(fineGiornoSettimana)) {

				debugMessage("apro quinto while");
				if (giornoStampato.equals(giornoCorrente)) {
					faidettaglioGiornaliero(dbc, md, dettaglioGiornaliero, giornoStampato.get(Calendar.DAY_OF_MONTH), cartAss, cur_data);
				} else {
					faidettaglioGiornaliero(dbc, md, new Hashtable(), giornoStampato.get(Calendar.DAY_OF_MONTH), null, null);
				}
				giornoStampato.set(Calendar.DAY_OF_MONTH, giornoStampato.get(Calendar.DAY_OF_MONTH) + 1);
			}

			debugMessage("chiuso quinto while");
			fineRigaAssistitoPlan(md);
			faiRigaAssistitoPlanFine(md);
			stampa(punto + "FINE");
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.mkBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkBody()");
		}
	}*/
/*
	private String lineCal(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.MONTH) + "/" + cal.get(Calendar.YEAR);
	}
*/
	private Calendar recuperaCalendarFormatoItaliano(String data) {
		String punto = MIONOME + "recuperaCalendarFormatoItaliano ";
		Calendar calender = null;
		try {
			//			 String data = "30/05/2011";
			int giorno = -1, mese = -1, anno = -1;

			int pos = data.indexOf("/");
			if (pos > 0) {
				giorno = getIntero(data.substring(0, 2));
			}
			String val = data.substring(pos + 1);
			pos = val.indexOf("/");
			if (pos > 0) {
				mese = getIntero(val.substring(0, pos));
				anno = getIntero(val.substring(pos + 1));
			}
			mese = (mese > 0 ? mese - 1 : mese);
			calender = new GregorianCalendar(anno, mese, giorno);
			//			stampa(punto + "data>" + giorno + "/" + mese + "/" + anno + "<");
			//			stampa(punto + "giorno>" + calender.get(Calendar.DAY_OF_MONTH) + "<mese>" + calender.get(Calendar.MONTH) + "<"
			//					+ calender.get(Calendar.YEAR) + "< data>" + data + "<");
		} catch (Exception e) {
			System.out.println("Errore nel recuperare il calendar \n\ndata da processare >" + data + "<\n\n");
		}
		return calender;
	}


	/*
		private void mkBodyPlanOLD(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc, Hashtable dati) throws Exception {
			String punto = MIONOME + "mkBodyPlan ";
			String oldCart = "", cartAss = "";
			String old_oper = "", cur_oper = "";
			String cur_data = "";
			stampa(punto + " Inizio con dati>" + par + "<\n dati Aggiornati>" + dati + "<\n");
			try {
				Hashtable dettaglioGiornaliero = new Hashtable();
				int inizioGiornoSettimana = getIntero(ISASUtil.getValoreStringa(par, "primoGiornoSettimana"));
				int fineGiornoSettimana = inizioGiornoSettimana + 6;
				int giorniStampati = inizioGiornoSettimana;
				String oldData = "";
				String prestazione = "";
				int progressivoInserimento = 0;
				//			stampa(punto + "inizioGiornisettimaan>" + inizioGiornoSettimana + "< finegiornosettina>" + fineGiornoSettimana + "<");
				int numeroRighe = dbcur.getDimension();
				int attuale = 0;
				int giornoStampato = inizioGiornoSettimana;
				int giornoCorrente = 0;
				String mattinaPomeriggio = "";
				boolean aggiuntaPrestazione = false;
				while (dbcur.next()) {
					aggiuntaPrestazione = false;
					ISASRecord dbr = dbcur.getRecord();
					//				stampa(punto + "\n Record>" + dbr.getHashtable() + "<\n");
					cartAss = ISASUtil.getValoreStringa(dbr, "ag_cartella");
					prestazione = ISASUtil.getValoreStringa(dbr, "prestazione");
					cur_oper = ISASUtil.getValoreStringa(dbr, "codice");
					cur_data = getDateField(dbr, "ag_data");

					mattinaPomeriggio = ISASUtil.getValoreStringa(dbr, "ag_orario");

					giornoCorrente = getGiorno(cur_data);
					//				if (!ISASUtil.valida(oldCart)) {
					//					stampa(punto + "\n setto il dettagglio con prestazione>" + prestazione + "< progressivo>" + progressivoInserimento
					//							+ "<");
					//					dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
					//				} else {
					//					stampa(punto + "\n cartOld valido");
					//				}
					stampa(punto + "\t" + (attuale++) + "/" + numeroRighe + "\t\t operatore>" + cur_oper + "< cur_data>" + cur_data
							+ "< cartAss>" + cartAss + "< \nprestazione>" + prestazione + "<\ngiornistampati>" + giorniStampati
							+ "<giornoStampato>" + giornoStampato + "<\n oldCart>" + oldCart + "<giornoCorrente>" + giornoCorrente);

					if (!ISASUtil.valida(oldData)) {
						oldData = cur_data;
					}

					if (!ISASUtil.valida(oldCart)) {
						oldCart = cartAss;
						oldData = cur_data;
					}
					if (!ISASUtil.valida(old_oper)) {
						mkInizioTabellaPlan(dbc, md, dbr, dati);
						faiRigaAssistitoPlan(dbr, md);
						old_oper = cur_oper;
					}

					if (ISASUtil.valida(old_oper) && ISASUtil.valida(cur_oper) && (!(cur_oper.equals(old_oper)))) {
						int giornoOldCorrente = getGiorno(oldData);
						while (giornoStampato <= fineGiornoSettimana) {
							if (giornoStampato == giornoOldCorrente) {
								faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
								//							dettaglioGiornaliero.clear();
							} else {
								faidettaglioGiornaliero(md, new Hashtable(), giornoStampato);
								//							faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
							}
							giornoStampato += 1;
						}
						fineRigaAssistitoPlan(md);
						faiRigaAssistitoPlanFine(md);
						giornoStampato = inizioGiornoSettimana;
						stampaDoc(punto, "taglia", new Hashtable());
						md.write("taglia");
						mkInizioTabellaPlan(dbc, md, dbr, dati);
						faiRigaAssistitoPlan(dbr, md);
					} else {
						//					stampa(punto + " stesso operatore");
					}
					stampa(punto + "Cartella>" + cartAss + "< oldCart>" + oldCart + "< \n " + "cur_data>" + cur_data + "< oldData>" + oldData
							+ "<\ngiornoStampato>" + giornoStampato + "<");

					if (ISASUtil.valida(cartAss) && ISASUtil.valida(oldCart) && cartAss.equals(oldCart)) {//riga assistito
						//					stampa(punto + "stessa cartella, aggiungo prestazioni>" + prestazione + "<cur_data>" + cur_data + "< oldData>"
						//							+ oldData + "<");
						//					dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
						if (ISASUtil.valida(cur_data) && ISASUtil.valida(oldData) && cur_data.equals(oldData)) {
							aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
									aggiuntaPrestazione);
							//						dettaglioGiornaliero.put((++progressivoInserimento) + "", prestazione);
							//						stampa(punto + " stessa data> aggiungo prestazionea>" + dettaglioGiornaliero + "<");
						} else {
							stampa(punto + " data diversa scarico le prestazioni >" + dettaglioGiornaliero + "<");
							int giornoOldCorrente = getGiorno(oldData);
							while (giornoStampato <= giornoOldCorrente) {
								if (giornoStampato == giornoOldCorrente) {
									faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
									//								dettaglioGiornaliero = new Hashtable();
								} else {
									//								faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
									faidettaglioGiornaliero(md, new Hashtable(), giornoStampato);
								}
								giornoStampato += 1;
							}
							oldData = cur_data;
							aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione,
									aggiuntaPrestazione);
							//						dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
							//						stampa(punto + " data diversa aggiungo prestazionea>" + dettaglioGiornaliero + "<");
						}
					} else {
						int giornoOldCorrente = getGiorno(oldData);
						stampa(punto + "\t cartella diversa scarico i dati scarico dati> " + dettaglioGiornaliero + "<\n"
								+ "giornoOldCorrente>" + giornoOldCorrente + "<fineGiornoSettimana>" + fineGiornoSettimana + "<\n"
								+ "giornoStampato>" + giornoStampato + "<oldData>" + oldData);
						while (giornoStampato <= fineGiornoSettimana) {
							if (giornoStampato == giornoOldCorrente) {
								faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
								//							dettaglioGiornaliero = new Hashtable();
							} else {
								faidettaglioGiornaliero(md, new Hashtable(), giornoStampato);
								//							faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
							}
							giornoStampato += 1;
						}

						giornoStampato = inizioGiornoSettimana;
						fineRigaAssistitoPlan(md);
						faiRigaAssistitoPlan(dbr, md);
						oldData = cur_data;
						progressivoInserimento = 0;
					}
					aggiuntaPrestazione = aggiungPrestazione(dettaglioGiornaliero, progressivoInserimento++, prestazione, aggiuntaPrestazione);
					//				dettaglioGiornaliero.put((progressivoInserimento++) + "", prestazione);
					old_oper = cur_oper;
					oldCart = cartAss;
					stampa(punto + "\t aggiungo la prestazione al giorno>" + cur_data + "< dettaglioGiornaliero>" + dettaglioGiornaliero + "<");

				}//end while

				//			while (giornoStampato <= giornoCorrente) {
				while (giornoStampato <= fineGiornoSettimana) {
					if (giornoStampato == giornoCorrente) {
						faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
					} else {
						//					faidettaglioGiornaliero(md, new Hashtable(), giornoStampato);
						faidettaglioGiornaliero(md, dettaglioGiornaliero, giornoStampato);
					}
					giornoStampato += 1;
				}
				fineRigaAssistitoPlan(md);
				faiRigaAssistitoPlanFine(md);

			} catch (Exception e) {
				debugMessage("FoAgendaGiornEJB.mkBody(): " + e);
				e.printStackTrace();
				throw new SQLException("Errore eseguendo mkBody()");
			}
		}
	*/
	private boolean aggiungPrestazione(Hashtable dettaglioGiornaliero, int progressivoInserimento, String prestazione,
			boolean aggiuntaPrestazione) {

		if (!aggiuntaPrestazione) {
			dettaglioGiornaliero.put(progressivoInserimento + "", prestazione);
		}
		return true;
	}

	private boolean getStampareSeparatore(String mattinaPomeriggio, String mattinaPomeriggioOld) {
		boolean stampaSeparatore = false;
		if (ISASUtil.valida(mattinaPomeriggio) && ISASUtil.valida(mattinaPomeriggioOld)) {
			stampaSeparatore = mattinaPomeriggio.equals("1") && mattinaPomeriggioOld.equals("0");
		}
		return stampaSeparatore;
	}

	private void faiRigaAssistitoPlanFine(mergeDocument md) {
		md.write("rigaAssistitoFine");
		
	}

	private int getGiorno(String cur_data) {
		String punto = MIONOME + "getGiorno ";
		int giorno = 0;
		try {
			String giornoDb = cur_data.substring(0, 2);
			giorno = Integer.parseInt(giornoDb);
		} catch (Exception e) {
			stampa(punto + "\t ERRORE NEL RECUPERARE IL GIORNO da>" + cur_data + "<");
		}
		return giorno;
	}

	private void faidettaglioGiornaliero1(mergeDocument md, Hashtable dettaglioGiornaliero, int posizione) {
		String punto = MIONOME + "faidettaglioGiornaliero ";
		Hashtable prtCall = new Hashtable();
		//		stampa(punto + "Scarico i dati>" + dettaglioGiornaliero + "<" + posizione + "<\n");

		if (dettaglioGiornaliero.size() > 0) {
			stampaDoc(punto, "rigaAssistitodettInizio", new Hashtable());
			md.write("rigaAssistitodettInizio");
			Enumeration keys = dettaglioGiornaliero.keys();
			String key, value = "";
			while (keys.hasMoreElements()) {
				key = (String) keys.nextElement();
				value = ISASUtil.getValoreStringa(dettaglioGiornaliero, key);
				prtCall.put("#prestazione#", value);
				//				stampaDoc1(punto, "rigaAssistitodettCorpo", prtCall);
				md.writeSostituisci("rigaAssistitodettCorpo", prtCall);
			}

			stampaDoc(punto, "rigaAssistitodettCorpoFine");
			md.write("rigaAssistitodettCorpoFine");
			dettaglioGiornaliero.clear();
			//			stampa(punto + "Scarico i dati>" + dettaglioGiornaliero + "<\n Svuotata i dati");
		} else {
			//			stampa(punto + "\t riga vuota");
			stampaDoc(punto, "rigaAssistitodettVuota " + posizione + "<", dettaglioGiornaliero);
			md.write("rigaAssistitodettVuota");
		}
		//		stampaDoc(punto, "rigaAssistitodett " + posizione + "<", dettaglioGiornaliero);
		//		md.writeSostituisci("rigaAssistitodett", prtCall);

	}

	private void faidettaglioGiornaliero(ISASConnection dbc, mergeDocument md, Hashtable dettaglioGiornaliero,
			String cartAss, String cur_data) {
		String punto = MIONOME + "faidettaglioGiornaliero ";
		Hashtable prtCall = new Hashtable();
		//		stampa(punto + "Scarico i dati>" + dettaglioGiornaliero + "<" + posizione + "<" + "cartella>" + cartAss + "< data>" + cur_data
		//				+ "<\n");

		if (dettaglioGiornaliero.size() > 0) {
			stampaDoc(punto, "rigaAssistitodettInizio", new Hashtable());
			md.write("rigaAssistitodettInizio");

			// Sort hashtable.
			Vector v = new Vector(dettaglioGiornaliero.values());
			Collections.sort(v);
			// Display (sorted) hashtable.
			boolean giornoSospeso = false;
			for (Enumeration e = v.elements(); e.hasMoreElements();) {
				String prestazione = (String) e.nextElement();
				prtCall.put("#prestazione#", prestazione.length()>=40 ? prestazione.substring(0, 39) : prestazione);				
				//prtCall.put("#prestazione#", prestazione.substring(40));
				
				if (ISASUtil.valida(cartAss) && ISASUtil.valida(cur_data)) {
					//					stampa(punto + "\t recupero se la prestazione e' stata sospesa.");
					giornoSospeso = recuperaInfoGGiornoSospensione(dbc, cartAss, cur_data);
				}

				if (giornoSospeso) {
					stampaDoc(punto, "rigaAssistitodettCorpoSospeso", prtCall);
					md.writeSostituisci("rigaAssistitodettCorpoSospeso", prtCall);
				} else {
					stampaDoc(punto, "rigaAssistitodettCorpoSospeso", prtCall);
					md.writeSostituisci("rigaAssistitodettCorpo", prtCall);
				}
				//	            System.out.println("app_chiave: " + app_chiave + " app_valore: " + app_valore);
			}

			stampaDoc(punto, "rigaAssistitodettCorpoFine");
			md.write("rigaAssistitodettCorpoFine");
			dettaglioGiornaliero.clear();
			//			stampa(punto + "Scarico i dati>" + dettaglioGiornaliero + "<\n Svuotata i dati");
		} else {
			//			stampa(punto + "\t riga vuota");
		//	stampaDoc(punto, "rigaAssistitodettVuota " + posizione + "<", dettaglioGiornaliero);
			md.write("rigaAssistitodettVuota");
		}
		//		stampaDoc(punto, "rigaAssistitodett " + posizione + "<", dettaglioGiornaliero);
		//		md.writeSostituisci("rigaAssistitodett", prtCall);
	}
	
	private void faidettaglioGiornalieroSin(ISASConnection dbc, mergeDocument md, Hashtable dettaglioGiornaliero,
			String cartAss, String cur_data) {
		String punto = MIONOME + "faidettaglioGiornalieroSin ";
		Hashtable prtCall = new Hashtable();
		
		if (dettaglioGiornaliero.size() > 0) {
			stampaDoc(punto, "rigaAssistitodettInizio", new Hashtable());
			md.write("rigaAssistitodettInizio");

			// Sort hashtable.
			Vector v = new Vector(dettaglioGiornaliero.values());
			Collections.sort(v);
			// Display (sorted) hashtable.
			boolean giornoSospeso = false;
			Enumeration e = v.elements();
			//for (Enumeration e = v.elements(); e.hasMoreElements();) {
				String prestazione = (String) e.nextElement();
				if (prestazione!=null && !prestazione.equals(""))
					prtCall.put("#prestazione#", " O ");
				else
					prtCall.put("#prestazione#", " - ");

				if (ISASUtil.valida(cartAss) && ISASUtil.valida(cur_data)) {
					//					stampa(punto + "\t recupero se la prestazione e' stata sospesa.");
					giornoSospeso = recuperaInfoGGiornoSospensione(dbc, cartAss, cur_data);
				}

				if (giornoSospeso) {
					stampaDoc(punto, "rigaAssistitodettCorpoSospeso", prtCall);
					md.writeSostituisci("rigaAssistitodettCorpoSospeso", prtCall);
				} else {
					stampaDoc(punto, "rigaAssistitodettCorpoSospeso", prtCall);
					md.writeSostituisci("rigaAssistitodettCorpo", prtCall);
				}
				//	            System.out.println("app_chiave: " + app_chiave + " app_valore: " + app_valore);
			//}

			stampaDoc(punto, "rigaAssistitodettCorpoFine");
			md.write("rigaAssistitodettCorpoFine");
			dettaglioGiornaliero.clear();
			//			stampa(punto + "Scarico i dati>" + dettaglioGiornaliero + "<\n Svuotata i dati");
		} else {
			//			stampa(punto + "\t riga vuota");
		//	stampaDoc(punto, "rigaAssistitodettVuota " + posizione + "<", dettaglioGiornaliero);
			md.write("rigaAssistitodettVuota");
		}
		//		stampaDoc(punto, "rigaAssistitodett " + posizione + "<", dettaglioGiornaliero);
		//		md.writeSostituisci("rigaAssistitodett", prtCall);
	}
	
	


	private boolean recuperaInfoGGiornoSospensione(ISASConnection dbc, String cartAss, String cur_data) {
		String punto = MIONOME + "recuperaInfoGGiornoSospensione ";
		boolean sospesoGiorno = false;

		String query = "SELECT * FROM PROGETTO_SOSP WHERE N_CARTELLA = " + cartAss + " AND PRS_DATA_SOSPESO <= "
				+ formatDate(dbc, cur_data) + " AND PRS_DATA_RIATTIVA > " + formatDate(dbc, cur_data);
		ISASRecord dbrProgettoSosp = getRecord(dbc, query);

		sospesoGiorno = (dbrProgettoSosp != null);
		//		stampa(punto + "\n (dbrProgettoSosp != null)>" + sospesoGiorno + "<\n");

		return sospesoGiorno;
	}

	private ISASRecord getRecord(ISASConnection dbc, String query) {
		String punto = MIONOME + "getRecord ";
		ISASRecord dbrRecord = null;
		//		stampaQuery(punto, query);
		try {
			dbrRecord = dbc.readRecord(query);
		} catch (Exception e) {
			stampa(punto + "\n Errore in recupero record>" + query + "<\n");
			e.printStackTrace();
		}

		return dbrRecord;
	}

	private void fineRigaAssistitoPlan(mergeDocument md) {
		String punto = MIONOME + "fineRigaAssistitoPlan ";

		//		stampaDoc(punto, "rigaAssistitodettFine", new Hashtable());
		md.write("rigaAssistitodettFine");
		//md.write("finetab");
		//		stampaDoc(punto, "fineassistito", new Hashtable());
		//		md.write("fineassistito");

	}

	private void mkBody(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		boolean first_time = true;

		String old_cart = "*", cart_ass = "";
		String old_oper = "*", cur_oper = "";
		String old_data = "*", cur_data = "";
		try {
			String data_inizio = "";
			String data_fine = "";
			int orapar = 0;
			int oregen = 0;
			int ora = 0;
			ServerUtility su = new ServerUtility();
			data_inizio = (String) (par.get("data_inizio"));
			data_fine = (String) (par.get("data_fine"));
			ISASRecord dbr = null; 
			while (dbcur.next()) {
				dbr = dbcur.getRecord();
				//gb 21/09/07	     cart_ass=((Integer)dbr.get("ag_cartella")).toString()+"/"+((Integer)dbr.get("ag_contatto")).toString();
				//gb 21/09/07 *******
				cart_ass = ((Integer) dbr.get("ag_cartella")).toString() + "/" + ((Integer) dbr.get("ag_contatto")).toString() + "/"
						+ (String) dbr.get("cod_obbiettivo") + "/" + ((Integer) dbr.get("n_intervento")).toString();
				//gb 21/09/07: fine *******

				cur_oper = (String) dbr.get("ag_oper_ref");
				cur_data = getDateField(dbr, "ag_data");
				if ((!cur_oper.equals(old_oper) && !old_oper.equals("*")) || (!cur_data.equals(old_data) && !old_data.equals("*"))) {//taglio nuova tabella
					
					md.write("fineassistito");
					verificaNote(md, dbc, dbr, old_data);
					mkFineTabella(md);
					md.write("taglia");
					old_oper = "*";
					old_cart = "*";
					old_data = "*";
				}
				/*else{
				if(!cur_data.equals(old_data)&& !old_data.equals("*")){
					md.write("fineassistito");
					mkFineTabella(md);
					md.write("taglia");
					mkInizioTabella(dbc,md,dbr);
				}
				}*/

				if (old_oper.equals("*"))
					mkInizioTabella(dbc, md, dbr);
				if (!cart_ass.equals(old_cart) && !old_cart.equals("*")) {//riga assistito
					md.write("fineassistito");
					faiRigaAssistito(dbc, dbr, md, par);
				}
				if (old_cart.equals("*"))
					faiRigaAssistito(dbc,dbr, md, par);
				old_oper = cur_oper;
				old_data = cur_data;
				old_cart = cart_ass;
				faiRigaIntervento(dbr, md);
				

			}//end while
			md.write("fineassistito");
			verificaNote(md, dbc, dbr, old_data);
			mkFineTabella(md);
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.mkBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkBody()");
		}
	}
	
	private void mkBodySin(mergeDocument md, ISASCursor dbcur, Hashtable par, ISASConnection dbc) throws Exception {
		boolean first_time = true;

		String old_cart = "*", cart_ass = "";
		String old_oper = "*", cur_oper = "";
		String old_data = "*", cur_data = "";
		try {
			String data_inizio = "";
			String data_fine = "";
			int orapar = 0;
			int oregen = 0;
			int ora = 0;
			ServerUtility su = new ServerUtility();
			data_inizio = (String) (par.get("data_inizio"));
			data_fine = (String) (par.get("data_fine"));
			ISASRecord dbr = null; 
			
			while (dbcur.next()) {
				
				dbr = dbcur.getRecord();
				//gb 21/09/07	     cart_ass=((Integer)dbr.get("ag_cartella")).toString()+"/"+((Integer)dbr.get("ag_contatto")).toString();
				//gb 21/09/07 *******
				cart_ass = ((Integer) dbr.get("ag_cartella")).toString() + "/" + ((Integer) dbr.get("ag_contatto")).toString() + "/"
						+ (String) dbr.get("cod_obbiettivo") + "/" + ((Integer) dbr.get("n_intervento")).toString();
				//gb 21/09/07: fine *******

				cur_oper = (String) dbr.get("ag_oper_ref");
				cur_data = getDateField(dbr, "ag_data");
				
				if ((!cur_oper.equals(old_oper) && !old_oper.equals("*")) || (!cur_data.equals(old_data) && !old_data.equals("*"))) {//taglio nuova tabella
					
					md.write("fineassistito");
					verificaNote(md, dbc, dbr, old_data);
					mkFineTabella(md);
					md.write("taglia");
					old_oper = "*";
					old_cart = "*";
					old_data = "*";
					
				}
				/*else{
				if(!cur_data.equals(old_data)&& !old_data.equals("*")){
					md.write("fineassistito");
					mkFineTabella(md);
					md.write("taglia");
					mkInizioTabella(dbc,md,dbr);
				}
				}*/
				
				
				if (old_oper.equals("*")){
					
					mkInizioTabellaSin(dbc, md, dbr, par);
				}
				
				if (!cart_ass.equals(old_cart) && !old_cart.equals("*")) {//riga assistito
					md.write("fineassistito");
					faiRigaAssistitoSin(dbc, dbr, md, par);
					
				}
				if (old_cart.equals("*")){							
					faiRigaAssistitoSin(dbc,dbr, md, par);
					
				}
				
				
				old_oper = cur_oper;
				old_data = cur_data;
				old_cart = cart_ass;
				//faiRigaInterventoSin(dbc,dbr, md, par);
				

			}//end while
			md.write("fineassistito");
			verificaNote(md, dbc, dbr, old_data);
			mkFineTabella(md);
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.mkBody(): " + e);
			e.printStackTrace();
			throw new SQLException("Errore eseguendo mkBody()");
		}
	}

	public void verificaNote(mergeDocument md, ISASConnection dbc, ISASRecord dbr, String dataDiario) throws Exception {
		String note = "";
		if(conf_note){
			note=getNoteDiario(dbc,dbr, dataDiario);
			if(ISASUtil.valida(note)){
				Hashtable<String, String> dati = new Hashtable<String, String>();
				dati.put("#note#", note);
				md.writeSostituisci("note", dati);
			}
		}
	}

	private String getNoteDiario(ISASConnection dbc, ISASRecord dbr, String dataDiario) {
		String punto = MIONOME + "getNoteDiario ";
		String strTesto="";
		Statement stmt = null;
		ResultSet rset = null;
		try {
			String nCartella = ISASUtil.getValoreStringa(dbr, "ag_cartella");
			String nContatto = ISASUtil.getValoreStringa(dbr, "ag_contatto");
			String tipoOperatore = ISASUtil.getValoreStringa(dbr,"ag_tipo_oper");
//			String dataDiario = ISASUtil.getValoreStringa(dbr,"ag_data");
			if (ManagerDate.validaData(dataDiario)) {
				String query = "SELECT d.testo FROM rm_diario d WHERE d.n_cartella = " +nCartella+
						" AND d.n_contatto = " +nContatto + "  AND d.tipo_operatore = '" +tipoOperatore+
						"' AND d.data_diario = " +formatDate(dbc, dataDiario) + 
						" AND (d.progr_inse || '*' || d.progr_modi) IN ( SELECT MAX (x.progr_inse)||'*'|| MAX(x.progr_modi) FROM rm_diario x WHERE x.n_cartella = d.n_cartella " +
						" AND x.n_contatto = d.n_contatto AND x.tipo_operatore = d.tipo_operatore) ";
				LOG.trace(punto + " query>> " +query);
				
				stmt = dbc.conn.getConnection().createStatement();	
				rset = stmt.executeQuery(query);
				if (rset.next()) {
					Object testo = rset.getObject("testo");
					if (testo instanceof Clob) {
						Clob clob = (Clob) testo;
						long len = clob.length();
						LOG.debug("CLOB len = " + len);
						if (len > Integer.MAX_VALUE) {
							LOG.warn("il CLOB e' lungo " + len + " bytes, cioe' piu' di 2GB. restituisco i primi 2GB");
							len = Integer.MAX_VALUE;
						}
						strTesto = clob.getSubString(1, (int) len);
					} else {
						strTesto = testo.toString();
					}
					LOG.trace(punto + " prima >>" + strTesto);
					if (ISASUtil.valida(strTesto)) {
						strTesto = strTesto.replaceAll("<.*?>", "");
						strTesto = strTesto.replaceAll("&nbsp;", " ");
						strTesto = strTesto.replaceAll("&egrave;", "e'");
						strTesto = strTesto.replaceAll("&igrave;", "i'");
						strTesto = strTesto.replaceAll("&agrave;", "a'");
						strTesto = strTesto.replaceAll("&ugrave;", "u'");
						strTesto = strTesto.replaceAll("&ograve;", "o'");
						strTesto = strTesto.replaceAll("&quot;", "\"");
						strTesto = strTesto.replaceAll("&rsquo;", "'");
					}
					LOG.trace(punto + " dopo >>" + strTesto);
				}else {
					LOG.trace(punto + " RECORD NON RECUPERATO ");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			close_rset_nothrow("queryKey", rset);
			close_stmt_nothrow("queryKey", stmt);
			
		}
		return strTesto;
	}	
	
	/**
	* restituisce la select per la stampa sintetica.
	*/

	private String getSelect(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getSelect ";

		String s = "SELECT ag_cartella,ag_oper_ref,ag_tipo_oper,"
				+ "ap_prest_cod || ' ' || nvl(trim(prest_des),'') prestazione,"
				+
				//gb 21/09/07             "ag_contatto,ag_orario,ap_prest_qta, ag_data,ap_stato,"+
				"ag_contatto, cod_obbiettivo, n_intervento, anagra_c.telefono1, anagra_c.telefono2, ag_orario,ap_prest_qta, ag_data,ap_stato,"
				+ //gb 21/09/07
				//gb 21/09/07             "ag_cartella || '/' || ag_contatto || '-' || nvl(trim(cognome),'') || ' ' ||  nvl(trim (nome),'') assistito,"+
				//gb 21/09/07 *******
				//"ag_cartella || '/' || ag_contatto || '/' || cod_obbiettivo || '/' || n_intervento || '-' || nvl(trim(car.cognome),'') || ' ' ||  nvl(trim (car.nome),'') assistito,"
				"ag_cartella || '-' || nvl(trim(car.cognome),'') || ' ' ||  nvl(trim (car.nome),'') assistito,"
				+
				//gb 21/09/07: fine *******
				"nvl(trim(dom_indiriz),'') || ' ' ||  nvl(trim (comuni.descrizione),'')"
				+ " || ' ' ||  nvl(trim (dom_prov),'')  indirizzo, anagra_c.nome_camp, anagra_c.cod_med, st_data, car.data_nasc, pres.despres "
				+
				//gb 21/09/07             " from cartella,anagra_c,agenda_interv,agenda_intpre,comuni,prestaz "+
				//gb 21/09/07 *******
				" from cartella car, anagra_c, agendant_interv aiv, agendant_intpre aie, comuni, prestaz, operatori oper, presidi pres "
				+
				//gb 21/09/07: fine *******
				" where " + " prest_cod = ap_prest_cod " + " and ap_data = ag_data" + " and ap_progr = ag_progr"
				+ " and ag_oper_ref = ap_oper_ref" + " and car.n_cartella = aiv.ag_cartella"
				+ " and data_variazione in (select max(data_variazione) from anagra_c where " + " anagra_c.n_cartella = aiv.ag_cartella)"
				+ " and anagra_c.n_cartella = aiv.ag_cartella" + " and comuni.codice = dom_citta";

		try {
			String w = getSelectParteWhere(dbc, par);
			if (!w.equals(""))
				s = s + " and " + w;
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.getSelect(): " + e);
			e.printStackTrace();
		}
		
		//gb 21/09/07        s = s + " ORDER BY ag_oper_ref,ag_data,ag_cartella,ag_contatto,ag_orario";
		if (par.get("ord").equals("A"))
			s = s + " ORDER BY ag_cartella, ag_oper_ref, ag_data, ag_contatto, cod_obbiettivo, n_intervento, ag_orario";
		else
			s = s + " ORDER BY ag_oper_ref, ag_data, ag_cartella, ag_contatto, cod_obbiettivo, n_intervento, ag_orario"; //gb 21/09/07
		stampaQuery(punto, s);
		return s;
	}
	
	private String getSelectSintetica(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getSelectSintetica ";

		String s = "SELECT ag_cartella,ag_oper_ref,ag_tipo_oper,"+
				   " count(ap_prest_cod) as numeroprestazioni, " +				
				   " ag_contatto, cod_obbiettivo, n_intervento, anagra_c.telefono1, anagra_c.telefono2, ag_orario, ag_data,ap_stato,"+
				   " ag_cartella || '-' || nvl(trim(car.cognome),'') || ' ' ||  nvl(trim (car.nome),'') assistito,"+
				   " nvl(trim(dom_indiriz),'') || ' ' ||  nvl(trim (comuni.descrizione),'')"+
				   " || ' ' ||  nvl(trim (dom_prov),'')  indirizzo, anagra_c.nome_camp, anagra_c.cod_med, st_data, car.data_nasc, pres.despres, ap_prest_cod "+
				   " from cartella car, anagra_c, agendant_interv aiv, agendant_intpre aie, comuni, operatori oper, presidi pres "+
				   " where " + " ap_data = ag_data" + " and ap_progr = ag_progr"
				+ " and ag_oper_ref = ap_oper_ref" + " and car.n_cartella = aiv.ag_cartella"
				+ " and data_variazione in (select max(data_variazione) from anagra_c where " + " anagra_c.n_cartella = aiv.ag_cartella)"
				+ " and anagra_c.n_cartella = aiv.ag_cartella" + " and comuni.codice = dom_citta";

		try {
			String w = getSelectParteWhere(dbc, par);
			if (!w.equals(""))
				s = s + " and " + w;
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.getSelect(): " + e);
			e.printStackTrace();
		}
		s = s + " group by ag_cartella, ag_oper_ref, ag_tipo_oper, ag_contatto, cod_obbiettivo, n_intervento,"+
				"anagra_c.telefono1, anagra_c.telefono2, ag_orario, ag_data, ap_stato,car.cognome,"+
				"car.nome, dom_indiriz,comuni.descrizione,dom_prov, anagra_c.nome_camp,anagra_c.cod_med,"+
				"st_data, car.data_nasc, pres.despres, ap_prest_cod";
		//gb 21/09/07        s = s + " ORDER BY ag_oper_ref,ag_data,ag_cartella,ag_contatto,ag_orario";
		if (par.get("ord").equals("A"))
			s = s + " ORDER BY ag_cartella, ag_oper_ref, ag_data, ag_contatto, cod_obbiettivo, n_intervento, ag_orario";
		else
			s = s + " ORDER BY ag_oper_ref, ag_data, ag_cartella, ag_contatto, cod_obbiettivo, n_intervento, ag_orario"; //gb 21/09/07
		stampaQuery(punto, s);
		return s;
	}
	
	private String getSelectSinteticaPrelievi(ISASConnection dbc, Hashtable par, String prelievo, String data, String cartella) {
		String punto = MIONOME + "getSelectSinteticaPrelievi ";

		String s = "SELECT ag_cartella,ag_oper_ref,ag_tipo_oper,"+
				   " count(ap_prest_cod) as numeroprelievi, " +				
				   " ag_contatto, cod_obbiettivo, n_intervento, anagra_c.telefono1, anagra_c.telefono2, ag_orario, ag_data,ap_stato,"+
				   " ag_cartella || '-' || nvl(trim(car.cognome),'') || ' ' ||  nvl(trim (car.nome),'') assistito,"+
				   " nvl(trim(dom_indiriz),'') || ' ' ||  nvl(trim (comuni.descrizione),'')"+
				   " || ' ' ||  nvl(trim (dom_prov),'')  indirizzo, anagra_c.nome_camp, anagra_c.cod_med, st_data, car.data_nasc, pres.despres "+
				   " from cartella car, anagra_c, agendant_interv aiv, agendant_intpre aie, comuni, operatori oper, presidi pres "+
				   " where " + " ap_data = ag_data" + " and ap_progr = ag_progr"
				+ " and ag_oper_ref = ap_oper_ref" + " and car.n_cartella = aiv.ag_cartella"
				+ " and data_variazione in (select max(data_variazione) from anagra_c where " + " anagra_c.n_cartella = aiv.ag_cartella)"
				+ " and anagra_c.n_cartella = aiv.ag_cartella" + " and comuni.codice = dom_citta"
				+ " and ap_prest_cod =" + prelievo;

		try {
			if (ISASUtil.valida(data))
					s = s+ " and ag_data = " + formatDate(dbc, data);
			if (ISASUtil.valida(cartella))
				s = s+ " and ag_cartella = " + cartella;
			String w = getSelectParteWherePrelievi(dbc, par);
			if (!w.equals(""))
				s = s + " and " + w;
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.getSelect(): " + e);
			e.printStackTrace();
		}
		s = s + "group by ag_cartella, ag_oper_ref, ag_tipo_oper, ag_contatto, cod_obbiettivo, n_intervento,"+
				"anagra_c.telefono1, anagra_c.telefono2, ag_orario, ag_data, ap_stato,car.cognome,"+
				"car.nome, dom_indiriz,comuni.descrizione,dom_prov, anagra_c.nome_camp,anagra_c.cod_med,"+
				"st_data, car.data_nasc, pres.despres";
		//gb 21/09/07        s = s + " ORDER BY ag_oper_ref,ag_data,ag_cartella,ag_contatto,ag_orario";
		if (par.get("ord").equals("A"))
			s = s + " ORDER BY ag_cartella, ag_oper_ref, ag_data, ag_contatto, cod_obbiettivo, n_intervento, ag_orario";
		else
			s = s + " ORDER BY ag_oper_ref, ag_data, ag_cartella, ag_contatto, cod_obbiettivo, n_intervento, ag_orario"; //gb 21/09/07
		stampaQuery(punto, s);
		return s;
	}

	private String getSelectPlanOperatori(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getSelectPlanOperatori ";
		stampa(punto + " Inizio con dati>" + par + "<\n");

		String zona = ISASUtil.getValoreStringa(par, "zona");
		String distretto = ISASUtil.getValoreStringa(par, "distretto");
		String presidio = ISASUtil.getValoreStringa(par, "codpres");
		boolean datiPresidioPresente = (ISASUtil.valida(zona) || ISASUtil.valida(distretto) || ISASUtil.valida(presidio));
		stampa(punto + " Usare il presidio>" + datiPresidioPresente + "<");

		String s = "SELECT ag_data, oper.cognome,  oper.nome, car.cognome ass_cogn, car.nome ass_nome,ag_orario, "
				+ " ap_prest_cod || ' ' || NVL (TRIM (prest_des), '') prestazione, oper.codice, ag_cartella,  ag_oper_ref, "
				+ " ag_tipo_oper,ap_stato, ag_contatto, cod_obbiettivo, n_intervento, " +
						"st_data," +//aggiunto bargi 08/03/2013
						"anagra_c.*, " +
						"comuni.descrizione comune_descr, "
				+ " ap_prest_qta, ag_data, car.data_nasc " +
				" from cartella car, anagra_c, agendant_interv aiv, agendant_intpre aie, "
				+ " comuni, prestaz, operatori oper ";

		s += (datiPresidioPresente ? ", presidi pres " : " ");
		s += " where " + " prest_cod = ap_prest_cod " + " and ap_data = ag_data" + " and ap_progr = ag_progr"
				+ " and ag_oper_ref = ap_oper_ref" + " and car.n_cartella = aiv.ag_cartella"
				+ " and data_variazione in (select max(data_variazione) from anagra_c where " + " anagra_c.n_cartella = aiv.ag_cartella)"
				+ " and anagra_c.n_cartella = aiv.ag_cartella" + " and comuni.codice = dom_citta ";
		//		//		TODO togliere il codice della cartella
		//		s += " AND car.n_cartella = 4203 ";

		try {
			String w = getSelectParteWhereOperatori(dbc, par, datiPresidioPresente);

			if (!w.equals(""))
				s = s + " and " + w;
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.getSelect(): " + e);
			e.printStackTrace();
		}
		if (par.get("ord").equals("A"))
			s = s + " ORDER BY ass_cogn, ass_nome, oper.cognome, oper.nome,  aiv.ag_data, ag_orario, prestazione ";
		else
			s = s + " ORDER BY oper.cognome, oper.nome, ass_cogn, ass_nome, aiv.ag_data, ag_orario, prestazione ";

		stampaQuery(punto, s);
		return s;
	}

	private String getSelectParteWhereOperatori(ISASConnection dbc, Hashtable par, boolean datiPresidioPresente) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = MIONOME + "getSelectParteWhereOperatori ";
		ServerUtility su = new ServerUtility();

		String dataInizio = ISASUtil.getValoreStringa(par, "dataIniziSettimana");
		String s = "";
		if (ISASUtil.valida(dataInizio)) {
			s = su.addWhere("", su.REL_AND, "ag_data", su.OP_GE_NUM, formatDate(dbc, dataInizio));
		} else {
			dataInizio = ISASUtil.getValoreStringa(par, "data_inizio");
			if (ISASUtil.valida(dataInizio)) {
				s = su.addWhere("", su.REL_AND, "ag_data", su.OP_GE_NUM, formatDate(dbc, dataInizio));
			}
		}

		String dataFine = ISASUtil.getValoreStringa(par, "dataFineSettimana");
		if (ISASUtil.valida(dataFine)) {
			s = su.addWhere(s, su.REL_AND, "ag_data", su.OP_LE_NUM, formatDate(dbc, dataFine));
		} else {
			dataFine = ISASUtil.getValoreStringa(par, "data_fine");
			if (ISASUtil.valida(dataFine)) {
				s = su.addWhere(s, su.REL_AND, "ag_data", su.OP_LE_NUM, formatDate(dbc, dataFine));
			}
		}

		s = su.addWhere(s, su.REL_AND, "ag_oper_ref", su.OP_EQ_STR, (String) par.get("codope"));
		String figprof = "";
		if (!par.get("figprof").equals("00"))
			figprof = (String) par.get("figprof");
		s = su.addWhere(s, su.REL_AND, "ag_tipo_oper", su.OP_EQ_STR, figprof);
		if (datiPresidioPresente) {
			s = su.addWhere(s, su.REL_AND, "pres.codzon", su.OP_EQ_STR, (String) par.get("zona"));
			s = su.addWhere(s, su.REL_AND, "pres.coddistr", su.OP_EQ_STR, (String) par.get("distretto"));
			s = su.addWhere(s, su.REL_AND, "pres.codpres", su.OP_EQ_STR, (String) par.get("codpres"));
			s += " AND oper.cod_presidio = pres.codpres ";
			s += " AND pres.codazsan = '" + (String) par.get("codazsan") + "'" + " AND pres.codreg = '" + (String) par.get("codreg") + "'";
		} else {
			stampa(punto + "\n NON metto il filtro per distretto!!!!");
		}

		s += " AND oper.codice = aiv.ag_oper_ref ";
		
		if (ISASUtil.valida(par.get("ass").toString()))
			s +=" AND ag_cartella=" + par.get("ass");
		if (ISASUtil.valida(par.get("pre").toString()))
			s +=" AND ap_prest_cod='" + (String) par.get("pre") + "'";
		String selectPrelievi = " select conf_txt from conf where conf_kproc='SINS' and conf_key='PRESTPREL'";
		ISASRecord dbr_prelievi = dbc.readRecord(selectPrelievi);
		String prelievi = (String) dbr_prelievi.get("conf_txt");
		if (par.get("tp").equals("P"))
			s +=" AND ap_prest_cod IN (" +prelievi +")";
		else if (par.get("tp").equals("AP"))
			s +=" AND ap_prest_cod NOT IN (" +prelievi+")";
		
		if (ISASUtil.valida(par.get("esec").toString()))
			s +=" AND ag_oper_esec='" + (String) par.get("esec") + "'";
		
		return s;
	}
/*
	private String getSelectPlan(ISASConnection dbc, Hashtable par) {
		String punto = MIONOME + "getSelectPlan ";
		String s = "SELECT ag_data, oper.cognome,  oper.nome, car.cognome ass_cogn, car.nome ass_nome,ag_orario, "
				+ " ap_prest_cod || ' ' || NVL (TRIM (prest_des), '') prestazione, oper.codice, ag_cartella,  ag_oper_ref, "
				+ " ag_tipo_oper,ap_stato, ag_contatto, cod_obbiettivo, n_intervento, anagra_c.*, comuni.descrizione comune_descr, "
				+ " ap_prest_qta, ag_data  from cartella car, anagra_c, agendant_interv aiv, agendant_intpre aie, "
				+ " comuni, prestaz, operatori oper, presidi pres " + " where " + " prest_cod = ap_prest_cod " + " and ap_data = ag_data"
				+ " and ap_progr = ag_progr" + " and ag_oper_ref = ap_oper_ref" + " and car.n_cartella = aiv.ag_cartella"
				+ " and data_variazione in (select max(data_variazione) from anagra_c where " + " anagra_c.n_cartella = aiv.ag_cartella)"
				+ " and anagra_c.n_cartella = aiv.ag_cartella" + " and comuni.codice = dom_citta ";
		//		//		TODO togliere il codice della cartella
		//		s += " AND car.n_cartella = 4203 ";

		try {
			String w = getSelectParteWhere(dbc, par);
			if (!w.equals(""))
				s = s + " and " + w;
		} catch (Exception e) {
			debugMessage("FoAgendaGiornEJB.getSelect(): " + e);
			e.printStackTrace();
		}
		s = s + " ORDER BY oper.cognome, oper.nome, ass_cogn, ass_nome, aiv.ag_data, ag_orario, prestazione ";
		stampaQuery(punto, s);
		return s;
	}
*/
	/**
	* restituisce la parte where della select valorizzata secondo i parametri di ingresso.
	 * @throws DBSQLException 
	 * @throws DBMisuseException 
	 * @throws ISASPermissionDeniedException 
	 * @throws ISASMisuseException 
	*/
	private String getSelectParteWhere(ISASConnection dbc, Hashtable par) throws SQLException, ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		ServerUtility su = new ServerUtility();

		String dataInizio = ISASUtil.getValoreStringa(par, "dataIniziSettimana");
		String s = "";
		if (ISASUtil.valida(dataInizio)) {
			s = su.addWhere("", su.REL_AND, "ag_data", su.OP_GE_NUM, formatDate(dbc, dataInizio));
		} else {
			dataInizio = ISASUtil.getValoreStringa(par, "data_inizio");
			if (ISASUtil.valida(dataInizio)) {
				s = su.addWhere("", su.REL_AND, "ag_data", su.OP_GE_NUM, formatDate(dbc, dataInizio));
			}
		}

		String dataFine = ISASUtil.getValoreStringa(par, "dataFineSettimana");
		if (ISASUtil.valida(dataFine)) {
			s = su.addWhere(s, su.REL_AND, "ag_data", su.OP_LE_NUM, formatDate(dbc, dataFine));
		} else {
			dataFine = ISASUtil.getValoreStringa(par, "data_fine");
			if (ISASUtil.valida(dataFine)) {
				s = su.addWhere(s, su.REL_AND, "ag_data", su.OP_LE_NUM, formatDate(dbc, dataFine));
			}
		}
		s = su.addWhere(s, su.REL_AND, "ag_oper_ref", su.OP_EQ_STR, (String) par.get("codope"));
		String figprof = "";
		if (!par.get("figprof").equals("00"))
			figprof = (String) par.get("figprof");
		s = su.addWhere(s, su.REL_AND, "ag_tipo_oper", su.OP_EQ_STR, figprof);

		//gb 21/09/07 *******
		s = su.addWhere(s, su.REL_AND, "pres.codzon", su.OP_EQ_STR, (String) par.get("zona"));
		s = su.addWhere(s, su.REL_AND, "pres.coddistr", su.OP_EQ_STR, (String) par.get("distretto"));
		s = su.addWhere(s, su.REL_AND, "pres.codpres", su.OP_EQ_STR, (String) par.get("codpres"));

		s += " AND oper.codice = aiv.ag_oper_ref" + " AND oper.cod_presidio = pres.codpres";

		s += " AND pres.codazsan = '" + (String) par.get("codazsan") + "'" + " AND pres.codreg = '" + (String) par.get("codreg") + "'";
		//gb 21/09/07: fine *******

		/*gb 21/09/07 *******
		    	String presido="";
		        if(!par.get("codpres").equals(""))
				{
		              //presidio=(String)par.get("codpres");
				s=s+" AND ag_oper_ref in "+getOperatoriPresidio(dbc,par);
				}
		*gb 21/09/07: fine *******/
		
		if (ISASUtil.valida(par.get("ass").toString()))
			s +=" AND ag_cartella=" + par.get("ass");
		if (ISASUtil.valida(par.get("pre").toString()))
			s +=" AND ap_prest_cod='" + (String) par.get("pre") + "'";
		
		String selectPrelievi = " select conf_txt from conf where conf_kproc='SINS' and conf_key='PRESTPREL'";
		ISASRecord dbr_prelievi = dbc.readRecord(selectPrelievi);
		String prelievi = (String) dbr_prelievi.get("conf_txt");
		if (par.get("tp").equals("P"))
			s +=" AND ap_prest_cod IN (" +prelievi +")";
		else if (par.get("tp").equals("AP"))
			s +=" AND ap_prest_cod NOT IN (" +prelievi+")";
		
		
		if (ISASUtil.valida(par.get("esec").toString()))
			s +=" AND ag_oper_esec='" + (String) par.get("esec") + "'";
		
		return s;
	}
	
	private String getSelectParteWherePrelievi(ISASConnection dbc, Hashtable par) throws SQLException, ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		ServerUtility su = new ServerUtility();

		
		String s = "";
		
		s = su.addWhere(s, su.REL_AND, "ag_oper_ref", su.OP_EQ_STR, (String) par.get("codope"));
		String figprof = "";
		if (!par.get("figprof").equals("00"))
			figprof = (String) par.get("figprof");
		s = su.addWhere(s, su.REL_AND, "ag_tipo_oper", su.OP_EQ_STR, figprof);

		//gb 21/09/07 *******
		s = su.addWhere(s, su.REL_AND, "pres.codzon", su.OP_EQ_STR, (String) par.get("zona"));
		s = su.addWhere(s, su.REL_AND, "pres.coddistr", su.OP_EQ_STR, (String) par.get("distretto"));
		s = su.addWhere(s, su.REL_AND, "pres.codpres", su.OP_EQ_STR, (String) par.get("codpres"));

		s += " AND oper.codice = aiv.ag_oper_ref" + " AND oper.cod_presidio = pres.codpres";

		s += " AND pres.codazsan = '" + (String) par.get("codazsan") + "'" + " AND pres.codreg = '" + (String) par.get("codreg") + "'";
		//gb 21/09/07: fine *******

		/*gb 21/09/07 *******
		    	String presido="";
		        if(!par.get("codpres").equals(""))
				{
		              //presidio=(String)par.get("codpres");
				s=s+" AND ag_oper_ref in "+getOperatoriPresidio(dbc,par);
				}
		*gb 21/09/07: fine *******/
		
		if (ISASUtil.valida(par.get("ass").toString()))
			s +=" AND ag_cartella=" + par.get("ass");
		if (ISASUtil.valida(par.get("pre").toString()))
			s +=" AND ap_prest_cod='" + (String) par.get("pre") + "'";
		
		String selectPrelievi = " select conf_txt from conf where conf_kproc='SINS' and conf_key='PRESTPREL'";
		ISASRecord dbr_prelievi = dbc.readRecord(selectPrelievi);
		String prelievi = (String) dbr_prelievi.get("conf_txt");
		if (par.get("tp").equals("P"))
			s +=" AND ap_prest_cod IN (" +prelievi +")";
		else if (par.get("tp").equals("AP"))
			s +=" AND ap_prest_cod NOT IN (" +prelievi+")";
		
		if (ISASUtil.valida(par.get("esec").toString()))
			s +=" AND ag_oper_esec='" + (String) par.get("esec") + "'";
		return s;
	}

	private String getDescPresidio(ISASConnection dbc, Hashtable h) throws SQLException {
		boolean done = false;
		try {
			String myselect = "Select despres from presidi where " + "codreg='" + (String) h.get("codreg") + "' and " + "codazsan='"
					+ (String) h.get("codazsan") + "' and " + "codpres='" + (String) h.get("codpres") + "'";
			ISASRecord dbr = dbc.readRecord(myselect);
			System.out.println("Query presidi " + myselect);
			if (dbr != null)
				return (String) dbr.get("despres");
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una getDescPresidio()  ");
		}
	}

	private String getOperatoriPresidio(ISASConnection dbc, Hashtable h) throws SQLException {
		boolean done = false;
		ISASCursor dbcur = null;
		String ritorno = "";
		try {
			String myselect = "Select codice from operatori where " + "cod_presidio='" + (String) h.get("codpres") + "'";
			System.out.println("Query operatori " + myselect);
			dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			if (vdbr.size() > 0) {
				Enumeration e = vdbr.elements();
				String lista = "";
				while (e.hasMoreElements()) {
					String codice = (String) ((ISASRecord) e.nextElement()).get("codice");
					if (!lista.equals(""))
						lista += " , ";
					lista += "'" + codice + "'";
				}
				ritorno = "( " + lista + " )";
			} else
				ritorno = "";
			dbcur.close();
			done = true;
			return ritorno;

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una getOperatoriPresidio()  ");
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	private String getDescOperatori(ISASConnection dbc, String codice) throws SQLException {
		String punto = MIONOME + "getDescOperatori ";
		boolean done = false;
		ISASRecord dbr = null;
		String ritorno = "";
		try {
			String myselect = "Select nvl(trim(cognome),'') || nvl(trim(nome),'') " + " operatore from operatori where " + "codice='"
					+ codice + "'";
			//			stampa(punto + "Query> " + myselect + "<");
			dbr = dbc.readRecord(myselect);
			if (dbr != null) {
				ritorno = (String) dbr.get("operatore");
			} else
				ritorno = "";
			return ritorno;

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una getDescOperatori()  ");
		}
	}

	/**
	* restituisce un parametro data come stringa nel formato gg/mm/aaaa
	*/
	private String getStringDate(Hashtable par, String k) {
		try {
			String s = (String) par.get(k);
			s = s.substring(8, 10) + "/" + s.substring(5, 7) + "/" + s.substring(0, 4);
			return s;
		} catch (Exception e) {
			debugMessage("getStringDate(" + par + ", " + k + "): " + e);
			return "";
		}
	}

	/**
	* restituisce un parametro come stringa
	*/
	private String getStringField(ISASRecord dbr, String f) {
		try {
			return (dbr.get(f)).toString();
		} catch (Exception e) {
			debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
			return "";
		}
	}

	/**
	* restituisce un campo data come stringa
	*/
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

	/**
	* restituisce la decodifica della figura professionale
	*/
	private String getFiguraProfessionale(String tipo) {
		String fp = "";
		try {
			if (tipo.equals("00"))
				fp = "TUTTE LE FIGURE PROFESSIONALI";
			else if (tipo.equals("01"))
				fp = "Assistente sociale";
			else if (tipo.equals("02"))
				fp = "Infermiere";
			else if (tipo.equals("03"))
				fp = "Medico";
			else if (tipo.equals("04"))
				fp = "Fisioterapista";
			else
				fp = "FIGURA PROFESSIONALE NON VALIDA";
		} catch (Exception e) {
			fp = "FIGURA PROFESSIONALE ERRATA";
		}
		return fp;
	}
	
	public int ConvertData (String dataold,String datanew)
	{
	        //inizializzazione della variabile eta
	        int eta=0;
	        int tempeta=0;

	        //preparazione primo array
	        int[] datavecchia= new int[3];
	        Integer giorno = new Integer(dataold.substring(8,10));
	        datavecchia[0]= giorno.intValue();
	        Integer mese = new Integer(dataold.substring(5,7));
	        datavecchia[1]= mese.intValue();
	        Integer anno = new Integer(dataold.substring(0,4));
	        datavecchia[2]= anno.intValue();

	        //preparazione secondo array

	        int[] datanuova= new int[3];
	        Integer day = new Integer(datanew.substring(8,10));
	        datanuova[0]= day.intValue();
	        Integer mounth = new Integer(datanew.substring(5,7));
	        datanuova[1]= mounth.intValue();
	         Integer year = new Integer(datanew.substring(0,4));
	        datanuova[2]= year.intValue();

	        tempeta= datanuova[2]-datavecchia[2];
	        //confronto mese
	        if (datanuova[1] < datavecchia[1])
	                tempeta=tempeta-1;      // anni non ancora compiuti
	        else if (datanuova[1] == datavecchia[1])
	                if (datanuova[0] < datavecchia[0])      //confronto giorno
	                        tempeta=tempeta-1;      // anni non ancora compiuti
	        eta=tempeta;
	        return eta;
	}
	
	public static String getjdbcDate()
	{
	        java.util.Date d=new java.util.Date();
	        java.text.SimpleDateFormat local_dateFormat =
	                new java.text.SimpleDateFormat("yyyy-MM-dd");
	        return local_dateFormat.format(d);
	}
	
	private Calendar recuperaGiorno(String data) {
		String punto = MIONOME + " recupeaGiorno ";
		int giorno = -1, mese = -1, anno = -1;
		Calendar calender = null;
		if (ISASUtil.valida(data) && data.length() >= 10) {
			int pos = data.indexOf("-");
			if (pos > 0) {
				if (pos == 4) {
					// stampa(punto + " formato yyyy-mm-dd");
					if (pos > 0) {
						anno = getIntero(data.substring(0, 4));
					}
					String val = data.substring(pos + 1);
					pos = val.indexOf("-");
					if (pos > 0) {
						mese = getIntero(val.substring(0, pos));
						giorno = getIntero(val.substring(pos + 1));
					}
					mese = (mese > 0 ? mese - 1 : mese);
					calender = new GregorianCalendar(anno, mese, giorno);
					LOG.trace(punto + " formato yyyy-mm-dd giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + " dataRecuperata>"
							+ calender.get(Calendar.DAY_OF_MONTH) + "/" + calender.get(Calendar.MONTH) + "/" + calender.get(Calendar.YEAR)
							+ "<");
				} else {
					// stampa(punto + " formato dd-mm-yyyy");
					if (pos > 0) {
						giorno = getIntero(data.substring(0, 2));
					}
					String val = data.substring(pos + 1);
					pos = val.indexOf("-");
					if (pos > 0) {
						mese = getIntero(val.substring(0, pos));
						anno = getIntero(val.substring(pos + 1));
					}
					mese = (mese > 0 ? mese - 1 : mese);
					calender = new GregorianCalendar(anno, mese, giorno);
					LOG.trace(punto + " formato dd-mm-yyyy giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + " dataRecuperata>"
							+ calender.get(Calendar.DAY_OF_MONTH) + "/" + calender.get(Calendar.MONTH) + "/" + calender.get(Calendar.YEAR)
							+ "<");
				}
			} else {
				pos = data.indexOf("/");
				if (pos == 4) {
					LOG.debug(punto + " formato yyyy/mm/dd");
					if (pos > 0) {
						anno = getIntero(data.substring(0, 4));
					}
					String val = data.substring(pos + 1);
					pos = val.indexOf("/");
					if (pos > 0) {
						mese = getIntero(val.substring(0, pos));
						giorno = getIntero(val.substring(pos + 1));
					}
					mese = (mese > 0 ? mese - 1 : mese);
					calender = new GregorianCalendar(anno, mese, giorno);
					LOG.trace(punto + "formato dd-mm-yyyy  giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + " dataRecuperata>"
							+ calender.get(Calendar.DAY_OF_MONTH) + "/" + calender.get(Calendar.MONTH) + "/" + calender.get(Calendar.YEAR)
							+ "<");
				} else {
					// stampa(punto + " formato dd/mm/yyyy");
					if (pos > 0) {
						giorno = getIntero(data.substring(0, 2));
					}
					String val = data.substring(pos + 1);
					pos = val.indexOf("/");
					if (pos > 0) {
						mese = getIntero(val.substring(0, pos));
						anno = getIntero(val.substring(pos + 1));
					}
					mese = (mese > 0 ? mese - 1 : mese);
					calender = new GregorianCalendar(anno, mese, giorno);
					LOG.trace(punto + "formato dd/mm/yyyy giorno>" + giorno + "< mese>" + mese + "< anno>" + anno + " dataRecuperata>"
							+ calender.get(Calendar.DAY_OF_MONTH) + "/" + calender.get(Calendar.MONTH) + "/" + calender.get(Calendar.YEAR)
							+ "<");
				}
			}
		} else {
			LOG.trace(punto + " Data non valida \n ");
		}

		return calender;
	}

} // End of FoAgendaGiorn class

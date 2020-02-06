package it.caribel.app.sinssnt.bean;
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 01/09/2008 - EJB di connessione alla procedura SINS Tabella FoEleAccAss
//
//
// ==========================================================================

import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Hashtable;

public class FoEleAccAssEJB extends SINSSNTConnectionEJB {

it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

public FoEleAccAssEJB() {}

	private void preparaLayout(mergeDocument md, ISASConnection dbc, String data_inizio, String data_fine,
			String cartella, Hashtable par) {
		String punto = "preparaLayout ";
		Hashtable htxt = new Hashtable();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));

			ServerUtility su = new ServerUtility();
			String data = "";
			if (ManagerDate.validaData(data_inizio)) {
				data = data_inizio.substring(8, 10) + "/" + data_inizio.substring(5, 7) + "/"
						+ data_inizio.substring(0, 4);
			}
			htxt.put("#data_inizio#", data);
			data = "";
			if (ManagerDate.validaData(data_fine)) {
				data = data_fine.substring(8, 10) + "/" + data_fine.substring(5, 7) + "/" + data_fine.substring(0, 4);
			}
			htxt.put("#data_fine#", data);
			htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
			htxt.put(
					"#assistito#",
					cartella
							+ " "
							+ util.getDecode(dbc, "cartella", "n_cartella", cartella,
									"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))", "assistito"));

			String codOperatore = ISASUtil.getValoreStringa(par, Costanti.COD_OPERATORE);
			//        String nCartella = ISASUtil.getValoreStringa(par, Costanti.CTS_INT_CARTELLA);
			String codZona = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_ZONE);
			String codDistretto = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_DISTRETTO);
			String codPresidio = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_STP_REPORT_PCA);

			String dettaglioOperatore = "";
			if (ISASUtil.valida(codOperatore)) {
				dettaglioOperatore = ManagerDecod.recuperaLabels("accessi.effettuati.ricerca.operatore")+": "+
						util.getDecode(dbc, "operatori", "codice", codOperatore,
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))", "assistito");
			}
			htxt.put("#dettaglio_operatore#", dettaglioOperatore);

			String dettaglioAssistito = "";
			if (ISASUtil.valida(cartella)) {
				dettaglioAssistito = ManagerDecod.recuperaLabels("accessi.effettuati.ricerca.assistito")+": "+ util.getDecode(dbc, "cartella", "n_cartella", cartella,
						"(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))", "assistito");
			}
			htxt.put("#dettaglio_assistito#", dettaglioAssistito);

			String dettaglioUbucazione = "";
			if (ISASUtil.valida(codZona)) {
				dettaglioUbucazione = ManagerDecod.recuperaLabels("generic.zona") + ": "
						+ util.getDecode(dbc, "zone", "codice_zona", codZona, "descrizione_zona");

			}
			if (ISASUtil.valida(codDistretto)) {
				dettaglioUbucazione += " " + ManagerDecod.recuperaLabels("generic.distretto") + ": "
						+ util.getDecode(dbc, "distretti", "cod_distr", codDistretto, "des_distr");
			}
			if (ISASUtil.valida(codPresidio)) {
				String desPresidio = "";
				try {
					SkInfEJB skInfEJB = new SkInfEJB();
					desPresidio = skInfEJB.decodPresidio(dbc, codPresidio, dbc.getKuser());
				} catch (Exception e) {
					LOG.error(punto + " Errore nel recuperare la decodifica del presidio ");
				}
				dettaglioUbucazione += " " + ManagerDecod.recuperaLabels("PanelUbicazione.presidioCombo") + ": " + desPresidio;
			}
			htxt.put("#dettaglio_ubucazione#", dettaglioUbucazione);

		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		md.writeSostituisci("layout", htxt);
	}

public byte[] query_report(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	byte[] rit;
	IntInfEJB intInfEJB = new IntInfEJB();

        String data_ini="";
        String data_fine="";
	boolean entrato=false;
	try{
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);
		String cartella=ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_INT_CARTELLA);
		String dataFine=ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE);
		String dataInizio=ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO);
		
////		if (par.get("data_fine") != null && !((String)par.get("data_fine")).equals(""))
////    		    data_fine=(String)par.get("data_fine");
////
////                if (par.get("data_inizio") != null && !((String)par.get("data_inizio")).equals(""))
////		    data_ini=(String)par.get("data_inizio");
//
//		
//		String tipo=(String)par.get("tipo");
//                String cartella=(String)par.get("cartella");
//                String contatto=(String)par.get("contatto");
//
//		// 26/01/09 m.: se accessi SOC si deve filtrare con n_progetto e NON n_contatto ---
//		String nmFldCont = "int_contatto";
//		if ("01".equals(tipo))
//			nmFldCont = "n_progetto";
//		// 26/01/09 m. --------------------------------------------------------------------
//
//// 26/01/09		String myselect = "SELECT DISTINCT a.int_data_prest data_prest, a.int_tempo tempo_prest,"+
//		String myselect = "SELECT a.int_data_prest data_prest, a.int_tempo tempo_prest, a.*,"+ // 26/01/09
//			          " e.prest_des prest_des, c.tippre_des tipo_prest, "+
//                                  " nvl(trim(d.cognome),'') ||' ' || nvl(trim(d.nome),'') operatore FROM"+
//                                  " interv a, intpre b, tippre c, operatori d, prestaz e  WHERE "+
//                                  " a.int_cartella = " + cartella +
//// 26/01/09	   						" AND a.int_contatto = " + contatto +
//							   		(ISASUtil.valida(contatto) ? " AND a." + nmFldCont + " = " + contatto:"")
//                                   + // 26/01/09
//                                  " AND a.int_data_prest>="+formatDate(dbc, data_ini)+
//                                  " AND a.int_data_prest<="+formatDate(dbc, data_fine)+
//                                  " AND a.int_anno = b.pre_anno and a.int_contatore = b.pre_contatore "+
//                                  " AND a.int_tipo_prest = c.tippre_cod and a.int_cod_oper = d.codice "+
//                                  (ISASUtil.valida(tipo) ? " AND d.tipo='"+ tipo +"'": "")+
//                                  " AND e.prest_cod = b.pre_cod_prest order by a.int_data_prest desc";
//
//                System.out.println("Select FoEleAccAss=="+ myselect);
		String myselect = intInfEJB.recuperaQueryAccessi(dbc, par);
			
		
		ISASCursor dbcur=dbc.startCursor(myselect);
                if (dbcur == null) {
			preparaLayout(doc,dbc,dataInizio,dataFine,cartella,par);
			doc.write("messaggio");
			doc.write("finale");
		} else	{
			if (dbcur.getDimension() <= 0) {
				preparaLayout(doc,dbc,dataInizio,dataFine,cartella,par);
				doc.write("messaggio");
				doc.write("finale");
			} else	{
				preparaLayout(doc,dbc,dataInizio,dataFine,cartella,par);
				preparaBody(doc, dbcur);
				doc.write("finale");
			}	// fine if dbcur.getDimension()
		}	// fine if dbcur
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
                //System.out.println("Rit:"+new String(doc.get()));
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_ospdim()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}

	private void preparaBody(mergeDocument md, ISASCursor dbcur) {
		md.write("iniziotab");
		int conta = 0;
		Hashtable p = new Hashtable();
		try {
			ISASRecord dbrOld =null;
			while (dbcur.next()) {
				conta++;
				ISASRecord dbr = dbcur.getRecord();
//				gestioneAperturaTabella(md, dbr, dbrOld);
				p.put("#data#", (String) util.getObjectField(dbr, "data_prest", 'T'));
				p.put("#prestazione#", (String) util.getObjectField(dbr, "prest_des", 'S'));
				p.put("#tipo#", (String) util.getObjectField(dbr, "tipo_prest", 'S'));
				p.put("#tempo#", (String) util.getObjectField(dbr, "tempo_prest", 'I'));
				p.put("#operatore#", (String) util.getObjectField(dbr, "operatore", 'S'));
				md.writeSostituisci("tabella", p);
				dbrOld = dbr;
			} // fine while
		} catch (Exception ex) {
			p.put("#data#", "");
			p.put("#prestazione#", "*** errore in lettura: " + ex + " ***");
			p.put("#tipo#", "");
			p.put("#tempo#", "");
			p.put("#operatore#", "");
			md.writeSostituisci("tabella", p);
		}
		md.write("finetab");
		p.put("#totale#", "" + conta);
		md.writeSostituisci("totale", p);
	}

	private void gestioneAperturaTabella(ISASConnection dbc, mergeDocument md, ISASRecord dbr, ISASRecord dbrOld) {
		String zona, distretto, presidio;
		String zonaOld, distrettoOld, presidioOld;
		String nCartella, nCartellaOld;
		String codOperatore, codOperatoreOld;
		
		String dettaglioOperatore = "";
		String dettaglioUbicazione = "";
		String dettaglioAssistito = "";
		
		if (dbrOld == null) {
			zona = ISASUtil.getValoreStringa(dbr, "zona");
			distretto = ISASUtil.getValoreStringa(dbr, "distretto");
			presidio = ISASUtil.getValoreStringa(dbr, "presidio");
			
			nCartella= ISASUtil.getValoreStringa(dbr, "presidio");
			nCartella= ISASUtil.getValoreStringa(dbr, "presidio");
		}
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

}	// End of FoMAssEle class


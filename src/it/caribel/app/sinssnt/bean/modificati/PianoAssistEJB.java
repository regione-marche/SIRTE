package it.caribel.app.sinssnt.bean.modificati;




// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//********NUOVA RELEASE GIUGNO 2012 bargi

//--rivisto per agenda bargi 01/06/2012 versione 12_03_01 in poi  

// - EJB di connessione alla procedura SINS Tabella PianoAssist

// ==========================================================================


import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.SkFpgEJB;
import it.caribel.app.sinssnt.controllers.agenda.CostantiAgenda;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CaricaAgendaPrestazioni;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.dateutility;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PianoAssistEJB extends SINSSNTConnectionEJB  {

		// 06/10/06 m.: aggiunto generazione di CariException quando si tenta di fare
		//	una INSERT ed esiste gi� un record con la stessa chiave. Il problema potrebbe
		//	sussistere per le tabelle con data in chiave: modificato metodi "insert_pianoVer()" 
		// e "insert_all()"
private String nomeEJB="PianoAssistEJB";
private String metodo="";
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
private final Log LOG = LogFactory.getLog(getClass());//elisa b 15/05/12
dateutility dt=new dateutility();//bargi 05/06/2012
CaricaAgendaPrestazioni cap = new CaricaAgendaPrestazioni();

/* elisa b 21/06/16: due dei possibili stati del piano assistenziale */
private static String IN_ELABORAZIONE = "0",
	DA_INVIARE = "1",
	//tipo operazione 
	INVIO_PIANO = "IP";

public PianoAssistEJB() {}

private void decodificaOperatore(ISASConnection mydbc, ISASRecord dbr) throws Exception
  {
  String strCodOperatore = (String) dbr.get("pa_operatore");
  String strCognome = "";
  String strNome = "";
 
  if ((strCodOperatore == null) || strCodOperatore.equals(""))
    {
    dbr.put("operCognome", "");
    dbr.put("operNome", "");
    dbr.put("operCognNome", "");
    return;
    }

  String selS = "SELECT cognome, nome" +
                " FROM operatori" +
                " WHERE codice = '" + strCodOperatore + "'";

  ISASRecord rec = mydbc.readRecord(selS);

  if (rec != null)
    {
    strCognome = (String)rec.get("cognome");
    strNome = (String)rec.get("nome");
    }

  if (strCognome == null)
    strCognome = "";
  dbr.put("operCognome", strCognome.trim());
  if (strNome == null)
    strNome = "";
  dbr.put("operNome", strNome.trim());

  dbr.put("operCognNome", strCognome.trim() + " " + strNome.trim());
  }
private String getOperatore(ISASConnection dbc, String codice) throws Exception {
	String decod="";
	try{
		String sel = "SELECT cognome,nome FROM operatori "+
			"WHERE codice = '"+codice.trim()+"'";
		ISASRecord dbcom = dbc.readRecord(sel);
		String cognome=(String)dbcom.get("cognome");
		String nome=(String)dbcom.get("nome");
		decod=cognome + " " + nome;
	} catch(Exception e) {
		debugMessage("getOperatore("+dbc+", "+decod+"): "+e);
		return "";
	}
	   return decod;
	}
private void decodificaFrequenza(ISASConnection mydbc, ISASRecord dbr) throws Exception
  {
  String strCodice = (String) dbr.get("pi_freq");
  String strDescrizione = "";

  if ((strCodice == null) || strCodice.equals(""))
    {
    dbr.put("frequenza", strDescrizione);
    return;
    }

  String selS = "SELECT tab_descrizione" +
                " FROM tab_voci" +
                " WHERE tab_cod = 'FREQAC'" +
                " AND tab_val = '" + strCodice + "'";

  ISASRecord rec = mydbc.readRecord(selS);

  if (rec != null)
    {
    strDescrizione = (String)rec.get("tab_descrizione");
    }
  if (strDescrizione == null)
    strDescrizione = "";
  dbr.put("frequenza", strDescrizione);
  }

private void decodificaPrestaz(ISASConnection mydbc, ISASRecord dbr) throws Exception
  {
  String strCodice = (String) dbr.get("pi_prest_cod");
  String strDescrizione = "";

  if ((strCodice == null) || strCodice.equals(""))
    {
    dbr.put("pi_prest_desc", strDescrizione);
    return;
    }

  String selS = "SELECT prest_des" +
                " FROM prestaz" +
                " WHERE prest_cod = '" + strCodice + "'";

  ISASRecord rec = mydbc.readRecord(selS);

  if (rec != null)
    {
    strDescrizione = (String)rec.get("prest_des");
    }
  if (strDescrizione == null)
    strDescrizione = "";
  dbr.put("pi_prest_desc", strDescrizione);
  }

public ISASRecord queryKey_pianoAss(myLogin mylogin, Hashtable h) throws SQLException{
	boolean done = false;
	String methodName = "queryKey_pianoAss";

	ISASConnection dbc = null;

	String strPaTipoOper = (String) h.get("pa_tipo_oper");
	String strNCartella = (String) h.get("n_cartella");
	String strNProgetto = (String) h.get("n_progetto");
	String strCodObiettivo = (String) h.get("cod_obbiettivo");
	String strNIntervento = (String) h.get("n_intervento");
	String strPaData = (String) h.get("pa_data");

	try{
		dbc=super.logIn(mylogin);

		String mySelPianoAss = "SELECT *" +
				" FROM piano_assist" +
				" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
				" AND n_cartella = " + strNCartella +
				" AND n_progetto = " + strNProgetto +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
				" AND n_intervento = " + strNIntervento +
				" AND pa_data = " + formatDate(dbc, strPaData);
		LOG.info("PianoAssistEJB/queryKey_pianoAss - mySelPianoAss : " + mySelPianoAss);
		ISASRecord dbrPianoAss = dbc.readRecord(mySelPianoAss);

		if (dbrPianoAss != null){
			String mySelPianoVer = "SELECT *" +
					" FROM piano_verifica" +
					" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
					" AND n_cartella = " + strNCartella +
					" AND n_progetto = " + strNProgetto +
					" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
					" AND n_intervento = " + strNIntervento +
					" AND pa_data = " + formatDate(dbc, strPaData) +
					" ORDER BY ve_data";
			LOG.info("PianoAssistEJB/queryKey_pianoAss - mySelPianoVer : " + mySelPianoVer);
			ISASCursor dbcurPianoVer = dbc.startCursor(mySelPianoVer);
			Vector vdbrPianoVer = dbcurPianoVer.getAllRecord();
			if (vdbrPianoVer.size() > 0)
				dbrPianoAss.put("gridPianoVer", vdbrPianoVer);

			String mySelPianoAcc = "SELECT *" +
					" FROM piano_accessi" +
					" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
					" AND n_cartella = " + strNCartella +
					" AND n_progetto = " + strNProgetto +
					" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
					" AND n_intervento = " + strNIntervento +
					" AND pa_data = " + formatDate(dbc, strPaData) +
					" ORDER BY pi_prog";
			LOG.info("PianoAssistEJB/queryKey_pianoAss - mySelPianoAcc : " + mySelPianoAcc);
			ISASCursor dbcurPianoAcc = dbc.startCursor(mySelPianoAcc);
			Vector vdbrPianoAcc = dbcurPianoAcc.getAllRecord();
			if (vdbrPianoAcc.size() > 0)
				dbrPianoAss.put("gridPianoAcc", vdbrPianoAcc);
			// Decodifica dei 'pi_freq' e 'pi_prest_cod' in tutti gli ISASRecord del Vector
			decodificaQueryPianoAcc(dbc, vdbrPianoAcc);
			aggiungiFlagPianificata(dbc, vdbrPianoAcc);
		}//end

		if (dbrPianoAss != null){
			decodificaOperatore(dbc, dbrPianoAss);
			String indirizzo = selectAnagra(dbc, strNCartella);		
			dbrPianoAss.put("indirizzo", indirizzo);
		}

		dbc.close();
		super.close(dbc);
		done=true;
		return dbrPianoAss;
	} catch(Exception e){
		throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
	}finally {
		logout_nothrow(methodName, dbc);
	}
}

private void aggiungiFlagPianificata(ISASConnection dbc, Vector<ISASRecord> vdbr) throws Exception{
	ISASRecord dbr = null;
	for (int i=0; i<vdbr.size(); i++){
		dbr = (ISASRecord) vdbr.get(i);
		aggiungiFlagPianificata(dbc, dbr);
	}
}

private void aggiungiFlagPianificata(ISASConnection dbc, ISASRecord dbr) throws ISASMisuseException, SQLException {
	ISASRecord dbr1 = null;
	String mysel =	" SELECT MAX(ag.as_giorno_sett) as max_gs"+
					" FROM agendant_sett_tipo ag " +
					" WHERE ag.AS_TIPO_OPER   = "+ dbr.get("pa_tipo_oper") +
					" AND   ag.n_cartella     = "+ dbr.get("n_cartella") +
					" AND   ag.COD_OBBIETTIVO = '"+ dbr.get("cod_obbiettivo") + "'"+
					" AND   ag.n_intervento   = "+ dbr.get("n_intervento") +
					" AND   ag.as_data		  = "+ formatDate(dbc, dbr.get("pa_data").toString()) +
					" AND   ag.as_prog        = "+ dbr.get("pi_prog") + " ";
	try {
		dbr1=dbc.readRecord(mysel);
		dbr.put(CostantiSinssntW.PIANIFICAZIONE_PAI, CostantiAgenda.getPianificazioneVuota());
		if (dbr1 != null && dbr1.getHashtable().containsKey("max_gs")){
			dbr.put("pi_pianificato", "S");		   
		}else{
			mysel =	" SELECT pianificazione " +
					" FROM PAI p , rm_skso s " +
					" WHERE p.n_cartella = s.n_cartella AND s.pr_data_chiusura IS NULL AND s.ispianocongelato='S'" +
					" AND   p.PREST_COD = '"+ ISASUtil.getValoreStringa(dbr, "pi_prest_cod").trim() + "' " +
					" AND   p.n_cartella     = "+ dbr.get("n_cartella");
			dbr1=dbc.readRecord(mysel);
			if (dbr1 != null && dbr1.getHashtable().containsKey(CostantiSinssntW.PIANIFICAZIONE_PAI)){
				dbr.put(CostantiSinssntW.PIANIFICAZIONE_PAI, dbr1.getHashtable().get(CostantiSinssntW.PIANIFICAZIONE_PAI));		   
			}
		}
	} catch(Exception e){
		throw newEjbException("Errore eseguendo aggiungiFlagPianificata: " + e.getMessage(), e);
	}
}

private String selectAnagra(ISASConnection dbc,String cartella)throws SQLException{
 	try{
 		String mysel ="SELECT "+
 				"nvl(trim(indirizzo_rep),'') || ' ' ||  nvl(trim (comuni.descrizione),'')"+
 				" || ' ' ||  nvl(trim (prov_rep),'')  indirizzo"+
 				" from anagra_c,comuni "+
 				" where n_cartella="+cartella+
 				" and comuni.codice=comune_rep"+
 				" and data_variazione in (select max(data_variazione)"+
 				" from anagra_c where n_cartella="+cartella+")";

	  ISASRecord dbr=dbc.readRecord(mysel);
	  if (dbr != null){
		 return (String)dbr.get("indirizzo");
	  }
	return "";
	}catch(Exception e1){
	  debugMessage(""+e1);
	  throw new SQLException("Errore eseguendo una selectAnagra() - "+  e1);
	}

 }

private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr) throws Exception
  {
  for (int i=0; i<vdbr.size(); i++)
    {
    ISASRecord dbr = (ISASRecord) vdbr.get(i);
    decodificaOperatore(mydbc, dbr);
    }
  }

public Vector query_pianoAss(myLogin mylogin, Hashtable h) throws  SQLException
  {
	String methodName = "query_pianoAss";
  boolean done=false;
  ISASConnection dbc=null;
  ISASCursor dbcur = null;

  String strPaTipoOper = ISASUtil.getValoreStringa(h,"pa_tipo_oper");
  String strNCartella = ISASUtil.getValoreStringa(h,"n_cartella");
  String strNProgetto = ISASUtil.getValoreStringa(h,"n_progetto");
  String strCodObiettivo = ISASUtil.getValoreStringa(h,"cod_obbiettivo");
  String strNIntervento = ISASUtil.getValoreStringa(h,"n_intervento");

   try
    {
    dbc = super.logIn(mylogin);

    String mySelect = "SELECT *" +
                      " FROM piano_assist" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " ORDER BY pa_data DESC";
    LOG.debug("PianoAssistEJB/query_pianoAss - mySelect : " + mySelect);
    dbcur = dbc.startCursor(mySelect);
    Vector vdbr = dbcur.getAllRecord();

    // Decodifica dei Cognomi e Nomi degli operatori in tutti gli ISASRecord del Vector
    decodificaQueryInfo(dbc, vdbr);

    done=true;
    return vdbr;
	} catch(Exception e){
		throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
	}finally {
		close_dbcur_nothrow(methodName, dbcur);
		logout_nothrow(methodName, dbc);
	}
  }

public ISASRecord insert_pianoAss(myLogin mylogin, Hashtable htPianoAss)
                  throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done=false;
  ISASConnection dbc=null;
  String strPaTipoOper = (String) htPianoAss.get("pa_tipo_oper");
  String strNCartella = (String) htPianoAss.get("n_cartella");
  String strNProgetto = (String) htPianoAss.get("n_progetto");
  String strCodObiettivo = (String) htPianoAss.get("cod_obbiettivo");
  String strNIntervento = (String) htPianoAss.get("n_intervento");
  String strPaData = (String) htPianoAss.get("pa_data");

  try
    {
    dbc = super.logIn(mylogin);

    ISASRecord dbr = dbc.newRecord("piano_assist");
    Enumeration n = htPianoAss.keys();
    while(n.hasMoreElements())
      {
      String e = (String)n.nextElement();
      dbr.put(e, htPianoAss.get(e));
      }
    /* elisa b 22/06/16: si setta a 'IN_ELABOTAZIONE' il campo flag_stato*/
    dbr.put("flag_stato", IN_ELABORAZIONE);
    
    dbc.writeRecord(dbr);

    String mySelPianoAss = "SELECT *" +
                           " FROM piano_assist" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData);
    LOG.debug("PianoAssistEJB/insert_pianoAss - mySelPianoAss: " + mySelPianoAss);
    ISASRecord dbrRead = dbc.readRecord(mySelPianoAss);

    if (dbrRead != null)
      {
      decodificaOperatore(dbc, dbrRead);
      }

    dbc.close();
    super.close(dbc);
    done=true;
    return dbrRead;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    throw new SQLException("Errore eseguendo la insert_pianoAss() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }

	public ISASRecord update_pianoAss(myLogin mylogin, ISASRecord dbr) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException, CariException {
	  boolean done = false;
	  ISASConnection dbc = null;
	  String strPaTipoOper = null;
	  String strNCartella = null;
	  String strNProgetto = null;
	  String strCodObiettivo = null;
	  String strNIntervento = null;
	  String strPaData = null;
	  String strPaDataChiusura = null;
	  ISASCursor dbcur = null; //gb 09/08/07
	  try {
	    strPaTipoOper = (String) dbr.get("pa_tipo_oper");
	    strNCartella = (String) dbr.get("n_cartella");
	    strNProgetto = (String) dbr.get("n_progetto");
	    strCodObiettivo = (String) dbr.get("cod_obbiettivo");
	    strNIntervento = (String) dbr.get("n_intervento");
	    strPaData = (String)dbr.get("pa_data");
	
		//gb 10/08/07: nel caso di tipi operatore != '01' nelle key 'cod_obbiettivo'
		//	e 'n_intervento' della hashtable htPianoAss ci saranno rispettivamente
		//	due codici speciali: '00000000' e 0, che verranno scritti nel record.
		
		//gb 09/08/07 *******
		    if (dbr.get("pa_data_chiusura") != null)
		       strPaDataChiusura = (String)dbr.get("pa_data_chiusura");
		//gb 09/08/07: fine *******
				
		    dbc=super.logIn(mylogin);
		    dbc.startTransaction();
		
		    String mySelect = "SELECT *" +
		                      " FROM piano_assist" +
		                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
		                      " AND n_cartella = " + strNCartella +
		                      " AND n_progetto = " + strNProgetto +
		                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
		                      " AND n_intervento = " + strNIntervento +
		                      " AND pa_data = " + formatDate(dbc, strPaData);
		    LOG.debug("PianoAssistEJB/update_pianoAss - mySelect: " + mySelect);
		    ISASRecord dbrRead = dbc.readRecord(mySelect);
		
		    //gb 09/08/07 *******
		    //chiudo i piani accessi se ho chiuso il piano assistenziale
		    if((strPaDataChiusura != null) && dbrRead.get("pa_data_chiusura")==null){
				LOG.debug("DEVO CHIUDERE piano_accessi E RIMUOVERE da AGENDA (agendant_interv e agendant_intpre)!");
				//gb 02/10/07 *******
				CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
				// date aper. dei piani accessi.
				String strMsgCheckDtCh = clCcec.checkDtChDaPianoAssGTDtApeDtCh(dbc, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData, strPaTipoOper, strPaDataChiusura);
				if(!strMsgCheckDtCh.equals(""))
				   throw new CariException(strMsgCheckDtCh, -2);
				// Chiusure entita' che stanno sotto il piano assistenziale:
				// Piani accessi
				// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
				clCcec.chiudoDaPianoAssistInGiu(dbc, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData, strPaDataChiusura, strPaTipoOper, (String)dbr.get("pa_operatore"));
				
				/*elisa b 23/06/16: in caso di chiusura si congela il piano*/
				dbr.put("flag_stato", DA_INVIARE);

			}
		    //gb 09/08/07: fine *******
		    
		    // Scrivo il record nella tabella
		    dbc.writeRecord(dbr);
		
		    //gb 09/08/07: in myselect c'e' ancora la query fatta all'inizio del metodo.
		    ISASRecord dbrRet=dbc.readRecord(mySelect);
		
			if (dbrRet != null) {
				decodificaOperatore(dbc, dbrRet);
				String indirizzo = selectAnagra(dbc, strNCartella);
				dbrRet.put("indirizzo", indirizzo);
			}
	
		    dbc.commitTransaction(); //gb 09/08/07
		
		    done=true;
		    return dbrRet;
		}
		//gb 03/10/07 *******
		catch (CariException ce) {
			ce.setISASRecord(null);
			throw ce;
		}
		//gb 03/10/07: fine *******
		catch (DBRecordChangedException e) {
			e.printStackTrace();			
			throw newEjbException("Errore eseguendo la update_pianoAss", e);
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();			
			throw newEjbException("Errore eseguendo la update_pianoAss", e);
		} catch (Exception e1) {
			e1.printStackTrace();			
			throw newEjbException("Errore eseguendo la update_pianoAss", e1);
		} finally {
			if (!done)
				rollback_nothrow(nomeEJB, dbc);
			close_dbcur_nothrow(nomeEJB, dbcur);
			logout_nothrow(nomeEJB, dbc);
		}
  }

private void deleteAll_pianoVer(ISASConnection mydbc, String strPaTipoOper, String strNCartella,
                                String strNProgetto, String strCodObiettivo, String strNIntervento,
                                String strPaData) throws SQLException
  {
  boolean done = false;
  ISASCursor dbcur = null;
  String strVeData = null;

  try
    {
    String mySelect = "SELECT *" +
                      " FROM piano_verifica" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " AND pa_data = " + formatDate(mydbc, strPaData);

    LOG.debug("PianoAssistEJB/deleteAll_pianoVer - mySelect=[" + mySelect + "]");

    dbcur = mydbc.startCursor(mySelect);
    // Metto i record letti in un vector (un vector di ISASRecord).
    Vector vdbr=dbcur.getAllRecord();

    for( int i=0; i<vdbr.size(); i++ )
      {
      ISASRecord dbr = (ISASRecord) vdbr.get(i);
      strVeData = ((java.sql.Date)dbr.get("ve_data")).toString();

      String myS =  "SELECT *" +
                    " FROM piano_verifica" +
                    " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                    " AND n_cartella = " + strNCartella +
                    " AND n_progetto = " + strNProgetto +
                    " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                    " AND n_intervento = " + strNIntervento +
                    " AND pa_data = " + formatDate(mydbc, strPaData) +
                    " AND ve_data = " + formatDate(mydbc, strVeData);
      LOG.debug("deleteAll_pianoVer (" + i + ") : "  + myS);
      ISASRecord rec = mydbc.readRecord(myS);
      // Cancellazione del Record piano_verifica
      mydbc.deleteRecord(rec);
      }
    dbcur.close();
    done = true;
    }
  catch (Exception e)
    {
    LOG.error("PianoAssistEJB/deleteAll_pianoVer - Eccezione= " + e);
    throw new SQLException("Errore eseguendo la delete dei record piano_verifica");
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        }
      catch(Exception e1)
        {
        LOG.error("PianoAssistEJB/deleteAll_pianoVer - Eccezione nella chiusura del dbcur = " + e1);
        }
      }
    }
  }

private void deleteAll_pianoAcc(ISASConnection mydbc, String strPaTipoOper, String strNCartella,
                                String strNProgetto, String strCodObiettivo, String strNIntervento,
                                String strPaData)//, Object objDataInput) 
throws SQLException
  {
  boolean done = false;
  ISASCursor dbcur = null;
  String strPiProg = null;

  try
    {
    String mySelect = "SELECT *" +
                      " FROM piano_accessi" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " AND pa_data = " + formatDate(mydbc, strPaData);

    LOG.debug("PianoAssistEJB/deleteAll_pianoAcc - mySelect=[" + mySelect + "]");

    dbcur = mydbc.startCursor(mySelect);
    // Metto i record letti in un vector (un vector di ISASRecord).
    Vector vdbr = dbcur.getAllRecord();

    for( int i=0; i<vdbr.size(); i++ )
      {
      ISASRecord dbr = (ISASRecord) vdbr.get(i);
      strPiProg = ((Integer)dbr.get("pi_prog")).toString();

      String myS =  "SELECT *" +
                    " FROM piano_accessi" +
                    " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                    " AND n_cartella = " + strNCartella +
                    " AND n_progetto = " + strNProgetto +
                    " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                    " AND n_intervento = " + strNIntervento +
                    " AND pa_data = " + formatDate(mydbc, strPaData) +
                    " AND pi_prog = " + strPiProg;
      LOG.debug("deleteAll_pianoAcc (" + i + ") : "  + myS);
      ISASRecord rec = mydbc.readRecord(myS);
     
      // Cancellazione del Record piano_accessi
      if(rec!=null){
	      mydbc.deleteRecord(rec);	
	      //gb 10/09/07 *******
	   //   if (objDataInput != null)
	   //     {26/06/2012
	        Hashtable htAgenda = new Hashtable();
	        htAgenda.put("pa_tipo_oper", strPaTipoOper);
	        htAgenda.put("n_cartella", strNCartella);
	        htAgenda.put("n_progetto", strNProgetto);
	        htAgenda.put("cod_obbiettivo", strCodObiettivo);
	        htAgenda.put("n_intervento", strNIntervento);
	        htAgenda.put("pa_data", strPaData);
	        htAgenda.put("pi_prog", strPiProg);
		    htAgenda.put("pi_prest_cod", (String)rec.get("pi_prest_cod"));
	        htAgenda.put("data_input", strPaData);//25/06/2012 si rimuove tutto!!!!! (String)objDataInput);
	        htAgenda.put("pi_op_esecutore", (String)rec.get("pi_op_esecutore"));
	        deleteAgenda(mydbc, htAgenda);
	   //     }
      }
    //gb 10/09/07: fine *******
      }


    dbcur.close();
    done = true;
    }
  catch (Exception e)
    {
    LOG.error("PianoAssistEJB/deleteAll_pianoAcc - Eccezione= " + e);
    throw new SQLException("Errore eseguendo la delete dei record piano_accessi");
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        }
      catch(Exception e1)
        {
        LOG.error("PianoAssistEJB/deleteAll_pianoAcc - Eccezione nella chiusura del dbcur = " + e1);
        }
      }
    }
  }

/*gb 10/09/07 *******
public void delete_pianoAss(myLogin mylogin, ISASRecord dbr)
            throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;
  String strPaData = null;

  try
    {
    strPaTipoOper = (String) dbr.get("pa_tipo_oper");
    strNCartella = ((Integer) dbr.get("n_cartella")).toString();
    strNProgetto = ((Integer) dbr.get("n_progetto")).toString();
    strCodObiettivo = (String) dbr.get("cod_obbiettivo");
    strNIntervento = ((Integer) dbr.get("n_intervento")).toString();
    strPaData=((java.sql.Date)dbr.get("pa_data")).toString();

    dbc=super.logIn(mylogin);

    // Inizio la TRANSAZIONE
    dbc.startTransaction();

    deleteAll_pianoVer(dbc, strPaTipoOper, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData);
    deleteAll_pianoAcc(dbc, strPaTipoOper, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData);

    dbc.deleteRecord(dbr);

    // Concludo la TRANSAZIONE
    dbc.commitTransaction();
    dbc.close();
    super.close(dbc);
    done=true;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new SQLException("Errore eseguendo una rollback() - "+  e);
      }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new SQLException("Errore eseguendo una rollback() - "+  e);
      }
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e2)
      {
      throw new SQLException("Errore eseguendo una rollback() - "+  e1);
      }
    throw new SQLException("Errore eseguendo la delete_pianoAss() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }
*gb 10/09/07 *******/

public void delete_pianoAss(myLogin mylogin, Hashtable ht)
            throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;
  String strPaData = null;
  Object objInputData = null;

  try
    {
    LOG.debug("---->delete_pianoAss / Hashtable ininput: " + ht.toString());
    strPaTipoOper = (String) ht.get("pa_tipo_oper");
    strNCartella = (String) ht.get("n_cartella");
    strNProgetto = (String) ht.get("n_progetto");
    strCodObiettivo = (String) ht.get("cod_obbiettivo");
    strNIntervento = (String) ht.get("n_intervento");
    strPaData = (String)ht.get("pa_data");
    if ((ht.get("data_input") != null) && !((String)ht.get("data_input")).equals(""))
	objInputData = (Object) ht.get("data_input");

    dbc=super.logIn(mylogin);

    // Inizio la TRANSAZIONE
    dbc.startTransaction();

    deleteAll_pianoVer(dbc, strPaTipoOper, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData);
    deleteAll_pianoAcc(dbc, strPaTipoOper, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData);//26/05/2012 delete all, objInputData);

    String mySelPianoAss = "SELECT *" +
                           " FROM piano_assist" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData);
    LOG.debug("PianoAssistEJB/delete_pianoAss - mySelPianoAss: " + mySelPianoAss);
    ISASRecord dbrRead = dbc.readRecord(mySelPianoAss);
    if (dbrRead != null)
	dbc.deleteRecord(dbrRead);

    // Concludo la TRANSAZIONE
    dbc.commitTransaction();
    dbc.close();
    super.close(dbc);
    done=true;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new SQLException("Errore eseguendo una rollback() - "+  e);
      }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new SQLException("Errore eseguendo una rollback() - "+  e);
      }
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e2)
      {
      throw new SQLException("Errore eseguendo una rollback() - "+  e1);
      }
    throw new SQLException("Errore eseguendo la delete_pianoAss() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }

public ISASRecord queryKey_pianoVer(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;

  String strPaTipoOper = (String) h.get("pa_tipo_oper");
  String strNCartella = (String) h.get("n_cartella");
  String strNProgetto = (String) h.get("n_progetto");
  String strCodObiettivo = (String) h.get("cod_obbiettivo");
  String strNIntervento = (String) h.get("n_intervento");
  String strPaData = (String) h.get("pa_data");
  String strVeData = (String) h.get("ve_data");

  try
    {
    dbc = super.logIn(mylogin);
    String mySelPianoVer = "SELECT *" +
                           " FROM piano_verifica" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData) +
                           " AND ve_data = " + formatDate(dbc, strVeData);
    LOG.debug("PianoAssistEJB/queryKey_pianoVer - mySelPianoVer : " + mySelPianoVer);
    ISASRecord dbrPianoVer = dbc.readRecord(mySelPianoVer);
    dbc.close();
    super.close(dbc);
    done=true;
    return dbrPianoVer;
    }
  catch(Exception e)
    {
    e.printStackTrace();
    throw new SQLException("Errore eseguendo una queryKey_pianoVer()  ", e);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e1)
        {LOG.error(e1);}
      }
    }
  }

public Vector query_pianoVer(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  ISASCursor dbcur = null;

  String strPaTipoOper = (String) h.get("pa_tipo_oper");
  String strNCartella = (String) h.get("n_cartella");
  String strNProgetto = (String) h.get("n_progetto");
  String strCodObiettivo = (String) h.get("cod_obbiettivo");
  String strNIntervento = (String) h.get("n_intervento");
  String strPaData = (String) h.get("pa_data");

   try
    {
    dbc = super.logIn(mylogin);

    String mySelect = "SELECT *" +
                      " FROM piano_verifica" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " AND pa_data = " + formatDate(dbc, strPaData) +
                      " ORDER BY ve_data DESC";
    LOG.debug("PianoAssistEJB/query_pianoVer - mySelect : " + mySelect);
    dbcur = dbc.startCursor(mySelect);
    Vector vdbr = dbcur.getAllRecord();

    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return vdbr;
    }
  catch(Exception e)
    {
    e.printStackTrace();
    throw new SQLException("Errore eseguendo la query_pianoVer()  ");
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e1)
        {LOG.error(e1);}
      }
    }
  }

public ISASRecord insert_pianoVer(myLogin mylogin, Hashtable htPianoVer)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,CariException// 06/10/06
  {
  boolean done=false;
  ISASConnection dbc=null;
  String strPaTipoOper = (String) htPianoVer.get("pa_tipo_oper");
  String strNCartella = (String) htPianoVer.get("n_cartella");
  String strNProgetto = (String) htPianoVer.get("n_progetto");
  String strCodObiettivo = (String) htPianoVer.get("cod_obbiettivo");
  String strNIntervento = (String) htPianoVer.get("n_intervento");
  String strPaData = (String) htPianoVer.get("pa_data");
  String strVeData = (String) htPianoVer.get("ve_data");
	ISASRecord dbr = null;

  try
    {
    dbc = super.logIn(mylogin);

    dbr = dbc.newRecord("piano_verifica");
    Enumeration n = htPianoVer.keys();
    while(n.hasMoreElements())
      {
      String e = (String)n.nextElement();
      dbr.put(e, htPianoVer.get(e));
      }

		// 06/10/06 m. -----
		String msg = "Impossibile inserire: data preesistente!";
		String mySelPianoVer = "SELECT *" +
                           " FROM piano_verifica" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData) +
                           " AND ve_data = " + formatDate(dbc, strVeData);	
		
		ISASRecord dbrRead = dbc.readRecord(mySelPianoVer);
		if (dbrRead != null)
			throw new CariException(msg, -2);
		// 06/10/06 m. -----

    dbc.writeRecord(dbr);

    LOG.debug("PianoAssistEJB/insert_pianoVer - mySelPianoVer: " + mySelPianoVer);
    dbrRead = dbc.readRecord(mySelPianoVer);

    dbc.close();
    super.close(dbc);
    done=true;
    return dbrRead;
    } // 06/10/06 --
	catch(CariException ce)	{
        ce.setISASRecord(null);
		throw ce;
	} // 06/10/06 --
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    throw new SQLException("Errore eseguendo la insert_pianoVer() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }

public ISASRecord update_pianoVer(myLogin mylogin, ISASRecord dbr)
                  throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	
	String methodName= "update_pianoVer";
  boolean done = false;
  ISASConnection dbc = null;
  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;
  String strPaData = null;
  String strVeData = null;
  try
    {
    strPaTipoOper = (String) dbr.get("pa_tipo_oper");
    strNCartella = ISASUtil.getValoreStringa(dbr, "n_cartella");
    strNProgetto = ISASUtil.getValoreStringa(dbr, "n_progetto");
    strCodObiettivo = (String) dbr.get("cod_obbiettivo");
    strNIntervento = ISASUtil.getValoreStringa(dbr, "n_intervento");
    strPaData = ISASUtil.getDateField(dbr, "pa_data");
    strVeData = (String)dbr.get("ve_data");

    // Ottengo la connessione al database
    dbc=super.logIn(mylogin);

    // Scrivo il record nella tabella
    dbc.writeRecord(dbr);

    String mySelect = "SELECT *" +
                      " FROM piano_verifica" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " AND pa_data = " + formatDate(dbc, strPaData) +
                      " AND ve_data = " + formatDate(dbc, strVeData);
    LOG.debug("PianoAssistEJB/update_pianoVer - mySelect: " + mySelect);
    ISASRecord dbrRead = dbc.readRecord(mySelect);

    dbc.close();
    super.close(dbc);
    done=true;
    return dbrRead;
    } catch(Exception e){
		throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
	}finally {
		logout_nothrow(methodName, dbc);
	}
  }

public void delete_pianoVer(myLogin mylogin, ISASRecord dbr)
            throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;

  try
    {
    dbc=super.logIn(mylogin);

    dbc.deleteRecord(dbr);

    dbc.close();
    super.close(dbc);
    done=true;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    throw new SQLException("Errore eseguendo la delete_pianoVer() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }

public ISASRecord queryKey_pianoAcc(myLogin mylogin, Hashtable h) throws SQLException
{
	boolean done = false;
	ISASConnection dbc = null;

	String strPaTipoOper = (String) h.get("pa_tipo_oper");
	String strNCartella = (String) h.get("n_cartella");
	String strNProgetto = (String) h.get("n_progetto");
	String strCodObiettivo = (String) h.get("cod_obbiettivo");
	String strNIntervento = (String) h.get("n_intervento");
	String strPaData = (String) h.get("pa_data");
	String strPiProg = (String) h.get("pi_prog");

	try
	{
		dbc = super.logIn(mylogin);
		String mySelPianoAcc = "SELECT *" +
				" FROM piano_accessi" +
				" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
				" AND n_cartella = " + strNCartella +
				" AND n_progetto = " + strNProgetto +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
				" AND n_intervento = " + strNIntervento +
				" AND pa_data = " + formatDate(dbc, strPaData) +
				" AND pi_prog = " + strPiProg;
		LOG.info("PianoAssistEJB/queryKey_pianoAcc - mySelPianoAcc : " + mySelPianoAcc);
		ISASRecord dbrPianoAcc = dbc.readRecord(mySelPianoAcc);

		// Decodifca del 'pi_prest_cod' in tabella 'prestaz'.
		decodificaPrestaz(dbc, dbrPianoAcc);
		aggiungiFlagPianificata(dbc, dbrPianoAcc);

		debugMessage("decodifica operatore esecut "+dbrPianoAcc.get("pi_op_esecutore"));
		if (dbrPianoAcc.get("pi_op_esecutore")!=null && 
				!((String)dbrPianoAcc.get("pi_op_esecutore")).equals("")){
			dbrPianoAcc.put("pi_op_esec_desc",getOperatore(dbc,(String)dbrPianoAcc.get("pi_op_esecutore")));		   
		}
		//gb 05/09/07    if (dbrPianoAcc != null)
		//gb 05/09/07	dbrPianoAcc.put("data_input", "1900-01-01");

		dbc.close();
		super.close(dbc);
		done=true;
		return dbrPianoAcc;
	} catch(Exception e){
		throw newEjbException("Errore eseguendo "+ e.getStackTrace()[0].getMethodName() + ": " + e.getMessage(), e);
	}finally {
		logout_nothrow(Thread.currentThread().getStackTrace()[0].getMethodName(), dbc);
	}
}

private void decodificaQueryPianoAcc(ISASConnection mydbc, Vector vdbr) throws Exception
  {
  for (int i=0; i<vdbr.size(); i++)
    {
    ISASRecord dbr = (ISASRecord) vdbr.get(i);
    decodificaFrequenza(mydbc, dbr);
    decodificaPrestaz(mydbc, dbr);
    debugMessage("decodifica operatore esecut "+dbr.get("pi_op_esecutore"));
    if (dbr.get("pi_op_esecutore")!=null && 
			 !((String)dbr.get("pi_op_esecutore")).equals("")){
			dbr.put("pi_op_esec_desc",getOperatore(mydbc,(String)dbr.get("pi_op_esecutore")));		   
		}
    }
  }

public Vector query_pianoAcc(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  ISASCursor dbcur = null;

  String strPaTipoOper = (String) h.get("pa_tipo_oper");
  String strNCartella = (String) h.get("n_cartella");
  String strNProgetto = (String) h.get("n_progetto");
  String strCodObiettivo = (String) h.get("cod_obbiettivo");
  String strNIntervento = (String) h.get("n_intervento");
  String strPaData = (String) h.get("pa_data");

//gb 10/08/07: nel caso di tipi operatore != '01' nelle key 'cod_obbiettivo'
//	e 'n_intervento' della hashtable htPianoAss ci saranno rispettivamente
//	due codici speciali: '00000000' e 0, che verranno scritti nel record.

   try
    {
    dbc = super.logIn(mylogin);

    String mySelect = "SELECT *" +
                      " FROM piano_accessi" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento ;
    if(strPaData == null || strPaData.isEmpty()){
    	strPaData = (String) h.get("data_agenda");
    	//caso dell'agenda in cui voglio le prestazioni previste dal piano ad una certa data
    	 mySelect += " AND pi_data_inizio <= " + formatDate(dbc, strPaData) + 
    			 " AND (pi_data_fine is null OR " + formatDate(dbc, strPaData) + " <= pi_data_fine ) "; 
    }else{
    	//caso normale in cui passo la data del piano assistenziale
       mySelect += " AND pa_data = " + formatDate(dbc, strPaData) ; 
    }
                      
    mySelect += " ORDER BY pi_prog";
                      
    LOG.debug("PianoAssistEJB/query_pianoAcc - mySelect : " + mySelect);
    dbcur = dbc.startCursor(mySelect);
    Vector vdbr = dbcur.getAllRecord();

    // Decodifica dei 'pi_freq' e 'pi_prest_cod' in tutti gli ISASRecord del Vector
    decodificaQueryPianoAcc(dbc, vdbr);
	aggiungiFlagPianificata(dbc, vdbr);

    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return vdbr;
    }
  catch(Exception e)
    {
    e.printStackTrace();
    throw new SQLException("Errore eseguendo la query_pianoAcc()  ");
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e1)
        {LOG.error(e1);}
      }
    }
  }

private int getProgressivoPianoAcc(ISASConnection dbc, String strPaTipoOper, String strNCartella,
                                   String strNProgetto, String strCodObiettivo, String strNIntervento,
                                   String strPaData) throws Exception
  {
  int intProgressivo = 1;

  String mySelMax = "SELECT MAX(pi_prog) max_progressivo" +
                    " FROM piano_accessi" +
                    " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                    " AND n_cartella = " + strNCartella +
                    " AND n_progetto = " + strNProgetto +
                    " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                    " AND n_intervento = " + strNIntervento +
                    " AND pa_data = " + formatDate(dbc, strPaData);
    LOG.debug("PianoAssistEJB/getProgressivoPianoAcc - mySelMax: " + mySelMax);
    ISASRecord dbrMaxProgr = dbc.readRecord(mySelMax);
    if (dbrMaxProgr != null)
      {
      Integer iProgressivo = (Integer) dbrMaxProgr.get("max_progressivo");
      if (iProgressivo != null)
        intProgressivo = iProgressivo.intValue() + 1;
      }
    return intProgressivo;
  }

public ISASRecord insert_pianoAcc(myLogin mylogin, Hashtable htPianoAcc)
                  throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
	
	String methodName = "insert_pianoAcc";
  boolean done=false;
  ISASConnection dbc=null;
  int intProgressivo = 1;

  String strPaTipoOper = (String) htPianoAcc.get("pa_tipo_oper");
  String strNCartella = (String) htPianoAcc.get("n_cartella");
  String strNProgetto = (String) htPianoAcc.get("n_progetto");
  String strCodObiettivo = (String) htPianoAcc.get("cod_obbiettivo");
  String strNIntervento = (String) htPianoAcc.get("n_intervento");
  String strPaData = (String) htPianoAcc.get("pa_data");
  String strPiProg = null;

//gb 10/08/07: nel caso di tipi operatore != '01' nelle key 'cod_obbiettivo'
//	e 'n_intervento' della hashtable htPianoAss ci saranno rispettivamente
//	due codici speciali: '00000000' e 0, che verranno scritti nel record.

  try
    {
    dbc = super.logIn(mylogin);

    intProgressivo = getProgressivoPianoAcc(dbc, strPaTipoOper, strNCartella, strNProgetto,
                                            strCodObiettivo, strNIntervento, strPaData);
    ISASRecord dbr = dbc.newRecord("piano_accessi");
    Enumeration n = htPianoAcc.keys();
    while(n.hasMoreElements())
      {
      String e = (String)n.nextElement();
      dbr.put(e, htPianoAcc.get(e));
      }
    dbr.put("pi_prog", new Integer(intProgressivo));
    /* elisa b 22/06/16: si setta a 'IN_ELABOTAZIONE' il campo flag_stato*/
    dbr.put("flag_stato", IN_ELABORAZIONE);
    dbc.writeRecord(dbr);

    strPiProg = (new Integer(intProgressivo)).toString();
    String mySelPianoAcc = "SELECT *" +
                           " FROM piano_accessi" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData) +
                           " AND pi_prog = " + strPiProg;
    LOG.debug("PianoAssistEJB/insert_pianoAcc - mySelPianoAcc: " + mySelPianoAcc);
    ISASRecord dbrRead = dbc.readRecord(mySelPianoAcc);

    if ((dbrRead != null) && (dbrRead.get("pi_op_esecutore") != null) &&
	!((String)dbrRead.get("pi_op_esecutore")).equals(""))
	dbrRead.put("pi_op_esec_desc",getOperatore(dbc,(String)dbrRead.get("pi_op_esecutore")));

    dbc.close();
    super.close(dbc);
    done=true;
    return dbrRead;
    } catch(Exception e){
		throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
	}finally {
		logout_nothrow(methodName, dbc);
	}
  }

public ISASRecord update_pianoAcc(myLogin mylogin, ISASRecord dbr)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;
  String strPaData = null;
  String strPiProg = null;
//gb 02/10/07 *******
  String strDtFine = null;
  String strDtFinePrec = null;
  String strPrestCod = null;
  String strCodOperEsec = null;
//gb 02/10/07: fine *******
  try
    {
    strPaTipoOper = (String) dbr.get("pa_tipo_oper");
    strNCartella = ISASUtil.getValoreStringa(dbr, "n_cartella");//(String) dbr.get("n_cartella");
    strNProgetto = ISASUtil.getValoreStringa(dbr, "n_progetto"); //(String) dbr.get("n_progetto");
    strCodObiettivo = (String) dbr.get("cod_obbiettivo");
    strNIntervento = ISASUtil.getValoreStringa(dbr, "n_intervento");//(String) dbr.get("n_intervento");
    strPaData =  ISASUtil.getValoreStringa(dbr, "pa_data");//(String)dbr.get("pa_data");
    strPiProg = (String)dbr.get("pi_prog");
//gb 02/10/07 *******
    strDtFine = ISASUtil.getValoreStringa(dbr, "pi_data_fine"); //(String)dbr.get("pi_data_fine");
    strPrestCod = (String)dbr.get("pi_prest_cod");
    strCodOperEsec = (String)dbr.get("pi_op_esecutore");
//gb 02/10/07: fine *******

//gb 10/08/07: nel caso di tipi operatore != '01' nelle key 'cod_obbiettivo'
//	e 'n_intervento' della hashtable htPianoAss ci saranno rispettivamente
//	due codici speciali: '00000000' e 0, che verranno scritti nel record.

    // Ottengo la connessione al database
    dbc=super.logIn(mylogin);

    dbc.startTransaction(); //gb 02/10/07

//gb 02/10/07 *******
    String mySelect = "SELECT *" +
                      " FROM piano_accessi" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " AND pa_data = " + formatDate(dbc, strPaData) +
                      " AND pi_prog = " + strPiProg;
    LOG.debug("PianoAssistEJB/update_pianoAcc (1) - mySelect: " + mySelect);
    ISASRecord dbrPreRead = dbc.readRecord(mySelect);
    if (dbrPreRead.get("pi_data_fine") != null)
       strDtFinePrec =((java.sql.Date)dbrPreRead.get("pi_data_fine")).toString();
    if (dbrPreRead != null)
	{
	if ((strDtFine != null) && !strDtFine.equals(""))
	   if ((strDtFinePrec == null) || !strDtFinePrec.equals(strDtFine))
		{
// Chiusure entit� che stanno sotto il piano accessi:
//   in questo caso solo rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
		CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
		clCcec.chiudoDaPianoAccessiInGiu(dbc, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strDtFine, strPaTipoOper, strPrestCod, strCodOperEsec);
		}
	}
//gb 02/10/07: fine *******
    // Scrivo il record nella tabella
    dbc.writeRecord(dbr);

    mySelect = "SELECT *" +
               " FROM piano_accessi" +
               " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
               " AND n_cartella = " + strNCartella +
               " AND n_progetto = " + strNProgetto +
               " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
               " AND n_intervento = " + strNIntervento +
               " AND pa_data = " + formatDate(dbc, strPaData) +
               " AND pi_prog = " + strPiProg;
    LOG.debug("PianoAssistEJB/update_pianoAcc (2) - mySelect: " + mySelect);
    ISASRecord dbrRead = dbc.readRecord(mySelect);

    if ((dbrRead != null) && (dbrRead.get("pi_op_esecutore") != null) &&
	!((String)dbrRead.get("pi_op_esecutore")).equals(""))
	dbrRead.put("pi_op_esec_desc",getOperatore(dbc,(String)dbrRead.get("pi_op_esecutore")));

    dbc.commitTransaction(); //gb 02/10/07

    dbc.close();
    super.close(dbc);
    done=true;
    return dbrRead;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    try{
	dbc.rollbackTransaction();
    }catch(Exception e1){
	throw new SQLException("Errore eseguendo una rollback() - "+  e);
    }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    try{
	dbc.rollbackTransaction();
    }catch(Exception e1){
	throw new SQLException("Errore eseguendo una rollback() - "+  e);
    }
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    try{
	dbc.rollbackTransaction();
    }catch(Exception e2){
	throw new SQLException("Errore eseguendo una rollback() - "+  e1);
    }
    throw new SQLException("Errore eseguendo la update_pianoAcc() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }

//gb 05/09/07 public void delete_pianoAcc(myLogin mylogin, ISASRecord dbr)
 public void delete_pianoAcc(myLogin mylogin, Hashtable h)
            throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;

  LOG.debug("delete_pianoAcc / Hashtable in Input: " + h.toString()); //gb 05/09/07

//gb 05/09/07 *******
  String strPaTipoOper = (String) h.get("pa_tipo_oper");
  String strNCartella = (String) h.get("n_cartella");
  String strNProgetto = (String) h.get("n_progetto");
  String strCodObiettivo = (String) h.get("cod_obbiettivo");
  String strNIntervento = (String) h.get("n_intervento");
  String strPaData = (String) h.get("pa_data");
  String strPiProg = (String) h.get("pi_prog");
//gb 05/09/07: fine *******

  try
    {
    dbc=super.logIn(mylogin);

    dbc.startTransaction(); //gb 05/09/07
//gb 05/09/07    LOG.debug("delete_pianoAcc / Hashtable dell'ISASRecord: " + dbr.getHashtable().toString()); //gb 05/09/07

//gb 05/09/07 *******
    String mySelPianoAcc = "SELECT *" +
                           " FROM piano_accessi" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData) +
                           " AND pi_prog = " + strPiProg;
    LOG.debug("PianoAssistEJB/delete_pianoAcc - mySelPianoAcc : " + mySelPianoAcc);
    ISASRecord dbrPianoAcc = dbc.readRecord(mySelPianoAcc);
//gb 05/09/07: fine *******

//gb 05/09/07    dbc.deleteRecord(dbr);

//gb 05/09/07 *******
    if (dbrPianoAcc != null)
	{
    	dbc.deleteRecord(dbrPianoAcc);
	deleteAgenda(dbc, h);
	}
    dbc.commitTransaction();
//gb 05/09/07: fine *******
    dbc.close();
    super.close(dbc);
    done=true;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    try{
	dbc.rollbackTransaction();
    }catch(Exception e1){
	throw new SQLException("Errore eseguendo una rollback() - "+  e);
    }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    try{
	dbc.rollbackTransaction();
    }catch(Exception e1){
	throw new SQLException("Errore eseguendo una rollback() - "+  e);
    }
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    try{
	dbc.rollbackTransaction();
    }catch(Exception e2){
	throw new SQLException("Errore eseguendo una rollback() - "+  e1);
    }
    throw new SQLException("Errore eseguendo la delete_pianoAcc() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }

//gb 05/09/07 *******
private void deleteAgenda(ISASConnection dbc, Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASCursor dbcur=null;
	metodo="deleteAgenda";
	debugMessage("deleteAgenda h="+h.toString());
  String strPaTipoOper = (String) h.get("pa_tipo_oper");
  String strNCartella = (String) h.get("n_cartella");
  String strNProgetto = (String) h.get("n_progetto");
  String strCodObiettivo = (String) h.get("cod_obbiettivo");
  String strNIntervento = (String) h.get("n_intervento");
  String strPaData = (String) h.get("pa_data");
  String strPiProg = (String) h.get("pi_prog");
	try{
		String myselect="SELECT * FROM agendant_sett_tipo WHERE "+
				"n_cartella="+strNCartella+" and "+
				"n_contatto="+strNProgetto+" and "+
				"cod_obbiettivo= '"+strCodObiettivo+"' and "+
				"n_intervento="+strNIntervento+" and "+
                        	"as_tipo_oper='"+strPaTipoOper+"' and "+
				"as_data="+formatDate(dbc,strPaData)+" AND "+
                        	"as_prog="+strPiProg;
		LOG.info("deleteAgenda / myselect: " + myselect);
	    	dbcur=dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
		for(int i=0; i<vdbr.size(); i++) {
			ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
			if (dbrec != null){
			   String mysel="SELECT * FROM agendant_sett_tipo WHERE "+
					"n_cartella="+strNCartella+" and "+
					"n_contatto="+strNProgetto+" and "+
					"cod_obbiettivo= '"+strCodObiettivo+"' and "+
					"n_intervento="+strNIntervento+" and "+
					"as_data="+formatDate(dbc,strPaData)+" AND "+
                        		"as_prog="+strPiProg+" and "+
                        		"as_tipo_oper='"+strPaTipoOper+"' and "+
                        		"as_op_referente='"+(String)dbrec.get("as_op_referente")+"' AND "+
                        		"as_giorno_sett="+(Integer)dbrec.get("as_giorno_sett");
			LOG.info("deleteAgenda / mysel: " + mysel);
                        ISASRecord dbag=dbc.readRecord(mysel);
                        dbc.deleteRecord(dbag);                        
                        LOG.debug("cancellazione " + mysel);//elisa b 15/05/12
             }
		}				
		Object dataInput=h.get("data_input");
		if(dataInput!=null && !((String)dataInput).equals("")){
			h.put("pi_data_fine",(String)dataInput);	
			cap.rimuovoAgendaCaricata(dbc,h,nomeEJB+"/"+metodo);
		}
		if (dbcur != null)dbcur.close();
		done=true;
	}catch(Exception e1){
			throw new SQLException("Errore eseguendo una deleteAgenda - "+  e1);
	}finally{
		if(!done){
			try{
				if (dbcur != null)
								   dbcur.close();
			}catch(Exception e2){LOG.error(e2);}
		}
	}

}
//gb 05/09/07: fine *******

//gb 30/08/07 *******
private Vector reload_all_pianoVer(ISASConnection dbc, String strPaTipoOper, String strNCartella,
			String strNProgetto, String strCodObiettivo, String strNIntervento,
			String strPaData) throws Exception
  {
  String mySelPianoVer = "SELECT *" +
                         " FROM piano_verifica" +
                         " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                         " AND n_cartella = " + strNCartella +
                         " AND n_progetto = " + strNProgetto +
                         " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                         " AND n_intervento = " + strNIntervento +
                         " AND pa_data = " + formatDate(dbc, strPaData);
  LOG.debug("PianoAssistEJB/reload_all_pianoVer - mySelPianoVer: " + mySelPianoVer);
  ISASCursor dbcurPianoVer = dbc.startCursor(mySelPianoVer);
  Vector vdbrPianoVer = dbcurPianoVer.getAllRecord();

  if (dbcurPianoVer != null)
    dbcurPianoVer.close();

  return vdbrPianoVer;
  }
//gb 30/08/07: fine *******

private Vector insert_all_pianoVer(ISASConnection dbc, Hashtable htPianoVer) throws Exception
  {
  String strPaTipoOper = (String) htPianoVer.get("pa_tipo_oper");
  String strNCartella = (String) htPianoVer.get("n_cartella");
  String strNProgetto = (String) htPianoVer.get("n_progetto");
  String strCodObiettivo = (String) htPianoVer.get("cod_obbiettivo");
  String strNIntervento = (String) htPianoVer.get("n_intervento");
  String strPaData = (String) htPianoVer.get("pa_data");
//  String strVeData = (String) htPianoVer.get("ve_data");

  ISASRecord dbr = dbc.newRecord("piano_verifica");
  Enumeration n = htPianoVer.keys();
  while(n.hasMoreElements())
    {
    String e = (String)n.nextElement();
    dbr.put(e, htPianoVer.get(e));
    }
  dbc.writeRecord(dbr);

  String mySelPianoVer = "SELECT *" +
                         " FROM piano_verifica" +
                         " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                         " AND n_cartella = " + strNCartella +
                         " AND n_progetto = " + strNProgetto +
                         " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                         " AND n_intervento = " + strNIntervento +
                         " AND pa_data = " + formatDate(dbc, strPaData);
  LOG.debug("PianoAssistEJB/insert_all_pianoVer - mySelPianoVer: " + mySelPianoVer);
  ISASCursor dbcurPianoVer = dbc.startCursor(mySelPianoVer);
  Vector vdbrPianoVer = dbcurPianoVer.getAllRecord();

  if (dbcurPianoVer != null)
    dbcurPianoVer.close();

  return vdbrPianoVer;
  }

//gb 30/08/07 *******
private Vector reload_all_pianoAcc(ISASConnection dbc, String strPaTipoOper, String strNCartella,
			String strNProgetto, String strCodObiettivo, String strNIntervento,
			String strPaData) throws Exception
  {
  String mySelPianoVer = "SELECT *" +
                         " FROM piano_accessi" +
                         " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                         " AND n_cartella = " + strNCartella +
                         " AND n_progetto = " + strNProgetto +
                         " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                         " AND n_intervento = " + strNIntervento +
                         " AND pa_data = " + formatDate(dbc, strPaData) +
                         " ORDER BY pi_prog";
  LOG.debug("PianoAssistEJB/reload_all_pianoAcc - mySelPianoVer: " + mySelPianoVer);
  ISASCursor dbcurPianoAcc = dbc.startCursor(mySelPianoVer);
  Vector vdbrPianoAcc = dbcurPianoAcc.getAllRecord();

  if (dbcurPianoAcc != null)
    dbcurPianoAcc.close();

  return vdbrPianoAcc;
  }
//gb 30/08/07: fine *******

private Vector insert_all_pianoAcc(ISASConnection dbc, Hashtable htPianoAcc) throws Exception
  {
  String strPaTipoOper = (String) htPianoAcc.get("pa_tipo_oper");
  String strNCartella = (String) htPianoAcc.get("n_cartella");
  String strNProgetto = (String) htPianoAcc.get("n_progetto");
  String strCodObiettivo = (String) htPianoAcc.get("cod_obbiettivo");
  String strNIntervento = (String) htPianoAcc.get("n_intervento");
  String strPaData = (String) htPianoAcc.get("pa_data");
  String strPiProg = null;

  int intProgressivo = getProgressivoPianoAcc(dbc, strPaTipoOper, strNCartella, strNProgetto,
                                          strCodObiettivo, strNIntervento, strPaData);
  ISASRecord dbr = dbc.newRecord("piano_accessi");
  Enumeration n = htPianoAcc.keys();
  while(n.hasMoreElements())
    {
    String e = (String)n.nextElement();
    dbr.put(e, htPianoAcc.get(e));
    }
  dbr.put("pi_prog", new Integer(intProgressivo));
  
  /* elisa b 22/06/16: si setta a 'IN_ELABOTAZIONE' il campo flag_stato*/
  dbr.put("flag_stato", IN_ELABORAZIONE);
  
  dbc.writeRecord(dbr);

  // strPiProg = (new Integer(intProgressivo)).toString();
  String mySelPianoAcc = "SELECT *" +
                         " FROM piano_accessi" +
                         " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                         " AND n_cartella = " + strNCartella +
                         " AND n_progetto = " + strNProgetto +
                         " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                         " AND n_intervento = " + strNIntervento +
                         " AND pa_data = " + formatDate(dbc, strPaData) +
                         " ORDER BY pi_prog";
  LOG.debug("PianoAssistEJB/insert_all_pianoAcc - mySelPianoAcc: " + mySelPianoAcc);

  ISASCursor dbcurPianoAcc = dbc.startCursor(mySelPianoAcc);
  Vector vdbrPianoAcc = dbcurPianoAcc.getAllRecord();
  // Decodifica dei 'pi_freq' e 'pi_prest_cod' in tutti gli ISASRecord del Vector
  decodificaQueryPianoAcc(dbc, vdbrPianoAcc);

  if (dbcurPianoAcc != null)
    dbcurPianoAcc.close();

  return vdbrPianoAcc;
  }

public ISASRecord insert_all(myLogin mylogin, Hashtable htPianoAss, Vector vctr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,CariException// 06/10/06
  {
  boolean done = false;
  ISASConnection dbc = null;
  Vector vdbrPianoVer = new Vector();
  Vector vdbrPianoAcc = new Vector();
  String strPaTipoOper = (String) htPianoAss.get("pa_tipo_oper");
  String strNCartella = (String) htPianoAss.get("n_cartella");
  String strNProgetto = (String) htPianoAss.get("n_progetto");
  String strCodObiettivo = (String) htPianoAss.get("cod_obbiettivo");
  String strNIntervento = (String) htPianoAss.get("n_intervento");
  String strPaData = (String) htPianoAss.get("pa_data");
//gb 09/08/07 *******
  String strPaDataChiusura = null;
  if (htPianoAss.get("pa_data_chiusura") != null)
     strPaDataChiusura = (String) htPianoAss.get("pa_data_chiusura");
//gb 09/08/07: fine *******

  ISASRecord dbr = null;
  ISASCursor dbcur = null; //gb 09/08/07

  try
    {
    dbc = super.logIn(mylogin);

    // Inizio la TRANSAZIONE
    dbc.startTransaction();

//gb 10/08/07: nel caso di tipi operatore != '01' nelle key 'cod_obbiettivo'
//	e 'n_intervento' della hashtable htPianoAss ci saranno rispettivamente
//	due codici speciali: '00000000' e 0, che verranno scritti nel record.

    dbr = dbc.newRecord("piano_assist");
    Enumeration n = htPianoAss.keys();
    while(n.hasMoreElements())
      {
      String e = (String)n.nextElement();
      dbr.put(e, htPianoAss.get(e));
      }

		// 06/10/06 m. -----
		String msg = "Impossibile inserire: data preesistente!";
		String mySelPianoAss = "SELECT *" +
                           " FROM piano_assist" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData);
		
    LOG.debug("PianoAssistEJB/insert_all - mySelPianoAss: " + mySelPianoAss);
		ISASRecord dbrRead = dbc.readRecord(mySelPianoAss);
		if (dbrRead != null)
			throw new CariException(msg, -2);
		// 06/10/06 m. -----

	/* elisa b 22/06/16: si setta a 'IN_ELABOTAZIONE' il campo flag_stato*/
	dbr.put("flag_stato", IN_ELABORAZIONE);
	    
    dbc.writeRecord(dbr);

    Hashtable htPianoVer = (Hashtable) vctr.get(0);
    if (htPianoVer != null)
       {
       vdbrPianoVer = insert_all_pianoVer(dbc, htPianoVer);
       }
    else
       {
       vdbrPianoVer = reload_all_pianoVer(dbc, strPaTipoOper, strNCartella,
			strNProgetto, strCodObiettivo, strNIntervento,
			strPaData);
       }
    Hashtable htPianoAcc = (Hashtable) vctr.get(1);
    if (htPianoAcc != null)
       {
       vdbrPianoAcc = insert_all_pianoAcc(dbc, htPianoAcc);
       }
    else
       {
       vdbrPianoAcc = reload_all_pianoAcc(dbc, strPaTipoOper, strNCartella,
			strNProgetto, strCodObiettivo, strNIntervento,
			strPaData);
       }

//gb 08/09/07 *******
    //chiudo i piani accessi se ho chiuso il piano assistenziale
    if(strPaDataChiusura!=null)
	{
//gb 02/10/07 *******
	CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
// date aper. dei piani accessi.
	String strMsgCheckDtCh = clCcec.checkDtChDaPianoAssGTDtApeDtCh(dbc, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData, strPaTipoOper, strPaDataChiusura);
	if(!strMsgCheckDtCh.equals(""))
	   throw new CariException(strMsgCheckDtCh, -2);
// Chiusure entit� che stanno sotto il piano assistenziale:
// Piani accessi
// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
	clCcec.chiudoDaPianoAssistInGiu(dbc, strNCartella, strNProgetto, strCodObiettivo, strNIntervento, strPaData, strPaDataChiusura, strPaTipoOper, (String)dbrRead.get("pa_operatore"));
//gb 02/10/07: fine *******
/*gb 02/10/07 *******
	String sel = "SELECT *" +
		     " FROM piano_accessi" +
		     " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
		     " AND n_cartella = " + strNCartella +
                     " AND n_progetto = " + strNProgetto +
                     " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                     " AND n_intervento = " + strNIntervento +
		     " AND pa_data = " + formatDate(dbc,strPaData)+ 
		     //09/01/2007 chiudo solo quelli aperti !!!	o con data chiusura superiore a data nuova             
		     " AND (pi_data_fine is null or pi_data_fine > " + formatDate(dbc, strPaDataChiusura)+")";
	debugMessage("Select pianointerv:"+sel);
	dbcur = dbc.startCursor(sel);
	while(dbcur.next()){
	   ISASRecord dbr2 = dbcur.getRecord();
	   String mysel = "SELECT *" +
			  " FROM piano_accessi" +
		     	  " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
		     	  " AND n_cartella = " + strNCartella +
                     	  " AND n_progetto = " + strNProgetto +
                     	  " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                     	  " AND n_intervento = " + strNIntervento +
                          " AND pa_data = " + formatDate(dbc,""+dbr2.get("pa_data"))+
                          " AND pi_prog = " + dbr2.get("pi_prog");
           ISASRecord dbrW = dbc.readRecord(mysel);
	   dbrW.put("pi_data_fine", ""+java.sql.Date.valueOf(strPaDataChiusura));
	   dbc.writeRecord(dbrW);
	   rimuovoAgendaCaricata(dbc,dbrW.getHashtable());
	   }
*gb 02/10/07: fine *******/
	}
//gb 08/09/07: fine *******

    dbrRead = dbc.readRecord(mySelPianoAss);

    if (dbrRead != null)
      {
      decodificaOperatore(dbc, dbrRead);
      String indirizzo = selectAnagra(dbc, strNCartella);		
      dbrRead.put("indirizzo", indirizzo);
      }

    dbrRead.put("gridPianoVer", vdbrPianoVer);
    dbrRead.put("gridPianoAcc", vdbrPianoAcc);

    // Concludo la TRANSAZIONE
    dbc.commitTransaction();

    if (dbcur != null)
      dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return dbrRead;
    } // 06/10/06 --
      catch(CariException ce)	{
      ce.setISASRecord(null);
      try{
	dbc.rollbackTransaction();
      }catch(Exception e1){
	throw new CariException("Errore eseguendo una rollback() - "+  ce);
      }
      throw ce;
    }catch(Exception e){
		throw newEjbException("Errore eseguendo insert_all: " + e.getMessage(), e);
	}finally {
		close_dbcur_nothrow("insert_all", dbcur);
		logout_nothrow("insert_all", dbc);
	}
	}

/*gb 08/08/07: fine *******
public Boolean controllo_piani(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;

  ISASRecord dbr = null;
  ISASCursor dbcur = null;
  ISASConnection dbc = null;
  boolean boolExistPianiAperti = false;

  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;

  try
    {
    strPaTipoOper = (String) h.get("pa_tipo_oper");
    strNCartella = (String) h.get("n_cartella");
    strNProgetto = (String) h.get("n_progetto");
    strCodObiettivo = (String) h.get("cod_obbiettivo");
    strNIntervento = (String) h.get("n_intervento");
    }
  catch (Exception e)
    {
    e.printStackTrace();
    throw new SQLException("PianoAssistEJB/controllo_piani - Errore: manca la chiave primaria");
    }

  try
    {
    dbc = super.logIn(mylogin);

    String sel = "SELECT *" +
                 " FROM piano_accessi" +
                 " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                 " AND n_cartella = " + strNCartella +
                 " AND n_progetto = " + strNProgetto +
                 " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                 " AND n_intervento = " + strNIntervento +
                 " AND pi_data_fine IS NULL";

    LOG.debug("PianoAssistEJB/controllo_piani - sel: " + sel);
    dbcur = dbc.startCursor(sel);
    if(dbcur.next())
      boolExistPianiAperti = true;

    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return new Boolean(boolExistPianiAperti);
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    throw new SQLException("Errore eseguendo la controllo_piani() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }
*gb 08/08/07: fine *******/

//gb 08/08/07: ora si controlla se esistono piani assist. aperti se la data chiusura � null
public Boolean controllo_piani(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done = false;

  ISASCursor dbcur = null;
  ISASConnection dbc = null;
  boolean boolExistPianiAperti = false;

  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;

  try
    {
    strPaTipoOper = (String) h.get("pa_tipo_oper");
    strNCartella = (String) h.get("n_cartella");
    strNProgetto = (String) h.get("n_progetto");
    strCodObiettivo = (String) h.get("cod_obbiettivo");
    strNIntervento = (String) h.get("n_intervento");
    }
  catch (Exception e)
    {
    e.printStackTrace();
    throw new SQLException("PianoAssistEJB/controllo_piani - Errore: manca la chiave primaria");
    }

  try
    {
    dbc = super.logIn(mylogin);

    String sel = "SELECT *" +
                 " FROM piano_assist" +
                 " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                 " AND n_cartella = " + strNCartella +
                 " AND n_progetto = " + strNProgetto +
                 " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                 " AND n_intervento = " + strNIntervento +
                 " AND pa_data_chiusura IS NULL";

    LOG.debug("PianoAssistEJB/controllo_piani - sel: " + sel);
    dbcur = dbc.startCursor(sel);
    if(dbcur.next())
      boolExistPianiAperti = true;

    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return new Boolean(boolExistPianiAperti);
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    throw new SQLException("Errore eseguendo la controllo_piani() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }
//gb 08/08/07: fine *******

/*gb 08/08/07 *******
public Integer chiudi_piani(myLogin mylogin, Hashtable h)
               throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done=false;
  ISASConnection dbc=null;
  ISASCursor dbcur = null;

  int intPianiAcessiAperti = 0;

  String strPaTipoOper = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;
  String strPaData = null;
  String strPiProg = (String) h.get("pi_prog");
	//String dataSk=null;

  try
    {
    strPaTipoOper = (String) h.get("pa_tipo_oper");
    strNCartella = (String) h.get("n_cartella");
    strNProgetto = (String) h.get("n_progetto");
    strCodObiettivo = (String) h.get("cod_obbiettivo");
    strNIntervento = (String) h.get("n_intervento");
		//dataSk=(String)h.get("skpa_data");
    }
  catch (Exception e)
    {
    e.printStackTrace();
    throw new SQLException("PianoAssistEJB/chiudi_piani - Errore: manca la chiave primaria");
    }

  try
    {
    ServerUtility su=new ServerUtility();
    dbc=super.logIn(mylogin);

    dbc.startTransaction();

    String dataCh=(String)h.get("data_chiusura");

    String mySelect = "SELECT *" +
                      " FROM piano_accessi" +
                      " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                      " AND n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " AND pi_data_fine IS NULL ";

    LOG.debug("PianoAssistEJB/chiudi_piani - mySelect: " + mySelect);
    dbcur = dbc.startCursor(mySelect);
    while(dbcur.next())
      {
      ISASRecord dbr = dbcur.getRecord();
      strPaData = ((java.sql.Date) dbr.get("pa_data")).toString();
      strPiProg = ((Integer) dbr.get("pi_prog")).toString();

      String mySelPianoAcc = "SELECT *" +
                           " FROM piano_accessi" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData) +
                           " AND pi_prog = " + strPiProg +
                           " AND pi_data_fine IS NULL ";

      LOG.debug("PianoAssistEJB/chiudi_piani - mySelPianoAcc: " + mySelPianoAcc);
      ISASRecord dbr2 = dbc.readRecord(mySelPianoAcc);
                  //LOG.info("DataChiusura:"+""+java.sql.Date.valueOf(dataCh));
//      LOG.debug("\n-->PianoAssistEJB/chiudi_piani - PRIMA di dbr2.put(pi_data_fine)");
      dbr2.put("pi_data_fine", ""+java.sql.Date.valueOf(dataCh));
//      LOG.debug("\n-->PianoAssistEJB/chiudi_piani - PRIMA di dbc.writeRecord(dbr2)");
      dbc.writeRecord(dbr2);
//      LOG.debug("\n-->PianoAssistEJB/chiudi_piani - DOPO di dbc.writeRecord(dbr2)");
      intPianiAcessiAperti++;
      }

    dbc.commitTransaction();

    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return (new Integer(intPianiAcessiAperti));
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new SQLException("Errore eseguendo la rollback() - "+  e);
      }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new SQLException("Errore eseguendo la rollback() - "+  e);
      }
    throw e;
    }
  catch(Exception e1)
    {
    LOG.error(e1);
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e2)
      {
      throw new SQLException("Errore eseguendo la rollback() - "+  e1);
      }
    throw new SQLException("Errore eseguendo la chiudi_piano() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        if (dbcur != null)
          dbcur.close();
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {LOG.error(e2);}
      }
    }
  }
*gb 08/08/07 *******/

//gb 08/08/07 *******
public Integer chiudi_piani(myLogin mylogin, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
  boolean done=false;
  ISASConnection dbc=null;
  ISASCursor dbcur = null;
	metodo="chiudi_piani";

	try{
		ServerUtility su=new ServerUtility();
		dbc=super.logIn(mylogin);
		
		dbc.startTransaction();

		int intPianiAcessiAperti = 0;

		intPianiAcessiAperti = chiudi_pianiTransactional(dbc, h);

		dbc.commitTransaction();
		done=true;
		return (new Integer(intPianiAcessiAperti));
	} catch (Exception e) {
		rollback_nothrow(metodo, dbc);
		throw newEjbException("Errore eseguendo " + metodo + ": " + e.getMessage(), e);
	} finally {
		close_dbcur_nothrow(metodo, dbcur);
		logout_nothrow(metodo, dbc);
	}
  }


/**
 * Permette di eseguire la chiudi_piani rimanendo all'interno di una transazione
 * 
 * @param dbc	la connessione sulla quale effettuare la insert
 * @param h		l'hashtabel con i parametri di insert
 * @return		il numero di piani accessi rimasti aperti
 */
public Integer chiudi_pianiTransactional(ISASConnection dbc, Hashtable h) throws Exception{
	metodo="chiudi_pianiTransactional";
	ISASCursor dbcur = null;

	int intPianiAcessiAperti = 0;
	try{
		String strPaTipoOper = null;
		String strNCartella = null;
		String strNProgetto = null;
		String strCodObiettivo = null;
		String strNIntervento = null;
		String strPaData = null;
		String strClausoleAggiuntive = "";
		//gb 08/08/07  String strPiProg = (String) h.get("pi_prog");
		String strPiProg = ""; //gb 08/08/07

		try{
			strPaTipoOper = (String) h.get("pa_tipo_oper");
			strNCartella = (String) h.get("n_cartella");
			strNProgetto = (String) h.get("n_progetto");
			//gb 10/08/07 *******
			if (strPaTipoOper.equals("01")){
				strCodObiettivo = (String) h.get("cod_obbiettivo");
				strNIntervento = (String) h.get("n_intervento");
			}
			//gb 10/08/07: fine *******
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("PianoAssistEJB/chiudi_piani - Errore: manca la chiave primaria", e);
		}

		String dataCh=(String)h.get("data_chiusura");

		//gb 10/08/07 *******
		if (strPaTipoOper.equals("01")){
			strClausoleAggiuntive = " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
					" AND n_intervento = " + strNIntervento;
		}
		//gb 10/08/07: fine *******

		String sel = "SELECT *" +
				" FROM piano_assist" +
				" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
				" AND n_cartella = " + strNCartella +
				" AND n_progetto = " + strNProgetto +
				strClausoleAggiuntive + //gb 10/08/07
				" AND pa_data_chiusura IS NULL";

		debugMessage("PianoAssistEJB/chiudi_piani - sel: " + sel);
		dbcur = dbc.startCursor(sel);
		while(dbcur.next()){
			ISASRecord dbr = dbcur.getRecord();
			String selRead = "SELECT *" +
					" FROM piano_assist" +
					" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
					" AND n_cartella = " + strNCartella +
					" AND n_progetto = " + strNProgetto +
					strClausoleAggiuntive + //gb 10/08/07
					" AND pa_data = " + formatDate(dbc,((java.sql.Date)dbr.get("pa_data")).toString());
			dbr=dbc.readRecord(selRead);
			dbr.put("pa_data_chiusura", ""+java.sql.Date.valueOf(dataCh));
			//  debugMessage("chiudo piano_assist..."+dbr.getHashtable().toString());
			dbc.writeRecord(dbr);
		}

		String mySelect = "SELECT *" +
				" FROM piano_accessi" +
				" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
				" AND n_cartella = " + strNCartella +
				" AND n_progetto = " + strNProgetto +
				strClausoleAggiuntive + //gb 10/08/07
				" AND (pi_data_fine IS NULL ";
		String dataChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.DATACHIUSURAPIANO);
		if (!ManagerDate.validaData(dataChiusura)){
			dataChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.DATA_CHIUSURA);
		}
		mySelect +=" OR pi_data_fine > " + formatDate(dbc, dataChiusura) + " ) ";

		LOG.info("PianoAssistEJB/chiudi_piani - (piani accessi e data_fine==null): " + mySelect);
		dbcur = dbc.startCursor(mySelect);
		while(dbcur.next()){
			ISASRecord dbr = dbcur.getRecord();
			strPaData = ((java.sql.Date) dbr.get("pa_data")).toString();
			strPiProg = ((Integer) dbr.get("pi_prog")).toString();

			String mySelPianoAcc = "SELECT *" +
					" FROM piano_accessi" +
					" WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
					" AND n_cartella = " + strNCartella +
					" AND n_progetto = " + strNProgetto +
					strClausoleAggiuntive + //gb 10/08/07
					" AND pa_data = " + formatDate(dbc, strPaData) +
					" AND pi_prog = " + strPiProg +
					" AND (pi_data_fine IS NULL " +
					" OR pi_data_fine > " + formatDate(dbc, ISASUtil.getValoreStringa(h, CostantiSinssntW.DATACHIUSURAPIANO)) + " ) ";

			LOG.info("PianoAssistEJB/chiudi_piani - mySelPianoAcc: " + mySelPianoAcc);
			ISASRecord dbr2 = dbc.readRecord(mySelPianoAcc);
			//LOG.info("DataChiusura:"+""+java.sql.Date.valueOf(dataCh));
			//      LOG.debug("\n-->PianoAssistEJB/chiudi_piani - PRIMA di dbr2.put(pi_data_fine)");
			dbr2.put("pi_data_fine", ""+java.sql.Date.valueOf(dataCh));
			//      LOG.debug("\n-->PianoAssistEJB/chiudi_piani - PRIMA di dbc.writeRecord(dbr2)");
			dbc.writeRecord(dbr2);
			//      LOG.debug("\n-->PianoAssistEJB/chiudi_piani - DOPO di dbc.writeRecord(dbr2)");
			intPianiAcessiAperti++;
			cap.rimuovoAgendaCaricata(dbc,dbr2.getHashtable(),nomeEJB+"/"+metodo); //gb 09/08/07
		}
	} catch(Exception e){
		throw newEjbException("Errore eseguendo "+ metodo + ": " + e.getMessage(), e);
	}finally{
		close_dbcur_nothrow(nomeEJB+metodo, dbcur);
	}
	return intPianiAcessiAperti;
}

//gb 05/02/07: porting da CJ 29/11/2006
public ISASRecord duplica_piano(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	metodo="duplica_piano";
	boolean done=false;
	//ISASCursor dbcurAg=null;
	//ISASCursor dbcur=null;
	/*String strNCartella =null;
	String strNProgetto =null;
	String strCodObiettivo =null;
	String strNIntervento =null;
	String strPaTipoOper =null;
    String strPaData =null;*/
    ISASRecord dbrit=null;
	//ISASRecord dbrPA=null;
	ISASConnection dbc=null;
	// 04/05/11: causa problemi dovuti a campi ISAS voluti da RME
	//String msg = "";
	//String msg_0 = "Non si hanno sufficienti permessi per aggiornare la data chiusura";
	
  /*   try {
		strNCartella = (String)h.get("n_cartella");
		strNProgetto = (String)h.get("n_progetto");
		strCodObiettivo = (String)h.get("cod_obbiettivo");
		strNIntervento = (String)h.get("n_intervento");
		strPaTipoOper = (String)h.get("pa_tipo_oper");
        strPaData = (String)h.get("data_apertura");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}*/
	try{
		ServerUtility su=new ServerUtility();
        dbc=super.logIn(mylogin);
        dbc.startTransaction();
        try {
        dbrit=cap.duplicaPiano(dbc, h);
        }catch(CariException e) {
        	dbc.commitTransaction();
        	done=true;
        	throw e;
        }     
        dbc.commitTransaction();
	    done=true;
	    return dbrit;
	} catch (CariException ce) {// 04/05/11
			ce.setISASRecord(dbrit);
			throw ce;
	}catch(DBRecordChangedException e){
		throw e;
	} catch(ISASPermissionDeniedException e){
		//throw e;
		throw new CariException("permessi insufficienti");		
	} catch(Exception e){
		e.printStackTrace();
		throw newEjbException(nomeEJB+"duplica_piano: " + e.getMessage(), e);
	} finally{
		if (!done){
			rollback_nothrow(nomeEJB+".duplica_piano", dbc);
			LOG.error(nomeEJB+".duplicaPiano ROLLBACK ");
		}
		//       close_dbcur_nothrow(nomeEJB+".duplicaPiano", dbcur);
		logout_nothrow(nomeEJB+"duplica_piano", dbc);
	} 
}

/**
*  fase aggiornamento agenda_interv e agenda_intpre
*  nonpiu usata giugno 2012
*/
/*
public Hashtable update_agenda(myLogin mylogin,Hashtable hin)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
    Hashtable hRit=null;
	String cartella=null;
	String strNProgetto = null;
	String strCodObiettivo = null;
	String strNIntervento = null;
	String dataIn=null;
    String dataInput=null;
	String dataFi=null;
	String prest_old=null;
	String oper=null;
	String prog=null;
	ISASConnection dbc=null;
	ISASCursor dbcur=null;
	try {
		cartella=(String)hin.get("n_cartella");
		strNProgetto = (String)hin.get("n_progetto");
		strCodObiettivo = (String)hin.get("cod_obbiettivo");
		strNIntervento = (String)hin.get("n_intervento");
		dataInput=formDate((String)hin.get("data_input"),"aaaa-mm-gg");
		dataIn=formDate((String)hin.get("pi_data_inizio"),"aaaa-mm-gg");
		dataFi=formDate((String)hin.get("pi_data_fine"),"aaaa-mm-gg");
		prest_old=(String)hin.get("pi_prest_cod_old");
		oper=(String)hin.get("as_op_referente");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
	  dbc=super.logIn(mylogin);
	  dbc.startTransaction();
	  Vector modif=(Vector)hin.get("vet_modif");
	  debugMessage("VETTORE CON MODIFICHE APPORTATE:"+modif.toString());
	  String del1="";
	  String del2="";
	  if(modif.contains("DI")) del1=" and ap_data<"+formatDate(dbc,dataIn)+" ";
	  //Il metodo formDate restituisce la stringa 0000-00-00.
	  //Siccome d� errore, controllo che se la data inizia con 00 non filtri la data fine
	  if(modif.contains("DF"))
		if(!dataFi.startsWith("00"))
		  del2=" and ap_data>"+formatDate(dbc,dataFi)+" ";
//gb 29/08/07	  String mysel="select * from agenda_interv,agenda_intpre "+
	  String mysel="select * from agendant_interv, agendant_intpre "+ //gb 29/08/07
		" where ag_data>="+formatDate(dbc,dataInput)+" and "+
		" ag_oper_ref='"+oper+"' and "+
		" ag_cartella="+cartella+" and "+
//gb 29/08/07		" ag_contatto="+contatto+" and "+
//gb 29/08/07 *******
		" ag_contatto =  " + strNProgetto + " and "+
		" cod_obbiettivo = '" + strCodObiettivo + "' and "+
		" n_intervento = " + strNIntervento + " and "+
//gb 29/08/07: fine *******
		" ap_prest_cod='"+prest_old+"' and "+
		" ag_data=ap_data and "+
		" ag_progr=ap_progr and "+
		" ag_oper_ref=ap_oper_ref "+
		del1+del2+
		" order by ag_data";
	   debugMessage("select caso modifica data inizio o data fine per rimuovere:"+mysel);
        if(modif.contains("DI")||modif.contains("DF")){
		dbcur=dbc.startCursor(mysel);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				//debugMessage("record i="+i+" rec letto="+dbrec.getHashtable().toString());
				if (dbrec != null){
					cap.cancellaAppuntam(dbrec,dbc);
				}
			}
        }
       if(modif.contains("P")||modif.contains("Q")||modif.contains("F")){
		dbcur=null;
//gb 29/08/07		mysel="select * from agenda_interv,agenda_intpre "+
		mysel="select * from agendant_interv, agendant_intpre "+ //gb 29/08/07
		" where ag_data>="+formatDate(dbc,dataInput)+" and "+
		" ag_oper_ref='"+oper+"' and "+
		" ag_cartella="+cartella+" and "+
//gb 29/08/07		" ag_contatto="+contatto+" and "+
//gb 29/08/07 *******
		" ag_contatto =  " + strNProgetto + " and "+
		" cod_obbiettivo = '" + strCodObiettivo + "' and "+
		" n_intervento = " + strNIntervento + " and "+
//gb 29/08/07: fine *******
		" ap_prest_cod='"+prest_old+"' and "+
		" ag_data=ap_data and "+
		" ag_progr=ap_progr and "+
		" ag_oper_ref=ap_oper_ref "+
		" order by ag_data";
		debugMessage("Select per aggiornamento agenda "+mysel);
		dbcur=dbc.startCursor(mysel);
		Vector vdbr = dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
			for(int i=0; i<vdbr.size(); i++) {
				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
				//debugMessage("record i="+i+" rec letto="+dbrec.getHashtable().toString());
				if (dbrec != null){
					modifica(dbrec,dbc,hin);
				}
			}
       }

	if(dbcur!=null)
		dbcur.close();
	dbc.commitTransaction();
	dbc.close();
	super.close(dbc);
	done=true;
	String msg="Operazione completata con successo.";
	hRit=new Hashtable();
	hRit.put("msg",msg);
	hRit.put("prog",""+prog);
	return hRit;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
                try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(Exception e1){
		LOG.error(e1);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw new SQLException("Errore eseguendo una insert() - "+  e1);
	}finally{
		if(!done){
			try{
				if (dbcur != null)
                    dbcur.close();
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){LOG.error(e2);}
		}
	}
}

*/
/*
private void modifica(ISASRecord dbrec,ISASConnection dbc,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
  Vector v=(Vector)h.get("vet_modif");
  try{
  String data=((java.sql.Date)dbrec.get("ap_data")).toString();
//gb 29/08/07  String selag="SELECT * FROM agenda_intpre WHERE "+
  String selag="SELECT * FROM agendant_intpre WHERE " + //gb 29/08/07
		"ap_data="+formatDate(dbc,data)+" AND "+
		"ap_progr="+dbrec.get("ap_progr")+" AND "+
		"ap_oper_ref='"+(String)dbrec.get("ap_oper_ref")+"' AND "+
		"ap_prest_cod='"+(String)dbrec.get("ap_prest_cod")+"'";
  //LOG.info("select modifica ..."+selag);
  ISASRecord dbag=dbc.readRecord(selag);
  String freq;
  if(dbag!=null)
  {
    //LOG.info("in modifica dbag..."+dbag.getHashtable().toString());
    if(v.contains("P")){
//gb 29/08/07      ISASRecord dbr=dbc.newRecord("agenda_intpre");
      ISASRecord dbr=dbc.newRecord("agendant_intpre"); //gb 29/08/07
      dbr.put("ap_prest_cod",(String)h.get("pi_prest_cod"));
      dbr.put("ap_prest_qta",(String)h.get("quantita"));
      dbr.put("ap_data",dbag.get("ap_data"));
      dbr.put("ap_progr",dbag.get("ap_progr"));
      dbr.put("ap_oper_ref",dbag.get("ap_oper_ref"));
      dbr.put("ap_stato",dbag.get("ap_stato"));
	  //bargi 9/2/2007 si mette in agenda la frequenza 
      dbr.put("ap_alert",dbag.get("ap_alert"));
      dbc.writeRecord(dbr);      LOG.debug("modifica " + dbr.getHashtable().toString());//elisa b 15/05/12
      dbc.deleteRecord(dbag);      LOG.debug("cancellazione " + selag);//elisa b 15/05/12
    }else{//caso Q (quantit�) e F (frequenza)
      dbag.put("ap_prest_qta",(String)h.get("quantita"));
	  dbag.put("ap_alert",(String)h.get("ap_alert"));
      dbc.writeRecord(dbag);      LOG.debug("modifica " + selag);//elisa b 15/05/12
    }
  }
  }catch(Exception e){
    LOG.debug("Errore in modifica agenda_intpre");
    }
}
*/
	// 07/04/11
	public Double query_FreqMaxAcc(myLogin mylogin, Hashtable h) throws SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		String strPaTipoOper = (String)h.get("pa_tipo_oper");
		String strNCartella = (String)h.get("n_cartella");
		String strNProgetto = (String)h.get("n_progetto");
		String strCodObiettivo = (String)h.get("cod_obbiettivo");
		String strNIntervento = (String)h.get("n_intervento");
		String strPaData = (String)h.get("pa_data");

		try {
			dbc = super.logIn(mylogin);

			String mySelect = "SELECT *" +
                           " FROM piano_accessi" +
                           " WHERE pa_tipo_oper = '" + strPaTipoOper + "'" +
                           " AND n_cartella = " + strNCartella +
                           " AND n_progetto = " + strNProgetto +
                           " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                           " AND n_intervento = " + strNIntervento +
                           " AND pa_data = " + formatDate(dbc, strPaData);

			LOG.info("PianoAssistEJB/query_FreqMaxAcc - mySelect : " + mySelect);
			dbcur = dbc.startCursor(mySelect);

			double maxAcc = 0.0;
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr = dbcur.getRecord();
					String codFrq = (String)dbr.get("pi_freq");
					int quantita = 1;
					if (dbr.get("pi_prest_qta") != null)
						quantita = ((Integer)dbr.get("pi_prest_qta")).intValue();
					double numAcc = calcNumAcc(dbc, codFrq, quantita);
					if (numAcc > maxAcc)
						maxAcc = numAcc;
				}
			}
			dbcur.close();

			dbc.close();
			super.close(dbc);
			done=true;
			
			return new Double(maxAcc);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la query_FreqMaxAcc()  ");
		}
		finally {
			if(!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1){
					LOG.error(e1);
				}
			}
		}
	}

	// 07/04/11
	private double calcNumAcc(ISASConnection dbc, String cFrq, int quant) throws Exception
	{
		ISASRecord dbr = null;
		double ret = 0.0;
		double frq = 0.0;
		
		try {			
			String sel = "SELECT * FROM tab_voci"
					+ " WHERE tab_cod = 'FREQAC'"
					+ " AND tab_val = '" + cFrq + "'";
	
			dbr = dbc.readRecord(sel);
			
			if ((dbr != null) && (dbr.get("tab_codreg") != null)) {			
				frq = (new Double((String)dbr.get("tab_codreg"))).doubleValue();
				ret = frq * quant;				
			}
			LOG.info("PianoAssistEJB/calcNumAcc: frq=["+frq+"] - quant=["+quant+"] - ret=["+ret+"]");
			
			return ret;
		} catch(Exception e) {
			LOG.error("PianoAssistEJB/calcNumAcc: eccezione =" + e);
			throw e;
		}
	}
	
	// 26/03/13: cntrl esistenza PianoAssistenziale
	public Boolean checkEsistePianoAssist(myLogin mylogin, Hashtable h) throws Exception
	{
		ServerUtility su = new ServerUtility();
		String methodName = "checkEsistePianoAssist";
		ISASConnection dbc = null;
		boolean ret = false;
		ISASCursor dbcur = null;
		
		try {
			dbc = super.logIn(mylogin);
			
			String sel = "SELECT * FROM piano_assist"
				+ " WHERE n_cartella = " + (String)h.get("n_cartella");
				
			sel = su.addWhere(sel, su.REL_AND, "n_progetto", su.OP_EQ_NUM, (String)h.get("n_progetto"));
			sel = su.addWhere(sel, su.REL_AND, "cod_obbiettivo", su.OP_EQ_STR, (String)h.get("cod_obbiettivo"));
			sel = su.addWhere(sel, su.REL_AND, "n_intervento", su.OP_EQ_NUM, (String)h.get("n_intervento"));
			sel = su.addWhere(sel, su.REL_AND, "pa_tipo_oper", su.OP_EQ_STR, (String)h.get("pa_tipo_oper"));
		
			dbcur = dbc.startCursor(sel);
			ret = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();

			return new Boolean(ret);
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(methodName, dbcur);
			logout_nothrow(methodName, dbc);
		}
	}
	
	public String gestisci_chiusurePerSospensioneOProroga(myLogin mylogin, ISASConnection dbc, Hashtable dati) throws  Exception{
		RMSkSOEJB rmskso = new RMSkSOEJB();
		// Chiusura Contatto Inf
		String msg="";
		String sep="";
		String scheda="infermieristica";
		String nCartella = dati.get(CostantiSinssntW.N_CARTELLA).toString();
		String idSkSo    = dati.get(CostantiSinssntW.CTS_ID_SKSO).toString();
		try{
		
		String ski_select="select * from skinf where n_cartella = "+nCartella.toString()
							+" and id_skso = "+idSkSo.toString()
							+" and ski_data_uscita is null";
		ISASRecord ski_dbr = dbc.readRecord(ski_select);
		if (ski_dbr!=null){
			chiudiPADaContatto(dbc, dati, rmskso, ski_dbr, GestTpOp.CTS_COD_INFERMIERE);
		}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException) throw e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		try{
		//Chiusura contatto Fisio
		scheda="fisioterapista";
		String skf_select="select * from skfis" +
				" where n_cartella = "+nCartella.toString()
							+" and id_skso = "+idSkSo.toString()
							+" and skf_data_chiusura is null";
		ISASRecord skf_dbr = dbc.readRecord(skf_select);
		if (skf_dbr!=null){
			chiudiPADaContatto(dbc, dati, rmskso, skf_dbr, GestTpOp.CTS_COD_FISIOTERAPISTA);
		}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException) throw e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
			}
		try{
		//Chiusura contatto medico
		scheda="medica";
		SkMedEJB skfmed_ejb = new SkMedEJB();
		String skm_select="select * from skmedico" +
				" where n_cartella = "+nCartella.toString()
							+" and id_skso = "+idSkSo.toString()
							+" and skm_data_chiusura is null";
		ISASRecord skm_dbr = dbc.readRecord(skm_select);
		if (skm_dbr!=null){
			chiudiPADaContatto(dbc, dati, rmskso, skm_dbr, GestTpOp.CTS_COD_MEDICO);
		}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException) throw e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		try{
			//Chiusura contatto medico
			scheda="operatore generico";
			SkFpgEJB skfpg_ejb = new SkFpgEJB();
			String skfpg_select="select * from skfpg" +
					" where n_cartella = "+nCartella.toString()
								+" and id_skso = "+idSkSo.toString()
								+" and skfpg_data_uscita is null";
			ISASCursor skfpg_dbcur = dbc.startCursor(skfpg_select);
			if (skfpg_dbcur!=null && skfpg_dbcur.getDimension()>0){
				while (skfpg_dbcur.next()){
					ISASRecord curr_skfpg = skfpg_dbcur.getRecord();
					chiudiPADaContatto(dbc, dati, rmskso, curr_skfpg, ISASUtil.getValoreStringa(curr_skfpg, "skfpg_tipo_operatore"));
				}
				skfpg_dbcur.close();
			}
		}catch(Exception e){
			e.printStackTrace();			
			if (e instanceof CariException) throw e;
			else{
				msg+=sep+"scheda "+scheda;
				sep=",";
			}
		}
		return msg;
	}

	private void chiudiPADaContatto(ISASConnection dbc, Hashtable dati, RMSkSOEJB rmskso, ISASRecord ski_dbr,
			String tipoOperatore) throws Exception{
		dati.put(CostantiSinssntW.TIPO_OPERATORE, tipoOperatore);
		dati.put("data_richiesta", dati.get(CostantiSinssntW.DATACHIUSURAPIANO));
		dati.put("data_chiusura", dati.get(CostantiSinssntW.DATACHIUSURAPIANO));
		boolean pianificato = rmskso.isPianificatoPAITransactional(dbc, dati);
		if(pianificato){
			dati.put(CostantiSinssntW.N_PROGETTO, ISASUtil.getValoreStringa(ski_dbr, CostantiSinssntW.N_CONTATTO));
			dati.put("pa_tipo_oper", tipoOperatore);
			LOG.debug("Chiudo i piani per il Tipo Operatore: "+ tipoOperatore);
			Integer piano_assist= chiudi_pianiTransactional(dbc, dati);
		}
	}
	
	public String gestisci_aperturePerSospensioneOProroga(myLogin mylogin, ISASConnection dbc, Hashtable dati) throws  Exception{
		GestCasi gestione_casi = new GestCasi();
		if(gestione_casi.getUbicazione(dbc, dati)==GestCasi.UBI_RMARCHE){
			dati.put(CostantiSinssntW.COD_OBBIETTIVO, "00000000");
			dati.put(CostantiSinssntW.N_INTERVENTO, 0);
		}
		RMSkSOEJB rmskso = new RMSkSOEJB();
		// Duplica piano per  Contatto Inf
		String msg="";
		String sep="";
		String scheda="infermieristica";
		String nCartella = dati.get("n_cartella").toString();
		String idSkSo    = dati.get("id_skso").toString();
		try{
		
		String ski_select="select * from skinf where n_cartella = "+nCartella.toString()
							+" and id_skso = "+idSkSo.toString();
		ISASRecord ski_dbr = dbc.readRecord(ski_select);
		if (ski_dbr!=null){
			duplicaPADaContatto(mylogin, dati, rmskso, ski_dbr, GestTpOp.CTS_COD_INFERMIERE);
		}
		}catch(Exception e){
			e.printStackTrace();			
			msg+=sep+"scheda "+scheda;
			sep=",";
		}
		try{
		//Duplica piano per  contatto Fisio
		scheda="fisioterapista";
		String skf_select="select * from skfis" +
				" where n_cartella = "+nCartella.toString()
							+" and id_skso = "+idSkSo.toString()
							+" and skf_data_chiusura is null";
		ISASRecord skf_dbr = dbc.readRecord(skf_select);
		if (skf_dbr!=null){
			duplicaPADaContatto(mylogin, dati, rmskso, skf_dbr, GestTpOp.CTS_COD_FISIOTERAPISTA);
		}
		}catch(Exception e){
			e.printStackTrace();			
				msg+=sep+"scheda "+scheda;
				sep=",";
		}
		try{
		//Duplica piano per  contatto medico
		scheda="medica";
		SkMedEJB skfmed_ejb = new SkMedEJB();
		String skm_select="select * from skmedico" +
				" where n_cartella = "+nCartella.toString()
							+" and id_skso = "+idSkSo.toString()
							+" and skm_data_chiusura is null";
		ISASRecord skm_dbr = dbc.readRecord(skm_select);
		if (skm_dbr!=null){
			duplicaPADaContatto(mylogin, dati, rmskso, skm_dbr, GestTpOp.CTS_COD_MEDICO);
		}
		}catch(Exception e){
			e.printStackTrace();			
			msg+=sep+"scheda "+scheda;
			sep=",";
		}
		try{
			//Duplica piano per  contatto medico
			scheda="operatore generico";
			SkFpgEJB skfpg_ejb = new SkFpgEJB();
			String skfpg_select="select * from skfpg" +
					" where n_cartella = "+nCartella.toString()
								+" and id_skso = "+idSkSo.toString()
								+" and skfpg_data_uscita is null";
			ISASCursor skfpg_dbcur = dbc.startCursor(skfpg_select);
			if (skfpg_dbcur!=null && skfpg_dbcur.getDimension()>0){
				while (skfpg_dbcur.next()){
					ISASRecord curr_skfpg = skfpg_dbcur.getRecord();
					duplicaPADaContatto(mylogin, dati, rmskso, curr_skfpg, (String) curr_skfpg.get("skfpg_tipo_operatore"));
				}
				skfpg_dbcur.close();
			}
		}catch(Exception e){
			e.printStackTrace();			
			msg+=sep+"scheda "+scheda;
			sep=",";
		}
		return msg;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void duplicaPADaContatto(myLogin mylogin, Hashtable dati, RMSkSOEJB rmskso, ISASRecord ski_dbr,
			String tipoOperatore) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException, ISASMisuseException {
		dati.put(CostantiSinssntW.TIPO_OPERATORE, tipoOperatore);
		dati.put("data_richiesta", dati.get(CostantiSinssntW.DATACHIUSURAPIANO));
		dati.put("data_chiusura", dati.get(CostantiSinssntW.DATACHIUSURAPIANO));
		boolean pianificato = rmskso.isPianificatoPAI(mylogin, dati);
		if(pianificato){
			dati.put("n_progetto", ski_dbr.get(CostantiSinssntW.N_CONTATTO));
			dati.put("pa_tipo_oper", tipoOperatore);
			LOG.debug("Duplico i piani per il Tipo Operatore: "+ tipoOperatore);
			Vector<ISASRecord> piani = query_pianoAss(mylogin, dati);
			if(piani.size()>0){
				//devo duplicare l'ultimo piano quindi prendo la data di apertura del primo record 
				ISASRecord piano = piani.get(0);
				dati.put("data_apertura", piano.get("pa_data"));
				duplica_piano(mylogin, dati);
			}
		}
	}
	
	
	/**
	 * elisa b 21/06/16:
	 * Metodo per la gestione del campo flag_stato che indica lo
	 * stato del piano assistenziale e del relativo piano accessi in relazione
	 * ai servizi esposti alle cooperative che ne consentono l'acquisizione.
	 * A livello applicativo il flag stato puo' essere settato solo ai valori seguenti
	 * 0 = IN ELABORAZIONE
	 * 1 = DA INVIARE (piano congelato e quindi non modificabile)
	 * @param mylogin
	 * @param h
	 * @throws Exception
	 */
	public void aggiornaFlagStatoPianoAssistenziale(myLogin mylogin, Hashtable h) throws Exception {
		String methodName = "aggiornaFlagStatoPianoAssistenziale";
		ISASConnection dbc = null;
		boolean done = false;
		String stato = (String)h.get("flag_stato");
		
		try {
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			
			aggiornaFlagStatoPianoAssist(dbc, h, stato);
			
			dbc.commitTransaction();
			done = true;				
		} catch(DBRecordChangedException e){
			/* caso in cui il record letto e' stato modificato da procedura
			 * o da un altro accesso concorrente. Si fa una nuova lettura e
			 * si ritenta l'aggiornamento*/
			LOG.info(methodName + " piano modificato con accesso concorrento, tento di nuovo l'aggiornamento");
	     	aggiornaFlagStatoPianoAssist(dbc, h, stato);
	     	
			dbc.commitTransaction();
			done = true;
		} catch (Exception e) {
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);

		} finally {
			if(!done)
				rollback_nothrow(methodName, dbc);
			logout_nothrow(methodName, dbc);
		}
	}
	
	/**
	 * elisa b 21/06/16
	 * Metoto privato che aggiorna lo stato del campo flag_stato
	 * @param dbc
	 * @param h
	 * @param stato
	 * @throws ISASMisuseException
	 * @throws ISASPermissionDeniedException
	 * @throws DBMisuseException
	 * @throws DBRecordChangedException
	 * @throws DBSQLException
	 */
	private void aggiornaFlagStatoPianoAssist(ISASConnection dbc, Hashtable h, String stato)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBRecordChangedException,
			DBSQLException {
		String sel = "SELECT *" +
     			" FROM piano_assist" +
    			" WHERE pa_tipo_oper = '" + h.get("tipo_operatore") + "'" + 
    			" AND n_cartella = " + h.get("n_cartella") +
    			" AND n_intervento = " + h.get("n_intervento") +
    			" AND n_progetto = " + h.get("n_progetto") +
    			" AND pa_data = " + formatDate(dbc, h.get("pa_data").toString());
     	
		ISASRecord dbrP = dbc.readRecord(sel);
		
		aggiornaFlagStatoPianoAccessi(dbc, dbrP, stato);
		
     	dbrP.put("flag_stato", stato);
     		     	
     	dbc.writeRecord(dbrP);

	}
	
	/**
	 * elisa b 21/06/16:
	 * Metodo per la gestione del campo flag_stato che indica lo
	 * stato del piano accessi in relazione
	 * ai servizi esposti alle cooperative che ne consentono l'acquisizione.
	 * A livello applicativo il flag stato puo' essere settato solo ai valori seguenti
	 * 0 = IN ELABORAZIONE
	 * 1 = DA INVIARE (piano congelato e quindi non modificabile) 
	 * @param dbc
	 * @param dbr
	 * @param stato
	 * @throws ISASMisuseException
	 * @throws ISASPermissionDeniedException
	 * @throws DBMisuseException
	 * @throws DBSQLException
	 * @throws DBRecordChangedException
	 */
	private void aggiornaFlagStatoPianoAccessi(ISASConnection dbc,
			ISASRecord dbr, String stato)
			throws ISASMisuseException, ISASPermissionDeniedException,
			DBMisuseException, DBSQLException, DBRecordChangedException {
		ISASCursor dbcur = null;
		
		try {
			String sel = "SELECT *" +
	     			" FROM piano_accessi" +
	    			" WHERE pa_tipo_oper = '" + dbr.get("pa_tipo_oper") + "'" + 
	    			" AND n_cartella = " + dbr.get("n_cartella") +
	    			" AND n_progetto = " + dbr.get("n_progetto") +
	    			" AND cod_obbiettivo = " + dbr.get("cod_obbiettivo") +
	    			" AND n_intervento = " + dbr.get("n_intervento") +    			
	    			" AND pa_data = " + formatDate(dbc, dbr.get("pa_data").toString());
			dbcur = dbc.startCursor(sel);
			while(dbcur.next()){
		     	ISASRecord dbrP = dbcur.getRecord();
		     	sel = "SELECT *" +
		     			" FROM piano_accessi" +
		    			" WHERE pa_tipo_oper = '" + dbrP.get("pa_tipo_oper") + "'" + 
		    			" AND n_cartella = " + dbrP.get("n_cartella") +
		    			" AND n_progetto = " + dbrP.get("n_progetto") +
		    			" AND cod_obbiettivo = '" + dbrP.get("cod_obbiettivo") + "'" + 
		    			" AND n_intervento = " + dbrP.get("n_intervento") +    			
		    			" AND pa_data = " + formatDate(dbc, dbrP.get("pa_data").toString()) +
		    			" AND pi_prog = " + dbrP.get("pi_prog");
		     	ISASRecord dbrP1 = dbc.readRecord(sel);
		     	dbrP1.put("flag_stato", stato);
		     	dbc.writeRecord(dbrP1);
			}
		} finally {
			close_dbcur_nothrow("aggiornaFlagStatoPianoAccessi", dbcur);
		}
	}

	/**
	 * elisa b 21/06/16
	 * Metodo che verifica se il piano e' stato inviato a una qualsiasi societa'
	 * di servizi
	 * @param mylogin
	 * @param h
	 * @return
	 * @throws Exception
	 */
	public Boolean isPianoAssistInviato(myLogin mylogin, Hashtable h) throws Exception{
		ServerUtility su = new ServerUtility();
		String methodName = "isPianoAssistInviato";
		ISASConnection dbc = null;
		
		try {
			dbc = super.logIn(mylogin);
			
			String sel = "SELECT count(*) tot" +
						" FROM cde_eventi pi" +					
	    				" WHERE pa_tipo_oper = '" + h.get("tipo_operatore") + "'" + 
		    			" AND n_cartella = " + h.get("n_cartella") +
		    			" AND n_intervento = " + h.get("n_intervento") +
		    			" AND n_progetto = " + h.get("n_progetto") +
		    			" AND pa_data = " + formatDate(dbc, h.get("pa_data").toString()) +		        			
	        			" AND tipo_operazione = '" + INVIO_PIANO + "'";
			ISASRecord dbr = dbc.readRecord(sel);
			
			LOG.debug(methodName + " , " + sel);
			
			return new Boolean(dbr != null && (Integer)dbr.get("tot") > 0);
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(methodName, dbc);
		}
	}
}

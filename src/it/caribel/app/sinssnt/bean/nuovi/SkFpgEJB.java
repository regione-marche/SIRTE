package it.caribel.app.sinssnt.bean.nuovi;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 27/10/2014  Contatto Generico
// ripreso da SkInfEJB
// ==========================================================================

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.sinssnt.util.Costanti;
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
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.casi_adrsa.GestSegnalazione;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CartCntrlEtChiusure;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.catalina.Manager;
import org.zkoss.util.resource.Labels;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SkFpgEJB extends SINSSNTConnectionEJB  
{
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	// 14/04/08 m.: se configurato, permette la presenza di pi� contatti aperti contemporaneamente

	//06/04/2007 bargi aggiunto data chiusura del progetto piano assistenziale

	// 07/12/06 m.: sostituito campi di SKPATOLOGIE con quelli della nuova tabella DIAGNOSI.
	// 31/10/06 m.: aggiunto ONCOLOGO nel metodo "deleteContsan()".
	private String ver ="1-";
	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();
	// 21/05/09 m.
	private ScaleVal gest_scaleVal = new ScaleVal(); 
	
	// 05/02/13
	private EveUtils eveUtl = new EveUtils();

	private boolean mydebug = true;
		
	public SkFpgEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException, CariException 
	{
		boolean done=false;
		String n_cartella=null;
		String n_contatto=null;
		String skfpg_tipo_operatore=null;
		String data_apertura = null;
		String dimiss=null;
		ISASConnection dbc=null;
		ISASCursor dbcur = null;   // 07/12/06 m.
		String dtAssistitoChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);

		try
		{
			n_cartella=ISASUtil.getValoreStringa(h,"n_cartella");
			n_contatto=ISASUtil.getValoreStringa(h,"n_contatto");
			skfpg_tipo_operatore=ISASUtil.getValoreStringa(h,"skfpg_tipo_operatore");
			data_apertura = ISASUtil.getValoreStringa(h,"skfpg_data_apertura");    
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SQLException("SkInf queryKey: Errore: manca la chiave primaria", e);
		}

		try
		{
			dbc=super.logIn(mylogin);
			
			
			if(h.get("dimiss")!=null)	dimiss=(String)h.get("dimiss");

			String myselect="Select s.* from skfpg s where s.n_cartella= "+n_cartella + 
								" and skfpg_tipo_operatore = '"+skfpg_tipo_operatore+"' ";
			
			if (ISASUtil.valida(n_contatto)){
				myselect +=" and n_contatto= "+n_contatto;
			}
			if (ManagerDate.validaData(dtAssistitoChiusura)){
				myselect += " AND s.n_contatto IN ( SELECT MAX (x.n_contatto) FROM skfpg x " +
							" WHERE x.n_cartella = s.n_cartella AND x.skfpg_tipo_operatore = s.skfpg_tipo_operatore ) "; 
			}
			
			//Commento richiesto da Andrea
			//" and skfpg_data_apertura="+formatDate(dbc,data_apertura);
			printError("select query_key su skfpg==="+myselect);

			ISASRecord dbr=dbc.readRecord(myselect);
			// 22/08/14 simone: aggionto per poterlo richiamare anche da altri metodi
			if (dbr!=null){
				dbr = verificaEAggiornareIdSkso(dbc, dbr, myselect);
				gestisciDecodifiche(dbc,dbr);
			
			Object strDtSkVal = (String)h.get("pr_data"); // 17/04/08
			// 20/12/11
//			if (strDtSkVal == null) {
//				ISASRecord recProg = getProgetto(dbc, n_cartella, dbr.get("skfpg_data_apertura").toString());
//				if ((recProg != null) && (recProg.get("pr_data") != null))
//					strDtSkVal = (recProg.get("pr_data"));
//			}
			dbr.put("pr_data", strDtSkVal);// 17/04/08
			
			
//			// 20/05/09 Elisa Croci			
//			if(gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN))
//			{
//				// 15/06/09 Elisa Croci    ********************************************************
//				if(h.containsKey("ubicazione") && h.get("ubicazione") != null)
//					dbr.put("ubicazione", h.get("ubicazione"));
//				if(h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
//					dbr.put("update_segnalazione", h.get("update_segnalazione"));
//				// *********************************************************************************
//				
//				int caso = prendi_dati_caso(dbc,dbr);
//				if(prendi_segnalazione(dbc,caso,dbr))
//					prendi_presacarico(dbc,caso,dbr);
//			}
			}
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{		
			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}
		finally
		{
			if(!done)
			{
				try
				{
					if (dbcur != null) dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2){    LOG.error(e2);    }
			}
		}
	}

	private ISASRecord verificaEAggiornareIdSkso(ISASConnection dbc, ISASRecord dbr, String query){
		String punto = ver + "verificaAggiornareIdSkso ";
		String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
		if (ISASUtil.valida(idSkso)) {
			LOG.trace(punto + " idskso e' valorizzato non modifico ");
		} else {
			LOG.trace(punto + " idskso non e' valorizzato, provo a recuperarlo ");
			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
			String nCartella = ISASUtil.getValoreStringa(dbr, Costanti.N_CARTELLA);
			String skiDataApertura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.SKI_DATA_APERTURA);
			
			ISASRecord dbrRmSkSoMMG;
			try {
				dbrRmSkSoMMG = rmSkSOEJB.recuperaRmSksoMmg(dbc, nCartella, skiDataApertura);
				idSkso = ISASUtil.getValoreStringa(dbrRmSkSoMMG, CostantiSinssntW.CTS_ID_SKSO);
				if (ISASUtil.valida(idSkso)){
					dbr.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
					dbc.writeRecord(dbr);
					LOG.trace(punto + " query per rileggere il record aggiornato \n" + query);
					dbr = dbc.readRecord(query);
				}
			} catch (ISASMisuseException e) {
				LOG.error(punto + " Errore mancano diritti per leggere le info ", e);				
			} catch (ISASPermissionDeniedException e) {
				LOG.error(punto + " Errore mancano i permesssi per leggere le info ", e);
			} catch (DBMisuseException e) {
				LOG.error(punto + " Errore mancano per leggere le info ", e);
			} catch (DBSQLException e) {
				LOG.error(punto + " Errore nelle query ", e);
			} catch (Exception e) {
				LOG.error(punto + " Errore generico ", e);
			}
		}
		return dbr;
	}
	
	private void gestisciDecodifiche(ISASConnection dbc, ISASRecord dbr) throws Exception {
		if (dbr!= null) 
		{
			String n_cartella = dbr.get("n_cartella").toString();
//			dbr.put("skfpg_data_adl",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_adl", dbr.get("skfpg_data_uscita")));
//			dbr.put("skfpg_data_pfeiffer",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_pfeiffer", dbr.get("skfpg_data_uscita")));
//			dbr.put("skfpg_data_iadl",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_iadl",dbr.get("skfpg_data_uscita")));
//			dbr.put("skfpg_data_caregiver",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_caregiver", dbr.get("skfpg_data_uscita"), "skcg_data"));
//			dbr.put("skfpg_data_braden",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_braden", dbr.get("skfpg_data_uscita"), "skb_data"));
//			dbr.put("skfpg_data_tiq",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_tiq", dbr.get("skfpg_data_uscita")));
//			// fine 05/12/06
//			dbr.put("skfpg_data_barthel",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_barthel", dbr.get("skfpg_data_uscita"),"skbt_data"));

			Hashtable h1 = dbr.getHashtable();
			printError("Hashtable=>"+h1.toString());
			
			if (h1.get("skfpg_operatore")!=null && !((String)h1.get("skfpg_operatore")).equals(""))
			{
				dbr.put("desc_operat", decodifica("operatori","codice",dbr.get("skfpg_operatore"),
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",dbc));
			}
			else dbr.put("desc_operat","");

			if (h1.get("skfpg_referente")!=null && !((String)h1.get("skfpg_referente")).equals(""))
			{
				dbr.put("desc_skfpg_referente", decodifica("operatori","codice",dbr.get("skfpg_referente"),
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",dbc));
			}
			else dbr.put("desc_skfpg_referente","");

			if (h1.get("skfpg_inviato")!=null && !((String)h1.get("skfpg_inviato")).equals(""))
			{
				dbr.put("desc_inviato", decodifica("segnala","codice",
						dbr.get("skfpg_inviato"),"descrizione",dbc));
			}
			else dbr.put("desc_inviato", "");

			if (h1.get("skfpg_tipout")!=null && !((String)h1.get("skfpg_tipout")).equals(""))
			{
				dbr.put("desc_tipout", decodifica("tipute_s","codice",
						dbr.get("skfpg_tipout"),"descrizione",dbc));
			}
			else dbr.put("desc_tipout", "");

			if (h1.get("skfpg_osp_dim")!=null && !((String)h1.get("skfpg_osp_dim")).equals(""))
			{
				dbr.put("desc_ospdim", decodifica("ospedali","codosp",
						dbr.get("skfpg_osp_dim"),"descosp",dbc));
			}
			else dbr.put("desc_ospdim", "");

			if (h1.get("skfpg_uo_dim")!=null && !((String)h1.get("skfpg_uo_dim")).equals(""))
			{
				dbr.put("desc_rep", decodifica("reparti","cd_rep",
						dbr.get("skfpg_uo_dim"),"reparto",dbc));
			}
			else dbr.put("desc_rep", "");

		
			leggiDiagnosi(dbc, dbr);
			
			// 26/03/13
//			dbr.put("grid_intamb", leggiSkiIntAmb(dbc, (Hashtable)dbr.getHashtable()));			

			
			
			// 05/02/13 
			dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skfpg_cod_presidio"), (String)dbr.get("skfpg_infermiere")));
			
			
		}
		
	}

	public ISASRecord getContattoGenCorrente(myLogin mylogin, Hashtable h)throws SQLException, CariException
	{
		printError("SkFpgEJB: getContattoGenCorrente " + h.toString());
		
		boolean done = false;
		String n_cartella = null;
		String skfpg_tipo_operatore = null;
		String dimiss = "";
		ISASConnection dbc = null;
		ISASRecord dbr = null; //gb 27/08/07

		n_cartella = (String)h.get("n_cartella");
		skfpg_tipo_operatore = (String)h.get("skfpg_tipo_operatore");
		String strDtSkVal = (String)h.get("pr_data"); //gb 27/08/07

		// 14/04/08: se configurato, permette la presenza di pi� contatti aperti contemporaneamente
		boolean multiCont = false;

		try
		{
			//gb 27/08/07 ---
			if (strDtSkVal == null)
			{
				printError("SkFpgEJB -->> getContattoGenCorrente: dataSkVal NULLA!!");
				done = true;
				return dbr;
			}
			//gb 27/08/07 ---

			// Ottengo la connessione al database
			dbc = super.logIn(mylogin);

			// 14/04/08
			String abilMultiCont = (String)leggiConf(dbc, "ABIL_NEWCONT_INF");
			printError(" abilitazione dal CONF: " + abilMultiCont);

			// 15/07/08 -----
			String zonaOper = getZonaOper(dbc, (String)h.get("skfpg_operatore"));
			abilMultiCont = getValxZona(abilMultiCont, zonaOper);
			// 15/07/08 -----

			multiCont = ((abilMultiCont != null) && (abilMultiCont.trim().equals("SI")));

		
			// Preparo la SELECT del record
			String myselect = "SELECT s.* FROM skfpg s" +
			" WHERE s.n_cartella = " + n_cartella +
			" AND s.skfpg_tipo_operatore ='"+skfpg_tipo_operatore+"'"+
			" AND s.skfpg_data_uscita IS NULL";

			// 14/04/08: se possono esserci pi� contatti aperti, si prende quello pi� recente
			if (multiCont)
				myselect += " AND s.skfpg_data_apertura IN (SELECT MAX(a.skfpg_data_apertura) FROM skfpg a" +
				" WHERE a.n_cartella = s.n_cartella" +
				" AND a.skfpg_tipo_operatore ='"+skfpg_tipo_operatore+"'"+
				" AND a.skfpg_data_uscita IS NULL)";

			printError("SkFpgEJB/getContattoGenCorrente : " + myselect);

			// Leggo il record
			dbr = dbc.readRecord(myselect);

			// Si decodificano alcuni campi della maschera a video.
			if (dbr != null)
			{
				
				gestisciDecodifiche(dbc, dbr);
				

				leggiDiagnosi(dbc, dbr);
				
				// 26/03/13
				
				dbr.put("pr_data", strDtSkVal);//gb 27/08/07
				
			
				// 05/02/13 
				
//				if(gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN))
//				{
//					// 15/06/09 Elisa Croci    ******************************************************
//					if(h.containsKey("ubicazione") && h.get("ubicazione") != null)
//						dbr.put("ubicazione", h.get("ubicazione"));
//					if(h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
//						dbr.put("update_segnalazione", h.get("update_segnalazione"));
//					// ******************************************************************************
//					
//					int caso = prendi_dati_caso(dbc,dbr);
//					printError("CASO DEL CONTATTO CORRENTE: " + caso);
//					if(prendi_segnalazione(dbc,caso,dbr))
//						prendi_presacarico(dbc,caso,dbr);
//				}
				
				printError("SkFpgEJB.getContattoGenCorrente DBR finale: " + dbr.getHashtable().toString());
			}
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{
			LOG.error("SkFpgEJB.getContattoGenCorrente(): "+e);
			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SQLException("Errore eseguendo SkFpgEJB/getContattoGenCorrente()  ", e);
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
				catch(Exception e2){   LOG.error(e2);   }
			}
		}
	}
	
	// 20/05/09 Elisa Croci
	/* 1) Il caso non esiste: creo il caso e la segnalazione
	 * 2) Il caso esiste ma e' chiuso: creo il caso e la segnalazione
	 * 3) Il caso e' attivo: aggiorno la segnalazione
	*/
//	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov) 
//	throws NumberFormatException, ISASMisuseException, CariException
//	{
//		printError(" gestione_segnalazione -- HASH: " +  h.toString() + " REC: " + dbr.getHashtable().toString());
//		
//		int stato_caso = -1;
//		int id_caso = -1;
//		
//		h.put("operZonaConf", (String)dbr.get("skfpg_operatore")); // 15/10/09
//		
//		if(dbr.get("id_caso") != null && !dbr.get("id_caso").equals("-1"))
//		{
//			// il caso esiste, prendo l'id e il suo stato
//			stato_caso = Integer.parseInt(dbr.get("stato").toString());
//			id_caso = Integer.parseInt(dbr.get("id_caso").toString());
//		}
//		
//		// se sono in insert e il caso non esiste oppure e' concluso, devo crearne uno!
//		if(prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU))
//		{
//			// se il caso non esiste, non c'e' nemmeno la segnalazione, allora la creo!
//			try 
//			{
//				h.put("tipo_caso", new Integer(GestCasi.CASO_SAN));
//				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_SANITARIO));
//				
//				if(h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals(""))
//					h.put("dt_segnalazione", h.get("skfpg_data_apertura"));
//				
//				if(h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
//					h.put("dt_presa_carico", h.get("dt_segnalazione"));
//				
//				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
//				// arriva la data del progetto, cosi' me la copio dal dbr!
//				h.put("pr_data", dbr.get("pr_data"));
//				
//				ISASRecord rec_segn = gestore_segnalazioni.insert(dbc, h);
//				
//				if(rec_segn != null)
//				{	
//					Enumeration en = rec_segn.getHashtable().keys();
//					while(en.hasMoreElements())
//					{
//						String chiave = en.nextElement().toString();
//						dbr.put(chiave, rec_segn.get(chiave));
//					}
//					
//					ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
//					if(rec_pc != null)
//					{
//						Enumeration en1 = rec_pc.getHashtable().keys();
//						while(en1.hasMoreElements())
//						{
//							String chiave = en1.nextElement().toString();
//							dbr.put(chiave, rec_pc.get(chiave));
//						}
//					
//						dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
//					}
//					
//					return Integer.parseInt(rec_segn.get("id_caso").toString());
//				}
//				else return -1;
//			}
//			catch (CariException e) // 17/11/09
//			{	
//				LOG.error("SkFpgEJB gestione_segnalazione, insert -- " + e);
//				throw e;
//			} 
//			catch (DBRecordChangedException e) 
//			{	
//				LOG.error("SkFpgEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
//				return id_caso;
//			} 
//			catch (ISASPermissionDeniedException e) 
//			{	
//				LOG.error("SkFpgEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);  
//				return id_caso;
//			} 
//			catch (SQLException e) 
//			{	
//				LOG.error("SkFpgEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
//				return id_caso;
//			} 
//			catch (Exception e) 
//			{   
//				LOG.error("SkFpgEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
//				return id_caso;
//			}
//		}
//// 29/03/10	else if(id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU &&  stato_caso != -1))
//		else if(id_caso != -1 && (stato_caso != -1))
//		{
//			// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione e della presa in carico
//			try 
//			{
//				Enumeration e = dbr.getHashtable().keys();
//				while(e.hasMoreElements())
//				{
//					String chiave = e.nextElement().toString();
//					
//					if(!h.containsKey(chiave))
//						h.put(chiave, dbr.get(chiave));
//				}
//				
//				printError(" gestione_segnalazione - UPDATE, H: " + h.toString());
//				
//				// 12/08/10 m ---------------
//				if(dbr.get("origine") != null && !dbr.get("origine").equals("")) {
//					int origine = Integer.parseInt(dbr.get("origine").toString());
//					printError("gestione_segnalazione: Origine del caso "+id_caso+" =["+origine+"]");
//				
//					//  aggiorno solo se il caso nel frattempo non � diventato UVM, altrimenti partono comunicazioni di EVENTI non previste 
//					if(origine == GestCasi.CASO_SAN) { // 12/08/10 m ---		
//						ISASRecord new_segnalazione = gestore_segnalazioni.update(dbc, h);
//						
//						if(new_segnalazione != null)
//						{
//							Enumeration en = new_segnalazione.getHashtable().keys();
//							while(en.hasMoreElements())
//							{
//								String chiave = en.nextElement().toString();
//								dbr.put(chiave, new_segnalazione.get(chiave));
//							}
//						}
//						
//						// 29/03/12: aggiunto cntrl su esistenza rec, dato che il CASO e la SEGNALAZIONE potrebbero essere stati inseriti da Sins_PUA 
//						//	(e quindi necessita update) , ma la PRESACARICO potrebbe dover essere in insert.
//						if (!esistePresaCar(dbc, h)) {
//							if(h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
//								h.put("dt_presa_carico", h.get("skfpg_data_apertura"));
//
//							ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
//							if(rec_pc != null)
//							{
//								gestore_casi.presaCaricoCaso(dbc, h);
//							
//								Enumeration en1 = rec_pc.getHashtable().keys();
//								while(en1.hasMoreElements())
//								{
//									String chiave = en1.nextElement().toString();
//									dbr.put(chiave, rec_pc.get(chiave));
//								}
//							
//								dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
//							}
//						} else {
//							ISASRecord update_presacarico = gestore_presacarico.update(dbc, h);
//							if(update_presacarico != null)
//							{
//								Enumeration en = update_presacarico.getHashtable().keys();
//								while(en.hasMoreElements())
//								{
//									String chiave = en.nextElement().toString();
//									dbr.put(chiave, update_presacarico.get(chiave));
//								}
//								
//								dbr.put("cod_usl", dbr.get("reg_ero").toString() + dbr.get("asl_ero").toString());
//							}
//						}
//					}
//				}
//				
//				return id_caso;
//			} 
//			catch (CariException e) // 17/11/09
//			{	
//				LOG.error("SkFpgEJB gestione_segnalazione, update -- " + e);
//				throw e;
//			} 
//			catch (Exception e) 
//			{
//				LOG.error("SkFpgEJB gestione_segnalazione, update() -- " + e);  
//				return id_caso;
//			}
//		}
//		else return id_caso;
//	}
//	
//	// 25/05/09 Elisa Croci
//	private void prendi_presacarico(ISASConnection dbc, int caso,ISASRecord dbr)
//	{
//		try 
//		{
//			if(caso != -1) 
//			{
//				Hashtable h = new Hashtable();
//				h.put("n_cartella", dbr.get("n_cartella"));
//				h.put("pr_data", dbr.get("pr_data"));
//				h.put("id_caso", new Integer(caso));
//				h.put("ubicazione", dbr.get("ubicazione"));
//				
//				ISASRecord res = gestore_presacarico.queryKey(dbc, h);
//	
//				if(res != null)
//				{
//					Enumeration e = res.getHashtable().keys();
//					while(e.hasMoreElements())
//					{
//						String chiave = e.nextElement().toString();
//						dbr.put(chiave, res.get(chiave));
//					}
//					
//					dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());
//				}
//			}
//		} 
//		catch (ISASMisuseException e1) 
//		{
//			LOG.error("SkFpgEJB prendi_presacarico, ERRORE REPERIMENTO CHIAVE! -- " + e1);
//		}
//		catch (Exception e) 
//		{
//			LOG.error("SkFpgEJB prendi_presacarico, fallimento! -- " + e);
//		}
//	}
//
//	// 20/05/09 Elisa Croci
//	// prendo la segnalazione relativa al caso a cui il contatto deve fare riferimento
//	private boolean prendi_segnalazione(ISASConnection dbc, int caso,ISASRecord dbr)
//	{
//		try 
//		{
//			/* prendo la segnalazione solo se il caso esiste e se sono in un contesto in cui si
//		 		gestiscono le segnalazioni
//		 	*/
//			if(gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN) && caso != -1)
//			{
//				Hashtable h = new Hashtable();
//				h.put("n_cartella", dbr.get("n_cartella"));
//				h.put("pr_data", dbr.get("pr_data"));
//				h.put("id_caso", new Integer(caso));
//				ISASRecord res = gestore_segnalazioni.queryKey(dbc, h);
//	
//				if(res != null)
//				{
//					Enumeration e = res.getHashtable().keys();
//					while(e.hasMoreElements())
//					{
//						String chiave = e.nextElement().toString();
//						dbr.put(chiave, res.get(chiave));
//					}
//				}
//				
//				return true;
//			}
//			else return false;
//		} 
//		catch (ISASMisuseException e1) 
//		{
//			LOG.error("SkFpgEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
//			return false;
//		}
//		catch (Exception e) 
//		{
//			LOG.error("SkFpgEJB prendi_segnalazione, fallimento! -- " + e);
//			return false;
//		}
//	}
//
//	// 20/05/09 Elisa Croci
//	// dato un contatto, prendo il caso attivo se esiste altrimenti quello chiuso piu' recente!
//	private int prendi_dati_caso(ISASConnection dbc, ISASRecord dbr)
//	{
//		Hashtable h = new Hashtable();
//		
//		try 
//		{
//			h.put("n_cartella", dbr.get("n_cartella"));
//			h.put("pr_data",dbr.get("pr_data"));
//			
//			LOG.error("SkFpgEJB -- prendi dati caso: " + h.toString());
//			
//			ISASRecord rec = gestore_casi.getCasoRif(dbc, h);
//			if(rec != null)
//			{
//				Enumeration e = rec.getHashtable().keys();
//				while(e.hasMoreElements())
//				{
//					String chiave = e.nextElement().toString();
//					dbr.put(chiave, rec.get(chiave));
//				}
//
//				int caso = Integer.parseInt(dbr.get("id_caso").toString());
//				return caso;
//			}
//			else return -1;
//		} 
//		catch(ISASMisuseException e)
//		{
//			LOG.error("SkFpgEJB prendi_dati_caso, manca chiave primaria! -- " + e);
//			return -1;
//		}
//		catch (Exception e) 
//		{
//			LOG.error("SkFpgEJB prendi_dati_caso, fallimento! -- " + e);
//			return -1;
//		}
//	}
//
//	private int prendi_dati_casoOrigine(ISASConnection dbc, ISASRecord dbr)
//	{
//		Hashtable h = new Hashtable();
//		
//		try 
//		{
//			h.put("n_cartella", dbr.get("n_cartella"));
//			h.put("pr_data",dbr.get("pr_data"));
//			h.put("origine",dbr.get("origine"));
//			
//			LOG.error("SkFpgEJB -- prendi dati caso x origine: " + h.toString());
//			
//			ISASRecord rec = gestore_casi.getCasoRifOrigine(dbc, h);
//			if(rec != null)
//			{
//				Enumeration e = rec.getHashtable().keys();
//				while(e.hasMoreElements())
//				{
//					String chiave = e.nextElement().toString();
//					dbr.put(chiave, rec.get(chiave));
//				}
//
//				int caso = Integer.parseInt(dbr.get("id_caso").toString());
//				return caso;
//			}
//			else return -1;
//		} 
//		catch(ISASMisuseException e)
//		{
//			LOG.error("SkFpgEJB prendi_dati_caso, manca chiave primaria! -- " + e);
//			return -1;
//		}
//		catch (Exception e) 
//		{
//			LOG.error("SkFpgEJB prendi_dati_caso, fallimento! -- " + e);
//			return -1;
//		}
//	}	
	
	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String myselect="SELECT * FROM skfpg WHERE n_cartella="+
			(String)h.get("n_cartella")+
			" AND skfpg_tipo_operatore = '"+h.get("skfpg_tipo_operatore")+"'"+
			" ORDER BY n_contatto,skfpg_data_apertura DESC ";
			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ", e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){LOG.error(e1);}
			}
		}

	}

	//gb 02/07/07: caricamento della grid della frame 'JFrameGridSkInf' *******
	public Vector query_loadGridSkInf(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;

		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String)h.get("pr_data");// 26/10/06
		String skfpg_tipo_operatore = (String) h.get("skfpg_tipo_operatore");
		
		Vector vdbr = new Vector();

		// 14/04/08: seleziona i contatti chiusi oppure quelli aperti (eccetto il pi� recente)
		boolean contAperti = false;

		try{
			// 26/10/06 ---
			if (strDtSkVal == null){
				printError("\nSkFpgEJB -->> query_loadGridSkInf: dataSkVal NULLA!!");
				done = true;
				return vdbr;
			}
			// 26/10/06 ---

			// Connessione al database
			dbc=super.logIn(mylogin);
				
			// 14/04/08 -------------------------
			contAperti = ((h.get("contAperti") != null) && (((String)h.get("contAperti")).trim().equals("S")));
			String critDtChius = " AND s.skfpg_data_uscita IS" + (contAperti?"":" NOT") + " NULL";
			if (contAperti)
				critDtChius += " AND s.skfpg_data_apertura NOT IN (SELECT MAX(a.skfpg_data_apertura) FROM skfpg a" +
				" WHERE a.n_cartella = s.n_cartella" +
				" AND a.skfpg_tipo_operatore = '"+skfpg_tipo_operatore+"'"+
				" AND a.skfpg_data_uscita IS NULL)";
			// 14/04/08 -------------------------

			// Compongo la SELECT
			String myselect = "SELECT s.*" +
			" FROM skfpg s" +
//			" progetto_cont pc" + // 26/10/06
			" WHERE s.n_cartella = " + strNAssistito +
			" AND s.skfpg_tipo_operatore = '"+skfpg_tipo_operatore+"'"+
			// 14/04/08	                " AND s.skfpg_data_uscita IS NOT NULL" +
			critDtChius + // 14/04/08
			// 26/10/06 : x estrarre solo quelli collegati ad una scheda valutaz
//			" AND pc.prc_tipo_op = '"+skfpg_tipo_operatore+"'" +
//			" AND pc.n_cartella = s.n_cartella" +
//			" AND pc.pr_data = " + formatDate(dbc, strDtSkVal) +
//			" AND pc.prc_n_contatto = s.n_contatto" +
			// 26/10/06 --------------------------------------------------------
			" ORDER BY s.skfpg_data_apertura, s.skfpg_data_uscita";
			
			ISASCursor dbcur=dbc.startCursor(myselect);
			vdbr=dbcur.getAllRecord();
			
			// Decodifica dei Cognomi e Nomi degli operatori e della descrizione
			// del grado di importanza in tutti gli ISASRecord del Vector
			decodificaQueryInfo(dbc, vdbr);
			
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ this.getClass().getName()+": "+Thread.currentThread().getStackTrace()[1].getMethodName()+ ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(this.getClass().getName()+": "+Thread.currentThread().getStackTrace()[1].getMethodName(), dbc);
		}
	}
	//gb 02/07/07: fine *******

	//gb 02/07/07: decodifiche dei record trovati nel metodo 'query_loadGridSkInf' *******
	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr) throws Exception
	{
		for (int i=0; i<vdbr.size(); i++)
		{
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			decodificaQueryOperatore(mydbc, dbr, "skfpg_operatore", "operatore_apertura");
			decodificaQueryOperatore(mydbc, dbr, "skfpg_referente", "operatore_referente");
//			dbr.put("skfpg_tipocura_desc", (String)util.getDecode(mydbc, "tab_voci", "tab_cod", "tab_val", "SAOADI", (String)dbr.get("skfpg_tipocura"), "tab_descrizione"));
		}
	}
	//gb 02/07/07: fine *******

	//gb 02/07/07 *******
	private void decodificaQueryOperatore(ISASConnection mydbc, ISASRecord dbr,
			String dbFldNameCod, String dbName) throws Exception
			{
		String strCodOperatore = (String) dbr.get(dbFldNameCod);

		String strCognome = "";
		String strNome = "";

		if (strCodOperatore == null)
		{
			dbr.put(dbName, "");
			return;
		}
		String selS = "SELECT cognome, nome FROM operatori" +
		" WHERE codice = '" + strCodOperatore + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null)
		{
			if (rec.get("cognome") != null)
				strCognome = (String)rec.get("cognome");
			if (rec.get("nome") != null)
				strNome = (String)rec.get("nome");
		}
		dbr.put(dbName, strCognome + " " + strNome);
			}
	//gb 02/07/07: fine *******

//	public Vector query_intamb(myLogin mylogin,Hashtable h) throws Exception 
//	{
//		ISASConnection dbc=null;
//		String methodName = "query_intamb";
//		try{
//			dbc=super.logIn(mylogin);
//			
///** 26/03/13: spostato in metodo apposito
//			String myselect="Select * from skintamb where "+
//			" n_cartella="+(String)h.get("n_cartella")+
//			" and n_contatto="+(String)h.get("n_contatto")+
//			//" and skfpg_data_apertura="+formatDate(dbc,(String)h.get("skfpg_data_apertura"))+
//			" ORDER BY ska_prog DESC ";
//			ISASCursor dbcur=dbc.startCursor(myselect);
//			Vector vdbr=dbcur.getAllRecord();
//			for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); ){
//				ISASRecord dbrdes=(ISASRecord)senum.nextElement();
//				if (dbrdes.get("ska_prestaz")!=null && !(dbrdes.get("ska_prestaz")).equals("")){
//					dbrdes.put("desc_prest", decodifica("prestaz","prest_cod",
//							dbrdes.get("ska_prestaz"),"prest_des",dbc));
//				}else dbrdes.put("desc_prest","");
//				if(dbrdes.get("ska_freq")!=null && !(dbrdes.get("ska_freq")).equals(""))
//					dbrdes.put("desc_freq", getFrequenza(dbrdes));
//				else dbrdes.put("desc_freq"," ");
//			}
//			dbcur.close();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//
//**/			
//			// 26/03/13
//			Vector vdbr = leggiSkiIntAmb(dbc, h);
//			
//			return vdbr;
//		}catch(Exception e){
//			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
//		}finally {
//			logout_nothrow(methodName, dbc);
//		}
//	}

	// 26/03/13
//	private Vector leggiSkiIntAmb(ISASConnection dbc, Hashtable h) throws Exception
//	{
//		ISASCursor dbcur = null;
//		try {
//			String myselect="Select * from skintamb where "+
//				" n_cartella="+h.get("n_cartella")+
//				" and n_contatto="+h.get("n_contatto")+
//				//" and skfpg_data_apertura="+formatDate(dbc,(String)h.get("skfpg_data_apertura"))+
//				" ORDER BY ska_prog DESC ";
//			dbcur=dbc.startCursor(myselect);
//			Vector vdbr=dbcur.getAllRecord();
//			for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); ){
//				ISASRecord dbrdes=(ISASRecord)senum.nextElement();
//				if (dbrdes.get("ska_prestaz")!=null && !(dbrdes.get("ska_prestaz")).equals("")){
//					dbrdes.put("desc_prest", decodifica("prestaz","prest_cod",
//							dbrdes.get("ska_prestaz"),"prest_des",dbc));
//				}else dbrdes.put("desc_prest","");
//				if(dbrdes.get("ska_freq")!=null && !(dbrdes.get("ska_freq")).equals(""))
//					dbrdes.put("desc_freq", getFrequenza(dbrdes));
//				else dbrdes.put("desc_freq"," ");
//			}
//			dbcur.close();
//			
//			return vdbr;
//		} finally {
//			close_dbcur_nothrow("leggiSkiIntAmb", dbcur);
//		}
//	}
	
//	private String getFrequenza(ISASRecord dbr) {
//		try {
//			switch (new Integer((String)dbr.get("ska_freq")).intValue()) {
//			case 1: return "GIORNALIERA";
//			case 2: return "BIGIORNALIERA";
//			case 3: return "SETTIMANALE";
//			case 4: return "BISETTIMANALE";
//			case 5: return "TRISETTIMANALE";
//			case 6: return "QUINDICINALE";
//			case 7: return "MENSILE";
//			case 8: return "SU CHIAMATA";
//			default : return "";
//			}
//		} catch(Exception e) {
//			return "";
//		}
//	}

	public ISASRecord query_salvataggio(myLogin mylogin,Hashtable h)
	throws SQLException {
		boolean done=false;
		String n_cartella=null;
		String n_contatto=null;
		String skfpg_tipo_operatore=null;
		String data_apertura=null;
		ISASConnection dbc=null;
		try {
			n_cartella=(String)h.get("n_cartella");
			n_contatto=(String)h.get("n_contatto");
			skfpg_tipo_operatore=(String)h.get("skfpg_tipo_operatore");
			data_apertura=(String)h.get("skfpg_data_apertura");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkFpgEJB queryKey: Errore: manca la chiave primaria", e);
		}
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select n_cartella,n_contatto,skfpg_tipo_operatore,skfpg_data_apertura from skfpg where "+
			" n_cartella="+n_cartella+
			" and n_contatto="+n_contatto+
			" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'"+
			" and skfpg_data_apertura="+formatDate(dbc,data_apertura);
			printError("select query_salvataggio su skfpg==="+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			throw new SQLException("Errore eseguendo una query_salvataggio()  ", e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
	}
//	//Serve per caricare la griglia degli ausili della protesica
//	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
//		
//		boolean done=false;
//		ISASConnection dbc=null;
//		try{
//			printError("SkFpgEJB -- QueryPaginate()"+h.get("tipo"));
//			ServerUtility su=new ServerUtility();
//			dbc=super.logIn(mylogin);
//			String myselect="SELECT m.mde_numinv,p.tau_des,m.mde_datcons "+
//			"FROM p_tipo_ausili p, p_movdet m,p_movtes s WHERE ";
//			String sel="";
//			sel=su.addWhere(sel,su.REL_AND,"s.mte_cartella",su.OP_EQ_NUM,(String)h.get("n_cartella"));
//			if(!((String)h.get("tipo")).equals("T"))
//				sel=su.addWhere(sel,su.REL_AND,"s.mte_tipocons",su.OP_EQ_STR,(String)h.get("tipo"));
//
//			sel+=" AND s.mte_anno=m.mde_anno AND s.mte_numero=m.mde_numero"+
//			" AND m.mde_tipo_ausilio=p.tau_cod AND"+
//			" s.mte_tipomov='S' ";
//			myselect=myselect+sel;
//
//			myselect=myselect+" ORDER BY m.mde_numinv";
//			printError("queryPaginate SkInf: "+myselect);
//			ISASCursor dbcur=dbc.startCursor(myselect);
//			int start = Integer.parseInt((String)h.get("start"));
//			int stop = Integer.parseInt((String)h.get("stop"));
//			Vector vdbr = dbcur.paginate(start, stop);
//			dbcur.close();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return vdbr;
//		}catch(Exception e){
//			e.printStackTrace();
//			throw new SQLException("Errore eseguendo una query()  ");
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e1){LOG.error(e1);}
//			}
//		}
//	}

	//gb 30/04/07 *****************************************************************
	// 13/09/07 m.: aggiunto crit su contatto diverso da quello in oggetto.
	private boolean dtApeContLEMaxDtContChius(ISASConnection dbc, Hashtable h) throws Exception
	{
		String strNCartella =  h.get("n_cartella").toString();
		String skfpg_tipo_operatore = h.get("skfpg_tipo_operatore").toString();
		String strDataApeContatto = h.get("skfpg_data_apertura").toString();
// 12/02/13	String strNContatto = (String) h.get("n_contatto"); // 13/09/07 m.
		// 12/03/13
		String strNContatto = strNContatto =ISASUtil.getValoreStringa(h,"n_contatto");;

		String mySel = "SELECT skfpg_data_uscita" +
		" FROM skfpg" +
		" WHERE n_cartella = " + strNCartella +
		" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'"+
		(ISASUtil.valida(strNContatto)?" AND n_contatto <> "+strNContatto:"") + // 13/09/07 m.
		" AND skfpg_data_uscita >= " + formatDate(dbc, strDataApeContatto) +
		" AND skfpg_data_uscita IS NOT NULL";

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}
	// ****************************************************************************

	//gb 12/09/07 *****************************************************************
	//	Restituisce un messaggio appropriato se si verifica che le date di apertura
	//	e chiusura della scheda contatto infermieri non sono congrue con le rispettive
	//	date del piano assistenziale (tabella 'piano_assit').
	//	Se invece sono congrue il metodo ritorna "" (stringa vuota).
	//	
	private String checkDateContEDatePianoAssist(ISASConnection dbc, Hashtable h) throws Exception
	{
		ISASCursor dbcur = null;
		String strNCartella = h.get("n_cartella").toString();
		String strNContatto =  h.get("n_contatto").toString();
		String skfpg_tipo_operatore = h.get("skfpg_tipo_operatore").toString();
		String strDataApeContatto =  h.get("skfpg_data_apertura").toString();
		String strDataChiuContatto = null;
		String msg = "";

		String mySel = "SELECT *" +
		" FROM piano_assist" +
		" WHERE n_cartella = " + strNCartella +
		" AND n_progetto = " + strNContatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0" +
		" AND pa_tipo_oper = '"+skfpg_tipo_operatore+"'"+
		" AND pa_data < " + formatDate(dbc, strDataApeContatto);
		printError("SkFpgEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
		dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			msg = "Attenzione esistono Piani Assistenziali la cui data di apertura � antecedente della data apertura della scheda contatto infermieri.";
		else
		{
			dbcur = null;
			if (h.get("skfpg_data_uscita") != null)
			{
				strDataChiuContatto = "" + h.get("skfpg_data_uscita");

				if (strDataChiuContatto!=null && !(strDataChiuContatto.equals("")))
				{
					mySel =	 "SELECT *" +
					" FROM piano_assist" +
					" WHERE n_cartella = " + strNCartella +
					" AND n_progetto = " + strNContatto +
					" AND cod_obbiettivo = '00000000'" +
					" AND n_intervento = 0" +
					" AND pa_tipo_oper = '"+skfpg_tipo_operatore+"'"+
					" AND pa_data_chiusura > " + formatDate(dbc, strDataChiuContatto) +
					" AND pa_data_chiusura IS NOT NULL ";
					printError("SkFpgEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
					dbcur = dbc.startCursor(mySel);

					if ((dbcur != null) && (dbcur.getDimension() >0))
						msg = "Attenzione esistono Piani Assistenziali la cui data di chiusura � successiva alla data chiusura della scheda contatto infermieri.";
				}
			}
		}

		if (dbcur != null)
			dbcur.close();
		return msg;
	}
	// ****************************************************************************

//	//gb 27/08/07 *****************************************************************
//	private boolean dtApeProgettoLTDtApeCartella(ISASConnection dbc, String strNAssistito, String strDtSkVal) throws Exception
//	{
//		String mySel = "SELECT *" +
//		" FROM cartella" +
//		" WHERE n_cartella = " + strNAssistito +
//		" AND data_apertura > " + formatDate(dbc, strDtSkVal);
//
//		ISASRecord rec = dbc.readRecord(mySel);
//		if (rec == null)
//			return false; // Ammissibile
//		else
//			return true;
//	}
//	// ****************************************************************************

	//gb 30/04/07 *****************************************************************
//	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, String strNAssistito, String strDtSkVal)
//	throws Exception
//	{
//		String dt = strDtSkVal;
//		//gb 27/08/07  dt = dt.substring(0,2) + dt.substring(3,5) + dt.substring(6,10);
//		dt = dt.substring(8,10) + dt.substring(5,7) + dt.substring(0,4); //gb 27/08/07
//		DataWI dataWIApertura = new DataWI(dt);
//
//		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" +
//		" FROM progetto" +
//		" WHERE n_cartella = " + strNAssistito +
//		" AND pr_data_chiusura IS NOT NULL";
//
//		ISASRecord rec = dbc.readRecord(mySel);
//		if (rec == null)
//			return false; // Ammissibile
//
//		if ((java.sql.Date)rec.get("max_data_chius") == null)
//			return false; // Ammissibile
//
//		dt = ((java.sql.Date)rec.get("max_data_chius")).toString();
//		if (dt.equals(""))
//			return false; // Ammissibile
//
//		dt=dt.substring(0,4) + dt.substring(5,7) + dt.substring(8,10);
//		String max_data_chiusura = dt;
//		int rit = dataWIApertura.confrontaConDt(max_data_chiusura);
//		// Codici ritornati da confrontaConDt:
//		// se data_apertura � maggiore di data_chiusura restituisce 1
//		// se data_apertura � minore di data_chiusura restituisce 2
//		// se data_apertura � = di data_chiusura restituisce 0
//		// se da errore -1
//		if ((rit == 2) || (rit == 0))
//			return true; // Non ammissibile
//		else if (rit < 0)
//		{
//			throw new Exception("SocAssProgettoEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
//			// Si � verificato un errore nel metodo di confronto delle 2 date.
//		}
//		else // (rit == 1)
//			return false; // Ammissibile
//	}
//	// ****************************************************************************
//
//	// 30/04/07: inserimento su tabella PROGETTO di un record con i soli valori della chiave
//	private void scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception
//	{
//		ISASRecord dbrPrg = mydbc.newRecord("progetto");
//		dbrPrg.put("n_cartella", numCart);
//		dbrPrg.put("pr_data", dtSkVal);
//		mydbc.writeRecord(dbrPrg);
//		printError("\n SkFpgEJB -->> scriviProgetto: Inserito record su tabella PROGETTO");
//	}

//	// 30/04/07: inserimento su tabella PROGETTO_CONT
//	private void scriviProgettoCont(ISASConnection mydbc, String numCart, String dtSkVal, String tpOper, String numCont) throws Exception
//	{
//		ISASRecord dbrPrgCont = mydbc.newRecord("progetto_cont");
//		dbrPrgCont.put("n_cartella", numCart);
//		dbrPrgCont.put("pr_data", dtSkVal);
//		dbrPrgCont.put("prc_tipo_op", tpOper);
//		dbrPrgCont.put("prc_n_contatto", new Integer(numCont));
//		mydbc.writeRecord(dbrPrgCont);
//		printError("\n SkFpgEJB -->> scriviProgettoCont: Inserito record su tabella PROGETTO_CONT");
//	}

	//gb 07/05/07: ricava il nuovo progressivo per le operazioni di 'insert'.
	private int getProgressivo(ISASConnection mydbc, String strNAssistito,String skfpg_tipo_operatore) throws Exception
	{
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String myselect="SELECT MAX(n_contatto) max_n_contatto" +
		" FROM skfpg" +
		" WHERE n_cartella = " + strNAssistito+
		" AND skfpg_tipo_operatore = '"+skfpg_tipo_operatore+"'";
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null)
		{
			intProgressivo = u.getIntField(dbr, "max_n_contatto");
		}
		intProgressivo++;
		return intProgressivo;
	}
	//gb 07/05/07: fine *******

	public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException{
		printError("SkFpgEJB: insert HASH -- " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;// 07/12/06 m.
		
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();

			ISASRecord dbr = insertTransactional(dbc, h);
			
			dbc.commitTransaction();
			
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(CariException ce){
			ce.setISASRecord(null);
			try{
				LOG.error("SkFpgEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1, e1);
			}
			throw ce;
		}catch(DBRecordChangedException e){
			LOG.error("SkFpgEJB.insert(): "+e);
			try{   
				dbc.rollbackTransaction(); 
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			LOG.error("SkFpgEJB.insert(): "+e);
			try{    
				dbc.rollbackTransaction(); 
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}
		catch(Exception e1){
			LOG.error("SkFpgEJB.insert(): "+e1);
			try{    
				dbc.rollbackTransaction();	
			}catch(Exception ex){
				throw new SQLException("Errore eseguendo una rollback() - "+  ex, ex);
			}
//			e1.printStackTrace();
			throw new SQLException("Errore eseguendo una insert() - "+  e1, e1);
		} finally{
			if(!done){
				try{
					if (dbcur != null) 
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){   
					LOG.error("SkFpgEJB: Errore eseguendo una insert() --> " + e2);   
				}
			}
		}
	}

	/**
	 * Permette di eseguire la insert rimanendo all'interno di una transazione
	 * 
	 * @param dbc	la connessione sulla quale effettuare la insert
	 * @param h		l'hashtabel con i parametri di insert
	 * @return		l'ISASRecord inserito
	 */
	public ISASRecord insertTransactional(ISASConnection dbc, Hashtable h) throws SQLException,
			Exception, ISASMisuseException {
		String n_cartella = null;
		String n_contatto = null;
		String skfpg_tipo_operatore = null;
		String data_apertura = null;
		//String dimissOsp = "";
		
		String strDtSkVal = (String)h.get("pr_data");//gb 30/04/07
		
		// 14/04/08: se configurato, permette la presenza di più contatti aperti contemporaneamente
		boolean multiCont = false;
		
		try{
			n_cartella= h.get("n_cartella").toString();
			n_contatto= h.get("n_contatto").toString();
			skfpg_tipo_operatore = h.get("skfpg_tipo_operatore").toString();
			data_apertura = h.get("skfpg_data_apertura").toString();
			//dimissOsp=(String)h.get("dimiss");
		}catch (Exception e){
//				LOG.error("SkFpgEJB.insert(): "+e);
			throw new SQLException("SkInf insert: Errore: manca la chiave primaria", e);
		}

		// 10/01/12 m ---
		ISASRecord dbr = insertNew(dbc, h, n_cartella,skfpg_tipo_operatore,strDtSkVal);
//			gestSegnalNew(dbc, dbr, h);
		if (h.get("tempo_t") != null)
			dbr.put("tempo_t", h.get("tempo_t").toString());
		// 10/01/12 m ---			
		// Simone 25/11/14 Aggiornamento id_skso su rm_diario
		String idSkso = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_ID_SKSO);
//			if (h.get("id_skso")!=null&&!h.get("id_skso").toString().trim().equals("")){
		if(ISASUtil.valida(idSkso)){
			Hashtable h_rm_diario = (Hashtable)h.clone();
			h_rm_diario.put("tipo_operatore", h.get("skfpg_tipo_operatore"));
			h_rm_diario.put("id_skso", h.get("id_skso"));
			try {
				Boolean id_skso_updated = RMDiarioEJB.updateIdSkso(dbc, h_rm_diario);
				LOG.debug("Esito aggiornamento id_skso su diario = "+id_skso_updated.booleanValue());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Hashtable dati = (Hashtable)h.clone();
			dati.put(CostantiSinssntW.CTS_OP_INSERIRE_GENERICO, Costanti.CTS_SI);
			dati.put(CostantiSinssntW.TIPO_OPERATORE, ISASUtil.getValoreStringa(h,  CostantiSinssntW.CTS_SKFPG_TIPO_OPERATORE));
			dati.put(CostantiSinssntW.CTS_COD_OP_CORRENTE, dbc.getKuser());
			dati.put("SOVRASCRIVI", "SI");
			dati.put(CostantiSinssntW.DT_PRESA_CARICO, ISASUtil.getValoreStringa(h, CostantiSinssntW.SKFPG_DATA_APERTURA));
			
			try {
				RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
				rmSkSOOpCoinvoltiEJB.inserisciOperatoriFigure(dati, dbc, n_cartella, idSkso);
				LOG.debug("Operatore inserito ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return dbr;
	}

	// 10/01/12 m
	public ISASRecord insertNew(ISASConnection dbc, Hashtable h, String n_cartella, String skfpg_tipo_operatore, String strDtSkVal) throws Exception
	{
		printError("SkFpgEJB: insert HASH -- " + h.toString());

		String dimissOsp = (String)h.get("dimiss");
		String data_apertura=(String)h.get("skfpg_data_apertura");

		// 14/04/08: se configurato, permette la presenza di pi� contatti aperti contemporaneamente
		boolean multiCont = false;

		// 14/04/08: si esegue il ctrl solo se configurato per 1 solo contatto aperto  ---
		String abilMultiCont = (String)leggiConf(dbc, "ABIL_NEWCONT_INF");
		// 15/07/08 -----
		String zonaOper = getZonaOper(dbc, (String)h.get("skfpg_operatore"));
		abilMultiCont = getValxZona(abilMultiCont, zonaOper);
		// 15/07/08 -----
		multiCont = ((abilMultiCont != null) && (abilMultiCont.trim().equals("SI")));
		if (!multiCont) 
		{ 
			// 14/04/08 ---
			//gb 30/04/07 
			if(dtApeContLEMaxDtContChius(dbc, h))
			{
				String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
				throw new CariException(msg, -2);
			}
		}

		//gb 07/05/07: si ottiene il nuovo progressivo (non si usa pi� CONTSAN).
		int intProgressivo = getProgressivo(dbc, n_cartella, skfpg_tipo_operatore);
		Integer iProgressivo = new Integer(intProgressivo);
		String n_contatto = iProgressivo.toString();
		// ************************************

		// 14/05/10
		String proven = (String)h.get("provenienza");
						
		ISASRecord dbr=dbc.newRecord("skfpg");
		Enumeration n=h.keys();
		while(n.hasMoreElements())
		{
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}

		// Si setta il campo 'n_contatto' col nuovo progressivo
		dbr.put("n_contatto",iProgressivo);
		if ((gestore_casi.isUbicazRegMarche(dbc, h)).booleanValue()) {	
			if ((h.get("skfpg_descr_contatto") == null) || ("".equals((String)h.get("skfpg_descr_contatto"))))
					dbr.put("skfpg_descr_contatto", "Scheda n. "+iProgressivo);
		}
		// 18/03/11: solo per VENETO: se descr contatto NON valorizzata si mette un avlore default
		if ((gestore_casi.isUbicazRegVeneto(dbc, h)).booleanValue()) {	
			if ((h.get("skfpg_descr_contatto") == null) || ("".equals((String)h.get("skfpg_descr_contatto"))))
				dbr.put("skfpg_descr_contatto", "CONTATTO INFERMIERISTICO N."+iProgressivo);
		}
						
		// 09/10/12
		if ((h.get("skfpg_modalita") == null) || ("".equals((String)h.get("skfpg_modalita"))))
			dbr.put("skfpg_modalita", "1");
			
			
		dbc.writeRecord(dbr);
		
		ISASUtil.checkUnicoContattoAperto(dbc, h, n_contatto, "skfpg", "skfpg_data_uscita", Labels.getLabel("contatti.msg.contattoUnicoApertoViolato"));


//		// 30/04/07: scrittura su PROGETTO_CONT ed, eventualmente, PROGETTO -----
//		if ((strDtSkVal == null) || ((strDtSkVal != null) && (strDtSkVal.trim().equals(""))))
//		{
//			strDtSkVal = (String)h.get("skfpg_data_apertura");
//			//gb 30/04/07
//			// Mettere controllo che data_ape sk_valutaz. fittizia sia >= data chiusura di ultima sk_valutaz. chiusa pre-esistente.
//			if (dtApeMinoreMaxDtChius(dbc, n_cartella, strDtSkVal))
//			{
//				String msg = "Attenzione: Data apertura antecedente a data chiusura di ultima Scheda valutazione chiusa!";
//				throw new CariException(msg, -2);
//			}
//		
//			//gb 27/07/08: Controllo che la data di apertura del contatto sia >= data_apetura della tab. cartella.
//			if (dtApeProgettoLTDtApeCartella(dbc, n_cartella, strDtSkVal))
//			{
//				String msg = "Attenzione: Data apertura contatto e' antecedente alla data apertura dell'assistito!";
//				throw new CariException(msg, -2);
//			}
//			ISASRecord progetto = getProgetto(dbc, n_cartella, dbr.get("skfpg_data_apertura").toString());
//			if (progetto == null)
//			scriviProgetto(dbc, n_cartella, strDtSkVal);
//			else strDtSkVal = progetto.get("pr_data").toString();
//		}
			
//		scriviProgettoCont(dbc, n_cartella, strDtSkVal, "02", n_contatto);

		String myselect=
				" Select * " +
				" from skfpg" +
				" where n_cartella="+n_cartella+
				" and n_contatto="+n_contatto+
				" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'"+			
				" and skfpg_data_apertura="+formatDate(dbc,data_apertura);
			
		dbr=dbc.readRecord(myselect);

		if (dbr != null) {
			gestisciDecodifiche(dbc,dbr);
			// fine 05/12/06
//			dbr.put("skfpg_data_barthel",CaricaData(dbc, n_cartella, dbr.get("skfpg_data_apertura"), "sc_barthel", dbr.get("skfpg_data_uscita"),"skbt_data"));
			String descr_infer=(String)dbr.get("skfpg_descr_contatto");
			String data_chiusura="";
			
			if (dbr.get("skfpg_data_uscita")!=null && !((dbr.get("skfpg_data_uscita"))).equals(""))
				data_chiusura=((java.sql.Date)dbr.get("skfpg_data_uscita")).toString();

			String selref="SELECT * FROM skfpg_referente WHERE "+
						"n_cartella="+n_cartella+" AND "+
						"n_contatto="+n_contatto+" AND "+
						"skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
			//"skfpg_data_apertura="+formatDate(dbc,data_apertura)
			
			ISASRecord dbref=dbc.readRecord(selref);
			if(dbref==null)
			{
				if(dbr.get("skfpg_referente")!=null && !((String)dbr.get("skfpg_referente")).equals(""))
				{
					String infref=(String)dbr.get("skfpg_referente");
					String data_ref="";
				
					if (dbr.get("skfpg_referente_da")!=null)
						data_ref=((java.sql.Date)dbr.get("skfpg_referente_da")).toString();

					this.insertInfRef(dbc,infref,data_ref,data_apertura,n_cartella,n_contatto,skfpg_tipo_operatore);
				}
			}
	
		
			//gb 30/04/07: per rimandare indietro al client la data della scheda valutazione
			dbr.put("pr_data", strDtSkVal);//gb 30/04/07

			// 21/05/09 m. ------------------
			if(strDtSkVal!=null)
				h.put("pr_data", strDtSkVal);
			// lettura dtConclusione CASO precedente
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String)h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, strDtSkVal,
										"", dtChiusCasoPrec, "", tempoT, "02");
			// 21/05/09 m. ------------------
	

					
			// 15/06/09 Elisa Croci  ******************************************************
			if(h.containsKey("ubicazione"))
				dbr.put("ubicazione", h.get("ubicazione"));
			if(h.containsKey("update_segnalazione"))
				dbr.put("update_segnalazione", h.get("update_segnalazione"));
			// ****************************************************************************
			
			dbr.put("tempo_t", tempoT); // 21/05/09 m.
			
			printError("SkFpgEJB: insert -- DBR restituito === " + dbr.getHashtable().toString());
		}
					
		return dbr;
	}
	
//	public void gestSegnalNew(ISASConnection dbc, ISASRecord dbr, Hashtable h) throws Exception
//	{
//		String proven = (String)h.get("provenienza");	
//	
//		// 16/07/10: si comunicano solo i contatti DOMICILIARI
//		int idCaso = -1;
//		if ((dbr.get("skfpg_modalita") != null) && ("1".equals((String)dbr.get("skfpg_modalita")))) {
//			if (!"ELECASI".equals(proven)) { // 14/05/10: se non provengo da "ElencoCasi"
//				// 21/05/09 Elisa Croci
//				if (gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN))
//				{
//					idCaso = prendi_dati_caso(dbc, dbr);
//					int risu = gestione_segnalazione(dbc, dbr, h, "insert");
//					
//					printError("gestSegnalNew() -- Fatta gestione segnalazione per il caso: " + idCaso);						
//				} else { 
///*** 26/06/13 m: NON SERVE PIU	***			
//					// 08/10/10 m: solo x PIEMONTE
//					if(h.get("skfpg_dtsegnalazione") == null || h.get("skfpg_dtsegnalazione").equals(""))
//						h.put("dt_segnalazione", h.get("skfpg_data_apertura"));
//					else
//						h.put("dt_segnalazione", h.get("skfpg_dtsegnalazione"));
//				
//					if(h.get("skfpg_dtpresacarico") == null || h.get("skfpg_dtpresacarico").equals(""))
//						h.put("dt_presa_carico", h.get("dt_segnalazione"));
//					else
//						h.put("dt_presa_carico", h.get("skfpg_dtpresacarico"));
//				
//					// 25/03/13 ---
//					idCaso = prendi_dati_caso(dbc, dbr);
//					if (idCaso <= 0) // 25/03/13 ---
//						idCaso = (gestore_casi.apriCasoSan(dbc, h)).intValue();
//					
//					h.put("operZonaConf", (String)h.get("skfpg_operatore"));
//					if ((gestore_casi.isUbicazRegPiem(dbc, h)).booleanValue())							
//						aggRpPresaCar(dbc, (Hashtable)dbr.getHashtable());
//						
//					h.put("origine", ""+GestCasi.CASO_SAN);	
//*** 26/06/13 m: NON SERVE PIU ***/
//					// 26/06/13
//					dbr.put("origine", ""+GestCasi.CASO_SAN);
//					idCaso = prendi_dati_casoOrigine(dbc, dbr);	
//				}
//			}
//				
//			String data_chiusura="";
//			if (dbr.get("skfpg_data_uscita")!=null && !((dbr.get("skfpg_data_uscita"))).equals(""))
//				data_chiusura=((java.sql.Date)dbr.get("skfpg_data_uscita")).toString();				
//				
//			if (data_chiusura != null && !data_chiusura.equals(""))
//			{
//				printError("insert() -- Controllo contatto UNICO SANITARIO H == " + h.toString());
//							
//				boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true, true);
//				if (idCaso != -1 && unico)
//				{
//					printError("insert() -- Gestisco la chiusura del caso" );
//					// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
//					// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
//					// sociale associato!
//					int origine = -1;
//					if(dbr.get("origine") != null && !(dbr.get("origine").toString()).equals(""))
//						origine = Integer.parseInt(dbr.get("origine").toString());
//					else if(h.get("origine") != null && !(h.get("origine").toString()).equals(""))
//						origine = Integer.parseInt(h.get("origine").toString());
//						
//					// 26/06/13: verifico stato caso = ATTIVO
//					int statoCaso = -1;
//					if(dbr.get("stato") != null && !(dbr.get("stato").toString()).equals(""))
//						statoCaso = Integer.parseInt(dbr.get("stato").toString());
//						
//					if (origine != -1)
//					{
//						printError("insert() -- Origine del caso: " + origine + " - stato = " + statoCaso);
//						if ((origine == GestCasi.CASO_SAN)
//						&& ((statoCaso == GestCasi.STATO_ATTIVO) 
//							|| (((Boolean)gestore_casi.isUbicazRegTosc(dbc, h)).booleanValue())))
//						{
//							Hashtable hCaso = new Hashtable();
//							hCaso.put("n_cartella", h.get("n_cartella"));
//							hCaso.put("pr_data", h.get("pr_data"));
//							hCaso.put("id_caso", new Integer(idCaso));
//							hCaso.put("dt_conclusione", dbr.get("skfpg_data_uscita"));
//							String motChiu = (String)h.get("skfpg_dimissioni");
//							String motChiuFlux = getTabVociCodReg(dbc, "ICHIUS", motChiu);
//							hCaso.put("motivo", motChiuFlux);
//
//							hCaso.put("operZonaConf", (String)dbr.get("skfpg_operatore")); // 15/10/09
//							printError(" insert() -- Chiudi caso = HashCaso: " + hCaso.toString());
//							Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
//							printError("insert() -- Ritorno di ChiudiCaso == " + r);
//						}
//					}
//				}
//			}
//		} else 
//			LOG.error("--- SkFpgEJB.insert: Contatto NON DOMICILIARE quindi NON si comunicano SEGNALAZ e PRESACAR ---");	
//	}
	
	
	
//	private void AggiornaDimissione(ISASConnection dbc, String cartella, String dimissOsp, ISASRecord dbr)
//	throws SQLException 
//	{
//		try
//		{
//			//Devo aggiornare la tabella dimiss_osp
//			String mysel= "SELECT * FROM dimiss_osp WHERE progressivo="+dimissOsp+
//							" AND n_cartella = "+cartella;
//			
//			ISASRecord dbrDim = dbc.readRecord(mysel);
//			if(dbrDim!=null)
//			{				
//				dbrDim.put("data_carico", ""+dbr.get("skfpg_infermiere_da"));
//				
//				dbrDim.put("oper_carico", ""+dbr.get("skfpg_infermiere"));
//				
//				dbc.writeRecord(dbrDim);
//			}
//
//		}catch (Exception e){
//			e.printStackTrace();
//			throw new SQLException("SkInf=>AggiornaDimissione: Errore");
//		}
//	}
	public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException,
																	SQLException, CariException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr_ret = null;
				try
		{
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
				
			dbr_ret = update(dbc,dbr);
			
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr_ret;
		}
		//gb 30/04/07 **************
		catch(CariException ce){
			ce.setISASRecord(null);
			try{
				LOG.error("SkFpgEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo la rollback() - " +  ce);
			}
			throw ce;
			// *************************
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e1){
			e1.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception ex){
				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
			}
			throw new SQLException("Errore eseguendo una update() - "+  e1);
		}finally{
			if(!done){
				try{				
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}

	}
	/*gb 02/05/07 *******
        private void deleteContattoInProgCont(ISASConnection dbc, String cartella,
				String pr_data, String prc_tipo_op, String prc_n_contatto)
		throws Exception
	{
                String myselect = "SELECT * FROM progetto_cont" +
                                " WHERE n_cartella = " + cartella +
                                " AND pr_data = " +formatDate(dbc,pr_data)+
                                " AND prc_tipo_op = '" +prc_tipo_op+"'"+
                                " AND prc_n_contatto = " +prc_n_contatto;

                debugMessage("SkFpgEJB: deleteContattoInProgCont - myselect=[" + myselect + "]");
                ISASRecord recProg = dbc.readRecord(myselect);
                 if (recProg!=null){
                  dbc.deleteRecord(recProg);
		}
	}
gb 02/05/07: fine *******/

	//gb 02/05/07 *******
	private void deletePianoAssist(ISASConnection dbc, String cartella, String contatto,String skfpg_tipo_operatore)
	throws Exception
	{
		String myselect="SELECT * FROM piano_assist" +
		" WHERE pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0";
		printError("deletePianoAssist "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next())
		{
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_assist" +
			" WHERE pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
			" AND n_cartella = " + cartella +
			" AND n_progetto = " + contatto +
			" AND cod_obbiettivo = '00000000'" +
			" AND n_intervento = 0" +
			" AND pa_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("pa_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}
	//gb 02/05/07: fine *******

	//gb 02/05/07 *******
	private void deletePianoAccessi(ISASConnection dbc, String cartella, String contatto,String skfpg_tipo_operatore)
	throws Exception
	{
		String myselect="SELECT * FROM piano_accessi" +
		" WHERE pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0";
		printError("deletePianoAccessi " + myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next())
		{
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_accessi" +
			" WHERE pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
			" AND n_cartella = " + cartella +
			" AND n_progetto = " + contatto +
			" AND cod_obbiettivo = '00000000'" +
			" AND n_intervento = 0" +
			" AND pa_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("pa_data"))) +
			" AND pi_prog = " + (Integer)dbr.get("pi_prog");
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}
	//gb 02/05/07: fine *******

	//gb 02/05/07 *******
	private void deletePianoVerifiche(ISASConnection dbc, String cartella, String contatto,String skfpg_tipo_operatore)
	throws Exception
	{
		String myselect="SELECT * FROM piano_verifica" +
		" WHERE pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0";
		printError("deletePianoVerifiche "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next())
		{
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_verifica" +
			" WHERE pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
			" AND n_cartella = " + cartella +
			" AND n_progetto = " + contatto +
			" AND cod_obbiettivo = '00000000'" +
			" AND n_intervento = 0" +
			" AND pa_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("pa_data"))) +
			" AND ve_data = " + formatDate(dbc,(""+(java.sql.Date)dbr.get("ve_data")));
			ISASRecord dbr2 = dbc.readRecord(sel);
			dbc.deleteRecord(dbr2);
		}
		dbcur.close();
	}
	//gb 02/05/07: fine *******

	public Object deleteAll(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException,
	SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String cartella=dbr.get("n_cartella").toString();
			String contatto=dbr.get("n_contatto").toString();
			String skfpg_tipo_operatore=dbr.get("skfpg_tipo_operatore").toString();
			//gb 02/05/07        deleteLegameProgetto(dbc,cartella,contatto,"02");
			//gb 02/05/07 Spostata dentro l'if pi� sotto
			//Date data=(Date)dbr.get("skfpg_data_apertura");
			String ris=VerificaInterv(dbc,dbr);
			if (ris.equals("N"))
			{
				dbc.deleteRecord(dbr);
				//gb 07/05/07          deleteContsan(dbc,data,cartella,contatto);
//				deleteLegameProgetto(dbc,cartella,contatto,"02"); //gb 02/05/07
				//VADO A CANCELLARE TUTTE LE SCHEDE ASSOCIATE AL CONTATTO
				//gb 02/05/07: tolte le tabelle 'skiprogass' e 'pianointerv' dall'array Vschede
				
				//TODO serratore definire quali sono le schede eventualmente collegate a skfpg
//				String[] Vschede={"skiprestaz","skiaccoglienza","skifamiglia","skiterapia","skinorton",
//						"skidolore","skirelaz","skicoscienza","skiumore","respiraz","skinutriz",
//						"skiescret","skidiagnosi","skimotric","skialtro","skialteraz","skiesami",
//						/*"skiprogass",*/"skiverifica","skfpg_referente","skintamb","skiausili_t",
//						"skiausili_d","skidecubito_t","skidecubito_d","skiulcere_t","skiulcere_d",
//						"skivasco_t","skivasco_d",/*"pianointerv",*/"skieventi"};
//				//gb 02/05/07: tolti i nomi campi 'skpa_data' e 'skpa_data' dall'array Vdate
//				//		 relativi alle tabelle 'skiprogass' e 'pianointerv'
//				String[] Vdate={"skp_data","ska_data","skf_data","skt_data","skno_data",
//						"skid_data","skr_dat","skc_data","sku_data","skr_data","skn_data",
//						"ske_data","ske_data","skd_data","skx_data","skal_data","skes_data",
//						/*"skpa_data",*/"skpa_data","skir_infermiere_da","ska_prog","skat_data",
//						"skat_data","skdt_data","skdt_data","skut_data","skut_data",
//						"skvt_data","skvt_data",/*"skpa_data",*/"skev_data"};
//				
//				for (int i=0;i<Vschede.length;i++)
//				{
//					deleteSchede(dbc, Vschede[i],cartella,contatto,Vdate[i]);
//				}
				deletePianoAssist(dbc, cartella, contatto,skfpg_tipo_operatore); //gb 02/05/07
				deletePianoAccessi(dbc, cartella, contatto,skfpg_tipo_operatore); //gb 02/05/07
				deletePianoVerifiche(dbc, cartella, contatto,skfpg_tipo_operatore); //gb 02/05/07
			}

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			if (ris.equals("N"))
				return new Integer(0); //Non esistono accessi (interventi): cancellazione eseguita
			else
				return new Integer(1); //Esistono accessi (interventi): cancellazione non eseguita
		}catch(DBRecordChangedException e){
			LOG.error("SkFpgEJB.delete1(): "+e);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			LOG.error("SkFpgEJB.delete2(): "+e);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e1){
			LOG.error("SkFpgEJB.delete3(): "+e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception ex){
				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
			}
			throw new SQLException("Errore eseguendo una delete() - ",  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
	}

	private String VerificaInterv(ISASConnection dbc, ISASRecord dbr)
	throws  SQLException {
		String ritorno="";
		try {
			String mysel = "SELECT * FROM interv WHERE "+
			" int_cartella =" + dbr.get("n_cartella") +
			" AND int_contatto =" + dbr.get("n_contatto")+
			" AND int_tipo_oper='"+dbr.get("skfpg_tipo_operatore")+"'";
			printError("Dentro VerificaInterv=>"+mysel);
			ISASRecord dbtxt = dbc.readRecord(mysel);
			if (dbtxt!=null)  ritorno="S";
			else              ritorno="N";
			return ritorno;
		} catch (Exception ex) {
			return ritorno="";
		}
	}
//	private void deleteLegameProgetto(ISASConnection dbc,String cartella,String contatto,String figprof)
//	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		ISASCursor dbcur=null;
//		try{
//			String myselect="SELECT * FROM progetto_cont WHERE "+
//			" n_cartella="+cartella+
//			" AND prc_n_contatto="+contatto+
//			" AND prc_tipo_op='"+figprof+"'";
//			debugMessage("deleteLegameProgetto=>"+myselect);
//			dbcur=dbc.startCursor(myselect);
//			while (dbcur.next()){
//				ISASRecord dbr = dbcur.getRecord();
//				String sel= "SELECT * FROM progetto_cont WHERE "+
//				" n_cartella="+cartella+
//				" AND prc_n_contatto="+contatto+
//				" AND prc_tipo_op='"+figprof+"'"+
//				" AND pr_data = "+formatDate(dbc,(""+(java.sql.Date)dbr.get("pr_data")));
//				ISASRecord dbr2 = dbc.readRecord(sel);
//				dbc.deleteRecord(dbr2);
//			}
//			dbcur.close();
//			done=true;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			throw new SQLException("Errore eseguendo una deleteLegameProgetto() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					if (dbcur != null)
//						dbcur.close();
//					//dbc.close();	19.06.08 rb
//					//super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

//	private void deleteSchede(ISASConnection dbc,String tabella,String cartella,
//			String contatto, String data)
//	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		try{
//			String myselect="SELECT * FROM "+tabella+" WHERE "+
//			" n_cartella="+cartella+
//			" AND n_contatto="+contatto;
//			printError("DeleteSchede "+myselect);
//			ISASCursor dbcur=dbc.startCursor(myselect);
//			while (dbcur.next()){
//				ISASRecord dbr = dbcur.getRecord();
//				String sel= "SELECT * FROM "+tabella+" WHERE "+
//				" n_cartella="+cartella+
//				" AND n_contatto="+contatto;
//				Object tipo_dato = dbr.get(data);
//				if (tipo_dato instanceof Integer)
//					sel += " AND "+data+" = "+(Integer)dbr.get(data);
//				else if (tipo_dato instanceof java.sql.Date)
//					sel += " AND "+data+" = "+formatDate(dbc,(""+(java.sql.Date)dbr.get(data)));
//				ISASRecord dbr2 = dbc.readRecord(sel);
//				dbc.deleteRecord(dbr2);
//			}
//			dbcur.close();
//			done=true;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			throw new SQLException("Errore eseguendo una deleteSchede() - "+  e1);
//		}/*finally{	// 19.06.08 rb
//        if(!done){
//            try{
//            dbc.close();
//	    super.close(dbc);
//	    }catch(Exception e2){LOG.error(e2);}
//        }
//    }*/
//	}
//	public void delete_ambu(myLogin mylogin,Hashtable h)
//	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		ISASConnection dbc=null;
//		try{
//			dbc=super.logIn(mylogin);
//			String sel_del="SELECT * FROM skintamb WHERE"+
//			" n_cartella="+(String)h.get("n_cartella")+
//			" AND n_contatto="+(String)h.get("n_contatto")+
//			" AND ska_prog='"+(String)h.get("ska_prog")+"'";
//			ISASRecord dbr=dbc.readRecord(sel_del);
//			printError("Delete_ambu: "+sel_del);
//			if (dbr!=null)
//				dbc.deleteRecord(dbr);
//			dbc.close();
//			super.close(dbc);
//			done=true;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			throw new SQLException("Errore eseguendo una delete() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

//	public void delete_eventi(myLogin mylogin,Hashtable h)
//	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		ISASConnection dbc=null;
//		try{
//			dbc=super.logIn(mylogin);
//			String sel_del="SELECT * FROM skieventi WHERE"+
//			" n_cartella="+(String)h.get("n_cartella")+
//			" AND n_contatto="+(String)h.get("n_contatto")+
//			" AND skev_data="+formatDate(dbc,((String)h.get("skev_data")));
//			ISASRecord dbr=dbc.readRecord(sel_del);
//			
//			if (dbr!=null)
//				dbc.deleteRecord(dbr);
//			dbc.close();
//			super.close(dbc);
//			done=true;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			throw new SQLException("Errore eseguendo una delete() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}



//	private void CalcolaDataMinima(ISASConnection dbc, String cartella, String contatto)
//	throws DBRecordChangedException,ISASPermissionDeniedException, SQLException {
//
//		try{
//			String mysel="SELECT data_inizio FROM contsan_n WHERE n_cartella="+cartella+
//			" AND n_contatto="+contatto+
//			" ORDER BY data_inizio";
//			printError("ContattiEJB.Contsan_n(): "+mysel);
//			ISASRecord dbrcont=dbc.readRecord(mysel);
//			if (dbrcont!=null){
//				String data_minore=((java.sql.Date)dbrcont.get("data_inizio")).toString();
//				String selcon="Select * from contsan where n_cartella="+cartella+
//				" and n_contatto="+contatto;
//				ISASRecord dbrupdate=dbc.readRecord(selcon);
//				dbrupdate.put("data_contatto",data_minore);
//				dbc.writeRecord(dbrupdate);
//			}
//		}catch(DBRecordChangedException e){
//			LOG.error("ContattiEJB.deleteContsan(): "+e);
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			LOG.error("ContattiEJB.deleteContsan(): "+e);
//			throw e;
//		}catch(Exception e1){
//			LOG.error("ContattiEJB.deleteContsan(): "+e1);
//			throw new SQLException("Errore eseguendo una deleteContsan() - "+  e1);
//		}
//	}


	private ISASRecord insertInfRef(ISASConnection dbc, String infref,
			String data_ref, String data_apertura, String n_cartella,
			String n_contatto,String skfpg_tipo_operatore) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {

		boolean done=false;
		try{
			ISASRecord dbref=dbc.newRecord("skfpg_referente");
			
			dbref.put("n_cartella",n_cartella);
			dbref.put("n_contatto",n_contatto);
			dbref.put("skfpg_tipo_operatore",skfpg_tipo_operatore);
			
			dbref.put("skfpg_referente",infref);
			dbref.put("skfpg_referente_da",data_ref);
			dbc.writeRecord(dbref);
			done=true;
			return dbref;
		}catch(Exception e){
			throw newEjbException("Errore eseguendo insertInfRef: " + e.getMessage(), e);
		}
	}



//	public ISASRecord insert_intamb(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		String n_cartella=null;
//		String n_contatto=null;
//		String data_apertura=null;
//		ISASConnection dbc=null;
//		try {
//			
//			n_cartella=(String)h.get("n_cartella");
//			n_contatto=(String)h.get("n_contatto");
//			data_apertura=(String)h.get("skfpg_data_apertura");
//		}catch (Exception e){
//			e.printStackTrace();
//			throw new SQLException("SkInf insert: Errore: manca la chiave primaria");
//		}
//		try{
//			dbc=super.logIn(mylogin);
//			dbc.startTransaction();
//			String myselectMAX="Select MAX(ska_prog) prog from skintamb "+
//			" where n_cartella="+n_cartella+" and " +
//			" n_contatto="+n_contatto;
//			ISASRecord dbr_0=dbc.readRecord(myselectMAX);
//			int prog=0;
//			if (dbr_0!=null){
//				Integer k=(Integer)dbr_0.get("prog");
//				if (k!=null && !k.equals("")){
//					prog=k.intValue();
//					prog=prog+1;
//				}else
//					prog=0;
//			}
//			ISASRecord dbr=dbc.newRecord("skintamb");
//			Enumeration n=h.keys();
//			while(n.hasMoreElements()){
//				String e=(String)n.nextElement();
//				dbr.put(e,h.get(e));
//			}
//			dbr.put("ska_prog",""+prog);
//			dbc.writeRecord(dbr);
//			String myselect="Select * from skintamb where n_cartella="+n_cartella+" and " +
//			" n_contatto="+n_contatto+
//			" and ska_prog="+prog;
//			dbr=dbc.readRecord(myselect);
//			
//			dbc.commitTransaction();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return dbr;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception ex){
//				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
//			}
//			throw new SQLException("Errore eseguendo una insert() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

//	public ISASRecord insert_eventi(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		String n_cartella=null;
//		String n_contatto=null;
//		String data=null;
//		ISASConnection dbc=null;
//		try {
//			
//			n_cartella=(String)h.get("n_cartella");
//			n_contatto=(String)h.get("n_contatto");
//			data=(String)h.get("skev_data");
//		}catch (Exception e){
//			e.printStackTrace();
//			throw new SQLException("SkInf insert_eventi: Errore: manca la chiave primaria");
//		}
//		try{
//			dbc=super.logIn(mylogin);
//			dbc.startTransaction();
//			ISASRecord dbr=dbc.newRecord("skieventi");
//			Enumeration n=h.keys();
//			while(n.hasMoreElements()){
//				String e=(String)n.nextElement();
//				dbr.put(e,h.get(e));
//			}
//			dbc.writeRecord(dbr);
//			String myselect="Select * from skieventi where n_cartella="+n_cartella+" and " +
//			" n_contatto="+n_contatto+
//			" and skev_data="+formatDate(dbc,data);
//			dbr=dbc.readRecord(myselect);
//
//			dbc.commitTransaction();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return dbr;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception ex){
//				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
//			}
//			throw new SQLException("Errore eseguendo una insert() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

	private String CaricaData(ISASConnection dbc, String cartella, String contatto,
			String tabella, String data_scheda)
	throws SQLException {
		String rit = null;
		try{
			String myselect="SELECT MAX("+data_scheda+") data FROM "+tabella+" WHERE"+
			" n_cartella="+cartella;
			if(!tabella.equals("skinucleo_fam"))
				myselect += " and n_contatto="+contatto;
			
			ISASRecord dbr = dbc.readRecord(myselect);
			if (dbr!=null)
				if (dbr.get("data") != null)
					rit = ""+dbr.get("data");
			return rit;
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkInf=>CaricaData: Errore: manca la chiave primaria", e);
		}
	}

	public ISASRecord query_aggiorna(myLogin mylogin,Hashtable h)
	throws SQLException {
		boolean done=false;
		String cartella=null;
		String contatto=null;
		String skfpg_tipo_operatore=null;
		String skfpg_data_uscita=null;
		//String apertura=null;
		ISASConnection dbc=null;
		try {
			cartella=(String)h.get("n_cartella");
			contatto=(String)h.get("n_contatto");
			skfpg_tipo_operatore=(String)h.get("skfpg_tipo_operatore");
			
			//apertura=(String)h.get("skfpg_data_apertura");
			skfpg_data_uscita = (String)h.get("skfpg_data_uscita");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkInf query_aggiorna: Errore: manca la chiave primaria", e);
		}
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skfpg where "+
			" n_cartella="+cartella+
			" and n_contatto="+contatto+
			" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
			//" and skfpg_data_apertura="+formatDate(dbc,apertura);
			ISASRecord dbr=dbc.readRecord(myselect);
			dbr.put("skfpg_data_uscita", skfpg_data_uscita);
			dbr.put("skfpg_data_prest_ric",CaricaData(dbc,cartella,contatto,"skiprestaz","skp_data"));
			dbr.put("skfpg_data_accogl",CaricaData(dbc,cartella,contatto,"skiaccoglienza","ska_data"));
			dbr.put("skfpg_data_fam",CaricaData(dbc,cartella,contatto,"skifamiglia","skf_data"));

			// 15/05/06 dalla vers. 06.03
			dbr.put("skfpg_data_nucleofam",CaricaData(dbc,cartella,contatto,"skinucleo_fam","data_variazione"));
			/*** 16/11/06 m.
        dbr.put("skfpg_data_caregiver",CaricaData(dbc,cartella,contatto,"skicaregiver","skcg_data"));
        dbr.put("skfpg_data_braden",CaricaData(dbc,cartella,contatto,"skibraden","skb_data"));
			 ***/
			// 16/11/06 m.
			dbr.put("skfpg_data_caregiver",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_caregiver", dbr.get("skfpg_data_uscita"), "skcg_data"));
			dbr.put("skfpg_data_braden",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_braden", dbr.get("skfpg_data_uscita"), "skb_data"));
			dbr.put("skfpg_data_tiq",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_tiq", dbr.get("skfpg_data_uscita")));
			// fine 15/05/06

			dbr.put("skfpg_data_terapia",CaricaData(dbc,cartella,contatto,"skiterapia","skt_data"));
			dbr.put("skfpg_data_norton",CaricaData(dbc,cartella,contatto,"skinorton","skno_data"));
			dbr.put("skfpg_data_dolore",CaricaData(dbc,cartella,contatto,"skidolore","skid_data"));
			dbr.put("skfpg_data_ausili",CaricaData(dbc,cartella,contatto,"skiausili_t","skat_data"));
			dbr.put("skfpg_data_relaz",CaricaData(dbc,cartella,contatto,"skirelaz","skr_dat"));
			dbr.put("skfpg_data_coscienza",CaricaData(dbc,cartella,contatto,"skicoscienza","skc_data"));
			dbr.put("skfpg_data_umore",CaricaData(dbc,cartella,contatto,"skiumore","sku_data"));
			dbr.put("skfpg_data_respiraz",CaricaData(dbc,cartella,contatto,"respiraz","skr_data"));
			dbr.put("skfpg_data_nutriz",CaricaData(dbc,cartella,contatto,"skinutriz","skn_data"));
			dbr.put("skfpg_data_escret",CaricaData(dbc,cartella,contatto,"skiescret","ske_data"));
			dbr.put("skfpg_data_motr",CaricaData(dbc,cartella,contatto,"skimotric","skd_data"));
			dbr.put("skfpg_data_altro",CaricaData(dbc,cartella,contatto,"skialtro","skx_data"));
			dbr.put("skfpg_data_decubito",CaricaData(dbc,cartella,contatto,"skidecubito_t","skdt_data"));
			dbr.put("skfpg_data_ulcere",CaricaData(dbc,cartella,contatto,"skiulcere_t","skut_data"));
			dbr.put("skfpg_data_lesioni",CaricaData(dbc,cartella,contatto,"skivasco_t","skvt_data"));
			dbr.put("skfpg_data_alteraz",CaricaData(dbc,cartella,contatto,"skialteraz","skal_data"));
			dbr.put("skfpg_data_indagini",CaricaData(dbc,cartella,contatto,"skiesami","skes_data"));
			//bargi 10/11/2006 dbr.put("skfpg_data_adl",CaricaData(dbc,cartella,contatto,"tabadl","data"));
			//bargi 10/11/2006 dbr.put("skfpg_data_iadl",CaricaData(dbc,cartella,contatto,"tabiadl","data"));
			//bargi 10/11/2006 dbr.put("skfpg_data_pfeiffer",CaricaData(dbc,cartella,contatto,"pfeiffer","data"));

			dbr.put("skfpg_data_adl",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_adl", dbr.get("skfpg_data_uscita")));
			dbr.put("skfpg_data_pfeiffer",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_pfeiffer", dbr.get("skfpg_data_uscita")));
			dbr.put("skfpg_data_iadl",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_iadl",dbr.get("skfpg_data_uscita")));
			dbr.put("skfpg_data_barthel",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_barthel", dbr.get("skfpg_data_uscita"),"skbt_data"));

			printError("SkFpgEJB -- query_aggiorna: "+dbr.getHashtable().toString());
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_aggiorna()  ", e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
	}

	public ISASRecord query_aggRecord(myLogin mylogin,Hashtable h)
	throws SQLException {
		boolean done=false;
		String cartella=null;
		String contatto=null;
		String skfpg_tipo_operatore=null;
		String apertura=null;
		String skfpg_data_uscita=null;
		ISASConnection dbc=null;
		try {
			cartella=(String)h.get("n_cartella");
			contatto=(String)h.get("n_contatto");
			skfpg_tipo_operatore=(String)h.get("skfpg_tipo_operatore");
			//apertura=(String)h.get("skfpg_data_apertura");
			skfpg_data_uscita = (String)h.get("skfpg_data_uscita");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkFpgEJB queryKey: Errore: manca la chiave primaria", e);
		}
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skfpg where "+
			" n_cartella="+cartella+
			" and n_contatto="+contatto+
			" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
			//" and skfpg_data_apertura="+formatDate(dbc,apertura);
			ISASRecord dbr=dbc.readRecord(myselect);
			dbr.put("skfpg_data_uscita", skfpg_data_uscita);
			dbr.put("skfpg_data_prest_ric",CaricaData(dbc,cartella,contatto,"skiprestaz","skp_data"));
			dbr.put("skfpg_data_accogl",CaricaData(dbc,cartella,contatto,"skiaccoglienza","ska_data"));
			dbr.put("skfpg_data_fam",CaricaData(dbc,cartella,contatto,"skifamiglia","skf_data"));

			// 15/05/06 dalla vers. 06.03
			dbr.put("skfpg_data_nucleofam",CaricaData(dbc,cartella,contatto,"skinucleo_fam","data_variazione"));
			/*** 16/11/06 m.
        dbr.put("skfpg_data_caregiver",CaricaData(dbc,cartella,contatto,"skicaregiver","skcg_data"));
        dbr.put("skfpg_data_braden",CaricaData(dbc,cartella,contatto,"skibraden","skb_data"));
			 ***/
			/*	// 16/11/06 m.
        dbr.put("skfpg_data_caregiver",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_caregiver", dbr.get("skfpg_data_uscita"), "skcg_data"));
        dbr.put("skfpg_data_braden",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_braden", dbr.get("skfpg_data_uscita"), "skb_data"));
        dbr.put("skfpg_data_tiq",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_tiq", dbr.get("skfpg_data_uscita")));
	// fine 15/05/06
			 */
			dbr.put("skfpg_data_terapia",CaricaData(dbc,cartella,contatto,"skiterapia","skt_data"));
			dbr.put("skfpg_data_norton",CaricaData(dbc,cartella,contatto,"skinorton","skno_data"));
			dbr.put("skfpg_data_dolore",CaricaData(dbc,cartella,contatto,"skidolore","skid_data"));
			dbr.put("skfpg_data_ausili",CaricaData(dbc,cartella,contatto,"skiausili_t","skat_data"));
			dbr.put("skfpg_data_relaz",CaricaData(dbc,cartella,contatto,"skirelaz","skr_dat"));
			dbr.put("skfpg_data_coscienza",CaricaData(dbc,cartella,contatto,"skicoscienza","skc_data"));
			dbr.put("skfpg_data_umore",CaricaData(dbc,cartella,contatto,"skiumore","sku_data"));
			dbr.put("skfpg_data_respiraz",CaricaData(dbc,cartella,contatto,"respiraz","skr_data"));
			dbr.put("skfpg_data_nutriz",CaricaData(dbc,cartella,contatto,"skinutriz","skn_data"));
			dbr.put("skfpg_data_escret",CaricaData(dbc,cartella,contatto,"skiescret","ske_data"));
			dbr.put("skfpg_data_motr",CaricaData(dbc,cartella,contatto,"skimotric","skd_data"));
			dbr.put("skfpg_data_altro",CaricaData(dbc,cartella,contatto,"skialtro","skx_data"));
			dbr.put("skfpg_data_decubito",CaricaData(dbc,cartella,contatto,"skidecubito_t","skdt_data"));
			dbr.put("skfpg_data_ulcere",CaricaData(dbc,cartella,contatto,"skiulcere_t","skut_data"));
			dbr.put("skfpg_data_lesioni",CaricaData(dbc,cartella,contatto,"skivasco_t","skvt_data"));
			dbr.put("skfpg_data_alteraz",CaricaData(dbc,cartella,contatto,"skialteraz","skal_data"));
			dbr.put("skfpg_data_indagini",CaricaData(dbc,cartella,contatto,"skiesami","skes_data"));


			//bargi 10/11/2006 dbr.put("skfpg_data_adl",CaricaData(dbc,cartella,contatto,"tabadl","data"));
			//bargi 10/11/2006 dbr.put("skfpg_data_iadl",CaricaData(dbc,cartella,contatto,"tabiadl","data"));
			//bargi 10/11/2006 dbr.put("skfpg_data_pfeiffer",CaricaData(dbc,cartella,contatto,"pfeiffer","data"));

			/*		dbr.put("skfpg_data_adl",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_adl", dbr.get("skfpg_data_uscita")));
		dbr.put("skfpg_data_pfeiffer",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_pfeiffer", dbr.get("skfpg_data_uscita")));
		dbr.put("skfpg_data_iadl",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_iadl",dbr.get("skfpg_data_uscita")));
			 */
			
			dbc.writeRecord(dbr);
			//bargi 05/12/2006
			dbr.put("skfpg_data_adl",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_adl", dbr.get("skfpg_data_uscita")));
			dbr.put("skfpg_data_pfeiffer",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_pfeiffer", dbr.get("skfpg_data_uscita")));
			dbr.put("skfpg_data_iadl",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_iadl",dbr.get("skfpg_data_uscita")));
			dbr.put("skfpg_data_caregiver",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_caregiver", dbr.get("skfpg_data_uscita"), "skcg_data"));
			dbr.put("skfpg_data_braden",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_braden", dbr.get("skfpg_data_uscita"), "skb_data"));
			dbr.put("skfpg_data_tiq",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_tiq", dbr.get("skfpg_data_uscita")));
			// fine 05/12/06
			dbr.put("skfpg_data_barthel",CaricaData(dbc, cartella, dbr.get("skfpg_data_apertura"), "sc_barthel", dbr.get("skfpg_data_uscita"),"skbt_data"));

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_aggRecord()  ", e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
	}

//	public ISASRecord update_intamb(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		String n_cartella=null;
//		String n_contatto=null;
//		String prog=null;
//		ISASConnection dbc=null;
//		try {
//			
//			n_cartella=(String)h.get("n_cartella");
//			n_contatto=(String)h.get("n_contatto");
//			prog=(String)h.get("ska_prog");
//		}catch (Exception e){
//			e.printStackTrace();
//			throw new SQLException("SkInf update: Errore: manca la chiave primaria");
//		}
//		try{
//			dbc=super.logIn(mylogin);
//			dbc.startTransaction();
//			String myselect="Select * from skintamb where "+
//			" n_cartella="+n_cartella+
//			" and ska_prog="+prog+
//			" and n_contatto="+n_contatto;
//			//" and skfpg_data_apertura="+formatDate(dbc,data_apertura);
//			
//			ISASRecord dbr=dbc.readRecord(myselect);
//			if (dbr!=null){
//				dbr.put("ska_operatore",h.get("ska_operatore"));
//				dbr.put("ska_data_fine",h.get("ska_data_fine"));
//				dbr.put("ska_data_inizio",h.get("ska_data_inizio"));
//				dbr.put("ska_prestaz",h.get("ska_prestaz"));
//				dbr.put("ska_freq",h.get("ska_freq"));
//			}
//
//			ISASRecord dbamb=dbc.newRecord("skintamb");
//			Enumeration n=dbr.getHashtable().keys();
//			while(n.hasMoreElements()){
//				String e=(String)n.nextElement();
//				dbamb.put(e,dbr.get(e));
//			}
//
//			dbc.deleteRecord(dbr);
//			dbc.writeRecord(dbamb);
//			String sel="Select * FROM skintamb WHERE "+
//			" n_cartella="+n_cartella+
//			" and ska_prog="+prog+
//			" and n_contatto="+n_contatto;
//			dbr=dbc.readRecord(sel);
//			dbc.commitTransaction();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return dbr;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception ex){
//				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
//			}
//			throw new SQLException("Errore eseguendo una update() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

	public ISASRecord salvaReferente(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		String n_cartella=null;
		String n_contatto=null;
		String skfpg_tipo_operatore=null;
		ISASConnection dbc=null;
		try {
			printError("***HASH "+h.toString());
			n_cartella=(String)h.get("n_cartella");
			n_contatto=(String)h.get("n_contatto");
			skfpg_tipo_operatore=(String)h.get("skfpg_tipo_operatore");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkInf update: Errore: manca la chiave primaria", e);
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String myselect="Select * from skfpg where "+
			" n_cartella="+n_cartella+
			" and n_contatto="+n_contatto+
			" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
			printError("select prima di aggiornamento su skfpg==="+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			
			if (dbr!=null){
				LOG.error("CI ENTROOOOOO");
				dbr.put("skfpg_referente",(String)h.get("skfpg_referente"));
				dbr.put("skfpg_referente_da",java.sql.Date.valueOf((String)h.get("skfpg_referente_da")));
			}
			
			dbc.writeRecord(dbr);
			myselect="Select * from skfpg where "+
			" n_cartella="+n_cartella+
			" and n_contatto="+n_contatto+
			" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
			dbr=dbc.readRecord(myselect);
			
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			
			return dbr;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e1){
			LOG.error(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception ex){
				throw new SQLException("Errore eseguendo una rollback() - "+  ex, ex);
			}
			throw new SQLException("Errore eseguendo una update() - "+  e1, e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
	}

//	public ISASRecord update_eventi(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		boolean done=false;
//		String n_cartella=null;
//		String n_contatto=null;
//		String data_apertura=null;
//		String prog=null;
//		ISASConnection dbc=null;
//		try {
//			printError("***HASH "+h.toString());
//			n_cartella=(String)h.get("n_cartella");
//			n_contatto=(String)h.get("n_contatto");
//			data_apertura=(String)h.get("skfpg_data_apertura");
//		}catch (Exception e){
//			e.printStackTrace();
//			throw new SQLException("SkInf update_eventi: Errore: manca la chiave primaria");
//		}
//		try{
//			dbc=super.logIn(mylogin);
//			dbc.startTransaction();
//			String myselect="Select * from skieventi where "+
//			" n_cartella="+n_cartella+
//			" and n_contatto="+n_contatto;
//			printError("update_eventi su skfpg==="+myselect);
//			ISASRecord dbr=dbc.readRecord(myselect);
//			if (dbr!=null){
//				dbr.put("skev_operatore",h.get("skev_operatore"));
//				dbr.put("skev_data",h.get("skev_data"));
//				dbr.put("skev_note",h.get("skev_note"));
//			}
//
//			ISASRecord dbamb=dbc.newRecord("skieventi");
//			Enumeration n=dbr.getHashtable().keys();
//			while(n.hasMoreElements()){
//				String e=(String)n.nextElement();
//				dbamb.put(e,dbr.get(e));
//			}
//
//			dbc.deleteRecord(dbr);
//			dbc.writeRecord(dbamb);
//			String sel="Select * FROM skieventi WHERE "+
//			" n_cartella="+n_cartella+
//			" and n_contatto="+n_contatto;
//			dbr=dbc.readRecord(sel);
//			dbc.commitTransaction();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return dbr;
//		}catch(DBRecordChangedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(ISASPermissionDeniedException e){
//			e.printStackTrace();
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception e1){
//				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
//			}
//			throw e;
//		}catch(Exception e1){
//			LOG.error(e1);
//			try{
//				dbc.rollbackTransaction();
//			}catch(Exception ex){
//				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
//			}
//			throw new SQLException("Errore eseguendo una update() - "+  e1);
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

	public ISASRecord query_controlloData(myLogin mylogin,Hashtable h)
	throws  SQLException {
		String ritorno="";
		String ritorno_max="";
		ISASConnection dbc=null;
		ISASRecord dbtxt = null;
		boolean done=false;
		try {
			dbc=super.logIn(mylogin);
			String mysel = "SELECT MIN (int_data_prest) data "+
			", MAX (int_data_prest) data_max"+
			" FROM interv WHERE "+
			" int_cartella =" + (String)h.get("n_cartella") +
			" AND int_contatto =" + (String)h.get("n_contatto")+
			" AND int_tipo_oper='"+ (String)h.get("skfpg_tipo_operatore")+"'";
			dbtxt = dbc.readRecord(mysel);

			if (dbtxt!=null)
				if (dbtxt.get("data")!=null)
				{
					debugMessage("QUA");

					ritorno=""+((java.sql.Date)dbtxt.get("data"));
					if (dbtxt.get("data_max")!=null)
						ritorno_max=""+((java.sql.Date)dbtxt.get("data_max"));

				}
				else  ritorno="N";
			else ritorno="N";

			dbtxt.put("trova_interv", ritorno);
			dbtxt.put("trova_interv_max", ritorno_max);
			dbtxt.put("n_cartella",(String)h.get("n_cartella"));
			dbtxt.put("n_contatto",(String)h.get("n_contatto"));
			dbc.close();
			super.close(dbc);
			done=true;
			return dbtxt;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_controlloData()  ", e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){LOG.error(e1);}
			}
		}
	}
//	public Vector query_progass(myLogin mylogin,Hashtable h) throws SQLException {
//		boolean done=false;
//		ISASConnection dbc=null;
//		try{
//			dbc=super.logIn(mylogin);
//			String myselect="Select * from skiprogass where n_cartella="+
//			(String)h.get("n_cartella")+" and n_contatto="+
//			(String)h.get("n_contatto")+" ORDER BY skpa_data";
//			ISASCursor dbcur=dbc.startCursor(myselect);
//			Vector vdbr=dbcur.getAllRecord();
//			dbcur.close();
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return vdbr;
//		}catch(Exception e){
//			e.printStackTrace();
//			throw new SQLException("Errore eseguendo una query()  ");
//		}finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e1){LOG.error(e1);}
//			}
//		}
//
//	}


	private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
		Hashtable htxt = new Hashtable();
		if (val_codice==null) return "";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			return ((String)dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return "";
		}
	}

	public Hashtable query_Allcombo(myLogin mylogin,Hashtable h,Vector vkey) throws SQLException {

		boolean done=false;
		Hashtable res =null;
		String mysel_where=null;
		ISASConnection dbc=null;

		try{
			dbc=super.logIn(mylogin);
			String myselect = "";
			for(int i=0;i<vkey.size();i++)
			{
				String key=(String)vkey.elementAt(i);
				
				if (key.equals("ICHIUS")){
					myselect = "SELECT * FROM tab_voci WHERE "+
					"tab_cod='"+key+
					"' AND tab_val <> '#'";
					//Viene messo questo filtro perch� nella tabella tab_voci il primo record
					//ha valore # e indica il nome della combo
					myselect=myselect+" ORDER BY tab_val ";
					printError("query_comboTAB: "+myselect);
					ISASCursor dbcur=dbc.startCursor(myselect);
					Vector vdbr=dbcur.getAllRecord();
					if(res==null)
						res=new Hashtable();
					res.put(key,vdbr);
					dbcur.close();
				}else if(key.equals("SEGNALA")){
					myselect="SELECT * FROM segnala";
					printError("query_comboSegnala: "+myselect);
					ISASCursor dbcur=dbc.startCursor(myselect);
					Vector vdbr=dbcur.getAllRecord();
					if(res==null)
						res=new Hashtable();
					res.put(key,vdbr);
					dbcur.close();
				}else if(key.equals("TIPUTES")){
					myselect="SELECT * FROM tipute_s";
					printError("query_comboTipute: "+myselect);
					ISASCursor dbcur=dbc.startCursor(myselect);
					Vector vdbr=dbcur.getAllRecord();
					if(res==null)
						res=new Hashtable();
					res.put(key,vdbr);
					dbcur.close();
				}else if(key.equals("MOTIVOS")){
					myselect="SELECT * FROM motivo_s";
					printError("query_comboMotivoS: "+myselect);
					ISASCursor dbcur=dbc.startCursor(myselect);
					Vector vdbr=dbcur.getAllRecord();

					if(res==null)
						res=new Hashtable();
					res.put(key,vdbr);
					dbcur.close();
				}
			}//end for
			dbc.close();
			super.close(dbc);
			done=true;
			return res;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_Allcombo()  ", e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					LOG.error(e1);
				}
			}
		}
	} // fine query_Allcombo

	/*Devo andare a caricare la scheda con data = alla data massima compresa
tra la data apertura e, se inserita, la data chiusura */
	// 16/11/06 m.
	private String CaricaData(ISASConnection dbc, String cartella, Object apertura,
			String tabella, Object chiusura)
	throws SQLException {
		return CaricaData(dbc, cartella, apertura, tabella, chiusura, "data");
	}


	private String CaricaData(ISASConnection dbc, String cartella, Object apertura,
			String tabella, Object chiusura, String nomeData)
	throws SQLException {
		String rit = null;
		try{
			String mysel="SELECT MAX(" + nomeData + ") data FROM "+tabella+
			" WHERE n_cartella="+cartella+
			" AND " + nomeData + ">="+formatDate(dbc,""+apertura);
			if(chiusura!=null)
				mysel+=" AND " + nomeData + "<="+formatDate(dbc,""+chiusura);
			
			ISASRecord dbr = dbc.readRecord(mysel);
			if (dbr!=null)
				if (dbr.get("data") != null)
					rit = ""+dbr.get("data");
			
			return rit;
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkInf=>CaricaData: Errore: manca la chiave primaria",e );
		}
	}


	
	public void leggiDiagnosi(ISASConnection mydbc, ISASRecord mydbr) throws Exception{
		String punto = ver + "leggiDiagnosi ";
		Vector vdbr = new Vector();
		DiagnosiEJB diagnosiEJB = new DiagnosiEJB();
		Hashtable dati = mydbr.getHashtable();
		ISASCursor dbcur = null;
		try {
			diagnosiEJB.leggiDiagnosi_interno(mydbc, dbcur, vdbr, dati, false);
		} finally{
			close_dbcur_nothrow(punto, dbcur);
		}
		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	
	// 07/12/06 m.: x DIAGNOSI --------------------------------------------------
	private void leggiDiagnosi_(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		String cart = ((Integer)mydbr.get("n_cartella")).toString();
		Object dtApertura = (Object)mydbr.get("skfpg_data_apertura");
		//gb 27/04/07		Object dtChiusura = (Object)mydbr.get("skfpg_data_chiusura");
		Object dtChiusura = (Object)mydbr.get("skfpg_data_uscita"); //gb 27/04/07

		Vector vdbr = new Vector();

		String critDtChius = "";
		if (dtChiusura != null)
			critDtChius = " AND data_diag <= " + formatDate(mydbc, dtChiusura.toString());

		String myselect = "SELECT * FROM diagnosi" +
		" WHERE n_cartella = " + cart +
		critDtChius +
		" ORDER BY data_diag DESC";

		ISASRecord recD = mydbc.readRecord(myselect);

		if (recD != null) {
			String dataIni = "";
			if (dtApertura != null)
				dataIni = dtApertura.toString();
			String dtIni = dataIni.substring(0,4) + dataIni.substring(5,7) + dataIni.substring(8,10);
			decodificaDiagn(mydbc, recD);
			decodificaOper(mydbc, recD);
			boolean isDataInContesto = checkData(recD, dtIni);
			costruisci5Rec(mydbc, recD, vdbr, (isDataInContesto?"C":"")+"0");
		}

		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	// Costruisce 5 record da quello letto: hanno tutti i campi del DB uguali, pi� le colonne fittizie del
	// codice e della descrizione, ognuno con i valori corrispondenti(rec1 con diag_1 e desc_diag_1, ecc).
	private void costruisci5Rec(ISASConnection mydbc, ISASRecord mydbr, Vector vett, String coloreCol) throws Exception
	{
		// aggiungo colonne fittizie al primo record
		mydbr.put("cod_alldiag", (String)mydbr.get("diag1"));
		mydbr.put("desc_alldiag", (String)mydbr.get("desc_diag1"));
		mydbr.put("progr", "1");
		mydbr.put("dt_diag", (java.sql.Date)mydbr.get("data_diag"));
		mydbr.put("clr_column", coloreCol);
		vett.addElement((ISASRecord)mydbr);

		// copio rec letto nei 4 nuovi record
		Hashtable h_1 = (Hashtable)mydbr.getHashtable();
		for (int j=2; j<6; j++) {
			ISASRecord dbr_i = mydbc.newRecord("diagnosi");
			copiaRec(h_1, dbr_i);
			
			//elisa b 25/01/11 : aggiungo solo record che hanno una patologia valorizzata
			if((dbr_i.get("diag" + j) == null) || 
					(dbr_i.get("diag" + j).toString().equals("")))
				break;
			
			// aggiungo colonne fittizie agli altri 4 record
			dbr_i.put("cod_alldiag", (String)dbr_i.get("diag" + j));
			dbr_i.put("desc_alldiag", (String)dbr_i.get("desc_diag" + j));
			dbr_i.put("progr", "" + j);
			dbr_i.put("dt_diag", "");
			vett.addElement((ISASRecord)dbr_i);
		}
	} // END costruisci6Rec

	private void copiaRec(Hashtable h_1, ISASRecord mydbr) throws Exception
	{
		Enumeration n_1 = h_1.keys();
		while (n_1.hasMoreElements()){
			String e = (String)n_1.nextElement();
			mydbr.put(e, h_1.get(e));
		}
	} // END copiaRec
	// 11/12/06: restituisce true se la data diagnosi e' >= della dataInizio del contesto
	private boolean checkData(ISASRecord mydbr, String dataI) throws Exception
	{
		DataWI dtDiag = new DataWI((java.sql.Date)mydbr.get("data_diag"));
		return dtDiag.isUguOSucc(dataI);
	} // END checkData
	// 07/12/06m.: x DIAGNOSI -------------------------------------------------

	// ============== Decodifiche ==========================

	private void decodificaDiagn(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		for (int k=1; k<6; k++){
			String cod = (String)mydbr.get("diag" + k);
			String desc = util.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi", cod, "diagnosi");
			mydbr.put("desc_diag" + k, desc);
		}
	}// END decodificaDiagn

	private void decodificaOper(ISASConnection mydbc, ISASRecord mydbr) throws Exception
	{
		String cod = (String)mydbr.get("cod_operatore");
		String desc = util.getDecode(mydbc, "operatori", "codice", cod,
				"nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nome_oper");
		mydbr.put("desc_oper", desc);
	}// END decodificaOper



//	//bargi 160407 chiusura piani assist pianointerv (piano_accessi) e agenda alla chiusura del contatto
//	//gb 12/09/07 *******
//	private void AggiornaData(String strNomeTabella, String strNCartella, String strNContatto,String skfpg_tipo_operatore, String strNomeFldDataChiusura, String strDataChiusura, String strNomeFldDataApertura, ISASConnection dbc)
//	throws  SQLException{
//		try {
//			debugMessage("chiudo TABELLA-->"+strNomeTabella);
//			String mysel = "SELECT *" +
//			" FROM " + strNomeTabella + 
//			" WHERE n_cartella = " + strNCartella +
//			" AND n_progetto = " + strNContatto +
//			" AND cod_obbiettivo = '00000000'" +
//			" AND n_intervento = 0" +
//			" AND pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
//			" AND " + strNomeFldDataChiusura + " IS NULL";
//			debugMessage("chiudo -->"+strNomeTabella+" select-->"+mysel);
//			ISASCursor dbcur=dbc.startCursor(mysel);
//			Vector vdbr=dbcur.getAllRecord();
//			for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
//			{
//				ISASRecord dbr=(ISASRecord)senum.nextElement();
//				String strDataApertura =((java.sql.Date)dbr.get(strNomeFldDataApertura)).toString();
//				
//				String sel =	"SELECT *" +
//				" FROM " + strNomeTabella +
//				" WHERE n_cartella = " + strNCartella +
//				" AND n_progetto = " + strNContatto +
//				" AND cod_obbiettivo = '00000000'" +
//				" AND n_intervento = 0" +
//				" AND pa_tipo_oper = '"+skfpg_tipo_operatore+"'" +
//				" AND " + strNomeFldDataApertura + " = " + formatDate(dbc, strDataApertura);
//				debugMessage("chiudo TABELLA-->"+sel);
//				ISASRecord dbrDett=dbc.readRecord(sel);
//				if(dbrDett.get(strNomeFldDataChiusura)==null){
//					dbrDett.put(strNomeFldDataChiusura, strDataChiusura);					
//				}				  
//				dbc.writeRecord(dbrDett);
//				if(dbrDett!=null)
//					AggiornaDataPianointerv(strNCartella, strNContatto, skfpg_tipo_operatore, strDataChiusura, strDataApertura, dbc);
//			}//fine for
//			dbcur.close();
//		} catch (Exception ex) {
//			LOG.error(ex);
//			throw new SQLException("Errore eseguendo una AggiornaData()  ");
//		}
//	}


	//gb 12/09/07 *******
//	private void AggiornaDataPianointerv(String strNCartella, String strNContatto,String skfpg_tipo_operatore, String strDataChiusura, String strDataApertura, ISASConnection dbc)
//	throws  SQLException{
//		try {
//			debugMessage("chiudo TABELLA--> piano_accessi");
//			String mysel =	"SELECT *" +
//			" FROM piano_accessi" +
//			" WHERE n_cartella = " + strNCartella +
//			" AND n_progetto =" + strNContatto +
//			" AND cod_obbiettivo = '00000000'" +
//			" AND n_intervento = 0" +
//			" AND pa_tipo_oper ='"+skfpg_tipo_operatore+"'" +
//			" AND pa_data = " + formatDate(dbc,strDataApertura) +
//			" AND (pi_data_fine IS NULL OR pi_data_fine > " + formatDate(dbc,strDataChiusura)+")";
//			debugMessage("piano_accessi da chiudere select-->"+mysel);
//			//se pi_data_fine � valorizzata ma data > della data chiusura questa viene anticipata
//			ISASCursor dbcur=dbc.startCursor(mysel);
//			while (dbcur.next())
//			{
//				ISASRecord dbr=dbcur.getRecord();
//				String sel =	"SELECT *" +
//				" FROM piano_accessi" +
//				" WHERE n_cartella = " + strNCartella +
//				" AND n_progetto =" + strNContatto +
//				" AND cod_obbiettivo = '00000000'" +
//				" AND n_intervento = 0" +
//				" AND pa_tipo_oper ='"+skfpg_tipo_operatore+"'" +
//				" AND pa_data = " + formatDate(dbc,strDataApertura) +
//				" AND pi_prog = " + (Integer)dbr.get("pi_prog");
//				debugMessage("chiudo piano_accessi select-->"+sel);
//				ISASRecord dbrDett=dbc.readRecord(sel);
//				if(dbrDett!=null){
//					dbrDett.put("pi_data_fine", strDataChiusura);
//					dbc.writeRecord(dbrDett);
//				}
//			}//fine for
//			dbcur.close();
//		} catch (Exception ex) {
//			LOG.error(ex);
//			throw new SQLException("Errore eseguendo una AggiornaData()  ");
//		}
//	}
//
//
//	// rimuovoAgendaCaricata(n_cartella, n_contatto, data_chiusura, dbc);	
//	//gb 12/09/07 private void rimuovoAgendaCaricata(String cartella,String contatto,String data_chiusura,ISASConnection dbc)
//	private void rimuovoAgendaCaricata(String strNCartella, String strNContatto,String skfpg_tipo_operatore, String strDataChiusura, ISASConnection dbc)
//	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
//	{
//		boolean done=false;
//		ISASCursor dbcur=null;
//		try{
//			//gb 06/09/07	 String mysel =	"select * from agenda_interv, agenda_intpre " +
//			String mysel =	"select * from agendant_interv, agendant_intpre " + //gb 06/09/07
//			" where ag_data > " + formatDate(dbc, strDataChiusura) + " and " +
//			" ag_cartella = " + strNCartella + " and " +
//			" ag_contatto = " + strNContatto + " and " +
//			" ag_tipo_oper = '"+skfpg_tipo_operatore+"' and " +
//			" ag_stato = 0 and " +//cancello solo appunt con stato a 0
//			" ag_data = ap_data and " +
//			" ag_progr = ap_progr and " +
//			" ag_oper_ref = ap_oper_ref " +
//			" order by ag_data";
//			debugMessage("rimuovoAgendaCaricata select=="+mysel);
//			dbcur=dbc.startCursor(mysel);
//			while (dbcur.next())
//			{
//				ISASRecord dbr=dbcur.getRecord();
//				cancellaAppuntam(dbr, dbc);
//			}
//			if (dbcur != null) dbcur.close();
//			done=true;
//		}catch(Exception e){
//			LOG.error("Errore in cancella agendant_intpre..."+e);
//			throw new SQLException("Errore eseguendo rimuovoAgendaCaricata()  ");
//		}finally{
//			if(!done)
//			{
//				try{
//					if (dbcur != null)
//						dbcur.close();
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}

//	private void cancellaAppuntam(ISASRecord dbrec,ISASConnection dbc)
//	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
//	{
//		try{
//
//			String data=((java.sql.Date)dbrec.get("ap_data")).toString();
//			String selag = "SELECT *" +
//			//gb 06/09/07		     " FROM agenda_intpre" +
//			" FROM agendant_intpre" + //gb 06/09/07
//			" WHERE ap_data = " + formatDate(dbc,data) +
//			" AND ap_progr = " + dbrec.get("ap_progr") +
//			" AND ap_oper_ref = '" + (String)dbrec.get("ap_oper_ref") + "'" +
//			" AND ap_prest_cod = '" + (String)dbrec.get("ap_prest_cod") + "'";
//			ISASRecord dbag=dbc.readRecord(selag);
//			if(dbag!=null){
//				dbc.deleteRecord(dbag);
//				dbag=null;
//				//devo controllare se sono rimasti record su agendant_intpre se non
//				//ce ne sono occorre cancellare anche il record su agendant_interv
//				selag = "SELECT COUNT(*) tot" +
//				//gb 06/09/07		  " FROM agenda_intpre" +
//				" FROM agendant_intpre" + //gb 06/09/07
//				" WHERE ap_data = " + formatDate(dbc,data) +
//				" AND ap_progr = " + dbrec.get("ap_progr") +
//				" AND ap_oper_ref = '" + (String)dbrec.get("ap_oper_ref") + "'";
//				dbag=dbc.readRecord(selag);
//
//				int t=0;
//				if(dbag!=null) t=util.getIntField(dbag, "tot");//convNumDBToInt("tot",dbag);
//				if(t==0)
//				{
//					//cancello da agenda_interv
//					selag = "SELECT *" +
//					//gb 06/09/07		     " FROM agenda_interv" +
//					" FROM agendant_interv" + //gb 06/09/07
//					" WHERE ag_data = " + formatDate(dbc,data) +
//					" AND ag_progr = " + dbrec.get("ag_progr") +
//					" AND ag_oper_ref = '" + (String)dbrec.get("ag_oper_ref") + "'";
//					dbag=dbc.readRecord(selag);
//					dbc.deleteRecord(dbag);
//				}
//			}
//		}catch(Exception e){
//			LOG.error("Errore in cancella agenda_intpre..."+e);
//			throw new SQLException("Errore eseguendo cancellaAppuntam()  ");
//		}
//	}

	// 13/09/07 m.: ctrl esistenza contatti successivi ad una certa data
	public Boolean query_checkContSuccessivi(myLogin mylogin, Hashtable h0)
	{
		boolean done = false;
		ISASConnection dbc = null;
		boolean risu = false;
		ISASCursor dbcur = null;

		try {
			dbc = super.logIn(mylogin);

			String cart = (String)h0.get("n_cartella");
			String skfpg_tipo_operatore = (String)h0.get("skfpg_tipo_operatore");
			String dtRiferimento = (String)h0.get("dataRif");
			// 14/04/08 ----
			boolean soloAperti = ((h0.get("soloAperti") != null) && (((String)h0.get("soloAperti")).trim().equals("S")));
			String critSoloAperti = " AND skfpg_data_uscita IS NULL";
			// 14/04/08 ----

			String myselect = "SELECT * FROM skfpg" +
			" WHERE n_cartella = " + cart +
			" AND skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'"+
			" AND skfpg_data_apertura > " + formatDate(dbc, dtRiferimento) +
			(soloAperti?critSoloAperti:""); // 14/04/08

			printError("SkFpgEJB: query_checkContSuccessivi - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();
			dbc.close();	// 19.0.08 rb
			super.close(dbc);
			done = true;

			return (new Boolean(risu));
		} catch(Exception e1){
			LOG.error("SkFpgEJB.query_checkContSuccessivi - Eccezione=[" + e1 + "]");
			return (Boolean)null;
		} finally{
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

//	// 12/10/07: chiusura skValutazione -> aggiornamento dataChiusura
//	private void chiudiSkValutaz(ISASConnection mydbc, String numCart, ISASRecord mydbr, String data_chiusura) throws Exception
//	{
//		if (mydbr != null){
//			mydbr.put("pr_data_chiusura", data_chiusura);
//			mydbc.writeRecord(mydbr);
//		}
//	}


	// 06/12/07: si usano degli ALIAS per riportare i nome dei campi a quelli estratti da un'analoga select sulla tabella ASS_PROGETTO
//	public Vector query_contattiAperti(myLogin mylogin, Hashtable h) throws  SQLException, ISASPermissionDeniedException
//	{
//		boolean done = false;
//		ISASConnection dbc = null;
//		ISASCursor dbcur = null;
//
//		String strDataDa = (String) h.get("da_data");
//		String strDataA = (String) h.get("a_data");
//		String strOperLoggato = (String) h.get("operatore_loggato");
//
//		try{ 
//			dbc=super.logIn(mylogin);
//
//			String myselect = "SELECT sk.*," +
//			" sk.n_contatto n_progetto," +
//			" sk.skfpg_data_apertura ap_data_apertura," +
//			" sk.skfpg_data_uscita ap_data_chiusura," +
//			" sk.skfpg_descr_contatto motivo_decod," +
//			" p.pr_data," +
//			" p.pr_data_chiusura," +
//			" p.pr_motivo_val_ap," +
//			" p.pr_motivi_val_ch," +
//			" p.pr_data_carico" +
//			" FROM skfpg sk," +
//			" progetto p," + 
//			" progetto_cont pc" +
//			/** 14/04/08
//                      " WHERE sk.skfpg_data_apertura <= " + formatDate(dbc, strDataA) +
//                      " AND (sk.skfpg_data_uscita IS NULL OR sk.skfpg_data_uscita >= " + formatDate(dbc, strDataDa) + ")" +
//			 **/
//			// 14/04/08: selezione solo dei contatti aperti nel periodo (x ridurre il num dei rec estratti) ----
//			" WHERE sk.skfpg_data_apertura <= " + formatDate(dbc, strDataA) +
//			" AND sk.skfpg_data_apertura >= " + formatDate(dbc, strDataDa) +
//			// 14/04/08 -----------
//			" AND pc.prc_tipo_op = '"+skfpg_tipo_operatore+"'"+
//			" AND pc.n_cartella = sk.n_cartella" +
//			" AND pc.prc_n_contatto = sk.n_contatto" +
//			" AND p.n_cartella = pc.n_cartella" +
//			" AND p.pr_data = pc.pr_data" +
//			" ORDER BY sk.skfpg_data_apertura";
//
//			printError("SkFpgEJB/query_contattiAperti: "+myselect);
//
//			dbcur = dbc.startCursor(myselect, 200); // 14/04/08
//			Vector vdbr=dbcur.getAllRecord();
//			dbcur.close();
//
//			// Decodifica in tutti gli ISASRecord del Vector
//			decodificaQueryContattiApertiInfo(dbc, vdbr);
//
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return vdbr;
//		} catch(Exception e)  {
//			e.printStackTrace();
//			throw new SQLException("Errore eseguendo la query_progettiAperti()  ");
//		}
//		finally  {
//			if(!done){
//				try{
//					if (dbcur != null)
//						dbcur.close();
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e1)
//				{LOG.error(e1);}
//			}
//		}
//	}

//	private void decodificaQueryContattiApertiInfo(ISASConnection mydbc, Vector vdbr) throws Exception
//	{
//		Hashtable htSesso = new Hashtable();
//		htSesso.put("F","Femmina");
//		htSesso.put("M","Maschio");
//
//		it.pisa.caribel.util.ISASUtil utl = new it.pisa.caribel.util.ISASUtil();
//
//		for (int i=0; i<vdbr.size(); i++) {
//			ISASRecord dbr = (ISASRecord) vdbr.get(i);
//			decodificaCartellaCognomeNome(mydbc, dbr, htSesso);
//
//			dbr.put("desc_val_ap", (String)util.getDecode(mydbc, "tab_voci", "tab_cod", "tab_val", "PRMOAP", (String)dbr.get("pr_motivo_val_ap"), "tab_descrizione"));
//			dbr.put("desc_val_ch", (String)util.getDecode(mydbc, "tab_voci", "tab_cod", "tab_val", "PRMOCH", (String)dbr.get("pr_motivi_val_ch"), "tab_descrizione"));
//
//			dbr.put("tipo_utente_decod", (String)util.getDecode(mydbc, "tipute_s", "codice", (String)dbr.get("skfpg_tipout"), "descrizione"));
//		}
//	}

//	private void decodificaCartellaCognomeNome(ISASConnection mydbc, ISASRecord dbr, Hashtable htSesso) throws Exception
//	{
//		String strNCartella = ((Integer) dbr.get("n_cartella")).toString();
//		String strCognome = "";
//		String strNome = "";
//		String strCodSesso = "";
//		java.sql.Date dateDataNascita = null;
//		String strCodComuneNascita = "";
//		String strDescrComuneNascita = "";
//
//		// 05/10/07 -----
//		String mot_chius = "";
//		java.sql.Date dtChius = null;
//		// 05/10/07 -----
//
//		// 14/11/07 ---
//		String strCodComuneDom = "";
//		String strDescrComuneDom = "";
//		String strIndirizzoDom = "";
//		// 14/11/07 ---
//
//		// 08/10/08
//		String codFisc = "";
//
//		if (strNCartella.equals("")) {
//			dbr.put("cognome", "");
//			dbr.put("nome", "");
//			dbr.put("sesso_decod", "");
//			dbr.put("data_nascita", "");
//			dbr.put("cod_comune_nascita", "");
//			dbr.put("comune_nascita_decod", "");
//
//			// 05/10/07 -----
//			dbr.put("motivo_chiusura", "");
//			dbr.put("data_chiusura", "");
//			// 05/10/07 -----
//
//			// 14/11/07 ---
//			dbr.put("dom_citta", "");
//			dbr.put("desc_dom_citta", "");
//			dbr.put("dom_indiriz", "");
//			// 14/11/07 ---
//
//			// 08/10/08
//			dbr.put("cod_fisc", "");
//
//			return;
//		}
//
//		String selS = "SELECT c.cognome, c.nome," +
//		" c.sesso," +
//		" c.data_nasc," +
//		" c.cod_com_nasc," +
//		" c.motivo_chiusura, c.data_chiusura," + // 05/10/07
//		" ac.dom_citta, ac.dom_indiriz," + // 14/11/07
//		" c.cod_fisc" + // 08/10/08
//		" FROM cartella c," +
//		" anagra_c ac" + // 14/11/07
//		" WHERE c.n_cartella = " + strNCartella +
//		" AND ac.n_cartella = c.n_cartella" + // 14/11/07
//		" AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
//		" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";
//
//		ISASRecord rec = mydbc.readRecord(selS);
//
//		if (rec != null) {
//			strCognome = ((String)rec.get("cognome")).trim();
//			strNome = ((String)rec.get("nome")).trim();
//			if (rec.get("sesso") != null)
//				strCodSesso = ((String)rec.get("sesso")).trim();
//			if (rec.get("data_nasc") != null)
//				dateDataNascita = (java.sql.Date)rec.get("data_nasc");
//			if (rec.get("cod_com_nasc") != null) 
//				strCodComuneNascita = (String) rec.get("cod_com_nasc");
//			// 05/10/07 -----
//			if (rec.get("motivo_chiusura") != null)
//				mot_chius = "" + rec.get("motivo_chiusura");
//			if (rec.get("data_chiusura") != null)
//				dtChius = (java.sql.Date)rec.get("data_chiusura");
//			// 05/10/07 -----
//			// 14/11/07 ---
//			if (rec.get("dom_citta") != null)
//				strCodComuneDom = (String) rec.get("dom_citta");
//			if (rec.get("dom_indiriz") != null)
//				strIndirizzoDom = (String) rec.get("dom_indiriz");
//			// 14/11/07 ---
//
//			// 08/10/08
//			if (rec.get("cod_fisc") != null)
//				codFisc = (String) rec.get("cod_fisc");
//		}
//
//		dbr.put("cognome", strCognome);
//		dbr.put("nome", strNome);
//		dbr.put("sesso", strCodSesso);
//		dbr.put("sesso_decod", (htSesso.get(strCodSesso)!=null?(String)htSesso.get(strCodSesso):""));
//		dbr.put("data_nascita", (java.sql.Date)dateDataNascita);
//		dbr.put("cod_comune_nascita", strCodComuneNascita);
//		dbr.put("comune_nascita_decod", (String)util.getDecode(mydbc, "comuni", "codice", (String)dbr.get("cod_comune_nascita"), "descrizione"));
//		// 05/10/07 -----
//		dbr.put("motivo_chiusura", mot_chius);
//		dbr.put("data_chiusura", dtChius);
//		// 05/10/07 -----
//
//		// 14/11/07 -----
//		dbr.put("dom_citta", strCodComuneDom);
//		dbr.put("desc_dom_citta", (String)util.getDecode(mydbc, "comuni", "codice", (String)dbr.get("dom_citta"), "descrizione"));
//		dbr.put("dom_indiriz", strIndirizzoDom);
//		// 14/11/07 -----
//
//		// 08/10/08
//		dbr.put("cod_fisc", codFisc);
//	}



	// 11/01/08
	private String leggiConf(ISASConnection mydbc, String cod) throws Exception
	{
		String desc = "";

		String selConf = "SELECT conf_txt" +
		" FROM conf" +
		" WHERE conf_kproc = 'SINS'" +
		" AND conf_key = '" + cod + "'";

		ISASRecord dbrDec = mydbc.readRecord(selConf);
		if (dbrDec != null)
			if (dbrDec.get("conf_txt") != null)
				desc = (String)dbrDec.get("conf_txt");

		return desc;
	}// END leggiConf


	// 15/07/08
	private String getZonaOper(ISASConnection mydbc, String oper) throws Exception
	{
		printError("SkFpgEJB getZonaOper oper == " + oper);
		String zonaOper = "";
		if ((oper != null) && (!oper.trim().equals(""))) {
			String sel = "SELECT cod_zona FROM operatori WHERE codice = '" + oper + "'";
			ISASRecord rec = mydbc.readRecord(sel);
			if ((rec != null) && (((String)rec.get("cod_zona")) != null))
			{	zonaOper = rec.get("cod_zona").toString();		}
		}
		
		
		return zonaOper;
	}

	// 15/07/08
	private String getValxZona(String val, String zonaOper)
	{
		String rit = ""; 
		// non esiste codifica per zona -> ritorno il valore cos� com'� letto,
		// visto che il valore � unico per tutte le zone.
		if (val.indexOf("#") == -1)
			return val;

		if ((zonaOper != null) && (!zonaOper.trim().equals(""))) {
			boolean trovato = false;
			String keyZona = zonaOper + "=";
			StringTokenizer strTkzZona = new StringTokenizer(val, "#");
			while ((strTkzZona.hasMoreTokens()) && (!trovato)) {
				String tkZona = strTkzZona.nextToken();
				int pos = tkZona.indexOf(keyZona);
				trovato = (pos != -1);
				if (trovato){
					rit = tkZona.substring(pos+zonaOper.length()+1);                    
				}
			}
		}
		if (rit.trim().equals(""))// non esiste codifica x la zona dell'oper (oppure oper senza zona!)
			printError("!!!! SkFpgEJB.getValxZona: NON esiste codifica su CONF per la zona=["+zonaOper+"] !!!!");
		return rit;
	}
	
	// 21/05/09 m.: lettura dtConclusione CASO precedente
	private String getDtChiuCasoPrec(ISASConnection dbc, Hashtable h) throws Exception 
	{
		String dtChiusPrec = "";
		// 11/06/10
		h.put("orig_caso_chiuso", new Integer(gestore_casi.CASO_UVM));
		
		ISASRecord lastCasoChiu = (ISASRecord)gestore_casi.getLastCasoChiuso(dbc, h);

		if ((lastCasoChiu != null) && (lastCasoChiu.get("dt_conclusione") != null))
			dtChiusPrec = "" + lastCasoChiu.get("dt_conclusione");
		printError("getDtChiuCasoPrec - dtChiusPrec=["+dtChiusPrec+"]");

		return dtChiusPrec;
	}

	// 26/03/10
	private String getTabVociCodReg(ISASConnection dbc, String tbCod, String tbVal) throws Exception
	{
		String codReg = "99";
		String sel = "SELECT tab_codreg FROM tab_voci" +
					" WHERE tab_cod = '" + tbCod + "'" +
					" AND tab_val = '" + tbVal + "'";
					
		ISASRecord dbr1 = dbc.readRecord(sel);
		if ((dbr1 != null) && (dbr1.get("tab_codreg") != null))
			codReg = (String)dbr1.get("tab_codreg");
		return codReg;
	}
	
	
	// 08/10/10 m
//	private void aggRpPresaCar(ISASConnection dbc, Hashtable h_dbr) throws Exception
//	{	
//		gestore_presacarico.aggRpPresaCarFromSkInf(dbc, h_dbr);	
//	}
	
	
	// 08/10/10 m
//	public Hashtable leggiLastSkInfInCaso(myLogin mylogin, Hashtable h_in) throws Exception
//	{	
//		boolean done = false;
//		ISASConnection dbc = null;
//		
//		Hashtable h_ret = new Hashtable();
//
//		try {
//			dbc = super.logIn(mylogin);
//			
//			String cart = "" + h_in.get("n_cartella");
//			String strDtSkVal = "" + h_in.get("pr_data");
//			String id_caso = "" + h_in.get("id_caso");
//			
//			String sel = "SELECT s.* FROM skfpg s"
//				+ " WHERE s.n_cartella = " + cart
//				+ " AND s.skfpg_data_apertura IN (SELECT MAX(a.skfpg_data_apertura)"
//						+ " FROM skfpg a"
//						+ " WHERE a.n_cartella = s.n_cartella"
//						+ " AND EXISTS (SELECT c.* FROM caso c"
//								+ " WHERE c.n_cartella = a.n_cartella"
//								+ " AND c.pr_data = " + dbc.formatDbDate(strDtSkVal)
//								+ " AND c.id_caso = " + id_caso
//								+ " AND c.dt_presa_carico <= a.skfpg_data_apertura"
//								+ " AND ((c.dt_conclusione IS NULL)"
//										+ " OR (c.dt_conclusione >= a.skfpg_data_apertura))"
//						+ ")"
//				+ ")";
//			printError("SkFpgEJB.leggiLastSkInfInCaso(): sel=["+sel+"]");
//			
//			ISASRecord dbr2 = dbc.readRecord(sel);
//			if (dbr2 != null) {
//				h_ret = (Hashtable)dbr2.getHashtable();
//				
//				java.sql.Date dtPresaCarSql = (java.sql.Date)dbr2.get("skfpg_dtpresacarico");
//				if (dtPresaCarSql != null) {
//					DataWI myDtPresaCar = new DataWI(dtPresaCarSql);	
//					h_ret.put("skfpg_dtpresacarico", (myDtPresaCar!=null?myDtPresaCar.getString(0):""));				
//				}
//			}
//			
//			dbc.close();
//			super.close(dbc);
//			done = true;
//
//			return h_ret;
//		} catch(Exception e1){
//			LOG.error("SkFpgEJB.leggiLastSkInfInCaso - Eccezione=[" + e1 + "]");
//			/**
//			LOG.error("***************************");
//			e1.printStackTrace();
//			LOG.error("***************************");
//			 **/
//			return (Hashtable)null;
//		} finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}
	
	

	private void printError(String msg)
	{
		if(mydebug)
			LOG.error("SkFpgEJB: " + msg);
	}
	
	
//	/**
//	 * elisa b 27/01/11
//	 * @param mylogin
//	 * @param h0
//	 * @return
//	 */
//	public Boolean checkAbilitazioneFlussi(myLogin mylogin, Hashtable h){
//		boolean done = false;
//		ISASConnection dbc = null;
//		boolean abilitazione = false;
//
//		try {
//			dbc = super.logIn(mylogin);
//
//			String n_cartella = h.get("n_cartella").toString();
//			//String data_apertura = h.get("data_apertura").toString();
//			String pr_data = h.get("pr_data").toString();
//			
//			String condizioneData = "";
//			if((h.containsKey("skfpg_data_uscita")) 
//					&& (!h.get("skfpg_data_uscita").toString().equals("")))
//				condizioneData = " AND pr_data_puac <= " + formatDate(dbc, h.get("skfpg_data_uscita").toString());
//
//			String myselect = "SELECT r.pr_tipologia FROM rl_puauvm r" +
//							" WHERE r.n_cartella = " + n_cartella +
//							" AND r.pr_data = " + formatDate(dbc, pr_data) +
//							" AND r.pr_data_puac IN (" +
//								" SELECT MAX(r1.pr_data_puac) FROM rl_puauvm r1" +
//								" WHERE r1.n_cartella = r.n_cartella" +
//								" AND r1.pr_data = r.pr_data" +
//								condizioneData + 
//								")";
//
//			printError("SkFpgEJB: checkAbilitazioneFlussi - myselect= " + myselect);
//
//			ISASRecord dbr = dbc.readRecord(myselect);
//			//i flussi sono abilitati solo se il campo tipologia vale 2 o 3
//			if((dbr != null) && (dbr.get("pr_tipologia") != null) &&
//			((dbr.get("pr_tipologia").toString().equals("2")) || (dbr.get("pr_tipologia").toString().equals("3"))))
//				abilitazione = true;
//			
//			dbc.close();	
//			super.close(dbc);
//			done = true;
//
//			return (new Boolean(abilitazione));
//			
//		} catch(Exception e1){
//			e1.printStackTrace();
//			//return (Boolean)null;
//			return null;
//		} finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}
	
	
	/**
	 * elisa b 23/02/11
	 * @param mylogin
	 * @param h0
	 * @return
	 */
	/*public String getTipologiaUV(myLogin mylogin, Hashtable h){ 
	 * mod elisa b 06/04/11: dalla scheda pua si leggono tipologia, segnalante e motivo*/
	public Hashtable getDatiRLPuaUvm(myLogin mylogin, Hashtable h){
		boolean done = false;
		ISASConnection dbc = null;
		Hashtable hR = new Hashtable();
		String tipologia = "";
		String segnalante = "";
		String motivo = "";

		try {
			dbc = super.logIn(mylogin);
			
			LOG.info("SkFpgEJB: getDatiRLPuaUvm " + h.toString());

			String n_cartella = h.get("n_cartella").toString();
			//String data_apertura = h.get("data_apertura").toString();
			String pr_data = h.get("pr_data").toString();
			
			
			if(pr_data.equals("/__/")){
//				// 26/04/11
//				ISASRecord dbrProg = cercaProgetto(dbc, h);
//				if ((dbrProg != null) && (dbrProg.get("pr_data") != null))
//					pr_data = (dbrProg.get("pr_data")).toString();
					
				//elisa b 19/04/11
				if ((pr_data.equals("")) || (pr_data.equals("/__/"))){//contatto non ancora aperto
					hR.put("tipologia", "");
					hR.put("segnalante", "");
					hR.put("motivo", "");
					return hR;
				}
			}
			
			String condizioneData = "";
			if((h.containsKey("data_chiusura")) 
					&& (!h.get("data_chiusura").toString().equals("")))
				condizioneData = " AND pr_data_puac <= " + formatDate(dbc, h.get("data_chiusura").toString());

			String myselect = "SELECT r.pr_tipologia, r.pr_segnalante, r.pr_motivo" +
							" FROM rl_puauvm r" +
							" WHERE r.n_cartella = " + n_cartella +
							" AND r.pr_data = " + formatDate(dbc, pr_data) +
							" AND r.pr_data_puac IN (" +
								" SELECT MAX(r1.pr_data_puac) FROM rl_puauvm r1" +
								" WHERE r1.n_cartella = r.n_cartella" +
								" AND r1.pr_data = r.pr_data" +
								condizioneData + 
								")";

			printError("SkFpgEJB: getDatiRLPuaUvm - myselect= " + myselect);

			ISASRecord dbr = dbc.readRecord(myselect);
			if(dbr != null){
				if(dbr.get("pr_tipologia") != null)
					tipologia = dbr.get("pr_tipologia").toString();
				if(dbr.get("pr_segnalante") != null)
					segnalante = dbr.get("pr_segnalante").toString();
				if(dbr.get("pr_motivo") != null)
					motivo = dbr.get("pr_motivo").toString();
			}
			hR.put("tipologia", tipologia);
			hR.put("segnalante", segnalante);
			hR.put("motivo", motivo);
			
			dbc.close();	
			super.close(dbc);
			done = true;

			//return (new Boolean(abilitazione));
			return hR;
		} catch(Exception e1){
			e1.printStackTrace();
			//return (Boolean)null;
			return null;
		} finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){LOG.error(e2);}
			}
		}
	}
	
//	private ISASRecord cercaProgetto(ISASConnection dbc, Hashtable h) throws Exception
//	{
//		if ((h.get("n_cartella") == null) || (h.get("data_apertura") == null))
//			return (ISASRecord)null;
//			
//		String cart = h.get("n_cartella").toString();		
//		String dtApe = h.get("data_apertura").toString();
//		
//		String sel = "SELECT * FROM progetto"
//					+ " WHERE n_cartella = " + cart
//					+ " AND pr_data IN (SELECT MAX(a.pr_data) FROM progetto a"
//						+ " WHERE a.n_cartella = " + cart
//						+ " AND a.pr_data <= " + formatDate(dbc, dtApe) 
//						+ " AND ((a.pr_data_chiusura IS NULL)"
//							+ " OR (a.pr_data_chiusura >= " + formatDate(dbc, dtApe) + "))"
//						+ ")";
//		
//		return (ISASRecord)dbc.readRecord(sel);
//	}
	
	// Aggiunto per prendere il tipo di modalit� del contatto (Domiciliare / Ambulatoriale)
//	public String getModalita (myLogin mylogin, Hashtable h)
//	{
//		ISASConnection dbc = null;
//		boolean done = false;
//		ISASRecord dbr = null;
//		String n_cartella = "";
//		String n_contatto = "";
//		String ret="";
//		
//		try 
//		{
//			dbc = super.logIn(mylogin);
//			n_cartella = (h.get("n_cartella")!=null?h.get("n_cartella").toString():"");
//			n_contatto = (h.get("n_contatto")!=null?h.get("n_contatto").toString():"");
//			if (n_cartella.equals("") || n_contatto.equals(""))
//			{
//			LOG.error("Eccezione in Skinf.getModalita(): Manca la chiave primaria");
//			dbc.close();
//			super.close(dbc);
//			return null;
//			}
//			String myselect = "select * from  skfpg where n_cartella = "+n_cartella+" and n_contatto="+n_contatto;
//			dbr = dbc.readRecord(myselect);
//			if (dbr!=null) ret = dbr.get("skfpg_modalita").toString();
//			else ret=null;
//			dbc.close();
//			super.close(dbc);
//			done=true;
//			return ret;
//		}catch (Exception e) {LOG.error("Eccezione in Skinf.getModalita(): "+e); return null;}
//		finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//		
//	}
	
	
	
//	/**
//	 * elisa b 03/06/11 (x il veneto): metodo che verifica la presenza di una risposta 
//	 * assistenziale aperta del tipo passato come parametro
//	 * @param mylogin
//	 * @param h
//	 * @return
//	 */
//	public Boolean checkEsistenzaRispostaAssistenziale(myLogin mylogin, Hashtable h){
//		boolean done = false;
//		ISASConnection dbc = null;
//		boolean rispEsistente = false;
//		String condPrData = "";
//
//		try {
//			dbc = super.logIn(mylogin);
//			
//			LOG.error("SkFpgEJB: checkEsistenzaRispostaAssistenziale " + h.toString());
//
//			String n_cartella = h.get("n_cartella").toString();
//			String pr_data = h.get("pr_data").toString();
//			String data_apertura = h.get("data_apertura").toString();
//			String tipo = decodificaTipoContatto( h.get("tipo_contatto").toString());
//			
//			/* elisa b 08/07/11 : se il metodo e' chiamato dalla scheda
//			 * autorizzazioni il campo pr_data non e' valorizzato */
//			if(!pr_data.equals(""))
//				condPrData = " AND pr_data = " + formatDate(dbc, pr_data);
//			
//			/* mod 23/08/11 : si si prende l'ultima valutazione valida alla 
//			 * data di apertura del contatto*/
//			 String myselect =  "SELECT p.*" +
//					" FROM rv_puauvm p" +
//					" WHERE p.n_cartella = " + n_cartella +
//					//" AND p.pr_data = " + formatDate(dbc, pr_data) + mod elisab
//					condPrData +
//					//" AND p.pr_data_puac <= " + formatDate(dbc, data_apertura) +
//					/*" AND p.pr_progr IN (SELECT MAX(a.pr_progr) FROM rv_puauvm a" + 
//						" WHERE a.n_cartella = p.n_cartella" +
//						" AND a.pr_data = p.pr_data)";*/
//					/* mod 23/08/11 si ordina in base alla data, non al progr
//					 * perche' si possono inserire anche schede con date
//					 * inferiori all'ultima */
//					" AND p.pr_data_puac IN (SELECT MAX(a.pr_data_puac)" +
//						" FROM rv_puauvm a" + 
//						" WHERE a.n_cartella = p.n_cartella" +
//						" AND a.pr_data = p.pr_data" +
//						" AND a.pr_data_puac <= " + formatDate(dbc, data_apertura) +
//					")";
//		
//			printError("SkFpgEJB: checkEsistenzaRispostaAssistenziale - myselect= " + myselect);
//
//	    	ISASCursor dbcur = dbc.startCursor(myselect);
//			while(dbcur.next()){
//				ISASRecord dbr = dbcur.getRecord();
//			
//				if(dbr != null){
//					String sel = "SELECT * FROM rv_puauvm_rispro" +
//								" WHERE n_cartella = " + n_cartella +
//								" AND pr_data = " + formatDate(dbc,  dbr.get("pr_data").toString()) +
//								" AND pr_progr = " + dbr.get("pr_progr") +
//								" AND risprocod in (" + tipo + ")" + //modificato in seguito a introduzione tabella operatori->risp. progr.
//								//" AND risprocod in  (select codice from rv_ope_rispro where tipo_op = '" + tipo + "' and abilitazione = 'S')" +  // modifica annullata su richiesta di artioli.
//								//25/07/11 la risposta deve essere precedente la data apertura
//								" AND dt_ini <= " + formatDate(dbc, data_apertura)+
//								" AND (dt_fine IS NULL" +
//									" OR dt_fine >= " + formatDate(dbc, data_apertura)+
//								")";
//					LOG.error("SkFpgEJB: checkEsistenzaRispostaAssistenziale - myselect=[" + sel + "]");
//					ISASRecord dbrRisp = dbc.readRecord(sel);
//					if(dbrRisp != null){
//						rispEsistente = true;
//						break;
//					}
//				}
//			}
//			
//			dbc.close();	
//			super.close(dbc);
//			done = true;
//
//			return (new Boolean(rispEsistente));
//		} catch(Exception e1){
//			e1.printStackTrace();
//			//return (Boolean)null;
//			return null;
//		} finally{
//			if(!done){
//				try{
//					dbc.close();
//					super.close(dbc);
//				}catch(Exception e2){LOG.error(e2);}
//			}
//		}
//	}
	
//	/**
//	 * elisa b 06/06/11
//	 * @param tipo
//	 * @return
//	 */
//	private String decodificaTipoContatto(String tipo){
//		String codice = "";
//		
//		if(tipo.equalsIgnoreCase("med"))
//			codice = "1";
//		else if(tipo.equalsIgnoreCase("inf"))
//			codice = "2";
//		else if(tipo.equalsIgnoreCase("fis"))
//			codice = "3";
//		else if(tipo.equalsIgnoreCase("soc"))
//			codice = "5,6";
//		else if(tipo.equalsIgnoreCase("pal"))
//			codice = "4";
//		
//		return codice;
//		
//	}
	
	
//	public ISASRecord getProgetto(ISASConnection dbc, String cartella, String dataRif) throws Exception
//	{
//		ISASRecord rec = null;
//
//		try {
//			String sel = " SELECT * FROM progetto WHERE n_cartella = " + cartella
//			+ " AND pr_data <= " + dbc.formatDbDate(dataRif)
//			+ " AND (pr_data_chiusura IS NULL " + " OR pr_data_chiusura >= " + dbc.formatDbDate(dataRif) + ")";
//
//			rec = dbc.readRecord(sel);
//
//			return rec;
//		} catch (Exception e) {
//			LOG.error("SkFpgEJB.getProgetto: ERRORE - e=" + e);
//			throw e;
//		}
//	}	
	
//	// 29/03/12: cntrl esistenza rec PRESACARICO
//	private boolean esistePresaCar(ISASConnection dbc, Hashtable h) throws Exception
//	{
//		ISASRecord recPC = (ISASRecord)gestore_presacarico.queryKey(dbc, h);
//		return (recPC != null);
//	}
	
	// 05/02/13
	private String decodPresidio(ISASConnection dbc, String codPres, String codOper) throws Exception
	{
		String ret = "";
		Hashtable h_conf = eveUtl.leggiConf(dbc, codOper, new String[]{"codice_regione", "codice_usl"});
			
		StringBuffer strBufSel = new StringBuffer("SELECT * FROM presidi WHERE codreg = '");
		strBufSel.append((String)h_conf.get("codice_regione"));
		strBufSel.append("' AND codazsan = '");
		strBufSel.append((String)h_conf.get("codice_usl"));
		strBufSel.append("' AND codpres = '");
		strBufSel.append(codPres);
		strBufSel.append("'");
			
		ISASRecord dbr = dbc.readRecord(strBufSel.toString());
		if ((dbr != null) && (dbr.get("despres") != null))
			ret = ((String)dbr.get("despres")).trim();
		return ret;
	}
	
	// 05/04/13
	public ISASRecord getSkInf(ISASConnection dbc, String cartella,String skfpg_tipo_operatore,String dataRif) throws Exception
	{
		ISASRecord rec = null;

		try {
			String sel = " SELECT * FROM skfpg WHERE n_cartella = " + cartella
					+ " AND skfpg_tipo_operatore = '"+skfpg_tipo_operatore+"'"
					+ " AND skfpg_data_apertura <= " + dbc.formatDbDate(dataRif)
			+ " AND ((skfpg_data_uscita IS NULL) OR (skfpg_data_uscita >= " + dbc.formatDbDate(dataRif) + "))";

			rec = dbc.readRecord(sel);

			return rec;
		} catch (Exception e) {
			LOG.error("SkFpgEJB.getSkInf: ERRORE - e=" + e);
			throw e;
		}
	}	

	// 26/06/13 
	public Boolean query_checkDtApeContLEMaxDtContChius(myLogin mylogin, Hashtable h0) throws Exception
	{
		ISASConnection dbc = null;
		String methodName = "query_checkDtApeContLEMaxDtContChius";
		boolean ret = false;
		try {
			dbc = super.logIn(mylogin);
			ret = dtApeContLEMaxDtContChius(dbc, h0);
			return new Boolean(ret);
		} catch(Exception e) {
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(methodName, dbc);
		}
	}
	public ISASRecord update(ISASConnection dbc,ISASRecord dbr) throws Exception{
		String n_cartella = null;
		String n_contatto = null;
		String skfpg_tipo_operatore = null;
		String data_apertura = null;
//		String dimissOsp = null;
//		String strDtSkVal = null;

		// 14/04/08: se configurato, permette la presenza di pi� contatti aperti contemporaneamente
		boolean multiCont = false;

		try 
		{
			//strDtSkVal = (String)dbr.get("pr_data");//gb 03/05/07

			n_cartella=dbr.get("n_cartella").toString();
			n_contatto=dbr.get("n_contatto").toString();
			skfpg_tipo_operatore=dbr.get("skfpg_tipo_operatore").toString();
			data_apertura=dbr.get("skfpg_data_apertura").toString();
//			dimissOsp=(String)dbr.get("dimiss");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SQLException("SkInf update: Errore: manca la chiave primaria", e);
		}
		Hashtable h = dbr.getHashtable();
		
		String skValDaChiudere = (String)h.get("skValDaChiudere");

		// 14/04/08: si esegue il ctrl solo se configurato per 1 solo contatto aperto  ---
		String abilMultiCont = (String)leggiConf(dbc, "ABIL_NEWCONT_INF");
		// 15/07/08 -----
		String zonaOper = getZonaOper(dbc, (String)dbr.get("skfpg_operatore"));
		abilMultiCont = getValxZona(abilMultiCont, zonaOper);
		// 15/07/08 -----
		multiCont = ((abilMultiCont != null) && (abilMultiCont.trim().equals("SI")));
		if (!multiCont) 
		{
			// 14/04/08 ---
			//gb 30/04/07 	        
			if(dtApeContLEMaxDtContChius(dbc, h))
			{
				String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
				throw new CariException(msg, -2);
			}
		}

		//gb 12/09/07 *************************
		String strMsgCheckDatePianoAssist = checkDateContEDatePianoAssist(dbc, h);
		if(!strMsgCheckDatePianoAssist.equals(""))
			throw new CariException(strMsgCheckDatePianoAssist, -2);
		//gb 12/09/07: fine *************************

		if ((gestore_casi.isUbicazRegMarche(dbc, h)).booleanValue()) {	
			if ((h.get("skfpg_descr_contatto") == null) || ("".equals((String)h.get("skfpg_descr_contatto"))))
					dbr.put("skfpg_descr_contatto", "Scheda n. "+dbr.get("n_contatto"));
		}
		// 18/03/11: solo per VENETO: se descr contatto NON valorizzata si mette un avlore default
		if ((gestore_casi.isUbicazRegVeneto(dbc, h)).booleanValue()) {	
			if ((h.get("skfpg_descr_contatto") == null) || ("".equals((String)h.get("skfpg_descr_contatto"))))
				dbr.put("skfpg_descr_contatto", "CONTATTO INFERMIERISTICO N."+dbr.get("n_contatto"));
		}			
		
		
		printError("SkInf.UPDATE: "+(dbr.getHashtable()).toString());
		dbc.writeRecord(dbr);

		String myselect="Select * from skfpg where "+
						" n_cartella="+n_cartella+
						" and n_contatto="+n_contatto+
						" and skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
		
		dbr = dbc.readRecord(myselect);

		if (dbr != null) {
			String data_chiusura="";
			if (dbr.get("skfpg_data_uscita")!=null)
				data_chiusura=((java.sql.Date)dbr.get("skfpg_data_uscita")).toString();
			
			if (data_chiusura != null && !data_chiusura.equals(""))
			{
				//bargi 16/04/2007
				//gb 01/10/07: Controlli e chiusure entit� sottostanti
				CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
				// date prestazioni erogate della tabella interv.
				// date aper. e date chius. dei piani assitenziali.
				// date aper. dei piani accessi.
				String strMsgCheckDtCh = clCcec.checkDtChDaContProgGTDtApeDtCh(dbc, n_cartella, n_contatto, data_chiusura, skfpg_tipo_operatore);
				if(!strMsgCheckDtCh.equals(""))
					throw new CariException(strMsgCheckDtCh, -2);
				
				// Chiusure entit� che stanno sotto il contatto:
				// Piani assistenziali
				// Piani accessi
				// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
				clCcec.chiudoDaContattoInGiu(dbc, n_cartella, n_contatto, data_chiusura, skfpg_tipo_operatore, (String)dbr.get("skfpg_operatore"));
				//gb 01/10/07: fine *******
				//chiudo piani assistenziali
				/*gb 01/10/07 *******
				//gb 12/09/07  AggiornaData("skiprogass", n_cartella,  n_contatto, "skpa_data_chiusura", data_chiusura, "skpa_data", dbc);
					   	AggiornaData("piano_assist", n_cartella,  n_contatto, "pa_data_chiusura", data_chiusura, "pa_data", dbc);
				
					   	//rimuovo da agenda appuntamenti caricati per la cartella chiusa
					   	rimuovoAgendaCaricata(n_cartella, n_contatto, data_chiusura, dbc);
				 *gb 01/10/07: fine *******/

//				// 12/10/07 m. ---
//				if ((skValDaChiudere != null) && (skValDaChiudere.trim().equals("S")))
//					chiudiSkValutaz(dbc, n_cartella, getProgetto(dbc, dbr.get("n_cartella").toString(),dbr.get("skfpg_tipo_operatore").toString(), dbr.get("skfpg_data_uscita").toString()), data_chiusura);   
//				// 12/10/07 m. ---
			}

			String selref="SELECT * FROM skfpg_referente WHERE "+
							"n_cartella="+n_cartella+" AND "+
							"n_contatto="+n_contatto+" AND "+
							"skfpg_tipo_operatore='"+skfpg_tipo_operatore+"'";
			
		
	
	ISASCursor dbcur = dbc.startCursor(selref);
	String infref=(String)dbr.get("skfpg_referente");
	String data_ref=((java.sql.Date)dbr.get("skfpg_referente_da")).toString();
	if (!dbcur.next())
	{
		if(dbr.get("skfpg_referente")!=null && !((String)dbr.get("skfpg_referente")).equals(""))
		{
			this.insertInfRef(dbc,infref,data_ref,data_apertura,n_cartella,n_contatto,skfpg_tipo_operatore);			}
	}
	
	// Aggiorno l'operatore referente nel caso in cui sia diverso da quello precedente, altrimenti ne inserisco uno nuovo.
	selref="SELECT * FROM skfpg_referente WHERE "+
			"n_cartella="+n_cartella+" AND "+
			"n_contatto="+n_contatto+" AND " +
			"skfpg_tipo_operatore='"+skfpg_tipo_operatore+"' AND "+
			"skfpg_referente_da="+formatDate(dbc, dbr.get("skfpg_referente_da").toString());
	ISASRecord dbr_ref = dbc.readRecord(selref);
	if (dbr_ref == null){
		this.insertInfRef(dbc,infref,data_ref,data_apertura,n_cartella,n_contatto,skfpg_tipo_operatore);		}
	else if (!dbr_ref.get("skfpg_referente").toString().equals(dbr.get("skfpg_referente").toString()))
	{
		dbr_ref.put("skfpg_infermiere",dbr.get("skfpg_referente").toString());
		dbc.writeRecord(dbr_ref);
	}
	
			
			dbcur.close();

		
			//gb 03/05/07: per rimandare indietro al client la data della scheda valutazione
//			dbr.put("pr_data", strDtSkVal);

			// 21/05/09 m. ------------------
			// lettura dtConclusione CASO precedente
			String dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			String tempoT = (String)h.get("tempo_t");

			// letture scale max
		  	gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, (String)h.get("pr_data"), 
									"", dtChiusCasoPrec, "", tempoT, skfpg_tipo_operatore);
			// 21/05/09 m. ------------------				

			int idCaso = -1;
			// 16/07/10: si comunicano solo i contatti DOMICILIARI
			if ((dbr.get("skfpg_modalita") != null) && ("1".equals((String)dbr.get("skfpg_modalita")))) {			
//				// 21/05/09 Elisa Croci
//				if(gestore_segnalazioni.isSegnalDaGestire(dbc, GestCasi.CASO_SAN))
//				{
//					idCaso = prendi_dati_caso(dbc, dbr);					
//					if (idCaso != -1)
//						gestione_segnalazione(dbc, dbr, h, "update");
//					else // 14/05/10
//						gestione_segnalazione(dbc, dbr, h, "insert");
//					
//					printError("Fatta gestione segnalazione per il caso: " + idCaso);						
//				} else { // 08/10/10 m: solo x PIEMONTE
///*** 26/06/13 m: NON SERVE PIU	***					
//					h.put("operZonaConf", (String)h.get("skfpg_operatore"));
//					if ((gestore_casi.isUbicazRegPiem(dbc, h)).booleanValue())							
//						aggRpPresaCar(dbc, (Hashtable)dbr.getHashtable());
//					h.put("origine", ""+GestCasi.CASO_SAN);
//					idCaso = (gestore_casi.getIdCasoOrigine(dbc, h)).intValue();
//*** 26/06/13 m: NON SERVE PIU ***/												
//					// 26/06/13
//					dbr.put("origine", ""+GestCasi.CASO_SAN);
//					idCaso = prendi_dati_casoOrigine(dbc, dbr);				
//				}
				
				if (data_chiusura != null && !data_chiusura.equals(""))
				{
					printError("Controllo contatto UNICO SANITARIO H == " + h.toString());
					boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true, true);
					if (idCaso != -1 && unico)
					{
						printError("Gestisco la chiusura del caso" );
						// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
						// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
						// sociale associato!
						int origine = -1;
						if(dbr.get("origine") != null && !(dbr.get("origine").toString()).equals(""))
							origine = Integer.parseInt(dbr.get("origine").toString());
						else if(h.get("origine") != null && !(h.get("origine").toString()).equals(""))
							origine = Integer.parseInt(h.get("origine").toString());
							
						// 26/06/13: verifico stato caso = ATTIVO
						int statoCaso = -1;
						if(dbr.get("stato") != null && !(dbr.get("stato").toString()).equals(""))
							statoCaso = Integer.parseInt(dbr.get("stato").toString());								
							
						if (origine != -1)
						{
							printError("update() -- Origine del caso: " + origine + " - stato = " + statoCaso);
							if ((origine == GestCasi.CASO_SAN)
							&& ((statoCaso == GestCasi.STATO_ATTIVO) 
								|| (((Boolean)gestore_casi.isUbicazRegTosc(dbc, h)).booleanValue())))
							{
								Hashtable hCaso = new Hashtable();
								hCaso.put("n_cartella", h.get("n_cartella"));
								hCaso.put("pr_data", h.get("pr_data"));
								hCaso.put("id_caso", new Integer(idCaso));
								hCaso.put("dt_conclusione", dbr.get("skfpg_data_uscita"));
//26/03/10								hCaso.put("motivo", "99");
								// 26/03/10 ----
								String motChiu = (String)h.get("skfpg_dimissioni");
								String motChiuFlux = getTabVociCodReg(dbc, "ICHIUS", motChiu);
								hCaso.put("motivo", motChiuFlux);
								// 26/03/10 ----										
								hCaso.put("operZonaConf", (String)dbr.get("skfpg_operatore")); // 15/10/09
								printError(" -- update(): Chiudi caso = HashCaso: " + hCaso.toString());
								Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
								printError("Ritorno di ChiudiCaso == " + r);
							}
						}
					}
				}					
			} else 
				LOG.info("--- SkFpgEJB.update: Contatto NON DOMICILIARE quindi NON si comunicano SEGNALAZ e PRESACAR ---");
			
			// 15/06/09 Elisa Croci  ***************************************************************
			if(h.containsKey("ubicazione") && dbr != null)
				dbr.put("ubicazione", h.get("ubicazione"));
			if(h.containsKey("update_segnalazione") && dbr != null)
				dbr.put("update_segnalazione", h.get("update_segnalazione"));
			// *************************************************************************************

			dbr.put("tempo_t", tempoT); // 21/05/09 m.
			
			gestisciDecodifiche(dbc, dbr);
			}

		// Simone 25/11/14 Aggiornamento id_skso su rm_diario
					if (h.get("id_skso")!=null&&!h.get("id_skso").toString().trim().equals("")){
						Hashtable h_rm_diario = (Hashtable)h.clone();
						h_rm_diario.put("tipo_operatore", h.get("skfpg_tipo_operatore"));
						h_rm_diario.put("id_skso", h.get("id_skso"));
						try {
							Boolean id_skso_updated = RMDiarioEJB.updateIdSkso(dbc, h_rm_diario);
							LOG.debug("Esito aggiornamento id_skso su diario = "+id_skso_updated.booleanValue());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						}
					
	return dbr;

	}

	public int recuperaMaxContatto(myLogin myLogin, Hashtable<String, String> dati) {
		String punto = ver + "recuperaMaxContatto ";
		int nContatto = -1;
		ISASConnection dbc = null;
		ISASRecord dbrSkpg = null;
		try {
			dbc = super.logIn(myLogin);
			String nCartella = ISASUtil.getValoreStringa(dati, CostantiSinssntW.N_CARTELLA);
			String tipoOperatore = ISASUtil.getValoreStringa(dati, CostantiSinssntW.CTS_SKFPG_TIPO_OPERATORE);
			
			String query = "SELECT MAX (n_contatto) AS contatto FROM skfpg WHERE n_cartella = " + nCartella +" AND skfpg_tipo_operatore = '" +
			tipoOperatore+ "' ";
			LOG.trace(punto + " query>>" + query);
			dbrSkpg = dbc.readRecord(query);

			nContatto = ISASUtil.getValoreIntero(dbrSkpg, "contatto");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			logout_nothrow(punto, dbc);
		}
		return nContatto;
	}

	public ISASRecord recuperaMaxSkFpg(ISASConnection dbc, String nCartella, String tipoOperatore) {
		String punto = ver + "recuperaMaxSkFpg ";
		ISASRecord dbrSkpg = null;
		try {
			String query = "SELECT k.* FROM skfpg k WHERE k.n_cartella = " +nCartella+ 
			" AND k.skfpg_tipo_operatore = '" +tipoOperatore + "' AND k.n_contatto IN (SELECT MAX (x.n_contatto) " +
			" FROM skfpg x WHERE x.n_cartella = k.n_cartella " +
			" and k.skfpg_tipo_operatore = x.skfpg_tipo_operatore ) "; 
			LOG.trace(punto + " query>>" + query);
			dbrSkpg = dbc.readRecord(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return dbrSkpg;
	}
}
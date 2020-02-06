package it.caribel.app.sinssnt.bean.modificati;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// 30/09/2002 --------EJB Scheda Medici----
// Jessica Caccavale
//16/04/2007 bargi alla chiusura del contatto devo chiudere anche 
//il piano assistenziale e relativi piani intervento
// ==========================================================================

import it.caribel.app.common.ejb.DiagnosiEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMDiarioEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMPuaUvmCommissioneEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOBaseEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.ScaleVal;
import it.caribel.app.sinssnt.controllers.contatto.CaribelContattoFormCtrl;
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
import it.pisa.caribel.operatori.GestTpOp;
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
import java.util.Vector;

import org.zkoss.util.resource.Labels;
// 07/08/07
//gb 01/10/07
//Elisa Croci 19/05/09

public class SkMedEJB extends SINSSNTConnectionEJB  
{
	// 07/08/07 m.: aggiunto "catch" e "throw" dell'eccezione ISASPermissionDenied
	//	anche sui metodi "queryProgettoCorrente()", "queryKey()".
	//	Nel metodo "query()": modificato "SELECT campo1, campo2,.." in "SELECT *"
	//	perche' vengano eseguiti i ctrl ISAS.

	//06/04/2007 bargi aggiunto data chiusura del progetto piano assistenziale

	// 22/11/06 m.: sostituito campi di SKPATOLOGIE con quelli della nuova tabella DIAGNOSI.
	// 31/10/06 m.: aggiunto ONCOLOGO nel metodo "deleteContsan()".


	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	// 07/08/07

	//05/09 Elisa Croci
	private GestCasi gestore_casi = new GestCasi();
	private GestSegnalazione gestore_segnalazioni = new GestSegnalazione();
	private GestPresaCarico gestore_presacarico = new GestPresaCarico();
	// 21/05/09 m.
	private ScaleVal gest_scaleVal = new ScaleVal(); 
	// 05/02/13
	private EveUtils eveUtl = new EveUtils();
	private boolean mydebug = true;

	private String SKM_MEDICO = "skm_medico";
	private String COD_OPERATORE = "cod_operatore";
	private String SKM_MEDICO_DA = "skm_medico_da";
	private String SKM_DESCR_CONTATTO = "skm_descr_contatto";
	private String SKM_INVIATO= "skm_inviato";
	private String SKM_TIPOUT = "skm_tipout";
	private String SKM_MOTIVO = "skm_motivo";
	private String SKM_COD_PRESIDIO = "skm_cod_presidio";
	private String SKM_DATA_APERTURA = "skm_data_apertura";
	
	private static final String ver = "9-";
	
	public SkMedEJB() {}

	public ISASRecord insert_terapia(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "insert_terapia ";
		LOG.debug(punto + " dati che esamino>>"+ (dbr!=null ? dbr.getHashtable()+"": " non dati "));
		Hashtable<String, Object>dati = convertiDati(dbr);
		return insert_terapia(mylogin, dbr.getHashtable());
	}
	
	private Hashtable<String, Object> convertiDati(ISASRecord dbr) {
		Hashtable<String, Object> datiConvertire = new Hashtable<String, Object>();
		datiConvertire.putAll(dbr.getHashtable());
		datiConvertire.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA));
		datiConvertire.put(CostantiSinssntW.N_CONTATTO, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CONTATTO));
		datiConvertire.put(CostantiSinssntW.SKT_PROGR, ISASUtil.getValoreStringa(dbr, CostantiSinssntW.SKT_PROGR));
		
		return datiConvertire;
	}

	
	public ISASRecord update_terapia(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//	public ISASRecord update_terapia(myLogin mylogin,Hashtable<String, Object> dati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String punto = ver + "insert_terapia ";
		LOG.debug(punto + " dati che esamino>>"+ (dbr!=null ? dbr.getHashtable()+"": " non dati "));
		Hashtable<String, Object>dati = convertiDati(dbr);
		return salva_terapia(mylogin, dati);
	}
	
	public void delete_terapia(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
//		public ISASRecord update_terapia(myLogin mylogin,Hashtable<String, Object> dati) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
			String punto = ver + "delete_terapia ";
			LOG.debug(punto + " dati che esamino>>"+ (dbr!=null ? dbr.getHashtable()+"": " non dati "));
			Hashtable<String, Object>dati = convertiDati(dbr);
			delete_terapia(mylogin, dati);
	}
	   
	public ISASRecord queryKeyTerapia(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = ver + "queryKeyTerapia ";
		ISASConnection dbc = null;
		ISASRecord dbrTerapia = null;
		String sktProgr =  ISASUtil.getValoreStringa(h, CostantiSinssntW.SKT_PROGR);
		LOG.info(punto + " inizio con dati>>" + (h != null ? h + "" : " no dati "));
		try {
			if (ISASUtil.valida(sktProgr)){
				dbc = super.logIn(mylogin);
				String myselect = "Select * from skmterapia where n_cartella=" + ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA)
						+ " and n_contatto=" + ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CONTATTO)
						+ " and skt_progr = " + ISASUtil.getValoreStringa(h, CostantiSinssntW.SKT_PROGR);
				LOG.trace(punto + " query>>" + myselect);
				dbrTerapia = dbc.readRecord(myselect);
			}

		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		return dbrTerapia;
	}
	
	
	public ISASRecord queryKey(myLogin mylogin,Hashtable h)
	throws SQLException, ISASPermissionDeniedException, CariException
	{
		String punto = ver + "queryKey ";
		// 07/08/07
		boolean done = false;
		String n_cartella = null;
		String n_contatto = null;
		String data_apertura = null;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;// 22/11/06 m.
		String dtAssistitoChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);

		try 
		{
			n_cartella=ISASUtil.getValoreStringa(h,"n_cartella");
			n_contatto=ISASUtil.getValoreStringa(h,"n_contatto");
			data_apertura=ISASUtil.getValoreStringa(h,"skm_data_apertura");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new SQLException("SkMed queryKey: Errore: manca la chiave primaria");
		}

		try
		{
			dbc=super.logIn(mylogin);
			String myselect = " SELECT m.* FROM skmedico m WHERE m.n_cartella= "+n_cartella;
			if (ISASUtil.valida(n_contatto)){
				myselect += " AND m.n_contatto="+n_contatto;
			}
			if (ManagerDate.validaData(dtAssistitoChiusura)){
				myselect +=" AND m.n_contatto IN (SELECT MAX (x.n_contatto) FROM skmedico x  WHERE x.n_cartella = m.n_cartella ) ";
			}
			
			//+
			//13-05-05 Commento voluto da Andrea
			//" AND m.skm_data_apertura="+formatDate(dbc,data_apertura);
			printError("select query_key su skmed==="+myselect);
			ISASRecord dbr = dbc.readRecord(myselect);

			String w_codice="";
			String w_descr="";
			String w_select="";

			ISASRecord w_dbr = null;
			if (dbr!= null) 
			{
				dbr = verificaEAggiornareIdSkso(dbc, dbr, myselect);
				
				n_contatto = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CONTATTO);
				Hashtable h1 = dbr.getHashtable();
				if (h1.get("cod_operatore")!=null && !((String)h1.get("cod_operatore")).equals(""))
				{
					dbr.put("desc_operat", decodifica("operatori","codice",h1.get("cod_operatore"),
							"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",dbc));
				}
				else dbr.put("desc_operat","");

				if (h1.get("skm_osp_dim")!=null && !((String)h1.get("skm_osp_dim")).equals(""))
				{
					dbr.put("des_osp", decodifica("ospedali","codosp",h1.get("skm_osp_dim"),"descosp",dbc));
				}
				else dbr.put("des_osp","");

				if (h1.get("skm_uo_dim")!=null && !((String)h1.get("skm_uo_dim")).equals(""))
				{
					dbr.put("des_rep", decodifica("reparti","cd_rep",h1.get("skm_uo_dim"),"reparto",dbc));
				}
				else dbr.put("des_rep","");

				if (h1.get("skm_medico")!=null && !((String)h1.get("skm_medico")).equals(""))
				{
					w_codice  = ISASUtil.getValoreStringa(h1,  "skm_medico"); //(String)h1.get("skm_medico");
					w_select = "SELECT * FROM operatori WHERE codice='"+w_codice+"'";
					w_dbr=dbc.readRecord(w_select);
					dbr.put("desc_inf", "");
					if (dbr!=null){
						dbr.put("desc_inf",ISASUtil.getValoreStringa(w_dbr,"cognome")+" "+ ISASUtil.getValoreStringa(w_dbr,"nome"));
					}
				}
				else dbr.put("desc_inf","");

				/*** 22/11/06 m.
                 String pato="SELECT pat.* FROM skpatologie pat WHERE"+
                            " pat.n_cartella="+n_cartella+
                            " AND pat.n_contatto="+n_contatto;
                ISASRecord dbrpato=dbc.readRecord(pato);
                if (dbrpato!=null){
                   if (dbrpato.get("skpat_patol1")!=null && !((String)dbrpato.get("skpat_patol1")).equals("")){
                      dbr.put("skpat_patol1",dbrpato.get("skpat_patol1"));
                      dbr.put("des1", decodifica("icd9","cd_diag",dbrpato.get("skpat_patol1"),
                                                 "diagnosi",dbc));
                   }else dbr.put("des1","");
                   if (dbrpato.get("skpat_patol2")!=null && !((String)dbrpato.get("skpat_patol2")).equals("")){
                        dbr.put("skpat_patol2",dbrpato.get("skpat_patol2"));
                        dbr.put("des2", decodifica("icd9","cd_diag",dbrpato.get("skpat_patol2"),
                                                   "diagnosi",dbc));
                   }else dbr.put("des2","");
                   if (dbrpato.get("skpat_patol3")!=null && !((String)dbrpato.get("skpat_patol3")).equals("")){
                        dbr.put("skpat_patol3",dbrpato.get("skpat_patol3"));
                        dbr.put("des3", decodifica("icd9","cd_diag",dbrpato.get("skpat_patol3"),
                                                   "diagnosi",dbc));
                   }else dbr.put("des3","");
                  if (dbrpato.get("skpat_patol4")!=null && !((String)dbrpato.get("skpat_patol4")).equals("")){
                      dbr.put("skpat_patol4",dbrpato.get("skpat_patol4"));
                      dbr.put("des4", decodifica("icd9","cd_diag",dbrpato.get("skpat_patol4"),
                                                 "diagnosi",dbc));
                   }else dbr.put("des4","");
                 dbr.put("skpat_conf_med",dbrpato.get("skpat_conf_med"));
                }
				 ****/

				// 22/11/06 m.
				leggiDiagnosi(dbc, dbr);

				// 05/02/13 
				dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));				
				
				/*** 07/08/07 m.: piano assistenziale scorporato dal contatto
				//06/04/2007 bargi ,mepa_data_chiusura 
                String myprest="SELECT mepa_data,mepa_progetto,mepa_data_chiusura FROM medprogass WHERE "+
                               "n_cartella="+(String)h.get("n_cartella")+" and "+
			       "n_contatto="+(String)h.get("n_contatto")+
                               " ORDER BY mepa_data";;
                ISASCursor cur_ass=dbc.startCursor(myprest);
                Vector dbass=cur_ass.getAllRecord();
                dbr.put("griglia_ass",dbass);
                //Jessy 26-06-05
                cur_ass.close();
				 ***/

//				String selg = "Select * from skmterapia where "+
//				" n_cartella="+(String)h.get("n_cartella")+
//				" and n_contatto="+(String)h.get("n_contatto")+
//				" ORDER BY skt_progr";
				String selg = "Select * from skmterapia where n_cartella="+n_cartella+
						" and n_contatto="+n_contatto+ " ORDER BY skt_progr";
				LOG.trace(punto + " query>>" + selg);
				ISASCursor dbgriglia = dbc.startCursor(selg);
				Vector vdbg = dbgriglia.getAllRecord();
				dbr.put("griglia",vdbg);

				//Jessy 26-06-05
				dbgriglia.close();

				// 20/05/09 Elisa Croci
				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SAN))
				{
					// 15/06/09 Elisa Croci    ********************************************************
					if(h.containsKey("ubicazione") && h.get("ubicazione") != null)
						dbr.put("ubicazione", h.get("ubicazione"));
					if(h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
						dbr.put("update_segnalazione", h.get("update_segnalazione"));
					// *********************************************************************************
					
					int caso = prendi_dati_caso(dbc,dbr);
					if(prendi_segnalazione(dbc,caso,dbr))
						prendi_presacarico(dbc,caso,dbr);
				}
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{		
			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
		}
		catch(Exception e) 
		{
			e.printStackTrace();
			System.out.println("SkMedEJB.queryKey(): "+e);
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
				catch(Exception e2){System.out.println(e2);}
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
			String skiDataApertura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SKM_DATA_APERTURA);
			  
			ISASRecord dbrRmSkSoMMG;  
			try {
				dbrRmSkSoMMG = rmSkSOEJB.recuperaRmSksoMmg(dbc, nCartella, skiDataApertura);
				idSkso = ISASUtil.getValoreStringa(dbrRmSkSoMMG, CostantiSinssntW.CTS_ID_SKSO);
				if (ISASUtil.valida(idSkso)){
					dbr.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
					dbc.writeRecord(dbr);
					LOG.trace(punto + " query per rileggere il record aggiornato \n:"+query);
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

	// 07/08/07
	public ISASRecord getContattoMedCorrente(myLogin mylogin, Hashtable h) throws SQLException, ISASPermissionDeniedException, CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null; // 07/08/07

		String n_cartella = (String)h.get("n_cartella");
		String strDtSkVal_ = (String)h.get("pr_data");// 07/08/07

		try
		{
			// 07/08/07 ---
			/*
			 * if (strDtSkVal == null)
			{
				printError("\nSkMedEJB -->> getContattoMedCorrente: dataSkVal NULLA!!");
				done = true;
				return dbr;
			}
			 */
			// 07/08/07 ---

			// Ottengo la connessione al database
			dbc = super.logIn(mylogin);

			// Preparo la SELECT del record
			String myselect = "SELECT * FROM skmedico" +
			" WHERE n_cartella = " + n_cartella +
			" AND skm_data_chiusura IS NULL";

			printError("SkMedEJB/getContattoMedCorrente: " + myselect);
			dbr = dbc.readRecord(myselect);

			String w_codice="";
			String w_descr="";
			String w_select="";
			ISASRecord w_dbr=null;

			if (dbr != null) 
			{
				Hashtable h1 = dbr.getHashtable();
				if (h1.get("cod_operatore")!=null && !((String)h1.get("cod_operatore")).equals(""))
				{
					dbr.put("desc_operat", decodifica("operatori","codice",h1.get("cod_operatore"),
							"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",dbc));
				}
				else dbr.put("desc_operat","");

				if (h1.get("skm_osp_dim")!=null && !((String)h1.get("skm_osp_dim")).equals(""))
				{
					dbr.put("des_osp", decodifica("ospedali","codosp",h1.get("skm_osp_dim"),"descosp",dbc));
				}
				else dbr.put("des_osp","");

				if (h1.get("skm_uo_dim")!=null && !((String)h1.get("skm_uo_dim")).equals(""))
				{
					dbr.put("des_rep", decodifica("reparti","cd_rep",h1.get("skm_uo_dim"),"reparto",dbc));
				}
				else dbr.put("des_rep","");

				if (h1.get("skm_medico")!=null && !((String)h1.get("skm_medico")).equals(""))
				{
					w_codice  = (String)h1.get("skm_medico");
					w_select = "SELECT * FROM operatori WHERE codice='"+w_codice+"'";
					w_dbr=dbc.readRecord(w_select);
					if (w_dbr!=null)
					dbr.put("desc_inf", w_dbr.get("cognome")+" "+w_dbr.get("nome"));
					
				}
				else dbr.put("desc_inf","");

				// 07/12/06 m.
				leggiDiagnosi(dbc, dbr);

				String selg="Select * from skmterapia where "+
				" n_cartella="+n_cartella+
				" and n_contatto="+dbr.get("n_contatto")+
				" ORDER BY skt_progr";

				ISASCursor dbgriglia=dbc.startCursor(selg);
				Vector vdbg=dbgriglia.getAllRecord();
				dbr.put("griglia",vdbg);

				//Jessy 26-06-05
				dbgriglia.close();

//				dbr.put("pr_data", strDtSkVal);// 07/08/07
				
				// 05/02/13 
				dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));					

				// 05/09 Elisa Croci
//				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SAN))
//				{
//					// 15/06/09 Elisa Croci    ********************************************************
//					if(h.containsKey("ubicazione") && h.get("ubicazione") != null)
//						dbr.put("ubicazione", h.get("ubicazione"));
//					if(h.containsKey("update_segnalazione") && h.get("update_segnalazione") != null)
//						dbr.put("update_segnalazione", h.get("update_segnalazione"));
//					// *********************************************************************************
//					
//					int caso = prendi_dati_caso(dbc,dbr);
//					if(prendi_segnalazione(dbc,caso,dbr))
//						prendi_presacarico(dbc,caso,dbr);
//				}
			}

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		// 07/08/07 ---
		catch(ISASPermissionDeniedException e1){
			System.out.println("SkMedEJB.getContattoMedCorrente(): "+e1);
			throw new CariException(CostantiSinssntW.MSG_ECCEZIONE_DIRITTI_MANCANTI, -2);
		}
		// 07/08/07 --- 
		catch(Exception e){
			e.printStackTrace();
			e.printStackTrace();
			throw new SQLException("SkMedEJB.getContattoMedCorrente(): "+e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}



	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException, ISASPermissionDeniedException { // 07/08/07
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String myselect="SELECT * FROM skmedico WHERE n_cartella="+
			(String)h.get("n_cartella")+" ORDER BY skm_data_apertura DESC ";
			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
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
	}

	// 07/08/07: caricamento della grid della frame "JFrameGridSkMed"
	public Vector query_loadGridSkMed(myLogin mylogin,Hashtable h) throws  SQLException, ISASPermissionDeniedException 
	{
		boolean done=false;
		ISASConnection dbc=null;

		String strNAssistito = (String) h.get("n_cartella");
		String strDtSkVal = (String)h.get("pr_data");// 26/10/06

		Vector vdbr = new Vector();

		try{
			// 26/10/06 ---
			if (strDtSkVal == null){
				printError("\nSkMedEJB -->> query_loadGridSkMed: dataSkVal NULLA!!");
				done = true;
				return vdbr;
			}
			// 26/10/06 ---

			// Connessione al database
			dbc=super.logIn(mylogin);

			// Compongo la SELECT
			String myselect = "SELECT skm.*" +
			" FROM skmedico skm" +
//			"," +
//			" progetto_cont pc" + // 26/10/06
			" WHERE skm.n_cartella = " + strNAssistito +
			" AND skm.skm_data_chiusura IS NOT NULL" +
			// 26/10/06 : x estrarre solo quelli collegati ad una scheda valutaz
//			" AND pc.prc_tipo_op = '03'" +
//			" AND pc.n_cartella = skm.n_cartella" +
//			" AND pc.pr_data = " + formatDate(dbc, strDtSkVal) +
//			" AND pc.prc_n_contatto = skm.n_contatto" +
			// 26/10/06 --------------------------------------------------------
			" ORDER BY skm.skm_data_apertura, skm.skm_data_chiusura";

			printError("-->>query GridSkMed: "+myselect);
			ISASCursor dbcur=dbc.startCursor(myselect);
			vdbr=dbcur.getAllRecord();
			decodificaQueryInfo(dbc, vdbr);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		} catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo la query_loadGridSkMed()  ");
		} finally {
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	// 07/08/07
	private void decodificaQueryInfo(ISASConnection mydbc, Vector vdbr) throws Exception
	{
		for (int i=0; i<vdbr.size(); i++){
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			decodificaQueryOperatore(mydbc, dbr, "cod_operatore", "operatore_apertura");
			decodificaQueryOperatore(mydbc, dbr, "skm_medico", "operatore_referente");
		}
	}

	// 07/08/07 
	private void decodificaQueryOperatore(ISASConnection mydbc, ISASRecord dbr,
			String dbFldNameCod, String dbName) throws Exception
			{
		String strCodOperatore = (String) dbr.get(dbFldNameCod);

		String strCognome = "";
		String strNome = "";

		if (strCodOperatore == null) {
			dbr.put(dbName, "");
			return;
		}
		String selS = "SELECT cognome, nome FROM operatori" +
		" WHERE codice = '" + strCodOperatore + "'";

		ISASRecord rec = mydbc.readRecord(selS);

		if (rec != null) {
			if (rec.get("cognome") != null)
				strCognome = (String)rec.get("cognome");
			if (rec.get("nome") != null)
				strNome = (String)rec.get("nome");
		}
		dbr.put(dbName, strCognome + " " + strNome);
			}



	public Vector query_terapia(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		String punto = ver + "query_terapia ";
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skmterapia where "+
			" n_cartella="+(String)h.get("n_cartella")+
			" and n_contatto="+(String)h.get("n_contatto")+
			" ORDER BY skt_progr DESC ";
			LOG.debug(punto + " query>>"+myselect);
			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_terapia()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}

	}

	public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, 
	ISASPermissionDeniedException, SQLException, CariException // 07/08/07
	{
		boolean done=false;
		ISASConnection dbc=null;
		ISASCursor dbcur = null;// 22/11/06 m.
		try{
			dbc=super.logIn(mylogin);	
			dbc.startTransaction();

			ISASRecord dbr = insertTransactional(dbc, h);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		// 07/08/07 **************
		catch(CariException ce){
			ce.setISASRecord(null);
			try{
				System.out.println("SkMedEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}
		// *************************
		catch(DBRecordChangedException e){
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
				throw new SQLException("Errore eseguendo una rollback() - ",  ex);
			}
			
			throw new SQLException("Errore eseguendo una insert() - ",  e1);
		}finally{
			if(!done){
				try{
					if (dbcur != null) // 22/11/06 m.
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
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
	public ISASRecord insertTransactional(ISASConnection dbc, Hashtable h) throws SQLException, Exception,
	CariException, ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException,
	DBRecordChangedException {
		String punto = ver +"insert ";
		String n_cartella=null;
		String n_contatto=null;
		String data_apertura=null;
		boolean abilitatoProgetto = false;

		try{
			n_cartella = h.get(Costanti.N_CARTELLA).toString();
			n_contatto = h.get(Costanti.N_CONTATTO).toString();
			data_apertura = h.get(SKM_DATA_APERTURA ).toString();
		}catch (Exception e){
			throw new SQLException("SkMed insert: Errore: manca la chiave primaria", e);
		}
		//		String strDtSkVal = (String)h.get("pr_data");// 07/08/07
		String strDtSkVal = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_SKM_DATA_APERTURA);// 07/08/07

		if (abilitatoProgetto){
			// 07/08/07 *************************
			if(dtApeContLEMaxDtContChius(dbc, h))
			{
				String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
				throw new CariException(msg, -2);
			}
			// ************************************
		}

		// 07/08/07: si ottiene il nuovo progressivo (non si usa piï¿½ CONTSAN).
		int intProgressivo = getProgressivo(dbc, n_cartella);
		Integer iProgressivo = new Integer(intProgressivo);
		n_contatto = iProgressivo.toString();
		// ************************************

		ISASRecord dbr=dbc.newRecord("skmedico");
		//INSERISCO NELLA TABELLA DEL MEDICO
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}

		// 07/08/07: Si setta il campo 'n_contatto' col nuovo progressivo
		dbr.put("n_contatto",iProgressivo);

		dbc.writeRecord(dbr);
		if (abilitatoProgetto){
			// 07/08/07: scrittura su PROGETTO_CONT ed, eventualmente, PROGETTO -----
			if (strDtSkVal == null) {
				ISASRecord progetto = getProgetto(dbc, n_cartella, dbr.get("skm_data_apertura").toString());
				if (progetto == null){				
					strDtSkVal = scriviProgetto(dbc, n_cartella, strDtSkVal);					
				}else 
					strDtSkVal = progetto.get("pr_data").toString();
				//gb 02/11/06
				// Mettere controllo che data_ape sk_valutaz. fittizia sia >=
				// data chiusura di ultima sk_valutaz. chiusa pre-esistente.
				if (dtApeMinoreMaxDtChius(dbc, n_cartella, strDtSkVal)){
					String msg = "Attenzione: Data apertura antecedente a data chiusura di ultima Scheda valutazione chiusa!";
					throw new CariException(msg, -2);
				}
				//gb 01/06/07: Controllo che la data di apertura del contatto (skm_data_apertura)
				//		sia >= data_apetura della tab. cartella.

				if (dtApeContattoLTDtApeCartella(dbc, n_cartella, strDtSkVal))  
				{
					String msg = "Attenzione: Data apertura contatto e' antecedente alla data apertura dell'assistito!";
					throw new CariException(msg, -2);
				}
			}
			//scriviProgettoCont(dbc, n_cartella, strDtSkVal, "03", n_contatto);
			// 07/08/07 -------------------------------------------------------------
		}

		String myselect="SELECT * FROM skmedico WHERE n_cartella="+n_cartella+
				" AND n_contatto="+n_contatto+
				" AND skm_data_apertura="+formatDate(dbc,data_apertura);
		dbr=dbc.readRecord(myselect);

		if (dbr != null) {
			String descr_medico=(String)dbr.get("skm_descr_contatto");
			String data_chiusura="";

			if (dbr.get("skm_data_chiusura")!=null)
				data_chiusura=((java.sql.Date)dbr.get("skm_data_chiusura")).toString();

			String selref="SELECT * FROM skmed_referente WHERE "+
					"n_cartella="+n_cartella+" AND "+
					"n_contatto="+n_contatto;
			//"ski_data_apertura="+formatDate(dbc,data_apertura)
			ISASRecord dbref=dbc.readRecord(selref);
			if(dbref==null){
				if(dbr.get("skm_medico")!=null && !((String)dbr.get("skm_medico")).equals("")){
					String infref=(String)dbr.get("skm_medico");
					String data_ref="";

					if (dbr.get("skm_medico_da")!=null)
						data_ref=((java.sql.Date)dbr.get("skm_medico_da")).toString();

					this.insertMedRef(dbc,infref,data_ref,data_apertura,n_cartella,n_contatto);
				}
			}

			/** 07/08/07: eliminato tabella CONTSAN
			String operatore=(String)dbr.get("cod_operatore");
			String selcont="SELECT * FROM contsan where n_cartella="+n_cartella+
			               " AND n_contatto="+n_contatto;
			ISASRecord dbcont=dbc.readRecord(selcont);
			if(dbcont!=null)
			  this.updateContsan(dbc,data_apertura,data_chiusura,descr_medico,n_cartella,n_contatto);
			else
			  this.insertContsan(dbc,data_apertura,data_chiusura,descr_medico,n_cartella,operatore);
			 ***/

			// 22/11/06 m.
			leggiDiagnosi(dbc, dbr);

			// 07/08/07
			dbr.put("pr_data", strDtSkVal);

			// 21/05/09 m. ------------------
			h.put("pr_data", strDtSkVal);
			String dtChiusCasoPrec = "";
			if (abilitatoProgetto){
				// lettura dtConclusione CASO precedente
				dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);
			}
			String tempoT = (String)h.get("tempo_t");

			// letture scale max
			gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, strDtSkVal, 
					"", dtChiusCasoPrec, "", tempoT, "03");
			if (abilitatoProgetto){
				// 21/05/09 m. ------------------
				int idCaso=-1;
				// 21/05/09 Elisa Croci
				if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SAN)){
					idCaso = prendi_dati_caso(dbc, dbr);
					gestione_segnalazione(dbc, dbr, h, "insert");

					printError("Fatta gestione segnalazione per il caso: " + idCaso);
				}else{
					h.put("operZonaConf", (String)h.get("cod_operatore"));
					if ((gestore_casi.isUbicazRegPiem(dbc, h)).booleanValue())							
						aggRpPresaCar(dbc, (Hashtable)dbr.getHashtable());
					h.put("dt_segnalazione",data_apertura);
					h.put("dt_presa_carico",data_apertura);
					h.put("origine", ""+GestCasi.CASO_SAN);
					idCaso = gestore_casi.apriCasoSan(dbc,h).intValue();
				}
				// 15/06/09 Elisa Croci      ***************************************************************
				if (data_chiusura != null && !data_chiusura.equals("")){
					printError("Controllo contatto UNICO SANITARIO H == " + h.toString());
					boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true, true);
					if (idCaso != -1 && unico){
						printError("Gestisco la chiusura del caso" );
						// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
						// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
						// sociale associato!
						int origine = -1;
						if(dbr.get("origine") != null && !(dbr.get("origine").toString()).equals(""))
							origine = Integer.parseInt(dbr.get("origine").toString());
						else if(h.get("origine") != null && !(h.get("origine").toString()).equals(""))
							origine = Integer.parseInt(h.get("origine").toString());
						if (origine != -1){
							printError("Origine del caso: " + origine);
							if(origine == GestCasi.CASO_SAN){
								Hashtable hCaso = new Hashtable();
								hCaso.put("n_cartella", h.get("n_cartella"));
								hCaso.put("pr_data", h.get("pr_data"));
								hCaso.put("id_caso", new Integer(idCaso));
								hCaso.put("dt_conclusione", dbr.get("skm_data_chiusura"));
								// 26/03/10								hCaso.put("motivo", "99");
								// 26/03/10 ----
								String motChiu = (String)h.get("skm_motivo_chius");
								String motChiuFlux = getTabVociCodReg(dbc, "MCHIUS", motChiu);
								hCaso.put("motivo", motChiuFlux);
								// 26/03/10 ----
								hCaso.put("operZonaConf", (String)dbr.get("cod_operatore")); // 15/10/09
								printError(" -- update(): Chiudi caso = HashCaso: " + hCaso.toString());
								Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
								printError("Ritorno di ChiudiCaso == " + r);
							}
						}
					}
				}
				// ****************************************************************************************

				// 15/06/09 Elisa Croci  ***************************************************************
				if(h.containsKey("ubicazione") && dbr != null)
					dbr.put("ubicazione", h.get("ubicazione"));
				if(h.containsKey("update_segnalazione") && dbr != null)
					dbr.put("update_segnalazione", h.get("update_segnalazione"));
				// *************************************************************************************
			}
			dbr.put("tempo_t", tempoT); // 21/05/09 m.
			// 05/02/13 
			dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));					

			LOG.trace(punto + " restituito === " + dbr.getHashtable().toString());
		}

		// Simone 25/11/14 Aggiornamento id_skso su rm_diario
		if (h.get("id_skso")!=null&&!h.get("id_skso").toString().trim().equals("")){
			Hashtable h_rm_diario = (Hashtable)h.clone();
			h_rm_diario.put("tipo_operatore", CostantiSinssntW.TIPO_OPERATORE_MEDICO);
			h_rm_diario.put("id_skso", h.get("id_skso"));
			try {
				Boolean id_skso_updated = RMDiarioEJB.updateIdSkso(dbc, h_rm_diario);
				LOG.debug("Esito aggiornamento id_skso su diario = "+id_skso_updated.booleanValue());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ISASUtil.checkUnicoContattoAperto(dbc, h, n_contatto, "skmedico", "skm_data_chiusura", Labels.getLabel("contatti.msg.contattoUnicoApertoViolato"));
		
		return dbr;
	}
	
	public ISASRecord getProgetto(ISASConnection dbc, String cartella, String dataRif) throws Exception
	{
		ISASRecord rec = null;

		try {
			
			String sel = " SELECT * FROM progetto WHERE n_cartella = " + cartella
			+ ((dataRif != null)?" AND pr_data <= " + dbc.formatDbDate(dataRif):"")
			+ ((dataRif != null)?" AND (pr_data_chiusura IS NULL " + " OR pr_data_chiusura >= " + dbc.formatDbDate(dataRif) + ")":"");

			rec = dbc.readRecord(sel);

			return rec;
		} catch (Exception e) {
			LOG.error("SkMedEJB.getProgetto: ERRORE - e=" + e);
			throw e;
		}
	}	
	// 07/08/07
	private boolean dtApeContattoLTDtApeCartella(ISASConnection dbc, String strNAssistito, String strDtApeCont) throws Exception
	{
		String mySel = "SELECT *" +
		" FROM cartella" +
		" WHERE n_cartella = " + strNAssistito +
		" AND data_apertura > " + formatDate(dbc, strDtApeCont);

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile
		else
			return true;
	}

	// 07/08/07
	private boolean dtApeMinoreMaxDtChius(ISASConnection dbc, String strNAssistito, String strDtSkVal) throws Exception
	{
		String dt = strDtSkVal;
		// 25/06/07  dt = dt.substring(0,2) + dt.substring(3,5) + dt.substring(6,10);
		dt=dt.substring(8,10) + dt.substring(5,7) + dt.substring(0,4);
		DataWI dataWIApertura = new DataWI(dt);

		String mySel = "SELECT MAX(pr_data_chiusura) max_data_chius" +
		" FROM progetto" +
		" WHERE n_cartella = " + strNAssistito +
		" AND pr_data_chiusura IS NOT NULL";

		ISASRecord rec = dbc.readRecord(mySel);
		if (rec == null)
			return false; // Ammissibile

		if ((java.sql.Date)rec.get("max_data_chius") == null)
			return false; // Ammissibile

		dt = ((java.sql.Date)rec.get("max_data_chius")).toString();
		if (dt.equals(""))
			return false; // Ammissibile

		dt=dt.substring(0,4) + dt.substring(5,7) + dt.substring(8,10);
		String max_data_chiusura = dt;
		int rit = dataWIApertura.confrontaConDt(max_data_chiusura);
		// Codici ritornati da confrontaConDt:
		// se data_apertura ï¿½ maggiore di data_chiusura restituisce 1
		// se data_apertura ï¿½ minore di data_chiusura restituisce 2
		// se data_apertura ï¿½ = di data_chiusura restituisce 0
		// se da errore -1
		if ((rit == 2) || (rit == 0))
			return true; // Non ammissibile
		else if (rit < 0){
			throw new Exception("SkMedEJB/dtApeMinoreMaxDtChius: Errore in confronto date");
			// Si ï¿½ verificato un errore nel metodo di confronto delle 2 date.
		} else // (rit == 1)
			return false; // Ammissibile
	}

	// 07/08/07
	// 13/09/07 m.: aggiunto crit su contatto diverso da quello in oggetto.
	private boolean dtApeContLEMaxDtContChius(ISASConnection dbc, Hashtable h) throws Exception
	{
//		String strNCartella = (String) h.get("n_cartella");
//		String strDataApeContatto = (String) h.get("skm_data_apertura");
//		String strNContatto = (String) h.get("n_contatto"); // 13/09/07 m.
		String strNCartella = ISASUtil.getValoreStringa(h, "n_cartella");
		String strDataApeContatto = ISASUtil.getValoreStringa(h, "skm_data_apertura");
		String strNContatto = ISASUtil.getValoreStringa(h, "n_contatto"); 

		String mySel = "SELECT skm_data_chiusura" +
		" FROM skmedico" +
		" WHERE n_cartella = " + strNCartella +
		(strNContatto!=null?" AND n_contatto <> "+strNContatto:"") + // 13/09/07 m.
		" AND skm_data_chiusura >= " + formatDate(dbc, strDataApeContatto) +
		" AND skm_data_chiusura IS NOT NULL";

		ISASCursor dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			return true;
		else
			return false;
	}

	//gb 12/09/07 *****************************************************************
	//	Restituisce un messaggio appropriato se si verifica che le date di apertura
	//	e chiusura della scheda contatto medici non sono congrue con le rispettive
	//	date del piano assistenziale (tabella 'piano_assist').
	//	Se invece sono congrue il metodo ritorna "" (stringa vuota).
	//	
	private String checkDateContEDatePianoAssist(ISASConnection dbc, Hashtable h) throws Exception
	{
		ISASCursor dbcur = null;
//		String strNCartella = (String) h.get("n_cartella");
//		String strNContatto = (String) h.get("n_contatto");
//		String strDataApeContatto = (String) h.get("skm_data_apertura");
		String strNCartella = ISASUtil.getValoreStringa(h,"n_cartella");
		String strNContatto = ISASUtil.getValoreStringa(h, "n_contatto");
		String strDataApeContatto =ISASUtil.getValoreStringa(h, "skm_data_apertura");
		
		String strDataChiuContatto = null;
		String msg = "";

		String mySel = "SELECT *" +
		" FROM piano_assist" +
		" WHERE n_cartella = " + strNCartella +
		" AND n_progetto = " + strNContatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0" +
		" AND pa_tipo_oper = '03'" +
		" AND pa_data < " + formatDate(dbc, strDataApeContatto);
		printError("SkMedEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
		dbcur = dbc.startCursor(mySel);

		if ((dbcur != null) && (dbcur.getDimension() >0))
			msg = "Attenzione esistono Piani Assistenziali la cui data di apertura ï¿½ antecedente della data apertura della scheda contatto.";
		else
		{
			dbcur = null;
			if (h.get("skm_data_chiusura") != null)
			{
				strDataChiuContatto = "" + h.get("skm_data_chiusura");

				if (strDataChiuContatto!=null && !(strDataChiuContatto.equals("")))
				{
					mySel =	 "SELECT *" +
					" FROM piano_assist" +
					" WHERE n_cartella = " + strNCartella +
					" AND n_progetto = " + strNContatto +
					" AND cod_obbiettivo = '00000000'" +
					" AND n_intervento = 0" +
					" AND pa_tipo_oper = '03'" +
					" AND pa_data_chiusura > " + formatDate(dbc, strDataChiuContatto) +
					" AND pa_data_chiusura IS NOT NULL ";
					printError("SkMedEJB / checkDateContEDatePianoAssist / mySel: " + mySel);
					dbcur = dbc.startCursor(mySel);

					if ((dbcur != null) && (dbcur.getDimension() >0))
						msg = "Attenzione esistono Piani Assistenziali la cui data di chiusura ï¿½ successiva alla data chiusura della scheda contatto.";
				}
			}
		}

		if (dbcur != null)
			dbcur.close();
		return msg;
	}




	// 10/09/07: inserimento su tabella PROGETTO di un record con i soli valori della chiave
	private String scriviProgetto(ISASConnection mydbc, String numCart, String dtSkVal) throws Exception {
		ISASRecord dbrPrg = mydbc.newRecord("progetto");
		dbrPrg.put("n_cartella", numCart);
		if (dtSkVal==null) dtSkVal=getDataAperturaCartella(mydbc,numCart);
		dbrPrg.put("pr_data", dtSkVal);
		mydbc.writeRecord(dbrPrg);
		System.out.println("\n SkFisioEJB -->> insert: Inserito record su tabella PROGETTO");
		return dtSkVal;
	}
	private String getDataAperturaCartella(ISASConnection mydbc, String numCart) throws Exception{
		ISASRecord dbr = mydbc.readRecord("select data_apertura from cartella where n_cartella ="+numCart); 
		return dbr.get("data_apertura").toString();
	}
	// 07/08/07: inserimento su tabella PROGETTO_CONT
	private void scriviProgettoCont(ISASConnection mydbc, String numCart, String dtSkVal,
			String tpOper, String numProg) throws Exception
			{
		ISASRecord dbrPrgCont = mydbc.newRecord("progetto_cont");
		dbrPrgCont.put("n_cartella", numCart);
		dbrPrgCont.put("pr_data", dtSkVal);
		dbrPrgCont.put("prc_tipo_op", tpOper);
		dbrPrgCont.put("prc_n_contatto", new Integer(numProg));
		mydbc.writeRecord(dbrPrgCont);
		printError("\n SkMedEJB -->> insert: Inserito record su tabella PROGETTO_CONT");
			}

	// 07/08/07: ricava il nuovo progressivo per le operazioni di 'insert'.
	private int getProgressivo(ISASConnection mydbc, String strNAssistito) throws Exception
	{
		ISASUtil u = new ISASUtil();
		int intProgressivo = 0;

		String myselect="SELECT MAX(n_contatto) max_n_contatto" +
		" FROM skmedico" +
		" WHERE n_cartella = " + strNAssistito ;
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr != null) 
			intProgressivo = u.getIntField(dbr, "max_n_contatto");

		intProgressivo++;
		return intProgressivo;
	}




	private ISASRecord insertMedRef(ISASConnection dbc, String infref,
			String data_ref, String data_apertura, String n_cartella,
			String n_contatto) throws DBRecordChangedException,
			ISASPermissionDeniedException, SQLException {

		boolean done=false;
		try{
			ISASRecord dbref=dbc.newRecord("skmed_referente");
			printError("SkMedEJB.insertMedRef(): Ok ci sono dentro!");
			dbref.put("n_cartella",n_cartella);
			dbref.put("n_contatto",n_contatto);
			//dbref.put("ski_data_apertura",data_apertura);
			dbref.put("skm_medico",infref);
			dbref.put("skm_medico_da",data_ref);
			dbc.writeRecord(dbref);
			done=true;
			return dbref;
		}catch(DBRecordChangedException e){
			System.out.println("SkMedEJB.insertMedRef(): "+e);
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("SkMedEJB.insertMedRef(): "+e);
			throw e;
		}catch(Exception e1){
			System.out.println("SkMedEJB.insertMedRef(): "+e1);
			throw new SQLException("Errore eseguendo una insertMedRef() - "+  e1);
		}
	}

	public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException, CariException // 07/08/07 
	{
		boolean done=false;
		ISASConnection dbc=null;
		ISASRecord dbr_ret = null;
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();

			dbr_ret = update(dbc,dbr);
			
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;

			return dbr_ret;
		}
		// 07/08/07 **************
		catch(CariException ce){
			ce.setISASRecord(null);
			try{
				System.out.println("SkMedEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo la rollback() - " +  ce);
			}
			throw ce;
			// *************************
		} catch(DBRecordChangedException e){
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
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception ex){
				ex.printStackTrace();
				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
			}
			
			throw new SQLException("Errore eseguendo una update() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	public Object deleteAll(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();

			String cartella=(String)dbr.get("n_cartella");
			String contatto=(String)dbr.get("n_contatto");

			String ris=VerificaInterv(dbc,dbr);
			if (ris.equals("N")){
				String data=(String)dbr.get("skm_data_apertura");

				dbc.deleteRecord(dbr);
				deleteLegameProgetto(dbc,cartella,contatto,"03");

				// 07/08/07     deleteContsan(dbc,data,cartella,contatto); // eliminata tabella CONTSAN
				deleteAllSchede(dbc,cartella,contatto,"skmdiaria","skd_data");
				deleteAllSchede(dbc,cartella,contatto,"skmrelcli","skr_data");
				deleteAllSchede(dbc,cartella,contatto,"skmterapia","skt_progr");
				// 07/08/07 ---
				deleteAllSchede(dbc,cartella,contatto,"skmed_referente","skm_medico_da");
				deletePianoAssist(dbc, cartella, contatto); 
				deletePianoAccessi(dbc, cartella, contatto); 
				deletePianoVerifiche(dbc, cartella, contatto); 
				// 07/08/07 ---
			}
			dbc.commitTransaction();

			dbc.close();
			super.close(dbc);
			done=true;
			if (ris.equals("N"))
				return new Integer(0);
			else  
				return new Integer(1);
		}catch(DBRecordChangedException e){
			System.out.println("SkMedEJB.delete1(): "+e);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("SkMedEJB.delete2(): "+e);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e1){
			System.out.println("SkMedfEJB.delete3(): "+e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception ex){
				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
			}
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	// 07/08/07
	private void deletePianoAssist(ISASConnection dbc, String cartella, String contatto) throws Exception
	{
		String myselect="SELECT * FROM piano_assist" +
		" WHERE pa_tipo_oper = '03'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0";
		printError("deletePianoAssist "+myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next()){
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_assist" +
			" WHERE pa_tipo_oper = '03'" +
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

	// 07/08/07
	private void deletePianoAccessi(ISASConnection dbc, String cartella, String contatto) throws Exception
	{
		String myselect="SELECT * FROM piano_accessi" +
		" WHERE pa_tipo_oper = '03'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0";
		printError("deletePianoAccessi " + myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_accessi" +
			" WHERE pa_tipo_oper = '03'" +
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

	// 07/08/07
	private void deletePianoVerifiche(ISASConnection dbc, String cartella, String contatto)	throws Exception
	{
		String myselect="SELECT * FROM piano_verifica" +
		" WHERE pa_tipo_oper = '03'" +
		" AND n_cartella = " + cartella +
		" AND n_progetto = " + contatto +
		" AND cod_obbiettivo = '00000000'" +
		" AND n_intervento = 0";
		printError("deletePianoVerifiche "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		while (dbcur.next()) {
			ISASRecord dbr = dbcur.getRecord();
			String sel = "SELECT * FROM piano_verifica" +
			" WHERE pa_tipo_oper = '03'" +
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

	private void deleteLegameProgetto(ISASConnection dbc,String cartella,String contatto,String figprof)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		ISASCursor dbcur=null;
		try{
			String myselect="SELECT * FROM progetto_cont WHERE "+
			" n_cartella="+cartella+
			" AND prc_n_contatto="+contatto+
			" AND prc_tipo_op='"+figprof+"'";
			printError("deleteLegameProgetto=>"+myselect);
			dbcur=dbc.startCursor(myselect);
			while (dbcur.next()){
				ISASRecord dbr = dbcur.getRecord();
				String sel= "SELECT * FROM progetto_cont WHERE "+
				" n_cartella="+cartella+
				" AND prc_n_contatto="+contatto+
				" AND prc_tipo_op='"+figprof+"'"+
				" AND pr_data = "+
				formatDate(dbc,(""+(java.sql.Date)dbr.get("pr_data")));
				ISASRecord dbr2 = dbc.readRecord(sel);
				dbc.deleteRecord(dbr2);
			}
			dbcur.close();
			done=true;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una deleteLegameProgetto() - "+  e1);
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	private void deleteAllSchede(ISASConnection dbc,String cartella,
			String contatto, String tabella, String dato)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		try{
			String myselect="SELECT * FROM "+tabella+" WHERE "+
			" n_cartella="+cartella+
			" AND n_contatto="+contatto;
			printError("DeleteSchede "+myselect);
			ISASCursor dbcur=dbc.startCursor(myselect);
			while (dbcur.next()){
				ISASRecord dbr = dbcur.getRecord();
				String sel= "SELECT * FROM "+tabella+" WHERE "+
				" n_cartella="+cartella+
				" AND n_contatto="+contatto;
				Object tipo_dato = dbr.get(dato);
				if (tipo_dato instanceof Integer)
					sel += " AND "+dato+" = "+(Integer)dbr.get(dato);
				else if (tipo_dato instanceof java.sql.Date)
					sel += " AND "+dato+" = "+formatDate(dbc,(""+(java.sql.Date)dbr.get(dato)));
				ISASRecord dbr2 = dbc.readRecord(sel);
				dbc.deleteRecord(dbr2);
			}
			dbcur.close();
			done=true;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una deleteSchede() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	/**** 07/08/07: eliminato tabella CONTSAN  
private ISASRecord insertContsan(ISASConnection dbc, String data_apertura,
	String data_chiusura, String descr_infer, String n_cartella,String operatore)
        throws DBRecordChangedException,
	ISASPermissionDeniedException, SQLException {
        int contatto=0;
    try{

        ISASRecord dbmax=null;
        if (n_cartella!=null && !n_cartella.equals("")){
        String selmax="SELECT * FROM cartella WHERE n_cartella="+n_cartella;
         dbmax=dbc.readRecord(selmax);
         int max=((Integer)dbmax.get("nmax_contatti")).intValue();
         max++;
         contatto=max;
         dbmax.put("nmax_contatti",new Integer(max));
         dbc.writeRecord(dbmax);
        }
        ISASRecord dbr=dbc.newRecord("contsan");
        dbr.put("n_contatto",(new Integer(contatto)).toString());
        dbr.put("n_cartella",n_cartella);
        dbr.put("cod_operatore",operatore);
        dbr.put("data_contatto",data_apertura);
        dbr.put("data_medico",data_apertura);
        if(data_chiusura.compareTo("")!=0)
          dbr.put("data_chius_medico",data_chiusura);
        dbr.put("descr_medico",descr_infer);
        dbc.writeRecord(dbr);
        String myselect="Select * from contsan where "+
                "n_cartella="+n_cartella+" and "+
                "n_contatto="+contatto;
        dbr=dbc.readRecord(myselect);
        return dbr;
    }catch(DBRecordChangedException e){
        System.out.println("SkMedEJB.updateContsan(): "+e);
	throw e;
    }catch(ISASPermissionDeniedException e){
        System.out.println("SkMedEJB.updateContsan(): "+e);
	throw e;
    }catch(Exception e1){
        System.out.println("SkMedEJB.updateContsan(): "+e1);
	throw new SQLException("Errore eseguendo una update() - "+  e1);
    }
}

private ISASRecord updateContsan(ISASConnection dbc,String data_apertura,String data_chiusura,String descr_medico,String n_cartella,String n_contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    try{

        String myselect="Select * from contsan where "+
			" n_cartella="+n_cartella+
                        " and n_contatto="+n_contatto;
        System.out.println("Update Contsan su SkMed==="+myselect);
        ISASRecord dbr=dbc.readRecord(myselect);
        dbr.put("data_medico",data_apertura);
        dbr.put("data_chius_medico",data_chiusura);
        dbr.put("descr_medico",descr_medico);
        dbc.writeRecord(dbr);
        //FACCIO LA SELECT SULLA VISTA CONTSAN_N PER TROVARE LA DATA MINIMA
        //FRA QUELLE INSERITE DAI VARI OPERATORI
        CalcolaDataMinima(dbc,n_cartella,n_contatto);
        done=true;
        return dbr;
    }catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una update() - "+  e1);
    }finally{
        if(!done){
            try{
            dbc.close();
	    super.close(dbc);
	    }catch(Exception e2){System.out.println(e2);}
        }
    }

}
private void CalcolaDataMinima(ISASConnection dbc, String cartella, String contatto)
throws DBRecordChangedException,ISASPermissionDeniedException, SQLException {

    try{
        String mysel="SELECT data_inizio FROM contsan_n WHERE n_cartella="+cartella+
                     " AND n_contatto="+contatto+
                     " ORDER BY data_inizio";
        System.out.println("CalcolaDataMinima: "+mysel);
        ISASRecord dbrcont=dbc.readRecord(mysel);
        if (dbrcont!=null){
          String data_minore=((java.sql.Date)dbrcont.get("data_inizio")).toString();
          System.out.println("Data minima "+data_minore);
          String selcon="Select * from contsan where n_cartella="+cartella+
                        " and n_contatto="+contatto;
          ISASRecord dbrupdate=dbc.readRecord(selcon);
          dbrupdate.put("data_contatto",data_minore);
          dbc.writeRecord(dbrupdate);
          System.out.println("Ho scritto il record");
        }
    }catch(DBRecordChangedException e){
        System.out.println("SkMedEJB.deleteContsan(): "+e);
	throw e;
    }catch(ISASPermissionDeniedException e){
        System.out.println("SkMedEJB.deleteContsan(): "+e);
	throw e;
    }catch(Exception e1){
        System.out.println("SkMedEJB.deleteContsan(): "+e1);
	throw new SQLException("Errore eseguendo una deleteContsan() - "+  e1);
    }
}

private ISASRecord deleteContsan(ISASConnection dbc,String data,String cartella,String contatto) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String data_fis="";
    String data_soc="";
    String data_inf="";
	String data_onc="";// 31/10/06 m.
    try{

        String myselect="Select * from contsan where "+
			" n_cartella="+cartella+
                        " and n_contatto="+contatto;
        ISASRecord dbr=dbc.readRecord(myselect);
        if(dbr.get("data_fisiot")!=null && !(dbr.get("data_fisiot")).equals(""))
          data_fis=((java.sql.Date)dbr.get("data_fisiot")).toString();
        if(dbr.get("data_sociale")!=null && !(dbr.get("data_sociale")).equals(""))
          data_soc=((java.sql.Date)dbr.get("data_sociale")).toString();
        if(dbr.get("data_infer")!=null && !(dbr.get("data_infer")).equals(""))
          data_inf=((java.sql.Date)dbr.get("data_infer")).toString();
		// 31/10/06 m. ---
		if(dbr.get("data_ostetr")!=null && !(dbr.get("data_ostetr")).equals(""))
          data_onc=((java.sql.Date)dbr.get("data_ostetr")).toString();
		// 31/10/06 m. ---

        if(data_fis.equals("") && data_soc.equals("") && data_inf.equals("")
			&& data_onc.equals(""))// 31/10/06 m.
           dbc.deleteRecord(dbr);
        else{
           dbr.put("data_medico","");
           dbr.put("data_chius_medico","");
           dbr.put("descr_medico","");
           dbc.writeRecord(dbr);
        }
        CalcolaDataMinima(dbc,cartella,contatto);
        done=true;
        return dbr;
    }catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una deleteContsan() - "+  e1);
    }finally{
        if(!done){
            try{
            dbc.close();
	    super.close(dbc);
	    }catch(Exception e2){System.out.println(e2);}
        }
    }
}
	 ******* 07/08/07: eliminato tabella CONTSAN ********/



	public ISASRecord query_salvataggio(myLogin mylogin,Hashtable h)
	throws SQLException {
		boolean done=false;
		String n_cartella=null;
		String n_contatto=null;
		String data_apertura=null;
		ISASConnection dbc=null;
		try {
			n_cartella=(String)h.get("n_cartella");
			n_contatto=(String)h.get("n_contatto");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkMed queryKey: Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select n_cartella,n_contatto from skmedico where "+
			" n_cartella="+n_cartella+
			" and n_contatto="+n_contatto;
			printError("select query_salvataggio su skmed==="+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_salvataggio()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	public ISASRecord query_salvataggio2(myLogin mylogin,Hashtable h)
	throws SQLException {
		boolean done=false;
		ISASConnection dbc=null;

		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from skmedico where "+
			"n_cartella="+(String)h.get("n_cartella")+" and "+
			"n_contatto="+(String)h.get("n_contatto")+" and "+
			"skm_data_apertura="+formatDate(dbc,(String)h.get("skm_data_apertura"));

			printError("select query_salvataggio su skmed==="+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_salvataggio()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	/*** 07/08/07: piano assistenziale scorporato dal contatto
public Vector query_progass(myLogin mylogin,Hashtable h) throws SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        String myselect="Select * from medprogass where n_cartella="+
            (String)h.get("n_cartella")+" and n_contatto="+
	    (String)h.get("n_contatto")+" ORDER BY mepa_data";
	    ISASCursor dbcur=dbc.startCursor(myselect);
	    Vector vdbr=dbcur.getAllRecord();
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
}
	 *******/

	public ISASRecord insert_terapia(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		String n_cartella=null;
		String n_contatto=null;
		ISASConnection dbc=null;
		try {
			n_cartella=(String)h.get("n_cartella");
			n_contatto=(String)h.get("n_contatto");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkMed insert: Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String myselectMAX="Select MAX(skt_progr) progr from skmterapia "+
			" where n_cartella="+n_cartella+" and " +
			" n_contatto="+n_contatto;
			ISASRecord dbr_0=dbc.readRecord(myselectMAX);
			int progr=0;
			if (dbr_0!=null){
				Integer k=(Integer)dbr_0.get("progr");
				if (k!=null && !k.equals("")){
					progr=k.intValue();
					progr=progr+1;
				}else   progr=1;
			}
			ISASRecord dbr=dbc.newRecord("skmterapia");
			Enumeration n=h.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}
			dbr.put("skt_progr",new Integer(progr));
			dbc.writeRecord(dbr);
			String myselect="SELECT * FROM skmterapia WHERE n_cartella="+n_cartella+
			" AND n_contatto="+n_contatto+
			" AND skt_progr="+progr;
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
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception ex){
				throw new SQLException("Errore eseguendo una rollback() - "+  ex);
			}
			throw new SQLException("Errore eseguendo una insert() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}

	}


	public ISASRecord salva_terapia(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		String n_contatto=null;
		String n_cartella=null;
		String progr=null;
		ISASConnection dbc=null;
		try {
			dbc=super.logIn(mylogin);
			n_cartella=(String)h.get("n_cartella");
			n_contatto=(String)h.get("n_contatto");
			progr=(String)h.get("skt_progr");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			String sel="SELECT * FROM skmterapia WHERE "+
			" n_cartella="+n_cartella+
			" and skt_progr="+progr+
			" and n_contatto="+n_contatto;
			ISASRecord dbr=dbc.readRecord(sel);
			if (dbr!=null){
				dbr.put("skt_principio",h.get("skt_principio"));
				dbr.put("skt_data_fine",h.get("skt_data_fine"));
				dbr.put("skt_data_inizio",h.get("skt_data_inizio"));
				dbr.put("skt_nome",h.get("skt_nome"));
				dbr.put("skt_cat_atc",h.get("skt_cat_atc"));
				dbr.put("skt_cons_dom",h.get("skt_cons_dom"));
			}

			ISASRecord dbterap=dbc.newRecord("skmterapia");
			Enumeration n=dbr.getHashtable().keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbterap.put(e,dbr.get(e));
			}
			dbc.deleteRecord(dbr);
			dbc.writeRecord(dbterap);
			String myselect="Select * FROM skmterapia WHERE "+
			" n_cartella="+n_cartella+
			" and skt_progr="+progr+
			" and n_contatto="+n_contatto;
			dbr=dbc.readRecord(myselect);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una update() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	
	
	public void delete(myLogin mylogin, ISASRecord dbrSkmed)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		String punto = ver + "delete ";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			if (dbrSkmed != null)
				dbc.deleteRecord(dbrSkmed);

		} catch (DBRecordChangedException e) {
			e.printStackTrace();
			throw e;
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e1) {
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - " + e1);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

	public void delete_terapia(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			String sel_del="SELECT * FROM skmterapia WHERE"+
			" n_cartella="+(String)h.get("n_cartella")+
			" AND n_contatto="+(String)h.get("n_contatto")+
			" AND skt_progr="+(String)h.get("skt_progr");
			ISASRecord dbr=dbc.readRecord(sel_del);
			if (dbr!=null)
				dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done=true;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	/*** 22/11/06 m.
private ISASRecord insertPato(ISASConnection dbc,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String n_cartella=null;
    String n_contatto=null;
    try {
        n_cartella=(String)h.get("n_cartella");
        n_contatto=(String)h.get("n_contatto");
    }catch (Exception e){
        e.printStackTrace();
		throw new SQLException("SkMed insert: Errore: manca la chiave primaria");
    }
    try{
        ISASRecord dbr=dbc.newRecord("skpatologie");
        //INSERISCO NELLA TABELLA DELLE PATOLOGIE DEL MEDICO
        dbr.put("n_cartella",n_cartella);
        dbr.put("n_contatto",n_contatto);
        dbr.put("cod_operatore",h.get("cod_operatore"));
        dbr.put("skpat_patol1",h.get("skpat_patol1"));
        dbr.put("skpat_patol2",h.get("skpat_patol2"));
        dbr.put("skpat_patol3",h.get("skpat_patol3"));
        dbr.put("skpat_patol4",h.get("skpat_patol4"));
        dbr.put("skpat_conf_med",h.get("skpat_conf_med"));
        dbc.writeRecord(dbr);
        String myselect="Select * from skpatologie where n_cartella="+n_cartella+" and " +
			" n_contatto="+n_contatto;
        dbr=dbc.readRecord(myselect);
        return dbr;
    }catch(Exception e){
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una insertPato() e() - "+  e);

        }
   }

private ISASRecord updatePato(ISASConnection dbc,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    ISASRecord dbausi=null;
    String n_cartella=null;
    String n_contatto=null;
    try {
        if (dbr.get("n_cartella") instanceof String)
          n_cartella=(String)dbr.get("n_cartella");
        else if (dbr.get("n_cartella") instanceof Integer)
          n_cartella=""+(Integer)dbr.get("n_cartella");
        if (dbr.get("n_contatto") instanceof String)
          n_contatto=(String)dbr.get("n_contatto");
        else if (dbr.get("n_contatto") instanceof Integer)
          n_contatto=""+(Integer)dbr.get("n_contatto");
    }catch (Exception e){
        e.printStackTrace();
        throw new SQLException("SkPato update: Errore: manca la chiave primaria");
    }
    try{
        String myselect="Select * from skpatologie where "+
			" n_cartella="+n_cartella+
                        " and n_contatto="+n_contatto;
        dbausi=dbc.readRecord(myselect);
        if(dbausi!=null){
          dbausi.put("cod_operatore",dbr.get("cod_operatore"));
          dbausi.put("skpat_patol1",dbr.get("skpat_patol1"));
          dbausi.put("skpat_patol2",dbr.get("skpat_patol2"));
          dbausi.put("skpat_patol3",dbr.get("skpat_patol3"));
          dbausi.put("skpat_patol4",dbr.get("skpat_patol4"));
          dbausi.put("skpat_conf_med",dbr.get("skpat_conf_med"));
        }else{
          dbausi=dbc.newRecord("skpatologie");
          dbausi.put("n_cartella",n_cartella);
          dbausi.put("n_contatto",n_contatto);
          dbausi.put("cod_operatore",dbr.get("cod_operatore"));
          dbausi.put("skpat_patol1",dbr.get("skpat_patol1"));
          dbausi.put("skpat_patol2",dbr.get("skpat_patol2"));
          dbausi.put("skpat_patol3",dbr.get("skpat_patol3"));
          dbausi.put("skpat_patol4",dbr.get("skpat_patol4"));
          dbausi.put("skpat_conf_med",dbr.get("skpat_conf_med"));
        }
        dbc.writeRecord(dbausi);
        return dbausi;
        }catch(Exception e){
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una UpdatePato() e() - "+  e);

     }
  }
	 ****/

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
			" AND int_tipo_oper='03'";
			//debugMessage("Dentro VerificaContatti=>"+mysel);
			dbtxt = dbc.readRecord(mysel);
			if (dbtxt!=null)
				if (dbtxt.get("data")!=null)
				{

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
			throw new SQLException("Errore eseguendo una query_controlloData()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
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
			" AND int_tipo_oper='03'";
			printError("Dentro VerificaInterv=>"+mysel);
			ISASRecord dbtxt = dbc.readRecord(mysel);
			if (dbtxt!=null)  ritorno="S";
			else              ritorno="N";
			return ritorno;
		} catch (Exception ex) {
			return ritorno="";
		}
	}

	/*** 22/11/06 m.
public ISASRecord query_patomed(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from skpatologie where "+
			"n_cartella="+(String)h.get("n_cartella")+" and "+
			"n_contatto="+(String)h.get("n_contatto");
                ISASRecord dbr=dbc.readRecord(myselect);
                if (dbr!= null) {
                 Hashtable h1 = dbr.getHashtable();
                 if(h1.get("skpat_patol1")!=null && !(h1.get("skpat_patol1").equals(""))){
                     dbr.put("des1", decodifica("icd9","cd_diag",h1.get("skpat_patol1"),
                                     "diagnosi",dbc));
                 }else  dbr.put("des1","");
                 if(h1.get("skpat_patol2")!=null && !(h1.get("skpat_patol2").equals(""))){
                     dbr.put("des2", decodifica("icd9","cd_diag",h1.get("skpat_patol2"),
                                     "diagnosi",dbc));
                 }else  dbr.put("des2","");
                 if(h1.get("skpat_patol3")!=null && !(h1.get("skpat_patol3").equals(""))){
                     dbr.put("des3", decodifica("icd9","cd_diag",h1.get("skpat_patol3"),
                                     "diagnosi",dbc));
                 }else  dbr.put("des3","");
                 if(h1.get("skpat_patol4")!=null && !(h1.get("skpat_patol4").equals(""))){
                     dbr.put("des4", decodifica("icd9","cd_diag",h1.get("skpat_patol4"),
                                     "diagnosi",dbc));
                 }else  dbr.put("des4","");
                }
                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_patomed()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

public ISASRecord insert_patomed(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String n_cartella=null;
    String n_contatto=null;
    String data_apertura=null;
    ISASConnection dbc=null;
    try {
        n_cartella=(String)h.get("n_cartella");
        n_contatto=(String)h.get("n_contatto");
    }catch (Exception e){
        e.printStackTrace();
		throw new SQLException("SkInf insert_pato: Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        ISASRecord dbr=dbc.newRecord("skpatologie");
        //INSERISCO NELLA TABELLA DELLE PATOLOGIE DELL'infermiere o del fisioterapista
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        String myselect="Select * from skpatologie where n_cartella="+n_cartella+" and " +
			" n_contatto="+n_contatto;
        dbr=dbc.readRecord(myselect);
        dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una insert_patomed()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}
public ISASRecord update_patomed(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String n_contatto=null;
	String n_cartella=null;
	String data_apertura=null;
        String progr=null;
	ISASConnection dbc=null;
	try {
		dbc=super.logIn(mylogin);
		n_cartella=(String)h.get("n_cartella");
                n_contatto=(String)h.get("n_contatto");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		String sel="SELECT * FROM skpatologie WHERE "+
                           " n_cartella="+n_cartella+
                           " and n_contatto="+n_contatto;
                ISASRecord dbr=dbc.readRecord(sel);
		if (dbr!=null){
                  dbr.put("n_cartella",n_cartella);
                  dbr.put("n_contatto",n_contatto);
                  dbr.put("cod_operatore",h.get("cod_operatore"));
                  dbr.put("skpat_patol1",h.get("skpat_patol1"));
                  dbr.put("skpat_patol2",h.get("skpat_patol2"));
                  dbr.put("skpat_patol3",h.get("skpat_patol3"));
                  dbr.put("skpat_patol4",h.get("skpat_patol4"));
                  dbr.put("skpat_conf_med",h.get("skpat_conf_med"));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * FROM skpatologie WHERE "+
                                " n_cartella="+n_cartella+
                                " and n_contatto="+n_contatto;
		ISASRecord dbter=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbter;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e1){
		System.out.println(e1);
		throw new SQLException("Errore eseguendo una update_patomed() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}
	 ***/



	private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
		Hashtable htxt = new Hashtable();
		if (val_codice==null) return "codice "+val_codice+" non trovato";
		try {
			String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			return ((String)dbtxt.get("descrizione"));
		} catch (Exception ex) {
			return "codice "+val_codice+" non trovato";
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

	
	
	// 22/11/06 m.: x DIAGNOSI --------------------------------------------------
	public void leggiDiagnosi_(ISASConnection mydbc, ISASRecord mydbr) throws Exception{
		String punto = ver + "leggiDiagnosi ";
		String cart = ((Integer)mydbr.get("n_cartella")).toString();
		Object dtApertura = (Object)mydbr.get(CostantiSinssntW.CTS_SKM_DATA_APERTURA);
		Object dtChiusura = (Object)mydbr.get("skm_data_chiusura");

		Vector vdbr = new Vector();
		DiagnosiEJB diagnosiEJB = new DiagnosiEJB();
		Hashtable dati = mydbr.getHashtable();
		ISASCursor dbcur = null;
		try {
			diagnosiEJB.leggiDiagnosi_interno(mydbc, dbcur, vdbr, dati, false);
		} finally{
			close_dbcur_nothrow(punto, dbcur);
		}
		
//		String critDtChius = "";
//		if (dtChiusura != null)
//			critDtChius = " AND data_diag <= " + formatDate(mydbc, dtChiusura.toString());
//
//		String myselect = "SELECT * FROM diagnosi" +
//		" WHERE n_cartella = " + cart +
//		critDtChius +
//		" ORDER BY data_diag DESC";
//
//		//      System.out.println("SkMedEJB: leggiDiagnosi - myselect=[" + myselect + "]");
//		LOG.trace(punto + " query>>" + myselect);
//		
//		ISASRecord recD = mydbc.readRecord(myselect);
//
//		if (recD != null) {
//			String dataIni = "";
//			String dtIni = "";
//			if (ManagerDate.validaData(dtApertura+"")){
//				dataIni = dtApertura.toString();
//				dtIni = dataIni.substring(0,4) + dataIni.substring(5,7) + dataIni.substring(8,10);
//			}
//			decodificaDiagn(mydbc, recD);
//			decodificaOper(mydbc, recD);
//			
//			boolean isDataInContesto = false;
//			if (ManagerDate.validaData(dtIni+"")){
//				isDataInContesto = checkData(recD, dtIni);
//			}
//			costruisci5Rec(mydbc, recD, vdbr, (isDataInContesto?"C":"")+"0");
//		}

		mydbr.put("diagn_associate", vdbr);
	}// END leggiDiagnosi

	// Costruisce 5 record da quello letto: hanno tutti i campi del DB uguali, piï¿½ le colonne fittizie del
	// codice e della descrizione, ognuno con i valori corrispondenti(rec1 con diag_1 e desc_diag_1, ecc).
	public void costruisci5Rec(ISASConnection mydbc, ISASRecord mydbr, Vector vett, String coloreCol) throws Exception
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
		String codDiag, descrDiag;
		for (int j=2; j<6; j++) {
			ISASRecord dbr_i = mydbc.newRecord("diagnosi");
			copiaRec(h_1, dbr_i);

			// aggiungo colonne fittizie agli altri 4 record
			codDiag = ISASUtil.getValoreStringa(dbr_i, "diag" + j);
			descrDiag = ISASUtil.getValoreStringa(dbr_i, "desc_diag" + j);
			LOG.trace(ver + j+" codDiag>>"+codDiag +"<< descrDiag>>"+descrDiag+"<<");
			if (ISASUtil.valida(codDiag) && ISASUtil.valida(descrDiag)){
				dbr_i.put("cod_alldiag", (String)dbr_i.get("diag" + j));
				dbr_i.put("desc_alldiag", (String)dbr_i.get("desc_diag" + j));
				dbr_i.put("progr", "" + j);
				dbr_i.put("dt_diag", "");
				vett.addElement((ISASRecord)dbr_i);
			}
//			dbr_i.put("cod_alldiag", (String)dbr_i.get("diag" + j));
//			dbr_i.put("desc_alldiag", (String)dbr_i.get("desc_diag" + j));
//			dbr_i.put("progr", "" + j);
//			dbr_i.put("dt_diag", "");
//			vett.addElement((ISASRecord)dbr_i);
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
	// 22/11/06m.: x DIAGNOSI -------------------------------------------------

	// ============== Decodifiche ==========================

	public void decodificaDiagn(ISASConnection mydbc, ISASRecord mydbr) throws Exception
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


	//gb 12/09/07 *******
	private void AggiornaData(String strNomeTabella, String strNCartella, String strNContatto, String strNomeFldDataChiusura, String strDataChiusura, String strNomeFldDataApertura, ISASConnection dbc)
	throws  SQLException{
		try {
			debugMessage("SkMedEJB/AggiornaData, chiudo TABELLA-->"+strNomeTabella);
			String mysel = "SELECT *" +
			" FROM " + strNomeTabella + 
			" WHERE n_cartella = " + strNCartella +
			" AND n_progetto = " + strNContatto +
			" AND cod_obbiettivo = '00000000'" +
			" AND n_intervento = 0" +
			" AND pa_tipo_oper = '03'" +
			" AND " + strNomeFldDataChiusura + " IS NULL";
			debugMessage("SkMedEJB/AggiornaData, chiudo -->"+strNomeTabella+" mysel-->"+mysel);
			ISASCursor dbcur=dbc.startCursor(mysel);
			Vector vdbr=dbcur.getAllRecord();
			for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
			{
				ISASRecord dbr=(ISASRecord)senum.nextElement();
				String strDataApertura =((java.sql.Date)dbr.get(strNomeFldDataApertura)).toString();
				
				String sel =	"SELECT *" +
				" FROM " + strNomeTabella +
				" WHERE n_cartella = " + strNCartella +
				" AND n_progetto = " + strNContatto +
				" AND cod_obbiettivo = '00000000'" +
				" AND n_intervento = 0" +
				" AND pa_tipo_oper = '03'" +
				" AND " + strNomeFldDataApertura + " = " + formatDate(dbc, strDataApertura);
				printError("SkMedEJB/AggiornaData, chiudo TABELLA sel-->"+sel);
				ISASRecord dbrDett=dbc.readRecord(sel);
				if(dbrDett.get(strNomeFldDataChiusura)==null){
					dbrDett.put(strNomeFldDataChiusura, strDataChiusura);					
				}				  
				dbc.writeRecord(dbrDett);
				if(dbrDett!=null)
					AggiornaDataPianointerv(strNCartella, strNContatto, strDataChiusura, strDataApertura, dbc);
			}//fine for
			dbcur.close();
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}
	//gb 12/09/07: fine *******

	//bargi 160407 chiusura piani assist pianointerv e agenda alla chiusura del contatto
	/*gb 12/09/07 *******
private void AggiornaData(String tabella, String cartella,String contatto,String data_tabella,String data_chiusura,String data_ini_tab,ISASConnection dbc)
throws  SQLException{
        try {
debugMessage("chiudo TABELLA-->"+tabella);
		String mysel = "SELECT * FROM " + tabella + " WHERE "+
			"n_cartella =" + cartella +
			" and n_contatto =" + contatto+
			" and "+data_tabella +" is null";
		debugMessage("chiudo -->"+tabella+" select-->"+mysel);
		ISASCursor dbcur=dbc.startCursor(mysel);
		Vector vdbr=dbcur.getAllRecord();
                for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
                {
                  ISASRecord dbr=(ISASRecord)senum.nextElement();
                  String data_inizio=((java.sql.Date)dbr.get(data_ini_tab)).toString();
                  System.out.println(data_ini_tab +" "+data_inizio);
                  String sel = "SELECT * FROM " + tabella + " WHERE "+
			       "n_cartella = " + cartella + " AND "+
                               "n_contatto = " + contatto+ " AND "+
//                               data_ini_tab + " = '" + data_inizio+"'";
                               data_ini_tab + " = " + formatDate(dbc,data_inizio);
							   debugMessage("chiudo TABELLA-->"+sel);
                  ISASRecord dbrDett=dbc.readRecord(sel);
                  if(dbrDett.get(data_tabella)==null){
                    dbrDett.put(data_tabella,data_chiusura);					
				  }				  
                  dbc.writeRecord(dbrDett);
				  if(dbrDett!=null)
				  AggiornaDataPianointerv(cartella,contatto,data_chiusura,data_inizio,dbc);
                }//fine for
                dbcur.close();
	} catch (Exception ex) {
		System.out.println(ex);
		throw new SQLException("Errore eseguendo una AggiornaData()  ");
	}
}
	 *gb 12/09/07: fine *******/

	//gb 12/09/07 *******
	private void AggiornaDataPianointerv(String strNCartella, String strNContatto, String strDataChiusura, String strDataApertura, ISASConnection dbc)
	throws  SQLException{
		try {
			debugMessage("SkMedEJB/AggiornaDataPianointerv, chiudo TABELLA--> piano_accessi");
			String mysel =	"SELECT *" +
			" FROM piano_accessi" +
			" WHERE n_cartella = " + strNCartella +
			" AND n_progetto =" + strNContatto +
			" AND cod_obbiettivo = '00000000'" +
			" AND n_intervento = 0" +
			" AND pa_tipo_oper ='03'" + 
			" AND pa_data = " + formatDate(dbc,strDataApertura) +
			" AND ( pi_data_fine IS NULL OR pi_data_fine > " + formatDate(dbc,strDataChiusura)+")";
			debugMessage("SkMedEJB/AggiornaDataPianointerv, piano_accessi da chiudere mysel-->"+mysel);
			//se pi_data_fine ï¿½ valorizzata ma data > della data chiusura questa viene anticipata
			ISASCursor dbcur=dbc.startCursor(mysel);
			while (dbcur.next())
			{
				ISASRecord dbr=dbcur.getRecord();
				String sel =	"SELECT *" +
				" FROM piano_accessi" +
				" WHERE n_cartella = " + strNCartella +
				" AND n_progetto =" + strNContatto +
				" AND cod_obbiettivo = '00000000'" +
				" AND n_intervento = 0" +
				" AND pa_tipo_oper ='03'" + 
				" AND pa_data = " + formatDate(dbc,strDataApertura) +
				" AND pi_prog = " + (Integer)dbr.get("pi_prog");
				debugMessage("SkMedEJB/AggiornaDataPianointerv, chiudo piano_accessi sel-->"+sel);
				ISASRecord dbrDett=dbc.readRecord(sel);
				if(dbrDett!=null){
					dbrDett.put("pi_data_fine", strDataChiusura);
					dbc.writeRecord(dbrDett);
				}
			}//fine for
			dbcur.close();
		} catch (Exception ex) {
			System.out.println(ex);
			throw new SQLException("Errore eseguendo una AggiornaData()  ");
		}
	}
	//gb 12/09/07: fine *******

	/*gb 12/09/07 *******
private void AggiornaDataPianointerv(String cartella,String contatto,String data_chiusura,String data_apertura,ISASConnection dbc)
throws  SQLException{
        try {
	debugMessage("chiudo TABELLA--> pianointerv");
		String mysel = "SELECT * FROM pianointerv WHERE "+
			"n_cartella =" + cartella +
			" and n_contatto =" + contatto +
			" and pi_tipo_oper ='03'" + 
			" and skpa_data =" + formatDate(dbc,data_apertura) +
		    " AND ( pi_data_fine is null OR pi_data_fine > "+
			formatDate(dbc,data_chiusura)+")";
		debugMessage("chiudo pianointerv select-->"+mysel);
		//se pi_data_fine ï¿½ valorizzata ma data > della data chiusura questa viene anticipata
		ISASCursor dbcur=dbc.startCursor(mysel);
		        while (dbcur.next())
                {
                  ISASRecord dbr=dbcur.getRecord();
                  String data_inizio=((java.sql.Date)dbr.get("skpa_data")).toString();
                  String sel = "SELECT * FROM pianointerv WHERE "+
							   "n_cartella = " + cartella + " AND "+
                               "n_contatto = " + (Integer)dbr.get("n_contatto") + " AND "+
                               "skpa_data = " + formatDate(dbc,data_inizio)+" AND "+
                               "pi_prog = " + (Integer)dbr.get("pi_prog") + " AND "+
							   "pi_tipo_oper= '03'";
                  ISASRecord dbrDett=dbc.readRecord(sel);
                  if(dbrDett!=null){
                    dbrDett.put("pi_data_fine",data_chiusura);
					dbc.writeRecord(dbrDett);
				  }
                }//fine for
                dbcur.close();
	} catch (Exception ex) {
		System.out.println(ex);
		throw new SQLException("Errore eseguendo una AggiornaData()  ");
	}
}
	 *gb 12/09/07 *******/

	//gb 12/09/07 private void rimuovoAgendaCaricata(String cartella,String contatto,String data_chiusura,ISASConnection dbc)
	private void rimuovoAgendaCaricata(String strNCartella, String strNContatto, String strDataChiusura, ISASConnection dbc)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		boolean done=false;
		ISASCursor dbcur=null;
		try{
			//gb 12/09/07	 String mysel="select * from agenda_interv,agenda_intpre "+
			String mysel =	"select * from agendant_interv, agendant_intpre " + //gb 12/09/07
			" where ag_data>"+formatDate(dbc, strDataChiusura)+" and "+
			" ag_cartella="+strNCartella+" and "+
			" ag_contatto="+strNContatto+" and "+
			" ag_tipo_oper='03' and "+
			" ag_stato=0 and "+//cancello solo appunt con stato a 0
			" ag_data=ap_data and "+
			" ag_progr=ap_progr and "+
			" ag_oper_ref=ap_oper_ref "+
			" order by ag_data";
			debugMessage("SkMedEJB/rimuovoAgendaCaricata mysel="+mysel);
			dbcur=dbc.startCursor(mysel);
			while (dbcur.next())
			{
				ISASRecord dbr=dbcur.getRecord();
				cancellaAppuntam(dbr, dbc);
			}
			if (dbcur != null) dbcur.close();
			done=true;
		}catch(Exception e){
			System.out.println("SkMedEJB/rimuovoAgendaCaricata Errore in cancella agenda_intpre..."+e);
			throw new SQLException("SkMedEJB: Errore eseguendo rimuovoAgendaCaricata()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	private void cancellaAppuntam(ISASRecord dbrec,ISASConnection dbc)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		try{

			String data=((java.sql.Date)dbrec.get("ap_data")).toString();
			String selag="SELECT *" +
			//gb 12/09/07		" FROM agenda_intpre WHERE "+
			" FROM agendant_intpre  WHERE " + //gb 12/09/07
			"ap_data="+formatDate(dbc,data)+" AND "+
			"ap_progr="+dbrec.get("ap_progr")+" AND "+
			"ap_oper_ref='"+(String)dbrec.get("ap_oper_ref")+"' AND "+
			"ap_prest_cod='"+(String)dbrec.get("ap_prest_cod")+"'";
			debugMessage("SkMedEJB/cancellaAppuntam, selag(1)="+selag);
			ISASRecord dbag=dbc.readRecord(selag);
			if(dbag!=null){
				dbc.deleteRecord(dbag);
				dbag=null;
				//devo controllare se sono rimasti record su agenda_intpre se non
				//ce ne sono occorre cancellare anche il record su agenda_interv
				selag="SELECT COUNT(*) tot" +
				//gb 12/09/07		" FROM agenda_intpre WHERE "+
				" FROM agendant_intpre WHERE "+ //gb 12/09/07
				"ap_data="+formatDate(dbc,data)+" AND "+
				"ap_progr="+dbrec.get("ap_progr")+" AND "+
				"ap_oper_ref='"+(String)dbrec.get("ap_oper_ref")+"'";
				debugMessage("SkMedEJB/cancellaAppuntam, selag(2)="+selag);
				dbag=dbc.readRecord(selag);

				int t=0;
				if(dbag!=null) t=util.getIntField(dbag,"tot");//convNumDBToInt("tot",dbag);
				if(t==0)
				{
					//cancello da agenda_interv
					selag="SELECT *" +
					//gb 12/09/07		   " FROM agenda_interv WHERE "+
					" FROM agendant_interv WHERE "+ //gb 12/09/07
					"ag_data="+formatDate(dbc,data)+" AND "+
					"ag_progr="+dbrec.get("ag_progr")+" AND "+
					"ag_oper_ref='"+(String)dbrec.get("ag_oper_ref")+"'";
					debugMessage("SkMedEJB/cancellaAppuntam, selag(3)="+selag);
					dbag=dbc.readRecord(selag);
					dbc.deleteRecord(dbag);
				}
			}
		}catch(Exception e){
			System.out.println("Errore in cancella agenda_intpre..."+e);
			throw new SQLException("Errore eseguendo cancellaAppuntam()  ");
		}
	}

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
			String dtRiferimento = (String)h0.get("dataRif");

			String myselect = "SELECT * FROM skmedico" +
			" WHERE n_cartella = " + cart +
			" AND skm_data_apertura > " + formatDate(dbc, dtRiferimento);

			printError("SkMedEJB: query_checkContSuccessivi - myselect=[" + myselect + "]");

			dbcur = dbc.startCursor(myselect);
			risu = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();
			
			dbc.close();
			super.close(dbc);
			done = true;			

			return (new Boolean(risu));
		} catch(Exception e1){
			System.out.println("SkMedEJB.query_checkContSuccessivi - Eccezione=[" + e1 + "]");
			return (Boolean)null;
		} finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	// 12/10/07: chiusura skValutazione -> aggiornamento dataChiusura
	private void chiudiSkValutaz(ISASConnection mydbc, String numCart, String dtSkVal, String data_chiusura) throws Exception
	{
		String mysel = "SELECT p.* FROM progetto p" +
		" WHERE p.n_cartella = " + numCart +
		" AND p.pr_data = " + formatDate(mydbc, dtSkVal);

		printError("SkMedEJB -->> chiudiSkValutaz: mysel=["+mysel+"]");
		ISASRecord mydbr = mydbc.readRecord(mysel);
		if (mydbr != null){
			mydbr.put("pr_data_chiusura", data_chiusura);
			mydbc.writeRecord(mydbr);
		}
	}


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

	// 20/05/09 Elisa Croci
	/* 1) Il caso non esiste: creo il caso e la segnalazione
	 * 2) Il caso esiste ma e' chiuso: creo il caso e la segnalazione
	 * 3) Il caso e' attivo: aggiorno la segnalazione
	 */
	private int gestione_segnalazione(ISASConnection dbc, ISASRecord dbr, Hashtable h, String prov)
	throws NumberFormatException, ISASMisuseException, CariException
	{
		printError("SkMedEJB: gestione_segnalazione -- HASH: " +  h.toString() + " REC: " + dbr.getHashtable().toString());

		int stato_caso = -1;
		int id_caso = -1;

		h.put("operZonaConf", (String)dbr.get("cod_operatore")); // 15/10/09
		
		if(h.get("id_caso") != null && !h.get("id_caso").equals("-1"))
		{
			// il caso esiste, prendo l'id e il suo stato
			stato_caso = Integer.parseInt(h.get("stato").toString());
			id_caso = Integer.parseInt(h.get("id_caso").toString());
		}

		// se sono in insert e il caso non esiste oppure e' concluso, devo crearne uno!
		if(prov.equals("insert") && (id_caso == -1 || stato_caso == GestCasi.STATO_CONCLU))
		{
			// se il caso non esiste, non c'e' nemmeno la segnalazione, allora la creo!
			try 
			{
				h.put("tipo_caso", new Integer(GestCasi.CASO_SAN));
				h.put("esito1lettura", new Integer(GestSegnalazione.ESITO_SANITARIO));

				if(h.get("dt_segnalazione") == null || h.get("dt_segnalazione").equals(""))
					h.put("dt_segnalazione", h.get("skm_data_apertura"));

				if(h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
					h.put("dt_presa_carico", h.get("dt_segnalazione"));

				// nel caso in cui il progetto viene creato insieme al contatto, dal client non mi
				// arriva la data del progetto, cosi' me la copio dal dbr!
				h.put("pr_data", dbr.get("pr_data"));

				ISASRecord rec_segn = gestore_segnalazioni.insert(dbc, h);

				if(rec_segn != null)
				{	
					Enumeration en = rec_segn.getHashtable().keys();
					while(en.hasMoreElements())
					{
						String chiave = en.nextElement().toString();
						dbr.put(chiave, rec_segn.get(chiave));
					}

					ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
					if(rec_pc != null)
					{
						Enumeration en1 = rec_pc.getHashtable().keys();
						while(en1.hasMoreElements())
						{
							String chiave = en1.nextElement().toString();
							dbr.put(chiave, rec_pc.get(chiave));
						}

						dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
					}

					return Integer.parseInt(rec_segn.get("id_caso").toString());
				}
				else return -1;
			}
			catch (CariException e) // 17/11/09
			{	
				System.out.println("SkMedEJB gestione_segnalazione, insert -- " + e);
				throw e;
			} 
			catch (DBRecordChangedException e) 
			{	
				System.out.println("SkMedEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} 
			catch (ISASPermissionDeniedException e) 
			{	
				System.out.println("SkMedEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);  
				return id_caso;
			} 
			catch (SQLException e) 
			{	
				System.out.println("SkMedEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);
				return id_caso;
			} 
			catch (Exception e) 
			{   
				System.out.println("SkMedEJB gestione_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e);  
				return id_caso;
			}
		}
// 29/03/10	else if(id_caso != -1 && (stato_caso != GestCasi.STATO_CONCLU &&  stato_caso != -1))
		else if(id_caso != -1 && (stato_caso != -1))
		{
			// il caso esiste, non e' concluso, quindi aggiorno i dati della segnalazione e della presa in carico
			try 
			{
				Enumeration e = dbr.getHashtable().keys();
				while(e.hasMoreElements())
				{
					String chiave = e.nextElement().toString();

					if(!h.containsKey(chiave))
						h.put(chiave, dbr.get(chiave));
				}

				printError(" gestione_segnalazione - UPDATE, H: " + h.toString());
				
				// 12/08/10 m ---------------
				if(dbr.get("origine") != null && !dbr.get("origine").equals("")) {
					int origine = Integer.parseInt(dbr.get("origine").toString());
					printError("gestione_segnalazione: Origine del caso "+id_caso+" =["+origine+"]");
				
					//  aggiorno solo se il caso nel frattempo non ï¿½ diventato UVM, altrimenti prtono comunicazioni
					//	di EVENTI non previste 
					if(origine == GestCasi.CASO_SAN) { // 12/08/10 m ---		
						ISASRecord new_segnalazione = gestore_segnalazioni.update(dbc, h);
						
						if(new_segnalazione != null)
						{
							Enumeration en = new_segnalazione.getHashtable().keys();
							while(en.hasMoreElements())
							{
								String chiave = en.nextElement().toString();
								dbr.put(chiave, new_segnalazione.get(chiave));
							}
						}
						
						// 29/03/12: aggiunto cntrl su esistenza rec, dato che il CASO e la SEGNALAZIONE potrebbero essere stati inseriti da Sins_PUA 
						//	(e quindi necessita update) , ma la PRESACARICO potrebbe dover essere in insert.
						if (!esistePresaCar(dbc, h)) {
							if(h.get("dt_presa_carico") == null || h.get("dt_presa_carico").equals(""))
								h.put("dt_presa_carico", h.get("skm_data_apertura"));

							ISASRecord rec_pc = gestore_presacarico.insert(dbc, h);
							if(rec_pc != null)
							{
								gestore_casi.presaCaricoCaso(dbc, h);
							
								Enumeration en1 = rec_pc.getHashtable().keys();
								while(en1.hasMoreElements())
								{
									String chiave = en1.nextElement().toString();
									dbr.put(chiave, rec_pc.get(chiave));
								}
							
								dbr.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());
							}
						} else {
							ISASRecord update_presacarico = gestore_presacarico.update(dbc, h);
							if(update_presacarico != null)
							{
								Enumeration en = update_presacarico.getHashtable().keys();
								while(en.hasMoreElements())
								{
									String chiave = en.nextElement().toString();
									dbr.put(chiave, update_presacarico.get(chiave));
								}
								
								dbr.put("cod_usl", dbr.get("reg_ero").toString() + dbr.get("asl_ero").toString());
							}
						}
					}
				}
				
				return id_caso;
			} 
			catch (CariException e) // 17/11/09
			{	
				System.out.println("SkMedEJB gestione_segnalazione, update -- " + e);
				throw e;
			} 
			catch (Exception e) 
			{
				System.out.println("SkMedEJB gestione_segnalazione, update() -- " + e);  
				return id_caso;
			}
		}
		else return id_caso;
	}// END gestione_segnalazione

	// 25/05/09 Elisa Croci
	private void prendi_presacarico(ISASConnection dbc, int caso,ISASRecord dbr)
	{
		try 
		{
			if(caso != -1) 
			{
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				h.put("ubicazione", dbr.get("ubicazione"));
				
				ISASRecord res = gestore_presacarico.queryKey(dbc, h);

				if(res != null)
				{
					Enumeration e = res.getHashtable().keys();
					while(e.hasMoreElements())
					{
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}

					dbr.put("cod_usl", res.get("reg_ero").toString() + res.get("asl_ero").toString());
				}
			}
		} 
		catch (ISASMisuseException e1) 
		{
			System.out.println("SkMedEJB prendi_presacarico, ERRORE REPERIMENTO CHIAVE! -- " + e1);
		}
		catch (Exception e) 
		{
			System.out.println("SkMedEJB prendi_presacarico, fallimento! -- " + e);
		}
	}// END prendi_presacarico

	// 20/05/09 Elisa Croci
	// prendo la segnalazione relativa al caso a cui il contatto deve fare riferimento
	private boolean prendi_segnalazione(ISASConnection dbc, int caso,ISASRecord dbr)
	{
		try 
		{
			/* prendo la segnalazione solo se il caso esiste e se sono in un contesto in cui si
		 		gestiscono le segnalazioni
			 */
			if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SAN) && caso != -1)
			{
				Hashtable h = new Hashtable();
				h.put("n_cartella", dbr.get("n_cartella"));
				h.put("pr_data", dbr.get("pr_data"));
				h.put("id_caso", new Integer(caso));
				ISASRecord res = gestore_segnalazioni.queryKey(dbc, h);

				if(res != null)
				{
					Enumeration e = res.getHashtable().keys();
					while(e.hasMoreElements())
					{
						String chiave = e.nextElement().toString();
						dbr.put(chiave, res.get(chiave));
					}
				}

				return true;
			}
			else return false;
		} 
		catch (ISASMisuseException e1) 
		{
			System.out.println("SkMedEJB prendi_segnalazione, ERRORE REPERIMENTO CHIAVE! -- " + e1);
			return false;
		}
		catch (Exception e) 
		{
			System.out.println("SkMedEJB prendi_segnalazione, fallimento! -- " + e);
			return false;
		}
	}// END prendi_segnalazione

	// 20/05/09 Elisa Croci
	// dato un contatto, prendo il caso attivo se esiste altrimenti quello chiuso piu' recente!
	private int prendi_dati_caso(ISASConnection dbc, ISASRecord dbr)
	{
		Hashtable h = new Hashtable();

		try 
		{
			h.put("n_cartella", dbr.get("n_cartella"));
			h.put("pr_data",dbr.get("pr_data"));

			printError("SkMedEJB -- prendi dati caso: " + h.toString());

			ISASRecord rec = gestore_casi.getCasoRif(dbc, h);
			if(rec != null)
			{
				Enumeration e = rec.getHashtable().keys();
				while(e.hasMoreElements())
				{
					String chiave = e.nextElement().toString();
					dbr.put(chiave, rec.get(chiave));
				}

				int caso = Integer.parseInt(dbr.get("id_caso").toString());
				return caso;
			}
			else return -1;
		} 
		catch(ISASMisuseException e)
		{
			System.out.println("SkMedEJB prendi_dati_caso, manca chiave primaria! -- " + e);
			return -1;
		}
		catch (Exception e) 
		{
			System.out.println("SkMedEJB prendi_dati_caso, fallimento! -- " + e);
			return -1;
		}
	}// END prendi_dati_caso
	
	
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
	
	private void aggRpPresaCar(ISASConnection dbc, Hashtable h_dbr) throws Exception
	{	
		gestore_presacarico.aggRpPresaCarFromSkMed(dbc, h_dbr);	
	}
	
	// 29/03/12: cntrl esistenza rec PRESACARICO
	private boolean esistePresaCar(ISASConnection dbc, Hashtable h) throws Exception
	{
		ISASRecord recPC = (ISASRecord)gestore_presacarico.queryKey(dbc, h);
		return (recPC != null);
	}	
	
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
	
	
	
	
	
	
	private void printError(String msg)
	{
		if(mydebug)
			System.out.println("SkMedEJB: " + msg);
	}
	
	
	
	public ISASRecord update(ISASConnection dbc,ISASRecord dbr)throws Exception{
		
		String punto = ver + "update ";
		String n_cartella=null;
		String n_contatto=null;
		String strDtSkVal = null;// 07/08/07
		boolean abilitatoProgetto = true;

		try {
//			strDtSkVal = (String)dbr.get("pr_data");// 07/08/07
			strDtSkVal = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SKM_DATA_APERTURA);
			n_cartella=dbr.get("n_cartella").toString();
			n_contatto=dbr.get("n_contatto").toString();
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("SkMed update: Errore: manca la chiave primaria");
		}

		// 07/08/07 *************************
					Hashtable h = dbr.getHashtable();
					LOG.debug(punto + " dati che  riceveo>>" + h+"<<");
					if (abilitatoProgetto){
						if(dtApeContLEMaxDtContChius(dbc, h)){
							String msg = Labels.getLabel("contatti.data_apertura.inf.data_chiusura.msg");
							throw new CariException(msg, -2);
						}
						// 07/08/07: fine *************************

						//gb 12/09/07 *************************
						String strMsgCheckDatePianoAssist = checkDateContEDatePianoAssist(dbc, h);
						if(!strMsgCheckDatePianoAssist.equals(""))
							throw new CariException(strMsgCheckDatePianoAssist, -2);        
						//gb 12/09/07: fine *************************
					}
					dbc.writeRecord(dbr);
					String myselect="Select * from skmedico where n_cartella=" + n_cartella + " and n_contatto=" + n_contatto;
					dbr=dbc.readRecord(myselect);

					if (dbr != null) {
						String descr_medico=(String)dbr.get("skm_descr_contatto");
						String data_chiusura="";
						if (dbr.get("skm_data_chiusura")!=null)
							data_chiusura=""+(java.sql.Date)dbr.get("skm_data_chiusura");
						String data_apertura=""+(java.sql.Date)dbr.get("skm_data_apertura");
						// 07/08/07 this.updateContsan(dbc,data_apertura,data_chiusura,descr_medico,n_cartella,n_contatto); // eliminato tabella CONTSAN
			
						if(abilitatoProgetto){
							if (data_chiusura!=null && !(data_chiusura.equals(""))){
								//bargi 16/04/2007
								//gb 01/10/07: Controlli e chiusure entitï¿½ sottostanti
								CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure();
								// date prestazioni erogate della tabella interv.
								// date aper. e date chius. dei piani assitenziali.
								// date aper. dei piani accessi.
								String strMsgCheckDtCh = clCcec.checkDtChDaContProgGTDtApeDtCh(dbc, n_cartella, n_contatto, data_chiusura, "03");
								if(!strMsgCheckDtCh.equals(""))
									throw new CariException(strMsgCheckDtCh, -2);
				
								// Chiusure entitï¿½ che stanno sotto il contatto:
								// Piani assistenziali
								// Piani accessi
								// Rimozione record da agendant_interv e agendant_intpre con date successive a data chiusura
								clCcec.chiudoDaContattoInGiu(dbc, n_cartella, n_contatto, data_chiusura, "03", (String)dbr.get("cod_operatore"));
								//gb 01/10/07: fine *******
								//chiudo piani assistenziali
								//gb 12/09/07	AggiornaData("medprogass",n_cartella,n_contatto,"mepa_data_chiusura",data_chiusura,"mepa_data",dbc);
								/*gb 01/10/07 *******
					   			AggiornaData("piano_assist", n_cartella,  n_contatto, "pa_data_chiusura", data_chiusura, "pa_data", dbc);
				
								//rimuovo da agenda appuntamenti caricati per la cartella chiusa
								rimuovoAgendaCaricata(n_cartella, n_contatto, data_chiusura, dbc);
								 *gb 01/10/07: fine *******/
				
								// 12/10/07 m. ---
								String skValDaChiudere = (String)h.get("skValDaChiudere");
								System.out.println("SkMedEJB: skValDaChiudere" +  skValDaChiudere);
								if ((skValDaChiudere!=null) && (skValDaChiudere.trim().equals("S")))
									chiudiSkValutaz(dbc, n_cartella, strDtSkVal, data_chiusura);
								// 12/10/07 m. ---
							}
						}
						String selref="SELECT * FROM skmed_referente WHERE n_cartella=" + n_cartella + " AND n_contatto=" + n_contatto;
						ISASCursor dbcur = dbc.startCursor(selref);
						String infref=(String)dbr.get("skm_medico");				
						String data_ref=((java.sql.Date)dbr.get("skm_medico_da")).toString();
						if (!dbcur.next()){
							if(dbr.get("skm_medico")!=null && !((String)dbr.get("skm_medico")).equals("")){
								
										
								this.insertMedRef(dbc,infref,data_ref,data_apertura,n_cartella,n_contatto);
							}
						}
						
						
						// Aggiorno l'operatore referente nel caso in cui sia diverso da quello precedente, altrimenti ne inserisco uno nuovo.
						selref="SELECT * FROM skmed_referente WHERE "+
								"n_cartella="+n_cartella+" AND "+
								"n_contatto="+n_contatto+" AND " +
								"skm_medico_da="+formatDate(dbc, dbr.get("skm_medico_da").toString());
						ISASRecord dbr_ref = dbc.readRecord(selref);
						if (dbr_ref == null){
							this.insertMedRef(dbc,infref,data_ref,data_apertura,n_cartella,n_contatto);
						}
						else if (!dbr_ref.get("skm_medico").toString().equals(dbr.get("skm_medico").toString()))
						{
							dbr_ref.put("skm_medico",dbr.get("skm_medico").toString());
							dbc.writeRecord(dbr_ref);
						}
						
						
						dbcur.close();
						leggiDiagnosi(dbc, dbr);
						// 07/08/07: per rimandare indietro al client la data della scheda valutazione
						dbr.put("pr_data", strDtSkVal);
						// 21/05/09 m. ------------------
						// lettura dtConclusione CASO precedente
						String dtChiusCasoPrec = "";
						if(abilitatoProgetto){
							dtChiusCasoPrec = getDtChiuCasoPrec(dbc, h);   
						}
						String tempoT = (String)h.get("tempo_t");
						// letture scale max
					  	gest_scaleVal.getScaleMax(dbc, dbr, n_cartella, (String)h.get("pr_data"), 
												"", dtChiusCasoPrec, "", tempoT, "03");

					  	
					  	if(abilitatoProgetto){
					  		//21/01/15 Simone: gestione caso non piÃ¹ necessaria
					  		
					  	// 21/05/09 m. ------------------				
//						int idCaso = -1;
//						// 21/05/09 Elisa Croci
//						if(gestore_segnalazioni.isSegnalDaGestire(dbc,GestCasi.CASO_SAN))
//						{
//							idCaso = prendi_dati_caso(dbc, dbr);
//							gestione_segnalazione(dbc, dbr, h, "update");
//							
//							printError("Fatta gestione segnalazione per il caso: " + idCaso);
//						}else{
//						// 08/10/10 m: solo x PIEMONTE
//						h.put("operZonaConf", (String)h.get("cod_operatore"));
//						if ((gestore_casi.isUbicazRegPiem(dbc, h)).booleanValue())							
//							aggRpPresaCar(dbc, (Hashtable)dbr.getHashtable());
//							h.put("dt_segnalazione",data_apertura);
//							h.put("dt_presa_carico",data_apertura);
//							h.put("origine", ""+GestCasi.CASO_SAN);
//							idCaso = gestore_casi.getIdCasoOrigine(dbc,h).intValue();
//						}
//							 15/06/09 Elisa Croci      ***************************************************************
//							if (data_chiusura != null && !data_chiusura.equals(""))
//							{
//								printError("Controllo contatto UNICO SANITARIO H == " + h.toString());
//								boolean unico = gestore_casi.query_checkUnicoContAperto(dbc, h, true, true);
//								if (idCaso != -1 && unico)
//								{
//									printError("Gestisco la chiusura del caso" );
//									// E' uguale ad S quando c'e' la possibilita' che ci siano piu' contatti e questo e'
//									// l'ultimo contatto aperto che stiamo chiudendo! Quindi devo chiudere, se esiste, il caso
//									// sociale associato!
//									int origine = -1;
//						       if(dbr.get("origine") != null && !(dbr.get("origine").toString()).equals(""))
//						        origine = Integer.parseInt(dbr.get("origine").toString());
//						       else if(h.get("origine") != null && !(h.get("origine").toString()).equals(""))
//						        origine = Integer.parseInt(h.get("origine").toString());
//						       if (origine != -1)
//						       {
//										printError("Origine del caso: " + origine);
//										if(origine == GestCasi.CASO_SAN)
//										{
//											Hashtable hCaso = new Hashtable();
//											hCaso.put("n_cartella", h.get("n_cartella"));
//											hCaso.put("pr_data", h.get("pr_data"));
//											hCaso.put("id_caso", new Integer(idCaso));
//											hCaso.put("dt_conclusione", dbr.get("skm_data_chiusura"));
//		// 26/03/10								hCaso.put("motivo", "99");
//											// 26/03/10 ----
//											String motChiu = (String)h.get("skm_motivo_chius");
//											String motChiuFlux = getTabVociCodReg(dbc, "MCHIUS", motChiu);
//											hCaso.put("motivo", motChiuFlux);
//											// 26/03/10 ----
//											hCaso.put("operZonaConf", (String)dbr.get("cod_operatore")); // 15/10/09
//											printError(" -- update(): Chiudi caso = HashCaso: " + hCaso.toString());
//											Integer r = gestore_casi.chiudiCaso(dbc, hCaso);
//											printError("Ritorno di ChiudiCaso == " + r);
//										}
//									}
//								}
//							}
							// ****************************************************************************************
						
					  	}
							
						// 15/06/09 Elisa Croci  ***************************************************************
						if(h.containsKey("ubicazione") && dbr != null)
							dbr.put("ubicazione", h.get("ubicazione"));
						if(h.containsKey("update_segnalazione") && dbr != null)
							dbr.put("update_segnalazione", h.get("update_segnalazione"));
						// *************************************************************************************
						dbr.put("tempo_t", tempoT); // 21/05/09 m.
						dbr.put("desc_presidio", decodPresidio(dbc, (String)dbr.get("skm_cod_presidio"), (String)dbr.get("skm_medico")));					
					}

					// Simone 25/11/14 Aggiornamento id_skso su rm_diario
					if (h.get("id_skso")!=null&&!h.get("id_skso").toString().trim().equals("")){
						Hashtable h_rm_diario = (Hashtable)h.clone();
						h_rm_diario.put("tipo_operatore", CostantiSinssntW.TIPO_OPERATORE_MEDICO);
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

	public ISASRecord recuperaMaxSkmed(ISASConnection dbc, String nCartella) {
		String punto = ver + "recuperaMaxSkmed ";
		ISASRecord dbrSkMed = null;
		try {
			String query = "SELECT k.* FROM skmedico k  WHERE k.n_cartella = " +nCartella+
					" AND k.n_contatto IN (SELECT MAX (x.n_contatto) FROM skmedico x " +
					" WHERE x.n_cartella = k.n_cartella) ";
			LOG.trace(punto + " query>>" + query);
			dbrSkMed = dbc.readRecord(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dbrSkMed;
	}

	public void inserisciAutorizzazioneMMG(myLogin myLogin, Hashtable<String, String> dati) {
		String punto = ver + "inserisciAutorizzazioneMMG ";
		ISASConnection dbc = null;
		String nCartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
		String idSkso = ISASUtil.getValoreStringa(dati, Costanti.CTS_ID_SKSO);

		try {
			dbc = super.logIn(myLogin);
			dbc.startTransaction();
			
			if (ISASUtil.valida(nCartella) && ISASUtil.valida(idSkso)) {
				RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
				ISASRecord dbrRmSkso = rmSkSOEJB.queryKeySkSoAllDatiCorrente(dbc, nCartella, idSkso);
				
				if (dbrRmSkso != null) {
					String codRichiedente = ISASUtil.getValoreStringa(dbrRmSkso, Costanti.CTS_PUA_RICHIEDENTE);
					String tipoUte = ISASUtil.getValoreStringa(dbrRmSkso, "tipo_ute");
					String tipoCure = ISASUtil.getValoreStringa(dbrRmSkso, Costanti.CTS_TIPOCURA);

					Hashtable<String, String> datiDaAggiornare = new Hashtable<String, String>();
					datiDaAggiornare.put(SKM_INVIATO, CaribelContattoFormCtrl.getRichiedente(codRichiedente));
					datiDaAggiornare.put(SKM_TIPOUT, tipoUte);
					datiDaAggiornare.put(SKM_MOTIVO, tipoCure);
					
					datiDaAggiornare.put(Costanti.N_CARTELLA,nCartella);
					datiDaAggiornare.put(Costanti.N_CONTATTO,"0");
					String dataAttivazioneSo = ISASUtil.getValoreStringa(dbrRmSkso, Costanti.CTS_DATA_PRESA_CARICO_SKSO);
					String dtInizioPiano = ISASUtil.getValoreStringa(dbrRmSkso, Costanti.CTS_SKSO_MMG_DATA_INIZIO);
					
					datiDaAggiornare.put(SKM_DATA_APERTURA, dtInizioPiano);
					datiDaAggiornare.put(SKM_DESCR_CONTATTO, ManagerDate.formattaDataIta(dtInizioPiano)
							+ CostantiSinssntW.DA_SCHEDA_SO);
					datiDaAggiornare.put(SKM_MEDICO_DA,
							ISASUtil.getValoreStringa(dbrRmSkso, Costanti.CTS_SKSO_MMG_DATA_INIZIO));

					RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
					ISASRecord dbrOpCoinvoltiMedicoDistretto = rmSkSOOpCoinvoltiEJB.queryKey(dbc, nCartella, idSkso,
							GestTpOp.CTS_COD_MEDICO);
					String codMedico = ISASUtil.getValoreStringa(dbrOpCoinvoltiMedicoDistretto, RMSkSOOpCoinvoltiEJB.COD_OPERATORE);
					
					if (!ISASUtil.valida(codMedico)){
						LOG.trace(punto + " Vado a recuperare il codice dal medico dai componenti uvi");
						codMedico = recuperaMedicoDaCommissioneUvi(dbc, nCartella, idSkso);
						dbrOpCoinvoltiMedicoDistretto.put(RMSkSOOpCoinvoltiEJB.COD_OPERATORE, codMedico);
					}
					
					datiDaAggiornare.put(SKM_MEDICO, codMedico);
					datiDaAggiornare.put(COD_OPERATORE, codMedico);
					datiDaAggiornare.put(Costanti.CTS_ID_SKSO, idSkso);
					datiDaAggiornare.put(SKM_COD_PRESIDIO,
							ISASUtil.getValoreStringa(dbrOpCoinvoltiMedicoDistretto, RMSkSOOpCoinvoltiEJB.COD_PRESIDIO));
					
					int nContatto = inserisciContattoMedico(dbc, nCartella, datiDaAggiornare);
					
					if (ISASUtil.valida(tipoCure) 
							&& (tipoCure.equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE))) {
						LOG.trace(punto + " insierire autorizzazione ADI");
						
						datiDaAggiornare.put(SkmmgEJB.SKADI_DATA, dataAttivazioneSo);
						datiDaAggiornare.put(Costanti.N_CONTATTO, nContatto+"");
						datiDaAggiornare.put(SkmmgEJB.SKADI_OPERATORE, codMedico);
						
						ISASRecord dbrOpCoinvoltiMMg = rmSkSOOpCoinvoltiEJB.queryKey(dbc, nCartella, idSkso,
								GestTpOp.CTS_COD_MMG);
						String codMedMMg = ISASUtil.getValoreStringa(dbrOpCoinvoltiMMg, RMSkSOOpCoinvoltiEJB.COD_OPERATORE);

						datiDaAggiornare.put(SkmmgEJB.SKADI_MMGPLS, codMedMMg);
						datiDaAggiornare.put(SkmmgEJB.SKADI_APPROVA, Costanti.CTS_S);
						
						datiDaAggiornare.put(SkmmgEJB.SKADI_DATA_INIZIO, ISASUtil.getValoreStringa(dbrRmSkso, RMSkSOBaseEJB.DATA_INIZIO));
						datiDaAggiornare.put(SkmmgEJB.SKADI_DATA_FINE, ISASUtil.getValoreStringa(dbrRmSkso, RMSkSOBaseEJB.DATA_FINE));
						datiDaAggiornare.put(SkmmgEJB.SKADI_FREQ_MENS, ISASUtil.getValoreStringa(dbrRmSkso, RMSkSOBaseEJB.FREQUENZA));
						datiDaAggiornare.put(SkmmgEJB.SKADI_SPECIFICA, SkmmgEJB.SKADI_SPECIFICA_0);
						
						inserisciAutorizzazioneAdi(dbc, nCartella, nContatto, dataAttivazioneSo, datiDaAggiornare);
						dbrOpCoinvoltiMedicoDistretto.put(CostantiSinssntW.DT_PRESA_CARICO,dataAttivazioneSo);
						rmSkSOOpCoinvoltiEJB.aggiornaPresaCarico(dbc, dbrOpCoinvoltiMedicoDistretto.getHashtable());
						
					}
				}
			}
			dbc.commitTransaction();
		} catch (Exception e) {
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1) {
				e1.printStackTrace();
				LOG.error(punto + " Errore in rollbackTransaction  ", e1);
			}
			LOG.error(punto + " Errore nel recuperare i dati della terapia ", e);
		} finally {
			logout_nothrow(punto, dbc);
		}
	}

	private String recuperaMedicoDaCommissioneUvi(ISASConnection dbc, String nCartella, String idSkso)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String punto = ver + "recuperaMedicoDaCommissioneUvi ";
		String codMedico = "";
		RMPuaUvmCommissioneEJB rmPuaUvmCommissioneEJB = new RMPuaUvmCommissioneEJB();
		String query = rmPuaUvmCommissioneEJB.recuperaQueryKey(nCartella, idSkso, "", GestTpOp.CTS_COD_MEDICO, false);
		LOG.debug(punto + " query >>" + query);

		ISASRecord dbrPuaUvmCommissioni = dbc.readRecord(query);
		codMedico = ISASUtil.getValoreStringa(dbrPuaUvmCommissioni, RMPuaUvmCommissioneEJB.CTS_PR_OPERATORE);
		
		return codMedico;
	}

	private void inserisciAutorizzazioneAdi(ISASConnection dbc, String nCartella, int nContatto,
			String skadiData, Hashtable<String, String> dati) throws Exception {
		String punto = ver + "inserisciAutorizzazioneAdi ";
		
		SkmmgEJB skmmgEJB = new SkmmgEJB();
		ISASRecord dbrSkmmgAdi = skmmgEJB.queryKeyAdi(dbc, nContatto+"", skadiData, nCartella);
		
		if (dbrSkmmgAdi == null){
			LOG.trace(punto + " Inserisco autorizzazione adi >>" + dati);
			skmmgEJB.insert_Adi(dbc, nCartella, nContatto+"", skadiData, dati);
		}else {
			String key,value;
			Enumeration<String> keys = dati.keys();
			while(keys.hasMoreElements()){
				key = keys.nextElement();
				value = ISASUtil.getValoreStringa(dati, key);
				dbrSkmmgAdi.put(key, value);
			}
			dbc.writeRecord(dbrSkmmgAdi);
		}
	}

	private int inserisciContattoMedico(ISASConnection dbc, String nCartella, Hashtable<String, String> dati) throws  Exception {
		String punto = ver + "inserisciContattoMedico ";
		int nContatto = 0;
		
		String query = "select * from skmedico where skm_data_chiusura is null and n_cartella = "+ nCartella;
		LOG.trace(punto + " query>>" + query);
		ISASRecord dbrSkMedico = dbc.readRecord(query);
		if (dbrSkMedico== null){
			LOG.trace(punto + " Inserisco il contatto medico con dati>>" + dati);
			dbrSkMedico = insertTransactional(dbc, dati);
		}
		nContatto = ISASUtil.getValoreIntero(dbrSkMedico, Costanti.N_CONTATTO);

		return nContatto;
	}
		
}

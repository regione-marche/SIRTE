package it.caribel.app.sinssnt.bean;


//----------------------------------------------------------------------------
//
// 17/01/2011 - EJB di connessione alla procedura SINSSNT Tabella rl_presacarico
//
//
//============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;

public class RLPresaCaricoEJB extends SINSSNTConnectionEJB
{
	private GestCasi gestione_casi = new GestCasi();
	private GestPresaCarico gestione_presacarico = new GestPresaCarico();
	
	private String nomeEJB = "RLPresaCaricoEJB";
	private boolean mydebug = true;

	public RLPresaCaricoEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("queryKey -- " + h.toString());

		boolean done = false;

		String n_cartella = null;
		String data_apertura = null;

		ISASConnection dbc = null;
		ISASRecord result = null;

		try	{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();

			if(n_cartella == null || data_apertura == null) return null;
		} catch (Exception e) {
			System.out.println("RLPresaCaricoEJB: queryKey() -- MANCA CHIAVE PRIMARIA per ricerca presa in carico"+ e);
			return null;
		}

		try	{
			dbc = super.logIn(mylogin);

            ISASRecord rec_pc = gestione_presacarico.queryKey(dbc, h);
			
            if (rec_pc != null) {
				if(rec_pc.get("asl_residenza")!=null)
					rec_pc.put("asl_res_desc", ISASUtil.getDecode(dbc, "tabusl", "cd_usl", rec_pc.get("asl_residenza"), "desusl"));
				if(rec_pc.get("distretto_erogatore")!=null)
					rec_pc.put("distr_ero_desc", ISASUtil.getDecode(dbc, "rl_asl_distretti", "codice_asl_distr", rec_pc.get("distretto_erogatore"), "descrizione"));					
				result = rec_pc;
            } else 
				result = null;
			dbc.close();
			super.close(dbc);
			done = true;
			return result;
		}
		catch(ISASPermissionDeniedException e){
			System.out.println("RLPresaCaricoEJB.queryKey(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e){
			System.out.println("RLPresaCaricoEJB.queryKey(): " + e);
			throw new SQLException("Errore eseguendo una PresaCaricoEJB.queryKey()");
		}
		finally	{
			if(!done){
				try	{
					dbc.close();
					super.close(dbc);
				} catch(Exception e2){    
					System.out.println("RLPresaCaricoEJB.queryKey(): " + e2);   
				}
			}
		}
	}

	public ISASRecord selectCaso(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("RLPresaCaricoEJB: selectCaso -- " + h.toString());

		boolean done = false;

		String n_cartella = null;
		String data_apertura = null;

		ISASConnection dbc = null;

		try	{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();

			if(n_cartella == null || data_apertura == null)
				return null;
		}catch (Exception e){
			System.out.println("RLPresaCaricoEJB: selectCaso() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
			return null;
		}

		try	{
			dbc = super.logIn(mylogin);

			int chiamante = Integer.parseInt(h.get("chiamante").toString());
			printError("Chiamante: " + chiamante);

			ISASRecord caso_riferimento =null;

			if(chiamante == GestCasi.CASO_UVM)
				caso_riferimento = gestione_casi.getCasoRifUvm(dbc, h);
			else caso_riferimento = gestione_casi.getCasoRif(dbc, h);

			if(caso_riferimento != null) {
				if(h.containsKey("chiamante"))
					caso_riferimento.put("chiamante", h.get("chiamante"));
				if(h.containsKey("ubicazione"))
					caso_riferimento.put("ubicazione", h.get("ubicazione"));
			}

			dbc.close();
			super.close(dbc);
			done = true;

			return caso_riferimento;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("RLPresaCaricoEJB.selectCaso(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("RLPresaCaricoEJB.selectCaso(): " + e);
			throw new SQLException("Errore eseguendo una PresaCaricoEJB.queryKey()");
		}
		finally	{
			if(!done){
				try {
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2){    System.out.println("RLPresaCaricoEJB.selectCaso(): " + e2);    }
			}
		}
	}

	private void printError(String msg)
	{
		if(mydebug)
			System.out.println("RLPresaCaricoEJB: " + msg);
	}



    public ISASRecord insert(myLogin mylogin, Hashtable h) 
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
	    boolean done=false;
	    String cartella=null;
	    String pr_data=null;
  
		String id_caso=null;
	    ISASConnection dbc=null;
	    try {
	        cartella=(String)h.get("n_cartella");
	        pr_data=(String)h.get("pr_data");
	        id_caso=(String)h.get("id_caso");
	    }catch (Exception e){
	        e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
	    }
	    try{
	        dbc=super.logIn(mylogin);

			dbc.startTransaction();

/*** 05/04/13: spostato nelmetodo insertNew()			
			printError("insert - H IN =["+h.toString()+"]");
			
			if (h.get("chiamante") != null) { 
				if (Integer.parseInt(h.get("chiamante").toString()) == GestCasi.CASO_UVM) {
					Integer result = gestione_casi.presaCaricoCasoUvm(dbc, h);
					printError(" da gestore casi, PRESA CARICO UVM? " + result);
				} else if (Integer.parseInt(h.get("chiamante").toString()) == GestCasi.CASO_SAN) {
					Integer result = gestione_casi.presaCaricoCaso(dbc, h);
					printError(" da gestore casi, PRESA CARICO SAN? " + result);	
				}
			}

	        ISASRecord dbr = gestione_presacarico.insert(dbc,h);
			if (dbr != null) {
				System.out.println("HASH DBR PRESACAR: "+dbr.getHashtable().toString());
				if(dbr.get("asl_residenza")!=null)
					dbr.put("asl_res_desc", ISASUtil.getDecode(dbc, "tabusl", "cd_usl", dbr.get("asl_residenza"), "desusl"));
				if(dbr.get("distretto_erogatore")!=null)
					dbr.put("distr_ero_desc", ISASUtil.getDecode(dbc, "rl_asl_distretti", "codice_asl_distr", dbr.get("distretto_erogatore"), "descrizione"));

					// 11/10/10: aggiornamento date su CASO
				aggCaso(dbc, h);
			}
***/			
			// 05/04/13
			ISASRecord dbr = insertNew(dbc, h);
			
			dbc.commitTransaction();

	        dbc.close();
	        super.close(dbc);
	        done=true;

	        return dbr;
	    }catch(DBRecordChangedException e){
	        e.printStackTrace();
			try  {
				System.out.println("RLPresaCaricoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1)  {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e1);
			}
			throw e;
	    }catch(ISASPermissionDeniedException e){
	        e.printStackTrace();
			try	{
				System.out.println("RLPresaCaricoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
	    }catch(Exception e1){
	        System.out.println(e1);
			try {
				System.out.println("RLPresaCaricoEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e2) {
				throw new SQLException("Errore eseguendo una rollback() - " +  e2);
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

	// 05/04/13: per chiamate da altri EJB
	public ISASRecord insertNew(ISASConnection dbc, Hashtable h) throws Exception
	{
		printError("insertNew - H IN =["+h.toString()+"]");

		// 25/06/13: ricerca eventuale caso concluso per evitare di inserire prese carico nei periodi di altri casi 
		// e utilizza come dtPresaCar la MAX tra quella che riceve e l'eventuale dtConclusione trovata
		String dtApe = (String)h.get("dt_presa_carico");
		DataWI dtConclCasoWI = null;
		ISASRecord dbrMaxCasoConcl = (ISASRecord)getMaxCasoConcl(dbc, h);
		if (dbrMaxCasoConcl != null) {
			dtConclCasoWI =  new DataWI((java.sql.Date)dbrMaxCasoConcl.get("dt_conclusione"));
			if (dtConclCasoWI.isSuccessiva(dtApe.replaceAll("-", ""))) {
				dtConclCasoWI = dtConclCasoWI.aggiungiGg(1);
				h.put("dt_presa_carico", dtConclCasoWI.getFormattedString2(1));
			}
		}
		// 25/06/13 ---
			
		if (h.get("chiamante") != null) { 
			if (Integer.parseInt(h.get("chiamante").toString()) == GestCasi.CASO_UVM) {
				Integer result = gestione_casi.presaCaricoCasoUvm(dbc, h);
				printError(" da gestore casi, PRESA CARICO UVM? " + result);
			} else if (Integer.parseInt(h.get("chiamante").toString()) == GestCasi.CASO_SAN) {
				Integer result = gestione_casi.presaCaricoCaso(dbc, h);
				printError(" da gestore casi, PRESA CARICO SAN? " + result);	
			}
		}

		ISASRecord dbr = gestione_presacarico.insert(dbc,h);
		if (dbr != null) {
			System.out.println("HASH DBR PRESACAR: "+dbr.getHashtable().toString());
			if(dbr.get("asl_residenza")!=null)
				dbr.put("asl_res_desc", ISASUtil.getDecode(dbc, "tabusl", "cd_usl", dbr.get("asl_residenza"), "desusl"));
			if(dbr.get("distretto_erogatore")!=null)
				dbr.put("distr_ero_desc", ISASUtil.getDecode(dbc, "rl_asl_distretti", "codice_asl_distr", dbr.get("distretto_erogatore"), "descrizione"));

			// 11/10/10: aggiornamento date su CASO
			aggCaso(dbc, h);
		}
		
		return dbr;
	}
	
	
   public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
   {
        System.out.println("HASH IN INGRESSO UPDATE_PRESACARICO: "+ dbr.getHashtable().toString());
	    boolean done=false;
	    String cartella=null;
	    String pr_data=null;
		String id_caso=null;
	    ISASConnection dbc=null;
        Hashtable h = null;
	    try {
			h = dbr.getHashtable();
	        cartella=(String)h.get("n_cartella");
	        pr_data=(String)h.get("pr_data");
	        id_caso=(String)h.get("id_caso");
		}catch (Exception e){
	        e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
	    }
	    try{
	        dbc=super.logIn(mylogin);

			dbc.startTransaction();
			
	        ISASRecord dba = gestione_presacarico.update(dbc,h);
				
			if (dba != null) {
				System.out.println("HASH DBR PRESACAR: "+dba.getHashtable().toString());
				if(dba.get("asl_residenza")!=null)
					dba.put("asl_res_desc", ISASUtil.getDecode(dbc, "tabusl", "cd_usl", dba.get("asl_residenza"), "desusl"));
				if(dba.get("distretto_erogatore")!=null)
					dba.put("distr_ero_desc", ISASUtil.getDecode(dbc, "rl_asl_distretti", "codice_asl_distr", dba.get("distretto_erogatore"), "descrizione"));
				
				// 11/10/10: aggiornamento date su CASO
				aggCaso(dbc, h);
			}

			dbc.commitTransaction();

	        dbc.close();
	        super.close(dbc);
	        done=true;
	        return dba;
	    }catch(DBRecordChangedException e){
	        try  {
				System.out.println("RLPresaCaricoEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1)  {
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e1);
			}
			throw e;
	    }catch(ISASPermissionDeniedException e){
	        e.printStackTrace();
			try	{
				System.out.println("RLPresaCaricoEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
	    }catch(Exception e1){
	        System.out.println(e1);
			try {
				System.out.println("RLPresaCaricoEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e2) {
				throw new SQLException("Errore eseguendo una rollback() - " +  e2);
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

	public void delete(myLogin mylogin, Hashtable h) 
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		String cartella=null;
		String pr_data=null;
		String id_caso=null;
		ISASConnection dbc=null;
		try {
			cartella=(String)h.get("n_cartella");
			pr_data=(String)h.get("pr_data");
			id_caso=(String)h.get("id_caso");
	    }catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			gestione_presacarico.delete(dbc,h);
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


	public Integer selectIdcaso(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("RLPresaCaricoEJB: selectIdCaso -- " + h.toString());

		boolean done = false;

		String n_cartella = null;
		String data_apertura = null;

		ISASConnection dbc = null;
		Integer result = null;

		try
		{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();

			if(n_cartella == null || data_apertura == null) return null;
		}
		catch (Exception e)
		{
			System.out.println("RLPresaCaricoEJB: selectIdCaso() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
			return null;
		}

		try
		{
			dbc = super.logIn(mylogin);

			int chiamante = Integer.parseInt(h.get("chiamante").toString());
			printError("Chiamante: " + chiamante);

			ISASRecord caso_riferimento =null;

			if(chiamante == GestCasi.CASO_UVM)
				caso_riferimento = gestione_casi.getCasoRifUvm(dbc, h);
			else caso_riferimento = gestione_casi.getCasoRif(dbc, h);

			if(caso_riferimento != null){
				if(h.containsKey("chiamante"))
					caso_riferimento.put("chiamante", h.get("chiamante"));
				if(h.containsKey("ubicazione"))
					caso_riferimento.put("ubicazione", h.get("ubicazione"));
                if (caso_riferimento.get("id_caso")!=null)
                    result = (Integer)(caso_riferimento.get("id_caso"));
                System.out.println("RLPresaCaricoEJB.selectIdCaso():" +caso_riferimento.getHashtable().toString());
			}

			dbc.close();
			super.close(dbc);
			done = true;
			return result;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("RLPresaCaricoEJB.selectIdCaso(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("RLPresaCaricoEJB.selectIdCaso(): " + e);
			throw new SQLException("Errore eseguendo una PresaCaricoEJB.queryKey()");
		}
		finally	{
			if(!done){
				try {
					dbc.close();
					super.close(dbc);
				} catch(Exception e2){    
					System.out.println("RLPresaCaricoEJB.selectIdCaso(): " + e2);    
				}
			}
		}
	}


	public Hashtable selCaso(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("RLPresaCaricoEJB: selCaso -- " + h.toString());

		ISASRecord risu = (ISASRecord)selectCaso(mylogin, h);
		if (risu != null) {
			if (!((Boolean)gestione_casi.isStatoConcluso(risu)).booleanValue())
				return (Hashtable)risu.getHashtable();
		}
		return new Hashtable();
	}

	// 11/10/10 m: se modifico la dataPresaCarico ---> aggiorno la tabella CASO
	private void aggCaso(ISASConnection dbc, Hashtable h_in) throws Exception
	{	
		ISASRecord rCaso = gestione_casi.getCaso(dbc, h_in);
		if ((rCaso != null) && (h_in.get("dt_presa_carico") != null)) {
			rCaso.put("dt_presa_carico", h_in.get("dt_presa_carico"));
		}
		
		// 29/11/10: aggiornamento anche della dataSegnalazione ---
		if (rCaso != null) {
			if (h_in.get("dt_segnalazione") != null) 
				rCaso.put("dt_segnalazione", h_in.get("dt_segnalazione"));
			else 
				gestione_presacarico.aggDtSegnCaso(rCaso, h_in.get("dt_presa_carico"));
		}
		// 29/11/10 ----
		
		dbc.writeRecord(rCaso);		
	}
	
	// 18/11/10 m : apertura CASO
	public Hashtable creaCaso (myLogin mylogin, Hashtable h) throws SQLException, CariException, Exception
	{
		printError(" creaCaso -- " + h.toString());
		
		NumberDateFormat ndf = new NumberDateFormat();
		boolean done = false;
		String n_cartella = null;
		String data_apertura = null;
		ISASConnection dbc = null;
		Hashtable h_ret = new Hashtable();

		try {
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();

			if(n_cartella == null || data_apertura == null) return null;
		} catch (Exception e) {
			System.out.println("RLPresaCaricoEJB: creaCaso() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
			return null;
		}

		try	{
			dbc = super.logIn(mylogin);

			int chiamante = Integer.parseInt(h.get("chiamante").toString());
			printError("RLPresaCaricoEJB: creaCaso() - Chiamante: " + chiamante);

			ISASRecord caso_riferimento = null;
			Integer id_caso = null;

			String strDataSistema = ndf.getJdbcSystemDate();
			String strDataApertura = ndf.formDate(data_apertura, "aaaa-mm-gg", true);
			
// 05/04/13	h.put("dt_segnalazione", strDataSistema);			
			// 05/04/13
			if (h.get("dt_segnalazione") == null) 
				h.put("dt_segnalazione", strDataApertura);
			h.put("pr_data", strDataApertura);
printError(" creaCaso - chiamo apertura CASO con h=" + h.toString());			
			if (chiamante == GestCasi.CASO_UVM) {
				h.put("origine", ""+GestCasi.CASO_UVM);
				id_caso = gestione_casi.apriCasoUvm(dbc, h);
			} else {
				// 24/06/13 
				if (((Boolean)gestione_casi.isUbicazRegLAzio(dbc, h)).booleanValue()) {
// 05/04/13		h.put("dt_presa_carico", strDataSistema);
					// 05/04/13
					if (h.get("dt_presa_carico") == null) 
						h.put("dt_presa_carico", strDataApertura);

					h.put("origine", ""+GestCasi.CASO_SAN);
					id_caso = gestione_casi.apriCasoSan(dbc, h);
				} else { // Molise, Abruzzo
					h.put("origine", ""+GestCasi.CASO_SAN);
					id_caso = gestione_casi.apriCasoSanNoPresaCar(dbc, h);				
				}			
			}
			
			if (id_caso != null) {
				h.put("id_caso", id_caso);
				caso_riferimento = gestione_casi.getCaso(dbc, h);
				h_ret = (Hashtable)caso_riferimento.getHashtable();
			}
			
			dbc.close();
			super.close(dbc);
			done = true;

			return h_ret;
		} catch(ISASPermissionDeniedException e) {
			System.out.println("RLPresaCaricoEJB.creaCaso(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		} catch(Exception e)	{
			System.out.println("RLPresaCaricoEJB.creaCaso(): " + e);
			throw new SQLException("Errore eseguendo una PresaCaricoEJB.creaCaso()");
		}
		finally	{
			if(!done){
				try {
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){    
					System.out.println("RLPresaCaricoEJB.creaCaso(): " + e2);   
				}
			}
		}
	}
	
	// 
	public Boolean checkDtPresaCarCaso(myLogin mylogin, Hashtable h) throws Exception
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur= null;
		boolean ret = false;

		try {
			dbc = super.logIn(mylogin);

			String n_cartella = (String)h.get("n_cartella");
			String dtPC = (String)h.get("dt_presa_carico");
			String idCaso = (String)h.get("id_caso");
			
			String critCaso = "";
			if ((idCaso != null) && (!idCaso.trim().equals("")))
				critCaso = " AND c.id_caso <> " + idCaso;

/** pi� semplice				
			String myselect = "SELECT * FROM rl_presacarico r" +
							" WHERE r.n_cartella = " + n_cartella +
							" AND r.dt_presa_carico < " + formatDate(dbc, dtPC) +
							" AND EXISTS (SELECT * FROM caso c" +
									" WHERE c.n_cartella = r.n_cartella" +
									" AND c.pr_data = r.pr_data" +
									" AND c.id_caso = r.id_caso" +
									" AND c.dt_presa_carico <= " + formatDate(dbc, dtPC) +
									" AND c.dt_conclusione >= " + formatDate(dbc, dtPC) +
									critCaso + ")";
**/
			String myselect = "SELECT * FROM caso c" +
									" WHERE c.n_cartella = " + n_cartella +
									" AND c.dt_presa_carico <= " + formatDate(dbc, dtPC) +
									" AND c.dt_conclusione >= " + formatDate(dbc, dtPC) +
									" AND c.origine <> " + GestCasi.CASO_SOC +
									critCaso;
									
			printError("RLPresaCaricoEJB: checkDtPresaCarCaso - myselect= " + myselect);

			dbcur = dbc.startCursor(myselect);
			ret = ((dbcur != null) && (dbcur.getDimension() > 0));
			dbcur.close();
			
			dbc.close();	
			super.close(dbc);
			done = true;

			return (new Boolean(!ret));
		} catch(Exception e1){
			e1.printStackTrace();
			//return (Boolean)null;
			return null;
		} finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println(e2);
				}
			}
		}
	}
	
	
	// 31/01/13: controllo sia:
	//	- dataPresaCarico <= della data conclusione (eventualmente presente)
	// 	- dataConclusione > della data presa in carico
	public Integer checkDtPreCaAndConclCaso(myLogin mylogin, Hashtable h) throws Exception
	{
		String nomeMetodo = "checkDtPreCaAndConclCaso";
		ISASConnection dbc = null;
		int risu = -1;

		try {
			dbc = super.logIn(mylogin);
			
			String dtPreCa = null;
			String dtConcl = null;
			
			
			if (h.get("dt_presa_carico") != null)
				dtPreCa = (String)h.get("dt_presa_carico");
			if (h.get("dt_conclusione") != null)
				dtConcl = (String)h.get("dt_conclusione");				
							
			StringBuffer strBuffSel = new StringBuffer("SELECT * FROM caso");
			strBuffSel.append(" WHERE n_cartella = " + (String)h.get("n_cartella"));
			strBuffSel.append(" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data")));
			strBuffSel.append(" AND id_caso = " + (String)h.get("id_caso"));
			
			if (dtPreCa != null) { // controllo sia <= della data conclusione (eventualmente presente)
				strBuffSel.append(" AND ((dt_conclusione IS NULL)");
				strBuffSel.append(" OR (dt_conclusione >= ");
				strBuffSel.append(formatDate(dbc, dtPreCa));
				strBuffSel.append("))");
			} else if (dtConcl != null) { //  controllo sia >= della data presa in carico
				strBuffSel.append(" AND dt_presa_carico <= ");
				strBuffSel.append(formatDate(dbc, dtConcl));
			}
			
			LOG.debug(" - sel=["+strBuffSel.toString()+"]");
			
			ISASRecord dbr = dbc.readRecord(strBuffSel.toString());
						
			risu = ((dbr != null)?1:0);
						
			return new Integer(risu);
		} catch(Exception e) {
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	
	// 31/01/13: cntrl presaCarico estratta per i flussi SIAD
	public Integer checkPrecaCarEstratta(myLogin mylogin, Hashtable h) throws Exception
	{
		String nomeMetodo = "checkPrecaCarEstratta";
		ISASConnection dbc = null;
		int risu = -1;

		try {
			dbc = super.logIn(mylogin);
						
			StringBuffer strBuffSel = new StringBuffer("SELECT * FROM rl_presacarico");
			strBuffSel.append(" WHERE n_cartella = " + (String)h.get("n_cartella"));
			strBuffSel.append(" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data")));
			strBuffSel.append(" AND id_caso = " + (String)h.get("id_caso"));
						
			LOG.debug(" - sel=["+strBuffSel.toString()+"]");
			
			ISASRecord dbr = dbc.readRecord(strBuffSel.toString());
			
			if ((dbr != null) && (dbr.get("flag_sended") != null) 
			&& (!dbr.get("flag_sended").toString().equals("0"))
			&& (!dbr.get("flag_sended").toString().trim().equals("")))
				risu = 1;
			else 
				risu = 0;
						
			return new Integer(risu);
		} catch(Exception e) {
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	
	// 26/03/13: cntrl esistenza PresaCarico alla dtRif
	public Boolean checkEsistePresaCar(myLogin mylogin, Hashtable h) throws Exception
	{
		String methodName = "checkEsistePresaCar";
		ISASConnection dbc = null;
		boolean ret = false;
		ISASCursor dbcur = null;
		
		try {
			dbc = super.logIn(mylogin);
			
			String cart = (String)h.get("n_cartella");
			String dtRif = (String)h.get("dt_rif");
		
			String sel = "SELECT r.* FROM rl_presacarico r"
				+ " WHERE r.n_cartella = " + cart
				+ " AND r.dt_presa_carico <= " + formatDate(dbc, dtRif)
				+ " AND EXISTS (SELECT * FROM caso c"
						+ " WHERE c.n_cartella = r.n_cartella"
						+ " AND c.pr_data = r.pr_data"
						+ " AND c.id_caso = r.id_caso"
						+ " AND c.dt_presa_carico <= " + formatDate(dbc, dtRif)
						+ " AND ((c.dt_conclusione >= " + formatDate(dbc, dtRif) + ")"
							+ " OR"
							+ " (c.dt_conclusione IS NULL)"
						+ ")"
				+ ")";
		
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

// 26/06/13: cntrl esistenza PresaCarico aperta
	public Boolean checkEsistePresaCarAperta(myLogin mylogin, Hashtable h) throws Exception
	{
		String methodName = "checkEsistePresaCarAperta";
		ISASConnection dbc = null;
		boolean ret = false;
		ISASCursor dbcur = null;
		
		try {
			dbc = super.logIn(mylogin);
			
			String cart = (String)h.get("n_cartella");
			String dtRif = (String)h.get("dt_rif");
		
			String sel = "SELECT r.* FROM rl_presacarico r"
				+ " WHERE r.n_cartella = " + cart
				+ " AND EXISTS (SELECT * FROM caso c"
						+ " WHERE c.n_cartella = r.n_cartella"
						+ " AND c.pr_data = r.pr_data"
						+ " AND c.id_caso = r.id_caso"
						+ " AND c.dt_conclusione IS NULL"
				+ ")";
		
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

	// 26/03/13: cntrl esistenzaConclusione alla dtRif
	public Boolean checkEsisteConcl(myLogin mylogin, Hashtable h) throws Exception
	{
		String methodName = "checkEsisteConcl";
		ISASConnection dbc = null;
		boolean ret = false;
		ISASCursor dbcur = null;
		
		try {
			dbc = super.logIn(mylogin);
			
			String cart = (String)h.get("n_cartella");
			String dtRif = (String)h.get("dt_rif");
		
			String sel = "SELECT * FROM caso"
				+ " WHERE n_cartella = " + cart
				+ " AND dt_presa_carico <= " + formatDate(dbc, dtRif)
				+ " AND dt_conclusione >= " + formatDate(dbc, dtRif);
		
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

	// 05/04/13
	public ISASRecord getRLPreCar(ISASConnection dbc, String cartella, String dtSkVal, String idCaso) throws Exception
	{
		ISASRecord rec = null;

		try {
			String sel = "SELECT * FROM rl_presacarico"
			+ " WHERE n_cartella = " + cartella
			+ " AND dt_presa_carico = " + dbc.formatDbDate(dtSkVal)
			+ " AND id_caso = " + idCaso;

			rec = dbc.readRecord(sel);

			return rec;
		} catch (Exception e) {
			System.out.println("RLPresaCaricoEJB.getRLPreCar: ERRORE - e=" + e);
			throw e;
		}
	}

	// 25/06/13
	public ISASRecord getMaxCasoConcl(ISASConnection dbc, Hashtable h_in) throws Exception
	{
		ISASRecord dbrC = null;
		
		try {
			String sel = "SELECT c.* FROM caso c"
			+ " WHERE c.n_cartella = " + h_in.get("n_cartella")
			+ " AND c.pr_data = " + dbc.formatDbDate((String)h_in.get("pr_data"))
			+ " AND c.id_caso IN (SELECT MAX(d.id_caso) FROM caso d"
				+ " WHERE d.n_cartella = c.n_cartella"
				+ " AND d.pr_data = c.pr_data"
				+ " AND d.dt_conclusione IS NOT NULL"
				+ " AND d.dt_conclusione IN (SELECT MAX(e.dt_conclusione) FROM caso e"
					+ " WHERE e.n_cartella = d.n_cartella"
					+ " AND e.pr_data = d.pr_data"
					+ " AND e.dt_conclusione IS NOT NULL"
				+ ")"
			+ ")"; 
		
			dbrC = dbc.readRecord(sel);

			return dbrC;
		} catch (Exception e) {
			System.out.println("RLPresaCaricoEJB.getMaxCasoConcl: ERRORE - e=" + e);
			throw e;
		}
	}
			
	// 26/06/13: insererimento su RL_PRESACARICO e, se non � gi� esistente, anche su SKINF
	public ISASRecord insertAlsoSkInf(myLogin mylogin, Hashtable h, Vector vettDati) throws Exception
	{
		String methodName = "insertAlsoSkInf";
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		try {
			dbc = super.logIn(mylogin);

			dbc.startTransaction();

			String cart = (String)h.get("n_cartella");
			String dataRif = (String)h.get("dt_presa_carico");
			String dtSkVal = null;		

			if (vettDati != null) {
				// 1) inserimento su SKINF
				dtSkVal = checkAndInsertSkInf(dbc, cart, dataRif, (Hashtable)vettDati.elementAt(0));

				// 2) inseerimento su RL_PRESACARICO				
				dbr = checkAndInsertRLPreCar(dbc, cart, dtSkVal, (Hashtable)vettDati.elementAt(1));
			}
			dbc.commitTransaction();

			return dbr;
		} catch(CariException ce) {
			ce.setISASRecord(null);
			LOG.info(methodName+"(): CariException => ROLLBACK");
			dbc.rollbackTransaction();
			
			throw ce;
		} catch(Exception e) {
			dbc.rollbackTransaction();
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {			
			logout_nothrow(methodName, dbc);
		}	
	}

	// 26/06/13	
	private String checkAndInsertSkInf(ISASConnection dbc, String cart, String dataRif, Hashtable h_datiSkInf) throws Exception 
	{
		String msg = "";
		String dtSkVal = null;
		boolean esisteGiaSkInf = false;

		SkInfEJB skInf = new SkInfEJB();		
		ISASRecord recProgetto = skInf.getProgetto(dbc, cart, dataRif);
		if ((recProgetto != null) && (recProgetto.get("pr_data") != null))
			dtSkVal = recProgetto.get("pr_data").toString();

		// cntrl esistenza
		esisteGiaSkInf = (skInf.getSkInf(dbc, cart, dataRif) != null);
		if (!esisteGiaSkInf) {
			ISASRecord recInf = skInf.insertNew(dbc, h_datiSkInf, cart, dtSkVal);			
			if (recInf == null) {
				System.out.println(nomeEJB + ".checkAndInsertSkInf() - FALLITO insert su SKINF!!");
				msg = "Attenzione: fallito inserimento scheda infermieristica.";
				throw new CariException(msg, -2);
			}
		} else 
			System.out.println(nomeEJB + ".checkAndInsertSkInf() - rec su SKINF gia esistente");
		
		return dtSkVal;
	}
		
	// 26/06/13
	private ISASRecord checkAndInsertRLPreCar(ISASConnection dbc, String cart, String dtSkVal, Hashtable h_datiRLPresaCar) throws Exception 
	{
		String msg = "";
		boolean esisteGiaRLPreCar = false;
		ISASRecord recRLPreCar = null;
		
		// cntrl esistenza
		if (dtSkVal != null) {
			String idCaso = (String)h_datiRLPresaCar.get("id_caso");
			recRLPreCar = this.getRLPreCar(dbc, cart, dtSkVal, idCaso);
			esisteGiaRLPreCar = (recRLPreCar != null);
			if (!esisteGiaRLPreCar)
				h_datiRLPresaCar.put("pr_data", dtSkVal);
		}

		if (!esisteGiaRLPreCar) {
			recRLPreCar = this.insertNew(dbc, h_datiRLPresaCar);
			if (recRLPreCar == null) {
				System.out.println(nomeEJB + ".checkAndInsertRLPreCar() - FALLITO insert su RL_PRESACARICO!!");
				msg = "Attenzione: fallito inserimento presa carico per flussi SIAD.";
				throw new CariException(msg, -2);
			}
		} else 
			System.out.println(nomeEJB + ".checkAndInsertRLPreCar() - rec su RL_PRESACARICO gia esistente");
			
		return recRLPreCar;
	}	
			
}


package it.caribel.app.sinssnt.bean;

//============================================================================


//CARIBEL S.r.l.
//----------------------------------------------------------------------------
//
// 17/01/2011 - EJB di connessione alla procedura SINSSNT Tabella Caso (Conclusione)
//
//
//============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;

public class RLConclusioneEJB extends SINSSNTConnectionEJB
{

	private GestCasi gestione_casi = new GestCasi();


	private boolean mydebug = true;

	public RLConclusioneEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("queryKey -- " + h.toString());

		boolean done = false;

		String n_cartella = null;
		String data_apertura = null;
        String id_caso = null;

		ISASConnection dbc = null;
		ISASRecord result = null;

		try	{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();
            id_caso = h.get("id_caso").toString();

            System.out.println("RLConclusioneEJB: queryKey()"+data_apertura);
			if(n_cartella == null || data_apertura == null || id_caso == null ) return null;
		} catch (Exception e) {
			System.out.println("RLConclusioneEJB: queryKey() -- MANCA CHIAVE PRIMARIA per ricerca rivalutazione"+ e);
			return null;
		}

		try	{
			dbc = super.logIn(mylogin);

//			int chiamante = Integer.parseInt(h.get("chiamante").toString());
//			printError("Chiamante: " + chiamante);

			ISASRecord conclusione =null;

			conclusione = gestione_casi.getCaso(dbc, h);

			if(conclusione != null) {
				printError("queryKey() -- hash da CASO: " + conclusione.getHashtable().toString());
				result = conclusione;
			}
			else result = null;

			dbc.close();
			super.close(dbc);
			done = true;
			return result;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("RLConclusioneEJB.queryKey(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("RLConclusioneEJB.queryKey(): " + e);
			throw new SQLException("Errore eseguendo una RLConclusioneEJB.queryKey()");
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
				catch(Exception e2){    System.out.println("RLConclusioneEJB.queryKey(): " + e2);    }
			}
		}
	}

	public ISASRecord selectCaso(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("PresaCaricoEJB: selectCaso -- " + h.toString());

		boolean done = false;

		String n_cartella = null;
		String data_apertura = null;

		ISASConnection dbc = null;
		ISASRecord result = null;

		try
		{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();

			if(n_cartella == null || data_apertura == null) return null;
		}
		catch (Exception e)
		{
			System.out.println("PresaCaricoEJB: selectCaso() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
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

			if(caso_riferimento != null)
			{
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
			System.out.println("PresaCaricoEJB.selectCaso(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("PresaCaricoEJB.selectCaso(): " + e);
			throw new SQLException("Errore eseguendo una PresaCaricoEJB.queryKey()");
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
				catch(Exception e2){    System.out.println("PresaCaricoEJB.selectCaso(): " + e2);    }
			}
		}
	}

	private void printError(String msg)
	{
		if(mydebug)
			System.out.println("RLConclusioneEJB: " + msg);
	}



    public ISASRecord insert(myLogin mylogin,Hashtable h) 
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
			Integer a = gestione_casi.chiudiCaso(dbc,h);
			ISASRecord dbr = gestione_casi.getCaso(dbc,h);
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

	// 31/01/13: cntrl dataConclusione CASO >= data di tutte le prestazioni su INTERV
	public Integer checkDtConclCasoAndAcce(myLogin mylogin, Hashtable h) throws Exception
	{
		String nomeMetodo = "checkDtConclCasoAndAcce";
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		int risu = -1;

		try {
			dbc = super.logIn(mylogin);
			
			String dtConcl = null;

			if (h.get("dt_conclusione") != null)
				dtConcl = (String)h.get("dt_conclusione");				
							
			StringBuffer strBuffSel = new StringBuffer("SELECT i.* FROM interv i");
			strBuffSel.append(" WHERE i.int_cartella = " + (String)h.get("n_cartella"));
			
			//  controllo che dataPrestazione NON sia > dataConclusioneCaso e che non appartenga ad altri casi
			if (dtConcl != null) { 
				strBuffSel.append(" AND i.int_data_prest > ");
				strBuffSel.append(formatDate(dbc, dtConcl));
				strBuffSel.append(" AND NOT EXISTS (SELECT * FROM rl_presacarico p");
				strBuffSel.append(" WHERE p.n_cartella = i.int_cartella");
				strBuffSel.append(" AND p.id_caso <> ");
				strBuffSel.append((String)h.get("id_caso"));
				strBuffSel.append(" AND p.dt_presa_carico >= ");
				strBuffSel.append(formatDate(dbc, dtConcl));
				strBuffSel.append(" AND p.dt_presa_carico <= i.int_data_prest");
				strBuffSel.append(")");
			}
			
			LOG.debug(" - sel=["+strBuffSel.toString()+"]");
			
			dbcur = dbc.startCursor(strBuffSel.toString());
						
			risu = (((dbcur != null) && (dbcur.getDimension() > 0))?0:1);
			
			if (dbcur != null) 
				dbcur.close();
				
			return new Integer(risu);
		} catch(Exception e) {
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(), e);
		} finally {
			close_dbcur_nothrow(nomeMetodo, dbcur);
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	// 31/01/13: cntrl Conclusione estratta per i flussi SIAD
	public Integer checkConclusioneEstratta(myLogin mylogin, Hashtable h) throws Exception
	{
		String nomeMetodo = "checkConclusioneEstratta";
		ISASConnection dbc = null;
		int risu = -1;

		try {
			dbc = super.logIn(mylogin);
						
			StringBuffer strBuffSel = new StringBuffer("SELECT * FROM caso");
			strBuffSel.append(" WHERE n_cartella = " + (String)h.get("n_cartella"));
			strBuffSel.append(" AND pr_data = " + formatDate(dbc, (String)h.get("pr_data")));
			strBuffSel.append(" AND id_caso = " + (String)h.get("id_caso"));
			strBuffSel.append(" AND dt_conclusione IS NOT NULL");
						
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
	
}


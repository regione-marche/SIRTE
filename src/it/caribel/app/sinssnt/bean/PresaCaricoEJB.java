package it.caribel.app.sinssnt.bean;
//============================================================================
//CARIBEL S.r.l.
//----------------------------------------------------------------------------
//
//25/05/2009 - EJB di connessione alla procedura SINSSNT Tabella rt_presacarico
//
//Elisa Croci
//
//============================================================================

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.exception.*;

public class PresaCaricoEJB extends SINSSNTConnectionEJB  
{
	private GestCasi gestione_casi = new GestCasi();
	private GestPresaCarico gestione_presacarico = new GestPresaCarico();
	
	
	private boolean mydebug = true;
	
	public PresaCaricoEJB() {}
	
	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("queryKey -- " + h.toString());
		
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
			System.out.println("PresaCaricoEJB: queryKey() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
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
				printError("queryKey() -- caso di riferimento: " + caso_riferimento.getHashtable().toString());
				
				h.put("id_caso", caso_riferimento.get("id_caso"));
				ISASRecord rec_pc = gestione_presacarico.queryKey(dbc, h);
				
				if(rec_pc != null)
				{
				printError("presacarico letta : "+rec_pc.getHashtable().toString());
					Enumeration e = caso_riferimento.getHashtable().keys();
					while(e.hasMoreElements())
					{
						String chiave = e.nextElement().toString();
						rec_pc.put(chiave, caso_riferimento.get(chiave));
					}
					
					rec_pc.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());			
					rec_pc.put("regione", rec_pc.get("reg_ero").toString());
					rec_pc.put("ubicazione", h.get("ubicazione"));
					rec_pc.put("chiamante", h.get("chiamante"));
					rec_pc.put("tipo_percorso_letto", rec_pc.get("tipo_percorso"));
					rec_pc.put("update_presacar", "S");
									
					result = rec_pc;
				}
				else result = null;
			}
			else result = null;

			dbc.close();
			super.close(dbc);
			done = true;
			//printError("presacarico result : "+result.getHashtable().toString());
			return result;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("PresaCaricoEJB.queryKey(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("PresaCaricoEJB.queryKey(): " + e);
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
				catch(Exception e2){    System.out.println("PresaCaricoEJB.queryKey(): " + e2);    }
			}
		}
	}
	
	public ISASRecord SelectStorico(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("SelectSto -- " + h.toString());
		
		boolean done = false;
		
		String n_cartella = null;
		String data_apertura = null;
	
		ISASConnection dbc = null;
		ISASRecord result = null;

		try 
		{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("dtRif").toString();
			
			if(n_cartella == null || data_apertura == null) return null;
		}
		catch (Exception e)
		{
			System.out.println("PresaCaricoEJB: SelectSto() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
			return null;
		}
		
		try
		{
			dbc = super.logIn(mylogin);

			String pr_data = "";
			if (!h.containsKey("pr_data"))
			{
				String sql = "select pr_data From progetto where n_cartella = "+n_cartella+ 
						" and pr_data <= "+dbc.formatDbDate(data_apertura)+
						" and (pr_data_chiusura is null or pr_data_chiusura > "+dbc.formatDbDate(data_apertura)+")";
				ISASRecord dbr = dbc.readRecord(sql);
				if (dbr!=null) pr_data = dbr.get("pr_data").toString();
				h.put("pr_data", pr_data);
			}
			int chiamante = Integer.parseInt(h.get("chiamante").toString());
			printError("Chiamante: " + chiamante);
			
			ISASRecord caso_riferimento =null;
			if (!pr_data.equals(""))
			caso_riferimento = gestione_casi.getCasoAllaData(dbc, h);
			
			
			if(caso_riferimento != null)
			{
				printError("queryKey() -- caso di riferimento: " + caso_riferimento.getHashtable().toString());
				
				h.put("id_caso", caso_riferimento.get("id_caso"));
				ISASRecord rec_pc = gestione_presacarico.queryKey(dbc, h);
				
				if(rec_pc != null)
				{
				printError("presacarico letta : "+rec_pc.getHashtable().toString());
					Enumeration e = caso_riferimento.getHashtable().keys();
					while(e.hasMoreElements())
					{
						String chiave = e.nextElement().toString();
						rec_pc.put(chiave, caso_riferimento.get(chiave));
					}
					
					rec_pc.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());			
					rec_pc.put("regione", rec_pc.get("reg_ero").toString());
					rec_pc.put("ubicazione", h.get("ubicazione"));
					rec_pc.put("chiamante", h.get("chiamante"));
					rec_pc.put("tipo_percorso_letto", rec_pc.get("tipo_percorso"));
					rec_pc.put("update_presacar", "S");
									
					result = rec_pc;
				}
				else result = null;
			}
			else result = null;

			dbc.close();
			super.close(dbc);
			done = true;
			//printError("presacarico result : "+result.getHashtable().toString());
			return result;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("PresaCaricoEJB.SelectSto(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("PresaCaricoEJB.SelectSto(): " + e);
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
				catch(Exception e2){    System.out.println("PresaCaricoEJB.SelectSto(): " + e2);    }
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
			System.out.println("PresaCaricoEJB: " + msg);
	}
	
	
	public Hashtable getDatiPresaCarico(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		printError("getDatiPresaCarico -- " + h.toString());
		
		boolean done = false;
		
		String n_cartella = null;
		String data_apertura = null;
	
		ISASConnection dbc = null;
		Hashtable result = new Hashtable();

		try 
		{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();
			
			if(n_cartella == null || data_apertura == null) return null;
		}
		catch (Exception e)
		{
			System.out.println("PresaCaricoEJB: getDatiPresaCarico() -- MANCA CHIAVE PRIMARIA per ricerca caso"+ e);
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
				printError("getDatiPresaCarico() -- caso di riferimento: " + caso_riferimento.getHashtable().toString());
				
				h.put("id_caso", caso_riferimento.get("id_caso"));
				ISASRecord rec_pc = gestione_presacarico.queryKey(dbc, h);
				
				if(rec_pc != null)
				{
				printError("presacarico letta : "+rec_pc.getHashtable().toString());
					Enumeration e = caso_riferimento.getHashtable().keys();
					while(e.hasMoreElements())
					{
						String chiave = e.nextElement().toString();
						rec_pc.put(chiave, caso_riferimento.get(chiave));
					}
					
					rec_pc.put("cod_usl", rec_pc.get("reg_ero").toString() + rec_pc.get("asl_ero").toString());			
					rec_pc.put("regione", rec_pc.get("reg_ero").toString());
					rec_pc.put("ubicazione", h.get("ubicazione"));
					rec_pc.put("chiamante", h.get("chiamante"));
					rec_pc.put("tipo_percorso_letto", rec_pc.get("tipo_percorso"));
					rec_pc.put("tipo_caso",rec_pc.get("origine"));
									
					result = rec_pc.getHashtable();
				}
				
			}
			
			dbc.close();
			super.close(dbc);
			done = true;
			//printError("presacarico result : "+result.getHashtable().toString());
			return result;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("PresaCaricoEJB.getDatiPresaCarico(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("PresaCaricoEJB.getDatiPresaCarico(): " + e);
			throw e;
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
				catch(Exception e2){e2.printStackTrace(); }
			}
		}
	}
	
	
}


package it.caribel.app.sinssnt.bean;//============================================================================
//CARIBEL S.r.l.
//----------------------------------------------------------------------------
//
// 17/01/2011 - EJB di connessione alla procedura SINSSNT Tabella rl_rivalutazione
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
import it.pisa.caribel.sinssnt.casi_adrsa.GestRivalutazione;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;

public class RLRivalutazioneEJB extends SINSSNTConnectionEJB
{
	private GestCasi gestore_caso = new GestCasi();
	private GestRivalutazione gestore_rivalutazione = new GestRivalutazione();

	private boolean mydebug = true;

	public RLRivalutazioneEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException, CariException, Exception
	{
		System.out.println("queryKey -- " + h.toString());

		boolean done = false;

		String n_cartella = null;
		String data_apertura = null;
        String id_caso = null;
		String progr = null;
		ISASConnection dbc = null;
		ISASRecord result = null;

		try
		{
			n_cartella = h.get("n_cartella").toString();
			data_apertura = h.get("pr_data").toString();
            id_caso = h.get("id_caso").toString();
            progr = h.get("progr").toString();

			if(n_cartella == null || data_apertura == null || id_caso == null || progr == null) return null;
		}
		catch (Exception e)
		{
			System.out.println("RLRivalutazioneEJB: queryKey() -- MANCA CHIAVE PRIMARIA per ricerca rivalutazione"+ e);
			return null;
		}

		try
		{
			dbc = super.logIn(mylogin);

			int chiamante = Integer.parseInt(h.get("chiamante").toString());
			System.out.println("Chiamante: " + chiamante);

			ISASRecord rivalutazione =null;

			rivalutazione = gestore_rivalutazione.queryKey(dbc, h);

			if(rivalutazione != null) {
				System.out.println("queryKey() --  RLRivalutazione: " + rivalutazione.getHashtable().toString());

				result = rivalutazione;
			} else 
				result = null;

			dbc.close();
			super.close(dbc);
			done = true;
			return result;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println("RLRivalutazioneEJB.queryKey(): "+e);
			throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
		}
		catch(Exception e)
		{
			System.out.println("RLRivalutazioneEJB.queryKey(): " + e);
			throw new SQLException("Errore eseguendo una RLRivalutazioneEJB.queryKey()");
		}
		finally	{
			if(!done){
				try	{
					dbc.close();
					super.close(dbc);
				} catch(Exception e2){    
					System.out.println("RLRivalutazioneEJB.queryKey(): " + e2);   
				}
			}
		}
	}

	
	
	
	private void deleteIng(ISASConnection dbc, Hashtable hin)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException 
	{
		String cartella=null;
		String pr_data=null;
        String id_caso=null;
		String progr=null;
		String tipouvg=null;
		try{
			cartella=(String)hin.get("n_cartella");
			pr_data=(String)hin.get("pr_data");
            id_caso=(String)hin.get("id_caso");
            progr=(String)hin.get("progr");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
            ServerUtility su =new ServerUtility();
            //cancello tutti i record su skiesami con quella chiave
            String select ="SELECT * FROM rl_rivalutazione WHERE ";
            String sel="";
            sel=su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,((String)hin.get("n_cartella")));
			sel=su.addWhere(sel,su.REL_AND,"id_caso",su.OP_EQ_NUM,((String)hin.get("id_caso")));
			sel=su.addWhere(sel,su.REL_AND,"pr_data",su.OP_EQ_NUM,formatDate(dbc,(String)hin.get("pr_data")));
            select=select+sel;
			
            ISASCursor dbcur=dbc.startCursor(select);
            Vector vdbr=dbcur.getAllRecord();
            ISASRecord dbr=null;
            //System.out.println("VETTORE DI RECORDS === "+ vdbr.toString());
            for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); ){
                dbr=(ISASRecord)senum.nextElement();
                //System.out.println(" RECORD CORRENTE === " +dbr.getHashtable().toString());
                deleteAll(dbc,dbr.getHashtable());
            }
        }catch(DBRecordChangedException e){
                e.printStackTrace();
                throw e;
        }catch(ISASPermissionDeniedException e){
                e.printStackTrace();
                throw e;
        }catch(Exception e1){
                System.out.println(e1);
                throw new SQLException("Errore eseguendo una delete() - "+e1);
        }
	}

	public void deleteAll(ISASConnection dbc, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
        try{
            ServerUtility su =new ServerUtility();

			String select="SELECT * FROM rl_rivalutazione WHERE ";
			String sel="";
			sel=su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,((Integer)h.get("n_cartella")).toString());
			sel=su.addWhere(sel,su.REL_AND,"id_caso",su.OP_EQ_NUM,((Integer)h.get("id_caso")).toString());
			sel=su.addWhere(sel,su.REL_AND,"pr_data",su.OP_EQ_NUM,formatDate(dbc,((Object)h.get("pr_data")).toString()));
			sel=su.addWhere(sel,su.REL_AND,"progr",su.OP_EQ_NUM,((Integer)h.get("progr")).toString());

			select=select+sel;
			ISASRecord dbr=dbc.readRecord(select);
			dbc.deleteRecord(dbr);
        }catch(DBRecordChangedException e){
            e.printStackTrace();
            throw e;
        }catch(ISASPermissionDeniedException e){
            e.printStackTrace();
            throw e;
        }catch(Exception e1){
            System.out.println(e1);
            throw new SQLException("Errore eseguendo una delete() - "+e1);
        }
	}

	public ISASRecord update(myLogin mylogin,Hashtable hin,Vector vh)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		String cartella=null;
		String pr_data=null;
        String id_caso=null;
		ISASConnection dbc=null;
		ISASRecord dba=null;
		try {
			cartella=(String)hin.get("n_cartella");
			pr_data=(String)hin.get("pr_data");
			id_caso=(String)hin.get("id_caso");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}

		Hashtable h=new Hashtable();
		try{
            dbc=super.logIn(mylogin);

			dbc.startTransaction();

			deleteIng(dbc,hin);

			for(Enumeration en= vh.elements(); en.hasMoreElements(); )  {
                Hashtable h_r=(Hashtable)en.nextElement();
				Enumeration n=hin.keys();
				while(n.hasMoreElements()){
					String e=(String)n.nextElement();
					h_r.put(e,hin.get(e));
				}
				h_r.put("tempo_t", h_r.get("progr"));
				printError(".update() - vado a inserire rivalutaz: h=["+h_r.toString()+"]");
				// avendo cancellato tutto, si chiama la insert
				ISASRecord rec_riv = gestore_rivalutazione.insert(dbc, h_r);
				// aggiornamento tempo T su tabella CASO
				ISASRecord rec_caso = gestore_caso.updateCaso(dbc, h_r);
				if (rec_caso != null)
					printError("- aggiornato tabella CASO: tempoT=["+rec_caso.get("tempo_t")+"]");
			}

			// per restituire al client un record
			dba = dbc.newRecord("caso");
			Enumeration n=hin.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dba.put(e,hin.get(e));
			}
			
			dbc.commitTransaction(); 

			dbc.close();
			super.close(dbc);
			done=true;
			return dba;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
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
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e2);
			}
			throw new SQLException("Errore eseguendo una update() - "+ e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e3){System.out.println(e3);}
			}
		}
	}


	public void deleteAllRecords(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);

	    	dbc.startTransaction(); 

            deleteIng(dbc,h);

	        dbc.commitTransaction(); 
			
			dbc.close();
			super.close(dbc);
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
        }catch(Exception e){
            e.printStackTrace();
            try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - "+  e);
			}
            throw new SQLException("Errore eseguendo una delete() - "+e);
        }
	}


	public Vector query_rivalutazioni(myLogin mylogin, Hashtable h) throws  SQLException
	{
		boolean done=false;
		ISASConnection dbc=null;
		ISASRecord dbr = null;
		try{
			Vector vdbg=null;
			//System.out.println("HASH IN QUERY RIVALUTAZIONI: "+ h.toString());
			dbc=super.logIn(mylogin);
			String selg="SELECT * FROM rl_rivalutazione" +
				" WHERE n_cartella = " + (String)h.get("n_cartella") +
                " AND pr_data = " + formatDate(dbc,(String)h.get("pr_data")) +
                " AND id_caso = " + (String)h.get("id_caso") +
				" ORDER BY progr";
			System.out.println("RlRivalutazioniEJB/query_rivalutazioni: " + selg);

			ISASCursor dbgriglia=dbc.startCursor(selg);
			if (dbgriglia!=null)  {
				vdbg=dbgriglia.getAllRecord();
				for(Enumeration senum=vdbg.elements();senum.hasMoreElements(); ){
					dbr=(ISASRecord)senum.nextElement();
					String motivo = ""+dbr.get("motivo");
					if(motivo.trim().equals("1")) 						//mod elisa b 27/09/11
						//dbr.put("motivo_des","Variazione delle condizioni del paziente");						dbr.put("motivo_des","Scadenza del periodo");
					else 												//mod elisa b 27/09/11
						//dbr.put("motivo_des","Scadenza del periodo");						dbr.put("motivo_des","Variazione delle condizioni del paziente");
					String conferma = ""+dbr.get("conferma");
					if(conferma.trim().equals("1")) 
						dbr.put("conferma_des","Si");
					else 
						dbr.put("conferma_des","No");
					//System.out.println(" RECORD CORRENTE === " +dbr.getHashtable().toString());
				}
			} else 
				return null;
				
			dbgriglia.close();
			dbc.close();
			super.close(dbc);
			done=true;
			
			return vdbg;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryGrid()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}


	public void delete(myLogin mylogin,ISASRecord dbr)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
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
				}catch(Exception e2){
					System.out.println(e2);
				}
			}
		}
	}


    public Vector insertAllRivalutazioni(myLogin mylogin,Hashtable hin,Vector hv1)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done=false;
		String cartella=null;
		String pr_data=null;
        String id_caso=null;
		String tipouvg=null;
		String progr=null;

		ISASConnection dbc=null;
		try {
			cartella=(String)hin.get("n_cartella");
			pr_data=(String)hin.get("pr_data");
            id_caso=(String)hin.get("id_caso");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		
		try{
			dbc=super.logIn(mylogin);

	    	dbc.startTransaction(); //gb 17/07/07

			for(Enumeration en=hv1.elements(); en.hasMoreElements(); )  {
                Hashtable h_r=(Hashtable)en.nextElement();
				Enumeration n=hin.keys();
				while(n.hasMoreElements()){
					String e=(String)n.nextElement();
					h_r.put(e,hin.get(e));
				}
				h_r.put("tempo_t", h_r.get("progr"));
				ISASRecord rec_riv = gestore_rivalutazione.insert(dbc, h_r);
				// aggiornamento tempo T su tabella CASO
				ISASRecord rec_caso = gestore_caso.updateCaso(dbc, h_r);
				if (rec_caso != null)
					printError("- aggiornato tabella CASO: tempoT=["+rec_caso.get("tempo_t")+"]");
			}

			String myselect="Select * from rl_rivalutazione where "+
						"n_cartella="+cartella+
						" and pr_data="+formatDate(dbc,(String)hin.get("pr_data"))+
						" and id_caso="+id_caso;
			ISASCursor dbgriglia=dbc.startCursor(myselect);
            Vector vdbg=dbgriglia.getAllRecord();
			
	        dbc.commitTransaction(); 

			dbc.close();
			super.close(dbc);
			done=true;
			return vdbg;
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
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e1);
			}
			throw new SQLException("Errore eseguendo una insert() - ",  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	
	// 28/04/11
	public Boolean query_checkDtConcl(myLogin mylogin, Hashtable h) throws  SQLException
	{
		boolean done=false;
		ISASConnection dbc=null;
		ISASCursor dbcur = null;
		boolean trovato = true;
		try{
			//System.out.println("HASH IN query_checkDtConcl: "+ h.toString());
			dbc=super.logIn(mylogin);
			
			String selg = "SELECT * FROM rl_rivalutazione" +
				" WHERE n_cartella = " + (String)h.get("n_cartella") +
                " AND pr_data = " + formatDate(dbc,(String)h.get("pr_data")) +
                " AND id_caso = " + (String)h.get("id_caso") +
				" AND dt_rivalutazione > " + formatDate(dbc,(String)h.get("dt_chiusura"));
			System.out.println("RlRivalutazioniEJB/query_checkDtConcl: " + selg);

			dbcur = dbc.startCursor(selg);
			trovato =  ((dbcur != null) && (dbcur.getDimension() > 0));
							
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			
			return new Boolean(!trovato);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_checkDtConcl()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	
	
	
	
	private void printError(String msg)
	{
		if(mydebug)
			System.out.println("RLRivalutazioneEJB: " + msg);
	}
	
}


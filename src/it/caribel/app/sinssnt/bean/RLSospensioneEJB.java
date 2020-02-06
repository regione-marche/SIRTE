package it.caribel.app.sinssnt.bean;

//============================================================================
//CARIBEL S.r.l.
//----------------------------------------------------------------------------
//
// 17/01/2011 - EJB di connessione alla procedura SINSSNT Tabella rl_Sospensione
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
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasiSosp;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;

public class RLSospensioneEJB extends SINSSNTConnectionEJB
{
	private GestCasiSosp gestore_casiSosp = new GestCasiSosp();

	private boolean mydebug = true;

	public RLSospensioneEJB() {}


	public ISASRecord update(myLogin mylogin, Hashtable hin, Vector vh)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		boolean done=false;

		ISASConnection dbc=null;
		ISASRecord dba=null;

		Hashtable h=new Hashtable();
		try{
            dbc=super.logIn(mylogin);

			dbc.startTransaction(); //gb 17/07/07

			// elimino tutti gli elementi presenti sul DB
			gestore_casiSosp.deleteAllSospCaso(dbc, hin);

			// iserisco tutti gli elementi arrivati col Vector vh
			gestore_casiSosp.insertAllSospCaso(dbc, vh, hin);

            dba = dbc.newRecord("caso_sospensione");

			Enumeration n = hin.keys();
			while(n.hasMoreElements())	{
				String e=(String)n.nextElement();
				dba.put(e,h.get(e));
			}

			dbc.commitTransaction(); //gb 17/07/07

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
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);

	    	dbc.startTransaction(); 

            deleteIng(dbc,h);

	        dbc.commitTransaction(); 
			
			dbc.close();
            super.close(dbc);
			done=true;
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
		finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e3){System.out.println(e3);}
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
            String select ="SELECT * FROM caso_sospensione WHERE ";
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

			String select="SELECT * FROM caso_sospensione WHERE ";
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

	public Vector insertAllsospensioni(myLogin mylogin, Vector hv1, Hashtable hin)
		throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		boolean done=false;
		ISASConnection dbc=null;

		try{
			dbc=super.logIn(mylogin);

	    	dbc.startTransaction(); //gb 17/07/07
			System.out.println("RLSospensioneEJB: HASH insert: "+ hin.toString());
			System.out.println("RLSospensioneEJB: Vettore insert size: "+ hv1.size());
			
			// iserisco tutti gli elementi arrivati col Vector vh
			gestore_casiSosp.insertAllSospCaso(dbc, hv1, hin);

			Vector vdbg = gestore_casiSosp.getVettSospCaso(dbc, hin);

	        dbc.commitTransaction(); //gb 17/07/07

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

	public Vector query_sospensioni(myLogin mylogin, Hashtable h) throws  SQLException
	{
		boolean done=false;
		ISASConnection dbc=null;
		try{
			//System.out.println("HASH in : "+ h.toString());
			dbc=super.logIn(mylogin);

			Vector vdbg = gestore_casiSosp.getVettSospCaso(dbc, h);

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
}


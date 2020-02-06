package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
// Scheda Metastasi
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class SkMPalMetEJB extends SINSSNTConnectionEJB  {
public SkMPalMetEJB() {}
public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from skmpal_metastasi where "+
                                " n_cartella="+(String)h.get("n_cartella")+                                
                                " AND sks_progr="+(String)h.get("sks_progr");
		System.out.println("QueryKey SkMPalMetEJB:"+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryKey()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

		// 25/09/07: legge l'ultimo rec (per data e progr) e vi aggiunge gli isas record per la griglia
		public ISASRecord queryKeyLast(myLogin mylogin,Hashtable h) throws  SQLException 
		{
			boolean done = false;
			ISASConnection dbc = null;
			ISASRecord dbr = null;

		    try{
				dbc=super.logIn(mylogin);
				
				String mysel = "SELECT * FROM skmpal_metastasi" +
								" WHERE n_cartella = " + (String)h.get("n_cartella");								

				String myselCur = mysel + " ORDER BY sks_data DESC, sks_progr DESC";

				System.out.println("SkMPalMetEJB: QueryKeyLast - myselCur=["+myselCur+"]");
	        	ISASCursor dbcur = dbc.startCursor(myselCur);
				Vector vdbr = dbcur.getAllRecord();
				dbcur.close();

				if ((vdbr != null) && (vdbr.size() > 0)){
					ISASRecord dbr_1 = (ISASRecord)vdbr.firstElement();
					String myselRec = mysel + " AND sks_progr = " + dbr_1.get("sks_progr");
					System.out.println("SkMPalMetEJB: QueryKeyLast - myselRec=["+myselRec+"]");

					dbr = dbc.readRecord(myselRec);					
					if (dbr != null)
						dbr.put("griglia", (Vector)vdbr);
				}

				dbc.close();
				super.close(dbc);
				done=true;
		
				return dbr;
			}catch(Exception e){
				e.printStackTrace();
				throw new SQLException("Errore eseguendo una queryKeyLast()  ");
			}finally{
				if(!done){
					try{
						dbc.close();
						super.close(dbc);
					}catch(Exception e1){System.out.println(e1);}
				}
			}
		}



		
public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM skmpal_metastasi WHERE "+
                                "n_cartella="+(String)h.get("n_cartella");
                myselect=myselect+" ORDER BY sks_data DESC, sks_progr DESC";

                System.out.println("query skmpal_metastasi: "+myselect);
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
public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String n_cartella=null;
    int progr=0;
    ISASConnection dbc=null;
    try {
        n_cartella=(String)h.get("n_cartella");
    }catch (Exception e){
        e.printStackTrace();
		throw new SQLException("SKMPALMET insert: Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        dbc.startTransaction();
        ISASRecord dbr=dbc.newRecord("skmpal_metastasi");
        //INSERISCO NELLA TABELLA METASTASI
        /*Mi vado a calcolare il progressivo*/
        String selProg = "SELECT MAX(sks_progr) progr FROM skmpal_metastasi WHERE "+
                         "n_cartella="+n_cartella;
        ISASRecord dbrProg = dbc.readRecord(selProg);
        if(dbrProg.get("progr")!=null){
            System.out.println("Dentro l'if");
            progr=Integer.parseInt(""+dbrProg.get("progr"));
            System.out.println("Progr:"+progr);
        }
        progr++;
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbr.put("sks_progr", ""+progr);

        System.out.println("Ho scritto il record:"+dbr.getHashtable().toString());
        dbc.writeRecord(dbr);
        String myselect="SELECT * FROM skmpal_metastasi WHERE n_cartella="+n_cartella+
                        " AND sks_progr="+progr;
        dbr=dbc.readRecord(myselect);
        System.out.println("select skmpal_metastasi insert: "+myselect);

			// 25/09/07
			if (dbr != null)
				dbr.put("griglia", (Vector)leggiTuttiRec(dbc, n_cartella));

        dbc.commitTransaction();
        dbc.close();
        super.close(dbc);
        done=true;
        System.out.println("select skmpal_metastasi insert: "+dbr.getHashtable().toString());
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

public ISASRecord update(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;

        String progr=null;
	ISASConnection dbc=null;
	try {
		cartella=dbr.get("n_cartella").toString();
                 progr=""+dbr.get("sks_progr");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="SELECT * FROM skmpal_metastasi WHERE "+
                                "n_cartella="+cartella+
                                " AND sks_progr="+progr;
		dbr=dbc.readRecord(myselect);

			// 25/09/07
			if (dbr != null)
				dbr.put("griglia", (Vector)leggiTuttiRec(dbc, cartella));

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

public void delete(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
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
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}
}

		// 25/09/07
		private Vector leggiTuttiRec(ISASConnection mydbc, String cart) throws Exception
		{
			String myselect = "SELECT * FROM skmpal_metastasi" +
								" WHERE n_cartella = " + cart +
								" ORDER BY sks_data DESC, sks_progr DESC";

	        ISASCursor dbcur = mydbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			return vdbr;
		}


}

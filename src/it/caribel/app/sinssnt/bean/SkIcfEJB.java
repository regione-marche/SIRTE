package it.caribel.app.sinssnt.bean;

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class SkIcfEJB extends SINSSNTConnectionEJB  {

public SkIcfEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		
		// 19/01/10 ---
		String cont = (String)h.get("n_contatto");
		String data2 = (String)h.get("data");
		boolean conCont = ((cont != null) && (!cont.trim().equals("")));
		// per AS: nuova tabella senza "n_contatto", ma con "data" in chiave
		String nmTab = (conCont?"":"ass_") + "skicf";
		// 19/01/10 ---

		String myselect="SELECT *"+
				" FROM " + nmTab +
				" WHERE n_cartella = " + (String)h.get("n_cartella"); 
		if (conCont)
            myselect += " AND n_contatto = " + (String)h.get("n_contatto");
		else if ((data2 != null) && (!data2.trim().equals("")))
			myselect += " AND data = " + formatDate(dbc,data2);
		System.out.println("SkicfEJB.queryKey - myselect=["+myselect+"]");
		
		ISASRecord dbr=dbc.readRecord(myselect);
        if (dbr != null)
            dbr.put("des_operatore", decodifica("operatori","codice",dbr.get("operatore"),"nvl(cognome,'') || nvl(nome,'')",dbc));
        
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

public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;
	ISASConnection dbc=null;
	try {
		cartella=(String)h.get("n_cartella");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("SkICF-INSERT-->Errore: manca la chiave primaria");
	}
	try{
		// 19/01/10 ---
		String cont = (String)h.get("n_contatto");
		String data2 = (String)h.get("data");
		boolean conCont = ((cont != null) && (!cont.trim().equals("")));
		// per AS: nuova tabella senza "n_contatto", ma con "data" in chiave
		String nmTab = (conCont?"":"ass_") + "skicf";
		// 19/01/10 ---
	
		dbc=super.logIn(mylogin);
        dbc.startTransaction();
		ISASRecord dbr=dbc.newRecord(nmTab);
		Enumeration n=h.keys();
		while(n.hasMoreElements()) {
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		
		if (conCont) // 19/01/10: solo nel caso FISIOT
            AggiornaTabellaSkFis(dbc,h);
			 
        dbc.commitTransaction();
		/*String myselect="Select * from SkIcf where "+
			" n_cartella='"+cartella+"'"+
                        " AND n_contatto='"+contatto+"'";
		dbr=dbc.readRecord(myselect);*/
                dbr=queryKey(mylogin,h);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
                }
                throw e;
	}catch(ISASPermissionDeniedException e){
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
                }
                throw e;
	}catch(Exception e1){
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
private void AggiornaTabellaSkFis(ISASConnection dbc,Hashtable h) throws  SQLException
{
    String cartella=null;
    String contatto=null;
    try {
            cartella=(String)h.get("n_cartella");
            contatto=(String)h.get("n_contatto");
    }catch (Exception e){
            e.printStackTrace();
            throw new SQLException("SkICF-AggiornaTabellaSkFis-->Errore: manca la chiave primaria");
    }
    try
    {
            String select="SELECT * FROM skfis WHERE "+
      			" n_cartella="+cartella+
                        " AND n_contatto="+contatto;
            ISASRecord dbr=dbc.readRecord(select);
            if (dbr!=null)
            {
                dbr.put("skf_disab_strutture",new Integer(ConvertiInInt(h.get("punt_strutcorp"))));
//		System.out.println("funzioni"+ConvertiInInt(h.get("punt_funzcorp")))	;
                dbr.put("skf_disab_funzioni",new Integer(ConvertiInInt(h.get("punt_funzcorp"))));
//		System.out.println("funzioni"+	ConvertiInInt(h.get("punt_attivita")))	;
                dbr.put("skf_disab_attpart",new Integer(ConvertiInInt(h.get("punt_attivita"))));
//		System.out.println("funzioni"+	ConvertiInInt(h.get("punt_fattamb")))	;
                dbr.put("skf_disab_fattamb",new Integer(ConvertiInInt(h.get("punt_fattamb"))));
                dbc.writeRecord(dbr);
            }//fine dbr diverso da null
    }
    catch(Exception e)
    {
        e.printStackTrace();
        throw new SQLException("Errore eseguendo una AggiornaTabellaSkFis()");
    }
}
private int ConvertiInInt(Object campo)
{
    int ret=0;
    if (campo!=null)
    {
        try
        {
            ret=Integer.parseInt((String)campo);
//          System.out.println("qui "+ ret);
        }
        catch(Exception err)
        {
          ret=0;
          System.out.println("ERRORE NELLA CONVERSIONE DI UN INTERO "+ err);
        }
    }
    return  ret;
}

public ISASRecord update(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;
	ISASConnection dbc=null;
	try {
		cartella=(String)dbr.get("n_cartella");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("SkICF-Update-->Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
        dbc.startTransaction();
		
		// 19/01/10 ---
		String cont = (String)dbr.get("n_contatto");
		String data2 = (String)dbr.get("data");
		boolean conCont = ((cont != null) && (!cont.trim().equals("")));
		// 19/01/10 ---
		
		dbc.writeRecord(dbr);
		
		if (conCont) // 19/01/10: solo nel caso FISIOT
            AggiornaTabellaSkFis(dbc,dbr.getHashtable());
			
        dbc.commitTransaction();
/*		String myselect="Select * from SkIcf where "+
			" n_cartella='"+cartella+"'"+
                        " AND n_contatto='"+contatto+"'";
		dbr=dbc.readRecord(myselect);*/
                dbr=queryKey(mylogin,dbr.getHashtable());
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
                }
                throw e;
	}catch(ISASPermissionDeniedException e){
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
                }
                throw e;
	}catch(Exception e1){
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

private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
        String ret="NON ESISTE DECODIFICA";
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
                System.out.println("Decodifica-->tabella:"+ tabella+" Select: "+ mysel);
		ISASRecord dbtxt = dbc.readRecord(mysel);
                if (dbtxt!=null && dbtxt.get("descrizione")!=null)
                {
                    ret=(String)dbtxt.get("descrizione");
		}
                return ret;
	} catch (Exception ex) {
		return "NON ESISTE DECODIFICA";
	}
}

// 19/01/10
public Vector query_grigliaStorico(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		
		dbc=super.logIn(mylogin);
		
		// 19/01/10 ---
		String cont = (String)h.get("n_contatto");
		boolean conCont = ((cont != null) && (!cont.trim().equals("")));
		// per AS: nuova tabella senza "n_contatto", ma con "data" in chiave
		String nmTab = (conCont?"":"ass_") + "skicf";
		// 19/01/10 ---

		String dtChiusSk = (String)h.get("dt_chiusura");
		String critDtChius = "";
		if ((dtChiusSk != null) && (!dtChiusSk.trim().equals("")))		
			critDtChius = " AND data <= " + formatDate(dbc, dtChiusSk);
		
		String myselect = "SELECT *"+
			" FROM " + nmTab +
			" WHERE n_cartella = " + (String)h.get("n_cartella");
			
		if (conCont)
            myselect += " AND n_contatto = " + (String)h.get("n_contatto");
					
        myselect += critDtChius + " ORDER BY data DESC";
//		System.out.println("query_grigliaStorico - myselect: " + myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		
		if ((vdbr != null) && (vdbr.size() > 0))
		   {
		   for (int k=0; k<vdbr.size(); k++)
			{
			ISASRecord dbr_1 = (ISASRecord)vdbr.elementAt(k);

			dbr_1.put("des_operatore", decodifica("operatori","codice",dbr_1.get("operatore"),"nvl(cognome,'') || nvl(nome,'')",dbc));
			}
		   }

		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_grigliaStorico()  ");
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

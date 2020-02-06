package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 15/05/2000 - EJB di connessione alla procedura SINS Tabella comuvm
//
// paolo ciampolini
//
// ============================================================================

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
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
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class CommissUVMEJB extends SINSSNTConnectionEJB  {

public CommissUVMEJB() {}
	
	private boolean myDebug = true;
	
	private String ver = "1-";

	

private void decodificaOperatore(ISASConnection mydbc, ISASRecord dbr,
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
	System.out.println("CommissUVMEJB/decodificaOperatore/selS: " + selS);
	ISASRecord rec = mydbc.readRecord(selS);

	if (rec != null)
	{
	  strCognome = (String)rec.get("cognome");
	  strNome = (String)rec.get("nome");
	}
	dbr.put(dbName, strCognome + " " + strNome);
  }

  private void getZonaByDistretto(ISASConnection dbc, ISASRecord dbr) throws Exception
  {
	String strCod = (String) dbr.get("cm_cod_distr");
	String strCodZona = "";

	if ((strCod == null) || strCod.equals(""))
	{
	  dbr.put("cod_zona", "");
	  return;
	}

	String selS = "SELECT cod_zona FROM distretti" +
                " WHERE cod_distr = '" + strCod + "'";
	System.out.println("CommissUVMEJB/getZonaByDistretto/selS: " + selS);
	ISASRecord rec = dbc.readRecord(selS);
	if (rec != null)
	{
	  strCodZona = (String)rec.get("cod_zona");
	}
	dbr.put("cod_zona", strCodZona);
  }

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect = "Select *" +
				" from comuvm" +
				" where cm_cod_comm = '" + h.get("cm_cod_comm").toString()+ "'";
		System.out.println("CommissUVMEJB/queryKey/myselect: " + myselect);
		ISASRecord dbr=dbc.readRecord(myselect);

		if (dbr != null)
		{
		  decodificaOperatore(dbc, dbr, "cm_oper_resp", "descr_operatore");
		  getZonaByDistretto(dbc, dbr);
		  
			// 25/10/11 lettura componenti commissione
			leggiCompComm(dbc, dbr, h.get("cm_cod_comm").toString());
		}
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
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


public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	String punto = ver  + "query ";
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String codDistretto = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_COD_DISTRETTO_UVM);
		
		Vector vdbr = recuperaCommissioni(dbc, codDistretto);
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
	}finally{
		 logout_nothrow(punto, dbc);
   	}
}

	public Vector<ISASRecord> recuperaCommissioni(ISASConnection dbc,
			String codDistretto) throws ISASMisuseException, DBMisuseException,
			DBSQLException, ISASPermissionDeniedException {
	
		String punto = ver + "recuperaCommissioni ";
		String myselect = "Select * from comuvm ";
		String condWhere = "";
		ISASCursor dbcur =null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			if (ISASUtil.valida(codDistretto)) {
				condWhere = " cm_cod_distr = '" + codDistretto + "' ";
			}
			if (ISASUtil.valida(condWhere)) {
				myselect = myselect + " WHERE " + condWhere;
			}
			
			myselect += " ORDER BY cm_descr";

			LOG.trace(punto + " query >>" + myselect);
			dbcur = dbc.startCursor(myselect);
			vdbr = dbcur.getAllRecord();
		} finally {
			close_dbcur_nothrow(punto, dbcur);
		}
		return vdbr;
	}

public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from comuvm";

        //controllo valore corretto des_cittadin

        scr=(String)(h.get("cm_descr"));
	if (!(scr==null))
            if (!(scr.equals("")))
              {
	       scr=duplicateChar(scr,"'");
               myselect=myselect+" where cm_descr like '"+scr+"%'";
              }
	myselect=myselect+" ORDER BY cm_descr ";
        System.out.println("query GridCommissUVM: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		//Vector vdbr=dbcur.getAllRecord();
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
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

public ISASRecord insert(myLogin mylogin,Hashtable h, Vector vettCompComm)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
	/*try {
		codice = (String)h.get("cm_cod_comm");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}*/
	try{
		dbc=super.logIn(mylogin);
		
		dbc.startTransaction(); // 25/10/11
		
		ISASRecord dbr=dbc.newRecord("comuvm");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		String newProgr = calcolaProgr(dbc).toString();//calcolo il progressivo
		dbr.put("cm_cod_comm",newProgr);
		dbc.writeRecord(dbr);
		
		// 25/10/11
		if (vettCompComm != null) {
			mySystemOut("vettCompComm != null ");
			scriviAllCompComm(dbc, vettCompComm,newProgr.toString());
		}		
		
		String myselect = "Select *" +	
				" from comuvm" +
				" where cm_cod_comm = '" + newProgr + "'";
				//" where cm_cod_comm = '" + codice + "'";
        	System.out.println("CommissUVMEJB/insert/myselect: "+myselect);
		dbr=dbc.readRecord(myselect);
		if (dbr != null)
		{
		  decodificaOperatore(dbc, dbr, "cm_oper_resp", "descr_operatore");
		  getZonaByDistretto(dbc, dbr);
		  
		  	// 25/10/11 lettura componenti commissione
			leggiCompComm(dbc, dbr,dbr.get("cm_cod_comm").toString());
		}
		
		dbc.commitTransaction(); // 25/10/11
		
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	} catch(CariException ce)	{
			System.out.println("CommisUVMEJB.insert(): Eccezione= " + ce);
			ce.setISASRecord(null);
			try{
				System.out.println("CommisUVMEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
	}catch(DBRecordChangedException e){
			System.out.println("CommisUVMEJB.insert(): Eccezione= " + e);
			try{
				System.out.println("CommisUVMEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo la rollback() - " +  e1);
			}
			throw e;
	}catch(ISASPermissionDeniedException e){
			System.out.println("CommisUVMEJB.insert(): Eccezione= " + e);
			try{
				System.out.println("CommisUVMEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo la rollback() - "+  e1);
			}
			throw e;
	}catch(Exception e){
			System.out.println("CommisUVMEJB.insert(): Eccezione= " + e);
			try{
				System.out.println("CommisUVMEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
			e1.printStackTrace();
				throw new SQLException("Errore eseguendo la rollback() - " +  e1);
			}
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una insert() - " +  e);
			
	}finally{
			if (!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("CommisUVMEJB.insert(): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
	}				
}


public ISASRecord update(myLogin mylogin,ISASRecord dbr, Vector vettCompComm)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
	try {
		codice=(String)dbr.get("cm_cod_comm");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		
		dbc.startTransaction(); // 25/10/11
		
		dbc.writeRecord(dbr);
		
		// 25/10/11
		cancellaAllCompComm(dbc, dbr.get("cm_cod_comm").toString());
		if (vettCompComm != null) {
			mySystemOut("vettCompComm != null ");
			scriviAllCompComm(dbc, vettCompComm,dbr.get("cm_cod_comm").toString());
		}
		
		String myselect = "Select *" +
				" from comuvm" +
				" where cm_cod_comm = '" + codice + "'";
        	System.out.println("CommissUVMEJB/update/myselect: "+myselect);
		dbr=dbc.readRecord(myselect);
		if (dbr != null)
		{
		  decodificaOperatore(dbc, dbr, "cm_oper_resp", "descr_operatore");
		  getZonaByDistretto(dbc, dbr);
		  
			// 25/10/11 lettura componenti commissione
			leggiCompComm(dbc, dbr, dbr.get("cm_cod_comm").toString());		  
		}
		
		dbc.commitTransaction(); // 25/10/11
		
		
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(CariException ce){
			System.out.println("CommisUVMEJB.update(): Eccezione= " + ce);
			ce.setISASRecord(null);
			try	{
				System.out.println("CommisUVMEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1)	{
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
	}catch(DBRecordChangedException e){
		System.out.println("CommisUVMEJB.update(): Eccezione= " + e);
		try	{
			System.out.println("CommisUVMEJB.update() => ROLLBACK");
			dbc.rollbackTransaction();
		}catch(Exception e1)	{
			throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e1);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("CommisUVMEJB.update(): Eccezione= " + e);
		try	{
			System.out.println("CommisUVMEJB.update() => ROLLBACK");
			dbc.rollbackTransaction();
		}catch(Exception e1)	{
			throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(Exception e){
		e.printStackTrace();
		System.out.println("CommisUVMEJB.update(): Eccezione= " + e);
		try	{
			System.out.println("CommisUVMEJB.update() => ROLLBACK");
			dbc.rollbackTransaction();
		}catch(Exception e1)	{
			throw new SQLException("Errore eseguendo una rollback() - " +  e1);
		}
		throw new SQLException("Errore eseguendo una update() - " +  e);
	} finally	{
		if (!done){
			try	{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2)  {	
				System.out.println("CommisUVMEJB.update(): - Eccezione nella chiusura della connessione= " + e2);	
			}
		}
	}		
}


public void delete(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		
		dbc.startTransaction(); // 25/10/11
		
		// 25/10/11
		cancellaAllCompComm(dbc, dbr.get("cm_cod_comm").toString());		
		
		dbc.deleteRecord(dbr);
		
		dbc.commitTransaction(); // 25/10/11		
		
		dbc.close();
		super.close(dbc);
		done=true;
	}catch(DBRecordChangedException e) {
            debugMessage("CommisUVMEJB.delete(): Eccezione= " + e);
            try {
              System.out.println("CommisUVMEJB.delete() => ROLLBACK");
              dbc.rollbackTransaction();
            } catch(Exception e1) {
              throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
            }
            throw e;
	}catch(ISASPermissionDeniedException e) {
		System.out.println("CommisUVMEJB.delete(): Eccezione= " + e);
		try{
		  System.out.println("CommisUVMEJB.delete() => ROLLBACK");
		  dbc.rollbackTransaction();
		}catch(Exception e1){
		  throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("CommisUVMEJB.delete(): Eccezione= " + e);
		try{
		  System.out.println("CommisUVMEJB.delete() => ROLLBACK");
		  dbc.rollbackTransaction();
		}catch(Exception e1){
		  throw new SQLException("Errore eseguendo una rollback() - " +  e1);
		}
		throw new SQLException("Errore eseguendo una delete() - " +  e);
	} finally{
		if (!done){
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e2){
				System.out.println("CommisUVMEJB.delete(): - Eccezione nella chiusura della connessione= " + e2);
			}
		}
	}
}

public String duplicateChar(String s, String c) {
        if ((s == null) || (c == null)) return s;
        String mys = new String(s);
        int p = 0;
        while (true) {
                int q = mys.indexOf(c, p);
                if (q < 0) return mys;
                StringBuffer sb = new StringBuffer(mys);
                StringBuffer sb1 = sb.insert(q, c);
                mys = sb1.toString();
                p = q + c.length() + 1;
        }
}

public Vector query_ass(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	ISASCursor dbcur = null;
	try{
		dbc=super.logIn(mylogin);
		String n_cartella = h.get("n_cartella").toString();
		
/** 26/10/11	
		String myselect="select a.n_cartella, a.pr_data_seduta data, b.cm_descr descrizione, "+
		"trim(c.nome)||' '||trim(c.cognome) as responsabile " +
		"from puauvm a  join comuvm b    on a.pr_cod_comm = b.cm_cod_comm  left outer join operatori c on c.codice = b.cm_oper_resp "+
		"where pr_stato_convoc = '2' and n_cartella = " +n_cartella;
**/		
		// 26/10/11
		String myselect = "SELECT a.*, a.pr_data_seduta data," +
						" b.cm_cod_comm, b.cm_descr descrizione" +
						" FROM puauvm a, comuvm b" +
						" WHERE a.pr_cod_comm = b.cm_cod_comm" +
						" AND a.pr_stato_convoc = '2'" +
						" AND a.n_cartella = " + n_cartella;
		
		
		System.out.println("query_ass selectt "+ myselect);
		dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
        if (dbcur!=null) 
			dbcur.close();
		
		// 26/10/11 
		for(int i=0; i<vdbr.size(); i++) {
			ISASRecord rec = (ISASRecord) vdbr.get(i);
			if ((rec != null) && (rec.get("cm_cod_comm") != null))
				leggiRespComm(dbc, rec, rec.get("cm_cod_comm").toString()); 	
		}
		
		
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
			if (dbcur!=null) dbcur.close();
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}


	// 25/10/11
	private void leggiCompComm(ISASConnection dbc, ISASRecord dbr, String codComm) throws Exception
	{
		boolean done = false;
		ISASCursor dbcur=null;
		
		Hashtable h_tpOp = ManagerOperatore.getTipiOperatori(null);
		
		String select = "SELECT * FROM comuvm_compo"
					+ " WHERE cm_cod_comm ='" + codComm + "'"
					+ " ORDER BY pr_responsabile DESC, cm_progr";

		mySystemOut("  leggiCompComm == " + select);
		try
		{
			dbcur = dbc.startCursor(select);
			Vector vdbr = dbcur.getAllRecord();
			Vector result = new Vector();

			for(int i = 0; i < vdbr.size(); i++)
			{
				ISASRecord rec = (ISASRecord) vdbr.get(i);
				if(rec.get("pr_tipo") != null && !rec.get("pr_tipo").equals(""))
				{
					String qualifica = rec.get("pr_tipo").toString();
					if(h_tpOp.get(qualifica)!=null && !h_tpOp.get(qualifica).equals(""))
					rec.put("desc_qualifica", h_tpOp.get(qualifica).toString());
					String cognome = "";
					String nome = "";

					if(qualifica.equals(GestTpOp.CTS_COD_MMG))	{
						cognome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "mecogn", "cognome");
						nome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "menome", "nome");
					} else {
						cognome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "cognome", "cognome");
						nome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "nome", "nome");
					}

					rec.put("cognome_op", cognome);
					rec.put("nome_op", nome);
					rec.put("cognome_nome_op", cognome + " " + " "+ nome);
				}

				result.add(rec);
			}

			if(dbcur != null) dbcur.close();

/***
			for(int i = 0; i < result.size(); i++)
			{
				ISASRecord r = (ISASRecord) result.get(i);
				mySystemOut("leggiCompComm - RECORD " + i + " == " + r.getHashtable().toString());
			}
***/
			dbr.put("griglia_componenti", result);
			done=true;			
		}
		catch (Exception e)
		{
			System.out.println("CommisUVMEJB: leggiCompComm exc=[" + e + "]");
			throw e;
		}
		finally{
            if (!done){
	            if (dbcur != null)
	               	dbcur.close();	                
            }
        }
	}

	public void cancellaAllCompComm(ISASConnection dbc, String codComm) throws Exception{
		mySystemOut(" cancellaAllCompComm ");
		boolean done = false;
		ISASCursor dbcur=null;
		
		try
		{
			String sel =  "SELECT * FROM comuvm_compo"
					+ " WHERE cm_cod_comm = '" + codComm + "'";

			mySystemOut(" cancellaAllCompComm -- " + sel);
			dbcur = dbc.startCursor(sel);

			Vector vdbr = dbcur.getAllRecord();
	        if ((vdbr != null) && (vdbr.size() > 0))
       		{
	        	for(int i = 0; i < vdbr.size(); i++)
	        	{
	            	ISASRecord dbrec = (ISASRecord)vdbr.get(i);
	                if (dbrec != null)
	                {
		                String sel2 = sel +
						" AND cm_progr = " + dbrec.get("cm_progr");

						ISASRecord dbrD = dbc.readRecord(sel2);
	                    dbc.deleteRecord(dbrD);
	                }
	            }
       		}

	        if (dbcur != null)	dbcur.close();
			done=true;
		}
		catch(Exception e)
		{
			System.out.println("CommisUVMEJB: cancellaAllCompComm [" + e + "]");
			throw e;
		}
		finally{
            if (!done){
	            if (dbcur != null)
	               	dbcur.close();	                
            }
        }
	}

	private void scriviAllCompComm(ISASConnection dbc, Vector vCompComm,String cm_cod_comm) throws Exception
	{
		mySystemOut(" scriviAllCompComm ");
		try
		{
			for (int i = 0; i < vCompComm.size(); i++)
			{
				Hashtable hash = (Hashtable)vCompComm.get(i);
				mySystemOut("scriviAllCompComm HASH " + i + " == " + hash.toString());
				scriviCompComm(dbc, hash,cm_cod_comm,i);
			}
		}
		catch(Exception e)
		{
			System.out.println("CommisUVMEJB: scriviAllCompComm [" + e + "]");
			throw e;
		}
	}

	private void scriviCompComm(ISASConnection dbc, Hashtable h,String cm_cod_comm, int i) throws Exception
	{
		mySystemOut(" scriviCompComm -- H = " + h.toString());
		try
		{
			ISASRecord rec = dbc.newRecord("comuvm_compo");

			Enumeration e = h.keys();
			while (e.hasMoreElements())
			{
				String key = e.nextElement().toString();
				mySystemOut(" Chiave: " + key);
				rec.put(key, h.get(key));				
			}
			rec.put("cm_cod_comm", cm_cod_comm);
			rec.put("cm_progr", new Integer(i));
			
			
			mySystemOut(" Scrivere REC: " + rec.getHashtable().toString());
			dbc.writeRecord(rec);
			mySystemOut(" Scritto!!! ");
		}
		catch(Exception e)
		{
			System.out.println("CommisUVMEJB: scriviCompComm [" + e + "]");
			throw e;
		}
	}
	
	// 26/10/11
	private void leggiRespComm(ISASConnection dbc, ISASRecord dbr, String codComm) throws Exception
	{
		mySystemOut("  leggiRespComm: codComm=[" + codComm + "]");
		Hashtable h_tpOp = ManagerOperatore.getTipiOperatori(null);
		
		String select = "SELECT * FROM comuvm_compo"
					+ " WHERE cm_cod_comm = '" + codComm + "'"
					+ " AND pr_responsabile = 'S'";

		mySystemOut("  leggiRespComm == " + select);
		try	{
			ISASRecord rec = dbc.readRecord(select);

			if ((rec != null) 
			&& (rec.get("pr_tipo") != null && !rec.get("pr_tipo").equals(""))){
				String qualifica = rec.get("pr_tipo").toString();

				dbr.put("desc_qualifica", h_tpOp.get(qualifica).toString());
				String cognome = "";
				String nome = "";
				if(qualifica.equals(GestTpOp.CTS_COD_MMG))	{
					cognome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "mecogn", "cognome");
					nome = ISASUtil.getDecode(dbc, "medici", "mecodi", rec.get("pr_operatore").toString(), "menome", "nome");
				} else {
					cognome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "cognome", "cognome");
					nome = ISASUtil.getDecode(dbc, "operatori", "codice", rec.get("pr_operatore").toString(), "nome", "nome");
				}

				dbr.put("cognome_op", cognome);
				dbr.put("nome_op", nome);
				dbr.put("responsabile", cognome + " " + nome);
			} else
				dbr.put("responsabile", "");
		}catch (Exception e){
			System.out.println("CommisUVMEJB: leggiRespComm exc=[" + e + "]");
			throw e;
		}
	}
	
	private Integer calcolaProgr(ISASConnection dbc)throws Exception{
		String nomeMetodo = "calcolaProgr";
		try{
			int mass = 1;
			String selmax = "SELECT MAX (cm_cod_comm) massimo" +
					" FROM comuvm ";
			ISASRecord dbmax = dbc.readRecord(selmax);
			if (dbmax!=null && dbmax.get("massimo")!=null){
				Integer massimo = (Integer)dbmax.get("massimo");
				mass = massimo.intValue()+1;
			}
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT[] OUTPUT["+mass+"]");
			return new Integer(mass);
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	
	
	private void mySystemOut(String msg)
  	{
		if (myDebug)
			System.out.println("CommisUVMEJB: " + msg);
	}
}

package it.caribel.app.sinssnt.bean;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 16/07/2009 - EJB di connessione alla procedura SINS Tabella SkSvamaSoc
//
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.util.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.sinssnt.casi_adrsa.*; // 15/07/09

public class SkSvamaSocEJB extends SINSSNTConnectionEJB  {
public SkSvamaSocEJB() {}

	// 15/07/09
	private ScaleVal gest_scaleVal = new ScaleVal(); 
	
	// 13/09/12
	private EveUtils evUtl = new EveUtils();

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	ISASCursor dbcur = null; // 04/08/09
	try{
            ServerUtility su=new ServerUtility();
            dbc=super.logIn(mylogin);
            String cartella=(String)h.get("n_cartella");
           //bargi10/11/06 String pr_data=(String)h.get("pr_data");
            String data=(String)h.get("data_variazione");

            String myselect="SELECT * FROM svama_soc WHERE ";
            String sel="";
            sel=su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
            //bargi10/11/06sel=su.addWhere(sel,su.REL_AND,"pr_data",su.OP_EQ_NUM,formatDate(dbc,pr_data));
            sel=su.addWhere(sel,su.REL_AND,"data_variazione",su.OP_EQ_NUM,formatDate(dbc,data));
            myselect=myselect+sel;
            System.out.println("QUERYKEY=>SkSvamaSoc "+myselect);
            ISASRecord dbr=dbc.readRecord(myselect);
          //bargi 22/12/2006 controllo tempo T
			if(dbr==null){
/*** 08/06/09 m.: il tempo T viene imposto dal client			
					myselect="Select count(n_cartella)tot from svama_soc where "+
							"n_cartella="+(String)h.get("n_cartella")+ //bargi08/11/06" AND "+
							 " and data>="+formatDate(dbc,(String)h.get("data_inizio"));
								 if(h.get("data_fine")!=null && !((String)h.get("data_fine")).equals(""))
								 myselect+=" and data<="+formatDate(dbc,(String)h.get("data_fine"));
								 debugMessage("SELECT PER TEMPO_T "+myselect);
					ISASRecord dbT=dbc.readRecord(myselect);
					int t=0;
					if(dbT!=null){
						t=convNumDBToInt("tot",dbT);
						if(t>9)t=9;
					}
					debugMessage("TEMPO==>>"+t);
					if (t>0)
					{
***/
					if ((h.get("tempo_t") != null) && (Integer.parseInt((String)h.get("tempo_t")) >= 0)) { // 08/06/09 m.					
						dbr=dbc.newRecord("cartella");
						 Enumeration n=h.keys();//� stupido ma per ora 
						 //bisogna fare cos� per non sbiancare campi passati..
						while(n.hasMoreElements()){
							String e=(String)n.nextElement();
							dbr.put(e,h.get(e));
						}
// 08/06/09 m.				dbr.put("tempo_t",new Integer(t));
						dbr.put("stato","insert");
						debugMessage("dbr chetorna..."+dbr.getHashtable().toString());
					}
				}
				else {
					it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
		            dbr.put("operatore_desc",util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"cod_operatore",'S'),
		                    "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));

					// 29/04/13		
					decodComuneProv(dbc, dbr, (String)dbr.get("soc_rifer_comune"), "ref");	
					if ((dbr.get("soc_assistente") != null) && (!((String)dbr.get("soc_assistente")).trim().equals("")))
						dbr.put("soc_assistente_nome", util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"soc_assistente",'S'),
								"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));				
					leggiSvamaAss(dbc, dbr, h);
					dbr.put("griglia_rete", (Vector)leggiAllSvamaFam(dbc, dbcur, h));
				}

            dbc.close();
            super.close(dbc);
            done=true;
            return dbr;
	}catch(Exception e){
		System.out.println(e);
		throw new SQLException("Errore eseguendo una queryKey()  ");
	}finally{
		if(!done){
			try{
				if (dbcur != null)// 04/08/09
					dbcur.close();
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
		String myselect="Select * from svama_soc where "+
                                "n_cartella="+(String)h.get("n_cartella");//bargi10/11/06" AND "+
                            //bargi10/11/06    "pr_data="+formatDate(dbc,(String)h.get("pr_data"))+
							
/*** 20/09/12: si mostrano comunque tutte							
		// 14/09/12: x gestire vecchie e nuove versioni delle scale
		Hashtable h_conf = (Hashtable)evUtl.leggiConf(dbc, (String)h.get("cod_operatore"), new String[]{"ABL_SKSOC_DR2259"});
		if ((h_conf.get("ABL_SKSOC_DR2259") != null) && (h_conf.get("ABL_SKSOC_DR2259").toString().trim().equals("SI"))) {
			int versScala = 0;
			if ((h.get("flg_new_version") != null) && (!h.get("flg_new_version").toString().trim().equals(""))) {
				try {
					versScala = Integer.parseInt(h.get("flg_new_version").toString().trim());
				} catch (NumberFormatException nfe) {
					versScala = 0;
				}
			}
			if (versScala > 0)
				myselect += " AND flg_new_version = " + versScala;
			else
				myselect += " AND ((flg_new_version IS NULL) OR (flg_new_version = 0))";
		}
		// 14/09/12-------------
**/
		
        myselect += " ORDER BY data_variazione DESC";
//		System.out.println("SkSvamaSocEJB.query: myselect=["+myselect+"]");
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		System.out.println(e);
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

public ISASRecord insertSocEFamEAss(myLogin mylogin, Hashtable h, Vector vettR) 
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
{
    boolean done=false;
    String cartella=null;
//bargi10/11/06    String pr_data=null;
    String data=null;
    ISASConnection dbc=null;
	ISASCursor dbcur = null; // 04/08/09

    try {
        cartella=(String)h.get("n_cartella");
        data=(String)h.get("data_variazione");
       //bargi10/11/06 pr_data=(String)h.get("pr_data");
    }catch (Exception e){
        System.out.println(e);
		throw new SQLException("Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);

		dbc.startTransaction(); // 04/08/09

        ISASRecord dbr=dbc.newRecord("svama_soc");
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);

		// 07/05/13: scrittura SVAMA_ASS
		scriviSvamaAss(dbc, h);
		
		// 04/08/09
		if (vettR != null)
			scriviAllSvamaFam(dbc, vettR, h);

        String myselect="SELECT * FROM svama_soc WHERE "+
			"n_cartella="+cartella+
		//bargi10/11/06	" AND pr_data="+formatDate(dbc,pr_data)+
			" AND data_variazione="+formatDate(dbc,data);
        dbr=dbc.readRecord(myselect);

    	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
        dbr.put("operatore_desc",util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"cod_operatore",'S'),
                "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));

		// 29/04/13		
		decodComuneProv(dbc, dbr, (String)dbr.get("soc_rifer_comune"), "ref");	
		if ((dbr.get("soc_assistente") != null) && (!((String)dbr.get("soc_assistente")).trim().equals("")))
			dbr.put("soc_assistente_nome", util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"soc_assistente",'S'),
					"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));		
		leggiSvamaAss(dbc, dbr, h);		
		// 04/08/09
		dbr.put("griglia_rete", (Vector)leggiAllSvamaFam(dbc, dbcur, h));

		dbc.commitTransaction(); // 04/08/09

        dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
    }catch(DBRecordChangedException e){
        System.out.println(e);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw e;
    }catch(ISASPermissionDeniedException e){
        System.out.println(e);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw e;
    }catch(Exception e1){
        System.out.println(e1);e1.printStackTrace();
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw newEjbException("\n Errore eseguendo una rollback() - "+  e2, e2);
		}
		throw newEjbException("Errore eseguendo una insert() - "+  e1, e1);
    }finally{
   	    if(!done){
   	        try{
				if (dbcur != null)// 04/08/09
					dbcur.close();
		dbc.close();
		super.close(dbc);
		}catch(Exception e2){System.out.println(e2);}
   	        }
   	}
}

public ISASRecord query_insert(myLogin mylogin,Hashtable h) throws
SQLException, ISASPermissionDeniedException {
	boolean done=false;
	ISASConnection dbc=null;
	ISASCursor dbcur = null; // 04/08/09
	String cartella = "";
     //bargi10/11/06   String pr_data = "";
	String data = "";
        ISASRecord dbr=null;
        try{
        	dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                try{
			cartella = (String)h.get("n_cartella");
		//bargi10/11/06	pr_data = (String)h.get("pr_data");
                        data = (String)h.get("data_variazione");
                }catch(Exception ex){
                    throw new SQLException("SkSvamaSocEJB.query_insert()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
                }
                String sel = "SELECT * FROM svama_soc WHERE n_cartella="+cartella+
                     //bargi10/11/06        " AND pr_data="+formatDate(dbc,pr_data)+
                             " AND data_variazione="+formatDate(dbc,data);
                dbr=dbc.readRecord(sel);
				//bargi 22/12/2006 controllo tempo T
			if(dbr==null){
/*** 08/06/09 m.: il tempo T viene imposto dal client
					String myselect="Select count(n_cartella)tot from svama_soc where "+
							"n_cartella="+(String)h.get("n_cartella")+ //bargi08/11/06" AND "+
							 " and data>="+formatDate(dbc,(String)h.get("data_inizio"));
								 if(h.get("data_fine")!=null && !((String)h.get("data_fine")).equals(""))
								 myselect+=" and data<="+formatDate(dbc,(String)h.get("data_fine"));
								 debugMessage("SELECT PER TEMPO_T "+myselect);
					ISASRecord dbT=dbc.readRecord(myselect);
					int t=0;
					if(dbT!=null){
						t=convNumDBToInt("tot",dbT);
						if(t>9)t=9;
					}
					debugMessage("TEMPO==>>"+t);

					if (t>0)
					{
***/
					if ((h.get("tempo_t") != null) && (Integer.parseInt((String)h.get("tempo_t")) >= 0)) { // 08/06/09 m.
						dbr=dbc.newRecord("cartella");
						 Enumeration n=h.keys();//� stupido ma per ora 
						 //bisogna fare cos� per non sbiancare campi passati..
						while(n.hasMoreElements()){
							String e=(String)n.nextElement();
							dbr.put(e,h.get(e));
						}
// 08/06/09 m.			dbr.put("tempo_t",new Integer(t));
						dbr.put("stato","insert");
						debugMessage("dbr chetorna..."+dbr.getHashtable().toString());
					}
				}
				else {
					it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
		            dbr.put("operatore_desc",util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"cod_operatore",'S'),
		                    "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));

					// 29/04/13		
					decodComuneProv(dbc, dbr, (String)dbr.get("soc_rifer_comune"), "ref");	
					if ((dbr.get("soc_assistente") != null) && (!((String)dbr.get("soc_assistente")).trim().equals("")))
						dbr.put("soc_assistente_nome", util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"soc_assistente",'S'),
								"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));					
					leggiSvamaAss(dbc, dbr, h);		
					// 04/08/09
					dbr.put("griglia_rete", (Vector)leggiAllSvamaFam(dbc, dbcur, h));
				}

                dbc.close();
		super.close(dbc);
		done=true;
                return dbr;
        }catch(ISASPermissionDeniedException e){
		System.out.println("eccezione permesso negato "+e);
		return null;
	}catch(Exception e){
		System.out.println(e);
		throw new SQLException("Errore eseguendo una query_insert()  ");
	}finally{
		if(!done){
			try{
				if (dbcur != null)// 04/08/09
					dbcur.close();
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}


public ISASRecord updateSocEFamEAss(myLogin mylogin,ISASRecord dbr, Vector vettR)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
{
    boolean done=false;
    String cartella=null;
    String data=null;
//bargi10/11/06    String pr_data=null;
    ISASConnection dbc=null;
	ISASCursor dbcur = null; // 04/08/09
    try {
        cartella=(String)dbr.get("n_cartella");
        data=(String)dbr.get("data_variazione");
     //bargi10/11/06   pr_data=(String)dbr.get("pr_data");
    }catch (Exception e){
        System.out.println(e);
		throw new SQLException("Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);

		dbc.startTransaction(); // 04/08/09

        dbc.writeRecord(dbr);
		
		Hashtable h0 = (Hashtable)dbr.getHashtable();
		
		// 07/05/13: scrittura SVAMA_ASS
		cancSvamaAss(dbc, h0);
		scriviSvamaAss(dbc, h0);
		
		// 04/08/09 ---
		cancAllSvamaFam(dbc, dbcur, h0);
		if (vettR != null)
			scriviAllSvamaFam(dbc, vettR, h0);
		// 04/08/09 ---

        String myselect="SELECT * FROM svama_soc WHERE "+
			"n_cartella="+cartella+
		//bargi10/11/06	" AND pr_data="+formatDate(dbc,pr_data)+
			" AND data_variazione="+formatDate(dbc,data);
        dbr=dbc.readRecord(myselect);

    	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
        dbr.put("operatore_desc",util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"cod_operatore",'S'),
                "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));
  
		// 29/04/13		
		decodComuneProv(dbc, dbr, (String)dbr.get("soc_rifer_comune"), "ref");
		if ((dbr.get("soc_assistente") != null) && (!((String)dbr.get("soc_assistente")).trim().equals("")))
			dbr.put("soc_assistente_nome", util.getDecode(dbc,"operatori","codice",util.getObjectField(dbr,"soc_assistente",'S'),
					"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));		
		leggiSvamaAss(dbc, dbr, h0);
		// 04/08/09
		dbr.put("griglia_rete", (Vector)leggiAllSvamaFam(dbc, dbcur, h0));

		dbc.commitTransaction(); // 04/08/09

	    dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
    }catch(DBRecordChangedException e){
        System.out.println(e);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw e;
    }catch(ISASPermissionDeniedException e){
        System.out.println(e);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw e;
    }catch(Exception e1){
        System.out.println(e1);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw newEjbException("Errore eseguendo una update() - "+  e1,e1);
    }finally{
   	    if(!done){
   	        try{
				if (dbcur != null)// 04/08/09
					dbcur.close();
		dbc.close();
			super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}

}


public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    ISASConnection dbc=null;
	ISASCursor dbcur = null; // 04/08/09
    try{
        dbc=super.logIn(mylogin);

		dbc.startTransaction(); // 04/08/09

		Hashtable h0 = (Hashtable)dbr.getHashtable();
		
		// 07/05/13
		cancSvamaAss(dbc, h0);		
		
		// 04/08/09 
		cancAllSvamaFam(dbc, dbcur, h0);
		
        dbc.deleteRecord(dbr);

		dbc.commitTransaction(); // 04/08/09

		dbc.close();
		super.close(dbc);
		done=true;
    }catch(DBRecordChangedException e){
        System.out.println(e);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw e;
    }catch(ISASPermissionDeniedException e){
        System.out.println(e);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw e;
    }catch(Exception e1){
        System.out.println(e1);
        try{
			dbc.rollbackTransaction();
		}catch(Exception e2){
			throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
		}
		throw new SQLException("Errore eseguendo una delete() - "+  e1);
    }finally{
   	    if(!done){
   	        try{
				if (dbcur != null)// 04/08/09
					dbcur.close();
		dbc.close();
			super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}

}


	// 19/09/12: duplicazione parziale se il record da duplicare � della versione vecchia
	public Integer duplicaMaxRecordSvamaSoc(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException,ISASPermissionDeniedException, SQLException
	{
		boolean done = false;
		String cart = null;
        String dataDup = null;
		String codOper = null;
		String tempoT = null;
 
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);

			dbc.startTransaction(); // 04/08/09

			cart = (String)h.get("n_cartella");
			dataDup = (String)h.get("data");// data in cui verr� duplicato il record
			codOper = (String)h.get("cod_operatore");
			tempoT = (String)h.get("tempo_t");
			
			String myselect = "SELECT a.*"
							+ " FROM svama_soc a"
							+ " WHERE a.n_cartella = " + cart
							+ " AND a.data_variazione IN (SELECT MAX(b.data_variazione) FROM svama_soc b"
							+ 	" WHERE b.n_cartella = a.n_cartella)"; 
			
			ISASRecord dbr_1 = (ISASRecord)dbc.readRecord(myselect);
			
			if ((dbr_1 != null) && (dbr_1.get("data_variazione") != null)) {
				// cntrl che la dtDuplicaz non esista gi� sul DB
				String maxData = ((java.sql.Date)dbr_1.get("data_variazione")).toString();
				if ((!dataDup.trim().equals(""))
				&& (!dataDup.trim().equals(maxData))) {
					ISASRecord dbr_2 = dbc.newRecord("svama_soc");
					Hashtable h_1 = (Hashtable) dbr_1.getHashtable();
					// duplico vecchi valori nel nuovo record
					Enumeration enuK = h_1.keys();
					while (enuK.hasMoreElements()) {
						String eK = (String) enuK.nextElement();
						dbr_2.put(eK, h_1.get(eK));
					}
					// inserisco nuovi valori, sbiancando quelli che devono impostati dall'utente
					dbr_2.put("data_variazione", dataDup);
					dbr_2.put("cod_operatore", codOper);
					dbr_2.put("nome", codOper + " (duplicato)");
// 04/03/13			dbr_2.put("data_test", "");
					dbr_2.put("data_test", dataDup);					

					//duplica tutti i campi, tranne i campi di chi ha modificato la scheda e la data
					dbr_2.put("operatore_mod", "");
					dbr_2.put("mod_data", "");
					
					dbr_2.put("tempo_t", new Integer(tempoT));
					
					dbc.writeRecord(dbr_2);
					
					// duplicazione familiari
					duplicaMaxRecordSvamaFam(dbc, cart, maxData, dataDup, codOper);
					
					// duplicazione assistenza
					duplicaMaxRecordSvamaAss(dbc, cart, maxData, dataDup, codOper);					
				}
			}

			dbc.commitTransaction(); // 04/08/09

			dbc.close();
			super.close(dbc);
			done=true;

			return new Integer(1);
		}
		catch(DBRecordChangedException e){
			System.out.println(e);
	        try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println(e);
	        try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
			}
			throw e;
		}catch(Exception e){
			System.out.println(e);
	        try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("\n Errore eseguendo una rollback() - "+  e2);
			}
			throw new SQLException("Errore eseguendo una duplicaMaxRecord() - "+  e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	
	// 19/09/12
	private void duplicaMaxRecordSvamaFam(ISASConnection mydbc, String cart, String dtMax,
								String dtDup, String codOper) throws Exception 
	{
		boolean done = false;
		ISASCursor dbcur = null;

		try {
			Hashtable h_0 = new Hashtable();
			h_0.put("n_cartella", cart);
			h_0.put("data_variazione", dtMax);
			String myselect = getSelSvamaTab(mydbc, h_0, "fam");														
							
			dbcur = mydbc.startCursor(myselect);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {
					ISASRecord dbr_1 = (ISASRecord) dbcur.getRecord();

					if ((dbr_1 != null) && (dbr_1.get("data_variazione") != null)) {
						// cntrl che la dtDuplicaz non esista gi� sul DB
						if ((!dtDup.trim().equals(""))
						&& (!dtDup.trim().equals(dtMax))) {
							ISASRecord dbr_2 = mydbc.newRecord("svama_fam");
							Hashtable h_1 = (Hashtable)dbr_1.getHashtable();
							// duplico vecchi valori nel nuovo record
							Enumeration enuK = h_1.keys();
							while (enuK.hasMoreElements()) {
								String eK = (String) enuK.nextElement();
								dbr_2.put(eK, h_1.get(eK));
							}
							// inserisco nuovi valori, sbiancando quelli che devono impostati dall'utente
							dbr_2.put("data_variazione", dtDup);

							mydbc.writeRecord(dbr_2);
						}
					}
				}
			}

			done = true;
		} catch (Exception e) {
			System.out.println("SkSvamaSocEJB.duplicaMaxRecordSvamaFam(): eccezione=[" + e	+ "]");
			throw e;
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}
	}	
	
	private Vector leggiAllSvamaFam(ISASConnection dbc, ISASCursor dbcur, Hashtable h0) throws Exception
    {
		try {
			Vector vRet = new Vector();
			String sel = getSelSvamaTab(dbc, h0, "fam");
			sel += " ORDER BY fam_tipo, fam_parentela, fam_cognomenome";

			dbcur = dbc.startCursor(sel);
			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				while (dbcur.next()) {			
					ISASRecord dbr_1 = dbcur.getRecord();
					String tpFam = (String)dbr_1.get("fam_tipo");
					String tpFamDesc = "";
					if (tpFam != null)
						tpFamDesc = (tpFam.trim().equals("0")?"Parente":"Altra persona attiva");
					dbr_1.put("fam_tipo_desc", tpFamDesc);
					decodComuneProv(dbc, dbr_1, (String)dbr_1.get("fam_comune_res"), "fam");	
					if (dbr_1 !=  null) 
						vRet.addElement(dbr_1);
				}
				dbcur.close();
			}
			
			return vRet;	
		} finally {
			if (dbcur !=null)
				dbcur.close();
		}
	}

	// 04/08/09
	private void cancAllSvamaFam(ISASConnection dbc, ISASCursor dbcur, Hashtable h0) throws Exception
    {
		String sel = getSelSvamaTab(dbc, h0, "fam");

		dbcur = dbc.startCursor(sel);
        if ((dbcur != null) && (dbcur.getDimension() > 0)) {
			while (dbcur.next()) {
               	ISASRecord dbr_1 = (ISASRecord)dbcur.getRecord();
				String progr = "" + dbr_1.get("fam_progr");
				String sel2 = sel + " AND fam_progr = " + progr;
	                    
				ISASRecord dbrD = dbc.readRecord(sel2);
                dbc.deleteRecord(dbrD);
			}
		}
		if (dbcur != null)
			dbcur.close();
	}

	// 04/08/09
	private void scriviAllSvamaFam(ISASConnection dbc, Vector vett, Hashtable h0) throws Exception
    {
		for (int k=0; k<vett.size(); k++){
			Hashtable hashR = (Hashtable)vett.elementAt(k);
			scriviSvamaFam(dbc, hashR, (k+1));
		}
	}	

	// 04/08/09
	private void scriviSvamaFam(ISASConnection dbc, Hashtable h, int num) throws Exception
	{
		ISASRecord rec = dbc.newRecord("svama_fam");
			
		Enumeration e = h.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			rec.put(key, h.get(key));
		}

		rec.put("fam_progr", new Integer(num));
		dbc.writeRecord(rec);	
	}

	// 04/08/09
	private String getSelSvamaTab(ISASConnection mydbc, Hashtable h0, String nmTabSvama) throws Exception
    {
		return "SELECT * FROM svama_" + nmTabSvama +
					" WHERE n_cartella = " + h0.get("n_cartella") +
					" AND data_variazione = " + formatDate(mydbc, ""+h0.get("data_variazione"));
	}


	// 29/04/13
	private void decodComuneProv(ISASConnection dbc, ISASRecord dbr, String codCom, String nmFld) throws Exception
	{
		if ((codCom != null) && (!codCom.trim().equals(""))) {
			String sel = "SELECT * FROM comuni WHERE codice = '" + codCom + "'";
			ISASRecord dbrC = dbc.readRecord(sel);
			if (dbrC != null) {
				if (dbrC.get("descrizione") != null)
					dbr.put("desc_"+nmFld+"_comune", dbrC.get("descrizione").toString().trim());
				if (dbrC.get("cod_pro") != null)
					dbr.put(nmFld+"_prov", dbrC.get("cod_pro").toString().trim());
			}
		}
	}


	
	// 07/05/13
	private void leggiSvamaAss(ISASConnection dbc, ISASRecord dbr, Hashtable h0) throws Exception
	{
		String myselect = getSelSvamaTab(dbc, h0, "ass");
					
		ISASRecord dbr_1 = (ISASRecord)dbc.readRecord(myselect);	
		if (dbr_1 != null) {
			Hashtable h_1 = (Hashtable)dbr_1.getHashtable();
			Enumeration n = h_1.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				if ((e != null) && (e.startsWith("ass_")))
					dbr.put(e,h_1.get(e));
			}
		}
	}
	
	// 07/05/13
	private void scriviSvamaAss(ISASConnection dbc, Hashtable h0) throws Exception
	{
		ISASRecord rec = dbc.newRecord("svama_ass");
			
		Enumeration e = h0.keys();
		while (e.hasMoreElements()) {
			String key = e.nextElement().toString();
			rec.put(key, h0.get(key));
		}

		dbc.writeRecord(rec);	
	}

	// 07/05/13
	private void cancSvamaAss(ISASConnection dbc, Hashtable h0) throws Exception
	{
		String myselect = getSelSvamaTab(dbc, h0, "ass");
		
		ISASRecord dbr_1 = (ISASRecord)dbc.readRecord(myselect);
		if (dbr_1 != null)
			dbc.deleteRecord(dbr_1);
	}
	
	// 07/05/13
	// 19/09/12
	private void duplicaMaxRecordSvamaAss(ISASConnection mydbc, String cart, String dtMax,
								String dtDup, String codOper) throws Exception 
	{
		Hashtable h_0 = new Hashtable();
		h_0.put("n_cartella", cart);
		h_0.put("data_variazione", dtMax);
		String myselect = getSelSvamaTab(mydbc, h_0, "ass");	
		
		ISASRecord dbr_1 = (ISASRecord)mydbc.readRecord(myselect);
		
		if ((dbr_1 != null) && (dbr_1.get("data_variazione") != null)) {
			// cntrl che la dtDuplicaz non esista gi� sul DB
			if ((!dtDup.trim().equals(""))
			&& (!dtDup.trim().equals(dtMax))) {
				ISASRecord dbr_2 = mydbc.newRecord("svama_ass");
				Hashtable h_1 = (Hashtable) dbr_1.getHashtable();
				// duplico vecchi valori nel nuovo record
				Enumeration enuK = h_1.keys();
				while (enuK.hasMoreElements()) {
					String eK = (String) enuK.nextElement();
					dbr_2.put(eK, h_1.get(eK));
				}
				// inserisco nuovi valori, sbiancando quelli che devono impostati dall'utente
				dbr_2.put("data_variazione", dtDup);
				
				mydbc.writeRecord(dbr_2);
			}
		}	
	}
			
	
	
	
		private int convNumDBToInt(String nomeCampo, ISASRecord mydbr)throws Exception
         {
              int numero = 0;
              Object numDB = (Object)mydbr.get(nomeCampo);
              if (numDB != null) {
               if (numDB.getClass().getName().endsWith("Double"))
                numero = ((Double)mydbr.get(nomeCampo)).intValue();
               else if (numDB.getClass().getName().endsWith("Integer"))
                numero = ((Integer)mydbr.get(nomeCampo)).intValue();
              }
              return numero;
         }// END convNumDBToInt


	

}

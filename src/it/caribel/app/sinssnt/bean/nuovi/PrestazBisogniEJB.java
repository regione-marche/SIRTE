package it.caribel.app.sinssnt.bean.nuovi;
// ==========================================================================

// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 25/03/2015 - EJB di connessione alla procedura SINS Tabella Prestaz_bisogni
//
// mariarita minerba
//
// ==========================================================================

//import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;


import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.profile2.*;
import it.caribel.app.common.connection.GenericConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

public class PrestazBisogniEJB extends GenericConnectionEJB  {

public PrestazBisogniEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * "+
				"from prestaz_bisogni where "+
			        "id="+h.get("id");
		ISASRecord dbr=dbc.readRecord(myselect);
                System.out.println("QueryKey_medici ole"+myselect);
                String w_codice="";
               
                if (dbr != null) {
                Hashtable h1 = dbr.getHashtable();
                  if (h1.get("prest_cod")!=null && !((String)h1.get("prest_cod")).equals("")){
                      w_codice = (String)h1.get("prest_cod");
                      dbr.put("prest_des", decodifica("prestaz","prest_cod",w_codice,"prest_des",dbc));
                  }else{
                      dbr.put("prest_des", "");
                  }
                  
                }
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


public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
        String scr1=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from prestaz_bisogni";

        //controllo valore corretto mecogn

		scr = recuperaFiguraProfessionale(dbc, h);
		scr1 = (String)(h.get("tipo_bisogno"));
      
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
	       scr=duplicateChar(scr,"'");
               myselect=myselect+" and p.prest_tipo like '"+scr+"%'";
              }
	    if (scr1.equals("P"))
	    	 myselect=myselect+" AND b.bisognO NOT LIKE 'CP_%'";
	    else if (scr1.equals("C"))
	    	 myselect=myselect+" AND b.bisognO LIKE 'CP_%'";
	    myselect=myselect+" ORDER BY b.id";
        System.out.println("query GridMedici: "+myselect);
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


public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
        String scr1=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from prestaz p, prestaz_bisogni b" +
				" where b.prest_cod=trim(p.prest_cod)";

        //controllo valore corretto figura professionale
		scr = recuperaFiguraProfessionale(dbc, h);
		//scr1 = (String)(h.get("tipo_bisogno"));
      
	    if (!(scr==null))
            if (!(scr.equals(" ")))
              {
	       scr=duplicateChar(scr,"'");
               myselect=myselect+" and p.prest_tipo like '"+scr+"%'";
              }
	    //if (scr1!=null && !scr.equals("")){
	    /*if (scr1.equals("P"))	    		    
	    	 myselect=myselect+" AND b.bisognO NOT LIKE 'CP_%'";
	    else if (scr1.equals("C"))
	    	 myselect=myselect+" AND b.bisognO LIKE 'CP_%'";
	    }*/
	    myselect=myselect+" ORDER BY b.id";
	  
        System.out.println("query GridPrestazBisogni: "+myselect);
               
		ISASCursor dbcur=dbc.startCursor(myselect);

		 int start = Integer.parseInt((String)h.get("start"));
         int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
                if ((vdbr != null) && (vdbr.size() > 0))
                for(int i=0; i<vdbr.size()-1; i++){
                  ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                  
                 if(dbr.get("frequenza")!=null || !((String)dbr.get("frequenza")).equals("")){
                    String selFreq=" select * from tab_voci where tab_cod = 'FREQAC' AND TAB_VAL = '"+(String)dbr.get("frequenza")+"'";
                    		
                    ISASRecord dbFreq=dbc.readRecord(selFreq);
                    if(dbFreq!=null)
                      dbr.put("des_frequenza",(String)dbFreq.get("tab_descrizione"));
                    else
                      dbr.put("des_frequenza","");
                  }else dbr.put("des_frequenza","");
                 
                  if(dbr.get("bisogno")!=null || !((String)dbr.get("bisogno")).equals("")){
                   
                	  if(dbr.get("bisogno").equals("COMP_DIST_COGN_GRAVE"))
                          dbr.put("bisogno","Disturbo cognitivo grave");
                        else if(dbr.get("bisogno").equals("COMP_DIST_COGN_MODERATO"))
                          dbr.put("bisogno","Disturbo cognitivo moderato");
                        else if(dbr.get("bisogno").equals("COMP_DIST_COMP"))
                        	 dbr.put("bisogno","Disturbo comportamentale");
                        else if(dbr.get("bisogno").equals("COMP_PSICO_SALUTE"))
                       	 dbr.put("bisogno","Condizioni di salute psichiatrica");
                        else if(dbr.get("bisogno").equals("CUTE_ALTRO"))
                          	 dbr.put("bisogno","Altri problemi cutanei");
                        else if(dbr.get("bisogno").equals("CUTE_CURA"))
                         	 dbr.put("bisogno","Cura della ferita");
                        else if(dbr.get("bisogno").equals("CUTE_LACERAZIONI"))
                        	 dbr.put("bisogno","Lacerazioni o tagli non chirugici");
                        else if(dbr.get("bisogno").equals("CUTE_PRESSIONE"))
                       	 dbr.put("bisogno","Stato più grave di ulcera da pressione");
                        else if(dbr.get("bisogno").equals("CUTE_ULCERE12"))
                          	 dbr.put("bisogno","Ulcere cutanee 1°-2° grado");
                        else if(dbr.get("bisogno").equals("CUTE_ULCERE34"))
                         	 dbr.put("bisogno","Ulcere cutanee 3°-4° grado");
                        else if(dbr.get("bisogno").equals("GASTR_INCONT"))
                        	 dbr.put("bisogno","Incontinenza fecale");
                        else if(dbr.get("bisogno").equals("GASTR_SANG"))
                       	 dbr.put("bisogno","Sanguinamento gastrointestinale");
                        else if(dbr.get("bisogno").equals("GASTR_STIPSI"))
                          	 dbr.put("bisogno","Stipsi");
                        else if(dbr.get("bisogno").equals("GASTR_STOMIA"))
                         	 dbr.put("bisogno","Stomia");
                        else if(dbr.get("bisogno").equals("GASTR_VOMITO"))
                        	 dbr.put("bisogno","Vomito");
                        else if(dbr.get("bisogno").equals("GENURI_CATETERISMO"))
                       	 dbr.put("bisogno","Cateterismo vescicale");
                        else if(dbr.get("bisogno").equals("GENURI_DIALISI"))
                          	 dbr.put("bisogno","Dialisi");
                        else if(dbr.get("bisogno").equals("GENURI_EMATURIA"))
                         	 dbr.put("bisogno","Ematuria");
                        else if(dbr.get("bisogno").equals("GENURI_INCONT"))
                        	 dbr.put("bisogno","Incontinenza/ritenzione urinaria");
                        else if(dbr.get("bisogno").equals("GENURI_UROSTOMIA"))
                       	 dbr.put("bisogno","Urostomia");
                        else if(dbr.get("bisogno").equals("NUTR_DIMAGRIMENTO"))
                          	 dbr.put("bisogno","Dimagrimento");
                        else if(dbr.get("bisogno").equals("NUTR_DISFAGIA"))
                         	 dbr.put("bisogno","Nutrizione/Disfagia");
                        else if(dbr.get("bisogno").equals("NUTR_DISIDRATAZIONE"))
                        	 dbr.put("bisogno","Disidratazione");
                        else if(dbr.get("bisogno").equals("ONCO_CHEMIOTERAPIA"))
                       	 dbr.put("bisogno","Chemioterapia");
                        else if(dbr.get("bisogno").equals("ONCO_DOLORE"))
                          	 dbr.put("bisogno","Dolore presente");
                        else if(dbr.get("bisogno").equals("ONCO_ONCOLOGICO"))
                         	 dbr.put("bisogno","Oncologico");
                        else if(dbr.get("bisogno").equals("ONCO_RADIOTERAPIA"))
                        	 dbr.put("bisogno","Radioterapia");
                        else if(dbr.get("bisogno").equals("ONCO_TERM_NON_ONCO"))
                       	 dbr.put("bisogno","Terminalità non oncologica");
                        else if(dbr.get("bisogno").equals("ONCO_TERM_ONCO"))
                          	 dbr.put("bisogno","Terminalità oncologica");
                        else if(dbr.get("bisogno").equals("PREST_ECG"))
                         	 dbr.put("bisogno","ECG");
                        else if(dbr.get("bisogno").equals("PREST_GESTIONE_CVC"))
                        	 dbr.put("bisogno","Gestione CVC");
                        else if(dbr.get("bisogno").equals("PREST_PRELIEVO"))
                       	 dbr.put("bisogno","Prelievo venoso non occasionale");
                        else if(dbr.get("bisogno").equals("PREST_TELEMETRIA"))
                          	 dbr.put("bisogno","Telemetria");
                        else if(dbr.get("bisogno").equals("PREST_TERAPIA_EV"))
                         	 dbr.put("bisogno","Terapia EV");
                        else if(dbr.get("bisogno").equals("PREST_TERAPIA_SOTCUT"))
                        	 dbr.put("bisogno","Terapia sottocutanea/intram.");
                        else if(dbr.get("bisogno").equals("PREST_TRASFUSIONI"))
                       	 dbr.put("bisogno","Trasfusioni");
                        else if(dbr.get("bisogno").equals("RESP_OSSIGENOTERAPIA"))
                          	 dbr.put("bisogno","Ossigenoterapia");
                        else if(dbr.get("bisogno").equals("RESP_PORTATORE_TRACHEO"))
                         	 dbr.put("bisogno","Tracheostomia");
                        else if(dbr.get("bisogno").equals("RESP_TOSSE_SECR"))
                        	 dbr.put("bisogno","Tosse e secrezioni");
                        else if(dbr.get("bisogno").equals("RESP_VENTILOTERAPIA"))
                       	 dbr.put("bisogno","Ventiloterapia");
                        else if(dbr.get("bisogno").equals("RIAB_AFASIA"))
                          	 dbr.put("bisogno","Afasia");
                        else if(dbr.get("bisogno").equals("RIAB_MANTENIMENTO"))
                         	 dbr.put("bisogno","Riabilitazione di Mantenimento");
                        else if(dbr.get("bisogno").equals("RIAB_NEUROLOGICA"))
                        	 dbr.put("bisogno","Riabilitazione Neurologica");
                        else if(dbr.get("bisogno").equals("RIAB_ORTOPEDICA"))
                       	 dbr.put("bisogno","Riabilitazione Ortopedica");
                        else if(dbr.get("bisogno").equals("RISCHIO_FEBBRE"))
                          	 dbr.put("bisogno","Febbre");
                        else if(dbr.get("bisogno").equals("RISCHIO_PRESENTE"))
                         	 dbr.put("bisogno","Rischio infettivo presente");
                        else if(dbr.get("bisogno").equals("RITMO_ALTERATO"))
                        	 dbr.put("bisogno","Ritmo sonno/veglia alterato");
                        else if(dbr.get("bisogno").equals("AUTONOMIA1"))
                          	 dbr.put("bisogno","Autonomo");
                        else if(dbr.get("bisogno").equals("AUTONOMIA2"))
                       	 dbr.put("bisogno","Parzialmente autonomo");
                        else if(dbr.get("bisogno").equals("AUTONOMIA3"))
                          	 dbr.put("bisogno","Totalmente dipendente");
                        else if(dbr.get("bisogno").equals("CP_ADP"))
                         	 dbr.put("bisogno","ADP");
                        else if(dbr.get("bisogno").equals("CP_AID"))
                        	 dbr.put("bisogno","AID");
                        else if(dbr.get("bisogno").equals("CP_ARD"))
                       	 dbr.put("bisogno","ARD");
                        else if(dbr.get("bisogno").equals("CP_VSD"))
                          	 dbr.put("bisogno","VSD");
                      }else dbr.put("bisogno", "");

                }
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
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


public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
	
	try{
		dbc=super.logIn(mylogin);
		
		dbc.startTransaction(); // 25/10/11
		
		ISASRecord dbr=dbc.newRecord("prestaz_bisogni");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		String newProgr = calcolaProgr(dbc).toString();//calcolo il progressivo
		dbr.put("id",newProgr);
		dbc.writeRecord(dbr);
		
		
		
		String myselect = "Select *" +	
				" from prestaz_bisogni" +
				" where id = " + newProgr;
				
        	System.out.println("CommissUVMEJB/insert/myselect: "+myselect);
		dbr=dbc.readRecord(myselect);
		
		
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


public ISASRecord update(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
	try {
		codice=(String)dbr.get("id");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from prestaz_bisogni where "+
			"id="+codice;
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
	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		return ((String)dbtxt.get("descrizione"));
	} catch (Exception ex) {
		return " ";
	}
}

	
	
	private String recuperaFiguraProfessionale(ISASConnection dbc, Hashtable h) {
		String tipoOper=(String)h.get("figura_profesionale");
		String query = "Select  conf_txt from conf where "+
                        "conf_kproc ='SINS' and "+
                        "conf_key ='TIPDEF"+tipoOper+"'";
		ISASRecord dbrConf = getRecord(dbc, query);
		String figura_prof = ISASUtil.getValoreStringa(dbrConf, "conf_txt");

		return figura_prof;
	
	}
	private Integer calcolaProgr(ISASConnection dbc)throws Exception{
		String nomeMetodo = "calcolaProgr";
		try{
			int mass = 1;
			String selmax = "SELECT MAX (id) massimo" +
					" FROM prestaz_bisogni ";
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
	private ISASRecord getRecord(ISASConnection dbc, String query) {		
		ISASRecord dbrRecord = null;		
		try {
			dbrRecord = dbc.readRecord(query);
		} catch (Exception e) {			
			e.printStackTrace();
		}

		return dbrRecord;
	}

}

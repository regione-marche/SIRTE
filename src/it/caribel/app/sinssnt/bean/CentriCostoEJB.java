package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 29/01/2008 - EJB di connessione alla procedura SINS Tabella Centri di costo
//
//
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class CentriCostoEJB extends SINSSNTConnectionEJB  {

public CentriCostoEJB() {}
private static final String MIONOME = "1-CentriCostoEJB.";
public ISASRecord queryKey(myLogin mylogin,Hashtable h)
throws  SQLException, ISASPermissionDeniedException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		
		
		
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM co_comuni WHERE "+
			        "codice='"+(String)h.get("codice")+"'";
		myselect = myselect + getCentriCosto(dbc, " codice " , " and ");
		System.out.println("QueryKey su Centri di costo: "+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(ISASPermissionDeniedException e){
            e.printStackTrace();
            throw e;
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

private String getCentriCosto(ISASConnection dbc, String campo_codice_ccosto, String clause) {
	String user = dbc.getKuser();
	String sql = "select * from co_ope_ccosto where operatore = '"+user+ "' and tipo = 'C'";
	String ret = "";
	try{
		ISASCursor dbcur = dbc.startCursor(sql);
		if (dbcur != null && dbcur.getDimension()>0)
		{
			int dim = dbcur.getDimension();
			int currRecord = 1;
			ret = clause + campo_codice_ccosto + " in (";
		while (dbcur.next()){
			ISASRecord dbr = dbcur.getRecord();
			ret += "'"+dbr.get("co_ccosto").toString()+"'";
			if (currRecord < dim) ret+= ",";
			else ret+=")";
			currRecord++;
		}
		}
		if (dbcur!=null)dbcur.close();
	}catch (Exception e){
		e.printStackTrace();
	}
	return ret;
}

public Vector query_loadCmbBoxCentriCosto(myLogin mylogin, Hashtable h) throws SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	Vector vdbr = new Vector();
	try {
		dbc = super.logIn(mylogin);

		String myselect = "SELECT *" + " FROM co_comuni";
		myselect += getCentriCosto(dbc, " codice ", " where ");
		System.out.println("ContribEJB/query_loadCmbBoxCentriCosto(3) : " + myselect);
		ISASCursor dbcur = dbc.startCursor(myselect);
		System.out.println("ContribEJB/query_loadCmbBoxCentriCosto: Fatta la startcursor");
		vdbr = dbcur.getAllRecord();
		System.out.println("ContribEJB/query_loadCmbBoxCentriCosto: Creato il vettore");
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;
		return vdbr;
	} catch (Exception e) {
		e.printStackTrace();
		throw new SQLException("!!Errore eseguendo la query_loadCmbBoxCentriCosto()  ");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch (Exception e1) {
				System.out.println(e1);
			}
		}
	}
}

public Vector query_loadCmbBoxCentriCostoV(myLogin mylogin, Hashtable h) throws SQLException {
	String punto = MIONOME + "query_loadCmbBoxCentriCostoV ";
	boolean done = false;
	ISASConnection dbc = null;
	Vector vdbr = new Vector();
	try {
		dbc = super.logIn(mylogin);

		String myselect = "SELECT *" + " FROM co_comuni";
		myselect+=getCentriCosto(dbc, " codice ", " where ");
		System.out.println(punto + "Query>" + myselect);
		ISASCursor dbcur = dbc.startCursor(myselect);
		System.out.println(punto + "dati letti>" + dbcur.getDimension() + "<\n");

		ISASRecord dbr = dbc.newRecord("ass_contrib");
		dbr.put("codice", "0");
		dbr.put("descrizione", ".");
		vdbr.add(dbr);
		Vector risultato = dbcur.getAllRecord();
		for (int i = 0; i < risultato.size(); i++) {
			  vdbr.add(risultato.get(i));
		}
		
		System.out.println(punto + " restituisco>" + vdbr.size() + "<");
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;
		return vdbr;
	} catch (Exception e) {
		e.printStackTrace();
		throw new SQLException("!!Errore eseguendo la query_loadCmbBoxCentriCosto()  ");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch (Exception e1) {
				System.out.println(e1);
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

public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
            dbc=super.logIn(mylogin);
            String myselect="SELECT * FROM co_comuni";
            
             //controllo valore corretto descrizione
             scr=(String)(h.get("descrizione"));
             if (!(scr==null) && !(scr.equals(" ")))
             {
            	 scr=duplicateChar(scr,"'");
                 myselect=myselect+" where descrizione like '"+scr+"%'";
                 myselect+=getCentriCosto(dbc, " codice ", " and ");
             }
             else 
            	 myselect+=getCentriCosto(dbc, " codice ", " where ");
                
            myselect=myselect+" ORDER BY descrizione ";
            System.out.println("query GridCentriCosto: "+myselect);


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

public Vector queryComboxDistretti(myLogin mylogin,Hashtable h) throws  SQLException {
        System.out.println("queryComboxDistretti ");

	ServerUtility su =new ServerUtility();
	boolean done=false;
        String scr="";
        String cod_distr="";
	ISASConnection dbc=null;
	Vector vdbr=new Vector();;
	try{
		String ccosto = "";
		dbc=super.logIn(mylogin);
                if(h.get("cod_distr")!=null && !((String)h.get("cod_distr")).equals("")
	            && !((String)h.get("cod_distr")).equals("TUTTO")){
    	            cod_distr=(String)(h.get("cod_distr"));
		    String myselect=" SELECT * FROM co_comuni ";

                    scr = su.addWhere(scr, su.REL_AND, "cod_distretto", su.OP_EQ_STR,cod_distr);
                    if(!scr.equals(""))
                    {
                     myselect = myselect + " WHERE ";
                     ccosto=getCentriCosto(dbc, " codice ", " and ");
                    }
                    else 
                    	ccosto=getCentriCosto(dbc, " codice ", " where ");
                    myselect=myselect + scr + ccosto + " ORDER BY descrizione ";

                    System.out.println("queryComboxDistretti : "+myselect);
                    ISASCursor dbcur=dbc.startCursor(myselect);
                    vdbr=dbcur.getAllRecord();
    		    dbcur.close();
                }
			if (ccosto.equals("")) {
				ISASRecord dbr = dbc.newRecord("co_comuni");

				dbr.put("codice", "TUTTO");
				dbr.put("descrizione", "TUTTI");

				vdbr.insertElementAt((Object)dbr,0);
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
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM co_comuni";

                 //controllo valore corretto descrizione

                scr=(String)(h.get("descrizione"));
                if (!(scr==null)&&!(scr.equals(" "))){
        	       scr=duplicateChar(scr,"'");
                       myselect=myselect+" WHERE descrizione like '"+scr+"%'";
                       myselect+=getCentriCosto(dbc, " codice ", " and ");
                    }
                else myselect+=getCentriCosto(dbc, " codice ", " where ");
                myselect=myselect+" ORDER BY descrizione ";
                System.out.println("query GridCentriCosto: "+myselect);

                //Jessy 19/10/2004
		ISASCursor dbcur=dbc.startCursor(myselect,200);
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

public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
	try {
		codice=(String)h.get("codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("co_comuni");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="SELECT * FROM co_comuni WHERE "+
        			"codice='"+codice+"'";
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
	String codice=null;
	ISASConnection dbc=null;
	try {
		codice=(String)dbr.get("codice");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="SELECT * FROM co_comuni WHERE "+
        			"codice='"+codice+"'";
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

//ILARIA 31/01/2005 INIZIO
public Vector queryComboxDistretti_NesDiv(myLogin mylogin,Hashtable h)
throws  SQLException {

        ServerUtility su =new ServerUtility();
        boolean done=false;
        String scr="";
        String ccosto = "";
        String cod_distr="";
        ISASConnection dbc=null;
        Vector vdbr=new Vector();;
        try{

            dbc=super.logIn(mylogin);
            if(h.get("cod_distretto")!=null && !((String)h.get("cod_distretto")).equals("")
                && !((String)h.get("cod_distretto")).equals("TUTTO")
                && !((String)h.get("cod_distretto")).equals("NESDIV")){
                    cod_distr=(String)(h.get("cod_distretto"));
                    String myselect=" SELECT * FROM co_comuni ";
                    scr = su.addWhere(scr, su.REL_AND, "cod_distretto",su.OP_EQ_STR,cod_distr);
                    if(!scr.equals(""))
                    {
                              myselect = myselect + " WHERE ";
                              ccosto=getCentriCosto(dbc, " codice ", " and ");
                    }
                    else ccosto=getCentriCosto(dbc, " codice ", " where ");
                    myselect=myselect + scr + ccosto +" ORDER BY descrizione ";
                    ISASCursor dbcur=dbc.startCursor(myselect);
                    vdbr=dbcur.getAllRecord();
                    dbcur.close();
            }
            if (ccosto.equals(""))
    		{
            ISASRecord dbr = dbc.newRecord("co_comuni");
            dbr.put("codice", "NESDIV");
            dbr.put("descrizione", "NESSUNA DIVISIONE");
            vdbr.insertElementAt((Object)dbr,0);
            dbr = dbc.newRecord("co_comuni");        	
    		dbr.put("codice", "TUTTO");
    		dbr.put("descrizione", "TUTTI");
    		 vdbr.insertElementAt((Object)dbr,0);
    		}
            dbc.close();
            super.close(dbc);
            done=true;
            return vdbr;
        }catch(Exception e){
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una queryComboxDistretti_NesDiv  ");
        }finally{
                if(!done){
                        try{
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e1){System.out.println(e1);}
                }
        }
}
public ISASRecord queryProgressivo(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);
			String key = "CONTMANDELE_"+(String)h.get("me_anno");
                        String mysel = "SELECT val_libero FROM chiavi_libere"+
                                       " WHERE nome_chiave='"+ key+"'";
                        ISASRecord dbr = dbc.readRecord(mysel);
                        System.out.println("queryProgressivo: select=[" + mysel + "]");

			if (dbr != null) {
    			    dbr.put("progressivo", ""+dbr.get("val_libero"));
			}else{
                            System.out.println(">>>ContMandEleEM - scrivo nuovo record su chiavi_libere ");
                            ISASRecord dbrNew = dbc.newRecord("chiavi_libere");
                            dbrNew.put("nome_chiave", "CONTMANDELE_"+(String)h.get("me_anno")) ;
                            dbrNew.put("val_libero", "1");
                            dbc.writeRecord(dbrNew);
                            System.out.println(">>>ContMandEleEM - cosa scrivo su chiavi_libere:"+dbrNew.getHashtable().toString());
                            dbr=dbc.newRecord("chiavi_libere");
                            dbr.put("progressivo", "1");
                        }
                        System.out.println(">>>sssssss");
                        dbr.put("me_anno", (String)h.get("me_anno"));
                        System.out.println(">>>rrrrrrrr");
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		} catch (Exception e) {
			System.out.println("CentriCostoEJB: queryProgressivo - Eccezione= " + e);
			throw new SQLException("Errore eseguendo una queryProgressivo()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("CentriCostoEJB: queryProgressivo - Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}
	}// END queryKey
//public String getCentroCosto(myLogin mylogin,Hashtable h)
//throws  SQLException, ISASPermissionDeniedException {
//	boolean done=false;
//	ISASConnection dbc=null;
//	String ret = "";
//	try{
//		dbc=super.logIn(mylogin);
//		String myselect="SELECT co_ccosto FROM co_ope_ccosto WHERE"+
//			        " operatore = '"+(String)h.get("codice_operatore")+"'" +
//			        " and tipo = 'C'";
//		
//		System.out.println("getCentroCosto su Centri di costo: "+myselect);
//                ISASRecord dbr=dbc.readRecord(myselect);
//        if(dbr!=null && dbr.get("co_ccosto")!=null) ret = dbr.get("co_ccosto").toString(); 
//		
//        dbc.close();
//		super.close(dbc);
//		done=true;
//		return ret;
//	}catch(ISASPermissionDeniedException e){
//            e.printStackTrace();
//            throw e;
//        }catch(Exception e){
//		e.printStackTrace();
//		throw new SQLException("Errore eseguendo una getCentroCosto()  ");
//	}finally{
//		if(!done){
//			try{
//				dbc.close();
//				super.close(dbc);
//			}catch(Exception e1){System.out.println(e1);}
//		}
//	}
//}

public Hashtable getPunteggi(myLogin mylogin, Hashtable in)
throws DBRecordChangedException, ISASPermissionDeniedException,
SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	try {
		dbc = super.logIn(mylogin);
		Hashtable ret = new Hashtable();
		System.out.println("h_in: "+in.toString());
		String dt_ref = (String) in.get("data");
		String n_cartella = (String) in.get("n_cartella");

		String sql = "select punteggio from sc_valsoc_adeamb where n_cartella = "
			+ n_cartella
			+ ((dt_ref != null)?" and data <= "+ dbc.formatDbDate(dt_ref):"");
		System.out.println("sel: "+sql);
		ISASRecord dbr = dbc.readRecord(sql);
		if (dbr != null && dbr.get("punteggio")!=null)
			ret.put("iaca",dbr.get("punteggio").toString());
		dbr = null;

		sql = "select max(pr_data_verbale_uvm) pr_data_puac, liv_isogravita from puauvm where n_cartella = "
			+ n_cartella
			+ ((dt_ref != null)?" and pr_data_verbale_uvm is not null and pr_data_verbale_uvm <= "+ dbc.formatDbDate(dt_ref):"");
		System.out.println("sel: "+sql);
		dbr = dbc.readRecord(sql);
		if (dbr != null && dbr.get("pr_data_puac")!=null)
			ret.put("pr_data_puac",dbr.get("pr_data_puac").toString());
		if (dbr != null && dbr.get("liv_isogravita")!=null)
			ret.put("isogravita",dbr.get("liv_isogravita").toString());




		dbc.close();
		super.close(dbc);
		done = true;
		return ret;
	} catch (Exception e1) {
		System.out.println(e1);
		throw new SQLException("CentriCostoEJB:Errore eseguendo una getPunteggi() - " + e1);
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch (Exception e2) {
				System.out.println(e2);
			}
		}
	}
}


public Integer getNextProgValut(myLogin mylogin, Hashtable h) throws Exception {
	ISASConnection dbc = null;
	boolean done = false;
	try {
		Integer ret = null;
		dbc = super.logIn(mylogin);
		ISASRecord rec = null;
		String sql = "select nvl(max(prog_valut),0) next_progr from co_valutaz where n_cartella = "+h.get("n_cartella").toString();
        rec = dbc.readRecord(sql);
		dbc.close();
		super.close(dbc);
		done = true;
		if (rec!=null) ret = (Integer)rec.get("next_progr");
		return ret;
	} catch (Exception e) {
		System.out.println("CentriCostoEJB: ERRORE getNextProgValut() - " + e);			
		throw e;
	} finally {
		if (!done) {
			if (dbc != null) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println("CentriCostoEJB: Errore eseguendo una getNextProgValut()  " + e1);
				}
			}
		}
	}
}


//public Hashtable getCCostoDistrZona(myLogin mylogin,Hashtable h)
//throws  SQLException, ISASPermissionDeniedException {
//	boolean done=false;
//	ISASConnection dbc=null;
//	Hashtable ret = new Hashtable();
//	ret.put("c_costo","");
//	try{
//		dbc=super.logIn(mylogin);
//		String sql="SELECT co_ccosto FROM co_ope_ccosto WHERE"+
//			        " operatore = '"+(String)h.get("codice_operatore")+"'" +
//			        " and tipo = 'C'";
//		
//		System.out.println("getCCostoDistrZona su Centri di costo: "+sql);
//                ISASRecord dbr=dbc.readRecord(sql);
//        if(dbr!=null && dbr.get("co_ccosto")!=null){
//        	ret = new Hashtable();
//        	String c_costo = (String)dbr.get("co_ccosto");
//        	sql = "select cod_zona, cod_distr from distretti where cod_distr = (select cod_distretto from co_comuni where codice = '"+c_costo+"')";
//        	System.out.println("getCCostoDistrZona su distretti: "+sql);
//        	ISASRecord dbr1=dbc.readRecord(sql);
//        	if(dbr1!=null && dbr1.get("cod_zona")!=null && dbr1.get("cod_distr")!=null)
//        		{
//        			ret.put("zona", (String)dbr1.get("cod_zona"));
//        			ret.put("distretto", (String)dbr1.get("cod_distr"));
//        			
//        		}
//        	ret.put("c_costo",c_costo); 
//        }
//        
//        dbc.close();
//		super.close(dbc);
//		done=true;
//		return ret;
//	}catch(ISASPermissionDeniedException e){
//            e.printStackTrace();
//            throw e;
//        }catch(Exception e){
//		e.printStackTrace();
//		throw new SQLException("Errore eseguendo una getCentroCosto()  ");
//	}finally{
//		if(!done){
//			try{
//				dbc.close();
//				super.close(dbc);
//			}catch(Exception e1){System.out.println(e1);}
//		}
//	}
//}

}

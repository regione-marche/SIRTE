package it.caribel.app.sinssnt.bean.nuovi;

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class SchedaFIMEJB extends SINSSNTConnectionEJB  {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
public SchedaFIMEJB() {}

public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
	String punto = "update ";
	LOG.debug(punto + ">>dati che ricervo>>"+dbr+"<<<");
	Hashtable<String, Object> dati = convertiDati(dbr);
	return update_scheda(mylogin, dati);
}  
private Hashtable<String, Object> convertiDati(ISASRecord dbr) {
	Hashtable<String, Object> datiConvertire = new Hashtable<String, Object>();
	datiConvertire.putAll(dbr.getHashtable());
	datiConvertire.put("n_cartella", ISASUtil.getValoreStringa(dbr, "n_cartella"));
	datiConvertire.put("fim_progr", ISASUtil.getValoreStringa(dbr, "fim_progr"));
	datiConvertire.put("fim_spr_data", ISASUtil.getValoreStringa(dbr, "fim_spr_data"));	
	return datiConvertire;
}
public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        try{
            dbc=super.logIn(mylogin);
	    System.out.println("Hash:"+h.toString());
            String myselect="SELECT r.* FROM ri_scala_fim r WHERE "+
		            "r.n_cartella="+(String)h.get("n_cartella")+
                            " AND r.fim_spr_data="+formatDate(dbc,(String)h.get("fim_spr_data"));
                            ;
            if(h.get("fim_progr")!=null)
                myselect+=" AND r.fim_progr="+h.get("fim_progr");
            else//bargi 13/11/2009 scommentato
                myselect+=" AND r.fim_progr IN (SELECT MAX(f.fim_progr) FROM ri_scala_fim f WHERE "+
                          "r.n_cartella=f.n_cartella AND "+
                          "r.fim_spr_data="+formatDate(dbc,(String)h.get("fim_spr_data"))+")";
            
            System.out.println("QueryKey su Schedafim: "+myselect);
            ISASRecord dbr = dbc.readRecord(myselect);
            if(dbr!=null){
                String op_1 = (String)util.getObjectField(dbr,"fim_operatore",'S');
		dbr.put("desc_op",util.getDecode(dbc,"operatori","codice",
		                  op_1,"nvl(trim(cognome),'')|| ' ' ||nvl(trim(nome),'')","nomeoper"));
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
        ISASConnection dbc=null;
        try{
                dbc=super.logIn(mylogin);
                System.out.println("In query:"+h);
                String myselect="SELECT * FROM ri_scala_fim WHERE "+
                                "n_cartella="+h.get("n_cartella")+
                                " AND fim_spr_data="+formatDate(dbc,""+h.get("fim_spr_data"))+
                                " ORDER BY fim_data DESC";
                System.out.println("select:"+myselect);
                ISASCursor dbcur=dbc.startCursor(myselect);
                Vector vdbr=dbcur.getAllRecord();
                for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); ){
    				ISASRecord dbr=(ISASRecord)senum.nextElement();
    				String op = (String)util.getObjectField(dbr,"fim_operatore",'S');
    				dbr.put("des_operatore",util.getDecode(dbc,"operatori","codice",
    				         op,"nvl(trim(cognome),'')|| ' ' ||nvl(trim(nome),'')","des_operatore"));
                }
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
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
	Vector v=null;
	boolean done=false;
	ISASConnection dbc=null;
	try{
            dbc=super.logIn(mylogin);
            dbc.startTransaction();
            String cartella = "";
            String data_spr = "";
            int numero_fim = 0;
            System.out.println("SchedaFim:"+h.toString());
            it.pisa.caribel.util.ServerUtility su = new it.pisa.caribel.util.ServerUtility();
            try{
              cartella = (String)h.get("n_cartella");
              data_spr = (String)h.get("fim_spr_data");
            }catch(Exception e0){
              System.out.println("SchedaFimEJB.insert(): manca la chiave primaria! "+e0);
            }
            String anno = (su.getTodayDate("dd/MM/yyyy")).substring(6,10);
            ISASRecord dba = dbc.newRecord("ri_scala_fim");

            dba.put("n_cartella",new Integer(cartella));
            String sel="SELECT MAX(fim_progr) progr FROM ri_scala_fim WHERE "+
                       "n_cartella="+cartella+
                       " AND fim_spr_data="+formatDate(dbc, data_spr);
            //System.out.println("SEL:"+sel);
            ISASRecord dbrPr = dbc.readRecord(sel);
            if(dbrPr!=null && dbrPr.get("progr")!=null){
                System.out.println("E' diverso da null");
                numero_fim= ((Integer)dbrPr.get("progr")).intValue();
                numero_fim++;
            }else
                numero_fim++;
            System.out.println("Numero:"+numero_fim);
            dba.put("fim_progr",new Integer(numero_fim));
            Enumeration n=h.keys();
            while(n.hasMoreElements()){
              String elem=(String)n.nextElement();
              dba.put(elem,h.get(elem));
            }
            dbc.writeRecord(dba);

            sel = "SELECT * FROM ri_scala_fim WHERE n_cartella="+cartella+
                  " AND fim_spr_data="+formatDate(dbc, data_spr)+
                  " AND fim_progr="+numero_fim;
            ISASRecord dbret = dbc.readRecord(sel);
            dbc.commitTransaction();
            dbc.close();
            super.close(dbc);
            done = true;
            return dbret;
	} catch(DBRecordChangedException e) {
		System.out.println("SchedaFimEJB.insert(): "+e);
		e.printStackTrace();
		try{dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1 );
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("SchedaFimEJB.insert(): "+e);
		e.printStackTrace();
		try{dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(Exception e){
		System.out.println("SchedaFimEJB.insert(): "+e);
		e.printStackTrace();
		try{dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
 		}
		throw new SQLException(e.getMessage());
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

public ISASRecord update_scheda(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String cartella=null;
        String data_spr=null;
	String progr=null;
	ISASConnection dbc=null;
	try {
		cartella=(String)h.get("n_cartella");
                data_spr=(String)h.get("fim_spr_data");
		progr=(String)h.get("fim_progr");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
                String myselect="Select * from ri_scala_fim where "+
		    	        "n_cartella="+cartella+" and "+
                                "fim_spr_data="+formatDate(dbc,data_spr)+" and "+
		    	        "fim_progr="+progr;
		ISASRecord dbr=dbc.readRecord(myselect);
		Enumeration n=h.keys();
                while(n.hasMoreElements()){
                    String e=(String)n.nextElement();
                    dbr.put(e,h.get(e));
                }
                //System.out.println("ISAS_DOPO:"+dbr.getHashtable().toString());
                dbc.writeRecord(dbr);
		myselect="Select * from ri_scala_fim where "+
			 "n_cartella="+cartella+" and "+
                         "fim_spr_data="+formatDate(dbc,data_spr)+" and "+
			 "fim_progr="+progr;
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
                System.out.println("ISAS:"+dbr.getHashtable().toString());
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

}

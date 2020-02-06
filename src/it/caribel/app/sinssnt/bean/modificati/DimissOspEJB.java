package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 28/08/2006 - EJB di connessione alla procedura SINS Dimissioni Ospedaliere
//
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
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.sinssnt.connection.*;

public class DimissOspEJB extends SINSSNTConnectionEJB  {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

public DimissOspEJB() {}

//Viene utilizzata all'apertura del progetto sins per segnalare che ci sono
//nuove dimissioni inviate dagli ospedali
public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws
SQLException, ISASPermissionDeniedException, CariException {
	boolean done=false;
	ISASConnection dbc=null;
	ISASRecord dbr=null;
        String mex = "";
        try{
        	dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                String sel = "SELECT * FROM dimiss_osp WHERE data_carico is NULL";
                dbr=dbc.readRecord(sel);
                if(dbr!=null){
                  mex += "Controllare la lista delle segnalazioni di dimissione inviate dagli ospedali!";
                }
                //System.out.println("Hashtable:"+h.toString());
                if(((String)h.get("dimo_msg")).equals("true")){
                  if(!mex.equals(""))
                    throw new CariException(mex);
                }
                dbc.close();
		super.close(dbc);
		done=true;
                return dbr;
        }
        catch(CariException ce){
                ce.setISASRecord(dbr);
		throw ce;
        }catch(ISASPermissionDeniedException e){
		System.out.println("eccezione permesso negato "+e);
		return null;
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


public ISASRecord esiste_contatto(myLogin mylogin,Hashtable h) throws
SQLException, ISASPermissionDeniedException{
	boolean done=false;
	ISASConnection dbc=null;
	ISASRecord dbr=null;
        String cartella="";
        try{
        	dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                cartella = (String)h.get("n_cartella");
                String sel = "SELECT * FROM skinf WHERE n_cartella = "+cartella+
                             " AND ski_data_uscita IS NULL";
                System.out.println("SELECT"+sel);
                dbr=dbc.readRecord(sel);
                if(dbr!=null)
                  dbr.put("esiste", "S");
                else{
                  String selCont = "SELECT MAX (n_contatto) esiste FROM contsan WHERE "+
                                   "n_cartella="+cartella;
                  dbr = dbc.readRecord(selCont) ;
                  dbr.put("n_cartella", cartella);
                }

                dbc.close();
		super.close(dbc);
		done=true;
                return dbr;
        }catch(ISASPermissionDeniedException e){
		System.out.println("eccezione permesso negato "+e);
		return null;
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


public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String d1 = "";
        String d2 = "";
        String ospedale = "";
        String reparto = "";
        String sel = "";
        try{
		dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                if(h.get("data1")!=null && !h.get("data1").equals(""))
                  sel=su.addWhere(sel,su.REL_AND,"data_dimissioni",
                                  su.OP_GE_NUM,formatDate(dbc,(String)h.get("data1")));
                if(h.get("data2")!=null && !h.get("data1").equals(""))
                  sel=su.addWhere(sel,su.REL_AND,"data_dimissioni",
                                  su.OP_LE_NUM,formatDate(dbc,(String)h.get("data2")));
                if(h.get("cod_ospedale")!=null)
                  sel=su.addWhere(sel,su.REL_AND,"cod_ospedale",
                                  su.OP_EQ_STR,(String)h.get("cod_ospedale"));
                if(h.get("cod_reparto")!=null)
                  sel=su.addWhere(sel,su.REL_AND,"cod_reparto",
                                  su.OP_EQ_STR,(String)h.get("cod_reparto"));
                System.out.println("Prima di select");
		String myselect= "SELECT d.*, c.data_nasc, c.sesso, c.cognome, c.nome "+
                                 " FROM dimiss_osp d, cartella c WHERE "+
                                 " d.n_cartella=c.n_cartella AND "+
                                 " d.data_carico IS NULL";
                if(!sel.equals(""))   sel = " AND "+sel;
                myselect += sel+ " ORDER BY data_dimissioni DESC";
                System.out.println("query_consulta"+myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
		if ((vdbr != null) && (vdbr.size() > 0)){
                  for(int i=0; i<vdbr.size()-1; i++){
                    ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                    if (dbr.get("cod_diag1")!=null && !((String)dbr.get("cod_diag1")).equals("")){
                      String desc = util.getDecode(dbc,"icd9","cd_diag",
                             (String)util.getObjectField(dbr,"cod_diag1",'S'),"diagnosi");
                      if(!desc.equals("")){
                        if(desc.length()>=50)
                          desc=desc.substring(0,50);
                        dbr.put("diag1", desc);
                      }
                    }else dbr.put("diag1", "");
                    dbr.put("ospedale",util.getDecode(dbc,"ospedali","codosp",
                           (String)util.getObjectField(dbr,"cod_ospedale",'S'),"descosp"));
                    dbr.put("reparto",util.getDecode(dbc,"reparti","cd_rep",
                           (String)util.getObjectField(dbr,"cod_reparto",'S'),"reparto"));
                  }
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
			}catch(Exception e1){
				e1.printStackTrace();
				System.out.println(e1);}
		}
	}
}

public Vector query_consulta(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String d1 = "";
        String d2 = "";
        String ospedale = "";
        String reparto = "";
        String sel = "";
        try{
		dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                if(h.get("data1")!=null)
                  sel=su.addWhere(sel,su.REL_AND,"data_dimissioni",
                                  su.OP_GE_NUM,formatDate(dbc,(String)h.get("data1")));
                if(h.get("data2")!=null)
                  sel=su.addWhere(sel,su.REL_AND,"data_dimissioni",
                                  su.OP_LE_NUM,formatDate(dbc,(String)h.get("data2")));
                if(h.get("cod_ospedale")!=null)
                  sel=su.addWhere(sel,su.REL_AND,"cod_ospedale",
                                  su.OP_EQ_STR,(String)h.get("cod_ospedale"));
                if(h.get("cod_reparto")!=null)
                  sel=su.addWhere(sel,su.REL_AND,"cod_reparto",
                                  su.OP_EQ_STR,(String)h.get("cod_reparto"));
                System.out.println("Prima di select");
		String myselect= "SELECT d.*, c.data_nasc, c.sesso, c.cognome, c.nome "+
                                 " FROM dimiss_osp d, cartella c WHERE "+
                                 " d.n_cartella=c.n_cartella";
                if(!sel.equals(""))   sel = " AND "+sel;
                myselect += sel;
                System.out.println("query_consulta"+myselect);

		ISASCursor dbcur=dbc.startCursor(myselect);
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
		if ((vdbr != null) && (vdbr.size() > 0)){
                  for(int i=0; i<vdbr.size()-1; i++){
                    ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                    if (dbr.get("cod_diag1")!=null && !((String)dbr.get("cod_diag1")).equals("")){
                      String desc = util.getDecode(dbc,"icd9","cd_diag",
                             (String)util.getObjectField(dbr,"cod_diag1",'S'),"diagnosi");
                      if(!desc.equals("")){
                        if(desc.length()>=50)
                          desc=desc.substring(0,50);
                        dbr.put("diag1", desc);
                      }
                    }else dbr.put("diag1", "");
                    dbr.put("ospedale",util.getDecode(dbc,"ospedali","codosp",
                           (String)util.getObjectField(dbr,"cod_ospedale",'S'),"descosp"));
                    dbr.put("reparto",util.getDecode(dbc,"reparti","cd_rep",
                           (String)util.getObjectField(dbr,"cod_reparto",'S'),"reparto"));
                  }
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

public ISASRecord CaricaDecodifiche(ISASConnection dbc,ISASRecord dbr)
throws  Exception {
    boolean done=false;
    try{
        Hashtable h = dbr.getHashtable();
        dbr.put("opsegna1",util.getDecode(dbc,"operatori","codice",
                           (String)util.getObjectField(dbr,"oper_dimis",'S'),
                           "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","oper_alias"));
        dbr.put("opsegna2",util.getDecode(dbc,"operatori","codice",
                           (String)util.getObjectField(dbr,"oper_carico",'S'),
                           "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","oper_alias"));
        dbr.put("des_osp",util.getDecode(dbc,"ospedali","codosp",
                           (String)util.getObjectField(dbr,"cod_ospedale",'S'),"descosp"));
        dbr.put("des_rep",util.getDecode(dbc,"reparti","cd_rep",
                           (String)util.getObjectField(dbr,"cod_reparto",'S'),"reparto"));
        for(int i=0; i<=4; i++){
              if (dbr.get("cod_diag"+i)!=null && !((String)dbr.get("cod_diag"+i)).equals("")){
                dbr.put("desc_patol"+i, util.getDecode(dbc,"icd9","cd_diag",
                           (String)util.getObjectField(dbr,"cod_diag"+i,'S'),"diagnosi"));
              }else dbr.put("desc_patol"+i, "");
        }
        String sel = "SELECT nvl(trim(cognome),'') || ' ' || nvl(trim(nome),'') ass FROM cartella "+
                     " WHERE n_cartella="+dbr.get("n_cartella");
        ISASRecord dbcar = dbc.readRecord(sel);
        if(dbcar!=null)
          dbr.put("assistito", (String)dbcar.get("ass"));
        return dbr;
    }catch(Exception e){
        throw new SQLException("Errore eseguendo una CaricaDecodifiche() - "+  e);
    }
  }

}


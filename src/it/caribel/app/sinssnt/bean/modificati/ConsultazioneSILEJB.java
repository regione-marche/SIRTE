package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 24/07/2007 - EJB di connessione alla procedura SINS Tabella SchedaUte
//
// G.Brogi
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class ConsultazioneSILEJB extends SINSSNTConnectionEJB  {

public ConsultazioneSILEJB() {}

public ISASRecord queryKey_schedaut(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from sil_scheda where "+
			"n_cartella="+(String)h.get("n_cartella");
		System.out.println("QueryKey su SchedaUte: "+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
		if (dbr!=null){//decodifiche varie
		    it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		    String segnalante_op_1 = (String)util.getObjectField(dbr,"segnalante_op_1",'S');
		    dbr.put("opsegna1",util.getDecode(dbc,"operatori","codice",
			      segnalante_op_1,"cognome"));

		    String segnalante_op_2 = (String)util.getObjectField(dbr,"segnalante_op_2",'S');
		    dbr.put("opsegna2",util.getDecode(dbc,"operatori","codice",
			      segnalante_op_2,"cognome"));

		    String oper_1_segn = (String)util.getObjectField(dbr,"oper_1_segn",'S');
		    dbr.put("oprice1",util.getDecode(dbc,"operatori","codice",
			      oper_1_segn,"cognome"));

		    String oper_2_segn = (String)util.getObjectField(dbr,"oper_2_segn",'S');
		    dbr.put("oprice2",util.getDecode(dbc,"operatori","codice",
			      oper_2_segn,"cognome"));

		    String oper_ref1 = (String)util.getObjectField(dbr,"oper_ref1",'S');
		    dbr.put("oprefe1",util.getDecode(dbc,"operatori","codice",
			      oper_ref1,"cognome"));

		    String oper_ref2 = (String)util.getObjectField(dbr,"oper_ref2",'S');
		    dbr.put("oprefe2",util.getDecode(dbc,"operatori","codice",
			      oper_ref2,"cognome"));

		    String nazionalita = (String)util.getObjectField(dbr,"nazionalita",'S');
		    dbr.put("des_cittadin",util.getDecode(dbc,"cittadin","cd_cittadin",
			      nazionalita,"des_cittadin"));

		    String azienda = (String)util.getObjectField(dbr,"occupato_azienda",'I');
		    dbr.put("az_ragsoc",util.getDecode(dbc,"sil_aziende","az_progr",
			      azienda,"az_ragsoc"));

		    String distretto = (String)util.getObjectField(dbr,"distretto",'S');
		    dbr.put("des_distr",util.getDecode(dbc,"distretti","cod_distr",
			      distretto,"des_distr"));
			//prova bargi
			//String servizi = (String)util.getObjectField(dbr,"seguito_da",'S');
		    //dbr.put("seguito_da",selectTabBase(dbc,servizi,"SERVIZI"));

		    //caricamento griglia familiari
		    dbr.put("griglia_familiari",CaricaFamiliari(dbc,dbr,""+dbr.get("n_cartella")));

		    //caricamento griglia interventi
		    dbr.put("griglia_interventi",CaricaInterventi(dbc,dbr,""+dbr.get("n_cartella")));

		    //caricamento griglia schede lavoro
		    dbr.put("griglia_schedelav",CaricaSchedeLav(dbc,dbr,""+dbr.get("n_cartella")));

		//caricamento combo sola consultazione
		CaricaComboCons(dbc,dbr,""+dbr.get("n_cartella"));

		}//end if dbr!=null

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


// ==================================================================
// caricamento combo tipo_servizio / status_attuale / int_progetto
// sono caricati con i rispettivi campi delle tabelle
// sil_schede_lav e sil_interventi, leggendo il record pi� recente.
////bargi 31/01/2007 dati solo da interv
// ==================================================================
private void CaricaComboCons(ISASConnection dbc, ISASRecord dbr,String cartella)throws Exception{
  try{


      String sel2 = "SELECT * FROM sil_interventi"+
		    " WHERE n_cartella = "+cartella+
		    " ORDER BY int_data DESC"; // G.Brogi 17/07/07ordinamento per data
      ISASRecord dbi = dbc.readRecord(sel2);
		if(dbi.get("int_progetto")!=null)
			  dbr.put("progetto_attuale",""+dbi.get("int_progetto"));


	if(dbi!=null)
		EstraggoDatiDaInt(dbr,dbi);
  }catch(Exception e){
    System.out.println("Errore eseguendo una CaricaComboCons: "+e);
  }
}

	private void EstraggoDatiDaInt(ISASRecord dbr,ISASRecord dbi)throws Exception{
		try{
			if(dbi.get("int_servizio")!=null)
			  dbr.put("tipo_servizio",""+dbi.get("int_servizio"));
			if(dbi.get("int_intervento")!=null)
			  dbr.put("status_attuale",""+dbi.get("int_intervento"));
		}catch(Exception e){
			System.out.println("Errore in EstraggoDatiDaInt: "+e);
		}

	}

  private Vector CaricaFamiliari(ISASConnection dbc, ISASRecord dbr,String cartella)throws Exception{
    try{
	String sel = "  SELECT * FROM sil_familiari WHERE "+
		     " n_cartella = "+cartella;
	ISASCursor dbcur = dbc.startCursor(sel);
	Vector vdbr = dbcur.getAllRecord();

	for(Enumeration en = vdbr.elements();en.hasMoreElements(); ){
		ISASRecord dbv = (ISASRecord)en.nextElement();
		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		String par = (String)util.getObjectField(dbv,"sf_gradopar",'S');
		dbv.put("parentela",util.getDecode(dbc,"parent","codice",par,"descrizione"));

	}
	System.out.println("CaricaFam - vettore = "+vdbr.size());

	dbcur.close();
	return vdbr;
    }catch(Exception e){
        System.out.println("Errore eseguendo una CaricaFamiliari: "+e);
	return null;
    }
  }

  private Vector CaricaInterventi(ISASConnection dbc, ISASRecord dbr,String cartella)throws Exception{
    try{
	String sel = "  SELECT * FROM sil_interventi WHERE "+
		     " n_cartella = "+cartella;
	ISASCursor dbcur = dbc.startCursor(sel);
	Vector vdbr = dbcur.getAllRecord();
	/*for(Enumeration enum = vdbr.elements();enum.hasMoreElements(); ){
		ISASRecord dbv = (ISASRecord)enum.nextElement();
		    it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		  String serv = (String)dbv.get("int_servizio");
		  String seld = "SELECT tb_descrizione FROM sil_tabase "+
				"WHERE tb_cod='TIPOSERV' AND tb_val = '"+serv+"'";
		  ISASRecord dbd = dbc.readRecord(seld);
		  String desc = (String)util.getObjectField(dbd,"tb_descrizione",'S');

		  dbv.put("desc_servizio",desc);

	}*/
System.out.println("CaricaInt - vettore = "+vdbr.size());
	dbcur.close();
	return vdbr;
    }catch(Exception e){
        System.out.println("Errore eseguendo una CaricaInterventi: "+e);
	return null;
    }
  }

  private Vector CaricaSchedeLav(ISASConnection dbc, ISASRecord dbr,String cartella)throws Exception{
    try{
	String sel = "  SELECT * FROM sil_schede_lav WHERE "+
		     " n_cartella = "+cartella;
	ISASCursor dbcur = dbc.startCursor(sel);
	Vector vdbr = dbcur.getAllRecord();
	for(Enumeration en = vdbr.elements();en.hasMoreElements(); ){
		ISASRecord dbv = (ISASRecord)en.nextElement();
		    it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		  String az = (String)util.getObjectField(dbv,"sl_azienda",'I');
		  dbv.put("desc_azienda",util.getDecode(dbc,"sil_aziende","az_progr",az,"az_ragsoc"));

	}
System.out.println("CaricaSchedeLav - vettore = "+vdbr.size());
	dbcur.close();
	return vdbr;
    }catch(Exception e){
        System.out.println("Errore eseguendo una CaricaSchedeLav: "+e);
	return null;
    }
  }

public ISASRecord queryKey_schedelav(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="";
		debugMessage("sl_progr"+h.get("sl_progr"));
		if(h.get("sl_progr")!=null)// && !(h.get("sl_progr")).equals("0"))
		    myselect="Select * from sil_schede_lav where "+
			"n_cartella="+(String)h.get("n_cartella")+
			" AND sl_progr="+h.get("sl_progr");
		else myselect="Select s1.* from sil_schede_lav s1 where "+
			"s1.n_cartella="+(String)h.get("n_cartella")+
			" AND s1.sl_progr= (select max(s2.sl_progr) from sil_schede_lav s2 where "+
			"s2.n_cartella="+(String)h.get("n_cartella")+")";
		debugMessage("QueryKey su SchedeLav: "+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
		if (dbr!=null){
		  //decodifiche varie
		  it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		  String op_1 = (String)util.getObjectField(dbr,"sl_operatore1",'S');
		  dbr.put("desc_op1",util.getDecode(dbc,"operatori","codice",op_1,"cognome"));

		  String op_2 = (String)util.getObjectField(dbr,"sl_operatore2",'S');
		  dbr.put("desc_op2",util.getDecode(dbc,"operatori","codice",op_2,"cognome"));

		  String az = (String)util.getObjectField(dbr,"sl_azienda",'I');
		  dbr.put("desc_azienda",util.getDecode(dbc,"sil_aziende","az_progr",az,"az_ragsoc"));

		  String com = (String)util.getObjectField(dbr,"sl_comune_sede",'S');
		  dbr.put("desc_comune",util.getDecode(dbc,"comuni","codice",com,"descrizione"));

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

public ISASRecord queryKey_interv(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="";
		debugMessage("int_progr"+h.get("int_progr"));
		if(h.get("int_progr")!=null)//&& !(h.get("int_progr")).equals("0"))
		    myselect="Select * from sil_interventi where "+
			"n_cartella="+(String)h.get("n_cartella")+
			" AND int_progr="+h.get("int_progr");
		else myselect="Select i.* from sil_interventi i where "+
			"i.n_cartella="+(String)h.get("n_cartella")+
			" AND i.int_progr= (select max(i2.int_progr) from sil_interventi i2 where "+
			"i2.n_cartella="+(String)h.get("n_cartella")+")";

		System.out.println("QueryKey su Interventi: "+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
		if (dbr!=null){
		  //decodifiche varie
		  it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		  String op_1 = (String)util.getObjectField(dbr,"int_operatore1",'S');
		  dbr.put("cognomeop1",util.getDecode(dbc,"operatori","codice",op_1,"cognome"));

		  String op_2 = (String)util.getObjectField(dbr,"int_operatore2",'S');
		  dbr.put("cognomeop2",util.getDecode(dbc,"operatori","codice",op_2,"cognome"));

		  String azienda = (String)util.getObjectField(dbr,"int_azienda",'I');
		  dbr.put("az_ragsoc",util.getDecode(dbc,"sil_aziende","az_progr",
			    azienda,"az_ragsoc"));

		  String comune = (String)util.getObjectField(dbr,"int_comune_sede",'S');
		  dbr.put("descrizione",util.getDecode(dbc,"comuni","codice",comune,"descrizione"));
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

public ISASRecord queryKey_familiari(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String datavar = (String)h.get("data_variazione");
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from sb_familiari where "+
			"n_cartella="+(String)h.get("n_cartella")+" and " +
			"data_variazione="+formatDate(dbc, datavar)+" and "+
            "tipo_fam='"+(String)h.get("tipo_fam")+"'"+
			" AND progr="+(String)h.get("progr");
		ISASRecord dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException(" Errore eseguendo una queryKey()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}


public Vector query_familiari(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String cod="";
        String sel="";
        ISASRecord secondo=null;
	String datavar = (String)h.get("data_variazione");
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from sb_familiari where "+
		  "n_cartella="+(String)h.get("n_cartella")+" and "+
                  "data_variazione="+formatDate(dbc, datavar)+
		  "ORDER BY data_variazione DESC, tipo_fam, progr ";
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
                {
                  ISASRecord dbr=(ISASRecord)senum.nextElement();
                  if(dbr.get("com_nasc")!=null && !((String)dbr.get("com_nasc")).equals("")){
                      cod=(String)dbr.get("com_nasc");
                      sel="SELECT descrizione FROM comuni WHERE codice='"+cod+"'";
                      secondo=dbc.readRecord(sel);
                      dbr.put("desc1",secondo.get("descrizione"));
                  }else dbr.put("desc1"," ");
                  if(dbr.get("res_citta")!=null && !((String)dbr.get("res_citta")).equals("")){
                      cod=(String)dbr.get("res_citta");
                      sel="SELECT descrizione FROM comuni WHERE codice='"+cod+"'";
                      secondo=dbc.readRecord(sel);
                      dbr.put("desc2",secondo.get("descrizione"));
                  }else dbr.put("desc2"," ");
                  if(dbr.get("parentela")!=null && !((String)dbr.get("parentela")).equals("")){
                      cod=(String)dbr.get("parentela");
                      sel="SELECT descrizione FROM parent WHERE codice='"+cod+"'";
                      secondo=dbc.readRecord(sel);
                      dbr.put("parent",secondo.get("descrizione"));
                  }else dbr.put("parent"," ");
                  if(dbr.get("occupazione")!=null && !((String)dbr.get("occupazione")).equals("")){
                      cod=(String)dbr.get("occupazione");
                      sel="SELECT descrizione FROM profes WHERE codice='"+cod+"'";
                      secondo=dbc.readRecord(sel);
                      dbr.put("occupazione_descr",secondo.get("descrizione"));
                  }else dbr.put("occupazione_descr"," ");
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

public Vector queryDate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select DISTINCT data_variazione from "+
			"sb_familiari where n_cartella="+(String)h.get("n_cartella")+
			" ORDER BY data_variazione DESC";
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryDate()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}


public ISASRecord queryKey_genaff(myLogin mylogin,Hashtable h) throws
SQLException, ISASPermissionDeniedException,ISASMisuseException {
	boolean done=false;
	ISASConnection dbc=null;
	ISASRecord dbrPrest = null;
	String cartella = "";
	String data = "";
	String data1 = "";
	String data2 = "";
	String myselect = "";
	String mysel = "";
	try{
		dbc=super.logIn(mylogin);

		ISASRecord dbr=leggiRecord(dbc,h);

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

public Vector query_genaff(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		if(h.get("n_cartella") == null) {
			throw new SQLException("Manca numero cartella in esecuzione query()");
		}
		StringBuffer myselect= new StringBuffer("Select * "+
			"from sb_genitori where "+
			"n_cartella="+(String)h.get("n_cartella"));
		if(h.get("data") != null) {
			myselect.append(" and data="+formatDate(dbc,(String)h.get("data")));
		}
		myselect.append(" ORDER BY data DESC");
                System.out.println("Query_combo=>SbGenAff"+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect.toString());
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

public Vector query_progetto(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	ISASRecord istab=null;
	try  {
		dbc = super.logIn(mylogin);
		String myselect="SELECT * FROM sb_progetti WHERE "+
			"n_cartella= "+h.get("n_cartella")+
			" ORDER BY spp_data_inizio DESC ";
		System.out.println("query "+myselect);
		ISASCursor dbcur = dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		for(int c=0;c<vdbr.size();c++)
		{
			ISASRecord isdb=(ISASRecord)vdbr.elementAt(c);
			if(isdb.get("spp_servizio")!=null && !isdb.get("spp_servizio").toString().trim().equals(""))
			{
				myselect="Select tb_descrizione from sb_tabase where tb_cod='SERVIZIO'"
					+" AND tb_val='"+isdb.get("spp_servizio")+"'";
				istab=dbc.readRecord(myselect);
				if(istab!=null)
					isdb.put("servizio_desc",""+istab.get("tb_descrizione"));
			}
			if(isdb.get("spp_progetto")!=null && !isdb.get("spp_progetto").toString().trim().equals(""))
			{
				myselect="Select tb_descrizione from sb_tabase where tb_cod='PROGETTO'"
					+" AND tb_val='"+isdb.get("spp_progetto")+"'";
				istab=dbc.readRecord(myselect);
				if(istab!=null)
					isdb.put("progetto_desc",""+istab.get("tb_descrizione"));
			}
			if(isdb.get("spp_ente")!=null && !isdb.get("spp_ente").toString().trim().equals(""))
			{
				myselect="Select sbe_ragsoc from sb_enti where sbe_progr='"
					+isdb.get("spp_ente")+"'";
				istab=dbc.readRecord(myselect);
				if(istab!=null)
					isdb.put("ente_desc",""+istab.get("sbe_ragsoc"));
			}
			//carico anche la data di apertura della cartella per i controlli sucessivi
				myselect="Select data_apertura from cartella where n_cartella="
					+isdb.get("n_cartella");
				istab=dbc.readRecord(myselect);
				isdb.put("data_cartella",istab.get("data_apertura"));

			vdbr.setElementAt(isdb,c);
		}
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;
		return vdbr;
	} catch(Exception e) {
		System.out.println("SbProgettoEJB.query(): "+e);
		throw new SQLException("Errore eseguendo una query()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				System.out.println("SbProgettoEJB.query(): "+e1);
			}
		}
	}
}


public Vector query_relazioni(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	ISASRecord istab=null;
	try  {
		dbc = super.logIn(mylogin);
		String myselect="SELECT * FROM sb_relazioni WHERE "+
			"n_cartella= "+h.get("n_cartella")+
			" ORDER BY sr_cognome,sr_nome DESC ";
		System.out.println("query "+myselect);
		ISASCursor dbcur = dbc.startCursor(myselect);
		Vector vdbr = dbcur.getAllRecord();
		for(int c=0;c<vdbr.size();c++)
		{
			ISASRecord isdb=(ISASRecord)vdbr.elementAt(c);
			if(isdb.get("sr_tipo_relaz")!=null && !isdb.get("sr_tipo_relaz").toString().trim().equals(""))
			{
				myselect="Select tb_descrizione from sb_tabase where tb_cod='TIPORELA'"
					+" AND tb_val='"+isdb.get("sr_tipo_relaz")+"'";
				istab=dbc.readRecord(myselect);
				if(istab!=null)
					isdb.put("tipo_desc",""+istab.get("tb_descrizione"));
			}
			if(isdb.get("sr_citta")!=null && !isdb.get("sr_citta").toString().trim().equals(""))
			{
				myselect="Select descrizione from comuni where codice='"
					+isdb.get("sr_citta")+"'";
				istab=dbc.readRecord(myselect);
				if(istab!=null)
					isdb.put("comuni_desc",""+istab.get("descrizione"));
			}
			//carico anche la data di apertura della cartella per i controlli sucessivi
				myselect="Select data_apertura from cartella where n_cartella="
					+isdb.get("n_cartella");
				istab=dbc.readRecord(myselect);
				isdb.put("data_cartella",istab.get("data_apertura"));

			vdbr.setElementAt(isdb,c);
		}
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done = true;
		return vdbr;
	} catch(Exception e) {
		System.out.println("SbRelazioniEJB.query(): "+e);
		throw new SQLException("Errore eseguendo una query()");
	} finally {
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				System.out.println("SbRelazioniEJB.query(): "+e1);
			}
		}
	}
}


public ISASRecord leggiRecord(ISASConnection dbc,Hashtable h) throws
SQLException, ISASPermissionDeniedException,ISASMisuseException {
	ISASRecord dbrPrest = null;
	String cartella = "";
	String data = "";
	String data1 = "";
	String data2 = "";
	String myselect = "";
	String mysel = "";
	try{
                ServerUtility su =new ServerUtility();
		if(!(h.get("n_cartella") == null || h.get("n_cartella").equals("")))
			cartella = (String)h.get("n_cartella");
		if(!(h.get("data")== null  || h.get("data").equals("")))
			data = (String)h.get("data");
		myselect="SELECT * FROM sb_genitori WHERE ";
                mysel=su.addWhere(mysel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
		if(data!=null && !data.equals(""))
                	mysel=su.addWhere(mysel,su.REL_AND,"data",su.OP_EQ_NUM,formatDate(dbc,data));
		else
			mysel += " ORDER BY data DESC";
                myselect=myselect+mysel;
                System.out.println("SbGenAff-->leggiRecord: "+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
                if (dbr!=null)
		{
			String cod="";
			String sel="";
			//carico le decodifiche
			if(dbr.get("p_comune_nasc")!=null && !((String)dbr.get("p_comune_nasc")).equals(""))
			{
                      		cod=(String)dbr.get("p_comune_nasc");
                      		sel="SELECT descrizione FROM comuni WHERE codice='"+cod+"'";
                      		ISASRecord secondo=dbc.readRecord(sel);
                      		dbr.put("p_comdesc",((secondo.get("descrizione")!=null)?((String)secondo.get("descrizione")).trim():""));
			}else dbr.put("p_comdesc"," ");
			if(dbr.get("m_comune_nasc")!=null && !((String)dbr.get("m_comune_nasc")).equals(""))
			{
                      		cod=(String)dbr.get("m_comune_nasc");
                      		sel="SELECT descrizione FROM comuni WHERE codice='"+cod+"'";
                      		ISASRecord secondo=dbc.readRecord(sel);
                      		dbr.put("m_comdesc",((secondo.get("descrizione")!=null)?((String)secondo.get("descrizione")).trim():""));
			}else dbr.put("m_comdesc"," ");
			if(dbr.get("citta")!=null && !((String)dbr.get("citta")).equals(""))
			{
                      		cod=(String)dbr.get("citta");
                      		sel="SELECT descrizione FROM comuni WHERE codice='"+cod+"'";
                      		ISASRecord secondo=dbc.readRecord(sel);
                      		dbr.put("res_comdesc",((secondo.get("descrizione")!=null)?((String)secondo.get("descrizione")).trim():""));
			}else dbr.put("res_comdesc"," ");
			if(dbr.get("p_cittadinanza")!=null && !((String)dbr.get("p_cittadinanza")).equals(""))
			{
                      		cod=(String)dbr.get("p_cittadinanza");
                      		sel="SELECT des_cittadin FROM cittadin WHERE cd_cittadin='"+cod+"'";
                      		ISASRecord secondo=dbc.readRecord(sel);
                      		dbr.put("p_cittadindesc",((secondo.get("des_cittadin")!=null)?((String)secondo.get("des_cittadin")).trim():""));
			}else dbr.put("p_cittadindesc"," ");
			if(dbr.get("m_cittadinanza")!=null && !((String)dbr.get("m_cittadinanza")).equals(""))
			{
                      		cod=(String)dbr.get("m_cittadinanza");
                      		sel="SELECT des_cittadin FROM cittadin WHERE cd_cittadin='"+cod+"'";
                      		ISASRecord secondo=dbc.readRecord(sel);
                      		dbr.put("m_cittadindesc",((secondo.get("des_cittadin")!=null)?((String)secondo.get("des_cittadin")).trim():""));
			}else dbr.put("m_cittadindesc"," ");
                  
			myselect="";
			mysel="";
			myselect="SELECT * FROM sb_affidi WHERE ";
			mysel=su.addWhere(mysel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
			if(data!=null && !data.equals(""))
				mysel=su.addWhere(mysel,su.REL_AND,"data",su.OP_EQ_NUM,formatDate(dbc,data));
			myselect=myselect+mysel;
			myselect=myselect+" ORDER BY corso_pass DESC";
			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdb=dbcur.getAllRecord();
			ISASUtil iu =new ISASUtil();
			if (vdb.size()>0)
			{
			//carico le decodifiche
			for(int p=0;p<vdb.size();p++)
			{
				ISASRecord is2=(ISASRecord)vdb.elementAt(p);
				if(is2.get("nazionalita")!=null && !((String)is2.get("nazionalita")).equals(""))
				{
					cod=(String)is2.get("nazionalita");
					sel="SELECT des_cittadin FROM cittadin WHERE cd_cittadin='"+cod+"'";
					ISASRecord secondo=dbc.readRecord(sel);
					is2.put("nazionalita_desc",((secondo.get("des_cittadin")!=null)?((String)secondo.get("des_cittadin")).trim():""));
				}else is2.put("nazionalita_desc"," ");
					String copas=(String)iu.getObjectField(is2,"corso_pass",'S');
					if(copas.equals("1"))
						copas="Passato";
					else if(copas.equals("2"))
						copas="In corso";
					is2.put("corso_pass_desc",copas);
					vdb.setElementAt(is2,p);
				}
				dbr.put("tabella",vdb);
			}
		}
	       return dbr;
	}catch(ISASPermissionDeniedException e){
		System.out.println("eccezione permesso negato "+e);
		return null;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryKey()  ");
	}
}


public Vector queryPaginate_schedelav(myLogin mylogin,Hashtable h) throws  SQLException {
        boolean done=false;
        String scr=" ";
        ISASConnection dbc=null;
        try{
                dbc=super.logIn(mylogin);
                String myselect="Select * from sil_schede_lav";


                scr=(String)(h.get("n_cartella"));
                if ((scr!=null && !scr.equals("")))
                        myselect=myselect+" where n_cartella ="+scr;
                myselect=myselect+" ORDER BY sl_progr ";
                System.out.println("query GridSchedeLav: "+myselect);

                ISASCursor dbcur=dbc.startCursor(myselect,200);
                //Vector vdbr=dbcur.getAllRecord();
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);


                it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
                for(int i = 0; i < vdbr.size()-1; i++){

                        ISASRecord dbr = (ISASRecord)vdbr.elementAt(i);
                        String codice = (String)util.getObjectField(dbr,
                                        "sl_azienda",
                                        'I');
                        dbr.put("az_ragsoc",
                                        util.getDecode(dbc,"sil_aziende",
                                        "az_progr",codice,"az_ragsoc"));
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



public Vector queryPaginate_interventi(myLogin mylogin,Hashtable h) throws  SQLException {
        boolean done=false;
        String scr=" ";
        ISASConnection dbc=null;
        try{
                dbc=super.logIn(mylogin);
                String myselect="Select * from sil_interventi";


                scr=(String)(h.get("n_cartella"));
                if ((scr!=null && !scr.equals("")))
                        myselect=myselect+" where n_cartella ="+scr;
                myselect=myselect+" ORDER BY int_progr ";
                System.out.println("query GridInterventi: "+myselect);

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

}

package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 11/05/2000 - EJB di connessione alla procedura SINS Tabella Acchand
//
// paolo ciampolini
//
// ============================================================================
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*; //gb 04/06/07
import it.pisa.caribel.sinssnt.connection.*;

public class AcchandEJB extends SINSSNTConnectionEJB {

public AcchandEJB() {}

private void decodificaZona(ISASConnection dbc, ISASRecord dbr, Hashtable h1) throws Exception
  {
  if (h1.get("zona") != null && !(h1.get("zona").equals("")))
    {
    String codpro  = (String)h1.get("zona");
    String myinv = "SELECT * FROM zone WHERE codice_zona='" + codpro + "'";
    System.out.println("AcchandEJB/decodificaZona: " + myinv);
// 11/06/07 ISASRecord dbrinv = dbc.readRecord(myinv);
	// 11/06/07 --------------
	ISASRecord dbrinv = null;
	try {
	    dbrinv = dbc.readRecord(myinv);
	} catch(ISASPermissionDeniedException e){
		System.out.println("AcchandEJB/decodificaZona: MANCANO I DIRITTI di lettura sulla zona=["+codpro+"]");
      	dbrinv = null;
    }
	// 11/06/07 --------------
    if (dbrinv!=null && dbrinv.get("descrizione_zona")!=null)
      dbr.put("descrizione_zona", dbrinv.get("descrizione_zona"));
    else
      {
      dbr.put("descrizione_zona","NON ESISTE DECODIFICA");
      dbr.put("zona","");
      }
    }
  else
    dbr.put("descrizione_zona","");
  }

private void decodificaOperatore(ISASConnection dbc, ISASRecord dbr, Hashtable h1) throws Exception
  {
  if(h1.get("cod_operatore")!=null && !(h1.get("cod_operatore").equals("")))
    {
    String codric  = (String)h1.get("cod_operatore");
    String myric="SELECT * FROM operatori WHERE codice = '" + codric + "'";
    System.out.println("AcchandEJB/decodificaOperatore: " + myric);
    ISASRecord dbric = dbc.readRecord(myric);
    if (dbric!=null && dbric.get("cognome")!=null)
      {
      String strDescrOperatore = (String)dbric.get("cognome") + " " + (String)dbric.get("nome");
      dbr.put("cognome",strDescrOperatore);
      }
    else
      {
      dbr.put("cognome","NON ESISTE DECODIFICA");
      dbr.put("cod_operatore","");
      }
    }
  else
    dbr.put("cognome","");
  }

private void decodificaPatologia(ISASConnection dbc, ISASRecord dbr, Hashtable h1) throws Exception
  {
  if(h1.get("patol")!=null && !(h1.get("patol").equals("")))
    {
    String codop  = (String)h1.get("patol");
    String myop = "SELECT * FROM icd9 WHERE cd_diag = '" + codop + "'";
    System.out.println("AcchandEJB/decodificaPatologia: " + myop);
    ISASRecord dbop = dbc.readRecord(myop);
    if (dbop!=null && dbop.get("diagnosi")!=null)
      dbr.put("diagnosi",dbop.get("diagnosi"));
    else
      {
      dbr.put("diagnosi","NON ESISTE DECODIFICA");
      dbr.put("patol","");
      }
    }
  else
    dbr.put("diagnosi","");
  }

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException
  {

  boolean done = false;
  ISASConnection dbc = null;
  String datavar = (String)h.get("data");

  try {
    dbc = super.logIn(mylogin);

    String myselect = "SELECT *" +
                      " FROM ass_acchand" +
                      " WHERE n_cartella = " + (String)h.get("n_cartella") +
                      " AND data = " + formatDate(dbc, datavar);
    System.out.println("AcchandEJB-->QUERYKEY: " + myselect);
    ISASRecord dbr = dbc.readRecord(myselect);

    if (dbr != null)
      {
      Hashtable h1 = dbr.getHashtable();

      decodificaZona(dbc, dbr, h1);
      decodificaOperatore(dbc, dbr, h1);
      decodificaPatologia(dbc, dbr, h1);
      }

    dbc.close();
    super.close(dbc);
    done=true;
    return dbr;
    } catch(Exception e) {
      System.out.println(e);
      throw new SQLException("AcchandEJB: Errore eseguendo la queryKey()  ");
    } finally {
      if(!done) {
        try {
          dbc.close();
          super.close(dbc);
        } catch(Exception e1) {System.out.println(e1);}
      }
    }// finally
  }

private void decodificaQueryFrameCntrl(ISASRecord dbr, Hashtable ht,
                                       String dbFieldName, String dbDecodName) throws Exception
  {
    String decodifica = (String) ht.get(dbr.get(dbFieldName));
    dbr.put(dbDecodName,decodifica);
  }

private void decodificaQueryInfo(Vector vdbr) throws Exception
  {
  Hashtable htSgrFis = new Hashtable();
  htSgrFis.put("0","NO");
  htSgrFis.put("1","SI");

  Hashtable htAliSolo = new Hashtable();
  htAliSolo.put("9","-");
  htAliSolo.put("0","NO");
  htAliSolo.put("1","SI");
  htAliSolo.put("2","Con aiuto");

  Hashtable htCapVis = new Hashtable();
  htCapVis.put("9","-");
  htCapVis.put("0","NO");
  htCapVis.put("1","SI");
  htCapVis.put("2","Parzialmente");

  Hashtable htCapVer = new Hashtable();
  htCapVer.put("9","-");
  htCapVer.put("0","NO");
  htCapVer.put("1","SI");
  htCapVer.put("2","Parzialmente");

  for (int i=0; i<vdbr.size(); i++)
    {
    ISASRecord dbr = (ISASRecord) vdbr.get(i);
    decodificaQueryFrameCntrl(dbr, htSgrFis, "motsgra", "motsgra_decod");
    decodificaQueryFrameCntrl(dbr, htAliSolo, "cp_d3", "cp_d3_decod");
    decodificaQueryFrameCntrl(dbr, htCapVis, "cie_d11", "cie_d11_decod");
    decodificaQueryFrameCntrl(dbr, htCapVer, "cie_d12", "cie_d12_decod");
    }
  }

public Vector query(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  ISASCursor dbcur = null;

  try {
    dbc = super.logIn(mylogin);
    if (h.get("n_cartella") == null)
      {
      throw new SQLException("AcchandEJB: Manca numero cartella in esecuzione query()");
      }
			
	//gb 10/05/07 (02/05/06 m). -------------
	String dtChiusPrg = (String)h.get("ap_data_chiusura");
	String critDtChius = "";
	if ((dtChiusPrg != null) && (!dtChiusPrg.trim().equals("")))		
		critDtChius = " AND data <= " + formatDate(dbc, dtChiusPrg);
	//gb 10/05/07 (02/05/06 m). -------------

    String myselect = " SELECT *" +
                      " FROM ass_acchand" +
                      " WHERE n_cartella = " + (String)h.get("n_cartella") +
		      critDtChius + //gb 10/05/07 (02/05/06 m).
                      " ORDER BY data DESC";
    System.out.println("AcchandEJB-->QUERY: " + myselect);
    dbcur=dbc.startCursor(myselect);
    Vector vdbr=dbcur.getAllRecord();

    decodificaQueryInfo(vdbr);

    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return vdbr;
    }catch(Exception e){
      System.out.println(e);
      throw new SQLException("AcchandEJB: Errore eseguendo la query()  ");
    }finally{
      if(!done){
        try{
          if (dbcur != null)
            dbcur.close();
          dbc.close();
          super.close(dbc);
        }catch(Exception e1){System.out.println(e1);}
      }
    }
  }

//gb 04/06/07 *******
private boolean dtRecNonInclusaInUnProgetto(ISASConnection dbc, String cartella, String data_variazione) throws Exception
   {
  String mySel = "SELECT *" +
		 " FROM ass_progetto" +
                 " WHERE n_cartella = " + cartella +
                 " AND ap_data_apertura <= " + data_variazione +
		 " AND (ap_data_chiusura >= " + data_variazione + 
		 " OR ap_data_chiusura is null)";
  ISASCursor dbcur = dbc.startCursor(mySel);

  if ((dbcur != null) && (dbcur.getDimension() >0))
    return false; // la data � inclusa
  else
    return true; // la data � non inclusa
   }
//gb 04/06/07: fine *******

public ISASRecord insert(myLogin mylogin, Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException
  {
  ISASConnection dbc=null;
  boolean done=false;
  String cartella=null;
  String datavar = null;
  String data=null;

  try {
    dbc=super.logIn(mylogin);
    cartella=(String)h.get("n_cartella");
    //data=(String)h.get("data");
    datavar = (String)h.get("data");
    data=formatDate(dbc, datavar);
    }catch (Exception e){
      System.out.println(e);
      throw new SQLException("AcchandEJB/insert: Errore: manca la chiave primaria");
    }

  try{

    dbc.startTransaction(); //gb 10/05/07
//gb 04/06/07 *******
    if (dtRecNonInclusaInUnProgetto(dbc, cartella, data))
	{
	String msg = "Attenzione: La data variazione non e' inclusa in nessun progetto per quell'assistito!";
	throw new CariException(msg, -2);
	}
//gb 04/06/07: fine *******

/*gb 10/05/07 ******* La tabella 'setint' non � pi� in uso.
    int i=10;
    boolean aggiunto_rec_setint = false;
    while(!aggiunto_rec_setint)
      {
      try{
        dbc.startTransaction();
        // Inserisce un nuovo record in tabella setint
        ISASRecord dbr_setint = dbc.newRecord("setint");
        String cod_interv = "ACCHAN";
        dbr_setint.put("cartella", new Integer(cartella));
        dbr_setint.put("n_contatto", new Integer(-1));
        dbr_setint.put("cod_interv", cod_interv);
        dbr_setint.put("ult_data", datavar);
        dbc.writeRecord(dbr_setint);
        String myselect = "SELECT *" +
                          " FROM setint" +
                          " WHERE cartella = " + cartella +
                          " AND ult_data = " + data +
                          " AND n_contatto = -1" +
                          " AND cod_interv = '" + cod_interv + "'";
	System.out.println("AcchandEJB-->insert/setint: " + myselect);
        dbr_setint = dbc.readRecord(myselect);
        aggiunto_rec_setint = true;
        }catch (Exception ex){
          dbc.rollbackTransaction();
          i--;
          if(i <= 0)
            {
            throw new SQLException("AcchandEJB: Errore eseguendo un inserimento in tabella setint - "+  ex);
            }
        }
      }	// while
gb 10/05/07 *******/

    ISASRecord dbr = dbc.newRecord("ass_acchand");
    Enumeration n = h.keys();
    while(n.hasMoreElements())
      {
      String e = (String)n.nextElement();
      dbr.put(e,h.get(e));
      }
    dbc.writeRecord(dbr);
    String myselect = "SELECT *" +
                      " FROM ass_acchand" +
                      " WHERE n_cartella = " + cartella +
                      " AND data = " + data;
    System.out.println("AcchandEJB-->insert: " + myselect);
    dbr = dbc.readRecord(myselect);

    if (dbr != null)
      {
      Hashtable h1 = dbr.getHashtable();

      decodificaZona(dbc, dbr, h1);
      decodificaOperatore(dbc, dbr, h1);
      decodificaPatologia(dbc, dbr, h1);
      }

    dbc.commitTransaction();
    dbc.close();
    super.close(dbc);
    done=true;
    return dbr;
    }
//gb 04/06/07 *******
    	catch(CariException ce){
      		ce.setISASRecord(null);
      		try{
          		System.out.println("AcchandEJB.insert() => ROLLBACK");
          		dbc.rollbackTransaction();
        	}catch(Exception e1){
          	throw new CariException("Errore eseguendo la rollback() - " +  e1);
        	}
      		throw ce;
    	}
//gb 04/06/07: fine *******
      catch(DBRecordChangedException e){
      System.out.println(e);
      try{
        dbc.rollbackTransaction();
        }catch(Exception e1){
          throw new DBRecordChangedException("AcchandEJB/insert: Errore eseguendo la rollback() - "+  e);
        }
      throw e;
    }catch(ISASPermissionDeniedException e){
      System.out.println(e);
      try{
        dbc.rollbackTransaction();
        }catch(Exception e1){
          throw new DBRecordChangedException("AcchandEJB/insert: Errore eseguendo la rollback() - "+  e);
        }
      throw e;
    }catch(Exception e){
      try{
        dbc.rollbackTransaction();
        }catch(Exception e1){
          throw new DBRecordChangedException("AcchandEJB/insert: Errore eseguendo la rollback() - "+  e);
        }
      System.out.println(e);
      throw new SQLException("AcchandEJB: Errore eseguendo la insert() - "+  e);
    }finally{
      if(!done){
        try{
          dbc.close();
          super.close(dbc);
          }catch(Exception e2){System.out.println(e2);}
        }
    }
  }


public ISASRecord update(myLogin mylogin, ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException
  {
  boolean done=false;
  String cartella=null;
  String data=null;
  String data_var = null; //gb 04/06/07
  ISASConnection dbc=null;

  try
    {
    dbc=super.logIn(mylogin);
    cartella = (String)dbr.get("n_cartella");
    //data=(String)dbr.get("data");
    String datavar = (String)dbr.get("data");
    data=formatDate(dbc, datavar);
    }
  catch (Exception e)
    {
    System.out.println(e);
    throw new SQLException("AcchandEJB/update: Errore, manca la chiave primaria");
    }

  try
    {
//gb 04/06/07 *******
    if (dtRecNonInclusaInUnProgetto(dbc, cartella, data))
      {
	String msg = "Attenzione: La data variazione non e' inclusa in nessun progetto per quell'assistito!";
	throw new CariException(msg, -2);
      }
//gb 04/06/07: fine *******
    dbc.writeRecord(dbr);
    String myselect = "SELECT *" +
                      " FROM ass_acchand" +
                      " WHERE n_cartella = " + cartella +
                      " AND data = " + data;
    System.out.println("AcchandEJB-->update: " + myselect);
    dbr = dbc.readRecord(myselect);

    if (dbr != null)
      {
      Hashtable h1 = dbr.getHashtable();

      decodificaZona(dbc, dbr, h1);
      decodificaOperatore(dbc, dbr, h1);
      decodificaPatologia(dbc, dbr, h1);
      }

    dbc.close();
    super.close(dbc);
    done = true;
    return dbr;
    }
//gb 04/06/07 *******
    catch(CariException ce)
    {
	ce.setISASRecord(null);
	throw ce;
    }
//gb 04/06/07: fine *******
  catch(DBRecordChangedException e)
    {
    System.out.println(e);
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    System.out.println(e);
    throw e;
    }
  catch(Exception e1)
    {
    System.out.println(e1);
    throw new SQLException("AcchandEJB/update: Errore eseguendo una update() - "+  e1);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {System.out.println(e2);}
      }
    }
  }


public void delete(myLogin mylogin, ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done=false;
  ISASConnection dbc=null;
  try
    {
    dbc=super.logIn(mylogin);
    /*****
    String cartella =(String)dbr.get("n_cartella");
    String data     =(String)dbr.get("data");
    String contatto =(String)dbr.get("n_contatto");
    *****/
    String cartella =((Integer)dbr.get("n_cartella")).toString();
    //String data     =((java.sql.Date)dbr.get("data")).toString();
    String datavar=((java.sql.Date)dbr.get("data")).toString();
    String data =formatDate(dbc, datavar);
    String contatto =(new Integer(-1)).toString();
    String cod_interv = "ACCHAN";

    dbc.startTransaction(); //gb 10/05/07

/*gb 10/05/07 ******* La tabella 'setint' non � pi� in uso.
    try
      {
      dbc.startTransaction();
      // Cancella un record dalla tabella setint
      String myselect = "SELECT *" +
                        " FROM setint" +
                        " WHERE cod_interv = '" + cod_interv + "'" +
                        " AND cartella = " + cartella +
                        " AND n_contatto = " + contatto +
                        " AND ult_data = " + data;
      System.out.println("AcchandEJB-->delete()/setint: " + myselect);
      ISASRecord dbr_setint = dbc.readRecord(myselect);
      dbc.deleteRecord(dbr_setint);
      }
    catch (Exception ex)
      {
      dbc.rollbackTransaction();
      throw new SQLException("AcchandEJB: Errore eseguendo la delete in tabella setint - "+  ex);
      }
gb 10/05/07 *******/

    dbc.deleteRecord(dbr);
    dbc.commitTransaction();
    dbc.close();
    super.close(dbc);
    done=true;
    }
  catch(DBRecordChangedException e)
    {
    System.out.println(e);
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new DBRecordChangedException("AcchandEJB/delete: Errore eseguendo una rollback() - "+  e1);
      }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    System.out.println(e);
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new DBRecordChangedException("AcchandEJB/delete: Errore eseguendo la rollback() - "+  e1);
      }
    throw e;
    }
  catch(Exception e)
    {
    System.out.println(e);
    try
      {
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new DBRecordChangedException("AcchandEJB/delete: Errore eseguendo la rollback() - "+  e1);
      }
    throw new SQLException("AcchandEJB: Errore eseguendo la delete() - "+  e);
    }
  finally
    {
    if(!done)
      {
      try
        {
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {System.out.println(e2);}
      }
    }
  }
}

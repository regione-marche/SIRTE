package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//TODO
// allineato con quello di contributi mantenere allineamento
//23/11/2012 bargi
// 16/12/2005 - EJB di connessione alla procedura SINS Tabella Contrib
// Ilaria Mancini
//
// ==========================================================================

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.sinssnt.co_routine.*;
import it.pisa.caribel.sinssnt.calcoli.*;
import it.pisa.caribel.sinssnt.interventi.*;
import it.pisa.caribel.sinssnt.autoriz.*;
//import it.pisa.caribel.sins_ct.connection.*;
import it.pisa.caribel.sinssnt.connection.*;

public class ContribEJB extends SINSSNTConnectionEJB  {
	//public class ContribEJB extends SINS_CTConnectionEJB  {

	private static final String CODICI_SUSSIDIO_MONTEDOMINI = "'CFPROP','CFREAL','AFPROP','AFREAL','AFSOSP','CFSOSP'";
	
	/*** 19/03/09 x versione 08.05: 
		modificato FLAG x gestione stato SOSPENSIONE, cio� reinvio al livello sottostante:
		- non si utilizzano pi� "flag_invio" e "flag_accolto/i";
	 	- si utilizzano i nuovi "flag_livello" (livello raggiunto) e "flag_esito" (esito elaborazione).
	***/
	String nomeEJB="ContribEJB";
it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();

	AutorizzInterv autorInterv = new AutorizzInterv();

	private String MIONOME ="6-ContribEJB.";

public ContribEJB() {}
calcoloContoEconomico cEco = new calcoloContoEconomico();
private String selectConf(ISASConnection dbc)
throws SQLException
{
        String ret="N";
	try {

  		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ABILITAZ_CT'";
		ISASRecord dbConf = dbc.readRecord(mysel);
                if (dbConf!=null  && dbConf.get("conf_txt")!=null)
                            ret=(String)dbConf.get("conf_txt");
          	return ret;
        } catch (Exception ex) {
            throw new SQLException("ContribEJB.selectConf()-->ERRORE NEL REPERIMENTO CHIAVE DAL CONF"+ ex);
	}
}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws
SQLException, ISASPermissionDeniedException,CariException {
	boolean done=false;
	ISASConnection dbc=null;
	String cartella = "";
        String strNProgetto = "";
        String strCodObiettivo = "";
        String strNIntervento = "";
	String data = "";
        String messaggio="";
        ISASRecord dbr=null;
        ISASRecord dbrContatti=null;
        it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat  numUt = new it.pisa.caribel.util.NumberDateFormat();
        try{
        	dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                try{
			cartella = (String)h.get("n_cartella");
			strNProgetto = (String)h.get("n_progetto");
                        strCodObiettivo = (String)h.get("cod_obbiettivo");
                        strNIntervento = (String)h.get("n_intervento");
			data = (String)h.get("data");
                }catch(Exception ex){
                    throw new SQLException("ContribEJB.queryKey()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
                }
                dbr = caricaGriglia(dbc, h);
                /*
                Devo controllare nel caso di nuovo se la data apertura della scheda �
                compresa tra la data apertura e chiusura del contatto
                */
/*GB **************
               if (dbr==null){
                      String selectContatti="SELECT * FROM contatti WHERE n_cartella="+ cartella+
                                    " AND n_contatto="+ contatto;
                      dbrContatti=dbc.readRecord(selectContatti);
                      String dataScheda= data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
                      if (dbrContatti!=null)
                      {
                          //prendo la data apertura
                          String dataApertura="";
                          if (dbrContatti.get("data_contatto")!=null)
                          {
                                dataApertura=((java.sql.Date)dbrContatti.get("data_contatto")).toString();
                                dataApertura= dataApertura.substring(8,10)+"/"+dataApertura.substring(5,7)+"/"+dataApertura.substring(0,4);
                                //confronto le due date
                                int ret=numUt.dateCompare(dataScheda,dataApertura);
                                if (ret==1 || ret==0)
                                {
                                    //controllo se esiste la data chiusura e se � compresa
                                    String dataChiusura="";
                                    if (dbrContatti.get("data_chiusura")!=null){
                                      dataChiusura=((java.sql.Date)dbrContatti.get("data_chiusura")).toString();
                                      dataChiusura= dataChiusura.substring(8,10)+"/"+dataChiusura.substring(5,7)+"/"+dataChiusura.substring(0,4);
                                      ret=numUt.dateCompare(dataScheda,dataChiusura);
                                      if (ret==1)
                                          messaggio="La data variazione della scheda \n"+
                                                    "deve essere compresa tra la data apertura "+ dataApertura+
                                                    "\n e la data chiusura " + dataChiusura +" del contatto";

                                    }
                                }else{
                                    messaggio="La data variazione della scheda \n"+
                                              "deve essere maggiore o uguale della data apertura del contatto "+ dataApertura;
                                }
                            }
                      }
                }
*********/
                dbc.close();
		super.close(dbc);
		done=true;
/*GB **************
                if(!messaggio.equals(""))
                {
                   dbrContatti.put("risultato","KO");
                   throw new CariException(messaggio);
                }
**********/
                return dbr;
/*GB **************
        }catch(CariException ce){
                ce.setISASRecord(dbrContatti);
                System.out.println("AAAAAAAAAAAAAAA-->"+ dbrContatti.getHashtable().toString());
                throw ce;
**********/
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

public ISASRecord queryComAreadis(myLogin mylogin,Hashtable h) throws
SQLException
{
    boolean done=false;
    ISASConnection dbc=null;
    String cartella=null;
    String dataVariazione=null;
    it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

    try{
          dbc=super.logIn(mylogin);
          try{
                cartella=(String)h.get("n_cartella");
                dataVariazione=(String)h.get("data_var");
          }catch(Exception eChiave)
          {
               throw new SQLException("Errore eseguendo una ContribEJB.decodComAreaDis()."+
               "Manca la chiave primaria "+ eChiave);
          }
         String myselect= "SELECT a.citta,a.areadis "+
                                    "FROM anagra_c a,cartella WHERE "+
                                    "a.n_cartella="+ cartella+
                                    " AND a.data_variazione IN ";
          String parteIn="SELECT MAX(data_variazione) from anagra_c WHERE "+
			 "anagra_c.n_cartella=a.n_cartella ";
          String where= " AND data_variazione<="+formatDate(dbc,dataVariazione);
          //cerco per primo il comune e l'area distrettuale relativo al periodo della scheda
          String select=myselect + "(" + parteIn + where +")";
          ISASRecord dbr=dbc.readRecord(select);
          if (dbr==null){
              //non l'ho trovato relativo al periodo della scheda , cerco il massimo in assoluto
              select=myselect + "(" + parteIn +")";
              dbr=dbc.readRecord(select);
          }
          if( dbr!=null){
              dbr.put("cod_comune", (String)util.getObjectField(dbr,"citta",'S'));
              dbr.put("des_comune",util.getDecode(dbc,"comuni","codice",
                            (String)util.getObjectField(dbr,"citta",'S'),"descrizione"));
              dbr.put("cod_areadis", (String)util.getObjectField(dbr,"areadis",'S'));
              dbr.put("desc_areadis",util.getDecode(dbc,"areadis","codice",
                        (String)util.getObjectField(dbr,"cod_areadis",'S'),"descrizione"));
          }
          dbc.close();
          super.close(dbc);
          done=true;
          return dbr;
    }catch(Exception e){
               throw new SQLException("Errore eseguendo una ContribEJB.decodComAreaDis() "+ e);
     }finally{
          if(!done){
              try{
                      dbc.close();
                      super.close(dbc);
              }catch(Exception e1){System.out.println("Errore eseguendo una ContribEJB.decodComAreaDis()" +e1);}
          }
      }
}

//gb 15/02/08 *******
public String query_getCodAreadisAssist(myLogin mylogin, Hashtable h) throws SQLException
{
    boolean done=false;
    ISASConnection dbc=null;
    String strNCartella = null;
    String strDtVariaz = null;
    String strAreaDis = "";

  try{
	strNCartella = (String)h.get("n_cartella");
	strDtVariaz = (String)h.get("data_var");
	dbc = super.logIn(mylogin);

        String myselect = "SELECT a.areadis"+
			  " FROM anagra_c a"+
			  " WHERE a.n_cartella = " + strNCartella +
			  " AND a.data_variazione IN (" +
			  " SELECT MAX(data_variazione)" +
			  " FROM anagra_c" +
			  " WHERE anagra_c.n_cartella = a.n_cartella" +
			  " AND data_variazione <= " + formatDate(dbc, strDtVariaz) + ")";
	System.out.println("ContribEJB/query_getCodAreadisAssist: " + myselect);
	ISASRecord dbr = dbc.readRecord(myselect);
	if( (dbr!=null) && (dbr.get("areadis") != null))
	{
	  strAreaDis = (String)dbr.get("areadis");
	}

	dbc.close();
	super.close(dbc);
	done = true;
	return strAreaDis;
    }catch(Exception e){
      System.out.println("ContribEJB: query_getCodAreadisAssist - Eccezione= " + e);
      throw new SQLException("Errore eseguendo una query()  ");
    }finally{
      if(!done){
        try{
          dbc.close();
          super.close(dbc);
        }catch(Exception e1){
          System.out.println("ContribEJB: query_getCodAreadisAssist - Eccezione nella chiusura della connessione= " + e1);
        }
      }
    }
}
//gb 15/02/08: fine *******

//gb 15/02/08 *******
public Vector query_loadCmbBoxAreeDistr(myLogin mylogin, Hashtable h) throws SQLException
{
    boolean done=false;
    ISASConnection dbc=null;
    try
      {
        dbc=super.logIn(mylogin);
		
        String myselect="SELECT * FROM areadis";
		
		// 17/05/10 m. ---------
		String distrOper = (String)h.get("distr_operatore");
		if ((distrOper != null) && (!distrOper.trim().equals("")))
			myselect += " WHERE cod_distretto = '" + distrOper + "'"; 
		// 17/05/10 m. ---------
		
        System.out.println("ContribEJB/query_loadCmbBoxAreeDistr: " + myselect);

        ISASCursor dbcur=dbc.startCursor(myselect);
        System.out.println("ContribEJB/query_loadCmbBoxAreeDistr: Fatta la startcursor");
        Vector vdbr=dbcur.getAllRecord();
        System.out.println("ContribEJB/query_loadCmbBoxAreeDistr: Creato il vettore");
        dbcur.close();
        dbc.close();
        super.close(dbc);
        done=true;
        return vdbr;
      }catch(Exception e){
        e.printStackTrace();
        throw new SQLException("!!Errore eseguendo la query_loadCmbBoxAreeDistr()  ");
      }
      finally
      {
        if(!done)
          {
          try
            {
            dbc.close();
            super.close(dbc);
            }catch(Exception e1){System.out.println(e1);}
          }
      }
}
//gb 15/02/08: fine *******

/*gb 17.12.08
//gb 15/02/08 *******
public Vector query_loadCmbBoxCentriCosto(myLogin mylogin, Hashtable h) throws SQLException
  {
    boolean done=false;
    ISASConnection dbc=null;
    String strCodOperatore = (String) h.get("operatore");
    String strCodRegione = (String) h.get("codreg");
    String strCodAzSanitaria = (String) h.get("codazsan");
    Vector vdbr = new Vector();
    try
      {
        dbc=super.logIn(mylogin);
        String myselect = "SELECT *" +
			  " FROM operatori" +
			  " WHERE codice = '" + strCodOperatore + "'";
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto(1) : " + myselect);
	ISASRecord dbrOp = dbc.readRecord(myselect);
	if ((dbrOp == null) || (dbrOp.get("cod_presidio") == null) || ((String)dbrOp.get("cod_presidio")).trim().equals(""))
	  return vdbr;
	String strCodPresidio = (String)dbrOp.get("cod_presidio");

        myselect = "SELECT *" +
		   " FROM presidi" +
		   " WHERE codreg = '" + strCodRegione + "'" +
		   " AND codazsan = '" + strCodAzSanitaria + "'" +
		   " AND codpres = '" + strCodPresidio + "'";
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto(2) : " + myselect);
	ISASRecord dbrPres = dbc.readRecord(myselect);
	if ((dbrPres == null) || (dbrPres.get("coddistr") == null) || ((String)dbrPres.get("coddistr")).trim().equals(""))
	  return vdbr;
	String strCodDistretto = (String)dbrPres.get("coddistr");

        myselect = "SELECT *" +
		   " FROM co_comuni" +
		   " WHERE cod_distretto = '" + strCodDistretto + "'";
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto(3) : " + myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto: Fatta la startcursor");
        vdbr=dbcur.getAllRecord();
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto: Creato il vettore");
        dbcur.close();
        dbc.close();
        super.close(dbc);
        done=true;
        return vdbr;
      }catch(Exception e){
        e.printStackTrace();
        throw new SQLException("!!Errore eseguendo la query_loadCmbBoxCentriCosto()  ");
      }
      finally
      {
        if(!done)
          {
          try
            {
            dbc.close();
            super.close(dbc);
            }catch(Exception e1){System.out.println(e1);}
          }
      }
  }
//gb 15/02/08: fine *******
*gb 17.12.08 */

//gb 17.12.08
public Vector query_loadCmbBoxCentriCosto(myLogin mylogin, Hashtable h) throws SQLException
  {
    boolean done=false;
    ISASConnection dbc=null;
    Vector vdbr = new Vector();
    try
      {
        dbc=super.logIn(mylogin);

        String myselect = "SELECT *" +
		   " FROM co_comuni";
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto(3) : " + myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto: Fatta la startcursor");
        vdbr=dbcur.getAllRecord();
        System.out.println("ContribEJB/query_loadCmbBoxCentriCosto: Creato il vettore");
        dbcur.close();
        dbc.close();
        super.close(dbc);
        done=true;
        return vdbr;
      }catch(Exception e){
        e.printStackTrace();
        throw new SQLException("!!Errore eseguendo la query_loadCmbBoxCentriCosto()  ");
      }
      finally
      {
        if(!done)
          {
          try
            {
            dbc.close();
            super.close(dbc);
            }catch(Exception e1){System.out.println(e1);}
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


/* 06.03.12 */
public Vector query_loadCmbBoxCCDistretto(myLogin mylogin, Hashtable h) throws SQLException
{
	String punto = MIONOME  + "query_loadCmbBoxCCDistretto  ";
	System.out.println(punto + "dati che ricevo>"+ h+"<\n");
  boolean done=false;
  ISASConnection dbc=null;
  Vector vdbr = new Vector();
  try
    {
      dbc=super.logIn(mylogin);
      String distretto = ISASUtil.getValoreStringa(h, "distretto");
      String myselect = " SELECT * FROM co_comuni ";
      if (ISASUtil.valida(distretto)){
    	  myselect+= " where cod_distretto = '" +distretto+"' ";
      }
      System.out.println(punto + "Query: " + myselect);
      ISASCursor dbcur=dbc.startCursor(myselect);
      vdbr=dbcur.getAllRecord();
      System.out.println(punto +" dati recuperati>"+(vdbr !=null ? vdbr.size()+"":" non ci dati") );
      dbcur.close();
      dbc.close();
      super.close(dbc);
      done=true;
      return vdbr;
    }catch(Exception e){
      e.printStackTrace();
      throw new SQLException("!!Errore eseguendo la query_loadCmbBoxCCDistretto() ");
    }
    finally
    {
      if(!done)
        {
        try
          {
          dbc.close();
          super.close(dbc);
          }catch(Exception e1){System.out.println(e1);}
        }
    }
}


//gb 31/01/08 *******
private void decodRecord(ISASConnection dbc, ISASRecord dbrec) throws Exception
{
        it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
    try	{
    	getDecodifiche(dbc,dbrec);
	//beneficiario
	dbrec.put("b_cognome",util.getDecode(dbc,"beneficiari","b_codice",
                                  (String)util.getObjectField(dbrec,"codice_benef",'I'),"b_cognome"));
	dbrec.put("b_nome",util.getDecode(dbc,"beneficiari","b_codice",
                                  (String)util.getObjectField(dbrec,"codice_benef",'I'),"b_nome"));
	//tipo contributo
	dbrec.put("tipo",util.getDecode(dbc,"tipocontr","tc_codice",
                                  (String)util.getObjectField(dbrec,"tipo_contrib",'S'),"tc_descri"));
	dbrec.put("contributo",util.getDecode(dbc,"sussidi","codice_suss",
                                  (String)util.getObjectField(dbrec,"cod_contrib",'S'),"descrizione_suss"));
	 dbrec.put("flag_grad",util.getDecode(dbc,"sussidi","codice_suss",
             (String)util.getObjectField(dbrec,"cod_contrib",'S'),"flag_grad"));
	//comune
	dbrec.put("desc_comune",util.getDecode(dbc,"comuni","codice",
                                    (String)util.getObjectField(dbrec,"cod_comune_auto",'S'),"descrizione"));
	//areadis
	dbrec.put("desc_areadis",util.getDecode(dbc,"areadis","codice",
                                  (String)util.getObjectField(dbrec,"cod_areadis",'S'),"descrizione"));
	//operatore
	dbrec.put("desc_oper",util.getDecode(dbc,"operatori","codice",
                                    (String)util.getObjectField(dbrec,"operatore",'S'),
                                    "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","desc_oper"));
	//operatore auto1
	dbrec.put("desc_oper_auto1",util.getDecode(dbc,"operatori","codice",
                                    (String)util.getObjectField(dbrec,"oper_auto1",'S'),
                                    "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","desc_oper"));
	
	//operatore auto2
	dbrec.put("desc_oper_auto2",util.getDecode(dbc,"operatori","codice",
                                    (String)util.getObjectField(dbrec,"oper_auto2",'S'),
                                    "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","desc_oper"));
/** 23/03/09 m.
	String strFlagInvio = getStringField(dbrec, "flag_invio");
	String strFlagAccolti = getStringField(dbrec, "flag_accolti");
	if (strFlagInvio.equals("S") || strFlagAccolti.equals("S"))
	  dbrec.put("contrib_autorizzato", "S");
	else
	  dbrec.put("contrib_autorizzato", "N");
**/
		// 23/03/09 m.
		String strFlagEsito = getStringField(dbrec, "flag_esito");
		String strFlagLivello = getStringField(dbrec, "flag_livello");
		if (strFlagLivello.equals(""+autorInterv.LIV_CONC) && strFlagEsito.equals(""+autorInterv.ESI_CONC))
			dbrec.put("contrib_autorizzato", "S");
		else
		  	dbrec.put("contrib_autorizzato", "N");
	}
    catch(Exception e)
	{
		System.out.println("ContribEJB.decodRecord(): "+e);
		throw new SQLException("Errore eseguendo la decodRecord()");
	} 
}
//gb 31/01/08: fine *******


private String getStringField (ISASRecord dbr, String fldName)
  {
	try {
		String str = "";
		if (dbr.get(fldName) != null)
		  str = (String) dbr.get(fldName);
		return str;
	}
	catch (Exception ex)
	{
		return "";
	}
  }
public ISASRecord decod_ccosto(myLogin mylogin,Hashtable h)
throws  SQLException, ISASPermissionDeniedException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM co_comuni WHERE "+
			        "codice='"+(String)h.get("codice")+"'";
		System.out.println("decod_ccosto su Centri di costo: "+myselect);
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
		throw new SQLException("Errore eseguendo una decod_ccosto()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

//gb 31/01/08: Modificata per prendere in considerazione un solo record per data.
public ISASRecord queryContrib(myLogin mylogin,Hashtable h) throws
SQLException, ISASPermissionDeniedException,CariException {
//richiamata quando vado in apri di un dettaglio
	boolean done=false;
	ISASConnection dbc=null;
        ISASRecord dbrec=null;
        String strNProgetto = null;
        String strCodObiettivo = null;
        String strNIntervento = null;
	String cartella = null;
	String data = null;
//gb 31/01/08        String progressivo =null;
        String messaggio="";
	try{
		dbc=super.logIn(mylogin);
                ServerUtility su =new ServerUtility();
                try{
			cartella = (String)h.get("n_cartella");
			strNProgetto = (String)h.get("n_progetto");
                        strCodObiettivo = (String)h.get("cod_obbiettivo");
                        strNIntervento = (String)h.get("n_intervento");
			data = (String)h.get("data");
//gb 31/01/08			progressivo = (String)h.get("progressivo");
                }catch(Exception ex){
                     throw new SQLException("ContribEJB.queryContrib()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
                }

		String myselect = "SELECT *" +
                                  " FROM ass_contrib" +
                                  " WHERE n_cartella = " + cartella +
                                  " AND n_progetto = " + strNProgetto +
                                  " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                                  " AND n_intervento = " + strNIntervento +
                                  " AND data = " + formatDate(dbc,data);
//gb 31/01/08                                  " AND progressivo = " + progressivo;
                System.out.println("ContribEJB/queryContrib: " + myselect);

                dbrec= dbc.readRecord(myselect);
                if (dbrec!=null){
		  decodRecord(dbc, dbrec);
                }
//gb 31/01/08		else
//gb 31/01/08                      messaggio="Operazione fallita.\n"+
//gb 31/01/08                                "I dati richiesti sono stati rimossi da un altro utente";
//gb 31/01/08                if(!messaggio.equals(""))
//gb 31/01/08                   throw new CariException(messaggio);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbrec;
        }
        catch(CariException ce)
        {
                ce.setISASRecord(dbrec);
                throw ce;
	}catch(ISASPermissionDeniedException e){
		System.out.println("queryContrib: eccezione permesso negato "+e);
		//gb 21/02/08 return null;
		//gb  21/02/08: messe le triplette dei permessi sulla tabella ass_contrib
		//		e quindi devo mandare una messaggio e una eccezione al client
		//		se l'operatore non ha i diritti di lettura record.
		throw new CariException("Attenzione: Mancano i permessi per leggere il record dal database!", -2);
	}catch(Exception e){
		throw new SQLException("Errore eseguendo una ContribEJB.queryContrib() " +e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}
public String updateContribBenefic(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException ,CariException{
	boolean done=false;
	ISASConnection dbc=null;
	String strNProgetto = null;
	String strCodObiettivo = null;
	String strNIntervento = null;
	String cartella = null;
	String data = null;
	String strAbilitazCt = "";
	try{
		cartella = (String)h.get("n_cartella");
		strNProgetto = (String)h.get("n_progetto");
		strCodObiettivo = (String)h.get("cod_obbiettivo");
		strNIntervento = (String)h.get("n_intervento");
		data = (String)h.get("data");
		strAbilitazCt = (String)h.get("abilitaz_ct");
	}catch(Exception ex){
		throw new SQLException("ContribEJB.updateContribBenefic()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
	}
	try{
		String    	strCodOperatore = (String)h.get("cod_operatore");
		dbc=super.logIn(mylogin);
		dbc.startTransaction();
		String myselect = "SELECT * " +
		" FROM ass_contrib" +
		" WHERE n_cartella = " + cartella +
		" AND n_progetto = " + strNProgetto +
		" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
		" AND n_intervento = " + strNIntervento +
		" AND data = " + formatDate(dbc,data);
		System.out.println("ContribEJB/updateContribBenefic: " + myselect);
		ISASRecord dbrOld = dbc.readRecord(myselect);
		if (dbrOld!=null)
		{
			int auto=1;
             if(dbrOld.get("flag_livello")!=null)
		     auto=Integer.parseInt((String)dbrOld.get("flag_livello"));
		     dbrOld.put("codice_benef", h.get("codice_benef"));
		     if (h.containsKey("prog_piano"))
		     dbrOld.put("co_prog_piano", h.get("prog_piano"));
		     System.out.println("RECORD= "+dbrOld.getHashtable().toString());
			if ((strAbilitazCt != null) && (strAbilitazCt.equals("SI")))
			{
				System.out.println("RECORD= ho CONTRIBUTI");
				if (dbrOld.get("parere_comm2")!=null && ((String)dbrOld.get("parere_comm2")).equals("1")
				&&auto == autorInterv.LIV_CONC) {
					System.out.println("RECORD aggiorno co_fffiss solo beneficiario e eventuale piano");
		    		corou.updateContribCoFfiss(dbc, dbrOld, strCodOperatore);
				}
			}		
			
			dbc.writeRecord(dbrOld);	 
		}

		dbc.commitTransaction();
		dbc.close();
		super.close(dbc);
		done=true;
		return "OK";
	}catch(DBRecordChangedException e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.updateContrib().rollback() - "+e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.updateContrib().rollback()- "+e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.updateContrib().rollback()- "+  e);
		}
		throw new SQLException("ContribEJB.updateContrib()-->"+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}
public ISASRecord updateContrib(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException ,CariException{
        boolean done=false;
        ISASConnection dbc=null;
        String strNProgetto = null;
        String strCodObiettivo = null;
        String strNIntervento = null;
	String cartella = null;
	String data = null;
	String strTipoUpdate = "";
	String strAbilitazCt = "";

 
//gb 31/01/08        String progressivo =null;
//La griglia non esiste pi�
        String messaggio="";
//GB    ServerUtility su =new ServerUtility();
        try{
                cartella = (String)dbr.get("n_cartella");
                strNProgetto = (String)dbr.get("n_progetto");
                strCodObiettivo = (String)dbr.get("cod_obbiettivo");
                strNIntervento = (String)dbr.get("n_intervento");
                data = (String)dbr.get("data");
		strAbilitazCt = (String)dbr.get("abilitaz_ct");
		strTipoUpdate = (String)dbr.get("tipo_update");
//gb 31/01/08                progressivo = (String)dbr.get("progressivo");
//La griglia non esiste pi�
        }catch(Exception ex){
            throw new SQLException("ContribEJB.updateContrib()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
        }
        try{
                dbc=super.logIn(mylogin);
                dbc.startTransaction();
                /*devo rileggere il record e aggiornare il conto economico
                Mi pu� essere cambiato anche il conto economico non solo le date
                o l'importo
                */
		String myselect = "SELECT *" +
                                  " FROM ass_contrib" +
                                  " WHERE n_cartella = " + cartella +
                                  " AND n_progetto = " + strNProgetto +
                                  " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                                  " AND n_intervento = " + strNIntervento +
                                  " AND data = " + formatDate(dbc,data);
//gb 31/01/08                                  " AND progressivo = " + progressivo;
//La griglia non esiste pi�
                System.out.println("ContribEJB/updateContrib: " + myselect);
		ISASRecord dbrOld = dbc.readRecord(myselect);
	/*	if (strTipoUpdate.equals("10")) // 10 == Update fittizio: equivalente a insert
		{
		//Il Pannello proposta pu� essere abilitato una sola volta
		//quindi non c'� da sottrarre nulla.
		  //Aggiorno il nuovo conto Economico con l'impegnato
		  //metto i flag ad N
			// 23/03/09 m
		  	dbr.put("flag_livello", ""+autorInterv.LIV_AUT1);
		  	dbr.put("flag_esito", ""+autorInterv.ESI_CONC);
		}*/
	//	else //Update non fittizio: equivalente a update
	//	{
		  if (dbrOld!=null)
		  {
			  if ((strAbilitazCt != null) && (strAbilitazCt.equals("SI")))
			  {
					//Devo fare l'aggiornamento dei conti economici (sottraggo l'attuale)			
					//if (strParere != null)
						messaggio += cEco.updateContribContoEcon(-1, dbc, dbrOld);
						debugMessage(nomeEJB+" stornato importo");
				}
			  
		  /*scrivo i campi che non mi arrivano dal client e che non possono essere modificati
		  da questa frame
		  */
/** 23/03/09 m
		    dbr.put("flag_invio",dbrOld.get("flag_invio"));
		    dbr.put("flag_accolti",dbrOld.get("flag_accolti"));
**/
			// 16/10/09  --
			dbr.put("flag_livello", dbrOld.get("flag_livello"));
			dbr.put("flag_esito", dbrOld.get("flag_esito"));
			// 16/10/09 --
			
		    dbr.put("anno_verbale",dbrOld.get("anno_verbale"));
		    dbr.put("num_verbale",dbrOld.get("num_verbale"));
		   
		  //gb 04/06/08 updateContribSettaFlags(dbr);
		  //scrivo il record
			 if ((strAbilitazCt != null) && (strAbilitazCt.equals("SI")))
			  {
					//Devo fare l'aggiornamento dei conti economici (sottraggo l'attuale)			
					//if (strParere != null)
						messaggio += cEco.updateContribContoEcon(1, dbc, dbr);
						debugMessage(nomeEJB+" dopo storno impegno di nuovo importo");
				}
		  dbc.writeRecord(dbr);


	/*bargi 22/10/2012
	 * 	  if ((strAbilitazCt != null) && (strAbilitazCt.equals("SI")))
		  {
                    //devo sottrarre dal vecchio conto economico l'importo impegnato
                    if (dbrOld.get("importo_proposto")!=null)
                    {     String dataInizio=((java.sql.Date)(dbrOld.get("data_inizio_prop"))).toString();
                          String dataFine=((java.sql.Date)(dbrOld.get("data_fine_prop"))).toString();
                          messaggio=AggiornamentoContoEconomico(dbc,dbrOld,-Double.parseDouble(""+dbrOld.get("importo_proposto")),
                                                      dataInizio,dataFine);
                          //in questo caso il messaggio dovrebbe essere vuoto
                    }
                    System.out.println("ContribEJB.updateContrib 000");
                    //Aggiorno il nuovo conto Economico con l'impegnato
                    if (dbr.get("importo_proposto")!=null)
                    {     String dataInizio=""+(dbr.get("data_inizio_prop"));
                          String dataFine=""+(dbr.get("data_fine_prop"));
                          messaggio=messaggio+AggiornamentoContoEconomico(dbc,dbr,Double.parseDouble(""+dbr.get("importo_proposto")),
                                                        dataInizio,dataFine);
                    }
		  }
		  */

		 

//gb 31/01/08 *******
		  myselect = "SELECT *" +
			" FROM ass_contrib" +
			" WHERE n_cartella = " + cartella +
			" AND n_progetto = " + strNProgetto +
			" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
			" AND n_intervento = " + strNIntervento +
			" AND data = " + formatDate(dbc,data);
		  System.out.println("ContribEJB/updateContrib(Rileggo): " + myselect);
		
		  dbr= dbc.readRecord(myselect);
		  if (dbr!=null)
		  {
		    decodRecord(dbc, dbr);
		    debugMessage(nomeEJB+" decodRecord called");
		  }
//gb 31/01/08: fine *******
		}
		else
		{
                  messaggio="Operazione fallita.\n"+
                                "I dati richiesti sono stati rimossi da un altro utente";
		}
       updateTestata(dbc, dbr);
	    debugMessage(nomeEJB+" updateTestata called");
                dbc.commitTransaction();
                dbc.close();
                super.close(dbc);
                done=true;
                if(!messaggio.equals(""))
                   throw new CariException(messaggio, -2);
                return dbr;
        }catch(CariException ce){
      		System.out.println("ContribEJB.ISAS()-->"+ dbr.getHashtable().toString());
                ce.setISASRecord(dbr);
                throw ce;
	}catch(DBRecordChangedException e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.updateContrib().rollback() - "+e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.updateContrib().rollback()- "+e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.updateContrib().rollback()- "+  e);
		}
		throw new SQLException("ContribEJB.updateContrib()-->"+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

/*
private void updateContribSettaFlags(ISASRecord dbr)
	throws SQLException
  {
    try{
	String strParereAuto1 = (String) dbr.get("parere_comm");
	if ((strParereAuto1 != null) && (strParereAuto1.equals("1")))
	{
	  dbr.put("flag_invio","S");
	}
	else if ((strParereAuto1 != null) && (strParereAuto1.equals("2")))
	{
	  dbr.put("flag_invio","N");
	}

	String strParereAuto2 = (String) dbr.get("parere_comm2");
	if ((strParereAuto2 != null) && (strParereAuto2.equals("1"))
	{
	  dbr.put("flag_accolti","S");
	}
	else if ((strParereAuto2 != null) && (strParereAuto2.equals("2"))
	{
	  dbr.put("flag_accolti","N");
	}
    }catch (Exception ex)
    {
         throw new SQLException("Errore eseguendo una ContribEJB.updateContribSettaFlags()- "+ex);
    }
  }
*/


/*
private void updateContribCoFfiss(ISASConnection dbc, ISASRecord dbr, String strCodOperatore)
	throws SQLException
  {
    try{
	ServerUtility su =new ServerUtility();
	//Controllo se � stato accolto
	if (dbr.get("parere_comm2")!=null && ((String)dbr.get("parere_comm2")).equals("1"))
	{   //accolto.
	  //scrivo su co_ffiss
	      ISASRecord dbrFissi = dbc.newRecord("co_ffiss");
	      dbrFissi.put("n_cartella",dbr.get("n_cartella"));
	      dbrFissi.put("cod_contrib",dbr.get("cod_contrib"));
	      String myselprog="";
	      int progressivo=1;
	      myselprog=su.addWhere(myselprog,su.REL_AND,"n_cartella",su.OP_EQ_NUM,""+dbr.get("n_cartella"));
	      myselprog=su.addWhere(myselprog,su.REL_AND,"cod_contrib",su.OP_EQ_STR,""+dbr.get("cod_contrib"));
	      String select ="SELECT MAX(progressivo) massimo FROM co_ffiss WHERE " + myselprog;
	      ISASRecord dbrmax= dbc.readRecord(select);
	      if(dbrmax!=null  && dbrmax.get ("massimo")!=null)
	          progressivo=((Integer)dbrmax.get ("massimo")).intValue()+1;
	      dbrFissi.put("progressivo",new Integer(""+progressivo));
	      dbrFissi.put("co_tipcontr",dbr.get("tipo_contrib"));
	      //dbrFissi.put("co_impo",(Double)dbr.get("importo_comm2"));
	      dbrFissi.put("co_impo", dbr.get("importo_comm2"));
	      //nuovi campi bargi 30/10/2012 
	      dbrFissi.put("co_impo_ass", dbr.get("importo_comm2_ass"));
	      dbrFissi.put("co_prog_piano",dbr.get("co_prog_piano"));
	      dbrFissi.put("co_unimis",dbr.get("ass_unimis"));
	      dbrFissi.put("co_freq",dbr.get("int_freq"));
	      //se sono qui la data autorizzazione � piena non testo se � nulla
	      if (dbr.get("data_fine_comm2") != null)
	      {
	      	//String data = ((java.sql.Date)dbr.get("data_fine_comm2")).toString();
	      	String data = "" + dbr.get("data_fine_comm2");
	      	if(data.length()==10){
	              dbrFissi.put("co_anno_scad",data.substring(0,4));
	              dbrFissi.put("co_mese_scad",data.substring(5,7));
	      	}
	      }
	      //per tutti deve essere settato il co_flag ad S indipendentemente dal valore di tc_flag
	      dbrFissi.put("co_flag","S");
	      dbrFissi.put("co_oper_soc",(String)dbr.get("operatore"));
	      //dbrFissi.put("co_bene",(Integer)dbr.get("codice_benef"));
	      dbrFissi.put("co_bene",dbr.get("codice_benef"));
	      dbrFissi.put("co_comu_soc",(String)dbr.get("cod_comune_auto"));
	      dbrFissi.put("co_dist_soc",(String)dbr.get("cod_areadis"));
	      //se sono qui la data autorizzazione � piena non testo se � nulla
	      if (dbr.get("data_inizio_comm2") != null)
	      {
	      	//String data_ini = ((java.sql.Date)dbr.get("data_inizio_comm2")).toString();
	      	String data_ini = "" + dbr.get("data_inizio_comm2");
	      	if(data_ini.length()==10){
	              dbrFissi.put("co_anno_ini",data_ini.substring(0,4));
	              dbrFissi.put("co_mese_ini",data_ini.substring(5,7));
	      	}
	      }
	      dbrFissi.put("co_data",dbr.get("data"));
//	      dbrFissi.put("co_contatto",dbr.get("n_contatto"));
	      dbrFissi.put("co_contatto",dbr.get("n_progetto"));
	      dbrFissi.put("co_prog",dbr.get("progressivo"));
	      dbrFissi.put("co_oper", strCodOperatore);
	      dbc.writeRecord(dbrFissi);
	}
    }catch (Exception ex)
    {
         throw new SQLException("Errore eseguendo una ContribEJB.updateContribCoFfiss()- "+ex);
    }
  }
*/
private String AggiornamentoContoEconomico(ISASConnection dbc,ISASRecord dbrContrib,double importo,
                                            String dataInizio,String dataFine)
throws SQLException
{
      String messaggio="";
      try{
//gb 11/07/08:  Si toglie il controllo dalla chiave ABILITAZ_CT = SI/NO perch�
//		� gi� stato fatto in updateContrib con la variabile proveniente
//		dal client prima di chiamare il presente metodo. (Da insertDbContrib
//		e da deleteContrib non ci si arriva pi� perch� tali metodi non vengono
//		pi� chiamati).
//gb 11/07/08            String ret=selectConf(dbc);
//gb 11/07/08            if (ret.equals("SI"))
//gb 11/07/08            {
                calcoloContoEconomico cEco=new calcoloContoEconomico();
                messaggio=cEco.AggiornaContoEconomico(dbc,dbrContrib,importo,"I",dataInizio,dataFine);
//gb 11/07/08            }
      return messaggio;
      }catch (Exception ex)
      {
ex.printStackTrace();
         	throw new SQLException("Errore eseguendo una ContribEJB.AggiornamentoContoEconomico()- "+ex);
      }

}

public ISASRecord insertContrib(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException{
        boolean done=false;
        ISASConnection dbc=null;
        String strNProgetto = null;
        String strCodObiettivo = null;
        String strNIntervento = null;
	String cartella = null;
	String data = null;
        String messaggio="";
        ISASRecord dbr=null;
        ServerUtility su =new ServerUtility();
        try{
                cartella = (String)h.get("n_cartella");
                strNProgetto = (String)h.get("n_progetto");
                strCodObiettivo = (String)h.get("cod_obbiettivo");
                strNIntervento = (String)h.get("n_intervento");
                 data = (String) h.get("data");
        }catch(Exception ex){
            throw new SQLException("ContribEJB.insertContrib()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
        }
        try{
                dbc=super.logIn(mylogin);
                dbc.startTransaction();
                System.out.println("ContribEJB/insertContrib");
                messaggio=insertDbContrib(dbc,h);
//gb 31/01/08                dbr=caricaGriglia(dbc,h);
//La griglia non esiste pi�
//gb 31/01/08 *******
		String myselect = "SELECT *" +
				" FROM ass_contrib" +
				" WHERE n_cartella = " + cartella +
				" AND n_progetto = " + strNProgetto +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
				" AND n_intervento = " + strNIntervento +
				" AND data = " + formatDate(dbc,data);
		System.out.println("ContribEJB/insertContrib(Rileggo): " + myselect);

		dbr = dbc.readRecord(myselect);
		if (dbr!=null)
		{
		  decodRecord(dbc, dbr);
		}
//gb 31/01/08: fine *******

                dbc.commitTransaction();
                dbc.close();
                super.close(dbc);
                if(!messaggio.equals(""))
                   throw new CariException(messaggio, -2);
                return dbr;
        }catch(CariException ce)
        {
                ce.setISASRecord(dbr);
                throw ce;
	}catch(DBRecordChangedException e){
		System.out.println("ContribEJB.insertContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.insertContrib().rollback() - "+e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.insertContrib().rollback()- "+e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("ContribEJB.insertContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.insertContrib().rollback()- "+  e);
		}
		throw new SQLException("ContribEJB.insertContrib()-->"+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}
private String insertDbContrib(ISASConnection dbc,Hashtable h) throws Exception
  {
    int progressivo=1;
    String messaggio="";
    try{
          ISASRecord dbr=dbc.newRecord("ass_contrib");
//vado a calcolarmi il progressivo come massimo+1
          //non testo che siano nulli perch� lo fa la funzione chiamante
          ServerUtility su =new ServerUtility();
          String cartella = (String)h.get("n_cartella");
          String strNProgetto = (String)h.get("n_progetto");
          String strCodObiettivo = (String)h.get("cod_obbiettivo");
          String strNIntervento = (String)h.get("n_intervento");
          String data = (String) h.get("data");
          String select = "SELECT MAX(progressivo) massimo" +
                          " FROM ass_contrib" +
                          " WHERE n_cartella = " + cartella +
                          " AND n_progetto = " + strNProgetto +
                          " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                          " AND n_intervento = " + strNIntervento +
                          " AND data = " + formatDate(dbc,data);
          System.out.println("ContribEJB/insertDbContrib: " + select);
          ISASRecord dbrmax= dbc.readRecord(select);
          System.out.println("ContribEJB/insertDbContrib: Dopo la dbc.readRecord(select)");
          if(dbrmax!=null  && dbrmax.get ("massimo")!=null)
              progressivo=((Integer)dbrmax.get ("massimo")).intValue()+1;
          Enumeration n=h.keys();
          while(n.hasMoreElements()){
                  String e=(String)n.nextElement();
                  dbr.put(e,h.get(e));
          }
          //inserisco il progressivo
          dbr.put("progressivo",new Integer (progressivo));
          //metto i flag ad N
/** 23/03/09 m
          dbr.put("flag_invio","N");
          dbr.put("flag_accolti","N");
**/
			// 23/03/09 m
		  	dbr.put("flag_livello", ""+autorInterv.LIV_AUT1);
		  	dbr.put("flag_esito", ""+autorInterv.ESI_CONC);

	  System.out.println("ContribEJB/insertDbContrib/Contenuto dbr:" + dbr.getHashtable().toString());
          dbc.writeRecord(dbr);
          String dataInizio=""+dbr.get("data_inizio_prop");
          String dataFine=""+dbr.get("data_fine_prop");
          messaggio=AggiornamentoContoEconomico(dbc,dbr,Double.parseDouble(""+dbr.get("importo_proposto")),
                                        dataInizio,dataFine);
          return messaggio;
    } catch (Exception ex){
          throw new SQLException("Errore eseguendo una ContribEJB.insertDbContrib()- "+ex);
    }
  }

//gb 13.02.09
public Hashtable insertContribEtTestata(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
        boolean done=false;
        ISASConnection dbc=null;
	String cartella = "";
        String strNProgetto = "";
        String strCodObiettivo = "";
        String strNIntervento = "";
	String data = "";
        String messaggio="";
	String strAbilitazCt = (String)h.get("abilitaz_ct");
	Hashtable htRet = new Hashtable();
        ServerUtility su =new ServerUtility();
        try{
                cartella = (String)h.get("n_cartella");
                strNProgetto = (String)h.get("n_progetto");
                strCodObiettivo = (String)h.get("cod_obbiettivo");
                data = (String) h.get("data");
        }catch(Exception ex){
            throw new SQLException("ContribEJB.insertContribEtTestata()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
        }
        try{
		System.out.println("ContribEJB/insertContribEtTestata/Hashtable input:" + h.toString());
                dbc=super.logIn(mylogin);
                String codice_suss=h.get("codice_suss").toString();
                //modifica richiesta da firenze
                if (checkContribGrad(dbc,codice_suss))//NO bargi 27/06/2014 dbc.getUserProfile().canIUse("MONTEDOM","CONS"))
            	messaggio = esisteContributoMDomini(dbc,h);
        		if (!messaggio.equals("")){
        			htRet.put("messaggio", messaggio);
        			dbc.close();
                    super.close(dbc);
                    done = true;
                    return htRet;			
        		}
                
                dbc.startTransaction();
                System.out.println("ContribEJB/insertContribEtTestata");

		int intProgressivo = writeTestata(dbc, (Hashtable)h.get("testata"));

	
		
		
		
		ISASRecord dbr=dbc.newRecord("ass_contrib");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
		  String e=(String)n.nextElement();
		  if (!e.equals("testata")) //si mettono solo i campi del dettaglio,
					    //alla chiave 'testata' corrisponde la 
					    //la hashtable della testata.
		    dbr.put(e,h.get(e));
		}
		//inserisco n_intervento e il progressivo=1
		dbr.put("n_intervento",new Integer (intProgressivo));
		dbr.put("progressivo",new Integer (1));
		//metto i flag ad N
/** 23/03/09 m
		dbr.put("flag_invio","N");
		dbr.put("flag_accolti","N");
**/
			// 23/03/09 m
		  	dbr.put("flag_livello", ""+autorInterv.LIV_AUT1);
		  	dbr.put("flag_esito", ""+autorInterv.ESI_CONC);
		System.out.println("ContribEJB/insertContribEtTestata/Contenuto dbr:" + dbr.getHashtable().toString());
		dbc.writeRecord(dbr);

		if ((strAbilitazCt != null) && (strAbilitazCt.equals("SI")))
		{
		  if ((h.get("importo_proposto")!=null) && !((String)h.get("importo_proposto")).equals(""))
		  {
		    String dataInizio=""+dbr.get("data_inizio_prop");
		    String dataFine=""+dbr.get("data_fine_prop");
		    messaggio=AggiornamentoContoEconomico(dbc,dbr,Double.parseDouble(""+dbr.get("importo_proposto")),
							dataInizio,dataFine);
		  }
		}

                htRet.put("messaggio", messaggio);
		htRet.put("n_intervento", "" + intProgressivo);

                dbc.commitTransaction();
                dbc.close();
                super.close(dbc);
		done = true;
                return htRet;
	}catch(DBRecordChangedException e){
		System.out.println("ContribEJB.insertContribEtTestata()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore di rollback per ContribEJB.insertContribEtTestata().rollback() - "+e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("ContribEJB.updateContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore di rollback per ContribEJB.insertContribEtTestata().rollback()- "+e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("ContribEJB.insertContribEtTestata()-->"+ e);
e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore di rollback per ContribEJB.insertContribEtTestata().rollback()- "+  e);
		}
		throw new SQLException("ContribEJB.insertContribEtTestata()-->"+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}
private boolean checkContribGrad(ISASConnection dbc,String codice_suss) throws  Exception {
    boolean ret=false;
        String myselect="Select * from sussidi where codice_suss = '"+codice_suss+"'";
        System.out.println("query checkContribGrad: "+myselect);
        ISASRecord dbr = dbc.readRecord(myselect);
        if (dbr.get("flag_grad")!=null && dbr.get("flag_grad").toString().equals("S"))
        	ret=true;
		return ret;  
   	}

private String esisteContributoMDomini(ISASConnection dbc, Hashtable h) {
	String ret = "";
	try {
	
	String sql = "select c.* from co_ffiss c where c.n_cartella = "+h.get("n_cartella").toString()+
					" and c.cod_contrib in ("+CODICI_SUSSIDIO_MONTEDOMINI+")";
	ISASRecord dbr = dbc.readRecord(sql);
	if (dbr!=null) ret = "Attenzione, esiste gi� un sussidio di tipo "+dbr.get("cod_contrib").toString()+" per questo assistito.\nNon � possibile inserire altri sussidi di questa tipologia.";
	LOG.info("contributo trovato: "+dbr.get("cod_contrib").toString());
	}catch (Exception e){e.printStackTrace();}
	return ret;
}

//gb 13.02.09
private int writeTestata(ISASConnection dbc, Hashtable h)
	throws Exception
  {
        try{
	System.out.println("ContribEJB/writeTestata/Hashtable testata: " + h.toString());
	TestataInterventi ti = new TestataInterventi();
	return ti.insertTestata(dbc, h);
      }catch(Exception ex)
      {
	  System.out.println("Errore eseguendo una ContribEJB.writeTestata() " + ex);
          throw new Exception("ContribEJB.writeTestata()-->"+ ex);
      }
  }

private ISASRecord caricaGriglia(ISASConnection dbc,Hashtable h) throws Exception
{
        Vector vdbg=new Vector();
	String cartella = null;
        String strNProgetto = null;
        String strCodObiettivo = null;
        String strNIntervento = null;
	String data = null;
        it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
        ServerUtility su =new ServerUtility();
        ISASRecord dbr=null;
        try{
                cartella = (String)h.get("n_cartella");
                strNProgetto = (String)h.get("n_progetto");
                strCodObiettivo = (String)h.get("cod_obbiettivo");
                strNIntervento = (String)h.get("n_intervento");
                data = (String)h.get("data");
        }catch(Exception ex){
             throw new SQLException("ContribEJB.caricaGriglia()-->MANCANO LE CHIAVI PRIMARIE"+ ex);
        }
        try {
            String myselect = "SELECT *" +
                              " FROM ass_contrib" +
                              " WHERE n_cartella = " + cartella +
                              " AND n_progetto = " + strNProgetto +
                              " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                              " AND n_intervento = " + strNIntervento +
                              " AND data = " + formatDate(dbc,data) +
                              " ORDER BY data_inizio_prop DESC";
            System.out.println("ContribEJB/caricaGriglia: " + myselect);

            ISASCursor dbcur=dbc.startCursor(myselect);

            dbr=dbc.readRecord(myselect);
            while (dbcur.next()){
                  ISASRecord dbrec=dbcur.getRecord();
                  //beneficiario
                  dbrec.put("beneficiario",util.getDecode(dbc,"beneficiari","b_codice",
                            (String)util.getObjectField(dbrec,"codice_benef",'I'),
                            "(nvl(trim(b_cognome),'') || ' ' || nvl(trim(b_nome),''))","benef_alias"));
                  //tipo contributo
                  dbrec.put("tipo",util.getDecode(dbc,"tipocontr","tc_codice",
                            (String)util.getObjectField(dbrec,"tipo_contrib",'S'),"tc_descri"));
                  dbrec.put("contributo",util.getDecode(dbc,"sussidi","codice_suss",
                          (String)util.getObjectField(dbrec,"cod_contrib",'S'),"descrizione_suss"));
                  dbrec.put("flag_grad",util.getDecode(dbc,"sussidi","codice_suss",
                          (String)util.getObjectField(dbrec,"cod_contrib",'S'),"flag_grad"));
                
                  getDecodifiche(dbc,dbr);
                  //comune
                  dbrec.put("desc_comune",util.getDecode(dbc,"comuni","codice",
                            (String)util.getObjectField(dbrec,"cod_comune_auto",'S'),"descrizione"));
                  //areadis
                  dbrec.put("desc_areadis",util.getDecode(dbc,"areadis","codice",
                            (String)util.getObjectField(dbrec,"cod_areadis",'S'),"descrizione"));
                  System.out.println("dbr contrib: "+dbrec.getHashtable().toString());
                  vdbg.addElement(dbrec);
           }//end while
          if (dbcur.getDimension()>0)
                  dbr.put("tabella",vdbg);
          dbcur.close();
          return dbr;
      }catch(Exception ex)
      {
          throw new SQLException("ContribEJB.caricaGriglia()-->"+ ex);
      }
}
private ISASRecord getIntervento(ISASConnection dbc, ISASRecord dbr) throws Exception{
	String myselect = "SELECT * FROM ass_interventi" +
	" WHERE n_cartella = " + dbr.get("n_cartella")
	+ " AND n_progetto = " + dbr.get("n_progetto") + " AND cod_obbiettivo = '" + dbr.get("cod_obbiettivo") + "'"
	+ " AND n_intervento = " + dbr.get("n_intervento");
	System.out.println("queryKey SocAssPrestazEJB : " + myselect);
	// Leggo il record
	ISASRecord dbrI = dbc.readRecord(myselect);	
	return dbrI;
}

private void updateTestata(ISASConnection dbc, ISASRecord dbr) throws Exception{	
	ISASRecord dbrI = getIntervento(dbc, dbr);
	if(dbrI!=null) {
		if( dbr.get("cod_servizio")!=null)
		dbrI.put("kservizio", dbr.get("cod_servizio"));	
	}
}
private void getDecodifiche(ISASConnection dbc, ISASRecord dbr) throws Exception {

    it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();  
    String strCdSettore = "";
	String strCdTipo = "";
	String strCdIntervento = "";
	String strCdConcat = "";
    ISASRecord dbrI=getIntervento(dbc, dbr);
    strCdConcat = (String) dbrI.get("int_cod_intervento");
	strCdSettore = strCdConcat.substring(0, 2);
	strCdTipo = strCdConcat.substring(2, 4);
	strCdIntervento = strCdConcat.substring(4);

  	 //info servizio
	String strServ="";
	if(dbr.get("cod_servizio")!=null)
	   strServ=dbr.get("cod_servizio").toString();
	
	if(strServ.equals(""))return;
  	   	 String cod_com_res=getComuneRes(dbc, dbr);
  	//if(h.containsKey("filtro_serv")&&h.get("filtro_serv").toString().equals("S"))
	//{	
  		LOG.info("getDecodifiche: servizio inizio ");
		 String myselect="select t.* from "
		  +" tab_servizi t where "
		  + " t.cod_settore_interv='"+strCdSettore+"'"
		  + " and t.cod_tipo_interv='" +strCdTipo+"'"
		  + " and t.cod_intervento='"+strCdIntervento+"'"
		  + " and t.cod_servizio='"+strServ+"'";
		  if(!cod_com_res.equals(""))					
		  myselect += " and t.cod_comune='" +cod_com_res+"'";
		  else if(dbr.get("cod_comune_auto")!=null && !dbr.get("cod_comune_auto").toString().equals(""))
		  {
			  myselect +=   " and t.cod_comune='"+ dbr.get("cod_comune_auto").toString().trim()+"'" ;
		  }
		  LOG.info("getDecodifiche: servizio:  "+myselect);
		  ISASRecord dbrServ=dbc.readRecord(myselect) ;
		  	 if(dbrServ!=null) {
		  		dbr.put("costounitario",dbrServ.get("costounitario"));
		  		dbr.put("unimis_desc",dbrServ.get("unimis_desc"));
		  		dbr.put("tipo_auto",dbrServ.get("tipo_auto"));
		  	 }
//}
}

private String getComuneRes(ISASConnection dbc, ISASRecord dbr)throws Exception{   	

	String mysel = "SELECT a.citta FROM anagra_c a  WHERE "+
	 " a.n_cartella ="+dbr.get("n_cartella")+
	 " and a.data_variazione=(select max(data_variazione) from anagra_c where n_cartella= "+dbr.get("n_cartella")+"" +
	 		" and data_variazione <="+ formatDate(dbc,dbr.get("data").toString())+")";
	LOG.info("getComuneRes "+mysel);
	ISASRecord dbrC=dbc.readRecord(mysel);
	String cod_com="";
	if(dbrC!=null) {
			cod_com=dbrC.get("citta").toString();
	}
	return cod_com;

}
public void deleteContrib(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
        boolean done=false;
        ISASConnection dbc=null;
/*GB ********
	String contatto = null;
	String cartella = null;
	String data = null;
        String progressivo =null;
        ServerUtility su =new ServerUtility();
*************/
        try{
                dbc=super.logIn(mylogin);
                dbc.startTransaction();
                dbc.deleteRecord(dbr);
                //Aggiornare il conto economico
                String dataInizio=((java.sql.Date)(dbr.get("data_inizio_prop"))).toString();
                String dataFine=((java.sql.Date)(dbr.get("data_fine_prop"))).toString();
                String messaggio=AggiornamentoContoEconomico(dbc,dbr,-((Double)dbr.get("importo_proposto")).doubleValue(),
                                              dataInizio,dataFine);
                dbc.commitTransaction();
                dbc.close();
                super.close(dbc);
                done=true;
	}catch(DBRecordChangedException e){
		System.out.println("ContribEJB.deleteContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.deleteContrib().rollback() - "+e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("ContribEJB.deleteContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.deleteContrib().rollback()- "+e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("ContribEJB.deleteContrib()-->"+ e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una ContribEJB.deleteContrib().rollback()- "+  e);
		}
		throw new SQLException("ContribEJB.deleteContrib()-->"+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

public ISASRecord insert(myLogin mylogin,Hashtable hcom)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
        CariException {
	boolean done=false;
	String cartella=null;
        String strNProgetto = null;
        String strCodObiettivo = null;
        String strNIntervento = null;
	String data=null;
        String messaggio="";
	ISASConnection dbc=null;
        ISASRecord dbr=null;
	try {
                cartella=(String)hcom.get("n_cartella");
                strNProgetto = (String)hcom.get("n_progetto");
                strCodObiettivo = (String)hcom.get("cod_obbiettivo");
                strNIntervento = (String)hcom.get("n_intervento");
		data=(String)hcom.get("data");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: mancano elementi della chiave primaria");
	}
	try{
              dbc=super.logIn(mylogin);
              dbc.startTransaction();
              System.out.println("ContribEJB/insert");
              messaggio=insertDbContrib(dbc,hcom);
              dbr=caricaGriglia(dbc,hcom);
              dbc.commitTransaction();
              dbc.close();
              super.close(dbc);
              done=true;
              if(!messaggio.equals(""))
                   throw new CariException(messaggio);
              return dbr;
        }catch(CariException ce)
        {
                ce.setISASRecord(dbr);
                throw ce;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(Exception e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw new SQLException("Errore eseguendo una insert() - "+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

public ISASRecord query_decodconto(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    ISASRecord dbrAusi = null;
    try{
          dbc=super.logIn(mylogin);
          String myselect="Select * from sussidi where "+
                          "codice_suss='"+(String)h.get("codice_suss")+"'";
          ISASRecord dbr=dbc.readRecord(myselect);
          if (dbr!=null){
            String select = "SELECT * FROM co_economici WHERE "+
                            "eco_anno='"+(String)h.get("anno")+
                            "' AND eco_comune='"+(String)h.get("comune_auto")+
                            "' AND eco_codice='"+(String)dbr.get("capitolo_suss")+"'";
            dbrAusi = dbc.readRecord(select);
          }
          dbc.close();
          super.close(dbc);
          done=true;
          if (dbrAusi != null){
            dbr.put("eco_budget", dbrAusi.get("eco_budget"));
            dbr.put("eco_impegn", dbrAusi.get("eco_impegn"));
            dbr.put("ritorno", "T");
            //System.out.println("Cosa ritorna? "+dbr.getHashtable().toString());
            return dbr;
          }else{
            if (dbr!=null)
              dbr.put("ritorno","S");
            return dbr;
          }
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


public Vector query(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done=false;
  ISASConnection dbc=null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;

  try{
    dbc=super.logIn(mylogin);

    if(h.get("n_cartella") == null)
      {
      throw new SQLException("Manca numero cartella in esecuzione query()");
      }
    else
      strNCartella = (String)h.get("n_cartella");
    if(h.get("n_progetto") == null)
      {
      throw new SQLException("Manca numero progetto in esecuzione query()");
      }
    else
      strNProgetto = (String)h.get("n_progetto");
    if(h.get("cod_obbiettivo") == null)
      {
      throw new SQLException("Manca il cod. obiettivo in esecuzione query()");
      }
    else
      strCodObiettivo = (String)h.get("cod_obbiettivo");
    if(h.get("n_intervento") == null)
      {
      throw new SQLException("Manca il numero intervento in esecuzione query()");
      }
    else
      strNIntervento = (String)h.get("n_intervento");

    String myselect = "SELECT DISTINCT data, n_contatto" +
                      " FROM ass_contrib" +
                      " WHERE n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento;
    System.out.println("ContribEJB/query: " + myselect);

    ISASCursor dbcur=dbc.startCursor(myselect);
    Vector vdbr=dbcur.getAllRecord();
    dbcur.close();
    dbc.close();
    super.close(dbc);
    done=true;
    return vdbr;
    }
  catch(Exception e)
    {
    e.printStackTrace();
    throw new SQLException("Errore eseguendo una query()  ");
    }
  finally
    {
    if(!done)
      {
      try{
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e1)
        {System.out.println(e1);}
      }
    }
  }

private void decodificaQueryStoricoTipoContrib(ISASConnection dbc, ISASRecord dbr,
            String codTipoContrib, String descrTipoContribDbName) throws Exception
  {
  if ( codTipoContrib == null || codTipoContrib.equals(""))
    {
    dbr.put(descrTipoContribDbName, "");
    return;
    }

  String myselect = "SELECT * FROM tipocontr" +
                    " WHERE tc_codice = '" + codTipoContrib + "'";
  System.out.println("ContribEJB/decodificaQueryStoricoTipoContrib: " + myselect);
  ISASRecord dbrDecod = dbc.readRecord(myselect);

  String strDescrTipoContrib = (String)dbrDecod.get("tc_descri");
  if ( strDescrTipoContrib != null )
    dbr.put(descrTipoContribDbName, strDescrTipoContrib);
  else
    dbr.put(descrTipoContribDbName, "");
  }

public Vector query_storico(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  ISASConnection dbc = null;
  String strNCartella = null;
  String strNProgetto = null;
  String strCodObiettivo = null;
  String strNIntervento = null;
  String sel = "";
  try{
    dbc = super.logIn(mylogin);

    if(h.get("n_cartella") == null)
      {
      throw new SQLException("Manca numero cartella in esecuzione query_storico()");
      }
    else
      strNCartella = (String)h.get("n_cartella");
    if(h.get("n_progetto") == null)
      {
      throw new SQLException("Manca numero progetto in esecuzione query_storico()");
      }
    else
      strNProgetto = (String)h.get("n_progetto");
    if(h.get("cod_obbiettivo") == null)
      {
      throw new SQLException("Manca il cod. obiettivo in esecuzione query_storico()");
      }
    else
      strCodObiettivo = (String)h.get("cod_obbiettivo");
    if(h.get("n_intervento") == null)
      {
      throw new SQLException("Manca il numero intervento in esecuzione query_storico()");
      }
    else
      strNIntervento = (String)h.get("n_intervento");

    String myselect = "SELECT DISTINCT data FROM ass_contrib" +
                      " WHERE n_cartella = " + strNCartella +
                      " AND n_progetto = " + strNProgetto +
                      " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                      " AND n_intervento = " + strNIntervento +
                      " ORDER BY data DESC";
    System.out.println(">>>>>>>>>ContribEJB.query_storico - myselect=["+myselect+"]");

    ISASCursor dbcur = dbc.startCursor(myselect);
    Vector vdbr = dbcur.getAllRecord();
    dbcur.close();

    for (Enumeration senum = vdbr.elements(); senum.hasMoreElements(); )
      {
      ISASRecord dbr =(ISASRecord)senum.nextElement();
      String dataVar = ((java.sql.Date)dbr.get("data")).toString();

      sel = "SELECT tipo_contrib FROM ass_contrib" +
            " WHERE n_cartella = " + strNCartella +
            " AND n_progetto = " + strNProgetto +
            " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
            " AND n_intervento = " + strNIntervento +
            " AND data = " + formatDate(dbc, dataVar);
    System.out.println(">>>>>>>>>ContribEJB.query_storico - sel=["+sel+"]");

      ISASCursor dbcur2 = dbc.startCursor(sel);
      int k = 1;
      while (dbcur2.next())
        {
        ISASRecord dbr2 = dbcur2.getRecord();

        if(dbr2.get("tipo_contrib")!=null && !((String)dbr2.get("tipo_contrib")).equals(""))
          {
          dbr.put("tipo_contrib_"+k, (String)dbr2.get("tipo_contrib"));
          decodificaQueryStoricoTipoContrib(dbc, dbr, (String)dbr2.get("tipo_contrib"), "descr_tipo_contrib_"+k);
          }
        else
          dbr.put("tipo_contrib_"+k, " ");
        k++;
        }
      dbcur2.close();
      }

    dbc.close();
    super.close(dbc);
    done = true;
    return vdbr;
    }
  catch(Exception e)
    {
    e.printStackTrace();
    throw new SQLException("Errore eseguendo una query()  ");
    }
  finally
    {
    if(!done)
      {
      try{
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e1)
        {System.out.println(e1);}
      }
    }
  }

public void delete(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
  {
  boolean done=false;
  ISASConnection dbc=null;
  try{
    dbc=super.logIn(mylogin);

    String cartella =(String)h.get("n_cartella");
    String strNProgetto = (String)h.get("n_progetto");
    String strCodObiettivo = (String)h.get("cod_obbiettivo");
    String strNIntervento = (String)h.get("n_intervento");
    String data =(String)h.get("data");

    dbc.startTransaction();

    //cancello tutti i record su ass_contrib con quella chiave
    String select = "SELECT *" +
                    " FROM ass_contrib" +
                    " WHERE n_cartella = " + cartella +
                    " AND n_progetto = " + strNProgetto +
                    " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
                    " AND n_intervento = " + strNIntervento +
                    " AND data = " + formatDate(dbc,data);
    System.out.println("ContribEJB/delete: " + select);

    ISASCursor dbcur=dbc.startCursor(select);
    Vector vdbr=dbcur.getAllRecord();
    ISASRecord dbdiag=null;
    String selectProgr = "";
    for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
      {
      dbdiag = (ISASRecord)senum.nextElement();
      String strProgr = ((Integer)dbdiag.get("progressivo")).toString();
      String clauseProgr = " AND progressivo = " + strProgr;
      selectProgr = select + clauseProgr;
      ISASRecord dbrdel = dbc.readRecord(selectProgr);
      if (dbrdel!=null)
        {
        dbc.deleteRecord(dbrdel);
        String dataInizio=((java.sql.Date)(dbrdel.get("data_inizio_prop"))).toString();
        String dataFine=((java.sql.Date)(dbrdel.get("data_fine_prop"))).toString();
        String messaggio=AggiornamentoContoEconomico(dbc,dbrdel,-((Double)dbrdel.get("importo_proposto")).doubleValue(),
                                                      dataInizio,dataFine);
        }
      }
    dbc.commitTransaction();
    dbc.close();
    super.close(dbc);
    done=true;
    }
  catch(DBRecordChangedException e)
    {
    e.printStackTrace();
    try{
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
      }
    throw e;
    }
  catch(ISASPermissionDeniedException e)
    {
    e.printStackTrace();
    try{
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
      }
    throw e;
    }
  catch(Exception e)
    {
    e.printStackTrace();
    try{
      dbc.rollbackTransaction();
      }
    catch(Exception e1)
      {
      throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
      }
    throw new SQLException("Errore eseguendo una delete() - "+  e);
    }
  finally
    {
    if(!done)
      {
      try{
        dbc.close();
        super.close(dbc);
        }
      catch(Exception e2)
        {System.out.println(e2);}
      }
    }
  }

public Hashtable query_Allcombo(myLogin mylogin,Hashtable h,Vector vkey)
 throws SQLException {

        Hashtable res =null;
        String mysel_where=null;
        try{
                for(int i=0;i<vkey.size();i++)
                {
                      String key=(String)vkey.elementAt(i);
                      if (key.equals("CONTRIB")){
                        Vector vdbr=queryCombo_Contrib(mylogin,h);
                        if(res==null)
                            res=new Hashtable();
                        res.put(key,vdbr);
                      }else if(key.equals("TIPOCONTR")){
                        Vector vdbr=queryCombo_TipoContrib(mylogin,h);
                        if(res==null)
                            res=new Hashtable();
                        res.put(key,vdbr);
                      }
                    }
                return res;
        }catch(Exception e){
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una ContribEJB.query_Allcombo() ");
        }
} // fine query_Allcombo

public Vector queryCombo_Contrib(myLogin mylogin,Hashtable h) throws SQLException {
        boolean done = false;
        ISASConnection dbc = null;
        try {
                dbc=super.logIn(mylogin);
                String myselect = "SELECT * FROM sussidi "+
                                  " ORDER BY descrizione_suss ";
                ISASCursor dbcur=dbc.startCursor(myselect);
                Vector vdbr=dbcur.getAllRecord();
                dbc.close();
                super.close(dbc);
                done = true;
                return vdbr;
        } catch(Exception e) {
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una ContribEJB.queryCombo_Contrib()  ");
        }finally {
                if(!done) {
                        try {
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e1) {
                                System.out.println(e1);
                        }
                }
        }
}
public Vector queryCombo_TipoContrib(myLogin mylogin,Hashtable h) throws SQLException {
        boolean done = false;
        ISASConnection dbc = null;
        try {
                dbc=super.logIn(mylogin);
                String myselect="SELECT * FROM tipocontr"+
                               " ORDER BY tc_descri ";
                ISASCursor dbcur=dbc.startCursor(myselect);
                Vector vdbr=dbcur.getAllRecord();
                dbc.close();
                super.close(dbc);
                done = true;
                return vdbr;
        } catch(Exception e) {
                e.printStackTrace();
                throw new SQLException("Errore eseguendo una ContribEJB.queryCombo_TipoContrib()  ");
        }finally {
                if(!done) {
                        try {
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e1) {
                                System.out.println(e1);
                        }
                }
        }
}

	public  Vector autorizzaDaElenco(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,CariException 
	{
		//per la commissione
		boolean done=false;
		ISASConnection dbc=null;
		String data_comm=null;
		ISASRecord dbrec=null;
	//	boolean autor=false;
		int auto=0;
		String strCodOperatore = "";
		String strAbilCtAuto = "";
	//	boolean boolAbil2Livelli = false;

		// 30/04/10
		
		
    	try {           
            data_comm = (String)h.get("data_auto");
    //        String au=(String)h.get("autorizzo");
    //        if(au.equals("OK"))
	//			autor=true;
            auto=Integer.parseInt((String)h.get("livello_auto"));
	      	strCodOperatore = (String)h.get("cod_operatore");
	      	strAbilCtAuto = (String)h.get("abilCt_SI_auto_SI");
	      	//gb 03/11/2008
	   //   	String strAbil2Livelli = (String) h.get("abil_2_livelli");
	 //     	boolAbil2Livelli = ((strAbil2Livelli != null) && strAbil2Livelli.equals("SI"));
	      	//gb 03/11/2008: fine
        }catch (Exception e){
            e.printStackTrace();
            throw new SQLException("DEBUG:ContribEJB.autorizzaDaElenco()-->Errore: manca la chiave primaria");
        }

        try{
            dbc=super.logIn(mylogin);
            dbc.startTransaction();
            System.out.println("autorizzaDaElenco:"+h.toString());   
         
            //prendo il vettore
            Vector vdb=(Vector)h.get("vettore");

            for (int j=0;j<vdb.size();j++)  {
                Hashtable hDati=(Hashtable)vdb.elementAt(j);
            	aggiornaContributo(dbc,hDati,h);          
			} // fine ciclo for
    
			dbc.commitTransaction();
		    dbc.close();
		    super.close(dbc);
		    done=true;
		
		    return vdb;
		}catch(CariException ce){
            ce.setISASRecord(dbrec);
            throw ce;
   		}catch(DBRecordChangedException e){
		    System.out.println("ContribEJB.autorizzaDaElenco()-->"+ e);
		    try{
        		dbc.rollbackTransaction();
      		}catch(Exception e1){
		        throw new DBRecordChangedException("Errore eseguendo una ContribEJB/autorizzaDaElenco.rollback() - "+  e1);
      		}
      		throw e;
    	}catch(ISASPermissionDeniedException e){
	       	System.out.println("ContribEJB.autorizzaDaElenco()-->"+ e);
      		try{
        		dbc.rollbackTransaction();
      		} catch(Exception e1){
        		throw new ISASPermissionDeniedException("Errore eseguendo una ContribEJB/autorizzaDaElenco.rollback() - "+  e);
      		}
      		throw e;
    	}catch(Exception e){
       		System.out.println("ContribEJB.autorizzaDaElenco()-->"+ e);
      		try{
        		dbc.rollbackTransaction();
      		}catch(Exception e1){
        		throw new SQLException("Errore eseguendo una ContribEJB/autorizzaDaElenco.rollback() - "+  e);
      		}
      		throw new SQLException("Errore eseguendo una ContribEJB/autorizzaDaElenco.updateVector() - "+  e);
    	}
    	finally{
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
	/**
	 * 
	 * @param dbc
	 * @param hDati
	 * @param h con la presenza di:
	 * data_comm = (String)h.get("data_auto");     
     *  auto=Integer.parseInt((String)h.get("livello_auto"));
	 *   strCodOperatore = (String)h.get("cod_operatore");
	*    strAbilCtAuto = (String)h.get("abilCt_SI_auto_SI");
	 * @throws Exception
	 */
private void aggiornaContributo(ISASConnection dbc,Hashtable hDati,Hashtable h) throws Exception{
	String data_comm = (String)h.get("data_auto");
     int auto=Integer.parseInt((String)h.get("livello_auto"));
	  String    	strCodOperatore = (String)h.get("cod_operatore");
	   String   	strAbilCtAuto = (String)h.get("abilCt_SI_auto_SI");
    String selPro="";
    selPro="SELECT * FROM ass_contrib"+
               " WHERE n_cartella="+hDati.get("n_cartella")+
               " AND n_progetto="+hDati.get("n_progetto")+
               " AND n_intervento="+hDati.get("n_intervento")+
               " AND cod_obbiettivo='"+hDati.get("cod_obbiettivo")+ "'";
               //" AND data="+formatDate(dbc,""+hDati.get("data"));
    System.out.println("Select:"+selPro);
   ISASRecord  dbrec=null;
    dbrec = dbc.readRecord(selPro);

	String strParere = (String)hDati.get("parere");

	if ((strAbilCtAuto != null) && strAbilCtAuto.equals("SI")){
		//Devo fare l'aggiornamento dei conti economici (sottraggo l'attuale)
		String messaggio = "";
		if (strParere != null)
//30/04/10		    		messaggio = updateContribContoEcon(-1, dbc, dbrec);
			// 30/04/10	
			messaggio = cEco.updateContribContoEcon(-1, dbc, dbrec);
	}

	if (strParere != null) {					
		// per registrare i campi non riguardanti prop/auto1/auto2, i cui campi, invece, verranno valorizzati dopo.
		Enumeration enu = hDati.keys();
      	while(enu.hasMoreElements()){
			String key = (String)enu.nextElement();
			dbrec.put(key, hDati.get(key));
		}

		if (strParere.equals(""+autorInterv.PAR_POS) || strParere.equals(""+autorInterv.PAR_NEG)) {
			if (auto == autorInterv.LIV_PROP) {
			    //Porto i dati dell'i-esima riga della griglia nei campi di prop
			    dbrec.put("data_inizio_prop", ndf.formDate((String)hDati.get("data_ini"),"aaaa-mm-gg"));
			    dbrec.put("data_fine_prop", ndf.formDate((String)hDati.get("data_fin"),"aaaa-mm-gg"));
			    dbrec.put("importo_proposto",new Double(hDati.get("importo")!=null?(String)hDati.get("importo"):"0"));
				dbrec.put("desc_proposta", (String)hDati.get("parere_motivo"));
			} else if (auto == autorInterv.LIV_AUT1) {
			    //Porto i dati dell'i-esima riga della griglia nei campi di auto-1
			    dbrec.put("data_inizio", ndf.formDate((String)hDati.get("data_ini"),"aaaa-mm-gg"));
			    dbrec.put("data_fine", ndf.formDate((String)hDati.get("data_fin"),"aaaa-mm-gg"));
			    dbrec.put("importo_comm1",new Double(hDati.get("importo")!=null?(String)hDati.get("importo"):"0"));
			    dbrec.put("data_comm", data_comm);
			    dbrec.put("desc_comm1", (String)hDati.get("parere_motivo"));
			    dbrec.put("parere_comm", strParere);
			    dbrec.put("oper_auto1", strCodOperatore); 
			} else if (auto == autorInterv.LIV_AUT2) {
			    //Porto i dati dell'i-esima riga della griglia nei campi di auto-2
			    dbrec.put("data_inizio_comm2", ndf.formDate((String)hDati.get("data_ini"),"aaaa-mm-gg"));
			    dbrec.put("data_fine_comm2", ndf.formDate((String)hDati.get("data_fin"),"aaaa-mm-gg"));
			    dbrec.put("importo_comm2",new Double(hDati.get("importo")!=null?(String)hDati.get("importo"):"0"));
			    dbrec.put("data_comm2", data_comm);
			    dbrec.put("desc_comm2", (String)hDati.get("parere_motivo"));
			    dbrec.put("parere_comm2", strParere);
			    dbrec.put("oper_auto2", strCodOperatore);
	            if(dbrec.get("data_comm")==null)dbrec.put("data_comm", data_comm);
			    //gb 03/11/2008
			  //  if (!boolAbil2Livelli) 
					
	  		}

		    dbrec.put("flag_livello", "" + (auto+1));
		    dbrec.put("flag_esito", "" + autorInterv.ESI_CONC);
		} else if (strParere.equals(""+autorInterv.PAR_SOS)) { // sospesi
			if (auto == autorInterv.LIV_AUT1) {
				dbrec.put("data_comm", data_comm);
			    dbrec.put("desc_comm1", (String)hDati.get("parere_motivo"));
			    dbrec.put("parere_comm", strParere);
			    dbrec.put("oper_auto1", strCodOperatore);
			} else if (auto == autorInterv.LIV_AUT2) {
				dbrec.put("data_comm2", data_comm);
			    dbrec.put("desc_comm2", (String)hDati.get("parere_motivo"));
			    dbrec.put("parere_comm2", strParere);
			    dbrec.put("oper_auto2", strCodOperatore);
			}
		
		    dbrec.put("flag_livello", "" + (auto-1));
			dbrec.put("flag_esito", "" + autorInterv.ESI_SOSP);
		}

  		dbc.writeRecord(dbrec);
	}
			 selPro="SELECT * FROM ass_contrib"+
		     " WHERE n_cartella="+hDati.get("n_cartella")+
		     " AND n_progetto="+hDati.get("n_progetto")+
		     " AND n_intervento="+hDati.get("n_intervento")+
		     " AND cod_obbiettivo='"+hDati.get("cod_obbiettivo")+ "'";
		     //" AND data="+formatDate(dbc,""+hDati.get("data"));
			System.out.println("Select:"+selPro);
			dbrec = dbc.readRecord(selPro);
			if(dbrec!=null) {
				if ((strAbilCtAuto != null) && strAbilCtAuto.equals("SI")){
					//Devo fare l'aggiornamento dei conti economici (aggiungo il nuovo)
			  		String messaggio = "";
			  		//bargi 18/10/2012 se il parere � negativo e sono al livello 2 non devo aggiornare positivamente i conti
			  		if (strParere != null ){
			//30/04/10		    		messaggio = updateContribContoEcon(1, dbc, dbrec);
						// 30/04/10	
			  			if((auto == autorInterv.LIV_AUT2)&&strParere.equals(""+autorInterv.PAR_NEG))
			  				messaggio+="STORNATO IMPEGNATO";
			  			else
						messaggio = cEco.updateContribContoEcon(1, dbc, dbrec);						
						
				    	if ((auto == autorInterv.LIV_AUT2) && (strParere.equals(""+autorInterv.PAR_POS)))
				    		corou.insertContribCoFfiss(dbc, dbrec, strCodOperatore);
				  	}
				}
			}
}
public String getResiduoEsistPrimoAnno(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  String strResult = "";
  ISASConnection dbc = null;

  try{
	System.out.println("ContribEJB/getResiduoEsistPrimoAnno/h: " + h.toString());
	dbc = super.logIn(mylogin);

	calcoloContoEconomico cEco = new calcoloContoEconomico();
	strResult = cEco.getResiduoContoEconomico(dbc, h);
	if (strResult.trim().equals(""))
	{
	  strResult = "KO";
	}
	dbc.close();
	super.close(dbc);
	done = true;

	return strResult;
  }catch(Exception e){
      System.out.println("ContribEJB: getResiduoEsistPrimoAnno - Eccezione= " + e);
      throw new SQLException("Errore eseguendo i controlli entit� chiuse  ");
    }finally{
      if(!done){
        try{
          dbc.close();
          super.close(dbc);
        }catch(Exception e1){
          System.out.println("ContribEJB: getResiduoEsistPrimoAnno - Eccezione nella chiusura della connessione= " + e1);
        }
      }
    }
  }

public String getImportoGgGettPresenza(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  String strResult = "";
  ISASConnection dbc = null;

  try{
	System.out.println("ContribEJB/getImportoGgGettPresenza/h: " + h.toString());
	dbc = super.logIn(mylogin);

	String strDtInizioValid = (String) h.get("data_inizio_validita");
	String strDtFormatted = formatDate(dbc, strDtInizioValid);
	h.put("data_ini_val_formatted", strDtFormatted);

	calcoloContoEconomico cEco = new calcoloContoEconomico();
	strResult = cEco.getImportoGgGettPresenza(dbc, h);
	if (strResult.trim().equals(""))
	{
	  strResult = "KO";
	}
	dbc.close();
	super.close(dbc);
	done = true;

	return strResult;
  }catch(Exception e){
      System.out.println("ContribEJB: getImportoGgGettPresenza - Eccezione= " + e);
      throw new SQLException("Errore eseguendo i controlli entit� chiuse  ");
    }finally{
      if(!done){
        try{
          dbc.close();
          super.close(dbc);
        }catch(Exception e1){
          System.out.println("ContribEJB: getImportoGgGettPresenza - Eccezione nella chiusura della connessione= " + e1);
        }
      }
    }
  }

public Hashtable getCodsGettPresenza(myLogin mylogin, Hashtable h) throws SQLException
  {
  boolean done = false;
  Hashtable htResult = new Hashtable();
  ISASConnection dbc = null;
  String strCodZona = "";
  String strCodTipoContrib = "";
  ISASCursor dbcur = null;
  try{
	System.out.println("ContribEJB/getCodsGettPresenza/h: " + h.toString());
	dbc = super.logIn(mylogin);

	strCodZona = (String) h.get("cod_zona");
	strCodTipoContrib = (String) h.get("cod_tipo_contrib");

	String strQuery = "SELECT DISTINCT a.cod_contrib" +
			" FROM co_impgett a" +
			" WHERE a.cod_zona = '" + strCodZona + "'" +
			" AND a.tipo_gett_presenza = '" + strCodTipoContrib + "'";
	System.out.println("ContribEJB/getCodsGettPresenza/strQuery: " + strQuery);
	dbcur = dbc.startCursor(strQuery);
	if ((dbcur != null) && (dbcur.getDimension() > 0))
	{
	  while (dbcur.next())
	  {
	    ISASRecord dbrec = dbcur.getRecord();
	    if ((dbrec != null) && (dbrec.get("cod_contrib") != null))
	    {
		String strCod = (String) dbrec.get("cod_contrib");
		htResult.put(strCod, strCod);
	    }
	  }
	}
	if (dbcur != null)
	  dbcur.close();
	dbc.close();
	super.close(dbc);
	done = true;

	return htResult;
  }catch(Exception e){
      System.out.println("ContribEJB: getCodsGettPresenza - Eccezione= " + e);
      throw new SQLException("Errore eseguendo i controlli entit� chiuse  ");
    }finally{
      if(!done){
        try{
	  if (dbcur != null)
	    dbcur.close();
          dbc.close();
          super.close(dbc);
        }catch(Exception e1){
          System.out.println("ContribEJB: getCodsGettPresenza - Eccezione nella chiusura della connessione= " + e1);
        }
      }
    }
  }
//======================================================================================================
/**
 *@TODO GESTIONE PIANIFICAZIONE SERVIZI
 */

routineContrib corou=new routineContrib();

public Vector queryPianif(myLogin mylogin, Hashtable h) throws Exception {
	ISASConnection dbc = null;
	boolean done = false;
	try {
		dbc = super.logIn(mylogin);
		Vector vdbr = null;
         vdbr=corou.queryPianif(dbc, h);
		dbc.close();
		super.close(dbc);
		done = true;
		return vdbr;
	} catch (Exception e) {
		System.out.println(nomeEJB + " ERRORE queryPianif() - " + e);			
		throw e;
	} finally {
		if (!done) {
			if (dbc != null) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + " Errore eseguendo una queryPianif()  " + e1);
				}
			}
		}
	}
}

public ISASRecord insertPianif(myLogin mylogin, Hashtable h) throws Exception {
	ISASConnection dbc = null;
	boolean done = false;
	try {
		dbc = super.logIn(mylogin);
		dbc.startTransaction();
		ISASRecord rec = null;
         rec=corou.insertPianif(dbc, h);
		dbc.commitTransaction();
		dbc.close();
		super.close(dbc);
		done = true;
		return rec;
	} catch (Exception e) {
		System.out.println(nomeEJB + " ERRORE insertPianif() - " + e);		
		try {
			dbc.rollbackTransaction();
		} catch (Exception e1) {
			throw new SQLException(nomeEJB+"Errore insertPianif().rollback() - " + e);
		}
		throw e;
	} finally {
		if (!done) {
			if (dbc != null) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + " Errore eseguendo una insertPianif()  " + e1);
				}
			}
		}
	}
}

public ISASRecord updatePianif(myLogin mylogin, Hashtable h,Vector v) throws Exception {
	ISASConnection dbc = null;
	boolean done = false;
	try {
		dbc = super.logIn(mylogin);
		dbc.startTransaction();
		ISASRecord rec = null;
        rec=corou.updatePianif(dbc, h); 
		dbc.commitTransaction();
		dbc.close();
		super.close(dbc);
		done = true;
		return rec;
	} catch (Exception e) {
		System.out.println(nomeEJB + "ERRORE updatePianif() - " + e);		
		try {
			dbc.rollbackTransaction();
		} catch (Exception e1) {
			throw new SQLException(nomeEJB+"Errore updatePianif().rollback() - " + e);
		}
		throw e;
	} finally {
		if (!done) {
			
			if (dbc != null) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + " Errore eseguendo una updatePianif()  " + e1);
				}
			}
		}
	}
}

public void deletePrianif(myLogin mylogin, Hashtable h) throws Exception {
	System.out.println(nomeEJB + " deletePrianif()- H ==  " + h.toString());
	ISASConnection dbc = null;
	boolean done = false;
	try {
		dbc = super.logIn(mylogin);
		dbc.startTransaction();
		corou.deletePianif(dbc, h);
		dbc.commitTransaction();
		dbc.close();
		super.close(dbc);
		done = true;
	} catch (Exception e) {
		System.out.println(nomeEJB + " ERRORE deletePrianif() - " + e);			
		try {
			dbc.rollbackTransaction();
		} catch (Exception e1) {
			throw new SQLException(nomeEJB+"Errore deletePianif().rollback() - " + e);
		}
		throw e;
	} finally {
		if (!done) {
			if (dbc != null) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + " Errore eseguendo una deletePrianif()  " + e1);
				}
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
		String sql = "select nvl(max(prog_valut),0)+1 next_progr from co_valutaz where n_cartella = "+h.get("n_cartella").toString();
        rec = dbc.readRecord(sql);
		done = true;
		dbc.close();
		super.close(dbc);
		if (rec!=null) ret = (Integer)rec.get("next_progr");
		return ret;
	} catch (Exception e) {
		System.out.println(nomeEJB + "ERRORE getNextProgValut() - " + e);			
		throw e;
	} finally {
		if (!done) {
			if (dbc != null) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(nomeEJB + " Errore eseguendo una getNextProgValut()  " + e1);
				}
			}
		}
	}
}

//======================================================================================================
}

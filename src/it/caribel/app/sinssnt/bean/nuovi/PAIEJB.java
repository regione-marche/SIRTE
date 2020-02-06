package it.caribel.app.sinssnt.bean.nuovi;

//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//
//21/11/2014 - EJB di connessione alla procedura SINSSNT Tabella sc_bisogni
//
// Valerio Franchi
//==========================================================================

import it.caribel.app.sinssnt.bean.PrRugEJB;
import it.caribel.app.sinssnt.bean.SclValutazioneEJB;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.caribel.app.sinssnt.bean.nuovi.ScaleVal;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class PAIEJB extends SINSSNTConnectionEJB  
{
	private ServerUtility su = new ServerUtility();

	private ScaleVal gest_scaleVal = new ScaleVal(); 
	private EveUtils eveUtl = new EveUtils();
	
	private boolean myDebug = true;
	private String nomeEJB = getClass().getName();

	private String ver = "1-";
	
	public PAIEJB() {}


	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
		LOG.debug("queryKey public -- H == " + h.toString());		
		ISASConnection dbc = null;
		
		try	{			
			dbc = super.logIn(mylogin);
			return queryKey(dbc, h);
		} catch(Exception e){
			throw newEjbException(nomeEJB + "Errore eseguendo una queryKey()  ", e);
		} finally {
			logout_nothrow(nomeEJB + "queryKey()  ", dbc);
		}
	}
	
	private ISASRecord queryKey(ISASConnection dbc, Hashtable h) throws  Exception {
		
			String cartella = "";
			String data = "";
			
			try	{
				cartella = (String)h.get("n_cartella");
				data = (String)h.get("data");
			} catch(Exception e) {
				System.out.println(nomeEJB + " query_insert() [ " + e + " ] == Errore: manca la chiave primaria!!!");
				throw new SQLException("Errore: manca la chiave primaria");
			}

			String myselect = "SELECT a.* " +
					"FROM sc_bisogni a WHERE "+
								" a.n_cartella = "+cartella+
								" and a.data = "+dbc.formatDbDate(data);
			
			String myselect_rug_siad = "SELECT " +
					" b.a12,b.a13_a,b.a13_b,b.a13_c,b.a14," +
					"b.rug_punt,b.g1a,b.g1d,b.g1e," +
					" b.g2g,b.g2h,b.g2i,b.g2j,b.l7,b.j3j,b.n2e,b.n2g,b.n2j,b.n2h,b.k2a,b.k2b,b.k3,b.j3n," +
					" b.j3s_int,b.j3s_uri,b.j3s,b.n2b,b.l1,b.l4,b.l5,b.n2k,b.c1,b.e3a,b.e3b,b.e3c,b.e3d," +
					" b.e3e,b.e3f,b.j3h,b.j3i,b.j3r,b.n2a,b.n2f,b.j7,b.n2i,b.n2d, " +
					" c.pat_prev, c.pat_conco,c.autonomia,c.mobilita,c.cognitivi,c.comportamento,c.supp_sociale," +
					" c.rischio_infettivo,c.livello,c.drenaggio,c.ossigeno_terapia,c.ventiloterapia,c.tracheotomia," +
					" c.alim_assistita,c.alim_enterale,c.alim_parentale,c.stomia,c.elim_urinaria,c.sonno_veglia," +
					" c.educ_terap,c.ulcere12g,c.ulcere34g,c.prelievi_venosi,c.ecg,c.telemetria,c.ter_sottocut," +
					" c.catetere,c.trasfusioni,c.dolore,c.terminale_onc,c.terminale_nononc,c.neurologico,c.ortopedico," +
					" c.mantenimento,c.supervisione,c.ass_iadl,c.ass_adl,c.care_giver,c.elim_urinaria_int,c.elim_urinaria_uri," +
					" c.stomia_int,c.stomia_uri "+
					"FROM  rugiii_hc b, scl_valutazione c WHERE "+
								" b.n_cartella = "+cartella+
								" and b.data = "+dbc.formatDbDate(data)
								+ " and b.n_cartella = c.n_cartella and b.data=c.data";
			
			ISASRecord dbr = dbc.readRecord(myselect);
			ISASRecord dbr1 = dbc.readRecord(myselect_rug_siad);	
			if (dbr1!=null){
			Hashtable h1 = dbr1.getHashtable();
			Enumeration n=h1.keys();
			while(n.hasMoreElements()) {
				String e = n.nextElement().toString();
				dbr.put(e,h1.get(e));
			}
			}
			return dbr;

	}

	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			
			dbc=super.logIn(mylogin);
			
			String cartella = (String) h.get("n_cartella");
			String id_skso = h.containsKey("id_skso")?(String)h.get("id_skso"):"";
			String myselect = "SELECT a.n_cartella, a.data, a.cod_operatore,a.id_skso FROM sc_bisogni a WHERE "+
					" a.n_cartella = "+cartella+
					(id_skso.equals("")?"":" and a.id_skso = "+id_skso)+
					" order by a.data";
			
			ISASCursor dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ",e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public Vector query_grigliaStorico(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		try	{
			dbc = super.logIn(mylogin);

			
			String cartella = (String) h.get("n_cartella");
			String id_skso = h.containsKey("id_skso")?(String)h.get("id_skso"):"";
			String myselect = "SELECT a.n_cartella, a.data, a.cod_operatore,a.id_skso FROM sc_bisogni a WHERE "+
								" a.n_cartella = "+cartella+
								(id_skso.equals("")?"":" and a.id_skso = "+id_skso)+
								" order by a.data";
			
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
			System.out.println(nomeEJB + "query_grigliaStorico() [ " + e + " ]");
			throw new SQLException("Errore eseguendo una query_grigliaStorico()  ",e);
		}
		finally {
			if(!done) {
				try	{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1){   
					System.out.println(nomeEJB + "query_grigliaStorico() [ " + e1 + " ]");   
				}
			}
		}
	}

	public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		mySystemOut(" insert() -- H == " + h.toString());
		boolean done = false;
		String cartella = null;
		String data = null;
		ISASConnection dbc = null;
		
		try {
			cartella = h.get("n_cartella").toString();
			data = h.get("data").toString();
		} catch (Exception e) {
			LOG.error(nomeEJB + "insert() [ " + e + " ] == Errore: manca la chiave primaria!!!");
			throw new SQLException("Errore: manca la chiave primaria");
		}
		
		try	{
			dbc = super.logIn(mylogin);
			// scrivo sc_bisogni
			dbc.startTransaction();
			ISASRecord dbr = dbc.newRecord("sc_bisogni");
			
			Enumeration n=h.keys();
			while(n.hasMoreElements()) {
				String e = n.nextElement().toString();
				if (h.get(e)!=null)
				dbr.put(e,h.get(e));
			}
			dbc.writeRecord(dbr);
			
			dbc.commitTransaction();
			
			dbr = queryKey(dbc, h);
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			LOG.error(nomeEJB + "insert() [ " + e + " ]");
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			LOG.error(nomeEJB + "insert() [ " + e + " ]");
			throw e;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw newEjbException("Errore eseguendo una insert() - ",  e);
		}
		finally	{
			if (!done){
				try{
					dbc.rollbackTransaction();
					dbc.close();
					super.close(dbc);
				}catch (Exception e){
					LOG.error("Errore effettuando una rollback");
				}
			}
			logout_nothrow("insert", dbc);
		}
	}

	private String getMax(String value1, String value2) {
		try{
		if (Integer.parseInt(value1)>=Integer.parseInt(value2)) return value1;
		else return value2;
		}catch(Exception e){
			e.printStackTrace();
			return "0";
		}
	}


	/* Viene chiamanta per controllare che una certa scheda possa essere inserita.
	   Non viene restituito NULL altrimenti vengono cancellati tutti i campi della form client.
	*/
	public ISASRecord query_insert(myLogin mylogin,Hashtable h) throws SQLException, ISASPermissionDeniedException 
	{		
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		
		try	{
			dbc = super.logIn(mylogin);
			dbr = queryKey(dbc, h);
			
			if(dbr == null)	{
				if ((h.get("tempo_t") != null) && (Integer.parseInt((String)h.get("tempo_t")) >= 0)) { 
					//� stupido ma per ora bisogna fare cos� per non sbiancare campi passati...
					// Mi procuro un dbr e ci metto i dati passati dal Client!
					dbr = dbc.newRecord("cartella");
					Enumeration n = h.keys();
					
					while(n.hasMoreElements()) {
						String e=(String)n.nextElement();
						dbr.put(e,h.get(e));
					}
					
					dbr.put("stato","insert");

					

					mySystemOut(" query_insert dbr chetorna..."+dbr.getHashtable().toString());
				}
			} else {
				dbr.put("operatore_desc",ISASUtil.getDecode(dbc,"operatori","codice",
						ISASUtil.getObjectField(dbr,"cod_operatore",'S'),
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));
			}
						
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{
			LOG.error(nomeEJB + "query_insert()", e);
			return null;
		}
		catch(Exception e)
		{
			LOG.error(nomeEJB + "query_insert()", e);
			throw newEjbException("Errore eseguendo una query_insert()  ", e);
		}
		finally	{
			logout_nothrow("query_insert", dbc);
		}
	}

	public ISASRecord update(myLogin mylogin, ISASRecord dbr)
			throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException {
		boolean done = false;
		String cartella = null;
		String data = null;
		ISASConnection dbc = null;
		Hashtable h = dbr.getHashtable();
		
		try {
			cartella = (String)dbr.get("n_cartella");
			data = (String)dbr.get("data");
		} catch (Exception e)	{
			System.out.println(nomeEJB + "update() [ " + e + " ]");
			throw new SQLException("Errore: manca la chiave primaria");
		}
		
		try	{
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			dbc.writeRecord(dbr);

			SclValutazioneEJB scl = new SclValutazioneEJB();
			ISASRecord dbr_siad = scl.queryKey(mylogin, h);
			Enumeration n=h.keys();
			while(n.hasMoreElements()) {
				String e = n.nextElement().toString();
				if (h.get(e)!=null)
				dbr_siad.put(e,h.get(e));
			}
			
			dbr_siad.put("codice_evento", eveUtl.getProgrxEveValutazione(dbc));
			
			dbc.writeRecord(dbr_siad);
			
			
			PrRugEJB rug = new PrRugEJB();
			ISASRecord dbr_rug = rug.queryKey(mylogin, h);
			
			n=h.keys();
			while(n.hasMoreElements()) {
				String e = n.nextElement().toString();
				if (h.get(e)!=null)
				dbr_rug.put(e,h.get(e));
			}
			dbr_rug.put("j3s",getMax(h.get("j3s_int").toString(),h.get("j3s_uri").toString()));
			
			dbc.writeRecord(dbr_rug);			
			
			dbc.commitTransaction();
			
			dbr = queryKey(dbc, h);			
			dbr.put("operatore_desc",ISASUtil.getDecode(dbc,"operatori","codice",
					ISASUtil.getObjectField(dbr,"cod_operatore",'S'),
					"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));
			
			dbc.close();
			super.close(dbc);
			done=true;
			
			
			return dbr;
		}
		catch (DBRecordChangedException e) {
			LOG.error(nomeEJB + "update() [ " + e + " ]");
			throw e;
		} catch (ISASPermissionDeniedException e) {
			LOG.error(nomeEJB + "update() [ " + e + " ]");
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw newEjbException(
					"Errore eseguendo una "+ nomeEJB + ".queryKey()  ", e);
		} finally {
			if (!done){
			try{
				dbc.rollbackTransaction();
				dbc.close();
				super.close(dbc);
			}catch (Exception e){
				LOG.error("Errore effettuando una rollback");
			}
		}
			logout_nothrow(nomeEJB+".queryKey", dbc);
		}
	}

	public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		try	{
			
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			Hashtable h = dbr.getHashtable();
			dbc.deleteRecord(dbr);
			SclValutazioneEJB scl = new SclValutazioneEJB();
			ISASRecord dbr_siad = scl.queryKey(mylogin, h);
			dbc.deleteRecord(dbr_siad);
			PrRugEJB rug = new PrRugEJB();
			ISASRecord dbr_rug = rug.queryKey(mylogin, h);
			dbc.deleteRecord(dbr_rug);
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			
		}
		catch(DBRecordChangedException e)
		{
			 System.out.println(nomeEJB + "delete() [ " + e + " ]");
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + "delete() [ " + e + " ]");
			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "delete() [ " + e + " ]");
			throw new SQLException("Errore eseguendo una delete() - ",  e);
		}
		finally	{
			if(!done) {
				try {
					dbc.rollbackTransaction();
					dbc.close();
					super.close(dbc);
				} catch(Exception e) {   
					System.out.println(nomeEJB + "delete() [ " + e + " ]");  
				}
			}
		}
	}

	// 08/07/09
	public Integer duplicaMaxRecord(myLogin mylogin, Hashtable h)
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

			cart = (String)h.get("n_cartella");
			dataDup = (String)h.get("data");// data in cui verr� duplicato il record
			codOper = (String)h.get("cod_operatore");
			tempoT = (String)h.get("tempo_t");

			gest_scaleVal.duplicaMaxRec(dbc, "sc_bisogni", cart, dataDup, codOper, tempoT);

			dbc.close();
			super.close(dbc);
			done=true;

			return new Integer(1);
		}
		catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
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



	
		
		
		
        public Integer DeleteValutaz(myLogin mylogin,Hashtable h) throws  SQLException
        {
			boolean done = false;
            ISASConnection dbc = null;
			int risu = 0;
			
			try	{
				dbc = super.logIn(mylogin);
				String cartella = (String) h.get("n_cartella");
				String tempo_t = (String)h.get("tempo_t");

				String myselect = "SELECT * FROM sc_bisogni WHERE ";
				String sel="";
				sel = su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
				sel	= su.addWhere(sel,su.REL_AND,"tempo_t",su.OP_EQ_NUM,tempo_t);
				myselect = myselect+sel;
				mySystemOut(" selectExistVal --  " + myselect);

				ISASRecord dbr = dbc.readRecord(myselect);

				if(dbr != null)	{
					delete(mylogin,dbr);
                    risu = 1;
				}
				
                dbc.close();
				super.close(dbc);
				done = true;
				
				return new Integer(risu);
			} catch(Exception e){
				System.out.println(nomeEJB + ".selectExistVal() [ " + e + " ]");
				throw new SQLException("Errore eseguendo una selectExistVal()  ");
			}
			finally	{
				if(!done) {
					try	{
						dbc.close();
						super.close(dbc);
					} catch(Exception e1){   
						System.out.println(nomeEJB + "queryKey() [ " + e1 + " ]");  
					}
				}
			}
        }

        public Integer selectExistVal(myLogin mylogin,Hashtable h) throws  SQLException
        {
			boolean done=false;
            ISASConnection dbc = null;
			int risu = 0;

			try	{
				dbc = super.logIn(mylogin);
				String cartella = (String) h.get("n_cartella");
				String tempo_t = (String)h.get("tempo_t");

				String myselect = "SELECT * FROM sc_bisogni WHERE ";
				String sel="";
				sel = su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
				sel = su.addWhere(sel,su.REL_AND,"tempo_t",su.OP_EQ_NUM,tempo_t);
				myselect = myselect+sel;
				mySystemOut(" selectExistVal --  " + myselect);

				ISASRecord dbr = dbc.readRecord(myselect);
				if(dbr != null)	
					risu = 1;

				dbc.close();
				super.close(dbc);
				done = true;
				
				return new Integer(risu);
			} catch(Exception e) {
				System.out.println(nomeEJB + ".selectExistVal() [ " + e + " ]");
				throw new SQLException("Errore eseguendo una selectExistVal()  ");
			}
			finally	{
				if(!done){
					try	{
						dbc.close();
						super.close(dbc);
					} catch(Exception e1){   
						System.out.println(nomeEJB + "queryKey() [ " + e1 + " ]");   
					}
				}
			}
        }

	private void mySystemOut(String msg)
  	{
		if (myDebug)
			System.out.println(nomeEJB + ": " + msg);
	}
	
		
	private ISASRecord getLastDiag(ISASConnection dbc,Hashtable h) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException{
		String critDtChius = "";
		if ((h.get("data")!= null) && (!h.get("data").toString().trim().equals(""))){
			String dataFin = h.get("data").toString();
			critDtChius += " AND d2.data_diag <= " + formatDate(dbc, dataFin);
		}

		String myselect = "SELECT d1.diag1, d1.diag2, d1.diag3 FROM diagnosi d1" +
						" WHERE d1.n_cartella = " + (String)h.get("n_cartella") +
                                                    " AND d1.data_diag IN "+
                                                    " (SELECT MAX(d2.data_diag) FROM diagnosi d2 WHERE "+
                                                    " d2.n_cartella=d1.n_cartella "+
						critDtChius +")";

		System.out.println("DiagnosiEJB: query_DiagnosiXSvama= [" + myselect + "]");

		ISASRecord dbr = dbc.readRecord(myselect);
        	return dbr;
		
	}


	public boolean esistePai(ISASConnection dbc, String nCartella, String idSkso) {
		String punto = ver  +"recuperaPai ";
		String query = "select count(*) numero from pai where n_cartella = " +nCartella +" and id_skso = "+ idSkso;
		LOG.debug(punto + " query>> "+query);
		ISASRecord dbrPai = null;
		int numeroPai = -1; 
		try {
			dbrPai = dbc.readRecord(query);
			numeroPai = ISASUtil.getValoreIntero(dbrPai, "numero");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (numeroPai > 0); 
	}
}
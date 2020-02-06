package it.caribel.app.sinssnt.bean.nuovi;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import oracle.jdbc.driver.OracleResultSet;
import oracle.sql.CLOB;

import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

public class SkMPalClinicaNewEJB extends SINSSNTConnectionEJB  {

	ServerUtility 		su = new ServerUtility();
	ManagerOperatore 	mo = new ManagerOperatore();
	private static final String ver ="1-";
	
	public SkMPalClinicaNewEJB() {}	
	
	public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, Exception {
		String nomeMetodo = "delete";
		ISASConnection dbc=null;
		ISASCursor dbcur=null;
		try{
			dbc=super.logIn(mylogin);
			
			//campi chiave
			String n_cartella 		= ""+dbr.get("n_cartella");
			String n_contatto 		= ""+dbr.get("n_contatto");
			String progr_inse 		= ""+dbr.get("progr_inse");
			String tipo_operatore 	= (String)dbr.get("tipo_operatore");
			
			String myselect=
					" select d.n_cartella, d.n_contatto, d.tipo_operatore," +
					" d.progr_inse,d.progr_modi" +
					" from skmpal_relcli_new d"+
					" where 0=0"+
					" and d.n_cartella="+n_cartella+
					" and d.n_contatto="+n_contatto+
					" and d.progr_inse="+progr_inse+
					" and d.tipo_operatore='"+tipo_operatore+"'";
			dbcur=dbc.startCursor(myselect);
			Vector<ISASRecord> vdbr=dbcur.getAllRecord();
			
			String myselectFine="";
			ISASRecord dbrCorr=null;
			for(int i=0; i<vdbr.size(); i++){
				myselectFine = myselect+ " and d.progr_modi="+((ISASRecord)vdbr.get(i)).get("progr_modi");
				dbrCorr = dbc.readRecord(myselectFine);
				dbc.deleteRecord(dbrCorr);
	    	}
			
		}catch(DBRecordChangedException e) {
			throw e;
		}catch(ISASPermissionDeniedException e){
			throw e;
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Exception:"+ e.getMessage());
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	public ISASRecord update(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, Exception{
		String nomeMetodo = "update";
		ISASConnection dbc = null;
		try {	
			/*
			 * ATTENZIONE: NON SI FA UPDATE DEL RECORD MA NE INSERISCO 
			 * SEMPRE UNO NUOVO PER CONSERVARE LO STORICO DELLE REVISIONI
			 */
			dbc=super.logIn(mylogin);
			
			Hashtable h = dbr.getHashtable();
			
			//campi chiave
			String n_cartella 		= ""+h.get("n_cartella");
			String n_contatto 		= ""+h.get("n_contatto");
			String progr_inse 		= ""+h.get("progr_inse");
			String tipo_operatore 	= (String)h.get("tipo_operatore");
			Integer progr_modi 		= calcolaProgrModi(dbc,n_cartella,n_contatto,tipo_operatore,progr_inse);
			
			h.put("progr_modi", progr_modi.toString());
			
			return insert(mylogin, h);
		}catch(DBRecordChangedException e){
			throw e;
		}catch(ISASPermissionDeniedException e){
			throw e;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	public ISASRecord insert(myLogin mylogin,Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String nomeMetodo = "insert";
		boolean done = false;
		ISASConnection dbc = null;
		PreparedStatement pstmt = null;
		try{
			dbc=super.logIn(mylogin);

			//campi chiave
			String n_cartella 		= ""+h.get("n_cartella");
			String n_contatto 		= ""+h.get("n_contatto");
			String progr_inse 		= ""+h.get("progr_inse");
			String progr_modi 		= ""+h.get("progr_modi");
			String tipo_operatore 	= (String)h.get("tipo_operatore");
						
			//altri campi
			String data_relcli 		= ""+h.get("data_relcli");
			String testo 			= (String)h.get("testo");
			String oggetto 			= (String)h.get("oggetto");
			String info_privata 	= ""+h.get("info_privata");
			String id_skso 			= ""+h.get("id_skso");
			
			if(id_skso.equals("null") || id_skso.trim().equals(""))
				id_skso="-1";
			
			//Ripulisco l'oggetto da eventuali apici
			ServerUtility su = new ServerUtility();
			oggetto = su.duplicateChar(oggetto,"'");
			
			//campi di servizio
			String op_inse          = ""; 
			String data_inse        = "";  
			String ora_inse         = "";
			String op_modi          = ""; 
			String data_modi        = "";  
			String ora_modi         = "";
			String codice_operatore	= CaribelSessionManager.getInstance().getStringFromProfile("codice_operatore");
			String dataOdierna 		= (String)su.getTodayDate("yyyy-MM-dd");
			String orario 			= (String)su.getTodayDate("HH:mm");
			if(progr_modi.trim().equals("") || progr_modi.equals("null") || progr_modi.trim().equals("0")){
				//NUOVO INSERIMENTO
				progr_inse = ""+calcolaProgrInse(dbc,n_cartella,n_contatto,tipo_operatore);
				progr_modi = "0";
				op_inse = codice_operatore;
				data_inse = dataOdierna;
				ora_inse = orario;
				op_modi   = ""; 
				data_modi = "";  
				ora_modi  = "";
			}else{
				op_inse   = ""+h.get("op_inse");
				data_inse = ""+h.get("data_inse");
				ora_inse  = ""+h.get("ora_inse");
				op_modi = codice_operatore;
				data_modi = dataOdierna;
				ora_modi = orario;
			}
			
			Connection jdbc = dbc.conn.getConnection();

			//############ INIZIO TRANSAZIONE ########################
			dbc.startTransaction();

			String sqlText =
					"INSERT INTO skmpal_relcli_new ("+
							"n_cartella," +
							"n_contatto," +
							"tipo_operatore,"+
							"progr_inse,"+   
							"progr_modi,"+   
							"op_inse,"+      
							"data_inse,"+    
							"ora_inse,"+     
							"op_modi,"+      
							"data_modi,"+    
							"ora_modi,"+     
							"data_relcli,"+  
							"oggetto,"+      
							"testo,"+        
							"info_privata,"+ 
							"id_skso)"
					+"VALUES("+
							n_cartella+"," +
							n_contatto+","+
							"'"+tipo_operatore+"'," +
							progr_inse+","+   
							progr_modi+","+
							"'"+op_inse+"'," +
							"TO_DATE('"+data_inse+"','yyyy-MM-dd'),"+
							"'"+ora_inse+"',"+ 
							"'"+op_modi+"'," +
							"TO_DATE('"+data_modi+"','yyyy-MM-dd'),"+
							"'"+ora_modi+"',"+ 
							"TO_DATE('"+data_relcli+"','yyyy-MM-dd'),"+
							"'"+oggetto+"',"+
							"EMPTY_CLOB(),"+    
							"'"+info_privata+"',"+ 
							id_skso+
							")";			
			pstmt = jdbc.prepareStatement(sqlText);
			pstmt.executeUpdate();

			String sql4update =
					" select * " +
					" from skmpal_relcli_new d"+
					" where 0=0"+
					" and d.n_cartella="+n_cartella+
					" and d.n_contatto="+n_contatto+
					" and d.tipo_operatore='"+tipo_operatore+"'"+
					" and d.progr_inse="+progr_inse+
					" and d.progr_modi="+progr_modi;
			LOG.debug("sql4update = " + sql4update);
			ResultSet rset = null;
			Statement stmt = jdbc.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
			try{
				rset = stmt.executeQuery(sql4update);
				rset.next();
				CLOB clob = ((OracleResultSet) rset).getCLOB("testo");
				clob.putString(1, testo);
			}finally{
				if(rset!=null)
					rset.close();
				if (stmt!=null)
					stmt.close();
			}


			dbc.commitTransaction();
			done=true;
			//############ FINE TRANSAZIONE ########################

			LOG.info(nomeMetodo+" -  Metodo eseguito");

			h.put("progr_inse", progr_inse);
			h.put("progr_modi", progr_modi);
			ISASRecord dbret = queryKey(mylogin, h);
			
			return dbret;
			
		}catch(DBRecordChangedException e){
			throw e;
		}catch(ISASPermissionDeniedException e){
			throw e;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			if (pstmt!=null)
				pstmt.close();
			if (!done)
				rollback_nothrow(nomeMetodo, dbc);
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	
	public ISASRecord queryKey(myLogin mylogin, Hashtable h) throws  Exception {
		String nomeMetodo = "queryKey";
		ISASConnection dbc=null;
		Statement stmt = null;
		ResultSet rset = null;
		try{
			String n_cartella 		= (String) h.get("n_cartella");
			String n_contatto 		= (String) h.get("n_contatto");
			String tipo_operatore 	= (String)h.get("tipo_operatore");
			String progr_inse 		= (String)h.get("progr_inse");
			String progr_modi 		= (String)h.get("progr_modi");
			
			String myselect=
					" select d.n_cartella, d.n_contatto, d.tipo_operatore," +
					" d.progr_inse,d.progr_modi," +
					" d.data_inse, d.op_inse,d.ora_inse," +
					" d.op_modi, d.data_modi, d.ora_modi," +
					" d.data_relcli, d.oggetto," +	//NB: non selezionare il CLOB o fare select *
					" d.info_privata, d.id_skso" +
					" from skmpal_relcli_new d"+
					" where 0=0"+
					" and d.n_cartella="+n_cartella+
					" and d.n_contatto="+n_contatto+
					" and d.tipo_operatore='"+tipo_operatore+"'"+
					" and d.progr_inse="+progr_inse+
					" and d.progr_modi="+progr_modi;
			
			LOG.debug(this.getClass().getName()+"."+nomeMetodo+": myselect=" + myselect);
			
			dbc=super.logIn(mylogin);
			ISASRecord dbr=dbc.readRecord(myselect);
			decodificaISASRecord(dbc, dbr, true);
			
			//Recupero il campo CLOB
			String strTesto="";
			myselect = 
					" select d.testo" +
					" from skmpal_relcli_new d" +
					" where 0=0"+
					" and d.n_cartella="+n_cartella+
					" and d.n_contatto="+n_contatto+
					" and d.tipo_operatore='"+tipo_operatore+"'"+
					" and d.progr_inse="+progr_inse+
					" and d.progr_modi="+progr_modi;
			stmt = dbc.conn.getConnection().createStatement();	
			rset = stmt.executeQuery(myselect);
			rset.next();
			Object testo = rset.getObject("testo");
			if (testo instanceof Clob) {
				Clob clob = (Clob) testo;
				long len = clob.length();
				LOG.debug("CLOB len = " + len);
				if (len > Integer.MAX_VALUE) {
					LOG.warn("il CLOB e' lungo " + len + " bytes, cioe' piu' di 2GB. restituisco i primi 2GB");
					len = Integer.MAX_VALUE;
				}
				strTesto = clob.getSubString(1, (int) len);
			}else{
				strTesto = testo.toString();
			}				
			dbr.put("testo", strTesto);

			return dbr;

		}catch(Exception e){
			LOG.error(nomeMetodo+" - Exception:"+ e.getMessage());
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			close_rset_nothrow("queryKey", rset);
			close_stmt_nothrow("queryKey", stmt);
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	public Vector<ISASRecord> query(myLogin mylogin,Hashtable h) throws  Exception {
		String nomeMetodo = "query";
		ISASConnection dbc=null;
		ISASCursor dbcur=null;
		try{
			String n_cartella 		= ""+h.get("n_cartella");
			String n_contatto 		= ""+h.get("n_contatto");
			String tipo_operatore 	= ""+h.get("tipo_operatore");
			String progr_inse 		= ""+h.get("progr_inse");			
			String dadata 			= ""+h.get("dadata");
			String adata 			= ""+h.get("adata");
			String id_skso  		= ""+h.get("id_skso");
			String op_inse 	        = ""+h.get("op_inse");

			String op_sessione		= CaribelSessionManager.getInstance().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
			
			dbc=super.logIn(mylogin);

			String myselect=
					" select d.n_cartella, d.n_contatto, d.tipo_operatore," +
					" d.progr_inse,d.progr_modi," +
					" d.data_inse, d.op_inse,d.ora_inse," +
					" d.op_modi, d.data_modi, d.ora_modi," +
					" d.data_relcli, d.oggetto," +	//NB: non selezionare il CLOB o fare select *
					" d.info_privata, d.id_skso" +
					" from skmpal_relcli_new d"+
					" where 0=0";

			if(n_cartella!=null && !n_cartella.trim().equals("")&& !n_cartella.trim().equals("null"))
				myselect += " and d.n_cartella = "+n_cartella;
//			if(n_contatto!=null && !n_contatto.trim().equals("")&& !n_contatto.trim().equals("null"))
//				myselect += " and d.n_contatto = "+n_contatto;
			if(id_skso!=null && !id_skso.trim().equals("")&& !id_skso.trim().equals("null"))
				myselect += " and (d.id_skso = "+id_skso+" or d.id_skso =-1)";
			if(tipo_operatore!=null && !tipo_operatore.trim().equals("")&& !tipo_operatore.trim().equals("null"))
				myselect += " and d.tipo_operatore = '"+tipo_operatore+"'";
			if(progr_inse!=null && !progr_inse.trim().equals("")&& !progr_inse.trim().equals("null")){
				//Si sta cercando lo storico delle revisioni
				myselect += " and d.progr_inse = "+progr_inse;
				myselect += " and d.n_contatto = "+n_contatto;
			}else{
				myselect += 
						" and d.progr_modi = ("+
						"   select max (d1.progr_modi) progr_modi " +
						"   from skmpal_relcli_new d1"+
						"   where 0=0"+
						"   and d1.n_cartella=d.n_cartella"+
						"   and d1.n_contatto=d.n_contatto"+
						"   and d1.tipo_operatore=d.tipo_operatore"+
						"   and d1.progr_inse=d.progr_inse"+
						" )";
			}
			
			if(dadata != null && !dadata.equals("") && !dadata.equals("null"))
				myselect += " and d.data_relcli >= " + formatDate(dbc, dadata);
			if(adata != null && !adata.equals("") && !adata.equals("null"))
				myselect += " and d.data_relcli <= " + formatDate(dbc, adata);
			if(op_inse!=null && !op_inse.trim().equals("")&& !op_inse.trim().equals("null"))
				myselect += " and d.op_inse = '"+op_inse+"'";
			
			//I diari privati devono essere visibili solo all'operatore che li ha inseriti
			myselect += 
					" and (" +
					"  (d.info_privata = 'N')" +
					"  or " +
					"  (d.info_privata = 'S' and d.op_inse = '"+op_sessione+"')" +
					" ) ";
			
			myselect=myselect+" ORDER BY data_relcli desc, progr_inse, progr_modi desc";

			LOG.trace(nomeMetodo+" - myselect: "+myselect);
			dbcur=dbc.startCursor(myselect,200);
			Vector<ISASRecord> vdbr=dbcur.getAllRecord();
			
			vdbr = decodificaVectorISASRecord(dbc, vdbr);

			return vdbr;

		}catch(Exception e){
			e.printStackTrace();
			LOG.error(nomeMetodo+" - Exception:"+ e.getMessage());
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	
	
	private ISASRecord decodificaISASRecord(ISASConnection dbc,ISASRecord dbr,boolean aggiungiDettagliScheda)throws Exception{
		String nomeMetodo = "decodificaISASRecord";
		try{
			if(dbr!=null){			
				
				//decodifica operatore
				dbr.put("op_inse_descr",ISASUtil.getDecode(dbc,"operatori","codice",""+dbr.get("op_inse"),
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","op_inse_descr"));
				
				dbr.put("op_modi_descr",ISASUtil.getDecode(dbc,"operatori","codice",""+dbr.get("op_modi"),
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","op_modi_descr"));
				
				//decodifica tipo operatore
				String tipo_operatore_descr = ManagerOperatore.decodificaTipoOperatore(dbc, ""+dbr.get("tipo_operatore"), Costanti.TAB_VAL_SO_TIPO_OPERATORE);
				dbr.put("tipo_operatore_descr", tipo_operatore_descr);
				
				if(aggiungiDettagliScheda){
					
				}
			}
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT[dbr,"+aggiungiDettagliScheda+"]");
			return dbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	private Vector<ISASRecord> decodificaVectorISASRecord(ISASConnection dbc,Vector<ISASRecord> vdbr)throws Exception{
		String nomeMetodo = "decodificaVectorISASRecord";
		try{
			for (int i =0;i<vdbr.size();i++ ) {
				Object obj = vdbr.get(i);
				if(obj instanceof ISASRecord){
					ISASRecord dbr = (ISASRecord)vdbr.get(i);	
					dbr = (ISASRecord)vdbr.elementAt(i);
					dbr = decodificaISASRecord(dbc, dbr,false);
				}
			}
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT["+vdbr.size()+"] OUTPUT["+vdbr.size()+"]");
			return vdbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	private Integer calcolaProgrInse(ISASConnection dbc,String n_cartella,String n_contatto,String tipo_operatore)throws Exception{
		String nomeMetodo = "calcolaProgrInse";
		try{
			int mass = 1;
			String selmax = "SELECT MAX (progr_inse) massimo FROM"+
					" skmpal_relcli_new WHERE 0=0 "+
					" AND n_cartella = " + n_cartella+
					" AND n_contatto = " + n_contatto+
					" AND tipo_operatore = '" + tipo_operatore+"'";
			ISASRecord dbmax = dbc.readRecord(selmax);
			if (dbmax!=null && dbmax.get("massimo")!=null){
				Integer massimo = (Integer)dbmax.get("massimo");
				mass = massimo.intValue()+1;
			}
			return new Integer(mass);
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	private Integer calcolaProgrModi(ISASConnection dbc,String n_cartella,String n_contatto,String tipo_operatore,String progr_inse)throws Exception{
		String nomeMetodo = "calcolaProgrModi";
		try{
			int mass = 1;
			String selmax = "SELECT MAX (progr_modi) massimo " +
					" FROM skmpal_relcli_new " +
					" WHERE 0=0 "+
					" AND n_cartella = " + n_cartella+
					" AND n_contatto = " + n_contatto+
					" AND progr_inse = " + progr_inse+
					" AND tipo_operatore = '" + tipo_operatore+"'";
			ISASRecord dbmax = dbc.readRecord(selmax);
			if (dbmax!=null && dbmax.get("massimo")!=null){
				Integer massimo = (Integer)dbmax.get("massimo");
				mass = massimo.intValue()+1;
			}
			return new Integer(mass);
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}

	
	public static Boolean updateIdSkso(ISASConnection dbc,Hashtable h)throws Exception{
		String nomeMetodo = "updateIdSkso";
		
		ISASCursor dbcur = null;
		boolean ret = false,done = false;
		try {	
			
			//campi chiave
			String n_cartella 		= h.get("n_cartella").toString();
			String n_contatto 		= h.get("n_contatto").toString();
			String tipo_operatore 	= h.get("tipo_operatore").toString();
			String id_skso			= h.get("id_skso").toString();
			
			String sql = "select n_cartella, n_contatto,tipo_operatore,progr_inse,progr_modi,op_inse," +
					"data_inse,ora_inse,op_modi,data_modi,ora_modi,data_relcli,oggetto,info_privata," +
					"id_skso from skmpal_relcli_new where n_cartella = "+n_cartella+
						" and n_contatto = "+n_contatto+
						" and tipo_operatore = '"+tipo_operatore+"'";
			dbcur = dbc.startCursor(sql);
			
			
			if (dbcur!=null && dbcur.getDimension()>0){
				while (dbcur.next()){
					ISASRecord dbr_cur = (ISASRecord)dbcur.getRecord();
					ISASRecord dbr = dbc.readRecord("select n_cartella, n_contatto,tipo_operatore,progr_inse,progr_modi,op_inse," +
					"data_inse,ora_inse,op_modi,data_modi,ora_modi,data_relcli,oggetto,info_privata," +
					"id_skso from skmpal_relcli_new where n_cartella = "+dbr_cur.get("n_cartella").toString()+
						" and n_contatto = "+dbr_cur.get("n_contatto").toString()+
						" and tipo_operatore = '"+dbr_cur.get("tipo_operatore").toString()+"'"+
						" and progr_inse = " + dbr_cur.get("progr_inse").toString()+
						" and progr_modi = " + dbr_cur.get("progr_modi").toString());
					if (dbr!=null){
						dbr.put("id_skso", id_skso);
						dbc.writeRecord(dbr);
					}
					
				}
			}		
			
			if (dbcur!=null)dbcur.close();
			done = true;
			return new Boolean(ret);
		}catch(DBRecordChangedException e){
			throw e;
		}catch(ISASPermissionDeniedException e){
			throw e;
		}catch(Exception e){
			throw new Exception("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			if (!done){
				if (dbcur!=null)dbcur.close();
			}
		
		}
	}

	public boolean esisteDiario(ISASConnection dbc, String nCartella, String idSkso) {
		String punto = ver  +"esisteDiario ";
		
		StringBuffer query = new StringBuffer();
		query.append("select count(*) numero from skmpal_relcli_new  where n_cartella = ");
		query.append(nCartella);
		query.append(" and id_skso = ");
		query.append(idSkso);
		query.append(" and info_privata <>'S'");
		LOG.debug(punto + " query>> "+ query);
		ISASRecord dbrDiario = null;
		int numeroNote = -1; 
		try {
			dbrDiario = dbc.readRecord(query.toString());
			numeroNote = ISASUtil.getValoreIntero(dbrDiario, "numero");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return (numeroNote > 0); 
	}
	
}

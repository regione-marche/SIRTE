package it.caribel.app.sinssnt.bean;
//**
//********NUOVA RELEASE GIUGNO 2012 bargi versione 12_03_01

//bargi 16/05/2011 inserito gestore eventi per la registrazione interventi ... 
// ATTENZIONE AL MOMENTO SI PREVEDE CHE IN AGENDA SONO PRESENTI SOLO INTERVENTI DOMICILIARI!
//************************************************************************************************************************************
////10/12/2007 aggiunto caso stato 9 PUA bargi
//9/06/04 - EJB di connessione alla procedura SINS sinss
//richiamata dai moduli client:
//--->JFrameAgendaRegistra per la lettura delle prestazioni in agenda e per l'eventuale registrazione.
//--->JFrameAgendaRegistraPopUp per l'inserimento di nuove prestazioni non programmate
//e per la registrazione prestazione per prestazione
//bargi
//==========================================================================

import it.caribel.app.sinssnt.bean.nuovi.RMDiarioEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.StripTags;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.GestErogazione;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
public class AgendaRegistraEJB extends SINSSNTConnectionEJB
{
	myLogin mylogin_global=null;
	private GestErogazione gestore_erog = new GestErogazione();
	
	private final Log LOG = LogFactory.getLog(getClass());
	static public final java.text.SimpleDateFormat dateFormatClient = new java.text.SimpleDateFormat("dd-MM-yyyy");
	
	public AgendaRegistraEJB() {}
//	Metodi richiamati
	/**
	 JFrameAgendaRegistra ---->  registra_prestaz
	 
	 JFrameAgendaRegistraPopUp ----> registra_prestaz_cella
	 */
//	*************SELEZIONATA COLONNA
//	registrazione accessi e prestazioni e aggiornamenti flag stati vari****************
	/**
	 *   Esegue le seguenti operazioni:
	 */
	public String registra_prestaz(myLogin mylogin, Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException,CariException
	{
		boolean done = false;
		mylogin_global=mylogin;
		ISASConnection dbc = null;
		String methodName = "registra_prestaz";
		try{
			dbc = super.logIn(mylogin);
			debugMessage("AgendaRegistra/registra_prestaz: apro");
			//debugMessage("hashtable passata="+h.toString());
			
			dbc.startTransaction();
			Hashtable ht=(Hashtable)h.get("appuntamenti");
			Enumeration eDate=ht.keys();
			while(eDate.hasMoreElements()){
				String data=(String)eDate.nextElement();
				Hashtable hC=(Hashtable)ht.get(data);
				Enumeration eCart=hC.keys();
				while(eCart.hasMoreElements()){
					String cartella=(String)eCart.nextElement();
					Vector vP=(Vector)hC.get((cartella));
					aggiornamenti(dbc,data,cartella,vP,h);
				}
			}
			Vector<String> giorni = (Vector<String>) h.get("giorni");
			Hashtable tempiKm = (Hashtable) h.get("tempiKm");
			String data;
			Hashtable<String, Object> hpar = new Hashtable<String, Object>();
			hpar.put("ag_oper_esec", h.get("esecutivo"));
			String km;
			String mm;
			for (int i = 0; i < giorni.size(); i++) {
				km = (String) tempiKm.get("km"+i);
				mm = (String) tempiKm.get("mm"+i);
				if(km!=null && mm!=null && !km.isEmpty() && !mm.isEmpty()){
					data = giorni.get(i);
					hpar.put("ag_oper_esec", h.get("esecutivo"));
					hpar.put("ag_data", data);
					hpar.put("ag_km", km);
					hpar.put("ag_minuti", mm);
					insertOrUpdate(dbc, hpar);
				}
			}
			
//			dbc.rollbackTransaction();
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			debugMessage("AGENDA: REGISTRA_PRESTAZ:CHIUDO");
			done = true;
			String msg="Operazione completata con successo.";
			return msg;
		}catch(CariException ce){
			ce.setISASRecord(null);
			try	{
				LOG.error("AgendaRegistra.registra_prestaz() => ROLLBACK");
				rollback_nothrow("AgendaRegistra.registra_prestaz() => ROLLBACK", dbc);
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1, e1);
			}
			throw ce;
		}catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName + ": " + e.getMessage(), e);
		}finally {
			if (!done) {
				rollback_nothrow("AgendaRegistraEJB.registra_prestaz", dbc);
			}
			logout_nothrow(methodName, dbc);
		}
	}
	
	private void insertOrUpdate(ISASConnection dbc, Hashtable<String, Object> h) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException, DBRecordChangedException, ParseException {
		String data=(String)h.get("ag_data");
		String dataISAS =  data.substring(6,10)+"-"+data.substring(3,5)+"-"+data.substring(0,2);
		String mysel ="SELECT * "+
		" from AGENDANT_TEMPIKM"+ //gb 17/09/07
		" where ag_data="+formatDate(dbc,data)+
		" and AG_OPER_ESEC='"+((String)h.get("ag_oper_esec")).trim()+"'";
		debugMessage("AgendaRegistra/aggiornamenti, SELECT mysel: "+mysel);
		ISASRecord dbr=dbc.readRecord(mysel);

		if (dbr == null){
			dbr = dbc.newRecord("AGENDANT_TEMPIKM");
			dbr.put("ag_data", dataISAS);
			dbr.put("ag_oper_esec", h.get("ag_oper_esec"));
		}
		dbr.put("ag_km", h.get("ag_km"));
		dbr.put("ag_minuti", h.get("ag_minuti"));
		dbc.writeRecord(dbr);
	}
	
	private void aggiornamenti(ISASConnection dbc,String data,String cartella,Vector progressivi,Hashtable h)throws SQLException{
		try{
			Enumeration en=progressivi.elements();
			while(en.hasMoreElements())
			{
				String prog=(String)en.nextElement();
				String mysel ="SELECT * "+
//				gb 17/09/07				" from agenda_interv"+
				" from agendant_interv"+ //gb 17/09/07
				" where ag_data="+formatDate(dbc,data)+
				" and ag_progr="+prog+
				" and ag_stato in (0,3,9)"+
				" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
				debugMessage("AgendaRegistra/aggiornamenti, SELECT mysel: "+mysel);
				ISASRecord dbr=dbc.readRecord(mysel);
				if (dbr != null)
				{
					Hashtable h_interv=dbr.getHashtable();
					h_interv.put("esecutivo",h.get("esecutivo"));
					aggiornoAgInterv(dbc,h_interv,2);
					//debugMessage("record letto="+dbr.getHashtable().toString());
					int contatore=inseriscoInterv(dbc,h_interv);
					int numPrest=leggoAgIntpre(dbc, dbr,contatore);
					String anno=(((java.sql.Date)(dbr.get("ag_data"))).toString()).substring(0,4);
					aggiornoInterv(dbc,contatore,anno,numPrest);
				}
			}
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una aggiornamenti() - "+  e1, e1);
		}
	}
	
	private void aggiornoAgInterv(ISASConnection dbc,Hashtable h_interv,int stato)throws SQLException{
		try{
			String prog="";
			if (h_interv.get("ag_progr") instanceof Integer)
				prog=((Integer)h_interv.get("ag_progr")).toString();
			else prog=(String)h_interv.get("ag_progr");
			
			String mysel = "SELECT *" +
//			gb 18/09/07			    " from agenda_interv" +
			" from agendant_interv" + //gb 18/09/07
			" where ag_data="+formatDate(dbc,((java.sql.Date)h_interv.get("ag_data")).toString())+
			" and ag_progr="+prog+
			" and ag_oper_ref='"+((String)h_interv.get("ag_oper_ref")).trim()+"'";
			debugMessage("AgendaRegistra/aggiornoAgInterv, SELECT mysel: "+mysel);
			ISASRecord dbr=dbc.readRecord(mysel);
			if (dbr != null){
				dbr.put("ag_stato",""+stato);
				dbr.put("ag_oper_esec",(String)h_interv.get("esecutivo"));
				dbc.writeRecord(dbr);
				LOG.info("aggiornoAgInterv " + mysel);//elisa b 15/05/12
			}//debugMessage("SELECT agenda_interv aggiornato");
			else{//Se non ci sono previsti interventi in agenda ne inserisco uno
				dbr = dbc.newRecord("agendant_interv");
				dbr.put("ag_data", (java.sql.Date)h_interv.get("ag_data"));
				dbr.put("ag_progr", new Integer(prog));
				dbr.put("ag_oper_ref", ISASUtil.getValoreStringa(h_interv, "ag_oper_ref"));
				dbr.put("ag_cartella", ISASUtil.getValoreStringa(h_interv, "ag_cartella"));
				dbr.put("ag_contatto", ISASUtil.getValoreStringa(h_interv, "ag_contatto"));
				dbr.put("cod_obbiettivo", ISASUtil.getValoreStringa(h_interv, "cod_obbiettivo"));
				dbr.put("n_intervento", ISASUtil.getValoreStringa(h_interv, "n_intervento"));
				dbr.put("ag_tipo_oper", ISASUtil.getValoreStringa(h_interv, "tipo_operatore"));
				dbr.put("ag_orario", new Integer(ISASUtil.getValoreStringa(h_interv, "ag_orario")));
				dbr.put("ag_oper_esec", ISASUtil.getValoreStringa(h_interv, "esecutivo"));
				dbr.put("ag_stato", ""+stato);
				dbc.writeRecord(dbr);
				LOG.info("aggiornoAgIntpre " + mysel);
			}
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una aggiornoAgInterv() - "+  e1, e1);
		}
	}
	
	private int inseriscoInterv(ISASConnection dbc,Hashtable h_interv)throws SQLException, CariException{
		ISASConnection dbc2=null;
		String strTipoOper = "";
		try{
			String data=((java.sql.Date)(h_interv.get("ag_data"))).toString();
			//String data=(String)(h_interv.get("ag_data"));
			String anno=data.substring(0,4);
			debugMessage("AgendaRegistra/inseriscoInterv: apro DBC2 PER PROGRESSIVO");
			dbc2 = super.logIn(mylogin_global);
			dbc2.startTransaction();
			int contatore=selectProgressivo(dbc2,"INTERV"+anno);
			dbc2.commitTransaction();
			dbc2.close();
			super.close(dbc2);
			debugMessage("AgendaRegistra/inseriscoInterv: CHIUDO");
			debugMessage("AgendaRegistra/inseriscoInterv, ATTENZIONE IL CONTATORE PER INTERV E'= "+contatore);
			ISASRecord dbr=dbc.newRecord("interv");
			dbr.put("int_anno",anno);
			dbr.put("int_contatore",new Integer(contatore));
			Hashtable h_anag=new Hashtable();
			
			if (h_interv.get("ag_cartella") instanceof Integer)
			{
				dbr.put("int_cartella",(Integer)h_interv.get("ag_cartella"));
				h_anag=selectAnagra(dbc,((Integer)h_interv.get("ag_cartella")).toString());
			}
			else
			{
				dbr.put("int_cartella",(String)h_interv.get("ag_cartella"));
				h_anag=selectAnagra(dbc,(String)h_interv.get("ag_cartella"));
			}
			/*gb 17/09/07 *******
			 if (h_interv.get("ag_contatto") instanceof Integer)
			 dbr.put("int_contatto",(Integer)h_interv.get("ag_contatto"));
			 else
			 dbr.put("int_contatto",(String)h_interv.get("ag_contatto"));
			 *gb 17/09/07: fine *******/
			
//			gb 17/09/07 *******
			if (h_interv.get("ag_contatto") instanceof Integer)
			{
				strTipoOper = (String)h_interv.get("ag_tipo_oper"); //gb 18/09/07
				if (!strTipoOper.equals("01"))
				{
					dbr.put("int_contatto", (Integer)h_interv.get("ag_contatto"));
					dbr.put("n_progetto", new Integer(0));
					dbr.put("cod_obbiettivo", "00000000");
					dbr.put("n_intervento", new Integer(0));
				}
				else
				{
					dbr.put("int_contatto", new Integer(0));
					dbr.put("n_progetto", (Integer)h_interv.get("ag_contatto"));
					dbr.put("cod_obbiettivo", (String)h_interv.get("cod_obbiettivo"));
					dbr.put("n_intervento", (Integer)h_interv.get("n_intervento"));
				}
			}
			else
			{
				strTipoOper = (String)h_interv.get("tipo_operatore"); //gb 18/09/07
				if (!strTipoOper.equals("01"))
				{
					dbr.put("int_contatto",(String)h_interv.get("ag_contatto"));
					dbr.put("n_progetto", new Integer(0));
					dbr.put("cod_obbiettivo", "00000000");
					dbr.put("n_intervento", new Integer(0));
				}
				else
				{
					dbr.put("int_contatto", new Integer(0));
					dbr.put("n_progetto", new Integer((String)h_interv.get("ag_contatto")));
					dbr.put("cod_obbiettivo", (String)h_interv.get("cod_obbiettivo"));
					dbr.put("n_intervento", new Integer((String)h_interv.get("n_intervento")));
				}
			}
//			gb 17/09/07: fine *******
			
			dbr.put("int_data_prest",(java.sql.Date)h_interv.get("ag_data"));
//			gb 17/09/07            Hashtable h_op =selectTipoOpe(dbc,(String)h_interv.get("esecutivo"));
//			gb 17/09/07 *******
			Hashtable h_op =selectTipoOpe(dbc,(String)h_interv.get("esecutivo"), strTipoOper);
//			gb 17/09/07: fine *******
			dbr.put("int_tipo_prest",selectTipoPrest(dbc,"TIPDEF"+(String)h_op.get("tipo")));
			dbr.put("int_cod_oper",(String)h_interv.get("esecutivo"));
			dbr.put("int_qual_oper",h_op.get("cod_qualif"));
			dbr.put("int_tipo_oper",(String)h_op.get("tipo"));
			dbr.put("int_cod_areadis",h_anag.get("dom_areadis"));
			dbr.put("int_cod_comune",h_anag.get("dom_citta"));
			// 09/06/10 m ---
			dbr.put("int_cod_res_areadis",h_anag.get("areadis"));
			dbr.put("int_cod_res_comune",h_anag.get("citta"));			
			// 09/06/10 m ---
			dbr.put("int_codpres",h_op.get("cod_presidio"));
			dbr.put("int_coduf",h_op.get("unita_funz"));
			dbr.put("int_ambdom","D");
			dbr.put("int_flag_flussi",new Integer(0));
			dbr.put("int_maxprest",new Integer(1));
			if(h_interv.containsKey("testoPulito")){
				dbr.put("int_note",(String)h_interv.get("testoPulito"));
			}
			dbc.writeRecord(dbr);
			LOG.info("AgendaRegistra.inserito interv contatore="+contatore);
			//bargi 16/05/2011
			int risu = segnalaErogazione(dbc, dbr, true);
			return contatore;
		} catch(CariException ce){
			ce.setISASRecord(null);
			try	{
					LOG.error("AgendaRegistra.update() => ROLLBACK");
					dbc.rollbackTransaction();
			}catch(Exception e1){
					throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una inseriscoInterv() - "+  e1, e1);
		}
	}
	// 28/10/09 m.: x gestione Flussi AD-RSA
	//bargi 16/05/2011 inserito anche in agenda
	private int segnalaErogazione(ISASConnection mydbc, ISASRecord mydbr, boolean isInsert) throws Exception, CariException
	{
		// 16/07/10: si comunicano solo le EROGAZIONI DOMICILIARI
		if ((mydbr.get("int_ambdom") != null) && (!"D".equals((String)mydbr.get("int_ambdom")))) {
			LOG.info("--- IntervEJB.segnalaErogazione: Accesso NON DOMICILIARE quindi NON si segnala EROGAZIONE ---");
			return 1;
		}

		Hashtable h_par = new Hashtable();
		h_par.put("int_cartella", mydbr.get("int_cartella"));
		h_par.put("int_data_prest", mydbr.get("int_data_prest"));
		h_par.put("int_anno", mydbr.get("int_anno"));
		h_par.put("int_contatore", mydbr.get("int_contatore"));
		h_par.put("int_tipo_oper", mydbr.get("int_tipo_oper"));
		h_par.put("int_cod_oper", mydbr.get("int_cod_oper"));
		// 02/08/11
		h_par.put("int_qual_oper", mydbr.get("int_qual_oper"));

		int risu = -1;
		if (isInsert)
			risu = gestore_erog.insert(mydbc, h_par);
		else
			risu = gestore_erog.update(mydbc, h_par);

		if (risu <= 0)
			LOG.info("::: AgendaRegistraEJB.segnalaErogazione - NON segnalata EROGAZIONE! :::risu===>"+risu);
		return risu;
	}
	private void aggiornoInterv(ISASConnection dbc,int contatore,String anno,int numPrest)throws SQLException,CariException{
		try{
			String select="select * from interv "+
			"where int_anno='"+anno+"' and "+
			" int_contatore="+contatore;
			debugMessage("AgendaRegistra/aggiornoInterv, select= "+select);
			ISASRecord dbr=dbc.readRecord(select);
			dbr.put("int_max_prest",new Integer(numPrest));
			dbc.writeRecord(dbr);
			//bargi 16/05/2011
			int risu = segnalaErogazione(dbc, dbr, false);
		}catch(CariException ce){
			ce.setISASRecord(null);
			try	{
					LOG.error("AgendaRegistra.update() => ROLLBACK");
					dbc.rollbackTransaction();
			}catch(Exception e1){
					throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una aggiornoInterv() - "+  e1);
		}
	}
	
	private int leggoAgIntpre(ISASConnection dbc,ISASRecord mydbr,int contatore) throws Exception
	{
		Hashtable h = mydbr.getHashtable();
		boolean done=false;
		String data = ((java.sql.Date)h.get("ag_data")).toString();
		int prog = ((Integer)h.get("ag_progr")).intValue();
		String oper=((String)h.get("ag_oper_ref")).trim();
		ISASCursor dbcur=null;
		try{
			String mysel = "SELECT * "+
//			gb 17/09/07                	" FROM agenda_intpre "+
			" FROM agendant_intpre " + //gb 17/09/07
			" where ap_data="+formatDate(dbc,data)+
			" and ap_oper_ref='"+oper+"'"+
			" and ap_progr="+prog;
			debugMessage("AgendaRegistra/leggoAgIntpre, mysel= "+mysel);
			dbcur=dbc.startCursor(mysel);
			int maxprest=1;
			while (dbcur.next())
			{
				ISASRecord dbrc=dbcur.getRecord();
				if(dbrc!=null)
				{
					Hashtable h_intpre=dbrc.getHashtable();
					aggiornoAgIntpre(dbc,h_intpre);
					inseriscoIntpre(dbc,h_intpre,contatore,maxprest);
				}
				maxprest++;
			}
			if (dbcur!=null) dbcur.close();
			done=true;
			return maxprest-1;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una leggoAgIntpre() - "+  e1);
		}
		finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e2){debugMessage(""+e2);}
			}
		}
	}// END selectAgendaIntpre
	
	private void aggiornoAgIntpre(ISASConnection dbc,Hashtable h_intpre )throws SQLException{
		try{
			String prog="";
			String qta="";
			if (h_intpre.get("ap_progr") instanceof Integer)
				prog=((Integer)h_intpre.get("ap_progr")).toString();
			else prog=(String)h_intpre.get("ap_progr");
			if (h_intpre.get("ap_prest_qta") instanceof Integer)
				qta=((Integer)h_intpre.get("ap_prest_qta")).toString();
			else qta=(String)h_intpre.get("ap_prest_qta");
			String mysel ="SELECT *" +
//			gb 17/09/07			" from agenda_intpre"+
			" from agendant_intpre"+ //gb 17/09/07
			" where ap_data="+formatDate(dbc,((java.sql.Date)h_intpre.get("ap_data")).toString())+
			" and ap_progr="+prog+
			" and ap_prest_cod='"+((String)h_intpre.get("ap_prest_cod")).trim()+"'"+
			" and ap_oper_ref='"+((String)h_intpre.get("ap_oper_ref")).trim()+"'";
			debugMessage("AgendaRegistra/aggiornoAgIntpre, mysel: "+mysel);
			ISASRecord dbr=dbc.readRecord(mysel);
			if (dbr != null){
				dbr.put("ap_stato",new Integer(1));
				dbr.put("ap_prest_qta",new Integer(qta));
				dbc.writeRecord(dbr);
				LOG.info("aggiornoAgIntpre " + mysel);//elisa b 15/05/12
			}
			else{
//				gb 17/09/07                ISASRecord rec_ag = dbc.newRecord("agenda_intpre");
				ISASRecord rec_ag = dbc.newRecord("agendant_intpre"); //gb 17/09/07
				rec_ag.putHashtable(h_intpre); //copia la cartella
				rec_ag.put("ap_data", (java.sql.Date)h_intpre.get("ap_data"));
				rec_ag.put("ap_progr", new Integer(prog));
				rec_ag.put("ap_prest_qta",new Integer(qta));
				rec_ag.put("ap_prest_cod",((String)h_intpre.get("ap_prest_cod")).trim());
				rec_ag.put("ap_oper_ref",((String)h_intpre.get("ap_oper_ref")).trim());
				rec_ag.put("ap_stato",new Integer(1)); //la prestazione è stata registrata
				rec_ag.put("ap_alert",new Integer(-1)); //prestazione occasionale
				dbc.writeRecord(rec_ag);
				LOG.info("aggiornoAgIntpre " + mysel);//elisa b 15/05/12
			}
			
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una aggiornoAgIntpre() - "+  e1);
		}
	}
	
	private int inseriscoIntpre(ISASConnection dbc,Hashtable h_intpre,int contatore,int prog)throws SQLException{
		try{
			String data=((java.sql.Date)(h_intpre.get("ap_data"))).toString();
			String anno=data.substring(0,4);
			String qta="";
			if (h_intpre.get("ap_prest_qta") instanceof Integer)
				qta=((Integer)h_intpre.get("ap_prest_qta")).toString();
			else qta=(String)h_intpre.get("ap_prest_qta");
			
			ISASRecord dbr=dbc.newRecord("intpre");
			dbr.put("pre_anno",anno);
			dbr.put("pre_contatore",new Integer(contatore));
			dbr.put("pre_progr",new Integer(prog));
			dbr.put("pre_numero",new Integer(qta));
			dbr.put("pre_cod_prest",(String)h_intpre.get("ap_prest_cod"));
			dbr.put("pre_des_prest",selectPrestaz(dbc,(String)h_intpre.get("ap_prest_cod")));
			dbr.put("pre_note", (String)h_intpre.get("testo"));
			debugMessage("AgendaRegistra/inseriscoIntpre, scrivo su intpre.."+dbr.getHashtable().toString());
			
			dbc.writeRecord(dbr);
			return contatore;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una inseriscoIntpre() - "+  e1);
		}
	}
	
//	**********SELEZIONATA CELLA*****************************************************
	public String registra_prestazCella(myLogin mylogin, Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException,CariException
	{
		boolean done = false;
		ISASConnection dbc = null;
		String methodName = "registra_prestazCella";
		try{
			dbc = super.logIn(mylogin);
			LOG.info("AgendaRegistra/registra_prestazCella: apro");
			mylogin_global=mylogin;
			//debugMessage("************inizio cella**************");
			LOG.info("AgendaRegistra/registra_prestazCella: hashtable arrivata="+h.toString());
			//VFR In alcuni casi arriva già una data invece di una stringa 
			//introdotta la gestione di tutti i casi
//			java.sql.Date w=java.sql.Date.valueOf((String)h.get("ag_data"));
			Object tmp = h.get("ag_data");
			java.sql.Date w = null;
			if(tmp instanceof String){
				w=java.sql.Date.valueOf((String) tmp);
			}else if(tmp instanceof java.util.Date){
				w = new java.sql.Date(((java.util.Date) tmp).getTime());
				debugMessage("Passata una data invece che una stringa" );
	            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
	            for (int i=0; i < trace.length; i++)
	                debugMessage("\tat " + trace[i]);
			}else if(tmp instanceof java.sql.Date){
				w = (java.sql.Date) tmp;
				debugMessage("Passata una data invece che una stringa" );
	            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
	            for (int i=0; i < trace.length; i++)
	                debugMessage("\tat " + trace[i]);
			}
			h.put("ag_data",w);
			h.put("ag_oper_ref",h.get("referente"));
			
			String s = (String) h.get("testo");
			if(s!=null){
				String s1 = StripTags.strip(s);
				String s2 = StripTags.stripNewLines(s1);
				String s3 = StripTags.stripBlanks(s2);
				LOG.trace(s3);
				h.put("testoPulito", s3);
			}
			
			dbc.startTransaction();
			//debugMessage("inseriscointerv="+h.toString());
			int contatore=inseriscoInterv(dbc,h);
			String progressivoAgenda = ISASUtil.getValoreStringa(h, "ag_progr");
			if(progressivoAgenda.isEmpty() || progressivoAgenda.equals("00")){
				int prog = new AgendaEditEJB().inseriscoAgendaInterv(dbc, h);
				h.put("ag_progr", ""+prog);
			}
			
			//String anno=((String)(h.get("ag_data"))).substring(0,4);
			String anno=(((java.sql.Date)(h.get("ag_data"))).toString()).substring(0,4);
			
			Vector v_prest=(Vector)h.get("prestazioni");
			Vector v_quant=(Vector)h.get("quantita");
			Enumeration en=v_prest.elements();
			int numPrest=1;
			Hashtable<String, Object> h_prest=new Hashtable<String, Object>();
			int ind=0;
			while(en.hasMoreElements())
			{
				String pr =(String)en.nextElement();
				h_prest.put("ap_prest_cod",pr);
				h_prest.put("ap_prest_qta",(String)v_quant.elementAt(ind));
				ind++;
				h_prest.put("ap_data",h.get("ag_data"));
				h_prest.put("ap_progr",h.get("ag_progr"));
				h_prest.put("ap_oper_ref",h.get("referente"));
				h_prest.put("ap_cartella", h.get("ag_cartella"));
				if(h.containsKey("testoPulito")){
					h_prest.put("testo", h.get("testoPulito"));
				}
				//debugMessage("aggiornoagintpre="+h_prest.toString());
				aggiornoAgIntpre(dbc,h_prest);
				//debugMessage("inseriscointpre="+h_prest.toString());
				inseriscoIntpre(dbc,h_prest,contatore,numPrest);
				
				numPrest++;
			}
			//le prestazioni su agenda_intpre potrebbero
			//essere inferiori perche ci sono le prestazioni
			//nuove non programmate
			int conta=contaAgIntpre(dbc,h_prest);
			numPrest=numPrest-1;
			//debugMessage("Numero record su agenda_inpre="+conta);
			//debugMessage("Numero record aggiornati="+numPrest);
			int stato=0;
			if(conta==numPrest) 
				stato=2; 
			else 
				stato=1;
			//se la prestazione inserita è quella di non eseguito imposto tutto l'accesso a non eseguito (4)
			String prestazioneNonEseguito = selectTipoPrest(dbc, ManagerProfile.AG_NN_ES);
			if(v_prest.size()==1 && v_prest.get(0).equals(prestazioneNonEseguito+h.get("tipo_operatore"))){
				stato=4;
			}
			//debugMessage("aggiornoaginterv="+h.toString());
			aggiornoAgInterv(dbc,h,stato);
			aggiornoInterv(dbc,contatore,anno,numPrest);
			//debugMessage("************fine**************");
			aggiornoDiario(mylogin, dbc, h);
			dbc.commitTransaction();
			debugMessage("AGENDA: REGISTRAPRESTAZ:CHIUDO");
			done = true;
			String msg="Operazione completata con successo.";
			return msg;
		}catch(CariException ce){
			ce.setISASRecord(null);
			try	{
					LOG.error("AgendaRegistra.registra_prestazCella() => ROLLBACK");
					dbc.rollbackTransaction();
			}catch(Exception e1){
					throw new CariException("Errore eseguendo la rollback() - " +  e1, e1);
			}
			throw ce;
		}catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName + ": " + e.getMessage(), e);
		}finally {
			if (!done) {
				rollback_nothrow("AgendaRegistraEJB.registra_prestaz", dbc);
			}
			logout_nothrow(methodName, dbc);
		}
	}
	
	
	private void aggiornoDiario(myLogin mylogin, ISASConnection dbc, Hashtable h) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		if(h.containsKey("oggetto")){
			RMDiarioEJB diario = new RMDiarioEJB();
			diario.insert(mylogin, h);
		}
	}
	
	private int contaAgIntpre(ISASConnection dbc,Hashtable h) throws Exception
	{
		String data = ((java.sql.Date)h.get("ap_data")).toString();
		String prog = (String)h.get("ap_progr");
		String oper=((String)h.get("ap_oper_ref")).trim();
		try{
			String mysel = "SELECT count(*) tot "+
//			gb 18/09/07                " FROM agenda_intpre "+
			" FROM agendant_intpre " + //gb 18/09/07
			" where ap_data="+formatDate(dbc,data)+
			" and ap_oper_ref='"+oper+"'"+
			" and ap_progr="+prog;
			debugMessage("AgendaRegistra/contaAgIntpre, mysel: "+mysel);
			ISASRecord dbr=dbc.readRecord(mysel);
			int t=0;
			if(dbr!=null){
				t=convNumDBToInt("tot",dbr);
				//debugMessage("num record presenti in agenda intpre="+t);
			}
			return t;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaRegistra: Errore eseguendo una contaAgIntpre() - "+  e1);
		}
		
	}// END contaAgendaIntpre
//	*************
	private String selectPrestaz(ISASConnection dbc,String chiave)
	throws SQLException
	{
		try{
			String select="select prest_des from prestaz "+
			" where prest_cod='"+chiave+"'";
			ISASRecord is_dbr = dbc.readRecord(select);
			if(is_dbr!=null){
				return(String)is_dbr.get("prest_des");
			}
			return "";
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una selectPrestaz() - "+  e1);
		}
	}
	
	private String selectTipoPrest(ISASConnection dbc,String chiave)
	throws SQLException
	{
		try{
			String tipo="";
			String selectTipo="select conf_txt from conf where conf_key='"+chiave+"'";
			ISASRecord is_dbr = dbc.readRecord(selectTipo);
			if(is_dbr!=null){
				tipo=(String)is_dbr.get("conf_txt");
			}
			return tipo;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una selectTipoPrest() - "+  e1);
		}
	}
	
	private Hashtable selectAnagra(ISASConnection dbc,String cartella)throws SQLException{
		try{
			String mysel ="SELECT dom_areadis,dom_citta"+
			", areadis, citta" + // 09/06/10 m
			" from anagra_c"+
			" where n_cartella="+cartella+
			" and data_variazione in (select max(data_variazione)"+
			" from anagra_c where n_cartella="+cartella+")";
			
			ISASRecord dbr=dbc.readRecord(mysel);
			if (dbr != null){
				return dbr.getHashtable();
			}
			return null;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una selectAnagra() - "+  e1);
		}
		
	}
	
//	gb        private Hashtable selectTipoOpe(ISASConnection dbc,String codice)throws SQLException{
	private Hashtable selectTipoOpe(ISASConnection dbc,String codice, String strTipoOper)
	throws SQLException{
		try{
//			gb             String mysel = "SELECT tipo,cod_qualif,cod_presidio,unita_funz" +
			String mysel = "SELECT cod_qualif,cod_presidio,unita_funz" + //gb
			" from operatori"+
			" where codice='"+codice+"'";
			ISASRecord dbr=dbc.readRecord(mysel);
			if (dbr != null){
				dbr.put("tipo", strTipoOper); //gb
				return dbr.getHashtable();
			}
			return null;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una selectAgendaInterv() - "+  e1);
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

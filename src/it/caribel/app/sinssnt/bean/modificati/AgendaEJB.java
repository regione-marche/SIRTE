package it.caribel.app.sinssnt.bean.modificati;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//********NUOVA RELEASE GIUGNO 2012 bargi versione 12_03_01

// 25/09/2008 bargi inserito collegamento a import it.pisa.caribel.sinssnt.controlli.CaricaAgendaPrestazioni;
// 25/08/2008 bargi corretto controllo del tempo corretto per caricare la prestazione in agenda se la data � superiore 
//  al limite minimo di caricamento va caricato.
// 9/06/04 - EJB di connessione alla procedura SINS sinss
// richiamata dai moduli client:
//--->JFrameAgenda per la lettura delle settimane caricate
//		e per caricare in agenda le prestazioni programmate
//--->JFrameAgendaRegistra per la lettura delle prestazioni in agenda
//--->JFrameAgendaRegistraPopUp per la lettura delle prestazioni della cella e per caricamento tabella prestazioni
//--->JFrameAgendaModifica per la lettura delle prestazioni in agenda
//--->JFrameAgendaModificaPopUp per la lettura delle prestazioni della cella per caricamento tabella prestazioni
// bargi
// ==========================================================================


import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.util.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.sinssnt.controlli.CaricaAgendaPrestazioni;

public class AgendaEJB extends SINSSNTConnectionEJB
{
	String messaggio_carica="";
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
//myLogin mylogin_global=null;
	CaricaAgendaPrestazioni carica = new CaricaAgendaPrestazioni(); //bargi 25/09/2008
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	public AgendaEJB() {}
//Metodi richiamati
/**
JFrameAgenda		 ---->	query_caricati
							carica_agenda

JFrameAgendaRegistra ---->  query_agenda
							isSettCaricata

JFrameAgendaRegistraPopUp ----> query_agendapopup
								CaricaTabellaPrestioni
JFrameAgendaModifica ---->  query_agenda
							isSettCaricata

JFrameAgendaModificaPopUp ----> query_agendapopup
								CaricaTabellaPrestioni
JFrameAgendaModOperatore ---->  query_agenda
								isSettCaricata

JFrameAgenda(...da fare!) ----> query_agendapopup
								CaricaTabellaPrestioni
*/
/**
*   Esegue le seguenti operazioni:
	*   1) seleziona da agenda_interv i giorni caricati in modo da evidenziarli
*/
	public Vector query_caricati(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
	{
        boolean done = false;
		ISASConnection dbc = null;
		try{
			LOG.debug("AGENDA: query_caricati parametri "+h.toString());
			dbc = super.logIn(mylogin);
			Vector vdbr = new Vector();
			vdbr=selectGiorni(dbc,h);
			dbc.close();
			super.close(dbc);
			debugMessage("AGENDA: query_caricati:CHIUDO");
			done = true;
			return vdbr;
		}catch(DBRecordChangedException e){
			System.out.println("AgendaEJB.query_caricati(): Eccezione= " + e);
			throw new DBRecordChangedException("Errore eseguendo una query_caricati() - "+  e);
		}catch(ISASPermissionDeniedException e){
			System.out.println("AgendaEJB.query_caricati(): Eccezione= " + e);
			throw new ISASPermissionDeniedException("Errore eseguendo una query_caricati() - "+  e);
		}catch(Exception e){
			System.out.println("AgendaEJB.query_caricati(): Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query_caricati() - " +  e);
		}finally{
			if (!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("AgendaEJB.query_caricati (): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}

	}
 private Vector selectGiorni(ISASConnection dbc,Hashtable h) throws SQLException {
	boolean done=false;
        ISASCursor dbcur=null;
	try{
		String myselect="Select distinct ag_data from agendant_interv "+
                 "where ag_oper_ref='"+(String)h.get("referente")+"'"+    
          	    " and ag_tipo_oper='"+((String)h.get("tipo_operatore")).trim()+"'"+//aggiunto bargi 19/06/2012   
                 " order by ag_data";
		dbcur=dbc.startCursor(myselect);
		LOG.debug("selectGiorni caricati "+myselect);
		Vector v=new Vector();
		Vector vdbr=dbcur.getAllRecord();
		if ((vdbr != null) && (vdbr.size() > 0))
		for(int i=0; i<vdbr.size(); i++) {
			
			ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
			if (dbrec != null){
			  String data=((java.sql.Date)dbrec.get("ag_data")).toString();
			  LOG.debug("selectGiorni vsett= "+i+" data="+data);
			  if (!v.contains(data)) {
				caricaSettimane(data,v);//
				//v=giorniSettimana((Vector)v.clone(),data);
				LOG.debug("selectGiorni caricato vsett= "+v.size());
			  }
			  
			}
		}
		  LOG.debug("selectGiorni vsett finale= "+v.size());
		dbcur.close();
		done=true;
		return v;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una selectGiorni()  ");
   	}finally{
		if(!done){
 			try{
				if(dbcur!=null) dbcur.close();
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}

}

        /**
	*   Esegue le seguenti operazioni:
        *   1) seleziona da agenda_interv con primo giorno sett per vedere se
        *   eventualmente la settiman sia già stata caricata e quindi in questo caso
        *   da errore
	*   1) seleziona da agenda_sett_tipo le prestazioni pianificate
        *   nella settimana inerenti all'operatore referente
        *   2)carica agenda ma controlla la data fine e se inferiore alla data
        *   da caricare
        *   non viene caricato
        *
	*/
	public String carica_agenda(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
		{
        	boolean done = false;
		ISASConnection dbc = null;
        	String msg="";
        	 msg=carica.carica_agenda(mylogin,h);//bargi 25/09/2008
        	 return msg;       
	}
	
    /**
	*  
	*/
	public String carica_agenda_operatori(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
		{
        	boolean done = false;
    		ISASConnection dbc = null;
    		LOG.info("AgendaEJB. carica_agenda_operatori "+h.toString());
        	   try{
						dbc = super.logIn(mylogin);
						dbc.startTransaction();
						String msg="";
						boolean rimuovi=false;
						if(h.get("rimuovi")!=null && h.get("rimuovi").toString().equals("S"))
							rimuovi=true;
						Vector v_operatori=null;
						if(h.get("v_operatori")!=null)v_operatori=(Vector)h.get("v_operatori");
						if(v_operatori!=null) {
							Hashtable h_par=new Hashtable();
							h_par.put("data_inizio_ag", h.get("data_inizio"));
							h_par.put("data_fine_ag", h.get("data_fine"));
							h_par.put("tipo_operatore", h.get("tipo_operatore"));
							Enumeration en=v_operatori.elements();
							while (en.hasMoreElements()) {
								String oper_esec=(String)en.nextElement();
								h_par.put("referente", oper_esec);
								msg=carica.carica_agenda_apartireda( dbc, h_par,true,rimuovi);
							}
							msg="Operazione eseguita correttamente";
						}
						else msg="operatori non presenti caricamento non riuscito";
					     dbc.commitTransaction();
					      dbc.close();
							super.close(dbc);
							 return msg;       					    
        	   }catch(DBRecordChangedException e){
       			System.out.println("AgendaEJB.carica_agenda_operatori(): Eccezione= " + e);
    			try{
    				System.out.println("AgendaEJB.carica_agenda_operatori() => ROLLBACK");
    				dbc.rollbackTransaction();
    			}catch(Exception e1){
    				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
    			}
    			throw e;
    		}catch(ISASPermissionDeniedException e){
    			System.out.println("AgendaEJB.carica_agenda_operatori(): Eccezione= " + e);
    			try{
    				System.out.println("AgendaEJB.carica_agenda_operatori() => ROLLBACK");
    				dbc.rollbackTransaction();
    			}catch(Exception e1){
    				throw new ISASPermissionDeniedException("Errore eseguendo una carica_agenda_operatori() - "+  e);
    			}
    			throw e;
    		}catch(Exception e){
    			System.out.println("AgendaEJB.carica_agenda_operatori(): Eccezione= " + e);
    			try{
    				System.out.println("AgendaEJB.carica_agenda_operatori() => ROLLBACK");
    				dbc.rollbackTransaction();
    			}catch(Exception e1){
    				throw new SQLException("Errore eseguendo una rollback() - " +  e1);
    			}
    			throw new SQLException("Errore eseguendo una query_agenda() - " +  e);
    		}finally{
    			if (!done){
    				try{
    					dbc.close();
    					super.close(dbc);
    				}catch(Exception e2){
    					System.out.println("AgendaEJB.carica_agenda_operatori (): - Eccezione nella chiusura della connessione= " + e2);
    				}
    			}
    		}

    	}
	/**
	*   Esegue le seguenti operazioni:
        *   1) seleziona da agendant_interv con primo giorno sett per vedere se
        *   eventualmente la settimana sia gi� stata caricata e quindi in questo caso
        *   va restituita
	*   1) seleziona da agendant_sett_tipo le prestazioni pianificate
        *   nella settimana inerenti all'operatore referente
        *   2)(NO)controlla la data fine e se inferiore alla data da caricare
        *   non viene caricato e viene rimosso da agengant_sett_tipo
        *
	*/
	public Vector query_agenda(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
			{
		String methodName = "query_agenda";
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			debugMessage("AgendaEJB/query_agenda: Hashtable input: "+h.toString());
			dbc.startTransaction();
			Vector vdbr = new Vector();
			Vector v=(Vector)h.get("giorni");
			//						debugMessage("query_agenda *");
			if(ManagerProfile.isConfigurazioneMarche(CaribelSessionManager.getInstance())){
				vdbr=selectAgendaInterv(dbc,h);
			}else{
				if (isAgCaricata(dbc,h,v)){
				//		debugMessage("query_agenda **");
				vdbr=selectAgendaInterv(dbc,h);
				//		debugMessage("query_agenda ***");
				}else{
					debugMessage("Non esistono record");
					//vdbr.addElement("");
					//vdbr=null;
				}
			}
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			debugMessage("AGENDA: query_AGENDA:CHIUDO");
			done = true;
			// String msg="Operazione completata con successo.";
			return vdbr;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(methodName, dbc);
		}

			}
	
	public Hashtable<String, Object> query_tempiKm(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException{
		String methodName = "query_tempiKm";
		ISASConnection dbc = null;
		boolean done = false;
		Hashtable<String, Object> ret = new Hashtable<String, Object>();
		try{
			dbc = super.logIn(mylogin);
			LOG.debug("AgendaEJB/query_tempiKm: Hashtable input: "+h.toString());
			Vector v=(Vector)h.get("giorni");
			String op= ((String)h.get("referente")).trim();
			String primo_gg=(String)v.elementAt(0);
			String ultimo_gg=(String)v.elementAt(6);

			Integer km, mm;
			String mysel;
			for (int i = 0; i < 7; i++) {
				mysel = "SELECT * FROM AGENDANT_TEMPIKM " +
						" WHERE AG_OPER_ESEC = '"+ op + "' " +
						" and ag_data = " +formatDate(dbc,(String)v.elementAt(i));
				ISASRecord dbr=dbc.readRecord(mysel);
				if(dbr != null){
					km = (Integer) dbr.get("ag_km");
					mm = (Integer) dbr.get("ag_minuti");
					ret.put("km"+i, km);
					ret.put("mm"+i, mm);
				}else{
					ret.put("km"+i, "");
					ret.put("mm"+i, "");
				}
			}
			dbc.close();
			super.close(dbc);
			LOG.debug("AGENDA: query_tempiKm:CHIUDO");
			done = true;
			return ret;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName + ": " + e.getMessage(), e);
		}finally {
			if (!done) {
				rollback_nothrow("AgendaEJB.query_tempiKm", dbc);
			}
			logout_nothrow(methodName, dbc);
		}
	}
	
        private boolean isAgCaricata(ISASConnection dbc,Hashtable h,Vector v)throws SQLException{
               boolean done=false;
               String primo_gg=(String)v.elementAt(0);
               String ultimo_gg=(String)v.elementAt(6);
           try{
//gb 12/09/07             String mysel ="SELECT COUNT(*) tot from agenda_interv "+
             String mysel = "SELECT COUNT(*) tot from agendant_interv " + //gb 12/09/07
                    	    " where ag_data>="+formatDate(dbc,primo_gg)+
                    	    " and ag_data<="+formatDate(dbc,ultimo_gg)+
                    	    " and ag_oper_ref='"+((String)h.get("referente")).trim()+"'"+
                    	    " and ag_tipo_oper='"+((String)h.get("tipo_operatore")).trim()+"'";//aggiunto bargi 19/06/2012

            //debugMessage("SELECT agenda_interv"+mysel);
            ISASRecord dbr=dbc.readRecord(mysel);
            int t=0;
            if(dbr!=null){
              t=util.getIntField(dbr,"tot");
	      //t=convNumDBToInt("tot",dbr);
              }
            if(t==0)return false;
            else return true;
            }catch(Exception e1){
              debugMessage(""+e1);
              throw new SQLException("Errore eseguendo una isAgCaricata() - "+  e1);
            }

        }


        @SuppressWarnings({ "rawtypes", "unchecked" })
		private Vector selectAgendaInterv(ISASConnection dbc,Hashtable h)throws SQLException{
        	String methodName="selectAgendaInterv";
//        	boolean done=false;
        	ISASCursor dbcur=null;
        	Vector v=(Vector)h.get("giorni");
        	String primo_gg=(String)v.elementAt(0);
        	String ultimo_gg=(String)v.elementAt(6);
        	try{
        		String mysel ="SELECT  anti.*," +
        				//bargi 21112008 per triplette "ag_cartella, ag_oper_ref,"+
        				//gb 13/09/07             "ag_contatto, ag_orario, ag_data, ag_progr, ag_stato,"+
        				//gb 13/09/07 *******
        				//           bargi 21112008 per triplette" ag_contatto, anti.cod_obbiettivo, anti.n_intervento, ag_orario, ag_data, ag_progr, ag_stato,"+
        				//gb 13/09/07: fine *******
        				" nvl(trim(cognome),'') || ' ' ||  nvl(trim (nome),'') assistito,"+
        				/** 12/02/13 mv SEGN 33723             
			 " nvl(trim(dom_indiriz),'') || ' ' ||  nvl(trim (comuni.descrizione),'')"+
			" || ' ' ||  nvl(trim (dom_prov),'')  indirizzo"+
        				 **/ 
        				 " nvl(trim (comuni.descrizione),'') || ' ' || DECODE(prov_rep, NULL, '', '('||TRIM(PROV_REP)||')')"+
        				 " || ' - ' || nvl(trim(indirizzo_rep),'') indirizzo";
        		String myfrom = 
        				 //gb 13/09/07             " from cartella,anagra_c,agenda_interv,comuni "+
        				 " from cartella, anagra_c, agendant_interv anti, comuni "; //gb 13/09/07
        		String mywhere = 
        				 " where ag_data>="+formatDate(dbc,primo_gg)+
        				 " and ag_data<="+formatDate(dbc,ultimo_gg)+
//        				 " and ag_oper_ref='"+((String)h.get("referente")).trim()+"'"+
        				 " and ag_tipo_oper='"+((String)h.get("tipo_operatore")).trim()+"'"+//aggiunto bargi 19/06/2012
        				 //gb 13/09/07             " and cartella.n_cartella=agenda_interv.ag_cartella"+
        				 " and cartella.n_cartella = anti.ag_cartella"+
        				 " and data_variazione in (select max(data_variazione) from anagra_c where "+
        				 " anagra_c.n_cartella = anti.ag_cartella)"+
        				 " and anagra_c.n_cartella = anti.ag_cartella"+
        				 " and comuni.codice=comune_rep";
        				 //gb 13/09/07             " order by ag_cartella, ag_contatto, ag_orario, ag_data";
        	if(h.containsKey("referente") && !((String)h.get("referente")).isEmpty()){
        		mywhere += " and ag_oper_ref='"+((String)h.get("referente")).trim()+"'" ;
        	}else{
//        		myfrom += ", operatori o ";
        		mywhere += " and EXISTS (select o.codice from operatori o " +
        				" where 1=1 " +
        				" and o.cod_presidio = '"+((String)h.get("presidio_op")).trim()+"'" +
        				" and o.tipo = '"+((String)h.get("tipo_operatore")).trim()+"'"+
        				"and ag_oper_ref = o.codice " +
        				") ";
        	}
        	
        	if(h.containsKey("sk_motivo") && !ISASUtil.getValoreStringa(h, "sk_motivo").equals("T")){
        		Hashtable<String, String> parmotivi = getParametriMotivi(((String)h.get("tipo_operatore")).trim());
        		mysel   += " , CONTATTO."+ parmotivi.get("chiave") + " AS sk_motivo ";
        		myfrom  += " , "+ parmotivi.get("tabella") + " contatto ";
        		mywhere += 	" and contatto.n_cartella = ag_cartella and contatto.n_contatto = ag_contatto " +
        					" and contatto."+ parmotivi.get("chiave") + "= '"+((String)h.get("sk_motivo")).trim()+"' " ;
        	}

        	if(h.containsKey("ag_stato") && !ISASUtil.getValoreStringa(h, "ag_stato").equals("T")){
        		mywhere += " and ag_stato IN ("+((String)h.get("ag_stato")).trim()+") " ;
        		if(ISASUtil.getValoreStringa(h, "ag_stato").equals("2")){
        			mywhere += " and AG_OPER_ESEC = '"+((String)h.get("esecutivo")).trim()+"' " ;
        		}
        	}

        	
        	mysel += myfrom + mywhere +
             //elisa b 08/06/11: cambiato l'ordinamento " order by ag_cartella, ag_contatto, anti.cod_obbiettivo, anti.n_intervento, ag_orario, ag_data";
             //bargi 15/02/2012 " order by cognome,nome";
             //bargi 15/02/2012 
             " order by cognome,nome,ag_cartella, ag_oper_ref, ag_contatto, anti.cod_obbiettivo, anti.n_intervento, ag_orario, ag_data";
        		debugMessage("agendaEJB/selectAgendaInterv, mysel: "+mysel);
        		dbcur=dbc.startCursor(mysel);
        		Vector vdbr = dbcur.getAllRecord();
        		Vector rigatabella=new Vector();
        		ISASRecord recriga=null;
        		String chiave_new="";
        		String chiave_old="";
        		int index=0;
        		boolean sec=false;
        		String concatprestazioni = "";
        		int numprestazioni = 0;
        		int numprelievi = 0;
        		// devo scorrere il vettore per decodificare l'operatore
        		int countPrestazioni[] = new int[]{0,0,0,0,0,0,0};
        		int countPrelievi[] = new int[7];
        		
        		//seleziono da conf le prestazioni che sono prelievi
    			String selectPrelievi = " select conf_txt from conf where conf_kproc='SINS' and conf_key='"+ManagerProfile.PRESTPREL+"'";
    			ISASRecord tmp = dbc.readRecord(selectPrelievi);
    			String prelievi = (String) tmp.get("conf_txt");
        		
        		if ((vdbr != null) && (vdbr.size() > 0)){
        			for(int i=0; i<vdbr.size(); i++){
        				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
        				//debugMessage("record i="+i+" rec letto="+dbrec.getHashtable().toString());
        				if (dbrec != null){
        					String referente = (String)dbrec.get("ag_oper_ref");
        					String data=((java.sql.Date)dbrec.get("ag_data")).toString();
        					//						   debugMessage("selectAgendaInterv ******");
        					String gg=scorroVett(data,v);//ritorna lun mar etc
        					dbrec.put("data"+gg,data);
        					dbrec.put("progr"+gg,((Integer)dbrec.get("ag_progr")).toString());
        					dbrec.put("stato"+gg,(String)dbrec.get("ag_stato"));
        					chiave_new=((Integer)dbrec.get("ag_orario")).toString()+
        							((Integer)dbrec.get("ag_cartella")).toString()+
        							((Integer)dbrec.get("ag_contatto")).toString() +
        							((String)dbrec.get("ag_oper_ref")).toString() +
        							//gb 13/09/07 *******
        							(String)dbrec.get("cod_obbiettivo") +
        							((Integer)dbrec.get("n_intervento")).toString();//+
        					debugMessage("AgendaEJB/selectAgendaInterv: chiave_new--> ["+chiave_new+"]");
        					debugMessage("AgendaEJB/selectAgendaInterv: chiave_old--> ["+chiave_old+"]");
        					//gb 13/09/07: fine *******
        					//no � pippo   ((Integer)dbrec.get("ag_progr")).toString();
        					dbrec.put("tipoPrestazioni", ISASUtil.getValoreStringa(h, "tipoPrestazioni"));
        					dbrec.put("esecutivo", ISASUtil.getValoreStringa(h, "esecutivo"));
        					
        					//inserisco le prestazioni che sono identificate come prelievi per effettuare 
        					//il conteggio e l'eventuale filtraggio
        					dbrec.put("prelievi", prelievi);
        					if (!chiave_new.equals(chiave_old)){
        						if(!chiave_old.equals("") && !concatprestazioni.isEmpty()){
        							//debugMessage("carico in vettore=");
        							//debugMessage((recriga.getHashtable()).toString());
        							rigatabella.add(recriga);
        							concatprestazioni = "";
        							//debugMessage("vettore=="+rigatabella.toString());
        						}
        						chiave_old=chiave_new;
        						selectAgendaIntpre(dbc, dbrec);
        						recriga=dbrec;
        						//debugMessage("rec "+(recriga.getHashtable()).toString());
        						recriga.put(gg,(String)dbrec.get("prestazioni"));
        						//debugMessage("nuova riga dopo="+(recriga.getHashtable()).toString());
        						dbrec.put("sk_motivo", ricercaMotivo(dbc, dbrec));
        						index++;
        					}else{//stessa chiave
        						//debugMessage("stessa riga prima="+(recriga.getHashtable()).toString());
        						selectAgendaIntpre(dbc, dbrec);
        						recriga.put(gg,(String)dbrec.get("prestazioni"));
        					}
        					concatprestazioni += (String)dbrec.get("prestazioni"); 
        					recriga.put("data"+gg,data);
        					recriga.put("progr"+gg,((Integer)dbrec.get("ag_progr")).toString());
        					recriga.put("stato"+gg,(String)dbrec.get("ag_stato"));
        					recriga.put("prestazioni_desc"+gg,(String)dbrec.get("prestazioni_desc"));
        					recriga.put("ag_oper_ref_desc", decodificaGenerica("operatori", "codice", referente, "cognome", dbc) + " " +
        												    decodificaGenerica("operatori", "codice", referente, "nome", dbc) );
        					numprestazioni = (Integer) dbrec.get("numprestazioni");
        					numprelievi  = (Integer) dbrec.get("numprelievi");
        					if(numprestazioni>0){
        						countPrestazioni[Integer.parseInt(gg)]+= 1;
        					}
        					if(numprelievi>0){
        						countPrelievi[Integer.parseInt(gg)]+= 1;
        					}
        					//debugMessage("stessa riga dopo="+(recriga.getHashtable()).toString());
        				}
        			}
        		}
        		if ((vdbr != null) && (vdbr.size() > 0) && !concatprestazioni.isEmpty()){
        			//debugMessage("ultima riga che carico in vettore=");
        			//debugMessage((recriga.getHashtable()).toString());
        			rigatabella.add(recriga);
        		}
//        		if (dbcur != null)dbcur.close();
//        		done = true;
        		if(rigatabella != null && (rigatabella.size()>0)){
        			ISASRecord primo = ((ISASRecord) rigatabella.get(0));
        			for (int i = 0; i < countPrestazioni.length; i++) {
        				primo.put("numprestazioni"+i, countPrestazioni[i]);
        				primo.put("numprelievi"+i, countPrelievi[i]);
					}
        		}
        		
        		
        		return rigatabella;
    		} catch(Exception e){
    			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
    		}finally {
    			close_dbcur_nothrow(methodName, dbcur);
//    			logout_nothrow(methodName, dbc);
    		}
        }

        private Hashtable<String, String> getParametriMotivi(String tipo_oper) {
        	Hashtable<String, String> result = new Hashtable<String, String>();
        	if(tipo_oper.equals(GestTpOp.CTS_COD_INFERMIERE)){
        		result.put("chiave", "ski_motivo");
        		result.put("tabella", "skinf");
        	} else if(tipo_oper.equals(GestTpOp.CTS_COD_MEDICO)){
        		result.put("chiave", "skm_motivo");
        		result.put("tabella", "skmedico"); 
        	} else if(tipo_oper.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)){
        		result.put("chiave", "skf_motivo");
        		result.put("tabella", "skfis"); 
        	} else {
        		result.put("chiave", "skfpg_motivo");
        		result.put("tabella", "SKFPG");
        	}

        	return result;
        }
	private String ricercaMotivo(ISASConnection dbc, ISASRecord dbrec) throws Exception {
    		String tipo_oper = ISASUtil.getValoreStringa(dbrec,"ag_tipo_oper");
    		String cartella  = ISASUtil.getValoreStringa(dbrec,"ag_cartella");
    		String contatto  = ISASUtil.getValoreStringa(dbrec,"ag_contatto");
    		String sql = "";
    		if(tipo_oper.equals(GestTpOp.CTS_COD_INFERMIERE)){
    			sql = " SELECT ski_motivo as sk_motivo from skinf where n_cartella = " + cartella + " and " + " n_contatto = " + contatto;
    		} else if(tipo_oper.equals(GestTpOp.CTS_COD_MEDICO)){
    			sql = " SELECT skm_motivo as sk_motivo from skmedico where n_cartella = " + cartella + " and " + " n_contatto = " + contatto;
    		} else if(tipo_oper.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)){
    			sql = " SELECT skf_motivo as sk_motivo from skfis where n_cartella = " + cartella + " and " + " n_contatto = " + contatto;
    		} else {
    			sql = " SELECT skfpg_motivo as sk_motivo from SKFPG where n_cartella = " + cartella + " and " + " n_contatto = " + contatto;
    		}

    		ISASRecord tmp = dbc.readRecord(sql);
			return ISASUtil.getValoreStringa(tmp, "sk_motivo");
		}
	@SuppressWarnings("unchecked")
	private void selectAgendaIntpre(ISASConnection dbc,ISASRecord mydbr) throws Exception{
    	ISASCursor dbcur=null;
//        	boolean done=false;
    	try{
			String prelievi = (String) mydbr.get("prelievi");
    		
    		Hashtable<String, Object> h = mydbr.getHashtable();
    		String data = ((java.sql.Date)h.get("ag_data")).toString();
    		String oper = ((String)h.get("ag_oper_ref")).trim();
    		int prog = ((Integer)h.get("ag_progr")).intValue();
    		String mysel = "SELECT ap_prest_cod, ap_alert" +
    				//gb 13/09/07				" FROM agenda_intpre "+
    				" FROM agendant_intpre "+ //gb 13/09/07
    				" WHERE ap_data="+formatDate(dbc,data)+
    				" AND ap_oper_ref='"+oper+"'"+
    				" AND ap_progr="+prog;
    		String tipoPrestazione = ISASUtil.getValoreStringa(mydbr, "tipoPrestazioni");
   			if(tipoPrestazione.equals("P")){
   				mysel += " AND ap_prest_cod IN ( " + prelievi + ")"; 
   			}else if(tipoPrestazione.equals("A")){
   				mysel += " AND ap_prest_cod NOT IN ( " + prelievi + ")";
    		}
    		
    		debugMessage("AgendaEJB/selectAgendaIntpre mysel="+mysel);
    		dbcur=dbc.startCursor(mysel);
    		String prestazioni="";
    		String descPrestazioni="";
    		String sep="";
    		String sep2="";
    		String prestazione = "";
    		int numprelievi = 0;
    		while (dbcur.next()){
    			ISASRecord dbrc=dbcur.getRecord();
	    		if(dbrc!=null){
	    			/*String fr="";
		              int freq=((Integer)dbrc.get("ap_alert")).intValue();
		              if (freq==1)fr="(Q)";
		              else if (freq==2)fr="(M)";
		              prestazioni+=sep+fr+(String)dbrc.get("ap_prest_cod");
		            */
	    			prestazione = (String)dbrc.get("ap_prest_cod");
	    			prestazioni+=sep+prestazione;
	    			if(prelievi.contains("'"+prestazione+"'")){
	    				numprelievi++ ;
	    			}
	    			descPrestazioni+=sep2+decodificaGenerica("prestaz", "prest_cod", prestazione, "prest_des", dbc);
	    		}
	    		sep="-";
	    		sep2="\n";
    		}
//    		if (dbcur!=null) dbcur.close();
    		mydbr.put("prestazioni", prestazioni);
    		mydbr.put("prestazioni_desc", descPrestazioni);
    		
    		mydbr.put("numprestazioni", dbcur.getDimension());
    		mydbr.put("numprelievi", numprelievi);
//        		done=true;
    	} catch(Exception e){
    		throw newEjbException("Errore eseguendo selectAgendaIntpre: " + e.getMessage(), e);
    	}finally {
    		close_dbcur_nothrow("selectAgendaIntpre", dbcur);
//    		logout_nothrow("selectAgendaIntpre", dbc);
    	}
	}// END selectAgendaIntpre

//attenzione stesso metodo anche sotto AgendaModOperatore
    
//**********SELEZIONATA CELLA*****************************************************
/**
	*   Esegue le seguenti operazioni:
        *   1) seleziona da agenda_intpre le prestazioni da effettuare con relativa descrizione
	*/
	public Vector query_agendapopup(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
	{
                boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur=null;

                //debugMessage("call query_agendapopup h="+h.toString());
                try{
                  dbc = super.logIn(mylogin);
		  debugMessage("Agenda/query_agendapopup: APRO");
                  dbc.startTransaction();
                  //Vector vdbr = new Vector();
                   String data = (String)h.get("ag_data");
                  //int prog = ((Integer)h.get("ag_progr")).intValue();
                  String prog = (String)h.get("ag_progr");
                  String referente = (String)h.get("referente");
                  String mysel = "SELECT ap_prest_cod,ap_alert,ap_stato,prest_des ap_prest_desc,ap_prest_qta "+
//gb 14/09/07                  " FROM agenda_intpre, prestaz "+
                  " FROM agendant_intpre, prestaz " + //gb 14/09/07
                  " where ap_data="+formatDate(dbc,data)+
                  " and ap_progr="+prog+
                  " and ap_oper_ref='"+referente+"'"+
                  " and ap_prest_cod=prest_cod(+)";
                  if(h.containsKey("ag_cartella")){
                	  mysel = mysel + " AND ap_cartella = "+ISASUtil.getValoreStringa(h, "ag_cartella");
                  }
		  debugMessage("Agenda/query_agendapopup, mysel: " + mysel);
                  dbcur=dbc.startCursor(mysel);
                  Vector vdbr = dbcur.getAllRecord();
		  for (int i=0;i<vdbr.size();i++)
		    {
		    ISASRecord dbtab=(ISASRecord)vdbr.elementAt(i);
		    if (dbtab.get("ap_alert")!=null)
		      {
		      dbtab.put("ap_alert",((Integer)dbtab.get("ap_alert")).toString());
		      decodificaTabBase(dbc, dbtab, "FREQAC", "ap_alert", "frequenza");
		      }
		    }
                  dbc.commitTransaction();
		  if (dbcur != null)dbcur.close();
                  dbc.close();
                  super.close(dbc);
		  debugMessage("Agenda/query_agendapopup: CHIUDO");
                  done = true;
                 // String msg="Operazione completata con successo.";
                  return vdbr;
		}catch(DBRecordChangedException e){
			System.out.println("AgendaEJB.query_agendapopup(): Eccezione= " + e);
			try{
				System.out.println("AgendaEJB.query_agendapopup() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("AgendaEJB.query_agendapopup(): Eccezione= " + e);
			try{
				System.out.println("AgendaEJB.query_agendapopup() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una query_agendapopup() - "+  e);
			}
			throw e;
		}catch(Exception e){
			System.out.println("AgendaEJB.query_agendapopup(): Eccezione= " + e);
			try{
				System.out.println("AgendaEJB.query_agendapopup() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw newEjbException("Errore eseguendo una rollback() - " +  e1, e1);
			}
			throw newEjbException("Errore eseguendo una query_agendapopup() - " +  e, e);
		}finally{
			if (!done){
				try{
					if (dbcur != null)dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("AgendaEJB.query_agendapopup (): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}

	}

private void decodificaTabBase(ISASConnection dbc,ISASRecord dbr,
                     String cod,String nomeCampo,String descriz) throws Exception
{
//it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

		String val = (String)util.getObjectField(dbr,nomeCampo, 'S');
		String seltab = "SELECT tab_descrizione FROM tab_voci "+
				" WHERE tab_cod = '"+cod+"'"+
				" AND tab_val = '"+val+"'";
		ISASRecord dbtab = dbc.readRecord(seltab);
		String desc = (String)util.getObjectField(dbtab,"tab_descrizione", 'S');
		dbr.put(descriz, desc);
}// END decodificaTabBase

public Vector CaricaTabellaPrestazioni(myLogin mylogin, Hashtable h) throws SQLException
	{
        boolean done = false;
        Vector vdbr = new Vector();
        ISASConnection dbc = null;
        ISASCursor dbcur=null;
        ISASCursor dbcurConf =null;
        String msg="";
	String tipoOper = "";
        try{
               dbc = super.logIn(mylogin);
               Hashtable hTipo=new Hashtable();
	       if ((h.get("PianoAssistenziale") != null) ||
		   (h.get("JFramePrestazioniPopUp") != null) ||
		   (h.get("JFrameAgendaModificaPopUp") != null) ||
		   (h.get("JFrameAgendaRegistraPopUp") != null))
		  //gb la form del Piano assistenziale gli passa direttamente il tipo operatore
		  //gb cos� pure la form JFramePrestazioniPopUp
		  //gb cos� pure la form JFrameAgendaModificaPopUp
		  tipoOper = (String)h.get("referente");
	       else
               //vado a prendere il tipo operatore
                  tipoOper=//(String)h.get("tipo_oper");
			   selectTipoOperatore(dbc,(String)h.get("referente"));
//               System.out.println("Carica griglia Prestazioni su agenda: ");
               /*se il tipo operatore � 05--> amministrativo prendo tutte le prestazioni
               altrimenti prendo quelle del tipo dell'operatore.
               Nel caso di assistente sociale devo prendere i due tipi a seconda del servizio
               */
               String sWhere="";
               if (!tipoOper.equals("05"))
               {
                   String aggiunta=")";
                   if (tipoOper.equals("01"))
                          aggiunta= " OR conf_key ='TIPDEF"+tipoOper+"B')";
                  String myselectconf="Select  conf_txt from conf where "+
                          "conf_kproc ='SINS' and "+
                          "(conf_key ='TIPDEF"+tipoOper+"'"+ aggiunta;
               debugMessage("CONF: "+myselectconf);
                  dbcurConf = dbc.startCursor(myselectconf);

                  String tipoPrest="";
                  while(dbcurConf.next()){
                      ISASRecord dbconf = dbcurConf.getRecord();
                      if (dbconf!=null)
                      {
                        tipoPrest=(String)dbconf.get("conf_txt");
                        if (sWhere.equals(""))
                            sWhere=sWhere+" prest_tipo='"+tipoPrest+"'";
                        else
                            sWhere=sWhere+" OR prest_tipo='"+tipoPrest+"'";
                      }
                   }
                  if(dbcurConf.getDimension()==0 && !tipoOper.equals("")){
                	  sWhere=sWhere+" prest_tipo='"+tipoOper+"'";
                  }
                   if (!sWhere.equals(""))sWhere=" WHERE " +sWhere;
                   dbcurConf.close();
                   dbcurConf=null;
               }//fine operatore 05


               String sel="SELECT prest_cod, prest_des "+
                          " FROM prestaz " + sWhere+
                          " ORDER BY prest_des ";
               debugMessage("Carica griglia Prestazioni su agenda:"+sel);
			   dbcur=dbc.startCursor(sel);
				debugMessage("numero recs letti:"+dbcur.getDimension());
                vdbr=dbcur.getAllRecord();

                dbcur.close();
                dbc.close();
				super.close(dbc);
                done = true;
                return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaPrest()  ");
	}finally{
		if(!done){
			try{
				if (dbcur!=null)dbcur.close();
                                if (dbcurConf!=null)dbcurConf.close();
                                dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}
 public boolean isSettCaricata(myLogin mylogin,Hashtable h)throws Exception{
            boolean done=false;
            String dataIn=(String)h.get("giornoSel");
//               debugMessage("Data selezionata="+dataIn);
            Vector v=caricaSettimane(dataIn,null);//giorniSettimana(null,dataIn);
            String primo_gg=(String)v.elementAt(0);
            String ultimo_gg=(String)v.elementAt(6);
            ISASConnection dbc = null;
            try{
		dbc = super.logIn(mylogin);
//gb 29/08/07                  String mysel ="SELECT COUNT(*) tot from agenda_interv "+
                String mysel ="SELECT COUNT(*) tot from agendant_interv " + //gb 29/08/07
                    " where ag_data>="+formatDate(dbc,primo_gg)+
                    " and ag_data<="+formatDate(dbc,ultimo_gg)+
                    " and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";

            debugMessage("SELECT issettcaricata="+mysel);
            ISASRecord dbr=dbc.readRecord(mysel);
            int t=0;
            if(dbr!=null){
              t=util.getIntField(dbr,"tot");//t=convNumDBToInt("tot",dbr);
              }
            dbc.close();
            super.close(dbc);
            done=true;
            if(t==0)return false;
            else return true;
            }catch(Exception e1){
              debugMessage(""+e1);
              throw new SQLException("Errore eseguendo una isSettCaricata() - "+  e1);
            }finally{
              if (!done){
                      try{
                              dbc.close();
                              super.close(dbc);
                      }catch(Exception e2){
                              System.out.println("AgendaEditEJB.isSettCaricata (): - Eccezione nella chiusura della connessione= " + e2);
                      }
              }
            }

        }

	private Vector caricaSettimane(String dataI, Vector v_settimane)throws Exception {
	    try{
	    	if(v_settimane==null)v_settimane=new Vector();
	    	  dateutility dt =new dateutility();
	    String data_successiva=carica.getFirstDayWeek(dataI,6);
	    String dataF=carica.getLastDayWeek(dataI,6);
	  LOG.debug("caricaSettimane per data "+dataI);
	  int giorno=1;
	    int i=0;
	  LOG.debug("caricaSettimane prima del while "+data_successiva+" **** "+dataF );
	    //while(dt2.isSuccessiva(data_successiva)){
	
	  while(dt.confrontaDate(data_successiva,dataF)!=1) {
	        LOG.debug("caricaSettimane prima nel while ");	 
	    	String dt_carico=ndf.formDate(data_successiva, "aaaa-mm-gg");//"gg-mm-aaaa");
	        v_settimane.addElement(dt_carico);
	       LOG.debug("carica vettore "+i+" giorno="+dt_carico);
	        giorno++;	        
	        data_successiva=dt.getDataNGiorni(data_successiva,1);
	    }
	    return v_settimane;
	    }catch(Exception e) {
	    	throw new SQLException("AgendaEJB Errore eseguendo una caricaSettimane() - " +  e);
	    }
	  }

private Vector giorniSettimana(Vector vett,String giornoSel){
    debugMessage("calcola data="+giornoSel);
    /*String gg=giornoSel.substring(0,2);
    if(gg.substring(0,1).equals("0"))
      gg=gg.substring(1);

    String mm=giornoSel.substring(3,5);
    if(mm.substring(0,1).equals("0"))
      mm=mm.substring(1);

    String aaaa=giornoSel.substring(6);*/
	String giorno="00";
	String mese="00";
	String anno="0000";
	String data=giornoSel;
	String formato="gg-mm-aaaa";
	if(data.charAt(2)=='-' ||data.charAt(2)=='/')//gg-mm-aaaa
	  {
		giorno=data.substring(0,2);
		mese=data.substring(3,5);
		anno=data.substring(6,10);
		formato="gg"+data.charAt(2)+"mm"+data.charAt(2)+"aaaa";
	  }else	 if(data.charAt(4)=='-'	||data.charAt(4)=='/')//aaaa-mm-gg
	  {
		giorno=data.substring(8,10);
		mese=data.substring(5,7);
		anno=data.substring(0,4);
		formato="aaaa"+data.charAt(4)+"mm"+data.charAt(4)+"gg";
	  }
    GregorianCalendar gc =
    new GregorianCalendar(Integer.parseInt(anno),
                          Integer.parseInt(mese)-1,
                          Integer.parseInt(giorno));
   // System.out.println("giorno che arriva ="+getData(gc));
    int day=gc.get(Calendar.DAY_OF_WEEK);
    //System.out.println("giorno che arriva ="+getData(gc)+" day="+day);
    String data_inizio="";
    gc.add(gc.DATE,(day-2)*(-1));
    data_inizio=getData(gc,formato);
	Vector v =new Vector();
	if (vett!=null && !vett.isEmpty())
	{
		v=(Vector)vett.clone();
	}
    v.addElement(getData(gc,formato));
    for(int i=0;i<6;i++)
    {
      gc.add(gc.DATE,1);
      v.addElement(getData(gc,formato));
    }
   System.out.println("giornisettimana reperiti="+v.toString());
    return v;

}

String getData(GregorianCalendar gc){
	return getData(gc,"gg/mm/aaaa");
}
String getData(GregorianCalendar gc,String formato){
    int dd_i=gc.get(Calendar.DAY_OF_MONTH);
    int mm_i=gc.get(Calendar.MONTH)+1;
    int aaaa=gc.get(Calendar.YEAR);
    String dd=""+dd_i;
    String mm=""+mm_i;
    if(dd_i<=9) dd="0"+dd_i;
    if(mm_i<=9) mm="0"+mm_i;
    //String data=aaaa+mm+dd;
	String data="";
	if(formato.equals("gg-mm-aaaa"))
		data=dd+"-"+mm+"-"+aaaa;
    else if(formato.equals("gg/mm/aaaa"))
		data=dd+"/"+mm+"/"+aaaa;
    else if(formato.equals("aaaa/mm/gg"))
		data=aaaa+"/"+mm+"/"+dd;
	else data=aaaa+"-"+mm+"-"+dd;
    return data;
 }
//*************
/*
        private String selectPrestaz(ISASConnection dbc,String chiave)
        throws SQLException
        {
        boolean done=false;
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
        boolean done=false;
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
             String mysel ="SELECT dom_areadis,dom_citta from anagra_c"+
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

        }*/
        /*
        private Hashtable selectTipologiaOpe(ISASConnection dbc,String codice)throws SQLException{
          try{
             String mysel ="SELECT tipo,cod_qualif,cod_presidio,unita_funz from operatori"+
             " where codice='"+codice+"'";
              ISASRecord dbr=dbc.readRecord(mysel);
              if (dbr != null){
                 return dbr.getHashtable();
              }
            return null;
            }catch(Exception e1){
              debugMessage(""+e1);
              throw new SQLException("Errore eseguendo una selectAgendaInterv() - "+  e1);
            }

        }*/
		private String selectTipoOperatore(ISASConnection dbc,String codice)throws SQLException{
          try{
             String mysel ="SELECT tipo from operatori"+
             " where codice='"+codice+"'";
              ISASRecord dbr=dbc.readRecord(mysel);
              if (dbr != null){
                 return (String)dbr.get("tipo");
              }
            return "";
            }catch(Exception e1){
              throw new SQLException("Errore eseguendo una selectTipoOperatore() - "+  e1);
            }

        }/*
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
*/

private String scorroVett(String gg,Vector v){
          // gg=formattaData(gg);
		    gg=formDate(gg,"gg-mm-aaaa");
           for(int k=0;k<v.size();k++)
           {
            String is =(String)v.elementAt(k);
            if (is.equals(gg)){
               return ""+k;
            }
           }
            return null;
        }/*
        private String formattaData2(String campo)
{
//restituisce la data nel formato aaaa-mm-gg
          String dataret="0000-00-00";
          if (campo!=null && !campo.equals(""))
          {
                String dataLetta=campo;
                if (dataLetta.length()==10)
                {
                    //dataret=dataLetta.substring(8)+"-"+dataLetta.substring(5,7)
                    //          +"-"+dataLetta.substring(0,4);
                    dataret=dataLetta.substring(6)+"-"+dataLetta.substring(3,5)
                              +"-"+dataLetta.substring(0,2);
                }
          }
          return dataret;
}*/
 /*       private String formattaData(String campo)
{
//restituisce la data nel formato gg-mm-aaaa
          String dataret="00-00-0000";
          if (campo!=null && !campo.equals(""))
          {
                String dataLetta=campo;
                if (dataLetta.length()==10)
                {
                    dataret=dataLetta.substring(8)+"-"+dataLetta.substring(5,7)
                              +"-"+dataLetta.substring(0,4);
                }
          }
          return dataret;
}*/
private	String formDate(Object dataI,String	formato){
	return formDate(dataI,formato,false);//false =>	aaaammgg
}
private	String formDate(Object dataI,String	formato,boolean	it){
  String giorno="00";
  String mese="00";
  String anno="0000";
  String data="";
  if(dataI!=null){
	if(dataI instanceof	java.sql.Date) data=((java.sql.Date)dataI).toString();
	else if(dataI instanceof String) data=(String)dataI;
  }
  //it=false =>	aaaammgg
  //it=true	=> ggmmaaaa
  if(data!=null	&& !data.equals("")){

	  if(data.charAt(2)=='-' ||data.charAt(2)=='/')//gg-mm-aaaa
	  {
		giorno=data.substring(0,2);
		mese=data.substring(3,5);
		anno=data.substring(6,10);
	  }else	 if(data.charAt(4)=='-'	||data.charAt(4)=='/')//aaaa-mm-gg
	  {
		giorno=data.substring(8,10);
		mese=data.substring(5,7);
		anno=data.substring(0,4);
	  }else	//ggmmaaaa aaaammgg
	  {	if(it==false)//aaaammgg
		{
		 giorno=data.substring(6,8);
		 mese=data.substring(4,6);
		 anno=data.substring(0,4);
		}
		else//ggmmaaaa
		{
		 giorno=data.substring(0,2);
		 mese=data.substring(2,4);
		 anno=data.substring(4,8);
	   }
	  }
  }
  if(formato.equals("gg-mm-aaaa"))
	return giorno+"-"+mese+"-"+anno;
  else if(formato.equals("aaaa-mm-gg"))
	return anno+"-"+mese+"-"+giorno;
  else if(formato.equals("gg/mm/aaaa"))
	return giorno+"/"+mese+"/"+anno;
  else if(formato.equals("aaaa/mm/gg"))
	return anno+"/"+mese+"/"+giorno;
  else if(formato.equals("ggmmaaaa"))
   return giorno+mese+anno;
  else if(formato.equals("aaaammgg"))
	return anno+mese+giorno;
  else return data;
}
}

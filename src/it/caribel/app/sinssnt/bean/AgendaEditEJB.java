package it.caribel.app.sinssnt.bean;

//==========================================================================
//CARIBEL S.r.l.
//------------------------------------------------------------------------
//********NUOVA RELEASE GIUGNO 2012 bargi versione 12_03_01

//19/06/2012 bargi riguardata x la registrazione per tipo operaore sociale e gestione accessi occasionali con ap_alert=-1
//25/02/2008 aggiunto aggiornamento record su puauvm per i casi PUA bargi
//10/12/2007 aggiunto caso stato 9 PUA bargi
//9/06/04 - EJB di connessione alla procedura SINS sinss
//29/01/2007bargi ap_stato � 0 finche non avviene registrazione della prestazione			  
//
//==========================================================================
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.generic_controllers.CaribelDatesTableCtrl;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CollSinsPua;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//bargi 30/09/2008

public class AgendaEditEJB extends SINSSNTConnectionEJB{
	
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	CollSinsPua collSins=new CollSinsPua();	
	private final Log LOG = LogFactory.getLog(getClass());//elisa b 15/05/12
	String nomeEJB="AgendaEdit";
	public AgendaEditEJB() {}

	/**
	 * richiamato da agendaEdit
	 *   Esegue le seguenti operazioni:
	 *   inserisce su agenda_interv il nuovo intervento e le relative prestazioni
	 *
	 */
	public String cancella_intervCella(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
			{
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			LOG.debug("cancellointerv="+h.toString());
			h.put("ag_oper_ref",h.get("referente"));
			ISASRecord dbr=leggoInterv(dbc,h);			
			if(dbr!=null){
				boolean remove=removePrestazioni(dbc,dbr);
				if(remove) {
					dbc.deleteRecord(dbr);
					LOG.info("cancellazione " + dbr.getHashtable().toString());//elisa b 15/05/12
				}
			}			
			dbc.commitTransaction();
			done = true;
			String msg="Operazione completata con successo.";
			return msg;		
		}catch(DBRecordChangedException e){
			throw e;
		} catch(ISASPermissionDeniedException e){
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw newEjbException("Errore in PsicoPrestazEJB.cancella_intervCella: " + e.getMessage(), e);
		} finally{
			if (!done){
				rollback_nothrow(nomeEJB+".cancella_intervCella", dbc);
				LOG.error(nomeEJB+"ROLLBACK ");
			}
			//close_dbcur_nothrow("PsicoContattiEJB.insert", dbcur);
			logout_nothrow(nomeEJB+"cancella_intervCella", dbc);
		} 
			}
	public String sposto_appunt(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
			{
		boolean done = false;
		ISASConnection dbc = null;
		String msg="";
		try{
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			Vector vdbr = new Vector();
			boolean esiste=selectDestinazione(dbc,h);
			LOG.debug("AgendaEdit/sposto_appunt, return selectDestinazione="+esiste);

			String operaz=(String)h.get("operaz");
			if(operaz.equals("COLUMN")){
				if (esiste){
					msg=scorroColonna(dbc,h);
					LOG.debug("ret scorroColonna="+msg);					
				}else{
					copiaColonna(dbc,h);//non esiste destinaz
				}
			}else{
				msg=copioCella(dbc,h);
			}
			if(msg.equals("")){
				msg=deleteAppunt(dbc,h);
				LOG.debug("deleteAppunt="+msg);
				dbc.commitTransaction();
				LOG.debug("ESEGUITO commmit="+msg);
			}else{
				dbc.rollbackTransaction();
				msg+="Spostamento non effettuato!";
				LOG.debug("rollback="+msg);
			}
			dbc.close();
			super.close(dbc);
			done = true;
			if(msg.equals(""))
				msg="Operazione completata con successo.";
			LOG.debug("RITORNO="+msg);
			return msg;
		}catch(DBRecordChangedException e){
			System.out.println("AgendaEditEJB.sposto_appunt(): Eccezione= " + e);
			try{
				System.out.println("AgendaEditEJB.sposto_appunt() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("AgendaEditEJB.sposto_appunt(): Eccezione= " + e);
			try{
				System.out.println("AgendaEditEJB.sposto_appunt() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una sposto_appunt() - "+  e);
			}
			throw e;
		}catch(Exception e){
			System.out.println("AgendaEditEJB.sposto_appunt(): Eccezione= " + e);
			try{
				System.out.println("AgendaEditEJB.sposto_appunt() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - " +  e1);
			}
			throw new SQLException("Errore eseguendo una sposto_appunt() - " +  e);
		}finally{
			if (!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("AgendaEditEJB.sposto_appunt (): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}

			}

	public String copio_appunt(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
			{
		boolean done = false;
		ISASConnection dbc = null;
		String msg="";
		try{
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			Vector vdbr = new Vector();
			boolean esiste=selectDestinazione(dbc,h);
			LOG.debug("AgendaEdit/copio_appunt, return selectDestinazione="+esiste);
			String operaz=(String)h.get("operaz");
			if(operaz.equals("COLUMN")){
				if (esiste){
					msg=scorroColonna(dbc,h);					
				}else{
					copiaColonna(dbc,h);//non esiste destinaz					
				}
			}else{
				msg=copioCella(dbc,h);				
			}
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			if(msg.equals(""))
				msg="Operazione completata con successo.";
			return msg;
		} catch (Exception e) {
			throw newEjbException("Errore eseguendo "+ e.getStackTrace()[0].getMethodName() + ": " + e.getMessage(), e);
		} finally {
			if (!done) {
				rollback_nothrow(Thread.currentThread().getStackTrace()[1].getMethodName(), dbc);
			}
			logout_nothrow(Thread.currentThread().getStackTrace()[0].getMethodName(), dbc);
		}
			}

	private String copioCella(ISASConnection dbc,Hashtable h)throws SQLException{
		boolean done=false;
		ISASCursor dbcur=null;
		Hashtable hA=new Hashtable();
		hA=(Hashtable)h.get("hA");
		Hashtable hDa=new Hashtable();
		hDa=(Hashtable)h.get("hDa");
		String msg="";
		try{
			String mysel ="SELECT * "+
					" from agendant_interv, agendant_intpre " + 
					" where ag_data="+formatDate(dbc,(String)hDa.get("ag_data"))+
					" and ag_cartella="+(String)hDa.get("ag_cartella")+
					" and ag_contatto="+(String)hDa.get("ag_contatto")+
					" and n_intervento = " + (String)hDa.get("n_intervento") +
					" and ag_orario="+(String)hDa.get("ag_orario")+
					" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'"+
					" and ag_data=ap_data" +
					" and ag_progr=ap_progr" +
					" and ag_oper_ref=ap_oper_ref";
			if(( !(hDa.get("cod_obbiettivo").toString()).trim().equals("")))
				mysel+=" and cod_obbiettivo = '" + (String)hDa.get("cod_obbiettivo") + "'" ;
			else 	 mysel+=" and cod_obbiettivo is null" ;
			LOG.debug("AgendaEdit/copioCella, mysel= "+mysel);
			int progrOld=-1;
			int progr=0;
			String operaz=(String)h.get("operaz");
			dbcur=dbc.startCursor(mysel);

			Vector vdbr = dbcur.getAllRecord();
			if ((vdbr != null) && (vdbr.size() > 0))
				for(int i=0; i<vdbr.size(); i++)
				{
					ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
					LOG.debug("AgendaEdit/copioCella, record i="+i+" rec letto="+dbrec.getHashtable().toString());
					if (dbrec != null)
					{
						int progrL=((Integer)dbrec.get("ag_progr")).intValue();
						Hashtable hRec=dbrec.getHashtable();
						if(operaz.equals("CELL"))
						{
							hRec.put("ag_cartella",new Integer ((String)hA.get("ag_cartella")));
							hRec.put("ag_contatto",new Integer ((String)hA.get("ag_contatto")));
							hRec.put("cod_obbiettivo",(String)hA.get("cod_obbiettivo"));
							hRec.put("n_intervento",new Integer ((String)hA.get("n_intervento")));
							hRec.put("ag_orario",new Integer ((String)hA.get("ag_orario")));
						}
						if(progrOld!=progrL)
						{
							progrOld=progrL;
							progr=caricoAgendaInterv(dbc,(String)hA.get("ag_data"),hRec);
							if(progr==-1)
							{//non proseguo ... destinazione ha stato!=0
								msg +=  "Non copiato appunt data="+(String)hDa.get("ag_data")+
										" cartella="+(String)hDa.get("ag_cartella")+
										" contatto="+(String)hDa.get("ag_contatto")+
										" obiettivo="+(String)hDa.get("cod_obbiettivo")+
										" intervento="+(String)hDa.get("n_intervento")+
										" stato intervento destinazione diverso da 0.\n";
								i=vdbr.size();//mi blocco tanto non ha scritto ancora nulla
							}
							caricoAgendaIntpre(dbc,hRec ,progr,(String)hA.get("ag_data"));
						}
						else
						{
							caricoAgendaIntpre(dbc,hRec ,progr,(String)hA.get("ag_data"));
						}
					}
				}
			dbcur.close();
			done = true;
			//LOG.debug("size vettore="+rigatabella.size());
			return msg;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("Errore eseguendo una copiaCella() - "+  e1);
		}
		finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e2){LOG.debug(""+e2);}
			}
		}
	}
	private String scorroColonna(ISASConnection dbc,Hashtable h)throws SQLException{
		boolean done=false;
		ISASCursor dbcur=null;
		ISASCursor dbcur2=null;
		Hashtable hA=new Hashtable();
		hA=(Hashtable)h.get("hA");
		Hashtable hDa=new Hashtable();
		hDa=(Hashtable)h.get("hDa");
		String msg="";
		try{
			String mysel ="SELECT * "+
					//			gb 14/09/07             		" from agenda_interv"+
					" from agendant_interv"+ //gb 14/09/07
					" where ag_data="+formatDate(dbc,(String)hDa.get("ag_data"))+
					" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
			int progrOld=-1;
			int progr=0;
			LOG.debug("AgendaEdit/scorroColonna, mysel: "+mysel);
			dbcur=dbc.startCursor(mysel);
			Vector vdbr = dbcur.getAllRecord();
			if ((vdbr != null) && (vdbr.size() > 0))
				for(int i=0; i<vdbr.size(); i++) {
					ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);

					Hashtable hRec=dbrec.getHashtable();
					LOG.debug("AgendaEdit/scorroColonna, record i="+i+" rec letto="+dbrec.getHashtable().toString());
					if (dbrec != null){
						progr=caricoAgendaInterv(dbc,(String)hA.get("ag_data"),hRec);
						if(progr==-1){//non proseguo ... destinazione ha stato!=0
							msg+="Non copiato appunt data="+(String)hDa.get("ag_data")+
									" cartella="+(Integer)dbrec.get("ag_cartella")+
									" contatto="+(Integer)dbrec.get("ag_contatto")+
									//							gb 14/09/07 *******
									" obiettivo="+(String)dbrec.get("cod_obbiettivo")+
									" intervento="+(Integer)dbrec.get("n_intervento")+
									//							gb 14/09/07: fine *******
									" stato intervento destinazione diverso da 0.\n";
						}else{
							//
							String mysel2 ="SELECT * "+
									" from agendant_intpre "+
									" where ap_data="+formatDate(dbc,(String)hDa.get("ag_data"))+
									" and ap_oper_ref='"+((String)h.get("referente")).trim()+"'"+
									" and ap_progr="+dbrec.get("ag_progr");
							LOG.debug("AgendaEdit/scorroColonna, mysel2: "+mysel2);
							dbcur2=dbc.startCursor(mysel2);
							Vector vdbr2 = dbcur2.getAllRecord();
							if ((vdbr2 != null) && (vdbr2.size() > 0))
								for(int j=0; j<vdbr2.size(); j++) {
									ISASRecord dbrec2 = (ISASRecord)vdbr2.elementAt(j);
									if (dbrec2 != null){
										caricoAgendaIntpre(dbc,dbrec2.getHashtable() ,progr,(String)hA.get("ag_data"));
									}
								}							
						}
					}
				}
			if(dbcur!=null)
				dbcur.close();
			if(dbcur2!=null)
				dbcur2.close();
			done = true;
			//LOG.debug("size vettore="+rigatabella.size());
			return msg;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("Errore eseguendo una scorroColonna() - "+  e1);
		}
		finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					if(dbcur2!=null)
						dbcur2.close();
				}catch(Exception e2){LOG.debug(""+e2);}
			}
		}
	}

	private Vector copiaColonna(ISASConnection dbc,Hashtable h)throws SQLException{
		boolean done=false;
		ISASCursor dbcur=null;
		Hashtable hA=new Hashtable();
		hA=(Hashtable)h.get("hA");
		Hashtable hDa=new Hashtable();
		hDa=(Hashtable)h.get("hDa");
		LOG.debug("AgendaEdit/copioColonna");
		try{
			String mysel ="SELECT * "+
					" from agendant_interv, agendant_intpre " + //gb 14/09/07
					" where ag_data="+formatDate(dbc,(String)hDa.get("ag_data"))+
					" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'"+
					" and ag_data=ap_data" +
					" and ag_progr=ap_progr" +
					" and ag_oper_ref=ap_oper_ref";
			int progrOld=-1;
			int progr=0;
			LOG.debug("AgendaEdit/copioColonna, mysel: " + mysel); //gb 14/09/07
			dbcur=dbc.startCursor(mysel);
			Vector vdbr = dbcur.getAllRecord();
			if ((vdbr != null) && (vdbr.size() > 0))
				for(int i=0; i<vdbr.size(); i++)
				{
					ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
					//LOG.debug("record i="+i+" rec letto="+dbrec.getHashtable().toString());
					if (dbrec != null)
					{
						int progrL=((Integer)dbrec.get("ag_progr")).intValue();
						if(progrOld!=progrL)
						{
							progrOld=progrL;
							progr=caricoAgendaInterv(dbc,(String)hA.get("ag_data"),dbrec.getHashtable());
							caricoAgendaIntpre(dbc,dbrec.getHashtable() ,progr,(String)hA.get("ag_data"));
						}
						else
						{
							caricoAgendaIntpre(dbc,dbrec.getHashtable() ,progr,(String)hA.get("ag_data"));
						}
					}
				}
			dbcur.close();
			done = true;
			LOG.debug("ret copioColonna="+vdbr.toString());
			return vdbr;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("Errore eseguendo una copiaColonna() - "+  e1);
		}
		finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e2){LOG.debug(""+e2);}
			}
		}
	}

	private int caricoAgendaInterv(ISASConnection dbc,String data,Hashtable h)
			throws SQLException
			{
		LOG.debug("AgendaEdit/caricoAgendaInterv="+h.toString());
		LOG.debug("AgendaEdit/caricoAgendaInterv..la data �="+data);

		String cartella=((Integer)h.get("ag_cartella")).toString();
		String contatto=((Integer)h.get("ag_contatto")).toString();
		String strCodObiettivo=((String)h.get("cod_obbiettivo")).trim();
		String strNIntervento=((Integer)h.get("n_intervento")).toString();
		String oper=((String)h.get("ag_oper_ref")).trim();
		String strTipoOper = ((String)h.get("ag_tipo_oper")).trim(); //gb 14/09/07
		String orario=((Integer)h.get("ag_orario")).toString();
		ISASRecord dbr = null;
		try{
			String selprog = "SELECT * "+
					" FROM agendant_interv "+ 
					" WHERE ag_data =" +formatDate(dbc,data)+" and "+
					" ag_oper_ref='"+oper+"' and "+
					" ag_cartella="+cartella+" and "+
					" ag_contatto="+contatto+" and "+
					" n_intervento = " + strNIntervento + " and " +
					" ag_orario="+orario;
			if( (!strCodObiettivo.equals("")))
				selprog+=" and cod_obbiettivo = '" +strCodObiettivo+ "'" ;
			else 	 selprog+=" and cod_obbiettivo is null" ;
			LOG.debug("AgendaEdit/caricoAgendaInterv, select selprog: "+selprog);
			dbr = dbc.readRecord(selprog);
			int progressivo=0;
			if (dbr != null) {
				progressivo=((Integer)dbr.get("ag_progr")).intValue();
				String stato=(String)dbr.get("ag_stato");
				if(!stato.equals("0")&&!stato.equals("9")){
					return -1;//esiste il rec su interv e stato non compatibile
				}
			}else{
				// 1) prelevo il max progr inserito per quel giorno
				ISASRecord dbmax = null;
				String selmax = "SELECT MAX(ag_progr) max "+
						" FROM agendant_interv "+ //gb 14/09/07
						" WHERE ag_data =" +formatDate(dbc,data);
				LOG.debug("AgendaEdit/caricoAgendaInterv, select selmax: "+selmax); //gb 14/09/07
				dbmax = dbc.readRecord(selmax);
				if (dbmax != null) {
					if(dbmax.get("max")!=null){
						int max = ((Integer)dbmax.get("max")).intValue();
						max++;
						progressivo = max;
					}
				}
				ISASRecord rec_ag = dbc.newRecord("agendant_interv"); //gb 14/09/07
				rec_ag.put("ag_data",data);
				rec_ag.put("ag_progr", new Integer(progressivo));
				rec_ag.put("ag_cartella",cartella);
				rec_ag.put("ag_contatto",contatto);
				rec_ag.put("num_scheda_pua",h.get("num_scheda_pua"));
				//				gb 14/09/07 *******
				rec_ag.put("cod_obbiettivo", strCodObiettivo);
				rec_ag.put("n_intervento", strNIntervento);
				//				gb 14/09/07: fine *******
				rec_ag.put("ag_oper_ref",oper);
				//				gb 14/09/07                  rec_ag.put("ag_tipo_oper", (selectTipoOpe(dbc,oper)).get("tipo"));
				rec_ag.put("ag_tipo_oper", strTipoOper); //gb 14/09/07
				rec_ag.put("ag_orario",new Integer(orario));
				rec_ag.put("ag_oper_esec", "");
				rec_ag.put("ag_stato", (String)h.get("ag_stato"));//"0");[PUA]
				if(h.containsKey("num_scheda_pua")&& !(h.get("num_scheda_pua").toString()).equals("")){
					Hashtable hUpd=new Hashtable();
					hUpd.put("data",data);
					hUpd.put("num_scheda",h.get("num_scheda_pua"));
					hUpd.put("cod_oper",oper);
					collSins.designaOperatore(dbc,hUpd,"I");				
				}

				LOG.debug("AgendaEdit/caricoAgendaInterv, carico su agendant_interv il record: \n"+(rec_ag.getHashtable()).toString());
				dbc.writeRecord(rec_ag);
				LOG.info("caricoAgendaInterv " + rec_ag.getHashtable());//elisa b 15/05/12
				LOG.debug("AgendaEdit/caricoAgendaInterv **** caricato!***");
			}
			return progressivo;
		}catch(Exception e){
			LOG.debug("AgendaEdit/caricoAgendaInterv, errore in carico su agenda_interv="+e);
			throw new SQLException("AgendaEdit, Errore eseguendo un caricoAgendaInterv() - "+  e);
		}
			}

	private void caricoAgendaIntpre(ISASConnection dbc,Hashtable h,int progressivo,String data)
			throws SQLException
			{
		LOG.debug("AgendaEdit/caricoAgendaIntpre, sono in carica agenda_intpre.."+h.toString());
		LOG.debug("AgendaEdit/caricoAgendaIntpre, sono in carica agenda_intpre..la data �="+data);
		// 1) prelevo la prestazione inserito per quel giorno
		String prest=((String)h.get("ap_prest_cod")).trim();
		String oper=((String)h.get("ap_oper_ref")).trim();
		try{
			String sel="SELECT *" +
					//			gb 14/09/07			    " FROM agenda_intpre WHERE "+
					" FROM agendant_intpre WHERE " + //gb 14/09/07
					"ap_data="+formatDate(dbc,data)+" AND "+
					"ap_progr="+progressivo+" AND "+
					"ap_oper_ref='"+oper+"' AND "+
					"ap_prest_cod='"+prest+"'";
			LOG.debug("AgendaEdit/caricoAgendaIntpre, sel: " + sel); //gb 14/09/07
			ISASRecord is = dbc.readRecord(sel);
			if(is==null){
				//				gb 14/09/07                  ISASRecord rec_ag = dbc.newRecord("agenda_intpre");
				ISASRecord rec_ag = dbc.newRecord("agendant_intpre"); //gb 14/09/07
				rec_ag.put("ap_data",data);
				rec_ag.put("ap_progr", new Integer(progressivo));
				rec_ag.put("ap_prest_cod",prest);
				rec_ag.put("ap_oper_ref",oper);
				rec_ag.put("ap_stato",new Integer(0));
				rec_ag.put("ap_prest_qta",h.get("ap_prest_qta"));
				rec_ag.put("ap_alert",h.get("ap_alert"));
				rec_ag.put("ap_cartella", h.get("ag_cartella"));
				LOG.debug("AgendaEdit/caricoAgendaIntpre: carico su agendant_intpre il record: \n"+(rec_ag.getHashtable()).toString());
				dbc.writeRecord(rec_ag);
				LOG.info("caricoAgendaIntpre " + rec_ag.getHashtable().toString());//elisa b 15/05/12
				LOG.debug("AgendaEdit/caricoAgendaIntpre **** caricato!***");
			}else{
				LOG.debug("AgendaEdit/caricoAgendaIntpre **** IL RECORD RISULTA CARICATO: \n"+(is.getHashtable()).toString());
			}
		}catch(Exception e){
			LOG.debug("AgendaEdit, errore in caricoAgendaIntpre()="+e);
			throw new SQLException("AgendaEdit, Errore eseguendo una caricoAgendaIntpre() - "+  e);
		}
			}

	private String deleteAppunt(ISASConnection dbc,Hashtable h)throws SQLException{
		boolean done=false;
		ISASCursor dbcur=null;
		ISASCursor dbcur2=null;
		Hashtable hA=new Hashtable();
		hA=(Hashtable)h.get("hA");
		Hashtable hDa=new Hashtable();
		hDa=(Hashtable)h.get("hDa");
		String operaz=(String)h.get("operaz");
		String msg="";
		try{
			String mysel ="SELECT * "+
					//			gb 14/09/07			" from agenda_interv "+
					" from agendant_interv " + //gb 14/09/07
					" where ag_data="+formatDate(dbc,(String)hDa.get("ag_data"))+
					" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
			if(operaz.equals("CELL")||operaz.equals("CELLDAY"))
			{
				mysel+=" and ag_cartella="+(String)hDa.get("ag_cartella");
				mysel+=" and ag_contatto="+(String)hDa.get("ag_contatto");
				//				gb 14/09/07 *******
				//mysel+=" and cod_obbiettivo = '" + (String)hDa.get("cod_obbiettivo") + "'";
				if (! ((String)hDa.get("cod_obbiettivo")).trim() .equals(""))
					mysel+=" and cod_obbiettivo = '" + (String)hDa.get("cod_obbiettivo") + "'" ;
				else 	 mysel+=" and cod_obbiettivo is null" ;

				mysel+=" and n_intervento = " + (String)hDa.get("n_intervento");
				//				gb 14/09/07: fine *******
				mysel+=" and ag_orario="+(String)hDa.get("ag_orario");
			}
			LOG.debug("AgendaEdit/deleteAppunt, SELECT mysel= " + mysel);
			dbcur=dbc.startCursor(mysel);
			Vector vdbr = dbcur.getAllRecord();
			if ((vdbr != null) && (vdbr.size() > 0))
				for(int i=0; i<vdbr.size(); i++)
				{
					ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
					LOG.debug("AgendaEdit/deleteAppunt, record i="+i+" rec letto="+dbrec.getHashtable().toString());
					if (dbrec != null)
					{
						/*if(!((String)dbrec.get("ag_stato")).equals("0")){//non proseguo ... destinazione ha stato!=0
						 LOG.debug("stato diverso da 0");
						 msg+="Spostamento non effettuato l'appuntamento del="+((java.sql.Date)dbrec.get("ag_data")).toString()+
						 " cartella="+(String)dbrec.get("ag_cartella")+" contatto="+(String)dbrec.get("ag_contatto")+" ha stato intervento diverso da 0.\n";
						 i=vdbr.size();//mi blocco tanto non ha scritto ancora nulla
						 return msg;
						 }*/
						//  int progrL=((Integer)dbrec.get("ag_progr")).intValue();
						String mysel2 ="SELECT * "+
								//						gb 14/09/07                                 " from agenda_intpre "+
								" from agendant_intpre " + //gb 14/09/07
								" where ap_data="+formatDate(dbc,((java.sql.Date)dbrec.get("ag_data")).toString())+
								" and ap_oper_ref='"+((String)dbrec.get("ag_oper_ref")).trim()+"'"+
								" and ap_progr="+dbrec.get("ag_progr");
						LOG.debug("AgendaEdit/deleteAppunt, SELECT mysel2= " + mysel2);
						dbcur2=dbc.startCursor(mysel2);
						Vector vdbr2 = dbcur2.getAllRecord();
						if ((vdbr2 != null) && (vdbr2.size() > 0))
							for(int j=0; j<vdbr2.size(); j++)
							{
								ISASRecord dbrec2 = (ISASRecord)vdbr2.elementAt(j);
								LOG.debug("record j="+j+" rec letto="+dbrec2.getHashtable().toString());
								if (dbrec2 != null)
								{
									String del1="SELECT * "+
											//									gb 14/09/07					" from agenda_intpre "+
											" from agendant_intpre " + //gb 14/09/07
											" where ap_data="+formatDate(dbc,((java.sql.Date)dbrec2.get("ap_data")).toString())+
											" and ap_oper_ref='"+((String)dbrec2.get("ap_oper_ref")).trim()+"'"+
											" and ap_prest_cod='"+((String)dbrec2.get("ap_prest_cod")).trim()+"'"+
											" and ap_progr="+dbrec2.get("ap_progr");
									LOG.debug("AgendaEdit/deleteAppunt, SELECT del1= " + del1);
									ISASRecord is = dbc.readRecord(del1);
									if(is!=null)
									{
										if(primaVisita(dbc,dbrec2.get("ap_prest_cod").toString(),dbrec.get("ag_tipo_oper").toString())){
											if(dbrec.get("num_scheda_pua")!=null &&!(dbrec.get("num_scheda_pua").toString()).equals("") ){
												Hashtable hUpd=new Hashtable();
												hUpd.put("data",dbrec2.get("ap_data").toString());
												hUpd.put("num_scheda",dbrec.get("num_scheda_pua"));
												hUpd.put("cod_oper",dbrec2.get("ap_oper_ref").toString());
												collSins.designaOperatore(dbc,hUpd,"R");							
											}
										}
										LOG.debug("AgendaEdit/deleteAppunt, delete del1 (is)= "+is.getHashtable().toString());
										dbc.deleteRecord(is);
										LOG.info("cancellazione " + del1);//elisa b 15/05/12
									}
								}
							}
						String del2 = "SELECT * "+
								//						gb 14/09/07				" from agenda_interv "+
								" from agendant_interv " + //gb 14/09/07
								" where ag_data="+formatDate(dbc,((java.sql.Date)dbrec.get("ag_data")).toString())+
								" and ag_oper_ref='"+((String)dbrec.get("ag_oper_ref")).trim()+"'"+
								" and ag_progr="+dbrec.get("ag_progr");
						LOG.debug("AgendaEdit/deleteAppunt, SELECT del2= " + del2);
						ISASRecord is2 = dbc.readRecord(del2);
						if(is2!=null)
						{
							LOG.debug("AgendaEdit/deleteAppunt, delete del2 (is2)= "+is2.getHashtable().toString());
							dbc.deleteRecord(is2);
							LOG.info("cancellazione " + del2);//elisa b 15/05/12
						}
					}
				}

			if(dbcur!=null)
				dbcur.close();
			if(dbcur2!=null)
				dbcur2.close();
			done = true;
			return msg;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("AgendaEdit, Errore eseguendo una deleteAppunt() - "+  e1);
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					if(dbcur2!=null)
						dbcur2.close();
				}catch(Exception e2){LOG.debug(""+e2);}
			}
		}
	}
	private boolean primaVisita(ISASConnection dbc,String prest,String tipo)throws SQLException{
		prest=prest.trim();
		String sel=" select conf_txt from conf where conf_kproc='SINS' and conf_key='PRESTPUA"+tipo+"'";
		String prest_conf="";
		try{
			ISASRecord dbr=dbc.readRecord(sel);
			if(dbr!=null){
				if(dbr.get("conf_txt")!=null)
					prest_conf=dbr.get("conf_txt").toString().trim();
				if(!prest_conf.equals("") && prest_conf.equals(prest)) return true;				
			}
			return false;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("AgendaEdit, Errore eseguendo una primaVisita() - "+  e1);
		}
	}
	private boolean selectDestinazione(ISASConnection dbc,Hashtable h)throws SQLException{
		boolean done=false;
		ISASCursor dbcur=null;
		Hashtable hA=new Hashtable();
		hA=(Hashtable)h.get("hA");
		Hashtable hDa=new Hashtable();
		hDa=(Hashtable)h.get("hDa");
		String operaz=(String)h.get("operaz");
		LOG.debug("AgendaEdit/selectDestinazione");

		try{
			String mysel ="SELECT COUNT(*) tot "+
					//			gb 14/09/07             " from agenda_interv "+
					" from agendant_interv "+ //gb 14/09/07
					" where ag_data="+formatDate(dbc,(String)hA.get("ag_data"))+
					" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
			if(operaz.equals("CELL")){
				mysel+=" and ag_cartella="+(String)hA.get("ag_cartella");
				mysel+=" and ag_contatto="+(String)hA.get("ag_contatto");
				//				gb 14/09/07 *******
				//	mysel+=" and cod_obbiettivo = '" + (String)hA.get("cod_obbiettivo") + "'";
				if (! ((String)hA.get("cod_obbiettivo")).trim() .equals(""))
					mysel+=" and cod_obbiettivo = '" + (String)hA.get("cod_obbiettivo") + "'" ;
				else 	 mysel+=" and cod_obbiettivo is null" ;

				mysel+=" and n_intervento = " + (String)hA.get("n_intervento");
				//				gb 14/09/07: fine *******
				mysel+=" and ag_orario="+(String)hA.get("ag_orario");
			}else if(operaz.equals("CELLDAY")){
				mysel+=" and ag_cartella="+(String)hDa.get("ag_cartella");
				mysel+=" and ag_contatto="+(String)hDa.get("ag_contatto");
				//				gb 14/09/07 *******
				//mysel+=" and cod_obbiettivo = '" + (String)hDa.get("cod_obbiettivo") + "'";
				if (! ((String)hDa.get("cod_obbiettivo")).trim() .equals(""))
					mysel+=" and cod_obbiettivo = '" + (String)hDa.get("cod_obbiettivo") + "'" ;
				else 	 mysel+=" and cod_obbiettivo is null" ;

				mysel+=" and n_intervento = " + (String)hDa.get("n_intervento");
				//				gb 14/09/07: fine *******
				mysel+=" and ag_orario="+(String)hDa.get("ag_orario");
			}
			LOG.debug("AgendaEdit/selectDestinazione, mysel: "+mysel);
			ISASRecord dbr=dbc.readRecord(mysel);
			int t=0;
			if(dbr!=null){
				t=convNumDBToInt("tot",dbr);
			}

			done=true;
			if(t==0)return false;
			else return true;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("AgendaEdit: Errore eseguendo una selectDestinazione() - "+  e1);
		}
	}

	//	**********SELEZIONATA CELLA*****************************************************
	//	**
	/**
	 * richiamato da agendaEdit
	 *   Esegue le seguenti operazioni:
	 *
	 *   inserisce su agenda_intpre le nuove prestazioni da fare
	 *   *non si inserisce prima visita da qui quindi non considero puauvm
	 */
	public String inserisci_prestazCella(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
			{
		String methodName = "inserisci_prestazCella";
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			//LOG.debug("************inizio cella**************");
			LOG.debug("AgendaEdit/inserisci_prestazCella hashtable arrivata="+h.toString());
			java.sql.Date w=java.sql.Date.valueOf((String)h.get("ag_data"));
			h.put("ag_data",w);
			h.put("ag_oper_ref",h.get("referente"));			
			dbc.startTransaction();
			LOG.debug("prestazioni="+h.toString());
			h.put("ag_oper_ref",h.get("referente"));
			ISASRecord dbr=leggoInterv(dbc,h);			
			if(dbr!=null){
				boolean remove=removePrestazioni(dbc,dbr);
			}			

			///***
			Vector v_prest=(Vector)h.get("prestazioni");
			Vector v_quant=(Vector)h.get("quantita");
			Vector v_freq=(Vector)h.get("frequenza");
			Enumeration en=v_prest.elements();
			int numPrest=1;

			int ind=0;
			while(en.hasMoreElements())
			{
				Hashtable h_prest=new Hashtable();
				String pr =(String)en.nextElement();
				h_prest.put("ap_prest_cod",pr);
				h_prest.put("ap_prest_qta",(String)v_quant.elementAt(ind));
				String fr=(String)v_freq.elementAt(ind);
				if(fr.equals(""))fr="-1";
				h_prest.put("ap_alert",new Integer(fr)); //bargi 15/06/2012
				//h_prest.put("ap_alert",new Integer(-1));//prest occasionale
				ind++;
				h_prest.put("ap_data",h.get("ag_data"));
				h_prest.put("ap_progr",h.get("ag_progr"));
				h_prest.put("ap_oper_ref",h.get("referente"));
				h_prest.put("ap_cartella",h.get("ag_cartella"));
				LOG.debug("AgendaEdit/inserisci_prestazCella, aggiornoagintpre="+h_prest.toString());
				aggiornoAgIntpre(dbc, h_prest);
				numPrest++;
			}
			//LOG.debug("************fine**************");
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			String msg="Operazione completata con successo.";
			return msg;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo " + methodName + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(methodName, dbc);
		}

			}

	private void aggiornoAgIntpre(ISASConnection dbc,Hashtable h_intpre )throws SQLException{
		try{
			String prog="";
			String qta="";
			prog=h_intpre.get("ap_progr").toString();
			qta=h_intpre.get("ap_prest_qta").toString();

			String mysel ="SELECT *" +
					" from agendant_intpre" +
					" where ap_data="+formatDate(dbc,h_intpre.get("ap_data").toString())+
					" and ap_progr="+prog+
					" and ap_prest_cod='"+((String)h_intpre.get("ap_prest_cod")).trim()+"'"+
					" and ap_oper_ref='"+((String)h_intpre.get("ap_oper_ref")).trim()+"'";
			LOG.debug("AgendaEdit/aggiornoAgIntpre, mysel: " + mysel);
			ISASRecord dbr=dbc.readRecord(mysel);
			if (dbr != null)
			{//pu� essere stata aggiornata la quantit�
				dbr.put("ap_prest_qta",new Integer(qta));
				dbc.writeRecord(dbr);
				LOG.info("aggiornoAgIntpre " + mysel);//elisa b 15/05/12
			}
			else
			{
				ISASRecord rec_ag = dbc.newRecord("agendant_intpre"); //gb 14/09/07
				rec_ag.put("ap_data", ndf.formDate(h_intpre.get("ap_data").toString(),"aaaa-mm-gg"));
				rec_ag.put("ap_progr", new Integer(prog));
				rec_ag.put("ap_prest_qta",new Integer(qta));
				rec_ag.put("ap_prest_cod",((String)h_intpre.get("ap_prest_cod")).trim());
				rec_ag.put("ap_oper_ref",((String)h_intpre.get("ap_oper_ref")).trim());
				rec_ag.put("ap_cartella",h_intpre.get("ap_cartella"));
				rec_ag.put("ap_stato",new Integer(0));				
				rec_ag.put("ap_alert",h_intpre.get("ap_alert").toString());
				dbc.writeRecord(rec_ag);
				LOG.info("aggiornoAgIntpre " + rec_ag.getHashtable().toString());//elisa b 15/05/12
			}
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("AgendaEdit, Errore eseguendo una aggiornoAgIntpre() - "+  e1);
		}
	}

	/**
	 * richiamato da agendaEdit
	 *   Esegue le seguenti operazioni:
	 *   inserisce su agenda_interv il nuovo intervento e le relative prestazioni
	 *non si inserisce prima visita da qui quindi non considero puauvm
	 */
	public String inserisci_intervCella(myLogin mylogin, Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
			{
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			LOG.debug("AgendaEdit/inserisci_intervCella, Hashtabledi input= "+h.toString());		
			h.put("ag_oper_ref",h.get("referente"));			
			dbc.startTransaction();
			
			StringTokenizer st = new StringTokenizer(h.get("ag_data").toString(), CaribelDatesTableCtrl.sep);
			Vector<String> dateIntervento = new Vector<String>(st.countTokens());
			if(ManagerProfile.isConfigurazioneMarche(CaribelSessionManager.getInstance())){
				while (st.hasMoreElements()) {
					String dataInt = (String) st.nextElement();
					dateIntervento.add(dataInt);
				}
			}else{
				dateIntervento.add(st.toString().trim());
			}
			for (Iterator iterator = dateIntervento.iterator(); iterator.hasNext();) {
				String dataInterv = (String) iterator.next();
				h.put("ag_data", dataInterv);
				
				int contatore=inseriscoAgendaInterv(dbc,h);
				Vector v_prest=(Vector)h.get("prestazioni");
				Vector v_quant=(Vector)h.get("quantita");
				Vector v_freq=(Vector)h.get("frequenza");
				Enumeration en=v_prest.elements();
				int numPrest=1;
				Hashtable h_prest=new Hashtable();
				int ind=0;
				while(en.hasMoreElements()){
					String pr =(String)en.nextElement();
					h_prest.put("ap_prest_cod",pr);
					h_prest.put("ap_prest_qta",(String)v_quant.elementAt(ind));
					String frq="0";
					/*if(v_freq.size()>0){
					if((v_freq.elementAt(ind).toString()).equals("")) frq="0";
					else frq=v_freq.elementAt(ind).toString();
					}*/
					h_prest.put("ap_alert",new Integer(-1));//prest occasionale
					//bargi 15/06/2012 h_prest.put("ap_alert",frq);
					ind++;
					h_prest.put("ap_data",ndf.formDate(h.get("ag_data").toString(),"aaaa-mm-gg"));
					h_prest.put("ap_progr",new Integer(contatore));
					h_prest.put("ap_oper_ref",h.get("referente"));
					h_prest.put("ap_cartella",h.get("ag_cartella"));
					//LOG.debug("aggiornoagintpre="+h_prest.toString());
					aggiornoAgIntpre(dbc,h_prest);
					//LOG.debug("inseriscointpre="+h_prest.toString());
					numPrest++;
				}
			}
			//per imomento abbiamo detto che se l'intervento  risulta
			//completo non � possibile inserire nuove prestazioni
			//se si cambia idea allora occorre aggiornare lo stato
			/*int stato=parseInt((String)h.get("ag_stato"));
			 if(stato==2)//completo{
			 stato=1;//incompleto
			 aggiornoAgInterv(dbc,h,stato);
			 }*/
			//LOG.debug("************fine**************");
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done = true;
			String msg="Operazione completata con successo.";
			return msg;
		}catch(DBRecordChangedException e){
			System.out.println("AgendaEditEJB.inserisci_intervCella(): Eccezione= " + e);
			try{
				System.out.println("AgendaEditEJB.inserisci_intervCella() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println("AgendaEditEJB.inserisci_intervCella(): Eccezione= " + e);
			try{
				System.out.println("AgendaEditEJB.inserisci_intervCella() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una registra_prestazCella() - "+  e);
			}
			throw e;
		}catch(Exception e){
			System.out.println("AgendaEditEJB.inserisci_intervCella(): Eccezione= " + e);
			try{
				System.out.println("AgendaEditEJB.inserisci_intervCella() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - " +  e1);
			}
			throw newEjbException("Errore eseguendo una inserisci_intervCella() - " +  e, e);
		}finally{
			if (!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println("AgendaEditEJB.inserisci_intervCella (): - Eccezione nella chiusura della connessione= " + e2);
				}
			}
		}

			}

	protected int inseriscoAgendaInterv(ISASConnection dbc,Hashtable h)
			throws Exception
			{
		// 1) prelevo il max progr inserito per quel giorno
		ISASRecord dbmax = null;
		int progressivo=0;
		String selmax = "SELECT MAX(ag_progr) max "+
				" FROM agendant_interv " + 
				" WHERE ag_data =" +formatDate(dbc,h.get("ag_data").toString());
		LOG.debug("AgendaEditEJB/inseriscoAgendaInterv, selmax= " + selmax);
		dbmax = dbc.readRecord(selmax);
		if (dbmax != null) {
			if(dbmax.get("max")!=null){
				int max = ((Integer)dbmax.get("max")).intValue();
				max++;
				progressivo = max;
			}
		}
		ISASRecord rec_ag = dbc.newRecord("agendant_interv"); //gb 14/09/07
		rec_ag.put("ag_data",ndf.formDate(h.get("ag_data").toString(),"aaaa-mm-gg"));
		rec_ag.put("ag_progr", new Integer(progressivo));
		rec_ag.put("ag_cartella",h.get("ag_cartella"));
		rec_ag.put("ag_contatto",h.get("ag_contatto"));
		rec_ag.put("cod_obbiettivo",h.get("cod_obbiettivo"));
		rec_ag.put("n_intervento",h.get("n_intervento"));
		rec_ag.put("ag_oper_ref",h.get("referente"));
		rec_ag.put("ag_tipo_oper", h.get("tipo_operatore"));//NO!!!! 19/06/2012 con ADMIN fa casino!!!(selectTipoOpe(dbc,(String)h.get("referente"))).get("tipo"));
		rec_ag.put("ag_orario", new Integer(h.get("ag_orario").toString()) );//((((String)h.get("ag_orario")).trim()).equals("P")?new Integer("1"):new Integer("0")));
		rec_ag.put("ag_oper_esec", "");
		rec_ag.put("ag_stato", (String)h.get("ag_stato"));//[PUA]"0");
		LOG.debug("AgendaEditEJB/inseriscoAgendaInterv, carico su agendant_interv il record: \n"+(rec_ag.getHashtable()).toString());
		dbc.writeRecord(rec_ag);
		LOG.info("inseriscoAgendaInterv " + rec_ag.getHashtable().toString());//elisa b 15/05/12
		return progressivo;
			}

	private ISASRecord leggoInterv(ISASConnection dbc,Hashtable hKey)throws SQLException, DBSQLException, DBMisuseException, ISASPermissionDeniedException, ISASMisuseException{
		ISASRecord dbr=null;
		Object oj=hKey.get("ag_data");
		String data="";
		if(oj instanceof java.sql.Date)  data=((java.sql.Date)oj).toString();
		else if(oj instanceof String)	data=(String)oj;
		int prog=Integer.parseInt((String)hKey.get("ag_progr"));
		String oper=(String)hKey.get("ag_oper_ref");
		String sel="select * from agendant_interv "+
				" where ag_data="+formatDate(dbc,data)+
				" and ag_oper_ref='"+oper.trim()+"'"+
				" and ag_progr="+prog;
		dbr=dbc.readRecord(sel);
		return dbr;		
	}
	private boolean removePrestazioni(ISASConnection dbc,ISASRecord dbrec)throws Exception{
		boolean done=false;
		ISASCursor dbcur=null;
		try{
			if (dbrec != null){
				String mysel ="SELECT * "+
						" from agendant_intpre "+
						" where ap_data="+formatDate(dbc,((java.sql.Date)dbrec.get("ag_data")).toString())+
						" and ap_oper_ref='"+((String)dbrec.get("ag_oper_ref")).trim()+"'"+
						" and ap_progr="+dbrec.get("ag_progr");
				dbcur=dbc.startCursor(mysel);
				Vector vdbr = dbcur.getAllRecord();
				if ((vdbr != null) && (vdbr.size() > 0))
					for(int j=0; j<vdbr.size(); j++) {
						ISASRecord dbrec2 = (ISASRecord)vdbr.elementAt(j);
						if (dbrec2 != null){
							String del="SELECT * "+
									" from agendant_intpre "+
									" where ap_data="+formatDate(dbc,((java.sql.Date)dbrec2.get("ap_data")).toString())+
									" and ap_oper_ref='"+((String)dbrec2.get("ap_oper_ref")).trim()+"'"+
									" and ap_prest_cod='"+((String)dbrec2.get("ap_prest_cod")).trim()+"'"+
									" and ap_progr="+dbrec2.get("ap_progr");
							ISASRecord is = dbc.readRecord(del);
							if(is!=null){
								LOG.debug("delete="+is.getHashtable().toString());
								dbc.deleteRecord(is);
								LOG.info("cancellazione " + del);//elisa b 15/05/12
							}
						}
					}				
			}
			done = true;
			return done;
		}finally{
			close_dbcur_nothrow(nomeEJB+"removePrestazioni", dbcur);
		}
	}

	public Vector combo_contatti(myLogin mylogin, Hashtable h) throws SQLException
	{
		boolean done = false;
		Vector vdbr = new Vector();
		ISASConnection dbc = null;
		ISASCursor dbcur=null;
		String tipoOper = "";
		try{
			dbc = super.logIn(mylogin);
			LOG.info("combo_contatti "+h.toString());
			//mod 31/05/12 String sel=faiSelectContatti(dbc,h.get("tipo_operatore").toString(),h);
			String sel=faiSelectContatti(dbc, h.get("tipo_operatore").toString(),h);
			LOG.debug("select per combo contatti"+sel);	               
			if(!sel.equals("")) {//aggiunto 31/05/12
				dbcur=dbc.startCursor(sel);	   			
				vdbr = dbcur.getAllRecord();
				if(dbcur!=null)
					dbcur.close();
			}	              
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e1){
			LOG.debug(""+e1);
			throw new SQLException("Errore eseguendo una COMBO_CONTATTI() - "+  e1);
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					dbc.close();//bargi 17/02/2012
					super.close(dbc);//17/02/2012	
				}catch(Exception e2){LOG.debug(""+e2);}
			}
		}
	}

	/*
	private String faiSelectContatti(ISASConnection dbc,String tipoOper,Hashtable h)
	{
	    String  myselect="";
	    String data=h.get("ag_data").toString();
	    String cartella=h.get("ag_cartella").toString();
	    ServerUtility su =new ServerUtility ();
	    //if (tipoOper.equals("01")){

	    //}else
	    if (tipoOper.equals("02")){
//	gb 07/05/07:	infermieri: non si usa pi� contsan, si va direttamente
//			nella tabella infermieri 'skinf'		
	        myselect = "SELECT c.*, c.ski_descr_contatto descr," +
	                    " c.ski_data_apertura data_contatto," +
			    		" c.ski_data_uscita data_chiusura" +
	                    " FROM skinf c" +
	                    " WHERE c.ski_descr_contatto IS NOT NULL"+
	        			"  and ski_data_apertura<="+formatDate(dbc,data)+
	        			" and (ski_data_uscita is null or ski_data_uscita>="+formatDate(dbc,data)+")";
	    } else if (tipoOper.equals("03")){
		    myselect = "SELECT c.*, c.skm_descr_contatto descr," +
	                    " c.skm_data_apertura data_contatto," +
			    		" c.skm_data_chiusura data_chiusura"+
	                    " FROM skmedico c" +
	                    " WHERE c.skm_descr_contatto IS NOT NULL"+
	        			"  and skm_data_apertura<="+formatDate(dbc,data)+
	        			" and (skm_data_uscita is null or skm_data_uscita>="+formatDate(dbc,data)+")";
	   }else if (tipoOper.equals("04")){
	       	myselect = "SELECT c.*, c.skf_descr_contatto descr," +
	                    " c.skf_data data_contatto," +
			    		" c.skf_data_chiusura data_chiusura" +
	                    " FROM skfis c" +
	                    " WHERE c.skf_descr_contatto IS NOT NULL"+
	        			"  and skf_data_apertura<="+formatDate(dbc,data)+
	        			" and (skf_data_uscita is null or skf_data_uscita>="+formatDate(dbc,data)+")";
	   } else if (tipoOper.equals("52")){ // 18/12/06: x l'oncologo si usano i campi dell'ostetrica
			 myselect = "SELECT c.*, c.skm_descr_contatto descr," +
	                    " c.skm_data_apertura data_contatto," +
					    " c.skm_data_chiusura data_chiusura" +
	                    " FROM skmedpal c" +
	                    " WHERE c.skm_descr_contatto IS NOT NULL"+
	        			"  and skm_data_apertura<="+formatDate(dbc,data)+
	        			" and (skm_data_uscita is null or skm_data_uscita>="+formatDate(dbc,data)+")";
	   }
	    else{
	      System.out.println("ERRORE--> SI STA CERCANDO DI INSERIRE UN CONTATTO PER UN OPERATORE CHE E'"+
	             "DI TIPO "+ tipoOper +" OPERATORI AMMESSI 01;02;03;04;52");
	    }
	   if (!myselect.equals(""))
	              myselect = su.addWhere(myselect, su.REL_AND, "c.n_cartella",su.OP_EQ_NUM,cartella);
	   return myselect;
	}*/

	private String faiSelectContatti(ISASConnection dbc, String tipoOper,Hashtable h)throws Exception
	{
		String  myselect="";
		ServerUtility su =new ServerUtility ();
		if (tipoOper.equals("01")){
			//bargi 17/02/2012 per il caso 01 si carica come accesso occasionale
			myselect="";
			System.out.println("CASO ASSISTENTE SOCIALE "+
					" DI TIPO "+ tipoOper );
			//bargi 17/02/2012 da ultimare lato client 
			return faiSelectInterventi(h);
		}else if (tipoOper.equals("02")){
			myselect = "SELECT c.*, '00000000' cod_obbiettivo,'0' n_intervento,c.ski_descr_contatto descr," +
					" c.ski_tipocura descr_vco," + 
					" c.ski_data_apertura data_contatto," +
					" c.ski_data_uscita data_chiusura" +
					" FROM skinf c" +
					" WHERE ((c.ski_descr_contatto IS NOT NULL)" +
					" OR (c.ski_tipocura IS NOT NULL)) " +
					"and c.ski_data_apertura <= "+ formatDate(dbc,(String) h.get("ag_data")) +
					"and (c.ski_data_uscita is null OR c.ski_data_uscita >= " + formatDate(dbc,(String) h.get("ag_data")) + " ) "; 
		} else if (tipoOper.equals("03")){
			myselect = "SELECT c.*, '00000000' cod_obbiettivo,'0' n_intervento, c.skm_descr_contatto descr," +
					" c.skm_tipocura descr_vco," +
					" c.skm_data_apertura data_contatto," +
					" c.skm_data_chiusura data_chiusura"+
					" FROM skmedico c" +
					" WHERE (c.skm_descr_contatto IS NOT NULL  or c.skm_tipocura IS NOT NULL) "+
					"and c.skm_data_apertura <= "+ formatDate(dbc,(String) h.get("ag_data")) +
					"and (c.skm_data_chiusura is null OR c.skm_data_chiusura >= " + formatDate(dbc,(String) h.get("ag_data")) + " ) "; 
		}else if (tipoOper.equals("04")){
			myselect = "SELECT c.*, '00000000' cod_obbiettivo,'0' n_intervento, c.skf_descr_contatto descr," +
					" c.skf_data data_contatto," +
					" c.skf_data_chiusura data_chiusura" +
					" FROM skfis c" +
					" WHERE c.skf_descr_contatto IS NOT NULL"+
					"and c.skf_data <= "+ formatDate(dbc,(String) h.get("ag_data")) +
					"and (c.skf_data_chiusura is null OR c.skf_data_chiusura >= " + formatDate(dbc,(String) h.get("ag_data")) + " ) "; 
		} else if (tipoOper.equals("52")){ // 18/12/06: x l'oncologo si usano i campi dell'ostetrica
			//gb 07/05/07:	oncologi: non si usa pi� contsan, si va direttamente
			//			nella tabella oncologi 'skmedpal'
			myselect = "SELECT c.*, '00000000' cod_obbiettivo,'0' n_intervento, c.skm_descr_contatto descr," +
					" c.skm_data_apertura data_contatto," +
					" c.skm_data_chiusura data_chiusura" +
					" FROM skmedpal c" +
					" WHERE c.skm_descr_contatto IS NOT NULL" +
					"and c.skm_data_apertura <= "+ formatDate(dbc,(String) h.get("ag_data")) +
					"and (c.skm_data_chiusura is null OR c.skm_data_chiusura >= " + formatDate(dbc,(String) h.get("ag_data")) + " ) "; 
		}   else{
			System.out.println("ERRORE--> SI STA CERCANDO DI INSERIRE UN CONTATTO PER UN OPERATORE CHE E'"+
					"DI TIPO "+ tipoOper +" OPERATORI AMMESSI 01;02;03;04;52");
		}
		if (!myselect.equals(""))
			myselect = su.addWhere(myselect, su.REL_AND, "c.n_cartella",su.OP_EQ_NUM,(String)h.get("ag_cartella"));
		return myselect;
	}
	private String faiSelectContattiOld(String tipoOper,Hashtable h)throws Exception
	{
		String  myselect="";
		ServerUtility su =new ServerUtility ();
		/*gb 07/05/07:	Per il caso "01" (assistenti sociali) si utilizza intervASEJB.java
			quindi in questo caso (cio� caso "01") non ci siamo mai. 	   
	    if (tipoOper.equals("01")){
	           myselect=" SELECT c.descr_sociale descr,"+
	                     " c.data_sociale data_contatto,c.data_chius_sociale data_chiusura,"+
	                     " c.n_cartella,c.n_contatto,co.tipo_servizio "+
	                     " FROM contsan c, contatti co "+
	                     " WHERE  descr_sociale is not null"+
	                     " AND c.n_cartella=co.n_cartella AND c.n_contatto=co.n_contatto ";
	                     String TipServ="";
	                      if (h.get("tipo_servizio")!=null)
	                           TipServ=(String)h.get("tipo_servizio");
	                      if (!TipServ.equals(""))
	                          myselect += " AND co.tipo_servizio ="+ TipServ;
	    } else
	gb 07/05/07 *******/

		if (tipoOper.equals("01")){
			//bargi 17/02/2012 per il caso 01 si carica come accesso occasionale
			myselect="";
			System.out.println("CASO ASSISTENTE SOCIALE "+
					" DI TIPO "+ tipoOper );
			//bargi 17/02/2012 da ultimare lato client faiSelectInterventi(h);
		}else if (tipoOper.equals("02")){
			//gb 07/05/07:	infermieri: non si usa pi� contsan, si va direttamente
			//			nella tabella infermieri 'skinf'
			/*gb 07/05/07 *******
	           myselect="SELECT descr_infer descr,"+
	                    "data_infer data_contatto,data_chius_infer data_chiusura,"+
	                    " n_contatto,n_cartella "+
	                    " FROM contsan c "+
	                    " WHERE descr_infer is not null";
	gb 07/05/07 *******/
			/*** 11/07/06
	//gb 07/05/07
	           myselect="SELECT ski_descr_contatto descr,"+
	                    " ski_data_apertura data_contatto," +
			    " ski_data_uscita data_chiusura,"+
	                    " n_contatto, n_cartella "+
	                    " FROM skinf c "+
	                    " WHERE ski_descr_contatto is not null";
	//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.ski_descr_contatto descr," +
					" c.ski_tipocura descr_vco," + // 14/07/10
					" c.ski_data_apertura data_contatto," +
					" c.ski_data_uscita data_chiusura" +
					" FROM skinf c" +
					" WHERE ((c.ski_descr_contatto IS NOT NULL)" +
					" OR (c.ski_tipocura IS NOT NULL))"; // 14/07/10
		} else if (tipoOper.equals("03")){
			//gb 07/05/07:	medici: non si usa pi� contsan, si va direttamente
			//			nella tabella medici 'skmedico'
			/*gb 07/05/07 *******
	           myselect="SELECT descr_medico descr,"+
	                    "data_medico data_contatto,data_chius_medico data_chiusura,"+
	                    " n_contatto,n_cartella "+
	                    " FROM contsan c "+
	                    " WHERE descr_medico is not null";
	gb 07/05/07 *******/
			/*** 11/07/06
	//gb 07/05/07
	           myselect="SELECT skm_descr_contatto descr,"+
	                    " skm_data_apertura data_contatto," +
			    " skm_data_chiusura data_chiusura,"+
	                    " n_contatto, n_cartella "+
	                    " FROM skmedico c "+
	                    " WHERE skm_descr_contatto is not null";
	//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.skm_descr_contatto descr," +
					" c.skm_tipocura descr_vco," +
					" c.skm_data_apertura data_contatto," +
					" c.skm_data_chiusura data_chiusura"+
					" FROM skmedico c" +
					" WHERE (c.skm_descr_contatto IS NOT NULL  or c.skm_tipocura IS NOT NULL)";
		}else if (tipoOper.equals("04")){
			//gb 07/05/07:	fisioterapeuti: non si usa pi� contsan, si va direttamente
			//			nella tabella fisioterapeuti 'skfis'
			/*gb 07/05/07 *******
	           myselect="SELECT descr_fisiot descr,"+
	                    "data_fisiot data_contatto,data_chius_fisiot data_chiusura,"+
	                    " n_contatto,n_cartella "+
	                    " FROM contsan c "+
	                    " WHERE descr_fisiot  is not null";
	gb 07/05/07 *******/
			/*** 11/07/06
	//gb 07/05/07
	           myselect="SELECT skf_descr_contatto descr," +
	                    " skf_data data_contatto," +
			    " skf_data_chiusura data_chiusura," +
	                    " n_contatto, n_cartella " +
	                    " FROM skfis c " +
	                    " WHERE skf_descr_contatto is not null";
	//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.skf_descr_contatto descr," +
					" c.skf_data data_contatto," +
					" c.skf_data_chiusura data_chiusura" +
					" FROM skfis c" +
					" WHERE c.skf_descr_contatto IS NOT NULL";
		} else if (tipoOper.equals("52")){ // 18/12/06: x l'oncologo si usano i campi dell'ostetrica
			//gb 07/05/07:	oncologi: non si usa pi� contsan, si va direttamente
			//			nella tabella oncologi 'skmedpal'
			/*gb 07/05/07 *******
	           myselect="SELECT descr_ostetr descr,"+
	                    "data_ostetr data_contatto,data_chius_ostetr data_chiusura,"+
	                    " n_contatto,n_cartella"+
	                    " FROM contsan c "+
	                    " WHERE descr_ostetr is not null";
	gb 07/05/07 *******/
			/*** 11/07/06
	//gb 07/05/07
	           myselect="SELECT skm_descr_contatto descr," +
	                    " skm_data_apertura data_contatto," +
			    " skm_data_chiusura data_chiusura," +
	                    " n_contatto, n_cartella" +
	                    " FROM skmedpal c " +
	                    " WHERE skm_descr_contatto is not null";
	//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.skm_descr_contatto descr," +
					" c.skm_data_apertura data_contatto," +
					" c.skm_data_chiusura data_chiusura" +
					" FROM skmedpal c" +
					" WHERE c.skm_descr_contatto IS NOT NULL";
		}
		else{
			System.out.println("ERRORE--> SI STA CERCANDO DI INSERIRE UN CONTATTO PER UN OPERATORE CHE E'"+
					"DI TIPO "+ tipoOper +" OPERATORI AMMESSI 01;02;03;04;52");
		}
		if (!myselect.equals(""))
			myselect = su.addWhere(myselect, su.REL_AND, "c.n_cartella",su.OP_EQ_NUM,(String)h.get("n_cartella"));
		return myselect;
	}

	// 14/06/06 m.
	// 19/06/07 m.: modificato decodifica da TAB_INTERVENTI x suddivisione in 3 tabelle
	// 	+	sostituito descr della comboBox:
	//		-> era "numProg: dtProg - descObb - numInterv: descInterv (eventuale dtChiusInterv): flag"
	//		-> diventa "descSettore - descTipo - descInterv (eventuale dtChiusInterv): flag"
	private String faiSelectInterventi(Hashtable h0) throws Exception
	{
		String cart = (String)h0.get("ag_cartella");
		String oper = (String)h0.get("cod_oper");
		String myselect = "SELECT i.n_progetto n_contatto," +
				" i.cod_obbiettivo," +
				" i.n_intervento," +
				" i.int_cod_intervento," +
				" ap.*," + // 11/06/07
				" tb.des_obbiettivo," +
				" ti.des_intervento," +
				" ti.dettaglio_intervento," + // 03/07/08
				" to_char(i.n_progetto) || '#' || TRIM(i.cod_obbiettivo) || '#' ||to_char(i.n_intervento) kcombo," +
				" to_char(i.n_intervento) || ': '" +
				" || SUBSTR(ts.des_settore_interv, 1, 20) || ' - '" +
				" || SUBSTR(tt.des_tipo_interv, 1, 20) || ' - '" +
				" || SUBSTR(ti.des_intervento, 1, 20) descr" +
				// 19/06/07 -----
				" FROM ass_interventi i," +
				" ass_progetto ap," +
				" tab_obbiettivi tb," +
				" tab_interventi ti," +
				// 03/07/08				" ass_opabil oa," +
				// 19/06/07 -----
				" tab_settore_interv ts," +
				" tab_tipo_interv tt" +
				// 19/06/07 -----
				" WHERE i.n_cartella = " + cart +
				" AND ap.n_cartella = i.n_cartella" +
				" AND ap.n_progetto = i.n_progetto" +
				" AND tb.cod_obbiettivo = i.cod_obbiettivo" +
				// 19/06/07			" AND ti.cod_intervento = i.int_cod_intervento" +
				// 19/06/07 -----
				" AND ts.cod_settore_interv = SUBSTR(i.int_cod_intervento,1,2)" +
				" AND tt.cod_settore_interv = ts.cod_settore_interv" +
				" AND tt.cod_tipo_interv = SUBSTR(i.int_cod_intervento,3,2)" +
				" AND ti.cod_settore_interv = ts.cod_settore_interv" +
				" AND ti.cod_tipo_interv = tt.cod_tipo_interv" +
				" AND ti.cod_intervento = SUBSTR(i.int_cod_intervento,5,4)" +
				// 19/06/07 -----
				" AND i.int_accessi = 'S'" +
				/** 03/07/08: accessi effettuati anche da oper non AS e non collegati al progetto (per es. OTA-OSA)
					" AND oa.n_cartella = i.n_cartella" +
					" AND oa.n_progetto = i.n_progetto" +
					" AND oa.opabil_cod = '" + oper + "'" +
				 **/
				 // 30/01/08	    	" ORDER BY i.int_data_autorizzato DESC, i.int_data_concl DESC, i.n_intervento DESC";
				 " ORDER BY i.int_data_ins DESC, i.n_intervento DESC";

		return myselect;
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

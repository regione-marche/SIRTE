package it.caribel.app.sinssnt.bean;

//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//********NUOVA RELEASE GIUGNO 2012 bargi versione 12_03_01

//29/09/2008 bargi tolto il caricamento in agenda delle prestazioni pianificate ogni operatore si carica le prestazioni consapevolmente. 
//25/02/2008 aggiunto aggiornamento record su puauvm per i casi PUA bargi
//10/12/2007 aggiunto caso stato 9 PUA bargi
//21/11/2007 - aggiunto il distretto nel caricamento degli operatori
//19/04/2007 - EJB di connessione alla procedura SINS sinss 
//richiamata dai moduli client: 
//--->JFrameAgendaModOperatore per la lettura delle prestazioni in agenda e per l'eventuale registrazione.
//
//bargi
//==========================================================================

import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.sinssnt.controlli.CollSinsPua;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
//bargi 30/09/2008

public class AgendaModOperatoreEJB extends SINSSNTConnectionEJB
{
	myLogin mylogin_global=null;
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	CollSinsPua collSins=new CollSinsPua();
	public AgendaModOperatoreEJB() {}
//	Metodi richiamati 
	/**
	 JFrameAgendaModOperatore ---->  registra_prestaz
	 */
//	*************SELEZIONATA COLONNA
//	registrazione accessi e prestazioni e aggiornamenti flag stati vari****************
	/**
	 *   Esegue le seguenti operazioni:
	 */
	@SuppressWarnings("deprecation")
	public String cambia_operatore(myLogin mylogin, @SuppressWarnings("rawtypes") Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException{
		String methodName = "cambia_operatore";
		mylogin_global=mylogin;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			debugMessage("AgendaModOperatore/cambia_operatore: apro");
			debugMessage("AgendaModOperatore/cambia_operatore: hashtable arrivata="+h.toString());
			
			dbc.startTransaction();
			//29/09/2008 bargi caricoSettimana(dbc,h);
			@SuppressWarnings("rawtypes")
			Hashtable ht=(Hashtable)h.get("appuntamenti");
			@SuppressWarnings("rawtypes")
			Enumeration eDate=ht.keys();
			while(eDate.hasMoreElements())
			{
				String data=(String)eDate.nextElement();
				System.out.println("DATA key==>"+data);
				@SuppressWarnings("rawtypes")
				Hashtable hC=(Hashtable)ht.get(data);					
				System.out.println("hC"+hC.toString());
				@SuppressWarnings("rawtypes")
				Enumeration eCart=hC.keys();
				while(eCart.hasMoreElements())
				{
					String cartella=(String)eCart.nextElement();                 
					@SuppressWarnings("rawtypes")
					Vector vP=(Vector)hC.get((cartella));
					debugMessage("aggiorno:["+data+"],\n"+",["+cartella+"],\n["+vP.toString()+"]");
					aggiornamenti(dbc,data,cartella,vP,h);
				}
			}
			dbc.commitTransaction();
			debugMessage("AgendaModOperatore/cambia_operatore: CHIUDO");
			String msg="Operazione completata con successo.";
			return msg;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ e.getStackTrace()[0].getMethodName() + ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(methodName, dbc);
		}
	}
	
	private void aggiornamenti(ISASConnection dbc,String data,String cartella,Vector progressivi,Hashtable h)throws SQLException
	{
		try{
			Enumeration en=progressivi.elements();
			while(en.hasMoreElements())
			{
				String prog=(String)en.nextElement();
				String mysel = "SELECT * "+
//				gb 18/09/07				" from agenda_interv"+
				" from agendant_interv" + //gb 18/09/07
				" where ag_data="+formatDate(dbc,data)+
				" and ag_progr="+prog+
				" and ag_stato in (0,3,9)"+//[PUA]
				" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
				debugMessage("AgendaModOperatore/aggiornamenti, SELECT mysel: "+mysel);
				ISASRecord dbr=dbc.readRecord(mysel);
				if (dbr != null)
				{ 
					int ag_progr=caricoAppuntAgendaInterv(dbc,dbr,h);
					caricoAppuntAgendaIntpre(dbc,dbr,h,ag_progr);
					dbc.deleteRecord(dbr);
				}		
			}						
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaModOperatore: Errore eseguendo una aggiornamenti() - "+  e1);
		}
	}
	
	private int caricoAppuntAgendaInterv(ISASConnection dbc,ISASRecord dbrOld,Hashtable h)
	throws Exception
	{
		String oper=(String)h.get("referente_new");
		boolean done=false;
		ISASRecord dbr = null;
		try{
			//controllo se per stessa cartella esiste gi� appuntamento in agenda del nuovo operatore
			//in modo da accodarla
			String selprog = "SELECT * "+
			" FROM agendant_interv " + //gb 19/09/07
			" WHERE ag_data =" +formatDate(dbc,((java.sql.Date)dbrOld.get("ag_data")).toString())+
			" AND ag_oper_ref='"+oper+"' and "+
			" ag_cartella="+dbrOld.get("ag_cartella")+" and "+
			" ag_contatto="+dbrOld.get("ag_contatto")+" and "+			
			" n_intervento = " + dbrOld.get("n_intervento") + " and "+
			" ag_orario="+dbrOld.get("ag_orario");
			if (! ((String)dbrOld.get("cod_obbiettivo")).trim() .equals(""))
				selprog+=" and cod_obbiettivo = '" + (String)dbrOld.get("cod_obbiettivo") + "'" ;
			else 	 selprog+=" and cod_obbiettivo is null" ;
			debugMessage("AgendaModOperatore/caricoAppuntAgendaInterv, selprog: "+selprog);
			dbr = dbc.readRecord(selprog);
			int progressivo=0;
			if (dbr != null)
			{
				//aggiorna stato agenda se diverso da zero
				progressivo=((Integer)dbr.get("ag_progr")).intValue();
				String stato=(String)dbr.get("ag_stato");
				if(!stato.equals("0")&& !stato.equals("9"))//[PUA]
				{
					progressivo= -1;//non posso accodare
				}
			}
			
			if(dbr==null || progressivo==-1)
			{
				// 1) prelevo il max progr inserito per quel giorno
				ISASRecord dbmax = null;
				String selmax = "SELECT MAX(ag_progr) max "+
				" FROM agendant_interv " + //gb 19/09/07
				" WHERE ag_data =" +formatDate(dbc,((java.sql.Date)dbrOld.get("ag_data")).toString())+
				" AND ag_oper_ref='"+oper+"'";
				//  debugMessage("select su agenda_interv="+selmax);
				dbmax = dbc.readRecord(selmax);
				if (dbmax != null)
				{
					if(dbmax.get("max")!=null)
					{
						int max = ((Integer)dbmax.get("max")).intValue();
						max++;
						progressivo = max;
					}
				}
//				debugMessage("progressivo="+progressivo);
				//2)inserisco nuovo record su agenda_interv
//				gb 19/09/07		ISASRecord rec_ag = dbc.newRecord("agenda_interv");
				ISASRecord rec_ag = dbc.newRecord("agendant_interv"); //gb 19/09/07
				Hashtable h_read = dbrOld.getHashtable();
				Enumeration n=h_read.keys();
				while(n.hasMoreElements())
				{
					String e=(String)n.nextElement();
					rec_ag.put(e,h_read.get(e));
				}
				rec_ag.put("ag_progr", new Integer(progressivo));
				rec_ag.put("ag_oper_ref",oper);
				/*if(rec_ag.get("pr_data")!=null && rec_ag.get("pr_progr")!=null){							  
					updatePuauvm(dbc,rec_ag.getHashtable(),"I");
				}*/
				if(rec_ag.get("num_scheda_pua")!=null && !(rec_ag.get("num_scheda_pua").toString()).equals("")){
					Hashtable hUpd=new Hashtable();
					hUpd.put("data",rec_ag.get("ag_data").toString());
					hUpd.put("num_scheda",rec_ag.get("num_scheda_pua"));
					hUpd.put("cod_oper",oper);
					collSins.designaOperatore(dbc,hUpd,"I");				
				}
				debugMessage("AgendaModOperatore/caricoAppuntAgendaInterv******carico su agenda_interv il record: \n"+(rec_ag.getHashtable()).toString());
				dbc.writeRecord(rec_ag);
				done=true;
			}
			return progressivo;
		}
		catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaModOperatore: Errore eseguendo una caricoAppuntAgendaInterv() - "+  e1);
		}        
	}
	
	private void caricoAppuntAgendaIntpre(ISASConnection dbc,ISASRecord dbrOld,Hashtable h,int progressivo)
	throws Exception
	{
		ISASCursor dbcur=null;
		boolean done=false;
		try{
			String oper=(String)h.get("referente_new");
			//leggo prestazioni da spostare
			String sel = "SELECT *" +
//			gb 19/09/07			" FROM agenda_intpre WHERE "+
			" FROM agendant_intpre WHERE "+ //gb 19/09/07
			" ap_data="+formatDate(dbc,((java.sql.Date)dbrOld.get("ag_data")).toString())+
			" AND ap_oper_ref='"+dbrOld.get("ag_oper_ref")+"'"+
			" AND ap_progr="+dbrOld.get("ag_progr");
			debugMessage("AgendaModOperatore/caricoAppuntAgendaIntpre, sel: "+sel);
			dbcur=dbc.startCursor(sel);
			while (dbcur.next())
			{  
				ISASRecord dbrc=dbcur.getRecord();
				sel = "SELECT *" +
//				gb 19/09/07			" FROM agenda_intpre WHERE "+
				" FROM agendant_intpre WHERE "+ //gb 19/09/07
				"ap_data="+formatDate(dbc,((java.sql.Date)dbrc.get("ap_data")).toString())+
				" AND ap_oper_ref='"+oper+"'"+
				" AND ap_progr="+progressivo+
				" AND ap_prest_cod='"+dbrc.get("ap_prest_cod")+"'";
				debugMessage("AgendaModOperatore/caricoAppuntAgendaIntpre, sel (2): "+sel);
				ISASRecord is = dbc.readRecord(sel);
				if(is==null)
				{
					Hashtable h_read = dbrc.getHashtable();
					Enumeration n=h_read.keys();
//					gb 19/09/07		   ISASRecord rec_ag = dbc.newRecord("agenda_intpre");
					ISASRecord rec_ag = dbc.newRecord("agendant_intpre"); //gb 19/09/07
					while(n.hasMoreElements())
					{
						String e=(String)n.nextElement();
						rec_ag.put(e,h_read.get(e));
					}
					rec_ag.put("ap_progr", new Integer(progressivo));
					rec_ag.put("ap_oper_ref",oper);
					dbc.writeRecord(rec_ag);
				}
				else
				{
					debugMessage("**** IL RECORD RISULTA CARICATO: \n"+(is.getHashtable()).toString());
					//non carico nuovamente l aprestazione e proseguo
				}
				String sel2="SELECT *" +
//				gb 19/09/07			    " FROM agenda_intpre WHERE "+
				" FROM agendant_intpre WHERE "+ //gb 19/09/07
				" ap_data="+formatDate(dbc,((java.sql.Date)dbrc.get("ap_data")).toString())+
				" AND ap_oper_ref='"+dbrc.get("ap_oper_ref")+"'"+
				" AND ap_progr="+dbrc.get("ap_progr")+
				" AND ap_prest_cod='"+dbrc.get("ap_prest_cod")+"'";
				debugMessage("AgendaModOperatore/caricoAppuntAgendaIntpre, sel2 (3): "+sel2);
				ISASRecord is2 = dbc.readRecord(sel2);
				dbc.deleteRecord(is2);
			}
			if (dbcur != null)dbcur.close();
			done=true;
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("AgendaModOperatore: Errore eseguendo una caricoAppuntAgendaIntpre() - "+  e1);
		}
		finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e2){debugMessage(""+e2);}
			}
		}
	}

	public Vector CaricaTabellaOperatori(myLogin mylogin, Hashtable h)
	throws SQLException{
		boolean done = false;
		Vector vdbr = new Vector();
		ISASConnection dbc = null;
		ISASCursor dbcur=null;
		String msg="";
		try{
			dbc = super.logIn(mylogin);
			//String tipoOper=(String)h.get("tipo_operatore");			   
			String myselect="Select codice cod_oper,"+
			"nvl(trim(cognome),'')||' '||nvl(trim(nome),'') desc_oper "+
			" from operatori where "+
			" tipo='"+(String)h.get("tipo_operatore")+"'";
			if(!((String)h.get("zona")).equals(""))
				myselect+=" AND cod_zona='"+(String)h.get("zona")+"'";
			if(!((String)h.get("presidio")).equals(""))				
				myselect+=" AND cod_presidio='"+(String)h.get("presidio")+"'";
			else if(!((String)h.get("distretto")).equals(""))//bargi [distretto]
				myselect+=" AND cod_presidio in ("+getPresidi((String)h.get("distretto"),(String)h.get("zona"))+")";
			
			myselect+="order by cognome,nome";
			debugMessage("AgendaModOperatore/CaricaTabellaOperatori, myselect: "+myselect);
			dbcur = dbc.startCursor(myselect);
			debugMessage("AgendaModOperatore/CaricaTabellaOperatori, numero recs letti:"+dbcur.getDimension());
			vdbr=dbcur.getAllRecord();
			if (dbcur != null) //gb 18/09/07            
				dbcur.close();
			dbc.close();                
			super.close(dbc);
			done = true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("AgendaModOperatore: Errore eseguendo una CaricaTabellaOperatori()  ");
		}finally{
			if(!done){
				try{
					if (dbcur!=null)dbcur.close();                                
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Vector CaricaTabellaOperatoriNew(myLogin mylogin, Hashtable h)
	throws SQLException{
		Vector vdbr = new Vector();
		ISASConnection dbc = null;
		ISASCursor dbcur=null;
		try{
			dbc = super.logIn(mylogin);
			String selectPrelievi = " select conf_txt from conf where conf_kproc='SINS' and conf_key='PRESTPREL'";
			ISASRecord tmp = dbc.readRecord(selectPrelievi);
			String prelievi = (String) tmp.get("conf_txt");
			//String tipoOper=(String)h.get("tipo_operatore");			
	     	Vector v=(Vector)h.get("giorni");
	     	String primo_gg=(String)v.elementAt(0);
	     	String ultimo_gg=(String)v.elementAt(6);
	     	String tipoOperatore = (String)h.get("tipo_operatore");
//					"Select codice cod_oper,"+
//			"nvl(trim(cognome),'')||' '||nvl(trim(nome),'') desc_oper "+
//			" from operatori where "+
//			" tipo='"+(String)h.get("tipo_operatore")+"'";
            String outJoin_1 = dbc.getoutTab();
            String outJoin_2 = dbc.getoutCrit(); 
	     	
			String myselect=
			" SELECT  codice, codice as cod_oper, nvl(trim(cognome),'')||' '||nvl(trim(nome),'') as desc_oper ,ag_data, count(ag_data) as num_accessi" +
			" FROM agendant_interv anti, operatori" +
			" WHERE ag_data "+outJoin_2+">="+formatDate(dbc,primo_gg) +
			"   AND ag_data"+outJoin_2+"<="+formatDate(dbc,ultimo_gg) +
			"   AND ag_oper_ref"+outJoin_2+" = codice AND tipo='" + tipoOperatore + "'";
//			+
//			"   AND ag_tipo_oper='" + tipoOperatore + "' ";
			
			StringBuffer filtroZona = new StringBuffer();
			if(!((String)h.get("zona")).equals(""))
				filtroZona.append(" AND cod_zona='"+(String)h.get("zona")+"'");
			if(!((String)h.get("presidio")).equals(""))				
				filtroZona.append(" AND cod_presidio='"+(String)h.get("presidio")+"'");
			else if(!((String)h.get("distretto")).equals(""))//bargi [distretto]
				filtroZona.append(" AND cod_presidio in ("+getPresidi((String)h.get("distretto"),(String)h.get("zona"))+")");
			
			myselect+=filtroZona.toString();
			myselect+=" group by codice, nvl(trim(cognome),'')||' '||nvl(trim(nome),''),  ag_data " +
					  " order by codice, ag_data";
			debugMessage("AgendaModOperatore/CaricaTabellaOperatori, myselect: "+myselect);
			dbcur = dbc.startCursor(myselect);
			debugMessage("AgendaModOperatore/CaricaTabellaOperatori, numero recs letti:"+dbcur.getDimension());
			vdbr=dbcur.getAllRecord();
			Vector<ISASRecord> res; 
			if(vdbr != null && !vdbr.isEmpty()){
				res = new Vector<ISASRecord>();
				String operatore = (String) ((ISASRecord) vdbr.get(0)).get("codice");
				res.add((ISASRecord) vdbr.get(0));
				for (Iterator<ISASRecord> iterator = vdbr.iterator(); iterator.hasNext();) {
					ISASRecord rec = (ISASRecord) iterator.next();
					String opCurr = ISASUtil.getValoreStringa(rec, "codice");
					if(!operatore.equals(opCurr)){
						res.add(rec);
						rec.getDBRecord().put(decodificaGiorno(v, formDate(ISASUtil.getDateField(rec, "ag_data"), "gg-mm-aaaa")), ISASUtil.getValoreStringa(rec, "num_accessi"));
						operatore = opCurr;
					}
					tmp = (ISASRecord) res.elementAt(res.size()-1);
					tmp.getDBRecord().put(decodificaGiorno(v, formDate(ISASUtil.getDateField(rec, "ag_data"), "gg-mm-aaaa")), ISASUtil.getValoreStringa(rec, "num_accessi"));
				}
				if(tipoOperatore.equals(GestTpOp.CTS_COD_INFERMIERE)){
					String selectPrel = "SELECT ap_oper_ref, ap_data, count(distinct ap_progr) as prelievi " +
										"  FROM agendant_intpre a, operatori " +
										"  WHERE ap_data>="+formatDate(dbc, primo_gg) +
										"   AND ap_data<="+formatDate(dbc, ultimo_gg) +
										"   AND ap_oper_ref = codice and tipo='" + tipoOperatore + "' " +
										filtroZona.toString() +
										"  AND EXISTS (  SELECT * " +
										"  FROM agendant_intpre b " +
										"  WHERE a.ap_oper_ref=b.ap_oper_ref " +
										"  AND a.ap_data = b.ap_data " +
										"  AND a.ap_progr = b.ap_progr " +
										"  AND ap_prest_cod in (" + prelievi + "))" +
										"  GROUP BY ap_oper_ref, ap_data " +
										"  ORDER BY AP_OPER_REF, AP_DATA";
					dbcur = dbc.startCursor(selectPrel);
					Vector prel = dbcur.getAllRecord();
					Vector vprelievi;
					//se nessun operatore ha prelievi salto i cicli
					if(prel != null && !prel.isEmpty()){
						vprelievi = new Vector<ISASRecord>();
						operatore = (String) ((ISASRecord) prel.get(0)).get("ap_oper_ref");
						vprelievi.add(prel.get(0));
						for (Iterator<ISASRecord> iterator = prel.iterator(); iterator.hasNext();) {
							ISASRecord rec = (ISASRecord) iterator.next();
							String opCurr = ISASUtil.getValoreStringa(rec, "ap_oper_ref");
							if(!operatore.equals(opCurr)){
								vprelievi.add(rec);
								operatore = opCurr;
							}
							tmp = (ISASRecord) vprelievi.elementAt(vprelievi.size()-1);
							tmp.getDBRecord().put(decodificaGiorno(v, formDate(ISASUtil.getDateField(rec, "ap_data"), "gg-mm-aaaa")), ISASUtil.getValoreStringa(rec, "prelievi"));
						}
						int compare = 0;
						for (Iterator<ISASRecord> iterator = res.iterator(); iterator.hasNext();) {
							ISASRecord oper = (ISASRecord) iterator.next();
							operatore = ISASUtil.getValoreStringa(oper, "codice");
							for (Iterator iterator2 = vprelievi.iterator(); iterator2.hasNext();) {
								ISASRecord prelieviOp = (ISASRecord) iterator2.next();
								String opCurr = ISASUtil.getValoreStringa(prelieviOp, "ap_oper_ref");
								compare = operatore.compareTo(opCurr);
								if(compare == 0){
									String valore = "";
									for (int i = 0; i < v.size(); i++) {
										valore = StringUtils.defaultIfEmpty(ISASUtil.getValoreStringa(oper, "C"+i), "0") +" - " + 
													StringUtils.defaultIfEmpty(ISASUtil.getValoreStringa(prelieviOp, "C"+i), "0");
										oper.getDBRecord().put("C"+i,valore);
									}
//									oper.getDBRecord().put("num_accessi", StringUtils.defaultIfEmpty(ISASUtil.getValoreStringa(oper, "num_accessi"), "0") +" - " + 
//											StringUtils.defaultIfEmpty(ISASUtil.getValoreStringa(prelieviOp, "prelievi"), "0"));
									oper.getDBRecord().put("num_prelievi", StringUtils.defaultIfEmpty(ISASUtil.getValoreStringa(prelieviOp, "prelievi"), "0"));
									break;
								}else if(compare<0){
									break;
								}
							}
						}
					}
				}
			}else {
				res = vdbr;
			}
			return res;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ e.getStackTrace()[0].getMethodName() + ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(Thread.currentThread().getStackTrace()[0].getMethodName(), dbcur);
			logout_nothrow(Thread.currentThread().getStackTrace()[0].getMethodName(), dbc);
		}
		
	}
	
    private String decodificaGiorno(Vector<String> giorni, String valoreStringa) {
		for (int i = 0; i < giorni.size(); i++) {
			if(giorni.get(i).equals(valoreStringa)){
				return "C"+Integer.toString(i);
			}
		}
		return "";
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
		private Vector selectAgendaInterv(ISASConnection dbc,Hashtable h)throws SQLException{
     	String methodName="selectAgendaInterv";
//     	boolean done=false;
     	ISASCursor dbcur=null;
     	Vector v=(Vector)h.get("giorni");
     	String primo_gg=(String)v.elementAt(0);
     	String ultimo_gg=(String)v.elementAt(6);
     	try{
     		String mysel ="SELECT  anti.*," +
     				 " from agendant_interv anti "+ //gb 13/09/07
     				 " where ag_data>="+formatDate(dbc,primo_gg)+
     				 " and ag_data<="+formatDate(dbc,ultimo_gg)+
     				 " and ag_oper_ref='"+((String)h.get("referente")).trim()+"'"+
     				 " and ag_tipo_oper='"+((String)h.get("tipo_operatore")).trim()+"'"+//aggiunto bargi 19/06/2012
     				 " order by ag_data, ag_orario";
     		debugMessage("agendaEJB/selectAgendaInterv, mysel: "+mysel);
     		dbcur=dbc.startCursor(mysel);
     		Vector vdbr = dbcur.getAllRecord();
     		Vector rigatabella=new Vector();
     		ISASRecord recriga=null;
     		String chiave_new="";
     		String chiave_old="";
     		int index=0;
     		boolean sec=false;
     		// devo scorrere il vettore per decodificare l'operatore
     		if ((vdbr != null) && (vdbr.size() > 0))
     			for(int i=0; i<vdbr.size(); i++){
     				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
     				//debugMessage("record i="+i+" rec letto="+dbrec.getHashtable().toString());
     				if (dbrec != null){
     					String data=((java.sql.Date)dbrec.get("ag_data")).toString();
     					//						   debugMessage("selectAgendaInterv ******");
     					String gg=scorroVett(data,v);//ritorna lun mar etc
     					dbrec.put("data"+gg,data);
     					dbrec.put("progr"+gg,((Integer)dbrec.get("ag_progr")).toString());
     					dbrec.put("stato"+gg,(String)dbrec.get("ag_stato"));
     					chiave_new=((Integer)dbrec.get("ag_orario")).toString()+
     							((Integer)dbrec.get("ag_cartella")).toString()+
     							((Integer)dbrec.get("ag_contatto")).toString() +
     							//gb 13/09/07 *******
     							(String)dbrec.get("cod_obbiettivo") +
     							((Integer)dbrec.get("n_intervento")).toString();//+
     					debugMessage("AgendaEJB/selectAgendaInterv: chiave_new--> ["+chiave_new+"]");
     					debugMessage("AgendaEJB/selectAgendaInterv: chiave_old--> ["+chiave_old+"]");
     					//gb 13/09/07: fine *******
     					//no � pippo   ((Integer)dbrec.get("ag_progr")).toString();
     					if (!chiave_new.equals(chiave_old)){
     						if(!chiave_old.equals("")){
     							//debugMessage("carico in vettore=");
     							//debugMessage((recriga.getHashtable()).toString());
     							rigatabella.add(recriga);
     							//debugMessage("vettore=="+rigatabella.toString());
     						}
     						chiave_old=chiave_new;
     						selectAgendaIntpre(dbc, dbrec);
     						recriga=dbrec;
     						//debugMessage("rec "+(recriga.getHashtable()).toString());
     						recriga.put(gg,(String)dbrec.get("prestazioni"));
     						//debugMessage("nuova riga dopo="+(recriga.getHashtable()).toString());
     						index++;
     					}else{//stessa chiave
     						//debugMessage("stessa riga prima="+(recriga.getHashtable()).toString());
     						selectAgendaIntpre(dbc, dbrec);
     						recriga.put(gg,(String)dbrec.get("prestazioni"));
     					}
     					recriga.put("data"+gg,data);
     					recriga.put("progr"+gg,((Integer)dbrec.get("ag_progr")).toString());
     					recriga.put("stato"+gg,(String)dbrec.get("ag_stato"));
     					recriga.put("prestazioni_desc"+gg,(String)dbrec.get("prestazioni_desc"));

     					//debugMessage("stessa riga dopo="+(recriga.getHashtable()).toString());
     				}
     			}
     		if ((vdbr != null) && (vdbr.size() > 0)){
     			//debugMessage("ultima riga che carico in vettore=");
     			//debugMessage((recriga.getHashtable()).toString());
     			rigatabella.add(recriga);
     		}
     		//     		if (dbcur != null)dbcur.close();
     		//     		done = true;

     		return rigatabella;
     	} catch(Exception e){
     		throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
     	}finally {
     		close_dbcur_nothrow(methodName, dbcur);
     		// 			logout_nothrow(methodName, dbc);
     	}
    }
	
    @SuppressWarnings("unchecked")
 	private void selectAgendaIntpre(ISASConnection dbc,ISASRecord mydbr) throws Exception{
     	ISASCursor dbcur=null;
//         	boolean done=false;
     	try{
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
     		debugMessage("AgendaEJB/selectAgendaIntpre mysel="+mysel);
     		dbcur=dbc.startCursor(mysel);
     		String prestazioni="";
     		String descPrestazioni="";
     		String sep="";
     		String sep2="";
     		while (dbcur.next()){
     			ISASRecord dbrc=dbcur.getRecord();
 	    		if(dbrc!=null){
 	    			/*String fr="";
 		              int freq=((Integer)dbrc.get("ap_alert")).intValue();
 		              if (freq==1)fr="(Q)";
 		              else if (freq==2)fr="(M)";
 		              prestazioni+=sep+fr+(String)dbrc.get("ap_prest_cod");
 		            */
 	    			prestazioni+=sep+(String)dbrc.get("ap_prest_cod");
 	    			descPrestazioni+=sep2+decodificaGenerica("prestaz", "prest_cod", (String)dbrc.get("ap_prest_cod"), "prest_des", dbc);
 	    		}
 	    		sep="-";
 	    		sep2="\n";
     		}
//     		if (dbcur!=null) dbcur.close();
     		mydbr.put("prestazioni", prestazioni);
     		mydbr.put("prestazioni_desc", descPrestazioni);
//         		done=true;
     	} catch(Exception e){
     		throw newEjbException("Errore eseguendo selectAgendaIntpre: " + e.getMessage(), e);
     	}finally {
     		close_dbcur_nothrow("selectAgendaIntpre", dbcur);
//     		logout_nothrow("selectAgendaIntpre", dbc);
     	}
 	}// END selectAgendaIntpre

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
    }
    
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
    		{	
    			if(it==false)//aaaammgg
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
    
	
	
	private String getPresidi(String distretto,String zona){
		String select="select codpres from presidi where "+
		" codzon='"+zona+"' and coddistr='"+distretto+"'";
		return select;
	}
	/*
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
	}*/
	/*
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
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una selectAgendaInterv() - "+  e1);
		}
		
	}*/
	/*
	private void updatePuauvm(ISASConnection dbc,Hashtable h,String tipo)
	throws Exception
	{
		//tipo R --> rimosso  I -->inserim
		debugMessage("updatePuauvm "+h.toString()+" tipo="+tipo);
		try{
			String data=((java.sql.Date)h.get("pr_data")).toString();
			String data_vis=((java.sql.Date)h.get("ag_data")).toString();
			String sel="select * from puauvm where n_cartella="+h.get("ag_cartella")+
			" and pr_progr="+(Integer)h.get("pr_progr")+
			" and pr_data="+formatDate(dbc,data);
			debugMessage("updatePuauvm select  "+sel);
			ISASRecord dbr=dbc.readRecord(sel);
			if(dbr!=null){
				if(h.get("ag_tipo_oper")!=null ){
					if (((String)h.get("ag_tipo_oper")).equals("01")){//ass soc
						if(tipo.equals("I")){
						dbr.put("pr_soc_data_visita",ndf.formDate(data_vis,"aaaa-mm-gg"));
						dbr.put("pr_soc_codice",(String)h.get("ag_oper_ref"));
						}else if(tipo.equals("R")){
							dbr.put("pr_soc_data_visita",null);
							dbr.put("pr_soc_codice","");
							
						}
						dbc.writeRecord(dbr);
					}else if (((String)h.get("ag_tipo_oper")).equals("02")){//inferm
						if(tipo.equals("I")){
							dbr.put("pr_inf_data_visita",ndf.formDate(data_vis,"aaaa-mm-gg"));
							dbr.put("pr_inf_codice",(String)h.get("ag_oper_ref"));
							}else if(tipo.equals("R")){
								dbr.put("pr_inf_data_visita",null);
								dbr.put("pr_inf_codice","");								
							}
						dbc.writeRecord(dbr);
					}					
				}
			}
		}catch(Exception e){
			System.out.println("updatePuauvm(): Eccezione= " + e);
			throw new Exception("Errore eseguendo una updatePuauvm() - "+  e);
		}
	}
	*/
	
	/**
	 * elisa b 07/06/11
	 * metodo che permette di trasferire tutta la pianificazione di un operatore
	 * ad un altro
	 */
	public void trasferisciPianificazione(myLogin mylogin, Hashtable h)
	throws SQLException {
		ISASConnection dbc = null;
		boolean done = false;
		ISASCursor dbcur = null;
		Hashtable hApp = new Hashtable();
		Hashtable hPar = new Hashtable();
		Hashtable hTmp = new Hashtable();
		String condizCartella = "";
		System.out.println("DATA h-- "+h.toString());
		String dtInizio = h.get("data_inizio").toString();
		
		String dtFine = "31/12/3000";
		if((h.get("data_fine") != null) && (!h.get("data_fine").equals("")))
			dtFine = h.get("data_fine").toString();
		System.out.println("DATA dtInizio " + dtInizio);
		//caso in cui sono selezionati alcuni assistiti
		if(h.containsKey("assistititi")){
			String assistiti = h.get("assistititi").toString();
			assistiti = assistiti.replaceAll("\\|", ",");
			condizCartella = " AND ag_cartella IN(" + assistiti + ")";
		}
		
		try {
			dbc = super.logIn(mylogin);

			String mysel = "SELECT *"
					+ " FROM agendant_interv"
					+ " WHERE ag_data >= " + formatDate(dbc, dtInizio)
					+ " AND ag_data <= " + formatDate(dbc, dtFine)
					+ " AND ag_oper_ref = '" + ((String) h.get("referente")).trim() + "'"					
					+ condizCartella //elisa b 06/07/11
					+ " ORDER BY ag_cartella, ag_contatto, cod_obbiettivo, n_intervento, ag_orario, ag_data";
			
			debugMessage("agendaModOperatoreEJB/selectAgendaInterv, mysel: " + mysel);
			
			dbcur = dbc.startCursor(mysel);
			Vector vdbr = dbcur.getAllRecord();
			String chiave = "";
			
			/* creo un hashtable con la struttura seguente: 
			 * hApp={data={chiave=[v]}}}
			 */
			if ((vdbr != null) && (vdbr.size() > 0))
				for (int i = 0; i < vdbr.size(); i++) {
					ISASRecord dbrec = (ISASRecord) vdbr.elementAt(i);	
					
					
					if (dbrec != null) {
						String data = dbrec.get("ag_data").toString();
						
						chiave = ((Integer) dbrec.get("ag_cartella")).toString()
								+ "-" 
								+ ((Integer) dbrec.get("ag_contatto")).toString()
								+ "-" 
								+ (String) dbrec.get("cod_obbiettivo")
								+ "-" 
								+ ((Integer) dbrec.get("n_intervento")).toString();
						
						if(dbrec.get("ag_orario").toString().equals("0"))
							chiave += "-M";
						else
							chiave += "-P";
								
						//debugMessage("agendaModOperatoreEJB/selectAgendaInterv: chiave "+ chiave);
						
						if(hTmp.containsKey(data))
							hTmp = (Hashtable)hApp.get(data);					
						
						if(!hTmp.containsKey(chiave)){
							Vector v = new Vector();
							v.add(dbrec.get("ag_progr").toString());
							hTmp.put(chiave, v);
						}else{
							Vector v = (Vector)hTmp.get(chiave);
							v.add(dbrec.get("ag_progr").toString());
							hTmp.put(chiave, v);
						}	
						
						hApp.put(data, hTmp);
						
						// debugMessage("stessa riga dopo="+(recriga.getHashtable()).toString());
					}
				}
			
			//creo un hashtable con la struttura che si aspetta il metodo esistente
			hPar.put("referente_new", (String)h.get("referente_new"));
			hPar.put("referente", (String)h.get("referente"));
			
			dbc.startTransaction();

			Enumeration eDate = hApp.keys();
			while(eDate.hasMoreElements()){
				String data=(String)eDate.nextElement();
				System.out.println("DATA key==>"+data);
				Hashtable hC = (Hashtable)hApp.get(data);					
				System.out.println("hC"+hC.toString());
				Enumeration eCart=hC.keys();
				while(eCart.hasMoreElements()){
					String cartella = (String)eCart.nextElement();                 
					Vector vP = (Vector)hC.get((cartella));
					debugMessage("aggiorno:["+data+"],\n"+",["+cartella+"],\n["+vP.toString()+"]");
					aggiornamenti(dbc,data,cartella,vP,hPar);
				}
			}
			
			if (dbcur != null)
				dbcur.close();
			done = true;
			
			dbc.commitTransaction();
			//dbc.rollbackTransaction();
			super.close(dbc);
			System.out.println("COMMIT!!");
			//System.out.println("rollbackTransaction!!");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"AgendaModOperatoreEJB: Errore eseguendo una selectAgendaInterv() - "+ e);
		} finally {
			if (!done) {
				try {
					if (dbcur != null)
						dbcur.close();
					dbc.rollbackTransaction();
					System.out.println("ROLLBACK!!");
					super.close(dbc);
				} catch (Exception e2) {
					debugMessage("" + e2);
				}
			}
		}
}
}

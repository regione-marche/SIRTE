package it.caribel.app.sinssnt.bean.modificati;

//============================================================================
//CARIBEL S.r.l.
//----------------------------------------------------------------------------
////********NUOVA RELEASE GIUGNO 2012 bargi  versione 12_03_01 

//30/06/2004 - EJB di connessione alla procedura SINS Tabella AgendaSett
//
//bargi 12/07/2012 alla ripianificazone agenda si passa anche la prestazione in modo che venga ricaricata solo quella modificata!
//bargi 01/04/2011 La frequenza non la devo considerare per la prima prestazione che pianifico o di cui modifico la frequenza
//============================================================================

import java.util.*;
import java.sql.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.properties.CaptionSideMaker;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.sinssnt.controlli.CaricaAgendaPrestazioni;

public class AgendaSettEJB extends SINSSNTConnectionEJB  {
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	dateutility dt=new dateutility();
	ServerUtility su=new ServerUtility();
	//boolean ctrlFreq=true;//bargi 01/04/2011 La frequenza non la devo considerare per la prima prestazione che pianifico
	public AgendaSettEJB() {}

	CaricaAgendaPrestazioni carica = new CaricaAgendaPrestazioni(); //bargi 25/09/2008
	/**
	 *   Esegue le seguenti operazioni:
	 *   1) seleziona da agenda_sett_tipo per vedere se
	 *   esistono delle pianif gia� caricate
	 *   1) seleziona da agenda_sett_tipo le prestazioni pianificate
	 *   nella settimana inerenti all'operatore referente
	 *   2)controlla la data fine e se inferiore alla data da caricare
	 *   non viene caricato e viene rimosso da agenga_sett_tipo
	 */
	public Vector query_agenda(myLogin mylogin, Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		try{
			dbc = super.logIn(mylogin);
			//dbc.startTransaction();
			Vector vdbr = new Vector();
			//scaricaScaduti(dbc,h);
			vdbr=selectAgendaSettTipo(dbc,h);
			///dbc.commitTransaction();
			
			done = true;
			// String msg="Operazione completata con successo.";
			return vdbr;
		}catch(DBRecordChangedException e){
			throw e;
		}catch(ISASPermissionDeniedException e){
			throw e;
		}catch(Exception e){
			throw newEjbException("Errore eseguendo una query_agenda() - ",  e);
		}finally{
			//if (!done)
			//	rollback_nothrow("query_agenda", dbc);
			logout_nothrow("query_agenda", dbc);
		}

	}

	private Vector selectAgendaSettTipo(ISASConnection dbc,Hashtable h)throws SQLException{
		String methodName = "selectAgendaSettTipo";
		boolean done=false;
		ISASCursor dbcur=null;
		//LOG.debug("hashtable ceh arriva="+h.toString());
		try{
			//ServerUtility su=new ServerUtility();
			String data_oggi = null;
			if(h.containsKey("startPianificazione")){
				data_oggi = ISASUtil.getValoreStringa(h, "startPianificazione");
			}else{
				data_oggi=su.getTodayDate("dd/MM/yyyy");
			}
			
			//			gb 30/08/07     String mysel ="SELECT agenda_sett_tipo.n_cartella,as_op_referente,as_tipo_oper,"+
			//			gb 30/08/07     "agenda_sett_tipo.n_contatto,as_orario,as_data,as_prog,as_giorno_sett gg,"+
			String mysel ="SELECT ast.n_cartella, as_op_referente, as_tipo_oper,"+
			"ast.n_contatto, ast.cod_obbiettivo, ast.n_intervento, as_orario, as_data, as_prog, as_giorno_sett gg,"+
			"nvl(trim(cognome),'') || ' ' ||  nvl(trim (nome),'') assistito,"+
			"nvl(trim(indirizzo_rep),'') || ' ' ||  nvl(trim (comuni.descrizione),'')"+
			" || ' ' ||  nvl(trim (prov_rep),'')  indirizzo " +
			" ,piano_accessi.n_progetto "+ 
			" from cartella, anagra_c, agendant_sett_tipo ast, piano_accessi, comuni " + //gb 30/08/07
			" where as_op_referente='"+((String)h.get("as_op_referente")).trim()+"'"+
			" and as_tipo_oper='"+((String)h.get("as_tipo_oper")).trim()+"'"+
			" and cartella.n_cartella = ast.n_cartella" + //gb 30/08/07
			" and data_variazione in (select max(data_variazione) from anagra_c where "+
			" anagra_c.n_cartella = ast.n_cartella)" + //gb 30/08/07
			" and anagra_c.n_cartella = ast.n_cartella" + //gb 30/08/07
			" and comuni.codice=comune_rep"+
			" and piano_accessi.cod_obbiettivo = ast.cod_obbiettivo"+//bargi 17/04/2008
			" and piano_accessi.n_intervento = ast.n_intervento"+//bargi 17/04/2008		
			" and piano_accessi.pa_tipo_oper = ast.as_tipo_oper"+
			" and piano_accessi.n_cartella = ast.n_cartella"+
			" and piano_accessi.n_progetto = ast.n_contatto"+
			" and piano_accessi.pa_data = ast.as_data"+
			" and piano_accessi.pi_prog = ast.as_prog"+
			//bargi 12/06/2012	" and (piano_accessi.pi_data_fine IS NULL OR piano_accessi.pi_data_fine>="+formatDate(dbc,data_oggi)+")"+
			" and (piano_accessi.pi_data_fine IS NULL" +
				" OR piano_accessi.pi_data_fine>="
				//elisa b 19/07/12 ripristinata data oggi
				//+formatDate(dbc,h.get("pi_data_inizio").toString())+
				+formatDate(dbc,data_oggi)+
			")"+
			" order by ast.n_cartella, ast.n_contatto, ast.cod_obbiettivo, ast.n_intervento, as_data, as_orario, as_giorno_sett";
			//			gb 04/09/07: fine *******
			LOG.debug("SELECT agendant_sett_tipo"+mysel);
			dbcur=dbc.startCursor(mysel);
			LOG.debug("SELECT dimensione"+dbcur.getDimension());
			Vector vdbr = dbcur.getAllRecord();
			Vector rigatabella=new Vector();
			ISASRecord recriga=null;
			String chiave_new="";
			String chiave_old="";
			int index=0;
			boolean sec=false;
			// devo scorrere il vettore per decodificare l'operatore
			if ((vdbr != null) && (vdbr.size() > 0))
				for(int i=0; i<vdbr.size(); i++) {
					ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
					//LOG.debug("record i="+i+" rec letto="+dbrec.getHashtable().toString());
					if (dbrec != null){
						//String data=((java.sql.Date)dbrec.get("as_data")).toString();
						//String gg=scorroVett(data,v);//ritorna lun mar etc
						//dbrec.put("data"+gg,data);
						//dbrec.put("progr"+gg,((Integer)dbrec.get("as_prog")).toString());
						chiave_new=((Integer)dbrec.get("as_orario")).toString()+
						((Integer)dbrec.get("n_cartella")).toString()+
						((Integer)dbrec.get("n_contatto")).toString()+
						(String)dbrec.get("cod_obbiettivo") + //gb 04/09/07
						((Integer)dbrec.get("n_intervento")).toString() + //gb 04/09/07
						((java.sql.Date)dbrec.get("as_data")).toString();
						String gg=((Integer)dbrec.get("gg")).toString();
						if (!chiave_new.equals(chiave_old)){
							if(!chiave_old.equals("")){
								rigatabella.add(recriga);
							}
							chiave_old=chiave_new;
							selectPianoInterv(dbc, dbrec);
							recriga=dbrec;
							//LOG.debug("riga... "+(recriga.getHashtable()).toString());
							recriga.put(gg,(String)dbrec.get("prestazioni"));
							recriga.put("prestazioni_desc"+gg,(String)dbrec.get("prestazioni_desc"));
							
							//LOG.debug("aggiornato riga...="+(recriga.getHashtable()).toString());
							index++;
						}else {//stessa chiave
							//LOG.debug("stessa chiave aggiorno riga="+(recriga.getHashtable()).toString());
							selectPianoInterv(dbc, dbrec);
							String w_gg="";
							String w_gg_desc="";
							if(recriga.get(gg)!=null )
								w_gg=(String)recriga.get(gg);
							if(recriga.get("prestazioni_desc"+gg)!=null )
								w_gg_desc=(String) recriga.get("prestazioni_desc"+gg);
							String sep="-";
							String sep2="\n";
							if (w_gg.equals("")) sep="";
							if (w_gg_desc.equals("")) sep2="";
							recriga.put(gg,w_gg+sep+(String)dbrec.get("prestazioni"));
							recriga.put("prestazioni_desc"+gg, w_gg_desc+sep2+(String)dbrec.get("prestazioni_desc"));
							//recriga.put(gg,(String)dbrec.get("prestazioni"));                            }
							//LOG.debug("stessa riga dopo="+(recriga.getHashtable()).toString());
						}
					}
				}
			if ((vdbr != null) && (vdbr.size() > 0)){
				//LOG.debug("ultima riga che carico in vettore=");
				//LOG.debug((recriga.getHashtable()).toString());
				rigatabella.add(recriga);
			}
			dbcur.close();
			done = true;
			//LOG.debug("size vettore="+rigatabella.size());
			return rigatabella;
			//		}catch(Exception e1){
			//			LOG.error(""+e1);
			//			throw new SQLException("Errore eseguendo una selectAgendaSettTipo() - "+  e1);
			//		}
			//		finally{
			//			if(!done){
			//				try{
			//					if (dbcur != null)
			//						dbcur.close();
			//				}catch(Exception e2){LOG.error(""+e2);}
			//			}
			//		}
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(methodName, dbcur);
//			logout_nothrow(methodName, dbc);
		}
	}//end selectAgendaSEttTipo--
	private void selectPianoInterv(ISASConnection dbc,ISASRecord mydbr) throws Exception
	{
		Hashtable h = mydbr.getHashtable();
		//LOG.debug("hash che arriva a pianointerv"+h.toString());
		String data = ((java.sql.Date)h.get("as_data")).toString();
		String cartella=((Integer)h.get("n_cartella")).toString();
		//		gb 04/09/07        String contatto=((Integer)h.get("n_contatto")).toString();
		//mod 30/05/12 String strNProgetto =((Integer)h.get("n_contatto")).toString(); //gb 04/09/07
		String strNProgetto =((Integer)h.get("n_progetto")).toString(); 

		String strCodObiettivo = (String)h.get("cod_obbiettivo"); //gb 04/09/07
		//		gb 04/09/07 *******
		String strNIntervento = "0";
		if (h.get("n_intervento") != null)
			strNIntervento = ((Integer)h.get("n_intervento")).toString();
		//		gb 04/09/07: fine *******
		String tipo_op=(String)h.get("as_tipo_oper");
		int prog = ((Integer)h.get("as_prog")).intValue();
		ISASCursor dbcur=null;
		//		gb 04/09/07 *******
		String strClausoleXSociale = "";
		if (tipo_op.equals("01"))
			strClausoleXSociale = " AND cod_obbiettivo = '" + strCodObiettivo + "'" +
			" AND n_intervento = " + strNIntervento;
		//		gb 04/09/07: fine *******
		//		gb 04/09/07        String mysel = "SELECT pi_prest_cod FROM pianointerv "+
		String mysel = "SELECT pi_prest_cod FROM piano_accessi "+ //gb 04/09/07
		//		gb 04/09/07        " where skpa_data="+formatDate(dbc,data)+
		" where pa_data="+formatDate(dbc,data)+ //gb 04/09/07
		" and n_cartella="+cartella+
		//		gb 04/09/07        " and n_contatto="+contatto+
		" and n_progetto = " + strNProgetto + //gb 04/09/07
		strClausoleXSociale +  //gb 04/09/07
		//		gb 04/09/07        " and pi_tipo_oper='"+tipo_op+"'"+
		" and pa_tipo_oper='"+tipo_op+"'"+ //gb 04/09/07
		" and pi_prog="+prog;
		LOG.trace("select piano_accessi = "+mysel);
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
    			prestazioni+=sep+(String)dbrc.get("pi_prest_cod");
    			descPrestazioni+=sep2+decodificaGenerica("prestaz", "prest_cod", (String)dbrc.get("pi_prest_cod"), "prest_des", dbc);
    		}
    		sep="-";
    		sep2="\n";
		}
		if (dbcur!=null) dbcur.close();
		mydbr.put("prestazioni", prestazioni);
		mydbr.put("prestazioni_desc", descPrestazioni);
	}// END selectPianoInterv

	/**
	 *  fase inserimento su agenda_sett_tipo
	 */
	public Hashtable insert_agenda(myLogin mylogin,Hashtable hin)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		String methodName = "insert_agenda";
		boolean done=false;
		Hashtable hRit=null;
		String cartella=null;
		//		gb 03/09/07		String contatto=null;
		//		gb 03/09/07 *******
		String strNProgetto=null;
		String strCodObiettivo=null;
		String strNIntervento=null;
		//		gb 03/09/07: fine *******
		String data=null;
		String prest=null;
		String oper=null;
		String tipo=null;
		String prog=null;
		String progressivi=null;
		String prestazioni=null;
		LOG.debug("insert_agenda Hashtable: "+hin.toString());
		ISASConnection dbc=null;
		try {
			cartella=(String)hin.get("n_cartella");
			strNProgetto=(String)hin.get("n_progetto");
			strCodObiettivo=(String)hin.get("cod_obbiettivo");
			strNIntervento=(String)hin.get("n_intervento");
			data=formDate((String)hin.get("pa_data"),"aaaa-mm-gg"); 
			hin.put("pa_data",data); 
	
			String data_w=formDate((String)hin.get("pi_data_inizio"),"aaaa-mm-gg");
			hin.put("pi_data_inizio",data_w);
			if(!((String)hin.get("pi_data_fine")).equals("")){
				data_w=formDate((String)hin.get("pi_data_fine"),"aaaa-mm-gg");
				hin.put("pi_data_fine",data_w);
			}
			oper=(String)hin.get("as_op_referente");
			tipo=(String)hin.get("as_tipo_oper");
			prestazioni=(String)hin.get("prestazioni");
			progressivi=(String)hin.get("progressivi");

		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria", e);
		}

		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();		
			String msg_err="";
			StringTokenizer st_prest = new StringTokenizer(prestazioni,"-");
			StringTokenizer st_prog = new StringTokenizer(progressivi,"-");
			String dataInput="";
			if(hin.get("pi_data_inizio")!=null && hin.get("data_input")!=null ) {
				if(dt.confrontaDate(hin.get("pi_data_inizio").toString(), hin.get("data_input").toString())<2) {
					dataInput=hin.get("pi_data_inizio").toString();
				}else 	dataInput=(String)hin.get("data_input");
			}
			while(st_prest.hasMoreTokens())
			{
				prest = st_prest.nextToken();
				prog  = st_prog.nextToken();
				hin.put("pi_prest_cod",prest);
				hin.put("as_prog",prog);
				Vector hv1=(Vector)hin.get("peragenda");
				int i = 0;
				for(Enumeration en=hv1.elements(); en.hasMoreElements(); ){
					Vector v2=(Vector)en.nextElement();
					for(Enumeration enum2=v2.elements(); enum2.hasMoreElements(); ){
						String h=(String)enum2.nextElement();
						ISASRecord dbage=dbc.newRecord("agendant_sett_tipo");
						dbage.put("n_cartella",new Integer(cartella));
						dbage.put("n_contatto",new Integer(strNProgetto));
						dbage.put("cod_obbiettivo", strCodObiettivo);
						dbage.put("n_intervento",new Integer(strNIntervento));
						dbage.put("as_data",data);
						dbage.put("as_prog",new Integer(Integer.parseInt(prog)));
						dbage.put("as_prest_cod",prest);//bargi 11/06/2012
						dbage.put("as_op_referente",oper);
						dbage.put("as_tipo_oper",tipo);
						String ora= ""+i; //h.substring(0,1);
//						if (ora.equals("M"))
//							dbage.put("as_orario","0");
//						else  dbage.put("as_orario","1");
						dbage.put("as_orario", ora);
						String gg=h; //.substring(1);
						int giorno=Integer.parseInt(gg);
						giorno=giorno-2;
						dbage.put("as_giorno_sett",""+giorno);
						dbc.writeRecord(dbage);
						LOG.debug("agendant_sett_tipo " + dbage.getHashtable().toString());//elisa b 15/05/12
					}
					i++;
				}//end for
				
				//	Vector vSettimane=new Vector();
				/*
				 * carico su agenda le nuove prestazioni pianificate
				 */
				if(!dataInput.equals("")){
					Hashtable h_par=new Hashtable();
					h_par.put("data_inizio_ag", dataInput);
					h_par.put("n_cartella",cartella);
					h_par.put("data_inizio_caric", dataInput);
					h_par.put("referente", oper);
					h_par.put("tipo_operatore", tipo);
					h_par.put("pi_prest_cod",prest);
					//LOG.debug("AgendaSett  carico agenda passando: "+h_par.toString());
					//h_par.put("data_fine_ag", arg1)			           
					String ms=carica.ripianificaAgenda(dbc, h_par,false);
					//LOG.debug("AgendaSett  carico agenda ritorna: "+ms);
					
					if(!ms.equals(""))  msg_err+="\n"+ms;
				}
			} //end while
			
			
	
			LOG.debug("AgendaSettEJB.insert_agenda faccio commit!");
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			String msg="Operazione completata con successo "+msg_err;
			hRit=new Hashtable();
			hRit.put("msg",msg);
			hRit.put("prog",""+prog);
			return hRit;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(methodName, dbc);
		}
	}

	/**
	 *  fase aggiornamento agenda_sett_tipo e su pianointerv
	 */
	@SuppressWarnings("rawtypes")
	public Hashtable update_agenda(myLogin mylogin,Hashtable hin)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		Hashtable hRit=null;
		String cartella=null;
		String strNProgetto = null;
		String strCodObiettivo = null;
		String strNIntervento = null;
		String data=null;
		String prest=null;
		String oper=null;
		String tipo=null;
		String prog=null;
		String progressivi = null;
		String prestazioni = null;
		LOG.debug("update_agenda Hashtable: "+hin.toString());
		ISASConnection dbc=null;
		String msg_err="";
		try {
			cartella=(String)hin.get("n_cartella");
			strNProgetto=(String)hin.get("n_progetto");
			strCodObiettivo=((String)hin.get("cod_obbiettivo")).trim();
			strNIntervento = (String)hin.get("n_intervento");
			prog=(String)hin.get("as_prog");	
			progressivi=(String)hin.get("progressivi");
			data=formDate((String)hin.get("pa_data"),"aaaa-mm-gg"); //gb 04/09/07
			prest=(String)hin.get("pi_prest_cod");
			prestazioni=(String)hin.get("prestazioni");
			oper=(String)hin.get("as_op_referente");
			tipo=(String)hin.get("as_tipo_oper");			
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria", e);
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String dataInput="";
			if(hin.get("data_input")!=null)
				dataInput=(String)hin.get("data_input");
			//fase cancellazione
			LOG.debug("FASE CANCELLAZIONE");
			Vector hv1_rm=(Vector)hin.get("peragenda_rm");
			int i = 0; 
			for(Enumeration enum_rm=hv1_rm.elements(); enum_rm.hasMoreElements(); ){
				Vector v2_rm=(Vector)enum_rm.nextElement();
				for(Enumeration enum2_rm=v2_rm.elements(); enum2_rm.hasMoreElements(); ){
					String h_rm=(String)enum2_rm.nextElement();
					//System.out.println("Dentro vettore interno!"+h_rm);
					StringTokenizer st_new=new StringTokenizer(progressivi,"-");
					while(st_new.hasMoreTokens()){
						prog=(st_new.nextToken()).trim();
//						String ora_rm=h_rm.substring(0,1);
//						String orario="";
//						if (ora_rm.equals("M"))
//							orario="0";
//						else  orario="1";
						String orario = ""+i;
						String gg_r=h_rm;//.substring(1);
						int giorno_rm=Integer.parseInt(gg_r);
						giorno_rm=giorno_rm-2;
						String sel_rm="select * from agendant_sett_tipo " + //gb 04/09/07
								"where as_op_referente='"+oper+"'"+
								" and as_giorno_sett="+giorno_rm+
								" and as_orario="+orario+
								" and n_cartella="+cartella+
								" and n_contatto = " + strNProgetto +
								" and cod_obbiettivo = '" + strCodObiettivo + "'" +
								" and n_intervento = " + strNIntervento +
								" and as_data="+formatDate(dbc,data)+
								" and as_prog="+prog+
								" and as_tipo_oper='"+tipo+"'";
						ISASRecord dbr=dbc.readRecord(sel_rm);
						//LOG.debug("cancello rec da agenda_sett_tipo ="+(dbr.getHashtable()).toString());
						if(dbr!=null) {
							dbc.deleteRecord(dbr);
							LOG.debug("cancellazione " + sel_rm);//elisa b 15/05/12
						}
					}
				}//end for interno
				i++;
			}//end for
			//FINE FASE CANCELLAZIONE
			//fase inserimento
			LOG.debug("FASE INSERIMENTO AGENDA SETTIMANALE");
			i = 0;
			Vector hv1=(Vector)hin.get("peragenda");
			for(Enumeration en=hv1.elements(); en.hasMoreElements(); ){
				Vector v2=(Vector)en.nextElement();
				for(Enumeration enum2=v2.elements(); enum2.hasMoreElements(); ){
					String h=(String)enum2.nextElement();
					StringTokenizer st_new=new StringTokenizer(progressivi,"-");
					StringTokenizer st_prest=new StringTokenizer(prestazioni,"-");
					while(st_new.hasMoreTokens()){
						prog=(st_new.nextToken()).trim();
						prest=(st_prest.nextToken()).trim();
						ISASRecord dbage=dbc.newRecord("agendant_sett_tipo"); 
						dbage.put("n_cartella",new Integer(cartella));
						dbage.put("n_contatto", new Integer(strNProgetto));
						dbage.put("cod_obbiettivo", strCodObiettivo);
						dbage.put("n_intervento", new Integer(strNIntervento));
						dbage.put("as_data",data);
						dbage.put("as_prog",new Integer(prog));
						dbage.put("as_op_referente",oper);
						dbage.put("as_prest_cod",prest);//bargi 11/06/2012
						dbage.put("as_tipo_oper",tipo);
//						String ora=h.substring(0,1);
//						if (ora.equals("M"))
//							dbage.put("as_orario",new Integer("0"));
//						else  dbage.put("as_orario",new Integer("1"));
						dbage.put("as_orario",new Integer(i));
						String gg=h; //.substring(1);
						int giorno=Integer.parseInt(gg);
						giorno=giorno-2;
						dbage.put("as_giorno_sett",new Integer(giorno));
						dbc.writeRecord(dbage);
						LOG.debug("update_agenda " + dbage.getHashtable().toString());//elisa b 15/05/12
					}
				}//end for interno
				i++;
			}//end for
			//FINE fase inserimento
			String msg="Operazione completata con successo.";
			
			if(!dataInput.equals("")) {
				StringTokenizer st_prest=new StringTokenizer(prestazioni,"-");
				while(st_prest.hasMoreTokens()){
					prest=(st_prest.nextToken()).trim();
					Hashtable h_par=new Hashtable();
					h_par.put("data_inizio_ag", dataInput);
					h_par.put("data_inizio_caric", dataInput);
					h_par.put("referente", oper);
					h_par.put("n_cartella",cartella);
					h_par.put("tipo_operatore", tipo);
					h_par.put("pi_prest_cod",prest);
					msg_err+=carica.ripianificaAgenda(dbc,h_par,false);//per prima sett non deve controllare la frequenza
				}
			}
			LOG.debug("AgendaSettEJB.insert_agenda faccio commit!");
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;

			if(!msg_err.equals(""))  msg=msg_err;
			hRit=new Hashtable();
			hRit.put("msg",msg);
			hRit.put("prog",""+prog);
			return hRit;
		} catch(Exception e){
			try {
				dbc.rollbackTransaction();
			} catch (Exception e1){
				throw newEjbException("Errore eseguendo update_agenda: " + e.getMessage(), e);
			}
			throw newEjbException("Errore eseguendo update_agenda: " + e.getMessage(), e);
		}finally {
			logout_nothrow("update_agenda", dbc);
		}
	}


	/*
	private void insert_appuntamenti(ISASConnection dbc,Hashtable hin,Vector hv1,Hashtable hSett,boolean ctrlFreq)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
	{
		boolean done=false;
		String dataIn=null;
		String dataInput=null;
		String dataFi=null;
		debugMessage("insert appuntamenti Hashtable: "+hin.toString());
		ISASCursor dbcur=null;
		Vector vSett=new Vector();
		try {
			dataIn=formDate((String)hSett.get("data_inizio"),"aaaa-mm-gg");
			dataFi=formDate((String)hSett.get("data_fine"),"aaaa-mm-gg");
			vSett=carica.getSettimanaDa(dataIn);
			DataWI dtIn=new DataWI(formDate(dataIn,"aaaammgg"),1);
			DataWI dtFi=new DataWI(formDate(dataFi,"aaaammgg"),1);			
			String data_inizio=formDate((String)hin.get("pi_data_inizio"),"aaaammgg");
			String data_fine=formDate((String)hin.get("pi_data_fine"),"aaaammgg");
			if((dtFi.isSuccessiva(data_inizio))&&
					(data_fine.equals("00000000") || dtIn.isPrecedente(data_fine)))
			{
				debugMessage("procedo a inserimento appuntamento");
			}
			else
			{
				debugMessage("NON procedo a inserimento appuntamento");
				return;
			}

		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{

			for(Enumeration en=hv1.elements(); en.hasMoreElements(); ){
				Vector v2=(Vector)en.nextElement();
				for(Enumeration enum2=v2.elements(); enum2.hasMoreElements(); ){
					String h=(String)enum2.nextElement();
					// System.out.println("Dentro vettore interno!"+h);
					String ora=h.substring(0,1);
					String orario="";
					if (ora.equals("M"))
						orario="0";
					else  orario="1";
					String gg_r=h.substring(1);
					int giorno=Integer.parseInt(gg_r);
					giorno=giorno-2;
					String dataInsert=(String)vSett.elementAt(giorno);//data da inserire
					debugMessage("DATA CHE INSERISCO in agenda..."+dataInsert);
					dataInput=formDate(hin.get("data_input"),"aaaa-mm-gg");

					DataWI dInp=new DataWI(formDate(dataInput,"aaaammgg"),1);
					String datainizio=formDate((String)hin.get("pi_data_inizio"),"aaaammgg");
					DataWI dInizio=new DataWI(datainizio,1);
					debugMessage("DATA Inizio prestazione..."+datainizio);
					if(dInp.isSuccessiva(formDate(dataInsert,"aaaammgg")))
					{
						debugMessage("data_input>data_insert?"+dataInput+">"+dataInsert);
						debugMessage("Non proseguo data input � succ a data che voglio inserire");
					}else{
						debugMessage("data_input<=data_insert?"+dataInput+"<="+dataInsert);
						debugMessage("Proseguo data input � prec a data che voglio inserire");
						if(!dInizio.isSuccessiva(formDate(dataInsert,"aaaammgg")))
						{
							debugMessage("data_inizio<=data_insert?"+datainizio+"<="+dataInsert);
							debugMessage("Proseguo datainsert � success a data inizio prestazione");
							hin.put("orario",orario);
							//devo controllare se data inizio prestazione � > data che carico!
//							gb 04/09/07			ISASRecord dbrInterv=dbc.newRecord("agenda_interv");;
							ISASRecord dbrInterv=dbc.newRecord("agendant_interv"); //gb 04/09/07
							Hashtable hRit=caricoAgendaInterv(dbc,dataInsert,hin,dbrInterv);
							boolean isNew=((Boolean)hRit.get("carica")).booleanValue();
							int progr=((Integer)hRit.get("progr")).intValue();
							if(progr>=0){
								boolean caricata=caricoAgendaIntpre(dbc,hin,progr,dataInsert,ctrlFreq);
								if(isNew&&caricata){
									dbc.writeRecord(dbrInterv);   
									LOG.debug("insert_appuntamenti (agendaInterv)" + dbrInterv.getHashtable().toString());//elisa b 15/05/12
								}
							}
						}
						else{
							debugMessage("non inserisco appuntamento data di inizio superiore a data agenda");
							debugMessage("data_ininizio>data_insert?"+datainizio+">"+dataInsert);
						}

					}
				}}
			if (dbcur != null)
				dbcur.close();
			done=true;
		}catch(DBRecordChangedException e){
			System.out.println("AgendaSettEJB.query_caricati(): Eccezione= " + e);
			throw new DBRecordChangedException("Errore eseguendo una query_caricati() - "+  e);
		}catch(ISASPermissionDeniedException e){
			System.out.println("AgendaSettEJB.insert_appuntamenti(): Eccezione= " + e);
			throw new ISASPermissionDeniedException("Errore eseguendo una query_caricati() - "+  e);
		}catch(Exception e){
			System.out.println("AgendaSettEJB.insert_appuntamenti(): Eccezione= " + e);
			throw new SQLException("Errore eseguendo una query_caricati() - " +  e);
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	 */
	//private boolean// caricoAgendaInterv(ISASConnection dbc,String data,Hashtable h,ISASRecord rec_ag,int progressivo)
	private Hashtable caricoAgendaInterv(ISASConnection dbc,String data,Hashtable h,ISASRecord rec_ag)
	throws Exception
	{
		Hashtable hRit=new Hashtable();
		hRit.put("progr",new Integer(-1));
		hRit.put("carica",new Boolean(false));
		String cartella=(String)h.get("n_cartella");
		//		gb 04/09/07		String contatto=(String)h.get("n_contatto");
		//		gb 04/09/07 *******
		String strNProgetto = (String) h.get("n_progetto");
		String strCodObiettivo = (String) h.get("cod_obbiettivo");
		String strNIntervento = (String) h.get("n_intervento");
		//		gb 04/09/07: fine *******
		//String prest=(String)hin.get("pi_prest_cod");
		String oper=(String)h.get("as_op_referente");
		String tipo_oper_referente = (String)h.get("as_tipo_op_referente"); //gb 04/09/07
		String orario=(String)h.get("orario");
		ISASRecord dbr = null;
		// ISASRecord rec_ag =null;
		try{
			String selprog = "SELECT * "+
			//			gb 04/09/07                        " FROM agenda_interv "+
			" FROM agendant_interv "+
			" WHERE ag_data =" +formatDate(dbc,data)+" and "+
			" ag_oper_ref='"+oper+"' and "+
			" ag_cartella="+cartella+" and "+
			//			gb 04/09/07                        " ag_contatto="+contatto+" and "+
			//			gb 04/09/07 *******
			" ag_contatto = " + strNProgetto + " and " +
			" cod_obbiettivo = '" + strCodObiettivo + "' and " +
			" n_intervento = " + strNIntervento + " and "+
			//			gb 04/09/07: fine *******
			" ag_orario="+orario;
			debugMessage("select su agendant_interv="+selprog);
			dbr = dbc.readRecord(selprog);
			int progressivo=0;
			if (dbr != null) {
				//aggiorna stato agenda se diverso da zero
				progressivo=((Integer)dbr.get("ag_progr")).intValue();
				String stato=(String)dbr.get("ag_stato");
				if(!stato.equals("0")){
					progressivo= -1; //non posso proseguire
					hRit.put("progr",new Integer(-1));
					hRit.put("carica",new Boolean(false));
					return hRit;
				}
				hRit.put("progr",new Integer(progressivo));
				hRit.put("carica",new Boolean(false));
				return hRit;
			}else{
				// 1) prelevo il max progr inserito per quel giorno
				ISASRecord dbmax = null;
				String selmax = "SELECT MAX(ag_progr) max "+
				//				gb 04/09/07                                  " FROM agenda_interv "+
				" FROM agendant_interv " + //gb 04/09/07
				" WHERE ag_data =" +formatDate(dbc,data)+
				" AND ag_oper_ref='"+oper+"'";//20/03/2007
				//  debugMessage("select su agenda_interv="+selmax);
				dbmax = dbc.readRecord(selmax);
				if (dbmax != null) {
					if(dbmax.get("max")!=null){
						int max = ((Integer)dbmax.get("max")).intValue();
						max++;
						progressivo = max;
					}
				}
				//				debugMessage("progressivo="+progressivo);
				//2)inserisco nuovo record su agenda_interv
				//dbr = dbc.newRecord("agenda_interv");
				rec_ag.put("ag_data",formDate(data,"aaaa-mm-gg"));
				rec_ag.put("ag_progr", new Integer(progressivo));
				rec_ag.put("ag_cartella",cartella);
				//				gb 04/09/07		rec_ag.put("ag_contatto",contatto);
				//				gb 04/09/07 *******
				rec_ag.put("ag_contatto", strNProgetto);
				rec_ag.put("cod_obbiettivo", strCodObiettivo);
				rec_ag.put("n_intervento", strNIntervento);
				//				gb 04/09/07: fine *******
				rec_ag.put("ag_oper_ref",oper);
				//				gb 04/09/07		rec_ag.put("ag_tipo_oper", (selectTipoOpe(dbc,oper)).get("tipo"));
				rec_ag.put("ag_tipo_oper", tipo_oper_referente); //gb 04/09/07
				rec_ag.put("ag_orario",new Integer(orario));
				rec_ag.put("ag_oper_esec", "");
				rec_ag.put("ag_stato", "0");
				debugMessage("******carico su agenda_interv il record: \n"+(rec_ag.getHashtable()).toString());
				//04/07/2007 dbc.writeRecord(dbr);
				hRit.put("progr",new Integer(progressivo));
				hRit.put("carica",new Boolean(true));
				return hRit;
			}
			//			return false;
		}catch(Exception e){
			LOG.error("AgendaSettEJB.caricoAgendaInterv(): Eccezione= " + e);
			throw new Exception("Errore eseguendo una caricoAgendaInterv() - "+  e, e);
		}
	}


	private boolean caricoAgendaIntpre(ISASConnection dbc,Hashtable h,int progressivo,String data,boolean ctrlFreq)
	throws Exception
	{
		//		debugMessage("sono in carica agenda_intpre.."+h.toString());
		// 1) prelevo la prestazione inserito per quel giorno
		String prest=(String)h.get("pi_prest_cod");
		String oper=(String)h.get("as_op_referente");
		String freq="";
		//la quantita e frequenza hanno nonmi diversi
		//se siamo caso update o insert
		String quantita="";

		try{
			freq  = (String)h.get("pi_freq");
			quantita  = (String)h.get("pi_prest_qta");
			if(freq==null||freq.equals(""))freq=(String)h.get("ap_alert");
			if(quantita==null||quantita.equals(""))quantita=(String)h.get("quantita");
			//			gb 04/09/07                String sel="SELECT * FROM agenda_intpre WHERE "+
			String sel="SELECT * FROM agendant_intpre WHERE " + //gb 04/09/07
			"ap_data="+formatDate(dbc,data)+" AND "+
			"ap_progr="+progressivo+" AND "+
			"ap_oper_ref='"+oper+"' AND "+
			"ap_prest_cod='"+prest+"'";
			ISASRecord is = dbc.readRecord(sel);
			boolean proseguo=false;
			if(is==null){
				debugMessage("ctrlnew caricoAgendaIntpre ISCorrectTime  prestazione="+prest+",prog="+progressivo+" data="+data+" ctrlfreq="+ctrlFreq);


				if(ctrlFreq)proseguo= carica.ISCorrectTime(dbc,freq,h,data);//bargi 31/03/11
				else proseguo=true;

				if (proseguo){
					//					gb 04/09/07                        ISASRecord rec_ag = dbc.newRecord("agenda_intpre");
					ISASRecord rec_ag = dbc.newRecord("agendant_intpre"); //gb 04/09/07
					rec_ag.put("ap_data", formDate(data,"aaaa-mm-gg"));
					rec_ag.put("ap_progr", new Integer(progressivo));
					rec_ag.put("ap_prest_cod",prest);
					rec_ag.put("ap_oper_ref",oper);
					rec_ag.put("ap_stato",new Integer(0));
					rec_ag.put("ap_prest_qta",new Integer(quantita));
					rec_ag.put("ap_alert",new Integer(freq));
					//                debugMessage("**** carico su agenda_intpre il record: \n"+(rec_ag.getHashtable()).toString());
					dbc.writeRecord(rec_ag);
					LOG.debug("caricoAgendaIntpre " + rec_ag.getHashtable().toString());//elisa b 15/05/12
				}
			}else{
				LOG.debug("**** IL RECORD RISULTA CARICATO: \n"+(is.getHashtable()).toString());
			}
			return proseguo;
		}catch(Exception e){
			LOG.error("AgendaSettEJB.caricoAgendaIntpre(): Eccezione= " + e);
			throw new Exception("Errore eseguendo una caricoAgendaIntpre() - "+  e, e);
		}
	}
	private int giornoSett(String giornoSel){
		giornoSel=formDate(giornoSel,"gg-mm-aaaa");
		String gg=giornoSel.substring(0,2);
		if(gg.substring(0,1).equals("0"))
			gg=gg.substring(1);

		String mm=giornoSel.substring(3,5);
		if(mm.substring(0,1).equals("0"))
			mm=mm.substring(1);

		String aaaa=giornoSel.substring(6);

		GregorianCalendar gc =
			new GregorianCalendar(Integer.parseInt(aaaa),
					Integer.parseInt(mm)-1,
					Integer.parseInt(gg));
		int day=gc.get(Calendar.DAY_OF_WEEK);
		return day;
	}
	/*
	private String getData(GregorianCalendar gc){
		int dd_i=gc.get(Calendar.DAY_OF_MONTH);
		int mm_i=gc.get(Calendar.MONTH)+1;
		int aaaa=gc.get(Calendar.YEAR);
		String dd=""+dd_i;
		String mm=""+mm_i;
		if(dd_i<=9) dd="0"+dd_i;
		if(mm_i<=9) mm="0"+mm_i;
		//String data=aaaa+mm+dd;
		String data=dd+"/"+mm+"/"+aaaa;
		return data;
	}
	 */
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
	/**
	 *  fase aggiornamento agenda_interv e agenda_intpre
	 *  nel caso di modifica nel piano assist di
	 *  frequenza
	 *  nel caso in cui non si modifica la pianificazione
	 *TODO non si usa piu'          
	 */
	/*
	public Hashtable aggiornaAgenda(myLogin mylogin,Hashtable hin)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		Hashtable hRit=null;
		String cartella=null;
		//bargi19012012 String contatto=null;
		String data=null;
		String prest=null;
		String oper=null;
		String tipo=null;
		String prog=null;
		debugMessage("AgendaSettEJB aggiornaAgenda Hashtable: "+hin.toString());
		ISASConnection dbc=null;
		ISASCursor dbcur=null;
		String strNProgetto = (String)hin.get("n_progetto");
		String strCodObiettivo = (String)hin.get("cod_obbiettivo");
		String strNIntervento = (String)hin.get("n_intervento");
		try {
			cartella=(String)hin.get("n_cartella");
			//bargi19012012 contatto=(String)hin.get("n_contatto");
			prog=(String)hin.get("as_prog");
			//devo trasformare le date per DB
			data=formDate((String)hin.get("pa_data"),"aaaa-mm-gg");//formattaData((String)hin.get("skpa_data"));
			//hin.put("skpa_data",data);
			prest=(String)hin.get("pi_prest_cod");
			oper=(String)hin.get("as_op_referente");
			tipo=(String)hin.get("as_tipo_oper");

		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();			
			Vector vSettimane=new Vector();
			//mod 30/05/12 String sel="select ag.* from agendant_sett_tipo ag,piano_accessi pi "+
			String sel="select ag.*,pi.n_progetto from agendant_sett_tipo ag,piano_accessi pi "+
			"where as_op_referente='"+oper+"'"+
			" and ag.n_cartella="+cartella+
			" and ag.n_contatto="+strNProgetto+
			" and ag.as_data="+formatDate(dbc,data)+
			" and ag.as_prog="+prog+
			" and ag.as_tipo_oper='"+tipo+"'"+
			" and pi.pa_tipo_oper=ag.as_tipo_oper"+
			" and pi.n_cartella=ag.n_cartella"+
			" and pi.n_progetto=ag.n_contatto"+
			" and pi.cod_obbiettivo = ag.cod_obbiettivo"+
			" and pi.n_intervento = ag.n_intervento"+			
			" and pi.pa_data=ag.as_data"+
			" and pi.pi_prog=ag.as_prog"+
			" and pi.pi_prest_cod='"+prest.trim()+"'";
			dbcur=dbc.startCursor(sel);
			debugMessage("AgendaSettEJB aggiornaAgenda "+sel);
			Vector vdbr = dbcur.getAllRecord();
			Vector vOre=new Vector();
			if ((vdbr != null) && (vdbr.size() > 0))
				for(int i=0; i<vdbr.size(); i++) {
					ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
					if (dbrec != null){
						String ora="M";
						int gg=((Integer)dbrec.get("as_giorno_sett")).intValue();
						gg+=2;
						if(((Integer)dbrec.get("as_orario")).intValue()==0)
							ora="M";
						else ora="P";
						//devo fare su vettore per poter utilizzare ppoi lo stesso metodo...
						Vector v=new Vector();
						v.addElement(ora+gg);
						vOre.addElement(v);
					}
				}



			debugMessage("caricato vettore ore "+vOre.toString());
			if(!vOre.isEmpty()){
				vSettimane=carica.getSettimaneCaricate(dbc,hin);
				//Vector v1_rm=(Vector)hin.get("peragenda_rm");
				//debugMessage("vettore remove==>"+v1_rm.size());
				//if(!ISEmpty(v1_rm)){
				for(Enumeration en=vSettimane.elements(); en.hasMoreElements(); ){
					Hashtable hSettimana=(Hashtable)en.nextElement();
					debugMessage("REMOVE settimana "+hSettimana.toString());
					remove_appuntamenti(dbc,hin,vOre,hSettimana);
				}
				// }
				//
				// Vector v1=(Vector)hin.get("peragenda");
				// if(!ISEmpty(v1)){
			//	int k=0;
			//	boolean ctrlFreq=false;
			//	for(Enumeration enum2=vSettimane.elements(); enum2.hasMoreElements(); ){
			//		if(k==0)ctrlFreq=false;//bargi 01/04/2011
			//		else ctrlFreq=true;
			//		k++;
			//		Hashtable hSettimana=(Hashtable)enum2.nextElement();
			//		debugMessage("INSERT settimana"+hSettimana.toString());
			//		insert_appuntamenti(dbc,hin,vOre,hSettimana,ctrlFreq);
			//	}
				Hashtable h_par=new Hashtable();
	            h_par.put("data_inizio_ag", dataInput);
	            h_par.put("n_cartella",cartella);
	            h_par.put("data_inizio_caric", dataInput);
	            h_par.put("referente", oper);
	            h_par.put("tipo_operatore", tipo);
	        	LOG.debug("AgendaSett  carico agenda passando: "+h_par.toString());
	            //h_par.put("data_fine_ag", arg1)			           
	             String ms=carica.carica_agenda_apartireda(dbc, h_par,false);
	             if(!ms.equals(""))  msg_err+="\n"+ms;
				//}
			}
			if(dbcur!=null)dbcur.close();
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			String msg="Operazione completata con successo.";
			hRit=new Hashtable();
			hRit.put("msg",msg);
			hRit.put("prog",""+prog);
			return hRit;
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
		}catch(Exception e1){
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e1);
			}
			throw new SQLException("Errore eseguendo una insert() - "+  e1);
		}finally{
			if(!done){
				try{
					if(dbcur!=null)dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}*/
	private boolean ISEmpty(Vector v){
		Enumeration en=v.elements();
		int elem=0;
		while(en.hasMoreElements()){
			Vector vt=(Vector)en.nextElement();
			elem+=vt.size();
		}
		if(elem==0)return true;
		else return false;   
	}
}

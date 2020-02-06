package it.pisa.caribel.sinssnt.controlli;
//==========================================================================

//CARIBEL S.r.l.

//********NUOVA RELEASE GIUGNO 2012 bargi versione 12_03_01

//--------------------------------------------------------------------------
//bargi 12/07/2012 alla ripianificazone agenda si passa anche la prestazione in modo che venga ricaricata solo quella modificata!
//12/01/2012 bargi modificato controllo isCorrectTime

//30/11/2011 Barbara Giannattasio aggiunto filtro per ag_oraRIO per permettere inserimento accesso mattina e pomeriggio

//25/09/2008 Barbara Giannattasio

//estrapolato da EJB il caricamento in agenda delle settimane caricate.

//sulla hashtable se � presente la chiave "controllo_caricata" procedo al caricamento della settimana solo se

//non esiste ancora alcuna oprestazione gi� caricata nella settimana analizzata altrimenti carica quello che considera buono.

//==========================================================================



import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMetaInfo;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.NumberDateFormat;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.dateutility;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CaricaAgendaPrestazioni extends SINSSNTConnectionEJB
{
	boolean ctrlFreq=true;
	myLogin mylogin_global=null;
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	dateutility dt=new dateutility();
	String messaggio_carica="";
	String messaggio_scarica="";
	String nomeEJB="CaricaAgendaPrestazioni_1301_1";
	private final Log LOG = LogFactory.getLog(getClass());//elisa b 16/05/12

	public CaricaAgendaPrestazioni() {}


	/**
	 * 
	 * @param dbc
	 * @param hPar parametri x lettura: data_chiusura (obbligatorio sarebbe nuova data apertura la data chiusura calcolata come giorno prima)
	 *                                                                     operatore_new (se non presente l'operatore resta lo stesso)
	 * @throws Exception
	 */
	public ISASRecord duplicaPiano(ISASConnection dbc,Hashtable h)
	throws CariException, Exception {
		String metodo="duplica_piano";
		boolean done=false;		
		ISASCursor dbcur=null;
		String strNCartella =null;
		String strNProgetto =null;
		String strCodObiettivo =null;
		String strNIntervento =null;
		String strPaTipoOper =null;
		String strPaData =null;
		ISASRecord dbrit=null;
		ISASRecord dbrPA=null;
		// 04/05/11: causa problemi dovuti a campi ISAS voluti da RME
		String msg = "";
	//	String msg_0 = "Non si hanno sufficienti permessi per aggiornare la data chiusura";
		String oper_new="";
		LOG.info(nomeEJB+"."+metodo+" parametri "+h.toString());
		if(h.get("operatore_new")!=null && !h.get("operatore_new").toString().equals(""))
			oper_new=h.get("operatore_new").toString();
		try {
			strNCartella = ISASUtil.getValoreStringa(h,"n_cartella");
			strNProgetto = ISASUtil.getValoreStringa(h,"n_progetto");
			strCodObiettivo = ISASUtil.getValoreStringa(h,"cod_obbiettivo");
			strNIntervento = ISASUtil.getValoreStringa(h,"n_intervento");
			strPaTipoOper = ISASUtil.getValoreStringa(h,"pa_tipo_oper");
			strPaData = ISASUtil.getValoreStringa(h,"data_apertura");
		}catch (Exception e){
			throw new SQLException("Errore: manca la chiave primaria", e);
		}
		try{
			Hashtable hDt=new Hashtable();
			Vector vOp=new Vector();
			ServerUtility su=new ServerUtility();
			String forzaProlungamento = "";
			String forzaDataFinePiano = ISASUtil.getValoreStringa(h, "forzaDataFinePiano");
			String dataInizio = ISASUtil.getValoreStringa(h,"data_chiusura");
			String dataCh=dt.getDataNGiorni(ISASUtil.getValoreStringa(h,"data_chiusura"),-1);//bargi 05/06/2012 data chiusura giorno prima rispetto apertura
			if(h.containsKey("forzaDataFinePiano")){
				if(h.containsKey("dt_sospensione_fine")){
					forzaProlungamento = " OR p.pi_data_fine = " + formatDate(dbc, forzaDataFinePiano);
				}else{
					forzaProlungamento = " OR p.pi_data_fine = " + formatDate(dbc, dataCh);
				}
			}
			
			ISASRecord dbr_lett = null;
			//se sono in una sospensione devo ricaricareo la data di chiusura con quella impostata nel PAI quindi recupero la scheda SO
			if(h.containsKey("tipoVariazione") && h.get("tipoVariazione").equals("sospensione")){
				//recupero la scheda della segreteria organizzativa per l'assisitito.
				String sql = "SELECT p.* FROM rm_skso p WHERE p.n_cartella = " + strNCartella + " AND p.pr_data_chiusura IS NULL " +
					     " AND ispianocongelato='S' ";
				dbr_lett = dbc.readRecord(sql);
			}
			ISASCursor dbgriglia;
			
			//RECUPERO LE PRESTAZIONI CHE VOGLIO DUPLICARE
			String sel = "SELECT a.*, p.pi_prog" +
			" FROM piano_assist a, piano_accessi p" +
			" WHERE a.n_cartella = " + strNCartella +
			" AND a.n_progetto = " + strNProgetto +
			" AND a.cod_obbiettivo = '" + strCodObiettivo + "'" +
			" AND a.n_intervento = " + strNIntervento + 
			" AND a.pa_tipo_oper = '" + strPaTipoOper + "'" + 
			" AND a.pa_data = " + formatDate(dbc, strPaData)+
			" AND (p.pi_data_fine > " + formatDate(dbc, dataCh)+//bargi 01/06/2012 riporto solo quelli aperti
			forzaProlungamento + 
			" or p.pi_data_fine is null)"+
			" AND a.n_cartella = p.n_cartella" +
			" AND a.n_progetto = p.n_progetto"+
			" AND a.cod_obbiettivo = p.cod_obbiettivo"+
			" AND a.n_intervento = p.n_intervento"+
			" AND a.pa_tipo_oper = p.pa_tipo_oper"+
			" AND a.pa_data = p.pa_data";
			LOG.info("PianoAssistEJB/duplica_piano 1: " + sel);
			dbcur = dbc.startCursor(sel);
			String nuovaChiusura = dt.getData(dataCh,"yyyy-mm-dd");
			java.sql.Date date_ch=java.sql.Date.valueOf(formDate(dataCh,"aaaa-mm-gg"));
			if(dbcur!=null && dbcur.getDimension()>0){ //VFR se non ho prestazioni da riportare nel nuovo piano non lo apro nemmeno
				while(dbcur.next()){
					ISASRecord dbr = dbcur.getRecord();
					String mysel = "SELECT *" +
					" FROM piano_accessi" +
					" WHERE n_cartella = " + strNCartella +
					" AND n_progetto = " + strNProgetto +
					" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
					" AND n_intervento = " + strNIntervento +
					" AND pa_data = " + formatDate(dbc,  dbr.get("pa_data").toString())+
					" AND pa_tipo_oper = '" + strPaTipoOper + "'" +
					" AND pi_prog = " + "" + dbr.get("pi_prog");
					LOG.info("PianoAssistEJB/duplica_piano 2: " + mysel);
					ISASRecord dbr2 = dbc.readRecord(mysel);	    
					Hashtable hr2=(Hashtable)dbr2.getHashtable().clone();
					if(dbr2.get("pi_data_fine")==null || java.sql.Date.valueOf(dbr2.get("pi_data_fine").toString()).after(date_ch)){ 
						hr2.put("pi_data_fine",dataCh);
					}
					LOG.debug("PianoAssist  rimuovo agenda : "+hr2.toString());
					/************************28/06/2012 */
					String dataF="";
					if(!h.containsKey("data_fine_ag")) {
						if(oper_new.equals("")) {
								Hashtable h_par=new Hashtable();
								h_par.put("data_inizio_ag", dataInizio);
								h_par.put("referente", hr2.get("pi_op_esecutore").toString());
								h_par.put("tipo_operatore", strPaTipoOper);
								h_par.put("n_cartella",strNCartella );
								dataF=getDataFineAg(dbc,h_par,dataInizio);
								hDt.put(hr2.get("pi_op_esecutore").toString(), dataF);
						}
					}
					
					/*******************************/
					rimuovoAgendaCaricata(dbc,hr2,nomeEJB+"/"+metodo);
					
					//bargi 01/06/2012 duplico anche la settimana tipo!
					duplicoSettTipo(dbc,hr2,h);   
					//Scrivo il nuovo record su pianointerv
					dbrit = dbc.newRecord("piano_accessi");
					Hashtable h_rit = dbr2.getHashtable();
	
					Enumeration n=h_rit.keys();
					while(n.hasMoreElements()){
						String e=(String)n.nextElement();
						dbrit.put(e, h_rit.get(e));
					}
					String esecutore_old=h_rit.get("pi_op_esecutore").toString();
	
					dbrit.put("pa_data", ndf.formDate(dataInizio, "aaaa-mm-gg"));//"" + java.sql.Date.valueOf(dataInizio));
					//Anche l'inizio della prestazione deve essere alla data specificata
					dbrit.put("pi_data_inizio",ndf.formDate(dataInizio, "aaaa-mm-gg"));//"" + java.sql.Date.valueOf(dataInizio));
					if(oper_new.equals(""))
						oper_new=esecutore_old;
	
					//a questo punto controllo che data fine è impostata, se ho chiesto la forzatura agisco sulla data fine
					if(h.containsKey("forzaDataFinePiano")){
						if(dbr2.get("pi_data_fine")!=null && dt.getData(dbr2.get("pi_data_fine"),"yyyy-mm-dd").equals(nuovaChiusura)){
							if(forzaDataFinePiano.isEmpty()){
								dbrit.getDBRecord().remove("pi_data_fine");
							}else{
								dbrit.put("pi_data_fine",forzaDataFinePiano);
							}
						}
					}
					
					//se sono in una sospensione ricarico la data di chiusura con quella impostata nel PAI
					if(h.containsKey("tipoVariazione") && h.get("tipoVariazione").equals("sospensione")){
						//recupero la scheda della segreteria organizzativa per l'assisitito.
						String sql = "";
						if (dbr_lett != null) {
							String id_skso = dbr_lett.get("id_skso").toString();
							sql = "SELECT * FROM PAI p, prestaz pr WHERE p.n_cartella = " + strNCartella + " AND p.id_skso = " + id_skso + 
								  " AND p.prest_cod = pr.prest_cod AND pr.prest_cod = '" + ISASUtil.getValoreStringa(dbrit,"pi_prest_cod") + "' AND pr.prest_tipo = "+ strPaTipoOper + 
								  " order by p.pai_data_fine desc"; //ordino per data fine per essere sicuro di prendere quello che finisce dopo.
							dbgriglia = dbc.startCursor(sql);
							Vector<ISASRecord> vdbg=dbgriglia.getSomeRecord(1);
							if(vdbg!= null && vdbg.size()>0){
								dbrit.put("pi_data_fine",((ISASRecord)vdbg.elementAt(0)).get("pai_data_fine"));
							}
						}
					}
						
					dbrit.put("pi_op_esecutore",oper_new);
					
					if(!vOp.contains(oper_new))
					vOp.add(oper_new);
					//NO!!! bargi 01/06/2012 riporto solo quelli aperti o chiusi con data successiva
					//   if(dbrit.get("pi_data_fine")!=null)
					//		dbrit.put("pi_data_fine","");				
					dbc.writeRecord(dbrit);
					//Aggiorno la data fine a quello vecchio
					if(dbr2.get("pi_data_fine")==null || java.sql.Date.valueOf(dbr2.get("pi_data_fine").toString()).after(date_ch)) {
						dbr2.put("pi_data_fine", ndf.formDate(dataCh, "aaaa-mm-gg"));
							dbc.writeRecord(dbr2);					
					}			
				}
				Enumeration en=vOp.elements();
				String msg_err="";
				while(en.hasMoreElements()) {
					String oper_esec=(String)en.nextElement();
					Hashtable h_par=new Hashtable();
					h_par.put("data_inizio_ag", dataInizio);
					if(hDt.containsKey(oper_esec)) {
					    h_par.put("data_fine_ag", hDt.get(oper_esec));
					    
					}
					h_par.put("referente", oper_esec);
					h_par.put("tipo_operatore", strPaTipoOper);
					h_par.put("n_cartella",strNCartella );
					LOG.debug("PianoAssist  carico agenda passando: "+h_par.toString());
					 msg_err=carica_agenda_apartireda(dbc, h_par,false,false);
					if(msg_err!=null) {
						msg+=msg_err+"\n";
					}
				}
				//Ora vado riscrivere ed aggiornare il record su piano_assist
				sel = "SELECT *" +
				" FROM piano_assist" +
				" WHERE n_cartella = " + strNCartella +
				" AND n_progetto = " + strNProgetto +
				" AND cod_obbiettivo = '" + strCodObiettivo + "'" +
				" AND n_intervento = " + strNIntervento +
				" AND pa_data = " + formatDate(dbc, strPaData) +
				" AND pa_tipo_oper = '" + strPaTipoOper + "'";
				LOG.info(nomeEJB+".duplica_piano - Select solo su piano_assist: " + sel);
				dbrPA = dbc.readRecord(sel);
				dbrit = dbc.newRecord("piano_assist");
				Hashtable hA=dbrPA.getHashtable();
				Enumeration nA=hA.keys();
				while(nA.hasMoreElements()){
					String e=(String)nA.nextElement();
					dbrit.put(e,hA.get(e));
				}
				
				if(h.get("operatore_new")!=null && !h.get("operatore_new").toString().equals(""))
								dbrit.put("pa_operatore",oper_new);//bargi 26/09/2012 su richiesta si cambia anche operatore responsabile pianificazione
				dbrit.put("pa_data", "" + java.sql.Date.valueOf(dataInizio));
				//gb 08/08/07: Se il piano assistenziale era chiuso, si imposta il nuovo piano a aperto.
				if(dbrit.get("pa_data_chiusura")!=null && !h.containsKey("preservaChiusura"))
					dbrit.put("pa_data_chiusura","");
				//gb 08/08/07: fine *******
				dbc.writeRecord(dbrit);
			}
			
			//Aggiorno la data fine a quello vecchio
			if(dbrPA.get("pa_data_chiusura")==null || java.sql.Date.valueOf(dbrPA.get("pa_data_chiusura").toString()).after(date_ch)) {
				dbrPA.put("pa_data_chiusura", ndf.formDate(dataCh, "aaaa-mm-gg"));//"" + java.sql.Date.valueOf(dataCh));
					dbc.writeRecord(dbrPA);			
			}	      
			// 04/05/11
			
			if (!"".equals(msg.trim())) 
				throw new CariException(msg);				
			
			return dbrit;
		} finally{			
			close_dbcur_nothrow(nomeEJB+".duplicaPiano", dbcur);			
		} 
	}
	/**
	 * 
	 * @param dbc
	 * @param hPiano hashtable con dati letti ad piano_accessi
	 * @param hPar parametri x lettura: data_chiusura (obbligatorio sarebbe nuova data apertura la data chiusura calcolata come giorno prima)
	 *                                                                     operatore_new (se non presente l'operatore resta lo stesso)
	 * @throws Exception
	 */
	public void duplicoSettTipo(ISASConnection dbc,Hashtable hPiano,Hashtable hPar)throws Exception {
		ISASCursor dbcurAg=null;
		String dataInizio = (String)hPar.get("data_chiusura");
		//String dataCh=dt.getDataNGiorni((String)hPar.get("data_chiusura"),-1);
		String oper_new=hPiano.get("pi_op_esecutore").toString();
		try {
			if(hPar.get("operatore_new")!=null && !hPar.get("operatore_new").toString().equals(""))
				oper_new=hPar.get("operatore_new").toString();
			String myselAg = "SELECT *" +
			" FROM agendant_sett_tipo" +
			" WHERE n_cartella = " + hPiano.get("n_cartella") +		
			" and as_op_referente='"+((String)hPiano.get("pi_op_esecutore")).trim()+"'"+
			" and as_tipo_oper= '"+(String)hPiano.get("pa_tipo_oper")+"'"+
			" and n_contatto="+hPiano.get("n_progetto")+			
			" and cod_obbiettivo = '" + hPiano.get("cod_obbiettivo") + "' "+
			" and n_intervento = " + hPiano.get("n_intervento")+
			//" and as_orario = " + hPiano.get("as_orario")+
			" and as_data =" +formatDate(dbc,hPiano.get("pa_data").toString())+
			"and as_prog ="+hPiano.get("pi_prog");		 
			LOG.info(nomeEJB+".duplicoSettTipo�: " + myselAg);
			dbcurAg=dbc.startCursor(myselAg);	
			if(dbcurAg!=null) {
				while(dbcurAg.next()){
					ISASRecord dbrAg = dbcurAg.getRecord();
					ISASRecord dbritAgN = dbc.newRecord("agendant_sett_tipo");
					Hashtable h_ag = dbrAg.getHashtable();			
					Enumeration n=h_ag.keys();
					while(n.hasMoreElements()){
						String e=(String)n.nextElement();
						dbritAgN.put(e, h_ag.get(e));
					}
					dbritAgN.put("as_data", "" + java.sql.Date.valueOf(dataInizio));
					dbritAgN.put("as_op_referente", oper_new);
					LOG.debug(nomeEJB+".carico agenda_sett_tipo nuova : "+dbritAgN.getHashtable().toString());
					dbc.writeRecord(dbritAgN);
				}
			}
		}finally {
			close_dbcur_nothrow(nomeEJB+".duplicoSettTipo", dbcurAg);
		}
	}
	/** 
	 *  @param myLogin mylogin    : login
	 * @param Hashtable h    :  hashtable deve contenere il vettore di vettori key="giorni" (con tutte le settimane) 
	 *                                                    il "referente" e il "tipo_operatore"
	 *   ed eventualmente la cartella n_cartella
	 *   Esegue le seguenti operazioni:
	 *  
	 *   1) seleziona da agenda_sett_tipo le prestazioni pianificate in join con piano_accessi
	 *   nella settimana inerenti all'operatore referente
	 *   2)carica agenda ma controlla la data fine e se inferiore alla data  da caricare  non viene caricato
	 *
	 */

	public String carica_agenda(myLogin mylogin, Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
		String msg="";
		try{
			LOG.debug(nomeEJB+"/carica_agenda: param="+h.toString());
			dbc = super.logIn(mylogin);
			dbc.startTransaction();
			Vector v=(Vector)h.get("giorni");
			Enumeration sett=v.elements();
			int i=0;
			while(sett.hasMoreElements())
			{
				Vector settimana=new Vector();
				messaggio_carica="";
				settimana =(Vector)sett.nextElement();
				msg+=carica_agenda(dbc, h, settimana,true);
				if(i==0)ctrlFreq=true;//solo prima settimana eventualmente non si testa frequenza
				i++;
			}
			LOG.debug(nomeEJB+"/FINE carica_agenda");
			dbc.commitTransaction();
			done = true;
			if (msg.equals(""))msg=null;//per controllo lato client
			return msg;
		}catch(DBRecordChangedException e){
			throw e;
		} catch(ISASPermissionDeniedException e){
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw newEjbException(nomeEJB+"carica_agenda: " + e.getMessage(), e);
		} finally{
			if (!done){
				rollback_nothrow(nomeEJB+".carica_agenda", dbc);
				LOG.error(nomeEJB+".carica_agenda ROLLBACK ");
			}
			//       close_dbcur_nothrow(nomeEJB+".duplicaPiano", dbcur);
			logout_nothrow(nomeEJB+"carica_agenda", dbc);
		} 
	}


	/** 
	 *  @param ISASConnection dbc
	 * @param Hashtable h    :  hashtable deve contenere la data inizio 
	 * "data_inizio_ag" per caricare appuntamenti in agenda e 
	 *                                                    volendo la data fine 
	 * "data_fine_ag" (se non presente viene letto l'ultimo giorno caricato in agenda)
	 * il "referente" e il 
	 * n_cartella che puo'non essere valorizzato
	 * "tipo_operatore"
	 *     eventualmente la data a partire dalla quale si carica in agenda 
	 * "data_inizio_caric"
	 * pi_prest_cod potrebbe essre valorizzata in caso di modifica pianificazione
	 *   Esegue le seguenti operazioni:
	 *  @param ctrFrequenza si effettua o meno il controllo della frequenza x il primo caricamento
	 *  @param scarica rimuove da agenda secondo i parametri paSSATI
	 *   1) seleziona da agenda_sett_tipo le prestazioni pianificate in join con piano_accessi
	 *   nella settimana inerenti all'operatore referente
	 *   2)carica agenda ma controlla la data fine e se inferiore alla data  da caricare  non viene caricato
	 *
	 */
	public String carica_agenda_apartireda(ISASConnection dbc, Hashtable h,boolean ctrFrequenza,boolean scarica)
	throws Exception
	{
		String msg="";
		this.ctrlFreq=ctrFrequenza;
		LOG.info(nomeEJB+".carica_agenda_apartireda: hash="+h.toString()+ " ctrFreq? ="+ ctrFrequenza);
		String dataI="";
		String dataF="";

		if(h.containsKey("data_inizio_ag")) {
			dataI=h.get("data_inizio_ag").toString();
		}else {
			return "data inizio per caricamento agenda non presente";
		}
		if(h.containsKey("data_fine_ag")) {
			dataF=h.get("data_fine_ag").toString();
		}else {
			dataF=getDataFineAg(dbc,h,dataI);
		}
		if(dataF.equals("")) {
			LOG.debug(nomeEJB+".carica_agenda_apartireda return data fine per caricamento agenda non reperito");
			return "caricamento agenda non effettuato non risulta caricata l'agenda";
		}
		Vector v=caricaSettimane(dataI, dataF);
		LOG.debug(nomeEJB+".carica_agenda_apartireda vettoreSettimane="+v.size());
		Enumeration sett=v.elements();
		int settim=0;
		while(sett.hasMoreElements())
		{
			settim++;
			Vector settimana=new Vector();
			messaggio_carica="";
			settimana =(Vector)sett.nextElement();
			msg+=carica_agenda(dbc, h, settimana,scarica);
			if(settim==1)ctrlFreq=true;//solo prima settimana eventualmente non si testa frequenza			
		}
		if (msg.equals(""))msg=null;//per controllo lato client
		return msg;
	}
/**
 * 
 * @param dataI
 * @param dataF
 * @return vettore di vettori con caricari tutti i giorni delle settimane calcolate in base a data inizio e fine passate
 * @throws Exception
 */
	public Vector caricaSettimane(String dataI,String dataF)throws Exception {
		dateutility dt =new dateutility();
		String data_successiva=getFirstDayWeek(dataI,6);
		dataF=getLastDayWeek(dataF,6);
		int giorno=1;
		Vector v_giorni=new Vector();
		Vector v_settimane=new Vector();
		int i=0;
		LOG.debug("caricaSettimane  "+data_successiva+" **** "+dataF );
		while(dt.confrontaDate(data_successiva,dataF)!=1) {
			if (giorno==8){
				i++;
				v_settimane.addElement((Vector)v_giorni.clone());
				v_giorni=new Vector();	          
				giorno=1;
			}
			v_giorni.addElement(ndf.formDate(data_successiva, "gg-mm-aaaa"));
			//   LOG.debug("carica vettore "+i+" giorno="+data_successiva);
			giorno++;	        
			data_successiva=dt.getDataNGiorni(data_successiva,1);
		}
		v_settimane.addElement(v_giorni);
		return v_settimane;

	}

	public	Vector getSettimanaDa(String giornoSel){
		GregorianCalendar gc =dt.getGreg(giornoSel);			
		Vector v =new Vector();
		v.addElement(dt.getData(gc,"gg/mm/aaaa"));
		for(int i=0;i<6;i++)
		{
			gc.add(gc.DATE,1);
			v.addElement(dt.getData(gc,"gg/mm/aaaa"));
		}
		LOG.debug("giornisettimana="+v.toString());
		return v;
	}
	/** 
	 *  @param ISASConnection dbc
	 * @param Hashtable h    :  hashtable deve contenere
	 *                                                    il "referente" e il "tipo_operatore"
	 *                                                    eventualmente la data a partire dalla quale si carica in agenda "data_inizio_caric"
	 *                                                    e la cartella
	 *  @param Vector settimana i giorni della settimana che si carica
	 *   Esegue le seguenti operazioni:
	 *  
	 *   1) seleziona da agenda_sett_tipo le prestazioni pianificate in join con piano_accessi
	 *   nella settimana inerenti all'operatore referente
	 *   2)carica agenda ma controlla la data fine e se inferiore alla data  da caricare  non viene caricato
	 *
	 */

	public  String carica_agenda(ISASConnection dbc,Hashtable h,Vector settimana,boolean scarica)	
	throws Exception
	{
		//LOG.debug(nomeEJB+".carica_agenda size vettore "+settimana.size());
		String msg="";
		String primo_gg=(String)settimana.elementAt(0);
		String ultimo_gg=(String)settimana.elementAt(6);
		if(scarica)
			scaricaAgenda(dbc,h,settimana);
		
		
		boolean b=caricaDaAgendaSettTipo(dbc,h,settimana,primo_gg,ultimo_gg);
		if(b==true)
			msg="\n Settimana ["+primo_gg+" - "+ultimo_gg+"]: caricata con successo"+messaggio_carica;
		else
			msg="\n Settimana  ["+primo_gg+" - "+ultimo_gg+"]: non caricata non esistono prestazioni valide";
		LOG.debug("Ritorno msg= "+msg);			
		return msg;
	}
	private String getDataFineAg(ISASConnection dbc,Hashtable h,String dataI)throws Exception{
		String mysel ="SELECT max(ag_data) data_fine " +
		" from agendant_interv " + 
		" where ag_data>="+formatDate(dbc,dataI)+
		" and ag_oper_ref='"+((String)h.get("referente")).trim()+"'";
		//	if(h.get("n_cartella")!=null)
		//		mysel+=" and ag_cartella="+h.get("n_cartella");
		LOG.info(nomeEJB+".getDataFineAg mysel: " + mysel);
		ISASRecord dbr=dbc.readRecord(mysel);
		String dataF="";
		if(dbr!=null){
			dataF=util.getDateField(dbr,"data_fine");
		}

		//nelle Marche non carico più le agende per gli operatori direttamente ma lo faccio settimana per settimana 
		//se l'operatore non ha nulla caricato gli carico l'agenda fino a 4 settimane dopo oggi
		if(dataF.isEmpty() && ManagerProfile.isConfigurazioneMarche(CaribelSessionManager.getInstance())){
			Calendar c = Calendar.getInstance(); 
			c.setTime(new Date()); 
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			c.add(Calendar.WEEK_OF_MONTH, 4);
			dataF = UtilForUI.getDateForUI(c.getTime());	
		}
		
		return dataF;
	}
	private boolean caricaDaAgendaSettTipo(ISASConnection dbc,Hashtable h,Vector v,String primo_gg,String ultimo_gg)
	throws Exception
	{LOG.debug(nomeEJB+".caricaDaAgendaSettTipo  giorni ["+primo_gg+"****"+ultimo_gg+"]");
		boolean done=false;
		ISASCursor dbcur=null;
		try {
			String mysel ="SELECT ag.*,sp.* "+
			" from agendant_sett_tipo ag, piano_accessi sp" + 
			" where as_op_referente='"+((String)h.get("referente")).trim()+"'"+
			" and pi_data_inizio<="+formatDate(dbc,ultimo_gg)+
			" and (pi_data_fine>="+formatDate(dbc,primo_gg)+
			" or pi_data_fine is null )"+
			" and ag.n_cartella=sp.n_cartella"+
			" and ag.n_contatto = sp.n_progetto" +
			" and ag.cod_obbiettivo = sp.cod_obbiettivo" +
			" and ag.n_intervento = sp.n_intervento" +
			" and as_data = pa_data" + 
			" and ag.as_tipo_oper = sp.pa_tipo_oper" + 
			" and sp.pi_op_esecutore='"+((String)h.get("referente")).trim()+"'"+//aggiunto bargi 04/06/2012
			" and ag.as_tipo_oper='"+(String)h.get("tipo_operatore")+"'"+
			" and as_prog=pi_prog";
			if(h.get("n_cartella")!=null)
				mysel+=" and ag.n_cartella="+h.get("n_cartella");
			 
			if(h.get("pi_prest_cod")!=null)
				mysel+=" and sp.pi_prest_cod='"+h.get("pi_prest_cod").toString().trim()+"'";	 	
			mysel+=" order by ag.n_cartella, ag.n_contatto, ag.cod_obbiettivo, ag.n_intervento, as_giorno_sett, as_orario"; 

			LOG.info(nomeEJB+"caricaDaAgendaSettTipo x caricamento : "+mysel);
			dbcur=dbc.startCursor(mysel);
		
		//	Vector vdbr = dbcur.getAllRecord();
			int progr=-1;
			//   Hashtable hMsg=new Hashtable();
			boolean caricata=false;
			// devo scorrere il vettore per decodificare l'operatore
			//if ((vdbr != null) && (vdbr.size() > 0))
				//for(int i=0; i<vdbr.size(); i++)
			//	{
	boolean esiste=false;
	int count=0;
			while (dbcur.next())
			{
				ISASRecord dbrec=dbcur.getRecord();							
					//ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);	                        
					if (dbrec != null)
					{		
						count++;
						LOG.debug("Scorro rec= "+count);				
						String data=(String)v.elementAt(((Integer)dbrec.get("as_giorno_sett")).intValue());
						String w_data_caricoag=formDate(data,"aaaammgg");
						DataWI dt_carico_ag=new DataWI( w_data_caricoag,1);
						String data_inizio=formDate(dbrec.get("pi_data_inizio"),"aaaammgg");
						String data_fine=formDate(dbrec.get("pi_data_fine"),"aaaammgg");		
						String data_inizio_caric=data_inizio;
						if(h.get("data_inizio_caric")!=null)data_inizio_caric=formDate(h.get("data_inizio_caric"),"aaaammgg");
						String w_data_inizio_caric=formDate(data_inizio_caric,"aaaammgg");
						DataWI dt_data_inizio_caric=new DataWI( w_data_inizio_caric,1);
						if (dt_data_inizio_caric.isSuccessiva(w_data_caricoag))
						{
							LOG.debug("data_input>data_insert?"+data_inizio_caric+">"+w_data_caricoag);
							LOG.debug("Non proseguo data input � succ a data che voglio inserire");
						}else{
							if (!(dt_carico_ag.isPrecedente(data_inizio))&&
									((data_fine.equals("00000000") ||!(dt_carico_ag.isSuccessiva(data_fine)))))
							{								
								progr = caricoAgendaInterv(dbc, dbrec, data, h,primo_gg,ultimo_gg);		
								if(progr!=-1) {
									caricata=caricoAgendaIntpre(dbc, dbrec,h, progr, data);
									if(caricata)esiste=true;
								}

							}
						}
					}
				}
			LOG.debug("TERMINATO tot rec= "+count);			
			puliziaAgenda(dbc,primo_gg,ultimo_gg);
			LOG.debug("TORNO da pulizia ho caricato? "+esiste);			
			done = true;			
			if (esiste)return true;
			else return false;
		}finally{
			close_dbcur_nothrow(nomeEJB+".duplicoSettTipo", dbcur);
		}
	}
	private void puliziaAgenda(ISASConnection dbc,String primo_gg,String ultimo_gg) throws Exception
	{
		ISASCursor dbcur=null;
		try{
			String mysel="select count(*) tot from agendant_interv  " +
			"where not exists" +
			"(select 1 from agendant_intpre " +
			" where ag_data=ap_data " +
			"and ag_progr= ap_progr " +
			"and trim(ag_oper_ref)= trim(ap_oper_ref)" +
			" and ap_data>="+formatDate(dbc,primo_gg)+
			" and ap_data<="+formatDate(dbc,ultimo_gg)+")" +
			" and ag_data>="+formatDate(dbc,primo_gg)+
			" and ag_data<="+formatDate(dbc,ultimo_gg);			
			 LOG.debug(nomeEJB+"puliziaAgenda ctrl se esistono rec " + mysel);	
			ISASRecord dbrTest=dbc.readRecord(mysel);
			int tot_row=0;
			if(dbrTest!=null) tot_row=util.getIntField(dbrTest,"tot");
			if(tot_row==0)
			{
				 LOG.debug(nomeEJB+"puliziaAgenda non necessaria");
				return;
			}	 
			ISASMetaInfo imt_ag = new ISASMetaInfo(dbc, "agendant_interv");
			boolean tabAgIsasi = false;
			try {
				tabAgIsasi = imt_ag.containsISASFields();
			} catch (Exception e) {
				LOG.error(nomeEJB+".puliziaAgenda errore nel recuperare i campi isass ");
				e.printStackTrace();
			}
		
			  mysel="select * " +
			  		" from agendant_interv  " +
				"where not exists" +
				"(select * from agendant_intpre " +
				" where ag_data=ap_data " +
				"and ag_progr= ap_progr " +
				"and trim(ag_oper_ref)= trim(ap_oper_ref)" +
				" and ap_data>="+formatDate(dbc,primo_gg)+
				" and ap_data<="+formatDate(dbc,ultimo_gg)+")" +
				" and ag_data>="+formatDate(dbc,primo_gg)+
				" and ag_data<="+formatDate(dbc,ultimo_gg);			
				 LOG.debug(nomeEJB+"puliziaAgenda  " + mysel);	
				dbcur=dbc.startCursor(mysel);		 
				 LOG.debug(nomeEJB+"puliziaAgenda cursore aperto ");		 	
					while (dbcur.next())
					{
						ISASRecord dbrec=dbcur.getRecord();				
						if (dbrec != null)
						{	
							 LOG.debug(nomeEJB+"puliziaAgenda rimuovo rec");
							String selag="SELECT * FROM agendant_interv WHERE "+ //gb 29/08/07
							"ag_data="+formatDate(dbc,dbrec.get("ag_data").toString())+" AND "+
							"ag_progr="+dbrec.get("ag_progr")+" AND "+
							"ag_oper_ref='"+(String)dbrec.get("ag_oper_ref")+"'";
						try {
								ISASRecord dbag=dbc.readRecord(selag);
								if(dbag!=null)	dbc.deleteRecord(dbag);
							}catch(ISASPermissionDeniedException e) { LOG.debug(nomeEJB+"puliziaAgenda NO PERMESSO cancellazione " + selag);}
							
							//     LOG.debug(nomeEJB+"puliziaAgenda cancellazione " + selag);
						}
				}
					 LOG.debug(nomeEJB+"FINE puliziaAgenda  ");
		}finally{
			close_dbcur_nothrow(nomeEJB+".puliziaAgenda", dbcur);
		}
	}
	/**
	 * 
	 * @param dbc
	 * @param hin
	 * @return vettore con settimane caricate a partire da data input calcolando il primo giorno
	 * @throws DBRecordChangedException
	 * @throws SQLException
	 */
	public Vector getSettimaneCaricate(ISASConnection dbc,Hashtable hin)
	throws Exception {
		String dataInput=null;
		String oper=null;
		String data_fine=null;
		Hashtable ht=new Hashtable();
		LOG.debug("settimaneCaricate Hashtable: "+hin.toString());
		ISASCursor dbcur=null;
		boolean primo=true;
		try {
			//da data input ricavo il lunedi dela settimana
			dataInput=formDate((String)hin.get("data_input"),"aaaa-mm-gg");//formattaData((String)hin.get("data_input"));
			LOG.debug("VsettimaneCaricate/dataInput che arriva "+dataInput);

			ht=getEstremiWeek(dataInput,6);
			dataInput=(String)ht.get("data_inizio");
			data_fine=(String)ht.get("data_fine");
			oper=(String)hin.get("as_op_referente");
		}catch (Exception e){
			LOG.error("ECCEZIONE settimaneCaricate="+e);
			throw new SQLException("Errore: manca la chiave primaria", e);
		}
		try{
			String mysel="select distinct(ag_data) from agendant_interv, agendant_intpre "+
			" where ag_data>="+formatDate(dbc,dataInput)+" and "+
			" ag_oper_ref='"+oper+"' and "+
			" ag_data=ap_data and "+
			" ag_progr=ap_progr and "+
			" ag_oper_ref=ap_oper_ref "+
			" order by ag_data";
			LOG.info("select settimane caricate=="+mysel);
			dbcur=dbc.startCursor(mysel);
	//		Vector vdbr = dbcur.getAllRecord();
			Vector settimana=new Vector();
			String data_letta=null;
			//			String data_fine=null;
			DataWI dt;
	//		if ((vdbr != null) && (vdbr.size() > 0))
	//			for(int i=0; i<vdbr.size(); i++) {
	//				ISASRecord dbrec = (ISASRecord)vdbr.elementAt(i);
			while (dbcur.next())
			{
				ISASRecord dbrec=dbcur.getRecord();
		
					//					LOG.debug("record i="+i+" rec letto="+dbrec.getHashtable().toString());
					if (dbrec != null){
						data_letta=((java.sql.Date)dbrec.get("ag_data")).toString();						
						data_fine=formDate(data_fine,"aaaammgg");
						dt=new DataWI(formDate(data_letta,"aaaammgg"),1);
						if(primo){//ho gia la settimana che contiene data input in pancia devo vedere se settimana � caricata
							if(dt.isPrecedente(data_fine))settimana.addElement(ht);
							primo=false;
						}
						if(data_fine==null || dt.isSuccessiva(data_fine)){
							Hashtable h=new Hashtable();
							h=getEstremiWeek(data_letta,6);
							data_fine=formDate((String)h.get("data_fine"),"aaaammgg");
							settimana.addElement(h);
						}else{
							LOG.debug("non calcolo data_letta="+data_letta+" data_fine="+data_fine);
						}						
					}
				}			
			LOG.debug("FINITO...."+settimana.toString());
			return settimana;
		}finally{
			close_dbcur_nothrow(nomeEJB+"removePrestazioni", dbcur);
		}
	}

	public String ripianificaAgenda(ISASConnection dbc,Hashtable hPar,boolean ctrlFre) throws Exception{
		LOG.info(nomeEJB+".ripianificaAgenda");
		String ms=carica_agenda_apartireda(dbc, hPar,ctrlFre,true);
		LOG.debug(nomeEJB+".ripianificaAgenda agenda caricata ritorna: "+ms);
		return ms;	
	}
	public String getFirstDayWeek(String giornoSel,int dayFirst) {
		Hashtable h=getEstremiWeek(giornoSel,dayFirst);
		return h.get("data_inizio").toString();
	}
	public String getLastDayWeek(String giornoSel,int dayFirst) {
		Hashtable h=getEstremiWeek(giornoSel,dayFirst);
		return h.get("data_fine").toString();
	}
	private Hashtable getEstremiWeek(String giornoSel,int dayFirst){
		GregorianCalendar gc=dt.getGreg(giornoSel);
		int day=gc.get(Calendar.DAY_OF_WEEK);
		if (day==1)day=8;
		String data_inizio="";
		if((day-2)<=dayFirst){//carico la settimana corrente
			gc.add(gc.DATE,(day-2)*(-1));
			data_inizio=dt.getData(gc,"gg/mm/aaaa");
		}else{//vado alla prossima
			gc.add(gc.DATE,7-day+2);
			data_inizio=dt.getData(gc,"gg/mm/aaaa");
		}
		gc.add(gc.DATE,6);//arrivo alla domenica
		String data_fine=dt.getData(gc,"gg/mm/aaaa");
		Hashtable h=new Hashtable();
		h.put("data_fine",data_fine);
		h.put("data_inizio",data_inizio);
		return h;
	}
	/** 
	 *  @param ISASConnection dbc
	 * @param Hashtable h    :  isasrecord di piano_accessi/piano_assist
	 *    
        String op_esecutore=h.get("pi_op_esecutore").toString();
        String prest=((String)h.get("pi_prest_cod")).trim();		
		String strPaTipoOper =  h.get("pa_tipo_oper").toString();
		  String strNCartella =  h.get("n_cartella").toString();
		  String strNProgetto =  h.get("n_progetto").toString();
		  String strCodObiettivo =  h.get("cod_obbiettivo").toString();
		  String strNIntervento =  h.get("n_intervento").toString();		 
		  String strPiDataFine = h.get("pi_data_fine").toString();		

		String stato="0"; //cancello solo appunt con stato a 0
		String alert="0"; //cancello solo appunt non occasionali che sono quelli con alert=0
	 *   Esegue le seguenti operazioni:
	 *  BARGI 05/06/2012
	 *   1) seleziona da agenda_sett_tipo le prestazioni pianificate in join con piano_accessi
	 *   nella settimana inerenti all'operatore referente
	 *   2)carica agenda ma controlla la data fine e se inferiore alla data  da caricare  non viene caricato
	 *
	 */
	public void rimuovoAgendaCaricata(ISASConnection dbc,Hashtable h,String invocato)
	throws Exception
	{
		ISASCursor dbcur=null;
		//String strTipoOper = ""; //gb 10/09/07
		String strClausoleAggiuntive = ""; //gb 10/08/07
		String op_esecutore=h.get("pi_op_esecutore").toString();
		String prest=((String)h.get("pi_prest_cod")).trim();		
		String strPaTipoOper =  h.get("pa_tipo_oper").toString();
		String strNCartella =  h.get("n_cartella").toString();
		String strNProgetto =  h.get("n_progetto").toString();
		String strCodObiettivo =  h.get("cod_obbiettivo").toString();
		String strNIntervento =  h.get("n_intervento").toString();		 
		String strPiDataFine = h.get("pi_data_fine").toString();				
		String stato="0"; //cancello solo appunt con stato a 0
		int alert=-1; //cancello solo appunt non occasionali che sono quelli con alert=-1
		LOG.info(nomeEJB+".rimuovoAgendaCaricata / invocato da:"+invocato+" Hashtable h: " + h.toString());

		try{
			strClausoleAggiuntive = " AND cod_obbiettivo = '" +strCodObiettivo+ "'" +
			" AND n_intervento = " + strNIntervento;
			String mysel="SELECT *" +
			" FROM agendant_interv, agendant_intpre " + 
			" WHERE ag_data > " + formatDate(dbc,strPiDataFine) +
			" AND ag_oper_ref = '" + op_esecutore + "'" +
			" AND ag_cartella = " + strNCartella+
			" AND ag_contatto = " +strNProgetto +
			strClausoleAggiuntive + 
			" AND ag_tipo_oper = '" + strPaTipoOper+ "'" + 
			" AND ap_prest_cod = '" + prest+ "'" +
			" AND ag_stato = " +stato+
			" AND ap_alert <> "+alert+
			" AND ag_data = ap_data" +
			" AND ag_progr = ap_progr" +
			" AND ag_oper_ref = ap_oper_ref " +
			" ORDER BY ag_data";
			LOG.info(nomeEJB+"rimuovoAgendaCaricata select=="+mysel);
			dbcur=dbc.startCursor(mysel);
	//	Vector vdbr = dbcur.getAllRecord();
		//	if ((vdbr != null) && (vdbr.size() > 0))
	//			for(int i=0; i<vdbr.size(); i++)
	//			{
	//				ISASRecord dbrec2 = (ISASRecord)vdbr.elementAt(i);
				while (dbcur.next())
				{
					ISASRecord dbrec2=dbcur.getRecord();		
					cancellaAppuntam(dbrec2,dbc);
				}
		}finally{
			close_dbcur_nothrow(nomeEJB+"removePrestazioni", dbcur);
		}
	}


	public void scaricaAgenda(ISASConnection dbc,Hashtable h,Vector settimana)
	throws Exception
	{
		ISASCursor dbcur=null;	
		String primo_gg=(String)settimana.elementAt(0);
		String ultimo_gg=(String)settimana.elementAt(6);
		LOG.info(nomeEJB+".scaricaAgenda giorni ["+primo_gg+"****"+ultimo_gg+"]");	
		String stato="0"; //cancello solo appunt con stato a 0
		int alert=-1; //cancello solo appunt non occasionali che sono quelli con alert=-1
		try{
			String mysel="SELECT *" +
			" FROM agendant_interv, agendant_intpre " + 
			" WHERE ag_data >= " + formatDate(dbc,primo_gg) +
			" and ag_data<="+ formatDate(dbc,ultimo_gg) +
			" AND ag_oper_ref = '" +((String)h.get("referente")).trim()+ "'" +
			" AND ag_stato = " +stato+
			" AND ap_alert <> "+alert+
			" AND ag_data = ap_data" +
			" AND ag_progr = ap_progr" +
			" AND ag_oper_ref = ap_oper_ref ";
			if(h.get("n_cartella")!=null)
				mysel+=" and ag_cartella="+h.get("n_cartella");	 	 
			if(h.get("pi_prest_cod")!=null)
				mysel+=" and ap_prest_cod='"+h.get("pi_prest_cod").toString().trim()+"'";	 	
			LOG.info(nomeEJB+"scaricaAgenda select=="+mysel);
			dbcur=dbc.startCursor(mysel);
			while (dbcur.next())
			{
				ISASRecord dbrec2=dbcur.getRecord();
			
			
		//	Vector vdbr = dbcur.getAllRecord();
			//if ((vdbr != null) && (vdbr.size() > 0))
				//for(int i=0; i<vdbr.size(); i++)
			//	{
				//	ISASRecord dbrec2 = (ISASRecord)vdbr.elementAt(i);
					cancellaAppuntam(dbrec2,dbc);
				}
		}finally{
			close_dbcur_nothrow(nomeEJB+"scaricaAgenda", dbcur);
		}
	}
	/**
	 * 
	 * @param dbrec isas record contenente agendant_interv e agendant_intpre in join
	 * @param dbc
	 * @throws DBRecordChangedException
	 * @throws ISASPermissionDeniedException
	 * @throws SQLException
	 */
	public void cancellaAppuntam(ISASRecord dbrec,ISASConnection dbc)
	throws Exception
	{
		String data=((java.sql.Date)dbrec.get("ap_data")).toString();
		String selag="SELECT * FROM agendant_intpre WHERE "+ 
		"ap_data="+formatDate(dbc,data)+" AND "+
		"ap_progr="+dbrec.get("ap_progr")+" AND "+
		"ap_oper_ref='"+(String)dbrec.get("ap_oper_ref")+"' AND "+
		"ap_prest_cod='"+(String)dbrec.get("ap_prest_cod")+"'";
		ISASRecord dbag=dbc.readRecord(selag);
		if(dbag!=null){
			dbc.deleteRecord(dbag);         
			LOG.info(nomeEJB+"cancellaAppuntam intpre  " + selag);//elisa b 15/05/12
			dbag=null;
			//devo controllare se sono rimasti record su agenda_intpre se non
			//ce ne sono occorre cancellare anche il record su agenda_interv
			selag="SELECT COUNT(*) tot FROM agendant_intpre WHERE "+ //gb 29/08/07
			"ap_data="+formatDate(dbc,data)+" AND "+
			"ap_progr="+dbrec.get("ap_progr")+" AND "+
			"ap_oper_ref='"+(String)dbrec.get("ap_oper_ref")+"'";
			dbag=dbc.readRecord(selag);
			LOG.info(nomeEJB+"cancellaAppuntam intpre sele x cancellare interv  " + selag);//elisa b 15/05/12
			int t=0;
			if(dbag!=null) t=util.getIntField(dbag,"tot");//convNumDBToInt("tot",dbag);
			if(t==0)
			{
				//cancello da agenda_interv
				LOG.info(nomeEJB+"cancellaAppuntam interv " );//elisa b 15/05/12
				selag="SELECT * FROM agendant_interv WHERE "+ //gb 29/08/07
				"ag_data="+formatDate(dbc,data)+" AND "+
				"ag_progr="+dbrec.get("ap_progr")+" AND "+
				"ag_oper_ref='"+(String)dbrec.get("ag_oper_ref")+"'";
				dbag=dbc.readRecord(selag);
				dbc.deleteRecord(dbag);            
				LOG.info(nomeEJB+"cancellaAppuntam " + selag);//elisa b 15/05/12
			}
		}	 
	}

	private int caricoAgendaInterv(ISASConnection dbc,ISASRecord dbrec,String data,Hashtable h,String primo_gg,String ultimo_gg)
	throws Exception
	{
		ISASRecord dbmax = null;
		int progressivo=0;
		String selInterv=" select  ag_progr from agendant_interv where "+
		" ag_data =" +formatDate(dbc,data) +
		" and ag_oper_ref= '"+(String)dbrec.get("as_op_referente")+"'"+
		" and ag_tipo_oper= '"+(String)dbrec.get("as_tipo_oper")+"'"+
		" and ag_cartella="+dbrec.get("n_cartella")+
		" and ag_contatto="+dbrec.get("n_progetto")+			
		" and cod_obbiettivo = '" + dbrec.get("cod_obbiettivo") + "' "+
		" and n_intervento = " + dbrec.get("n_intervento")+
		" and ag_orario = " + dbrec.get("as_orario");
		LOG.debug(nomeEJB+".caricoAgendaInterv  x rec="+dbrec.getHashtable().toString());

		//	LOG.debug(nomeEJB+".caricoAgendaInterv select x interv=="+selInterv);
		dbmax = dbc.readRecord(selInterv);
		if (dbmax != null) {
			if(dbmax.get("ag_progr")!=null  ){
				//	LOG.debug(nomeEJB+".caricoAgendaInterv ritorno sel max: " +dbmax.toString());
				int max = ((Integer)dbmax.get("ag_progr")).intValue();				
				progressivo = max;
			}
			//	LOG.debug(nomeEJB+".caricoAgendaInterv  esiste rec =="+progressivo);
			return progressivo;
		}else {		
			LOG.debug(nomeEJB+".caricoAgendaInterv  non esiste rec  invoco ISCaricata x data="+data);
			boolean isCaricata=ISCaricata(dbc,dbrec,primo_gg,ultimo_gg,data);
			if(isCaricata) {
				LOG.debug(nomeEJB+".caricoAgendaInterv  ISCaricata true x data="+data);					
				return -1;
			}
			String selmax = "SELECT MAX(ag_progr) max "+
			" FROM agendant_interv " + 
			" WHERE ag_data =" +formatDate(dbc,data);
			dbmax = dbc.readRecord(selmax);
			LOG.debug(nomeEJB+".caricoAgendaInterv devo inserire nuovo rec agendant_interv "+data);
			if (dbmax != null) {
				if(dbmax.get("max")!=null){
					int max = ((Integer)dbmax.get("max")).intValue();
					max++;
					progressivo = max;
				}
			}
			//	LOG.debug(nomeEJB+".caricoAgendaInterv  agendainterv non esiste progr=="+progressivo);
		}

		ISASRecord rec_ag=dbc.newRecord("agendant_interv");
		rec_ag.put("ag_data", formDate(data,"aaaa-mm-gg"));
		rec_ag.put("ag_progr", new Integer(progressivo));
		rec_ag.put("ag_cartella",dbrec.get("n_cartella"));
		//mod 30/05/12 rec_ag.put("ag_contatto",dbrec.get("n_contatto"));
		rec_ag.put("ag_contatto",dbrec.get("n_progetto"));
		rec_ag.put("cod_obbiettivo",dbrec.get("cod_obbiettivo"));
		rec_ag.put("n_intervento",dbrec.get("n_intervento"));
		rec_ag.put("ag_oper_ref", dbrec.get("as_op_referente"));
		rec_ag.put("ag_tipo_oper",dbrec.get("as_tipo_oper")); 
		rec_ag.put("ag_orario",dbrec.get("as_orario"));
		rec_ag.put("ag_oper_esec", "");
		rec_ag.put("ag_stato", "0");
		//rif sett tipo
		rec_ag.put("st_data", formDate(dbrec.get("as_data"),"aaaa-mm-gg"));
		rec_ag.put("st_prog", dbrec.get("as_prog"));
		rec_ag.put("st_orario",dbrec.get("as_orario"));
		rec_ag.put("st_giorno_sett", dbrec.get("as_giorno_sett"));
		rec_ag.put("st_op_referente",dbrec.get("as_op_referente"));
		dbc.writeRecord(rec_ag);
		LOG.debug(nomeEJB+".caricoAgendaInterv  nuovo rec=" + rec_ag.getHashtable().toString());//elisa b 16/05/12
		return progressivo;
	}


	private boolean caricoAgendaIntpre(ISASConnection dbc, ISASRecord dbrec, Hashtable hPar,int progressivo, String data)
	throws Exception
	{
		// 1) prelevo la prestazione inserito per quel giorno
		ISASRecord db = null;
		String msg="";
		boolean proseguo=false;
		LOG.info(nomeEJB+".caricoAgendaIntpre, hpar: "+hPar.toString());
		
		LOG.info(nomeEJB+".caricoAgendaIntpre, reci: "+dbrec.getHashtable().toString());
		String sel = "SELECT pi_prest_cod, pi_freq, pi_prest_qta "+
		" FROM piano_accessi " + 
		" WHERE pa_data =" +formatDate(dbc,(dbrec.get("as_data")).toString())+
		" and pi_prog="+dbrec.get("as_prog")+
		" and pa_tipo_oper='"+(String)dbrec.get("as_tipo_oper")+"'"+ //gb 12/09/07
		" and n_cartella="+dbrec.get("n_cartella")+
		//mod 30/05/12 " and n_progetto = " + dbrec.get("n_contatto") +
		" and n_progetto = " + dbrec.get("n_progetto") +
		" and cod_obbiettivo = '" + dbrec.get("cod_obbiettivo") + "'" +
		" and n_intervento = " + dbrec.get("n_intervento");
		LOG.info(nomeEJB+".caricoAgendaIntpre, select su piano_accessi: "+sel);

		db = dbc.readRecord(sel);
		String prest="";
		String freq="";
		Integer quantita=new Integer(0);
		if (db != null) {
			//faccio l'update su cartella
			prest = (String)db.get("pi_prest_cod");
			freq  = (String)db.get("pi_freq");
			quantita  = (Integer)db.get("pi_prest_qta");
			String selCtr = "select * from agendant_intpre " +
			" where ap_data="+formatDate(dbc,data)+
			" AND ap_progr="+new Integer(progressivo)+
			" AND ap_oper_ref='"+dbrec.get("as_op_referente").toString()+"'"+
			" AND ap_prest_cod='"+prest+"'";

			//LOG.info(nomeEJB+".caricoAgendaIntpre, select su agendant_intpre: "+selCtr);

			ISASRecord rec_ag = dbc.readRecord(selCtr);
			if(rec_ag==null){
				Hashtable h=new Hashtable();
				h.put("pi_prest_cod",prest);
				h.put("n_cartella",(dbrec.get("n_cartella")).toString());
				//mod 30/05/12 h.put("n_contatto",(dbrec.get("n_contatto")).toString());
				h.put("n_contatto",(dbrec.get("n_progetto")).toString());					
				//aggiunto 30/05/12
				h.put("n_progetto",(dbrec.get("n_progetto")).toString());					
				h.put("cod_obbiettivo",dbrec.get("cod_obbiettivo").toString());
				h.put("n_intervento",(dbrec.get("n_intervento")).toString());
				//	LOG.debug(nomeEJB+".caricoAgendaIntpre: orario:"+dbrec.get("as_orario"));
				h.put("as_orario",dbrec.get("as_orario"));//bargi 30/11/2011
				h.put("as_op_referente",dbrec.get("as_op_referente"));
				//	proseguo=! ISCaricata(dbc,  h, data);
				//if (proseguo)

				LOG.debug(nomeEJB+".caricoAgendaIntpre devo inserire nuovo rec agendant_intpre data="+data+ " controllo freq?"+ctrlFreq);
				if(ctrlFreq)
					proseguo= ISCorrectTime(dbc, freq, h, data);
				else {
				 	proseguo=true;
					if(hPar.get("data_inizio_ag")!=null ) {
						LOG.debug(nomeEJB+".caricoAgendaIntpre sono nel caso in cui NON controllo freq ma testo " +
								" se la data inizio caric > data nuovo piano" +
								" se � > controllo frequenza ==>["+hPar.get("data_inizio_ag").toString()+"]>["+dbrec.get("pi_data_inizio").toString()+"]");						
						//data di caricamento > data piano devo controllare la frequenza
						if(dt.confrontaDate(hPar.get("data_inizio_ag").toString(), dbrec.get("pi_data_inizio").toString())==1) {
							proseguo= ISCorrectTime(dbc, freq, h, data);
							LOG.debug(nomeEJB+".caricoAgendaIntpre controllata frequenza proseguo? "+proseguo);
						}
					}
				}
				if (proseguo){
					LOG.debug(nomeEJB+".caricoAgendaIntpre ok inserisco");
					rec_ag = dbc.newRecord("agendant_intpre");
					rec_ag.put("ap_data", formDate(data,"aaaa-mm-gg"));
					rec_ag.put("ap_progr", new Integer(progressivo));
					rec_ag.put("ap_prest_cod",prest);
					rec_ag.put("ap_oper_ref",dbrec.get("as_op_referente"));
					rec_ag.put("ap_stato",new Integer(0));
					rec_ag.put("ap_prest_qta",quantita);
					rec_ag.put("ap_alert",freq);
					rec_ag.put("ap_cartella",dbrec.get("n_cartella"));
					dbc.writeRecord(rec_ag);
					LOG.info(nomeEJB+".caricoAgendaIntpre scrivo=" + rec_ag.getHashtable().toString());//elisa b 16/05/12
				}
			}else{
				LOG.debug(nomeEJB+".caricoAgendaIntpre record su agendant_intpre presente ");
				//msg="\n ATTENZIONE per la cartella: "+dbrec.get("n_cartella")+
				//"\n esiste un progetto chiuso ancora valido con stessa prestazione pianificata \n per nuovo progetto: ["+prest+"]";
			}
		}
		LOG.debug(nomeEJB+".caricoAgendaIntpre  caricata? "+proseguo);
		messaggio_carica+=msg;
		return proseguo;

	}
	private String getDataUltimaErogazione(ISASConnection dbc, Hashtable h, String data,String frequenza)
	throws Exception{
		String data_primogg=getFirstDayWeek(data, 6);
		String cartella=(String)h.get("n_cartella");
		//commentato 30/05/12 String contatto=(String)h.get("n_contatto");
		//aggiunto 30/05/12
		String strNProgetto = (String)h.get("n_progetto");			
		String strCodObiettivo = (String)h.get("cod_obbiettivo");
		String strNIntervento = (String)h.get("n_intervento");
		String prest=(String)h.get("pi_prest_cod");
		String oper=(String)h.get("as_op_referente");
		String sel="SELECT max(ag_data) data_max "+
		" FROM agendant_interv, agendant_intpre WHERE "+ //gb 12/09/07
		" ag_cartella="+cartella+" AND "+
		//mod 30/05/12 " ag_contatto="+contatto+" AND "+
		" ag_contatto="+strNProgetto+" AND "+			
		" cod_obbiettivo = '" + strCodObiettivo + "' AND "+
		" n_intervento = " + strNIntervento + " AND "+
		//" ag_data<"+formatDate(dbc,data)+" AND "+
		" ag_data<"+formatDate(dbc,data_primogg)+" AND "+
		" ag_data=ap_data AND "+
		" ag_progr=ap_progr AND "+
		" ag_oper_ref=ap_oper_ref AND "+
		" ag_oper_ref='"+oper+"' AND "+
		" ap_prest_cod='"+prest+"'"+
		" and ap_alert="+frequenza;//devo controllare con ultima prestazione caricata e che abbia la stessa frequenza 12/01/2012
		LOG.info(nomeEJB+".getDataUltimaErogazione, select data_max: "+sel);
		ISASRecord is = dbc.readRecord(sel);
		if(is==null || is.get("data_max")==null ||
				(((java.sql.Date)is.get("data_max")).toString()).equals(""))
			return null;
		else{
			return formDate(((java.sql.Date)is.get("data_max")).toString(),"gg-mm-aaaa");
		}	
	}


	/** 
	 *  @param ISASConnection dbc
	 *  @param String frequenza
	 * @param Hashtable h    :  hashtable deve contenere il vettore di vettori key="giorni" (con tutte le settimane) 
	 *                                                    il "referente" e il "tipo_operatore"

	 *  @param String data	: la data che voglio caricare in agenda                                                    
	 *   
	 *   Esegue le seguenti operazioni:
	 *  
	 *   1) seleziona da agenda_sett_tipo le prestazioni pianificate in join con piano_accessi
	 *   nella settimana inerenti all'operatore referente
	 *   2)carica agenda ma controlla la data fine e se inferiore alla data  da caricare  non viene caricato
	 *
	 */
	public boolean ISCorrectTime(ISASConnection dbc, String frequenza, Hashtable h, String data)
	throws Exception{
		int numSett=getNumSettFreq(dbc,frequenza);
		if(numSett<=1)return true;
		String giornoUltimaErog=getDataUltimaErogazione(dbc,h,data,frequenza);
		if(giornoUltimaErog==null)return true;
		String dataDaErogare=formDate(data,"gg-mm-aaaa");
		boolean rit=calcola(giornoUltimaErog,numSett,dataDaErogare);
		LOG.debug(nomeEJB+" ISCorrectTime?"+rit);
		return rit;	
	}
	private  boolean calcola(String giornoUltimaErog,int numSett,String dataDaErogare) {
		 String nomeEJB="TEST";
			LOG.debug(nomeEJB+"calcola giornoUltimaErog=["+giornoUltimaErog+
					"]dataDaErogare=["+dataDaErogare+"] frequenza num settimane="+numSett);
			int gg=7*numSett;
			String dataPossibile=dt.getDataNGiorni(giornoUltimaErog, gg);
			 LOG.debug(nomeEJB+"calcola dataPossibile=["+dataPossibile+"]");
			 
			 String dataDomPossib=getLastDayWeek(dataPossibile,6);
			 LOG.debug(nomeEJB+"calcola dataDomPossib=["+dataDomPossib+"]");			 
		
			 String dataLunPossib=getFirstDayWeek(dataPossibile,6);
			 LOG.debug(nomeEJB+"calcola dataLunPossib=["+dataLunPossib+"]");
			 LOG.debug(nomeEJB+" controllo se la dataDaErogare = dataDomenica o lunedi della data possibile ---> ok");
			// dataDaErogare>dataDomPossibile --->ok
			 if(dt.confrontaDate(dataDaErogare,dataDomPossib)==0)return true;
			 if(dt.confrontaDate(dataDaErogare,dataLunPossib)==0)return true;
			 LOG.debug(nomeEJB+" controllo se la dataDaErogare successiva a  data Domenica della data possibile " +
			 		" oppure " +
			 		"se la dataDaErogare rientra nella settimana in cui cade la data possibile ---> ok");
			/* " controllo se la dataDaErogare successiva a  data Domenica della data possibile " +
		 		" oppure " +
		 		"se la dataDaErogare rientra nella settimana in cui cade la data possibile ---> ok"*/
			 if((dt.confrontaDate(dataDaErogare,dataDomPossib)==1)||
					( (dt.confrontaDate(dataDaErogare,dataLunPossib)==1)&&dt.confrontaDate(dataDaErogare,dataDomPossib)==2)) return true;
			 return false;
	 }
	private  boolean calcola_20120608(String giornoUltimaErog,int numSett,String dataDaErogare) {
		//LOG.debug(nomeEJB+"calcola giornoUltimaErog=["+giornoUltimaErog+"]dataDaErogare=["+dataDaErogare+"] frequenza num settimane="+numSett);
		int gg=7*numSett;
		String dataPossibile=dt.getDataNGiorni(giornoUltimaErog, gg);
		// LOG.debug(nomeEJB+"calcola dataPossibile=["+dataPossibile+"]");
		int numSettPossibileErog=NumSettYear(dataPossibile);
		// LOG.debug(nomeEJB+"calcola numSettPossibileErog=["+numSettPossibileErog+"]");
		int annoSettPossibileErog=dt.getAnno(dataPossibile);
		// LOG.debug(nomeEJB+"calcola annoSettPossibileErog=["+annoSettPossibileErog+"]");
		int numSettDaErog=NumSettYear(dataDaErogare);
		// LOG.debug(nomeEJB+"calcola numSettDaErog=["+numSettDaErog+"]");
		int annoSettDaErog=dt.getAnno(dataDaErogare);
		// LOG.debug(nomeEJB+"calcola annoSettDaErog=["+annoSettDaErog+"]");						
/* elisa b 19/07/12		 
* se la data di possibile erogazione cade nella prima		 
* settimana dell'anno successivo a quello di ultima erogazione si ha 		 
* un disallineamento tra annoSettPossibileErog e numSettPossibileErog		 
* perche' l'anno coincide con quello corrente ma il numero		 
* della settimana e' pari a 1 (la prima del nuovo anno)		 
* In questo caso, se la data possibile e quella di confronto non		 
* coincidono, si considera l'anno come successivo a quello corrente*/
		int meseSettPossibileErog=dt.getMese(dataPossibile);		
if((meseSettPossibileErog == 12) && (numSettPossibileErog == 1)){
			if(!dataPossibile.equals(dataDaErogare)){	
			annoSettPossibileErog ++;
				LOG.debug(nomeEJB+"calcola CASO PARTICOLARE mod annoSettPossibileErog = ["+annoSettPossibileErog+"]");				LOG.debug(nomeEJB+"calcola giornoUltimaErog=["+giornoUltimaErog+"]dataDaErogare=["+dataDaErogare+"] frequenza num settimane="+numSett);			}
		}
		/*fine*/			
		if(annoSettPossibileErog==annoSettDaErog) 
			if(numSettPossibileErog<=numSettDaErog)return true;
		if(annoSettPossibileErog<annoSettDaErog)	
			if(numSettPossibileErog>=numSettDaErog)return true;
			else return false;
		return false;
	}

	private boolean ISCaricata(ISASConnection dbc, ISASRecord dbrec, String primo_gg,String ultimo_gg,String data)
	throws Exception{
		LOG.debug(nomeEJB+"ISCaricata "+dbrec.getHashtable().toString());
		ISASRecord dbmax = null;
		int progressivo=0;

		String selInterv=" select  count(ag_progr) tot from agendant_interv,agendant_intpre where "+
		" ag_data<="+formatDate(dbc,ultimo_gg)+
		" and ag_data>="+formatDate(dbc,primo_gg)+
		" and (ag_oper_ref= '"+(String)dbrec.get("as_op_referente")+"'"+
		" or st_op_referente= '"+(String)dbrec.get("as_op_referente")+"')"+
		" and ag_tipo_oper= '"+(String)dbrec.get("as_tipo_oper")+"'"+
		" and ag_cartella="+dbrec.get("n_cartella")+
		" and ag_contatto="+dbrec.get("n_progetto")+			
		" and cod_obbiettivo = '" + dbrec.get("cod_obbiettivo") + "' "+
		" and n_intervento = " + dbrec.get("n_intervento")+
		" and ag_orario = " + dbrec.get("as_orario")+
		" and ag_data=ap_data AND "+
		" ag_progr=ap_progr AND "+
		" ag_oper_ref=ap_oper_ref AND "+
		" ap_prest_cod='"+ dbrec.get("pi_prest_cod")+"'";

		LOG.debug(nomeEJB+"ISCaricata sel=="+selInterv);
		dbmax = dbc.readRecord(selInterv);
		int tCaricati=0;
		if(dbmax!=null){

			tCaricati=util.getIntField(dbmax,"tot");
			//		 LOG.debug(nomeEJB+"ISCaricata CARICATI=="+tCaricati);
		}
		if(tCaricati>0) {
			String mysel ="SELECT count(*) tot "+
			" from agendant_sett_tipo " + 
			" where as_op_referente='"+((String)dbrec.get("as_op_referente")).trim()+"'"+
			" and as_tipo_oper= '"+(String)dbrec.get("as_tipo_oper")+"'"+
			" and n_cartella="+dbrec.get("n_cartella")+
			" and n_contatto="+dbrec.get("n_progetto")+			
			" and cod_obbiettivo = '" + dbrec.get("cod_obbiettivo") + "' "+
			" and n_intervento = " + dbrec.get("n_intervento")+
			" and as_orario = " + dbrec.get("as_orario")+
			" and as_data =" +formatDate(dbc,dbrec.get("as_data").toString())+
			"and as_prog ="+dbrec.get("as_prog");		
			LOG.debug(nomeEJB+"ISCaricata mysel=="+mysel);
			dbmax = dbc.readRecord(mysel);
			int tPrevisti=0;
			if(dbmax!=null){
				tPrevisti=util.getIntField(dbmax,"tot");
				//	 LOG.debug(nomeEJB+"ISCaricata previsti=="+tPrevisti);
			}
			if(tCaricati>=tPrevisti) {
				LOG.debug(nomeEJB+"ISCaricata NON CARICO "+dbrec.get("pi_prest_cod")+ " ");
				return true;				  
			}
		}
		return false;
	}
	public int NumSettYear(String data){
		GregorianCalendar gc =dt.getGreg(data);		
		int NumSettYear=gc.get(Calendar.WEEK_OF_YEAR);
		//LOG.debug("WEEK_OF_YEAR erog: " + NumSettYear);
		return NumSettYear;
	}

	private int getNumSettFreq(ISASConnection dbc,String frequenza)throws Exception{
		if(frequenza.equals(""))return 1;
		String seltab = "SELECT tab_numero FROM tab_voci "+
		" WHERE tab_cod = 'FREQAC'"+
		" AND tab_val = '"+frequenza+"'"+
		" and tab_numero is not null";
		ISASRecord dbr=dbc.readRecord(seltab);
		if (dbr != null){
			return ((Integer)dbr.get("tab_numero")).intValue();
		}
		return 1;
	}


	private	String formDate(Object dataI,String	formato){
		NumberDateFormat ndf=new NumberDateFormat();
		return ndf.formDate(dataI,formato,false);
	}
}

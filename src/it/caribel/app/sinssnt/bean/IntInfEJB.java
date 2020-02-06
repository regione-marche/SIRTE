package it.caribel.app.sinssnt.bean;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 19/09/2000 - EJB di connessione alla procedura SINS Tabella IntInf
//
// paolo ciampolini
//
// ==========================================================================

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOSKSoProrogheEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

public class IntInfEJB extends SINSSNTConnectionEJB  {
public IntInfEJB() {}

	private static final String ver ="13-IntInfEJB ";
	
	public Vector<ISASRecord> query(myLogin mylogin,Hashtable<String, Object> h) throws  SQLException {
		String punto = ver + "query ";
		LOG.info(punto + " inizio con dati>>"+ h);
		ISASConnection dbc = null;
//		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr= new Vector<ISASRecord>();
//		String myselect = "";
		try{
			dbc=super.logIn(mylogin);
			
			vdbr = recuperaElementiAccessi(dbc,h);
			
//			myselect = recuperaQueryAccessi(dbc, h);
//			
//			LOG.trace(punto + "query "+ myselect);
//			
//			dbcur=dbc.startCursor(myselect);
//			vdbr= dbcur.getAllRecord();
		}
	    	catch(Exception e){
	    		e.printStackTrace();
		     e.printStackTrace();
		     throw new SQLException("Errore eseguendo una query() in IntInf ");
	    	}
	    	finally{
//	    		close_dbcur_nothrow(punto, dbcurConf);
	    		logout_nothrow(punto, dbc);
	    	}
		return vdbr;
	    }


	public Vector<ISASRecord> recuperaElementiAccessi(ISASConnection dbc, Hashtable<String, Object> h)
			throws ISASMisuseException, DBMisuseException, DBSQLException, ISASPermissionDeniedException {
		String punto = ver + "recuperaElementiAccessi ";
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			String myselect = recuperaQueryAccessi(dbc, h);
			LOG.trace(punto + "query " + myselect);
			dbcur = dbc.startCursor(myselect);
			vdbr = (Vector<ISASRecord>) dbcur.getAllRecord();  
		} finally {
			close_dbcur_nothrow(punto, dbcur);
		}
		return vdbr;
	}

	public String recuperaQueryAccessi(ISASConnection dbc,Hashtable<String, Object> h) throws ISASMisuseException,
			DBMisuseException, DBSQLException, ISASPermissionDeniedException {
		String myselect = "";
		ISASCursor dbcurConf = null;
		String punto = ver + "recuperaQueryAccessi ";
		try {
			String tipoOper = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_TIPO_OPERATORE);
			String critTpPrest = "";
			String aggiunta = ")";
			if (ISASUtil.valida(tipoOper)) {
				if ((tipoOper != null) && (tipoOper.trim().equals("01")))
					aggiunta = " OR conf_key = 'TIPDEF" + tipoOper + "B')";

				String selConf = "SELECT * FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND (conf_key ='TIPDEF"
						+ tipoOper + "'" + aggiunta;
				dbcurConf = dbc.startCursor(selConf);

				while (dbcurConf.next()) {
					ISASRecord dbconf = dbcurConf.getRecord();
					if ((dbconf != null) && (dbconf.get("conf_txt") != null)) {
						String tpPrest = ((String) dbconf.get("conf_txt")).trim();
						if (critTpPrest.trim().equals(""))
							critTpPrest = " AND (a.int_tipo_prest = '" + tpPrest + "'";
						else
							critTpPrest += " OR a.int_tipo_prest = '" + tpPrest + "'";
					}
				}
				if (ISASUtil.valida(critTpPrest)){
					critTpPrest += ")";
				}else {
					critTpPrest +=  " AND (a.int_tipo_prest = '" +CostantiSinssntW.CTS_TIPO_PRESTAZIONE_NON_CENSITA  + "' )";
				}
				
			}
			// Aggiunte le istruzioni sul tipo operatore in modo da
			// tirare fuori solamente gli interventi inseriti da un
			// opertore dello stesso gruppo
			myselect = recuperaQuery(dbc, h, critTpPrest);
		} finally {
			close_dbcur_nothrow(punto, dbcurConf);
		}
		return myselect;
	}


	public String recuperaQuery(ISASConnection dbc, Hashtable h, String critTpPrest) throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		StringBuffer myselect = new StringBuffer();

		String punto = ver + "recuperaQuery ";
		LOG.trace(punto + " ");
		String critDataPrest = "";
		String critDataPrestSO = "";
		String critIntContatto = "";
		String tipoOperatore = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_TIPO_OPERATORE);
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_INT_CARTELLA);
		String perDataInizio = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO);
		String perDataFine = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE);
		String codOperatore =ISASUtil.getValoreStringa(h, CostantiSinssntW.COD_OPERATORE);
		
		
		String intContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_INT_CONTATTO);
		boolean isSegreteriaOrganizzativa = ISASUtil.getvaloreBoolean(h, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
		boolean isStatoFlussoSiad = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_ACCESSI_FLUSSI_SIAD_INVIATO); 
		String dtFineProroga = "";
		String dtFinePeriodo = "";
		
		
		if (isSegreteriaOrganizzativa) {
			dtFineProroga = perDataFine; // il calcolo viene effettuato nella parte della SO
		}		
		
		if (ManagerDate.validaData(perDataInizio)) {
			critDataPrest = " and a.int_data_prest >= " + formatDate(dbc, perDataInizio);
			critDataPrestSO = " AND b.int_data >= " + formatDate(dbc, perDataInizio); 
		}
		
		if (isSegreteriaOrganizzativa) {
			if (ManagerDate.validaData(dtFineProroga)) {
				dtFinePeriodo = formatDate(dbc, dtFineProroga);
				critDataPrest += " and a.int_data_prest <= " + dtFinePeriodo;
			}
		} else {
			if (ManagerDate.validaData(perDataFine)) {
				dtFinePeriodo = formatDate(dbc, perDataFine);
				critDataPrest += " and a.int_data_prest <= " +dtFinePeriodo ;
			}
		}
		

		if (ISASUtil.valida(intContatto)) {
			critIntContatto = " and ( a.int_contatto = " + intContatto +" or a.int_contatto = 0 ) " ;
		}
		myselect.append("SELECT DISTINCT data_prest, tempo_prest, prest_des,  tipo_prest,operatore ");
		if (isStatoFlussoSiad){
			myselect.append(", flag_sent ");
		}
		myselect.append("FROM ( ");
		myselect.append(" select a.int_data_prest data_prest, a.int_tempo tempo_prest,");
		myselect.append(" e.prest_des prest_des, c.tippre_des tipo_prest, ");
		myselect.append(" nvl(trim(d.cognome),'')|| ' ' ||  nvl(trim (d.nome),'') operatore");
		if (isStatoFlussoSiad){
			myselect.append(", a.flag_sent ");	
		}
		myselect.append(" from ");
		myselect.append(" interv a, intpre b, tippre c, operatori d, prestaz e");
		myselect.append(recuperaQueryUbicazione(h, true,"",""));
		myselect.append("  where e.prest_cod = b.pre_cod_prest ");
		if (ISASUtil.valida(nCartella)){
			myselect.append(" and a.int_cartella = " + nCartella);
		}
		myselect.append(critIntContatto);
		myselect.append(" and a.int_anno = b.pre_anno and a.int_contatore = b.pre_contatore ");
		myselect.append(" and a.int_tipo_prest = c.tippre_cod and a.int_cod_oper = d.codice ");
		myselect.append(critTpPrest);
		myselect.append(critDataPrest);

		if (ISASUtil.valida(codOperatore)){
			myselect.append(" AND d.codice = '"+codOperatore+"' ");
		}
		myselect.append(critDataPrest);
		myselect.append(recuperaQueryUbicazione(h, false, "a.int_cartella", dtFinePeriodo));
		if (isStatoFlussoSiad){
			myselect.append(" AND a.flag_sent in ( "+ CostantiSinssntW.FLAG_DA_INVIARE_V+", "+ CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO +")");
		}
		
		if (isSegreteriaOrganizzativa && 
				(!ISASUtil.valida(tipoOperatore) || 
				(ISASUtil.valida(tipoOperatore) && tipoOperatore.equals(GestTpOp.CTS_COD_MMG)) ) ){
			myselect.append(" UNION SELECT b.int_data data_prest, null tempo_prest, e.pipp_des prest_des, ");
			myselect.append(" c.tippre_des tipo_prest, NVL (TRIM (d.mecogn), '') || ' ' || NVL (TRIM (d.menome), '') operatore ");
			myselect.append(" FROM intmmg b, tippre c, medici d, tabpipp e ");
			myselect.append(recuperaQueryUbicazione(h, true, "",""));
			myselect.append(" WHERE b.int_prestaz = e.pipp_codi ");
			if (ISASUtil.valida(nCartella)){
				myselect.append(" AND b.int_cartella = "+ nCartella);
			}
			myselect.append(" AND c.tippre_cod = '" +GestTpOp.CTS_COD_MMG+"' ");
//			myselect.append(" AND b.int_codoper = d.codice ");
			myselect.append(" AND b.int_medico = d.mecodi ");
			if (ISASUtil.valida(codOperatore)){
				myselect.append(" AND d.mecodi = '"+codOperatore+"' ");
			}
			myselect.append(recuperaQueryUbicazione(h, false,"b.int_cartella", dtFinePeriodo ));
			
			if (isStatoFlussoSiad){
				myselect.append(" AND b.flag_sent in ( "+ CostantiSinssntW.FLAG_DA_INVIARE_V+", "+ CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO +")");
			}
			myselect.append(critDataPrestSO);
//			String critDataPrestSOFine= recuperaEventualiProroghe(dbc, nCartella, idSkso, perDataFine);
			if (ManagerDate.validaData(dtFineProroga)){
				myselect.append(" AND b.int_data <= " + formatDate(dbc, dtFineProroga));
			}
		}else {
//			myselect.append("  UNION select a.int_data_prest data_prest, a.int_tempo tempo_prest,");
//			myselect.append(" e.prest_des prest_des, c.tippre_des tipo_prest, ");
//			myselect.append(" nvl(trim(d.cognome),'')|| ' ' ||  nvl(trim (d.nome),'') operatore from ");
//			myselect.append(" interv a, intpre b, tippre c, operatori d, prestaz e  where ");
//			myselect.append(" a.int_cartella = " + nCartella);
//			myselect.append(" and a.int_contatto = 0 ");
//			myselect.append(" and a.int_anno = b.pre_anno and a.int_contatore = b.pre_contatore ");
//			myselect.append(" and a.int_tipo_prest = c.tippre_cod and a.int_cod_oper = d.codice ");
//			myselect.append(critTpPrest);
//			myselect.append(critDataPrest);
//			myselect.append(" and e.prest_cod = b.pre_cod_prest ");
		}
		
		myselect.append(" ) order by data_prest desc");

		LOG.trace(punto + " query>>" +myselect);
		
		return myselect.toString();
	}
	
	private String recuperaQueryUbicazione(Hashtable h, boolean from, String campoCartella, String dataFine) {
		String punto = ver + "recuperaQueryUbicazione ";
		boolean isUbicazione = verificaSeUbicazionePresente(h);
		String query = "";
		if (isUbicazione) {
			if (from) {
				query = ", cartella cl, anagra_c ac ";
			} else {
				String codZona = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_STP_REPORT_ZONE);
				String codDistretto = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_STP_REPORT_DISTRETTO);
				String codPresidio = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_STP_REPORT_PCA);
				
				query = "  and cl.n_cartella = " + campoCartella +
                    " and ac.n_cartella = cl.n_cartella and ac.data_variazione in " +
                    " (select max(x.data_variazione) from anagra_c x where x.n_cartella = cl.n_cartella ";
                    if (ISASUtil.valida(dataFine)){
                    	query +=" and ac.data_variazione  <= " +dataFine;
                    	
                    }
                    query +=" ) AND (   (   (ac.comune_rep IS NULL) OR (ac.comune_rep IN ( "
					+ recuperaQueryInUbicazione(codZona, codDistretto, codPresidio) + ") ) ) "
					+ " OR (   (ac.dom_citta IS NULL)OR (ac.dom_citta IN ( "
					+ recuperaQueryInUbicazione(codZona, codDistretto, codPresidio) + " ) ) ) "
					+ " OR (   (ac.citta IS NULL) OR (ac.citta IN ( "
					+ recuperaQueryInUbicazione(codZona, codDistretto, codPresidio) + " ) ) ) ) ";
			}
		} else {
			LOG.debug(punto + " non e stato selezionato l'ubicazione ");
		}

		return query;
	}

	private String recuperaQueryInUbicazione(String codZona, String codDistretto, String codPresidio) {
		String query = " SELECT codice FROM ubicazioni_n WHERE cod_zona = '" + codZona + "' ";
		if (ISASUtil.valida(codDistretto)) {
			query += " AND cod_distretto = '" + codDistretto + "' ";

			if (ISASUtil.valida(codPresidio)) {
				query += " and tipo ='P' and codice = '" + codPresidio + "' ";
			}
		}
		return query;
	}

	private boolean verificaSeUbicazionePresente(Hashtable h) {
		String codDistretto = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_STP_REPORT_DISTRETTO);
		String codPresidio = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_STP_REPORT_PCA);
				
		return (ISASUtil.valida(codDistretto) || ISASUtil.valida(codPresidio));
	}


	private String recuperaEventualiProroghe(ISASConnection dbc, String nCartella, String idSkso, String dtFinePiano)
			throws ISASMisuseException, ISASPermissionDeniedException, DBMisuseException, DBSQLException {
		String dtFineProroga = "";
//		String condWhere = "";

		RMSkSOSKSoProrogheEJB rmSkSOSKSoProrogheEJB = new RMSkSOSKSoProrogheEJB();
		dtFineProroga = rmSkSOSKSoProrogheEJB.getMaxDataProroga(dbc, nCartella, idSkso);
		
		if (ManagerDate.validaData(dtFineProroga)) {
//			condWhere = " AND b.int_data <= " + formatDate(dbc, dtFineProroga);
		} else {
			if (ManagerDate.validaData(dtFinePiano)){
				dtFineProroga = dtFinePiano;
//				condWhere = " AND b.int_data <= " + formatDate(dbc, dtFinePiano);
			}
		}
		return dtFineProroga;
//		return condWhere;
	}




//public Vector<ISASRecord> query_(myLogin mylogin,Hashtable h) throws  SQLException {
//	String punto = ver + "query ";
//	LOG.info(punto + " inizio con dati>>"+ h);
//	ISASConnection dbc = null;
//	ISASCursor dbcur = null;
//	Vector<ISASRecord> vdbr= new Vector<ISASRecord>();
//	String myselect = "";
//	// 09/02/11
//	ISASCursor dbcurConf = null;
//	try{
//		dbc=super.logIn(mylogin);
//		
//		String tipoOper = ISASUtil.getValoreStringa(h, "tipo");
//		String prDataPuac = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_PR_DATA_PUAC);
//		String prDataChiusura = ISASUtil.getValoreStringa(h, CostantiSinssntW.PR_DATA_CHIUSURA);
//		String intContatto = ISASUtil.getValoreStringa(h, "int_contatto");
//		String critTpPrest = "";
//		String critDataPrest = "";
//		String critIntContatto = "";
//		String aggiunta = ")";
//		if (ISASUtil.valida(tipoOper)){
//			if ((tipoOper != null) && (tipoOper.trim().equals("01")))
//	            aggiunta = " OR conf_key = 'TIPDEF" + tipoOper + "B')";
//			
//			String selConf = "SELECT * FROM conf"
//					+ " WHERE conf_kproc = 'SINS'"
//					+ " AND (conf_key ='TIPDEF" + tipoOper + "'" + aggiunta;
//			dbcurConf = dbc.startCursor(selConf);
//			
//			while(dbcurConf.next()){
//	            ISASRecord dbconf = dbcurConf.getRecord();
//	            if ((dbconf != null) && (dbconf.get("conf_txt") != null)) {
//	                String tpPrest = ((String)dbconf.get("conf_txt")).trim();
//	                if (critTpPrest.trim().equals(""))
//	                    critTpPrest = " AND (a.int_tipo_prest = '" + tpPrest + "'";
//	                else
//	                    critTpPrest += " OR a.int_tipo_prest = '" + tpPrest + "'";
//	            }
//	        }
//	        critTpPrest += ")";
//	        dbcurConf.close(); 
//		}
//		if (ManagerDate.validaData(prDataPuac)){
//			critDataPrest = " and a.int_data_prest >= " +formatDate(dbc, prDataPuac);
//		}
//		if (ManagerDate.validaData(prDataChiusura)){
//			critDataPrest += " and a.int_data_prest <= " +formatDate(dbc, prDataChiusura);
//		}
//		
//		if (ISASUtil.valida(intContatto)){
//			critIntContatto = " and a.int_contatto = " +intContatto;
//		}
//		myselect = recuperaQuery(h, critTpPrest, critDataPrest, critIntContatto);
//		LOG.trace(punto + "query "+ myselect);
//		dbcur=dbc.startCursor(myselect);
//		vdbr= dbcur.getAllRecord();
//	}
//    	catch(Exception e){
//    		e.printStackTrace();
//	     e.printStackTrace();
//	     throw new SQLException("Errore eseguendo una query() in IntInf ");
//    	}
//    	finally{
//    		close_dbcur_nothrow(punto, dbcurConf);
//    		logout_nothrow(punto, dbcur, dbc);
//    	}
//	return vdbr;
//    }

public Vector query_dett(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	ISASCursor dbcur = null;
	Vector vdbr= new Vector();
	String myselect = "";
	// 09/02/11
	ISASCursor dbcurConf = null;

	try{
		dbc=super.logIn(mylogin);
		// 26/01/09 m.: se accessi SOC si deve filtrare con n_progetto e NON n_contatto ---
		String nmFldCont = "int_contatto";
		if ("01".equals((String)h.get("tipo")))
			nmFldCont = "n_progetto";
		// 26/01/09 m. --------------------------------------------------------------------

		// 09/02/11 -------------------------------------------------------------------------
		String tipoOper = (String)h.get("tipo");
		String critTpPrest = "";
		
		String aggiunta = ")";
		if ((tipoOper != null) && (tipoOper.trim().equals("01")))
            aggiunta = " OR conf_key = 'TIPDEF" + tipoOper + "B')";
		
		String selConf = "SELECT * FROM conf"
				+ " WHERE conf_kproc = 'SINS'"
				+ " AND (conf_key ='TIPDEF" + tipoOper + "'" + aggiunta;
		dbcurConf = dbc.startCursor(selConf);
		
		while(dbcurConf.next()){
            ISASRecord dbconf = dbcurConf.getRecord();
            if ((dbconf != null) && (dbconf.get("conf_txt") != null)) {
                String tpPrest = ((String)dbconf.get("conf_txt")).trim();
                if (critTpPrest.trim().equals(""))
                    critTpPrest = " AND (a.int_tipo_prest = '" + tpPrest + "'";
                else
                    critTpPrest += " OR a.int_tipo_prest = '" + tpPrest + "'";
            }
        }
        critTpPrest += ")";
		dbcurConf.close(); 
		// 09/02/11 -------------------------------------------------------------------------		
		
		
		//Aggiunte le istruzioni sul tipo operatore in modo da
		//tirare fuori solamente gli interventi inseriti da un
		//opertore dello stesso gruppo
		myselect = "select a.int_data_prest data_prest, a.int_tempo tempo_prest,"
				+ " a.*," // 26/01/09: per campi ISAS
			   +" e.prest_des prest_des, c.tippre_des tipo_prest, d.cognome operatore from"
			   +" interv a, intpre b, tippre c, operatori d, prestaz e  where "
			   +" a.int_cartella = " + (String)h.get("int_cartella")//+" and a.int_cartella = b.pre_cartella and"
// 26/01/09	   +" and a.int_contatto = " + (String)h.get("int_contatto")
			   +" and a." + nmFldCont + " = " + (String)h.get("int_contatto") // 26/01/09
			   +" and a.int_anno = b.pre_anno and a.int_contatore = b.pre_contatore "
			   +" and a.int_tipo_prest = c.tippre_cod and a.int_cod_oper = d.codice "
/** 09/02/11			   
			   +" and a.int_tipo_oper='"+(String)h.get("tipo")
                           +"' and a.int_tipo_oper=d.tipo"
**/
				// 09/02/11
				+ critTpPrest		   
				+" and e.prest_cod = b.pre_cod_prest order by a.int_data_prest desc";
                System.out.println("query IntInf x riepilogo "+ myselect);
		dbcur=dbc.startCursor(myselect);
		vdbr=dbcur.getAllRecord();
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}
    	catch(Exception e){
	     e.printStackTrace();
	     throw new SQLException("Errore eseguendo una query() in IntInf ");
    	}
    	finally{
   	    if(!done){
   	        try{
				if (dbcurConf!=null)
					dbcurConf.close();
				if (dbcur!=null)
					dbcur.close();
				dbc.close();
				super.close(dbc);}
   	        catch(Exception e1){System.out.println(e1);}
	    }
    	}
    }


	public int recuperaNumeroAccessi(ISASConnection dbc, Hashtable<String, Object> prtDati) {
		int numeroAccessi = 0;
		try {
			numeroAccessi = recuperaQueryNumeroAccessi(dbc, prtDati);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numeroAccessi;

	}

	private int recuperaQueryNumeroAccessi(ISASConnection dbc, Hashtable<String, Object> h) {
		StringBuffer myselect = new StringBuffer();
		int numeroAccessi = 0;
		String punto = ver + "recuperaQueryNumeroAccessi ";
		LOG.trace(punto + " ");
		String critDataPrest = "";
		String critDataPrestSO = "";
		String critIntContatto = "";
		String tipoOperatore = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_TIPO_OPERATORE);
		String nCartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_INT_CARTELLA);
		String perDataInizio = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO);
		String perDataFine = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE);

		String intContatto = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_INT_CONTATTO);
		boolean isSegreteriaOrganizzativa = ISASUtil.getvaloreBoolean(h, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
		boolean isStatoFlussoSiad = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_ACCESSI_FLUSSI_SIAD_INVIATO);
		String dtFineProroga = "";

		if (isSegreteriaOrganizzativa) {
			dtFineProroga = perDataFine; // il calcolo viene effettuato nella parte della SO
		}

		if (ManagerDate.validaData(perDataInizio)) {
			critDataPrestSO = " AND int_data >= " + formatDate(dbc, perDataInizio);
			critDataPrest = " and int_data_prest >= " + formatDate(dbc, perDataInizio);
		}
		if (isSegreteriaOrganizzativa) {
			if (ManagerDate.validaData(dtFineProroga)) {
				critDataPrest += " and int_data_prest <= " + formatDate(dbc, dtFineProroga);
			}
		} else {
			if (ManagerDate.validaData(perDataFine)) {
				critDataPrest += " and int_data_prest <= " + formatDate(dbc, perDataFine);
			}
		}
		if (ISASUtil.valida(intContatto)) {
			critIntContatto = " and ( int_contatto = " + intContatto + " or int_contatto = 0 ) ";
		}

		myselect.append(" select count(*) as numero from interv a where a.int_cartella = " + nCartella);
		myselect.append(critIntContatto);
		myselect.append(critDataPrest);
		if (isStatoFlussoSiad) {
			myselect.append(" AND a.flag_sent in ( " + CostantiSinssntW.FLAG_DA_INVIARE_V + ", "
					+ CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO + ")");
		}

		ISASRecord dbrInterv;
		try {
			LOG.trace(punto + " query>>" + myselect);
			dbrInterv = dbc.readRecord(myselect.toString());
			numeroAccessi = ISASUtil.getValoreIntero(dbrInterv, "numero");
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(punto + " Errore nel recuperare i dati ", e);
		}

		if (isSegreteriaOrganizzativa
				&& (!ISASUtil.valida(tipoOperatore) || (ISASUtil.valida(tipoOperatore) && tipoOperatore
						.equals(GestTpOp.CTS_COD_MMG)))
						) {

			myselect = new StringBuffer();
			myselect.append(" SELECT count(*) as numero FROM intmmg  where int_cartella = " + nCartella);
			if (ManagerDate.validaData(perDataInizio)) {
				myselect.append(" AND int_data >= " + formatDate(dbc, perDataInizio));
			}
			if (isStatoFlussoSiad) {
				myselect.append(" AND flag_sent in ( " + CostantiSinssntW.FLAG_DA_INVIARE_V + ", "
						+ CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO + ")");
			}
			myselect.append(critDataPrestSO);
			if (ISASUtil.valida(dtFineProroga)) {
				myselect.append(" AND int_data <= " + formatDate(dbc, dtFineProroga));
			}
			try {
				LOG.trace(punto + " query>>" + myselect);
				dbrInterv = dbc.readRecord(myselect.toString());
				if (dbrInterv != null && ISASUtil.getValoreIntero(dbrInterv, "numero") > 0) {
					numeroAccessi += ISASUtil.getValoreIntero(dbrInterv, "numero");
				}
			} catch (Exception e) {
				e.printStackTrace();
				LOG.error(punto + " Errore nel recuperare i dati ", e);
			}
		}
		LOG.trace(punto+" numeroAccessi>>"+ numeroAccessi + " nCartella>>" + nCartella + " perDataInizio>>" +perDataInizio);

		return numeroAccessi;
	}

}

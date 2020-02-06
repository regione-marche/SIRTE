package it.caribel.app.sinssnt.bean.nuovi;

import it.caribel.app.common.ejb.PresidiEJB;
import it.caribel.app.sinssnt.controllers.lista_assistiti.ListaAssistitiGridCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelSessionManager;
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
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import org.zkoss.util.resource.Labels;

public class ListaAssistitiEJB extends SINSSNTConnectionEJB {

	private Hashtable<String, Integer> hGiorniPerAlert = null;
	private String ver = "29-";
	public static final int CTS_QUERY_PAGINAZIONE = 1;
	public static final int CTS_QUERY_ALL_DATI    = 2;
	public static final int CTS_QUERY_CONTEGGIO   = 3;

	public ListaAssistitiEJB() {
	}
	
	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h) throws Exception {
		 return query(mylogin, h, CTS_QUERY_CONTEGGIO);
	}

	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h, int tipoQuery) throws Exception {
		String nomeMetodo = "query";
		ISASConnection dbc = null;
		boolean isSo = ISASUtil.getvaloreBoolean(h, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE); 
		String codAzSan  = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);
		try {
			dbc = super.logIn(mylogin);
			boolean caricareDatiGriglia = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_LISTA_ASSISTITI_CARICA_DATI);
			if (caricareDatiGriglia) {
				String tipo_operatore = "" + h.get("tipo_operatore");
				vdbr = recuperaDatiLista(dbc, h, false, true, false, isSo, tipoQuery);
				vdbr = decodificaVectorISASRecord(dbc, vdbr, isSo, tipo_operatore, codReg, codAzSan);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(nomeMetodo + " - Exception:" + e.getMessage());
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
		return vdbr;
	}

	public Vector<ISASRecord> queryPaginate(myLogin mylogin, Hashtable h) throws SQLException {
		String nomeMetodo = ver + "queryPaginate";
		LOG.info(nomeMetodo + "inizio con dati>>" + h);
		ISASConnection dbc = null;
		boolean isSo = ISASUtil.getvaloreBoolean(h, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE); 
		String codAzSan  = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);
		try {
			dbc = super.logIn(mylogin);
			boolean caricareDatiGriglia = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_LISTA_ASSISTITI_CARICA_DATI);
			if (caricareDatiGriglia) {
				String tipo_operatore = "" + h.get("tipo_operatore");
				vdbr = recuperaDatiLista(dbc, h, false, true, true, isSo, CTS_QUERY_PAGINAZIONE);
				vdbr = decodificaVectorISASRecord(dbc, vdbr, isSo, tipo_operatore, codReg, codAzSan);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(nomeMetodo + " - Exception:" + e.getMessage());
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
		return vdbr;
	}

	public Vector<ISASRecord> recuperaDatiLista(ISASConnection dbc, Hashtable h, boolean isGroupBy, boolean noFonte,
			boolean isPaginate, boolean isSo, int tipoQuery) throws ISASMisuseException, DBMisuseException, DBSQLException,
			ISASPermissionDeniedException {
		String punto = ver + "recuperaDatiLista ";
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			String n_cartella = ISASUtil.getValoreStringa(h, CostantiSinssntW.N_CARTELLA);
			String dadata = ISASUtil.getValoreStringa(h, ListaAssistitiGridCtrl.CTS_LISTA_ASSISTITI_DADATA);
			String adata = ISASUtil.getValoreStringa(h, ListaAssistitiGridCtrl.CTS_LISTA_ASSISTITI_ADATA);
			String tipo_fonte = "" + h.get("tipo_fonte");
			String fontiDaEscludere = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_FONTI_DA_ESCLUDERE);
			String tpOrdinamentoDati = "" + h.get(CostantiSinssntW.CTS_LISTA_ATTIVITA_ORDINAMENTO);
			String codice_operatore = "";
			String zona_operatore = "";
			String distr_operatore = "";
			String codOperatoreSede = "";
			boolean rich_perso = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_L_RICHIESTE_PERSONALI_SEDE_MIA);
			boolean rich_altri = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_L_RICHIESTE_PERSONALI_SEDE_ALTRI);
			String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE);
			String codAzSan = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);
			String destinatariRichiesta = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_L_RICHIESTE_DESTINATARI);
			
			String tipo_operatore = "" + h.get("tipo_operatore");
			boolean contattiChiusi = ISASUtil.getvaloreBoolean(h, ListaAssistitiGridCtrl.CTS_LISTA_ASSISTITI_CONTATTI_CHIUSI);
			
			// aggiunto per gestire presa in carico diretta da scheda so al
			// momento di cambio container
			String id_richiesta = (h.get("id_richiesta") != null ? h.get("id_richiesta").toString() : "");
			// boolean obbligoPV = ISASUtil.getvaloreBoolean(h,
			// ManagerProfile.SO_OBB_CDI_PRIMA_VISITA);
			if (h.get("codice_operatore") != null && !h.get("codice_operatore").equals(""))
				codice_operatore = "" + h.get("codice_operatore");
			else
				codice_operatore = CaribelSessionManager.getInstance().getStringFromProfile("codice_operatore");

			if (h.get("zona_operatore") != null && !h.get("zona_operatore").equals(""))
				zona_operatore = "" + h.get("zona_operatore");
			else
				zona_operatore = CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");

			if (h.get("distr_operatore") != null && !h.get("distr_operatore").equals(""))
				distr_operatore = "" + h.get("distr_operatore");
			else
				distr_operatore = CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");
			
			codOperatoreSede = ISASUtil.getValoreStringa(h, ManagerProfile.PRES_OPERATORE);
			if (!ISASUtil.valida(codOperatoreSede)){
				if (h.get("codOperatoreSede") != null && !h.get("codOperatoreSede").equals(""))
					codOperatoreSede = "" + h.get("codOperatoreSede");
				else
					codOperatoreSede = CaribelSessionManager.getInstance().getStringFromProfile(ManagerProfile.PRES_OPERATORE);
					
			}
			
			String myselect = recuperaQuery(dbc, n_cartella, dadata, adata, tipo_fonte, tipo_operatore, codReg,
					codAzSan, isSo, contattiChiusi, codice_operatore, zona_operatore, distr_operatore, noFonte, isGroupBy, 
					destinatariRichiesta, codOperatoreSede);
			
//			if (rich_perso && !rich_altri && ISASUtil.valida(codOperatoreSede)){
//				myselect +=" AND " +CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD+ " = '" + codOperatoreSede + "' ";
//			}
			
			if (h.get("ordinamento") != null)
				myselect+= " ORDER BY " + h.get("ordinamento");
			else {
			if (isGroupBy) {
				myselect += " GROUP BY "+CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA+", " +CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+ 
						 ", " + CostantiSinssntW.CTS_FONTE;
			} else {
				
				String orderBy = "";
				if (ISASUtil.valida(tpOrdinamentoDati)) {
					if (tpOrdinamentoDati.indexOf(CostantiSinssntW.CTS_LISTA_AO_ASSISTITO) >= 0) {
						orderBy += (ISASUtil.valida(orderBy) ? ", " : "") + " cognome, nome ";
					}
					if (tpOrdinamentoDati.indexOf(CostantiSinssntW.CTS_LISTA_AO_DATA) >= 0) {
						orderBy += (ISASUtil.valida(orderBy) ? ", " : "") + CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO +"  desc ";
					}
				} else {
					// per default
					orderBy = " cognome, nome ";
				}

				if (ISASUtil.valida(orderBy)) {
					myselect += " ORDER BY " + orderBy;
				}
				}
			}
			LOG.info(punto + " - myselect: \n " + myselect);

			//if (isPaginate) {
			switch (tipoQuery) {
			case CTS_QUERY_PAGINAZIONE:
				dbcur = dbc.startCursor(myselect);
				int start = Integer.parseInt((String) h.get("start"));
				int stop = Integer.parseInt((String) h.get("stop"));
				vdbr = dbcur.paginate(start, stop);
				LOG.trace(punto + " con Paginazione>>" + (vdbr != null ? vdbr.size() + "" : "no dati "));
			//} else {
				break;
			case CTS_QUERY_CONTEGGIO:
				StringBuffer query = new StringBuffer();
				query.append("select trim(");
				query.append(CostantiSinssntW.L_AT_FONTE);
				query.append(") as ");
				query.append(CostantiSinssntW.L_AT_FONTE);
				query.append(", count(*) as ");
				query.append(CostantiSinssntW.CTS_AS_NUMERO);
				query.append(" from( ");
				query.append(myselect);
				query.append(" ) group by ");
				query.append(CostantiSinssntW.L_AT_FONTE);
				query.append(" order by ");
				query.append(CostantiSinssntW.L_AT_FONTE);
				query.append(" asc "); 
				LOG.trace(punto + " query >>" + query);;
				dbcur = dbc.startCursor(query.toString());
				vdbr = dbcur.getAllRecord();
				break;
				case CTS_QUERY_ALL_DATI:
				dbcur = dbc.startCursor(myselect.toString());
				if (dbcur!=null){
					vdbr = dbcur.getAllRecord();
				}
				LOG.debug(punto + " No Paginazione >>" + (vdbr != null ? vdbr.size() + "" : "no dati "));
			}
		} finally {
			close_dbcur_nothrow(punto, dbcur);
		}
		LOG.trace(punto + " record recuperati>>" + (vdbr != null ? vdbr.size() + "" : "no dati "));

		return vdbr;
	}

	private String recuperaQuery(ISASConnection dbc, String n_cartella, String dadata, String adata, String tipo_fonte,
			String tipo_operatore, String codReg, String codAzSan, boolean isSo, boolean contattiChiusi, String codice_operatore,
			String zona_operatore, String distr_operatore, boolean conFonte, boolean isGroupBy, String destinatariRichiesta, 
			String codOperatoreSede) {
		String punto = ver + "recuperaQuery ";
		
		String tabellaContatto    = "skinf";
		String tb_n_cartella      = "n_cartella";
		String tb_n_contatto      = "n_contatto";
		String ski_data_apertura  = "ski_data_apertura";
		String tb_ski_data_uscita = "ski_data_uscita";
		String tb_ski_dimissioni  = "ski_dimissioni_txt";
		String tb_tipoCura   	  = "ski_motivo";
		String tb_codOperatoreReferente  = "ski_infermiere";
		String tb_CodOperatore    = "ski_operatore";
		String tb_id_skso		  = "id_skso";
		
		
		
		if(ISASUtil.valida(tipo_operatore)){
			if (tipo_operatore.equals(GestTpOp.CTS_COD_MEDICO)){
				tabellaContatto    = "skmedico";
				tb_n_cartella      = "n_cartella";
				tb_n_contatto      = "n_contatto";
				ski_data_apertura  = "skm_data_apertura";
				tb_ski_data_uscita = "skm_data_chiusura";
				tb_ski_dimissioni  = "skm_motivo_chius";
				tb_tipoCura	   = "skm_motivo";
				tb_codOperatoreReferente  = "skm_medico";
				tb_CodOperatore   = "skm_medico";
				tb_id_skso		  = "id_skso";
			}else if (tipo_operatore.equals(GestTpOp.CTS_COD_INFERMIERE)){
				tabellaContatto    = "skinf";
				tb_n_cartella      = "n_cartella";
				tb_n_contatto      = "n_contatto";
				ski_data_apertura  = "ski_data_apertura";
				tb_ski_data_uscita = "ski_data_uscita";
				tb_ski_dimissioni  = "ski_dimissioni_txt";
				tb_tipoCura	  = "ski_motivo";
				tb_codOperatoreReferente  = "ski_infermiere";
//				tb_CodOperatore   = "ski_operatore";
//				TODO allineare tutti gli operatori referenti
				tb_CodOperatore   = "ski_infermiere";
				tb_id_skso		  = "id_skso";
			}else if(tipo_operatore.equals(GestTpOp.CTS_COD_FISIOTERAPISTA)){
				tabellaContatto    = "skfis";
				tb_n_cartella      = "n_cartella";
				tb_n_contatto      = "n_contatto";
				ski_data_apertura  = "skf_data";
				tb_ski_data_uscita = "skf_data_chiusura";
				tb_ski_dimissioni  = "skf_motivo_chius";
				tb_tipoCura	  = "skf_motivo";
				tb_codOperatoreReferente  = "skf_fisiot";
				tb_CodOperatore   = "skf_fisiot";
//				tb_CodOperatore   = "skf_operatore";
				tb_id_skso		  = "id_skso";
			}else {
				LOG.trace(punto + " OPERATORE GENERICO ");
				tabellaContatto    = "skfpg";
				tb_n_cartella      = "n_cartella";
				tb_n_contatto      = "n_contatto";
				ski_data_apertura  = "skfpg_data_apertura";
				tb_ski_data_uscita = "skfpg_data_uscita";
				tb_ski_dimissioni  = "skfpg_motivo_txt";
				tb_tipoCura	  = "skfpg_motivo";
				tb_codOperatoreReferente  = "skfpg_referente";
				tb_CodOperatore   = "skfpg_referente";
//				tb_CodOperatore   = "skfpg_operatore";
				tb_id_skso		  = "id_skso";
			}
			
		}
		
		String query = recuperaQueryOperatore(tabellaContatto,tb_n_cartella, tb_n_contatto, ski_data_apertura, tb_ski_data_uscita,
				 tb_ski_dimissioni, tb_tipoCura, tb_codOperatoreReferente, tb_CodOperatore, codReg, codAzSan, tb_id_skso, isSo, isGroupBy);
		String condWhere = "";

		if (contattiChiusi){
			condWhere += (ISASUtil.valida(condWhere)? " AND ": "") + " ( " + CostantiSinssntW.CTS_DATA_CONCLUSIONE + " IS NOT NULL "; 
			if(ManagerDate.validaData(adata)){
				condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO + "<= " +formatDate(dbc, adata);
							
			}
			if(ManagerDate.validaData(dadata)){
				condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") +CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO + ">= " +formatDate(dbc, dadata);
			}
			
			condWhere+= " ) ";
		}else {
			condWhere += (ISASUtil.valida(condWhere)? " AND ": "") + " ( " + CostantiSinssntW.CTS_DATA_CONCLUSIONE + " IS NULL ";
			if(ManagerDate.validaData(adata)){
				condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") 
						+ " ( " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO + "<= " +formatDate(dbc, adata) + " ) ";	
			}
			if(ManagerDate.validaData(dadata)){
				condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") +
						" ( " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO + ">= " +formatDate(dbc, dadata) + " ) ";
			}
			condWhere+= " ) ";
		}
		
		if (ISASUtil.valida(destinatariRichiesta)){
			if(destinatariRichiesta.equals(CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_OPERATORE)){
				condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + " operatore_referente "+ " = '"+codice_operatore+"' ";
			}else {
				if(ISASUtil.valida(zona_operatore)){
					condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA + " = '"+zona_operatore+"'";
				}
				if (destinatariRichiesta.equals(CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_SEDE)){
					condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+ " = '"+distr_operatore+"' ";
					condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD+ " = '"+codOperatoreSede+"' ";
				}else {
					if (destinatariRichiesta.equals(CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_DISTRETTO)){
						condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+ " = '"+distr_operatore+"' ";
					}
				}
			}
		}
		
//		if(ISASUtil.valida(distr_operatore)){
//			condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+ " = '"+distr_operatore+"' ";
//		}
		
		
		if (conFonte){
			if(ISASUtil.valida(tipo_fonte)){
				condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + CostantiSinssntW.CTS_FONTE+ " IN( " + tipo_fonte + " ) " ;
			}
		}
		
		if (!isSo){
			condWhere +=(ISASUtil.valida(condWhere)? " AND ": "") + " NOT ( TRIM( " + CostantiSinssntW.CTS_TIPOCURA+ ") = '" 
														+ CostantiSinssntW.CTS_COD_CURE_RESIDENZIALI +"' ) " ;
		}
		
		if (ISASUtil.valida(condWhere)){
			query +=" WHERE " + condWhere;
		}
		
		LOG.trace(punto + " query >>" +query);
		return query;
	}

	private String recuperaQueryOperatore(String tabellaContatto, String tb_n_cartella, String tb_n_contatto,
			String tb_dataApertura, String tb_ski_data_uscita, String tb_ski_dimissioni, String tb_tipoCura,
			String tb_codOperatoreReferente, String tb_codOperatore,String codReg, String codAzSan, 
			String tb_id_skso, boolean isSo, boolean groupBy) {
		String punto = ver +"recuperaQueryOperatore ";
		
		String query = "";
//		query+=" select cod_zona, cod_distretto, fonte,tipo_operatore ";
		query = "SELECT " + CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA+", " +CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+ 
				 ", " + CostantiSinssntW.CTS_FONTE+
				 ", cod_med_descr" + 
				 ", operatore_referente_desc" + 
				 ", " + CostantiSinssntW.CTS_TIPOCURA_DESCR+
				 ", proroga_inizio" + 
				 ", proroga_fine" + 
				 ", sospeso_inizio" + 
				 ", sospeso_fine"+
				 ", sede_descr";
		if (!groupBy){
			query +=",  " + CostantiSinssntW.CTS_L_ASSISTITO_N_CARTELLA+
					", cognome, nome, " +CostantiSinssntW.CTS_L_ASSISTITO_N_CONTATTO+
					", " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO +
					", " +CostantiSinssntW.CTS_DATA_CONCLUSIONE +
					", " +
					" motivo_conclusione, skso_presente, stato, dt_attivazione,  "+
					" tipo_operatore, fonte_dettaglio, " 
					+CostantiSinssntW.CTS_TIPOCURA +
					" ,  operatore_referente, " + CostantiSinssntW.CTS_L_ASSISTITO_ID_SKSO + ", "+
					CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_INIZIO + ", " +
					CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_FINE +", " +CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		if (isSo){
			query += " FROM ( SELECT a.cod_zona AS " +CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA+   
					", a.cod_distretto AS " +CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+
					", TRIM(NVL(a.tipocura,'" +CostantiSinssntW.CTS_L_ASSISTITO_FONTE_TIPO_CURA_NON_SPECIFICATA +"'))  AS " 
					 + CostantiSinssntW.CTS_FONTE+
					", (SELECT TRIM (mecogn) || ' ' || TRIM (menome)"+
                    " FROM medici m"+
                    " WHERE m.mecodi = g.cod_med) AS cod_med_descr,"+
                 
                 	"(SELECT TRIM (cognome) || ' ' || TRIM (nome)"+
                    " FROM operatori o"+
                    " WHERE o.codice = d.cod_case_manager) AS operatore_referente_desc,"+
                   
                 	"(SELECT TRIM (tab_descrizione)"+
                    " FROM tab_voci v"+
                    " WHERE v.tab_cod = '" +CostantiSinssntW.TAB_VAL_TIPOCURA+"' "+
                    " AND v.tab_val = a.tipocura) AS tipocura_descr,"+
                 
                 	"(SELECT dt_proroga_inizio"+
                    " FROM rm_skso_proroghe skp"+
                    " WHERE skp.n_cartella = d.n_cartella"+
                    " AND skp.id_skso = d.id_skso"+
                    " AND (dt_proroga_inizio >= SYSDATE OR dt_proroga_fine IS NULL)"+
                    ") AS proroga_inizio,"+
                   
                 	"(SELECT dt_proroga_fine"+
                    " FROM rm_skso_proroghe skp"+
                    " WHERE skp.n_cartella = d.n_cartella"+
                    " AND skp.id_skso = d.id_skso"+
                    " AND (dt_proroga_inizio >= SYSDATE OR dt_proroga_fine IS NULL)"+
                    ") AS proroga_fine,"+
                   
                 	"(SELECT dt_sospensione_inizio"+
                    " FROM rm_skso_sospensioni so"+
                    " WHERE so.n_cartella = d.n_cartella"+
                    " AND so.id_skso = d.id_skso"+
                    " AND dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null)"+
                    ") AS sospeso_inizio,"+
                   
                 	"(SELECT dt_sospensione_fine"+
                    " FROM rm_skso_sospensioni so"+
                    " WHERE so.n_cartella = d.n_cartella"+
                    " AND so.id_skso = d.id_skso"+
                    " AND dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null)"+
                    ") AS sospeso_fine"+
                    
					", (SELECT despres FROM presidi"+
					" WHERE codpres = d.cod_presidio"+ 
					" AND codreg = '" +codReg+"' AND codazsan = '" +codAzSan+"'" +
					") as sede_descr";
                    
			
		 if (!groupBy){
			 query += ", c.n_cartella AS n_cartella, c.cognome AS cognome, c.nome AS nome, " +
					 " d.id_skso AS " +CostantiSinssntW.CTS_L_ASSISTITO_N_CONTATTO+
					 ", d.pr_data_puac AS " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO+ 
					 ", d.pr_data_chiusura AS " +CostantiSinssntW.CTS_DATA_CONCLUSIONE +
					 ", d.pr_motivo_chiusura AS motivo_conclusione" +
					 ", 1 AS skso_presente, " +
					 " NULL AS stato, d.data_presa_carico_skso AS dt_attivazione, " +
					 " '---' AS tipo_operatore, NULL AS fonte_dettaglio, " +
					 " a.tipocura AS " +CostantiSinssntW.CTS_TIPOCURA +
					 ", d.cod_case_manager AS operatore_referente, " +
					 " a.id_skso AS id_skso, a.data_inizio as " +
					 CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_INIZIO + ", a.data_fine as " +
					 CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_FINE + ", d.cod_presidio as "+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		 }
			query+=" FROM rm_skso_mmg a, rm_skso d, cartella c, anagra_c g " +
				" WHERE a.n_cartella = c.n_cartella  AND a.n_cartella = c.n_cartella " +
				" AND a.n_cartella = d.n_cartella AND a.id_skso = d.id_skso "+
				" AND g.n_cartella = a.n_cartella"+
	            " AND g.data_variazione IN (SELECT MAX (x.data_variazione)"+
	                                         " FROM anagra_c x"+
	                                         " WHERE x.n_cartella = g.n_cartella)";
//				+ " AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) ";
		}else {
			query += " FROM ( SELECT " + " o.cod_zona AS " +CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA+ 
					", p.coddistr AS " +CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO + 
					", TRIM(NVL(k." +tb_tipoCura+",'" +CostantiSinssntW.CTS_L_ASSISTITO_FONTE_TIPO_CURA_NON_SPECIFICATA +"'))  AS " 
					+ CostantiSinssntW.CTS_FONTE+
					
					", (SELECT TRIM (mecogn) || ' ' || TRIM (menome)"+
                    " FROM medici m"+
                    " WHERE m.mecodi = g.cod_med) AS cod_med_descr,"+
                 
                 	"(SELECT TRIM (cognome) || ' ' || TRIM (nome)"+
                    " FROM operatori o"+
                    " WHERE o.codice = k." +tb_codOperatoreReferente + ") AS operatore_referente_desc,"+
                    
                 	"(SELECT TRIM (tab_descrizione)"+
                    " FROM tab_voci v"+
                    " WHERE v.tab_cod = '" +CostantiSinssntW.TAB_VAL_TIPOCURA+"' "+
                    " AND v.tab_val = k." +tb_tipoCura + ") AS tipocura_descr,"+
                 
                 	"(SELECT dt_proroga_inizio"+
                    " FROM rm_skso_proroghe skp"+
                    " WHERE skp.n_cartella = k.n_cartella"+
                    " AND skp.id_skso = k.id_skso"+
                    " AND (dt_proroga_inizio >= SYSDATE OR dt_proroga_fine IS NULL)"+
                    ") AS proroga_inizio,"+
                   
                 	"(SELECT dt_proroga_fine"+
                    " FROM rm_skso_proroghe skp"+
                    " WHERE skp.n_cartella = k.n_cartella"+
                    " AND skp.id_skso = k.id_skso"+
                    " AND (dt_proroga_inizio >= SYSDATE OR dt_proroga_fine IS NULL)"+
                    ") AS proroga_fine,"+
                   
                 	"(SELECT dt_sospensione_inizio"+
                    " FROM rm_skso_sospensioni so"+
                    " WHERE so.n_cartella = k.n_cartella"+
                    " AND so.id_skso = k.id_skso"+
                    " AND dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null)"+
                    ") AS sospeso_inizio,"+
                   
                 	"(SELECT dt_sospensione_fine"+
                    " FROM rm_skso_sospensioni so"+
                    " WHERE so.n_cartella = k.n_cartella"+
                    " AND so.id_skso = k.id_skso"+
                    " AND dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null)"+
                    ") AS sospeso_fine, p.despres as sede_descr";
			
			
			
			if(!groupBy){
				query+=", c.n_cartella AS " +CostantiSinssntW.CTS_L_ASSISTITO_N_CARTELLA+
						", c.cognome AS cognome, "+
						" c.nome AS nome, k." +tb_n_contatto+ " AS " + CostantiSinssntW.CTS_L_ASSISTITO_N_CONTATTO+
						", k." +tb_dataApertura+ " AS " + CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO + ", "+
						" k." +tb_ski_data_uscita+" AS data_conclusione, "+
						" k." +tb_ski_dimissioni+" AS motivo_conclusione, "+
						" NULL AS skso_presente, NULL AS stato, NULL AS dt_attivazione, "+
						" TRIM (o.tipo) AS tipo_operatore, NULL AS fonte_dettaglio, "+
						" k." +tb_tipoCura+ " AS " +CostantiSinssntW.CTS_TIPOCURA +",  "+
						" k." +tb_codOperatoreReferente +" as operatore_referente ,k." +tb_id_skso + " as " +
						CostantiSinssntW.CTS_L_ASSISTITO_ID_SKSO +
						", NULL as " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_INIZIO + ", NULL as " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_FINE
						+ ", o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
			}
				
				query+=	" FROM " +tabellaContatto+" k, operatori o, presidi p, cartella c, anagra_c g "+
					" WHERE c.n_cartella = k." +tb_n_cartella +
					" AND o.codice = k." +tb_codOperatore +
					" AND o.cod_presidio = p.codpres "+
//					" AND k." +tb_ski_motivo+" = '1' "+
					" AND p.codreg = '" +codReg+"' "+
					" AND p.codazsan = '" +codAzSan + "' "+
					" AND k." +tb_dataApertura + " IN ( "+
					" SELECT MAX (x." +tb_dataApertura+") "+
					" FROM " +tabellaContatto +" x "+
					" WHERE x." +tb_n_cartella+ " = k." +tb_n_cartella+
					" AND x." +tb_n_contatto+" = k." +tb_n_contatto +" ) " + 
					" AND k.id_skso IS NULL "+
					" AND g.n_cartella = k.n_cartella"+
		            " AND g.data_variazione IN (SELECT MAX (x.data_variazione)"+
		                                        " FROM anagra_c x"+
		                                        " WHERE x.n_cartella = g.n_cartella)"+
					" UNION SELECT " + " o.cod_zona AS " +CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA+ 
					", p.coddistr AS " +CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO+ 
					", TRIM(NVL(k." +tb_tipoCura+",'" + 
					 CostantiSinssntW.CTS_L_ASSISTITO_FONTE_TIPO_CURA_NON_SPECIFICATA+"')) AS " + CostantiSinssntW.CTS_FONTE+
				
					", (SELECT TRIM (mecogn) || ' ' || TRIM (menome)"+
	                "   FROM medici m"+
	                "   WHERE m.mecodi = g.cod_med) AS cod_med_descr,"+
	                 
	                " (SELECT TRIM (cognome) || ' ' || TRIM (nome)"+
	                "    FROM operatori o"+
	                "  WHERE o.codice = k." +tb_codOperatoreReferente + ") AS operatore_referente_desc,"+
	                   
	                " (SELECT TRIM (tab_descrizione)"+
	                "    FROM tab_voci v"+
	                "   WHERE v.tab_cod = '" +CostantiSinssntW.TAB_VAL_TIPOCURA+"' "+
	                "   AND v.tab_val = k." +tb_tipoCura + ") AS tipocura_descr,"+
	                 
	                " (SELECT dt_proroga_inizio"+
	                "    FROM rm_skso_proroghe skp"+
	                "   WHERE skp.n_cartella = k.n_cartella"+
	                "   AND skp.id_skso = k.id_skso"+
	                "   AND (dt_proroga_inizio >= SYSDATE OR dt_proroga_fine IS NULL)"+
	                "   ) AS proroga_inizio,"+
	                   
	                " (SELECT dt_proroga_fine"+
	                "    FROM rm_skso_proroghe skp"+
	                "   WHERE skp.n_cartella = k.n_cartella"+
	                "   AND skp.id_skso = k.id_skso"+
	                " AND (dt_proroga_inizio >= SYSDATE OR dt_proroga_fine IS NULL)"+
	                "   ) AS proroga_fine,"+
	                   
	                " (SELECT dt_sospensione_inizio"+
	                "    FROM rm_skso_sospensioni so"+
	                "   WHERE so.n_cartella = k.n_cartella"+
	                "   AND so.id_skso = k.id_skso"+
	                " AND dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null)"+
	                "   ) AS sospeso_inizio,"+
	                   
	                "  (SELECT dt_sospensione_fine"+
	                "    FROM rm_skso_sospensioni so"+
	                "   WHERE so.n_cartella = k.n_cartella"+
	                "   AND so.id_skso = k.id_skso"+
	                " AND dt_sospensione_inizio  <= sysdate and (dt_sospensione_fine >= sysdate or dt_sospensione_fine is null)"+
	                "   ) AS sospeso_fine, p.despres as sede_descr";
				
				if (!groupBy){
					query+=", c.n_cartella AS " +CostantiSinssntW.CTS_L_ASSISTITO_N_CARTELLA+
						", c.cognome AS cognome, "+
						" c.nome AS nome, k." +tb_n_contatto +" AS " +CostantiSinssntW.CTS_L_ASSISTITO_N_CONTATTO+
						", k." +tb_dataApertura + " AS " + CostantiSinssntW.CTS_L_ASSISTITO_DATA_INIZIO +", "+
						" k." +tb_ski_data_uscita + " AS data_conclusione, "+
						" k." +tb_ski_dimissioni + " AS motivo_conclusione, "+
						" NULL AS skso_presente, NULL AS stato, m.data_presa_carico_skso AS dt_attivazione, "+
						" TRIM (o.tipo) AS tipo_operatore, NULL AS fonte_dettaglio, "+
						" k." + tb_tipoCura + " AS " +CostantiSinssntW.CTS_TIPOCURA +",  "+
						" k." +tb_codOperatoreReferente + " as operatore_referente  ,k." +tb_id_skso + " as " +
						CostantiSinssntW.CTS_L_ASSISTITO_ID_SKSO +
						", a.data_inizio as " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_INIZIO + 
						", a.data_fine as " +CostantiSinssntW.CTS_L_ASSISTITO_DATA_PIANO_FINE 
						+ ", o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD ;
				}
				
				query+=" FROM " +tabellaContatto+ " k, operatori o, presidi p, cartella c, rm_skso m, rm_skso_mmg a, anagra_c g "+
					" WHERE c.n_cartella = k." +tb_n_cartella +
					" AND o.codice = k."+tb_codOperatore+
					" AND m.n_cartella = k." +tb_n_cartella +
					" AND m.id_skso = k.id_skso AND m.n_cartella = a.n_cartella AND m.id_skso = a.id_skso "+
					" AND o.cod_presidio = p.codpres "+
//					" AND ( k." +tb_ski_motivo + " = '1' OR k." +tb_ski_motivo + " = '2' ) "+
					" AND p.codreg = '" +codReg + "' "+
					" AND p.codazsan = '" +codAzSan +"' "+
					" AND k." +tb_dataApertura+" IN ( " +
					" SELECT MAX (x." +tb_dataApertura+ ") "+    
					" FROM " +tabellaContatto+ " x "+
					" WHERE x." +tb_n_cartella +" = k." +tb_n_cartella +
					" AND x." +tb_n_contatto+" = k." +tb_n_contatto + " ) "+
					" AND g.n_cartella = k.n_cartella"+
		            " AND g.data_variazione IN (SELECT MAX (x.data_variazione)"+
		                                       "  FROM anagra_c x"+
		                                       " WHERE x.n_cartella = g.n_cartella)"; 
		}
		query+=" ) ";
		LOG.trace(punto + " query>" +query);
		return query;
	}
	private ISASRecord decodificaISASRecord(ISASConnection dbc, ISASRecord dbr, boolean aggiungiDettagliScheda,
			Hashtable<String, String> tipoCuraDescr, Hashtable<String, String> tipoOperatoreDescrizione, boolean isSo, 
			String tipoOperatore, String codReg, String codAzSan)
			throws Exception {
		String nomeMetodo = "decodificaISASRecord";
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		String operatoreReferente = "";
		try {
			if (dbr != null) {
				operatoreReferente = ISASUtil.getValoreStringa(dbr, "operatore_referente");
				// decodifica medico MMG
				String query_med = "SELECT ac.cod_med FROM anagra_c ac WHERE ac.n_cartella ="+ dbr.get("n_cartella")+
									" AND data_variazione IN (SELECT MAX("+
									"c.data_variazione) FROM anagra_c c"+
									" WHERE c.n_cartella=ac.n_cartella)";
				ISASRecord dbr_med = dbc.readRecord(query_med);
				if (dbr_med!=null){
					if (dbr_med.get("cod_med")!=null && !dbr_med.get("cod_med").equals("")){
						String cod_med_descr = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + dbr_med.get("cod_med"),
								"nvl(trim(mecogn),'') ||' '  ||nvl(trim(menome),'')", "cod_med_descr");
						dbr.put("cod_med_descr", cod_med_descr);
					}else dbr.put("cod_med_descr", "");
				}else dbr.put("cod_med_descr", "");
				/*String cod_med_descr = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + dbr.get("cod_med"),
						"nvl(trim(mecogn),'') ||' '  ||nvl(trim(menome),'')", "cod_med_descr");
				if (cod_med_descr == null || cod_med_descr.equals("")) {
					cod_med_descr = "";
					if (dbr.get("pr_mmg_altro") != null) {
						cod_med_descr = (String) dbr.get("pr_mmg_altro");
					}
				}
				dbr.put("cod_med_descr", cod_med_descr);*/
				// decodifica operatore
				dbr.put("operatore_referente_desc", ISASUtil.getDecode(dbc, "operatori", "codice",
						"" + operatoreReferente, "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",
						"cod_operatore_descr"));
				// decodifica distretto
				String distretti = ISASUtil.getDecode(dbc, "distretti", "cod_distr", "" + dbr.get("cod_distretto"),
						"des_distr");
				// dbr.put("cod_distretti_descr",ISASUtil.getDecode(dbc,"distretti","cod_distr",""+dbr.get("cod_distretto"),"cod_distretti_descr"));
				dbr.put("cod_distretti_descr", distretti);
				// decodifica zona
				String zona = ISASUtil.getDecode(dbc, "zone", "codice_zona", "" + dbr.get("cod_zona"),
						"descrizione_zona");
				// dbr.put("cod_zona_descr",ISASUtil.getDecode(dbc,"zone","codice_zona",""+dbr.get("cod_zona"),"cod_zona_descr"));
				dbr.put("cod_zona_descr", zona);

				// Calcolo giorni passati per mostrare alert
//				addInfoCalcoloGGPerAlert(dbc, dbr);

				/* mostrare il tipo di cura */
				String tipoCura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_TIPOCURA);
				String tipoCuraDescrizione = ISASUtil.getValoreStringa(tipoCuraDescr, tipoCura);
				dbr.put(CostantiSinssntW.CTS_TIPOCURA_DESCR, tipoCuraDescrizione);
				
				String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
				String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
				String msgStatoScheda= Labels.getLabel(CostantiSinssntW.CTS_SKSO_STATO_NON_PRESENTE);
				if (ISASUtil.valida(idSkso) ){
					rmSkSOEJB.recuperaInfoScheda(dbc, dbr);
					if (dbr.getHashtable().containsKey(CostantiSinssntW.CTS_SKSO_STATO)){
						String statoScheda = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SKSO_STATO);
						msgStatoScheda = Labels.getLabel(statoScheda);
					}
				}
				dbr.put("stato_descr", msgStatoScheda);
				
				recuperaPeriodo(dbc, nCartella, isSo, dbr, tipoOperatore);
				if (isSo && ISASUtil.valida(idSkso)){
					verificaProroghe(dbc, nCartella, idSkso, dbr);
					verificaSospensione(dbc, nCartella, idSkso, dbr);
				}
				String codSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD);
				if (ISASUtil.valida(codSede)){
					dbr.put(CostantiSinssntW.CTS_DESCRIZIONE_SEDE,recuperaSede(dbc, codSede,codReg, codAzSan));
				}
				
			}
			// LOG.info(nomeMetodo+" -  Metodo eseguito INPUT[dbr,"+aggiungiDettagliScheda+"]");
			return dbr;
		} catch (Exception e) {
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		}
	}

	private Object recuperaSede(ISASConnection dbc, String codSede, String codReg, String codAzSan) {
		PresidiEJB presidio = new PresidiEJB();
		String descrizionePresidio = presidio.recuperaDescrizionePresidio(dbc, codSede, codReg, codAzSan);
		
		return descrizionePresidio;
	}

	private void verificaSospensione(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbr) {
		String punto = ver + "verificaSospensione ";
		if (ISASUtil.valida(idSkso)){
			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
			String query = rmSkSOEJB.recuperaQuerySospensione(nCartella, idSkso);
			try {
				ISASRecord dbrSospensioni = dbc.readRecord(query);
				String dtSospensioneInizio = ISASUtil.getValoreStringa(dbrSospensioni, "dt_sospensione_inizio");
				String dtSospensioneFine = ISASUtil.getValoreStringa(dbrSospensioni, "dt_sospensione_fine");
				dbr.put("sospeso_inizio", ManagerDate.formattaDataIta(dtSospensioneInizio, "/"));
				dbr.put("sospeso_fine", ManagerDate.formattaDataIta(dtSospensioneFine, "/"));
			} catch (Exception e) {
				LOG.error(punto + " Errore nel recuperare i dati>>" + query + "\n", e);
			}
		}else {
			LOG.trace(punto + "Non effettuo il salvataggio dei dati ");
		}
	}

	private void recuperaPeriodo(ISASConnection dbc, String nCartella,boolean isSo, ISASRecord dbr, String tipoOperatore) {
		String punto = ver + "recuperaPeriodo ";
		String query = "";
		if (!isSo){
			query +=" SELECT p.pa_data, p.pa_data_chiusura, p.pa_operatore, p.* FROM piano_assist p WHERE p.n_cartella = " +
					nCartella + " AND p.pa_tipo_oper = '" +tipoOperatore+ "' AND pa_data IN (SELECT max(pa_data) " +
							" FROM piano_assist x WHERE p.n_cartella = x.n_cartella) ";
			ISASRecord dbrPianoAssist;
			try {
				dbrPianoAssist = dbc.readRecord(query);
				if(dbrPianoAssist !=null){
					String dtPaData = ISASUtil.getValoreStringa(dbrPianoAssist, "pa_data");
					String dtPaDataChiusura = ISASUtil.getValoreStringa(dbrPianoAssist, "pa_data_chiusura");
					dbr.put("data_piano_inizio", ManagerDate.formattaDataIta(dtPaData, "/"));
					dbr.put("data_piano_fine", ManagerDate.formattaDataIta(dtPaDataChiusura, "/"));
				}
			} catch (Exception e) {
				LOG.error(punto + " Errore nel recuperare i dati sul piano assist >>"+query+"< \n",e);
			}
		}
	}

	private void verificaProroghe(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbr) {
		String punto = ver + "verificaProroghe ";
		if (ISASUtil.valida(idSkso)) {
			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
			String query = rmSkSOEJB.recuperaQueryProroghe(nCartella, idSkso);
			try {
				ISASRecord dbrProroghe = dbc.readRecord(query);
				String dtProrogaInizio = ISASUtil.getValoreStringa(dbrProroghe, "dt_proroga_inizio");
				String dtProrogaFine = ISASUtil.getValoreStringa(dbrProroghe, "dt_proroga_fine"); 
				dbr.put("proroga_inizio", ManagerDate.formattaDataIta(dtProrogaInizio, "/"));
				dbr.put("proroga_fine", ManagerDate.formattaDataIta(dtProrogaFine, "/"));
			} catch (Exception e) {
				LOG.error(punto + " Errore nel recuperare i dati>>" + query + "\n", e);
			}
		}else {
			LOG.trace(punto + "Non effettuo il salvataggio dei dati ");
		}
	}

	private Vector<ISASRecord> decodificaVectorISASRecord(ISASConnection dbc, Vector<ISASRecord> vdbr, boolean isSo, String tipoOperatore, String codReg, String codAzSan) throws Exception {
		String nomeMetodo = this.getClass().getName() + ".decodificaVectorISASRecord";
		try {
			int elementi = (vdbr != null ? vdbr.size() : 0);
			Hashtable<String, String> descrizioneTipoOperatore = ManagerOperatore.loadTipiOperatori();
			Hashtable<String, String> tipoCuraDescrizione = ManagerDecod
					.caricaDaTabVoci(dbc, CostantiSinssntW.TAB_VAL_TIPOCURA);
			for (int i = 0; i < vdbr.size(); i++) {
				LOG.trace(nomeMetodo + "\n Esamino>" + i + "/" + elementi);
				Object obj = vdbr.get(i);
				if (obj instanceof ISASRecord) {
					ISASRecord dbr = (ISASRecord) vdbr.get(i);
					dbr = (ISASRecord) vdbr.elementAt(i);
					dbr = decodificaISASRecord(dbc, dbr, false, tipoCuraDescrizione, descrizioneTipoOperatore, 
							isSo,tipoOperatore, codReg, codAzSan);
				}
			}
			LOG.info(nomeMetodo + " -  Metodo eseguito INPUT[" + vdbr.size() + "] OUTPUT[" + vdbr.size() + "]");
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		}
	}

	private void addInfoCalcoloGGPerAlert(ISASConnection dbc, ISASRecord dbr) {
		String nomeMetodo = this.getClass().getName() + ".addInfoCalcoloGGPerAlert()";
		try {
			java.sql.Date data_richiesta = (java.sql.Date) dbr.get("data_richiesta");
			String fonte = ((Integer) dbr.get("fonte")).toString();
			DataWI dtRichiesta = new DataWI(data_richiesta);
			DataWI dtOdierna = new DataWI();
			// if (dtRichiesta.getString(1)!=null){
			// int numGGpassati =
			// dtOdierna.contaGgTra(dtRichiesta.getString(1));
			// int num_gg_alert1 = recuperaConfNumGGAlert(dbc,
			// fonte,ListaAttivitaGridCtrl.LIVELLO_ALERT1);
			// int num_gg_alert2 = recuperaConfNumGGAlert(dbc,
			// fonte,ListaAttivitaGridCtrl.LIVELLO_ALERT2);
			// if(num_gg_alert1>0 && numGGpassati>num_gg_alert1)
			// dbr.put(ListaAttivitaGridCtrl.LIVELLO_ALERT,
			// ListaAttivitaGridCtrl.LIVELLO_ALERT1);
			// if(num_gg_alert2>0 && numGGpassati>num_gg_alert2)
			// dbr.put(ListaAttivitaGridCtrl.LIVELLO_ALERT,
			// ListaAttivitaGridCtrl.LIVELLO_ALERT2);
			// }
		} catch (Exception e) {
			LOG.error(nomeMetodo + ": " + e.getMessage(), e);
		}
	}

	public int getConfNumGGAlert(myLogin mylogin, String fonte, String livello_alert) {
		String nomeMetodo = this.getClass().getName() + ".getConfNumGGAlert";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			return recuperaConfNumGGAlert(dbc, fonte, livello_alert);
		} catch (Exception e) {
			LOG.error(nomeMetodo + ": reperimento RM_CONF_ALERT non riuscito o mal configurato" + e.getMessage(), e);
			return 0;
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public int recuperaConfNumGGAlert(ISASConnection dbc, String fonte, String livello_alert) {
		String nomeMetodo = " recuperaConfNumGGAlert ";
		ISASCursor dbcur = null;
		try {
			if (this.hGiorniPerAlert == null) {
				this.hGiorniPerAlert = new Hashtable<String, Integer>();
				String myselect = "SELECT * from RM_CONF_ALERT";
				dbcur = dbc.startCursor(myselect);
				Vector<ISASRecord> vdbr = dbcur.getAllRecord();
				String chiaveCorr = "";
				ISASRecord dbrCorr = null;
				for (int i = 0; i < vdbr.size(); i++) {
					dbrCorr = vdbr.get(i);
					// chiaveCorr =
					// (String)dbrCorr.get("tipo_fonte")+"_"+ListaAttivitaGridCtrl.LIVELLO_ALERT1;
					// this.hGiorniPerAlert.put(chiaveCorr,
					// (Integer)dbrCorr.get("num_gg_alert_liv1"));
					// chiaveCorr =
					// (String)dbrCorr.get("tipo_fonte")+"_"+ListaAttivitaGridCtrl.LIVELLO_ALERT2;
					// this.hGiorniPerAlert.put(chiaveCorr,
					// (Integer)dbrCorr.get("num_gg_alert_liv2"));
				}
			}
			String chiave = fonte + "_" + livello_alert;
			if (this.hGiorniPerAlert.get(chiave) != null)
				return ((Integer) this.hGiorniPerAlert.get(chiave)).intValue();
			else
				return 0;
		} catch (Exception e) {
			LOG.error(nomeMetodo + ": reperimento RM_CONF_ALERT non riuscito o mal configurato" + e.getMessage(), e);
			return 0;
		} finally {
			close_dbcur_nothrow(nomeMetodo, dbcur);
		}
	}

	public void concludiRichiestaRM_RICH_MMG(myLogin mylogin, String n_cartella, String id_rich) throws Exception {
		String nomeMetodo = this.getClass().getName() + ".concludiRichiestaRM_RICH_MMG";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "select * from rm_rich_mmg where n_cartella=" + n_cartella + " and id_rich = " + id_rich;
			ISASRecord dbr = dbc.readRecord(myselect);
			dbr.put("stato", new Integer(3));
			dbc.writeRecord(dbr);
		} catch (Exception e) {
			LOG.error(nomeMetodo + " - Exception: " + e.getMessage(), e);
			throw e;
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public void concludiRichiestaRM_SKSO(myLogin mylogin, String n_cartella, String id_rich) throws Exception {
		String nomeMetodo = this.getClass().getName() + ".concludiRichiestaRM_SKSO";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM rm_skso WHERE n_cartella = " + n_cartella + " and id_skso = " + id_rich;
			ISASRecord dbr = dbc.readRecord(myselect);
			dbr.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, new Integer(CostantiSinssntW.CTS_FLAG_STATO_RIMOSSA));
			dbc.writeRecord(dbr);
		} catch (Exception e) {
			LOG.error(nomeMetodo + " - Exception: " + e.getMessage(), e);
			throw e;
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public void concludiRichiestaRM_SKSO_OP_COINVOLTI(myLogin mylogin, String n_cartella, String id_rich,
			String tipo_operatore) throws Exception {
		String nomeMetodo = this.getClass().getName() + ".concludiRichiestaRM_SKSO_OP_COINVOLTI";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM rm_skso_op_coinvolti WHERE n_cartella = " + n_cartella + " and id_skso = "
					+ id_rich + " and tipo_operatore = '" + tipo_operatore + "'";
			ISASRecord dbr = dbc.readRecord(myselect);
			dbr.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, new Integer(CostantiSinssntW.CTS_FLAG_STATO_RIMOSSA));
			dbc.writeRecord(dbr);
		} catch (Exception e) {
			LOG.error(nomeMetodo + " - Exception: " + e.getMessage(), e);
			throw e;
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public void annullaRichiestaChiusura(myLogin mylogin, String n_cartella, String id_rich, String fonte_dettaglio)
			throws Exception {
		String nomeMetodo = this.getClass().getName() + ".concludiRichiestaRM_SKSO_OP_COINVOLTI";
		ISASConnection dbc = null;
		ISASCursor dbcur = null;
		try {
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM richieste_chiusura WHERE n_cartella = " + n_cartella + " and id_skso = "
					+ id_rich + " and esito_richiesta = " + fonte_dettaglio;
			dbcur = dbc.startCursor(myselect);
			if (dbcur != null && dbcur.getDimension() > 0) {
				while (dbcur.next()) {
					ISASRecord dbr = dbcur.getRecord();

					String sql = "SELECT * FROM richieste_chiusura WHERE n_cartella = " + n_cartella
							+ " and id_skso = " + id_rich + " and data_richiesta = "
							+ dbc.formatDbDate(dbr.get("data_richiesta").toString()) + " and cod_zona_richiedente = "
							+ dbr.get("cod_zona_richiedente").toString();
					ISASRecord dbrw = dbc.readRecord(sql);
					if (fonte_dettaglio.equals(CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA)) {
						dbrw.put("esito_richiesta", CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_ANNULLATA);
						dbrw.put("cod_operatore_chiusura", dbc.getKuser());
						dbrw.put("data_chiusura", new java.sql.Date(Calendar.getInstance().getTimeInMillis()));
						dbc.writeRecord(dbrw);
					}
					// la richiesta è già stata elaborata e quindi va rimossa
					else
						dbc.deleteRecord(dbrw);
				}
				dbcur.close();
			}
		} catch (Exception e) {
			LOG.error(nomeMetodo + " - Exception: " + e.getMessage(), e);
			throw e;
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public Vector<ISASRecord> filtriRicercaFonte(myLogin myLogin, Hashtable h) throws Exception {
		String nomeMetodo = ver + "filtriRicercaFonte";
		ISASConnection dbc = null;
		Vector<ISASRecord> vdbr = null;
		boolean isSo = ISASUtil.getvaloreBoolean(h, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
		try {
			dbc = super.logIn(myLogin);
			vdbr = recuperaDatiLista(dbc, h, false, false, false,isSo,CTS_QUERY_CONTEGGIO);
//			Hashtable<String, Integer> datiFonte = contaDatiFonte(vdbr);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(nomeMetodo + " - Exception:" + e.getMessage());
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
		return vdbr;
	}

	private Hashtable<String, Integer> contaDatiFonte(Vector vdbr) {
		String punto = ver + "contaDatiFonte ";
		int numero;
		String fonte;
		Hashtable<String, Integer> datiFonte = new Hashtable<String, Integer>();
		for (int i = 0; i < vdbr.size(); i++) {
			ISASRecord dbrFonte = (ISASRecord) vdbr.get(i);
			fonte = ISASUtil.getValoreStringa(dbrFonte,CostantiSinssntW.CTS_FONTE);
			numero = ISASUtil.getValoreIntero(datiFonte, fonte);
			if (numero < 0) {
				numero = 0;
			}
			numero++;
			datiFonte.put(fonte, numero);
		}
		LOG.trace(punto + " dati recuperati >>" + datiFonte);
		return datiFonte;
	}

	
}

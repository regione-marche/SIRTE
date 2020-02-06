package it.caribel.app.sinssnt.bean.nuovi;

import it.caribel.app.common.ejb.PresidiEJB;
import it.caribel.app.sins_pht.bean.Pht2TabBaseEJB;
import it.caribel.app.sins_pht.util.CostantiPHT;
import it.caribel.app.sinssnt.controllers.lista_attivita.AttribuzioneDistrettoPht2Ctrl;
import it.caribel.app.sinssnt.controllers.lista_attivita.ListaAttivitaGridCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASConnection;
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
import java.util.StringTokenizer;
import java.util.Vector;

public class ListaAttivitaEJB extends SINSSNTConnectionEJB {

	private Hashtable<String, Integer> hGiorniPerAlert = null;
	private String ver = "60-";
	public static final int CTS_QUERY_PAGINAZIONE = 1;
	public static final int CTS_QUERY_ALL_DATI    = 2;
	public static final int CTS_QUERY_CONTEGGIO   = 3;
	
	private Hashtable<String, String> pht2DoMotivoDimissione = null;
	private String CTS_PHT2_DISTRETTO = "distretto";
	private String CTS_PHT2_OPERATORE = "operatore";
	private String CTS_PHT2_PRECEDENTE_DISTRETTO = "prec_distretto";
	private String CTS_PHT2_PRECEDENTE_OPERATORE = "prec_operatore";
	
	
	public ListaAttivitaEJB() {
	}

	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h) throws Exception {
		 return query(mylogin, h, CTS_QUERY_CONTEGGIO);
	}
	
	
	
	public Vector<ISASRecord> query(myLogin mylogin, Hashtable h, int tipoQuery) throws Exception {
		String nomeMetodo = "query";
		ISASConnection dbc = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			dbc = super.logIn(mylogin);
			boolean caricareDatiGriglia = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_LISTA_ATTIVITA_CARICA_DATI);
			String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE);
			String codAzSan = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);

			if (caricareDatiGriglia) {
//				vdbr = recuperaDatiLista(dbc, h, false, true, false, codReg, codAzSan);
				vdbr = recuperaDatiLista(dbc, h, false, true, tipoQuery, codReg, codAzSan);
				vdbr = decodificaVectorISASRecord(dbc, vdbr, codReg, codAzSan);
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
	
	
	public Vector<ISASRecord> recuperaDatiLista(ISASConnection dbc, Hashtable h) throws Exception {
		String nomeMetodo = ver +"recuperaDatiLista";
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE);
			String codAzSan = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);
			vdbr = recuperaDatiLista(dbc, h, false, true, CTS_QUERY_ALL_DATI, codReg, codAzSan);
			vdbr = decodificaVectorISASRecord(dbc, vdbr, codReg, codAzSan);
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(nomeMetodo + " - Exception:" + e.getMessage());
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		}
		return vdbr;
	}
	
	
	
	public Vector<ISASRecord> queryPaginate(myLogin mylogin, Hashtable h) throws SQLException {
		String nomeMetodo = ver + "queryPaginate";
		LOG.info(nomeMetodo + "inizio con dati>>" + h);
		ISASConnection dbc = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			dbc = super.logIn(mylogin);
			boolean caricareDatiGriglia = ISASUtil.getvaloreBoolean(h, CostantiSinssntW.CTS_LISTA_ATTIVITA_CARICA_DATI);
			String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE);
			String codAzSan = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);
			if (caricareDatiGriglia) {
				vdbr = recuperaDatiLista(dbc, h, false, true, CTS_QUERY_PAGINAZIONE, codReg, codAzSan);
				vdbr = decodificaVectorISASRecord(dbc, vdbr, codReg, codAzSan);
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

	private Vector<ISASRecord> recuperaDatiLista(ISASConnection dbc, Hashtable h, boolean isGroupBy, boolean noFonte,
			int tipoRichiesta, String codReg, String codAzSan) throws ISASMisuseException, DBMisuseException,
			DBSQLException, ISASPermissionDeniedException {
		String punto = ver + "recuperaDatiLista ";
		ISASCursor dbcur = null;
		Vector<ISASRecord> vdbr = new Vector<ISASRecord>();
		try {
			String n_cartella = "" + h.get("n_cartella");
			String dadata = "" + h.get("dadata");
			String adata = "" + h.get("adata");
			String tipo_fonte = "" + h.get("tipo_fonte");
			String tipo_operatore = "" + h.get("tipo_operatore");
			int ggVistaSegnalazioneOp = ISASUtil.getValoreIntero(h, ManagerProfile.GG_VISTA_SEGNALAZIONE_OPERATORE);
			String fontiDaEscludere = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_FONTI_DA_ESCLUDERE);
			int ggValutazioneBisogni = ISASUtil.getValoreIntero(h, ManagerProfile.GG_VALUTAZIONE_BISOGNI);
			String rich_perso = "" + h.get("rich_perso");
			String rich_altri = "" + h.get("rich_altri");
			String tpOrdinamentoDati = "" + h.get(CostantiSinssntW.CTS_LISTA_ATTIVITA_ORDINAMENTO);
			String codice_operatore = "";
			String codOperatoreSede = "";
			String zona_operatore = "";
			String distr_operatore = "";
			LinkedList<String> listaFonte = recuperaElementiFontiLista(h);
			int ggScadenzaRicovero = ISASUtil.getValoreIntero(h, ManagerProfile.GG_SCADENZA_RICOVERI_RSA);

			int ggPregressoPrestazioni = ISASUtil.getValoreIntero(h, ManagerProfile.GG_PREGRESSO_PRESTAZIONI);
			boolean isSo = ISASUtil.getvaloreBoolean(h, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);
			String destinatariRichiesta = ISASUtil.getValoreStringa(h, CostantiSinssntW.CTS_L_RICHIESTE_DESTINATARI);

			if (noFonte && listaFonte.contains(CostantiSinssntW.CTS_VALORE_DEFAULT_NESSUNO)) {
				LOG.error(punto + " dati da esaminare ");
			} else {
				// aggiunto per gestire presa in carico diretta da scheda so al momento di cambio container
				String id_richiesta = (h.get("id_richiesta") != null ? h.get("id_richiesta").toString() : "");

				boolean obbligoPV = ISASUtil.getvaloreBoolean(h, ManagerProfile.SO_OBB_CDI_PRIMA_VISITA);

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
				if (!ISASUtil.valida(codOperatoreSede)) {
					codOperatoreSede = CaribelSessionManager.getInstance().getStringFromProfile(
							ManagerProfile.PRES_OPERATORE);
				}

				StringBuffer myselect = new StringBuffer();
				myselect.append(recuperaQuery(dbc, n_cartella, dadata, adata, tipo_fonte, tipo_operatore,
						ggVistaSegnalazioneOp, ggValutazioneBisogni, rich_perso, rich_altri, codice_operatore,
						zona_operatore, distr_operatore, id_richiesta, obbligoPV, isGroupBy, noFonte,
						ggPregressoPrestazioni, codReg, codAzSan, isSo, fontiDaEscludere, codOperatoreSede, listaFonte,
						destinatariRichiesta, ggScadenzaRicovero));

				if (isGroupBy) {
					myselect.append(" group by cod_zona, cod_distretto, fonte, tipo_operatore ");
				} else {
					String orderBy = "";
					if (ISASUtil.valida(tpOrdinamentoDati)) {
						if (tpOrdinamentoDati.indexOf(CostantiSinssntW.CTS_LISTA_AO_FONTE) >= 0) {
							orderBy += (ISASUtil.valida(orderBy) ? ", " : "") + " fonte  ";
						}
						if (tpOrdinamentoDati.indexOf(CostantiSinssntW.CTS_LISTA_AO_ASSISTITO) >= 0) {
							orderBy += (ISASUtil.valida(orderBy) ? ", " : "") + " cognome, nome ";
						}
						if (tpOrdinamentoDati.indexOf(CostantiSinssntW.CTS_LISTA_AO_DATA) >= 0) {
							orderBy += (ISASUtil.valida(orderBy) ? ", " : "") + " data_richiesta desc ";
						}
					} else {
						//	per default 
						orderBy = " cognome, nome ";
					}

					if (ISASUtil.valida(orderBy)) {
						myselect.append(" ORDER BY " + orderBy);
					}
				}
				LOG.debug(punto + " - myselect: \n " + myselect);
				switch (tipoRichiesta) {
				case CTS_QUERY_PAGINAZIONE:
					LOG.trace(punto + " query>>" + myselect);
					dbcur = dbc.startCursor(myselect.toString());
					int start = Integer.parseInt((String) h.get("start"));
					int stop = Integer.parseInt((String) h.get("stop"));
					vdbr = dbcur.paginate(start, stop);
					LOG.trace(punto + " con Paginazione>>" + (vdbr != null ? vdbr.size() + "" : "no dati "));
					break;
				case CTS_QUERY_CONTEGGIO:
					LOG.trace(punto + " inserisco ordinamento e conteggio delle fonti");
					StringBuffer query = new StringBuffer();
					query.append("select ");
					query.append(CostantiSinssntW.L_AT_FONTE);
					query.append(", count(*) as ");
					query.append(CostantiSinssntW.CTS_AS_NUMERO);
					query.append(" from ( ");
					query.append(myselect);
					query.append(" ) group by ");
					query.append(CostantiSinssntW.L_AT_FONTE);
					query.append(" order by ");
					query.append(CostantiSinssntW.L_AT_FONTE);
					query.append(" asc ");
					LOG.trace(punto + " query>>" + query);
					dbcur = dbc.startCursor(query.toString());
					vdbr = dbcur.getAllRecord();
					LOG.debug(punto + " No conteggio >>" + (vdbr != null ? vdbr.size() + "" : "no dati "));
					break;
				case CTS_QUERY_ALL_DATI:
					dbcur = dbc.startCursor(myselect.toString());
					if (dbcur!=null){
						vdbr = dbcur.getAllRecord();
					}
					LOG.trace(punto + " con all Dati >>" + (vdbr != null ? vdbr.size() + "" : "no dati "));
					break;
				default:
					break;
				}
			}
		} finally {
			close_dbcur_nothrow(punto, dbcur);
		}
		LOG.trace(punto + " record recuperati>>" + (vdbr != null ? vdbr.size() + "" : "no dati "));

		return vdbr;
	}

	/* recupero la lista nel caso della stampa: viene passata una String e non l'oggetto LinkedList */
	private LinkedList<String> recuperaElementiFontiLista(Hashtable h) {
		String punto = ver +"recuperaElementiFontiLista ";
		LinkedList<String> listaFonte = new LinkedList<String>();

		Object obj = h.get(CostantiSinssntW.CTS_ELEMENTI_FONTI_LISTA);
		if (obj != null) {
			if (obj instanceof String && ISASUtil.valida((String)obj)) {
				String vLista = (String) obj;
				int inizio = vLista.indexOf("[");
				int fine = vLista.indexOf("]");
				if (inizio<0){
					inizio = 0;
				}else {
					inizio++;
				}
				if (fine<0){
					fine = vLista.length();
				}
				vLista = vLista.substring(inizio, fine);
				StringTokenizer tk = new StringTokenizer(vLista, ",");
				String fonte= "";
			     while (tk.hasMoreTokens()) {
			    	 fonte = tk.nextToken();
			         listaFonte.add(fonte.trim());
			     }
			} else {
				listaFonte = (LinkedList<String>) obj;
			}
		}
		LOG.trace(punto + "lista fonte>>"+listaFonte);
		return listaFonte;
	}

	public static String recuperaQuery(ISASConnection dbc, String n_cartella, String dadata, String adata,
			String tipo_fonte, String tipo_operatore, int ggVistaSegnalazioneOp, int ggValutazioneBisogni,
			String rich_perso, String rich_altri, String codice_operatore, String zona_operatore,
			String distr_operatore, String id_richiesta, boolean obbligoPV, boolean groupBy, boolean conFonte,
			int ggPregressoPrestazioni, String codReg, String codAzSan, boolean isSo, String fonteDaEscludere,
			String codSedeOperatore, LinkedList<String> listaFonte, String destinatariRichiesta, 
			int ggScadenzaRicovero) {
		String punto = "2-recuperaQuery ";
		StringBuffer query = new StringBuffer();
		query.append(" select cod_zona, cod_distretto, fonte,tipo_operatore ");
		if (!groupBy) {
			query.append(", fonte_dettaglio , n_cartella, cognome, nome, cod_med,pr_mmg_altro, "
					+ CostantiSinssntW.CTS_TIPOCURA);
			;
			query.append(", data_richiesta, id_richiesta, cod_operatore, " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD);
		} else {
			query.append(", count(*) " + CostantiSinssntW.CTS_NUMERO_RICHIESTE_FONTE);
		}
		query.append(" from ");
		query.append(" ( ");
		String queryUnion = "";
		String queryFonte = "";
		
		if (!conFonte || (conFonte && contiene(listaFonte, CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI))) {
			if(!conFonte){
				queryFonte = makeQueryOpCoinvolti(groupBy, obbligoPV);
				queryUnion =addQuery(queryUnion, queryFonte);
			}else {
				String valFonte = "";
				boolean fonteScandenza = false;
				for (int i = 0; i < listaFonte.size(); i++) {
					valFonte = listaFonte.get(i);
					fonteScandenza = Costanti.recuperaFonte(valFonte) ==  CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI;
					if(fonteScandenza){
						queryFonte = makeQueryOpCoinvolti(groupBy, obbligoPV);
						queryUnion =addQuery(queryUnion, queryFonte);
					}
				}
			}
		}

		//		" --Richieste MMG                                          "+
		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA + ""))) {
			//		 Richiesta inserita dalle SO per la prima visita
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE + ""))) {
			//			 SO per cui sono viste le prime visite
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI + ""))) {
			queryFonte = recuperaQueryFonte(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI, groupBy, obbligoPV,
					tipo_operatore, ggVistaSegnalazioneOp, ggValutazioneBisogni);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
/*
		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI + ""))) {
			queryFonte = recuperaQueryFonteAccessiNonConsuntivati(ggPregressoPrestazioni, codReg, codAzSan, groupBy, isSo, false
					, CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI,CostantiSinssntW.CTS_T_F_A_P_NCGG_0);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
	*/	
		
		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0 + ""))) {
			queryFonte = recuperaQueryFonteAccessiNonConsuntivati(-1, codReg, codAzSan, groupBy, isSo, false, 
					CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0, 
					CostantiSinssntW.CTS_T_F_A_P_NCGG_0);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1 + ""))) {
			queryFonte = recuperaQueryFonteAccessiNonConsuntivati(-1, codReg, codAzSan, groupBy, isSo, true, 
					CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1,CostantiSinssntW.CTS_T_F_A_P_NCGG_1);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTE_RICOVERO_RSA + ""))) {
			queryFonte = recuperaQueryFonteRichiesteRicoveroRsa(groupBy, isSo);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte
				|| (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_DIMISSIONE_PROTETTA + ""))) {
			queryFonte = recuperaQueryFonteRichiestaDimissioneProtetta(groupBy, isSo);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_POSTI_DISPONIBILI + ""))) {
			queryFonte = recuperaQueryFontePostiDisponibili(groupBy, isSo);
			queryUnion = addQuery(queryUnion, queryFonte);
		}

		if (!conFonte
				|| (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_INTERRUZIONE_DIMISSIONE + ""))) {
			queryFonte = recuperaQueryFonteInterruzioneDimissione(groupBy, isSo);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		
		if (!conFonte || (conFonte && contiene(listaFonte, CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA))) {
//			String dtAttuale = dbc.formatDbDate(procdate.getjdbcDate());
			if(!conFonte){
				queryFonte = makeQueryScadenzeSo(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA+"", groupBy);
				queryUnion =addQuery(queryUnion, queryFonte);
			}else {
				String valFonte = "";
				boolean fonteScandenza = false;
				for (int i = 0; i < listaFonte.size(); i++) {
					valFonte = listaFonte.get(i);
					fonteScandenza = Costanti.recuperaFonte(valFonte) ==  CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA;
					if(fonteScandenza){
						queryFonte = makeQueryScadenzeSo(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA+"", groupBy);
						queryUnion =addQuery(queryUnion, queryFonte);
					}
				}
			}
		}
		
		if (!conFonte
				|| (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA + ""))) {
			queryFonte = recuperaQueryFonteDimissioneProtetta(groupBy);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		
		if (!conFonte || (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA + ""))) {
			queryFonte = makeQueryNoSchedaSo(codReg, codAzSan ,groupBy);
			queryUnion =addQuery(queryUnion, queryFonte);
		}
		
		if (!conFonte
				|| (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_PUA+ ""))) {
			queryFonte = recuperaQueryFontePua(codReg, codAzSan, groupBy);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		
		if (!conFonte
				|| (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_RICOVERI_IN_SCADENZA+ ""))) {
			queryFonte = recuperaQueryFonteRicoveriInScadenza(groupBy, ggScadenzaRicovero);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		
		if (!conFonte
				|| (conFonte && listaFonte.contains(CostantiSinssntW.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD+ ""))) {
			queryFonte = recuperaQueryFonteEstrazioneFlussiSiad(groupBy);
			queryUnion = addQuery(queryUnion, queryFonte);
		}
		
		query.append(queryUnion);
		query.append(" ) where 0=0 ");
		if (n_cartella != null && !n_cartella.trim().equals("") && !n_cartella.trim().equals("null"))
			query.append(" and n_cartella = " + n_cartella);
		if (dadata != null && !dadata.equals(""))
			query.append(" and data_richiesta >= " + dbc.formatDbDate(dadata));
		if (adata != null && !adata.equals(""))
			query.append(" and data_richiesta <= " + dbc.formatDbDate(adata));

		//		if(zona_operatore!=null)
		//			query += " and cod_zona = '"+zona_operatore+"'";

		/*
		 * if(distr_operatore!=null)
			query += " and (cod_distretto = '"+distr_operatore+"' OR cod_distretto = '0000')";
		if (!isSo){
			if(rich_perso.equals("true") && rich_altri.equals("false"))
				query += " and (cod_operatore is null or cod_operatore='"+codice_operatore+"' )";
			if(rich_perso.equals("false") && rich_altri.equals("true"))
				query += " and (cod_operatore is null or cod_operatore!='"+codice_operatore+"' )";
			if(rich_perso.equals("false") && rich_altri.equals("false") && !isSo){
				query += " and cod_operatore = 'NESSUNO'";
			}
		}
		 */

		if (ISASUtil.valida(destinatariRichiesta)) {
			if (destinatariRichiesta.equals(CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_OPERATORE)) {
				query.append(" AND cod_operatore = '" + codice_operatore + "' ");
			} else {
				if (ISASUtil.valida(zona_operatore)) {
					query.append(" AND " + CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA + " = '" + zona_operatore + "'");
				}
				if (destinatariRichiesta.equals(CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_SEDE)) {
					query.append(" AND " + CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO + " = '" + distr_operatore
							+ "' ");
					query.append(" AND (" + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD + " = '" + codSedeOperatore + "' ");
					query.append(" OR " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD + " is null ) ");
				} else {
					if (destinatariRichiesta.equals(CostantiSinssntW.CTS_L_ASSISTITI_RICERCA_DISTRETTO)) {
						query.append(" AND ( trim(" + CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO + ") = '"
								+ distr_operatore + "' )") ;
					}
				}
			}
		}

		if (isSo && codSedeOperatore != null) {
			if (rich_perso.equals("true") && !rich_altri.equals("true")) {
				query.append(" AND " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD + " = '" + codSedeOperatore + "'");
			}
		}

		if (conFonte) {
			String fonti = "";
			for (int i = 0; i < listaFonte.size(); i++) {
				fonti +=(ISASUtil.valida(fonti)? ",":"")+ "'"+listaFonte.get(i)+"' ";
			}
			query.append(" and fonte IN(" + fonti + ")");
		}

		if (ISASUtil.valida(fonteDaEscludere) && !fonteDaEscludere.equals(CostantiSinssntW.CTS_NO)) {
			query.append(faiqueryDaEscludere(fonteDaEscludere));
			
		}
		if (!tipo_operatore.equals(""))
			query.append(" and (tipo_operatore is null or tipo_operatore='" + tipo_operatore + "' "
					+ " or tipo_operatore ='" + ISASUtil.getValoreIntero(tipo_operatore) + "' )");
		if (!id_richiesta.equals(""))
			query.append(" and (id_richiesta =" + id_richiesta + ")");

		return query.toString();
	}

	private static String recuperaQueryFonteEstrazioneFlussiSiad(boolean groupBy) {
		String query = " SELECT m.cod_zona as cod_zona, m.cod_distretto AS COD_DISTRETTO, trim(" + 
				Costanti.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD+ ") as fonte," + " NULL AS tipo_operatore ";
	if (!groupBy) {
		query += ", r.flag_sent AS fonte_dettaglio, c.n_cartella, c.cognome, c.nome, NULL as cod_med,"
				+ " null as pr_mmg_altro, m.tipocura AS tipocura, "
				+ " r.data_presa_carico_skso AS DATA_RICHIESTA, m.id_skso AS id_richiesta,"
				+ " null AS cod_operatore, NULL as  "
				+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
	}
	query += " from rm_skso r, rm_skso_mmg m, cartella c " +
			 " WHERE r.n_cartella = m.n_cartella AND r.n_cartella = c.n_cartella " +
			 " AND r.id_skso = m.id_skso AND m.tipocura = '" +Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE +
			 "' and r.data_presa_carico_skso is not null AND ( r.flag_sent in ( "+
			Costanti.FLAG_DA_INVIARE_I+","+ 
			Costanti.FLAG_DA_INVIARE_V+","+
			Costanti.FLAG_IN_CONVALIDA_I+","+
			Costanti.FLAG_IN_CONVALIDA_V+","+
			Costanti.FLAG_MOD_IN_CONVALIDA_I+","+
			Costanti.FLAG_MOD_IN_CONVALIDA_V+") OR r.flag_sent IS NULL ) ";

	return query;
	}

	private static String makeQueryNoSchedaSo(String codReg, String codAsl, boolean groupBy) {
		StringBuffer query = new StringBuffer();

		String tipoCura = "m.ski_motivo";
		String dataApertura = "m.ski_data_apertura";
		String codOperatore = "m.ski_operatore";
		String tabella = "skinf";
		String dataChiusura = "m.ski_data_uscita";
		String queryTab = recuperaQueryNoSchedaSo(codReg, codAsl, groupBy, tipoCura, dataApertura, codOperatore, tabella, dataChiusura);

		query.append(queryTab);
		query.append(" UNION  ");
		tipoCura = "m.skf_motivo";
		dataApertura = "m.skf_data";
		codOperatore = "m.skf_operatore";
		tabella = "skfis";
		dataChiusura = "m.skf_data_chiusura";
		queryTab = recuperaQueryNoSchedaSo(codReg, codAsl, groupBy, tipoCura, dataApertura, codOperatore, tabella, dataChiusura);
		query.append(queryTab);

		query.append(" UNION  ");
		tipoCura = "m.skfpg_motivo";
		dataApertura = "m.skfpg_data_apertura";
		codOperatore = "m.skfpg_operatore";
		tabella = "skfpg";
		dataChiusura = "m.skfpg_data_uscita";
		
		queryTab = recuperaQueryNoSchedaSo(codReg, codAsl, groupBy, tipoCura, dataApertura, codOperatore, tabella, dataChiusura);
		query.append(queryTab);
		
		query.append(" UNION  ");
		tipoCura = "m.skm_motivo";
		dataApertura = "m.skm_data_apertura";
		codOperatore = "skm_medico";
		tabella = "skmedico";
		dataChiusura = "m.skm_data_chiusura";
		
		queryTab = recuperaQueryNoSchedaSo(codReg, codAsl, groupBy, tipoCura, dataApertura, codOperatore, tabella, dataChiusura);
		query.append(queryTab);
		
		return query.toString();
	}
	

	private static String recuperaQueryNoSchedaSo( String codReg, String codAsl, boolean groupBy,
			String tipoCura, String dataApertura, String codOperatore, String tabella, String dataChiusura) {
		StringBuffer query = new StringBuffer();
		
		query.append("SELECT o.cod_zona, p.coddistr AS cod_distretto, TRIM (" + CostantiSinssntW.CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA
				+ ") AS fonte, o.tipo AS tipo_operatore ");
		if (!groupBy) {
			query.append(", 0 AS fonte_dettaglio, c.n_cartella, c.cognome,  c.nome, NULL AS cod_med, NULL AS pr_mmg_altro, ");
			query.append(tipoCura +"  AS tipocura, " +dataApertura +" AS data_richiesta, n_contatto as id_richiesta, ");
			query.append(" o.codice AS cod_operatore, NULL AS " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD);
		}
		query.append(" FROM " +tabella+ " m, cartella c, operatori o, presidi p ");
		query.append(" WHERE m.n_cartella = c.n_cartella AND " +codOperatore + " = o.codice AND p.codpres = o.cod_presidio ");
		query.append(" AND m.id_skso IS NULL AND p.codreg = '" + codReg + "' AND p.codazsan = '" + codAsl);
		query.append("' AND "+dataChiusura +" IS NULL ");
		query.append(" AND m.n_cartella NOT IN (SELECT x.n_cartella FROM rm_skso x WHERE x.n_cartella = m.n_cartella )");
		
		return query.toString();
	}


	private static String recuperaQueryFonteDimissioneProtetta(boolean groupBy) {
		String query = " SELECT D.COD_ZONA, M.DISTRETTO AS COD_DISTRETTO, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+ ") as fonte,"
				+ " null AS tipo_operatore ";
		if (!groupBy) {
			query += ", 0 AS fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, null as cod_med,"
					+ " m.tipo_caso as pr_mmg_altro, M.SERVIZIO_RICHIESTO AS tipocura, " 
					+ " M.DATA_DIM_PREVISTA AS DATA_RICHIESTA,"
					+ " M.ID_SCHEDA AS ID_RICHIESTA," +
					" trim(M.OPERATORE) AS cod_operatore," +
					" m.presidio as  "+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		
		query += " FROM PHT2_GENERALE M, CARTELLA C, DISTRETTI D, operatori o WHERE M.N_CARTELLA = C.N_CARTELLA AND M.STATO_SCHEDA = '" +
				CostantiPHT.statoCompleta + "' AND M.DISTRETTO = D.COD_DISTR  and m.operatore = o.codice ";
		
		return query;
		
	}

	private static String recuperaQueryFontePua(String codReg, String codAsl, boolean groupBy) {
		String query = " SELECT d.cod_zona as cod_zona, d.cod_distr AS COD_DISTRETTO, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_PUA
				+ ") as fonte," + " NULL AS tipo_operatore ";
		if (!groupBy) {
			query += ", 0 AS fonte_dettaglio " + ", c.n_cartella, c.cognome, c.nome, NULL as cod_med,"
					+ "  m.bisogno_rich_altro as pr_mmg_altro, m." +Costanti.CTS_PUA_TIPO_CURA +
					" AS tipocura, "
					+ " m.data_reg AS DATA_RICHIESTA," + " m.PROGRESSIVO AS ID_RICHIESTA,"
					+ " null AS cod_operatore, NULL as  "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		/*
	SELECT d.cod_zona AS cod_zona, d.cod_distr AS cod_distretto,
       TRIM (20) AS fonte, NULL AS tipo_operatore, 0 AS fonte_dettaglio,
       c.n_cartella, c.cognome, c.nome, NULL AS cod_med,
       m.bisogno_rich_altro AS pr_mmg_altro, m.int_assistenziale AS tipocura,
       m.data_reg AS data_richiesta, m.progressivo AS id_richiesta,
       TRIM (m.cod_operatore) AS cod_operatore, NULL AS l_cod_sede
  FROM ass_anagrafica m, cartella c, distretti d
 WHERE m.n_cartella = c.n_cartella
   AND m.esito_contatto = '6'
   AND m.id_skso IS NULL
   AND m.distretto_uvi = d.cod_distr
		*/
		query += " FROM ass_anagrafica m, cartella c, distretti d "
				+ " WHERE m.n_cartella = c.n_cartella AND m. esito_contatto = '"
				+ CostantiSinssntW.CTS_ESITO_CONTATTO_PUA + "' "
				+ " AND m.id_skso IS NULL AND m.distretto_uvi = d.cod_distr ";

		return query;

	}

	private static String recuperaQueryFonteRicoveriInScadenza(boolean groupBy, int ggScadenzaRicovero) {
		String query = " SELECT m.cod_zona as cod_zona, m.cod_distretto AS COD_DISTRETTO, trim(" + 
					CostantiSinssntW.CTS_TIPO_FONTE_RICOVERI_IN_SCADENZA + ") as fonte," + " NULL AS tipo_operatore ";
		if (!groupBy) {
			query += ", 0 AS fonte_dettaglio " + ", c.n_cartella, c.cognome, c.nome, NULL as cod_med,"
					+ " null as pr_mmg_altro, m.tipocura AS tipocura, "
					+ " v.ric_datdim AS DATA_RICHIESTA, m.id_skso AS id_richiesta,"
					+ " null AS cod_operatore, NULL as  "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		/*
	SELECT m.cod_zona, m.cod_distretto AS cod_distretto, TRIM (21) AS fonte,
       null AS tipo_operatore, 0 AS fonte_dettaglio,
       r.n_cartella, c.cognome, c.nome, NULL AS cod_med,
       NULL AS pr_mmg_altro, m.tipocura AS tipocura, v.ric_datdim,
       r.id_skso AS id_richiesta, null AS cod_operatore,
       r.cod_presidio AS l_cod_sede
 FROM rm_skso r, rm_skso_mmg m, zk_rsa_ricoveri v, cartella c
 WHERE r.n_cartella = m.n_cartella
   AND r.n_cartella = c.n_cartella
   AND r.id_skso = m.id_skso
   AND m.tipocura = '3'
   AND r.n_cartella = v.n_cartella
   and r.id_skso = v.id_skso
   AND (v.ric_motdim IS NOT NULL OR v.ric_motdim <> '0')
   and v.ric_datdim <= sysdate + 2
		*/
		query += " from rm_skso r, rm_skso_mmg m, zk_rsa_ricoveri v, cartella c " +
				 " WHERE r.n_cartella = m.n_cartella AND r.n_cartella = c.n_cartella " +
				 " AND r.id_skso = m.id_skso AND m.tipocura = '" +Costanti.CTS_COD_CURE_RESIDENZIALI +
				 "' AND r.n_cartella = v.n_cartella and r.id_skso = v.id_skso " +
				 " AND (v.ric_motdim IS NULL ) and v.ric_datdim <= sysdate "+(ggScadenzaRicovero>0 ? " + "+ggScadenzaRicovero:"");

		return query;

	}

	
	private static Object faiqueryDaEscludere(String fonteDaEscludere) {
		String vFonte ="";
		int fonte;
		String fontiApiciEscludere = "";
		String query = "";
		if(ISASUtil.valida(fonteDaEscludere)){
			StringTokenizer token = new StringTokenizer(fonteDaEscludere, ",");
			while (token.hasMoreElements()){
				vFonte = token.nextToken();
				fonte = Costanti.recuperaFonte(vFonte);
				if (fonte == CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA){
					query += "  AND FONTE  not LIKE '" +fonte +".%' "; 
				}else {
					fontiApiciEscludere += (ISASUtil.valida(fontiApiciEscludere)?", ": "") + "'"+vFonte+"'"; 
				}
			}
		}
		if (ISASUtil.valida(fontiApiciEscludere)){
			query +=" AND fonte NOT IN (" + fontiApiciEscludere +" ) ";
		}
		
		return query;
	}

	private static boolean contiene(LinkedList<String> listaFonte, int ctsTipoFonteAccessiSoInScadenza) {
		String fonte="";
		boolean trovato = false;
		int i=0;
		while (i < listaFonte.size() && !trovato) {
			fonte = listaFonte.get(i)+"";
			trovato = Costanti.recuperaFonte(fonte) ==  ctsTipoFonteAccessiSoInScadenza;
			i++;
		}
		return trovato;
	}

	private static String makeQueryScadenzeSo(String fonte, boolean groupBy) {
		String query = " SELECT a.cod_zona, a.cod_distretto, trim(" + fonte+ 
				"|| '.' || a.tipocura ||'.'||1) as fonte,"
				+ " d.pv_tp_operatore AS tipo_operatore ";
		if (!groupBy) {
			query += ",  0 as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med,"
//					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura,  a.data_fine AS data_richiesta,"
					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura,  " 
					+ " GREATEST(a.data_fine, NVL((select max(pro.DT_PROROGA_FINE) FROM rm_skso_proroghe pro WHERE pro.n_cartella = a.n_cartella AND a.id_skso = pro.id_skso), date '0000-01-01')" 
					+") AS data_richiesta,"
					+ " a.id_skso AS id_richiesta, trim(d.pv_cod_operatore) AS cod_operatore , d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " FROM rm_skso_mmg a, rm_skso d, cartella c WHERE a.n_cartella = c.n_cartella "
			  + " AND d.n_cartella = c.n_cartella AND a.id_skso = d.id_skso " 
			  + " AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) " 
//			  + " AND a.data_fine <= " + dtAttuale +
			  + " AND ((" +
			  " (select max(pro.DT_PROROGA_FINE) from rm_skso_proroghe pro " +
			  " where pro.n_cartella = a.n_cartella and a.id_skso = pro.id_skso ) <= SYSDATE ) " 
			  + " OR ( ( a.data_fine < sysdate ) " 
			  + " AND not exists (select * from rm_skso_proroghe pro where pro.n_cartella = a.n_cartella and a.id_skso = pro.id_skso ) ))" 
			  + " AND data_presa_carico_skso IS NOT NULL "
			  + " UNION SELECT a.cod_zona, a.cod_distretto, trim(" + fonte+ 
				"|| '.' || a.tipocura ||'.'||2) as fonte,"
				+ " d.pv_tp_operatore AS tipo_operatore ";
		if (!groupBy) {
			query += ",  0 as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med,"
//					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura,a.data_fine AS data_richiesta,"
					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura,  " 
					+ " GREATEST(a.data_fine, NVL((select max(pro.DT_PROROGA_FINE) FROM rm_skso_proroghe pro WHERE pro.n_cartella = a.n_cartella AND a.id_skso = pro.id_skso), date '0000-01-01')" 
					+") AS data_richiesta,"
					+ " a.id_skso AS id_richiesta, trim(d.pv_cod_operatore) AS cod_operatore , d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " FROM rm_skso_mmg a, rm_skso d, cartella c WHERE a.n_cartella = c.n_cartella "
			  + " AND d.n_cartella = c.n_cartella AND a.id_skso = d.id_skso " 
			  + " AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) " 
//			  + " AND a.data_fine  > " +dtAttuale +
			  + " AND ((sysdate < " +
			  "	(select max(pro.DT_PROROGA_FINE) from rm_skso_proroghe pro where pro.n_cartella = a.n_cartella and a.id_skso = pro.id_skso ) " 
			  +	" AND TO_CHAR(SYSDATE, 'mm-yyyy') = TO_CHAR((select max(pro.DT_PROROGA_FINE) from rm_skso_proroghe pro where pro.n_cartella = a.n_cartella and a.id_skso = pro.id_skso), 'mm-yyyy'))" 
			  + " OR ( ( a.data_fine > sysdate ) AND ( TO_CHAR (a.data_fine, 'mm-yyyy') = TO_CHAR (SYSDATE, 'mm-yyyy') ) " 
			  + " AND not exists (select * from rm_skso_proroghe pro where pro.n_cartella = a.n_cartella and a.id_skso = pro.id_skso ) ) )" 
			  + " AND data_presa_carico_skso IS NOT NULL ";
			  
		return query;
	}

	
	private static String recuperaQueryFonteInterruzioneDimissione(boolean groupBy, boolean isSo) {
		String query = "";
		if (isSo) {
			query = " select m.cod_zona, m.cod_distretto as cod_distretto, trim("
					+ CostantiSinssntW.CTS_TIPO_FONTE_INTERRUZIONE_DIMISSIONE + ") as fonte,"
					+ " TRIM (o.tipo) AS tipo_operatore ";
			if (!groupBy) {
				query += ", 0 as fonte_dettaglio " + ", c.n_cartella, c.cognome, c.nome,"
						+ " null as cod_med, o.cod_presidio as pr_mmg_altro," + " null as tipocura, m.data_richiesta,"
						+ " m.id_skso as id_richiesta, 'cod_op' AS cod_operatore, o.cod_presidio AS l_cod_sede ";
			}
			query += " FROM operatori o, rm_skso_mmg m, cartella c, rm_skso k  WHERE m.tipocura = '"
					+ CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' AND m.n_cartella = k.n_cartella "
					+ " AND c.n_cartella = k.n_cartella  AND m.id_skso = k.id_skso and o.CODICE = k.pv_cod_operatore";
		}
		return query;
	}

	private static String recuperaQueryFontePostiDisponibili(boolean groupBy, boolean isSo) {
		String query = "";
		if (isSo) {
			query = " select m.cod_zona, m.cod_distretto as cod_distretto, trim("
					+ CostantiSinssntW.CTS_TIPO_FONTE_POSTI_DISPONIBILI + ") as fonte,"
					+ " TRIM (o.tipo) AS tipo_operatore ";
			if (!groupBy) {
				query += ", 0 as fonte_dettaglio " + ", c.n_cartella, c.cognome, c.nome,"
						+ " null as cod_med, o.cod_presidio as pr_mmg_altro," + " null as tipocura, m.data_richiesta,"
						+ " m.id_skso as id_richiesta, 'cod_op' AS cod_operatore, o.cod_presidio AS l_cod_sede ";
			}

			query += " FROM operatori o, rm_skso_mmg m, cartella c, rm_skso k  WHERE m.tipocura = '"
					+ CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' AND m.n_cartella = k.n_cartella "
					+ " AND c.n_cartella = k.n_cartella  AND m.id_skso = k.id_skso and o.CODICE = k.pv_cod_operatore";
		}
		return query;
	}

	private static String recuperaQueryFonteRichiestaDimissioneProtetta(boolean groupBy, boolean isSo) {
		String query = "";
		if (isSo) {
			query = " select m.cod_zona, m.cod_distretto as cod_distretto, trim("
					+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_DIMISSIONE_PROTETTA + ") as fonte,"
					+ " TRIM (o.tipo) AS tipo_operatore ";
			if (!groupBy) {
				query += ", 0 as fonte_dettaglio " + ", c.n_cartella, c.cognome, c.nome,"
						+ " null as cod_med, o.cod_presidio as pr_mmg_altro," + " null as tipocura, m.data_richiesta,"
						+ " m.id_skso as id_richiesta, 'cod_op' AS cod_operatore, o.cod_presidio AS l_cod_sede ";
			}

			query += " FROM operatori o, rm_skso_mmg m, cartella c, rm_skso k  WHERE m.tipocura = '"
					+ CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' AND m.n_cartella = k.n_cartella "
					+ " AND c.n_cartella = k.n_cartella  AND m.id_skso = k.id_skso and o.CODICE = k.pv_cod_operatore";
		}
		return query;

	}

	private static String recuperaQueryFonteRichiesteRicoveroRsa(boolean groupBy, boolean isSo) {
		String query = "";
		if (isSo) {
			query = " select m.cod_zona, m.cod_distretto as cod_distretto, trim("
					+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTE_RICOVERO_RSA + ") as fonte,"
					+ " TRIM (o.tipo) AS tipo_operatore ";
			if (!groupBy) {
				query += ", 0 as fonte_dettaglio , c.n_cartella, c.cognome, c.nome,"
						+ " null as cod_med, o.cod_presidio as pr_mmg_altro," + " null as tipocura, m.data_richiesta,"
						+ " m.id_skso as id_richiesta, 'cod_op' AS cod_operatore, o.cod_presidio AS l_cod_sede  ";
			}

			query += " FROM operatori o, rm_skso_mmg m, cartella c, rm_skso k  WHERE m.tipocura = '"
					+ CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' AND m.n_cartella = k.n_cartella "
					+ " AND c.n_cartella = k.n_cartella  AND m.id_skso = k.id_skso and o.CODICE = k.pv_cod_operatore";
		}
		return query;
	}

	private static String addQuery(String queryUnion, String queryFonte) {
		String query = queryUnion;
		if (ISASUtil.valida(queryFonte)) {
			query += (ISASUtil.valida(query) ? " UNION " : " ") + queryFonte;
		}
		return query;
	}

	private static String recuperaQueryFonte(int fonte, boolean groupBy, boolean obbligoPV, String tipo_operatore,
			int ggVistaSegnalazioneOp, int ggValutazioneBisogni) {
		String query = "";

		switch (fonte) {
		case CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG:
			query = makeQueryRichiestaMMG(groupBy);
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA:
			query = makeQueryFontePrimaVisita(groupBy);
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE:
			query = makeQueryVisteSo(groupBy);
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA:
			// " --Richieste Chiusura Scheda So in attesa				   "+
			query = makeQueryRichiestaChiusura(groupBy);
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI:
			// SEGNALAZIONI
			query = makeQuerySegnalazioni(groupBy, tipo_operatore, ggVistaSegnalazioneOp);

			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI:
			/* Schede valutazioni in scadenza */
			query = makeQueryValutazioneUvi(groupBy);
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE:
			query = makeQueryMancaAttivazione(groupBy);
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI:
			query = makeQueryValutazioneBisogni(groupBy, ggValutazioneBisogni);
			break;
		default:
			break;
		}
		return query;
	}

	private static String makeQueryValutazioneBisogni(boolean groupBy, int ggValutazioneBisogni) {
		String query = "SELECT a.cod_zona, a.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI
				+ ") AS fonte, TRIM (d.pv_tp_operatore)  AS tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI_DETTAGLIO + " AS fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med, a.medico_altro_desc AS pr_mmg_altro, a.tipocura,"
					+ " v.data    AS data_richiesta, a.id_skso AS id_richiesta,"
					+ " TRIM (d.pv_cod_operatore) AS cod_operatore, d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}

		query += " FROM rm_skso_mmg a,   rm_skso d,   cartella c,   scl_valutazione v "
				+ " WHERE a.n_cartella     = c.n_cartella"
				+ " AND d.n_cartella = c.n_cartella AND a.id_skso = d.id_skso AND C.DATA_CHIUSURA   IS NULL"
				+ " AND C.MOTIVO_CHIUSURA IS NULL AND c.n_cartella       = v.n_cartella " + " AND a.tipocura = '"
				+ CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE
				+ "' AND NOT EXISTS ( SELECT 1 FROM SCL_VALUTAZIONE X WHERE X.N_CARTELLA = A.N_CARTELLA AND X.DATA >= "
				+ " (sysdate - " + (ggValutazioneBisogni > 0 ? ggValutazioneBisogni : 0) + " ) ) "
				+ " AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) " + " and v.data IN "
				+ " ( SELECT MAX(x.data)   FROM scl_valutazione x   WHERE x.n_cartella = a.n_cartella "
				+ " AND x.data <= (sysdate - " + (ggValutazioneBisogni > 0 ? ggValutazioneBisogni : 0) + " ) ) ";
		return query;
	}

	private static String makeQueryMancaAttivazione(boolean groupBy) {
		String query = " SELECT a.cod_zona, a.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE
				+ ") AS fonte, TRIM (d.pv_tp_operatore) AS tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE_DETTAGLIO + " AS fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med, a.medico_altro_desc AS pr_mmg_altro, a.tipocura, "
					+ " d.pr_data_puac AS data_richiesta, a.id_skso AS id_richiesta, "
					+ " TRIM (d.pv_cod_operatore) AS cod_operatore, d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " FROM rm_skso_mmg a, rm_skso d, cartella c WHERE a.n_cartella = c.n_cartella "
				+ " AND d.n_cartella = c.n_cartella AND a.id_skso = d.id_skso AND c.data_chiusura IS NULL AND c.motivo_chiusura IS NULL "
				+ " AND D.PR_DATA_CHIUSURA IS NULL AND D.DATA_PRESA_CARICO_SKSO IS NULL AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) ";
		/* RIMOSSO IN QUANDO CI SONO DATI DEL PORTING che non vanno bene
		" AND ( ( a.tipocura = '1' and ( (d.ispianocongelato is null) or (upper(d.ispianocongelato) <>'S') ) ) " +
		" OR ( (a.tipocura = '2' and d.data_presa_carico_skso is null) ) ) ";
		*/
		return query;
	}

	private static String makeQueryValutazioneUvi(boolean groupBy) {
		String query = " SELECT a.cod_zona, a.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI
				+ ") AS fonte, TRIM (d.pv_tp_operatore) AS tipo_operatore  ";

		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI_DETTAGLIO + " AS fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med, a.medico_altro_desc AS pr_mmg_altro,"
					+ " a.tipocura, d.pr_data_revisione AS data_richiesta, a.id_skso AS id_richiesta, "
					+ "TRIM (d.pv_cod_operatore) AS cod_operatore, d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " FROM rm_skso_mmg a, rm_skso d, cartella c WHERE a.n_cartella = c.n_cartella"
				+ " AND d.n_cartella = c.n_cartella AND a.id_skso = d.id_skso AND pr_data_revisione IS NOT NULL"
				+ " AND C.DATA_CHIUSURA IS NULL AND C.MOTIVO_CHIUSURA IS NULL AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) "
				+ " AND TO_CHAR (pr_data_revisione, 'yyyy-mm') <= TO_CHAR (SYSDATE, 'yyyy-mm') AND NOT EXISTS ("
				+ " SELECT 1 FROM rm_skso x WHERE x.n_cartella = d.n_cartella AND x.id_skso > d.id_skso) " 
				+ " AND NOT EXISTS ( SELECT 1  FROM rm_skso_proroghe r WHERE n_cartella = d.n_cartella AND id_skso = d.id_skso " 
				+ " AND dt_proroga_uvi IN ( SELECT MAX (dt_proroga_uvi) FROM rm_skso_proroghe x WHERE x.n_cartella = r.n_cartella " 
				+ " AND x.id_skso = r.id_skso) AND TO_CHAR (r.dt_proroga_uvi, 'yyyy-mm') >= TO_CHAR (SYSDATE, 'yyyy-mm') ) " 
				+ " AND NOT EXISTS ( SELECT 1 FROM rm_esiti_valutazioni_uvi r WHERE n_cartella = d.n_cartella AND id_skso = d.id_skso " 
				+ " AND TO_CHAR (dt_prossima_valutazione, 'yyyy-mm') >= TO_CHAR (SYSDATE, 'yyyy-mm') ) ";
		return query;
	}

	private static String makeQuerySegnalazioni(boolean groupBy, String tipo_operatore, int ggVistaSegnalazioneOp) {
		String query = " SELECT  a.cod_zona, a.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI
				+ ") AS fonte, " + " TRIM (s.tipo_operatore) AS tipo_operatore ";

		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI_DETTAGLIO + " AS fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med, s.oggetto AS pr_mmg_altro, a.tipocura,"
					+ " s.data_segnalazione AS data_richiesta, a.id_skso AS id_richiesta, "
					+ " TRIM (s.cod_operatore) AS cod_operatore, d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}

		query += " FROM rm_skso_mmg a, rm_skso d, cartella c, rm_segnalazioni s WHERE a.n_cartella = d.n_cartella"
				+ " AND a.n_cartella = c.n_cartella AND a.n_cartella = s.n_cartella AND a.id_skso = d.id_skso"
				+ " AND ((a.id_skso = s.id_skso) OR (s.id_skso IS NULL)) AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) ";
		if (ISASUtil.valida(tipo_operatore)) {
			if (ggVistaSegnalazioneOp > 0) {
				query += " AND ( (vista_so = '2' and data_segnalazione < sysdate - " + ggVistaSegnalazioneOp
						+ " ) OR vista_so <> '2' ) ";
			} else {
				query += " AND (vista_so <> '2' or vista_so is null) ";
			}
		} else {
			query += "AND (vista_so <> '2' OR vista_so IS NULL) ";
		}
		return query;
	}

	private static String makeQueryRichiestaChiusura(boolean groupBy) {
		String query = " select cod_zona_presacarico as cod_zona," + " cod_distretto_presacarico as cod_distretto, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ") as fonte," + " null as tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_5_DETTAGLIO_0 + " as fonte_dettaglio"
					+ ", c.n_cartella, c.cognome, c.nome," + " null as cod_med, null as pr_mmg_altro,"
					+ " null as tipocura, m.data_richiesta," + " m.id_skso as id_richiesta,"
					+ " cod_operatore_richiedente as cod_operatore, r.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}

		query += " from richieste_chiusura m, cartella c, rm_skso r "
				+ " where m.n_cartella = c.n_cartella and m.n_cartella = r.n_cartella and m.id_skso = r.id_skso "
				+ " and m.esito_richiesta = '" + CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA + "' UNION"
				+
				// " --Richieste Chiusura Scheda So confermate				   "+
				" select cod_zona_presacarico as cod_zona," + " cod_distretto_presacarico as cod_distretto, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ") as fonte," + " null as tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_5_DETTAGLIO_1 + " as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome," + " null as cod_med, null as pr_mmg_altro,"
					+ " null as tipocura, m.data_richiesta," + " m.id_skso as id_richiesta,"
					+ " cod_operatore_richiedente as cod_operatore, r.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}

		query += " from richieste_chiusura m, cartella c, rm_skso r "
				+ " where m.n_cartella = c.n_cartella and m.n_cartella = r.n_cartella and m.id_skso = r.id_skso "
				+ " and m.esito_richiesta = '" + CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_CONFERMATA + "'" + " UNION"
				+
				// " --Richieste Chiusura Scheda So Annullate				   "+
				" select  cod_zona_presacarico as cod_zona," + " cod_distretto_presacarico as cod_distretto, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ") as fonte," + " null as tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_5_DETTAGLIO_2 + " as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome," + " null as cod_med, null as pr_mmg_altro,"
					+ " null as tipocura, m.data_richiesta," + " m.id_skso as id_richiesta,"
					+ " cod_operatore_richiedente as cod_operatore, r.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from richieste_chiusura m, cartella c, rm_skso r "
				+ " where m.n_cartella = c.n_cartella and m.n_cartella = r.n_cartella and m.id_skso = r.id_skso "
				+ " and m.esito_richiesta = '" + CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_ANNULLATA + "'" + " UNION"
				+
				// " --Richieste Chiusura Scheda So in consultazione (distretto richiedente) in attesa				   "+
				" select  cod_zona_richiedente as cod_zona," + " '0000' as cod_distretto, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ") as fonte," + " null as tipo_operatore  ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_5_DETTAGLIO_0 + " as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, null as cod_med, null as pr_mmg_altro,"
					+ " null as tipocura, m.data_richiesta, m.id_skso as id_richiesta,"
					+ " cod_operatore_richiedente as cod_operatore, r.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from richieste_chiusura m, cartella c, rm_skso r "
				+ " where m.n_cartella = c.n_cartella and m.n_cartella = r.n_cartella and m.id_skso = r.id_skso "
				+ " and m.esito_richiesta = '" + CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA + "'" + " UNION"
				+
				// " --Richieste Chiusura Scheda So in consultazione (distretto richiedente) confermata				   "+
				" select  cod_zona_richiedente as cod_zona," + " '0000' as cod_distretto, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ") as fonte," + " null as tipo_operatore  ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_5_DETTAGLIO_1 + " as fonte_dettaglio"
					+ ", c.n_cartella, c.cognome, c.nome," + " null as cod_med, null as pr_mmg_altro,"
					+ " null as tipocura, m.data_richiesta," + " m.id_skso as id_richiesta, "
					+ " cod_operatore_richiedente as cod_operatore, r.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from richieste_chiusura m, cartella c, rm_skso r "
				+ " where m.n_cartella = c.n_cartella  and m.n_cartella = r.n_cartella and m.id_skso = r.id_skso "
				+ " and m.esito_richiesta = '" + CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_CONFERMATA + "'" + " UNION"
				+
				// " --Richieste Chiusura Scheda So in consultazione (distretto richiedente) annullata		   "+
				" select cod_zona_richiedente as cod_zona," + " '0000' as cod_distretto, trim("
				+ CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA + ") as fonte," + " null as tipo_operatore  ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_5_DETTAGLIO_2 + " as fonte_dettaglio "
					+ " ,c.n_cartella, c.cognome, c.nome," + " null as cod_med, null as pr_mmg_altro,"
					+ " null as tipocura, m.data_richiesta," + " m.id_skso as id_richiesta,"
					+ " cod_operatore_richiedente as cod_operatore, r.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from richieste_chiusura m, cartella c, rm_skso r "
				+ " where m.n_cartella = c.n_cartella and m.n_cartella = r.n_cartella and m.id_skso = r.id_skso "
				+ " and m.esito_richiesta = '" + CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_ANNULLATA + "'";
		return query;
	}

	private static String makeQueryVisteSo(boolean groupBy) {

		String query = " SELECT a.cod_zona, a.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE + 
				") as fonte, d.pv_tp_operatore AS tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO4 + " as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med,"
					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura," + " d.pr_data_puac AS data_richiesta,"
					+ " a.id_skso AS id_richiesta, TRIM (d.pv_cod_operatore) AS cod_operatore , d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " FROM rm_skso_mmg a, rm_skso d, cartella c" + " WHERE a.n_cartella = c.n_cartella"
				+ " AND d.n_cartella = c.n_cartella" + " AND a.id_skso = d.id_skso" + " AND d.VISTA_DA_SO = '"
				+ CostantiSinssntW.CTS_FLAG_STATO_FATTA
				+ "' AND (d.pr_data_chiusura IS NULL AND d.pr_motivo_chiusura IS NULL) UNION "
				+ " SELECT b.cod_zona, b.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE + ") as fonte,"
				+ " TRIM (b.tipo_operatore) AS tipo_operatore ";

		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO5 + " as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med,"
					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura,"
					+ " a.pr_mmg_data_richiesta AS data_richiesta,"
					+ " a.id_skso AS id_richiesta,  TRIM (b.cod_operatore) AS cod_operatore, d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " FROM rm_skso_mmg a, rm_skso d, rm_skso_op_coinvolti b,"
				+ " cartella c WHERE a.n_cartella = c.n_cartella  AND"
				+ " b.n_cartella = c.n_cartella AND d.n_cartella = c.n_cartella"
				+ " AND a.id_skso = b.id_skso  AND a.id_skso = d.id_skso" + " AND b.vista_da_so = '"
				+ CostantiSinssntW.CTS_FLAG_STATO_FATTA + "'";
		return query;
	}

	private static String makeQueryFontePrimaVisita(boolean groupBy) {
		String query = " SELECT a.cod_zona, a.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA
				+ ") as fonte, " + " TRIM(d.pv_tp_operatore) AS tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO3 + " as fonte_dettaglio "
					+ ", c.n_cartella, c.cognome, c.nome, a.cod_med,"
					+ " a.medico_altro_desc AS pr_mmg_altro, a.tipocura," + " d.pr_data_puac AS data_richiesta,"
					+ " a.id_skso AS id_richiesta," + " TRIM(d.pv_cod_operatore) AS cod_operatore, o.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}

		query += " FROM rm_skso_mmg a, rm_skso d, cartella c, operatori o WHERE a.n_cartella = c.n_cartella"
				+ " AND d.n_cartella = c.n_cartella AND d.pv_cod_operatore = o.codice AND a.id_skso = d.id_skso"
				+ " AND d.pv_dt_visita IS NULL AND d.pv_tp_operatore IS NOT NULL AND d.pv_cod_operatore IS NOT NULL"
				+ " and (d.vista_da_so is null or d.vista_da_so != '" + CostantiSinssntW.CTS_FLAG_STATO_RIMOSSA + "')"
				+ " and pr_data_chiusura is null" + " and pr_motivo_chiusura is null ";
		return query;
	}

	private static String makeQueryRichiestaMMG(boolean groupBy) {
		String query = " select  m.cod_zona, m.cod_distretto, trim(" + CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG
				+ ") as fonte," + " null as tipo_operatore ";
		if (!groupBy) {
			query += "," + CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO2 + " as fonte_dettaglio, "
					+ " c.n_cartella, c.cognome, c.nome," + " m.cod_med, null as pr_mmg_altro,"
					+ " m.tipocura, m.pr_mmg_data_richiesta as data_richiesta," + " m.id_rich as id_richiesta,"
					+ " null as cod_operatore, null as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from rm_rich_mmg m, cartella c " + " where m.n_cartella = c.n_cartella and m.stato = '"+RmRichiesteMMGEJB.STATO_RICH_MMG_ATTESA+"'";
		return query;
	}

	private static String makeQueryOpCoinvolti(boolean groupBy, boolean obbligoPV) {
		String query = " select b.cod_zona, b.cod_distretto, trim(";
		query += CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI + " ||'.' || a.tipocura) as fonte, TRIM (b.tipo_operatore) as tipo_operatore ";
		if (!groupBy) {
			query += ", "
					+ CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO1
					+ " as fonte_dettaglio, c.n_cartella, c.cognome, c.nome, a.cod_med, a.medico_altro_desc as pr_mmg_altro,";
			query += " a.tipocura,  d.data_presa_carico_skso as data_richiesta,";
			query += " a.id_skso as id_richiesta, TRIM (b.cod_operatore) as cod_operatore, d.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from rm_skso_mmg a, rm_skso d, ";
		query += " rm_skso_op_coinvolti b, cartella c ";
		query += " where a.n_cartella = c.n_cartella " + " and b.n_cartella = c.n_cartella"
				+ " and d.n_cartella = c.n_cartella" + " and a.id_skso = b.id_skso" + " and a.id_skso = d.id_skso"
				+ " and b.dt_presa_carico is null AND B.COD_OPERATORE IS NULL "
				+ recuperaSoloPerPresaCarico() 
				+ " AND ( D.PR_DATA_CHIUSURA IS NULL AND PR_MOTIVO_CHIUSURA IS NULL) "
				+ " AND ( UPPER(b.no_alert) = 'S' or b.no_alert is NULL) "
				+ " and d.data_presa_carico_skso is not null";
		if (obbligoPV) {
			query += " AND ( a.tipocura <> '" + CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' OR"
					+ " ( a.tipocura = '" + CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' "
					+ " AND d.pv_tp_operatore IS NOT NULL AND d.pv_cod_operatore IS NOT NULL )" + ")";
		}
		query += " and (b.vista_da_so is null or b.vista_da_so != '" + CostantiSinssntW.CTS_FLAG_STATO_RIMOSSA + "')";

		query += " UNION select b.cod_zona, b.cod_distretto, trim(";
		query += CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI + " ||'.' || a.tipocura) as fonte, TRIM (b.tipo_operatore) as tipo_operatore ";
		if (!groupBy) {
			query += ", "
					+ CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO1
					+ " as fonte_dettaglio, c.n_cartella, c.cognome, c.nome, a.cod_med, a.medico_altro_desc as pr_mmg_altro,";
			query += " a.tipocura,  d.data_presa_carico_skso as data_richiesta,";
			query += " a.id_skso as id_richiesta, TRIM (b.cod_operatore) as cod_operatore, o.cod_presidio as "
					+ CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
		query += " from rm_skso_mmg a, rm_skso d, rm_skso_op_coinvolti b, cartella c, operatori o ";
		query += " where a.n_cartella = c.n_cartella " + " and b.n_cartella = c.n_cartella"
				+ " and d.n_cartella = c.n_cartella" + " and a.id_skso = b.id_skso" + " and a.id_skso = d.id_skso"
				+ " and b.dt_presa_carico is null AND B.COD_OPERATORE IS not NULL"
				+ recuperaSoloPerPresaCarico() 
				+ " AND ( D.PR_DATA_CHIUSURA IS NULL AND PR_MOTIVO_CHIUSURA IS NULL) "
				+ " AND ( UPPER(b.no_alert) = 'S' or b.no_alert is NULL) "
				+ " and d.data_presa_carico_skso is not null and b.cod_operatore = o.codice ";
		if (obbligoPV) {
			query += " AND ( a.tipocura <> '" + CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' OR"
					+ " ( a.tipocura = '" + CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "' "
					+ " AND d.pv_tp_operatore IS NOT NULL AND d.pv_cod_operatore IS NOT NULL )" + ")";
		}
		query += " and (b.vista_da_so is null or b.vista_da_so != '" + CostantiSinssntW.CTS_FLAG_STATO_RIMOSSA + "')";

		return query;
	}

	
	private static String recuperaSoloPerPresaCarico() {
		return " AND a.tipocura in ('" + CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE + "', '"
				+ CostantiSinssntW.CTS_COD_CURE_PRESTAZIONALI + "') ";
	}

	private static String recuperaQueryFonteAccessiNonConsuntivati(int pregressoPrestazioni, String codReg, String codAzSan, boolean groupBy,
			boolean isSo, boolean conNumeroAccessi, int numeroFonte, int numAccessiContare) {
		
		String query ="";
		if (isSo){
				query = " select m.cod_zona, m.cod_distretto as cod_distretto, trim("+
						numeroFonte+") as fonte,"+
						" trim(a.pa_tipo_oper) AS tipo_operatore ";
				if (!groupBy){
					query +=", 0 as fonte_dettaglio "+
							", c.n_cartella, c.cognome, c.nome,"+
							" null as cod_med, o.cod_presidio as pr_mmg_altro,"+
							" null as tipocura, m.data_richiesta,"+
							" m.id_skso as id_richiesta, s.pa_operatore AS cod_operatore , o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
				}
				
				query +=" FROM pai p, piano_accessi a, piano_assist s, operatori o, prestaz z, rm_skso_mmg m, cartella c, rm_skso k " +
						" WHERE a.n_cartella = p.n_cartella AND c.n_cartella = a.n_cartella AND O.CODICE = S.PA_OPERATORE " +
						" AND a.n_cartella = k.n_cartella and a.pa_tipo_oper= s.pa_tipo_oper and a.n_progetto = s.n_progetto " +
						" AND a.cod_obbiettivo= s.cod_obbiettivo and a.n_intervento =  s.n_intervento " +
						" AND m.tipocura = '" +Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE +"' " +
						" AND a.n_cartella = m.n_cartella AND p.id_skso = m.id_skso AND a.pi_prest_cod = z.prest_cod " +
						" AND a.pi_prest_cod = p.prest_cod AND a.pi_data_inizio >= p.pai_data_inizio  AND a.pi_freq <> '-1' " +
						" AND m.n_cartella = s.n_cartella and m.id_skso = k.id_skso and a.pi_data_inizio <= sysdate " +
						" and (k.pr_data_chiusura is null AND k.pr_motivo_chiusura IS NULL AND k.data_presa_carico_skso IS NOT NULL ) ";
				query += existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare, "m.data_inizio");
				query+=" UNION ";
				query += " select m.cod_zona, m.cod_distretto as cod_distretto, trim("+
						numeroFonte+") as fonte,"+
						" trim(a.pa_tipo_oper) AS tipo_operatore ";
				if (!groupBy){
					query +=", 0 as fonte_dettaglio "+
	//					", m.id_skso as fonte_dettaglio "+
						", c.n_cartella, c.cognome, c.nome,"+
						" null as cod_med, o.cod_presidio as pr_mmg_altro,"+
						" null as tipocura, m.data_richiesta,"+
						" m.id_skso as id_richiesta, s.pa_operatore AS cod_operatore, o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
				}
				query += " FROM pai p, piano_accessi a, piano_assist s, operatori o, rm_skso_proroghe g, prestaz z, rm_skso_mmg m, cartella c, rm_skso k " +
						" WHERE a.n_cartella = p.n_cartella AND a.n_cartella = m.n_cartella AND p.id_skso = m.id_skso " +
						" AND m.tipocura = '" +Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE +"' " +
						" AND a.n_cartella = k.n_cartella and a.pa_tipo_oper= s.pa_tipo_oper and a.n_progetto = s.n_progetto " +
						" AND a.cod_obbiettivo= s.cod_obbiettivo and a.n_intervento =  s.n_intervento AND O.CODICE = S.PA_OPERATORE" +
						" AND c.n_cartella = a.n_cartella AND a.pi_prest_cod = z.prest_cod AND a.pi_prest_cod = p.prest_cod" +
						" AND a.pi_data_inizio >= g.dt_proroga_inizio AND m.n_cartella = s.n_cartella   AND a.pi_freq <> '-1' " +
						" AND M.N_CARTELLA = G.N_CARTELLA AND M.ID_SKSO = G.ID_SKSO" +
						" and m.id_skso = k.id_skso  and (k.pr_data_chiusura is null AND k.pr_motivo_chiusura IS NULL AND k.data_presa_carico_skso IS NOT NULL ) " +
						" and a.pi_data_inizio <= sysdate ";
				query += existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare, "m.data_inizio");
		}else {
			query += " select o.cod_zona, p.coddistr  as cod_distretto, trim("+ numeroFonte+") as fonte,"+
					" trim(a.pa_tipo_oper) AS tipo_operatore ";
		if (!groupBy){
			query +=", null as fonte_dettaglio "+
//					", k.id_skso as fonte_dettaglio "+
					", c.n_cartella, c.cognome, c.nome,"+
					" null as cod_med, o.cod_presidio as pr_mmg_altro,"+
					" null as tipocura, null as data_richiesta,"+
					(isSo ? " k.id_skso ":" k.n_contatto " )+
					" as id_richiesta, s.pa_operatore AS cod_operatore , o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
			query+= " FROM piano_accessi a, piano_assist s, prestaz z, skinf k, operatori o, presidi p, cartella c " +
					" WHERE a.pi_prest_cod = z.prest_cod AND c.n_cartella = k.n_cartella AND o.cod_presidio = codpres " +
					" AND a.n_cartella = s.n_cartella and   a.pa_tipo_oper= s.pa_tipo_oper  and   a.n_progetto = s.n_progetto " +
					" AND a.cod_obbiettivo= s.cod_obbiettivo and a.n_intervento =  s.n_intervento " +
					"  and k.ski_motivo = '" +Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE +"' " +
					" AND o.codice = k.ski_infermiere AND codreg = '" +codReg + "' AND codazsan = '" +codAzSan + 
					 "'  AND a.pa_tipo_oper= '" + GestTpOp.CTS_COD_INFERMIERE +
					 "' AND k.n_cartella = a.n_cartella AND k.ski_data_apertura IN ( SELECT MAX (x.ski_data_apertura) " +
					 " FROM skinf x WHERE x.n_cartella = k.n_cartella AND x.ski_data_apertura <= a.pi_data_inizio) " +
					 " AND k.ski_data_uscita is null  AND a.pi_freq <> '-1' " +
					 " and a.pi_data_inizio <= sysdate ";
//			query += existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare);
			query += existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare, "k.ski_data_apertura");
			query+=" UNION ";
		query += " select o.cod_zona, p.coddistr as cod_distretto, trim("+numeroFonte+") as fonte,"+
					" trim(a.pa_tipo_oper) AS tipo_operatore ";
		if (!groupBy){
			query +=", null as fonte_dettaglio "+
					", c.n_cartella, c.cognome, c.nome,"+
					" null as cod_med, o.cod_presidio as pr_mmg_altro,"+
					" null as tipocura, null as data_richiesta,"+
					(isSo ? " k.id_skso ":" k.n_contatto " )+
					" as id_richiesta, s.pa_operatore AS cod_operatore , o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
		}
			query +=" FROM piano_accessi a, piano_assist s, prestaz z, skfis k, operatori o, presidi p, cartella c " +
					" WHERE a.pi_prest_cod = z.prest_cod AND c.n_cartella = k.n_cartella AND o.cod_presidio = codpres" +
					" AND a.n_cartella = s.n_cartella and   a.pa_tipo_oper= s.pa_tipo_oper  and   a.n_progetto = s.n_progetto " +
					" AND a.cod_obbiettivo= s.cod_obbiettivo and a.n_intervento =  s.n_intervento " +
					" AND k.skf_motivo = '" +CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE +"' " +
					" AND o.codice = k.skf_fisiot AND codreg = '" +codReg + "' AND codazsan = '" +codAzSan + 
					"' AND a.pi_prest_cod = z.prest_cod AND a.pa_tipo_oper = '" + GestTpOp.CTS_COD_FISIOTERAPISTA 
					+"' AND k.n_cartella = a.n_cartella  " +
							" AND k.skf_data IN (SELECT MAX (x.skf_data) FROM skfis x WHERE x.n_cartella = k.n_cartella" +
							" AND x.skf_data <= a.pi_data_inizio) AND k.skf_data_chiusura is null  AND a.pi_freq <> '-1' " +
							" and a.pi_data_inizio <= sysdate " ;
			query += existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare, "k.skf_data");
			query+=" UNION ";
				query += " select o.cod_zona, p.coddistr as cod_distretto, trim("+
						numeroFonte+") as fonte,"+
							" trim(a.pa_tipo_oper) AS tipo_operatore ";
				if (!groupBy){
					query +=", null as fonte_dettaglio "+
							", c.n_cartella, c.cognome, c.nome,"+
							" null as cod_med, o.cod_presidio as pr_mmg_altro,"+
							" null as tipocura, null as data_richiesta,"+
							(isSo ? " k.id_skso ":" k.n_contatto " )+
							"  as id_richiesta, s.pa_operatore AS cod_operatore , o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
				}
					query+=" FROM piano_accessi a, piano_assist s, prestaz z, skmedico k,operatori o,presidi p,cartella c " +
					" WHERE a.pi_prest_cod = z.prest_cod AND c.n_cartella = k.n_cartella AND o.cod_presidio = codpres " +
					" AND a.n_cartella = s.n_cartella and   a.pa_tipo_oper= s.pa_tipo_oper  and   a.n_progetto = s.n_progetto " +
					" AND a.cod_obbiettivo= s.cod_obbiettivo and a.n_intervento =  s.n_intervento " +
					" AND k.skm_motivo = '" +Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE +"' " +
					" AND o.codice = k.skm_medico AND codreg = '" +codReg + "' AND codazsan = '" +codAzSan + "' " +
					" AND a.pi_prest_cod = z.prest_cod AND a.pa_tipo_oper = '" + GestTpOp.CTS_COD_MEDICO + 
					"' AND k.n_cartella = a.n_cartella AND k.skm_data_apertura IN (" +
					" SELECT MAX (x.skm_data_apertura) FROM skmedico x WHERE x.n_cartella = k.n_cartella " +
					" AND x.skm_data_apertura <= a.pi_data_inizio) AND k.skm_data_chiusura is null AND a.pi_freq <> '-1' " +
					" and a.pi_data_inizio <= sysdate ";
			query += existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare, "k.skm_data_apertura");
			query+=" UNION ";
			query += " select o.cod_zona, p.coddistr as cod_distretto, trim("+
					numeroFonte+") as fonte,"+
						" trim(z.prest_tipo) AS tipo_operatore ";
			if (!groupBy){
				query +=", null as fonte_dettaglio "+
						", c.n_cartella, c.cognome, c.nome,"+
						" null as cod_med, o.cod_presidio as pr_mmg_altro,"+
						" null as tipocura, null as data_richiesta,"+
						(isSo ? " k.id_skso ":" k.n_contatto " )+
						" as id_richiesta, s.pa_operatore AS cod_operatore , o.cod_presidio as " + CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD;
			}
			query +=" FROM piano_accessi a, piano_assist s, prestaz z, skfpg k,operatori o,presidi p,cartella c " +
			" WHERE a.pi_prest_cod = z.prest_cod AND c.n_cartella = k.n_cartella AND o.cod_presidio = codpres " +
			" AND a.n_cartella = s.n_cartella and   a.pa_tipo_oper= s.pa_tipo_oper  and   a.n_progetto = s.n_progetto " +
			" AND a.cod_obbiettivo= s.cod_obbiettivo and a.n_intervento =  s.n_intervento " +
			" AND k.skfpg_motivo = '" +CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE +"' " +
			" AND o.codice = k.skfpg_referente AND codreg = '" +codReg+ "' AND codazsan = '" +codAzSan + 
			"' AND k.skfpg_tipo_operatore = z.prest_tipo and " +
			" (a.pa_tipo_oper <> '" +GestTpOp.CTS_COD_INFERMIERE +
			"' and a.pa_tipo_oper <> '" +GestTpOp.CTS_COD_MEDICO+
			"' and a.pa_tipo_oper <> '" +GestTpOp.CTS_COD_FISIOTERAPISTA+"' )  " +
			" and k.n_cartella = a.n_cartella and k.skfpg_data_apertura in (  SELECT MAX (x.skfpg_data_apertura)" +
			" FROM skfpg x  WHERE x.n_cartella = k.n_cartella AND x.skfpg_data_apertura <= a.pi_data_inizio ) " +
			" AND k.skfpg_data_uscita is null  AND a.pi_freq <> '-1'  and a.pi_data_inizio <= sysdate ";
			query+=existsIntervento(pregressoPrestazioni, conNumeroAccessi, numAccessiContare, "k.skfpg_data_apertura");
		}	
			return query;
	}

	private static String existsIntervento(int pregressoPrestazioni, boolean interventoNumerico, int conNumeroAccessi, String campoData) {
		String query = "";
		if (interventoNumerico) {
			query = "  AND " +conNumeroAccessi+ " = ( SELECT count(*) ";
		} else {
			query = " AND NOT EXISTS ( SELECT 1 ";
		}
		query += " FROM interv vx, intpre px "
				+ " WHERE vx.int_anno = px.pre_anno AND vx.int_contatore = px.pre_contatore "
				+ " AND vx.int_cartella = a.n_cartella " +
				/* " px.pre_cod_prest = a.pi_prest_cod " + */
//				" AND vx.int_data_prest >=  m.data_inizio AND " +
				" AND vx.int_data_prest >= " +campoData+	
				" AND vx.int_data_prest <= sysdate "
				+ (pregressoPrestazioni > 0 ? " AND vx.int_data_prest >= sysdate - " + pregressoPrestazioni : " ")
				+ " ) ";
		return query;
	}	
	
	

	private ISASRecord decodificaISASRecord(ISASConnection dbc, ISASRecord dbr, boolean aggiungiDettagliScheda,
			Hashtable<String, String> tipoCuraDescr, Hashtable<String, String> tipoOperatoreDescrizione, String codReg,
			String codAzSan) throws Exception {
		String nomeMetodo = ver + "decodificaISASRecord";
		try {

			if (dbr != null) {
				//decodifica medico MMG
				String cod_med_descr = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + dbr.get("cod_med"),
						"nvl(trim(mecogn),'') ||' '  ||nvl(trim(menome),'')", "cod_med_descr");
				if (cod_med_descr == null || cod_med_descr.equals("")) {
					cod_med_descr = "";
					if (dbr.get("pr_mmg_altro") != null) {
						cod_med_descr = (String) dbr.get("pr_mmg_altro");
					}
				}
				dbr.put("cod_med_descr", cod_med_descr);

				String tipoFonte = ISASUtil.getValoreStringa(dbr, "fonte");
				String tipo_operatore_descr = "";
				String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
				String idSkso = ISASUtil.getValoreStringa(dbr, "id_richiesta");
				//decodifica tipo operatore
				if (ISASUtil.valida(tipoFonte)) {
					String tipoOperatore = ISASUtil.getValoreStringa(dbr, "tipo_operatore");
					int numFonte = Costanti.recuperaFonte(tipoFonte);
					switch (numFonte) {
					case CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI:
						tipo_operatore_descr = ManagerOperatore.decodificaTipoOperatore(dbc, tipoOperatore,
								CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE);
						break;
					case CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA:
					case CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE:
						tipo_operatore_descr = ManagerOperatore.decodificaTipoOperatore(dbc, tipoOperatore,
								CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE_PRIMA_VISITA);
						break;
					case CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI:
						tipo_operatore_descr = ISASUtil.getValoreStringa(tipoOperatoreDescrizione, tipoOperatore);
						break;
//					case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI:
					case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1:
					case CostantiSinssntW.CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA:
						tipo_operatore_descr = ISASUtil.getValoreStringa(tipoOperatoreDescrizione, tipoOperatore);
						break;
					default:
						break;
					}
				}
				dbr.put(CostantiSinssntW.CTS_LST_OPERATORE_DESCR, tipo_operatore_descr);

				String codOperatore = ISASUtil.getValoreStringa(dbr, "cod_operatore");
				if (ISASUtil.valida(codOperatore)) {
					//decodifica operatore
					dbr.put(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE, ISASUtil.getDecode(dbc, "operatori",
							"codice", codOperatore, "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",
							"cod_operatore_descr"));

				} else if (Costanti.recuperaFonte(tipoFonte) == CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI) {
//					String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
//					String idSkso = ISASUtil.getValoreStringa(dbr, "id_richiesta");
					String tipoOperatore = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.TIPO_OPERATORE);

					RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
					String operatoreDescr = rmSkSOOpCoinvoltiEJB.recuperaCognomeNomeOperatore(dbc, nCartella, idSkso,
							tipoOperatore);
					dbr.put(CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE, operatoreDescr);
				}
				
				if (Costanti.recuperaFonte(tipoFonte) == CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA) {
					String tipoCura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_TIPOCURA);
					if (ISASUtil.valida(tipoCura) && tipoCura.equals(CostantiSinssntW.CTS_COD_CURE_PRESTAZIONALI)) {
						RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
						ISASRecord dbrRmSkso = null;
						try {
							Hashtable<String, String> dati = new Hashtable<String, String>();
							dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
							dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
							dbrRmSkso = rmSkSOEJB.selectSkValCorrente(dbc, dati);
						} catch (Exception e) {
							LOG.error(nomeMetodo + " Dati scheda SO non recuperati correttamente ", e);
						}
						if (dbrRmSkso != null) {
							dbr.put(CostantiSinssntW.CTS_RMSKSO_ADP,
									ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_RMSKSO_ADP));
							dbr.put(CostantiSinssntW.CTS_RMSKSO_AID,
									ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_RMSKSO_AID));
							dbr.put(CostantiSinssntW.CTS_RMSKSO_ARD,
									ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_RMSKSO_ARD));
							dbr.put(CostantiSinssntW.CTS_RMSKSO_VSD,
									ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_RMSKSO_VSD));
						}
					} else {
						LOG.trace(nomeMetodo + " non sono nel caso di cure prestazionali ");
					}
				}

				String codDistretto = ISASUtil.getValoreStringa(dbr, "cod_distretto");
				//decodifica distretto
				String distretti = ISASUtil.getDecode(dbc, "distretti", "cod_distr", codDistretto, "des_distr");
				//dbr.put("cod_distretti_descr",ISASUtil.getDecode(dbc,"distretti","cod_distr",""+dbr.get("cod_distretto"),"cod_distretti_descr"));
				dbr.put("cod_distretti_descr", distretti);
				/*
				if (tipoFonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI + "")) {
					String codSede = ISASUtil.getValoreStringa(dbr, "pr_mmg_altro");
					dbr.put(CostantiSinssntW.CTS_DESCRIZIONE_SEDE, recuperaSede(dbc, codSede, codReg, codAzSan));
				}
				*/
				//decodifica zona
				String zona = ISASUtil.getDecode(dbc, "zone", "codice_zona", "" + dbr.get("cod_zona"),
						"descrizione_zona");
				//dbr.put("cod_zona_descr",ISASUtil.getDecode(dbc,"zone","codice_zona",""+dbr.get("cod_zona"),"cod_zona_descr"));
				dbr.put("cod_zona_descr", zona);

				if (tipoFonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+"")){
					String tipoCaso = ISASUtil.getValoreStringa(dbr, "pr_mmg_altro"); // ho usato questo campo per passare info di tipo_caso
					if (pht2DoMotivoDimissione == null){
						Pht2TabBaseEJB pht2TabBaseEJB = new Pht2TabBaseEJB();
						pht2DoMotivoDimissione = pht2TabBaseEJB.caricaDaPht2Tabase(dbc, Costanti.PHT_TABASE_GEN_TIP_CASO);
					} 
					String motivoDimissione= ISASUtil.getValoreStringa(pht2DoMotivoDimissione, tipoCaso);
					dbr.put(CostantiSinssntW.CTS_MOTIVO_DIMISSIONI_DO, motivoDimissione);
				}
				
				if (tipoFonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI+"")){
					LOG.debug(nomeMetodo + " recupero l'ultima data richiesta per la valutazione ");
					EsitiValutazioniUviEJB esitiValutazioniUviEJB = new EsitiValutazioniUviEJB();
					try {
						ISASRecord dbrEsitiValutazione = esitiValutazioniUviEJB.recuperaUltimaValutazione(nCartella,
								idSkso, dbc);
						String dataRichiestaUltimaValutazione = ISASUtil.getValoreStringa(dbrEsitiValutazione,
								CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE);
						if (ManagerDate.validaData(dataRichiestaUltimaValutazione)) {
							dbr.put(CostantiSinssntW.RICH_MMG_DATA_RICHIESTA, dbrEsitiValutazione.get(CostantiSinssntW.CTS_DT_PROSSIMA_VALUTAZIONE));
						}
					} catch (Exception e) {
						LOG.error(nomeMetodo +" Errore ", e);
					}
				}
				
				//Calcolo giorni passati per mostrare alert
				addInfoCalcoloGGPerAlert(dbc, dbr);

				/* mostrare il tipo di cura */
				String tipoCura = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_TIPOCURA);
				String tipoCuraDescrizione = ISASUtil.getValoreStringa(tipoCuraDescr, tipoCura);
				dbr.put(CostantiSinssntW.CTS_TIPOCURA_DESCR, tipoCuraDescrizione);

				if (aggiungiDettagliScheda) {

				}
			}
			//			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT[dbr,"+aggiungiDettagliScheda+"]");
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

	private Vector<ISASRecord> decodificaVectorISASRecord(ISASConnection dbc, Vector<ISASRecord> vdbr, String codReg,
			String codAzSan) throws Exception {
		String nomeMetodo = this.getClass().getName() + ".decodificaVectorISASRecord";
		try {
			int elementi = (vdbr != null ? vdbr.size() : 0);   
			Hashtable<String, String> descrizioneTipoOperatore = ManagerOperatore.loadTipiOperatori(dbc);
			Hashtable<String, String> tipoCuraDescrizione = ManagerDecod.caricaDaTabVoci(dbc,
					CostantiSinssntW.TAB_VAL_TIPOCURA);
			for (int i = 0; i < vdbr.size(); i++) {
				LOG.trace(nomeMetodo + "\n Esamino>" + i + "/" + elementi);
				Object obj = vdbr.get(i);
				if (obj instanceof ISASRecord) {
					ISASRecord dbr = (ISASRecord) vdbr.get(i);
					dbr = (ISASRecord) vdbr.elementAt(i);
					dbr = decodificaISASRecord(dbc, dbr, false, tipoCuraDescrizione, descrizioneTipoOperatore, codReg,
							codAzSan);
				}
			}
			LOG.info(nomeMetodo + " -  Metodo eseguito INPUT[" + vdbr.size() + "] OUTPUT[" + vdbr.size() + "]");
			return vdbr;
		} catch (Exception e) {
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		}
	}

	private void addInfoCalcoloGGPerAlert(ISASConnection dbc, ISASRecord dbr) {
		String nomeMetodo = this.getClass().getName() + ".addInfoCalcoloGGPerAlert()";
		try {
			java.sql.Date data_richiesta = (java.sql.Date) dbr.get("data_richiesta");
//			String fonte = ((Integer) dbr.get("fonte")).toString();
			String fonte = ISASUtil.getValoreStringa(dbr,"fonte");
			DataWI dtRichiesta = new DataWI(data_richiesta);
			DataWI dtOdierna = new DataWI();
			if (dtRichiesta.getString(1) != null) {
				int numGGpassati = dtOdierna.contaGgTra(dtRichiesta.getString(1));
				int num_gg_alert1 = recuperaConfNumGGAlert(dbc, fonte, ListaAttivitaGridCtrl.LIVELLO_ALERT1);
				int num_gg_alert2 = recuperaConfNumGGAlert(dbc, fonte, ListaAttivitaGridCtrl.LIVELLO_ALERT2);
				if (num_gg_alert1 > 0 && numGGpassati > num_gg_alert1)
					dbr.put(ListaAttivitaGridCtrl.LIVELLO_ALERT, ListaAttivitaGridCtrl.LIVELLO_ALERT1);
				if (num_gg_alert2 > 0 && numGGpassati > num_gg_alert2)
					dbr.put(ListaAttivitaGridCtrl.LIVELLO_ALERT, ListaAttivitaGridCtrl.LIVELLO_ALERT2);
			}
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
					chiaveCorr = (String) dbrCorr.get("tipo_fonte") + "_" + ListaAttivitaGridCtrl.LIVELLO_ALERT1;
					this.hGiorniPerAlert.put(chiaveCorr, (Integer) dbrCorr.get("num_gg_alert_liv1"));
					chiaveCorr = (String) dbrCorr.get("tipo_fonte") + "_" + ListaAttivitaGridCtrl.LIVELLO_ALERT2;
					this.hGiorniPerAlert.put(chiaveCorr, (Integer) dbrCorr.get("num_gg_alert_liv2"));
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
					// la richiesta Ã¨ giÃ  stata elaborata e quindi va rimossa
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
		try {
			dbc = super.logIn(myLogin);
			//			Vector vdbr = recuperaDatiLista(dbc, h, true);
			String codReg = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_REGIONE);
			String codAzSan = ISASUtil.getValoreStringa(h, ManagerProfile.CODICE_USL);
			Vector<ISASRecord> vdbr = recuperaDatiLista(dbc, h, false, false, CTS_QUERY_CONTEGGIO, codReg, codAzSan);
			//			Hashtable<String, Integer> datiFonte =contaDatiFonte(vdbr); 

			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(nomeMetodo + " - Exception:" + e.getMessage());
			throw newEjbException("Errore in " + nomeMetodo + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(nomeMetodo, dbc);
		}
	}

	public boolean spostaUtentePht2(myLogin myLogin, Hashtable<String, String> dati) throws SQLException {
		String punto = ver + "spostaUtentePht2 ";
		LOG.error(punto + " inizio con dati>" + dati);
		boolean aggiornamentoOk = false;
		ISASConnection dbc = null;
		ISASRecord dbrPht2 = null;
		String operatoreCheModifica = "";
		String distrettoDaAssegnare = "";
		try {
			dbc = super.logIn(myLogin);
			String idScheda = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_ID_SCHEDA_PHT2);
			String query = "select x.* from pht2_generale x where id_scheda = " + idScheda;
			LOG.trace(punto + " query>>" + query);
			dbrPht2 = dbc.readRecord(query);
			if (dbrPht2 != null) {
				operatoreCheModifica = myLogin.getUser();
				distrettoDaAssegnare = ISASUtil.getValoreStringa(dati,
						AttribuzioneDistrettoPht2Ctrl.CTS_DISTRETTO_ASSEGNATO);
				dbrPht2.put(CTS_PHT2_PRECEDENTE_DISTRETTO, ISASUtil.getValoreStringa(dbrPht2, CTS_PHT2_DISTRETTO));
				dbrPht2.put(CTS_PHT2_PRECEDENTE_OPERATORE, ISASUtil.getValoreStringa(dbrPht2, CTS_PHT2_OPERATORE));
				dbrPht2.put(CTS_PHT2_DISTRETTO, distrettoDaAssegnare);
				dbrPht2.put(CTS_PHT2_OPERATORE, operatoreCheModifica);
				LOG.trace(punto + " dati che aggiorno>" + dbrPht2.getHashtable());
				dbc.writeRecord(dbrPht2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOG.error(punto + " - Exception:" + e.getMessage());
			throw newEjbException("Errore in " + punto + ": " + e.getMessage(), e);
		} finally {
			logout_nothrow(punto, dbc);
		}
		aggiornamentoOk = (dbrPht2 != null);

		return aggiornamentoOk;
	}

}

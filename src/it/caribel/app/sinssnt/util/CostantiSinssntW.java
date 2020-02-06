package it.caribel.app.sinssnt.util;

public class CostantiSinssntW extends Costanti{
	public final static String PATH_ZUL = "/web/ui/sinssnt/";
//	public final static String N_CARTELLA = Costanti.N_CARTELLA;//"n_cartella";
//	public final static String N_CONTATTO = Costanti.N_CONTATTO;//"n_contatto";
	public final static String CTS_SKFPG_TIPO_OPERATORE = "skfpg_tipo_operatore";
	public static final String PR_DATA = "pr_data";
	public static final String MMGPRPR_DATA = "mmgpr_data";
	public static final String MMG_ADI_SKADI_DATA = "skadi_data";
	public static final String MMG_ADI_SKADP_DATA = "skadp_data";
	public static final String MMG_ADI_SKADR_DATA = "skadr_data";
//	public static final String CTS_DATA_DIAG = Costanti.CTS_DATA_DIAG;//"data_diag";   
	public static final String RICH_MMG_DATA_RICHIESTA = "data_richiesta";
	public static String STATO = "stato";
	public static String CTS_DIARIO = "diarioForm";
	public static String NUOVA_SCHEDA_MMG = "new_scheda";
	/* segreteria organizzativa */
//	public static String CTS_ID_SKSO = Costanti.CTS_ID_SKSO;//"id_skso"; // TODO usato anche in costanti zk da rimuovere  
//	public static String CTS_SO_TIPO_OPERATORE = Costanti.CTS_SO_TIPO_OPERATORE;//"tipo_operatore";
//	public static String CTS_SO_ID_PROROGA = Costanti.CTS_SO_ID_PROROGA;//"id_proroga";
//	public static String CTS_SO_ID_SOSPENSIONE =Costanti.CTS_SO_ID_SOSPENSIONE;//"id_sospensione";
//	public static String CTS_DT_PROROGA_INIZIO = Costanti.CTS_DT_PROROGA_INIZIO;//"dt_proroga_inizio";
//	public static String CTS_DT_PROROGA_FINE = Costanti.CTS_DT_PROROGA_FINE;//"dt_proroga_fine";
//	
	public static String CTS_DT_SOSPENSIONE_INIZIO = "dt_sospensione_inizio";
	public static String CTS_DT_SOSPENSIONE_FINE = "dt_sospensione_fine";
	
	public static String CTS_CAREGIVER_COGNOME = "caregiver_cognome";
	public static String CTS_CAREGIVER_NOME = "caregiver_nome";
	
//	public static String CTS_PR_TIPO = Costanti.CTS_PR_TIPO;//"pr_tipo";
//	public static String CTS_PR_OPERATORE = Costanti.CTS_PR_OPERATORE;//"pr_operatore";
//	public static String CTS_PR_RESPONSABILE = Costanti.CTS_PR_RESPONSABILE;//"pr_responsabile";
//	public static String CTS_PR_PARTECIPA = Costanti.CTS_PR_PARTECIPA;//"pr_partecipa";
	
	public static String CTS_PR_OPERATORE_COGNOME = "pr_operatore_cognome";
	public static String CTS_PR_OPERATORE_NOME = "pr_operatore_nome";
	
	
	
	/* richieste mmg */
//	public static String CTS_ID_RICH =Costanti.CTS_ID_RICH;//"id_rich";
//	public static String CTS_ID_RICHIESTA = Costanti.CTS_ID_RICHIESTA;// "id_richiesta";
	public static String CTS_STATO_RICHIESTA="stato";
	//public static Object CTS_STATO_RICHIESTA_ATTIVATA = "0"; // In attesa //costante ridefinita in RmRichiesteMMGEJB 
	public static String CTS_ID_SKSO_MMG = "id_scheda_so";
	
//	public static String CTS_SKSO_MMG_DATA_FINE = Costanti.CTS_SKSO_MMG_DATA_FINE;//"data_fine";
//	public static String CTS_SKSO_MMG_DATA_INIZIO = Costanti.CTS_SKSO_MMG_DATA_INIZIO;//"data_inizio";
	public static String CTS_SKSO_MMG_DATA_PROTOCOLLO = "data_protocollo";
	public static String CTS_SKSO_MMG_NUMERO_PROTOCOLLO = "num_protocollo";
	public static String PR_MOTIVO_CHIUSURA = "pr_motivo_chiusura";
	public static String PR_MOTIVO_CHIUSURA_AMMINISTRATIVA = "12";
	
	public static String CTS_PERIODO_CONTATTO_DATA_INIZIO = "per_data_inizio";
	public static String CTS_PERIODO_CONTATTO_DATA_FINE = "per_data_fine";
	public static String CTS_PERIODO_CONTATTO_ESTREMO_DATA_INIZIO = "per_estremo_data_inizio";
	public static String CTS_PERIODO_CONTATTO_ESTREMO_DATA_FINE = "per_estremo_data_fine";
	
	public static String CTS_INT_CONTATTO = "int_contatto";
	public static String CTS_INT_CARTELLA = "int_cartella";
	
	
	
//	public static String CTS_DATA_PRESA_CARICO_SKSO = Costanti.CTS_DATA_PRESA_CARICO_SKSO;//"data_presa_carico_skso";
	public static String CTS_DATA_PRESA_CARICO = "data_presa_carico";
	public static String CTS_STATO ="stato";
//	public static String STATO_RICH_MMG_RICHIESTA="1"; //costante ridefinita in RmRichiesteMMGEJB 

//	public static String CTS_RECORD_FITTIZIO = Costanti.CTS_RECORD_FITTIZIO;//"rec_fitt";
//	public static String CTS_RECORD_FITTIZIO_SI = Costanti.CTS_RECORD_FITTIZIO_SI;//"SI";

	public static String TAB_VAL_SO_MOTIVO_CHIUSURA = "VALPCMCH";
//	public static String TAB_VAL_SO_TIPO_OPERATORE = Costanti.TAB_VAL_SO_TIPO_OPERATORE;// "SOOPCOIN";
//	public static String TAB_VAL_SO_MOTIVO_SOSPENSIONE = Costanti.TAB_VAL_SO_MOTIVO_SOSPENSIONE;//"FTMOTSOS";
//	public static String TAB_VAL_MOTIVO_CONCLUSIONE_FLUSSI_SIAD = Costanti.TAB_VAL_MOTIVO_CONCLUSIONE_FLUSSI_SIAD;//"FLMOTCON";
//	public static String TAB_VAL_SO_TIPO_OPERATORE_PRIMA_VISITA = Costanti.TAB_VAL_SO_TIPO_OPERATORE_PRIMA_VISITA;//"SOOPPRVS"; // TIPO OPERATORE PRIMA VISITA 
//	public static String TAB_VAL_MOTIVO =Costanti.TAB_VAL_MOTIVO;// "MOTIVO";
//	public static String TAB_VAL_FREQUENZA=Costanti.TAB_VAL_FREQUENZA;//"FREQAC"; //FREQUENZA PAI
	public static String TAB_VAL_TIPO_OPERATORE_CHIUSURA  = "CONTCHIU";
	public static String TAB_VAL_TIPO_OPERATORE_REFERENTE = "REFOPCHN";
	
//	public static String TAB_VAL_SEGNALANTE = Costanti.TAB_VAL_SEGNALANTE;//"FMRICH";
//	public static String TAB_VAL_RMBADA = Costanti.TAB_VAL_RMBADA;//"RMBADA";
//	public static String VOCI_TABELLE_STACIV = Costanti.VOCI_TABELLE_STACIV;//"STACIV";
//	public static String TAB_VAL_AUTOSUFFICIENTE =Costanti.TAB_VAL_AUTOSUFFICIENTE;//  "RMAUTSUF";
//	public static String TAB_VAL_LIVELLO_PRESA_CARICO = Costanti.TAB_VAL_LIVELLO_PRESA_CARICO;//"LVPRESCR";
//	public static String TAB_VAL_OPERATORI_UVI =  Costanti.TAB_VAL_OPERATORI_UVI;//"OPPREUVI";
	public static String TAB_VAL_ICHIUS = "ICHIUS";

//	public static String CTS_MEDICO_DESCRIZIONE = Costanti.CTS_MEDICO_DESCRIZIONE;//"medico_desc";
//	
//	public static String TAB_VAL_TIPOCURA = Costanti.TAB_VAL_TIPOCURA;//"RMTPCURA";
////	collegate a tab_voci TAB_VAL_TIPOCURA
//	public static final String CTS_COD_CURE_DOMICILIARI_INTEGRATE = Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE;//"1";
//	public static final String CTS_COD_CURE_PRESTAZIONALI = Costanti.CTS_COD_CURE_PRESTAZIONALI;//"2";
//	public static final String CTS_COD_CURE_RESIDENZIALI = Costanti.CTS_COD_CURE_RESIDENZIALI;//"3";
	public static final String CTS_L_ASSISTITO_FONTE_TIPO_CURA_NON_SPECIFICATA ="4";
	
//	public static String CTS_TIPOCURA = Costanti.CTS_TIPOCURA;// "tipocura";
	public static String CTS_TIPOCURA_DESCR = "tipocura_descr";
	
	public static final String CTS_COD_MED = "cod_med";
//	public static final String CTS_PR_PRESENZA = Costanti.CTS_PR_PRESENZA;//"pr_presenza";
	public static final String CTS_NESSUNO_ATTIVI = ""; 
//	public static final String CTS_OP_COINVOLTI_DB_NAME_ZONA = "cod_zona"; // TODO sono presenti in costanti 
//	public static final String CTS_SO_DB_NAME_ZONA = Costanti.CTS_SO_DB_NAME_ZONA;//"cod_zona";
//	public static final String CTS_SO_DB_NAME_DISTRETTO = Costanti.CTS_SO_DB_NAME_DISTRETTO;//"cod_distretto";
//	public static final String CTS_SO_DB_NAME_PRESIDIO = Costanti.CTS_SO_DB_NAME_PRESIDIO;//"cod_presidio";
	public static final String CTS_DB_NAME_PRESIDI ="cod_presidio"; // TODO sono presenti in costanti 
	public static final String CTS_OP_INSERIRE_MMG = "op_mmg_ins";
	public static final String CTS_OP_INSERIRE_PV = "op_prima_visita";
	public static final String CTS_OP_INSERIRE_GENERICO = "op_generico";
	public static final String CTS_COD_OP_CORRENTE = "cod_op_corrente";
//	public static final String CTS_OP_COINVOLTI = Costanti.CTS_OP_COINVOLTI;//"mmgpls_op_coinvolti_pr";
//	public static final String CTS_OP_DATA_FINE_PIANO = Costanti.CTS_OP_DATA_FINE_PIANO;//"dt_fine_piano";

	public static String CTS_DISTRETTI_VOCE_TUTTI = "dst_voc";
	
//	public static String CTS_NO = Costanti.CTS_NO;//"NO";
//	public static String CTS_SI = Costanti.CTS_SI;//"SI";
//	public static String CTS_N = Costanti.CTS_N;//"N";
	public static String CTS_S = "S";
	
	public static String CTS_TIPO_UTENTE_ANZIANO = "1";
	public static String CTS_TIPO_UTENTE_MINORE = "2";
	public static String CTS_TIPO_UTENTE_ADULTO = "6";
	public static String CTS_TIPO_UTENTE_MALATO_TERMINALE = "7";

	public static String CTS_PV_TIPO_OPERATORE = "pv_tp_operatore";
	public static String CTS_PV_COD_OPERATORE ="pv_cod_operatore";
	public static String CTS_PV_DT_VISITA ="pv_dt_visita";

//	public static String CTS_FLAG_STATO_VISTA_DA_SO = Costanti.CTS_FLAG_STATO_VISTA_DA_SO;//"vista_da_so";
	
//	public static String CTS_FLAG_STATO_FATTA = Costanti.CTS_FLAG_STATO_FATTA;//"1";
//	public static String CTS_FLAG_STATO_VISTA = Costanti.CTS_FLAG_STATO_VISTA;//"2";
//	public static String CTS_FLAG_STATO_RIMOSSA =Costanti.CTS_FLAG_STATO_RIMOSSA;//"3";
//	 1= fatta --> in lista x SO;
//	 2=Vista---> (la SO ha appreso che la visita è stata fatta);
//	 3=Annullata (rimossa)

	 
	
	
//	public static String CTS_OPERATORE_PRESIDIO = Costanti.CTS_OPERATORE_PRESIDIO;//"presidio_op";
//
//	public static int INT_TIME_OUT = Costanti.INT_TIME_OUT;//5000; // TODO PORTATO IN CONSTATI 

	
	public static String CTS_SEGNALAZIONE_DATA_SEGNALAZIONE = "data_segnalazione";
//	public static String CTS_SEGNALAZIONE_PROGRESSIVO = "progressivo";
	public static String CTS_SEGNALAZIONE_TIPO_OPERATORE = "tipo_operatore";
	public static String CTS_SEGNALAZIONE_COD_OPERATORE = "cod_operatore";
	public static String CTS_SEGNALAZIONE_VISTA_DA_SO = "vista_so";
	public static int CTS_SEGNALAZIONE_VISTA_DA_SO_INSERITA_UPDATE = 1;// segnalazione aperta
	public static int CTS_SEGNALAZIONE_VISTA_DA_SO_VISTA = 2;// segnalazione chiusa
	
	public static String CTS_SEGNALAZIONE_SONO_SO = "sgn_is_so";
	
	
	public static String CTS_IA_DATA_RILEVAZIONE = "data_rilevazione";
	public static String CTS_IA_PROGRESSIVO = "progressivo";
	public static String CTS_IA_PRINCIPIO_ATTIVO = "p_attivo";
//	public static String CTS_RICHIESTA_PREFERENZE_CONSULTAZIONE = "rich_pref_cons";

	public static final Object CTS_VETTORE_DETTAGLIO ="dettVettore";
	public static final String AUTORIZZAZIONE_READONLY = "auto_readOnly";
	public static final String CTS_STAMPA_REPORT ="st_report";
	public static final String CTS_STAMPA_METODO = "st_metodo";
	public static final String CTS_STAMPA_EJB = "st_ejb";
	public static final String CTS_STAMPA_PARAMETRI = "st_parametri";
//	public static final String CTS_STAMPA_BEAN =Costanti.CTS_STAMPA_BEAN;// "st_bean";
	public static final String CTS_SO_STAMPA_VALUTAZIONE = "so_stampa_val";

//	public static final String CTS_ELEMENTI_FONTI_LISTA="elem_list_fon";
//	aggiunte della fonte 20

//	public static final String L_AT_FONTE = "fonte";
//	public static final String CTS_AS_NUMERO = "numero";
//	public static final int LISTA_ATTIVITA_NUMERO_COLONNE = 5;
//	public static final int LISTA_ATTIVITA_LUNGHEZZA_CARATTERI_RIGA = 90;
//	public static final int LISTA_ASSISTITO_NUMERO_COLONNE = 2;

	public static final String CTS_ROW_RED = "topMenuButtonRed";
	public static final String CTS_L_RICHIESTE_PERSONALI_SEDE_MIA = "rich_perso";
	public static final String CTS_L_RICHIESTE_PERSONALI_SEDE_ALTRI = "rich_altri";
	public static final String CTS_APERTURA_CONTATTO_SKSO_PROPOSTA = "dt_proposta_contatto";

	
	public static final String CTS_L_RICHIESTE_DESTINATARI = "filtro_dest";
	
	public static final String CTS_L_ATTIVITA_SEDE_COD = "l_cod_sede";
	public static final String CTS_TIPO_PRESTAZIONE_NON_CENSITA = "--";
	public static final String CTS_TIPO_OPERATORE = "tipo";
	public static final String CTS_OPERATORE_COD_PRESIDO = "cod_presidio";
//	public static final String CTS_FONTI_DA_ESCLUDERE = "ls_fonti_no";
//	public static final String CTS_LST_OPERATORE_DESCRIZIONE = Costanti.CTS_LST_OPERATORE_DESCRIZIONE;//"cod_operatore_descr";

	public static final String CTS_LST_OPERATORE_DESCR = "tipo_operatore_descr";
//	public static final String CTS_CARTELLA_CHIUSA = "cart_chiusa";
//	public static final String CTS_CARTELLA_ATTIVA = "cart_attiva";
	public static final String CTS_L_ASSISTITO_FONTE_CDI =CTS_COD_CURE_DOMICILIARI_INTEGRATE;
	public static final String CTS_L_ASSISTITO_FONTE_CP =CTS_COD_CURE_PRESTAZIONALI;
	public static final String CTS_L_ASSISTITO_FONTE = "fonte";
	public static final String CTS_L_ASSISTITO_DATA_INIZIO = "data_inizio";
	public static final String CTS_L_ASSISTITO_COD_ZONA = "cod_zona";
	public static final String CTS_L_ASSISTITO_COD_DISTRETTO = "cod_distretto";
	public static final String CTS_L_ASSISTITO_N_CONTATTO = "contatto";
	public static final String CTS_L_ASSISTITO_N_CARTELLA = "n_cartella";
	public static final String CTS_L_ASSISTITO_ID_SKSO = "id_skso";
	public static final String CTS_L_ASSISTITO_DATA_PIANO_INIZIO = "data_piano_inizio";
	public static final String CTS_L_ASSISTITO_DATA_PIANO_FINE = "data_piano_fine";
	public static final String CTS_L_ASSISTITO_DATA_PROROGA_INIZIO = "data_proroga_inizio";
	public static final String CTS_L_ASSISTITO_DATA_PROROGA_FINE = "data_proroga_fine";
	public static final String CTS_L_ASSISTITO_DATA_SOSPENSIONE_INIZIO = "data_sospensione_inizio";
	public static final String CTS_L_ASSISTITO_DATA_SOSPENSIONE_FINE = "data_sospensione_fine";
	
//	public static final String CTS_IA_UPDATE = "cs_update";
	public static final String MODALE = "modale";
	public static final String CTS_DT_DIAG = "dt_diag";
//	public static final String CTS_SONO_IN_SO = Costanti.CTS_SONO_IN_SO;//"prv_so_vista";
//
//	public static final String CTS_COD_COMMISSIONE = Costanti.CTS_COD_COMMISSIONE;//"cod_commis_uvm";
//	public static final String CTS_LISTA_ATTIVITA_ORDINAMENTO = "lst_tp_ord";
//	public static final String CTS_LISTA_AO_DATA = "dt_ord";
//	public static final String CTS_LISTA_AO_ASSISTITO = "cn_ord";
//	public static final String CTS_LISTA_AO_FONTE = "fonte";

//	public static final String CTS_FONTE = Costanti.CTS_FONTE;//"fonte";
	public static final int CTS_TIPO_FONTE_RICH_MMG = 1;
//	public static final int CTS_TIPO_FONTE_RICH_MMG_Intero_ = 1;
	public static final int CTS_TIPO_FONTE_COINVOLTI = 2;
	public static final int CTS_TIPO_FONTE_PRIMA_VISITA = 3;
//	public static final int CTS_TIPO_FONTE_PRIMA_VISITA_Intero = 3;
	public static final int CTS_TIPO_FONTE_SO_VISTE = 4;
//	public static final int CTS_TIPO_FONTE_SO_VISTE_Intero = 4;
	public static final int CTS_TIPO_FONTE_RICHIESTA_CHIUSURA = 5;
//	public static final int CTS_TIPO_FONTE_RICHIESTA_CHIUSURA_Intero = 5;
	public static final int CTS_TIPO_FONTE_SEGNALAZIONI = 6;
	public static final int CTS_TIPO_FONTE_VALUTAZIONI_UVI = 7;
	public static final int CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE = 8;
	public static final int CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI = 9;
	/* rimosso questa fonte: non la voglio più */
	public static final int CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_ = 10;
	public static final int CTS_TIPO_FONTE_RICHIESTE_RICOVERO_RSA = 11;
	public static final int CTS_TIPO_FONTE_RICHIESTA_DIMISSIONE_PROTETTA = 12;
	public static final int CTS_TIPO_FONTE_POSTI_DISPONIBILI = 13;
	public static final int CTS_TIPO_FONTE_INTERRUZIONE_DIMISSIONE = 14;
	
	public static final int CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA = 15;
	public static final String CTS_TIPO_FONTE_ACCESSI_SO_CD_IN_SCADENZA = "15.1";
	public static final String CTS_TIPO_FONTE_ACCESSI_SO_CP_IN_SCADENZA = "15.2";
	public static final int CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0= 16;
	public static final int CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1= 17;
//	public static final int CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA = Costanti.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA;//18;  
	public static final int CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA= 19;
	
	public static final String CTS_TIPO_FONTE_DETTAGLIO1 = "1";
	public static final String CTS_TIPO_FONTE_DETTAGLIO2 = "2";
	public static final String CTS_TIPO_FONTE_DETTAGLIO3 = "3";
	public static final String CTS_TIPO_FONTE_DETTAGLIO4 = "4";
	public static final String CTS_TIPO_FONTE_DETTAGLIO5 = "5";
	public static final String CTS_TIPO_FONTE_5_DETTAGLIO_0 = "0";
	public static final String CTS_TIPO_FONTE_5_DETTAGLIO_1 = "1";
	public static final String CTS_TIPO_FONTE_5_DETTAGLIO_2 = "2";
	public static final String CTS_TIPO_FONTE_SEGNALAZIONI_DETTAGLIO ="0";
	public static final String CTS_TIPO_FONTE_VALUTAZIONI_UVI_DETTAGLIO = "0";
	public static final int CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE_DETTAGLIO = 0;
	public static final int CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI_DETTAGLIO = 0;
	public static final int CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_DETTAGLIO = 0;
	
	public static final String CTS_DATA_CONCLUSIONE = "data_conclusione";
	
//	public static final String CTS_OPERATORE_DISTRETTO = Costanti.CTS_OPERATORE_DISTRETTO;//"distretto_op";
//	public static final String CTS_OPERATORE_ZONA = Costanti.CTS_OPERATORE_ZONA;//"zona_op";
//	public static final String CTS_FIGURE_PROFESSIONALI = Costanti.CTS_FIGURE_PROFESSIONALI;//"figprof";
	
	public static final String CTS_CONF_CODICE_REGIONE = "codice_regione";
	public static final String CTS_CONF_CODAZSAN = "codice_usl";

//	public static final String CTS_SO_VERBALE_DB_NAME_ZONA = "cod_zona_verbale";
//	public static final String CTS_SO_VERBALE_DB_NAME_DISTRETTO = "cod_distretto_verbale";
	public static final String CTS_OP_COINVOLTI_DB_NAME_DISTRETTO = "cod_distretto"; // TODO sono presenti in costanti 
	public static final String CTS_OP_COINVOLTI_DB_NAME_ZONA = "cod_zona";
	public static final String CTS_ATTIVI_SOSPENSIONE= "so.conferma.chiusura.sospensioni.msg";
	public static final String CTS_ATTIVI_PROROGHE= "so.conferma.chiusura.proroghe.msg";
	public static final String CTS_ATTIVI_PROROGHE_SOSPENSIONI = "so.conferma.chiusura.proroghe.sospensioni.msg";
//
//	public static final String CTS_COD_DISTRETTO_UVM =Costanti.CTS_COD_DISTRETTO_UVM;// "cod_distretto_uvm";
//	
//	public static final String CTS_PR_DATA_PUAC = Costanti.CTS_PR_DATA_PUAC;//"pr_data_puac";
//	public static final String CTS_PR_DATA_VERBALE_UVM ="pr_data_verbale_uvm";
//	public static final String CTS_ACCESSI_MMG = Costanti.CTS_ACCESSI_MMG;//"accessi_mmg";
//	public static final String CTS_SKM_DATA_APERTURA = Costanti.CTS_SKM_DATA_APERTURA;//"skm_data_apertura";
//	public static final String N_PROGETTO = Costanti.N_PROGETTO;//"n_progetto";
	
	public static final String SKD_DATA = "skd_data";
	public static final String SKR_DATA = "skr_data";   
	public static final String SKF_DATA = "skf_data";   
	public static final String SKFPG_DATA_APERTURA = "skfpg_data_apertura";   
	
	/* */
	public static final String SKT_PROGR = "skt_progr";
	public static final String ASSISTITO_NOME = "ast_nome";
	public static final String ASSISTITO_COGNOME = "ast_cognome";
	public static final String ASSISTITO_CARTELLA_APERTA = "ass_cart_aperta";
//	public static final String ASSISTITO_CARTELLA_CHIUSA = Costanti.ASSISTITO_CARTELLA_CHIUSA;//"ass_cart_chiusa";
//
//	public static final String TIPO_OPERATORE = Costanti.TIPO_OPERATORE;//"tipo_operatore";
//	public static final String COD_OPERATORE = Costanti.COD_OPERATORE;//"cod_operatore";
//	public static final String DT_PRESA_CARICO = Costanti.DT_PRESA_CARICO;//"dt_presa_carico";
	public static final String COD_OPERATORE_REF = "cod_operatore_ref";
	
	
	public static final String TIPO_OPERATORE_ASSISTENTE_SOC = "01";
	public static final String TIPO_OPERATORE_INFERMIERE = "02";
	public static final String TIPO_OPERATORE_MEDICO = "03";
	public static final String TIPO_OPERATORE_FISIOTERAPISTA = "04";
	public static final String TIPO_OPERATORE_AMMINISTRATORE = "05";
	public static final String TIPO_OPERATORE_OSTETRICO = "06";
	public static final String TIPO_OPERATORE_PSICHIATRA = "07";
	public static final String TIPO_OPERATORE_GINECOLOGO = "08";
	public static final String TIPO_OPERATORE_PSICOLOGO = "09";
	public static final String TIPO_OPERATORE_PEDIATRA = "10";
	public static final String TIPO_OPERATORE_DIETISTA = "11";
	public static final String TIPO_OPERATORE_EDUCATORE = "12";
	public static final String TIPO_OPERATORE_FISIOKINESITERAPISTA = "13";
	public static final String TIPO_OPERATORE_LOGOPEDISTA = "14";
	public static final String TIPO_OPERATORE_NEUROPSICHIATRA = "15";
	public static final String TIPO_OPERATORE_OTA = "16";
	public static final String TIPO_OPERATORE_CONSULENTE = "17";
	public static final String TIPO_OPERATORE_NEUROPSICOMOTRICISTA = "18";
	public static final String TIPO_OPERATORE_PSICONCOLOGO = "19";
	public static final String TIPO_OPERATORE_PNEUMOLOGO = "51";
	public static final String TIPO_OPERATORE_MEDICO_CURE_PALLIATIVE = "52";
	public static final String TIPO_OPERATORE_CARDIOLOGO = "53";
	public static final String TIPO_OPERATORE_GERIATRA = "54";
	public static final String TIPO_OPERATORE_MEDICO_CRI = "55";
	public static final String TIPO_OPERATORE_CONSULENTE_LEGALE = "56";
	public static final String TIPO_OPERATORE_MEDIATORE_CULTURALE = "57";
	public static final String TIPO_OPERATORE_MEDICO_SPECIALISTA = "98";
	public static final String TIPO_OPERATORE_MMG = "99";
	
	public static final String SI = "SI";
	public static final String NO = "NO";
	
	public static final String VALORE_COMBO_DEFAULT = ".";
	
	// tipologia del caso
	public static final int CASO_UVM = 0; // caso con valutaz multidim
	public static final int CASO_SAN = 1; // caso semplice sanitario
	public static final int CASO_SOC = 2; // caso semplice sociale
	
	// stato del caso
	public static final int STATO_ATTESA = 0; // caso in attesa di valutaz multidim
	public static final int STATO_ATTIVO = 1; // caso attivo
	public static final int STATO_CONCLU = 2; // caso concluso

	public static final String COD_COM_NASC = "cod_com_nasc";


	public static final String SKI_DATA_APERTURA ="ski_data_apertura";
	public static final String SKI_DATA_USCITA   ="ski_data_uscita";
	public static final String SKI_DTSEGNALAZIONE="ski_dtsegnalazione";
	public static final String SKI_DTPRESACARICO ="ski_dtpresacarico";

	public static final String SKMED_DATA_CHIUSURA ="skm_data_chiusura";
	public static final String SKF_DATA_CHIUSURA ="skf_data_chiusura";
	public static final String SKPG_DATA_CHIUSURA ="skfpg_data_uscita";
	
//	public static final String COD_OBBIETTIVO = Costanti.COD_OBBIETTIVO;// "cod_obbiettivo";
//	public static final String N_INTERVENTO = Costanti.N_INTERVENTO;//"n_intervento";

	public static final String DATA_APERTURA = "data_apertura";
	public static final String DATA_CHIUSURA = "data_chiusura";

//	public static final String PR_DATA_CHIUSURA = Costanti.PR_DATA_CHIUSURA;//"pr_data_chiusura";
//
//	public static final int CONSULTA_RICHIESTA = Costanti.CONSULTA_RICHIESTA;// 0;
	public static final int PRESA_CARICO_RICHIESTA = 1;
	public static final int CAMBIA_PIANO = 2;

//	public static final String ACTION =Costanti.ACTION;// "action";
	public static final String CTS_TUTTI = "TUTTI";

	public static final String DA_SCHEDA_SO = " DA SCHEDA SO";

	public static final String FIRST_ACCESS = "FIRST_ACCESS";

	//public static final String STATO_RICH_MMG_ARCHIVIATO = "3";//costante ridefinita in RmRichiesteMMGEJB 

	//public static final String STATO_RICH_MMG_CONFERMA = "2";//costante ridefinita in RmRichiesteMMGEJB 


//	public static final String STATO_RICHIESTA_CHIUSURA_IN_ATTESA = Costanti.STATO_RICHIESTA_CHIUSURA_IN_ATTESA;//"0";
	public static final String STATO_RICHIESTA_CHIUSURA_CONFERMATA = "1";
	public static final String STATO_RICHIESTA_CHIUSURA_ANNULLATA = "2";

//	public static final String MSG_ECCEZIONE_DIRITTI_MANCANTI = Costanti.MSG_ECCEZIONE_DIRITTI_MANCANTI;//"Mancano i permessi per leggere il record dal database!";
	
	public static final String ESISTE_RICHIESTA_MSG = "La richiesta che si vuole inoltrare risulta già inviata. E' possibile verificarne lo stato da Lista Attività.";

	public static final String FORZA_CHIUSURA = "forzaChiusura";
	public static final String CODUSL = "cod_usl";
	public static final String MOTIVO_CHIUSURA = "motivo_chiusura";
	


	public static final String DT_PRIMA_VISITA = "dt_prima_visita";

//	public static final String COD_OPERATORE_PC = Costanti.COD_OPERATORE_PC;//"cod_operatore_pc";

	public static final String DA_PRIMA_VISITA = "da_pv";

//	public static final String DATACHIUSURAPIANO = Costanti.DATACHIUSURAPIANO;//"dataChiusuraPiano";


	
//	public final static String FLAG_DA_NON_INVIARE = "-1";
//	public final static String FLAG_DA_INVIARE_I = "0";
//	public final static String FLAG_DA_INVIARE_V = "1";
//	public final static String FLAG_IN_CONVALIDA_I = "2";
//	public final static String FLAG_IN_CONVALIDA_V = "3";
//	public final static String FLAG_MOD_IN_CONVALIDA_I = "4";
//	public final static String FLAG_MOD_IN_CONVALIDA_V = "5";
//	public final static String FLAG_ESTRATTO_DEFINITIVO = "6";
//	public final static String STP_ASSISTITI_FLUSSO_SIAD_INVIATO = "report.elenco.assistiti.evento.inviato";
//	public final static String STP_ASSISTITI_FLUSSO_SIAD_NON_INVIATO = "report.elenco.assistiti.evento.non.inviato";

	
	public static final String CTS_STP_REPORT_ASS_APERTE = "st_aperte";
	public static final String CTS_STP_REPORT_ASS_CHIUSE = "st_chiuse";
	public static final String CTS_STP_REPORT_ASS_PAI = "st_pai";
	public static final String CTS_STP_REPORT_ASS_DIARIO = "st_diario";
	public static final String CTS_STP_REPORT_ASS_INTOLLERANZE = "st_intolleranze";
	public static final String CTS_STP_REPORT_ASS_TIPO_UTE = "st_tipo_ute";
	public static final String CTS_STP_REPORT_ASS_SEGNALAZIONE = "st_segnalazione";
	public static final String CTS_STP_REPORT_ASS_INTENSITA = "st_intensita";
	public static final String CTS_STP_REPORT_ASS_ATTIVE = "st_attive";
	public static final String CTS_STP_REPORT_ASS_CONCLUSE = "st_concluse";
	public static final String CTS_STP_REPORT_ASS_SOSPESE = "st_sospese";
	public static final String CTS_STP_REPORT_ASS_PROROGHE = "st_proroghe";
	public static final String CTS_STP_REPORT_ASS_FINE_PIANO = "st_fine_piano";
	public static final String CTS_STP_REPORT_ASS_RIVALUTAZIONE = "st_rivalutazione";
	public static final String CTS_STP_REPORT_COD_MEDICO="cod_medico";
//	public static final String CTS_STP_REPORT_FLAG_SIAD = "flg_siad";
	public static String CTS_STP_REPORT_FLAG_SIAD_CHECK_INVIATI= "flg_siad_check_si";
	public static String CTS_STP_REPORT_FLAG_SIAD_CHECK_NO_INVIATI= "flg_siad_check_no";
	
	public static final String CTS_STP_REPORT_RAGGRUPPAMENTO ="ragg";
	public static final String CTS_STP_REPORT_ZONE="zona";
	public static final String CTS_STP_REPORT_DISTRETTO="distretto";
	public static final String CTS_STP_REPORT_PCA="pca";
	public static final String CTS_STP_REPORT_SOCSAN="socsan";
	public static final String CTS_STP_REPORT_TIPO_UBI="dom_res";
	public static final String CTS_STP_REPORT_DATA_INIZIO = "data_inizio";
	public static final String CTS_STP_REPORT_DATA_FINE = "data_fine";
	public static final String CTS_STP_REPORT_TERR="terr";
	public static final String CTS_STP_REPORT_ASS_COD_TIPOCURA = "cod_tipocura";
	public static final String CTS_STP_REPORT_ASS_COD_TIPO_UTE = "cod_tipo_ute";
	public static final String CTS_STP_REPORT_ASS_COD_LIVELLO = "cod_livello";
	public static final String CTS_STP_REPORT_ASS_LIVELLO = "st_livello";
	public static final String CTS_STP_REPORT_ASS_ACCESSI_EFFETTUATI = "st_acc_eff";
	
//	public static final String PIANIFICAZIONE_PAI = Costanti.PIANIFICAZIONE_PAI; //"pianificazione";

	public static final String CTS_NUMERO_RICHIESTE_FONTE = "num_rich_fn";
//	public static final String CTS_LISTA_ATTIVITA_CARICA_DATI = "ls_car_dati";
//	public static final String CTS_LISTA_ASSISTITI_CARICA_DATI = "lst_car_dati";

//	public static final String CTS_DATA_ULTIMA_PROROGA_SKSO = Costanti.CTS_DATA_ULTIMA_PROROGA_SKSO;//"dt_ult_proroga";
//	public static final String CTS_NUMERO_SKSO_CONCLUSE = Costanti.CTS_NUMERO_SKSO_CONCLUSE;// "num_scheda_conc";
//	public static final String CTS_SOSPENSIONE_SKSO_DT_INIZIO = Costanti.CTS_SOSPENSIONE_SKSO_DT_INIZIO;//"dt_so_inz";
//	public static final String CTS_SOSPENSIONE_SKSO_DT_FINE = Costanti.CTS_SOSPENSIONE_SKSO_DT_FINE;//"dt_so_fn";
//	
//	public static final String CTS_SKSO_STATO = Costanti.CTS_SKSO_STATO;//"so_stato_scheda";
//	public static final String CTS_SKSO_STATO_NUOVA = Costanti.CTS_SKSO_STATO_NUOVA;//"msg.so.stato.nuovo";
//	public static final String CTS_SKSO_STATO_CONCLUSA = Costanti.CTS_SKSO_STATO_CONCLUSA;//"msg.so.stato.concluso";
//	public static final String CTS_SKSO_STATO_PROROGA  = Costanti.CTS_SKSO_STATO_PROROGA;//"msg.so.stato.proroga";
//	public static final String CTS_SKSO_STATO_SOSPESA  = Costanti.CTS_SKSO_STATO_SOSPESA;//"msg.so.stato.sospeso";
//	public static final String CTS_SKSO_STATO_ATTIVA   = Costanti.CTS_SKSO_STATO_ATTIVA;//"msg.so.stato.attiva";
//	public static final String CTS_SKSO_STATO_IN_DEFINIZIONE = Costanti.CTS_SKSO_STATO_IN_DEFINIZIONE;//"msg.so.stato.in.definizione";
//	public static final String CTS_SKSO_STATO_SCADUTA  = Costanti.CTS_SKSO_STATO_SCADUTA;//"msg.so.stato.scaduto.piano";
	public static final String CTS_SKSO_STATO_NON_PRESENTE= "msg.so.stato.non.presente";

	public static final String CTS_DT_CONTATTO_APERTURA = "dt_cont_ap";
	public static final String CTS_DT_CONTATTO_CHIUSURA = "dt_cont_ch";

	public static final String CTS_DESCRIZIONE_SEDE = "sede_descr";

	public static final String CTS_L_ASSISTITI_RICERCA_OPERATORE = "oper";
	public static final String CTS_L_ASSISTITI_RICERCA_SEDE = "sede";
	public static final String CTS_L_ASSISTITI_RICERCA_DISTRETTO = "dist";

//	public static final String CTS_VALORE_DEFAULT_NESSUNO = "-1";
//	public static final String PRE_IST_FILTRO = "p_i_filtro";
	
	public static final String CTS_ID_ESITO_VALUTAZIONE ="id_esito_valutazione";
	public static final String CTS_DT_VALUTAZIONE = "dt_valutato";
	public static final String CTS_ESITO_VALUTAZIONE ="esito_valutazione";

//	public static final String CTS_DT_PROSSIMA_VALUTAZIONE = Costanti.CTS_DT_PROSSIMA_VALUTAZIONE;//"dt_prossima_valutazione";
	public static final String CTS_PR_REVISIONE ="pr_revisione";

	public static final String CTS_RMSKSO_PR_DATA_REVISIONE = "pr_data_revisione";
	public static final String CTS_RMSKSO_PR_REVISIONE = "pr_revisione";
	public static final String CTS_ESITO_VALUTAZIONE_CONFERMA = "C";
	public static final String CTS_ESITO_VALUTAZIONE_CAMBIA_PIANO = "P";
	/*nel caso del cambio piano: viene attivata una nuova scheda so */
	public static String CTS_ESITO_VALUTAZIONE_ID_SKSO_NEW = "idskso_new";
	/* ricarica scheda segreteria SO */
	public static String CTS_ESITO_VALUTAZIONE_RICARICA_SO = "est_val_rica_SO";

	public static String CTS_LABEL_MESSAGGIO = "lbx_messaggio";

	public static String CTS_NO_CHIUSURA_CONTATTI_ACCESSI_DOPO_CONCLUSIONE = "contatti.presenti.accessi.dopo.conclusione";
	public static String CTS_NO_CHIUSURA_CONTATTI_ACCESSI_PRIMA_APERTURA = "contatti.presenti.accessi.prima.apertura";
//	public static String CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO_DETTAGLIO = Costanti.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO_DETTAGLIO;//"contatti.presenti.accessi.SO.dettaglio";
//	public static String CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO = Costanti.CTS_NO_CHIUSURA_CONTATTI_ACCESSI_SO;//"contatti.presenti.accessi.SO";
	public static String CTS_MOTIVO_DIMISSIONI_DO = "ds_mt_dim_do";

//	public final static String CTS_CONVIVENTI = "conviventi";
	public final static String CTS_ID_SKSO_NUOVO_INSERIMENTO = "-1";
	public final static String CTS_ACCESSI_FLUSSI_SIAD_INVIATO = "acc_flu_siad";
	public static final int CTS_T_F_A_P_NCGG_0 = 0;
	public static final int CTS_T_F_A_P_NCGG_1 = 1;
	
//	public static String CTS_PHT2_STAMPA_MODELLO_CON_DATI= Costanti.CTS_PHT2_STAMPA_MODELLO_CON_DATI;
	public static String NOME_APPLICAZIONE = "SINSSNT_WEB2";
	public static final String CTS_ESITO_CONTATTO_PUA = "6";
	public static final String CTS_COD_DISTRETTO_PRESA_CARICO = "cod_distretto_presacarico";
	public static final String CTS_RMSKSO_ADP = "adp";
	public static final String CTS_RMSKSO_AID = "aid";
	public static final String CTS_RMSKSO_ARD = "ard";
	public static final String CTS_RMSKSO_VSD = "vsd";
	
	public static final String CTS_STP_REPORT_TIPO_PRESTAZIONE= "tp_prestaz";
	public static final String CTS_STP_REPORT_TIPO_PRESTAZIONE_DOMICILIARE = "D";
	public static final String CTS_STP_REPORT_FIGURA_PROFESSIONALE = "tp_figz";
	public static final String CTS_STP_REPORT_ASS_ACCESSI_EFFETTUATI_SIAD = "st_acc_eff_inv";
	
	
	public static String getDecodificaSN(String codice) {
		return Costanti.getDecodificaSN(codice);
	}
}

package it.caribel.app.sinssnt.controllers.login;

import it.caribel.app.common.ejb.ConfEJB;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.PreferenzeStruttureCtrl;
import it.caribel.util.Application;
import it.caribel.util.CaribelClass;
import it.caribel.util.CaribelSessionManager;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.ISASUtil;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Messagebox;

public class ManagerProfile extends ManagerProfileBase{

	public static final String ZONA_SM_OPERATORE = "zonaSm_operatore";
	public static final String ABILITAZ_DISTRETTO = "abilitaz_distretto";
	public static final String ABILITAZ_DISTR_AI = "abilitaz_distr_ai";
	public static final String ABILITAZ_ZONA = "abilitaz_zona";
	public static final String PRES_SM_OPERATORE = "presSm_operatore";   
	public static final String DISTR_SM_OPERATORE = "distrSm_operatore";
		
	public static final String HASH_TPPREAI = "hash_tpPreAI";
	public static final String HASH_PRESTCONSU = "hash_prestConsu";
	public static final String TP_INTERVENTO_DEFAULT = "tp_intervento_default";
	public static final String NASCONDI_TEMPO_PRESTAZ = "nascondi_tempo_prestaz";
	/*ABILITAZIONE VARIE */
	public static final String ABILITAZIONE_RFC115_6 = "rfc115_6";
	
	public static final String CLOSE_SCHEDA_ASS = "A_CLSSKA"; //  ass soc
    public static final String REOPEN_SCHEDA_ASS = "A_OPNSKA";
    public static final String CLOSE_SCHEDA_INF = "A_CLSSKI"; //  infermiere
    public static final String REOPEN_SCHEDA_INF = "A_OPNSKI";
    public static final String CLOSE_SCHEDA_MED = "A_CLSSKM"; //  medico
    public static final String REOPEN_SCHEDA_MED = "A_OPNSKM";
    public static final String CLOSE_SCHEDA_FIS = "A_CLSSKF"; //  fisioterap
    public static final String REOPEN_SCHEDA_FIS = "A_OPNSKF";
    public static final String CLOSE_SCHEDA_ONC = "A_CLSSKO"; //  medico palliativista
    public static final String REOPEN_SCHEDA_ONC = "A_OPNSKO";	
	public static final String ABIL_ASTERVIEW = "abil_asterview";
	public static final String ABIL_SKVALPUAUVM = "abil_skvalpuauvm";
	public static final String ABIL_NEWCONT_INF = "abil_newcont_inf";
	public static final String ACCE_DURATA_OBBL = "acce_durata_obbl";
	public static final String DEFAULT_ACCESSI = "default_accessi";
	public static final String ABIL_ACC_UNIFUN = "abil_acc_unifun";
	public static final String ABL_GST_SPR = "abl_gst_spr";
	public static final String OP_ACC_GEN = "op_acc_gen";
	public static final String OP_ACC_OCC = "op_acc_occ";
	public static final String ABIL_RICERCA_ANAG_CENT = "abil_ricerca_anag_cent";
//	public static final String SO_OBB_CDI_PRIMA_VISITA = "OBBL_SO_PV"; 
	public static final String GG_VISTA_SEGNALAZIONE_OPERATORE ="GG_VISTA_SEGN";
	public static final String GG_SCADENZA_RICOVERI_RSA ="GG_SCAD_RICOVERI";
	public static final String SO_PROPORRE_DT_SCHEDA ="PROP_DT_SCHEDA";
//	public static final String ABL_INS_DATA_VALUTAZIONE_BISOGNI = "ABL_DT_VAL_BIS";
	public static final String GG_DA_PROROGA =ManagerProfileBase.GG_DA_PROROGA;//"GG_DA_PROROGA";
	public static final String GG_VALUTAZIONE_BISOGNI ="GG_VAL_BIS";
	public static final String GG_PREGRESSO_PRESTAZIONI ="GG_PREGR_PRS";
	public static final String ESTRAZIONE_FLUSSI_FLS21_X_AREA = "FLS21_X_AREA";
	public static final String L_A_FONTI_DA_ESCLUDERE_FIG_PROF ="LA_NO_FONTE";
	public static final String L_A_FONTI_DA_ESCLUDERE_SO ="LA_NO_FONTE_SO";
	public static final String CTR_AUTO_WEB ="CTR_AUTO_WEB";
	public static final String CTR_SOSP_WEB ="CTR_SOSP_WEB";
	public static final String FORZA_INTMMG_WEB ="FORZA_INTMMG_WEB";
	public static final String CTR_AUTO_POR ="CTR_AUTO_POR";
	public static final String CTR_SOSP_POR ="CTR_SOSP_POR";
	public static final String FORZA_INTMMG_POR ="FORZA_INTMMG_POR";
//	public static final String ABIL_SO_CAMBIO_DISTRETTO_SEDE ="ABL_SO_CG_DIS_SD";
	public static final String ABIL_FILTRO_MODULO_RICHIESTA = PreferenzeStruttureCtrl.ABIL_FILTRO_MODULO_RICHIESTA;
	public static final String CTS_PHT2_NOME_APPLICATIVO_DEPLOYATO ="PHT2_NOME_APP";
	public static final String ABILIT_INVIO_FSE = "ABILIT_INVIO_FSE";
	public static final String AG_NN_ES = "AG_NN_ES"; 				//VFR: prestazione per il non eseguito 
	public static final String PRESTPREL = "PRESTPREL"; 				//VFR: elenco delle prestazioni che sono prelievi 
	
	public static String IS_SEGRETERIA_ORGANIZZATIVA = "segreteriaOrganizzativa";
	
	private final String conf_kproc_key = "conf_kproc";
	private final String conf_kproc_value = "SINS"; 
	private final String app_name = "SINSSNT";
	
	public ManagerProfile() {}

	public boolean caricaProfile() {
		boolean esito = selectAccessoProcedura() &&	abilitazioniProcedura();
		if(!esito){
			CaribelSessionManager.getInstance().logOff();
		}else{	
			String url = Application.getUrl();
			
			String prefissoServlet = app_name.toUpperCase();
			String nameForFile	= prefissoServlet+"FileServlet/"+prefissoServlet+"FileServlet#";
			String nameForFop	= prefissoServlet+"FoServlet/"+prefissoServlet+"FoServlet";
			String nameForGprs 	= prefissoServlet+"GprsServlet/"+prefissoServlet+"GprsServlet#";
			
			CaribelSessionManager.getInstance().addParamToProfile(FOP,url+nameForFop);
			CaribelSessionManager.getInstance().addParamToProfile(ACCESSO_FILE,url +app_name+"/"+app_name.toLowerCase()+"/accessoFile.jsp");
			
			// il parametro 'lettura_sysdate' dice se la getItaDate() legge la data
			// da server: NO=Non legge data da server ma da client
			//            SI=Legge data da server (c'è bisogno dell'EJB sul server).
			//gb 12/03/07      profile.putParameter("lettura_sysdate","NO");
//			profile.putParameter("lettura_sysdate","NO"); //gb 02/04/07
			profile.putParameter("infosistema",url+nameForGprs+"SINS_INFOSISTEMA");			
			profile.putParameter(profile.CLIENT_INTERFACE,this.client_interface);
            profile.putParameter(profile.CONNECTION, this.class4connection);
			
			//Riportare qui tutte le url per il lancio dei flussi
			CaribelSessionManager.getInstance().addParamToProfile("flussinazio", url + nameForFile +"SINS_FLUSSI_NAZ");
			CaribelSessionManager.getInstance().addParamToProfile("flussiprestaz", url + nameForFile +"SINS_FLUSSI_PRESTAZ");
			
		}
		return esito;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean selectAccessoProcedura() {
		try{		
			Hashtable h = new Hashtable();
			h.put(conf_kproc_key,conf_kproc_value);
			h.put("conf_keyReg",CODICE_REGIONE);
			h.put("conf_keyUsl",CODICE_USL);
			h.put("codice",CaribelSessionManager.getInstance().getIsasUser().getKUser());

			ISASRecord dbrRet = (ISASRecord)CaribelClass.isasInvoke(confEJB, "queryAccessoProcedura", h);
			if(dbrRet==null){
				esci("");
				return false;
			}else if(!((String)dbrRet.get("ris")).equals("0")){
				esci((String)dbrRet.get("ris"));
				return false;
			}

			Hashtable hProfile = new Hashtable();
			hProfile.put(CODICE_OPERATORE,    CaribelSessionManager.getInstance().getIsasUser().getKUser());
			hProfile.put(CODICE_REGIONE,     (String)dbrRet.get("conf_txtReg"));
			hProfile.put(CODICE_USL,         (String)dbrRet.get("conf_txtUsl"));
			hProfile.put(COGNOME_OPERATORE,  (String)dbrRet.get("cognome"));
			hProfile.put(NOME_OPERATORE,     (String)dbrRet.get("nome"));
			hProfile.put(CF_OPERATORE,		 (String)dbrRet.get("cod_fiscale"));
			hProfile.put(TIPO_OPERATORE,     (String)dbrRet.get("tipo"));
			hProfile.put(QUAL_OPERATORE,     (String)dbrRet.get("cod_qualif"));
			hProfile.put(PRES_OPERATORE,     (String)dbrRet.get("cod_presidio"));
			// x ASSISTENZA SOCIALE
			hProfile.put("subzona_operatore",(String)dbrRet.get("cod_subzona"));
			hProfile.put("settore_operatore",(String)dbrRet.get("settore"));
			// 09/01/08
			hProfile.put(ZONA_OPERATORE,	 (String)dbrRet.get("cod_zona"));
			// 13/01/09
			hProfile.put(DISTRETTO_OPERATORE,  (String)dbrRet.get("cod_distretto"));
			// 28/11/12 x PHT ----
			hProfile.put("ospedale_operatore",(String)dbrRet.get("cod_ospedale"));
			hProfile.put("reparto_operatore", (String)dbrRet.get("cod_reparto"));
			hProfile.put(ManagerProfileBase.COD_CENTROSERV, ISASUtil.getValoreStringa(dbrRet, ManagerProfileBase.COD_CENTROSERV));
			hProfile.put("cod_reparto","");
			// 28/11/12 x PHT ----
			
			CaribelSessionManager.getInstance().addParamsToProfile(hProfile);
			

		}catch(Exception ex){
			Messagebox.show(
					"Reperimento configurazione di accesso non riuscito!\nContattare l'assistenza.",
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
			return false;
		}
		return true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean abilitazioniProcedura()
	{
		try{
			Hashtable h=new Hashtable();
		    Hashtable hRet=new Hashtable();
		    /**
		     * "ABILITAZ_SS_AGEN": serve per abilitare la procedura dell'agenda
		     * "ABILITAZ_NEWCONT": serve per abilitare il pulsante nuovo contatto dalla griglia dei contatti
		     * "DEFAULT_ACCESSI": Serve per caricare i dati di default su Interventi
		     * "ABIL_ACC_UNIFUN": Serve definire se la combo unita funzionale su accessi è visibile o no
		     * "RV_STAMPE_ASSDOM":Serve per visualizzare la scelta di menu per le stampe: Prospetto per rilevazione assistenza domiciliare
		     * "RV_FLUSSI_SIDADI":Serve per visualizzare la scelta di menu Flussi Regione Veneto
		     * "RT_FLUSSI_SPA":Serve per visualizzare la scelta di menu Flussi SPA
		     * "RT_FLUSSI_SAV":Serve per visualizzare la scelta di menu Flussi SAV
		     * "RV_SVAMA":Serve per visulizzare il pulsante svama all'interno del progetto
		     * "FLUSSO_NAZ":Serve per visulizzare il pulsante scheda valutativa all'interno del progetto
		     * "GESTASSEGNO":Serve per visulizzare il pulsante assegno di cura all'interno del progetto
		     *                e per il menu graduatoria assegno di cura
		     * "ABILITAZ_CT": per la JframeContrib : se esiste ed è a Si, vuol dire che hanno la procedura
		     *                dei contributi e/o che vogliono gestito la tabelladeiconti economici
		     * "tab_patologia": serve per vedere su quale tabella vengono decodificate le patologie
		     *                  del progetto
		     * "ABILITAZ_RSA": per la JframeRSARicoveriProposta : se esiste ed è a Si, vuol dire che hanno la procedura
		     *                RSA e/o che vogliono gestito la tabelladeiconti economici per rsa
		     * "CONS_ML": per la JFrameSkRiepilogo, visibilita' pulsante consultaz. dati Med. Leg.
		     * "CONS_PROTE": per la JFrameSkRiepilogo, visibilita' pulsante consultaz. dati protesica
		     * "CONS_SIL": per la JFrameSkRiepilogo, visibilita' pulsante consultaz. dati SIL
		     * "CONS_SOC": per la JFrameSkRiepilogo, visibilita' pulsante consultaz. dati Sociale (SIL)
		     *
		     * gb 12/10/07
		     * "AS_PROG_MOT": abilitazione uso del campo "motivo" nel progetto per ass soc
		     * "AS_PROG_PROB": abilitazione uso del campo "problema" nel progetto per ass soc
		     * m 10/12/07
		     * "ABIL_AGENDA_PUA": abilitazione agenda per gestione appuntamenti del PUA
		     * m 09/01/08
		     * "ABIL_SKVALBISASS": abilitazione uso bisogni assistenziali nella SkValutazione
		     * m 04/02/08
		     * "ABIL_AC_PARG": abilitazione uso scheda PARG in analisi del caso
		     * "ABIL_AC_ACCHANDI": abilitazione uso scheda accertamento handicap in analisi del caso
		     * "ABIL_AC_ACCGRAU": abilitazione uso scheda accertamento grado autosuff in analisi del caso
		     * m 20/02/08
		     * "ABIL_SKVALPUAC": abilitazione uso scheda PUACUVM nella SkValutazione
		     * 14/04/08
		     * "ABIL_OBBL_SKVAL": obbligo esistenza skValutazione per inserimento contatto
		     * "ABIL_NEWCONT_INF": abilitazione nuovo contatto infermieristico (anche in presenza di altri cont già aperti)
		     * m 03/10/08 + 17/10/08
		     * "ABIL_AGPUA_SAN_C", "ABIL_AGPUA_SAN_S", "ABIL_AGPUA_SOC_C", "ABIL_AGPUA_SOC_S": abilitazioni agenda per casi
		     *  semplici e complessi sanitari o sociali
		     * m 17/10/08
		     * "VISTO_PUAC_SOC", "VISTO_PUAC_SAN": necessità di visto PUAC perchè i csi complex siano selezionabili dagli AS/INF
		     * m 12/11/08
		     * "ABIL_ATTR_CASI": abilitazione per attribuzione casi semplici/complex ad oper senza agenda
		     * Elisa 13/11/09
		     *  "RSA_OBBLCONTRSAN" , "RSA_OBBLCONTRSOC": controllo obbligatorieta' scelta contributi in sins/rsa
		     */

		    String key[]={"ABILITAZ_SS_AGEN","ABILITAZ_NEWCONT",
		      "DEFAULT_ACCESSI","RV_STAMPE_ASSDOM","RV_FLUSSI_SIDADI","RT_FLUSSI_SPA",
		      "RV_SVAMA","FLUSSO_NAZ","ABIL_ACC_UNIFUN","GESTASSEGNO",
		      "ABILITAZ_CT","FASCE_ETA", "tab_patologia","RT_FLUSSI_SAV",ABILITA_SCALA,
//		    abilitazioni RSA
		      ABILITAZIONE_RSA,		
		      		"ABILITAZ_FIT",//bargi 15/09/2009 per abilitare al conteggio importo fittizio
		      		"RSA_NUMISTITUTI",//bargi 17/07/2009
		            "RSA_OBBLISTITUTO",//bargi 17/07/2009
		            "RSA_OBBLCONTO",//bargi 17/07/2009
		            "RSA_OBBLTARSOC",//bargi 17/07/2009
		            "RSA_OBBLTARSAN",//bargi 17/07/2009
		            "ABILITAZ_RSA_DOM",//bargi 21/07/2009
		            "RSA_OBBLCONTRSAN", //Elisa 13/11/09
		            "RSA_OBBLCONTRSOC", //Elisa 13/11/09
		      "CONS_ML","CONS_PROTE","CONS_SIL","CONS_SOC","AS_PROG_MOT","AS_PROG_PROB", //gb 12/10/07
		      "ABIL_AGENDA_PUA",   // m. 10/12/07
		      "ABIL_SKVALBISASS", // m. 09/01/08
		      "ABIL_AC_PARG", // m. 04/02/08
		      "ABIL_AC_ACCHANDI",
		      "ABIL_AC_ACCGRAU",
		      "ABIL_SKVALPUAC", // m. 20/02/08
		      "ABIL_OBBL_SKVAL", // m. 14/04/08
		      "ABIL_NEWCONT_INF",
		      "ABIL_AGPUA_SAN_C", // m. 03/10/08
		      "ABIL_AGPUA_SAN_S",
		      "ABIL_AGPUA_SOC_C",
		      "ABIL_AGPUA_SOC_S",
		      "VISTO_PUAC_SOC", // m. 17/10/08
		      "VISTO_PUAC_SAN",
		      "ABIL_ATTR_CASI", // m. 12/11/08
		      "ABIL_NEWOBB_SOC", // m. 27/11/08
		      "OP_ACC_GEN", // m. 14/05/08: da SINS
		      "OP_ACC_OCC",
		      "MUGELLO_STAMPE", //gb 21/05/08
		      "ABIL_2_LIVELLI", //gb 23/05/08
		      "ABILCT_SINS_AUTO", //gb 30/05/08
		      "ABILRS_SINS_AUTO", //bargi 27/05/10
		      "MAIL_HOST", //gb 15/07/08
		      "MAIL_MITT", //gb 15/07/08
		      "MAIL_PORT", //gb 15/07/08
		      "AGG_INTEXP", //gb 17/07/08
		      "GETT_PRESENZA", //gb 21/07/08
		      "GETT_PRES_TIPO", //gb 17/09/08
		      "AUTOR_GIUDIZ", //gb 24.11.08
		      "ABIL_AC_MALVIO", //gb 24.11.08
		      "ABIL_AC_COPPIE", //gb 19.01.09
		      "ABIL_AC_ASDODI", //gb 27.01.09
		      "ABIL_AC_ASDOED", //gb 27.01.09
		      "ABIL_ANAMNESI", //gb 27.11.08
		      "ABIL_MINORI_2008", //gb 23.01.09
		      "ABIL_ST_SIENA",
		      UBICAZIONE, 
//		      "ADRSA_UBIC", // Elisa Croci 19/05/09
		      "ADRSA_REG_ERO", // Elisa Croci 22/05/09
		      "ADRSA_ASL_ERO", // Elisa Croci 22/05/09
		      "ABIL_SKVAL", // m. 23/06/09
		      "ABIL_SKVALMMG",
		      "ABIL_SKVALCONT",
		      "ABIL_EVENTI", // m. 06/10/09
		      "OBBL_SCALE_UVM", // m. 12/10/09
		      "PI_DENOM_PROC", // 27/11/09 m.
		      "PI_DENOM_RICESP",
		      "PI_DENOM_BIS1",
		      "PI_DENOM_BIS2",
		      "PI_DENOM_BIS3",
		      "PI_DENOM_BIS4",
		      "ABIL_FLUX_MIN", // 15/01/10 m.
		      "ABIL_AS_TEST", // 20/01/10 m.
		      "AREADISTR_BYOPER", // 17/05/10 m.
		      "FINE_DETGEN_FREE", // 08/06/10 m.
		      "FINE_PRESTP_SET",
		      "ABIL_GENEVE", // m. 30/06/10
			  "VCO_ABIL_DATE",// 28/06/10 bysp
		      "VCO_SAOADI", // 29/06/10 bysp
		      "VCO_DISTRETTO",// 30/06/10 bysp
		      "VCO_TEMPOGO", // 01/07/10 bysp
		      "VCO_TEMPOF", // 01/07/10 bysp
		      "VCO_LESIONI",// 01/07/10 bysp
		      "VCO_ABIL_DOM",// 23/07/10 bysp
		      "AUTOR_OPREF", // 01/09/10 m.
		      "CT_OBBL_BEN_COGN", // 15/09/10 m
		      "CT_OBBL_BEN_CF",
		      "CT_OBBL_BEN_COMU",
		      "CT_OBBL_BEN_INDI",
		      "CT_OBBL_BEN_COMD",
		      "CT_OBBL_BEN_CAP",
		      "CT_OBBL_BEN_PROV",
		      "CT_OBBL_BEN_PAGA",
		      "LAB_STMP_VERBALE", // 29/09/10 m.
		      "LAB_STMP_VERBALED", // 29/09/10 m.
		      "LAB_STMP_SOTTOSC",
		      "TITOLO_PUA", //17/11/10 bysp
		      "TITOLO_PAP", //17/11/10 bysp
		      "TITOLO_UVM",//17/11/10 bysp
		      "NUCLEOFAMUNI", // elisa b 03/12/10
		      "MAIL-PAP", // mail pap
		      "ACCE_DURATA_OBBL", // 31/05/11 m.
		      "ABIL_INS_VALUVMD", // elisa b 15/06/11
		      "CONTRIBUTO_GG", // elisa b 29/06/11
		      "CTR_SKINF_RISPRO", // elisa b 01/08/11
		      "CTR_SKFIS_RISPRO", // elisa b 01/08/11
		      "CTR_SKPAL_RISPRO", // elisa b 01/08/11
		      "CTR_SKASS_RISPRO", // elisa b 01/086/11
		      "CTR_SKMMG_RISPRO", // elisa b 01/08/11
		      "CTR_DTAPE_SKPUAC", // elisa b 02/08/11
		      "STMP_PAP_EMPOLI", // boffa 31/08/11
		      "AREADIS_X_USL", // elisa b 03/10/11
		      "FLUSSI_SINBA", // 10/10/11 m
		      "SINBA_ENTE_RILEV", // 10/10/11 m
		      "STMP_SOTTOSC_IMM", // 07/11/11 m
		       "CONTRIBUTO_TOT",//bargi 26/03/2012
		      "RTPAPNEW",//11/11/11 bargi
		      "PAP_NGG_RISP",//bargi 20/12/11
		      "PAP_NGG_LIQ",//bargi 20/12/11
		      "PAP_NGG_SSCRIZ",//bargi 20/12/11
		      "MOD_SCHEDA_UVM",//boffa 03/01/12
		      "STMP_TRAPANI",//boffa 16/01/12
		      "RV_ADT", // 14/02/12 m.
		      "OBB_CC_DET_INTER", //boffa 05/03/12 obbligatorieta nel dettaglio intervento
		      "ABIL_DTSCELTAMED", //elisa b 19/04/12 : abilita il campo data_medico nella scheda anagrafica
		      "RV_FLUSSI_FAR", //boffa 26/04/12: abilitazione per i flussi far molise.
		      "RV_PUAUVM_OBBL", //simonep 290612 gestione obbligatorietà campi in rv_puauvm per flussi.
		      "RV_RISPRO_OBBL",//simonep 290612 gestione obbligatorietà inserimento almeno una risposta x valutazione
		      "ABIL_RISPDT_BLK", //simonep 120712 gestione controllo bloccante su data risposta nella valutazione (x Veneto)
		      "GEST_VAL_DA_CONT", //simonep 130712 gestione scheda valutazione da chiusura contatto (per med,medpal,fisio,inf) e chiusura obiettivo per ass. soc. (X Cittadella)
		      "CONTR_LETT_ABIL",//boffa
		      "FILTRO_SERV",//bargi 27082012  serve per ricercare i sussidi abbinati al contributo relativi all'intervento ..servizio tabe_serv_sussidi"
		      "OBBL_PATOL_UVM", // mv 03/09/12: obbligo definizione patologie in scheda PUAUVM
		      "COD_FIGLI_CONIU", // mv 03/09/12: codifica figli o coniuge/convivente su tabella PARENT
		      "ABL_SKSOC_DR2259", // mv 17/09/12: abilitazione gestione nuova versione scalaSociale secondo DR2259/2012
		      "ABIL_QRC",  //abilitazione per codice qrc
		      "ABIL_UVMD", //abilitazione per la gestione uvmd
		      "FLUSSI_SINBA_NEW", //flussi sinba nuova versione
		      "CT_TIPO_MANDELET", // 29/10/2012
		      "TRASPORTI_TIPO", // 14/11/2012
		      "ABL_ST_DISABILE", // 11/12/2012 abilitazione stampa disabili per bassano
		      "COD_ST_DISABILE", // 11/12/2012 codice disabile per abilitazione stampa disabili per bassano
		      "ABIL_SK_PHT", // 28/11/12: abilitazione gestione PHT
		      "ABIL_SAPIO", // 16/01/13: abilitazione inport prestazioni SAPIO
		      "SAPIO_DIR_DATI", // 16/01/13: directory dati per inport prestazioni SAPIO
		      "ABL_GST_SPR", // 17/01/2013: abilitazione flussi SPR
		      "OBL_MED_SPR", // 22/02/2013: abilitazione flussi SPR: rendere obbligatorio il medico nel caso dei flussi SPR
		      "CTRL_CHIU_ANAG",// 23/01/13: abilitazione controllo per chiusura anagrafica
		      "OBBL_SCALE_UVMD", // boffa 05/02/13 obbligo per la compilazione delle scale uvmd
		      "ELEASSLIV", // minerba 28/02/13 pistoia
		      "ABL_STMP_SKATTIV", // minerba 04/03/13 stampa scheda attivazione da contatto sociale
		      "ABL_SKUVT_SKPUAC", // 14/03/13 mv:abilitazione apertura skuvt dalla skpuac/skinf
		      "CTRL_SKINF_SIAD", // 25/03/13 mv: abilitazione controlli su skinf per flussi siad
		      ABILITAZIONE_RFC115_6, //Simone 14/05/13 abilitazione nuovo rfc 115-118 versione 6
		      "AB_ARZIGN", // Mariarita Minerba  05/06/13
		      "EP_CURA", // Mariarita Minerba  05/06/13
		      "CONTR_MED", // Mariarita Minerba  05/06/13
		      "RISP_ASS", // Mariarita Minerba  05/06/13
		      "DECEDU", // Mariarita Minerba  05/06/13
		      "CONT_AUTO", // Mariarita Minerba  05/06/13
		      "VALUTA", // Mariarita Minerba  05/06/13
		      "PAP_BLOCCO_ISEE", // bargi 05/08/2013
		      "ABIL_COMUNE_NASC",// simone 25/11/13
		      "OBBLIGO_OSP_REP",//24/10/2013: rendo obbligatori campi su contato infermieristico se conf_txt=presidi ospedalieri
		      "NM_FLUX_ENNA", // simone 09/12/13 nomi personalizzate flussi enna
		      "STRIEP", // mariarita 05/03/2014
		      "PAZPERI", // mariarita 05/03/2014
		      "PREPAI", // mariarita 05/03/2014
		      "ASSUVM", // mariarita 05/03/2014
		      "SEDUVM", // mariarita 05/03/2014
		      "ELSVA", // mariarita 05/03/2014
		      "ELPAS", // mariarita 05/03/2014
		      "FLS21", // mariarita 18/03/2014
		      "PAP_CALCOLO",//bargi 10/04/2014
		      "PAP_BLOCCO_BEN",//bargi 10/04/2014
		      "URL_ASTERVIEW", // 27/05/14 mv: url per AsterView
		      "ABL_RIC_ANA_CENT", //elisa b :abilitazione ricerca su anagrafe centrale
		      SO_OBB_CDI_PRIMA_VISITA, // boffa: obbligatorio in SO nel caso di intensità assistenziale CDI di prima visita
		      GG_VISTA_SEGNALAZIONE_OPERATORE, // boffa: gg a cui far vedere le segnalazioni che sono state viste dalla SO
		      SO_PROPORRE_DT_SCHEDA, // boffa: viene proposta la data della scheda
		      ABL_INS_DATA_VALUTAZIONE_BISOGNI, //boffa: viene inserita la data del verbale, nella scala di valutazione bisogni
		      GG_VALUTAZIONE_BISOGNI, //boffa: valutazione bisogni scadenza
		      GG_PREGRESSO_PRESTAZIONI, //boffa: giorni di pregresso delle prestazioni
		      ESTRAZIONE_FLUSSI_SIAD_X_AREA, //simone, abilitazione all'estrazione flussi siad per area vasta anziché distretto.
		      ESTRAZIONE_FLUSSI_FLS21_X_AREA, //simone, abilitazione all'estrazione flussi fls21 per area vasta anziché distretto.
		      L_A_FONTI_DA_ESCLUDERE_FIG_PROF, //boffa: fonti da escludere per le varie figure professionali
		      L_A_FONTI_DA_ESCLUDERE_SO, //boffa: fonti da escludere per la SO
		      CTR_AUTO_WEB, //serratore: controllo sull'autorizzato delle prestazioni MMG da webapp 
		      CTR_SOSP_WEB, //serratore: controllo sulla sospensione delle prestazioni MMG da webapp
		      FORZA_INTMMG_WEB, //serratore: forzo anche su INTMMG (se passano i vari controlli) le prestazioni mmg  da webapp 
		      CTR_AUTO_POR, //serratore: controllo sull'autorizzato delle prestazioni MMG da portale
		      CTR_SOSP_POR, //serratore: controllo sulla sospensione delle prestazioni MMG da portale
		      FORZA_INTMMG_POR, //serratore: forzo anche su INTMMG (se passano i vari controlli) le prestazioni mmg da portale
		      ABIL_SO_CAMBIO_DISTRETTO_SEDE,  //BOFFA: abilitazione per il cambio del distretto e delle sede della scheda
		      ABIL_FILTRO_MODULO_RICHIESTA, //BOFFA: abilitazione filtro sul modulo 
		      GG_DA_PROROGA, //VFR: numero di gg massimo prima della scadenza per inserire una proroga
		      CTS_PHT2_NOME_APPLICATIVO_DEPLOYATO, //BOFFA: nome pht2 deployata sullo stesso tomcat
		      "WEBHS_RICHIESTA", 
		      CTS_OBBLIGO_SO_STATO_CIVILE,
		      CTS_OBBLIGO_SO_NUMERO_FAMILIARI,
		      CTS_OBBLIGO_SO_ASSISTENZA_NON_FAMILIARE,
			  CTS_OBBLIGO_SO_CON_CHI_VIVE,
			  CTS_OBBLIGO_SO_RAZZA_ETNIA,
			  CTS_OBBLIGO_SO_LINGUA,
			  CTS_OBBLIGO_SO_TITOLO_STUDIO,
			  CTS_OBBLIGO_SO_RICHIEDENTE,
			  CTS_OBBLIGO_SO_STRUTTURA_PROVENIENZA,
			  CTS_OBBLIGO_SO_MOTIVO_RICHIESTA,
			  SO_NO_OBBL_SCALA_VAL_BISOGNI, 
			  CONF_OBB_RICOVERO_STANZA, 
			  CONF_OBB_RICOVERO_POSTO_LETTO, 
			  GG_SCADENZA_RICOVERI_RSA, // boffa: gg di scadenza del ricovero
			  ABIL_CHECKB_PORT, //cv: abilitazione alla visualizzazione diverse checkbox per il palliativista nella maschera presidi
			  ABILIT_INVIO_FSE, // serratore: Abil. inoltro PAI a FSE
			  FAR_ON_RUG, //s.puntoni abilitazione estrazione flussi far da RUGVAL anziché da SCL_VALUTAZIONE
			  AG_NN_ES,
			  PRESTPREL,
		    };

		    String profiles[]={"abil_agen","abil_newcontat",
		      "default_accessi","rv_stampe_assdom","rv_flussi_sidadi","rt_flussi_spa",
		      "rv_svama","flusso_naz","abil_acc_unifun","gestassegno","abilitaz_ct","fasce_eta",
		      "tab_patologia","rt_flussi_sav",ABILITA_SCALA,
		      //abilitazioni RSA   ABILITAZIONE_RSA,
		      "abilitaz_rsa",//bargi 17/07/2009  		
		      "abilitaz_fit",//bargi 15/09/2009
		      "rsa_numistituti",//bargi 17/07/2009
		      "rsa_obblistituto",//bargi 17/07/2009
		      "rsa_obblconto",//bargi 17/07/2009
		      "rsa_obbltarsoc",//bargi 17/07/2009
		      "rsa_obbltarsan",//bargi 17/07/2009
		      "abilitaz_rsa_dom",//bargi 21/07/2009
		      "rsa_obblcontrsan" , "rsa_obblcontrsoc", // Elisa 13/11/09
		      "cons_ml","cons_prote","cons_sil","cons_soc","as_prog_mot","as_prog_prob", //gb 12/10/07
		      "abil_agen_pua",    // m. 10/12/07
		      "abil_skvalbisass",// m. 09/01/08
		      "abil_ac_parg",// m. 04/02/08
		      "abil_ac_acchandi",
		      "abil_ac_accgrau",
		      "abil_skvalpuauvm", // m. 20/02/08
		      "abil_obbl_skval", // m. 14/04/08
		      "abil_newcont_inf",
		      "abil_agpua_san_c", // m. 03/10/08
		      "abil_agpua_san_s",
		      "abil_agpua_soc_c",
		      "abil_agpua_soc_s",
		      "visto_puac_soc", // 17/10/08
		      "visto_puac_san",
		      "abil_attr_casi", // m. 12/11/08
		      "abil_newobb_soc", // m. 27/11/08
		       "op_acc_gen", // m. 14/05/08: da SINS
		       "op_acc_occ",
		       "mugello_stampe", //gb 21/05/08
		       "abil_2_livelli", //gb 23/05/08
		       "abilct_sins_auto", //gb 30/05/08
		       "abilrs_sins_auto", //bargi 27/05/10
		       "mail_host", //gb 15/07/08
		       "mail_mitt", //gb 15/07/08
		       "mail_port", //gb 15/07/08
		       "agg_intexp", //gb 17/07/08
		       "gett_presenza", //gb 21/07/08 (x Contributi)
		       "gett_pres_tipo", //gb 17/09/08 (x Contributi)
		       "autor_giudiz", //gb 24.11.08
		       "abil_ac_malvio", //gb 24.11.08
		       "abil_ac_coppie", //gb 19.01.09
		       "abil_ac_asdodi", //gb 27.01.09
		       "abil_ac_asdoed", //gb 27.01.09
		       "abil_anamnesi", //gb 27.11.08
		       "abil_minori_2008", //gb 23.01.09
		       "abil_st_siena",
		       UBICAZIONE, 
//		       "ADRSA_UBIC", // Elisa Croci 19/05/09
		       "adrsa_reg_ero", // Elisa Croci 22/05/09
		       "adrsa_asl_ero",// Elisa Croci 22/05/09
		       "abil_skval", // m. 23/06/09
		       "abil_skvalmmg",
		       "abil_skvalcont",
		       "abil_eventi", // m. 06/10/09
		       "obbl_scale_uvm", // m. 12/10/09
		      "pi_denom_proc", // 27/11/09 m
		      "pi_denom_ricesp",
		      "pi_denom_bis1",
		      "pi_denom_bis2",
		      "pi_denom_bis3",
		      "pi_denom_bis4",
		      "abil_flux_min", // 15/01/10 m.
		      "abil_as_test", // 20/01/10 m.
		      "areadistr_byoper", // 17/05/10 m.
		      "fine_detgen_free", // 08/06/10 m.
		      "fine_prestp_set",
		      "abil_geneve", // 30/06/10 m.
			  "vco_abil_date",// 28/06/10 bysp
		      "vco_saoadi",// 29/06/10 bysp
		      "vco_distretto", // 30/06/10 bysp
		      "vco_tempogo", // 01/07/10 bysp
		      "vco_tempof", // 01/07/10 bysp
		      "vco_lesioni",// 01/07/10 bysp
		      "vco_abil_dom", // 23/07/10 bysp
		      "autor_opref", // 01/09/10 m.
		      "ct_obbl_ben_cogn", // 15/09/10 m
		      "ct_obbl_ben_cf",
		      "ct_obbl_ben_comu",
		      "ct_obbl_ben_indi",
		      "ct_obbl_ben_comd",
		      "ct_obbl_ben_cap",
		      "ct_obbl_ben_prov",
		      "ct_obbl_ben_paga",
		      "lab_stmp_verbale", // 29/09/10 m.
		      "lab_stmp_verbaled", // 29/09/10 m.
		      "lab_stmp_sottosc",
		      "titolo_pua", //17/11/10 bysp
		      "titolo_pap", //17/11/10 bysp
		      "titolo_uvm",//17/11/10 bysp
		      "nucleofamuni", //elisa b 03/12/10
			  "MAIL-PAP", // mail pap
		      "acce_durata_obbl", // 31/05/11 m.
		      "abil_ins_valuvmd", // elisa b 15/06/11
		      "contributo_gg", // elisa b 29/06/11
		      "ctrl_skinf_rispro", // elisa b 01/08/11
		      "ctrl_skfis_rispro", // elisa b 01/08/11
		      "ctrl_skpal_rispro", // elisa b 01/08/11
		      "ctrl_skass_rispro", // elisa b 01/08/11
		      "ctrl_skmmg_rispro", // elisa b 01/08/11
		      "ctr_dtape_skpuac", // elisa b 02/08/11
		      "stmp_pap_empoli", // boffa 31/08/11
		      "areadis_x_usl", // elisa b 03/10/11
		      "flussi_sinba", // 10/10/11 m
		      "sinba_ente_rilev", // 10/10/11 m
		      "stmp_sottosc_imm", // 07/11/11 m
		      "contributo_tot",//bargi 26/03/2012
		      "rtpapnew",//bargi 11/11/11
		      "pap_ngg_risp",//bargi 20/12/11
		      "pap_ngg_liq",//bargi 20/12/11
		      "pap_ngg_sscriz",//bargi 20/12/11
		      "mod_scheda_uvm", //boffa 03/01/12
		      "stmp_trapani", //boffa 16/01/12
		      "rv_adt", // 14/02/12 m.
		      "obb_cc_det_inter",//boffa 05/03/12 convenzione sul centro costo,  se il valore è: 0= non visibile, 1= visibile, 2= visibile ed obbligatorio ;
		      "abil_dtsceltamed", //elisa b 19/04/12 : abilita il campo data_medico nella scheda anagrafica
		      "rv_flussi_far", //boffa 26/04/12: abilitazione per i flussi far molise.
		      "rv_puauvm_obbl",//simonep 290612 gestione obbligatorietà campi in rv_puauvm per flussi.
		      "rv_rispro_obbl", //simonep 290612 gestione obbligatorietà inserimento almeno una risposta x valutazione
		      "abil_rispdt_blk", //simonep 120712 gestione controllo bloccante su data risposta nella valutazione (x Veneto)
		      "gest_val_da_cont", //simonep 130712 gestione scheda valutazione da chiusura contatto (per med,medpal,fisio,inf) e chiusura obiettivo per ass. soc. (X Cittadella)
		      "contr_lett_abil",//boffa
		      "filtro_serv",//bargi 27082012  serve per ricercare i sussidi abbinati al contributo relativi all'intervento ..servizio tabe_serv_sussidi
		      "obbl_patol_uvm", // mv 03/09/12
		      "cod_figli_coniu", // mv 03/09/12: codifica figli o coniuge/convivente su tabella PARENT
		      "abl_sksoc_dr2259", // mv 17/09/12: abilitazione gestione nuova versione scalaSociale secondo DR2259/2012
		      "abil_qrc", //abilitazione per codice qrc
		      "abil_uvmd",  //abilitazione per la gestione uvmd
		      "flussi_sinba_new", // 10/10/11 m
		      "ct_tipo_mandelet", // 29/10/2012
		      "trasporti_tipo",
		      "abl_st_disabile", // 11/12/2012 abilitazione stampa disabili per bassano
		      "cod_st_disabile", // 11/12/2012 codice disabile per abilitazione stampa disabili per bassano
		      "abil_sk_pht", // 28/11/12: abilitazione gestione PHT
		      "abil_sapio", // 16/01/13: abilitazione inport prestazioni SAPIO
		      "sapio_dir_dati", // 16/01/13: directory dati per inport prestazioni SAPIO
		      "abl_gst_spr", // 17/01/2013: abilitazione flussi SPR
		      "obl_med_spr", // 22/02/2013: abilitazione flussi SPR: rendere obbligatorio il medico nel caso dei flussi SPR
		      "ctrl_chiu_anag", // 23/01/13: abilitazione controllo per chiusura anagrafica
		      "obbl_scale_uvmd", // boffa 05/02/13
		      "ELEASSLIV", // minerba 28/02/13 pistoia
		      "abl_stmp_skattiv", // minerba 04/03/13 stampa scheda attivazione da contatto sociale
		      "abl_skuvt_skpuac", // 14/03/13 mv:abilitazione apertura skuvt dalla skpuac/skinf
		      "ctrl_skinf_siad", // 25/03/13 mv: abilitazione controlli su skinf per flussi siad
		      ABILITAZIONE_RFC115_6, //Simone 14/05/13 abilitazione nuovo rfc 115-118 versione 6
		      "ab_arzign", // Mariarita Minerba  05/06/13
		      "ep_cura", // Mariarita Minerba  05/06/13
		      "contr_med", // Mariarita Minerba  05/06/13
		      "risp_ass", // Mariarita Minerba  05/06/13
		      "decedu", // Mariarita Minerba  05/06/13
		      "cont_auto", // Mariarita Minerba  05/06/13
		      "valuta", // Mariarita Minerba  05/06/13
		      "pap_blocco_isee",//bargi 05/08/2013
		      "abil_comune_nasc",// simone 25/11/13
		      "obbligo_osp_rep",//24/10/2013: rendo obbligatori campi su contato infermieristico se conf_txt=presidi ospedalieri
		      "nm_flux_enna", // simone 09/12/13 nomi personalizzate flussi enna
		      "striep", // mariarita 05/03/2014
		      "pazperi", // mariarita 05/03/2014
		      "prepai", // mariarita 05/03/2014
		      "assuvm", // mariarita 05/03/2014
		      "seduvm", // mariarita 05/03/2014
		      "elsva", // mariarita 05/03/2014
		      "elpas", // mariarita 05/03/2014
		      "fls21", // mariarita 05/03/2014
		      "pap_calcolo",//bargi 10/04/2014
		      "pap_blocco_ben",//bargi 10/04/2014
		      "url_asterview", // 27/05/14 mv: url per AsterView
		      "abil_ricerca_anag_cent",//elisa b :abilitazione ricerca su anagrafe centrale
		      SO_OBB_CDI_PRIMA_VISITA,// boffa: obbligo in SO nel caso di intensità assistenziale CDI di prima visita
		      GG_VISTA_SEGNALAZIONE_OPERATORE, // boffa: gg a cui far vedere le segnalazioni che sono state viste dalla SO
		      SO_PROPORRE_DT_SCHEDA, // boffa: viene proposta la data della scheda
		      ABL_INS_DATA_VALUTAZIONE_BISOGNI, //boffa: viene inserita la data del verbale, nella scala di valutazione bisogni
		      GG_VALUTAZIONE_BISOGNI, //boffa: valutazione bisogni scadenza
		      GG_PREGRESSO_PRESTAZIONI, //boffa: giorni di pregresso delle prestazioni
		      ESTRAZIONE_FLUSSI_SIAD_X_AREA, //simone, abilitazione all'estrazione flussi siad per area vasta anziché distretto.
		      ESTRAZIONE_FLUSSI_FLS21_X_AREA, //simone, abilitazione all'estrazione flussi fls21 per area vasta anziché distretto.
		      L_A_FONTI_DA_ESCLUDERE_FIG_PROF, //boffa: fonti da escludere per le varie figure professionali
		      L_A_FONTI_DA_ESCLUDERE_SO, //boffa: fonti da escludere per la SO
		      CTR_AUTO_WEB, //serratore: controllo sull'autorizzato delle prestazioni MMG da webapp 
		      CTR_SOSP_WEB, //serratore: controllo sulla sospensione delle prestazioni MMG da webapp
		      FORZA_INTMMG_WEB, //serratore: forzo anche su INTMMG (se passano i vari controlli) le prestazioni mmg  da webapp 
		      CTR_AUTO_POR, //serratore: controllo sull'autorizzato delle prestazioni MMG da portale
		      CTR_SOSP_POR, //serratore: controllo sulla sospensione delle prestazioni MMG da portale
		      FORZA_INTMMG_POR, //serratore: forzo anche su INTMMG (se passano i vari controlli) le prestazioni mmg da portale
		      ABIL_SO_CAMBIO_DISTRETTO_SEDE, //BOFFA: abilitazione per il cambio del distretto e delle sede della scheda
		      ABIL_FILTRO_MODULO_RICHIESTA, //BOFFA: abilitazione filtro sul modulo
		      GG_DA_PROROGA, //VFR: numero di gg massimo prima della scadenza per inserire una proroga
		      CTS_PHT2_NOME_APPLICATIVO_DEPLOYATO, //BOFFA: nome pht2 deployata sullo stesso tomcat
		      "WEBHS_RICHIESTA", 
		      CTS_OBBLIGO_SO_STATO_CIVILE,
		      CTS_OBBLIGO_SO_NUMERO_FAMILIARI,
		      CTS_OBBLIGO_SO_ASSISTENZA_NON_FAMILIARE,
			  CTS_OBBLIGO_SO_CON_CHI_VIVE,
			  CTS_OBBLIGO_SO_RAZZA_ETNIA,
			  CTS_OBBLIGO_SO_LINGUA,
			  CTS_OBBLIGO_SO_TITOLO_STUDIO,
			  CTS_OBBLIGO_SO_RICHIEDENTE,
			  CTS_OBBLIGO_SO_STRUTTURA_PROVENIENZA,
			  CTS_OBBLIGO_SO_MOTIVO_RICHIESTA,
			  SO_NO_OBBL_SCALA_VAL_BISOGNI,
			  CONF_OBB_RICOVERO_STANZA, 
			  CONF_OBB_RICOVERO_POSTO_LETTO, 
			  GG_SCADENZA_RICOVERI_RSA, // boffa: gg di scadenza del ricovero
			  ABIL_CHECKB_PORT,
			  ABILIT_INVIO_FSE, // serratore: Abil. inoltro PAI a FSE
			  FAR_ON_RUG, //s.puntoni abilitazione estrazione flussi far da RUGVAL anziché da SCL_VALUTAZIONE
			  AG_NN_ES,
			  PRESTPREL,
		    };

		    String zona_op = CaribelSessionManager.getInstance().getStringFromProfile(ZONA_OPERATORE);
		    
		    h.put("conf_kproc","SINS");
		    h.put("keys",key);
		    h.put("zona_op", zona_op);
		   		
			hRet = (Hashtable)CaribelClass.isasInvoke(confEJB, "selectAbilitazioni", h);
			
			String abil_anagcom=hRet.get("abil_anagcom").toString();	    
		    CaribelSessionManager.getInstance().addParamToProfile("abil_anagcom", abil_anagcom);
		    String vals[]=(String [])hRet.get("vals");
		    Hashtable hProfile = new Hashtable();
		    for (int i=0;i<vals.length;i++){
		        // 12/02/08: configurazione diversa per ogni zona
		        String valore = ConfEJB.getValxZona(vals[i].trim(), zona_op);
//		        profile.putParameter(profiles[i], valore);
		        hProfile.put(profiles[i], valore);
		    }
		    
		    CaribelSessionManager.getInstance().addParamsToProfile(hProfile);

		}catch(Exception ex){
			Messagebox.show(
					"Reperimento abilitazioni non riuscito!\nContattare l'assistenza.",
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
			return false;
		}
		return true;
	}

}

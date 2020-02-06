package it.caribel.app.sinssnt.util;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChiaviISASSinssntWeb extends ManagerChiaviISAS {

	static final Log logger = LogFactory.getLog(ChiaviISASSinssntWeb.class);

	// CHIAVI ISAS PER GLI ACCESSI
	public static final String INTERV = "INTERV";
	public static final String TABPIPP = "TABPIPP";
	public static final String ACCSPE = "ACCSPE";
	public static final String CONS_MIL = "CONS_MIL";//Convalida prestazioni MMG ex Millenium (Form Consultazione)
	public static final String INTCHGOP = "INTCHGOP"; //diritto di cambiare l'operatore esecutore
	
	// CHIAVI ISAS PER LISTA ATTIVITA
	public static final String LISTA_ATTIVITA = "LSTATTIV";

	// CHIAVI ISAS PER CARTELLA
	public static final String CARTELLA = "CARTELLA";
	public static final String ANAGRAFICA_MODI = "ANAGMODI";//G.Brogi 26/04/11 - per disabilitare parte dei campi
	public static final String ANAGRAFICA_CLOSE = "CLS_ANAG";// 23/01/13 m: funzione ISAS x chiusura anagrafica

	// CHIAVI ISAS PER RICHIESTA MMG
	public static final String RICH_MMG = "RICH_MMG"; //finestra principale
	public static final String ARCH_RIC = "ARCH_RIC"; //pulsante archivia 
	public static final String PC_RIC = "PC_RIC"; //pulsante presa in carico 
	public static final String CONF_RIC = "CONF_RIC"; //pulsante conferma 

	// CHIAVI ISAS PER CONTATTO INFERMIERISTICO
	public static final String SKINF = "SKINF"; //finestra principale
	public static final String I_CONSTO = "I_CONSTO"; //pulsante storico contatti 

	// CHIAVI ISAS PER CONTATTO MEDICO 
	public static String CONTATTO_MEDICO = "SKMED";
	public static String CONTATTO_MEDICO_STORICO = "SKMEDSTO";
	//	 CHIAVI ISAS PER SEGRETERIA ORGANIZZATIVA 
	public static String SEGRETERIA_ORGANIZZATIVA =ChiaviIsasBase.SEGRETERIA_ORGANIZZATIVA;// "RASKPUAC";
	public static String SEGRETERIA_ORGANIZZATIVA_STORICO = "SCHESTOR";
	public static String ESTRAZIONE_FLUSSI_FLS21 = "FLSFL21S";
	public static String ESTRAZIONE_RUG_IIIHC = "ESTR_RUG";

	// CHIAVI ISAS PER CONTATTO FISIOTERAPICO
	public static final String SKFISIO = "SKFISIO";

	// CHIAVI ISAS PER CONTATTO MEDICO PALLIATIVISTA
	public static final String SKMEDPAL = "SKMEDPAL";

	// CHIAVI ISAS PER CONTATTO PUAC
	public static final String A_OPPUAC = "A_OPPUAC";

	//CHIAVI ISAS PER CONTATTO GENERICO -XX- VA SOSTITUITO OPPORTUNAMENTEO IL CODICE DELLA FIGURA PROFESSIONALE
	public static String CONTATTO_GENERICO = "SKFPG";
	public static String CONTATTO_GENERICO_STORICO = "SKFPGSTO";
	public static String CONTATTO_GENERICO_REFERENTE = "SKFPGREF";

	//CHIAVI ISAS PER L'AGENDA 
	public static final String ST_AGVIS = "ST_AGVIS"; //visualizzazione dell'agenda
	public static final String AGEMODOP = "AGEMODOP"; //modifica dell'operatore di cui stampare l'agenda
	public static final String AG_REG = "AG_REG"; //registrazioni prestaz
	public static final String AG_CAR = "AG_CAR"; //carica settimane
	public static final String AG_SPO = "AG_SPO"; //spostamento della pianificazione dell'agenda
	public static final String AG_OPE = "AG_OPE"; //cambio operatore
	public static final String APRIAGGR = "APRIAGGR"; //POSSIBILITà APRIRE AGENDA DI ALTRI OPERATORI NEL GRUPPO DI APPARTENENZA

	//CHIAVI ISAS PER IL DIARIO
	public static final String RMDIARIO = "RMDIARIO";

	/* CHIAVE ISAS PER INTOLLERANZA ALLERGIA */
	public static String CTS_INTOLLERANZE_ALLERGIE = "INTOALLE";
	//	TODO BOFFA GESTIRE LA DOCUMENTAZIONE DELLA CHIAVE ISAS PER LE SEGNALAZIONI
	/* CHIAVE ISAS PER SEGNALAZIONI */
	public static String CTS_SEGNALAZIONI = "SEGNALAZ";

	/* CHIAVE ISAS PER LA STAMPA DEL RIEPILO ASSISTITI */
	public static String CTS_RIEPILOGO_ASSISTITI = "RIEPASSI";
	public static String RIEPILO_ACCESSI = "INTACCES";
	/* CHIAVE ISAS PER LA MONITORAGGIO INDICATORI CURE DOMICILIARI */
	public static String STAMPA_MONITORAGGIO_CURE_DOMICILIARI = "MONIDTCD";

//	public static final String SC_BISOGNI = "SC_BISOG";
//	public static final String SCBISSAN = "SCBISSAN";
//	public static final String CONVALIDA_UVI = "CONV_UVI";
	public static final String FLS21 = "FLS21";
	// chiave isas per inserire il esito della valutazione uvi nella SO
	public static final String ESITO_VALUTAZIONI_UVI = "ESTVALVU";
	/*ATTRIBUZIONE OPERATORE REFENTE */
	public static final String ATTRIBUZIONE_OPERATORE_REFERENTE = "ATRNV_OP";
	
	/* CHIUSURA contatti attivi */
	public static final String CHISURA_CONTATTI_ATTIVI = "CHI_CONT";
	
	
	@Override
	protected void inserisciKFunk(List<InfoChiaveIsas> listaKey) {

		inserisciChiaveIsas(listaKey, CONTATTO_GENERICO, " CHIAVI ISAS PER CONTATTO GENERICO -XX- VA SOSTITUITO OPPORTUNAMENTEO IL CODICE DELLA FIGURA PROFESSIONALE ", new String[] { CONS,
				INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CONTATTO_GENERICO_STORICO, " contatto generico storico ", new String[] { CONS,
				INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CONTATTO_GENERICO_REFERENTE, "contatto generico referente ", new String[] { CONS,
				INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CARTELLA, "Cartella", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, ANAGRAFICA_MODI, "Per disabilitare parte dei campi", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, ANAGRAFICA_CLOSE, "Funzione ISAS x chiusura anagrafica", new String[] { CONS,
				INSE, MODI, CANC });

		inserisciChiaveIsas(listaKey, INTERV, "chiave per accessi", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, TABPIPP, "chiave per accessi", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, ACCSPE, "chiave per accessi", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CONS_MIL, "Convalida prestazioni MMG ex Millenium (Form Consultazione)",
				new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, ChiaviIsasBase.CONN_ATT, "CHIAVI ISAS PER LISTA UTENTI (CONNESSIONI) ATTIVE", new String[] {
				CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, LISTA_ATTIVITA, "CHIAVI ISAS PER LISTA ATTIVITA", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, RICH_MMG, "finestra principale", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, ARCH_RIC, " pulsante archivia ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, PC_RIC, " pulsante presa in carico ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CONF_RIC, " pulsante conferma  ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, SKINF, "CHIAVI ISAS PER CONTATTO INFERMIERISTICO", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, I_CONSTO, " pulsante storico contatti ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CONTATTO_MEDICO, "CHIAVI ISAS PER CONTATTO MEDICO ", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, CONTATTO_MEDICO_STORICO, " contatto medico storico ", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, SEGRETERIA_ORGANIZZATIVA, " CHIAVI ISAS PER SEGRETERIA ORGANIZZATIVA ",
				new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, SEGRETERIA_ORGANIZZATIVA_STORICO, " SO CONTATTO STORICO ", new String[] { CONS,
				INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, ESTRAZIONE_FLUSSI, " FLUSSI SIAD ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, ESTRAZIONE_RUG_IIIHC, " RUG III HC ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, SKFISIO, "CHIAVI ISAS PER CONTATTO FISIOTERAPICO ", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, SKMEDPAL, "CHIAVI ISAS PER CONTATTO MEDICO PALLIATIVISTA ", new String[] { CONS,
				INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, A_OPPUAC, " CHIAVI ISAS PER CONTATTO PUAC ", new String[] { CONS, INSE, MODI,
				CANC });
		inserisciChiaveIsas(listaKey, ST_AGVIS, "CHIAVI ISAS PER L'AGENDA ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, AGEMODOP, " modifica dell'operatore di cui stampare agenda ", new String[] {
				CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, AG_REG, " registrazioni  prestazioni ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, AG_CAR, " carica settimane ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, AG_SPO, " spostamento della pianificazione dell''agenda ", new String[] { CONS,
				INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, AG_OPE, " agenda cambio operatore ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, APRIAGGR,
				" POSSIBILITa'' APRIRE AGENDA DI ALTRI OPERATORI NEL GRUPPO DI APPARTENENZA ", new String[] { CONS,
						INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, RMDIARIO, "CHIAVI ISAS PER IL DIARIO ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CTS_INTOLLERANZE_ALLERGIE, "CHIAVE ISAS PER INTOLLERANZA ALLERGIA", new String[] {
				CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CTS_SEGNALAZIONI, "CHIAVE ISAS PER SEGNALAZIONI", new String[] { CONS, INSE,
				MODI, CANC });
		inserisciChiaveIsas(listaKey, CTS_RIEPILOGO_ASSISTITI, "CHIAVE ISAS PER LA STAMPA DEL RIEPILO ASSISTITI ",
				new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, SC_BISOGNI, " scala bisogno ", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, EXFLUSSI, "", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, SCBISSAN, "", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, CONVALIDA_UVI, "", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, FLS21, "", new String[] { CONS, INSE, MODI, CANC });
//		inserisciChiaveIsas(listaKey,, "Lista preferenze per so ", new String[] { CONS, INSE, MODI,
//				CANC });
//		inserisciChiaveIsas(listaKey, LISTA_GRADUATORIA_RSA, "Lista graduatoria RSA ", new String[] { CONS, INSE, MODI,
//				CANC });
		inserisciChiaveIsas(listaKey, ESITO_VALUTAZIONI_UVI, "Esito della valutazione uvi nella SO ", new String[] { CONS, INSE, MODI,
				CANC });
		
		inserisciChiaveIsas(listaKey, CHISURA_CONTATTI_ATTIVI, "CHIUSURA contatti attivi", new String[] { CONS, INSE, MODI,
				CANC });
		
		inserisciChiaveIsas(listaKey, ATTRIBUZIONE_OPERATORE_REFERENTE, "ATTRIBUZIONE NUOVO OPERATORE REFERENTE", new String[] { CONS, INSE, MODI,
				CANC });
		
		inserisciChiaveIsas(listaKey, ESTRAZIONE_FLUSSI_SU, "FLUSSI SIAD MODIFICARE DISTRETTO",	new String[] { MODI});
		inserisciChiaveIsas(listaKey, ESTRAZIONE_FLUSSI_FLS21, "FLUSSI FL21 MODIFICARE DISTRETTO",	new String[] { MODI});
		
		inserisciChiaveIsas(listaKey, LISTA_PREFERENZE_RSA, "Lista preferenze rsa", new String[] { CONS, INSE, MODI, CANC });
		inserisciChiaveIsas(listaKey, SCALA_VALUT_BARTHEL_MOD,  "chiave permesso scala Barthel Index Mod", CRUD);
		inserisciChiaveIsas(listaKey, SCALA_VALUT_DISABILITA_COM,  "chiave permesso scala Disabilità Comunicativa", CRUD);
		inserisciChiaveIsas(listaKey, SCALA_VALUT_FIM,  "chiave permesso scala FIM", CRUD);
	}

	public static void main(String[] args) {
		ChiaviISASSinssntWeb chiaviISASSinssntWeb = new ChiaviISASSinssntWeb();
		List<InfoChiaveIsas> listaKey = new LinkedList<InfoChiaveIsas>();
		logger.debug(" Popolo la struttura dati  \n\n");
		chiaviISASSinssntWeb.inserisciKFunk(listaKey);
		logger.debug(" Generazione delle chiavi isas \n\n");
		chiaviISASSinssntWeb.generaSqlInsert(listaKey, " SINSSNT WEB2 ");
		System.out.println("\n\nFine generazione delle chiavi isas ");
	}

}
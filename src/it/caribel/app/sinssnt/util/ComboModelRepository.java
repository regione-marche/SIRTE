package it.caribel.app.sinssnt.util;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.LogFactory;
import org.zkoss.util.resource.Labels;

public class ComboModelRepository{
	
	protected ComboModelRepository() {
    	LogFactory.getLog(this.getClass()).info("ComboModelRepository Version 0.0");    	
    }

 	public static Map<String, String> MODALITA_ACCESSO_DATI_SPA = 
     			new TreeMap<String, String>();

    public static Map<String, String> MODALITA_ACCESSO = 
 			new TreeMap<String, String>();

 	public static Map<String, String> FINALITA_PRESTAZIONE = 
     			new TreeMap<String, String>();
    
 	public static Map<String, String> TIPI_INDIVIDUAZIONE_UTENTE = 
     			new TreeMap<String, String>();
    
 	public static Map<String, String> TIPI_VERIFICA_CF = 
     			new TreeMap<String, String>();
    
 	public static Map<String, Map<String, String>> ESITI_TIPI_VERIFICA_CF = 
 			new TreeMap<String, Map<String, String>>();
 	private static Map<String, String> ESITI_C = 
 			new TreeMap<String, String>();
 	private static Map<String, String> ESITI_S = 
 			new TreeMap<String, String>();
 	private static Map<String, String> ESITI_A = 
 			new TreeMap<String, String>();

 	public static Map<String, String> CONTROLLI_BIFFATURA = new TreeMap<String, String>();
 	
 	public static Map<String, String> CONTROLLI_STAMPA_ELETTRONICA = 
     			new TreeMap<String, String>();
    
 	public static Map<String, String> SPA_SUGGERITA = 
     			new TreeMap<String, String>();
    
 	public static Map<String, String> SPA_TIPI_ASSISTITO = 
     			new TreeMap<String, String>();
    
    public static final Map<String, String> SPA_TIPI_PATOLOGIA = new TreeMap<String, String>();

	public static final Map<String, String> LOCALIZZAZIONE = new TreeMap<String, String>();;
	
	public static final Map<String, String> BISOGNO = new TreeMap<String, String>();

    static { 	
	    MODALITA_ACCESSO.put("1",Labels.getLabel("generic.modalita_accesso.programmata"));
	    MODALITA_ACCESSO.put("2",Labels.getLabel("generic.modalita_accesso.urgente"));
	    MODALITA_ACCESSO.put("3",Labels.getLabel("generic.modalita_accesso.nonUrgente"));
	    MODALITA_ACCESSO.put("4",Labels.getLabel("generic.modalita_accesso.differibile"));
				
		MODALITA_ACCESSO_DATI_SPA.put("01",Labels.getLabel("generic.modalita_accesso_dati_SPA.prescrizione_ricettario_SSN"));
		MODALITA_ACCESSO_DATI_SPA.put("02",Labels.getLabel("generic.modalita_accesso_dati_SPA.prescrizione_altro_ricettario"));
		MODALITA_ACCESSO_DATI_SPA.put("03",Labels.getLabel("generic.modalita_accesso_dati_SPA.accesso_diretto"));
		MODALITA_ACCESSO_DATI_SPA.put("04",Labels.getLabel("generic.modalita_accesso_dati_SPA.pronto_soccorso"));
		MODALITA_ACCESSO_DATI_SPA.put("05",Labels.getLabel("generic.modalita_accesso_dati_SPA.pre_ricovero"));
		MODALITA_ACCESSO_DATI_SPA.put("07",Labels.getLabel("generic.modalita_accesso_dati_SPA.post_ricovero"));
		MODALITA_ACCESSO_DATI_SPA.put("08",Labels.getLabel("generic.modalita_accesso_dati_SPA.iniziativa_aziendale"));
		MODALITA_ACCESSO_DATI_SPA.put("11",Labels.getLabel("generic.modalita_accesso_dati_SPA.libera_professione"));
		MODALITA_ACCESSO_DATI_SPA.put("12",Labels.getLabel("generic.modalita_accesso_dati_SPA.altro_accesso"));

	    FINALITA_PRESTAZIONE.put("10",Labels.getLabel("generic.finalita_prestazione.screening_oncologico"));
	    FINALITA_PRESTAZIONE.put("20",Labels.getLabel("generic.finalita_prestazione.donazione"));
	    FINALITA_PRESTAZIONE.put("40",Labels.getLabel("generic.finalita_prestazione.procreazione_medicalmente_assistita"));
	    FINALITA_PRESTAZIONE.put("50",Labels.getLabel("generic.finalita_prestazione.percorso_CORD"));
	    FINALITA_PRESTAZIONE.put("00",Labels.getLabel("generic.finalita_prestazione.altro"));
	    
	    TIPI_INDIVIDUAZIONE_UTENTE.put("1", Labels.getLabel("generic.tipi_individuazione_utente.codiceFiscale"));
	    TIPI_INDIVIDUAZIONE_UTENTE.put("3", Labels.getLabel("generic.tipi_individuazione_utente.stranieroTemporaneamentePresente"));
	    TIPI_INDIVIDUAZIONE_UTENTE.put("4", Labels.getLabel("generic.tipi_individuazione_utente.anonimo"));
	    TIPI_INDIVIDUAZIONE_UTENTE.put("5", Labels.getLabel("generic.tipi_individuazione_utente.cittadinoUnioneEuropea"));
	    TIPI_INDIVIDUAZIONE_UTENTE.put("6", Labels.getLabel("generic.tipi_individuazione_utente.altro"));

	    TIPI_VERIFICA_CF.put("S", Labels.getLabel("generic.tipi_verifica_cf.successiva"));
	    TIPI_VERIFICA_CF.put("C", Labels.getLabel("generic.tipi_verifica_cf.contestuale"));
	    TIPI_VERIFICA_CF.put("A", Labels.getLabel("generic.tipi_verifica_cf.assente"));
	    
	    ESITI_S.put("01", Labels.getLabel("generic.esiti_s.codiceFistcaleDifforme"));
	    ESITI_S.put("02", Labels.getLabel("generic.esiti_s.codiceFistcaleConforme"));
	    ESITI_C.put("10", Labels.getLabel("generic.esiti_c.effettuataPositiva"));
	    ESITI_C.put("11", Labels.getLabel("generic.esiti_c.efettuataNegativa"));
	    ESITI_C.put("12", Labels.getLabel("generic.esiti_c.nonEffettuataMancanzaTessera"));
	    ESITI_C.put("13", Labels.getLabel("generic.esiti_c.nonEffettuataMancanzaRegistrazione"));
	    ESITI_C.put("14", Labels.getLabel("generic.esiti_c.effettuataRegionalePositiva"));
	    ESITI_C.put("15", Labels.getLabel("generic.esiti_c.effettuataRegionaleNegativa"));
	    ESITI_C.put("16", Labels.getLabel("generic.esiti_c.nonEffettuata"));
	    ESITI_A.put("00", Labels.getLabel("generic.esiti_a.vuota"));
	    
	    ESITI_TIPI_VERIFICA_CF.put("S", ESITI_S);
	    ESITI_TIPI_VERIFICA_CF.put("C", ESITI_C);
	    ESITI_TIPI_VERIFICA_CF.put("A", ESITI_A);
	    
	    CONTROLLI_BIFFATURA.put(" ", Labels.getLabel("generic.controlli_biffatura.vuota"));
	    CONTROLLI_BIFFATURA.put("0", Labels.getLabel("generic.controlli_biffatura.assente"));
	    CONTROLLI_BIFFATURA.put("1", Labels.getLabel("generic.controlli_biffatura.presenteCFinBarcode"));
	    CONTROLLI_BIFFATURA.put("2", Labels.getLabel("generic.controlli_biffatura.presente"));
	    
	    CONTROLLI_STAMPA_ELETTRONICA.put("1", Labels.getLabel("generic.controlli_stampa_elettronica.proceduraInformatica"));
	    CONTROLLI_STAMPA_ELETTRONICA.put("2", Labels.getLabel("generic.controlli_stampa_elettronica.proceduraNonInformatica"));
	    CONTROLLI_STAMPA_ELETTRONICA.put("3", Labels.getLabel("generic.controlli_stampa_elettronica.nessunaUlterioreVerifica"));
	    CONTROLLI_STAMPA_ELETTRONICA.put(" ", Labels.getLabel("generic.controlli_stampa_elettronica.vuoto"));

	    SPA_SUGGERITA.put("01", Labels.getLabel("generic.spa_suggerita.si"));
	    SPA_SUGGERITA.put("02", Labels.getLabel("generic.spa_suggerita.altro"));
	    SPA_SUGGERITA.put("03", Labels.getLabel("generic.spa_suggerita.nonRilevato"));
	    SPA_SUGGERITA.put("", Labels.getLabel("generic.spa_suggerita.vuoto"));
	    
	    SPA_TIPI_ASSISTITO.put("01", Labels.getLabel("generic.spa_tipi_assistito.assicuratiExtraeuropei"));
	    SPA_TIPI_ASSISTITO.put("02", Labels.getLabel("generic.spa_tipi_assistito.assistitiSASNambulatoriale"));
	    SPA_TIPI_ASSISTITO.put("03", Labels.getLabel("generic.spa_tipi_assistito.assistitiSASNdomiciliare"));
	    SPA_TIPI_ASSISTITO.put("04", Labels.getLabel("generic.spa_tipi_assistito.assistitiIstituzioniEstere"));
	    SPA_TIPI_ASSISTITO.put("05", Labels.getLabel("generic.spa_tipi_assistito.assistitiSASNextraeuropei"));
	    SPA_TIPI_ASSISTITO.put("99", Labels.getLabel("generic.spa_tipi_assistito.assistitiAltraAssistenza"));
	    
	    SPA_TIPI_PATOLOGIA.put("01", Labels.getLabel("generic.spa_tipo_patologia.acuta"));
	    SPA_TIPI_PATOLOGIA.put("02", Labels.getLabel("generic.spa_tipo_patologia.nonAcuta"));
	    SPA_TIPI_PATOLOGIA.put("03", Labels.getLabel("generic.spa_tipo_patologia.nonRilevata"));
	    SPA_TIPI_PATOLOGIA.put("", Labels.getLabel("generic.spa_tipo_patologia.vuoto"));

	    LOCALIZZAZIONE.put("1", Labels.getLabel("generic.indirizzo.localizzazione.centro"));
	    LOCALIZZAZIONE.put("2", Labels.getLabel("generic.indirizzo.localizzazione.periferia"));
	    LOCALIZZAZIONE.put("3", Labels.getLabel("generic.indirizzo.localizzazione.frazione"));
	    
	    
	    BISOGNO.put("COMP_DIST_COGN_GRAVE", Labels.getLabel("scalaBisogni.comportamento.cogn_grave"));
	    BISOGNO.put("COMP_DIST_COGN_MODERATO", Labels.getLabel("scalaBisogni.comportamento.cogn_moderato"));
	    BISOGNO.put("COMP_DIST_COMP", Labels.getLabel("scalaBisogni.comportamento.comportamentale"));
	    BISOGNO.put("COMP_PSICO_SALUTE", Labels.getLabel("scalaBisogni.comportamento.condizioni_salute_psi"));
	    BISOGNO.put("CUTE_ALTRO", Labels.getLabel("scalaBisogni.tegumentario.altro"));		
	    BISOGNO.put("CUTE_CURA", Labels.getLabel("scalaBisogni.tegumentario.cura"));
	    BISOGNO.put("CUTE_LACERAZIONI", Labels.getLabel("scalaBisogni.tegumentario.lacerazioni"));
	    BISOGNO.put("CUTE_ULCERE12", Labels.getLabel("scalaBisogni.bisogno.cute_ulcere12"));
	    BISOGNO.put("CUTE_ULCERE34", Labels.getLabel("scalaBisogni.bisogno.cute_ulcere34"));
	    BISOGNO.put("CUTE_PRESSIONE", Labels.getLabel("scalaBisogni.tegumentario.pressione"));
	    BISOGNO.put("GASTR_INCONT", Labels.getLabel("scalaBisogni.gastrointestinale.incont"));
	    BISOGNO.put("GASTR_SANG", Labels.getLabel("scalaBisogni.gastrointestinale.sang"));
	    BISOGNO.put("GASTR_STIPSI", Labels.getLabel("scalaBisogni.gastrointestinale.stipsi"));
	    BISOGNO.put("GASTR_STOMIA", Labels.getLabel("scalaBisogni.gastrointestinale.stomia"));
	    BISOGNO.put("GASTR_VOMITO", Labels.getLabel("scalaBisogni.gastrointestinale.vomito"));
	    BISOGNO.put("GASTR_DIARREA", Labels.getLabel("scalaBisogni.gastrointestinale.diarrea"));
	    BISOGNO.put("GENURI_CATETERISMO", Labels.getLabel("scalaBisogni.genitourinaria.cateterismo"));
	    BISOGNO.put("GENURI_DIALISI", Labels.getLabel("scalaBisogni.genitourinaria.dialisi"));
	    BISOGNO.put("GENURI_DIALISI_PERI", Labels.getLabel("scalaBisogni.bisogno.genuri_dialisi_peri"));
	    BISOGNO.put("GENURI_EMATURIA", Labels.getLabel("scalaBisogni.genitourinaria.ematuria"));
	    BISOGNO.put("GENURI_INCONT", Labels.getLabel("scalaBisogni.genitourinaria.incontinenza_uri"));
	    BISOGNO.put("GENURI_UROSTOMIA", Labels.getLabel("scalaBisogni.genitourinaria.urostomia"));
	    BISOGNO.put("NUTR_DIMAGRIMENTO", Labels.getLabel("scalaBisogni.nutrizionale.dimagrimento"));
	    BISOGNO.put("NUTR_DISFAGIA", Labels.getLabel("scalaBisogni.nutrizionale.disfagia"));
	    BISOGNO.put("NUTR_DISIDRATAZIONE", Labels.getLabel("scalaBisogni.nutrizionale.disidratazione"));
	    BISOGNO.put("ONCO_CHEMIOTERAPIA", Labels.getLabel("scalaBisogni.onco.chemio"));
	    BISOGNO.put("ONCO_DOLORE", Labels.getLabel("scalaBisogni.onco.dolore"));
	    BISOGNO.put("ONCO_ONCOLOGICO", Labels.getLabel("scalaBisogni.onco.onco"));
	    BISOGNO.put("ONCO_RADIOTERAPIA", Labels.getLabel("scalaBisogni.onco.radio"));
	    BISOGNO.put("ONCO_TERM_NON_ONCO", Labels.getLabel("scalaBisogni.onco.term_non_onco"));
	    BISOGNO.put("ONCO_TERM_ONCO", Labels.getLabel("scalaBisogni.onco.term_onco"));
	    BISOGNO.put("PREST_ECG", Labels.getLabel("scalaBisogni.prestazioni.ecg"));
	    BISOGNO.put("PREST_GESTIONE_CVC", Labels.getLabel("scalaBisogni.prestazioni.gestione_cvc"));
	    BISOGNO.put("PREST_PRELIEVO", Labels.getLabel("scalaBisogni.prestazioni.prelievo"));
	    BISOGNO.put("PREST_TELEMETRIA", Labels.getLabel("scalaBisogni.prestazioni.telemetria"));
	    BISOGNO.put("PREST_TERAPIA_EV", Labels.getLabel("scalaBisogni.prestazioni.terapia"));
	    BISOGNO.put("PREST_TERAPIA_SOTCUT", Labels.getLabel("scalaBisogni.prestazioni.terapia_sotcut"));
	    BISOGNO.put("PREST_TRASFUSIONI", Labels.getLabel("scalaBisogni.prestazioni.trasfusioni"));
	    BISOGNO.put("RESP_OSSIGENOTERAPIA", Labels.getLabel("scalaBisogni.respiratorio.ossigenoterapia"));
	    BISOGNO.put("RESP_PORTATORE_TRACHEO", Labels.getLabel("scalaBisogni.respiratorio.tracheostomia"));
	    BISOGNO.put("RESP_TOSSE_SECR", Labels.getLabel("scalaBisogni.respiratorio.tosse_secr"));
	    BISOGNO.put("RESP_VENTILOTERAPIA", Labels.getLabel("scalaBisogni.respiratorio.ventiloterapia"));
	    BISOGNO.put("RIAB_AFASIA", Labels.getLabel("scalaBisogni.riabilitazione.afasia"));
	    BISOGNO.put("RIAB_MANTENIMENTO", Labels.getLabel("scalaBisogni.riabilitazione.mantenimento"));
	    BISOGNO.put("RIAB_NEUROLOGICA", Labels.getLabel("scalaBisogni.riabilitazione.neurologica"));
	    BISOGNO.put("RIAB_ORTOPEDICA", Labels.getLabel("scalaBisogni.riabilitazione.ortopedica"));
	    BISOGNO.put("RISCHIO_FEBBRE", Labels.getLabel("generic.sintomi5"));
	    BISOGNO.put("RISCHIO_PRESENTE", Labels.getLabel("scalaBisogni.rischio.presente"));
	    BISOGNO.put("RITMO_ALTERATO", Labels.getLabel("scalaBisogni.sonno.alterato"));
	    BISOGNO.put("AUTONOMIA1", Labels.getLabel("scalaBisogni.autonomia.1"));
	    BISOGNO.put("AUTONOMIA2", Labels.getLabel("scalaBisogni.autonomia.2"));
	    BISOGNO.put("AUTONOMIA3", Labels.getLabel("scalaBisogni.autonomia.3"));
	    BISOGNO.put("STATO_VEGETATIVO", Labels.getLabel("scalaBisogni.autonomia.stato"));
	    BISOGNO.put("CP_ADP", Labels.getLabel("RichiestaMMG.principale.adp"));
	    BISOGNO.put("CP_AID", Labels.getLabel("RichiestaMMG.principale.aid"));
	    BISOGNO.put("CP_ARD", Labels.getLabel("RichiestaMMG.principale.ard"));
	    BISOGNO.put("CP_VSD", Labels.getLabel("RichiestaMMG.principale.vsd"));
		
	        
	    }
	    
    @SuppressWarnings("unchecked")
 	public static Set<Pair<Object, String>> SERVIZIO = 
     			new TreeSet<Pair<Object, String>>(Arrays.asList(
     					new ImmutablePair<Object, String>("AI","Assistenza integrativa"),
     					new ImmutablePair<Object, String>("DC","Doppio canale"),
     					new ImmutablePair<Object, String>("AD","ADI")
     					));
    @SuppressWarnings("unchecked")
 	public static Set<Pair<Object, String>> ABILZONA = 
     			new TreeSet<Pair<Object, String>>(Arrays.asList(
     					new ImmutablePair<Object, String>("N","Nessuna"),
     					new ImmutablePair<Object, String>("S","Solo la propria"),
     					new ImmutablePair<Object, String>("#","Tutte")
     					));
    @SuppressWarnings("unchecked")
 	public static Set<Pair<Object, String>> ABILDISTR_AI = 
     			new TreeSet<Pair<Object, String>>(Arrays.asList(
     					new ImmutablePair<Object, String>("N","Nessuno"),
     					new ImmutablePair<Object, String>("A","Solo Adulti"),
     					new ImmutablePair<Object, String>("I","Solo Infanzia"),
     					new ImmutablePair<Object, String>("#","Entrambi")
     					));
    @SuppressWarnings("unchecked")
 	public static Set<Pair<Object, String>> ABILDISTRETTO = 
     			new TreeSet<Pair<Object, String>>(Arrays.asList(
     					new ImmutablePair<Object, String>("N","Nessuno"),
     					new ImmutablePair<Object, String>("S","Solo il proprio"),
     					new ImmutablePair<Object, String>("#","Tutti")
     					));
}

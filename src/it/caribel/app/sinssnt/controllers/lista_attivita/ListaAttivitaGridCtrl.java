package it.caribel.app.sinssnt.controllers.lista_attivita;

import it.caribel.app.sins_pht.util.CostantiPHT;
import it.caribel.app.sinssnt.bean.nuovi.ListaAttivitaEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOBaseEJB;
import it.caribel.app.sinssnt.controllers.ContainerFisioterapicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerGenericoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerInfermieristicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerPalliativistaCtrl;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.contattoGenerico.ContattoGenFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.DatiStampaCtrl;
import it.caribel.app.sinssnt.util.DatiStampaRichiesti;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGrid;
import it.caribel.zk.generic_controllers.interfaces.AskDatiInput;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Window;

public class ListaAttivitaGridCtrl extends CaribelGrid implements AskDatiInput{

	private static final long serialVersionUID = 1L;

	private String myKeyPermission = "";
	private ListaAttivitaEJB myEJB = new ListaAttivitaEJB();
	
	public static final String myPathZul = "/web/ui/sinssnt/lista_attivita/listaAttivitaGrid.zul";
	
	public static final String LIVELLO_ALERT 	= "livello_alert";
	public static final String LIVELLO_ALERT1 	= "livello_alert1";
	public static final String LIVELLO_ALERT2 	= "livello_alert2";
	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	private Groupbox box_attivita;
	private CaribelCheckbox	checkAllAttiv;
	boolean caricareGrigliaDati= false;
	private CaribelCheckbox checkOrdAssistito;
	private CaribelCheckbox checkOrdData;
	private CaribelRadiogroup ordinamentoFonte;
	private CaribelRadiogroup metodoRicerca;
	private Radio rich_operatore;
	public static final String CTS_SEPARATORE_STAMPA= "X*X";
	private static final String ver = "32- ";

	/* servono per la gestione del pht2 */
	private String idSchedaPht2 =null;
	private String nCartellaPht2 =null;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission,new ListaAttivitaGridItemRenderer());
		super.doAfterCompose(comp);
//		allineaSede();
		boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();
		int posizioneSelezionato = 0;
		if (isSegreteriaOrganizzativa){
			rich_operatore.setVisible(false);
//			posizioneSelezionato++;
		}else {
			rich_operatore.setVisible(true);
		}
		metodoRicerca.setSelectedIndex(posizioneSelezionato);
	}
	
	public void onChangeScheltaSede(Event e) {
		doCerca();
	}
	
	public void onCheck$checkAllAttiv(Event e) {
		String punto = ver + "onCheck$checkAllAttiv ";
		logger.trace(punto + " Tutte le fonti ");
		settaTutteFonti(checkAllAttiv.isChecked());
		doCerca();
	}
	
	private void settaTutteFonti(boolean check) {
		String punto = ver  + "tutte le fonti ";
		for (Iterator it = box_attivita.getChildren().iterator(); it.hasNext();) {
			Component corr = (Component)it.next();
			if (corr != null && (corr instanceof CaribelCheckbox)) {
				((CaribelCheckbox)corr).setChecked(check);
			}
			logger.trace(punto + " check>>" + check + "<");
		}
	}
	
	
	protected void doApri(){
		try{
			CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
			
			Listitem item = this.caribellb.getSelectedItem();
			ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");

			int n_cartella 			= ((Integer)dbrFromGrid.get("n_cartella")).intValue();
			int id_richiesta 		= ((Integer)dbrFromGrid.get("id_richiesta")).intValue();
//			String fonte 			= ""+dbrFromGrid.get("fonte");
			String valFonte 		= ""+dbrFromGrid.get("fonte");
			String fonte 			= Costanti.recuperaFonte(valFonte)+"";
	    	String tipo_operatore 	= (String)dbrFromGrid.get("tipo_operatore");
	    	String cod_operatore 	= (String)dbrFromGrid.get("cod_operatore");
	    	String fonteDettaglio 	= dbrFromGrid.get("fonte_dettaglio")+"";
	    	String zona				= (String)dbrFromGrid.get("cod_zona");
	    	String distretto		= (String)dbrFromGrid.get("cod_distretto");
	    	
	    	String op_zona = getProfile().getStringFromProfile(ManagerProfile.ZONA_OPERATORE);  
	    	String op_distretto = getProfile().getStringFromProfile(ManagerProfile.DISTRETTO_OPERATORE);  
	    	
			if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI+"")
					|| ( 
//							(fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI+"") ||
							( fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PUA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICOVERI_IN_SCADENZA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1+"")) 
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD+"")
						 && (UtilForContainer.isSegregeriaOrganizzativa())
						)	
						
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE+"")
					|| (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"") && fonteDettaglio
							.equals(CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA))
					||( fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI+"") && 
							containerCorr instanceof ContainerPuacCtrl)	) {
				if (iu.canIUse(ChiaviISASSinssntWeb.A_OPPUAC)) {
					if (!(containerCorr instanceof ContainerPuacCtrl)) {
						showNotificationChangeAreaAt(Labels.getLabel("menu.puac"));
					}
					String parametri = CostantiSinssntW.CTS_FONTE + "=" + fonte + "&" + CostantiSinssntW.N_CARTELLA + "=" + n_cartella;
					if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PUA+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICOVERI_IN_SCADENZA+"")) {
						parametri += (ISASUtil.valida(parametri) ? "&" : "") + CostantiSinssntW.CTS_ID_RICHIESTA+ "="
								+ id_richiesta;
					}else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG+"")) {
						parametri += (ISASUtil.valida(parametri) ? "&" : "") + CostantiSinssntW.CTS_ID_RICH + "="
								+ id_richiesta;
					} else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI+"")
//							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE+"")
							|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"")) {
						parametri += (ISASUtil.valida(parametri) ? "&" : "") + CostantiSinssntW.CTS_ID_SKSO + "="
								+ id_richiesta;
					} else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI+"")) {
						parametri += (ISASUtil.valida(parametri) ? "&" : "") + CostantiSinssntW.N_CARTELLA + "="
								+ n_cartella +"&" +CostantiSinssntW.CTS_ID_SKSO +"=" +id_richiesta;
					}
					if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+"")){
						doGestionePht2(dbrFromGrid, parametri);
					}else {
						Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul + "?" + parametri);
					}
					// CostantiSinssntW.CTS_FONTE+"="+fonte+"&"+CostantiSinssntW.N_CARTELLA+"="+n_cartella+"&"+
					// CostantiSinssntW.CTS_ID_RICH+"="+id_richiesta);
				} else if (!overrideAlert(fonte, fonteDettaglio))
					showAlertPermission();
			} else if ((fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"") 
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"")
//					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0+"")
					|| fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1+"")
					) && tipo_operatore != null) {// Prese carico CD
				if (cod_operatore != null && cod_operatore.trim().length() > 0) {
					if (!(op_zona.equals(zona) && (op_distretto.equals(distretto) || distretto.equals("0000")))) {
						showAlertPermission();
						return;
					}
				}
				if ( (tipo_operatore.equals(GestTpOp.CTS_COD_INFERMIERE) 
					|| GestTpOp.stessoOperatore(tipo_operatore, GestTpOp.CTS_COD_INFERMIERE) )
						&& iu.canIUse(ChiaviISASSinssntWeb.SKINF)) {
					if (!(containerCorr instanceof ContainerInfermieristicoCtrl))
						showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.inf"));
					Executions.getCurrent().sendRedirect(
							ContainerInfermieristicoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
									+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
									+ id_richiesta);
				} else if ( 
						(tipo_operatore.equals(GestTpOp.CTS_COD_MEDICO) || GestTpOp.stessoOperatore(tipo_operatore, GestTpOp.CTS_COD_MEDICO))
						&& iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_MEDICO)) {
					if (!(containerCorr instanceof ContainerMedicoCtrl))
						showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.medico"));
					Executions.getCurrent().sendRedirect(
							ContainerMedicoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
									+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
									+ id_richiesta);
				} else if ( (tipo_operatore.equals(GestTpOp.CTS_COD_FISIOTERAPISTA) 
							|| GestTpOp.stessoOperatore(tipo_operatore, GestTpOp.CTS_COD_MEDICO) )
						&& iu.canIUse(ChiaviISASSinssntWeb.SKFISIO)) {
					if (!(containerCorr instanceof ContainerFisioterapicoCtrl))
						showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.fisio"));
					Executions.getCurrent().sendRedirect(
							ContainerFisioterapicoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
									+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
									+ id_richiesta);
				}				
				//Carlo Volpicelli - 09/05/2017. Aggiungo il caso in cui l'operatore è il medico palliativista
				else if ( (tipo_operatore.equals(GestTpOp.CTS_MEDICO_CURE_PALLIATIVE) 
						|| GestTpOp.stessoOperatore(tipo_operatore, GestTpOp.CTS_MEDICO_CURE_PALLIATIVE) )
					&& iu.canIUse(ChiaviISASSinssntWeb.SKMEDPAL)) {
				if (!(containerCorr instanceof ContainerPalliativistaCtrl))
					showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.palliat"));
				Executions.getCurrent().sendRedirect(
						ContainerPalliativistaCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
								+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
								+ id_richiesta);
				}//
				else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA+"")){
					if(apriConContainerGenerico(ManagerProfile.getTipoOperatore(getProfile()), n_cartella, id_richiesta, fonteDettaglio, containerCorr))
					logger.debug("Ho aperto con container generico!");
				}else {
					if(apriConContainerGenerico(tipo_operatore, n_cartella, id_richiesta, fonteDettaglio, containerCorr))
						logger.debug("Ho aperto con container generico!");
					else
						showAlertPermission();
				}
			} else {
				if(fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI+"")){
					if (containerCorr instanceof ContainerMedicoCtrl) {
						Executions.getCurrent().sendRedirect(
								ContainerMedicoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
										+ id_richiesta);
					} else if ((containerCorr instanceof ContainerInfermieristicoCtrl)){
						Executions.getCurrent().sendRedirect(
								ContainerInfermieristicoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
										+ id_richiesta);
					}else if (containerCorr instanceof ContainerFisioterapicoCtrl){
						Executions.getCurrent().sendRedirect(
								ContainerFisioterapicoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
										+ id_richiesta);
					}//Carlo Volpicelli - 10/05/2017
					else if (containerCorr instanceof ContainerPalliativistaCtrl){
						Executions.getCurrent().sendRedirect(
								ContainerPalliativistaCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
										+ id_richiesta);
					}//
					else if(containerCorr instanceof ContainerGenericoCtrl){
						Executions.getCurrent().sendRedirect(
								ContainerGenericoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
										+ id_richiesta + "&" + CostantiSinssntW.TIPO_OPERATORE + "=" + tipo_operatore);
					}else {
						if (!overrideAlert(fonte, fonteDettaglio))
							showAlertPermission();
					}
				}
			}
		}catch(Exception e){
			doShowException(e);
		}
	}

	private void doGestionePht2(ISASRecord dbrFGrid, String parametri) {
		String punto = ver + "doGestionePht2 ";
		logger.debug(punto + "Inizio con dati >>" + dbrFGrid.getHashtable()+"");
		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		
		nCartellaPht2 = ISASUtil.getValoreStringa(dbrFGrid, Costanti.N_CARTELLA);
		idSchedaPht2 = ISASUtil.getValoreStringa(dbrFGrid, Costanti.CTS_ID_RICHIESTA);
		String assistito = ISASUtil.getValoreStringa(dbrFGrid, "cognome") +" " + ISASUtil.getValoreStringa(dbrFGrid, "nome");
		
		String codZonaAppartenenza = ISASUtil.getValoreStringa(dbrFGrid, CostantiSinssntW.CTS_L_ASSISTITO_COD_ZONA);
		String codDistrettoAppartenenza = ISASUtil.getValoreStringa(dbrFGrid,CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO);
		String codPresidioAppartenenza = ISASUtil.getValoreStringa(dbrFGrid,CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD);
		
		boolean esisteSksoAttiva = verificaEsistenzaSkso(nCartellaPht2);
		if (esisteSksoAttiva){
			logger.trace(punto + " Esiste una scheda so: chiedo se si vuole archiviare la richiesta del pht2");
			String messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.richiesta.archiviazione", new String[]{assistito});
			Messagebox.show(messaggio,
					Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception{
					if (Messagebox.ON_YES.equals(event.getName())) {
						archiviaRichiestaPht2();
					}
				}
			});
		}else if (!(ISASUtil.getValoreStringa(dbrFGrid,CostantiSinssntW.CTS_L_ASSISTITO_COD_DISTRETTO).equals(ManagerProfile.getDistrettoOperatore(getProfile())))){
			logger.debug(punto + " Procedo con la richiesta di sposta la scheda so ");			
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_DISTRETTO_APPARTENENZA, codZonaAppartenenza);
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_DISTRETTO_APPARTENENZA, codDistrettoAppartenenza);
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_SEDE_APPARTENENZA, codPresidioAppartenenza);
			dati.put(AttribuzioneDistrettoPht2Ctrl.METHODNAME, "caricaSchedaPHT2");
			
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_INFO_ASSISTITO,assistito);
			dati.put(Costanti.N_CARTELLA, nCartellaPht2);
			dati.put(AttribuzioneDistrettoPht2Ctrl.CTS_ID_SCHEDA_PHT2, idSchedaPht2);
			
			Executions.getCurrent().createComponents(AttribuzioneDistrettoPht2Ctrl.myPathZul, self, dati);
		}else {
			
			Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul + "?" + parametri);
		}
	}
	
	protected void archiviaRichiestaPht2() {
		String punto = ver + "archiviaRichiestaPht2 ";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(Costanti.N_CARTELLA, nCartellaPht2);
		dati.put(Costanti.CTS_ID_RICHIESTA, idSchedaPht2);
		dati.put(CostantiPHT.dbNameStatoScheda, CostantiPHT.statoArchiviata);
		logger.trace(punto + " dati rercuperati>" +dati);
		RMSkSOBaseEJB rmSkSOBaseEJB = new RMSkSOBaseEJB();
		boolean archiviazioneOk = rmSkSOBaseEJB.modificaStatoPht2Generale(CaribelSessionManager.getInstance().getMyLogin(),dati);
		
		if (archiviazioneOk) {
			doCerca();
		}
	}

	private boolean verificaEsistenzaSkso(String nCartella) {
		String punto = ver + "verificaEsistenzaSkso ";
		boolean esisteSchedaSo = false;
		
		RMSkSOBaseEJB rmSkSOBaseEJB = new RMSkSOBaseEJB();
		Hashtable<String, String>dati = new Hashtable<String, String>();
		dati.put(Costanti.N_CARTELLA, nCartella);
		ISASRecord dbrRmSkso = null;
		try {
			dbrRmSkso = rmSkSOBaseEJB.selectSkValCorrenteNoISAS(CaribelSessionManager.getInstance().getMyLogin(), dati);
		} catch (CariException e) {
			logger.error(punto + " Errore nel recuperare i dati ", e);
		}
		esisteSchedaSo = (dbrRmSkso != null);
		logger.trace(punto + " Esiste una scheda so attiva: " + esisteSchedaSo);

		return esisteSchedaSo;
	}

	public  void caricaSchedaPHT2(Hashtable<String, String> dati) {
		String punto = ver + "caricaSchedaPHT2 ";
		logger.debug(punto + "inizio con dati >" + dati);
		int fonte = CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA;
		String n_cartella = ISASUtil.getValoreStringa(dati, Costanti.N_CARTELLA);
		String id_richiesta = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_ID_SCHEDA_PHT2);

		String parametri = CostantiSinssntW.CTS_FONTE + "=" + fonte + "&" + CostantiSinssntW.N_CARTELLA + "="
				+ n_cartella;
		parametri += "&" + CostantiSinssntW.CTS_ID_RICHIESTA + "=" + id_richiesta;
		logger.trace(punto + " parametri>>" +parametri);

		int operazioneDaEseguire = ISASUtil.getValoreIntero(dati, AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE);
		if (operazioneDaEseguire > 0) {
			switch (operazioneDaEseguire) {
			case AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE_CARICA_ASSISTITO:
				String assistito = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_INFO_ASSISTITO);
				String messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.carica.richiesta.msg", new String[]{assistito});
				Clients.showNotification(messaggio,"info", self, "middle_center",CostantiSinssntW.INT_TIME_OUT);
				Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul + "?" + parametri);
				break;
			case AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE_SPOSTA_ASSITITO:
				spostaUtentePht2(dati);
				break;
			case AttribuzioneDistrettoPht2Ctrl.OPERAZIONE_DA_EFFETTUARE_CANCELLA:
				doCerca();
				break;
			default:
				break;
			}
		}else {
			doCerca();
		}
		
	}

	private void spostaUtentePht2(Hashtable<String, String> dati) {
		String punto = ver + "spostaUtentePht2 ";
		logger.trace(punto + " inizio con dati >>" +dati +"< ");
		boolean aggiornamentoOk = false;
		try {
			aggiornamentoOk = myEJB.spostaUtentePht2(CaribelSessionManager.getInstance().getMyLogin(), dati);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String assistito = ISASUtil.getValoreStringa(dati, AttribuzioneDistrettoPht2Ctrl.CTS_INFO_ASSISTITO);
		String messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg.ko", new String[]{assistito});
		if (aggiornamentoOk){
			messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg.ok", new String[]{assistito});
		}
		Clients.showNotification(messaggio,"info", self, "middle_center",CostantiSinssntW.INT_TIME_OUT);
		doCerca();
	}

	public void setDatiInput(Hashtable<String, String> dati) throws Exception {
		String punto = ver + "setDatiInput ";
		logger.trace(punto + " inizio con dati >>" +dati +"< ");
		
		Component pandata = self.getFellowIfAny("attribuzioneDistrettoA");
		if(pandata != null){
			caricaSchedaPHT2(dati);
		}
		if(pandata != null){
			pandata.detach();
		}
		logger.trace(punto + " Chiudo il pannello ");

	}

	private boolean apriConContainerGenerico(String tipo_operatore,int n_cartella,int id_richiesta,String fonte,CaribelContainerCtrl containerCorr) throws Exception {
		Hashtable<String, String> tipiOp = ManagerOperatore.getTipiOperatori(CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE);
		Enumeration<String> n = tipiOp.keys();
		while (n.hasMoreElements()){
			String tipo_operatore_corr = (String)n.nextElement();
			if ( tipo_operatore.equals(tipo_operatore_corr)&&
					iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_GENERICO+tipo_operatore_corr)) {
				if (!(containerCorr instanceof ContainerGenericoCtrl) || 
						!((ContainerGenericoCtrl)containerCorr).getTipoOpFromMyInstance().equals(tipo_operatore_corr)) {
					String labelScheda = ContattoGenFormCtrl.getLabelScheda(tipo_operatore);
					showNotificationChangeAreaAt(labelScheda);
				}
				Executions.getCurrent().sendRedirect(
						ContainerGenericoCtrl.myPathZul + "?" + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
								+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.CTS_ID_SKSO + "="
								+ id_richiesta + "&" + ContainerGenericoCtrl.parameter_tipo_op + "=" + tipo_operatore);
				return true;
			}
		}
		return false;
	}

	private boolean overrideAlert(String fonte, String fonteDettaglio) {
		boolean ret = false;
		if ((fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"") && !fonteDettaglio.equals(CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA)))
			ret = true;
		return ret;
	}

	@Override
	public void doStampa() {
		String punto = ver + "doStampa ";
		try{

			String ejb    = "SINS_FOLISTAATTIVITA";
			String metodo = "query_attivita";
			String report = "lista_attivita";
			String codice_operatore = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
			
			String zona_operatore  		= CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
			String distr_operatore 		= CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");	
			
			
			impostaFiltro(true); 
			Hashtable<String, Object> parametri = new Hashtable<String, Object>();
			parametri.put("codice_operatore", codice_operatore);
			parametri.put("zona_operatore", zona_operatore);
			parametri.put("distr_operatore", distr_operatore);
			parametri.putAll(this.hParameters);
			DatiStampaRichiesti datiStampaRichiesti = new DatiStampaRichiesti(metodo, report, ejb, parametri);
			
			datiStampaRichiesti.setTitoloMaschera(((Window)self).getTitle());
			Hashtable<String , Object> dati = new Hashtable<String, Object>();
			dati.put(CostantiSinssntW.CTS_STAMPA_BEAN, datiStampaRichiesti);
			
			logger.trace(punto + " dati >>" + parametri+"<<");

			Executions.getCurrent().createComponents(DatiStampaCtrl.CTS_FILE_ZUL, self,dati);
			
		}catch(Exception ex){
			doShowException(ex);
		}
	}

	private String settaFiltroOrdinamento() {
		String ordinamento = "";
//		ordinamentoFonte
//		if ( ordinamentoFonte.get)
		String ordinamentoFt = ordinamentoFonte.getSelectedValue();
		if (ISASUtil.valida(ordinamentoFt) && ordinamentoFt.equalsIgnoreCase("S")){
			ordinamento += (ISASUtil.valida(ordinamento)?", " :"" ) + CostantiSinssntW.CTS_LISTA_AO_FONTE;
		}
		
		if (checkOrdData.isChecked()){
			ordinamento += (ISASUtil.valida(ordinamento)?", " :"" ) + CostantiSinssntW.CTS_LISTA_AO_DATA;
		}

		if (checkOrdAssistito.isChecked()){
			ordinamento +=(ISASUtil.valida(ordinamento)?",":"")+ CostantiSinssntW.CTS_LISTA_AO_ASSISTITO;
		}
		return ordinamento;
	}
	
	public void onClickedDeleteButton(Event event) throws Exception {
		if(this.caribellb !=null && this.caribellb.getSelectedIndex()==-1){
			UtilForUI.doAlertSelectOneRow();
			return;
		}
		Listitem item = this.caribellb.getSelectedItem();
		ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");
		
		String fonte 		= ""+dbrFromGrid.get("fonte");
		String descrFonte 	= Labels.getLabel("listaAttivitaGrid.attivita."+fonte);
		String cognome 		= (String)dbrFromGrid.get("cognome");
    	String nome 		= (String)dbrFromGrid.get("nome");
		
		Messagebox.show(Labels.getLabel("listaAttivitaGrid.delete.question",new String[]{descrFonte,cognome,nome}), 
				Labels.getLabel("messagebox.attention"),
				Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
				new EventListener<Event>() {
					public void onEvent(Event event)throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())){
							doDelete();
						}
					}
				});
	}
	
	private void doDelete(){
		try{
			Listitem item 			= this.caribellb.getSelectedItem();
			ISASRecord dbrFromGrid 	= (ISASRecord) item.getAttribute("dbr_from_grid");
			String n_cartella 		= ""+dbrFromGrid.get("n_cartella");
			String id_richiesta 	= ""+dbrFromGrid.get("id_richiesta");
			String fonte 			= ""+dbrFromGrid.get(CostantiSinssntW.CTS_FONTE);
			String tipo_operatore 	= ""+dbrFromGrid.get("tipo_operatore");
			String fonte_dettaglio 	= ""+dbrFromGrid.get("fonte_dettaglio");
			int numeroFonte = Costanti.recuperaFonte(fonte);
			if (numeroFonte>0){
				if(numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI){ 
		    		//CTS_TIPO_FONTE_COINVOLTI
					myEJB.concludiRichiestaRM_SKSO_OP_COINVOLTI(CaribelSessionManager.getInstance().getMyLogin(), n_cartella, id_richiesta,tipo_operatore);
		    		Clients.showNotification(Labels.getLabel("common.msg.ok.notification"),"info",self,"middle_center",2500);
				} else if (numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG) {
		    		//CTS_TIPO_FONTE_RICH_MMG
					myEJB.concludiRichiestaRM_RICH_MMG(CaribelSessionManager.getInstance().getMyLogin(), n_cartella, id_richiesta);
		    		Clients.showNotification(Labels.getLabel("common.msg.ok.notification"),"info",self,"middle_center",2500);
		    	}else if(numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA ){
		    		//CTS_TIPO_FONTE_PRIMA_VISITA
		    		myEJB.concludiRichiestaRM_SKSO(CaribelSessionManager.getInstance().getMyLogin(), n_cartella, id_richiesta);
		    		Clients.showNotification(Labels.getLabel("common.msg.ok.notification"),"info",self,"middle_center",2500);
		    	}else if(numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE ){
		    		//CTS_TIPO_FONTE_SO_VISTE
		    		if(fonte_dettaglio.equals(CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO4)){
			    		myEJB.concludiRichiestaRM_SKSO(CaribelSessionManager.getInstance().getMyLogin(), n_cartella, id_richiesta);
			    		Clients.showNotification(Labels.getLabel("common.msg.ok.notification"),"info",self,"middle_center",2500);
		    	}
		    	}else if(numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA){
		    		myEJB.annullaRichiestaChiusura(CaribelSessionManager.getInstance().getMyLogin(), n_cartella, id_richiesta, fonte_dettaglio);
			    		Clients.showNotification(Labels.getLabel("common.msg.ok.notification"),"info",self,"middle_center",2500);
		    	}
		    	//Ricarico la griglia
		    	doCerca();
			}
	    	
		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	@Override
	protected void doNuovo() {
		//N.A:
	}

	@Override
	protected void doPulisciRicerca() {
		//N.A:
	}
	
	@Override
	protected void doTrasmetti() {
		//N.A:
	}
	
	public void doCerca(){
		try{
			if (!caricareGrigliaDati){
				checkAllAttiv.setChecked(caricareGrigliaDati);
				settaTutteFonti(caricareGrigliaDati);
			}
			impostaFiltro(false);
			doRefreshNoAlert();
			doAbilitaFonteRicerca();
			caricareGrigliaDati = true;
		}catch(Exception e){
			e.printStackTrace();
			doShowException(e);  
		}
	}

	private void doAbilitaFonteRicerca() {
		String punto = ver + "doAbilitaFonteRicerca ";
		logger.debug(punto + " inizio con dati ");
		try {
			
			Vector<String> ordinamentoListaAttivita = new Vector<String>();
			
			if (UtilForContainer.isSegregeriaOrganizzativa()){
				ordinamentoListaAttivita.add(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG+"");
				ordinamentoListaAttivita.add(CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA+"");
				ordinamentoListaAttivita.add(CostantiSinssntW.CTS_TIPO_FONTE_PUA+"");
			}else {
				ordinamentoListaAttivita.add(CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"");
			}
			
			Vector<ISASRecord> fonteRicerca = myEJB.filtriRicercaFonte(CaribelSessionManager.getInstance().getMyLogin(), hParameters);
			LinkedList<String> tipoFonteL =(LinkedList<String>)this.hParameters.get(CostantiSinssntW.CTS_ELEMENTI_FONTI_LISTA);
			String fonte ;
			int numeroElementiFonte;
			disabilitaFonte();
			faiOrdinamento(ordinamentoListaAttivita, fonteRicerca, tipoFonteL,"color: red;");
			
			Vector<String> ordinamentoListaAttivitaGialla = new Vector<String>();
			ordinamentoListaAttivitaGialla.add(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0+"");
			ordinamentoListaAttivitaGialla.add(CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1+"");
			ordinamentoListaAttivitaGialla.add(Costanti.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD+""); 
			ordinamentoListaAttivitaGialla.add(CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE+""); 
			    
			faiOrdinamento(ordinamentoListaAttivitaGialla, fonteRicerca, tipoFonteL,"background:yellow");
			
			for (int i = 0; i < fonteRicerca.size(); i++) {
				ISASRecord dbrFonte = fonteRicerca.get(i);
				fonte  = ISASUtil.getValoreStringa(dbrFonte, CostantiSinssntW.L_AT_FONTE);
				if (giaInseritaFonte(ordinamentoListaAttivita,ordinamentoListaAttivitaGialla,fonte)){
					logger.trace(punto + " non aggiugo la fonte>>" + fonte+ "< stampata prima ");
				}else {
					logger.trace(punto + " Aggiugo la fonte>>" + fonte+ "<");
					numeroElementiFonte = ISASUtil.getValoreIntero(dbrFonte, CostantiSinssntW.CTS_AS_NUMERO);
					logger.trace(punto + " fonte>>" + fonte + " elementi>>" +numeroElementiFonte);
					impostaCheck(fonte, numeroElementiFonte, tipoFonteL, "");
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean giaInseritaFonte(Vector<String> ordinamentoListaAttivita,
			Vector<String> ordinamentoListaAttivitaGialla, String fonte) {

		boolean inseritaFonte = inseritaFonte(ordinamentoListaAttivita, fonte);
		if (!inseritaFonte){
			inseritaFonte = inseritaFonte(ordinamentoListaAttivitaGialla, fonte);
		}
		
		return inseritaFonte;
	}

	private boolean inseritaFonte(Vector<String> ordinamentoLista, String fonte) {
		boolean inseritaFonte = false;
		int i = 0;
		int numFonte = Costanti.recuperaFonte(fonte);
		if (numFonte > 0) {
			int fonteAttuale;
			while (i < ordinamentoLista.size() && !inseritaFonte) {
				fonteAttuale = Costanti.recuperaFonte(ordinamentoLista.get(i));
				inseritaFonte = (numFonte == fonteAttuale);
				i++;
			}
		}

		return inseritaFonte;
	}

	private void faiOrdinamento(Vector<String> ordinamentoListaAttivita, Vector<ISASRecord> fonteRicerca,
			LinkedList<String> tipoFonteL, String formattazione) {
		String punto = ver + "faiOrdinamento ";
		String fonte;
		int numeroElementiFonte;
		String fonteOrdinamento;
		int numFonte, numFonteOrdinamento;
		for (int j = 0; j < ordinamentoListaAttivita.size(); j++) {
			fonteOrdinamento = ordinamentoListaAttivita.get(j);
			numFonteOrdinamento = Costanti.recuperaFonte(fonteOrdinamento);
			if (ISASUtil.valida(fonteOrdinamento)) {
				int index = 0;
				while (index < fonteRicerca.size()) {
					ISASRecord dbrFonte = fonteRicerca.get(index);
					fonte = ISASUtil.getValoreStringa(dbrFonte, CostantiSinssntW.L_AT_FONTE);
					numFonte = Costanti.recuperaFonte(fonte);
					if (fonte.equals(fonteOrdinamento) || (numFonte>0 &&  numFonte == numFonteOrdinamento)) {
						numeroElementiFonte = ISASUtil.getValoreIntero(dbrFonte, CostantiSinssntW.CTS_AS_NUMERO);
						logger.trace(punto + " fonte>>" + fonte + " elementi>>" + numeroElementiFonte);
						impostaCheck(fonte, numeroElementiFonte, tipoFonteL, formattazione);
					} else {
						logger.trace(punto + " non e' la stessa fonte>>" + fonte + "< fonteOrdinamento>"
								+ fonteOrdinamento);
					}
					index++;
				}
			}
		}
	}
	
	
	private void disabilitaFonte() {
		String punto = ver +"disabilitaFonte ";
		logger.trace(punto + " gestire la disabilitazione delle fonti ");
		box_attivita.getChildren().clear();
	}

	private void impostaCheck(String fonte, int numeroElementiFonte, LinkedList<String> tipoFonteL, String style) {
		String punto = ver + "impostaCheck ";
		String testo = recuperaTestoFonte(fonte, numeroElementiFonte);
		aggiungiCheck(fonte,testo, numeroElementiFonte, tipoFonteL, style);
	}

	private String recuperaTestoFonte(String fonte, int numeroElementiFonte) {
		String testo = Labels.getLabel("listaAttivitaGrid.attivita_num."+fonte, new String[]{numeroElementiFonte+""});
		return testo;
	}

	private void aggiungiCheck(String fonte, String testo, int numeroElementiFonte, LinkedList<String> tipoFonteL, String style) {
		String punto = ver + "aggiungiCheck ";
		logger.trace(punto + " testo>" + testo);
		String styleCheck = "display: inline-block;";
		if (numeroElementiFonte>0){
			CaribelCheckbox checkAttivita = new CaribelCheckbox();
			if (ISASUtil.valida(style)){
				styleCheck +=style;
			}
			checkAttivita.setStyle(styleCheck);
			checkAttivita.setWidth("16em");
			checkAttivita.setLabel(testo);
			checkAttivita.setParent(box_attivita);
			checkAttivita.setValue(fonte+"");
			checkAttivita.setChecked(false);
			if (tipoFonteL != null && tipoFonteL.contains(fonte + "")) {
				checkAttivita.setChecked(true);
			} else {
				if (checkAllAttiv.isChecked()) {
					checkAllAttiv.setChecked(false);
				}
			}
			checkAttivita.addEventListener(Events.ON_CHECK, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					doCerca();
				}
			});
		}	
	}

	private void impostaFiltro(boolean sePerStampa)throws Exception{	
		//da Filtro di ricerca
		this.hParameters.put("dadata", dadata.getValueForIsas());
		this.hParameters.put("adata", adata.getValueForIsas());

		//Tipo fonte
		String tipoFonte = recuperaDatiFonte();
		this.hParameters.put("tipo_fonte", tipoFonte);
		LinkedList<String> tipoFonteL = recuperaDatiFonteL();
		

		if (sePerStampa) {
			String codTipofonte = "";
			String lineCodTipoFonte = "";
			for (int i = 0; i < tipoFonteL.size(); i++) {
				codTipofonte = tipoFonteL.get(i);
				if (ISASUtil.valida(codTipofonte)) {
					lineCodTipoFonte += (ISASUtil.valida(lineCodTipoFonte) ? CTS_SEPARATORE_STAMPA : "");
					lineCodTipoFonte += codTipofonte;
				}
			}
			this.hParameters.put(CostantiSinssntW.CTS_ELEMENTI_FONTI_LISTA, lineCodTipoFonte);
		} else {
			this.hParameters.put(CostantiSinssntW.CTS_ELEMENTI_FONTI_LISTA, (LinkedList<String>) tipoFonteL);
		}
		
		this.hParameters.put(CostantiSinssntW.CTS_LISTA_ATTIVITA_CARICA_DATI, new Boolean(caricareGrigliaDati));
		
		String ordinamento = settaFiltroOrdinamento();
		this.hParameters.put(CostantiSinssntW.CTS_LISTA_ATTIVITA_ORDINAMENTO, ordinamento);
		
		//Filtro su tipo_operatore
		this.hParameters.put("tipo_operatore", UtilForContainer.getTipoOperatorerContainer());
		
		boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();
		//Filtro su segreteria organizzativa 
		this.hParameters.put(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA, new Boolean(isSegreteriaOrganizzativa));
		
		this.hParameters.put(ManagerProfile.PRES_OPERATORE,ManagerProfile.getValue(getProfile(), ManagerProfile.PRES_OPERATORE));

		// Filtro per non aggiungere le fonti per le figure professionali
		if (!isSegreteriaOrganizzativa) {
			this.hParameters.put(CostantiSinssntW.CTS_FONTI_DA_ESCLUDERE,
					ManagerProfile.getValue(getProfile(), ManagerProfile.L_A_FONTI_DA_ESCLUDERE_FIG_PROF));
		} else if (isSegreteriaOrganizzativa) {
			this.hParameters.put(CostantiSinssntW.CTS_FONTI_DA_ESCLUDERE,
					ManagerProfile.getValue(getProfile(), ManagerProfile.L_A_FONTI_DA_ESCLUDERE_SO));
		}
		
		
		//Filtro su obbligo prima visita 
		boolean obbligoPV = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_OBB_CDI_PRIMA_VISITA);
		this.hParameters.put(ManagerProfile.SO_OBB_CDI_PRIMA_VISITA, new Boolean(obbligoPV));
		
		//Filtro su gg da far vedere agli operatori dopo che la segnalazione è stata vista dalla SO  
		String ggSegnalazioni = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_VISTA_SEGNALAZIONE_OPERATORE);
		this.hParameters.put(ManagerProfile.GG_VISTA_SEGNALAZIONE_OPERATORE, ggSegnalazioni);
		
//		Filtro su gg per mostrare le schede delle valutazioni bisogni   
		String ggValutazioni = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_VALUTAZIONE_BISOGNI);
		this.hParameters.put(ManagerProfile.GG_VALUTAZIONE_BISOGNI, ggValutazioni);
		
		String ggPregressoPrestazioni = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_PREGRESSO_PRESTAZIONI);
		this.hParameters.put(ManagerProfile.GG_PREGRESSO_PRESTAZIONI, ggPregressoPrestazioni);

		String codReg = ManagerProfile.getValue(getProfile(), ManagerProfile.CODICE_REGIONE);
		this.hParameters.put(ManagerProfile.CODICE_REGIONE, codReg);
		
		String codAzSan= ManagerProfile.getValue(getProfile(), ManagerProfile.CODICE_USL);
		this.hParameters.put(ManagerProfile.CODICE_USL, codAzSan);
		
		//Destinatari richieste
//		this.hParameters.put("rich_perso",""+checkDestMeStesso.isChecked());
//		this.hParameters.put("rich_altri",""+checkDestAltrui.isChecked());
		
		this.hParameters.put(CostantiSinssntW.CTS_L_RICHIESTE_DESTINATARI, metodoRicerca.getSelectedValue());
		
	}

	public String recuperaDatiFonte() {
		String tipoFonte = "";
		
		for (Iterator it = box_attivita.getChildren().iterator(); it.hasNext();) {
			Component corr = (Component) it.next();
			if (corr != null && (corr instanceof CaribelCheckbox)) {
				tipoFonte = recuperaValoreAttivita(tipoFonte, (CaribelCheckbox) corr);
			}
		}
		if(tipoFonte.equals(""))
			tipoFonte = "-1";//Se nessun check selezionato allora nessun record trovato!
		return tipoFonte;
	}
	
	private String recuperaValoreAttivita(String tipoFonte, CaribelCheckbox checkAttivita) {
		String valoreCheck = recuperaValoreChekAttivita(checkAttivita);
		if(ISASUtil.valida(valoreCheck)){
			tipoFonte +=(ISASUtil.valida(tipoFonte)?", ": "")+ valoreCheck;
		}
		return tipoFonte;
	}

	private String recuperaValoreChekAttivita(CaribelCheckbox checkAttivita) {
		String valoreCheck = "";
		if(checkAttivita!=null &&checkAttivita.isChecked())
			valoreCheck = checkAttivita.getValue();
		return valoreCheck;
	}

	public LinkedList<String> recuperaDatiFonteL() {
		String punto = ver + "recuperaDatiFonteL ";
		LinkedList<String> tipoFonte = new LinkedList<String>();
		String valoreCheck = "";
		for (Iterator it = box_attivita.getChildren().iterator(); it.hasNext();) {
			Component corr = (Component) it.next();
			if (corr != null && (corr instanceof CaribelCheckbox)) {
				CaribelCheckbox cbx = (CaribelCheckbox)corr;
	    		if (cbx!=null && (cbx instanceof CaribelCheckbox)) {
	    			 valoreCheck = recuperaValoreChekAttivita(cbx);
	    			if(ISASUtil.valida(valoreCheck)){
	    				tipoFonte.add(valoreCheck);
	    			}
				}	
			}
			
		}	
		if(tipoFonte.size() == 0 ){
			tipoFonte.add(Costanti.CTS_VALORE_DEFAULT_NESSUNO);//Se nessun check selezionato allora nessun record trovato!
		}
		
		return tipoFonte;
	}
	
	
	
	private void showNotificationChangeAreaAt(String nextArea){
		Clients.showNotification(Labels.getLabel(
				"listaAttivitaGrid.switch_container.notification",
				new String[] {nextArea.toUpperCase()}), "info", Path.getComponent("/main"), "middle_center", 2500);
	}
	private void showAlertPermission(){
		Messagebox.show(
				Labels.getLabel("exception.ISASPermissionDeniedException.msg"),
				Labels.getLabel("messagebox.attention"),
				Messagebox.OK,
				Messagebox.EXCLAMATION);
		
	}

}


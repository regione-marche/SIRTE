package it.caribel.app.sinssnt.controllers.lista_assistiti;

import it.caribel.app.sinssnt.bean.nuovi.ListaAssistitiEJB;
import it.caribel.app.sinssnt.controllers.ContainerFisioterapicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerGenericoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerInfermieristicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.contattoGenerico.ContattoGenFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.DatiStampaCtrl;
import it.caribel.app.sinssnt.util.DatiStampaRichiesti;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;

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
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

//public class ListaAssistitiGridCtrl extends CaribelGrid { 
public class ListaAssistitiGridCtrl extends CaribelGridCtrl {
	private static final long serialVersionUID = 1L;

	private String myKeyPermission = "";
	private ListaAssistitiEJB myEJB = new ListaAssistitiEJB();

	public static final String myPathZul = "/web/ui/sinssnt/lista_assistiti/listaAssistitiGrid.zul";

	public static final String LIVELLO_ALERT = "livello_alert";
	public static final String LIVELLO_ALERT1 = "livello_alert1";
	public static final String LIVELLO_ALERT2 = "livello_alert2";

	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();

	private Vlayout elenco_schede;
	private Hlayout riga_schede;
	private int elementiInSchedeAttive = 0;
	
	private CaribelDatebox dadata;
	private CaribelDatebox adata;
	private CaribelListheader opReferente;

	private LinkedList<String> lTipoFonte = new LinkedList<String>();

	private CaribelCheckbox checkAllAttiv;
	boolean caricareGrigliaDati = false;

	private CaribelCheckbox checkOrdAssistito;
	private CaribelCheckbox checkOrdData;
	private CaribelCheckbox checkContattiChiusi;
//	private CaribelCheckbox checkDestMeStesso_;
//	private CaribelCheckbox checkDestAltrui_;
	private CaribelRadiogroup metodoRicerca;
	private Radio rich_operatore;

	public static final String CTS_LISTA_ASSISTITI_DADATA = "dadata";
	public static final String CTS_LISTA_ASSISTITI_ADATA = "adata";
	public static final String CTS_LISTA_ASSISTITI_CONTATTI_CHIUSI = "la_ass_cont_ch";

	private static final String ver = "9- ";

	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathZul);
		super.doAfterCompose(comp);
		boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();
		
		int posizioneSelezionato = 0;
		if (isSegreteriaOrganizzativa){
			rich_operatore.setVisible(false);
			posizioneSelezionato++;
		}else {
			rich_operatore.setVisible(true);
		}
		metodoRicerca.setSelectedIndex(posizioneSelezionato);
		
//		allineaSede();
	}

//	private void allineaSede() {
//		String lCheckDestMeStesso = "";
//		String lCheckDestAltrui = "";
//		
//		String[] labels = new String[] {ManagerProfile.getOperatoreDescrizioneSede(getProfile())};
//		lCheckDestMeStesso = Labels.getLabel("listaAttivitaGrid.attivita.descrizione.sede",labels);
//		lCheckDestAltrui = Labels.getLabel("listaAttivitaGrid.attivita.dest.altre.sedi");
//	
//		checkDestAltrui.setLabel(lCheckDestAltrui);
//		checkDestMeStesso.setLabel(lCheckDestMeStesso);
//	}
//
//	
//	public void onCheck$checkDestAltrui(Event e){
//		String punto  =ver+ "onCheck$checkDestAltrui ";
//		logger.trace(punto + " devo selezionare/deselezionare un valore ");
//		if(!checkDestAltrui.isChecked() && !checkDestMeStesso.isChecked()){
//			checkDestMeStesso.setChecked(true);
//		}
//		doCerca();
//	}
//	
//	public void onCheck$checkDestMeStesso(Event e){
//		String punto  =ver+ "onCheck$checkDestMeStesso ";
//		logger.trace(punto + " devo selezionare/deselezionare un valore ");
//		if(!checkDestMeStesso.isChecked() && !checkDestAltrui.isChecked()){
//			checkDestAltrui.setChecked(true);
//		}
//		doCerca();
//	}

	public void onChangeScheltaSede(Event e) {
		doCerca();
	}

	public void onCheck$checkAssistiti1(Event e) {
		doCerca();
	}
	
	public void onCheck$checkAssistiti2(Event e) {
		doCerca();
	}

	public void onCheck$checkAssistiti3(Event e) {
		doCerca();
	}
	
	public void onCheck$checkContattiChiusi(Event e) {
		doCerca();
	}

	public void onCheck$checkAllAttiv(Event e) {
		String punto = ver + "onCheck$checkAllAttiv ";
		logger.trace(punto + " Tutte le fonti ");
		settaTutteFonti(checkAllAttiv.isChecked());
		doCerca();
	}

	private void settaTutteFonti(boolean check) {
		String punto = ver + "tutte le fonti ";
		for (Iterator it = elenco_schede.getChildren().iterator(); it.hasNext();) {
			Hlayout riga = (Hlayout) it.next();
			if (riga != null && (riga instanceof Hlayout)) {
				for (int i = 0; i < riga.getChildren().size(); i++) {
					CaribelCheckbox cbx = (CaribelCheckbox) riga.getChildren().get(i);
					if (cbx != null && (cbx instanceof CaribelCheckbox)) {
						cbx.setChecked(check);
					}
				}
			}
			logger.trace(punto + " check>>" + check + "<");
		}
	}

	protected void doApri() {
		String punto = ver +"doApri ";
		logger.trace(punto + " apro ");
		try {
			CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
			Listitem item = this.caribellb.getSelectedItem();
			ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");

			int n_cartella = ISASUtil.getValoreIntero(dbrFromGrid, CostantiSinssntW.CTS_L_ASSISTITO_N_CARTELLA);
			int n_contatto = ISASUtil.getValoreIntero(dbrFromGrid, CostantiSinssntW.CTS_L_ASSISTITO_N_CONTATTO);
			int idSkso = ISASUtil.getValoreIntero(dbrFromGrid, CostantiSinssntW.CTS_L_ASSISTITO_ID_SKSO);
			String fonte = "" + dbrFromGrid.get("fonte");
			String tipo_operatore = (String) dbrFromGrid.get("tipo_operatore");
			String cod_operatore = (String) dbrFromGrid.get("cod_operatore");
			String fonteDettaglio = dbrFromGrid.get("fonte_dettaglio") + "";
			String zona = (String) dbrFromGrid.get("cod_zona");
			String distretto = (String) dbrFromGrid.get("cod_distretto");
			boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();

			String op_corr = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
			String op_zona = getProfile().getStringFromProfile(ManagerProfile.ZONA_OPERATORE);
			String op_distretto = getProfile().getStringFromProfile(ManagerProfile.DISTRETTO_OPERATORE);
			
			int id_richiesta = 0;
			if (isSegreteriaOrganizzativa) {
//				UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.N_CARTELLA, n_cartella);
//				UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO, idSkso);
				String parametri = CostantiSinssntW.CTS_FONTE + "=" + fonte + "&" + CostantiSinssntW.N_CARTELLA + "=" + n_cartella;
				parametri += (ISASUtil.valida(parametri) ? "&" : "") + CostantiSinssntW.CTS_ID_SKSO + "=" + idSkso;
				Executions.getCurrent().sendRedirect(ContainerPuacCtrl.myPathZul + "?" + parametri);
			} else {
				if (ISASUtil.valida(tipo_operatore)) {
					UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.N_CARTELLA, n_cartella);
					UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.N_CONTATTO, n_contatto);
					
					if ((tipo_operatore.equals(GestTpOp.CTS_COD_INFERMIERE) || GestTpOp.stessoOperatore(tipo_operatore,
							GestTpOp.CTS_COD_INFERMIERE)) && iu.canIUse(ChiaviISASSinssntWeb.SKINF)) {
						if (!(containerCorr instanceof ContainerInfermieristicoCtrl))
							showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.inf"));
						Executions.getCurrent().sendRedirect(
								ContainerInfermieristicoCtrl.myPathZul + "?" //+ CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.N_CONTATTO + "="
										+ n_contatto );
					} else if ((tipo_operatore.equals(GestTpOp.CTS_COD_MEDICO) || GestTpOp.stessoOperatore(
							tipo_operatore, GestTpOp.CTS_COD_MEDICO)) && iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_MEDICO)) {
						if (!(containerCorr instanceof ContainerMedicoCtrl))
							showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.medico"));
						
						Executions.getCurrent().sendRedirect(
								ContainerMedicoCtrl.myPathZul + "?"// + CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.N_CONTATTO + "="
										+ n_contatto);
					} else if ((tipo_operatore.equals(GestTpOp.CTS_COD_FISIOTERAPISTA) || GestTpOp.stessoOperatore(
							tipo_operatore, GestTpOp.CTS_COD_FISIOTERAPISTA)) && iu.canIUse(ChiaviISASSinssntWeb.SKFISIO)) {
						if (!(containerCorr instanceof ContainerFisioterapicoCtrl))
							showNotificationChangeAreaAt(Labels.getLabel("menu.contatto.fisio"));
						
						Executions.getCurrent().sendRedirect(
								ContainerFisioterapicoCtrl.myPathZul + "?" //+ CostantiSinssntW.CTS_FONTE + "=" + fonte + "&"
										+ CostantiSinssntW.N_CARTELLA + "=" + n_cartella + "&" + CostantiSinssntW.N_CONTATTO + "="
										+ n_contatto);
					}//else if(apriConContainerGenerico(tipo_operatore, n_cartella, id_richiesta,containerCorr))
					 else if(apriConContainerGenerico(tipo_operatore, n_cartella, n_contatto, containerCorr)) //cv: passato n_contatto in luogo di id_richiesta al metodo
						 logger.debug("Ho aperto con container generico!");
				}else {
					logger.trace(punto + " tipo operatore non valido ");
				}
			}
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private boolean apriConContainerGenerico(String tipo_operatore, int n_cartella, int n_contatto,CaribelContainerCtrl containerCorr) throws Exception {
		Hashtable<String, String> tipiOp = ManagerOperatore.getTipiOperatori(CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE);
		Enumeration<String> n = tipiOp.keys();
		while (n.hasMoreElements()){
			String tipo_operatore_corr = (String)n.nextElement();
			if ((tipo_operatore.equals(tipo_operatore_corr) || GestTpOp.stessoOperatore(tipo_operatore, tipo_operatore_corr))
					&& iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_GENERICO+tipo_operatore_corr)) {
				if (!(containerCorr instanceof ContainerGenericoCtrl) || 
						!((ContainerGenericoCtrl)containerCorr).getTipoOpFromMyInstance().equals(tipo_operatore_corr)) {
					String labelScheda = ContattoGenFormCtrl.getLabelScheda(tipo_operatore);
					showNotificationChangeAreaAt(labelScheda);
				}
				Executions.getCurrent().sendRedirect(
						ContainerGenericoCtrl.myPathZul +
						"?" + CostantiSinssntW.N_CARTELLA + "="+ n_cartella +
						"&" + CostantiSinssntW.N_CONTATTO + "="+ n_contatto +
						"&" + ContainerGenericoCtrl.parameter_tipo_op + "=" + tipo_operatore);
			}
		}
		return false;
	}

	private boolean overrideAlert(String fonte, String fonteDettaglio) {
		boolean ret = false;
		if ((fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"") && !fonteDettaglio
				.equals(CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA)))
			ret = true;
		return ret;
	}

	@Override
	public void doStampa() {
		String punto = ver + "doStampa ";
		try {

			String ejb = "SINS_FOLISTAASSISTITI";
			String metodo = "query_assistiti";
			String report = "lista_assistiti";
			String codice_operatore = getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE);
			String codOperatoreSede = getProfile().getStringFromProfile(ManagerProfile.PRES_OPERATORE);
			String zona_operatore = CaribelSessionManager.getInstance().getStringFromProfile("zona_operatore");
			String distr_operatore = CaribelSessionManager.getInstance().getStringFromProfile("distr_operatore");

			impostaFiltro();
			Hashtable<String, Object> parametri = new Hashtable<String, Object>();
			parametri.put("codice_operatore", codice_operatore);
			parametri.put("zona_operatore", zona_operatore);
			parametri.put("distr_operatore", distr_operatore);
			parametri.put("codOperatoreSede", codOperatoreSede);
			parametri.putAll(this.hParameters);
			DatiStampaRichiesti datiStampaRichiesti = new DatiStampaRichiesti(metodo, report, ejb, parametri);

			datiStampaRichiesti.setTitoloMaschera(((Window) self).getTitle());
			Hashtable<String, Object> dati = new Hashtable<String, Object>();
			dati.put(CostantiSinssntW.CTS_STAMPA_BEAN, datiStampaRichiesti);

			logger.trace(punto + " dati >>" + parametri + "<<");

			Executions.getCurrent().createComponents(DatiStampaCtrl.CTS_FILE_ZUL, self, dati);

		} catch (Exception ex) {
			doShowException(ex);
		}
	}

	private String settaFiltroOrdinamento() {
		String ordinamento = "";
		// ordinamentoFonte
		// if ( ordinamentoFonte.get)
		// String ordinamentoFt = ordinamentoFonte.getSelectedValue();
		// if (ISASUtil.valida(ordinamentoFt) &&
		// ordinamentoFt.equalsIgnoreCase("S")){
		// ordinamento += (ISASUtil.valida(ordinamento)?", " :"" ) +
		// CostantiSinssntW.CTS_LISTA_AO_FONTE;
		// }

		if (checkOrdData.isChecked()) {
			ordinamento += (ISASUtil.valida(ordinamento) ? ", " : "") + CostantiSinssntW.CTS_LISTA_AO_DATA;
		}

		if (checkOrdAssistito.isChecked()) {
			ordinamento += (ISASUtil.valida(ordinamento) ? "," : "") + CostantiSinssntW.CTS_LISTA_AO_ASSISTITO;
		}

		if (checkOrdAssistito.isChecked()) {
			ordinamento += (ISASUtil.valida(ordinamento) ? "," : "") + CostantiSinssntW.CTS_LISTA_AO_ASSISTITO;
		}

		return ordinamento;
	}

	public void onClickedDeleteButton(Event event) throws Exception {
		if (this.caribellb != null && this.caribellb.getSelectedIndex() == -1) {
			UtilForUI.doAlertSelectOneRow();
			return;
		}
		Listitem item = this.caribellb.getSelectedItem();
		ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");

		String fonte = "" + dbrFromGrid.get("fonte");
		String descrFonte = Labels.getLabel("listaAttivitaGrid.attivita." + fonte);
		String cognome = (String) dbrFromGrid.get("cognome");
		String nome = (String) dbrFromGrid.get("nome");

		Messagebox.show(
				Labels.getLabel("listaAttivitaGrid.delete.question", new String[] { descrFonte, cognome, nome }),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION,
				new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							doDelete();
						}
					}
				});
	}

	private void doDelete() {
		try {
			Listitem item = this.caribellb.getSelectedItem();
			ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");
			String n_cartella = "" + dbrFromGrid.get("n_cartella");
			String id_richiesta = "" + dbrFromGrid.get("id_richiesta");
			String fonte = "" + dbrFromGrid.get(CostantiSinssntW.CTS_FONTE);
			String tipo_operatore = "" + dbrFromGrid.get("tipo_operatore");
			String fonte_dettaglio = "" + dbrFromGrid.get("fonte_dettaglio");

			if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"")) {
				// CTS_TIPO_FONTE_COINVOLTI
				myEJB.concludiRichiestaRM_SKSO_OP_COINVOLTI(CaribelSessionManager.getInstance().getMyLogin(),
						n_cartella, id_richiesta, tipo_operatore);
				Clients.showNotification(Labels.getLabel("common.msg.ok.notification"), "info", self, "middle_center",
						2500);
			} else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICH_MMG+"")) {
				// CTS_TIPO_FONTE_RICH_MMG
				myEJB.concludiRichiestaRM_RICH_MMG(CaribelSessionManager.getInstance().getMyLogin(), n_cartella,
						id_richiesta);
				Clients.showNotification(Labels.getLabel("common.msg.ok.notification"), "info", self, "middle_center",
						2500);
			} else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"")) {
				// CTS_TIPO_FONTE_PRIMA_VISITA
				myEJB.concludiRichiestaRM_SKSO(CaribelSessionManager.getInstance().getMyLogin(), n_cartella,
						id_richiesta);
				Clients.showNotification(Labels.getLabel("common.msg.ok.notification"), "info", self, "middle_center",
						2500);
			} else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE+"")) {
				// CTS_TIPO_FONTE_SO_VISTE
				if (fonte_dettaglio.equals(CostantiSinssntW.CTS_TIPO_FONTE_DETTAGLIO4)) {
					myEJB.concludiRichiestaRM_SKSO(CaribelSessionManager.getInstance().getMyLogin(), n_cartella,
							id_richiesta);
					Clients.showNotification(Labels.getLabel("common.msg.ok.notification"), "info", self,
							"middle_center", 2500);
				}
			} else if (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"")) {
				myEJB.annullaRichiestaChiusura(CaribelSessionManager.getInstance().getMyLogin(), n_cartella,
						id_richiesta, fonte_dettaglio);
				Clients.showNotification(Labels.getLabel("common.msg.ok.notification"), "info", self, "middle_center",
						2500);
			}
			// Ricarico la griglia
			doCerca();

		} catch (Exception ex) {
			doShowException(ex);
		}
	}

	@Override
	protected void doNuovo() {
		// N.A:
	}

	@Override
	protected void doPulisciRicerca() {
		// N.A:
	}

	@Override
	protected void doTrasmetti() {
		// N.A:
	}

	public void doCerca() {
		String punto = ver + "doCerca ";
		logger.trace(punto + " Ricerca");
		try {
			if (!caricareGrigliaDati) {
				checkAllAttiv.setChecked(caricareGrigliaDati);
				settaTutteFonti(caricareGrigliaDati);
			}
			impostaFiltro();
			doRefreshNoAlert();
			doAbilitaFonteRicerca();
			caricareGrigliaDati = true;
		} catch (Exception e) {
			e.printStackTrace();
			doShowException(e);
		}
	}

	private void doAbilitaFonteRicerca() {
		String punto = ver + "doAbilitaFonteRicerca ";
		logger.debug(punto + " inizio con dati ");
		try {
			Vector<ISASRecord> fonteRicerca = myEJB.filtriRicercaFonte(CaribelSessionManager.getInstance()
					.getMyLogin(), hParameters);
			int fonte;
			int numeroElementiFonte;
			disabilitaFonte();
			
			for (int i = 0; i < fonteRicerca.size(); i++) {
				ISASRecord dbrFonte = (ISASRecord)fonteRicerca.get(i);
				fonte = ISASUtil.getValoreIntero(dbrFonte, CostantiSinssntW.L_AT_FONTE);
				numeroElementiFonte = ISASUtil.getValoreIntero(dbrFonte, CostantiSinssntW.CTS_AS_NUMERO);
				impostaCheck(fonte, numeroElementiFonte,lTipoFonte);
			}
			
			String labelReferente= "";
			if ( UtilForContainer.isSegregeriaOrganizzativa()){
				labelReferente = Labels.getLabel("lista.assistiti.col.operatore.so.referente_descr");
			}else {
				labelReferente = Labels.getLabel("lista.assistiti.col.operatore.referente.descr");
			}
			
			logger.trace(punto + "labelReferente>"+labelReferente+"<");
			opReferente.setVisible(false);
			if (ISASUtil.valida(labelReferente)){
				opReferente.setLabel(labelReferente);
				opReferente.setVisible(true);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void disabilitaFonte() {
		String punto = ver + "disabilitaFonte ";
		logger.trace(punto + " gestire la disabilitazione delle fonti ");
		elenco_schede.getChildren().clear();
		elementiInSchedeAttive = 0;
	}
	
	
	private void impostaCheck(int fonte, int numeroElementiFonte, LinkedList<String> tipoFonteL) {
		String punto = ver + "impostaCheck ";
		String testo = Labels.getLabel("listaAssistitiGrid.attivita_num."+fonte, new String[]{numeroElementiFonte+""});
//		logger.trace(punto + " testo>>" + testo);
		aggiungiCheck(fonte,testo, numeroElementiFonte, tipoFonteL);
	}
	
	
	private void aggiungiCheck(int fonte, String testo, int numeroElementiFonte, LinkedList<String> tipoFonteL) {
		String punto = ver + "aggiungiCheck ";
		logger.trace(punto + " testo>" + testo);
		if (numeroElementiFonte>0){
			if ((elementiInSchedeAttive % CostantiSinssntW.LISTA_ASSISTITO_NUMERO_COLONNE) == 0){
				riga_schede = new Hlayout();
				riga_schede.setParent(elenco_schede); 
			}
			
			elementiInSchedeAttive ++;
			CaribelCheckbox checkAttivita = new CaribelCheckbox();
			checkAttivita.setLabel(testo);
			checkAttivita.setParent(riga_schede);
			checkAttivita.setValue(fonte+"");
			checkAttivita.setChecked(false);
			if (tipoFonteL !=null && tipoFonteL.contains(fonte+"")){
				checkAttivita.setChecked(true);
			}else {
				if (checkAllAttiv.isChecked()){
					//disabilito la scelta di tutti, in quanto ho trovato un ceck a false.
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

	private void impostaFiltro() throws Exception {
		// da Filtro di ricerca
		this.hParameters.put(CTS_LISTA_ASSISTITI_DADATA, dadata.getValueForIsas());
		this.hParameters.put(CTS_LISTA_ASSISTITI_ADATA, adata.getValueForIsas());

		// Contatti chiusi
		this.hParameters.put(CTS_LISTA_ASSISTITI_CONTATTI_CHIUSI, new Boolean(checkContattiChiusi.isChecked()));

		// Tipo fonte
		String tipoFonte = recuperaDatiFonte();
		this.hParameters.put("tipo_fonte", tipoFonte);
		this.hParameters.put(CostantiSinssntW.CTS_LISTA_ASSISTITI_CARICA_DATI, new Boolean(caricareGrigliaDati));

		String ordinamento = settaFiltroOrdinamento();
		this.hParameters.put(CostantiSinssntW.CTS_LISTA_ATTIVITA_ORDINAMENTO, ordinamento);

		// Filtro su tipo_operatore
		this.hParameters.put("tipo_operatore", UtilForContainer.getTipoOperatorerContainer());

		boolean isSegreteriaOrganizzativa = UtilForContainer.isSegregeriaOrganizzativa();
		// Filtro su segreteria organizzativa
		this.hParameters.put(ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA, new Boolean(isSegreteriaOrganizzativa));

		// Filtro per non aggiungere le fonti per le figure professionali
		if (!isSegreteriaOrganizzativa) {
			this.hParameters.put(CostantiSinssntW.CTS_FONTI_DA_ESCLUDERE,
					ManagerProfile.getValue(getProfile(), ManagerProfile.L_A_FONTI_DA_ESCLUDERE_FIG_PROF));
		}else if (isSegreteriaOrganizzativa) {
			this.hParameters.put(CostantiSinssntW.CTS_FONTI_DA_ESCLUDERE,
					ManagerProfile.getValue(getProfile(), ManagerProfile.L_A_FONTI_DA_ESCLUDERE_SO));
		}
		
		
		// Filtro su obbligo prima visita
		boolean obbligoPV = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.SO_OBB_CDI_PRIMA_VISITA);
		this.hParameters.put(ManagerProfile.SO_OBB_CDI_PRIMA_VISITA, new Boolean(obbligoPV));

		// Filtro su gg da far vedere agli operatori dopo che la segnalazione è
		// stata vista dalla SO
		String ggSegnalazioni = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_VISTA_SEGNALAZIONE_OPERATORE);
		this.hParameters.put(ManagerProfile.GG_VISTA_SEGNALAZIONE_OPERATORE, ggSegnalazioni);

		// Filtro su gg per mostrare le schede delle valutazioni bisogni
		String ggValutazioni = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_VALUTAZIONE_BISOGNI);
		this.hParameters.put(ManagerProfile.GG_VALUTAZIONE_BISOGNI, ggValutazioni);

		String ggPregressoPrestazioni = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_PREGRESSO_PRESTAZIONI);
		this.hParameters.put(ManagerProfile.GG_PREGRESSO_PRESTAZIONI, ggPregressoPrestazioni);

		String codReg = ManagerProfile.getValue(getProfile(), ManagerProfile.CODICE_REGIONE);
		this.hParameters.put(ManagerProfile.CODICE_REGIONE, codReg);

		String codAzSan = ManagerProfile.getValue(getProfile(), ManagerProfile.CODICE_USL);
		this.hParameters.put(ManagerProfile.CODICE_USL, codAzSan);

		// //Destinatari richieste
//		 this.hParameters.put(CostantiSinssntW.CTS_L_RICHIESTE_PERSONALI_SEDE_MIA,new Boolean(checkDestMeStesso.isChecked()));
//		 this.hParameters.put(CostantiSinssntW.CTS_L_RICHIESTE_PERSONALI_SEDE_ALTRI,new Boolean(checkDestAltrui.isChecked()));
		
		this.hParameters.put(CostantiSinssntW.CTS_L_RICHIESTE_DESTINATARI, metodoRicerca.getSelectedValue());

		String ggScadenzaRicovero = ManagerProfile.getValue(getProfile(), ManagerProfile.GG_SCADENZA_RICOVERI_RSA);
		this.hParameters.put(ManagerProfile.GG_SCADENZA_RICOVERI_RSA, ggScadenzaRicovero);
		
	}

	
	public String recuperaDatiFonte() {
		String punto = ver + "recuperaDatiFonte ";
		String tipoFonte = "";
		lTipoFonte = new LinkedList<String>();
		for (Iterator it = elenco_schede.getChildren().iterator(); it.hasNext();) {
		    Hlayout riga =(Hlayout) it.next();
		    if (riga != null && (riga instanceof Hlayout)){
		    	for (int i=0; i< riga.getChildren().size(); i++) {
		    		CaribelCheckbox cbx = (CaribelCheckbox)riga.getChildren().get(i);
		    		if (cbx!=null && (cbx instanceof CaribelCheckbox)) {
						tipoFonte = recuperaValoreAttivita(tipoFonte, cbx);
					}
		    	}
		    }
		    logger.trace(punto + " check>>" +tipoFonte+ "<");
		}
		if(tipoFonte.equals(""))
			tipoFonte = "-1";//Se nessun check selezionato allora nessun record trovato!
		return tipoFonte;
	}
	
	private String recuperaValoreAttivita(String tipoFonte, CaribelCheckbox cbx) {
		String valoreCheck = recuperaValoreChekAttivita(cbx);
		if (ISASUtil.valida(valoreCheck)) {
			tipoFonte += (ISASUtil.valida(tipoFonte) ? ", " : "") + valoreCheck;
			lTipoFonte.add(valoreCheck);
		}
		return tipoFonte;
	}

	private String recuperaValoreChekAttivita(CaribelCheckbox checkAttivita) {
		String valoreCheck = "";
		if(checkAttivita!=null &&checkAttivita.isChecked())
			valoreCheck = checkAttivita.getValue();
		return valoreCheck;
	}
	
	private void showNotificationChangeAreaAt(String nextArea) {
		Clients.showNotification(
				Labels.getLabel("listaAttivitaGrid.switch_container.notification",
						new String[] { nextArea.toUpperCase() }), "info", Path.getComponent("/main"), "middle_center",
				2500);
	}

	private void showAlertPermission() {
		Messagebox.show(Labels.getLabel("exception.ISASPermissionDeniedException.msg"),
				Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.EXCLAMATION);

	}
}


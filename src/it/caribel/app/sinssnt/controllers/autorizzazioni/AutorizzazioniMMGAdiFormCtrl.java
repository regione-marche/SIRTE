package it.caribel.app.sinssnt.controllers.autorizzazioni;

import it.caribel.app.sinssnt.bean.modificati.SkmmgEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOSKSoProrogheBaseEJB;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Window;

public class AutorizzazioniMMGAdiFormCtrl extends AutorizzazioniMMGAdiFormBaseCtrl {

	private static final long serialVersionUID = 1L;

	/* form di riferimento JFrameMMGAutorAdi */
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/autorizzazioneMMG/autorizzazioneMMGAdi.zul";
	public static final String myKeyPermission = "SKMMGADI";

	static public final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
	static public final java.text.SimpleDateFormat dateFormat1 = new java.text.SimpleDateFormat("yyyy-MM-dd");

	private SkmmgEJB myEJB = new SkmmgEJB();
	protected Window sntAutorizzazioneMMGADI;

	boolean rispostaChiusuraContatto = false;
	private CaribelCombobox cbxskadi_freq; // frequenza accessi 
	private CaribelCombobox cbxskadi_specifica; // tipo specifica 
	private AbstractComponent medicoDistrettoSearch;
	private CaribelListbox tableAutorizzazioneAdi;

	private Button btn_pazienteGiaAssistito;
	private Button btn_moduloInserimentoPaziente;
	private Button btn_moduloAutorizzazione;
	
	private CaribelIntbox n_cartella;
//	private CaribelDatebox Keypr_data;
	private CaribelIntbox Key_n_contatto;
	private CaribelDatebox skadi_data;
	private CaribelIntbox Keycartella;
	private CaribelIntbox skadi_freq_mens;
	
	private CaribelDatebox skadi_data_inzio;
	private CaribelDatebox skadi_data_fine;
	
	private CaribelCheckbox ch_skadi_attiv9;
	private CaribelTextbox akadi_altro;
	
	private CaribelCheckbox ch_skadi_spe;
	private CaribelTextbox skadi_spe_des;
	
	private CaribelCheckbox ch_skadi_alt;
	private CaribelTextbox skadi_alt_des;
	
	private Radio aprovataS;
	private CaribelTextbox skadi_motivo;
	
	private CaribelTextbox skadi_freq_altro;


	private CaribelCombobox desc_operADI;
	private CaribelTextbox skadi_operatore;

	private CaribelCombobox desc_MMG;
	private CaribelTextbox skadi_mmgpls;
	
//	private CaribelDatebox pr_data;
	private CaribelIntbox n_contatto;

	private String ver = "21-" + this.getClass().getName() + "\n ";

	public static final String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";
	//	TODO VERIFICARE IL CORRETTO FUNZIONAMENTO: pr_data 
	public static final String CTS_DATA_CART = "dataCart";
	public static final String CTS_ZUL_CHIAMANTE = "zul_chiamante";
	public static final String CTS_DATA_RIF = "dataRif";

	private Tabpanel mmg_adi_principale;
	private Tabpanel mmg_adi_dettaglio;
	
	public static final String CTS_DATA_APERTURA = "skm_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "skm_data_chiusura";
	private ISASRecord dbrSkSo = null;
	@Override
	protected void doInitGridForm() {
		super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
		this.setMethodNameForInsert("insert_Adi");
		this.setMethodNameForUpdate("updateADI");
		this.setMethodNameForDelete("deleteADI");
		super.setMethodNameForQuery("queryAll_Adi");
		super.setMethodNameForQueryKey("queryKey_Adi");
		
		btn_pazienteGiaAssistito.setVisible(false);
		btn_moduloInserimentoPaziente.setVisible(false);
		btn_moduloAutorizzazione.setVisible(false);
		
		try {
			CaribelSearchCtrl medicoSearch = (CaribelSearchCtrl) medicoDistrettoSearch.getAttribute(MY_CTRL_KEY);
			medicoSearch.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, GestTpOp.CTS_COD_MEDICO);
			
			doPopulateCombobox();
			if (arg.get(CostantiSinssntW.N_CARTELLA) == null) {
				CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
				if (containerCorr instanceof ContainerMedicoCtrl) {
					settaDatiIniziali();
				} else {
					throw new Exception("Reperimento chiavi Diagnosi non riuscito!");
				}
				doLoadGrid();
			}
			abilitaMaschera();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void abilitaMaschera() {
		onChangeFrequenza();
//		TODO BOFFA DA TESTARE IL READONLY
		readOnly();
	}


	private void readOnly() {
		String punto = ver + "readOnly ";
		boolean insAutorizzazione = isAutorizzazioneReadOnly();
		logger.trace(punto + " insAutorizzazione>"+insAutorizzazione+"<");
//		mmg_adi_principale.getLinkedTab().setDisabled(insAutorizzazione);
//		mmg_adi_dettaglio.getLinkedTab().setDisabled(insAutorizzazione);
		tableAutorizzazioneAdi.setDisabled(insAutorizzazione);
//		UtilForBinding.setComponentReadOnly(sntAutorizzazioneMMGADI, insAutorizzazione);
//		UtilForComponents.disableListBox(tableAutorizzazioneAdi, insAutorizzazione);
		UtilForBinding.setComponentReadOnly(self,insAutorizzazione);
	}

	public static boolean isAutorizzazioneReadOnly() {
		boolean isReadOnly = false;
		String readOnly = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.AUTORIZZAZIONE_READONLY)+"";
		if (ISASUtil.valida(readOnly)&& readOnly.equals(CostantiSinssntW.CTS_SI)){
			isReadOnly = true;
		}
		return isReadOnly;
	}

	@Override
	protected void afterSetStatoInsert(){
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " inizio ");
		
		if (ISASUtil.valida(this.stato_corr) && this.stato_corr.equals(CaribelGridStateCtrl.STATO_INSERT)){
			skadi_data.setValue(procdate.getDate());
		}
		dbrSkSo = recuperaDatiDaRmSkSo();
		onChangeFrequenza();
		abilitazionePeriodo(skadi_data_inzio, skadi_data_fine);
		skadi_freq_mens.setValue(ISASUtil.getValoreIntero(dbrSkSo, "accessi_mmg"));
	}
	
	private void abilitazionePeriodo(CaribelDatebox dataInizio, CaribelDatebox dataFine) {
		String punto =ver + "abilitazionePeriodo ";
		
		boolean dataInizioValida = ManagerDate.validaData(dataInizio);
//		dataInizio.setReadonly(dataInizioValida);
		logger.trace(punto + " dataInizioValida>>" + dataInizioValida);
		
		boolean dataFineValida = ManagerDate.validaData(dataFine);
//		dataFine.setReadonly(dataFineValida);
		
		dataInizio.setRequired(true);
		dataFine.setRequired(true);
		logger.trace(punto + " dataInizioValida>>" + dataFineValida);
	}

	@Override
	protected void afterSetStatoUpdate() {
		String punto = ver + "afterSetStatoUpdate ";
		logger.trace(punto + " inizio ");
		dbrSkSo = recuperaDatiDaRmSkSo();
		onChangeFrequenza();
		abilitazionePeriodo(skadi_data_inzio, skadi_data_fine);
	}
	
	private ISASRecord recuperaDatiDaRmSkSo() {
		String punto = ver + "recuperaDatiDaRmSkSo ";
		ISASRecord dbrSkso = recuperaSkSo();
		ISASRecord dbrOpCoinvolti = recuperaSkSoOpCoinvolti();
		ISASRecord dbrRmSksoProroghe = recuperaProroga();
		
		if (dbrSkso!=null){
			logger.trace(punto + " popolo i dati\n>>"+ dbrSkso.getHashtable()+"<\n");
			skadi_operatore.setValue(getProfile().getIsasUser().getKUser());
			desc_operADI.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));

			desc_MMG.setValue(ISASUtil.getValoreStringa(dbrSkso, "medico_desc"));
			skadi_mmgpls.setValue(ISASUtil.getValoreStringa(dbrSkso, "cod_med"));
			
			try {
				cbxskadi_freq.setSelectedValue(ISASUtil.getValoreStringa(dbrSkso, "frequenza"));
				if (dbrRmSksoProroghe!=null) {
					logger.trace(punto + " imposto il periodo letto dalla proroga ");
					impostaPeriodo(dbrRmSksoProroghe, "dt_proroga_inizio", "dt_proroga_fine");
				}else {
					logger.trace(punto + " imposto il periodo letto da operatori coinvolti ");
					impostaPeriodo(dbrOpCoinvolti, "dt_inizio_piano", "dt_fine_piano");
				}
			} catch (Exception e) {
				logger.trace(punto + " Errore in impostazione data ");
			}
		}
		return dbrSkso;
	}

	private void impostaPeriodo(ISASRecord dbr, String dtInizio, String dtFine) throws ISASMisuseException {
		if (ManagerDate.validaData(dbr.get(dtInizio) + "")) {
			skadi_data_inzio.setValue((Date) dbr.get(dtInizio));
		}
		if (ManagerDate.validaData(dbr.get(dtFine) + "")) {
			skadi_data_fine.setValue((Date) dbr.get(dtFine));
		}
	}

	public static  ISASRecord recuperaProroga() {
		String punto = "recuperaProroga ";
		RMSkSOSKSoProrogheBaseEJB rmSkSoProrogheBaseEJB = new RMSkSOSKSoProrogheBaseEJB();
		String nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"";
		String idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO)+"";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
		
		ISASRecord dbr = null;
		try {
			dbr = (ISASRecord)rmSkSoProrogheBaseEJB.recuperaUltimaProroga(CaribelSessionManager.getInstance().getMyLogin(), dati);
		} catch (Exception e) {
//			logger.error(punto + "Errore nel recuperare i dati ");
		}
		return dbr;
	}

	public static ISASRecord recuperaSkSo() {
		String punto = "recuperaSkSo ";
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		String nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"";
		String idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO)+"";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);

		ISASRecord dbr = null;
		try {
			dbr = (ISASRecord)rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
		} catch (Exception e) {
//			logger.error(punto + "Errore nel recuperare i dati ");
		}
		return dbr;
	}

	public static ISASRecord recuperaSkSoOpCoinvolti() {
		String punto = "recuperaSkSoOpCoinvolti ";
		RMSkSOOpCoinvoltiEJB rmSkSOSoOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
		String nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+"";
		String idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO)+"";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
		dati.put(CostantiSinssntW.CTS_SO_TIPO_OPERATORE, GestTpOp.CTS_COD_MMG);
		
		ISASRecord dbrOpCoinvolti = null;
		try {
			dbrOpCoinvolti = (ISASRecord)rmSkSOSoOpCoinvoltiEJB.queryKey(CaribelSessionManager.getInstance().getMyLogin(), dati);
		} catch (Exception e) {
//			logger.error(punto + "Errore nel recuperare i dati ");
		}
		return dbrOpCoinvolti;
	}

	
	
	public void onChangeFrequenza() {
		String punto = ver + "onChange=onChangeFrequenza ";
		String valoreCombo = cbxskadi_freq.getSelectedValue();
		logger.trace(punto + " valore combo>>"+ valoreCombo+"<");
		skadi_freq_altro.setReadonly(true);
		skadi_freq_mens.setReadonly(true);
		
		if (ISASUtil.valida(valoreCombo)) {
			skadi_freq_mens.setReadonly(false);
			int frequenza = Integer.parseInt(valoreCombo);
			int frequenzaMensile = -1;
			skadi_freq_altro.setText("");
			if (frequenza == 8) {
				skadi_freq_mens.setReadonly(true);
				skadi_freq_altro.setReadonly(false);
			}
			frequenzaMensile = recuperaFequenzaAdi(frequenza);
			logger.trace(punto + " valore da impostare>>" + frequenzaMensile + "<<");
			skadi_freq_mens.setValue(frequenzaMensile);
		}
	}

//	public static int recuperaFequenzaAdi(int frequenza) {
//		int frequenzaMensile=0;
//		switch (frequenza) {
//			case 0:
//				frequenzaMensile = 0;
//		        break;
//		    case 1:
//		    	frequenzaMensile = 1;
//		        break;
//		    case 2:
//		    	frequenzaMensile = 2;
//		        break;
//		    case 3:
//		    	frequenzaMensile = 4;
//		        break;
//		    case 4:
//		        frequenzaMensile = 10;
//		        break;
//		    case 5:
//		        frequenzaMensile = 15;
//		        break;
//		    case 6:
//		        frequenzaMensile = 30;
//		        break;
//		    case 7:
//		        frequenzaMensile = 60;
//		        break;
//		    case 8:
//		    	frequenzaMensile = 0;
//		    	break;
//		}
//		return frequenzaMensile;
//	}


	public void onClick$btn_pazienteGiaAssistito(Event event) throws Exception	{
		String punto = ver + "onClick$btn_pazienteGiaAssistito ";
		logger.trace(punto + " dati ");
		try{
			 if (!saveForm()){
				 logger.trace(punto + "NON HO SALVATO IL FORM: non effettuo la stampa ");
				 return ;
			 }
		 	 String user = CaribelSessionManager.getInstance().getMyLogin().getUser();
	         String passwd = CaribelSessionManager.getInstance().getMyLogin().getPassword();
	         String cart=n_cartella.getValue()+"";
	         String dataSK = skadi_data.getValueForIsas(); //pr_data.getValueForIsas();
//	         String data = pr_data.getValueForIsas();
			 // TODO CORREGGERE NELLA STAMPA PASSANDOGLI N_CONTATTO
			 String data = "";
	          
	          if (!ManagerDate.validaData(dataSK)){
	        	  return ;
	          }
	          
		      String servlet=profile.getParameter("fop")+"?EJB=SINS_FOLETTERE&USER="+user+"&WORD="+passwd+"&METHOD=query_lettera"+
		              "&cart="+cart+"&dataSK="+dataSK+"&data="+data+"&tipo=ADI"+"&REPORT=assistenzaAuto.fo";
			
		      logger.trace(punto + " servelet>>" +servlet+"<<\n");
	          it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);

		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	private boolean saveForm() {
		boolean salvataggioForm = false;
//		TODO BOFFA: EFFETTUARE IL SALVATAGGIO DELLA MASCHERA 
//	      rit=((Boolean)saveDati()).booleanValue();
	   logger.trace(" HO EFFETTUATO IL SALVATAGGIO DEL FORM ");
		return salvataggioForm;
	}

	public void onClick$btn_moduloInserimentoPaziente(Event event) throws Exception {
		String punto = ver + "onClick$btn_moduloInserimentoPaziente ";
		logger.trace(punto + " dati ");
		try {

			if (!saveForm()) {
				logger.trace(punto + "NON HO SALVATO IL FORM: non effettuo la stampa ");
				return;
			}

			String user = CaribelSessionManager.getInstance().getMyLogin().getUser();
			String passwd = CaribelSessionManager.getInstance().getMyLogin().getPassword();
			String cart = n_cartella.getValue() + "";
			String dataSK = skadi_data.getValueForIsas(); //pr_data.getValueForIsas();
//			String data = pr_data.getValueForIsas();
			// TODO CORREGGERE NELLA STAMPA PASSANDOGLI N_CONTATTO
			String data = "";

			if (!ManagerDate.validaData(dataSK)) {
				return;
			}
			String servlet = profile.getParameter("fop") + "?EJB=SINS_FOLETTERE&USER=" + user + "&WORD=" + passwd + "&METHOD=query_lettera"
					+ "&cart=" + cart + "&dataSK=" + dataSK + "&data=" + data + "&tipo=ADI" + "&REPORT=inserimentoAuto.fo";
			logger.trace(punto + " servelet>>" + servlet + "<<\n");
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);

		} catch (Exception ex) {
			doShowException(ex);
		}
	}
	
	public void onClick$btn_moduloAutorizzazione(Event event) throws Exception {
		String punto = ver + "onClick$btn_moduloInserimentoPaziente ";
		logger.trace(punto + " dati ");
		try {
			if (!saveForm()) {
				logger.trace(punto + "NON HO SALVATO IL FORM: non effettuo la stampa ");
				return;
			}
//			String data = pr_data.getValueForIsas();
			// TODO CORREGGERE NELLA STAMPA PASSANDOGLI N_CONTATTO
			String data = "";
			String cart = n_cartella.getValue() + "";
			String dataSK = skadi_data.getValueForIsas(); //pr_data.getValueForIsas();
			if (!ManagerDate.validaData(dataSK)) {
				return;
			}
			String user = CaribelSessionManager.getInstance().getMyLogin().getUser();
			String passwd = CaribelSessionManager.getInstance().getMyLogin().getPassword();

			String servlet = "/SINSSNTFoServlet/SINSSNTFoServlet" + "?EJB=SINS_FOMODULI&USER=" + user + "&WORD=" + passwd
					+ "&METHOD=query_stampaADI" + "&cart=" + cart + "&dataSK=" + dataSK + "&data=" + data + "&REPORT=moduloADI.fo";

			logger.trace(punto + " servelet>>" + servlet + "<<\n");
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);

		} catch (Exception ex) {
			doShowException(ex);
		}
	}
	
	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + " carico la combo box ");

		loadFrequenzaAdi(cbxskadi_freq);
		
		loadTipoSpecifica(cbxskadi_specifica);
	}

	private void loadTipoSpecifica(CaribelCombobox cbx) {
		String punto = ver + "loadTipoSpecifica ";
		logger.debug(punto +" carico combo specifica ");
		CaribelComboRepository.addComboItem(cbx, "0", Labels.getLabel("autorizzazionemmg.adi.principale.specifica.0"));
		CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("autorizzazionemmg.adi.principale.specifica.1"));
		CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("autorizzazionemmg.adi.principale.specifica.2"));
		CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("autorizzazionemmg.adi.principale.specifica.3"));
		
	}
	
	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		settaDatiIniziali();
		boolean periodoConforme =true; 
		
		if(periodoConforme){
			String dataAperturaSkso = ISASUtil.getValoreStringa(dbrSkSo, CostantiSinssntW.CTS_PR_DATA_PUAC);
			if (ManagerDate.validaData(dataAperturaSkso) && 
						!ManagerDate.confrontaDate(dataAperturaSkso, skadi_data.getValueForIsas())){
				String lbprDataPuac = ManagerDecod.recuperaDescrizioneLabel(self, "lb_skadi_data"); 
				String[] lables = new String[] {lbprDataPuac};
				String messaggio = Labels.getLabel("autorizzazione.adi.msg.precedente.so", lables);
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.QUESTION);
				periodoConforme = false;
			}
		}
		if (periodoConforme){
			periodoConforme = ManagerDate.controllaPeriodo(self, skadi_data_inzio, skadi_data_fine,
					"lbx_skadi_data_inzio","lbx_skadi_data_fine");
		}

		if (periodoConforme) {
			try {
				String idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO) + "";
				String messaggioPeriodoInSoOProroga = controllaPeriodoInSoOProroga(n_cartella.getValue() + "", idSkso,
						skadi_data_inzio.getValueForIsas(), skadi_data_fine.getValueForIsas());
				if (ISASUtil.valida(messaggioPeriodoInSoOProroga)) {
					Messagebox.show(messaggioPeriodoInSoOProroga, Labels.getLabel("messagebox.attention"),
							Messagebox.OK, Messagebox.QUESTION);
					periodoConforme = false;
				}
			} catch (Exception e) {
				periodoConforme = false;
				logger.error(punto + " Errore nel controllo del periodo ");
			}
		}
		
		logger.trace(punto + "periodoConforme>"+periodoConforme+"<");
		
		return periodoConforme;
	}
	
	public static String controllaPeriodoInSoOProroga(String nCartella, String idSkso, String dtInizio, String dtFine) throws DBRecordChangedException, ISASPermissionDeniedException,
			SQLException, CariException {
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
		dati.put(SkmmgEJB.SKADI_DATA_INIZIO, dtInizio);
		dati.put(SkmmgEJB.SKADI_DATA_FINE, dtFine);

		SkmmgEJB skmmg = new SkmmgEJB();
		String messaggio = skmmg.controllaPeriodoInSoOProroga(CaribelSessionManager.getInstance().getMyLogin(), dati);

		return messaggio;
	}

	private void settaDatiIniziali() {
		String punto = ver + "settaDatiIniziali ";
		n_cartella.setValue(Integer.parseInt(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+""));
//		settaData(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.MMGPRPR_DATA)+"");
		int nContatto = ISASUtil.getValoreIntero(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CONTATTO+"")+"");
		logger.trace(punto + " ncontatto>>"+ nContatto);
		n_contatto.setValue(nContatto);
		Key_n_contatto.setValue(nContatto);
		Keycartella.setValue(Integer.parseInt(UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA)+""));
		
	}

	@Override
	protected void doLoadGrid() throws Exception {
		String punto = ver + "doLoadGrid ";
		logger.trace(punto + " inizio con dati ");
		if (ISASUtil.valida(n_cartella.getValue()+"") && n_contatto.getValue()>=0) {
			hParameters.putAll(getOtherParametersString());
			this.hParameters.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
			this.hParameters.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue()+"");
			Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
			clb.getItems().clear();
			clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
		}else {
			logger.debug(" \n Non effettuo il caricamento della griglia ");
		}
	}
	
	public void onCheck$ch_skadi_attiv9() throws Exception {
		String punto = ver + "onCheck$ch_skadi_attiv9 ";
		logger.trace(punto + " dati letti ");
		akadi_altro.setReadonly(true);
		akadi_altro.setText("");
		if (ch_skadi_attiv9.isChecked()){
			logger.trace(punto + " ");
			akadi_altro.setReadonly(false);
		}
	}

	public void onCheck$ch_skadi_spe() throws Exception {
		String punto = ver + "onCheck$ch_skadi_spe ";
		logger.trace(punto + " dati letti ");
		skadi_spe_des.setReadonly(true);
		skadi_spe_des.setText("");
		if (ch_skadi_spe.isChecked()){
			logger.trace(punto + " ");
			skadi_spe_des.setReadonly(false);
		}
	}

	public void onCheck$ch_skadi_alt() throws Exception {
		String punto = ver + "onCheck$ch_skadi_alt ";
		logger.trace(punto + " dati letti ");
		skadi_alt_des.setReadonly(true);
		skadi_alt_des.setText("");
		if (ch_skadi_alt.isChecked()){
			logger.trace(punto + " ");
			skadi_alt_des.setReadonly(false);
		}
	}
	
	public void onCheck$skadi_approva() throws Exception {
		String punto = ver + "onCheck$skadi_approva ";
		logger.trace(punto + " dati letti ");
		skadi_motivo.setReadonly(true);
		skadi_motivo.setText("");

		if (aprovataS.isSelected()){
			logger.trace(punto + " ");
			skadi_motivo.setReadonly(false);
		}
	}

}
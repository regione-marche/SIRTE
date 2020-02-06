package it.caribel.app.sinssnt.controllers.autorizzazioni;

import it.caribel.app.sinssnt.bean.modificati.SkmmgEJB;
import it.caribel.app.sinssnt.controllers.ContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.generic_controllers.CaribelGridStateCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;
import java.sql.Date;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Window;

public class AutorizzazioniMMGAdrFormCtrl extends CaribelGridCRUDCtrl {

	private static final long serialVersionUID = 1L;

	/* form di riferimento JFrameMMGAutorAdr */   
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/autorizzazioneMMG/autorizzazioneMMGAdr.zul";
	
	public static final String myKeyPermission = "SKMMGADR";
	static public final java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
	static public final java.text.SimpleDateFormat dateFormat1 = new java.text.SimpleDateFormat("yyyy-MM-dd");

	private SkmmgEJB myEJB = new SkmmgEJB();
	protected Window sntAutorizzazioneMMGADR;
	boolean rispostaChiusuraContatto = false;
	private CaribelCombobox cbxskadr_freq; // frequenza accessi 
	private CaribelCombobox cbxskadr_specifica;
	private CaribelIntbox n_cartella;
//	private CaribelDatebox Keypr_data;
	private CaribelIntbox n_contatto;
	private CaribelIntbox Key_n_contatto;
	private ISASRecord dbrSkSo = null;
	
	private CaribelDatebox skadr_data;
	private CaribelDatebox skadr_data_inizio;
	private CaribelDatebox skadr_data_fine;
	private CaribelIntbox Keycartella;
	private CaribelIntbox skadr_freq_mens;

	private CaribelTextbox skadr_operatore;
	private CaribelCombobox desc_operADR;
	private CaribelCombobox desc_MMGADR;
	private CaribelTextbox skadr_mmgpls;
	
	private Button btn_pazienteGiaAssistito;
	private Button btn_moduloInserimentoPaziente;
	private Button btn_moduloAutorizzazione;
	
	private AbstractComponent medicoDistrettoSearch;
	
	private Radio aprovataS;
	private CaribelTextbox skadr_motivo;
	private CaribelTextbox skadr_freq_altro;
//	private CaribelDatebox pr_data;

	private String ver = "12-" + this.getClass().getName() + "\n ";

	public static final String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";
	//	TODO VERIFICARE IL CORRETTO FUNZIONAMENTO: pr_data 
	public static final String CTS_DATA_CART = "dataCart";
	public static final String CTS_ZUL_CHIAMANTE = "zul_chiamante";
	public static final String CTS_DATA_RIF = "dataRif";

	public static final String CTS_DATA_APERTURA = "skm_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "skm_data_chiusura";

	@Override
	protected void doInitGridForm() {
		super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
		this.setMethodNameForInsert("insert_Adr");
		this.setMethodNameForUpdate("updateADR");
		this.setMethodNameForDelete("deleteADR");
		super.setMethodNameForQuery("queryAll_Adr");
		super.setMethodNameForQueryKey("queryKey_Adr");
		
		try {
			
			btn_pazienteGiaAssistito.setVisible(false);
			btn_moduloInserimentoPaziente.setVisible(false);
			btn_moduloAutorizzazione.setVisible(false);
			
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

	
	@Override
	protected void afterSetStatoInsert(){
		String punto = ver + "afterSetStatoInsert ";
		logger.trace(punto + " sono in insert ");
		
		if (ISASUtil.valida(this.stato_corr) && this.stato_corr.equals(CaribelGridStateCtrl.STATO_INSERT)){
			logger.trace(punto + "Imposto la data ");
			skadr_data.setValue(procdate.getDate());
		}
		dbrSkSo = recuperaDatiDaRmSkSo();
		onChangeFrequenza();
		abilitazionePeriodo(skadr_data_inizio, skadr_data_fine);
		skadr_freq_mens.setValue(ISASUtil.getValoreIntero(dbrSkSo, "accessi_mmg"));
	}
	
	
	@Override
	protected void afterSetStatoUpdate() {
		String punto = ver + "afterSetStatoUpdate ";
		logger.trace(punto + " inizio ");
		dbrSkSo = recuperaDatiDaRmSkSo();
		onChangeFrequenza();	
		abilitazionePeriodo(skadr_data_inizio, skadr_data_fine);
	}
	
	
	private void abilitazionePeriodo(CaribelDatebox dataInizio, CaribelDatebox dataFine) {
		String punto = ver + "abilitazionePeriodo ";
		
		boolean dataInizioValida = ManagerDate.validaData(dataInizio);
		dataInizio.setReadonly(dataInizioValida);
		logger.trace(punto + " dataInizioValida>>" + dataInizioValida);
		
		boolean dataFineValida = ManagerDate.validaData(dataFine);
		dataFine.setReadonly(dataFineValida);
		
		dataInizio.setRequired(true);
		dataFine.setRequired(true);
		logger.trace(punto + " dataInizioValida>>" + dataFineValida);
	}
	
	
	private ISASRecord recuperaDatiDaRmSkSo() {
		String punto = ver + "recuperaDatiDaRmSkSo ";
		ISASRecord dbrRmSkso = AutorizzazioniMMGAdiFormCtrl.recuperaSkSo();
		ISASRecord dbrOpCoinvolti = AutorizzazioniMMGAdiFormCtrl.recuperaSkSoOpCoinvolti();
		ISASRecord dbrRmSksoProroghe = AutorizzazioniMMGAdiFormCtrl.recuperaProroga();
		
		if (dbrRmSkso!=null){
			logger.trace(punto + " popolo i dati\n>>"+ dbrRmSkso.getHashtable()+"<\n");
			skadr_operatore.setValue(getProfile().getIsasUser().getKUser());
			desc_operADR.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));

			desc_MMGADR.setValue(ISASUtil.getValoreStringa(dbrRmSkso, "medico_desc"));
			skadr_mmgpls.setValue(ISASUtil.getValoreStringa(dbrRmSkso, "cod_med"));
			
			try {
				cbxskadr_freq.setSelectedValue(ISASUtil.getValoreStringa(dbrRmSkso, "frequenza"));

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
		return dbrRmSkso;
	}
	
	private void impostaPeriodo(ISASRecord dbr, String dtInizio, String dtFine) throws WrongValueException,
			ISASMisuseException {
		if (ManagerDate.validaData(dbr.get(dtInizio) + "")) {
			skadr_data_inizio.setValue((Date) dbr.get(dtInizio));
		}
		if (ManagerDate.validaData(dbr.get(dtFine) + "")) {
			skadr_data_fine.setValue((Date) dbr.get(dtFine));
		}
	}


	private void abilitaMaschera() {
		onChangeFrequenza();
		readOnly();
	}


	private void readOnly() {
		String punto = ver + "readOnly ";
		String readOnly = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.AUTORIZZAZIONE_READONLY)+"";
		boolean insAutorizzazione = false;
		if (ISASUtil.valida(readOnly)&& readOnly.equals(CostantiSinssntW.CTS_SI)){
			insAutorizzazione = true;
		}
		logger.trace(punto + " readOnly>>" + readOnly+"<");
//		mmg_adi_principale.getLinkedTab().setDisabled(insAutorizzazione);
//		mmg_adi_dettaglio.getLinkedTab().setDisabled(insAutorizzazione);
		UtilForBinding.setComponentReadOnly(sntAutorizzazioneMMGADR, insAutorizzazione);
	}


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
	         String dataSK = skadr_data.getValueForIsas(); //pr_data.getValueForIsas();
//	         String data = pr_data.getValueForIsas();
	         String data ="";
	          
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
		return false;
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
			String dataSK = skadr_data.getValueForIsas(); //pr_data.getValueForIsas();
//	         String data = pr_data.getValueForIsas();
	         String data ="";

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
//	         String data = pr_data.getValueForIsas();
	         String data ="";
			String cart = n_cartella.getValue() + "";
			String dataSK = skadr_data.getValueForIsas(); //pr_data.getValueForIsas();
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

		loadFrequenzaAdr(cbxskadr_freq);

		loadTipoSpecifica(cbxskadr_specifica);
	}

	private void loadTipoSpecifica(CaribelCombobox cbxskadr_specifica) {
		String punto = ver + "loadTipoSpecifica ";
		logger.debug(punto +" carico combo specifica ");
		CaribelComboRepository.addComboItem(cbxskadr_specifica, "0", Labels.getLabel("autorizzazionemmg.adr.principale.specifica.0"));
		CaribelComboRepository.addComboItem(cbxskadr_specifica, "1", Labels.getLabel("autorizzazionemmg.adr.principale.specifica.1"));
		CaribelComboRepository.addComboItem(cbxskadr_specifica, "2", Labels.getLabel("autorizzazionemmg.adr.principale.specifica.2"));
		CaribelComboRepository.addComboItem(cbxskadr_specifica, "3", Labels.getLabel("autorizzazionemmg.adr.principale.specifica.3"));
	}

	public static void loadFrequenzaAdr(CaribelCombobox cbx) throws Exception {
//		logger.debug(ver + " dentro loadFrequenzaAdr ");
//		AutorizzazioniMMGAdpFormCtrl.loadFrequenzaAdp(cbx);
		CaribelComboRepository.addComboItem(cbx, "", "");
		CaribelComboRepository.addComboItem(cbx, "0", Labels.getLabel("autorizzazionemmg.adr.principale.frequenza.0"));
		CaribelComboRepository.addComboItem(cbx, "1", Labels.getLabel("autorizzazionemmg.adr.principale.frequenza.1"));
		CaribelComboRepository.addComboItem(cbx, "2", Labels.getLabel("autorizzazionemmg.adr.principale.frequenza.2"));
		CaribelComboRepository.addComboItem(cbx, "3", Labels.getLabel("autorizzazionemmg.adr.principale.frequenza.3"));
		CaribelComboRepository.addComboItem(cbx, "4", Labels.getLabel("autorizzazionemmg.adr.principale.frequenza.4"));
	}
	
	@Override
	protected boolean doValidateForm() {
		String punto = ver + "doValidateForm ";
		settaDatiIniziali();
//		boolean periodoConforme = ManagerDate.controllaPeriodo(self, Keypr_data, skadr_data, "lb_skadi_data_pr_data","lb_skadi_data");
		
		boolean periodoConforme =true; 
		
		if(periodoConforme){
			String dataAperturaSkso = ISASUtil.getValoreStringa(dbrSkSo, CostantiSinssntW.CTS_PR_DATA_PUAC);
			if (ManagerDate.validaData(dataAperturaSkso) && 
						!ManagerDate.confrontaDate(dataAperturaSkso, skadr_data.getValueForIsas())){
				String lbprDataPuac = ManagerDecod.recuperaDescrizioneLabel(self, "lb_skadr_data"); 
				String[] lables = new String[] {lbprDataPuac};
				String messaggio = Labels.getLabel("autorizzazione.adi.msg.precedente.so", lables);
				Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.QUESTION);
				periodoConforme = false;
			}
		}
		if (periodoConforme){
			periodoConforme = ManagerDate.controllaPeriodo(self, skadr_data_inizio, skadr_data_fine, "lb_skadr_data_inizio","lb_skadr_data_fine");
		}
		
		if (periodoConforme) {
			try {
				String idSkso = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO) + "";
				String messaggioPeriodoInSoOProroga =
						AutorizzazioniMMGAdiFormCtrl.controllaPeriodoInSoOProroga(n_cartella.getValue() + "", idSkso,
						skadr_data_inizio.getValueForIsas(), skadr_data_fine.getValueForIsas());
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
/*
	private void settaData(String prData) {
		if (ISASUtil.valida(prData)) {
			try {
				pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
				Keypr_data.setValue(UtilForBinding.getDateFromIsas(prData));
			} catch (WrongValueException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}	
	}
	*/
	
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
	
	public void onCheck$skadi_approva() throws Exception {
		String punto = ver + "onCheck$skadi_approva ";
		logger.trace(punto + " dati letti ");
		skadr_motivo.setReadonly(true);
		skadr_motivo.setText("");

		if (aprovataS.isSelected()){
			logger.trace(punto + " ");
			skadr_motivo.setReadonly(false);
		}
	}
	
	public void onChangeFrequenza() {
		String punto = ver + "onChange=onChangeFrequenza ";
		String valoreCombo = cbxskadr_freq.getSelectedValue();
		logger.trace(punto + " valore combo>>"+ valoreCombo+"<");
		skadr_freq_altro.setReadonly(true);
		skadr_freq_mens.setReadonly(true);
		
		if (ISASUtil.valida(valoreCombo)) {
			skadr_freq_mens.setReadonly(false);
			int frequenza = Integer.parseInt(valoreCombo);
			int frequenzaMensile = -1;
			skadr_freq_altro.setText("");
			if (frequenza == 4) {
				skadr_freq_mens.setReadonly(true);
				skadr_freq_altro.setReadonly(false);
			}
			frequenzaMensile = recuperaFrequenzaAdr(frequenza);
			logger.trace(punto + " valore da impostare>>" + frequenzaMensile + "<<");
			skadr_freq_mens.setValue(frequenzaMensile);
		}
	}


	public static int recuperaFrequenzaAdr(int frequenza) {
		int frequenzaMensile = -1;
		switch (frequenza) {
		case 0:
			frequenzaMensile = 0;
			break;
		case 1:
			frequenzaMensile = 1;
			break;
		case 2:
			frequenzaMensile = 2;
			break;
		case 3:
			frequenzaMensile = 4;
			break;
		case 4:
			frequenzaMensile = 0 ;
			break;
		}
		return frequenzaMensile;
	}
}
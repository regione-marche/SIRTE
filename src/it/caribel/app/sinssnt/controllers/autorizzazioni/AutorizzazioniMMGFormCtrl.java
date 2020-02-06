package it.caribel.app.sinssnt.controllers.autorizzazioni;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.modificati.SkmmgEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.Hashtable;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class AutorizzazioniMMGFormCtrl extends CaribelFormCtrl {

	private static final long serialVersionUID = 1L;

	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/autorizzazioneMMG/schedaMMGSegnalazione.zul";
	/*
	 *  ho reso pubblica in quanto è la stessa
	 *  chiave che viene usata nel contatto delle terapie 
	 */
	public static final String myKeyPermission = "SKMMGCOR";
/* convertita a partire da JFrameMMGSkmmg.java */
	private static final int RISPOSTACHIUSURACONTATTO = 1;
	private SkmmgEJB myEJB = new SkmmgEJB();
	protected Window sntAutorizzazioneMMG;

	boolean rispostaChiusuraContatto = false;	

	private String ver = "5-" + this.getClass().getName() + "\n ";
	
	private int check_save_step = 0;
	private CaribelDatebox pr_data;
	private CaribelTextbox n_cartella;
	private CaribelTextbox skmmg_operatore;
	private CaribelTextbox desc_oper;

	private CaribelCombobox cbx_skmmg_segnalatore; //Segnalatore 
	
	/*private CaribelCombobox cbxMotivoChius; // motivo chiusura
	private CaribelCombobox cbxMotivo; // motivo
	private CaribelCombobox cbxTipute; // tipoUtente  

	
	private Label label_defInizioPer;
	private Label label_defFinePer;
	
	private Component protesica = null;
	private Component storico_operatore_ref = null;
	private Component accessi;
	private Component diagnosi;
	private Component idMenuTerapia;
	private Component contattoMedicoTerapia;
	
	
	
	private CaribelCombobox skm_medico_descr;
	private CaribelTextbox skm_medico;
	

	private Tabpanel tabpanel_scale;
	private ScalePanelCtr scalPnlCtr;// = new ScalePanelCtr(); 
	private Component panel_scale;

	//	private Button btn_limitaA; 
	private Button btn_accessi;
	private Button btn_protesica;
	private Button btn_riapri;
	private Button btn_storico;
	private Button btn_patologie;
	private CaribelDatebox skm_data_apertura;
	private CaribelDatebox skm_data_chiusura;
	private CaribelDatebox skm_medico_da;
	

//	private CaribelTextbox skm_medico;
//	private CaribelCombobox skm_medico_descr;
	private CaribelTextbox skm_cod_presidio;
	private CaribelCombobox presidio_descr;

	private Vector<Hashtable<String, String>> vettOper = new Vector<Hashtable<String, String>>();

	protected CaribelListbox tablePrestazioni;

	public static final String CTS_TIPO_OPERATORE = "tipo_oper";
	public static final String CTS_TROVA_INVERV = "trova_interv";
	public static final String CTS_TROVA_INVERV_MAX = "trova_interv_max";
	//	TODO VERIFICARE IL CORRETTO FUNZIONAMENTO: pr_data 
	public static final String CTS_DATA_CART = "dataCart";
	public static final String CTS_ZUL_CHIAMANTE = "zul_chiamante";
	public static final String CTS_DATA_RIF = "dataRif";

	public static final String CTS_DATA_APERTURA = "skm_data_apertura";
	public static final String CTS_DATA_CHIUSURA = "skm_data_chiusura";
	*/
	
	public void doInitForm() {
		String punto = this.getClass().getName() + ".doInitForm \n";
		logger.debug(punto + "inizio ");
		try {
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQueryKey("queryKey_solommg");
			super.setMethodNameForUpdate("update_solommg");
			super.setMethodNameForInsert("insert_solommg");
			doMakeControl();
			doPopulateCombobox();

			logger.trace(punto + " dati disponibili>>" + (arg != null ? arg.toString() : "no dati ") + "<<");
			String nCartella = (String) arg.get(CostantiSinssntW.N_CARTELLA);
			String prData = (String)arg.get(CostantiSinssntW.MMGPRPR_DATA);
			
			if (ISASUtil.valida(prData)) {
				pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
			}
			
			if (ISASUtil.valida(nCartella) && ISASUtil.valida(prData)) {
				hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella);
				hParameters.put(CostantiSinssntW.PR_DATA, prData);
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			} else if (dbrFromList != null) {
				//sono stato invocato da una griglia con il contatto (es:storico contatti)
				hParameters.put(CostantiSinssntW.N_CARTELLA, ((Integer) dbrFromList.get(CostantiSinssntW.N_CARTELLA)).toString());
				hParameters.put(CostantiSinssntW.PR_DATA, (dbrFromList.get(CostantiSinssntW.PR_DATA)).toString());
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			} else if (super.caribelContainerCtrl != null && super.caribelContainerCtrl.hashChiaveValore != null
					&& super.caribelContainerCtrl.hashChiaveValore.get("n_cartella") != null) {
				//arrivo da nuovo contatto o da qualunque altro posto
				String cart = (String) hParameters.get("n_cartella");
				if (cart == null) {
					//se non ho il codice cartella lo prendo dal container.
					nCartella = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.N_CARTELLA);
					prData = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.MMGPRPR_DATA);
					this.hParameters.put(CostantiSinssntW.N_CARTELLA, nCartella.toString());
					this.hParameters.put(CostantiSinssntW.PR_DATA, prData);
//					boolean newSchedaMMG = ISASUtil.getvaloreBoolean(caribe, key))
				}
				
				if (ISASUtil.valida(nCartella) && ISASUtil.valida(prData)) {
					doQueryKeySuEJB();
					doWriteBeanToComponents();
				}else if (ISASUtil.valida(nCartella) && !ISASUtil.valida(prData)){
					//sono in inserimento					
					this.n_cartella.setText(nCartella+"");
					skmmg_operatore.setValue(getProfile().getStringFromProfile("codice_operatore"));
					desc_oper.setValue(getProfile().getStringFromProfile("cognome_operatore"));
				}    
			}
			abilitazioneMaschera();
			doFreezeForm();
		} catch (Exception e) {
			e.printStackTrace();
			doShowException(e);
		}
	}
	
	
	private void abilitazioneMaschera() throws WrongValueException, Exception {
		boolean insert = false;
		String punto = ver + "abilitazioneMaschera ";
		logger.trace(punto);
		
		if (super.currentIsasRecord != null) {
			insert = false;
		} else {
			insert = true;
		}
		
		logger.trace(ver + " sono in inserimento>>" + insert + "<<");
			
			/*
			String nCartella = (String) arg.get("n_cartella");
			n_cartella.setText(nCartella);
			n_contatto.setText("0");
			insert = true;
			skm_medico.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skm_medico_descr.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
			String codPresidio = getProfile().getStringFromProfile(ManagerProfile.PRES_OPERATORE);
			skm_cod_presidio.setValue(codPresidio);
			presidio_descr.setValue(ManagerDecod.decodPresidi(getProfile(), codPresidio));
			String skm_data_chiusura = ISASUtil.getValoreStringa(this.currentIsasRecord, "skm_data_chiusura");
//			if (ManagerDate.validaData(skm_data_chiusura)){
//				statoConsultazione  = true;
//			}
			logger.trace(ver + " aggiorno>>" + "<< " + "\ndesc>>" + "<");
			//			cbxMotivoChius.setSelectedValue("0");
		}
		
		logger.trace(ver + " sono in inserimento:" + insert+" statoConsultazione ");
		btn_accessi.setDisabled(insert);
		btn_protesica.setDisabled(insert);
		btn_riapri.setDisabled(true); // sia in inser/update deve essere sempre disabilitato
		btn_storico.setDisabled(insert);
		btn_patologie.setDisabled(insert);
		skm_data_chiusura.setDisabled(insert);
		cbxMotivoChius.setDisabled(insert);
		label_defInizioPer.setVisible(!insert);
		label_defFinePer.setVisible(!insert);
		
		if (isContattoInfChiuso()){
			logger.trace(ver + " sono in  statoConsultazione ");
//			arg.put("forceReadOnly", true);
			this.setReadOnly(true);
			btn_accessi.setDisabled(false);
			btn_protesica.setDisabled(false);
			btn_storico.setDisabled(true);
			
			boolean abilRiap = (getProfile().getIsasUser().canIUse(ManagerProfile.REOPEN_SCHEDA_MED,"MODI"));
			this.btn_riapri.setVisible(abilRiap);
			this.btn_riapri.setDisabled(false);	
			
		}else {
			logger.trace(ver + "NON sono in Consultazione ");
		}
		*/
	}
	
	private void doPopulateCombobox() throws Exception {
		String punto = ver + "doPopulateCombobox \n";
		logger.debug(punto + "");
		Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
		Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

		Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
		if (ManagerProfile.isConfigurazioneAbruzzo(getProfile()) || ManagerProfile.isConfigurazioneMolise(getProfile())
				|| ManagerProfile.isConfigurazioneMarche(getProfile())) {
			//			combo=new String[]{"MCHIUS","SAOADI","FMRICH", "MOTIVO"};
			h_xCBdaTabBase.put("SEGNAL", cbx_skmmg_segnalatore);
		}
		CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
				"tab_descrizione", false);
	}
	/*
	public void onBlurDataApertura(Event event) throws Exception {
		if (skm_medico_da.getValue()==null) skm_medico_da.setValue(skm_data_apertura.getValue());
		
	}
*/
	private void doMakeControl() {
		if (ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABILITA_SCALA)) {
			//		TODO METTERE LA PRESA CARICO E PANNELLO SEGNALAZIONE SOLO PER LA TOSCANA.
		}
		
	}
	private String recuperaNContatto(String nCartella, String prData) throws ISASPermissionDeniedException, SQLException, CariException {
		String nContatto = "";
		String punto = ver + "recuperaNContatto ";
		logger.debug(punto + " recuperare nContatto>>" + nCartella);
		
		/*
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.PR_DATA, prData);
		ISASRecord dbrSkMed = myEJB.getContattoMedCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
		nContatto = ISASUtil.getValoreStringa(dbrSkMed, CostantiSinssntW.N_CONTATTO);
		*/
		logger.debug(punto + " contatto recuperato>>" + nContatto);

		return nContatto;
	}

	@Override
	public boolean doValidateForm() throws Exception {
		String punto = ver + "doSaveForm ";
		boolean canSave = true;
		logger.debug(punto + " salvataggio >>>" );
		skmmg_operatore.setValue(getProfile().getStringFromProfile("codice_operatore"));
		desc_oper.setValue(getProfile().getStringFromProfile("cognome_operatore"));


		
//		 if(!checkOrderDate(JCariDateTextFieldDataInizio, JCariDateTextFieldDataFine)){
//        new it.pisa.caribel.swing2.cariInfoDialog(null,"Le date inserite non sono corrette!","Attenzione!").show();
//        JCariDateTextFieldDataInizio.setUnmaskedText("");
//        JCariDateTextFieldDataFine.setUnmaskedText("");
//        return new Boolean(false);
//      }


		
		
		/*
		 * logger.debug(punto + " canSave>>" + canSave);
		if (canSave) {
			canSave = controlloDatiSalvataggio();
			if (canSave) {
				settaDati();
				if (this.currentIsasRecord == null) {
					doInsertRecord();
				} else {
					doUpdateRecord();
				}
			} else {
				logger.trace(punto + " non posso effetturare il salvataggio ");
			}
		} else {
			logger.debug(punto + " non effettuo salvataggio ");
		}
		 * /
		
		
		switch(check_save_step){
		case 0: if(!controlloDatiSalvataggio()) return false;
		case 1: settaDati(); canSave = true; settaSaveStep(0);break;
		}		
		*/
		return canSave;
	}

	/*
	private void settaDati() throws SQLException {
		if (this.currentIsasRecord == null && !isDataValida(skm_medico_da)) {
			skm_medico_da.setValue(skm_data_apertura.getValue());
		}
		//Jessy 11/05 deve essere salvato sempre l'ultimo operatore che modifica qcs
		cod_operatore.setText(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		desc_operat.setText(ManagerProfile.getCognomeNomeOperatore(getProfile()));
		
		
		String prData = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.PR_DATA);
		if (ISASUtil.valida(prData)) {
			try {
				pr_data.setValue(UtilForBinding.getDateFromIsas(prData));
			} catch (WrongValueException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		if (this.currentIsasRecord == null) {
			doInsertRecord();
		} else {
			doUpdateRecord();
		}
	}

	private void settaSaveStep(int step) throws Exception {
		check_save_step = step;
	}
	private boolean controlloDatiSalvataggio() {
		boolean canSave = true;
		String punto = ver + "controlloDatiSalvataggio ";
		logger.debug(punto + " controllo dati salvataggio ");

		canSave = controlloDataAperturaInferioreContatto();
		if (!canSave) {
			return canSave;    
		}
		Date dataApertura = skm_data_apertura.getValue();
		Date dataChiusura = skm_data_chiusura.getValue();
		boolean isChiusura = dataChiusura != null;
		boolean isMotivoChiusuraInserito = isMotivoChiusuraInserito();

		if (isChiusura) {
			canSave = ManagerDate.controllaPeriodo(self, skm_data_apertura, skm_data_chiusura, "lb_skm_data_apertura", "lb_skm_data_chiusura");
//			if (dataApertura.after(dataChiusura)) {
//				Label lblSmesData = (Label) self.getFellow("lb_skm_data_apertura");
//				Label lblSkmChiusura = (Label) self.getFellow("lb_skm_data_chiusura");
//				skm_data_chiusura.setErrorMessage(Labels.getLabel("common.msg.NoOrderDate.1maggioreDi0",
//						new String[] { lblSmesData.getValue(), lblSkmChiusura.getValue() }));
//				canSave = false;
//				return canSave;
//			} else {
//				logger.debug(punto + "da ok ");
//			}
			
			if (canSave){
				logger.debug(punto + "da ok ");
			}else {
				logger.debug(punto + "periodo non valido>>");
				return canSave;
			}
			
			if (isDataValida(skm_data_chiusura)) {
				//				int i = UtilForUI.standardYesOrNo(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.contatto",
				//							new String[]{""}), null );
				//				canSave =(i == 1);//no
				canSave = messaggioConfermaChiusura();
//				return messaggioConfermaChiusura();
			}
			if (!canSave) {
				return canSave;
			}
			if (isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg"));
				cbxMotivoChius.focus();
				canSave = false;
				return canSave;
			}
		} else {
			logger.debug(punto + " contatto non chiuso ");
			if (isMotivoChiusuraInserito) {
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.no.data"));
				cbxMotivoChius.focus();
				canSave = false;
				return canSave;
			}
		}
		logger.debug(punto + " fine ");
		return canSave;
	}

	private boolean controlloDataAperturaInferioreContatto() {
		String punto = ver + "controlloDataAperturaInferioreContatto ";
		boolean controlloOk = true;
		String prData = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.PR_DATA);
		logger.trace(punto + " dati letti>>" + super.caribelContainerCtrl.hashChiaveValore + "<<<");

		if (ISASUtil.valida(prData) && isDataValida(skm_data_apertura)) {
			try {
//				Date skVal = new Date();
//				SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy");
//				try {
//					skVal = format1.parse(prData);
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				logger.trace(punto + " data>>" + prData + "<<");
//				Date dataApertura = skm_data_apertura.getValue();
//				if (skVal.after(dataApertura)) {
//					UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.aperto"));
//					controlloOk = false;
//				}
				
				if (pr_data.getValue()!=null) {
					try {
						logger.trace(" data>>" + pr_data.getValue().toString() + "<<");
						Date dataApertura = skm_data_apertura.getValue();
						if (DateUtils.truncate(skm_data_apertura.getValue(), Calendar.DATE).after(DateUtils.truncate(dataApertura, Calendar.DATE))) {
							UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.aperto"));
							controlloOk = false;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return controlloOk;
	}
	
	
	private boolean messaggioConfermaChiusura() {
		rispostaChiusuraContatto = false;
		Messagebox.show(Labels.getLabel("contatto.medico.anamnesi.motivo.chiusura.msg.contatto", new String[] {}),
				Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if (Messagebox.ON_YES.equals(event.getName())) {
							settaSaveStep(RISPOSTACHIUSURACONTATTO);
							doSaveForm();
							doMakeControlAfterRead();
						}
					}
				});
		return rispostaChiusuraContatto;
	}

	private void doMakeControlAfterRead() throws WrongValueException, Exception {
		
		/*
		if (getProfile().getStringFromProfile("ctrl_skinf_siad").equals(CostantiSinssntW.SI) && 
				isInUpdate() && cbx_motivo.isVisible())
		{
			if (cbx_motivo.getSelectedValue().equals(""))
			cbx_motivo.setDisabled(false);
			else cbx_motivo.setDisabled(true);
		}
		else cbx_motivo.setDisabled(false);
			
		//setto se prima valutazione o rivalutazione (x Toscana, RFC115)
		if (ManagerProfile.isConfigurazioneToscana(getProfile()) && 
				getProfile().getStringFromProfile("rfc115_6").equals(CostantiSinssntW.SI))
			settaValutazione();
			
		// simone 14/08/14 i metodi seguenti sono stati spostati nella gestisciLeftMenu del containerInf
//		checkMotivoxFlussi();
//        checkEsistePresaCar();
//        checkEsistePianoAssist();
//        faiMsgNoGestFlussi();
		
		//gestisco personalizzazioni
		gestisciPersonalizzazioni();
		
		// gestisco l'abilitazione/disabilitazione dei vari componenti, a seconda dello stato della maschera
		abilitazioniMaschera();
		
		* /
	}
 

	private boolean isDataValida(CaribelDatebox data) {
		String punto = ver + "isDataValida ";
		boolean dataValida = false;
		Date dt = data.getValue();
		dataValida = (dt != null);
		logger.trace(punto + " data valida>>" + dt + "<< dataValida>" + dataValida + "<<");
		return dataValida;
	}   

	private boolean isMotivoChiusuraInserito() {
		boolean motivoChiusuraInserito = false;
		if (cbxMotivoChius.getSelectedValue()!=null && !cbxMotivoChius.getSelectedValue().equals("") 
				&& (cbxMotivoChius.getSelectedItem()!=null)
				&& !cbxMotivoChius.getSelectedItem().getLabel().equals(CostantiSinssntW.VALORE_COMBO_DEFAULT)) {
			cbxMotivoChius.focus();
			motivoChiusuraInserito = true;
		}
		logger.trace(ver + "isMotivoChiusuraInserito "+motivoChiusuraInserito );
		return motivoChiusuraInserito;
	}

	private void doUpdateRecord() throws SQLException {
		String punto = ver + "doInsertRecord ";
		logger.trace(punto + " sono in insert ");

		String data_corrente = skm_data_apertura.getText();
		//ilaria controllo anche la data chiusura
		String data_corrente_chiu = skm_data_chiusura.getText();
		String dataContatto = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skm_data_apertura");
		String dataCorrenteChiu = ISASUtil.getValoreStringa(this.currentIsasRecord.getHashtable(), "skm_data_chiusura");
		if (!data_corrente.equals(dataContatto) || !data_corrente_chiu.equals(dataCorrenteChiu)) {
			//entra qui quando è cambiata la data apertura contatto o la data chiusura contatto
			Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
			Hashtable<String, String> datiRicevuti = selectControlloData();
			//System.out.println("Controllodata=>"+trovaInterv.getUnmaskedText());
			String trovaInterv = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV);
			String trovaIntervMax = ISASUtil.getValoreStringa(datiRicevuti, CTS_TROVA_INVERV_MAX);
			if (!(trovaInterv.equals("N"))) {//ci sono degli interventi controllo le date
				String data = trovaInterv;
				String datastr = data.substring(8, 10) + "/" + data.substring(5, 7) + "/" + data.substring(0, 4);
				java.sql.Date dtscad = java.sql.Date.valueOf(data);
				data = data_corrente;
				data = data.substring(6, 10) + "-" + data.substring(3, 5) + "-" + data.substring(0, 2);
				java.sql.Date corrente = java.sql.Date.valueOf(data);
				if (corrente.after(dtscad)) {
					new it.pisa.caribel.swing2.cariInfoDialog(null, "Impossibile effettuare il salvataggio.\nSono presenti interventi con "
							+ "data minore della data apertura inserita.\n" + "Massima data valida " + datastr, "Attenzione!").show();
					//					return count=-1;
				}
				//controllo la data chiusura
				if (!data_corrente_chiu.equals("__/__/____")) {
					String data_max = trovaIntervMax;
					String datastr_chiu = data_max.substring(8, 10) + "/" + data_max.substring(5, 7) + "/" + data_max.substring(0, 4);
					java.sql.Date dtscad_chiu = java.sql.Date.valueOf(data_max);
					data_max = data_corrente_chiu;
					data_max = data_max.substring(6, 10) + "-" + data_max.substring(3, 5) + "-" + data_max.substring(0, 2);
					java.sql.Date corrente_chiu = java.sql.Date.valueOf(data_max);
					if (dtscad_chiu.after(corrente_chiu)) {
						new it.pisa.caribel.swing2.cariInfoDialog(null,
								"Impossibile effettuare il salvataggio.\nSono presenti interventi con"
										+ " data maggiore della data chiusura inserita.\n" + "Minima data valida " + datastr_chiu,
								"Attenzione!").show();
						//						return count=-1;
					}
				}
				//se c'è qualcosa che non va, qui non arriva altrimenti deve fare update
				//				count=db.Update(t);
				//				setTipoUVG();

			} else {
				//				count=db.Update(t);
				//               setTipoUVG();
			}
		}
	}

	private Hashtable<String, String> selectControlloData() throws SQLException {
		String punto = ver + "selectControlloData ";
		Hashtable<String, String> datiRicevuti = new Hashtable<String, String>();
		Hashtable<String, String> controlloInterv = new Hashtable<String, String>();
		controlloInterv.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
		controlloInterv.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
		//		super.select("query_controlloData",profile.getParameter("skmed"),t);
		ISASRecord dbrInterv = myEJB.query_controlloData(CaribelSessionManager.getInstance().getMyLogin(), controlloInterv);

		datiRicevuti.put(CTS_TROVA_INVERV, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV));
		datiRicevuti.put(CTS_TROVA_INVERV_MAX, ISASUtil.getValoreStringa(dbrInterv, CTS_TROVA_INVERV_MAX));
		logger.debug(punto + " dati recuperati>>" + (datiRicevuti != null ? datiRicevuti + "" : " no dati") + "");

		return datiRicevuti;
	}

	private void doInsertRecord() {
		String punto = ver + "doUpdateRecord ";
		logger.trace(punto + " sono in update ");
	}

	
	private String recuperaNContatto(String nCartella, String prData) throws ISASPermissionDeniedException, SQLException, CariException {
		String nContatto = "";
		String punto = ver + "recuperaNContatto ";
		logger.debug(punto + " recuperare nContatto>>" + nCartella);
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.PR_DATA, prData);

		ISASRecord dbrSkMed = myEJB.getContattoMedCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
		nContatto = ISASUtil.getValoreStringa(dbrSkMed, CostantiSinssntW.N_CONTATTO);
		logger.debug(punto + " contatto recuperato>>" + nContatto);

		return nContatto;
	}

	public void doStampa() {
		try{
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getText());
			dati.put(CostantiSinssntW.N_CONTATTO, n_contatto.getText());
			dati.put("data_ap", skm_data_apertura.getText());
			dati.put("data_chiu", skm_data_chiusura.getText());
			dati.put("assistito", UtilForContainer.getCognomeNomeAssistito());
			dati.put("operatore", ManagerProfile.getCognomeNomeOperatore(getProfile()));
//			TODO NON C'e stampa
//			Executions.getCurrent().createComponents(StampaContattoMedicoCtrl.CTS_FILE_ZUL, self, dati);
		}catch(Exception ex){
			doShowException(ex);
		}
	}

	private void abilitazioneMaschera() throws WrongValueException, Exception {
		boolean insert = false;
		
		if (super.currentIsasRecord != null) {
			insert = false;
		} else {
			String nCartella = (String) arg.get("n_cartella");
			n_cartella.setText(nCartella);
			n_contatto.setText("0");
			insert = true;
			skm_medico.setValue(getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
			skm_medico_descr.setValue(ManagerProfile.getCognomeNomeOperatore(getProfile()));
			String codPresidio = getProfile().getStringFromProfile(ManagerProfile.PRES_OPERATORE);
			skm_cod_presidio.setValue(codPresidio);
			presidio_descr.setValue(ManagerDecod.decodPresidi(getProfile(), codPresidio));
			String skm_data_chiusura = ISASUtil.getValoreStringa(this.currentIsasRecord, "skm_data_chiusura");
//			if (ManagerDate.validaData(skm_data_chiusura)){
//				statoConsultazione  = true;
//			}
			logger.trace(ver + " aggiorno>>" + "<< " + "\ndesc>>" + "<");
			//			cbxMotivoChius.setSelectedValue("0");
		}
		
		logger.trace(ver + " sono in inserimento:" + insert+" statoConsultazione ");
		btn_accessi.setDisabled(insert);
		btn_protesica.setDisabled(insert);
		btn_riapri.setDisabled(true); // sia in inser/update deve essere sempre disabilitato
		btn_storico.setDisabled(insert);
		btn_patologie.setDisabled(insert);
		skm_data_chiusura.setDisabled(insert);
		cbxMotivoChius.setDisabled(insert);
		label_defInizioPer.setVisible(!insert);
		label_defFinePer.setVisible(!insert);
		
		if (isContattoInfChiuso()){
			logger.trace(ver + " sono in  statoConsultazione ");
//			arg.put("forceReadOnly", true);
			this.setReadOnly(true);
			btn_accessi.setDisabled(false);
			btn_protesica.setDisabled(false);
			btn_storico.setDisabled(true);
			
			boolean abilRiap = (getProfile().getIsasUser().canIUse(ManagerProfile.REOPEN_SCHEDA_MED,"MODI"));
			this.btn_riapri.setVisible(abilRiap);
			this.btn_riapri.setDisabled(false);	
			
		}else {
			logger.trace(ver + "NON sono in Consultazione ");
		}

	}
	
	public boolean isContattoInfChiuso(){
		return ManagerDate.validaData(skm_data_chiusura);
	}

	public void onAccessi(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onAccessi ";
		logger.debug(punto + "inizio ");
		
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("n_cartella", n_cartella.getValue().toString());
		map.put("n_contatto", n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);

		accessi = Executions.getCurrent().createComponents(AccessiEffettuatiGridCtrl.myPathFormZul, self, map);
	}

	public void onProtesica(ForwardEvent e) throws Exception {
		String punto = ver + ".onProtesica ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		protesica = Executions.getCurrent().createComponents(RiepilogoAusiliProtesicaGridCtrl.myPathFormZul, self, map);
	}
	
	public void onClick$btn_riapri(Event event) throws Exception	{
		try{
			doRiapriContatto();
		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	public void doRiapriContatto() throws Exception {
		String punto = ver + "doRiapriContatto() ";
		
		Object isSkValChiusaObj = UtilForContainer.getObjectFromMyContainer(ContainerMedicoCtrl.CTS_IS_SK_VAL_CHIUSA); 
		if(isSkValChiusaObj !=null){
			// ctrl skValutazione aperta
			boolean isSkValChiusa = ISASUtil.getvaloreBoolean(isSkValChiusaObj);
			if(isSkValChiusa){
				UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.msg.errore.riapertura"));
				return ;
			}
		}	
//		ctrl non esistenza di altri contatti successivi
		Hashtable hCtrl = new Hashtable();
		hCtrl.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, CostantiSinssntW.N_CARTELLA));
		hCtrl.put(CTS_DATA_RIF, (skm_data_apertura !=null ? skm_data_apertura.getValueForIsas(): ""));
		logger.trace(punto + " dati che invio >>" + hCtrl + "<<");
		
		Object contattiSuccessivi =myEJB.query_checkContSuccessivi(CaribelSessionManager.getInstance().getMyLogin(), hCtrl); 
		if (ISASUtil.getvaloreBoolean(contattiSuccessivi)) {
			UtilForUI.standardExclamation(Labels.getLabel("contatto.medico.msg.errore.no.riapertura"));
			return;
		}
			
		skm_data_chiusura.setValue(null);
		cbxMotivoChius.setSelectedIndex(-1);
		super.doSaveForm();
		
		// aggiorno il record
        boolean risu = doSaveForm();
//        TODO BOFFA SETTARE I CASI DELL'ESITO DEL SALVATAGGIO
//    	if (risu > 0){
//			if (this.myContainer != null) {
//				// chiudo e riapro il contatto per resettare tutti i livelli
//				this.myContainer.invocaMetodo("concludiCont");
//			}
//		} else if (risu != -2){
//			new it.pisa.caribel.swing2.cariInfoDialog(null,"Si è verificato un errore in fase di aggiornamento/inserimento","Attenzione!").show();
//			setStato(this.CONSULTA);
//		}
        
        this.setReadOnly(false);
        cod_operatore.setDisabled(true);
        desc_operat.setDisabled(true);
        
        doMakeControlAfterRead();
		
		doFreezeForm();
	}
	

	public void onStorico(ForwardEvent e) throws Exception {
		String punto = ver + ".onStorico ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue().toString());
		map.put(CostantiSinssntW.N_CONTATTO, n_contatto.getValue().toString());
		map.put(CTS_TIPO_OPERATORE, CostantiSinssntW.TIPO_OPERATORE_MEDICO);

		String cognomeNome = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl instanceof ContainerMedicoCtrl) {
			cognomeNome = ISASUtil.getValoreStringa(((ContainerMedicoCtrl) caribelContainerCtrl).hashChiaveValore,
					CostantiSinssntW.ASSISTITO_COGNOME);
			cognomeNome += (ISASUtil.valida(cognomeNome) ? " " : "")
					+ ISASUtil.getValoreStringa(((ContainerMedicoCtrl) caribelContainerCtrl).hashChiaveValore,
							CostantiSinssntW.ASSISTITO_NOME);
		}

		map.put(StoricoMedicoReferenteGridCtrl.CTS_COGNOME_NOME_ASSISTITO, cognomeNome);
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
		//TODO BOFFA non permettere la riapertura se la finestra è gia aperta
		map.put(CTS_ZUL_CHIAMANTE, (AutorizzazioniMMGFormCtrl) this);//Utile per l'aggiornamento da finestra modale
		if (storico_operatore_ref == null) {
			storico_operatore_ref = Executions.getCurrent().createComponents(StoricoMedicoReferenteGridCtrl.myPathFormZul, self, map);
			logger.trace(punto + " Chiudo storico Medico Referente già aperto ");
			storico_operatore_ref = null;
		} else {
			logger.trace(punto + " storico Medico Referente già aperto ");
		}
	}

	public void settaMedicoReferente(String codice, String descrizione, Date dataDa) throws Exception {
		String punto = ver + ".onPatologie ";
		logger.debug(punto + "inizio codice>>" + codice + "<< descrizione>>" + descrizione + "<< data>>" + dataDa);
		skm_medico.setValue(codice);
		skm_medico_descr.setValue(descrizione);
		skm_medico_da.setValue(dataDa);
	}


	public void onPatologie(ForwardEvent e) throws Exception {
		String punto = this.getClass().getName() + ".onPatologie ";
		logger.debug(punto + "inizio ");

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put(CTS_DATA_APERTURA, skm_data_apertura.getValue());
		if(skm_data_chiusura!=null)
			map.put(CTS_DATA_CHIUSURA, skm_data_chiusura.getValue());
		map.put("caribelContainerCtrl", this.caribelContainerCtrl);
		if (vettOper != null && vettOper.size() > 0) {
			map.put("vettOper", vettOper);
		}
		try {
			diagnosi = Executions.getCurrent().createComponents(
					DiagnosiFormCtrl.myPathFormZul, self, map);
			diagnosi.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
				@SuppressWarnings({ "rawtypes", "unchecked" })
				public void onEvent(Event event) throws Exception{
					CaribelListModel lm = new CaribelListModel();
					Hashtable h = new Hashtable();
					h.put("data_apertura", skm_data_apertura.getValueForIsas());
					if (skm_data_chiusura != null)
						h.put("data_chiusura", skm_data_chiusura.getValueForIsas());
					h.put(CostantiSinssntW.N_CARTELLA,n_cartella.getValue().toString());
					lm.addAll((Collection) invokeGenericSuEJB(new DiagnosiEJB(), h, "queryLastDiagContesto"));
					tablePrestazioni.setModel(lm);
					doFreezeForm();
				}
			});
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	private void gestionePannelloScale() throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		if (panel_scale == null) {
			map.put("cartella", n_cartella.getValue().toString());
			map.put("contatto", n_contatto.getValue().toString());
			boolean conTempoT = true;
			boolean tuttiCampi = false;
			int CASO_SAN = 1; // caso semplice sanitario
			map.put("tutti_campi", String.valueOf(tuttiCampi));
			map.put("con_tempo_t", String.valueOf(conTempoT));
			// TP_OPER_MED ="03"; // medico
			map.put("tipo_op", "03");
			map.put("chiamante", String.valueOf(CASO_SAN));
			map.put("classe_padre", this);
			if (pr_data != null)
				map.put("pr_data", pr_data.getValueForIsas());

			panel_scale = Executions.getCurrent().createComponents(
					ScalePanelCtr.myPathFormZul, tabpanel_scale, map);
			scalPnlCtr = (ScalePanelCtr) panel_scale.getAttribute(MY_CTRL_KEY);

		} else {
			scalPnlCtr.settaDtApeAllScale(UtilForBinding.getValueForIsas(skm_data_apertura.getValue()));
			scalPnlCtr.settaDtApeSkVal(UtilForBinding.getValueForIsas(pr_data.getValue()));
			scalPnlCtr.settaNCart(String.valueOf(n_cartella.getValue())); 
			scalPnlCtr.getTempoT();
		}
	}
	
    // lettura di tutti i campi di ogni scala max per un certo tempoT
    //  (anche se mostro solo la data)
    public Boolean leggiDateMaxScale() throws Exception{
        int count = 0;
        if(panel_scale != null)
        	count = scalPnlCtr.leggiScaleMax();

        return new Boolean(count > 0);
    }

	private void loadTipologiaUtente(CaribelCombobox cbx) throws Exception {
		cbx.clear();
		Hashtable h = new Hashtable();
		// comboPreLoad.load("TIPUTES","query",profile.getParameter("tiputes"),cTipo,jCariComboBoxTipute, "codice", "descrizione");
		CaribelComboRepository.comboPreLoad("f_tipo_utente", new TiputeSEJB(), "query", h, cbx, null, "codice", "descrizione", false);
	}

	
	*/

	@Override
	protected boolean doSaveForm() throws Exception {
		boolean ret = super.doSaveForm();
//		TODO CONTROLLARE ABILITAZIONE MASCHERA
//		abilitazioneMaschera();
		return ret;
	}
	
	
}
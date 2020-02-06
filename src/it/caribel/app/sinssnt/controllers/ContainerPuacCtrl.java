package it.caribel.app.sinssnt.controllers;

import it.caribel.app.sinssnt.bean.MotivoSEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkmmgEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RmRichiesteMMGEJB;
import it.caribel.app.sinssnt.controllers.intolleranzeAllergie.IntolleranzeAllergieGridCRUDCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerLogout;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.segnalazione.SegnalazioneGridCRUDCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.PreferenzeStruttureCtrl;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SegreteriaOrganizzativaFormBaseCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.app.sinssnt.util.UtilForContainerGen;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.ButtonMenuContainer;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tree;

public class ContainerPuacCtrl extends ContainerSinssntCtrl {
	private static final long serialVersionUID = 1L;
	public static final String myPathZul = "/web/ui/containerPuac.zul";
	public static final String CTS_IS_SK_VAL_CHIUSA = "isSkValChiusa";
	Tab menuLeftTabContatto;
	Tab menuLeftTabAutorizzazioni;  
	Tree menu_tree_operazioni;
	public static final String CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA = "puacSchedaCorrForm";
	public static final String CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO = "puacSchedaStoGrid";
	public static final String CTS_RICHIESTE_MMG = "ElencoRichiesteMMGForm";
	public static final String CTS_RICHIESTA_MMG = "richiestaMMGForm";
	public static final String CTS_SEGNALAZIONI = ContainerMedicoCtrl.CTS_SEGNALAZIONI;
	private String fonte = "";
	private boolean aprireSegnalazione = false;
	private int ricaricaSo = 0; // caso in cui modifica anagrafica, DEVO RICARICARE LA SCHEDA SO, ALTRIMENTI MANTINE I VECCHI DATI ANAGRAFICI
	private String CTS_INTOLLERANZE_ALLERGIE = "intolleranzeAllergieForm";
	private String CTS_RIEPILOGO_ACCESSI = "riepilogoAccessi";
	private String CTS_RELAZIONE_MEDICA = "medicoRelCliForm";
	private String CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE = "sntAutorizzazioneMMG";
	private String CTS_AUTORIZZAZIONI_MMG_STORICO = "sntAutorizzazioneMMGStorico";
	private String CTS_AUTORIZZAZIONI_MMG_ADI = "sntAutorizzazioneMMGADI";
	private String CTS_AUTORIZZAZIONI_MMG_ADP = "sntAutorizzazioneMMGADP";
	private String CTS_AUTORIZZAZIONI_MMG_ADR = "sntAutorizzazioneMMGADR";
	private boolean isConCtrlFlusSiad;
	private String codMotivo_xFlussiSiad;
	private String CTS_FLUSSI_SIAD_FORM = "flussiSiadForm";
	private String CTS_FLUSSI_SIAD_STORICO_FORM = "flussiStoForm";

	private String PUA="", PAP="", UVM="";
	private String ver = "16-";
	
	public ComponentInfo doBeforeCompose(Page page, Component parent,ComponentInfo compInfo){
		try{
			ISASUser iu =CaribelSessionManager.getInstance().getIsasUser();
			if(iu!=null && !iu.canIUse(ChiaviISASSinssntWeb.A_OPPUAC)){
				Messagebox.show(
						Labels.getLabel("commons.toolbar.area.msg.noPermessi"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.INFORMATION);
				(new ManagerLogout()).doLogout();
				return null;
			}else
				return super.doBeforeCompose(page, parent, compInfo);
		}catch(Exception ex){
			UtilForUI.standardExclamation(Labels.getLabel("exception.generic.error.msg"));
			return null;
		}
	}
	
	protected void initContainer() {
		super.initContainer();
		String punto = ver + "initContainer ";
		List<Component> listComp = UtilForComponents.getAllChildren(menu_left);
		for (Component corrComp : listComp) {
			if (corrComp instanceof ButtonMenuContainer) {
				String idForm = ((ButtonMenuContainer) corrComp).getIdForm();
				String pathZulForm = ((ButtonMenuContainer) corrComp).getPathZulForm();
				hIdUrizul.put(idForm, pathZulForm);
				super.hashMenuLeft.put(idForm, ((ButtonMenuContainer) corrComp));
			}
			settaDatiConfigurazione();
		}

		// Simone: inizializzo parametri per controllo flussi
		isConCtrlFlusSiad = getProfile().getStringFromProfile("ctrl_skinf_siad").equals(CostantiSinssntW.SI);
		if (isConCtrlFlusSiad) {
			MotivoSEJB motivoEjb = new MotivoSEJB();
			try {
				this.codMotivo_xFlussiSiad = motivoEjb.getCodMotivoSxFlussi(CaribelSessionManager.getInstance().getMyLogin(),
						(Hashtable<String, String>) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.trace(punto + " gestito ");
	}

	private void settaDatiConfigurazione() {
		String confPUA = (String) profile.getParameter("titolo_pua");
		  PUA = ((confPUA != null && !confPUA.trim().equals("NO")) ? confPUA : "PUA");
		//Descrizione termine PAP da Configuratore
		String confPAP = (String) profile.getParameter("titolo_pap");
		  PAP = ((confPAP != null && !confPAP.trim().equals("NO")) ? confPAP : "PAP");
		//Descrizione termine UVM da Configuratore
		String confUVM = (String) profile.getParameter("titolo_uvm");
		  UVM = ((confUVM != null && !confUVM.trim().equals("NO")) ? confUVM : "UV");
	}

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		initContainer();
		Selectors.wireEventListeners(comp, this);//Per collegare gli eventi sul menu di destra dopo l'initContainer
		//Recupero il codice assistito passato dall'altro container
		String n_cartella = Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA);
		fonte = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE);
		aprireSegnalazione = (ISASUtil.valida(fonte) && fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI+""));
		
		if(n_cartella!=null && !n_cartella.trim().equals("")){
	        UtilForContainer.restartContainerFromFassi(n_cartella);
	    }else{
	    	UtilForContainer.restartContainerFromListaAttivita();
	    }
		
		UtilForContainer.refreshToolBar();
		if(Executions.getCurrent().getSession().hasAttribute(CostantiSinssntW.FIRST_ACCESS)){
			UtilForUI.doTestVersionBrowser();
			Executions.getCurrent().getSession().removeAttribute(CostantiSinssntW.FIRST_ACCESS);
		}
		
		configuraMenuOperazioni();
	}
	
	private void configuraMenuOperazioni(){
		if(menu_tree_operazioni !=null){
			MenuTreeNode corr;
			MenuItem corrMenuItem;
			int childCount = menu_tree_operazioni.getModel().getChildCount(menu_tree_operazioni.getModel().getRoot());
			for(int i=0; i<childCount;i++){
				corr = ((MenuTreeNode)menu_tree_operazioni.getModel().getChild(new int[] {i}));
				corrMenuItem = ((MenuItem)corr.getData());
				if(corrMenuItem.getKeyPermission()!=null){
					if( corrMenuItem.getKeyPermission().equals(ChiaviISASSinssntWeb.INTERV))
						//corrMenuItem.getKeyPermission().equals(ChiaviISAS.TABPIPP))
						//corrMenuItem.getKeyPermission().equals(ChiaviISAS.ACCSPE))
							corrMenuItem.setEnabled(false);
				}
			}
		}
	}

	public void gestisciChiaviCorr(Component comp) {
		String punto = ver +" gestisciChiaviCorr ";
		logger.trace(punto + " chiave>>" + (comp != null ? comp.getId() + "" : " no dati ") + "<<");

		if (comp != null) {
			if (comp.getId() == null || comp.getId().equals(""))
				return;

			if (comp.getId().equals("cartellaForm")) {
				Object n_cartella = ((CaribelIntbox) comp.query("#n_cartella")).getRawValue();
				if (n_cartella != null)
					hashChiaveValore.put("n_cartella", n_cartella);
				Object cod_com_nasc = ((CaribelTextbox) comp.query("#cod_com_nasc")).getRawValue();
				if (cod_com_nasc != null)
					hashChiaveValore.put(CostantiSinssntW.COD_COM_NASC, cod_com_nasc);   
				Object desc_com_nasc = ((CaribelCombobox) comp.query("#desc_com_nasc")).getRawValue();
				if (desc_com_nasc != null)
					hashChiaveValore.put(Costanti.DESC_COM_NASC, desc_com_nasc);   
				Object dataNasc = ((CaribelDatebox) comp.query("#data_nasc")).getRawValue();
				if (dataNasc != null)
					hashChiaveValore.put("data_nasc", dataNasc);
				Object dataCart = ((CaribelDatebox) comp.query("#data_apertura")).getRawValue();
				if (dataCart != null)
					hashChiaveValore.put("data_apertura", dataCart);
				Object dataCartChius = ((CaribelDatebox) comp.query("#data_chiusura")).getRawValue();
				if (dataCartChius != null)
					hashChiaveValore.put("data_chiusura", dataCartChius);
				Object codiceFiscale = ((CaribelTextbox)comp.query("#cod_fiscale")).getRawValue();
				if(codiceFiscale!=null)
					hashChiaveValore.put(Costanti.ASSISTITO_COD_FISC, codiceFiscale);

			} else if (comp.getId().equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA)) {
				String id_RICHIESTA = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_ID_RICH);
				logger.trace(punto + " dati disponibili>>" + hashChiaveValore + "<\n");	
				Object idSkso = ((CaribelIntbox)comp.query("#id_skso")).getRawValue();
				String n_cartella = ((CaribelTextbox) comp.query("#n_cartella")).getRawText();
				if (idSkso !=null){
					hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
					hashChiaveValore.put(Costanti.CTS_ID_DOMANDA, idSkso);
					hashChiaveValore.put(Costanti.CTS_ID_RICHIESTA, PreferenzeStruttureCtrl.CTS_ID_RICHIESTA);
					hashChiaveValore.put(Costanti.CTS_OPERATORE_DISTRETTO, ManagerProfile.getDistrettoOperatore(getProfile()));
					hashChiaveValore.put(Costanti.CTS_OPERATORE_ZONA, ManagerProfile.getZonaOperatore(getProfile()));
				}
//				recuperaSchedaSO(n_cartella);
				addChiaveValore(CostantiSinssntW.CTS_ID_SKSO, "#id_skso", comp);
				addChiaveValore(CostantiSinssntW.CTS_PR_DATA_PUAC, "#pr_data_puac", comp);
				addChiaveValore(CostantiSinssntW.PR_DATA_CHIUSURA, "#pr_data_chiusura", comp);
				addChiaveValore(CostantiSinssntW.CTS_SKSO_MMG_DATA_INIZIO, "#data_inizio", comp);
				addChiaveValore(CostantiSinssntW.CTS_SKSO_MMG_DATA_FINE, "#data_fine", comp);
			} else if (comp.getId().equals(CostantiSinssntW.CTS_DIARIO)) {
				logger.debug(punto + "diario ");
			} else if (comp.getId().equals(CTS_RELAZIONE_MEDICA)) {
				logger.debug(punto + "RELAZIONE MEDICA");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)) {
				logger.debug(punto + "CTS_SCHEDA_MMG_CORRENTE");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)) {
				logger.debug(punto + "CTS_AUTORIZZAZIONI_MMG_ADI");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)) {
				logger.debug(punto + "CTS_AUTORIZZAZIONI_MMG_ADP ");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)) {
				logger.debug(punto + "CTS_AUTORIZZAZIONI_MMG_ADR ");
			} else if (comp.getId().equals("fassiGrid")) {
				hashChiaveValore.clear();
			}
		}
	}

	public void gestisciTopMenu(Component comp) {
		String punto = ver + "gestisciTopMenu ";
		logger.trace(punto + " componente>>" + comp.getId() + "<<");

		if (comp != null) {
			if (comp.getId().equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA)) {
//				String n_contatto = ((CaribelIntbox) comp.query("#n_contatto")).getRawText();
//				String skm_data_apertura = ((CaribelDatebox) comp.query("#skm_data_apertura")).getRawText();
				String testo = "Storico ----";

//				if (n_contatto == null || n_contatto.equals(""))
//					testo = Labels.getLabel("caribelContainer.contatto.in_fase_di_ins");
//				else
//					testo = Labels.getLabel("caribelContainer.contatto.numero") + ": " + n_contatto + " "
//							+ Labels.getLabel("caribelContainer.contatto.data_apertura") + " " + skm_data_apertura;
				addItemMenuTop(comp, testo);
			} else

			if (comp.getId() == null || comp.getId().equals(""))
				return;

			if (comp.getId().equals("cartellaForm")) {
				String n_cartella = ((CaribelIntbox) comp.query("#n_cartella")).getRawText();
				String cognome = ((CaribelTextbox) comp.query("#cognome")).getRawText();
				String nome = ((CaribelTextbox) comp.query("#nome")).getRawText();
				String desc_com_nasc = ((CaribelCombobox) comp.query("#desc_com_nasc")).getRawText();
				String data_nasc = ((CaribelDatebox) comp.query("#data_nasc")).getRawText();

				String testo = Labels.getLabel("caribelContainer.cartella.numero") + ": " + n_cartella + " "
						+ Labels.getLabel("caribelContainer.cartella.assistito") + " " + cognome + " " + nome + " "
						+ Labels.getLabel("caribelContainer.cartella.data_nascita") + " " + data_nasc + " "
						+ Labels.getLabel("caribelContainer.cartella.luogo_nascita") + " " + desc_com_nasc;
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_COGNOME, cognome);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_NOME, nome);
				String dataApertura =((CaribelDatebox)comp.query("#data_apertura")).getRawText();
				
				String dataChiusura="";
				String valoreMotivoChiusura = "";
				try {
					dataChiusura=((CaribelDatebox)comp.query("#data_chiusura")).getRawText();
					valoreMotivoChiusura = "";
					if (ManagerDate.validaData(dataChiusura)){
						if (comp.query("#cbx_motivoChiusura")!=null && ((CaribelCombobox)comp.query("#cbx_motivoChiusura")).getRawText() !=null){
							valoreMotivoChiusura = ((CaribelCombobox) comp.query("#cbx_motivoChiusura")).getRawText();
						}
					}
				} catch (Exception e) {
					logger.error(punto + " Errore nel recuperare la chiusura cartella ");
				}
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_CARTELLA_APERTA, dataApertura);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA, dataChiusura);
				if (desc_com_nasc != null){
					hashChiaveValore.put("desc_com_nasc", desc_com_nasc);
				}
				
				testo = ContainerInfermieristicoCtrl.gestioneChiusuraCartella(testo, dataChiusura, valoreMotivoChiusura);
				addItemMenuTop(comp,testo, (ManagerDate.validaData(dataChiusura) ? CostantiSinssntW.CTS_ROW_RED:""));
//				addItemMenuTop(comp, testo);
			} else if (comp.getId().equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA)) {
				Object idSksoObj = ((CaribelIntbox)comp.query("#id_skso")).getRawValue();
				String idSkso = "";
				try {
					 idSkso= (idSksoObj!=null ?idSksoObj +"": "");
				} catch (Exception e) {
					e.printStackTrace();
				}
				logger.trace(punto + " idskso>>"+ idSkso + "<");
				
				if (!ISASUtil.valida(idSkso)){
					idSkso = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.CTS_ID_SKSO);
					logger.trace(punto + " Verifico nel cointainer >>"+ idSkso + "<");
				}
				String data_apertura = ((CaribelDatebox)comp.query("#pr_data_puac")).getRawText();
				String testo = "";

				if(ISASUtil.valida(idSkso)){
					testo = SegreteriaOrganizzativaFormBaseCtrl.getIntestazioneSchedaSo(idSkso, data_apertura);
				}else {
					idSkso = " 0 ";
					testo =Labels.getLabel("caribelContainer.contatto.menu.puac.scheda")+
							" "+Labels.getLabel("caribelContainer.contatto.menu.puac.in_fase_di_ins")+": "+idSkso;
				}
				String prDataChiusura = ((CaribelDatebox)comp.query("#pr_data_chiusura")).getRawText();
				if(ManagerDate.validaData(prDataChiusura)){
					testo+=" " +Labels.getLabel("SchedaInfForm.conclusione.data.linea.menu")+": "+ prDataChiusura;
				}
				
				addItemMenuTop(comp,testo,(ManagerDate.validaData(prDataChiusura) ? CostantiSinssntW.CTS_ROW_RED:""));
			} else if (comp.getId().equals(CTS_RICHIESTA_MMG)) {
						Object idRichMMGObj = ((CaribelIntbox)comp.query("#id_rich")).getRawValue();
						String idRichMMG = "";
						try {
							idRichMMG= (idRichMMGObj!=null ? idRichMMGObj +"": "");
						} catch (Exception e) {
							e.printStackTrace();
						}
						logger.trace(punto + " idRichMMG>>"+ idRichMMG + "<");
						
						if (!ISASUtil.valida(idRichMMG)){
							idRichMMG = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.CTS_ID_RICH);
							logger.trace(punto + " Verifico nel cointainer >>"+ idRichMMG + "<");
						}
						String data_apertura = ((CaribelDatebox)comp.query("#data_richiesta")).getRawText();
						String testo = "";

						if(ISASUtil.valida(idRichMMG)){
							testo = Labels.getLabel("caribelContainer.contatto.menu.puac.scheda.rich_mmg")+": "+idRichMMG+
									" "+Labels.getLabel("caribelContainer.contatto.menu.puac.data_apertura.rich_mmg")+" "+data_apertura;
						}else {
							idRichMMG = " 0 ";
							testo =Labels.getLabel("caribelContainer.contatto.menu.puac.scheda.rich_mmg")+
									" "+Labels.getLabel("caribelContainer.contatto.menu.puac.in_fase_di_ins.rich_mmg")+": "+idRichMMG;
						}

						addItemMenuTop(comp,testo);
			
				} else if (comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
						logger.debug(punto + " diario>>");
			} else if (comp.getId().equals(CTS_RELAZIONE_MEDICA)) {
				logger.debug(punto + " relazione medica ");

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)) {
				logger.debug(punto + "\n TODO \n CTS_AUTORIZZAZIONI_MMG_ADI ");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)) {
				logger.debug(punto + "\n TODO \n CTS_AUTORIZZAZIONI_MMG_ADP ");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)) {
				logger.debug(punto + "\n TODO \n CTS_AUTORIZZAZIONI_MMG_ADR ");

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)) {
				logger.debug(punto + " CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE ");

				//				String n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawText();
				String pr_dataMMg = ((CaribelDatebox) comp.query("#pr_data")).getRawText();
				String operatore = ((CaribelTextbox) comp.query("#desc_oper")).getRawText();
				String segnalante = ((CaribelCombobox) comp.query("#cbx_skmmg_segnalatore")).getRawText();
				//String patologia = ((CaribelCombobox) comp.query("#desc_patol1")).getRawText();
//				TODO BOFFA da recuperare in quanto le patologie sono state commentate 
				String patologia = "--DA RECUPERARE ";
				String testo = "";

				testo = Labels.getLabel("caribelContainer.autorizzazionemmg.scheda.mmg") + pr_dataMMg
						+ Labels.getLabel("caribelContainer.autorizzazionemmg.operatore") + operatore
						+ Labels.getLabel("caribelContainer.autorizzazionemmg.segnalante") + segnalante
						+ Labels.getLabel("caribelContainer.autorizzazionemmg.patologia") + patologia;

				/*String dtVariazSk = "";
				 String operAp = "";
				 String segnal = "";
				 String patol = "";

				 if (lastSkMMG.get("stato").toString().equals("2"))
				 {
				  dtVariazSk = lastSkMMG.get("pr_data").toString();
				  operAp = lastSkMMG.get("desc_oper").toString();
				  segnal = lastSkMMG.get("descr_segnal").toString();
				  patol = lastSkMMG.get("desc_patol1").toString();
				 }

				 lastSkMMG.put("mtdDaInvocare", "apriJFSkmmg");

				 String info = "SCHEDA MMG: Aperta il: " + dtVariazSk +
				         " - Operatore: " + operAp + " - Segnalante: " + segnal +
				         " - Patologia: " + patol;
				*/

				addItemMenuTop(comp, testo);

			}
		}
	}

	public void gestisciLeftMenu(Component comp) {
		String punto = "gestisciLeftMenu ";
		logger.debug(punto + " ");
		//Disabilito tutto
		disableAllButtonMenuLeft(true);
		if (ManagerProfile.isConfigurazioneMarche(getProfile())){
//			hashMenuLeft.get("flussiStoForm").setVisible(false);
//			hashMenuLeft.get("flussiSiadForm").setVisible(false);
			
		}
		String nCartella = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA);
		String id_skso = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.CTS_ID_SKSO);
		if (ISASUtil.valida(nCartella)) {
			//Abilito Contatto
			menuLeftTabContatto.setSelected(true);
			disableButtonMenuLeft(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA, false);
			disableButtonMenuLeft(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO, false);
			disableButtonMenuLeft(CTS_RICHIESTE_MMG, false);
//			//Abilito Relazione medica
//			disableButtonMenuLeft(CTS_RELAZIONE_MEDICA, false);
//			//			TODO CONTROLLARE LE ABILITAZIONI 
			disableButtonMenuLeft(CTS_FLUSSI_SIAD_FORM, false);
			disableButtonMenuLeft(CTS_FLUSSI_SIAD_STORICO_FORM, false);

			//Abilito Relazione medica
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_STORICO, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADI, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADP, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADR, false);

			checkEsistePianoAssist();
			//			faiMsgNoGestFlussi(comp);
			
			//Abilito il bottone di AsterView
			disableButtonMenuLeft("asterview", !conAsterView);
			//Abilito il bottone dei documenti
			disableButtonMenuLeft("documenti", false);
			//Abilito il bottone degli accertamentii
			//disableButtonMenuLeft("accertamenti", false);			
			//Abilito il bottone svama
			disableButtonMenuLeft("valutazioneSocialeGrid", false);
			
		}
		if (ISASUtil.valida(id_skso)) {
			//Abilito le intolleranze
			disableButtonMenuLeft(CTS_INTOLLERANZE_ALLERGIE, false);
			//Abilito il riepilogo accessi
			disableButtonMenuLeft(CTS_RIEPILOGO_ACCESSI, false);
			//Abilito Diario
			disableButtonMenuLeft(CostantiSinssntW.CTS_DIARIO, false);
			//Abilito Segnalazioni
			disableButtonMenuLeft(CTS_SEGNALAZIONI,false);
		}
		if (comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE) || comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_STORICO)
				|| comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI) || comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)
				|| comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)) {
			menuLeftTabAutorizzazioni.setSelected(true);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_STORICO, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADI, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADP, false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADR, false);
			
		}
	}

	public void checkEsistePianoAssist() {
		//		     if(getBoolValueFromHash("contattoInfInInsert"))
		//	            return;
		isConCtrlFlusSiad = true;
		if (isConCtrlFlusSiad) {
			String cart = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA);
			String cont = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CONTATTO);
			if (!cart.equals("") && !cont.equals("")) {
				try {
					Hashtable<String, String> h_dati = new Hashtable<String, String>();
					h_dati.put(CostantiSinssntW.N_CARTELLA, cart);
					h_dati.put(CostantiSinssntW.N_PROGETTO, cont);
					h_dati.put("pa_tipo_oper", CostantiSinssntW.TIPO_OPERATORE_MEDICO);
					PianoAssistEJB pa = new PianoAssistEJB();
					hashChiaveValore.put("existsPianoAssist",
							new Boolean(pa.checkEsistePianoAssist(CaribelSessionManager.getInstance().getMyLogin(), h_dati)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void gestisciApertureAutomatiche(Component comp) {
		String punto = ver +"gestisciApertureAutomatiche ";
		if (comp != null) {
			if (comp.getId() == null || comp.getId().equals(""))
				return;

			if (comp.getId().equals("cartellaForm")) {
				logger.trace(punto + " verifico se non ho un idSkso, tutte le volete che effettuo una modifica anagrafica e necessario " +
						" ricaricare i dati della scheda so ");
				// potrei aver fatto un modifica alla scheda so
				if (ISASUtil.getValoreIntero(hashChiaveValore,Costanti.CTS_ID_SKSO)<0){
					ricaricaSo ++;  
				}
				//Ho aperto una Cartella, allora provo ad aprire anche il relativo Contatto, se non e' stato ancora aperto
				String n_cartella = ((CaribelIntbox) comp.query("#n_cartella")).getRawText();
				if (super.hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA) == null && n_cartella != null && !n_cartella.equals("")) {
					recuperaSchedaSO(n_cartella);
					HashMap<String, Object> mapParam = new HashMap<String, Object>();
					mapParam.putAll(hashChiaveValore);
					Component schedaSegreteriaOrganizzativa = createComponent(hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA), mapParam);
					showComponent(schedaSegreteriaOrganizzativa);
				}
				if (n_cartella != null && !n_cartella.equals("")) {
					recuperagetLastSkMMG(n_cartella);         
				}
			} else if (comp.getId().equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA)){
				String n_cartella = ((CaribelTextbox) comp.query("#n_cartella")).getRawText();
				if (ISASUtil.valida(n_cartella) && ISASUtil.getValoreIntero(hashChiaveValore,Costanti.CTS_ID_SKSO)<0){
					recuperaSchedaSO(n_cartella);
//				|| comp.equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO)) {
				logger.trace(punto + " dati  ");
				super.removeComponent(hashIdComponent.get(CTS_RICHIESTE_MMG));
				HashMap<String, Object> mapParam = new HashMap<String, Object>();
				if(ricaricaSo >1 && ISASUtil.getValoreIntero(hashChiaveValore,Costanti.CTS_ID_SKSO)<0){
					ricaricaSo--;
					mapParam.putAll(hashChiaveValore);
					super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
					Component schedaSegreteriaOrganizzativa = createComponent(hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA), mapParam);
					showComponent(schedaSegreteriaOrganizzativa);
				}
				
				if (aprireSegnalazione){
					aprireSegnalazione = false;
					String nCartella = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA);
					mapParam = new HashMap<String, Object>();
					mapParam.put(CostantiSinssntW.N_CARTELLA, nCartella);
					mapParam.put(SegnalazioneGridCRUDCtrl.CTS_POSSO_MODIFICARE_TUTTE_SEGNALAZIONI, new Boolean(false));
					Component sntSegnalazioni = createComponent(hIdUrizul.get(CTS_SEGNALAZIONI), mapParam);
					showComponent(sntSegnalazioni);
				}
//					else {
//					boolean ricaricare = false;
//					if(super.hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA) != null){
//						String idSksoAperta = ((CaribelIntbox) comp.query("#id_skso")).getRawText();
//						String idSkso = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.CTS_ID_SKSO); 
//						
//						if (idSksoAperta.equals(idSkso)){
//							logger.trace(punto + " NON RICARICO LA SCHEDA ho gli stessi ID ");
//						}else {
//							logger.trace(punto + " RICARICO LA SCHEDA SKSO ");
//							super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
//							ricaricare = true;
//						}
//					}
//					if (ricaricare){
//						HashMap<String, Object> mapParam = new HashMap<String, Object>();
//						mapParam.putAll(hashChiaveValore);
//						Component schedaSegreteriaOrganizzativa = createComponent(hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA), mapParam);
//						showComponent(schedaSegreteriaOrganizzativa);
//					}
//				}
				
				}
			} else if (comp.getId().equals(CTS_RELAZIONE_MEDICA)) {
				HashMap<String, Object> mapParam = new HashMap<String, Object>();
				mapParam.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA));
				mapParam.put(CostantiSinssntW.N_CONTATTO, ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CONTATTO));

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)) {
				logger.trace(punto + " dati CTS_AUTORIZZAZIONI_MMG_ADI");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)) {
				logger.trace(punto + " dati CTS_AUTORIZZAZIONI_MMG_ADP");
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)) {
				logger.trace(punto + " dati CTS_AUTORIZZAZIONI_MMG_ADR ");

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)) {
				logger.trace(punto + " dati CTS_SCHEDA_MMG_CORRENTE");
				String n_cartella = ((CaribelTextbox) comp.query("#n_cartella")).getRawText();
				if (n_cartella != null && !n_cartella.equals("")) {
					recuperagetLastSkMMG(n_cartella);
				}
			}
		}
	}

	private void recuperagetLastSkMMG(String n_cartella) {
		/*  Carica i dati della scheda di valutazione */
		SkmmgEJB skmmgEJB = new SkmmgEJB();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		try {
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
			Hashtable<String, String> datiRicevuti = skmmgEJB.getLastSkMMG(CaribelSessionManager.getInstance().getMyLogin(), dati);
			/* pr_data autorizzazione non è uguale alla pr_data della valutazione */
			hashChiaveValore.put(CostantiSinssntW.MMGPRPR_DATA, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.MMGPRPR_DATA));
			hashChiaveValore.put(CostantiSinssntW.STATO, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.STATO));

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (CariException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			// TODO DA VERIFICARE PER IL DISCORSO DELLE ISASPERMISSION
			e.printStackTrace();
		}
	}

	private void recuperaSchedaSO(String n_cartella) {
		String punto = ver + "recuperaSchedaSO ";
		/*  Carica i dati della scheda SO */
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		try {
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
			ISASRecord datiRicevuti = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(),
					dati);
			logger.debug(punto + " dati presenti hashChiaveValore>>"+hashChiaveValore+"<");
			if (datiRicevuti!=null){
				logger.debug(punto + " dati ricevuti>>"+ (datiRicevuti!=null ? datiRicevuti.getHashtable()+"":" no dati "));
				hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.CTS_ID_SKSO));
				hashChiaveValore.put(Costanti.CTS_ID_DOMANDA, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.CTS_ID_SKSO));
				hashChiaveValore.put(Costanti.CTS_ID_RICHIESTA, PreferenzeStruttureCtrl.CTS_ID_RICHIESTA);
				hashChiaveValore.put(Costanti.CTS_OPERATORE_DISTRETTO, ManagerProfile.getDistrettoOperatore(getProfile()));
				hashChiaveValore.put(Costanti.CTS_OPERATORE_ZONA, ManagerProfile.getZonaOperatore(getProfile()));
			}
			logger.trace(punto + " dopo dati presenti hashChiaveValore>>"+hashChiaveValore+"<");
			
		} catch (CariException e) {
			e.printStackTrace();
		}
	}

	private void gestisciRefreshGrid(Component comp) {
		if (comp != null) {
			if (comp.getId() == null || comp.getId().equals(""))
				return;
		}
	}

	@Override
	public void onShowComponent(Event evt) {
		Component comp = ((Component) evt.getData());
		gestisciChiaviCorr(comp);
		gestisciTopMenu(comp);
		gestisciApertureAutomatiche(comp);
		gestisciRefreshGrid(comp);
		gestisciLeftMenu(comp);
	}
	
	@Listen("onClick=#btn_segnalazioniForm")
	public void btn_segnalazioniForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI),false,null);
			}else{
				super.showComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_puacSchedaCorrForm")
	public void btn_puacSchedaCorrForm() {
		String punto = ver + "btn_puacSchedaCorrForm ";
		logger.trace(punto + " verifico ");
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA,hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA),false,null);
			}else{
//				super.showComponent(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA, hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
				removeComponentsFrom(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA);
				super.showComponent(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA, hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA), mapParam);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_puacSchedaStoGrid")
	public void btn_puacSchedaStoGrid() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "N");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO,hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO),true,mapParam);
			}else{
				removeComponentsFrom(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO);
				super.showComponent(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO, hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO), mapParam);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_ElencoRichiesteMMGForm")
	public void btn_ElencoRichiesteMMGForm() {
		String punto = ver + "btn_ElencoRichiesteMMGForm ";
		try{
			int richieste = getRichieste();
			logger.trace(punto + " elementi recuperati>>"+ richieste);
			boolean accedereGriglia = true;
			if (richieste == 1){
				String statoRichiesta = ISASUtil.getValoreStringa(this.hashChiaveValore, CostantiSinssntW.CTS_STATO_RICHIESTA);
				accedereGriglia = (ISASUtil.valida(statoRichiesta) && !statoRichiesta.equals(RmRichiesteMMGEJB.STATO_RICH_MMG_ATTESA));
				logger.trace(punto + "statorichiesta>>"+ statoRichiesta+"< accedereGriglia>>"+accedereGriglia);
			}else if (richieste == 0){
				accedereGriglia = false;
			}
			logger.trace(punto + "accedereGriglia>>"+accedereGriglia);
//			if (richieste>=2){
			if(accedereGriglia){
				HashMap<String, Object> mapParam = new HashMap<String, Object>();
				if(formNeedApprovalForSave()){			
					executeApprovalForSaveBeforShowComponent(CTS_RICHIESTE_MMG,hIdUrizul.get(CTS_RICHIESTE_MMG),true,mapParam);
				}else{
					removeComponentsFrom(CTS_RICHIESTE_MMG);
					super.showComponent(CTS_RICHIESTE_MMG, hIdUrizul.get(CTS_RICHIESTE_MMG), mapParam);
				}
			}else{
				btn_RichiestaMMGForm();
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	private int getRichieste(){
		int ret = 0;
		Hashtable h = new Hashtable();
		h.put(CostantiSinssntW.N_CARTELLA, hashChiaveValore.get(CostantiSinssntW.N_CARTELLA).toString());
		RmRichiesteMMGEJB rmrichEJB = new RmRichiesteMMGEJB();
		try {
			Vector v = (Vector)invokeGenericSuEJB(rmrichEJB, h, "query_loadGridRichMMG");
			if (v!=null && v.size()>0){
				if (v.size() == 1 ){
					ISASRecord dbrRichiestaMMG = (ISASRecord)v.get(0);
					hashChiaveValore.put(CostantiSinssntW.N_CARTELLA, ISASUtil.getValoreStringa(dbrRichiestaMMG, CostantiSinssntW.N_CARTELLA));
					hashChiaveValore.put(CostantiSinssntW.CTS_ID_RICH, ISASUtil.getValoreStringa(dbrRichiestaMMG, CostantiSinssntW.CTS_ID_RICH));
					hashChiaveValore.put(CostantiSinssntW.CTS_STATO_RICHIESTA, ISASUtil.getValoreStringa(dbrRichiestaMMG, CostantiSinssntW.CTS_STATO_RICHIESTA));
				}
				ret = v.size();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	public void btn_RichiestaMMGForm() throws ISASMisuseException,Exception{
		HashMap<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put(CostantiSinssntW.N_CARTELLA, hashChiaveValore.get(CostantiSinssntW.N_CARTELLA).toString());
		mapParam.put(CostantiSinssntW.CTS_ID_RICH, ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.CTS_ID_RICH));
		if(formNeedApprovalForSave()){			
			executeApprovalForSaveBeforShowComponent(CTS_RICHIESTA_MMG,"/web/ui/sinssnt/segreteriaOrganizzativa/richiesta_mmg.zul",true,mapParam);
		}else{
			removeComponentsFrom(CTS_RICHIESTA_MMG);
			super.showComponent(CTS_RICHIESTA_MMG,"/web/ui/sinssnt/segreteriaOrganizzativa/richiesta_mmg.zul", mapParam);
		}
		
	}
	
	@Listen("onClick=#btn_intolleranzeAllergieForm")
	public void btn_intolleranzeAllergieForm() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_NOME_TABELLA, "RM_INTOLLERANZE_ALLERGIE");
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_STRING_N_CARTELLA, UtilForContainerGen.getCartellaCorr());
			mapParam.put(IntolleranzeAllergieGridCRUDCtrl.ARGS_ID_MY_WINDOW, CTS_INTOLLERANZE_ALLERGIE);
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_INTOLLERANZE_ALLERGIE,IntolleranzeAllergieGridCRUDCtrl.myPathFormZul,false,mapParam);
			}else{
				super.showComponent(CTS_INTOLLERANZE_ALLERGIE,IntolleranzeAllergieGridCRUDCtrl.myPathFormZul,mapParam);
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	@Listen("onClick=#btn_riepilogoAccessi")
	public void btn_riepilogoAccessi() {
		try{
			HashMap<String ,Object> map =new HashMap<String, Object>();
			
			map.put(CostantiSinssntW.N_CARTELLA,ISASUtil.getValoreStringa(this.hashChiaveValore, CostantiSinssntW.N_CARTELLA));
			map.put(CostantiSinssntW.CTS_PR_DATA_PUAC, ISASUtil.getValoreStringa(this.hashChiaveValore, CostantiSinssntW.CTS_PR_DATA_PUAC));
			
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI),false,map);
			}else{
				super.showComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_diarioForm")
	public void btn_diarioForm() {
		try {
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO),false,null);
			}else{
				super.showComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_medicoRelCliForm")
	public void btn_medicoRelCliForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_RELAZIONE_MEDICA,hIdUrizul.get(CTS_RELAZIONE_MEDICA),false,null);
			}else{
				super.showComponent(CTS_RELAZIONE_MEDICA,hIdUrizul.get(CTS_RELAZIONE_MEDICA));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_sntAutorizzazioneMMG")
	public void btn_sntAutorizzazioneMMG() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE,hIdUrizul.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE),false,null);
			}else{
				super.showComponent(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE,hIdUrizul.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_sntAutorizzazioneMMGADI")
	public void btn_sntAutorizzazioneMMGADI() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_AUTORIZZAZIONI_MMG_ADI,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADI),false,null);
			}else{
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADI,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADI));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_sntAutorizzazioneMMGADP")
	public void btn_sntAutorizzazioneMMGADP() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_AUTORIZZAZIONI_MMG_ADP,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADP),false,null);
			}else{
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADP,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADP));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_sntAutorizzazioneMMGADR")
	public void btn_sntAutorizzazioneMMGADR() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_AUTORIZZAZIONI_MMG_ADR,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADR),false,null);
			}else{
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADR,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADR));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}


	@Listen("onClick=#btn_sntAutorizzazioneMMGStorico")
	public void sntAutorizzazioneMMGStorico() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "N");		
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_AUTORIZZAZIONI_MMG_STORICO,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_STORICO),true,mapParam);
			}else{
				removeComponentsFrom(CTS_AUTORIZZAZIONI_MMG_STORICO);
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_STORICO,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_STORICO),mapParam);
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_cartellaForm")
	public void btn_cartellaForm() {	
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("cartellaForm",hIdUrizul.get("cartellaForm"),false,null);
			}else{
				super.showComponent("cartellaForm",hIdUrizul.get("cartellaForm"));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}





	private boolean checkMyValidity() {
		// TODO Auto-generated method stub
		return false;
	}




	@Listen("onClick=#btn_medicoAccessiForm")
	public void btn_medicoAccessiForm() {
		try{
			HashMap<String, Object> argForZul = new HashMap<String, Object>();
			argForZul.put("provAccessiPrestazioni", 1 + "");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("accessiPrestazioniForm",hIdUrizul.get("medicoAccessiForm"),true,argForZul);
			}else{
				removeComponentsFrom("accessiPrestazioniForm");
				super.showComponent("accessiPrestazioniForm",hIdUrizul.get("medicoAccessiForm"),argForZul);
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Override
	public void doRefreshOnSave(Component comp) {
		gestisciChiaviCorr(comp);
		gestisciTopMenu(comp);
		gestisciLeftMenu(comp);
	}

	@Override
	public void doRefreshOnDelete(Component comp) {
		if (comp != null && comp.getId() != null || !comp.getId().equals("")) {
			if (comp.getId().equals("cartellaForm")) {
				//Cancellazione della Cartella
				removeComponentsFrom(comp.getId());
				super.showComponent("fassiGrid", hIdUrizul.get("fassiGrid"));

			} else if (comp.getId().equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA)) {
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent("cartellaForm", hIdUrizul.get("cartellaForm"));
			} else if (comp.getId().equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO)) {
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO, hIdUrizul.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO));	
			} else if (comp.getId().equals(CostantiSinssntW.CTS_DIARIO)) {
				//Diario 
				removeComponentsFrom(comp.getId());
				super.showComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO));
			}else if(comp.getId().equals(CTS_RELAZIONE_MEDICA)){
				// Relazione medica  
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_RELAZIONE_MEDICA,hIdUrizul.get(CTS_RELAZIONE_MEDICA));
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI));
			}else if (comp.getId().equals(CTS_RELAZIONE_MEDICA)) {
				// Relazione medica  
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_RELAZIONE_MEDICA, hIdUrizul.get(CTS_RELAZIONE_MEDICA));
			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)) {
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE, hIdUrizul.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)) {
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADI, hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADI));

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)) {
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADP, hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADP));

			} else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)) {
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADR, hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADR));

			} 
			gestisciLeftMenu(comp);
		}
	}

	public void removeComponentsFrom(String idComp) {
		if (idComp != null && !idComp.equals("")) {
			if (idComp.equals("cartellaForm") || idComp.equals("fassiGrid")) {
				hashChiaveValore.remove("n_cartella");
				hashChiaveValore.remove("n_contatto");
				hashChiaveValore.remove("skm_data_apertura");
				super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADI));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADP));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADR));
			} else if (idComp.equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA) || idComp.equals(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO)) {
				hashChiaveValore.remove("n_contatto");
				hashChiaveValore.remove("skm_data_apertura");
				//super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));
				super.removeComponent(hashIdComponent.get(CTS_RICHIESTE_MMG));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADI));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADP));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADR));

			} else if (idComp.equals(CTS_RICHIESTE_MMG)) {
				super.removeComponent(hashIdComponent.get(CTS_RICHIESTE_MMG));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO));
			} else if (idComp.equals(CTS_RICHIESTA_MMG)) {
				super.removeComponent(hashIdComponent.get(CTS_RICHIESTA_MMG));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA));
				super.removeComponent(hashIdComponent.get(CTS_SCHEDA_SEGRETERIA_ORGANIZZATIVA_STORICO));
			}
		}
	}

	public boolean isSkValChiusa() {
		boolean isSkValChiusa = ISASUtil.getvaloreBoolean(hashChiaveValore, CTS_IS_SK_VAL_CHIUSA);

		if (isSkValChiusa) {
			isSkValChiusa = ISASUtil.getvaloreBoolean(hashChiaveValore, CTS_IS_SK_VAL_CHIUSA);
		}
		return isSkValChiusa;
	}

	public Boolean concludiCont() {
		//		TODO BOFFA SISTEMARE  
		boolean valore = true;
		//		reloadJFSkValContatti();
		//		// chiusura contatto corrente e apertura nuovo cont
		//		if (isFrameEsistente("JFrameSkMed"))
		//			closeFrame("JFrameSkMed");
		//		return apriJFSkMed();
		return valore;
	}
	
//	@Listen("onClick=#btn_accertamenti")
//	public void btn_accertamenti() {
//		try {
//			if(formNeedApprovalForSave()){			
//				executeApprovalForSaveBeforShowComponent("accertamenti",hIdUrizul.get("accertamenti"),false,null);
//			}else{
//				super.showComponent("accertamenti",hIdUrizul.get("accertamenti"));
//			}
//		}catch(Exception e){
//			doShowException(e);
//		}
//	}
	
		
	@Listen("onClick=#btn_svama")
	public void btn_svama() {
		try {
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("valutazioneSocialeGrid",hIdUrizul.get("valutazioneSocialeGrid"),false,null);
			}else{
				super.showComponent("valutazioneSocialeGrid", hIdUrizul.get("valutazioneSocialeGrid"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
}

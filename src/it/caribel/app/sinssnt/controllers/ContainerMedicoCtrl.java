package it.caribel.app.sinssnt.controllers;

import it.caribel.app.sinssnt.bean.RLPresaCaricoEJB;
import it.caribel.app.sinssnt.bean.SkValutazEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkmmgEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.controllers.autorizzazioni.AutorizzazioniMMGAdiFormCtrl;
import it.caribel.app.sinssnt.controllers.interfacce.IContainerMedicoCtrl;
import it.caribel.app.sinssnt.controllers.intolleranzeAllergie.IntolleranzeAllergieGridCRUDCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerLogout;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
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
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.util.ISASUtil;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
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

public class ContainerMedicoCtrl extends ContainerSinssntCtrl implements IContainerMedicoCtrl {
	private static final long serialVersionUID = -3133332801348919852L;
	public static final String myPathZul = "/web/ui/containerMedico.zul";
	//Hashtable<String, String> hIdUrizul = new Hashtable<String, String>();
	public static final String CTS_IS_SK_VAL_CHIUSA = "isSkValChiusa";
	public static final String CTS_RIEPILOGO_ACCESSI = "riepilogoAccessiForm";
	Tab menuLeftTabContatto;
	Tab menuLeftTabAutorizzazioni;
	
	private String CTS_CONTATTO_MEDICO = "sntContattoMedico";
	private String CTS_CONTATTO_MEDICO_STORICO = "sntContattoMedicoSto";
	private String CTS_RELAZIONE_MEDICA = "medicoRelCliForm";
	private String CTS_INTOLLERANZE_ALLERGIE = "intolleranzeAllergieForm";
	public static final String CTS_SEGNALAZIONI = "segnalazioniForm";
	private String CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE = "sntAutorizzazioneMMG";
	private String CTS_AUTORIZZAZIONI_MMG_STORICO = "sntAutorizzazioneMMGStorico";
	private String CTS_AUTORIZZAZIONI_MMG_ADI = "sntAutorizzazioneMMGADI";
	private String CTS_AUTORIZZAZIONI_MMG_ADP = "sntAutorizzazioneMMGADP";
	private String CTS_AUTORIZZAZIONI_MMG_ADR = "sntAutorizzazioneMMGADR";
	
	private boolean isConCtrlFlusSiad;
	private String CTS_FLUSSI_SIAD_FORM = "flussiSiadForm";
	private String CTS_FLUSSI_SIAD_STORICO_FORM = "flussiStoForm";
	private String CTS_PIANO_ASSISTENZIALE = "pianoAssistForm";
	private String ver = "8-";
	private String fonte = "";
	private boolean aprireSegnalazione = false;
	
	public ComponentInfo doBeforeCompose(Page page, Component parent,ComponentInfo compInfo){
		try{
			ISASUser iu =CaribelSessionManager.getInstance().getIsasUser();
			if(iu!=null && !iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_MEDICO)){
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
	
	protected void initContainer(){	
		super.initContainer();
		List<Component> listComp = UtilForComponents.getAllChildren(menu_left); 
		for (Component corrComp : listComp){
			if(corrComp instanceof ButtonMenuContainer){
				String idForm = ((ButtonMenuContainer)corrComp).getIdForm();
				String pathZulForm = ((ButtonMenuContainer)corrComp).getPathZulForm();
				hIdUrizul.put(idForm, pathZulForm);
				super.hashMenuLeft.put(idForm, ((ButtonMenuContainer)corrComp));
			}	
		}
	}

	public void doAfterCompose(Component comp) throws Exception {
		String punto = ver + "doAfterCompose ";
		super.doAfterCompose(comp);
		initContainer();
		Selectors.wireEventListeners(comp, this);//Per collegare gli eventi sul menu di destra dopo l'initContainer
		//Recupero il codice assistito passato dall'altro container
		String n_cartella = Executions.getCurrent().getParameter(CostantiSinssntW.N_CARTELLA);
		fonte = Executions.getCurrent().getParameter(CostantiSinssntW.CTS_FONTE);
		aprireSegnalazione = (ISASUtil.valida(fonte) && fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI+"")); 
		logger.debug(punto + " stampa >>"+fonte+"<<");
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
	}
	
	
	public void gestisciChiaviCorr(Component comp){
		String punto = ver + "gestisciChiaviCorr ";
		logger.debug(punto + " chiave>>"+(comp!=null? comp.getId()+"": " no dati ")+"<<");
		
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				Object n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawValue();
				if(n_cartella!=null)
					hashChiaveValore.put("n_cartella", n_cartella);
				Object cod_com_nasc = ((CaribelTextbox)comp.query("#cod_com_nasc")).getRawValue();
				if(cod_com_nasc!=null)
					hashChiaveValore.put(CostantiSinssntW.COD_COM_NASC, cod_com_nasc);
				Object desc_com_nasc = ((CaribelCombobox) comp.query("#desc_com_nasc")).getRawValue();
				if (desc_com_nasc != null)
					hashChiaveValore.put(Costanti.DESC_COM_NASC, desc_com_nasc);   

				Object dataNasc = ((CaribelDatebox)comp.query("#data_nasc")).getRawValue();
				if(dataNasc!=null)
					hashChiaveValore.put("data_nasc", dataNasc);
				Object dataCart = ((CaribelDatebox)comp.query("#data_apertura")).getRawValue();
				if(dataCart!=null)
					hashChiaveValore.put("data_apertura", dataCart);
				Object dataCartChius = ((CaribelDatebox)comp.query("#data_chiusura")).getRawValue();
				if(dataCartChius!=null)
					hashChiaveValore.put("data_chiusura", dataCartChius);
				Object codiceFiscale = ((CaribelTextbox)comp.query("#cod_fiscale")).getRawValue();
				if(codiceFiscale!=null)
					hashChiaveValore.put(Costanti.ASSISTITO_COD_FISC, codiceFiscale);				
			}else if(comp.getId().equals(CTS_CONTATTO_MEDICO)){
				Object n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawValue();
				Object id_skso = ((CaribelIntbox)comp.query("#id_skso")).getRawValue();
				Object skm_data_apertura = ((CaribelDatebox)comp.query("#skm_data_apertura")).getValueForIsas();
				Object skm_data_chiusura = ((CaribelDatebox)comp.query("#skm_data_chiusura")).getValueForIsas();
//				if(n_contatto!=null)
//					hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, n_contatto);
//				if(id_skso!=null)
//					hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO, id_skso);
				
				aggiungiValoreContainer(CostantiSinssntW.CTS_ID_SKSO, "#id_skso", comp);
				aggiungiValoreContainer(CostantiSinssntW.N_CONTATTO, "#n_contatto", comp);
				
				if(skm_data_apertura!=null){
					hashChiaveValore.put("skm_data_apertura", skm_data_apertura);
//					addChiaveValore(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, "#skf_data", comp);
				}
				aggiungiValoreContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, "#skm_data_apertura", comp);
				if(skm_data_chiusura!=null){
					hashChiaveValore.put("skm_data_chiusura", skm_data_chiusura);
//					addChiaveValore(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, "#skf_data_chiusura", comp);
				}
				aggiungiValoreContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, "#skm_data_chiusura", comp);
				aggiungiValoreContainer(CostantiSinssntW.COD_OPERATORE_REF, "#skm_medico", comp);

				
				hashChiaveValore.put(CostantiSinssntW.TIPO_OPERATORE, ManagerProfile.getTipoOperatore(getProfile()));
				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				logger.debug(punto + "diario ");
			}else if(comp.getId().equals(CTS_RELAZIONE_MEDICA)){
				logger.debug(punto + "RELAZIONE MEDICA");
			}else if(comp.getId().equals(CTS_INTOLLERANZE_ALLERGIE)){
				logger.debug(punto + "CTS_INTOLLERANZE_ALLERGIE ");
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				logger.debug(punto + "CTS_SEGNALAZIONI ");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)){
				logger.debug(punto + "CTS_SCHEDA_MMG_CORRENTE");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)){
				logger.debug(punto + "CTS_AUTORIZZAZIONI_MMG_ADI");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)){
				logger.debug(punto + "CTS_AUTORIZZAZIONI_MMG_ADP ");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)){
				logger.debug(punto + "CTS_AUTORIZZAZIONI_MMG_ADR ");
			}else if(comp.getId().equals("fassiGrid")){
				hashChiaveValore.clear();
			}
		}
	}
	
	public void gestisciTopMenu(Component comp){
		String punto = "gestisciTopMenu ";
		logger.trace(punto + " componente>>" +comp.getId()+"<<");
		
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				String n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawText();
				String cognome = ((CaribelTextbox)comp.query("#cognome")).getRawText();
				String nome = ((CaribelTextbox)comp.query("#nome")).getRawText();
				String desc_com_nasc = ((CaribelCombobox)comp.query("#desc_com_nasc")).getRawText();
				String data_nasc = ((CaribelDatebox)comp.query("#data_nasc")).getRawText();

				String testo = 	Labels.getLabel("caribelContainer.cartella.numero")+": "+
								n_cartella+" "+Labels.getLabel("caribelContainer.cartella.assistito")+" "+
								cognome +" "+
								nome+
								" "+Labels.getLabel("caribelContainer.cartella.data_nascita")+" "+data_nasc+
								" "+Labels.getLabel("caribelContainer.cartella.luogo_nascita")+" "+desc_com_nasc;	
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

			}else if(comp.getId().equals(CTS_CONTATTO_MEDICO)){
				String n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawText();
				String skm_data_apertura = ((CaribelDatebox)comp.query("#skm_data_apertura")).getRawText();
				String testo = "";

				if(n_contatto==null || n_contatto.equals(""))
					testo = Labels.getLabel("caribelContainer.contatto.in_fase_di_ins");
				else
					testo = Labels.getLabel("caribelContainer.contatto.numero")+": "+n_contatto+
							" "+Labels.getLabel("caribelContainer.contatto.data_apertura")+" "+skm_data_apertura;
				String skmDataChiusura = ((CaribelDatebox)comp.query("#skm_data_chiusura")).getRawText();
				if(ManagerDate.validaData(skmDataChiusura)){
					testo+=" " +Labels.getLabel("contatto.medico.anamnesi.data.chiusura")+": "+ skmDataChiusura;
				}
				
				addItemMenuTop(comp,testo, (ManagerDate.validaData(skmDataChiusura) ? CostantiSinssntW.CTS_ROW_RED:""));
				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				logger.debug(punto + " diario>>");
			}else if(comp.getId().equals(CTS_RELAZIONE_MEDICA)){
				logger.debug(punto + " relazione medica ");
				
			}else if(comp.getId().equals(CTS_INTOLLERANZE_ALLERGIE)){
				logger.debug(punto + " intolleranza allergie ");
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				logger.debug(punto + " CTS_SEGNALAZIONI ");	
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)){
				logger.debug(punto + "\n TODO \n CTS_AUTORIZZAZIONI_MMG_ADI ");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)){
				logger.debug(punto + "\n TODO \n CTS_AUTORIZZAZIONI_MMG_ADP ");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)){
				logger.debug(punto + "\n TODO \n CTS_AUTORIZZAZIONI_MMG_ADR ");
				
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)){
				logger.debug(punto + " CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE ");
				
//				String n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawText();
				String pr_dataMMg = ((CaribelDatebox)comp.query("#pr_data")).getRawText();
				String operatore = ((CaribelTextbox)comp.query("#desc_oper")).getRawText();
				String segnalante = ((CaribelCombobox)comp.query("#cbx_skmmg_segnalatore")).getRawText();
				String patologia =((CaribelCombobox)comp.query("#desc_patol1")).getRawText();
				String testo = "";
				
				testo = Labels.getLabel("caribelContainer.autorizzazionemmg.scheda.mmg")+ pr_dataMMg + 
						Labels.getLabel("caribelContainer.autorizzazionemmg.operatore") + operatore + 
						Labels.getLabel("caribelContainer.autorizzazionemmg.segnalante")+ segnalante +
		                Labels.getLabel("caribelContainer.autorizzazionemmg.patologia") + patologia;
				
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
				
				addItemMenuTop(comp,testo);
				
			}
		}
	}
	
	public void gestisciLeftMenu(Component comp){
		String punto = "3-gestisciLeftMenu ";
		logger.debug(punto + " comp>"+comp.getId()+"<");
		//Disabilito tutto
		disableAllButtonMenuLeft(true);
		
		//Abilito pulsanti sempre attivi
		//disableButtonMenuLeft("esci",false);
		//disableButtonMenuLeft("fassiGrid",false);
		String nCartella = ISASUtil.getValoreStringa(hashChiaveValore,CostantiSinssntW.N_CARTELLA);
		if(ISASUtil.valida(nCartella)){
			//Abilito Contatto
			disableButtonMenuLeft(CTS_CONTATTO_MEDICO,false);
			//Abilito Scale di valutazione
			disableButtonMenuLeft("smScaleGrid",false); 
			//Abilito Storico contatti
			disableButtonMenuLeft(CTS_CONTATTO_MEDICO_STORICO,false);
			//Abilito il bottone di AsterView
			disableButtonMenuLeft("asterview", !conAsterView);
			//Abilito il bottone dei documenti
			disableButtonMenuLeft("documenti", false);
		}
		if(hashChiaveValore.get(CostantiSinssntW.N_CONTATTO)!=null && !hashChiaveValore.get(CostantiSinssntW.N_CONTATTO).toString().equals("0")){
			menuLeftTabContatto.setSelected(true);
			//abilita accessi
			disableButtonMenuLeft("medicoAccessiForm",false); 
			//Abilito Relazione medica
			disableButtonMenuLeft(CTS_RELAZIONE_MEDICA,false);
			//Abilito Inolleranze Allergie
			disableButtonMenuLeft(CTS_INTOLLERANZE_ALLERGIE,false);
			//Abilito Segnalazioni
			disableButtonMenuLeft(CTS_SEGNALAZIONI,false);
			//Abilito Diario
			disableButtonMenuLeft(CostantiSinssntW.CTS_DIARIO , false);

			disableButtonMenuLeft(CTS_FLUSSI_SIAD_FORM,false);
			disableButtonMenuLeft(CTS_FLUSSI_SIAD_STORICO_FORM, false);
			disableButtonMenuLeft(CTS_PIANO_ASSISTENZIALE,false);
			
			//Abilito Relazione medica
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE,false);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_STORICO,false);
			disableButtonMenuLeft(CTS_RIEPILOGO_ACCESSI,false);
			gestioneAutorizzazione();
			
			checkEsistePianoAssist();
//			faiMsgNoGestFlussi(comp);	
		}
		if(comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)|| comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_STORICO)
				|| comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI) 
				|| comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)
				|| comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)){
			menuLeftTabAutorizzazioni.setSelected(true);
			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_STORICO,false);
//			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADI,false);
//			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADP,false);
//			disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADR,false);
			if(!AutorizzazioniMMGAdiFormCtrl.isAutorizzazioneReadOnly()){
				gestioneAutorizzazione();
			}else {
				logger.trace(punto + " autorizzazioni non sono in sola lettura>"+ comp.getId()+"<<");
				if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)){
					Component autorizzazioneADi = self.getFellowIfAny("sntAutorizzazioneMMGADI", true);
					if(autorizzazioneADi!=null){
						logger.trace(punto + " setto READONLY ");
						UtilForBinding.setComponentReadOnly(autorizzazioneADi,true); 
					}else {
						logger.trace(punto + " componente non recuperato ");
					}
				}else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)){
					Component autorizzazioneADp = self.getFellowIfAny("sntAutorizzazioneMMGADP", true);
					if(autorizzazioneADp!=null){
						logger.trace(punto + " setto READONLY ");
						UtilForBinding.setComponentReadOnly(autorizzazioneADp,true); 
					}else {
						logger.trace(punto + " componente non recuperato ");
					}
				}else if (comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)){
					Component autorizzazioneADr = self.getFellowIfAny("sntAutorizzazioneMMGADR", true);
					if(autorizzazioneADr!=null){
						logger.trace(punto + " setto READONLY ");
						UtilForBinding.setComponentReadOnly(autorizzazioneADr,true); 
					}else {
						logger.trace(punto + " componente non recuperato ");
					}
				}
			}
		}
		gestisciPersonalizzazioni();
	}

	public void gestioneAutorizzazione() {
		String punto = ver + "gestioneAutorizzazione ";
		if (possoInserireAutorizzazioni()){
			logger.trace(punto + " posso inserire autorizzazioni ");
			String tipoCura = recuperaTipoCura();
			if (ISASUtil.valida(tipoCura)){
				if (tipoCura.equals(CostantiSinssntW.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
					disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADI,false);
				}else{
					disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADP,false);
					disableButtonMenuLeft(CTS_AUTORIZZAZIONI_MMG_ADR,false);
				}
			}
		}
	}
	
	
	private String recuperaTipoCura() {
		String punto = ver + "recuperaTipoCura ";
		String tipoCura ="";
		ISASRecord dbrRmSkso = AutorizzazioniMMGAdiFormCtrl.recuperaSkSo();
		tipoCura = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_TIPOCURA);
		logger.trace(punto + " tipo cura>>"+ tipoCura+"<<");
		
		return tipoCura;
	}

	private boolean possoInserireAutorizzazioni() {
		String punto = ver  + "possoInserireAutorizzazioni ";
		boolean possoInserireAutorizzazioni = controlloPossoInserireAutorizzazioni();

		if(possoInserireAutorizzazioni){
			logger.trace(punto + " verifico se + stato inserito MMG TRA LE FIGURE COINVOLTE ");
			ISASRecord dbrOpCoinvolti = AutorizzazioniMMGAdiFormCtrl.recuperaSkSoOpCoinvolti();
			possoInserireAutorizzazioni = (dbrOpCoinvolti!=null);
			logger.trace(punto + " verifico se + stato inserito MMG TRA LE FIGURE COINVOLTE>> "+ possoInserireAutorizzazioni);
		}
		return possoInserireAutorizzazioni;
	}

	
	private boolean controlloPossoInserireAutorizzazioni() {
		String punto = ver + "controlloPossoInserireAutorizzazioni ";
		boolean possoInserireAutorizzazioni = false;
		String nCartella = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CARTELLA) + "";
		String nContatto = UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.N_CONTATTO) + "";
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.put(CostantiSinssntW.N_CARTELLA, nCartella);
		dati.put(CostantiSinssntW.N_CONTATTO, nContatto);
		ISASRecord dbrRmSkso = recuperaSchedaSkSo(dati);
		// int idSkSo = ISASUtil.getValoreIntero(hashChiaveValore,
		// CostantiSinssntW.CTS_ID_SKSO);
		String dataPresaCaricoSkso = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_DATA_PRESA_CARICO_SKSO);
		String dataChiusura = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.PR_DATA_CHIUSURA);
		possoInserireAutorizzazioni = ManagerDate.validaData(dataPresaCaricoSkso);

		hashChiaveValore.put(CostantiSinssntW.AUTORIZZAZIONE_READONLY,(ManagerDate.validaData(dataChiusura)?CostantiSinssntW.CTS_SI:"N"));
		
		logger.trace(punto + " dataPresaCaricoSkso>" + dataPresaCaricoSkso + "< dataChiusura>>" + dataChiusura + "<<");
		return possoInserireAutorizzazioni;
	}

	private ISASRecord recuperaSchedaSkSo(Hashtable<String, String> dati) {
		ISASRecord dbrRmSkso =null;
		
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		dbrRmSkso =(ISASRecord) rmSkSOEJB.recuperaRmSkSo(CaribelSessionManager.getInstance().getMyLogin(), dati);
				
		return dbrRmSkso;
	}

	private void gestisciPersonalizzazioni() {
		if (ManagerProfile.isConfigurazioneMarche(getProfile())){
			hashMenuLeft.get("flussiStoForm").setVisible(false);
			hashMenuLeft.get("flussiSiadForm").setVisible(false);
			hashMenuLeft.get("pianoAssistForm").setVisible(false);
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
	

	private void gestisciApertureAutomatiche(Component comp){
		String punto =  "3-gestisciApertureAutomatiche ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;   
			
			if(comp.getId().equals("cartellaForm") ){
				//Ho aperto una Cartella, allora provo ad aprire anche il relativo Contatto, se non e' stato ancora aperto
				String n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawText();	
				if(super.hashIdComponent.get(CTS_CONTATTO_MEDICO)==null &&  n_cartella!=null && !n_cartella.equals("")){
					recuperaDaSchedaValutazione(n_cartella);
					HashMap<String, Object> mapParam = new HashMap<String, Object>();
					mapParam.put("n_cartella", n_cartella);
					Component sntContattoMedico = createComponent(hIdUrizul.get(CTS_CONTATTO_MEDICO), mapParam);
					showComponent(sntContattoMedico);
				}
				if( n_cartella!=null && !n_cartella.equals("")){
					recuperagetLastSkMMG(n_cartella);
				}
			} else if(comp.getId().equals(CTS_CONTATTO_MEDICO) && aprireSegnalazione){
				logger.trace(punto + " dati CTS_CONTATTO_MEDICO");
				aprireSegnalazione = false;
				String nCartella = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA);
				HashMap<String, Object> mapParam = new HashMap<String, Object>();
				mapParam.put(CostantiSinssntW.N_CARTELLA, nCartella);
//				mapParam.put(SegnalazioneGridCRUDCtrl.CTS_POSSO_MODIFICARE_TUTTE_SEGNALAZIONI, new Boolean(true));
//				hashChiaveValore.put(SegnalazioneGridCRUDCtrl.CTS_POSSO_MODIFICARE_TUTTE_SEGNALAZIONI, new Boolean(true));
				Component sntSegnalazioni = createComponent(hIdUrizul.get(CTS_SEGNALAZIONI), mapParam);
				showComponent(sntSegnalazioni);
			}else if(comp.getId().equals(CTS_RELAZIONE_MEDICA)){
				logger.trace(punto + " dati CTS_RELAZIONE_MEDICA");
			}else if(comp.getId().equals(CTS_INTOLLERANZE_ALLERGIE)){
				logger.trace(punto + " dati CTS_INTOLLERANZE_ALLERGIE");
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				logger.trace(punto + " dati CTS_SEGNALAZIONI ");	
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)){
				logger.trace(punto + " dati CTS_AUTORIZZAZIONI_MMG_ADI");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)){
				logger.trace(punto + " dati CTS_AUTORIZZAZIONI_MMG_ADP");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)){
				logger.trace(punto + " dati CTS_AUTORIZZAZIONI_MMG_ADR ");
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)){
				logger.trace(punto + " dati CTS_SCHEDA_MMG_CORRENTE");
				String n_cartella = ((CaribelTextbox)comp.query("#n_cartella")).getRawText();
				if( n_cartella!=null && !n_cartella.equals("")){
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
			Hashtable<String, String> datiRicevuti = skmmgEJB.getLastSkMMG(CaribelSessionManager.getInstance().getMyLogin(),dati);
			/* pr_data autorizzazione non è uguale alla pr_data della valutazione */
			hashChiaveValore.put(CostantiSinssntW.MMGPRPR_DATA, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.MMGPRPR_DATA));
			hashChiaveValore.put(CostantiSinssntW.STATO, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.STATO));
						
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (CariException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		}
	}
	
	private void recuperaDaSchedaValutazione(String n_cartella) {
		/*  Carica i dati della scheda di valutazione */
		SkValutazEJB skValutazEJB = new SkValutazEJB();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		try {
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
			Hashtable<String, String> datiRicevuti = skValutazEJB.query_esisteSkValAttiva(CaribelSessionManager.getInstance().getMyLogin(),
					dati);
  
			if(datiRicevuti.containsKey(CostantiSinssntW.PR_DATA)){
				hashChiaveValore.put(CostantiSinssntW.PR_DATA, ISASUtil.getValoreStringa(datiRicevuti, CostantiSinssntW.PR_DATA));
			}
			hashChiaveValore.put("desc_val_ap", ISASUtil.getValoreStringa(datiRicevuti, "desc_val_ap"));
			String prDataCarico=ISASUtil.getValoreStringa(datiRicevuti, "pr_data_carico"); 
			if (ISASUtil.valida(prDataCarico)){
				hashChiaveValore.put("pr_data_carico", prDataCarico);
			}
			String prDataValutaz = ISASUtil.getValoreStringa(datiRicevuti, "pr_data_valutaz");
			if (ISASUtil.valida(prDataValutaz)){
				hashChiaveValore.put("pr_data_valutaz", prDataValutaz);
			}
			
			RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
			ISASRecord dbrRmSkso = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(), dati);
			String idSkso = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_ID_SKSO);
			String dataPresaCarico = ISASUtil.getValoreStringa(dbrRmSkso, CostantiSinssntW.CTS_DATA_PRESA_CARICO_SKSO);
			hashChiaveValore.put(CostantiSinssntW.CTS_ID_SKSO, idSkso);
			hashChiaveValore.put(CostantiSinssntW.CTS_DATA_PRESA_CARICO_SKSO, dataPresaCarico);
			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (CariException e) {
			e.printStackTrace();
		}
	}

	private void gestisciRefreshGrid(Component comp){
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
		}
	}
	
	@Override
	public void onShowComponent(Event evt) {
		String punto = ver + "onShowComponent ";
		Component comp = ((Component)evt.getData());
		logger.trace(punto + " chiave>>"+(comp!=null? comp.getId()+"": " no dati ")+"<<");
		gestisciChiaviCorr(comp);
		gestisciTopMenu(comp);
		gestisciApertureAutomatiche(comp);
		gestisciRefreshGrid(comp);
		gestisciLeftMenu(comp);
	}

	@Listen("onClick=#btn_sntContattoMedico")
	public void btn_sntContattoMedico() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_CONTATTO_MEDICO,hIdUrizul.get(CTS_CONTATTO_MEDICO),true,null);
			}else{
				removeComponentsFrom(CTS_CONTATTO_MEDICO);
				super.showComponent(CTS_CONTATTO_MEDICO,hIdUrizul.get(CTS_CONTATTO_MEDICO));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	@Listen("onClick=#btn_riepilogoAccessiForm")
	public void btn_riepilogoAccessiForm() {
		String punto = ver + "btn_riepilogoAccessiForm ";
		logger.trace(punto + " inizio ");
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI),false,null);
			}else{
				removeComponentsFrom(CTS_RIEPILOGO_ACCESSI);
				super.showComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_diarioForm")
	public void btn_diarioForm() {		
		try{
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
	
	@Listen("onClick=#btn_sntContattoMedicoSto")
	public void btn_sntContattoMedicoSto() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "N");		
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent(CTS_CONTATTO_MEDICO_STORICO,hIdUrizul.get(CTS_CONTATTO_MEDICO_STORICO),true,mapParam);
			}else{
				removeComponentsFrom(CTS_CONTATTO_MEDICO_STORICO);
				super.showComponent(CTS_CONTATTO_MEDICO_STORICO,hIdUrizul.get(CTS_CONTATTO_MEDICO_STORICO),mapParam);
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
	
	@Listen("onClick=#btn_smScaleGrid")
	public void btn_smScaleValutazione() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("smScaleGrid",hIdUrizul.get("smScaleGrid"),false,null);
			}else{
				super.showComponent("smScaleGrid",hIdUrizul.get("smScaleGrid"));
			}		
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_flussiSiadForm")
	public void btn_flussiSiadForm() {
		try{
			if (caricaCaso()){			
				if(formNeedApprovalForSave()){			
					executeApprovalForSaveBeforShowComponent(CTS_FLUSSI_SIAD_FORM,hIdUrizul.get(CTS_FLUSSI_SIAD_FORM),true,null);
				}else{
					removeComponentsFrom(CTS_FLUSSI_SIAD_FORM);
					super.showComponent(CTS_FLUSSI_SIAD_FORM,hIdUrizul.get(CTS_FLUSSI_SIAD_FORM));
				}
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
		
	   private boolean caricaCaso() throws Exception
	   {
	       
	       // 05/04/13 ---

	       Hashtable h_skval = (Hashtable)hashChiaveValore.clone();
	       h_skval.put("tipouvg","1");

	       Hashtable h_caso = new Hashtable();
	       h_caso.put(CostantiSinssntW.N_CARTELLA,h_skval.get(CostantiSinssntW.N_CARTELLA));

	       if (h_skval.containsKey(CostantiSinssntW.PR_DATA) && h_skval.get(CostantiSinssntW.PR_DATA)!= null)
	           h_caso.put(CostantiSinssntW.PR_DATA, h_skval.get(CostantiSinssntW.PR_DATA));
	      
	     
	       h_caso.put("chiamante", CostantiSinssntW.CASO_SAN);
	       h_caso.put("tipouvg","1");

	       
	       Hashtable nuovoCaso = null;
	       Hashtable datiCaso = (Hashtable)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_caso, "selCaso");

	       if ((datiCaso != null) && (datiCaso.size() > 0)) {
	           if (datiCaso.get("id_caso") != null)
	               hashChiaveValore.put("id_caso", ((Integer)datiCaso.get("id_caso")).toString());

	           if (datiCaso.get("dt_conclusione") != null)
	        	   hashChiaveValore.put("dt_conclusione", datiCaso.get("dt_conclusione").toString());

	           if (datiCaso.get("dt_segnalazione") != null)
	        	   hashChiaveValore.put("dt_segnalazione", datiCaso.get("dt_segnalazione").toString());

	           if (datiCaso.get("pr_data") != null) {
	               String dtSkVal = datiCaso.get("pr_data").toString();               
	               hashChiaveValore.put("pr_data", dtSkVal);             
	           }
	          
	       } else
	       {
	           nuovoCaso = (Hashtable)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_caso, "creaCaso");

	           if ((nuovoCaso != null) && (nuovoCaso.size() > 0))
	           {
	               if (nuovoCaso.get("id_caso") != null)
	            	   hashChiaveValore.put("id_caso", ((Integer)nuovoCaso.get("id_caso")).toString());

	               if (nuovoCaso.get("dt_conclusione") != null)
	            	   hashChiaveValore.put("dt_conclusione", nuovoCaso.get("dt_conclusione").toString());

	               if (nuovoCaso.get("dt_segnalazione") != null)
	            	   hashChiaveValore.put("dt_segnalazione", nuovoCaso.get("dt_segnalazione").toString());

	      
	           if (nuovoCaso.get("pr_data") != null) {
	               String dtSkVal = nuovoCaso.get("pr_data").toString();
	               hashChiaveValore.put("pr_data", dtSkVal);
	           }
	       
	              logger.debug("JCariContainerINFCartella.caricaCaso(): CASO creato da zero!!!");
	           }else
	           {
	        	   logger.debug("JCariContainerINFCartella.caricaCaso(): creazione nuovo CASO non riuscita!!!");
	               return false;
	           }
	       }
	       return true;
	   }
   private boolean checkMyValidity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Listen("onClick=#btn_flussiStoForm")
	public void btn_flussiStoForm() {
		try {
			if (caricaCaso()){
				if(formNeedApprovalForSave()){			
					executeApprovalForSaveBeforShowComponent("flussiSiadForm",hIdUrizul.get("flussiSiadForm"),true,null);
				}else{
					removeComponentsFrom("flussiSiadForm");
					super.showComponent("flussiSiadForm",hIdUrizul.get("flussiSiadForm"));
				}
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_medicoAccessiForm")
	public void btn_medicoAccessiForm() {
		try{
			HashMap <String, Object> argForZul = new HashMap<String, Object>();
			argForZul.put("provAccessiPrestazioni", 1+"");
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
	
	@Listen("onClick=#btn_pianoAssistForm")
	public void btn_pianoAssistForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("pianoAssistForm",hIdUrizul.get("pianoAssistForm"),true,null);
			}else{
				removeComponentsFrom("pianoAssistForm");
				super.showComponent("pianoAssistForm",hIdUrizul.get("pianoAssistForm"));
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
		if(comp!=null && comp.getId()!=null || !comp.getId().equals("")){
			if(comp.getId().equals("cartellaForm")){
				//Cancellazione della Cartella
				removeComponentsFrom(comp.getId());
				super.showComponent("fassiGrid",hIdUrizul.get("fassiGrid"));
			}else if(comp.getId().equals(CTS_CONTATTO_MEDICO)){
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent("cartellaForm",hIdUrizul.get("cartellaForm"));
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI));
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				//Diario 
				removeComponentsFrom(comp.getId());
				super.showComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO));
			}else if(comp.getId().equals(CTS_RELAZIONE_MEDICA)){
				// Relazione medica  
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_RELAZIONE_MEDICA,hIdUrizul.get(CTS_RELAZIONE_MEDICA));
			}else if(comp.getId().equals(CTS_INTOLLERANZE_ALLERGIE)){
				// Relazione medica  
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_INTOLLERANZE_ALLERGIE,hIdUrizul.get(CTS_INTOLLERANZE_ALLERGIE));
			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE,hIdUrizul.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));

			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADI)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADI,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADI));

			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADP)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADP,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADP));

			}else if(comp.getId().equals(CTS_AUTORIZZAZIONI_MMG_ADR)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_AUTORIZZAZIONI_MMG_ADR,hIdUrizul.get(CTS_AUTORIZZAZIONI_MMG_ADR));
			}else if(comp.getId().equals(CTS_RIEPILOGO_ACCESSI)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_RIEPILOGO_ACCESSI,hIdUrizul.get(CTS_RIEPILOGO_ACCESSI));
			}
			
			gestisciLeftMenu(comp);
		}
	}
	
	
	public void removeComponentsFrom(String idComp){
		if(idComp!=null && !idComp.equals("")){
			if(idComp.equals("cartellaForm") || idComp.equals("fassiGrid")){
				hashChiaveValore.remove("n_cartella");
				hashChiaveValore.remove("n_contatto");
				hashChiaveValore.remove(CostantiSinssntW.CTS_ID_SKSO);
				hashChiaveValore.remove("skm_data_apertura");
				super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get(CTS_CONTATTO_MEDICO));
				super.removeComponent(hashIdComponent.get(CostantiSinssntW.CTS_DIARIO));
				super.removeComponent(hashIdComponent.get(CTS_RELAZIONE_MEDICA));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
				super.removeComponent(hashIdComponent.get(CTS_CONTATTO_MEDICO_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADI));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADP));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADR));
				
			}else if(idComp.equals(CTS_CONTATTO_MEDICO) || idComp.equals(CTS_CONTATTO_MEDICO_STORICO)){
				hashChiaveValore.remove("n_contatto");
				hashChiaveValore.remove(CostantiSinssntW.CTS_ID_SKSO);
				hashChiaveValore.remove("skm_data_apertura");
				//super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get(CTS_CONTATTO_MEDICO));
				super.removeComponent(hashIdComponent.get(CostantiSinssntW.CTS_DIARIO));
				super.removeComponent(hashIdComponent.get(CTS_RELAZIONE_MEDICA));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_CONTATTO_MEDICO_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_SCHEDA_MMG_CORRENTE));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_STORICO));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADI));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADP));
				super.removeComponent(hashIdComponent.get(CTS_AUTORIZZAZIONI_MMG_ADR));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
			}else if (idComp.equals(CTS_RIEPILOGO_ACCESSI)){
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
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
}

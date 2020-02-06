package it.caribel.app.sinssnt.controllers;

import it.caribel.app.sinssnt.bean.MotivoSEJB;
import it.caribel.app.sinssnt.bean.RLPresaCaricoEJB;
import it.caribel.app.sinssnt.bean.SkValutazEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.controllers.interfacce.IContainerInfermieristicoCtrl;
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
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.util.ISASUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;

public class ContainerInfermieristicoCtrl  extends ContainerSinssntCtrl implements IContainerInfermieristicoCtrl{

	private static final long serialVersionUID = 6356060650240007911L;

	public static final String myPathZul = "/web/ui/containerInfermieristico.zul";

	private String CTS_INTOLLERANZE_ALLERGIE = "intolleranzeAllergieForm";
	public static final String CTS_SEGNALAZIONI = ContainerMedicoCtrl.CTS_SEGNALAZIONI;
	public static final String CTS_RIEPILOGO_ACCESSI = "riepilogoAccessiForm";
	private String fonte = "";
	private boolean aprireSegnalazione = false;

	//Hashtable<String, String> hIdUrizul = new Hashtable<String, String>();
	
	Tab menuLeftTabContatto;

//	private boolean conPuaUvm;

//	private boolean multiCont;

	private boolean isConCtrlFlusSiad;

	private String codMotivo_xFlussiSiad;

	private boolean conPuaUvm;

	private boolean multiCont;

	private boolean isMotxFlussi;

	private boolean existsPresaCar;

	private static final String ver ="3-";

	public ComponentInfo doBeforeCompose(Page page, Component parent,ComponentInfo compInfo){
		try{
//			urlAsterView = getProfile().getStringFromProfile("url_asterview");
//			this.conAsterView  = ((urlAsterView != null) && (!urlAsterView.trim().equals(""))
//	                && (!urlAsterView.trim().equals("NO"))
//	                && (urlAsterView.indexOf(".") > 0));
			
			ISASUser iu =CaribelSessionManager.getInstance().getIsasUser();
			if(iu!=null && !iu.canIUse(ChiaviISASSinssntWeb.SKINF)){
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
		
		this.conPuaUvm = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABIL_SKVALPUAUVM);// ((abilPuaUvm != null) && (abilPuaUvm.trim().equals("SI")));
        this.multiCont = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ABIL_NEWCONT_INF);//((abilMultiCont != null) && (abilMultiCont.trim().equals("SI")));
		
        // Simone: inizializzo parametri per controllo flussi
		isConCtrlFlusSiad = getProfile().getStringFromProfile("ctrl_skinf_siad").equals(CostantiSinssntW.SI);
		if (isConCtrlFlusSiad) {
			MotivoSEJB motivoEjb = new MotivoSEJB();
			try {
				this.codMotivo_xFlussiSiad = motivoEjb.getCodMotivoSxFlussi(
						CaribelSessionManager.getInstance().getMyLogin(),
						(Hashtable<String,String>) null);
			} catch (Exception e) {
				e.printStackTrace();
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
//		UtilForContainer.restartContainerFromFassi();
		
		UtilForContainer.refreshToolBar();
		if(Executions.getCurrent().getSession().hasAttribute(CostantiSinssntW.FIRST_ACCESS)){
			UtilForUI.doTestVersionBrowser();
			Executions.getCurrent().getSession().removeAttribute(CostantiSinssntW.FIRST_ACCESS);
		}
	}
	
	public void gestisciChiaviCorr(Component comp){
		String punto = "gestisciChiaviCorr ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				Object n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawValue();
				if(n_cartella!=null)
					hashChiaveValore.put(CostantiSinssntW.N_CARTELLA, n_cartella);
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
			}else if(comp.getId().equals("contattoInfForm")){
//				addChiaveValore(CostantiSinssntW.N_CONTATTO, "#n_contatto", comp);
//				addChiaveValore(CostantiSinssntW.CTS_ID_SKSO, "#id_skso", comp);
				addChiaveValore(CostantiSinssntW.SKI_DATA_APERTURA, "#ski_data_apertura", comp);
				
				aggiungiValoreContainer(CostantiSinssntW.CTS_ID_SKSO, "#id_skso", comp);
				aggiungiValoreContainer(CostantiSinssntW.N_CONTATTO, "#n_contatto", comp);
				
				aggiungiValoreContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_INIZIO, "#ski_data_apertura", comp);
				aggiungiValoreContainer(CostantiSinssntW.CTS_PERIODO_CONTATTO_DATA_FINE, "#ski_data_uscita", comp);
				aggiungiValoreContainer(CostantiSinssntW.COD_OPERATORE_REF, "#ski_infermiere", comp);

				
//				addChiaveValore(CostantiSinssntW.SKI_DTSEGNALAZIONE, "#ski_dtsegnalazione", comp);
//				addChiaveValore(CostantiSinssntW.SKI_DTPRESACARICO, "#ski_dtpresacarico", comp);
//				addChiaveValore(CostantiSinssntW.SKI_DATA_USCITA, "#ski_data_uscita", comp);
				hashChiaveValore.put(CostantiSinssntW.TIPO_OPERATORE, GestTpOp.CTS_COD_INFERMIERE);

//				h_dati.put("cod_operatore",cod_operatore);
//				h_dati.put("cognome_operatore",cognome_operatore);
//				h_dati.put("n_cartella", numCart);
//				h_dati.put("n_contatto", numCont);
//				h_dati.put("pr_data", dtSkVal);// 26/10/06
//				h_dati.put("ski_data_apertura", dtApertura);
//				h_dati.put("ski_dtsegnalazione", dtSegnalaz);//29/06/10
//				h_dati.put("ski_dtpresacarico", dtPreInCar);//29/06/10
//				h_dati.put("ski_data_uscita", dtChiusura);

//				TODO vfr
//				msgxCons = " - Chiuso il: " + dtChiusura;
				
//				h_dati.put("stato", new Integer(this.stato));
//				h_dati.put("isContChiuso", new Boolean(isContattoInfChiuso()));

//				TODO vfr
//				// 26/04/06 m ---
//				String nomeJF = this.getClass().getName();
//				if ((isContattoInfChiuso()) || (contAperti)) // 14/04/08
//					nomeJF = this.myContainer.getNome2ndFrame(nomeJF);
//				h_dati.put("parMtdDaInvocare", new Object[]{nomeJF});
				
				// 26/04/06 m ---
//				h_dati.put("tipo_operatore", "02");
				
//				Object n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawValue();
//				Object ski_data_apertura = ((CaribelDatebox)comp.query("#ski_data_apertura")).getRawValue();
//				Object ski_data_uscita = ((CaribelDatebox)comp.query("#ski_data_uscita")).getRawValue();
//				Object pr_data = ((CaribelDatebox)comp.query("#pr_data")).getRawValue();
//				
//				Object motivo = ((CaribelCombobox)comp.query("#cbx_motivo")).getSelectedValue();
//				
//				if(n_contatto!=null)
//					hashChiaveValore.put(CostantiSinssntW.N_CONTATTO, n_contatto);
//				if(ski_data_apertura!=null)
//					hashChiaveValore.put("ski_data_apertura", ski_data_apertura);
//				if(ski_data_uscita!=null)
//					hashChiaveValore.put("ski_data_uscita", ski_data_uscita);
//				if(pr_data!=null)
//					hashChiaveValore.put("pr_data", pr_data);
//				if(motivo!=null)
//					hashChiaveValore.put("motivo", motivo);
				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				logger.debug(punto + "diario ");
			}else if(comp.getId().equals("fassiGrid")){
				hashChiaveValore.clear();
			}
		}
	}

	public void gestisciTopMenu(Component comp){
		String punto = "gestisciTopMenu ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				String n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawText();
				String cognome = ((CaribelTextbox)comp.query("#cognome")).getRawText();
				String nome = ((CaribelTextbox)comp.query("#nome")).getRawText();
				String desc_com_nasc = ((CaribelCombobox)comp.query("#desc_com_nasc")).getRawText();
				String data_nasc = ((CaribelDatebox)comp.query("#data_nasc")).getRawText();
				String dataApertura =((CaribelDatebox)comp.query("#data_apertura")).getRawText();
				
				String testo = 	Labels.getLabel("caribelContainer.cartella.numero")+": "+
								n_cartella+" "+Labels.getLabel("caribelContainer.cartella.assistito")+" "+
								cognome +" "+
								nome+
								" "+Labels.getLabel("caribelContainer.cartella.data_nascita")+" "+data_nasc+
								" "+Labels.getLabel("caribelContainer.cartella.luogo_nascita")+" "+desc_com_nasc;
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
				
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_COGNOME, cognome);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_NOME, nome);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_CARTELLA_APERTA, dataApertura);
				hashChiaveValore.put(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA, dataChiusura);
				if (desc_com_nasc != null){
					hashChiaveValore.put("desc_com_nasc", desc_com_nasc);
				}
				
				testo = gestioneChiusuraCartella(testo, dataChiusura, valoreMotivoChiusura);
				addItemMenuTop(comp,testo, (ManagerDate.validaData(dataChiusura) ? CostantiSinssntW.CTS_ROW_RED:"")); 
			
			}else if(comp.getId().equals("contattoInfForm")){
				String n_contatto = ((CaribelIntbox)comp.query("#n_contatto")).getRawText();
				String ski_data_apertura = ((CaribelDatebox)comp.query("#ski_data_apertura")).getRawText();
				String skiDataChiusura  = ((CaribelDatebox)comp.query("#ski_data_uscita")).getRawText();
				String testo = "";

				if(n_contatto==null || n_contatto.equals(""))
					testo = Labels.getLabel("caribelContainer.contatto.in_fase_di_ins");
				else
					testo = Labels.getLabel("caribelContainer.contatto.numero")+": "+n_contatto+
							" "+Labels.getLabel("caribelContainer.contatto.data_apertura")+" "+ski_data_apertura;
				
				if(ManagerDate.validaData(skiDataChiusura)){
					testo+=" " +Labels.getLabel("SchedaInfForm.conclusione.data.linea.menu")+": "+ skiDataChiusura;
				}
				
				addItemMenuTop(comp,testo, (ManagerDate.validaData(skiDataChiusura) ? CostantiSinssntW.CTS_ROW_RED:""));
//				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				logger.debug(punto + " diario>>");
			}
		}
	}
	

	public static String gestioneChiusuraCartella(String testo, String dataChiusura, String valoreMotivoChiusura) {
		if (ManagerDate.validaData(dataChiusura)) {
			testo += " " + Labels.getLabel("caribelContainer.cartella.data.chiusura.contatto") + ": " + dataChiusura;
		}
		if (ISASUtil.valida(valoreMotivoChiusura)) {
			testo += " " + Labels.getLabel("caribelContainer.cartella.data.motivo.chiusura") + ": "
					+ valoreMotivoChiusura;
		}
		return testo;
	}

	public void gestisciLeftMenu(Component comp){
		//Disabilito tutto
		disableAllButtonMenuLeft(true);
		
		//Abilito pulsanti sempre attivi
		//disableButtonMenuLeft("esci",false);
		//disableButtonMenuLeft("fassiGrid",false);
		if(multiCont && hashMenuLeft.get("contattoInfGridAltri")!=null){
				hashMenuLeft.get("contattoInfGridAltri").setVisible(true);
		}
		
		if(hashChiaveValore.get(CostantiSinssntW.N_CARTELLA)!=null){
			//Abilito Contatto
			disableButtonMenuLeft("contattoInfForm",false);
			//Abilito Storico contatti
			disableButtonMenuLeft("contattoInfGridSto",false); 
			//Abilito Altri Contatti e lo rendo visibile
			disableButtonMenuLeft("contattoInfGridAltri", !multiCont);
			//Abilito il bottone di AsterView
			disableButtonMenuLeft("asterview", !conAsterView);
			//Abilito il bottone dei documenti
			disableButtonMenuLeft("documenti", false);
		}
		if(hashChiaveValore.get(CostantiSinssntW.N_CONTATTO)!=null && !hashChiaveValore.get(CostantiSinssntW.N_CONTATTO).equals("0")){
			menuLeftTabContatto.setSelected(true);
			//abilita accessi
			disableButtonMenuLeft("infAccessiForm",false);
			disableButtonMenuLeft("pianoAssistForm",false);
			//Abilito Diario
			disableButtonMenuLeft(CostantiSinssntW.CTS_DIARIO , false);
			disableButtonMenuLeft(CTS_INTOLLERANZE_ALLERGIE,false);
			//Abilito Segnalazioni
			disableButtonMenuLeft(CTS_SEGNALAZIONI,false);
			disableButtonMenuLeft(CTS_RIEPILOGO_ACCESSI,false);
		}
		if(hashChiaveValore.get("cbx_motivo")!=null && ManagerProfile.isConfigurazioneMolise(getProfile())){
			
	        if (this.isConCtrlFlusSiad) {
	        	hashChiaveValore.put("isMotxFlussi",new Boolean((codMotivo_xFlussiSiad.indexOf((String) hashChiaveValore.get("cbx_motivo")) != -1)));
	            disableButtonMenuLeft("flussiSiadForm",!getBoolValueFromHash("isMotxFlussi"));
	            disableButtonMenuLeft("flussiStoForm", !getBoolValueFromHash("isMotxFlussi"));
	            disableButtonMenuLeft("pianoAssistForm",getBoolValueFromHash("isMotxFlussi"));
	            hashChiaveValore.put("isObblPresaCar",new Boolean(getBoolValueFromHash("isMotxFlussi")));
	        }
//	        if (getBoolValueFromHash("isMotxFlussi")){
//	        	checkEsistePresaCar(comp);
//	        }
	        checkEsistePianoAssist();
			faiMsgNoGestFlussi(comp);			
		}
		gestisciPersonalizzazioni();
		
		
	}
	 private void gestisciPersonalizzazioni() {
		if (ManagerProfile.isConfigurazioneMarche(getProfile())){
		 hashMenuLeft.get("flussiStoForm").setVisible(false);
		 hashMenuLeft.get("flussiSiadForm").setVisible(false);
		 hashMenuLeft.get("infAccertamentoForm").setVisible(false);
		 hashMenuLeft.get("infStatoSaluteForm").setVisible(false);
		 hashMenuLeft.get("infCuteMucoseForm").setVisible(false);
		 hashMenuLeft.get("infEventiForm").setVisible(false);
		 hashMenuLeft.get("infScaleTestForm").setVisible(false);
		 hashMenuLeft.get("pianoAssistForm").setVisible(false);
		}
		 
		
	}

	public void checkEsistePianoAssist()
	    {
		     if(getBoolValueFromHash("contattoInfInInsert"))
	            return;
	         	
	        if (isConCtrlFlusSiad) {
	        	
	        	String cart =  hashChiaveValore.containsKey("n_cartella")?hashChiaveValore.get("n_cartella").toString():"";
	            String cont =  hashChiaveValore.containsKey("n_contatto")?hashChiaveValore.get("n_contatto").toString():"";
	            if (!cart.equals("") && !cont.equals("")) {
	            try {
	            	Hashtable<String,String> h_dati = new Hashtable<String,String>();
		            h_dati.put("n_cartella", cart);
		            h_dati.put("n_progetto", cont);
		            h_dati.put("pa_tipo_oper", GestTpOp.CTS_COD_INFERMIERE);
		            PianoAssistEJB pa = new PianoAssistEJB();
		            hashChiaveValore.put("existsPianoAssist", new Boolean(pa.checkEsistePianoAssist(CaribelSessionManager.getInstance().getMyLogin(),h_dati)));
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	}
	        }
	    }
//	   public void checkEsistePresaCar(Component comp)
//	    {
//		 
//	       if(getBoolValueFromHash("contattoInfInInsert")||getBoolValueFromHash("contattoInfReadOnly"))
//	    	   hashChiaveValore.put("existsPresaCar", new Boolean(false));
//	       
//	       	String cart  = hashChiaveValore.containsKey("n_cartella")?hashChiaveValore.get("n_cartella").toString():"";
//			String dtApe = hashChiaveValore.containsKey("ski_data_apertura")?hashChiaveValore.get("ski_data_apertura").toString():"";
//			
//
//			if (!cart.equals("") && !dtApe.equals("")) {
//			Hashtable<String,String> h_dati = new Hashtable<String,String>();
//            h_dati.put("n_cartella", cart);
//            h_dati.put("dt_rif", dtApe);
//            RLPresaCaricoEJB rlpc = new RLPresaCaricoEJB();
//					try {
//						hashChiaveValore.put("existsPresaCar", new Boolean(rlpc.checkEsistePresaCarAperta(
//								CaribelSessionManager.getInstance().getMyLogin(), h_dati)));
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//			}
//            disableButtonMenuLeft("pianoAssistForm",!(getBoolValueFromHash("existsPresaCar") && !getBoolValueFromHash("contattoInfInInsert")));
//            
//
//            if (!getBoolValueFromHash("existsPresaCar") && getBoolValueFromHash("contattoInfInUpdate") && comp.getId().equals("contattoInfForm"))
//	        	Messagebox.show(
//						Labels.getLabel("SchedaInfForm.message.inf.notExistPCxMotivo"),
//						Labels.getLabel("messagebox.attention"),
//						Messagebox.OK,
//						Messagebox.EXCLAMATION);
//	    }
	   private void faiMsgNoGestFlussi(Component comp)
	    {
	        if ((isConCtrlFlusSiad) 
	        		&& hashChiaveValore.containsKey("cbx_motivo") 
	        		&& !hashChiaveValore.get("cbx_motivo").toString().trim().equals("")
	        && !getBoolValueFromHash("isObblPresaCar") 
	        && !getBoolValueFromHash("existsPianoAssist")
	        && getBoolValueFromHash("contattoInfInUpdate")
	        && comp.getId().equals("contattoInfForm"))          
	        Messagebox.show(
						Labels.getLabel("SchedaInfForm.message.inf.notNecessaryPCxMotivo"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.EXCLAMATION);
	        
	       
	    }


	private void gestisciApertureAutomatiche(Component comp){
		String punto = ver  + "gestisciApertureAutomatiche ";
		if(comp!=null){
			if(comp.getId()==null || comp.getId().equals(""))
				return;
			
			if(comp.getId().equals("cartellaForm")){
				//Ho aperto una Cartella, allora provo ad aprire anche il relativo Contatto, se non e' stato ancora aperto
				String n_cartella = ((CaribelIntbox)comp.query("#n_cartella")).getRawText();	
				if(super.hashIdComponent.get("contattoInfForm")==null &&  n_cartella!=null && !n_cartella.equals("")){
					recuperaDaSchedaValutazione(n_cartella);
					HashMap<String, Object> mapParam = new HashMap<String, Object>();
					mapParam.put(CostantiSinssntW.N_CARTELLA, n_cartella);					
					Component contattoInfForm = createComponent(hIdUrizul.get("contattoInfForm"), mapParam);
					showComponent(contattoInfForm);
				}
			} else if(comp.getId().equals("contattoInfForm") && aprireSegnalazione){
				logger.trace(punto + " dati CTS_CONTATTO_MEDICO");
				aprireSegnalazione = false;
				String nCartella = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CARTELLA);
				HashMap<String, Object> mapParam = new HashMap<String, Object>();
				mapParam.put(CostantiSinssntW.N_CARTELLA, nCartella);
				Component sntSegnalazioni = createComponent(hIdUrizul.get(CTS_SEGNALAZIONI), mapParam);
				showComponent(sntSegnalazioni);
			}
		}
	}
	
	private void recuperaDaSchedaValutazione(String n_cartella) {
		/*  Carica i dati della scheda di valutazione */
		SkValutazEJB skValutazEJB = new SkValutazEJB();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		try {
			dati.put(CostantiSinssntW.N_CARTELLA, n_cartella);
			Hashtable<String, String> h_SkVal = skValutazEJB.query_esisteSkValAttiva(CaribelSessionManager.getInstance().getMyLogin(),dati);

			if(h_SkVal.containsKey(CostantiSinssntW.PR_DATA)){
				hashChiaveValore.put(CostantiSinssntW.PR_DATA, ISASUtil.getValoreStringa(h_SkVal, CostantiSinssntW.PR_DATA));
			}
			hashChiaveValore.put("desc_val_ap", ISASUtil.getValoreStringa(h_SkVal, "desc_val_ap"));
			String prDataCarico=ISASUtil.getValoreStringa(h_SkVal, "pr_data_carico"); 
			if (ISASUtil.valida(prDataCarico)){
				hashChiaveValore.put("pr_data_carico", prDataCarico);
			}
			String prDataValutaz = ISASUtil.getValoreStringa(h_SkVal, "pr_data_valutaz");
			if (ISASUtil.valida(prDataValutaz)){
				hashChiaveValore.put("pr_data_valutaz", prDataValutaz);
			}
//			  if (h_SkVal.containsKey("retCode")) {
//		            int retCode = Integer.parseInt(h_SkVal.get("retCode"));
//		            if (retCode == this.NODIRITTI) {
//		                this.gestSkValNoDiritti();
//		                return new Boolean(false);
//		            }
//		        }
//		        int statoAp = 0;
//		        Integer iStatoAp = (Integer)hashChiaveValore.get("stato");
//		        if (iStatoAp != null)
//		            statoAp = iStatoAp.intValue();
//		        int stato = (statoAp!=this.CONSULTA?(esisteSkVal?this.UPDATE_DELETE:this.INSERT):this.CONSULTA);
//
//		        hashChiaveValore.put("stato", new Integer(stato));

		        hashChiaveValore.put("isSkValChiuso", new Boolean(false));

			
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (CariException e) {
			e.printStackTrace();
		} catch (WrongValueException e) {
			// TODO Auto-generated catch block
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
		Component comp = ((Component)evt.getData());
		gestisciChiaviCorr(comp);
		gestisciTopMenu(comp);
		gestisciApertureAutomatiche(comp);
		gestisciRefreshGrid(comp);
		gestisciLeftMenu(comp);
	}

	@Listen("onClick=#btn_contattoInfForm")
	public void btn_contattoInfForm() {
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("contattoInfForm",hIdUrizul.get("contattoInfForm"),true,null);
			}else{
				removeComponentsFrom("contattoInfForm");
				super.showComponent("contattoInfForm",hIdUrizul.get("contattoInfForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_contattoInfGridSto")
	public void btn_contattoInfGridSto() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "N");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("contattoInfGridSto",hIdUrizul.get("contattoInfGridSto"),true,mapParam);
			}else{
				removeComponentsFrom("contattoInfGridSto");
				super.showComponent("contattoInfGridSto",hIdUrizul.get("contattoInfGridSto"),mapParam);
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
	
	@Listen("onClick=#btn_contattoInfGridAltri")
	public void btn_contattoInfGridAltri() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "S");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("contattoInfGridSto",hIdUrizul.get("contattoInfGridSto"),true,mapParam);
			}else{
				removeComponentsFrom("contattoInfGridSto");
				super.showComponent("contattoInfGridSto",hIdUrizul.get("contattoInfGridSto"),mapParam);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_infAccertamentoForm")
	public void btn_infAccertamentoForm() {
		try{
			HashMap<String, Object> mapParam = new HashMap<String, Object>();
			mapParam.put("contAperti", "S");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("infAccertamentoForm",hIdUrizul.get("infAccertamentoForm"),true,mapParam);
			}else{
				removeComponentsFrom("infAccertamentoForm");
				super.showComponent("infAccertamentoForm",hIdUrizul.get("infAccertamentoForm"),mapParam);
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
	
	@Listen("onClick=#btn_infStatoSaluteForm")
	public void btn_infStatoSaluteForm() {		
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("infStatoSaluteForm",hIdUrizul.get("infStatoSaluteForm"),false,null);
			}else{
				super.showComponent("infStatoSaluteForm",hIdUrizul.get("infStatoSaluteForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_infCuteMucoseForm")
	public void btn_infCuteMucoseForm() {	
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("infCuteMucoseForm",hIdUrizul.get("infCuteMucoseForm"),true,null);
			}else{
				removeComponentsFrom("infCuteMucoseForm");
				super.showComponent("infCuteMucoseForm",hIdUrizul.get("infCuteMucoseForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_infScaleTestForm")
	public void btn_infScaleTestForm() {		
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("infScaleTestForm",hIdUrizul.get("infScaleTestForm"),true,null);
			}else{
				removeComponentsFrom("infScaleTestForm");
				super.showComponent("infScaleTestForm",hIdUrizul.get("infScaleTestForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_infAccessiForm")
	public void btn_infAccessiForm() {
		try{
			HashMap <String, Object> argForZul = new HashMap<String, Object>();
			argForZul.put("provAccessiPrestazioni", 1+"");
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("accessiPrestazioniForm",hIdUrizul.get("infAccessiForm"),true,argForZul);
			}else{
				removeComponentsFrom("accessiPrestazioniForm");
				super.showComponent("accessiPrestazioniForm",hIdUrizul.get("infAccessiForm"),argForZul);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_infEventiForm")
	public void btn_infEventiForm() {		
		try{
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("eventiForm",hIdUrizul.get("eventiForm"),true,null);
			}else{
				removeComponentsFrom("eventiForm");
				super.showComponent("eventiForm",hIdUrizul.get("eventiForm"));
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	@Listen("onClick=#btn_infPianoAssistForm")
	public void btn_infPianoAssistForm() {		
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
		
	@Listen("onClick=#btn_infFlussiSiadForm")
	public void btn_infFlussiSiadForm() {	
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
	
	@Listen("onClick=#btn_infFlussiStoForm")
	public void btn_infFlussiStoForm() {	
		try {
			if(formNeedApprovalForSave()){			
				executeApprovalForSaveBeforShowComponent("flussiStoForm",hIdUrizul.get("flussiStoForm"),true,null);
			}else{
				removeComponentsFrom("flussiStoForm");
				super.showComponent("flussiStoForm",hIdUrizul.get("flussiStoForm"));
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
				
			}else if(comp.getId().equals("contattoInfForm")){
				//Cancellazione del Contatto
				removeComponentsFrom(comp.getId());
				super.showComponent("cartellaForm",hIdUrizul.get("cartellaForm"));
				
			}else if(comp.getId().equals(CostantiSinssntW.CTS_DIARIO)){
				//Diario 
				removeComponentsFrom(comp.getId());
				super.showComponent(CostantiSinssntW.CTS_DIARIO,hIdUrizul.get(CostantiSinssntW.CTS_DIARIO));
			}else if(comp.getId().equals(CTS_INTOLLERANZE_ALLERGIE)){
				// Relazione medica  
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_INTOLLERANZE_ALLERGIE,hIdUrizul.get(CTS_INTOLLERANZE_ALLERGIE));
			}else if(comp.getId().equals(CTS_SEGNALAZIONI)){
				removeComponentsFrom(comp.getId());
				super.showComponent(CTS_SEGNALAZIONI,hIdUrizul.get(CTS_SEGNALAZIONI));
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
				hashChiaveValore.remove(CostantiSinssntW.N_CARTELLA);
				hashChiaveValore.remove(CostantiSinssntW.N_CONTATTO);
				hashChiaveValore.remove(CostantiSinssntW.CTS_ID_SKSO);
				hashChiaveValore.remove("ski_data_apertura");
				super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get("contattoInfForm"));
				super.removeComponent(hashIdComponent.get("flussiSiadForm"));
				super.removeComponent(hashIdComponent.get("contattoInfGridSto"));
				super.removeComponent(hashIdComponent.get(CostantiSinssntW.CTS_DIARIO));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
				
			}else if(idComp.equals("contattoInfForm") || idComp.equals("contattoFisioGridSto")){
				hashChiaveValore.remove(CostantiSinssntW.N_CONTATTO);
				hashChiaveValore.remove(CostantiSinssntW.CTS_ID_SKSO);
				hashChiaveValore.remove("ski_data_apertura");
				//super.removeComponent(hashIdComponent.get("cartellaForm"));
				super.removeComponent(hashIdComponent.get("contattoInfForm"));
				super.removeComponent(hashIdComponent.get("flussiSiadForm"));
				super.removeComponent(hashIdComponent.get("contattoInfGridSto"));
				super.removeComponent(hashIdComponent.get(CostantiSinssntW.CTS_DIARIO));
				super.removeComponent(hashIdComponent.get(CTS_INTOLLERANZE_ALLERGIE));
				super.removeComponent(hashIdComponent.get(CTS_SEGNALAZIONI));
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
			}else if (idComp.equals(CTS_RIEPILOGO_ACCESSI)){
				super.removeComponent(hashIdComponent.get(CTS_RIEPILOGO_ACCESSI));
			}
		}
	}
	
	public boolean checkMyValidity() throws Exception
    {
        boolean dtApeOk = true;

        // se configurato per poter avere un solo contatto aperto
        if ((!multiCont)) {
            String cart = hashChiaveValore.get(CostantiSinssntW.N_CARTELLA).toString();
            String cont = hashChiaveValore.get(CostantiSinssntW.N_CONTATTO).toString();
            String dtApe = hashChiaveValore.get("ski_data_apertura").toString();
            Hashtable h_dati = new Hashtable();
            h_dati.put("n_cartella", cart);
            h_dati.put("n_contatto", cont);
            h_dati.put("ski_data_apertura", dtApe);

            dtApeOk = !((Boolean) invokeGenericSuEJB(new SkInfEJB(), h_dati, "query_checkDtApeContLEMaxDtContChius")).booleanValue();

            if (!dtApeOk)
            		Messagebox.show(
    						Labels.getLabel("SchedaInfForm.message.inf.dataNonCongruente"),
    						Labels.getLabel("messagebox.attention"),
    						Messagebox.OK,
    						Messagebox.EXCLAMATION);
              
        }
        return dtApeOk;
     
    }
	
    private boolean caricaCaso() throws Exception
   {
       
       if ((isConCtrlFlusSiad) && !checkMyValidity())
               return false;
       
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
       
              logger.debug("caricaCaso(): CASO creato da zero!!!");
           }else
           {
        	   logger.debug("caricaCaso(): creazione nuovo CASO non riuscita!!!");
               return false;
           }
       }
       return true;
   }
	private boolean getBoolValueFromHash(String key){
		if (hashChiaveValore.containsKey(key) && hashChiaveValore.get(key) instanceof Boolean){
			return ((Boolean)hashChiaveValore.get(key)).booleanValue();
		}
		else return false;
	}
	
    // 25/03/13 mv
    private boolean isDaAbilPulsPianoAssist()
    {
        if (this.isConCtrlFlusSiad)
            return ((!this.isMotxFlussi) || (this.existsPresaCar));
        return true;
    }
}

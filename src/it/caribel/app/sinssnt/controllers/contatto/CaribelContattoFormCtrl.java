package it.caribel.app.sinssnt.controllers.contatto;

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOOpCoinvoltiEJB;
import it.caribel.app.sinssnt.bean.nuovi.SCBisogniEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa.SegreteriaOrganizzativaFormCtrl;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tabpanel;

public class CaribelContattoFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = 6993028098023457122L;

	protected CaribelIntbox n_cartella;
	protected String id_skso_url = null;
	protected String skso_fonte = null;
	protected Component sofc = null;
	protected Hashtable temp_container_hash = null;
	protected CaribelIntbox id_skso;
	protected Tabpanel tabpanel_scale;	
	protected Component cs_operatore_referente;
	protected CaribelSearchCtrl csct_operatore_referente;

	private static final String ver = "2-";
	
	@Override
	public void doInitForm() {
		CaribelSearchCtrl csct_operatore_referente = (CaribelSearchCtrl) cs_operatore_referente.getAttribute(MY_CTRL_KEY);
		try {
			if(UtilForContainer.getTipoOperatorerContainer() != null && !UtilForContainer.getTipoOperatorerContainer().isEmpty()){
				csct_operatore_referente.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, UtilForContainer.getTipoOperatorerContainer());
				csct_operatore_referente.putLinkedSearchObjects("tipo_op_lock", true);
			}else{
				csct_operatore_referente.putLinkedSearchObjects(CostantiSinssntW.CTS_FIGURE_PROFESSIONALI, ManagerProfile.getTipoOperatore(getProfile()));
			}
		} catch (Exception e) {
			UtilForBinding.setComponentReadOnly(self, true);
		}
		csct_operatore_referente.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_ZONA, ManagerProfile.getZonaOperatore(getProfile()));
		csct_operatore_referente.putLinkedSearchObjects(CostantiSinssntW.CTS_OPERATORE_DISTRETTO, ManagerProfile.getDistrettoOperatore(getProfile()));
		
		Object dtCartellaAperta =  UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.ASSISTITO_CARTELLA_APERTA);
		if (ManagerDate.validaData(dtCartellaAperta+"")){
			hParameters.put(CostantiSinssntW.ASSISTITO_CARTELLA_APERTA, dtCartellaAperta);
		}
		Object dtCartellaChiusa =  UtilForContainer.getObjectFromMyContainer(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA);
		if (ManagerDate.validaData(dtCartellaChiusa+"")){
			hParameters.put(CostantiSinssntW.ASSISTITO_CARTELLA_CHIUSA, dtCartellaChiusa);
		}
		
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected void gestioneRichiestaChiusura(boolean fromException) {
		try{
			final Hashtable h = new Hashtable();
			h.putAll(arg);
		if (!fromException){
				ISASRecord dbr = (ISASRecord)invokeGenericSuEJB(new RMSkSOEJB(), h, "selectZonaSkValCorrente");
				if (dbr!=null){
					final String distretto_cod = dbr.get("cod_distretto_verbale").toString();
					final String zona_cod = dbr.get("zona_cod").toString();
					final String id_skso = dbr.get("id_skso").toString();
					
					String zona = dbr.get("gid").toString();
					String zona_desc = dbr.get("zona_desc").toString();
					if(!getProfile().getIsasUser().isInGroup(new Integer(zona))){				
				Messagebox.show(Labels.getLabel("so.conferma.richiesta.chiusura",new String[]{zona_desc}), 
						Labels.getLabel("messagebox.attention"),
						Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
						new EventListener<Event>() {
							public void onEvent(Event event)throws Exception {
								if (Messagebox.ON_YES.equals(event.getName())){
									String msg = inviaRichiestaChiusura(zona_cod,distretto_cod,id_skso, h);
									Clients.showNotification(msg.equals("")?Labels.getLabel("so.richiesta.chiusura.successo"):msg);									
								}
								if (Messagebox.ON_NO.equals(event.getName())){
									setReadOnly(true);
									return;			
								}						
							}							
						});
					} else return;
				}
			else return;
				
			}
		
		else{	
			this.setReadOnly(true);
			ISASRecord dbr = (ISASRecord)invokeGenericSuEJB(new RMSkSOEJB(), h, "selectZonaSkValCorrente");
			if (dbr!=null){
				final String zona_cod = dbr.get("zona_cod").toString();
				final String distretto_cod = dbr.get("cod_distretto_verbale").toString();
				final String id_skso = dbr.get("id_skso").toString();
			Messagebox.show(Labels.getLabel("so.conferma.richiesta.chiusura",new String[]{zona_cod}), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())){
								String msg = inviaRichiestaChiusura(zona_cod,distretto_cod,id_skso, h);
								Clients.showNotification(msg.equals("")?Labels.getLabel("so.richiesta.chiusura.successo"):msg);									
							}
							if (Messagebox.ON_NO.equals(event.getName())){
								return;			
							}						
						}							
					});
			}
			
		}
			}catch (Exception ex2) {
				doShowException(ex2);
			}
	}
	protected String inviaRichiestaChiusura(String zona_cod, String distretto_cod, String id_skso, Hashtable h) {
		String ret = "";
		try{
			h.put("esito_richiesta", CostantiSinssntW.STATO_RICHIESTA_CHIUSURA_IN_ATTESA);
			h.put("cod_zona_richiedente", ManagerProfile.getZonaOperatore(getProfile()));
			h.put("cod_zona_presacarico", zona_cod);
			h.put("cod_distretto_presacarico",distretto_cod);
			h.put("cod_operatore_richiedente",getProfile().getIsasUser().getKUser());
			h.put("data_richiesta", UtilForBinding.getValueForIsas(new Date()));
			h.put("id_skso",id_skso);
			boolean res = ((Boolean)invokeGenericSuEJB(new RMSkSOEJB(), h, "insertRichiestaChiusura")).booleanValue();
			if (res) return ret; else return Labels.getLabel("so.richiesta.chiusura.errore");
		}catch (InvocationTargetException e){
			if (((InvocationTargetException) e).getTargetException() instanceof CariException)
				ret = ((InvocationTargetException) e).getTargetException().getMessage();
		}catch (Exception e){
			e.printStackTrace();
			ret = Labels.getLabel("so.richiesta.chiusura.errore");
		}
		return ret;
	}
	
	protected void impostaCollegamentoSo(Button btnSchedaSo, ISASRecord currentIsasRecord) {
		String idSkso = ISASUtil.getValoreStringa(currentIsasRecord, CostantiSinssntW.CTS_ID_SKSO);
		String testoBtnSo = Labels.getLabel("SchedaInfForm.principale.SchedaSO");
		if (ISASUtil.valida(idSkso)){
			testoBtnSo +=" "+ Labels.getLabel("SchedaInfForm.principale.SchedaSO.numero");
			testoBtnSo +=" "+idSkso;
		}
		btnSchedaSo.setLabel(testoBtnSo);
	}

   protected Hashtable ricercaOpCoinvolti() throws Exception {
		Hashtable h = null;
		RMSkSOOpCoinvoltiEJB ocEJB = new RMSkSOOpCoinvoltiEJB();
		Hashtable h_oc = (Hashtable)hParameters.clone();
		h_oc.put(CostantiSinssntW.TIPO_OPERATORE,ManagerProfile.getTipoOperatore(getProfile()));
		h = (Hashtable) invokeGenericSuEJB(ocEJB, h_oc, "cercaOpCoinvolti");
		return h;
	}
   
   protected boolean scriviPrimaVisita(String data_pc, String n_cartella, String id_skso_url, boolean da_pv) throws Exception{
	   String punto = ver + "scriviPrimaVisita ";
	   boolean aggiornato = false;
	   Hashtable<String ,String> h_pv = new Hashtable<String ,String> ();
		h_pv.put(CostantiSinssntW.N_CARTELLA,n_cartella);
		h_pv.put(CostantiSinssntW.COD_OPERATORE,getProfile().getStringFromProfile(ManagerProfile.CODICE_OPERATORE));
		h_pv.put(CostantiSinssntW.TIPO_OPERATORE, ManagerProfile.getTipoOperatore(getProfile()));
		h_pv.put(CostantiSinssntW.CTS_FLAG_STATO_VISTA_DA_SO, CostantiSinssntW.CTS_FLAG_STATO_FATTA);
//		h_pv.put(CostantiSinssntW.CTS_ID_SKSO,id_skso_url);	
		h_pv.put(CostantiSinssntW.DT_PRIMA_VISITA, data_pc);
		h_pv.put(CostantiSinssntW.DT_PRESA_CARICO, data_pc);
		if (da_pv) h_pv.put(CostantiSinssntW.DA_PRIMA_VISITA, CostantiSinssntW.SI);
		int idSkso =ISASUtil.getValoreIntero(id_skso_url); 
				
		if (idSkso< 0){
			RMSkSOOpCoinvoltiEJB rmSkSOOpCoinvoltiEJB = new RMSkSOOpCoinvoltiEJB();
			Hashtable<String, String>dati = new Hashtable<String, String>();
			dati.put(Costanti.N_CARTELLA, n_cartella);
			ISASRecord dbrRmSkso = rmSkSOOpCoinvoltiEJB.recuperaIdSksoDaAggiornare(getProfile().getInstance().getMyLogin(), dati);
			idSkso = ISASUtil.getValoreIntero(dbrRmSkso, Costanti.CTS_ID_SKSO);
		}
		if (idSkso<0){
			logger.debug(punto + " Non esiste un scheda so ATTIVA per la cartella>"+ n_cartella);
		}else {
			h_pv.put(CostantiSinssntW.CTS_ID_SKSO, idSkso + "");
			// aggiorno anche l'eventuale scala bisogni non ancora collegata alla skso
			logger.debug(punto + "Esiste aggiorno i dati >"+ n_cartella);
			invokeGenericSuEJB(new SCBisogniEJB(), h_pv, "updateIdSkso");
			aggiornato = ((Boolean)invokeGenericSuEJB(new RMSkSOOpCoinvoltiEJB(), h_pv , "updatePv")).booleanValue();
		}
		
		return aggiornato;
   }

	
   
	protected void onSchedaSO() throws Exception{
		Hashtable h = new Hashtable();
		h.put("n_cartella", n_cartella.getValue().toString());
		if (id_skso.getValue()!=null)
			{
				String id_sk = id_skso.getValue().toString();
				h.put(CostantiSinssntW.CTS_ID_SKSO, id_sk);
				h.put(CostantiSinssntW.ACTION, new Integer(CostantiSinssntW.CONSULTA_RICHIESTA));
				h.put("caribelContainerCtrl", super.caribelContainerCtrl);
				temp_container_hash = (Hashtable) super.caribelContainerCtrl.hashChiaveValore.clone();
				Component puacSchedaCorrForm = self.getFellowIfAny("puacSchedaCorrForm");
				if (puacSchedaCorrForm !=null){
					puacSchedaCorrForm.detach();
				}
   			    sofc = Executions.getCurrent().createComponents(SegreteriaOrganizzativaFormCtrl.CTS_FILE_ZUL, self, h);
				sofc.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
					public void onEvent(Event event) throws Exception{
						onCloseSchedaSO();
					}
				});		
				 
			}
		else{
		ISASRecord dbr = (ISASRecord) invokeGenericSuEJB(new RMSkSOEJB(), h,"selectSkValCorrente");
		if (dbr != null && dbr.get(CostantiSinssntW.CTS_ID_SKSO)!=null){
			String id_sk = dbr.get(CostantiSinssntW.CTS_ID_SKSO).toString();
			h.put(CostantiSinssntW.CTS_ID_SKSO, id_sk);
			h.put(CostantiSinssntW.ACTION, new Integer(CostantiSinssntW.CONSULTA_RICHIESTA));
			h.put("caribelContainerCtrl", super.caribelContainerCtrl);
			temp_container_hash = (Hashtable) super.caribelContainerCtrl.hashChiaveValore.clone();
			 sofc = Executions.getCurrent().createComponents(SegreteriaOrganizzativaFormCtrl.CTS_FILE_ZUL, self, h);
			 sofc.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
					public void onEvent(Event event) throws Exception{
						onCloseSchedaSO();
					}
				});		
			 
		}
				else Messagebox.show(
				Labels.getLabel("SchedaInfForm.msg.no_scheda_so_attiva"),
				Labels.getLabel("messagebox.attention"),
				Messagebox.OK,
				Messagebox.EXCLAMATION);  
          return;
	}
	}
	
	protected void onCloseSchedaSO(){
		super.caribelContainerCtrl.hashChiaveValore.clear();
		super.caribelContainerCtrl.hashChiaveValore.putAll(temp_container_hash);
		
	}
	
	protected void doFreezeForm() throws Exception {
		if (tabpanel_scale != null && tabpanel_scale.getFirstChild() != null){
			tabpanel_scale.getFirstChild().detach();
		}
		super.doFreezeForm();
		gestionePannelloScale();
	}

	protected void gestionePannelloScale() throws Exception {
	}
	
	protected void recuperaDatiSettaggio(CaribelIntbox id_skso, CaribelCombobox cbxBoxSegn, CaribelCombobox cbxTipute, 
			CaribelCombobox cbxMotivo, CaribelTextbox skm_descr_contatto,CaribelDatebox dtApertura, CaribelDatebox skm_medico_da) throws CariException {
		String punto = ver  + "recuperaDatiSettaggio ";
		Hashtable<String,String> datiDaInviare = new Hashtable<String,String>();
		String idSkSo = (id_skso.getValue() !=null ? id_skso.getValue()+"":"");
		datiDaInviare.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
		datiDaInviare.put(CostantiSinssntW.CTS_ID_SKSO, idSkSo);
		logger.debug(punto + " dati che invio>>" + datiDaInviare);
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		ISASRecord dbrValutazione = rmSkSOEJB.selectSkValCorrente(CaribelSessionManager.getInstance().getMyLogin(),
				datiDaInviare);
		idSkSo = ISASUtil.getValoreStringa(dbrValutazione, CostantiSinssntW.CTS_ID_SKSO);
		if (ISASUtil.valida(idSkSo)){
			
			String codRichiedente = ISASUtil.getValoreStringa(dbrValutazione, "richiedente");
			String tipoUte = ISASUtil.getValoreStringa(dbrValutazione, "tipo_ute");
			String tipocure = ISASUtil.getValoreStringa(dbrValutazione, "tipocura");
			logger.debug(punto + " dati codRichiedente>" + codRichiedente + "<codRichiedente>" + codRichiedente
					+ "< tipocure>" + tipocure);
			cbxBoxSegn.setSelectedValue(getRichiedente(codRichiedente));
			cbxTipute.setSelectedValue(tipoUte);
			cbxMotivo.setSelectedValue(tipocure);
			cbxBoxSegn.setRequired(!codRichiedente.equals(""));
			cbxTipute.setRequired(!tipoUte.equals(""));
	
			if (ISASUtil.valida(tipocure)){
				cbxMotivo.setDisabled(true);
			}else {
				cbxMotivo.setDisabled(false);
			}
			cbxTipute.setDisabled(true);
			cbxBoxSegn.setDisabled(true);
		}
		
		if (!skm_descr_contatto.getValue().contains(CostantiSinssntW.DA_SCHEDA_SO))
			skm_descr_contatto.setValue(skm_descr_contatto.getValue() + CostantiSinssntW.DA_SCHEDA_SO);
		if (this.currentIsasRecord == null && !ManagerDate.validaData(dtApertura)) {
			skm_medico_da.setValue(dtApertura.getValue());
		}
		// }
	}
	
	public static String getRichiedente(String codRichiedente) {
		return codRichiedente.equals("") ? "9" : codRichiedente;
	}

	protected Date recuperaDataAccettazioneSkSo(CaribelIntbox n_cartella, CaribelIntbox id_skso, CaribelCombobox cbx_provenienza,
			CaribelCombobox cbx_utenza, CaribelCombobox cbx_motivo, CaribelTextbox ski_descr_contatto,
			CaribelDatebox ski_data_apertura, CaribelDatebox ski_infermiere_da) {
		Date dataAccettazione = null;
		Hashtable<String, String> prtDati = new Hashtable<String, String>();
		prtDati.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue()+"");
		prtDati.put(CostantiSinssntW.CTS_ID_SKSO, (id_skso!=null && id_skso.getValue()!=null )? id_skso.getValue().toString():"");
		prtDati.put(CostantiSinssntW.TIPO_OPERATORE, ManagerProfile.getTipoOperatore(getProfile()));
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		try {
			ISASRecord dbrRmSkso = rmSkSOEJB.recuperaDataApertura(CaribelSessionManager.getInstance().getMyLogin(), prtDati);
			if (dbrRmSkso!=null){
				dataAccettazione =(Date) dbrRmSkso.get(CostantiSinssntW.CTS_APERTURA_CONTATTO_SKSO_PROPOSTA);
				id_skso.setValue(ISASUtil.getValoreIntero(dbrRmSkso, CostantiSinssntW.CTS_ID_SKSO));
				recuperaDatiSettaggio(id_skso, cbx_provenienza, cbx_utenza, cbx_motivo,ski_descr_contatto,  ski_data_apertura, ski_infermiere_da);
			}
		} catch (Exception e) {
		}
		return dataAccettazione;
	}
}

package it.caribel.app.sinssnt.controllers.lista_attivita;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneDuplicatoCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.interfaces.AskDatiInput;
import it.pisa.caribel.util.ISASUtil;
import java.util.Hashtable;
import java.util.Map;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

public class AttribuzioneDistrettoPht2Ctrl extends CaribelFormCtrl{
	private static final long serialVersionUID = -5295859076639542148L;
	public final static String myPathZul = "/web/ui/sinssnt/lista_attivita/attribuzione_distrettoPht2.zul";
	public final static String METHODNAME = "methodName"; //parametro per la selezione del metodo nel chiamante
	public final static String OPERAZIONE_DA_EFFETTUARE = "op_ind_oper"; //operatore selezionata dall'utente
	public final static int OPERAZIONE_DA_EFFETTUARE_CARICA_ASSISTITO = 1; //carica assistito su ubicazione attuale
	public final static int OPERAZIONE_DA_EFFETTUARE_SPOSTA_ASSITITO = 2; //sposta assistito su ubicazione indicata
	public final static int OPERAZIONE_DA_EFFETTUARE_CANCELLA = 3; //non eseguire nessuna operazione
	
	protected CaribelDatebox data;
	protected Label message;
	protected String returnMethodName = null;
	public static final String ver = "3-AttribuzioneDistrettoPht2Ctrl. ";
	private CaribelCombobox zona;
	private CaribelCombobox distretto;
	private CaribelCombobox presidio_comune_area;
	private CaribelCombobox zonaDp;
	private CaribelCombobox distrettoDp;
	private CaribelCombobox presidio_comune_areaDp;
	private Button btnSposta;
	
	public static final String CTS_INFO_ASSISTITO = "info_ass";
	public static final String CTS_ZONA_APPARTENENZA = "ap_zona";
	public static final String CTS_DISTRETTO_APPARTENENZA = "ap_distretto";
	public static final String CTS_SEDE_APPARTENENZA = "ap_sede";
	public static final String CTS_ID_SCHEDA_PHT2 = "idSchedaPht2";
	
	public static final String CTS_ZONA_ASSEGNATO = "zona_ass";
	public static final String CTS_DISTRETTO_ASSEGNATO = "distretto_ass";
	public static final String CTS_SEDE_ASSEGNATO = "sede_ass";
	
	public void doInitForm() {
		try {
			impostaDatiUbicazione(arg);
			
			impostaDatiAssistito(arg);
			
			
			zonaDp.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					abilitaBottoneSposta();
					return;
				}
			});			

			distrettoDp.addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
				public void onEvent(Event event){
					abilitaBottoneSposta();
				}
			});	
			

//			presidio_comune_areaDp.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
//				public void onEvent(Event event) throws Exception {
//					abilitaBottoneSposta();
//					return;
//				}
//			});			
			
			if(arg.containsKey(METHODNAME)){
				returnMethodName = (String) arg.get(METHODNAME);
			}
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	protected void abilitaBottoneSposta() {
		boolean disabilitareCambio = recuperaSeAbilitareCampo();
		((Button)btnSposta).setDisabled(disabilitareCambio);
	}

	private boolean recuperaSeAbilitareCampo() {
		String punto = ver + "recuperaSeAbilitareCampo ";
		String zonaVal, zonaValDp;
		String distrettoVal, distrettoValDp;
//		String sedeVal, sedeValDp;
		
		zonaVal = zona.getSelectedValue();
		zonaValDp = zonaDp.getSelectedValue();
		distrettoVal = distretto.getSelectedValue();
		distrettoValDp = distrettoDp.getSelectedValue();
//		sedeVal = presidio_comune_area.getSelectedValue();
//		sedeValDp = presidio_comune_areaDp.getSelectedValue();
		
		boolean uguale = uguale(zonaVal, zonaValDp); 
		if (uguale){
			uguale = uguale(distrettoVal, distrettoValDp);
//			if (uguale){
//				uguale = uguale(sedeVal, sedeValDp);
//			}
		}
		logger.trace(punto + " uguale >>" + uguale);
		return uguale;
	}
	
	private String recuperaCosaCambio() {
		String punto = ver + "recuperaCosaCambio ";
		String zonaVal, zonaValDp;
		String distrettoVal, distrettoValDp;
//		String sedeVal, sedeValDp;
		
		zonaVal = zona.getSelectedValue();
		zonaValDp = zonaDp.getSelectedValue();
		distrettoVal = distretto.getSelectedValue();
		distrettoValDp = distrettoDp.getSelectedValue();
//		sedeVal = presidio_comune_area.getSelectedValue();
//		sedeValDp = presidio_comune_areaDp.getSelectedValue();
		String messaggio = "";
		boolean uguale = uguale(zonaVal, zonaValDp); 
		if (!uguale){
			messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg.zona");
		}
		uguale = uguale(distrettoVal, distrettoValDp);
		if (!uguale){
			messaggio+=(ISASUtil.valida(messaggio)?"/":"");
			messaggio += Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg.distretto");
		}
		logger.trace(punto + " rest >>" + messaggio);
		
		return messaggio;
	}

	

	private boolean uguale(String attuale, String assegnato) {
		return (ISASUtil.valida(attuale) && ISASUtil.valida(assegnato) && attuale.equals(assegnato));
	}

	private void impostaDatiAssistito(Map arg) {
		String assistito = ISASUtil.getValoreStringa(arg, CTS_INFO_ASSISTITO );
		if (ISASUtil.valida(assistito)){
			String titolo = Labels.getLabel("attribuzione.distretto.referente.pht2.info.assistito", new String[]{assistito});
			((Window) self).setTitle(titolo);
		}
	}

	private void impostaDatiUbicazione(Map dati) {
		String punto = ver  + "impostaDatiUbicazione ";
		Component p = self.getFellow("panel_ubicazione");
		PanelUbicazioneCtrl c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
		c.doInitPanel(false, false);
		
		String zonaAppartenenza = ISASUtil.getValoreStringa(dati, CTS_ZONA_APPARTENENZA);
		if (!ISASUtil.valida(zonaAppartenenza)){
			zonaAppartenenza = ManagerProfile.getZonaOperatore(getProfile());
		}
		String distrettoAppartenenza = ISASUtil.getValoreStringa(dati, CTS_DISTRETTO_APPARTENENZA);
		if (!ISASUtil.valida(distrettoAppartenenza)){
			distrettoAppartenenza = ManagerProfile.getDistrettoOperatore(getProfile());
		}
		
//		String sedeAppartenenza = ISASUtil.getValoreStringa(dati, CTS_SEDE_APPARTENENZA);
//		if (!ISASUtil.valida(sedeAppartenenza)){
//			sedeAppartenenza = ManagerProfile.getPresidioOperatore(getProfile());
//		}
		
		logger.trace(punto + " zona appartenenza>>" + zonaAppartenenza);
		zona.setSelectedValue(zonaAppartenenza);
		Events.sendEvent(Events.ON_SELECT, zona, null);
		c.settaRaggrContatti("CA");
		zona.setDisabled(true);
		distretto.setSelectedValue(distrettoAppartenenza);
		Events.sendEvent(Events.ON_SELECT, distretto, null);
		distretto.setDisabled(true);
		presidio_comune_area.setVisible(false);
		c.setVisiblePresidioComuneAreaDis(false);

		Component pDp = self.getFellow("panel_ubicazioneDp");
		PanelUbicazioneDuplicatoCtrl cDp = (PanelUbicazioneDuplicatoCtrl) pDp.getAttribute(MY_CTRL_KEY);
		cDp.doInitPanel(true, false, false);
		zonaDp.setSelectedValue(zonaAppartenenza);
		Events.sendEvent(Events.ON_SELECT, zonaDp, null);
		cDp.settaRaggrContatti("CA");
		distrettoDp.setSelectedValue(distrettoAppartenenza);
		Events.sendEvent(Events.ON_SELECT, distrettoDp, null);
		presidio_comune_areaDp.setVisible(false);
		logger.trace(punto + "impostare \nzona appartenenza>>" + zonaAppartenenza +" distretto>"+ distrettoAppartenenza);
		cDp.setVisiblePresidioComuneAreaDis(false);
	}
	
	
	public void onCarica(ForwardEvent e) throws Exception{
		String punto = ver + "onCarica ";
		Hashtable<String, String> dati = recuperaDati();
		
		dati.put(OPERAZIONE_DA_EFFETTUARE, OPERAZIONE_DA_EFFETTUARE_CARICA_ASSISTITO+"");
		logger.trace(punto + " dati che invio>>" + dati + "<");
		((AskDatiInput) this.getForm().getParent().getAttribute(MY_CTRL_KEY)).setDatiInput(dati);
	}

	private Hashtable<String, String> recuperaDati() {
		String codZonaAssegnata = zonaDp.getSelectedValue();
		String codDistrettoAssegnato = distrettoDp.getSelectedValue();
//		String codPresidioAssegnato_ = presidio_comune_areaDp.getSelectedValue();
		Hashtable<String, String> dati = new Hashtable<String, String>();
		dati.putAll(arg);
		dati.put(CTS_ZONA_ASSEGNATO, codZonaAssegnata);
		dati.put(CTS_DISTRETTO_ASSEGNATO, codDistrettoAssegnato);
//		dati.put(CTS_SEDE_ASSEGNATO, codPresidioAssegnato);
		return dati;
	}

	public void onSposta(ForwardEvent e) throws Exception {
		
		String distrettoValDp = distrettoDp.getSelectedValue();
		String messaggio ="";
		if (!ISASUtil.valida(distrettoValDp)){
			messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.obbligo.distretto");
			Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.OK,
					Messagebox.EXCLAMATION);
			return;
		}
		
		String cosaCambio = recuperaCosaCambio();
		messaggio = Labels.getLabel("attribuzione.distretto.referente.pht2.spostamento.richiesta.msg", new String[]{cosaCambio});
		
		Messagebox.show(messaggio, Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO,
		Messagebox.QUESTION, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				if (Messagebox.ON_YES.equals(event.getName())) {
					Hashtable<String, String> dati = recuperaDati();
					dati.put(OPERAZIONE_DA_EFFETTUARE, OPERAZIONE_DA_EFFETTUARE_SPOSTA_ASSITITO + "");
					spostaAssistito(dati);
				}
			}
		});
	}

	private void spostaAssistito(Hashtable<String, String> dati) throws Exception {
		String punto = ver + "spostaAssistito ";
		logger.trace(punto + " dati che invio>>" + dati + "<");
		((AskDatiInput) this.getForm().getParent().getAttribute(MY_CTRL_KEY)).setDatiInput(dati);
	}

	public void onChiudi(ForwardEvent e) throws Exception{
		String punto = ver + "onChiudi ";
		Hashtable<String, String> dati = recuperaDati();
		dati.put(OPERAZIONE_DA_EFFETTUARE, OPERAZIONE_DA_EFFETTUARE_CANCELLA+"");
		logger.trace(punto + " dati che invio>>" + dati + "<");
		((AskDatiInput) this.getForm().getParent().getAttribute(MY_CTRL_KEY)).setDatiInput(dati);
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return false;
	}
}

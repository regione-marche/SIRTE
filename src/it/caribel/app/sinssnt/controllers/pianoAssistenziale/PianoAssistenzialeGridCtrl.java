package it.caribel.app.sinssnt.controllers.pianoAssistenziale;

import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.swing2.util.cariTableCom;
import it.pisa.caribel.util.ISASUtil;

import java.util.Date;
import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

public class PianoAssistenzialeGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;

	private String myKeyPermission = "";
	private PianoAssistEJB myEJB = new PianoAssistEJB();
	private String myPathFormZul = "/web/ui/sinssnt/piano_assistenziale/pianoAssistenzialeForm.zul";

	private CaribelTextbox	 jCariTextFieldHiddNCartella;
	private CaribelTextbox   jCariTextFieldHiddNProgetto;
	private CaribelTextbox   jCariTextFieldHiddCodObiettivo;
	private CaribelTextbox   jCariTextFieldHiddNIntervento;
	private CaribelTextbox   jCariTextFieldHiddTpOper;
//	private CaribelDatebox   jCariDateTextFieldChiusura;

	//	private Component operatore;

	private String MIONOME = this.getClass().getName();
	private final int INSERT = 1;
	private final int UPDATE_DELETE = 2;
	private final int CONSULTA = 3;

	//gb 10/08/07 *******
	private final String COD_OBIET_TIPO_OPER_NOT_01 = "00000000";
	private final String N_INTERV_TIPO_OPER_NOT_01 = "0";
	//gb 10/08/07: fine *******

	private cariTableCom ctc = new cariTableCom();
	private cariTableCom tXchiusura = new cariTableCom();
//	private utils u = new utils();

	private String gl_strUltimaDataChiusura = ""; //gb 08/08/07

	private int gl_intStatoAp = 0;

//	private JCariContainer myContainer = null;

	private String gl_strDtApeToPassToPianoAssist = null; //gb 14/11/06

	private Button jButtonDuplica;

	private Component searchField;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		this.setMethodNameForQuery("query_pianoAss");
		init();

		if(!loadDati2nd("")){
			UtilForUI.standardExclamation("Errore nel caricamento del Piano assistenziale");
		}
	}

	private void init() {
	}

	public void doCerca(){
		try {
			super.hParameters.putAll(UtilForBinding.getHashtableFromComponent(searchField));
		} catch (Exception e) {
			doShowException(e);
		}
		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			doRefreshNoAlert();
		}else{
			doRefresh();
		}
	}

	public void doStampa() {		
	}

	public Boolean loadDati2nd(String nomeLivelloLoad) throws Exception{
//		this.nomeLivLoad = nomeLivelloLoad; // 25/01/07

		Hashtable<String, Object> h_pianoAss = this.caribelContainerCtrl.hashChiaveValore; // myContainer.getHashRiga(this.nomeLivLoad);
		h_pianoAss.putAll(arg);
		String numCart = ISASUtil.getValoreStringa(h_pianoAss, CostantiSinssntW.N_CARTELLA);//((Integer)h_pianoAss.get("n_cartella")).toString();
		String tipoOperatore = UtilForContainer.getTipoOperatorerContainer(); //(String)h_pianoAss.get("tipo_operatore");

		//gb 17/05/07 *******
		if(tipoOperatore == null){
			logger.info("PianoAssistenzialeGridCtrl.loadDati2nd - TIPO OPERATORE NON VALIDO!!");
			return new Boolean(false);
		}
		jCariTextFieldHiddTpOper.setValue(tipoOperatore); //gb 17/05/07
		//gb 17/05/07 *******

		logger.info(" --> PianoAssistenzialeGridCtrl / loadDati2nd / tipoOperatore: " + tipoOperatore);

		//gb 10/08/07 *******
		String numProg = null;
		String numCont = null;
		String codObiet = null;
		String numInterv = null;

		if (tipoOperatore.equals("01") && !ManagerProfile.isConfigurazioneMarche(getProfile())){
			numProg = (String)h_pianoAss.get("n_progetto");
			codObiet = (String)h_pianoAss.get("cod_obbiettivo");
			numInterv = (String)h_pianoAss.get("n_intervento");
		}else{
			numProg = ISASUtil.getValoreStringa(h_pianoAss,"n_contatto");
			if(numProg.isEmpty()){//vengo dalla form del contatto
				numProg = (String) arg.get(CostantiSinssntW.N_CONTATTO);
			}
		}
		//gb 10/08/07: fine *******

		//gb 14/11/06 ****
		if (tipoOperatore.equals("01") && !ManagerProfile.isConfigurazioneMarche(getProfile())) //gb 07/09/07
			gl_strDtApeToPassToPianoAssist = (String)h_pianoAss.get("int_data_ins");
		//gb 07/09/07 ****
		else if (tipoOperatore.equals("02"))
			gl_strDtApeToPassToPianoAssist = (String)h_pianoAss.get("ski_data_apertura");
		else if (tipoOperatore.equals("03"))
			gl_strDtApeToPassToPianoAssist = (String)h_pianoAss.get("skm_data_apertura");
		else if (tipoOperatore.equals("04"))
			gl_strDtApeToPassToPianoAssist = (String)h_pianoAss.get("skf_data");
		else 
			gl_strDtApeToPassToPianoAssist = (String)h_pianoAss.get("skfpg_data_apertura");
		//gb 07/09/07: fine ****
		//gb 14/11/06: fine ****

		Integer iStatoAp=null;
		try{
		iStatoAp = (Integer)h_pianoAss.get("stato");
		h_pianoAss.remove("stato");
		}catch(ClassCastException e){iStatoAp = new Integer(h_pianoAss.get("stato").toString());}

		if (iStatoAp != null)
			gl_intStatoAp = iStatoAp.intValue();

		btn_new.setDisabled(!(gl_intStatoAp != CONSULTA));
		jButtonDuplica.setDisabled(!(gl_intStatoAp != CONSULTA));// 02/10/07


		if (numCart == null) {
			logger.info("PianoAssistenzialeGridCtrl.loadDati2nd - CARTELLA NON VALIDA!!");
			return (new Boolean(false));
		}
		jCariTextFieldHiddNCartella.setValue(numCart);


		//gb 10/08/07 *******
		if (numProg == null){
			if (tipoOperatore.equals("01") && !ManagerProfile.isConfigurazioneMarche(getProfile()))
				logger.info("PianoAssistenzialeGridCtrl.loadDati2nd - PROGETTO NON VALIDO!!");
			else
				logger.info("PianoAssistenzialeGridCtrl.loadDati2nd - CONTATTO NON VALIDO!!");
			return new Boolean(false);
		}else
			jCariTextFieldHiddNProgetto.setValue(numProg);

		if (tipoOperatore.equals("01") && !ManagerProfile.isConfigurazioneMarche(getProfile())){
			if (codObiet == null){
				logger.info("PianoAssistenzialeGridCtrl.loadDati2nd - OBIETTIVO NON VALIDO!!");
				return new Boolean(false);
			}
			jCariTextFieldHiddCodObiettivo.setValue(codObiet);
		}else{
			jCariTextFieldHiddCodObiettivo.setValue(COD_OBIET_TIPO_OPER_NOT_01);
		}

		if (tipoOperatore.equals("01") && !ManagerProfile.isConfigurazioneMarche(getProfile())){
			if (numInterv == null){
				logger.info("PianoAssistenzialeGridCtrl.loadDati2nd - INTERVENTO NON VALIDO!!");
				return new Boolean(false);
			}
			jCariTextFieldHiddNIntervento.setValue(numInterv);
		}else{
			jCariTextFieldHiddNIntervento.setValue(N_INTERV_TIPO_OPER_NOT_01);
		}
		//gb 10/08/07: fine *******

//		int ret = execSelect();
		doCerca();
		this.caribelContainerCtrl.hashChiaveValore.put("h_pianoAss", this.caribelContainerCtrl.hashChiaveValore);
		return new Boolean(caribellb.getItemCount() >= 0);
	}
	
	protected void doApri(){
		setInfoJCariContainer(this.UPDATE_DELETE, null);
		super.doApri();
	}
	
	
	protected void doNuovo(){
        // ctrl esistenza piani assistenziali precedenti non chiusi
        if (checkPianiAssAperti()){
          setInfoJCariContainer(this.INSERT, null);
          super.doNuovo();
        }
	}
	private boolean checkPianiAssAperti(){
		/*Prima di aprire un nuovo progetto assistenziale se ne esistono altri già aperti
        devo dare il messaggio se questi ultimi devono essrer chiusi, in caso di risposta
        positiva dare la possibilià di inserire la data di chiusura, mostrando la data odierna
		 */
		Hashtable ht = getParametriPianoAssistenziale();
		logger.info("checkPianiAssAperti / ht prima di db.ControlloPianiAperti(ht): " + ht.toString());
		boolean boolAperto = false;
		try {
			boolAperto = (Boolean) invokeGenericSuEJB(myEJB, ht, "controllo_piani");
		} catch (Exception e) {
			doShowException(e);
		}
		if(boolAperto){
	        UtilForUI.standardYesOrNo(Labels.getLabel("pianoAssistenziale.msg.pianiAperti"),new EventListener<Event>(){
				public void onEvent(Event event) throws Exception{
					if (Messagebox.ON_YES.equals(event.getName())){
						gestisciEsistenzaPianoAperto();
					}
					//altrimenti ritorna cmq. false e non viene eseguita la cancellazione
				}
			});
			return false;
		}else{
			Object o = "";
			//gb 08/08/07 *******
			if (caribellb.getItemCount() > 0)
				o = ((CaribelListModel)caribellb.getModel()).getFromRow(0, "pa_data_chiusura");
				gl_strUltimaDataChiusura = o.toString();
			//gb 08/08/07: fine *******
			return true;
		}
	}

	public void setInfoJCariContainer(int statoAp, String data_input){
		if(data_input!=null){
			gl_strUltimaDataChiusura = data_input;
			this.doRefreshNoAlert();
		}
		int numR = -1;
		String[] arrExistingDtVar = null;

		String numCart = jCariTextFieldHiddNCartella.getText().trim();
		String numProg = jCariTextFieldHiddNProgetto.getText().trim();
		String codObiet = jCariTextFieldHiddCodObiettivo.getText().trim();
		String numInterv = jCariTextFieldHiddNIntervento.getText().trim();
		String tpOper = jCariTextFieldHiddTpOper.getText().trim();
		String dataPiano = "";

		if (statoAp != this.INSERT){
			numR = caribellb.getSelectedIndex();//.getSelectedRow();
			dataPiano = (UtilForBinding.getValueForIsas((Date)((CaribelListModel)caribellb.getModel()).getFromRow(numR, "pa_data")));
		}

		Hashtable h_dati = new Hashtable();
		h_dati.put("n_cartella", numCart);
		h_dati.put("n_progetto", numProg);
		h_dati.put("cod_obbiettivo", codObiet);
		h_dati.put("n_intervento", numInterv);
		h_dati.put("int_data_ins", gl_strDtApeToPassToPianoAssist); //gb 14/11/06
		h_dati.put("pa_tipo_oper", tpOper);
		h_dati.put("pa_data", dataPiano);
		h_dati.put("ultima_data_chiusura", gl_strUltimaDataChiusura); //gb 08/08/07

		//gb 24/09/07 *******
		String strDataChiusura = "";
		if (numR >= 0)
			strDataChiusura = (UtilForBinding.getValueForIsas((Date)((CaribelListModel)caribellb.getModel()).getFromRow(numR, "pa_data_chiusura")));
		if (strDataChiusura.equals(""))
			//gb 24/09/07: fine *******
		{
			if (gl_intStatoAp == CONSULTA)
				h_dati.put("stato", new Integer(CONSULTA));
			else
				h_dati.put("stato", new Integer(statoAp));
		}
		//gb 24/09/07 *******
		else
		{
			h_dati.put("stato", new Integer(CONSULTA));
		}
		//gb 24/09/07: fine *******

		arrExistingDtVar = getExistingDtVar();
		if (arrExistingDtVar != null)
			h_dati.put("arrCodEsistenti", (String[])arrExistingDtVar);
		this.caribelContainerCtrl.hashChiaveValore.put("h_pianoAss", h_dati);
		
		if(data_input!=null){
			super.doNuovo();
		}
// TODO Verificare che fa ciò che mi aspetto
//		// setto informazioni nella barra di stato
//		this.myContainer.clearAndSetLivello(this.nomeLivInfo, "", h_dati);// 25/01/07
//		// apro lo storico
//		this.myContainer.invocaMetodo("apriJFPianoAssist");
	}
	
    private String[] getExistingDtVar(){
        String[] arrExistingCods = null;
        int numRigheGriglia = this.caribellb.getItemCount();
        int numRiga = caribellb.getSelectedIndex();
        if (numRigheGriglia > 0) {
            arrExistingCods = new String[numRigheGriglia];
            for (int j=0; j<numRigheGriglia; j++){
                if (j != numRiga)
                    arrExistingCods[j] = UtilForBinding.getValueForIsas(((Date)((CaribelListModel)caribellb.getModel()).getFromRow(j, "pa_data")));
                else
                    arrExistingCods[j] = "";
            }
            return arrExistingCods;
        } else
            return null;
    }
	
	private Hashtable getParametriPianoAssistenziale() {
		Hashtable ht = new Hashtable();
		ht.put("pa_tipo_oper",jCariTextFieldHiddTpOper.getText());
		ht.put("n_cartella",jCariTextFieldHiddNCartella.getText());
		ht.put("n_progetto",jCariTextFieldHiddNProgetto.getText());
		ht.put("cod_obbiettivo",jCariTextFieldHiddCodObiettivo.getText());
		ht.put("n_intervento",jCariTextFieldHiddNIntervento.getText());
		return ht;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean gestisciEsistenzaPianoAperto() {
		Hashtable ht = getParametriPianoAssistenziale();
		String mess=Labels.getLabel("pianoAssistenziale.msg.inserisciDataChiusura");
//		String titolo="Attenzione!";
		ht.put("mess", mess);
		ht.put("titolo", Labels.getLabel("messagebox.attention"));
		ht.put("dataUltima", UtilForBinding.getValueForIsas((java.sql.Date) ((CaribelListModel)caribellb.getModel()).getFromRow(0, "pa_data")));
		Executions.getCurrent().createComponents(PannelloDataFormCtrl.myZul, self, ht);
		return false;
	}
}

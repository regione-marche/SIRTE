package it.caribel.app.sinssnt.controllers.interventi;

import it.caribel.app.sinssnt.bean.modificati.ContIntervEJB;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.generic_controllers.CaribelSearchCtrl;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.procdate;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Window;

public class AccessiGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "CONTINTERV";
	private SINSSNTConnectionEJB myEJB = new ContIntervEJB();
	private String myPathFormZul = "/web/ui/sinssnt/interventi/accessiPrestazioniForm.zul";
	
	private CaribelIntbox int_anno;
	private CaribelIntbox int_progr;
	private CaribelTextbox n_cartella;
	
	String tipoOperatore = "";
	private CaribelTextbox JCariTextFieldProv;
	private CaribelTextbox tipo_op;
	private CaribelDatebox dataDa;
	private CaribelDatebox dataA;
	private CaribelTextbox cod_operatore;
	private CaribelSearchCtrl assistito;
	private Component cs_assistito;
	
	private Component operatore;

	private String MIONOME = this.getClass().getName();
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		if(arg.containsKey("int_anno")){
			int_anno.setValue(Integer.parseInt((String) arg.get("int_anno")));
		}else{
			int_anno.setValue(Integer.parseInt(procdate.getAnno()));
		}
		JCariTextFieldProv.setValue((String) (arg.containsKey("prov")?arg.get("prov"):""));
		tipoOperatore=(String) (arg.containsKey("tipo")?arg.get("tipo"):"");
		tipo_op.setValue(tipoOperatore);
		CaribelSearchCtrl ctrlOperatore = (CaribelSearchCtrl) operatore.getAttribute(MY_CTRL_KEY);
		ctrlOperatore.putLinkedSearchObjects("figprof", tipo_op);
		
		if(arg.get("caribelContainerCtrl") != null){
			((Window)getOwner()).setVflex("1");
			((Window)getOwner()).setHflex("1");
		}else{
			((Window)getOwner()).setMode("modal");
		}
		btn_print.setDisabled(true);
		n_cartella.focus();
		String cartella = (String) arg.get("n_cartella");
		if(cartella != null && !cartella.isEmpty()){
			n_cartella.setValue(cartella.toString());
			Events.sendEvent(Events.ON_CHANGE, n_cartella, cartella.toString());
			assistito = (CaribelSearchCtrl) cs_assistito.getAttribute(MY_CTRL_KEY);
			assistito.setReadonly(true);
			doCerca();
		}
		int_progr.addEventListener(Events.ON_OK, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doCerca();
			}});
    }
	
	public void doCerca(){
		super.hParameters.clear();
		if(int_anno.getValue()==null){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.gridAccessi.annoObbigatorio"));
			return;
		}
		
		if(int_progr.getValue()==null && (n_cartella.getValue() == null || n_cartella.getValue().isEmpty())){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.gridAccessi.cartellaObbigatoria"));
			return;			
		}
		
		super.hParameters.put("anno", int_anno.getValue().toString());
		if(int_progr.getValue()!=null){
			super.hParameters.put("int_contatore", int_progr.getValue().toString());
//			currentBean = new IntervEJB();
//		}else{
//			currentBean = new ContIntervEJB();
		}
		if(n_cartella.getValue() != null && !n_cartella.getValue().isEmpty()){
			super.hParameters.put("cartella", n_cartella.getValue());
		}
		
		if(JCariTextFieldProv.getValue()!=null && !JCariTextFieldProv.getValue().isEmpty()){
			super.hParameters.put("prov", JCariTextFieldProv.getValue());
		}
		if(!tipoOperatore.isEmpty()){
			super.hParameters.put("tipo", tipoOperatore);
		}
		
		if(dataDa.getValue()!= null)
			super.hParameters.put(dataDa.getDb_name(), dataDa.getValueForIsas());
		if(dataA.getValue()!= null)
			super.hParameters.put(dataA.getDb_name(), dataA.getValueForIsas());
		if(cod_operatore.getValue()!= null)
			super.hParameters.put(cod_operatore.getDb_name(), cod_operatore.getValue());
		
		doRefresh();
		if(caribellb.getItemCount()>0){
			btn_print.setDisabled(false);
		}
	}

	public void doStampa() {		
		String punto = MIONOME + "printAction_actionPerformed ";
		//recupero parametri
		String user = CaribelSessionManager.getInstance().getMyLogin().getUser();
		String passwd = CaribelSessionManager.getInstance().getMyLogin().getPassword();
		String anno = (String) int_anno.getValue().toString();
		String cartella = (String) n_cartella.getValue();
		//invocazione alla servlet

		String dataInizio = dataDa.getValueForIsas();
		String dataFine = dataA.getValueForIsas();

		if (periodoDate()) {
			logger.info(punto + "\n Periodo valido \n");
		} else {
			logger.info(punto + "\n Periodo NON valido \n");
			return;
		}

		String servlet = "";
		servlet = getProfile().getStringFromProfile("fop") + "?EJB=SINS_FOCONTINTERV&USER=" + user + "&ID509=" + passwd + "&METHOD=query_continterv"
				+ "&anno=" + anno + "&cartella=" + cartella + "&dtInizio=" + dataInizio + "&dtFine=" + dataFine + "&codOperatore="
				+ cod_operatore.getValue() + "&REPORT=continterv.fo";
		logger.info("percorso servlet FoContInterv" + servlet);
		
		it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self, servlet);
	}

	public boolean periodoDate() {
		String punto = MIONOME + "periodoDate ";
		logger.info(punto + " Controllo date");
		boolean periodoOk = true;

		String dataInizio = "";
		String dataFine = "";
		String anno = int_anno.getValue().toString();
		dataInizio = dataDa.getValueForIsas();
		dataFine = dataA.getValueForIsas();

		if (dataInizio != null && dataInizio.trim().length() >= 10) {
			logger.info(punto + "data corretta");
			if (!(anno.trim().length() >= 4 && getAnno(dataInizio).equals(anno))) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.gridAccessi.coerenzaAnnoDataDa", new String[]{anno}));
				dataDa.focus();
				periodoOk = false;
				return periodoOk;
			}
		} else {
			logger.info(punto + " data inizio non specificata ");
		}

		if (dataFine != null && dataFine.trim().length() >= 10) {
			logger.info(punto + "data corretta");
			if (!(anno.trim().length() >= 4 && getAnno(dataFine).equals(anno))) {
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.gridAccessi.coerenzaAnnoDataA", new String[]{anno}));
				dataA.focus();
				periodoOk = false;
				return periodoOk;
			}
		} else {
			logger.info(punto + " data fine non specificata ");
		}

		if (dataInizio.trim().length() >= 10 && dataFine.trim().length() >= 10) {
			int ris = this.ControlloData(dataInizio, dataFine);
			if (ris != 1) {
				new it.pisa.caribel.swing2.cariInfoDialog(null, "La data inizio è antecedente alla data fine", "Attenzione!").show();
				periodoOk = false;
			} else {
				logger.info(punto + "\n le date sono comprese nel perido");
				periodoOk = true;
			}
		} else {
			logger.info(punto + "\n non sono state inserite le due data dataInizio>" + dataInizio + "< dataFine>" + dataFine + "<\n");
		}
		logger.info(punto + "Periodo inserito >" + periodoOk + "<\n");

		return periodoOk;
	}

	private String getAnno(String data) {
		String punto = MIONOME + "getAnno ";
		String anno = "";
		if (data != null && data.length() >= 10) {
			anno = data.substring(6, 10);
		}
		logger.info(punto + "anno>" + anno + "<");
		return anno;
	}

	public int ControlloData(String dataold, String datanew) {
		// controlla se dataold < datanew

		//preparazione primo array

		int[] datavecchia = new int[3];

		Integer giorno = new Integer(dataold.substring(0, 2));
		datavecchia[0] = giorno.intValue();
		Integer mese = new Integer(dataold.substring(3, 5));
		datavecchia[1] = mese.intValue();
		Integer anno = new Integer(dataold.substring(6, 10));
		datavecchia[2] = anno.intValue();

		//preparazione secondo array

		int[] datanuova = new int[3];

		Integer day = new Integer(datanew.substring(0, 2));
		datanuova[0] = day.intValue();
		Integer mounth = new Integer(datanew.substring(3, 5));
		datanuova[1] = mounth.intValue();
		Integer year = new Integer(datanew.substring(6, 10));
		datanuova[2] = year.intValue();

		//confronto anno
		if (datanuova[2] < datavecchia[2])
			return -1;
		else if (datanuova[2] == datavecchia[2])
			//confronto mes
			if (datavecchia[1] > datanuova[1])
				return -1;
			else if (datanuova[1] == datavecchia[1])
				//confronto giorno
				if (datanuova[0] < datavecchia[0])
					return -1;
		return 1;	
	}
}

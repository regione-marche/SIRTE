package it.caribel.app.sinssnt.controllers.scheda_fim;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.nuovi.SchedaFIMEJB;
import it.caribel.app.sinssnt.controllers.contattoFisioterapico.ContattoFisioFormCtrl;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JLabel;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

public class FimFormCtrl extends CaribelFormCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "SKICF";
	private SchedaFIMEJB myEJB = new SchedaFIMEJB();
	
	public static final String myPathFormZul = "/web/ui/sinssnt/scheda_fim/scheda_fim.zul";
	
	private CaribelIntbox key_cartella;
	private CaribelIntbox key_progr;
	private CaribelDatebox key_data;
	private CaribelTextbox JLabelAssistito;
	
	Date dtApertura = null;
	String nCartella = "";

	public static final String CTS_COGNOME_NOME_ASSISTITO = "cgn_nome_assistito";

	private CaribelCombobox combo_fase; 
	private CaribelCombobox combo_ictus; 
	private CaribelCombobox combo_abita; 
	private CaribelCombobox combo_femore; 
	private CaribelCombobox combo_lavoro; 
	private CaribelCombobox combo_comorbilita; 
	
	private CaribelIntbox fim_nutrirsi;
	private CaribelIntbox fim_rassettarsi;
	private CaribelIntbox fim_lavarsi;
	private CaribelIntbox fim_vestirsi_su;
	private CaribelIntbox fim_vestirsi_giu;
	private CaribelIntbox fim_igiene;
	private CaribelIntbox fim_vescica;
	private CaribelIntbox fim_alvo;
	private CaribelIntbox fim_letto_sedia;
	private CaribelIntbox fim_wc;
	private CaribelIntbox fim_vasca_doccia;
	private CaribelIntbox fim_cammino;
	private CaribelIntbox fim_scale;
	private CaribelIntbox fim_comprensione;
	private CaribelIntbox fim_espressione;
	private CaribelIntbox fim_rapporto;
	private CaribelIntbox fim_problemi;
	private CaribelIntbox fim_memoria;

	private CaribelIntbox fim_punteggio;
	
	@Override
//	public void doInitForm() {
//		try {
//			super.initCaribelFormCtrl(myEJB, myKeyPermission);
//			
//			String assistito = ISASUtil.getValoreStringa((HashMap<String, String>) arg, CTS_COGNOME_NOME_ASSISTITO);
//			logger.debug("ARG:"+arg.toString());
//			nCartella=((Integer) arg.get(Costanti.N_CARTELLA)).toString();
//			dtApertura = (Date)arg.get(ContattoFisioFormCtrl.CTS_DATA_APERTURA);
//			key_data.setValue(dtApertura);
//			key_cartella.setText(nCartella);
//			hParameters.put(Costanti.N_CARTELLA, nCartella);
//			hParameters.put(key_data.getDb_name(), key_data.getValueForIsas());
//			doPopulateCombobox();
//			doQueryKeySuEJB();
//			doWriteBeanToComponents();
//			combo_scheda.setVisible(false);
//			JLabelAssistito.setText(assistito);
//			if(!isInInsert()){
//				doCalcolaPunteggio();
//				key_progr.setDisabled(true);
//			}
//		} catch (Exception e) {
//			doShowException(e);
//		}
//	}

	public void doInitForm() {
		try {

			logger.debug("inizio ");
			super.initCaribelFormCtrl(myEJB, myKeyPermission);
			doPopulateCombobox();
			
			if (dbrFromList != null) {
				hParameters.put(Costanti.N_CARTELLA, ((Integer) dbrFromList.get(Costanti.N_CARTELLA)).toString());
				hParameters.put(key_data.getDb_name(), ((java.sql.Date) dbrFromList.get("fim_spr_data")).toString());
				hParameters.put(key_progr.getDb_name(), ((Integer) dbrFromList.get("fim_progr")).toString());
				doQueryKeySuEJB();
				doWriteBeanToComponents();
			}else{
				if(super.caribelContainerCtrl!=null){
					String Data = ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore, "skf_data");
					super.hParameters.put(Costanti.N_CARTELLA, super.caribelContainerCtrl.hashChiaveValore.get(Costanti.N_CARTELLA));
					super.hParameters.put("fim_spr_data", ISASUtil.getValoreStringa(super.caribelContainerCtrl.hashChiaveValore,"fim_spr_data"));
					key_cartella.setValue((Integer)super.caribelContainerCtrl.hashChiaveValore.get(Costanti.N_CARTELLA));
					key_data.setValue(UtilForBinding.getDateFromIsas(Data));
					key_progr.focus();					
				}
			}
			JLabelAssistito.setText(super.caribelContainerCtrl.hashChiaveValore.get(Costanti.ASSISTITO_COGNOME)+" "+
									super.caribelContainerCtrl.hashChiaveValore.get(Costanti.ASSISTITO_NOME));
			if(isInUpdate()){
				doCalcolaPunteggio();
				key_progr.setDisabled(true);
			}
		} catch (Exception e) {
			doShowException(e);
		}
	}
	public void onClick$btn_punteggio(Event event) throws Exception	{
		try{
			doCalcolaPunteggio();
		}catch(Exception ex){
			doShowException(ex);
		}
	}
	
	public void doCalcolaPunteggio() throws Exception {
		Component VCampi[] = { fim_nutrirsi, fim_rassettarsi, fim_lavarsi, fim_vestirsi_su, 
							   fim_vestirsi_giu, fim_igiene, fim_vescica, fim_alvo, 
							   fim_letto_sedia, fim_wc, fim_vasca_doccia, fim_cammino,
							   fim_scale, fim_comprensione, fim_espressione, fim_rapporto,
							   fim_problemi, fim_memoria };
        int totale = 0;
        for(int i = 0; i < 18; i++)
        	if(!((CaribelIntbox)VCampi[i]).getText().equals("")){
        		int num = Integer.parseInt(((CaribelIntbox)VCampi[i]).getText());
                totale += num;
            }
            fim_punteggio.setText(""+totale);
	}
	
	
	
protected boolean doValidateForm() throws Exception {
	return true;
}

private void doPopulateCombobox() throws Exception {
	String punto = "doPopulateCombobox \n";
	String[] combo;
	logger.debug(punto + "");
	Hashtable<String, CaribelCombobox> h_xCBdaTabBase = new Hashtable<String, CaribelCombobox>(); // x le comboBox da caricare
	Hashtable<String, Label> h_xLabdaTabBase = new Hashtable<String, Label>(); // x le Label da caricare

	Hashtable<String, String> h_xAllCB = new Hashtable<String, String>();
	h_xAllCB.put("combo_tipostru_V", "S"); // x notificare che serve 1 riga vuota
	h_xAllCB.put("combo_tipostru_C", "S"); // x notificare che serve anche il codReg
	combo=new String[]{"FTIPRI","FMEDPR","FCONDG",
			"FCONDC","FDEAMB","FAUTON","FDECPA","FDISAB",
			"FTEMPO","FCAREG","FCHIUS", "MOTIVO"};
	h_xCBdaTabBase.put("FASEVA", combo_fase);
	h_xCBdaTabBase.put("SITABI", combo_abita);
	h_xCBdaTabBase.put("FICTUS", combo_ictus);
	h_xCBdaTabBase.put("FFEMOR", combo_femore);
	h_xCBdaTabBase.put("FCOLAV", combo_lavoro);
	h_xCBdaTabBase.put("FCMORB", combo_comorbilita);
	
	CaribelComboRepository.comboPreLoadAll(new TabVociEJB(), "query_Allcombo", h_xAllCB, h_xCBdaTabBase, h_xLabdaTabBase, "tab_val",
			"tab_descrizione", false);
}
}

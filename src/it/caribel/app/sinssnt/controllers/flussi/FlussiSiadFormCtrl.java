package it.caribel.app.sinssnt.controllers.flussi;


import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.caribel.app.sinssnt.bean.RLPresaCaricoEJB;
import it.caribel.app.sinssnt.bean.SclValutazioneEJB;
import it.caribel.app.common.ejb.StacivEJB;
import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.common.ejb.TabuslEJB;
import it.caribel.app.sinssnt.bean.corretti.RLSkPuacEJB;
import it.caribel.app.sinssnt.controllers.scaleValutazione.SIAD.ValutazioneSIADFormCtrl;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForBinding;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Window;

public class FlussiSiadFormCtrl extends CaribelFormCtrl{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/flussi/flussiSiadForm.zul";
	private String myKeyPermission = "RA_PRESA";
	private RLPresaCaricoEJB myEJB = new RLPresaCaricoEJB();
	protected Window flussiSiadForm;

	private CaribelIntbox n_cartella;
	private CaribelIntbox id_caso;
	private CaribelDatebox pr_data;
	


	private CaribelDatebox dt_presa_carico;

	private Button btn_valutazione;
	private CaribelCombobox cbx_stato_civile;
	private CaribelCombobox richiedente;
	private CaribelCombobox asl_residenza;
	private CaribelIntbox num_fam;
	private CaribelRadiogroup badante;
	private CaribelCombobox cbx_motivo;
	private int check_save_step = 0;
	private String data_valutazione;
	private boolean esisteValutazione = false;
	private Tabpanels tabpanels_flussi_siad;
	private Tabpanel conclusione;
	private Tabpanel rivalutazione;
	private Tabpanel sospensione;
	
	
	
	
	
	
	@Override
	public void doInitForm() {
		

		super.initCaribelFormCtrl(myEJB, myKeyPermission);
		
		
		try {
			doPopulateKeys();
			doPopulateCombobox();
			doMakeControl();			
			doQueryKeySuEJB();
			doWriteBeanToComponents();
			doMakeControlAfterRead();
			doFreezeForm();
									
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	
	private void doMakeControlAfterRead() {
		if (this.isInInsert()){
			conclusione.getLinkedTab().setDisabled(true);
			rivalutazione.getLinkedTab().setDisabled(true);
			sospensione.getLinkedTab().setDisabled(true);
		}
		else {
			conclusione.getLinkedTab().setDisabled(false);
			rivalutazione.getLinkedTab().setDisabled(false);
			sospensione.getLinkedTab().setDisabled(false);
		}
	}


	private void doMakeControl() throws Exception {
		Hashtable datiCaso = (Hashtable)super.caribelContainerCtrl.hashChiaveValore.clone();
		datiCaso.put("tempo_t","0");
        this.data_valutazione = invokeGenericSuEJB(new SclValutazioneEJB(), datiCaso, "leggiLastValutazione").toString();

        if (!this.data_valutazione.equals("")){
          esisteValutazione = true;
          super.caribelContainerCtrl.hashChiaveValore.put("dt_valutazione", data_valutazione);
        }

    	Hashtable htAssAnagrafica = new Hashtable();
          
    	htAssAnagrafica.put("ass_anagr_cognome",(String)super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ASSISTITO_COGNOME));
    	htAssAnagrafica.put("ass_anagr_nome",(String)super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ASSISTITO_NOME));
    	htAssAnagrafica.put("ass_anagr_data_nascita", super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.DATA_NASC));
    	htAssAnagrafica.put("ass_anagr_com_nascita", (String)super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.COD_COM_NASC));

    	htAssAnagrafica.put("n_cartella", (String)n_cartella.getText());
    	
       Hashtable<String,String> htAssAnagrResult = null;
	try {
		htAssAnagrResult = (Hashtable<String, String>)invokeGenericSuEJB(new RLSkPuacEJB(), htAssAnagrafica, "query_getAssAnagrAge");
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

       if ((htAssAnagrResult != null) && (!htAssAnagrResult.isEmpty()))
         if(isInInsert()){
        	 String segnalante = (String)htAssAnagrResult.get("arrivato");
             if ((segnalante != null) && (!segnalante.trim().equals(""))){
               richiedente.setValue(segnalante);
               logger.debug("segnalante: " + segnalante);
             }

             String motivo = (String)htAssAnagrResult.get("motivo");
             if ((motivo != null) && (!motivo.trim().equals(""))){
               cbx_motivo.setValue(motivo);
               logger.debug("motivo " + motivo);
              }
          }else{
             //si disabilitano le combo
        	  richiedente.setDisabled(true);
             cbx_motivo.setDisabled(true);
         
         }
       
       if(this.isInInsert()){
       Hashtable<String,String> hCart = new Hashtable<String,String>();
       hCart.put("n_cartella", n_cartella.getValue().toString());
       hCart = (Hashtable)invokeGenericSuEJB(new CartellaEJB(), hCart, "query_getDatiFromAnagraC");
       if(hCart.get("usl") != null){
         String codAsl = getProfile().getStringFromProfile("codice_regione") + hCart.get("usl").toString();
         if (asl_residenza.getItemByValue(codAsl)!=null)
         asl_residenza.setSelectedValue(codAsl);
       }
       }
       // non si permette modifica della dataPresaCarico
       if (isPreCaGiaEstratta())
           this.dt_presa_carico.setDisabled(true);
           
		
	}


	private void doPopulateKeys() throws WrongValueException, ParseException {
		if (super.caribelContainerCtrl != null
				&& super.caribelContainerCtrl.hashChiaveValore != null
				&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA) != null
				&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.PR_DATA) != null
				&& super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ID_CASO) != null) {
		
				n_cartella.setValue(new Integer(super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA).toString()));
				id_caso.setValue(new Integer(super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.ID_CASO).toString()));
				pr_data.setValue(UtilForBinding.getDateFromIsas(super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.PR_DATA).toString()));
				hParameters.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
				hParameters.put(CostantiSinssntW.ID_CASO, id_caso.getValue());
				hParameters.put(CostantiSinssntW.PR_DATA, pr_data.getValueForIsas());
		}
	}

	private void doPopulateCombobox() throws Exception {
		Hashtable<String, String> asl_residenza_hash = new Hashtable<String, String>();
		asl_residenza_hash.put("cd_reg", getProfile().getStringFromProfile("codice_regione"));
		CaribelComboRepository.comboPreLoad("flussi_siad_asl_residenza",
				new TabuslEJB(), "queryCombo", asl_residenza_hash, asl_residenza, null,
				"cd_usl", "desusl", false);
		
		Hashtable<String, String> motivo_hash = new Hashtable<String, String>();
		motivo_hash.put("tab_cod", "MOTIVO");
		CaribelComboRepository.comboPreLoad("flussi_siad_motivo",
				new TabVociEJB(), "query", motivo_hash, cbx_motivo, null,
				"tab_val", "tab_descrizione", false);
		
	    
		Hashtable<String, String> stato_civile_hash = new Hashtable<String, String>();
		stato_civile_hash.put("codice", "STACIV");
		CaribelComboRepository.comboPreLoad("flussi_siad_stato_civile",
				new StacivEJB(), "query", stato_civile_hash, cbx_stato_civile, null,
				"cd_civ", "desciv", false);
		
		Hashtable<String, String> richiedente_hash = new Hashtable<String, String>();
		richiedente_hash.put("tab_cod", "FMRICH");
		CaribelComboRepository.comboPreLoad("flussi_siad_richiedente",
				new TabVociEJB(), "query", richiedente_hash, richiedente, null,
				"tab_val", "tab_descrizione", false);
		
	}

		
	   // 31/01/13: cntrl che la presaCarico NON sia stata giÃ  estratta per i FLUSSI SIAD
    private boolean isPreCaGiaEstratta() throws Exception
    {
        Hashtable<String,String> h_dati = new Hashtable<String,String>();
        h_dati.put("n_cartella", this.n_cartella.getValue().toString());
        h_dati.put("pr_data", this.pr_data.getValueForIsas());
        h_dati.put("id_caso", this.id_caso.getValue().toString());

        int risu = ((Integer)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_dati, "checkPrecaCarEstratta")).intValue();
        if (risu < 0) {
            logger.debug("FlussiSiadForm: isPreCaGiaEstratta(): ERRORE nella lettura di CASO");
            return false;
        }
        return (risu == 1);
    }
	@Override
		public boolean doValidateForm() throws Exception {
			
			boolean canSave = false;			
			switch(check_save_step){
			case 0: controlloDatiSalvataggio();break;
			case 1: settaDati(); canSave = true; check_save_step = 0;break;
			}		
			return canSave;
	
	}
	
	private void settaDati() {
		// TODO Auto-generated method stub
		
	}

	
	private boolean controlloDatiSalvataggio() throws Exception {
		
		 if(!esisteValutazione){
			 Hashtable datiCaso = (Hashtable)super.caribelContainerCtrl.hashChiaveValore.clone();
				datiCaso.put("tempo_t","0");
		        this.data_valutazione = invokeGenericSuEJB(new SclValutazioneEJB(), datiCaso, "leggiLastValutazione").toString();

		        if (!this.data_valutazione.equals(""))
		          esisteValutazione = true;
	        }

		 if ((dt_presa_carico.getValue()==null) && (esisteValutazione)) {
	        	Messagebox.show(
	    				Labels.getLabel("FlussiSiadForm.msg.ValutazioneNoPC"),
	    				Labels.getLabel("messagebox.attention"),
	    				Messagebox.OK,
	    				Messagebox.EXCLAMATION);  
	            return new Boolean(false);
	        }
		 if ((dt_presa_carico.getValue()!=null) && (!esisteValutazione)) {
	        	Messagebox.show(
	    				Labels.getLabel("FlussiSiadForm.msg.PCNoValutazione"),
	    				Labels.getLabel("messagebox.attention"),
	    				Messagebox.OK,
	    				Messagebox.EXCLAMATION);  
	            return new Boolean(false);
	        }
	       
	        // 05/10/12: cntrl che la dataPresaCarico non ricada nel periodo di altri CASI
	        if (!checkDtPC()) {
	        	Messagebox.show(
	    				Labels.getLabel("FlussiSiadForm.msg.EsisteCasoPrecedente"),
	    				Labels.getLabel("messagebox.attention"),
	    				Messagebox.OK,
	    				Messagebox.EXCLAMATION);  
	            return new Boolean(false);
	        }

	        // 31/01/13: cntrl che la dataPresaCarico sia <= della dataConclusione (eventualmente presente)
	        if (!checkDtPCDtConcl()) {
	        	Messagebox.show(
	    				Labels.getLabel("FlussiSiadForm.msg.ChiusuraPrecedentePC"),
	    				Labels.getLabel("messagebox.attention"),
	    				Messagebox.OK,
	    				Messagebox.EXCLAMATION);  
	            return new Boolean(false);
	        }


	      if (!dateIsInSkInfDateRange()) //gb 09/07/07
	        return new Boolean(false);

	    

	    

	       	this.check_save_step = 1;
	       	doSaveForm();
	       	this.check_save_step = 0;	       	
	        return new Boolean(true);
	}
	  private boolean dateIsInSkInfDateRange() throws ParseException
	    {
	    boolean ret = true;
	    Date DataToCompare = dt_presa_carico.getValue();

	    Date ski_data_apertura = UtilForBinding.getDateFromIsas((super.caribelContainerCtrl.hashChiaveValore.get("ski_data_apertura").toString()));
	    Date ski_data_uscita = UtilForBinding.getDateFromIsas((super.caribelContainerCtrl.hashChiaveValore.get("ski_data_uscita").toString()));

	    if (DataToCompare.before(ski_data_apertura)) ret = false;
	    if (ski_data_uscita!=null && ski_data_uscita.before(DataToCompare)) ret = false;
	    
	    if (!ret) Messagebox.show(
				Labels.getLabel("FlussiSiadForm.msg.DataPcOutofRange"),
				Labels.getLabel("messagebox.attention"),
				Messagebox.OK,
				Messagebox.EXCLAMATION);  
         

	    return ret;
	    
	    }
	private boolean checkDtPC() throws Exception
    {
        if (this.dt_presa_carico.getValue() == null)
            return true;

        Hashtable<String,String> h_dati = new Hashtable<String,String>();
        h_dati.put("n_cartella", this.n_cartella.getValue().toString());
        h_dati.put("dt_presa_carico", this.dt_presa_carico.getValueForIsas());
        h_dati.put("id_caso", this.id_caso.getValue().toString());

        return ((Boolean)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_dati, "checkDtPresaCarCaso")).booleanValue();
    }

    // 31/01/13: cntrl che la dataPresaCarico sia <= della dataConclusione (eventualmente presente)
    private boolean checkDtPCDtConcl() throws Exception
    {
        if (this.dt_presa_carico.getValue() == null)
            return true;

        Hashtable<String,String> h_dati = new Hashtable<String,String>();
        h_dati.put("n_cartella", this.n_cartella.getValue().toString());
        h_dati.put("dt_presa_carico", this.dt_presa_carico.getValueForIsas());
        h_dati.put("id_caso", this.id_caso.getValue().toString());
        h_dati.put("pr_data", this.pr_data.getValueForIsas());
        
        int risu = ((Integer)invokeGenericSuEJB(new RLPresaCaricoEJB(), h_dati, "checkDtPreCaAndConclCaso")).intValue();
        if (risu < 0) {
            System.out.println("JFrameRAPresaCarico: checkDtPCDtConcl(): ERRORE nella lettura di CASO");
            return false;
        }
        return (risu == 1);
    }
	public void onValutazione(ForwardEvent e) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put(CostantiSinssntW.N_CARTELLA, n_cartella.getValue());
		map.put("caribelContainerCtrl", super.caribelContainerCtrl);
		map.put("statoI", new Integer(1));
		map.put("tempoT", "0");
		map.put("daFlussi", true);
				
		Executions.getCurrent().createComponents(ValutazioneSIADFormCtrl.myPathFormZul, self, map);
	}
	
	@Override
	public boolean doSaveForm() throws Exception{
		boolean ret = super.doSaveForm();
		doMakeControlAfterRead();
		doFreezeForm();
		return ret;
	}

	@Override
	protected void doDeleteForm() throws Exception {
		Hashtable h_delete = new Hashtable();
		h_delete.put(CostantiSinssntW.N_CARTELLA, hParameters.get(CostantiSinssntW.N_CARTELLA).toString());
		h_delete.put(CostantiSinssntW.PR_DATA, hParameters.get(CostantiSinssntW.PR_DATA).toString());
		h_delete.put(CostantiSinssntW.ID_CASO, hParameters.get(CostantiSinssntW.ID_CASO).toString());
		invokeSuEJB(myEJB, h_delete, "delete");
		currentIsasRecord=null;
		super.doUndoForm();
		super.doCheckPermission();
		doInitForm();
	}
	

}

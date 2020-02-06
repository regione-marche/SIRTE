package it.caribel.app.sinssnt.controllers.agenda;


import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.AgendaEditEJB;
import it.caribel.app.sinssnt.bean.AgendaRegistraEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMDiarioEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.util.CaribelComparator;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.procdate;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.zkforge.ckez.CKeditor;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.South;
import org.zkoss.zul.Toolbarbutton;

@SuppressWarnings({"rawtypes","unchecked"})
public class AgendaRegistraPopUpNewFormCtrl extends AgendaGenericPopupFormCtrl {

	private static final long serialVersionUID = 8110873716088864092L;
	public static final String myPathZul = "/web/ui/sinssnt/agenda/agendaRegistraPopupNewForm.zul";
	protected String keyPermission = "AG_REG";
	protected String keyPermissionSPO = "AG_SPO";

	private int stato=0;
	private int stato_form=0;
	private boolean salvato=false;
	protected Hashtable hashDapassare =new Hashtable();
	protected int ultimo_y=0;
	protected String dataOdierna=procdate.getitaDate();
	private Label lbl_selezionaPrestazioniErogate;
	private Listbox tablePrestazioni;
	private Button btn_confermaSelezione;
	private CaribelTextbox assistito;
	private CaribelDatebox data_diario;

	private AgendaEJB agendaEJB = new AgendaEJB();
	private AgendaRegistraEJB agendaRegistraEJB = new AgendaRegistraEJB();
	private AgendaEditEJB agendaEditEJB = new AgendaEditEJB();

	private Component myForm;
	private CKeditor testo;
	private boolean salvaDiario = false;
	protected Toolbarbutton btn_modifica = null;
	private String data_selezionata;
	private South diario;
	private AbstractComponent pianoAccessi;
	private RMDiarioEJB diarioEJB = new RMDiarioEJB();
	private Hashtable<String, Object> prestazioniDaPiano = new Hashtable<String, Object>();

	//gestione non erogato
	protected CaribelCombobox cbx_nonEseguito;
	protected CaribelCheckbox cb_nonEseguito;
	protected boolean salvabile = true;
	@Override
	public void doInitForm() {
		try {
			super.doInitForm();
			populateCombobox();
			
			if(btn_modifica==null){
				btn_modifica = new Toolbarbutton();
				btn_modifica.setImage("~./zul/img/transfer24x24.png");
				btn_modifica.setTooltiptext(Labels.getLabel("agenda.btn.trasferisci.ttt"));
				btn_save.setTooltiptext(Labels.getLabel("agenda.btn.registra"));
				btn_save.getParent().insertBefore(btn_modifica, btn_undo);
				btn_modifica.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					public void onEvent(Event event){
						try{
							doTrasferisciModifica(event);
						}catch(Exception e){
							doShowException(e);
						}
					}});
			}

			CaribelListModel emptyModel = new CaribelListModel();
			emptyModel.setMultiple(true);
			tablePrestazioni.setModel(emptyModel);

			logger.info("AgendaRegistraPopUpFormCtrl/Inizializzazione arg in input: " + arg.toString());
			stato_form = (Integer) arg.get("stato_interv");
			if (stato_form!=0){
				lbl_selezionaPrestazioniErogate.setValue(Labels.getLabel("agendaRegistraPopup.prestazioniErogate"));
				UtilForComponents.disableListBox(tablePrestazioni, true);
				UtilForComponents.disableListBox(tableGrigliaPrestazioni, true);
				UtilForBinding.setComponentReadOnly(myForm,true);
				btn_confermaSelezione.setDisabled(true);
			}
			
			hashDapassare.putAll(arg);
			data_selezionata = formDate(hashDapassare.get("ag_data"), "gg-mm-aaaa");
			String msg = Labels.getLabel("agendaRegistraPopup.assistito", 
					new String[]{(String) hashDapassare.get("assistito"), 
					data_selezionata,
					Labels.getLabel("agenda.common.mattinaPomeriggio." + hashDapassare.get("ag_orario"))});
			assistito.setValue(msg);

			String w_stato=(String)arg.get("stato");
			if(w_stato.equals("UPDATE")){
				doCheckPermission();//in stato update he devo fare?????
			}

			execSelect();
			if (stato_form!=0){
				this.setReadOnly(true);
				lbl_selezionaPrestazioniErogate.setValue(Labels.getLabel("agendaRegistraPopup.prestazioniErogate"));
			} else {
				doCheckPermission();
			}
			if(ControlloData(data_selezionata,dataOdierna)<0){
				UtilForBinding.setComponentReadOnly(diario,true);
				diario.setOpen(false);
			}
			execSelectPrestazioni();
			data_diario.setValue(UtilForBinding.getDateFromIsas((String) hashDapassare.get("ag_data")));
			execSelectDiario();
			
			String ag_stato = (String) arg.get("ag_stato");
			if(!ag_stato.equals("0")){
				UtilForComponents.disableListBox(tablePrestazioni, true);
				UtilForComponents.disableListBox(tableGrigliaPrestazioni, true);
				UtilForBinding.setComponentReadOnly(self,true);
				btn_confermaSelezione.setDisabled(true);
			}
			String motivo = (String) arg.get("nn_eseguito_motivo");
			if(motivo!=null && ! motivo.isEmpty()){
				cbx_nonEseguito.setSelectedValue(motivo);
				cb_nonEseguito.setChecked(true);
				Events.sendEvent(Events.ON_CHECK, cb_nonEseguito, true);
				UtilForBinding.setComponentReadOnly(self, true);
				setBtnSaveDisabled(true);
				salvabile  = false;
			}
			//			execSelectPiano();
		} catch (Exception e) {
			doShowException(e);
		}

	}

	private void populateCombobox() {
        Hashtable<String, CaribelCombobox> hCombo=new Hashtable<String, CaribelCombobox>();
        hCombo.put("AG_NN_ES",cbx_nonEseguito);
        TabVociEJB tabVociEJB = new TabVociEJB();
        try{
        	CaribelComboRepository.comboPreLoadAll(tabVociEJB, "query_Allcombo", new Hashtable<String, Object>(), hCombo, new Hashtable<String, Label>(), Costanti.TABVOCI_TAB_VAL, Costanti.TABVOCI_TAB_DESCRIZIONE, false);
        }catch(Exception e){
          logger.error("caricamento delle combo fallito!!!!", e);
        }
        cbx_nonEseguito.setDisabled(true);
	}
	
	public void onCheck$cb_nonEseguito(Event evt){
		UtilForBinding.setComponentReadOnly(cbx_nonEseguito, !cb_nonEseguito.isChecked());
		cbx_nonEseguito.setRequired(cb_nonEseguito.isChecked());
		cbx_nonEseguito.invalidate();
	}

	private void execSelectDiario() throws Exception {
		Hashtable h = new Hashtable<String, String>();		
		h.put("n_cartella", arg.get("ag_cartella"));
		h.put("n_contatto", arg.get("ag_contatto"));
		h.put("tipo_operatore", arg.get("tipo_operatore"));
		//		h.put("cod_obbiettivo", arg.get("cod_obbiettivo"));
		//		h.put("n_intervento", arg.get("n_intervento"));
		h.put("dadata", arg.get("ag_data"));
		h.put("adata", arg.get("ag_data"));

		Vector<ISASRecord> obj = diarioEJB.query(CaribelSessionManager.getInstance().getMyLogin(), h );
		if(obj != null && obj.size()>0){
			ISASRecord diar = obj.get(0);
			h.put("progr_inse", diar.get("progr_inse")+"");
			h.put("progr_modi", diar.get("progr_modi")+"");
			diar = diarioEJB.queryKey(CaribelSessionManager.getInstance().getMyLogin(), h);
			UtilForBinding.bindDataToComponent(logger, diar, diario);
		}
	}

	protected boolean doTrasferisciModifica(Event event) throws Exception {
		if(cb_nonEseguito.isChecked()){
			UtilForUI.standardExclamation(Labels.getLabel("agendaRegistraNewPopup.msg.trasferisciConCheckNonEseguito"));
			return false;
		}
		Vector agg=new Vector();
		Vector rim=new Vector();
		for (Iterator<Listitem> iterator = tablePrestazioni.getItems().iterator(); iterator.hasNext();) {
			Listitem item = (Listitem) iterator.next();
			Object itemGrid = item.getAttribute("ht_from_grid");
			String codicePrestazione = (String) ((Hashtable)itemGrid).get("ap_prest_cod");
			if(item.isSelected()){
				agg.add(codicePrestazione);
			}else{
				rim.add(codicePrestazione);
			}
		}

		if(rim.size()>0 && agg.size()>0){
			String messaggio = Labels.getLabel("agendaModificaPopup.msg.pianificareErimuovere", new String[]{agg.toString(), rim.toString()});
			UtilForUI.standardYesOrNo(messaggio, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception{
					if (Messagebox.ON_YES.equals(event.getName())){	
						doTrasferisciModificaEffettivo(event);
					}
				}
			} );
			return false;
		}

		return doTrasferisciModificaEffettivo(event);
	}


	protected boolean doTrasferisciModificaEffettivo(Event event) throws Exception {
		//			int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
		//			if(num==0){
		//				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
		//				return false;
		//			}
		Vector v=new Vector();
		Vector q=new Vector();
		Vector f=new Vector();
		//			
		for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			v.addElement((String) ((Hashtable)itemGrid).get("ap_prest_cod"));
			Integer qta = (Integer) ((Hashtable)itemGrid).get("ap_prest_qta");//((String)cariObjectTableModelPrestaz.getValueAt(i,col_qta));
			if(qta == null)q.addElement("1");
			else q.addElement(qta.toString());
			f.addElement(((Hashtable)itemGrid).get("ap_alert"));
		}
		logger.trace("vettore="+v.toString());

		if(v.size()==0){
			if(((String)hashDapassare.get("stato")).equals("UPDATE")){  
				UtilForUI.standardYesOrNo(Labels.getLabel("agendaModificaPopup.rimuovereTutte"), new EventListener<Event>(){
					public void onEvent(Event event) throws Exception{
						if (Messagebox.ON_YES.equals(event.getName())){	
							Object o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "cancella_intervCella"); //db.deleteInterv(hashDapassare);
							((AgendaGenericFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
							self.detach();
						}
					}
				} );
				return false;
			}
		}

		this.hashDapassare.put("prestazioni",v);
		this.hashDapassare.put("quantita",q);
		this.hashDapassare.put("frequenza",f);

		logger.trace("Registro hashtable="+hashDapassare.toString());
		Object o=null;

		System.out.println("doSaveForm/hashDapassare: " + hashDapassare.toString());
		if(((String)hashDapassare.get("stato")).equals("UPDATE")){
			o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_prestazCella"); //db.registraPrest(hashDapassare);
		}else{
			o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_intervCella");//db.registraInterv(hashDapassare);
		}
		((AgendaGenericFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
		self.detach();

		return true;
	}

	protected void doCheckPermission() throws Exception{
		setBtnSaveDisabled(ControlloData(data_selezionata,dataOdierna)<0);	
		if(this.currentIsasRecord!=null)
			setBtnDeleteDisabled(false);
		else
			setBtnDeleteDisabled(true);

		if(keyPermission!=null && !keyPermission.equals("")){
			boolean isSav = isSavable();
			if(!isSav){
				UtilForBinding.setComponentReadOnly(self,true);
				setBtnSaveDisabled(true);
			}

			boolean isDel = isDeletable();
			if(!isDel){
				setBtnDeleteDisabled(true);
			}else{
				setBtnDeleteDisabled(false);
			}
		}
		if(keyPermissionSPO!=null  && !keyPermissionSPO.equals("")){
			ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
			boolean canIModify = iu.canIUse(keyPermissionSPO,"MODI");
			boolean canIInsert = (currentIsasRecord == null) && iu.canIUse(keyPermission,"INSE");
			boolean canIWrite = (currentIsasRecord == null) || iu.canIWrite(currentIsasRecord);
			boolean isMod = (canIModify && canIWrite && currentIsasRecord != null) || canIInsert;

			if(!isMod){
				UtilForBinding.setComponentReadOnly(self,!isMod);
				if(btn_modifica!=null){
					btn_modifica.setDisabled(!isMod);
					if(btn_modifica.getImage()!=null)
						btn_modifica.setImage("~./zul/img/transfer24x24.png"+(!isMod?"-dis":"" )+".png");
				}
			}
		}
	}

	private int execSelectPrestazioni() throws Exception{
		modelloPrestazioni = new CaribelListModel();
		modelloPrestazioni.setMultiple(true);
		tableGrigliaPrestazioni.setModel(modelloPrestazioni);

		execSelectPiano();

		//griglia prestazioni
		Hashtable h = new Hashtable();
		Vector griglia=new Vector();
		h.put("referente",hashDapassare.get("tipo_operatore"));
		h.put("JFrameAgendaRegistraPopUp","JFrameAgendaRegistraPopUp");
		//		h.put("JFrameAgendaModificaPopUp","JFrameAgendaModificaPopUp");
		logger.debug("Richieste le prestazioni per: " + h);
		Object obj=profile.getObject((String)hashDapassare.get("referente")+"_prestazioniRefer"); 
		if(obj != null){
			griglia=(Vector)obj;
			logger.trace("prestazioni caricate da profile " + griglia);
		}else{
			Object o= invokeGenericSuEJB(agendaEJB, h, "CaricaTabellaPrestazioni");//db.CaricaPrest(h);
			if (o!=null){
				logger.trace("prestazioni caricate da DBprofile" + o);
				if(((Vector)o).size()<=0){
					UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
					return -1;
				}
				else griglia=(Vector)o;
			}
			//logger.debug("REGISTR put profile"+(String)hashDapassare.get("referente")+"_prestazioniRefer");
			profile.putParameter((String)hashDapassare.get("referente")+"_prestazioniRefer",griglia);
		}
		CaribelComparator comp = new CaribelComparator("prest_des");
		comp.setAscendingOrder(true);
		Set<Hashtable> daPiano = new TreeSet<Hashtable>(comp);
		Enumeration en=griglia.elements();
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			Hashtable ht=is.getHashtable();
			String codicePrestazione = (String) ht.get("prest_cod");
			//logger.debug("carico pannello in hash=="+ht.toString());
			if(!prestazioniDaPiano.containsKey(codicePrestazione)){
				caricaTabPrestazioni(ht);
			}else{
				//				prestazioniDaPiano.put(codicePrestazione,ht);
				daPiano.add(ht);
			}
		}
		modelloPrestazioni.addAll(0, daPiano);
		modelloPrestazioni.setSelection(daPiano);
		//	        this.JCariTableGrigliaPrestaz.setColumnSizes();
		return 1;
	}

	//carico tabella prestaz
	private int caricaTabPrestazioni(Hashtable hash){
		modelloPrestazioni.add(hash);
		return 1;
	}//end caricaPrest

	private int execSelect() throws Exception {
		Object o= invokeGenericSuEJB(agendaEJB, hashDapassare, "query_agendapopup"); // db.selezAgenda(hashDapassare);
		Vector griglia=new Vector();
		if (o!=null){
			if(((Vector)o).size()<=0){
				//				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
				return -1;
			}
			else griglia=(Vector)o;}
		Enumeration en=griglia.elements();
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			Hashtable h=is.getHashtable();
			logger.trace("carico prestazioni della cella selezionata in tabella hash=="+h.toString());
			caricaRigaPrest(h);
		}
		return 0;
	}

	private int caricaRigaPrest(Hashtable hash){
		((CaribelListModel) tablePrestazioni.getModel()).add(hash);
		return 1;
	}//end caricaRigaPrest

	@Override
	protected boolean doValidateForm() throws Exception {
		String tmp = testo.getValue();
		//se non ho eseguito l'accesso diario e motivo sono obbligatori
		if(cb_nonEseguito.isChecked()){
			boolean valorizzati = UtilForComponents.testRequiredFieldsNoCariException(self) && (tmp!=null && !tmp.isEmpty());
			if(!valorizzati){
				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistraNewPopup.msg.checkNonEseguitoObbligatori"));
				return false;
			}
			int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
			if(num!=0){
				UtilForUI.standardExclamation(Labels.getLabel("agendaRegistraNewPopup.msg.noPrestazioniSelezionateSeNonEseguito"));
				return false;
			}
		}
		
		if(tmp == null || tmp.isEmpty()){
			salvaDiario  = false;
			return true;
		}else{
			salvaDiario  = true;
			UtilForComponents.testRequiredFields(myForm);
			return true;
		}
	}
	public int ControlloData (String dataold, String datanew)
	{
		// controlla se dataold < datanew
		//preparazione primo array
		int[] datavecchia= new int[3];

		Integer giorno = new Integer(dataold.substring(0,2));
		datavecchia[0]= giorno.intValue();
		Integer mese = new Integer(dataold.substring(3,5));
		datavecchia[1]= mese.intValue();
		Integer anno = new Integer(dataold.substring(6,10));
		datavecchia[2]= anno.intValue();
		//preparazione secondo array
		int[] datanuova= new int[3];

		Integer day = new Integer(datanew.substring(0,2));
		datanuova[0]= day.intValue();
		Integer mounth = new Integer(datanew.substring(3,5));
		datanuova[1]= mounth.intValue();
		Integer year = new Integer(datanew.substring(6,10));
		datanuova[2]= year.intValue();

		//confronto anno

		if (datanuova[2] < datavecchia[2])
		{
			//new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
			return -1;
		}
		else
			if (datanuova[2] == datavecchia[2])

				//confronto mes
				if (datavecchia[1] > datanuova[1])
				{
					//  new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
					return -1;
				}
				else
					if (datanuova[1] == datavecchia[1])
						//confronto giorno
						if (datanuova[0] < datavecchia[0])
						{
							//  new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
							return -1;
						}
		return 1;

	}
	public void onConfermaSelezione(Event event) throws Exception{
		int ret =this.caricaPrestazioniSelezionate();
		if(ret==-1){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniOperatore"));
			return;
		}
		else if (ret==-2){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
			return;
		}
		tableGrigliaPrestazioni.clearSelection();
	}
	public int caricaPrestazioniSelezionate(){
		ListModel<Object> modello = tableGrigliaPrestazioni.getModel();
		CaribelListModel modelloGriglia = (CaribelListModel) tablePrestazioni.getModel();
		if (modello.getSize() == 0)
			return -1;
		boolean trovato=false;
		for (Iterator iterator = tableGrigliaPrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			trovato=true ;
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			if (itemGrid instanceof Hashtable) {
				String cod_prest = (String) ((Hashtable) itemGrid).get("prest_cod");
				/*Controllo se e' gia' presente nella griglia un record con lo stesso codice prestazione
	                  ed eventualmente devo aumentare la quantità
				 */
				Hashtable hNuovo=new Hashtable() ;
				hNuovo.put("ap_prest_cod",cod_prest.trim());
				int num_riga = modelloGriglia.columnsContains(hNuovo);
				int quantita=1;//Integer.parseInt((String)modello.getValueAt(k, Col_quantita_nuova));
				String frequenza=" ";
				String ap_alert="";
				if (num_riga!=-1){
					//l'ho inserito devo aumentare la quantità
					//aggiorno tutto
					int quant_vecchia= (Integer) modelloGriglia.getFromRow(num_riga, "ap_prest_qta"); //Integer.parseInt(qta);
					quantita=quant_vecchia+quantita;
					frequenza=(String) modelloGriglia.getFromRow(num_riga, "frequenza");//cariObjectTableModelPrestaz.getValueAt(num_riga,cariObjectTableModelPrestaz.getColumnLocationByName("frequenza"));
					//da modifica
					ap_alert=(String) modelloGriglia.getFromRow(num_riga, "ap_alert");//cariObjectTableModelPrestaz.getValueAt(num_riga,cariObjectTableModelPrestaz.getColumnLocationByName("frequenza"));
				}
				//  Questo perché se la frequenza è una stringa vuota allora la concatenazione che segue
				//  produrrebbe una riga con due separatori contigui.
				if (frequenza.equals(""))
					frequenza=" ";
				//gb 18/09/07: fine *******
				Hashtable<String, Object> hDati = (Hashtable<String, Object>) itemGrid;
				hDati.put("ap_prest_qta", quantita);
				hDati.put("frequenza", frequenza);
				hDati.put("ap_alert", ap_alert); //da modifica
				hDati.put("ap_prest_cod", cod_prest);
				hDati.put("ap_prest_desc", (String) ((Hashtable) itemGrid).get("prest_des"));

				if (num_riga!=-1){
					modelloGriglia.remove(num_riga);
					modelloGriglia.add(num_riga, hDati);
					modelloGriglia.addToSelection(modelloGriglia.get(num_riga));
				}else{
					modelloGriglia.add(hDati);
					modelloGriglia.addToSelection(modelloGriglia.get(modelloGriglia.size()-1));
				}
			}
		}
		if(trovato)
			return 1;
		else
			return -2;
	}

	private	String formDate(Object dataI,String formato){
		return formDate(dataI,formato,false);//false =>	aaaammgg
	}

	private	String formDate(Object dataI,String formato,boolean it){
		String giorno="00";
		String mese="00";
		String anno="0000";
		String data="";
		if(dataI!=null){
			if(dataI instanceof java.sql.Date) data=((java.sql.Date)dataI).toString();
			else if(dataI instanceof String) data=(String)dataI;
		}
		//it=false =>	aaaammgg
		//it=true	=> ggmmaaaa
		if(data!=null	&& !data.equals("")){

			if(data.charAt(2)=='-' ||data.charAt(2)=='/')//gg-mm-aaaa
			{
				giorno=data.substring(0,2);
				mese=data.substring(3,5);
				anno=data.substring(6,10);
			}else	 if(data.charAt(4)=='-'	||data.charAt(4)=='/')//aaaa-mm-gg
			{
				giorno=data.substring(8,10);
				mese=data.substring(5,7);
				anno=data.substring(0,4);
			}else	//ggmmaaaa aaaammgg
			{	if(it==false)//aaaammgg
			{
				giorno=data.substring(6,8);
				mese=data.substring(4,6);
				anno=data.substring(0,4);
			}
			else//ggmmaaaa
			{
				giorno=data.substring(0,2);
				mese=data.substring(2,4);
				anno=data.substring(4,8);
			}
			}
		}
		if(formato.equals("gg-mm-aaaa"))
			return giorno+"-"+mese+"-"+anno;
		else if(formato.equals("aaaa-mm-gg"))
			return anno+"-"+mese+"-"+giorno;
		else if(formato.equals("ggmmaaaa"))
			return giorno+mese+anno;
		else if(formato.equals("aaaammgg"))
			return anno+mese+giorno;
		else if(formato.equals("gg/mm/aaaa"))
			return giorno+"/"+mese+"/"+anno;
		else if(formato.equals("aaaa/mm/gg"))
			return anno+"/"+mese+"/"+giorno;
		else return data;
	}

	@Override
	protected boolean doSaveForm() throws Exception {
		if(!isSavable()){
			Messagebox.show(
					Labels.getLabel("permissions.insufficient.on.doSaveForm"),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
			return false;
		}	
		if(!doValidateForm()){
			return false;
		}
		Vector v=new Vector();
		Vector q=new Vector();

		if(cb_nonEseguito.isChecked()){
			
			v.add(getProfile().getStringFromProfile(ManagerProfile.AG_NN_ES)+hashDapassare.get("tipo_operatore"));
			q.add("1");
		}else{
			int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
			if(num==0){
				UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
				return false;
			}
			//		int col_qta=cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_qta");
			//		int col_pr=cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_cod");
			//		
			for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
				Listitem litem = (Listitem) iterator.next();
				Object itemGrid = litem.getAttribute("ht_from_grid");
				v.addElement((String) ((Hashtable)itemGrid).get("ap_prest_cod"));
				//				v.addElement((String)cariObjectTableModelPrestaz.getValueAt(i,col_pr));
				Integer qta = (Integer) ((Hashtable)itemGrid).get("ap_prest_qta");//((String)cariObjectTableModelPrestaz.getValueAt(i,col_qta));
				if(qta == null)q.addElement("1");
				else q.addElement(qta.toString());
				//logger.debug("carico nel vettore="+check.JCariTextFieldCodPrest.getUnmaskedText());
			}
			//logger.debug("vettore="+v.toString());
		}
		
		this.hashDapassare.put("prestazioni",v);
		this.hashDapassare.put("quantita",q);
		//logger.debug("Registro hashtable="+hashDapassare.toString());
		Object o=null;

		logger.info("JActionButtonSave_actionPerformed/hashDapassare: " + hashDapassare.toString());
		if(salvaDiario){
			hashDapassare.putAll(UtilForBinding.getHashtableFromComponent(myForm));
			hashDapassare.put(CostantiSinssntW.N_CARTELLA, hashDapassare.get("ag_cartella"));
			hashDapassare.put(CostantiSinssntW.N_CONTATTO, hashDapassare.get("ag_contatto"));
		}

		o = invokeGenericSuEJB(agendaRegistraEJB, hashDapassare, "registra_prestazCella");//db.registraPrest(hashDapassare);

		//FIXME inserire i dati del diario
		((AgendaGenericFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
		self.detach();

		return true;
		//		padre.aggiornaTabella();
	}

	protected boolean doSaveModificaForm() throws Exception {
		int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
		if(num==0){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
			return false;
		}
		Vector v=new Vector();
		Vector q=new Vector();
		Vector f=new Vector();

		for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			v.addElement((String) ((Hashtable)itemGrid).get("ap_prest_cod"));
			Integer qta = (Integer) ((Hashtable)itemGrid).get("ap_prest_qta");//((String)cariObjectTableModelPrestaz.getValueAt(i,col_qta));
			if(qta == null)q.addElement("1");
			else q.addElement(qta.toString());
			f.addElement(((Hashtable)itemGrid).get("ap_alert"));
		}
		logger.trace("vettore="+v.toString());

		if(v.size()==0){
			if(((String)hashDapassare.get("stato")).equals("UPDATE")){  
				UtilForUI.standardYesOrNo(Labels.getLabel("agendaModificaPopup.rimuovereTutte"), new EventListener<Event>(){
					public void onEvent(Event event) throws Exception{
						if (Messagebox.ON_YES.equals(event.getName())){	
							Object o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "cancella_intervCella"); //db.deleteInterv(hashDapassare);
							((AgendaRegistraFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
						}
					}
				} );
				return false;
			}
		}

		this.hashDapassare.put("prestazioni",v);
		this.hashDapassare.put("quantita",q);
		this.hashDapassare.put("frequenza",f);

		Object o=null;

		logger.trace("doSaveModificaForm/hashDapassare: " + hashDapassare.toString());
		if(((String)hashDapassare.get("stato")).equals("UPDATE")){
			o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_prestazCella"); //db.registraPrest(hashDapassare);
		}else{
			o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_intervCella");//db.registraInterv(hashDapassare);
		}
		((AgendaGenericFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
		self.detach();

		return true;
	}

	private int execSelectPiano() throws Exception {
		if(prestazioniDaPiano.isEmpty()){
			prestazioniDaPiano= new Hashtable<String, Object>();
			Hashtable h = new Hashtable<String, String>();		
			h.put("pa_tipo_oper", arg.get("tipo_operatore"));
			h.put("n_cartella", arg.get("ag_cartella"));
			h.put("n_progetto", arg.get("ag_contatto"));
			h.put("cod_obbiettivo", arg.get("cod_obbiettivo"));
			h.put("n_intervento", arg.get("n_intervento"));
			h.put("data_agenda", arg.get("ag_data"));
			Object o= new PianoAssistEJB().query_pianoAcc(CaribelSessionManager.getInstance().getMyLogin(), h );
			Vector griglia=new Vector();
			if (o!=null){
				if(((Vector)o).size()<=0){
					//				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
					return -1;
				}
				else griglia=(Vector)o;}
			Enumeration en=griglia.elements();
			int i=0;
			while(en.hasMoreElements()){
				ISASRecord is =(ISASRecord)en.nextElement();
				h=is.getHashtable();
				String codice = (String) h.get("pi_prest_cod");
				logger.trace("carico prestazioni della cella selezionata in tabella hash=="+h.toString());
				Label leb = new Label((String) h.get("pi_prest_cod"));
				leb.setTooltiptext((String) h.get("pi_prest_desc"));
				pianoAccessi.appendChild(leb);
				prestazioniDaPiano.put(codice, codice);		
			}
			pianoAccessi.invalidate();

		}
		return 0;
	}
	
	public boolean isSavable() throws ISASMisuseException{
		return (super.isSavable()) && salvabile; //Salvabile è false solo se ho aperto un intervento già salvato come non eseguito 
	}

}

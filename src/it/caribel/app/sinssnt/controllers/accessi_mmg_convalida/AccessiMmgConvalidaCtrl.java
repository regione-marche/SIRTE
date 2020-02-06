package it.caribel.app.sinssnt.controllers.accessi_mmg_convalida;

import it.caribel.app.sinssnt.bean.nuovi.AccessiMmgConvalidaEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MaximizeEvent;
import org.zkoss.zk.ui.event.SizeEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;


public class AccessiMmgConvalidaCtrl extends CaribelGridCtrl{

	private static final long serialVersionUID = 1L;

	private String myKeyPermission = ChiaviISASSinssntWeb.CONS_MIL;
	private AccessiMmgConvalidaEJB myEJB = new AccessiMmgConvalidaEJB();
	public static String myPathZul = "/web/ui/sinssnt/accessi_mmg_convalida/accessi_mmg_conv_grid.zul";
	
	private CaribelRadiogroup tipo_data;
//	private CaribelTextbox n_cartella;
//	private CaribelCombobox cognomeAss;
	private CaribelTextbox cod_medico;
	private CaribelCombobox desc_medico;
	private CaribelIntbox reg_anno;
	private CaribelCombobox cbx_reg_mese;
	private CaribelCheckbox ck_errati;
	private CaribelCheckbox ck_anomali;
	private CaribelCheckbox ck_corretti;
	
	private String oldHeight = "";
	
	public void onSize$myWindow(SizeEvent event){
		redrawPaging(event.getHeight());
    }
	
	public void onMaximize$myWindow(MaximizeEvent event) {
        redrawPaging(event.getHeight());
    }
	
	private void redrawPaging(String strWindowHeight) {
		if(!strWindowHeight.equals(oldHeight)){
			oldHeight = strWindowHeight;
			int altezzaFiltro = 200;//in pixel
			int altezzaRiga = 30;//in pixel
			strWindowHeight = strWindowHeight.replaceAll("px", "");
			int windowHeight = Integer.parseInt(strWindowHeight);
			int altezzaListBox = windowHeight-altezzaFiltro;
			int pagSize = altezzaListBox/altezzaRiga;
			super.pagCaribellb.setPageSize(pagSize-1);
			doCerca();
		}
	}

	public void doAfterCompose(Component comp) throws Exception {	
		super.initCaribelGridCtrl(myEJB, myKeyPermission,new MyGridItemRenderer());
		//this.pathFormZul= myPathZul;
		super.doAfterCompose(comp);
		doPopulateCombobox();
		doPulisciRicerca();
	}

	private void doPopulateCombobox() throws Exception {
		String nomeMetodo = "doPopulateCombobox";
		try{
			//Carica combo mese
			Map<String, String> hMesi = new TreeMap<String, String>();
			hMesi.put("01",Labels.getLabel("generic.mese1"));
			hMesi.put("02",Labels.getLabel("generic.mese2"));
			hMesi.put("03",Labels.getLabel("generic.mese3"));
			hMesi.put("04",Labels.getLabel("generic.mese4"));
			hMesi.put("05",Labels.getLabel("generic.mese5"));
			hMesi.put("06",Labels.getLabel("generic.mese6"));
			hMesi.put("07",Labels.getLabel("generic.mese7"));
			hMesi.put("08",Labels.getLabel("generic.mese8"));
			hMesi.put("09",Labels.getLabel("generic.mese9"));
			hMesi.put("10",Labels.getLabel("generic.mese10"));
			hMesi.put("11",Labels.getLabel("generic.mese11"));
			hMesi.put("12",Labels.getLabel("generic.mese12"));
			CaribelComboRepository.populateCombobox(cbx_reg_mese, hMesi, true);
		}catch(Exception e){
			logger.error(nomeMetodo+" Errore durante il caricamento delle combobox - Exception: "+e);
			throw e;
		}
	}
	
	public void doStampa() {}
	
	@Override
	public void onDoubleClickedItem(Event event) throws Exception {}
	@Override
	protected void doTrasmetti(){}
	@Override
	protected void doApri(){}
	
	private Hashtable<String, Object> getHForQuery(){
		Hashtable<String, Object> h = new Hashtable<String, Object>();
		String anno = reg_anno.getRawText();
		String mese = cbx_reg_mese.getSelectedValue();
		String data_inizio = "01/"+mese+"/"+anno;
		String data_fine = getUltimoGiornoMese(mese, anno)+"/"+mese+"/"+anno;
		h.put("data_inizio", data_inizio);
		h.put("data_fine", data_fine);
		h.put("tipo_data", tipo_data.getSelectedValue());
		h.put("medico", cod_medico.getRawText());
//		h.put("n_cartella", n_cartella.getRawText());
		if(ck_errati.isChecked())
			h.put("ck_errati", ck_errati.getValue());
		if(ck_anomali.isChecked())
			h.put("ck_anomali", ck_anomali.getValue());
		if(ck_corretti.isChecked())
			h.put("ck_corretti", ck_corretti.getValue());
		return h;
	}

	public void doCerca(){		
		super.hParameters.clear();
		super.hParameters.putAll(getHForQuery());
		doRefresh();
	}
	
	public void onClick$btn_forza() {
		try{
			int countSel = 0;
			for (Iterator iterator = this.caribellb.getSelectedItems().iterator(); iterator.hasNext();) {
				Listitem litem = (Listitem) iterator.next();
				if(litem.isCheckable())
					countSel++;
			}
			if(countSel==0){
				UtilForUI.doAlertSelectOneRow();
				return;
			}
			Messagebox.show(
					Labels.getLabel("accessi_mmg_convalida.btn_forza.question", new String[] {""+countSel}),
					Labels.getLabel("messagebox.attention"), Messagebox.YES + Messagebox.NO, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event) throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())) {
								try{
									doForzatura();
									doRefresh();
								}catch(Exception e){
									doShowException(e);
								}
							}
						}
					});
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void onClick$btn_delete_row() {
		try{
			Messagebox.show(Labels.getLabel("accessi_mmg_convalida.btn_rimuovi.question"), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
					new EventListener<Event>(){
						public void onEvent(Event event) throws Exception{
							if (Messagebox.ON_YES.equals(event.getName())){
								doRimozione();
								Clients.showNotification(Labels.getLabel("form.delete.ok.notification"),"info",self,"middle_center",2500);
								doRefresh();
							}
						}
					});
		}catch(Exception e){
			doShowException(e);
		}
	}

	@SuppressWarnings("rawtypes")
	private void doRimozione()throws Exception {
		Listitem item = this.caribellb.getSelectedItem();
		@SuppressWarnings("unchecked")
		Hashtable<String, Object> hFromGrid = (Hashtable<String,Object>) item.getAttribute("ht_from_grid");
		@SuppressWarnings("unchecked")
		Hashtable<String, Object> h =  (Hashtable<String, Object>)UtilForBinding.getHashtableForEJBFromHashtable(hFromGrid);
		Vector<Hashtable<String, Object>> hv1 = new Vector<Hashtable<String, Object>>();
		hv1.add(h);
		myEJB.deleteDoppi(CaribelSessionManager.getInstance().getMyLogin(), new Hashtable(), hv1);
	}
	
	private void doForzatura()throws Exception {
		Vector<Hashtable<String,String>> vctr = new Vector<Hashtable<String,String>>();
		for (Iterator iterator = this.caribellb.getSelectedItems().iterator(); iterator.hasNext();) {
			Listitem litem = (Listitem) iterator.next();
			if(litem.isCheckable()){
				ISASRecord dbrFromGrid = (ISASRecord) litem.getAttribute("dbr_from_grid");
				String strCaricato = (String)dbrFromGrid.get("flag_caricato");
				String strCodMedico = (String)dbrFromGrid.get("cod_medico");
				String strIdTrasm = (String)dbrFromGrid.get("id_trasmissione");
				if (strCaricato != null && strCaricato.equals("N")){
		              Hashtable<String,String> h_row = new Hashtable<String,String>();
		              h_row.put("cod_medico", strCodMedico);
		              h_row.put("id_trasmissione", strIdTrasm);
		              vctr.addElement(h_row);
		        }
			}
		}
		if(vctr.size()>0){
			ISASRecord dbr = myEJB.forzaPrestazioni(CaribelSessionManager.getInstance().getMyLogin(), getHForQuery(), vctr);
			//Vector vDbr = (Vector)dbr.get("tabella");
			//evito di prendere il vettore ritornato da forzaPrestazioni e richiamo doCerca() cosi 
			//mantengo query e queryPaginate di AccessiMmgConvalidaEJB
			doCerca();
			Clients.showNotification(Labels.getLabel("common.msg.ok.notification"), "info", self, "middle_center",2500);
		}
	}

	public void doPulisciRicerca() {
		try {
			setDefault();
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void setDefault() throws Exception{		
		if(caribellb.getItemCount()>0){
			caribellb.getItems().clear();
		}
		
		//Imposto anno e mese corrente
		GregorianCalendar calendario = new GregorianCalendar();
		calendario.setTime(new Date());
		int numMeseCorr= calendario.get(Calendar.MONTH)+1;
		String strMeseCorr = ""+numMeseCorr;
		if(strMeseCorr.length()==1)
			strMeseCorr = "0"+strMeseCorr;
		cbx_reg_mese.setSelectedValue(strMeseCorr);
		reg_anno.setText(""+calendario.get(Calendar.YEAR));
		
		//Resetto il resto	
		tipo_data.setSelectedValue("1");
		cod_medico.setText("");
		desc_medico.setText("");
//		n_cartella.setText("");
//		cognomeAss.setText("");
		ck_errati.setChecked(true);
		ck_anomali.setChecked(true);
		ck_corretti.setChecked(true);
	}
	
	// ritorna l'ultimo giorno del mese
	public String getUltimoGiornoMese(String mese, String anno)
	{
		if(mese.equals("01") || mese.equals("03") || mese.equals("05")
				|| mese.equals("07") || mese.equals("08") ||
				mese.equals("10") || mese.equals("12")) return "31";
		else if (mese.equals("02"))
		{
			GregorianCalendar c = new GregorianCalendar();
			if(c.isLeapYear(Integer.parseInt(anno))) return "29";
			else return "28";
		}
		else return "30";
	}

}



package it.caribel.app.sinssnt.controllers.palliat_ricoveri;

import it.caribel.app.common.ejb.TabVociEJB;
import it.caribel.app.sinssnt.bean.modificati.SkMPalRicoEJB;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.app.sinssnt.util.UtilForContainerGen;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zul.Window;
import org.zkoss.zul.Window.Mode;

public class RicoveriGridCRUDCtrl extends CaribelGridCRUDCtrl 
{
	private static final long serialVersionUID = 1L;
	//public static String myKeyPermission = ChiaviIsasBase.INTOLLERANZA_ALLERGIA;
	public static String myKeyPermission = "";
	private SkMPalRicoEJB myEJB = null;	
	public static final String myPathFormZul ="/web/ui/sinssnt/contatto_palliativista/ricoveriForm.zul";
	
	public static final String ARGS_ID_MY_WINDOW 		= "ARGS_ID_MY_WINDOW";
	public static final String ARGS_NOME_TABELLA		= "NOME_TABELLA";
	public static final String ARGS_STRING_N_CARTELLA 	= "STRING_N_CARTELLA";

	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	private CaribelTextbox skr_progr; 
	private CaribelCombobox combo_tipo_ricovero;
	private CaribelCombobox combo_agente_ricovero;

	String nCartella = "";
	String nContatto = "";

	private static final String ver = "9-";

	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		try 
		{
			myEJB = new SkMPalRicoEJB();

			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			doPopulateCombobox(combo_tipo_ricovero, combo_agente_ricovero);
			if (arg.get(ARGS_STRING_N_CARTELLA) == null) {
				nCartella = UtilForContainerGen.getCartellaCorr();
				if(nCartella == null || nCartella.trim().equals(""))
					throw new Exception("Reperimento codice Assistito non riuscito!");
			}else {
				nCartella = ISASUtil.getValoreStringa(arg, ARGS_STRING_N_CARTELLA);
				//cv
				n_cartella.setValue(Integer.parseInt(nCartella));
			}

			//cv
			String s = UtilForContainer.getNcontatto();
			if (s != null)
			{
				nContatto = s;						
				n_contatto.setValue(Integer.parseInt(nContatto));
			}				

			logger.trace(punto + " nCartella>>"+ nCartella+"<");
			//key_cartella.setText(nCartella);

			//Recupero eventuale ID_MY_WINDOW passato dal container (utile in caso di piu diari nello stesso container)
			if(arg.get(ARGS_ID_MY_WINDOW)!=null && !(""+arg.get(ARGS_ID_MY_WINDOW)).trim().equals("")){
				self.setId((String)arg.get(ARGS_ID_MY_WINDOW));
			}

			String modal = ISASUtil.getValoreStringa(arg, "modale");
			if(ISASUtil.valida(modal)&& modal.equals("SI"))
			{
				logger.trace(punto + " apertura modale ");
				((Window)self).setMode(Mode.MODAL);
				((Window)self).setClosable(true);
			}else {
				logger.trace(punto + " apertura NON modale ");
			}

			doLoadGrid();

		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void doPopulateCombobox(CaribelCombobox cbx1, CaribelCombobox cbx2) 
	{
		//combo tipo ricovero
		cbx1.clear();
		CaribelComboRepository.addComboItem(cbx1, "0", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.0"));
		CaribelComboRepository.addComboItem(cbx1, "1", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.1"));
		CaribelComboRepository.addComboItem(cbx1, "2", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.2"));
		CaribelComboRepository.addComboItem(cbx1, "3", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.3"));
		cbx1.setSelectedValue("0");	
		
		cbx2.clear();
		try 
		{	
			Hashtable<String, String> h = new Hashtable<String, String>();			
			h.put("tab_cod", "SKMAGRIC");
			CaribelComboRepository.comboPreLoad(""+"SKMAGRIC",
					new TabVociEJB(),"query",h, cbx2, null,
					"tab_val", "tab_descrizione", true);
		
		}catch(Exception e){
			doShowException(e);
		}	
	}
	
	@Override
	public void onClick$btn_formgrid_new() {
		try{
			this.clb.setSelectedIndex(-1);
			UtilForBinding.resetForm(myForm,this.parkSetting);
			this.setStato(STATO_INSERT);
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Override
	protected void notEditable() {
		//altro_principio_attivo.setReadonly(!cbx_principio_attivo.getSelectedValue().equals("0"));
	}

	@Override
	protected void afterSetStatoInsert(){
		
	}

	@Override
	protected void afterSetStatoUpdate() {
		super.afterSetStatoUpdate();
	}

	@Override
	protected void afterSetStatoWait() {
		String punto = ver + "afterSetStatoWait ";
		super.afterSetStatoWait();
		logger.trace(punto + " afterStatoWait ");
	}

//	private void verificaData() {
//		
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl#doLoadGrid()
	 * Sovrascrivo il metodo perche la query ha bisogno dei campi dtApertura e
	 * dtChiusura che non fanno parte dell'ISASRecord
	 */
	@Override
	protected void doLoadGrid() throws Exception {
		hParameters.putAll(getOtherParametersString());
		// REFRESH SULLA LISTA
		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
		clb.getItems().clear();
		clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
	}

	@Override
	protected boolean doValidateForm() 
	{
		String punto = ver + "doValidateForm ";
		logger.debug(punto + "inzio ");
		Hashtable<String, Object> dati = new Hashtable<String, Object>();
		dati.put("n_cartella", n_cartella.getValue()+"");
		//cv
		dati.put("n_contatto", n_contatto.getValue());
		ISASRecord dbrIntolleranze = null;
		return true;
	}

	@Override
	protected Map<String,String> getOtherParametersString() 
	{
		Hashtable<String, String> ret = new Hashtable<String, String>();
		ret.put("n_cartella", nCartella);
		ret.put("n_contatto", nContatto);
		return ret;
	}

	//Carlo Volpicelli
	@Override
	protected boolean doUpdateGridForm() throws Exception 
	{
		String s = UtilForContainer.getNcontatto();
		if (s != null)
		{
			nContatto = s;						
			n_contatto.setValue(Integer.parseInt(nContatto));
		}
		return super.doUpdateGridForm();
	}
//	private static final long serialVersionUID = 1L;
//	//public static String myKeyPermission = ChiaviIsasBase.INTOLLERANZA_ALLERGIA;
//	public static String myKeyPermission = "";
//	private SkMPalRicoEJB myEJB = null;	
//	public static final String myPathFormZul ="/web/ui/sinssnt/contatto_palliativista/ricoveriForm.zul";
//	
//	public static final String ARGS_ID_MY_WINDOW 		= "ARGS_ID_MY_WINDOW";
//	public static final String ARGS_NOME_TABELLA		= "NOME_TABELLA";
//	public static final String ARGS_STRING_N_CARTELLA 	= "STRING_N_CARTELLA";
//
//	private CaribelIntbox n_cartella;
//	private CaribelIntbox n_contatto;
//	private CaribelTextbox skr_progr; 
//	private CaribelCombobox combo_tipo_ricovero;
//	private CaribelCombobox combo_agente_ricovero;
//
//	String nCartella = "";
//	String nContatto = "";
//	String progr = "";
//
//	private static final String ver = "9-";
//
//	protected void doInitGridForm() {
//		String punto = ver + "doInitGridForm ";
//		try 
//		{
//			myEJB = new SkMPalRicoEJB();
//
//			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
//			super.setMethodNameForQuery("query");
//			doPopulateCombobox(combo_tipo_ricovero, combo_agente_ricovero);
//			if (arg.get(ARGS_STRING_N_CARTELLA) == null) {
//				nCartella = UtilForContainerGen.getCartellaCorr();
//				if(nCartella == null || nCartella.trim().equals(""))
//					throw new Exception("Reperimento codice Assistito non riuscito!");
//			}else {
//				nCartella = ISASUtil.getValoreStringa(arg, ARGS_STRING_N_CARTELLA);
//				//cv
//				n_cartella.setValue(Integer.parseInt(nCartella));
//			}
//
//			//cv
//			String s = UtilForContainer.getNcontatto();
//			if (s != null)
//			{
//				nContatto = s;						
//				n_contatto.setValue(Integer.parseInt(nContatto));
//			}
//
//			logger.trace(punto + " nCartella>>"+ nCartella+"<");
//			//key_cartella.setText(nCartella);
//
//			//Recupero eventuale ID_MY_WINDOW passato dal container (utile in caso di piu diari nello stesso container)
//			if(arg.get(ARGS_ID_MY_WINDOW)!=null && !(""+arg.get(ARGS_ID_MY_WINDOW)).trim().equals("")){
//				self.setId((String)arg.get(ARGS_ID_MY_WINDOW));
//			}
//
//			String modal = ISASUtil.getValoreStringa(arg, "modale");
//			if(ISASUtil.valida(modal)&& modal.equals("SI"))
//			{
//				logger.trace(punto + " apertura modale ");
//				((Window)self).setMode(Mode.MODAL);
//				((Window)self).setClosable(true);
//			}else {
//				logger.trace(punto + " apertura NON modale ");
//			}
//
//			doLoadGrid();
//
//		} catch (Exception e) {
//			doShowException(e);
//		}
//	}
//
//	private void doPopulateCombobox(CaribelCombobox cbx1, CaribelCombobox cbx2) 
//	{
//		//combo tipo ricovero
//		cbx1.clear();
//		CaribelComboRepository.addComboItem(cbx1, "0", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.0"));
//		CaribelComboRepository.addComboItem(cbx1, "1", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.1"));
//		CaribelComboRepository.addComboItem(cbx1, "2", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.2"));
//		CaribelComboRepository.addComboItem(cbx1, "3", Labels.getLabel("schedaPalliat.ricoveri.tipoRicovero.3"));
//		cbx1.setSelectedValue("0");	
//		
//		cbx2.clear();
//		try 
//		{	
//			Hashtable<String, String> h = new Hashtable<String, String>();			
//			h.put("tab_cod", "SKMAGRIC"); //la prima stringa indica il nome del campo TAB_COD della vista TAB_VOCI, mentre la seconda stringa identifica il valore di TAB_COD che identifica tutti i valori da inserire in una medesima combobox
//					
//			CaribelComboRepository.comboPreLoad("", new TabVociEJB(),"queryCombo", h, cbx2, null, "tab_val", "tab_descrizione", true); //tab_val e tab_descrizione sono i nomi dei campi della vista tab_voci che identificano rispettivamente chiave e valore per ogni elemento della combobox 
//		}catch(Exception e){
//			doShowException(e);
//		}	
//	}
//
//	@Override
//	public void onClick$btn_formgrid_new() {
//		try{
//			this.clb.setSelectedIndex(-1);
//			UtilForBinding.resetForm(myForm,this.parkSetting);
//			this.setStato(STATO_INSERT);
//		}catch(Exception e){
//			doShowException(e);
//		}
//	}
//
//	@Override
//	protected void notEditable() {
//		//altro_principio_attivo.setReadonly(!cbx_principio_attivo.getSelectedValue().equals("0"));
//	}
//
//	@Override
//	protected void afterSetStatoInsert(){
//		
//	}
//
//	@Override
//	protected void afterSetStatoUpdate() {
//		super.afterSetStatoUpdate();
//	}
//
//	@Override
//	protected void afterSetStatoWait() {
//		String punto = ver + "afterSetStatoWait ";
//		super.afterSetStatoWait();
//		logger.trace(punto + " afterStatoWait ");
//	}
//
////	private void verificaData() {
////		
////	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see it.caribel.zk.generic_controllers.CaribelGridCRUDCtrl#doLoadGrid()
//	 * Sovrascrivo il metodo perche la query ha bisogno dei campi dtApertura e
//	 * dtChiusura che non fanno parte dell'ISASRecord
//	 */
//	@Override
//	protected void doLoadGrid() throws Exception {
//		hParameters.putAll(getOtherParametersString());
//		// REFRESH SULLA LISTA
//		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
//		clb.getItems().clear();
//		clb.setModel(new CaribelListModel<ISASRecord>(vDbr));
//	}
//
//	@Override
//	protected boolean doValidateForm() 
//	{
//		String punto = ver + "doValidateForm ";
//		logger.debug(punto + "inzio ");
//		Hashtable<String, Object> dati = new Hashtable<String, Object>();
//		dati.put("n_cartella", n_cartella.getValue()+"");
//		//cv
//		dati.put("n_contatto", n_contatto.getValue());
//		//dati.put("skr_progr", skr_progr.getValue());
//		//ISASRecord dbrIntolleranze = null;
//		return true;
//	}
//
//	@Override
//	protected Map<String,String> getOtherParametersString() 
//	{
//		Hashtable<String, String> ret = new Hashtable<String, String>();
//		ret.put("n_cartella", nCartella);
//		ret.put("n_contatto", nContatto);
//		ret.put("skr_progr", progr);
//		return ret;
//	}
//
//	//Carlo Volpicelli
//	@Override
//	protected boolean doUpdateGridForm() throws Exception 
//	{
//		String s = UtilForContainer.getNcontatto();
//		if (s != null)
//		{
//			nContatto = s;						
//			n_contatto.setValue(Integer.parseInt(nContatto));
//		}
//		return super.doUpdateGridForm();
//	}

}	
		
		

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

package it.caribel.app.sinssnt.controllers.palliat_sintomi;

import it.caribel.app.sinssnt.bean.modificati.SkMPalSintEJB;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.app.sinssnt.util.UtilForContainerGen;
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

import org.zkoss.zul.Window;
import org.zkoss.zul.Window.Mode;

public class SintomiGridCRUDCtrl extends CaribelGridCRUDCtrl 
{
	private static final long serialVersionUID = 1L;
	//public static String myKeyPermission = ChiaviIsasBase.INTOLLERANZA_ALLERGIA;
	public static String myKeyPermission = "";
	private SkMPalSintEJB myEJB = null;	
	public static final String myPathFormZul ="/web/ui/sinssnt/contatto_palliativista/sintomiForm.zul";
	
	public static final String ARGS_ID_MY_WINDOW 		= "ARGS_ID_MY_WINDOW";
	public static final String ARGS_NOME_TABELLA		= "NOME_TABELLA";
	public static final String ARGS_STRING_N_CARTELLA 	= "STRING_N_CARTELLA";

	private CaribelIntbox n_cartella;
	private CaribelIntbox n_contatto;
	private CaribelTextbox sks_progr; 

	String nCartella = "";
	String nContatto = "";

	private static final String ver = "9-";

	protected void doInitGridForm() {
		String punto = ver + "doInitGridForm ";
		try 
		{
			myEJB = new SkMPalSintEJB();

			super.initCaribelGridCRUDCtrl(myEJB, myKeyPermission);
			super.setMethodNameForQuery("query");
			//doPopulateCombobox();
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

}	
		
		

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import java.util.HashMap;
import java.util.Map;

import it.caribel.app.sinssnt.bean.nuovi.RmRichiesteMMGEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.util.ISASUtil;


import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

public class ElencoRichiesteMMGGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "RICH_MMG";
	private RmRichiesteMMGEJB myEJB = new RmRichiesteMMGEJB();
	private String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/richiesta_mmg.zul";

	private CaribelTextbox tb_filter1;

	private static final String ver = "3-";

	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		this.setMethodNameForQuery("query_loadGridRichMMG");
		
		if(super.caribelSearchCtrl!=null){
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get("n_cartella");
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				tb_filter1.setText(textToSearch);
				super.hParameters.put(tb_filter1.getDb_name(), textToSearch);
				doCerca();
			}
		}
		if(super.caribelContainerCtrl!=null){
			Object ncartella = super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA);
			Object data_chiusura = super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.PR_DATA_CHIUSURA);
			Object id_skso = super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.CTS_ID_SKSO); 
			super.hParameters.put("n_cartella", ncartella.toString());
			if (id_skso!=null && ISASUtil.valida(id_skso.toString()))
			super.hParameters.put("id_skso", id_skso.toString());
			
			
			if (ISASUtil.valida((String)data_chiusura) && ISASUtil.valida((String)id_skso)){
				//((Window)self).setTitle(Labels.getLabel("RichiestaMMG.gridTitleRichiesteSO"));
				super.hParameters.put("id_skso", id_skso.toString());
				//this.setMethodNameForQuery("query_loadGridRichStoMMG");
			}
			//else{
				((Window)self).setTitle(Labels.getLabel("RichiestaMMG.gridTitleRichieste"));
				
			//}
			btn_new.setVisible(true);
			doCerca();
			
		}
    }
	
	public void doStampa() {		

	}
	
	
	
	@Override
	public void doRefresh() {
		doRefreshNoAlert();
//		if(caribellb.getModel().getSize()==1){
//		caribellb.setSelectedIndex(0);
//		Listitem item = caribellb.getItemAtIndex(0);
//		item.setAttribute("dbr_from_grid", caribellb.getModel().getElementAt(0));
//		this.doApri();
//		
//	}
		if (caribellb.getModel().getSize() == 0){
			this.doNuovo();
			
		}

	}
	
	

	public void doCerca() {
		String punto = ver  + "doCerca ";
		Object cod_cartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
		this.hParameters.put("n_cartella", cod_cartella.toString());
		Object id_skso = super.caribelContainerCtrl.hashChiaveValore.get("id_skso");
		if (id_skso!=null && ISASUtil.valida(id_skso.toString()))
			this.hParameters.put("id_skso", id_skso.toString());
		doRefresh();
		
		if(caribellb.getModel().getSize()==1){
			logger.trace(punto + " carico di dati: ho una sola riga");
			caribellb.setSelectedIndex(0);
			Listitem item = caribellb.getItemAtIndex(0);
			item.setAttribute("dbr_from_grid", caribellb.getModel().getElementAt(0));
			this.doApri();
		}else if(caribellb.getModel().getSize()==0){
			logger.trace(punto + " Non ci sono schede: vado in inserimento");
			this.doNuovo();
		}else {
			logger.trace(punto + " Ho schede non faccio nulla ");
		}
	}
	public void doNuovo() {
		Object cod_cartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
		this.hParameters.put("n_cartella", cod_cartella.toString());
		super.doNuovo();
	}	
	protected Map<? extends String, ? extends Object> getMapParameters() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(hParameters);
		return map;
	}
	
}

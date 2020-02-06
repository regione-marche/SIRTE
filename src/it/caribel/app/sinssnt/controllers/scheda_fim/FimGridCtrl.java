package it.caribel.app.sinssnt.controllers.scheda_fim;

import it.caribel.app.sinssnt.bean.nuovi.SchedaFIMEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.zk.ui.Component;

public class FimGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "RB_FIM";
	private SchedaFIMEJB myEJB = new SchedaFIMEJB();
	private String myPathFormZul = "/web/ui/sinssnt/scheda_fim/scheda_fim.zul";
	public static final String myPathGridZul = "/web/ui/sinssnt/scheda_fim/scheda_fimGrid.zul";

	private CaribelDatebox tb_filter2;
	private CaribelTextbox tb_filter1;

	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		hParameters.put("fim_spr_data", arg.get("fim_spr_data"));
		
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
			super.hParameters.put(CostantiSinssntW.N_CARTELLA, super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA));
			super.hParameters.put(CostantiSinssntW.N_CONTATTO, super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CONTATTO));
			super.hParameters.put(tb_filter2.getDb_name(), arg.get("fim_spr_data"));
			
			doRefreshNoAlert();
		}
    }
	
	public void doStampa() {		

	}
	
	public void doCerca() {		
//		super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getValue().toUpperCase());
//		doRefresh();
		//da Container
		Object cod_cartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
		this.hParameters.put("n_cartella", cod_cartella.toString());
		doRefresh();
	}	
}

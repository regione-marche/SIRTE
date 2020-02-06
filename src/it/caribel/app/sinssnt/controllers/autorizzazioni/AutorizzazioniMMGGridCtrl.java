package it.caribel.app.sinssnt.controllers.autorizzazioni;

import it.caribel.app.sinssnt.bean.modificati.SkmmgEJB;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

public class AutorizzazioniMMGGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "SKMMGSTO";
	private SkmmgEJB myEJB = new SkmmgEJB(); 
	public static final String myPathFormZul = "/web/ui/sinssnt/autorizzazioneMMG/schedaMMGSegnalazione.zul";
	public static final String myPathZul = "/web/ui/sinssnt/autorizzazioneMMG/autorizzazioni_MMG_storicoGrid.zul";

	private CaribelTextbox tb_filter1;
//	private boolean contAperti;
//	public static final String CTS_CONTATTI_APERTI = "contAperti";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		this.setMethodNameForQuery("query_loadGridMMGSkmmg");
		
		if(super.caribelSearchCtrl!=null){
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get(CostantiSinssntW.N_CARTELLA);
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				tb_filter1.setText(textToSearch);
				super.hParameters.put(tb_filter1.getDb_name(), textToSearch);
				doCerca();
			}
		}
		if(super.caribelContainerCtrl!=null){
			Object ncartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
			super.hParameters.put(CostantiSinssntW.N_CARTELLA, ncartella.toString());
//			super.hParameters.put(CTS_CONTATTI_APERTI, arg.get(CTS_CONTATTI_APERTI));
			super.hParameters.put(CostantiSinssntW.PR_DATA, super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.MMGPRPR_DATA));
//			contAperti = ((arg.get("contAperti") != null) && (((String)arg.get("contAperti")).trim().equals("S")));
//			if (contAperti){
//				Listhead lh = (Listhead) caribellb.getHeads().iterator().next();
//				for (Iterator<Component> iterator = lh.getChildren().iterator(); iterator.hasNext();) {
//					Component type = (Component) iterator.next();
//					if(type instanceof CaribelListheader && ((CaribelListheader)type).getDb_name().equals("skf_data_chiusura")) {
//						type.setVisible(false);
//						break;
//					}
//				}
//				((Window)self).setTitle(Labels.getLabel("storico.medico.gridTitleAltri"));
//			}else{
				((Window)self).setTitle(Labels.getLabel("storico.medico.gridTitleStorico"));
//			}
			doRefresh();
		}
    }
	
	public void doStampa() {		

	}
	
	public void doCerca() {		
//		super.hParameters.put(tb_filter1.getDb_name(), tb_filter1.getValue().toUpperCase());
//		doRefresh();
		//da Container
		Object cod_cartella = super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA);
		this.hParameters.put(CostantiSinssntW.N_CARTELLA, cod_cartella.toString());
		doRefresh();
	}
	public void doNuovo() {		
		//verifico prima che non sia già presente un contatto aperto 
		//altrimenti apro quello
		Object cod_cartella = super.caribelContainerCtrl.hashChiaveValore.get(CostantiSinssntW.N_CARTELLA);
		this.hParameters.put(CostantiSinssntW.N_CARTELLA, cod_cartella.toString());
		super.doNuovo();
	}	
	
}

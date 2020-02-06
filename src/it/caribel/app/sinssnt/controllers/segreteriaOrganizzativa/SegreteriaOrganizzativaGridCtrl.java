package it.caribel.app.sinssnt.controllers.segreteriaOrganizzativa;

import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.util.HashMap;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

public class SegreteriaOrganizzativaGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	/* frame JFrameGridSchedaVal.java */
//	private String myKeyPermission = "SCHESTOR";
	private String myKeyPermission = ChiaviISASSinssntWeb.SEGRETERIA_ORGANIZZATIVA_STORICO;
//	private SkValutazEJB myEJB = new SkValutazEJB();
	private RMSkSOEJB myEJB = new RMSkSOEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/segreteriaOrganizzativa/schedaSegreteriaOrganizzativa.zul";
	public static final String myPathZul = "/web/ui/sinssnt/segreteriaOrganizzativa/storicoSegreteriaOrganizzativaGrid.zul";

	private CaribelTextbox tb_filter1;
//	private boolean contAperti;
//	public static final String CTS_CONTATTI_APERTI = "contAperti";
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		this.setMethodNameForQuery("query_skVal_chiuse");
		UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO, "");
				
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
				((Window)self).setTitle(Labels.getLabel("common.scheda.storico"));
//			}
			doRefresh();
		}
    }
	
	public void doStampa() {		

	}
	
	
	protected void doApri(){
		if(this.caribellb!=null){
			Listitem item = this.caribellb.getSelectedItem();
			ISASRecord dbrFromGrid = (ISASRecord) item.getAttribute("dbr_from_grid");
	
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("dbrFromList", dbrFromGrid);
			map.put("caribelGridCtrl", this);//Utile per l'aggiornamento da finestra modale
			map.put("caribelContainerCtrl", this.caribelContainerCtrl);
			
			UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO, ISASUtil.getValoreStringa(dbrFromGrid, CostantiSinssntW.CTS_ID_SKSO));
			
			if(pathFormZul!=null && !pathFormZul.isEmpty()){
				Component comp = null;
				try{
					comp = Executions.getCurrent().createComponents(pathFormZul, self.getParent(), map);
				}catch(UiException e){
					if(e.getMessage().startsWith("Not unique in ID space")){
						self.getParent().getFellowIfAny(e.getMessage().substring(e.getMessage().lastIndexOf(": ")+2), true).detach();
						comp = Executions.getCurrent().createComponents(pathFormZul, self.getParent(), map);
//						self.getFellow(e.getMessage().substring(e.getMessage().lastIndexOf(": ")+2)).detach();
					}else{
						throw e;
					}
				}
				if(this.caribelContainerCtrl!=null)
					this.caribelContainerCtrl.showComponent(comp);
			}else{
				UtilForUI.workInProgress();
			}
		}
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
	    UtilForContainer.setObjectFromMyContainer(CostantiSinssntW.CTS_ID_SKSO, CostantiSinssntW.CTS_ID_SKSO_NUOVO_INSERIMENTO);
		super.doNuovo();
	}	
	
}

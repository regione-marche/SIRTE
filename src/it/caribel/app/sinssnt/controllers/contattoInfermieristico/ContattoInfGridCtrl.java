package it.caribel.app.sinssnt.controllers.contattoInfermieristico;

import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Window;

public class ContattoInfGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = ChiaviISASSinssntWeb.I_CONSTO;
	private SkInfEJB myEJB = new SkInfEJB();
	private String myPathFormZul = "/web/ui/sinssnt/contatto_infermieristico/contatto_inf.zul";
//	private String myPathFormZul = "/web/ui/common/TEMPLATE/TEMPLATEForm.zul";

	private CaribelTextbox tb_filter1;

	private boolean contAperti;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		this.setMethodNameForQuery("query_loadGridSkInf");
		
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
			Object ncartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
			super.hParameters.put("n_cartella", ncartella.toString());
			Object pr_data = (super.caribelContainerCtrl.hashChiaveValore.get(Costanti.PR_DATA));
			if(pr_data != null){
				super.hParameters.put(Costanti.PR_DATA, pr_data);
			}
			contAperti = ((arg.get("contAperti") != null) && (((String)arg.get("contAperti")).trim().equals("S")));
			if (contAperti){
				Listhead lh = (Listhead) caribellb.getHeads().iterator().next();
				for (Iterator<Component> iterator = lh.getChildren().iterator(); iterator.hasNext();) {
					Component type = (Component) iterator.next();
					if(type instanceof CaribelListheader && ((CaribelListheader)type).getDb_name().equals("ski_data_uscita")) {
						type.setVisible(false);
						break;
					}
				}
				((Window)self).setTitle(Labels.getLabel("schedaInfGrid.gridTitleAltri"));
				super.hParameters.put("contAperti", (String)arg.get("contAperti"));
				btn_new.setVisible(true);
			}else{
				((Window)self).setTitle(Labels.getLabel("common.scheda.storico"));
			}
	 //           JCariTable1.setColumnHidden("Data chiusura");
			doRefresh();
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
	public void doNuovo() {		
		//verifico prima che non sia già presente un contatto aperto 
		//altrimenti apro quello
		Object cod_cartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
		this.hParameters.put("n_cartella", cod_cartella.toString());
		super.doNuovo();
	}	
	
	protected Map<? extends String, ? extends Object> getMapParameters() {
		Map map = new HashMap<String, Object>();
		map.putAll(hParameters);
		return map;
	}

}

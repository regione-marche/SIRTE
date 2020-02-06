package it.caribel.app.sinssnt.controllers.contattoFisioterapico;

import java.util.Iterator;

import it.caribel.app.sinssnt.bean.modificati.SkFisioEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.zk.composite_components.CaribelListheader;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Window;

public class ContattoFisioGridCtrl extends CaribelGridCtrl {

	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "SKFISSTO";
	private SkFisioEJB myEJB = new SkFisioEJB();
	private String myPathFormZul = "/web/ui/sinssnt/contatto_fisioterapico/contatto_fisio.zul";

	private CaribelTextbox tb_filter1;

	private boolean contAperti;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
		this.setMethodNameForQuery("query_loadGridSkFis");
		
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
//			super.hParameters.put(Costanti.PR_DATA, super.caribelContainerCtrl.hashChiaveValore.get(Costanti.PR_DATA));
			contAperti = ((arg.get("contAperti") != null) && (((String)arg.get("contAperti")).trim().equals("S")));
			if (contAperti){
				Listhead lh = (Listhead) caribellb.getHeads().iterator().next();
				for (Iterator<Component> iterator = lh.getChildren().iterator(); iterator.hasNext();) {
					Component type = (Component) iterator.next();
					if(type instanceof CaribelListheader && ((CaribelListheader)type).getDb_name().equals("skf_data_chiusura")) {
						type.setVisible(false);
						break;
					}
				}
				((Window)self).setTitle(Labels.getLabel("schedaFisioGrid.gridTitleAltri"));
				super.hParameters.put("contAperti", arg.get("contAperti"));
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
	
}

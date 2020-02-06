package it.caribel.app.sinssnt.controllers.menu;

import it.caribel.app.sinssnt.controllers.ContainerNoProfiloCtrl;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tree;

public class MenuGeneraleCtrl extends CaribelForwardComposer {

	private static final long serialVersionUID = 1L;
	
	private Panel panel_generale;
	private Tabbox tabbox_menu;
			
	private Tab tab_menu_agenda;
	private Tab tab_menu_flussi;
	private Tab tab_menu_operazioni;
	private Tab tab_menu_puac;
	private Tab tab_menu_servizi;
	private Tab tab_menu_stampe;
	private Tab tab_menu_stampe2;
	private Tab tab_menu_tabelle1;
	private Tab tab_menu_tabelle2;
	private Tab tab_menu_fittizio;
	
	private Tree menu_tree_agenda;
	private Tree menu_tree_flussi;
	private Tree menu_tree_operazioni;
	private Tree menu_tree_puac;
	private Tree menu_tree_servizi;
	private Tree menu_tree_stampe;
	private Tree menu_tree_stampe2;
	private Tree menu_tree_tabelle;
	private Tree menu_tree_tabelle_sinssnt;
	
	private int contaClickOperazioni = 0;
	private int contaClickFlussi = 0;
	private int contaClickAgenda = 0;
	private int contaClickPuac = 0;
	private int contaClickServizi = 0;
	private int contaClickStampe = 0;
	private int contaClickStampe2 = 0;
	private int contaClickTabelle1 = 0;
	private int contaClickTabelle2 = 0;

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		int countInvisible = 0; 
		
		if(menu_tree_agenda!=null && isTreeEmpty(menu_tree_agenda)){
			tab_menu_agenda.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_flussi!=null && isTreeEmpty(menu_tree_flussi)){
			tab_menu_flussi.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_operazioni!=null && isTreeEmpty(menu_tree_operazioni)){
			tab_menu_operazioni.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_puac!=null && isTreeEmpty(menu_tree_puac)){
			tab_menu_puac.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_servizi!=null && isTreeEmpty(menu_tree_servizi)){
			tab_menu_servizi.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_stampe!=null && isTreeEmpty(menu_tree_stampe)){
			tab_menu_stampe.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_stampe2!=null && isTreeEmpty(menu_tree_stampe2)){
			tab_menu_stampe2.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_tabelle!=null && isTreeEmpty(menu_tree_tabelle)){
			tab_menu_tabelle1.setVisible(false);
			countInvisible++;
		}
		
		if(menu_tree_tabelle_sinssnt!=null && isTreeEmpty(menu_tree_tabelle_sinssnt)){
			tab_menu_tabelle2.setVisible(false);
			countInvisible++;
		}
		
		countInvisible++;//tengo in considerazione anche il tab_menu_fittizio
		
		if(countInvisible == tabbox_menu.getTabs().getChildren().size()){
			//Nascondo tutto il menu generale
			panel_generale.setVisible(false);	
			if (UtilForContainer.getContainerCorr() instanceof ContainerNoProfiloCtrl) {
				Messagebox.show(
						Labels.getLabel("common.msg.noPermessi"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.INFORMATION);
			}
		}	
	}	
	
	private boolean isTreeEmpty(Tree myTree){
		String nomeMetodo = "isTreeEmpty";
		try{
			if(myTree!=null){
				MenuTreeNode corr;
				int childCount = myTree.getModel().getChildCount(myTree.getModel().getRoot());
				for(int i=0; i<childCount;i++){
					corr = ((MenuTreeNode)myTree.getModel().getChild(new int[] {i}));
					if(corr.getData().isEnabled())
						return false;//Appena trovo un menu abilitato esco (l'abero non è vuoto)!
				}
			}
		}catch(Exception ex){
			logger.error(MenuGeneraleCtrl.class.getName()+"."+nomeMetodo+" - Exception: "+ex);
		}
		return true;
	}
	
	public void onClick$tab_menu_operazioni(Event event) throws Exception{
		contaClickOperazioni++;
		if(contaClickOperazioni>1){
			contaClickOperazioni=0;
			tab_menu_operazioni.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_operazioni.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_flussi(Event event) throws Exception{
		contaClickFlussi++;
		if(contaClickFlussi>1){
			contaClickFlussi=0;
			tab_menu_flussi.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_flussi.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_agenda(Event event) throws Exception{
		contaClickAgenda++;
		if(contaClickAgenda>1){
			contaClickAgenda=0;
			tab_menu_agenda.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_agenda.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_puac(Event event) throws Exception{
		contaClickPuac++;
		if(contaClickPuac>1){
			contaClickPuac=0;
			tab_menu_puac.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_puac.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_servizi(Event event) throws Exception{
		contaClickServizi++;
		if(contaClickServizi>1){
			contaClickServizi=0;
			tab_menu_servizi.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_servizi.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_stampe(Event event) throws Exception{
		contaClickStampe++;
		if(contaClickStampe>1){
			contaClickStampe=0;
			tab_menu_stampe.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_stampe.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_stampe2(Event event) throws Exception{
		contaClickStampe2++;
		if(contaClickStampe2>1){
			contaClickStampe2=0;
			tab_menu_stampe2.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_stampe2.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_tabelle1(Event event) throws Exception{
		contaClickTabelle1++;
		if(contaClickTabelle1>1){
			contaClickTabelle1=0;
			tab_menu_tabelle1.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_tabelle1.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	public void onClick$tab_menu_tabelle2(Event event) throws Exception{
		contaClickTabelle2++;
		if(contaClickTabelle2>1){
			contaClickTabelle2=0;
			tab_menu_tabelle2.setSelected(false);
			tab_menu_fittizio.setSelected(true);
		}else{
			tab_menu_tabelle2.setSelected(true);
			tab_menu_fittizio.setSelected(false);
		}
	}
	
	
	
	
}

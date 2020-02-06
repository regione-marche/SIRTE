package it.caribel.app.sinssnt.controllers.menu.menu_left;

import it.caribel.app.sinssnt.controllers.anagrafica.FassiGridCtrl;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuComposer;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tree;

public class ComposerMenuLeft extends MenuComposer {

	private static final long serialVersionUID = 4231564509395483264L;
	
	Tab ricercaAssistito;
	Tab listaAttivita;
	Tab listaAssistiti;
	
	@Wire
	private Tree menu_tree_operazioni;

	CaribelForwardComposer cfc = new CaribelForwardComposer();
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		if(ricercaAssistito==null){
			ricercaAssistito = (Tab) getSelf().getFellowIfAny("ricercaAssistito");
			if(ricercaAssistito!=null){
				ricercaAssistito.addEventListener(Events.ON_CLICK, new EventListener<Event>() {	
					@Override
					public void onEvent(Event event) throws Exception {
						CaribelContainerCtrl contCorr = UtilForContainer.getContainerCorr();
						if(contCorr.formNeedApprovalForSave()){
							final Object ctrl = contCorr.getPrecCompShowed().getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
							Messagebox.show(Labels.getLabel("form.modified.question.in.container"), 
									Labels.getLabel("messagebox.attention"),
									Messagebox.YES+Messagebox.NO+Messagebox.CANCEL, Messagebox.QUESTION,
									new EventListener<Event>(){
										public void onEvent(Event event) throws Exception{
											try{
												if (Messagebox.ON_YES.equals(event.getName())){
													if(((CaribelFormCtrl)ctrl).executeDoSaveForm())
														UtilForContainer.restartContainerFrom(FassiGridCtrl.myIdWindow, FassiGridCtrl.myPathFormZul);
												}else if (Messagebox.ON_NO.equals(event.getName())){
													((CaribelFormCtrl)ctrl).doUndoForm();
													UtilForContainer.restartContainerFrom(FassiGridCtrl.myIdWindow, FassiGridCtrl.myPathFormZul);
												}else{
													return;
												}
											}catch (Exception e){
												cfc.doShowException(e);
											}
										}
									});
						}else{
							UtilForContainer.restartContainerFrom(FassiGridCtrl.myIdWindow, FassiGridCtrl.myPathFormZul);
						}
					}});
			}
		}
		if(listaAttivita==null){
			listaAttivita = (Tab) getSelf().getFellowIfAny("listaAttivita");
			if(listaAttivita!=null){
				listaAttivita.addEventListener(Events.ON_CLICK, new EventListener<Event>() {	
					@Override
					public void onEvent(Event event) throws Exception {
						CaribelContainerCtrl contCorr = UtilForContainer.getContainerCorr();
						if(contCorr.formNeedApprovalForSave()){
							final Object ctrl = contCorr.getPrecCompShowed().getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
							Messagebox.show(Labels.getLabel("form.modified.question.in.container"), 
									Labels.getLabel("messagebox.attention"),
									Messagebox.YES+Messagebox.NO+Messagebox.CANCEL, Messagebox.QUESTION,
									new EventListener<Event>(){
										public void onEvent(Event event) throws Exception{
											try{
												if (Messagebox.ON_YES.equals(event.getName())){
													if(((CaribelFormCtrl)ctrl).executeDoSaveForm())
														UtilForContainer.restartContainerFromListaAttivita();
												}else if (Messagebox.ON_NO.equals(event.getName())){
													((CaribelFormCtrl)ctrl).doUndoForm();
													UtilForContainer.restartContainerFromListaAttivita();
												}else{
													return;
												}
											}catch (Exception e){
												cfc.doShowException(e);
											}
										}
									});
						}else{
							UtilForContainer.restartContainerFromListaAttivita();
						}
					}});
			}
		}
		if(listaAssistiti==null){
			System.out.println(" sono in listaAssistiti ");
			listaAssistiti = (Tab) getSelf().getFellowIfAny("listaAssistiti");
			if(listaAssistiti!=null){
				listaAssistiti.addEventListener(Events.ON_CLICK, new EventListener<Event>() {	
					@Override
					public void onEvent(Event event) throws Exception {
						CaribelContainerCtrl contCorr = UtilForContainer.getContainerCorr();
						if(contCorr.formNeedApprovalForSave()){
							final Object ctrl = contCorr.getPrecCompShowed().getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
							Messagebox.show(Labels.getLabel("form.modified.question.in.container"), 
									Labels.getLabel("messagebox.attention"),
									Messagebox.YES+Messagebox.NO+Messagebox.CANCEL, Messagebox.QUESTION,
									new EventListener<Event>(){
										public void onEvent(Event event) throws Exception{
											try{
												if (Messagebox.ON_YES.equals(event.getName())){
													if(((CaribelFormCtrl)ctrl).executeDoSaveForm())
														UtilForContainer.restartContainerFromListaAssistiti();
												}else if (Messagebox.ON_NO.equals(event.getName())){
													((CaribelFormCtrl)ctrl).doUndoForm();
													UtilForContainer.restartContainerFromListaAssistiti();
												}else{
													return;
												}
											}catch (Exception e){
												cfc.doShowException(e);
											}
										}
									});
						}else{
						UtilForContainer.restartContainerFromListaAssistiti();
						}
					}});
			}
		}
	}
}
package it.caribel.app.sinssnt.controllers;

import it.caribel.app.sinssnt.bean.nuovi.ListaAttivitaEJB;
import it.caribel.app.sinssnt.controllers.contattoGenerico.ContattoGenFormCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerOperatore;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelComboRepository;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.pisa.caribel.operatori.GestTpOp;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

public class MainToolbarCtrl extends MainToolbarBaseCtrl {

	private static final long serialVersionUID = 1L;
	
	public static final String HASH_TIPO_OP = "#";
	
	@Override
	protected void loadComboArea(CaribelCombobox cbx)throws Exception {
		cbx.clear();
		if(iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_MEDICO))
			CaribelComboRepository.addComboItem(cbx, ContainerMedicoCtrl.myPathZul, Labels.getLabel("menu.contatto.medico").toUpperCase());
		if(iu.canIUse(ChiaviISASSinssntWeb.SKINF))
			CaribelComboRepository.addComboItem(cbx, ContainerInfermieristicoCtrl.myPathZul, Labels.getLabel("menu.contatto.inf").toUpperCase());
		if(iu.canIUse(ChiaviISASSinssntWeb.SKFISIO))
			CaribelComboRepository.addComboItem(cbx, ContainerFisioterapicoCtrl.myPathZul, Labels.getLabel("menu.contatto.fisio").toUpperCase());
		if(iu.canIUse(ChiaviISASSinssntWeb.SKMEDPAL))
			CaribelComboRepository.addComboItem(cbx, ContainerPalliativistaCtrl.myPathZul, Labels.getLabel("menu.contatto.palliat").toUpperCase());
		if(iu.canIUse(ChiaviISASSinssntWeb.A_OPPUAC)){
			//Descrizione termine PUA da Configuratore
            //String confPUA = getProfile().getStringFromProfile("titolo_pua");
            //String PUA = ((confPUA!=null && !confPUA.trim().equals("NO"))?confPUA:"PUA");
			//CaribelComboRepository.addComboItem(cbx, ContainerPuacCtrl.myPathZul, PUA.toUpperCase());
			CaribelComboRepository.addComboItem(cbx, ContainerPuacCtrl.myPathZul, Labels.getLabel("menu.puac").toUpperCase());
		}
		
		aggiungiVociPerGenerici(cbx);
		
		if(cbx.getItemCount()==0)
			CaribelComboRepository.addComboItem(cbx, ContainerNoProfiloCtrl.myPathZul, Labels.getLabel("menu.toolbar.generic_container").toUpperCase());
			
		cbx.setSelectedIndex(0);
		
		if(cbx.getItemCount()==1)
			cbx.setDisabled(true);
	}
	
	private void aggiungiVociPerGenerici(CaribelCombobox cbx) throws Exception {
		Hashtable<String, String> tipiOp = ManagerOperatore.getTipiOperatori(CostantiSinssntW.TAB_VAL_SO_TIPO_OPERATORE);
		Enumeration<String> n = tipiOp.keys();
		while (n.hasMoreElements()){
			String tipo_operatore = (String)n.nextElement();
			if(		!tipo_operatore.equals(GestTpOp.CTS_COD_INFERMIERE) && 
					!tipo_operatore.equals(GestTpOp.CTS_COD_MEDICO) &&
					!tipo_operatore.equals(GestTpOp.CTS_COD_FISIOTERAPISTA) &&
					//!tipo_operatore.equals(GestTpOp.CTS_MEDICO_CURE_PALLIATIVE) &&
					iu.canIUse(ChiaviISASSinssntWeb.CONTATTO_GENERICO+tipo_operatore)){
				String labelScheda = ContattoGenFormCtrl.getLabelScheda(tipo_operatore);
				if(labelScheda!=null)
					CaribelComboRepository.addComboItem(cbx, ContainerGenericoCtrl.myPathZul+HASH_TIPO_OP+tipo_operatore, labelScheda.toUpperCase());
			}
		}
	}

	public void onChange$cb_area(Event event) throws Exception {
		String selectdetLabel = cb_area.getSelectedItem().getLabel();
		Messagebox.show(Labels.getLabel("menu.toolbar.switch_container",new String[]{selectdetLabel}), 
			Labels.getLabel("messagebox.attention"),
			Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
			new EventListener<Event>() {
				public void onEvent(Event event)throws Exception {
					if (Messagebox.ON_YES.equals(event.getName())){
						try{
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
															redirectContainer();
														else
															impostaComboArea();
													}else if (Messagebox.ON_NO.equals(event.getName())){
														redirectContainer();
													}else{
														impostaComboArea();
													}
												}catch (Exception e){
													impostaComboArea();
													doShowException(e);
												}
											}
										});
							}else{
								redirectContainer();
							}
						}catch (Exception e){
							impostaComboArea();
							doShowException(e);
						}
					}else{
						impostaComboArea();
					}
				}
			});
    }
	
	public void redirectContainer() throws Exception {
		final String cartellaCorr = UtilForContainer.getCartellaCorr();
		final String id_skso = (UtilForContainer.getContainerCorr() instanceof ContainerPuacCtrl && 
				((ContainerPuacCtrl)UtilForContainer.getContainerCorr()).hashChiaveValore.get(CostantiSinssntW.CTS_ID_SKSO)!=null)?
			((ContainerPuacCtrl)UtilForContainer.getContainerCorr()).hashChiaveValore.get(CostantiSinssntW.CTS_ID_SKSO).toString():"";
			
		String selVal = cb_area.getSelectedItem().getValue();
		int index_hash_tipo_op = selVal.indexOf(HASH_TIPO_OP);
		String tipo_op = "";
		final boolean goToContainerGenerico = index_hash_tipo_op>0;
		if(goToContainerGenerico){
			tipo_op = selVal.substring(index_hash_tipo_op+1);
			selVal = selVal.substring(0,index_hash_tipo_op);
		}
			
		final String selectedValue = selVal;
		final String tipoOp = tipo_op;
		if(cartellaCorr!=null && !(UtilForContainer.getContainerCorr() instanceof ContainerNoProfiloCtrl )){
			Messagebox.show(Labels.getLabel("menu.toolbar.switch_container.assistito"), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.NO, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							String url = selectedValue;
							if (Messagebox.ON_YES.equals(event.getName())){
								url+= "?"+CostantiSinssntW.N_CARTELLA+"="+cartellaCorr;	
								Hashtable prima_visita = getPV_PC(cartellaCorr,id_skso,CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"");
								url+= (prima_visita!=null?"&"+CostantiSinssntW.CTS_ID_SKSO+"="+id_skso+"&"+CostantiSinssntW.CTS_FONTE+"="+CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA:"");	
								if (prima_visita == null){
									Hashtable presa_carico = getPV_PC(cartellaCorr,id_skso,CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI+"");
									url+= (presa_carico!=null?"&"+CostantiSinssntW.CTS_ID_SKSO+"="+id_skso+"&"+CostantiSinssntW.CTS_FONTE+"="+CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI:"");
								}
						    	//url+= "&arg2=xxxx";
							}
							if(goToContainerGenerico){
								//Devo redirigere al ContainerGenerico con il tipo_operatore del caso
								if(url.indexOf("?")>0)
									url+="&"+ContainerGenericoCtrl.parameter_tipo_op+"="+tipoOp;
								else
									url+="?"+ContainerGenericoCtrl.parameter_tipo_op+"="+tipoOp;
							}
							Executions.getCurrent().sendRedirect(url);
						}
					});
		}else{
			String url = selectedValue;
			if(goToContainerGenerico){
				//Devo redirigere al ContainerGenerico con il tipo_operatore del caso
				url+="?"+ContainerGenericoCtrl.parameter_tipo_op+"="+tipo_op;
			}
			Executions.getCurrent().sendRedirect(url);
		}
	}
	
	private Hashtable getPV_PC(String cartellaCorr, String id_skso, String tipo_fonte) throws Exception {
		if (id_skso.equals("")) return null;
		String tipo_operatore = getProfile().getStringFromProfile(ManagerProfile.TIPO_OPERATORE);
		if (tipo_fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_PRIMA_VISITA+"")&& !tipo_operatore.equals(CostantiSinssntW.TIPO_OPERATORE_ASSISTENTE_SOC) &&
				!tipo_operatore.equals(CostantiSinssntW.TIPO_OPERATORE_INFERMIERE)) return null;
		Hashtable params = new Hashtable();
		params.put(CostantiSinssntW.N_CARTELLA, cartellaCorr);
		params.put("tipo_fonte", tipo_fonte);
		params.put(CostantiSinssntW.CTS_ID_SKSO, id_skso);
		params.put("tipo_operatore",tipo_operatore);
		params.put("id_richiesta", id_skso);
		params.put("rich_perso", "true");
		params.put("rich_altri", "false");
		params.put(CostantiSinssntW.CTS_LISTA_ATTIVITA_ORDINAMENTO,"");
		params.put("adata","");
		params.put("dadata","");
		Vector res = (Vector)invokeGenericSuEJB(new ListaAttivitaEJB(), params, "query");
		if (res!=null && res.size()>0){			
			return params;
		}
		else return null;
	}

	

	public void impostaComboArea(){
		CaribelContainerCtrl containerCorr = UtilForContainer.getContainerCorr();
		if(containerCorr!=null){
			if(containerCorr instanceof ContainerPuacCtrl){
				cb_area.setSelectedValue(ContainerPuacCtrl.myPathZul);
			}else if(containerCorr instanceof ContainerMedicoCtrl){
				cb_area.setSelectedValue(ContainerMedicoCtrl.myPathZul);
			}else if(containerCorr instanceof ContainerInfermieristicoCtrl){
				cb_area.setSelectedValue(ContainerInfermieristicoCtrl.myPathZul);
			}else if (containerCorr instanceof ContainerFisioterapicoCtrl){
				cb_area.setSelectedValue(ContainerFisioterapicoCtrl.myPathZul);
			}else if (containerCorr instanceof ContainerGenericoCtrl){
				String tipoOperatore = ((ContainerGenericoCtrl)containerCorr).getTipoOpFromMyInstance();
				cb_area.setSelectedValue(ContainerGenericoCtrl.myPathZul+HASH_TIPO_OP+tipoOperatore);
			}else if (containerCorr instanceof ContainerNoProfiloCtrl){
				cb_area.setSelectedValue(ContainerNoProfiloCtrl.myPathZul);
			}else if (containerCorr instanceof ContainerPalliativistaCtrl){
				cb_area.setSelectedValue(ContainerPalliativistaCtrl.myPathZul);
			}
			
			
		}		
	}
	
}

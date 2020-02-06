package it.caribel.app.sinssnt.util;

import it.caribel.app.sinssnt.controllers.ContainerPuacCtrl;
import it.caribel.app.sinssnt.controllers.MainToolbarCtrl;
import it.caribel.app.sinssnt.controllers.anagrafica.FassiGridCtrl;
import it.caribel.app.sinssnt.controllers.lista_assistiti.ListaAssistitiGridCtrl;
import it.caribel.app.sinssnt.controllers.lista_attivita.ListaAttivitaGridCtrl;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;

public class UtilForContainer extends UtilForContainerGen{
	
 	public static void refreshToolBar(){
		Component mainToolbar = Path.getComponent("/main/main_toolbar");
		if(mainToolbar!=null &&  mainToolbar.getParent()!=null){
			final Object mainToolbarCtrl = mainToolbar.getParent().getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
			if(mainToolbarCtrl!=null && mainToolbarCtrl instanceof MainToolbarCtrl){
				((MainToolbarCtrl)mainToolbarCtrl).impostaComboArea();	
			}
		}
	}
 	
 	public static void restartContainerFromListaAttivita(){ 		
 		Component containerCorr = Path.getComponent("/main");
 		if(containerCorr!=null){
 			Object containerCtrl = containerCorr.getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
 			((CaribelContainerCtrl)containerCtrl).removeComponentsFrom("fassiGrid"); 			
 			((CaribelContainerCtrl)containerCtrl).showComponent("listaAttivitaGrid",ListaAttivitaGridCtrl.myPathZul);
 			//Recupero il controller della griglia e forzo il refresh
 			Component listaAttivitaGrid = Path.getComponent("/main/listaAttivitaGrid");
 			if(listaAttivitaGrid!=null){
	 			Object listaAttivitaGridCtrl = listaAttivitaGrid.getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
	 			((ListaAttivitaGridCtrl)listaAttivitaGridCtrl).doCerca();
 			}
 		}
 	}
 	
 	public static void restartContainerFromListaAssistiti(){ 		
 		Component containerCorr = Path.getComponent("/main");
 		if(containerCorr!=null){
 			Object containerCtrl = containerCorr.getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
 			((CaribelContainerCtrl)containerCtrl).removeComponentsFrom("fassiGrid");
 			((CaribelContainerCtrl)containerCtrl).removeComponentsFrom("listaAttivitaGrid");
 			((CaribelContainerCtrl)containerCtrl).showComponent("listaAssistitiGrid",ListaAssistitiGridCtrl.myPathZul);
 			//Recupero il controller della griglia e forzo il refresh
 			Component listaAssititGrid = Path.getComponent("/main/listaAssistitiGrid");
 			if(listaAssititGrid !=null){
	 			Object listaAttivitaGridCtrl = listaAssititGrid.getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
	 			((ListaAssistitiGridCtrl)listaAttivitaGridCtrl).doCerca();
 			}   
 		}
 	}
 	
 	public static void restartContainerFromFassi(String n_cartella){
 		restartContainerFrom(FassiGridCtrl.myIdWindow, FassiGridCtrl.myPathFormZul);
 		Component containerCorr = Path.getComponent("/main");
 		if(containerCorr!=null){
 			Object containerCtrl = containerCorr.getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
 			Component compFassiGrid = ((CaribelContainerCtrl)containerCtrl).hashIdComponent.get("fassiGrid");
 			FassiGridCtrl fassiGridCtrl = (FassiGridCtrl)compFassiGrid.getAttribute(CaribelForwardComposer.MY_CTRL_KEY);
 			fassiGridCtrl.setNCartella(n_cartella);
 			fassiGridCtrl.doCerca();
 		}
	}
	
	/*
	 * recupera dal container corrente il cognome nome dell'assistito 
	 */
	public static String getCognomeNomeAssistito() throws Exception  {
		String ret = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl != null && (caribelContainerCtrl.hashChiaveValore).containsKey(Costanti.N_CARTELLA)) {
			Object n_cartella = (caribelContainerCtrl.hashChiaveValore).get(Costanti.N_CARTELLA);
			ret = ManagerDecod.getCognomeNomeAssistito(n_cartella);
		}
		return ret;
	}

//	public static String getTipoOperatorerContainer() throws Exception  {
//		String tipoOperatore = "";
//		CaribelContainerCtrl container = getContainerCorr();
//		if(container instanceof ContainerInfermieristicoCtrl){
//			tipoOperatore = GestTpOp.CTS_COD_INFERMIERE;
//		} else if(container instanceof ContainerMedicoCtrl){
//			tipoOperatore = GestTpOp.CTS_COD_MEDICO;
//		} else if(container instanceof ContainerFisioterapicoCtrl){
//			tipoOperatore = GestTpOp.CTS_COD_FISIOTERAPISTA;
//		} else if(container instanceof ContainerGenericoCtrl){
//			tipoOperatore = ((ContainerGenericoCtrl)container).getTipoOpFromMyInstance();
//		}else if(container instanceof ContainerNoProfiloCtrl){
//			tipoOperatore = CaribelSessionManager.getInstance().getStringFromProfile(ManagerProfile.TIPO_OPERATORE);
//		} 
//		return tipoOperatore;
//	}

	public static boolean isSegregeriaOrganizzativa() {
		boolean isSO = false;
		
		CaribelContainerCtrl container = getContainerCorr();
		if(container instanceof ContainerPuacCtrl){
			isSO = true;
		}

		return isSO;
	}
	
	
	//Carlo Volpicelli - 11/01/2017
	public static String getNcontatto() throws Exception  {
		String ret = "";
		CaribelContainerCtrl caribelContainerCtrl = UtilForContainer.getContainerCorr();
		if (caribelContainerCtrl != null && (caribelContainerCtrl.hashChiaveValore).containsKey(Costanti.N_CONTATTO)) {
			Object n_contatto = (caribelContainerCtrl.hashChiaveValore).get(Costanti.N_CONTATTO);
			//ret = ManagerDecod.getNcontatto(n_contatto);
			ret = n_contatto.toString();
		}
		return ret;
	}


}

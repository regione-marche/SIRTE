package it.caribel.app.sinssnt.controllers.lista_attivita;

import java.util.Date;
import it.caribel.app.sinssnt.bean.nuovi.ListaAttivitaEJB;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.util.ApplicationResourcesProperties;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.procdate;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.sys.ComponentsCtrl;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Vlayout;
 
public class ListaAttivitaGridItemRenderer implements ListitemRenderer<ISASRecord>{
 
	ListaAttivitaEJB listaAttEjb = new ListaAttivitaEJB();
	
	String iconsPrefix = ApplicationResourcesProperties.getInstance().getProperty("icons.lista_attivita.prefix");
	String iconsPath    = ApplicationResourcesProperties.getInstance().getProperty("icons.root");
	
    public void render(Listitem item, ISASRecord dbr, int index) throws Exception {
    	
    	ComponentsCtrl.applyForward(item, "onDoubleClick=onDoubleClickedItem");
    	item.setAttribute("dbr_from_grid", dbr);
    	
    	CaribelDatebox dtRichiesta = new CaribelDatebox();
    	String dataRichiesta = ISASUtil.getValoreStringa(dbr, "data_richiesta");
    	
    	if (ManagerDate.validaData(dataRichiesta)){
    		dtRichiesta.setValue((java.sql.Date)dbr.get("data_richiesta"));
    	}
    	String n_cartella 			= ""+dbr.get("n_cartella");
    	String cognome 				= (String)dbr.get("cognome");
    	String nome 				= (String)dbr.get("nome");
    	String strDataRichiesta 	= dtRichiesta.getText();
    	String fonte 				= ""+dbr.get("fonte");
    	String valFonte 		    = ISASUtil.getValoreStringa(dbr, "fonte"); 
//    	String tipo_operatore 		= (String)dbr.get("tipo_operatore");
    	String tipo_operatore_descr = (String)dbr.get("tipo_operatore_descr");
    	String cod_med 				= (String)dbr.get("cod_med");
    	String cod_med_descr 		= (String)dbr.get("cod_med_descr");
    	String cod_operatore 		= (String)dbr.get("cod_operatore");
    	String cod_operatore_descr 	= (String)dbr.get("cod_operatore_descr");
    	String tipoCuraDescr 		= (String)dbr.get(CostantiSinssntW.CTS_TIPOCURA_DESCR);
    	String prMmgAltro 			=  ISASUtil.getValoreStringa(dbr, "pr_mmg_altro");
    	String fonteDettaglio 		= dbr.get("fonte_dettaglio")+"";
    	String livello_alert 		= ""+dbr.get(ListaAttivitaGridCtrl.LIVELLO_ALERT);
    	String tipoCura    		    = (String)dbr.get("tipocura");
    	
    	String toolTipDelete		= Labels.getLabel("listaAttivitaGrid.delete.tooltip");
    	int numeroFonte = Costanti.recuperaFonte(valFonte);
    	
    	Listcell lc = new Listcell();
    	Hlayout hL = new Hlayout();
    	hL.setHflex("true");
    	hL.setVflex("true");
    	hL.setParent(lc);
    	String dettaglio = (fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"")?fonteDettaglio:"");
    	String nomeFileImg = iconsPrefix+numeroFonte;
    	switch (numeroFonte) {
		case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA:
			Date dtAttuale = procdate.getDate();
    		if (ManagerDate.validaData(dtRichiesta) && ManagerDate.validaData(dtAttuale) &&
    				( (dtRichiesta.getValue()).before(dtAttuale) || (dtRichiesta.getValue()).before(dtAttuale) ) ){
    			nomeFileImg += ".scaduta";
    		}
    		
			break;
		case CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI:
			if (ISASUtil.valida(tipoCura)){
				nomeFileImg += "." + tipoCura;
			}
		default:
			break;
		}
    	
    	String iconsFileName= ApplicationResourcesProperties.getInstance().getProperty(nomeFileImg);
    	Image img = new Image(iconsPath+dettaglio+iconsFileName);
    	item.setAttribute("iconaMetaInfoCorr", img);
    	img.setParent(hL);
    	
    	Hlayout hText = new Hlayout();
    	hText.setHflex("true");
    	hText.setParent(hL);
    	
    	
    	Vlayout vl = new Vlayout();
    	vl.setHflex("true");
    	vl.setParent(hText);
    	
    	
    	Hlayout hTextNord = new Hlayout();
    	hTextNord.setWidth("100%");
    	hTextNord.setParent(vl);
    	
    	Hlayout hTextSud = new Hlayout();
    	hTextSud.setWidth("100%");
    	hTextSud.setParent(vl);
    	
    	//Prima riga di testo
    	if(strDataRichiesta!=null && !strDataRichiesta.equals("")){
    		if(numeroFonte!= CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI &&
    		   numeroFonte!= CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA	 &&
    		   numeroFonte!= CostantiSinssntW.CTS_TIPO_FONTE_RICOVERI_IN_SCADENZA &&
    		   numeroFonte!= CostantiSinssntW.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD){
		    	Label lab1 = new Label();
		    	lab1.setValue(strDataRichiesta);
		    	lab1.setParent(hTextNord);
    		}
    	}
    	
    	Label lab2 = new Label();
    	String descrizioneFonte = recuperaDescrizioneFonte(fonte, fonteDettaglio);
    	String operatore = fonte.equals(CostantiSinssntW.CTS_TIPO_FONTE_RICHIESTA_CHIUSURA+"")?"listaAttivitaGrid.attivita.op_rich":"listaAttivitaGrid.attivita.op_dest";
    	String rigo1 = " "+ descrizioneFonte;
    	String separatore = " - ";
    	String descSede ="";
    	if (numeroFonte>0){
    		switch (numeroFonte) {
			case CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI:
				String oggetto = "listaAttivitaGrid.attivita." + fonte + ".oggetto";
				String da = "listaAttivitaGrid.attivita." + fonte + ".da";
				rigo1 += " " + recuperaLabels(da) + " " + tipo_operatore_descr + " " + recuperaLabels(oggetto) + " " + prMmgAltro;	
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI:
				rigo1 += " " + strDataRichiesta;	
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE:
				rigo1 += (ISASUtil.valida(tipoCuraDescr) ? separatore : "") + tipoCuraDescr + " ";	
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA:
				rigo1 += (ISASUtil.valida(tipoCuraDescr) ? separatore : "") + tipoCuraDescr + " ";
				String descMotivoDim = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_MOTIVO_DIMISSIONI_DO);
				if (ISASUtil.valida(descMotivoDim)){
					String lbMotDim = "listaAttivitaGrid.attivita." + fonte + ".motivo.dimissione";
					rigo1 += separatore+ recuperaLabels(lbMotDim) + ": " + descMotivoDim +" ";
				}
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_PUA:
				rigo1 += (ISASUtil.valida(tipoCuraDescr) ? separatore : "") + tipoCuraDescr + " ";
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_RICOVERI_IN_SCADENZA:
				rigo1 += " " + strDataRichiesta;					
				break;	
//			case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI:
//				oggetto = "listaAttivitaGrid.attivita." + fonte + ".per";
//				rigo1 += " " + recuperaLabels(oggetto) +(ISASUtil.valida(tipo_operatore_descr) ? ": " + tipo_operatore_descr:"");
//
//				String descSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_DESCRIZIONE_SEDE);
//				if (ISASUtil.valida(descSede)){
//					rigo1 += separatore +recuperaLabels("listaAttivitaGrid.attivita.10.sede.presidio")+": " + descSede ;
//				}
//				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_0:
				oggetto = "listaAttivitaGrid.attivita." + fonte + ".per";
				rigo1 += " " + recuperaLabels(oggetto) +(ISASUtil.valida(tipo_operatore_descr) ? ": " + tipo_operatore_descr:"");

				descSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_DESCRIZIONE_SEDE);
				if (ISASUtil.valida(descSede)){
					rigo1 += separatore +recuperaLabels("listaAttivitaGrid.attivita.10.sede.presidio")+": " + descSede ;
				}
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI_MIN_1:
				oggetto = "listaAttivitaGrid.attivita." + fonte + ".per";
				rigo1 += " " + recuperaLabels(oggetto) +(ISASUtil.valida(tipo_operatore_descr) ? ": " + tipo_operatore_descr:"");

				descSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_DESCRIZIONE_SEDE);
				if (ISASUtil.valida(descSede)){
					rigo1 += separatore +recuperaLabels("listaAttivitaGrid.attivita.10.sede.presidio")+": " + descSede ;
				}
				break;	
			case CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA:
				rigo1 += " " +strDataRichiesta;
				boolean is_ADP =recuperaSeAdp(dbr, tipoCura);
	    		boolean is_AID_ADP_VSD = recuperaSeAidAdpVsd(dbr, tipoCura);
				if (is_ADP || is_AID_ADP_VSD) {
					String dettaglioServ = "";
					String adp = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_ADP);
					if (ISASUtil.valida(adp) && adp.equalsIgnoreCase(Costanti.CTS_S)) {
						dettaglioServ += (ISASUtil.valida(dettaglioServ) ? ", " : "");
						dettaglioServ += Labels.getLabel("RichiestaMMG.principale.adp");
					}
					String ard = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_ARD);
					if (ISASUtil.valida(ard) && ard.equalsIgnoreCase(Costanti.CTS_S)) {
						dettaglioServ += (ISASUtil.valida(dettaglioServ) ? ", " : "");
						dettaglioServ += Labels.getLabel("RichiestaMMG.principale.ard");
					}
					String aid = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_AID);
					if (ISASUtil.valida(aid) && aid.equalsIgnoreCase(Costanti.CTS_S)) {
						dettaglioServ += (ISASUtil.valida(dettaglioServ) ? ", " : "");
						dettaglioServ += Labels.getLabel("RichiestaMMG.principale.aid");
					}
					String vsd = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_VSD);
					if (ISASUtil.valida(vsd) && vsd.equalsIgnoreCase(Costanti.CTS_S)) {
						dettaglioServ += (ISASUtil.valida(dettaglioServ) ? ", " : "");
						dettaglioServ += Labels.getLabel("RichiestaMMG.principale.vsd");
					}
					
					if (ISASUtil.valida(dettaglioServ)){
						rigo1 += " " + separatore + " " + Labels.getLabel("listaAttivitaGrid.attivita.15.2.servizi.attivi") + ": ";
						rigo1 += dettaglioServ;
					}
				}		
				
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI:
				rigo1 += (ISASUtil.valida(tipoCuraDescr) ? separatore + tipoCuraDescr : " ") +
						(ISASUtil.valida(tipo_operatore_descr) ? separatore + tipo_operatore_descr : " ");
							
				rigo1 +=separatore+Labels.getLabel(operatore)+": "+(ISASUtil.valida(cod_operatore) ? "   ":"")+ cod_operatore_descr;
				
				if (ManagerDate.validaData(strDataRichiesta)){
					rigo1+= separatore + recuperaLabels("menu.segreteria.organizzativa.scheda.uvm.data.presa.carico")+":" + strDataRichiesta; 
				}
				break;
			case CostantiSinssntW.CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA:
				oggetto = "listaAttivitaGrid.attivita." + fonte+".dalla";
				rigo1 += " " + recuperaLabels(oggetto) +(ISASUtil.valida(tipo_operatore_descr) ? ": " + tipo_operatore_descr:"");
				rigo1 +=(ISASUtil.valida(tipoCuraDescr) ? separatore + tipoCuraDescr : " ");
				
				descSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_DESCRIZIONE_SEDE);
				if (ISASUtil.valida(descSede)){
					rigo1 += separatore +recuperaLabels("listaAttivitaGrid.attivita.10.sede.presidio")+": " + descSede ;
				}
				break;
			case Costanti.CTS_TIPO_FONTE_ESTRAZIONE_FLUSSI_SIAD:
				int numFonteDettaglio = ISASUtil.getValoreIntero(fonteDettaglio);
				if (numFonteDettaglio<=2){
					/*gestisco come ancora da inviare */
					oggetto = "listaAttivitaGrid.attivita." + fonte+".1";
				}else{ 
					// gestisco come una convalida 
					oggetto = "listaAttivitaGrid.attivita." + fonte+".2";
				}

				rigo1 += " " + Labels.getLabel(oggetto, new String[]{strDataRichiesta});
				
				descSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_DESCRIZIONE_SEDE);
				if (ISASUtil.valida(descSede)){
					rigo1 += separatore +recuperaLabels("listaAttivitaGrid.attivita."+fonte)+": " + descSede ;
				}
				break;	
				
			default:
				rigo1 += (ISASUtil.valida(tipoCuraDescr) ? separatore + tipoCuraDescr + separatore : " ") + tipo_operatore_descr;
				break;   
			}
    	}
		
    	lab2.setValue(rigo1);
    	lab2.setParent(hTextNord);

    	if(cod_operatore_descr!=null && !cod_operatore_descr.equals("") && 
    			(numeroFonte !=CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI) && 
//    			(numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI) &&
				(numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_SO_IN_SCADENZA) &&
    			(numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI)
    			&& (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI)
    			&& (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_NON_ESISTE_SCHEDA_SO_ATTIVA	)
    			&& (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE)
    			&& (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_DIMISSIONI_OSPEDALIERA)){
	    	Label lab3 = new Label();
	    	lab3.setValue(separatore+Labels.getLabel(operatore)+": "+(ISASUtil.valida(cod_operatore) ? "   ":"")+ cod_operatore_descr);
	    	lab3.setParent(hTextNord);
    	}
    	
    	//Seconda riga di testo
    	Label lab5 = new Label();
    	lab5.setValue(Labels.getLabel("common.assistito")+": "+n_cartella+"  "+ cognome +" "+nome);
    	lab5.setStyle("font-weight: bold;");
    	lab5.setParent(hTextSud);   
    	
    	Label lab6 = new Label();
//    	if(numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI){
//    		String operatoreDescrizione = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_LST_OPERATORE_DESCRIZIONE);
//			if (ISASUtil.valida(operatoreDescrizione)){
//				lab6.setValue(separatore+recuperaLabels("listaAttivitaGrid.attivita.10.respondabile.pianificazione")+": " + operatoreDescrizione); 
//			}
//			lab6.setParent(hTextSud);
//    	}else{	
    		if (ISASUtil.valida(cod_med)){
    			lab6.setValue("   "+Labels.getLabel("tab_voci.mmg")+": "+cod_med+"  "+ cod_med_descr);
    		}
    		lab6.setParent(hTextSud);
//    	}
    	
    	if(livello_alert.equals(ListaAttivitaGridCtrl.LIVELLO_ALERT1)){
    		int num_gg_alert1			= listaAttEjb.getConfNumGGAlert(CaribelSessionManager.getInstance().getMyLogin(),fonte,ListaAttivitaGridCtrl.LIVELLO_ALERT1);
        	String toolTipAlertLiv1		= Labels.getLabel("listaAttivitaGrid.alert.tooltip",new String[]{""+num_gg_alert1});
    		Image imgAlert1 = new Image(iconsPath+"warning24x24.png");
    		imgAlert1.setTooltiptext(toolTipAlertLiv1);
    		imgAlert1.setParent(hL);
    	}else if(livello_alert.equals(ListaAttivitaGridCtrl.LIVELLO_ALERT2)){
    		int num_gg_alert2			= listaAttEjb.getConfNumGGAlert(CaribelSessionManager.getInstance().getMyLogin(),fonte,ListaAttivitaGridCtrl.LIVELLO_ALERT2);
        	String toolTipAlertLiv2		= Labels.getLabel("listaAttivitaGrid.alert.tooltip",new String[]{""+num_gg_alert2});	
    		Image imgAlert2 = new Image(iconsPath+"alert24x24.png");  	
    		imgAlert2.setTooltiptext(toolTipAlertLiv2);
    		imgAlert2.setParent(hL);
    	}
    	
    	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
    	if(iu.canIUse(ChiaviISASSinssntWeb.LISTA_ATTIVITA,ChiaviISASSinssntWeb.CANC)){
//	    	if ((!ISASUtil.valida(fonte)) || 
//	    		( ( (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONE_BISOGNI) ) &&
//	    		  ( (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_VALUTAZIONI_UVI) ) &&
//	    		( (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_MANCATA_ATTIVAZIONE) ) &&
//	    		( (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_SEGNALAZIONI) )
////	    		&& ( (numeroFonte != CostantiSinssntW.CTS_TIPO_FONTE_ACCESSI_PIANIFICATI_NON_CONSUNTIVATI) )
//	    		)	){
			if (numeroFonte >= 0
					&& ((numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_SO_VISTE) || 
						 numeroFonte == CostantiSinssntW.CTS_TIPO_FONTE_COINVOLTI)) {
				Div mioDiv = new Div();
				mioDiv.setVflex("true");
				mioDiv.setStyle("vertical-align: middle;");
				Image imgDelete = new Image("~./zul/img/delete24x24.png");
				imgDelete.setStyle("vertical-align: middle;");
				imgDelete.setTooltiptext(toolTipDelete);
				ComponentsCtrl.applyForward(imgDelete, "onClick=onClickedDeleteButton");
				imgDelete.setParent(mioDiv);
				mioDiv.setParent(hL);
			}
    	}
		
    	item.appendChild(lc);
    }

	private boolean recuperaSeAidAdpVsd(ISASRecord dbr, String tipoCura) {
		boolean is_AID_ADP_VSD = false;
		
		if (ISASUtil.valida(tipoCura) && tipoCura.equals(CostantiSinssntW.CTS_COD_CURE_PRESTAZIONALI)){
			String aid = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_AID);
			String ard = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_ARD);
			String vsd = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_VSD);
			
			is_AID_ADP_VSD = (ISASUtil.valida(aid) && aid.equalsIgnoreCase(Costanti.CTS_S))
					|| (ISASUtil.valida(ard) && ard.equalsIgnoreCase(Costanti.CTS_S))
					|| (ISASUtil.valida(vsd) && vsd.equalsIgnoreCase(Costanti.CTS_S));
		}
		return is_AID_ADP_VSD;
	}

	private boolean recuperaSeAdp(ISASRecord dbr, String tipoCura) {
		boolean is_ADP = false;
		if (ISASUtil.valida(tipoCura) && tipoCura.equals(CostantiSinssntW.CTS_COD_CURE_PRESTAZIONALI)){
			String adp = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_RMSKSO_ADP);
			is_ADP = (ISASUtil.valida(adp) && adp.equalsIgnoreCase(Costanti.CTS_S));
		}
		return is_ADP;
	}

	public String recuperaDescrizioneFonte(String fonte, String fonteDettaglio) {
		String labelFonte = "listaAttivitaGrid.attivita."+fonte + "."+fonteDettaglio;
    	String descrizioneFonte = recuperaLabels(labelFonte);
    	if ( (descrizioneFonte == null) || (!ISASUtil.valida(descrizioneFonte)) ){
    		labelFonte = "listaAttivitaGrid.attivita."+fonte;
    		descrizioneFonte = recuperaLabels(labelFonte);
    	}
    	return descrizioneFonte;
	}

	private String recuperaLabels(String label) {
		Object obj = Labels.getLabel(label); 
		return  (obj!=null ? obj+"":"");
	}

}
package it.caribel.app.sinssnt.controllers;

import it.caribel.app.common.connection.GenericConnectionEJB;
import it.caribel.app.common.controllers.integrazioni.AsterViewLauncher;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.zk.generic_controllers.CaribelContainerCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.util.ISASUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;

public abstract class ContainerSinssntCtrl extends CaribelContainerCtrl {
	
	private static final long serialVersionUID = -4485728437640194375L;
	Hashtable<String, String> hIdUrizul = new Hashtable<String, String>();
	protected String urlAsterView;
	protected boolean conAsterView = false;
	
	protected void initContainer(){
		urlAsterView = getProfile().getStringFromProfile("url_asterview");
		this.conAsterView  = ((urlAsterView != null) && (!urlAsterView.trim().equals(""))
                && (!urlAsterView.trim().equals("NO")));
		if(conAsterView){
			Execution exec = Executions.getCurrent();
			if(urlAsterView.startsWith("/")){
				urlAsterView=exec.getScheme()+"://"+exec.getServerName() + (exec.getServerPort()!=80 ? ":"+exec.getServerPort() : "" ) + urlAsterView;
			}else if(!urlAsterView.startsWith("http")){
				urlAsterView+=exec.getScheme()+"://";
			}
		}
	}
	
	@Listen("onClick=#btn_documenti")
	public void btn_documenti() {
		try{
			this.setProvenienza("CD");
			HashMap<String, Object> hdihc = new HashMap<String, Object>(); // hashtable per il passaggio dei parametri
//			hdihc.put("accertamentoForm", 1 + "");
			
			String codOperatore = getProfile().getMyLogin().getUser();
			String password = getProfile().getMyLogin().getPassword();
			// -----------------------------------------------------------
			String cognomeAssistito = (String) hashChiaveValore.get(Costanti.ASSISTITO_COGNOME);
			String nomeAssistito = (String) hashChiaveValore.get(Costanti.ASSISTITO_NOME);
			String datiNascita = (String) hashChiaveValore.get(CostantiSinssntW.COD_COM_NASC);
			String datiNascitaDesc = (String) hashChiaveValore.get("desc_com_nasc");
	        
//	        String datiResidenza = (String) currentIsasRecord.get("residenza");
	        String dtns = hashChiaveValore.get("data_nasc").toString();
			String codiceFiscale= (String) hashChiaveValore.get(Costanti.ASSISTITO_COD_FISC);;
			String area= ManagerProfile.getPresidioOperatore(getProfile()); //cod_areadis_dom.getValue();
			String desArea= ManagerProfile.getOperatoreDescrizioneSede(getProfile());//(String) currentIsasRecord.get("desc_distretto"); //(String)JCariTextFieldDescDistr.getUnmaskedText();

			/*****************************************************************
			 * SE NON E' STATO EFFETTUATO IL SALVATAGGIO
			 * SI PROVVEDE A FARLO
			 *****************************************************************/
			String n_cartella = ISASUtil.getValoreStringa(hashChiaveValore, Costanti.N_CARTELLA);
			String n_contatto = ISASUtil.getValoreStringa(hashChiaveValore, CostantiSinssntW.N_CONTATTO);


			logger.debug("******Codice accertamento= " + n_cartella + "_" + getProvenienza());

//			Hashtable<String, Object> hdihc = new Hashtable<String, Object>(); 

			hdihc.put("ACCERTAMENTO_ID", "SINSSNT_"+n_cartella + "_" + getProvenienza()); //idminori.getValue());
			Object data = hashChiaveValore.get(CostantiSinssntW.ASSISTITO_CARTELLA_APERTA);
			String date;
			if(data instanceof Date){
				date = UtilForBinding.getValueForIsas((java.sql.Date) data);
			}else if (data instanceof Date ) {
				date = (String) data;
			}else{
				date = data.toString();
			}

			hdihc.put("ACCERTAMENTO_DATA", date);
			hdihc.put("ACCERTAMENTO_EVENTO_SOCIOSAN_DESCRIZIONE", "CURE DOMICILIARI");

			hdihc.put("TIPO_ACCERTAMENTO_CODICE",  getProvenienza());
			//	        hdihc.put("TIPO_ACCERTAMENTO_DESCRIZIONE", "Consultorio Percorso Nascita");

			hdihc.put("STRUTTURA_CODICE", area);
			hdihc.put("STRUTTURA_DESCRIZIONE", desArea);

			hdihc.put("ASSISTITO_CODICE_FISCALE", codiceFiscale);

			hdihc.put("ASSISTITO_COGNOME", cognomeAssistito);
			hdihc.put("ASSISTITO_NOME", nomeAssistito);

	        if (!dtns.equals("/__/")) {
	            hdihc.put("ASSISTITO_DATA_NASCITA", dtns);
	        }
	        hdihc.put("ASSISTITO_COMUNE_NASCITA_CODICE_ISTAT", datiNascita);
	        hdihc.put("ASSISTITO_COMUNE_NASCITA_DESCRIZIONE", datiNascitaDesc);

			hdihc.put("username", codOperatore);
			hdihc.put("password", password);
			hdihc.put("procedure_name", GenericConnectionEJB.appName);

			hdihc.put("OPERATORE_COGNOME", getProfile().getStringFromProfile(ManagerProfile.COGNOME_OPERATORE));
			hdihc.put("OPERATORE_NOME", getProfile().getStringFromProfile(ManagerProfile.NOME_OPERATORE));
			hdihc.put("OPERATORE_QUALIFICA_CODICE",	getProfile().getStringFromProfile(ManagerProfile.QUAL_OPERATORE));
			hdihc.put("OPERATORE_CODICE_FISCALE", getProfile().getStringFromProfile(ManagerProfile.CF_OPERATORE));

			String insertURL = "";
			String appletURL = getProfile().getStringFromProfile("WEBHS_RICHIESTA");

			Object[] params = new Object[3];
			params[0] = insertURL;
			params[1] = appletURL;
			params[2] = hdihc;

			hdihc.put("application_url",  appletURL);
			Component comp = hashIdComponent.get("accertamentoForm");
			removeComponent(comp);
			if(comp != null) {
				comp.detach();
			}
			comp = Executions.createComponents(hIdUrizul.get("documenti"), self, hdihc);
			hashIdComponent.put(comp.getId(), comp);
			
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Listen("onClick=#btn_asterview")
	public void btn_asterview() {
			try{
				if(formNeedApprovalForSave()){			
					executeApprovalForSaveBeforShowComponent("contattoInfForm",hIdUrizul.get("contattoInfForm"),true,null);
				}else{
					if(conAsterView){
						String user   = getProfile().getMyLogin().getUser();
						String passwd = getProfile().getMyLogin().getPassword();
						String codFisc = (String) hashChiaveValore.get(Costanti.ASSISTITO_COD_FISC);
		
						String nmProc = "/loadPatient";
						String toCall = "" + urlAsterView 
								+ "/" + nmProc
								+ "?USER=" + user
								+ "&ID509=" + passwd
								+ "&cifrcf=" + codFisc;
						logger.info("JCCPUACCartella.apriAsterView() - 2 - toCall=["+toCall+"]");
	//					Executions.getCurrent().sendRedirect(toCall, "_blank");
						AsterViewLauncher.launchAsterview(self, toCall);
					}
				}
			}catch(Exception e){
				doShowException(e);
			}
		}
}

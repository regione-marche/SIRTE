package it.caribel.app.sinssnt.controllers.menu.menu_left_operazioni.data;

import it.caribel.app.rsa.controllers.graduatoriaRsa.GraduatoriaRsaCtrl;
import it.caribel.app.sinssnt.controllers.accessi_effettuati.AccessiEffettuatiGridCtrl;
import it.caribel.app.sinssnt.controllers.accessi_mmg_convalida.AccessiMmgConvalidaCtrl;
import it.caribel.app.sinssnt.controllers.attribuzione_operatore_referente.AttribuzioneOperatoreReferenteCtrl;
import it.caribel.app.sinssnt.controllers.chiusura_contatti.ChiusuraContattiCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.login.ManagerProfileBase;
import it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm.CommissioneUvmFormCtrl;
import it.caribel.app.sinssnt.util.ChiaviIsasBase;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.UtilForContainer;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import it.pisa.caribel.isas2.ISASUser;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;

public class ListaMenu {

	ISASUser iu = CaribelSessionManager.getInstance().getIsasUser();
	CaribelSessionManager profile = CaribelSessionManager.getInstance();

	private MenuTreeNode root;

	MenuItem elenco_segnalazioni = new MenuItem (Labels.getLabel("menu.operazioni.elenco.segnalazioni"));
	MenuItem progetti_contatti = new MenuItem (Labels.getLabel("menu.operazioni.ricerca.progetti_contatti"));
	MenuItem attribuzione_segnalazioni = new MenuItem (Labels.getLabel("menu.operazioni.attribuzione.segnalazioni"));
	MenuItem accessi_prestazioni = new MenuItem (Labels.getLabel("menu.operazioni.accessi_prestazioni"));
	MenuItem chiusura_contatti = new MenuItem (Labels.getLabel("menu.operazioni.chiusura_contatti"));
	MenuItem attribuzioneOperatoreReferente = new MenuItem (Labels.getLabel("menu.operazioni.attribuzione.operatore.referente"));
	MenuItem estrazione_flussi_siad = new MenuItem (Labels.getLabel("menu.operazioni.estr.flussi.siad"));
	MenuItem accessi_occasionali = new MenuItem (Labels.getLabel("menu.operazioni.accessi_occasionali"));
	MenuItem riepilogoAccessi = new MenuItem (getTitoloRiepilogoAccessi());
	MenuItem accessi_mmg_registrazione = new MenuItem (Labels.getLabel("menu.operazioni.accessi_mmg"));
	MenuItem accessi_mmg_convalida = new MenuItem (Labels.getLabel("menu.operazioni.accessi_mmg_convalida"));
	MenuItem commissioni_uvm = new MenuItem (Labels.getLabel("menu.tabelle.commissione_uvm"));

	MenuItem estrazione_rug_iii_hc = new MenuItem (Labels.getLabel("menu.stampe.estr.rug_iii_hc"));
	MenuItem stampe_fls21 = new MenuItem (Labels.getLabel("menu.stampe.FLS21"));
	
	MenuItem graduatoriaRsa = new MenuItem (Labels.getLabel("menu.operazioni.graduatoria.rsa"));
	
	private void costruisciNodi(){
		commissioni_uvm.setKeyPermission(CommissioneUvmFormCtrl.myKeyPermission);
		commissioni_uvm.setPathZul("/web/ui/sinssnt/tabelle/commissione_uvm/commissioneUvmGrid.zul");

		root = new MenuTreeNode(null,
				new MenuTreeNode[] {
//				new MenuTreeNode(elenco_segnalazioni),
//				new MenuTreeNode(progetti_contatti),
//				new MenuTreeNode(attribuzione_segnalazioni),
				new MenuTreeNode(accessi_prestazioni),
				new MenuTreeNode(accessi_occasionali),
				new MenuTreeNode(riepilogoAccessi),
				new MenuTreeNode(accessi_mmg_registrazione),
				new MenuTreeNode(accessi_mmg_convalida),
				new MenuTreeNode(chiusura_contatti),
				new MenuTreeNode(attribuzioneOperatoreReferente),
				new MenuTreeNode(commissioni_uvm),
				new MenuTreeNode(estrazione_flussi_siad),
				
				new MenuTreeNode(estrazione_rug_iii_hc),
				new MenuTreeNode(stampe_fls21),	
				new MenuTreeNode(graduatoriaRsa)
				
		});
		
		try {
			accessi_prestazioni.setKeyPermission(ChiaviISASSinssntWeb.INTERV);
			accessi_prestazioni.setPathZul("/web/ui/sinssnt/interventi/accessiPrestazioniForm.zul");
			Hashtable<String, String> argForZul = new Hashtable<String, String>();
			argForZul.put("provAccessiPrestazioni", 1+"");
			argForZul.put("mode", "overlapped");
			accessi_prestazioni.setArgForZul(argForZul);
			chiusura_contatti.setKeyPermission(ChiusuraContattiCtrl.myKeyPermission);
			chiusura_contatti.setPathZul(ChiusuraContattiCtrl.myPathFormZul);
			attribuzioneOperatoreReferente.setKeyPermission(AttribuzioneOperatoreReferenteCtrl.myKeyPermission);
			attribuzioneOperatoreReferente.setPathZul(AttribuzioneOperatoreReferenteCtrl.myPathFormZul);
			accessi_mmg_registrazione.setKeyPermission(ChiaviISASSinssntWeb.TABPIPP);
			graduatoriaRsa.setKeyPermission(ChiaviIsasBase.LISTA_GRADUATORIA_RSA);
			graduatoriaRsa.setPathZul(GraduatoriaRsaCtrl.myPathFormZul);
			Hashtable<String, String> argForZulRsa = new Hashtable<String, String>();
			argForZulRsa.put(GraduatoriaRsaCtrl.CTS_PROVENIENZA_APPLICATIVO, GraduatoriaRsaCtrl.CTS_PROVENIENZA_APPLICATIVO_SINSSNT_WEB2+"");
			argForZulRsa.put(Costanti.CTS_COD_ISTITUTO_SU_CUI_OPERARE, ManagerProfile.getCodIstitutoOperatore(profile) );
			argForZulRsa.put(ManagerProfileBase.UBICAZIONE, ManagerProfile.getCodiceUbicazione(profile)+"");
			System.out.println(" dati che invio>>" + argForZulRsa);
			graduatoriaRsa.setArgForZul(argForZulRsa);
			
			accessi_mmg_registrazione.setKeyPermission(ChiaviISASSinssntWeb.TABPIPP);
			accessi_mmg_registrazione.setPathZul("/web/ui/sinssnt/accessi_mmg/accessi_mmg_wa.zul");
			accessi_mmg_convalida.setKeyPermission(ChiaviISASSinssntWeb.CONS_MIL);
			accessi_mmg_convalida.setPathZul(AccessiMmgConvalidaCtrl.myPathZul);
			estrazione_flussi_siad.setKeyPermission(ChiaviISASSinssntWeb.ESTRAZIONE_FLUSSI);
			estrazione_flussi_siad.setPathZul("/web/ui/sinssnt/flussi/estrattore_flussi_siad.zul");
			estrazione_rug_iii_hc.setKeyPermission(ChiaviISASSinssntWeb.ESTRAZIONE_RUG_IIIHC);
			estrazione_rug_iii_hc.setPathZul("/web/ui/report/reportEstrRugIIIHC.zul");
			stampe_fls21.setKeyPermission(ChiaviISASSinssntWeb.FLS21);
			stampe_fls21.setPathZul("/web/ui/report/reportFLS21.zul");	
			accessi_occasionali.setKeyPermission(ChiaviISASSinssntWeb.ACCSPE);
			accessi_occasionali.setPathZul("/web/ui/sinssnt/interventi/accessiPrestazioniForm.zul");
			Hashtable<String, String> argForZulO = new Hashtable<String, String>();
			argForZulO.put("provAccessiPrestazioni", 2+"");
			argForZulO.put("mode", "overlapped");
			accessi_occasionali.setArgForZul(argForZulO);
			
			riepilogoAccessi.setKeyPermission(ChiaviISASSinssntWeb.RIEPILO_ACCESSI);
			riepilogoAccessi.setPathZul(AccessiEffettuatiGridCtrl.myPathFormZul);
			Hashtable<String, String> dati = new Hashtable<String, String>();
			dati.put(AccessiEffettuatiGridCtrl.CTS_PROVENIENZA_OPERAZIONE, AccessiEffettuatiGridCtrl.CTS_PROVENIENZA_FROM_OPERAZIONE);
			dati.put(AccessiEffettuatiGridCtrl.CTS_TITOLO_FORM,getTitoloRiepilogoAccessi());
			riepilogoAccessi.setArgForZul(dati);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTitoloRiepilogoAccessi() {
		return Labels.getLabel("menu.operazioni.riepilogo_accessi");
	}

	public ListaMenu() {		
		impostaConfigurazioni();
		costruisciNodi();
	}
	public MenuTreeNode getRoot() {
		return root;
	}

	private void impostaConfigurazioni(){
		//##################OPERAZIONI##################
		visualMenuAS();

	}

	// visualizza voci del Menu Operazioni solo x gli ass sociali e (dal 05/12/07) gli infermieri
	private void visualMenuAS()
	{
		boolean abilxAS = iu.canIUse("CONTATTI");
		boolean abilxINF = iu.canIUse("SKINF"); // 05/07/12
		boolean abil = (abilxAS || abilxINF);
		elenco_segnalazioni.setEnabled(abil);
		if (abilxAS && !abilxINF)
			progetti_contatti.setLabel(Labels.getLabel("menu.operazioni.ricerca.progetti"));
		else if (abilxINF && !abilxAS)
			progetti_contatti.setLabel(Labels.getLabel("menu.operazioni.ricerca.contatti"));
		else if (abilxAS && abilxINF)
			progetti_contatti.setLabel(Labels.getLabel("menu.operazioni.ricerca.progetti_contatti"));
		progetti_contatti.setEnabled(abil);
		// 19/11/08: attribuzione casi ad oper (gestito da CONF)
		String attrCasi = profile.getStringFromProfile("abil_attr_casi");
		boolean abilAttrCasi = ((attrCasi != null) && (attrCasi.trim().equals("SI")));
		attribuzione_segnalazioni.setEnabled(abilAttrCasi);
	}
	
	public void onClick$accessi_prestazioni(Event event) throws Exception{
		Hashtable<String, String> argForZul = new Hashtable<String, String>();
		argForZul.put("tipoOp", UtilForContainer.getTipoOperatorerContainer());
		argForZul.put("provAccessiPrestazioni", 1+"");
		argForZul.put("mode", "overlapped");
		accessi_prestazioni.setArgForZul(argForZul);
		
	}
 
}

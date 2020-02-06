package it.caribel.app.sinssnt.controllers.menu.menu_left_stampe.data;

import it.caribel.app.sinssnt.controllers.report.ReportMonitoraggioCureDomiciliariCtrl;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import java.util.Hashtable;
import org.zkoss.util.resource.Labels;

public class ListaMenu {

	private MenuTreeNode root;

	public ListaMenu() {

		// ################STAMPE
		// INFERMIERISTICA######################################
		MenuItem stampe_infermieristica = new MenuItem(Labels.getLabel("menu.stampe.infermieristica"));
		String[] keysInf = new String[] { "ELASSINF", // 0
				"PMMG3", // 1
				"ELASSMOT", // 2
				"ELINFINV", // 3
				"STMTELAS", // 4
				"TIPODIM", // 5
				"STIAODIM" // 6
		};
		stampe_infermieristica.setKeyPermissionChildrens(keysInf);
		MenuItem assistiti_contatto_aperto = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_contatto_aperto"));
		assistiti_contatto_aperto.setKeyPermission(keysInf[0]);
		assistiti_contatto_aperto.setPathZul("/web/ui/report/reportInfContattiAp.zul");

		MenuItem assistiti_MMG_contatto_aperto = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_MMG_contatto_aperto"));
		assistiti_MMG_contatto_aperto.setKeyPermission(keysInf[1]);
		assistiti_MMG_contatto_aperto.setPathZul("/web/ui/report/reportInfMMGContattiAp.zul");

		MenuItem assistiti_motivo_dimissione = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_motivo_dimissione"));
		assistiti_motivo_dimissione.setKeyPermission(keysInf[2]);
		assistiti_motivo_dimissione.setPathZul("/web/ui/report/reportInfMotivoDim.zul");

		MenuItem assistiti_soggetto_inviante = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_soggetto_inviante"));
		assistiti_soggetto_inviante.setKeyPermission(keysInf[3]);
		assistiti_soggetto_inviante.setPathZul("/web/ui/report/reportInfSogInviante.zul");

		MenuItem assistiti_segnalazione_motivo = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_segnalazione_motivo"));
		assistiti_segnalazione_motivo.setKeyPermission(keysInf[4]);
		assistiti_segnalazione_motivo.setPathZul("/web/ui/report/reportInfSegMotivo.zul");

		MenuItem assistiti_riep_attivita_utente_dim = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_riep_attivita_utente_dim"));
		assistiti_riep_attivita_utente_dim.setKeyPermission(keysInf[5]);
		assistiti_riep_attivita_utente_dim.setPathZul("/web/ui/report/reportInfRiepAttUtDim.zul");

		MenuItem assistiti_ospedale_dimissione = new MenuItem(
				Labels.getLabel("menu.stampe.infermieristica.assistiti_ospedale_dimissione"));
		assistiti_ospedale_dimissione.setKeyPermission(keysInf[6]);
		assistiti_ospedale_dimissione.setPathZul("/web/ui/report/reportInfOspedaleDim.zul");
		Hashtable<String, String> assistiti_ospedale_dimissione_args = new Hashtable<String, String>();
		assistiti_ospedale_dimissione_args.put("tipoOpe", "I");
		assistiti_ospedale_dimissione.setArgForZul(assistiti_ospedale_dimissione_args);

		// ################STAMPE
		// MEDICA##############################################
		MenuItem stampe_medica = new MenuItem(Labels.getLabel("menu.stampe.medica"));
		String[] keysMed = new String[] { "ELASSMED", // 0
				"STIAODIM" // 1
		};
		stampe_medica.setKeyPermissionChildrens(keysMed);
		MenuItem assistiti_medici_contatto_aperto = new MenuItem(
				Labels.getLabel("menu.stampe.medica.assistiti_medici_contatto_aperto"));
		assistiti_medici_contatto_aperto.setKeyPermission(keysMed[0]);
		assistiti_medici_contatto_aperto.setPathZul("/web/ui/report/reportMedContattiAperti.zul");

		MenuItem assistiti_medici_ospedale_dimissione = new MenuItem(
				Labels.getLabel("menu.stampe.medica.assistiti_ospedale_dimissione"));
		assistiti_medici_ospedale_dimissione.setKeyPermission(keysMed[1]);
		assistiti_medici_ospedale_dimissione.setPathZul("/web/ui/report/reportInfOspedaleDim.zul");
		Hashtable<String, String> assistiti_medici_ospedale_dimissione_args = new Hashtable<String, String>();
		assistiti_medici_ospedale_dimissione_args.put("tipoOpe", "M");
		assistiti_medici_ospedale_dimissione.setArgForZul(assistiti_medici_ospedale_dimissione_args);

		// ################STAMPE
		// FISIOTERAPICA##############################################
		MenuItem stampe_fisioterapica = new MenuItem(Labels.getLabel("menu.stampe.fisioterapica"));
		String[] keysFisio = new String[] { "ELASSFIS" // 0
		};
		stampe_fisioterapica.setKeyPermissionChildrens(keysFisio);
		MenuItem assistiti_fisio_contatto_aperto = new MenuItem(
				Labels.getLabel("menu.stampe_fisioterapica.assistiti_fisio_contatto_aperto"));
		assistiti_fisio_contatto_aperto.setKeyPermission(keysFisio[0]);
		assistiti_fisio_contatto_aperto.setPathZul("/web/ui/report/reportFisioContattiAperti.zul");

		// ################STAMPE CURE
		// PALLIATIVE##############################################
		MenuItem stampe_cure_palliative = new MenuItem(Labels.getLabel("menu.stampe.cure_palliative"));
		String[] keysPal = new String[] { "ELEASDPA", // 0
				"MOVMEDPA", // 1
				"RIEP_DEC", // 2
				"CONTA_GG", // 3
				"CONTA_DG" // 4
		};
		stampe_cure_palliative.setKeyPermissionChildrens(keysPal);
		MenuItem assistiti_cure_palliative = new MenuItem(
				Labels.getLabel("menu.stampe_cure_palliative.assistiti_cure_palliative"));
		assistiti_cure_palliative.setKeyPermission(keysPal[0]);
		assistiti_cure_palliative.setPathZul("/web/ui/report/reportCurePalAssistiti.zul");

		MenuItem movimenti_cure_palliative = new MenuItem(
				Labels.getLabel("menu.stampe_cure_palliative.movimenti_cure_palliative"));
		movimenti_cure_palliative.setKeyPermission(keysPal[1]);
		movimenti_cure_palliative.setPathZul("/web/ui/report/reportCurePalMovimenti.zul");

		MenuItem deceduti_cure_palliative = new MenuItem(
				Labels.getLabel("menu.stampe_cure_palliative.deceduti_cure_palliative"));
		deceduti_cure_palliative.setKeyPermission(keysPal[2]);
		deceduti_cure_palliative.setPathZul("/web/ui/report/reportCurePalDeceduti.zul");

		MenuItem giorniAss_cure_palliative = new MenuItem(
				Labels.getLabel("menu.stampe_cure_palliative.giorniAss_cure_palliative"));
		giorniAss_cure_palliative.setKeyPermission(keysPal[3]);
		giorniAss_cure_palliative.setPathZul("/web/ui/report/reportCurePalGiorniAss.zul");

		MenuItem diagnosi_cure_palliative = new MenuItem(
				Labels.getLabel("menu.stampe_cure_palliative.diagnosi_cure_palliative"));
		diagnosi_cure_palliative.setKeyPermission(keysPal[4]);
		diagnosi_cure_palliative.setPathZul("/web/ui/report/reportCurePalDiagnosi.zul");

		// ################STAMPE
		// STATISTICHE##############################################
		MenuItem stampe_statistiche = new MenuItem(Labels.getLabel("menu.stampe.statistiche"));
		String[] keysStat = new String[] { "STATPRES", // 0
				"ASSMENS", // 1
				"PAZACQUI", // 2
				"FREQPAT", // 3
				"VASCCER", // 4
				"STATDIME", // 5
				"PRESTDOM", // 6
				"PRENODOM", // 7
				"PRESFREQ" // 8
		};
		stampe_statistiche.setKeyPermissionChildrens(keysStat);

		MenuItem statistiche_riepilogo_prestazioni = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_riepilogo_prestazioni"));
		statistiche_riepilogo_prestazioni.setKeyPermission(keysStat[0]);
		statistiche_riepilogo_prestazioni.setPathZul("/web/ui/report/reportStatRiepPrestazioni.zul");

		MenuItem statistiche_riepilogo_paz_ass = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_riepilogo_paz_ass"));
		statistiche_riepilogo_paz_ass.setKeyPermission(keysStat[1]);
		statistiche_riepilogo_paz_ass.setPathZul("/web/ui/report/reportStatRiepPazAss.zul");

		MenuItem statistiche_riepilogo_paz_mese = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_riepilogo_paz_mese"));
		statistiche_riepilogo_paz_mese.setKeyPermission(keysStat[2]);
		statistiche_riepilogo_paz_mese.setPathZul("/web/ui/report/reportStatRiepPazMese.zul");

		MenuItem statistiche_frequenza_patologie = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_frequenza_patologie"));
		statistiche_frequenza_patologie.setKeyPermission(keysStat[3]);
		statistiche_frequenza_patologie.setPathZul("/web/ui/report/reportStatFreqPat.zul");

		MenuItem statistiche_assistiti_patologie = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_assistiti_patologie"));
		statistiche_assistiti_patologie.setKeyPermission(keysStat[4]);
		statistiche_assistiti_patologie.setPathZul("/web/ui/report/reportStatAssPato.zul");

		MenuItem statistiche_accessi_domiciliari = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_accessi_domiciliari"));
		statistiche_accessi_domiciliari.setKeyPermission(keysStat[5]);
		statistiche_accessi_domiciliari.setPathZul("/web/ui/report/reportStatAccessiDom.zul");

		MenuItem statistiche_prestazioni_domiciliari = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_prestazioni_domiciliari"));
		statistiche_prestazioni_domiciliari.setKeyPermission(keysStat[6]);
		statistiche_prestazioni_domiciliari.setPathZul("/web/ui/report/reportStatPrestDom.zul");

		MenuItem statistiche_prestazioni_no_domiciliari = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_prestazioni_no_domiciliari"));
		statistiche_prestazioni_no_domiciliari.setKeyPermission(keysStat[7]);
		statistiche_prestazioni_no_domiciliari.setPathZul("/web/ui/report/reportStatPrestNoDom.zul");

		MenuItem statistiche_frequenza_prestazioni = new MenuItem(
				Labels.getLabel("menu.stampe_statistiche.statistiche_frequenza_prestazioni"));
		statistiche_frequenza_prestazioni.setKeyPermission(keysStat[8]);
		statistiche_frequenza_prestazioni.setPathZul("/web/ui/report/reportStatFreqPrest.zul");

		// ################STAMPE
		// STATISTICHE##############################################
		MenuItem stampe_ele_ass_accessi = new MenuItem(Labels.getLabel("menu.stampe.ele_ass_accessi"));
		stampe_ele_ass_accessi.setKeyPermission("PINFREF3");
		stampe_ele_ass_accessi.setPathZul("/web/ui/report/reportEleAssAccessi.zul");

		MenuItem stampe_ele_ass_ope = new MenuItem(Labels.getLabel("menu.stampe.ele_ass_ope"));
		stampe_ele_ass_ope.setKeyPermission("PELASSOP");
		stampe_ele_ass_ope.setPathZul("/web/ui/report/reportEleAssOpe.zul");

		MenuItem stampeRiepilogoAssisti = new MenuItem(Labels.getLabel("menu.stampe.riepilogo_assistiti"));
		stampeRiepilogoAssisti.setKeyPermission(ChiaviISASSinssntWeb.CTS_RIEPILOGO_ASSISTITI);
		stampeRiepilogoAssisti.setPathZul("/web/ui/report/reportRiepilogoAssistiti.zul");

		MenuItem stampe_elenco_prestazioni = new MenuItem(Labels.getLabel("menu.stampe.elenco_prestazioni"));
		stampe_elenco_prestazioni.setKeyPermission("PINFREF2");
		stampe_elenco_prestazioni.setPathZul("/web/ui/report/reportElencoPrestazioni.zul");
		
		MenuItem stampeMonitoraggioCureDomiciliari = new MenuItem(Labels.getLabel("menu.stampe.monitoraggio.cure.cd"));
		stampeMonitoraggioCureDomiciliari.setKeyPermission(ChiaviISASSinssntWeb.STAMPA_MONITORAGGIO_CURE_DOMICILIARI);
		stampeMonitoraggioCureDomiciliari.setPathZul(ReportMonitoraggioCureDomiciliariCtrl.PATH_ZUL);

		MenuItem stampe_riep_ass_eta = new MenuItem(Labels.getLabel("menu.stampe.riep_ass_eta"));
		stampe_riep_ass_eta.setKeyPermission("PSTATUTE");
		stampe_riep_ass_eta.setPathZul("/web/ui/report/reportRiepAssEta.zul");

		MenuItem stampe_riep_prest_branca = new MenuItem(Labels.getLabel("menu.stampe.riep_prest_branca"));
		stampe_riep_prest_branca.setKeyPermission("RIEPBRAN");
		stampe_riep_prest_branca.setPathZul("/web/ui/report/reportRiepPrestBranca.zul");

		MenuItem stampe_ele_accessi_op = new MenuItem(Labels.getLabel("menu.stampe.ele_accessi_op"));
		stampe_ele_accessi_op.setKeyPermission("PRESTOPE");
		stampe_ele_accessi_op.setPathZul("/web/ui/report/reportEleAccessiOp.zul");

		MenuItem stampe_cartelle_chiuse = new MenuItem(Labels.getLabel("menu.stampe.cartelle_chiuse"));
		stampe_cartelle_chiuse.setKeyPermission("STACACHI");
		stampe_cartelle_chiuse.setPathZul("/web/ui/report/reportCartelleChiuse.zul");

		MenuItem stampe_ore_ass = new MenuItem(Labels.getLabel("menu.stampe.ore_ass"));
		stampe_ore_ass.setKeyPermission("OREASS");
		stampe_ore_ass.setPathZul("/web/ui/report/reportOreAss.zul");

		MenuItem stampe_riep_prest_dett_ore = new MenuItem(Labels.getLabel("menu.stampe.riep_prest_dett_ore"));
		stampe_riep_prest_dett_ore.setKeyPermission("DETTORE");
		stampe_riep_prest_dett_ore.setPathZul("/web/ui/report/reportRiepPrestDettOre.zul");

		MenuItem stampe_ele_ass_no_pato = new MenuItem(Labels.getLabel("menu.stampe.ele_ass_no_pato"));
		stampe_ele_ass_no_pato.setKeyPermission("ASSNOPAT");
		stampe_ele_ass_no_pato.setPathZul("/web/ui/report/reportEleAssNoPato.zul");

		MenuItem stampe_ele_piani_asssist = new MenuItem(Labels.getLabel("menu.stampe.ele_piani_asssist"));
		stampe_ele_piani_asssist.setKeyPermission("RLELPASS");
		stampe_ele_piani_asssist.setPathZul("/web/ui/report/reportElePianiAssist.zul");

		MenuItem stampe_ele_piani_ass_scad = new MenuItem(Labels.getLabel("menu.stampe.ele_piani_ass_scad"));
		stampe_ele_piani_ass_scad.setKeyPermission("STPASCAD");
		stampe_ele_piani_ass_scad.setPathZul("/web/ui/report/reportElencoPianiAssScad.zul");

		MenuItem stampe_ele_assist_liv_ass = new MenuItem(Labels.getLabel("menu.stampe.ele_assist_liv_ass"));
		stampe_ele_assist_liv_ass.setKeyPermission("STASSLIA");
		stampe_ele_assist_liv_ass.setPathZul("/web/ui/report/reportElencoAssistLivAssist.zul");

		MenuItem stampe_ele_sosp_concl = new MenuItem(Labels.getLabel("menu.stampe.ele_sosp_concl"));
		stampe_ele_sosp_concl.setKeyPermission("RLELSOCO");
		stampe_ele_sosp_concl.setPathZul("/web/ui/report/reportEleSospConcl.zul");

		MenuItem stampe_contatti_aperti = new MenuItem(Labels.getLabel("menu.stampe.contatti_aperti"));
		stampe_contatti_aperti.setKeyPermission("ELCONAPE");
		stampe_contatti_aperti.setPathZul("/web/ui/report/reportEleContattiAperti.zul");
		
		// ################STAMPE ALTRO######################################
		MenuItem stampe_altro = new MenuItem(Labels.getLabel("menu.stampe.altro"));
		String[] keysAltro = new String[] { "ELASSALT", // 0
				};		
		stampe_altro.setKeyPermissionChildrens(keysAltro);
		MenuItem assistiti_altro_contatto_aperto = new MenuItem(
		Labels.getLabel("menu.stampe.altro.assistiti_contatto_aperto"));
		assistiti_altro_contatto_aperto.setKeyPermission(keysAltro[0]);
		assistiti_altro_contatto_aperto.setPathZul("/web/ui/report/reportAltroContattiAp.zul");
		
		root = new MenuTreeNode(null, new MenuTreeNode[] {

				new MenuTreeNode(stampe_infermieristica, new MenuTreeNode[] {
						new MenuTreeNode(assistiti_contatto_aperto), new MenuTreeNode(assistiti_MMG_contatto_aperto),
						//new MenuTreeNode(assistiti_motivo_dimissione), new MenuTreeNode(assistiti_soggetto_inviante),
						//new MenuTreeNode(assistiti_segnalazione_motivo),
						//new MenuTreeNode(assistiti_riep_attivita_utente_dim),
						//new MenuTreeNode(assistiti_ospedale_dimissione) 
						}, false),

				new MenuTreeNode(stampe_medica, new MenuTreeNode[] {
						new MenuTreeNode(assistiti_medici_contatto_aperto),
						//new MenuTreeNode(assistiti_medici_ospedale_dimissione) 
						}, false),

				new MenuTreeNode(stampe_fisioterapica, new MenuTreeNode[] { new MenuTreeNode(
						assistiti_fisio_contatto_aperto) }, false),

				/*new MenuTreeNode(stampe_cure_palliative, new MenuTreeNode[] {
						new MenuTreeNode(assistiti_cure_palliative), new MenuTreeNode(movimenti_cure_palliative),
						new MenuTreeNode(deceduti_cure_palliative), new MenuTreeNode(giorniAss_cure_palliative),
						new MenuTreeNode(diagnosi_cure_palliative) }, false),*/

				new MenuTreeNode(stampe_altro, new MenuTreeNode[] {
								new MenuTreeNode(assistiti_altro_contatto_aperto),
						}, false),
						
				new MenuTreeNode(stampe_statistiche, new MenuTreeNode[] {
						//new MenuTreeNode(statistiche_riepilogo_prestazioni),
						//new MenuTreeNode(statistiche_riepilogo_paz_ass),
						//new MenuTreeNode(statistiche_riepilogo_paz_mese),
						//new MenuTreeNode(statistiche_frequenza_patologie),
						new MenuTreeNode(statistiche_assistiti_patologie),
						//new MenuTreeNode(statistiche_accessi_domiciliari),
						//new MenuTreeNode(statistiche_prestazioni_domiciliari),
						//new MenuTreeNode(statistiche_prestazioni_no_domiciliari),
						//new MenuTreeNode(statistiche_frequenza_prestazioni) 
						}, false),
				

				new MenuTreeNode(stampeRiepilogoAssisti), new MenuTreeNode(stampe_ele_ass_accessi),
				//new MenuTreeNode(stampe_ele_ass_ope), 
				new MenuTreeNode(stampe_elenco_prestazioni),
				new MenuTreeNode(stampeMonitoraggioCureDomiciliari),
				//new MenuTreeNode(stampe_riep_ass_eta), new MenuTreeNode(stampe_riep_prest_branca),
				//new MenuTreeNode(stampe_ele_accessi_op), new MenuTreeNode(stampe_cartelle_chiuse),
				//new MenuTreeNode(stampe_ore_ass), new MenuTreeNode(stampe_riep_prest_dett_ore),
				//new MenuTreeNode(stampe_ele_ass_no_pato), new MenuTreeNode(stampe_ele_piani_asssist),
				new MenuTreeNode(stampe_ele_piani_ass_scad)
				//new MenuTreeNode(stampe_ele_assist_liv_ass),
				//new MenuTreeNode(stampe_ele_sosp_concl), new MenuTreeNode(stampe_contatti_aperti)

		});

	}

	public MenuTreeNode getRoot() {
		return root;
	}
}

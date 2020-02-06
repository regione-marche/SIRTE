package it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle.data;

import it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle.data.MenuItem;
import it.caribel.zk.generic_controllers.menu_tree.tree.dynamic_tree.MenuTreeNode;
import org.zkoss.util.resource.Labels;
import it.caribel.app.common.controllers.areadis.*;
import it.caribel.app.common.controllers.attivita_prest.*;
import it.caribel.app.common.controllers.banche.*;
import it.caribel.app.common.controllers.beneficiario.*;
import it.caribel.app.sinssnt.controllers.tabelle.commissione_uvm.CommissioneUvmFormCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.comuni.ComuniFormCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.operatori.OperatoriFormCtrl;
import it.caribel.app.sinssnt.controllers.tabelle.operatori.OperatoriGridCtrl;
import it.caribel.app.common.controllers.distretti.*;
import it.caribel.app.common.controllers.prestazioni.*;
import it.caribel.app.common.controllers.tipo_prestazioni.*;
import it.caribel.app.common.controllers.grado_parent.*;
import it.caribel.app.common.controllers.medici.*;
import it.caribel.app.common.controllers.rsa_motDim.*;
import it.caribel.app.common.controllers.ospedali.*;
import it.caribel.app.common.controllers.pensioni.*;
import it.caribel.app.common.controllers.pipp.*;
import it.caribel.app.common.controllers.presidi.*;
import it.caribel.app.common.controllers.professione.*;
import it.caribel.app.common.controllers.reparti.*;
import it.caribel.app.common.controllers.specialita.*;
import it.caribel.app.common.controllers.statoProf.*;
import it.caribel.app.common.controllers.tipoMedici.*;
import it.caribel.app.common.controllers.valutatori.*;
import it.caribel.app.common.controllers.zone.*;
import it.caribel.app.sinssnt.controllers.anagrafica.*;
import it.caribel.app.common.controllers.prestaz_bisogni.*;


public class ListaMenu {
	
	private MenuTreeNode root;
	
	public ListaMenu() {
		
		MenuItem aree_distr = new MenuItem (Labels.getLabel("menu.tabelle.aree_distr"));
		aree_distr.setKeyPermission(AreadisFormCtrl.myKeyPermission);
		aree_distr.setPathZul("~./ui/common/areadis/areadisGrid.zul");
		
		MenuItem prestaz_bisogni = new MenuItem (Labels.getLabel("menu.tabelle.prestaz_bisogni"));	
		prestaz_bisogni.setKeyPermission(PrestazBisogniFormCtrl.myKeyPermission);
		prestaz_bisogni.setPathZul("/web/ui/common/prestaz_bisogni/prestazBisogniGrid.zul");
		
		MenuItem attivita_prest = new MenuItem (Labels.getLabel("menu.tabelle.attivita_prest"));
		attivita_prest.setKeyPermission(AttivitaPrestFormCtrl.myKeyPermission);
		attivita_prest.setPathZul("/web/ui/common/attivita_prest/attivitaPrestGrid.zul");
		
		MenuItem banche = new MenuItem (Labels.getLabel("menu.tabelle.banche"));
		banche.setKeyPermission(BancheFormCtrl.myKeyPermission);
		banche.setPathZul("/web/ui/common/banche/bancheGrid.zul");
		
		MenuItem beneficiario = new MenuItem (Labels.getLabel("menu.tabelle.beneficiario"));
		beneficiario.setKeyPermission(BeneficiarioFormCtrl.myKeyPermission);
		beneficiario.setPathZul("/web/ui/common/beneficiario/beneficiarioGrid.zul");
		
		//MenuItem centro_costo = new MenuItem (Labels.getLabel("menu.tabelle.centro_costo"));		
		//centro_costo.setPathZul("/web/ui/common/centro_costo/centroCostoGrid.zul");
		
		//MenuItem cittadin = new MenuItem (Labels.getLabel("menu.tabelle.cittadin"));
		//cittadin.setKeyPermission("CITTADIN");
		//cittadin.setPathZul("~./ui/common/cittadin/cittadinGrid.zul");
		
		MenuItem commissioni_uvm = new MenuItem (Labels.getLabel("menu.tabelle.commissione_uvm"));
		commissioni_uvm.setKeyPermission(CommissioneUvmFormCtrl.myKeyPermission);
		commissioni_uvm.setPathZul("/web/ui/sinssnt/tabelle/commissione_uvm/commissioneUvmGrid.zul");
		
		MenuItem comuni = new MenuItem (Labels.getLabel("menu.tabelle.comuni"));
		comuni.setKeyPermission(ComuniFormCtrl.myKeyPermission);
		comuni.setPathZul("~./ui/sinssnt/tabelle/comuni/comuniGrid.zul");
		//MenuItem conti_eco = new MenuItem (Labels.getLabel("menu.tabelle.conti_eco"));
		//conti_eco.setPathZul("/web/ui/common/rsa_conti/rsaContiGrid.zul");
		//MenuItem contributi = new MenuItem (Labels.getLabel("menu.tabelle.contributi"));
		//contributi.setPathZul("/web/ui/common/contributi/contributiGrid.zul");
		
		MenuItem distretti = new MenuItem (Labels.getLabel("menu.tabelle.distretti"));	
		distretti.setKeyPermission(DistrettiFormCtrl.myKeyPermission);
		distretti.setPathZul("~./ui/common/distretti/distrettiGrid.zul");		
		
		//MenuItem esenzioni = new MenuItem (Labels.getLabel("menu.tabelle.esenzioni"));		
		//esenzioni.setPathZul("/web/ui/common/esenzioni/esenzioniGrid.zul");	
		
		MenuItem gestione_prestazioni = new MenuItem (Labels.getLabel("menu.tabelle.gestione_prestazioni"));
		String[] keyPrestaz = new String[]{
				PrestazioniFormCtrl.myKeyPermission,
				TipoPrestazioniFormCtrl.myKeyPermission
		};
		gestione_prestazioni.setKeyPermissionChildrens(keyPrestaz);
		MenuItem prestazioni = new MenuItem(Labels.getLabel("menu.tabelle.prestazioni"));
		prestazioni.setKeyPermission(keyPrestaz[0]);
		prestazioni.setPathZul("/web/ui/common/prestazioni/prestazioniGrid.zul");
		MenuItem tipo_prestazioni = new MenuItem(Labels.getLabel("menu.tabelle.tipo_prestazioni"));
		tipo_prestazioni.setKeyPermission(keyPrestaz[1]);
		tipo_prestazioni.setPathZul("~./ui/common/tipo_prestazioni/tipoPrestazioniGrid.zul");
		
		MenuItem grado_parent = new MenuItem (Labels.getLabel("menu.tabelle.grado_parent"));
		grado_parent.setKeyPermission(GradoParentFormCtrl.myKeyPermission);
		grado_parent.setPathZul("/web/ui/common/grado_parent/gradoParentGrid.zul");
		
		//MenuItem istituti = new MenuItem (Labels.getLabel("menu.tabelle.istituti"));	
		//istituti.setKeyPermission(IstitutiFormCtrl.myKeyPermission);
		//istituti.setPathZul("/web/ui/common/istituti/istitutiGrid.zul");
		
		MenuItem medici = new MenuItem (Labels.getLabel("menu.tabelle.medici"));	
		medici.setKeyPermission(MediciFormCtrl.myKeyPermission);
		medici.setPathZul("~./ui/common/medici/mediciGrid.zul");	
		
		MenuItem rsa_motDim = new MenuItem (Labels.getLabel("menu.tabelle.rsaMotDimGrid"));	
		rsa_motDim.setKeyPermission(RsaMotDimFormCtrl.myKeyPermission);
		rsa_motDim.setPathZul("/web/ui/common/rsa_motDim/rsaMotDimGrid.zul");
		
		MenuItem operatori = new MenuItem (Labels.getLabel("menu.tabelle.operatori"));	
		operatori.setKeyPermission(OperatoriFormCtrl.myKeyPermission);
		operatori.setPathZul(OperatoriGridCtrl.myPathZul); 
		
		MenuItem ospedali = new MenuItem (Labels.getLabel("menu.tabelle.ospedali"));
		ospedali.setKeyPermission(OspedaliFormCtrl.myKeyPermission);
		ospedali.setPathZul("~./ui/common/ospedali/ospedaliGrid.zul");
		
		MenuItem pensioni = new MenuItem (Labels.getLabel("menu.tabelle.pensioni"));	
		pensioni.setKeyPermission(PensioniFormCtrl.myKeyPermission);
		pensioni.setPathZul("/web/ui/common/pensioni/pensioniGrid.zul");
		
		MenuItem pipp = new MenuItem (Labels.getLabel("menu.tabelle.pipp"));
		pipp.setKeyPermission(PippFormCtrl.myKeyPermission);
		pipp.setPathZul("~./ui/common/pipp/pippGrid.zul");	
		
		MenuItem presidi = new MenuItem (Labels.getLabel("menu.tabelle.presidi"));	
		presidi.setKeyPermission(PresidiFormCtrl.myKeyPermission);
		presidi.setPathZul("~./ui/common/presidi/presidiGrid.zul");	
		
		//MenuItem socpre = new MenuItem (Labels.getLabel("menu.tabelle.socpre"));	
		//socpre.setPathZul("/web/ui/common/socpre/socpreGrid.zul");	
		//MenuItem socpro = new MenuItem (Labels.getLabel("menu.tabelle.socpro"));	
		//socpro.setPathZul("/web/ui/common/socpro/socproGrid.zul");	
		
		MenuItem professione = new MenuItem (Labels.getLabel("menu.tabelle.professione"));
		professione.setKeyPermission(ProfessioneFormCtrl.myKeyPermission);
		professione.setPathZul("~./ui/common/professione/professioneGrid.zul");
		
		//MenuItem operqual = new MenuItem (Labels.getLabel("menu.tabelle.operqual"));	
		//operqual.setPathZul("/web/ui/common/operqual/operqualGrid.zul");
		
		MenuItem reparti = new MenuItem (Labels.getLabel("menu.tabelle.reparti"));	
		reparti.setKeyPermission(RepartiFormCtrl.myKeyPermission);
		reparti.setPathZul("~./ui/common/reparti/repartiGrid.zul");
		
		//MenuItem responsabile_gom = new MenuItem (Labels.getLabel("menu.tabelle.responsabile_gom"));	
		//responsabile_gom.setPathZul("/web/ui/common/responsabile_gom/responsabileGOMGrid.zul");
		//MenuItem responsabile_progr = new MenuItem (Labels.getLabel("menu.tabelle.responsabile_progr"));	
		//responsabile_progr.setPathZul("/web/ui/common/responsabile_progr/responsabilePROGGrid.zul");
		//MenuItem ruoli_operatore = new MenuItem (Labels.getLabel("menu.tabelle.ruoli_operatore"));	
		//ruoli_operatore.setPathZul("/web/ui/common/ruoli_operatore/ruoliOperatoreGrid.zul");
		
		MenuItem specialita = new MenuItem (Labels.getLabel("menu.tabelle.specialita"));
		specialita.setKeyPermission(SpecialitaFormCtrl.myKeyPermission);
		specialita.setPathZul("/web/ui/common/specialita/specialitaGrid.zul");
		
		MenuItem statoProf = new MenuItem (Labels.getLabel("menu.tabelle.statoProf"));	
		statoProf.setKeyPermission(StatoProfFormCtrl.myKeyPermission);
		statoProf.setPathZul("/web/ui/common/statoProf/statoProfGrid.zul");
		
		//MenuItem sussidi_filtrati = new MenuItem (Labels.getLabel("menu.tabelle.sussidi_filtrati"));	
		//sussidi_filtrati.setPathZul("/web/ui/common/sussidi_filtrati/sussidiFiltratiGrid.zul");
		
		MenuItem tipoMedici = new MenuItem (Labels.getLabel("menu.tabelle.tipoMedici"));
		tipoMedici.setKeyPermission(TipoMediciFormCtrl.myKeyPermission);
		tipoMedici.setPathZul("~./ui/common/tipoMedici/tipoMediciGrid.zul");
		
		//MenuItem tipoTariffa = new MenuItem (Labels.getLabel("menu.tabelle.tipoTariffa"));	
		//tipoTariffa.setPathZul("/web/ui/common/tipoTariffa/tipoTariffaGrid.zul");
		//MenuItem soctab = new MenuItem (Labels.getLabel("menu.tabelle.soctab"));	
		//soctab.setPathZul("/web/ui/common/soctab/soctabGrid.zul");
		
		MenuItem valutatori = new MenuItem (Labels.getLabel("menu.tabelle.valutatori"));
		valutatori.setKeyPermission(ValutatoriFormCtrl.myKeyPermission);
		valutatori.setPathZul("/web/ui/common/valutatori/valutatoriGrid.zul");
		
		MenuItem zone = new MenuItem (Labels.getLabel("menu.tabelle.zone"));
		zone.setKeyPermission(ZoneFormCtrl.myKeyPermission);
		zone.setPathZul("~./ui/common/zone/zoneGrid.zul");
		
		root = new MenuTreeNode(null,
			new MenuTreeNode[] {
				new MenuTreeNode(aree_distr),
				new MenuTreeNode(prestaz_bisogni),
				new MenuTreeNode(attivita_prest),
				new MenuTreeNode(banche),
				new MenuTreeNode(beneficiario),
				//new MenuTreeNode(cartella),
				//new MenuTreeNode(centro_costo),
				//new MenuTreeNode(cittadin),
//				new MenuTreeNode(commissioni_uvm),
				new MenuTreeNode(comuni),
				//new MenuTreeNode(conti_eco),
				//new MenuTreeNode(contributi),
				new MenuTreeNode(distretti),
				//new MenuTreeNode(esenzioni),
				new MenuTreeNode(gestione_prestazioni,new MenuTreeNode[] {
						new MenuTreeNode(prestazioni),
						new MenuTreeNode(tipo_prestazioni)
					},false),
				new MenuTreeNode(grado_parent),
				//new MenuTreeNode(istituti),
				new MenuTreeNode(medici),
				new MenuTreeNode(rsa_motDim),
				new MenuTreeNode(operatori),
				new MenuTreeNode(ospedali),
				new MenuTreeNode(pensioni),
				new MenuTreeNode(pipp),
				new MenuTreeNode(presidi),
				//new MenuTreeNode(socpre),
				//new MenuTreeNode(socpro),
				new MenuTreeNode(professione),	
				//new MenuTreeNode(operqual),
				new MenuTreeNode(reparti),
				//new MenuTreeNode(responsabile_gom),
				//new MenuTreeNode(responsabile_progr),
				//new MenuTreeNode(ruoli_operatore),
				new MenuTreeNode(specialita),
				new MenuTreeNode(statoProf),
				//new MenuTreeNode(sussidi_filtrati),
				//new MenuTreeNode(tipoTariffa),
				new MenuTreeNode(tipoMedici),
				//new MenuTreeNode(soctab),
				new MenuTreeNode(valutatori),
//				new MenuTreeNode(zone),
		});
		
		
	}
	public MenuTreeNode getRoot() {
		return root;
	}
}

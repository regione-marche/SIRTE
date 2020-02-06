/**
 * Servlet di implementazione per le stampe
 */

import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaADLIndexFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaBarthelFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaBarthelIndexModFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaBisogniFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaBradenFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaCirsFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaConleyFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaDisabilitaComFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaDoloreAdFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaFimFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaIADLFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaKarnofskyFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaRUGFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaSPMSQFormCtrl;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ScalaWoundFormCtrl;

import it.pisa.caribel.gprs2.GprsElement;
import it.pisa.caribel.gprs2.ReportRouterServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import org.zkoss.util.resource.Labels;

public class SINSSNTFoServlet extends ReportRouterServlet {

	private static String pkg = "it.caribel.app.sinssnt.bean.";
	private static String pkg_mod = "it.caribel.app.sinssnt.bean.modificati.";
	private static String pkg_nuovi = "it.caribel.app.sinssnt.bean.nuovi.";

	public void init(ServletConfig conf) throws ServletException {
		try {
			inizializza(conf, "SINSSNTFoServlet");   
		} catch (Exception ex) {
			throw new ServletException(ex.getMessage());
		}
		/*CLASSTYPEbegin
		int t = GprsElement.CLASS_TYPE;
		/*CLASSTYPEend*/
		/*EJBTYPEbegin*/
		//int t = GprsElement.EJB_TYPE;
		int t = GprsElement.CLASS_TYPE;
		/*EJBTYPEend*/

		util.export(t, "ASTERDROID", "AsterDroidEJB", "ASTERDROID", "AsterDroidHome");
		util.export(t, "SINS_FOSKSACEF", pkg+"FoSkacefEJB", "SINS_FOSKSACEF","FoSkacefHome");
		util.export(t, "SINS_FOREPORT", pkg+"FoReportEJB", "SINS_FOREPORT","FoReportHome");
		util.export(t, "SINS_FOPARG", pkg+"FoPargEJB", "SINS_FOPARG", "FoPargHome");
		util.export(t, "SINS_FOBISASS", pkg+"FoBisAssEJB", "SINS_FOBISASS","FoBisAssHome");
		util.export(t, "SINS_FODATI", pkg+"FoDatiEJB", "SINS_FODATI", "FoDatiHome");
		util.export(t, "SINS_FOOPERATORI", pkg+"FoOperatoriEJB", "SINS_FOOPERATORI", "FoOperatoriHome");
		util.export(t, "SINS_FOSTAMPE", pkg+"FoStampeEJB", "SINS_FOSTAMPE", "FoStampeHome");
		util.export(t, "SINS_FOCITTADIN", pkg+"FoCittadinEJB", "SINS_FOCITTADIN", "FoCittadinHome");
		util.export(t, "SINS_FOAREADIS", pkg+"FoAreaDisEJB", "SINS_FOAREADIS","FoAreaDisHome");
		util.export(t, "SINS_FOVALAUTO", pkg+"FoValAutoEJB", "SINS_FOVALAUTO", "FoValAutoHome");
		util.export(t, "SINS_FODISTRETTI", pkg+"FoDistrettiEJB", "SINS_FODISTRETTI", "FoDistrettiHome");
		util.export(t, "SINS_FOPROFADI", pkg+"FoProfAdiEJB", "SINS_FOPROFADI", "FoProfAdiHome");
		util.export(t, "SINS_FOTIPOLOGIE", pkg+"FoTipologieEJB", "SINS_FOTIPOLOGIE", "FoTipologieHome");
		util.export(t, "SINS_FOELERSA", pkg+"FoEleRsaEJB", "SINS_FOELERSA", "FoEleRsaHome");
		util.export(t, "SINS_FOSUBAREA", pkg+"FoConsSubareaEJB", "SINS_FOSUBAREA", "FoConsSubareaHome");
		util.export(t, "SINS_FORSAATTESA", pkg+"FoRsaAttesaEJB", "SINS_FORSAATTESA", "FoRsaAttesaHome");
		util.export(t, "SINS_FORSACOM", pkg+"FoRsaComEJB", "SINS_FORSACOM", "FoRsaComHome");
		util.export(t, "SINS_FOOREOSA", pkg+"FoOreOsaEJB", "SINS_FOOREOSA", "FoOreOsaHome");
		util.export(t, "SINS_FOVASCCER", pkg_mod+"FoVascCerEJB", "SINS_FOVASCCER", "FoVascCerHome");
		util.export(t, "SINS_FOINFELENCO", pkg+"FoInfElencoEJB", "SINS_FOINFELENCO", "FoInfElencoHome");
		util.export(t, "SINS_FOIPREST", pkg_mod+"FoIPrestEJB", "SINS_FOIPREST", "FoIPrestHome");
		util.export(t, "SINS_FOINTERVOPE", pkg+"FoIntervOpeEJB", "SINS_FOINTERVOPE", "FoIntervOpeHome");
		util.export(t, "SINS_FOIASSELE", pkg_mod+"FoIAssEleEJB", "SINS_FOIASSELE", "FoIAssEleHome");
		util.export(t, "SINS_FOIASSELE2", pkg_mod+"FoIAssEle_2EJB", "SINS_FOIASSELE2", "FoIAssEle_2Home");
		util.export(t, "SINS_FOFASSELE", pkg_mod+"FoFAssEleEJB", "SINS_FOFASSELE", "FoFAssEleHome");
		util.export(t, "SINS_FOTRASPORTI", pkg+"FoTrasportiEJB", "SINS_FOTRASPORTI", "FoTrasportiHome");
		util.export(t, "SINS_FOMEDICI", pkg+"FoMediciEJB", "SINS_FOMEDICI", "FoMediciHome");
		util.export(t, "SINS_FOCONECON", pkg+"FoConEconEJB", "SINS_FOCONECON", "FoConEconHome");
		util.export(t, "SINS_FONOMENC", pkg+"FoNomencEJB", "SINS_FONOMENC", "FoNomencHome");
		util.export(t, "SINS_FOPRESIDI", pkg+"FoPresidiEJB", "SINS_FOPRESIDI", "FoPresidiHome");
		util.export(t, "SINS_FOBANCHE", pkg+"FoBancheEJB", "SINS_FOBANCHE", "FoBancheHome");
		util.export(t, "SINS_FOPENSIONI", pkg+"FoPensioniEJB", "SINS_FOPENSIONI", "FoPensioniHome");
		util.export(t, "SINS_FORSACONTR", pkg+"FoRsaContrEJB", "SINS_FORSACONTR", "FoRsaContrHome");
		util.export(t, "SINS_FORSACONTI", pkg+"FoRsaContiEJB", "SINS_FORSACONTI", "FoRsaContiHome");
		util.export(t, "SINS_FORSACONTRPROP", pkg+"FoRsaContrPropEJB", "SINS_FORSACONTRPROP", "FoRsaContrPropHome");
		util.export(t, "SINS_FOPRESTAZ", pkg+"FoPrestazEJB", "SINS_FOPRESTAZ", "FoPrestazHome");
		util.export(t, "SINS_FOBRANCA", pkg+"FoBrancaEJB", "SINS_FOBRANCA", "FoBrancaHome");
		util.export(t, "SINS_FOSUSSIDI", pkg+"FoSussidiEJB", "SINS_FOSUSSIDI", "FoSussidiHome");
		util.export(t, "SINS_FOMASSELE", pkg_mod+"FoMAssEleEJB", "SINS_FOMASSELE", "FoMAssEleHome");
		util.export(t, "SINS_FOMSOCELE", pkg+"FoMSocEleEJB", "SINS_FOMSOCELE", "FoMSocEleHome");
		util.export(t, "SINS_FOELESOC", pkg+"FoEleSocEJB", "SINS_FOELESOC", "FoEleSocHome");
		util.export(t, "SINS_FOSOCPREST", pkg+"FoSocPrestEJB", "SINS_FOSOCPREST", "FoSocPrestHome");
		util.export(t, "SINS_FOCOPPIE", pkg+"FoCoppieEJB", "SINS_FOCOPPIE", "FoCoppieHome");
		util.export(t, "SINS_FOINTERV", pkg+"FoIntervEJB", "SINS_FOINTERV", "FoIntervHome");
		util.export(t, "SINS_FOCONTINTERV", pkg+"FoContIntervEJB", "SINS_FOCONTINTERV", "FoContIntervHome");
		util.export(t, "SINS_FORICRSA", pkg+"FoRicRSAEJB", "SINS_FORICRSA", "FoRicRSAHome");
		util.export(t, "SINS_FOMALVIO", pkg+"FoMalvioEJB", "SINS_FOMALVIO", "FoMalvioHome");
		util.export(t, "SINS_FOCONTGOIF", pkg+"FoContGoifEJB", "SINS_FOCONTGOIF", "FoContGoifHome");
		util.export(t, "SINS_FOOREASS", pkg_mod+"FoOreAssEJB", "SINS_FOOREASS", "FoOREASSHome");
		util.export(t, "SINS_FODETTOREPREST", pkg_mod+"FoDettOrePrestEJB", "SINS_FODETTOREPREST", "FoDettOrePrestHome");
		util.export(t, "SINS_FOTIPOUTEDIMIS", pkg+"FoTipoUteDimisEJB", "SINS_FOTIPOUTEDIMIS", "FoTipoUteDimisHome");
		util.export(t, "SINS_FOSINGOLAPROF", pkg+"FoSingolaProfEJB", "SINS_FOSINGOLAPROF", "FoSingolaProfHome");
		util.export(t, "SINS_FORIEPBRANCA", pkg_mod+"FoRiepBrancaEJB", "SINS_FORIEPBRANCA", "FoRiepBrancaHome");
		util.export(t, "SINS_FOPRESTQUALOP", pkg+"FoPrestQualOpEJB", "SINS_FOPRESTQUALOP", "FoPrestQualOpHome");
		util.export(t, "SINS_FORIEPBRANCAXRIGA", pkg_mod+"FoRiepBrancaXRigaEJB", "SINS_FORIEPBRANCAXRIGA",
				"FoRiepBrancaXRigaHome");
		util.export(t, "SINS_FORIEPPRESOP", pkg+"FoRiepPrestOpEJB", "SINS_FORIEPPRESOP", "FoRiepPrestOpHome");
		util.export(t, "SINS_FORIEPBRANCAOP", pkg+"FoRiepBrancaOpEJB", "SINS_FORIEPBRANCAOP", "FoRiepBrancaOpHome");
		util.export(t, "SINS_FORIEPUTEN", pkg+"FoRiepUtenEJB", "SINS_FORIEPUTEN", "FoRiepUtenHome");
		util.export(t, "SINS_FORIEPUTENETASES", pkg+"FoRiepUtenEtaSesEJB", "SINS_FORIEPUTENETASES", "FoRiepUtenEtaSesHome");
		util.export(t, "SINS_FOASSMENSILE", pkg+"FoAssMensileEJB", "SINS_FOASSMENSILE", "FoAssMensileHome");
		util.export(t, "SINS_FOASSHANDI", pkg+"FoAssHandiEJB", "SINS_FOASSHANDI", "FoAssHandiHome");
		util.export(t, "SINS_FOFREQPAT", pkg+"FoFreqPatEJB", "SINS_FOFREQPAT", "FoFreqPatHome");
		util.export(t, "SINS_FOPAZCOMUNI", pkg+"FoPazComuniEJB", "SINS_FOPAZCOMUNI", "FoPazComuniHome");
		util.export(t, "SINS_FOPRESTFREQ", pkg+"FoPrestFreqEJB", "SINS_FOPRESTFREQ", "FoPrestFreqHome");
		util.export(t, "SINS_FOCOSTIPAT", pkg+"FoCostiPatEJB", "SINS_FOCOSTIPAT", "FoCostiPatHome");
		util.export(t, "SINS_FOFCONS", pkg+"FoFConsEJB", "SINS_FOFCONS", "FoFConsHome");
		util.export(t, "SINS_FOPAZACQUISITI", pkg+"FoPazAcquisitiEJB", "SINS_FOPAZACQUISITI", "FoPazAcquisitiHome");
		util.export(t, "SINS_FOPAZMENSDOM", pkg+"FoPazMensDomEJB", "SINS_FOPAZMENSDOM", "FoPazMensDomHome");
		util.export(t, "SINS_FOTABPIPP", pkg+"FoTabpippEJB", "SINS_FOTABPIPP", "FoTabpippHome");
		util.export(t, "SINS_FOCARTELLA", pkg+"FoCartellaEJB", "SINS_FOCARTELLA", "FoCartellaHome");
		util.export(t, "SINS_FOBENEFIC", pkg+"FoBeneficEJB", "SINS_FOBENEFIC", "FoBeneficHome");
		util.export(t, "SINS_FOACCHAND", pkg+"FoAcchandEJB", "SINS_FOACCHAND", "FoAcchandHome");
		util.export(t, "SINS_FOATTMED", pkg+"FoAttMedEJB", "SINS_FOATTMED", "FoAttMedHome");
		util.export(t, "SINS_FOCONTRIB", pkg+"FoContribEJB", "SINS_FOCONTRIB", "FoContribHome");
		util.export(t, "SINS_FOPROPSI", pkg+"FoPropsiEJB", "SINS_FOPROPSI", "FoPropsiHome");
		util.export(t, "SINS_FOTABADL", pkg+"FoTabAdlEJB", "SINS_FOTABADL", "FoTabAdlHome");
		util.export(t, "SINS_FOTABIADL", pkg+"FoTabIAdlEJB", "SINS_FOTABIADL", "FoTabIAdlHome");
		util.export(t, "SINS_FOPFEIFFER", pkg+"FoPfeifferEJB", "SINS_FOPFEIFFER", "FoPfeifferHome");
		util.export(t, "SINS_FOSKINFE", pkg+"FoSkInfeEJB", "SINS_FOSKINFE", "FoSkInfeHome");
		util.export(t, "SINS_FOSKMEDI", pkg+"FoSkMediEJB", "SINS_FOSKMEDI", "FoSkMediHome");
		util.export(t, "SINS_FOSKFISIO", pkg+"FoSkFisioEJB", "SINS_FOSKFISIO", "FoSkFisioHome");
		util.export(t, "SINS_FOSKSOCIALE", pkg+"FoSkSocialeEJB", "SINS_FOSKSOCIALE", "FoSkSocialeHome");
		util.export(t, "SINS_FOELEASSMED", pkg_mod+"FoEleAssMedEJB", "SINS_FOELEASSMED", "FoEleAssMedHome");
		util.export(t, "SINS_FOASSDOMED", pkg+"FoEleAssDomEdEJB", "SINS_FOASSDOMED", "FoAssDomEdHome");
		util.export(t, "SINS_FOASSDOMDIR", pkg+"FoEleAssDomDirEJB", "SINS_FOASSDOMDIR", "FoAssDomDirHome");
		util.export(t, "SINS_FOASSMINORI", pkg+"FoEleAssMinoriEJB", "SINS_FOASSMINORI", "FoAssMinoriHome");
		util.export(t, "SINS_FOASSISTMIN", pkg+"FoAssMinoriEJB", "SINS_FOASSISTMIN", "FoAssMinoriHome");
		util.export(t, "SINS_FOASSELENCOMINORI", pkg+"FoAssElencoMinoriEJB", "SINS_FOASSELENCOMINORI",
				"FoAssElencoMinoriHome");
		util.export(t, "SINS_FOPIPP", pkg+"FoPippEJB", "SINS_FOPIPP", "FoPippHome");
		util.export(t, "SINS_FOSTATPREST", pkg_mod+"FoStatPrestEJB", "SINS_FOSTATPREST", "FoStatPrestHome");
		util.export(t, "SINS_FOPRESTDOM", pkg_mod+"FoPrestDomEJB", "SINS_FOPRESTDOM", "FoPrestDomHome");
		util.export(t, "SINS_FOPRESTAMB", pkg_mod+"FoPrestDomEJB", "SINS_FOPRESTAMB","FoPrestDomHome");
		util.export(t, "SINS_FOCOSTOPREST", pkg+"FoCostoPrestEJB", "SINS_FOCOSTOPREST", "FoCostoPrestHome");
		util.export(t, "SINS_FOMEDIOPREST", pkg+"FoMedioPrestEJB", "SINS_FOMEDIOPREST", "FoMedioPrestHome");
		util.export(t, "SINS_FOSTATDOMICILIARE", pkg_mod+"FoStatDomiciliareEJB", "SINS_FOSTATDOMICILIARE",
				"FoStatDomiciliareHome");
		util.export(t, "SINS_FOFISIO", pkg+"FoFisioEJB", "SINS_FOFISIO", "FoFisioHome");
		util.export(t, "SINS_FOUNITA", pkg+"FoUnitaEJB", "SINS_FOUNITA", "FoUnitaHome");
		util.export(t, "SINS_FOCONTSOC", pkg+"FoContsocEJB", "SINS_FOCONTSOC", "FoContsocHome");
		util.export(t, "SINS_FOCONTTIPO", pkg+"FoContTipoEJB", "SINS_FOCONTTIPO", "FoContTipoHome");
		util.export(t, "SINS_FOMODULI", pkg+"FoModuliSkmmgEJB", "SINS_FOMODULI", "FoModuliSkmmgHome");
		util.export(t, "SINS_FOLETTERE", pkg+"FoLettereAutoEJB", "SINS_FOLETTERE", "FoLettereAuto");
		util.export(t, "SINS_FOSKSVAMA", pkg+"FoSkSvamaEJB", "SINS_FOSKSVAMA", "FoSkSvamaHome");
		util.export(t, "SINS_FORIEPIPP", pkg+"FoRiepPippEJB", "SINS_FORIEPIPP", "FoRiepPippHome");
		util.export(t, "SINS_FOCONTOPER", pkg+"FoContOperEJB", "SINS_FOCONTOPER", "FoContOperHome");
		util.export(t, "SINS_FORIEPDETTPIPP", pkg+"FoRiepDettPippEJB", "SINS_FORIEPDETTPIPP", "FoRiepDettPippHome");
		util.export(t, "SINS_FOSKINF", pkg+"FoSkInfEJB", "SINS_FOSKINF", "FoSkInfHome");
		util.export(t, "SINS_FOSKIPROGASS", pkg+"FoSkiProgAssEJB", "SINS_FOSKIPROGASS","FoSkiProgAssHome");
		util.export(t, "SINS_FOASSPROGASS", pkg+"FoAssProgAssEJB", "SINS_FOASSPROGASS", "FoAssProgAssHome");
		util.export(t, "SINS_FOMEDPROGASS", pkg+"FoMedProgAssEJB", "SINS_FOMEDPROGASS", "FoMedProgAssHome");
		util.export(t, "SINS_FOFISPROGASS", pkg+"FoFisProgAssEJB", "SINS_FOFISPROGASS", "FoFisProgAssHome");
		util.export(t, "SINS_FOPIANOASSIST", pkg+"FoPianoAssistEJB", "SINS_FOPIANOASSIST", "FoPianoAssistHome");
		// AGENDA
		util.export(t, "SINS_FOAGENDAGIORN", pkg+"FoAgendaGiornEJB", "SINS_FOAGENDAGIORN", "FoAgendaGiornHome");

		util.export(t, "SINS_FSCARICHI", pkg+"FScarichiEJB", "SINS_FSCARICHI", "FScarichiHome");
		util.export(t, "SINS_FPATO", pkg+"FPatologieEJB", "SINS_FPATO", "FPatologieHome");
		util.export(t, "SINS_FCARICHI", pkg+"FCarichiEJB", "SINS_FCARICHI", "FCarichiHome");
		util.export(t, "SINS_FPRODO", pkg+"FProdottiEJB", "SINS_FPRODO", "FProdottiHome");
		util.export(t, "SINS_FOGRSPESA", pkg+"FoGrSpesaEJB", "SINS_FOGRSPESA", "FoGrSpesaHome");
		util.export(t, "SINS_FOCONTAPERTI", pkg+"FoContApertiEJB", "SINS_FOCONTAPERTI", "FoContApertiHome");
		util.export(t, "SINS_FOADIMESE", pkg+"FoAdiMeseEJB", "SINS_FOADIMESE", "FoAdiMeseHome");
		util.export(t, "SINS_FO_SIDADI", pkg+"FoSidAdiEJB", "SINS_FO_SIDADI", "FoSidAdiHome");
		util.export(t, "SINS_FOSIDADIUTEACC", pkg+"FoSidAdiUteAccEJB", "SINS_FOSIDADIUTEACC", "FoSidAdiUteAccHome");
		util.export(t, "SINS_FOSIDADIUTEMMG", pkg+"FoSidAdiUteMMGEJB", "SINS_FOSIDADIUTEMMG", "FoSidAdiUteMMGHome");
		util.export(t, "SINS_FOSIDADIDIMESSI", pkg+"FoSidAdiDimessiEJB", "SINS_FOSIDADIDIMESSI", "FoSidAdiDimessiHome");
		util.export(t, "SINS_FOSIDADIUTEPROFANNO", pkg+"FoSidAdiUteProfAnnoEJB", "SINS_FOSIDADIUTEPROFANNO",
				"FoSidAdiUteProfAnnoHome");
		util.export(t, "SINS_FOSIDADIMED", pkg+"FoSidAdiMedEJB", "SINS_FOSIDADIMED", "FoSidAdiMedHome");
		util.export(t, "SINS_FOSITPERADI", pkg+"FoSitPerADIEJB", "SINS_FOSITPERADI", "FoSitPerADIHome");

		util.export(t, "SINS_FOMCPELEASS", pkg+"FoMCPEleAssEJB", "SINS_FOMCPELEASS", "FoMCPEleAssHome");

		util.export(t, "SINS_FOINFELEASS", pkg_mod+"FoInfEleAssEJB", "SINS_FOINFELEASS", "FoInfEleAssHome");
		util.export(t, "SINS_FOINFELEMOT", pkg+"FoInfEleMotEJB", "SINS_FOINFELEMOT", "FoInfEleMotHome");
		util.export(t, "SINS_FOINFELEINV", pkg+"FoInfEleInvEJB", "SINS_FOINFELEINV", "FoInfEleInvHome");
		util.export(t, "SINS_FOPRESTDINAM", pkg+"FoPrestDinamEJB", "SINS_FOPRESTDINAM", "FoPrestDinamHome");
		util.export(t, "SINS_FOVSPERSONALE", pkg+"FoVSPersonaleEJB", "SINS_FOVSPERSONALE", "FoVSPersonaleHome");
		util.export(t, "SINS_FOVSVERBALE", pkg+"FoVSVerbaleEJB", "SINS_FOVSVERBALE", "FoVSVerbaleHome");
		util.export(t, "SINS_FOVSSTRUTTURA", pkg+"FoVSStrutturaEJB", "SINS_FOVSSTRUTTURA", "FoVSStrutturaHome");
		util.export(t, "SINS_FOVSLETTERA", pkg+"FoVSLetteraEJB", "SINS_FOVSLETTERA", "FoVSLetteraHome");
		util.export(t, "SINS_FOCONTCOMVERB", pkg+"FoContComVerbEJB", "SINS_FOCONTCOMVERB", "FoContComVerbHome");
		util.export(t, "SINS_FOLISTAMENS", pkg+"FoListaMensEJB", "SINS_FOLISTAMENS", "FoListaMensHome");
		util.export(t, "SINS_FORIESAME", pkg+"FoRiesameEJB", "SINS_FORIESAME", "FoRiesameHome");
		util.export(t, "SINS_FORIEPASS", pkg+"FoRiepAssEJB", "SINS_FORIEPASS", "FoRiepAssHome");
		util.export(t, "SINS_FOSESTRUTTURE", pkg+"FoSEStruttureEJB", "SINS_FOSESTRUTTURE", "FoSEStruttureHome");
		util.export(t, "SINS_FOSEOPERATORI", pkg+"FoSEOperatoriEJB", "SINS_FOSEOPERATORI", "FoSEOperatoriHome");
		util.export(t, "SINS_FOSEPRESCOSTI", pkg+"FoSEPrescostiEJB", "SINS_FOSEPRESCOSTI", "FoSEPrescostiHome");
		util.export(t, "SINS_FOPRESTASS", pkg+"FoPrestAssEJB", "SINS_FOPRESTASS", "FoPrestAssHome");
		util.export(t, "SINS_FOASSNOPATO", pkg+"FoAssNoPatoEJB", "SINS_FOASSNOPATO", "FoAssNoPatoHome");
		util.export(t, "SINS_FO_RIC_FAM", pkg+"FoRicoveriFamEJB", "SINS_FO_RIC_FAM", "FoRicoveriFamHome");
		//		util.export(t, "SINS_FOTESTBARTHEL", pkg+"FoTestBarthelEJB", "SINS_FOTESTBARTHEL", "FoTestBarthelHome");
		util.export(t, ScalaBarthelFormCtrl.CST_NOME, pkg + ScalaBarthelFormCtrl.CST_NOME_EJB,
				ScalaBarthelFormCtrl.CST_NOME, ScalaBarthelFormCtrl.CST_NOME_HOME);
		util.export(t, "SINS_FOSCALACAREG", pkg+"FoScalaCaregiverEJB", "SINS_FOSCALACAREG", "FoScalaCaregiverHome");
		//		util.export(t, "SINS_FOSCALABRADEN", pkg+"FoScalaBradenEJB", "SINS_FOSCALABRADEN","FoScalaBradenHome");
		util.export(t, ScalaBradenFormCtrl.CST_NOME, pkg + ScalaBradenFormCtrl.CST_NOME_EJB,
				ScalaBradenFormCtrl.CST_NOME, ScalaBradenFormCtrl.CST_NOME_HOME);
		util.export(t, "SINS_FOSCALATIQ", pkg+"FoScalaTIQEJB", "SINS_FOSCALATIQ", "FoScalaTIQHome");
		//Jessica 26/07/2011
		util.export(t, "SINS_FOSCLVALUTAZ", pkg+"FoScalaValutazEJB", "SINS_FOSCLVALUTAZ","FoSclValutazBHome");
		//		util.export(t, "SINS_FOSCLVALBIS", pkg_nuovi+"FoScalaValBisogniEJB", "SINS_FOSCLVALBIS","FoSclValBisHome");
		util.export(t, ScalaBisogniFormCtrl.CST_NOME, pkg_nuovi + ScalaBisogniFormCtrl.CST_NOME_EJB,
				ScalaBisogniFormCtrl.CST_NOME, ScalaBisogniFormCtrl.CST_NOME_HOME );

		//		boffa 23/02/2012
		util.export(t,"SINS_FODIFRSARICOVI","FoDifRsaRicoveriEJB","SINS_FODIFRSARICOVI","FoDifRsaRicoveriHome");

		util.export(t, "SINS_FOSSOCINT", pkg+"FoSSocIntEJB", "SINS_FOSSOCINT", "FoSSocIntHome");
		util.export(t, "SINS_FOCONTSSOCINT", pkg+"FoContSSocIntEJB", "SINS_FOCONTSSOCINT", "FoContSSocIntHome");
		util.export(t, "SINS_FOSSOCPREST", pkg+"FoSSocPrestEJB", "SINS_FOSSOCPREST", "FoSSocPrestHome");
		util.export(t, "SINS_FOSSOCRIEPINT", pkg+"FoSSocRiepIntEJB", "SINS_FOSSOCRIEPINT", "FoSSocRiepIntHome");
		util.export(t, "SINS_FOSSOCRIPREST", pkg+"FoSSocRiPrestEJB", "SINS_FOSSOCRIPREST", "FoSSocRiPrestHome");
		util.export(t, "SINS_FOCARTCHIUSE", pkg+"FoCartChiuseEJB", "SINS_FOCARTCHIUSE", "FoCartChiuseHome");
		util.export(t, "SINS_FOPROGSINS", pkg+"FoProgettoSinsEJB", "SINS_FOPROGSINS", "FoProgettoSinsHome");
		util.export(t, "SINS_FORIEPOSPDIM", pkg+"FoRiepOspDimEJB", "SINS_FORIEPOSPDIM", "FoRiepOspDimHome");
		util.export(t, "SINS_FOELESKUVT", pkg+"FoEleSkuvtEJB", "SINS_FOELESKUVT", "FoEleSkuvt");

		util.export(t, "SINS_FOGESTASSEGNO", pkg+"FoGestAssegnoEJB", "SINS_FOGESTASSEGNO", "FoGestAssegno");
		util.export(t, "SINS_GESTASSEGNOGRAD", pkg+"GestAssegnoGradEJB", "SINS_GESTASSEGNOGRAD", "GestAssegnoGrad");
		util.export(t, "SINS_FOGESTASSEGNOEROG", pkg+"FoGestAssegnoErogEJB", "SINS_FOGESTASSEGNOEROG", "FoGestAssegnoErog");
		util.export(t, "SINS_FOELENCODOM", pkg+"FoElencoDomEJB", "SINS_FOELENCODOM", "FoElencoDom");
		util.export(t, "SINS_FOASSGRADUATORIA", pkg+"FoAssGraduatoriaEJB", "SINS_FOASSGRADUATORIA","FoAssegnoGraduatoria");
		util.export(t, "SINS_FOASSCAMPIPIENI", pkg+"FoAssCampiPieniEJB", "SINS_FOASSCAMPIPIENI", "FoAssCampiPieni");
		util.export(t, "SINS_FOSERVIZIOADI", pkg+"FoServizioAdiEJB", "SINS_FOSERVIZIOADI", "FoServizioAdi");
		util.export(t, "SINS_REGACC", pkg+"RegAccessiEJB", "SINS_REGACC", "RegAccessiHome");
		util.export(t, "SINS_FOUTEPATO", pkg+"FoUtePatoEJB", "SINS_FOUTEPATO", "FoUtePatoHome");
		util.export(t, "SINS_FOSCMINSCA", pkg+"FoScMinScaEJB", "SINS_FOSCMINSCA", "FoScMinScaHome");
		util.export(t, "SINS_FOUTEPROGE", pkg+"FoUteProgeEJB", "SINS_FOUTEPROGE", "FoUteProgeHome");
		util.export(t, "SINS_FOTESTGDS", pkg+"FoTestGdsEJB", "SINS_FOTESTGDS", "FoTestGdsHome");
		util.export(t, "SINS_FOTESTCBI", pkg+"FoTestCbiEJB", "SINS_FOTESTCBI", "FoTestCbiHome");
		util.export(t, "SINS_FOTESTADL", pkg+"FoTestAdlEJB", "SINS_FOTESTADL", "FoTestAdlHome");
		util.export(t, "SINS_FOTESTPFEIFFER", pkg+"FoTestPfeifferEJB", "SINS_FOTESTPFEIFFER", "FoTestPfeifferHome");
		//		util.export(t, "SINS_FOTESTIADL", pkg+"FoTestIadlEJB", "SINS_FOTESTIADL", "FoTestIadlHome");
		util.export(t, ScalaIADLFormCtrl.CST_NOME, pkg + ScalaIADLFormCtrl.CST_NOME_EJB, ScalaIADLFormCtrl.CST_NOME,
				ScalaIADLFormCtrl.CST_NOME_HOME);
		util.export(t, "SINS_FOTESTSCPIADL", pkg+"FoTestScpIadlEJB", "SINS_FOTESTSCPIADL", "FoTestScpIadlHome"); //bysp 12/07/10
		util.export(t, "SINS_FOTESTSCPADL", pkg+"FoTestScpAdlEJB", "SINS_FOTESTSCPADL", "FoTestScpAdlHome"); //bysp 12/07/10
		util.export(t, "SINS_FOTESTSCPIADL", pkg+"FoTestScpIadlEJB", "SINS_FOTESTSCPIADL", "FoTestScpIadlHome"); //bysp 12/07/10
		//		util.export(t, "SINS_FOTESTSCPSPMSQ", pkg+"FoTestScpSpmsqEJB", "SINS_FOTESTSCPSPMSQ", "FoTestScpSpmsqHome"); //bysp 12/07/10
		util.export(t, ScalaSPMSQFormCtrl.CST_NOME, pkg + ScalaSPMSQFormCtrl.CST_NOME_EJB, ScalaSPMSQFormCtrl.CST_NOME,
				ScalaSPMSQFormCtrl.CST_NOME_HOME); //bysp 12/07/10
		util.export(t, "SINS_FOTESTSCPDISCO", pkg+"FoTestScpDiscoEJB", "SINS_FOTESTSCPDISCO", "FoTestScpDiscoHome"); //bysp 12/07/10
		util.export(t, "SINS_FOTESTSCPDMI", pkg+"FoTestScpDmiEJB", "SINS_FOTESTSCPDMI", "FoTestScpDmiHome"); //bysp 12/07/10
		util.export(t, "SINS_FOTESTMMSE", pkg+"FoTestMmseEJB", "SINS_FOTESTMMSE", "FoTestMmseHome");
		//		util.export(t, "SINS_FOTESTBADL", pkg+"FoTestBadlEJB", "SINS_FOTESTBADL", "FoTestBadlHome");
		util.export(t, ScalaADLIndexFormCtrl.CST_NOME, pkg + ScalaADLIndexFormCtrl.CST_NOME_EJB,
				ScalaADLIndexFormCtrl.CST_NOME, ScalaADLIndexFormCtrl.CST_NOME_HOME);
		util.export(t, "SINS_FOTESTCIRS", pkg+"FoTestCirsEJB", "SINS_FOTESTCIRS", "FoTestCirsHome");
		util.export(t, "SINS_FOTESTNPI", pkg+"FoTestNpiEJB", "SINS_FOTESTNPI", "FoTestNpiHome");
		util.export(t, "SINS_FOTESTTINETTI", pkg+"FoTestTinettiEJB", "SINS_FOTESTTINETTI", "FoTestTinettiHome");
		util.export(t, "SINS_FOTESTRESPON", pkg+"FoTestResponEJB", "SINS_FOTESTRESPON", "FoTestResponHome");
		util.export(t, "SINS_FOTESTPROT", pkg+"FoTestProtEJB", "SINS_FOTESTPROT", "FoTestProtHome");
		util.export(t, "SINS_FOTESTICA", pkg+"FoTestIcaEJB", "SINS_FOTESTICA", "FoTestIcaHome");
		util.export(t, "SINS_FOADMMG", pkg+"FoEleAdMmgEJB", "SINS_FOADMMG", "FoEleAdMmgHome");
		util.export(t, "SINS_FOADDISTR", pkg+"FoEleAdDistrEJB", "SINS_FOADDISTR", "FoEleAdDistrHome");
		util.export(t, "SINS_FOINTENSADI", pkg+"FoIntensitaAdiEJB", "SINS_FOINTENSADI", "FoIntensitaAdiHome");
		util.export(t, "SINS_FORUTESCALE", pkg+"FoRiepUtenScaleEJB", "SINS_FORUTESCALE", "FoRiepUtenScaleHome");
		util.export(t, "SINS_FORADSCAL", pkg+"FoRadScalaEJB", "SINS_FORADSCAL", "FoRadScalaHome");
		util.export(t, "SINS_FOTESTMDSC", pkg+"FoTestMdsCompEJB", "SINS_FOTESTMDSC", "FoTestMdsCompHome");
		util.export(t, "SINS_FOTESTMDSU", pkg+"FoTestMdsUmoreEJB", "SINS_FOTESTMDSU", "FoTestMdsUmoreHome");
		util.export(t, "SINS_FOTESTSCHEDAINFERM", pkg+"FoTestSchedaInfermEJB", "SINS_FOTESTSCHEDAINFERM",
				"FoTestSchedaInfermHome");
		util.export(t, "SINS_FOTESTSCHEDACLINICA", pkg+"FoTestSchedaClinicaEJB", "SINS_FOTESTSCHEDACLINICA",
				"FoTestSchedaClinicaHome");
		util.export(t, "SINS_FOPROFORG", pkg+"FoProfOrgEJB", "SINS_FOPROFORG", "FoProfOrgHome");
		util.export(t, "SINS_FOTESTMINIST", pkg+"FoTestMinisteroEJB", "SINS_FOTESTMINIST", "FoTestMinisteroHome");
		//		util.export(t, "SINS_FOTESTSCPCIRS", pkg+"FoTestScpCirsEJB", "SINS_FOTESTSCPCIRS", "FoTestScpCirsHome");
		util.export(t, ScalaCirsFormCtrl.CST_NOME, pkg + ScalaCirsFormCtrl.CST_NOME_EJB, ScalaCirsFormCtrl.CST_NOME,
				ScalaCirsFormCtrl.CST_NOME_HOME);

		// dimissioni ospedaliere
		util.export(t, "SINS_FOSEGNALA", pkg+"FoSegnalaEJB", "SINS_FOSEGNALA", "FoSegnalaHome");
		util.export(t, "SINS_FOCONSULTA", pkg+"FoConsultaEJB", "SINS_FOCONSULTA", "FoConsultaHome");

		// medico oncologo
		util.export(t, "SINS_FOSKMEDPAL", pkg+"FoSkMedPalEJB", "SINS_FOSKMEDPAL", "FoSkMedPalHome");
		//		util.export(t, "SINS_FOSCALAKPS", pkg+"FoScalaKPSEJB", "SINS_FOSCALAKPS", "FoScalaKPSHome");
		util.export(t, ScalaKarnofskyFormCtrl.CST_NOME, pkg + ScalaKarnofskyFormCtrl.CST_NOME_EJB,
				ScalaKarnofskyFormCtrl.CST_NOME, ScalaKarnofskyFormCtrl.CST_NOME_HOME);
		//		util.export(t, "SINS_FOSCALAWOUND", pkg+"FoScalaWoundEJB", "SINS_FOSCALAWOUND", "FoScalaWoundHome");
		util.export(t, ScalaWoundFormCtrl.CST_NOME, pkg + ScalaWoundFormCtrl.CST_NOME_EJB, ScalaWoundFormCtrl.CST_NOME,
				ScalaWoundFormCtrl.CST_NOME_HOME);
		util.export(t, "SINS_FOSCALAPAP", pkg+"FoScalaPAPEJB", "SINS_FOSCALAPAP", "FoScalaPAPHome");
		util.export(t, "SINS_FOSCALANRS", pkg+"FoScalaNRSEJB", "SINS_FOSCALANRS", "FoScalaNRSHome");
		util.export(t, "SINS_FORIEPMOVMEDPAL", pkg+"FoRiepMovMedPalEJB", "SINS_FORIEPMOVMEDPAL", "FoRiepMovMedPalHome");
		util.export(t, "SINS_FOSTATKPS", pkg+"FoStatKPSEJB", "SINS_FOSTATKPS", "FoStatKPSHome");
		util.export(t, "SINS_FORIEPDECESSI", pkg+"FoRiepDecessiEJB", "SINS_FORIEPDECESSI", "FoRiepDecessiHome");
		util.export(t, "SINS_FOCONTAGIORNI", pkg+"FoContaGiorniEJB", "SINS_FOCONTAGIORNI", "FoContaGiorniHome");
		util.export(t, "SINS_FOCONTADIAGNOSI", pkg+"FoContaDiagnosiEJB", "SINS_FOCONTADIAGNOSI", "FoContaDiagnosiHome");

		// stampe importate dal SINS_AS
		util.export(t, "SINS_FOPROGAPERTI", pkg+"FoProgApertiEJB", "SINS_FOPROGAPERTI", "FoProgApertiHome");
		util.export(t, "SINS_FOOPERABILITATI", pkg+"FoOperAbilitatiEJB", "SINS_FOOPERABILITATI", "FoOperAbilitatiHome");
		util.export(t, "SINS_FOOBIEINTERVENTI", pkg+"FoObieInterventiEJB", "SINS_FOOBIEINTERVENTI", "FoObieInterventiHome");

		// gb 21/05/08: 3 Stampe solo per il Mugello:
		util.export(t, "SINS_FOPROGAPERTI_MUG", pkg+"FoProgApertiMugEJB", "SINS_FOPROGAPERTI_MUG", "FoProgApertiMugHome");
		util.export(t, "SINS_FOOPERABILITATI_MUG", pkg+"FoOperAbilitatiMugEJB", "SINS_FOOPERABILITATI_MUG",
				"FoOperAbilitatiMugHome");
		util.export(t, "SINS_FOOBIEINTERVENTI_MUG", pkg+"FoObieInterventiMugEJB", "SINS_FOOBIEINTERVENTI_MUG",
				"FoObieInterventiMugHome");

		util.export(t, "SINS_FOOBIEPRESTAZ", pkg+"FoObiePrestazEJB", "SINS_FOOBIEPRESTAZ", "FoObiePrestazHome");

		// stampa statistiche svama
		util.export(t, "SINS_FOSTATISTICHESVAMA", pkg+"FoStatisticheSvamaEJB", "SINS_FOSTATISTICHESVAMA",
				"FoStatisticheSvamaHome");

		// gb 21/01/08
		util.export(t, "SINS_ELENCO_SKVALPUAC", pkg+"ElencoSkValPuacEJB", "SINS_ELENCO_SKVALPUAC", "ElencoSkValPuacHome");
		// gb 07/03/08
		util.export(t, "SINS_CONVOCA_PUAC", pkg+"ConvocaPuacEJB", "SINS_CONVOCA_PUAC", "ConvocaPuacHome");

		// gb 08/05/08: Stampa della tabella 'tab_interventi'.
		util.export(t, "SINS_FOTABINTERVENTI", pkg+"FoTabInterventiEJB", "SINS_FOTABINTERVENTI", "FoTabInterventiHome");

		// gb 07/08/08: Stampa contributi/lettere comunicazioni 2nd
		// autorizzazione.
		util.export(t, "SINS_FOCTRCOMUNASS", pkg+"FoCtrComunAssEJB", "SINS_FOCTRCOMUNASS", "FoCtrComunAssHome");
		// lettera assistito autorizzazione. rsa
		util.export(t, "SINS_FORSALETTSINS", pkg+"FoRsaLetteraDaSinsEJB", "SINS_FORSALETTSINS", "FoRsaLetteraDaSinsEJB");

		// gb 17/09/08:Stampa Schede PUA. Portata dal PUA.
		util.export(t, "SINS_FOSKPUA", pkg+"FoSkPuaEJB", "SINS_FOSKPUA", "FoSkPuaHome");
		// gb 18/09/08:Stampe/Sociale: Appunt. in Agenda di Ass. Sociali.
		// Portata dal PUA.
		util.export(t, "SINS_FOAPPUNAGE", pkg+"FoAppunAgeEJB", "SINS_FOAPPUNAGE", "FoAppunAgeHome");
		// gb 29/09/08:Stampa Singola Scheda PUA. Portata dal PUA.
		util.export(t, "SINS_FOSINGLESKPUA", pkg+"FoSingleSkPUAEJB", "SINS_FOSINGLESKPUA", "FoSingleSkPUAHome");

		// gb 02/10/08:Stampa Elenco Interventi Proposti/Autor.Liv.1/Autor.Liv.2
		util.export(t, "SINS_FOELEINTERVAUTO", pkg+"FoEleIntervAutoEJB", "SINS_FOELEINTERVAUTO", "FoEleIntervAutoHome");
		// gb 08/10/08 (Da Jes.02/09/08)
		util.export(t, "SINS_FOELEACCASS", pkg+"FoEleAccAssEJB", "SINS_FOELEACCASS", "FoEleAccAssHome");
		// gb 19.12.08 (Da Jes. email del 30.10.08)
		util.export(t, "SINS_FOETICHETTE", pkg+"FoEtichetteEJB", "SINS_FOETICHETTE", "FoEtichetteHome");
		// gb 09.02.09: Stampe per Siena (Assistenza domiciliare)
		util.export(t, "SINS_FOCASIADOI", pkg+"FoCasiAdoiEJB", "SINS_FOCASIADOI", "FoCasiAdoi");
		util.export(t, "SINS_FOADIZONA", pkg+"FoEleAdiZonaEJB", "SINS_FOADIZONA", "FoEleAdiZonaHome");
		util.export(t, "SINS_FOTOTASS", pkg+"FoTotAssistitiEJB", "SINS_FOTOTASS", "FoTotAssistiti");
		util.export(t, "SINS_FOCASIADI", pkg+"FoCasiAdiEJB", "SINS_FOCASIADI", "FoCasiAdi");
		util.export(t, "SINS_FONUMACC", pkg+"FoNumeroAccessiEJB", "SINS_FONUMACC", "FoNumeroAccessi");
		// 05/04/2009
		util.export(t, "SINS_FOCASIADOIAUTO", pkg+"FoCasiAdoiAutoEJB", "SINS_FOCASIADOIAUTO", "FoCasiAdoiAuto");

		// gb 02.03.09: Procedure esterne
		util.export(t, "SINS_FORIEPRESTMIL", pkg+"FoRiepPrestMilEJB", "SINS_FORIEPRESTMIL", "FoRiepPrestMilHome");

		// gb 06.03.09: Stampa elenco Prestazioni non autorizzate
		util.export(t, "SINS_FOPRESTNOAUTO", pkg+"FoElePrestNoAutoEJB", "SINS_FOPRESTNOAUTO", "FoElePrestNoAutoHome");

		// 10/03/09: Riepilogo segnalazioni PuntoInsieme
		util.export(t, "SINS_FORIEPSEGNPI", pkg+"FoRiepSegnPIEJB", "SINS_FORIEPSEGNPI", "FoRiepSegnPIHome");

		util.export(t, "SINS_FOSKVALPAP", pkg+"FoSkvalPapEJB", "SINS_FOSKVALPAP", "FoSkvalPapHome");
		util.export(t, "SINS_FOSSTCPAP", pkg+"FoSStcPapEJB", "SINS_FOSKVALPAP", "FoSkvalPapHome");

		// Elisa 19/11/09
		util.export(t, "SINS_FOFARMCACI", pkg+"FoFarmaciEJB", "SINS_FOFARMCACI", "FoFarmaciHome");
		setTitle("SINS_FOFARMCACI", "Elenco Farmaci");

		// Elisa 20/11/09
//		util.export(t, "SINS_FOSCALAHOSPICE", pkg+"FoScalaHospiceEJB", "SINS_FOSCALAHOSPICE", "FoScalaHospiceHome");
//		setTitle("SINS_FOSCALAHOSPICE", "Scheda di Valutazione HOSPICE");
		util.export(t, "SINS_FOSCALASTAS", pkg+"FoScalaSTASEJB", "SINS_FOSCALASTAS", "FoScalaSTASHome");
		setTitle("SINS_FOSCALASTAS", "Support Team Assessment Schedule - (STAS)");


		//elisa b : 09/07/10
		//		util.export(t, "SINS_FOTESTSCPCIRS", pkg+"FoTestScpCirsEJB", "SINS_FOTESTSCPCIRS", "FoTestScpCirsHome");
		//elisa b : 19/07/10
		util.export(t, "SINS_FOTESTSCPCAB", pkg+"FoTestScpCabEJB", "SINS_FOTESTSCPCAB","FoTestScpCabHome");
		//elisa b : 27/08/10
		util.export(t, "SINS_FOVCOINTERVENTI", pkg+"FoSkiVcoInterventiEJB", "SINS_FOVCOINTERVENTI", "FoSkiVcoInterventiHome");
		//bysp 20/09/
		util.export(t, "SINS_FOTESTSCPCEC", pkg+"FoTestScpCecEJB", "SINS_FOTESTSCPCEC", "FoTestScpCecHome");
		//bysp 23/09/10
		util.export(t, "SINS_FOTESTSCPCAS", pkg+"FoTestScpCasEJB", "SINS_FOTESTSCPCAS", "FoTestScpCasHome");

		//bargi 13/10/2010
		util.export(t, "SINS_FOCALCOLOPAI", pkg+"FoStampaCalcoloPaiEJB", "SINS_FOCALCOLOPAI", "FoStampaCalcoloPaiHome");

		//elisa b : 27/10/10
		util.export(t, "SINS_FOTESTSCPADICO", pkg+"FoTestScpAdicoEJB", "SINS_FOTESTSCPADICO", "FoTestScpAdicoHome");
		//elisa b : 27/10/10
		util.export(t, "SINS_FOTESTSCPADLBARTHEL", pkg+"FoTestScpAdlBarthelEJB", "SINS_FOTESTSCPADLBARTHEL", pkg+"FoTestScpAdlBarthelHome");
		//elisa b : 02/11/10
		util.export(t, "SINS_FOSCHEDEVCO", pkg+"FoSchedeVCOEJB", "SINS_FOSCHEDEVCO", "FoSchedeVCOHome");
		//elisa b : 04/11/10
		util.export(t, "SINS_FOSCPEXTONSMITH", pkg+"FoTestScpExtonSmithEJB", "SINS_FOSCPEXTONSMITH", "FoTestScpExtonSmithHome");
		//elisa b : 05/11/10
		util.export(t, "SINS_FOTESTSCPSVSS", pkg+"FoTestScpSvSSEJB", "SINS_FOTESTSCPSVSS", "FoTestScpSvSSHome");
		util.export(t, "SINS_FOSCVALSCA", pkg+"FoScValScaEJB", "SINS_FOSCVALSCA", "FoScValScaHome"); //bysp 10/11/10
		//elisa b : 22/11/10
		util.export(t, "SINS_FOTESTSCPTRATSPEC", pkg+"FoTestScpTratSpecEJB", "SINS_FOTESTSCPTRATSPEC", "FoTestScpTratSpecHome");
		//elisa b : 14/11/10
		util.export(t, "SINS_FOSTATCONTRIBUTI", pkg+"FoStatisticaContributiEJB", "SINS_FOSTATCONTRIBUTI", "FoStatisticaContributiHome");
		//elisa b : 21/12/10
		util.export(t, "SINS_FOGESTRPGRADRSA", pkg+"FoGestRpGradRsaEJB", "SINS_FOGESTRPGRADRSA", "FoGestRpGradRsaHome");
		//Jessica 28/03/2011
		util.export(t, "SINS_FOGESTRGGRADRSA", pkg+"FoGestRgGradRsaEJB", "SINS_FOGESTRGGRADRSA", "FoGestRgGradRsaHome");
		//bg 17/01/2012
		util.export(t, "SINS_GESTLISTARSAVEN", pkg+"GestListaRsaVenetoEJB", "SINS_GESTLISTARSAVEN", "GestListaRsaVenetoHome");

		//elisa b : 21/12/10
		util.export(t, "SINS_FOEROGAZCONTRIB", pkg+"FoErogazioneContributoEJB", "SINS_FOEROGAZCONTRIB", "FoErogazioneContributoHome");
		//elisa b : 14/01/11
		util.export(t, "SINS_FOISTRUTTORIA", pkg+"FoIstruttoriaEJB", "SINS_FOISTRUTTORIA", "FoIstruttoriaHome");
		//elisa b : 14/01/11
		util.export(t, "SINS_FOISTRUTTORIA_A", pkg+"FoIstruttoriaAEJB", "SINS_FOISTRUTTORIA_A", "FoIstruttoriaAHome");
		util.export(t,"SINS_FOISTITUTI", pkg+"FoIstitutiEJB","SINS_FOISTITUTI","FoIstitutiHome");// simone01/03/11
		//08/03/2011 Jessica
		util.export(t,"SINS_FOERRORIMMG", pkg+"FoErroriMMGEJB","SINS_FOERRORIMMG","FoErroriMMGHome");
		//09/03/11 simone
		util.export(t,"SINS_FOANAMNESI", pkg+"FoAnamnesiEJB","SINS_FOANAMNESI","FoAnamnesiHome");
		util.export(t,"SINS_FOSCPRLIADL", pkg+"FoScpRlIadlEJB","SINS_FOSCPRLIADL","FoScpRlIadlHome");
		util.export(t,"SINS_FOSCPRLDEAMB", pkg+"FoScpRlDeambEJB","SINS_FOSCPRLDEAMB","FoScpRlDeambHome");
		util.export(t,"SINS_FOSCPRLNUTRIZ", pkg+"FoScpRlNutrizEJB","SINS_FOSCPRLNUTRIZ","FoScpRlNutrizHome");
		util.export(t,"SINS_FOSCPRLBRADEN", pkg+"FoScpRlBradenEJB","SINS_FOSCPRLBRADEN","FoScpRlBradenHome");
		util.export(t,"SINS_FOSCPRLMMSE", pkg+"FoScpRlMmseEJB","SINS_FOSCPRLMMSE","FoScpRlMmseHome");
		util.export(t,"SINS_FOSCPRLNORTON", pkg+"FoScpRlNortonEJB","SINS_FOSCPRLNORTON","FoScpRlNortonHome");
		util.export(t,"SINS_FORLPAI", pkg+"FoRlPaiEJB","SINS_FORLPAI","FoRlPaiHome");
		util.export(t,"SINS_FOVALSANITARIA", pkg+"FoValSanitariaEJB","SINS_FOVALSANITARIA","FoValSanitariaHome");
		util.export(t,"SINS_FORPVALUTAZASLRSA", pkg+"FoRpValutazAslRsaEJB","SINS_FORPVALUTAZASLRSA","FoRpValutazAslRsaHome");
		util.export(t,"SINS_FORPMONITO", pkg+"FoRpMonitoEJB","SINS_FORPMONITO","FoRpMonitoHome");
		//elisa b : 22/04/11
		util.export(t, "SINS_FORPRICRSA", pkg+"FoRpRicoveriRsaEJB", "SINS_FORPRICRSA", "FoRpRicoveriRsaHome");

		// 02/05/11: m x RME
		util.export(t, "SINS_FORLSCVALSCA", pkg+"FoRLScValScaEJB", "SINS_FORLSCVALSCA", "FoRLScValScaHome");
		//		boffa: stampe per bassano
		util.export(t, "SINS_FOMMGPLS", pkg+"MmgPlsFoEJB", "SINS_FOMMGPLS", "MmgPlsFoHome");
		//		boffa: stampe vco
		util.export(t, "SINS_RIEPFAMMIN", pkg+"RiepFamigliaMinoriEJB", "SINS_RIEPFAMMIN", "RiepFamigliaMinoriFoHome");
		//elisa b: 03/06/11
		util.export(t, "SINS_FORGRICRSA", pkg+"FoRgRicoveriRsaEJB", "SINS_FORGRICRSA", "FoRgRicoveriRsaHome");
		//elisa b : 13/06/11
		util.export(t, "SINS_FOOPPIODIFORTI", pkg+"OppiodiFortiFoEJB", "SINS_FOOPPIODIFORTI", "OppiodiFortiFoHome");
		//elisa b : 14/06/11
		util.export(t, "SINS_FOAMMIDIMI", pkg+"RiepAmmiDimiFoEJB", "SINS_FOAMMIDIMI", "RiepAmmiDimiFoHome");
		//simone : 01/08/11
		util.export(t, "SINS_FOELEATTOPER", pkg+"EleAttOperFoEJB", "SINS_FOELEATTOPER", "EleAttOperFoHome");
		//elisa b : 03/08/11
		util.export(t, "SINS_FOADIMED", pkg+"FoMmgLetteraGuardiaMedicaEJB", "SINS_FOADIMED", "FoMmgLetteraGuardiaMedicaHome");

		//		boffa 30/08/11
		util.export(t, "SINS_FOSLTTPAP", pkg+"FoSLetPapEJB", "SINS_FOSLTTPAP", "FoSkletPapHome");
		//simone 14/09/11
		util.export(t, "SINS_FOGESTRPGRADDGR", pkg+"FoGestRpGradDgrEJB", "SINS_FOGESTRPGRADDGR", "FoGestRpGradDgrHome");
		//elisa b : 29/09/11
		util.export(t, "SINS_FOELEPIAASS", pkg+"FoElencoPianiAssistEJB", "SINS_FOELEPIAASS", "FoElencoPianiAssistHome");
		util.export(t, "SINS_FORIFAN", pkg+"FoRifanEJB", "SINS_FORIFAN", "FoRifanHome");
		//elisa b : 29/09/11
		util.export(t, "SINS_FOELESOSPCONC", pkg+"FoElencoSospConclEJB", "SINS_FOELESOSPCONC", "FoElencoSospConclHome");
		//elisa b : 03/10/11
		util.export(t, "SINS_FOELECONTATTIAPERTI", pkg+"FoElencoContattiApertiEJB", "SINS_FOELECONTATTIAPERTI", "FoElencoContattiApertiHome");

		//serratore 28/10/2011
		util.export(t, "SINS_FOBPCO", pkg+"FoBpcoEJB", "SINS_FOBPCO", "FoBpcoHome");

		//		boffa 30/11/11
		util.export(t, "SINS_CONVOCA_PUACV", pkg+"ConvocaPuacVEJB", "SINS_CONVOCA_PUACV", "ConvocaPuacVHome");

		//simone 02/12/11
		util.export(t, "SINS_FOTESTSCPSTRUTT", pkg+"FoTestScpStruttEJB", "SINS_FOTESTSCPSTRUTT", "FoTestScpStruttHome");

		//		boffa 12/01/12
		util.export(t, "SINS_TOTALI_OPASS", pkg+"FoTrapaniEJB", "SINS_TOTALI_OPASS", "FoTrapaniHome");

		//simone 12/01/12
		util.export(t, "SINS_FOTESTRPPAP", pkg+"FoTestRpPapEJB", "SINS_FOTESTRPPAP", "FoTestRpPapHome");
		//simone 12/01/12
		util.export(t, "SINS_FOTESTRPPAPFAR", pkg+"FoTestRpPapFarEJB", "SINS_FOTESTRPPAPFAR", "FoTestRpPapFarHome");
		//simone 14/01/12
		util.export(t, "SINS_FOSCPVALFAR", pkg+"FoScpValFarEJB", "SINS_FOSCPVALFAR", "FoScpValFarHome");
		//simone 14/01/12
		util.export(t, "SINS_FOSCPVALSIAD", pkg+"FoScpValSiadEJB", "SINS_FOSCPVALSIAD", "FoScpValSiadHome");
		//simone 14/01/12
		util.export(t, "SINS_FOSCPUCLA", pkg+"FoScpUclaEJB", "SINS_FOSCPUCLA", "FoScpUclaHome");
		//bargi 23/01/2012
		util.export(t, "SINS_FORGRSALETT", pkg+"FoRgRsaLettereEJB", "SINS_FORGRSALETT", "FoRgRsaLettereHome");
		// boffa stampa lettere gestione servizi generici
		util.export(t, "SINS_FOCTRCOMUNASSGEN", pkg+"FoCtrComunAssGenEJB", "SINS_FOCTRCOMUNASSGEN", "FoCtrComunAssGenHome");
		//simone 11/04/2012
		util.export(t, "SINS_FOMODELLOSAD", pkg+"FoModelloSadEJB", "SINS_FOMODELLOSAD", "FoModelloSadHome");
		//boffa 09/05/2012
		util.export(t, "SINS_FOELEPIAASSSCAD", pkg+"FoElePianoAssScadEJB", "SINS_FOELEPIAASSSCAD", "FoElePianoAssScadHome");
		util.export(t, "SINS_FOELEASSTLIVCOMP", pkg+"FoEleAssLivCompEJB", "SINS_FOELEASSTLIVCOMP", "FoEleAssLivCompHome");
		util.export(t, "SINS_FOINFSEGMOTIVO", pkg+"FoInfEleInvSegMotivoEJB", "SINS_FOINFSEGMOTIVO", "FoInfEleSegMotHome");
		util.export(t, "SINS_FORVVALMENS", pkg+"FoRvValMensEJB", "SINS_FORVVALMENS", "FoRvValMensHome");
		// boffa 01/09/2012
		util.export(t, "SINS_CONVOCA_PUACD", pkg+"ConvocaPuacDEJB", "SINS_CONVOCA_PUACD", "ConvocaPuacDHome");
		// boffa 04/10/2012
		util.export(t, "SINS_UVMD_MENOM", pkg+"FoUvmdMenomazEJB", "SINS_UVMD_MENOM", "FoUvmdMenomazHome");
		// 20/12/12: da Ilaria
		util.export(t, "SINS_FORIEPADIANNO", pkg+"FoRiepAdiAnnoEJB", "SINS_FORIEPADIANNO", "FoRiepAdiAnnoHome");
		//27/12/12 Marina
		util.export(t, "SINS_FOOBIEINTERVENTI2", pkg+"FoRiepPPrest2EJB", "SINS_FOOBIEINTERVENTI2", "FoRiepPPrest2Home");
		//14/01/13 mv
		util.export(t, "SINS_SAPIOLOADER", pkg+"SapioLoaderEJB", "SINS_SAPIOLOADER", "SapioLoaderHome");
		//minerba
		util.export(t, "SINS_FOTESTASGO", pkg+"FoTestAsgoEJB", "SINS_FOTESTASGO", "FoTestAsgoHome");
		// Minerba 08/01/13: Stampa della tabella 'tab_servizi'.
		util.export(t, "SINS_FOTABSERVIZI", pkg+"FoTabServiziEJB", "SINS_FOTABSERVIZI", "FoTabServiziHome");
		util.export(t, "SINS_FOTABSERVIZICOM", pkg+"FoTabServiziComEJB", "SINS_FOTABSERVIZICOM", "FoTabServiziComHome");
		//minerba
		//		util.export(t, "SINS_FOTESTCONLEY", pkg+"FoTestConleyEJB", "SINS_FOTESTCONLEY", "FoTestConleyHome");
		util.export(t, ScalaConleyFormCtrl.CST_NOME, pkg + ScalaConleyFormCtrl.CST_NOME_EJB,
				ScalaConleyFormCtrl.CST_NOME, ScalaConleyFormCtrl.CST_NOME_HOME);
		util.export(t, "SINS_FOTESTDOLORE", pkg+"FoTestDoloreEJB", "SINS_FOTESTDOLORE", "FoTestDoloreHome");
		//simone 11/02/13
		util.export(t, "SINS_FOCONTATTESA", pkg+"FoContAttesaEJB", "SINS_FOCONTATTESA", "FoContAttesaHome");
		// 26/02/13
		util.export(t, "SINS_FORAELEVAL", pkg+"FoRAEleValEJB", "SINS_FORAELEVAL", "FoRAEleValHome");
		//M.Minerba 28/02/2013
		util.export(t, "SINS_FOELENCOLIVASS", pkg+"FoElencoLivAssEJB", "SINS_FOELENCOLIVASS", "FoElencoLivAssHome");
		util.export(t, "SINS_FOSKCONTATTOSOCIALE", pkg+"FoSkContattoSocialeEJB", "SINS_FOSKCONTATTOSOCIALE", "FoSkContattoSocialeHome");
		//Jessica Gestione tabelle magazzino adi/altro
		util.export(t, "SINS_SP_REPARTO", pkg+"SpRepartoEJB", "SINS_SP_REPARTO", "SpRepartoHome");
		//14/05/2013
		util.export(t, "SINS_FOPHTBARTHEL", pkg+"FoPhtBarthelEJB", "SINS_FOPHTBARTHEL", "FoPhtBarthelHome");
		//Mariarita M. 14/05/2013
		util.export(t, "SINS_FOCONTRATTIMED", pkg+"FoContrattiMedEJB", "SINS_FOCONTRATTIMED", "FoContrattiMedHome");
		//Mariarita M. 19/02/2014
		util.export(t, "SINS_FOELEPAZGEST", pkg+"FoElePazGestitiEJB", "SINS_FOELEPAZGEST", "FoElePazGestitiHome");
		util.export(t, "SINS_FOELEPRESTPAI", pkg+"FoElePrestPaiEJB", "SINS_FOELEPRESTPAI", "FoElePrestPaiHome");
		util.export(t, "SINS_FOELEASSUVM", pkg+"FoEleAssUvmEJB", "SINS_FOELEASSUVM", "FoEleAssUvmHome");
		util.export(t, "SINS_FOELESEDUVM", pkg+"FoEleSeduteUvmEJB", "SINS_FOELESEDUVM", "FoEleSeduteUvmHome");
		util.export(t, "SINS_FOELESVAMA", pkg+"FoEleSvamaEJB", "SINS_FOELESVAMA", "FoEleSvamaHome");
		//Mariarita M. 18/03/2014
		util.export(t, "SINS_FOFLUSSI21", pkg+"FoFlussi21EJB", "SINS_FOFLUSSI21", "FoFlussi21Home");
		util.export(t, "SINS_FOFLUSSI21L", pkg+"FoFlussi21ListaEJB", "SINS_FOFLUSSI21L", "FoFlussi21ListaHome");
		//
		//Mariarita M. 03/11/2014
		util.export(t, "SINS_FOLISTAATTIVITA", pkg_nuovi+"FoListaAttivitaEJB", "SINS_FOLISTAATTIVITA", "FoListaAttivitaHome");
		util.export(t, "SINS_FOLISTAASSISTITI", pkg_nuovi+"FoListaAssistitiEJB", "SINS_FOLISTAASSISTITI", "FoListaAssistitiHome");
		//

		//Boffa 14/01/2015
		util.export(t, "SINS_FOPAI", pkg_nuovi+"FoStampaPaiEJB", "SINS_FOPAI", "FoStampaPaiHome");
		util.export(t, "SINS_FOSO", pkg_nuovi+"FoStampaSOEJB", "SINS_FOSO", "FoStampaSOHome");
		//Mariarita M. 30/01/2015
		util.export(t, "SINS_FORILDOLOREBAM", pkg+"FoRilDoloreBamEJB", "SINS_FORILDOLOREBAM", "FoRilDoloreBamHome");
		//      util.export(t, "SINS_FORILDOLOREAD", pkg+"FoRilDoloreAdEJB", "SINS_FORILDOLOREAD", "FoRilDoloreAdHome");
		util.export(t, ScalaDoloreAdFormCtrl.CST_NOME, pkg + ScalaDoloreAdFormCtrl.CST_NOME_EJB,
				ScalaDoloreAdFormCtrl.CST_NOME, ScalaDoloreAdFormCtrl.CST_NOME_HOME);
		//Mariarita M. 06/03/2015
		//      util.export(t, "SINS_FOSCALARUG", pkg+"FoScalaRugEJB", "SINS_FOSCALARUG", "FoScalaRugHome");
		util.export(t, ScalaRUGFormCtrl.CST_NOME, pkg + ScalaRUGFormCtrl.CST_NOME_EJB, ScalaRUGFormCtrl.CST_NOME,
				ScalaRUGFormCtrl.CST_NOME_HOME);

		//Boffa 19/02/2015
		util.export(t, "SINS_FOREPELEASS", pkg_nuovi+"FoReportElencoAssistitiEJB", "SINS_FOREPELEASS", "FoReportElencoAssHome");

		//Boffa 19/05/2016
		util.export(t, "SINS_FOREPMONICD", pkg_nuovi+"FoReportMonitoraggioCDEJB", "SINS_FOREPMONICD", "FoReportMonitoraggioCDHome");
		util.export(t, "SINS_FORICHMMG", pkg+"FoRichiestaMMGEJB", "SINS_FORICHMMG", "FoRichiestaMMGHome");

		// --------------------------------------------------------------------------------
		//bargi 23/01/2012
		setTitle("SINS_FORGRSALETT", "Stampa Impegnativa Residenziale Veneto");
		//bargi 13/10/2010
		setTitle("SINS_FOCALCOLOPAI","Elaborazione calcolo PAI");
		setTitle("SINS_FOSTATISTICHESVAMA", "Statistiche su SVAMA");


		//		boffa 23/02/2012
		setTitle("SINS_FODIFRSARICOVI","Differenze ricoveri Rsa-ospedaliero");

		setTitle("SINS_FORSACONTRPROP", "Elenco contributi proposti per R.S.A.");
		setTitle("SINS_FORSACONTRPROP", "Elenco contributi proposti per R.S.A.");
		setTitle("SINS_FOINTENSADI", "Elenco assistiti ragruppati per intensita calcolata per figura professionale");
		setTitle("SINS_FOADMMG", "Elenco autorizzazioni ADI/ADP per MMG");
		setTitle("SINS_FOADDISTR", "Elenco autorizzazioni ADI/ADP per Medico di Distretto");
		setTitle("SINS_FOPROGSINS", "Progetto Assistenziale");
		setTitle("SINS_FOPARG", "Rilevazione handicap");
		setTitle("SINS_FODATI", "Estrazione informazioni");
		setTitle("SINS_FOBISASS", "Elenco bisogni assistenziali");
		setTitle("SINS_FOOPERATORI", "Elenco operatori");
		setTitle("SINS_FOSTAMPE", "Tabelle");
		setTitle("SINS_FOCITTADIN", "Elenco cittadinanze");
		setTitle("SINS_FOAREADIS", "Elenco aree distrettuali");
		setTitle("SINS_FOVALAUTO", "Richieste valutazione grado autosufficienza");
		setTitle("SINS_FODISTRETTI", "Elenco distretti");
		setTitle("SINS_FOPROFADI", "Elenco profili adi");
		setTitle("SINS_FOSUBAREA", "Elenco Subaree di intervento");
		setTitle("SINS_FOELERSA", "Elenco utenti in R.S.A.");
		setTitle("SINS_FOTIPOLOGIE", "Elenco Tipologie per contributi");
		setTitle("SINS_FORSAATTESA", "Elenco utenti in lista di attesa");
		setTitle("SINS_FORSACOM", "Elenco utenti per comune e/o anno");
		setTitle("SINS_FOOREOSA", "Rilevazione Presenze O.S.A.");
		//setTitle("SINS_FOVASCCER", "Casi di Patologia specificata");
		setTitle("SINS_FOVASCCER", Labels.getLabel("menu.stampa.vasccer"));//gestione multilingua
		setTitle("SINS_FOINFELENCO", "Riepilogo assistiti per fasce di eta' con contatto aperto");
		//setTitle("SINS_FOIPREST", "Elenco prestazioni");
		setTitle("SINS_FOIPREST", Labels.getLabel("menu.stampa.iprest"));//gestione multilingua
		setTitle("SINS_FOPREST", "Accessi per Operatore");
		setTitle("SINS_FOPRESTDOM", "Statistica Prestazioni Domiciliari");
		setTitle("SINS_FOPRESTAMB", "Statistica Prestazioni Ambulatoriali");
		setTitle("SINS_FOCOSTOPREST", "Valorizzazione Prestazioni Domiciliari");
		setTitle("SINS_FOCOSTOPREST", "Costo Medio Pazienti in Assistenza Domiciliare");
		//setTitle("SINS_FOIASSELE", "Elenco assistiti con almeno un accesso");
		setTitle("SINS_FOIASSELE", Labels.getLabel("menu.stampa.ass_ele"));//gestione multilingua
		setTitle("SINS_FOIASSELE2", "Assistiti per operatore con almeno un accesso");
		//setTitle("SINS_FOFASSELE", "Elenco utenti assistenza fisioterapica");
		setTitle("SINS_FOFASSELE", Labels.getLabel("menu.stampa.fassele"));	//gestione multilingua	
		setTitle("SINS_FOMEDICI", "Elenco medici");
		setTitle("SINS_FOCONECON", "Elenco Contributi Economici Erogati");
		setTitle("SINS_FOCONTTIPO", "Contributi");
		setTitle("SINS_FOMODULI", "Moduli di autorizzazione");
		setTitle("SINS_FOLETTERE", "Lettere alla famiglia");
		setTitle("SINS_FOPRESIDI", "Elenco presidi");
		setTitle("SINS_FOBANCHE", "Elenco banche");
		setTitle("SINS_FOPENSIONI", "Elenco pensioni");
		setTitle("SINS_FORSACONTR", "Elenco contributi R.S.A.");
		setTitle("SINS_FORSACONTI", "Elenco conti economici");
		setTitle("SINS_FOPRESTAZ", "Elenco prestazioni");
		setTitle("SINS_FOBRANCA", "Scheda Svama");
		setTitle("SINS_FOSUSSIDI", "Elenco sussidi");
		//setTitle("SINS_FOMASSELE", "Elenco assistiti M.M.G. con contatto aperto");
		setTitle("SINS_FOMASSELE", Labels.getLabel("menu.stampa.massele"));//gestione multilingua
		setTitle("SINS_FOMSOCELE", "Elenco assistiti per MMG con progetto attivo");
		setTitle("SINS_FOCOPPIE", "Assistenza Coppie");
		setTitle("SINS_FOCONTCOMVERB", "Contributi inseriti da assistente sociale");
		setTitle("SINS_FOELESOC", "Elenco assistiti");
		setTitle("SINS_FOSOCPREST", "Elenco prestazioni per assistenti sociali");
		setTitle("SINS_FOINTERV", "Elenco accessi");
		setTitle("SINS_FOCONTINTERV", "Nominativi relativi a accessi");
		setTitle("SINS_FORICRSA", "Elenco ricoveri in RSA");
		setTitle("SINS_FOCONTGOIF", "Conteggio Goif");
		setTitle("SINS_FOOREASS", "Conteggio ore di assistenza");
		setTitle("SINS_FODETTOREPREST", "Ripilogo ore prestazioni per operatore");
		setTitle("SINS_FOTIPOUTEDIMIS", "Riepilogo attività in funzione del tipo utente e motivo dimissione");
		setTitle("SINS_FORIEPBRANCA", "Riepilogo prestazioni per Branca");
		setTitle("SINS_FOPRESTQUALOP", "Riepilogo prestazioni per qualifica");
		setTitle("SINS_FORIEPBRANCAXRIGA", "Riepilogo prestazioni per Branca");
		setTitle("SINS_FOSINGOLAPROF", "Riepilogo prestazioni/accessi per tutte le figure professionali");
		setTitle("SINS_FORIEPBRANCAOP", "Riepilogo prestazioni per Branca e Operatore");
		setTitle("SINS_FORIEPPRESOP", "Riepilogo prestazioni per Assistente sociale");
		setTitle("SINS_FORIEPUTEN", "Riepilogo utenti per comune");
		setTitle("SINS_FORIEPUTENETASES", "Riepilogo utenti per comune/eta'/sesso con almeno un accesso");
		setTitle("SINS_FOASSMENSILE", "Statistica Pazienti Mensilmente in Assistenza");
		setTitle("SINS_FOASSHANDI", "Elenco Assistiti con Handicap");
		setTitle("SINS_FOCONTECO", "Riepilogo Economato");
		setTitle("SINS_FOPAZACQUISITI", "Statistica Pazienti Mensilmente Acquisiti");
		setTitle("SINS_FOPAZMENSDOM", "Statistica Pazienti in Assistenza Domiciliare (Dett. mensile)");
		setTitle("SINS_FOFREQPAT", "Frequenza Patologie in un periodo");
		setTitle("SINS_FOPAZCOMUNI", "Pazienti per comune di residenza");
		setTitle("SINS_FOPRESTFREQ", "Frequenza Prestazioni Domiciliari in un periodo");
		setTitle("SINS_FOCOSTIPAT", "Costi Patologie in un periodo");
		setTitle("SINS_FOTABPIPP", "Elenco registrazioni PIPP");
		setTitle("SINS_FOCARTELLA", "Cartella Assistito");
		setTitle("SINS_FOBENEFIC", "Beneficiario");
		setTitle("SINS_FOTRASPORTI", "Trasporti");
		setTitle("SINS_FOFISIO", "Scheda Fisioterapista");
		setTitle("SINS_FOUNITA", "Unit� di Valutazione");
		setTitle("SINS_FOCONTSOC", "Scheda Contatti Sociali");
		setTitle("SINS_FOSKSVAMA", "Scheda Svama");
		setTitle("SINS_FORIEPIPP", "Riepilogo PIPP");
		setTitle("SINS_FOCONTOPER", "Conteggio prestazioni inserite per operatore");
		setTitle("SINS_FORIEPDETTPIPP", "Riepilogo Dettaglio PIPP");
		setTitle("SINS_FOSKINF", "Scheda Infermiere");
		setTitle("SINS_FOSKMEDI", "Scheda Medico");
		setTitle("SINS_FOSKFISIO", "Scheda Fisioterapista");
		setTitle("SINS_FOSKIPROGASS", "Scheda Infermieri - Progetto Assistenziale");
		setTitle("SINS_FOMEDPROGASS", "Scheda Medici - Progetto Assistenziale");
		setTitle("SINS_FOFISPROGASS", "Scheda Fisioterapisti - Progetto Assistenziale");
		setTitle("SINS_FOASSPROGASS", "Scheda Assistenti Sociali - Progetto Assistenziale");
		setTitle("SINS_FOPIANOASSIST", "Progetto Assistenziale");
		setTitle("SINS_FOACCHAND", "Elenco Tipologia Handicap");
		setTitle("SINS_FOATTMED", "Attestazione del medico curante");
		setTitle("SINS_FOCONTRIB", "Contributi economici");
		setTitle("SINS_FOPROPSI", "Individuazione di problematiche psichiatriche");
		setTitle("SINS_FOTABADL", "A.D.L. Grado Autonomia nelle attivit� della vita quotidiana");
		setTitle("SINS_FOTABIADL", "I.A.D.L. Scala delle attivit� strumentali quotidiane");
		setTitle("SINS_FOPFEIFFER", "Test di Pfeiffer");
		setTitle("SINS_FOINTERVOPE", "Accessi per Operatore");
		setTitle("SINS_FOSKINFE", "Scheda Infermieristica");
		setTitle("SINS_FOSKSOCIALE", "Scheda Sociale");
		setTitle("SINS_FOCONTLETT", "Lettere comunicazioni assistito");
		//setTitle("SINS_FOELEASSMED", "Elenco Assistiti per medico");
		setTitle("SINS_FOELEASSMED", Labels.getLabel("menu.stampa.eleassmed"));//gestione multilingua
		setTitle("SINS_FOASSDOMED", "Elenco Assistenza domiciliare educativa");
		setTitle("SINS_FOASSDOMDIR", "Elenco Assistenza domiciliare diretta");
		setTitle("SINS_FOASSMINORI", "Elenco Assistenza minori");
		setTitle("SINS_FOASSISTMIN", "Assistenza minori");
		setTitle("SINS_FOPIPP", "Elenco Prestazioni");
		setTitle("SINS_FOSTATDOMICILIARE", "Stampa Statistica Assistenza Infermieristica Domiciliare per anno");
		setTitle("SINS_FSCARICHI", "FARM Stampa consegne per assistito");
		setTitle("SINS_FPATO", "FARM: Stampa Patologie");
		setTitle("SINS_FCARICHI", "FARM: Stampa Carichi");
		setTitle("SINS_FCONSEGNE", "FARM: Stampa materiale da consegnare");
		setTitle("SINS_FOGRSPESA", "Gruppi di spesa");
		setTitle("SINS_FOCONTAPERTI", "Verifica contatti aperti");
		setTitle("SINS_FOADIMESE", "Verifica mensile ADI");
		setTitle("SINS_FO_SIDADI", "Stampe SID - ADI");
		setTitle("SINS_FOSIDADIUTEACC", "Elenco utenti con frequenze di accesso");
		setTitle("SINS_FOSIDADIUTEMMG", "Elenco utenti con frequenze di accesso di MMG/PLS");
		setTitle("SINS_FOSIDADIDIMESSI", "Dimessi nel periodo per motivo e tipo assistenza");
		setTitle("SINS_FOSIDADIMED", "Utenza del profilo D-ADIMED per condizione specifica");
		setTitle("SINS_FOMCPELEASS", "Elenco Assistiti per medici cure palliative");
		//setTitle("SINS_FOINFELEASS", "Elenco Assistiti per Infermieri");
		setTitle("SINS_FOINFELEASS", Labels.getLabel("menu.stampa.infeleass"));//gestione multilingua
		setTitle("SINS_FOINFELEMOT", "Elenco Assistiti per infermieri per motivo di dimissione");
		setTitle("SINS_FOINFELEINV", "Elenco Assistiti per infermieri per soggetto inviante");
		setTitle("SINS_FOPRESTDINAM", "Riepilogo Prestazioni per motivo, tipologia utente e problematica");
		setTitle("SINS_FOSITPERADI", "Riepilogo utenti secondo situazione riepilogo organizzativo ADI");
		setTitle("SINS_FOSIDADIUTEPROFANNO", "Elenco assistiti secondo profilo ADI");
		setTitle("SINS_FOELESKUVT", "Assistiti con scheda Unita di Valutazione");
		setTitle("SINS_FOSERVIZIOADI", "Servizio di assistenza domiciliare");
		setTitle("SINS_REGACC", "Elenco Registrazione accessi");
		setTitle("SINS_FOLISTAMENS", "Lista mensile assistiti per MMG/PLS");
		setTitle("SINS_FOPRESTASS", "Riepilogo prestazioni per assistito");
		setTitle("SINS_FORIESAME", "Elenco casi da riesaminare");
		setTitle("SINS_FORIEPASS", "Riepilogo Assistiti");
		setTitle("SINS_FOUTEPATO", "Utenti per patologia");
		setTitle("SINS_FOUTEPROGE", "Utenti per profilo assistenziale");
		setTitle("SINS_FOTESTGDS", "Test GDS");
		setTitle("SINS_FOTESTCBI", "Test CBI");
		setTitle("SINS_FOTESTADL", "Test ADL");
		//		setTitle("SINS_FOTESTIADL", "Test IADL");
		setTitle(ScalaIADLFormCtrl.CST_NOME, ScalaIADLFormCtrl.CST_TITOLO_STAMPA);
		//		setTitle("SINS_FOTESTBADL", "Test BADL");
		setTitle(ScalaADLIndexFormCtrl.CST_NOME, ScalaADLIndexFormCtrl.CST_TITOLO_STAMPA);
		setTitle(ScalaSPMSQFormCtrl.CST_NOME, ScalaSPMSQFormCtrl.CST_TITOLO_STAMPA);
		setTitle("SINS_FOTESTMDSC", "Test  MDSC Disturbi del Comportamento");
		setTitle("SINS_FOTESTMDSU", "Test  MDSU Dell'Umore");
		setTitle("SINS_FOTESTCIRS", "Test CIRS");
		setTitle("SINS_FOTESTPROT", "Livello di protezione nello spazio vita");
		setTitle("SINS_FOTESTPFEIFFER", "Test PFEIFFER");
		setTitle("SINS_FOTESTMMSE", "Test Mini-Mental state examination");
		setTitle("SINS_FOTESTNPI", "Ucla Neuropsychiatric Inventory");
		setTitle("SINS_FOTESTTINETTI", "Scala di Tinetti");
		setTitle("SINS_FOTESTRESPON", "Scala di responsabilizzazione");
		setTitle("SINS_FOTESTICA", "Indice di copertura Assistenziale");
		setTitle("SINS_RUTESCALE", "Riepilogo punteggi assistiti");
		setTitle("SINS_FORADSCAL", "Schema polare");
		setTitle("SINS_FOASSNOPATO", "Elenco Assistiti senza patologia");
		setTitle("SINS_FO_RIC_FAM", "Stampa ricoveri");
		//		setTitle("SINS_FOTESTBARTHEL", "Barthel Index Modificato");
		setTitle(ScalaBarthelFormCtrl.CST_NOME, ScalaBarthelFormCtrl.CST_TITOLO_STAMPA);
		setTitle(ScalaBisogniFormCtrl.CST_NOME, ScalaBisogniFormCtrl.CST_TITOLO_STAMPA);
		setTitle(ScalaIADLFormCtrl.CST_NOME,ScalaIADLFormCtrl.CST_TITOLO_STAMPA);
		setTitle("SINS_FOSCALACAREG", "Test Caregiver");
		//		setTitle("SINS_FOSCALABRADEN", "Test Braden");
		setTitle(ScalaBradenFormCtrl.CST_NOME, ScalaBradenFormCtrl.CST_TITOLO_STAMPA);
		setTitle("SINS_FOSCALATIQ", "Test TQI");
		setTitle("SINS_FOSCLVALUTAZ", "Scala Valutazione");
		setTitle("SINS_FOTESTSCHEDAINFERM", "Scheda Infermieristica");
		setTitle("SINS_FOTESTSCHEDACLINICA", "Scheda Clinica");
		setTitle("SINS_FOPROFORG", "Utenti per profilo organizzativo progettato");
		setTitle("SINS_FOTESTMINIST", "Scheda Ministeriale");
		//		setTitle("SINS_FOTESTSCPCIRS", "Test Scp Cirs");
		setTitle(ScalaCirsFormCtrl.CST_NOME, ScalaCirsFormCtrl.CST_TITOLO_STAMPA);

		// dimissioni ospedaliere    
		setTitle("SINS_FOSEGNALA", "SINS: Segnalazione");
		setTitle("SINS_FOCONSULTA", "SINS: Consultazione segnalazioni");

		// --------Stampe vigilanza------------------
		setTitle("SINS_FOVSPERSONALE", "VIGILANZA: Test Ore Personale");
		setTitle("SINS_FOVSERBALE", "VIGILANZA: Verbale di Seduta");
		setTitle("SINS_FOVSSTRUTTURA", "VIGILANZA: Test Struttura");
		setTitle("SINS_FOVSLETTERA", "VIGILANZA: Lettera al Sindaco");

		setTitle("SINS_FOSESTRUTTURE", "STRUTTURE ESTERNE: Elenco strutture convenzionate");
		setTitle("SINS_FOSEOPERATORI", "STRUTTURE ESTERNE: Elenco operatori");
		setTitle("SINS_FOSEPRESCOSTI", "STRUTTURE ESTERNE: Prestazioni e costi");

		// --------Stampe segretariato sociale-----------------
		setTitle("SINS_FOCONTSSOCINT", "SEGRETARIATO SOCIALE: Nominativi relativi a accessi ");
		setTitle("SINS_FOSSOCINT", "SEGRETARIATO SOCIALE: Elenco accessi");
		setTitle("SINS_FOSSOCPREST", "SEGRETARIATO SOCIALE: Elenco prestazioni per operatore");
		setTitle("SINS_FOSSOCRIEPINT", "SEGRETARIATO SOCIALE: Riepilogo accessi");
		setTitle("SINS_FOSSOCRIPREST", "SEGRETARIATO SOCIALE: Riepilogo prestazioni");

		setTitle("SINS_FOCARTCHIUSE", "SINS: Elenco cartelle chiuse");
		setTitle("SINS_FORIEPOSPDIM", "SINS: Elenco assistiti per ospedale di dimissione");

		// --------Assegno di Cura--------------------------------
		setTitle("SINS_FOGESTASSEGNO", "Assegno di Cura");
		setTitle("SINS_GESTASSEGNOGRAD", "Graduatoria Assegno di Cura");
		setTitle("SINS_FOGESTASSEGNOEROG", "Elenco assistiti beneficiari Assegno di Cura");
		setTitle("SINS_FOELENCODOM", "Elenco domande per Assegno di Cura");
		setTitle("SINS_FOASSGRADUATORIA", "Stampa Campi per Calcolo Graduatoria");
		setTitle("SINS_FOASSCAMPIPIENI", "Stampa Campi Valorizzati in Assegno di Cura");

		// x medico oncologo
		setTitle("SINS_FOSKMEDPAL", "Scheda Medico delle cure palliative");
		//		setTitle("SINS_FOSCALAKPS", "Test KPS");
		setTitle(ScalaKarnofskyFormCtrl.CST_NOME, ScalaKarnofskyFormCtrl.CST_TITOLO_STAMPA);
		//		setTitle("SINS_FOSCALAWOUND", "Scala Wound Bed Score");
		setTitle(ScalaWoundFormCtrl.CST_NOME, ScalaWoundFormCtrl.CST_TITOLO_STAMPA);
		setTitle("SINS_FOSCALAPAP", "Test PAP");
		setTitle("SINS_FOSCALANRS", "Test NRS");
		setTitle("SINS_FORIEPMOVMEDPAL", "Riepilogo movimenti medico cure palliative");
		setTitle("SINS_FOSTATKPS", "Statistica valori scala KPS");
		setTitle("SINS_FORIEPDECESSI", "Riepilogo decessi");
		setTitle("SINS_FOCONTAGIORNI", "Conteggio totale dei giorni di assistenza");
		setTitle("SINS_FOCONTADIAGNOSI", "Conteggio pazienti per diagnosi");

		// AGENDA
		setTitle("SINS_FOAGENDAGIORN", "STAMPA AGENDA PER GIORNO");

		// stampe importate dal SINS_AS
		setTitle("SINS_FOPROGAPERTI", "Assistenza Sociale: Progetti Aperti");
		setTitle("SINS_FOOPERABILITATI", "Assistenza Sociale: Operatori Abilitati");
		setTitle("SINS_FOOBIEINTERVENTI", "Assistenza Sociale: Obiettivi / Interventi");

		// gb 21/01/08
		setTitle("SINS_ELENCO_SKVALPUAC", "Elenco Schede Valutazione PUAC");

		// gb 08/05/08: Stampa della tabella 'tab_interventi'.
		setTitle("SINS_FOTABINTERVENTI", "Report Tabella tab_interventi");

		// gb 17/09/08:Stampe/Sociale: Schede PUA. Portata dal PUA.
		setTitle("SINS_FOSKPUA", "STAMPA SCHEDE PUNTO INSIEME");
		// gb 18/09/08:Stampe/Sociale: Appunt. in Agenda di Ass. Sociali.
		// Portata dal PUA.
		setTitle("SINS_FOAPPUNAGE", "STAMPA APPUNT. IN AGENDA DI Ass. Sociali.");
		// gb 29/09/08:Stampa Singola Scheda PUA. Portata dal PUA.
		setTitle("SINS_FOSINGLESKPUA", "STAMPA SCHEDA PUNTO INSIEME");

		// gb 02/10/08:Stampa Elenco Interventi Proposti/Autor.Liv.1/Autor.Liv.2
		setTitle("SINS_FOELEINTERVAUTO", "STAMPA ELENCO INTERVENTI PROPOSTI/AUTORIZZATI");
		// gb 08/10/08 (Da Jes.02/09/08)
		setTitle("SINS_FOELEACCASS", "Elenco accessi per assistito");

		setTitle("SINS_FOCTRCOMUNASS", "Lettere Comunicazioni Assistito per Contributi");
		// gb 19.12.08 (Da Jes. email del 30.10.08)
		setTitle("SINS_FOETICHETTE", "SINS: Etichette per assistito");

		// gb 09.02.09: Stampe per Siena (Assistenza domiciliare)
		setTitle("SINS_FOCASIADOI", "Casi seguiti in ADI malati terminali");
		setTitle("SINS_FOADIZONA", "Elenco autorizzazioni ADI/ADP per zona");
		setTitle("SINS_FOTOTASS", "Totale assistiti");
		setTitle("SINS_FOCASIADI", "Casi seguiti in ADI");
		setTitle("SINS_FONUMACC", "Numero accessi per operatore");
		// 05/04/2009
		setTitle("SINS_FOCASIADOIAUTO", "Accessi effettuati ai pazienti in ADOI");

		// gb 06.03.09: Stampa elenco Prestazioni non autorizzate
		setTitle("SINS_FOPRESTNOAUTO", "Elenco Prestazioni Non Autorizzate");

		// 10/03/09: Riepilogo segnalazioni PuntoInsieme
		setTitle("SINS_FORIEPSEGNPI", "Riepilogo segnalazioni PuntoInsieme");

		setTitle("SINS_FOSKVALPAP", "Scheda progetto assistenziale personalizzato");

		// 21/04/10
		setTitle("SINS_FOSKSACEF", "Scheda di valutazione sociale");

		//elisa b 09/07/10
		//		setTitle("SINS_FOTESTSCPCIRS", "Test Scp Cirs");
		//elisa b 19/07/10
		setTitle("SINS_FOTESTSCPCAB", "Test Scp Cab");
		//elisa b 27/08/10
		setTitle("SINS_FOVCOINTERVENTI", "Scheda osservazione e medicazione");
		//bysp 20/09/10
		setTitle("SINS_FOTESTSCPCEC", "Condizione economica");
		//bysp 23/09/10
		setTitle("SINS_FOTESTSCPCAS", "Condizione assistenziale");

		//elisa b : 27/10/10
		setTitle("SINS_FOTESTSCPADICO", "Test A. Di. Co");
		//elisa b : 27/10/10
		setTitle("SINS_FOTESTSCPADLBARTHEL", "Test Adl Barthel");
		//elisa b : 02/11/10
		setTitle("SINS_FOSCHEDEVCO", "Stampa schede VCO");
		//elisa b : 04/11/10
		setTitle("SINS_FOSCPEXTONSMITH", "Test Exton Smith");
		//elisa b : 05/11/10
		setTitle("SINS_FOTESTSCPSVSS", "Test Sintesi valutazione sociale e sanitaria");
		setTitle("SINS_FOSCVALSCA", "Elenco Valutazioni in Scadenza");
		//elisa b : 22/11/10
		setTitle("SINS_FOTESTSCPTRATSPEC", "Trattamenti specialistici");
		//elisa b : 14/11/10
		setTitle("SINS_FOSTATCONTRIBUTI", " Statistica Contributi");
		//elisa b : 21/12/10
		setTitle("SINS_FOGESTRPGRADRSA", "Gestione Graduatoria Piemonte");
		//28/03/2011
		setTitle("SINS_FOGESTRGGRADRSA", "Gestione Graduatoria Veneto");
		//elisa b : 21/12/10
		setTitle("SINS_FOEROGAZCONTRIB", "Erogazione Contributo Economico");
		//elisa b : 14/01/11
		setTitle("SINS_FOISTRUTTORIA", "Istruttoria UVG");
		//elisa b : 14/01/11
		setTitle("SINS_FOISTRUTTORIA_A", "Istruttoria UVG");
		setTitle("SINS_FOISTITUTI","SINS: Elenco Istituti");//simone 01/03/11
		// 08/03/2011 Jessica
		setTitle("SINS_FOERRORIMMG","SINS: Controllo accessi MMG e personale sanitario");
		//09/03/11 simone
		setTitle("SINS_FOANAMNESI", "SINS: Anamnesi / Diario ");
		setTitle("SINS_FOSCPRLIADL", "SINS: I.A.D.L. ");
		setTitle("SINS_FOSCPRLDEAMB", "SINS: Indice di Deambulazione");
		setTitle("SINS_FOSCPRLNUTRIZ", "SINS: Valutazione Nutrizionale");
		setTitle("SINS_FOSCPRLBRADEN", "SINS: Indice di Braden");
		setTitle("SINS_FOSCPRLMMSE", "SINS: Mini Mental State Examination");
		setTitle("SINS_FOSCPRLNORTON", "SINS: Indice di Norton");
		setTitle("SINS_FOVALSANITARIA", "SINS: Valutazione Sanitaria");
		setTitle("SINS_FORPVALUTAZASLRSA", "SINS: Conteggi valutazioni in scadenza");
		setTitle("SINS_FORPMONITO", "SINS: Monitoraggio Regionale RSA");
		//elisa b : 22/04/11
		setTitle("SINS_FORPRICRSA", "SINS: Elenco ricoverati RSA");
		// 02/05/11: m x RME
		setTitle("SINS_FORLSCVALSCA", "Elenco Valutazioni in Scadenza");

		setTitle("SINS_FOMMGPLS","Elenco MMG/PLS stampe");
		setTitle("SINS_RIEPFAMMIN","Statistiche ISTAT");
		//elisa b: 03/06/11
		setTitle("SINS_FORGRICRSA", "SINS: Gestione Centri Servizi");
		//elisa b : 13/06/11
		setTitle("SINS_FOOPPIODIFORTI", "SINS: Elenco oppiodi forti somministrati");
		//elisa b : 14/06/11
		setTitle("SINS_FOAMMIDIMI", "SINS: Elenco Ammissioni/Dimissioni");
		// 01/08/11
		setTitle("SINS_FOELEATTOPER", "SINS: Elenco Attivit� Operatori");
		//elisa b : 03/08/11
		setTitle("SINS_FOADIMED", "SINS: Lettera Per Guardia Medica");
		//		boffa 30/08/11
		setTitle("SINS_FOSLTTPAP", "SINS: Lettere Comunicazioni Pap");
		//simone 14/09/11
		setTitle("SINS_FOGESTRPGRADDGR","SINS: Graduatoria Dgr 39/56");
		//elisa b : 29/09/11
		setTitle("SINS_FOELEPIAASS", "SINS: Elenco Piani Assistenziali");
		//elisa b : 29/09/11
		setTitle("SINS_FOELESOSPCONC", "SINS: Elenco Sospensioni / Conclusioni");
		//elisa b : 03/10/11
		setTitle("SINS_FOELECONTATTIAPERTI", "SINS: Elenco contatti aperti");

		//serratore 28/10/2011
		setTitle("SINS_FOBPCO", "BPCO: Stampa scheda di controllo periodico");
		//simone 02/12/11
		setTitle("SINS_FOTESTSCPSTRUTT", "Stampa Lettera Strutture convenzionate");
		//simone 12/01/12
		setTitle("SINS_FOTESTRPPAP", "Stampa Progetto assistenziale domiciliare");
		//simone 12/01/12
		setTitle("SINS_FOTESTRPPAPFAR", "Stampa Progetto assistenziale residenziale");
		//simone 12/01/12
		setTitle("SINS_FOSCPVALSIAD", "Stampa Valutazione SIAD");
		//simone 12/01/12
		setTitle("SINS_FOSCPVALFAR", "Stampa Valutazione FAR");
		//simone 12/01/12
		setTitle("SINS_FOSCPUCLA", "Stampa Scala UCLA_NPI");
		//bg 17/01/2012
		setTitle("SINS_GESTLISTARSAVEN","Stampa Graduatoria RSA Veneto");
		// boffa 28/03/2012 stampa lettere gestione servizi generici
		setTitle("SINS_FOCTRCOMUNASSGEN", "Comunic. assistito autorizz. Generico");
		setTitle("SINS_FOMODELLOSAD","Modello SAD");
		//boffa 09/05/2012
		setTitle("SINS_FOELEPIAASSSCAD","Elenco piani assistenziali in scadenza");
		setTitle("SINS_FOELEASSTLIVCOMP","Elenco assistiti per livello assistenziale");
		setTitle("SINS_FOINFSEGMOTIVO", "Elenco assistiti per contatto infermieristico");
		setTitle("SINS_FORVVALMENS","Inserimento Automatico Valutazioni mensili (Rapporto)");

		setTitle("SINS_UVMD_MENOM","Stampa Scala Menomazione delle funzioni corporee");

		// 20/12/12 da Ilaria
		setTitle("SINS_FORIEPADIANNO", "Riepilogo annuale attivit� ADI sanitaria");

		//27/12/12 Marina
		setTitle("SINS_FOOBIEINTERVENTI2", "Riepilogo prestazioni professionali");

		//14/01/13 mv
		setTitle("SINS_SAPIOLOADER", "Elenco inport prestazioni SAPIO");

		//minerba
		setTitle("SINS_FOTESTASGO", "Scheda A.S.G.O.");
		// Minerba 08/01/13: Stampa della tabella 'tab_servizi'.
		setTitle("SINS_FOTABSERVIZI", "Report Tabella tab_servizi");
		setTitle("SINS_FOTABSERVIZICOM", "Elenco Servizi per Comune");
		//minerba 23/01/2013
		//		setTitle("SINS_FOTESTCONLEY", "Scala CONLEY");
		setTitle(ScalaConleyFormCtrl.CST_NOME, ScalaConleyFormCtrl.CST_TITOLO_STAMPA);
		setTitle("SINS_FOTESTDOLORE", "Valutazione del dolore");
		//simone 11/02/13
		setTitle("SINS_FOCONTATTESA", "Graduatoria Contributi");
		// 26/02/13
		setTitle("SINS_FORAELEVAL", "Elenco valutazioni effettuate");
		setTitle("SINS_FOELENCOLIVASS", "Elenco assistiti per livello assistenziale");
		//M.Minerba 04/03/2013
		setTitle("SINS_FOSKCONTATTOSOCIALE", "Scheda Contatto Sociale");

		//Jessica 08/04/13
		setTitle("SINS_SP_REPARTO", "Elenco reparti magazzino adi/altro");
		setTitle("SINS_FOPHTBARTHEL", "Barthel Index Modificato");
		//M.Minerba 14/05/2013
		setTitle("SINS_FOCONTRATTIMED", "Autorizzazioni di Contratti Medici");
		//M.Minerba 19/02/2014
		setTitle("SINS_FOELEPAZGEST", "Elenco pazienti gestiti nel periodo");
		setTitle("SINS_FOELEPRESTPAI", "Elenco prestazioni assegnate all'assistito sul pai");
		setTitle("SINS_FOELEASSUVM", "Elenco assistiti UVM");
		setTitle("SINS_FOELESEDUVM", "Elenco sedute UVM");
		setTitle("SINS_FOELESVAMA", "Elenco SVAMA");
		//M.Minerba 18/03/2014
		setTitle("SINS_FOFLUSSI21", "Flussi FLS21");
		setTitle("SINS_FOFLUSSI21L", "Flussi FLS21");
		//M.Minerba 03/11/2014
		setTitle("SINS_FOLISTAATTIVITA", "Stampa Lista Attività");
		setTitle("SINS_FOLISTAASSISTITI", Labels.getLabel("menu.stampa.lista_assistiti"));//gestione multilingua

		setTitle("SINS_FOPAI", "Stampa PAI Assistito");
		setTitle("SINS_FOSO", "Stampa Scheda Segreteria Organizzativa");

		//setTitle("SINS_FOREPELEASS", "Stampa Report Elenco Assistiti");
		setTitle("SINS_FOREPELEASS", Labels.getLabel("menu.stampa.repeleass"));//gestione multilingua
		//M.Minerba 30/01/2015
		setTitle("SINS_FORILDOLOREBAM", "Stampa rilevazione del dolore nel bambino");
		//      setTitle("SINS_FORILDOLOREAD", "Stampa rilevazione del dolore nell'adulto");
		setTitle(ScalaDoloreAdFormCtrl.CST_NOME, ScalaDoloreAdFormCtrl.CST_TITOLO_STAMPA);
		//M.Minerba 06/03/2015
		//      setTitle("SINS_FOSCALARUG", "Stampa Scala RUGIII");
		setTitle(ScalaRUGFormCtrl.CST_NOME, ScalaRUGFormCtrl.CST_TITOLO_STAMPA);
		//setTitle("SINS_FOREPMONICD", "Stampa Monitoraggio cure domiciliari");
		setTitle("SINS_FOREPMONICD", Labels.getLabel("menu.stampa.repmonicd"));//gestione multilingua
		setTitle("SINS_FORICHMMG", Labels.getLabel("richiesta_mmg.stampa.titolo"));      

		//M.Minerba 13/06/2016
		util.export(t, "SINS_FOALTROELEASS", pkg_nuovi+"FoAltroEleAssEJB", "SINS_FOALTROELEASS", "FoAltroEleAssHome");
		//setTitle("SINS_FOALTROELEASS", "Elenco Assistiti con contatto aperto");
		setTitle("SINS_FOALTROELEASS", Labels.getLabel("menu.stampa.altroeleass"));//gestione multilingua

		util.export(t, "SINS_FOREPORTESTRRUG", pkg_nuovi+"FoReportEstrRUGEJB", "SINS_FOREPORTESTRRUG", "FoReportEstrRUGHome");
		setTitle("SINS_FOREPORTESTRRUG", "Estrazione RUG III HC");
		
		util.export(t, ScalaDisabilitaComFormCtrl.CST_NOME, pkg_nuovi + ScalaDisabilitaComFormCtrl.CST_NOME_EJB, ScalaDisabilitaComFormCtrl.CST_NOME,
				ScalaDisabilitaComFormCtrl.CST_NOME_HOME);
		setTitle( ScalaDisabilitaComFormCtrl.CST_NOME, ScalaDisabilitaComFormCtrl.CST_TITOLO_STAMPA);
		
		util.export(t, ScalaBarthelIndexModFormCtrl.CST_NOME, pkg_nuovi + ScalaBarthelIndexModFormCtrl.CST_NOME_EJB, ScalaBarthelIndexModFormCtrl.CST_NOME,
				ScalaBarthelIndexModFormCtrl.CST_NOME_HOME);
		setTitle( ScalaBarthelIndexModFormCtrl.CST_NOME, ScalaBarthelIndexModFormCtrl.CST_TITOLO_STAMPA);

		util.export(t, ScalaFimFormCtrl.CST_NOME, pkg_nuovi + ScalaFimFormCtrl.CST_NOME_EJB, ScalaFimFormCtrl.CST_NOME,
				ScalaFimFormCtrl.CST_NOME_HOME);
		setTitle( ScalaFimFormCtrl.CST_NOME, ScalaFimFormCtrl.CST_TITOLO_STAMPA);
	} // End of init() method

} // End of SINSFoServlet class

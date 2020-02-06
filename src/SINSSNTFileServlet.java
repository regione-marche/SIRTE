/**
* Servlet di implementazione per elaborazioni con file
*/
import javax.servlet.*;

import it.pisa.caribel.gprs2.*;

public class SINSSNTFileServlet extends FileRouterServlet {

public void init(ServletConfig conf) throws ServletException {
	System.out.println("SINSSNTFileServlet.init()...");

	try {
		inizializza(conf);
		setServletName("SINSSNTFileServlet");
	} catch(Exception ex) {
		throw new ServletException(ex.getMessage());
	}
/*CLASSTYPEbegin
	int t = GprsElement.CLASS_TYPE;
/*CLASSTYPEend
/*EJBTYPEbegin*/
	//int t = GprsElement.EJB_TYPE;
	int t = GprsElement.CLASS_TYPE;
/*EJBTYPEend*/


util.export(t,"SINS_FSCACONS","FSCAConsEJB","SINS_FSCACONS","FSCAConsHome");
util.export(t,"SINS_FSCARTRA","FScarTrasfEJB","SINS_FSCARTRA","FScarTrasfHome");
util.export(t,"SINS_FMARNO","FMarnoEJB","SINS_FMARNO","FMarnoHome");
util.export(t,"SINS_PROTEFLUXADREG","ProteFluxAdRegEJB","SINS_PROTEFLUXADREG","ProteFluxAdRegHome");
util.export(t,"SINS_FLUSSI_VEN","SinsFlussiVenetoEJB","SINS_FLUSSI_VEN","SinsFlussiVenetoHome");
util.export(t,"SINS_FLUSSI_PRESTAZ","SinssFlussiPrestazEJB","SINS_FLUSSI_PRESTAZ","SinssFlussiPrestazHome");
util.export(t,"SINS_TABPIPP", "TabpippEJB", "SINS_TABPIPP", "TabpippHome");
util.export(t,"SINS_FLUSSI_NAZ","SinsFlussiNazionaliEJB","SINS_FLUSSI_NAZ","SinsFlussiNazionaliHome");
util.export(t,"SINS_FLUSSI_SAV","SinsFlussiSavEJB","SINS_FLUSSI_SAV","SinsFlussiSavHome");
// 15/01/10: Flussi AD-RSA Ministero
util.export(t,"SINS_FLUADRSA_MIN","FlussiADRSAMinEJB","SINS_FLUADRSA_MIN","FlussiADRSAMinHome");
util.export(t,"SINS_FLUADRSA_PIEM","FlussiADRSAPiemEJB","SINS_FLUADRSA_PIEM","FlussiADRSAPiemHome");
// 10/10/11: Flussi SINBA
util.export(t,"SINS_FLUSINBA","FlussiSinbaEJB","SINS_FLUSINBA","FlussiSinbaHome");
//18/01/13: Flussi SPR
util.export(t,"SINS_FLUSSI_SPR","FlussiSPREJB","SINS_FLUSSI_SPR","FlussiSPRHome");
//06/08/2013
util.export(t,"SINS_ESTRAZ_SILI","EstrazioneSILIEJB","SINS_ESTRAZ_SILI","EstrazioneSILIHome");


}	// End of init() method

}	// End of SINSFileServlet class

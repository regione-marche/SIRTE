/**
 * Servlet di implementazione per le stampe
 */
import javax.servlet.*;

import it.pisa.caribel.gprs2.*;

public class SINSSNTReportServlet extends Report2RouterServlet {

	private static String pkg = "it.caribel.app.sinssnt.bean.";
	private static String pkg_mod = "it.caribel.app.sinssnt.bean.modificati.";
	private static String pkg_nuovi = "it.caribel.app.sinssnt.bean.nuovi.";
	
	public void init(ServletConfig conf) throws ServletException {
		try {
			inizializza(conf, "SINSSNTReportServlet");
		} catch (Exception ex) {
			throw new ServletException(ex.getMessage());
		}
		
		/*CLASSTYPEbegin*/
		int t = GprsElement.CLASS_TYPE;
		/*CLASSTYPEend*/
		/*EJBTYPEbegin
		int t = GprsElement.EJB_TYPE;
		EJBTYPEend*/

		util.export(t, "JOBEJB", pkg_nuovi+"JobEJB", "JOBEJB", "JobHome");

		//Specificare come di seguito evidenziato tutte le stampe
		this.util.export(t, "SINS_FOXXYY", pkg_nuovi+"FoXxYyEJB", "SINS_FOXXYY", "FoXxYyHome");
		setTitle("SINS_FOXXYY", "Stampa xxxx yyyyyyy");

	}
}
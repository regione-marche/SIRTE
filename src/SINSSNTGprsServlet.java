import it.pisa.caribel.gprs2.GPRouterServlet;
import it.pisa.caribel.gprs2.GprsElement;
import it.pisa.caribel.utilProcedure.ManagerAnagrafica;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

public class SINSSNTGprsServlet extends GPRouterServlet
{

	public static String pkg = "it.caribel.app.sinssnt.bean.";
	public static String pkg_common ="it.caribel.app.common.ejb.";
	
	public void init(ServletConfig conf) throws ServletException {
		System.out.println("SINSSNTGprsServlet.init()...");
		try {
			inizializza(conf);
			setServletName("SINSSNTGprsServlet");
		} catch(Exception ex) {
			throw new ServletException(ex.getMessage());
		}

		/*CLASSTYPEbegin
			int t = GprsElement.CLASS_TYPE;
		/*CLASSTYPEend*/
		/*EJBTYPEbegin*/
			//int t = GprsElement.EJB_TYPE;
			int t = GprsElement.CLASS_TYPE;
		/*EJBTYPEend*/

		//la procedura sins utilizza la mutua esclusione per le connessioni perci� passa in pi� true a ISASEnv
		util.exportIsasEnv(t, "SINSISASENV", pkg_common+"IsasEnvEJB", "SINSISASENV", "IsasEnvHome", conf.getServletContext(), true);
		// utilieties
		util.export(t, "SINS_INFOSISTEMA", pkg_common+"InfoSistemaEJB","SINS_INFOSISTEMA", "InfoSistemaHome");
		
		//Esporto l'EJB di servizio per la chiusura anagrafica
		util.export(t, ManagerAnagrafica.CHIUSANAGR_KEY_EJB, "it.pisa.caribel.sinssnt.controlli.ChiusuraAnagraficaEJB",ManagerAnagrafica.CHIUSANAGR_KEY_EJB, "ChiusuraAnagraficaHome");
	}

}	// End of SINSGprsServlet class

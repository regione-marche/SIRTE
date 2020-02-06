import java.io.*;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

import it.pisa.caribel.gprs2.GprsElement;
import it.pisa.caribel.gprs2.GprsUtil;

public class AsterdroidServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static String pkg = "it.caribel.app.sinssnt.bean.";
	public static String pkg_common ="it.caribel.app.common.ejb.";
	
	protected GprsUtil util = null;

    public void init(ServletConfig conf) throws ServletException {
        if (util == null) {
            util = new GprsUtil(this.getClass().getClassLoader());
        }

        /*CLASSTYPEbegin
		int t = GprsElement.CLASS_TYPE;
	/*CLASSTYPEend*/
	/*EJBTYPEbegin*/
		//int t = GprsElement.EJB_TYPE;
		int t = GprsElement.CLASS_TYPE;
	/*EJBTYPEend*/   

	util.export(t, "ASTERDROID", pkg+"AsterDroidEJB", "ASTERDROID", "AsterDroidHome");
	util.export(t, "ASTERDROIDQRC", pkg+"AsterDroidQRCodeEJB60", "ASTERDROIDQRC", "AsterDroidQRCHome");
	util.export(t, "ASTERDROIDINF", pkg+"AsterDroidInfEJB", "ASTERDROIDINF", "AsterDroidInfHome");
    }

    public void destroy() {
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            OutputStream out = response.getOutputStream();
            System.out.println("\n\nAsterdroidServlet: DEVO SPEDIRE I DATI");
            // SET THE MIME TYPE
            //response.setContentType("application/vnd.ms-excel");

            // set content dispostion to attachment in
            // case the open/save dialog needs to appear
            //response.setHeader("Content-disposition", "inline; filename=sample");

            byte[] wb = this.generateResult(request, response);
            //wb.write(out);
		out.write(wb);
            // workBook.write(out);
            out.flush();
            out.close();
            System.out.println("\n\n SINSSNTExcelServlet FINE DEVO SPEDIRE I DATI");
        } catch (FileNotFoundException fne) {
            System.out.println("File not found...");
        } catch (IOException ioe) {
            System.out.println("IOException..." + ioe.toString());
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        processRequest(request, response);
    }

    public String getServletInfo() {
        return "Example to create a workbook in a servlet using HSSF";
    }

    private byte[] generateResult(HttpServletRequest req, HttpServletResponse res)
            throws ServletException {
        /* parametro ritornato */
        byte[] objres = null;

        Hashtable arght = new Hashtable();
        String keyName = ""; // EJB Jndi name
        String methodName = ""; // EJB Method name
        String user = "";
        String password = "";
        String msg = null; // di servizio per messaggistica

        // recupera i parametri di attivazione del servlet
        Vector argv = new Vector();
        argv.setSize(3);

        keyName = "" + req.getParameter("EJB");
        methodName = "" + req.getParameter("METHOD");
        user = "" + req.getParameter("USER");
        password = "" + req.getParameter("ID509");

        argv.setElementAt(user, 0);
        argv.setElementAt(password, 1);

        /* Accumula gli altri parametri nella Hashtable */
        for (Enumeration e2 = req.getParameterNames(); e2.hasMoreElements();) {
            String k = (String) e2.nextElement();
            if (!k.equals("EJB") && !k.equals("METHOD") && !k.equals("USER") && !k.equals("WORD"))
                arght.put(k, req.getParameter(k).toString());
        }

        /* se uno dei 4 parametri sopra � null ritorna errore */

        /* verifica la presenza dei parametri opzionali */
        if (argv.elementAt(0) == null)
            argv.setElementAt("GUEST", 0);
        if (argv.elementAt(1) == null)
            argv.setElementAt("GUEST", 1);

        /* inserisce parametri */
        argv.setElementAt(arght, 2);

        System.out.println("AsterdroidServlet: " + keyName + "."+methodName+"() autorizzato");
        System.out.println("AsterdroidServlet: parametri = " + argv.toString());
            /* esegue collegamento e chiamata all'ejb che restituisce il pdf */
            try {
                objres = (byte[])util.invoke(keyName, methodName, argv);
                if(keyName.equals("ASTERDROIDQRC")){
                	res.setContentType("application/pdf");
                	res.setHeader("Content-Disposition","inline; filename=\"QRCode.pdf\"");//Mettendo il filename fra doppi apici, funziona anche in firefox!
                	res.setContentLength(objres.length);
                	res.setHeader("Cache-Control", "cache, must-revalidate");
                	res.setHeader("Pragma", "public");
                } 
            } catch (Exception ex) {
                System.out.println("AsterdroidServlet: " + ex);
                ex.printStackTrace();
                throw new ServletException(ex.getMessage());
            }
        

        return objres;
    }
}

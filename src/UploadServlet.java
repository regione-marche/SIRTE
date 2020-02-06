// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 13/11/2003 - SERVLET di upload documenti progetto INVA
//
// Ivan Venuti
// ==========================================================================

import javax.servlet.*;
import javax.servlet.http.*;
import java.net.*;
import java.io.*;
import java.util.*;
import it.pisa.caribel.gprs2.*;
import it.pisa.caribel.profile2.*;


public class UploadServlet extends HttpServlet {
static final int GPRS_TYPE = GprsElement.CLASS_TYPE;


//--------------------------------------------------------------------------
//	Legge una richiesta multipart dal client e memorizza su db i valori
//	letti, andando a salvare nella directory "dir" i file inviati.
//--------------------------------------------------------------------------
  String dirName=null;
  int prog_locale = 0;

  public void init(ServletConfig config) throws ServletException {
//    super.init(config);
	ServletContext sx = config.getServletContext();
	File dir=null;
	// Read the uploadWebDir from the servlet parameters
	dirName = (config.getServletContext()).getInitParameter("uploadWebDir");
	System.out.println("Parametro : "+dirName);
	if (dirName == null) {
		System.out.println("parametro uploadWebDir assente: "+
			"verra' impostato a /tmp/!");
      //throw new ServletException("Please supply uploadWebDir parameter");
	}
	if (dirName!=null) {
		dir = new File(dirName);
		if (! dir.isDirectory()) { 
		throw new ServletException("Supplied uploadWebDir " + dirName +
                                 " is invalid");
		}
	}
  }




  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
	System.out.println("Upload.doPost()");
	try{
		ClassLoader cl = this.getClass().getClassLoader();

		GprsElement elemEjb = new GprsElement( GPRS_TYPE,
			"GestoreEJB", "SINS_GESTORE", "GestoreHome");
		Vector params = new Vector();
		params.add( request.getParameter("SAAPROC") );
		params.add( dirName );
		params.add( request );
		params.add( response );
		System.out.println("Vettore upload: "+params);
		elemEjb.invokeMethod(cl, "uploadBis", params);
	}catch(Exception ee) {
		System.out.println("Errore eseguendo l'upload: "+ee);
		ee.printStackTrace();
	}
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
	System.out.println("Upload.doGet() NOT IMPLEMENTED");
  }


}


// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 05/12/2003 - SERVLET di connessione al profilo utente di SINS
//
//		Questa servlet serve solo a verificare se esiste un
//		certificato. Se esiste crea un nuovo record sulla
//		sessione e manda un cookie al client.
//
// Ivan Venuti
// ==========================================================================
import java.io.*;
import java.util.*;
import java.net.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.caribel.app.common.connection.GenericConnectionEJB;
import it.caribel.app.sinssnt.controllers.login.ManagerAccessoFromAster;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.gprs2.*;

public class SINSSNTIsasEnvServlet extends GPIsasServlet
{

	public void destroy() { super.destroy(); }



public void init(ServletConfig conf)throws ServletException {
//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
        try {
	        	ServletContext sc = conf.getServletContext();
	    		GenericConnectionEJB.appName = sc.getInitParameter("isas_kproc");
	    		if(GenericConnectionEJB.appName == null || GenericConnectionEJB.appName.equals("")){
	    			throw new Exception("Nome del ISAS_KPROC non configurato nel web.xml ");
	    		}
        		
        		inizializza(conf);
                setServletName("SINSSNTIsasEnvServlet");
        } catch(Exception ex) {
		ex.printStackTrace();
                throw new ServletException(ex.getMessage());
        }
/*CLASSTYPEbegin
        int t = GprsElement.CLASS_TYPE;
CLASSTYPEend*/
/*EJBTYPEbegin*/
        //int t = GprsElement.EJB_TYPE;
        int t = GprsElement.CLASS_TYPE;
/*EJBTYPEend*/

	//util.export(t, "SINSISASENV", "IsasEnvEJB", "SINSISASENV",
	//	"SINSIsasEnvHome");
	//la procedura Sins utilizza il controllo per la muta esclusione degli accessi: passa true a IsasEnv
	util.exportIsasEnv(t, "SINSSNTISASENV", SINSSNTGprsServlet.pkg_common+"IsasEnvEJB", "SINSSNTISASENV", 
		"IsasEnvHome", conf.getServletContext(), true);
}


public void doPost(HttpServletRequest req, HttpServletResponse resp)
throws ServletException, IOException {
	
 // Open the I/O streams
 ObjectInputStream in=new ObjectInputStream(req.getInputStream());
 ObjectOutputStream out=new ObjectOutputStream( resp.getOutputStream());

 try{
	//boolean fatto = processaMetodiGet(req, "SINSISASENV");
	//if (!fatto)
	boolean fatto = processaMetodiPost(in, out, "SINSSNTISASENV");
	if (!fatto) {
		// Da gestire metodi non standard
		//...
	}
 }catch(Exception ee) {
	System.out.println("Errore in doPost(): "+ee);
	ee.printStackTrace();
 } finally {
  try{
	// Close the I/O streams
	in.close();
	out.close();
  }catch(Exception dc) {
	System.out.println("Errore cercando di chiudere gli stream: "+dc);
  }
 }
        
}

private void outputFile(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	try{
		super.readFileIntoMemory(pathConf);
	}catch(Exception e) {
		e.printStackTrace();
		LOG.error("Errore leggendo il file sotto '"+pathConf+"' : "+e);
	}
	String [] cred =  getCredential(req);
	if (cred != null){
		myLogin mylogin = new myLogin();
		mylogin.put(cred[0], cred[1]);
		req.getSession().setAttribute(ManagerAccessoFromAster.MY_LOGIN, mylogin);
	}
	String context_path = this.getServletContext().getContextPath();
	resp.sendRedirect(context_path+"/login.zul");
}

protected String[] getCredential( HttpServletRequest req) {
	String cid = (String) req.getParameter("cid");
	String[] c = null;
	LOG.debug("SINSSNTIsasEnvServlet.getCredential(): cid="+cid);
	//-------------------------------------------
	if (cid!=null){
		try{
			Vector<Object> para = new Vector<Object>();
			para.add(cid);
			para.add(params4IdFactory);
			c = (String[]) util.invoke("SINSSNTISASENV","lookupWithCidVector", para);
			return c;
		}catch(Exception ec){
			LOG.error("SINSSNTIsasEnvServlet.getCredential(): errore "+ec);
		}
	}
	return c;
}


public void doGet(HttpServletRequest req, HttpServletResponse resp)
	 throws ServletException, IOException {
//--------------------------------------------------------------------------
//--------------------------------------------------------------------------
 //System.out.println("Entrato nella doGet!!!!");
 outputFile(req, resp);
 //System.out.println("Esce dalla doGet!!!");
}


}

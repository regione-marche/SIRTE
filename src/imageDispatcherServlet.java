// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// Created on 21-giu-2004
//
// ultima modifica Sara 08/06/06
// ==========================================================================
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.*;
import javax.servlet.http.*;

/*import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.gprs3.*;
import com.oreilly.servlet.multipart.*;*/

//import logs.*;
/*
 */

public class imageDispatcherServlet extends HttpServlet //GPRouterServlet
{
    
    
    /** Holds value of property urlWebApp. */
    private String urlWebApp;
    
    /**
     * Constructor of the object.
     */
    
    public imageDispatcherServlet()
    {
        super();
    }
    
    /**
     * Destruction of the servlet. <br>
     */
    
    public void destroy()
    {
        super.destroy(); // Just puts "destroy" string in log
    }
    
    /**
     *
     * The doDelete method of the servlet. <br>
     * This method is called when a HTTP delete request is received.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    
    public void doDelete(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException
    {
        
        // Put your code here
        
    }
    
    /**
     * The doGet method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to get.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
    {
        doPost(request, response);
    }
    
    /**
     * The doPost method of the servlet. <br>
     *
     * This method is called when a form has its tag value method equals to post.
     *
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    
    public void doPost(HttpServletRequest request,HttpServletResponse response)
    throws ServletException, IOException
    {
      

        
        System.out.println("imageDispacherServlet.doPost");
        try
        {
            
            //String imgdir = "/home/tomcat/jakarta-tomcat-4.0.4/webapps";
            String imgdir = this.getUrlWebApp();
            System.out.println("imgdir da file di config "+imgdir);
            String uri = request.getRequestURI();
            System.out.println("uri "+uri);
            String contextPath = request.getContextPath();
            System.out.println("contextPath "+contextPath);
            
            //recupero il nome della risorsa per sottrazione dall'uri.
            
                          
            uri = uri.substring(contextPath.length()+1);
            System.out.println("uri 0 = "+uri);
            String filenameOrig = uri.substring(uri.lastIndexOf("/")+1);
            //System.out.println("uri 1 = "+uri);

            
            System.out.println("imageDispacherServlet.doPost filenameOrig  = "+filenameOrig);
                        
            ServletContext sc = this.getServletContext();
                       
            String mimeType = sc.getMimeType(filenameOrig);
            
            if (mimeType == null)
            {
                sc.log("Could not get MIME type of "+filenameOrig);
                System.out.println("Could not get MIME type of "+filenameOrig);
                //response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                //return;
		mimeType = "application/octet-stream";
            }
            
            
            String filename = this.replace(imgdir+filenameOrig,"/",""+File.separatorChar);
            System.out.println("imageDispacherServlet.doPost filename dopo la sostituzione = "+filename);
            
            // individuo la dimensione del file
            File file = new File(filename);
            System.out.println("Il file esiste? "+file.exists());
            System.out.println("Il file è nulllo ? "+(file==null));
            System.out.println("Il file halunghezza = "+file.length());
            
            if(!file.exists() || file==null || file.length()==0)
            {
                System.out.println("Il file non esiste o è nullo o ha lunghezza =0");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST );
                return;
                
            }
            
            System.out.println("imageDispacherServlet.doPost lunghezza del file = "+file.length());
            
            response.setContentLength((int) file.length());
	    response.setContentType(mimeType);
	    response.setHeader("Content-disposition", "attachment;filename="+filenameOrig);
            FileInputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();
            
            // copio il contenuto del file nel'output
            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = in.read(buf)) >= 0)
            {
                out.write(buf, 0, count);
            }
            
            in.close();
            
            out.close();
        }
        catch(Exception e)
        {
            System.out.println("imageDispacherServlet.doPost "+e.toString());
            e.printStackTrace();
            
        }
    }
    
     
    public void init() throws ServletException
    {
        
        System.out.println("imageDispacherServlet.init ");
        
        String urlParam = this.getServletContext().getInitParameter("outputFileDirectoryPath");
        //String path = this.getServletContext().getRealPath("/");

        //System.out.println("path nell'urlParam "+urlParam);
        System.out.println("path nell'urlParam "+urlParam);
        this.setUrlWebApp(urlParam);
        
    }
    
    /** Getter for property urlWebApp.
     * @return Value of property urlWebApp.
     */
    public String getUrlWebApp()
    {
        return this.urlWebApp;
    }
    
    /** Setter for property urlWebApp.
     * @param urlWebApp New value of property urlWebApp.
     */
    public void setUrlWebApp(String urlWebApp)
    {
        this.urlWebApp = urlWebApp;
    }
    
    /**
     * Sostituisce un carattere con una Stringa
     *
     * @param word la Stringa su cui operare
     * @param c la stringa da sostituire
     * @param s la Stringa da mettere al posto del carattere
     * @return String la Stringa dove ogni occorrenza di c diventa s
     *
     */
    public String replace( String word, String c, String s )
    {

        String ret = "" ;
        String ch ;

        if (word != null) word = word.trim() ;

        if ( word == null || word.equals("") )
            return "" ;

        for ( int i = 0 ; i < word.length(); i++  )
        {

            //ch = word.charAt(i) ;
            ch = word.substring(i,i+1) ;

            //if ( ch == c )
            if( ch.equals(c) )
                ret += s ;
            else
                ret += ch ;

        }

        return ret  ;

    }

}

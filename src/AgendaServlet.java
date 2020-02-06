import it.caribel.app.common.ejb.OperatoriEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.zk.util.UtilForBinding;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.gprs2.GprsUtil;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.dateutility;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AgendaServlet extends HttpServlet {

	protected Log logger = LogFactory.getLog(this.getClass());

	private static final long serialVersionUID = 1L;
	
	public static String pkg = "it.caribel.app.sinssnt.bean.";
	public static String pkg_common ="it.caribel.app.common.ejb.";
	
	protected GprsUtil util = null;
	dateutility dt = new dateutility();

	String formato = "gg-mm-aaaa";
	private ClickTask task;

	private Timer timer;

    @SuppressWarnings("unchecked")
	public void init(ServletConfig conf) throws ServletException {
        if (util == null) {
            util = new GprsUtil(this.getClass().getClassLoader());
        }
//		int t = GprsElement.CLASS_TYPE;
//	util.export(t, "ASTERDROID", pkg+"AsterDroidEJB", "ASTERDROID", "AsterDroidHome");
//	util.export(t, "ASTERDROIDQRC", pkg+"AsterDroidQRCodeEJB60", "ASTERDROIDQRC", "AsterDroidQRCHome");
//	util.export(t, "ASTERDROIDINF", pkg+"AsterDroidInfEJB", "ASTERDROIDINF", "AsterDroidInfHome");
        
        //Verifico se è venerdì:
        //se è venerdì carico l'agenda per tutti gli operatori per la 
        //QUINTA settimana a partire da oggi
		Calendar c = Calendar.getInstance(); 
		Calendar startSc = Calendar.getInstance();
		c.setTime(new Date()); 
//		c.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		if(c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY){
			startSc.setTime(c.getTime());
			c.set(Calendar.DAY_OF_WEEK, c.MONDAY);
			c.add(Calendar.WEEK_OF_YEAR, 4);	
			timer= new Timer();
			startSc.set(Calendar.HOUR, 20);
			startSc.set(Calendar.MINUTE, 15);
		    timer.schedule(new ClickTask(c), startSc.getTime());			
//			caricaAgendaTra(c, 7);
		}
    }

	class ClickTask extends java.util.TimerTask
	{
		private Calendar dataDaPianificare;

		public ClickTask(Calendar c){
			this.dataDaPianificare=c;
		}
		
		public void run()
		{
			caricaAgendaTra(dataDaPianificare, 7);
		}
	}
	protected void caricaAgendaTra(Calendar startDate, int numeroDiGiorni) {
		try {
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			myLogin mylogin = new myLogin();
			mylogin.put("GUEST", "GUEST");
			
			//carico tutti gli operatori dell'applicativo
			OperatoriEJB operatoriEJB = new OperatoriEJB();
			Vector<ISASRecord> operatori = operatoriEJB.query(mylogin, h);
		
			Vector v =new Vector();
			v.addElement(dt.getData(UtilForBinding.getValueForIsas(startDate.getTime()),formato));
			for(int i=1;i<7;i++)
			{
				startDate.add(startDate.DATE,1);
				v.addElement(dt.getData(UtilForBinding.getValueForIsas(startDate.getTime()),formato));
			}
			logger.info("giornisettimana="+v.toString());
			Vector g = new Vector();
			g.add(v);
			h.put("giorni", g);
			
			String codiceOperatore = "";
			String tipoOperatore = "";
			AgendaEJB agendaEJB = new AgendaEJB();
			long starTime = 0;
			logger.info("Caricamento delle agende per gli operatori nella settimana " + v);
			for (ISASRecord operatore : operatori) {
				codiceOperatore = ISASUtil.getValoreStringa(operatore, "codice");
				tipoOperatore = ISASUtil.getValoreStringa(operatore, "tipo");
				if(!(codiceOperatore.isEmpty()||tipoOperatore.isEmpty())){
					h.put("referente", codiceOperatore);
					h.put("tipo_operatore", tipoOperatore);
					starTime = System.nanoTime();
					agendaEJB.carica_agenda(mylogin, h);
					logger.trace("Caricata l'agenda per l'operatore:"+ codiceOperatore + " di tipo "+ tipoOperatore + " in:"+ (System.nanoTime() - starTime) + " millisecondi.");
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DBRecordChangedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public void destroy() {
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        try {
            OutputStream out = response.getOutputStream();
            logger.info("\n\nAgendaServlet: RICHIESTA CARICAMENTO");
            
            // recupera i parametri di attivazione del servlet
            String data = "" + request.getParameter("data");
            String numeroDiGiorni = "" + request.getParameter("numeroDiGiorni");
            
            long starTime = System.nanoTime();
            caricaAgendaTra(dt.getGreg(data), Integer.parseInt(numeroDiGiorni));
            String resp = "Caricata l'agenda per gli operatori in:"+ (System.nanoTime() - starTime) + " millisecondi.";
            
//            byte[] wb = this.generateResult(request, response);
            //wb.write(out);
            out.write(resp.getBytes());
            out.flush();
            out.close();
            logger.info("\n\n nAgendaServlet FINE RICHIESTA CARICAMENTO ");
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

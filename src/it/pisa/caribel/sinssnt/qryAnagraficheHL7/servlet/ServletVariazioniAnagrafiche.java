package it.pisa.caribel.sinssnt.qryAnagraficheHL7.servlet;

import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.pisa.caribel.profile2.myLogin;
import it.insielmercato.anagraficaHL7.Sa4Hl7ResponseParser;
import it.insielmercato.anagraficaHL7.model.PatientResponse;

import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ServletVariazioniAnagrafiche extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3795336657394427981L;
	private Log LOG = LogFactory.getLog(getClass());
	
	private String SOAP_SUCCESS = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
			+ "  <soapenv:Body>\n  </soapenv:Body>\n" + "</soapenv:Envelope>\n";
	
	private String SOAP_FAULT = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
			+ "  <soapenv:Body>\n <Fault> FAULT_MSG </Fault>\n </soapenv:Body>\n" + "</soapenv:Envelope>\n";
	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String response = SOAP_SUCCESS;
		Sa4Hl7ResponseParser rP = new Sa4Hl7ResponseParser();
		CartellaEJB cartellaEJB = new CartellaEJB();
		myLogin mylogin = new myLogin();
		String codUsl = "";
		try {
			Hashtable<String, Object> hret = rP.parseMessage(req);	
			PatientResponse pR = (PatientResponse) hret.get("PatientResponse");
			String user = pR.getUsername();
			String password = pR.getPassword();
			mylogin.put(user, password);
			codUsl = pR.getQryResponse().get(0).getAssistito().getCodiceUsl();
			cartellaEJB.gestioneAggiornamentiAnagrafici(mylogin, hret);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.error("Errore gestione aggiornamenti anagrafici", e);
			response = SOAP_FAULT.replaceAll("FAULT_MSG", e.getMessage());			
		} 
		LOG.info("Aggiornamenti anagrafici per codUsl = " + codUsl);
		resp.setContentType("text/xml");
		resp.setCharacterEncoding("UTF-8");
		Writer w = resp.getWriter();
		w.write(response);

	}	

}

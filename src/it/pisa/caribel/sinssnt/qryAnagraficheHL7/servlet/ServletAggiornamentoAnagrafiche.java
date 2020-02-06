package it.pisa.caribel.sinssnt.qryAnagraficheHL7.servlet;

import it.caribel.app.sinssnt.bean.CartellaEJB;
import it.pisa.caribel.dbinterf2.DBMissingDriverException;
import it.pisa.caribel.dbinterf2.DBMissingNameException;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASEnv;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUserUnknowException;
import it.pisa.caribel.isas2.ISASWrongPasswordException;
import it.pisa.caribel.profile2.myLogin;
import it.insielmercato.anagraficaHL7.QryAnagraficaHL7;
import it.insielmercato.anagraficaHL7.model.PatientResponse;
import it.insielmercato.anagraficaHL7.model.QueryFilter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * elisa b 24/01/16
 * @author Administrator
 *
 */
public class ServletAggiornamentoAnagrafiche extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3795336657394427981L;
	private Log LOG = LogFactory.getLog(getClass());
	
	private String SOAP_SUCCESS = "OK";
	private String SOAP_FAULT = "KO";
	private Properties prop;
	private myLogin mylogin = new myLogin();
	

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String response = SOAP_SUCCESS;
		ISASConnection dbc = null;
		try {
			loadProperties("/home/caribel/applicazioni/sinssnt/qryAnagrafiche/aggDatiAnagrafici.properties");

			dbc = apriConnessione();
	        if (dbc == null) {
	        	LOG.error("AggiornamentoDatiAnagraficiARCA.main(): exit -1");
	            System.exit(-1);
	        }
	        aggiornaPosizioni(dbc);
		} catch (Throwable e) {
			e.printStackTrace();
			LOG.error("Errore gestione aggiornamenti anagrafici", e);
			response = SOAP_FAULT.replaceAll("FAULT_MSG", e.getMessage());			
		} finally{
			try {
				dbc.close();
			}catch (Exception e){
				LOG.error("Errore chiusura connessionein gestione aggiornamenti anagrafici", e);
			}
		}		
		resp.setContentType("text/xml");
		resp.setCharacterEncoding("UTF-8");
		Writer w = resp.getWriter();
		w.write(response);

	}	

	/**
	 * Metodo che consente di effetttuare la ricerca su ARCA di alcune posizioni
	 * (selezionate tramite query) in modo da aggiornare i relativi dati anagrafici
	 * @param dbc
	 */
	private void aggiornaPosizioni(ISASConnection dbc){
		ISASCursor dbcur = null;
		CartellaEJB cartellaEJB = new CartellaEJB();
		QueryFilter qFilter = null;
		QryAnagraficaHL7 qHL7 = null;

		try {
			
			String sel = getSelect(dbc);
			LOG.debug("aggiornaPosizioni = " + sel);
			dbcur = dbc.startCursor(sel);
			while(dbcur.next()){ 
				qFilter = new QueryFilter();
				qHL7 = new QryAnagraficaHL7();				
				ISASRecord dbr = dbcur.getRecord();
				String idArca = (String)dbr.get("cod_usl");
				LOG.info("Allinemento anagrafica per codUsl = " + idArca);
				qFilter.setIdRegionale(idArca);
				PatientResponse pR = (PatientResponse) qHL7.cercaAnagrafica(qFilter);
				if(pR.getAssRestituiti() == 0)
					continue;
				Hashtable<String, Object> hret = new Hashtable<String, Object>();
				hret.put("PatientResponse", pR);
				cartellaEJB.gestioneAggiornamentiAnagrafici(mylogin, hret);				
			}
		} catch (Throwable e) {
			LOG.error("Errore in aggiornaPosizioni", e);
			e.printStackTrace();
		}finally{
			try {
				dbcur.close();
			} catch (Throwable e) {
				LOG.error("Errore in aggiornaPosizioni dbcur.close", e);
				e.printStackTrace();
			}
		}
	}
		
	private String getSelect(ISASConnection dbc){

		String critDtVariazione = "";
		String joinAnag = "";		
		StringBuffer sb = new StringBuffer(" WHERE data_chiusura IS NULL");
		
		if (inProperties("data_apertura_i")){
			sb.append(" AND data_apertura >= " + dbc.formatDbDate((String)prop.get("data_apertura_i")));
		}
		if (inProperties("data_apertura_f")){
			sb.append(" AND data_apertura <= " + dbc.formatDbDate((String)prop.get("data_apertura_f")));
		}
		if (inProperties("data_chiusura_i")){
			sb.append(" AND data_chiusura >= " + dbc.formatDbDate((String)prop.get("data_chiusura_i")));
		}
		if (inProperties("data_chiusura_f")){
			sb.append(" AND data_chiusura <= " + dbc.formatDbDate((String)prop.get("data_chiusura_f")));
		}
		
		if (inProperties("lastcng_i")){
			sb.append(" AND jdbinterf_lastcng >= " + dbc.formatDbDate((String)prop.get("lastcng_f")));
		}
		if (inProperties("lastcng_f")){
			sb.append(" AND jdbinterf_lastcng <= " + dbc.formatDbDate((String)prop.get("lastcng_f")));
		}

		if (inProperties("data_variazione_i") || inProperties("data_variazione_f")){
			if(inProperties("data_variazione_i"))
				critDtVariazione += " AND a.data_variazione >= " + dbc.formatDbDate((String)prop.get("data_variazione_i"));
			if(inProperties("data_variazione_f"))
				critDtVariazione += " AND a.data_variazione <= " + dbc.formatDbDate((String)prop.get("data_variazione_f"));
	
			joinAnag = ", anagra_c a";
			sb.append(" AND a.n_cartella = c.n_cartella");
			sb.append(" AND a.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
							" FROM anagra_c" +
							" WHERE anagra_c.n_cartella = a.n_cartella" +
							critDtVariazione +
						")");
		}
		
		if(inProperties("condizione")){
			sb.append(" " + prop.get("condizione"));
		}
		
		String sel = "SELECT c.* FROM cartella c" +
				joinAnag +
				sb.toString();
		
		return sel;
	}
	
	private boolean inProperties(String chiave){
		String p = (String)prop.getProperty(chiave);
		return(p != null && p.length()>0);
	}
	
	private void loadProperties(String filepath) throws FileNotFoundException, IOException{
	   	prop = new Properties();
    	prop.load(new FileInputStream(filepath));	

	}
	

	public ISASConnection apriConnessione() throws DBMissingDriverException,
			DBMissingNameException, DBSQLException, ISASWrongPasswordException, ISASUserUnknowException,
			ISASMisuseException, DBMisuseException {
		ISASConnection isasc = null;
		String username = (String) prop.get("username");
		String password = (String) prop.get("password");
		mylogin.put(username, password);


		ISASEnv isasenv = new ISASEnv();
		isasenv.init((String) prop.get("dbdriver"), (String) prop.get("dburl"),
				(String) prop.get("dbtype"), new Integer(0), false);
		isasc = isasenv.getProcConnection((String) prop.get("procname"), 
				username, password);
		isasenv.close();
		return isasc;

	}

}

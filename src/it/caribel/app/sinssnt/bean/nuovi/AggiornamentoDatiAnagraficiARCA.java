package it.caribel.app.sinssnt.bean.nuovi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class AggiornamentoDatiAnagraficiARCA {
	private final static Log LOG = LogFactory.getLog(AggiornamentoDatiAnagraficiARCA.class);
	
	//private static Properties prop;

	
	public static void main(String[] args) throws Exception {
		String urlServlet = args[0];
		//String urlServlet = getProperties("\\home\\caribel\\applicazioni\\sinssnt\\qryAnagrafiche\\aggDatiAnagrafici.properties");

		// Send the request
		URL url = new URL(urlServlet);
		URLConnection conn;
		conn = url.openConnection();
	
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "text/xml");
		OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

		// write parameters
		writer.write("");
		writer.flush();
		LOG.info("Chiamata ServletAggiornamentoAnagrafiche");		

		// Get the response
		StringBuffer answer = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null)
			answer.append(line);

		writer.close();
		reader.close();

		// Output the response
		String response = answer.toString();
		LOG.info("ServletAggiornamentoAnagrafiche " + response);		
	}

/*
	private static String getProperties(String filepath) throws Exception {
    	prop = new Properties();
    	prop.load(new FileInputStream(filepath));	

		return prop.getProperty("url");
	}*/

}

package it.caribel.app.sinssnt.bean.nuovi;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 03/11/2014 - EJB di stampa della Lista Attività
//
// Mariarita Minerba
//
// ==========================================================================

import it.caribel.app.sinssnt.controllers.lista_attivita.ListaAttivitaGridCtrl;
import it.caribel.app.sinssnt.controllers.lista_attivita.ListaAttivitaGridItemRenderer;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.Vector;
// fo merge

public class FoListaAttivitaEJB extends SINSSNTConnectionEJB {

private String ver = "1-FoListaAttivitaEJB ";
public FoListaAttivitaEJB() {}

private void preparaLayout(mergeDocument md, ISASConnection dbc, Hashtable par) {
	Hashtable htxt = new Hashtable();
	try {
		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ragione_sociale'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
		
		String dadata 			= ""+par.get("dadata");
		String adata 			= ""+par.get("adata");
		String tipo_fonte		= ""+par.get("tipo_fonte");		
		String rich_perso		= ""+par.get("rich_perso");
		String rich_altri		= ""+par.get("rich_altri");
		if (dadata!=null && !dadata.equals(""))
			htxt.put("#dadata#", "Dal" + " " + getStringDate(par, "dadata"));
		else 
			htxt.put("#dadata#", "" );
		if (adata!=null && !adata.equals(""))
			htxt.put("#adata#", "Al" + " " + getStringDate(par, "adata"));
		else 
			htxt.put("#adata#", "" );
		
		if (tipo_fonte.equals("1,2"))
			htxt.put("#attivita#", "Tutte le attività");
		else if (tipo_fonte.equals("1") && !tipo_fonte.equals("2"))
			htxt.put("#attivita#", "Richieste MMG");
		else if (!tipo_fonte.equals("1") && tipo_fonte.equals("2"))
			htxt.put("#attivita#", "Prese carico CD");
		else 
			htxt.put("#attivita#","");
		
		if (rich_perso.equals("true") && rich_altri.equals("true"))
			htxt.put("#destinatari#", "Destinate a tutte");
		else if (rich_perso.equals("true") && rich_altri.equals("false"))
			htxt.put("#destinatari#", "Destinate a me");
		else if (rich_perso.equals("false") && rich_altri.equals("true"))
			htxt.put("#destinatari#", "Destinate ad altri");
		else
			htxt.put("#destinatari#", "");
		
		
	} catch (Exception ex) {
		htxt.put("#txt#", "ragione_sociale");
	}
        ServerUtility su =new ServerUtility();
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	md.writeSostituisci("layout",htxt);
}

private void preparaBody(mergeDocument md, Vector vdbr,ISASConnection dbc,Hashtable par) {
	md.write("iniziotab");
	String fonte,fonteDettaglio; 
//	= ""+dbr.get("fonte");
//	String fonteDettaglio 		= dbr.get("fonte_dettaglio")+"";
	try {
		ListaAttivitaGridItemRenderer listaAttivitaGridItemRenderer = new ListaAttivitaGridItemRenderer();
		Enumeration en=vdbr.elements();
		while (en.hasMoreElements()){
			Hashtable p = new Hashtable();
			ISASRecord dbr = (ISASRecord)en.nextElement();
			//p.put("#data_evento#",(java.sql.Date)dbr.get("data_richiesta"));
			p.put("#data_evento#",getDateField(dbr, "data_richiesta"));
			fonte = ISASUtil.getValoreStringa(dbr, "fonte");
			fonteDettaglio = ISASUtil.getValoreStringa(dbr, "fonte_dettaglio");

			String tipoEvento = listaAttivitaGridItemRenderer.recuperaDescrizioneFonte(fonte, fonteDettaglio);
			p.put("#tipo_evento#", tipoEvento);
			
			p.put("#assistito#", ISASUtil.getValoreStringa(dbr,"n_cartella")+ "-" + ISASUtil.getValoreStringa(dbr, "cognome") + " " + 
					ISASUtil.getValoreStringa(dbr,"nome"));
			p.put("#medico#", ISASUtil.getValoreStringa(dbr,"cod_med_descr"));
            p.put("#op_dest#", ISASUtil.getValoreStringa(dbr,"cod_operatore_descr"));
            p.put("#zona#", ISASUtil.getValoreStringa(dbr, "cod_zona_descr"));
            p.put("#distretto#", ISASUtil.getValoreStringa(dbr, "cod_distretti_descr"));
             
			md.writeSostituisci("tabella", p);
		}			
			
		
	} catch (Exception ex) {
		
		Hashtable p = new Hashtable();
                p.put("#data_evento#","");
                p.put("#tipo_evento#", "");
                p.put("#assistito#", "");
                p.put("#medico#", "");
                p.put("#op_dest#", "");
                p.put("#distretto#", "*** errore in lettura: "+ex+" ***");
                p.put("#zona#", "");
		md.writeSostituisci("tabella", p);
		ex.printStackTrace();
	}
	md.write("finetab");
}

public byte[] query_attivita(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {
	String punto = ver  + "query_attivita ";

	//System.out.println("FoListaAttivitaEJB.query_attivita(): DEBUG inizio...");
	boolean done=false;
	LOG.info(punto + " dati che ricevo>" +par);
	ISASConnection dbc=null;
    ServerUtility su =new ServerUtility();

	try {
                myLogin lg = new myLogin();
                lg.put(utente,passwd);
                dbc = super.logIn(lg);
                ListaAttivitaEJB listaEJB = new ListaAttivitaEJB();
                
                recuperaListaFonte(par);
                
        		Vector vdbr = listaEJB.query(lg, par, ListaAttivitaEJB.CTS_QUERY_ALL_DATI);        		
        		if (vdbr == null || vdbr.size()==0) {
        			preparaLayout(eve, dbc,par);
        			eve.write("messaggio");
        			eve.write("finale");
        		} else	{			
        			preparaLayout(eve, dbc,par);
        			preparaBody(eve, vdbr,dbc,par);        			
        			eve.write("finale");
			}	// fine if dbcur.getDimension()
		
        

		eve.close();

		//System.out.println("FoListaAttivitaEJB.query_attivita(): DEBUG "+
		//	"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("FoListaAttivitaEJB.query_attivita(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				e1.printStackTrace();
				System.out.println("FoListaAttivitaEJB.query_attivita(): "+e1);
			}
		}
	}
}	// End of query_attivita() method
private void recuperaListaFonte(Hashtable par) {
	String punto = ver + "recuperaListaFonte ";
	LinkedList<String> listaFonte = new LinkedList<String>();
	
	String listFonte = ISASUtil.getValoreStringa(par, CostantiSinssntW.CTS_ELEMENTI_FONTI_LISTA);
	if (ISASUtil.valida(listFonte)){
		StringTokenizer codListaFonte = new StringTokenizer(listFonte, ListaAttivitaGridCtrl.CTS_SEPARATORE_STAMPA);
		while (codListaFonte.hasMoreTokens()) {
			String cod = (String) codListaFonte.nextElement();
			if (ISASUtil.valida(cod)){
				listaFonte.add(cod);
			}
		}
	}
	LOG.trace(punto + " listarecuperata>>" +listaFonte);
	par.put(CostantiSinssntW.CTS_ELEMENTI_FONTI_LISTA, listaFonte);
}

private String getDateField(ISASRecord dbr, String f) {
	try {
		if (dbr.get(f) == null)
			return "";
		String d = ((java.sql.Date) dbr.get(f)).toString();
		d = d.substring(8, 10) + "/" + d.substring(5, 7) + "/"
				+ d.substring(0, 4);
		return d;
	} catch (Exception e) {
		debugMessage("getStringField(" + dbr + ", " + f + "): " + e);
		return "";
	}	
	
}
private String getStringDate(Hashtable par, String k) {
	try {
		String s = (String) par.get(k);
		s = s.substring(8, 10) + "/" + s.substring(5, 7) + "/"
				+ s.substring(0, 4);
		return s;
	} catch (Exception e) {
		debugMessage("getStringDate(" + par + ", " + k + "): " + e);
		return "";
	}
}
}	// End of FoListaAttivita class

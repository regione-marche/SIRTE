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

import it.caribel.app.common.ejb.PresidiEJB;
import it.caribel.app.sinssnt.controllers.lista_assistiti.ListaAssistitiGridCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerDate;
import it.caribel.app.sinssnt.util.ManagerDecod;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
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

import org.zkoss.util.resource.Labels;

public class FoListaAssistitiEJB extends SINSSNTConnectionEJB {

private String ver = "1-FoListaAssistitiEJB ";
public static final int CTS_QUERY_ALL_DATI    = 2;
public FoListaAssistitiEJB() {}

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
		
		htxt.put("#lbl_lista_assistiti#", Labels.getLabel("listaAssistitiGrid.formTitle"));
		
		if (dadata!=null && !dadata.equals(""))
			htxt.put("#dadata#", Labels.getLabel("listaAttivitaGrid.data.da") + " " + getStringDate(par, "dadata"));
		else 
			htxt.put("#dadata#", "" );
		if (adata!=null && !adata.equals(""))
			htxt.put("#adata#", Labels.getLabel("listaAttivitaGrid.data.a")  + " " + getStringDate(par, "adata"));
		else 
			htxt.put("#adata#", "" );
		
		if (tipo_fonte.equals("1, 2, 3"))   
			htxt.put("#attivita#",  Labels.getLabel("listaAssistitiGrid.attivita_num.5"));
		else if (tipo_fonte.equals("1"))
			htxt.put("#attivita#", Labels.getLabel("listaAssistitiGrid.attivita_num.1_1"));
		else if (tipo_fonte.equals("2"))
			htxt.put("#attivita#", Labels.getLabel("listaAssistitiGrid.attivita_num.2_2"));
		else if (tipo_fonte.equals("3"))
			htxt.put("#attivita#", Labels.getLabel("listaAssistitiGrid.attivita_num.3_3"));
		else if (tipo_fonte.equals("1, 2"))
			htxt.put("#attivita#", Labels.getLabel("listaAssistitiGrid.attivita_num6"));
		else if (tipo_fonte.equals("1, 3"))
			htxt.put("#attivita#",  Labels.getLabel("listaAssistitiGrid.attivita_num.7"));
		else if (tipo_fonte.equals("2, 3"))
			htxt.put("#attivita#",  Labels.getLabel("listaAssistitiGrid.attivita_num.8"));
		else 
			htxt.put("#attivita#","");
		
	
		
		
	} catch (Exception ex) {
		htxt.put("#txt#", "ragione_sociale");
	}
        ServerUtility su =new ServerUtility();
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	md.writeSostituisci("layout",htxt);
}

private void preparaBody(mergeDocument md, Vector vdbr,ISASConnection dbc,Hashtable par, boolean isSo) {
	Hashtable h = new Hashtable();
	h.put("#lbl_id_assistito#",Labels.getLabel("lista.assistiti.col.n_cartella"));
	h.put("#lbl_assistito#",Labels.getLabel("lista.assistiti.col.assistito"));
	h.put("#lbl_cognome#",Labels.getLabel("lista.assistiti.col.cognome"));
	h.put("#lbl_nome#",Labels.getLabel("lista.assistiti.col.nome"));
	h.put("#lbl_data_scheda#",Labels.getLabel("lista.assistiti.col.data_inizio"));
	h.put("#lbl_sede#",Labels.getLabel("lista.assistiti.col.operatore.sede.descr"));
	h.put("#lbl_case_manager#",Labels.getLabel("lista.assistiti.col.operatore.referente.descr"));
	h.put("#lbl_int_ass#",Labels.getLabel("lista.assistiti.col.int_assist"));
	h.put("#lbl_stato#",Labels.getLabel("lista.assistiti.col.skso_presente"));
	h.put("#lbl_data_attivi#",Labels.getLabel("lista.assistiti.col.dt_attiv"));
	h.put("#lbl_data_conclus#",Labels.getLabel("lista.assistiti.col.data_conclus"));
	h.put("#lbl_mot_conclus#",Labels.getLabel("lista.assistiti.col.mot_conclus"));
	h.put("#lbl_inizio_piano#",Labels.getLabel("lista.assistiti.col.data_piano_inizio"));
	h.put("#lbl_fine_piano#",Labels.getLabel("lista.assistiti.col.data_piano_fine"));
	h.put("#lbl_inizio_pror#",Labels.getLabel("lista.assistiti.col.proroga.inizio"));
	h.put("#lbl_fine_pror#",Labels.getLabel("lista.assistiti.col.proroga.fine"));
	h.put("#lbl_inizio_sosp#",Labels.getLabel("lista.assistiti.col.sospeso.ini"));
	h.put("#lbl_fine_sosp#",Labels.getLabel("lista.assistiti.col.sospeso.fin"));
	h.put("#lbl_mmg#",Labels.getLabel("lista.assistiti.col.mmg"));
	md.writeSostituisci("iniziotab", h);
	
	String codReg = ISASUtil.getValoreStringa(par, ManagerProfile.CODICE_REGIONE); 
	String codAzSan  = ISASUtil.getValoreStringa(par, ManagerProfile.CODICE_USL);
	RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
	try {
		for(int i = 0; i < vdbr.size(); i++){
			ISASRecord dbr = (ISASRecord) vdbr.get(i);
			Hashtable p = new Hashtable();
			p.put("#cartella#",dbr.get("n_cartella").toString());
			p.put("#cognome#",ISASUtil.getValoreStringa(dbr,"cognome"));
			p.put("#nome#",ISASUtil.getValoreStringa(dbr,"nome"));
			p.put("#assistito#",ISASUtil.getValoreStringa(dbr,"cognome")+ " " + ISASUtil.getValoreStringa(dbr,"nome"));
			p.put("#data_scheda#",getDateField(dbr, "data_inizio"));
			String codSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD);
			if (ISASUtil.valida(codSede)){
				p.put("#sede#",recuperaSede(dbc, codSede,codReg, codAzSan));
			}else p.put("#sede#", "");
			String operatoreReferente = ISASUtil.getValoreStringa(dbr, "operatore_referente");
			p.put("#manager#", ISASUtil.getDecode(dbc, "operatori", "codice",
					"" + operatoreReferente, "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",
					"cod_operatore_descr"));
			String cod_tipocura = dbr.get("tipocura").toString();
			p.put("#intensita#", ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val", CostantiSinssntW.TAB_VAL_TIPOCURA, dbr.get("tipocura"),"tab_descrizione"));
			String idSkso = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_ID_SKSO);
			String msgStatoScheda= Labels.getLabel(CostantiSinssntW.CTS_SKSO_STATO_NON_PRESENTE);
			if (ISASUtil.valida(idSkso) ){
				rmSkSOEJB.recuperaInfoScheda(dbc, dbr);
				if (dbr.getHashtable().containsKey(CostantiSinssntW.CTS_SKSO_STATO)){
					String statoScheda = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_SKSO_STATO);
					msgStatoScheda = Labels.getLabel(statoScheda);
				}
			}
			p.put("#stato#", msgStatoScheda);
			p.put("#data_attiv#",getDateField(dbr, "dt_attivazione"));
			p.put("#data_conclus#",getDateField(dbr, "data_conclusione"));
			p.put("#motivo_conclus#",ISASUtil.getValoreStringa(dbr,"motivo_conclusione"));
			p.put("#inizio_piano#",getDateField(dbr, "data_piano_inizio"));
			p.put("#fine_piano#",getDateField(dbr, "data_piano_fine"));
			String nCartella = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.N_CARTELLA);
			if (isSo && ISASUtil.valida(idSkso)){
				verificaProroghe(dbc, nCartella, idSkso, dbr, p);
				verificaSospensione(dbc, nCartella, idSkso, dbr,p);
			}
			String query_med = "SELECT ac.cod_med FROM anagra_c ac WHERE ac.n_cartella ="+ dbr.get("n_cartella")+
					" AND data_variazione IN (SELECT MAX("+
					"c.data_variazione) FROM anagra_c c"+
					" WHERE c.n_cartella=ac.n_cartella)";
			ISASRecord dbr_med = dbc.readRecord(query_med);
			if (dbr_med!=null){
				if (dbr_med.get("cod_med")!=null && !dbr_med.get("cod_med").equals("")){
					String cod_med_descr = ISASUtil.getDecode(dbc, "medici", "mecodi", "" + dbr_med.get("cod_med"),
							"nvl(trim(mecogn),'') ||' '  ||nvl(trim(menome),'')", "cod_med_descr");
				p.put("#mmg#", cod_med_descr);
				}else p.put("#mmg#", "");
			}else p.put("#mmg#", "");
			
			md.writeSostituisci("tabella", p);
		}
			
		/*Enumeration en=vdbr.elements();
		while (en.hasMoreElements()){
			Hashtable p = new Hashtable();
			ISASRecord dbr = (ISASRecord)en.nextElement();
			p.put("#cartella#",ISASUtil.getValoreStringa(dbr,"n_cartella"));
			p.put("#cognome#",ISASUtil.getValoreStringa(dbr,"cognome"));
			p.put("#nome#",ISASUtil.getValoreStringa(dbr,"nome"));
			p.put("#data_scheda#",getDateField(dbr, "data_inizio"));
			String codSede = ISASUtil.getValoreStringa(dbr, CostantiSinssntW.CTS_L_ATTIVITA_SEDE_COD);
			if (ISASUtil.valida(codSede)){
				p.put("#sede#",recuperaSede(dbc, codSede,codReg, codAzSan));
			}
			String operatoreReferente = ISASUtil.getValoreStringa(dbr, "operatore_referente");
			p.put("#manager#", ISASUtil.getDecode(dbc, "operatori", "codice",
					"" + operatoreReferente, "nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')",
					"cod_operatore_descr"));
			
			String cod_tipocura = dbr.get("tipocura").toString();
			Hashtable<String, String> tipoCuraDescrizione = ManagerDecod
					.caricaDaTabVoci(dbc, CostantiSinssntW.TAB_VAL_TIPOCURA);
			
			
			
			p.put("#assistito#", ISASUtil.getValoreStringa(dbr,"n_cartella")+ "-" + ISASUtil.getValoreStringa(dbr, "cognome") + " " + 
					ISASUtil.getValoreStringa(dbr,"nome"));
			p.put("#medico#", ISASUtil.getValoreStringa(dbr,"cod_med_descr"));
            p.put("#op_dest#", ISASUtil.getValoreStringa(dbr,"cod_operatore_descr"));
            p.put("#zona#", ISASUtil.getValoreStringa(dbr, "cod_zona_descr"));
            p.put("#distretto#", ISASUtil.getValoreStringa(dbr, "cod_distretti_descr"));
             
			md.writeSostituisci("tabella", p);
		}	*/		
			
		
	} catch (Exception ex) {
		
		Hashtable p = new Hashtable();
                p.put("#cartella#","");
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

public byte[] query_assistiti(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {
	String punto = ver  + "query_assistiti ";

	boolean done=false;
	LOG.info(punto + " dati che ricevo>" +par);
	ISASConnection dbc=null;
    ServerUtility su =new ServerUtility();

	try {
                myLogin lg = new myLogin();
                lg.put(utente,passwd);
                dbc = super.logIn(lg);
                ListaAssistitiEJB listaEJB = new ListaAssistitiEJB();
                
                
        		boolean isSo = ISASUtil.getvaloreBoolean(par, ManagerProfile.IS_SEGRETERIA_ORGANIZZATIVA);

        		Vector vdbr = listaEJB.recuperaDatiLista(dbc, par, false, true, false, isSo, CTS_QUERY_ALL_DATI);      
               		
                
        		if (vdbr == null || vdbr.size()==0) {
        			preparaLayout(eve, dbc,par);
        			eve.write("messaggio");
        			eve.write("finale");
        		} else	{			
        			preparaLayout(eve, dbc,par);
        			preparaBody(eve, vdbr,dbc,par,isSo);        			
        			eve.write("finale");
			}	// fine if dbcur.getDimension()
		
        

		eve.close();

		//System.out.println("FoListaAssistitiEJB.query_attivita(): DEBUG "+
		//	"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("FoListaAssistitiEJB.query_attivita(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				e1.printStackTrace();
				System.out.println("FoListaAssistitiEJB.query_attivita(): "+e1);
			}
		}
	}
}	// End of query_attivita() method


private Object recuperaSede(ISASConnection dbc, String codSede, String codReg, String codAzSan) {
	PresidiEJB presidio = new PresidiEJB();
	String descrizionePresidio = presidio.recuperaDescrizionePresidio(dbc, codSede, codReg, codAzSan);
	
	return descrizionePresidio;
}

private void verificaProroghe(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbr, Hashtable p) {
	String punto = ver + "verificaProroghe ";
	
	if (ISASUtil.valida(idSkso)) {
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		String query = rmSkSOEJB.recuperaQueryProroghe(nCartella, idSkso);
		try {
			ISASRecord dbrProroghe = dbc.readRecord(query);
			String dtProrogaInizio = ISASUtil.getValoreStringa(dbrProroghe, "dt_proroga_inizio");
			String dtProrogaFine = ISASUtil.getValoreStringa(dbrProroghe, "dt_proroga_fine"); 
			p.put("#inizio_proroga#", ManagerDate.formattaDataIta(dtProrogaInizio, "/"));
			p.put("#fine_proroga#", ManagerDate.formattaDataIta(dtProrogaFine, "/"));
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati>>" + query + "\n", e);
		}
	}else {
		LOG.trace(punto + "Non effettuo il salvataggio dei dati ");
	}
}

private void verificaSospensione(ISASConnection dbc, String nCartella, String idSkso, ISASRecord dbr, Hashtable p) {
	String punto = ver + "verificaSospensione ";
	if (ISASUtil.valida(idSkso)){
		RMSkSOEJB rmSkSOEJB = new RMSkSOEJB();
		String query = rmSkSOEJB.recuperaQuerySospensione(nCartella, idSkso);
		try {
			ISASRecord dbrSospensioni = dbc.readRecord(query);
			String dtSospensioneInizio = ISASUtil.getValoreStringa(dbrSospensioni, "dt_sospensione_inizio");
			String dtSospensioneFine = ISASUtil.getValoreStringa(dbrSospensioni, "dt_sospensione_fine");
			p.put("#inizio_sosp#", ManagerDate.formattaDataIta(dtSospensioneInizio, "/"));
			p.put("#fine_sosp#", ManagerDate.formattaDataIta(dtSospensioneFine, "/"));
		} catch (Exception e) {
			LOG.error(punto + " Errore nel recuperare i dati>>" + query + "\n", e);
		}
	}else {
		LOG.trace(punto + "Non effettuo il salvataggio dei dati ");
	}
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

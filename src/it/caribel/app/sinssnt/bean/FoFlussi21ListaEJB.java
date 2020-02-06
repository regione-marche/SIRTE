package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 17/05/04 - EJB di connessione alla procedura SINS Progetto salute mentale
//     scrittura nelle tabelle sm_consulenze
//
// mauro vannacci
// ==========================================================================

import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;

import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.DataWI;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.math.*;


public class FoFlussi21ListaEJB extends SINSSNTConnectionEJB {
	private static final String MIONOME = "FoFlussi21ListaEJB.";
	private boolean extrAreaVasta;
	private String distretto;
	private String area_vasta;


	public FoFlussi21ListaEJB() {}
	
	public byte[] query_flussi(String utente, String passwd, Hashtable h,
		    mergeDocument eve) throws SQLException
{
		boolean done = false;
		ISASConnection dbc = null;
		
		try {
			
			myLogin lg = new myLogin();
			lg.put(utente,passwd);
			dbc = super.logIn(lg);
			Hashtable h_xLayout = new Hashtable();		
			
			preparaLayoutAdi(eve,dbc,h);
			leggiEStampaAdi(h, eve, dbc);
			
			//leggiEStampaAdiMalati(h, eve, dbc);
			//leggiEStampaAdiAnziani(h, eve, dbc);
			leggiEStampaAccessiMmg(h, eve, dbc);
			leggiEStampaAccessiMmgTerm(h, eve, dbc);
			leggiEStampaAccessiMmgAnz(h, eve, dbc);
			leggiEStampaAccessiOp(h, eve, dbc);
			leggiEStampaAccessiOpTerm(h, eve, dbc);
			leggiEStampaAccessiOpAnz(h, eve, dbc);
			leggiEStampaAccessiOpFis(h, eve, dbc);
			leggiEStampaAccessiOpFisTerm(h, eve, dbc);
			leggiEStampaAccessiOpFisAnz(h, eve, dbc);
			leggiEStampaAccessiOpInf(h, eve, dbc);
			leggiEStampaAccessiOpInfTerm(h, eve, dbc);
			leggiEStampaAccessiOpInfAnz(h, eve, dbc);
			leggiEStampaAccessiOpAltri(h, eve, dbc);
			leggiEStampaAccessiOpAltriTerm(h, eve, dbc);
			leggiEStampaAccessiOpAltriAnz(h, eve, dbc);
			leggiEStampaADP(h, eve, dbc);
			leggiEStampaAutoADP(h, eve, dbc);
			
			eve.write("finale");

			eve.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get();	// restituisco il bytearray
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("FoFlussi21ListaEJB.query_report(): Eccezione= " + e);
			throw new SQLException("FoFlussi21ListaEJB.query_report(): " + e);
		}finally{
			if(!done){
				try{
					eve.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					e1.printStackTrace();
					System.out.println("FoFlussi21ListaEJB.query_report(): Eccezione nella chiusura della connessione= " + e1);
				}
			}
		}	
	}// END query_flussi


	private void preparaLayoutAdi(mergeDocument md, ISASConnection dbc,Hashtable par) throws Exception {
		Hashtable htxt = new Hashtable(); 
	        String data_inizio="01/01/"+par.get("anno");
			String data_fine="31/12/"+par.get("anno");
			htxt.put("#data_inizio#", data_inizio);
			htxt.put("#data_fine#", data_fine);
			ServerUtility su = new ServerUtility();
			htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	        md.writeSostituisci("layout_adi",htxt);
	}
	
private void preparaLayoutAdiMalati(mergeDocument md, ISASConnection dbc,Hashtable par) throws Exception {
		Hashtable htxt = new Hashtable();
		
	        String data_inizio="01/01/"+par.get("anno");
			String data_fine="31/12/"+par.get("anno");
			htxt.put("#data_inizio#", data_inizio);
			htxt.put("#data_fine#", data_fine);
	        md.writeSostituisci("layout_adi_mal",htxt);
	}
	


	private void stampaAdi(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_casi_adi#", faiNotNull(mydbr, "num_casi_adi", 'I'));
	    h_cons.put("#cod_zona#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAdi", (Hashtable)h_cons); 
		

	}// END stampaAdi
	

	
	private void leggiEStampaAdi(Hashtable h0, mergeDocument md,ISASConnection dbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		String suddivisione=(String)h0.get("tipo");
		this.area_vasta=(String)h0.get("area");
		this.distretto = (String)h0.get("distr");
		this.extrAreaVasta = (suddivisione != null && suddivisione.equals("A"));
		
		
		
		int totale = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "select SUM(ADI) as totale_adi, SUM(TERMINALE) as totale_adi_mal, SUM(ANZIANO) as totale_adi_anz,"
						+(extrAreaVasta?"'AREA VASTA "+area_vasta+"'":"'DISTRETTO "+distretto+"'")+" as zona from (SELECT 1 as ADI, decode(s.tipo_ute,"+CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE+",1,0) TERMINALE," +
						"decode(s.tipo_ute,"+CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO+",1,0) ANZIANO  from " +
								" SKSO_FLS21_CASI_TRATTATI s, anagra_c a,"+
						  " ubicazioni_n u " +
						  " where " +
						  " s.pr_data_puac between "+dbc.formatDbDate(data_inizio)+" and "+dbc.formatDbDate(data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta" +
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = s.n_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(dbc,data_fine)+
						  ")"+						  
						  ")";
						
		System.out.println("QUERY PC ADI ATTIVE NELL'ANNO" + myselect);

		
		ISASRecord dbr = dbc.readRecord(myselect);
		if (dbr == null) {
			md.write("messaggio");
			System.out.println("SCRITTO MESSAGGIO");
		}else{
			md.write("inizioTabAdi");			
		// scrivo totali adi
			stampaAdi(md, dbr, h0);
			md.write("fineTabAdi");
			h.put("#totale_adi#", faiNotNull(dbr, "totale_adi", 'I'));
			md.writeSostituisci("totaliAdi", (Hashtable)h); 
		  
		//scrivo di cui terminali
			md.write("inizioTabAdiMal"); 
			stampaAdiMalati(md, dbr, h0);
			md.write("fineTabAdiMal");
			h.put("#totale_adi_mal#", faiNotNull(dbr, "totale_adi_mal", 'I'));
			md.writeSostituisci("totaliAdiMal", (Hashtable)h); 
			
		//scrivo di cui anziani		
			md.write("inizioTabAdiAnz"); 
			stampaAdiAnziani(md, dbr, h0);		   
			md.write("fineTabAdiAnz");
			h.put("#totale_adi_anz#", faiNotNull(dbr, "totale_adi_anz", 'I'));			
			md.writeSostituisci("totaliAdiAnz", (Hashtable)h); 
		}
	}// END leggiEStampaAdi
	
	private void stampaAdiMalati(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_casi_adi_term#", faiNotNull(mydbr, "num_casi_adi_term", 'I'));
	    h_cons.put("#cod_zona_mal#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_mal#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAdiMal", (Hashtable)h_cons); 
		

	}// END stampaAdiMalati
	
	
	
//	private void leggiEStampaAdiMalati(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
//	{		
//	
//		String data_inizio="01/01/"+h0.get("anno");
//		String data_fine="31/12/"+h0.get("anno");
//		int totale = 0;
//		int totale_adi = 0;
//		Hashtable h = new Hashtable();
//		
//		String myselect = "SELECT COUNT(*) num_casi_adi_term, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+" "+
//						  " FROM skmmg_adi s,"+
//						  " anagra_c a,"+
//						  " ubicazioni_n u"+
//						  " WHERE s.skadi_data_inzio <= "+formatDate(mydbc,data_fine)+
//						  " AND ((s.skadi_data_fine IS NULL)"+ 
//						  " OR (s.skadi_data_fine >= "+formatDate(mydbc,data_inizio)+")"+ 
//						  ")"+
//						  " AND u.tipo = 'C'"+
//						  " AND u.codice = a.citta"+
//						  " AND a.n_cartella = s.n_cartella"+
//						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
//						  " WHERE a1.n_cartella = a.n_cartella"+
//						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
//						  ")"+
//						  " AND s.skadi_attiv1 = 'X'"+
//						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
//						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
//						
//		System.out.println("QUERY FLUSSI" + myselect);
//
//		ISASCursor dbcur=null;
//		
//		dbcur = mydbc.startCursor(myselect);	
//		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
//		if (!dentro) {
//			md.write("messaggio");			
//		}
//		md.write("inizioTabAdiMal"); 
//		ISASRecord dbr = null;
//		if (dbcur != null) {
//			
//			while(dbcur.next()){
//			dbr = dbcur.getRecord(); 			
//			totale = Integer.parseInt("" + dbr.get("num_casi_adi_term"));
//			totale_adi = totale + totale_adi;
//			dbr.put("totale_adi_mal", "" + totale_adi);
//			stampaAdiMalati(md, dbr, h0);
//			 
//		    }
//			md.write("fineTabAdiMal");
//			if (totale_adi==0)
//				h.put("#totale_adi_mal#", "0");				
//			else 
//				h.put("#totale_adi_mal#", faiNotNull(dbr, "totale_adi_mal", 'S'));
//			
//			md.writeSostituisci("totaliAdiMal", (Hashtable)h); 
//		    close_dbcur_nothrow("leggieStampaMal", dbcur);
//		   
//		   
//		}
//	}// END leggiEStampaAdiMal
	
	private void stampaAdiAnziani(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_casi_adi_anz#", faiNotNull(mydbr, "num_casi_adi_anz", 'I'));
	    h_cons.put("#cod_zona_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAdiAnz", (Hashtable)h_cons); 
		

	}// END stampaAdiAnziani
	
	
	
//	private void leggiEStampaAdiAnziani(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
//	{		
//	
//		String data_inizio="01/01/"+h0.get("anno");
//		String data_fine="31/12/"+h0.get("anno");
//		int totale = 0;
//		int totale_adi = 0;
//		Hashtable h = new Hashtable();
//		
//		String myselect = "SELECT COUNT(*) num_casi_adi_anz, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+" "+
//						  " FROM skmmg_adi s,"+
//						  " anagra_c a,"+
//						  " ubicazioni_n u, "+
//						  " cartella c "+
//						  " WHERE s.skadi_data_inzio <= "+formatDate(mydbc,data_fine)+
//						  " AND ((s.skadi_data_fine IS NULL)"+ 
//						  " OR (s.skadi_data_fine >= "+formatDate(mydbc,data_inizio)+")"+ 
//						  ")"+
//						  " AND u.tipo = 'C'"+
//						  " AND u.codice = a.citta"+
//						  " AND a.n_cartella = s.n_cartella"+
//						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
//						  " WHERE a1.n_cartella = a.n_cartella"+
//						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
//						  ")"+
//						  " AND s.skadi_attiv1 <> 'X'"+
//						  " AND c.n_cartella = s.n_cartella"+						  
//						  " AND (" + formatDate(mydbc,data_fine) + " - NUMTOYMINTERVAL(65, 'YEAR') >= c.data_nasc)" + 
//						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
//						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
//						
//		System.out.println("QUERY FLUSSI" + myselect);
//
//		ISASCursor dbcur=null;
//		
//		dbcur = mydbc.startCursor(myselect);	
//		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
//		if (!dentro) {
//			md.write("messaggio");			
//		}
//		md.write("inizioTabAdiAnz"); 
//		ISASRecord dbr = null;
//		if (dbcur != null) {
//			
//			while(dbcur.next()){
//			dbr = dbcur.getRecord(); 			
//			totale = Integer.parseInt("" + dbr.get("num_casi_adi_anz"));
//			totale_adi = totale + totale_adi;
//			dbr.put("totale_adi_anz", "" + totale_adi);
//			stampaAdiAnziani(md, dbr, h0);
//			 
//		    }
//			md.write("fineTabAdiAnz");
//			if (totale_adi==0)
//				h.put("#totale_adi_anz#", "0");				
//			else 
//				h.put("#totale_adi_anz#", faiNotNull(dbr, "totale_adi_anz", 'S'));
//			
//			md.writeSostituisci("totaliAdiAnz", (Hashtable)h); 
//		    close_dbcur_nothrow("leggieStampaAnziani", dbcur);
//		   
//		   
//		}
//	}// END leggiEStampaAdiAnziani
//	
	private void stampaAccessiMmg(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_mmg#", faiNotNull(mydbr, "num_acc_mmg", 'I'));
	    h_cons.put("#cod_zona_mmg#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_mmg#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiMed", (Hashtable)h_cons); 
		

	}// END stampaAccessiMmg
	
	
	
	private void leggiEStampaAccessiMmg(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_mmg, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+" "+
						  " FROM intmmg i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+  
						  " WHERE i.int_data >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data <= "+formatDate(mydbc,data_fine)+
						  " AND EXISTS (SELECT t.* FROM tabpipp t"+
						  " WHERE t.pipp_tipo = i.int_tipo_pres"+
						  " AND t.pipp_codi = i.int_prestaz"+
						  " AND t.pipp_sottotipo = '1'"+
						  ")"+
						  " AND i.int_tipo_pres = '3'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta "+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto"); 
						
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiMed"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_mmg"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_mmg", "" + totale_adi);
			stampaAccessiMmg(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiMed");
			if (totale_adi==0)
				h.put("#totale_acc_mmg#", "0");				
			else 
				h.put("#totale_acc_mmg#", faiNotNull(dbr, "totale_acc_mmg", 'S'));
			
			md.writeSostituisci("totaliAccessiMed", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiMmg", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiMmg
	
	private void stampaAccessiMmgTerm(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_mmg_term#", faiNotNull(mydbr, "num_acc_mmg_term", 'I'));
	    h_cons.put("#cod_zona_mmg_term#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_mmg_term#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiMedTerm", (Hashtable)h_cons); 
		

	}// END stampaAccessiMmg
	
	
	
	private void leggiEStampaAccessiMmgTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_mmg_term, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+" "+
						  " FROM intmmg i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+  
						  " WHERE i.int_data >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data <= "+formatDate(mydbc,data_fine)+
						  " AND EXISTS (SELECT t.* FROM tabpipp t"+
						  " WHERE t.pipp_tipo = i.int_tipo_pres"+
						  " AND t.pipp_codi = i.int_prestaz"+
						  " AND t.pipp_sottotipo = '1'"+
						  ")"+
						  " AND i.int_tipo_pres = '3'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto"); 
						
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiMedTerm"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_mmg_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_mmg_term", "" + totale_adi);
			stampaAccessiMmgTerm(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiMedTerm");
			if (totale_adi == 0)
				h.put("#totale_acc_mmg_term#", "0");				
			else 
				h.put("#totale_acc_mmg_term#", faiNotNull(dbr, "totale_acc_mmg_term", 'S'));
			
			md.writeSostituisci("totaliAccessiMedTerm", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiMmgTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiMmgTerm
	
	private void stampaAccessiMmgAnz(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_mmg_anz#", faiNotNull(mydbr, "num_acc_mmg_anz", 'I'));
	    h_cons.put("#cod_zona_mmg_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_mmg_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiMedAnz", (Hashtable)h_cons); 
		

	}// END stampaAccessiMmgAnz
	
	
	
	private void leggiEStampaAccessiMmgAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT  COUNT(*) num_acc_mmg_anz, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM intmmg i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u,"+
						  " cartella c"+  
						  " WHERE i.int_data >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data <= "+formatDate(mydbc,data_fine)+
						  " AND EXISTS (SELECT t.* FROM tabpipp t"+
						  " WHERE t.pipp_tipo = i.int_tipo_pres"+
						  " AND t.pipp_codi = i.int_prestaz"+
						  " AND t.pipp_sottotipo = '1'"+
						  ")"+
						  " AND i.int_tipo_pres = '3'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " AND c.n_cartella = i.int_cartella"+						  
						  " AND (" + formatDate(mydbc,data_fine) + " - NUMTOYMINTERVAL(65, 'YEAR') >= c.data_nasc)" + 
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
						
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiMedAnz"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_mmg_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_mmg_anz", "" + totale_adi);
			stampaAccessiMmgAnz(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiMedAnz");
			if (totale_adi==0)
				h.put("#totale_acc_mmg_anz#", "0");				
			else 
				h.put("#totale_acc_mmg_anz#", faiNotNull(dbr, "totale_acc_mmg_anz", 'S'));
			
			md.writeSostituisci("totaliAccessiMedAnz", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiMmgAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiMmgAnz
	
	private void stampaAccessiOp(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op#", faiNotNull(mydbr, "num_acc_oper", 'I'));
	    h_cons.put("#cod_zona_op#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOp", (Hashtable)h_cons); 
		

	}// END stampaAccessiOp
	
	
	
	private void leggiEStampaAccessiOp(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_oper, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto") ;

						
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOper"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_oper"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op", "" + totale_adi);
			stampaAccessiOp(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOp");
			if (totale_adi==0)
				h.put("#totale_acc_op#", "0");				
			else 
				h.put("#totale_acc_op#", faiNotNull(dbr, "totale_acc_op", 'S'));
			
			md.writeSostituisci("totaliAccessiOp", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOp", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOp
	
	private String getFiltroSkso(ISASConnection mydbc,String data_inizio,String data_fine) {		
		return " AND EXISTS (SELECT s.* FROM SKSO_FLS21_CASI_TRATTATI s"+
				  " WHERE s.n_cartella = i.int_cartella"+ 
				  " AND nvl(s.pr_data_puac_orig,s.pr_data_puac) <= "+formatDate(mydbc,data_fine)+
				  " AND ((s.pr_data_chiusura IS NULL)"+
				  " OR (s.pr_data_chiusura >= "+formatDate(mydbc,data_inizio)+
				  ")"+
				  ")"+
				  ")";
	}

	private void stampaAccessiOpTerm(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op_term#", faiNotNull(mydbr, "num_acc_oper_term", 'I'));
	    h_cons.put("#cod_zona_op_term#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_term#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpTerm", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpTerm
	
	
	
	private void leggiEStampaAccessiOpTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_oper_term,"+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+
				          " FROM interv i,"+
				          " anagra_c a,"+
				          " ubicazioni_n u"+ 
				          " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
				          " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
				          " AND i.int_ambdom = 'D'"+
				          getFiltroSkso(mydbc,data_inizio, data_fine)+
				          " AND u.tipo = 'C'"+
				          " AND u.codice = a.citta"+
				          " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
				          " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
				          " WHERE a1.n_cartella = a.n_cartella"+
				          " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
				          ")"+
				          " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperTerm"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_oper_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_term", "" + totale_adi);
			stampaAccessiOpTerm(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpTerm");
			if (totale_adi==0)
				h.put("#totale_acc_op_term#", "0");						
			else 
				h.put("#totale_acc_op_term#", faiNotNull(dbr, "totale_acc_op_term", 'S'));
			
			md.writeSostituisci("totaliAccessiOpTerm", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpTerm
	
	private void stampaAccessiOpAnz(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op_anz#", faiNotNull(mydbr, "num_acc_oper_anz", 'I'));
	    h_cons.put("#cod_zona_op_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpAnz", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpAnz
	
	
	
	private void leggiEStampaAccessiOpAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_oper_anz, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+	
						  " FROM interv i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u,"+
						  " cartella c"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " AND c.n_cartella = i.int_cartella"+						  
						  " AND (" + formatDate(mydbc,data_fine) + " - NUMTOYMINTERVAL(65, 'YEAR') >= c.data_nasc)" + 
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperAnz"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_oper_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_anz", "" + totale_adi);
			stampaAccessiOpAnz(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpAnz");
			if (totale_adi==0)
				h.put("#totale_acc_op_anz#", "0");				
			else 
				h.put("#totale_acc_op_anz#", faiNotNull(dbr, "totale_acc_op_anz", 'S'));
			
			md.writeSostituisci("totaliAccessiOpAnz", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpAnz
	
	private void stampaAccessiOpFis(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op_fis#", faiNotNull(mydbr, "num_acc_fis", 'I'));
	    h_cons.put("#cod_zona_op_fis#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_fis#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpFis", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpFis
	
	
	
	private void leggiEStampaAccessiOpFis(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_fis, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '04'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperFis"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_fis"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_fis", "" + totale_adi);
			stampaAccessiOpFis(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpFis");
			if (totale_adi==0)
				h.put("#totale_acc_op_fis#", "0");				
			else 
				h.put("#totale_acc_op_fis#", faiNotNull(dbr, "totale_acc_op_fis", 'S'));
			
			md.writeSostituisci("totaliAccessiOpFis", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpFis", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpFis
	
	private void stampaAccessiOpFisTerm(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op_fis_term#", faiNotNull(mydbr, "num_acc_fis_term", 'I'));
	    h_cons.put("#cod_zona_op_fis_term#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_fis_term#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpFisTerm", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpFisTerm
	
	
	
	private void leggiEStampaAccessiOpFisTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_fis_term, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '04'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperFisTerm"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_fis_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_fis_term", "" + totale_adi);
			stampaAccessiOpFisTerm(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpFisTerm");
			if (totale_adi==0)
				h.put("#totale_acc_op_fis_term#", "0");				
			else 
				h.put("#totale_acc_op_fis_term#", faiNotNull(dbr, "totale_acc_op_fis_term", 'S'));
			
			md.writeSostituisci("totaliAccessiOpFisTerm", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpFisTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpFisTerm
	
	private void stampaAccessiOpFisAnz(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op_fis_anz#", faiNotNull(mydbr, "num_acc_fis_anz", 'I'));
	    h_cons.put("#cod_zona_op_fis_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_fis_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpFisAnz", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpFisAnz
	
	
	
	private void leggiEStampaAccessiOpFisAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_fis_anz, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+
						  " FROM interv i, anagra_c a, ubicazioni_n u, cartella c"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '04'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " AND c.n_cartella = i.int_cartella"+
						  " AND (" + formatDate(mydbc,data_fine) + " - NUMTOYMINTERVAL(65, 'YEAR') >= c.data_nasc)" + 
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");
		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperFisAnz"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_fis_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_fis_anz", "" + totale_adi);
			stampaAccessiOpFisAnz(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpFisAnz");
			if (totale_adi==0)
				h.put("#totale_acc_op_fis_anz#", "0");				
			else 
				h.put("#totale_acc_op_fis_anz#", faiNotNull(dbr, "totale_acc_op_fis_anz", 'S'));
			
			md.writeSostituisci("totaliAccessiOpFisAnz", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpFisAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpFisAnz
	
	
	private void stampaAccessiOpInf(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_acc_op_inf#", faiNotNull(mydbr, "num_acc_inf", 'I'));
	    h_cons.put("#cod_zona_op_inf#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_inf#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpInf", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpInf
	
	
	
	private void leggiEStampaAccessiOpInf(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_inf,"+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i, anagra_c a, ubicazioni_n u"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '02'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperInf"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_inf"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_inf", "" + totale_adi);
			stampaAccessiOpInf(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpInf");
			if (totale_adi==0)
				h.put("#totale_acc_op_inf#", "0");				
			else 
				h.put("#totale_acc_op_inf#", faiNotNull(dbr, "totale_acc_op_inf", 'S'));
			
			md.writeSostituisci("totaliAccessiOpInf", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpInf", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpInf
	
	private void stampaAccessiOpInfTerm(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_acc_op_inf_term#", faiNotNull(mydbr, "num_acc_inf_term", 'I'));
	    h_cons.put("#cod_zona_op_inf_term#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_inf_term#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpInfTerm", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpInfTerm
	
	
	
	private void leggiEStampaAccessiOpInfTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_inf_term, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i, anagra_c a, ubicazioni_n u"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '02'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperInfTerm"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_inf_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_inf_term", "" + totale_adi);
			stampaAccessiOpInfTerm(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpInfTerm");
			if (totale_adi==0)
				h.put("#totale_acc_op_inf_term#", "0");				
			else 
				h.put("#totale_acc_op_inf_term#", faiNotNull(dbr, "totale_acc_op_inf_term", 'S'));
			
			md.writeSostituisci("totaliAccessiOpInfTerm", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpInfTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpInfTerm
	
	private void stampaAccessiOpInfAnz(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_acc_op_inf_anz#", faiNotNull(mydbr, "num_acc_inf_anz", 'I'));
	    h_cons.put("#cod_zona_op_inf_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_inf_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpInfAnz", (Hashtable)h_cons); 		

	}// END stampaAccessiOpInfAnz
	
	
	
	private void leggiEStampaAccessiOpInfAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_inf_anz, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i, anagra_c a,  ubicazioni_n u, cartella c"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '02'"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " AND c.n_cartella = i.int_cartella"+
						  " AND (" + formatDate(mydbc,data_fine) + " - NUMTOYMINTERVAL(65, 'YEAR') >= c.data_nasc)" + 
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperInfAnz"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_inf_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_inf_anz", "" + totale_adi);
			stampaAccessiOpInfAnz(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpInfAnz");
			if (totale_adi==0)
				h.put("#totale_acc_op_inf_anz#", "0");				
			else 
				h.put("#totale_acc_op_inf_anz#", faiNotNull(dbr, "totale_acc_op_inf_anz", 'S'));
			
			md.writeSostituisci("totaliAccessiOpInfAnz", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpInfAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpInfAnz
	
	
	private void stampaAccessiOpAltri(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_acc_op_altri#", faiNotNull(mydbr, "num_acc_altrioper", 'I'));
	    h_cons.put("#cod_zona_op_altri#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_altri#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpAltri", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpAltri
	
	
	
	private void leggiEStampaAccessiOpAltri(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_altrioper, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i, anagra_c a, ubicazioni_n u "+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper NOT IN ('02', '04')"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");


		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperAltri"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_altrioper"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_altri", "" + totale_adi);
			stampaAccessiOpAltri(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpAltri");
			if (totale_adi==0)
				h.put("#totale_acc_op_altri#", "0");				
			else 
				h.put("#totale_acc_op_altri#", faiNotNull(dbr, "totale_acc_op_altri", 'S'));
			
			md.writeSostituisci("totaliAccessiOpAltri", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpAltri", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpAltri
	
	private void stampaAccessiOpAltriTerm(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_acc_op_altri_term#", faiNotNull(mydbr, "num_acc_altrioper_term", 'I'));
	    h_cons.put("#cod_zona_op_altri_term#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_altri_term#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpAltriTerm", (Hashtable)h_cons); 
		

	}// END stampaAccessiOpAltriTerm
	
	
	
	private void leggiEStampaAccessiOpAltriTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_altrioper_term, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+
						  " FROM interv i, anagra_c a,ubicazioni_n u"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper NOT IN ('02', '04')"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperAltriTerm"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_altrioper_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_altri_term", "" + totale_adi);
			stampaAccessiOpAltriTerm(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpAltriTerm");
			if (totale_adi==0)
				h.put("#totale_acc_op_altri_term#", "0");				
			else 
				h.put("#totale_acc_op_altri_term#", faiNotNull(dbr, "totale_acc_op_altri_term", 'S'));
			
			md.writeSostituisci("totaliAccessiOpAltriTerm", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpAltriTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpAltriTerm
	
	private void stampaAccessiOpAltriAnz(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_acc_op_altri_anz#", faiNotNull(mydbr, "num_acc_altrioper_anz", 'I'));
	    h_cons.put("#cod_zona_op_altri_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_op_altri_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAccessiOpAltriAnz", (Hashtable)h_cons); 		

	}// END stampaAccessiOpAltriAnz
	
	
	
	private void leggiEStampaAccessiOpAltriAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_altrioper_anz, "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM interv i, anagra_c a, ubicazioni_n u, cartella c"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper NOT IN ('02', '04')"+
						  getFiltroSkso(mydbc,data_inizio, data_fine)+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = i.int_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " AND c.n_cartella = i.int_cartella"+
						  " AND (" + formatDate(mydbc,data_fine) + " - NUMTOYMINTERVAL(65, 'YEAR') >= c.data_nasc)" + 
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAccessiOperAltriAnz"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_altrioper_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_altri_anz", "" + totale_adi);
			stampaAccessiOpAltriAnz(md, dbr, h0);
			 
		    }
			md.write("fineTabAccessiOpAltriAnz");
			if (totale_adi==0)
				h.put("#totale_acc_op_altri_anz#", "0");				
			else 
				h.put("#totale_acc_op_altri_anz#", faiNotNull(dbr, "totale_acc_op_altri_anz", 'S'));
			
			md.writeSostituisci("totaliAccessiOpAltriAnz", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAccessiOpAltriAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpAltriAnz
	
	
	private void stampaADP(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_adp#", faiNotNull(mydbr, "num_medici", 'I'));
	    h_cons.put("#cod_zona_adp#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_adp#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaADP", (Hashtable)h_cons); 		

	}// END stampaADP
	
	
	
	private void leggiEStampaADP(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(DISTINCT(s.skadp_mmgpls)) num_medici,"+ 
						  " "+(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+""+
						  " FROM skmmg_adp s,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+
						  " WHERE s.skadp_data_inizio <= "+formatDate(mydbc,data_fine)+
						  " AND ((s.skadp_data_fine IS NULL)"+ 
						  " OR (s.skadp_data_fine >= "+formatDate(mydbc,data_inizio)+
						  ")"+ 
						  ")"+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = s.n_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");

		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabADP"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_medici"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_adp", "" + totale_adi);
			stampaADP(md, dbr, h0);
			 
		    }
			md.write("fineTabADP");
			if (totale_adi==0)
				h.put("#totale_acc_adp#", "0");				
			else 
				h.put("#totale_acc_adp#", faiNotNull(dbr, "totale_acc_adp", 'S'));
			
			md.writeSostituisci("totaliADP", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaADP", dbcur);
		   
		   
		}
	}// END leggiEStampaADP
	
	private void stampaAutoADP(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
	    h_cons.put("#num_auto_adp#", faiNotNull(mydbr, "num_assistiti", 'I'));
	    h_cons.put("#cod_zona_auto_adp#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_auto_adp#", faiNotNull(mydbr, "des_zona", 'S'));
	   
		md.writeSostituisci("tabellaAutoADP", (Hashtable)h_cons); 		

	}// END stampaAutoADP
	
	
	
	private void leggiEStampaAutoADP(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(DISTINCT(s.n_cartella)) num_assistiti,"+
				(extrAreaVasta?"u.cod_zona, u.des_zona":"u.cod_distretto as cod_zona, u.des_distretto as des_zona")+
						  " FROM skmmg_adp s,"+
						  " anagra_c a,"+
						  " ubicazioni_n u"+
						  " WHERE s.skadp_data_inizio <= "+formatDate(mydbc,data_fine)+
						  " AND ((s.skadp_data_fine IS NULL)"+ 
						  " OR (s.skadp_data_fine >= "+formatDate(mydbc,data_inizio)+
						  ")"+
						  ")"+
						  " AND u.tipo = 'C'"+
						  " AND u.codice = a.citta"+
						  " AND u."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
						  " AND a.n_cartella = s.n_cartella"+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")"+
						  " GROUP BY "+(extrAreaVasta?"cod_zona, des_zona":"cod_distretto, des_distretto")+
						  " ORDER BY "+(extrAreaVasta?"des_zona":"des_distretto");


		
		System.out.println("QUERY FLUSSI" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		md.write("inizioTabAutoADP"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_assistiti"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_auto_adp", "" + totale_adi);
			stampaAutoADP(md, dbr, h0);
			 
		    }
			md.write("fineTabAutoADP");
			if (totale_adi==0)
				h.put("#totale_acc_auto_adp#", "0");				
			else 
				h.put("#totale_acc_auto_adp#", faiNotNull(dbr, "totale_acc_auto_adp", 'S'));
			
			md.writeSostituisci("totaliAutoADP", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaAutoADP", dbcur);
		   
		   
		}
	}// END leggiEStampaAutoADP

// ================================= Utilita' ============================== //


	private String faiNotNull(ISASRecord mydbr, String nomeCampo, char tipoObj) throws Exception
	{
		String valore = "";
		if (mydbr == null)
			return valore;

		if ((Object)mydbr.get(nomeCampo) != null)
			switch(tipoObj)
			{
				case 'S':
					valore = (String)mydbr.get(nomeCampo);
					break;
				case 'I':
					valore = ((Integer)mydbr.get(nomeCampo)).toString();
					break;
				case 'D':
					valore = ((Double)mydbr.get(nomeCampo)).toString();
					break;
				case 'T':
					valore = new it.pisa.caribel.util.DataWI(
						(java.sql.Date)mydbr.get(nomeCampo)).getFormattedString(0);
					break;
				default:
					break;
			}

		return valore;
	} // END faiNotNull

	
	
}



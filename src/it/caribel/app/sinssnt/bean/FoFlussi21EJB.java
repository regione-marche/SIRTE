package it.caribel.app.sinssnt.bean;

import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.operatori.GestTpOp;
import it.pisa.caribel.profile2.myLogin;

import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Hashtable;

public class FoFlussi21EJB extends SINSSNTConnectionEJB {
	private static final String MIONOME = "FoFlussi21EJB.";
	private boolean extrAreaVasta;
	private String distretto;
	private String area_vasta;
	Hashtable h = new Hashtable();

	public FoFlussi21EJB() {}
	
	public byte[] query_flussi(String utente, String passwd, Hashtable hC,
		    mergeDocument eve) throws SQLException
{
		boolean done = false;
		ISASConnection dbc = null;
		
		try {			
			myLogin lg = new myLogin();
			lg.put(utente,passwd);
			dbc = super.logIn(lg);
			Hashtable h_xLayout = new Hashtable();		
			
			preparaLayoutAdi(eve,dbc,hC);
			Hashtable h_dati_generali = new Hashtable();
			h_dati_generali.put("#anno#", hC.get("anno").toString());
			
			h_dati_generali.put("#ragione_sociale_usl#",getConfStringField(dbc, "SINS", "ragione_sociale", "conf_txt"));
			h_dati_generali.put("#codice_usl#",getConfStringField(dbc, "SINS", "codice_usl", "conf_txt"));
			h_dati_generali.put("#codice_regione#",getConfStringField(dbc, "SINS", "codice_regione", "conf_txt"));
			
			
			eve.writeSostituisci("data_generali",h_dati_generali);
			eve.write("inizioTabella");
			leggiEStampaAdi(hC, eve, dbc);
			
//			leggiEStampaAdiMalati(h, eve, dbc);
//			leggiEStampaAdiAnziani(h, eve, dbc);
			leggiEStampaAccessiMmg(hC, eve, dbc);
			leggiEStampaAccessiMmgTerm(hC, eve, dbc);
			leggiEStampaAccessiMmgAnz(hC, eve, dbc);
			

			eve.write("fineTabella");
			
					
			leggiEStampaAccessiOpFis(hC, eve, dbc);			
			leggiEStampaAccessiOpInf(hC, eve, dbc);			
			leggiEStampaAccessiOpAltri(hC, eve, dbc);			
			leggiEStampaAccessiOp(hC, eve, dbc);
			
			
			leggiEStampaTotFisTerm(hC, eve, dbc);
			leggiEStampaTotInfTerm(hC, eve, dbc);
			leggiEStampaTotAltriTerm(hC, eve, dbc);
			leggiEStampaTotOpTerm(hC, eve, dbc);
			leggiEStampaTotFisAnz(hC, eve, dbc);
			leggiEStampaTotInfAnz(hC, eve, dbc);
			leggiEStampaTotAltriAnz(hC, eve, dbc);
			leggiEStampaTotOpAnz(hC, eve, dbc);	
			
			
			
			eve.writeSostituisci("accessi", h);
						
			
			eve.write("fineTabellaMalati");
			
			eve.write("inizioTabellaMedici");
			leggiEStampaTotADP(hC, eve, dbc);
			leggiEStampaTotAutoADP(hC, eve, dbc);
			eve.write("fineTabellaMedici");
			
			eve.write("finale");
			
			eve.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get();	// restituisco il bytearray
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("FoFlussi21EJB.query_report(): Eccezione= " + e);
			throw new SQLException("FoFlussi21EJB.query_report(): " + e);
		}finally{
			if(!done){
				try{
					eve.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					e1.printStackTrace();
					System.out.println("FoFlussi21EJB.query_report(): Eccezione nella chiusura della connessione= " + e1);
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
	

	


	private void stampaAdi(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_casi_adi#", faiNotNull(mydbr, "num_casi_adi", 'I'));
	    h_cons.put("#cod_zona#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAdi", (Hashtable)h_cons); 
		

	}// END stampaAdi
	

	
	private void leggiEStampaAdi(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		String suddivisione=(String)h0.get("tipo");
		this.area_vasta=(String)h0.get("area");
		this.distretto = (String)h0.get("distr");
		this.extrAreaVasta = (suddivisione != null && suddivisione.equals("A"));
		
		
		String myselect = "select SUM(ADI) as totale_adi, SUM(TERMINALE) as totale_adi_mal, SUM(ANZIANO) as totale_adi_anz,"
				+(extrAreaVasta?"'AREA VASTA "+area_vasta+"'":"'DISTRETTO "+distretto+"'")+" as zona from (SELECT 1 as ADI, decode(s.tipo_ute,"+CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE+",1,0) TERMINALE," +
				"decode(s.tipo_ute,"+CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO+",1,0) ANZIANO  from " +
						" SKSO_FLS21_CASI_TRATTATI s"+
				  " where " +
				  " (s.pr_data_chiusura is null or s.pr_data_chiusura >= "+mydbc.formatDbDate(data_inizio)+")"+
				  " and s.pr_data_puac <= "+mydbc.formatDbDate(data_fine)+						
				  " AND s."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
				  ")";
						
System.out.println("QUERY PC ADI ATTIVE NELL'ANNO" + myselect);
		
		ISASRecord dbr = mydbc.readRecord(myselect);
		if (dbr == null) {
			md.write("messaggio");
			System.out.println("SCRITTO MESSAGGIO");
		}else{
		md.write("inizioTabAdi"); 
		// scrivo totali adi
			stampaAdi(md, dbr, h0);
			md.write("fineTabAdi");
			h.put("#totale_adi#", dbr.get("totale_adi").toString());
			md.writeSostituisci("totaliAdi", h); 
		   
		//scrivo di cui terminali
			md.write("inizioTabAdiMal"); 
			stampaAdiMalati(md, dbr, h0);
			md.write("fineTabAdiMal");
			h.put("#totale_adi_mal#", dbr.get("totale_adi_mal").toString());
			md.writeSostituisci("totaliAdiMal", h); 
		   
		//scrivo di cui anziani		
			md.write("inizioTabAdiAnz"); 
			stampaAdiAnziani(md, dbr, h0);		   
			md.write("fineTabAdiAnz");
			h.put("#totale_adi_anz#", dbr.get("totale_adi_anz").toString());			
			md.writeSostituisci("totaliAdiAnz", h); 
		}
	}// END leggiEStampaAdi
	
	private void stampaAdiMalati(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_casi_adi_term#", faiNotNull(mydbr, "num_casi_adi_term", 'I'));
	    h_cons.put("#cod_zona_mal#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_mal#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAdiMal", h_cons); 
		

	}// END stampaAdiMalati
	
	
	
	private void stampaAdiAnziani(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
	{ 
		Hashtable h_cons = new Hashtable();
		
		ServerUtility su = new ServerUtility();
		
	    h_cons.put("#num_casi_adi_anz#", faiNotNull(mydbr, "num_casi_adi_anz", 'I'));
	    h_cons.put("#cod_zona_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
	    h_cons.put("#zona_anz#", faiNotNull(mydbr, "zona", 'S'));
	   
		md.writeSostituisci("tabellaAdiAnz", (Hashtable)h_cons); 
		

	}// END stampaAdiAnziani
	
	private String getFiltroSkso(ISASConnection mydbc,String data_inizio,String data_fine, String dt_rif, String tipo_ute, String cart_field) {		
		return " AND EXISTS (SELECT s.* from SKSO_FLS21_CASI_TRATTATI s" +
				 " where " +
				  " (s.pr_data_chiusura is null or s.pr_data_chiusura >= "+mydbc.formatDbDate(data_inizio)+")"+
				  " and s.pr_data_puac <= "+mydbc.formatDbDate(data_fine)+						
				  " AND s."+(extrAreaVasta?"cod_zona = '"+area_vasta+"'":"cod_distretto = '"+distretto+"'")+
				  " AND s.n_cartella = i."+cart_field+ 
				  " AND "+dt_rif+ " >= s.pr_data_puac " +
				  " AND ("+dt_rif+ " <= s.pr_data_chiusura or s.pr_data_chiusura is null)"+
				  (tipo_ute.equals("")?"":" AND s.tipo_ute = " +tipo_ute)+
				  ")";
	}

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
		int totale_interv = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_mmg"+
						  " FROM intmmg i"+
						  " WHERE i.int_data >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data <= "+formatDate(mydbc,data_fine)+
						  " AND EXISTS (SELECT t.* FROM tabpipp t"+
						  " WHERE t.pipp_tipo = i.int_tipo_pres"+
						  " AND t.pipp_codi = i.int_prestaz"+
						  " AND t.pipp_sottotipo = '1'"+
						  ")"+
						  " AND i.int_tipo_pres = '3'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data","", "int_cartella");
						
		System.out.println("QUERY FLUSSI num_acc_mmg:" + myselect);
		ISASRecord dbr = mydbc.readRecord(myselect);
		
		myselect = "SELECT COUNT(*) num_acc_med_interv"+
				  " FROM interv i"+  
				  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
				  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
				  " AND i.int_ambdom = 'D'"+
				  " AND i.int_tipo_oper in ('"+GestTpOp.CTS_COD_MEDICO_SPECIALISTA+"','"+GestTpOp.CTS_MEDICO_CURE_PALLIATIVE+"','"+GestTpOp.CTS_COD_GUARDIA_MEDICA+"')"+
				  " AND i.flag_sent in (1,6)"+
				  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest","", "int_cartella");


		System.out.println("QUERY FLUSSI num_acc_med_interv_anz:" + myselect);
		
		ISASRecord dbr_interv = mydbc.readRecord(myselect);
		
		totale_interv = Integer.parseInt("" +dbr_interv.get("num_acc_med_interv"));
		
		
			totale = Integer.parseInt("" + dbr.get("num_acc_mmg"));
			totale_adi = totale + totale_interv;
			dbr.put("totale_acc_mmg", "" + totale_adi);
		
			if (totale_adi==0)
				h.put("#totale_acc_mmg#", "0");				
			else 
				h.put("#totale_acc_mmg#", faiNotNull(dbr, "totale_acc_mmg", 'S'));
			
			md.writeSostituisci("totaliAccessiMed", (Hashtable)h); 
		   
		   
		
	}// END leggiEStampaAccessiMmg
	
//	private void stampaAccessiMmgTerm(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
//	{ 
//		Hashtable h_cons = new Hashtable();
//		
//		ServerUtility su = new ServerUtility();
//		
//	    h_cons.put("#num_acc_mmg_term#", faiNotNull(mydbr, "num_acc_mmg_term", 'I'));
//	    h_cons.put("#cod_zona_mmg_term#", faiNotNull(mydbr, "cod_zona", 'S'));
//	    h_cons.put("#zona_mmg_term#", faiNotNull(mydbr, "zona", 'S'));
//	   
//		md.writeSostituisci("tabellaAccessiMedTerm", (Hashtable)h_cons); 
//		
//
//	}// END stampaAccessiMmg
	
	
	
	private void leggiEStampaAccessiMmgTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale_mmg = 0;
		int totale_adi = 0;
		int totale_interv = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(*) num_acc_mmg_term"+
						  " FROM intmmg i"+  
						  " WHERE i.int_data >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data <= "+formatDate(mydbc,data_fine)+
						  " AND EXISTS (SELECT t.* FROM tabpipp t"+
						  " WHERE t.pipp_tipo = i.int_tipo_pres"+
						  " AND t.pipp_codi = i.int_prestaz"+
						  " AND t.pipp_sottotipo = '1'"+
						  ")"+
						  " AND i.int_tipo_pres = '3'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data",CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE, "int_cartella");
						
		System.out.println("QUERY FLUSSI num_acc_mmg_term:" + myselect);

	ISASRecord dbr=null;
		
		dbr = mydbc.readRecord(myselect);
		
		
		myselect = "SELECT COUNT(*) num_acc_med_interv_term"+
				  " FROM interv i"+  
				  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
				  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
				  " AND i.int_ambdom = 'D'"+
				  " AND i.int_tipo_oper in ('"+GestTpOp.CTS_COD_MEDICO_SPECIALISTA+"','"+GestTpOp.CTS_MEDICO_CURE_PALLIATIVE+"','"+GestTpOp.CTS_COD_GUARDIA_MEDICA+"')"+
				  " AND i.flag_sent in (1,6)"+
				  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest",CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");


		System.out.println("QUERY FLUSSI num_acc_med_interv_anz:" + myselect);
		
		ISASRecord dbr_interv = mydbc.readRecord(myselect);
		
		totale_interv = Integer.parseInt("" +dbr_interv.get("num_acc_med_interv_term"));
		
			totale_mmg = Integer.parseInt("" + dbr.get("num_acc_mmg_term"));
			totale_adi = totale_mmg + totale_interv;
			dbr.put("totale_acc_mmg_term", "" + totale_adi);
//			stampaAccessiMmgTerm(md, dbr, h0);
		
			if (totale_adi == 0)
				h.put("#totale_acc_mmg_term#", "0");				
			else 
				h.put("#totale_acc_mmg_term#", faiNotNull(dbr, "totale_acc_mmg_term", 'S'));
			
			md.writeSostituisci("totaliAccessiMedTerm", (Hashtable)h); 
//		    close_dbcur_nothrow("leggiEStampaAccessiMmgTerm", dbcur);
		   
		   
		
	}// END leggiEStampaAccessiMmgTerm
	
//	private void stampaAccessiMmgAnz(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
//	{ 
//		Hashtable h_cons = new Hashtable();
//		
//		ServerUtility su = new ServerUtility();
//		
//	    h_cons.put("#num_acc_mmg_anz#", faiNotNull(mydbr, "num_acc_mmg_anz", 'I'));
//	    h_cons.put("#cod_zona_mmg_anz#", faiNotNull(mydbr, "cod_zona", 'S'));
//	    h_cons.put("#zona_mmg_anz#", faiNotNull(mydbr, "zona", 'S'));
//	   
//		md.writeSostituisci("tabellaAccessiMedAnz", (Hashtable)h_cons); 
//		
//
//	}// END stampaAccessiMmgAnz
//	
	
	
	private void leggiEStampaAccessiMmgAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale_mmg = 0;
		int totale_interv = 0;
		int totale_adi = 0;
		
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT  COUNT(*) num_acc_mmg_anz"+
						  " FROM intmmg i"+  
						  " WHERE i.int_data >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data <= "+formatDate(mydbc,data_fine)+
						  " AND EXISTS (SELECT t.* FROM tabpipp t"+
						  " WHERE t.pipp_tipo = i.int_tipo_pres"+
						  " AND t.pipp_codi = i.int_prestaz"+
						  " AND t.pipp_sottotipo = '1'"+
						  ")"+
						  " AND i.int_tipo_pres = '3'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data",CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");
		
						
		System.out.println("QUERY FLUSSI num_acc_mmg_anz:" + myselect);

		ISASRecord dbr=null;
		
		dbr = mydbc.readRecord(myselect);
		
		
		myselect = "SELECT COUNT(*) num_acc_med_interv_anz"+
				  " FROM interv i"+  
				  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
				  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
				  " AND i.int_ambdom = 'D'"+
				  " AND i.int_tipo_oper in ('"+GestTpOp.CTS_COD_MEDICO_SPECIALISTA+"','"+GestTpOp.CTS_MEDICO_CURE_PALLIATIVE+"','"+GestTpOp.CTS_COD_GUARDIA_MEDICA+"')"+
				  " AND i.flag_sent in (1,6)"+
				  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest",CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");


		System.out.println("QUERY FLUSSI num_acc_med_interv_anz:" + myselect);
		
		ISASRecord dbr_interv = mydbc.readRecord(myselect);
		
		totale_interv = Integer.parseInt("" + dbr_interv.get("num_acc_med_interv_anz"));	
		
		md.write("inizioTabAccessiMedAnz"); 
			
		totale_mmg = Integer.parseInt("" + dbr.get("num_acc_mmg_anz"));
			
			
			totale_adi = totale_mmg + totale_interv;
			dbr.put("totale_acc_mmg_anz", "" + totale_adi);
//			stampaAccessiMmgAnz(md, dbr, h0);
//			 
		
//			md.write("fineTabAccessiMedAnz");
			if (totale_adi==0)
				h.put("#totale_acc_mmg_anz#", "0");				
			else 
				h.put("#totale_acc_mmg_anz#", faiNotNull(dbr, "totale_acc_mmg_anz", 'S'));
			
			md.writeSostituisci("totaliAccessiMedAnz", (Hashtable)h); 
		 
	}// END leggiEStampaAccessiMmgAnz
	
//	private void stampaAccessiOp(mergeDocument md, ISASRecord mydbr,Hashtable h) throws Exception
//	{ 
//		Hashtable h_cons = new Hashtable();
//		
//		ServerUtility su = new ServerUtility();
//		
//	    h_cons.put("#num_acc_op#", faiNotNull(mydbr, "num_acc_oper", 'I'));
//	    h_cons.put("#cod_zona_op#", faiNotNull(mydbr, "cod_zona", 'S'));
//	    h_cons.put("#zona_op#", faiNotNull(mydbr, "zona", 'S'));
//	   
//		md.writeSostituisci("tabellaAccessiOp", (Hashtable)h_cons); 
//		
//
//	}// END stampaAccessiOp
	
	
	
	private void leggiEStampaAccessiOp(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_oper"+
						  " FROM interv i"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest","", "int_cartella");
		

						
		System.out.println("QUERY FLUSSI num_acc_oper:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
//		md.write("inizioTabAccessiOper"); 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_oper"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op", "" + totale_adi);
//			stampaAccessiOp(md, dbr, h0);
			 
		    }
//			md.write("fineTabAccessiOp");
			if (totale_adi==0)
				h.put("#totale_acc_op#", "0");				
			else 
				h.put("#totale_acc_op#", faiNotNull(dbr, "totale_acc_op", 'S'));
			
			
			int ore_tot = Integer.parseInt(h.get("#ore_tot_fisio#").toString())+
					Integer.parseInt(h.get("#ore_tot_inf#").toString())+
					Integer.parseInt(h.get("#ore_tot_altri#").toString());
			
			h.put("#ore_tot#", Integer.toString(ore_tot));

		    close_dbcur_nothrow("leggiEStampaAccessiOp", dbcur);
		    
		   
		   
		}
	}// END leggiEStampaAccessiOp
	
	
	

	
	
	private void leggiEStampaAccessiOpFis(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		
		String myselect = "SELECT COUNT(*) num_acc_fis"+
						  " FROM interv i"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper in ('"+GestTpOp.CTS_COD_FISIOTERAPISTA+"','"+GestTpOp.CTS_COD_LOGOPEDISTA+"','"+GestTpOp.CTS_COD_TERAPISTA_OCCUPAZIONALE+"')"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest","", "int_cartella");

		
		System.out.println("QUERY FLUSSI num_acc_fis:" + myselect);

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
			 
		    }
			md.write("fineTabAccessiOpFis");
			if (totale_adi==0)
				h.put("#totale_acc_op_fis#", "0");				
			else 
				h.put("#totale_acc_op_fis#", faiNotNull(dbr, "totale_acc_op_fis", 'S'));
			
			h.put("#ore_tot_fisio#", h.get("#totale_acc_op_fis#"));

		    close_dbcur_nothrow("leggiEStampaAccessiOpFis", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpFis
	
	
	

	
	
	private void leggiEStampaAccessiOpInf(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_inf"+
						  " FROM interv i"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '"+GestTpOp.CTS_COD_INFERMIERE+"'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest","", "int_cartella");

		
		System.out.println("QUERY FLUSSI num_acc_inf:" + myselect);

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
		    }
			md.write("fineTabAccessiOpInf");
			if (totale_adi==0)
				h.put("#totale_acc_op_inf#", "0");				
			else 
				h.put("#totale_acc_op_inf#", faiNotNull(dbr, "totale_acc_op_inf", 'S'));
			
			int ore_inf = Integer.parseInt(h.get("#totale_acc_op_inf#").toString())*3/4;
			h.put("#ore_tot_inf#", Integer.toString(ore_inf));

		    close_dbcur_nothrow("leggiEStampaAccessiOpInf", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpInf
	
	

	
	private void leggiEStampaAccessiOpAltri(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_altrioper"+
						  " FROM interv i"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper NOT IN ('"+GestTpOp.CTS_COD_INFERMIERE+"', '"+GestTpOp.CTS_COD_FISIOTERAPISTA+"','"+GestTpOp.CTS_COD_LOGOPEDISTA+"','"+GestTpOp.CTS_COD_TERAPISTA_OCCUPAZIONALE+"')"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest","", "int_cartella");


		
		System.out.println("QUERY FLUSSI num_acc_altrioper:" + myselect);

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
		   }
			md.write("fineTabAccessiOpAltri");
			if (totale_adi==0)
				h.put("#totale_acc_op_altri#", "0");				
			else 
				h.put("#totale_acc_op_altri#", faiNotNull(dbr, "totale_acc_op_altri", 'S'));
			
			h.put("#ore_tot_altri#", h.get("#totale_acc_op_altri#"));
		    close_dbcur_nothrow("leggiEStampaAccessiOpAltri", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpAltri
	
	private void leggiEStampaTotFisTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_fis_term"+
						  " FROM interv i"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper IN ('"+GestTpOp.CTS_COD_FISIOTERAPISTA+"','"+GestTpOp.CTS_COD_LOGOPEDISTA+"','"+GestTpOp.CTS_COD_TERAPISTA_OCCUPAZIONALE+"')"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest", CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE, "int_cartella");
		
		System.out.println("QUERY FLUSSI num_acc_fis_term:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_fis_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_fis_term", "" + totale_adi);
			
			 
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_fis_term#", "0");				
			else 
				h.put("#totale_acc_op_fis_term#", faiNotNull(dbr, "totale_acc_op_fis_term", 'S'));
			
			
			h.put("#ore_term_fisio#", h.get("#totale_acc_op_fis_term#"));

		    close_dbcur_nothrow("leggiEStampaTotFisTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaTotFisTerm
	
	private void leggiEStampaTotInfTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_inf_term"+
						  " FROM interv i"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '"+GestTpOp.CTS_COD_INFERMIERE+"'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest", CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE, "int_cartella");
		
		System.out.println("QUERY FLUSSI num_acc_inf_term:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_inf_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_inf_term", "" + totale_adi);
			
			 
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_inf_term#", "0");				
			else 
				h.put("#totale_acc_op_inf_term#", faiNotNull(dbr, "totale_acc_op_inf_term", 'S'));
			
			int ore_inf = Integer.parseInt(h.get("#totale_acc_op_inf_term#").toString())*3/4;
			h.put("#ore_term_inf#", Integer.toString(ore_inf));
			
			
		    close_dbcur_nothrow("leggiEStampaTotInfTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpInfTerm
	
	private void leggiEStampaTotAltriTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_altrioper_term"+
						  " FROM interv i"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper NOT IN ('"+GestTpOp.CTS_COD_INFERMIERE+"', '"+GestTpOp.CTS_COD_FISIOTERAPISTA+"','"+GestTpOp.CTS_COD_LOGOPEDISTA+"','"+GestTpOp.CTS_COD_TERAPISTA_OCCUPAZIONALE+"')"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest",CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE, "int_cartella");
		

		System.out.println("QUERY FLUSSI num_acc_altrioper_term:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_altrioper_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_altri_term", "" + totale_adi);
			
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_altri_term#", "0");				
			else 
				h.put("#totale_acc_op_altri_term#", faiNotNull(dbr, "totale_acc_op_altri_term", 'S'));
			
			h.put("#ore_term_altri#", h.get("#totale_acc_op_altri_term#"));

		    close_dbcur_nothrow("leggiEStampaAccessiOpAltriTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaTotAltriTerm
	
	private void leggiEStampaTotOpTerm(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_oper_term"+
				          " FROM interv i"+ 
				          " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
				          " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
				          " AND i.int_ambdom = 'D'"+
				          " AND i.flag_sent in (1,6)"+
				          getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest", CostantiSinssntW.CTS_TIPO_UTENTE_MALATO_TERMINALE, "int_cartella");
		
		System.out.println("QUERY FLUSSI num_acc_oper_term:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_oper_term"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_term", "" + totale_adi);
			
		    }
			md.write("fineTabAccessiOpTerm");
			if (totale_adi==0)
				h.put("#totale_acc_op_term#", "0");						
			else 
				h.put("#totale_acc_op_term#", faiNotNull(dbr, "totale_acc_op_term", 'S'));

			int ore_term_tot = Integer.parseInt(h.get("#ore_term_fisio#").toString())+
					Integer.parseInt(h.get("#ore_term_inf#").toString())+
					Integer.parseInt(h.get("#ore_term_altri#").toString());
			
			h.put("#ore_term#", Integer.toString(ore_term_tot));
			
		    close_dbcur_nothrow("leggiEStampaAccessiOpTerm", dbcur);
		   
		   
		}
	}// END leggiEStampaAccessiOpTerm
	
	private void leggiEStampaTotFisAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_fis_anz"+
						  " FROM interv i"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper in ('"+GestTpOp.CTS_COD_FISIOTERAPISTA+"','"+GestTpOp.CTS_COD_LOGOPEDISTA+"','"+GestTpOp.CTS_COD_TERAPISTA_OCCUPAZIONALE+"')"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest",CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");
		
		System.out.println("QUERY FLUSSI num_acc_fis_anz:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_fis_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_fis_anz", "" + totale_adi);
			
			 
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_fis_anz#", "0");				
			else 
				h.put("#totale_acc_op_fis_anz#", faiNotNull(dbr, "totale_acc_op_fis_anz", 'S'));
			
			h.put("#ore_anz_fisio#", h.get("#totale_acc_op_fis_anz#"));

		    close_dbcur_nothrow("leggiEStampaAccessiOpFisAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaTotFisAnz
	
	private void leggiEStampaTotInfAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_inf_anz"+
						  " FROM interv i"+ 
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper = '"+GestTpOp.CTS_COD_INFERMIERE+"'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest", CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");
		
		
		System.out.println("QUERY FLUSSI num_acc_inf_anz:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		 
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_inf_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_inf_anz", "" + totale_adi);
			
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_inf_anz#", "0");				
			else 
				h.put("#totale_acc_op_inf_anz#", faiNotNull(dbr, "totale_acc_op_inf_anz", 'S'));
			
			int ore_inf = Integer.parseInt(h.get("#totale_acc_op_inf_anz#").toString())*3/4;
			h.put("#ore_anz_inf#", Integer.toString(ore_inf));
			
		    close_dbcur_nothrow("leggiEStampaTotInfAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaTotInfAnz
	
	private void leggiEStampaTotAltriAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_altrioper_anz"+
						  " FROM interv i"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.int_tipo_oper NOT IN ('"+GestTpOp.CTS_COD_INFERMIERE+"', '"+GestTpOp.CTS_COD_FISIOTERAPISTA+"','"+GestTpOp.CTS_COD_LOGOPEDISTA+"','"+GestTpOp.CTS_COD_TERAPISTA_OCCUPAZIONALE+"')"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest",CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");
		

		
		System.out.println("QUERY FLUSSI num_acc_altrioper_anz:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_altrioper_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_altri_anz", "" + totale_adi);
			
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_altri_anz#", "0");				
			else 
				h.put("#totale_acc_op_altri_anz#", faiNotNull(dbr, "totale_acc_op_altri_anz", 'S'));
			
			h.put("#ore_anz_altri#", h.get("#totale_acc_op_altri#"));

			
		    close_dbcur_nothrow("leggiEStampaTotAltriAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaTotAltriAnz
	
	private void leggiEStampaTotOpAnz(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		
		String myselect = "SELECT COUNT(*) num_acc_oper_anz"+
						  " FROM interv i"+  
						  " WHERE i.int_data_prest >= "+formatDate(mydbc,data_inizio)+
						  " AND i.int_data_prest <= "+formatDate(mydbc,data_fine)+
						  " AND i.int_ambdom = 'D'"+
						  " AND i.flag_sent in (1,6)"+
						  getFiltroSkso(mydbc,data_inizio, data_fine, "i.int_data_prest",CostantiSinssntW.CTS_TIPO_UTENTE_ANZIANO, "int_cartella");
		
		System.out.println("QUERY FLUSSI num_acc_oper_anz:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_acc_oper_anz"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_op_anz", "" + totale_adi);
			
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_op_anz#", "0");				
			else 
				h.put("#totale_acc_op_anz#", faiNotNull(dbr, "totale_acc_op_anz", 'S'));
			
			int ore_tot = Integer.parseInt(h.get("#ore_anz_fisio#").toString())+
					Integer.parseInt(h.get("#ore_anz_inf#").toString())+
					Integer.parseInt(h.get("#ore_anz_altri#").toString());
			
			h.put("#ore_anz#", Integer.toString(ore_tot));
			
		    close_dbcur_nothrow("leggiEStampaTotOpAnz", dbcur);
		   
		   
		}
	}// END leggiEStampaTotOpAnz
	
	private void leggiEStampaTotADP(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(DISTINCT(s.skadp_mmgpls)) num_medici"+
						  " FROM skmmg_adp s join skmedico b on s.n_cartella = b.n_cartella and s.n_contatto = b.n_contatto"+
						   " join anagra_c a on a.n_cartella = s.n_cartella"+
						  " WHERE s.skadp_data_inizio <= "+formatDate(mydbc,data_fine)+
						  " AND ((s.skadp_data_fine IS NULL)"+ 
						  " OR (s.skadp_data_fine >= "+formatDate(mydbc,data_inizio)+
						  ")"+
						  ")"+
						  "	AND b.skm_cod_presidio in (select codpres from presidi p where "+" p."+(extrAreaVasta?"codzon = '"+area_vasta+"')":"coddistr = '"+distretto+"')")+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")";

		

		
		System.out.println("QUERY FLUSSI num_medici:" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_medici"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_adp", "" + totale_adi);
			
			 
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_adp#", "0");				
			else 
				h.put("#totale_acc_adp#", faiNotNull(dbr, "totale_acc_adp", 'S'));
			
			md.writeSostituisci("totaliMedici", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaTotADP", dbcur);
		   
		   
		}
	}// END leggiEStampaTotADP
	private void leggiEStampaTotAutoADP(Hashtable h0, mergeDocument md,ISASConnection mydbc) throws Exception
	{		
	
		String data_inizio="01/01/"+h0.get("anno");
		String data_fine="31/12/"+h0.get("anno");
		int totale = 0;
		int totale_adi = 0;
		Hashtable h = new Hashtable();
		
		String myselect = "SELECT COUNT(DISTINCT(s.n_cartella)) num_assistiti"+
						  " FROM skmmg_adp s join skmedico b on s.n_cartella = b.n_cartella and s.n_contatto = b.n_contatto"+
						   " join anagra_c a on a.n_cartella = s.n_cartella"+
						  " WHERE s.skadp_data_inizio <= "+formatDate(mydbc,data_fine)+
						  " AND ((s.skadp_data_fine IS NULL)"+ 
						  " OR (s.skadp_data_fine >= "+formatDate(mydbc,data_inizio)+
						  ")"+
						  ")"+
						  "	AND b.skm_cod_presidio in (select codpres from presidi p where "+" p."+(extrAreaVasta?"codzon = '"+area_vasta+"')":"coddistr = '"+distretto+"')")+
						  " AND a.data_variazione IN (SELECT MAX(a1.data_variazione) FROM anagra_c a1"+
						  " WHERE a1.n_cartella = a.n_cartella"+
						  " AND a1.data_variazione <= "+formatDate(mydbc,data_fine)+
						  ")";


		
		System.out.println("QUERY FLUSSI num_assistiti" + myselect);

		ISASCursor dbcur=null;
		
		dbcur = mydbc.startCursor(myselect);	
		boolean dentro = (dbcur != null && dbcur.getDimension() > 0);
		if (!dentro) {
			md.write("messaggio");			
		}
		
		ISASRecord dbr = null;
		if (dbcur != null) {
			
			while(dbcur.next()){
			dbr = dbcur.getRecord(); 			
			totale = Integer.parseInt("" + dbr.get("num_assistiti"));
			totale_adi = totale + totale_adi;
			dbr.put("totale_acc_auto_adp", "" + totale_adi);
			
		    }
			
			if (totale_adi==0)
				h.put("#totale_acc_auto_adp#", "0");				
			else 
				h.put("#totale_acc_auto_adp#", faiNotNull(dbr, "totale_acc_auto_adp", 'S'));
			
			md.writeSostituisci("totaliAuto", (Hashtable)h); 
		    close_dbcur_nothrow("leggiEStampaTotAutoADP", dbcur);
		   
		   
		}
	}// END leggiEStampaTotAutoADP
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



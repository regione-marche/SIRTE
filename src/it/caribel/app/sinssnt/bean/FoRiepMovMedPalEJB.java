package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 19/12/2006 - EJB di connessione alla procedura SINS Tabella FoRiepMovMedPal
//
//
//	23/02/09 m.: modificato filtri x rendere i risultati uguali alla stampa "Riepilogo decessi"
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.ndo.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;	// fo merge
import it.pisa.caribel.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

//import org.apache.fop.apps.Driver;
//import org.apache.fop.apps.Version;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class FoRiepMovMedPalEJB extends SINSSNTConnectionEJB  
{

	String dom_res;
	String dr;
	public FoRiepMovMedPalEJB() {}

	NDOUtil unt = new NDOUtil();
	ServerUtility su = new ServerUtility();

	Hashtable hColonne = new Hashtable();
	
	private String codice_usl="";
	private String codice_regione="";

	Hashtable h_Ass = new Hashtable();// x contare 1 sola volta gli assistiti

	// 23/02/09
	Vector vMotDec = null;
	Vector vLuoDec = null;



	public byte[] query_riepmov(String utente, String passwd, Hashtable par,mergeDocument eve) throws SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;		

		try{
			this.dom_res=(String)par.get("dom_res");
			if (this.dom_res != null)
			{
			if (this.dom_res.equals("R")) this.dr="Residenza";
			else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
			myLogin lg = new myLogin();
			lg.put(utente,passwd);
			dbc = super.logIn(lg);


			NDOContainer cnt_1 = new NDOContainer();
			NDOContainer cnt_2 = new NDOContainer();
			NDOContainer cnt_3 = new NDOContainer();
			
			String ragSoc = leggiConf(dbc, "ragione_sociale");
			Hashtable h_subTitolo = getSubTit(dbc, par);

			// container x assistiti con contatti
			caricaHashColonne_1(dbc);
			par.put("tipoCnt", new Integer(1));
			gestContainer(dbc, par, cnt_1, getSelect_1(dbc, par), getTit_1(par), h_subTitolo, ragSoc);

			// container x assistiti con solo accessi occasionali(senza contatti)
			caricaHashColonne_2(dbc);
			par.put("tipoCnt", new Integer(2));
			gestContainer(dbc, par, cnt_2, getSelect_2(dbc, par), getTit_2(par), h_subTitolo, ragSoc);

			// container x statistiche: piu' cursori x lo stesso container
			caricaHashColonne_3(dbc);
			par.put("tipoCnt", new Integer(3));
			gestContainerMultiCursor(dbc, par, cnt_3, getArrSelect_3(dbc, par), getTit_3(par), h_subTitolo, ragSoc);

			dbc.close();
			super.close(dbc);
			done=true;
			return stampaNDO(par, cnt_1, cnt_2, cnt_3);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_riepmov()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	private void gestContainer(ISASConnection mydbc, Hashtable par,
												NDOContainer cnt, String select,
												String tit, Hashtable h_subTit, String ragSoc) throws Exception 
	{	
		// set di header, footer e vettoreGroupTitoli
	    preparaLayout(cnt, tit, h_subTit, ragSoc);
	
		// lettura record
		ISASCursor dbcur = mydbc.startCursor(select);
		if (dbcur.getDimension() <= 0) {
			debugMessage("FoRiepMovMedPalEJB.query_riepmov() - cursore di select: vuoto");
			cnt.setSubTitle("NESSUNA INFORMAZIONE REPERITA");
		} else {
			while (dbcur.next()) {
            	ISASRecord dbr = dbcur.getRecord();
				// elabora i dati del record e valorizza le celle del container
                elaboraDati(mydbc, dbr, cnt, par, (Vector)getVettoreIdRiga(dbr, par));
            }

			settaTitColonne(cnt);
			settaTitRighe(mydbc, cnt, par);

			// calcola totali
			cnt.calculate();
		}
		dbcur.close();

		// ordinamento
       	cnt.colSort();
	    cnt.rowSort();
	}

	// x le statistiche si fanno piu' select -> piu' "cnt.put()" per lo stesso container e
	// si calcolano i val medi basandosi sui valori aggregati dal container.
	private void gestContainerMultiCursor(ISASConnection mydbc, Hashtable par,
												NDOContainer cnt, String[] arrSelect,
												String tit, Hashtable h_subTit, String ragSoc) throws Exception 
	{	
		// set di header, footer e vettoreGroupTitoli
	    preparaLayout(cnt, tit, h_subTit, ragSoc);
	
		// lettura record
		boolean almenoUnoPieno = false;
		String base = ((Integer)par.get("tipoCnt")).toString();

		for (int k=0; k<arrSelect.length; k++) {
			par.put("tipoCnt", new Integer(base+k)); // per ottenere base0, base1, ecc.

			ISASCursor dbcur = mydbc.startCursor(arrSelect[k]);	
			if (dbcur.getDimension() > 0) {
				h_Ass.clear();

				while (dbcur.next()) {
	            	ISASRecord dbr = dbcur.getRecord();
					// elabora i dati del record e valorizza le celle del container
    	            elaboraDati(mydbc, dbr, cnt, par, (Vector)getVettoreIdRiga(dbr, par));
        	    }
				almenoUnoPieno = true;
			}
			dbcur.close();
		}

		if (!almenoUnoPieno) {
			debugMessage("FoRiepMovMedPalEJB.query_riepmov() - cursore di select: vuoto");
			cnt.setSubTitle("NESSUNA INFORMAZIONE REPERITA");
		} else {
			// x ogni coppia di col del cnt_3 devo calcolare la media ed 
			// inserirla nella corrispondente colonna "statistica"
			calcolaRapporti(cnt);

			settaTitColonne(cnt);
			settaTitRighe(mydbc, cnt, par);

			// nascondo le celle delle righe tot x le colonne statistiche che non avrebbero significato
			// perche' sommate in automatico dal container. (si deve invocare dopo il "calcolaRapporti()")
			nascondiCelleTotStat(cnt);

			// calcola totali
			cnt.calculate();

			// ordinamento
	       	cnt.colSort();
		    cnt.rowSort();
		}	
	}

	private void preparaLayout(NDOContainer cnt, String tit, Hashtable h_subTit, String ragSoc) throws Exception 
	{
		cnt.setFooter(ragSoc);
		cnt.setHeader(tit);

		String subTitolo = (String)h_subTit.get("subTit");
		Vector vettTitoli = (Vector)h_subTit.get("vTitoli");
		cnt.setSubTitle(subTitolo);
        cnt.setGroupTitles(vettTitoli);
	}

	private String getTit_1(Hashtable par) throws Exception
	{
		String titolo = "RIEPILOGO MOVIMENTI ASSISTITI IN CARICO dalla data: " + getStringDate(par, "data_inizio") + " - " +
                    "alla data: " + getStringDate(par, "data_fine");
		return titolo;
	}

	private String getTit_2(Hashtable par) throws Exception
	{
		String titolo = "RIEPILOGO ASSISTITI CON SOLI ACCESSI OCCASIONALI dalla data: " + getStringDate(par, "data_inizio") + " - " +
                    "alla data: " + getStringDate(par, "data_fine");
		return titolo;
	}

	private String getTit_3(Hashtable par) throws Exception
	{
		String titolo = "RIEPILOGO INDICATORI STATISTICI PER ASSISTITI IN CARICO dalla data: " + getStringDate(par, "data_inizio") + " - " +
                    "alla data: " + getStringDate(par, "data_fine");
		return titolo;
	}

	private Hashtable getSubTit(ISASConnection mydbc, Hashtable par) throws Exception
	{
		Vector vtitoli = new Vector();

        String tipoStampa=(String)par.get("terr");
        StringTokenizer st = new StringTokenizer(tipoStampa,"|");

        String sZona=st.nextToken();
        String sDis=st.nextToken();
        String sCom=st.nextToken();

        String subTitolo="";

        if (sZona.equals("1")){
	    	vtitoli.add("Zona");
            subTitolo = "Zona: " + DecodificaZona(mydbc,(String)par.get("zona"));
        } else
            subTitolo="Zona: Nessuna divisione";

        if (sDis.equals("1")){
        	vtitoli.add("Distretto");
            subTitolo += (" - Distretto: " + DecodificaDistretto(mydbc,(String)par.get("distretto")));
        } else
            subTitolo += " - Distretto: Nessuna divisione";

   	    String ragg =(String)par.get("ragg");
        String tipopca="";
//        if (ragg.equals("A")) {
//        	tipopca = " Area distrettuale ";
//        } else if (ragg.equals("C")) {
//            tipopca = " Comune ";
//        } else if (ragg.equals("P"))
//            tipopca = " Presidio ";
        
        
        if(this.dom_res==null)
        {
        	if (ragg.equals("A")) 
                tipopca = " Area distrettuale ";
             else if (ragg.equals("C")) 
                    tipopca = " Comune ";
             else if (ragg.equals("P"))
                    tipopca = " Presidio ";

        }else if (this.dom_res.equals("D"))
        {
        	if (ragg.equals("A")) 
                tipopca = " Area distrettuale di Domicilio ";
             else if (ragg.equals("C")) 
                    tipopca = " Comune di Domicilio ";
        }else if (this.dom_res.equals("R"))
            {
            	if (ragg.equals("A")) 
                    tipopca = " Area distrettuale di Residenza ";
                 else if (ragg.equals("C")) 
                        tipopca = " Comune di Residenza ";
            } 	        			

        if (sCom.equals("1")){
            vtitoli.add(tipopca);
            subTitolo += (" - " + tipopca+": " + DecodificaLiv3(mydbc,(String)par.get("pca"), ragg));
		} else
            subTitolo += " - " + tipopca + ": Nessuna divisione";
		
		Hashtable h_rit = new Hashtable();
		h_rit.put("subTit", (String)subTitolo);
		h_rit.put("vTitoli", (Vector)vtitoli);
		return h_rit;
    }

	private void caricaHashColonne_1(ISASConnection mydbc) throws Exception 
	{
		hColonne.clear();

		// N.B.: gli spazi in testa alla descr servono x l'ordinamento alfabetico
		// delle colonne: in stampa non si vedono. (piu' spazi -> posizione iniziale)
		Vector vAtt = leggiTabVoci(mydbc, "SPATTI");
		Vector vMot = leggiTabVoci(mydbc, "MCHIUS");
		int numSpazi = 5 + vAtt.size() + vMot.size();

		// totale assititi
	    hColonne.put("TOTASS", aggiungiSpazi("Utenti", numSpazi));
		numSpazi--;
		// num assistiti con valore scala KPS <= 59
	    hColonne.put("KPS", aggiungiSpazi("con K.P.S. fino a 59", numSpazi));
		numSpazi--;
		// num assistiti con i vari tipi del campo "attivazione"
		numSpazi = aggiungiColDaTabVoci(vAtt, hColonne, "ATT_", "Attivaz ", numSpazi);
		// num assistiti gia' in carico all'inizio del periodo di estrazione
	    hColonne.put("CAR_I", aggiungiSpazi("In carico inizio periodo", numSpazi));
		numSpazi--;
		// num assistiti inseriti durante il periodo
	    hColonne.put("CAR_P", aggiungiSpazi("Nuovi immessi nel periodo", numSpazi));
		numSpazi--;
		// num assistiti in carico alla fine del periodo
	    hColonne.put("CAR_F", aggiungiSpazi("In carico fine periodo", numSpazi));
		numSpazi--;
		// num assistiti non piu' in carico alla fine del periodo x i vari tipi del campo "motivo"
		numSpazi = aggiungiColDaTabVoci(vMot, hColonne, "MOT_", "Chiusi nel periodo per ", numSpazi);

//		System.out.println("====>FoRiepMovMedPalEJB: hColonne=["+hColonne.toString()+"]");
	}
	
	private String getSelect_1(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String sel = "SELECT sk.n_cartella," +
							" sk.n_contatto," +
							" sk.skm_data_apertura," +
							" sk.skm_data_chiusura," +
							" sk.skm_attivazione," +
							" sk.skm_motivo_chius," +
		                  	" u.cod_zona, u.des_zona," +
		                  	" u.cod_distretto, u.des_distretto," +
		                  	" u.codice, u.descrizione" +
						" FROM skmedpal sk," +
							" anagra_c ac," +
							" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
			
		String critWhere = getCritWhere_1(dbc, par);
	
		if (!critWhere.trim().equals("")) 
			sel += " WHERE" + critWhere;
	
		sel += " ORDER BY u.des_zona, u.des_distretto, u.descrizione";
		debugMessage("FoRiepMovMedPalEJB.getSelectElenco(): sel 1=[" + sel + "]");

		return sel;
	}

	private String getCritWhere_1(ISASConnection dbc, Hashtable par) throws Exception 
	{
	  	String crit = " sk.skm_data_apertura IN (SELECT MAX(skmedpal.skm_data_apertura)" +
								" FROM skmedpal WHERE skmedpal.n_cartella = sk.n_cartella" + 
								// 23/02/09 ---
								" AND skmedpal.skm_data_apertura <= " + formatDate(dbc, (String)par.get("data_fine")) +
								" AND (skmedpal.skm_data_chiusura >= " + formatDate(dbc, (String)par.get("data_inizio")) +
										  " OR skmedpal.skm_data_chiusura IS NULL)" +
								// 23/02/09 ---
								")" +
						" AND sk.n_cartella = ac.n_cartella" +
				        " AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
								  	" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

/*** 23/02/09: spostato come criteri della selMAX
		// contatti attivi nel periodo
		crit = su.addWhere(crit, su.REL_AND, "sk.skm_data_apertura", su.OP_LE_NUM,
								formatDate(dbc, (String)par.get("data_fine")));

		crit += " AND (sk.skm_data_chiusura >= " + formatDate(dbc, (String)par.get("data_inizio")) +
						  " OR sk.skm_data_chiusura IS NULL)";
***/

		// raggruppamento
		crit += getCritWhereRaggr(dbc, par);

		return crit;
	}

	private String getCritWhereRaggr(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String raggr = (String)par.get("ragg");
		String crit = su.addWhere(" ", su.REL_AND, "u.tipo", su.OP_EQ_STR, raggr);

		crit = su.addWhere(crit, su.REL_AND, "u.cod_zona",
								su.OP_EQ_STR, (String)par.get("zona"));
		crit = su.addWhere(crit, su.REL_AND, "u.cod_distretto",
								su.OP_EQ_STR, (String)par.get("distretto"));
		crit = su.addWhere(crit, su.REL_AND, "u.codice",
								su.OP_EQ_STR, (String)par.get("pca"));

//        if (raggr.equals("C"))
//			crit += " AND (((ac.dom_citta IS NOT NULL OR ac.dom_citta <> '')" +
//                     			" AND ac.dom_citta = u.codice)" +
//                     		" OR ((ac.dom_citta IS NULL OR ac.dom_citta = '')" +
//			                     " AND ac.citta = u.codice))";
//        else if (raggr.equals("A"))
//			crit += " AND (((ac.dom_areadis IS NOT NULL OR ac.dom_areadis <> '')" +
//                     			" AND ac.dom_areadis = u.codice)"+
//                     		" OR ((ac.dom_areadis IS NULL OR ac.dom_areadis = '') " +
//                     			" AND ac.areadis = u.codice))";

        //Aggiunto Controllo Domicilio/Residenza (BYSP)
		if((String)par.get("dom_res") == null)
		{
		        if (raggr.equals("C"))
		          crit += " AND (( (ac.dom_citta IS NOT NULL OR ac.dom_citta <> '')"+
		                " AND u.codice=a.dom_citta)"+
				" OR ( (ac.dom_citta IS NULL OR ac.dom_citta = '') "+
		                " AND u.codice=ac.citta))";
		        else if (raggr.equals("A"))
		          crit += " AND (( (ac.dom_areadis IS NOT NULL OR ac.dom_areadis <> '')"+
		                " AND u.codice=ac.dom_areadis)"+
				" OR ( (ac.dom_areadis IS NULL OR ac.dom_areadis = '') "+
		                " AND u.codice=ac.areadis))";
		}
		else if (((String)par.get("dom_res")).equals("D"))
		                          {
		                           if (raggr.equals("C"))
		          crit += " AND u.codice=ac.dom_citta";
		                            else if (raggr.equals("A"))
		          crit += " AND u.codice=ac.dom_areadis";
		                          }

		else if (((String)par.get("dom_res")).equals("R"))
		                {
		                if (raggr.equals("C"))
		          crit += " AND u.codice=ac.citta";
		        else if (raggr.equals("A"))
		          crit += " AND u.codice=ac.areadis";
		                }
		
		return crit;
	}



	private void caricaHashColonne_2(ISASConnection mydbc) throws Exception 
	{
		hColonne.clear();

		int numSpazi = 1;

		// totale assititi con i soli accessi occasionali
	    hColonne.put("TOTASS", aggiungiSpazi("Utenti", numSpazi));
		numSpazi--;
		// num accessi
	    hColonne.put("NUMACC", aggiungiSpazi("Numero accessi", numSpazi));
	}

	private String getSelect_2(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String sel = "SELECT COUNT(i.int_cartella) numacc," +
							" i.int_cartella," +
		                  	" u.cod_zona, u.des_zona," +
		                  	" u.cod_distretto, u.des_distretto," +
		                  	" u.codice, u.descrizione" +
						" FROM interv i," +
							" anagra_c ac," +
							" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
			
		String critWhere = getCritWhere_2(dbc, par);
	
		if (!critWhere.trim().equals("")) 
			sel += " WHERE" + critWhere;
	
		sel += " GROUP BY int_cartella," +
				" cod_zona, des_zona," +
				" cod_distretto, des_distretto," +
				" codice, descrizione";

		sel += " ORDER BY u.des_zona, u.des_distretto, u.descrizione";
		debugMessage("FoRiepMovMedPalEJB.getSelectElenco(): sel 2=[" + sel + "]");

		return sel;
	}

	private String getCritWhere_2(ISASConnection dbc, Hashtable par) throws Exception 
	{
	  	String crit = " i.int_tipo_oper = '52'" + // oncologo
						" AND i.int_contatto = 0" + // acc occasionali
						" AND i.int_ambdom = 'D'" +
						" AND i.int_cartella = ac.n_cartella" +
				        " AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
								  	" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

		// data accesso compresa nel periodo
		crit = su.addWhere(crit, su.REL_AND, "i.int_data_prest", su.OP_LE_NUM,
								formatDate(dbc, (String)par.get("data_fine")));

		crit = su.addWhere(crit, su.REL_AND, "i.int_data_prest", su.OP_GE_NUM,
								formatDate(dbc, (String)par.get("data_inizio")));

		// assistiti senza contatto
		crit += " AND i.int_cartella NOT IN (SELECT sk.n_cartella FROM skmedpal sk)";

		// raggruppamento
		crit += getCritWhereRaggr(dbc, par);

		return crit;
	}

	private void caricaHashColonne_3(ISASConnection mydbc) throws Exception 
	{
		hColonne.clear();

		int numSpazi = 12;

		// durata media assistenza domiciliare
	    hColonne.put("TOT_0", aggiungiSpazi("Tot. giorni ass. domiciliare", numSpazi));
		numSpazi--;
		hColonne.put("ASS_0", aggiungiSpazi("Tot. assistiti con accessi", numSpazi));
		numSpazi--;
	    hColonne.put("STAT_0", aggiungiSpazi("Media giorni ass. domiciliare per assistito", numSpazi));
		numSpazi--;
		// num gg presa in carico/assistiti
	    hColonne.put("TOT_1", aggiungiSpazi("Tot. giorni di presa in carico", numSpazi));
		numSpazi--;
		hColonne.put("ASS_1", aggiungiSpazi("Tot. assistiti con contatti attivi", numSpazi));
		numSpazi--;
	    hColonne.put("STAT_1", aggiungiSpazi("Media giorni carico per assistito", numSpazi));
		numSpazi--;
		// num medio accessi/assistiti
	    hColonne.put("TOT_2", aggiungiSpazi("Tot. accessi a domicilio", numSpazi));
		numSpazi--;
		hColonne.put("ASS_2", aggiungiSpazi("Tot. assistiti con accessi", numSpazi));
		numSpazi--;
	    hColonne.put("STAT_2", aggiungiSpazi("Media accessi per assistito", numSpazi));
		numSpazi--;
		// % deceduti a domicilio/tot assistiti deceduti
		hColonne.put("TOT_3", aggiungiSpazi("Tot. utenti deceduti a domicilio", numSpazi));
		numSpazi--;
		hColonne.put("ASS_3", aggiungiSpazi("Tot. utenti deceduti", numSpazi));
		numSpazi--;
	    hColonne.put("STAT_3", aggiungiSpazi("Percentuale deceduti a domicilio", numSpazi));
	}

	private String[] getArrSelect_3(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String[] arrSel3 = new String[4];
		arrSel3[0] = getSelect_30(dbc, par);
		arrSel3[1] = getSelect_31(dbc, par);
		arrSel3[2] = getSelect_32(dbc, par);
		arrSel3[3] = getSelect_33(dbc, par);
		return arrSel3;
	}


	private String getSelect_30(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String sel = "SELECT i.int_cartella n_cartella," +
							" MIN(i.int_data_prest) dt_int_min," +
							" MAX(i.int_data_prest) dt_int_max," +
		                  	" u.cod_zona, u.des_zona," +
		                  	" u.cod_distretto, u.des_distretto," +
		                  	" u.codice, u.descrizione" +
						" FROM interv i," +
							" anagra_c ac," +
							" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
			
		String critWhere = getCritWhere_30(dbc, par);
	
		if (!critWhere.trim().equals("")) 
			sel += " WHERE" + critWhere;
	
		sel += " ORDER BY u.des_zona, u.des_distretto, u.descrizione";
		debugMessage("FoRiepMovMedPalEJB.getSelectElenco(): sel 30=[" + sel + "]");

		return sel;
	}

	private String getCritWhere_30(ISASConnection dbc, Hashtable par) throws Exception 
	{
	  	String crit = " i.int_tipo_oper = '52'" + // oncologo
						" AND i.int_contatto <> 0" + // acc standard
						" AND i.int_ambdom = 'D'" +
						" AND i.int_cartella = ac.n_cartella" +
				        " AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
								  	" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

		// raggruppamento
		crit += getCritWhereRaggr(dbc, par);

		crit += " GROUP BY i.int_cartella," +
				" u.cod_zona, u.des_zona,"+
				" u.cod_distretto, u.des_distretto," +
				" u.codice, u.descrizione";

		return crit;
	}



	private String getSelect_31(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String sel = "SELECT sk.n_cartella," +
							" sk.n_contatto," +
							" sk.skm_data_apertura," +
							" sk.skm_data_chiusura," +
		                  	" u.cod_zona, u.des_zona," +
		                  	" u.cod_distretto, u.des_distretto," +
		                  	" u.codice, u.descrizione" +
						" FROM skmedpal sk," +
							" anagra_c ac," +
							" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
			
// 23/02/09		String critWhere = getCritWhere_31(dbc, par);
		// 23/02/09
		String critWhere = getCritWhere_1(dbc, par);
	
		if (!critWhere.trim().equals("")) 
			sel += " WHERE" + critWhere;
	
		sel += " ORDER BY u.des_zona, u.des_distretto, u.descrizione";
		debugMessage("FoRiepMovMedPalEJB.getSelectElenco(): sel 31=[" + sel + "]");

		return sel;
	}

	private String getCritWhere_31(ISASConnection dbc, Hashtable par) throws Exception 
	{
	  	String crit = " sk.n_cartella = ac.n_cartella" +
				        " AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
								  	" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

		// contatti attivi nel periodo
		crit = su.addWhere(crit, su.REL_AND, "sk.skm_data_apertura", su.OP_LE_NUM,
								formatDate(dbc, (String)par.get("data_fine")));

		crit += " AND (sk.skm_data_chiusura >= " + formatDate(dbc, (String)par.get("data_inizio")) +
						  " OR sk.skm_data_chiusura IS NULL)";

		// raggruppamento
		crit += getCritWhereRaggr(dbc, par);

		return crit;
	}

	private String getSelect_32(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String sel = "SELECT i.int_cartella n_cartella," +
		                  	" u.cod_zona, u.des_zona," +
		                  	" u.cod_distretto, u.des_distretto," +
		                  	" u.codice, u.descrizione" +
						" FROM interv i," +
							" anagra_c ac," +
							" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
			
		String critWhere = getCritWhere_32(dbc, par);
	
		if (!critWhere.trim().equals("")) 
			sel += " WHERE" + critWhere;
	
		sel += " ORDER BY u.des_zona, u.des_distretto, u.descrizione";
		debugMessage("FoRiepMovMedPalEJB.getSelectElenco(): sel 32=[" + sel + "]");

		return sel;
	}

	private String getCritWhere_32(ISASConnection dbc, Hashtable par) throws Exception 
	{
	  	String crit = " i.int_tipo_oper = '52'" + // oncologo
						" AND i.int_contatto <> 0" + // acc standard
						" AND i.int_ambdom = 'D'" +
						" AND i.int_cartella = ac.n_cartella" +
				        " AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
								  	" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

		// data accesso compresa nel periodo
		crit = su.addWhere(crit, su.REL_AND, "i.int_data_prest", su.OP_LE_NUM,
								formatDate(dbc, (String)par.get("data_fine")));

		crit = su.addWhere(crit, su.REL_AND, "i.int_data_prest", su.OP_GE_NUM,
								formatDate(dbc, (String)par.get("data_inizio")));

		// raggruppamento
		crit += getCritWhereRaggr(dbc, par);

		return crit;
	}

	private String getSelect_33(ISASConnection dbc, Hashtable par) throws Exception 
	{
		String sel = "SELECT sk.n_cartella," +
							" sk.skm_data_chiusura dt_chiu_sk," +
							" sk.skm_motivo_chius mt_chiu_sk," +
							" sk.skm_deceduto lg_chiu_sk," + // 23/02/09
							" c.data_chiusura dt_chiu_cart," +
							" c.motivo_chiusura mt_chiu_cart," +
		                  	" u.cod_zona, u.des_zona," +
		                  	" u.cod_distretto, u.des_distretto," +
		                  	" u.codice, u.descrizione" +
						" FROM skmedpal sk," +
							" anagra_c ac," +
							" "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u," +
							" cartella c";
			
		String critWhere = getCritWhere_33(dbc, par);
	
		if (!critWhere.trim().equals("")) 
			sel += " WHERE" + critWhere;
	
		sel += " ORDER BY u.des_zona, u.des_distretto, u.descrizione";
		debugMessage("FoRiepMovMedPalEJB.getSelectElenco(): sel 33=[" + sel + "]");

		return sel;
	}

	private String getCritWhere_33(ISASConnection dbc, Hashtable par) throws Exception 
	{
	  	String crit = " c.n_cartella = sk.n_cartella" +
						" AND sk.skm_data_apertura IN (SELECT MAX(skmedpal.skm_data_apertura)" +
									" FROM skmedpal WHERE skmedpal.n_cartella = sk.n_cartella" +
									// 23/02/09 ---
									" AND skmedpal.skm_data_apertura <= " + formatDate(dbc, (String)par.get("data_fine")) +
									" AND (skmedpal.skm_data_chiusura >= " + formatDate(dbc, (String)par.get("data_inizio")) +
											  " OR skmedpal.skm_data_chiusura IS NULL)" +
									// 23/02/09 ---
									")" +
						" AND sk.n_cartella = ac.n_cartella" +
				        " AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
								  	" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";

		// cartelle chiuse nel periodo
		crit = su.addWhere(crit, su.REL_AND, "c.data_chiusura", su.OP_LE_NUM,
								formatDate(dbc, (String)par.get("data_fine")));

		crit = su.addWhere(crit, su.REL_AND, "c.data_chiusura", su.OP_GE_NUM,
								formatDate(dbc, (String)par.get("data_inizio")));

		// con motivo chiusura = 2 (decesso)
		crit = su.addWhere(crit, su.REL_AND, "c.motivo_chiusura", su.OP_EQ_NUM, "2");

		// raggruppamento
		crit += getCritWhereRaggr(dbc, par);

		return crit;
	}



	// costruisco il vettore della catena identificativa della riga
	private Vector getVettoreIdRiga(ISASRecord dbr, Hashtable par) throws Exception 
	{
		//carico in un vettore gli eventuali codici di riga
        Vector vettChainRiga = null;

		// livelli dovuti al raggruppamento territoriale
		String tipoStampa = (String)par.get("terr");
        StringTokenizer st = new StringTokenizer(tipoStampa,"|");
        /*se trovo 0 vuol dire Nessuna divisione-->non devo stampare il livello
         se trovo 1 vuol dire che lo devo stampare
         la prima posizione � la zona
         la seconda posizione � il distretto
         la terza posizione � il comune/Areadis*/
		int iZona = Integer.parseInt(st.nextToken());
        int iDis = Integer.parseInt(st.nextToken());
        int iCom = Integer.parseInt(st.nextToken());

		int dim = iZona+iDis+iCom;

		switch (dim)  {
           	case 0: // voglio solo i totali
               	vettChainRiga = unt.mkPar("TOT");
                break;
            case 1: // solo zone 
                vettChainRiga = unt.mkPar(""+dbr.get("cod_zona"));
                break;
            case 2: // zone e distretti
                vettChainRiga = unt.mkPar(""+dbr.get("cod_zona"),""+dbr.get("cod_distretto"));
                break;
            case 3: // zone, distretti e comune/area distr
                vettChainRiga = unt.mkPar(""+dbr.get("cod_zona"),""+dbr.get("cod_distretto"),""+dbr.get("codice"));
                break;
		}

		return vettChainRiga;
	}


	private void  elaboraDati(ISASConnection dbc, ISASRecord dbr, NDOContainer cnt, Hashtable par, Vector vIdRiga) throws Exception 
	{
		try {
			switch(((Integer)par.get("tipoCnt")).intValue()) {
				case 1:	// container 1: assistiti con contatti
					elaboraDati_1(dbc, dbr, par);
					break;
				case 30:// container 3 - stat 0: media gg assistenza dom
					elaboraDati_30(dbr, par, vIdRiga);
					break;
				case 31:// container 3 - stat 1: media gg presa in carico
					elaboraDati_31(dbr, par, vIdRiga);
					break;
				case 32:// container 3 - stat 2: media accessi
					elaboraDati_32(dbr, par, vIdRiga);
					break;
				case 33:// container 3 - stat 3: media decessi
					elaboraDati_33(dbc, dbr, par);
					break;
			}

          	caricaNDO(vIdRiga, dbr, cnt, par);
        } catch(Exception e) {
			debugMessage("FoRiepMovMedPalEJB.ElaboraDati(): "+e);
			throw new SQLException("Errore eseguendo ElaboraDati()");
		}
	}

	private void  elaboraDati_1(ISASConnection dbc, ISASRecord dbr, Hashtable par) throws Exception
	{
		// ctrl valore scala KPS
		checkScKPS(dbc, dbr, par);
		// ctrl dtApertura con dtInizioPeriodo
		checkDtApertura(dbr, par);
		// ctrl dtChiusura con dtFinePeriodo ed eventuale motivo chius
		checkDtChiusura(dbr, par);
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> elaboraDati_1");
	}

	private void  elaboraDati_30(ISASRecord dbr, Hashtable par, Vector vIdRiga) throws Exception
	{
		// conto gg in assistenza domiciliare
		contaGgAssDom(dbr, par);
		// costruisco chiave x ass
		faiChiaveAss(dbr,vIdRiga);
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> elaboraDati_30");
	}

	private void  elaboraDati_31(ISASRecord dbr, Hashtable par, Vector vIdRiga) throws Exception
	{
		// conto gg in carico
		contaGgCarico(dbr, par);
		// costruisco chiave x ass
		faiChiaveAss(dbr,vIdRiga);
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> elaboraDati_31");
	}

	private void  elaboraDati_32(ISASRecord dbr, Hashtable par, Vector vIdRiga) throws Exception
	{
		// costruisco chiave x ass
		faiChiaveAss(dbr,vIdRiga);
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> elaboraDati_32");
	}

	private void  elaboraDati_33(ISASConnection dbc, ISASRecord dbr, Hashtable par) throws Exception
	{
		// conto decessi a domicilio
// 23/02/09		checkChiusCont(dbc, dbr, par);
		// 23/02/09
		checkChiusDecDom(dbc, dbr, par);
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> elaboraDati_33");
	}


	private void caricaNDO(Vector vIdRiga, ISASRecord dbr,
    	            					NDOContainer cnt, Hashtable par) throws SQLException
	{
        try{
			switch(((Integer)par.get("tipoCnt")).intValue()) {
				case 1:	// container 1: assistiti con contatti
					caricaNDO_1(vIdRiga, dbr, cnt);
					break;
				case 2: // container 2: assistiti con solo accessi occasionali
					caricaNDO_2(vIdRiga, dbr, cnt);
					break;
				case 30: // container 3: statistica 0
					caricaNDO_30(vIdRiga, dbr, cnt);
					break;
				case 31: // container 3: statistica 1
					caricaNDO_31(vIdRiga, dbr, cnt);
					break;
				case 32: // container 3: statistica 2
					caricaNDO_32(vIdRiga, dbr, cnt);
					break;
				case 33: // container 3: statistica 3
					caricaNDO_33(vIdRiga, dbr, cnt);
					break;
			}
        } catch(Exception e) {
			debugMessage("FoRiepMovMedPalEJB.caricaNDO(): "+e);
			throw new SQLException("Errore eseguendo caricaNDO()");
        }
	}

	private void caricaNDO_1(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		// totale assititi
	    cnt.put(vIdRiga, unt.mkPar("TOTASS"), new Integer(1));
	
		// num assistiti con valore scala KPS <= 59
		Integer valScKPS = (Integer)dbr.get("kps_valore");
		if ((valScKPS != null) && (valScKPS.intValue() <= 59))
		  	cnt.put(vIdRiga, unt.mkPar("KPS"), new Integer(1));
	
		// num assistiti con i vari tipi del campo "attivazione"
		String attivaz = (String)dbr.get("skm_attivazione");
		cnt.put(vIdRiga, unt.mkPar("ATT_"+attivaz), new Integer(1));
	
		// num assistiti gia' in carico all'inizio del periodo di estrazione
		// oppure inseriti nuovi durante il periodo
		String inCaricoIni = (String)dbr.get("inCaricoIni");
	    cnt.put(vIdRiga, unt.mkPar(inCaricoIni), new Integer(1));
	
		// num assistiti in carico alla fine del periodo 
		// oppure non piu' in carico x vari tipi di motivazione
		String inCaricoFin = (String)dbr.get("inCaricoFin");
	    cnt.put(vIdRiga, unt.mkPar(inCaricoFin), new Integer(1));
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_1");
	}

	private void caricaNDO_2(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		// totale assititi
	    cnt.put(vIdRiga, unt.mkPar("TOTASS"), new Integer(1));	
		// totale accessi
	    cnt.put(vIdRiga, unt.mkPar("NUMACC"), new Integer(convNumDBToInt("numacc", dbr)));
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_2");
	}

	private void caricaNDO_30(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		// gg assistenza domiciliare
		Double ggAssDom = new Double((String)dbr.get("ggAssDom"));
		if (ggAssDom.doubleValue() > 0)
			cnt.put(vIdRiga, unt.mkPar("TOT_0"), ggAssDom);
		// assistiti
		String keyAss = (String)dbr.get("keyAss");
		if ((!h_Ass.containsKey(keyAss)) && (ggAssDom.doubleValue() > 0)) {
			h_Ass.put(keyAss, "-");
			cnt.put(vIdRiga, unt.mkPar("ASS_0"), new Double(1));
		}

//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_30");
	}

	private void caricaNDO_31(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		// gg presa in carico
		cnt.put(vIdRiga, unt.mkPar("TOT_1"), new Double((String)dbr.get("ggCarico")));
		// assistiti
		String keyAss = (String)dbr.get("keyAss");
		if (!h_Ass.containsKey(keyAss)) {
			h_Ass.put(keyAss, "-");
			cnt.put(vIdRiga, unt.mkPar("ASS_1"), new Double(1));
		}
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_31");
	}

	private void caricaNDO_32(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		// num accessi
		cnt.put(vIdRiga, unt.mkPar("TOT_2"), new Double(1));
		// assistiti
		String keyAss = (String)dbr.get("keyAss");
		if (!h_Ass.containsKey(keyAss)) {
			h_Ass.put(keyAss, "-");
			cnt.put(vIdRiga, unt.mkPar("ASS_2"), new Double(1));
		}
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_32");
	}

/*** 23/02/09
	private void caricaNDO_33(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		// deceduti a domicilio
		String isDecDom = (String)dbr.get("decDom");
		if ((isDecDom != null) && (isDecDom.trim().equals("S")))
			cnt.put(vIdRiga, unt.mkPar("TOT_3"), new Double(1));
		//  deceduti 
		cnt.put(vIdRiga, unt.mkPar("ASS_3"), new Double(1));
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_33");
	}
***/

	// 23/02/09
	private void caricaNDO_33(Vector vIdRiga, ISASRecord dbr, NDOContainer cnt) throws Exception
	{
		String isDecDom = (String)dbr.get("decDom");
		if (isDecDom != null) {
			//  deceduti 
			if ((isDecDom.trim().equals("1")) || (isDecDom.trim().equals("2")))
				cnt.put(vIdRiga, unt.mkPar("ASS_3"), new Double(1));

			// deceduti a domicilio			
			if (isDecDom.trim().equals("2"))
				cnt.put(vIdRiga, unt.mkPar("TOT_3"), new Double(1));
		}
//		debugMessage(" *************** ************ FoRiepMovMedPalEJB -> caricaNDO_33");
	}




	private void settaTitColonne(NDOContainer cnt) throws Exception 
	{
		Vector vettVettCodCol = cnt.getColKeyTitle();
        for (int i=0; i<vettVettCodCol.size(); i++) {
	        Vector vCodCol = (Vector)vettVettCodCol.elementAt(i);
			String codCol = (String)vCodCol.lastElement();
            String titCol = (String)hColonne.get(codCol);
            cnt.setColTitle(vCodCol, (titCol!=null?titCol:"NON SPECIFICATO"));
        }
	}

	private void settaTitRighe(ISASConnection mydbc, NDOContainer cnt, Hashtable par) throws Exception 
	{
		Vector vettVettCodRow = cnt.getRowKeyTitle();
        for (int i=0; i<vettVettCodRow.size(); i++) {
	        Vector vCodRow = (Vector)vettVettCodRow.elementAt(i);
			String codRow = (String)vCodRow.lastElement();
			String titRow = "NON SPECIFICATO";
			int dim = vCodRow.size();
			switch (dim)  {
            	case 0://voglio solo i totali
                	titRow = "Totali generali";
                    break;
                case 1:
                    titRow = DecodificaZona(mydbc, codRow);
                    break;
                case 2:
                    titRow = DecodificaDistretto(mydbc, codRow);
                    break;
                case 3:
                    titRow = DecodificaLiv3(mydbc, codRow, (String)par.get("ragg"));
                    break;
			}
            cnt.setRowTitle(vCodRow, titRow);
        }
	} 

    private byte[] stampaNDO(Hashtable par, NDOContainer cnt_1, NDOContainer cnt_2, NDOContainer cnt_3) throws Exception
	{
        try {
     	    NDOPrinter prt = new NDOPrinter();

            String tipoStampa=(String)par.get("terr");
            StringTokenizer st = new StringTokenizer(tipoStampa,"|");

            String sZona=st.nextToken();
            String sDis=st.nextToken();
            String sCom=st.nextToken();
            int iZona=Integer.parseInt(sZona);
            int iDis=Integer.parseInt(sDis);
            int iCom=Integer.parseInt(sCom);

            int iLivello=(iZona+iDis+iCom)-1;
            if(iLivello>=0){
       			prt.addContainer(cnt_1,true,false,iLivello,0,true,false);
				prt.addContainer(cnt_2,true,false,iLivello,0,true,false);
				prt.addContainer(cnt_3,true,false,iLivello,0,true,false);
            } else {
                prt.addContainer(cnt_1,true,false,0,0,false,false);
                prt.addContainer(cnt_2,true,false,0,0,false,false);
                prt.addContainer(cnt_3,true,false,0,0,false,false);
			}
			String formato = (String)par.get("formato");
            return prt.getDocument(Integer.parseInt(formato));
      	} catch(Exception e) {
	    	debugMessage("FoSingolaProfEJB.StampaNDO(): "+e);
	    	throw new SQLException("Errore eseguendo StampaNDO()");
      	}
	}

	// per ogni riga, calcola i valori medi definiti 
	// come valoreCellaColonna1/valoreCellaColonna2
	// e li inserisce in una nuova terza colonna.
	private void calcolaRapporti(NDOContainer cnt) throws Exception
	{
		Vector vettVettCodRow = cnt.getRowKeyTitle();

		for (int i=0; i<vettVettCodRow.size(); i++) {
	        Vector vCodRow = (Vector)vettVettCodRow.elementAt(i);

			// 4 statistiche
			for (int j=0; j<4; j++) {
				String col1 = "TOT_"+j;
				String col2 = "ASS_"+j;

				// se nel container, le col sono state valorizzate 
				Vector vettCol1 = getVettColonnaVal(cnt,col1);
				Vector vettCol2 = getVettColonnaVal(cnt,col2);

				if ((vettCol1 != null) && (vettCol2 != null)) { 
					// si prendono i valori delle celle dal container
					Object valCella1 = (Object)cnt.get(vCodRow, vettCol1);
					Object valCella2 = (Object)cnt.get(vCodRow, vettCol2);
					if ((valCella1 != null) && (valCella2 != null)) {
						Double valD = calcolaRapp(valCella1, valCella2);	

						if (j==3)// x la stat 3 vogliono la percentuale
							valD = new Double((valD.doubleValue()*100));						
						cnt.put(vCodRow, unt.mkPar("STAT_"+j), valD);
					}
				} 	
			}
		}		
	}

	// se la colonna e' stata valorizzata nel container,
	// restituisce il vettore dei codColonna 
	private Vector getVettColonnaVal(NDOContainer cnt, String col) throws Exception
	{
		Vector vCodCol = null;
		Vector vettVettCodCol = cnt.getColKeyTitle();
		boolean trovata = false;
		int k = 0;
		while ((k<vettVettCodCol.size()) && (!trovata)) {
	        vCodCol = (Vector)vettVettCodCol.elementAt(k);
			String codCol = (String)vCodCol.lastElement();
			trovata = (codCol.trim().equals(col.trim()));
			k++;
		}
		if (trovata)
			return vCodCol;	
		else 
			return null;
	}

	private Double calcolaRapp(Object obj1, Object obj2)  throws Exception
	{
		double risu = 0.0;
		double num1 = Double.parseDouble(obj1.toString());
		double num2 = Double.parseDouble(obj2.toString());
		if (num2 > 0)
			risu = num1/num2;
//		debugMessage(" FoRiepMovMedPalEJB -> calcolaRapp - num1=["+num1+"] - num2=["+num2+"] - risu=["+risu+"]");
		return (new Double (risu));
	}

	// metodo da invocare dopo aver fatto la put anche delle col "statistiche"
	private void nascondiCelleTotStat(NDOContainer cnt) throws Exception
	{
		Vector vCodCol = null;
		// 4 statistiche
		for (int j=0; j<4; j++) {
			String col = "STAT_"+j;
			// se nel container, la col � stata valorizzata
			vCodCol = getVettColonnaVal(cnt,col);
			if (vCodCol != null)
				cnt.hideColTotal(vCodCol);
		}
	}

	// chiave x contare gli assistiti 1 sola volta per ogni livello
	// key= [codZona][|][codDistr][|][codComune/Area][|]n_cartella
	private void faiChiaveAss(ISASRecord mydbr, Vector vIdRiga) throws Exception
	{
		String keyAss = "";
		Enumeration enu = vIdRiga.elements();
		while (enu.hasMoreElements())
			keyAss += (enu.nextElement() + "|");
		keyAss += mydbr.get("n_cartella");
		mydbr.put("keyAss", keyAss);
	}






	private int aggiungiColDaTabVoci(Vector vett, Hashtable hCol, String cod, String desc, int numSp) throws Exception
	{
		if ((vett != null) && (vett.size() > 0)){
			for (int k=0; k<vett.size(); k++){
				ISASRecord dbrTV = (ISASRecord)vett.elementAt(k);
				hCol.put(cod+(String)dbrTV.get("tab_val"), 
								aggiungiSpazi(desc+(String)dbrTV.get("tab_descrizione"), numSp));
				numSp--;
			}
		}
		return numSp;
	}

	private Vector leggiTabVoci(ISASConnection mydbc, String cod) throws Exception
	{
		ISASCursor dbcur = null;
		boolean done = false;

		try {
			String sel = "SELECT * FROM tab_voci" +
					" WHERE tab_cod = '" + cod + "'" +
					" AND tab_val <> '#'";
	
			dbcur = mydbc.startCursor(sel);		
			Vector vett = dbcur.getAllRecord();	
            if (dbcur != null)
	            dbcur.close();
	        done = true;
			return vett;
		} catch(Exception e1){
			throw new SQLException("\n Errore eseguendo una leggiTabVoci() - "+  e1);
       	}
      	finally {
        	if (!done) {
				try {
					if (dbcur != null)
                    	dbcur.close();
				} catch(Exception e2){
					debugMessage(""+e2);
				}
			}
      	}
	}

	// leggo il valore della scala KPS con dataMax(<= dtFinePeriodo)
	private void checkScKPS(ISASConnection mydbc, ISASRecord mydbr, Hashtable par) throws Exception
	{
		int kps_valore = 100;
 
		String sel = "SELECT k.kps_valore FROM sc_kps k" +	
					" WHERE k.n_cartella = " + mydbr.get("n_cartella") +
					" AND k.data IN (SELECT MAX(sc_kps.data) FROM sc_kps" +
						" WHERE sc_kps.n_cartella = k.n_cartella" +
						" AND sc_kps.data <=" + formatDate(mydbc, (String)par.get("data_fine")) + ")";
		
		ISASRecord dbrK = mydbc.readRecord(sel);
		if ((dbrK != null) && ((Integer)dbrK.get("kps_valore") != null))
			kps_valore = ((Integer)dbrK.get("kps_valore")).intValue();

		mydbr.put("kps_valore", new Integer(kps_valore));
	}

	// ctrl se dataApertura contatto < dataInizio periodo estrazione
	private void checkDtApertura(ISASRecord mydbr, Hashtable par) throws Exception
	{
		DataWI dtApertura = new DataWI((java.sql.Date)mydbr.get("skm_data_apertura"));
		StringBuffer strBufInizio = new StringBuffer((String)par.get("data_inizio"));
		int pos = -1;
		while ((pos=((String)strBufInizio.toString()).indexOf("-")) != -1)
			strBufInizio.replace(pos, pos+1, "");
		if (dtApertura.isPrecedente(strBufInizio.toString()))
			mydbr.put("inCaricoIni", "CAR_I");
		else
			mydbr.put("inCaricoIni", "CAR_P");
	}

	// ctrl se dataChiusura contatto >= dataFine periodo estrazione
	private void checkDtChiusura(ISASRecord mydbr, Hashtable par) throws Exception
	{
		if (mydbr.get("skm_data_chiusura") == null) // non chiuso
			mydbr.put("inCaricoFin", "CAR_F");
		else {
			DataWI dtChiusura = new DataWI((java.sql.Date)mydbr.get("skm_data_chiusura"));
			StringBuffer strBufFine = new StringBuffer((String)par.get("data_fine"));
			int pos = -1;
			while ((pos=((String)strBufFine.toString()).indexOf("-")) != -1)
				strBufFine.replace(pos, pos+1, "");
			if (dtChiusura.isUguOSucc(strBufFine.toString())) // chiuso dopo
				mydbr.put("inCaricoFin", "CAR_F");
			else { // contatto chiuso nel periodo -> cerco motivo
				String motivo = (String)mydbr.get("skm_motivo_chius");
				mydbr.put("inCaricoFin", "MOT_"+motivo);
			}
		}
	}

	// conto gg in assistenza domiciliare:
	// a)per gli assistiti con 1 solo accesso:
	//		a1)se dataInterv compresa nel periodo -> si conta 1 giorno
	//		a2)altrimenti -> 0.
	// b)per gli assistiti con almeno 2 accessi: 
	//		b1)se almeno 1 ricade nel periodo -> come differenza tra MIN(dtUltimoAcc,dtFinePeriodo) e MAX(dtPrimoAcc,dtIniPeriodo)
	//		b2)se 1 e' precedente l'iniPeriodo e l'altro e' successivo la finePeriodo ->come differenza tra dtFinePeriodo e dtIniPeriodo
	//		b3)altrimenti -> 0.
	private void contaGgAssDom(ISASRecord mydbr, Hashtable par) throws Exception
	{
		DataWI dtF = null;
		DataWI dtI = null;
		int gg = 0;

		DataWI dtAccMin = new DataWI((java.sql.Date)mydbr.get("dt_int_min"));
		DataWI dtAccMax = new DataWI((java.sql.Date)mydbr.get("dt_int_max"));
//		debugMessage(" FoRiepMovMedPalEJB -> contaGgAssDom 1 - dtAccMin=["+dtAccMin.getString(0)+"] - dtAccMax=["+dtAccMax.getString(0)+"]");

		StringBuffer strBufInizio = new StringBuffer((String)par.get("data_inizio"));
		int pos = -1;
		while ((pos=((String)strBufInizio.toString()).indexOf("-")) != -1)
			strBufInizio.replace(pos, pos+1, "");
		String dtIniPeriodo = strBufInizio.toString();

		StringBuffer strBufFine = new StringBuffer((String)par.get("data_fine"));
		pos = -1;
		while ((pos=((String)strBufFine.toString()).indexOf("-")) != -1)
			strBufFine.replace(pos, pos+1, "");
		String dtFinPeriodo = strBufFine.toString();

		boolean isDtAccMin_P = dtAccMin.isPrecedente(dtIniPeriodo); // dtAcc MIN precedente l'iniPeriodo
		boolean isDtAccMin_C = (dtAccMin.isUguOSucc(dtIniPeriodo)) 
								&& (dtAccMin.isUguOPrec(dtFinPeriodo)); // dtAcc MIN compresa nel periodo
		boolean isDtAccMin_S = dtAccMin.isSuccessiva(dtFinPeriodo); // dtAcc MIN successiva la finPeriodo

		boolean isDtAccMax_P = dtAccMax.isPrecedente(dtIniPeriodo); // dtAcc MAX precedente l'iniPeriodo
		boolean isDtAccMax_C = (dtAccMax.isUguOSucc(dtIniPeriodo)) 
								&& (dtAccMax.isUguOPrec(dtFinPeriodo)); // dtAcc MAX compresa nel periodo
		boolean isDtAccMax_S = dtAccMax.isSuccessiva(dtFinPeriodo); // dtAcc MAX successiva la finPeriodo

		if (dtAccMin.isUguale(dtAccMax.getString(1))) { // a) se 1 solo accesso
			// a1) se compreso nel periodo
			if (isDtAccMin_C)
				gg = 1;
			else // a2) esterno al periodo
				gg = 0;
		} else { // b) almeno 2 accessi
			// b1) almeno 1 ricade nel periodo
			if ((isDtAccMin_P && isDtAccMax_C) || (isDtAccMin_C && isDtAccMax_S) || (isDtAccMin_C && isDtAccMax_C)) {
				if (isDtAccMin_P)
					dtI = new DataWI(dtIniPeriodo,1);
				else 
					dtI = dtAccMin;

				if (isDtAccMax_S)
					dtF = new DataWI(dtFinPeriodo,1);
				else
					dtF = dtAccMax;

				if ((dtF != null) && (dtI != null))
					gg = dtF.contaGgOggiValeUno(dtI.getString(1));	
//				debugMessage(" FoRiepMovMedPalEJB -> contaGgAssDom 2 - dtF=["+dtF.getString(0)+"] - dtI=["+dtI.getString(0)+"] - ggAssDom=["+gg+"]");
			} else if (isDtAccMin_P && isDtAccMax_S) { // b2)1 precedente e l'altro successivo
				dtI = new DataWI(dtIniPeriodo,1);
				dtF = new DataWI(dtFinPeriodo,1);

				if ((dtF != null) && (dtI != null))
					gg = dtF.contaGgOggiValeUno(dtI.getString(1));	
//				debugMessage(" FoRiepMovMedPalEJB -> contaGgAssDom 3 - dtF=["+dtF.getString(0)+"] - dtI=["+dtI.getString(0)+"] - ggAssDom=["+gg+"]");
			} else // b3) entrambi esterni al periodo
				gg = 0;
		}
		
		mydbr.put("ggAssDom", "" + gg);
	}

	// conto gg in carico come differenza tra MIN(dtChiusCont,dtFinePeriodo) e MAX(dtCont,dtIniPeriodo)
	private void contaGgCarico(ISASRecord mydbr, Hashtable par) throws Exception
	{
		DataWI dtF = null;
		DataWI dtI = null;
		int gg = 0;

		DataWI dtApertura = new DataWI((java.sql.Date)mydbr.get("skm_data_apertura"));
		StringBuffer strBufInizio = new StringBuffer((String)par.get("data_inizio"));
		int pos = -1;
		while ((pos=((String)strBufInizio.toString()).indexOf("-")) != -1)
			strBufInizio.replace(pos, pos+1, "");
		if (dtApertura.isPrecedente(strBufInizio.toString()))
			dtI = new DataWI(strBufInizio.toString(),1);
		else
			dtI = dtApertura;
		
		StringBuffer strBufFine = new StringBuffer((String)par.get("data_fine"));
		pos = -1;
		while ((pos=((String)strBufFine.toString()).indexOf("-")) != -1)
			strBufFine.replace(pos, pos+1, "");
		if (mydbr.get("skm_data_chiusura") == null) // non chiuso
			dtF = new DataWI(strBufFine.toString(),1);
		else {			
			DataWI dtChiusura = new DataWI((java.sql.Date)mydbr.get("skm_data_chiusura"));
			if (dtChiusura.isUguOSucc(strBufFine.toString())) // chiuso dopo
				dtF = new DataWI(strBufFine.toString(),1);
			else  // contatto chiuso nel periodo
				dtF = dtChiusura;
		}
		if ((dtF != null) && (dtI != null))
			gg = dtF.contaGgOggiValeUno(dtI.getString(1));		
		mydbr.put("ggCarico", "" + gg);
//		debugMessage(" FoRiepMovMedPalEJB -> contaGgCarico 1 - dtApert=["+dtApertura.getString(0)+"] - dtChiu=["+mydbr.get("skm_data_chiusura")+"]");
//		debugMessage(" FoRiepMovMedPalEJB -> contaGgCarico 2 - dtF=["+dtF.getString(0)+"] - dtI=["+dtI.getString(0)+"] - ggCarico=["+gg+"]");
	}

/*** 23/02/09
	// ctrl se motivoChiusura contatto = decesso 
	// (siccome e' tabellato su TABVOCI devo cercare il codice relativo alla descr=decesso)
	private void checkChiusCont(ISASConnection mydbc, ISASRecord mydbr, Hashtable par) throws Exception
	{
		if (mydbr.get("mt_chiu_sk") == null) // non chiuso
			mydbr.put("decDom", "N");
		else {
			String motChiu = "" + mydbr.get("mt_chiu_sk");
			Vector vMot = leggiTabVoci(mydbc, "MCHIUS");
			String codDeces = null;
			boolean trovato = false;
			int k = 0;

			while ((k<vMot.size()) && (!trovato)) {
				ISASRecord dbrTV = (ISASRecord)vMot.elementAt(k);
				String decDeces = ((String)dbrTV.get("tab_descrizione")).toUpperCase();
				trovato = (decDeces.indexOf("DECE") != -1);
				if (trovato)
					codDeces = "" + dbrTV.get("tab_val");
				k++;
			}			

			if ((codDeces != null) && (codDeces.trim().equals(motChiu))) 
				mydbr.put("decDom", "S");
			else
				mydbr.put("decDom", "N");
		}
	}
***/

	// 23/02/09: ctrl motChiusContatto e, se decesso, luogoDecesso
	// (siccome sono tabellati su TABVOCI devo cercare il codice relativo alla descr=decesso e alla descr=domicilio)
	private void checkChiusDecDom(ISASConnection mydbc, ISASRecord mydbr, Hashtable par) throws Exception
	{
		if (mydbr.get("mt_chiu_sk") == null) // non chiuso
			mydbr.put("decDom", "0");
		else {
			String motChiu = "" + mydbr.get("mt_chiu_sk");
			String luoChiu = "" + mydbr.get("lg_chiu_sk");
			if (vMotDec == null)
				vMotDec = leggiTabVoci(mydbc, "MCHIUS");
			if (vLuoDec == null)
				vLuoDec = leggiTabVoci(mydbc, "LUODEC");
			String codMotDec = null;
			String codLuoDec = null;
			boolean trovato = false;
			int k = 0;
	
			// cerco record su TAB_VOCI corrispondente a "DECESSO"
			while ((k<vMotDec.size()) && (!trovato)) {
				ISASRecord dbrTV = (ISASRecord)vMotDec.elementAt(k);
				String decMDeces = ((String)dbrTV.get("tab_descrizione")).toUpperCase();
				trovato = (decMDeces.indexOf("DECE") != -1);
				if (trovato)
					codMotDec = "" + dbrTV.get("tab_val");
				k++;
			}

			// se motChiu = decesso
			if ((codMotDec != null) && (codMotDec.trim().equals(motChiu))) {
				mydbr.put("decDom", "1");

				// cerco record su TAB_VOCI corrispondente a "DECEDUTO AL DOMICILIO"
				trovato = false;
				k = 0;
				while ((k<vLuoDec.size()) && (!trovato)) {
					ISASRecord dbrTV_1 = (ISASRecord)vLuoDec.elementAt(k);
					String decLDeces = ((String)dbrTV_1.get("tab_descrizione")).toUpperCase();
					trovato = (decLDeces.indexOf("DOMICILI") != -1);
					if (trovato)
						codLuoDec = "" + dbrTV_1.get("tab_val");
					k++;
				}	

				// se luogo decesso = domicilio
				if ((codLuoDec != null) && (codLuoDec.trim().equals(luoChiu)))
					mydbr.put("decDom", "2");
			}else
				mydbr.put("decDom", "0");			
		}
	}


	private String aggiungiSpazi(String str_old, int num)
	{
		String str_new = str_old;
		for (int k=0; k<num; k++)
			str_new = " " + str_new;
		return str_new;
	}
	



private String DecodificaLiv3(ISASConnection dbc,String pca,String ragg){
  String ret = "";
  try {
    if (pca!=null){
      if (pca.equals("") && !ragg.equals("A"))
        ret = " TUTTI";
      else if (pca.equals("") && ragg.equals("A"))
        ret = " TUTTE";
      else{
        ISASRecord dbr = null;
        String sel = "";
        if (ragg.equals("P")){
          sel = "SELECT despres des FROM presidi WHERE"+
            " codpres='"+pca+"' AND codreg='"+codice_regione+
            "' AND codazsan='"+codice_usl+"'";
          dbr = dbc.readRecord(sel);
        }else if (ragg.equals("C")){
          sel = "SELECT descrizione des FROM comuni WHERE"+
            " codice='"+pca+"'";
          dbr = dbc.readRecord(sel);
        }else if (ragg.equals("A")){
          sel = "SELECT descrizione des FROM areadis WHERE"+
            " codice='"+pca+"'";
          dbr = dbc.readRecord(sel);
        }
        if (dbr!=null && dbr.get("des")!=null)
          ret = (String)dbr.get("des");
      }
     }
    }catch (Exception ex) {}
    return ret;
}

private String DecodificaZona(ISASConnection dbc,String zona){
  String ret = "";
  try {
    if (zona!=null){
      if (zona.equals(""))
        ret = "TUTTE";
      else{
        String sel = "SELECT descrizione_zona FROM zone WHERE"+
          " codice_zona='"+zona+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("descrizione_zona")!=null)
          ret = (String)dbr.get("descrizione_zona");
      }
    }
  } catch (Exception ex) { }
  return ret;
}

private String DecodificaDistretto(ISASConnection dbc,String distr) {
  String ret = "";
  try {
    if (distr!=null){
      if (distr.equals(""))
        ret = "TUTTI";
      else{
        String sel = "SELECT des_distr FROM distretti WHERE"+
          " cod_distr='"+distr+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("des_distr")!=null)
          ret = (String)dbr.get("des_distr");
      }
    }
  }catch (Exception ex) {}
  return ret;
}

/**
* restituisce un parametro data come stringa nel formato gg/mm/aaaa
*/
private String getStringDate(Hashtable par, String k) {
	try {
		String s = (String)par.get(k);
		s = s.substring(8,10)+"/"+s.substring(5,7)+"/"+s.substring(0,4);
		return s;
	} catch(Exception e) {
		debugMessage("getStringDate("+par+", "+k+"): "+e);
		return "";
	}
}

public int ConvertData (String dataold,String datanew){
        //inizializzazione della variabile eta
        int eta=0;
        int tempeta=0;

        //preparazione primo array
        int[] datavecchia= new int[3];
        Integer giorno = new Integer(dataold.substring(8,10));
        datavecchia[0]= giorno.intValue();
        Integer mese = new Integer(dataold.substring(5,7));
        datavecchia[1]= mese.intValue();
        Integer anno = new Integer(dataold.substring(0,4));
         datavecchia[2]= anno.intValue();

        //preparazione secondo array

        int[] datanuova= new int[3];
        Integer day = new Integer(datanew.substring(8,10));
        datanuova[0]= day.intValue();
        Integer mounth = new Integer(datanew.substring(5,7));
        datanuova[1]= mounth.intValue();
         Integer year = new Integer(datanew.substring(0,4));
        datanuova[2]= year.intValue();

        tempeta= datanuova[2]-datavecchia[2];
        //confronto mese
        if (datanuova[1] < datavecchia[1])
                tempeta=tempeta-1;      // anni non ancora compiuti
        else if (datanuova[1] == datavecchia[1])
                 if (datanuova[0] < datavecchia[0])      //confronto giorno
                        tempeta=tempeta-1;      // anni non ancora compiuti
        eta=tempeta;
        return eta;
}

public static String getjdbcDate(){
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}


	private String leggiConf(ISASConnection mydbc, String cod) throws Exception
	{
		String desc = "";

		String selConf = "SELECT conf_txt" +
						" FROM conf" +
						" WHERE conf_kproc = 'SINS'" +
						" AND conf_key = '" + cod + "'";

		ISASRecord dbrDec = mydbc.readRecord(selConf);
		if (dbrDec != null)
			if (dbrDec.get("conf_txt") != null)
				desc = (String)dbrDec.get("conf_txt");

		return desc;
	}// END leggiConf

 private int convNumDBToInt(String nomeCampo, ISASRecord mydbr)throws Exception
 {
      int numero = 0;
      Object numDB = (Object)mydbr.get(nomeCampo);
      if (numDB != null) {
       if (numDB.getClass().getName().endsWith("Double"))
        numero = ((Double)mydbr.get(nomeCampo)).intValue();
       else if (numDB.getClass().getName().endsWith("Integer"))
        numero = ((Integer)mydbr.get(nomeCampo)).intValue();
      }
      return numero;
 }// END convNumDBToInt

}	// End of FoRiepMovMedPal class

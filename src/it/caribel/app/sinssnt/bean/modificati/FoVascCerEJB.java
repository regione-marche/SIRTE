package it.caribel.app.sinssnt.bean.modificati;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 15/11/2002 - EJB di connessione alla procedura SINS Tabella FoVascCer
//
// Jessica Caccavale
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;

import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.swing2.util.utils;
import it.pisa.caribel.merge.*;	// fo merge

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class FoVascCerEJB extends SINSSNTConnectionEJB {

	String dom_res;
	String Patologia_html;
	String dr;
	private String MIONOME = "4-FoVascCerEJB.";
	// 28/11/06 m._ aggiunto operatore ONCOLOGO (tipo=52).
	// 27/11/06 m.: sostituito campi "patologie" con "diagnosi" -> NON esiste piu'
	// il criterio sul "n_contatto" -> modificato SELECT.

public FoVascCerEJB() {}

private void preparaLayout(mergeDocument md, ISASConnection dbc,String data_ini,String data_fine,
String cod1,String cod2,String ass,String codZona) {
	Hashtable htxt = new Hashtable();
	try {
		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ragione_sociale'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
	} catch (Exception ex) {
		htxt.put("#txt#", "ragione_sociale");
	}
        ServerUtility su =new ServerUtility();
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        //DATE
        data_ini=data_ini.substring(8,10)+"/"+data_ini.substring(5,7)+"/"+
                 data_ini.substring(0,4);
        data_fine=data_fine.substring(8,10)+"/"+data_fine.substring(5,7)+"/"+
                 data_fine.substring(0,4);
/*        String desc1=decodifica("icd9","cd_diag",cod1,"diagnosi",dbc);
        String desc2=decodifica("icd9","cd_diag",cod2,"diagnosi",dbc);*/
//        htxt.put("#patologia#",desc+" ("+cod+")");
        htxt.put("#data_ini#",data_ini);
        htxt.put("#data_fine#",data_fine);
        String operatore = "";
        String descrZona = "";
	if (ass!=null && !ass.equals("")){
                /*int tipo=(new Integer(ass)).intValue();
                switch (tipo){
                  case 1:
                	   	operatore = "ASSISTENTI SOCIALI";
//                        htxt.put("#tipo#","ASSISTENTI SOCIALI");
                        break;
                   case 2:
                	   operatore ="INFERMIERI";
//                        htxt.put("#tipo#","INFERMIERI");
                        break;
                   case 3:
                	   operatore ="MEDICI";
//                        htxt.put("#tipo#","MEDICI");
                        break;
                   case 4:
                	   operatore ="FISIOTERAPISTI";
//                        htxt.put("#tipo#","FISIOTERAPISTI");
                        break;
				   case 52: // 28/11/06
					   operatore ="MEDICI CURE PALLIATIVE";
//				        htxt.put("#tipo#", "MEDICI CURE PALLIATIVE");
						break;
                  }*/
		
		int tipo=(new Integer(ass)).intValue();
        switch (tipo){
           case 02://infermiere
        	   	operatore = "INFERMIERI";
                break;
           case 03://medico distretto
        	   operatore ="MEDICI DISTRETTO";
                break;
           case 04: //fisioteratista
        	   operatore ="FISIOTERAPISTI";
                break;
           default:
        	   operatore ="ALTRI OPERATORI";
                break;
          }
				
        }else htxt.put("#tipo#","");
		if (ISASUtil.valida(operatore)){
			operatore = " Operatore: "+operatore; 
		}
	
		it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
		try {
			if (ISASUtil.valida(codZona)){
				descrZona = util.getDecode(dbc,"zone","codice_zona",codZona,"descrizione_zona");
			}else {
				descrZona = " TUTTE ";
			}
			if(ISASUtil.valida(descrZona)){
				descrZona = " Zona: "+ descrZona;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		htxt.put("#tipo#",descrZona +" " + operatore);
	
        md.writeSostituisci("layout",htxt);
}

private void preparaBody(mergeDocument md, ISASCursor dbcur,ISASConnection dbc) {
        String p_old = "";
        String p_new = "";
        String patologiaEx="";
	try {
          boolean entrato = false;
          md.write("iniziotabEx");
          while (dbcur.next()){
            Hashtable p = new Hashtable();
            ISASRecord dbr = dbcur.getRecord();
/** 27/11/06
            if (dbr.get("skpat_patol1")!=null && !((String)dbr.get("skpat_patol1")).equals(""))
              p_new = (String)dbr.get("skpat_patol1");
            if (!p_new.equals(p_old)){
			  String desc=decodifica("icd9","cd_diag",p_new,"diagnosi",dbc);
**/
			// 27/11/06 ---
            if (dbr.get("diag1")!=null && !((String)dbr.get("diag1")).equals(""))
              p_new = (String)dbr.get("diag1");
            if (!p_new.equals(p_old)){
              String desc=decodifica("tab_diagnosi","cod_diagnosi",p_new,"diagnosi",dbc);
			// 27/11/06 ---
              patologiaEx=desc+" ("+p_new+")";
              Hashtable hsalto = new Hashtable();
              hsalto.put("#patologia#",desc+" ("+p_new+")");
              if (entrato){
                md.write("finetab");
                md.write("salto");
              }
              md.writeSostituisci("pato",hsalto);
              md.write("iniziotab");
            }
            entrato = true;
            p.put("#patologia#",patologiaEx);
            p.put("#cartella#", ((Integer)dbr.get("n_cartella")).toString());
            p.put("#nome#", (String)dbr.get("cognome")+" "+(String)dbr.get("nome"));
            p.put("#sesso#", (String)dbr.get("sesso"));
            String datan=((java.sql.Date)dbr.get("data_nasc")).toString();
              datan=datan.substring(8,10)+"/"+datan.substring(5,7)+"/"+
              datan.substring(0,4);
            p.put("#data_nascita#", datan);
            md.writeSostituisci("tabella", p);
            p_old = p_new;
          }	// fine while
	} catch (Exception ex) {
		Hashtable p = new Hashtable();
		p.put("#cartella#","");
		p.put("#nome#", "*** errore in lettura: "+ex+" ***");
		p.put("#sesso#","");
		p.put("#data_nascita#", "");
		md.writeSostituisci("tabella", p);
	}
	md.write("finetab");
	md.write("finetabEx");
}
public byte[] query_vasccer(String utente, String passwd, Hashtable par,
		mergeDocument eve) throws SQLException {
		String punto = MIONOME  + "query_vasccer ";
		LOG.info(punto + " inizio con dati>"+ par + "<");
	        boolean done=false;
	        ISASConnection dbc=null;

	try{
		
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";		}
		
			
			myLogin lg = new myLogin();
			String selectedLanguage = (String)par.get(FileMaker.printParamLang);
			lg.put(utente,passwd,selectedLanguage);
	        //lg.put(utente,passwd);
	        dbc = super.logIn(lg);

	        ServerUtility su =new ServerUtility();
	        String data_ini=(String)par.get("data_inizio");
	        String data_fine=(String)par.get("data_fine");
	        String tipo=(String)par.get("tipo");
	        String cod1=(String)par.get("cod1");
	        String cod2=(String)par.get("cod2");
	        String cont=(String)par.get("cont");

	        String codZona = ISASUtil.getValoreStringa(par, "zona"); 
	        String ragg = (String)par.get("ragg");
	        String pca = (String)par.get("pca");

	        String tabella="";
	        String nome_data_ini="";
	        String nome_data_fine="";
	        String livello="";
	        String presidio="";
		    String strClauseNoOccasionali = "";
		    int tipo1;
		    if (tipo.equals("")){
		    	tipo1=0;
		    }
		    else{
		    	tipo1=(new Integer(tipo)).intValue();
		    }
	                switch (tipo1){
	                   case 02://infermiere
	                        if (cont.equals("C"))
	                        tabella="skinf";
	                        nome_data_ini="ski_data_apertura";
	                        nome_data_fine="ski_data_uscita";
	                        livello="ski_motivo";	
	                        presidio="ski_cod_presidio";	
	                        break;
	                   case 03: //medico distretto
	                        if (cont.equals("C"))
	                        tabella="skmedico";
	                        nome_data_ini="skm_data_apertura";
	                        nome_data_fine="skm_data_chiusura";
	                        livello="skm_motivo";
	                        presidio="skm_cod_presidio";
	                        break;
	                   case 04: //fisioterapista
	                        if (cont.equals("C"))
	                        tabella="skfis";
	                        nome_data_ini="skf_data";
	                        nome_data_fine="skf_data_chiusura";
	                        livello="skf_motivo";
	                        presidio="skf_cod_presidio";
	                        break;	                  
	                   default: // CASO diverso da 02 03 04
	                        if (cont.equals("C"))
	                        tabella="skfpg";
	                        nome_data_ini="skfpg_data_apertura";
	                        nome_data_fine="skfpg_data_uscita";
	                        livello="skfpg_motivo";
	                        presidio="skfpg_cod_presidio";
	                        break;
	                  }
	        String livello_ass=(String)(par.get("motivo"));
	        String mysel= "";
	
			// 27/11/06: -> si considera xogni assistito 1 sola diagnosi nel periodo.
			// a) Si prende la diagnosi con dataMax(< finePeriodo) per tutti i contatti attivi
			// nel periodo selezionato.
			// b) Si prende la diagnosi con dataMax(< finePeriodo) per quelle cartelle che hanno
			// accessi nel periodo selezionato
	        if (cont.equals("C"))
	        {
	            mysel = "SELECT DISTINCT c.n_cartella, c.nome, c.cognome, c.sesso, c.data_nasc," +
	    	                 " d.diag1" +
	                    " FROM cartella c," +
	                    " diagnosi d" +
	                    " WHERE d.n_cartella = c.n_cartella" +
	                    " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
	                        " FROM diagnosi," +
	                    tabella + " sk,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u,anagra_c a" +
	                    " WHERE diagnosi.n_cartella = d.n_cartella" +
	                    " AND diagnosi.n_cartella = sk.n_cartella" +
	                    " AND diagnosi.data_diag <= " + formatDate(dbc,data_fine) +
	                    " AND sk." + nome_data_ini + " <= " + formatDate(dbc,data_fine) +
	                    " AND (sk." + nome_data_fine + ">= " + formatDate(dbc,data_ini) +
	                    " OR sk." + nome_data_fine + " IS NULL)" +
	                    " AND d.diag1 IS NOT NULL"+
	                    " AND sk.n_cartella=a.n_cartella"+
	                    " AND a.data_variazione IN"+
	                    " (SELECT MAX (data_variazione)"+
	                    " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";

	              //mysel = su.addWhere(mysel, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

	            String condWhere = getFiltroUbicazione(par, su, tabella, presidio);
	            mysel=mysel+ " AND " + condWhere;
	              
	                if (livello_ass!=null && !livello_ass.equals("-1")) 
	                	mysel += " AND sk." + livello + "='"+livello_ass+"'";
	               if (!tipo.equals("02") && !tipo.equals("03") && !tipo.equals("04") && !tipo.equals("")) 
	            	   mysel += " AND skfpg_tipo_operatore ='" + tipo + "'"; 
	              //Aggiunto Controllo Domicilio/Residenza (BYSP)
	              /*if((String)par.get("dom_res") == null)
	              {
	                      if (ragg.equals("C"))
	                        mysel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
	                              " AND u.codice=a.dom_citta)"+
	              		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
	                              " AND u.codice=a.citta))";
	                      else if (ragg.equals("A"))
	                        mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
	                              " AND u.codice=a.dom_areadis)"+
	              		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
	                              " AND u.codice=a.areadis))";
	              }
	              else if (((String)par.get("dom_res")).equals("D"))
	                                        {
	                                         if (ragg.equals("C"))
	                        mysel += " AND u.codice=a.dom_citta";
	                                          else if (ragg.equals("A"))
	                        mysel += " AND u.codice=a.dom_areadis";
	                                        }

	              else if (((String)par.get("dom_res")).equals("R"))
	                              {
	                              if (ragg.equals("C"))
	                        mysel += " AND u.codice=a.citta";
	                      else if (ragg.equals("A"))
	                        mysel += " AND u.codice=a.areadis";
	                              }
	              
	             mysel = su.addWhere(mysel, su.REL_AND, "u.cod_zona",
	                su.OP_EQ_STR, (String)par.get("zona"));
	             mysel = su.addWhere(mysel, su.REL_AND, "u.cod_distretto",
	                su.OP_EQ_STR, (String)par.get("distretto"));
	             mysel = su.addWhere(mysel, su.REL_AND, "u.codice",
	                su.OP_EQ_STR, (String)par.get("pca"));*/

	            mysel += ")";
	        }else
	        {
	              mysel = "SELECT DISTINCT c.n_cartella, c.nome, c.cognome, c.sesso, c.data_nasc," +
	                        " d.diag1" +
	                        " FROM cartella c," +
	                        " diagnosi d," +
	                        " interv i,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u";
	                        if (livello_ass!=null && !livello_ass.equals("") && !livello_ass.equals("-1"))
	                        	mysel +=" ,rm_skso r, rm_skso_mmg m";
	                        mysel +=" WHERE d.n_cartella = c.n_cartella" +
	                        " AND d.n_cartella = i.int_cartella" +
	                        " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
	                        " FROM diagnosi" +
	                        " WHERE diagnosi.n_cartella = d.n_cartella" +
	                        " AND diagnosi.data_diag <= " + formatDate(dbc,data_fine) + ")" +
	                        " AND i.int_tipo_oper = '" + tipo + "'" +
	                        " AND i.int_data_prest >= " +	formatDate(dbc,data_ini) +
	                        " AND i.int_data_prest <= " + formatDate(dbc,data_fine) +
	                        " AND d.diag1 IS NOT NULL" +
	                        strClauseNoOccasionali;
	                        if (livello_ass!=null && !livello_ass.equals("") && !livello_ass.equals("-1"))
	                        	mysel +=" AND r.n_cartella=i.int_cartella"+
	                        			" AND r.data_presa_carico_skso >= " + formatDate(dbc,data_ini) + 
	                        			" AND r.data_presa_carico_skso <= " + formatDate(dbc,data_fine) + 
	                        			" AND r.n_cartella=m.n_cartella and r.id_skso=m.id_skso"+
	                        			" AND m.tipocura='"+livello_ass+"'";

	      		if(this.dom_res==null)
				{
				if(ragg.equals("A"))
	            {
					mysel += " AND int_cod_areadis=u.codice";
					mysel = su.addWhere(mysel, su.REL_AND, "int_cod_areadis" ,
	                        su.OP_EQ_STR,pca);
	            }
	            else if(ragg.equals("C"))
	            {
	            	mysel += " AND int_cod_comune=u.codice";
	            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
	                        su.OP_EQ_STR,pca);
	            }
	            else if(ragg.equals("P"))
	            {
	            	mysel += " AND int_cod_presidio_sk=u.codice";
	            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_presidio_sk" ,
	                        su.OP_EQ_STR,pca);
	            }
				}else if(this.dom_res.equals("D"))
				{
					if(ragg.equals("A"))
		            {
						mysel += " AND int_cod_areadis=u.codice";
						mysel = su.addWhere(mysel, su.REL_AND, "int_cod_areadis" ,
		                        su.OP_EQ_STR,pca);
		            }
		            else if(ragg.equals("C"))
		            {
		            	mysel += " AND int_cod_comune=u.codice";
		            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
		                        su.OP_EQ_STR,pca);
		            }
		            else if(ragg.equals("P"))
		            {
		            	mysel += " AND int_cod_presidio_sk=u.codice";
		            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_presidio_sk" ,
		                        su.OP_EQ_STR,pca);
		            }
					}else if(this.dom_res.equals("R"))
					{
						if(ragg.equals("A"))
			            {
							mysel += " AND int_cod_res_areadis=u.codice";
							mysel = su.addWhere(mysel, su.REL_AND, "int_cod_res_areadis" ,
			                        su.OP_EQ_STR,pca);
			            }
			            else if(ragg.equals("C"))
			            {
			            	mysel += " AND int_cod_comune=u.codice";
			            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
			                        su.OP_EQ_STR,pca);
			            }
			            else if(ragg.equals("P"))
			            {
			            	mysel += " AND int_cod_presidio_sk=u.codice";
			            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_presidio_sk" ,
			                        su.OP_EQ_STR,pca);
			            }
						}
	              
	                mysel = su.addWhere(mysel, su.REL_AND, "u.cod_zona",
	                        su.OP_EQ_STR, (String)par.get("zona"));
	                mysel = su.addWhere(mysel, su.REL_AND, "u.cod_distretto",
	                        su.OP_EQ_STR, (String)par.get("distretto"));
	                
	                
	        }
	        if(cod1 != null && !(cod1.equals("")))
				//mysel += " AND d.diag1 >= '" + cod1 + "'";
	        	mysel += " AND d.diag1 = '" + cod1 + "'";

	        if(cod2 != null && !(cod2.equals("")))
				mysel += " AND d.diag1 <= '" + cod2 + "'";

	        

	        mysel += " ORDER BY d.diag1, c.cognome, c.nome";

		LOG.info(punto + " Query["+mysel+"].");
	        //try {
	         ISASCursor dbcur = dbc.startCursor(mysel);
			if (dbcur == null) {
				preparaLayout(eve, dbc,data_ini,data_fine,cod1,cod2,tipo, codZona);
				eve.write("messaggio");
				eve.write("finale");
				//System.out.println("FoVascCerEJB.query_vasccer(): "+
				//	"cursore non valido ["+mysel+"].");
			} else	{
				if (dbcur.getDimension() <= 0) {
					preparaLayout(eve, dbc,data_ini,data_fine,cod1,cod2,tipo, codZona);
					eve.write("messaggio");
					eve.write("finale");
				} else	{
					preparaLayout(eve, dbc,data_ini,data_fine,cod1,cod2,tipo, codZona);
					preparaBody(eve, dbcur,dbc);
					eve.write("finale");
				}	// fine if dbcur.getDimension()
			}	// fine if dbcur
			dbcur.close();

			eve.close();

			//System.out.println("FoVascCerEJB.query_vasccer(): DEBUG "+
			//	"documento restituito ["+(new String(eve.get()))+"]");

			dbc.close();
			super.close(dbc);
			done=true;
			return eve.get();	// restituisco il bytearray
		} catch(Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			throw new SQLException("FoVascCerEJB.query_vasccer(): "+e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					e1.printStackTrace();
					System.out.println("FoVascCerEJB.query_vasccer(): "+e1);
				}
			}
		}
	}	// End of query_vasccer() method

public byte[] query_vasccer_old(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {
	String punto = MIONOME  + "query_vasccer ";
	LOG.info(punto + " inizio con dati>"+ par + "<");
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

        ServerUtility su =new ServerUtility();
        String data_ini=(String)par.get("data_inizio");
        String data_fine=(String)par.get("data_fine");
        String tipo=(String)par.get("tipo");
        //String cod=(String)par.get("cod");
        String cod1=(String)par.get("cod1");
        String cod2=(String)par.get("cod2");
        String cont=(String)par.get("cont");

        String codZona = ISASUtil.getValoreStringa(par, "zona"); 
        String ragg = (String)par.get("ragg");
        String pca = (String)par.get("pca");

        String tabella="";
        String nome_data_ini="";
        String nome_data_fine="";
        String livello="";
	    String strClauseNoOccasionali = "";

                int tipo1=(new Integer(tipo)).intValue();
                tabella = "interventi";
                switch (tipo1){
                   case 1:
/*gb 11/06/07 *******
                        if (cont.equals("C"))
                          tabella="contatti";
                        nome_data_ini="data_contatto";
                        nome_data_fine="data_chiusura";
*gb 11/06/07 *******/
//gb 11/06/07 *******
                        if (cont.equals("C"))
                          tabella="ass_progetto";
                        nome_data_ini="ap_data_apertura";
                        nome_data_fine="ap_data_chiusura";
//gb 11/06/07: fine *******
                        break;
                   case 2:
                        if (cont.equals("C"))
                          tabella="skinf";
                        nome_data_ini="ski_data_apertura";
                        nome_data_fine="ski_data_uscita";
                        livello="ski_motivo";
                        break;
                   case 3:
                        if (cont.equals("C"))
                          tabella="skmedico";
                        nome_data_ini="skm_data_apertura";
                        nome_data_fine="skm_data_chiusura";
                        livello="skm_motivo";
                        break;
                   case 4:
                        if (cont.equals("C"))
                          tabella="skfis";
                        nome_data_ini="skf_data";
                        nome_data_fine="skf_data_chiusura";
                        livello="skf_motivo";
                        break;
		   case 52: // 28/11/06
                        if (cont.equals("C"))
                          tabella="skmedpal";
                        nome_data_ini="skm_data_apertura";
                        nome_data_fine="skm_data_chiusura";
                        break;
                  }
//gb 11/06/07 *******
	if ((tipo1 == 1) && cont.equals("I"))
	      {
	      strClauseNoOccasionali = " AND i.n_progetto <> 0 AND i.n_progetto IS NOT NULL";
	      }
	else if ((tipo1 != 1) && cont.equals("I"))
	      {
	      strClauseNoOccasionali = " AND i.int_contatto <> 0 AND i.int_contatto IS NOT NULL";
	      }
//gb 11/06/07: fine *******

        String mysel= "";
/*** 27/11/06
        if (cont.equals("C"))
          mysel = "SELECT DISTINCT c.n_cartella, c.nome,c.cognome,c.sesso,c.data_nasc"+
                     ",skpatologie.skpat_patol1 FROM cartella c, skpatologie,"+tabella+" WHERE "+
                     "skpatologie.n_cartella="+tabella+".n_cartella AND "+
                     "skpatologie.n_contatto="+tabella+".n_contatto AND "+
                     tabella+"."+nome_data_ini+"<="+formatDate(dbc,data_fine)+" AND ("+
                     tabella+"."+nome_data_fine+">="+formatDate(dbc,data_ini)+" OR "+
                     tabella+"."+nome_data_fine+" is null) AND "+
                     tabella+".n_cartella=c.n_cartella"+
                     " AND skpatologie.skpat_patol1 is not null";
        else
          mysel = "SELECT DISTINCT c.n_cartella, c.nome,c.cognome,c.sesso,c.data_nasc"+
                     ",skpatologie.skpat_patol1 FROM cartella c, skpatologie,interv WHERE "+
                     "skpatologie.n_cartella=interv.int_cartella AND "+
                     "skpatologie.n_contatto=interv.int_contatto AND "+
                     "interv.int_data_prest>="+formatDate(dbc,data_ini)+" AND "+
                     "interv.int_data_prest<="+formatDate(dbc,data_fine)+" AND "+
                     "interv.int_cartella=c.n_cartella AND "+
                     "interv.int_tipo_oper='"+tipo+"'"+
                     " AND skpatologie.skpat_patol1 is not null";

        if(cod1 != null && !(cod1.equals("")))
                mysel += " AND skpatologie.skpat_patol1 >= '"+cod1+"'";
        if(cod2 != null && !(cod2.equals("")))
                mysel += " AND skpatologie.skpat_patol1 <= '"+cod2+"'";

        mysel += " ORDER BY skpatologie.skpat_patol1,c.cognome,c.nome";
***/
		// 27/11/06: -> si considera xogni assistito 1 sola diagnosi nel periodo.
		// a) Si prende la diagnosi con dataMax(< finePeriodo) per tutti i contatti attivi
		// nel periodo selezionato.
		// b) Si prende la diagnosi con dataMax(< finePeriodo) per quelle cartelle che hanno
		// accessi nel periodo selezionato
        if (cont.equals("C"))
        {
            mysel = "SELECT DISTINCT c.n_cartella, c.nome, c.cognome, c.sesso, c.data_nasc," +
    	                 " d.diag1" +
                    " FROM cartella c," +
                    " diagnosi d" +
                    " WHERE d.n_cartella = c.n_cartella" +
                    " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
                        " FROM diagnosi," +
                    tabella + " sk,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u,anagra_c a" +
                    " WHERE diagnosi.n_cartella = d.n_cartella" +
                    " AND diagnosi.n_cartella = sk.n_cartella" +
                    " AND diagnosi.data_diag <= " + formatDate(dbc,data_fine) +
                    " AND sk." + nome_data_ini + " <= " + formatDate(dbc,data_fine) +
                    " AND (sk." + nome_data_fine + ">= " + formatDate(dbc,data_ini) +
                    " OR sk." + nome_data_fine + " IS NULL)" +
                    " AND d.diag1 IS NOT NULL"+
                    " AND sk.n_cartella=a.n_cartella"+
                    " AND a.data_variazione IN"+
                    " (SELECT MAX (data_variazione)"+
                    " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";

              mysel = su.addWhere(mysel, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

//              if (ragg!=null && ragg.equals("C")){
//                    mysel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                            " AND u.codice=a.dom_citta)"+
//                            " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                            " AND u.codice=a.citta))";
//              }else if (ragg!=null && ragg.equals("A")){
//                mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                            " AND u.codice=a.dom_areadis)"+
//                            " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                            " AND u.codice=a.areadis))";
//             }
              
              //Aggiunto Controllo Domicilio/Residenza (BYSP)
              if((String)par.get("dom_res") == null)
              {
                      if (ragg.equals("C"))
                        mysel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                              " AND u.codice=a.dom_citta)"+
              		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                              " AND u.codice=a.citta))";
                      else if (ragg.equals("A"))
                        mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                              " AND u.codice=a.dom_areadis)"+
              		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                              " AND u.codice=a.areadis))";
              }
              else if (((String)par.get("dom_res")).equals("D"))
                                        {
                                         if (ragg.equals("C"))
                        mysel += " AND u.codice=a.dom_citta";
                                          else if (ragg.equals("A"))
                        mysel += " AND u.codice=a.dom_areadis";
                                        }

              else if (((String)par.get("dom_res")).equals("R"))
                              {
                              if (ragg.equals("C"))
                        mysel += " AND u.codice=a.citta";
                      else if (ragg.equals("A"))
                        mysel += " AND u.codice=a.areadis";
                              }
              
             mysel = su.addWhere(mysel, su.REL_AND, "u.cod_zona",
                su.OP_EQ_STR, (String)par.get("zona"));
             mysel = su.addWhere(mysel, su.REL_AND, "u.cod_distretto",
                su.OP_EQ_STR, (String)par.get("distretto"));
             mysel = su.addWhere(mysel, su.REL_AND, "u.codice",
                su.OP_EQ_STR, (String)par.get("pca"));

            mysel += ")";
        }else
        {
              mysel = "SELECT DISTINCT c.n_cartella, c.nome, c.cognome, c.sesso, c.data_nasc," +
                        " d.diag1" +
                        " FROM cartella c," +
                        " diagnosi d," +
                        " interv i,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u" +
                        " WHERE d.n_cartella = c.n_cartella" +
                        " AND d.n_cartella = i.int_cartella" +
                        " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
                        " FROM diagnosi" +
                        " WHERE diagnosi.n_cartella = d.n_cartella" +
                        " AND diagnosi.data_diag <= " + formatDate(dbc,data_fine) + ")" +
                        " AND i.int_tipo_oper = '" + tipo + "'" +
                        " AND i.int_data_prest >= " +	formatDate(dbc,data_ini) +
                        " AND i.int_data_prest <= " + formatDate(dbc,data_fine) +
                        " AND d.diag1 IS NOT NULL" +
                        strClauseNoOccasionali;
//                if(ragg.equals("A"))
//                {
//			mysel += " AND int_cod_areadis=u.codice";
//                        mysel = su.addWhere(mysel, su.REL_AND, "int_cod_areadis" ,
//                            su.OP_EQ_STR,pca);
//                }
//                else if(ragg.equals("C"))
//                {
//			mysel += " AND int_cod_comune=u.codice";
//                        mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
//                            su.OP_EQ_STR,pca);
//                }

      		if(this.dom_res==null)
			{
			if(ragg.equals("A"))
            {
				mysel += " AND int_cod_areadis=u.codice";
				mysel = su.addWhere(mysel, su.REL_AND, "int_cod_areadis" ,
                        su.OP_EQ_STR,pca);
            }
            else if(ragg.equals("C"))
            {
            	mysel += " AND int_cod_comune=u.codice";
            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
                        su.OP_EQ_STR,pca);
            }
			}else if(this.dom_res.equals("D"))
			{
				if(ragg.equals("A"))
	            {
					mysel += " AND int_cod_areadis=u.codice";
					mysel = su.addWhere(mysel, su.REL_AND, "int_cod_areadis" ,
	                        su.OP_EQ_STR,pca);
	            }
	            else if(ragg.equals("C"))
	            {
	            	mysel += " AND int_cod_comune=u.codice";
	            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
	                        su.OP_EQ_STR,pca);
	            }
				}else if(this.dom_res.equals("R"))
				{
					if(ragg.equals("A"))
		            {
						mysel += " AND int_cod_res_areadis=u.codice";
						mysel = su.addWhere(mysel, su.REL_AND, "int_cod_res_areadis" ,
		                        su.OP_EQ_STR,pca);
		            }
		            else if(ragg.equals("C"))
		            {
		            	mysel += " AND int_cod_comune=u.codice";
		            	mysel = su.addWhere(mysel, su.REL_AND, "int_cod_comune" ,
		                        su.OP_EQ_STR,pca);
		            }
					}
              
                mysel = su.addWhere(mysel, su.REL_AND, "u.cod_zona",
                        su.OP_EQ_STR, (String)par.get("zona"));
                mysel = su.addWhere(mysel, su.REL_AND, "u.cod_distretto",
                        su.OP_EQ_STR, (String)par.get("distretto"));
                
                String livello_ass=(String)(par.get("motivo"));
                if (livello_ass!=null && !livello_ass.equals("-1")) 
                	mysel += " AND sk." + livello + "='"+livello_ass+"'";
        }
        if(cod1 != null && !(cod1.equals("")))
			mysel += " AND d.diag1 >= '" + cod1 + "'";
        if(cod2 != null && !(cod2.equals("")))
			mysel += " AND d.diag1 <= '" + cod2 + "'";

        

        mysel += " ORDER BY d.diag1, c.cognome, c.nome";

	LOG.info(punto + " Query["+mysel+"].");
        //try {
         ISASCursor dbcur = dbc.startCursor(mysel);
		if (dbcur == null) {
			preparaLayout(eve, dbc,data_ini,data_fine,cod1,cod2,tipo, codZona);
			eve.write("messaggio");
			eve.write("finale");
			//System.out.println("FoVascCerEJB.query_vasccer(): "+
			//	"cursore non valido ["+mysel+"].");
		} else	{
			if (dbcur.getDimension() <= 0) {
				preparaLayout(eve, dbc,data_ini,data_fine,cod1,cod2,tipo, codZona);
				eve.write("messaggio");
				eve.write("finale");
			} else	{
				preparaLayout(eve, dbc,data_ini,data_fine,cod1,cod2,tipo, codZona);
				preparaBody(eve, dbcur,dbc);
				eve.write("finale");
			}	// fine if dbcur.getDimension()
		}	// fine if dbcur
		dbcur.close();

		eve.close();

		//System.out.println("FoVascCerEJB.query_vasccer(): DEBUG "+
		//	"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("FoVascCerEJB.query_vasccer(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				e1.printStackTrace();
				System.out.println("FoVascCerEJB.query_vasccer(): "+e1);
			}
		}
	}
}	// End of query_vasccer() method

public String getFiltroUbicazione(Hashtable<String, String> par, ServerUtility su, String tabella, String presidio) {

	String condWhere = "";
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.codice", su.OP_EQ_STR, (String) par.get("pca"));
	String raggruppamento = (String) par.get("ragg");
	condWhere = su.addWhere(condWhere, su.REL_AND, "u.tipo", su.OP_EQ_STR, raggruppamento);
	String dom_res = ISASUtil.getValoreStringa(par, "dom_res");
	if (!ISASUtil.valida(dom_res)) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')" + " AND u.codice=a.dom_citta)"
					+ " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') " + " AND u.codice=a.citta))";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"
					+ " AND u.codice=a.dom_areadis)" + " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "
					+ " AND u.codice=a.areadis))";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = sk." + presidio + " ";
		}
	} else if (dom_res.equals("D")) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND u.codice=a.dom_citta";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND u.codice=a.dom_areadis";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = sk." + presidio + " ";
		}
	} else if (dom_res.equals("R")) {
		if (raggruppamento.equals("C")) {
			condWhere += " AND u.codice=a.citta";
		} else if (raggruppamento.equals("A")) {
			condWhere += " AND u.codice=a.areadis";
		} else if (raggruppamento.equals("P")) {
			condWhere += " AND u.codice  = sk." + presidio + " ";
		}
	}
	return condWhere;
}

    private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
		//System.out.println("FoEleRsaEJB.decodifica(): DEBUG ["+mysel+"].");
                ISASRecord dbtxt = dbc.readRecord(mysel);
		return ((String)dbtxt.get("descrizione"));
	} catch (Exception ex) {
		return " ";
	}
}

}	// End of FoVascCer class

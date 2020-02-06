package it.caribel.app.sinssnt.bean;
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 06/09/2007 - EJB di connessione alla procedura SINS Tabella FoRiepDecessi
//
// Jessica Caccavale
//
//	23/02/09 m.: aggiunto ulteriore voce "HOSPICE" +
// 		ctrl su data e motivo chiusura = decesso su CARTELLA.
//	22/12/09 m.: aggiunto ulteriore voce "RSA" +
//		suddivisione territoriale con "NESSUNA DIVISIONE"
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.math.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;	// fo merge

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
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.DataWI;

public class FoRiepDecessiEJB extends SINSSNTConnectionEJB {

	String dom_res;
	String dr;

//hash per decodifica operatore
Hashtable hPatologia= new Hashtable();
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

String ragg="";
String d1="";
String d2="";
private String MIONOME = "5-FoRiepDecessiEJB.";
int iterazione = 1;
private void preparaLayout(mergeDocument md, ISASConnection dbc) {
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
        String data=d1.substring(8, 10)+"/"+d1.substring(5, 7)+"/"+d1.substring(0, 4);
        htxt.put("#d1#",data);
        data=d2.substring(8, 10)+"/"+d2.substring(5, 7)+"/"+d2.substring(0, 4);
        htxt.put("#d2#", data);
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	md.writeSostituisci("layout",htxt);
}

public static String getjdbcDate(){
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}


public FoRiepDecessiEJB() {}

public byte[] query_report(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	String punto =MIONOME  + "query_report ";
	System.out.println(punto + " inizio con dati>"+par+"\n");
	boolean done=false;
	ISASConnection dbc=null;
	byte[] jessy;

        String tipo="";
	boolean entrato=false;
	try{
	
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);
		String datasystem="";
		if (par.get("d1") != null && !((String)par.get("d1")).equals(""))
			d1=(String)par.get("d1");
		if (par.get("d2") != null && !((String)par.get("d2")).equals(""))
			d2=(String)par.get("d2");

		String myselect = mkSelectAnalitica(dbc, par);
                System.out.println("SELECT: "+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
                if(((String)par.get("TYPE")).equals("PDF")){
                    preparaLayout(doc, dbc);
                    Hashtable hGenerale=new Hashtable();
                    int k = 0;
                    while (dbcur.next()){
                       ISASRecord dbr= dbcur.getRecord();
                       hGenerale=AnalizzaZona(dbc,dbr,hGenerale, par);
                    }
                    if (hGenerale.size()!=0)
                        StampaDaZona(hGenerale,doc,dbc);
                    else
                        doc.write("messaggio");
                }else{
                    int i=0;
                    while(dbcur.next()){
                        if(i==0)    doc.write("layout");
                        i++;
                        ISASRecord dbr = dbcur.getRecord();
                        FaiExcel(dbc, dbr, doc);
                    }
                }
				Hashtable h = faiMedie(dbc,par);
				
				doc.writeSostituisci("medie",h);
				
                doc.write("finale");
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
                stampa(punto + " fine esecuzione");
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_report()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}

private void stampa(String messaggio) {
	if (iterazione <2){
		System.out.println(messaggio);
	}
}

private void FaiExcel(ISASConnection dbc, ISASRecord dbr, mergeDocument doc)throws SQLException{
    String sel_deceduti="";
    String sel_viventi="";
    String sel_incarico="";
    int tot_deceduti=0;
    int tot_viventi=0;
    int tot_incarico=0;
    ISASRecord dbConta=null;
    try{
          Hashtable tab=new Hashtable();
          //Conto i decessi totali nel periodo
          sel_deceduti = "SELECT COUNT (DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a"+
// 23/02/09         getParteWhere(dbc)+
					getParteWhereDecCart(dbc) + // 23/02/09: deceduti su CARTELLA
					" AND m.skm_motivo_chius='2' "+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr); // 22/12/09: nessuna divisione
          //System.out.println("Select per deceduti:"+sel_deceduti);
          dbConta = dbc.readRecord(sel_deceduti);
          if(dbConta!=null && dbConta.get("conta")!=null)
                tot_deceduti=util.getIntField(dbConta, "conta");

          //conto i viventi nel periodo
          sel_viventi = "SELECT COUNT (DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a"+
// 23/02/09         getParteWhere(dbc)+
					getParteWhereNoDecCart(dbc) + // 23/02/09: NON deceduti su CARTELLA 
//					" AND (m.skm_motivo_chius<>'2' OR m.skm_motivo_chius IS NULL)"+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr); // 22/12/09: nessuna divisione
          //System.out.println("Select per viventi:"+sel_viventi);
          dbConta = dbc.readRecord(sel_viventi);
          if(dbConta!=null && dbConta.get("conta")!=null)
                tot_viventi=util.getIntField(dbConta, "conta");

          //conto gli assititi in carico nel periodo.
          //Se l'assistito � decuduto nel periodo, cmq lo conto come in carico
          sel_incarico = "SELECT COUNT (DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a"+
                    getParteWhere(dbc)+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr); // 22/12/09: nessuna divisione
          //System.out.println("Select per assistiti in carico:"+sel_incarico);
          dbConta = dbc.readRecord(sel_incarico);
          if(dbConta!=null && dbConta.get("conta")!=null)
                tot_incarico=util.getIntField(dbConta, "conta");

          //conto i deceduti raggruppati per luogo di decesso
          String sel = "SELECT COUNT (DISTINCT m.n_cartella) conta, m.skm_deceduto FROM skmedpal m, anagra_c a"+
// 23/02/09         getParteWhere(dbc)+
					getParteWhereDecCart(dbc) + // 23/02/09: deceduti su CARTELLA
					" AND m.skm_motivo_chius='2' "+
// 22/12/09			getJoinComune((String)dbr.get("codice")) +
					getJoinComune(dbr) + // 22/12/09: nessuna divisione					
                    " GROUP BY m.skm_deceduto";
          System.out.println(" Select per deceduti raggruppati:"+sel);
          ISASCursor dbcur = dbc.startCursor(sel);
		  
          tab.put("#domicilio_pazienti#","");
          tab.put("#domicilio_percento#","");
          tab.put("#ospedale_pazienti#","");
          tab.put("#ospedale_percento#","");
          tab.put("#comunita_pazienti#","");
          tab.put("#comunita_percento#","");
          tab.put("#sede_pazienti#","");
          tab.put("#sede_percento#","");
          tab.put("#viventi_pazienti#","");
          tab.put("#viventi_percento#","");
			// 23/02/09 --
          tab.put("#hospice_pazienti#","");
          tab.put("#hospice_percento#","");
			// 23/02/09 --
			// 22/12/09 --
          tab.put("#rsa_pazienti#","");
          tab.put("#rsa_percento#","");
			// 22/12/09 --
			
          while (dbcur.next()){
            ISASRecord dbrTot= dbcur.getRecord();
            if(((String)dbrTot.get("skm_deceduto")).equals("1")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#domicilio_pazienti#", ""+dec);
                tab.put("#domicilio_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("2")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#ospedale_pazienti#", ""+dec);
                tab.put("#ospedale_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("3")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#comunita_pazienti#", ""+dec);
                tab.put("#comunita_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("4")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#sede_pazienti#", ""+dec);
                tab.put("#sede_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("5")){ // 23/02/09
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#hospice_pazienti#", ""+dec);
                tab.put("#hospice_percento#", ""+((dec*100)/tot_deceduti)+"%");
            } else if(((String)dbrTot.get("skm_deceduto")).equals("6")){ // 22/12/09
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#rsa_pazienti#", ""+dec);
                tab.put("#rsa_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }
          }
          tab.put("#viventi_pazienti#", ""+tot_viventi);
          tab.put("#viventi_percento#", ""+((tot_viventi*100)/tot_incarico)+"%");
		  
          tab.put("#zona#", (String)dbr.get("des_zona"));
          tab.put("#distretto#", (String)dbr.get("des_distretto"));
          tab.put("#comune#", (String)dbr.get("descrizione"));
		  
          doc.writeSostituisci("tabella", tab);
		  
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una FaiExcel()  ");
    }
}

/***
private String getJoinComune(String comune)throws SQLException{
    String mysel="";
    try{
		if(!comune.equals("NESSUNA DIVISIONE")){
	        if (ragg!=null && ragg.equals("C"))
	            mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
						 " AND a.dom_citta='"+comune+"')"+
						 " OR ((a.dom_citta IS NULL OR a.dom_citta = '') "+
						 " AND a.citta='"+comune+"'))";
	        else if (ragg!=null && ragg.equals("A"))
	            mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
	                     " AND a.dom_areadis='"+comune+"')"+
	                     " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
	                     " AND a.areadis='"+comune+"'))";
		}
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una getJoinComune()  ");
    }
    return mysel;
}
****/

	// 22/12/09
	private String getJoinComune(ISASRecord dbr)throws SQLException
	{
		String mysel=""; 		
		try{
			String comune = (String)dbr.get("codice");
			String distr = (String)dbr.get("cod_distretto");
			String zona = (String)dbr.get("cod_zona");
//			System.out.println("****** FoRiepDecessi.getJoinConune - comune=["+comune+"] - distr=["+distr+"] - zona=["+zona+"] *********");			
			if(this.dom_res==null)
			{				
			if (!comune.equals("NESSUNA DIVISIONE")){
				if (ragg!=null && ragg.equals("C"))
					mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')" +
									" AND a.dom_citta='"+comune+"')" +
								" OR ((a.dom_citta IS NULL OR a.dom_citta = '')" +
									" AND a.citta='"+comune+"'))";
				else if (ragg!=null && ragg.equals("A"))
					mysel += " AND (((a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" +
									" AND a.dom_areadis='"+comune+"')" +
								" OR ((a.dom_areadis IS NULL OR a.dom_areadis = '')" +
									" AND a.areadis='"+comune+"'))";
			} else if (!distr.equals("NESSUNA DIVISIONE")){
				if (ragg!=null && ragg.equals("C"))
					mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')" +
									" AND EXISTS (SELECT * FROM comuni c, distretti d" +
									" WHERE a.dom_citta = c.codice" +
									" AND c.cod_distretto = d.cod_distr" +
									" AND d.cod_distr = '" + distr + "'))" +
								" OR ((a.dom_citta IS NULL OR a.dom_citta = '')" +
									" AND EXISTS (SELECT * FROM comuni c, distretti d" +
									" WHERE a.citta = c.codice" +
									" AND c.cod_distretto = d.cod_distr" +
									" AND d.cod_distr = '" + distr + "')))";
				else if (ragg!=null && ragg.equals("A"))
					mysel += " AND (((a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" +
									" AND EXISTS (SELECT * FROM areadis ad, distretti d" +
									" WHERE a.dom_areadis = ad.codice" +
									" AND ad.cod_distretto = d.cod_distr" +
									" AND d.cod_distr = '" + distr + "'))" +
	                            " OR ((a.dom_areadis IS NULL OR a.dom_areadis = '') " +
									" AND EXISTS (SELECT * FROM areadis ad, distretti d" +
									" WHERE a.areadis = ad.codice" +
									" AND ad.cod_distretto = d.cod_distr" +
									" AND d.cod_distr = '" + distr + "')))";
			} else if (!zona.equals("NESSUNA DIVISIONE")){
				if (ragg!=null && ragg.equals("C"))
					mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')" +
									" AND EXISTS (SELECT * FROM comuni c, distretti d, zone z" +
									" WHERE a.dom_citta = c.codice" +
									" AND c.cod_distretto = d.cod_distr" +
									" AND d.cod_zona = z.codice_zona" +
									" AND z.codice_zona = '" + zona + "'))" +		
								" OR ((a.dom_citta IS NULL OR a.dom_citta = '')" +
									" AND EXISTS (SELECT * FROM comuni c, distretti d, zone z" +
									" WHERE a.citta = c.codice" +
									" AND c.cod_distretto = d.cod_distr" +
									" AND d.cod_zona = z.codice_zona" +
									" AND z.codice_zona = '" + zona + "')))";
				else if (ragg!=null && ragg.equals("A"))
					mysel += " AND (((a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')" +
									" AND EXISTS (SELECT * FROM areadis ad, distretti d, zone z" +
									" WHERE a.dom_areadis = ad.codice" +
									" AND ad.cod_distretto = d.cod_distr" +
									" AND d.cod_zona = z.codice_zona" +
									" AND z.codice_zona = '" + zona + "'))" +
								" OR ((a.dom_areadis IS NULL OR a.dom_areadis = '')" +
									" AND EXISTS (SELECT * FROM areadis ad, distretti d, zone z" +
									" WHERE a.areadis = ad.codice" +
									" AND ad.cod_distretto = d.cod_distr" +
									" AND d.cod_zona = z.codice_zona" +
									" AND z.codice_zona = '" + zona + "')))";
			}
			}
			
			else if (this.dom_res.equals("D"))
			{
				if (!comune.equals("NESSUNA DIVISIONE")){
					if (ragg!=null && ragg.equals("C"))
						mysel += " AND a.dom_citta='"+comune+"'";
					else if (ragg!=null && ragg.equals("A"))
						mysel += " AND a.dom_areadis='"+comune+"'";
				} else if (!distr.equals("NESSUNA DIVISIONE")){
					if (ragg!=null && ragg.equals("C"))
						mysel += " AND EXISTS (SELECT * FROM comuni c, distretti d" +
										" WHERE a.dom_citta = c.codice" +
										" AND c.cod_distretto = d.cod_distr" +
										" AND d.cod_distr = '" + distr + "')";
					else if (ragg!=null && ragg.equals("A"))
						mysel += " AND EXISTS (SELECT * FROM areadis ad, distretti d" +
										" WHERE a.dom_areadis = ad.codice" +
										" AND ad.cod_distretto = d.cod_distr" +
										" AND d.cod_distr = '" + distr + "')";
				} else if (!zona.equals("NESSUNA DIVISIONE")){
					if (ragg!=null && ragg.equals("C"))
						mysel += " AND EXISTS (SELECT * FROM comuni c, distretti d, zone z" +
										" WHERE a.dom_citta = c.codice" +
										" AND c.cod_distretto = d.cod_distr" +
										" AND d.cod_zona = z.codice_zona" +
										" AND z.codice_zona = '" + zona + "')";
					else if (ragg!=null && ragg.equals("A"))
						mysel += " AND EXISTS (SELECT * FROM areadis ad, distretti d, zone z" +
										" WHERE a.dom_areadis = ad.codice" +
										" AND ad.cod_distretto = d.cod_distr" +
										" AND d.cod_zona = z.codice_zona" +
										" AND z.codice_zona = '" + zona + "')";
				}
			}
			
			else if (this.dom_res.equals("R"))
			{
				if (!comune.equals("NESSUNA DIVISIONE")){
					if (ragg!=null && ragg.equals("C"))
						mysel += " AND a.citta='"+comune+"'";
					else if (ragg!=null && ragg.equals("A"))
						mysel += " AND a.areadis='"+comune+"'";
				} else if (!distr.equals("NESSUNA DIVISIONE")){
					if (ragg!=null && ragg.equals("C"))
						mysel += " AND EXISTS (SELECT * FROM comuni c, distretti d" +
										" WHERE a.citta = c.codice" +
										" AND c.cod_distretto = d.cod_distr" +
										" AND d.cod_distr = '" + distr + "')";
					else if (ragg!=null && ragg.equals("A"))
						mysel += " AND EXISTS (SELECT * FROM areadis ad, distretti d" +
										" WHERE a.areadis = ad.codice" +
										" AND ad.cod_distretto = d.cod_distr" +
										" AND d.cod_distr = '" + distr + "')";
				} else if (!zona.equals("NESSUNA DIVISIONE")){
					if (ragg!=null && ragg.equals("C"))
						mysel += " AND EXISTS (SELECT * FROM comuni c, distretti d, zone z" +
										" WHERE a.citta = c.codice" +
										" AND c.cod_distretto = d.cod_distr" +
										" AND d.cod_zona = z.codice_zona" +
										" AND z.codice_zona = '" + zona + "')";
					else if (ragg!=null && ragg.equals("A"))
						mysel += " AND EXISTS (SELECT * FROM areadis ad, distretti d, zone z" +
										" WHERE a.areadis = ad.codice" +
										" AND ad.cod_distretto = d.cod_distr" +
										" AND d.cod_zona = z.codice_zona" +
										" AND z.codice_zona = '" + zona + "')";
				}
			}
			
	    }catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una getJoinComune()  ");
	    }
	    return mysel;
	}



private String getParteWhere(ISASConnection dbc)throws SQLException{
    String mysel="";
    try{
        mysel=" WHERE m.skm_data_apertura <= " + formatDate(dbc,d2) +
              " AND (m.skm_data_chiusura IS NULL" +
					" OR m.skm_data_chiusura >= " + formatDate(dbc,d1) + ")"+
              " AND a.n_cartella = m.n_cartella"+
              " AND a.data_variazione IN (SELECT MAX(ac.data_variazione) data_var"+
					" FROM anagra_c ac WHERE a.n_cartella = ac.n_cartella)";
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una getParteWhere()  ");
    }
    return mysel;
}

	// 23/02/09: chiusura x decesso su CARTELLA
	private String getParteWhereDecCart(ISASConnection dbc)throws SQLException
	{
		String mysel = "";
	    try{
			mysel = getParteWhere(dbc);
			/*
//		rimosso il controllo su cartella in quanto a Siena (segnalazione n. 45406) ho verificato che la data di chiusura sono tutti 
 * valorizzate ma il motivo di chiusura (su cartella) era a 0. Parlandone con Andrea si � deciso di rimuovere questo controllo.
	        mysel += " AND EXISTS (SELECT * FROM cartella c" +
								" WHERE c.n_cartella = m.n_cartella" +
								" AND c.data_chiusura <= " + formatDate(dbc,d2) +
					            " AND c.data_chiusura >= " + formatDate(dbc,d1) + 
								" AND c.motivo_chiusura = 2)";
			 */
				
	    }catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una getParteWhere()  ");
	    }
	    return mysel;
	}

	// 23/02/09: non chiusi x decesso su CARTELLA
	private String getParteWhereNoDecCart(ISASConnection dbc)throws SQLException
	{
	    String mysel = "";
	    try{
			mysel = getParteWhere(dbc);
			mysel += " AND NOT EXISTS (SELECT * FROM cartella c" +
								" WHERE c.n_cartella = m.n_cartella" +
								" AND c.data_chiusura <= " + formatDate(dbc,d2) +
					            " AND c.data_chiusura >= " + formatDate(dbc,d1) + 
//								" AND c.motivo_chiusura = 2" + // boffa sempre in merito alle considerazioni della segnalazione 45406 
								")";
	    }catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una getParteWhere()  ");
	    }
	    return mysel;
	}

private String mkSelectAnalitica(ISASConnection dbc, Hashtable par)throws SQLException{
        ServerUtility su =new ServerUtility();
        String mysel="";
        try{
			// 22/12/09 ------
            String zona="";
            String distretto="";
            String comune="";
            String tipoStampa= (String)par.get("terr");
            StringTokenizer st = new StringTokenizer(tipoStampa,"|");
            String sZona=st.nextToken();
            String sDis=st.nextToken();
            String sCom=st.nextToken();

            if(sZona.equals("1"))
               zona= " u.cod_zona, u.des_zona,";
            else  zona= " 'NESSUNA DIVISIONE' cod_zona, 'NESSUNA DIVISIONE' des_zona,";
            if(sDis.equals("1"))
               distretto= " u.des_distretto, u.cod_distretto,";
            else  distretto= " 'NESSUNA DIVISIONE' des_distretto, 'NESSUNA DIVISIONE' cod_distretto,";
            if(sCom.equals("1"))
               comune= " u.codice , u.descrizione";
            else  comune= " 'NESSUNA DIVISIONE' codice, 'NESSUNA DIVISIONE' descrizione";		
			// 22/12/09 ------
			
/** 22/12/09
		            mysel = "SELECT DISTINCT u.cod_zona, u.des_zona descrizione_zona," +
		                    " u.cod_distretto, u.des_distretto des_distr," +
							" u.codice, u.descrizione" +
**/
			mysel = "SELECT DISTINCT "+zona+distretto+comune+ // 22/12/09
                    " FROM skmedpal m, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, anagra_c a"+getParteWhere(dbc);

            ragg = (String)par.get("ragg");
            mysel += " AND u.tipo='"+ragg+"'";

//            if (ragg!=null && ragg.equals("C"))
//                mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//								" AND u.codice = a.dom_citta)"+
//                         " OR ((a.dom_citta IS NULL OR a.dom_citta = '')"+
//								" AND u.codice = a.citta))";
//            else if (ragg!=null && ragg.equals("A"))
//                mysel += " AND (((a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//								" AND u.codice  a.dom_areadis)"+
//                         " OR ((a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//								" AND u.codice = a.areadis))";
            
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
                            

            mysel = su.addWhere(mysel, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String)par.get("zona"));
            mysel = su.addWhere(mysel, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String)par.get("distretto"));
            mysel = su.addWhere(mysel, su.REL_AND, "u.codice", su.OP_EQ_STR, (String)par.get("pca"));
// 22/12/09           mysel+= " ORDER BY descrizione_zona, des_distr, descrizione";
			mysel+= " ORDER BY des_zona, des_distretto, descrizione";
        }catch(Exception e){
		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una getParteWhere()  ");
        }
	return mysel;
}




private void StampaDaZona(Hashtable hzona, mergeDocument doc, ISASConnection dbc){
    Enumeration enumZona =orderedKeys(hzona);
    //int i = 0;
    while (enumZona.hasMoreElements()){
        String zona = "" + enumZona.nextElement();
        Hashtable hdistretti = (Hashtable)hzona.get(zona);
        Enumeration enumDistretti = orderedKeys(hdistretti);
        while (enumDistretti.hasMoreElements()) {
              String distretto = "" + enumDistretti.nextElement();
              Hashtable hcomuni = (Hashtable) hdistretti.get(distretto);
              Enumeration enumComuni = orderedKeys(hcomuni);
               while (enumComuni.hasMoreElements()){
                  //if(i!=0)
                     //doc.write("break");
                  String comune = "" + enumComuni.nextElement();
                  //i++;
                  Hashtable htab=new Hashtable();
                  htab.put("#descrizione_zona#",zona);
                  htab.put("#des_distr#",distretto);
                  String tipologia="";
//                  if (ragg.equals("A"))
//                      tipologia="Area Distr.";
//                  else if (ragg.equals("C"))
//                        tipologia="Comune";
                  
                  if(this.dom_res==null)
                  {
                  if (ragg.equals("A"))
                      tipologia="Area Distr.";
                  else if (ragg.equals("C"))
                        tipologia="Comune";
                  }else if (this.dom_res.equals("D"))
                  {
                      if (ragg.equals("A"))
                          tipologia="Area Distr. di Domicilio";
                      else if (ragg.equals("C"))
                            tipologia="Comune di Domicilio";
                      }else if (this.dom_res.equals("R"))
                      {
                          if (ragg.equals("A"))
                              tipologia="Area Distr. di Residenza";
                          else if (ragg.equals("C"))
                                tipologia="Comune di Residenza";
                          }                    
                  
                  htab.put("#tipologia#",tipologia);
                  htab.put("#descrizione#",comune);
                  doc.writeSostituisci("zona",htab);
                  Vector vDati = (Vector) hcomuni.get(comune);
                  doc.write("inizioTab");
                  Enumeration enumVett = vDati.elements();
                  while (enumVett.hasMoreElements()){
                        Hashtable hDati=(Hashtable)enumVett.nextElement();
                        doc.writeSostituisci("tabella",hDati);
                  }
                  //Devo scrivere i totali
                  doc.write("finetab");
              }//fine  ciclo comuni
        }//fine  ciclo distretti
    }//fine  ciclo zona
}

private Hashtable faiMedie(ISASConnection dbc, Hashtable par) throws SQLException
{
	 try{
            Hashtable h=new Hashtable();
            String data_inizio = par.get("d1").toString();
			String data_fine = par.get("d2").toString();
			System.out.println("1");
			String myselect = "select nvl(sum(skm_data_chiusura - skm_presacarico_data),0) as giorni , count(distinct(n_cartella)) as malati from skmedpal where skm_data_chiusura is not null and skm_data_chiusura between " +
			formatDate(dbc,data_inizio) +" and "+ formatDate(dbc,data_inizio) +" and skm_deceduto is not null and skm_data_chiusura is not null";
			ISASRecord dbr = dbc.readRecord (myselect);
			System.out.println("Hash giorni e malati: "+dbr.getHashtable().toString());
			if (dbr!= null )
			{
			int giorni = Integer.parseInt(dbr.get("giorni").toString().trim().equals("")?"0":dbr.get("giorni").toString());
			int malati = Integer.parseInt(dbr.get("malati").toString());
			System.out.println("2");
			int media = ((malati > 0)?giorni / malati:0);
			h.put("#t_medio_cura#",""+media);
			}
			else h.put("#t_medio_cura#","N.A.");
			System.out.println("3");
			myselect = "select tab_descrizione, skm_attivazione, count(*) as count "+
			"from skmedpal a   join tab_voci b    on a.skm_attivazione = b.tab_val where b.tab_cod = 'SPATTI'"+
			"and skm_data_apertura is not null    and skm_data_apertura between "+formatDate(dbc,data_inizio) +" and "+ formatDate(dbc,data_inizio) +
			" group by tab_descrizione, skm_attivazione";
			ISASCursor dbcur = dbc.startCursor(myselect);
			if (dbcur!=null)
			while (dbcur.next()){
                    ISASRecord dba= dbcur.getRecord();
					h.put(dba.get("skm_attivazione").toString(),dba.get("count").toString());
					}
		
			if (h.get("0")!=null ) h.put("#num_altro#",h.get("0").toString()); else h.put("#num_altro#","0");
			if (h.get("1")!=null ) h.put("#num_pal#",h.get("1").toString()); else h.put("#num_pal#","0");
			if (h.get("2")!=null ) h.put("#num_mmg#",h.get("2").toString()); else h.put("#num_mmg#","0");
				
            return h;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una faiMedie");
	}
}


private Hashtable AnalizzaZona(ISASConnection dbc,ISASRecord dbr ,Hashtable hzona, Hashtable par)
throws SQLException{
        try{
            Hashtable hdistretti=new Hashtable();
            if (dbr.get("des_zona")!=null && !((String)dbr.get("des_zona")).equals(""))            {
                String  zona=(String)dbr.get("des_zona");
				
                if (hzona!=null && ((Hashtable)hzona.get(zona))!=null)
                      hdistretti=(Hashtable)hzona.get(zona);
					  
                hdistretti=AnalizzaDistretti(dbc,dbr,hdistretti, par);
				
                hzona.put(zona,hdistretti);
            }//fine descrizione distretti
            return hzona;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaZona");
	}
}

private Hashtable AnalizzaDistretti(ISASConnection dbc,ISASRecord dbr,Hashtable hdistretti, Hashtable par)
throws SQLException{
        try{
            Hashtable hcomuni=new Hashtable();
            if (dbr.get("des_distretto")!=null && !((String)dbr.get("des_distretto")).equals(""))            {
                String  distretto=(String)dbr.get("des_distretto");
                if (hdistretti!=null && ((Hashtable)hdistretti.get(distretto))!=null)
                      hcomuni=(Hashtable)hdistretti.get(distretto);
                hcomuni=AnalizzaComuni(dbc,dbr,hcomuni, par);
                hdistretti.put(distretto,hcomuni);
            }//fine descrizione distretti
            return hdistretti;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaDistretti");
	}
}

private Hashtable AnalizzaComuni(ISASConnection dbc,ISASRecord dbr,Hashtable hcomuni, Hashtable par)
throws SQLException{
        try{
            Vector vDati=new Vector();
            if (dbr.get("descrizione")!=null && !((String)dbr.get("descrizione")).equals(""))            {
                String  comune=(String)dbr.get("descrizione");
                String  codice=(String)dbr.get("codice");
                if (hcomuni!=null && ((Vector)hcomuni.get(comune))!=null)
                    vDati=(Vector)hcomuni.get(comune);
              vDati=caricaDati(dbc,dbr,vDati, codice);
              hcomuni.put(comune, vDati);
            }//fine descrizione comuni
            return hcomuni;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaComuni");
	}
}

public  Vector caricaDati(ISASConnection dbc,ISASRecord dbr ,Vector vDati, String comune)
throws SQLException{
	String punto = MIONOME + "caricaDati ";
        String sel_deceduti="";
        String sel_viventi="";
        String sel_incarico="";
        int tot_deceduti=0;
        int tot_viventi=0;
        int tot_incarico=0;
        ISASRecord dbConta=null;
        try{
          Hashtable tab=new Hashtable();
          //Conto i decessi totali nel periodo
          sel_deceduti = "SELECT COUNT (DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a"+
// 23/02/09         getParteWhere(dbc)+
					getParteWhereDecCart(dbc) + // 23/02/09: deceduti su CARTELLA
					" AND m.skm_motivo_chius='2' "+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr); // 22/12/09: nessuna divisione
          stampa(punto+"Select per deceduti:"+sel_deceduti);
          dbConta = dbc.readRecord(sel_deceduti);
          if(dbConta!=null && dbConta.get("conta")!=null)
                tot_deceduti=util.getIntField(dbConta, "conta");

          //conto i viventi nel periodo
          sel_viventi = "SELECT COUNT (DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a"+
// 23/02/09         getParteWhere(dbc)+
					getParteWhereNoDecCart(dbc) + // 23/02/09: NON deceduti su CARTELLA 
					" AND (m.skm_motivo_chius<>'2' OR m.skm_motivo_chius IS NULL)"+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr); // 22/12/09: nessuna divisione
          stampa(punto+"Select per viventi:"+sel_viventi);
          dbConta = dbc.readRecord(sel_viventi);
          if(dbConta!=null && dbConta.get("conta")!=null)
                tot_viventi=util.getIntField(dbConta, "conta");

          //conto gli assititi in carico nel periodo.
          //Se l'assistito � decuduto nel periodo, cmq lo conto come in carico
          sel_incarico = "SELECT COUNT (DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a"+
                    getParteWhere(dbc)+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr); // 22/12/09: nessuna divisione
          stampa(punto+"Select per assistiti in carico:"+sel_incarico);
          dbConta = dbc.readRecord(sel_incarico);
          if(dbConta!=null && dbConta.get("conta")!=null)
                tot_incarico=util.getIntField(dbConta, "conta");

          //conto i deceduti raggruppati per luogo di decesso
          String sel = "SELECT COUNT (DISTINCT m.n_cartella) conta, m.skm_deceduto FROM skmedpal m, anagra_c a"+
// 23/02/09         getParteWhere(dbc)+
					getParteWhereDecCart(dbc) + // 23/02/09: deceduti su CARTELLA
					" AND m.skm_motivo_chius='2' "+
// 22/12/09			getJoinComune((String)dbr.get("codice"));
					getJoinComune(dbr) + // 22/12/09: nessuna divisione
                    " GROUP BY m.skm_deceduto";
          stampa(punto+"Select per deceduti raggruppati:"+sel);
          ISASCursor dbcur = dbc.startCursor(sel)  ;
		  	  
          tab.put("#domicilio_pazienti#","");
          tab.put("#domicilio_percento#","");
          tab.put("#ospedale_pazienti#","");
          tab.put("#ospedale_percento#","");
          tab.put("#comunita_pazienti#","");
          tab.put("#comunita_percento#","");
          tab.put("#sede_pazienti#","");
          tab.put("#sede_percento#","");
          tab.put("#viventi_pazienti#","");
          tab.put("#viventi_percento#","");
			// 23/02/09 --
          tab.put("#hospice_pazienti#","");
          tab.put("#hospice_percento#","");
			// 23/02/09 --
			// 22/12/09 --
          tab.put("#rsa_pazienti#","");
          tab.put("#rsa_percento#","");
			// 22/12/09 --			
			
          while (dbcur.next()){
            ISASRecord dbrTot= dbcur.getRecord();
            if(((String)dbrTot.get("skm_deceduto")).equals("1")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#domicilio_pazienti#", ""+dec);
                tab.put("#domicilio_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("2")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#ospedale_pazienti#", ""+dec);
                tab.put("#ospedale_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("3")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#comunita_pazienti#", ""+dec);
                tab.put("#comunita_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("4")){
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#sede_pazienti#", ""+dec);
                tab.put("#sede_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }else if(((String)dbrTot.get("skm_deceduto")).equals("5")){ // 23/02/09
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#hospice_pazienti#", ""+dec);
                tab.put("#hospice_percento#", ""+((dec*100)/tot_deceduti)+"%");
            } else if(((String)dbrTot.get("skm_deceduto")).equals("6")){ // 22/12/09
                int dec = util.getIntField(dbrTot, "conta");
                tab.put("#rsa_pazienti#", ""+dec);
                tab.put("#rsa_percento#", ""+((dec*100)/tot_deceduti)+"%");
            }
          }
          tab.put("#viventi_pazienti#", ""+tot_viventi);
          tab.put("#viventi_percento#", ""+((tot_viventi*100)/tot_incarico)+"%");
          //System.out.println("Hashtable che vado a scrivere:"+tab);
          vDati.add(tab);
          iterazione++;
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}

}

 public Enumeration orderedKeys(Hashtable hOrdinare){
    Enumeration keys = hOrdinare.keys();
    Vector temp = new Vector();
    while( keys.hasMoreElements() ){
      temp.addElement("" + keys.nextElement() );
    }
   Collections.sort(temp);
   return temp.elements();
  }

  private int convNumDBToInt(String nomeCampo, ISASRecord mydbr)
  throws Exception{
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

}	// End of FoUtePato class


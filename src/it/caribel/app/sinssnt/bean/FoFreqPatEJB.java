package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 12/11/2002 - EJB di connessione alla procedura SINS Tabella FoFreqPat
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
import java.math.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;	// fo merge
import it.pisa.caribel.util.*;

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

public class FoFreqPatEJB extends SINSSNTConnectionEJB {
	String dom_res;
	String dr;
	// 28/11/06 m._ aggiunto operatore ONCOLOGO (tipo=52).
	// 27/11/06 m.: sostituito campi "patologie" con "diagnosi" -> NON esiste piu'
	// il criterio sul "n_contatto" -> modificato SELECT.


public FoFreqPatEJB() {}

      private void preparaLayout(mergeDocument doc, ISASConnection dbc,String data_ini,String data_fine,String valore,String ass) {
	Hashtable htxt = new Hashtable();
        ServerUtility su =new ServerUtility();
	try {
		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ragione_sociale'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
	} catch (Exception ex) {
        	htxt.put("#txt#", "ragione_sociale");
        }
        htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        String ora=currentTime();
        htxt.put("#ora#",ora);
        //DATE
        data_ini=data_ini.substring(8,10)+"/"+data_ini.substring(5,7)+"/"+
                 data_ini.substring(0,4);
        data_fine=data_fine.substring(8,10)+"/"+data_fine.substring(5,7)+"/"+
                 data_fine.substring(0,4);
        htxt.put("#data_inizio#",data_ini);
        htxt.put("#data_fine#",data_fine);
        htxt.put("#valore#",valore);

	    if (ass!=null){
        	int tipo=(new Integer(ass)).intValue();
                switch (tipo){
                   case 1:
        		        htxt.put("#tipo#","ASSISTENTI SOCIALI");
                        break;
                   case 2:
        		        htxt.put("#tipo#","INFERMIERI");
                        break;
                   case 3:
        		        htxt.put("#tipo#","MEDICI");
                        break;
                   case 4:
        		        htxt.put("#tipo#","FISIOTERAPISTI");
                        break;
				case 52:// 28/11/06
          		        htxt.put("#tipo#","MEDICI CURE PALLIATIVE");
                        break;
                  }
	}else htxt.put("#tipo#","");

	doc.writeSostituisci("layout",htxt);
      }

      public String currentTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(" HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(cal.getTime());
      }
/**
*10/10/2003 andrea ha deciso che ora la frequenza delle patologie venga
*calcolata considerando solo la patologia1 .....
*/
public byte[] query_freqpat(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException {

        boolean done=false;
	ISASConnection dbc=null;
	int fisio=0;
        int inf=0;
        int med=0;
        int ass=0;
        int tot=0;
        Hashtable h = new Hashtable();
        ISASCursor dbcur=null;
        String data_ini="";
	String data_fine="";
        String valore="";
        String tipo="";
        String cod1="";
        String cod2="";
       ServerUtility su =new ServerUtility();

        byte[] rit;
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
                data_ini=(String)par.get("data_inizio");
                data_fine=(String)par.get("data_fine");
                valore=(String)par.get("val");
                tipo=(String)par.get("tipo");
                cod1=(String)par.get("cod1");
                cod2=(String)par.get("cod2");
                //System.out.println("Cod1: "+cod1+" cod2 "+cod2);

                int ausi=0;
                if(cod1!=null && !(cod1.equals("")) ||
                   cod2!=null && !(cod2.equals("")))
                    ausi=1;
                else
                    ausi=4;
                //System.out.println("Ausi vale: "+ausi);

                String tabella="";
                String nome_data_ini="";
                String nome_data_fine="";
                int tipo1=(new Integer(tipo)).intValue();
                //System.out.println("Tipo operatore: "+tipo1);
                switch (tipo1){
                   case 1:
/*gb 08/06/07 *******
                        tabella="contatti";
                        nome_data_ini="data_contatto";
                        nome_data_fine="data_chiusura";
*gb 08/06/07 *******/
//gb 08/06/07 *******
                        tabella="ass_progetto";
                        nome_data_ini="ap_data_apertura";
                        nome_data_fine="ap_data_chiusura";
//gb 08/06/07: fine *******
                        break;
                   case 2:
                        tabella="skinf";
                        nome_data_ini="ski_data_apertura";
                        nome_data_fine="ski_data_uscita";
                        break;
                   case 3:
                        tabella="skmedico";
                        nome_data_ini="skm_data_apertura";
                        nome_data_fine="skm_data_chiusura";
                        break;
                   case 4:
                        tabella="skfis";
                        nome_data_ini="skf_data";
                        nome_data_fine="skf_data_chiusura";
                        break;
                   case 52:// 28/11/06
                        tabella="skmedpal";
                        nome_data_ini="skm_data_apertura";
                        nome_data_fine="skm_data_chiusura";
                        break;
                  }

                //RIEMPIO L'HASH PER LA STAMPA
                preparaLayout(doc,dbc,data_ini,data_fine,valore,tipo);
                if((cod1!=null && !(cod1.equals(""))) &&
                   (cod2!=null && !(cod2.equals("")))){
                  Hashtable cod=new Hashtable();
                  cod.put("#cod1#","DA CODICE "+cod1+" A CODICE "+cod2);
                  doc.writeSostituisci("codici",cod);
                 }else if((cod1!=null && !(cod1.equals(""))) &&
                          (cod2==null || (cod2.equals("")))){
                  Hashtable cod=new Hashtable();
                  cod.put("#cod1#","DA CODICE "+cod1);
                  doc.writeSostituisci("codici",cod);
                 }else if((cod1==null || (cod1.equals(""))) &&
                          (cod2!=null && !(cod2.equals("")))){
                  Hashtable cod=new Hashtable();
                  cod.put("#cod1#","A CODICE "+cod2);
                  doc.writeSostituisci("codici",cod);
                 }

                Hashtable hcod = new Hashtable();
                Hashtable hpat = new Hashtable();
                Hashtable stampa = new Hashtable();
                boolean entrato=false;
/*** 27/11/06
                  String mysel="SELECT distinct skpat_patol1,"+tabella+".n_cartella"+
                               " FROM skpatologie,"+tabella+" WHERE "+
                               "skpatologie.n_cartella="+tabella+".n_cartella AND "+
                               "skpatologie.n_contatto="+tabella+".n_contatto AND "+
                               tabella+"."+nome_data_ini+"<="+formatDate(dbc,data_fine)+" AND ("+
                               tabella+"."+nome_data_fine+">="+formatDate(dbc,data_ini)+" OR "+
                               tabella+"."+nome_data_fine+" is null) ";
                  if(cod1!=null&& !(cod1.equals("")))
                    mysel=mysel+" AND skpat_patol1"+">='"+cod1+"'";
                  if(cod2!=null&& !(cod2.equals("")))
                    mysel=mysel+" AND skpat_patol1"+"<='"+cod2+"'";

                    mysel=mysel+" GROUP BY skpat_patol1,"+tabella+".n_cartella"+
                               " HAVING COUNT(skpat_patol1"+")>0"+
                               " ORDER BY skpat_patol1";
***/
				// 27/11/06: Si prende la diagnosi con dataMax(< finePeriodo) per tutti i contatti attivi
				// nel periodo selezionato. -> si considera xogni assistito 1 sola diagnosi nel periodo
               	String mysel = "SELECT DISTINCT d.diag1," +
				" co.n_cartella" +
                		" FROM diagnosi d," +
				tabella + " co,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u,anagra_c a" +
				" WHERE d.n_cartella = co.n_cartella" +
				" AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
	    		        	" FROM diagnosi," +
					tabella + " sk" +
					" WHERE diagnosi.n_cartella = d.n_cartella" +
					" AND diagnosi.n_cartella = sk.n_cartella" +
					" AND diagnosi.data_diag <= " + formatDate(dbc,data_fine) +
					" AND sk." + nome_data_ini + " <= " + formatDate(dbc,data_fine) +
					" AND (sk." + nome_data_fine + ">= " + formatDate(dbc,data_ini) +
					" OR sk." + nome_data_fine + " IS NULL))"+
                    " AND co.n_cartella=a.n_cartella"+
                    " AND a.data_variazione IN"+
                    " (SELECT MAX (data_variazione)"+
                    " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";

              String ragg = (String)par.get("ragg");
              String pca = (String)par.get("pca");
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


                  if(cod1!=null&& !(cod1.equals("")))
                    mysel = mysel + " AND d.diag1 >= '" + cod1 + "'";
                  if(cod2!=null&& !(cod2.equals("")))
                    mysel = mysel + " AND d.diag1 <= '" + cod2 + "'";

                    mysel = mysel + " GROUP BY d.diag1, co.n_cartella" +
                               " HAVING COUNT(d.diag1) > 0" +
                               " ORDER BY d.diag1";


                  System.out.println("***SELECT FREQPAT: "+mysel);
                  dbcur=dbc.startCursor(mysel);


                  int barbara = dbcur.getDimension();
                  //System.out.println("----- dimensione ---"+barbara);
                  while (dbcur.next()){
                    if (!entrato)
                      doc.write("testa");
                    entrato=true;
                    ISASRecord dbr=dbcur.getRecord();
                    /**
                    * controllo se la patologia � gia stata inserita
                    * se si allora incremento il valore contenuto
                    * altrimenti lo inserisco e inserisco la descrizione
                    * della patologia nella tabella patologie hpat
                    */
                    //System.out.println("patologia trovata:["+dbr.get("skpat_patol1")+"]");
// 27/11/06         if(hcod.get(dbr.get("skpat_patol1")) != null)
                    if(hcod.get(dbr.get("diag1")) != null)
                    {
// 27/11/06             String uffa=(String)dbr.get("skpat_patol1");
                        String uffa=(String)dbr.get("diag1");
                        int pato = ((Integer)hcod.get(uffa)).intValue();
                        //System.out.println("numero freq relativa:["+pato+"]");
                        pato=pato+1;
// 27/11/06             hcod.put(dbr.get("skpat_patol1"),(new Integer(pato)));
                        hcod.put(dbr.get("diag1"),(new Integer(pato)));
                        //System.out.println("inserita in hash "+pato);
                     }else{
// 27/11/06             hcod.put(dbr.get("skpat_patol1"),(new Integer(1)));
                        hcod.put(dbr.get("diag1"),(new Integer(1)));
                        //System.out.println("inserito in hash 1");
// 27/11/06             String selpat="SELECT diagnosi FROM icd9 WHERE icd9.cd_diag='"+dbr.get("skpat_patol1")+"'";
                        String selpat="SELECT diagnosi FROM tab_diagnosi WHERE cod_diagnosi = '" + dbr.get("diag1") + "'";
                        ISASRecord dbpat=dbc.readRecord(selpat);
                        //System.out.println("leggo descrizione patologia");
                        if (dbpat!=null && dbpat.get("diagnosi")!=null){
                          //System.out.println("descrizione patologia trovata:["+dbpat.get("diagnosi")+"]");
// 27/11/06               hpat.put(dbr.get("skpat_patol1"),dbpat.get("diagnosi"));
                          hpat.put(dbr.get("diag1"), dbpat.get("diagnosi"));
                        }else
// 27/11/06               hpat.put(dbr.get("skpat_patol1"),"");
                          hpat.put(dbr.get("diag1"),"");
                      }//fine else
                  }//fine while
                if(!entrato)
                  doc.write("messaggio");
                else{
                  //Vado scorrere le due Hashtable per riempire quella che poi
                  //mander� in stampa
                  Enumeration n=hcod.keys();
                  //System.out.println("Hash dei codici: "+hcod.toString());
                  int n_casi_ko=0;
                  int n_casi=0;
                  while(n.hasMoreElements()){
                    String e=(String)n.nextElement();
                    Integer conteggio=(Integer)hcod.get(e);
                    //System.out.println("Conteggio: "+conteggio.intValue());
                    int casi=conteggio.intValue();
                    int val=(new Integer(valore)).intValue();
                    n_casi++;
                    if(casi>val){
                    String instampa=(e+" - "+hpat.get(e));
                    stampa.put("#diagnosi#",instampa);
                    stampa.put("#casi#",conteggio);
                    doc.writeSostituisci("tabella",stampa);
                    }else n_casi_ko++;
                  }
                  if (n_casi_ko==n_casi)
                  { stampa.put("#diagnosi#","");
                    stampa.put("#casi#","0");
                    doc.writeSostituisci("tabella",stampa);
                    }
                  doc.write("finetab");
                  //System.out.println("***Ok sono uscita dall'Enumeration!");
                }
        doc.write("finale");
        dbcur.close();
	doc.close();
	//riprendo il bytearray
      	rit=(byte[])doc.get();
      	//riprendo l'array di byte
      //	System.out.println("byte[] restituito ");
	//String by= new String(rit);
      	//System.out.println("Stringa del byte array   :"+by);
	dbc.close();
	super.close(dbc);
	done=true;
	return rit;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_freqpat()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}
/* 10/10/2003 andrea ha deciso che ora la frequenza delle patologie venga
calcolata considerando solo la patologia1 .....
public byte[] query_freqpat(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException {

        boolean done=false;
	ISASConnection dbc=null;
	int fisio=0;
        int inf=0;
        int med=0;
        int ass=0;
        int tot=0;
        Hashtable h = new Hashtable();
        ISASCursor dbcur=null;
        String data_ini="";
	String data_fine="";
        String valore="";
        String tipo="";
        String cod1="";
        String cod2="";

        byte[] rit;
        try{
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);
                data_ini=(String)par.get("data_inizio");
                data_fine=(String)par.get("data_fine");
                valore=(String)par.get("val");
                tipo=(String)par.get("tipo");
                cod1=(String)par.get("cod1");
                cod2=(String)par.get("cod2");
                //System.out.println("Cod1: "+cod1+" cod2 "+cod2);

                int ausi=0;
                if(cod1!=null && !(cod1.equals("")) ||
                   cod2!=null && !(cod2.equals("")))
                    ausi=1;
                else
                    ausi=4;
                //System.out.println("Ausi vale: "+ausi);

                String tabella="";
                String nome_data_ini="";
                String nome_data_fine="";
                int tipo1=(new Integer(tipo)).intValue();
                //System.out.println("Tipo operatore: "+tipo1);
                switch (tipo1){
                   case 1:
                        tabella="contatti";
                        nome_data_ini="data_contatto";
                        nome_data_fine="data_chiusura";
                        break;
                   case 2:
                        tabella="skinf";
                        nome_data_ini="ski_data_apertura";
                        nome_data_fine="ski_data_uscita";
                        break;
                   case 3:
                        tabella="skmedico";
                        nome_data_ini="skm_data_apertura";
                        nome_data_fine="skm_data_chiusura";
                        break;
                   case 4:
                        tabella="skfis";
                        nome_data_ini="skf_data";
                        nome_data_fine="skf_data_chiusura";
                        break;
                  }

                //RIEMPIO L'HASH PER LA STAMPA
                preparaLayout(doc,dbc,data_ini,data_fine,valore,tipo);
                if((cod1!=null && !(cod1.equals(""))) &&
                   (cod2!=null && !(cod2.equals("")))){
                  Hashtable cod=new Hashtable();
                  cod.put("#cod1#","DA CODICE "+cod1+" A CODICE "+cod2);
                  doc.writeSostituisci("codici",cod);
                 }else if((cod1!=null && !(cod1.equals(""))) &&
                          (cod2==null || (cod2.equals("")))){
                  Hashtable cod=new Hashtable();
                  cod.put("#cod1#","DA CODICE "+cod1);
                  doc.writeSostituisci("codici",cod);
                 }else if((cod1==null || (cod1.equals(""))) &&
                          (cod2!=null && !(cod2.equals("")))){
                  Hashtable cod=new Hashtable();
                  cod.put("#cod1#","A CODICE "+cod2);
                  doc.writeSostituisci("codici",cod);
                 }

                Hashtable hcod = new Hashtable();
                Hashtable hpat = new Hashtable();
                Hashtable stampa = new Hashtable();
                boolean entrato=false;
                //Faccio il for per scorrermi le patologie nel caso in cui
                //non siano stati inseriti i codici, altrimenti se almeno
                //uno dei codici � stato inserito mi scorro solamente la
                //patologia principale cio� skpat_patol1
                for (int i=1;i<=ausi;i++){
                  String mysel="SELECT COUNT(skpat_patol"+i+") conta,skpat_patol"+i+
                               " FROM skpatologie,"+tabella+" WHERE "+
                               "skpatologie.n_cartella="+tabella+".n_cartella AND "+
                               "skpatologie.n_contatto="+tabella+".n_contatto AND "+
                               tabella+"."+nome_data_ini+"<="+formatDate(dbc,data_fine)+" AND ("+
                               tabella+"."+nome_data_fine+">="+formatDate(dbc,data_ini)+" OR "+
                               tabella+"."+nome_data_fine+" is null) ";
                  if(cod1!=null&& !(cod1.equals("")))
                    mysel=mysel+" AND skpat_patol"+i+">='"+cod1+"'";
                  if(cod2!=null&& !(cod2.equals("")))
                    mysel=mysel+" AND skpat_patol"+i+"<='"+cod2+"'";

                    mysel=mysel+" GROUP BY skpat_patol"+i+//",diagnosi "+
                               " HAVING COUNT(skpat_patol"+i+")>0"+
                               " ORDER BY skpat_patol"+i+",conta";//+",diagnosi";
                  //ISASCursor dbcur=dbc.startCursor(mysel);
                  dbcur=dbc.startCursor(mysel);

                  System.out.println("***SELECT FREQPAT: "+mysel);

                  int barbara = dbcur.getDimension();
                  //System.out.println("----- dimensione ---"+barbara);
                  while (dbcur.next()){
                    if (!entrato)
                      doc.write("testa");
                    entrato=true;
                    //System.out.println("***Sono dentro WHILE");
                    ISASRecord dbr=dbcur.getRecord();
                    if(hcod.get(dbr.get("skpat_patol"+i)) != null){
                      //System.out.println("***Sono nella parte IF");
                      //System.out.println("Hash dei codici in IF: "+hcod.toString());
                      String uffa=(String)dbr.get("skpat_patol"+i);
                      //System.out.println("***Uffa"+uffa);
                      //System.out.println("***patologia"+hcod.get(uffa));
                      int pato= 0;
		      if (hcod.get(uffa)!=null)
			pato = ((Double)hcod.get(uffa)).intValue();
                      //System.out.println("***Pato"+pato);
                      int conta=0;
		      if (dbr.get("conta")!=null)
			conta = ((Double)dbr.get("conta")).intValue();
                      //System.out.println("CONTA: "+conta);
                      int insieme=pato+conta;
                      String input=(new Integer(insieme)).toString();
                      //System.out.println("INPUT: "+input);
                      java.lang.Double indo=Double.valueOf(input);
                      hcod.put(dbr.get("skpat_patol"+i),indo);
                      //(new Integer(hcod.get(dbr.get("skpat_patol"+i))).intValue())+dbr.get("conta"));
                    }else{
                      //System.out.println("***Sono nella parte Else");
                      hcod.put(dbr.get("skpat_patol"+i),dbr.get("conta"));
                      String selpat="SELECT diagnosi FROM icd9 WHERE "+
                                    "icd9.cd_diag='"+dbr.get("skpat_patol"+i)+"'";
                      //System.out.println("***Select Patologie: "+selpat);
                      ISASRecord dbpat=dbc.readRecord(selpat);
		      if (dbpat!=null && dbpat.get("diagnosi")!=null)
                      	hpat.put(dbr.get("skpat_patol"+i),dbpat.get("diagnosi"));
		      else
                      	hpat.put(dbr.get("skpat_patol"+i),"");
                    }//fine else
                  }//fine while
                }//fine for

                if(!entrato)
                  doc.write("messaggio");
                else{
                  //Vado scorrere le due Hashtable per riempire quella che poi
                  //mander� in stampa
                  Enumeration n=hcod.keys();
                  //System.out.println("Hash dei codici: "+hcod.toString());
                  while(n.hasMoreElements()){
                    String e=(String)n.nextElement();
                    //System.out.println("Chiave: "+e);
                    String conteggio=((Double)hcod.get(e)).toString();
                    //System.out.println("Conteggio: "+conteggio);
                    if(conteggio.indexOf(".",0)!=-1)
                    {
                      //System.out.println("OK DENTRO!");
                      StringTokenizer tok = new StringTokenizer(conteggio,".");
                      conteggio = tok.nextToken();
                    }else
                      conteggio=(String)hcod.get(e);

                    int casi=(new Integer(conteggio)).intValue();
                    int val=(new Integer(valore)).intValue();
                    if(casi>val){
                    String instampa=(e+" - "+hpat.get(e));
                    //System.out.println("Riga in stampa: "+instampa);
                    stampa.put("#diagnosi#",instampa);
                    //System.out.println("CASI: "+conteggio);
                    stampa.put("#casi#",conteggio);
                    doc.writeSostituisci("tabella",stampa);
                    }
                  }
                  doc.write("finetab");
                  //System.out.println("***Ok sono uscita dall'Enumeration!");
                }
        //doc.write("finetab");
	doc.write("finale");
        dbcur.close();
	doc.close();
	//riprendo il bytearray
      	rit=(byte[])doc.get();
      	//riprendo l'array di byte
      //	System.out.println("byte[] restituito ");
	//String by= new String(rit);
      	//System.out.println("Stringa del byte array   :"+by);
	dbc.close();
	super.close(dbc);
	done=true;
	return rit;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_operatori()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}*/
}	// End of FoEleSoc class

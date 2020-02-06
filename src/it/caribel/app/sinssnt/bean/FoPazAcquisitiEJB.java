package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 06/11/2002 - EJB di connessione alla procedura SINS Tabella FoPazAcquisiti
//
// Jessica Caccavale
//
// ==========================================================================


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

public class FoPazAcquisitiEJB extends SINSSNTConnectionEJB {
String anno_ini_arrivato="";
String mese_ini_arrivato="";
String gg_ini_arrivato="";
String anno_fine_arrivato="";
String mese_fine_arrivato="";
String gg_fine_arrivato="";

String dom_res;
String dr;

public static String getjdbcDate()
{
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
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

public FoPazAcquisitiEJB() {}

public byte[] query_pazacquisiti(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException {

        boolean done=false;
	ISASConnection dbc=null;
	String mysel="";
	String data_ini="";
	String data_fine="";
        String tipo = "";
        //DICHIARAZIONI DELLE HASHTABLE DA RIEMPIRE VUOTE
        Hashtable hfis = new Hashtable();
        Hashtable hmed = new Hashtable();
        Hashtable hmedPal = new Hashtable(); //gb 08/06/07: oncologi
        Hashtable hinf = new Hashtable();
        Hashtable hass = new Hashtable();
        Hashtable stampa = new Hashtable();
        //DICHIARAZIONE DEL VETTORE CONTENENTE I MESI
        //String[] Vmesi={"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno","Agosto","Settembre","Ottobre","Novembre","Dicembre"};

        byte[] rit;
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
                data_ini=(String)par.get("data_inizio");
                data_fine=(String)par.get("data_fine");
                tipo = (String)par.get("tipo");
                anno_ini_arrivato=data_ini.substring(0,4);
                mese_ini_arrivato=data_ini.substring(5,7);
                gg_ini_arrivato=data_ini.substring(8,10);
                anno_fine_arrivato=data_fine.substring(0,4);
                mese_fine_arrivato=data_fine.substring(5,7);
                gg_fine_arrivato=data_fine.substring(8,10);

                if (tipo.equals("C")){
                  //CARICO TUTTE LE HASHTABLE
                  hfis=CaricaHash(dbc,"skfis", "skf_cod_presidio", "skf_data","skf_data_chiusura",data_ini,data_fine,hfis,par);
                  hmed=CaricaHash(dbc,"skmedico","skm_cod_presidio","skm_data_apertura","skm_data_chiusura",data_ini,data_fine,hmed,par);
//gb 08/06/07: oncologi *******
                  hmedPal=CaricaHash(dbc,"skmedpal","skm_cod_presidio","skm_data_apertura","skm_data_chiusura",data_ini,data_fine, hmedPal,par);
//gb 08/06/07: fine *******
                  hinf=CaricaHash(dbc,"skinf","ski_cod_presidio", "ski_data_apertura","ski_data_uscita",data_ini,data_fine,hinf,par);
//gb 08/06/07                  hass=CaricaHash(dbc,"contatti","data_contatto","data_chiusura",data_ini,data_fine,hass);
                  hass=CaricaHash(dbc,"ass_progetto","ap_ass_ref_presidio","ap_data_apertura","ap_data_chiusura",data_ini,data_fine,hass,par); //gb 08/06/07
                }else{
                  hfis=CaricaHashInterv(dbc,"04",data_ini,data_fine,hfis,"skfis","skf_data","skf_data_chiusura",par);
                  hmed=CaricaHashInterv(dbc,"03",data_ini,data_fine,hmed,"skmedico","skm_data_apertura","skm_data_chiusura",par);
//gb 08/06/07: oncologi *******
                  hmedPal=CaricaHashInterv(dbc,"52",data_ini,data_fine, hmedPal, "skmedpal","skm_data_apertura","skm_data_chiusura",par);
//gb 08/06/07: fine *******
                  hinf=CaricaHashInterv(dbc,"02",data_ini,data_fine,hinf,"skinf","ski_data_apertura","ski_data_uscita",par);
//gb 08/06/07                  hass=CaricaHashInterv(dbc,"01",data_ini,data_fine,hass,"contatti","data_contatto","data_chiusura");
                  hass=CaricaHashInterv(dbc,"01",data_ini,data_fine,hass,"ass_progetto","ap_data_apertura","ap_data_chiusura",par); //gb 08/06/07
                }
                //RIEMPIO L'HASH PER LA STAMPA
                preparaLayout(doc,dbc,par);
                doc.write("testa");

//gb 08/06/07                InizializzaHash(data_ini,data_fine,hfis,hinf,hass,hmed,doc);
                InizializzaHash(data_ini,data_fine,hfis,hinf,hass,hmed, hmedPal, doc); //gb 08/06/07: con oncologi

        doc.write("finetab");
        doc.write("finale");
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
}

      private void InizializzaHash(String data_ini,String data_fine,Hashtable hfis,Hashtable hinf,Hashtable hass,Hashtable hmed, Hashtable hmedPal, mergeDocument doc){
        Hashtable has = new Hashtable();
        String[] Vmesi={"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno","Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre"};
        String mi=data_ini.substring(5,7);
        String ai=data_ini.substring(0,4);
        String mf=data_fine.substring(5,7);
        String af=data_fine.substring(0,4);
        int mese_iniz=(new Integer(mi)).intValue();
        //System.out.println("Mese inizio="+mese_iniz);
        int anno_iniz=(new Integer(ai)).intValue();
        //System.out.println("Anno inizio="+anno_iniz);
        int mese_fine=(new Integer(mf)).intValue();
        //System.out.println("Mese fine="+mese_fine);
        int anno_fine=(new Integer(af)).intValue();
        //System.out.println("Anno fine="+anno_fine);
        int mese_ausi_iniz=mese_iniz;
        int mese_ausi_fine=0;
        for (int i=anno_iniz;i<=anno_fine;i++)
        {
          System.out.println("I="+i);
          if(i<anno_fine)
            mese_ausi_fine=12;
          else
            mese_ausi_fine=mese_fine;

          //System.out.println("Mese_ausiliario_fine="+mese_ausi_fine);
          //System.out.println("Mese_ausiliario_iniz="+mese_ausi_iniz);
          for (int j=mese_ausi_iniz;j<=mese_ausi_fine;j++)
          {
            //System.out.println("Dentro secondo for="+j);
            has.put("#mese#",Vmesi[j-1]+"-"+i);
            if(hfis.get(Vmesi[j-1]+i)!=null)
              has.put("#mese_fis#",hfis.get(Vmesi[j-1]+i));
            else
              has.put("#mese_fis#","0");
            if(hmed.get(Vmesi[j-1]+i)!=null)
              has.put("#mese_med#",hmed.get(Vmesi[j-1]+i));
            else
              has.put("#mese_med#","0");
//gb 08/06/07: oncologi *******
            if(hmedPal.get(Vmesi[j-1]+i)!=null)
              has.put("#mese_med_pal#",hmedPal.get(Vmesi[j-1]+i));
            else
              has.put("#mese_med_pal#","0");
//gb 08/06/07 *******
            if(hinf.get(Vmesi[j-1]+i)!=null)
              has.put("#mese_ip#",hinf.get(Vmesi[j-1]+i));
            else
              has.put("#mese_ip#","0");
            if(hass.get(Vmesi[j-1]+i)!=null)
              has.put("#mese_as#",hass.get(Vmesi[j-1]+i));
            else
              has.put("#mese_as#","0");
            doc.writeSostituisci("tabella",has);
            //System.out.println("Che butto dentro?="+Vmesi[j-1]+i);
          }
          mese_ausi_iniz=1;
        }
      }


      private Hashtable CaricaHash(ISASConnection dbc,String nometab,String nmCampoPresidio,
        String nome_data_ini,String nome_data_fine, String data_ini,
        String data_fine,Hashtable h,Hashtable par)
      throws SQLException
      {
       boolean done=false;
       ServerUtility su =new ServerUtility();

       try{
        String[] Vmesi={"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno","Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre"};
//        String myselect="SELECT DISTINCT to_char("+nome_data_ini+",'MM') mese ,"+
//                      " to_char("+nome_data_ini+",'YYYY') anno "+
        String myselect="SELECT DISTINCT "+dbc.formatDbMonth(nome_data_ini)+" mese ,"+
                        dbc.formatDbYear(nome_data_ini)+" anno "+
                        " FROM "+nometab+" t,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u,anagra_c a"+
                        " WHERE "+nome_data_ini+"<="+formatDate(dbc,data_fine)+" AND "+
                        nome_data_ini+">="+formatDate(dbc,data_ini)+
                        " AND t.n_cartella=a.n_cartella"+
                        " AND a.data_variazione IN"+
                        " (SELECT MAX (data_variazione)"+
                        " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";

              String ragg = (String)par.get("ragg");
              String pca = (String)par.get("pca");
              myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

//              if (ragg!=null && ragg.equals("C")){
//                    myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                            " AND u.codice=a.dom_citta)"+
//                            " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                            " AND u.codice=a.citta))";
//              }else if (ragg!=null && ragg.equals("A")){
//                myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                            " AND u.codice=a.dom_areadis)"+
//                            " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                            " AND u.codice=a.areadis))";
//             }
              
              
              //Aggiunto Controllo Domicilio/Residenza (BYSP)
              if((String)par.get("dom_res") == null)
              {
                      if (ragg.equals("C"))
                        myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                              " AND u.codice=a.dom_citta)"+
              		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                              " AND u.codice=a.citta))";
                      else if (ragg.equals("A"))
                        myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                              " AND u.codice=a.dom_areadis)"+
              		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                              " AND u.codice=a.areadis))";
                    //M.Minerba 21/02/2013 per Pistoia 
      				else if (ragg.equals("P"))
      				       myselect += " AND u.codice = t." + nmCampoPresidio;
      				//fine M.Minerba 21/02/2013 per Pistoia 
              }
              else if (((String)par.get("dom_res")).equals("D"))
                                        {
                                         if (ragg.equals("C"))
                        myselect += " AND u.codice=a.dom_citta";
                                          else if (ragg.equals("A"))
                        myselect += " AND u.codice=a.dom_areadis";
                                       //M.Minerba 21/02/2013 per Pistoia 
                          				else if (ragg.equals("P"))
                       myselect += " AND u.codice = t." + nmCampoPresidio;
                          				//fine M.Minerba 21/02/2013 per Pistoia 
                                        }

              else if (((String)par.get("dom_res")).equals("R"))
                              {
                              if (ragg.equals("C"))
                        myselect += " AND u.codice=a.citta";
                      else if (ragg.equals("A"))
                        myselect += " AND u.codice=a.areadis";
                      //M.Minerba 21/02/2013 per Pistoia 
      				else if (ragg.equals("P"))
      				       myselect += " AND u.codice = t." + nmCampoPresidio;
      				//fine M.Minerba 21/02/2013 per Pistoia 
                              }
                              
              
             myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
                su.OP_EQ_STR, (String)par.get("zona"));
             myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
                su.OP_EQ_STR, (String)par.get("distretto"));
             myselect = su.addWhere(myselect, su.REL_AND, "u.codice",
                su.OP_EQ_STR, (String)par.get("pca"));

                        myselect+=" ORDER BY anno,mese";
                        System.out.println("myselect"+myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        while (dbcur.next()){
          ISASRecord dbr=dbcur.getRecord();
//          String dataletta=((java.sql.Date)dbr.get(nome_data_ini)).toString() ;
  //        String meseletto=dataletta.substring(5,7);
          String meseletto=(String)dbr.get("mese") ;
          System.out.println("meseletto"+meseletto);
          String annoletto=(String)dbr.get("anno") ;
//          String annoletto=dataletta.substring(0,4);;
          System.out.println("annoletto"+annoletto);
          String data_ini_mese="";
          String data_fine_mese="";
          String mese_aggiornato="";
          int mese_ok=Integer.parseInt(meseletto);
          String segno_fine="";
          //Questo controllo serve nel caso in cui mi venga scelta una data inizio periodo
          //che non coincide con la data inizio mese e una data fine periodo che non
          //coincide con la fine del mese
          if (anno_ini_arrivato.equals(annoletto))
          {
            if(mese_ini_arrivato.equals(meseletto))
              data_ini_mese=gg_ini_arrivato+"/"+mese_ini_arrivato+"/"+anno_ini_arrivato;
            else
              data_ini_mese="01"+"/"+meseletto+"/"+annoletto;
          }
          else
            data_ini_mese="01"+"/"+meseletto+"/"+annoletto;
          if (anno_fine_arrivato.equals(annoletto))
          {
            if(mese_fine_arrivato.equals(meseletto))
            {     segno_fine="=";
              data_fine_mese=gg_fine_arrivato+"/"+mese_fine_arrivato+"/"+anno_fine_arrivato;
            }
            else{
               if (!meseletto.equals("12"))
              {
                mese_ok++;
                if(mese_ok<10)
                {
                  mese_aggiornato="0"+mese_ok;
                  data_fine_mese="01"+"/"+mese_aggiornato+"/"+annoletto;
                }else
                  data_fine_mese="01"+"/"+mese_ok+"/"+annoletto;
              }else
                data_fine_mese="01"+"/01/"+annoletto;
            }
          }
          else{
            if (!meseletto.equals("12"))
            {
              mese_ok++;
              if(mese_ok<10)
              {
                  mese_aggiornato="0"+mese_ok;
                  data_fine_mese="01"+"/"+mese_aggiornato+"/"+annoletto;
              }else
                  data_fine_mese="01"+"/"+mese_ok+"/"+annoletto;
            }else
              data_fine_mese="01"+"/01/"+annoletto;
          }

          String selcount="SELECT DISTINCT(t.n_cartella) conta FROM "+nometab+
						" t, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, anagra_c a" + // 15/04/10
						" WHERE "+
                          nome_data_ini+"<"+segno_fine+formatDate(dbc,data_fine_mese)+" AND "+
                          nome_data_ini+">="+formatDate(dbc,data_ini_mese) +
						  // 15/04/10 
						  " AND t.n_cartella=a.n_cartella"+
						" AND a.data_variazione IN"+
                        " (SELECT MAX (data_variazione)"+
                        " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";

			// 15/04/10 ---
			selcount = su.addWhere(selcount, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);

//            if (ragg!=null && ragg.equals("C")){
//                selcount += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                            " AND u.codice=a.dom_citta)"+
//                            " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                            " AND u.codice=a.citta))";
//              }else if (ragg!=null && ragg.equals("A")){
//                selcount += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                            " AND u.codice=a.dom_areadis)"+
//                            " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                            " AND u.codice=a.areadis))";
//             }
			
            //Aggiunto Controllo Domicilio/Residenza (BYSP)
			if((String)par.get("dom_res") == null)
			{
			        if (ragg.equals("C"))
			          selcount += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
			                " AND u.codice=a.dom_citta)"+
					" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
			                " AND u.codice=a.citta))";
			        else if (ragg.equals("A"))
			          selcount += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
			                " AND u.codice=a.dom_areadis)"+
					" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
			                " AND u.codice=a.areadis))";
			      //M.Minerba 21/02/2013 per Pistoia 
					else if (ragg.equals("P"))
					       selcount += " AND u.codice = t." + nmCampoPresidio;
					//fine M.Minerba 21/02/2013 per Pistoia 
			}
			else if (((String)par.get("dom_res")).equals("D"))
			                          {
			                           if (ragg.equals("C"))
			          selcount += " AND u.codice=a.dom_citta";
			                            else if (ragg.equals("A"))
			          selcount += " AND u.codice=a.dom_areadis";
			                         //M.Minerba 21/02/2013 per Pistoia 
			            				else if (ragg.equals("P"))
			           selcount += " AND u.codice = t." + nmCampoPresidio;
			            				//fine M.Minerba 21/02/2013 per Pistoia 
			                          }

			else if (((String)par.get("dom_res")).equals("R"))
			                {
			                if (ragg.equals("C"))
			          selcount += " AND u.codice=a.citta";
			        else if (ragg.equals("A"))
			          selcount += " AND u.codice=a.areadis";
			               //M.Minerba 21/02/2013 per Pistoia 
            		else if (ragg.equals("P"))
           selcount += " AND u.codice = t." + nmCampoPresidio;
            				//fine M.Minerba 21/02/2013 per Pistoia 
			                }
			                
			
             selcount = su.addWhere(selcount, su.REL_AND, "u.cod_zona",
                su.OP_EQ_STR, (String)par.get("zona"));
             selcount = su.addWhere(selcount, su.REL_AND, "u.cod_distretto",
                su.OP_EQ_STR, (String)par.get("distretto"));
             selcount = su.addWhere(selcount, su.REL_AND, "u.codice",
                su.OP_EQ_STR, (String)par.get("pca"));
			// 15/04/10 ---
          System.out.println("Select distinct:"+selcount);
          ISASCursor dbconta=dbc.startCursor(selcount);
          int i_conta = dbconta.getDimension();
          String conta = (new Integer(i_conta)).toString();
          System.out.println("Quante cartelle?: "+conta);
          int int_mese=(new Integer(meseletto)).intValue();
          h.put(Vmesi[int_mese-1]+annoletto,conta);
          dbconta.close();
        }
        done=true;
        dbcur.close();
        return h;
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
      }

      private void preparaLayout(mergeDocument doc, ISASConnection dbc,Hashtable par) {
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
        htxt.put("#data_inizio#", getStringDate(par, "data_inizio"));
	htxt.put("#data_fine#", getStringDate(par, "data_fine"));
        String tipo = (String)par.get("tipo");
        if (tipo.equals("C"))
          htxt.put("#tipo_cont#", "(SOLO CONTATTO APERTO)");
        else
          htxt.put("#tipo_cont#", "(ALMENO UN INTERVENTO)");
	doc.writeSostituisci("layout",htxt);
      }

      public String currentTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(" HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(cal.getTime());
      }

private Hashtable CaricaHashInterv(ISASConnection dbc,String oper, String data_ini,
    String data_fine,Hashtable h,String nometab,String nome_data_ini,
    String nome_data_fine,Hashtable par)
      throws SQLException
      {
        String strVariableClause = "";
       ServerUtility su =new ServerUtility();

        //gb 08/06/07: Assistente Sociale *******
        if (oper.equals("01")) //gb 08/06/07: Assistente Sociale
           {
           //gb 08/06/07: Non prende in considerazione gli accessi occasionali
           strVariableClause =	" i.n_progetto = " + nometab + ".n_progetto" +
                    " AND i.n_progetto IS NOT NULL" +
                    " AND i.n_progetto <> 0";
           }
        else //gb 08/06/07: Non Assistente Sociale
           {
           strVariableClause = "int_contatto = " + nometab + ".n_contatto";
           }
        //gb 08/06/07: fine *******

       boolean done=false;
       try{
                String[] Vmesi={"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno","Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre"};
                String myselect="SELECT DISTINCT "+
/*                        "substr(int_data_prest,1,4) anno,"+
                        "substr(int_data_prest,6,2) mese "+
*/
                        dbc.formatDbMonth("int_data_prest")+" mese ,"+
                        dbc.formatDbYear("int_data_prest")+" anno "+
                        "FROM interv i,"+nometab+","+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u WHERE "+
//gb 08/06/07			"int_contatto="+nometab+".n_contatto AND "+
                        strVariableClause +
//gb 08/06/07                        "int_cartella="+nometab+".n_cartella AND "+
                        " AND int_cartella="+nometab+".n_cartella AND "+ //gb 08/06/07
                        "int_data_prest<="+formatDate(dbc,data_fine)+" AND "+
                        "int_data_prest>="+formatDate(dbc,data_ini)+" AND "+
                        nome_data_ini+"<="+formatDate(dbc,data_fine)+" AND "+
                        nome_data_ini+">="+formatDate(dbc,data_ini)+
                        " AND int_tipo_oper='"+oper+"'";

               String ragg = (String)par.get("ragg");
               String pca = (String)par.get("pca");

                myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);
                // per coerenza con la scelta solo contatto/progetto aperto
                // anche nella scelta almeno un accesso non e' previsto che il terzo livello
                // sia il presidio
                if(ragg.equals("A"))
                {
			myselect += " AND int_cod_areadis=u.codice";
                        myselect = su.addWhere(myselect, su.REL_AND, "int_cod_areadis" ,
                            su.OP_EQ_STR,pca);
                }
                else if(ragg.equals("C"))
                {
			myselect += " AND int_cod_comune=u.codice";
                        myselect = su.addWhere(myselect, su.REL_AND, "int_cod_comune" ,
                            su.OP_EQ_STR,pca);
                }
                //M.Minerba 25/02/2013 per Pistoia
                else if(ragg.equals("P"))
                {
			myselect += " AND int_cod_presidio_sk=u.codice";
                        myselect = su.addWhere(myselect, su.REL_AND, "int_cod_presidio_sk" ,
                            su.OP_EQ_STR,pca);
                }
              //fine M.Minerba 25/02/2013 per Pistoia	
                myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
                        su.OP_EQ_STR, (String)par.get("zona"));
                myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
                        su.OP_EQ_STR, (String)par.get("distretto"));

        myselect +=" ORDER BY  anno,mese";
        System.out.println("Select CaricaHash"+myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
        while (dbcur.next()){
          ISASRecord dbr=dbcur.getRecord();
          String meseletto=(String)dbr.get("mese");
          String annoletto=(String)dbr.get("anno");


          String data_ini_mese="";
          String data_fine_mese="";
          String mese_aggiornato="";
          int mese_ok=Integer.parseInt(meseletto);
          String segno_fine="";
          //Questo controllo serve nel caso in cui mi venga scelta una data inizio periodo
          //che non coincide con la data inizio mese e una data fine periodo che non
          //coincide con la fine del mese
          if (anno_ini_arrivato.equals(annoletto))
          {
            if(mese_ini_arrivato.equals(meseletto))
              data_ini_mese=gg_ini_arrivato+"/"+mese_ini_arrivato+"/"+anno_ini_arrivato;
            else
              data_ini_mese="01"+"/"+meseletto+"/"+annoletto;
          }
          else
            data_ini_mese="01"+"/"+meseletto+"/"+annoletto;
          if (anno_fine_arrivato.equals(annoletto))
          {
            if(mese_fine_arrivato.equals(meseletto))
            {     segno_fine="=";
              data_fine_mese=gg_fine_arrivato+"/"+mese_fine_arrivato+"/"+anno_fine_arrivato;
            }
            else{
               if (!meseletto.equals("12"))
              {
                mese_ok++;
                if(mese_ok<10)
                {
                  mese_aggiornato="0"+mese_ok;
                  data_fine_mese="01"+"/"+mese_aggiornato+"/"+annoletto;
                }else
                  data_fine_mese="01"+"/"+mese_ok+"/"+annoletto;
              }else
                data_fine_mese="01"+"/01/"+annoletto;
            }
          }
          else{
            if (!meseletto.equals("12"))
            {
              mese_ok++;
              if(mese_ok<10)
              {
                  mese_aggiornato="0"+mese_ok;
                  data_fine_mese="01"+"/"+mese_aggiornato+"/"+annoletto;
              }else
                  data_fine_mese="01"+"/"+mese_ok+"/"+annoletto;
            }else
              data_fine_mese="01"+"/01/"+annoletto;
          }
          String selcount="SELECT DISTINCT i.int_cartella conta"+
                          " FROM interv i,"+nometab+
						  ", "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u" + // 15/04/10
						  " WHERE "+
//gb 08/06/07                          "i.int_contatto="+nometab+".n_contatto AND "+
                        strVariableClause +
//gb 08/06/07                          "i.int_cartella="+nometab+".n_cartella AND "+
                          " AND i.int_cartella="+nometab+".n_cartella AND "+ //gb 08/06/07
                          "i.int_data_prest<"+segno_fine+formatDate(dbc,data_fine_mese)+" AND"+
                          " i.int_data_prest>="+formatDate(dbc,data_ini_mese)+" AND "+
                          nome_data_ini+"<="+formatDate(dbc,data_fine_mese)+" AND "+
                          nome_data_ini+">="+formatDate(dbc,data_ini_mese)+
                          " AND i.int_tipo_oper='"+oper+"'";

			// 15/04/10 ---
			selcount = su.addWhere(selcount, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);
            // per coerenza con la scelta solo contatto/progetto aperto
            // anche nella scelta almeno un accesso non e' previsto che il terzo livello
            // sia il presidio
            if(ragg.equals("A")) {
				selcount += " AND int_cod_areadis=u.codice";
                selcount = su.addWhere(selcount, su.REL_AND, "int_cod_areadis" ,
                            su.OP_EQ_STR,pca);
            } else if(ragg.equals("C")) {
				selcount += " AND int_cod_comune=u.codice";
                selcount = su.addWhere(selcount, su.REL_AND, "int_cod_comune" ,
                            su.OP_EQ_STR,pca);
            }//M.Minerba 25/02/2013 per Pistoia
            else if(ragg.equals("P")) {
				selcount += " AND int_cod_presidio_sk=u.codice";
                selcount = su.addWhere(selcount, su.REL_AND, "int_cod_presidio_sk" ,
                            su.OP_EQ_STR,pca);
            }
          //fine M.Minerba 25/02/2013 per Pistoia

            selcount = su.addWhere(selcount, su.REL_AND, "u.cod_zona",
                        su.OP_EQ_STR, (String)par.get("zona"));
            selcount = su.addWhere(selcount, su.REL_AND, "u.cod_distretto",
                        su.OP_EQ_STR, (String)par.get("distretto"));
			// 15/04/10 ---
						  
//          System.out.println("Select Distnct su interv:"+selcount);

          ISASCursor dbconta=dbc.startCursor(selcount);
          int i_conta = dbconta.getDimension();
          String conta = (new Integer(i_conta)).toString();
          int int_mese=(new Integer(meseletto)).intValue();
          h.put(Vmesi[int_mese-1]+annoletto,conta);
          dbconta.close();
        }
        dbcur.close();
        done=true;
        return h;
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
      }


}	// End of FoEleSoc class

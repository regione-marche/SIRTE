package it.caribel.app.sinssnt.bean.modificati;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 20/11/2002 - EJB di connessione alla procedura SINS Tabella FoPrestDom
//
// Jessica Caccavale
// ILARIA MANCINI 22/09/2002
//
// 01/03/2007 ho aggiunto le hash anche per oncologo e medico specialista
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

public class FoPrestDomEJB extends SINSSNTConnectionEJB {
   Hashtable hTotaleADI = new Hashtable();
   Hashtable hTotaleNONADI = new Hashtable();
   String[] Vmesi={"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno","Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre"};
int tot=0;
int totriga=0;
int totrigaNONADI=0;
String dom_res;
String dr;

public static String getjdbcDate()
{
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
                new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}


public FoPrestDomEJB() {}

public byte[] query_elencoPrest(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException {

        boolean done=false;
	ISASConnection dbc=null;
	String mysel="";
	String data_ini="";
	String data_fine="";
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
                String tipo=(String)par.get("tipo");
                Hashtable hfis = new Hashtable();
                Hashtable hmed = new Hashtable();
                Hashtable hinf = new Hashtable();
                Hashtable hass = new Hashtable();
                /*01/03/07*/
                Hashtable honc = new Hashtable();
                Hashtable hspe = new Hashtable();

                hTotaleADI.put("01",hass);
                hTotaleADI.put("02",hinf);
                hTotaleADI.put("03",hmed);
                hTotaleADI.put("04",hfis);
                hTotaleADI.put("52",honc);
                hTotaleADI.put("98",hspe);

                hTotaleADI=CaricaHash(dbc,data_ini,data_fine,hTotaleADI, par);
                preparaLayout(doc,dbc,data_ini,data_fine,tipo);
                doc.write("testa");
                hfis = new Hashtable();
                hmed = new Hashtable();
                hinf = new Hashtable();
                hass = new Hashtable();
                honc = new Hashtable();
                hspe = new Hashtable();

                hTotaleNONADI.put("01",hass);
                hTotaleNONADI.put("02",hinf);
                hTotaleNONADI.put("03",hmed);
                hTotaleNONADI.put("04",hfis);
                hTotaleNONADI.put("52",honc);
                hTotaleNONADI.put("98",hspe);
                if (tipo.equals("NONADI"))
                    hTotaleNONADI=CaricaHashNONADI(dbc,data_ini,data_fine,hTotaleNONADI,"D", par);
                InizializzaHash(data_ini,data_fine,doc,tipo);
                doc.write("finetab");
                doc.write("finale");
                doc.close();
                //riprendo il bytearray
                rit=(byte[])doc.get();
                dbc.close();
                super.close(dbc);
                done=true;
                return rit;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_elencoPrest()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

public byte[] query_prest_nodom(String utente, String passwd, Hashtable par,mergeDocument doc) throws SQLException {

        boolean done=false;
	ISASConnection dbc=null;
	String mysel="";
	String data_ini="";
	String data_fine="";
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
                String tipo=(String)par.get("tipo");

                preparaLayout(doc,dbc,data_ini,data_fine,tipo);
                doc.write("testa");
                Hashtable hfis = new Hashtable();
                Hashtable hmed = new Hashtable();
                Hashtable hinf = new Hashtable();
                Hashtable hass = new Hashtable();
                /*01/03/2007*/
                Hashtable honc = new Hashtable();
                Hashtable hspe = new Hashtable();

                hTotaleNONADI.put("01",hass);
                hTotaleNONADI.put("02",hinf);
                hTotaleNONADI.put("03",hmed);
                hTotaleNONADI.put("04",hfis);
                /*01/03/2007*/
                hTotaleNONADI.put("52",honc);
                hTotaleNONADI.put("98",hspe);
                hTotaleNONADI=CaricaHashNONADI(dbc,data_ini,data_fine,hTotaleNONADI,"A", par);
                InizializzaHashAMB(data_ini,data_fine,doc);
                doc.write("finetab");
                doc.write("finale");
                doc.close();
                //riprendo il bytearray
                rit=(byte[])doc.get();
                dbc.close();
                super.close(dbc);
                done=true;
                return rit;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_elencoPrest()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

      private void InizializzaHash(String data_ini,String data_fine,mergeDocument doc,String tipo)
      {
        Hashtable has = new Hashtable();
     //   String[] Vmesi={"Gennaio","Febbraio","Marzo","Aprile","Maggio","Giugno","Luglio","Agosto","Settembre","Ottobre","Novembre","Dicembre"};
        String mi=data_ini.substring(5,7);
        String ai=data_ini.substring(0,4);
        String mf=data_fine.substring(5,7);
        String af=data_fine.substring(0,4);
        int mese_iniz=(new Integer(mi)).intValue();
        int anno_iniz=(new Integer(ai)).intValue();
        int mese_fine=(new Integer(mf)).intValue();
        int anno_fine=(new Integer(af)).intValue();
        int mese_ausi_iniz=mese_iniz;
        int mese_ausi_fine=0;

        Hashtable hass=(Hashtable) hTotaleADI.get("01");
        Hashtable hinf=(Hashtable) hTotaleADI.get("02");
        Hashtable hmed=(Hashtable) hTotaleADI.get("03");
        Hashtable hfis=(Hashtable) hTotaleADI.get("04");
        /*01/03/2007*/
        Hashtable honc=(Hashtable) hTotaleADI.get("52");
        Hashtable hspe=(Hashtable) hTotaleADI.get("98");
        Hashtable hassNONADI=(Hashtable) hTotaleNONADI.get("01");
        Hashtable hinfNONADI=(Hashtable) hTotaleNONADI.get("02");
        Hashtable hmedNONADI=(Hashtable) hTotaleNONADI.get("03");
        Hashtable hfisNONADI=(Hashtable) hTotaleNONADI.get("04");
        Hashtable honcNONADI=(Hashtable) hTotaleNONADI.get("52");
        Hashtable hspeNONADI=(Hashtable) hTotaleNONADI.get("98");

        int totaleADIas=0;
        int totaleNONADIas=0;
        int totaleADImed=0;
        int totaleNONADImed=0;
        int totaleADIfis=0;
        int totaleNONADIfis=0;
        int totaleADIinf=0;
        int totaleNONADIinf=0;
        /*01/03/2007*/
        int totaleADIonc=0;
        int totaleNONADIonc=0;
        int totaleADIspe=0;
        int totaleNONADIspe=0;

        for (int i=anno_iniz;i<=anno_fine;i++){
          if(i<anno_fine)
            mese_ausi_fine=12;
          else
            mese_ausi_fine=mese_fine;
          for (int j=mese_ausi_iniz;j<=mese_ausi_fine;j++){
            has.put("#mese#",Vmesi[j-1]+ '-' +i);
            has= AnalizzaHash (tipo, has,hass,hassNONADI,j,i,"#mese_as#");
            totaleADIas=totaleADIas+Integer.parseInt(""+has.get("TOTALEADI"));
            if (tipo.equals("NONADI"))
                totaleNONADIas=totaleNONADIas+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHash (tipo, has,hmed,hmedNONADI,j,i,"#mese_med#");
            totaleADImed=totaleADImed+Integer.parseInt(""+has.get("TOTALEADI"));
            if (tipo.equals("NONADI"))
                  totaleNONADImed=totaleNONADImed+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHash (tipo, has,hfis,hfisNONADI,j,i,"#mese_fis#");
            totaleADIfis=totaleADIfis+Integer.parseInt(""+has.get("TOTALEADI"));
            if (tipo.equals("NONADI"))
                  totaleNONADIfis=totaleNONADIfis+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHash (tipo, has,hinf,hinfNONADI,j,i,"#mese_ip#");
            totaleADIinf=totaleADIinf+Integer.parseInt(""+has.get("TOTALEADI"));
            if (tipo.equals("NONADI"))
                  totaleNONADIinf=totaleNONADIinf+Integer.parseInt(""+has.get("TOTALENONADI"));
            /*01/03/2007*/
            has= AnalizzaHash (tipo, has,honc,honcNONADI,j,i,"#mese_onc#");
            totaleADIonc=totaleADIonc+Integer.parseInt(""+has.get("TOTALEADI"));
            if (tipo.equals("NONADI"))
                  totaleNONADIonc=totaleNONADIonc+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHash (tipo, has,hspe,hspeNONADI,j,i,"#mese_spe#");
            totaleADIspe=totaleADIspe+Integer.parseInt(""+has.get("TOTALEADI"));
            if (tipo.equals("NONADI"))
                  totaleNONADIspe=totaleNONADIspe+Integer.parseInt(""+has.get("TOTALENONADI"));
            //DEVO ANDARE A METTERE DENTRO LA TABELLA IL TOTALE DI RIGA
            has.put("#totali#",(new Integer(totriga)).toString());
            if (tipo.equals("NONADI"))
                  has.put("#totali#",(new Integer(totrigaNONADI)).toString());
            doc.writeSostituisci("tabella",has);
            totriga=0;
            totrigaNONADI=0;
          }
          mese_ausi_iniz=1;
        }
        has.put("#mese#","Totali");
        has.put("#mese_fis#",""+totaleADIfis);
        has.put("#mese_med#",""+totaleADImed);
        has.put("#mese_ip#",""+totaleADIinf);
        has.put("#mese_as#",""+totaleADIas);
        has.put("#mese_onc#",""+totaleADIonc);
        has.put("#mese_spe#",""+totaleADIspe);

        tot=totaleADIfis+totaleADImed+totaleADIinf+totaleADIas+
            totaleADIonc+totaleADIspe;
        if (tipo.equals("NONADI")){
              has.put("#mese_fis#",""+totaleNONADIfis);
              has.put("#mese_med#",""+totaleNONADImed);
              has.put("#mese_ip#",""+totaleNONADIinf);
              has.put("#mese_as#",""+totaleNONADIas);
              has.put("#mese_onc#",""+totaleNONADIonc);
              has.put("#mese_spe#",""+totaleNONADIspe);

              tot=totaleNONADIfis+totaleNONADImed+totaleNONADIinf+totaleNONADIas+
                  totaleNONADIonc+totaleNONADIspe;
        }
        has.put("#totali#",""+tot);
        doc.writeSostituisci("tabella",has);
        //return has;
      }
      private void InizializzaHashAMB(String data_ini,String data_fine,mergeDocument doc)
      {
        Hashtable has = new Hashtable();
        String mi=data_ini.substring(5,7);
        String ai=data_ini.substring(0,4);
        String mf=data_fine.substring(5,7);
        String af=data_fine.substring(0,4);
        int mese_iniz=(new Integer(mi)).intValue();
        int anno_iniz=(new Integer(ai)).intValue();
        int mese_fine=(new Integer(mf)).intValue();
        int anno_fine=(new Integer(af)).intValue();
        int mese_ausi_iniz=mese_iniz;
        int mese_ausi_fine=0;

        Hashtable hassNONADI=(Hashtable) hTotaleNONADI.get("01");
        Hashtable hinfNONADI=(Hashtable) hTotaleNONADI.get("02");
        Hashtable hmedNONADI=(Hashtable) hTotaleNONADI.get("03");
        Hashtable hfisNONADI=(Hashtable) hTotaleNONADI.get("04");
        /*01/03/2007*/
        Hashtable honcNONADI=(Hashtable) hTotaleNONADI.get("52");
        Hashtable hspeNONADI=(Hashtable) hTotaleNONADI.get("98");

        int totaleNONADIas=0;
        int totaleNONADImed=0;
        int totaleNONADIfis=0;
        int totaleNONADIinf=0;
        /*01/03/2007*/
        int totaleNONADIonc=0;
        int totaleNONADIspe=0;

        for (int i=anno_iniz;i<=anno_fine;i++){
          if(i<anno_fine)
            mese_ausi_fine=12;
          else
            mese_ausi_fine=mese_fine;
          for (int j=mese_ausi_iniz;j<=mese_ausi_fine;j++){
            has.put("#mese#",Vmesi[j-1]+ '-' +i);
            has= AnalizzaHashAMB ( has,hassNONADI,j,i,"#mese_as#");
            totaleNONADIas=totaleNONADIas+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHashAMB ( has,hmedNONADI,j,i,"#mese_med#");
            totaleNONADImed=totaleNONADImed+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHashAMB ( has,hfisNONADI,j,i,"#mese_fis#");
            totaleNONADIfis=totaleNONADIfis+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHashAMB ( has,hinfNONADI,j,i,"#mese_ip#");
            totaleNONADIinf=totaleNONADIinf+Integer.parseInt(""+has.get("TOTALENONADI"));
            /*01/03/2007*/
            has= AnalizzaHashAMB ( has,honcNONADI,j,i,"#mese_onc#");
            totaleNONADIonc=totaleNONADIonc+Integer.parseInt(""+has.get("TOTALENONADI"));
            has= AnalizzaHashAMB ( has,hspeNONADI,j,i,"#mese_spe#");
            totaleNONADIspe=totaleNONADIspe+Integer.parseInt(""+has.get("TOTALENONADI"));

            //DEVO ANDARE A METTERE DENTRO LA TABELLA IL TOTALE DI RIGA
            has.put("#totali#",(new Integer(totriga)).toString());
            doc.writeSostituisci("tabella",has);
            totriga=0;
          }
          mese_ausi_iniz=1;
        }
        has.put("#mese#","Totali");
        has.put("#mese_fis#",""+totaleNONADIfis);
        has.put("#mese_med#",""+totaleNONADImed);
        has.put("#mese_ip#",""+totaleNONADIinf);
        has.put("#mese_as#",""+totaleNONADIas);
        /*01/03/2007*/
        has.put("#mese_onc#",""+totaleNONADIonc);
        has.put("#mese_spe#",""+totaleNONADIspe);

        tot=totaleNONADIfis+totaleNONADImed+totaleNONADIinf+totaleNONADIas+
            totaleNONADIonc+totaleNONADIspe;
        has.put("#totali#",""+tot);
        doc.writeSostituisci("tabella",has);
        //return has;
      }

private Hashtable  AnalizzaHash (String tipo,Hashtable has,Hashtable hADI,
        Hashtable hNONADI,int j,int i,String chiave)
{          int totTipoADI=0;
           int totTipoNONADI=0;
           if(hADI.get(Vmesi[j-1]+i)!=null && !((String)hADI.get(Vmesi[j-1]+i)).equals(""))
            {
              has.put(chiave,hADI.get(Vmesi[j-1]+i));
              int f1=(new Integer(""+has.get(chiave))).intValue();
              totriga=totriga+f1;
              has.put("TOTALEADI",""+(totTipoADI+f1));
            }else
            {
              has.put(chiave,"0");
              has.put("TOTALEADI",""+(totTipoADI));
            }
            if (tipo.equals("NONADI"))
            {
              int adi= Integer.parseInt(""+has.get(chiave));
              if(hNONADI.get(Vmesi[j-1]+i)!=null && !((String)hNONADI.get(Vmesi[j-1]+i)).equals(""))
              {
                int totali=Integer.parseInt(""+hNONADI.get(Vmesi[j-1]+i));
                int nonadi=totali-adi;
                has.put(chiave,""+nonadi);
                totrigaNONADI=totrigaNONADI+nonadi;
                has.put("TOTALENONADI",""+(totTipoNONADI+nonadi));
              }else
              {
                has.put(chiave,"0");
                has.put("TOTALENONADI",""+(totTipoNONADI));

              }
            }
   return has;
}


private Hashtable  AnalizzaHashAMB (Hashtable has,Hashtable hADI,
        int j,int i,String chiave)
{
          int totTipoADI=0;
          if(hADI.get(Vmesi[j-1]+i)!=null && !((String)hADI.get(Vmesi[j-1]+i)).equals(""))
          {
             has.put(chiave,hADI.get(Vmesi[j-1]+i));
             int f1=(new Integer(""+has.get(chiave))).intValue();
             totriga=totriga+f1;
             has.put("TOTALENONADI",""+(totTipoADI+f1));
          }else
           {
             has.put(chiave,"0");
             has.put("TOTALENONADI",""+(totTipoADI));
          }
   return has;
}

      private Hashtable CaricaHash(ISASConnection dbc,String data_ini,String data_fine,Hashtable h, Hashtable par)
      throws SQLException{
	ServerUtility su = new ServerUtility();
       boolean done=false;
       ISASCursor dbcur=null;
       ISASCursor dbconta=null;
       try{

          String myselect="SELECT DISTINCT s.n_cartella,sku_data,sku_data_fine "+
//gb 23/10/07                  " FROM skuvt "+
                  " FROM skuvt s, anagra_c a, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u"+ //gb 23/10/07
                  " WHERE sku_data<="+formatDate(dbc,data_fine)+
                  " AND( sku_data_fine>="+formatDate(dbc,data_ini)+" OR sku_data_fine IS NULL)"+
                  " AND sku_adi='S'" +
//gb 23/10/07 *******
		" AND s.n_cartella = a.n_cartella"+
		" AND a.data_variazione IN"+
		" (SELECT MAX (data_variazione)"+
		" FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";
//gb 23/10/07: fine *******
//gb 23/10/07 *******
            String sel="";
            String ragg = (String)par.get("ragg");
            sel = su.addWhere(sel, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);
//	    if (ragg!=null && ragg.equals("C")){
//		sel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                            " AND u.codice=a.dom_citta)"+
//                            " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                            " AND u.codice=a.citta))";
//	    }else if (ragg!=null && ragg.equals("A")){
//		sel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                            " AND u.codice=a.dom_areadis)"+
//                            " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                            " AND u.codice=a.areadis))";
//             }

            //Aggiunto Controllo Domicilio/Residenza (BYSP)
            if(this.dom_res == null)
            {
                    if (ragg.equals("C"))
                      sel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                            " AND u.codice=a.dom_citta)"+
            		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                            " AND u.codice=a.citta))";
                    else if (ragg.equals("A"))
                      sel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                            " AND u.codice=a.dom_areadis)"+
            		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                            " AND u.codice=a.areadis))";
                    //M.Minerba 01/03/2013 per Pistoia
                    else if (ragg.equals("P"))
                    	sel += " AND EXISTS (SELECT * FROM skinf c "+
                      			 " WHERE c.n_cartella = s.n_cartella "+
                    			" AND c.ski_data_apertura <= s.sku_data "+
                    			" AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                    			"AND c.ski_cod_presidio = u.codice)";    
                     //fine M.Minerba 28/02/2013 per Pistoia   
            }
            else if (this.dom_res.equals("D"))
                                      {
                                       if (ragg.equals("C"))
                      sel += " AND u.codice=a.dom_citta";
                                        else if (ragg.equals("A"))
                      sel += " AND u.codice=a.dom_areadis";
                                     //M.Minerba 28/02/2013 per Pistoia
                                        else if (ragg.equals("P"))
                    sel += " AND EXISTS (SELECT * FROM skinf c "+
                           " WHERE c.n_cartella = s.n_cartella "+
                           " AND c.ski_data_apertura <= s.sku_data "+
                           " AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                           "AND c.ski_cod_presidio = u.codice)";    
                                      //fine M.Minerba 28/02/2013 per Pistoia  
                                      }

            else if (this.dom_res.equals("R"))
                            {
                            if (ragg.equals("C"))
                      sel += " AND u.codice=a.citta";
                    else if (ragg.equals("A"))
                      sel += " AND u.codice=a.areadis";
                          //M.Minerba 28/02/2013 per Pistoia
                    else if (ragg.equals("P"))
                        sel += " AND EXISTS (SELECT * FROM skinf c "+
                               " WHERE c.n_cartella = s.n_cartella "+
                               " AND c.ski_data_apertura <= s.sku_data "+
                               " AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                               "AND c.ski_cod_presidio = u.codice)";    
                                 //fine M.Minerba 28/02/2013 per Pistoia  
                            }


	   sel = su.addWhere(sel, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String)par.get("zona"));
	   sel = su.addWhere(sel, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String)par.get("distretto"));
	   sel = su.addWhere(sel, su.REL_AND, "u.codice", su.OP_EQ_STR, (String)par.get("pca"));

	   myselect += " AND " + sel;
//gb 23/10/07: fine *******

        System.out.println("Select CaricaHash:PrestDom"+myselect);
        dbcur=dbc.startCursor(myselect);
        while (dbcur.next()){
          ISASRecord dbr=dbcur.getRecord();
          String selcount=" SELECT pre_numero,int_data_prest,int_tipo_oper " +
                        " FROM intpre,interv " +
                        " WHERE pre_contatore=int_contatore "+
                        " AND pre_anno=int_anno " +
                        " AND int_ambdom='D' " +
                        " AND int_data_prest>="+formatDate(dbc,""+dbr.get("sku_data"))+
                        " AND int_cartella="+dbr.get("n_cartella");
                        if (dbr.get("sku_data_fine")!=null)
                             selcount=selcount+ " AND int_data_prest<="+formatDate(dbc,""+dbr.get("sku_data_fine"));
                       System.out.println("Select INTERV:"+selcount);
                       dbconta=dbc.startCursor(selcount);
                       while (dbconta.next()){                    	   
                          ISASRecord dbrconta=dbconta.getRecord();
                          if (dbrconta.get("int_tipo_oper")!=null && !((String)dbrconta.get("int_tipo_oper")).equals(""))
                          { String tipo=(String)dbrconta.get("int_tipo_oper");
                         
                            Hashtable htipo =new Hashtable();                            
                            //M.Minerba 01/03/2013
                            if(h.get(tipo)!=null){                            	
                            	 htipo = (Hashtable)h.get(tipo);
                            }
                          //fine M.Minerba 01/03/2013
                            //htipo = (Hashtable)h.get(tipo) ;                            
                            int mese=0;
                            int anno=0;
                            if (dbrconta.get("int_data_prest")!=null){
                            	
                                  String dataprest=((java.sql.Date )dbrconta.get("int_data_prest")).toString();                                  
                                  if (dataprest.length()==10){
                                	 
                                       mese=Integer.parseInt(dataprest.substring(5,7));
                                       anno=Integer.parseInt(dataprest.substring(0,4));
                                       int tot=0;
                                       int pre=0;                                       
                                       if (htipo.get(Vmesi[mese-1]+anno)!=null){                               	   
                                    	  
                                              tot=Integer.parseInt(""+htipo.get(Vmesi[mese-1]+anno));
                                       }
                                      
                                       if (dbrconta.get("pre_numero")!=null)
                                          pre=((Integer)dbrconta.get("pre_numero")).intValue();
                                       
                                       tot=tot+pre;
                                       htipo.put(Vmesi[mese-1]+anno,""+tot);
                                      
                                  }
                                  
                            }
                           
                            h.put(tipo,htipo);
                            
                          }
                          //fine if intdataprest!=null
                       } //fine while
                       dbconta.close();
        }
        dbcur.close();
        done=true;
        return h;
       }catch(Exception e){
    	   e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una CaricaHash()  ");
       }finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
      }

      private void preparaLayout(mergeDocument doc, ISASConnection dbc,String data_ini,String data_fine,
            String tipo) {
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
        data_ini=data_ini.substring(8,10)+"/"+data_ini.substring(5,7)+"/"+
                 data_ini.substring(0,4);
        data_fine=data_fine.substring(8,10)+"/"+data_fine.substring(5,7)+"/"+
                 data_fine.substring(0,4);
        htxt.put("#data_inizio#",data_ini);
        htxt.put("#data_fine#",data_fine);
        htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        if (tipo.equals("NONADI"))
              htxt.put("#titolo#"," DOMICILIARI NON ADI" );
        else if (tipo.equals("AMB"))
              htxt.put("#titolo#"," AMBULATORIALI " );
        else
              htxt.put("#titolo#","DOMICILIARI ADI" );

        String ora=currentTime();
        htxt.put("#ora#",ora);
	doc.writeSostituisci("layout",htxt);
      }

      public String currentTime() {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat(" HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(cal.getTime());
      }

      private Hashtable CaricaHashNONADI(ISASConnection dbc, String data_ini,String data_fine,Hashtable h,String tipoamb, Hashtable par)
      throws SQLException{
	ServerUtility su = new ServerUtility();
       boolean done=false;
       String[] VOper={"01","02","03","04","52","98"};

       try{
        int anno_ini_arrivato=Integer.parseInt(data_ini.substring(0,4));
        int mese_ini_arrivato=Integer.parseInt(data_ini.substring(5,7));
        int gg_ini_arrivato=Integer.parseInt(data_ini.substring(8,10));
        int anno_fine_arrivato=Integer.parseInt(data_fine.substring(0,4));
        int mese_fine_arrivato=Integer.parseInt(data_fine.substring(5,7));
        int gg_fine_arrivato=Integer.parseInt(data_fine.substring(8,10));
        String data_ini_mese="";
        String data_fine_mese="";
        String mese_aggiornato="";
        String segno_fine="";
        String zero_gg="";
        String zero_mm="";
        int anno=0;
        for(anno=anno_ini_arrivato;anno<=anno_fine_arrivato;anno++){
            int mese_inizio=0;
            int mese_fine=0;
            if (anno_ini_arrivato==anno){
              mese_inizio=mese_ini_arrivato;
            }else{
              mese_inizio=1;
            }
            if (anno_fine_arrivato==anno){
              mese_fine=mese_fine_arrivato;
            }else{
              mese_fine=12;
            }
            for(int mese=mese_inizio;mese<=mese_fine;mese++){
              //System.out.println("Anno dopo il for di mesi: "+anno);
              zero_gg="";
              zero_mm="";
              int mese_ok=mese;
                if(mese_ini_arrivato==mese && anno==anno_ini_arrivato){
                  if (gg_ini_arrivato<10)
                    zero_gg="0";
                  if (mese_ini_arrivato<10)
                    zero_mm="0";
                  data_ini_mese=zero_gg+gg_ini_arrivato+"/"+zero_mm+mese_ini_arrivato+"/"+anno_ini_arrivato;
                  zero_gg="";
                  zero_mm="";
                }else{
                   //System.out.println("else mese_ini_arrivato=mese");
                   if (mese<10)
                    zero_mm="0";
                  data_ini_mese="01"+"/"+zero_mm+mese+"/"+anno;
                }
                if(mese_fine_arrivato==mese && anno==anno_fine_arrivato){
                  segno_fine="=";
                  if (gg_fine_arrivato<10)
                    zero_gg="0";
                   if (mese_fine_arrivato<10)
                    zero_mm="0";
                  data_fine_mese=zero_gg+gg_fine_arrivato+"/"+zero_mm+mese_fine_arrivato+"/"+anno_fine_arrivato;
                }
                else{
                  if (mese!=12){
                    mese_ok++;
                    if(mese_ok<10)
                    {
                      mese_aggiornato="0"+mese_ok;
                      data_fine_mese="01"+"/"+mese_aggiornato+"/"+anno;
                    }else
                      data_fine_mese="01"+"/"+mese_ok+"/"+anno;
                  }else{
                    int anno_incr=anno+1;
                    data_fine_mese="01"+"/01/"+anno_incr;
                  }
                }
//gb 12/06/07: Si era pensato di mettere  la clausola 'n_progetto is NOT NULL'
//		nella query quando int_tipo_opr = '01', ma poi abbiamo scelto
//		di lasciare tutto com'� perch� cos� si prendono anche i record
//		inseriti dal vecchio SINS per gli assistenti sociali prima del SINSSNT.
//		Cio� quei record che hanno int_tipo_opr = '01', int_contatto >= 0 e
//		n_progetto  uguale a NULL.

//gb 23/10/07 *******
        String raggruppamento = (String)par.get("ragg");
	String s = "";
	s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, raggruppamento);
                //RAGGRUPPAMENTO
//        if (raggruppamento!=null && raggruppamento.equals("C"))
//           s += " AND r.int_cod_comune=u.codice ";
//        else if (raggruppamento!=null && raggruppamento.equals("A"))
//           s += " AND r.int_cod_areadis=u.codice ";
//		// 12/01/10 m ---
//		else if (raggruppamento!=null && raggruppamento.equals("P"))
//			s += " AND r.int_codpres=u.codice";

	if (this.dom_res==null)
	{
		   if (raggruppamento!=null && raggruppamento.equals("C"))
	           s += " AND r.int_cod_comune=u.codice ";
	        else if (raggruppamento!=null && raggruppamento.equals("A"))
	           s += " AND r.int_cod_areadis=u.codice ";
			// 12/01/10 m ---
			else if (raggruppamento!=null && raggruppamento.equals("P"))
				s += " AND r.int_cod_presidio_sk=u.codice";
	}else if (this.dom_res.equals("D"))
	{
		   if (raggruppamento!=null && raggruppamento.equals("C"))
	           s += " AND r.int_cod_comune=u.codice ";
	        else if (raggruppamento!=null && raggruppamento.equals("A"))
	           s += " AND r.int_cod_areadis=u.codice ";
	        else if (raggruppamento!=null && raggruppamento.equals("P"))
				s += " AND r.int_cod_presidio_sk=u.codice";
			// 12/01/10 m ---
	}else if (this.dom_res.equals("R"))
	{
		if (raggruppamento!=null && raggruppamento.equals("C"))
        s += " AND r.int_cod_comune=u.codice ";
    else if (raggruppamento!=null && raggruppamento.equals("A"))
       s += " AND r.int_cod_areadis=u.codice ";
    else if (raggruppamento!=null && raggruppamento.equals("P"))
		s += " AND r.int_cod_presidio_sk=u.codice";
	// 12/01/10 m ---

	}

		// 12/01/10 m ---

	s = su.addWhere(s, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice", su.OP_EQ_STR, (String)par.get("pca"));
//gb 23/10/07: fine *******

              for (int i=0;i<=5;i++){
                    String selcount="SELECT nvl(SUM(pre_numero),0) conta" +
//gb 23/10/07				" FROM intpre, interv " +
				" FROM intpre, interv r, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u" + //gb 23/10/07
				" WHERE r.int_anno=intpre.pre_anno"+
                                " AND r.int_contatore=intpre.pre_contatore"+
                                " AND int_data_prest<"+segno_fine+formatDate(dbc,data_fine_mese)+
                                " AND int_data_prest>="+formatDate(dbc,data_ini_mese)+
                                " AND int_ambdom='"+tipoamb+"' "+
                                " AND int_tipo_oper='"+VOper[i]+"'";
		    selcount += " AND " + s; //gb 23/10/07
                    System.out.println("Select COUNT:"+selcount);
                    ISASRecord dbrconta=dbc.readRecord(selcount);
                    if (dbrconta!=null){
                         int meseletto=Integer.parseInt(data_ini_mese.substring(3,5));
                         int annoletto=Integer.parseInt(data_ini_mese.substring(6,10));
                         String tipo=VOper[i];
                         Hashtable htipo =new Hashtable();
                         htipo = (Hashtable)h.get(tipo) ;
                         String conta="";
                         if(dbrconta.get("conta")!=null){
                           //JESSY07/01/04
                           //conta=((Double)dbrconta.get("conta")).toString();
                           conta=""+dbrconta.get("conta");
                           if (conta.indexOf(".")!=-1){
                               StringTokenizer tok = new StringTokenizer(conta,".");
                               conta = tok.nextToken();
                           }
                         }
                         int int_mese=(new Integer(meseletto)).intValue();
                         htipo.put(Vmesi[int_mese-1]+annoletto,conta);
                         h.put(tipo,htipo);
                    }
                  }
              }
          }
        done=true;
        return h;
       }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una CaricaHashNONADI()  ");
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

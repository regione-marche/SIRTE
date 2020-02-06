package it.caribel.app.sinssnt.bean;
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 10/09/2007 - EJB di connessione alla procedura SINS Tabella FoContaGiorni
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

public class FoContaGiorniEJB extends SINSSNTConnectionEJB {

//hash per decodifica operatore
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

String ragg="";
String d1_R="";
String d2_R="";


String dom_res;
String dr;

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
        String data=d1_R.substring(8, 10)+"/"+d1_R.substring(5, 7)+"/"+d1_R.substring(0, 4);
        htxt.put("#d1#",data);
        data=d2_R.substring(8, 10)+"/"+d2_R.substring(5, 7)+"/"+d2_R.substring(0, 4);
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


public FoContaGiorniEJB() {}

public byte[] query_report(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
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
			d1_R=(String)par.get("d1");
		if (par.get("d2") != null && !((String)par.get("d2")).equals(""))
			d2_R=(String)par.get("d2");

		String myselect = mkSelectAnalitica(dbc, par);
                System.out.println("SELECT: "+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
                if(((String)par.get("TYPE")).equals("PDF")){
                    preparaLayout(doc, dbc);
                    Hashtable hGenerale=new Hashtable();
                    int k = 0;
                    while (dbcur.next()){
                       ISASRecord dbr= dbcur.getRecord();
                       hGenerale=AnalizzaZona(dbc,dbr,hGenerale);
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
                doc.write("finale");
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
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

private void FaiExcel(ISASConnection dbc, ISASRecord dbr, mergeDocument doc)throws SQLException{
    ISASRecord dbConta=null;
    try{
          Hashtable tab=new Hashtable();
          String data_ap = (String)util.getObjectField(dbr,"skm_data_apertura", 'T');
          String data_ch = (String)util.getObjectField(dbr,"skm_data_chiusura", 'T');
          tab.put("#cartella#", (String)util.getObjectField(dbr,"n_cartella", 'I'));
          tab.put("#assistito#",util.getDecode(dbc,"cartella","n_cartella",
                                (String)util.getObjectField(dbr,"n_cartella",'I'),
                                "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","assistito"));
          tab.put("#contatto#", (String)util.getObjectField(dbr,"n_contatto", 'I'));
          tab.put("#data_apertura#", data_ap);
          tab.put("#data_chiusura#", data_ch);
          java.sql.Date dateAp=java.sql.Date.valueOf(formDate(data_ap,"aaaa-mm-gg",false));
          //java.sql.Date dateCh=java.sql.Date.valueOf(formDate(data_ch,"aaaa-mm-gg",false));
          java.sql.Date date1_R=java.sql.Date.valueOf(formDate(d1_R,"aaaa-mm-gg",false));
          java.sql.Date date2_R=java.sql.Date.valueOf(formDate(d2_R,"aaaa-mm-gg",false));
          //System.out.println("dataAP:"+dateAp+" dataCh:"+dateCh);
          if(dateAp.before(date1_R))
            data_ap = d1_R.substring(8,10)+"/"+d1_R.substring(5,7)+"/"+d1_R.substring(0,4);
          if(data_ch.equals(""))
            data_ch=d2_R.substring(8,10)+"/"+d2_R.substring(5,7)+"/"+d2_R.substring(0,4);
          else{
            java.sql.Date dateCh=java.sql.Date.valueOf(formDate(data_ch,"aaaa-mm-gg",false));
            if(dateCh.after(date2_R))
                data_ch = d2_R.substring(8,10)+"/"+d2_R.substring(5,7)+"/"+d2_R.substring(0,4);
          }
          int giorni = DiffDate(data_ap,data_ch);
          tab.put("#giorni#", ""+giorni);
          tab.put("#zona#", (String)dbr.get("descrizione_zona"));
          tab.put("#distretto#", (String)dbr.get("des_distr"));
          tab.put("#comune#", (String)dbr.get("descrizione"));
          doc.writeSostituisci("tabella", tab);
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una FaiExcel()  ");
    }
}

private String getParteWhere(ISASConnection dbc)throws SQLException{
    String mysel="";
    try{
        mysel=" WHERE m.skm_data_apertura <= " + formatDate(dbc,d2_R) +
              " AND (m.skm_data_chiusura IS NULL" +
              " OR m.skm_data_chiusura >= " + formatDate(dbc,d1_R) + ")"+
              " AND a.n_cartella=m.n_cartella"+
              " AND a.data_variazione IN (SELECT MAX(ac.data_variazione) data_var"+
              " FROM anagra_c ac WHERE a.n_cartella=ac.n_cartella)";

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
            mysel = "SELECT DISTINCT m.n_cartella, m.n_contatto,m.skm_data_apertura, m.skm_data_chiusura,"+
                    "u.cod_zona, u.des_zona descrizione_zona," +
                    " "+"u.cod_distretto"+" as cod_distretto, u.des_distretto des_distr," +
	            " u.codice, u.descrizione" +
                    " FROM skmedpal m, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, anagra_c a"+getParteWhere(dbc);

            ragg = (String)par.get("ragg");
            mysel += " AND u.tipo='"+ragg+"'";

//            if (ragg!=null && ragg.equals("C"))
//                mysel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                         " AND u.codice=a.dom_citta)"+
//                         " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                         " AND u.codice=a.citta))";
//            else if (ragg!=null && ragg.equals("A"))
//                mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                         " AND u.codice=a.dom_areadis)"+
//                         " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                         " AND u.codice=a.areadis))";
            
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
            mysel+= " ORDER BY descrizione_zona, des_distr, descrizione";
        }catch(Exception e){
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

private Hashtable AnalizzaZona(ISASConnection dbc,ISASRecord dbr ,Hashtable hzona)
throws SQLException{
        try{
            Hashtable hdistretti=new Hashtable();
            if (dbr.get("descrizione_zona")!=null && !((String)dbr.get("descrizione_zona")).equals(""))            {
                String  zona=(String)dbr.get("descrizione_zona");
                if (hzona!=null && ((Hashtable)hzona.get(zona))!=null)
                      hdistretti=(Hashtable)hzona.get(zona);
                hdistretti=AnalizzaDistretti(dbc,dbr,hdistretti);
                hzona.put(zona,hdistretti);
            }//fine descrizione distretti
            return hzona;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaZona");
	}
}

private Hashtable AnalizzaDistretti(ISASConnection dbc,ISASRecord dbr,Hashtable hdistretti)
throws SQLException{
        try{
            Hashtable hcomuni=new Hashtable();
            if (dbr.get("des_distr")!=null && !((String)dbr.get("des_distr")).equals(""))            {
                String  distretto=(String)dbr.get("des_distr");
                if (hdistretti!=null && ((Hashtable)hdistretti.get(distretto))!=null)
                      hcomuni=(Hashtable)hdistretti.get(distretto);
                hcomuni=AnalizzaComuni(dbc,dbr,hcomuni);
                hdistretti.put(distretto,hcomuni);
            }//fine descrizione distretti
            return hdistretti;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaDistretti");
	}
}

private Hashtable AnalizzaComuni(ISASConnection dbc,ISASRecord dbr,Hashtable hcomuni)
throws SQLException{
        try{
            Vector vDati=new Vector();
            if (dbr.get("descrizione")!=null && !((String)dbr.get("descrizione")).equals(""))            {
                String  comune=(String)dbr.get("descrizione");
                String  codice=(String)dbr.get("codice");
                if (hcomuni!=null && ((Vector)hcomuni.get(comune))!=null)
                    vDati=(Vector)hcomuni.get(comune);
              vDati=caricaDati(dbc,dbr,vDati);
              hcomuni.put(comune, vDati);
            }//fine descrizione comuni
            return hcomuni;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaComuni");
	}
}

public  Vector caricaDati(ISASConnection dbc,ISASRecord dbr ,Vector vDati)
throws SQLException{
        ISASRecord dbConta=null;
        try{
          Hashtable tab=new Hashtable();
          String data_ap = (String)util.getObjectField(dbr,"skm_data_apertura", 'T');
          String data_ch = (String)util.getObjectField(dbr,"skm_data_chiusura", 'T');
          tab.put("#cartella#", (String)util.getObjectField(dbr,"n_cartella", 'I'));
          tab.put("#assistito#",util.getDecode(dbc,"cartella","n_cartella",
                                (String)util.getObjectField(dbr,"n_cartella",'I'),
                                "(nvl(trim(cognome),'') || ' ' || nvl(trim(nome),''))","assistito"));
          tab.put("#contatto#", (String)util.getObjectField(dbr,"n_contatto", 'I'));
          tab.put("#data_apertura#", data_ap);
          tab.put("#data_chiusura#", data_ch);



          java.sql.Date dateAp=java.sql.Date.valueOf(formDate(data_ap,"aaaa-mm-gg",false));
          //java.sql.Date dateCh=java.sql.Date.valueOf(formDate(data_ch,"aaaa-mm-gg",false));
          java.sql.Date date1_R=java.sql.Date.valueOf(formDate(d1_R,"aaaa-mm-gg",false));
          java.sql.Date date2_R=java.sql.Date.valueOf(formDate(d2_R,"aaaa-mm-gg",false));
          //System.out.println("dataAP:"+dateAp+" dataCh:"+dateCh);
          if(dateAp.before(date1_R))
            data_ap = d1_R.substring(8,10)+"/"+d1_R.substring(5,7)+"/"+d1_R.substring(0,4);
          if(data_ch.equals(""))
            data_ch=d2_R.substring(8,10)+"/"+d2_R.substring(5,7)+"/"+d2_R.substring(0,4);
          else{
            java.sql.Date dateCh=java.sql.Date.valueOf(formDate(data_ch,"aaaa-mm-gg",false));
            if(dateCh.after(date2_R))
                data_ch = d2_R.substring(8,10)+"/"+d2_R.substring(5,7)+"/"+d2_R.substring(0,4);
          }

          int giorni = DiffDate(data_ap,data_ch);
          tab.put("#giorni#", ""+giorni);
          vDati.add(tab);
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}

}

private int DiffDate(String data_ini,String data_fine){
        int ret = 0;
        System.out.println("Data Inizio:"+data_ini);
        System.out.println("Data Fine:"+data_fine);
        int anno = Integer.parseInt(data_ini.substring(6,10));
        int mese = Integer.parseInt(data_ini.substring(3,5));
        int giorno = Integer.parseInt(data_ini.substring(0,2));
        DataWI dt1 = new DataWI(anno,mese,giorno);
        ret = dt1.contaGgTra(data_fine.substring(6,10)+
                data_fine.substring(3,5)+data_fine.substring(0,2));
        //aggiungo 1 perch� mi perde un giorno
        ret =ret+1;
        System.out.println("Giorni:"+ret);
        return ret;
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

  private String formDate(Object dataI,String formato,boolean it){
  String giorno="00";
  String mese="00";
  String anno="0000";
  String data="";
  if(dataI!=null){
    if(dataI instanceof java.sql.Date) data=((java.sql.Date)dataI).toString();
    else if(dataI instanceof String) data=(String)dataI;
  }
  //it=false => aaaammgg
  //it=true => ggmmaaaa
  if(data!=null && !data.equals("")){

      if(data.charAt(2)=='-' ||data.charAt(2)=='/')//gg-mm-aaaa
      {
        giorno=data.substring(0,2);
        mese=data.substring(3,5);
        anno=data.substring(6,10);
      }else  if(data.charAt(4)=='-' ||data.charAt(4)=='/')//aaaa-mm-gg
      {
        giorno=data.substring(8,10);
        mese=data.substring(5,7);
        anno=data.substring(0,4);
      }else //ggmmaaaa aaaammgg
      { if(it==false)//aaaammgg
        {
         giorno=data.substring(6,8);
         mese=data.substring(4,6);
         anno=data.substring(0,4);
        }
        else//ggmmaaaa
        {
         giorno=data.substring(0,2);
         mese=data.substring(2,4);
         anno=data.substring(4,8);
       }
      }
  }
  if(formato.equals("gg-mm-aaaa"))
    return giorno+"-"+mese+"-"+anno;
  else if(formato.equals("aaaa-mm-gg"))
    return anno+"-"+mese+"-"+giorno;
  else if(formato.equals("ggmmaaaa"))
   return giorno+mese+anno;
  else if(formato.equals("aaaammgg"))
    return anno+mese+giorno;
  else return data;
}
}	// End of FoUtePato class


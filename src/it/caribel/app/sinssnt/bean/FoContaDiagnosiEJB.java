package it.caribel.app.sinssnt.bean;
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 10/09/2007 - EJB di connessione alla procedura SINS Tabella FoContaDiagnosi
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

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;

import java.util.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.DataWI;

public class FoContaDiagnosiEJB extends SINSSNTConnectionEJB {

it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

String ragg="";
String d1="";
String d2="";
String diag="";
String met="";
String sint="";
String TYPE="";
String dom_res;
String dr;

String[] vMet_campo={"sks_linfonodi","sks_polmonare","sks_pleura","sks_peritoneo","sks_fegato",
                     "sks_rene","sks_encefalo","sks_urinari","sks_pelle","sks_ossa","sks_loc_avanzate","sks_altro"};
String[] vMet_nome={"Linfonodi","Polmonare","Pleura","Peritoneo","Fegato","Rene",
                    "Encefalo, midollo spinale e sistema nervoso","Organi urinari","Pelle","Ossa e midollo","Localmente avanzato","Altro"};
String[] vSint_campo={"sks_dolore","sks_vomito","sks_nausea","sks_febbre","sks_astenia","sks_anemia","sks_dispnea",
                     "sks_edemi","sks_micosi","sks_ascite","sks_calo","sks_cachessia","sks_stipsi","sks_inappetenza",
                     "sks_ittero","sks_diarrea","sks_disuria","sks_disidratazione","sks_insonnia","sks_confusione",
//gb 19.12.08                     "sks_depressione","sks_lesioni_decubito","sks_ansia","sks_vertigine","sks_afasia","sks_disfagia"};
		     //gb 19.12.08
                     "sks_depressione","sks_lesioni_decubito","sks_ansia","sks_vertigine","sks_afasia","sks_disfagia","sks_altro"};
String[] vSint_nome={"Dolore","Vomito","Nausea","Febbre","Astenia","Anemia","Dispnea","Edemi declivi","Micosi orale",
                     "Ascite","Calo ponderale","Cachessia neoplastica","Stipsi","Inappetenza","Ittero","Diarrea",
                     "Disuria","Disidratazione","Insonnia","Confusione mentale","Depressione","Lesioni da decubito",
//gb 19.12.08                     "Ansia","Vertigine","Afasia","Disfagia"};
		     //gb 19.12.08
                     "Ansia","Vertigine","Afasia","Disfagia","Altro"};

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


public FoContaDiagnosiEJB() {}

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
			d1=(String)par.get("d1");
		if (par.get("d2") != null && !((String)par.get("d2")).equals(""))
			d2=(String)par.get("d2");
                diag=(String)par.get("diag");
                met=(String)par.get("met");
                sint=(String)par.get("sint");
                TYPE=(String)par.get("TYPE");

		String myselect = mkSelectAnalitica(dbc, par);
                System.out.println("SELECT FoContaDiagnosi: "+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
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
          tab.put("#zona#", (String)dbr.get("descrizione_zona"));
          tab.put("#distretto#", (String)dbr.get("des_distr"));
          tab.put("#comune#", (String)dbr.get("descrizione"));
          doc.writeSostituisci("tabella", tab);
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una FaiExcel()  ");
    }
}
private String getJoinComune(String comune)throws SQLException{
    String mysel="";
    try{
        if(!comune.equals("NESSUNA DIVISIONE")){
//            if (ragg!=null && ragg.equals("C"))
//                mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                         " AND a.dom_citta='"+comune+"')"+
//                         " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                         " AND a.citta='"+comune+"'))";
//            else if (ragg!=null && ragg.equals("A"))
//                mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                         " AND a.dom_areadis='"+comune+"')"+
//                         " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                         " AND a.areadis='"+comune+"'))";
            //Aggiunto Controllo Domicilio/Residenza (BYSP)
        	if(this.dom_res == null)
        	{
        		if (ragg!=null && ragg.equals("C"))
                    mysel += " AND (((a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                             " AND a.dom_citta='"+comune+"')"+
                             " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                             " AND a.citta='"+comune+"'))";
                else if (ragg!=null && ragg.equals("A"))
                    mysel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                             " AND a.dom_areadis='"+comune+"')"+
                             " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                             " AND a.areadis='"+comune+"'))";
        	}
        	else if (this.dom_res.equals("D"))
        	                          {
        	                           if (ragg.equals("C"))
        	          mysel += " AND a.dom_citta='"+comune+"'";
        	                            else if (ragg.equals("A"))
        	          mysel += " AND a.dom_areadis='"+comune+"'";
        	                          }

        	else if (this.dom_res.equals("R"))
						        	{
						                if (ragg.equals("C"))
						mysel += " AND a.citta='"+comune+"'";
						                 else if (ragg.equals("A"))
						mysel += " AND a.areadis='"+comune+"'";
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
              " AND a.n_cartella=m.n_cartella"+
              " AND a.data_variazione IN (SELECT MAX(ac.data_variazione) data_var"+
              " FROM anagra_c ac WHERE a.n_cartella=ac.n_cartella)";

    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una getParteWhere()  ");
    }
    return mysel;
}
private String getParteWhere2(ISASConnection dbc)throws SQLException{
    String mysel="";
    try{
        mysel=" WHERE m.skm_data_apertura <= " + formatDate(dbc,d1) +
              " AND (m.skm_data_chiusura IS NULL" +
              " OR m.skm_data_chiusura >= " + formatDate(dbc,d1) + ")"+
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
            String zona="";
            String distretto="";
            String comune="";
            String tipoStampa= (String)par.get("terr");
            StringTokenizer st = new StringTokenizer(tipoStampa,"|");
            String sZona=st.nextToken();
            String sDis=st.nextToken();
            String sCom=st.nextToken();

            if(sZona.equals("1"))
               zona= " u.cod_zona,u.des_zona,";
            else  zona= " 'NESSUNA DIVISIONE' cod_zona,'NESSUNA DIVISIONE' des_zona,";
            if(sDis.equals("1"))
               distretto= " u.des_distretto,"+"u.cod_distretto"+" as cod_distretto,";
            else  distretto= " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto,";
            if(sCom.equals("1"))
               comune= " u.codice ,u.descrizione ";
            else  comune= " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione ";
            mysel = "SELECT DISTINCT "+zona+distretto+comune+

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
            //mysel+= " ORDER BY descrizione_zona, des_distr, descrizione";
        }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una getParteWhere()  ");
        }
	return mysel;
}




private void StampaDaZona(Hashtable hzona, mergeDocument doc, ISASConnection dbc){
    Enumeration enumZona =orderedKeys(hzona);
    int i = 0;
    while (enumZona.hasMoreElements()){
        String zona = "" + enumZona.nextElement();
        Hashtable hdistretti = (Hashtable)hzona.get(zona);
        Enumeration enumDistretti = orderedKeys(hdistretti);
        while (enumDistretti.hasMoreElements()) {
              String distretto = "" + enumDistretti.nextElement();
              Hashtable hcomuni = (Hashtable) hdistretti.get(distretto);
              Enumeration enumComuni = orderedKeys(hcomuni);
               while (enumComuni.hasMoreElements()){
                  if(i!=0)      doc.write("break");
                  String comune = "" + enumComuni.nextElement();
                  i++;
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
                  //System.out.println("*/*/*/*Scrivo zona");
                  doc.writeSostituisci("zona",htab);
                  Vector vDati = (Vector) hcomuni.get(comune);
                  //doc.write("inizioTab");
                  Enumeration enumVett = vDati.elements();
                  while (enumVett.hasMoreElements()){
                        Hashtable hConta=(Hashtable)enumVett.nextElement();
                        Enumeration enumConta = orderedKeys(hConta);
                        while (enumConta.hasMoreElements()){
                            String tipo = "" + enumConta.nextElement();
                            Hashtable hTipo = (Hashtable) hConta.get(tipo);
                            Enumeration enumTipo = orderedKeys(hTipo);
                            int k = 0;
                            boolean entrato=false;
                            while (enumTipo.hasMoreElements()){
                                String num = "" + enumTipo.nextElement();
                                Hashtable hDati = new Hashtable();
                                entrato=true;
                                hDati = (Hashtable) hTipo.get(num);
                                if(!TYPE.equals("PDF")){
                                    hDati.put("#zona#", zona);
                                    hDati.put("#distretto#", distretto);
                                    hDati.put("#comune#", comune);
                                }
                                if(hDati.get("#tipo#")==null)
                                    hDati.put("#tipo#","");
                                if(hDati.get("#assistenza#")==null)
                                    hDati.put("#assistenza#","");
                                if(hDati.get("#carico#")==null)
                                    hDati.put("#carico#","");
                                if(hDati.get("#percento#")==null)
                                    hDati.put("#percento#","");
                                if(tipo.equals("0")){
                                    if(k==0){
                                        Hashtable Htit = new Hashtable();
                                        Htit.put("#titolo#", "DIAGNOSI");
                                        Htit.put("#totale#", ""+hDati.get("#totale_pazienti#"));
                                        //System.out.println("*/*/*/*Scrivo titolo");
                                        doc.writeSostituisci("titolo", Htit);
                                        //System.out.println("*/*/*/*inizioTabDiag");
                                        doc.write("inizioTabDiag");
                                    }
                                    k++;
                                    //System.out.println("*/*/*/*Scrivo tabellaDiag");
                                    doc.writeSostituisci("tabellaDiag",hDati);
                                }else{
                                    if(k==0){
                                        Hashtable Htit = new Hashtable();
                                        if(tipo.equals("1"))    Htit.put("#titolo#", "METASTASI");
                                        else                    Htit.put("#titolo#", "SINTOMI");
                                        Htit.put("#totale#", ""+hDati.get("#totale_pazienti#"));
                                        //System.out.println("*/*/*/*Scrivo titolo");
                                        doc.writeSostituisci("titolo", Htit);
                                        //System.out.println("*/*/*/*Scrivo iniziotab");
                                        doc.write("inizioTab");
                                    }
                                    k++;
                                    //System.out.println("*/*/*/*Scrivo tabella");
                                    doc.writeSostituisci("tabella",hDati);
                                }
                            }
                            if(entrato==true){
                                //System.out.println("*/*/*/*Scrivo finetab");
                                doc.write("finetab");
                            }
                        }
                  }
              }//fine  ciclo comuni
        }//fine  ciclo distretti
    }//fine  ciclo zona
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
        String sel_tipo="";
        int tot_tipo=0;
        int pazienti=0;
        ISASRecord dbConta=null;
        Hashtable hTipo=new Hashtable();
        try{
              for (int j=0; j<3; j++){
                Hashtable hAusi = new Hashtable();
                if(j==0){
                  //tiro fuori il numero dei pazienti che hanno una diagnosi
                  String sel_tot = "SELECT COUNT(DISTINCT m.n_cartella) pazienti FROM skmedpal m, anagra_c a, diagnosi d "+
                                   getParteWhere(dbc)+getJoinComune(comune)+" AND m.n_cartella=d.n_cartella "+
                                   " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag) FROM diagnosi " +
                                   " WHERE diagnosi.n_cartella = d.n_cartella" +
                                   " AND diagnosi.n_cartella = d.n_cartella" +
                                   " AND diagnosi.data_diag <= " + formatDate(dbc,d2) +
                                   " AND diagnosi.data_diag >= " + formatDate(dbc,d1) + ")";

                  if(!diag.equals(""))  sel_tot+=" AND d.diag1='"+diag+"'";

                  System.out.println("*/*/*/*/*/*Select pazienti in assistenza con diagnosi:"+sel_tot);
                  ISASRecord dbTot = dbc.readRecord(sel_tot);
                  if(dbTot!=null && dbTot.get("pazienti")!=null){
                      pazienti=util.getIntField(dbTot, "pazienti");//pazienti totali
                      //tiro fuori tutte le diagnosi inserite all'interno del contatto dell'oncologo
                      sel_tipo = "SELECT DISTINCT d.diag1, pa.diagnosi "+
                                 "FROM skmedpal m, anagra_c a, diagnosi d, tab_diagnosi pa "+
                                 getParteWhere(dbc)+getJoinComune(comune)+" AND m.n_cartella=d.n_cartella "+
                                 " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag) FROM diagnosi " +
                                 " WHERE diagnosi.n_cartella = d.n_cartella" +
                                 " AND diagnosi.n_cartella = d.n_cartella" +
                                 " AND diagnosi.data_diag <= " + formatDate(dbc,d2) +
                                 " AND diagnosi.data_diag >= " + formatDate(dbc,d1) + ")"+
                                 " AND d.diag1 = pa.cod_diagnosi";

                      if(!diag.equals(""))  sel_tipo+=" AND d.diag1='"+diag+"'";

                      System.out.println("*/*/*/*Select per diagnosi:"+sel_tipo);
                      ISASCursor dbcur = dbc.startCursor(sel_tipo);
                      int i = 0;
                      while (dbcur.next()){
                        Hashtable tab=new Hashtable();
                        ISASRecord dbrTipo= dbcur.getRecord();
                        //tiro fuori i pazienti in assistenza
                        String sel_ass = "SELECT COUNT(DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a, diagnosi d "+
                                         getParteWhere(dbc)+getJoinComune(comune)+
                                         " AND d.n_cartella = m.n_cartella"+
                                         " AND d.diag1='"+(String)dbrTipo.get("diag1")+"'"+
                                         " AND d.data_diag <= " + formatDate(dbc,d2) +
                                         " AND d.data_diag >= " + formatDate(dbc,d1) ;
                        System.out.println("*/*/*/*numero assistiti totale per diagnosi"+sel_ass);
                        dbConta=dbc.readRecord(sel_ass);
                        if(dbConta!=null && dbConta.get("conta")!=null){
                            int conta = util.getIntField(dbConta, "conta");//pazienti in assistenza con diagnosi
                            tab.put("#assistenza#", ""+conta);
                            tab.put("#percento#", ""+((conta*100)/pazienti)+"%");
                            tab.put("#tipo#", (String)dbrTipo.get("diag1")+" "+(String)dbrTipo.get("diagnosi"));
                        }
                        String sel_car = "SELECT COUNT(DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a, diagnosi d "+
                                         getParteWhere2(dbc)+getJoinComune(comune)+
                                         " AND d.n_cartella = m.n_cartella"+
                                         " AND d.diag1='"+(String)dbrTipo.get("diag1")+"'"+
                                         " AND d.data_diag <= " + formatDate(dbc,d2) +
                                         " AND d.data_diag >= " + formatDate(dbc,d1) ;
                        System.out.println("*/*/*/*numero assistiti in carico per diagnosi"+sel_car);
                        dbConta=dbc.readRecord(sel_car);
                        if(dbConta!=null && dbConta.get("conta")!=null){
                            int conta = util.getIntField(dbConta, "conta");//pazienti gi� assistenza con diagnosi
                            tab.put("#carico#", ""+conta);
                        }
                        //System.out.println("Diagnosi:"+tab);
                        tab.put("#totale_pazienti#", ""+pazienti);
                        hAusi.put(""+i, tab);
                        i++;
                      }
                      dbcur.close();
                  }
                }else if (j!=0){
                    //Numero di pazienti in assistenza
                    String sel_tot = "SELECT COUNT(DISTINCT m.n_cartella) pazienti FROM skmedpal m, anagra_c a "+
                                     getParteWhere(dbc)+getJoinComune(comune);
                                     //" GROUP BY n_cartella";
                    System.out.println("*/*/*/*/*/*Select pazienti in assistenza :"+sel_tot);
                    ISASRecord dbTot = dbc.readRecord(sel_tot);
                    if(dbTot!=null && dbTot.get("pazienti")!=null)
                        pazienti=util.getIntField(dbTot, "pazienti");//pazienti totali

                    if(j==1){//devo analizzare la parte della metastasi
                        int contatore = 0;
                        if(!met.equals("")){
                            for(int l=0; l<vMet_campo.length; l++){
                                if(vMet_campo[l].equals(met)){
                                    contatore=l;
                                    l=vMet_campo.length-1;
                                }
                            }
                        }
                        for(int m=0; m<vMet_campo.length; m++){
                            Hashtable tab=new Hashtable();
                            sel_tot = "SELECT COUNT(DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a,"+
                                      "skmpal_metastasi mt "+
                                      getParteWhere(dbc)+getJoinComune(comune)+
                                      " AND m.n_cartella=mt.n_cartella AND m.n_contatto=mt.n_contatto";
                            if(met.equals(""))
                                sel_tot+=" AND mt."+vMet_campo[m]+"='S'";
                            else{
                                sel_tot+=" AND mt."+vMet_campo[contatore]+"='S'";
                                m=vMet_campo.length-1;
                            }

                            System.out.println("*/*/*/*/*/*Select pazienti per metastasi :"+sel_tot);
                            dbConta=dbc.readRecord(sel_tot);
                            if(dbConta!=null && dbConta.get("conta")!=null){
                                //System.out.println("dbConta in metastasi:"+dbConta.toString());
                                int conta = util.getIntField(dbConta, "conta");
                                tab.put("#assistenza#", ""+conta);
                                tab.put("#percento#", ""+((conta*100)/pazienti)+"%");
                                if(met.equals(""))      tab.put("#tipo#", vMet_nome[m]);
                                else                    tab.put("#tipo#", vMet_nome[contatore]);
                            }
                            //System.out.println("Metastasi:"+tab);
                            tab.put("#totale_pazienti#", ""+pazienti);
                            hAusi.put(""+m, tab);
                        }
                    }else{//devo analizzare la parte deli sintomi
                        int contatore = 0;
                        if(!sint.equals("")){
                            for(int l=0; l<vSint_campo.length; l++){
                                if(vSint_campo[l].equals(sint)){
                                    contatore=l;
                                    l=vSint_campo.length-1;
                                }
                            }
                        }
                        for(int s=0; s<vSint_campo.length; s++){
                            Hashtable tab=new Hashtable();
                            sel_tot = "SELECT COUNT(DISTINCT m.n_cartella) conta FROM skmedpal m, anagra_c a,"+
                                      "skmpal_sintomi st "+
                                      getParteWhere(dbc)+getJoinComune(comune)+
                                      " AND m.n_cartella=st.n_cartella AND m.n_contatto=st.n_contatto";
                            if(sint.equals(""))
                                sel_tot+=" AND st."+vSint_campo[s]+"='S'";
                            else{
                                sel_tot+=" AND st."+vSint_campo[contatore]+"='S'";
                                s=vSint_campo.length-1;
                            }
                            System.out.println("*/*/*/*/*/*Select pazienti per sintomo :"+sel_tot);
                            dbConta=dbc.readRecord(sel_tot);
                            if(dbConta!=null && dbConta.get("conta")!=null){
                                //System.out.println("dbConta in sintomi:"+dbConta.toString());
                                int conta = util.getIntField(dbConta, "conta");
                                tab.put("#assistenza#", ""+conta);
                                tab.put("#percento#", ""+((conta*100)/pazienti)+"%");
                                if(sint.equals(""))    tab.put("#tipo#", vSint_nome[s]);
                                else                   tab.put("#tipo#", vSint_nome[contatore]);
                            }
                            //System.out.println("Sintomi:"+tab);
                            tab.put("#totale_pazienti#", ""+pazienti);
                            hAusi.put(""+s, tab);
                        }
                    }
                }
                hTipo.put(""+j, hAusi);
              }//fine for
              vDati.add(hTipo);
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


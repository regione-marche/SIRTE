package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 15/05/2007 - EJB di connessione alla procedura SINS Tabella FoAssNoPato
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

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
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

public class FoAssNoPatoEJB extends SINSSNTConnectionEJB {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
public FoAssNoPatoEJB() {}
int j = 0;//variabile che mi permette di stampare una sola volta l'header della tabella
          //se la stampa � stata richiesta in formato html
String vecchioCartella="";
Hashtable vDati = new Hashtable();
private void preparaLayout(mergeDocument md, ISASConnection dbc, Hashtable par) {
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
        String d1=(String)par.get("d1");
        d1=d1.substring(0 ,2)+"/"+d1.substring(3, 5)+"/"+d1.substring(6, 10);
        htxt.put("#d1#",d1);
        d1=(String)par.get("d2");
        d1=d1.substring(0 ,2)+"/"+d1.substring(3, 5)+"/"+d1.substring(6, 10);
        htxt.put("#d2#",d1);
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        md.writeSostituisci("layout",htxt);
}



public byte[] query_senzaPato(String utente, String passwd, Hashtable par,
	mergeDocument doc) throws SQLException {

	System.out.println("FoAssNoPatoEJB.query_senzaPato(): DEBUG inizio...");
	boolean done=false;
    ISASCursor dbcur = null;
	ISASConnection dbc=null;
    ServerUtility su =new ServerUtility();
    String mysel="";
    String mysel1="";
    try {
            myLogin lg = new myLogin();
            lg.put(utente,passwd);
            dbc = super.logIn(lg);

            mysel += " AND u.codice = operatori.cod_presidio AND u.tipo='P'";
            mysel = su.addWhere(mysel, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String)par.get("zona"));
            mysel = su.addWhere(mysel, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String)par.get("distretto"));
            mysel = su.addWhere(mysel, su.REL_AND, "u.codice", su.OP_EQ_STR, (String)par.get("pca"));

            mysel1 += " AND ub.codice = op.cod_presidio AND ub.tipo='P'";
            mysel1 = su.addWhere(mysel1, su.REL_AND, "ub.cod_zona", su.OP_EQ_STR, (String)par.get("zona"));
            mysel1 = su.addWhere(mysel1, su.REL_AND, "ub.cod_distretto", su.OP_EQ_STR, (String)par.get("distretto"));
            mysel1 = su.addWhere(mysel1, su.REL_AND, "ub.codice", su.OP_EQ_STR, (String)par.get("pca"));
            for (int i=1; i<=4; i++){
                String sel = "";
                if(i==1){
//gb 19/10/07                    sel = "SELECT DISTINCT c.n_cartella, ca.cognome, ca.nome, c.ass_referente, "+
                    sel = "SELECT DISTINCT c.n_cartella, ca.cognome, ca.nome, c.ap_ass_ref, "+
                          "operatori.cognome cogop, operatori.nome nomop,"+
                          "u.cod_zona, u.des_zona descrizione_zona, u.cod_distretto, "+
                          "u.des_distretto des_distr, u.codice, u.descrizione" +
//gb 19/10/07                " FROM contatti c, cartella ca, ubicazioni_n u, operatori"+
//gb 19/10/07 ******* sostituito contatti con ass_progetto.
                          " FROM ass_progetto c, cartella ca, ubicazioni_n u, operatori"+
                          " WHERE ca.n_cartella=c.n_cartella AND"+
//gb 19/10/07                          " c.tipo_servizio='1' AND "+
//gb 19/10/07 *******
                          " EXISTS ( SELECT * FROM ass_interventi si" +
				" WHERE c.n_cartella = si.n_cartella" +
				" AND c.n_progetto = si.n_progetto" +
				" AND si.int_accessi = 'S') AND "+				
//gb 19/10/07: fine *******
/*gb 19/10/07 *******
                          " c.ass_referente=operatori.codice AND"+
                          " c.data_contatto<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (c.data_chiusura IS NULL OR c.data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+")"+
*gb 19/10/07: fine *******/
//gb 19/10/07 *******
                          " c.ap_ass_ref = operatori.codice AND"+
                          " c.ap_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (c.ap_data_chiusura IS NULL OR c.ap_data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+")"+
//gb 19/10/07: fine *******
                          " AND c.n_cartella NOT IN (SELECT (d.n_cartella) FROM diagnosi d WHERE"+
                          " c.n_cartella=d.n_cartella)"+mysel+
/*gb 19/10/07 *******
                          " AND c.data_contatto IN "+
                          "(SELECT MAX(co.data_contatto) data FROM contatti co, cartella cr, ubicazioni_n ub, operatori op"+
                          " WHERE cr.n_cartella=co.n_cartella AND"+
                          " co.ass_referente=op.codice AND"+
                          " c.n_cartella=co.n_cartella AND "+
                          " co.data_contatto<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.data_chiusura IS NULL OR co.data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+")"+
*gb 19/10/07: fine *******/
//gb 19/10/07 *******
                          " AND c.ap_data_apertura IN "+
                          "(SELECT MAX(co.ap_data_apertura) data FROM ass_progetto co, cartella cr, ubicazioni_n ub, operatori op"+
                          " WHERE cr.n_cartella=co.n_cartella AND"+
                          " co.ap_ass_ref = op.codice AND"+
                          " c.n_cartella=co.n_cartella AND "+
                          " co.ap_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.ap_data_chiusura IS NULL OR co.ap_data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+")"+
//gb 19/10/07: fine *******
                          " AND co.n_cartella NOT IN (SELECT (dg.n_cartella) FROM diagnosi dg WHERE"+
                          " co.n_cartella=dg.n_cartella)"+mysel1+")";


                    sel += " ORDER BY descrizione_zona, des_distr, descrizione, cogop, nomop,c.n_cartella";
                }//fine if==1
                else if(i==2){
                    sel = "SELECT DISTINCT c.n_cartella, ca.cognome, ca.nome, c.ski_infermiere, "+
                          "operatori.cognome cogop, operatori.nome nomop,"+
                          "u.cod_zona, u.des_zona descrizione_zona, u.cod_distretto, "+
                          "u.des_distretto des_distr, u.codice, u.descrizione" +
                          " FROM skinf c, cartella ca, ubicazioni_n u, operatori"+
                          " WHERE ca.n_cartella=c.n_cartella AND"+
                          " c.ski_infermiere=operatori.codice AND"+
                          " c.ski_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (c.ski_data_uscita IS NULL OR c.ski_data_uscita>="+formatDate(dbc,(String)par.get("d1"))+")"+
                          " AND c.n_cartella NOT IN (SELECT (d.n_cartella) FROM diagnosi d WHERE"+
                          " c.n_cartella=d.n_cartella)"+mysel+
                          " AND c.ski_data_apertura IN "+
                          "(SELECT MAX(co.ski_data_apertura) data FROM skinf co, cartella cr, ubicazioni_n ub, operatori op"+
                          " WHERE cr.n_cartella=co.n_cartella AND"+
                          " co.ski_infermiere=op.codice AND"+
                          " c.n_cartella=co.n_cartella AND "+
                          " co.ski_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.ski_data_uscita IS NULL OR co.ski_data_uscita>="+formatDate(dbc,(String)par.get("d1"))+")"+
                          " AND co.n_cartella NOT IN (SELECT (dg.n_cartella) FROM diagnosi dg WHERE"+
                          " co.n_cartella=dg.n_cartella)"+mysel1+")";

                   sel += " ORDER BY descrizione_zona, des_distr, descrizione, cogop, nomop,c.n_cartella";
                }//fine if
                else if(i==3){
                    sel = "SELECT DISTINCT int_cartella n_cartella, c.cognome, c.nome, int_codoper, "+
                          "operatori.cognome cogop, operatori.nome nomop,"+
                          "u.cod_zona, u.des_zona descrizione_zona, u.cod_distretto, "+
                          "u.des_distretto des_distr, u.codice, u.descrizione" +
                          " FROM intmmg, cartella c, ubicazioni_n u, operatori, progetto pr"+
                          " WHERE c.n_cartella=int_cartella AND"+
                          " int_cartella=pr.n_cartella AND "+
                          " pr_data<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND pr_data>="+formatDate(dbc,(String)par.get("d1"))+
                          " AND int_codoper=operatori.codice AND"+
                          " int_data<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND int_data>="+formatDate(dbc,(String)par.get("d1"))+
                          " AND c.n_cartella NOT IN (SELECT (d.n_cartella) FROM diagnosi d WHERE"+
                          " c.n_cartella=d.n_cartella)"+
/*gb 29/10/07 *******
                          " AND c.n_cartella NOT IN (SELECT (co.n_cartella) FROM contatti co WHERE "+
                          " co.n_cartella=c.n_cartella AND co.data_contatto<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.data_chiusura IS NULL OR co.data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+"))"+
*gb 29/10/07: fine *******/
//gb 29/10/07 ******* sostituito contatti con ass_progetto.
                          " AND c.n_cartella NOT IN (SELECT (co.n_cartella) FROM ass_progetto co WHERE "+
                          " co.n_cartella=c.n_cartella AND co.ap_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.ap_data_chiusura IS NULL OR co.ap_data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+"))"+
//gb 29/10/07: fine *******

                          " AND c.n_cartella NOT IN (SELECT (co.n_cartella) FROM skinf co WHERE "+
                          " co.n_cartella=c.n_cartella AND co.ski_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.ski_data_uscita IS NULL OR co.ski_data_uscita>="+formatDate(dbc,(String)par.get("d1"))+
                          "))"+mysel;

                    sel += " ORDER BY descrizione_zona, des_distr, descrizione, cogop, nomop, int_cartella";
                }//fine if
                else if(i==4){
                    sel = "SELECT DISTINCT int_cartella n_cartella, c.cognome, c.nome, int_cod_oper, "+
                          "operatori.cognome cogop, operatori.nome nomop,"+
                          "u.cod_zona, u.des_zona descrizione_zona, u.cod_distretto, "+
                          "u.des_distretto des_distr, u.codice, u.descrizione" +
                          " FROM interv, cartella c, ubicazioni_n u, operatori, progetto pr"+
                          " WHERE c.n_cartella=int_cartella AND"+
                          " int_cartella=pr.n_cartella AND "+
                          " int_data_prest<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND int_data_prest>="+formatDate(dbc,(String)par.get("d1"))+
                          " AND int_cod_oper=operatori.codice AND"+
                          " pr_data<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND pr_data>="+formatDate(dbc,(String)par.get("d1"))+
//gb 29/10/07                          " AND int_contatto='0' AND (int_tipo_oper='98' OR int_tipo_oper='99')"+
//gb 29/10/07: ******* ora si distingue tra sociale (n_progetto) e non sociale (int_contatto).
			  " AND (int_contatto='0' AND n_progetto IS NULL)" +
/*gb 29/10/07 ******* si mette solo una clausola in quanto e perch� non si prende in considerazione
//			il tipo operatore 01 ma solo 98 e 99 (vedi clausola sotto).
                          " AND ((int_contatto='0' AND n_progetto = '0') OR " +
				"(int_contatto='0' AND n_progetto IS NULL))
*gb 29/10/07: fine *******/
//gb 29/10/07: fine *******
			  " AND (int_tipo_oper='98' OR int_tipo_oper='99')"+
                          " AND c.n_cartella NOT IN (SELECT (d.n_cartella) FROM diagnosi d WHERE"+
                          " c.n_cartella=d.n_cartella)"+

/*gb 29/10/07 *******
                          " AND c.n_cartella NOT IN (SELECT (co.n_cartella) FROM contatti co WHERE "+
                          " co.n_cartella=c.n_cartella AND co.data_contatto<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.data_chiusura IS NULL OR co.data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+"))"+
*gb 29/10/07: fine *******/
//gb 29/10/07 ******* sostituito contatti con ass_progetto.
                          " AND c.n_cartella NOT IN (SELECT (co.n_cartella) FROM ass_progetto co WHERE "+
                          " co.n_cartella=c.n_cartella AND co.ap_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.ap_data_chiusura IS NULL OR co.ap_data_chiusura>="+formatDate(dbc,(String)par.get("d1"))+"))"+
//gb 29/10/07: fine *******
                          " AND c.n_cartella NOT IN (SELECT (co.n_cartella) FROM skinf co WHERE "+
                          " co.n_cartella=c.n_cartella AND co.ski_data_apertura<="+formatDate(dbc,(String)par.get("d2"))+
                          " AND (co.ski_data_uscita IS NULL OR co.ski_data_uscita>="+formatDate(dbc,(String)par.get("d1"))+
                          "))"+mysel;

                    sel += " ORDER BY descrizione_zona, des_distr, descrizione, cogop, nomop, int_cartella";
                }//fine if
                System.out.println("FoAssNoPatoEJB.query_senzaPato(): DEBUG "+i+" ["+sel+"].");

                dbcur = dbc.startCursor(sel);
                if(((String)par.get("TYPE")).equals("PDF")){
                    if(i==1){
                        preparaLayout(doc, dbc, par);
                        Hashtable htit=new Hashtable();
                        htit.put("#descrizione#", "CONTATTO SOCIALE");
                        doc.writeSostituisci("titolo", htit);
                    }
                    else{
                        doc.write("break");
                        Hashtable htit=new Hashtable();
                        if(i==2)    htit.put("#descrizione#", "CONTATTO SANITARIO");
                        else if(i==3)    htit.put("#descrizione#", "ACCESSI MMG");
                        else if(i==4)    htit.put("#descrizione#", "DATI SAPIO");
                        doc.writeSostituisci("titolo", htit);
                    }
                    Hashtable hGenerale=new Hashtable();
                    while (dbcur.next()){
                       ISASRecord dbr= dbcur.getRecord();
                       hGenerale=AnalizzaZona(dbc,dbr,hGenerale);
                    }
                    if (hGenerale.size()!=0)
                        StampaDaZona(hGenerale,doc,dbc);
                    else
                        doc.write("messaggio");
                }else{
                    if(i==1){
                        //doc.write("layout");
                        preparaLayout(doc, dbc, par);
                        Hashtable htit=new Hashtable();
                        htit.put("#descrizione#", "CONTATTO SOCIALE");
                        doc.writeSostituisci("titolo", htit);
                    }
                    else{
                        Hashtable htit=new Hashtable();
                        if(i==2)         htit.put("#descrizione#", "CONTATTO SANITARIO");
                        else if(i==3)    htit.put("#descrizione#", "ACCESSI MMG");
                        else if(i==4)    htit.put("#descrizione#", "DATI SAPIO");
                        doc.writeSostituisci("titolo", htit);
                    }
                    int k=0;
                    while(dbcur.next()){
                        if(k==0)    doc.write("iniziotab");
                        k++;
                        ISASRecord dbr = dbcur.getRecord();
                        FaiExcel(dbc, dbr, doc);
                    }doc.write("finetab");
                }
            }
            dbcur.close();

            doc.write("finale");
	        dbcur.close();
            doc.close();

	        dbc.close();
            super.close(dbc);
            done=true;
            return doc.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("FoAssNoPatoEJB.query_senzaPato(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				System.out.println("FoAssNoPatoEJB.query_senzaPato): "+e1);
			}
		}
	}
}	// End of query_storico() method

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
          tab.put("#cartella#", (String)util.getObjectField(dbr,"n_cartella", 'I'));
          tab.put("#operatore#",(String)util.getObjectField(dbr,"cogop", 'S')+" "+
                                 (String)util.getObjectField(dbr,"nomop", 'S'));
          tab.put("#assistito#", (String)util.getObjectField(dbr,"cognome", 'S')+" "+
                                 (String)util.getObjectField(dbr,"nome", 'S'));
          vDati.add(tab);
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}
}

private void StampaDaZona(Hashtable hzona, mergeDocument doc, ISASConnection dbc){
    Enumeration enumZona =util.getOrderedKeys(hzona);
    int conta = 0;
    while (enumZona.hasMoreElements()){
        String zona = "" + enumZona.nextElement();
        Hashtable hdistretti = (Hashtable)hzona.get(zona);
        Enumeration enumDistretti = util.getOrderedKeys(hdistretti);
        while (enumDistretti.hasMoreElements()) {
              String distretto = "" + enumDistretti.nextElement();
              Hashtable hcomuni = (Hashtable) hdistretti.get(distretto);
              Enumeration enumComuni = util.getOrderedKeys(hcomuni);
               while (enumComuni.hasMoreElements()){
                  String comune = "" + enumComuni.nextElement();
                  Hashtable htab=new Hashtable();
                  htab.put("#descrizione_zona#",zona);
                  htab.put("#des_distr#",distretto);
                  htab.put("#tipologia#", "Presidio");
                  htab.put("#descrizione#",comune);
                  doc.writeSostituisci("zona",htab);
                  Vector vDati = (Vector) hcomuni.get(comune);
                  doc.write("iniziotab");
                  Enumeration enumVett = vDati.elements();
                  int parziale = 0;
                  while (enumVett.hasMoreElements()){
                        Hashtable hDati=(Hashtable)enumVett.nextElement();
                        doc.writeSostituisci("tabella",hDati);
                        parziale++;
                  }
                  //Devo scrivere i totali
                  doc.write("finetab");
                  Hashtable Htot = new Hashtable();
                  Htot.put("#totale#", ""+parziale);
                  Htot.put("#desc#", "PER PRESIDIO");
                  doc.writeSostituisci("totali", Htot);
                  conta+=parziale;
              }//fine  ciclo comuni
        }//fine  ciclo distretti
    }//fine  ciclo zona
    Hashtable Htot = new Hashtable();
    Htot.put("#totale#", ""+conta);
    Htot.put("#desc#", "GENERALE");
    doc.writeSostituisci("totali", Htot);
}

private void FaiExcel(ISASConnection dbc, ISASRecord dbr, mergeDocument doc)throws SQLException{
    ISASRecord dbConta=null;
    try{
          Hashtable tab=new Hashtable();
          tab.put("#cartella#", (String)util.getObjectField(dbr,"n_cartella", 'I'));
          tab.put("#operatore#",(String)util.getObjectField(dbr,"cogop", 'S')+" "+
                                 (String)util.getObjectField(dbr,"nomop", 'S'));
          tab.put("#assistito#", (String)util.getObjectField(dbr,"cognome", 'S')+" "+
                                 (String)util.getObjectField(dbr,"nome", 'S'));
          tab.put("#zona#", (String)dbr.get("descrizione_zona"));
          tab.put("#distretto#", (String)dbr.get("des_distr"));
          tab.put("#presidio#", (String)dbr.get("descrizione"));
          doc.writeSostituisci("tabella", tab);
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una FaiExcel()  ");
    }
}
}	// End of FoAssNoPato

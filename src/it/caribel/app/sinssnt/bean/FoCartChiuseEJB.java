package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 27/01/2005 - EJB di connessione alla procedura SINS Tabella FoCartChiuseEJB
//
// Ilaria Mancini
//
// ============================================================================

import java.io.*;
import java.sql.*;
import java.util.*;

import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.ndo.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;

public class FoCartChiuseEJB extends SINSSNTConnectionEJB {
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
public FoCartChiuseEJB() {}
String dom_res;
String dr;

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


public byte[] query_chiuse(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	byte[] jessy;

        String dataini="";
        String datafine="";
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

		if (par.get("data2") != null && !((String)par.get("data2")).equals(""))
			datafine=(String)par.get("data2");
		if (par.get("data1") != null && !((String)par.get("data1")).equals(""))
			dataini=(String)par.get("data1");

		String myselect = mkSelectAnalitica(dbc, par);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
                preparaLayout(doc,dbc,dataini,datafine,par);
                Hashtable hZona=new Hashtable();

                while (dbcur.next()){//caricamento dei dati
                   ISASRecord dbr= dbcur.getRecord();
                   if(((String)par.get("TYPE")).equals("PDF"))
                      hZona=AnalizzaZona(dbr,hZona,dbc);
                   else
                      FaiExcel(dbc, dbr, doc);
                }
                if(((String)par.get("TYPE")).equals("PDF")){
                  if (hZona.size()!=0)
                      StampaDaZona(hZona,doc,par,dbc);
                  else
                      doc.write("messaggio");
                }else  doc.write("fineTab");

                doc.write("finale");
                doc.close();
                dbcur.close();
                dbc.close();
                super.close(dbc);
                done=true;
                //System.out.println("Rit:"+new String(doc.get()));
                return (byte[])doc.get();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_elencoAss()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}

}

private void StampaDaZona(Hashtable hzona,mergeDocument doc,Hashtable par,ISASConnection dbc)
throws SQLException{

       try
        {
    Hashtable hConta=new Hashtable();
    Enumeration enumZona =orderedKeys( hzona);
    String raggruppamento = (String)par.get("ragg");
    while (enumZona.hasMoreElements())
    {
        String zona = "" + enumZona.nextElement();
        Hashtable hdistretti = (Hashtable)hzona.get(zona);
        Enumeration enumDistretti = orderedKeys(hdistretti);
        while (enumDistretti.hasMoreElements())
        {
              String distretto = "" + enumDistretti.nextElement();
              Hashtable hcomuni = (Hashtable) hdistretti.get(distretto);
              Enumeration enumComuni = orderedKeys(hcomuni);
              while (enumComuni.hasMoreElements())
              {
                 String comune = "" + enumComuni.nextElement();
                 Hashtable hTipologia = (Hashtable) hcomuni.get(comune);
                 Enumeration enumTipologia = orderedKeys(hTipologia);
                 while (enumTipologia.hasMoreElements())
                 {
                   String motivo = "" + enumTipologia.nextElement();
                   Vector vDati = (Vector) hTipologia.get(motivo);
                  Hashtable htab=new Hashtable();
                  if(zona.equals("NESSUNA DIVISIONE"))
                  {
                      htab.put("#zona#","");
                      htab.put("#distretto#","");
                      htab.put("#pca#","");
                      htab.put("#descrizione_zona#","");
                      htab.put("#des_distr#","");
                      htab.put("#descrizione#","");
                  }
                  else
                  {
                      htab.put("#zona#","Zona:");
                      htab.put("#distretto#","Distretto:");
                      htab.put("#descrizione_zona#",zona);
                      htab.put("#des_distr#",distretto);
                      String pca="";
//                      if (raggruppamento.equals("A"))
//                          pca="Area Distr.:";
//                      else if (raggruppamento.equals("C"))
//                           pca="Comune:";
                      
                      
                      
                      if(this.dom_res==null)
                   {
                   if (raggruppamento.equals("A"))
                	   pca="Area Distr.";
                   else if (raggruppamento.equals("C"))
                	   pca="Comune";
                   }else if (this.dom_res.equals("D"))
                   {
                       if (raggruppamento.equals("A"))
                    	   pca="Area Distr. di Domicilio";
                       else if (raggruppamento.equals("C"))
                    	   pca="Comune di Domicilio";
                       }else if (this.dom_res.equals("R"))
                       {
                           if (raggruppamento.equals("A"))
                        	   pca="Area Distr. di Residenza";
                           else if (raggruppamento.equals("C"))
                        	   pca="Comune di Residenza";
                           }  	
                      htab.put("#pca#",pca);
                      htab.put("#descrizione#",comune);
                  }
                  if (motivo.equals("NESSUNA DIVISIONE")){
                        htab.put("#motivo#","");
                        htab.put("#des_motivo#","");
                  }
                  else{ htab.put("#motivo#","Motivo Dimissione");
                        htab.put("#des_motivo#",decodMotivo(motivo));
                  }
                  doc.writeSostituisci("zona",htab);
                  doc.write("iniziotab");
                  Enumeration enumVett = vDati.elements();
                  while (enumVett.hasMoreElements())
                  {
                      Hashtable hDati=(Hashtable)enumVett.nextElement();
                      doc.writeSostituisci("tabella",hDati);
                  }//fine ciclo sul vettore
                  doc.write("finetab");
            }
              }//fine  ciclo comuni
        }//fine  ciclo distretti
    }//fine  ciclo zona
    hzona.clear();
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una StampaDaZona");
	}
}

private String mkSelectAnalitica(ISASConnection dbc, Hashtable par) {
        ServerUtility su = new ServerUtility();
        String zona="";
        String distretto="";
        String comune="";
        String tipologia="";
        String tipoStampa= (String)par.get("terr");
        StringTokenizer st = new StringTokenizer(tipoStampa,"|");
        String sZona=st.nextToken();
        String sDis=st.nextToken();
        String sCom=st.nextToken();
	String raggruppamento = (String)par.get("ragg");

        if(sZona.equals("1"))
           zona= " u.cod_zona,u.des_zona, ";
        else  zona= " 'NESSUNA DIVISIONE' cod_zona,'NESSUNA DIVISIONE' des_zona, ";

        if(sDis.equals("1"))
           distretto= " u.des_distretto,"+"u.cod_distretto"+ " as cod_distretto, ";
        else  distretto= " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto, ";

        if(sCom.equals("1"))
           comune= " u.codice ,u.descrizione ";
        else  comune= " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione ";


		// compongo la select da eseguire
		String myselect = "";
		myselect = su.addWhere(myselect, su.REL_AND, "data_chiusura", su.OP_GE_NUM,
			formatDate(dbc,(String)par.get("data1")));
		myselect = su.addWhere(myselect, su.REL_AND, "data_chiusura", su.OP_LE_NUM,
			formatDate(dbc,(String)par.get("data2")));
		String mot = (String)(par.get("motivo"));
		myselect = su.addWhere(myselect, su.REL_AND, "motivo_chiusura", su.OP_EQ_NUM,mot);
		if (! myselect.equals("")) myselect = " WHERE " + myselect;
		myselect = "SELECT cartella.n_cartella,cognome,nome,data_nasc,data_apertura,"+
		      "data_chiusura,motivo_chiusura,"+
		      zona+distretto+comune+
		      "FROM cartella,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u,anagra_c a "+myselect;

		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
			su.OP_EQ_STR, (String)par.get("zona"));
		myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
			su.OP_EQ_STR, (String)par.get("distretto"));

		myselect = su.addWhere(myselect, su.REL_AND, "u.codice",
			su.OP_EQ_STR, (String)par.get("pca"));

		raggruppamento = (String)par.get("ragg");
		myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR,raggruppamento);
//		if (raggruppamento!=null && raggruppamento.equals("C"))
//		  myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//			 " AND u.codice=a.dom_citta)"+
//			 " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//			 " AND u.codice=a.citta))";
//		else if (raggruppamento!=null && raggruppamento.equals("A"))
//		  myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//			 " AND u.codice=a.dom_areadis)"+
//			 " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//			 " AND u.codice=a.areadis))";
		
        //Aggiunto Controllo Domicilio/Residenza (BYSP)
        if(this.dom_res == null)
        {
                if (raggruppamento.equals("C"))
                  myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                        " AND u.codice=a.dom_citta)"+
        		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                        " AND u.codice=a.citta))";
                else if (raggruppamento.equals("A"))
                	myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                        " AND u.codice=a.dom_areadis)"+
        		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                        " AND u.codice=a.areadis))";
        }
        else if (this.dom_res.equals("D"))
                                  {
                                   if (raggruppamento.equals("C"))
                                	   myselect += " AND u.codice=a.dom_citta";
                                    else if (raggruppamento.equals("A"))
                                    	myselect += " AND u.codice=a.dom_areadis";
                                  }

        else if (this.dom_res.equals("R"))
                        {
                        if (raggruppamento.equals("C"))
                        	myselect += " AND u.codice=a.citta";
                else if (raggruppamento.equals("A"))
                	myselect += " AND u.codice=a.areadis";
                        }
		

                myselect +=  " AND cartella.n_cartella=a.n_cartella"+
			     " AND a.data_variazione IN (SELECT MAX(ac.data_variazione)"+
			     " FROM anagra_c ac WHERE ac.n_cartella=a.n_cartella"+
			     " AND ac.data_variazione<="+
			     formatDate(dbc,(String)par.get("data2"))+")"+
			     " ORDER BY data_chiusura DESC,cognome,nome";
		System.out.println("FoCartChiuseEJB.query_chiuse(): DEBUG ["+myselect+"].");

	return myselect;
}



private Hashtable AnalizzaZona(ISASRecord dbr,Hashtable hzona,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hdistretti=new Hashtable();
            if (dbr.get("des_zona")!=null && !((String)dbr.get("des_zona")).equals(""))
            {
                String  zona=(String)dbr.get("des_zona");
                  if (hzona!=null && ((Hashtable)hzona.get(zona))!=null)
                      hdistretti=(Hashtable)hzona.get(zona);
                hdistretti=AnalizzaDistretti(dbr,hdistretti,dbc);
                hzona.put(zona,hdistretti);
            }//fine descrizione distretti
            return hzona;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaZona");
	}
}
private Hashtable AnalizzaDistretti(ISASRecord dbr,Hashtable hdistretti,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hcomuni=new Hashtable();
            if (dbr.get("des_distretto")!=null && !((String)dbr.get("des_distretto")).equals(""))
            {
                String  distretto=(String)dbr.get("des_distretto");
                if (hdistretti!=null && ((Hashtable)hdistretti.get(distretto))!=null)
                      hcomuni=(Hashtable)hdistretti.get(distretto);
                hcomuni=AnalizzaComuni(dbr,hcomuni,dbc);
                hdistretti.put(distretto,hcomuni);
            }//fine descrizione distretti
            return hdistretti;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaDistretti");
	}
}

private Hashtable AnalizzaComuni(ISASRecord dbr,Hashtable hComuni,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hMotivo=new Hashtable();
            if (dbr.get("descrizione")!=null && !((String)dbr.get("descrizione")).equals(""))
            {
                String  comune=(String)dbr.get("descrizione");
                if (hComuni!=null && ((Hashtable)hComuni.get(comune))!=null)
                      hMotivo=(Hashtable)hComuni.get(comune);
                hMotivo=AnalizzaMotivo(dbr,hMotivo,dbc);
                hComuni.put(comune,hMotivo);
            }//fine descrizione comuni
            return hComuni;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaComuni");
	}
}

private String decodMotivo(String codice)throws
Exception {
	String ret = "";
	try{
		if(codice!=null){
			int cod = Integer.parseInt(codice);
		     	switch (cod){
			  case 1:
			      ret = "TRASFERIMENTO";
			      break;
			  case 2:
			      ret = "DECESSO";
			      break;
			}
		}
	}catch(Exception e){
	  System.out.println("Errore eseguendo una decodMotivo "+e);
	}
	return ret;
}

private Hashtable AnalizzaMotivo(ISASRecord dbr,Hashtable hMotivo,ISASConnection dbc)
throws SQLException
{
        try
        {
            Vector vDati=new Vector();
            if (dbr.get("motivo_chiusura")!=null)
            {
                String  motivo=""+dbr.get("motivo_chiusura");
                if (((Vector)hMotivo.get(motivo))!=null)
                    vDati=(Vector)hMotivo.get(motivo);
                vDati=caricaDati(dbc,dbr,vDati);
                hMotivo.put(motivo,vDati);
            }//fine descrizione assistito
            return hMotivo;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaMotivo");
	}
}

public  Vector caricaDati(ISASConnection dbc,ISASRecord dbr ,Vector vDati)
throws SQLException{
        try{
          Hashtable tab=new Hashtable();
          int cartella=0;
          if (dbr.get("n_cartella")!=null)
              cartella=((Integer)dbr.get("n_cartella")).intValue();
          tab.put("#n_cartella#","" + cartella);

          String cognome="";
          if (dbr.get("cognome")!=null && !((String)dbr.get("cognome")).equals(""))
                 cognome=(String)dbr.get("cognome");
          tab.put("#cognome#",cognome.trim());

          String nome="";
          if (dbr.get("nome")!=null && !((String)dbr.get("nome")).equals(""))
                  nome=(String)dbr.get("nome");
          tab.put("#nome#",nome.trim());

          String data_nasc="";
          if (dbr.get("data_nasc")!=null){
            data_nasc=((java.sql.Date)dbr.get("data_nasc")).toString();
            if (data_nasc.length()==10)
              data_nasc=data_nasc.substring(8,10)+"/"+
                    data_nasc.substring(5,7)+"/"+data_nasc.substring(0,4);
          }
          tab.put("#data_nasc#",data_nasc);

	  tab.put("#data_apertura#", formattaData((java.sql.Date)dbr.get("data_apertura")));
	  tab.put("#data_chiusura#", formattaData((java.sql.Date)dbr.get("data_chiusura")));
//	  tab.put("#motivo_chiusura#", decodMotivo(""+dbr.get("motivo_chiusura")));

          vDati.add(tab);
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}
}

private void FaiExcel(ISASConnection dbc, ISASRecord dbr, mergeDocument doc)
    throws Exception{
              Hashtable p = new Hashtable();
              if (dbr !=  null) {
                p.put("#zona#", ""+util.getObjectField(dbr,"des_zona",'S'));
                p.put("#distretto#", ""+util.getObjectField(dbr,"des_distretto",'S'));
                p.put("#comune#", ""+util.getObjectField(dbr,"descrizione",'S'));
                p.put("#cognome#", ""+util.getObjectField(dbr,"cognome",'S'));
                p.put("#nome#", ""+util.getObjectField(dbr,"nome",'S'));// 27/11/06
                p.put("#cartella#", ""+util.getObjectField(dbr,"n_cartella",'I'));
                p.put("#data_nasc#", ""+util.getObjectField(dbr,"data_nasc",'T'));
                p.put("#apertura#", ""+util.getObjectField(dbr,"data_apertura",'T'));
                p.put("#chiusura#", ""+util.getObjectField(dbr,"data_chiusura",'T'));
                String mot = (String)""+util.getObjectField(dbr,"motivo_chiusura",'I');
                if(!mot.equals("")){
                  if(mot.equals("1"))
                    p.put("#motivo#", "TRASFERITO");
                  else if(mot.equals("2"))
                    p.put("#motivo#", "DECEDUTO");
                  else
                    p.put("#motivo#", "NON SPECIFICATO");
                }else   p.put("#motivo#", "NON SPECIFICATO");
                doc.writeSostituisci("tabella", p);
              }
    }
private String formattaData(java.sql.Date dt){
	String data = "";
	if(dt!=null){
		data = dt.toString();
	 	if(data.length()==10){
			data = data.substring(8,10)+"/"+
				data.substring(5,7)+"/"+
				data.substring(0,4);
	 	}

	 }
	return data;
}


 public Enumeration orderedKeys(Hashtable hOrdinare)
 {
    Enumeration keys = hOrdinare.keys();
    Vector temp = new Vector();
    while( keys.hasMoreElements() )
    {
      temp.addElement("" + keys.nextElement() );
    }
     Collections.sort(temp);
     return temp.elements();
 }

private void preparaLayout(mergeDocument md, ISASConnection dbc,
      String data_inizio,String data_fine,Hashtable par) {
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
/*        String data=data_inizio.substring(8, 10)+"/"+
                  data_inizio.substring(5, 7)+"/"+data_inizio.substring(0, 4);
*/
        htxt.put("#data_inizio#",data_inizio);
/*        data=data_fine.substring(8, 10)+"/"+
                data_fine.substring(5, 7)+"/"+data_fine.substring(0, 4);
*/
        htxt.put("#data_fine#", data_fine);
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        md.writeSostituisci("layout",htxt);
}

private String DecodificaQualOp(ISASConnection dbc,String codice){
  String ret = "";
  try {
        String sel = "SELECT desc_qualif FROM operqual WHERE"+
          " cod_qualif='"+codice+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("desc_qualif")!=null)
          ret = (String)dbr.get("desc_qualif");
  } catch (Exception ex) {
        System.out.println("DecodificaQualOp"+ex);}
  return ret;
}
}	// End of FoCartChiuseEJB class

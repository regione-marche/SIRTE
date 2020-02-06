package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 27/01/2005 - EJB di connessione alla procedura SINS Tabella FoInfEleMotEJB
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

public class FoInfEleMotEJB extends SINSSNTConnectionEJB {

public FoInfEleMotEJB() {}

private String codice_usl="";
private String codice_regione="";
private String dom_res ="";
private String dr ="";


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


public byte[] query_inf(String utente, String passwd, Hashtable par,mergeDocument doc)
throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	byte[] jessy;

        String dataini="";
        String datafine="";
        String tipo="";
	boolean entrato=false;
	try{
	String type = par.get("TYPE")!=null?par.get("TYPE").toString():"PDF";
	this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);

		if (par.get("data_fine") != null && !((String)par.get("data_fine")).equals(""))
			datafine=(String)par.get("data_fine");
		if (par.get("data_inizio") != null && !((String)par.get("data_inizio")).equals(""))
			dataini=(String)par.get("data_inizio");

		String myselect = mkSelectAnalitica(dbc, par);
                System.out.println("SELECT "+ myselect);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
                preparaLayout(doc,dbc,dataini,datafine,par);
                Hashtable hZona=new Hashtable();
		if (type.equals("PDF"))
		{
		while (dbcur.next())
                {//caricamento dei dati
                   ISASRecord dbr= dbcur.getRecord();
                   hZona=AnalizzaZona(dbr,hZona,dbc);
                }
                if (hZona.size()!=0)
                    StampaDaZona(hZona,doc,par,dbc);
                else
                      doc.write("messaggio");
		}
		else if (dbcur!=null)
			mkSinteticaExcel(dbc,dbcur,par,doc);
			else doc.write("messaggio");
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



private void mkSinteticaExcel(ISASConnection dbc, ISASCursor dbcur, Hashtable par, mergeDocument doc)
    throws Exception{
            Hashtable p = new Hashtable();
            String ragg = faiRaggruppamento((String)par.get("ragg"));
           	p.put("#raggruppamento#", ragg);
			System.out.println("FoInfEleMotEJB: 2");
            doc.writeSostituisci("iniziotab", p);
			p.clear();
			String data_u="";
			String data_e="";
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
				p.put("#descrizione_zona#", dbr.get("des_zona").toString());
                p.put("#des_distr#", dbr.get("des_distretto").toString());
                p.put("#descrizione#",dbr.get("descrizione").toString());
			    p.put("#nome_ass#", (String)dbr.get("cognome")+" "+(String)dbr.get("nome"));
				p.put("#motivo_dim#",getMotivoUscita(dbc,dbr.get("ski_dimissioni").toString()));
		p.put("#data_nasc#", "("+((java.sql.Date)dbr.get("data_nasc"))+")");
		if (dbr.get("n_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
              p.put("#n_cartella#",((Integer)dbr.get("n_cartella")).toString());
              			System.out.println("FoInfEleMotEJB: 8");
		
		if (dbr.get("ski_data_uscita")!=null){
            data_u=""+((java.sql.Date)dbr.get("ski_data_uscita"));
            data_u=data_u.substring(8,10)+"/"+data_u.substring(5,7)+"/"+
                                          data_u.substring(0,4);
                p.put("#ski_data_uscita#",data_u);
          }else p.put("#ski_data_uscita#","");
          
          if (dbr.get("ski_data_apertura")!=null){
            data_e=""+((java.sql.Date)dbr.get("ski_data_apertura"));
            data_e=data_e.substring(8,10)+"/"+data_e.substring(5,7)+"/"+
                                          data_e.substring(0,4);
                p.put("#ski_data_apertura#",data_e);
          }else p.put("#ski_data_apertura#","");
			 int gg = CalcoloGiorni(dbc, dbr, data_e, data_u);
          p.put("#gg#",""+gg);
		  
		  
				doc.writeSostituisci("inizio_riga",p);
				p.clear();
            }
            doc.write("finetab");
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
                      if (raggruppamento.equals("A"))
                          pca="Area Distr.:";
                      else if (raggruppamento.equals("C"))
                           pca="Comune:";
                      else if (raggruppamento.equals("P"))
                          pca="Presidio:";
                      htab.put("#pca#",pca);
                      htab.put("#descrizione#",comune);
                  }
                  if (motivo.equals("NESSUNA DIVISIONE")){
                        htab.put("#motivo#","");
                        htab.put("#des_motivo#","");
                  }
                  else{ htab.put("#motivo#","Motivo Dimissione");
                        htab.put("#des_motivo#",getMotivoUscita(dbc,motivo));
                  }
                  doc.writeSostituisci("zona",htab);
                  doc.write("iniziotab");
                  Enumeration enumVett = vDati.elements();
                  while (enumVett.hasMoreElements())
                  {
                      Hashtable hDati=(Hashtable)enumVett.nextElement();
  //                    hContaGenerale.put(""+hDati.get("#cartella#"),"");
//                      hContaOperatore.put(""+hDati.get("#cartella#"),"");
//                      hContaRagg.put(""+hDati.get("#cartella#"),"");
                      doc.writeSostituisci("tabella",hDati);
                  }//fine ciclo sul vettore
                  doc.write("finetab");
            }
/*     hConta.put("#descrizione#","Totale numero assistiti:");
       hConta.put("#totale#",""+contaxZona);
       doc.writeSostituisci("totale",hConta);
*/

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
        String sUte=st.nextToken();

        if(sZona.equals("1"))
           zona= " u.cod_zona,u.des_zona, ";
        else  zona= " 'NESSUNA DIVISIONE' cod_zona,'NESSUNA DIVISIONE' des_zona, ";

        if(sDis.equals("1"))
           distretto= " u.des_distretto,u.cod_distretto, ";
        else  distretto= " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto, ";

        if(sCom.equals("1"))
           comune= " u.codice ,u.descrizione ,";
        else  comune= " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione, ";

        if(sUte.equals("1"))
           tipologia= " s.ski_dimissioni,";
        else  tipologia= " 'NESSUNA DIVISIONE' ski_dimissioni ,";

        String myselect= "SELECT c.n_cartella, "+
                      zona +
                      distretto+
                      comune+
		" c.cognome, c.nome, c.data_nasc, c.cod_com_nasc,"+
               tipologia+
		" ski_data_apertura,ski_data_uscita"+
		" FROM cartella c, skinf s ,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u ,anagra_c a WHERE"+
		" s.n_cartella=c.n_cartella AND a.n_cartella=c.n_cartella"+
		" AND a.data_variazione IN (SELECT MAX (data_variazione) FROM anagra_c"+
                " WHERE a.n_cartella=anagra_c.n_cartella)";

        if (par.get("data_fine") != null)
		myselect = myselect+
			" AND s.ski_data_apertura<="+
			formatDate(dbc,(String)par.get("data_fine"))+
			" AND s.ski_data_uscita<="+
			formatDate(dbc,(String)par.get("data_fine"));

        if (par.get("data_inizio") != null)
		myselect = myselect+" AND s.ski_data_uscita>="+
			formatDate(dbc,(String)par.get("data_inizio"));

         String ragg = (String)par.get("ragg");
	myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	myselect = su.addWhere(myselect, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));
    /*    if (ragg.equals("C"))
          myselect += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                " AND u.codice=a.dom_citta)"+
		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                " AND u.codice=a.citta))";
        else if (ragg.equals("A"))
          myselect += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                " AND u.codice=a.dom_areadis)"+
		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                " AND u.codice=a.areadis))";*/
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
                
                else if (ragg!=null && ragg.equals("P"))
                	myselect += " AND u.codice=s.ski_cod_presidio ";
        }
        else if (((String)par.get("dom_res")).equals("D"))
                                  {
                                   if (ragg.equals("C"))
                  myselect += " AND u.codice=a.dom_citta";
                                    else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.dom_areadis";
                                   
                                    else if (ragg.equals("P"))
                  myselect += " AND u.codice=s.ski_cod_presidio ";
                                  }

        else if (((String)par.get("dom_res")).equals("R"))
                        {
                        if (ragg.equals("C"))
                  myselect += " AND u.codice=a.citta";
                else if (ragg.equals("A"))
                  myselect += " AND u.codice=a.areadis";
                        }

        String assis="";
        if (par.get("ass")!=null)
                assis=(String)par.get("ass");
        if (!assis.equals("E"))
                myselect=myselect+" AND ski_modalita='"+assis+"'";

	String motivo="";
        if (par.get("motivo") != null && !((String)par.get("motivo")).equals("0")){
          motivo=(String)(par.get("motivo"));
          if (!motivo.equals(""))
                myselect=myselect+" AND s.ski_dimissioni='"+motivo+"'";
        }
        myselect+=" ORDER BY s.ski_dimissioni,c.cognome,c.n_cartella";
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

private String getMotivoUscita(ISASConnection dbc,String codice)
throws SQLException{
        String decod="";
        try {
                 //String codice=""+ dbr.get("ski_dimissioni");
                 String sel="SELECT tab_descrizione FROM tab_voci "+
                            " WHERE tab_cod='ICHIUS' AND tab_val='"+codice +"'";
                 //System.out.println("MOTIVO CHIUSURA-->"+sel);
                 ISASRecord dbDecod=dbc.readRecord(sel);
                 if (dbDecod!=null && dbDecod.get("tab_descrizione")!=null)
                   {
                     decod=(String)dbDecod.get("tab_descrizione");
                   }
		 else
		   decod = "NON SPECIFICATO"; //gb 01/08/07
          return decod;
	} catch(Exception e) {
		debugMessage("FoInfEleAssEJB.getMotivoUscita(): "+e);
		throw new SQLException("Errore eseguendo getMotivoUscita()");
	}
}

private Hashtable AnalizzaMotivo(ISASRecord dbr,Hashtable hMotivo,ISASConnection dbc)
throws SQLException
{
	String  motivo="";
        try
        {
              Vector vDati=new Vector();
            if (dbr.get("ski_dimissioni")!=null && !((String)dbr.get("ski_dimissioni")).equals(""))
               {
                motivo=(String)dbr.get("ski_dimissioni");
/*gb 01/08/07 *******
                if (((Vector)hMotivo.get(motivo))!=null)
                    vDati=(Vector)hMotivo.get(motivo);
                vDati=caricaDati(dbc,dbr,vDati);
                hMotivo.put(motivo,vDati);
*gb 01/08/07: fine *******/
               }//fine descrizione assistito
	    else
	      {
		motivo = "100"; //gb 01/08/07: Codice dimissioni non esistente nella tabella di decodifica
				//	       serve solo per far scrivere 'NON SPECIFICATO'
				//	       in fase di decodifica durante la Stampa.
	      }
//gb 01/08/07 *******
            if (((Vector)hMotivo.get(motivo))!=null)
              vDati=(Vector)hMotivo.get(motivo);
            vDati=caricaDati(dbc,dbr,vDati);
            hMotivo.put(motivo,vDati);
//gb 01/08/07: fine *******

            return hMotivo;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaMotivo");
	}
}

private Hashtable PulisciCampi(Hashtable hDati)
{//questa funzione sbianca i campi dell'assistito gi� stampato
    hDati.put("#cartella#","");
    hDati.put("#assistito#","");
  //  hDati.put("#cod_com_nasc#","");
    hDati.put("#data_nasc#","");
    return hDati;
}

public  Vector caricaDati(ISASConnection dbc,ISASRecord dbr ,Vector vDati)
throws SQLException{
        try{
          Hashtable tab=new Hashtable();
          int cartella=0;
          if (dbr.get("n_cartella")!=null)
              cartella=((Integer)dbr.get("n_cartella")).intValue();
          tab.put("#cartella#","" + cartella);

          String cognome="";
          if (dbr.get("cognome")!=null && !((String)dbr.get("cognome")).equals(""))
                 cognome=(String)dbr.get("cognome");

          String nome="";
          if (dbr.get("nome")!=null && !((String)dbr.get("nome")).equals(""))
                  nome=(String)dbr.get("nome");
          tab.put("#assistito#",cognome.trim()+" " + nome.trim());

         /* tab.put("#comune_nasc#",decodifica("comuni","codice",
                            (String)dbr.get("cod_com_nasc"),"descrizione",dbc));
          */
          String data_u="";
          if (dbr.get("ski_data_uscita")!=null){
            data_u=""+((java.sql.Date)dbr.get("ski_data_uscita"));
            data_u=data_u.substring(8,10)+"/"+data_u.substring(5,7)+"/"+
                                          data_u.substring(0,4);
                tab.put("#ski_data_uscita#",data_u);
          }else tab.put("#ski_data_uscita#","");
          String data_e="";
          if (dbr.get("ski_data_apertura")!=null){
            data_e=""+((java.sql.Date)dbr.get("ski_data_apertura"));
            data_e=data_e.substring(8,10)+"/"+data_e.substring(5,7)+"/"+
                                          data_e.substring(0,4);
                tab.put("#ski_data_apertura#",data_e);
          }else tab.put("#ski_data_apertura#","");
          String data_nasc="";

          if (dbr.get("data_nasc")!=null){
            data_nasc=((java.sql.Date)dbr.get("data_nasc")).toString();
            if (data_nasc.length()==10)
              data_nasc=data_nasc.substring(8,10)+"/"+
                    data_nasc.substring(5,7)+"/"+data_nasc.substring(0,4);
          }
          tab.put("#data_nasc#",data_nasc);
          int gg = CalcoloGiorni(dbc, dbr, data_e, data_u);
          tab.put("#gg#",""+gg);
          vDati.add(tab);
          return vDati;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
	}
}

private int CalcoloGiorni(ISASConnection dbc, ISASRecord dbr, String data_e, String data_u)
throws SQLException{
  try{
    //CALCOLO IL NUMERO DI GIORNI FRA LE DUE DATE
    String ausi1=data_u.substring(0,2)+data_u.substring(3,5)+data_u.substring(6,10);
    DataWI dataxfunz = new DataWI(ausi1);
    String ausi2=data_e.substring(6,10)+data_e.substring(3,5)+data_e.substring(0,2);
    int giorni=dataxfunz.contaGgOggiValeUno(ausi2);
    return giorni;
  }catch(Exception e){
    e.printStackTrace();
    throw new SQLException("Errore eseguendo una CalcoloGiorni()");
  }
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

private void preparaLayout(mergeDocument md, ISASConnection dbc,String data_inizio,String data_fine,Hashtable par) {
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
        String data=data_inizio.substring(8, 10)+"/"+
                  data_inizio.substring(5, 7)+"/"+data_inizio.substring(0, 4);
        htxt.put("#data_inizio#",data);
        data=data_fine.substring(8, 10)+"/"+
                data_fine.substring(5, 7)+"/"+data_fine.substring(0, 4);
        htxt.put("#data_fine#", data);
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
        String ass= (String)par.get("ass");
        if (ass.equals("1"))
          htxt.put("#assistenza#", "ASSISTENZA DOMICILIARE");
        else if (ass.equals("2"))
          htxt.put("#assistenza#", "ASSISTENZA AMBULATORIALE");
        else  htxt.put("#assistenza#", "");
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

private String faiRaggruppamento(String tipo) {
	String raggruppa="";
	if(this.dom_res==null)
    {
    if (tipo.trim().equals("A"))
    	raggruppa="Area Distr.";
    else if (tipo.trim().equals("C"))
    	raggruppa="Comune";
    else if(tipo.trim().equals("P"))
		raggruppa=" Presidio ";
    else	raggruppa="TIPO NON VALIDO";
    }else if (this.dom_res.equals("D"))
    {
        if (tipo.trim().equals("A"))
        	raggruppa="Area Distr. di Domicilio";
        else if (tipo.trim().equals("C"))
        	raggruppa="Comune di Domicilio";
        else	raggruppa="TIPO NON VALIDO";
        }else if (this.dom_res.equals("R"))
        {
            if (tipo.trim().equals("A"))
            	raggruppa="Area Distr. di Residenza";
            else if (tipo.trim().equals("C"))
            	raggruppa="Comune di Residenza";
            else	raggruppa="TIPO NON VALIDO";
            }  	
//	if(tipo.trim().equals("C"))
//		raggruppa=" Comune ";
//	else if(tipo.equals("A"))
//		raggruppa=" Area ";
	
	return raggruppa;
}
}	// End of FoInfEleMotEJB class

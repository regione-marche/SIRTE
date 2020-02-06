package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 27/01/2005 - EJB di connessione alla procedura SINS Tabella FoDettOrePrestEJB
//
// Ilaria Mancini
//
// 02/05/07 m.: aggiunto totali per suddivisione territoriale. 
//	I totali vengono stampati se il corrispondente livello (=zona, distr, ..., tipo utente) 
//	� presente pi� volte. Il totale generale viene stampato sempre.
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

public class FoDettOrePrestEJB extends SINSSNTConnectionEJB {

public FoDettOrePrestEJB() {}
//hash per calcolare il numero totale di assistiti
//Hashtable hContaGenerale= new Hashtable();

String dom_res;
String dr;
private String codice_usl="";
private String codice_regione="";

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

public int ConvertData (String dataold,String datanew)
{
        //inizializzazione della variabile eta
        int eta=0;
        int tempeta=0;
        //preparazione primo array
        int[] datavecchia= new int[3];
        Integer giorno = new Integer(dataold.substring(8,10));
        datavecchia[0]= giorno.intValue();
	Integer mese = new Integer(dataold.substring(5,7));
        datavecchia[1]= mese.intValue();
        Integer anno = new Integer(dataold.substring(0,4));
         datavecchia[2]= anno.intValue();

        //preparazione secondo array

        int[] datanuova= new int[3];
        Integer day = new Integer(datanew.substring(8,10));
        datanuova[0]= day.intValue();
        Integer mounth = new Integer(datanew.substring(5,7));
        datanuova[1]= mounth.intValue();
         Integer year = new Integer(datanew.substring(0,4));
        datanuova[2]= year.intValue();

        tempeta= datanuova[2]-datavecchia[2];
        //confronto mese
        if (datanuova[1] < datavecchia[1])
                tempeta=tempeta-1;      // anni non ancora compiuti
        else if (datanuova[1] == datavecchia[1])
                if (datanuova[0] < datavecchia[0])      //confronto giorno
                        tempeta=tempeta-1;      // anni non ancora compiuti
        eta=tempeta;
        return eta;
}
private int convNumDBToInt(String nomeCampo, ISASRecord mydbr)throws Exception
{
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

public static String getjdbcDate()
{
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
        new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}


/*-----------------------------------------------------------------------------------------
Parte riguardante la seconda scelta senza NDO
//------------------------------------------------------------------------------------------*/

public byte[] query_dettore(String utente, String passwd, Hashtable par,mergeDocument doc)
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
                Hashtable hConta=new Hashtable();
		String myselect = mkSelectEleAss(dbc, par);
	      	ISASCursor dbcur=dbc.startCursor(myselect);
		if (par.get("data_fine") != null && !((String)par.get("data_fine")).equals(""))
			datafine=(String)par.get("data_fine");
		if (par.get("data_inizio") != null && !((String)par.get("data_inizio")).equals(""))
			dataini=(String)par.get("data_inizio");

                preparaLayoutEleAss(doc,dbc,dataini,datafine,par);
                Hashtable hZona=new Hashtable();
		while (dbcur.next())
                {//caricamento dei dati
                   ISASRecord dbr= dbcur.getRecord();
                   hZona=AnalizzaZona(dbr,hZona,dbc);
                }
                if (hZona.size()!=0)
                {
                    StampaDaZona(hZona,doc,par,dbc);
/*	            hConta.put("#descrizione#","Totale generale numero assistiti:");
        	    hConta.put("#totale#",""+hContaGenerale.size());
	            doc.writeSostituisci("totale",hConta);
                    doc.write("break");*/
                }
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
private void StampaDaZona(Hashtable hzona,mergeDocument doc,Hashtable par,ISASConnection dbc) throws Exception
{
    Hashtable hConta=new Hashtable();

	// 02/05/07 totali di colonna ---
	Totali tot_tipo = new Totali();
	Totali tot_pca = new Totali();
	Totali tot_dist = new Totali();
	Totali tot_zona = new Totali();
	Totali tot_gene = new Totali();
	Hashtable h_xtit = new Hashtable();

    Hashtable hContaxTipo = new Hashtable();
    Hashtable hContaxPca = new Hashtable();
    Hashtable hContaxDist = new Hashtable();
    Hashtable hContaxZona = new Hashtable();
	// 02/05/07 totali di colonna ---

    Enumeration enumZona =orderedKeys( hzona);
    String raggruppamento = (String)par.get("ragg");
	boolean isTotxZonaDaStampare = (hzona.size() > 1); // 02/05/07
    while (enumZona.hasMoreElements())
    {
        String zona = "" + enumZona.nextElement();
        Hashtable hdistretti = (Hashtable)hzona.get(zona);
        Enumeration enumDistretti = orderedKeys(hdistretti);
		boolean isTotxDistDaStampare = (hdistretti.size() > 1); // 02/05/07
        while (enumDistretti.hasMoreElements())
        {
            String distretto = "" + enumDistretti.nextElement();
            Hashtable hcomuni = (Hashtable) hdistretti.get(distretto);
            Enumeration enumComuni = orderedKeys(hcomuni);
			boolean isTotxPcaDaStampare = (hcomuni.size() > 1); // 02/05/07
            while (enumComuni.hasMoreElements())
            {
                String comune = "" + enumComuni.nextElement();
                Hashtable hTipologia = (Hashtable) hcomuni.get(comune);
                Enumeration enumTipologia = orderedKeys(hTipologia);
				String pca="";
				boolean isTotxTipoDaStampare = (hTipologia.size() > 1); // 02/05/07
                while (enumTipologia.hasMoreElements())
                {
                	String tipolo = "" + enumTipologia.nextElement();
                  	Hashtable htab=new Hashtable();
	                htab.put("#descrizione_zona#",zona);
                  	htab.put("#des_distr#",distretto);

                  	if (raggruppamento.equals("A"))
                    	pca="Area Distr.";
                  	else if (raggruppamento.equals("C"))
                       	pca="Comune";
                  	else if (raggruppamento.equals("P"))
                      	pca="Presidio";
                  	htab.put("#pca#",pca);
                  	htab.put("#descrizione#",comune);
					String descTipo = "";
                  	if (tipolo.equals("NESSUNA DIVISIONE")){
                        htab.put("#tipologia#","");
                        htab.put("#tipo_ute#","");
                  	}else{ 
						htab.put("#tipologia#","Tipologia");
                        String figprof="";
                        if(!par.get("figprof").equals("00"))
                        	figprof=(String)par.get("figprof");
                        if (figprof.equals("01")) {
							descTipo = DecodificaTipoUtenteSOC(dbc,tipolo);
                            htab.put("#tipo_ute#", descTipo);
                        } else {
							descTipo = DecodificaTipoUtenteINF(dbc,tipolo);
                            htab.put("#tipo_ute#", descTipo);
						}
                  	}
                  	doc.writeSostituisci("zona",htab);

                  	doc.write("iniziotab");
                  	Hashtable hOperatore = (Hashtable) hTipologia.get(tipolo);
                  	Enumeration enumOperatore = orderedKeys(hOperatore);
                  	while (enumOperatore.hasMoreElements())
	               	{
                    	String operatore =""+enumOperatore.nextElement();
                      	Hashtable hAssistito = (Hashtable) hOperatore.get(operatore);
                      	Enumeration enumAssistito = orderedKeys(hAssistito);
                      	boolean primaVolta =true;
                      	while (enumAssistito.hasMoreElements())
                      	{ 
							String ass =""+enumAssistito.nextElement();
                        	Hashtable hDati=(Hashtable) hAssistito.get(ass);
                        	caricaTotxTipo(hDati, tot_tipo); // 02/05/07
							hDati=CaricaCampi(hDati);
                        	if (!primaVolta)
                            	hDati.put("#operatore#","");
                        	else
                              	primaVolta=false;
                        	doc.writeSostituisci("tabella",hDati);
							// 02/05/07 CONTATORI ASSISTITI---
							hContaxTipo.put(ass, "-");
							hContaxPca.put(ass, "-");
							hContaxDist.put(ass, "-");
							hContaxZona.put(ass, "-");
							hConta.put(ass, "-");
							// 02/05/07 ---
                      	}// fine ciclo assistito
                  	}// fine ciclo operatore

                  	doc.write("finetab");

					// 02/05/07 TOTALI X TIPOLOGIA ---
					if (isTotxTipoDaStampare) 
						stampaTot(doc, "tipologia " + descTipo, tot_tipo, hContaxTipo.size());	
					hContaxTipo.clear();
					tot_pca.add(tot_tipo);
					tot_tipo.setDefault();
					// 02/05/07 ----------------------
            	}//fine ciclo tipologia
/*     			hConta.put("#descrizione#","Totale numero assistiti:");
       			hConta.put("#totale#",""+contaxZona);
       			doc.writeSostituisci("totale",hConta);
*/

				// 02/05/07 TOTALI X COMUNE---
				if (isTotxPcaDaStampare) {
					doc.write("rigaSpazio");
					stampaTot(doc, pca + ": " + comune, tot_pca, hContaxPca.size());	
				}
				hContaxPca.clear();
				tot_dist.add(tot_pca);
				tot_pca.setDefault();
				// 02/05/07 ------------------
            }//fine  ciclo comuni

			// 02/05/07 TOTALI X DISTRETTO ---
			if (isTotxDistDaStampare){
				doc.write("rigaSpazio");
				stampaTot(doc, "distretto: " + distretto, tot_dist, hContaxDist.size());	
			}
			hContaxDist.clear();
			tot_zona.add(tot_dist);
			tot_dist.setDefault();
			// 02/05/07 ----------------------
        }//fine  ciclo distretti

		// 02/05/07 TOTALI X ZONA ---
		if (isTotxZonaDaStampare){
			doc.write("rigaSpazio");
			stampaTot(doc, "zona: " + zona, tot_zona, hContaxZona.size());	
		}
		hContaxZona.clear();
		tot_gene.add(tot_zona);
		tot_zona.setDefault();
		// 02/05/07 -----------------
    }//fine  ciclo zona

    hzona.clear();

	// 02/05/07 TOTALI GENERALI ----
	doc.write("rigaSpazio");
	doc.write("rigaSpazio");
	doc.write("rigaSpazio");
	stampaTot(doc, "la selezione", tot_gene, hConta.size());	
	hConta.clear();
	tot_gene.setDefault();
	// 02/05/07 --------------------
}

private String ConvertiOre(int ore)
{
  String tot="";
   if(ore<60){
         tot="0."+ore;
   }else if (ore>=60){
          int inhash=ore/60;
          int aiuto=ore-(inhash*60);
          tot=inhash+"."+aiuto;
   }
   return tot;
}

private Hashtable CaricaCampi(Hashtable hDati) throws Exception
{
	NumberDateFormat ndf = new NumberDateFormat(); // 02/05/07

    String sminuti=""+hDati.get("#tempo#");

    int iMinuti=Integer.parseInt(sminuti);
// 02/05/07    sminuti=ConvertiOre(iMinuti)+"h";
	sminuti = ndf.getHhMmTime(sminuti); // 02/05/07

    hDati.put("#tempo#",sminuti);

    String sNumPrest=""+hDati.get("#num_prest#");
    int iNumPrest=Integer.parseInt(sNumPrest);

    int media =iMinuti/iNumPrest;
// 02/05/07  sminuti=ConvertiOre(media)+"h";
	sminuti = ndf.getHhMmTime("" + media); // 02/05/07
    hDati.put("#media#",sminuti);

    return hDati;
}
/*
private String mkSelectEleAssInf(ISASConnection dbc, Hashtable par){
       ServerUtility su =new ServerUtility();
       String tipo_utente= (String)par.get("ute");

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
           tipologia= "ski.tipout  tipo_utente ";
        else  tipologia= " 'NESSUNA DIVISIONE' tipo_utente ";

     String s="SELECT distinct  c.n_cartella,"+
               zona +
              distretto+
              comune+
              " nvl(trim(c.cognome),'') ||' ' || nvl(trim(c.nome),'') assistito," +
              " i.int_tempo,int_anno,int_contatore,"  +
              " int_cod_oper cod_operatore,"+
              " nvl(trim(o.cognome),'') ||' ' || nvl(trim(o.nome),'') operatore," +
              tipologia+
              " FROM cartella c, operatori o"+
              " ,ubicazioni_n u ,interv i";
              s=s+",skinf cont ";
              s=s+ " WHERE  i.int_cod_oper=o.codice"+
//              " AND i.int_tipo_oper='01'"+
              " AND i.int_cartella=c.n_cartella "+
              " AND cont.n_cartella=c.n_cartella "+
              " AND cont.n_cartella=i.int_cartella "+
              " AND cont.n_contatto=i.int_contatto ";

              //filtro per la parte territoriale
              String ragg = (String)par.get("ragg");
              s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
              s = su.addWhere(s, su.REL_AND, "u.cod_zona",
                      su.OP_EQ_STR, (String)par.get("zona"));
              s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
                      su.OP_EQ_STR, (String)par.get("distretto"));
              s = su.addWhere(s, su.REL_AND, "u.codice",
                      su.OP_EQ_STR, (String)par.get("pca"));

              if (ragg.equals("A"))
                s += " AND i.int_cod_areadis=u.codice ";
              else if ( ragg.equals("P"))
                s += " AND u.codice=i.int_codpres ";
              else if ( ragg.equals("C"))
                s += " AND u.codice=i.int_cod_comune ";

              //filtro la qualifica
/*             String qualifica= (String)par.get("qual");
             s = su.addWhere(s, su.REL_AND, "o.cod_qualif",
                su.OP_EQ_STR, qualifica);
*/

/*		//filtro la figura professionale
	        String figprof="";
        	if(!par.get("figprof").equals("00"))
                	figprof=(String)par.get("figprof");

	        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
        	        su.OP_EQ_STR, figprof);

            //filtro la tipo utente
           s = su.addWhere(s, su.REL_AND, "cont.ski_tiput",
              su.OP_EQ_STR, tipo_utente);

            //controllo sulle date
            s = su.addWhere(s, su.REL_AND, "i.int_data_prest",su.OP_LE_NUM,
                      formatDate(dbc, (String)par.get("data_fine")));
              s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
                      su.OP_GE_NUM,
             formatDate(dbc, (String)par.get("data_inizio")));

            //filtro tipo prestazione
             String tipo_prest="";
              if(!par.get("tipopz").equals("E")){
                tipo_prest=(String)par.get("tipopz");
                s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, tipo_prest);
              }
                         debugMessage("FoDettOrePrestEJB.mkSelectEleAssInf(): "+s);
	return s;
}
*/
private String mkSelectEleAss(ISASConnection dbc, Hashtable par){
       ServerUtility su =new ServerUtility();
       String tipo_utente= (String)par.get("ute");

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
           distretto= " u.des_distretto,"+"u.cod_distretto"+" as cod_distretto, ";
        else  distretto= " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto, ";

        if(sCom.equals("1"))
           comune= " u.codice ,u.descrizione ,";
        else  comune= " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione, ";

        String figprof="";
        if(!par.get("figprof").equals("00"))
                figprof=(String)par.get("figprof");

        if(sUte.equals("1"))
        {
            if (figprof.equals("01"))
//gb 26/06/07               tipologia= " cont.tipo_utente ";
               tipologia= " cont.ap_tipo_utente tipo_utente"; //gb 26/06/07
            else
               tipologia= " cont.ski_tipout tipo_utente ";
        }
        else  tipologia= " 'NESSUNA DIVISIONE' tipo_utente ";

     String s="SELECT distinct  c.n_cartella,"+
               zona +
              distretto+
              comune+
              " nvl(trim(c.cognome),'') ||' ' || nvl(trim(c.nome),'') assistito," +
              " i.int_tempo,int_anno,int_contatore,"  +
              " int_cod_oper cod_operatore,"+
              " nvl(trim(o.cognome),'') ||' ' || nvl(trim(o.nome),'') operatore," +
              tipologia+
              " FROM cartella c, operatori o"+
              " ,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u ,interv i";
              if (figprof.equals("01"))
//gb 26/06/07                s=s+",contatti cont ";
                s=s+",ass_progetto cont "; //gb 26/06/07
              else
                s=s+",skinf cont ";

              s=s+ " WHERE  i.int_cod_oper=o.codice"+
//              " AND i.int_tipo_oper='01'"+
              " AND i.int_cartella=c.n_cartella "+
              " AND cont.n_cartella=c.n_cartella "+
              " AND cont.n_cartella=i.int_cartella ";
//gb 26/06/07              " AND cont.n_contatto=i.int_contatto ";
//gb 26/06/07 *******
              if (figprof.equals("01"))
                 s = s + " AND cont.n_progetto = i.n_progetto ";
	      else
	         s = s + " AND cont.n_contatto = i.int_contatto ";
//gb 26/06/07: fine *******
              
            //Minerba 06/03/2013		
       	   String qualifica = (String) par.get("qualifica");
       		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
       			s +=" AND i.int_qual_oper='"+qualifica+"'";
       		}//fine Minerba 06/03/2013

              //filtro per la parte territoriale
              String ragg = (String)par.get("ragg");
              s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
              s = su.addWhere(s, su.REL_AND, "u.cod_zona",
                      su.OP_EQ_STR, (String)par.get("zona"));
              s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
                      su.OP_EQ_STR, (String)par.get("distretto"));
              s = su.addWhere(s, su.REL_AND, "u.codice",
                      su.OP_EQ_STR, (String)par.get("pca"));

//              if (ragg.equals("A"))
//                s += " AND i.int_cod_areadis=u.codice ";
//              else if ( ragg.equals("P"))
//                s += " AND u.codice=i.int_codpres ";
//              else if ( ragg.equals("C"))
//                s += " AND u.codice=i.int_cod_comune ";

              if(this.dom_res==null)
              {
              	
                  if (ragg.equals("A"))
                      s += " AND i.int_cod_areadis=u.codice ";
                    else if ( ragg.equals("P"))
                      s += " AND u.codice=i.int_codpres ";
                    else if ( ragg.equals("C"))
                      s += " AND u.codice=i.int_cod_comune ";

              }
              else if (this.dom_res.equals("D"))
              		{
            	  if (ragg.equals("A"))
                      s += " AND i.int_cod_areadis=u.codice ";

                    else if ( ragg.equals("C"))
                      s += " AND u.codice=i.int_cod_comune ";
                    }
              else if (this.dom_res.equals("R"))
              		{
            	  if (ragg.equals("A"))
                      s += " AND i.int_cod_areadis=u.codice ";
                   
                    else if ( ragg.equals("C"))
                      s += " AND u.codice=i.int_cod_comune ";

              		}
              
              //filtro la qualifica
/*             String qualifica= (String)par.get("qual");
             s = su.addWhere(s, su.REL_AND, "o.cod_qualif",
                su.OP_EQ_STR, qualifica);
*/

		//filtro la figura professionale
/*	        String figprof="";
        	if(!par.get("figprof").equals("00"))
                	figprof=(String)par.get("figprof");
*/
	        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
        	        su.OP_EQ_STR, figprof);

            //filtro la tipo utente
            if (figprof.equals("01"))
               s = su.addWhere(s, su.REL_AND, "cont.ap_tipo_utente",
                      su.OP_EQ_STR, tipo_utente);
            else
                s = su.addWhere(s, su.REL_AND, "cont.ski_tipout",
                            su.OP_EQ_STR, tipo_utente);

            //controllo sulle date
            s = su.addWhere(s, su.REL_AND, "i.int_data_prest",su.OP_LE_NUM,
                      formatDate(dbc, (String)par.get("data_fine")));
              s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
                      su.OP_GE_NUM,
             formatDate(dbc, (String)par.get("data_inizio")));

            //filtro tipo prestazione
             String tipo_prest="";
              if(!par.get("tipopz").equals("E")){
                tipo_prest=(String)par.get("tipopz");
                s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, tipo_prest);
              }
                         debugMessage("FoDettOrePrestEJB.mkSelectEleAss(): "+s);
	return s;
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
            Hashtable hTipologia=new Hashtable();
            if (dbr.get("descrizione")!=null && !((String)dbr.get("descrizione")).equals(""))
            {
                String  comune=(String)dbr.get("descrizione");
                if (hComuni!=null && ((Hashtable)hComuni.get(comune))!=null)
                      hTipologia=(Hashtable)hComuni.get(comune);
                hTipologia=AnalizzaTipologia(dbr,hTipologia,dbc);
                hComuni.put(comune,hTipologia);
            }//fine descrizione comuni
            return hComuni;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaComuni");
	}
}

private Hashtable AnalizzaTipologia(ISASRecord dbr,Hashtable hTipologia,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hOperatore=new Hashtable();
            //la tipologia non � sempre specificata
            String  tipologia="NON SPECIFICATA";
            if (dbr.get("tipo_utente")!=null && !((String)dbr.get("tipo_utente")).equals(""))
            {
               tipologia=""+dbr.get("tipo_utente");
            }
            if (hTipologia!=null && ((Hashtable)hTipologia.get(tipologia))!=null)
            	hOperatore=(Hashtable)hTipologia.get(tipologia);
            hOperatore=AnalizzaOperatore(dbr,hOperatore,dbc);
            hTipologia.put(tipologia,hOperatore);
            //}//fine descrizione assistito
            return hTipologia;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaTipologia");
	}
}

private Hashtable AnalizzaOperatore(ISASRecord dbr,Hashtable hOperatore,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hAssistito=new Hashtable();
            if (dbr.get("cod_operatore")!=null && !((String)dbr.get("cod_operatore")).equals(""))
            {
               String operatore="";
               if (dbr.get("operatore")!=null && !((String)dbr.get("operatore")).equals(""))
                    operatore=(String)dbr.get("operatore");
                String  cartella=operatore+"|"+dbr.get("cod_operatore");
                if (hOperatore!=null && ((Hashtable)hOperatore.get(operatore))!=null)
                      hAssistito=(Hashtable)hOperatore.get(operatore);
                hAssistito=AnalizzaAssistito(dbr,hAssistito,dbc);
                hOperatore.put(operatore,hAssistito);
            }//fine descrizione assistito
            return hOperatore;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaAssistito");
	}
}

private Hashtable AnalizzaAssistito(ISASRecord dbr,Hashtable hAssistito,ISASConnection dbc)
throws SQLException
{
        try
        {
            Hashtable hDati=new Hashtable();
            if (dbr.get("n_cartella")!=null && !((Integer)dbr.get("n_cartella")).equals(""))
            {
               String assistito="";
               if (dbr.get("assistito")!=null && !((String)dbr.get("assistito")).equals(""))
                    assistito=(String)dbr.get("assistito");
                String  cartella=assistito+"|"+dbr.get("n_cartella");
                if (hAssistito!=null && ((Hashtable)hAssistito.get(cartella))!=null)
                     hDati=(Hashtable)hAssistito.get(cartella);
                hDati=caricaDati(dbr,hDati,dbc);
                hAssistito.put(cartella,hDati);
            }//fine descrizione assistito
            return hAssistito;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaPrestazione");
	}
}

public  Hashtable caricaDati(ISASRecord dbr ,Hashtable tab,ISASConnection dbc)
throws SQLException
{
        try
        {

          String assistito="";
          if (dbr.get("assistito")!=null && !((String)dbr.get("assistito")).equals(""))
                  assistito=(String)dbr.get("assistito");

          tab.put("#assistito#",assistito);

          String operatore="";
          if (dbr.get("operatore")!=null && !((String)dbr.get("operatore")).equals(""))
                  operatore=(String)dbr.get("operatore");

          tab.put("#operatore#",operatore);

           String intervento="";
          if (dbr.get("int_anno")!=null  )
                intervento=""+dbr.get("int_anno");
          if (dbr.get("int_contatore")!=null  )
                intervento=intervento +"|"+dbr.get("int_contatore");

          int numPrest=mkNumPrest(dbc,dbr);
          int numPrestTot =0;
          if (tab.get("#num_prest#")!=null)
          {
                   String sNumPrestTot=(String)tab.get("#num_prest#");
                   try{
                        numPrestTot=Integer.parseInt(sNumPrestTot);
                   }
                   catch(Exception e)
                   {System.out.println("ERRORE NELLA CONVERSIONE PER IL CALCOLO num_prest:"+e);
                   numPrestTot=0;
                   }
           }
           tab.put("#num_prest#",""+(numPrestTot+numPrest));

         int minuti=0;
         if (dbr.get("int_tempo")!=null)
                minuti=convNumDBToInt("int_tempo",dbr);
         //vado ad aggiornare il totale
         int minutiTot =0;
         if (tab.get("#tempo#")!=null)
        {
                 String sMinutiTot=(String)tab.get("#tempo#");
                 try{
                      minutiTot=Integer.parseInt(sMinutiTot);
                 }
                 catch(Exception e)
                 {System.out.println("ERRORE NELLA CONVERSIONE PER IL CALCOLO ORE TOTALE:"+e);
                 minutiTot=0;
                 }
         }
         tab.put("#tempo#",""+(minutiTot+minuti));

          return tab;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una caricaDati()");
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

private void preparaLayoutEleAss(mergeDocument md, ISASConnection dbc,String data_inizio,String data_fine,Hashtable par) {
	Hashtable htxt = new Hashtable();
	String titolo = "";
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
        String data=data_inizio.substring(8, 10)+"/"+
                  data_inizio.substring(5, 7)+"/"+data_inizio.substring(0, 4);
        htxt.put("#data_inizio#",data);
        data=data_fine.substring(8, 10)+"/"+
                data_fine.substring(5, 7)+"/"+data_fine.substring(0, 4);
        htxt.put("#data_fine#", data);
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

        String tipopz = (String)par.get("tipopz");
        String tipologia="";
        if (tipopz.equals("D"))
                tipologia="Domiciliari";
        else if (tipopz.equals("A"))
                tipologia="Ambulatoriali";
        htxt.put("#tipo_prestazione#", tipologia);

        String tipoStampa=(String)par.get("terr");
        StringTokenizer st = new StringTokenizer(tipoStampa,"|");

        String sZona=st.nextToken();
        String sDis=st.nextToken();
        String sCom=st.nextToken();
        String sTipo=st.nextToken();

        String criteri="";
        String zona="Nessuna divisione";
        if (sZona.equals("1"))
            zona=DecodificaZona(dbc,(String)par.get("zona"));
        criteri="Zona: "+zona+ "  ";

        String distretto="Nessuna divisione";
        if (sDis.equals("1"))
              distretto=DecodificaDistretto(dbc,(String)par.get("distretto"));
        criteri=criteri+"Distretto: "+distretto+ "  ";

       String ragg =(String)par.get("ragg");
       String pca="";
        if (ragg.equals("A"))
            pca="Area Distr.";
        else if (ragg.equals("C"))
             pca="Comune";
        else if (ragg.equals("P"))
            pca="Presidio";
        String comune="Nessuna Divisione";
        if (sCom.equals("1"))
             comune=DecodificaLiv3(dbc,(String)par.get("pca"), ragg);
        criteri=criteri+pca+": "+comune+ "  ";

        String figprof="";
        if(!par.get("figprof").equals("00"))
                      figprof=(String)par.get("figprof");

        String tipo="Nessuna divisione";
        if (sTipo.equals("1"))
        {
            if (figprof.equals("01"))
                    tipo=DecodificaTipoUtenteSOC(dbc,(String)par.get("ute"));
             else
                    tipo=DecodificaTipoUtenteINF(dbc,(String)par.get("ute"));
        }
        criteri=criteri+"Tipologia: "+tipo;
        htxt.put("#criteri#", criteri);


        if (par.get("figprof")!=null){
        	String fig = (String)par.get("figprof");
                if (fig.equals("01"))
                	titolo = "Servizio sociale :";
                else if (fig.equals("02"))
                	titolo = "Servizio Infermieristico: ";
                else if (fig.equals("03"))
                        titolo = "Servizio Medico: ";
               	else if (fig.equals("04"))
                        titolo = "Servizio Fisioterapico: ";
                else titolo = "SINSS: ";
        }
        htxt.put("#titolo#", titolo);


	md.writeSostituisci("layout",htxt);

}

private int mkNumPrest(ISASConnection dbc,ISASRecord dbr){
  int ret =1;
  try {
        String anno= ""+ dbr.get("int_anno");
        String contatore= ""+ dbr.get("int_contatore");
        String sel = "SELECT sum(pre_numero) somma FROM intpre WHERE"+
          " pre_anno='"+anno+"'" + " AND "+ " pre_contatore="+contatore ;
//        System.out.println("mkNumPrest"+sel);
        ISASRecord dbrpre = dbc.readRecord(sel);
        if (dbrpre!=null && dbrpre.get("somma")!=null)
              ret = convNumDBToInt("somma",dbrpre);
    return ret;
  } catch (Exception ex) {
    System.out.println("mkNumPrest"+ex);
       }
  return ret;
}


private String DecodificaTipoUtenteSOC(ISASConnection dbc,String codice){
  String ret = "NON SPECIFICATO";
  try {
        String sel = "SELECT descrizione FROM tipute WHERE"+
          " codice='"+codice+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("descrizione")!=null)
          ret = (String)dbr.get("descrizione");
  } catch (Exception ex) {
        System.out.println("DecodificaTiputente"+ex);}
  return ret;
}

private String DecodificaTipoUtenteINF(ISASConnection dbc,String codice){
  String ret = "NON SPECIFICATO";
  try {
        String sel = "SELECT descrizione FROM tipute_s WHERE"+
          " codice='"+codice+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("descrizione")!=null)
          ret = (String)dbr.get("descrizione");
  } catch (Exception ex) {
        System.out.println("DecodificaTiputente"+ex);}
  return ret;
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

	// 02/05/07
	private class Totali 
	{
		private Totali() 
		{
			this.setDefault();
		}

		private int tot_min = 0;
		private int tot_prest = 0;

		private void setDefault() { 
			this.tot_min = 0;
			this.tot_prest = 0;
		}	

		private void add(Totali t) {
		 	this.tot_min += t.tot_min;
		 	this.tot_prest += t.tot_prest;
		}
	} // end of Totali class


	// 02/05/07 
	private void caricaTotxTipo(Hashtable hD, Totali tot) throws Exception
	{
		String min = (String)hD.get("#tempo#");
		if ((min != null) && (!min.trim().equals("")))
	        tot.tot_min += Integer.parseInt(min);

		String prest = (String)hD.get("#num_prest#");
		if ((prest != null) && (!prest.trim().equals("")))
	        tot.tot_prest += Integer.parseInt(prest);
	}

	// 02/05/07
	private void stampaTot(mergeDocument md, String desc, Totali tot, int numAss) throws Exception
	{
		NumberDateFormat ndf = new NumberDateFormat();
		int minTot = tot.tot_min;
		int prestTot = tot.tot_prest;
		int mediaTot = minTot/prestTot;

		Hashtable h_t = new Hashtable();
		h_t.put("#tit_tot#", desc + ":");
		h_t.put("#tot_ass#", "" + numAss);
		h_t.put("#tot_tempo#", ndf.getHhMmTime(""+minTot));
		h_t.put("#tot_num_prest#", "" + prestTot);
		h_t.put("#tot_media#", ndf.getHhMmTime(""+mediaTot));
		md.writeSostituisci("tabTot", (Hashtable)h_t);
	}


}	// End of FoDettOrePrestEJB class

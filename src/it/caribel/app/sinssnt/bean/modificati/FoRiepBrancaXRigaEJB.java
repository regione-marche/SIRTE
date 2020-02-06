package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 24/09/2003 - EJB di connessione alla procedura SINS Tabella FoRiepBrancaXRiga
//
// Ilaria Mancini
// 22/03/05 bargi uguale a FoRiepBrancaXRiga ma invertite righe con colonne
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

public class FoRiepBrancaXRigaEJB extends SINSSNTConnectionEJB {
	Hashtable hDecZona=new Hashtable ();
	Hashtable hDecDistr=new Hashtable ();
	Hashtable hDecPca=new Hashtable ();
	Hashtable hDecBranca=new Hashtable ();
	Hashtable hDecPrest=new Hashtable ();
	private String codice_usl="";
	private String codice_regione="";
//HASHTABLE PER IL CONTEGGIO
	Hashtable hCartella=new Hashtable ();
	Hashtable hAccessi=new Hashtable ();
	String dom_res;
	String dr;
public FoRiepBrancaXRigaEJB() {}

private void preparaLayout(ISASConnection dbc,Hashtable par,NDOContainer cnt ) {
	String txt = "";
	boolean done = false;
	String titolo = "";
	ISASCursor dbconf = null;
	try {
		String  mysel = "SELECT conf_txt,conf_key FROM conf WHERE "+
			"conf_kproc='SINS' AND (conf_key='codice_regione'"+
			" OR conf_key='ragione_sociale'"+
			" OR conf_key='codice_usl')";
		//debugMessage("FoRiepBrancaXRigaEJB.preparaLayout(): "+mysel);

		dbconf = dbc.startCursor(mysel);
		while (dbconf.next()) {
			ISASRecord dbtxt=dbconf.getRecord();
			if (((String)dbtxt.get("conf_key")).equals("ragione_sociale"))
				txt=(String)dbtxt.get("conf_txt");
			if (((String)dbtxt.get("conf_key")).equals("codice_regione"))
				codice_regione=(String)dbtxt.get("conf_txt");
			if (((String)dbtxt.get("conf_key")).equals("codice_usl"))
				codice_usl=(String)dbtxt.get("conf_txt");
		}
		dbconf.close();
		cnt.setFooter(txt);

		// inserisco i filtri
//		if (((String)par.get("oper")).equals("01"))

                //Inserisco la decodifica del tipo prestazione
                String tipo_prest=(String)par.get("tipo_prest");
                if (tipo_prest.equals("D"))
                  tipo_prest="domiciliari";
                else if (tipo_prest.equals("A"))
                  tipo_prest="ambulatoriali";
                else tipo_prest = "";

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
			else if (fig.equals("52"))
				titolo = "Servizio Oncologico: ";
			else titolo = "SINSS: ";
		  String conte = (String)par.get("cont");
                  if (conte.equals("PR"))
                      titolo += " Prestazioni "+tipo_prest+" per branca e ";
                  else if (conte.equals("AC"))
                      titolo += " Prestazioni per branca (+ Accessi) e ";
                  else if (conte.equals("AS"))
                      titolo += " Prestazioni per branca (+ Assistiti) e ";

		}//end if figprof!=null
		String ragg =(String)par.get("ragg");
		String tipopca="";
		Vector vtitoli = new Vector();
//		if (ragg.equals("A")) {
//			tipopca = "area distrettuale";
//		} else if (ragg.equals("C")) {
//			tipopca = "comune";
//		} else if (ragg.equals("P")) {
//			tipopca = "presidio";
//		}
		
        
        if(this.dom_res==null)
        {
        	if (ragg.equals("A")) 
                tipopca = " Area distrettuale ";
             else if (ragg.equals("C")) 
                    tipopca = " Comune ";
             else if (ragg.equals("P"))
                    tipopca = " Presidio ";

        }else if (this.dom_res.equals("D"))
        {
        	if (ragg.equals("A")) 
                tipopca = " Area distrettuale di Domicilio ";
             else if (ragg.equals("C")) 
                    tipopca = " Comune di Domicilio ";
        }else if (this.dom_res.equals("R"))
            {
            	if (ragg.equals("A")) 
                    tipopca = " Area distrettuale di Residenza ";
                 else if (ragg.equals("C")) 
                        tipopca = " Comune di Residenza ";
            } 	        			
            
		
                vtitoli.add("Branca");
		vtitoli.add("Prestazioni");

		cnt.setGroupTitles(vtitoli);
		titolo += tipopca;

		String dataini = (String)par.get("data_inizio");
		if (dataini.length() == 10)
			dataini = dataini.substring(8,10)+"/"+
				dataini.substring(5,7)+
				"/"+dataini.substring(0,4);

		String datafine=(String)par.get("data_fine");
		if (datafine.length() == 10)
			datafine = datafine.substring(8,10)+"/"+
				datafine.substring(5,7)+
				"/"+datafine.substring(0,4);

		titolo = titolo + " dal " + dataini +" al " + datafine;
		cnt.setHeader(titolo);

		String zona = DecodificaZona(dbc,
			(String)par.get("zona"));
		String distr = DecodificaDistretto(dbc,
			(String)par.get("distretto"));
		String pca = DecodificaLiv3(dbc,
			(String)par.get("pca"), ragg);
		titolo = " zona: "+zona+" - distretto: "+distr+
			" - "+tipopca+": "+pca;

		String branca = DecodificaBranca(dbc,
			(String)par.get("branca"), (String)par.get("tipopre"));

		if (!(branca.trim()).equals(""))
			titolo += " - Branca :"+ branca;
		//02/05/2006 Sara
        String operatore = DecodificaOp(dbc,(String)par.get("oper"));
        if (!(operatore.trim()).equals(""))
			titolo += " - Operatore :"+ operatore;

		cnt.setSubTitle(titolo);
		done = true;
	} catch (Exception ex) {
		cnt.setHeader(titolo);
	}finally{
		if(!done){
			try{
                             if (dbconf!=null)
                                      dbconf.close();
			}catch(Exception e1){
				System.out.println("FoRiepBrancaXRigaEJB.preparaLayout(): "+e1);
			}
		}
	}
}

private void preparaBody(ISASCursor dbcur,NDOContainer cnt,NDOUtil unt,String conte)
	throws Exception {

	Hashtable hgen = new Hashtable();
	Hashtable htotAss = new Hashtable();
	Hashtable htotAcc = new Hashtable();
	
	Hashtable hCartAss = new Hashtable();
	Hashtable hAcc = new Hashtable();
	try {
		while (dbcur.next())
                {
			ISASRecord dbr=dbcur.getRecord();
			// zona,distretto,pca sono i tre livelli che mi
			// definiscono le righe
			String cod_zona=""+dbr.get("cod_zona");
			String cod_distretto=""+ dbr.get("cod_distretto");
			String pca=""+ dbr.get("codice");

			// codice branca | cod_tippre e codice prestazione mi
			// definiscono le colonne
			if (dbr.get("cod_branca")!=null &&
			    dbr.get("prest_cod")!=null){
				String cod_branca=""+dbr.get("cod_branca");
				String cod_pres=""+dbr.get("prest_cod");
                                String cod_tippre=""+dbr.get("cod_tippre");
                                if (conte.equals("PR")){   //conto le prestazioni
                                     cnt.put(unt.mkPar(cod_branca+"|"+cod_tippre,cod_pres),
                                     unt.mkPar(cod_zona,cod_distretto,pca),
                                         dbr.get("pre_numero"));

                                    settaTitoliRiga(dbr,cnt,unt);
                                    settaTitoliColonna(dbr,cnt,unt);

                                }else if (conte.equals("AC")){//conto gli accessi
                                          String anno=""+dbr.get("pre_anno");
                                          String contatore=""+dbr.get("pre_contatore");
                                              cnt.put(unt.mkPar(cod_branca+"|"+cod_tippre,cod_pres),
                                              unt.mkPar(cod_zona,cod_distretto,pca),
                                                  new Integer(1));
                                              hAccessi.put(anno+"|"+contatore+"|"+cod_pres,"");
                                              settaTitoliRiga(dbr,cnt,unt);
  				              settaTitoliColonna(dbr,cnt,unt);
							  
							  
							     if (hAcc.get(anno+contatore+"|"+cod_zona+"|"+cod_distretto+"|"+pca)==null){
					  if (htotAcc.get(cod_zona+"|"+cod_distretto+"|"+pca)==null){
					      
						  htotAcc.put(cod_zona+"|"+cod_distretto+"|"+pca,"1");
						}else{
						  int totAcc = Integer.parseInt(""+htotAcc.get(cod_zona+"|"+cod_distretto+"|"+pca));
						  totAcc++;
					     
						  htotAcc.put(cod_zona+"|"+cod_distretto+"|"+pca,""+totAcc);
					        }
						hAcc.put(anno+contatore+"|"+cod_zona+"|"+cod_distretto+"|"+pca,"");
					      }//end if hCartAss==null
							  

                                }else if (conte.equals("AS")){//conto gli assistiti
                                          String cartella=""+dbr.get("int_cartella");
					  String codCart = cartella+"|"+cod_branca+"|"+cod_pres;
					  if (hCartella.get(codCart)==null)
                                          {   cnt.put(unt.mkPar(cod_branca+"|"+cod_tippre,cod_pres),
                                              unt.mkPar(cod_zona,cod_distretto,pca),
                                                  new Integer(1));
                                              hCartella.put(codCart,"");
                                              settaTitoliRiga(dbr,cnt,unt);
  				              settaTitoliColonna(dbr,cnt,unt);
					      if (hgen.get(cod_zona+"|"+cod_distretto+"|"+pca)==null)
					        hgen.put(cod_zona+"|"+cod_distretto+"|"+pca,"1");
					      else{
						  int tot = Integer.parseInt(""+hgen.get(cod_zona+"|"+cod_distretto+"|"+pca));
						  tot++;
						  hgen.put(cod_zona+"|"+cod_distretto+"|"+pca,""+tot);
					      }
					      if (hCartAss.get(cartella+"|"+cod_zona+"|"+cod_distretto+"|"+pca)==null){
					      //System.out.println("*** cart|zona|distr|pca : "+cartella+"|"+cod_zona+"|"+cod_distretto+"|"+pca);
						if (htotAss.get(cod_zona+"|"+cod_distretto+"|"+pca)==null){
					      //System.out.println("*** SONO QUI 1: ");
						  htotAss.put(cod_zona+"|"+cod_distretto+"|"+pca,"1");
						}else{
						  int totA = Integer.parseInt(""+htotAss.get(cod_zona+"|"+cod_distretto+"|"+pca));
						  totA++;
					      //System.out.println("*** SONO QUI 2: "+totA);
						  htotAss.put(cod_zona+"|"+cod_distretto+"|"+pca,""+totA);
					        }
						hCartAss.put(cartella+"|"+cod_zona+"|"+cod_distretto+"|"+pca,"");
					      }//end if hCartAss==null

                                          }
                                }
			}
		}
		Enumeration en = hgen.keys();
		while (en.hasMoreElements()){
		  String e=(String)en.nextElement();
		  StringTokenizer st = new StringTokenizer(e,"|");
 		  cnt.put(unt.mkPar("ZZZZZA"),
                                              unt.mkPar(st.nextToken(),st.nextToken(),st.nextToken()),
                                                  new Integer(""+hgen.get(e)));

		}

		
		
		Enumeration enumAss = htotAss.keys();
		while (enumAss.hasMoreElements()){
		  String e=(String)enumAss.nextElement();
		  StringTokenizer st = new StringTokenizer(e,"|");
 		  if (conte.equals("AS")) cnt.put(unt.mkPar("ZZZZZB"),
                                              unt.mkPar(st.nextToken(),st.nextToken(),st.nextToken()),
                                                  new Integer(""+htotAss.get(e)));
		}
		
		Enumeration enumAcc = htotAcc.keys();
		while (enumAcc.hasMoreElements()){
		  String e=(String)enumAcc.nextElement();
		  StringTokenizer st = new StringTokenizer(e,"|");
 		  if (conte.equals("AC")) cnt.put(unt.mkPar("ZZZZZB"),
                                              unt.mkPar(st.nextToken(),st.nextToken(),st.nextToken()),
                                                  new Integer(""+htotAcc.get(e)));
		}

		cnt.calculate();
		//cnt.debugPrint();	// DEBUG
	} catch(Exception e) {
		debugMessage("FoRiepBrancaXRigaEJB.preparaBody(): "+e);
		throw new SQLException("Errore eseguendo preparaBody()");
	}
}
private void settaTitoliRiga(ISASRecord dbr,NDOContainer cnt,NDOUtil unt ) throws Exception
{
      try{
            String cod_branca=""+dbr.get("cod_branca");
            String cod_tippre=""+dbr.get("cod_tippre");
            String cod_pres=""+ dbr.get("prest_cod");

            if (hDecBranca.containsKey(cod_branca +"|" +cod_tippre) ){
                if (!hDecPrest.containsKey(cod_pres)){
                       hDecPrest.put(cod_pres,""+dbr.get("prest_des"));
                       cnt.setRowTitle(unt.mkPar(cod_branca +"|" +cod_tippre,cod_pres),(String)dbr.get("prest_des"));
                }
            }else{
                hDecBranca.put(cod_branca +"|" +cod_tippre,(String)dbr.get("branca"));
                hDecPrest.put(cod_pres,(String)dbr.get("prest_des"));
                cnt.setRowTitle(unt.mkPar(cod_branca +"|" +cod_tippre),(String)dbr.get("branca"));
                cnt.setRowTitle(unt.mkPar(cod_branca +"|" +cod_tippre,cod_pres),(String)dbr.get("prest_des"));
            }
      }catch(Exception e){
           debugMessage("FoRiepBrancaXRigaEJB.settaTitoliColonna: "+e);
            throw new SQLException("Errore eseguendo settaTitoliColonna()");
     }

}



private void settaTitoliColonna(ISASRecord dbr,NDOContainer cnt ,NDOUtil unt)
	throws Exception {
      try
      {
            String cod_zona=""+dbr.get("cod_zona");
            String cod_distretto=""+ dbr.get("cod_distretto");
            String pca=""+ dbr.get("codice");
            //debugMessage("settaTitoliColonna \n zona="+cod_zona+
            //" \n distretto="+cod_distretto+" \n pca="+pca);
            //debugMessage("hdecpca==="+hDecPca.toString());
            //controllo prima la zona se esiste nella hashtable hDecZona
            if (hDecZona.containsKey(cod_zona))
            {
               //controllo se esiste nella hashtable hDecDistr
               if (hDecDistr.containsKey(cod_distretto))
               {     //controllo se esiste nella hashtable hDecPca
                     if (!hDecPca.containsKey(pca))
                     { hDecPca.put(pca,""+dbr.get("descrizione"));
                       cnt.setColTitle(unt.mkPar(cod_zona,cod_distretto,pca),""+dbr.get("descrizione"));
                     }
               }
               else
               {
                      /*se non trovo la descrizione del distretto non l'ho neppure per
                      il terzo livello
                       */
                      hDecDistr.put(cod_distretto,""+dbr.get("des_distretto"));
                      hDecPca.put(pca,""+dbr.get("descrizione"));
                      cnt.setColTitle(unt.mkPar(cod_zona,cod_distretto),""+dbr.get("des_distretto"));
                      cnt.setColTitle(unt.mkPar(cod_zona,cod_distretto,pca),""+dbr.get("descrizione"));
               }
            }
            else
            {
                /*se non trovo la decodifica della zona sono sicura
                che non esiste neppure quella del distretto
                o del terzo livello*/
                hDecZona.put(cod_zona,""+dbr.get("des_zona"));
                hDecDistr.put(cod_distretto,""+dbr.get("des_distretto"));
                hDecPca.put(pca,""+dbr.get("descrizione"));
                cnt.setColTitle(unt.mkPar(cod_zona),""+dbr.get("des_zona"));
                cnt.setColTitle(unt.mkPar(cod_zona,cod_distretto),""+dbr.get("des_distretto"));
                cnt.setColTitle(unt.mkPar(cod_zona,cod_distretto,pca),""+dbr.get("descrizione"));
            }
      }
      catch(Exception e)
      {
           debugMessage("FoRiepBrancaXRigaEJB.settaTitoliRiga: "+e);
            throw new SQLException("Errore eseguendo settaTitoliRiga()");
     }

}

private String mkSelectAnalitica(ISASConnection dbc, Hashtable par)throws Exception {
     try {
     ServerUtility su =new ServerUtility();
     String s = "SELECT  "+
		" u.cod_zona,u.des_zona, "+
                " u.des_distretto,u.cod_distretto,"+
                " u.codice ,u.descrizione ,"+
                " p.pre_numero,b.cod_tippre,"+
//ILARIA INIZIO:per il conteggio degli assistiti, e degli interventi
                " p.pre_anno,p.pre_contatore,i.int_cartella,"+
//ILARIA FINE
                " b.descrizione branca,b.codice cod_branca,pz.prest_cod,"+
                " pz.prest_des "+
		" FROM interv i, intpre p,"+
                " "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, "+
                " prestaz pz,branca b "
				//02/05/2006 Sara
				+" ,operatori op"

				+" WHERE i.int_anno=p.pre_anno "+
                " AND i.int_contatore=p.pre_contatore "+
                " AND p.pre_cod_prest=pz.prest_cod "+
                " AND pz.prest_tipo=b.cod_tippre " +
                " AND pz.prest_branca=b.codice " 
				//02/05/2006 Sara
			   +" AND op.codice=i.int_cod_oper";

	if (!((String)par.get("branca")).equals("") && !((String)par.get("tipopre")).equals(""))
        {
                s = su.addWhere(s, su.REL_AND, "b.codice",
                        su.OP_EQ_STR, (String)par.get("branca"));
                s = su.addWhere(s, su.REL_AND, "b.cod_tippre",
                        su.OP_EQ_STR, (String)par.get("tipopre"));
	}
	
	//Minerba 06/03/2013		
	   String qualifica = (String) par.get("qualifica");
		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
			s +=" AND i.int_qual_oper='"+qualifica+"'";
		}//fine Minerba 06/03/2013

        s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));


	String figprof="";
	if(!par.get("figprof").equals("00"))
        	figprof=(String)par.get("figprof");

        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
                su.OP_EQ_STR, figprof);

	//02/05/2006 Sara
       s = su.addWhere(s, su.REL_AND, "op.codice",
                su.OP_EQ_STR,(String)par.get("oper"));

	String scr = (String)par.get("ragg");
	s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);
//        if (scr!=null && scr.equals("A"))
//          s += " AND i.int_cod_areadis=u.codice ";
//        else if (scr!=null && scr.equals("P"))
//          s += " AND u.codice=i.int_codpres ";
	if (this.dom_res==null)
	{
		 if (scr!=null && scr.equals("A"))
	          s += " AND i.int_cod_areadis=u.codice ";
	        else if (scr!=null && scr.equals("P"))
	          s += " AND u.codice=i.int_codpres ";
	       
	}else if (this.dom_res.equals("R"))
	{
		 if (scr!=null && scr.equals("A"))
	          s += " AND i.int_cod_areadis=u.codice ";
	        
	}else if (this.dom_res.equals("D"))
	{
		 if (scr!=null && scr.equals("A"))
	          s += " AND i.int_cod_areadis=u.codice ";
	        
	}
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM,
	formatDate(dbc, (String)par.get("data_inizio")));

        //12/01/2004 Jessica inserito filtro flag tipo accertamento:
        // domiciliare o ambulatoriale o entrambi(nessun filtro)
        String tipo_prest="";
        if(!par.get("tipo_prest").equals("E")){
          tipo_prest=(String)par.get("tipo_prest");
          //System.out.println("Tipo_prest: "+tipo_prest);
          s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, tipo_prest);
        }

        /*String scomuni="";
        while(dbcur.next()) {
                ISASRecord dbr = dbcur.getRecord();
                scomuni = su.addWhere(scomuni, su.REL_OR, "i.int_cod_comune",
                        su.OP_EQ_STR,  (String)dbr.get("codice"));
        }
	if (!scomuni.equals(""))
	        s = s + " AND ( "+scomuni+" )";
	//s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione ";*/
	s = s + " ORDER BY pz.prest_cod";
	debugMessage("FoRiepBrancaXRigaEJB.getSelectAnalitica(): "+s);
	return s;
        	} catch(Exception e) {
		debugMessage("FoRiepBrancaXRigaEJB.mkSelectAnalitica(): "+e);
		throw(e);
	}
}

private String mkSelectAnaliticaSenzaUbicazioni(ISASConnection dbc, Hashtable par,
                ISASCursor dbcur)throws Exception {
     try {
     ServerUtility su =new ServerUtility();
     String s = "SELECT  "+
                " d.cod_zona ,z.descrizione_zona des_zona, "+
		" c.codice,c.descrizione, d.cod_distr cod_distretto, "+
		" d.des_distr des_distretto, "+
                " p.pre_numero,b.cod_tippre,"+
//ILARIA INIZIO:per il conteggio degli assistiti, e degli interventi
                " p.pre_anno,p.pre_contatore,i.int_cartella,"+
//ILARIA FINE
                " b.descrizione branca,b.codice cod_branca,pz.prest_cod,"+
                " pz.prest_des "+
		" FROM interv i, intpre p,"+
                " comuni c ,distretti d,zone z, "+
                " prestaz pz,branca b "
				//02/05/2006 Sara
				+" ,operatori op"

				+" WHERE i.int_anno=p.pre_anno "+
                " AND c.cod_distretto = d.cod_distr "+
		" AND d.cod_zona = z.codice_zona "+
//                " AND i.int_cod_comune=c.codice " +
                " AND i.int_contatore=p.pre_contatore "+
                " AND p.pre_cod_prest=pz.prest_cod "+
                " AND pz.prest_tipo=b.cod_tippre " +
                " AND pz.prest_branca=b.codice " 
				//02/05/2006 Sara
			   +" AND op.codice=i.int_cod_oper";
 	if (this.dom_res==null)
		s+=" AND i.int_cod_comune=c.codice ";
	else if (this.dom_res.equals("D"))
		s+=" AND i.int_cod_comune=c.codice ";           	
	else if (this.dom_res.equals("R"))
		s+=" AND i.int_cod_comune=c.codice ";
	if (!((String)par.get("branca")).equals("") && !((String)par.get("tipopre")).equals(""))
        {
                s = su.addWhere(s, su.REL_AND, "b.codice",
                        su.OP_EQ_STR, (String)par.get("branca"));
                s = su.addWhere(s, su.REL_AND, "b.cod_tippre",
                        su.OP_EQ_STR, (String)par.get("tipopre"));
	}
	
	//Minerba 06/03/2013		
	   String qualifica = (String) par.get("qualifica");
		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
			s +=" AND i.int_qual_oper='"+qualifica+"'";
		}//fine Minerba 06/03/2013
		
  /*      s = su.addWhere(s, su.REL_AND, "u.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
		su.OP_EQ_STR, (String)par.get("distretto"));
	s = su.addWhere(s, su.REL_AND, "u.codice",
		su.OP_EQ_STR, (String)par.get("pca"));
*/
        s = su.addWhere(s, su.REL_AND, "d.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "d.cod_distr",
		su.OP_EQ_STR, (String)par.get("distretto"));
	String figprof="";
	if(!par.get("figprof").equals("00"))
        	figprof=(String)par.get("figprof");

        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
                su.OP_EQ_STR, figprof);

	//02/05/2006 Sara
       s = su.addWhere(s, su.REL_AND, "op.codice",
                su.OP_EQ_STR,(String)par.get("oper"));

	/*String scr = (String)par.get("ragg");
	//s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);
        if (scr!=null && scr.equals("C"))
          s += " AND i.int_cod_comune=u.codice ";
        else if (scr!=null && scr.equals("A"))
          s += " AND i.int_cod_areadis=u.codice ";
        else if (scr!=null && scr.equals("P"))
          s += " AND u.codice=i.int_codpres ";*/

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM,
	formatDate(dbc, (String)par.get("data_inizio")));

        //12/01/2004 Jessica inserito filtro flag tipo accertamento:
        // domiciliare o ambulatoriale o entrambi(nessun filtro)
        String tipo_prest="";
        if(!par.get("tipo_prest").equals("E")){
          tipo_prest=(String)par.get("tipo_prest");
          //System.out.println("Tipo_prest: "+tipo_prest);
          s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, tipo_prest);
        }

        String scomuni="";
        while(dbcur.next()) {
                ISASRecord dbr = dbcur.getRecord();
                scomuni = su.addWhere(scomuni, su.REL_OR, "c.codice",
                        su.OP_EQ_STR,  (String)dbr.get("codice"));
        }
	if (!scomuni.equals(""))
	        s = s + " AND ( "+scomuni+" )";
	//s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione ";
	s = s + " ORDER BY pz.prest_cod";
	debugMessage("FoRiepBrancaXRigaEJB.getSelectAnalitica(): "+s);
	return s;
        	} catch(Exception e) {
		debugMessage("FoRiepBrancaXRigaEJB.mkSelectAnalitica(): "+e);
		throw(e);
	}
}
private String mkSelectComuni(ISASConnection dbc, Hashtable par){
     ServerUtility su =new ServerUtility();
     String s = "SELECT  DISTINCT "/*+
 		" i.int_cod_comune codice "*/;
     	if (this.dom_res==null)
     		s+=" i.int_cod_comune codice ";
     	else if (this.dom_res.equals("D"))
     		s+=" i.int_cod_comune codice ";           	
     	else if (this.dom_res.equals("R"))
     		s+=" i.int_cod_comune codice ";
     	s+= " FROM interv i, intpre p,"+
                " prestaz pz,branca b "+
				//02/05/2006 Sara
				" ,operatori op"+

                " WHERE i.int_anno=p.pre_anno "+
                " AND i.int_contatore=p.pre_contatore "+
                " AND p.pre_cod_prest=pz.prest_cod "+
                " AND pz.prest_tipo=b.cod_tippre " +
                " AND pz.prest_branca=b.codice " 
				//02/05/2006 Sara
			   +" AND op.codice=i.int_cod_oper";

	if (!((String)par.get("branca")).equals("") && !((String)par.get("tipopre")).equals(""))
        {
                s = su.addWhere(s, su.REL_AND, "b.codice",
                        su.OP_EQ_STR, (String)par.get("branca"));
                s = su.addWhere(s, su.REL_AND, "b.cod_tippre",
                        su.OP_EQ_STR, (String)par.get("tipopre"));
	}
	//Minerba 06/03/2013		
	   String qualifica = (String) par.get("qualifica");
		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
			s +=" AND i.int_qual_oper='"+qualifica+"'";
		}//fine Minerba 06/03/2013
		
	String figprof="";
	if(!par.get("figprof").equals("00"))
        	figprof=(String)par.get("figprof");

        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
                su.OP_EQ_STR, figprof);

	//02/05/2006 Sara
       s = su.addWhere(s, su.REL_AND, "op.codice",
                su.OP_EQ_STR,(String)par.get("oper"));

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM,
	formatDate(dbc, (String)par.get("data_inizio")));
//	s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",		su.OP_EQ_STR, (String)par.get("pca"));
	if (this.dom_res==null)
		s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, (String)par.get("pca"));
	else if (this.dom_res.equals("D"))
		s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, (String)par.get("pca"));           	
	else if (this.dom_res.equals("R"))
		s = su.addWhere(s, su.REL_AND, "i.int_cod_comune",su.OP_EQ_STR, (String)par.get("pca"));
        //12/01/2004 Jessica inserito filtro flag tipo accertamento:
        // domiciliare o ambulatoriale o entrambi(nessun filtro)
        String tipo_prest="";
        if(!par.get("tipo_prest").equals("E")){
          tipo_prest=(String)par.get("tipo_prest");
          //System.out.println("Tipo_prest: "+tipo_prest);
          s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, tipo_prest);
        }
	//s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione ";
	debugMessage("FoRiepBrancaXRigaEJB.getSelectAnalitica(): "+s);
	return s;
}

public byte[] query_ripbranca(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	debugMessage("FoRiepBrancaXRigaEJB.query_ripbranca(): inizio...");

	try {
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
			
		
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);

		NDOContainer cnt = new NDOContainer();
		//cnt.setDebug(true);	// DEBUG
		NDOUtil unt = new NDOUtil ();

		preparaLayout(dbc, par, cnt);
                String scr = (String)par.get("ragg");
                ISASCursor dbcur=null;
                System.out.println("INIZIO QUERY" +currentTime());
                if (scr!=null && scr.equals("C"))
                {
                        System.out.println("INIZIO QUERY Comuni" +currentTime());
                        ISASCursor dbcurComuni = dbc.startCursor(mkSelectComuni(dbc,par));
                        dbcur = dbc.startCursor(mkSelectAnaliticaSenzaUbicazioni(dbc,par,dbcurComuni));
                        dbcurComuni.close();
                }
                else
                        dbcur = dbc.startCursor(mkSelectAnalitica(dbc,par));
                System.out.println("FINE QUERY" +currentTime());
                String conte = (String)par.get("cont");
		if (dbcur.getDimension() <= 0) {
			debugMessage("FoRiepBrancaXRigaEJB.query_ripbranca(): vuoto");
			cnt.setSubTitle("NESSUNA INFORMAZIONE REPERITA");
		} else {
System.out.println("RECORD TROVATI-->"+dbcur.getDimension() );
	            preparaBody(dbcur,cnt,unt,conte);
		}

		String tipo = ""+par.get("tipo");
		String formato = ""+par.get("formato");
		dbcur.close();

		dbc.close();
		super.close(dbc);
		done=true;
		return StampaNDO(cnt,tipo,formato,conte);
	} catch(Exception e) {
		debugMessage("FoRiepBrancaXRigaEJB.query_ripbranca(): "+e);
		throw new SQLException("FoRiepBrancaXRigaEJB.query_ripbranca(): "+e);
	} finally {
		debugMessage("FoRiepBrancaXRigaEJB.query_ripbranca(): ...fine.");
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				System.out.println("FoRiepBrancaXRigaEJB.query_ripbranca(): "+e1);
			}
		}
	}
}

public String currentTime() {
  Calendar cal = Calendar.getInstance(TimeZone.getDefault());
  java.text.SimpleDateFormat sdf =
          new java.text.SimpleDateFormat(" HH:mm:ss");
  sdf.setTimeZone(TimeZone.getDefault());
  return sdf.format(cal.getTime());
}

private byte[] StampaNDO(NDOContainer cnt, String sintetica, String formato,
			String conte)
throws Exception {

	try {
	        NDOUtil unt = new NDOUtil ();
                cnt.setRowTitle(unt.mkPar("ZZZZZA"),"ZZZZZA");
                cnt.setRowTitle(unt.mkPar("ZZZZZB"),"ZZZZZB");
				
                cnt.colSort();
		cnt.rowSort();
                if (sintetica.equals("S")){
		  if (conte.equals("AS")){
                      //cnt.setRowTitle(unt.mkPar("ZZZZZA"),"Totali Prestazioni"); - RB 10/02/12
                      cnt.setRowTitle(unt.mkPar("ZZZZZA"),"Totali colonna");
                      cnt.setRowTitle(unt.mkPar("ZZZZZB"),"Totali assistiti");
		  }
		  else if (conte.equals("AC")){
                      cnt.setRowTitle(unt.mkPar("ZZZZZA"),"Totale Prestazioni");
                      cnt.setRowTitle(unt.mkPar("ZZZZZB"),"Totale Accessi");
		  } 
                }else{
                   if (conte.equals("AS")){
                      cnt.setRowTitle(unt.mkPar("ZZZZZA"),"per colonna");
                      cnt.setRowTitle(unt.mkPar("ZZZZZB"),"assistiti");
		  }else if (conte.equals("AC")){
                      cnt.setRowTitle(unt.mkPar("ZZZZZA"),"per colonna");
                      cnt.setRowTitle(unt.mkPar("ZZZZZB"),"accessi");
		  }
                }

                NDOPrinter prt = new NDOPrinter();
                if (sintetica.equals("S")){
		  if (conte.equals("AS")|| conte.equals("AC"))
			prt.addContainer(cnt,true,false,0,2,false,true);
		  else
			prt.addContainer(cnt,true,false,0,2);
		}else{
		  if (conte.equals("AS") || conte.equals("AC"))
			prt.addContainer(cnt,true,false,1,2,false,true);
		  else
			prt.addContainer(cnt,true,false,1,2);
		}

		System.out.println("FINE STAMPA" +currentTime());
		return prt.getDocument(Integer.parseInt(formato));
	} catch(Exception e) {
		debugMessage("FoRiepBrancaXRigaEJB.StampaNDO(): "+e);
		throw new SQLException("Errore eseguendo StampaNDO()");
	}
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

private String DecodificaBranca(ISASConnection dbc,String branca,String tipopre)
{
  String ret = "";
  try {
    if (branca!=null || tipopre!=null){
      if (branca.equals("") || tipopre.equals(""))
        ret = "TUTTE";
      else{
        String sel = "SELECT descrizione FROM branca WHERE"+
          "codice ='"+branca+"'  AND cod_tippre='" + tipopre+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("descrizione")!=null)
          ret = (String)dbr.get("descrizione");
      }
    }
  }catch (Exception ex) {}
  return ret;
}
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

private String DecodificaOp(ISASConnection dbc,String codice)
{
  String ret = "";
  try {
    if (codice!=null && !codice.equals("") ){
        String sel = "SELECT  nvl(trim(cognome),'') || ' ' || nvl(trim(nome),'') operatore "+
          " FROM operatori WHERE codice='"+codice+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("operatore")!=null)
          ret = (String)dbr.get("operatore");
    }
  }catch (Exception ex) {}
  return ret;
}


}	// End of FoRiepBrancaXRiga class

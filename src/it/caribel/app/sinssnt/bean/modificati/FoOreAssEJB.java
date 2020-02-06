package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 12/04/2005 - EJB di connessione alla procedura SINS Tabella
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
import java.text.DecimalFormat;

public class FoOreAssEJB extends SINSSNTConnectionEJB {
	Hashtable hPrest=new Hashtable ();
	Hashtable hLivello=new Hashtable ();
	private String codice_usl="";
	private String codice_regione="";
//HASHTABLE PER IL CONTEGGIO
	Hashtable hCartellaEta=new Hashtable ();
        Hashtable hOperatore=new Hashtable ();
        String dom_res;
        String dr;

public FoOreAssEJB() {}

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
		debugMessage("FoOreAss.preparaLayout(): "+mysel);

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
			else titolo = "SINSS: ";
                }
		 String conte = (String)par.get("ore");
                 titolo += " Conteggio ore di assistenza";
                 if (conte.equals("int"))
                        titolo += " ( tempo interventi ) ";
                  else  titolo += " ( tempo prestazione ) ";

		String ragg =(String)par.get("ragg");
		String tipopca="";
//              if (ragg.equals("A")) {
//                      tipopca="Area distrettuale";
//              } else if (ragg.equals("C")) {
//                        tipopca="Comune";
//              } else if (ragg.equals("P"))
//                        tipopca="Presidio";
		
		
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

		Vector vtitoli = new Vector();
                String tipo=(String)par.get("tipo");
                if (tipo.equals("Z"))
                              vtitoli.add("Zona");
                else if (tipo.equals("D"))
                              vtitoli.add("Distretto");
                else if (tipo.equals("C"))
                {
//                        if (ragg.equals("A")) {
//                                vtitoli.add("Area distrettuale");
//                        } else if (ragg.equals("C")) {
//                                vtitoli.add("Comune");
//                        } else if (ragg.equals("P")) {
//                                vtitoli.add("Presidio");
                	
                    if(this.dom_res==null)
                    {
                    	if (ragg.equals("A")) {
                          vtitoli.add("Area distrettuale");
                  } else if (ragg.equals("C")) {
                          vtitoli.add("Comune");
                  } else if (ragg.equals("P")) {
                          vtitoli.add("Presidio");

                    }else if (this.dom_res.equals("D"))
                    {
                    	if (ragg.equals("A")) {
                            vtitoli.add("Area distrettuale di Domicilio ");
                    } else if (ragg.equals("C")) {
                            vtitoli.add("Comune di Domicilio ");
                    }	
                    }else if (this.dom_res.equals("R"))
                        {
                    	if (ragg.equals("A")) {
                            vtitoli.add("Area distrettuale di Residenza ");
                    } else if (ragg.equals("C")) {
                            vtitoli.add("Comune di Residenza ");
                    }	
                        
                        } 	
                	
		}
                } else if (tipo.equals("B"))
                          vtitoli.add("Branca");
                 else if (tipo.equals("O"))
                          vtitoli.add("Operatore");
                vtitoli.add("Prestazione");
		cnt.setGroupTitles(vtitoli);

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

                String  operatore=(String)par.get("op");
                if (!operatore.equals(""))
         		titolo += " - Operatore :"+DecodificaOperatore(dbc,operatore);

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
				System.out.println("FoOreAss.preparaLayout(): "+e1);
			}
		}
	}
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
public int CalcolaEta(ISASRecord dbr ,String  dataFine,ISASConnection dbc)
throws SQLException{
        try
        {
            int eta=-1;
            if (dbr.get("int_cartella")!=null)
            {
               String cartella=""+ dbr.get("int_cartella");
               if (hCartellaEta.get(cartella)==null)
               {
                     String select="SELECT data_nasc FROM cartella where n_cartella="+ cartella;
                     ISASRecord  dbrEta = dbc.readRecord(select);
                     if (dbrEta!=null && dbrEta.get("data_nasc")!=null)
                     {
                         String  mydate=((java.sql.Date)dbrEta.get("data_nasc")).toString();
                          if (!mydate.equals(""))
                            eta=this.ConvertData(mydate, dataFine);
                     }
                }
                else
                      eta=Integer.parseInt((String)hCartellaEta.get(cartella));
                hCartellaEta.put(cartella,""+eta);
            }
            return eta;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una CalcolaEta");
	}
}
private void preparaBody(ISASCursor dbcur,NDOContainer cnt,NDOUtil unt,
                          Hashtable par,ISASConnection dbc )
	throws Exception {

        try {
              Hashtable hAccessi= new Hashtable ();
              Hashtable hCartella=new Hashtable ();

              Hashtable hQuantita= new Hashtable();
              Hashtable hOreAss=new Hashtable();
              Hashtable hUltra65=new Hashtable();
              Hashtable hDesLivello=new Hashtable();
              Hashtable hDesPrest=new Hashtable();

              String tipo=(String)par.get("tipo");
              String tempo=(String)par.get("ore");
              while (dbcur.next())
              {
                      ISASRecord dbr=dbcur.getRecord();
                      String livello="";
                      if (tipo.equals("Z"))
                          livello=""+dbr.get("cod_zona");
                      else if (tipo.equals("D"))
                          livello=""+ dbr.get("cod_distretto");
                      else if (tipo.equals("C"))
                          livello=""+ dbr.get("codice");
                      else if (tipo.equals("B"))
                      {
                          String cod_branca=""+dbr.get("cod_branca");
                          String cod_tippre=""+dbr.get("cod_tippre");
                          livello=cod_branca+"-"+cod_tippre;
                      }
                      else if (tipo.equals("O"))
                          livello=""+ dbr.get("int_cod_oper");

                      String cod_pres=""+dbr.get("prest_cod");

                      int quantita=0;
                      if (hQuantita.get(livello+"|"+cod_pres)!=null)
                         quantita=Integer.parseInt(""+hQuantita.get(livello+"|"+cod_pres));
                      if (dbr.get("pre_numero")!=null)
                             quantita=quantita+Integer.parseInt(""+dbr.get("pre_numero")) ;
                      hQuantita.put(livello+"|"+cod_pres,""+quantita);


                     String anno=""+dbr.get("pre_anno");
                     String contatore=""+dbr.get("pre_contatore");
                     int ore=0;
                     int tot_ore=0;
                     if (hOreAss.get(livello+"|"+cod_pres)!=null)
                          tot_ore=Integer.parseInt(""+hOreAss.get(livello+"|"+cod_pres));
                     if (tempo.equals("int"))
                     {
                         //controllo che non abbia gi�  contato il tempo dell'intervento
                          if (hAccessi.get(anno+"|"+contatore+"|"+cod_pres)==null)
                          {
                                if (dbr.get("int_tempo")!=null)
                                        ore=Integer.parseInt(""+dbr.get("int_tempo"));
                                hAccessi.put(anno+"|"+contatore+"|"+cod_pres,"");
                                //conto le ore relative agli ultra 65
                                //Devo controllare l'eta
                                String dataFine= (String)par.get("data_fine");
                                if (CalcolaEta(dbr,dataFine,dbc)>=65)
                                {
                                  int tot65=0;
                                  if (hUltra65.get(livello+"|"+cod_pres)!=null)
                                      tot65=Integer.parseInt(""+hUltra65.get(livello+"|"+cod_pres));
                                  tot65=tot65+ore;
                                  hUltra65.put(livello+"|"+cod_pres,""+tot65);
                               }
                       }
                     }else{//caso conteggio prestazioni
                           if (dbr.get("tempo_prestaz")!=null)
                           {
                                ore=Integer.parseInt(""+dbr.get("tempo_prestaz"));
                                if (dbr.get("pre_numero")!=null)
                                {
                                     int numer =Integer.parseInt(""+dbr.get("pre_numero"));
                                     ore=ore*numer;
                                    //conto le ore relative agli ultra 65
                                    //Devo controllare l'eta
                                    String dataFine= (String)par.get("data_fine");
                                    if (CalcolaEta(dbr,dataFine,dbc)>=65)
                                    {
                                      int tot65=0;
                                      if (hUltra65.get(livello+"|"+cod_pres)!=null)
                                          tot65=Integer.parseInt(""+hUltra65.get(livello+"|"+cod_pres));
                                      tot65=tot65+ore;
                                      hUltra65.put(livello+"|"+cod_pres,""+tot65);
                                   }
                                }
                           }
                      }
                      tot_ore=tot_ore+ore;
                      hOreAss.put(livello+"|"+cod_pres,""+tot_ore);
                      /*String cartella=""+dbr.get("int_cartella");
                      caso in cui contavo il numero di assistiti ultra 65 -enni
                      Si vuole che si conti le ore di asssitenza e non il numero di assistiti
                      if (hCartella.get(livello+"|"+cod_pres+"|"+cartella)==null){
                          //Devo controllare l'eta
                          String dataFine= (String)par.get("data_fine");
                          if (CalcolaEta(dbr,dataFine,dbc)>=65)
                          {

                             cnt.put(unt.mkPar(livello,cod_pres),
                                       unt.mkPar("ASS"),
                                          new Integer(1) );
                          }
                          hCartella.put(livello+"|"+cod_pres+"|"+cartella,"");
                      }*/


                      hDesLivello.put(livello,Descrizione(dbr,cnt,unt,par,dbc));

                      String prest_des="";
                      if (dbr.get("prest_des")!=null)
                             prest_des=""+dbr.get("prest_des");
                      hDesPrest.put(cod_pres,cod_pres+":" +prest_des);
		}//fine while

                /*Scorro le hash caricate e riempio nDO
                */
//System.out.println("HASHTABLE hQuantita " +hQuantita.toString()) ;
//System.out.println("HASHTABLE hOreAss " +hOreAss.toString());
	System.out.println("FoOreAssEJB.preparaBody(): hQuantita.size()=" +hQuantita.size());
	int conta_cont = hQuantita.size();

              Enumeration enumQuant = hQuantita.keys();
              while (enumQuant.hasMoreElements())
              {
                if (((--conta_cont)%100) == 0) {
                        System.out.println("FoOreAssEJB.preparaBody(): conta_cont=" + conta_cont);
                        Thread.sleep(20);
                }
                  String chiave = "" + enumQuant.nextElement();
                  StringTokenizer st = new StringTokenizer(chiave,"|");
                  String sLivello=st.nextToken();
                  String sCodice=st.nextToken();
                  int num_prest=Integer.parseInt(""+hQuantita.get(chiave));
                  cnt.put(unt.mkPar(sLivello,sCodice),
                        unt.mkPar("QUAN"),
                          new Double(num_prest) );
                  int tempo_minuti=0;
                  if (hOreAss.get(chiave)!=null)
                        tempo_minuti=Integer.parseInt(""+hOreAss.get(chiave));
                  int media=0;
                  if (num_prest>0)
                    media= tempo_minuti/num_prest;
/*                  System.out.println("--------------------------------");
                  System.out.println("CODICE "+sCodice);
                  System.out.println("TEMPO MINUTI "+tempo_minuti);
                  System.out.println("NUMERO PRESTAZIONI "+num_prest);
                  System.out.println("MEDIA"+media);
                  System.out.println("--------------------------------");
*/                  cnt.put(unt.mkPar(sLivello,sCodice),
                        unt.mkPar("MEDIA"),
                          new Double(media) );
                  cnt.put(unt.mkPar(sLivello,sCodice),
                        unt.mkPar("ORE"),
                          new Double(ConvertiOre(tempo_minuti)) );
//                 System.out.println("ORE-->"+ new Double(ConvertiOre(tempo_minuti)));
                  //settaTitoliRiga(cnt,unt,par,dbc);
                if (hDesLivello.get(sLivello)!=null)
                {
                        cnt.setRowTitle(unt.mkPar(sLivello),""+hDesLivello.get(sLivello));
                        hDesLivello.remove(sLivello);
                }
                int tempo_minuti65=0;
                if (hUltra65.get(chiave)!=null)
                      tempo_minuti65=Integer.parseInt(""+hUltra65.get(chiave));
                cnt.put(unt.mkPar(sLivello,sCodice),
                        unt.mkPar("ASS"),
                          new Double(ConvertiOre(tempo_minuti65)) );
                cnt.setRowTitle(unt.mkPar(sLivello,sCodice),""+hDesPrest.get(sCodice));
              }
              cnt.setColTitle(unt.mkPar("QUAN"),"    Quantit�");
              cnt.setColTitle(unt.mkPar("MEDIA"),"   Tempo medio in minuti");
              cnt.setColTitle(unt.mkPar("ORE"),"  Ore di assistenza globale");
              cnt.setColTitle(unt.mkPar("ASS")," Di cui per anziani ultra 65-enni");
	      cnt.hideColTotal(unt.mkPar("MEDIA"));
	      cnt.calculate();

		//cnt.debugPrint();	// DEBUG
	} catch(Exception e) {
		debugMessage("FoOreAss.preparaBody(): "+e);
		throw new SQLException("Errore eseguendo preparaBody()");
	}
}

//private String ConvertiOre(int ore)
private double ConvertiOre(int ore)
{
/*  int tot=0;
  int aiuto=0;
   if(ore<60){
//         tot="0."+ore;
          tot=0;
   }else if (ore>=60){
          int inhash=ore/60;
          aiuto=ore-(inhash*60);
          //tot=inhash+"."+aiuto;
          tot=inhash;
   }
    // return inhash;
   if (aiuto>30)
        tot=tot+1;*/
   double tot=0;
   DecimalFormat df =new DecimalFormat("####0.00");
   tot=ore/60.0;
   String stampa=df.format(tot);
   stampa=stampa.replace(',','.');
   tot=Double.parseDouble(stampa);

   return tot;
}

private String Descrizione(ISASRecord dbr,NDOContainer cnt ,NDOUtil unt,Hashtable par,ISASConnection dbc)
	throws Exception {
      try
      {
            String tipo=(String)par.get("tipo");
            String livello="";
            if (tipo.equals("Z"))
                livello=""+dbr.get("des_zona");
            else if (tipo.equals("D"))
                 livello=""+ dbr.get("des_distretto");
             else if (tipo.equals("C"))
                livello=""+ dbr.get("descrizione");
            else if (tipo.equals("B"))
                livello=""+dbr.get("branca");
             else if (tipo.equals("O"))
            {
               String cod_oper=""+ dbr.get("int_cod_oper");
               String operatore="";
               if (hOperatore.get(cod_oper)==null)
               {
/*                     String select="SELECT  nvl(trim(cognome),'') ||' ' || nvl(trim(nome),'')"+
                                   " operatore FROM operatori where codice='"+ cod_oper+"'";
                     ISASRecord  dbrOp = dbc.readRecord(select);
                     if (dbrOp!=null && dbrOp.get("operatore")!=null)
                             operatore=(String)dbrOp.get("operatore")   ;*/
                  operatore=DecodificaOperatore(dbc,cod_oper);
                   // System.out.println("OPERATORE-->"+operatore);
                     hOperatore.put(cod_oper,operatore);
                }
                else
                      operatore=(String)hOperatore.get(cod_oper);
                livello=operatore;
            }
        return livello;
      }catch(Exception e)
      {
           debugMessage("FoOreAss.Descrizione: "+e);
            throw new SQLException("Errore eseguendo Descrizione()");
     }

}

private String mkSelectAnalitica(ISASConnection dbc, Hashtable par)throws Exception {
     try {
     /*Il tempo nel caso delle prestazioni deve essere preso da intpre
     perch� deve tenere conto dello storico.
     Se hanno cambiato il tempo nelle prestazioni in intpre rimane quello precedente
     */
     ServerUtility su =new ServerUtility();
     String s = "SELECT  "+
		" u.cod_zona,u.des_zona, "+
                " u.des_distretto,u.cod_distretto,"+
                " u.codice ,u.descrizione ,"+
                " p.pre_numero,b.cod_tippre,"+
                " p.pre_anno,p.pre_contatore,p.pre_tempo  tempo_prestaz ,"+
                " i.int_cartella,i.int_tempo,i.int_cod_oper,"+
                " b.descrizione branca,b.codice cod_branca,pz.prest_cod,"+
                " pz.prest_des "+
		" FROM interv i, intpre p,"+
                " "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u, "+
                " prestaz pz,branca b "+
                " WHERE i.int_anno=p.pre_anno "+
                " AND i.int_contatore=p.pre_contatore "+
                " AND p.pre_cod_prest=pz.prest_cod "+
                " AND pz.prest_tipo=b.cod_tippre " +
                " AND pz.prest_branca=b.codice " ;

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

	s = su.addWhere(s, su.REL_AND, "i.int_cod_oper",
		//CJsu.OP_EQ_STR, (String)par.get("oper"));
                su.OP_EQ_STR, (String)par.get("op"));

	String figprof="";
	if(!par.get("figprof").equals("00"))
        	figprof=(String)par.get("figprof");

        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
                su.OP_EQ_STR, figprof);

	String scr = (String)par.get("ragg");
	s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);
//        if (scr!=null && scr.equals("A"))
//          s += " AND i.int_cod_areadis=u.codice ";
//        else if (scr!=null && scr.equals("P"))
//          s += " AND u.codice=i.int_codpres ";
    if(this.dom_res==null)
    {
    	
    if (scr!=null && scr.equals("A"))
      s += " AND i.int_cod_areadis=u.codice ";
    else if (scr!=null && scr.equals("P"))
      s += " AND u.codice=i.int_codpres ";
    }
    
          
    else if (this.dom_res.equals("R"))
    		
    	if (scr!=null && scr.equals("A"))
            s += " AND i.int_cod_areadis=u.codice ";

    		

	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "i.int_data_prest",
		su.OP_GE_NUM,
	formatDate(dbc, (String)par.get("data_inizio")));

        String tipo_prest="";
        if(!par.get("tipo_prest").equals("E")){
          tipo_prest=(String)par.get("tipo_prest");
         s = su.addWhere(s, su.REL_AND, "i.int_ambdom",su.OP_EQ_STR, tipo_prest);
        }
	s = s + " ORDER BY pz.prest_cod";
	debugMessage("FoOreAss.getSelectAnalitica(): "+s);
	return s;
        	} catch(Exception e) {
		debugMessage("FoOreAss.mkSelectAnalitica(): "+e);
		throw(e);
	}
}

private String mkSelectAnaliticaSenzaUbicazioni(ISASConnection dbc, Hashtable par,
                ISASCursor dbcur)throws Exception {
     try {
     /*Il tempo nel caso delle prestazioni deve essere preso da intpre
     perch� deve tenere conto dello storico.
     Se hanno cambiato il tempo nelle prestazioni in intpre rimane quello precedente
     */
     ServerUtility su =new ServerUtility();
     String s = "SELECT  "+
                " d.cod_zona ,z.descrizione_zona des_zona, "+
		" c.codice,c.descrizione, d.cod_distr cod_distretto, "+
		" d.des_distr des_distretto, "+
                " p.pre_numero,b.cod_tippre,"+
                " p.pre_anno,p.pre_contatore,p.pre_tempo tempo_prestaz,"+
                " i.int_cartella,i.int_tempo,i.int_cod_oper,"+
                " b.descrizione branca,b.codice cod_branca,pz.prest_cod,"+
                " pz.prest_des "+
		" FROM interv i, intpre p,"+
                " comuni c ,distretti d,zone z, "+
                " prestaz pz,branca b "+
                " WHERE i.int_anno=p.pre_anno "+
                " AND c.cod_distretto = d.cod_distr "+
		" AND d.cod_zona = z.codice_zona "+
//                " AND i.int_cod_comune=c.codice " +
                " AND i.int_contatore=p.pre_contatore "+
                " AND p.pre_cod_prest=pz.prest_cod "+
                " AND pz.prest_tipo=b.cod_tippre " +
                " AND pz.prest_branca=b.codice " ;
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
	  

	s = su.addWhere(s, su.REL_AND, "i.int_cod_oper",
		su.OP_EQ_STR, (String)par.get("oper"));

        s = su.addWhere(s, su.REL_AND, "d.cod_zona",
		su.OP_EQ_STR, (String)par.get("zona"));
	s = su.addWhere(s, su.REL_AND, "d.cod_distr",
		su.OP_EQ_STR, (String)par.get("distretto"));
	String figprof="";
	if(!par.get("figprof").equals("00"))
        	figprof=(String)par.get("figprof");

        s = su.addWhere(s, su.REL_AND, "i.int_tipo_oper",
                su.OP_EQ_STR, figprof);

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
	debugMessage("FoOreAss.mkSelectAnaliticaSenzaUbicazioni(): "+s);
	return s;
        	} catch(Exception e) {
		debugMessage("FoOreAss.mkSelectAnaliticaSenzaUbicazioni(): "+e);
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
		
		
		
      	s+=" FROM interv i, intpre p,"+
                " prestaz pz,branca b "+
                " WHERE i.int_anno=p.pre_anno "+
                " AND i.int_contatore=p.pre_contatore "+
                " AND p.pre_cod_prest=pz.prest_cod "+
                " AND pz.prest_tipo=b.cod_tippre " +
                " AND pz.prest_branca=b.codice " ;

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
	debugMessage("FoOreAss.mkSelectComuni(): "+s);
	return s;
}

public byte[] query_oreass(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {

	ISASConnection dbc = null;
	boolean done = false;
	debugMessage("FoOreAss.query_oreas(): inizio...");

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
//                String conte = (String)par.get("cont");
		if (dbcur.getDimension() <= 0) {
			debugMessage("FoOreAss.query_oreas(): vuoto");
			cnt.setSubTitle("NESSUNA INFORMAZIONE REPERITA");
		} else {
System.out.println("RECORD TROVATI-->"+dbcur.getDimension() );
	            preparaBody(dbcur,cnt,unt,par,dbc);
		}

		String tipo = ""+par.get("tipo");
		String formato = ""+par.get("formato");
		dbcur.close();

		dbc.close();
		super.close(dbc);
		done=true;
		return StampaNDO(cnt,formato);
	} catch(Exception e) {
		debugMessage("FoOreAss.query_oreas(): "+e);
		throw new SQLException("FoOreAss.query_oreas(): "+e);
	} finally {
		debugMessage("FoOreAss.query_oreas(): ...fine.");
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				System.out.println("FoOreAss.query_oreas(): "+e1);
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

private byte[] StampaNDO(NDOContainer cnt, String formato)
throws Exception {

	try {
		cnt.colSort();
		cnt.rowSort();
		NDOPrinter prt = new NDOPrinter();

        	prt.addContainer(cnt,true,false,1,0,true,false);

		System.out.println("FINE STAMPA" +currentTime());
		return prt.getDocument(Integer.parseInt(formato));
	} catch(Exception e) {
		debugMessage("FoOreAss.StampaNDO(): "+e);
		throw new SQLException("Errore eseguendo StampaNDO()");
	}
}
private String DecodificaOperatore(ISASConnection dbc,String operat){
  String ret = "";
  try {
    if (operat!=null){
      if (operat.equals(""))
        ret = "";
      else{
       String select="SELECT  nvl(trim(cognome),'') ||' ' || nvl(trim(nome),'')"+
                     " operatore FROM operatori where codice='"+ operat+"'";
       ISASRecord  dbrOp = dbc.readRecord(select);
       if (dbrOp!=null && dbrOp.get("operatore")!=null)
               ret=(String)dbrOp.get("operatore");
      }
    }
  } catch (Exception ex) { }
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

}	// End of FoOreAss class

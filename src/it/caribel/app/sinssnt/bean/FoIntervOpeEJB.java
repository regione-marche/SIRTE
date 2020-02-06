package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 19/09/2003 - EJB di connessione alla procedura SINS Tabella FoIntervOpe
//
// Francesco Greco
//
// 26/03/2004 Giulia Brogi
// introdotta la possibilit� di lanciare la stampa per pi� operatori
// 07/03/2007 Jessica Caccavale
// aggiunto il metodo mkBodySaltoCartella che, se richiesto a video, fa il
// troncamento sulla cartella e scrive il totale in una pagina a parte
// ==========================================================================

import java.util.*;
import java.sql.*;
import java.io.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.util.*;

public class FoIntervOpeEJB extends SINSSNTConnectionEJB {

public int tot_num_ass=0;
public int tot_ore_ass=0;
public int tot_orego_ass=0;

public int tot_num_int_riep=0;
public int tot_ore_riep=0;
public int tot_orego_riep=0;
public int tot_op_riep=0;
String dom_res;
String dr;
public int tot_num_figprof=0;
public int tot_ore_figprof=0;
public int tot_orego_figprof=0;
private int tempo_prep=0;

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

/**
* restituisce un parametro come stringa
*/
private String getStringField(ISASRecord dbr, String f) {
	try {
		return (dbr.get(f)).toString();
	} catch(Exception e) {
		debugMessage("getStringField("+dbr+", "+f+"): "+e);
		return "";
	}
}

/**
* restituisce un campo data come stringa
*/
private String getDateField(ISASRecord dbr, String f) {
	try {
		if(dbr.get(f)==null)
			return "";
		String d = ((java.sql.Date)dbr.get(f)).toString();
		d = d.substring(8,10)+"/"+d.substring(5,7)+"/"+d.substring(0,4);
		return d;
	} catch(Exception e) {
		debugMessage("getStringField("+dbr+", "+f+"): "+e);
		return "";
	}
}

/**
* restituisce la decodifica della figura professionale
*/
private String getFiguraProfessionale(String tipo) {
	String fp = "";
	try {
		if (tipo.equals("00"))
			fp = "TUTTE LE FIGURE PROFESSIONALI";
		else if (tipo.equals("01"))
			fp = "Assistente sociale";
		else if (tipo.equals("02"))
			fp = "Infermiere";
		else if (tipo.equals("03"))
			fp = "Medico";
		else if (tipo.equals("04"))
			fp = "Fisioterapista";
		else if (tipo.equals("52"))
			fp = "Oncologo";
		else if (tipo.equals("98"))
			fp = "Medico specialista";
		else
			fp = "FIGURA PROFESSIONALE NON VALIDA";
	} catch(Exception e) {
		fp = "FIGURA PROFESSIONALE ERRATA";
	}
	return fp;
}

//minerba 06/03/2012
private String getQualifica(Object Oqualifica) {
	String fp = "";
	try {
              String qualifica=(String)Oqualifica;
              if (qualifica.equals("TUTTO"))
					fp = " TUTTE LE QUALIFICHE ";
				else if (qualifica.equals("F"))
					fp = "LOGOPEDISTA ";
				else if (qualifica.equals("H"))
					fp = "PSICOLOGO ";
				else if (qualifica.equals("1"))
					fp = "ASSISTENTE SOCIALE ";
				else if (qualifica.equals("2"))
					fp = "INFERMIERE ";
				else if (qualifica.equals("A"))
					fp = "OSA ";
				else if (qualifica.equals("O"))
					fp = "OTA ";
				else if (qualifica.equals("4"))
					fp = "FISIATRA ";
				else if (qualifica.equals("3"))
					fp = "FISIOTERAPISTA ";
				else if (qualifica.equals("5"))
					fp = "MEDICO ";
				else if (qualifica.equals("6"))
					fp = "AMMINISTRATIVO 1.LIV ";
				else if (qualifica.equals("7"))
					fp = "AMMINISTRATIVO 2.LIV ";
				else if (qualifica.equals("8"))
					fp = "CAPO SALA ";
				else if (qualifica.equals("9"))
					fp = "O.S.S. ";
				else if (qualifica.equals("G"))
					fp = "MED. MEDICINA GENER. ";		
				else
					fp = "QUALIFICA NON VALIDA";
	} catch(Exception e) {
		fp = "QUALIFICA ERRATA";
	}
	return fp;
}//fine minerba


public int ConvertData (String dataold,String datanew) {
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


public FoIntervOpeEJB() {}

/**
* stampa sintetica-analitica: sezione layout del documento
*/
private void mkLayout(ISASConnection dbc,
	Hashtable par, mergeDocument doc) {

	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();
	String cognome="";
	String nome="";
	String tipo="";

	ht.put("#txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));

        if(par.get("tipo_accert").equals("E"))
          ht.put("#tipo_accert#","");
        else if(par.get("tipo_accert").equals("A"))
          ht.put("#tipo_accert#","AMBULATORIALE");
        else if(par.get("tipo_accert").equals("D"))
          ht.put("#tipo_accert#","DOMICILIARE");

	ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

try{
		String sel = "SELECT cognome,nome,tipo FROM operatori "+
			"WHERE codice = '"+(String)par.get("codope")+"'";
		debugMessage("FoIntervOpeEJB.mkLayout(): "+sel);
		ISASRecord dbcom = dbc.readRecord(sel);
		cognome=(String)dbcom.get("cognome");
		nome=(String)dbcom.get("nome");
		tipo=(String)dbcom.get("tipo");
} catch(Exception e) {
	debugMessage("getOperatore("+dbc+", "+cognome+"): "+e);
}
	ht.put("#operatore#", cognome + " " + nome);

        if(tipo.equals("") && !par.get("figprof").equals("00"))
              tipo=(String)par.get("figprof");
    String qualifica = (String) par.get("qualifica");    
	ht.put("#tipo_ope#",  getFiguraProfessionale(tipo));
	ht.put("#qualifica#", "Qualifica: " + getQualifica(qualifica));

//gb 25/06/07 *******
	if (!((String)par.get("zona")).equals(""))
	   {
	   String zona = (String)par.get("zona");
	   ht.put("#zona#",  decodifica("zone","codice_zona",zona,"descrizione_zona",dbc));
	   }
	else
	   ht.put("#zona#",  " ");
	if (!((String)par.get("distretto")).equals(""))
	   {
	   String distretto = (String)par.get("distretto");
	   ht.put("#distretto#",  decodifica("distretti","cod_distr",distretto,"des_distr",dbc));
	   }
	else
	   ht.put("#distretto#",  " ");

	String pca = "";
	if (!((String)par.get("pca")).equals(""))
	   pca = (String)par.get("pca");
	ht.put("#pca#",  " ");
	if (((String)par.get("ragg")).equals("C"))
	   {
           ht.put("#titolo_pca#",  "COMUNE");
	   if (!pca.equals(""))
	      ht.put("#pca#",  decodifica("comuni", "codice", pca, "descrizione", dbc));
	   }
	else if (((String)par.get("ragg")).equals("A"))
	   {
	   ht.put("#titolo_pca#",  "AREA DISTRETT.");
	   if (!pca.equals(""))
	      ht.put("#pca#",  decodifica("areadis", "codice", pca, "descrizione", dbc));
	   }
	//M.Minerba 25/02/2013 per Pistoia
	else if (((String)par.get("ragg")).equals("P"))
	   {
	   ht.put("#titolo_pca#",  "PRESIDIO");
	   if (!pca.equals(""))
	      ht.put("#pca#",  decodificaP("presidi", "codpres", pca, "despres", dbc));
	   }
	//fine M.Minerba 25/02/2013 per Pistoia
//gb 25/06/07: fine *******

	doc.writeSostituisci("layout",ht);
}


    private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
		//System.out.println("FoIntervOpeEJB.decodifica(): DEBUG ["+mysel+"].");
                ISASRecord dbtxt = dbc.readRecord(mysel);
		return ((String)dbtxt.get("descrizione"));
	} catch (Exception ex) {
		return " ";
	}
}
    //M.Minerba 25/02/2013 per Pistoia
    private String decodificaP(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
    	Hashtable htxt = new Hashtable();
    	if (val_codice==null) return " ";
            try {
    		String mysel = "SELECT " + descrizione + " despres FROM " + tabella + " WHERE "+
    			nome_cod +" ='" + val_codice.toString() + "'";
    		//System.out.println("FoIntervOpeEJB.decodifica(): DEBUG ["+mysel+"].");
                    ISASRecord dbtxt = dbc.readRecord(mysel);
    		return ((String)dbtxt.get("despres"));
    	} catch (Exception ex) {
    		return " ";
    	}
    }
  //fine M.Minerba 25/02/2013 per Pistoia

/**
* stampa sintetica: sezione riga tabella
*/
private void faiRigaAssistito(ISASRecord dbr, mergeDocument doc) {
String int_contatto="";
	Hashtable ht = new Hashtable();
	try {
		ht.put("#nome_ass#", (String)dbr.get("cogn")+" "+
			(String)dbr.get("nomeut"));
		ht.put("#data_nasc#", "("+getDateField(dbr, "data_nasc")+")");
		if (dbr.get("int_cartella")==null)
			ht.put("#n_cartella#", " ");
		else
			ht.put("#n_cartella#",
				((Integer)dbr.get("int_cartella")).toString());

	} catch(Exception e) {
		ht.put("#nome_ass#", "*** ERRORE ***");
		ht.put("#data_nasc#"," ");
		ht.put("#n_cartella#", " ");
	}
	doc.writeSostituisci("rigaAssistito", ht);
}

/**
* stampa sintetica: sezione riga tabella
*/
private void faiRigaIntervento(ISASRecord dbr, mergeDocument doc) {
	Hashtable ht = new Hashtable();
	try {
		ht.put("#contatore_int#", ((Integer)dbr.get("int_contatore")).toString());
		ht.put("#data_int#", getDateField(dbr, "int_data_prest"));
		ht.put("#anno_int#",(String)dbr.get("int_anno"));
		ht.put("#n_interv#", "1");
	if(dbr.get("int_tempo")!=null)
		//ht.put("#ore#", convertiOre(((Integer)dbr.get("int_tempo")).intValue())+tempo_prep);	RB 10.08.2010
		ht.put("#ore#", convertiOre(((Integer)dbr.get("int_tempo")).intValue()+tempo_prep));
	else
		ht.put("#ore#", "0,0");
	if(dbr.get("int_tempogo")!=null)
		ht.put("#ore-go#", convertiOre(((Integer)dbr.get("int_tempogo")).intValue()));
	else
		ht.put("#ore-go#", "0,0");

	} catch(Exception e) {
		ht.put("#contatore_int#", "*** ERRORE ***");
		ht.put("#data_int#"," ");
		ht.put("#anno_int#", " ");
		ht.put("#n_interv#", "");
		ht.put("#ore#", " ");
                ht.put("#ore-go#", " ");
	}

	try {
	  if(dbr.get("int_tempo")!=null){
	    tot_ore_ass=tot_ore_ass+((Integer)dbr.get("int_tempo")).intValue()+tempo_prep;
	    tot_ore_figprof=tot_ore_figprof+((Integer)dbr.get("int_tempo")).intValue()+tempo_prep;
          }
          if(dbr.get("int_tempogo")!=null){
	    //tot_orego_ass=tot_orego_ass+((Integer)dbr.get("int_tempogo")).intValue()+tempo_prep;	RB 10.08.2010
	    //tot_orego_figprof=tot_orego_figprof+((Integer)dbr.get("int_tempogo")).intValue()+tempo_prep;	RB 10.08.2010
	    tot_orego_ass=tot_orego_ass+((Integer)dbr.get("int_tempogo")).intValue();
	    tot_orego_figprof=tot_orego_figprof+((Integer)dbr.get("int_tempogo")).intValue();
	  }
	} catch(Exception e) {System.out.println("ERRORE IN faiRigaIntervento()");}

	tot_num_ass++;
	tot_num_int_riep++;
	doc.writeSostituisci("rigaIntervento", ht);
}

/**
* stampa sintetica: sezione inizio tabella
*/
private void mkInizioTabella(mergeDocument doc, ISASRecord dbr) {
	Hashtable ht = new Hashtable();
        String cognome="";        String nome="";
        String cur_oper="";
  try {
        if(dbr.get("int_tipo_oper")!=null)
          cur_oper=(String)dbr.get("int_tipo_oper");
        if(dbr.get("opcogn")!=null)
          cognome =(String)dbr.get("opcogn");
        if(dbr.get("opnome")!=null)
          nome =(String)dbr.get("opnome");

	ht.put("#tipo_figprof#", getFiguraProfessionale(cur_oper));
        ht.put("#nome_figprof#", cognome + " " + nome);

  } catch(Exception e) {
		ht.put("#tipo_figprof#", "*** ERRORE ***");
                ht.put("#nome_figprof#", " " );
  }
	doc.writeSostituisci("operatore", ht);
	doc.write("iniziotab");
}



/**
* stampa sintetica: sezione fine tabella
*/
private void mkFineTabella(mergeDocument doc) {
System.out.println("----->Scrivo finetab");
	doc.write("finetab");

}

private void mkBody(mergeDocument md, ISASCursor dbcur, Hashtable par,
	ISASConnection dbc) throws Exception {
	boolean first_time = true;

	String old_cart = "*",   cart_ass = "";
	String old_oper = "*", cur_oper = "";
	String old_ass = "*",   cur_ass = "";
	String old_conta = "*", cur_conta = "";
	try {
	  String data_inizio = "";
	  String data_fine = "";
          int orapar=0;
          int oregen=0;
          int ora=0;
          ServerUtility su =new ServerUtility();
          data_inizio=(String)(par.get("data_inizio"));
          data_fine=(String)(par.get("data_fine"));
	  while (dbcur.next()){
	    ISASRecord dbr=dbcur.getRecord();
	    cart_ass=((Integer)dbr.get("int_cartella")).toString();
	    cur_oper = (String)dbr.get("int_cod_oper");
            if(! old_oper.equals(cur_oper))
               tot_op_riep++;

	    if (! old_oper.equals(cur_oper)){
	      if (first_time) {
		first_time = ! first_time;
	      } else {
		Hashtable htAss = new Hashtable();
		htAss.put("#tot_interv#", ""+tot_num_ass);
		htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
                htAss.put("#tot_orego#", convertiOre(tot_orego_ass));
		md.writeSostituisci("rigaTotAss", htAss);

		tot_num_ass=0;
		tot_ore_ass=0;

		Hashtable htotfp = new Hashtable();
		htotfp.put("#totali_interv#", ""+tot_num_figprof);
		htotfp.put("#totali_ore#", convertiOre(tot_ore_figprof));
		htotfp.put("#totali_orego#", convertiOre(tot_orego_figprof));
		md.writeSostituisci("rigaTotaliAssxFigProf", htotfp);
		tot_ore_riep=tot_ore_riep+tot_ore_figprof;
		tot_num_figprof=0;
		tot_ore_figprof=0;
		mkFineTabella(md);
		md.write("taglia");
	      }
	      mkInizioTabella(md,dbr);
	      faiRigaAssistito(dbr, md);
	    }else if(!cart_ass.equals(old_cart)){
	      if(!old_cart.equals("*")){
		Hashtable htAss = new Hashtable();
		htAss.put("#tot_interv#", ""+tot_num_ass);
		htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
                htAss.put("#tot_orego#", convertiOre(tot_orego_ass));
		md.writeSostituisci("rigaTotAss", htAss);

		tot_num_ass=0;
		tot_ore_ass=0;
		tot_orego_ass=0;
		faiRigaAssistito(dbr, md);
	      }
	    }

	    faiRigaIntervento(dbr, md);
	    if(first_time){
	      first_time=false;
	    }

	    tot_num_figprof++;

	    old_oper = cur_oper;
	    old_cart=cart_ass;

	  }//end while
	  Hashtable htAss = new Hashtable();
	  htAss.put("#tot_interv#", ""+tot_num_ass);
	  htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
          htAss.put("#tot_orego#", convertiOre(tot_orego_ass));
	  md.writeSostituisci("rigaTotAss", htAss);

	  //totale per l'ultimo operatore
	  Hashtable htotfp = new Hashtable();
	  htotfp.put("#totali_interv#", ""+tot_num_figprof);
	  htotfp.put("#totali_ore#", convertiOre(tot_ore_figprof));
          htotfp.put("#totali_orego#", convertiOre(tot_orego_figprof));
	  md.writeSostituisci("rigaTotaliAssxFigProf", htotfp);
	  tot_ore_riep=tot_ore_riep+tot_ore_figprof;
	  tot_num_figprof=0;
	  tot_ore_figprof=0;

	} catch (Exception e) {
		debugMessage("FoIAssEle_2EJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}
/*Lasciato anche il troncamento su operatore nel caso in cui non vogliano lanciare
la stampa per un solo operatore*/
private void mkBodySaltoCartella(mergeDocument md, ISASCursor dbcur, Hashtable par,
	ISASConnection dbc) throws Exception {
	boolean first_time = true;

	String old_cart = "*",   cart_ass = "";
	String old_oper = "*", cur_oper = "";
	String old_ass = "*",   cur_ass = "";
	String old_conta = "*", cur_conta = "";
	try {
	  String data_inizio = "";
	  String data_fine = "";
          int orapar=0;
          int oregen=0;
          int ora=0;
          ServerUtility su =new ServerUtility();
          data_inizio=(String)(par.get("data_inizio"));
          data_fine=(String)(par.get("data_fine"));
	  while (dbcur.next()){
	    ISASRecord dbr=dbcur.getRecord();
	    cart_ass=((Integer)dbr.get("int_cartella")).toString();
	    cur_oper = (String)dbr.get("int_cod_oper");
            if(! old_oper.equals(cur_oper))
               tot_op_riep++;

	    if (! old_oper.equals(cur_oper)){
	      if (first_time) {
		first_time = ! first_time;
	      } else {
		Hashtable htAss = new Hashtable();
		htAss.put("#tot_interv#", ""+tot_num_ass);
		htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
		htAss.put("#tot_orego#", convertiOre(tot_orego_ass));
		md.writeSostituisci("rigaTotAss", htAss);

		tot_num_ass=0;
		tot_ore_ass=0;

		/*CJ 03/07/2007
                Hashtable htotfp = new Hashtable();
		htotfp.put("#totali_interv#", ""+tot_num_figprof);
		htotfp.put("#totali_ore#", convertiOre(tot_ore_figprof));
		md.writeSostituisci("rigaTotaliAssxFigProf", htotfp);
                FINE CJ 03/07/2007*/
		tot_ore_riep=tot_ore_riep+tot_ore_figprof;
		tot_num_figprof=0;
		tot_ore_figprof=0;
		mkFineTabella(md);
		md.write("taglia");
	      }
	      mkInizioTabella(md,dbr);
	      faiRigaAssistito(dbr, md);
	    }else if(!cart_ass.equals(old_cart)){
	      if(!old_cart.equals("*")){
		Hashtable htAss = new Hashtable();
		htAss.put("#tot_interv#", ""+tot_num_ass);
		htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
                htAss.put("#tot_orego#", convertiOre(tot_orego_ass));
		md.writeSostituisci("rigaTotAss", htAss);

		tot_num_ass=0;
		tot_ore_ass=0;
                /* CJ 07/03/2007*/
                mkFineTabella(md);
		md.write("taglia");
                mkInizioTabella(md,dbr);
                /* fine CJ 07/03/2007*/
		faiRigaAssistito(dbr, md);
	      }
	    }

	    faiRigaIntervento(dbr, md);
	    if(first_time){
	      first_time=false;
	    }

	    tot_num_figprof++;

	    old_oper = cur_oper;
	    old_cart=cart_ass;

	  }//end while
	  Hashtable htAss = new Hashtable();
	  htAss.put("#tot_interv#", ""+tot_num_ass);
	  htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
          htAss.put("#tot_orego#", convertiOre(tot_orego_ass));
	  md.writeSostituisci("rigaTotAss", htAss);

	  //totale per l'ultimo operatore
	  /*CJ 07/03/2007
          Hashtable htotfp = new Hashtable();
	  htotfp.put("#totali_interv#", ""+tot_num_figprof);
	  htotfp.put("#totali_ore#", convertiOre(tot_ore_figprof));
	  md.writeSostituisci("rigaTotaliAssxFigProf", htotfp);
          FINE CJ 07/03/2007*/

	  tot_ore_riep=tot_ore_riep+tot_ore_figprof;
	  tot_num_figprof=0;
	  tot_ore_figprof=0;

	} catch (Exception e) {
		debugMessage("FoIAssEle_2EJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}


private String convertiOre(int ore){
  String tot="";
   if(ore<60){
         tot="0,"+ore;
   }else if (ore>=60){
          int inhash=ore/60;
          int aiuto=ore-(inhash*60);
          tot=inhash+","+aiuto;
   }
   return tot;
}

/**
* restituisce la select per la stampa sintetica.
*/

private String getSelect(ISASConnection dbc, Hashtable par) {
	String s = "SELECT DISTINCT "+
		"r.int_cartella, r.int_cod_oper, r.int_contatto, r.int_tipo_oper, "+
		"r.int_anno,r.int_contatore, r.int_data_prest, r.int_tempo, r.int_tempogo, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"o.cognome opcogn, o.nome opnome "+
//gb 22/06/07		"FROM interv r, cartella c, operatori o";
		"FROM interv r, cartella c, operatori o, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u"; //gb 22/06/07
	try {
		String w = getSelectParteWhere(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
	} catch(Exception e) {
		debugMessage("FoIntervOpeEJB.getSelect(): "+e);
		e.printStackTrace();
	}
	//JESSY 03/05s = s + " ORDER BY o.cognome, o.nome,r.int_data_prest,r.int_anno,cogn,nomeut,r.int_contatto";
//gb 22/06/07        s = s + " ORDER BY o.cognome, o.nome,r.int_cartella,r.int_anno,cogn,nomeut,r.int_contatto";
        s = s + " ORDER BY o.cognome, o.nome, r.int_cartella, r.int_anno, cogn, nomeut, r.int_contatore";

        debugMessage("FoIntervOpeEJB.getSelect(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhere(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();

//26/01/2004 jessy inserito filtro flag tipo accertamento: domiciliare o ambulatoriale
//se � E implica senza filtro
String tipo_accert=(String)par.get("tipo_accert");

	String s = su.addWhere("", su.REL_AND, "r.int_data_prest",
		su.OP_GE_NUM,
		formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	s = su.addWhere(s, su.REL_AND, "r.int_cod_oper",
		su.OP_EQ_STR,
		 (String)par.get("codope"));
        String figprof="";
        if(!par.get("figprof").equals("00"))
              figprof=(String)par.get("figprof");
              s = su.addWhere(s, su.REL_AND, "r.int_tipo_oper",
                      su.OP_EQ_STR, figprof);

//gb 22/06/07 *******
       String zona = (String)par.get("zona");//Zona
       s = su.addWhere(s, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, zona);
       String distretto = (String)par.get("distretto");//Distretto
       s = su.addWhere(s, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, distretto);
       String pca = (String)par.get("pca");//Comune
       s = su.addWhere(s, su.REL_AND, "u.codice", su.OP_EQ_STR, pca);

       String ragg = (String)par.get("ragg");//RAGGRUPPAMENTO
       s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
       
       String tppres = (String)par.get("tppres");//TIPO PRESIDIO
       System.out.println ("TIPO PRESIDIO" + tppres);

//      if (ragg!=null)
//         {
//         if (ragg.equals("C"))
//            s += " AND r.int_cod_comune=u.codice ";
//         else if ( ragg.equals("A"))
//            s += " AND r.int_cod_areadis=u.codice ";
//         else if (ragg.equals("P"))
//            s += " AND r.int_codpres=u.codice ";
//         }
       
     //Minerba 06/03/2013		
	   String qualifica = (String) par.get("qualifica");
		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
			s +=" AND r.int_qual_oper='"+qualifica+"'";
		}//fine Minerba 06/03/2013

       if(this.dom_res==null)
       {

           if (ragg.equals("C"))
             s += " AND r.int_cod_comune=u.codice ";
           else if ( ragg.equals("A"))
             s += " AND r.int_cod_areadis=u.codice ";
           //M.Minerba 27/02/2013 per Pistoia
           else if (ragg.equals("P")){
        	   if (tppres.equals("C")){
        		   s += " AND r.int_cod_presidio_sk=u.codice ";
        	   System.out.println ("C" );
        	   }
        	   else if (tppres.equals("A")){
        		   s += " AND r.int_codpres=u.codice ";
        	   System.out.println ("A" );
        	   }
        	   else {
        		   s += " AND r.int_codpres=u.codice ";
        	   System.out.println ("CA" );
        	   }
           } //fine M.Minerba 27/02/2013 per Pistoia
        	    
             
       }
       else if (this.dom_res.equals("D"))
       		{
       	if (ragg.equals("C"))
           s += " AND r.int_cod_comune=u.codice ";
           else if ( ragg.equals("A"))
             s += " AND r.int_cod_areadis=u.codice ";
      //M.Minerba 27/02/2013 per Pistoia
           else if (ragg.equals("P")){
        	   if (tppres.equals("C")){
        		   s += " AND r.int_cod_presidio_sk=u.codice ";
        	   System.out.println ("C" );
        	   }
        	   else if (tppres.equals("A")){
        		   s += " AND r.int_codpres=u.codice ";
        	   System.out.println ("A" );
        	   }
        	   else {
        		   s += " AND r.int_codpres=u.codice ";
        	   System.out.println ("CA" );
        	   }
           } //fine M.Minerba 27/02/2013 per Pistoia
          
             }
       else if (this.dom_res.equals("R"))
       		{
       	if (ragg.equals("C"))
           s += " AND r.int_cod_res_comune=u.codice ";
           else if ( ragg.equals("A"))
             s += " AND r.int_cod_res_areadis=u.codice ";
      //M.Minerba 27/02/2013 per Pistoia
           else if (ragg.equals("P")){
        	   if (tppres.equals("C")){
        		   s += " AND r.int_cod_presidio_sk=u.codice ";
        	   System.out.println ("C" );
        	   }
        	   else if (tppres.equals("A")){
        		   s += " AND r.int_codpres=u.codice ";
        	   System.out.println ("A" );
        	   }
        	   else {
        		   s += " AND r.int_codpres=u.codice ";
        	   System.out.println ("CA" );
        	   }
           } //fine M.Minerba 27/02/2013 per Pistoia
           
       		}

//gb 22/06/07: fine *******

        if(!tipo_accert.equals("E")){
                s = su.addWhere(s, su.REL_AND, "r.int_ambdom",
                        su.OP_EQ_STR, tipo_accert);
        }

	if (! s.equals("")) s += " AND ";
	s += " r.int_cartella = c.n_cartella AND r.int_cod_oper = o.codice ";
        return s;
}

public byte[] query_intervope(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {

	System.out.println("FoIntervOpeEJB.query_intervope(): DEBUG inizio...");
	boolean done=false;
	ISASConnection dbc=null;
        ServerUtility su =new ServerUtility();

        String data_inizio = "";
        String data_fine = "";
        String tp = "";
	try {


		if (par.get("tp")!=null)
                tp= (String)par.get("tp");
                this.tempo_prep = Integer.parseInt(tp);
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
                myLogin lg = new myLogin();
                lg.put(utente,passwd);
                dbc = super.logIn(lg);

	        System.out.println("FoIntervOpeEJB.query_intervope()");

		ISASCursor dbcur=dbc.startCursor(getSelect(dbc,par));
		mkLayout(dbc, par, eve);

		if (dbcur == null) {
			eve.write("messaggio");
			eve.write("finale");
			System.out.println("FoIntervOpeEJB.query_IntervOpe(): "+
				"cursore non valido");
		} else	{
			if (dbcur.getDimension() <= 0) {
				eve.write("messaggio");
				eve.write("finale");
			}else{
                          if(((String)par.get("cambio")).equals("N"))
				mkBody(eve, dbcur,par,dbc);
                          else
                                mkBodySaltoCartella(eve, dbcur,par,dbc);
                          mkFineTabella(eve);
                          Hashtable hriep = new Hashtable();
                          hriep.put("#tot_num_int#", ""+tot_num_int_riep);
                          hriep.put("#tot_ore_imp#", convertiOre(tot_ore_riep));
                          if(((String)par.get("cambio")).equals("S"))
                            eve.write("taglia");
                          eve.writeSostituisci("riepilogo", hriep);
                          eve.write("finale");
			}	// fine if dbcur.getDimension()
		}	// fine if dbcur
		dbcur.close();
		eve.close();
		//System.out.println("FoIntervOpeEJB.query_IntervOpe(): DEBUG "+
		//	"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
                System.out.println(eve.get());
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("FoIntervOpeEJB.query_intervope(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				System.out.println("FoIntervOpeEJB.query_intervope(): "+e1);
			}
		}
	}
}	// End of query_intervope() method

}	// End of FoIntervOpe class

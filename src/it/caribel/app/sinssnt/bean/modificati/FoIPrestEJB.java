package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 08/02/2002 - EJB di connessione alla procedura SINS Tabella FoIPrest
//
//
//
// ==========================================================================

import java.util.*;
import java.sql.*;
import java.io.*;

import it.pisa.caribel.gprs2.FileMaker;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.util.*;

public class FoIPrestEJB extends SINSSNTConnectionEJB {

	// 28/11/06 m.: aggiunto operatore ONCOLOGO (tipo=52).
	// 27/11/06 m.: sostituito campi "patologie" con "diagnosi" -> NON esiste piu'
	// il criterio sul "n_contatto" -> modificato SELECT in metodo "mkContatto()".
        // 28/02/07 CJ : aggiunto il tipo operatore del medico specialista
	String dom_res;
	String dr;
public int tot_num_ass=0;
public int tot_ore_ass=0;

public int tot_num_figprof=0;
public int tot_ore_figprof=0;

public int tot_num_int_riep=0;
public int tot_ore_riep=0;
public int tot_op_riep=0;

private String data_inizio="";
private String data_fine="";
private String raggruppax="";

String tipo_accert="";
boolean inPiemonte = false;
it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
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
		else if (tipo.equals("52"))// 28/11/06
			fp = "Oncologo";
		else if (tipo.equals("98"))
			fp = "Medico specialista";
		else
			fp = "";
	} catch(Exception e) {
		fp = "FIGURA PROFESSIONALE ERRATA";
	}
	return fp;
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
            System.out.println ("QUALIFICA1" +fp);
            
	} catch(Exception e) {
		fp = "QUALIFICA ERRATA";
	}
	return fp;
}//fine minerba

//private String faiRaggruppamento(String tipo) {
//	String raggruppa="";
//
//	if(tipo.trim().equals("C"))
//		raggruppa=" Comune ";
//	else if(tipo.equals("A"))
//		raggruppa=" Area ";
//	else if(tipo.equals("P"))
//		raggruppa=" Presidio ";
//	else
//     		raggruppa="TIPO NON VALIDO";
//	return raggruppa;
//}

/**
* restituisce il campo della tabella contsan relativo alla figura professionale in atto
*/
private String scegliDataAxFigProf(ISASRecord dbr, String tipo) {
	String dataA = "";
//System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+dbr);
	try {
		if (tipo.equals("01"))
//gb 21/06/07			dataA=getDateField(dbr, "data_sociale");
			dataA=getDateField(dbr, "ap_data_apertura"); //gb 21/06/07
		else if (tipo.equals("02"))
//gb 21/06/07			dataA=getDateField(dbr, "data_infer");
			dataA=getDateField(dbr, "ski_data_apertura"); //gb 21/06/07
		else if (tipo.equals("03"))
//gb 21/06/07			dataA=getDateField(dbr, "data_medico");
			dataA=getDateField(dbr, "skm_data_apertura"); //gb 21/06/07
		else if (tipo.equals("04"))
//gb 21/06/07			dataA=getDateField(dbr, "data_fisiot");
			dataA=getDateField(dbr, "skf_data"); //gb 21/06/07
		else if (tipo.equals("52"))// 28/11/06
//gb 21/06/07			dataA=getDateField(dbr, "data_ostetr");
			dataA=getDateField(dbr, "skm_data_apertura"); //gb 21/06/07
		else
			dataA = " ";
	} catch(Exception e) {
		dataA = " ";
		System.out.println("Errore in scegliDataAxFigProf()");
	}
	return dataA;
}

/**
* restituisce il campo della tabella contsan relativo alla figura professionale in atto
*/
private String scegliDataCxFigProf(ISASRecord dbr, String tipo) {
	String dataC = "";
	try {
		if (tipo.equals("01"))
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_sociale");
			dataC=getDateField(dbr, "ap_data_chiusura"); //gb 21/06/07
		else if (tipo.equals("02"))
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_infer");
			dataC=getDateField(dbr, "ski_data_uscita"); //gb 21/06/07
		else if (tipo.equals("03"))
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_medico");
			dataC=getDateField(dbr, "skm_data_chiusura"); //gb 21/06/07
		else if (tipo.equals("04"))
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_fisiot");
			dataC=getDateField(dbr, "skf_data_chiusura"); //gb 21/06/07
		else if (tipo.equals("52")) // 28/11/06
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_ostetr");
			dataC=getDateField(dbr, "skm_data_chiusura");
		else
			dataC = " ";
	} catch(Exception e) {
		dataC = " ";
		System.out.println("Errore in scegliDataCxFigProf()");
	}
	return dataC;
}

public String getTipocuraFld(ISASRecord dbr, String tipo)
{
String tipoC = "";
	try {
		if (tipo.equals("02"))
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_infer");
			tipoC=getStringField(dbr, "ski_tipocura"); //gb 21/06/07
		else if (tipo.equals("03"))
//gb 21/06/07			dataC=getDateField(dbr, "data_chius_medico");
			tipoC=getStringField(dbr, "skm_tipocura"); //gb 21/06/07

	} catch(Exception e) {
		tipoC = " ";
		System.out.println("Errore in getTipocuraFld()");
	}
	return tipoC;
}

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


public FoIPrestEJB() {}

private String codice_usl="";
private String codice_regione="";
private static final String mioNome = "1-FoIPrestEJB.";

/**
* stampa sintetica-analitica: sezione layout del documento
*/
private void mkLayout(ISASConnection dbc,
	Hashtable par, mergeDocument doc) throws java.lang.Exception{

	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();

	ht.put("#txt#", getConfStringField(dbc, "SINS",
		"ragione_sociale", "conf_txt"));
	//if(par.get("tipo_accert").equals("E"))
          ht.put("#tipo_accert#","");
       // else if(par.get("tipo_accert").equals("A"))
         // ht.put("#tipo_accert#","AMBULATORIALE");
        //else if(par.get("tipo_accert").equals("D"))
         // ht.put("#tipo_accert#","DOMICILIARE");


        ht.put("#data_inizio#", getStringDate(par, "data_inizio"));
	ht.put("#data_fine#", getStringDate(par, "data_fine"));
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	ht.put("#fig_prof#",
		getFiguraProfessionale(
			(String)par.get("figprof")).toUpperCase());
	//minerba 06/03/2013
		System.out.println ("QUALIFICA" + getQualifica(
				(String)par.get("qualifica")).toUpperCase());
			ht.put("#qualifica#",
					getQualifica(
							(String)par.get("qualifica")).toUpperCase());
			//fine minerba

	if (inPiemonte)
	{
		if (par.get("tc")!=null && !par.get("tc").toString().trim().equals("TUTTO")) ht.put("#tipocura_desc#",ISASUtil.getDecode(dbc,"tab_voci","tab_cod","tab_val","SAOADI",par.get("tc"),"tab_descrizione"));
		else ht.put("#tipocura_desc#","TUTTE");
		if (par.get("tprest")!=null && !par.get("tprest").toString().trim().equals("")) ht.put("#tipoprest_desc#",ISASUtil.getDecode(dbc,"prestaz","prest_cod",par.get("tprest"),"prest_des"));
		else ht.put("#tipoprest_desc#","TUTTE");

	}

//	doc.write("layout");
	doc.writeSostituisci("layout",ht);
}

/**
* stampa sintetica: sezione coordinate geografiche
*/
private void mkCoordinate(mergeDocument doc, Hashtable par,
	String cur_zona, String cur_dist, String cur_comu) {

	Hashtable ht = new Hashtable();
	ht.put("#descrizione_zona#", cur_zona);
	ht.put("#des_distr#", cur_dist);
	ht.put("#descrizione#", cur_comu);
	String ragg = faiRaggruppamento((String)par.get("ragg"));
	ht.put("#raggruppamento#", ragg);
	doc.writeSostituisci("area", ht);
}

/**
* stampa sintetica: sezione inizio tabella
*/
private void mkIniziaTabella(mergeDocument doc, ISASRecord dbr) {
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
private void mkContatto(ISASConnection dbc,ISASRecord dbr, mergeDocument doc){
    Hashtable ht = new Hashtable();
    String n_contatto="";
    String n_progetto = "";
    String n_cartella="";
    String diagnosi="";
    String data_contatto="";
    String data_chiusura="";
    String strSelect = "";
    String strTableName = "";
	try {
            /*28/02/07 se il tipo operatore � 98(medico specialista) non importa
                       andare sui contatti perch� in interv avr� sempre il contatto a 0
            */
//gb 21/06/07            if(!((String)dbr.get("int_tipo_oper")).equals("98")){
//gb 21/06/07 *******
            if((dbr.get("int_tipo_oper") != null) && !((String)dbr.get("int_tipo_oper")).equals("98"))
		{
		if (dbr.get("int_cartella")!=null)
	  	    n_cartella=((Integer)dbr.get("int_cartella")).toString();
		String strTipoOper = (String) dbr.get("int_tipo_oper");
		if (strTipoOper.equals("01"))
		   {
		   if (dbr.get("n_progetto")!=null)
		      {
		      n_progetto=((Integer)dbr.get("n_progetto")).toString();
		      strSelect = "SELECT *" +
				" FROM ass_progetto" +
				" WHERE n_cartella = " + n_cartella +
				" AND n_progetto = " + n_progetto;
		      }
		   }
		else if (strTipoOper.equals("02") || strTipoOper.equals("03") ||
			 strTipoOper.equals("04") || strTipoOper.equals("52"))
		   {
		   if (dbr.get("int_contatto")!=null)
		      {
		      n_contatto=((Integer)dbr.get("int_contatto")).toString();
		      if (strTipoOper.equals("02"))
		         strTableName = "skinf";
		      if (strTipoOper.equals("03"))
		         strTableName = "skmedico";
		      if (strTipoOper.equals("04"))
		         strTableName = "skfis";
		      if (strTipoOper.equals("52"))
		         strTableName = "skmedpal";
		      strSelect = "SELECT *" +
				" FROM " + strTableName +
				" WHERE n_cartella = " + n_cartella +
				" AND n_contatto = " + n_contatto;
		      }
		    }
		if (strSelect.equals(""))
		   {
     		   ht.put("#n_contatto#", " ");
		   ht.put("#patologia#", " ");
		   ht.put("#periodo#", " ");
		   }
		else
		   {
//gb 21/06/07: fine *******
/*gb 21/06/07: contsan non si usa pi� *******
		String sel = "SELECT c.* "+
		    " FROM contsan c " +
		    " WHERE "+
		    " c.n_cartella="+n_cartella+
		    " AND c.n_contatto="+n_contatto;
*gb 21/06/07: fine *******/
//		System.out.println("FoIPrest.mkContatto() select: "+sel);

//gb 21/06/07		ISASRecord dbper = dbc.readRecord(sel);
		   ISASRecord dbper = dbc.readRecord(strSelect);
//gb 21/06/07 *******
		   if (strTipoOper.equals("01"))
		      ht.put("#n_contatto#", n_progetto);
		   else
		      ht.put("#n_contatto#", n_contatto);
//gb 21/06/07: fine *******
		   if(dbr.get("int_tipo_oper")!=null && dbper!=null){
		      data_contatto=scegliDataAxFigProf(dbper,(String)dbr.get("int_tipo_oper"));
		      data_chiusura=scegliDataCxFigProf(dbper,(String)dbr.get("int_tipo_oper"));
		      if(data_chiusura.equals(""))
		         ht.put("#periodo#",data_contatto+"- ");
		      else
		         ht.put("#periodo#",data_contatto+"-"+data_chiusura);
		   }else{
		      ht.put("#periodo#"," ");
		   }
/*** 27/11/06
		String sel1 = "SELECT i.cd_diag, i.diagnosi "+
		    " FROM skpatologie p, icd9 i " +
		    " WHERE "+
		    " p.n_cartella="+n_cartella+" AND p.n_contatto="+n_contatto+
		    " AND i.cd_diag = p.skpat_patol1";
***/
		// 27/11/06: prendo la diagMax di ogni contatto
		   String critDtChius = "";
		   if ((data_chiusura != null) && (!data_chiusura.trim().equals("")))
			critDtChius = " AND diagnosi.data_diag <= " + formatDate(dbc, data_chiusura);

		   String sel1 = "SELECT td.cod_diagnosi, td.diagnosi"+
                                 " FROM diagnosi d,tab_diagnosi td" +
  			         " WHERE d.n_cartella= " + n_cartella +
                                 " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
		                 " FROM diagnosi" +
                                 " WHERE diagnosi.n_cartella = d.n_cartella" +
                                 " AND diagnosi.data_diag <= " + formatDate(dbc, data_fine) +
			         critDtChius + ")" +
                                 " AND d.diag1 = td.cod_diagnosi";


		//System.out.println("FoIPrest.mkContatto() select2: "+sel1);

		   ISASRecord dbpat = dbc.readRecord(sel1);
		   if(dbpat!=null){
		         if (dbpat.get("diagnosi")!=null)
			      diagnosi=(String)dbpat.get("diagnosi");
		         ht.put("#patologia#", diagnosi);
		   }else{
		         ht.put("#patologia#"," PATOLOGIA NON SPECIFICATA ");
		   }
		}
            }else{
     		ht.put("#n_contatto#", " ");
		ht.put("#patologia#", " ");
		ht.put("#periodo#", " ");
            }

            doc.writeSostituisci("rigaContatto", ht);
            mkPrestazioni(dbc,dbr,doc);
//	    doc.write("chiudirigaPrest");

	} catch (Exception ex) {
		debugMessage("FoIPrestEJB.mkContatto(): errore ");
		ht.put("#n_contatto#", "**** ERRORE ****");
		ht.put("#patologia#", " ");
		ht.put("#periodo#", " ");
		doc.writeSostituisci("rigaContatto", ht);
	}


//	doc.writeSostituisci("rigaTotaliAssxFigProf", ht4);
}

private void mkPrestazioni(ISASConnection dbc,ISASRecord dbr, mergeDocument doc)
 throws SQLException {

	//System.out.println("FoIPrestEJB.mkPrestazioni() ");
        ServerUtility su =new ServerUtility();
	try {
	String n_progetto = "";
        String n_contatto="";	String n_cartella="";
        String anno="";		String contatore="";
        String cod_oper="";
	String strSelect = "";
	String strClauseSel = "";
	ISASCursor dbcur = null;

//gb 21/06/07 *******
        if((dbr.get("int_tipo_oper") != null) && ((String)dbr.get("int_tipo_oper")).equals("01"))
	   {
	   if (dbr.get("n_progetto")!=null)
	      {
	      n_progetto = ((Integer)dbr.get("n_progetto")).toString();
	      strClauseSel = " AND n_progetto = " + n_progetto;
	      }
	   else
	      {
		Hashtable hterr1 = new Hashtable();
		hterr1.put("#prestazione#", " NESSUNA PRESTAZIONE TROVATA ");
		hterr1.put("#n_interv#", "");
		hterr1.put("#ore#", "");
		doc.writeSostituisci("rigaPrestazione", hterr1);
	      }
	   }
	else
	   {
           if (dbr.get("int_contatto")!=null)
                n_contatto=((Integer)dbr.get("int_contatto")).toString();
	   strClauseSel = " AND int_contatto = " + n_contatto;
	   }
//gb 21/06/07: fine *******
/*gb 21/06/07 *******
        if (dbr.get("int_contatto")!=null)
                n_contatto=((Integer)dbr.get("int_contatto")).toString();
*gb 21/06/07 *******/
String codprest= ((dbr.get("codice_prestazione")!=null  && dbr.get("codice_prestazione").toString().trim()!="")?" AND pre_cod_prest = '"+dbr.get("codice_prestazione").toString()+"'":"");
	if (!strClauseSel.equals(""))
	   {
           if (dbr.get("int_cartella")!=null)
                n_cartella=((Integer)dbr.get("int_cartella")).toString();
           if (dbr.get("int_cod_oper")!=null)
                cod_oper=(String)dbr.get("int_cod_oper");

           String sel= "SELECT pre_cod_prest,pre_des_prest,"+
			" SUM(pre_numero) pre_numero,SUM(pre_tempo*pre_numero) pre_tempo "+
			" FROM intpre, interv "+
			" WHERE int_cartella ="+n_cartella+
//gb 21/06/07            " AND int_contatto="+n_contatto+
			strClauseSel + //gb 21/06/07
			" AND int_cod_oper = '" + cod_oper + "'" +
			" AND pre_anno = int_anno"+
			" AND pre_contatore = int_contatore" + codprest;

	   sel = su.addWhere(sel, su.REL_AND, "int_data_prest",
				su.OP_GE_NUM, formatDate(dbc, data_inizio));
	   sel = su.addWhere(sel, su.REL_AND, "int_data_prest",
				su.OP_LE_NUM, formatDate(dbc, data_fine));

//21/01/2004 jessy inserito filtro flag tipo accertamento: domiciliare o ambulatoriale
//se � E implica senza filtro
	   if(!tipo_accert.equals("E")){
		sel = su.addWhere(sel, su.REL_AND, "int_ambdom",
		su.OP_EQ_STR, tipo_accert);
	   }
//           if (raggruppax!=null && raggruppax.equals("C"))
//                  sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
//           else if (raggruppax!=null && raggruppax.equals("A"))
//                  sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";
//           else if (raggruppax!=null && raggruppax.equals("P"))
//                  sel += " AND int_codpres='"+((String)dbr.get("codice"))+"'";
//	    " AND pre_anno ='"+anno+"' AND pre_contatore = "+contatore+

       if(this.dom_res==null)
       {

       if (raggruppax!=null && raggruppax.equals("C"))
         sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
       else if (raggruppax!=null && raggruppax.equals("A"))
    	   sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";
       else if (raggruppax!=null && raggruppax.equals("P"))
    	   sel += " AND int_codpres='"+((String)dbr.get("codice"))+"'";
       }
       else if (this.dom_res.equals("D"))
       		{
    	   if (raggruppax!=null && raggruppax.equals("C"))
    	         sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
    	       else if (raggruppax!=null && raggruppax.equals("A"))
    	    	   sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";

             }
       else if (this.dom_res.equals("R"))
       		{
    	   if (raggruppax!=null && raggruppax.equals("C"))
    	         sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
    	       else if (raggruppax!=null && raggruppax.equals("A"))
    	    	   sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";

       		}
     //Minerba 06/03/2013		
       //String tipo_op = dbr.get("figprof").toString();
       String qualifica = (String) dbr.get("qualifica");
      // if (!(tipo_op.equals("00"))){
    	if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
    		sel +=" AND int_qual_oper='"+qualifica+"'";
    	}
       //}//fine Minerba 06/03/2013


           sel = sel+ " GROUP BY pre_cod_prest,pre_des_prest";
	   dbcur=dbc.startCursor(sel);
System.out.println("**** SNT: FoIPrestEJB.mkPrestazioni - sel=["+sel+"]");
	   if (dbcur == null)
		{
		Hashtable hterr = new Hashtable();
		hterr.put("#prestazione#", "**ERRORE**");
		hterr.put("#n_interv#", "");
		hterr.put("#ore#", "");
		doc.writeSostituisci("rigaPrestazione", hterr);
		System.out.println("FoIPrestEJB.mkPrestazioni(): "+
				"cursore non valido");
		}
	   else
		{
		if (dbcur.getDimension() <= 0)
		   {
		   Hashtable hterr1 = new Hashtable();
		   hterr1.put("#prestazione#", " NESSUNA PRESTAZIONE TROVATA ");
		   hterr1.put("#n_interv#", "");
		   hterr1.put("#ore#", "");
		   doc.writeSostituisci("rigaPrestazione", hterr1);
		   }
		else
		   {
        	   while (dbcur.next())
			{
                        Hashtable ht = new Hashtable();
		        ISASRecord dbpre=dbcur.getRecord();
			if (dbpre.get("pre_des_prest")!=null)
	  		   ht.put("#prestazione#", (String)dbpre.get("pre_des_prest"));
			else
	  		   ht.put("#prestazione#", "");
                        if (dbpre.get("pre_numero")!=null)
			   {
                           String pre_num=""+ dbpre.get("pre_numero");
                           if (pre_num.indexOf(".")!=-1)
                              pre_num=pre_num.substring(0,pre_num.indexOf("."));
                           tot_num_ass=tot_num_ass+Integer.parseInt(pre_num);
	  		   ht.put("#n_interv#", pre_num);
                           }
			else
	  		   ht.put("#n_interv#", "");
			if (dbpre.get("pre_tempo")!=null)
                           {
                           String pre_tem=""+ dbpre.get("pre_tempo");
                           if (pre_tem.indexOf(".")!=-1)
                              pre_tem=pre_tem.substring(0,pre_tem.indexOf("."));
			   tot_ore_ass=tot_ore_ass+Integer.parseInt(pre_tem);
////	  		      ht.put("#ore#", ((Integer)dbpre.get("pre_tempo")).toString());}
	  		   ht.put("#ore#", convertiOre(Integer.parseInt(pre_tem)));
                           }
			 else
	  		   ht.put("#ore#", "");
			 doc.writeSostituisci("rigaPrestazione", ht);
			 }
		   }	// fine if dbcur.getDimension()
		} // fine if dbcur
	   if (dbcur != null)
	      dbcur.close();
	   } // fine if (strClauseSel.equals(""))
        Hashtable htAss = new Hashtable();
	htAss.put("#tot_interv#", ""+tot_num_ass);
	htAss.put("#tot_ore#", convertiOre(tot_ore_ass));
	doc.write("chiudirigaPrest");
	doc.writeSostituisci("rigaTotAss", htAss);
	tot_num_figprof=tot_num_figprof+tot_num_ass;
	tot_ore_figprof=tot_ore_figprof+tot_ore_ass;
	tot_num_ass=0;
	tot_ore_ass=0;
	} catch(Exception e) {
		Hashtable hterr2 = new Hashtable();
		hterr2.put("#prestazione#", "**ERRORE**");
		hterr2.put("#n_interv#", "");
		hterr2.put("#ore#", "");
		doc.writeSostituisci("rigaPrestazione", hterr2);
		doc.write("chiudirigaPrest");
		e.printStackTrace();
//System.out.println("ILARIA");
//gb 21/06/07		throw new SQLException("FoIPrestEJB.query_elencoPrest(): "+e);
		throw new SQLException("FoIPrestEJB.mkPrestazioni(): "+e);
	}
}	// End of query_elencoPrest() method

private void mkContattoExcel(ISASConnection dbc,ISASRecord dbr, mergeDocument doc, Hashtable hStampa){
    String n_contatto="";
    String n_progetto = "";
    String n_cartella="";
    String cod_diagnosi="";
    String diagnosi="";
    String data_contatto="";
    String data_chiusura="";
    String strSelect = "";
    String strTableName = "";
	try {
            /*28/02/07 se il tipo operatore � 98(medico specialista) non importa
                       andare sui contatti perch� in interv avr� sempre il contatto a 0
            */
            if((dbr.get("int_tipo_oper") != null) && !((String)dbr.get("int_tipo_oper")).equals("98")){
		if (dbr.get("int_cartella")!=null)
	  	    n_cartella=((Integer)dbr.get("int_cartella")).toString();
		String strTipoOper = (String) dbr.get("int_tipo_oper");
		if (strTipoOper.equals("01")){
		   if (dbr.get("n_progetto")!=null){
		      n_progetto=((Integer)dbr.get("n_progetto")).toString();
		      strSelect = "SELECT *" +
				" FROM ass_progetto" +
				" WHERE n_cartella = " + n_cartella +
				" AND n_progetto = " + n_progetto;
		      }
		   }
		else if (strTipoOper.equals("02") || strTipoOper.equals("03") ||
			 strTipoOper.equals("04") || strTipoOper.equals("52")) {
		   if (dbr.get("int_contatto")!=null){
		      n_contatto=((Integer)dbr.get("int_contatto")).toString();
		      if (strTipoOper.equals("02"))
		         strTableName = "skinf";
		      if (strTipoOper.equals("03"))
		         strTableName = "skmedico";
		      if (strTipoOper.equals("04"))
		         strTableName = "skfis";
		      if (strTipoOper.equals("52"))
		         strTableName = "skmedpal";
		      strSelect = "SELECT *" +
				" FROM " + strTableName +
				" WHERE n_cartella = " + n_cartella +
				" AND n_contatto = " + n_contatto;
                    }
                }
		if (strSelect.equals("")){
     		   hStampa.put("#n_contatto#", " ");
                   hStampa.put("#cod_pato#", " ");
		   hStampa.put("#patologia#", " ");
		   hStampa.put("#periodo#", " ");
	        }else{
		   ISASRecord dbper = dbc.readRecord(strSelect);
		   if (strTipoOper.equals("01"))
		      hStampa.put("#n_contatto#", n_progetto);
		   else
		      hStampa.put("#n_contatto#", n_contatto);
		   if(dbr.get("int_tipo_oper")!=null && dbper!=null){
		      data_contatto=scegliDataAxFigProf(dbper,(String)dbr.get("int_tipo_oper"));
		      data_chiusura=scegliDataCxFigProf(dbper,(String)dbr.get("int_tipo_oper"));
		      if(data_chiusura.equals(""))
		         hStampa.put("#periodo#",data_contatto+"- ");
		      else
		         hStampa.put("#periodo#",data_contatto+"-"+data_chiusura);
		   }else{
		      hStampa.put("#periodo#"," ");
		   }
		// 27/11/06: prendo la diagMax di ogni contatto
		   String critDtChius = "";
		   if ((data_chiusura != null) && (!data_chiusura.trim().equals("")))
			critDtChius = " AND diagnosi.data_diag <= " + formatDate(dbc, data_chiusura);

		   String sel1 = "SELECT td.cod_diagnosi, td.diagnosi"+
                                 " FROM diagnosi d,tab_diagnosi td" +
  			         " WHERE d.n_cartella= " + n_cartella +
                                 " AND d.data_diag IN (SELECT MAX(diagnosi.data_diag)" +
		                 " FROM diagnosi" +
                                 " WHERE diagnosi.n_cartella = d.n_cartella" +
                                 " AND diagnosi.data_diag <= " + formatDate(dbc, data_fine) +
			         critDtChius + ")" +
                                 " AND d.diag1 = td.cod_diagnosi";
                   //System.out.println("FoIPrest.mkContatto() select2: "+sel1);

		   ISASRecord dbpat = dbc.readRecord(sel1);
		   if(dbpat!=null){
		         if (dbpat.get("diagnosi")!=null){
			      diagnosi=(String)dbpat.get("diagnosi");
                              cod_diagnosi=(String)dbpat.get("cod_diagnosi");
		         }
		         hStampa.put("#patologia#", diagnosi);
                         hStampa.put("#cod_pato#", cod_diagnosi);
		   }else{
		         hStampa.put("#patologia#"," PATOLOGIA NON SPECIFICATA ");
                         hStampa.put("#cod_pato#"," XXXXX");
		   }
		}
            }else{
     		hStampa.put("#n_contatto#", " ");
		hStampa.put("#patologia#", " ");
                hStampa.put("#cod_pato#", " ");
		hStampa.put("#periodo#", " ");
            }
            mkPrestazioniExcel(dbc,dbr,doc, hStampa);

	} catch (Exception ex) {
		debugMessage("FoIPrestEJB.mkContattoExcel(): errore ");
		hStampa.put("#n_contatto#", "**** ERRORE ****");
		hStampa.put("#cod_pato#", " ");
                hStampa.put("#patologia#", " ");
		hStampa.put("#periodo#", " ");
	}
}

private void mkPrestazioniExcel(ISASConnection dbc,ISASRecord dbr, mergeDocument doc, Hashtable hStampa)
throws SQLException {
    //System.out.println("*****FoIPrestEJB.mkPrestazioniExcel(): "+hStampa.toString());
    ServerUtility su =new ServerUtility();
    try {
	String n_progetto = "";
        String n_contatto="";	String n_cartella="";
        String anno="";		String contatore="";
        String cod_oper="";
	String strSelect = "";
	String strClauseSel = "";
	ISASCursor dbcur = null;

        if((dbr.get("int_tipo_oper") != null) && ((String)dbr.get("int_tipo_oper")).equals("01")){
	   if (dbr.get("n_progetto")!=null){
	      n_progetto = ((Integer)dbr.get("n_progetto")).toString();
	      strClauseSel = " AND n_progetto = " + n_progetto;
           }else{
		Hashtable hterr1 = new Hashtable();
                hStampa.put("#cod_prestaz#", "XXXX");
		hStampa.put("#prestazione#", " NESSUNA PRESTAZIONE TROVATA ");
		hStampa.put("#n_interv#", "");
		hStampa.put("#ore#", "");
           }
        }else{
           if (dbr.get("int_contatto")!=null)
                n_contatto=((Integer)dbr.get("int_contatto")).toString();
	   strClauseSel = " AND int_contatto = " + n_contatto;
        }
        String codprest= ((dbr.get("codice_prestazione")!=null  && dbr.get("codice_prestazione").toString().trim()!="")?" AND pre_cod_prest = '"+dbr.get("codice_prestazione").toString()+"'":"");
	if (!strClauseSel.equals("")){
           if (dbr.get("int_cartella")!=null)
                n_cartella=((Integer)dbr.get("int_cartella")).toString();
           if (dbr.get("int_cod_oper")!=null)
                cod_oper=(String)dbr.get("int_cod_oper");

           String sel= "SELECT pre_cod_prest,pre_des_prest,"+
		       " SUM(pre_numero) pre_numero,SUM(pre_tempo*pre_numero) pre_tempo "+
		       " FROM intpre, interv "+
		       " WHERE int_cartella ="+n_cartella+
		       strClauseSel +
                       " AND int_cod_oper = '" + cod_oper + "'" +
                       " AND pre_anno = int_anno"+
                       " AND pre_contatore = int_contatore" + codprest;

	   sel = su.addWhere(sel, su.REL_AND, "int_data_prest",
				su.OP_GE_NUM, formatDate(dbc, data_inizio));
	   sel = su.addWhere(sel, su.REL_AND, "int_data_prest",
				su.OP_LE_NUM, formatDate(dbc, data_fine));

	   if(!tipo_accert.equals("E")){
		sel = su.addWhere(sel, su.REL_AND, "int_ambdom",
		su.OP_EQ_STR, tipo_accert);
	   }
	   
	 //Minerba 06/03/2013		
	   String qualifica = (String) dbr.get("qualifica");
		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
			sel +=" AND int_qual_oper='"+qualifica+"'";
		}//fine Minerba 06/03/2013



           if(this.dom_res==null) {
               if (raggruppax!=null && raggruppax.equals("C"))
                 sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
               else if (raggruppax!=null && raggruppax.equals("A"))
                   sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";
               else if (raggruppax!=null && raggruppax.equals("P"))
                   sel += " AND int_codpres='"+((String)dbr.get("codice"))+"'";
           }else if (this.dom_res.equals("D"))	{
                if (raggruppax!=null && raggruppax.equals("C"))
    	            sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
    	        else if (raggruppax!=null && raggruppax.equals("A"))
    	    	    sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";
           }else if (this.dom_res.equals("R")){
       	        if (raggruppax!=null && raggruppax.equals("C"))
                    sel += " AND int_cod_comune='"+((String)dbr.get("codice"))+"'";
    	        else if (raggruppax!=null && raggruppax.equals("A"))
    	    	   sel += " AND int_cod_areadis='"+((String)dbr.get("codice"))+"'";
	   }
           sel = sel+ " GROUP BY pre_cod_prest,pre_des_prest";
	   dbcur=dbc.startCursor(sel);
           //System.out.println("**** SNT: FoIPrestEJB.mkPrestazioniExcel - sel=["+sel+"]");
	   if (dbcur == null)	{
                hStampa.put("#cod_prestaz#", "XXXX");
		hStampa.put("#prestazione#", "**ERRORE**");
		hStampa.put("#n_interv#", "");
		hStampa.put("#ore#", "");
    	   }else{
		if (dbcur.getDimension() <= 0){
		   hStampa.put("#cod_prestaz#", "XXXX");
                   hStampa.put("#prestazione#", " NESSUNA PRESTAZIONE TROVATA ");
		   hStampa.put("#n_interv#", "");
		   hStampa.put("#ore#", "");
		}else{
        	   while (dbcur.next())	{
                        Hashtable ht = new Hashtable();
		        ISASRecord dbpre=dbcur.getRecord();
			if (dbpre.get("pre_des_prest")!=null){
                            hStampa.put("#prestazione#", (String)dbpre.get("pre_des_prest"));
 	  		    hStampa.put("#cod_prestaz#", (String)dbpre.get("pre_cod_prest"));
			}else{
                            hStampa.put("#cod_prestaz#", "");
	  		    hStampa.put("#prestazione#", "");
			}
                        if (dbpre.get("pre_numero")!=null)  {
                           String pre_num=""+ dbpre.get("pre_numero");
                           if (pre_num.indexOf(".")!=-1)
                              pre_num=pre_num.substring(0,pre_num.indexOf("."));
                           tot_num_ass=tot_num_ass+Integer.parseInt(pre_num);
	  		   hStampa.put("#n_interv#", pre_num);
                        }else
	  		   hStampa.put("#n_interv#", "");
			if (dbpre.get("pre_tempo")!=null){
                           String pre_tem=""+ dbpre.get("pre_tempo");
                           if (pre_tem.indexOf(".")!=-1)
                              pre_tem=pre_tem.substring(0,pre_tem.indexOf("."));
			   tot_ore_ass=tot_ore_ass+Integer.parseInt(pre_tem);
	  		   hStampa.put("#ore#", convertiOre(Integer.parseInt(pre_tem)));
                         }else
	  		   hStampa.put("#ore#", "");
                         doc.writeSostituisci("tabella", (Hashtable)hStampa.clone());
        	   }
		}	// fine if dbcur.getDimension()
            } // fine if dbcur
            if (dbcur != null)
                dbcur.close();
	   }
	} catch(Exception e) {
                hStampa.put("#cod_prestaz#", "XXXX");
		hStampa.put("#prestazione#", "**ERRORE**");
		hStampa.put("#n_interv#", "");
		hStampa.put("#ore#", "");
		throw new SQLException("FoIPrestEJB.mkPrestazioni(): "+e);
	}
}

/**
* stampa sintetica: sezione fine tabella
*/
private void mkFineTabella(mergeDocument doc) {
	doc.write("finetab");

//	Hashtable ht = new Hashtable();
//	ht.put("#totale#", " Totale n. assistiti: "+conta);
//	doc.writeSostituisci("totale", ht);
}

private void mkBody(mergeDocument md, ISASCursor dbcur, Hashtable par,
	ISASConnection dbc) throws Exception {
	boolean first_time = true;
	String old_zona = "*", cur_zona = "";
	String old_dist = "*", cur_dist = "";
	String old_comu = "*", cur_comu = "";
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
                while (dbcur.next())
                {
                  ISASRecord dbr=dbcur.getRecord();
                  cur_zona = (String)dbr.get("des_zona");
		  cur_dist = (String)dbr.get("des_distretto");
		  cur_comu = (String)dbr.get("descrizione");
		  cur_oper = (String)dbr.get("int_cod_oper");
                  cur_ass  = ((Integer)dbr.get("int_cartella")).toString();
//gb 21/06/07 *******
            	  if((dbr.get("int_tipo_oper") != null) && ((String)dbr.get("int_tipo_oper")).equals("01"))
		     if (dbr.get("n_progetto") != null)
			cur_conta = ((Integer)dbr.get("n_progetto")).toString();
		     else
			continue; // Va alla fine del while e comincia un altro ciclo.
		  else
		    cur_conta = ((Integer)dbr.get("int_contatto")).toString();
//gb 21/06/07: fine *******
                  if(!old_oper.equals(cur_oper))
                     tot_op_riep++;
		  if ((! old_zona.equals(cur_zona)) ||
		      (! old_dist.equals(cur_dist)) ||
                      (! old_comu.equals(cur_comu)) ||
                      (! old_oper.equals(cur_oper)))
		      {
                      if (first_time)
			{
                        first_time = ! first_time;
                        }
		      else
			{
          	        Hashtable htotfp = new Hashtable();
			htotfp.put("#totali_interv#", ""+tot_num_figprof);
			htotfp.put("#totali_ore#", convertiOre(tot_ore_figprof));
			md.writeSostituisci("rigaTotaliAssxFigProf", htotfp);
			tot_num_int_riep=tot_num_int_riep+tot_num_figprof;
			tot_ore_riep=tot_ore_riep+tot_ore_figprof;
			tot_num_figprof=0;
			tot_ore_figprof=0;
                        mkFineTabella(md);
			}

                      if ((! old_zona.equals(cur_zona)) ||
                          (! old_dist.equals(cur_dist)) ||
                          (! old_comu.equals(cur_comu)))
                         {
                         // intestazione cambio coordinate
			 mkCoordinate(md,par,cur_zona, cur_dist, cur_comu);
                         }
                        // apri tabella con muovo operatore
                      mkIniziaTabella(md,dbr);
                      }//chiude if

		    old_zona = cur_zona;
		    old_dist = cur_dist;
		    old_comu = cur_comu;

		    if (!old_ass.equals(cur_ass)){
                      faiRigaAssistito(dbr, md);
					  if(par.get("tprest")!=null && !par.get("tprest").toString().trim().equals(""))
					  dbr.put("codice_prestazione",par.get("tprest").toString());
		      mkContatto(dbc,dbr,md);}
		    else
		      {
                      if (!old_conta.equals(cur_conta) ||
			  !old_oper.equals(cur_oper))
			{
			if(!old_oper.equals(cur_oper))
			    faiRigaAssistito(dbr, md);
				if(par.get("tprest")!=null && !par.get("tprest").toString().trim().equals(""))
					  dbr.put("codice_prestazione",par.get("tprest").toString());
			mkContatto(dbc,dbr,md);
			}
		      }

		    old_oper = cur_oper;
		    old_ass = cur_ass;
		    old_conta = cur_conta;
		    // stampa riga
		    //mkSinteticaRigaTabella(dbc, dbr, md);
                } //chiude while

		if (!first_time) {
			// chiudi tabella precedente
////          	        Hashtable htAss3 = new Hashtable();
////			md.writeSostituisci("rigaTotAss", htAss3);
          	        Hashtable htotfp2 = new Hashtable();
			htotfp2.put("#totali_interv#", ""+tot_num_figprof);
			htotfp2.put("#totali_ore#", convertiOre(tot_ore_figprof));
			md.writeSostituisci("rigaTotaliAssxFigProf", htotfp2);
			tot_ore_riep=tot_ore_riep+tot_ore_figprof;
			tot_num_int_riep=tot_num_int_riep+tot_num_figprof;
			tot_num_figprof=0;
			tot_ore_figprof=0;
			mkFineTabella(md);
          	        Hashtable hriep = new Hashtable();
			hriep.put("#tot_num_int#", ""+tot_num_int_riep);
			hriep.put("#tot_ore_imp#", convertiOre(tot_ore_riep));
			hriep.put("#tot_operatori#", ""+tot_op_riep);
			md.writeSostituisci("riepilogo", hriep);
		}

	} catch (Exception e) {
		debugMessage("FoIAssEle_2EJB.mkSinteticaBody(): "+e);
		e.printStackTrace();
		throw new SQLException("Errore eseguendo mkSinteticaBody()");
	}
}

private String convertiOre(int ore)
{
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
private String stampaore(mergeDocument md,int ore_generali,int numero_assistiti)
{
   Hashtable oradai=new Hashtable();
   String ore="";
   ore=convertiOre(ore_generali);
   oradai.put("#oregen#",ore);
   oradai.put("#num_assistiti#",(new Integer(numero_assistiti)).toString());
   md.writeSostituisci("oregen",oradai);
   return ore;
}

/**
* restituisce la select per la stampa sintetica.
*/

private String getSelect(ISASConnection dbc, Hashtable par) {

	this.inPiemonte = par.get("piem").toString().equals("S");
	String tipocura = par.get("tc").toString();
	String tipoprest = par.get("tprest").toString();
	String tipo_op = par.get("figprof").toString();
	String livello_ass = (String)par.get("motivo");
	String tabella = "";
	String campo = "";
	if (tipo_op.equals("02"))
	{tabella = "skinf"; campo = "ski_tipocura";}
	else if (tipo_op.equals("03")) {tabella = "skmedico"; campo = "skm_tipocura";}
	String s="";
	if (inPiemonte && !tipocura.equals("TUTTO") && (tipo_op.equals("02")||tipo_op.equals("03")))

	s = "SELECT DISTINCT "+
		"r.int_cartella, r.int_cod_oper," +
//gb 21/06/07                "r.int_contatto, r.int_tipo_oper, r.int_ambdom, "+
                "r.int_contatto, r.n_progetto, r.int_tipo_oper, r.int_ambdom, "+ //gb 21/06/07
		//"r.int_anno,r.int_contatore, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"o.cognome opcogn, o.nome opnome, t."+campo+", "+
                // jessica 24/02/2012
                "u.cod_zona, u.cod_distretto, "+
		"u.codice,u.descrizione, u.des_zona, u.des_distretto "+
		"FROM interv r, cartella c, operatori o, "+tabella+" t,  "+
		"ubicazioni_n u";

	else
	s = "SELECT DISTINCT "+
		"r.int_cartella, r.int_cod_oper," +
//gb 21/06/07                "r.int_contatto, r.int_tipo_oper, r.int_ambdom, "+
                "r.int_contatto, r.n_progetto, r.int_tipo_oper, r.int_ambdom, "+ //gb 21/06/07
		//"r.int_anno,r.int_contatore, "+
		"c.cognome cogn, c.nome nomeut, c.data_nasc, "+
		"o.cognome opcogn, o.nome opnome, "+
		// jessica 24/02/2012
                "u.cod_zona, u.cod_distretto, "+
		"u.codice,u.descrizione, u.des_zona, u.des_distretto "+
		"FROM interv r, cartella c, operatori o, "+
		"ubicazioni_n u";
	if (livello_ass!=null && !livello_ass.equals("") && !livello_ass.equals("-1"))
		s +=" ,rm_skso rm, rm_skso_mmg m";
	try {
		String w = getSelectParteWhere(dbc, par);
		if (! w.equals("")) s = s + " WHERE " + w;
	} catch(Exception e) {
		debugMessage("FoIPrestEJB.getSelect(): "+e);
		e.printStackTrace();
	}
	s = s + " ORDER BY u.des_zona, u.des_distretto, u.descrizione, "+
		"o.cognome, o.nome, c.cognome, c.nome, r.int_contatto";
	debugMessage("FoIPrestEJB.getSelect(): "+s);
	return s;
}

/**
* restituisce la parte where della select valorizzata secondo i
* parametri di ingresso.
*/
private String getSelectParteWhere(ISASConnection dbc, Hashtable par) {
	ServerUtility su = new ServerUtility();
	this.inPiemonte = par.get("piem").toString().equals("S");
	String tipocura = par.get("tc").toString();
	String codprest = par.get("tprest").toString();
	String tipo_op = par.get("figprof").toString();
	String livello_ass = (String)par.get("motivo");
	String tabella = "";
	String campo = "";
	String campo_motivo = "";
	if (tipo_op.equals("02"))
	{tabella = "skinf"; campo = "ski_tipocura";}
	else if (tipo_op.equals("03")) {tabella = "skmedico"; campo = "skm_tipocura";}
String s="";	
String figprof="";
if(!par.get("figprof").equals("00"))
	figprof=(String)par.get("figprof");
//String s = su.addWhere("", su.REL_AND, "r.int_tipo_oper",
	//	su.OP_EQ_STR, figprof);

if (figprof.equals("02")) 
{tabella = "skinf"; campo_motivo = "ski_motivo";}
else if (figprof.equals("03")) {tabella = "skmedico"; campo_motivo = "skm_motivo";}



//09/01/2004 bargi inserito filtro flag tipo accertamento: domiciliare o ambulatoriale
//se � E implica senza filtro
//String tipo_accert="";
/*if(!par.get("tipo_accert").equals("E")){
	tipo_accert=(String)par.get("tipo_accert");
	s = su.addWhere(s, su.REL_AND, "r.int_ambdom",
		su.OP_EQ_STR, tipo_accert);
}*/

	

	if (inPiemonte && !tipocura.equals("TUTTO") && (tipo_op.equals("02")||tipo_op.equals("03")))
	{

		s += " AND r.int_cartella=t.n_cartella ";
		s += " AND r.int_contatto=t.n_contatto ";

	if (tipo_op.equals("02")||tipo_op.equals("03"))
	{
		if (tipocura!="TUTTO" && tipocura!="NESDIV")
		s = su.addWhere(s, su.REL_AND, "t."+campo,
		su.OP_EQ_STR, tipocura);
	}

	}

	s = su.addWhere("", su.REL_AND, "r.int_tipo_oper",
			su.OP_EQ_STR, figprof);
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_GE_NUM,
		formatDate(dbc, (String)par.get("data_inizio")));
	s = su.addWhere(s, su.REL_AND, "r.int_data_prest",
		su.OP_LE_NUM,
		formatDate(dbc, (String)par.get("data_fine")));
	//s = su.addWhere(s, su.REL_AND, "r.int_cod_oper",
	//	su.OP_GE_STR,
	//	 (String)par.get("codice_inizio"));
	s = su.addWhere(s, su.REL_AND, "r.int_cod_oper",
		su.OP_EQ_STR,
		 (String)par.get("codice_inizio"));
	s = su.addWhere(s, su.REL_AND, "r.int_cod_oper",
		su.OP_LE_STR,
		(String)par.get("codice_fine"));

data_inizio=(String)par.get("data_inizio");
data_fine=(String)par.get("data_fine");
//Minerba 06/03/2013		
	String qualifica = (String) par.get("qualifica");
	if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
		s +=" AND r.int_qual_oper='"+qualifica+"'";
	}//fine Minerba 06/03/2013

	if (! s.equals("")) s += " AND ";
	s += " r.int_cartella = c.n_cartella AND r.int_cod_oper = o.codice ";
	
	
	String data_ini=(String)par.get("data_inizio");
	String data_fine=(String)par.get("data_fine");
    if (livello_ass!=null && !livello_ass.equals("-1")) 
    	s +=" AND rm.n_cartella=r.int_cartella"+
    			" AND rm.data_presa_carico_skso >= " + formatDate(dbc,data_ini) + 
    			" AND rm.data_presa_carico_skso <= " + formatDate(dbc,data_fine) + 
    			" AND rm.n_cartella=m.n_cartella and rm.id_skso=m.id_skso"+
    			" AND m.tipocura='"+livello_ass+"'";
    
    String scr = (String)par.get("ragg");//RAGGRUPPAMENTO
    raggruppax=scr;
    s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, scr);
    if(this.dom_res==null)
    {

	 if (scr!=null && scr.equals("C"))
         s += " AND r.int_cod_comune=u.codice ";
       else if (scr!=null && scr.equals("A"))
         s += " AND r.int_cod_areadis=u.codice ";
       else if (scr!=null && scr.equals("P"))
         s += " AND u.codice=r.int_codpres ";
    }
    else if (this.dom_res.equals("D"))
		{
	 if (scr!=null && scr.equals("C"))
         s += " AND r.int_cod_comune=u.codice ";
       else if (scr!=null && scr.equals("A"))
         s += " AND r.int_cod_areadis=u.codice ";


      }
    else if (this.dom_res.equals("R"))
		{
	 if (scr!=null && scr.equals("C"))
         s += " AND r.int_cod_comune=u.codice ";
       else if (scr!=null && scr.equals("A"))
         s += " AND r.int_cod_areadis=u.codice ";
		}

s = su.addWhere(s, su.REL_AND, "u.cod_zona",
	su.OP_EQ_STR, (String)par.get("zona"));
s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
	su.OP_EQ_STR, (String)par.get("distretto"));
s = su.addWhere(s, su.REL_AND, "u.codice",
	su.OP_EQ_STR, (String)par.get("pca"));
	return s;
}

public byte[] query_elencoPrest(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {
	String punto = mioNome +"query_elencoPrest ";
	System.out.println(punto + "dati che ricevo>"+ par+"<\n");
	System.out.println("FoIPrestEJB.query_elencoPrest(): DEBUG inizio...");
	boolean done=false;
	ISASConnection dbc=null;
        ServerUtility su =new ServerUtility();

        String data_inizio = "";
        String data_fine = "";

	try {

		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}

			myLogin lg = new myLogin();
			String selectedLanguage = (String)par.get(FileMaker.printParamLang);
			lg.put(utente,passwd,selectedLanguage);
                //lg.put(utente,passwd);
                dbc = super.logIn(lg);

	       // System.out.println("FoIPrestEJB.query_2elencoPrest()");

		ISASCursor dbcur=dbc.startCursor(getSelect(dbc,par));
		mkLayout(dbc, par, eve);

		if (dbcur == null) {
			eve.write("messaggio");
			eve.write("finale");
			System.out.println("FoIPrestEJB.query_elencoPrest(): "+
				"cursore non valido");
		} else	{
			if (dbcur.getDimension() <= 0) {
				eve.write("messaggio");
				eve.write("finale");
			} else	{
                            if(((String)par.get("TYPE")).equals("PDF")){
				mkBody(eve, dbcur,par,dbc);
				//eve.write("finale");
                            }else{
                                FaiExcel(dbc, dbcur, eve, par);
                            }
                            eve.write("finale");
			}	// fine if dbcur.getDimension()
		}	// fine if dbcur
		dbcur.close();
		eve.close();
		//System.out.println("FoIPrestEJB.query_elencoPrest(): DEBUG "+
			//"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		e.printStackTrace();
		throw new SQLException("FoIPrestEJB.query_elencoPrest(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				e1.printStackTrace();
				System.out.println("FoIPrestEJB.query_elencoPrest(): "+e1);
			}
		}
	}
}	// End of query_elencoPrest() method



// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

private String mkPrestSinSelect(ISASConnection dbc, Hashtable par) {

	String sel = "SELECT DISTINCT c.data_nasc,c.sesso,distretti.des_distr,"+
		"comuni.descrizione,zone.descrizione_zona,c.n_cartella "+
		"FROM cartella c,interv i,distretti,operatori o,comuni,zone";
	String mywhere = " WHERE";

	// controllo valore tipo figura professionale
	String tipo = (String)par.get("tipo");
	tipo = "02";					// DEBUG: Toglimi!!!!
	if (!(tipo.equals("") || tipo == null))
		mywhere = mywhere+" o.tipo='"+tipo+"' AND";

	mywhere = mywhere+" i.int_cod_oper = o.codice "+
		"AND i.int_cartella = c.n_cartella";

	// controllo valore corretto data_fine
	String data_fine = (String)par.get("data_fine");
	if (!(data_fine.equals("") || data_fine == null))
		mywhere = mywhere+" AND i.int_data_prest<="+
			formatDate(dbc,data_fine);

	// controllo valore corretto data_inizio
	String data_inizio = (String)par.get("data_inizio");
	if (!(data_inizio.equals("") || data_inizio == null))
		mywhere = mywhere+" AND i.int_data_prest>="+
			formatDate(dbc,data_inizio);

        ServerUtility su = new ServerUtility();
      //Minerba 06/03/2013		
  	   String qualifica = (String) par.get("qualifica");
  		if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){				
  			sel +=" AND i.int_qual_oper='"+qualifica+"'";
  		}//fine Minerba 06/03/2013

	// controllo codice inizio (infermiere)
	mywhere = su.addWhere(mywhere, su.REL_AND, "o.codice",
		su.OP_GE_STR, (String)par.get("codice_inizio"));

	// controllo codice fine (infermiere)
	mywhere = su.addWhere(mywhere, su.REL_AND, "o.codice",
		su.OP_LE_STR, (String)par.get("codice_fine"));

	// controllo zona
	mywhere = su.addWhere(mywhere, su.REL_AND, "i.int_codzona",
		su.OP_EQ_STR, (String)par.get("zona"));

	// controllo distretto
	mywhere = su.addWhere(mywhere, su.REL_AND, "i.int_coddistr",
		su.OP_EQ_STR, (String)par.get("distretto"));

	// controllo comune
	mywhere = su.addWhere(mywhere, su.REL_AND, "i.int_codcomune",
		su.OP_EQ_STR, (String)par.get("comune"));

	mywhere = mywhere +" AND i.int_coddistr = distretti.cod_distr"+
		" AND i.int_codzona = zone.codice_zona"+
		" AND i.int_codcomune = comuni.codice " +
		" ORDER BY zone.descrizione_zona,distretti.des_distr,"+
		"comuni.descrizione,c.sesso";
	debugMessage("FoIPrestEJB.mkPrestSinSelect: "+sel+mywhere);
        return sel+mywhere;
}

String getConfString(ISASConnection dbc, String k) {
	String rc;
	String sel = "SELECT conf_txt,conf_key FROM conf WHERE "+
		"conf_kproc='SINS' AND conf_key='"+k+"'";
	try {
		ISASRecord dbtxt = dbc.readRecord(sel);
		rc = (String) dbtxt.get("conf_txt");
	} catch (Exception ex) {
		debugMessage("FoIPrestEJB.getConfString(): errore "+sel);
		rc = "*** "+k+" ***";
	}
	return rc;
}

private void mkPrestSinLayout(mergeDocument md, ISASConnection dbc,
	Hashtable par) {

try {
	Hashtable htxt = new Hashtable();
	htxt.put("#txt#", getConfString(dbc, "ragione_sociale"));
	codice_regione = getConfString(dbc, "codice_regione");
	codice_usl = getConfString(dbc, "codice_usl");
        ServerUtility su = new ServerUtility();
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));

	String data_inizio = (String)par.get("data_inizio");
        String data = data_inizio.substring(8, 10)+"/"+
		data_inizio.substring(5, 7)+"/"+data_inizio.substring(0, 4);
        htxt.put("#data_inizio#",data);

	String data_fine = (String)par.get("data_fine");
        data = data_fine.substring(8, 10)+"/"+
		data_fine.substring(5, 7)+"/"+data_fine.substring(0, 4);
        htxt.put("#data_fine#", data);

	String tipo = (String)par.get("tipo");
	tipo = "METTI FIGURA_PROFESSIONALE!";	// DEBUG: Toglimi!!!
        htxt.put("#figura_professionale#", tipo);

	md.writeSostituisci("layout",htxt);
} catch(Exception e) {
	debugMessage("FoIPrestEJB.mkPrestSinLayout(): "+e);
	e.printStackTrace();
}
}

    private void FaiExcel(ISASConnection dbc, ISASCursor dbcur, mergeDocument doc, Hashtable par)
    throws Exception{
            Hashtable p = new Hashtable();
            String ragg = faiRaggruppamento((String)par.get("ragg"));
            p.put("#raggruppamento#", ragg);
            doc.writeSostituisci("iniziotab", p);
            while(dbcur.next())	{
                ISASRecord dbr=dbcur.getRecord();
                p.put("#descrizione_zona#", ""+util.getObjectField(dbr,"des_zona",'S'));
                p.put("#des_distr#", ""+util.getObjectField(dbr,"des_distretto",'S'));
                p.put("#descrizione#", ""+util.getObjectField(dbr,"descrizione",'S'));
                p.put("#codice_zona#", ""+util.getObjectField(dbr,"cod_zona",'S'));
                p.put("#codice_distretto#", ""+util.getObjectField(dbr,"cod_distretto",'S'));
                p.put("#codice#", ""+util.getObjectField(dbr,"codice",'S'));

                p.put("#nome_ass#", (String)dbr.get("cogn")+" "+(String)dbr.get("nomeut"));
                p.put("#cod_inf#", (String)dbr.get("int_cod_oper"));
                p.put("#desc_inf#", (String)dbr.get("opcogn")+" "+(String)dbr.get("opnome"));
		p.put("#data_nasc#", "("+getDateField(dbr, "data_nasc")+")");
		if (dbr.get("int_cartella")==null)
                    p.put("#n_cartella#", " ");
		else
                    p.put("#n_cartella#",((Integer)dbr.get("int_cartella")).toString());
                mkContattoExcel(dbc, dbr, doc, p);
                //System.out.println("Hash che mando in stampa cosa contiene:"+p.toString());
                //doc.writeSostituisci("tabella", p);
            }
            doc.write("finetab");
    }

private void mkSinTotali(mergeDocument md, String tipo,
	String old_desc, int v[]) {

	Hashtable ht = new Hashtable();
	int tm = 0, tf = 0;
	for (int i = 0; i < 3; i++) { tm = tm + v[i]; }
	for (int i = 3; i < 6; i++) { tf = tf + v[i]; }
	for (int i = 0; i < 6; i++) { ht.put("#t"+i+"#", ""+v[i]); }
	int tr = tm + tf;
	ht.put("#tm#", ""+tm);
	ht.put("#tf#", ""+tf);
	ht.put("#tr#", ""+tr);

	if (tipo.equals("C")) {
		ht.put("#comune#", old_desc);
		md.writeSostituisci("comune",ht);
	} else if (tipo.equals("Z")) {
		ht.put("#zona#", old_desc);
		md.writeSostituisci("totale_zona",ht);
	} else if (tipo.equals("D")) {
		ht.put("#distretto#", old_desc);
		md.writeSostituisci("totale_distretto",ht);
	} else if (tipo.equals("G")) {
		md.writeSostituisci("totale_generale",ht);
	}
}

private void mkSinIntesta(mergeDocument md, String tipo,
	String cur_desc, int v[]) {

	Hashtable ht = new Hashtable();
	if (tipo.equals("Z")) {
		ht.put("#zona#", cur_desc);
		md.writeSostituisci("zona",ht);
	} else if (tipo.equals("D")) {
		ht.put("#distretto#", cur_desc);
		md.writeSostituisci("distretto",ht);
	}
}
/*
private int getSinIndex(String sesso, int eta) {
	int base = 0;
	if (sesso.equals("F")) base = 3;

	if (eta <= 64) return base;
	if ((eta >= 65) && (eta <= 74)) return base + 1;
	return base + 2;
}
  commento 061003
private void mkPrestSinBody(mergeDocument md, ISASCursor dbcur,
	Hashtable par,ISASConnection dbc) throws Exception {

	boolean first_time = true;
	String old_zona = "*", cur_zona = "";
	String old_dist = "*", cur_dist = "";
	String old_comu = "*", cur_comu = "";

	int[] dist = new int[6]; int[] zona = new int[6];
	int[] comu = new int[6]; int[] tgen = new int[6];
	for (int i = 0; i < 6; i++) {
		dist[i] = 0; zona[i] = 0; comu[i] = 0; tgen[i] = 0;
	}

        ServerUtility su = new ServerUtility();
	String data_stampa = su.getTodayDate("yyyy-MM-dd");
	md.write("tabella");

try {
	while (dbcur.next()) {
		ISASRecord dbr = dbcur.getRecord();
		cur_zona = (String)dbr.get("descrizione_zona");
		cur_dist = (String)dbr.get("des_distr");
		cur_comu = (String)dbr.get("descrizione");
		if (first_time) {
			first_time = ! first_time;
		} else {
			if ((! old_zona.equals(cur_zona)) ||
				(! old_dist.equals(cur_dist)) ||
				(! old_comu.equals(cur_comu))) {
				mkSinTotali(md, "C", old_comu, comu);
				for (int i = 0; i < 6; i++) { comu[i] = 0; }
			}
			if ((! old_zona.equals(cur_zona)) ||
				(! old_dist.equals(cur_dist))) {

				mkSinTotali(md, "D", old_dist, dist);
				for (int i = 0; i < 6; i++) { dist[i] = 0; }
			}
			if (! old_zona.equals(cur_zona)) {
				mkSinTotali(md, "Z", old_zona, zona);
				for (int i = 0; i < 6; i++) { zona[i] = 0; }
			}
		}

		if (! old_zona.equals(cur_zona)) {
			mkSinIntesta(md, "Z", cur_zona, zona);
		}
		if ((! old_zona.equals(cur_zona)) ||
			(! old_dist.equals(cur_dist))) {
			mkSinIntesta(md, "D", cur_dist, dist);
		}

		int idx = getSinIndex((String) dbr.get("sesso"),
			this.ConvertData(
				((java.sql.Date)dbr.get("data_nasc")).toString(),
				data_stampa));
		zona[idx]++; dist[idx]++; comu[idx]++; tgen[idx]++;
		old_zona = cur_zona;
		old_dist = cur_dist;
		old_comu = cur_comu;
	}
} catch(Exception e) {
	debugMessage("FoIPrestEJB.mkPrestSinBody(): "+e);
	e.printStackTrace();
}
	if (! first_time) {
		mkSinTotali(md, "C", old_comu, comu);
		mkSinTotali(md, "D", old_dist, dist);
		mkSinTotali(md, "Z", old_zona, zona);
		mkSinTotali(md, "G", "", tgen);
	}
	md.write("finetab");
}

public byte[] query_elencoPrestSin(String utente, String passwd,
	Hashtable par,mergeDocument eve) throws SQLException {
	boolean done = false;
	ISASConnection dbc = null;
	try{
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);
		String sel = mkPrestSinSelect(dbc,par);
                debugMessage("FoIPrestEJB.query_elencoPrestSin(1): "+sel);
		ISASCursor dbcur = dbc.startCursor(sel);
                mkPrestSinLayout(eve, dbc, par);
		if (dbcur == null) {
			eve.write("messaggio");
			eve.write("finale");
			debugMessage("FoIPrestEJB.query_elencoPrestSin(): "+
				"cursore nullo ["+sel+"].");
		} else	{
			if (dbcur.getDimension() <= 0) {
				eve.write("messaggio");
			} else	{
				mkPrestSinBody(eve, dbcur,par,dbc);
			}	// fine if dbcur.getDimension()
			eve.write("finale");
		}
		dbcur.close();
		eve.close();

		System.out.println("FoIPrestEJB.query_elencoPrestSin(): "+
			"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		debugMessage("FoIPrestEJB.query_elencoPrestSin(): "+e);
		throw new SQLException("FoIPrestEJB.query_elencoPrestSin(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				System.out.println("FoIPrestEJB.query_elencoPrestSin(): "+e1);
			}
		}
	}
}

// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------
// ----------------------------------------------------------------------------

public byte[] OLD_query_elencoPrestSin(String utente, String passwd, Hashtable par,mergeDocument eve) throws SQLException {

	boolean done=false;
	ISASConnection dbc=null;
	String myselect="";
        ServerUtility su =new ServerUtility();
        String  data_inizio="";
        String  data_fine="";
	try{
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);

		myselect="SELECT DISTINCT c.data_nasc,c.sesso,distretti.des_distr,"+
			 "comuni.descrizione,zone.descrizione_zona,c.n_cartella "+
			 "FROM cartella c,interv i,distretti,operatori o,"+
			 "comuni,zone ";
                 String mywhere=" WHERE o.tipo='02' AND i.int_cod_oper=o.codice "+
                         "AND i.int_cartella=c.n_cartella";

                // controllo valore corretto data_fine
		data_fine=(String)(par.get("data_fine"));
		if (!(data_fine.equals("") || data_fine == null))
			mywhere=mywhere+" AND i.int_data_prest<="+formatDate(dbc,data_fine);
                // controllo valore corretto data_inizio
                  data_inizio=(String)(par.get("data_inizio"));
                  if (!(data_inizio.equals("") || data_inizio == null)){
                          mywhere=mywhere+" AND i.int_data_prest>="+formatDate(dbc,data_inizio);
                  }
                //controllo codice inizio (infermiere)
                mywhere = su.addWhere(mywhere, su.REL_AND, "o.codice", su.OP_GE_STR,
                        (String)par.get("codice_inizio"));
                //controllo codice fine (infermiere)
                mywhere = su.addWhere(mywhere, su.REL_AND, "o.codice", su.OP_LE_STR,
                        (String)par.get("codice_fine"));
                //controllo zona
                mywhere = su.addWhere(mywhere, su.REL_AND, "i.int_codzona", su.OP_EQ_STR,
                        (String)par.get("zona"));
                //controllo distretto
                mywhere = su.addWhere(mywhere, su.REL_AND, "i.int_coddistr", su.OP_EQ_STR,
                        (String)par.get("distretto"));
               //controllo comune
                mywhere = su.addWhere(mywhere, su.REL_AND, "i.int_codcomune", su.OP_EQ_STR,
                        (String)par.get("comune"));

		mywhere=mywhere +" AND i.int_coddistr=distretti.cod_distr"+
                " AND i.int_codzona=zone.codice_zona"+
                " AND i.int_codcomune=comuni.codice " +
		" ORDER BY zone.descrizione_zona,distretti.des_distr,"+
		"comuni.descrizione,c.sesso";
                myselect=myselect+ mywhere;

		ISASCursor dbcur=dbc.startCursor(myselect);
                System.out.println("FoIPrestEJB.query_elencoPrestSin(): DEBUG ["+myselect+"].");

//////                preparaLayout(eve, dbc,data_inizio,data_fine);
		mkLayout(dbc, par, eve);
		if (dbcur == null) {
			eve.write("messaggio");
			eve.write("finale");
			System.out.println("FoIPrestEJB.query_elencoPrestSin(): "+
				"cursore non valido ["+myselect+"].");
		} else	{
			if (dbcur.getDimension() <= 0) {
				eve.write("messaggio");
				eve.write("finale");
			} else	{
				preparaBodySin(eve, dbcur,par,dbc);
				eve.write("finale");
			}	// fine if dbcur.getDimension()
		}	// fine if dbcur
		dbcur.close();
		eve.close();
		System.out.println("FoIPrestEJB.query_elencoPrestSin(): DEBUG "+
			"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("FoIPrestEJB.query_elencoPrestSin(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				System.out.println("FoIPrestEJB.query_elencoPrestSin(): "+e1);
			}
		}
	}
}// End of query_elencoPrestSin() method


private void preparaBodySin(mergeDocument md, ISASCursor dbcur,Hashtable par,ISASConnection dbc)
 {
	try {
             boolean stampa_zona=false;
             boolean stampa_distretto=false;
             boolean stampa_comune=false;
             boolean stampato=false;
             int eta=0;
             String old_com="";
             String new_com="";
             String old_d="";
             String new_d="";
             String old_z="";
             String new_z="";
             int tot_parz=0;
	String data_stampa="";
	String datanascita="";
    	String scr = "";
    	String comune = "";
    	String distretto = "";
    	String zona = "";
	int m0=0;		int f0=0;
	int m64=0;		int f64=0;
	int m75=0;		int f75=0;
	int totm=0;		int totf=0;
        int totute=0;
        ServerUtility su =new ServerUtility();
        boolean primo_giro=true;
        while (dbcur.next()){
                ISASRecord dbr=dbcur.getRecord();
                if (primo_giro)
                {   primo_giro=false;
                    if (dbr.get("descrizione_zona")!=null)
                        if (!((String)dbr.get("descrizione_zona")).equals(""))
                                old_z=(String)dbr.get("descrizione_zona");
                    if (dbr.get("des_distr")!=null)
                         if (!((String)dbr.get("des_distr")).equals(""))
                                old_d=(String)dbr.get("des_distr");
                     if (dbr.get("descrizione")!=null)
                          if (!((String)dbr.get("descrizione")).equals(""))
                                old_com=(String)dbr.get("descrizione");
                      data_stampa=su.getTodayDate("yyyy-MM-dd");
                      datanascita=((java.sql.Date) dbr.get("data_nasc")).toString();
                      //calcolo dell'et�
                      eta=this.ConvertData(datanascita, data_stampa);
                      String sesso=(String)dbr.get("sesso");
                      if (sesso.equals("M")){
                              if(eta>0 && eta<=64)	m0=m0+1;
                              else if (eta>=65 && eta<=74)	m64=m64+1;
                              else if (eta>=75)	m75=m75+1;
                      }else{
                              if(eta>0 && eta<=64)    f0=f0+1;
                              else if (eta>=65 && eta<=74)     f64=f64+1;
                              else if (eta>=75)        f75=f75+1;
                      }
                      totm=m0+m64+m75;
                      totf=f0+f64+f75;
                      totute=totm+totf;
                }
                else
                {
                        if (dbr.get("descrizione_zona")!=null)
                            if (!((String)dbr.get("descrizione_zona")).equals(""))
                                    new_z=(String)dbr.get("descrizione_zona");
                            else new_z=" ";
                        else new_z=" ";
                        if (!new_z.equals(old_z))
                             stampa_zona=true;
                        if (dbr.get("des_distr")!=null)
                             if (!((String)dbr.get("des_distr")).equals(""))
                                    new_d=(String)dbr.get("des_distr");
                             else new_d=" ";
                        else new_d=" ";
                        if (!new_d.equals(old_d))
                               stampa_distretto=true;

                         if (dbr.get("descrizione")!=null)
                              if (!((String)dbr.get("descrizione")).equals(""))
                                    new_com=(String)dbr.get("descrizione");
                              else new_com=" ";
                         else new_com=" ";
                         if (!new_com.equals(old_com) )
                                   stampa_comune=true;
                         if (stampa_zona || stampa_distretto || stampa_comune){
                                      Hashtable harea=new Hashtable();
                                      harea.put("#descrizione_zona#",old_z);
                                      harea.put("#des_distr#",old_d);
                                      harea.put("#descrizione#",old_com);
                                      md.writeSostituisci("area",harea);
                                      stampa_zona=false;
                                      stampa_distretto=false;
                                      stampa_comune=false;
                                      Hashtable p=new Hashtable();
                                      p.put("#utenti#",(new Integer(totute)).toString());
                                      String sm0=(new Integer(m0)).toString();
                                      p.put("#m0-64#",sm0);
                                      String sm64=(new Integer(m64)).toString();
                                      p.put("#m65-74#",sm64);
                                      String sm75=(new Integer(m75)).toString();
                                      p.put("#m75#",sm75);
                                      String sf0=(new Integer(f0)).toString();
                                      p.put("#f0-64#",sf0);
                                      String sf64=(new Integer(f64)).toString();
                                      p.put("#f65-74#",sf64);
                                      String sf75=(new Integer(f75)).toString();
                                      p.put("#f75#",sf75);
                                      md.write("inizioprimatab");
                                      md.writeSostituisci("primatabella",p);
                                      md.write("finetab");
                                      String smtot=(new Integer(totm)).toString();
                                      p.put("#utenti#",(new Integer(totute)).toString());
                                      p.put("#maschi#",smtot);
                                      String sftot=(new Integer(totf)).toString();
                                      p.put("#femmine#",sftot);
                                      md.write("iniziosecondatab");
                                      md.writeSostituisci("secondatabella",p);
                                      md.write("finetab");
                                      tot_parz=0;
                                      totm=0;
                                      totf=0;
                                      f0=0;
                                      f64=0;
                                      f75=0;
                                      m0=0;
                                      m64=0;
                                      m75=0;
                                  }
                                datanascita=((java.sql.Date) dbr.get("data_nasc")).toString();
                                //calcolo dell'et�
                                eta=this.ConvertData(datanascita, data_stampa);
                                String sesso=(String)dbr.get("sesso");
                                if (sesso.equals("M")){
                                        if(eta>0 && eta<=64)	m0=m0+1;
                                        else if (eta>=65 && eta<=74)	m64=m64+1;
                                        else if (eta>=75)	m75=m75+1;
                                }else{
                                        if(eta>0 && eta<=64)    f0=f0+1;
                                        else if (eta>=65 && eta<=74)     f64=f64+1;
                                        else if (eta>=75)        f75=f75+1;
                                }
                                totm=m0+m64+m75;
                                totf=f0+f64+f75;
                                totute=totm+totf;
                                old_d=new_d;
                                old_z=new_z;
                                old_com=new_com;
                        }
            }//end while
              Hashtable harea=new Hashtable();
              harea.put("#descrizione_zona#",old_z);
              harea.put("#des_distr#",old_d);
              harea.put("#descrizione#",old_com);
              md.writeSostituisci("area",harea);
              stampa_zona=false;
              stampa_distretto=false;
              stampa_comune=false;
              Hashtable p=new Hashtable();
              p.put("#utenti#",(new Integer(totute)).toString());
              String sm0=(new Integer(m0)).toString();
              p.put("#m0-64#",sm0);
              String sm64=(new Integer(m64)).toString();
              p.put("#m65-74#",sm64);
              String sm75=(new Integer(m75)).toString();
              p.put("#m75#",sm75);
              String sf0=(new Integer(f0)).toString();
              p.put("#f0-64#",sf0);
              String sf64=(new Integer(f64)).toString();
              p.put("#f65-74#",sf64);
              String sf75=(new Integer(f75)).toString();
              p.put("#f75#",sf75);
              md.write("inizioprimatab");
              md.writeSostituisci("primatabella",p);
              md.write("finetab");
              String smtot=(new Integer(totm)).toString();
              p.put("#utenti#",(new Integer(totute)).toString());
              p.put("#maschi#",smtot);
              String sftot=(new Integer(totf)).toString();
              p.put("#femmine#",sftot);
              md.write("iniziosecondatab");
              md.writeSostituisci("secondatabella",p);
              md.write("finetab");
             Hashtable gen=new Hashtable();
             int ut=dbcur.getDimension();
             String utenti=(new Integer(ut)).toString();
             gen.put("#totgen#",utenti);
             md.writeSostituisci("totgen",gen);

	} catch (Exception ex) {
		Hashtable p = new Hashtable();
		p.put("#descrizione_zona#","");
		p.put("#des_distr#","");
		p.put("#descrizione#", "");
		md.writeSostituisci("area", p);
		md.write("inizioprimatab");
                p.put("#utenti#", "");
     		p.put("#m0-64#", "");
                p.put("#m65-74#", "");
                p.put("#m75#", "");
                p.put("#f0-64#", "");
                p.put("#f65-74#", "");
                p.put("#f75#", "");
		md.writeSostituisci("primatabella", p);
                md.write("finetab");
                md.write("iniziosecondatab");
                p.put("#utenti#","");
                p.put("#maschi#","");
                p.put("#femmine#","");
		md.writeSostituisci("secondatabella", p);
                md.write("finetab");
                p.put("#num_assistiti#","");
                p.put("#totgen#","*** errore in lettura: "+ex+" ***");
                md.writeSostituisci("totgen", p);
	}
}*/
}	// End of FoIPrest class

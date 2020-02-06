package it.caribel.app.sinssnt.bean;

// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 20/05/2004 - EJB di connessione alla procedura SINS Tabella FoSkMedi
//
// Jessica 06/05/04
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

import java.text.NumberFormat;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.caribel.app.sinssnt.comuni_nascita.ComuniNascita;


public class FoSkMediEJB extends SINSSNTConnectionEJB {

		// 22/11/06 : sostituito campi su SKPATOLOGIE con nuova tabella DIAGNOSI.

public FoSkMediEJB() {}

	// 05/02/13
	private EveUtils eveUtl = new EveUtils();

//dati che arrivano dalla chiamata della stampa
public ISASRecord dbana=null;
public String n_cartella="";
public String n_contatto="";
public String data_apertura="";
public String data_chiusura=""; 
public String data_scheda="";//arriva solo nel caso di stampa singola

public String intesta_nome="";

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

private String dataIta(String dateita)
{
    if ( dateita!=null ){
            if (dateita.length()==10)
                dateita=dateita.substring(8,10)+"/"+
                       dateita.substring(5,7)+"/"+dateita.substring(0,4);
    }
    return dateita;
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

/***
/*	Restituisce l'hashtable con i valori vuoti da stampare se non trova il record
*/
private Hashtable faiVuota(Hashtable h,String[] campi) {
	for(int i=0;i<campi.length;i++){
		h.put("#"+campi[i]+"#","");
	}
   return h;
}


private String getDecodifica(ISASConnection dbc,String campo1, String campo2,
			     String tabella, String codice) throws Exception {
String decod="";
try{
	if(!codice.equals("")){
		String sel = "SELECT "+campo1+" FROM "+tabella+
			" WHERE "+campo2+" = '"+codice+"'";
		debugMessage("FoSkMediEJB.getDecodifica(): "+sel);
		ISASRecord dbcom = dbc.readRecord(sel);
		decod=(String)dbcom.get(campo1);
	     }
} catch(Exception e) {
	debugMessage("getDecodifica("+dbc+", "+codice+"): "+e);
	return "";
}
   return decod;
}

private String getOperatore(ISASConnection dbc, String codice) throws Exception {
String decod="";
try{
		String sel = "SELECT cognome,nome FROM operatori "+
			"WHERE codice = '"+codice+"'";
		debugMessage("FoEleSocEJB.getOperatore(): "+sel);
		ISASRecord dbcom = dbc.readRecord(sel);
		String cognome=(String)dbcom.get("cognome");
		String nome=(String)dbcom.get("nome");
		decod=cognome + " " + nome;
} catch(Exception e) {
	debugMessage("getOperatore("+dbc+", "+decod+"): "+e);
	return "";
}
   return decod;
}

/*** 22/11/06
private String getPato(ISASConnection dbc, String codice) throws Exception {
String decod="";
try{
		String sel = "SELECT diagnosi FROM icd9 "+
      			     "WHERE cd_diag = '"+codice+"'";
		debugMessage("FoSkMediEJB.getPato(): "+sel);
		ISASRecord dbcom = dbc.readRecord(sel);
		decod=(String)dbcom.get("diagnosi");
} catch(Exception e) {
	debugMessage("getPato("+dbc+", "+decod+"): "+e);
	return "";
}
   return decod;
}
***/
// 22/11/06
private String getDiagnosi(ISASConnection dbc, String codice) throws Exception {
String decod="";
try{
		String sel = "SELECT diagnosi FROM tab_diagnosi "+
      			     "WHERE cod_diagnosi = '"+codice+"'";
		debugMessage("FoSkMedPalEJB.getDiagnosi(): "+sel);
		ISASRecord dbcom = dbc.readRecord(sel);
		decod=(String)dbcom.get("diagnosi");
} catch(Exception e) {
	debugMessage("getDiagnosi("+decod+"): "+e);
	return "";
}
   return decod;
}

private void mkLayout(ISASConnection dbc,mergeDocument doc) {
	ServerUtility su = new ServerUtility();
	Hashtable ht = new Hashtable();
	String cognome="";
	String nome="";
	String tipo="";
try{
	ht.put("#txt#", getConfStringField(dbc));
}catch(Exception e) {ht.put("#txt#", " ");}
	ht.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	doc.writeSostituisci("layout",ht);
}

private String getConfStringField(ISASConnection dbc) throws Exception {
	String sel = "SELECT conf_txt from conf "+
		"WHERE conf_kproc = 'SINS' AND "+
		"conf_key='ragione_sociale'";
	debugMessage("FoSkMediEJB.getRagioneSociale(): "+sel);
	ISASRecord dbconf = dbc.readRecord(sel);
	return (String)dbconf.get("conf_txt");
}

private void leggiAnagrafica(ISASConnection dbc)
throws SQLException {
try{

	String nome=""; String cognome="";

	String sel="SELECT ac.*, c.* "+
	" FROM cartella c, anagra_c ac"+
	" WHERE c.n_cartella='"+n_cartella+"'"+
	" AND ac.n_cartella='"+n_cartella+"'"+
	" AND ac.data_variazione IN ( SELECT MAX (data_variazione) "+
 	" FROM anagra_c WHERE n_cartella ='"+n_cartella+"')";

	dbana = dbc.readRecord(sel);
	if (dbana != null) {
	   cognome = getStringField(dbana, "cognome");
	   nome    = getStringField(dbana, "nome");
	   intesta_nome=cognome+" "+nome;
	}

} catch(Exception e) {
	debugMessage("leggiAnagrafica("+dbc+" ) : "+e);
}

}

private void scriviPaginaIniziale(ISASConnection dbc, mergeDocument eve)
throws SQLException {
   ISASRecord dbana=null;
   String cartella="";		String data_nasc="";
   String cod_sanitario="";	String cod_fiscale="";
   String comune_nascita="";	String cittadinanza="";
   String cognome="";		String nome="";
   String residenza="";		String comune_res="";
   String prov_res="";		String local_res="";
   String domicilio="";		String comune_dom="";
   String tel_dom="";		String prov_dom="";
   String local_dom="";		String nome_camp="";

   String sessoF="";		String sessoM="";
try{
	String sel="SELECT ac.*, c.* "+
	" FROM cartella c, anagra_c ac"+
	" WHERE c.n_cartella='"+n_cartella+"'"+
	" AND ac.n_cartella='"+n_cartella+"'"+
	" AND ac.data_variazione IN ( SELECT MAX (data_variazione) "+
 	" FROM anagra_c WHERE n_cartella ='"+n_cartella+"')";

	dbana = dbc.readRecord(sel);

	cartella       = getStringField(dbana, "n_cartella");
	data_nasc      = getDateField(dbana, "data_nasc");
	cod_sanitario  = getStringField(dbana, "cod_reg");
	cod_fiscale    = getStringField(dbana, "cod_fisc");
//	comune_nascita = getDecodifica(dbc,"descrizione","codice",
//					"comuni",getStringField(dbana, "cod_com_nasc"));
	String codComNascita = ISASUtil.getValoreStringa(dbana,"cod_com_nasc");
	String dtNascita = ISASUtil.getValoreStringa(dbana, "data_nasc");
	comune_nascita = ComuniNascita.getDecodeComuneNascita(dbc, codComNascita, dtNascita);
	
	cittadinanza   = getDecodifica(dbc,"des_cittadin","cd_cittadin",
				       "cittadin",getStringField(dbana, "cittadinanza"));
	cognome        = getStringField(dbana, "cognome");
	nome           = getStringField(dbana, "nome");
	residenza      = getStringField(dbana, "indirizzo");
	comune_res     = getDecodifica(dbc,"descrizione","codice",
					"comuni",getStringField(dbana, "citta"));
	tel_dom        = getStringField(dbana, "telefono1");
	prov_res       = getStringField(dbana, "prov");
	local_res      = getStringField(dbana, "localita");
	domicilio      = getStringField(dbana, "dom_indiriz");
	comune_dom     = getDecodifica(dbc,"descrizione","codice",
					"comuni",getStringField(dbana, "dom_citta"));
	prov_dom       = getStringField(dbana, "dom_prov");
	local_dom      = getStringField(dbana, "dom_localita");
	nome_camp      = getStringField(dbana, "nome_camp");
	sessoF         = faiCrocetta("F",getStringField(dbana, "sesso"));
	sessoM         = faiCrocetta("M",getStringField(dbana, "sesso"));

} catch(Exception e) {
	debugMessage("scriviPaginaIniziale("+dbc+" ) : "+e);
}

   Hashtable ht = new Hashtable();
   intesta_nome=cognome+" "+nome;
   ht.put("#n_cartella#",cartella);		ht.put("#data_nasc#",data_nasc);
   ht.put("#cod_sanitario#",cod_sanitario);	ht.put("#cod_fiscale#",cod_fiscale);
   ht.put("#comune_nascita#",comune_nascita);	ht.put("#cittadinanza#",cittadinanza);
   ht.put("#cognome#",cognome);		        ht.put("#nome#",nome);
   ht.put("#residenza#",residenza);		ht.put("#comune_res#",comune_res);
   ht.put("#tel_dom#",tel_dom);			ht.put("#prov_res#",prov_res);
   ht.put("#local_res#",local_res);
   ht.put("#domicilio#",domicilio);		ht.put("#comune_dom#",comune_dom);
   ht.put("#tel_dom#",tel_dom);			ht.put("#prov_dom#",prov_dom);
   ht.put("#local_dom#",local_dom);		ht.put("#nome_camp#",nome_camp);
   ht.put("#sessof#",sessoF);			ht.put("#sessom#",sessoM);
   eve.writeSostituisci("datianagrafici",ht);
   queryEsenzioni(dbc,dbana,eve);
   eve.write("fineTabPaginaIniziale");
}

private void scriviPaginaSchedaMedico(ISASConnection dbc, mergeDocument eve, ISASRecord dbski)
throws SQLException {
   String data_ape="";		String operatore="";
   String descr_contatto="";    String medico="";
   String anamnesi_1="";	String anamnesi_2="";
   String anamnesi_3="";        String data_uscita="";
   String data_ref="";
	// 05/02/13
	String presidioCont = "";
try{
	data_ape       = getDateField(dbski, "skm_data_apertura");
	operatore      = getOperatore(dbc,getStringField(dbski, "cod_operatore"));
	descr_contatto = getStringField(dbski, "skm_descr_contatto");
	medico         = getOperatore(dbc,getStringField(dbski, "skm_medico"));
	anamnesi_1     = getStringField(dbski, "skm_anamnesi1");
	anamnesi_2     = getStringField(dbski, "skm_anamnesi2");
	anamnesi_3     = getStringField(dbski, "skm_anamnesi3");
	data_uscita    = getDateField(dbski, "skm_data_chiusura");
        data_ref       = getDateField(dbski, "skm_medico_da");
	// 05/02/13
	presidioCont = decodPresidio(dbc, (String)dbski.get("skm_cod_presidio"), (String)dbski.get("skm_medico"));		
} catch(Exception e) {
	debugMessage("scriviPaginaSchedaMedi("+dbc+" ) : "+e);
}
   Hashtable ht = new Hashtable();
   ht.put("#n_cartella#",n_cartella);		ht.put("#nome_assistito#",intesta_nome);
   ht.put("#n_contatto#",n_contatto);		ht.put("#descr_conta#",descr_contatto);
   ht.put("#data_apertura_contatto#",data_ape);
   ht.put("#data_aper#",data_ape);	        ht.put("#operatore#",operatore);
   ht.put("#med_referente#",medico);            ht.put("#anamnesi1#",anamnesi_1);
   ht.put("#anamnesi2#",anamnesi_2);	        ht.put("#anamnesi3#",anamnesi_3);
   ht.put("#data_uscita#",data_uscita);	        ht.put("#data_ref#",data_ref);
	// 05/02/13
	ht.put("#presidio_cont#", presidioCont);   
	
   eve.writeSostituisci("paginaSchedaMed",ht);
}

	private void scriviPaginaDiagnosi(ISASConnection dbc, mergeDocument eve, ISASRecord dbski) throws SQLException 
	{
	   	Hashtable ht = new Hashtable();
	   	String note="";
	   	String data_note="";
		try{
			data_note       = getDateField(dbski, "skm_data_diag");
			note            = getStringField(dbski, "skm_nota_diag");
/*** 22/11/06
	String selPato  = "SELECT * FROM skpatologie WHERE "+
                          "n_cartella="+n_cartella+" AND "+
                          "n_contatto="+n_contatto;
        ISASRecord dbPato=dbc.readRecord(selPato);
        if(dbPato!=null){
          for (int i=1; i<=4; i++){
            ht.put("#pato"+i+"#",getPato(dbc,getStringField(dbPato, "skpat_patol"+i)));
          }
          if ((getStringField(dbPato, "skpat_conf_med")).equals("S"))
                conferma="SI";
          else  conferma="NO";
        }else{
          for (int i=0; i<=4; i++){
            ht.put("#pato"+i+"#","");
          }
          conferma="NO";
        }
} catch(Exception e) {
	debugMessage("scriviPaginaDiagnosi("+dbc+" ) : "+e);
}
   ht.put("#n_cartella#",n_cartella);	ht.put("#nome_assistito#",intesta_nome);
   ht.put("#n_contatto#",n_contatto);	ht.put("#data_apertura_contatto#",dataIta(data_apertura));
   ht.put("#note#",note);		ht.put("#data_note#",data_note);
   if (conferma.equals("SI")){
      ht.put("#pp1_n#","");
      ht.put("#pp1_s#","X");
   }else{
      ht.put("#pp1_n#","X");
      ht.put("#pp1_s#","");
   }
***/

			String select = "SELECT * FROM diagnosi" +
						" WHERE n_cartella = " + n_cartella;

			if ((data_chiusura != null) && (!data_chiusura.trim().equals("")))
				select += " AND data_diag <= " + formatDate(dbc,data_chiusura);
		
			select += " ORDER BY data_diag DESC";
			ISASCursor dbcur = dbc.startCursor(select);

			if ((dbcur != null) && (dbcur.getDimension() > 0)) {
				// inizio
		   		ht.put("#n_cartella#", n_cartella);	
		   		ht.put("#n_contatto#", n_contatto);	
				ht.put("#nome_assistito#", intesta_nome);
				ht.put("#data_apertura_contatto#", dataIta(data_apertura));
		   		ht.put("#note#", note);		
				ht.put("#data_note#", data_note);
				eve.writeSostituisci("paginaPatologia", ht);

				while (dbcur.next()) {
					ISASRecord dbr = dbcur.getRecord();
					Hashtable h_xstampa = new Hashtable();
					h_xstampa.put("#data_diag#", getDateField(dbr, "data_diag"));
					for (int j=1; j<6; j++) {
						h_xstampa.put("#diag"+j+"#", getStringField(dbr, ("diag"+j)));
						h_xstampa.put("#desc_diag"+j+"#", getDiagnosi(dbc, getStringField(dbr, ("diag"+j))));
						h_xstampa.put("#diag"+j+"_ids#", getStringField(dbr, ("diag"+j+"_ids")));
					}
	   				eve.writeSostituisci("rigaPatologia", h_xstampa);	
			   	}
				eve.write("fineTabPatologia");
			}
		} catch(Exception e) {
			debugMessage("scriviPaginaDiagnosi("+dbc+" ) : "+e);
		}
	}

private void scriviPaginaTerapia(ISASConnection dbc, mergeDocument eve,ISASRecord dbski)
throws SQLException {
   String data_ter="";	String note_ter="";
try{
	note_ter=(String)dbski.get("skm_nota_terapia");
        data_ter=getDateField(dbski, "skm_data_terapia");
        Hashtable ht = new Hashtable();
	ht.put("#n_cartella#",n_cartella);		ht.put("#nome_assistito#",intesta_nome);
	ht.put("#n_contatto#",n_contatto);		ht.put("#data_apertura_contatto#",dataIta(data_apertura));
	ht.put("#note_terapia#",note_ter);              ht.put("#data_terapia#",data_ter);
        eve.writeSostituisci("paginaTerapia",ht);

	String sel="SELECT * FROM skmterapia "+
                   " WHERE n_cartella="+n_cartella+
                   " AND n_contatto="+n_contatto;
        ISASCursor dbcur=dbc.startCursor(sel);
	if (dbcur == null) {
		Hashtable hterr = new Hashtable();
                hterr.put("#data_inizio#","");	hterr.put("#nome_comm#","");
		hterr.put("#principio#","");	hterr.put("#data_fine#","");
                hterr.put("#categoria#","");    hterr.put("#dom#","");
		eve.writeSostituisci("tabella", hterr);
		System.out.println("FoSkMediEJB.scriviPaginaTerapia(): "+
			"cursore non valido");
	} else	{
		if (dbcur.getDimension() <= 0) {
                  Hashtable hterr = new Hashtable();
                  hterr.put("#data_inizio#","");	hterr.put("#nome_comm#","");
                  hterr.put("#principio#","");	        hterr.put("#data_fine#","");
                  hterr.put("#categoria#","");          hterr.put("#dom#","");
                  eve.writeSostituisci("tabella", hterr);
		} else{
                    while (dbcur.next()){
                      ISASRecord dbska=dbcur.getRecord();
                      Hashtable ht_a = new Hashtable();
                      ht_a.put("#data_inizio#",getDateField(dbska, "skt_data_inizio"));
                      ht_a.put("#data_fine#",getDateField(dbska, "skt_data_fine"));
                      ht_a.put("#nome_comm#",getStringField(dbska, "skt_nome"));
                      ht_a.put("#principio#",getStringField(dbska, "skt_principio"));
                      ht_a.put("#categoria#",getStringField(dbska, "skt_cat_atc"));
                      if((getStringField(dbska, "skt_cons_dom")).equals("S"))
                              ht_a.put("#dom#","SI");
                      else    ht_a.put("#dom#","NO");
                      eve.writeSostituisci("tabella",ht_a);
                    }
	        }
	}
} catch(Exception e) {
	debugMessage("scriviPaginaTerapia("+dbc+" ) : "+e);
}
   eve.write("fineTabPaginaTerapia");
}

private void scriviPaginaDiaria(ISASConnection dbc, mergeDocument eve)
throws SQLException {
try{
	Hashtable ht = new Hashtable();
	ht.put("#n_cartella#",n_cartella);		ht.put("#nome_assistito#",intesta_nome);
	ht.put("#n_contatto#",n_contatto);		ht.put("#data_apertura_contatto#",dataIta(data_apertura));
        eve.writeSostituisci("paginaDiaria",ht);

	String sel="SELECT * FROM skmdiaria "+
                   " WHERE n_cartella="+n_cartella+
                   " AND n_contatto="+n_contatto;

	ISASCursor dbcur=dbc.startCursor(sel);
	if (dbcur == null) {
		Hashtable hterr = new Hashtable();
   		hterr.put("#data_diaria#","");	hterr.put("#note_diaria#","");
		eve.writeSostituisci("tabellaDiaria", hterr);
		System.out.println("FoSkMediEJB.scriviPaginaDiaria(): "+
			"cursore non valido");
	} else	{
		if (dbcur.getDimension() <= 0) {
                  Hashtable hterr = new Hashtable();
                  hterr.put("#data_diaria#","");  hterr.put("#note_diaria#","***ERRORE***");
                  eve.writeSostituisci("tabellaDiaria", hterr);
		} else{
                    while (dbcur.next()){
                      ISASRecord dbska=dbcur.getRecord();
                      Hashtable ht_a = new Hashtable();
                      ht_a.put("#data_diaria#",getDateField(dbska, "skd_data"));
                      ht_a.put("#note_diaria#",getStringField(dbska, "skd_nota"));
                      eve.writeSostituisci("tabellaDiaria",ht_a);
                    }
	        }
	}
} catch(Exception e) {
	debugMessage("scriviPaginaDiaria("+dbc+" ) : "+e);
}
   eve.write("fineTabPaginaDiaria");
}

private void scriviPaginaClinica(ISASConnection dbc, mergeDocument eve)
throws SQLException {
try{
	Hashtable ht = new Hashtable();
	ht.put("#n_cartella#",n_cartella);		ht.put("#nome_assistito#",intesta_nome);
	ht.put("#n_contatto#",n_contatto);		ht.put("#data_apertura_contatto#",dataIta(data_apertura));
        eve.writeSostituisci("paginaClinica",ht);

	String sel="SELECT * FROM skmrelcli "+
                   " WHERE n_cartella="+n_cartella+
                   " AND n_contatto="+n_contatto;
//	" AND ski_data_apertura="+formatDate(dbc,data_apertura);

	ISASCursor dbcur=dbc.startCursor(sel);
	if (dbcur == null) {
		Hashtable hterr = new Hashtable();
   		hterr.put("#data_clinica#","");	hterr.put("#note_clinica#","");
		eve.writeSostituisci("tabellaClinica", hterr);
		System.out.println("FoSkMediEJB.scriviPaginaDiaria(): "+
			"cursore non valido");
	} else	{
		if (dbcur.getDimension() <= 0) {
                  Hashtable hterr = new Hashtable();
                  hterr.put("#data_clinica#","");  hterr.put("#note_clinica#","***ERRORE***");
                  eve.writeSostituisci("tabellaClinica", hterr);
		} else{
                    while (dbcur.next()){
                      ISASRecord dbska=dbcur.getRecord();
                      Hashtable ht_a = new Hashtable();
                      ht_a.put("#data_clinica#",getDateField(dbska, "skr_data"));
                      ht_a.put("#note_clinica#",getStringField(dbska, "skr_nota"));
                      eve.writeSostituisci("tabellaClinica",ht_a);
                    }
	        }
	}
} catch(Exception e) {
	debugMessage("scriviPaginaClinica("+dbc+" ) : "+e);
}
   eve.write("fineTabPaginaClinica");
}

private void scriviPagine(ISASConnection dbc,char carattere, mergeDocument eve,
                          Hashtable par, ISASRecord dbr)
throws  SQLException {

    ISASRecord dbrAnagra=null;
    ISASRecord dbrEse=null;
    String sel="";
    try{
            switch (carattere)
            {
                case 'A':
                    //scrivo PAGINA INIZIALE
                    scriviPaginaIniziale(dbc,eve);
                break;
                case 'B':
                    //scrivo PAGINA SCHEDA MEDICO
                    scriviPaginaSchedaMedico(dbc,eve,dbr);
                break;
                case 'C':
                    //scrivo PAGINA DIAGNOSI SCHEDA MEDICO
                    scriviPaginaDiagnosi(dbc,eve,dbr);
                break;
                case 'D':
                    //scrivo PAGINA TERAPIA SCHEDA MEDICO
                    scriviPaginaTerapia(dbc,eve,dbr);
                break;
                case 'E':
                    //scrivo PAGINA DIARIA SCHEDA MEDICO
                    scriviPaginaDiaria(dbc,eve);
                break;
                case 'F':
                    //scrivo PAGINA CLINICA SCHEDE MEDICO
                    scriviPaginaClinica(dbc,eve);
                break;
            }
      }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SKMEDI :Errore eseguendo una scriviPagine()  ");
    }


}

public byte[] query_skmed(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {

	System.out.println("SINSFoSkMedi.query_skmed(): DEBUG inizio...");
	boolean done=false;
	ISASConnection dbc=null;

        String pagine=(String)par.get("pagine");
        char[] numpagine=pagine.toCharArray();

       n_cartella    =(String)par.get("n_cart");
       n_contatto    =(String)par.get("n_conta");
       data_apertura =(String)par.get("data_apertura");
       data_scheda =(String)par.get("data_scheda");
		data_chiusura = (String)par.get("data_chiusura"); // 22/11/06 m.

	try {
               myLogin lg = new myLogin();
               lg.put(utente,passwd);
               dbc = super.logIn(lg);
               leggiAnagrafica(dbc);
                System.out.println("PAGINE"+numpagine.length);
                String mysel = "SELECT * FROM skmedico WHERE "+
                "n_cartella="+n_cartella+" AND n_contatto="+n_contatto+
                " AND skm_data_apertura="+formatDate(dbc,data_apertura);
                System.out.println("FoSkMedi=>select:"+mysel);
                ISASRecord dbr = dbc.readRecord(mysel);
                for (int i=0; i<numpagine.length;i++)
                {
                       if (i==0)
				mkLayout(dbc,eve);

                       scriviPagine(dbc,numpagine[i],eve,par,dbr);
                       System.out.println("PAGINE"+numpagine[i]);
		  if (i<numpagine.length-1)
                       eve.write("taglia");
                }
                eve.write("finale");
		eve.close();

	//System.out.println("SINSFoSkinfe.query_skinfe(): DEBUG "+
	//		"documento restituito ["+(new String(eve.get()))+"]");

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("SINSFoSkMedi.query_skmed(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				System.out.println("SINSFoSkMedi.query_skmed(): "+e1);
			}
		}
	}
}	// End of query_skmed() method

private void queryEsenzioni(ISASConnection dbc,ISASRecord dbr, mergeDocument doc)
throws  SQLException
{
Hashtable ht = new Hashtable();
try
 {
	String select = "SELECT  es_data_inizio,es_data_fine,cod_esenzione FROM anagra_esenzioni WHERE cod_usl='"+(String)((ISASRecord)dbr).get("cod_usl") +"'";
	ISASCursor dbcur = dbc.startCursor(select);

	if(dbcur!=null){
		while (dbcur.next()) {
			ISASRecord dbese = dbcur.getRecord();

		ht.put("#data_inizio_ese#",getDateField(dbese, "es_data_inizio"));
		ht.put("#data_fine_ese#",getDateField(dbese, "es_data_fine"));
		ht.put("#esenzione_des#",getDecodifica(dbc,"descrizione","cod_esenzione",
			"esenzioni",getStringField(dbese, "cod_esenzione")));
		doc.writeSostituisci("esenzioni", ht);
                }
   }else{//stampa riga vuota
		ht.put("#data_inizio_ese#",""); ht.put("#data_fine_ese#",""); ht.put("#esenzione_des#","");
		doc.writeSostituisci("esenzioni", ht);
	}

}catch(Exception e){
    e.printStackTrace();
    throw new SQLException("DEBUG FoSkMedi :Errore eseguendo una query_esenzioni()  ");
}

}
/***
/*	Controlla il valore della casella e il valore preso dal DB
/*	e se corrispondono inserisce la X nel campo a video
/*
*/
private String faiCrocetta(String valore, String crocetta) {
String stringa="";
	if(!crocetta.equals("")){
		if(crocetta.equals(valore))
			stringa="X";
	}
return stringa;
}

	// 05/02/13
	private String decodPresidio(ISASConnection dbc, String codPres, String codOper) throws Exception
	{
		String ret = "";
		Hashtable h_conf = eveUtl.leggiConf(dbc, codOper, new String[]{"codice_regione", "codice_usl"});
			
		StringBuffer strBufSel = new StringBuffer("SELECT * FROM presidi WHERE codreg = '");
		strBufSel.append((String)h_conf.get("codice_regione"));
		strBufSel.append("' AND codazsan = '");
		strBufSel.append((String)h_conf.get("codice_usl"));
		strBufSel.append("' AND codpres = '");
		strBufSel.append(codPres);
		strBufSel.append("'");
			
		ISASRecord dbr = dbc.readRecord(strBufSel.toString());
		if ((dbr != null) && (dbr.get("despres") != null))
			ret = ((String)dbr.get("despres")).trim();
		return ret;
	}	

}	// End of FoSkMedi class

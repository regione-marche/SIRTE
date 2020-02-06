package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 20/05/2004 - EJB di connessione alla procedura SINS Tabella FoSkFisio
//
// Ilaria Mancini
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.text.NumberFormat;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.caribel.app.sinssnt.comuni_nascita.ComuniNascita;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.merge.*;	// fo merge
import it.pisa.caribel.util.*;

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

import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;

public class FoSkFisioEJB extends SINSSNTConnectionEJB {

	// 05/02/13
	private EveUtils eveUtl = new EveUtils();
	
public FoSkFisioEJB() {}
private final String SI="x";
private final String NO="";

public String n_cartella="";
public String n_contatto="";
public String data_apertura="";
public String data_chiusura="";
public String data_scheda="";
public String intesta_nome="";


private void preparaLayout(mergeDocument doc, ISASConnection dbc) {
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
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	doc.writeSostituisci("layout",htxt);
}



public byte[] query_skfisio(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {

	System.out.println("SINSFoSkFisio.query_fisio(): DEBUG inizio...");
	boolean done=false;
	ISASConnection dbc=null;

        String pagine=(String)par.get("pagine");
        char[] numpagine=pagine.toCharArray();

       n_cartella    =(String)par.get("n_cart");
       n_contatto    =(String)par.get("n_conta");
       data_apertura =(String)par.get("data_apertura");
       data_scheda =(String)par.get("data_scheda");
		data_chiusura = (String)par.get("data_chiusura"); 

       //fai select
	try {
               myLogin lg = new myLogin();
               lg.put(utente,passwd);
               dbc = super.logIn(lg);
               String select=mkSelect(par);
               ISASRecord dbr= dbc.readRecord(select);

				if (dbr != null)
					intesta_nome=getStringField(dbr, "cognome")+" "+getStringField(dbr, "nome"); 

               for (int i=0; i<numpagine.length;i++)
               {
                       if (i==0)
				preparaLayout(eve,dbc);
                       scriviPagine(dbr,numpagine[i],eve,dbc);
                       System.out.println("PAGINE"+numpagine[i]);
		  if (i<numpagine.length-1)
                       eve.write("taglia");
                }
                eve.write("finale");
		eve.close();

		dbc.close();
		super.close(dbc);
		done=true;
		return eve.get();	// restituisco il bytearray
	} catch(Exception e) {
		e.printStackTrace();
		throw new SQLException("SINSFoSkFisio.query_fisio(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){
				System.out.println("SINSFoSkFisio.query_fisio(): "+e1);
			}
		}
	}
}	// End of query_fisio() method

private void scriviPaginaIniziale(ISASRecord dbr,mergeDocument eve,ISASConnection dbc)
throws SQLException {
try{
    //Anagrafica
    Hashtable pagina = new Hashtable();
    pagina=riempiCampo(dbr,"n_cartella","#n_cartella#",pagina,"I");
    pagina=riempiCampo(dbr,"cognome","#cognome#",pagina,"S");
    pagina=riempiCampo(dbr,"nome","#nome#",pagina,"S");
    pagina=riempiCampo(dbr,"cod_reg","#cod_sanitario#",pagina,"S");
    pagina=riempiCampo(dbr,"cod_fisc","#cod_fiscale#",pagina,"S");
    pagina=riempiCampo(dbr,"data_nasc","#data_nasc#",pagina,"DT");
    //pagina=query_comuni(dbc,dbr,"cod_com_nasc","#comune_nascita#",pagina);
    pagina=query_comuniNascita(dbc,dbr,"cod_com_nasc","#comune_nascita#",pagina);
    
    String sessoF=NO;
    String sessoM=NO;
    if (dbr!=null && dbr.get("sesso")!=null )
    {
      if (((String)dbr.get("sesso")).equals("F"))
             sessoF=SI   ;
      else if(((String)dbr.get("sesso")).equals("M"))
             sessoM=SI   ;
    }
    pagina.put("#sessof#",sessoF);
    pagina.put("#sessom#",sessoM);
    pagina=query_comuni(dbc,dbr,"citta","#comune_res#",pagina);
    pagina=riempiCampo(dbr,"indirizzo","#residenza#",pagina,"S");
    pagina=riempiCampo(dbr,"prov","#prov_res#",pagina,"S");
    pagina=riempiCampo(dbr,"localita","#local_res#",pagina,"S");
    pagina=query_comuni(dbc,dbr,"dom_citta","#comune_dom#",pagina);
    pagina=riempiCampo(dbr,"dom_indiriz","#domicilio#",pagina,"S");
    pagina=riempiCampo(dbr,"dom_localita","#local_dom#",pagina,"S");
    pagina=riempiCampo(dbr,"dom_prov","#prov_dom#",pagina,"S");
    pagina=riempiCampo(dbr,"telefono1","#tel_dom#",pagina,"S");
    pagina=riempiCampo(dbr,"nome_camp","#nome_camp#",pagina,"S");
    pagina=query_cittadin(dbc,dbr,"cittadinanza","#cittadinanza#",pagina);
    eve.writeSostituisci("datianagrafici",pagina);
    queryEsenzioni(dbc,dbr,eve);
    eve.write("fineTabPaginaIniziale");
   } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.scriviPaginaRigaFamiliari(): "+e);
   }
}

private void scriviPaginaDatiContatto(ISASRecord dbr,mergeDocument eve,ISASConnection dbc)
throws SQLException {
try{
    Hashtable pagina = new Hashtable();
    pagina=riempiCampo(dbr,"n_cartella","#n_cartella#",pagina,"I");
    pagina=riempiCampo(dbr,"n_contatto","#n_contatto#",pagina,"I");
    String cognome="";
    if (dbr.get("cognome")!=null && ((String)dbr.get("cognome")).equals(""))
          cognome=((String)dbr.get("cognome")).trim();
    if (dbr.get("nome")!=null && ((String)dbr.get("nome")).equals(""))
          cognome=cognome+" " +((String)dbr.get("nome")).trim();
    pagina=riempiCampo(dbr,"cognome","#nome_assistito#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_data","#data_aper_fis#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_data","#data_apertura_contatto#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_fisiot_da","#data_fisio_ref#",pagina,"DT");
    String operatore=decodifica("operatori","codice",
          dbr.get("skf_operatore"),"nvl(cognome,'')||' ' || nvl(nome,'')",dbc);
    pagina.put("#operatore_fisio#",operatore);
    String operatore_ref=decodifica("operatori","codice",
          dbr.get("skf_fisiot"),"nvl(cognome,'') || ' ' || nvl(nome,'')",dbc);
    pagina.put("#fis_referente#",operatore_ref);
    pagina=riempiCampo(dbr,"skf_fisiot_da","#data_fis_ref#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_descr_contatto","#descr_conta_fisio#",pagina,"S");
    pagina.put("#tipo_riabil_fisio#",query_Tabvoci(dbc, dbr.get("skf_domic_cous"), "FTIPRI"));
    pagina.put("#provenienza#",query_Tabvoci(dbc, dbr.get("skf_provenienza"), "FPROVE"));
    pagina.put("#medico_prop#",query_Tabvoci(dbc, dbr.get("skf_medico"), "FMEDPR"));
    pagina=riempiCampo(dbr,"skf_medico_altro","#descr_medico_prop#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_note","#annotazioni#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_data_chiusura","#data_chiusura_fis#",pagina,"DT");
    pagina.put("#motivo_dim_fis#",query_Tabvoci(dbc, dbr.get("skf_motivo_chius"), "FCHIUS"));
    pagina=riempiCampo(dbr,"skf_barthel_in","#indice_bart_ing#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_barthel_out","#indice_bart_dim#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_euroqol_in","#indice_euro_ing#",pagina,"I");
    pagina=riempiCampo(dbr,"skf_euroqol_out","#indice_euro_dim#",pagina,"I");
    String obi_si=NO;
    String obi_no=NO;
    String obi_inparte=NO;
    if (dbr!=null && dbr.get("skf_raggobb")!=null )
    {
      if (((String)dbr.get("skf_raggobb")).equals("0"))
             obi_no=SI   ;
      else if(((String)dbr.get("skf_raggobb")).equals("1"))
             obi_si=SI   ;
      else if(((String)dbr.get("skf_raggobb")).equals("2"))
            obi_inparte=SI;
    }
    pagina.put("#ragg_obi_s#",obi_si);
    pagina.put("#ragg_ob_n#",obi_no);
    pagina.put("#ragg_ob_parte#",obi_inparte);
	// 05/20/13
	pagina.put("#presidio_cont#", decodPresidio(dbc, (String)dbr.get("skf_cod_presidio"), (String)dbr.get("skf_fisiot")));
	
    eve.writeSostituisci("paginaSchedaFis",pagina);

   } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.scriviPaginaRigaFamiliari(): "+e);
   }
}

private void scriviPaginaSituazioneFam(ISASRecord dbr,mergeDocument eve,ISASConnection dbc)
throws SQLException {
try{
    Hashtable pagina = new Hashtable();
    pagina=riempiCampo(dbr,"n_cartella","#n_cartella#",pagina,"I");
    pagina=riempiCampo(dbr,"n_contatto","#n_contatto#",pagina,"I");
    String cognome="";
    if (dbr.get("cognome")!=null && ((String)dbr.get("cognome")).equals(""))
          cognome=((String)dbr.get("cognome")).trim();
    if (dbr.get("nome")!=null && ((String)dbr.get("nome")).equals(""))
          cognome=cognome+" " +((String)dbr.get("nome")).trim();
    pagina=riempiCampo(dbr,"cognome","#nome_assistito#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_data","#data_apertura_contatto#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_nucfam_num","#num_persone#",pagina,"I");
    pagina=riempiCheckSiNo("nucleo_fam_pres",dbr,"skf_nucfam_pres",pagina);
    pagina=riempiCheckSiNo("nucleo_fam_coll",dbr,"skf_nucfam_coll",pagina);
    pagina=riempiCheckSiNo("nucleo_fam_pres",dbr,"skf_nucfam_pres",pagina);
    pagina=riempiCheckSiNo("pres_scale",dbr,"skf_sitabit_scale",pagina);
    pagina=riempiCheckSiNo("accesso_bagno",dbr,"skf_sitabit_bagno",pagina);
    pagina=riempiCheckSiNo("camera",dbr,"skf_sitabit_camera",pagina);
    pagina=riempiCheckSiNo("acce_altro",dbr,"skf_sitabit_altro",pagina);
    pagina=riempiCheckSiNo("diffic_sup",dbr,"skf_sitabit_ausili",pagina);
    eve.writeSostituisci("paginaSituazFamiliare",pagina);
   } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.scriviPaginaRigaFamiliari(): "+e);
   }
}
private void scriviPaginaCondPaziente(ISASRecord dbr,mergeDocument eve,ISASConnection dbc)
throws SQLException {
try{
    Hashtable pagina = new Hashtable();
    pagina=riempiCampo(dbr,"n_cartella","#n_cartella#",pagina,"I");
    pagina=riempiCampo(dbr,"n_contatto","#n_contatto#",pagina,"I");
    String cognome="";
    if (dbr.get("cognome")!=null && ((String)dbr.get("cognome")).equals(""))
          cognome=((String)dbr.get("cognome")).trim();
    if (dbr.get("nome")!=null && ((String)dbr.get("nome")).equals(""))
          cognome=cognome+" " +((String)dbr.get("nome")).trim();
    pagina=riempiCampo(dbr,"cognome","#nome_assistito#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_data","#data_apertura_contatto#",pagina,"DT");
    pagina.put("#cond_generali#",query_Tabvoci(dbc, dbr.get("skf_condiz"), "FCONDG"));
    pagina.put("#cond_cognitiva#",query_Tabvoci(dbc, dbr.get("skf_cognitive"), "FCONDC"));
    pagina.put("#deambula#",query_Tabvoci(dbc, dbr.get("skf_deamb"), "FDEAMB"));
    pagina.put("#autonomia#",query_Tabvoci(dbc, dbr.get("skf_automomia"), "FAUTON"));
    pagina=riempiCampo(dbr,"skf_ausili_prop","#aus_proposti#",pagina,"S");
    pagina=riempiCheckSiNo("ausili",dbr,"skf_ausili",pagina);
    pagina.put("#patol_princ#",query_Tabvoci(dbc, dbr.get("skf_decorso_pat"), "FDECPA"));
    pagina.put("#tipo#",query_Tabvoci(dbc, dbr.get("skf_disabilita"), "FDISAB"));
    pagina=riempiCampo(dbr,"skf_disabilita_altro","#descrizione_profilo#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_disab_strutture","#punt_strut_corporee#",pagina,"I");
    pagina=riempiCampo(dbr,"skf_disab_funzioni","#punt_funz_corporee#",pagina,"I");
    pagina=riempiCampo(dbr,"skf_disab_attpart","#punt_att_limita#",pagina,"I");
    pagina=riempiCampo(dbr,"skf_disab_fattamb","#punt_fatt_ambientali#",pagina,"I");
    eve.writeSostituisci("paginaCondPaziente",pagina);
   } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.scriviPaginaRigaFamiliari(): "+e);
   }
}

private void scriviPaginaProgettoRiabI(ISASRecord dbr,mergeDocument eve,ISASConnection dbc)
throws SQLException {
try{
    Hashtable pagina = new Hashtable();
    pagina=riempiCampo(dbr,"n_cartella","#n_cartella#",pagina,"I");
    pagina=riempiCampo(dbr,"n_contatto","#n_contatto#",pagina,"I");
    String cognome="";
    if (dbr.get("cognome")!=null && ((String)dbr.get("cognome")).equals(""))
          cognome=((String)dbr.get("cognome")).trim();
    if (dbr.get("nome")!=null && ((String)dbr.get("nome")).equals(""))
          cognome=cognome+" " +((String)dbr.get("nome")).trim();
    pagina=riempiCampo(dbr,"cognome","#nome_assistito#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_data","#data_apertura_contatto#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_data_evento","#data_evento_acuto#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_data_riacutiz","#data_riac#",pagina,"DT");
    pagina=riempiCampo(dbr,"skf_progetto","#proge_riabil#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_medico_resp","#medico_respon#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_obiettivi","#obiet_progetto#",pagina,"S");
    pagina.put("#tempo_trattamento#",query_Tabvoci(dbc, dbr.get("skf_tempi"), "FTEMPO"));
    eve.writeSostituisci("paginaProgetto1",pagina);
   } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.scriviPaginaRigaFamiliari(): "+e);
   }
}

private String mkSelect(Hashtable par)
{
      String select="";
      select="SELECT c.cognome,c.nome,c.sesso,c.data_nasc,c.cittadinanza,"+
                     "c.cod_fisc,c.cod_reg,c.cod_com_nasc,c.cod_usl,"+
                     " a.* ,sc.*"+
             " FROM cartella c , anagra_c a, skfis sc "+
             " WHERE c.n_cartella=a.n_cartella "+
                  "  AND  sc.n_cartella=c.n_cartella "+
                  "  AND sc.n_cartella="+(String)par.get("n_cart")+
                  "  AND sc.n_contatto="+(String)par.get("n_conta")+
                  "  AND a.data_variazione IN ( SELECT MAX (data_variazione) "+
                          " FROM anagra_c WHERE n_cartella ="+(String)par.get("n_cart")+")";
      System.out.println("SELECT STAMPA SKFISIO :" + select);
      return select;

}
private void scriviPaginaProgettoRiabII(ISASRecord dbr,mergeDocument eve,ISASConnection dbc)
throws SQLException {
try{
    Hashtable pagina = new Hashtable();
    pagina=riempiCampo(dbr,"n_cartella","#n_cartella#",pagina,"I");
    pagina=riempiCampo(dbr,"n_contatto","#n_contatto#",pagina,"I");
    String cognome="";
    if (dbr.get("cognome")!=null && ((String)dbr.get("cognome")).equals(""))
          cognome=((String)dbr.get("cognome")).trim();
    if (dbr.get("nome")!=null && ((String)dbr.get("nome")).equals(""))
          cognome=cognome+" " +((String)dbr.get("nome")).trim();
    pagina=riempiCampo(dbr,"cognome","#nome_assistito#",pagina,"S");
    pagina=riempiCampo(dbr,"skf_data","#data_apertura_contatto#",pagina,"DT");
    pagina=riempiCheckSiNo("assi_dom",dbr,"skf_op_assdom",pagina);
    pagina=riempiCheckSiNo("assi_soc",dbr,"skf_op_asssoc",pagina);
    pagina=riempiCheckSiNo("fisio",dbr,"skf_op_fis",pagina);
    pagina=riempiCheckSiNo("inf",dbr,"skf_op_inf",pagina);
    pagina=riempiCheckSiNo("logo",dbr,"skf_op_logop",pagina);
    pagina=riempiCheckSiNo("med_riab",dbr,"skf_op_med",pagina);
    pagina=riempiCheckSiNo("terap_occ",dbr,"skf_op_terap",pagina);
    pagina=riempiCheckSiNo("med_spec",dbr,"skf_op_medspec",pagina);
    pagina=riempiCheckSiNo("altro",dbr,"skf_op_altro",pagina);
    pagina=riempiCampo(dbr,"skf_op_altro_desc","#spec_altro#",pagina,"S");
    pagina.put("#care_gaver#",query_Tabvoci(dbc, dbr.get("skf_persona"), "FCAREG"));
    pagina=riempiCampo(dbr,"skf_persona_desc","#specif_caregaver#",pagina,"S");
    pagina=riempiCheckSiNo("nessuno",dbr,"skf_condobb_n",pagina);
    pagina=riempiCheckSiNo("familiari",dbr,"skf_condobb_f",pagina);
    pagina=riempiCheckSiNo("paziente",dbr,"skf_condobb_p",pagina);
    pagina=riempiCheckSiNo("operatore",dbr,"skf_condobb_o",pagina);
    pagina=riempiCheckSiNo("altro_care",dbr,"skf_condobb_a",pagina);
    eve.writeSostituisci("paginaProgetto2",pagina);
   } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.scriviPaginaRigaFamiliari(): "+e);
   }
}


private Hashtable riempiCheckSiNo(String Focampo,Object dbr,String dbcampo,Hashtable h)
throws SQLException
{//check del tipo s/n su frame e check(quadratini si o no) sul file fo
  try
  {
         String valore="";
         if (dbr!=null && ((ISASRecord)dbr).get(dbcampo)!=null)
                valore=(String)(((ISASRecord)dbr).get(dbcampo));
         if (valore.equals("S"))
         {
          h.put("#"+Focampo+"_s#",SI);
          h.put("#"+Focampo+"_n#",NO);
         }
         else if (valore.equals("N"))
         {
          h.put("#"+Focampo+"_s#",NO);
          h.put("#"+Focampo+"_n#",SI);
         }
         else
         {
          h.put("#"+Focampo+"_s#",NO);
          h.put("#"+Focampo+"_n#",NO);
         }
        return h;
    } catch(Exception e) {
		e.printStackTrace();
	throw new SQLException("SINSFoSkFisio.riempiCheckSiNo(): "+e);
   }
}

private String  query_Tabvoci(ISASConnection dbc, Object val_codice, String chiave)
throws  SQLException
{
 String decodifica="";
try
 {
        if (val_codice!=null )
        {
              String select = "SELECT  tab_descrizione FROM tab_voci WHERE tab_cod='"+chiave +"'"
                              + " AND tab_val='"+val_codice.toString() +"'";
              ISASRecord dbrdecod = dbc.readRecord(select);
            //  System.out.println("Skfisio->TabVoci--> "+ select);
              if(dbrdecod!=null && dbrdecod.get("tab_descrizione")!=null)
                      decodifica=(String)dbrdecod.get("tab_descrizione");
        }
        return decodifica;
}catch(Exception e){
    e.printStackTrace();
    throw new SQLException("DEBUG SINSFoSkFisio :Errore eseguendo una query_Tabvoci()  ");
}

}



private void queryEsenzioni(ISASConnection dbc,ISASRecord dbr, mergeDocument doc)
throws  SQLException
{
Hashtable ht = new Hashtable();
try
 {
	String select = "SELECT  es_data_inizio,es_data_fine,cod_esenzione FROM anagra_esenzioni WHERE cod_usl='"+(String)((ISASRecord)dbr).get("cod_usl") +"'";
        System.out.println("FoSkFisio--> queryEsenzioni--> "+select);
	ISASCursor dbcur = dbc.startCursor(select);
	if(dbcur!=null){
		while (dbcur.next()) {
			ISASRecord dbese = dbcur.getRecord();

                        ht=riempiCampo(dbese,"es_data_inizio","#data_inizio_ese#",ht,"DT");
                        ht=riempiCampo(dbese,"es_data_fine","#data_fine_ese#",ht,"DT");
                       String esenzione=decodifica("esenzioni","cod_esenzione",
                            dbese.get("cod_esenzione"),"descrizione",dbc);
                        ht.put("#esenzione_des#",esenzione);
		        doc.writeSostituisci("esenzioni", ht);
                }
           }else{//stampa riga vuota
                        ht.put("#data_inizio_ese#","");
                        ht.put("#data_fine_ese#","");
                        ht.put("#esenzione_des#","");
                        doc.writeSostituisci("esenzioni", ht);
	}

}catch(Exception e){
    e.printStackTrace();
    throw new SQLException("DEBUG SINSFoSkFisio :Errore eseguendo una query_esenzioni()  ");
}

}


private Hashtable query_comuniNascita(ISASConnection dbc,Object dbr,String dbcampo,String FoCampo,Hashtable h)
throws  SQLException
{
//decodifica dalla tabella comuni
    String decodifica="";
    try
    {
    	String codComune = ISASUtil.getValoreStringa((ISASRecord)dbr, dbcampo);
    	String dtNascita = ISASUtil.getValoreStringa((ISASRecord)dbr, "data_nasc");
    	decodifica = ComuniNascita.getDecodeComuneNascita(dbc, codComune, dtNascita);
        
//    	if (dbr!=null && ((ISASRecord)dbr).get(dbcampo)!=null && !(((String)((ISASRecord)dbr).get(dbcampo)).trim()).equals(""))
//        {
//            String select = "SELECT descrizione FROM comuni WHERE codice='"+(String)((ISASRecord)dbr).get(dbcampo) +"'";
//       //     System.out.println("DEBUG SINSFoSkFisio  query_comuni select" + select);
//            ISASRecord dbdecod=dbc.readRecord(select);
//            if (dbdecod!=null )
//            {
//                if (dbdecod.get("descrizione")!=null && !((String)dbdecod.get("descrizione")).equals(""))
//                       decodifica=(String)dbdecod.get("descrizione");
//            }
//        }
        h.put(FoCampo,decodifica);
        return h;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SINSFoSkFisio :Errore eseguendo una query_comuni()  ");
    }
}

private Hashtable query_comuni(ISASConnection dbc,Object dbr,String dbcampo,String FoCampo,Hashtable h)
throws  SQLException
{
//decodifica dalla tabella comuni
    String decodifica="";
    try
    {
        if (dbr!=null && ((ISASRecord)dbr).get(dbcampo)!=null && !(((String)((ISASRecord)dbr).get(dbcampo)).trim()).equals(""))
        {
            String select = "SELECT descrizione FROM comuni WHERE codice='"+(String)((ISASRecord)dbr).get(dbcampo) +"'";
       //     System.out.println("DEBUG SINSFoSkFisio  query_comuni select" + select);
            ISASRecord dbdecod=dbc.readRecord(select);
            if (dbdecod!=null )
            {
                if (dbdecod.get("descrizione")!=null && !((String)dbdecod.get("descrizione")).equals(""))
                       decodifica=(String)dbdecod.get("descrizione");
            }
        }
        h.put(FoCampo,decodifica);
        return h;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SINSFoSkFisio :Errore eseguendo una query_comuni()  ");
    }
}

private Hashtable query_cittadin(ISASConnection dbc,Object dbr,String dbcampo,String FoCampo,Hashtable h)
throws  SQLException
{
//decodifica dalla tabella cittadin
    String decodifica="";
    try
    {
        if (dbr!=null && ((ISASRecord)dbr).get(dbcampo)!=null && !(((String)((ISASRecord)dbr).get(dbcampo)).trim()).equals(""))
        {
            String select = "SELECT des_cittadin FROM cittadin WHERE cd_cittadin='"+(String)((ISASRecord)dbr).get(dbcampo) +"'";
            System.out.println("DEBUG SINSFoSkFisio  query_cittadin select" + select);
            ISASRecord dbdecod=dbc.readRecord(select);
            if (dbdecod!=null )
            {
                if (dbdecod.get("des_cittadin")!=null && !((String)dbdecod.get("des_cittadin")).equals(""))
                       decodifica=(String)dbdecod.get("des_cittadin");
            }
        }
        h.put(FoCampo,decodifica);
        return h;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SINSFoSkFisio :Errore eseguendo una query_cittadin()  ");
    }
}


private Hashtable riempiCampo(Object dbr,String  dbcampo,String nomeFo,Hashtable h,String tipo)
{
//tipo-->S faccio il casting con una stringa
//tipo-->I faccio il casting con una Integer
//tipo-->D faccio il casting con una Double e lo formatto con  decimali,9 parte intera e la virgola come separatore
//metto tutto in un try catch in modo tale che se ho un errore di casting
//la stampa non schianti .
String valore="";
try{
        if ( dbr!=null && ((ISASRecord)dbr).get(dbcampo)!=null)
        {
            if (tipo.equals("S"))
              valore=(String)(((ISASRecord)dbr).get(dbcampo));
            else if (tipo.equals("I"))
               valore=""+(Integer)(((ISASRecord)dbr).get(dbcampo));
            else if (tipo.equals("D"))
            {
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMaximumFractionDigits(2) ;
                nf.setMaximumIntegerDigits(9) ;
                valore=nf.format(((Double)((ISASRecord)dbr).get(dbcampo)).doubleValue());
            }
            else if (tipo.equals("DT"))
                valore=dataIta(((ISASRecord)dbr).get(dbcampo)) ;
        }
  }catch (Exception e)
  {
      e.printStackTrace();
      valore="";
  }
  h.put(nomeFo,valore);
  return h;
}
private String dataIta(Object campo)
{
    String dateita="";
    if ( campo!=null )
    {
            dateita=""+(java.sql.Date)campo;
            if (dateita.length()==10)
                dateita=dateita.substring(8,10)+"/"+
                       dateita.substring(5,7)+"/"+dateita.substring(0,4);
    }
    return dateita;
}
private String dataIta2(String dateita)
{
    if ( dateita!=null ){
            if (dateita.length()==10)
                dateita=dateita.substring(8,10)+"/"+
                       dateita.substring(5,7)+"/"+dateita.substring(0,4);
    }
    return dateita;
}
/**
* restituisce un parametro come stringa
*/
private String getStringField(ISASRecord dbr, String f) {
	try {
		return (dbr.get(f)).toString();
	} catch(Exception e) {
		debugMessage("getStringField("+f+"): "+e);
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

private void scriviPagine(ISASRecord dbr,char carattere, mergeDocument eve,ISASConnection dbc)
throws  SQLException {

    try{
            switch (carattere)
            {
                case 'A':
                    scriviPaginaIniziale(dbr,eve,dbc);
                    break;
                case 'B':
                      scriviPaginaDatiContatto(dbr,eve,dbc);
                      break;
                case 'C':
                    scriviPaginaSituazioneFam(dbr,eve,dbc);
                    break;
                case 'D':
                    scriviPaginaCondPaziente(dbr,eve,dbc);
                    break;
                case 'E':
                     scriviPaginaProgettoRiabI(dbr,eve,dbc);
                     break;
                case 'F':
                     scriviPaginaProgettoRiabII(dbr,eve,dbc);
                     break;
                case 'G': // 06/12/06
                     scriviPaginaDiagnosi(dbc,eve,dbr);
                     break;
            }
      }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SINSFoSkFisio :Errore eseguendo una scriviPagine()  ");
    }
}

private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try {
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
			nome_cod +" ='" + val_codice.toString() + "'";
		System.out.println("FoSkFisioEJB.decodifica(): DEBUG ["+mysel+"].");
                ISASRecord dbtxt = dbc.readRecord(mysel);
		return ((String)dbtxt.get("descrizione"));
	} catch (Exception ex) {
		return " ";
	}
}


	private void scriviPaginaDiagnosi(ISASConnection dbc, mergeDocument eve, ISASRecord dbski)throws SQLException 
	{
	   	Hashtable ht = new Hashtable();
	   	String decorso_pat="";
		try{
			decorso_pat = query_Tabvoci(dbc, getStringField(dbski, "skf_decorso_pat"), "FDECPA");

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
				ht.put("#data_apertura_contatto#", dataIta2(data_apertura));
		   		ht.put("#skf_decorso_pat#", decorso_pat);		

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

}	// End of FoSkFisio class

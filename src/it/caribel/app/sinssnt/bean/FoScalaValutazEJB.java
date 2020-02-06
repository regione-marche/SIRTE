package it.caribel.app.sinssnt.bean;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//23/11/09 - EJB di connessione alla procedura SINS Tab. scl_valutazione
//
//==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.merge.*;	// fo merge

import javax.servlet.*;
import javax.servlet.http.*;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import org.apache.fop.apps.Driver;
import org.apache.fop.apps.Version;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;


public class FoScalaValutazEJB extends SINSSNTConnectionEJB{
	private static String nomeEJB = "FoScalaValutazEJB ";

	public FoScalaValutazEJB() {}

	private void preparaLayout(mergeDocument md, ISASConnection dbc,Hashtable par){
	Hashtable htxt = new Hashtable();
	try {
		String mysel = "SELECT conf_txt FROM conf WHERE "+
			"conf_kproc='SINS' AND conf_key='ragione_sociale'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
		String cart = ""+par.get("cartella");
		it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
		htxt.put("#assistito#",ut.getDecode(dbc,"cartella","n_cartella",cart,
		      "nvl(cognome,'')|| ' ' ||nvl(nome,'')","nomeass"));
		if(par.get("tp").equals("1"))// caso stampa dati
		  htxt.put("#data#",""+par.get("data"));
		else //caso modello vuoto
		  htxt.put("#data#","____/____/________");
	} catch (Exception ex) {
		htxt.put("#txt#", "ragione_sociale");
		htxt.put("#assistito#", " ");
		htxt.put("#data#", "__/__/____");
	}
	ServerUtility su =new ServerUtility();
	htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
	md.writeSostituisci("layout",htxt);
}

	public byte[] query_report(String utente, String passwd, Hashtable h, mergeDocument eve)
	throws SQLException{
		boolean done = false;
		ISASConnection dbc = null;
		ISASCursor dbcur = null;

		String cartella = null;
		String data = null;
		String tipoStampa = null;  // 2 == vuoto, 1 == con dati

		try{
			cartella = h.get("cartella").toString();
			data = h.get("data").toString();
			tipoStampa = h.get("tp").toString();
		}
		catch(Exception e){
			System.out.println(nomeEJB + " query_report() - MANCA CHIAVE, IMPOSSIBILE STAMPARE " + e);
			throw new SQLException(nomeEJB + " query_report() - MANCA CHIAVE, IMPOSSIBILE STAMPARE ");
		}

		try		{
			myLogin lg = new myLogin();
			lg.put(utente,passwd);
			dbc = super.logIn(lg);

			preparaLayout(eve, dbc, h);
			if(tipoStampa.equals("2")) creaVuoto(eve);
			else{
				String sel = " SELECT * FROM scl_valutazione WHERE n_cartella = " + cartella +
                                             " AND data = " + formatDate(dbc, data);

				System.out.println(nomeEJB + " query_report - sel == " + sel);
				ISASRecord rec = dbc.readRecord(sel);



				if(rec != null) creaBody(dbc,rec,eve);
				else eve.write("nodati");
			}

			eve.write("finale");

			eve.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return eve.get();	// restituisco il bytearray
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE query_report(): Eccezione= " + e);
			throw new SQLException(nomeEJB + " ERRORE query_report(): " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{
					if (dbcur != null)	dbcur.close();
					eve.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1)
				{
					System.out.println(nomeEJB + " ERRORE query_report() " + e1);
				}
			}
		}
	}

	private void valoreProblemi(ISASRecord rec, Hashtable dati, String campo, String pres, String ass)
        throws Exception{
		if((""+rec.get(campo)).equals("1")){
                    dati.put(pres, "X");		dati.put(ass, "");
                }else{
                    dati.put(pres, "");		        dati.put(ass, "X");
                }
	}

	private void analizzaProblemi(ISASRecord rec, Hashtable dati) throws Exception	{
            try{
		valoreProblemi(rec, dati, "alim_assistita",         "#1_pres#", "#1_ass#");
		valoreProblemi(rec, dati, "alim_enterale",          "#2_pres#", "#2_ass#");
		valoreProblemi(rec, dati, "alim_parentale",         "#3_pres#", "#3_ass#");
		valoreProblemi(rec, dati, "drenaggio",              "#4_pres#", "#4_ass#");
		valoreProblemi(rec, dati, "ossigeno_terapia",       "#5_pres#", "#5_ass#");
		valoreProblemi(rec, dati, "ventiloterapia",         "#6_pres#", "#6_ass#");
		valoreProblemi(rec, dati, "tracheotomia",           "#7_pres#", "#7_ass#");
		valoreProblemi(rec, dati, "stomia",                 "#8_pres#", "#8_ass#");
		valoreProblemi(rec, dati, "elim_urinaria",          "#9_pres#", "#9_ass#");
		valoreProblemi(rec, dati, "educ_terap",             "#10_pres#", "#10_ass#");
		valoreProblemi(rec, dati, "sonno_veglia",           "#11_pres#", "#11_ass#");
		valoreProblemi(rec, dati, "ulcere12g",              "#12_pres#", "#12_ass#");
		valoreProblemi(rec, dati, "ulcere34g",              "#13_pres#", "#13_ass#");
		valoreProblemi(rec, dati, "prelievi_venosi",        "#14_pres#", "#14_ass#");
                valoreProblemi(rec, dati, "care_giver",             "#15_pres#", "#15_ass#");
		valoreProblemi(rec, dati, "ecg",                    "#16_pres#", "#16_ass#");
		valoreProblemi(rec, dati, "telemetria",             "#17_pres#", "#17_ass#");
                valoreProblemi(rec, dati, "ter_sottocut",           "#18_pres#", "#18_ass#");
		valoreProblemi(rec, dati, "catetere",               "#19_pres#", "#19_ass#");
		valoreProblemi(rec, dati, "trasfusioni",            "#20_pres#", "#20_ass#");
		valoreProblemi(rec, dati, "dolore",                 "#21_pres#", "#21_ass#");
		valoreProblemi(rec, dati, "terminale_onc",          "#22_pres#", "#22_ass#");
		valoreProblemi(rec, dati, "terminale_nononc",       "#29_pres#", "#29_ass#");
		valoreProblemi(rec, dati, "neurologico",            "#23_pres#", "#23_ass#");
		valoreProblemi(rec, dati, "ortopedico",             "#24_pres#", "#24_ass#");
                valoreProblemi(rec, dati, "mantenimento",           "#25_pres#", "#25_ass#");
		valoreProblemi(rec, dati, "supervisione",           "#26_pres#", "#26_ass#");
		valoreProblemi(rec, dati, "ass_iadl",               "#27_pres#", "#27_ass#");
		valoreProblemi(rec, dati, "ass_adl",                "#28_pres#", "#28_ass#");
            }catch(Exception e){
                System.out.println("Eccezione:"+e);
            }
	}

	private void creaVuoto(mergeDocument doc){
		Hashtable dati = new Hashtable();

		dati.put("#1_pres#", "");               dati.put("#1_ass#", "");
		dati.put("#2_pres#", "");               dati.put("#2_ass#", "");
		dati.put("#3_pres#", "");               dati.put("#3_ass#", "");
		dati.put("#4_pres#", "");               dati.put("#4_ass#", "");
		dati.put("#5_pres#", "");               dati.put("#5_ass#", "");
		dati.put("#6_pres#", "");               dati.put("#6_ass#", "");
		dati.put("#7_pres#", "");               dati.put("#7_ass#", "");
		dati.put("#8_pres#", "");               dati.put("#8_ass#", "");
		dati.put("#9_pres#", "");               dati.put("#9_ass#", "");
		dati.put("#10_pres#", "");              dati.put("#10_ass#", "");
		dati.put("#11_pres#", "");              dati.put("#11_ass#", "");
		dati.put("#12_pres#", "");              dati.put("#12_ass#", "");
                dati.put("#13_pres#", "");               dati.put("#13_ass#", "");
		dati.put("#14_pres#", "");               dati.put("#14_ass#", "");
		dati.put("#15_pres#", "");               dati.put("#15_ass#", "");
		dati.put("#16_pres#", "");               dati.put("#16_ass#", "");
		dati.put("#17_pres#", "");               dati.put("#17_ass#", "");
		dati.put("#18_pres#", "");               dati.put("#18_ass#", "");
                dati.put("#19_pres#", "");               dati.put("#19_ass#", "");
		dati.put("#20_pres#", "");               dati.put("#20_ass#", "");
		dati.put("#21_pres#", "");               dati.put("#21_ass#", "");
		dati.put("#22_pres#", "");               dati.put("#22_ass#", "");
		dati.put("#23_pres#", "");               dati.put("#23_ass#", "");
		dati.put("#24_pres#", "");               dati.put("#24_ass#", "");
                dati.put("#25_pres#", "");               dati.put("#25_ass#", "");
		dati.put("#26_pres#", "");               dati.put("#26_ass#", "");
		dati.put("#27_pres#", "");               dati.put("#27_ass#", "");
		dati.put("#28_pres#", "");               dati.put("#28_ass#", "");
		dati.put("#29_pres#", "");               dati.put("#29_ass#", "");
                dati.put("#cog_assenti#","");
                dati.put("#cog_moderati#","");
                dati.put("#cog_gravi#","");
                dati.put("#com_assenti#","");
                dati.put("#com_moderati#","");
                dati.put("#com_gravi#","");
                dati.put("#autonomia#","");
                dati.put("#supporto#","");
                dati.put("#mobilita#","");
                dati.put("#assistenza#","");
                dati.put("#rischio#","");
                dati.put("#prevalente#","");
                dati.put("#concomitante#","");

                dati.put("#tempo#","");
                dati.put("#nome_test#","");
                dati.put("#data_test#","");

		doc.writeSostituisci("continuo", dati);
	}

	private void creaBody(ISASConnection dbc, ISASRecord rec, mergeDocument doc){
             ISASUtil utl = new ISASUtil();
		Hashtable dati = new Hashtable();
		try{
                        String data = null;

		        if(rec.get("data") != null)
			    data = rec.get("data").toString();

                        if (data != null)
			    dati.put("#data_test#", ("" + data).substring(8, 10)
					+ "/" + ("" + data).substring(5, 7) + "/"
					+ ("" + data).substring(0, 4));

                        if ((rec.get("nome") != null)
                                        && (!((String) rec.get("nome")).trim().equals(""))) {
                                dati.put("#nome_test#", ((String) rec.get("nome")).trim());
                        } else
                                dati.put("#nome_test#", "__________");

                        if ((rec.get("tempo_t") != null)) {
                                String strTempoT = "" + rec.get("tempo_t");
                                dati.put("#tempo#", strTempoT);
                        } else
                                dati.put("#tempo#", "__________");
                        analizzaProblemi(rec, dati);
                        dati.put("#prevalente#", utl.getDecode(dbc, "tab_diagnosi","cod_diagnosi",
                                                rec.get("pat_prev"), "diagnosi"));
                        dati.put("#concomitante#", utl.getDecode(dbc, "tab_diagnosi","cod_diagnosi",
                                                rec.get("pat_conco"), "diagnosi"));
                        dati.put("#cog_assenti#","");
                        dati.put("#cog_moderati#","");
                        dati.put("#cog_gravi#","");
                        if(rec.get("cognitivi")!=null){
                            String cog = ""+rec.get("cognitivi");
                            if(cog.equals("1"))           dati.put("#cog_assenti#","X");
                            else if (cog.equals("2"))     dati.put("#cog_moderati#","X");
                            else if (cog.equals("3"))     dati.put("#cog_gravi#","X");
                        }
                        dati.put("#com_assenti#","");
                        dati.put("#com_moderati#","");
                        dati.put("#com_gravi#","");
                        if(rec.get("comportamento")!=null){
                            String cog = ""+rec.get("comportamento");
                            if(cog.equals("1"))           dati.put("#com_assenti#","X");
                            else if (cog.equals("2"))     dati.put("#com_moderati#","X");
                            else if (cog.equals("3"))     dati.put("#com_gravi#","X");
                        }
                        if(rec.get("autonomia")!=null){
                            String auto = ""+rec.get("autonomia");
                            if(auto.equals("1"))           dati.put("#autonomia#","Autonomo");
                            else if (auto.equals("2"))     dati.put("#autonomia#","Parzialmente dip.");
                            else if (auto.equals("3"))     dati.put("#autonomia#","Totalmente dip.");
                        }
                        if(rec.get("mobilita")!=null){
                            String auto = ""+rec.get("mobilita");
                            if(auto.equals("1"))           dati.put("#mobilita#","Si sposta da solo");
                            else if (auto.equals("2"))     dati.put("#mobilita#","Si sposta assistito");
                            else if (auto.equals("3"))     dati.put("#mobilita#","Non si sposta");
                        }
                        if(rec.get("supp_sociale")!=null){
                            String auto = ""+rec.get("supp_sociale");
                            if(auto.equals("1"))           dati.put("#supporto#","Presente");
                            else if (auto.equals("2"))     dati.put("#supporto#","Presente parzialmente");
                            else if (auto.equals("3"))     dati.put("#supporto#","Non presente");
                        }
                        if(rec.get("livello")!=null){
                            String auto = ""+rec.get("livello");
                            if(auto.equals("1"))           dati.put("#assistenza#","Complessita' bassa");
                            else if (auto.equals("2"))     dati.put("#assistenza#","Complessita' medio-alta");
                            else if (auto.equals("3"))     dati.put("#assistenza#","Complessita' elevata");
                        }
                        if(rec.get("rischio_infettivo")!=null){
                            String auto = ""+rec.get("rischio_infettivo");
                            if(auto.equals("1"))           dati.put("#rischio#","SI");
                            else if (auto.equals("0"))     dati.put("#rischio#","NO");
                            else                           dati.put("#rischio#","");
                        }
                        else
                            dati.put("#rischio#","");
                        doc.writeSostituisci("continuo", dati);
		}
		catch(Exception e)
		{	doc.write("errore");	}
	}
}

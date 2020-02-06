package it.caribel.app.sinssnt.bean;
// --------------------------------------------------------------------------
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 29/05/2006 - EJB di stampa della procedura SINS Test IADL
//
// ==========================================================================

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Hashtable;

public class FoTestScpSpmsqEJB extends SINSSNTConnectionEJB {
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();

	public FoTestScpSpmsqEJB() {
	}

	private void preparaBody(ISASConnection dbc, mergeDocument doc,
			Hashtable par, String tp) throws Exception {
		Hashtable ht = new Hashtable();
		String data = null;
		
		
		FaiHashVuota(ht);
		
		if (tp.equals("1")) {
			// si flaggano le risposte errate 
			for(int i=1; i< 10; i++){
				String nomeCampo = "d0" + i;
			if(par.get(nomeCampo)!=null && !par.get(nomeCampo).equals("")){
				if(par.get(nomeCampo).toString().equals("1")){
					ht.put("#" + nomeCampo + "#", "X");
				System.out.println("FoTestScpSpsmq preparaBody: "+ nomeCampo + " :"  + par.get(nomeCampo).toString());
				}
			}
			}
			if(par.get("d10")!=null && !par.get("d10").equals("")){
			if(par.get("d10").toString().equals("1"))
				ht.put("#d10#", "X");
			}
			if(par.get("d11")!=null && !par.get("d11").equals("")){
			if(par.get("d11").toString().equals("1"))
				ht.put("#d11#", "X");
			System.out.println("FoTestScpSpsmq d10: "+ par.get("d10").toString());
			}
			if (par.get("scp_spmsq_punt") != null)
				ht.put("#scp_spmsq_punt#", "" + par.get("scp_spmsq_punt"));
			else
				ht.put("#scp_spmsq_punt#", "____________" );
		} else
			ht.put("#scp_spmsq_punt#", "____________");
		
		if (data != null)
			ht.put("#data_test#", ("" + data).substring(8, 10)
					+ "/" + ("" + data).substring(5, 7) + "/"
					+ ("" + data).substring(0, 4));

		/*if ((par.get("nome") != null)
				&& (!((String) par.get("nome")).trim().equals(""))) {
			ht.put("#nome_test#", ((String) par.get("nome")).trim());
		} else
			ht.put("#nome_test#", "__________");*/

		/*if ((par.get("tempo_t") != null)) {
			String strTempoT = "" + par.get("tempo_t");
			ht.put("#tempo_t#", strTempoT);
		} else
			ht.put("#tempo_t#", "__________");*/

		doc.writeSostituisci("tabScpSpmsq", ht);
	}

	private void FaiHashVuota(Hashtable ht) {
		for (int i = 1; i < 10; i++) 
		ht.put("#d0" + i + "#", "");
		ht.put("#d10#", "");
		ht.put("#d11#", "");
				
		ht.put("#nome_test#", "__________");
		ht.put("#tempo_t#", "__________");
		ht.put("#data_test#", "____/____/________");
	}


	public byte[] query_report(String utente, String passwd, Hashtable par,
			mergeDocument doc) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;

		try {
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			// preparo i titoli di stampa
			preparaLayout(doc, dbc, par);
			//System.out.println("FoTestScpSpsmq par: " + par.toString());
			doc.write("istruzioni");

			Hashtable helabora = new Hashtable();
			helabora = LeggiDati(dbc, par);
			preparaBody(dbc, doc, helabora, (String) par.get("tp"));
						
			doc.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return (byte[]) doc.get();
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(
					"FoTestScpSpsmq Errore eseguendo una query_report()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}

	}

	private Hashtable LeggiDati(ISASConnection dbc, Hashtable par)
			throws Exception {
		Hashtable hret = new Hashtable();
		try {

			String cartella = "" + par.get("cartella");
			String data = "" + par.get("data");

			String mysel = "SELECT s.*" +
					" FROM scp_spmsq s"
					+ " WHERE s.n_cartella = " + cartella  
					+ " AND s.data = " + formatDate(dbc, data);
			
			System.out.println("FoTestScpSpsmq leggiDati: " + mysel);
			
			ISASRecord dbr = dbc.readRecord(mysel);
			if (dbr != null)
				hret = dbr.getHashtable();
			else
				hret = null;
		} catch (Exception e) {
			System.out.println("FoTestScpSpsmq Errore eseguendo una leggiDati: "
					+ e);
		}
		return hret;
	}

	private void preparaLayout(mergeDocument md, ISASConnection dbc,
			Hashtable par) {
		Hashtable htxt = new Hashtable();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE "
					+ "conf_kproc='SINS' AND conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String) dbtxt.get("conf_txt"));
			String cart = "" + par.get("cartella");
			
			it.pisa.caribel.util.ISASUtil ut = new it.pisa.caribel.util.ISASUtil();
			htxt.put("#assistito#", ut.getDecode(dbc, "cartella", "n_cartella",
					cart, "nvl(cognome,'')|| ' ' ||nvl(nome,'')", "nomeass"));
			
			if (par.get("tp").equals("1"))// caso stampa dati
				htxt.put("#data#", "" + par.get("data"));
			else
				// caso modello vuoto
				htxt.put("#data#", "____/____/________");
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
			htxt.put("#assistito#", " ");
			htxt.put("#data#", "__/__/____");
		}
		ServerUtility su = new ServerUtility();
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		md.writeSostituisci("layout", htxt);
	}

	public static String getjdbcDate() {
		java.util.Date d = new java.util.Date();
		java.text.SimpleDateFormat local_dateFormat = new java.text.SimpleDateFormat(
				"yyyy-MM-dd");
		return local_dateFormat.format(d);
	}

} // End of FoMAssEle class


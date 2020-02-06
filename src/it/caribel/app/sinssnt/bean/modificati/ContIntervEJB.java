package it.caribel.app.sinssnt.bean.modificati;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 08/02/2000 - EJB di connessione alla procedura SINS Tabella ContInterv
//
// paolo ciampolini
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.ISASUtil;

public class ContIntervEJB extends SINSSNTConnectionEJB {

	private String MIONOME = "3-ContIntervEJB.";

	// 11/06/07 m.:	Nel metodo "query()" e "queryPaginate()": modificato "SELECT campo1, campo2,.." 
	//	in "SELECT *" perche' vengano eseguiti i ctrl ISAS.

	public ContIntervEJB() {
	}

	public Vector query(myLogin mylogin, Hashtable h) throws SQLException {
		String punto = MIONOME + "query ";

		stampa(punto + "Inizio con dati>" + h + "<\n");
		boolean done = false;
		ISASConnection dbc = null;
		// 09/02/11
		ISASCursor dbcur = null;
		ISASCursor dbcurConf = null;

		try {
			dbc = super.logIn(mylogin);

			// 09/02/11 -------------------------------------------------------------------------
			String tipoOper = (String) h.get("tipo");
			String critTpPrest = "";

			//			{cartella=76, dal=2011-01-01, tipo=02, prov=ACCES, anno=2011, int_cod_oper=ADMIN, al=2011-10-01, codice=INF-3233}
			String dataInizioPrestazione = ISASUtil.getValoreStringa(h, "dal");
			String dataFinePrestazione = ISASUtil.getValoreStringa(h, "al");
			String codiceOperatore = ISASUtil.getValoreStringa(h, "codice");

			String aggiunta = ")";
			if ((tipoOper != null) && (tipoOper.trim().equals("01")))
				aggiunta = " OR conf_key = 'TIPDEF" + tipoOper + "B')";

			String selConf = "SELECT * FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND (conf_key ='TIPDEF" + tipoOper + "'" + aggiunta;
			dbcurConf = dbc.startCursor(selConf);

			while (dbcurConf.next()) {
				ISASRecord dbconf = dbcurConf.getRecord();
				if ((dbconf != null) && (dbconf.get("conf_txt") != null)) {
					String tpPrest = ((String) dbconf.get("conf_txt")).trim();
					if (critTpPrest.trim().equals(""))
						critTpPrest = " AND (a.int_tipo_prest = '" + tpPrest + "'";
					else
						critTpPrest += " OR a.int_tipo_prest = '" + tpPrest + "'";
				}
			}
			critTpPrest += ")";
			dbcurConf.close();
			// 09/02/11 -------------------------------------------------------------------------						

			//		StringBuffer myselect= new StringBuffer("select a.int_anno,a.int_cartella,a.int_contatore,b.cognome,b.nome,a.int_data_prest ");

			/** 11/06/07
					StringBuffer myselect= new StringBuffer("select a.int_anno,"+
					"a.int_cartella,a.int_contatore,b.cognome,b.nome,"+
					"a.int_data_prest,o.cognome opcogn ");
			**/
			// 11/06/07
			StringBuffer myselect = new StringBuffer("select a.*," + " b.cognome, b.nome, o.cognome opcogn");

			myselect.append(" from interv a, cartella b,operatori o ");
			myselect.append(" where a.int_cartella = b.n_cartella ");
			myselect.append(" and o.codice=a.int_cod_oper");
			if (ISASUtil.valida(codiceOperatore)) {
				myselect.append("  AND INT_COD_OPER = '");
				myselect.append(codiceOperatore);
				myselect.append("'  ");
			}

			if (ISASUtil.valida(dataInizioPrestazione)) {
				myselect.append("  AND int_data_prest >= ");
				myselect.append(dbc.formatDbDate(dataInizioPrestazione));
				myselect.append(" ");
			}

			if (ISASUtil.valida(dataFinePrestazione)) {
				myselect.append("  AND int_data_prest <= ");
				myselect.append(dbc.formatDbDate(dataFinePrestazione));
				myselect.append(" ");
			}

			//			String provenienza = (String) h.get("prov");
			String provenienza = ISASUtil.getValoreStringa(h, ("prov"));

			if (h.containsKey("anno")) {
				//Per release 0602myselect.append(" and a.int_anno ='"+(String)h.get("anno")+"' ");
				myselect.append(" and " + dbc.formatDbYear("int_data_prest") + " = '" + (String) h.get("anno") + "' ");
			}

			if (h.containsKey("cartella"))
				myselect.append(" and a.int_cartella =" + h.get("cartella"));

			if(h.containsKey("int_contatore"))
				myselect.append(" and a.int_contatore=" + h.get("int_contatore"));
			
			
			/*    if(h.containsKey("tipo")) {
			          if (!((String)h.get("tipo")).equals("05"))
			myselect.append(" and a.int_tipo_oper ='"+(String)h.get("tipo")+"' ");
			}*/
			if (provenienza.equals("OCCAS")) {
				myselect.append(" and a.int_contatto =0 ");
			} else
				myselect.append(" and a.int_contatto <>0 ");

			//2015-03-31 serratore: rimuovo questo vincolo credo sia inutile e
			//limita l'operativita degli acessi da segreteria organizzativa
//			//gb 17/05/07: si mette questa clausola perch� se l'operatore era
//			//		il Sociale (cio� int_tipo_oper='01') allora veniva
//			//		chiamato il la form JFrameASContatoreInterv che, a
//			//		sua volta si appoggia sull'EJB ContIntervAS.
//			myselect.append(" and a.int_tipo_oper <> '01' "); //gb 17/05/07

			// 09/02/11
			if (!critTpPrest.trim().equals(")"))
				myselect.append(critTpPrest);

			myselect.append(" order by a.int_anno, b.cognome, b.nome, a.int_contatore");
			stampa(punto + "Query>" + myselect + "<\n");
			dbcur = dbc.startCursor(myselect.toString());
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query() in ContInterv ",e );
		} finally {
			if (!done) {
				try {
					if (dbcurConf != null)
						dbcurConf.close();
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);

	}

	public Vector queryPaginate(myLogin mylogin, Hashtable h) throws SQLException {
		boolean done = false;
		ISASConnection dbc = null;
		// 09/02/11
		ISASCursor dbcur = null;
		ISASCursor dbcurConf = null;

		try {
			dbc = super.logIn(mylogin);

			// 09/02/11 -------------------------------------------------------------------------
			String tipoOper = (String) h.get("tipo");
			String critTpPrest = "";

			String aggiunta = ")";
			if ((tipoOper != null) && (tipoOper.trim().equals("01")))
				aggiunta = " OR conf_key = 'TIPDEF" + tipoOper + "B')";

			String selConf = "SELECT * FROM conf" + " WHERE conf_kproc = 'SINS'" + " AND (conf_key ='TIPDEF" + tipoOper + "'" + aggiunta;
			dbcurConf = dbc.startCursor(selConf);

			while (dbcurConf.next()) {
				ISASRecord dbconf = dbcurConf.getRecord();
				if ((dbconf != null) && (dbconf.get("conf_txt") != null)) {
					String tpPrest = ((String) dbconf.get("conf_txt")).trim();
					if (critTpPrest.trim().equals(""))
						critTpPrest = " AND (a.int_tipo_prest = '" + tpPrest + "'";
					else
						critTpPrest += " OR a.int_tipo_prest = '" + tpPrest + "'";
				}
			}
			critTpPrest += ")";
			dbcurConf.close();
			// 09/02/11 -------------------------------------------------------------------------						

			/** 11/06/07
					StringBuffer myselect= new StringBuffer("select a.int_anno,"+
					"a.int_cartella,a.int_contatore,b.cognome,b.nome,"+
					"a.int_data_prest,o.cognome opcogn ");
			**/
			// 11/06/07
			StringBuffer myselect = new StringBuffer("select a.*," + " b.cognome, b.nome, o.cognome opcogn");

			myselect.append(" from interv a, cartella b,operatori o ");
			myselect.append(" where a.int_cartella = b.n_cartella ");
			myselect.append(" and o.codice=a.int_cod_oper");
			String provenienza = (String) h.get("prov");
			System.out.println("PROVENIENZA " + provenienza);
			if (h.containsKey("anno")) {
				//Per release 0603myselect.append(" and a.int_anno ='"+(String)h.get("anno")+"' ");
				myselect.append(" and " + dbc.formatDbYear("int_data_prest") + " = '" + (String) h.get("anno") + "' ");
			}

			if (h.containsKey("cartella"))
				myselect.append(" and a.int_cartella =" + h.get("cartella"));

			if(h.containsKey("int_contatore"))
				myselect.append(" and a.int_contatore=" + h.get("int_contatore"));
			
			/*    if(h.containsKey("tipo")) {
			          if (!((String)h.get("tipo")).equals("05"))
			myselect.append(" and a.int_tipo_oper ='"+(String)h.get("tipo")+"' ");
			}*/
			if (provenienza.equals("OCCAS")) {
				myselect.append(" and a.int_contatto =0 ");
			} else
				myselect.append(" and a.int_contatto <>0 ");

			//gb 17/05/07: si mette questa clausola perch� se l'operatore era
			//		il Sociale (cio� int_tipo_oper='01') allora veniva
			//		chiamato il la form JFrameASContatoreInterv che, a
			//		sua volta si appoggia sull'EJB ContIntervAS.
			myselect.append(" and a.int_tipo_oper <> '01' "); //gb 17/05/07

			// 09/02/11
			if (!critTpPrest.trim().equals(")"))
				myselect.append(critTpPrest);

			myselect.append(" order by a.int_data_prest,a.int_anno,b.cognome, b.nome, a.int_contatore");
			System.out.println("prima di startcursor select: " + myselect.toString());
			dbcur = dbc.startCursor(myselect.toString());
			//		Vector vdbr=dbcur.getAllRecord();
			int start = Integer.parseInt((String) h.get("start"));
			int stop = Integer.parseInt((String) h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query() in ContInterv ", e);
		} finally {
			if (!done) {
				try {
					if (dbcurConf != null)
						dbcurConf.close();
					if (dbcur != null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					System.out.println(e1);
				}
			}
		}
	}

}

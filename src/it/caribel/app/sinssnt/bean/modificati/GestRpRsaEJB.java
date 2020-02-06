package it.caribel.app.sinssnt.bean.modificati;
//============================================================================
// CARIBEL S.r.l.
//----------------------------------------------------------------------------
//
// ---- EJB di gestione - SINS_RPRSAI ----
//gestione ricoveri rsa e md per piemonte
//bargi 28/02/2012 aggiunto collegamento alle preferenze espresse in istruttoria nel caso di inserimento
//============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.sinssnt.rsa_routine.RicoveriRsa;

public class GestRpRsaEJB extends SINSSNTConnectionEJB  {

	public GestRpRsaEJB() {}
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	private String msg = "Mancano i diritti per leggere il record";
	private String nomeEJB="GestRpRsaEJB";
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	ServerUtility su =new ServerUtility();
	dateutility dt = new dateutility();
	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws
	SQLException, CariException{
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			System.out.println(nomeEJB+".QueryKey: "+h.toString());
			
			ISASRecord dbr=leggiRsa(dbc,h);

			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(ISASPermissionDeniedException e){
			System.out.println(nomeEJB+".queryKey(): "+e);
			throw new CariException(msg, -2);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}
	private ISASRecord recMaxRsa(ISASConnection dbc,Hashtable h)throws Exception{
		try{
			String myselect="Select p.n_cartella from rp_rsa_ricoveri p  where "+
			"p.n_cartella="+h.get("n_cartella");
			 myselect+=" and p.data_variazione= (select max(data_variazione) from rp_rsa_ricoveri p1 where p1.n_cartella=p.n_cartella)";
				
			ISASRecord dbr=dbc.readRecord(myselect);
			return dbr;
		}catch(Exception e){
			System.out.println("Errore eseguendo una recMaxRsa: "+e);
			throw new SQLException("Errore eseguendo una recMaxRsa: ");
			//  return null;
		}	
	}
	private ISASRecord leggiRsa(ISASConnection dbc,Hashtable h)throws Exception{
		try{
			String myselect="Select p.* from rp_rsa_ricoveri p  where "+
			"p.n_cartella="+h.get("n_cartella");
			
			
			if(h.get("data_variazione")!=null ) myselect+=" and p.data_variazione="+dbc.formatDbDate(h.get("data_variazione").toString());
			else {
			//	if(h.containsKey("tutti")&&h.get("tutti").toString().equals("S")) 
					 myselect+=" and p.data_variazione= (select max(data_variazione) from rp_rsa_ricoveri p1 where p1.n_cartella=p.n_cartella)";
				
			}
		/*	 {fl_integra=S, istituti=, cognome=CERRI, cod_org=A, n_cartella=7796, note=, tipo_ricovero=0, urgente=N, motivo_uscita=, fl_temporaneo=N, tipo_istituto=1, fl_entrato=N, fl_integr_fascia=N, flag_stato=AT, cod_autosu=S}
			 GestRpRsaEJB.leggiRsa: 
				 Select p.* from rp_rsa_ricoveri p  where p.n_cartella=7796 and p.data_variazione= (select max(data_variazione) from rp_rsa_ricoveri p1 where p1.n_cartella=p.n_cartella)
*/
			String s="";
			if(h.get("n_progetto")!=null) 
				su.addWhere(s, su.REL_AND, "p.n_progetto", su.OP_EQ_NUM, h.get("n_progetto").toString());
			
				if(h.get("cod_obbiettivo")!=null) 
				su.addWhere(s, su.REL_AND, "p.n_progetto", su.OP_EQ_STR, h.get("cod_obbiettivo").toString());
			if(h.get("n_intervento")!=null) 
				su.addWhere(s, su.REL_AND, "p.n_intervento", su.OP_EQ_NUM, h.get("n_intervento").toString());
				
			System.out.println(nomeEJB+".leggiRsa: "+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			if (dbr!=null){//decodifiche varie
					String strCodOper ="";
					strCodOper = (String)util.getObjectField(dbr,"cod_operatore",'S');
					if((strCodOper!=null) && !strCodOper.equals(""))
					{
						String strCognOper = util.getDecode(dbc,"operatori","codice", strCodOper, "cognome");
						String strNomeOper = util.getDecode(dbc,"operatori","codice", strCodOper, "nome");
						dbr.put("desc_operatore", strCognOper + " " + strNomeOper);
					}		
					strCodOper = (String)util.getObjectField(dbr,"cod_oper_ingresso",'S');
					if((strCodOper!=null) && !strCodOper.equals(""))
					{
						String strCognOper = util.getDecode(dbc,"operatori","codice", strCodOper, "cognome");
						String strNomeOper = util.getDecode(dbc,"operatori","codice", strCodOper, "nome");
						dbr.put("desc_oper_ingresso", strCognOper + " " + strNomeOper);
					}		
					String strCodIst = (String)util.getObjectField(dbr,"cod_istituto_ingresso",'S');
					if((strCodIst!=null) && !strCodIst.equals(""))
					{
						String strDescIst = util.getDecode(dbc,"istituti","ist_codice", strCodIst, "st_nome");
						dbr.put("desc_istitu_ingresso", strDescIst);
					}		
					strCodIst = (String)util.getObjectField(dbr,"sede_privato",'S');
					if((strCodIst!=null) && !strCodIst.equals(""))
					{
						String strDescIst = util.getDecode(dbc,"istituti","ist_codice", strCodIst, "st_nome");
						dbr.put("desc_sede_privato", strDescIst);
					}		
					String cart = (String)util.getObjectField(dbr,"n_cartella",'I');
					//debugMessage("cartella="+cart);
					if((cart!=null) && !cart.equals(""))
					{
						String strCogn = util.getDecode(dbc,"cartella","n_cartella", cart, "cognome");
						String strNome = util.getDecode(dbc,"cartella","n_cartella", cart, "nome");
						dbr.put("cognome", strCogn.trim() + " " + strNome.trim());
					}
					if(dbr.get("cod_tipo_assistito")!=null) {
						String cod_tipoass = dbr.get("cod_tipo_assistito").toString();
						String desc_tipo_assistito = ISASUtil.getDecode(dbc, "rsa_tipo_assistito", "codice", cod_tipoass,
								"descrizione");
						dbr.put("desc_tipo_assistito", desc_tipo_assistito);
						String cod_tipute = ISASUtil.getDecode(dbc, "rsa_tipo_assistito", "codice", cod_tipoass, "cod_tipass");
						dbr.put("cod_tipute", cod_tipute);
					}
				
					Vector vAllIstituti = query_elencoIstituti(dbc, h);
					dbr.put("istituti",vAllIstituti);
					dbr.put("op","old");
				debugMessage(nomeEJB+" FINE lettura leggiRsa "+dbr.getHashtable().toString());
			}else{
				dbr=dbc.newRecord("rp_rsa_ricoveri");
				Vector vAllIstituti = query_elencoIstituti(dbc, h);
				//bargi 28/02/2012
				Vector vIstitutiIstruttoria = query_istruttoriaIstituti(dbc, h);
				mergeVettori(vAllIstituti,vIstitutiIstruttoria);
			   Enumeration en=h.keys();
				while (en.hasMoreElements()) {
					String key=en.nextElement().toString();
					dbr.put(key,h.get(key));
				}
				dbr.put("istituti",vAllIstituti);
				dbr.put("op","nuovo");
			}//end if dbr!=null
			return dbr;
		}catch(Exception e){
			System.out.println("Errore eseguendo una leggiRsa: "+e);
			throw new SQLException("Errore eseguendo una leggiRsa: ");
			//  return null;
		}
	}

	public Vector query_elencoIstituti(myLogin mylogin,Hashtable h) throws  SQLException,CariException{	
		ISASConnection dbc=null;
		try{
				dbc=super.logIn(mylogin);
				Vector vAllIstituti = query_elencoIstituti(dbc, h);
				Vector vIstitutiIstruttoria = query_istruttoriaIstituti(dbc, h);
				mergeVettori(vAllIstituti,vIstitutiIstruttoria);
			return vAllIstituti;			
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}			
		}
	}
		
	/*	
		
		catch(ISASPermissionDeniedException e){
			throw (CariException)  new CariException(msgDir, -2).initCause(e);
		}catch(Exception e){
			throw newEjbException("Errore in query_elencoIstituti() ", e);
		}finally{
			logout_nothrow("query_elencoIstituti",dbc);
		}
	}*/
	private Vector query_elencoIstituti(ISASConnection dbc, Hashtable h) throws SQLException {
		Vector v = null;
		String myselect = "";
		ISASCursor dbcur = null;
		String outJoin_1 = "";
		String outJoin_2 = "";
		boolean outerj=true;
		//LOG.debug("query_elencoIstituti (privata): hashtable= " + h.toString());
		if(outerj) {
			outJoin_1 = dbc.getoutTab();
			outJoin_2 = dbc.getoutCrit();
		}
		try {
			myselect =
				" SELECT nvl2(p.n_cartella,'true','false')checked,decode(p.flag_rinuncia,'S','true','N','false')flag_rinuncia, " +
				"p.*,i.ist_codice || ' '|| i.st_nome st_nome, i.st_comu, " +
				"c.descrizione comune, i.st_tipoges,"+
				" tip.tipoist, tip.org_mod, "+
				" i.ist_codice" +
				" FROM istituti i," +
				" rsa_tipologia_istituto tip," +
				""+outJoin_1+" comuni c,"+outJoin_1+" rp_rsa_preferenze p"+
				" WHERE " +
				"i.ist_codice = tip.codice_ist AND "+
				"  c.codice "+outJoin_2+"= i.st_comu "+
				" AND trim(p.COD_ISTITUTO "+outJoin_2+")= trim(i.IST_CODICE)";
			//if(h.get("data_variazione")!=null)
			//	myselect+=" and p.data_variazione "+outJoin_2+"="+formatDate(dbc, h.get("data_variazione").toString());                    
			myselect+=" and p.n_cartella "+outJoin_2+"="+h.get("n_cartella");

			if (!isNullOrBlank((h.get("tipo_istituto"))))
				myselect += " AND tip.tipoist = '" + h.get("tipo_istituto").toString().trim() + "'";
			if(isNullOrBlank(h.get("filtro"))||h.get("filtro").toString().equals("N") ) {
				if (!isNullOrBlank(h.get("cod_org")))
					myselect += " AND tip.org_mod = '" + h.get("cod_org").toString().trim() + "'";
			}
			myselect += " ORDER BY  checked desc,i.st_nome";

			System.out.println("query_elencoIstituti(privata):  select = " + myselect);
			dbcur = dbc.startCursor(myselect);
			if (dbcur != null) {
				v = dbcur.getAllRecord();
				if (v != null && v.size() > 0) {
					for (int i = 0; i < v.size(); i++) {
						ISASRecord r = (ISASRecord) v.get(i);
						if (r != null) {
							getPostiDisp(dbc, r, h);
							r.put("modulo", util.getDecode(dbc, "rsa_organizzazione", "org_codice", util.getObjectField(r,
									"org_mod",
							'S'),"org_descri"));
							r.put("des_tipo_istituto", util.getDecode(dbc, "rsa_tipo_istituto", "codice", util.getObjectField(r,
									"tipoist",
							'S'),"descrizione"));
						}
					}
				}
			}
			return v;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE query_elencoIstituti() - " + e);
			throw new SQLException(nomeEJB + " Errore eseguendo una query_elencoIstituti()  ");
		}finally {
				try {
					if(dbcur!=null)
					dbcur.close();
				} catch (Exception e) {
					System.out.println("RicoveriRsa: Errore eseguendo una query_elencoIstituti()  " + e);
				}
		}
	}
		
		/*catch (Exception e) {
			throw newEjbException("errore in query_elencoIstituti(privata): " + e.getMessage(), e);
		} finally {
			close_dbcur_nothrow("query_elencoIstituti(privata)", dbcur);
		}
	}*/

	private Vector query_elencoIstituti_old(ISASConnection dbc, Hashtable h) throws SQLException {
		Vector v = null;
		String myselect = "";
		ISASCursor dbcur = null;
		boolean done=false;		
		 String outJoin_1 = "";
		 String outJoin_2 = "";
		boolean outerj=true;
		 if(outerj) {
			outJoin_1 = dbc.getoutTab();
			outJoin_2 = dbc.getoutCrit();
		 }
		try {
			
				myselect =
					" SELECT nvl2(p.n_cartella,'true','false')checked,decode(p.flag_rinuncia,'S','true','N','false')flag_rinuncia, " +
					"p.*,i.st_nome, i.st_comu, " +
					"c.descrizione comune, i.st_tipoges,"+    
					 //" tip.tipoist, tip.org_mod, "+
                   " i.ist_codice" +                  
                   " FROM istituti i," +
                   //" rsa_tipologia_istituto tip," +
                   ""+outJoin_1+" comuni c,"+outJoin_1+" rp_rsa_preferenze p"+
                   " WHERE " +
                   //"i.ist_codice = tip.codice_ist AND "+
                   "  c.codice "+outJoin_2+"= i.st_comu "+
                   " AND trim(p.COD_ISTITUTO "+outJoin_2+") = trim(i.IST_CODICE)"+ 
                   " and p.n_cartella "+outJoin_2+"="+h.get("n_cartella");
			
				//if (!isNullOrBlank((h.get("tipo_istituto"))))
				//	myselect += " AND tip.tipoist = '" + h.get("tipo_istituto").toString().trim() + "'";
			//	if (!isNullOrBlank(h.get("cod_org")))
				//	myselect += " AND tip.org_mod = '" + h.get("cod_org").toString().trim() + "'";		

				myselect += " ORDER BY  checked desc,i.st_nome";			

			debugMessage("query elencoIstituti: " + myselect);
			dbcur = dbc.startCursor(myselect);
		
			//	debugMessage("da ricoveri:  soc" + tarricsoc + " san=" + tarricsan + " istituto=" + istric);
			if (dbcur != null) {				
				v = dbcur.getAllRecord();				
			}

			if (dbcur != null)
				dbcur.close();
			done=true;
			return v;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE query_elencoIstituti() - " + e);
			throw new SQLException(nomeEJB + " Errore eseguendo una query_elencoIstituti()  ");
		}finally {
			if (!done) {
				try {
					if(dbcur!=null)
					dbcur.close();
				} catch (Exception e) {
					System.out.println("RicoveriRsa: Errore eseguendo una query_elencoIstituti()  " + e);
				}
			}
		}
	}
	private Vector query_istruttoriaIstituti(ISASConnection dbc, Hashtable h) throws SQLException {
		Vector v = null;
		String myselect = "";
		ISASCursor dbcur = null;
		boolean done=false;		
		 String outJoin_1 = "";
		 String outJoin_2 = "";
		boolean outerj=true;
		 if(outerj) {
			outJoin_1 = dbc.getoutTab();
			outJoin_2 = dbc.getoutCrit();
		 }
		try {			
				myselect =
					" SELECT nvl2(p.n_cartella,'true','false')checked," +
					"p.peso,i.st_nome, i.st_comu, " +
					"c.descrizione comune, i.st_tipoges,"+   
	               " i.ist_codice" +                  
	               " FROM istituti i," +
	               ""+outJoin_1+" comuni c, rp_istruttoria_pref p"+
	               " WHERE " +
	               "  c.codice "+outJoin_2+"= i.st_comu "+
	               " AND trim(p.ist_codice ) = trim(i.IST_CODICE)"+ 
	               " and p.n_cartella ="+h.get("n_cartella")+
	               " and p.progr = (select max(n_progetto) from ass_progetto where n_cartella ="+h.get("n_cartella")+")"  ;
				if(h.get("data_variazione")!=null)
					myselect+=" and p.dt_inserimento "+outJoin_2+"<="+formatDate(dbc, h.get("data_variazione").toString());    
				
				myselect+=" and p.dt_inserimento =(select max(dt_inserimento) from rp_istruttoria_pref p2 where " +
						" p2.n_cartella ="+h.get("n_cartella")+
						" and p2.progr=p.progr "+
						")"  ;
				myselect += " ORDER BY  checked desc,i.st_nome";			

			debugMessage("query query_istruttoriaIstituti: " + myselect);
			dbcur = dbc.startCursor(myselect);
		
			//	debugMessage("da ricoveri:  soc" + tarricsoc + " san=" + tarricsan + " istituto=" + istric);
			if (dbcur != null) {				
				v = dbcur.getAllRecord();				
			}

			if (dbcur != null)
				dbcur.close();
			done=true;
			return v;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE query_istruttoriaIstituti() - " + e);
			throw new SQLException(nomeEJB + " Errore eseguendo una query_istruttoriaIstituti()  ");
		}finally {
			if (!done) {
				try {
					if(dbcur!=null)
					dbcur.close();
				} catch (Exception e) {
					System.out.println("RicoveriRsa: Errore eseguendo una query_istruttoriaIstituti()  " + e);
				}
			}
		}
	}

	private void mergeVettori(Vector vfin,Vector vpos)  throws Exception{
		try {
		   for (int i =0;i<vfin.size();i++ )
	       {
	          ISASRecord dbr = (ISASRecord)vfin.elementAt(i);
	          Enumeration en=vpos.elements();
	          boolean trovato=false;
	          while (en.hasMoreElements() && ! trovato) {
	        	  ISASRecord dbpos=(ISASRecord)en.nextElement();
	        	  if(compareIsas(dbr,dbpos)) {
	        		  trovato=true;
	             	 dbr.put("checked","true");//dbpos.get("posizione"));
	        	  }
	          }
	       }
		}catch(Exception e){
				e.printStackTrace();
				throw new Exception("Errore eseguendo mergeVettori()  ");
			}
	}

	private boolean compareIsas(ISASRecord dbfin,ISASRecord dbpos) {
		boolean b=false;
		b=compareKeyHashtable(dbfin.getHashtable(),dbpos.getHashtable());
		return b;
	}
	private boolean compareKeyHashtable(Hashtable ht_old,Hashtable ht_new)
	{
		//debugMessage("compareKeyHashtable");
		boolean b=false;
	  b= !compare(ht_old.get("ist_codice"),ht_new.get("ist_codice"));// && !compare(ht_old.get("data_variazione"),ht_new.get("data_variazione"));
	  return b;
	}
	//presente in RicoveriRSa
	private void getPostiDisp(ISASConnection dbc, ISASRecord dbr, Hashtable h) throws SQLException {
		String sesso = "M";
		try {
			if (h.containsKey("n_cartella"))
				sesso = getSesso(dbc, h);
			String myselect = "select ";
			if (sesso.equals("F"))
				myselect += " sum(posti_f ) posti, sum(posti_disp_f ) posti_disp";
			else
				myselect += " sum(posti_m) posti, sum(posti_disp_m ) posti_disp";

			//G.Brogi			myselect += " ,sum(posti_m) posti_tot, sum(posti_disp_m ) posti_disp_tot";
			myselect += " ,sum(posti_m) posti_tot, sum(posti_disp_m ) posti_disp_tot, COUNT(*) tot_giorni";

			myselect += " from rsa_posti_istituto where ";

			myselect += " cod_istituto = '" + dbr.get("ist_codice") + "'" + " and cod_tipoist ='" + dbr.get("tipoist")
			+ "'" + " and cod_org_mod ='" + dbr.get("org_mod") + "'";
			ISASRecord dbrl = dbc.readRecord(myselect);
			if (dbrl != null) {
				if(dbrl.get("tot_giorni")!=null && (dbrl.get("tot_giorni").toString().equals("0")) )return;				
				String sel = "select flag_dist_sesso from ";
				sel += "  rsa_posti_istituto where ";
				sel += " cod_istituto = '" + dbr.get("ist_codice") + "'" + " and cod_tipoist ='" + dbr.get("tipoist")
				+ "'" + " and cod_org_mod ='" + dbr.get("org_mod") + "'";
				ISASRecord dbrT = dbc.readRecord(sel);
				String flg = "N";
				if (dbrT != null)
					if (!isNullOrBlank(dbrT.get("flag_dist_sesso")))
						flg = dbrT.get("flag_dist_sesso").toString();
				// G.Brogi -----------------------------------------------
				int media_gg_disp = ((Integer)dbrl.get("posti_disp")).intValue()>0?
						((Integer)dbrl.get("posti_disp")).intValue() / ((Integer)dbrl.get("tot_giorni")).intValue():0;
						dbr.put("posti_disp", ""+media_gg_disp);
						int media_gg_tot = ((Integer)dbrl.get("posti")).intValue()>0?
								((Integer)dbrl.get("posti")).intValue() / ((Integer)dbrl.get("tot_giorni")).intValue():0;
								dbr.put("posti", ""+media_gg_tot);
								if (flg.equals("N"))
								{
									media_gg_disp = ((Integer)dbrl.get("posti_disp_tot")).intValue()>0?
											((Integer)dbrl.get("posti_disp_tot")).intValue() / ((Integer)dbrl.get("tot_giorni")).intValue():0;
											dbr.put("posti_disp", ""+media_gg_disp);
											media_gg_tot = ((Integer)dbrl.get("posti_tot")).intValue()>0?
													((Integer)dbrl.get("posti_tot")).intValue() / ((Integer)dbrl.get("tot_giorni")).intValue():0;
													dbr.put("posti", ""+media_gg_tot);
								}
								// G.Brogi fine -------------------------------------------


								/*G.Brogi				dbr.put("posti_disp", dbrl.get("posti_disp"));
				dbr.put("posti", dbrl.get("posti"));
				if (flg.equals("N")) {
					dbr.put("posti_disp", dbrl.get("posti_disp_tot"));
					dbr.put("posti", dbrl.get("posti_tot"));
				}
								 */

			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(nomeEJB + " Errore eseguendo una getPostiDisp()  ");
		}
	}
	//presente in RicoveriRSa
	private String getSesso(ISASConnection dbc, Hashtable h) throws SQLException {
		String sesso = "M";
		try {
			String sel = "select  sesso from cartella where n_cartella=" + h.get("n_cartella");
			ISASRecord dbr = dbc.readRecord(sel);
			if (dbr != null)
				return dbr.get("sesso").toString();
			return sesso;

		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException(nomeEJB + " Errore eseguendo una getSesso()  ");
		}
	}

	public String duplicateChar(String s, String c) {
		if ((s == null) || (c == null)) return s;
		if(s.equals(""))return s;
		String mys = new String(s);
		int p = 0;
		while (true) {
			int q = mys.indexOf(c, p);
			if (q < 0) return mys;
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException,CariException{
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);

			String myselect=getSelect(dbc,h);
			ISASCursor dbcur=dbc.startCursor(myselect);

			Vector vdbr=dbcur.getAllRecord();			
			for(int i=0;i<vdbr.size();i++){
                ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                if(dbr.get("cod_istituto_ingresso")!=null &&!dbr.get("cod_istituto_ingresso").toString().equals("")) {
               	 String strDescIst = util.getDecode(dbc,"istituti","ist_codice", dbr.get("cod_istituto_ingresso").toString(), "st_nome");
					 dbr.put("istituto", strDescIst);
                }
			 }
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(ISASPermissionDeniedException e){
			System.out.println(nomeEJB+".query(): "+e);
			throw new CariException(msg, -2);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}
	
	private String getSelect(ISASConnection dbc, Hashtable h) {
	debugMessage("getSelect h="+h.toString());
		String myselect="SELECT p.*,c.* "+
		" FROM rp_rsa_ricoveri p, cartella c WHERE p.n_cartella=c.n_cartella ";
	
		String s="";
		ServerUtility su =new ServerUtility();	
		
		if(h.get("cod_obbiettivo")!=null)
			s = su.addWhere(s, su.REL_AND, "p.cod_obbiettivo", su.OP_EQ_STR, h.get("cod_obbiettivo").toString());
		if(h.get("n_progetto")!=null)
			s = su.addWhere(s, su.REL_AND, "p.n_progetto", su.OP_EQ_NUM, h.get("n_progetto").toString());
		if(h.get("n_intervento")!=null)
			s = su.addWhere(s, su.REL_AND, "p.n_intervento", su.OP_EQ_NUM, h.get("n_intervento").toString());
		if(h.get("n_cartella")!=null )
			s = su.addWhere(s, su.REL_AND, "p.n_cartella", su.OP_EQ_NUM, h.get("n_cartella").toString());
			debugMessage("4");

			if(h.get("dataini")!=null && h.get("datafin")!=null && !h.get("dataini").equals("") && h.get("datafin").equals("")) {
				s = su.addWhere(s, su.REL_AND, "p.data_variazione", su.OP_GE_NUM,formatDate(dbc, (String)h.get("dataini")));
				s = su.addWhere(s, su.REL_AND, "p.data_variazione", su.OP_LE_NUM,formatDate(dbc, (String)h.get("datafin")));
			}
	
	    if(!s.equals("")) myselect+=" AND ";
		myselect+=s;
		myselect+=" ORDER BY c.cognome,c.nome,p.data_variazione desc";

		System.out.println("query GridRpRsa : "+myselect);
		return myselect;
	}


	public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		String scr=" ";
		String scr1=" ";
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);

			String myselect=getSelect(dbc,h);
			//controllo valore corretto cognome

			System.out.println("QueryPaginate  su rp_rsa_ricoveri: "+myselect);
			ISASCursor dbcur=dbc.startCursor(myselect,200);
			//Vector vdbr=dbcur.getAllRecord();
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			 for(int i=0;i<vdbr.size()-1;i++){
                 ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                 if(dbr.get("cod_istituto_ingresso")!=null &&!dbr.get("cod_istituto_ingresso").toString().equals("")) {
                	 String strDescIst = util.getDecode(dbc,"istituti","ist_codice", dbr.get("cod_istituto_ingresso").toString(), "st_nome");
					 dbr.put("istituto", strDescIst);
                 }
			 }
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		}catch(Exception e){
			e.printStackTrace();
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryPaginate()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public ISASRecord insertRsa(myLogin mylogin,Hashtable h, Vector preferenze)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,
	CariException
	{
		Vector v=null;
		boolean done=false;
		debugMessage("INSERT h="+h.toString());
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);

			dbc.startTransaction();
/*
			ISASRecord dbcart = null;
		if(h.containsKey("n_cartella") && 
				!h.get("n_cartella").toString().equals("")) 
			{
				dbcart=dbc.readRecord("select  * from cartella where n_cartella="+h.get("n_cartella"));
			}else dbc.rollbackTransaction();
*/
			String dataodierna=ndf.formDate(procdate.getitaDate(),"aaaa/mm/gg");
			//dateutility dt=new dateutility();
			
			ISASRecord dba = dbc.newRecord("rp_rsa_ricoveri");
			this.setISAS(dbc,"rp_rsa_ricoveri",dba,h);
			System.out.println(nomeEJB+" insert VADO A SCRIVERE: "+dba.getHashtable().toString());
			//dbc.writeRecord(dba);		
			String msg="";
			//se nuovoingresso travaso in rsa
			String strCodOperatore=dba.get("cod_operatore").toString();
			String strZonaOper="";
			debugMessage("COD OPER="+strCodOperatore);
			if (!strCodOperatore.equals("")) 
				strZonaOper = getZonaFromOperatore(dbc, strCodOperatore);
			debugMessage("COD OPER="+strCodOperatore+" ZONA="+strZonaOper);
			if(entratoRsaFl(dba,null) ) {
				msg +=chiudereRicovero(dbc,dba);
				msg+=travasoRsa(dbc,dba, strCodOperatore);
			}
			if(dba.get("fl_temporaneo").equals("S") &&
					dba.get("flag_stato").equals("ET")) {
				
				ISASRecord dbrNew=dbc.newRecord("rp_rsa_ricoveri");
				this.setISAS(dbc,"rp_rsa_ricoveri",dbrNew,dba.getHashtable());
				inGraduatoria(dbrNew,"ET",dba);
				if(dt.confrontaDate(dba.get("data_variazione"), dataodierna)!=0)
					dbrNew.put("data_variazione", ndf.formDate(dataodierna, "aaaa-mm-gg"));		
				else {
					dbrNew.put("data_variazione",ndf.formDate(dt.getDataNGiorni(dataodierna, 1), "aaaa-mm-gg") );
				}
				
				dba.put("zona",strZonaOper);
				dbrNew.put("zona",strZonaOper);
				dbc.writeRecord(dba);
				dbc.writeRecord(dbrNew);
				
				
			}else {
				dba.put("zona",strZonaOper);
				dbc.writeRecord(dba);
			}
			
			
			
			inserisciPreferenze(dbc,h,preferenze);
			dbc.commitTransaction();
			debugMessage("COMMIT fatta");
			ISASRecord dbret =leggiRsa(dbc,h);
			dbc.close();
			super.close(dbc);
			done = true;
			return dbret;
		}catch(CariException ce){
		           throw ce;
		} catch(DBRecordChangedException e) {
			System.out.println(nomeEJB+".insert(): "+e);
			e.printStackTrace();
			try{dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1 );
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			System.out.println(nomeEJB+".insert(): "+e);
			e.printStackTrace();
			try{dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e){
			System.out.println(nomeEJB+".insert(): "+e);
			e.printStackTrace();
			try{dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw new SQLException(e.getMessage());
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println(e2);
				}
			}
		}
	}

	
	private void inserisciPreferenze(ISASConnection dbc, Hashtable h, Vector v) throws SQLException,
	ISASPermissionDeniedException {
		System.out.println(nomeEJB + " inserisciPreferenze() - H == " + h.toString());

		DeleteRpPreferenze(dbc, h);
		String strNCartella = "";
	//	String strNProgetto = "";
	////	String strCodObiettivo = "";
	//	String strNIntervento = "";
	//	String data = "";

		try {
			strNCartella = (String) h.get("n_cartella");
		//	strNProgetto = (String) h.get("n_progetto");
		//	strCodObiettivo = ((String) h.get("cod_obbiettivo")).trim();
		//	strNIntervento = (String) h.get("n_intervento");
		//	data = (String) h.get("data_variazione");
		} catch (Exception ex) {
			System.out.println(nomeEJB + " ERRORE inserisciPreferenze() - MANCANO LE CHIAVI PRIMARIE " + ex);
			throw new SQLException(nomeEJB + " ERRORE inserisciPreferenze() - MANCANO LE CHIAVI PRIMARIE " + ex);
		}

		try {
			Enumeration en = v.elements();
			while (en.hasMoreElements()) {
				Hashtable ht = (Hashtable) en.nextElement();
				System.out.println("RIGA preferenza SEL == " + ht.toString());

			//	String istituto = ht.get("cod_istitu").toString();
				ISASRecord dbr = dbc.newRecord("rp_rsa_preferenze");
				this.setISAS(dbc,"rp_rsa_preferenze",dbr,ht);
				//dbr.put("cod_istitu", istituto);
				dbr.put("n_cartella", new Integer(strNCartella));
			//	dbr.put("n_progetto", new Integer(strNProgetto));
			//	dbr.put("cod_obbiettivo", strCodObiettivo);
			//	dbr.put("n_intervento", new Integer(strNIntervento));
				//dbr.put("data_variazione", ndf.formDate(data, "aaaa-mm-gg"));

				// Elisa 15/10/09
				//if (ht.containsKey("priorita") && ht.get("priorita") != null)
				//	dbr.put("priorita", ht.get("priorita"));

				dbc.writeRecord(dbr);
			}
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE inserisciPreferenze() - " + e);
			throw new SQLException(nomeEJB + " ERRORE inserisciPreferenze() - " + e);
		}
	}
	public ISASRecord updateRsa(myLogin mylogin,ISASRecord dbr, Vector preferenze)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,CariException
	{
		boolean done=false;
		ISASRecord dbret = null;
		ISASConnection dbc=null;
		debugMessage("UPDDATE dbr="+dbr.getHashtable().toString());
		try{
		//	dateutility dt=new dateutility();
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			ISASRecord dbrOld=leggiRsa(dbc,dbr.getHashtable());
			String dataodierna=ndf.formDate(procdate.getitaDate(),"aaaa/mm/gg");
			
			//ATTENZIONE SE SOGGETTO ENTRA IN STRUTTURA MA TEMPORANEO DEVE RIMANERE
			//IN GRADUATORIA!!!
			String strCodOperatore=dbr.get("cod_operatore").toString();
			String strZonaOper="";
			if (!strCodOperatore.equals("")) 
				strZonaOper = getZonaFromOperatore(dbc, strCodOperatore);
			debugMessage("COD OPER="+strCodOperatore+" ZONA="+strZonaOper);
			String msg="";
			//se nuovoingresso travaso in rsa
		/*	if(uscitoRsa(dbr,dbrOld)) {
				calcolagg();
			}*/
			if(entratoRsaFl(dbr,dbrOld) ) {
				msg +=chiudereRicovero(dbc,dbr);
				msg +=travasoRsa(dbc,dbr, strCodOperatore);
			}else			//se cambio informazoni ricovero
				if(modificatoRicovero(dbr,dbrOld) ) {
					msg+=aggiornaRicovero(dbc,dbr, strCodOperatore,dbrOld);
			}
				//gestione duplicazione rec per rimanere in graduatoria	
			if(dbrOld!=null && 	!dbrOld.get("flag_stato").equals(dbr.get("flag_stato")) 
			&& dbr.get("fl_temporaneo").equals("S") &&
					dbr.get("flag_stato").equals("ET")) {
				//&& (dbrOld.get("flag_stato").equals("GR")||dbrOld.get("flag_stato").equals("MD"))) {
				
				ISASRecord dbrNew=dbc.newRecord("rp_rsa_ricoveri");
				this.setISAS(dbc,"rp_rsa_ricoveri",dbrNew,dbr.getHashtable());
				/*Enumeration en=dbr.getHashtable().keys();
				while (en.hasMoreElements()) {
					String key=en.nextElement().toString();
					dbrNew.put(key,dbr.get(key));
				}*/
				
				inGraduatoria(dbrNew,dbrOld.get("flag_stato").toString(),dbr);
				if(dt.confrontaDate(dbrOld.get("data_variazione"), dbr.get("data_ingresso"))!=0)
					//if(dt.confrontaDate(dbrOld.get("data_variazione"), dataodierna)!=0)
					dbr.put("data_variazione", ndf.formDate(dbr.get("data_ingresso").toString(), "aaaa-mm-gg"));		
				else {
					dbr.put("data_variazione",ndf.formDate(dt.getDataNGiorni(dbr.get("data_ingresso").toString(), 1), "aaaa-mm-gg") );
					//dbr.put("data_variazione",ndf.formDate(dt.getDataNGiorni(dataodierna, 1), "aaaa-mm-gg") );
				}
					dbr.put("zona",strZonaOper);
				dbrNew.put("zona",strZonaOper);
				int days=1;
				boolean esiste=true;
				while(esiste) {				    
					if(nonEsiste(dbc,dbr))esiste=false;
					else {
						days++;
					dbr.put("data_variazione",
							ndf.formDate(dt.getDataNGiorni(dbr.get("data_ingresso").toString(), days), "aaaa-mm-gg") );	
					}
				}
				dbc.writeRecord(dbr);//rec ingresso
				dbc.writeRecord(dbrNew);//rec graduatoria
				
				
			}else if(dbrOld!=null && 	!dbrOld.get("flag_stato").equals(dbr.get("flag_stato")) 
					&& dbrOld.get("fl_temporaneo").equals("S")&& dbr.get("fl_temporaneo").equals("N") &&
							dbr.get("flag_stato").equals("ED")) {
				String sel = "select * from rp_rsa_ricoveri where n_cartella ="+dbr.get("n_cartella").toString()+" and (flag_stato = 'GT' or flag_stato = 'MT')";
				ISASRecord GT = dbc.readRecord(sel);
				if (GT!=null)dbc.deleteRecord(GT);
				dbc.writeRecord(dbr);
			}else {
				dbr.put("zona",strZonaOper);
				//bysp Se in graduatoria temporanea e ricovero temporaneo � deceduto, allora bisogna toglierlo dalla graduatoria temporanea
				
				if (dbr.get("motivo_uscita")!=null && dbr.get("motivo_uscita").toString().trim().equals("5"))
				{
					String cartella = dbr.get("n_cartella").toString();
					String flag_stato = "DE";
					String sel = "select * from rp_rsa_ricoveri where n_cartella ="+cartella+" and (flag_stato = 'GT' or flag_stato = 'MT')";
					ISASRecord GT = dbc.readRecord(sel);
					if (GT!=null)
					{
					GT.put("flag_stato",flag_stato);
					System.out.println("Aggiornamento FLAG_STATO RSA: Record che vado a scrivere"+ GT.getHashtable().toString());
					dbc.writeRecord(GT);
					}
				}
				dbc.writeRecord(dbr);
			}
			//	Vector preferenze=new Vector();
		//		if (dbr.get("preferenze")!=null)
			//	      preferenze=(Vector)dbr.get("preferenze");
				inserisciPreferenze(dbc,dbr.getHashtable(),preferenze);
			dbc.commitTransaction();

			//lettura record
			 dbret = leggiRsa(dbc,dbr.getHashtable());
			dbc.close();
			super.close(dbc);
			done=true;
			if (msg != null && !msg.equals("")) {
				System.out.println(" ECCEZIONE CARI EXCEPTION per RSA " + msg);
				throw new CariException(msg, -1);
			}
			
			return dbret;

		} catch (CariException ce) {
			ce.setISASRecord(dbret);
			throw ce;
		} catch(DBRecordChangedException e){
			e.printStackTrace();
			try{dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1 );
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			try{dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - "+  e1);
			}
			throw new SQLException("Errore eseguendo una update() - "+  e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){
					System.out.println(e2);
				}
			}
		}
	}
	private boolean nonEsiste(ISASConnection dbc,ISASRecord dbr) throws Exception {
	
		try{
			String myselect="Select p.* from rp_rsa_ricoveri p  where "+
			"p.n_cartella="+dbr.get("n_cartella")+
			" and p.data_variazione="+dbc.formatDbDate(dbr.get("data_variazione").toString());
			ISASRecord dbrl=dbc.readRecord(myselect);
			if(dbrl!=null) return false;
			else return true;
		}catch(Exception e){
			System.out.println("Errore eseguendo una nonEsiste: "+e);
			throw new SQLException("Errore eseguendo una nonEsiste: ");
		}	
	}
	private String chiudereRicovero(ISASConnection dbc,ISASRecord dbr)throws Exception {
		try{
			String myselect="Select p.*  from rp_rsa_ricoveri p  where "+
			"p.n_cartella="+dbr.get("n_cartella");
			 myselect+=" and p.data_variazione <>"+dbc.formatDbDate(dbr.get("data_variazione").toString())+
			 " and p.flag_stato='ET' and (p.data_uscita is null or p.motivo_uscita is null" +
			 " or p.motivo_uscita = '0' ) ";//bargi 04/04/2012 aggiunto motivo_uscita nel filtro
				
			ISASRecord dbrl=dbc.readRecord(myselect);
			if(dbrl!=null) {
				return "ATTENZIONE CHIUDERE IL RICOVERO TEMPORANEO PRECEDENTE!";
			}
			return "";
		}catch(Exception e){
			System.out.println("Errore eseguendo una recMaxRsa: "+e);
			throw new SQLException("Errore eseguendo una recMaxRsa: ");
			//  return null;
		}	
	}
	private boolean modificatoRicovero(ISASRecord dbr,ISASRecord dbrOld)throws Exception {
		boolean dentro=false;
		if(dbr==null ) return false;
		else {
			Object fle=dbr.get("fl_entrato");			
			dentro=getVal(fle).equals("S");
			debugMessage(nomeEJB+".entratoRsa test nuovo= "+dentro);
		}
		if(dbrOld==null ) return !dentro;
		else {
			Object fle=dbrOld.get("fl_entrato");	
			debugMessage("flag in old:"+fle);
			boolean dentroold=getVal(fle).equals("S");
			dentro=dentro&&dentroold;
		}
		if(dentro) return isModificato(dbr,dbrOld);
		return dentro;
	}
	private boolean isModificato(ISASRecord dbr,ISASRecord dbrOld) throws Exception {
		boolean mod=false;
		if(compare((dbr.get("data_ingresso")),dbrOld.get("data_ingresso")))return true;
		if(compare((dbr.get("data_uscita")),dbrOld.get("data_uscita")))return true;
		if(compare((dbr.get("cod_istituto_ingresso")),dbrOld.get("cod_istituto_ingresso")))return true;
		if(compare((dbr.get("data_uvg")),dbrOld.get("data_uvg")))return true;
		if(compare((dbr.get("motivo_uscita")),dbrOld.get("motivo_uscita")))return true;
		if(compare((dbr.get("fl_integra")),dbrOld.get("fl_integra")))return true;
		if(compare((dbr.get("fl_integr_fascia")),dbrOld.get("fl_integr_fascia")))return true;
		if(compare((dbr.get("cod_tipo_assistito")),dbrOld.get("cod_tipo_assistito")))return true;
		if(compare((dbr.get("cod_org")),dbrOld.get("cod_org")))return true;
		if(compare((dbr.get("tipo_ricovero")),dbrOld.get("tipo_ricovero")))return true;
		return mod;
	}
	private boolean compare(Object o1,Object o2) {
		debugMessage("compare ["+getVal(o1) +"] **** ["+getVal(o2)+"]");
		
		if (getVal(o1).indexOf("/")>0|| getVal(o1).indexOf("-")>0) {//si tratta di data
			o1=ndf.formDate(o1.toString(), "gg/mm/aaaa");
			debugMessage("data o1="+o1);
		}
		if (getVal(o2).indexOf("/")>0|| getVal(o2).indexOf("-")>0) {//si tratta di data
			o2=ndf.formDate(o2.toString(), "gg/mm/aaaa");
			debugMessage("data o2="+o2);
		}	
		boolean rit=true;
		if(getVal(o1).equals(getVal(o2)))rit= false;
		
		debugMessage("compare sono diversi? "+rit);
		return rit;
			
	}
	private boolean entratoRsaFl(ISASRecord dbr,ISASRecord dbrOld)throws Exception {
		boolean dentro=false;
		if(dbr==null ) return false;
		else {
			Object fle=dbr.get("fl_entrato");			
			dentro=getVal(fle).equals("S");
			debugMessage(nomeEJB+".entratoRsa test nuovo= "+dentro);
		}
		if(dbrOld==null ) return dentro;
		else {
			Object fle=dbrOld.get("fl_entrato");	
			debugMessage("flag in old:"+fle);
			boolean dentroold=getVal(fle).equals("S");
			dentro=dentro&& !dentroold;
		}
		return dentro;
	}/*
	private boolean entratoRsa(ISASConnection dbc,ISASRecord dbr,ISASRecord dbrOld)throws Exception {
		boolean dentro=false;
		if(dbr==null ) return false;
		else {
			Object dataIn=dbr.get("data_ingresso");
			Object istIn=dbr.get("cod_istituto_ingresso");
			dentro=(dataIn!=null && !dataIn.toString().equals(""))&&(istIn!=null && !istIn.toString().equals(""));
			debugMessage(nomeEJB+".entratoRsa test nuovo= "+dentro);
		}
		if(dbrOld==null ) return dentro;
		else {
			Object dataIn=dbrOld.get("data_ingresso");
			Object istIn=dbrOld.get("cod_istituto_ingresso");
			debugMessage("datain old:"+dataIn);
			debugMessage("istin old:"+istIn);
			boolean dentroold=(dataIn==null || dataIn.toString().equals(""))&&(istIn==null|| istIn.toString().equals(""));
			dentro=dentro&&dentroold;
		}
		return dentro;
	}*/
	private boolean uscitoRsa(ISASRecord dbr,ISASRecord dbrOld)throws Exception {
		boolean uscito=false;
		if(dbr==null ) return false;
		else {
			Object dataOut=dbr.get("data_uscita");
			uscito=(dataOut!=null && !dataOut.toString().equals(""));
			debugMessage(nomeEJB+".uscitoRsa test = "+uscito);
		}
		if(dbrOld==null ) return uscito;
		else {
			Object dataOut=dbrOld.get("data_uscita");
			uscito=(dataOut!=null && !dataOut.toString().equals(""));
			debugMessage(nomeEJB+".uscitoRsa test = "+uscito);
			boolean uscitoold=(dataOut==null || dataOut.toString().equals(""));
			uscito=uscito&&uscitoold;
		}
		return uscito;
	}
private void inGraduatoria(ISASRecord dbr,String flagold,ISASRecord dbrNew)throws Exception {
	if(flagold.equals("GR"))
	dbr.put("flag_stato","GT");
	else if(flagold.equals("MD"))
		dbr.put("flag_stato","MT");
	else if(dbrNew.get("cod_org").equals("MD")) dbr.put("flag_stato","MT");
	else if(!dbrNew.get("cod_org").equals("MD")) dbr.put("flag_stato","GT");
	dbr.put("data_ingresso", "");
	dbr.put("data_uscita", "");
	dbr.put("fl_entrato", "N");
	dbr.put("urgente", "N");
	dbr.put("data_urgenza", "");
	dbr.put("fl_temporaneo", "N");
	dbr.put("cod_istituto_ingresso", "");
}
private String travasoRsa(ISASConnection dbc, ISASRecord dbrRic, String strCodOperatore)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,CariException {
System.out.println(nomeEJB + ".travasoRsa() - dbrRic == " + dbrRic.getHashtable().toString());

String messaggio="";
try {
String strAbilitazRsa = selectConf(dbc, strCodOperatore, "ABILITAZ_RSA");
if (!strAbilitazRsa.equals("SI"))
	return "";

System.out.println("***** t r a v a s o R s a OPERATORE abilitato a RSA ********");
String istituto_nuovo = dbrRic.get("cod_istituto_ingresso").toString();
String data = dbrRic.get("data_ingresso").toString();
String mysel = "SELECT * FROM rsa_ricoveri WHERE " + " ric_datric=" + dbc.formatDbDate(data)
		+ " and n_cartella=" + dbrRic.get("n_cartella") + " and ric_codist='" + istituto_nuovo + "'";
debugMessage("travasoRsa sel==>"+mysel);
ISASRecord dbr = dbc.readRecord(mysel);
ISASRecord dbr_ric = null;// rsa_ricoveri

if (dbr != null) {
	messaggio += "Il ricovero risulta gi� attivo nell'applicativo gestione delle rette ";
} else {
	dbr_ric = dbc.newRecord("rsa_ricoveri");
	dbr_ric.put("ric_datric", dbrRic.get("data_ingresso"));
	dbr_ric.put("n_cartella", dbrRic.get("n_cartella"));
	dbr_ric.put("ric_codist", istituto_nuovo);
	dbr_ric.put("ric_dataut", dbrRic.get("data_uvg"));
	dbr_ric.put("ric_motdim", dbrRic.get("motivo_uscita"));
	if(dbrRic.get("data_uscita")==null) {
		String out="31-12-"+dt.getAnno(dbrRic.get("data_ingresso"));
			dbr_ric.put("ric_datdim",ndf.formDate(out,"aaaa-mm-gg"));
		}else	dbr_ric.put("ric_datdim", dbrRic.get("data_uscita"));

	dbr_ric.put("fl_integra", dbrRic.get("fl_integra"));
	dbr_ric.put("fl_integr_fascia", dbrRic.get("fl_integr_fascia"));

	dbr_ric.put("cod_tipo_assistito", dbrRic.get("cod_tipo_assistito"));
	dbr_ric.put("org_mod", dbrRic.get("cod_org"));
	String valore = dbrRic.get("tipo_ricovero") + "";
	if (valore != null ) {
		if (valore.trim().equals("0"))valore = "2";//vitalizio
		else if(valore.trim().equals("1")) valore = "1";//temporaneo
	}
	dbr_ric.put("ric_flag1", valore);
	dbr_ric.put("cod_operatore", strCodOperatore);
	messaggio+="Ricovero inserito nell'applicativo gestione delle rette ";
	dbc.writeRecord(dbr_ric);
}
/*
if (!messaggio.equals("")) {
	System.out.println("travasoRsa CariException" + messaggio);
	throw new CariException(messaggio);
} */

return messaggio;
}	catch (Exception e) {
e.printStackTrace();
throw new SQLException("Errore eseguendo travasoRsa() e() - " + e);
}
}


private String aggiornaRicovero(ISASConnection dbc, ISASRecord dbrRic, String strCodOperatore,ISASRecord dbrOld)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException,CariException {
System.out.println(nomeEJB + ".aggiornoRicovero() - dbrRic == " + dbrRic.getHashtable().toString());

String messaggio="";
try {
String strAbilitazRsa = selectConf(dbc, strCodOperatore, "ABILITAZ_RSA");
if (!strAbilitazRsa.equals("SI"))
	return "";

System.out.println("***** aggiornoRicovero OPERATORE abilitato a RSA ********");
String istituto_nuovo = dbrRic.get("cod_istituto_ingresso").toString();
String istituto_old = dbrOld.get("cod_istituto_ingresso").toString();
String data = dbrRic.get("data_ingresso").toString();
String data_ric_old = dbrOld.get("data_ingresso").toString();

String mysel = "SELECT * FROM rsa_ricoveri WHERE " + " ric_datric=" + dbc.formatDbDate(data)
		+ " and n_cartella=" + dbrRic.get("n_cartella") + " and ric_codist='" + istituto_nuovo + "'";

ISASRecord dbr_ric = dbc.readRecord(mysel);

if (dbr_ric == null) {
	 mysel = "SELECT * FROM rsa_ricoveri WHERE " + " ric_datric=" + dbc.formatDbDate(data_ric_old)
	+ " and n_cartella=" + dbrRic.get("n_cartella") + " and ric_codist='" + istituto_old + "'";
	 ISASRecord dbr2 = dbc.readRecord(mysel);
	 if(dbr2!=null) messaggio += "Il ricovero risulta ATTIVO nell'applicativo gestione delle rette. \n " +
	 		" Essendo cambiato l'istituto e/o la data di ricoveronon e' stato possibile riportare le modifiche all'applicativo gestione delle rette !  ";
	 else 	messaggio += "Il ricovero NON risulta ATTIVO nell'applicativo gestione delle rette!  ";
} else {
	//dbr_ric.put("ric_datric", dbrRic.get("data_ingresso"));
	debugMessage("INVOCO DA aggiornaricovero COMPARE data ricovero");
	/*if(compare(dbrRic.get("data_ingresso"),dbr_ric.get("ric_datric"))) {
		messaggio += "La data di ricovero nell'applicativo gestione delle rette risulta con data diversa: "+dbr_ric.get("ric_datric").toString();
	}*/
	dbr_ric.put("ric_codist", istituto_nuovo);
	dbr_ric.put("ric_dataut", dbrRic.get("data_uvg"));
	dbr_ric.put("ric_motdim", dbrRic.get("motivo_uscita"));
	dbr_ric.put("ric_datdim", dbrRic.get("data_uscita"));
	dbr_ric.put("fl_integra", dbrRic.get("fl_integra"));
	dbr_ric.put("fl_integr_fascia", dbrRic.get("fl_integr_fascia"));
	dbr_ric.put("cod_tipo_assistito", dbrRic.get("cod_tipo_assistito"));
	dbr_ric.put("org_mod", dbrRic.get("cod_org"));
	String valore = dbrRic.get("tipo_ricovero") + "";
	if (valore != null ) {
		if (valore.trim().equals("0"))valore = "2";//vitalizio
		else if(valore.trim().equals("1")) valore = "1";//temporaneo
	}
	dbr_ric.put("ric_flag1", valore);
	dbr_ric.put("cod_operatore", strCodOperatore);
	messaggio+="Ricovero aggiornato nell'applicativo gestione delle rette ";
	dbc.writeRecord(dbr_ric);
}
/*
if (!messaggio.equals("")) {
	System.out.println("travasoRsa CariException" + messaggio);
	throw new CariException(messaggio);
} */

return messaggio;
}	catch (Exception e) {
e.printStackTrace();
throw new SQLException("Errore eseguendo travasoRsa() e() - " + e);
}
}
private String selectConf(ISASConnection dbc, String strCodOperatore, String key) throws SQLException {
	String ret = "NO";
	String strZona = "";
	String strZonaOper = "";

	try {
		if (!strCodOperatore.equals("")) {
			strZonaOper = getZonaFromOperatore(dbc, strCodOperatore);
			if (strZonaOper.trim().equals(""))// non esiste zona per l'oper
				System.out.println("!!!! PunteggioEJB.selectConf: NON esiste zona su OPERATORI per l'operatore=["
						+ strCodOperatore + "] !!!!");
		}

		String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='" + key + "'";
		// System.out.println("PunteggioEJB/selectConf/mysel: " + mysel);
		ISASRecord dbConf = dbc.readRecord(mysel);
		if ((dbConf != null) && dbConf.get("conf_txt") != null) {
			if (strZonaOper.equals(""))
				ret = (String) dbConf.get("conf_txt");
			else {
				String strVal = (String) dbConf.get("conf_txt");
				ret = getValxZona(strVal, strZonaOper);
			}
		}
		return ret;
	} catch (Exception ex) {
		System.out.println(nomeEJB + " ERRORE selectConf() - " + ex);
		throw new SQLException(nomeEJB + " ERRORE selectConf() - " + ex);
	}
}
// configurazione diversa per ogni zona -> si prevede una codifica
// del tipo "#codZona1=xxxx#codZona2=yyyy....#codZonaN=zzzz".
private String getValxZona(String val, String zonaOper) {
	String rit = "";

	// non esiste codifica per zona -> ritorno il valore cos� com'� letto,
	// visto che il valore � unico per tutte le zone.
	if (val.indexOf("#") == -1)
		return val;

	if ((zonaOper != null) && (!zonaOper.trim().equals(""))) {
		boolean trovato = false;
		String keyZona = zonaOper + "=";
		StringTokenizer strTkzZona = new StringTokenizer(val, "#");
		while ((strTkzZona.hasMoreTokens()) && (!trovato)) {
			String tkZona = strTkzZona.nextToken();
			int pos = tkZona.indexOf(keyZona);
			trovato = (pos != -1);
			if (trovato)
				rit = tkZona.substring(pos + zonaOper.length() + 1);
		}
	}

	if (rit.trim().equals(""))// non esiste codifica x la zona dell'oper
		// (oppure oper senza zona!)
		System.out.println("!!!! RicoveriRsa.getValxZona: NON esiste codifica su CONF per la zona=[" + zonaOper
				+ "] !!!!");
	return rit;
}
private String getZonaFromOperatore(ISASConnection dbc, String strCodOperatore) throws SQLException {
	try {
		String ret = "";
		String strQuery = "SELECT cod_zona" + " FROM operatori" + " WHERE codice = '" + strCodOperatore + "'";
		System.out.println("RicoveriRsa/getZonaFromOperatore/strQuery: " + strQuery);
		ISASRecord dbr = dbc.readRecord(strQuery);
		if ((dbr != null) && dbr.get("cod_zona") != null)
			ret = (String) dbr.get("cod_zona");
		return ret;
	} catch (Exception ex) {
		throw new SQLException("RicoveriRsa.getZonaFromOperatore() " + ex);
	}
}
	//public void deleteAll(myLogin mylogin,ISASRecord dbr)
	public void deleteAll(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			debugMessage("deleteAll "+h);
			DeleteRpRsa(dbc,h);
			Hashtable ht=(Hashtable)h.clone();
			ISASRecord dbrL=recMaxRsa(dbc,ht);
			if(dbrL==null)DeleteRpPreferenze(dbc,h);
			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	private void DeleteRpRsa(ISASConnection dbc,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
	{
		try{
			int i = 0;
			String sel="select * from rp_rsa_ricoveri where "+
				" n_cartella = "+h.get("n_cartella")+
			" and data_variazione="+dbc.formatDbDate(h.get("data_variazione").toString());
			ISASRecord dbr=dbc.readRecord(sel);
			dbc.deleteRecord(dbr);
			
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}
		catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una DeleteRpRsa() - "+  e+"  ");
		}

	}

	private void DeleteRpPreferenze(ISASConnection dbc, Hashtable h) throws SQLException, ISASPermissionDeniedException {
		ISASCursor isascur = null;
		try {
			String sel = selectIstitutiRichiesti(dbc, h);//tutte preferenze legate a cartella!!!
			if (sel.equals(""))
				throw new SQLException(nomeEJB+"DeleteRpPreferenze()-->MANCANO LE CHIAVI PRIMARIE");

			isascur = dbc.startCursor(sel);

			while (isascur.next()) {
				ISASRecord dbr = dbc.readRecord(sel);
				dbc.deleteRecord(dbr);
			}

			isascur.close();

		} catch (ISASPermissionDeniedException e) {
			System.out.println(nomeEJB + " DeleteRpPreferenze() - eccezione permesso negato " + e);
			return;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE DeleteRpPreferenze() - " + e);
			throw new SQLException(nomeEJB + " ERRORE DeleteRpPreferenze() - " + e);
		}
	}

	private String selectIstitutiRichiesti(ISASConnection dbc, Hashtable h) throws Exception, SQLException {
		debugMessage(nomeEJB + " selectIstitutiRichiesti() - H == " + h.toString());

		String strNCartella = "";
	//	String strNProgetto = "";
	//	String strCodObiettivo = "";
	//	String strNIntervento = "";
	//	String data = "";

		try {
			strNCartella = h.get("n_cartella").toString();
		//	strNProgetto = h.get("n_progetto").toString();
		//	strCodObiettivo = (h.get("cod_obbiettivo").toString()).trim();
		//	strNIntervento = h.get("n_intervento").toString();
		//	data = h.get("data_variazione").toString();
		} catch (Exception ex) {
			debugMessage(nomeEJB + " ERRORE getIstitutiRichiesti() --> MANCANO LE CHIAVI PRIMARIE " + ex);
			return "";
		}

		try {
			String sel = "SELECT * " + " FROM rp_rsa_preferenze " + " WHERE n_cartella = " + strNCartella;
			//+ " AND n_progetto = " + strNProgetto + " AND trim(cod_obbiettivo) = '" + strCodObiettivo + "'"
		//	+ " AND n_intervento = " + strNIntervento 
		//	+ " AND data_variazione = " + dbc.formatDbDate(data);

			System.out.println(nomeEJB + " selectIstitutiRichiesti() - " + sel);
			return sel;
		} catch (Exception e) {
			System.out.println(nomeEJB + " ERRORE selectistitutirichiesti() - " + e);
			throw new SQLException(nomeEJB + " ERRORE selectistitutirichiesti() - " + e);
		}
	}
	private ISASRecord setISAS(ISASConnection dbc,String nome_tab,ISASRecord dbred,Hashtable strutt)
	throws Exception
	{
		try {
		ISASMetaInfo mt=new ISASMetaInfo(dbc,nome_tab);
		Enumeration en=mt.getCampi();
		while(en.hasMoreElements()) {
			String campo=en.nextElement().toString();
			String tipo=mt.getType(campo);
			campo=campo.toLowerCase();
			debugMessage("*************** tabella:"+nome_tab+" campo["+campo+"] tipo["+tipo+"]");
			if(tipo.equalsIgnoreCase("NUMBER")||tipo.equalsIgnoreCase("INTEGER")) {
				if(campo.equals("n_cartella")&&strutt.get(campo)==null)
					dbred.put(campo,strutt.get(campo));
				else
				  dbred.put(campo,strutt.get(campo)!=null?strutt.get(campo):new Integer(0));
			}else if(tipo.equalsIgnoreCase("DATE")) {
				if(!getVal(strutt.get(campo)).equals(""))dbred.put(campo,ndf.formDate(getVal(strutt.get(campo)),"aaaa-mm-gg"));
				else dbred.put(campo,null);
			}else dbred.put(campo,getVal(strutt.get(campo)));
			
		}
		return dbred;
		}catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
	public Vector  getStoricoVisite(myLogin mylogin,Hashtable ht) throws  Exception {
		boolean done=false;
		String scr=" ";
		ISASConnection dbc=null;
		System.out.println("getStoricoVisite  h passato"+ht.toString());
		Vector vRet=new Vector();
		try{
			dbc=super.logIn(mylogin);
			String sel="select  * from rp_storico_visite  a"+
			" where a.n_cartella="+ht.get("n_cartella")+
			" order by data_visita desc";
			debugMessage("getStoricoVisite--->"+sel);
			ISASCursor dbcur=dbc.startCursor(sel);
			vRet=dbcur.getAllRecord();
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done=true;
			return vRet;
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Errore eseguendo reperimento getStoricoVisite()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}
	private static String getVal(Object o){
		if(o==null)return "";
		else if(o.toString().trim().equals("null"))return "";
		else return o.toString().trim();
	}
	private static boolean isNullOrBlank(Object o) {
		return (o == null || o.toString().trim().equals("") || o.toString().trim().equals("null"));
	}

}

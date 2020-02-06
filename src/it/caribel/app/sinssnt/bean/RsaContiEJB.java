package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------

//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
// ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
// 03/02/2003 - EJB di connessione alla procedura SINS Tabella Conti
// Jessica Caccavale
// ==========================================================================

import java.util.*;
import java.math.BigDecimal; 
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.rsa_routine.routineRsa;
import it.pisa.caribel.sinssnt.connection.*;
//import it.pisa.caribel.sins_rs.connection.*;
import it.pisa.caribel.util.*;

//TODO 28/04/2010 ATTENZIONE QUESTA CLASSE DEVE ESSERE TENUTA 
//ALLINEATA CON QUELLA PRESENTE SOTTO RSA!!!!
//public class RsaContiEJB extends SINS_RSConnectionEJB 
public class RsaContiEJB extends SINSSNTConnectionEJB  
{
	private Converti conv = new Converti();
	private static String nomeEJB = "RsaContiEJB ";
	private it.pisa.caribel.util.BigDecimalCalculator calc = new it.pisa.caribel.util.BigDecimalCalculator(4, BigDecimal.ROUND_HALF_EVEN);

	public RsaContiEJB() {}

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{
			dbc = super.logIn(mylogin);
			
			ISASRecord dbr = queryKey(dbc, h);
			if(dbr != null)
				System.out.println(nomeEJB + " queryKey() public - DBR prima di return  == " + dbr.getHashtable().toString());
				else System.out.println(nomeEJB + " queryKey() public dbr nullo");
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new SQLException(nomeEJB + " Errore eseguendo una queryKey()  ");
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1){   System.out.println(nomeEJB + " QueryKey(): " + e1);   }
			}
		}
	}
	
	private ISASRecord queryKey(ISASConnection dbc, Hashtable h)throws  SQLException 
	{
		String anno = "";
		String codice = "";
		String cc = "";
		
		try
		{
			anno = h.get("eco_anno").toString();
			codice = h.get("eco_codice").toString();
			cc = h.get("eco_centrocosto").toString();
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " QueryKey(privata) -- manca chiave primaria " + e);
			throw  new SQLException(); 
		}
		System.out.println(nomeEJB + " QueryKey(privata) h che arriva==="+h.toString());
		try
		{
			String myselect = " SELECT * from rsa_economici WHERE "+
						" eco_anno = '" + anno + "' AND eco_codice = '" + codice +
						"' AND eco_centrocosto = '" + cc + "' ";
			
			ISASRecord dbr = dbc.readRecord(myselect);
			System.out.println(nomeEJB + " QueryKey(privata) "+myselect);
			
			if(dbr != null)
			{
				Vector contributi = getAllContributi(dbc,h);
				
				String cod_cc = dbr.get("eco_centrocosto").toString();
				String desc_cc = ISASUtil.getDecode(dbc, "rsa_centrocosto", "codice", cod_cc, "descrizione");

				dbr.put("desc_cc", desc_cc);
				
//G.Brogi 16/03/10				String da = dbr.get("eco_dareavere").toString();
//				if(da.equals("0"))	dbr.put("desc_dareavere", "Dare");
//				else dbr.put("desc_dareavere", "Avere");
				String da = dbr.get("eco_socsan").toString();
				if(da.equals("0"))	dbr.put("desc_socsan", "Sociale");
				else dbr.put("desc_socsan", "Sanitario");
				
				if(contributi != null && contributi.size() > 0)
					dbr.put("griglia_contributi", contributi);
				
				//Elisa 25/11/09
				String zona = ISASUtil.getDecode(dbc, "rsa_centrocosto", "codice", cc, "cod_zona");
				String distr = ISASUtil.getDecode(dbc, "rsa_centrocosto", "codice", cc, "cod_distretto");
				dbr.put("cod_zona", zona);
				dbr.put("cod_distretto", distr);
				// -----------------------------------------------------------
				
				System.out.println(nomeEJB + " queryKey() - DBR == " + dbr.getHashtable().toString());
			}
			if(dbr != null)
			System.out.println(nomeEJB + " queryKey() - DBR prima di return  == " + dbr.getHashtable().toString());
			else System.out.println(nomeEJB + " queryKey() dbr nullo");
			return dbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE QueryKey(privata)  " + e);
			throw new SQLException(nomeEJB + " ERRORE QueryKey(privata)  " + e);
		}
	}
	
	private Vector getAllContributi(ISASConnection dbc, Hashtable h) throws Exception
	{
		try
		{
			String codice = "";
			String centrocosto = "";
			String anno = "";
			
			try
			{
				codice = h.get("eco_codice").toString();
				centrocosto = h.get("eco_centrocosto").toString();
				anno = h.get("eco_anno").toString();
			}
			catch(Exception e)
			{
				System.out.println(" getAllContributi() - manca chiave primaria - [" + e + "]");
				throw e;
			}
			
			String mysel = "SELECT * FROM rsa_contributi WHERE eco_anno = '" + anno + "' " +
						" AND eco_codice = '" + codice + 
						"' AND eco_centrocosto = '" + centrocosto + "' ORDER BY con_codice";
			
			System.out.println(" getAllContributi() - select = " + mysel);
			
			ISASCursor cur = dbc.startCursor(mysel);
			if(cur != null)
			{
				Vector v = cur.getAllRecord();
				if(v != null && v.size() > 0)
				{
					for(int i = 0; i < v.size(); i++)
					{
						ISASRecord rec = (ISASRecord) v.get(i);
						
						rec.put("desc_assistito", ISASUtil.getDecode(dbc,"rsa_tipo_assistito","codice",
								rec.get("cod_tipo_assistito").toString(),"descrizione"));
						
						rec.put("cod_tipass", ISASUtil.getDecode(dbc, "rsa_tipo_assistito", "codice", 
								rec.get("cod_tipo_assistito").toString(), "cod_tipass"));
						
						if(rec.get("con_socsan").toString().equals("2"))
							rec.put("desc_socsan", "sociale");
						else rec.put("desc_socsan", "sanitario");
												
						if(rec.get("con_tipute") != null && !rec.get("con_tipute").equals("!!"))
							rec.put("desc_tipute", ISASUtil.getDecode(dbc, "tipute", "codice", 
													rec.get("con_tipute").toString(), "descrizione"));	
					}
					
					return v;
				}
				else return null;
			}
			else return null;
		}
		catch(Exception e)
		{
			System.out.println(" getAllContributi() - ERRORE - [" + e + "] ");
			throw e;
		}
	}

	public String duplicateChar(String s, String c) 
	{
		if ((s == null) || (c == null)) return s;
	
		String mys = new String(s);
		int p = 0;
		while (true) 
		{
			int q = mys.indexOf(c, p);
			if (q < 0) return mys;
		
			StringBuffer sb = new StringBuffer(mys);
			StringBuffer sb1 = sb.insert(q, c);
			mys = sb1.toString();
			p = q + c.length() + 1;
		}
	}

	public Vector queryPaginate(myLogin mylogin, Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " queryPaginate() -- Hash: " + h.toString());
		
		boolean done = false;
		String scr = "";
		ISASConnection dbc = null;
		ISASCursor dbcur=null;
		try
		{
			dbc = super.logIn(mylogin);
			String myselect = "";
			boolean join = false;
			String desc_cc = "";
			
			//Elisa 25/11/09
			// Per ora commento...non mi ricordo perche' c'ho messo questo controllo, non ha senso....
			/*if(h.containsKey("desc_cc") && !h.get("desc_cc").equals(""))
			{
				join = true;
				desc_cc = h.get("desc_cc").toString();
				if(!desc_cc.equals("") && desc_cc!=null)
				{
					desc_cc = duplicateChar(desc_cc,"'");
					// modifico myselect per far mettere la 'AND' alle condizioni successive
					// se e' presente la descrizione del centro di costo, devo fare la join!!!
					myselect = " ";
				}
			}*/
			
			//elisa 27/10/09
			if(h.containsKey("cod_zona") && !h.get("cod_zona").equals(""))
			{
				scr = h.get("cod_zona").toString();
				if(!scr.equals("") && scr!=null)
					myselect = myselect+" AND c.cod_zona = '"+scr+"' ";
								
				join = true;
			}
			
			if(h.containsKey("cod_distretto") && !h.get("cod_distretto").equals(""))
			{
				scr = h.get("cod_distretto").toString();
				if(!scr.equals("") && scr!=null)
					myselect = myselect+" AND c.cod_distretto = '"+scr+"' ";
								
				join = true;
			}
			// -----------------------------------------------------------------------
			
			if(h.containsKey("eco_descri"))
			{
				scr = h.get("eco_descri").toString();
				if(!scr.equals("") && scr!=null) 
				{
					scr = duplicateChar(scr,"'");
					if(myselect.equals(""))	myselect = " e.eco_descri like '%"+scr+"%' ";
					else myselect = myselect + " AND e.eco_descri like '%"+scr+"%' ";
				}
			}
			
			// Elisa 29/06/09: al posto di "eco_comune" 
			if(h.containsKey("eco_centrocosto") && !h.get("eco_centrocosto").equals(""))
			{
				scr= h.get("eco_centrocosto").toString();
				if(!scr.equals("") && scr!=null)
					if(myselect.equals(""))	myselect = " e.eco_centrocosto = '"+scr+"' ";
					else	myselect = myselect + " AND e.eco_centrocosto = '"+scr+"' ";
			}
			
			if(h.containsKey("eco_anno") && !h.get("eco_anno").equals(""))
			{
				scr = h.get("eco_anno").toString();
				if(!scr.equals("") && scr!=null)
					if(myselect.equals(""))	myselect = " e.eco_anno = '"+scr+"' ";
					else	myselect=myselect+" AND e.eco_anno = '"+scr+"' ";
			}
			
			if(h.containsKey("eco_codice") && !h.get("eco_codice").equals(""))
			{
				scr = h.get("eco_codice").toString();
				if(!scr.equals("") && scr!=null)
					if(myselect.equals(""))	myselect = " e.eco_codice = '"+scr+"' ";
					else myselect=myselect+" AND e.eco_codice = '"+scr+"' ";
			}
			
//G.Brogi 16/03/10			if(h.containsKey("eco_dareavere") && !h.get("eco_dareavere").equals(""))
			if(h.containsKey("eco_socsan") && !h.get("eco_socsan").equals(""))
			{
//G.Brogi 16/03/10				scr = h.get("eco_dareavere").toString();
				scr = h.get("eco_socsan").toString();
				if(!scr.equals("2")) // 2 == tutte le tipologie di conto
				{
//G.Brogi 16/03/10					if(myselect.equals(""))	myselect = " e.eco_dareavere = '" + scr + "' ";
//					else myselect += " AND e.eco_dareavere = '" + scr + "' ";
					if(myselect.equals(""))	myselect = " e.eco_socsan = '" + scr + "' ";
					else myselect += " AND e.eco_socsan = '" + scr + "' ";
				}
			}
					
			// se ho delle condizioni e non ho da fare la join metto il WHERE
			if(!myselect.equals("") && !join) myselect = " WHERE " + myselect;			

			if(join && !desc_cc.equals(""))
				myselect = " SELECT e.*,c.*, c.descrizione desc_cc  FROM rsa_economici e, rsa_centrocosto c WHERE " +
							" c.codice = e.eco_centrocosto "+
							" AND c.descrizione like '%" + desc_cc + "%' " + myselect +
							" ORDER BY e.eco_codice, e.eco_centrocosto, e.eco_descri ";
			else if(join)
				myselect = " SELECT e.*,c.*, c.descrizione desc_cc  FROM rsa_economici e, rsa_centrocosto c WHERE " +
				" c.codice = e.eco_centrocosto "+ myselect +
				" ORDER BY e.eco_codice, e.eco_centrocosto, e.eco_descri ";
			else myselect = " SELECT * FROM rsa_economici e " + myselect +
						" ORDER BY e.eco_codice, e.eco_centrocosto, e.eco_descri";
			
			System.out.println(nomeEJB + " QueryPaginate() -- select: " + myselect);
			
			 dbcur=dbc.startCursor(myselect);
			
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			Vector vdbr = dbcur.paginate(start, stop);
			
			for(int i = 0; i < vdbr.size()-1; i++)
			{
				ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
				
				System.out.println(nomeEJB + " DBR == " + dbr.getHashtable().toString());
				double budget=0;
				double impeg_fittizio=0;
				double impeg_reale=0;
				
				if(dbr.get("eco_previs") != null && !dbr.get("eco_previs").equals(""))
				{
					budget=((Double)dbr.get("eco_previs")).doubleValue();
					String ecoprevis = conv.convertiDouble(budget);
					//System.out.println("eco_previs == " + ecoprevis);
					dbr.put("eco_previs", ecoprevis);
				}
				
				if(dbr.get("eco_utiliz") != null && !dbr.get("eco_utiliz").equals(""))
				{					
					String ecoutil = conv.convertiDouble(((Double)dbr.get("eco_utiliz")).doubleValue());
					//System.out.println("eco_utiliz == " + ecoutil);
					dbr.put("eco_utiliz", ecoutil);
				}
				
				if(dbr.get("eco_impreale") != null && !dbr.get("eco_impreale").equals(""))
				{
					impeg_reale=((Double)dbr.get("eco_impreale")).doubleValue();
					String eco_impreale = conv.convertiDouble(impeg_reale);
					//System.out.println("eco_impreale == " + eco_impreale);
					dbr.put("eco_impreale", eco_impreale);
				}
				
				if(dbr.get("eco_imppresu") != null && !dbr.get("eco_imppresu").equals(""))
				{
					impeg_fittizio=((Double)dbr.get("eco_imppresu")).doubleValue();
					String eco_imppresu = conv.convertiDouble(impeg_fittizio);
					//System.out.println("eco_imppresu == " + eco_imppresu);
					dbr.put("eco_imppresu", eco_imppresu);
				}
				
				//bargi 16/02/2012
				double disponibile=calc.subtract(budget,calc.sum(impeg_reale, impeg_fittizio));
				String str_disponibile = conv.convertiDouble(disponibile);
				//System.out.println("eco_imppresu == " + eco_imppresu);
				dbr.put("disponibile", str_disponibile);
				
				// Elisa 29/06/09: sostituito "eco_comune"
				/*if (dbr.get("eco_comune")!=null	&& !((String)dbr.get("eco_comune")).equals(""))
				{
					String w_codice  = (String)dbr.get("eco_comune");
					String w_select = "SELECT * FROM comuni WHERE codice='"+w_codice+"'";
					ISASRecord w_dbr=dbc.readRecord(w_select);
					dbr.put("eco_descri", w_dbr.get("descrizione"));
				}*/
				if (join==false && dbr.get("eco_centrocosto") != null	&& !((String)dbr.get("eco_centrocosto")).equals(""))
				{
					String descr_centrocosto ="";
					try {
					 descr_centrocosto = ISASUtil.getDecode(dbc, "rsa_centrocosto", "codice", 
											dbr.get("eco_centrocosto").toString(), "descrizione");
					}catch(Exception ecosto) {
					  System.out.println(nomeEJB+" ERRORE DI DECODIFICA CENTROCOSTO --->"+ecosto);	
					  descr_centrocosto=dbr.get("eco_centrocosto").toString()+": ERRORE DECODIFICA !";
					}
					dbr.put("desc_cc", descr_centrocosto);
				}
				else dbr.put("desc_cc", " ");
				
				// Elisa: "eco_socsan" non e' piu' presente nel conto economico, e' stato spostato 
				// 			nei contributi
				/*if (dbr.get("eco_socsan")!=null	&& !((String)dbr.get("eco_socsan")).equals(""))
				{
					String w_codice  = (String)dbr.get("eco_socsan");
					if(w_codice.equals("2"))
						dbr.put("eco_des_socsan", "Sociale");
					else if(w_codice.equals("1"))
						dbr.put("eco_des_socsan", "Sanitario");
				}
				else dbr.put("eco_des_socsan", "");*/
			
//G.Brogi 16/03/10				String da = dbr.get("eco_dareavere").toString();
//				if(da.equals("0"))	dbr.put("desc_dareavere", "Sociale");
//				else dbr.put("desc_dareavere", "Sanitario");
				String da = dbr.get("eco_socsan").toString();
				if(da.equals("0"))	dbr.put("desc_socsan", "Sociale");
				else dbr.put("desc_socsan", "Sanitario");
			}
			
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			
			return vdbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "Errore eseguendo una queryPaginate()  " + e);
			throw new SQLException(nomeEJB + "Errore eseguendo una queryPaginate()  ");
		}
		finally
		{
			if(!done)
			{
				try
				{
					if(dbcur!=null)
					dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1){System.out.println(nomeEJB + "Errore eseguendo una queryPaginate()  " + e1);}
			}
		}
	}
	
	/*private String convertiDouble(double d)
	{
		DecimalFormat df = new DecimalFormat("##,##0.00##");
		df.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.ITALIAN));
		
		return df.format(d);
	}*/

	public ISASRecord insert(myLogin mylogin,Hashtable h)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " insert - H: " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
	
		try
		{
			dbc = super.logIn(mylogin);
			
			ISASRecord dbr = dbc.newRecord("rsa_economici");
			Enumeration n = h.keys();
			while(n.hasMoreElements())
			{
				String e = n.nextElement().toString();
				dbr.put(e,h.get(e));
			}
			
			dbc.writeRecord(dbr);
			System.out.println(nomeEJB + " insert() - dopo write!");
		
			dbr = queryKey(dbc, h);
						
			dbc.close();
			super.close(dbc);
			done = true;
			
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + e);
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una insert() - "+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println("Errore eseguendo una insert() - " + e2);}
			}
		}
	}

	public ISASRecord update(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		System.out.println(nomeEJB + " update() - DBR = " + dbr.getHashtable().toString());
		
		boolean done = false;
		ISASConnection dbc = null;
			
		try
		{
			dbc = super.logIn(mylogin);
			dbc.writeRecord(dbr);
			dbr = queryKey(dbc, dbr.getHashtable());
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " Errore eseguendo una update() - " + e);
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " Errore eseguendo una update() - " + e);
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(e1);
			throw new SQLException(nomeEJB + " Errore eseguendo una update() - " +  e1);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2){System.out.println(nomeEJB + " Errore eseguendo una update() - " + e2);}
			}
		}
	}

	public void delete(myLogin mylogin,ISASRecord dbr)
	throws DBRecordChangedException, ISASPermissionDeniedException, SQLException 
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{
			dbc = super.logIn(mylogin);
			dbc.deleteRecord(dbr);
			dbc.close();
			super.close(dbc);
			done = true;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + " Errore eseguendo una delete() - " + e);
			throw e;
		}catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + " Errore eseguendo una delete() - " +e);
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(nomeEJB + " Errore eseguendo una delete() - " + e1);
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e2)
				{   System.out.println(nomeEJB + " Errore eseguendo una delete() - " + e2);   }
			}
		}
	}

	// Elisa 14/07/09: adesso quando vengono duplicati i conti, vengono duplicati anche i
	// contributi associati ad essi
	public ISASRecord copia_conti(myLogin mylogin,Hashtable h) throws  SQLException
	{
		System.out.println(nomeEJB + " copia_conti() -- H == " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		
		String annovecchio = "";
		String annonuovo = "";
		String sovrascrivi = "";
		ISASCursor dbcur=null;
		try
		{
			dbc = super.logIn(mylogin);
		
			sovrascrivi = h.get("sovrascrivi").toString();
			annovecchio = h.get("anno_vecchio").toString();
			annonuovo = h.get("anno_nuovo").toString();
			
			String myselect = "SELECT * FROM rsa_economici WHERE "+
							" eco_anno = '" + annovecchio + "' ";
			
			System.out.println(nomeEJB + " copia_conti() - Select vecchi conti:" + myselect);
			 dbcur = dbc.startCursor(myselect);
			
			while(dbcur.next())
			{
				dbr = dbcur.getRecord();
				System.out.println(nomeEJB + " conto economico: " + dbr.getHashtable().toString());
				
				/*Devo controllare se per quel codice, centro di costo ed anno
                      nuovo esiste gi� un conto economico, in quel caso non
                      devo inserire niente
				 */
				String selConto = "SELECT * from rsa_economici WHERE "+
								" eco_anno = '" + annonuovo + "' AND "+
								"eco_codice = '" + (String)dbr.get("eco_codice") + "' AND "+
								"eco_centrocosto = '" + (String)dbr.get("eco_centrocosto") + "' ";

				System.out.println(nomeEJB + " copia_conti() - Select nuovi conti: " + selConto);
				ISASRecord dbConti = dbc.readRecord(selConto);
				
				if(dbConti == null)
				{
					ISASRecord dbnew = dbc.newRecord("rsa_economici");
					
					dbnew.put("eco_anno",(String)h.get("anno_nuovo"));
					dbnew.put("eco_codice",(String)dbr.get("eco_codice"));
					dbnew.put("eco_centrocosto",(String)dbr.get("eco_centrocosto"));
					dbnew.put("eco_descri",(String)dbr.get("eco_descri"));
					dbnew.put("eco_previs","0");
					dbnew.put("eco_utiliz","0");
					dbnew.put("eco_impreale","0");
					dbnew.put("eco_imppresu","0");
					
					// Elisa - campo nuovo
//G.Brogi 16/03/10					dbnew.put("eco_dareavere", "0");
					//bargi 30/12/2010 dbnew.put("eco_socsan","0");
					dbnew.put("eco_socsan",dbr.get("eco_socsan"));		
					dbc.writeRecord(dbnew);
					copia_contributi(dbc, dbnew.getHashtable(), h.get("anno_vecchio").toString());
				}
				else if(sovrascrivi.equals("S"))
				{
					dbConti.put("eco_descri", (String)dbr.get("eco_descri"));
					dbConti.put("eco_budget", ""+(Double)dbr.get("eco_budget"));
					dbConti.put("eco_impegn", "0");
					dbConti.put("eco_liquid", "0");
						
					// Elisa - campo nuovo
// G.Brogi 16/03/10					dbConti.put("eco_dareavere", (String)dbr.get("eco_dareavere"));
					dbConti.put("eco_socsan", (String)dbr.get("eco_socsan"));
					
				
					dbc.writeRecord(dbConti);
					copia_contributi(dbc, dbConti.getHashtable(), h.get("anno_vecchio").toString());
				}
			}
			
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " Errore in copia_conti() - " + e);
			throw new SQLException(nomeEJB + "Errore eseguendo una copia_conti()  ");
		}
		finally
		{
			if(!done)
			{
				try
				{
					if(dbcur!=null)
						dbcur.close();
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e1)
				{   System.out.println(nomeEJB + " Errore in copia_conti() - " + e1);   }
			}
		}
	}
	
	private void copia_contributi(ISASConnection dbc, Hashtable h, String annoRif)throws  Exception
	{
		System.out.println(nomeEJB + " copia_contributi() - H == " + h.toString() + " anno rif == " + annoRif);
		ISASCursor dbcur=null;
		try
		{
			// prima elimino gli eventuali contributi del conto con l'anno nuovo 
			String myselect = " SELECT * FROM rsa_contributi WHERE " + 
						" eco_codice = '" + h.get("eco_codice").toString() + "' AND " +
						" eco_centrocosto = '" + h.get("eco_centrocosto").toString() + "' AND " +
						" eco_anno = '" + h.get("eco_anno").toString() + "' ";
			
			System.out.println(nomeEJB + " copia_contributi() 1 -- " + myselect);
			dbcur = dbc.startCursor(myselect);
			if(dbcur != null)
			{
				Vector v = dbcur.getAllRecord();
				if(v != null && v.size() > 0)
				{
					for(int i = 0; i < v.size(); i++)
					{
						ISASRecord dbr = (ISASRecord)v.get(i);
						String sel = " SELECT * FROM rsa_contributi WHERE " + 
						" eco_codice = '" + dbr.get("eco_codice").toString() + "' AND " +
						" eco_centrocosto = '" + dbr.get("eco_centrocosto").toString() + "' AND " +
						" eco_anno = '" + dbr.get("eco_anno").toString() + "' AND " +
						" con_codice = '" + dbr.get("con_codice").toString() + "' ";
						
						System.out.println(nomeEJB + " copia_contributi() (eliminazione) 2 -- " + sel);
						ISASRecord rec = dbc.readRecord(sel);
						dbc.deleteRecord(rec);
					}
				}
				
				dbcur.close();
			}
			dbcur=null;
			// prendo i contributi dell'anno vecchio e li copio in quello nuovo
			myselect = " SELECT * FROM rsa_contributi WHERE "+
						" eco_codice = '" + h.get("eco_codice").toString() + "' AND " +
						" eco_centrocosto = '" + h.get("eco_centrocosto").toString() + "' AND " +
						" eco_anno = '" + annoRif + "' ";
					
			System.out.println(nomeEJB + " copia_contributi() 3 -- " + myselect);
			dbcur = dbc.startCursor(myselect);
			if(dbcur != null)
			{
				Vector v = dbcur.getAllRecord();
				if(v != null && v.size() > 0)
				{
					for(int i = 0; i < v.size(); i++)
					{
						ISASRecord dbr = (ISASRecord)v.get(i);
						if(dbr != null)
						{
							dbr.put("eco_anno", h.get("eco_anno").toString());
							ISASRecord dbrnew = dbc.newRecord("rsa_contributi");
							Enumeration e = dbr.getHashtable().keys();
							while(e.hasMoreElements())
							{
								String chiave = e.nextElement().toString();
								dbrnew.put(chiave, dbr.get(chiave));
							}
									
							dbc.writeRecord(dbrnew);
						}								
					}
				}
					
				dbcur.close();
			}
		}
		catch(Exception e)
		{
			if(dbcur!=null)
				dbcur.close();
			System.out.println(nomeEJB + " ERRORE copia_contributi() " + e);
			throw new SQLException("Errore eseguendo una copia_contributi() ");
		}
	}
	
	/**
	 * 11/04/07
	 * Metodo invocato da JFRSAGridContImpegnati
	 * griglie di consultazione aperte da JFRSAConti
	 */
	public Vector queryPaginateCons(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " queryPaginateCons() - H == " + h.toString());

		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{
			dbc = super.logIn(mylogin);
			Vector vdbr = new Vector();

			String tp = (String)h.get("tp");
			
			if(tp.equals("liq"))
				// se il metodo e' invocato da JFRSAGridContLiquidati vado su rsa_liquidazioni
				vdbr = Paginate_Liquidazioni(dbc,h);
			else if(tp.equals("eff"))
				// se il metodo e' invocato da JFRSAGridContEffettivi vado su rsa_asstar
				vdbr = Paginate_Effettivi(dbc,h,true);
			else if(tp.equals("pre"))
				/** se il metodo e' invocato da JFRSAGridContPresunti vado su ricoveri,
				 *  devo andare a controllare se i campi relativi all'autorizzazione sono inseriti,
				 *  se non lo sono devo prendere quelli della commissione e se anche
				 *  questi sono vuoti devo prendere quelli della proposta
				 */
				vdbr = Paginate_Presunti(dbc,h,true);

			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE queryPaginateCons() -- " + e);
			throw new SQLException(nomeEJB + " ERRORE queryPaginateCons() -- " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{					
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{  System.out.println(nomeEJB + " ERRORE queryPaginateCons() -- " + e);   }
			}
		}
	}

	// impegnato reale al momento dell'assegnazione delle tariffe all'assistito all'interno di un ricovero
	private Vector Paginate_Liquidazioni(ISASConnection dbc,Hashtable h)throws Exception
	{
		try {
		/*
		myselect=" SELECT l.*, TRIM(cognome) cognome , TRIM(nome) nome, t.* " +
		" FROM rsa_liquidazioni l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t " +
		" WHERE l.n_cartella = ca.n_cartella AND l.n_cartella = r.n_cartella " +
		" AND l.liq_codist = r.ric_codist " +
		" AND l.liq_datric = r.ric_datric " +
		" AND l.liq_centrocosto = '" + centrocosto + "' " +
		" AND l.liq_economico = '" + conto + "' " +
		" AND l.liq_codtar = t.tar_codice ";
		
		if(socsan.equals("2")) myselect += " AND t.tar_sansoc =  '2' ";
		else if(socsan.equals("1")) myselect += " AND t.tar_sansoc = '1' ";
		
		if(tipo_anno.equals("I")) 
			myselect += " AND " + dbc.formatDbYear("l.liq_datiniliq") + " = '" + anno + "' ";
		else myselect += " AND " + dbc.formatDbYear("l.liq_datfinliq") + " = '" + anno + "' ";
		
		myselect += " ORDER BY ca.cognome, ca.nome ";
		*/
	    Vector v_liq=query_liquid(dbc,h);
	    debugMessage("rsa_liquidazioni: size="+v_liq.size());
		/*Vector v_fatt=query_fattattiva(dbc,h);
		 debugMessage("rsa_fat_ass size="+v_fatt.size());
		Vector v_fattfam=query_fattattivafam(dbc,h);
		 debugMessage("rsa_fat_fam size="+v_fattfam.size());
		mergeVett(v_liq,v_fatt);
		Vector vdbr=mergeVett(v_liq,v_fattfam);
		updDimVett(vdbr);*/
		return v_liq;
	}catch (Exception e)
		{
			System.out.println(nomeEJB + " ERRORE Paginate_Liquidazioni() -- MANCA CHIAVE PRIM. - " + e);
			throw e;
		}
	}
	 private Vector mergeVett(Vector v1,Vector v2){
	    	Enumeration en=v2.elements();
	    	while(en.hasMoreElements()){
	    		Object oj=en.nextElement();
	    		debugMessage("PAGINATE: INSTANCE OF : --->"+oj.getClass());
	    		if (oj instanceof Integer) {
	    			debugMessage("PAGINATE: INSTANCE OF INTEGER");
	    			oj=null;
	    		}else if  (oj instanceof Hashtable) {
	    			debugMessage("PAGINATE: INSTANCE OF HASH");
	    			Hashtable ht=(Hashtable)oj;
	    			debugMessage("PAGINATE: INSTANCE OF HASH "+ht.toString());
	    		}else v1.addElement(oj);
	    	}
	    	return v1;
	    }
	 private Vector updDimVett(Vector v2){
	    	Enumeration en=v2.elements();
	    	while(en.hasMoreElements()){
	    		Object oj=en.nextElement();
	    		if (oj instanceof Integer) {
	    			debugMessage("PAGINATE: DIMENSIONE INSTANCE OF INTEGER");
	    			v2.remove(oj);
	    		}
	    	}
	    	v2.addElement(new Integer (v2.size()));
	    	return v2;
	    }
private Vector query_liquid(ISASConnection dbc,Hashtable h)throws Exception
{
	String myselect = "";
	
	String anno = null;
	String centrocosto = null;
	String conto = null;
	String tipo_anno = null; // I == inizio, F == fine
	String socsan = null;   // tipo della tariffa sociale == 2, sanitaria == 1
	ISASCursor dbcur=null;
	try
	{
		anno = h.get("anno").toString();
		centrocosto = h.get("centrocosto").toString();
		conto = h.get("codice").toString();		
		tipo_anno = h.get("tipo_anno").toString(); 
		socsan = h.get("tipo_tariffa").toString(); 
	}
	catch (Exception e)
	{
		System.out.println(nomeEJB + " ERRORE Paginate_Effettivi() -- MANCA CHIAVE PRIM. - " + e);
		throw e;
	}
	try {
	myselect =// " SELECT l.*, TRIM(cognome) cognome , TRIM(nome) nome, t.* " +
		"  SELECT TRIM(cognome) cognome , TRIM(nome) nome,ca.n_cartella,"+
	"l.liq_contributo,t.tar_codice liq_codtar,l.liq_datcodtar,l.liq_quoass,l.liq_quocom,l.liq_quousl,l.liq_quofam, " +
	" l.liq_codist,'0' fata_quoflag,0 fatm_progfam " +
	" FROM rsa_liquidazioni l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t " +
	" WHERE l.n_cartella = ca.n_cartella AND l.n_cartella = r.n_cartella " +
	" AND l.liq_codist = r.ric_codist " +
	" AND l.liq_datric = r.ric_datric " +
	" AND l.liq_centrocosto = '" + centrocosto + "' " +
	" AND l.liq_economico = '" + conto + "' " +
	" AND l.liq_valida = 'S' " +
	" AND l.liq_codtar = t.tar_codice ";

	if(socsan.equals("2")) myselect += " AND t.tar_sansoc =  '2' ";
	else if(socsan.equals("1")) myselect += " AND t.tar_sansoc = '1' ";

	if(tipo_anno.equals("I")) 
		myselect += " AND " + dbc.formatDbYear("l.liq_datiniliq") + " = '" + anno + "' ";
	else myselect += " AND " + dbc.formatDbYear("l.liq_datfinliq") + " = '" + anno + "' ";

	myselect+=" UNION ";
	myselect +="  SELECT TRIM(cognome) cognome , TRIM(nome) nome,ca.n_cartella,"+
			"l.fata_contributo liq_contributo,t.tar_codice liq_codtar,l.fata_datcodtar liq_datcodtar," +
			"l.fata_quoint liq_quoass,l.fata_quoint liq_quocom,l.fata_quoint liq_quousl,l.fata_quoint liq_quofam, " +
	" l.fata_codist liq_codist ,fata_quoflag,0 fatm_progfam" +
	" FROM rsa_fat_ass l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t " +
	" WHERE l.n_cartella = ca.n_cartella AND l.n_cartella = r.n_cartella " +
	" AND l.fata_codist = r.ric_codist " +
	" AND l.fata_datric = r.ric_datric " +
	" AND l.fata_centrocosto = '" + centrocosto + "' " +
	" AND l.fata_economico = '" + conto + "' " +
	" AND l.fata_valida = 'S' " +
	" AND l.fata_codtar = t.tar_codice ";

	if(socsan.equals("2")) myselect += " AND t.tar_sansoc =  '2' ";
	else if(socsan.equals("1")) myselect += " AND t.tar_sansoc = '1' ";

	if(tipo_anno.equals("I")) 
		myselect += " AND " + dbc.formatDbYear("l.fata_datinifat") + " = '" + anno + "' ";
	else myselect += " AND " + dbc.formatDbYear("l.fata_datinifat") + " = '" + anno + "' ";

	
	
	
	myselect+=" UNION ";
	
	myselect +="  SELECT TRIM(cognome) cognome , TRIM(nome) nome,ca.n_cartella,"+
			"l.fatm_contributo liq_contributo,t.tar_codice liq_codtar,l.fatm_datcodtar liq_datcodtar," +
			" l.fatm_quoint liq_quoass,l.fatm_quoint liq_quocom,l.fatm_quoint liq_quousl,l.fatm_quoint liq_quofam, " +
	"  l.fatm_codist liq_codist ,'1' fata_quoflag,fatm_progfam" +
	" FROM rsa_fat_fam l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t " +
	" WHERE l.n_cartella = ca.n_cartella AND l.n_cartella = r.n_cartella " +
	" AND l.fatm_codist = r.ric_codist " +
	" AND l.fatm_datric = r.ric_datric " +
	" AND l.fatm_centrocosto = '" + centrocosto + "' " +
	" AND l.fatm_economico = '" + conto + "' " +
	" AND l.fatm_valida = 'S' " +
	" AND l.fatm_codtar = t.tar_codice ";

	if(socsan.equals("2")) myselect += " AND t.tar_sansoc =  '2' ";
	else if(socsan.equals("1")) myselect += " AND t.tar_sansoc = '1' ";

	if(tipo_anno.equals("I")) 
		myselect += " AND " + dbc.formatDbYear("l.fatm_datinifat") + " = '" + anno + "' ";
	else myselect += " AND " + dbc.formatDbYear("l.fatm_datinifat") + " = '" + anno + "' ";
	
	myselect += " ORDER BY 1,2 desc";         //ca.cognome, ca.nome ";;" +
			
	System.out.println(nomeEJB + " Paginate_Liquidazioni() -- sel == " + myselect);
	dbcur=dbc.startCursor(myselect);
	int start = Integer.parseInt((String)h.get("start"));
	int stop = Integer.parseInt((String)h.get("stop"));
	Vector vdbr = dbcur.paginate(start, stop);

	for(int i = 0; i < vdbr.size()-1; i++)
	{
		ISASRecord dbr = (ISASRecord)vdbr.elementAt(i);
		String tpc = (String)ISASUtil.getObjectField(dbr,"liq_codist",'S');
		String tipoges= ISASUtil.getDecode(dbc, "istituti", "ist_codice", tpc, "st_tipoges");
		String desc = ISASUtil.getDecode(dbc,"istituti","ist_codice",tpc,"st_nome");
		dbr.put("descr_istituto",desc);
		if(dbr.get("liq_contributo") != null && !dbr.get("liq_contributo").equals(""))
		{
			String selDescr = " SELECT con_descri FROM rsa_contributi WHERE eco_anno = '" + anno + "' " +
					" AND eco_codice = '" + conto + "' AND eco_centrocosto = '" + centrocosto + "' " +
					" AND con_codice = '" + dbr.get("liq_contributo").toString() + "' ";
			
			System.out.println(nomeEJB + " Paginate_Liquidazioni() - contrib -- " + selDescr);
			ISASRecord contr = dbc.readRecord(selDescr);
			if(contr != null)
				dbr.put("con_descri", contr.get("con_descri").toString());
		}	
		if(socsan.equals("1")) {//sanitario
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quoass" , "0");
			dbr.put("liq_quofam" , "0");
		}else {//sociale
		if(tipoges.equals("I")) {
			dbr.put("liq_quousl" , "0");
			dbr.put("liq_quoass" , "0");
			dbr.put("liq_quofam" , "0");
		}
		String flag=dbr.get("fata_quoflag").toString();
		if(flag.equals("A")) {
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quofam" , "0");
			dbr.put("liq_quousl" , "0");
		}
		if(flag.equals("F")) {//bargi 15/09/2010
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quoass" , "0");
			dbr.put("liq_quousl" , "0");
		}
		if(flag.equals("C")) {
			dbr.put("liq_quofam" , "0");
			dbr.put("liq_quousl" , "0");
			dbr.put("liq_quoass" , "0");
		}
		if(flag.equals("U")) {
			dbr.put("liq_quofam" , "0");
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quoass" , "0");
		}
		if(flag.equals("1")) {//familiare
			dbr.put("liq_quousl" , "0");
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quoass" , "0");
			dbr.put("liq_quofam", dbr.get("liq_quofam")+" (*)");
		}
		if(flag.equals("S")) {//SANITARIA
			dbr.put("liq_quofam" , "0");
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quoass" , "0");
		}
	}
	}
	dbcur.close();
	return vdbr;
	}catch (Exception e)
	{
		if(dbcur!=null)
			dbcur.close();
		System.out.println(nomeEJB + " ERRORE Paginate_Liquidazioni() -. - " + e);
		throw e;
	}
}

private Vector query_fattattiva(ISASConnection dbc,Hashtable h)throws Exception
{
	String myselect = "";
	
	String anno = null;
	String centrocosto = null;
	String conto = null;
	String tipo_anno = null; // I == inizio, F == fine
	String socsan = null;   // tipo della tariffa sociale == 2, sanitaria == 1
	ISASCursor dbcur=null;
	try
	{
		anno = h.get("anno").toString();
		centrocosto = h.get("centrocosto").toString();
		conto = h.get("codice").toString();		
		tipo_anno = h.get("tipo_anno").toString(); 
		socsan = h.get("tipo_tariffa").toString(); 
	}
	catch (Exception e)
	{
		System.out.println(nomeEJB + " ERRORE Paginate_Liquidazioni() -- MANCA CHIAVE PRIM. - " + e);
		throw e;
	}/*
		SELECT   l.*, TRIM (cognome) cognome, TRIM (nome) nome, t.*
	    FROM rsa_fat_ass l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t
	   WHERE l.n_cartella = ca.n_cartella
	     AND l.n_cartella = r.n_cartella
	     AND l.fata_codist = r.ric_codist
	     AND l.fata_datric = r.ric_datric
	    -- AND l.fata_centrocosto = '048014'
	    -- AND l.fata_economico = 'BARBARASOCQTUSL'
	     AND l.fata_codtar = t.tar_codice
	    AND t.tar_sansoc = '1'
	     AND TO_CHAR (l.fata_datinifat, 'YYYY') = '2010'
	ORDER BY ca.cognome, ca.nome
	*/
	try {
	myselect = " SELECT l.*, TRIM(cognome) cognome , TRIM(nome) nome, t.* " +
	" FROM rsa_fat_ass l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t " +
	" WHERE l.n_cartella = ca.n_cartella AND l.n_cartella = r.n_cartella " +
	" AND l.fata_codist = r.ric_codist " +
	" AND l.fata_datric = r.ric_datric " +
	" AND l.fata_centrocosto = '" + centrocosto + "' " +
	" AND l.fata_economico = '" + conto + "' " +
	" AND l.fata_codtar = t.tar_codice ";

	if(socsan.equals("2")) myselect += " AND t.tar_sansoc =  '2' ";
	else if(socsan.equals("1")) myselect += " AND t.tar_sansoc = '1' ";

	if(tipo_anno.equals("I")) 
		myselect += " AND " + dbc.formatDbYear("l.fata_datinifat") + " = '" + anno + "' ";
	else myselect += " AND " + dbc.formatDbYear("l.fata_datinifat") + " = '" + anno + "' ";

	myselect += " ORDER BY ca.cognome, ca.nome ";
	System.out.println(nomeEJB + " Paginate_Liquidazioni_fatt attiva() -- sel == " + myselect);
	dbcur=dbc.startCursor(myselect);
	int start = Integer.parseInt((String)h.get("start"));
	int stop = Integer.parseInt((String)h.get("stop"));
	Vector vdbr = dbcur.paginate(start, stop);

	for(int i = 0; i < vdbr.size()-1; i++)
	{
		ISASRecord dbr = (ISASRecord)vdbr.elementAt(i);
		String tpc = (String)ISASUtil.getObjectField(dbr,"fata_codist",'S');
		String desc = ISASUtil.getDecode(dbc,"istituti","ist_codice",tpc,"st_nome");
		dbr.put("descr_istituto",desc);
		String flag=dbr.get("fata_quoflag").toString();
		dbr.put("liq_quocom" , "0");
		dbr.put("liq_quoass" , "0");
		dbr.put("liq_quofam" , "0");
		dbr.put("liq_quousl","0");
		if(flag.equals("S")) {//sanitario
			dbr.put("liq_quocom" , "0");
			dbr.put("liq_quoass" , "0");
			dbr.put("liq_quofam" , "0");
			dbr.put("liq_quousl" , dbr.get("fata_quoint"));
		}else {//sociale
		if(flag.equals("A")) dbr.put("liq_quoass" , dbr.get("fata_quoint"));
		if(flag.equals("C")) dbr.put("liq_quocom" , dbr.get("fata_quoint"));
		if(flag.equals("U")) dbr.put("liq_quousl" , dbr.get("fata_quoint"));	
	}
	}
	dbcur.close();
	return vdbr;
	}catch (Exception e)
	{
		if(dbcur!=null)
			dbcur.close();
		System.out.println(nomeEJB + " ERRORE Paginate_Liquidazioni() -. - " + e);
		throw e;
	}
}

private Vector query_fattattivafam(ISASConnection dbc,Hashtable h)throws Exception
{
	String myselect = "";
	
	String anno = null;
	String centrocosto = null;
	String conto = null;
	String tipo_anno = null; // I == inizio, F == fine
	String socsan = null;   // tipo della tariffa sociale == 2, sanitaria == 1
	ISASCursor dbcur=null;
	try
	{
		anno = h.get("anno").toString();
		centrocosto = h.get("centrocosto").toString();
		conto = h.get("codice").toString();		
		tipo_anno = h.get("tipo_anno").toString(); 
		socsan = h.get("tipo_tariffa").toString(); 
	}
	catch (Exception e)
	{
		System.out.println(nomeEJB + " ERRORE Paginate_Liquidazioni() -- MANCA CHIAVE PRIM. - " + e);
		throw e;
	}
	try {
	myselect = " SELECT l.*, TRIM(cognome) cognome , TRIM(nome) nome, t.* " +
	" FROM rsa_fat_fam l, cartella ca, rsa_ricoveri r, rsa_tipo_tariffa t " +
	" WHERE l.n_cartella = ca.n_cartella AND l.n_cartella = r.n_cartella " +
	" AND l.fatm_codist = r.ric_codist " +
	" AND l.fatm_datric = r.ric_datric " +
	" AND l.fatm_centrocosto = '" + centrocosto + "' " +
	" AND l.fatm_economico = '" + conto + "' " +
	" AND l.fatm_codtar = t.tar_codice ";

	if(socsan.equals("2")) myselect += " AND t.tar_sansoc =  '2' ";
	else if(socsan.equals("1")) myselect += " AND t.tar_sansoc = '1' ";

	if(tipo_anno.equals("I")) 
		myselect += " AND " + dbc.formatDbYear("l.fatm_datinifat") + " = '" + anno + "' ";
	else myselect += " AND " + dbc.formatDbYear("l.fatm_datinifat") + " = '" + anno + "' ";

	myselect += " ORDER BY ca.cognome, ca.nome ";
	System.out.println(nomeEJB + " Paginate_Liquidazioni_fatt attiva() -- sel == " + myselect);
	dbcur=dbc.startCursor(myselect);
	int start = Integer.parseInt((String)h.get("start"));
	int stop = Integer.parseInt((String)h.get("stop"));
	Vector vdbr = dbcur.paginate(start, stop);

	for(int i = 0; i < vdbr.size()-1; i++)
	{
		ISASRecord dbr = (ISASRecord)vdbr.elementAt(i);
		String tpc = (String)ISASUtil.getObjectField(dbr,"fatm_codist",'S');
		String desc = ISASUtil.getDecode(dbc,"istituti","ist_codice",tpc,"st_nome");
		dbr.put("descr_istituto",desc);		
		dbr.put("liq_quocom" , "0");
		dbr.put("liq_quoass" , "0");
		dbr.put("liq_quofam" , dbr.get("fatm_quoint"));
		dbr.put("liq_quousl","0");		
	}
	dbcur.close();
	return vdbr;
	}catch (Exception e)
	{
		if(dbcur!=null)
			dbcur.close();
		System.out.println(nomeEJB + " ERRORE Paginate_Liquidazioni() -. - " + e);
		throw e;
	}
}
	private Vector Paginate_Effettivi(ISASConnection dbc,Hashtable h,boolean paginate)throws Exception
	{
		String myselect = "";
		ISASCursor dbcur=null;
		String anno = null;
		String centrocosto = null;
		String conto = null;
		String tipo_anno = null; // I == inizio, F == fine
		String socsan = null;   // tipo della tariffa sociale == 2, sanitaria == 1
	
		try
		{
			anno = h.get("anno").toString();
			centrocosto = h.get("centrocosto").toString();
			conto = h.get("codice").toString();		
			tipo_anno = h.get("tipo_anno").toString(); 
			socsan = h.get("tipo_tariffa").toString(); 
		}
		catch (Exception e)
		{
			System.out.println(nomeEJB + " ERRORE Paginate_Effettivi() -- MANCA CHIAVE PRIM. - " + e);
			throw e;
		}
		try {
	/*
		myselect = " SELECT a.*, TRIM(ca.cognome) cognome , TRIM(ca.nome) nome, t.*, tt.* " +
		" FROM rsa_asstar a, cartella ca, rsa_ricoveri r, rsa_tariffe t, rsa_tipo_tariffa tt " +
		" WHERE a.ass_codist = r.ric_codist " +
		" AND a.n_cartella = r.n_cartella " +
		" AND a.ass_datric = r.ric_datric " +
		" AND a.n_cartella = ca.n_cartella " +
		" AND a.ass_codtar = t.tar_codice AND a.ass_dattar = t.tar_datini " +
		" AND a.ass_codist = t.tar_codist " +
		" AND t.tar_codice = tt.tar_codice " +
		" AND a.eco_codice = '" + conto + "' AND a.eco_centrocosto = '" + centrocosto + "' ";
		
		if(socsan.equals("2")) myselect += " AND tt.tar_sansoc = '2' ";
		else if(socsan.equals("1")) myselect += " AND tt.tar_sansoc = '1' ";

		if(tipo_anno.equals("I")) 
			myselect += " AND " + dbc.formatDbYear("a.ass_datint") + " = '" + anno + "' ";
		else myselect += " AND " + dbc.formatDbYear("a.ass_datfit") + " = '" + anno + "' ";
		
		myselect += " ORDER BY ca.cognome, ca.nome";
		*/
		myselect = " SELECT a.*, TRIM(ca.cognome) cognome , TRIM(ca.nome) nome, t.*, tt.* " +
		" FROM rsa_asstar a, cartella ca, rsa_ricoveri r, rsa_tariffe t, rsa_tipo_tariffa tt " +
		" WHERE a.ass_codist = r.ric_codist " +
		" AND a.n_cartella = r.n_cartella " +
		" AND a.ass_datric = r.ric_datric " +
		" AND a.n_cartella = ca.n_cartella " +
		" AND a.ass_codtar = t.tar_codice AND a.ass_dattar = t.tar_datini " +
		" AND a.ass_codist = t.tar_codist " +
		" AND t.tar_codice = tt.tar_codice " +
		" AND a.eco_centrocosto = '" + centrocosto + "' ";

		if(socsan.equals("2")) {
			myselect +=" AND (a.eco_codice = '" + conto + "'  OR a.eco_codice_soc_usl = '" + conto + "' )"   ;
			myselect += " AND tt.tar_sansoc = '2' ";
		}
		else if(socsan.equals("1")) {
			myselect +=" AND a.eco_codice = '" + conto + "' " ;
			myselect += " AND tt.tar_sansoc = '1' ";
		}
/*
		if(tipo_anno.equals("I")) 
			myselect += " AND " + dbc.formatDbYear("a.ass_datint") + " = '" + anno + "' ";
		else myselect += " AND " + dbc.formatDbYear("a.ass_datfit") + " = '" + anno + "' ";
*/
		  if(tipo_anno.equals("I"))
			  myselect+= " AND " + dbc.formatDbYear("a.ass_datint") + " = '" + anno + "' ";
            else if(tipo_anno.equals("F"))
            	myselect += " AND " + dbc.formatDbYear("a.ass_datfit") + " = '" + anno + "' ";
            else {//bargi 21/01/2011 nell'anno
            	myselect += " AND " + dbc.formatDbYear("a.ass_datint") + " <= '" + anno + "' ";
            	myselect += " AND (a.ass_datfit is null or  " + dbc.formatDbYear("a.ass_datfit") + " >= '" + anno + "') ";
            }
		
		
		myselect += " ORDER BY ca.cognome, ca.nome";
		System.out.println(nomeEJB + " Paginate_Effettivi() -- " + myselect);
		dbcur=dbc.startCursor(myselect);
		Vector vdbr=null;
		int indice=0;
		if(paginate) {
		int start = Integer.parseInt((String)h.get("start"));
		int stop = Integer.parseInt((String)h.get("stop"));
		  vdbr = dbcur.paginate(start, stop);
		  indice=1;
		}else {
		 vdbr=dbcur.getAllRecord();
		 indice=0;
		}
		double totale=0;
		for(int i = 0; i < vdbr.size()-indice; i++)
		{
			ISASRecord dbr = (ISASRecord)vdbr.elementAt(i);

			String tpc = (String)ISASUtil.getObjectField(dbr,"ass_codist",'S');
			String desc = ISASUtil.getDecode(dbc,"istituti","ist_codice",tpc,"st_nome");
			dbr.put("descr_istituto",desc);
			
			if(dbr.get("ass_codcon") != null && !dbr.get("ass_codcon").equals(""))
			{
				String selDescr = " SELECT con_descri FROM rsa_contributi WHERE eco_anno = '" + anno + "' " +
						" AND eco_codice = '" + conto + "' AND eco_centrocosto = '" + centrocosto + "' " +
						" AND con_codice = '" + dbr.get("ass_codcon").toString() + "' ";
				
				System.out.println(nomeEJB + " Paginate_Effettivi() - contrib -- " + selDescr);
				ISASRecord contr = dbc.readRecord(selDescr);
				if(contr != null)
					dbr.put("con_descri", contr.get("con_descri").toString());
			}			
			String tipoges= ISASUtil.getDecode(dbc, "istituti", "ist_codice", (String) ISASUtil.getObjectField(
					dbr, "ass_codist", 'S'), "st_tipoges");
			String conto_usl="";
			String conto_soc=dbr.get("eco_codice").toString();
		
			if(tipoges.equals("D")) {
				if(dbr.get("eco_codice_soc_usl")!=null ) {
					conto_usl=dbr.get("eco_codice_soc_usl").toString();
				}
			}
			if(socsan.equals("2")) {//sociale
				if(tipoges.equals("I")) {
					dbr.put("ass_quocom",       (String)ISASUtil.getObjectField(dbr,"ass_quocom",'D'));
					dbr.put("ass_quousl",     "0");
					dbr.put("ass_quoass" , "0");
					dbr.put("ass_quofam" , "0");
				}
				else {
					if(!conto_usl.equals("")&& conto_usl.equals(conto)) {
						dbr.put("ass_quousl",       (String)ISASUtil.getObjectField(dbr,"ass_quousl",'D'));
						dbr.put("ass_quocom",       (String)ISASUtil.getObjectField(dbr,"ass_quocom",'D'));
					}else {
						dbr.put("ass_quousl",     "0");
						dbr.put("ass_quocom" , "0");
					}
					if(!conto_soc.equals("")&& conto_soc.equals(conto)) {
						dbr.put("ass_quoass",       (String)ISASUtil.getObjectField(dbr,"ass_quoass",'D'));		
						dbr.put("ass_quofam",       (String)ISASUtil.getObjectField(dbr,"ass_quofam",'D'));
					}else {
						dbr.put("ass_quoass" , "0");
						dbr.put("ass_quofam" , "0");
					}
				}
			}//fine sociale
			else if(socsan.equals("1")) {//sanitario
				dbr.put("ass_quousl",       (String)ISASUtil.getObjectField(dbr,"ass_quousl",'D'));
				dbr.put("ass_quoass" , "0");
				dbr.put("ass_quofam" , "0");
				dbr.put("ass_quocom" , "0");
			}
			//bargi 04/02/2011
			 if(tipo_anno.equals("C")) { // anno compreso
				 Hashtable hR=dbr.getHashtable();
				 String datfi="";
				 if(dbr.get("ass_datfit")!=null)datfi=dbr.get("ass_datfit").toString();
				  simulaCalcoloEff(dbc,hR,dbr.get("ass_datint").toString(),datfi);
				  System.out.println("simulaCalcoloEff Impegnato "+hR.toString());
				  if(hR.get("importo_calcolato_"+anno)!=null) {
					  dbr.put("importo_calcolato_"+anno, hR.get("importo_calcolato_"+anno));
					  totale=totale+ ((Double)dbr.get("importo_calcolato_"+anno)).doubleValue();
				  }else  dbr.put("importo_calcolato_"+anno,new Double(0));
				  NumberDateFormat ndf=new NumberDateFormat();
				  if(hR.get("data_fine")!=null) {
					  String datafine=ndf.formDate(hR.get("data_fine").toString(), "gg/mm/aaaa");
					  dbr.put("data_fine",datafine);
				  }
			 }
			
		}//end for
		debugMessage("TOTALE EFFETTIVI "+totale);
		h.put("totale_impegnato", new Double(totale));
		dbcur.close();
		return vdbr;
	}catch (Exception e)
	{
		if(dbcur!=null)
			dbcur.close();
		System.out.println(nomeEJB + " ERRORE Paginate_Effettivi() -- . - " + e);
		throw e;
	}
	}

	// sostituzione del comune con il centro di costo
	private Vector Paginate_Presunti(ISASConnection dbc,Hashtable h,boolean paginate)throws Exception
	{
		System.out.println(nomeEJB + " Paginate_Presunti() -- H == " + h.toString());
	
		String myselect = "";
		String mysel = "";
		ISASCursor dbcur=null;
		String anno = null;
		String centrocosto = null;
		String conto = null;
		String tipo_anno = null; // I == inizio, F == fine
		String socsan = null;   // tipo della tariffa sociale == 2, sanitaria == 1
		String tipoconto = null; // se dare == 0, avere == 1
		
		try
		{
			anno = h.get("anno").toString();
			centrocosto = h.get("centrocosto").toString();
			conto = h.get("codice").toString();		
			tipo_anno = h.get("tipo_anno").toString(); 
			socsan = h.get("tipo_tariffa").toString(); 
//G.Brogi 16/03/10			tipoconto = h.get("eco_dareavere").toString();
			tipoconto = h.get("eco_socsan").toString();
			
	/*		mysel = " SELECT o.*,TRIM(ca.cognome) cognome ,TRIM(ca.nome) nome "+
					" FROM ass_ricoveri o, cartella ca WHERE o.cod_conto = '" + conto + "'  " +
					" AND o.n_cartella = ca.n_cartella  AND (   o.data_ingresso IS NULL " +
					"	OR (o.data_ingresso IS NOT NULL AND o.flag_rinuncia <> 2))  " +
					" AND (   (o.au_data_auto IS NOT NULL AND o.au_parere <> 2) " + " OR " + 
					"         (o.au_data_auto IS NULL AND o.co_data_comm IS NOT NULL " +
					" 			AND o.co_parere <> 2   ) " + " OR " +
					"         (o.au_data_auto IS NULL AND o.co_data_comm IS NULL " +
					"		    AND o.as_data_prop IS NOT NULL) " + 
					"      ) ";
			
			if(tipoconto.equals("0"))
				myselect = myselect + " AND o.cod_centrocosto_avere = '" + centrocosto + "' ";
			else  myselect = myselect + " AND o.cod_centrocosto = '" + centrocosto + "' ";*/
			mysel = " SELECT o.*,TRIM(ca.cognome) cognome ,TRIM(ca.nome) nome "+
			" FROM ass_ricoveri o, cartella ca WHERE "+
			"  o.n_cartella = ca.n_cartella "+
			" AND (  o.flag_livello='1' " +
			"     OR     ( o.flag_livello='2'  AND o.co_parere <> '2') " + 
			"    OR     (o.flag_livello='3'  and  o.au_parere<>'2' ) " +
			"      )  " +
			" AND ( o.data_ingresso IS NULL and  o.flag_rinuncia<> '2' )" ;
			if(socsan.equals("2"))//sociale
				myselect = myselect +" AND (o.cod_conto_avere = '" + conto + "'  OR o.cod_conto_soc_usl = '" + conto + "' ) " + " AND o.cod_centrocosto_avere = '" + centrocosto + "' ";
			else  myselect = myselect+" AND o.cod_conto= '" + conto + "'  "  + " AND o.cod_centrocosto= '" + centrocosto + "' ";

			if(tipo_anno.equals("I")) // inizio anno
			{
				if(socsan.equals("2")) // sociale
				{
					myselect +=// " AND c.con_codice = o.cod_contribso AND " +
						"  AND (o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +  
						" = '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" = '" + anno + "') OR " +
						"(o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " = '" + anno + "' )) ";
				}
				else if(socsan.equals("1")) // sanitaria
				{
					myselect += //" AND c.con_codice = o.cod_contribsa AND " +
						"  AND (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +
						" = '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND "+
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" = '" + anno + "') OR " +
						"(o.au_codtarsa IS NULL AND o.co_codtarsa IS NULL AND o.as_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " = '" + anno + "' )) ";
				}
			}
			else if(tipo_anno.equals("F")) // fine anno
			{
				if(socsan.equals("2")) // sociale
				{
					myselect += //" AND c.con_codice = o.cod_contribso AND " +
						" AND (o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
						" = '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_fin") + 
						" = '" + anno + "' ) OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_fin") + " = '" + anno + "' )) ";
				}
				else if(socsan.equals("1"))
				{
					myselect += //" AND c.con_codice = o.cod_contribsa AND " +
						"  AND (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
						" = '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_fin") + 
						" = '" + anno + "') OR " +
						" (o.au_codtarsa IS NULL AND co_codtarsa IS NULL AND co_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_fin") + " = '" + anno + "' )) ";
				}
			}else {//bargi 28/01/2011
				if(socsan.equals("2")) // sociale
				{
					myselect += //" AND c.con_codice = o.cod_contribso AND " +
						" AND (o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
						" >= '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_fin") + 
						" >= '" + anno + "' ) OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_fin") + " >= '" + anno + "' )) ";
					myselect +=
						"  AND (o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +  
						" <= '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" <= '" + anno + "') OR " +
						"(o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " <= '" + anno + "' )) ";
				}
				else if(socsan.equals("1"))
				{
					myselect += //" AND c.con_codice = o.cod_contribsa AND " +
						"  AND (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
						" >= '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_fin") + 
						"> = '" + anno + "') OR " +
						" (o.au_codtarsa IS NULL AND co_codtarsa IS NULL AND co_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_fin") + " >= '" + anno + "' )) ";
					myselect += //" AND c.con_codice = o.cod_contribsa AND " +
						"  AND (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +
						" <= '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND "+
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" <= '" + anno + "') OR " +
						"(o.au_codtarsa IS NULL AND o.co_codtarsa IS NULL AND o.as_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " <= '" + anno + "' )) ";
				}
				
				
				
			}

			mysel = mysel + myselect + " ORDER BY ca.cognome, ca.nome";
			
		/*}
		catch (Exception e)
		{
			System.out.println(nomeEJB + " ERRORE Paginate_Presunti() - MANCA CHIAVE PRIM. -  " + e);
			throw e;
		}

		try
		{
			if(anno != null && !anno.equals(""))
			{
				if(tipo_anno.equals("I")) // inizio anno
				{
					if(socsan.equals("2")) // sociale
					{
						myselect += " AND c.con_codice = o.cod_contribso AND " +
						" (o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +  
						" = '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" = '" + anno + "') OR " +
						"(o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " = '" + anno + "' )) ";
					}
					else if(socsan.equals("1")) // sanitaria
					{
						myselect += " AND c.con_codice = o.cod_contribsa AND " +
						" (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +
						" = '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND "+
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" = '" + anno + "') OR " +
						"(o.au_codtarsa IS NULL AND o.co_codtarsa IS NULL AND o.as_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " = '" + anno + "' )) ";
					}
					else
					{
						myselect += 
						"AND ((o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +  
						" = '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" = '" + anno + "') OR " +
						"(o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " = '" + anno + "' )) " +
						" OR " +
						" (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_ini") +
						" = '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND "+
						dbc.formatDbYear("o.co_data_comm_ini") + 
						" = '" + anno + "') OR " +
						"(o.au_codtarsa IS NULL AND o.co_codtarsa IS NULL AND o.as_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_ini") + " = '" + anno + "' ))) ";
					}
				}
				else
				{
					if(socsan.equals("2")) // sociale
					{
						myselect += " AND c.con_codice = o.cod_contribso AND " +
						" (o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
						" = '" + anno + "' OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_fin") + 
						" = '" + anno + "' ) OR " +
						" (o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_fin") + " = '" + anno + "' )) ";
					}
					else if(socsan.equals("1"))
					{
						myselect += " AND c.con_codice = o.cod_contribsa AND " +
						" (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
						" = '" + anno + "' OR " +
						" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.co_data_comm_fin") + 
						" = '" + anno + "') OR " +
						" (o.au_codtarsa IS NULL AND co_codtarsa IS NULL AND co_codtarsa IS NOT NULL AND " +
						dbc.formatDbYear("o.as_data_prop_fin") + " = '" + anno + "' )) ";
					}
					else
					{
						myselect += 
							"AND ((o.au_codtarso IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
							" = '" + anno + "' OR " +
							" (o.au_codtarso IS NULL AND o.co_codtarso IS NOT NULL AND " +
							dbc.formatDbYear("o.co_data_comm_fin") + 
							" = '" + anno + "' ) OR " +
							" (o.au_codtarso IS NULL AND o.co_codtarso IS NULL AND o.as_codtarso IS NOT NULL AND " +
							dbc.formatDbYear("o.as_data_prop_fin") + " = '" + anno + "' )) " +
							" OR " +
							" (o.au_codtarsa IS NOT NULL AND " + dbc.formatDbYear("o.au_data_auto_fin") +
							" = '" + anno + "' OR " +
							" (o.au_codtarsa IS NULL AND o.co_codtarsa IS NOT NULL AND " +
							dbc.formatDbYear("o.co_data_comm_fin") + 		
							" = '" + anno + "') OR " +
							" (o.au_codtarsa IS NULL AND co_codtarsa IS NULL AND co_codtarsa IS NOT NULL AND " +
							dbc.formatDbYear("o.as_data_prop_fin") + " = '" + anno + "' )) )";
					}
				}
			}
		}
		catch(Exception e)
		{   System.out.println(nomeEJB + " ERRORE Paginate_Presunti() - anno - " + e);   }
		
		mysel = mysel + myselect + " ORDER BY ca.cognome, ca.nome";
		*/
		System.out.println(nomeEJB + " Paginate_Presunti() -- select == " + mysel);
		dbcur = dbc.startCursor(mysel);			
			Vector vdbr=null;
			int indice=0;
			if(paginate) {
			int start = Integer.parseInt((String)h.get("start"));
			int stop = Integer.parseInt((String)h.get("stop"));
			  vdbr = dbcur.paginate(start, stop);
			  indice=1;
			}else {
			 vdbr=dbcur.getAllRecord();
			 indice=0;
			}
			double totale=0;
			for(int i = 0; i < vdbr.size()-indice; i++)
			{
			ISASRecord dbr = (ISASRecord)vdbr.elementAt(i);
			String tpc = (String)ISASUtil.getObjectField(dbr,"cod_istitu",'S');
			String desc = ISASUtil.getDecode(dbc,"istituti","ist_codice",tpc,"st_nome");
			dbr.put("descr_istituto",desc);
			String tipoges= ISASUtil.getDecode(dbc, "istituti", "ist_codice", (String) ISASUtil.getObjectField(
					dbr, "cod_istitu", 'S'), "st_tipoges");
			String codtar = null;
			String conto_soc="";
			String conto_usl="";
				if(dbr.get("cod_conto_avere")!=null ) 
					 conto_soc=dbr.get("cod_conto_avere").toString();
			if(tipoges.equals("D")) {
				if(dbr.get("cod_conto_soc_usl")!=null ) {
					conto_usl=dbr.get("cod_conto_soc_usl").toString();
				}
			}
			// sociale
			String pref="as_";
			if(socsan == null || socsan.equals("T") || socsan.equals("2"))
			{
				if(dbr.get("au_codtarso") != null && !((String)dbr.get("au_codtarso")).equals(""))
				{
					System.out.println(nomeEJB + " Paginate_Presunti() - autorizzazione");
					
					codtar = (String)ISASUtil.getObjectField(dbr,"au_codtarso",'S');
					dbr.put("codtarsoc",  codtar);
					dbr.put("tar_descri_soc", ISASUtil.getDecode(dbc, "rsa_tipo_tariffa", "tar_codice", codtar,
							"tar_descri"));
					dbr.put("data_inizio",  (String)ISASUtil.getObjectField(dbr,"au_data_auto_ini",'T'));
					dbr.put("data_fine",    (String)ISASUtil.getObjectField(dbr,"au_data_auto_fin",'T'));
					pref="au_";
					if(tipoges.equals("I"))dbr.put("quocom",       (String)ISASUtil.getObjectField(dbr,"au_importosocom",'D'));
					else {
						if(!conto_usl.equals("")&& conto_usl.equals(conto)) {
							dbr.put("quousl",       (String)ISASUtil.getObjectField(dbr,"au_importosousl",'D'));
							dbr.put("quocom",       (String)ISASUtil.getObjectField(dbr,"au_importosocom",'D'));
						}
						if(!conto_soc.equals("")&& conto_soc.equals(conto)) {
							dbr.put("quoass",       (String)ISASUtil.getObjectField(dbr,"au_importosoass",'D'));		
							dbr.put("quofam",       (String)ISASUtil.getObjectField(dbr,"au_importosofam",'D'));
						}					
					}
				}
				else if(dbr.get("co_codtarso") != null && !((String)dbr.get("co_codtarso")).equals(""))
				{
					System.out.println(nomeEJB + " Paginate_Presunti() - commissione");
						
					codtar = (String)ISASUtil.getObjectField(dbr,"co_codtarso",'S');
					dbr.put("codtarsoc",  codtar);
					dbr.put("tar_descri_soc", ISASUtil.getDecode(dbc, "rsa_tipo_tariffa", "tar_codice", codtar,
							"tar_descri"));
					dbr.put("data_inizio",  (String)ISASUtil.getObjectField(dbr,"co_data_comm_ini",'T'));
					dbr.put("data_fine",    (String)ISASUtil.getObjectField(dbr,"co_data_comm_fin",'T'));
					pref="co_";
					if(tipoges.equals("I"))dbr.put("quocom",       (String)ISASUtil.getObjectField(dbr,"co_importosocom",'D'));
					else {
						if(!conto_usl.equals("")&& conto_usl.equals(conto)) {
							dbr.put("quousl",       (String)ISASUtil.getObjectField(dbr,"co_importosousl",'D'));
							dbr.put("quocom",       (String)ISASUtil.getObjectField(dbr,"co_importosocom",'D'));
						}
						if(!conto_soc.equals("")&& conto_soc.equals(conto)) {
							dbr.put("quoass",       (String)ISASUtil.getObjectField(dbr,"co_importosoass",'D'));		
							dbr.put("quofam",       (String)ISASUtil.getObjectField(dbr,"co_importosofam",'D'));
						}					
					}
					
				}
				else if(dbr.get("as_codtarso") != null && !((String)dbr.get("as_codtarso")).equals(""))
				{
					System.out.println(nomeEJB + " Paginate_Presunti() - proposta");
					
					codtar = (String)ISASUtil.getObjectField(dbr,"as_codtarso",'S');
					dbr.put("codtarsoc",  codtar);
					dbr.put("tar_descri_soc", ISASUtil.getDecode(dbc, "rsa_tipo_tariffa", "tar_codice", codtar,
							"tar_descri"));
					
					dbr.put("data_inizio",  (String)ISASUtil.getObjectField(dbr,"as_data_prop_ini",'T'));
					dbr.put("data_fine",    (String)ISASUtil.getObjectField(dbr,"as_data_prop_fin",'T'));
					pref="as_";
					if(tipoges.equals("I"))dbr.put("quocom",       (String)ISASUtil.getObjectField(dbr,"as_importosocom",'D'));
					else {
						if(!conto_usl.equals("")&& conto_usl.equals(conto)) {
							dbr.put("quousl",       (String)ISASUtil.getObjectField(dbr,"as_importosousl",'D'));
							dbr.put("quocom",       (String)ISASUtil.getObjectField(dbr,"as_importosocom",'D'));
						}
						if(!conto_soc.equals("")&& conto_soc.equals(conto)) {
							dbr.put("quoass",       (String)ISASUtil.getObjectField(dbr,"as_importosoass",'D'));		
							dbr.put("quofam",       (String)ISASUtil.getObjectField(dbr,"as_importosofam",'D'));
						}					
					}
				}
			}
			
			// sanitario
			if(socsan == null || socsan.equals("T") || socsan.equals("1"))
			{
				if(dbr.get("au_codtarsa") != null && !((String)dbr.get("au_codtarsa")).equals(""))
				{
					System.out.println(nomeEJB + " Paginate_Presunti() - autorizzazione");
					
					codtar = (String)ISASUtil.getObjectField(dbr,"au_codtarsa",'S');
					dbr.put("codtarsan",  codtar);
					dbr.put("tar_descri_san", ISASUtil.getDecode(dbc, "rsa_tipo_tariffa", "tar_codice", codtar,
							"tar_descri"));
					dbr.put("data_inizio",  (String)ISASUtil.getObjectField(dbr,"au_data_auto_ini",'T'));
					dbr.put("data_fine",    (String)ISASUtil.getObjectField(dbr,"au_data_auto_fin",'T'));
					dbr.put("importosa",       (String)ISASUtil.getObjectField(dbr,"au_importosa",'D'));
					pref="au_";
				}
				else if(dbr.get("co_codtarsa") != null && !((String)dbr.get("co_codtarsa")).equals(""))
				{
					System.out.println(nomeEJB + " Paginate_Presunti() - commissione");
						
					codtar = (String)ISASUtil.getObjectField(dbr,"co_codtarsa",'S');
					dbr.put("codtarsan",  codtar);
					dbr.put("tar_descri_san", ISASUtil.getDecode(dbc, "rsa_tipo_tariffa", "tar_codice", codtar,
							"tar_descri"));
					dbr.put("data_inizio",  (String)ISASUtil.getObjectField(dbr,"co_data_comm_ini",'T'));
					dbr.put("data_fine",    (String)ISASUtil.getObjectField(dbr,"co_data_comm_fin",'T'));
					dbr.put("importosa",       (String)ISASUtil.getObjectField(dbr,"co_importosa",'D'));
					pref="co_";
				}
				else if(dbr.get("as_codtarsa") != null && !((String)dbr.get("as_codtarsa")).equals(""))
				{
					System.out.println(nomeEJB + " Paginate_Presunti() - proposta");
					
					codtar = (String)ISASUtil.getObjectField(dbr,"as_codtarsa",'S');
					dbr.put("codtarsan",  codtar);
					dbr.put("tar_descri_san", ISASUtil.getDecode(dbc, "rsa_tipo_tariffa", "tar_codice", codtar,
							"tar_descri"));
					
					dbr.put("data_inizio",  (String)ISASUtil.getObjectField(dbr,"as_data_prop_ini",'T'));
					dbr.put("data_fine",    (String)ISASUtil.getObjectField(dbr,"as_data_prop_fin",'T'));
					dbr.put("importosa",       (String)ISASUtil.getObjectField(dbr,"as_importosa",'D'));	
					pref="as_";
				}
			}
			
			//bargi 04/02/2011
			 if(tipo_anno.equals("C")) { // anno compreso
				   simulaCalcolo(dbc,socsan,pref,dbr);
				   if(dbr.get("importo_calcolato_"+anno)!=null)
				   totale=totale+ ((Double)dbr.get("importo_calcolato_"+anno)).doubleValue();
				   else  dbr.put("importo_calcolato_"+anno,new Double(0));
				  System.out.println("simulaCalcoloPresuntoImpegnato "+dbr.getHashtable().toString());
			 }
			if(dbr.get("au_data_auto") != null && !dbr.get("au_data_auto").equals(""))
				dbr.put("fase", "Autorizz.");
			else if(dbr.get("co_data_comm") != null && !dbr.get("co_data_comm").equals(""))
				dbr.put("fase", "Commiss.");
			else dbr.put("fase", "Proposta");
			
			System.out.println(nomeEJB + " Paginate_Presunti() -- DBR == " + dbr.getHashtable().toString());
		}//end for
		h.put("totale_impegnato", new Double(totale));
		debugMessage("TOTALE PRESUNTI "+totale);
		dbcur.close();
		return vdbr;
		}catch (Exception e)
		{
			if(dbcur!=null)dbcur.close();
			System.out.println(nomeEJB + " ERRORE Paginate_Presunti() - MANCA CHIAVE PRIM. -  " + e);
			throw e;
		}
	}// fine Paginate_Presunti()
	routineRsa rtRsa = new routineRsa();
	private void simulaCalcolo(ISASConnection dbc,String socsan,String pref,ISASRecord dbr) throws Exception
		{
		try {
			
		 rtRsa.aggiornaCalcoliRicoveri(dbc, dbr, +1, pref,"I",true);		
		}catch (Exception e)
			{
				System.out.println(nomeEJB + " ERRORE simulaCalcolo() -  -  " + e);
				throw e;
			}
	}
	private void simulaCalcoloEff(ISASConnection dbc, Hashtable hRic, String dataInizio,
			String dataFine ) throws Exception	
	{
		   try {
			   if (dataFine==null || dataFine.length() != 10)
				   dataFine=getDataDim(dbc,hRic);
			   			hRic.put("data_fine",dataFine);
						rtRsa.aggiornaCalcoliRicoveriProcRsa(dbc, hRic, true, dataInizio, dataFine,"L",true);			
			}catch (Exception e)
				{
					System.out.println(nomeEJB + " ERRORE simulaCalcoloEff() -  -  " + e);
				throw e;
			}
}
	dateutility dtutil=new dateutility();
	private String getDataDim(ISASConnection dbc,Hashtable h)throws Exception {
		try {		
			String datafine=dtutil.getAnno(h.get("ass_datint")) + "-12-31";
		String sel="select * from rsa_ricoveri where " +
		" n_cartella="+h.get("n_cartella")+ "and ric_codist='"+h.get("ass_codist") +"'"+
		" and ric_datric="+ formatDate(dbc, h.get("ass_datric").toString());
		ISASRecord dbr=dbc.readRecord(sel);
		if(dbr!=null &&  dbr.get("ric_datdim")!=null  && !dbr.get("ric_datdim").toString().equals(""))
			datafine=dbr.get("ric_datdim").toString();
		return datafine;
		} catch (Exception e) {
			System.out.println(nomeEJB + " aggiornaContoEconomico() - " + e);
			throw e;
		}
	}
	public ISASRecord aggiornoImpegnato(myLogin mylogin,Hashtable h) throws  SQLException 
	{
		System.out.println(nomeEJB + " aggiornoImpegnato() - H == " + h.toString());

		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{
			dbc = super.logIn(mylogin);
			Vector vdbr = new Vector();
			String tp = (String)h.get("tp");
			ISASRecord dbr=null;
			 if(tp.equals("eff"))				// se il metodo e' invocato da JFRSAGridContEffettivi vado su rsa_asstar
				vdbr = Paginate_Effettivi(dbc,h,false);
			else if(tp.equals("pre"))
			vdbr = Paginate_Presunti(dbc,h,false);
			else return dbr;
			dbc.close();
			super.close(dbc);
			done = true;
			
			dbr=dbc.newRecord("rsa_ricoveri");
			Enumeration en=h.keys();
			while(en.hasMoreElements()) {
				String key=(String)en.nextElement();
				dbr.put(key,h.get(key));
			}
			String anno = null;
			String centrocosto = null;
			String conto = null;
			anno = h.get("anno").toString();
			centrocosto = h.get("centrocosto").toString();
			conto = h.get("codice").toString();		
			String selConto = "SELECT * from rsa_economici WHERE "+
			" eco_anno = '" + anno + "' AND "+
			"eco_codice = '" +conto + "' AND "+
			"eco_centrocosto = '" +centrocosto+ "' ";

			System.out.println(nomeEJB + " aggiorno conto  " + selConto);
			ISASRecord dbConti = dbc.readRecord(selConto);
			if(dbConti!=null) {
				Double totale=new Double(0);
				if(dbr.get("totale_impegnato")!=null)totale=(Double)dbr.get("totale_impegnato");
				if(tp.equals("eff"))		
						dbConti.put("eco_impreale", totale);
				else if(tp.equals("pre")) dbConti.put("eco_imppresu",totale);
				else return dbr;
				dbc.writeRecord(dbConti);
			}
			return dbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + " ERRORE queryPaginateCons() -- " + e);
			throw new SQLException(nomeEJB + " ERRORE queryPaginateCons() -- " + e);
		}
		finally
		{
			if(!done)
			{
				try
				{					
					dbc.close();
					super.close(dbc);
				}
				catch(Exception e)
				{  System.out.println(nomeEJB + " ERRORE queryPaginateCons() -- " + e);   }
			}
		}
	}
	
	//private calcolaGiorni calcolaGG = new calcolaGiorni();
	/*
	private void simulacalcolo(ISASConnection dbc,Hashtable hdati,String dataInizio,String dataFine,String anno) {
		
		Hashtable daysWeek=calcolaGG.getGiorniSettimana(dbc,hdati);
		Hashtable hGiorni=new Hashtable();
		if(daysWeek.isEmpty())daysWeek = calcolaGG.caricoTuttiGiorni(dbc,hdati);
		hGiorni = calcolaGG.calcolaGiorniHash(dataInizio,dataFine,daysWeek);
	}*/
} // fine RsaContiEJB

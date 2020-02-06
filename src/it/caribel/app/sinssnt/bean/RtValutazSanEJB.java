package it.caribel.app.sinssnt.bean;

//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//
//09/06/2009 - EJB di connessione alla procedura SINSSNT Tabella rt_valutaz_san
//
// Elisa Croci
//==========================================================================

import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestRivalutazione;
import it.pisa.caribel.sinssnt.casi_adrsa.GestPresaCarico;
import it.caribel.app.sinssnt.bean.nuovi.ScaleVal;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;
import it.pisa.caribel.util.dateutility;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class RtValutazSanEJB extends SINSSNTConnectionEJB  
{
	private ServerUtility su = new ServerUtility();

	// 15/07/09
	private ScaleVal gest_scaleVal = new ScaleVal(); 
	private GestCasi gestCasi = new GestCasi();
	private GestRivalutazione gestRiv = new GestRivalutazione();
	private GestPresaCarico gestPc = new GestPresaCarico();
	
	private dateutility du = new dateutility();
	private boolean myDebug = true;
	private String nomeEJB = "RtValutazSanEJB";

	
	public RtValutazSanEJB() {}

	
	
	
	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  Exception 
	{
		mySystemOut("queryKey public -- H == " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{			
			dbc = super.logIn(mylogin);
			String cartella = (String) h.get("n_cartella");
			String data = (String) h.get("data");

			String myselect = "SELECT * FROM rt_valutaz_san WHERE ";
			String sel="";
			
			sel = su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
			sel = su.addWhere(sel,su.REL_AND,"data",su.OP_EQ_NUM,formatDate(dbc,data));
			myselect = myselect+sel;
			
			mySystemOut(" queryKey --  " + myselect);
			
			
			
			ISASRecord dbr = dbc.readRecord(myselect);
			
			if(dbr != null)
			{
				if(dbr.get("pat_prev") != null && !dbr.get("pat_prev").equals(""))
				{
					dbr.put("des_patprev", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", 
							dbr.get("pat_prev").toString(), "diagnosi", "diagnosi"));
				}
				
				if(dbr.get("pat_conco") != null && !dbr.get("pat_conco").equals(""))
				{
					dbr.put("des_patconco", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", 
							dbr.get("pat_conco").toString(), "diagnosi", "diagnosi"));
				}
				
				if(dbr.get("medico_segn") != null && !dbr.get("medico_segn").equals(""))
				{
					dbr.put("medico_segn_desc", ISASUtil.getDecode(dbc, "medici", "mecodi", 
							dbr.get("medico_segn").toString(), "mecogn", "mecogn"));
				}
				if(dbr.get("medico_prop") != null && !dbr.get("medico_prop").equals(""))
				{
					dbr.put("medico_prop_desc", ISASUtil.getDecode(dbc, "medici", "mecodi", 
							dbr.get("medico_prop").toString(), "mecogn", "mecogn"));
				}
				if(dbr.get("ass_sociale") != null && !dbr.get("ass_sociale").equals(""))
				{
					dbr.put("ass_sociale_desc", ISASUtil.getDecode(dbc, "operatori", "codice", 
							dbr.get("ass_sociale").toString(), "cognome", "cognome"));
				}
				
				
				if(dbr.get("infermiere") != null && !dbr.get("infermiere").equals(""))
				{
					dbr.put("infermiere_desc", ISASUtil.getDecode(dbc, "operatori", "codice", 
							dbr.get("infermiere").toString(), "cognome", "cognome"));
				}
				if(dbr.get("medico_comunita") != null && !dbr.get("medico_comunita").equals(""))
				{
					dbr.put("medico_comunita_desc", ISASUtil.getDecode(dbc, "operatori", "codice", 
							dbr.get("medico_comunita").toString(), "cognome", "cognome"));
				}
				if(dbr.get("distretto") != null && !dbr.get("distretto").equals(""))
				{
					dbr.put("distretto_desc", ISASUtil.getDecode(dbc, "distretti", "cod_distr", 
							dbr.get("distretto").toString(), "des_distr", "des_distr"));
				}
				
				
				
				
//				ISASRecord caso_rif = gestCasi.getCasoRif(dbc, h);
//				if (caso_rif!=null)dbr.put("origine",caso_rif.get("origine").toString());
//				if (caso_rif!=null)dbr.put("tempo_t_caso",caso_rif.get("tempo_t").toString());
//				
				
				
				if(h.containsKey("stato"))
					dbr.put("stato", h.get("stato").toString());
				dbr.put("cod_operatore", h.get("cod_operatore").toString());
				dbr.put("desc_operatore", h.get("desc_operatore").toString());
				
				if (dbr.get("mmg_accessi") == null)
					dbr.put("mmg_accessi",""); 
			}
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "queryKey() [ " + e + " ]");
			throw e;
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
				catch(Exception e1){   System.out.println(nomeEJB + "queryKey() [ " + e1 + " ]");   }
			}
		}
	}
	
	private ISASRecord queryKey(ISASConnection dbc, Hashtable h) throws  Exception 
	{
		try
		{			
			String cartella = "";
			String data = "";
			
			try
			{
				cartella = (String)h.get("n_cartella");
				data = (String)h.get("data");
			}
			catch(Exception e)
			{
				System.out.println(nomeEJB + " query_insert() [ " + e + " ] == Errore: manca la chiave primaria!!!");
				throw new SQLException("Errore: manca la chiave primaria");
			}

			String myselect = "SELECT * FROM rt_valutaz_san WHERE ";
			String sel="";
			
			sel = su.addWhere(sel,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
			sel = su.addWhere(sel,su.REL_AND,"data",su.OP_EQ_NUM,formatDate(dbc,data));
			myselect = myselect+sel;
			
			mySystemOut(" queryKey --  " + myselect);
			
			ISASRecord dbr = dbc.readRecord(myselect);
			
			if(dbr != null)
			{
				if(dbr.get("pat_prev") != null && !dbr.get("pat_prev").equals(""))
				{
					dbr.put("des_patprev", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", 
							dbr.get("pat_prev").toString(), "diagnosi", "diagnosi"));
				}
				
				if(dbr.get("pat_conco") != null && !dbr.get("pat_conco").equals(""))
				{
					dbr.put("des_patconco", ISASUtil.getDecode(dbc, "tab_diagnosi", "cod_diagnosi", 
							dbr.get("pat_conco").toString(), "diagnosi", "diagnosi"));
				}
				
				if(dbr.get("medico_segn") != null && !dbr.get("medico_segn").equals(""))
				{
					dbr.put("medico_segn_desc", ISASUtil.getDecode(dbc, "medici", "mecodi", 
							dbr.get("medico_segn").toString(), "mecogn", "mecogn"));
				}
				if(dbr.get("medico_prop") != null && !dbr.get("medico_prop").equals(""))
				{
					dbr.put("medico_prop_desc", ISASUtil.getDecode(dbc, "medici", "mecodi", 
							dbr.get("medico_prop").toString(), "mecogn", "mecogn"));
				}
				if(dbr.get("ass_sociale") != null && !dbr.get("ass_sociale").equals(""))
				{
					dbr.put("ass_sociale_desc", ISASUtil.getDecode(dbc, "operatori", "codice", 
							dbr.get("ass_sociale").toString(), "cognome", "cognome"));
				}
				
				
				if(dbr.get("infermiere") != null && !dbr.get("infermiere").equals(""))
				{
					dbr.put("infermiere_desc", ISASUtil.getDecode(dbc, "operatori", "codice", 
							dbr.get("infermiere").toString(), "cognome", "cognome"));
				}
				if(dbr.get("medico_comunita") != null && !dbr.get("medico_comunita").equals(""))
				{
					dbr.put("medico_comunita_desc", ISASUtil.getDecode(dbc, "operatori", "codice", 
							dbr.get("medico_comunita").toString(), "cognome", "cognome"));
				}
				if(dbr.get("distretto") != null && !dbr.get("distretto").equals(""))
				{
					dbr.put("distretto_desc", ISASUtil.getDecode(dbc, "distretti", "cod_distr", 
							dbr.get("distretto").toString(), "des_distr", "des_distr"));
				}
				
				
				
				dbr.put("stato", h.get("stato").toString());
				dbr.put("cod_operatore", h.get("cod_operatore").toString());
				dbr.put("desc_operatore", h.get("desc_operatore").toString());
				if (dbr.get("mmg_accessi") == null)
					dbr.put("mmg_accessi",""); 
			
			}
			
			return dbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "queryKey() [ " + e + " ]");
			throw e;
		}
	}

	public Vector query(myLogin mylogin,Hashtable h) throws  Exception 
	{
		mySystemOut(" query(): H == " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
		String data_inizio = h.containsKey("pr_data") ? h.get("pr_data").toString() : null;
		String data_fine = h.containsKey("data_fine") ? h.get("data_fine").toString() : null;
		
		try
		{
			dbc = super.logIn(mylogin);
			String myselect = "SELECT * FROM rt_valutaz_san WHERE "+
							" n_cartella = " + (String)h.get("n_cartella");
			
			if(data_inizio != null)
				myselect = myselect + " AND data >= " + formatDate(dbc, data_inizio) + " ";
			if(data_fine != null)
				myselect = myselect + " AND data <= " + formatDate(dbc, data_fine) + " ";
			
			myselect = myselect + " ORDER BY data DESC";
			
			mySystemOut(" query(): myselect ==  " + myselect);
			ISASCursor dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			
			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "query() [ " + e + " ]");
			throw e;
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
				catch(Exception e1){   System.out.println(nomeEJB + "query() [ " + e1 + " ]");   }
			}
		}
	}

	public Vector query_grigliaStorico(myLogin mylogin,Hashtable h) throws  Exception 
	{
		boolean done = false;
		ISASConnection dbc = null;
		
		try
		{
			dbc = super.logIn(mylogin);

			
			String myselect = "SELECT * FROM rt_valutaz_san WHERE "+
							"n_cartella="+(String)h.get("n_cartella")+
							" ORDER BY data DESC";
			
			mySystemOut(" query_grigliaStorico - " + myselect);

			ISASCursor dbcur = dbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return vdbr;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "query_grigliaStorico() [ " + e + " ]");
			throw e;
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
				catch(Exception e1){   System.out.println(nomeEJB + "query_grigliaStorico() [ " + e1 + " ]");   }
			}
		}
	}

	public ISASRecord insert(myLogin mylogin,Hashtable h) throws Exception 
	{
		mySystemOut(" insert() -- H == " + h.toString());
		boolean done = false;
		ISASConnection dbc = null;
		
		try 
		{
			h.get("n_cartella").toString();
			h.get("data").toString();
		}
		catch (Exception e)
		{
			System.out.println(nomeEJB + "insert() [ " + e + " ] == Errore: manca la chiave primaria!!!");
			throw new SQLException("Errore: manca la chiave primaria");
		}
		
		try
		{
			dbc = super.logIn(mylogin);
			ISASRecord dbr = dbc.newRecord("rt_valutaz_san");
			
			
			Enumeration n=h.keys();
			while(n.hasMoreElements())
			{
				String e = n.nextElement().toString();
				dbr.put(e,h.get(e));
			}
			
			dbc.writeRecord(dbr);
			dbr = queryKey(dbc, h);
			
			ISASRecord caso = null;

			String pr_data = "";
			if (h.get("pr_data")==null || h.get("pr_data").toString().equals("0000-00-00") 
					|| h.get("pr_data").toString().equals(""))
			{
				pr_data = getProgetto(dbc,h);
				h.put("pr_data", pr_data);
			}
			else pr_data = h.get("pr_data").toString();
			
			if (!pr_data.equals(""))		
			{
			h.put("origine",Integer.toString(GestCasi.CASO_SAN));
				caso = gestCasi.getCasoRifOrigine(dbc, h);
			
			if (Integer.parseInt(dbr.get("tempo_t").toString())>0){
				//inserisco record su rt_rivalutazione per inviare evento.
				if (caso!=null){
				Hashtable h_caso = (Hashtable)h.clone();
				h_caso.put("id_caso", caso.get("id_caso"));
				//adatto campi della rivalutazione
				h_caso.put("dt_rivalutazione", h_caso.get("data"));
				h_caso.put("progr",h_caso.get("tempo_t"));
				h_caso.put("ubicazione",Integer.toString(GestCasi.UBI_RTOSC));
				
				//controllo che non ci sia una rivalutazione inserita
				ISASRecord rivalutaz = gestRiv.queryKey(dbc, h_caso);
				String conferma = "1";
				if (h_caso.get("conferma").toString().equals("N")) conferma = "2";
				h_caso.put("conferma",conferma);
				h_caso.put("badante", (h_caso.get("care_giver").toString().equals("1")?"S":"N"));
				//per sapere che si tratta di una valutazione di tipo sanitario inserisco valore 0 su palliative
				
				h_caso.put("palliative_sanitaria", "0");
				if (rivalutaz==null)
				gestRiv.insert(dbc,h_caso);
				else gestRiv.update(dbc, h_caso);
				}
			}
			else if (Integer.parseInt(h.get("tempo_t").toString())==0){
				//aggiorno la presa in carico per inviare evento.
				if (caso!=null){
				h.put("id_caso", caso.get("id_caso"));
				h.put("ubicazione",Integer.toString(GestCasi.UBI_RTOSC));
				
				//controllo che ci sia una presa in carico inserita
				ISASRecord pc = gestPc.queryKey(dbc, h);
				
				if (pc!=null){
					Hashtable dbr_h = (Hashtable)pc.getHashtable().clone();
					//per aggiornare eventuali record inseriti da COESO
					dbr_h.put("evento_id_in","");
					gestPc.update(dbc, dbr_h);
				}
				}
				
			}
			
			int tempo_t = 0;
			if (caso!=null)
					{
					LOG.info("Rivalutazione, caso letto = "+caso.get("id_caso").toString());
					if (Integer.parseInt(caso.get("tempo_t").toString()) <= Integer.parseInt(h.get("tempo_t").toString())) 
						tempo_t = Integer.parseInt(h.get("tempo_t").toString())+1;
					else tempo_t = Integer.parseInt(caso.get("tempo_t").toString())+1;
					caso.put("tempo_t",Integer.toString(tempo_t));
					LOG.info("Rivalutazione, nuovo tempo_t = "+caso.get("tempo_t").toString());
					
					dbc.writeRecord(caso);
					}
			}
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + "insert() [ " + e + " ]");
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + "insert() [ " + e + " ]");
			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "insert() [ " + e + " ]");
			throw e;
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
				catch(Exception e){   System.out.println(nomeEJB + "insert() [ " + e + " ]");  }
			}
		}
	}

	/* Viene chiamanta per controllare che una certa scheda possa essere inserita.
	   Non viene restituito NULL altrimenti vengono cancellati tutti i campi della form client.
	*/
	public ISASRecord query_insert(myLogin mylogin,Hashtable h) throws Exception 
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;
		
		try
		{
			dbc = super.logIn(mylogin);
			dbr = queryKey(dbc, h);
			
			if(dbr == null)
			{
				 
					//� stupido ma per ora bisogna fare cos� per non sbiancare campi passati...
					// Mi procuro un dbr e ci metto i dati passati dal Client!
					dbr = dbc.newRecord("cartella");
					Enumeration n = h.keys();
					
					while(n.hasMoreElements())
					{
						String e=(String)n.nextElement();
						dbr.put(e,h.get(e));
					}
					
					dbr.put("stato","insert");

					// 13/07/09 m. ---------
					leggiLastDiagnosi(dbc, dbr, (String)h.get("n_cartella"));
					// 13/07/09 -------------

					mySystemOut(" query_insert dbr chetorna..."+dbr.getHashtable().toString());
				
			}
			else 
			{
				dbr.put("operatore_desc",ISASUtil.getDecode(dbc,"operatori","codice",
						ISASUtil.getObjectField(dbr,"cod_operatore",'S'),
						"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));
				
				
			}
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + "query_insert() [ " + e + " ]");
			return null;
		}
		catch(Exception e)
		{
			 System.out.println(nomeEJB + "query_insert() [ " + e + " ]");
			throw e;
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
				catch(Exception e){   System.out.println(nomeEJB + "query_insert() [ " + e + " ]");   }
			}
		}
	}

	public ISASRecord update(myLogin mylogin,ISASRecord dbr) throws Exception 
	{
		boolean done = false;
		ISASConnection dbc = null;
		Hashtable h = dbr.getHashtable();
		System.out.println(nomeEJB + "update() -- h = [ " + h.toString()+ " ]");
		try 
		{
			dbr.get("n_cartella").toString();
			dbr.get("data").toString();
		}
		catch (Exception e)
		{
			System.out.println(nomeEJB + "update() [ " + e + " ]");
			throw new SQLException("Errore: manca la chiave primaria");
		}
		
		try
		{
			dbc = super.logIn(mylogin);
			
			dbc.writeRecord(dbr);

			dbr = queryKey(dbc, h);
			
			String pr_data = "";
			if (h.get("pr_data")==null || h.get("pr_data").toString().equals("0000-00-00") 
					|| h.get("pr_data").toString().equals(""))
			{
				pr_data = getProgetto(dbc,h);
				h.put("pr_data", pr_data);
			}
			else pr_data = h.get("pr_data").toString();
			
			if (!pr_data.equals(""))		
			{
			if (Integer.parseInt(dbr.get("tempo_t").toString())>0){
			//inserisco record su rt_rivalutazione per inviare evento.
			Hashtable h_caso = (Hashtable)h.clone();
			h_caso.put("origine", Integer.toString(GestCasi.CASO_SAN));
			ISASRecord caso_rif = gestCasi.getCasoRifOrigine(dbc, h_caso);
			if (caso_rif!=null){
			h_caso.put("id_caso", caso_rif.get("id_caso"));
			//adatto campi della rivalutazione
			h_caso.put("dt_rivalutazione", h_caso.get("data"));
			h_caso.put("progr",h_caso.get("tempo_t"));
			h_caso.put("ubicazione",Integer.toString(GestCasi.UBI_RTOSC));
			
			//controllo che non ci sia una rivalutazione inserita
			ISASRecord rivalutaz = gestRiv.queryKey(dbc, h_caso);
			String conferma = "1";
			if (h_caso.get("conferma").toString().equals("N")) conferma = "2";
			h_caso.put("badante", (h_caso.get("care_giver").toString().equals("1")?"S":"N"));
			h_caso.put("conferma",conferma);
			h_caso.put("update_rivalutazione","S");
			
			h_caso.put("palliative_sanitaria", "0");
			if (rivalutaz==null)
			gestRiv.insert(dbc,h_caso);
			else gestRiv.update(dbc, h_caso);
			}
			}
			else if (Integer.parseInt(h.get("tempo_t").toString())==0){
				//aggiorno la presa in carico per inviare evento.
				Hashtable h_caso = (Hashtable)h.clone();
				h_caso.put("origine", Integer.toString(GestCasi.CASO_SAN));
				ISASRecord caso_rif = gestCasi.getCasoRifOrigine(dbc, h_caso);
				
				if (caso_rif!=null){
					h_caso.put("id_caso", caso_rif.get("id_caso"));
					h_caso.put("ubicazione",Integer.toString(GestCasi.UBI_RTOSC));
				
				//controllo che ci sia una presa in carico inserita
				ISASRecord pc = gestPc.queryKey(dbc, h_caso);
				
				if (pc!=null){
					Hashtable dbr_h = (Hashtable)pc.getHashtable().clone();
					//per aggiornare eventuali record inseriti da COESO
					dbr_h.put("evento_id_in","");
					gestPc.update(dbc, dbr_h);
				}
				}
				
			}
		}
			dbr.put("operatore_desc",ISASUtil.getDecode(dbc,"operatori","codice",
					ISASUtil.getObjectField(dbr,"cod_operatore",'S'),
					"nvl(trim(cognome),'') ||' '  ||nvl(trim(nome),'')","desc_op"));
			
			dbc.close();
			super.close(dbc);
			done = true;
			return dbr;
		}
		catch(DBRecordChangedException e)
		{
			System.out.println(nomeEJB + "update() [ " + e + " ]");
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + "update() [ " + e + " ]");
			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "update() [ " + e + " ]");
			throw e;
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
				catch(Exception e){   System.out.println(nomeEJB + "update() [ " + e + " ]");   }
			}
		}
	}

	public void delete(myLogin mylogin,ISASRecord dbr)throws Exception 
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
			 System.out.println(nomeEJB + "delete() [ " + e + " ]");
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			System.out.println(nomeEJB + "delete() [ " + e + " ]");
			throw e;
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "delete() [ " + e + " ]");
			throw  e;
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
				catch(Exception e){   System.out.println(nomeEJB + "delete() [ " + e + " ]");  }
			}
		}

	}

	// 08/07/09
	public Integer duplicaMaxRecord(myLogin mylogin, Hashtable h)
		throws Exception
	{
		
		System.out.println("duplicaMaxRecord hash: "+h.toString());
		
		boolean done = false;
		String cart = null;
        String dataDup = null;
		String codOper = null;
		String tempoT = null;
		String pr_data = "";
		int ret = 1;
		ISASConnection dbc = null;

		try {
			dbc = super.logIn(mylogin);

			cart = (String)h.get("n_cartella");
			dataDup = (String)h.get("data");// data in cui verr� duplicato il record
			codOper = (String)h.get("cod_operatore");
			tempoT = "0";
			pr_data = (String)h.get("pr_data");
			

			
			
			//int tempo_t = 0;
			
			if (h.get("pr_data")==null || h.get("pr_data").toString().equals("0000-00-00") 
					|| h.get("pr_data").toString().equals(""))
			{
				pr_data = getProgetto(dbc,h);
				h.put("pr_data", pr_data);
			}
			
			
			if (!pr_data.equals(""))		
			{
				
				h.put("origine",Integer.toString(GestCasi.CASO_SAN));
				ISASRecord caso = gestCasi.getCasoRifOrigine(dbc, h);
			if (caso!=null){
				tempoT = Integer.toString(Integer.parseInt(caso.get("tempo_t").toString())+1);
				gest_scaleVal.duplicaMaxRec(dbc, "rt_valutaz_san", cart, dataDup, codOper, tempoT);
				
				String sql = "select * from rt_valutaz_san where n_cartella = " +cart+
				" and data = " +dbc.formatDbDate(dataDup);
				
				ISASRecord dbr_new = dbc.readRecord(sql);
				h = dbr_new.getHashtable();
				h.put("pr_data",pr_data);
				
				//inserisco record su rt_rivalutazione per inviare evento.	
				h.put("id_caso", caso.get("id_caso"));
				//adatto campi della rivalutazione
				h.put("dt_rivalutazione", h.get("data"));
				h.put("progr",h.get("tempo_t"));
				h.put("ubicazione",Integer.toString(GestCasi.UBI_RTOSC));
				
				//controllo che non ci sia una rivalutazione inserita
				ISASRecord rivalutaz = gestRiv.queryKey(dbc, h);
				
				String conferma = "1";
				if (h.get("conferma")!=null && h.get("conferma").toString().equals("N")) conferma = "2";
				h.put("conferma",conferma);
				h.put("badante", (h.get("care_giver")!=null && h.get("care_giver").toString().equals("1")?"S":"N"));
				h.put("palliative_sanitaria", "0");
				if (rivalutaz==null)
				gestRiv.insert(dbc,h);
				else gestRiv.update(dbc, h);
				
				LOG.info("Rivalutazione, caso letto = "+caso.get("id_caso").toString());
					caso.put("tempo_t",tempoT);
					LOG.info("Rivalutazione, nuovo tempo_t = "+caso.get("tempo_t").toString());
					
					dbc.writeRecord(caso);
					ret = 2;
				}
			else gest_scaleVal.duplicaMaxRec(dbc, "rt_valutaz_san", cart, dataDup, codOper, tempoT);
			}
			dbc.close();
			super.close(dbc);
			done=true;

			return new Integer(ret);
		}
		catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	public Hashtable leggiLastDiagnosi (myLogin mylogin, Hashtable h)
		throws Exception
	{
		boolean done = false;
	String cart = null;
    Hashtable ret = null;
	ISASConnection dbc = null;

	try {
		cart = h.get("n_cartella").toString();
		dbc = super.logIn(mylogin);
		ISASRecord dbr = dbc.newRecord("diagnosi");
		leggiLastDiagnosi(dbc,dbr,cart);
		ret = dbr.getHashtable();
	
	dbc.close();
	super.close(dbc);
	done=true;
	if (ret == null) ret = new Hashtable();
	return ret;
}
catch(Exception e){
	e.printStackTrace();
	throw e;
}finally{
	if(!done){
		try{
			dbc.close();
			super.close(dbc);
		}catch(Exception e2){System.out.println(e2);}
	}
}
}

	private void leggiLastDiagnosi(ISASConnection mydbc, ISASRecord mydbr, String cart) throws Exception
	{
		String pat_1 = "";
		String pat_2 = "000000";

		String sel = "SELECT d.* FROM diagnosi d" +
					" WHERE d.n_cartella = " + cart +
					" AND d.data_diag IN (SELECT MAX(a.data_diag) FROM diagnosi a" +
										" WHERE a.n_cartella = " + cart + ")"; 

		ISASRecord dbr_1 = mydbc.readRecord(sel);
		if (dbr_1 != null) {
			if ((dbr_1.get("diag1") != null) && (!((String)dbr_1.get("diag1")).trim().equals("")))
				pat_1 = (String)dbr_1.get("diag1");
			if ((dbr_1.get("diag2") != null) && (!((String)dbr_1.get("diag2")).trim().equals("")))
				pat_2 = (String)dbr_1.get("diag2");		
		}

		mydbr.put("pat_prev", pat_1);
		mydbr.put("des_patprev", ISASUtil.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi", 
														pat_1, "diagnosi", "diagnosi"));
		mydbr.put("pat_conco", pat_2);
		mydbr.put("des_patconco", ISASUtil.getDecode(mydbc, "tab_diagnosi", "cod_diagnosi", 
														pat_2, "diagnosi", "diagnosi"));
	}


	public Hashtable getCasoRif(myLogin mylogin,Hashtable h) throws  Exception 
	{
		mySystemOut("getCasoRif public -- H == " + h.toString());
		boolean done = false;
		Hashtable ret = new Hashtable();
		ret.put("tempo_t", "-1");
		ret.put("origine", "-1");
		ISASConnection dbc = null;
		
		try
		{			
			dbc = super.logIn(mylogin);
			String cartella = (String) h.get("n_cartella");
			String data = (String) h.get("pr_data");
			
			if (data==null || data.equals("")) {
				String sql = "select pr_data from progetto where n_cartella = "+cartella+
					 " and pr_data_chiusura is null";
				mySystemOut("getCasoRif sel progetto: "+sql);
				ISASRecord dbr = dbc.readRecord(sql);
				if (dbr!=null){
					data = du.getData(dbr.get("pr_data").toString(),"aaaa-mm-gg");
					h.put("pr_data",data);
				}
				else data = "";
				
				
			}
			
			if (cartella.equals("") || data.equals("")){
				mySystemOut("getCasoRif public: Chiave ricerca mancante");
				return ret;
			}
			
			
			
			h.put("origine",Integer.toString(GestCasi.CASO_SAN));
			ISASRecord caso_rif = gestCasi.getCasoRifOrigine(dbc, h);
			if (caso_rif!=null) ret = caso_rif.getHashtable();
			else{
				ret.put("tempo_t", "0");
				ret.put("origine", "-1");				
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return ret;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
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
				catch(Exception e1){   System.out.println(nomeEJB + "queryKey() [ " + e1 + " ]");   }
			}
		}
	}
	
	

	private String getProgetto (ISASConnection dbc, Hashtable h) throws DBSQLException, DBMisuseException, ISASPermissionDeniedException, ISASMisuseException{
		
		String cartella = (String) h.get("n_cartella");
		String data = "";
			
		
			String sql = "select pr_data from progetto where n_cartella = "+cartella+
				 " and pr_data_chiusura is null";
			mySystemOut("getProgetto sel progetto: "+sql);
			ISASRecord dbr = dbc.readRecord(sql);
			if (dbr!=null){
				data = du.getData(dbr.get("pr_data").toString(),"aaaa-mm-gg");
				h.put("pr_data",data);
			}
		
		System.out.println("getProgetto data: "+data);
		return data;
	}

	public String getTempoTfromCaso(myLogin mylogin,Hashtable h) throws  Exception 
	{
		mySystemOut("getCasoRif public -- H == " + h.toString());
		boolean done = false;
		String ret = "0";
		ISASConnection dbc = null;
		
		try
		{			
			dbc = super.logIn(mylogin);
			String cartella = (String) h.get("n_cartella");
			String data = (String) h.get("pr_data");
			
			if (data.equals("")) {
				String sql = "select pr_data from progetto where n_cartella = "+cartella+
					 " and pr_data_chiusura is null";
				mySystemOut("getCasoRif sel progetto: "+sql);
				ISASRecord dbr = dbc.readRecord(sql);
				if (dbr!=null){
					data = du.getData(dbr.get("pr_data").toString(),"aaaa-mm-gg");
				}
				
				
			}
			
			if (cartella.equals("") || data.equals("")){
				mySystemOut("getTempoTfromCaso: Chiave ricerca mancante");
				return ret;
			}
			h.put("pr_data",data);
			h.put("origine",Integer.toString(GestCasi.CASO_SAN));
			
			ISASRecord caso_rif = gestCasi.getCasoRifOrigine(dbc, h);
			if (caso_rif!=null)  {
				ret = caso_rif.get("tempo_t").toString();
				int t = Integer.parseInt(ret);
				ret = Integer.toString(t++);
				mySystemOut("getTempoTfromCaso tempo_t:"+ret);
			}
			dbc.close();
			super.close(dbc);
			
			
			done = true;
			return ret;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
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
				catch(Exception e1){   System.out.println(nomeEJB + "queryKey() [ " + e1 + " ]");   }
			}
		}
	}
	

	public Boolean query_PrimaValutazione(myLogin mylogin,Hashtable h) throws  Exception 
	{
		mySystemOut("query_PrimaValutazione public -- H == " + h.toString());
		boolean done = false;
		boolean ret = false;
		ISASConnection dbc = null;
				
		
		try
		{			
			dbc = super.logIn(mylogin);
			String cartella = (String) h.get("n_cartella");
			String data = (String) h.get("dt_presa_carico");

			if (data != null){
			
			String myselect = "SELECT * FROM rt_valutaz_san WHERE n_cartella = "+cartella+
							  " and data >= "+dbc.formatDbDate(data)+
							  " and tempo_t = 0";
			
			
			mySystemOut(" queryKey --  " + myselect);
			
			ISASRecord dbr = dbc.readRecord(myselect);
			
			if(dbr != null)ret = true;
			}
			dbc.close();
			super.close(dbc);
			done = true;
			return new Boolean(ret);
		}
		catch(Exception e)
		{
			System.out.println(nomeEJB + "query_PrimaValutazione() [ " + e + " ]");
			throw e;
		}
		finally
		{
			if(!done)
			{
				try
				{
					dbc.close();
					super.close(dbc);
					return new Boolean(ret);
				}
				catch(Exception e1){ e1.printStackTrace();   }
			}
		}
	}


	private void mySystemOut(String msg)
  	{
		if (myDebug)
			System.out.println(nomeEJB + ": " + msg);
	}

}


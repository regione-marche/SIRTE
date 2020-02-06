package it.caribel.app.sinssnt.bean.modificati;

// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 09/08/2002 - EJB di connessione alla procedura SINS Tabella Interv
// giulia brogi/jessica caccavale
//
// Bargi: 24 Gennaio 2005 inserito gestione flag per flussi:int_flag_flussi di INTERV
// 18/12/06 m.: aggiunto oncologo(=52) nel metodo "faiSelectContatti()".
// ==========================================================================

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.dbinterf2.DBMisuseException;
import it.pisa.caribel.dbinterf2.DBRecordChangedException;
import it.pisa.caribel.dbinterf2.DBSQLException;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASPermissionDeniedException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.casi_adrsa.GestCasi;
import it.pisa.caribel.sinssnt.casi_adrsa.GestErogazione;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.util.ServerUtility;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
// 26/08/09



public class IntervEJB extends SINSSNTConnectionEJB  {
	public IntervEJB() {}

	// 11/06/07 m.: modificato, nel metodo "faiSelectContatti()", la "SELECT ski.data_apertura,..."
	//	in "SELECT ski.*, ski.data_apertura" in modo che vengano eseguiti i ctrl ISAS.
	//	Nel metodo "queryKey()", nel "catch" dell'eccezione ISASPermissionDenied, sostituito
	//	"return null" con "throws" della CariException da gestire latoClient.


	// 11/06/07
	private	String msgNoD = "Mancano i diritti per leggere il record";
	private static final String MIONOME ="8-IntervEJB.";
	private static final String CONSTANTS_ABL_FLUSSI_SPR ="ABL_GST_SPR";
	private static final String CONSTANTS_FLAG_INVIATO_INSERIMENTO = "0"; // il record viene inserito
	private static final String CONSTANTS_FLAG_INVIATO_INVIATO = "1"; // il record � stato modificato
	private static final String CONSTANTS_FLAG_INVIATO_VARIATO = "2"; // il record viene modificato

	// 28/10/09
	private GestErogazione gestore_erog = new GestErogazione();
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	private GestCasi gestore_casi = new GestCasi();

	public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws
	SQLException, ISASPermissionDeniedException,ISASMisuseException, CariException {
		boolean done=false;
		ISASConnection dbc=null;
		ISASRecord dbrPrest = null;
		String contatore = "";
		String operatore = "";
		String cartella = "";
		String anno = "";
		ISASCursor dbgriglia=null;
		try{
			ServerUtility su =new ServerUtility();
			dbc=super.logIn(mylogin);
			if(!(h.get("int_anno") == null || h.get("int_anno").equals(""))) {
				anno = (String)h.get("int_anno");
			}
			if(!(h.get("int_contatore")== null  || h.get("int_contatore").equals(""))){
				contatore = ""+h.get("int_contatore");
			}
			String myselect="Select i.* ,o.tipo"+
					//String myselect="Select i.int_cartella,i.int_codpres ,o.tipo"+
					" from interv i,operatori o where "+
					"i.int_anno='"+anno+"' and "+
					"i.int_contatore="+contatore+
					" and o.codice=i.int_cod_oper";
			if (!operatore.equals(""))
				myselect=myselect+" and i.int_cod_oper='"+operatore+"'";
			System.out.println("INTERV-->QUERYKEY: "+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			if (dbr!=null){
				if (dbr.get("int_contatto")!=null && (((Integer)dbr.get("int_contatto")).toString()).equals("0"))
				{/*se int_contatto � diverso da 0 vuol dire che � un accesso effetuato da personale interno
                            quindi insertito da accessi "normali" .
				 */  System.out.println("INT_CONTATTO " + dbr.get("int_contatto"));
				 dbr.put("tipo_accesso","ESTERNO");
				} else
					dbr.put("tipo_accesso","INTERNO");
				if (dbr.get("int_cartella")!=null && !((Integer)dbr.get("int_cartella")).equals("")){
					cartella=((Integer)dbr.get("int_cartella")).toString();
					String selcart="SELECT cognome,nome from cartella"+
							" WHERE n_cartella="+cartella;
					ISASRecord dbcart=dbc.readRecord(selcart);
					if (dbcart!=null)
					{
						String nome=(String)dbcart.get("nome");
						dbr.put("cognome",(String)dbcart.get("cognome")+" "+nome);
					}
					if (dbr.get("int_qual_oper")!=null && !((String)dbr.get("int_qual_oper")).equals("")){
						String selop="SELECT desc_qualif from operqual"+
								" WHERE cod_qualif='"+(String)dbr.get("int_qual_oper")+"'";
						ISASRecord dbop=dbc.readRecord(selop);
						if (dbop!=null && dbop.get("desc_qualif")!=null)
							dbr.put("desc_qualif",(String)dbop.get("desc_qualif"));
						else
							dbr.put("desc_qualif","****CODICE "+(String)dbr.get("int_qual_oper")+" NON DECODIFICATO****");

					}
					if (dbr.get("int_codpres")!=null && !((String)dbr.get("int_codpres")).equals("")){
						String selpres="SELECT despres from presidi"+
								" WHERE codpres='"+(String)dbr.get("int_codpres")+"'";
						ISASRecord dbpres=dbc.readRecord(selpres);
						if (dbpres!=null && dbpres.get("despres")!=null)
							dbr.put("despres",(String)dbpres.get("despres"));
						else
							dbr.put("despres","****CODICE "+(String)dbr.get("int_codpres")+" NON DECODIFICATO****");
					}
					//Scommentato Jessy il 07/10/2003
					if (dbr.get("int_cod_oper")!=null && !((String)dbr.get("int_cod_oper")).equals("")){
						String seloper="SELECT nvl(trim(cognome),'') ||' ' || nvl(trim(nome),'') oper"+
								" FROM operatori"+
								" WHERE codice='"+(String)dbr.get("int_cod_oper")+"'";
						ISASRecord dboper=dbc.readRecord(seloper);
						if (dboper!=null && dboper.get("oper")!=null)
							dbr.put("opcogn",(String)dboper.get("oper"));
						else
							dbr.put("opcogn","****CODICE "+(String)dbr.get("int_cod_oper")+" NON DECODIFICATO****");
					}
					String chiave_ComboContatti="";
					String contatto="";
					if (dbr.get("int_contatto")!=null)
					{
						contatto=""+(Integer)dbr.get("int_contatto");
						if (!contatto.equals("0") && !contatto.equals(""))
							chiave_ComboContatti=cartella+"#"+contatto;
					}
					dbr.put("chiave_combo",chiave_ComboContatti);
					/*mi vado a caricare il tipo servizio se il tipo operatore
                               � 01 questo perch� devo poi caricare la combo solo con i contatti con quel
                               tipo servizio  in modo da avere prestazioni corrette
					 */
					String tipoServizio="";
					String tipo_oper="";
					if (dbr.get("int_tipo_oper")!=null)
						tipo_oper=(String)dbr.get("int_tipo_oper");
					if(tipo_oper.equals("01"))
					{
						if (!contatto.equals("0") && !contatto.equals(""))
						{
							String selCont="SELECT tipo_servizio FROM contatti"+
									" WHERE n_contatto="+contatto
									+ " AND n_cartella="+cartella;
							ISASRecord dbCont=dbc.readRecord(selCont);
							System.out.println("sel-->"+selCont);
							if (dbCont!=null && dbCont.get("tipo_servizio")!=null)
								tipoServizio=(String)dbCont.get("tipo_servizio");
							//System.out.println("tipoServ-->"+tipoServizio);
						}
					}
					dbr.put("tipo_servizio",tipoServizio);
					//caricamento griglia
					String selg="Select i.*,p.prest_des_dett FROM "+
							" intpre i,prestaz p WHERE "+
							//DEVE ESSERE TOLTA PRE_CARTELLA DA INTPRE 09/10/2003
							//"i.pre_cartella='"+cartella+"' and "+
							"i.pre_anno ='"+anno+"' and "+
							"i.pre_contatore="+contatore+" and "+
							"i.pre_cod_prest=p.prest_cod "+
							//                               " ORDER BY pre_progr desc";
							" ORDER BY pre_cod_prest ";
					dbgriglia=dbc.startCursor(selg);
					Vector vdbg=dbgriglia.getAllRecord();
					dbr.put("griglia",vdbg);
					dbgriglia.close();
					dbgriglia=null;

				}
				else
					dbr.put("cognome","NESSUNA DECODIFICA");
			}
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(ISASPermissionDeniedException e){
			/** 11/06/07
		System.out.println("eccezione permesso negato "+e);
		return null;
			 **/
			// 11/06/07
			System.out.println("IntervEJB.queryKey(): "+e);
			throw new CariException(msgNoD, -2);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una queryKey()  ");
		}finally{
			if(!done){
				try{
					if (dbgriglia!=null)dbgriglia.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public Vector CaricaPrest(myLogin mylogin, Hashtable h)
			throws SQLException{
		boolean done = false;
		Vector rit = new Vector();
		ISASConnection dbc = null;
		ISASCursor dbcur=null;
		ISASCursor dbcurConf =null;
		String msg="";
		try{
			dbc = super.logIn(mylogin);
			Hashtable hTipo=new Hashtable();
			//vado a prendere il tipo operatore
			String tipoOper=(String)h.get("tipo_oper");
			System.out.println("CaricaPrest su Interv: ");
			/*se il tipo operatore � 05--> amministrativo prendo tutte le prestazioni
               altrimenti prendo quelle del tipo dell'operatore.
               Nel caso di assistente sociale devo prendere i due tipi a seconda del servizio
			 */
			String sWhere="";
			if (!tipoOper.equals("05"))
			{
				String aggiunta=")";
				if (tipoOper.equals("01"))
					aggiunta= " OR conf_key ='TIPDEF"+tipoOper+"B')";
				String myselectconf="Select  conf_txt from conf where "+
						"conf_kproc ='SINS' and "+
						"(conf_key ='TIPDEF"+tipoOper+"'"+ aggiunta;
				System.out.println("CONF: "+myselectconf);
				dbcurConf = dbc.startCursor(myselectconf);

				String tipoPrest="";
				while(dbcurConf.next()){
					ISASRecord dbconf = dbcurConf.getRecord();
					if (dbconf!=null)
					{
						tipoPrest=(String)dbconf.get("conf_txt");
						if (sWhere.equals(""))
							sWhere=sWhere+" prest_tipo='"+tipoPrest+"'";
						else
							sWhere=sWhere+" OR prest_tipo='"+tipoPrest+"'";
					}
				}
				if (!sWhere.equals(""))sWhere=" WHERE " +sWhere;
				dbcurConf.close();
				dbcurConf=null;
			}//fine operatore 05


			String sel="SELECT prest_cod, prest_des,prest_des_dett,"  +
					"prest_tipo,prest_tempo,prest_codreg "+
					"FROM prestaz " + sWhere;
			// 08/04/08				+" ORDER BY prest_cod";
			//                          "ORDER BY prest_des ";
			System.out.println("CaricaPrest su Interv: "+sel);
			dbcur = dbc.startCursor(sel);
			int k=0; // 08/04/08: per conteggio in System.out
			while(dbcur.next()){
				Hashtable hpres=new Hashtable();
				ISASRecord dbr = dbcur.getRecord();
				System.out.println("prest_cod="+(String)dbr.get("prest_cod"));
				if (dbr.get("prest_cod")!=null && !((String)dbr.get("prest_cod")).equals(""))
				{
					// 08/04/08				System.out.println("/////// IntervAS - CaricaPrest 1: cod=["+(String)dbr.get("prest_cod")+"] - k=["+k+"] ///////");
					if (dbr.get("prest_tipo")!=null && !((String)dbr.get("prest_tipo")).equals(""))
					{
						String tipo=(String)dbr.get("prest_tipo");
						if (hTipo!=null && ((Hashtable)hTipo.get(tipo))!=null)
							hpres=(Hashtable)hTipo.get(tipo);
						hpres=AnalizzaPrestazioni(dbc,dbr,hpres);
						hTipo.put(tipo,hpres);
						// 08/04/08				System.out.println("/////// IntervAS - CaricaPrest 2: tipo=["+tipo+"] - k=["+k+"] ///////");
					}
				}
				k++; // 08/04/08: per conteggio in System.out
			}
			// 08/04/08		System.out.println("/////// IntervAS - CaricaPrest 3: k=["+k+"] //////");
			rit.add(hTipo);
			// 08/04/08			System.out.println("/////// IntervAS - CaricaPrest 4: sto x ritornare");
			//System.out.println("HTIPO=>"+hTipo.toString());
			dbcur.close();
			dbcur=null;
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		}catch(Exception e){
			System.out.println("************ IntervAS: exc="+e);
			// 08/04/08 
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una caricaPrest()  ");
		}finally{
			if(!done){
				try{
					if (dbcur!=null)dbcur.close();
					if (dbcurConf!=null)dbcurConf.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public ISASRecord query_operatoreQual(myLogin mylogin,Hashtable h) throws
	SQLException{
		boolean done=false;
		ISASConnection dbc=null;
		String operatore = "";
		String strTipoConstraint = ""; //gb 29/05/07
		boolean boolTipo05 = false;    //gb 29/05/07
		try{
			ServerUtility su =new ServerUtility();
			dbc=super.logIn(mylogin);
			if(!(h.get("codice")== null  || h.get("codice").equals(""))){
				operatore = (String)h.get("codice");
			}

			/** 11/02/11		
		//gb 29/05/07 ------
		if ((h.get("tipo") != null) && ((String)h.get("tipo")).equals("05")) {
			// 10/01/08
			 **/			
			if ((h.get("tipo_oper_constraint") != null) && (!((String)h.get("tipo_oper_constraint")).equals("")))
				strTipoConstraint = " AND tipo = '" + (String)h.get("tipo_oper_constraint") + "'";
			/** 11/02/11						
		  	boolTipo05 = true;
		}
		//gb 29/05/07: fine ----
			 **/

			System.out.println(":::::::: INTERV --> tipoOper=["+h.get("tipo")+"] - tipoOperSCELTO=["+h.get("tipo_oper_constraint")+"]");
			String myselect="SELECT * FROM operatori"+
					" WHERE codice ='"+operatore+"'"+
					strTipoConstraint;
			System.out.println("--->IntervEJB/query_operatoreQual - myselect="+myselect);

			ISASRecord dbr=dbc.readRecord(myselect);
			if (dbr!=null){
				//operatore
				String cognome="";
				if (dbr.get("cognome")!=null && !((String)dbr.get("cognome")).equals("")){
					cognome=((String)dbr.get("cognome")).trim();
				}
				if (dbr.get("nome")!=null && !((String)dbr.get("nome")).equals("")){
					cognome=cognome+ " "+((String)dbr.get("nome")).trim();
				}
				dbr.put("cognome",cognome);

				if (dbr.get("cod_qualif")!=null && !((String)dbr.get("cod_qualif")).equals("")){
					String selop="SELECT desc_qualif from operqual"+
							" WHERE cod_qualif='"+(String)dbr.get("cod_qualif")+"'";
					//                System.out.println("QUALIFICA--OPERATORE-->"+ selop);

					ISASRecord dbop=dbc.readRecord(selop);
					if (dbop!=null && dbop.get("desc_qualif")!=null) {
						dbr.put("desc_qualif",(String)dbop.get("desc_qualif"));
						//                    System.out.println("QUALIFICA--OPERATORE-->"+ (String)dbop.get("desc_qualif"));
					}
				}

				if (dbr.get("tipo")==null)
					System.out.println("L'OPERATORE "+operatore+" NON HA DEFINITO IL TIPO");

				/** 11/02/11				
		    //gb 29/05/07 *******
		    if (!boolTipo05)
		        if ((h.get("tipo") != null) && !((String)h.get("tipo")).equals(""))
			    	dbr.put("tipo", h.get("tipo"));
			 	else
			    	System.out.println("--->IntervEJB/query_operatoreQual - Il tipo operatore del client � non valorizzato");
		    //gb 29/05/07: fine *******
				 **/

				String tipo=(String)dbr.get("tipo");
				String myselectconf="Select  conf_txt from conf where "+
						"conf_kproc ='SINS' and "+
						"conf_key ='TIPDEF"+tipo+"'";
				System.out.println(myselectconf);

				ISASRecord dbconf=dbc.readRecord(myselectconf);
				if (dbconf!=null)
					dbr.put("int_tipo_prest",(String)dbconf.get("conf_txt"));
				else
					System.out.println("MANCA NEL CONFIGURATORE IL RECORD CON CHIAVE TIPDEF"+ tipo +"CHE MI DEFINISCE IL TIPO PRESTAZIONE");

				//vado a leggere il presidio
				String selpres="SELECT despres from presidi"+
						" WHERE codpres='"+(String)dbr.get("cod_presidio")+"'";

				ISASRecord dbpres=dbc.readRecord(selpres);
				if (dbpres!=null && dbpres.get("despres")!=null)
					dbr.put("des_presidio",(String)dbpres.get("despres"));
				else {
					if ((String)dbr.get("int_codpres")!=null)
						dbr.put("des_presidio","****CODICE "+(String)dbr.get("int_codpres")+" NON DECODIFICATO****");
					else
						dbr.put("des_presidio","");
				}
			}
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_operatoreQual()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}


	private Hashtable AnalizzaPrestazioni(ISASConnection dbc,ISASRecord dbr,Hashtable hpres)
			throws SQLException{
		try{
			//non testo che sia nullo perch� l'ho gi� fatto prima
			String prest=(String)dbr.get("prest_cod");
			// 08/04/08 System.out.println(":::::: IntervAS - AnalizzaPrestazioni: prest_cod=["+prest+"] :::::::");
			if (hpres!=null)
			{
				Hashtable hTuttoPrest=new Hashtable();
				hTuttoPrest.put("prest_des",(dbr.get("prest_des")!=null)?(String)dbr.get("prest_des"):"");
				hTuttoPrest.put("prest_des_dett",(dbr.get("prest_des_dett")!=null)?(String)dbr.get("prest_des_dett"):"");
				hTuttoPrest.put("prest_tempo",(dbr.get("prest_tempo")!=null)?""+((Integer)dbr.get("prest_tempo")):"0");
				hTuttoPrest.put("prest_importo",(dbr.get("prest_codreg")!=null)?CaricaImporto(dbc,(String)dbr.get("prest_codreg")):"0");
				hpres.put(prest, hTuttoPrest);
			}
			//System.out.println("HPRESTAZIONI=>"+hpres.toString());
			return hpres;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una AnalizzaPrestazioni");
		}
	}


	private String CaricaImporto(ISASConnection dbc,String codreg)
			throws SQLException{
		try{
			String importo="";
			String sel="SELECT n_tam FROM nomenc WHERE "+
					" n_cod='"+codreg+"'";
			ISASRecord dbreg=dbc.readRecord(sel);
			if(dbreg!=null &&dbreg.get("n_tam")!=null )
				importo=""+dbreg.get("n_tam");
			else
				importo="0";
			return importo;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una CaricaImporto");
		}
	}


	/*
public ISASRecord query_cartella(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from cartella where "+
			"n_cartella="+(String)h.get("n_cartella");
		ISASRecord dbr=dbc.readRecord(myselect);
		String w_codice="";
                String w_descr="";
                String w_select="";
                ISASRecord w_dbr=null;
                if (dbr!= null) {
                  w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
                                    "FROM anagra_c a,cartella WHERE "+
                                    "a.n_cartella="+(String)h.get("n_cartella");
                  //Ilaria 12/01/2005 INIZIO
                  //Aggiungo il controllo sulla data_variazione massima
		  // Giulia Brogi 13/04/05: il controllo sulla data
		 // variazione<=data prestazione � al salvataggio
                  w_select=w_select+" AND a.data_variazione IN "+
                          " (SELECT MAX(data_variazione) from anagra_c WHERE "+
			  "anagra_c.n_cartella=a.n_cartella )"+
			" AND cartella.n_cartella=a.n_cartella";
                  //Ilaria 12/01/2005 FINE
                  w_dbr=dbc.readRecord(w_select);
		  if (w_dbr.get("dom_citta") != null && !(w_dbr.get("dom_citta").equals("")))
                    dbr.put("citta", w_dbr.get("dom_citta"));
                  else
                    dbr.put("citta", w_dbr.get("citta"));
                  if (w_dbr.get("dom_areadis") != null && !(w_dbr.get("dom_areadis").equals("")))
                    dbr.put("cod_area", w_dbr.get("dom_areadis"));
                  else
                    dbr.put("cod_area", w_dbr.get("areadis"));
                  if(dbr.get("cod_area") != null && !(dbr.get("cod_area").equals(""))){
                    String selComu="SELECT descrizione FROM areadis WHERE "+
                                   "codice='"+(String)dbr.get("cod_area")+"'";
                    ISASRecord dbcomu = dbc.readRecord(selComu);
                    dbr.put("desc_area",dbcomu.get("descrizione"));
                  }
                }

                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(ISASPermissionDeniedException e){
		System.out.println("eccezione permesso negato "+e);
		return null;
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
	 */
	public Hashtable prestDef(myLogin mylogin,Hashtable h) throws SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		String myselect = "";
		String ritorno="";
		try{
			dbc=super.logIn(mylogin);
			//System.out.println("prestDef "+h.toString());
			myselect="Select tipo from operatori where "+
					"codice='"+(String)h.get("cod_oper")+"'";
			System.out.println(myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			String tipo = (String) dbr.get("tipo");
			if(tipo.equals("01")){
				//se il tipo servizio che viene dal client � uguale a 2 devo andare a prendere
				//nel conf il TIPDEF01B altrimenti il TIPDEF01
				if (h.get("tipo_servizio")!=null && !((String)h.get("tipo_servizio")).equals("")){
					String servizio = (String)h.get("tipo_servizio");
					if(servizio.equals("2"))
						tipo = tipo+"B";
				}
			}
			// preleva prestaz. e tipo prestazione di default per quel tipo operatore
			// da tabella di configurazione di Sins
			myselect="Select  conf_txt from conf where "+
					"conf_kproc ='SINS' and "+
					"conf_key ='TIPDEF"+tipo+"'";
			System.out.println(myselect);
			ISASRecord dbrec=dbc.readRecord(myselect);
			if (dbrec!=null)
				h.put("tipo_prest",(String)dbrec.get("conf_txt"));
			else
				System.out.println("MANCA NEL CONFIGURATORE IL RECORD CON CHIAVE TIPDEF"+ tipo +"CHE MI DEFINISCE IL TIPO PRESTAZIONE");
			dbc.close();
			super.close(dbc);
			done=true;
			return h;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una prestDef()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}

	public Hashtable getLastInterv(myLogin mylogin, Hashtable h)throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		ISASRecord dbr=null;
		String int_anno = "";
		String int_cartella = "";
		String int_cod_oper = "";
		String int_contatto = "";

		try{
			dbc=super.logIn(mylogin);
			try{
				int_anno=h.get("int_anno").toString();
				int_cartella=h.get("int_cartella").toString();
				int_cod_oper=h.get("int_cod_oper").toString();
				int_contatto=h.get("int_contatto").toString();
			}catch(Exception e) {System.out.println("Errore in getLastInterv: mancano int_cartella e/o int_anno   ex: "+e);
			throw new SQLException("Errore, manca la chiave primaria");
			}


			String myselect="Select * from interv where "+
					"int_anno= '"+int_anno+"' and int_cartella = "+ int_cartella+
					" and int_cod_oper = '"+int_cod_oper+"' ORDER BY int_contatore desc ";
			dbr=dbc.readRecord(myselect);
			Hashtable ret = dbr.getHashtable();
			dbc.close();
			super.close(dbc);
			done=true;
			return ret;
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
	public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		ISASCursor dbcur=null;
		String methodName = "query ";
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select * from interv where "+
					"tipo_prest= '"+(String)h.get("tipo_prest")+
					"' ORDER BY tipo_prest ";
			dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			dbcur.close();
			dbcur=null;
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(methodName, dbcur);
			logout_nothrow(methodName, dbc);
		}
	}


	//Carlo Volpicelli 09/03/2017
	public Vector queryAccessi(myLogin mylogin,Hashtable h) throws  SQLException {
		boolean done=false;
		ISASConnection dbc=null;
		ISASCursor dbcur=null;
		String methodName = "queryAccessi ";
		try{
			dbc=super.logIn(mylogin);
			String myselect="Select int_data_prest from interv where "+
					"int_tipo_prest= '"+(String)h.get("int_tipo_prest")+"' AND int_cartella="+
					(String)h.get("n_cartella")+" AND int_contatto="+(String)h.get("n_contatto");

			dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			dbcur.close();
			dbcur=null;
			dbc.close();
			super.close(dbc);
			done=true;
			return vdbr;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			close_dbcur_nothrow(methodName, dbcur);
			logout_nothrow(methodName, dbc);
		}
	}


	public Hashtable query_allcartella(myLogin mylogin,Hashtable h) throws  SQLException
	{/*mi carica tutto quello che riguarda l'assistito
   E' stato fatto per fare un unico accesso al db
   mi restituisce un hashtable con 5 chiavi

   chiave :n_cartella       valore :cognome nome assistito
   chiave :"contatti"       valore :vettore di ISASREcord con tutti i contatti .Viene scorso lato client
                                       per riempire la combo
   chiave :"pianoInterv"    valore : hashtable che ha come chiave il numero contatto e come valore
                                       il vettore di ISASrecord con le prestazioni

	 */
		boolean done=false;
		ISASConnection dbc=null;
		Hashtable hRet=new Hashtable();
		try{
			dbc=super.logIn(mylogin);
			//leggo la cartella----------------------------------------------------------------------------------
			String assistito="";
			if (!((String)h.get("n_cartella")).equals(""))
			{
				String myselect="Select nome,cognome," +
						" data_apertura, data_chiusura" + // 01/09/08
						" from cartella where "+
						"n_cartella="+(String)h.get("n_cartella");
				ISASRecord dbr=dbc.readRecord(myselect);
				if (dbr!= null)
				{
					if (dbr.get("cognome") != null )
						assistito=((String)dbr.get("cognome")).trim();
					if (dbr.get("nome") != null )
						assistito=assistito+" "+((String)dbr.get("nome")).trim();
					// 01/09/08 ----
					if (dbr.get("data_apertura") != null)
						hRet.put("data_apertura", "" + dbr.get("data_apertura"));
					if (dbr.get("data_chiusura") != null)
						hRet.put("data_chiusura", "" + dbr.get("data_chiusura"));
					// 01/09/08 ----
				}
			}
			hRet.put((String)h.get("n_cartella"),assistito);
			//-------------fine cartella --------------------------------------------
			/*vado a caricare i contatti mentre scorro i contatti mi carico anche il piano interventi
---------------------------------------------*/
			String operatore="";
			String tipo="";
			if(h.get("tipo_oper")!=null)
			{
				tipo=(String)h.get("tipo_oper");
				if (!((String)h.get("n_cartella")).equals(""))
					hRet=caricaContatti(h,tipo,hRet,dbc);
			}
			hRet.put("tipo_oper",tipo);
			//-------------fine cartella --------------------------------------------
			dbc.close();
			super.close(dbc);
			done=true;
			return hRet;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_allCartella  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}



	public Hashtable datiFlussi_spr(myLogin mylogin, Hashtable h) throws SQLException {
		/* recupero decodifiche di dati dei flussi spr
		 */
		String punto= "datiFlussi_spr ";
		boolean done = false;
		ISASConnection dbc = null;
		Hashtable hRet = new Hashtable();
		System.out.println(punto + "dati che ricevo>"+ h+"<");
		try {
			dbc = super.logIn(mylogin);
			// leggo la cartella----------------------------------------------------------------------------------

			//			dati.put("user", user);
			//			dati.put("presidioFlussiSPR", presidioFlussiSPR);
			//			dati.put("codreg", (String) profile.getParameter("codice_regione"));
			//			dati.put("codusl", (String) profile.getParameter("codice_usl"));
			//			dati.put("prestazioneFlussiSPR", prestazioneFlussiSPR);

			String user = ISASUtil.getValoreStringa(h, "user");
			String descrNome = "";
			if (ISASUtil.valida(user)) {
				String myselect = "select nvl(trim(cognome) || ' ', '') || nvl(nome,'') as nome from operatori where codice = '"
						+user+"'";
				ISASRecord dbr = dbc.readRecord(myselect);
				descrNome = ISASUtil.getValoreStringa(dbr, "nome");

			}
			hRet.put("user_desc", descrNome);
			String presidio = ISASUtil.getValoreStringa(h, "presidioFlussiSPR");
			String presidioDes = "";
			String codReg=ISASUtil.getValoreStringa(h, "codreg");
			String codUsl=ISASUtil.getValoreStringa(h, "codusl");

			if (ISASUtil.valida(presidio)) {
				String myselect = "select despres from presidi where codpres = '" +presidio+"' AND codreg = '" +codReg+
						"' AND codazsan = '" +codUsl+"' ";
				ISASRecord dbr = dbc.readRecord(myselect);
				presidioDes = ISASUtil.getValoreStringa(dbr, "despres");
			}
			hRet.put("presidioFlussiSPR_desc", presidioDes);
			String prestazione = ISASUtil.getValoreStringa(h, "prestazioneFlussiSPR");
			String prestazioneDes ="";
			if (ISASUtil.valida(prestazione)) {
				String myselect = "select * from prestaz where prest_cod = '" +prestazione+"' ";
				ISASRecord dbr = dbc.readRecord(myselect);
				presidioDes = ISASUtil.getValoreStringa(dbr, "prest_des");
			}
			hRet.put("prestazioneFlussiSPR_desc", prestazioneDes);

			System.out.println(punto + " dati che invio>"+hRet+"<\n");

			dbc.close();
			super.close(dbc);
			done = true;
			return hRet;
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_allCartella  ");
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



	private Hashtable caricaContatti(Hashtable h,String tipoOper,Hashtable hRet,ISASConnection dbc)
			throws  SQLException {
		ISASCursor dbcur=null;
		boolean done = false;
		try{
			String myselect =faiSelectContatti(tipoOper,h) ;
			if (!myselect.equals("")) {
				System.out.println("IntervEJB.caricaContatti - selectContatti=["+myselect+"]");
				dbcur=dbc.startCursor(myselect);
			} else
				return hRet;
			Vector vdbr = new Vector();
			Hashtable hPiano=new Hashtable() ;     
			while(dbcur.next()){
				//scorro i contatti
				ISASRecord dbrec=dbcur.getRecord();
				String desc=(String)dbrec.get("descr");
				// 14/07/10: x chi ha la configurazione percui non si utilizza la descr, ma il campo "tipocura" come x VCO ---
				if ((desc==null) || (desc.trim().equals(""))) {
					String desc_2 = (String)dbrec.get("descr_vco");
					if ((desc_2!=null) && !(desc_2.trim().equals("")))
						desc = ISASUtil.getDecode(dbc, "tab_voci", "tab_cod", "tab_val",
								"SAOADI", desc_2, "tab_descrizione");
				}
				// 14/07/10 ---
				if ((desc!=null && !(desc.equals(""))) &&
						dbrec.get("data_contatto")!=null) {
					String cart=((Integer)dbrec.get("n_cartella")).toString();
					String cont=((Integer)dbrec.get("n_contatto")).toString();
					String data=((java.sql.Date)dbrec.get("data_contatto")).toString();
					data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
					dbrec.put("kcombo",cart+"#"+cont);
					String servizio="";
					String descrizione = data+": ";
					//per la regione marche la descrizione contiene la data del contatto la cancello e ci mettol'id
					if ((gestore_casi .isUbicazRegMarche(dbc, h)).booleanValue()) {	
						desc = cont;
					}

					if (tipoOper.equals("01")) {
						if (dbrec.get("tipo_servizio") != null)
							if (((String)dbrec.get("tipo_servizio")).equals("1"))
								servizio = "T.S. Assistenza Domiciliare 1";
							else if(((String)dbrec.get("tipo_servizio")).equals("2"))
								servizio = "T.S. Sociale Prof./Segretariato Soc. 2";
						//28/11/2014 modificato per le Marche
						descrizione += desc.trim() +" "+servizio;
						//dbrec.put("descrizione",data+": "+desc.trim()+" "+servizio);
					}else{
						//28/11/2014 modificato per le Marche
						descrizione += desc.trim();
					}
					dbrec.put("descrizione", descrizione);

					//cambiare il campo descrizione.

					//Nel caso di servizio ="" per gli operatori 01 non lo inserisco
					if (!tipoOper.equals("01") || !servizio.equals(""))
						vdbr.addElement(dbrec);
					//vado a caricarmi il piano interventi per quel contatto-cartella-operatore
					Vector vPiano=CaricaPianoInterv(cart,cont,tipoOper,dbc);
					hPiano.put(cont,vPiano);
				}//fine if descrizione e data
			}//fine while
			hRet.put("pianoInterv",hPiano);
			hRet.put("contatti",vdbr);
			dbcur.close();
			dbcur=null;
			done=true;
			return hRet;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una CaricaContatti()  ");
		}finally{
			if(!done){
				try{
					if (dbcur!=null)dbcur.close();
				}catch(Exception e1){System.out.println(e1);}
			}
		}

	}


	public Hashtable query_anagra(myLogin mylogin, Hashtable h) throws Exception
	{
		System.out.println("IntervEJB: query_anagra HASH IN -> "+h.toString());
		String strNCartella = ((String)h.get("n_cartella"));
		String strCognome = "";
		String strNome = "";
		String strCodSesso = "";
		boolean done=false;
		java.sql.Date dateDataNascita = null;
		String strCodComuneNascita = "";
		String strDescrComuneNascita = "";
		ISASConnection mydbc = null;

		// 05/10/07 -----
		String mot_chius = "";
		java.sql.Date dtChius = null;
		// 05/10/07 -----

		// 14/11/07 ---
		String strCodComuneDom = "";
		String strDescrComuneDom = "";
		String strIndirizzoDom = "";
		// 14/11/07 ---

		// 08/10/08
		String codFisc = "";
		try{
			if (strNCartella.equals("")) 
			{
				h.put("cognome", "");
				h.put("nome", "");
				h.put("sesso_decod", "");
				h.put("data_nascita", "");
				h.put("cod_comune_nascita", "");
				h.put("comune_nascita_decod", "");

				// 05/10/07 -----
				h.put("motivo_chiusura", "");
				h.put("data_chiusura", "");
				// 05/10/07 -----

				// 14/11/07 ---
				h.put("dom_citta", "");
				h.put("desc_dom_citta", "");
				h.put("dom_indiriz", "");
				// 14/11/07 ---

				// 08/10/08
				h.put("cod_fisc", "");

				return h;
			}

			String selS = "SELECT c.cognome, c.nome," +
					" c.sesso," +
					" c.data_nasc," +
					" c.cod_com_nasc," +
					" c.motivo_chiusura, c.data_chiusura," + // 05/10/07
					" ac.dom_citta, ac.dom_indiriz," + // 14/11/07
					" c.cod_fisc" + // 08/10/08
					" FROM cartella c," +
					" anagra_c ac" + // 14/11/07
					" WHERE c.n_cartella = " + strNCartella +
					" AND ac.n_cartella = c.n_cartella" + // 14/11/07
					" AND ac.data_variazione IN (SELECT MAX(anagra_c.data_variazione)" +
					" FROM anagra_c WHERE anagra_c.n_cartella = ac.n_cartella)";
			System.out.println("IntervEJB: query_anagra SELECT -> "+selS);
			mydbc = super.logIn(mylogin);
			ISASRecord rec = mydbc.readRecord(selS);

			if (rec != null) 
			{
				System.out.println("IntervEJB: query_anagra ISASRecord -> "+rec.getHashtable().toString());		

				strCognome = ((String)rec.get("cognome")).trim();
				strNome = ((String)rec.get("nome")).trim();
				if (rec.get("sesso") != null)
					strCodSesso = ((String)rec.get("sesso")).trim();
				if (rec.get("data_nasc") != null)
					dateDataNascita = (java.sql.Date)rec.get("data_nasc");  
				if (rec.get("cod_com_nasc") != null) 
					strCodComuneNascita = (String) rec.get("cod_com_nasc");
				// 05/10/07 -----
				if (rec.get("motivo_chiusura") != null)
					mot_chius = "" + rec.get("motivo_chiusura");
				if (rec.get("data_chiusura") != null)
					dtChius = (java.sql.Date)rec.get("data_chiusura");
				// 05/10/07 -----
				// 14/11/07 ---
				if (rec.get("dom_citta") != null)
					strCodComuneDom = (String) rec.get("dom_citta");
				if (rec.get("dom_indiriz") != null)
					strIndirizzoDom = (String) rec.get("dom_indiriz");
				// 14/11/07 ---

				// 08/10/08
				if (rec.get("cod_fisc") != null)
					codFisc = (String) rec.get("cod_fisc");
			}

			h.put("cognome", strCognome);
			h.put("nome", strNome);
			h.put("sesso", strCodSesso);
			h.put("data_nascita", ((java.sql.Date)dateDataNascita)!=null?((java.sql.Date)dateDataNascita).toString():"");
			h.put("cod_comune_nascita", strCodComuneNascita);
			h.put("comune_nascita_decod", (String)util.getDecode(mydbc, "comuni", "codice", (String)h.get("cod_comune_nascita"), "descrizione"));
			// 05/10/07 -----
			h.put("motivo_chiusura", mot_chius);
			h.put("data_chiusura", (((java.sql.Date)dtChius)!=null?((java.sql.Date)dtChius).toString():""));
			// 05/10/07 -----

			// 14/11/07 -----
			h.put("dom_citta", strCodComuneDom);
			h.put("desc_dom_citta", (String)util.getDecode(mydbc, "comuni", "codice", (String)h.get("dom_citta"), "descrizione"));
			h.put("dom_indiriz", strIndirizzoDom);
			// 14/11/07 -----

			// 08/10/08
			h.put("cod_fisc", codFisc);

			done=true;
			mydbc.close();
			super.close(mydbc);
			return h;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una CaricaPianoInterv()  ");
		}finally{
			if(!done){
				try{
					if (mydbc!=null)
					{
						mydbc.close();
						super.close(mydbc);
					}
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}



	private Vector CaricaPianoInterv(String cartella,String contatto,String tipoOperatore,ISASConnection  dbc)
			throws 	SQLException{
		boolean done = false;
		Vector rit = new Vector();
		ISASCursor dbcur=null;
		ServerUtility su= new ServerUtility();
		try{
			//gb 07/05/07:	si sostituisce la tabella pianointerv con la tabella piano_accessi
			//		e relativi nomi dei campi
			//gb 07/05/07              String sel="SELECT DISTINCT pi_prest_cod,pi_data_inizio ,pi_data_fine,pi_prest_qta "+
			//gb 07/05/07                          "FROM pianointerv WHERE ";
			String sel="SELECT DISTINCT pi_prest_cod,pi_data_inizio ,pi_data_fine,pi_prest_qta "+
					"FROM piano_accessi WHERE ";
			String sel_where="";
			sel_where = su.addWhere(sel_where,su.REL_AND,"n_cartella",su.OP_EQ_NUM,cartella);
			//gb 07/05/07:	si sostituisce i nomi n_contatto e pi_tipo_oper con
			//		n_progetto e pa_tipo_oper rispettivamente
			//gb 07/05/07              sel_where = su.addWhere(sel_where,su.REL_AND,"n_contatto",su.OP_EQ_NUM,contatto);
			//gb 07/05/07              sel_where = su.addWhere(sel_where,su.REL_AND,"pi_tipo_oper",su.OP_EQ_STR,tipoOperatore);
			sel_where = su.addWhere(sel_where,su.REL_AND,"n_progetto",su.OP_EQ_NUM,contatto);
			sel_where = su.addWhere(sel_where,su.REL_AND,"pa_tipo_oper",su.OP_EQ_STR,tipoOperatore);
			sel=sel+sel_where;
			//System.out.println("CaricaPianoInterv su Interv: "+sel);
			dbcur = dbc.startCursor(sel);
			rit=dbcur.getAllRecord();
			dbcur.close();
			dbcur=null;
			done = true;
			return rit;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una CaricaPianoInterv()  ");
		}finally{
			if(!done){
				try{
					if (dbcur!=null)dbcur.close();
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}



	private String faiSelectContatti(String tipoOper,Hashtable h)
	{
		String  myselect="";
		ServerUtility su =new ServerUtility ();
		/*gb 07/05/07:	Per il caso "01" (assistenti sociali) si utilizza intervASEJB.java
		quindi in questo caso (cio� caso "01") non ci siamo mai.
    if (tipoOper.equals("01")){
           myselect=" SELECT c.descr_sociale descr,"+
                     " c.data_sociale data_contatto,c.data_chius_sociale data_chiusura,"+
                     " c.n_cartella,c.n_contatto,co.tipo_servizio "+
                     " FROM contsan c, contatti co "+
                     " WHERE  descr_sociale is not null"+
                     " AND c.n_cartella=co.n_cartella AND c.n_contatto=co.n_contatto ";
                     String TipServ="";
                      if (h.get("tipo_servizio")!=null)
                           TipServ=(String)h.get("tipo_servizio");
                      if (!TipServ.equals(""))
                          myselect += " AND co.tipo_servizio ="+ TipServ;
    } else
gb 07/05/07 *******/
		if (tipoOper.equals("02")){
			//gb 07/05/07:	infermieri: non si usa pi� contsan, si va direttamente
			//		nella tabella infermieri 'skinf'
			/*gb 07/05/07 *******
           myselect="SELECT descr_infer descr,"+
                    "data_infer data_contatto,data_chius_infer data_chiusura,"+
                    " n_contatto,n_cartella "+
                    " FROM contsan c "+
                    " WHERE descr_infer is not null";
gb 07/05/07 *******/
			/*** 11/07/06
//gb 07/05/07
           myselect="SELECT ski_descr_contatto descr,"+
                    " ski_data_apertura data_contatto," +
		    " ski_data_uscita data_chiusura,"+
                    " n_contatto, n_cartella "+
                    " FROM skinf c "+
                    " WHERE ski_descr_contatto is not null";
//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.ski_descr_contatto descr," +
					" c.ski_tipocura descr_vco," + // 14/07/10
					" c.ski_data_apertura data_contatto," +
					" c.ski_data_uscita data_chiusura" +
					" FROM skinf c" +
					" WHERE ((c.ski_descr_contatto IS NOT NULL)" +
					" OR (c.ski_tipocura IS NOT NULL))"; // 14/07/10
		} else if (tipoOper.equals("03")){
			//gb 07/05/07:	medici: non si usa pi� contsan, si va direttamente
			//		nella tabella medici 'skmedico'
			/*gb 07/05/07 *******
           myselect="SELECT descr_medico descr,"+
                    "data_medico data_contatto,data_chius_medico data_chiusura,"+
                    " n_contatto,n_cartella "+
                    " FROM contsan c "+
                    " WHERE descr_medico is not null";
gb 07/05/07 *******/
			/*** 11/07/06
//gb 07/05/07
           myselect="SELECT skm_descr_contatto descr,"+
                    " skm_data_apertura data_contatto," +
		    " skm_data_chiusura data_chiusura,"+
                    " n_contatto, n_cartella "+
                    " FROM skmedico c "+
                    " WHERE skm_descr_contatto is not null";
//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.skm_descr_contatto descr," +
					" c.skm_tipocura descr_vco," +
					" c.skm_data_apertura data_contatto," +
					" c.skm_data_chiusura data_chiusura"+
					" FROM skmedico c" +
					" WHERE (c.skm_descr_contatto IS NOT NULL  or c.skm_tipocura IS NOT NULL)";
		}else if (tipoOper.equals("04")){
			//gb 07/05/07:	fisioterapeuti: non si usa pi� contsan, si va direttamente
			//		nella tabella fisioterapeuti 'skfis'
			/*gb 07/05/07 *******
           myselect="SELECT descr_fisiot descr,"+
                    "data_fisiot data_contatto,data_chius_fisiot data_chiusura,"+
                    " n_contatto,n_cartella "+
                    " FROM contsan c "+
                    " WHERE descr_fisiot  is not null";
gb 07/05/07 *******/
			/*** 11/07/06
//gb 07/05/07
           myselect="SELECT skf_descr_contatto descr," +
                    " skf_data data_contatto," +
		    " skf_data_chiusura data_chiusura," +
                    " n_contatto, n_cartella " +
                    " FROM skfis c " +
                    " WHERE skf_descr_contatto is not null";
//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.skf_descr_contatto descr," +
					" c.skf_data data_contatto," +
					" c.skf_data_chiusura data_chiusura" +
					" FROM skfis c" +
					" WHERE c.skf_descr_contatto IS NOT NULL";
		} else if (tipoOper.equals("52")){ // 18/12/06: x l'oncologo si usano i campi dell'ostetrica
			//gb 07/05/07:	oncologi: non si usa pi� contsan, si va direttamente
			//		nella tabella oncologi 'skmedpal'
			/*gb 07/05/07 *******
           myselect="SELECT descr_ostetr descr,"+
                    "data_ostetr data_contatto,data_chius_ostetr data_chiusura,"+
                    " n_contatto,n_cartella"+
                    " FROM contsan c "+
                    " WHERE descr_ostetr is not null";
gb 07/05/07 *******/
			/*** 11/07/06
//gb 07/05/07
           myselect="SELECT skm_descr_contatto descr," +
                    " skm_data_apertura data_contatto," +
		    " skm_data_chiusura data_chiusura," +
                    " n_contatto, n_cartella" +
                    " FROM skmedpal c " +
                    " WHERE skm_descr_contatto is not null";
//gb 07/05/07: fine
			 ***/
			// 11/07/06
			myselect = "SELECT c.*, c.skm_descr_contatto descr," +
					" c.skm_data_apertura data_contatto," +
					" c.skm_data_chiusura data_chiusura" +
					" FROM skmedpal c" +
					" WHERE c.skm_descr_contatto IS NOT NULL";
		}
		else{
			myselect = "SELECT c.*, c.skfpg_descr_contatto descr," +
					" c.skfpg_data_apertura data_contatto," +
					" c.skfpg_data_uscita data_chiusura" +
					" FROM skfpg c" +
					" WHERE c.skfpg_descr_contatto IS NOT NULL"+
					" AND c.skfpg_tipo_operatore ='"+tipoOper+"'";
			/*
    	System.out.println("ERRORE--> SI STA CERCANDO DI INSERIRE UN CONTATTO PER UN OPERATORE CHE E'"+
    			"DI TIPO "+ tipoOper +" OPERATORI AMMESSI 01;02;03;04;52");
			 */
		}
		if (!myselect.equals(""))
			myselect = su.addWhere(myselect, su.REL_AND, "c.n_cartella",su.OP_EQ_NUM,(String)h.get("n_cartella"));
		return myselect;
	}


	public ISASRecord insert(myLogin mylogin,Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
		String punto = MIONOME + "insert ";
		stampa(punto + "dati che mi arrivano>"+h+"<");

		boolean done=false;
		String cartella=null;
		String anno=null;
		String cod_prest=null;
		String pre_tempo=null;
		int numero_interv = 0;
		ISASConnection dbc=null;
		String errore="";
		try {
			anno=(String)h.get("int_anno");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: mancano elementi della chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String chiave_tabella="INTERV"+anno;
			numero_interv=selectProgressivo(dbc,chiave_tabella);
			String cod_oper = (String) h.get("int_cod_oper");
			String select_oper = "Select tipo from operatori "+
					"where codice = '"+cod_oper+"'";
			ISASRecord dbr_oper = dbc.readRecord(select_oper);

			ISASRecord dbr=dbc.newRecord("interv");
			ISASRecord dbprest=dbc.newRecord("intpre");
			boolean num=false;
			gestisciFlag(dbc,dbr,true);
			Enumeration n=h.keys();

			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}

			//------------------COMUNE--------------------------------------------------------------
			ISASRecord w_dbr=null;
			String w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)h.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella "+
					" AND anagra_c.data_variazione<="+
					formatDate(dbc,""+h.get("int_data_prest"))+")"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			if (w_dbr==null)
			{System.out.println("DATO SPORCO ");
			w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)h.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella )"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			System.out.println("SELECT AGGIUNTA:"+w_select);
			}
			if (w_dbr==null)
			{
				errore="DATI ANAGRAFICA NON COMPLETI ";
				System.out.println("ERRORE INTERVENTI NON RIESCO A TROVARE IL COMUNE DI RESIDENZA:"+w_select);
			}
			/** 09/06/10
				if (w_dbr.get("dom_citta") != null && !(w_dbr.get("dom_citta").equals("")))
				                    dbr.put("int_cod_comune", w_dbr.get("dom_citta"));
				                else
				                    dbr.put("int_cod_comune", w_dbr.get("citta"));
				                if (w_dbr.get("dom_areadis") != null && !(w_dbr.get("dom_areadis").equals("")))
				                    dbr.put("int_cod_areadis", w_dbr.get("dom_areadis"));
				                else
				                    dbr.put("int_cod_areadis", w_dbr.get("areadis"));
			 **/
			// 09/06/10 m
			else
				settaDomResAss(dbr, w_dbr);
			//---------------------------------------------------------------------------------------------------

			dbr.put("int_tipo_oper", (String) dbr_oper.get("tipo"));
			dbr.put("int_contatore",new Integer(numero_interv));
			dbr.put("int_max_prest",new Integer(1));
			gestioneFlagFlussi(dbc,dbr,"I");

			// 14/03/13: per gli accessi STANDARD, si deve registrare il presidio presente sul contatto per stampe richieste da Pistoia
			dbr.put("int_cod_presidio_sk", leggiPresidioCont(dbc, dbr));

			dbc.writeRecord(dbr);
			String myselect="Select * from interv where "+
					"int_anno='"+anno+"' and "+
					"int_contatore="+(new Integer(numero_interv)).toString();
			dbr=dbc.readRecord(myselect);
			//inserimento prestazione in intpre
			dbprest.put("pre_anno",anno);
			dbprest.put("pre_contatore",new Integer(numero_interv));
			dbprest.put("pre_progr",new Integer(1));
			dbprest.put("pre_numero",((String)h.get("pre_numero")).toString());
			if (h.get("pre_cod_prest")!=null)
				dbprest.put("pre_cod_prest",(String)h.get("pre_cod_prest"));
			if (h.get("pre_des_prest")!=null)
				dbprest.put("pre_des_prest",(String)h.get("pre_des_prest"));
			if (h.get("pre_tempo")!=null)
				dbprest.put("pre_tempo",(String)h.get("pre_tempo"));
			if (h.get("pre_note")!=null)
				dbprest.put("pre_note",(String)h.get("pre_note"));
			if (h.get("pre_importo")!=null)
				dbprest.put("pre_importo",(String)h.get("pre_importo"));
			dbc.writeRecord(dbprest);

			String cogn=(String)h.get("cognome");
			dbr.put("cognome",cogn);
			cartella = ISASUtil.getValoreStringa(h, "int_cartella");
			String dataPrestazione = ISASUtil.getValoreStringa(h,"int_data_prest");
			cod_prest = ISASUtil.getValoreStringa(h, "pre_cod_prest");
			modificareContattoFisioterapista(dbc, cod_oper, cod_prest, cartella, dataPrestazione);

			// 26/08/09
			int risu = segnalaErogazione(dbc, dbr, true);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr;
		} catch(CariException ce){
			ce.setISASRecord(null);
			try	{
				System.out.println("IntervEJB.insert() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - "+ errore+ e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - "+ errore+ e);
			}
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new SQLException("Errore eseguendo una rollback() - "+ errore,e);
			}
			throw new SQLException("Errore eseguendo una insert() - "+ errore, e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}

	private void modificareContattoFisioterapista(ISASConnection dbc, String codiceOperatore, String codPrestazione, String n_cartella,
			String dataPrestazione) {
		boolean abilitazioneFlussiSPR = recuperaAblFlussiSPR(dbc, codiceOperatore);
		String punto = MIONOME + "modificareContattoFisioterapista ";
		if (abilitazioneFlussiSPR) {
			stampa(punto + " ho abilitazione flussi SPR");
			aggiornaContattoFisioterapistaFlussiSPR(dbc, codPrestazione, n_cartella, dataPrestazione);
		} else {
			stampa(punto + " No abilitazione flussi SPR");
		}
	}

	//	private void aggiornaContattoFisioterapistaFlussiSPR(ISASConnection dbc, String codPrestazione, String n_cartella,
	//			String dataPrestazione, boolean abilitazioneFlussiSPR) {
	//		if (abilitazioneFlussiSPR){
	//			aggiornaContattoFisioterapistaFlussiSPR(dbc, codPrestazione, n_cartella, dataPrestazione);
	//		}
	//	}

	private void aggiornaContattoFisioterapistaFlussiSPR(ISASConnection dbc, String codPrestazione, String n_cartella,
			String dataPrestazione) {
		String punto = MIONOME +"aggiornaContattoFisioterapistaFlussiSPR ";
		// non faccio applicare il controllo isas in quanto potrebbe essere un altro operatore a inserire o modificare le prestazioni
		String query = "select x.flag_inviato, x.n_contatto from skfis x where n_cartella = " + n_cartella + " and skf_data >= "
				+ dbc.formatDbDate(dataPrestazione) + " and ( (skf_data_chiusura is null)  or ( skf_data_chiusura >= "
				+ dbc.formatDbDate(dataPrestazione) + " ) ) and cod_prestaz = '" + codPrestazione + "' ";
		stampa(punto + " Query>" + query);
		ISASRecord dbrSkFis;
		try {
			dbrSkFis = dbc.readRecord(query);
			if (dbrSkFis != null) {
				// Non effettuo nessuna controllo sulla data chiusura del contatto fisioterapista
				String flagInviato = ISASUtil.getValoreStringa(dbrSkFis, "flag_inviato");
				if (ISASUtil.valida(flagInviato) && flagInviato.equals(CONSTANTS_FLAG_INVIATO_INVIATO)) {
					stampa(punto + " flusso e' stato inviato, lo marco come modifica " + flagInviato + "< ");
					String contatto = ISASUtil.getValoreStringa(dbrSkFis, "n_contatto");
					try {
						String updateSkfis = "UPDATE skfis set flag_inviato = '" + CONSTANTS_FLAG_INVIATO_VARIATO
								+ "' where n_cartella = " + n_cartella + " and n_contatto = " + contatto;
						stampa(" Aggiorno i dati " + updateSkfis);
						dbc.execSQL(updateSkfis);
					} catch (Exception e) {
						stampa(punto + "-->errore in Aggiorna Flag " + e + "<\n");
					}
				} else {
					stampa(punto + " flusso spr si trova o nello stato di inserimento oppure variazione" + flagInviato + "< ");
				}
			} else {
				stampa(punto + " \t record non trovato");
			}

		} catch (DBSQLException e) {
			e.printStackTrace();
		} catch (DBMisuseException e) {
			e.printStackTrace();
		} catch (ISASPermissionDeniedException e) {
			e.printStackTrace();
		} catch (ISASMisuseException e) {
			e.printStackTrace();
		}
	}

	private boolean recuperaAblFlussiSPR(ISASConnection dbc, String codiceOperatore) {
		String punto = MIONOME + "recuperaAblFlussiSPR ";
		boolean ablFlussiSPR = false;
		stampa(punto + " Controllo rec ablFlussiSPR>" + ablFlussiSPR + "<CONSTANTS_RICERCA_COMUNE_UVM>" + CONSTANTS_ABL_FLUSSI_SPR + "<");
		try {
			EveUtils eveUtl = new EveUtils();
			String[] datiCheInvio = new String[] { CONSTANTS_ABL_FLUSSI_SPR };

			Hashtable conf = eveUtl.leggiConf(dbc, codiceOperatore, datiCheInvio);
			stampa(punto + "dati recuperati>" + (conf != null ? conf + "" : "no dati" + ""));
			String valoreletto = ISASUtil.getValoreStringa(conf, CONSTANTS_ABL_FLUSSI_SPR);
			ablFlussiSPR = (ISASUtil.valida(valoreletto) && valoreletto.equalsIgnoreCase("SI"));
			stampa(punto + "ablFlussiSPR>" + valoreletto + "< ablFlussiSPR>" + ablFlussiSPR + "<");
		} catch (Exception e) {
			stampa(punto + "\t Errore nel recuperare abilitazione per il codice operatore>" + codiceOperatore + "<");
			e.printStackTrace();
		}
		stampa(punto + "fine: ablFlussiSPR>" + ablFlussiSPR + "<");
		return ablFlussiSPR;
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);

	}

	public ISASRecord update_interv(myLogin mylogin,Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
		boolean done=false;
		String int_anno=null;
		String int_contatore=null;
		ISASConnection dbc=null;
		String errore="";
		try{
			int_anno=(String)h.get("int_anno");
			int_contatore=(String)h.get("int_contatore");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			System.out.println("Update_interv: "+h.toString());
			String myselect="Select * from interv where "+
					"int_anno='"+int_anno+"' and "+
					"int_contatore="+int_contatore;
			System.out.println("SELECT: "+myselect);
			ISASRecord dbr=dbc.readRecord(myselect);
			System.out.println("DBR PRIMA: "+dbr.getHashtable().toString());

			//Giulia Brogi
			dbr = SettaCampiVuoti(dbr);
			//				if (dbr.get("flag_sent")==null || dbr.get("flag_sent").toString().equals("")|| dbr.get("flag_sent").toString().equals("0"))
			//				h.put("flag_sent","0");
			//				else if (dbr.get("flag_sent").toString().equals("1")||
			//				dbr.get("flag_sent").toString().equals("2")||
			//				dbr.get("flag_sent").toString().equals("3")) h.put("flag_sent","2");

			gestisciFlag(dbc,dbr,false);

			Enumeration n=h.keys();
			System.out.println("Enumeration in update_interv: "+h.toString());
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				dbr.put(e,h.get(e));
			}

			//------------------COMUNE--------------------------------------------------------------
			ISASRecord w_dbr=null;
			String w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)h.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella "+
					" AND anagra_c.data_variazione<="+
					formatDate(dbc,""+h.get("int_data_prest"))+")"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			if (w_dbr==null)
			{System.out.println("DATO SPORCO ");
			w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)h.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella )"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			System.out.println("SELECT AGGIUNTA "+ w_select);
			}
			if (w_dbr==null)
			{
				errore="DATI ANAGRAFICA NON COMPLETI ";
				System.out.println("ERRORE INTERVENTI NON RIESCO A TROVARE IL COMUNE DI RESIDENZA:"+w_select);
			}
			/** 09/06/10
		if (w_dbr.get("dom_citta") != null && !(w_dbr.get("dom_citta").equals("")))
                    dbr.put("int_cod_comune", w_dbr.get("dom_citta"));
                else
                    dbr.put("int_cod_comune", w_dbr.get("citta"));
                if (w_dbr.get("dom_areadis") != null && !(w_dbr.get("dom_areadis").equals("")))
                    dbr.put("int_cod_areadis", w_dbr.get("dom_areadis"));
                else
                    dbr.put("int_cod_areadis", w_dbr.get("areadis"));
			 **/
			// 09/06/10 m
			else
				settaDomResAss(dbr, w_dbr);
			//---------------------------------------------------------------------------------------------------

			gestioneFlagFlussi(dbc,dbr,"M");

			// 14/03/13: per gli accessi STANDARD, si deve registrare il presidio presente sul contatto per stampe richieste da Pistoia
			dbr.put("int_cod_presidio_sk", leggiPresidioCont(dbc, dbr));				

			dbc.writeRecord(dbr);
			ISASRecord dbr2=dbc.readRecord(myselect);
			String cogn=(String)h.get("cognome");
			dbr2.put("cognome",cogn);
			dbr2.put("descrizione",""+dbr.get("int_cartella")+"#"+
					""+dbr.get("int_contatto"))	;

			// 26/08/09
			int risu = segnalaErogazione(dbc, dbr, false);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			return dbr2;
		} catch(CariException ce){
			ce.setISASRecord(null);
			try	{
				System.out.println("IntervEJB.update() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e2);
			}
			throw new SQLException("Errore eseguendo una update() - "+errore+  e1);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}


	private ISASRecord SettaCampiVuoti(ISASRecord dbr){
		try{
			dbr.put("int_anno",null);
			dbr.put("int_contatore",null);
			dbr.put("int_cartella",null);
			dbr.put("int_contatto",null);
			dbr.put("int_data_prest",null);
			dbr.put("int_tipo_prest",null);
			dbr.put("int_cod_oper",null);
			dbr.put("int_qual_oper",null);
			dbr.put("int_tipo_oper",null);
			dbr.put("int_cod_areadis",null);
			dbr.put("int_cod_comune",null);
			// 09/06/10 --
			dbr.put("int_cod_res_comune", null);
			dbr.put("int_cod_res_areadis", null);
			// 09/06/10 --
			dbr.put("int_ambdom",null);
			dbr.put("int_codpres",null);
			dbr.put("int_ora_in",null);
			dbr.put("int_ora_out",null);
			System.out.println("STO SBIANCANDO TEMPO!!!!!!!!!!!");
			dbr.put("int_tempo",null);
			System.out.println("STO SBIANCANDO TEMPOGO!!!!!!!!!!!");
			dbr.put("int_tempogo",null);
			dbr.put("int_note",null);
			// 14/03/13
			dbr.put("int_cod_presidio_sk", null);		
			//dbr.put("int_max_prest",null);
		}catch(Exception e){}
		return dbr;
	}


	public void deleteAll(myLogin mylogin,Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
		boolean done=false;
		ISASConnection dbc=null;
		ISASCursor dbcur=null;
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String anno=(String)h.get("int_anno");
			String contatore=(String)h.get("int_contatore");  
			String selint="SELECT * FROM interv WHERE"+
					" int_anno='"+anno+"'"+
					" AND int_contatore="+contatore;
			ISASRecord dbr=dbc.readRecord(selint);

			if (dbr.get("flag_sent")==null || dbr.get("flag_sent").toString().equals("0")|| dbr.get("flag_sent").toString().equals("")){
				gestioneFlagFlussi(dbc,dbr,"D");
				dbc.deleteRecord(dbr);
			}
			else throw new CariException("Impossibile cancellare l'informazione perché risulta già inviata tramite flussi ministeriali.");

			//VADO A CANCELLARE LE PRESTAZIONI
			//ASSOCIATE ALL'INTERVENTO
			String select="SELECT * FROM intpre WHERE "+
					"pre_anno='"+anno+"' AND pre_contatore='"+contatore+"'";
			dbcur=dbc.startCursor(select);
			Vector vdbr=dbcur.getAllRecord();

			ISASRecord dbprest=null;
			for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
			{
				dbprest=(ISASRecord)senum.nextElement();
				//CANCELLAZIONE ELEMENTI DA INTPRE
				delete(dbc,dbprest.getHashtable());
			}

			dbc.commitTransaction();
			dbcur.close();
			dbcur=null;
			dbc.close();
			super.close(dbc);
			done=true;
		}catch(CariException e){
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}
		catch(DBRecordChangedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			try{
				dbc.rollbackTransaction();
			}catch(Exception e2){
				throw new SQLException("Errore eseguendo una rollback() - "+  e1);
			}
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}finally{
			if(!done){
				try{
					if (dbcur!=null)dbcur.close();
					dbc.close();
					super.close(dbc);
				}catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	private void gestioneFlagFlussi(ISASConnection dbc,ISASRecord dbr,String operaz)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
			{
		try{
			if (dbr.get("int_flag_flussi")!=null && !((String)dbr.get("int_flag_flussi")).equals("0"))
			{
				if(operaz.equals("D")){//occorre caricare il rec su tabella fdoc_sinss_canc
					ISASRecord dbrDel=dbc.newRecord("fdoc_sinss_canc");
					dbrDel.put("int_anno", (String)dbr.get("int_anno"));
					dbrDel.put("int_contatore",(Integer)dbr.get("int_contatore"));
					//((java.sql.Date)dbcon.get("int_data_prest")).toString()
					dbrDel.put("int_data_prest",dbr.get("int_data_prest"));
					dbc.writeRecord(dbrDel);
				}else if(operaz.equals("M")){//occorre modificare il flag flussi
					dbr.put("int_flag_flussi","3");
				}else if(operaz.equals("I")){//occorre modificare il flag flussi
					dbr.put("int_flag_flussi","0");
				}
			}

		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}
			}
	private void delete(ISASConnection dbc,Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
		try{
			System.out.println("Hash "+h.toString());
			String sel="SELECT * FROM INTPRE WHERE"+
					" pre_anno='"+(String)h.get("pre_anno")+
					"' AND pre_contatore="+(Integer)h.get("pre_contatore")+
					" AND pre_progr="+(Integer)h.get("pre_progr");
			ISASRecord dbr=dbc.readRecord(sel);
			dbc.deleteRecord(dbr);
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una delete() - "+  e1);
		}
	}

	//-------------------------------------------------------------------------
	private Hashtable DeleteDettagli(ISASConnection dbc,String anno,String contatore)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
			{   /*in hPrestaz devo salvarmi il codice delle prestazioni che avevo
      Questo perch� devo aggiornare int_max_prest di interv.
      Al vecchio valore di int_max_prest devo aggiungere il numero delle nuove prestazioni inserite
      Arrivando tutto insieme non so se quelle prestazioni esistevano gia e quindi erano conteggiate
      o no.Ricordo che nel caso di cancellazione di una prestazione non viene sottratto uno da int_max_prest
			 */
		Hashtable hPrestaz=new Hashtable ();
		try{
			String myselect="SELECT * from intpre where pre_anno ='" +anno+"'"+
					"AND pre_contatore="+contatore;
			ISASCursor dbcur=dbc.startCursor(myselect) ;
			while(dbcur.next())
			{
				ISASRecord dbrec=(ISASRecord)dbcur.getRecord() ;
				if (dbrec!=null && dbrec.get("pre_progr")!=null)
				{
					String selectCal=myselect + " AND pre_progr=" +(Integer)dbrec.get("pre_progr");
					ISASRecord dbprest=dbc.readRecord(selectCal);
					if (dbprest!=null)
					{     String codice=""+dbprest.get("pre_cod_prest");
					hPrestaz.put(codice,"");
					dbc.deleteRecord(dbprest);
					}
				}
			}
			dbcur.close();
			dbcur=null;
			return hPrestaz;
		}catch(DBRecordChangedException e){
			e.printStackTrace();
			throw e;
		}catch(ISASPermissionDeniedException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e1){
			System.out.println(e1);
			throw new SQLException("DEBUG:Interv.deleteDettagli()-->Errore eseguendo una deleteDettagli() - "+  e1);
		}

			}

	public Integer  insertVector(myLogin mylogin,Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
		String methodName = "insertVector";
		String punto = MIONOME + "insertVector ";
		stampa(punto + " inizio con dati>"+h+"<");
		boolean done=false;
		String cartella =null;
		String anno=null;
		String cod_prest=null;
		String pre_tempo=null;
		int numero_interv = 0;
		ISASConnection dbc=null;
		String errore="";
		boolean abilitazioneFlussiSPR = false;
		try {
			anno=(String)h.get("int_anno");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("DEBUG:Interv.insertVector()-->Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			String chiave_tabella="INTERV"+anno;
			System.out.println("DBTYPE = "+super.getDbType(dbc));
			numero_interv=selectProgressivo(dbc,chiave_tabella);
			String cod_oper = (String) h.get("int_cod_oper");
			String select_oper = "Select tipo from operatori "+
					"where codice = '"+cod_oper+"'";
			abilitazioneFlussiSPR = recuperaAblFlussiSPR(dbc, cod_oper);

			ISASRecord dbr_oper = dbc.readRecord(select_oper);
			ISASRecord dbr=dbc.newRecord("interv");
			//          ISASRecord dbprest=dbc.newRecord("intpre");
			h.put("flag_sent","0");
			Enumeration n=h.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				if (!e.equals("vettore"))
					dbr.put(e,h.get(e));
			}
			//------------------COMUNE--------------------------------------------------------------
			ISASRecord w_dbr=null;
			String w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)h.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella "+
					" AND anagra_c.data_variazione<="+
					formatDate(dbc,""+h.get("int_data_prest"))+")"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			if (w_dbr==null)
			{System.out.println("DATO SPORCO ");
			w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)h.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella )"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			System.out.println("SELECT AGGIUNTA:"+w_select);
			}
			if (w_dbr==null)
			{
				errore="DATI ANAGRAFICA NON COMPLETI ";
				System.out.println("ERRORE INTERVENTI NON RIESCO A TROVARE IL COMUNE DI RESIDENZA:"+w_select);
			}
			/** 09/06/10
            if (w_dbr.get("dom_citta") != null && !(w_dbr.get("dom_citta").equals("")))
                dbr.put("int_cod_comune", w_dbr.get("dom_citta"));
            else
                dbr.put("int_cod_comune", w_dbr.get("citta"));
            if (w_dbr.get("dom_areadis") != null && !(w_dbr.get("dom_areadis").equals("")))
                dbr.put("int_cod_areadis", w_dbr.get("dom_areadis"));
            else
                dbr.put("int_cod_areadis", w_dbr.get("areadis"));
			 **/
			// 09/06/10 m
			else
				settaDomResAss(dbr, w_dbr);
			//---------------------------------------------------------------------------------------------------

			dbr.put("int_tipo_oper", (String) dbr_oper.get("tipo"));
			dbr.put("int_contatore",new Integer(numero_interv));
			Vector hv=(Vector)h.get("vettore");
			if(hv.size()>0){
				dbr.put("int_max_prest",new Integer(hv.size()));
			}else{
				dbr.put("int_max_prest",new Integer(1));
			}
			gestioneFlagFlussi(dbc,dbr,"I");

			// 14/03/13: per gli accessi STANDARD, si deve registrare il presidio presente sul contatto per stampe richieste da Pistoia
			dbr.put("int_cod_presidio_sk", leggiPresidioCont(dbc, dbr));

			System.out.println("IntervEJB.insertVector() - SCRIVO-->"+ dbr.getHashtable().toString());
			dbc.writeRecord(dbr);

			cartella = ISASUtil.getValoreStringa(h,"int_cartella");
			String dataPrestazione = ISASUtil.getValoreStringa(h,"int_data_prest");

			Hashtable hPrestaz= new Hashtable();
			int num=InsertDettagli(dbc,anno,""+numero_interv,hv,hPrestaz, cartella, dataPrestazione,abilitazioneFlussiSPR);

			// 26/08/09
			int risu = segnalaErogazione(dbc, dbr, true);

			dbc.commitTransaction();
			dbc.close();
			super.close(dbc);
			done=true;
			return  new Integer(numero_interv);
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}finally {
			logout_nothrow(methodName, dbc);
		}
	}
	public ISASRecord updateVector(myLogin mylogin,Hashtable h)
			throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
		String punto = MIONOME + "updateVector ";
		stampa(punto + " dati che ricevo>" +h+"<");
		boolean done=false;
		String anno=null;
		String contatore=null;
		String errore="";
		ISASConnection dbc=null;
		ServerUtility su = new ServerUtility ();
		String codiceOperatore = ISASUtil.getValoreStringa(h, "int_cod_oper");
		boolean abilitazioneFlussiSPR = false;
		try {
			anno=(String)h.get("int_anno");
			contatore=(String)h.get("int_contatore");
		}catch (Exception e){
			e.printStackTrace();
			throw new SQLException("DEBUG:Interv.updateVector()-->Errore: manca la chiave primaria");
		}
		try{
			dbc=super.logIn(mylogin);
			dbc.startTransaction();
			//leggo interv e aggiorno
			String myselect="Select * from interv where "+
					"int_anno='"+anno+"' and "+
					"int_contatore="+contatore;
			ISASRecord dbr=dbc.readRecord(myselect);
			dbr = SettaCampiVuoti(dbr);
			// Simone 22/07/2015 il flag da inserire dipende dal flag precedente
			//h.put("flag_sent","2");
			Enumeration n=h.keys();
			while(n.hasMoreElements()){
				String e=(String)n.nextElement();
				if (!e.equals("vettore"))
					dbr.put(e,h.get(e));
			}
			//aggiorno la citta
			ISASRecord w_dbr=null;
			String w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)dbr.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella "+
					" AND anagra_c.data_variazione<="+
					formatDate(dbc,""+dbr.get("int_data_prest"))+")"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			if (w_dbr==null)
			{System.out.println("DATO SPORCO ");
			w_select = "SELECT a.citta,a.dom_citta,a.areadis,a.dom_areadis "+
					"FROM anagra_c a,cartella  WHERE "+
					"a.n_cartella="+(String)dbr.get("int_cartella")+
					" AND a.data_variazione IN "+
					" (SELECT MAX(data_variazione) from anagra_c WHERE "+
					"anagra_c.n_cartella=a.n_cartella )"+
					" AND cartella.n_cartella=a.n_cartella";
			w_dbr=dbc.readRecord(w_select);
			System.out.println("SELECT AGGIUNTA "+ w_select);
			}
			if (w_dbr==null)
			{
				errore="DATI ANAGRAFICA NON COMPLETI ";
				System.out.println("ERRORE INTERVENTI NON RIESCO A TROVARE IL COMUNE DI RESIDENZA:"+w_select);
			}
			/** 09/06/10
          if (w_dbr.get("dom_citta") != null && !(w_dbr.get("dom_citta").equals("")))
              dbr.put("int_cod_comune", w_dbr.get("dom_citta"));
          else
              dbr.put("int_cod_comune", w_dbr.get("citta"));
          if (w_dbr.get("dom_areadis") != null && !(w_dbr.get("dom_areadis").equals("")))
              dbr.put("int_cod_areadis", w_dbr.get("dom_areadis"));
          else
              dbr.put("int_cod_areadis", w_dbr.get("areadis"));
			 **/
			// 09/06/10 m
			else
				settaDomResAss(dbr, w_dbr);

			gestioneFlagFlussi(dbc,dbr,"M");

			gestisciFlag(dbc, dbr, false);

			String cartella = ISASUtil.getValoreStringa(h,"int_cartella");
			String dataPrestazione = ISASUtil.getValoreStringa(h,"int_data_prest");

			abilitazioneFlussiSPR = recuperaAblFlussiSPR(dbc, codiceOperatore);
			Hashtable hPrestaz= new Hashtable();
			hPrestaz=DeleteDettagli(dbc,anno,contatore);
			Vector hv=(Vector)h.get("vettore");
			int num=InsertDettagli(dbc,anno,contatore,hv,hPrestaz, cartella, dataPrestazione, abilitazioneFlussiSPR);
			int int_maxVecchio=0;
			if(dbr.get("int_max_prest")!=null)
			{
				int_maxVecchio=Integer.parseInt(""+dbr.get("int_max_prest"));
			}
			dbr.put("int_max_prest", new Integer(num+int_maxVecchio));

			// 14/03/13: per gli accessi STANDARD, si deve registrare il presidio presente sul contatto per stampe richieste da Pistoia
			dbr.put("int_cod_presidio_sk", leggiPresidioCont(dbc, dbr));

			dbc.writeRecord(dbr);

			// 26/08/09
			int risu = segnalaErogazione(dbc, dbr, false);

			dbc.commitTransaction();
			ISASRecord dbrletto=queryKey(mylogin,dbr.getHashtable());
			dbc.close();
			super.close(dbc);
			done=true;
			return dbrletto;
		} catch(CariException ce){
			ce.setISASRecord(null);
			try	{
				System.out.println("IntervEJB.updateVector() => ROLLBACK");
				dbc.rollbackTransaction();
			}catch(Exception e1){
				throw new CariException("Errore eseguendo la rollback() - " +  e1);
			}
			throw ce;
		}catch(DBRecordChangedException e)
		{
			e.printStackTrace();
			try
			{
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e1);
			}
			throw e;
		}
		catch(ISASPermissionDeniedException e)
		{
			e.printStackTrace();
			try
			{
				dbc.rollbackTransaction();
			}
			catch(Exception e1)
			{
				throw new ISASPermissionDeniedException("Errore eseguendo una rollback() - "+  e);
			}
			throw e;
		}
		catch(Exception e1)
		{
			System.out.println(e1);
			throw new SQLException("Errore eseguendo una update() - ",  e1);
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
				catch(Exception e2){System.out.println(e2);}
			}
		}
	}
	private int InsertDettagli(ISASConnection dbc,String anno,String contatore,Vector hv1,Hashtable hPrest,
			String cartella, String dataPrestazione, boolean abilitazioneFlussiSPR)
					throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
					{
		int numero=0;
		String codPrestazione = "";
		try{
			Hashtable h = new Hashtable();
			ISASRecord dbr = null;
			int i=0;
			for(Enumeration enume=hv1.elements(); enume.hasMoreElements(); )
			{

				h=(Hashtable)enume.nextElement();
				Enumeration n=h.keys();
				dbr=dbc.newRecord("intpre");
				dbr.put("pre_anno",anno);
				dbr.put("pre_contatore",new Integer(contatore));
				Integer progr = new Integer(i++);

				while(n.hasMoreElements())
				{
					String elem=(String)n.nextElement();
					dbr.put(elem,h.get(elem));
					//controllo che sia una prestazione nuova
					if(elem.equals("pre_cod_prest")){
						codPrestazione=ISASUtil.getValoreStringa(h, elem);
						if ( hPrest==null || hPrest.get(h.get(elem))!=null){
							numero++;
						}
					}
				}
				dbr.put("pre_progr", progr);
				dbc.writeRecord(dbr);
				System.out.println("SCRITTO INTPRE");
				if(abilitazioneFlussiSPR){
					aggiornaContattoFisioterapistaFlussiSPR(dbc, codPrestazione, cartella, dataPrestazione);
				}
			}
			return numero;
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
			throw new SQLException("Errore eseguendo una InsertDettagli() - ",  e);
		}
					}
	//-------------------------------------------------------------------------


	// 28/10/09 m.: x gestione Flussi AD-RSA
	private int segnalaErogazione(ISASConnection mydbc, ISASRecord mydbr, boolean isInsert) throws Exception, CariException
	{
		// 16/07/10: si comunicano solo le EROGAZIONI DOMICILIARI
		if ((mydbr.get("int_ambdom") != null) && (!"D".equals((String)mydbr.get("int_ambdom")))) {
			System.out.println("--- IntervEJB.segnalaErogazione: Accesso NON DOMICILIARE quindi NON si segnala EROGAZIONE ---");
			return 1;
		}

		Hashtable h_par = new Hashtable();
		h_par.put("int_cartella", mydbr.get("int_cartella"));
		h_par.put("int_data_prest", mydbr.get("int_data_prest"));
		h_par.put("int_anno", mydbr.get("int_anno"));
		h_par.put("int_contatore", mydbr.get("int_contatore"));
		h_par.put("int_tipo_oper", mydbr.get("int_tipo_oper"));
		h_par.put("int_cod_oper", mydbr.get("int_cod_oper"));
		// 02/08/11
		h_par.put("int_qual_oper", mydbr.get("int_qual_oper"));

		int risu = -1;
		if (isInsert)
			risu = gestore_erog.insert(mydbc, h_par);
		else
			risu = gestore_erog.update(mydbc, h_par);

		if (risu <= 0)
			System.out.println("::: IntervEJB.segnalaErogazione - NON segnalata EROGAZIONE! :::");
		return risu;
	}


	// 06/09/10 m.
	private void settaDomResAss(ISASRecord mydbr, ISASRecord dbrAnag) throws Exception
	{
		// comune dom
		mydbr.put("int_cod_comune", getFld_1_Or_2(dbrAnag, "dom_citta", "citta"));
		// comune res
		mydbr.put("int_cod_res_comune", getFld_1_Or_2(dbrAnag, "citta", "dom_citta"));
		// areadis dom
		mydbr.put("int_cod_areadis", getFld_1_Or_2(dbrAnag, "dom_areadis", "areadis"));
		// areadis res
		mydbr.put("int_cod_res_areadis", getFld_1_Or_2(dbrAnag, "areadis", "dom_areadis"));
	}

	// 06/09/10 m.
	private String getFld_1_Or_2(ISASRecord mydbr, String nmFld_1, String nmFld_2) throws Exception
	{
		String codDefault = "000000";
		if (mydbr == null)
			return codDefault;

		if ((mydbr.get(nmFld_1) != null) && (!(((String)mydbr.get(nmFld_1)).trim().equals(""))))
			return (String)mydbr.get(nmFld_1);
		if ((mydbr.get(nmFld_2) != null) && (!(((String)mydbr.get(nmFld_2)).trim().equals(""))))
			return (String)mydbr.get(nmFld_2);
		return codDefault;
	}

	public Hashtable query_contatto(myLogin mylogin, Hashtable h) throws  SQLException, ISASPermissionDeniedException
	{
		boolean done = false;
		ISASConnection dbc = null;
		ISASRecord dbr = null;


		String n_cartella = (h.get("n_cartella")!=null?(String)h.get("n_cartella"):"");
		String n_contatto = (h.get("n_contatto")!=null?(String)h.get("n_contatto"):"");
		if (n_cartella.equals("") || n_contatto.equals(""))
		{
			System.out.println("IntervEJB: Mancano dati per query su progetto e skinf");
			return null;
		}
		else
			try{ 
				dbc=super.logIn(mylogin);

				String myselect = "SELECT sk.*," +
						" sk.n_contatto n_progetto," +
						" sk.ski_data_apertura ap_data_apertura," +
						" sk.ski_data_uscita ap_data_chiusura," +
						" sk.ski_descr_contatto motivo_decod," +
						" p.pr_data," +
						" p.pr_data_chiusura," +
						" p.pr_motivo_val_ap," +
						" p.pr_motivi_val_ch," +
						" p.pr_data_carico" +
						" FROM skinf sk," +
						" progetto p," + 
						" progetto_cont pc" +
						" WHERE pc.prc_tipo_op = '02'" +
						" AND pc.n_cartella = sk.n_cartella" +
						" AND pc.prc_n_contatto = sk.n_contatto" +
						" AND p.n_cartella = pc.n_cartella" +
						" AND p.pr_data = pc.pr_data" +
						" AND sk.n_contatto = "+ n_contatto +
						" AND sk.n_cartella = "+ n_cartella;

				System.out.println("IntervEJB/query_contatto: "+myselect);

				dbr = dbc.readRecord(myselect); // 14/04/08


				// Decodifica in tutti gli ISASRecord del Vector
				decodificaQueryContattiApertiInfo(dbc, dbr);

				dbc.close();
				super.close(dbc);
				done=true;
				return dbr.getHashtable();
			} catch(Exception e)  {
				e.printStackTrace();
				throw new SQLException("Errore eseguendo la query_progettiAperti()  ");
			}
		finally  {
			if(!done){
				try{
					if (dbc != null)
						dbc.close();
					super.close(dbc);
				}catch(Exception e1)
				{System.out.println(e1);}
			}
		}
	}

	private void decodificaQueryContattiApertiInfo(ISASConnection mydbc,ISASRecord dbr) throws Exception
	{				
		dbr.put("desc_val_ap", (String)util.getDecode(mydbc, "tab_voci", "tab_cod", "tab_val", "PRMOAP", (String)dbr.get("pr_motivo_val_ap"), "tab_descrizione"));
		dbr.put("desc_val_ch", (String)util.getDecode(mydbc, "tab_voci", "tab_cod", "tab_val", "PRMOCH", (String)dbr.get("pr_motivi_val_ch"), "tab_descrizione"));
		dbr.put("tipo_utente_decod", (String)util.getDecode(mydbc, "tipute_s", "codice", (String)dbr.get("ski_tipout"), "descrizione"));
	}


	/**
	 * elisa b 27/09/11:
	 * Controlla che alla data di registrazione dell'accesso esista una presa in
	 * carico attiva
	 */
	public Boolean controlloEsistenzaPresaInCarico(myLogin mylogin, Hashtable h) throws SQLException, CariException{
		boolean done = false;
		boolean esito = false;
		String msg = "La data della prestazione non appartiene ad un periodo" +
				" in cui l'assistito e' preso in carico. Impossibile continuare.";
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			String cartella = h.get("n_cartella").toString();
			String dtPrest = h.get("data_prest").toString();

			String sel = "SELECT p.n_cartella" +
					" FROM rl_presacarico p JOIN caso c" +
					" ON p.n_cartella = c.n_cartella" +
					" AND p.pr_data = c.pr_data" +
					" AND p.id_caso = c.id_caso" +
					" WHERE p.n_cartella = " + cartella +
					" AND p.dt_presa_carico <= " + dbc.formatDbDate(dtPrest) +
					" AND (" +
					"c.dt_conclusione IS NULL OR c.dt_conclusione >= " + dbc.formatDbDate(dtPrest) +
					")";
			ISASRecord dbr = dbc.readRecord(sel);
			if(dbr != null)
				esito = true;

			done = true;
			dbc.close();
			super.close(dbc);
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo controlloEsistenzaSospensione " + e);
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e2) {
					System.out.println(e2);
				}
			}
		}

		if(!esito)
			throw new CariException(msg);

		return new Boolean(esito);
	}

	// 14/03/13: per gli accessi STANDARD, si deve registrare il presidio presente sul contatto per stampe richieste da Pistoia
	private String leggiPresidioCont(ISASConnection dbc, ISASRecord dbr) throws Exception
	{	
		String codPres = null;
		String nmTab = "skfpg";
		String nmFldContSk = "n_contatto";
		String nmFldContInterv = "int_contatto";
		String nmFldPres = "skfpg_cod_presidio";

		String sel = "SELECT * FROM #NM_TAB# WHERE n_cartella = #N_CARTELLA# AND #NM_FLD_CONTATTO# = #N_CONTATTO#";

		switch (Integer.parseInt(dbr.get("int_tipo_oper").toString())) {
		case 1: // ASS SOC
			nmTab = "ass_progetto";
			nmFldContSk = "n_progetto";
			nmFldContInterv = "n_progetto";
			nmFldPres = "ap_ass_ref_presidio";
			break;
		case 2: // INF
			nmTab = "skinf";
			nmFldPres = "ski_cod_presidio";
			break;
		case 3: // MEDICO
			nmTab = "skmedico";
			nmFldPres = "skm_cod_presidio";
			break;
		case 4: // FISIOTERAP
			nmTab = "skfis";
			nmFldPres = "skf_cod_presidio";
			break;
		case 52: // MEDICO CURE PALLIATIVE
			nmTab = "skmedpal";
			nmFldPres = "skm_cod_presidio";
			break;	
		default:
			LOG.info("leggiPresidioCont: tipo operatore NON DEFINITO!!");
		}

		if ((dbr.get(nmFldContInterv) != null) && (Integer.parseInt(dbr.get(nmFldContInterv).toString()) > 0)) {
			sel = sel.replaceAll("#NM_TAB#", nmTab);
			sel = sel.replaceAll("#N_CARTELLA#", dbr.get("int_cartella").toString());
			sel = sel.replaceAll("#NM_FLD_CONTATTO#", nmFldContSk);
			sel = sel.replaceAll("#N_CONTATTO#", dbr.get(nmFldContInterv).toString());

			LOG.debug("leggiPresidioCont: sel=["+sel+"]");	
			ISASRecord dbrC = dbc.readRecord(sel);			
			if ((dbrC != null) && (dbrC.get(nmFldPres) != null))
				codPres = (String)dbrC.get(nmFldPres);
		}
		return codPres;
	}
	private void gestisciFlag(ISASConnection dbc, ISASRecord dbr_prec, boolean inInsert) throws Exception {
		if (inInsert)		
			dbr_prec.put(CostantiSinssntW.FLAG_SENT, CostantiSinssntW.FLAG_DA_INVIARE_I);		
		else {
			//aggiornamento valutazione o rivalutazione		
			//			ISASRecord dbr_prec = queryKey(dbc, dbr.getHashtable());
			String flag_sent = dbr_prec.get("flag_sent")!=null?dbr_prec.get("flag_sent").toString():CostantiSinssntW.FLAG_DA_INVIARE_I;
			if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_I)) dbr_prec.put("flag_sent",CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_I);
			else if (flag_sent.equals(CostantiSinssntW.FLAG_IN_CONVALIDA_V)) dbr_prec.put("flag_sent",CostantiSinssntW.FLAG_MOD_IN_CONVALIDA_V);
			else if (flag_sent.equals(CostantiSinssntW.FLAG_ESTRATTO_DEFINITIVO)) dbr_prec.put("flag_sent",CostantiSinssntW.FLAG_DA_INVIARE_V);							

		}
	}
}

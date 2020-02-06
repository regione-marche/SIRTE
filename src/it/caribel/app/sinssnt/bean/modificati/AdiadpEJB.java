package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================

// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 11/05/2000 - EJB di connessione alla procedura SINS Tabella Adiadp
//
// andrea bernardi
//
// ============================================================================

import java.util.*;
import java.sql.*;

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.util.ISASUtil;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.exception.*; // 15/02/10
import it.pisa.caribel.sinssnt.casi_adrsa.*;  // 15/02/10
import it.caribel.app.sinssnt.comuni_nascita.ComuniNascita;




public class AdiadpEJB extends SINSSNTConnectionEJB  {

public AdiadpEJB() {}

	// 15/02/10 m. ------------------------------
	private GestCasi gestCaso = new GestCasi();
	private GestSegnalazione gestSegn = new GestSegnalazione();
	private GestPresaCarico gestPresaCar = new GestPresaCarico();
	private GestErogazione gestErog = new GestErogazione();
	private EveUtils eveUtl = new EveUtils();
	// 15/02/10 m. ------------------------------




public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;

	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from intmmg where "+
                                "int_medico='"+(String)h.get("int_medico")+"' and "+
                                "int_mese='" + (String)h.get("int_mese") + "' and " +
                                "int_anno='"+(String)h.get("int_anno")+"' and "+
                                "int_tipo_pres='"+(String)h.get("int_tipo_pres")+
                                "' and int_codoper='"+(String)h.get("int_codoper")+
                                "'ORDER BY int_data";
                System.out.println("Adiadp: queryKey(): "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
                Vector vdb=dbcur.getAllRecord();
		for (int i=0;i<vdb.size();i++){
                  ISASRecord dbind=(ISASRecord)vdb.elementAt(i);
                  //System.out.println("ISAS nel vettore: "+dbind.getHashtable().toString());
                  String cognome="";
                  String nome="";
                  String cod_reg="";
                  String data = "";
                  String comune_nas="";
				  // 14/05/10 --
				  java.sql.Date dtApeCart = null;
				  java.sql.Date dtChiuCart = null;
				  java.sql.Date data1 = null;
                  // 14/05/10 --
				  if (dbind.get("int_cartella") != null)
                  {
                    String mysel="SELECT cognome,nome,cod_com_nasc,data_nasc,cod_reg"
								+ ", data_apertura dt_ape_cart, data_chiusura dt_chiu_cart" // 14/05/10
								+ " FROM cartella"
								+ " WHERE n_cartella="+dbind.get("int_cartella");
                    ISASRecord dbcart = dbc.readRecord(mysel);
                    if (dbcart==null){
                      System.out.println("ATTENZIONE NON ESISTE PIU' LA CARTELLA NUMERO "+ dbind.get("int_cartella"));
                      cognome="******CARTELLA NUMERO "+dbind.get("int_cartella")+"ELIMINATA******************";
                    } else{
                            if (dbcart.get("cognome")!=null)
                                   cognome= (String)dbcart.get("cognome");
                            else
                                   System.out.println("ATTENZIONE COGNOME NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));
                            if (dbcart.get("nome")!=null)
                                   nome= (String)dbcart.get("nome");
                            else
                                   System.out.println("ATTENZIONE NOME NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));

                            if (dbcart.get("cod_reg")!=null)
                                   cod_reg= (String)dbcart.get("cod_reg");
                            else
                                   System.out.println("ATTENZIONE CODREG NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));
                            if(dbcart.get("data_nasc") != null){
                                //data = ((java.sql.Date)dbcart.get("data_nasc")).toString();
                                //data = data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
                                data1 =  (java.sql.Date)dbcart.get("data_nasc");
                              }
                            else
                                   System.out.println("ATTENZIONE DATA NASCITA NULLA PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));

                            if (dbcart.get("cod_com_nasc") != null && !(dbcart.get("cod_com_nasc").equals("")))
                            {
//                              String mycomu="SELECT descrizione FROM comuni WHERE "+
//                                            "codice='"+dbcart.get("cod_com_nasc")+"'";
//                              ISASRecord dbcomu=dbc.readRecord(mycomu);
//                              if (dbcomu!=null && dbcomu.get("descrizione")!=null)
//                                    comune_nas=(String)dbcomu.get("descrizione");
                             comune_nas = ComuniNascita.getDecodeComuneNascita(dbc, dbcart.get("cod_com_nasc"), dbcart.get("data_nasc").toString());
                                
//                              else
//                                  System.out.println("ATTENZIONE COMUNE CANCELLATO DALLA TABELLA COMUNI "+ dbcart.get("cod_com_nasc"));
                            }
                            else
                                  System.out.println("ATTENZIONE COMUNE NASCITA NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));

							// 14/05/10 --
							dtApeCart =  (java.sql.Date)dbcart.get("dt_ape_cart");
							dtChiuCart =  (java.sql.Date)dbcart.get("dt_chiu_cart");
							// 14/05/10 --
					}
                  }//fine cartella nulla
                  else
                      System.out.println("ATTENZIONE ESISTE UN RECORD CON  CARTELLA NULLA ");

                  dbind.put("cognome",cognome+" "+nome);
                  dbind.put("cogAss",cognome);
                  dbind.put("nomAss",nome);
                  dbind.put("cod_reg",cod_reg);
                  dbind.put("data",data);
                  dbind.put("comune",comune_nas);
				  // 14/05/10 ---
				  dbind.put("dt_ape_cart", dtApeCart);
				  dbind.put("dt_chiu_cart", dtChiuCart);
				  // 14/05/10 ---
                  //PARTE DEL MEDICO
                  String cognmedico="";
                  String nomemedico="";
                  if (dbind.get("int_medico_tit") != null && !(dbind.get("int_medico_tit").equals(""))){
                    String mysel="SELECT mecogn,menome FROM medici WHERE "+
                                 "mecodi='"+dbind.get("int_medico_tit")+"'";
                    ISASRecord dbcart = dbc.readRecord(mysel);
                    if (dbcart!=null )
                    {
                        if (dbcart.get("mecogn")!=null)
                              cognmedico=(String)dbcart.get("mecogn");
                        if (dbcart.get("menome")!=null)
                              nomemedico=(String)dbcart.get("menome");
                    }else
                    {
                        System.out.println("ATTENZIONE MEDICO CANCELLATO CODICE "+ dbind.get("int_medico_tit"));
                        cognmedico="******MEDICO CON CODICE "+dbind.get("int_medico_tit")+"ELIMINATO*************";
                    }
                  }
                  else
                      System.out.println("ATTENZIONE ESISTE UN RECORD CON  MEDICO NULLO campo int_medico_tit");
                 dbind.put("cog_medico",cognmedico+" "+nomemedico);

                 String pippdes="";
                 if (dbind.get("int_prestaz") != null && !(((String)dbind.get("int_prestaz")).equals(""))){
                    if (dbind.get("int_tipo_pres")!=null)
                    {
                            String mysel="SELECT pipp_des,pipp_importo FROM tabpipp WHERE "+
                                         "pipp_codi='"+dbind.get("int_prestaz")+"' AND "+
                                         "pipp_tipo='"+dbind.get("int_tipo_pres")+"'";
                            ISASRecord dbcart = dbc.readRecord(mysel);
                            if (dbcart!=null && dbcart.get("pipp_des")!= null)
                            {
                                    pippdes=(String)dbcart.get("pipp_des");
                                    //dbind.put("int_importo",dbcart.get("pipp_importo"));
                            }
                            else
                            {
                                System.out.println("ATTENZIONE PRESTAZIONE CANCELLATO CODICE "+ dbind.get("int_prestaz")
                                                    +" TIPO"+ dbind.get("int_tipo_pres"));
                                pippdes="******PRESTAZIONE TIPO "+dbind.get("int_tipo_pres") +"CODICE "+dbind.get("int_prestaz")
                                             +"ELIMINATA*************";
                            }
                      }
                      else
                        System.out.println("ATTENZIONE ESISTE UN RECORD CON TIPO PRESTAZIONE NULLA ,CODICE="+dbind.get("int_prestaz"));
                  }
                  else
                      System.out.println("ATTENZIONE ESISTE UN RECORD CON PRESTAZIONE NULLA campo int_prestaz");
                 dbind.put("descrizione",pippdes);

/*** 01/03/13				 
                 String cognop="";
                 String nomeop="";
                 if (dbind.get("int_codoper") != null && !(dbind.get("int_codoper").equals(""))){
                    String mysel="SELECT cognome,nome FROM operatori WHERE "+
                                 "codice='"+dbind.get("int_codoper")+"'";
                    ISASRecord dbcart = dbc.readRecord(mysel);
                    if (dbcart!=null)
                    {
                        if (dbcart.get("cognome")!=null)
                              cognop=(String)dbcart.get("cognome");
                        if (dbcart.get("nome")!=null)
                              nomeop=(String)dbcart.get("nome");
                    }
                    else{
                        System.out.println("ATTENZIONE OPERATORE CANCELLATO CODICE "+ dbind.get("int_codoper"));
                        cognop="******OPERATORE CON CODICE "+dbind.get("int_codoper")+"ELIMINATO*************";
                    }
                    dbr.put("desc_oper",cognop+" "+nomeop);
                  }
***/
		}//fine for
		if (vdb.size()>0)
			dbr.put("tabella",vdb);

        dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
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

public ISASRecord query_unica(myLogin mylogin,Hashtable h) throws SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String int_data = (String)h.get("int_data");
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from intmmg where "+
			"int_cartella="+(String)h.get("int_cartella")+" and "+
                        "int_tipo_pres='"+(String)h.get("int_tipo_pres")+"' and "+
                        "int_prestaz='"+(String)h.get("int_prestaz")+"'";
                System.out.println("Adiadp: query_unica(): "+myselect);
		ISASRecord dbr=dbc.readRecord(myselect);
                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
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
public ISASRecord query_conf(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
                String myselect = "SELECT tip_default FROM tippipp WHERE "+
			          "tip_cod='"+h.get("tip_cod")+"'";
		System.out.println("Query_conf su Adiadp=>"+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
                if (dbr != null && dbr.get("tip_default")!= null && !(dbr.get("tip_default").equals(""))){
                  String myselpipp="SELECT * FROM tabpipp WHERE "+
                                   "pipp_codi='"+dbr.get("tip_default")+"' AND "+
                                   "pipp_tipo='"+h.get("tip_cod")+"'";
                  //System.out.println("Select su tabpipp: "+myselpipp);
                  ISASRecord dbpipp=dbc.readRecord(myselpipp);
                  if(dbpipp != null){
                    if(dbpipp.get("pipp_codi")!=null && !((String)dbpipp.get("pipp_codi")).equals(""))
                      dbr.put("codice",(String)dbpipp.get("pipp_codi"));
                    if(dbpipp.get("pipp_des")!=null && !((String)dbpipp.get("pipp_des")).equals(""))
                      dbr.put("descrizione",(String)dbpipp.get("pipp_des"));
                    if(dbpipp.get("pipp_importo")!=null)
                      dbr.put("importo",dbpipp.get("pipp_importo"));
                    if(dbpipp.get("pipp_tipomed")!=null && !((String)dbpipp.get("pipp_tipomed")).equals(""))
                     dbr.put("pipp_tipomed",(String)dbpipp.get("pipp_tipomed"));
                    if(dbpipp.get("pipp_dagiorno")!=null)
                      dbr.put("pipp_dagiorno",dbpipp.get("pipp_dagiorno"));
                    if(dbpipp.get("pipp_agiorno")!=null)
                      dbr.put("pipp_agiorno",dbpipp.get("pipp_agiorno"));
                    if(dbpipp.get("pipp_unicareg")!=null)
                      dbr.put("pipp_unicareg",dbpipp.get("pipp_unicareg"));
                  }
                }
                //dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
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


public ISASRecord query_pediatra(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		 dbc=super.logIn(mylogin);
                String chiave=(String)h.get("mecodi");
                String myselect = "SELECT mecodi,metipo FROM medici WHERE "+
			          "mecodi='"+chiave+"'";
		System.out.println("Query_pediatra su Adiadp=>"+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);


		//G.Brogi 21/11/2005 controllo medico assegnato
		String cartella = (String)h.get("n_cartella");
		String selmed = "SELECT cod_med FROM anagra_c ac WHERE "+
				" ac.n_cartella = "+cartella +
				" AND data_variazione IN (SELECT MAX("+
				"c.data_variazione) FROM anagra_c c"+
				" WHERE c.n_cartella=ac.n_cartella"+
				" AND c.data_variazione<="+
                                formatDate(dbc,""+h.get("data_int"))+")";
                ISASRecord w_dbr=dbc.readRecord(selmed);
                if (w_dbr==null){
                  System.out.println("DATO SPORCO ");
                  selmed = "SELECT cod_med FROM anagra_c ac WHERE "+
                           " ac.n_cartella = "+cartella +
                           " AND data_variazione IN (SELECT MAX("+
                           "c.data_variazione) FROM anagra_c c"+
                           " WHERE c.n_cartella=ac.n_cartella)";
                }
                ISASRecord dbmed = dbc.readRecord(selmed);
		System.out.println("Query_pediatra2 su Adiadp=>"+selmed);
		if (dbmed!=null && dbr!=null)
			dbr.put("medico_ass",dbmed.get("cod_med"));

                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
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

public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect= "Select * from intmmg where "+
			"int_medico='"+(String)h.get("int_medico")+ "' and "+
			"int_mese='"+(String)h.get("int_mese")+ "' and "+
			"int_anno='"+(String)h.get("int_anno")+ "' and "+
			"int_tipo_pres='"+(String)h.get("int_tipo_pres")+ "'";	
			//"int_data="+formatDate(dbc,(String)h.get("int_data"))+ " and "+
			//"int_data="+formatDate(dbc,(String)h.get("int_data_reg"))+ " and "+//DA VEDERE DATA
                        //" int_cartella="+(String)h.get("int_cartella")+ " and"+
                        //" int_prestaz='"+(String)h.get("int_prestaz")+"'";
		ISASCursor dbcur=dbc.startCursor(myselect);
        ISASRecord dbr=dbc.readRecord(myselect);
        Vector vdb=dbcur.getAllRecord();

	for (int i=0;i<vdb.size();i++){
           ISASRecord dbind=(ISASRecord)vdb.elementAt(i);
           //System.out.println("ISAS nel vettore: "+dbind.getHashtable().toString());
           String cognome="";
           String nome="";
           String cod_reg="";
           String data = "";
           String comune_nas="";
			  // 14/05/10 --
			  java.sql.Date dtApeCart = null;
			  java.sql.Date dtChiuCart = null;
           // 14/05/10 --
			  if (dbind.get("int_cartella") != null)
           {
             String mysel="SELECT cognome,nome,cod_com_nasc,data_nasc,cod_reg"
							+ ", data_apertura dt_ape_cart, data_chiusura dt_chiu_cart" // 14/05/10
							+ " FROM cartella"
							+ " WHERE n_cartella="+dbind.get("int_cartella");
             ISASRecord dbcart = dbc.readRecord(mysel);
             if (dbcart==null){
               System.out.println("ATTENZIONE NON ESISTE PIU' LA CARTELLA NUMERO "+ dbind.get("int_cartella"));
               cognome="******CARTELLA NUMERO "+dbind.get("int_cartella")+"ELIMINATA******************";
             } else{
                     if (dbcart.get("cognome")!=null)
                            cognome= (String)dbcart.get("cognome");
                     else
                            System.out.println("ATTENZIONE COGNOME NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));
                     if (dbcart.get("nome")!=null)
                            nome= (String)dbcart.get("nome");
                     else
                            System.out.println("ATTENZIONE NOME NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));

                     if (dbcart.get("cod_reg")!=null)
                            cod_reg= (String)dbcart.get("cod_reg");
                     else
                            System.out.println("ATTENZIONE CODREG NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));
                     if(dbcart.get("data_nasc") != null){
                         data = ((java.sql.Date)dbcart.get("data_nasc")).toString();
                         data = data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
                       }
                     else
                            System.out.println("ATTENZIONE DATA NASCITA NULLA PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));

                     if (dbcart.get("cod_com_nasc") != null && !(dbcart.get("cod_com_nasc").equals("")))
                     {
                      comune_nas = ComuniNascita.getDecodeComuneNascita(dbc, dbcart.get("cod_com_nasc"), dbcart.get("data_nasc").toString());

                     }
                     else
                           System.out.println("ATTENZIONE COMUNE NASCITA NULLO PER LA CARTELLA NUMERO "+ dbind.get("int_cartella"));

						// 14/05/10 --
						dtApeCart =  (java.sql.Date)dbcart.get("dt_ape_cart");
						dtChiuCart =  (java.sql.Date)dbcart.get("dt_chiu_cart");
						// 14/05/10 --
				}
           }//fine cartella nulla
           else
               System.out.println("ATTENZIONE ESISTE UN RECORD CON  CARTELLA NULLA ");

           dbind.put("cognome",cognome+" "+nome);
           dbind.put("cogAss",cognome);
           dbind.put("nomAss",nome);
           dbind.put("cod_reg",cod_reg);
           dbind.put("data",data);
           dbind.put("comune",comune_nas);
			  // 14/05/10 ---
			  dbind.put("dt_ape_cart", dtApeCart);
			  dbind.put("dt_chiu_cart", dtChiuCart);
			  // 14/05/10 ---
           //PARTE DEL MEDICO
           String cognmedico="";
           String nomemedico="";
           if (dbind.get("int_medico_tit") != null && !(dbind.get("int_medico_tit").equals(""))){
             String mysel="SELECT mecogn,menome FROM medici WHERE "+
                          "mecodi='"+dbind.get("int_medico_tit")+"'";
             ISASRecord dbcart = dbc.readRecord(mysel);
             if (dbcart!=null )
             {
                 if (dbcart.get("mecogn")!=null)
                       cognmedico=(String)dbcart.get("mecogn");
                 if (dbcart.get("menome")!=null)
                       nomemedico=(String)dbcart.get("menome");
             }else
             {
                 System.out.println("ATTENZIONE MEDICO CANCELLATO CODICE "+ dbind.get("int_medico_tit"));
                 cognmedico="******MEDICO CON CODICE "+dbind.get("int_medico_tit")+"ELIMINATO*************";
             }
           }
           else
               System.out.println("ATTENZIONE ESISTE UN RECORD CON  MEDICO NULLO campo int_medico_tit");
          dbind.put("cog_medico",cognmedico+" "+nomemedico);

          String pippdes="";
          if (dbind.get("int_prestaz") != null && !(((String)dbind.get("int_prestaz")).equals(""))){
             if (dbind.get("int_tipo_pres")!=null)
             {
                     String mysel="SELECT pipp_des,pipp_importo FROM tabpipp WHERE "+
                                  "pipp_codi='"+dbind.get("int_prestaz")+"' AND "+
                                  "pipp_tipo='"+dbind.get("int_tipo_pres")+"'";
                     ISASRecord dbcart = dbc.readRecord(mysel);
                     if (dbcart!=null && dbcart.get("pipp_des")!= null)
                     {
                             pippdes=(String)dbcart.get("pipp_des");
                             //dbind.put("int_importo",dbcart.get("pipp_importo"));
                     }
                     else
                     {
                         System.out.println("ATTENZIONE PRESTAZIONE CANCELLATO CODICE "+ dbind.get("int_prestaz")
                                             +" TIPO"+ dbind.get("int_tipo_pres"));
                         pippdes="******PRESTAZIONE TIPO "+dbind.get("int_tipo_pres") +"CODICE "+dbind.get("int_prestaz")
                                      +"ELIMINATA*************";
                     }
               }
               else
                 System.out.println("ATTENZIONE ESISTE UN RECORD CON TIPO PRESTAZIONE NULLA ,CODICE="+dbind.get("int_prestaz"));
           }
           else
               System.out.println("ATTENZIONE ESISTE UN RECORD CON PRESTAZIONE NULLA campo int_prestaz");
          dbind.put("descrizione",pippdes);


	}//fine for
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdb;
	}catch(Exception e){
		e.printStackTrace();
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

    public ISASRecord query_controlloTipi(myLogin mylogin, Hashtable h)
    throws SQLException, CariException {
        boolean done = false;
        ISASConnection dbc = null;
        ISASCursor dbcur = null;
        String myselect = "";
        String mex = "";
        String cartella = "";
        String medico = "";
        String data = "";
        String tipo = "";
        String prestaz = "";
        try {
                it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
                System.out.println("query_controlloTipi:"+h.toString());
                dbc = super.logIn(mylogin);
                cartella = (String)h.get("int_cartella");
                medico = (String)h.get("int_medico");
                data = (String)h.get("int_data");
                tipo = (String)h.get("int_tipo_pres");
                prestaz = (String)h.get("int_prestaz");
                if(tipo.equals("1")){//caso PIPP
                    //controllo che non ci siano inserite prestazioni di tipo 3 (ACCESSI ADI, ADP o ADR)
                    myselect = "SELECT * FROM intmmg WHERE int_cartella=" + cartella+
                               " AND int_medico='"+medico+"'"+
                               " AND int_data="+formatDate(dbc, data)+
                               " AND int_tipo_pres='3'";
                    dbcur = dbc.startCursor(myselect);
                    if(dbcur.next()){
                        mex += "Impossibile inserire la prestazione: \nesistono gia' prestazioni di tipo ACCESSI nello stesso giorno,\n "+
                               "allo stesso paziente e per lo stesso medico!";
                    }
                }else if(tipo.equals("3")){//caso ACCESSI
                    String tipo_dacercare="";
                    //controllo che nn siano gia' state fatte prestazioni di tipo PIPP
                    myselect = "SELECT * FROM intmmg WHERE int_cartella=" + cartella+
                               " AND int_medico='"+medico+"'"+
                               " AND int_data="+formatDate(dbc, data)+
                               " AND int_tipo_pres='1'";
                    dbcur = dbc.startCursor(myselect);
                    if(dbcur.next()){
                        mex += "Impossibile inserire la prestazione: \nesistono gia' prestazioni di tipo PIPP nello stesso giorno,\n "+
                               "allo stesso paziente e per lo stesso medico!";
                               throw new CariException(mex);
                    }

                    String sottotipo = util.getDecode(dbc, "tabpipp", "pipp_codi", "pipp_tipo",
                                                      prestaz, tipo, "pipp_sottotipo");
                    if(sottotipo.equals("1"))
                        tipo_dacercare="2";
                    else if(sottotipo.equals("2"))
                        tipo_dacercare="1";
                    //controllo che nn ci siano prestazioni con sottotipo diverso da quello che si sta inserendo
                    myselect = "SELECT * FROM intmmg, tabpipp WHERE int_cartella=" + cartella+
                               " AND int_medico='"+medico+"'"+
                               " AND int_data="+formatDate(dbc, data)+
                               " AND int_tipo_pres='3'"+
                               " AND pipp_tipo = int_tipo_pres"+
                               " AND pipp_codi = int_prestaz"+
                               " AND pipp_sottotipo='"+tipo_dacercare+"'";
                    System.out.println("CASO ACCESSI: select x il controllo dei sottotipi diversi=>"+myselect);
                    dbcur = dbc.startCursor(myselect);
                    if(dbcur.next()){
                        if(sottotipo.equals("1"))
                            mex += "Impossibile inserire la prestazione: \nesistono gia' ACCESSI di tipo ADP nello stesso giorno,\n "+
                                   "allo stesso paziente e per lo stesso medico!";
                        else if(sottotipo.equals("2"))
                            mex += "Impossibile inserire la prestazione: \nesistono gia' ACCESSI di tipo ADI nello stesso giorno,\n "+
                                   "allo stesso paziente e per lo stesso medico!";
                    }
                }
                ISASRecord dbr = dbc.newRecord("intmmg");
                dbr.put("int_cartella", cartella);
                dbr.put("int_medico", medico);
                dbr.put("int_data", data);
                dbr.put("int_tipo", tipo);
                dbr.put("int_prestaz", prestaz);
                if (!mex.equals(""))
                        throw new CariException(mex);

                if(dbcur!=null)     dbcur.close();
                dbc.close();
                super.close(dbc);
                done = true;
                return dbr;
            } catch (CariException ce) {
                    throw new CariException(mex, -1);
                    //throw ce;
            } catch (Exception e) {
                    e.printStackTrace();
                    throw new SQLException("Errore eseguendo una query_controlloTipi()  ");
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
public ISASRecord insert(myLogin mylogin,Hashtable hin,Vector hv1)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	boolean done=false;
	String int_medico=null;
	String int_anno=null;
	String int_mese=null;
	String int_tipo=null;
	ISASConnection dbc=null;
	ISASRecord dbr=null;// 15/02/10
    try {
		dbc=super.logIn(mylogin);

		// 15/02/10 m
		dbc.startTransaction();

        System.out.println("Hashtable in hin: "+hin.toString());
		int_medico=(String)hin.get("int_medico");
        int_anno=(String)hin.get("int_anno");
        int_mese=(String)hin.get("int_mese");
        int_tipo=(String)hin.get("int_tipo_pres");
    }catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	int_medico=(String)hin.get("int_medico");
    int_anno=(String)hin.get("int_anno");
    int_mese=(String)hin.get("int_mese");
    int_tipo=(String)hin.get("int_tipo_pres");
	try{
/** 15/02/10
		ISASRecord dbr=dbc.newRecord("intmmg");
		Enumeration n=hin.keys();
**/
        for(Enumeration en=hv1.elements(); en.hasMoreElements(); ){
			// 15/02/10 ---
			int risu = 0;
			dbr=dbc.newRecord("intmmg");
			// 15/02/10 ---
            Hashtable h=(Hashtable)en.nextElement();
            System.out.println("DENTRO IL FOR: "+h.toString());
            dbr.put("int_cartella",h.get("int_cartella"));
            dbr.put("int_prestaz",h.get("int_prestaz"));
           // if (h.get("int_data")!=null)
                dbr.put("int_data",h.get("int_data"));
            if (h.get("int_data_reg")!=null)
				dbr.put("int_data_reg",h.get("int_data_reg"));
		    dbr.put("int_gg",h.get("int_gg"));
            dbr.put("int_medico_tit",h.get("int_medico_tit"));
			dbr.put("int_importo",h.get("int_importo"));
            dbr.put("int_qta",h.get("int_qta"));
            dbr.put("int_exp",h.get("int_exp"));
            dbr.put("int_exp_data",h.get("int_exp_data"));
			dbr.put("flag_sent",CostantiSinssntW.FLAG_DA_INVIARE_I);
			// 01/03/13
			dbr.put("int_codoper", h.get("int_codoper"));
			dbr.put("int_anno", h.get("int_anno"));
			dbr.put("int_mese", h.get("int_mese"));			
			dbr.put("int_medico", h.get("int_medico"));
			dbr.put("int_tipo_pres", h.get("int_tipo_pres"));
			
			// 15/02/10 m.
			Enumeration n=hin.keys();
            while(n.hasMoreElements()){
                String e=(String)n.nextElement();
                dbr.put(e,hin.get(e));
            }
            System.out.println("Vado a scrivere l'isasrecord: "+dbr.getHashtable().toString());
			// 15/02/10
			Hashtable h_fromDbr = (Hashtable)dbr.getHashtable();

            dbc.writeRecord(dbr);

			// 15/02/10
//			if ((int_tipo != null) && (int_tipo.trim().equals("3"))) // solo per tipo = ACCESSI
//				risu = gestCasoAndErogaz(dbc, h_fromDbr, true);
        }//end for
        String myselect="Select * from intmmg where "+
			"int_medico='"+int_medico+"' and "+
			"int_anno="+int_anno+" and "+
			"int_mese="+int_mese+" and "+
			"int_tipo_pres='"+int_tipo+"'";
		dbr=dbc.readRecord(myselect);

		// 15/02/10 m
		dbc.commitTransaction();

		dbc.close();
		super.close(dbc);
		done=true;

		return dbr;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			e1.printStackTrace();
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			e1.printStackTrace();
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(Exception e){
		e.printStackTrace();
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			e1.printStackTrace();
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw new SQLException("Errore eseguendo una insert() - "+  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){
				e2.printStackTrace();
				System.out.println(e2);}
		}
	}
}

public ISASRecord update(myLogin mylogin,Hashtable h,Vector hv1)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	boolean done=false;
	String int_medico=null;
	String int_anno=null;
	String int_mese=null;
	String int_tipo=null;
	ISASConnection dbc=null;
	ISASRecord dbr=null;
	try {
		dbc=super.logIn(mylogin);
		int_medico=(String)h.get("int_medico");
                int_anno=(String)h.get("int_anno");
                int_mese=(String)h.get("int_mese");
                int_tipo=(String)h.get("int_tipo_pres");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		// 15/02/10 m
		dbc.startTransaction();
		int risu = 0;

		// 15/02/10 m.: per verificare i record gi� presenti sul DB e NON inviare evento EROGAZIONE
		Hashtable h_recExist = new Hashtable();


		String myselect="Select * from intmmg where "+
                                "int_medico='"+int_medico+"' and "+
                                "int_anno="+int_anno+" and "+
                                "int_mese="+int_mese+" and "+
                                "int_tipo_pres='"+int_tipo+"'";
        ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		int flag_sent= ((((ISASRecord)vdbr.firstElement()).get("flag_sent")!=null)?ISASUtil.getIntField((ISASRecord)vdbr.firstElement(),"flag_sent"):0);
        for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); )
        {
            ISASRecord dbdiag=(ISASRecord)senum.nextElement();
            Hashtable hdiag=dbdiag.getHashtable();
// 15/02/10 m.	deleteDiag(dbc,hdiag);
			deleteDiag(dbc,hdiag, h_recExist);
        }

// 15/02/10	InserisciRecord(dbc,h,hv1,);
		InserisciRecord(dbc,h,hv1, h_recExist,flag_sent);
        String mysel="Select * from intmmg where "+
					"int_medico='"+int_medico+"' and "+
                    "int_anno="+int_anno+" and "+
                    "int_mese="+int_mese+" and "+
                    "int_tipo_pres='"+int_tipo+"'";
		dbr=dbc.readRecord(mysel);

		// 15/02/10 m
		dbc.commitTransaction();

		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(CariException ce){
		System.out.println(ce);
	    try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
	    ce.setISASRecord(dbr);
		throw ce;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw e;
	}catch(Exception e){
		e.printStackTrace();
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			e1.printStackTrace();
			throw new SQLException("Errore eseguendo una rollback() - "+  e1);
		}
		throw new SQLException("Errore eseguendo una update() - "+  e);
		
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

private String deleteDiag(ISASConnection dbc,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
	return this.deleteDiag(dbc, h, null);
}

// 15/02/10 m.: per verificare i record gi� presenti sul DB e NON inviare evento EROGAZIONE
private String deleteDiag(ISASConnection dbc,Hashtable h, Hashtable h_rec)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
        String data=((java.sql.Date)h.get("int_data")).toString();
	try{
		String myselect="SELECT * FROM intmmg WHERE "+
			"int_cartella="+(Integer)h.get("int_cartella")+" and "+
			"int_anno='"+(String)h.get("int_anno")+"' AND "+
			"int_mese='"+(String)h.get("int_mese")+"' AND "+
			"int_data="+formatDate(dbc,data)+" AND "+
            "int_tipo_pres='"+(String)h.get("int_tipo_pres")+"' AND "+
			"int_medico='"+(String)h.get("int_medico")+"' AND "+
			"int_prestaz='"+(String)h.get("int_prestaz")+"'";
            ISASRecord dbdiag=dbc.readRecord(myselect);
            System.out.println("Vado a cancellare il record()!");
            String msg = "";
			// 15/02/10 m.:
			if(dbdiag!=null) {
				String int_tipo = (String)h.get("int_tipo_pres");
				if ((int_tipo != null) && (int_tipo.trim().equals("3"))) { // solo per tipo = ACCESSI
					// chiave tabella INT_MMG ---
					String key_rec = h.get("int_anno")+"|"+h.get("int_mese")+"|"+h.get("int_medico")+
									"|"+int_tipo+"|"+h.get("int_cartella")+"|"+
									data+"|"+h.get("int_prestaz");
					if (h_rec != null) //Simone 250516: salvo il flag_sended per evitare scarti in caso di reinserimento di record già inviati
						h_rec.put(key_rec, dbdiag.get("flag_sent").toString());
					// 15/02/10 m. --------------------------------------
				}
				if (dbdiag.get("flag_sent")==null || dbdiag.get("flag_sent").toString().equals("0")){
			          dbc.deleteRecord(dbdiag);
				}
				else msg = "Uno o più accessi non sono stati cancellati perché già inviati tramite flussi ministeriali.";
             
				
			}
		done=true;
		return msg;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
                try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
                try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
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
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

// 15/02/10 private void InserisciRecord(ISASConnection dbc,Hashtable h1,Vector hv1)
private void InserisciRecord(ISASConnection dbc,Hashtable h1,Vector hv1, Hashtable h_rec,int flag_sent)
throws DBRecordChangedException, ISASPermissionDeniedException,
	ISASMisuseException, SQLException, CariException, Exception {
/** 15/02/10
	boolean done=false;
	try{

		ISASRecord dbr=dbc.newRecord("intmmg");
		Enumeration n=h1.keys();
**/
		for(Enumeration en=hv1.elements(); en.hasMoreElements(); )
        {
			// 15/02/10 ---
			int risu = 0;
			ISASRecord dbr=dbc.newRecord("intmmg");
			// 15/02/10 ---
			Hashtable h=(Hashtable)en.nextElement();
            dbr.put("int_cartella",h.get("int_cartella"));
            dbr.put("int_prestaz",h.get("int_prestaz"));
            if (h.get("int_data")!=null)
                dbr.put("int_data",h.get("int_data"));
            if (h.get("int_data_reg")!=null)
                dbr.put("int_data_reg",h.get("int_data_reg"));
            dbr.put("int_gg",h.get("int_gg"));
            dbr.put("int_medico_tit",h.get("int_medico_tit"));
            dbr.put("int_importo",h.get("int_importo"));
            dbr.put("int_qta",h.get("int_qta"));
            dbr.put("int_exp",h.get("int_exp"));
            dbr.put("int_exp_data",h.get("int_exp_data"));
//			dbr.put("flag_sent",new Integer(flag_sent));
			// 01/03/13
			dbr.put("int_codoper", h.get("int_codoper"));			
			
			// 15/02/10 m.
			Enumeration n=h1.keys();
            while(n.hasMoreElements()){
                String e=(String)n.nextElement();
                dbr.put(e,h1.get(e));
            }
            //System.out.println("Prima della write!"+dbr.getHashtable().toString());
			// 15/02/10
			Hashtable h_fromDbr = (Hashtable)dbr.getHashtable();

			String int_tipo=(String)h1.get("int_tipo_pres");
			if ((int_tipo != null) && (int_tipo.trim().equals("3"))) { // solo per tipo = ACCESSI
				// chiave tabella INT_MMG
				String key_recExist = h_fromDbr.get("int_anno")+"|"+h_fromDbr.get("int_mese")+
						"|"+h_fromDbr.get("int_medico")+"|"+int_tipo+"|"+h_fromDbr.get("int_cartella")+
						"|"+h_fromDbr.get("int_data")+"|"+h_fromDbr.get("int_prestaz");
				
//				if (!h_rec.containsKey(key_recExist))
//					risu = gestCasoAndErogaz(dbc, h_fromDbr, false);
				if (!h_rec.containsKey(key_recExist))
					dbr.put("flag_sent", CostantiSinssntW.FLAG_DA_INVIARE_I);
			}
			
            dbc.writeRecord(dbr);

			// 15/02/10
        }//end for
/** 15/02/10
		done=true;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
                try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
                try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
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
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
**/
}

public void delete(myLogin mylogin,Hashtable hin,Vector hv1)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException, CariException {
	boolean done=false;
	ISASConnection dbc=null;
	String int_medico=null;
	String int_anno=null;
	String int_mese=null;
	String int_tipo=null;
	try {
		int_medico=(String)hin.get("int_medico");
                int_anno=(String)hin.get("int_anno");
                int_mese=(String)hin.get("int_mese");
                int_tipo=(String)hin.get("int_tipo_pres");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
                String myselect="Select * from intmmg where "+
			"int_medico='"+int_medico+"' and "+
			"int_anno="+int_anno+" and "+
			"int_mese="+int_mese+" and "+
			"int_tipo_pres='"+int_tipo+"'";
                ISASCursor dbcur=dbc.startCursor(myselect);
            String msg = "";
		Vector vdbr=dbcur.getAllRecord();
                for(Enumeration senum=vdbr.elements();senum.hasMoreElements(); ) {
                  ISASRecord dbdiag=(ISASRecord)senum.nextElement();
                  Hashtable hdiag=dbdiag.getHashtable();
                  msg = deleteDiag(dbc,hdiag);
                }
                dbc.close();
		super.close(dbc);
		done=true;
		if (!msg.equals("")){
			throw new CariException(msg);
		}
	}catch(CariException e){           
		throw e;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
        try{
	dbc.rollbackTransaction();
}catch(Exception e1){
	throw new SQLException("Errore eseguendo una rollback() - "+  e);
}
throw e;
}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
                try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
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
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}
public ISASRecord query_cartella(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String myselect="";
        String sel="";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		if (h.get("n_cartella")!=null && !((String)h.get("n_cartella")).equals(""))
		  sel += "n_cartella="+(String)h.get("n_cartella");
                if (h.get("cod_reg")!=null && !((String)h.get("cod_reg")).equals(""))
                  if (sel.equals(""))
                    sel += " cod_reg='"+(String)h.get("cod_reg")+"'";
                  else
                    sel += " AND cod_reg='"+(String)h.get("cod_reg")+"'";

		myselect="Select n_cartella,cod_reg,cognome,nome,cod_com_nasc,data_nasc, data_chiusura"+
					", data_apertura dt_ape_cart, data_chiusura dt_chiu_cart" + // 06/05/10
                         " from cartella where "+sel;
                System.out.println("Query_cartella: "+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
                if (dbr!= null) {
                  dbr.put("desc_com_nasc", decodifica("comuni","codice",dbr.get("cod_com_nasc"),"descrizione",dbc));
                }
                dbc.close();
		super.close(dbc);
		done=true;
                return dbr;
	}catch(Exception e){
		e.printStackTrace();
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

public Vector CaricaPrest(myLogin mylogin, Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException{
        boolean done = false;
        Vector rit = new Vector();
        ISASConnection dbc = null;
        String msg="";
        try{
               dbc = super.logIn(mylogin);
               dbc.startTransaction();
               Hashtable hTipo=new Hashtable();
               Hashtable hTipDef=new Hashtable();
               String sel="SELECT t.tip_cod, p.*, t.tip_des,t.tip_default, "+
                          "p.pipp_unicareg FROM tabpipp p, tippipp t "+
                          "WHERE p.pipp_tipo=t.tip_cod "+
                          "ORDER BY p.pipp_des";
               System.out.println("CaricaPrest: "+sel);
               ISASCursor dbcur = dbc.startCursor(sel);
               while(dbcur.next()){
                  Hashtable hpres=new Hashtable();
                  ISASRecord dbr = dbcur.getRecord();
                  //Mi vado a caricare solo il cod del tipo tanto la descrizione la carico
                  //nell'altra hashtable
                  if (dbr.get("tip_cod")!=null && !((String)dbr.get("tip_cod")).equals("")){
                      String tipo=(String)dbr.get("tip_cod");
                       if (hTipo!=null && ((Hashtable)hTipo.get(tipo))!=null)
                            hpres=(Hashtable)hTipo.get(tipo);
                      hpres=AnalizzaPrestazioni(dbc,dbr,hpres);
                      hTipo.put(tipo,hpres);
                  }
                }
                String selDef = "SELECT DISTINCT tip_cod,tip_des,tip_default FROM tippipp"+
                                " ORDER BY tip_des";
                ISASCursor dbcurDef=dbc.startCursor(selDef);
                while (dbcurDef.next()){
                  ISASRecord dbDef = dbcurDef.getRecord();
                  if(dbDef.get("tip_default")!=null)
                    hTipDef.put((String)dbDef.get("tip_cod")+"|"+
                                (String)dbDef.get("tip_des"),
                                (String)dbDef.get("tip_default"));
                }
                rit.add(hTipDef);
                rit.add(hTipo);
                //System.out.println("*******HTIPO=>"+hTipo.toString());
                //System.out.println("*******HDEFAULT="+hTipDef.toString());
                dbcur.close();
                dbcurDef.close();
                dbc.commitTransaction();
                dbc.close();
                super.close(dbc);
                done = true;
                return rit;
        }catch(DBRecordChangedException e){
                System.out.println("AdiadpEJB.CaricaPrest(): Eccezione= " + e);
                try{
                        System.out.println("AdiadpEJB.CaricaPrest => ROLLBACK");
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
                }
                throw e;
        }catch(ISASPermissionDeniedException e){
                System.out.println("AdiadpEJB.CaricaPrest: Eccezione= " + e);
                try{
                        System.out.println("AdiadpEJB.CaricaPrest => ROLLBACK");
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new ISASPermissionDeniedException("Errore eseguendo una query_agenda() - "+  e);
                }
                throw e;
        }catch(Exception e){
                System.out.println("AdiadpEJB.CaricaPrest: Eccezione= " + e);
                try{
                        System.out.println("AdiadpEJB.CaricaPrest => ROLLBACK");
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new SQLException("Errore eseguendo una rollback() - " +  e1);
                }
                throw new SQLException("Errore eseguendo una CaricaPrest() - " +  e);
        }finally{
                if (!done){
                        try{
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e2){
                                System.out.println("AdiadpEJB.CaricaPrest (): - Eccezione nella chiusura della connessione= " + e2);
                        }
                }
        }
}

public Hashtable CaricaData(myLogin mylogin, Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException{
        boolean done = false;
        Hashtable hRit = new Hashtable();
        ISASConnection dbc = null;
        String msg="";
        try{
            dbc = super.logIn(mylogin);
            dbc.startTransaction();
            //System.out.println("Hashtable in arrivo["+h.toString()+"]");
            Enumeration enValori = h.keys();
            while(enValori.hasMoreElements()){
                String chiave = (String)enValori.nextElement();
                StringTokenizer str = new StringTokenizer(chiave, "|");
                String tipo = str.nextToken();
                String prest = str.nextToken();
                String sel="SELECT pipp_datafine FROM tabpipp WHERE "+
                           "pipp_tipo='"+tipo+"' AND "+
                           "pipp_codi='"+prest+"'";
                //System.out.println("Sel data: "+sel);
                ISASRecord dbr = dbc.readRecord(sel);
                if(dbr!=null && dbr.get("pipp_datafine")!=null){
                    String data=""+dbr.get("pipp_datafine");
                    //data=data.substring(8,10)+"/"+data.substring(5,7)+"/"+data.substring(0,4);
                    hRit.put(chiave, data);
                }else
                    hRit.put(chiave, "");
            }
            dbc.commitTransaction();
            dbc.close();
            super.close(dbc);
            done = true;
            return hRit;
        }catch(DBRecordChangedException e){
                System.out.println("AdiadpEJB.CaricaData(): Eccezione= " + e);
                try{
                        System.out.println("AdiadpEJB.CaricaData => ROLLBACK");
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new DBRecordChangedException("Errore eseguendo una rollback() - " +  e);
                }
                throw e;
        }catch(ISASPermissionDeniedException e){
                System.out.println("AdiadpEJB.CaricaData: Eccezione= " + e);
                try{
                        System.out.println("AdiadpEJB.CaricaData => ROLLBACK");
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new ISASPermissionDeniedException("Errore eseguendo una query_agenda() - "+  e);
                }
                throw e;
        }catch(Exception e){
                System.out.println("AdiadpEJB.CaricaData: Eccezione= " + e);
                try{
                        System.out.println("AdiadpEJB.CaricaData => ROLLBACK");
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new SQLException("Errore eseguendo una rollback() - " +  e1);
                }
                throw new SQLException("Errore eseguendo una CaricaData() - " +  e);
        }finally{
                if (!done){
                        try{
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e2){
                                System.out.println("AdiadpEJB.CaricaData (): - Eccezione nella chiusura della connessione= " + e2);
                        }
                }
        }
}
private Hashtable AnalizzaPrestazioni(ISASConnection dbc,ISASRecord dbr,Hashtable hpres)
throws SQLException{
        try{
            double imp=0;
            String unica="";
            String da_giorno="";
            String agiorno="0";
            String dagiorno="0";
            String tipmed="0";
            String data_fine="";
            if (dbr.get("pipp_codi")!=null && !((String)dbr.get("pipp_codi")).equals("")){
                String prest=(String)dbr.get("pipp_codi");
                if (hpres!=null){
                  /*28/10/2008 l'importo devo andare a ricavarlo dalla nuova tabella pipp_importi
                               prendendo quello con data MAX
                    if(dbr.get("pipp_importo")!=null)
                    imp=((Double)dbr.get("pipp_importo")).doubleValue();
                  */
                  String selImporto = "SELECT i.pipp_importo FROM pipp_importi i WHERE "+
                                      "i.pipp_tipo='"+(String)dbr.get("pipp_tipo")+"' AND "+
                                      "i.pipp_codi='"+(String)dbr.get("pipp_codi")+"' AND "+
                                      "i.pipp_data IN (SELECT MAX (p.pipp_data) FROM pipp_importi p "+
                                      "WHERE p.pipp_tipo=i.pipp_tipo AND p.pipp_codi=i.pipp_codi)";
                  System.out.println("Select per importi:"+selImporto);
                  ISASRecord dbrImp = dbc.readRecord(selImporto);
                  if(dbrImp!=null && dbrImp.get("pipp_importo")!=null){
                     imp=((Double)dbrImp.get("pipp_importo")).doubleValue();
                     System.out.println("Importo:"+imp);
                  }
                  //System.out.println(dbr.get("pipp_codi")+" "+dbr.get("pipp_unicareg"));
                  if(dbr.get("pipp_unicareg")!=null &&
                    !((String)dbr.get("pipp_unicareg")).equals(" ")&&
                    !((String)dbr.get("pipp_unicareg")).equals("N")&&
                    !((String)dbr.get("pipp_unicareg")).equals("")){
                    unica="S";
                  }else
                    unica="N";
                  if(dbr.get("pipp_tipomed")!=null)
                    tipmed=""+(String)dbr.get("pipp_tipomed");
                  if(dbr.get("pipp_dagiorno")!=null)
                    dagiorno=""+(Integer)dbr.get("pipp_dagiorno");
                  if(dbr.get("pipp_agiorno")!=null)
                    agiorno=""+(Integer)dbr.get("pipp_agiorno");
                  hpres.put(prest, (String)dbr.get("pipp_des")+"|"+imp+"|"+unica+"|"+
                                   tipmed+"|"+dagiorno+"|"+agiorno);
                }
            }
            //System.out.println("HPRESTAZIONI=>"+hpres.toString());
            return hpres;
 	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una AnalizzaPrestazioni");
	}
}

    private String decodifica(String tabella, String nome_cod, Object val_codice,String descrizione,ISASConnection dbc) {
	Hashtable htxt = new Hashtable();
	if (val_codice==null) return " ";
        try{
		String mysel = "SELECT " + descrizione + " descrizione FROM " + tabella + " WHERE "+
                               nome_cod +" ='" + val_codice.toString() + "'";
		ISASRecord dbtxt = dbc.readRecord(mysel);
		return ((String)dbtxt.get("descrizione"));
	}catch (Exception ex) {
		return " ";
	}
    }


	// 15/02/10 m.: gestione CASO: se NON esiste nessun caso ATTIVO, si inserisce un caso SAN ---------------------------------
//	private int gestCasoAndErogaz(ISASConnection dbc, Hashtable h_fromDbr, boolean isInsert) throws Exception, CariException
//	{
//		try {
//			String cart = ((Object)h_fromDbr.get("int_cartella")).toString();
//			String datarif = ((Object)h_fromDbr.get("int_data")).toString();
//			String oper = ((Object)h_fromDbr.get("int_codoper")).toString();
//
//			Hashtable h_par = new Hashtable();
//			String pr_data = null;
//
//			// gestione progetto: ricerca ed eventualmente crea progetto contenente datarif
//			ISASRecord progetto_rif = getProgetto(dbc, cart, datarif);
//
//			if (progetto_rif != null)
//				pr_data = progetto_rif.get("pr_data").toString();
//
//			if (pr_data != null)
//				h_par.put("pr_data", pr_data);
//			else {
//// 13/05/10		throw new Exception("AdiadpEJB.gestisciCaso() - non e' stato trovato un progetto");
//				System.out.println("*** AdiadpEJB.gestisciCaso() - non e' stato trovato un progetto attivo"+
//					"\n alla data " + datarif + ", quindi NON provo a comunicare EROGAZIONE ***");
//				return 0;
//			}
//
//			h_par.put("n_cartella", cart);
//			h_par.put("dt_segnalazione", datarif);
//			h_par.put("operZonaConf", oper);
//
//			// gestione caso [e segnalazione]
//			h_par.put("dtRif", datarif);
//// 26/10/12: per erogazione anche nei casi chiusi	ISASRecord caso = (ISASRecord)gestCaso.getCasoAttivoAllaData(dbc, h_par);
//			ISASRecord caso = (ISASRecord) gestCaso.getCasoAllaData(dbc, h_par);
//			Integer idCaso = null;
//
//			boolean isToscana = ((Boolean)gestCaso.isUbicazRegTosc(dbc, h_par)).booleanValue();
//			// 21/10/10
//			boolean isPiemonte = ((Boolean)gestCaso.isUbicazRegPiem(dbc, h_par)).booleanValue();
//
//			if (caso == null) { // NON esiste il caso
//				h_par.put("dt_presa_carico", datarif);
//				
//				// 12/11/12 -----
//				String opAuto = getOperSkmmg(dbc, cart, datarif);
//				// 27/02/13: ricerca oper della zona di residenza dell'assistito ----
//				if (opAuto == null) {
//					System.out.println("*** AdiadpEJB.gestCasoAndErogaz(): NON ESISTE oper su SKMMG per la cart=["+cart+"]");
//					opAuto = getOperZonaResAss(dbc, cart, datarif);
//					
//					if (opAuto == null) {
//						System.out.println("*** AdiadpEJB.gestCasoAndErogaz(): NON ESISTE oper della zonaRes per la cart=["+cart+"]");
//						opAuto = "SASS1";
//					}
//				}
//				// 27/02/13 ----------------------				
//				h_par.put("oper_auto", opAuto);
//				// 12/11/12 -----				
//
//				gestSegn.settaDatiDefault(dbc, h_par);
//				gestSegn.settaDatiDefaultSan(dbc, h_par);
//
//				// se devo gestire l'evento segnalazione (solo in Toscana per ora) -
//				// ci pensa la segnalazione a creare il caso
//				if (isToscana) {
//					ISASRecord segn = (ISASRecord)gestSegn.insert(dbc, h_par);
//					if (segn != null) {
//						idCaso = new Integer(segn.get("id_caso").toString());
//
//						// gestione presa carico: fuori Toscana, non si fa la presa carico per casi SAN, ma solo UVM
//						h_par.put("id_caso", idCaso);
//						h_par.put("ubicazione", ""+GestCasi.UBI_RTOSC);
//
//						//  valori default x presacarico ---
//						Hashtable h_Conf = eveUtl.leggiConf(dbc, oper);
//						String cod_reg = (String)h_Conf.get("ADRSA_REG_ERO");
//						String cod_usl = (String)h_Conf.get("ADRSA_ASL_ERO");
//						h_par.put("reg_ero", cod_reg);
//						h_par.put("asl_ero", cod_usl.substring(3));
//						h_par.put("tipo_percorso", "2"); // AD solo sanitaria
//						h_par.put("percorso_progettato", "1");	// AD diretta
//						// 12/11/12
//						h_par.put("zon_ero", segn.get("zona_segnalazione"));
//						//  valori default x presacarico ---
//
//						ISASRecord presaCar = gestPresaCar.insert(dbc, h_par);
//						if (presaCar == null)
//							return -1;
//					} else
//						return -1;
//				} else if (isPiemonte) { // 21/10/10
//					h_par.put("dt_presa_carico", h_par.get("dt_segnalazione"));
//					idCaso = (Integer)gestCaso.apriCasoSan(dbc, h_par);
//				} else
//					return -1;
//			} else
//				idCaso = (Integer)caso.get("id_caso");
//
//			// gestione erogazione
//			h_par.put("int_cartella", cart);
//			h_par.put("int_data_prest", datarif);
//			// lettura da chiavi_libere del contatore accessi che serve come codice per l'evento EROGAZIONE
//			String anno = datarif.substring(0, 4);
//			Integer progr = (Integer)eveUtl.getProgrxAccessi(dbc, anno);
//			if (progr == null)
//				return -1;
//			h_par.put("int_anno", anno);
//			h_par.put("int_contatore", progr);
//			h_par.put("int_tipo_oper", "99"); // MMG
//			h_par.put("int_cod_oper", oper);
//
//			int risu = 0;
//			if (isInsert)
//				risu = gestErog.insert(dbc, h_par);
//			else
//				risu = gestErog.update(dbc, h_par);
//
//			if (risu <= 0)
//				System.out.println("::: AdiadpEJB.segnalaErogazione - NON segnalata EROGAZIONE! :::");
//			return risu;
//		} catch (Exception e) {
//			System.out.println("AdiadpEJB.gestisciCaso: ERRORE - e=" + e);
//			throw e;
//		}
//	}

//	private ISASRecord getProgetto(ISASConnection dbc, String cartella, String dataRif) throws Exception
//	{
//		ISASRecord rec = null;
//
//		try {
//			String sel = " SELECT * FROM progetto WHERE n_cartella = " + cartella
//			+ " AND pr_data <= " + dbc.formatDbDate(dataRif)
//			+ " AND (pr_data_chiusura IS NULL " + " OR pr_data_chiusura >= " + dbc.formatDbDate(dataRif) + ")";
//
//			rec = dbc.readRecord(sel);
//
///*** 13/05/10: per problemi sulle date apertura, deciso di non generare pi� la comunicazione dell'evento EROGAZIONE se non esiste gi� il CASO (e quindi il PROGETTO)
//			if (rec == null) {
//				rec = dbc.newRecord("progetto");
//				rec.put("n_cartella", new Integer(cartella));
//				rec.put("pr_data", dataRif);
//				dbc.writeRecord(rec);
//				rec = dbc.readRecord(sel);
//			}
//***/
//			return rec;
//		} catch (Exception e) {
//			System.out.println("AdiadpEJB.getProgetto: ERRORE - e=" + e);
//			throw e;
//		}
//	}
//	
//	// 12/11/12: ricerca dell'operatore che ha registrato l'ultima schedaMMG prima della dtRif
//	private String getOperSkmmg(ISASConnection dbc, String cart,
//									String dataRif) throws Exception 
//	{
//		String oper = null;
//		try {
//			String critDt = " AND b.pr_data <= " + dbc.formatDbDate(dataRif);
//		
//			String sel = "SELECT a.* FROM skmmg a" 
//				+ " WHERE a.n_cartella = " + cart
//				+ " AND a.pr_data IN (SELECT MAX(b.pr_data) FROM skmmg b"
//					+ " WHERE b.n_cartella = a.n_cartella";
//				
//			ISASRecord dbr = dbc.readRecord(sel + critDt + ")");
//			if (dbr == null);
//				dbr = dbc.readRecord(sel + ")");
//				
//			if ((dbr != null) && (dbr.get("skmmg_operatore") != null))
//				oper = dbr.get("skmmg_operatore").toString();
//			return oper;
//		} catch (Exception ex) {
//			throw newEjbException("errore in getOperSkmmg(): " + ex.getMessage(), ex);
//		}
//	}
//	
//	// 27/02/13: ricerca un qualsiasi oper della zona di residenza dell'assistito 
//	private String getOperZonaResAss(ISASConnection dbc, String cart,
//									String dataRif) throws Exception 
//	{
//		String oper = null;
//		try {
//			// lettura comune residenza su ANAGRA_C
//			String critDt = " AND b.data_variazione <= " + dbc.formatDbDate(dataRif);
//			
//			String sel = "SELECT a.* FROM anagra_c a" 
//				+ " WHERE a.n_cartella = " + cart
//				+ " AND a.data_variazione IN (SELECT MAX(b.data_variazione) FROM anagra_c b"
//					+ " WHERE b.n_cartella = a.n_cartella";
//				
//			ISASRecord dbrA = dbc.readRecord(sel + critDt + ")");
//			if (dbrA == null);
//				dbrA = dbc.readRecord(sel + ")");
//			
//			// lettura codice zona su COMUNI
//			if ((dbrA != null) && (dbrA.get("citta") != null)) {
//				sel = "SELECT * FROM comuni"
//					+ " WHERE codice = '" + (String)dbrA.get("citta") + "'";
//					
//				ISASRecord dbrC = dbc.readRecord(sel);
//				
//				// lettura codice su OPERATORI di un operatore
//				if ((dbrC != null) && (dbrC.get("cod_ist") != null)
//				&& (!dbrC.get("cod_ist").toString().trim().equals(""))) {
//					sel = "SELECT x.* FROM operatori x"
//						+ " WHERE x.cod_zona = '" + (String)dbrC.get("cod_ist") + "'"
//						+ " AND x.codice IN (SELECT MIN(y.codice) FROM operatori y"
//							+ " WHERE y.cod_zona = x.cod_zona)";
//						
//					ISASRecord dbrO = dbc.readRecord(sel);
//					
//					if ((dbrO != null) && (dbrO.get("codice") != null))
//						oper = dbrO.get("codice").toString();
//				}
//			}
//	
//			return oper;
//		} catch (Exception ex) {
//			throw newEjbException("errore in getOperZonaResAss(): " + ex.getMessage(), ex);
//		}
//	}	
	// 15/02/10 m.: gestione CASO ---------------------------------



}

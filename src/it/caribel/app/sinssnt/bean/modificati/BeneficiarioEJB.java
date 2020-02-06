package it.caribel.app.sinssnt.bean.modificati;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//31/10/2013 FIRENZE NON VUOLE CONTROLLO XCHE HANNO REC DOPPI OCNF:CTCTRLBEN
//30/11/2012 bargi modificato controlo esistenza solo x codice fiscale che � stato reso obbligatorio nella maschera
//tenere allineata con contributi
// 08/08/2003 - EJB di connessione alla procedura SINS Tabella Beneficiario
//
// Jessica Caccavale
//
// ============================================================================

import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
//import it.pisa.caribel.sins_ct.connection.*;
import it.pisa.caribel.sinssnt.connection.*;
//public class BeneficiarioEJB extends SINS_CTConnectionEJB  {
public class BeneficiarioEJB extends SINSSNTConnectionEJB  {
public BeneficiarioEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException,ISASPermissionDeniedException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from beneficiari where "+
			        "b_codice="+h.get("b_codice");
		ISASRecord dbr=dbc.readRecord(myselect);
		System.out.println(" BeneficiarioEJB.queryKey() SELECT: "+myselect);
//se il dbr nullo viene testato in AggiungiModifiche
      	        dbr = AggiungiDecodifiche(dbc,dbr);
                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(ISASPermissionDeniedException e){
        e.printStackTrace();
        throw e;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("Errore eseguendo una BeneficiarioEJB.queryKey()"+ e);
}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}
/*
 //gb 16.02.09
public ISASRecord query_controlloEsistenza(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String myselect="";
System.out.println("BeneficiarioEJB query_controlloEsistenza"+h.toString());
	String strCognome = (String)h.get("b_cognome");
	String strNome = (String)h.get("b_nome");
	try{

		if (strCognome != null)
		  strCognome=duplicateChar(strCognome,"'");
		if(!((String)h.get("b_flag")).equals("D"))
		  if (strNome != null)
		    strNome=duplicateChar(strNome,"'");

		dbc=super.logIn(mylogin);
                if(((String)h.get("b_flag")).equals("D"))
		  myselect="SELECT * FROM beneficiari WHERE "+
	                   "b_cognome='"+strCognome+"' AND "+
                           "b_cod_fiscale='"+(String)h.get("b_cod_fiscale")+"'";
                else
                  myselect="SELECT * FROM beneficiari WHERE "+
	                   "b_cognome='"+strCognome+"' AND "+
                           "b_nome='"+strNome+"' AND "+
                           "b_data_nascita="+formatDate(dbc,(String)h.get("b_data_nascita"))+" AND "+
                           "b_cod_fiscale='"+(String)h.get("b_cod_fiscale")+"'";
                if(h.get("b_codice")!=null)
                  myselect+= " AND b_codice<>"+(String)h.get("b_codice");
                System.out.println("Select:"+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.query_controlloEsistenza()"+ e);
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
//bargi 30/11/2012
public ISASRecord query_controlloEsistenza(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String myselect="";
System.out.println("BeneficiarioEJB query_controlloEsistenza"+h.toString());
	//String strCognome = (String)h.get("b_cognome");
	//String strNome = (String)h.get("b_nome");
	try{

		//if (strCognome != null)
	//	  strCognome=duplicateChar(strCognome,"'");
	//	if(!((String)h.get("b_flag")).equals("D"))
	//	  if (strNome != null)
	//	    strNome=duplicateChar(strNome,"'");

		dbc=super.logIn(mylogin);
		//31/10/2013 FIRENZE NON VUOLE CONTROLLO XCHE HANNO REC DOPPI OCNF:CTCTRLBEN
		String val=getConfByZonaOp(dbc, dbc.getKuser(), "CTCTRLBEN");
		if(val.equals("NO"))return null;
		
		
                if(((String)h.get("b_flag")).equals("D"))//ditte
		  myselect="SELECT * FROM beneficiari WHERE "+
	                //   "b_cognome='"+strCognome+"' AND "+
                           "b_cod_fiscale='"+(String)h.get("b_cod_fiscale")+"'";
                else
                  myselect="SELECT * FROM beneficiari WHERE "+
	                  // "b_cognome='"+strCognome+"' AND "+
                       //    "b_nome='"+strNome+"' AND "+
                        //   "b_data_nascita="+formatDate(dbc,(String)h.get("b_data_nascita"))+" AND "+
                           "b_cod_fiscale='"+(String)h.get("b_cod_fiscale")+"'";
                if(h.get("b_codice")!=null)
                  myselect+= " AND b_codice<>"+(String)h.get("b_codice");
                System.out.println("Select:"+myselect);
                ISASRecord dbr=dbc.readRecord(myselect);
                dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.query_controlloEsistenza()"+ e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

public String duplicateChar(String s, String c) {
        if ((s == null) || (c == null)) return s;
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

//da chi viene richiamata?
public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	String punto = "query ";
        String scr=" ";
	String scr1=" ";
	ISASConnection dbc=null;
	ISASCursor dbcur=null;
	try{
		dbc=super.logIn(mylogin);
		boolean dato_presente = false;
		String myselect= " Select * from cartella ";
                //controllo valore corretto cognome
                scr=(String)(h.get("cognome"));
	        if (!(scr==null))
                  if (!(scr.equals(" ")))
                   {
		    scr=duplicateChar(scr,"'");
                    myselect=myselect+" where cognome like '"+scr+"%'";
                   }

	        //Controllo esistenza nome
        	scr1=(String)(h.get("nome"));
            	 if (!(scr1==null))
                  if (!(scr1.equals(" ")))
               	   myselect=myselect+" and nome like '"+scr1+"%'";

                myselect=myselect+" ORDER BY cod_operatore, cognome, nome,data_apertura, data_chiusura";
		System.out.println("Query su cartella: "+myselect);
		dbcur=dbc.startCursor(myselect.toString());
		Vector vdbr=dbcur.getAllRecord();
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
   	}finally{
   		close_dbcur_nothrow(punto, dbcur);
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	String punto = "queryPaginate ";
	boolean done=false;
        String scr=" ";
	String scr1=" ";
	ISASConnection dbc=null;
	ISASCursor dbcur = null;
	try{
		dbc=super.logIn(mylogin);
		boolean dato_presente = false;
		String myselect= " Select * from beneficiari ";

                //controllo valore corretto cognome
                String myWhere="";
		scr=(String)(h.get("b_cognome"));
	        if (!(scr==null) && !(scr.trim().equals("")))
                {
		    scr=duplicateChar(scr,"'");
                    myWhere=" b_cognome like '"+scr+"%'";
                }

                //controllo valore corretto nome
                scr=(String)(h.get("b_nome"));
	        if (!(scr==null) && !(scr.trim().equals("")))
                {
		    scr=duplicateChar(scr,"'");
		    if (myWhere.equals(""))
		      myWhere=myWhere+" b_nome like '"+scr+"%'";
		    else
		       myWhere=myWhere+"AND b_nome like '"+scr+"%'";
                }
		
		if (!myWhere.equals(""))
			myWhere=" WHERE " + myWhere;
                myselect=myselect+myWhere+" ORDER BY b_codice,b_cognome, b_nome";
		System.out.println("QueryPaginate=>Beneficiari: "+myselect);
		dbcur=dbc.startCursor(myselect.toString());
		int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
		if ((vdbr != null) && (vdbr.size() > 0))
                for(int i=0; i<vdbr.size()-1; i++){
                  ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
                  if (dbr.get("b_comune_nasc")!=null &&
                    !((String)dbr.get("b_comune_nasc")).equals("")){
                      String selComu="SELECT descrizione FROM comuni WHERE "+
                                     "codice='"+(String)dbr.get("b_comune_nasc")+"'";
                      ISASRecord dbComu=dbc.readRecord(selComu);
		      if (dbComu!=null && dbComu.get("descrizione")!=null)
	                      dbr.put("desc_comune",(String)dbComu.get("descrizione"));
		      else
	                      dbr.put("desc_comune","");
                  }
		  //gb 11/11/08
		  String strBancaDescr = getBancaDescr(dbc, dbr);
		  dbr.put("banca_descr",strBancaDescr);
		  String strTipoPagamDescr = getTipoPagamDescr(dbr);
		  dbr.put("tipo_pagam_descr",strTipoPagamDescr);
                }
                dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.queryPaginate()  ");
   	}finally{
   		close_dbcur_nothrow(punto , dbcur);
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

//gb 11/11/08
private String getBancaDescr(ISASConnection dbc, ISASRecord dbr)
	throws Exception
  {
    try {
	String strBancaDescr = "-";
	String strAbi = (String) dbr.get("b_abi");
	String strCab = (String) dbr.get("b_cab");
	if ( ((strAbi == null) || strAbi.trim().equals("")) ||
	     ((strCab == null) || strCab.trim().equals("")) )
	  return strBancaDescr;

	String strSqlQuery = "SELECT ban_descrizione" +
			" FROM banche" +
			" WHERE ban_codice_abi = '" +  strAbi + "'" +
			" AND ban_cab_sport = '" + strCab + "'";
	System.out.println("BeneficiarioEJB/getBancaDescr/strSqlQuery: " + strSqlQuery);
	ISASRecord dbrBan = dbc.readRecord(strSqlQuery);
	if ((dbrBan != null) && (dbrBan.get("ban_descrizione") != null))
	{
	  strBancaDescr = (String) dbrBan.get("ban_descrizione");
	}
	return strBancaDescr;
	}catch(Exception e){
		e.printStackTrace();
		throw new Exception("Errore eseguendo una BeneficiarioEJB.getBancaDescr()  ");
   	} 
  }

private String getTipoPagamDescr(ISASRecord dbr)
	throws Exception
  {
    try {
	String strDescr = " ";
	Hashtable ht = new Hashtable();
	ht.put("0"," ");
	ht.put("1","Contanti");
	ht.put("2","Ass. Circolare");
	ht.put("3","C/C Bancario");
	ht.put("4","C/C Postale");
	String strCod = (String) dbr.get("b_paga");
	if ((strCod != null) && !strCod.equals(""))
	{
	  strDescr = (String) ht.get(strCod);
	  if (strDescr == null)
	    strDescr = " ";
	}
	return strDescr;
	}catch(Exception e){
		e.printStackTrace();
		throw new Exception("Errore eseguendo una BeneficiarioEJB.getTipoPagamDescr()  ");
   	} 
  }

//gb 19.11.08
private void setAbiCabDaIban(Hashtable h)
	throws Exception
  {
    try {
	String strAbi = "";
	String strCab = "";
	String strIban = (String) h.get("b_iban");
	if ((strIban != null) && (strIban.trim().length() >= 15))
	{
	  strAbi = "0" + strIban.substring(5,10);
	  strCab = strIban.substring(10,15);
	}
	if (!strAbi.equals(""))
	  h.put("b_abi", strAbi);
	if (!strCab.equals(""))
	  h.put("b_cab", strCab);

	}catch(Exception e){
		e.printStackTrace();
		throw new Exception("Errore eseguendo una BeneficiarioEJB.setAbiCabDaIban/x insert ");
   	} 
  }

//gb 19.11.08
private void setAbiCabDaIban(ISASRecord dbr)
	throws Exception
  {
    try {
	String strAbi = "";
	String strCab = "";
	String strIban = (String) dbr.get("b_iban");
	if ((strIban != null) && (strIban.trim().length() >= 15))
	{
	  strAbi = "0" + strIban.substring(5,10);
	  strCab = strIban.substring(10,15);
	}
	if (!strAbi.equals(""))
	  dbr.put("b_abi", strAbi);
	if (!strCab.equals(""))
	  dbr.put("b_cab", strCab);

	}catch(Exception e){
		e.printStackTrace();
		throw new Exception("Errore eseguendo una BeneficiarioEJB.setAbiCabDaIban/x update ");
   	} 
  }

public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
                dbc.startTransaction();
		int numero_cartelle = selectProgressivo(dbc,"CO_BENEFICIARI");

		ISASRecord dbr=dbc.newRecord("beneficiari");
		setAbiCabDaIban(h); //gb 19.11.08
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
                        dbr.put(e,h.get(e));
                }
		dbr.put("b_codice",new Integer(numero_cartelle));
		dbc.writeRecord(dbr);
                dbc.commitTransaction();
                //rileggo
		String myselect="Select * from beneficiari where "+
			"b_codice="+numero_cartelle;
		dbr=dbc.readRecord(myselect);
                dbr = AggiungiDecodifiche(dbc,dbr);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.insert(): "+e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una BeneficiarioEJB.insert().rollback() - "+  e1);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.insert(): "+e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new ISASPermissionDeniedException("Errore eseguendo una BeneficiarioEJB.insert().rollback() - "+  e);
		}
		throw e;
	}catch(Exception e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.insert(): "+e);
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new SQLException("Errore eseguendo una BeneficiarioEJB.insert().rollback() - "+  e1);
		}
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.insert(): "+e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

public ISASRecord update(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String codice=null;
	ISASConnection dbc=null;
        try {
		dbc=super.logIn(mylogin);
//                dbc.startTransaction();
		setAbiCabDaIban(dbr); //gb 19.11.08
		dbc.writeRecord(dbr);
//                dbc.commitTransaction();
                codice=(String)dbr.get("b_codice");
		String myselect="Select * from beneficiari where "+
			        "b_codice="+codice;
		dbr=dbc.readRecord(myselect);
                dbr = AggiungiDecodifiche(dbc,dbr);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.update(): "+e);
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.update(): "+e);
		throw e;
	}catch(Exception e1){
		System.out.println("Errore eseguendo una BeneficiarioEJB.update(): "+e1);
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.update() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}


public void delete(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
        	dbc.deleteRecord(dbr);
                dbc.close();
		super.close(dbc);
		done=true;
	}catch(DBRecordChangedException e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.delete(): "+e);
		throw e;
	}catch(ISASPermissionDeniedException e){
		System.out.println("Errore eseguendo una BeneficiarioEJB.delete(): "+e);
		throw e;
	}catch(Exception e1){
		System.out.println("Errore eseguendo una BeneficiarioEJB.delete(): "+e1);
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.delete() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

public ISASRecord AggiungiDecodifiche(ISASConnection dbc,ISASRecord dbr)
throws  Exception {
    boolean done=false;
    try{
                String w_codice="";
                String w_descr="";
                String w_select="";
                ISASRecord w_dbr=null;
	        if (dbr!= null) {
                        Hashtable h1 = dbr.getHashtable();
			//comune nascita
			String comNascita="";
			String comCodFis="";
                        if (h1.get("b_comune_nasc")!=null && !((String)h1.get("b_comune_nasc")).equals(""))
                        {
                            w_codice  = (String)h1.get("b_comune_nasc");
//gb 21.01.09                            w_select = "SELECT * FROM comuni WHERE codice="+w_codice;
                            w_select = "SELECT * FROM comuni WHERE codice= '"+w_codice + "'";//gb 21.01.09
                            w_dbr=dbc.readRecord(w_select);
			    if (w_dbr!=null)
			    {
			      if (w_dbr.get("descrizione")!=null)
	                        comNascita=(String) w_dbr.get("descrizione");
			      if (w_dbr.get("cod_fis")!=null)
        	                comCodFis=(String) w_dbr.get("cod_fis");
			    }
                        }
			dbr.put("desc_com_nasc", comNascita);
                        dbr.put("xcf", comCodFis);
			String ComuneDom="";
                        if (h1.get("b_comune_dom")!=null && !((String)h1.get("b_comune_dom")).equals(""))
                        {
                            w_codice  = (String)h1.get("b_comune_dom");
//gb 21.01.09                            w_select = "SELECT * FROM comuni WHERE codice="+w_codice;
                            w_select = "SELECT * FROM comuni WHERE codice= '"+w_codice + "'"; //gb 21.01.09
                            w_dbr=dbc.readRecord(w_select);
		            if (w_dbr!=null && w_dbr.get("descrizione")!=null)
                                    ComuneDom=(String) w_dbr.get("descrizione");
                        }
                        dbr.put("desc_com_dom", ComuneDom);
                        String banca="";
                        if((dbr.get("b_abi")!=null && !(dbr.get("b_abi").equals(""))) &&
                           (dbr.get("b_cab")!=null && !(dbr.get("b_cab").equals(""))))
                        {
                           w_codice=(String)dbr.get("b_abi");
                           String cod2=(String)dbr.get("b_cab");
                           w_select="SELECT ban_descr_sport FROM banche WHERE ban_codice_abi='"+w_codice+"'"+
                               " AND ban_cab_sport='"+cod2+"'";
                           w_dbr=dbc.readRecord(w_select);
                           if (w_dbr!=null && w_dbr.get("ban_descr_sport")!=null)
                                 banca=(String)w_dbr.get("ban_descr_sport");
                        }
                        dbr.put("des_banca", banca);
                }
		return dbr;
            }catch(Exception e){
		throw new SQLException("Errore eseguendo una BeneficiarioEJB.AggiungiDecodifiche() - "+  e);
	}
    }
	
	// 23/09/10 m: controllo che tutti i campi obbligatori(i cui nomi si ricevono dal client) siano valorizzati
	public Boolean checkDatiObblBenef(myLogin mylogin, Hashtable h_in) throws  SQLException 
	{
		boolean done=false;
		ISASConnection dbc=null;
		
		boolean tuttoOk = true;
		try{
			dbc=super.logIn(mylogin);
			
			String codBenef = (String)h_in.get("b_codice");
			Vector vettDbNm = (Vector)h_in.get("vettDbNmFldObbl");
			
			String selB = "SELECT * FROM beneficiari" +
							" WHERE b_codice = '" + codBenef + "'";
							
			ISASRecord dbrB = dbc.readRecord(selB);
			
			if ((dbrB != null) && (vettDbNm.size() > 0)) {
				Enumeration e1 = vettDbNm.elements();
				while (tuttoOk && e1.hasMoreElements()) {
					String dbNmObbl = (String)e1.nextElement();
					tuttoOk = checkValObbl(dbNmObbl, dbrB);
				}
			}
			
            dbc.close();
			super.close(dbc);
			done=true;
			return new Boolean(tuttoOk);
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una BeneficiarioEJB.checkDatiObblBenef()"+ e);
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}
	
	// 23/10/09 m
	private boolean checkValObbl(String nm, ISASRecord rec) throws Exception
	{
		Object obj = (Object)rec.get(nm);
		if (obj == null)
			return false;
		return (!((String)obj.toString()).trim().equals(""));
	}
	
//	private String getConfByZonaOp(ISASConnection dbc, String strCodOperatore, String key) throws Exception {
//		String ret = "";
//		String strZona = "";
//		String strZonaOper = "";
//			if (!strCodOperatore.equals("")) {
//				strZonaOper = getZonaFromOperatore(dbc, strCodOperatore);
//				if (strZonaOper.trim().equals(""))// non esiste zona per l'oper
//					System.out.println("!!!! SINS_CTConnectionEJB.selectConf: NON esiste zona su OPERATORI per l'operatore=["
//							+ strCodOperatore + "] !!!!");
//			}
//
//			String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='" + key + "'";
//			ISASRecord dbConf = dbc.readRecord(mysel);
//			if ((dbConf != null) && dbConf.get("conf_txt") != null) {
//				if (strZonaOper.equals(""))
//					ret = (String) dbConf.get("conf_txt");
//				else {
//					String strVal = (String) dbConf.get("conf_txt");
//					ret = getValxZona(strVal, strZonaOper);
//				}
//			}
//			return ret;
//	}

	private String getZonaFromOperatore(ISASConnection dbc, String strCodOperatore) throws Exception {
				String ret = "";
			String strQuery = "SELECT cod_zona" + " FROM operatori" + " WHERE codice = '" + strCodOperatore + "'";
			System.out.println("SINS_CTConnectionEJB/getZonaFromOperatore/strQuery: " + strQuery);
			ISASRecord dbr = dbc.readRecord(strQuery);
			if ((dbr != null) && dbr.get("cod_zona") != null)
				ret = (String) dbr.get("cod_zona");
			return ret;
	
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
			System.out.println("!!!! SINS_CTConnectionEJB.getValxZona: NON esiste codifica su CONF per la zona=[" + zonaOper
					+ "] !!!!");
		return rit;
	}	
}

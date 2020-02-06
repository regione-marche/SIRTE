package it.caribel.app.sinssnt.bean;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 21/11/2002 - EJB di connessione alla procedura SINS Tabella FoPrestFreq
//
// Jessica Caccavale
//
// 01/09/2003 - Giulia: uso 1 hashtable per ogni tipo operatore
//
// ==========================================================================

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;

public class FoPrestFreqEJB extends SINSSNTConnectionEJB {

	String dom_res;
	String dr;

public FoPrestFreqEJB() {}

	// Giulia 01/09/2003: uso 1 hashtable per ogni tipo operatore
	Vector vh = new Vector();
	Hashtable hcod_01 = new Hashtable();
	Hashtable hcod_02 = new Hashtable();
        Hashtable hcod_03 = new Hashtable();
	Hashtable hcod_04 = new Hashtable();
        //02/03/07
        Hashtable hcod_52 = new Hashtable();
	Hashtable hcod_98 = new Hashtable();
	Hashtable hpat = new Hashtable();

	private void preparaLayout(mergeDocument doc, ISASConnection dbc,
		String data_ini,String data_fine,String valore) {

		Hashtable htxt = new Hashtable();
		ServerUtility su = new ServerUtility();
		try {
			String mysel = "SELECT conf_txt FROM conf WHERE "+
				"conf_kproc='SINS' AND "+
				"conf_key='ragione_sociale'";
			ISASRecord dbtxt = dbc.readRecord(mysel);
			htxt.put("#txt#", (String)dbtxt.get("conf_txt"));
		} catch (Exception ex) {
			htxt.put("#txt#", "ragione_sociale");
		}
		htxt.put("#data_stampa#", su.getTodayDate("dd/MM/yyyy"));
		String ora = currentTime();
		htxt.put("#ora#",ora);
		//DATE
		data_ini=data_ini.substring(8,10)+"/"+
			data_ini.substring(5,7)+"/"+data_ini.substring(0,4);
		data_fine=data_fine.substring(8,10)+"/"+
			data_fine.substring(5,7)+"/"+data_fine.substring(0,4);
		htxt.put("#data_inizio#",data_ini);
		htxt.put("#data_fine#",data_fine);
		htxt.put("#valore#",valore);
		doc.writeSostituisci("layout",htxt);
	}

	public String currentTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat(" HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(cal.getTime());
	}

public byte[] query_prestfreq(String utente, String passwd, Hashtable par, mergeDocument doc)
	throws SQLException {

        ServerUtility su =new ServerUtility(); //gb 23/10/07
        boolean done=false;
	ISASConnection dbc=null;
	int fisio=0;
        int inf=0;
        int med=0;
        int ass=0;
        int tot=0;
        //02/03/07
        int onc=0;
        int spe=0;

        Hashtable h = new Hashtable();
        String data_ini="";
	String data_fine="";
        String valore="";
        String tipo="";
        String cod1="";
        String cod2="";

        vh.add(0,hcod_01);
        vh.add(1,hcod_02);
        vh.add(2,hcod_03);
        vh.add(3,hcod_04);
        //02/03/07
        vh.add(4,hcod_52);
        vh.add(5,hcod_98);
        byte[] rit;
        try{
        	
        	this.dom_res=(String)par.get("dom_res");
        	if (this.dom_res != null)
        	{
        	if (this.dom_res.equals("R")) this.dr="Residenza";
        	else if (this.dom_res.equals("D")) this.dr="Domicilio";
        	}
        	
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc=super.logIn(lg);
                data_ini=(String)par.get("data_inizio");
                data_fine=(String)par.get("data_fine");
                valore=(String)par.get("valore");

                //RIEMPIO L'HASH PER LA STAMPA
                preparaLayout(doc,dbc,data_ini,data_fine,valore);
                boolean entrato=false;
                //Faccio il for per scorrermi le patologie nel caso in cui
                //non siano stati inseriti i codici, altrimenti se almeno
                //uno dei codici � stato inserito mi scorro solamente la
                //patologia principale cio� skpat_patol1
                String myselect="SELECT DISTINCT s.n_cartella, sku_data, sku_data_fine" +
//gb 23/10/07			" FROM skuvt " +
                  	" FROM skuvt s, anagra_c a, "+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u"+ //gb 23/10/07
                        " WHERE sku_data<="+formatDate(dbc,data_fine)+
                        " AND ( sku_data_fine>="+formatDate(dbc,data_ini)+
			" OR sku_data_fine IS NULL ) AND sku_adi = 'S'" +
//gb 23/10/07 *******
			 " AND s.n_cartella = a.n_cartella"+
			 " AND a.data_variazione IN"+
			 " (SELECT MAX (data_variazione)"+
			 " FROM anagra_c WHERE a.n_cartella=anagra_c.n_cartella)";
//gb 23/10/07: fine *******
//gb 23/10/07 *******
            String sel="";
            String ragg = (String)par.get("ragg");
            sel = su.addWhere(sel, su.REL_AND, "u.tipo", su.OP_EQ_STR,ragg);
//	    if (ragg!=null && ragg.equals("C")){
//		sel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
//                            " AND u.codice=a.dom_citta)"+
//                            " OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
//                            " AND u.codice=a.citta))";
//	    }else if (ragg!=null && ragg.equals("A")){
//		sel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
//                            " AND u.codice=a.dom_areadis)"+
//                            " OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
//                            " AND u.codice=a.areadis))";
//             }
            
            
            //Aggiunto Controllo Domicilio/Residenza (BYSP)
            if(this.dom_res == null)
            {
                    if (ragg.equals("C"))
                      sel += " AND (( (a.dom_citta IS NOT NULL OR a.dom_citta <> '')"+
                            " AND u.codice=a.dom_citta)"+
            		" OR ( (a.dom_citta IS NULL OR a.dom_citta = '') "+
                            " AND u.codice=a.citta))";
                    else if (ragg.equals("A"))
                      sel += " AND (( (a.dom_areadis IS NOT NULL OR a.dom_areadis <> '')"+
                            " AND u.codice=a.dom_areadis)"+
            		" OR ( (a.dom_areadis IS NULL OR a.dom_areadis = '') "+
                            " AND u.codice=a.areadis))";
                    //M.Minerba 28/02/2013 per Pistoia
                    else if (ragg.equals("P"))
                        sel += " AND EXISTS (SELECT * FROM skinf c "+
                   			 " WHERE c.n_cartella = s.n_cartella "+
                 			" AND c.ski_data_apertura <= s.sku_data "+
                 			" AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                 			"AND c.ski_cod_presidio = u.codice)";    
                  //fine M.Minerba 28/02/2013 per Pistoia                   
                    
            }
            else if (this.dom_res.equals("D"))
                                      {
                                       if (ragg.equals("C"))
                      sel += " AND u.codice=a.dom_citta";
                                        else if (ragg.equals("A"))
                      sel += " AND u.codice=a.dom_areadis";
                                     //M.Minerba 28/02/2013 per Pistoia
                                        else if (ragg.equals("P"))
                    sel += " AND EXISTS (SELECT * FROM skinf c "+
                           " WHERE c.n_cartella = s.n_cartella "+
                           " AND c.ski_data_apertura <= s.sku_data "+
                           " AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                           "AND c.ski_cod_presidio = u.codice)";    
                                      //fine M.Minerba 28/02/2013 per Pistoia    
                                      }

            else if (this.dom_res.equals("R"))
                            {
                            if (ragg.equals("C"))
                      sel += " AND u.codice=a.citta";
                    else if (ragg.equals("A"))
                      sel += " AND u.codice=a.areadis";
                          //M.Minerba 28/02/2013 per Pistoia
                    else if (ragg.equals("P"))
                        sel += " AND EXISTS (SELECT * FROM skinf c "+
                               " WHERE c.n_cartella = s.n_cartella "+
                               " AND c.ski_data_apertura <= s.sku_data "+
                               " AND ((c.ski_data_uscita IS NULL) OR (c.ski_data_uscita >= s.sku_data))"+
                               "AND c.ski_cod_presidio = u.codice)";    
                                 //fine M.Minerba 28/02/2013 per Pistoia  
                            }

	   sel = su.addWhere(sel, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String)par.get("zona"));
	   sel = su.addWhere(sel, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String)par.get("distretto"));
	   sel = su.addWhere(sel, su.REL_AND, "u.codice", su.OP_EQ_STR, (String)par.get("pca"));

	   myselect += " AND " + sel;
//gb 23/10/07: fine *******

                debugMessage("FoPrestFreqEJB.query_prestfreq1(): "+myselect);
                ISASCursor curs=dbc.startCursor(myselect);
		while (curs.next()){
			ISASRecord dbrec=curs.getRecord();
			String mysel = "SELECT SUM(intpre.pre_numero) conta_double, "+
				"intpre.pre_cod_prest, interv.int_tipo_oper "+
				"FROM intpre,interv WHERE "+
				"interv.int_ambdom='D' AND "+
				"interv.int_anno=intpre.pre_anno AND "+
				"interv.int_contatore=intpre.pre_contatore AND "+
				"interv.int_data_prest >= "+
				formatDate(dbc,""+dbrec.get("sku_data"))+
				" AND interv.int_cartella="+
				dbrec.get("n_cartella");

			if (dbrec.get("sku_data_fine")!=null)
                                mysel += " AND interv.int_data_prest<="+
					formatDate(dbc,
						""+dbrec.get("sku_data_fine"));

			mysel += " GROUP BY intpre.pre_cod_prest, "+
				"interv.int_tipo_oper "+
				"HAVING COUNT(intpre.pre_cod_prest) > 0 "+
				"ORDER BY interv.int_tipo_oper, "+
				"pre_cod_prest,conta_double";

                    //debugMessage("FoPrestFreqEJB.query_prestfreq2(): "+mysel);
                  ISASCursor dbcur=dbc.startCursor(mysel);
                  String old_op = "";
                  String new_op = "";
                  while (dbcur.next()){
                    ISASRecord dbr=dbcur.getRecord();
                    entrato = true;
                    if( dbr.get("pre_cod_prest")!=null &&
                       !((String)dbr.get("pre_cod_prest")).equals(""))
                    if (dbr.get("int_tipo_oper")!=null &&
                      !((String)dbr.get("int_tipo_oper")).equals("")){
                      int tp=(new Integer((String)dbr.get("int_tipo_oper"))).intValue();
                      switch (tp){
                       case 1:
                            InserisciHash(dbr,1,dbc);
                            break;
                       case 2:
                            InserisciHash(dbr,2,dbc);
                            break;
                       case 3:
                            InserisciHash(dbr,3,dbc);
                            break;
                       case 4:
                            InserisciHash(dbr,4,dbc);
                            break;
                       case 52:
                            InserisciHash(dbr,5,dbc);
                            break;
                       case 98:
                            InserisciHash(dbr,6,dbc);
                            break;
                      }
                    }
                  }//fine while
                dbcur.close();
                }//end while esterno
                if(!entrato)
                  doc.write("messaggio");
                else{
                  //scorrimento delle 4 hashtable (piu' htab)
                  //e stampa di quelle piene
                  ScorrimentoStampa(doc,valore);
                  //System.out.println("Hash dei codici: "+hcod.toString());
                }
        //doc.write("finetab");
        //if(!entrato)
          //doc.write("messaggio");
	doc.write("finale");
        curs.close();
	doc.close();
	//riprendo il bytearray
      	rit=(byte[])doc.get();
      	//riprendo l'array di byte
      //	System.out.println("byte[] restituito ");
	//String by= new String(rit);
      	//System.out.println("Stringa del byte array   :"+by);
	dbc.close();
	super.close(dbc);
	done=true;
	return rit;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query_prestfreq()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

private void InserisciHash(ISASRecord dbr,int i,ISASConnection dbc){
try{
      Hashtable hcod = new Hashtable();
      hcod = (Hashtable)vh.get(i-1);
      if (hcod.get(dbr.get("pre_cod_prest")) != null){
      String uffa=(String)dbr.get("pre_cod_prest");
      //int pato=((Double)hcod.get(uffa)).intValue();
      int pato=((Integer)hcod.get(uffa)).intValue();;
      //Jessy 14/01/2004 Oracle restituisce un Integer e Informix un Double
      int conta=Integer.parseInt(""+((Double)dbr.get("conta_double")).doubleValue());


      int insieme=pato+conta;
      String input=(new Integer(insieme)).toString();
      java.lang.Double indo=Double.valueOf(input);
      hcod.put(dbr.get("pre_cod_prest"),indo);
    }else{
      hcod.put(dbr.get("pre_cod_prest"),dbr.get("conta_double"));
      String selpat="SELECT prest_des FROM prestaz WHERE "+
        "prest_cod='"+dbr.get("pre_cod_prest")+"'";
      ISASRecord dbpat=dbc.readRecord(selpat);
//gb 23/10/07 *******
      if (dbpat != null)
	{
      	String strPrestDes = (String) dbpat.get("prest_des");
      	if (strPrestDes != null)
      	  hpat.put(dbr.get("pre_cod_prest"),dbpat.get("prest_des"));
      	else
      	  hpat.put(dbr.get("pre_cod_prest"),"*** NON DISPONIBILE ***");
	}
      else
	hpat.put(dbr.get("pre_cod_prest"),"*** NON DISPONIBILE ***");
//gb 23/10/07: fine *******
    }//fine else
    vh.remove(i-1);
    vh.insertElementAt(hcod,i-1);
    hcod = null;
    }catch (Exception e){
    	//System.out.println("Errore InserisciHash: "+e);
    	}
}

private void ScorrimentoStampa(mergeDocument doc,String valore){
  Enumeration n1=((Hashtable)vh.get(0)).keys();
  Enumeration n2=((Hashtable)vh.get(1)).keys();
  Enumeration n3=((Hashtable)vh.get(2)).keys();
  Enumeration n4=((Hashtable)vh.get(3)).keys();
  //02/03/07
  Enumeration n5=((Hashtable)vh.get(4)).keys();
  Enumeration n6=((Hashtable)vh.get(5)).keys();

  Ciclo(n1,1,doc,valore);
  Ciclo(n2,2,doc,valore);
  Ciclo(n3,3,doc,valore);
  Ciclo(n4,4,doc,valore);
  //02/03/007
  Ciclo(n5,5,doc,valore);
  Ciclo(n6,6,doc,valore);

}

private void Ciclo (Enumeration n,int num,mergeDocument doc,String valore){
  //System.out.println("Dentro ciclo!");
  Hashtable hcod = (Hashtable)vh.get(num-1);
  Hashtable hop = new Hashtable();
  String desc_op="";
  if (num==5)
    desc_op = DecodOp(52);
  else if (num==6)
    desc_op = DecodOp(98);
  else
    desc_op = DecodOp(num);
  hop.put("#tipo#",desc_op);

  Hashtable stampa = new Hashtable();
  boolean entrato = false;
  while(n.hasMoreElements()){
    String e=(String)n.nextElement();
    //String conteggio=((Double)hcod.get(e)).toString();
    //Differenza restituzione dati da Oracle e Informix
    String conteggio=""+hcod.get(e);
    if(conteggio.indexOf(".",0)!=-1){
      StringTokenizer tok = new StringTokenizer(conteggio,".");
      conteggio = tok.nextToken();
    }// 14/01/2004 Jessy else
        //conteggio=(String)hcod.get(e);
    int casi=(new Integer(conteggio)).intValue();
    int val=(new Integer(valore)).intValue();
    if(casi>val){
            if (!entrato){
              doc.writeSostituisci("tipoper",hop);
              doc.write("testa");
              entrato = true;
            }
      String instampa=(e+" - "+hpat.get(e));
      stampa.put("#diagnosi#",instampa);
      stampa.put("#casi#",conteggio);
      doc.writeSostituisci("tabella",stampa);
    }
  }
  if (entrato)
    doc.write("finetab");
}



private String DecodOp (int tipo){
  String ret = "";
  switch (tipo){
     case 1:
          ret = "ASSISTENTI SOCIALI";
          break;
     case 2:
          ret = "INFERMIERI";
          break;
     case 3:
          ret = "MEDICI";
          break;
     case 4:
          ret = "FISIOTERAPISTI";
          break;
     case 52:
          ret = "ONCOLOGI";
          break;
     case 98:
          ret = "MEDICI SPECIALISTI";
          break;
    }
    return ret;
}
}	// End of FoEleSoc clas

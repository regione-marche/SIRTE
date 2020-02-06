package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
//
// 04/06/2003 -
// -------EJB Scheda SVAMA----
// Ilaria Mancini
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.util.ServerUtility;

public class SkSvamaEJB extends SINSSNTConnectionEJB  {
  // 14/12/06 m.: sostituito decod da tabella SVAMA_PAT a TAB_DIAGNOSI nel metodo "query_patologia()".
 public SkSvamaEJB() {}


 
 public ISASRecord queryKey(myLogin mylogin,Hashtable h)
 throws SQLException
 {
    boolean done=false;
    String n_cartella=null;
    String data_variazione=null;
    ISASConnection dbc=null;
    ISASCursor dbgriglia=null;
    ServerUtility su = new ServerUtility ();
    ISASRecord dbr=null;
    try
    {
        n_cartella=(String)h.get("n_cartella");
        data_variazione=(String)h.get("data_variazione");
    }catch (Exception e)
    {
        e.printStackTrace();
        throw new SQLException("SkSvama queryKey: Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);

        ISASRecord dbr_ass=query_tabella(dbc,"ass",h,"Q");
        dbr=caricaISASRecord(dbr_ass,dbr);
        ISASRecord dbr_uod=query_tabella(dbc,"uod",h,"Q");
        dbr=caricaISASRecord(dbr_uod,dbr);
        ISASRecord dbr_san=query_tabella(dbc,"san",h,"Q");
        dbr=caricaISASRecord(dbr_san,dbr);
        ISASRecord dbr_vcf=query_tabella(dbc,"vcf",h,"Q");
        dbr=caricaISASRecord(dbr_vcf,dbr);
        ISASRecord dbr_soc=query_tabella(dbc,"soc",h,"Q");
        dbr=caricaISASRecord(dbr_soc,dbr);
	//11/02/11 G.Brogi
        ISASRecord dbr_rsa=query_tabella(dbc,"rsa",h,"Q");
        dbr=caricaISASRecord(dbr_rsa,dbr);

        //caricamento della griglia
        Vector Vgriglia=caricaGriglia(dbc,n_cartella,data_variazione);
        if (dbr!= null)
        {
          //decodifica dei vari operatori
          dbr.put("desc_operatore_san",query_operatore(dbc,dbr.get("cod_operatore_san")));
          dbr.put("desc_operatore_soc",query_operatore(dbc,dbr.get("cod_operatore_soc")));
          dbr.put("desc_operatore_uod",query_operatore(dbc,dbr.get("cod_operatore_uod")));
          dbr.put("desc_operatore_vcf",query_operatore(dbc,dbr.get("cod_operatore_vcf")));
          dbr.put("desc_assistente_soc",query_operatore(dbc,dbr.get("soc_assistente")));
	  //G.Brogi 11/02/11
          dbr.put("desc_operatore_rsa",query_operatore(dbc,dbr.get("cod_operatore_rsa")));

          //decodifica della tabella patologie
          dbr.put("des_patologia1",query_patologia(dbc,dbr.get("san_patologia1")));
          dbr.put("des_patologia2",query_patologia(dbc,dbr.get("san_patologia2")));
          dbr.put("des_patologia3",query_patologia(dbc,dbr.get("san_patologia3")));

          //esiste un record non � nuovo!
          dbr.put("nuovo","N");
        }
        else
        {   if (Vgriglia==null || Vgriglia.size()==0)
            {
                  dbr=query_anagras(dbc,n_cartella,data_variazione);
                  if (dbr!=null)
                        dbr.put("data_variazione",data_variazione);
            }
            else
            {
                 dbr=query_tabella(dbc,"fam",h,"O");
                 dbr.put("nuovo","N");
            }
        }
        if (dbr!=null )
            dbr.put("dettagli",Vgriglia);

        dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
    }catch(Exception e){
            e.printStackTrace();
            throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una queryKey()  ");
    }finally{
        if(!done){
            try{
            if (dbgriglia!=null)dbgriglia.close();
            dbc.close();
            super.close(dbc);
            }catch(Exception e2){System.out.println(e2);}
        }
    }
}


private ISASRecord caricaISASRecord(ISASRecord dbr_ret,ISASRecord dbr_arrivo)
 throws SQLException
{
    try
    {
        if (dbr_ret==null && dbr_arrivo!=null)
                  return dbr_arrivo;
        if (dbr_arrivo!=null)
        {
              Hashtable h=dbr_arrivo.getHashtable();
              Enumeration n=h.keys();
              while(n.hasMoreElements())
              {
                  String e=(String)n.nextElement();
                  dbr_ret.put(e,h.get(e));
              }
        }
    return dbr_ret;
    }catch (Exception e)
    {
        e.printStackTrace();
        throw new SQLException("DEBUG SKSVAMA caricaISASRecord: Errore:" + e);
    }

}
private String query_operatore(ISASConnection dbc,Object campo)
throws  SQLException
{
//decodifica dalla tabella operatori
    String decodifica="NON ESISTE LA DECODIFICA";
    try
    {
        if (campo==null || (((String)campo).trim()).equals("")) return "";
        String select = "SELECT * FROM operatori WHERE codice='"+(String)campo +"'";
//        System.out.println("DEBUG SKSVAMA  query_operatore select" + select);
        ISASRecord dbdecod=dbc.readRecord(select);

        if (dbdecod!=null )
        {
            if (dbdecod.get("cognome")!=null && !((String)dbdecod.get("cognome")).equals(""))
                   decodifica=(String)dbdecod.get("cognome");
            if (dbdecod.get("nome")!=null && !((String)dbdecod.get("nome")).equals(""))
                   decodifica=decodifica + " " + (String)dbdecod.get("nome");
        }
        return decodifica;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SKSVAMA :Errore eseguendo una query_operatore()  ");
    }
}

private String query_patologia(ISASConnection dbc,Object campo)
throws  SQLException
{
//decodifica dalla tabella patologia
    String decodifica="NON ESISTE LA DECODIFICA";
    try
    {
        if (campo==null || (((String)campo).trim()).equals("")) return "";
// 14/12/06        String select = "SELECT * FROM svama_pat WHERE pat_codice='"+(String)campo +"'";
        String select = "SELECT * FROM tab_diagnosi WHERE cod_diagnosi = '" + (String)campo + "'"; // 14/12/06
        ISASRecord dbdecod=dbc.readRecord(select);
//        System.out.println("DEBUG SKSVAMA: query_patologia-->"+ select);
        if (dbdecod!=null )
        {
/** 14/12/06
            if (dbdecod.get("pat_descrizione")!=null && !((String)dbdecod.get("pat_descrizione")).equals(""))
                   decodifica=(String)dbdecod.get("pat_descrizione");
**/
            if (dbdecod.get("diagnosi")!=null && !((String)dbdecod.get("diagnosi")).equals(""))
                   decodifica=(String)dbdecod.get("diagnosi");
        }
        return decodifica;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SKSVAMA :Errore eseguendo una query_patologia()  ");
    }
}


private ISASRecord query_anagras(ISASConnection dbc,String cartella,String data_var) throws
 SQLException
{
    try{
        String myselect="SELECT stato_anagrafico,titolo_studio,n_cartella FROM anagra_s WHERE "+
			" n_cartella="+cartella+
                        " AND data_variazione IN (SELECT MAX(data_variazione) FROM anagra_s WHERE  n_cartella="+cartella+")";
//        System.out.println("DEBUG SKSVAMA Sono in query_anagras " + myselect);
        ISASRecord dbr_ass=dbc.readRecord(myselect);
        if (dbr_ass!=null)
        {   // E' nuovo!
            dbr_ass.put("nuovo","S");
            if (dbr_ass.get("stato_anagrafico")!=null )
            {    String stato_anagrafico= (String)dbr_ass.get("stato_anagrafico");
                  if (stato_anagrafico.equals("")) dbr_ass.put("soc_statociv","");
                  else if (stato_anagrafico.equals("1")) dbr_ass.put("soc_statociv","4");
                  else if (stato_anagrafico.equals("2")) dbr_ass.put("soc_statociv","1");
                  else if (stato_anagrafico.equals("3") || stato_anagrafico.equals("4")) dbr_ass.put("soc_statociv","2");
                  else if (stato_anagrafico.equals("5")) dbr_ass.put("soc_statociv","3");
            }
            if (dbr_ass.get("titolo_studio")!=null )
            {     String titolo_studio= (String)dbr_ass.get("titolo_studio");
                        //System.out.println("DEBUG SKSVAMA  " + titolo_studio);
                  if (titolo_studio.equals("")) dbr_ass.put("soc_istruzione","");
                  else if (titolo_studio.equals("1") || titolo_studio.equals("2")) dbr_ass.put("soc_istruzione","1");
                  else if (titolo_studio.equals("3")) dbr_ass.put("soc_istruzione","2");
                  else if (titolo_studio.equals("5")) dbr_ass.put("soc_istruzione","5");
                  else if (titolo_studio.equals("7")) dbr_ass.put("soc_istruzione","6");
            }
        }
        return dbr_ass;
        }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SKSVAMA :Errore eseguendo una query_anagras()  ");
    }
}


private Vector caricaGriglia(ISASConnection dbc,String cartella,String data_var)
throws  SQLException
{
//decodifica dalla tabella Griglia
    ISASCursor dbgriglia=null;
    Vector vdbg=null;
    try
    {
      if (cartella.equals("") && data_var.equals("")) return vdbg;
      //caricamento griglia dei familiari
      String selgriglia="SELECT * FROM svama_fam WHERE "+
              " n_cartella="+ cartella+
              " AND data_variazione="+formatDate(dbc,data_var)+
              " ORDER BY fam_progr";
//      System.out.println("DEBUG SKSVAMA--> carica_Griglia:" + selgriglia) ;
      dbgriglia=dbc.startCursor(selgriglia);
      vdbg=dbgriglia.getAllRecord();
      dbgriglia.close();
      return vdbg;
    }catch(Exception e){
         try{
                  if (dbgriglia!=null)dbgriglia.close();
          }catch(Exception e1){System.out.println(e1);}
          throw new SQLException("DEBUG SKSVAMA :Errore eseguendo una caricaGriglia()  "+ e);
    }
}

public ISASRecord update(myLogin mylogin,Hashtable hin,Vector vh)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{       boolean done=false;
        ISASConnection dbc=null;
        Hashtable tabelle= null;
        String n_cartella=null;
        String data_variazione=null;
        String pr_data="";
        ISASRecord dbr=null;
        try
        {
            n_cartella=(String)hin.get("n_cartella");
            data_variazione=(String)hin.get("data_variazione");
            pr_data=(String)hin.get("pr_data");
        }catch (Exception e)
        {
            e.printStackTrace();
            throw new SQLException("DEBUG SKSVAMA update Errore: manca la chiave primaria");
        }
        try{
             dbc=super.logIn(mylogin);
             dbc.startTransaction();
             tabelle=(Hashtable)vh.elementAt(0);

             if (tabelle!=null)
             {
                Hashtable hAss=(Hashtable)tabelle.get("svama_ass");
                if (hAss!=null )
                     dbr=opera_tabella(dbc,"ass",hAss,"");
                Hashtable hUod=(Hashtable)tabelle.get("svama_uod");
                if (hUod!=null )
                     dbr=opera_tabella(dbc,"uod",hUod,"");
                Hashtable hSan=(Hashtable)tabelle.get("svama_san");
                if (hSan!=null )
                     dbr=opera_tabella(dbc,"san",hSan,"");
                Hashtable hVcf=(Hashtable)tabelle.get("svama_vcf");
                if (hVcf!=null )
                     dbr=opera_tabella(dbc,"vcf",hVcf,"");
                Hashtable hSoc=(Hashtable)tabelle.get("svama_soc");
                if (hSoc!=null ){
                     hSoc.put("pr_data", pr_data);
                     dbr=opera_tabella(dbc,"soc",hSoc,"");
                }

		//11/02/11 G.Brogi
                Hashtable hRsa=(Hashtable)tabelle.get("svama_rsa");
                if (hRsa!=null ){
                     dbr=opera_tabella(dbc,"rsa",hRsa,"");
                }

                Vector VFam=(Vector)tabelle.get("svama_fam");
                if (((String)tabelle.get("modificaFam")).equals("SI"))
                      deleteDettagli(dbc,n_cartella,data_variazione);
                if (VFam!=null )
                {
                     for (int i=0 ;i<VFam.size();i++)
                     {
                        Hashtable hFam =(Hashtable) VFam.elementAt(i);
                        dbr=insert_Tabella(dbc,hFam,"fam");
                     }
                }
             }
            dbc.commitTransaction();
            dbc.close();
            super.close(dbc);
            done=true;
//vogliono rimanere in update
	    dbr= queryKey(mylogin, hin);
	    //System.out.println("letto");

            return dbr;
        }catch(DBRecordChangedException e){
                e.printStackTrace();
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una rollback()" + e1);
                }
                throw e;
        }catch(ISASPermissionDeniedException e){
                e.printStackTrace();
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una rollback()"+ e1);
                }
                throw e;
        }catch(Exception e1){
                System.out.println(e1);
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e2){
                        throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una rollback()"+ e1);
                }
                throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una update() - "+  e1);
        }finally{
                if(!done){
                        try{
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e2){System.out.println(e2);}
                }
        }
}


private ISASRecord query_tabella(ISASConnection dbc,String nomeTabella,Hashtable Htab,String prov)
throws  SQLException
{
//esegue una query key sulla tabella selezionata.
//se prov =Q vuol dire che vengo dalla querykey e devo leggere  cod_operatore_ +finale nome tabella
//altrimenti se vale O vuol dire che vengo da opera e devo leggere lo *,oppure vengo dalla querykey ma per la tabella svama_fam
//Se no faccio cos� nella write prende l'alias del codice operatore e da errore.
    ServerUtility su =new ServerUtility();
    String n_cartella=null;
    String data_variazione=null;
    try
    {
      n_cartella=""+Htab.get("n_cartella");
      data_variazione=""+Htab.get("data_variazione");
    }catch (Exception e)
    {
       e.printStackTrace();
       throw new SQLException("DEBUG SKSVAMA query_tabella Errore: manca la chiave primaria");
    }
    try
    {
      String mywhere="";
      mywhere = su.addWhere(mywhere, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
              n_cartella);
      mywhere = su.addWhere(mywhere,su.REL_AND, "data_variazione",su.OP_EQ_NUM,
              formatDate(dbc,data_variazione));
      String select = "";
      if (nomeTabella.equals("ass") || (!nomeTabella.equals("ass") && prov.equals("O")))
            select= "SELECT * FROM svama_"+ nomeTabella +" WHERE "+ mywhere;
      else
           select= "SELECT svama_"+ nomeTabella +".*,cod_operatore cod_operatore_"+ nomeTabella+" FROM svama_"+ nomeTabella +" WHERE "+ mywhere;
      //System.out.println("DEBUG SKSVAMA-->query_tabella-->"+ select);
      ISASRecord dbTabella=dbc.readRecord(select);
      return dbTabella;
      }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SKSVAMA :Errore eseguendo una query_tabella()  ");
    }
}

private ISASRecord opera_tabella(ISASConnection dbc,String nomeTabella,Hashtable Htab,String operazione)
throws  SQLException
{//fa una insert un'update o una delete
//se query_tabella non trova niente fa una insert altrimenti fa un update
    try
    {
      ISASRecord dbTabella=query_tabella(dbc,nomeTabella, Htab,"O");
      if (dbTabella!=null)
      {
          if (operazione.equals("D"))
               delete_Tabella(dbc,dbTabella);
           else
               dbTabella= update_Tabella(dbc,Htab,dbTabella);
      }
      else if (dbTabella==null && !operazione.equals("D"))
            dbTabella=insert_Tabella(dbc,Htab,nomeTabella);
      return dbTabella;
    }catch(Exception e){
	e.printStackTrace();
	throw new SQLException("DEBUG SKSVAMA :Errore eseguendo una opera_tabella()  ");
    }
}


private ISASRecord update_Tabella(ISASConnection dbc,Hashtable h,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
    try
    {
        Enumeration n=h.keys();
        while(n.hasMoreElements())
        {
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        return dbr;
    }catch(DBRecordChangedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un update_Tabella "+e);
        return dbr;
    }catch(ISASPermissionDeniedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un update_Tabella "+e);
        return dbr;
    }catch(Exception e1){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un update_Tabella "+e1);
        return dbr;
    }
}

private void delete_Tabella(ISASConnection dbc,ISASRecord dbr) throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
    try
    {
        dbc.deleteRecord(dbr);
    }catch(DBRecordChangedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un update_Tabella "+e);
    }catch(ISASPermissionDeniedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un update_Tabella "+e);
    }catch(Exception e1){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un update_Tabella "+e1);
    }
}

public void delete(myLogin mylogin,ISASRecord dbr)throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
    ISASConnection dbc=null;
    ISASRecord dr=null;
    String n_cartella=null;
    String data_variazione=null;
    boolean done=false;
    try
    {
        n_cartella=""+(Integer)dbr.get("n_cartella");
        data_variazione=""+(java.sql.Date)dbr.get("data_variazione");
    }catch (Exception e)
    {
        e.printStackTrace();
        throw new SQLException("DEBUG SKSVAMA delete Errore: manca la chiave primaria");
    }
    try{
         dbc=super.logIn(mylogin);
         dbc.startTransaction();
         Hashtable hTab=dbr.getHashtable();
         dbr=opera_tabella(dbc,"ass",hTab,"D");
         dbr=opera_tabella(dbc,"uod",hTab,"D");
         dbr=opera_tabella(dbc,"san",hTab,"D");
         dbr=opera_tabella(dbc,"vcf",hTab,"D");
         dbr=opera_tabella(dbc,"soc",hTab,"D");
	 //G.Brogi 11/02/11
         dbr=opera_tabella(dbc,"rsa",hTab,"D");

         deleteDettagli(dbc,n_cartella,data_variazione);
         dbc.commitTransaction();
         dbc.close();
         super.close(dbc);
         done=true;
        }catch(DBRecordChangedException e){
                e.printStackTrace();
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una rollback()" + e1);
                }
                throw e;
        }catch(ISASPermissionDeniedException e){
                e.printStackTrace();
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e1){
                        throw new SQLException("DEBUG SKSVAMA delete:Errore eseguendo una rollback()"+ e1);
                }
                throw e;
        }catch(Exception e1){
                System.out.println(e1);
                try{
                        dbc.rollbackTransaction();
                }catch(Exception e2){
                        throw new SQLException("DEBUG SKSVAMA delete: Errore eseguendo una rollback()"+ e1);
                }
                throw new SQLException("DEBUG SKSVAMA delete:Errore eseguendo una delete() - "+  e1);
        }finally{
                if(!done){
                        try{
                                dbc.close();
                                super.close(dbc);
                        }catch(Exception e2){System.out.println(e2);}
                }
        }
}

private ISASRecord insert_Tabella(ISASConnection dbc,Hashtable h,String nomeTabella)
 throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
   ISASRecord dbr=null;
    try
    {
        dbr=dbc.newRecord("svama_"+nomeTabella);
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
System.out.println("**** SCRIVO: "+dbr.getHashtable().toString());
        dbc.writeRecord(dbr);
        if(h.get("pr_data")!=null){
          dbr.put("pr_data", h.get("pr_data"));
          AggiornaStorico(dbc, dbr);
        }
        return dbr;
    }catch(DBRecordChangedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un insert_Tabella sulla tabella "+ nomeTabella + "--> "+e);
        return dbr;
    }catch(ISASPermissionDeniedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un insert_Tabella sulla tabella "+ nomeTabella + "--> "+e);
         return dbr;
    }catch(Exception e1){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un insert_Tabella sulla tabella "+ nomeTabella + "--> "+e1);
         return dbr;
    }
}


private void AggiornaStorico(ISASConnection dbc, ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException{
  try{
    NumberDateFormat nf= new NumberDateFormat();
    String pr_data = "";
    String data_var = "";
    if (dbr.get("pr_data") instanceof String)
      pr_data = (String)dbr.get("pr_data");
    else if (dbr.get("pr_data") instanceof java.sql.Date)
      pr_data = ""+(java.sql.Date)dbr.get("pr_data");
    if (dbr.get("data_variazione") instanceof String)
      data_var = (String)dbr.get("data_variazione");
    else if (dbr.get("data_variazione") instanceof java.sql.Date)
      data_var = ""+(java.sql.Date)dbr.get("data_variazione");

    String cartella = (String)dbr.get("n_cartella");
    String selVal = "SELECT MAX(prv_data_valutaz) data FROM progetto_val WHERE "+
                    "n_cartella="+cartella+" AND pr_data="+formatDate(dbc, pr_data);
    System.out.println("Select: "+selVal);
    ISASRecord dbrVal = dbc.readRecord(selVal);
    if(dbrVal.get("data")!=null){
      String dataMax = ""+(java.sql.Date)dbrVal.get("data");
      dataMax=dataMax.substring(8,10)+"/"+ dataMax.substring(5,7)+
              "/"+dataMax.substring(0,4);
      data_var=data_var.substring(8,10)+"/"+ data_var.substring(5,7)+
              "/"+data_var.substring(0,4);
      if (nf.dateCompare(data_var, dataMax)==1){
        //Devo inserire un record nello storico
        ISASRecord dbStor = dbc.newRecord("progetto_val");
        dbStor.put("n_cartella", (String)dbr.get("n_cartella"));
        pr_data = pr_data.substring(6,10)+"-"+pr_data.substring(3,5)+"-"+
                  pr_data.substring(0,2);
        dbStor.put("pr_data", pr_data);
        data_var = data_var.substring(6,10)+"-"+data_var.substring(3,5)+"-"+
                   data_var.substring(0,2);
        dbStor.put("prv_data_valutaz", data_var);
        dbStor.put("prv_tvcodice", "1");
        dbc.writeRecord(dbStor);
      }
    }
  }catch(DBRecordChangedException e){
        System.out.println("DEBUG SKSVAMA:DEBUG SKSVAMA AggiornaStorico" + e);
  }catch(ISASPermissionDeniedException e){
         System.out.println("DEBUG SKSVAMA:DEBUG SKSVAMA AggiornaStorico" + e);
  }catch(Exception e){
            System.out.println("DEBUG SKSVAMA AggiornaStorico: "+ e);
  }
}

private void deleteDettagli(ISASConnection dbc,String n_cartella,String data_variazione)
 throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
    ISASCursor dbcur=null;
    ServerUtility su = new ServerUtility ();
    try
    {
      String mywhere="";
      mywhere = su.addWhere(mywhere, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
              n_cartella);
      mywhere = su.addWhere(mywhere,su.REL_AND, "data_variazione",su.OP_EQ_NUM,
              formatDate(dbc,data_variazione));
      String select ="";
      select =" SELECT * FROM svama_fam WHERE " + mywhere;
      dbcur=dbc.startCursor(select);
      while(dbcur.next())
      {
          ISASRecord dbr=(ISASRecord)dbcur.getRecord();
          deleteRecordFamiliare(dbc,dbr);
      }
      dbcur.close();
    }catch(DBRecordChangedException e){
        try{
            if (dbcur!=null) dbcur.close();
        }catch(Exception e2)
        {System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteDettagli chiusura cursore" + e2);
        }
        System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteDettagli"+e);
    }catch(ISASPermissionDeniedException e){
        try{
            if (dbcur!=null) dbcur.close();
        }catch(Exception e2)
        {System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteDettagli chiusura cursore" + e2);
        }
        System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteDettagli"+e);
    }catch(Exception e1){
            try{
            if (dbcur!=null) dbcur.close();
        }catch(Exception e2)
        {System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteDettagli chiusura cursore" + e2);
        }
        System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteDettagli"+e1);
    }
}

private void deleteRecordFamiliare(ISASConnection dbc,ISASRecord dbr)
 throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
    ServerUtility su = new ServerUtility ();
    String n_cartella=null;
    String data_variazione=null;
    String progr=null;
    try
    {
        n_cartella=""+(Integer)dbr.get("n_cartella");
        data_variazione=""+(java.sql.Date)dbr.get("data_variazione");
        progr=""+(Integer)dbr.get("fam_progr");
    }catch (Exception e)
    {
        e.printStackTrace();
        throw new SQLException("DEBUG SKSVAMA deleteRecordFamiliare: Errore: manca la chiave primaria");
    }
    try
    {
      String mywhere="";
      mywhere = su.addWhere(mywhere, su.REL_AND, "n_cartella", su.OP_EQ_NUM,
              n_cartella);
      mywhere = su.addWhere(mywhere,su.REL_AND, "data_variazione",su.OP_EQ_NUM,
              formatDate(dbc,data_variazione));
      mywhere = su.addWhere(mywhere,su.REL_AND, "fam_progr",su.OP_EQ_NUM,progr);

      String select ="";
      select =" SELECT * FROM svama_fam WHERE " + mywhere;
      ISASRecord dbfam=dbc.readRecord(select);
      if (dbfam!=null)
          dbc.deleteRecord(dbfam);
    }catch(DBRecordChangedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteRecordFamiliare "+e);
    }catch(ISASPermissionDeniedException e){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteRecordFamiliare "+e);
    }catch(Exception e1){
        System.out.println("DEBUG SKSVAMA: errore eseguendo un deleteRecordFamiliare "+e1);
    }
}

public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
    	ISASConnection dbc=null;
        ISASCursor dbcur=null;
        ServerUtility su =new ServerUtility();
        String mywhere="";
        String n_cartella=null;
        String data_variazione=null;
        String select = "";
        n_cartella=(String)h.get("n_cartella");
        data_variazione=(String)h.get("data_variazione");
        try
        {
            dbc=super.logIn(mylogin);

            mywhere=" WHERE s.n_cartella=c.n_cartella ";
            if (n_cartella!=null )
                mywhere = su.addWhere(mywhere, su.REL_AND, "s.n_cartella", su.OP_EQ_NUM,
                      n_cartella);

           /* if (data_variazione!=null && !data_variazione.equals(""))
            {
                  mywhere = su.addWhere(mywhere,su.REL_AND, "s.data_variazione",su.OP_EQ_NUM,
                    formatDate(dbc,data_variazione));
            }*/
            String[] tabelle={"ass","fam","san","soc","uod","vcf","rsa"};
            for(int i=0;i<tabelle.length;i++)
            {
                if (!select.equals(""))
                      select = select + " UNION ";
                select =select+" SELECT s.n_cartella,s.data_variazione,nvl(trim(c.cognome),'') ||' ' || nvl(trim(c.nome),'') des_cartella "+
                        " FROM svama_"+ tabelle[i] + " s,cartella c " + mywhere;

            }
            //System.out.println("DEBUG SKSVAMA :querypaginate-->"+ select);
            dbcur=dbc.startCursor(select);
            int start = Integer.parseInt((String)h.get("start"));
            int stop = Integer.parseInt((String)h.get("stop"));
            Vector vdbr = dbcur.paginate(start, stop);
            dbcur.close();
            dbc.close();
            super.close(dbc);
            done=true;
            return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("DEBUG SKSVAMA :querypaginate");
	}finally{
		if(!done){
			try{
                                if (dbcur!=null)dbcur.close();
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}
public String getUltimaSvama(myLogin mylogin, Hashtable h)
		throws DBRecordChangedException, ISASPermissionDeniedException,	SQLException
	{
		boolean done = false;
		ISASConnection dbc = null;
                ISASCursor dbcur=null;
                String n_cartella = (String)h.get("n_cartella");
                String data_variazione_aperta = (String)h.get("data_variazione_aperta");
                String data_inizio = (String)h.get("data_inizio");
                String data_fine = (String)h.get("data_fine");
                String selCh="";
                String ritorna=null;
		try{
                 dbc = super.logIn(mylogin);
                 if (!data_fine.equals("")){
                   selCh=" and  data_variazione<="+formatDate(dbc,data_fine);
                 }
                 String select = "";
                 String[] tabelle={"ass","fam","san","soc","uod","vcf","rsa"};
                 for(int i=0;i<tabelle.length;i++)
                 {
                      if (!select.equals(""))
                        select = select + " UNION ";
                        select =select+" SELECT data_variazione "+
                          " FROM svama_"+ tabelle[i] + " where "+
                          " data_variazione>"+formatDate(dbc,data_inizio)+
                          selCh+ " and data_variazione > "+formatDate(dbc,data_variazione_aperta)+
                          " and n_cartella="+n_cartella;
                 }
                 //debugMessage("getUltimaSvama select="+select);
                 dbcur=dbc.startCursor(select);
                 if (dbcur.getDimension()>0)
                 {
                  ritorna="ko";
                 }else{
                  ritorna="ok";
                 }

                  dbcur.close();
                  dbc.close();
                  super.close(dbc);
                  done=true;
                  return ritorna;
		}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("DEBUG SKSVAMA :getUltimaSvama");
              }finally{
                  if(!done){
                          try{
                            if (dbcur!=null)dbcur.close();
                            dbc.close();
                            super.close(dbc);
                          }catch(Exception e1){System.out.println(e1);}
                  }
              }
	}// END ultima svama

 //CJ 28/11/2006 metodo per la duplicazione della scheda
 //Prima di duplicare devo controllare se esiste gi� un record
 //con la data inserita dall'operatore
 public ISASRecord query_duplica(myLogin mylogin,Hashtable h)
 throws SQLException,CariException{
    boolean done=false;
    String cartella=null;
    String data_var=null;
    String data=null;
    ISASConnection dbc=null;
    ServerUtility su = new ServerUtility ();
    ISASRecord dbr=null;
    ISASRecord dbr_ass=null;
    Hashtable h_ass=new Hashtable();
    try {
        cartella=(String)h.get("n_cartella");
        data_var=(String)h.get("data_variazione");
        data=(String)h.get("data_duplica");
    }catch (Exception e){
        e.printStackTrace();
        throw new SQLException("SkSvama queryKey: Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        String[] Vtab={"ass","san","soc","uod","vcf","rsa"};
        /*Lancio le query per il controllo dell'esistenza del record*/

        for(int i=0; i<Vtab.length; i++){
          dbr_ass=prendiISASRecord(dbc,cartella,data,Vtab[i]);
          if(dbr_ass!=null){
            throw new CariException("Attenzione record gia' esistente!",2);
          }
        }

        String sel = "SELECT * FROM svama_fam WHERE n_cartella="+cartella+
                     " AND data_variazione="+formatDate(dbc,data);
        dbr_ass=dbc.readRecord(sel);
        if(dbr_ass!=null){
          throw new CariException("Attenzione record gia' esistente!",2);
        }
        /*Fine controllo*/
        data=data.substring(6,10)+"-"+data.substring(3,5)+"-"+data.substring(0,2);
        for(int i=0; i<5; i++){
          dbr_ass=dbc.newRecord("svama_"+Vtab[i]);
          dbr=prendiISASRecord(dbc,cartella,data_var,Vtab[i]);
          if(dbr!=null){
            h_ass=dbr.getHashtable();
            Enumeration n=h_ass.keys();
            while(n.hasMoreElements()){
                    String e=(String)n.nextElement();
                    dbr_ass.put(e,h_ass.get(e));
            }
            dbr_ass.put("data_variazione", java.sql.Date.valueOf(data));
            if(dbr.get("cod_operatore_"+Vtab[i])!=null){
              dbr_ass.put("cod_operatore",dbr.get("cod_operatore_"+Vtab[i]));
            }
            //Se la tabella � uguale a svama_san e la data valutazione � inserita
            //Vado a cambiarla con la data proposta dall'operatore
            if(i==2){
              System.out.println("HASS:"+h_ass.toString());
              if(h_ass.get("soc_data_valutaz")!=null)
                dbr_ass.put("soc_data_valutaz", java.sql.Date.valueOf(data));
            }
            dbc.writeRecord(dbr_ass);
          }
        }
        sel = "SELECT * FROM svama_fam WHERE n_cartella="+cartella+
              " AND data_variazione="+formatDate(dbc,data_var);
        //System.out.println("SELECT FAM:"+sel);
        ISASCursor dbcur = dbc.startCursor(sel);
        while(dbcur.next()){
          dbr=dbcur.getRecord();
          dbr_ass=dbc.newRecord("svama_fam");
          sel = "SELECT * FROM svama_fam WHERE n_cartella="+cartella+
                " AND data_variazione="+formatDate(dbc,data_var)+
                " AND fam_progr="+(Integer)dbr.get("fam_progr");
          ISASRecord dbrFam = dbc.readRecord(sel);
          h_ass=dbrFam.getHashtable();
          Enumeration n=h_ass.keys();
          while(n.hasMoreElements()){
                  String e=(String)n.nextElement();
                  dbr_ass.put(e,h_ass.get(e));
          }
          dbr_ass.put("data_variazione", java.sql.Date.valueOf(data));

          dbc.writeRecord(dbr_ass);
        }
        //Metto la nuova data in modo da farmi caricare il record duplicato
        h.put("data_variazione", data);
        dbr= queryKey(mylogin, h);
        if(dbcur!=null) dbcur.close();
        dbc.close();
        super.close(dbc);
        done=true;
        return dbr;
    }catch(CariException ce){
            ce.setISASRecord(dbr);
            throw ce;
    }catch(Exception e){
            e.printStackTrace();
            throw new SQLException("DEBUG SKSVAMA:Errore eseguendo una query_duplica()  ");
    }finally{
        if(!done){
            try{
            dbc.close();
            super.close(dbc);
            }catch(Exception e2){System.out.println(e2);}
        }
    }
}

private ISASRecord prendiISASRecord(ISASConnection dbc,String cartella, String data, String tabella)
 throws SQLException{
    try{
      System.out.println("prendiIsas:"+cartella+" - "+data+" - "+tabella);
      String mysel="";
      if(!tabella.equals("ass"))
        mysel = "SELECT svama_"+tabella+".*,cod_operatore cod_operatore_"+tabella+
                " FROM svama_"+tabella+" WHERE ";
      else
        mysel = "SELECT * FROM svama_"+tabella+" WHERE ";

      mysel+=" n_cartella="+cartella+
             " AND data_variazione="+formatDate(dbc,data);
      System.out.println("SELECT:"+mysel);
      ISASRecord dbr_ret = dbc.readRecord(mysel);
    return dbr_ret;
    }catch (Exception e)
    {
        e.printStackTrace();
        throw new SQLException("DEBUG SKSVAMA prendiISASRecord: Errore:" + e);
    }

}

/**
 * G.Brogi 18/02/11 metodo che restituisce alcuni valori dell'ultima SVAMA inserita
 * o dell'ultima SVAMA rispetto ad un verbale di RV_PUAUVM i cui riferimenti arrivano dal
 * client
 */
    public ISASRecord getSkSvama(myLogin mylogin, Hashtable h)throws SQLException
	{
		boolean done            = false;
		ISASConnection dbc      = null;
		
		System.out.println("SkSvama.getSkSvama() -  H_IN:"+h.toString());		
        
        String n_cartella       = (String)h.get("n_cartella");
        String pr_progr         = (h.get("pr_progr")!=null?(String)h.get("pr_progr"):null);
        String pr_data          = (h.get("pr_data")!=null?(String)h.get("pr_data"):null);
		String pr_data_verbale  = (h.get("pr_data_verbale_uvm")!=null?(String)h.get("pr_data_verbale_uvm"):null);
        String myselect="";
		
		try{
            dbc = super.logIn(mylogin);

            myselect = " s1.n_cartella = "+n_cartella;
            if (pr_progr != null && pr_data!=null) {
                if (Integer.parseInt(pr_progr) > 1) {//se pr_progr = 1 non esistono record precedenti!
/*** 29/08/11: ordinamento verbali su RV_PUAUVM non pi� per progr ma per data				
					int pr_precedente = Integer.parseInt(pr_progr)-1;
					myselect += " AND s1.data_variazione IN (SELECT MAX(s2.data_variazione) FROM svama_vcf s2" +
									" WHERE s2.n_cartella = s1.n_cartella" +
									" AND s2.data_variazione <= (SELECT pr_data_verbale_uvm FROM rv_puauvm t"+
										" WHERE t.n_cartella = s1.n_cartella" +
										" AND pr_data = "+formatDate(dbc,pr_data)+
										" AND pr_progr = "+pr_precedente+"))";
***/					
					// 29/08/11
					myselect += " AND s1.data_variazione IN (SELECT MAX(s2.data_variazione) FROM svama_vcf s2"
									+ " WHERE s2.n_cartella = s1.n_cartella"
									+ " AND s2.data_variazione <= (SELECT MAX(t.pr_data_verbale_uvm) FROM rv_puauvm t"
										+ " WHERE t.n_cartella = s1.n_cartella"
										+ " AND t.pr_data = " + formatDate(dbc,pr_data)
										+ " AND t.pr_progr <> " + pr_progr
										+ (pr_data_verbale!=null?" AND t.pr_data_verbale_uvm <= " + formatDate(dbc,pr_data_verbale):"")
										+ "))";
                }
            } else {
                   myselect += " AND s1.data_variazione IN (SELECT MAX(s2.data_variazione) FROM svama_vcf s2" +
									" WHERE s2.n_cartella = s1.n_cartella)";
            }
            
			String select = "";
            select =select+" SELECT vcf_punt_calcolato,vcf_punt_assegnato, vcf_tot_mobilita,vcf_tot_funzionale,"+
                                " uod_tot_inferiabil, uod_srs_totale, uod_pcog, uod_profilo"+
							" FROM svama_vcf s1, svama_uod s3" +
							" WHERE " +  myselect +
							" AND s1.n_cartella = s3.n_cartella"+
							" AND s1.data_variazione = s3.data_variazione";
            debugMessage("getUltimaSvama select="+select);
			
            ISASRecord dbr = dbc.readRecord(select);
            if(dbr!=null) {
				// se vcf_punt_calcolato vale zero, va trasmesso vcf_punt_assegnato
/** 28/09/12				
				if(dbr.get("vcf_punt_calcolato")== null ||
			   ((Integer)dbr.get("vcf_punt_calcolato")).intValue()==0) {		
**/			   
				// 28/09/12: il punteggio 0 � valido
				if(dbr.get("vcf_punt_calcolato") == null) {
					dbr.put("vcf_punt",(Integer)dbr.get("vcf_punt_assegnato"));
				} else {
					dbr.put("vcf_punt",(Integer)dbr.get("vcf_punt_calcolato"));
				}
			}

            dbc.close();
			super.close(dbc);
            done=true;
            return dbr;
		}catch(Exception e){
			e.printStackTrace();
			throw new SQLException("DEBUG SKSVAMA :getUltimaSvama");
        }finally{
            if(!done){
                try{
                    dbc.close();
                    super.close(dbc);
                }catch(Exception e1){System.out.println(e1);}
			}
		}
	}// END getSkSvama


}

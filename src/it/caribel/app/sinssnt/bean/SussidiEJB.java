package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//TODO
//bargi 15/04/2013 aggiunta gestione flag_auto  x non elencare quelli obsoleti
//23/11/2012 allineato con sins occorre mantenerli allineati
//bargi 23/08/2012 aggiunto il metodo queryPaginateFiltri per poter filtrare i sussidi in base 
//al centro di costo associato al conto del sussidio all'eventuale associazione del cod contributo alla 
//tabella tab_serv_sussidi dove ho l'associazione tripletta intervento con eventuale codice comune al sussidio stesso
//per il momento non si usa anche il codice del servizio

// 24/05/2000 - EJB di connessione alla procedura SINS Tabella Sussidi
//
// paolo ciampolini
//
// ==========================================================================

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
//import it.pisa.caribel.sins_ct.connection.*
import it.pisa.caribel.sinssnt.connection.*;

public class SussidiEJB extends SINSSNTConnectionEJB  {
	//public class SussidiEJB extends SINS_CTConnectionEJB  {
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
public SussidiEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException, ISASPermissionDeniedException {
    boolean done=false;
    ISASConnection dbc=null;
    try{
		    dbc=super.logIn(mylogin);
		    String myselect="Select * from sussidi where codice_suss='"+(String)h.get("codice_suss")+"'";
		    ISASRecord dbr=dbc.readRecord(myselect);
		    if(dbr!=null){
	              dbr.put("des_sottospec",util.getDecode(dbc,"co_sottsuss","ss_codice",
	                            (String)util.getObjectField(dbr,"codice_ss",'S'),"ss_descrizione"));
	              dbr.put("des_area",util.getDecode(dbc,"co_area","ar_area",
	                            (String)util.getObjectField(dbr,"codice_ar",'S'),"ar_descrizione"));
	            }
		dbc.close();
		    super.close(dbc);
		    done=true;
		    return dbr;
		    }catch(ISASPermissionDeniedException ex){
		        System.out.println(ex);
		    	throw ex;
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
public ISASRecord queryDecode(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    ISASCursor dbcur=null;
    try{
		    dbc=super.logIn(mylogin);
		    String co_filtro_conti ="NO";//autorizzo da sins
	        co_filtro_conti = selectConf(dbc, "", "CO_FILTRO_CONTI");
			
		    System.out.println("queryDecode parametri: "+h.toString());
		    String myselect="Select * from sussidi where codice_suss='"+(String)h.get("codice_suss")+"'";
		    if(co_filtro_conti!=null && co_filtro_conti.equals("NO"))
			{
		    	System.out.println("CASO NO FILTRO CONTI");
			}else {
			    if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
				{
					 myselect += " AND capitolo_suss in (select distinct eco_codice from co_economici where eco_comune= '" + h.get("cod_ccosto").toString().trim() + "' )" ;
				}
			    if(h.containsKey("filtro_serv")&&h.get("filtro_serv").toString().equals("S"))
				{
					  myselect=myselect+" and codice_suss in (select cod_sussidio from "
					  +" tab_serv_sussidi t where "
					  + " cod_settore_interv='"+h.get("settore_interv").toString().trim()+"'"
					  + " and cod_tipo_interv='" +h.get("tipo_interv").toString().trim()+"'"
					  + " and cod_intervento='"+h.get("cod_interv").toString().trim()+"'";
					  if(h.containsKey("cod_servizio")&& !h.get("cod_servizio").toString().equals(""))	
						  myselect+= " and cod_servizio='"+h.get("cod_servizio").toString().trim()+"'";				
							
					  //codice comune residenza
					  //se non ho comune residenza prendo in base al codice del centro costo in co_assoc_ccosto
					  //dove ho ragruppamento dei centro di costo.
					  if(h.containsKey("cod_comune_res")&& !h.get("cod_comune_res").toString().equals(""))					
					  myselect += " and cod_comune='" +h.get("cod_comune_res").toString().trim()+"'";
					  else if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
							{
						  myselect += " and (t.cod_comune in " +
							"( select cod_comune from assoc_ccosto_comuni where" +
							" cod_centrocosto='"+ h.get("cod_ccosto").toString().trim()+"'"+
							" and tipo='C' "+
							" )" +
									" OR t.cod_comune='"+ h.get("cod_ccosto").toString().trim()+"')" ;
					  }
					  /*
					     else if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
							{
						  myselect += " and (t.cod_comune in " +
							"( select cod_centrocosto from co_assoc_ccosto where cod_centrocosto_padre='"+ h.get("cod_ccosto").toString().trim()+"')" +
									" OR t.cod_comune='"+ h.get("cod_ccosto").toString().trim()+"')" ;
					  }
					   */
					  	myselect +=")";
					   
				}
			}
		    System.out.println("queryDecode select: "+myselect);
	        dbcur=dbc.startCursor(myselect);
	        ISASRecord dbr=null;
	        if(dbcur!=null) {
	        	Vector vdbr=dbcur.getAllRecord();
	        	if(vdbr.size()>0) {
	               dbr=(ISASRecord)vdbr.elementAt(0);
	               String comune="";
	               if(h.get("cod_ccosto")!=null)
	             	  comune=h.get("cod_ccosto").toString().trim();
	        	   getDecodifiche(dbc,dbr,h,comune);
	        	}
	           dbcur.close();
	        }
	        dbc.close();
		    super.close(dbc);
		    done=true;
		    return dbr;
		    }catch(Exception e){
			    e.printStackTrace();
				throw new SQLException("Errore eseguendo una queryDecode()  ");
   	        }finally{
   	            if(!done){
   	                try{
   	                 if(dbcur!=null) {
   	  	        	dbcur.close();
   	  	        }
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


public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
     String scr=" ";
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        String myselect="Select * from sussidi";

      //controllo valore corretto descrizione_suss

        scr=(String)(h.get("descrizione_suss"));
	    if (!(scr==null))
            if (!(scr.equals(" "))){
               scr=duplicateChar(scr,"'");
               myselect=myselect+" where descrizione_suss like '"+scr+"%'";
              }
        myselect=myselect+" ORDER BY descrizione_suss ";
        System.out.println("query GridSussidi: "+myselect);
        ISASCursor dbcur=dbc.startCursor(myselect);
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
   	    if(!done){
   	        try{
		dbc.close();
	super.close(dbc);
	}catch(Exception e1){System.out.println(e1);}
   	        }
   	}

}
private Vector getPaginate(ISASConnection dbc,Hashtable h)throws Exception {
	 String myselect="Select * from sussidi where 0=0 ";
	 ISASCursor dbcur=null;
     //controllo valore corretto descrizione_suss
	  try{
		  
    String  scr=(String)(h.get("descrizione_suss"));
	    if (!(scr==null))
           if (!(scr.equals(" ")))
             {
              scr=duplicateChar(scr,"'");
              myselect=myselect+" and descrizione_suss like '"+scr+"%'";
             }
	      myselect=myselect+" ORDER BY descrizione_suss ";
       System.out.println("query GridSussidi: "+myselect);
        dbcur=dbc.startCursor(myselect);
       //Vector vdbr=dbcur.getAllRecord();
       int start = Integer.parseInt((String)h.get("start"));
       int stop = Integer.parseInt((String)h.get("stop"));
       Vector vdbr = dbcur.paginate(start, stop);
       if ((vdbr != null) && (vdbr.size() > 0))
       for(int i=0; i<vdbr.size()-1; i++){
         ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
         if(dbr.get("socsan_suss")!=null)
           if(((String)dbr.get("socsan_suss")).equals("0"))
             dbr.put("descr_flag","Sociale");
           else if(((String)dbr.get("socsan_suss")).equals("1"))
             dbr.put("descr_flag","Sanitario");
           else
             dbr.put("descr_flag","");
         else
             dbr.put("descr_flag","");
         dbr.put("des_sottospec",util.getDecode(dbc,"co_sottsuss","ss_codice",
                 (String)util.getObjectField(dbr,"codice_ss",'S'),"ss_descrizione"));
         dbr.put("des_area",util.getDecode(dbc,"co_area","ar_area",
                 (String)util.getObjectField(dbr,"codice_ar",'S'),"ar_descrizione"));
       }
       return vdbr;
	  }catch(Exception e){
   		e.printStackTrace();
   		throw new SQLException("Errore eseguendo una query()  ");
      	}finally{
      		if(dbcur!=null)
      	    	dbcur.close();      	      
      	   }
}

private Vector getPaginateFiltri(ISASConnection dbc,Hashtable h)throws Exception {
	 ISASCursor dbcur=null;
     //controllo valore corretto descrizione_suss
	  try{
		  String myselect="Select s.*,e.* " +
  		" from sussidi s,co_economici e where 0=0";
			  myselect += " AND capitolo_suss =eco_codice";
			//controllo valore corretto descrizione_suss
			  Object scr=h.get("descrizione_suss");
			  if (!(scr==null))
			      if (!(scr.toString().equals(" ")))
			        {
			         scr=duplicateChar(scr.toString(),"'");
			         myselect=myselect+" and descrizione_suss like '"+scr+"%'";
			        }
			  if(h.containsKey("eco_codice") && !h.get("eco_codice").toString().equals(""))
				{
					 myselect += " AND eco_codice= '" + h.get("eco_codice").toString().trim() + "' " ;				 
				}
			  myselect += " AND (flag_auto is null or flag_auto='S')";//bargi 15/04/2013 x non elencare quelli obsoleti
			  if(h.containsKey("eco_anno")&& !h.get("eco_anno").toString().equals(""))
					 myselect +=" and eco_anno= '" + h.get("eco_anno") + "' ";
				else  myselect +=" and eco_anno=(select max(eco_anno) from co_economici c2 where c2.eco_comune=e.eco_comune" +
				 		" and c2.eco_codice=s. capitolo_suss  )";
			
				if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
				{
					 myselect += " AND eco_comune= '" + h.get("cod_ccosto").toString().trim() + "' " ;
				//	 if(h.containsKey("eco_anno")&& !h.get("eco_anno").toString().equals(""))
				//		 myselect +=" and eco_anno= '" + h.get("eco_anno") + "' ";
			//		 else  myselect +=" and eco_anno=(select max(eco_anno) from co_economici c2 where c2.eco_comune= '" + h.get("cod_ccosto").toString().trim() + "' )";
					 
				}
				if(h.containsKey("con_socsan")&& !h.get("con_socsan").toString().equals(""))
				{			
					  myselect=myselect+" and (socsan_suss is null or socsan_suss='"+h.get("con_socsan") +"')";
				}
				if(h.containsKey("filtro_serv")&&h.get("filtro_serv").toString().equals("S"))
				{
					  myselect=myselect+" and codice_suss in (select cod_sussidio from "
					  +" tab_serv_sussidi t where "
					  + " cod_settore_interv='"+h.get("settore_interv").toString().trim()+"'"
					  + " and cod_tipo_interv='" +h.get("tipo_interv").toString().trim()+"'"
					  + " and cod_intervento='"+h.get("cod_interv").toString().trim()+"'";
					  if(h.containsKey("cod_servizio")&& !h.get("cod_servizio").toString().equals(""))	
						  myselect+= " and cod_servizio='"+h.get("cod_servizio").toString().trim()+"'";				
					
					  //codice comune residenza
					  //se non ho comune residenza prendo in base al codice del centro costo in co_assoc_ccosto
					  //dove ho ragruppamento dei centro di costo.
					  if(h.containsKey("cod_comune_res")&& !h.get("cod_comune_res").toString().equals(""))					
					  myselect += " and cod_comune='" +h.get("cod_comune_res").toString().trim()+"'";
				/*	  else if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
							{
						  myselect += " and (t.cod_comune in " +
							"( select cod_centrocosto from co_assoc_ccosto where cod_centrocosto_padre='"+ h.get("cod_ccosto").toString().trim()+"')" +
									" OR t.cod_comune='"+ h.get("cod_ccosto").toString().trim()+"')" ;
					  }  */else if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
							{
						  myselect += " and (t.cod_comune in " +
							"( select cod_comune from assoc_ccosto_comuni where" +
							" cod_centrocosto='"+ h.get("cod_ccosto").toString().trim()+"'"+
							" and tipo='C' "+
							" )" +
									" OR t.cod_comune='"+ h.get("cod_ccosto").toString().trim()+"')" ;
					  }
					  	myselect +=")";
					   
				}
			  myselect=myselect+" ORDER BY descrizione_suss ";
			  LOG.info("query GridSussidi: "+myselect);
			 dbcur=dbc.startCursor(myselect);
			 // Vector vdbr=dbcur.getAllRecord();
			  int start = Integer.parseInt((String)h.get("start"));
			  int stop = Integer.parseInt((String)h.get("stop"));
			  Vector vdbr = dbcur.paginate(start, stop);
			  if ((vdbr != null) && (vdbr.size() > 0))
			  for(int i=0; i<vdbr.size()-1; i++){
			    ISASRecord dbr=(ISASRecord)vdbr.elementAt(i);
			    LOG.info("query GridSussidi letto: "+dbr.getHashtable().toString());
			    String comune="";
			    if(dbr.get("eco_comune")!=null)
			  	  comune=dbr.get("eco_comune").toString().trim();
			    getDecodifiche(dbc,dbr,h,comune);    
			  }
			  dbcur.close();    
       return vdbr;
	  }catch(Exception e){
   		e.printStackTrace();
   		throw new SQLException("Errore eseguendo una queryPaginate()  ");
      	}finally{
      		if(dbcur!=null)
      	    	dbcur.close();      	      
      	   }
}
public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
     String scr=" ";
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        Vector vdbr=getPaginate(dbc,h);
        dbc.close();
        super.close(dbc);
        done=true;
        return vdbr;
    }catch(Exception e){
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

public Vector queryPaginateFiltri(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false;
    ISASConnection dbc=null;
    ISASCursor dbcur=null;
    Vector vdbr =null;
    try{
        dbc=super.logIn(mylogin);
        System.out.println("Kuser="+dbc.getKuser());
        String co_filtro_conti ="NO";//autorizzo da sins
        co_filtro_conti = selectConf(dbc, "", "CO_FILTRO_CONTI");
		
        LOG.info("queryPaginateFiltri parametri "+h.toString());
        
    	if(co_filtro_conti!=null && co_filtro_conti.equals("NO"))
		{
    		//si esegue vecchia select
    		vdbr=getPaginate(dbc,h);
		}else {
			vdbr=getPaginateFiltri(dbc,h);
		}
        dbc.close();
        super.close(dbc);
        done=true;
        System.out.println("vettrore size= "+vdbr.size());
        return vdbr;
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryPaginateFiltri()  ");
   	}finally{
   	    if(!done){
   	        try{
   	        	if(dbcur!=null)
   	         dbcur.close();
					dbc.close();
				super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
   	        }
   	}

}
private void getDecodifiche(ISASConnection dbc, ISASRecord dbr,Hashtable h,String comune) throws Exception {
	LOG.info("getDecodifiche invocato ");
    it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();  
    if(dbr.get("socsan_suss")!=null)
    if(((String)dbr.get("socsan_suss")).equals("0"))
      dbr.put("descr_flag","Sociale");
    else if(((String)dbr.get("socsan_suss")).equals("1"))
      dbr.put("descr_flag","Sanitario");
    else
      dbr.put("descr_flag","");
  else
      dbr.put("descr_flag","");
    dbr.put("des_sottospec",util.getDecode(dbc,"co_sottsuss","ss_codice",
            (String)util.getObjectField(dbr,"codice_ss",'S'),"ss_descrizione"));
    dbr.put("des_area",util.getDecode(dbc,"co_area","ar_area",
            (String)util.getObjectField(dbr,"codice_ar",'S'),"ar_descrizione"));
  	 //info servizio
  	   	 
  	if(h.containsKey("filtro_serv")&&h.get("filtro_serv").toString().equals("S"))
	{	LOG.info("getDecodifiche: servizio inizio ");
		 String myselect="select  ser.descservizio,s.tipo_contrib,t.* from "
		  +" tab_serv_sussidi s,tab_tipo_servizi ser,tab_servizi t where "
		  +" s.cod_sussidio='"+dbr.get("codice_suss")+"'"
		  + " and s.cod_settore_interv='"+h.get("settore_interv").toString().trim()+"'"
		  + " and s.cod_tipo_interv='" +h.get("tipo_interv").toString().trim()+"'"
		  + " and s.cod_intervento='"+h.get("cod_interv").toString().trim()+"'"
		  + " and ser.cod_settore_interv='"+h.get("settore_interv").toString().trim()+"'"
		  + " and ser.cod_tipo_interv='" +h.get("tipo_interv").toString().trim()+"'"
		  + " and ser.cod_intervento='"+h.get("cod_interv").toString().trim()+"'"
		  + " and ser.cod_servizio=s.cod_servizio"
		  + " and t.cod_settore_interv='"+h.get("settore_interv").toString().trim()+"'"
		  + " and t.cod_tipo_interv='" +h.get("tipo_interv").toString().trim()+"'"
		  + " and t.cod_intervento='"+h.get("cod_interv").toString().trim()+"'"
		  + " and t.cod_servizio=s.cod_servizio"
		  + " and t.cod_comune=s.cod_comune";  
		 
		  if(h.containsKey("cod_servizio")&& !h.get("cod_servizio").toString().equals(""))	
			  myselect+= " and t.cod_servizio='"+h.get("cod_servizio").toString().trim()+"'";				
			
		  if(h.containsKey("cod_comune_res")&& !h.get("cod_comune_res").toString().equals(""))					
		  myselect += " and s.cod_comune='" +h.get("cod_comune_res").toString().trim()+"'";
		/*  else if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
		  {
			  myselect +=   " and (s.cod_comune in " +
				"( select cod_centrocosto from co_assoc_ccosto where cod_centrocosto_padre='"+comune+"')" +
						" OR s.cod_comune='"+ h.get("cod_ccosto").toString().trim()+"')" ;
		  }*/  else if(h.containsKey("cod_ccosto") && !h.get("cod_ccosto").toString().equals(""))
			{
			  myselect += " and (s.cod_comune in " +
				"( select cod_comune from assoc_ccosto_comuni where" +
				" cod_centrocosto='"+comune.trim()+"'"+
				" and tipo='C' "+
				" )" +
						" OR s.cod_comune='"+ h.get("cod_ccosto").toString().trim()+"')" ;
		  }
		  LOG.info("getDecodifiche: servizio:  "+myselect);
		  ISASRecord dbrServ=dbc.readRecord(myselect) ;
		  	 if(dbrServ!=null) {
		  		 dbr.put("descservizio",dbrServ.get("descservizio"));
		  		 dbr.put("cod_servizio",dbrServ.get("cod_servizio"));
		  		dbr.put("costounitario",dbrServ.get("costounitario"));
		  		dbr.put("unimis_desc",dbrServ.get("unimis_desc"));
		  		dbr.put("tipo_auto",dbrServ.get("tipo_auto"));
		  		dbr.put("tipo_contrib",dbrServ.get("tipo_contrib"));
		  	 }
	}
}
public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
    boolean done=false;
    String codice=null;
    ISASConnection dbc=null;
    try {
        codice=(String)h.get("codice_suss");
    }catch (Exception e){
        e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        ISASRecord dbr=dbc.newRecord("sussidi");
        Enumeration n=h.keys();
        while(n.hasMoreElements()){
            String e=(String)n.nextElement();
            dbr.put(e,h.get(e));
        }
        dbc.writeRecord(dbr);
        String myselect="Select * from sussidi where codice_suss='"+codice+"'";
		dbr=dbc.readRecord(myselect);
		if(dbr!=null){
	          dbr.put("des_sottospec",util.getDecode(dbc,"co_sottsuss","ss_codice",
	                        (String)util.getObjectField(dbr,"codice_ss",'S'),"ss_descrizione"));
	        }
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
    }catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una insert() - "+  e1);
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
        codice=(String)dbr.get("codice_suss");
    }
    catch (Exception e){
        e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
    }
    try{
        dbc=super.logIn(mylogin);
        dbc.writeRecord(dbr);
        String myselect="Select * from sussidi where codice_suss='"+codice+"'";
		dbr=dbc.readRecord(myselect);
		  if(dbr!=null){
	          dbr.put("des_sottospec",util.getDecode(dbc,"co_sottsuss","ss_codice",
	                        (String)util.getObjectField(dbr,"codice_ss",'S'),"ss_descrizione"));
	        }
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
    }
    catch(DBRecordChangedException e){
        e.printStackTrace();
		throw e;
    }
    catch(ISASPermissionDeniedException e){
        e.printStackTrace();
		throw e;
    }

    catch(Exception e1){
        System.out.println(e1);
		throw new SQLException("Errore eseguendo una update() - "+  e1);
    }
    finally{
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
private String selectConf(ISASConnection dbc, String strCodOperatore, String key) throws SQLException {
	String ret =null;
	String strZona = "";
	String strZonaOper = "";
	try {
		if (!strCodOperatore.equals("")) {
			strZonaOper = getZonaFromOperatore(dbc, strCodOperatore);
			if (strZonaOper.trim().equals(""))// non esiste zona per l'oper
				System.out.println("!!!! SussidiEJB.selectConf: NON esiste zona su OPERATORI per l'operatore=["
						+ strCodOperatore + "] !!!!");
		}

		String mysel = "SELECT conf_txt FROM conf WHERE " + "conf_kproc='SINS' AND conf_key='" + key + "'";
		System.out.println("SussidiEJB /selectConf/mysel: " + mysel);
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
		throw new SQLException("SussidiEJB.selectConf()-->ERRORE NEL REPERIMENTO CHIAVE DAL CONF" + ex);
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
		System.out.println("!!!! SussidiEJB.getValxZona: NON esiste codifica su CONF per la zona=[" + zonaOper
				+ "] !!!!");
	return rit;
}


private String getZonaFromOperatore(ISASConnection dbc, String strCodOperatore) throws SQLException {
	try {
		String ret = "";
		String strQuery = "SELECT cod_zona" + " FROM operatori" + " WHERE codice = '" + strCodOperatore + "'";
		System.out.println("SussidiEJB /getZonaFromOperatore/strQuery: " + strQuery);
		ISASRecord dbr = dbc.readRecord(strQuery);
		if ((dbr != null) && dbr.get("cod_zona") != null)
			ret = (String) dbr.get("cod_zona");
		return ret;
	} catch (Exception ex) {
		throw new SQLException("SussidiEJB.getZonaFromOperatore() " + ex);
	}
}


public Boolean checkContribGrad(myLogin mylogin,Hashtable h) throws  SQLException {
    boolean done=false,ret=false;
     String codice_suss;
    ISASConnection dbc=null;
    try{
        dbc=super.logIn(mylogin);
        codice_suss=(String)(h.get("codice_suss"));
        String myselect="Select * from sussidi where codice_suss = '"+codice_suss+"'";
        System.out.println("query checkContribGrad: "+myselect);
        ISASRecord dbr = dbc.readRecord(myselect);
        if (dbr.get("flag_grad")!=null && dbr.get("flag_grad").toString().equals("S"))
        	ret=true;
		dbc.close();
	    super.close(dbc);
	    done=true;
		return new Boolean(ret);
    }catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una checkContribGrad()  ");
   	}finally{
   	    if(!done){
   	        try{
		dbc.close();
	super.close(dbc);
	}catch(Exception e1){System.out.println(e1);}
   	        }
   	}

}


}

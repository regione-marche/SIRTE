package it.caribel.app.sinssnt.bean;

//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 27/08/2002 - EJB di connessione alla procedura SINS Tabella Intpre
//
// giulia brogi
//
// Bargi: 24 Gennaio 2005 inserito gestione flag per flussi:int_flag_flussi di INTERV
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.exception.*;
import it.pisa.caribel.sinssnt.casi_adrsa.EveUtils;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.ISASUtil;

public class IntpreEJB extends SINSSNTConnectionEJB  {
public IntpreEJB() {}

	// 11/06/07 m.: Nel metodo "queryKey()" -> aggiunto "catch" dell'ISASPermissioneDenied  
	//		x rilanciare una CariException da gestire latoClient;


	// 11/06/07
	private String msgNoD = "Mancano i diritti per leggere il record";
	private static final String MIONOME ="2-IntpreEJB.";
	private static final String CONSTANTS_ABL_FLUSSI_SPR ="ABL_GST_SPR";
	private static final String CONSTANTS_FLAG_INVIATO_INSERIMENTO = "0"; // il record viene inserito
	private static final String CONSTANTS_FLAG_INVIATO_INVIATO = "1"; // il record � stato modificato
	private static final String CONSTANTS_FLAG_INVIATO_VARIATO = "2"; // il record viene modificato
	
	
public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException, CariException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * FROM intpre WHERE "+
                                "pre_cod_prest="+(String)h.get("pre_cod_prest")+" and "+
                                "pre_anno='"+(String)h.get("pre_anno")+"' and "+
                                "pre_contatore="+(String)h.get("pre_contatore");
		System.out.println("QueryKey Intpre:"+myselect);
		ISASRecord dbr=dbc.readRecord(myselect);
 		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}
	// 11/06/07 ---
	catch(ISASPermissionDeniedException e){
      	System.out.println("IntpreEJB.queryKey(): "+e);
		throw new CariException(msgNoD, -2);
    }
	catch(Exception e){
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
		String myselect="Select * FROM intpre WHERE "+
                                "pre_anno ='"+(String)h.get("pre_anno")+"' and "+
                                "pre_contatore="+(String)h.get("pre_contatore")+
                                " ORDER BY pre_progr desc";
	 	System.out.println("Query IntPre:"+myselect);
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


public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String anno=null;
	String intervento=null;
	ISASConnection dbc=null;
        boolean aggiornato=false;
	try {
		anno=(String)h.get("pre_anno");
		intervento=(String)h.get("pre_contatore");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		System.out.println("in INSERT INTPRE:"+h.toString());
		//inserimento prestazione in intpre
		ISASRecord dbprest=dbc.newRecord("intpre");
		dbprest.put("pre_anno",anno);
		dbprest.put("pre_contatore",intervento);

		//recupero e incremento int_max_prest
		String selmax="SELECT * FROM interv "+
				"WHERE int_anno="+anno+
				" AND int_contatore="+intervento;
		String selmax1="SELECT max(pre_progr) as int_max_prest from intpre where pre_anno="+anno+
				" AND pre_contatore="+intervento;
		
		ISASRecord dbmax1=dbc.readRecord(selmax1);
		ISASRecord dbmax=dbc.readRecord(selmax);

		if (dbmax!=null){
			if(dbmax1.get("int_max_prest")!=null){
				int max=((Integer)dbmax1.get("int_max_prest")).intValue();
				max++;
				//aggiornamento tabella interv
				dbmax.put("int_max_prest",new Integer(max));
				if(dbmax.get("int_flag_flussi")!=null &&
						!((String)dbmax.get("int_flag_flussi")).equals("0")){
					dbmax.put("int_flag_flussi","3");
					aggiornato=true;
				}
				dbc.writeRecord(dbmax); //---fine aggiornamento tab interv
				dbprest.put("pre_progr",new Integer(max));
			}
		}
                if(aggiornato==false)//questo perche' ci sono degli interventi in cui
                //int_max_prest � nullo

                //JESSY031203 dbprest.put("pre_contatto",(new Integer(0)));
                System.out.println("Prestazione: "+h.get("pre_cod_prest"));
                if (h.get("pre_cod_prest")!=null)
		 dbprest.put("pre_cod_prest",(String)h.get("pre_cod_prest"));
                if (h.get("pre_des_prest")!=null)
		 dbprest.put("pre_des_prest",(String)h.get("pre_des_prest"));
                if (h.get("pre_tempo")!=null)
                  dbprest.put("pre_tempo",(String)h.get("pre_tempo"));
                if (h.get("pre_numero")!=null)
		  dbprest.put("pre_numero",(new Integer((String)h.get("pre_numero"))));
                if (h.get("pre_note")!=null)
                  dbprest.put("pre_note",(String)h.get("pre_note"));
                if (h.get("pre_importo")!=null)
                  dbprest.put("pre_importo",(String)h.get("pre_importo"));

                Enumeration n=h.keys();
                while(n.hasMoreElements()){
                        String e=(String)n.nextElement();
                        dbprest.put(e,h.get(e));
                }
                dbc.writeRecord(dbprest);
                gestioneFlagFlussi(dbc,intervento,anno);
                String codOperatore = dbc.getKuser().trim();
                String pre_cod = ISASUtil.getValoreStringa(h, "pre_cod_prest");
                boolean abilitazioneFlussiSPR=recuperaAblFlussiSPR(dbc, codOperatore);
                if (abilitazioneFlussiSPR){
                	modificaContattoFisioterapista(pre_cod, anno, intervento, dbc);      
                }
                
		dbc.close();
		super.close(dbc);
		done=true;
		return dbprest;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e);
		}
		throw e;
	}catch(Exception e){
		e.printStackTrace();
		try{
			dbc.rollbackTransaction();
		}catch(Exception e1){
			throw new DBRecordChangedException("Errore eseguendo una rollback() - "+  e);
		}
		throw new SQLException("Errore eseguendo una insert() - ",  e);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}

public ISASRecord salva(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String pre_cod=null;
	String pre_anno=null;
	String pre_contatore=null;
	ISASConnection dbc=null;
	try {
		dbc=super.logIn(mylogin);
		pre_cod=(String)h.get("pre_cod_prest");
		pre_anno=(String)h.get("pre_anno");
		pre_contatore=(String)h.get("pre_contatore");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		String sel="SELECT * FROM intpre WHERE "+
                           "pre_anno='"+pre_anno+
                           "' AND pre_cod_prest='"+pre_cod+
                           "' AND pre_contatore="+pre_contatore;
                System.out.println("Select in intpre su salva: "+sel);
		ISASRecord dbr=dbc.readRecord(sel);
		if (dbr!=null){
                        String num=(String)h.get("pre_numero");
                        dbr.put("pre_numero",new Integer(num));
                        String imp=(String)h.get("pre_importo");
                        dbr.put("pre_importo",imp);
                        String note=(String)h.get("pre_note");
                        dbr.put("pre_note",note);
		}
		ISASRecord dbprest=dbc.newRecord("intpre");
                Enumeration n=dbr.getHashtable().keys();
                while(n.hasMoreElements()){
                        String e=(String)n.nextElement();
	                dbprest.put(e,dbr.get(e));
                }
		dbc.deleteRecord(dbr);
		dbc.writeRecord(dbprest);
		String myselect="Select * FROM intpre WHERE "+
                                "pre_cod_prest='"+pre_cod+"' and "+
                                "pre_anno='"+pre_anno+"' and "+
                                "pre_contatore="+pre_contatore;
		dbr=dbc.readRecord(myselect);
                gestioneFlagFlussi(dbc,pre_contatore,pre_anno);
        String codOperatore = dbc.getKuser().trim();
        boolean abilitazioneFlussiSPR=recuperaAblFlussiSPR(dbc, codOperatore);
        if (abilitazioneFlussiSPR){
        	modificaContattoFisioterapista(pre_cod, pre_anno, pre_contatore, dbc);      
        }
                
		dbc.close();
		super.close(dbc);
		done=true;
                //if(dbr!=null)
                  //System.out.println("Ritorno: "+(dbr.getHashtable()).toString());
		return dbr;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e1){
		System.out.println(e1);
		throw new SQLException("Errore eseguendo una update() - ",  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}


private void modificaContattoFisioterapista(String pre_cod, String pre_anno, String pre_contatore, ISASConnection dbc) {
	String punto = MIONOME + "modificaContattoFisioterapista ";
	String nCartella = "";
	String dataPrestazione = "";
	String query = " select int_cartella, int_contatto, int_data_prest from interv where int_anno = '" +pre_anno+ "' and int_contatore = "+ 
	pre_contatore;
	try {
		ISASRecord dbrInterv = dbc.readRecord(query);
		nCartella = ISASUtil.getValoreStringa(dbrInterv, "int_cartella");
		dataPrestazione = ISASUtil.getValoreStringa(dbrInterv, "int_data_prest");
		
	} catch (Exception e) {
		stampa(punto + "Errore nel recuperare la chiave di intervento ");
	}
	if (ISASUtil.valida(nCartella) && ISASUtil.valida(dataPrestazione)){
		aggiornaContattoFisioterapistaFlussiSPR(dbc, pre_cod,nCartella, dataPrestazione);
	}else {
		stampa(punto + "Non sono validi cartella>"+nCartella + "< dataPrestazione>"+dataPrestazione+"<");
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


	private void aggiornaContattoFisioterapistaFlussiSPR(ISASConnection dbc, String codPrestazione, String n_cartella,
			String dataPrestazione) {
		String punto = MIONOME + "aggiornaContattoFisioterapistaFlussiSPR ";
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
						String updateSkfis = "UPDATE skfis set flag_inviato = '" + CONSTANTS_FLAG_INVIATO_VARIATO + "' where n_cartella = "
								+ n_cartella + " and n_contatto = " + contatto;
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


public void delete(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String sel_del="SELECT * FROM intpre WHERE"+
                               " pre_anno="+(String)h.get("pre_anno")+
                               " AND pre_contatore="+ ISASUtil.getValoreStringa(h, "pre_contatore")+
                               " AND pre_cod_prest='"+(String)h.get("pre_cod_prest")+"'";
		ISASRecord dbr=dbc.readRecord(sel_del);
		if (dbr!=null){
                 dbc.deleteRecord(dbr);
                 gestioneFlagFlussi(dbc, ISASUtil.getValoreStringa(h, "pre_contatore"), (String)h.get("pre_anno"));
                }
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
private void gestioneFlagFlussi(ISASConnection dbc,String contatore,String anno)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException
{
	try{
          String myselect="Select * from interv where "+
                                "int_anno='"+anno+"' and "+
                                "int_contatore="+contatore;
          ISASRecord dbr=dbc.readRecord(myselect);
          if(dbr!=null){
            if (dbr.get("int_flag_flussi")!=null &&
            !((String)dbr.get("int_flag_flussi")).equals("0"))
            {
                dbr.put("int_flag_flussi","3");
                dbc.writeRecord(dbr);
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

}

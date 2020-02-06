package it.pisa.caribel.sinssnt.controlli;
//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------
//30/09/2008 Barbara Giannattasio
//estrapolato da EJB lì'aggiornamento di ass_anagrafica (scheda pua) che nel corso dell'iter viene aggiornato 
//secondo i casi anche dalla procedura sinssnt. Per cui questa classe deve essere presente sia sotto la procedura PUA che SINS
//==========================================================================

import java.util.*;
import java.sql.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class CollSinsPua extends SINSSNTConnectionEJB
{
	myLogin mylogin_global=null;
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	String messaggio_carica="";
	String messaggio_scarica="";
	public CollSinsPua() {}
	/** designaOperatore
    @param dbc isasConnection
    @param h hashtable contenente: "data"--> data appuntamento o presa carico
    																	"num_scheda"--> numero della scheda di segnalazione (chiave ass_anagrafica)
    																	"cod_oper"-->l'operatore che si prende in carico il caso
    @param operazione : se "I"== Inserisco i riferimenti dell'operatore se "R" tolto i riferimenti dell'operatore																	
    */
	public void designaOperatore(ISASConnection dbc,Hashtable h,String operazione)	throws Exception	{
	try{
		//Hashtable h: data, num_scheda, cod_oper
		updateAssAnag(dbc,h,operazione);	
		//	operazione R --> rimosso  I -->inserim		
	}catch(ISASPermissionDeniedException e){
		System.out.println("CollSinsPua.designaOperatore(): Eccezione= " + e);
		throw new ISASPermissionDeniedException("Errore eseguendo una designaOperatore() - "+  e);
	}catch(Exception e){
		System.out.println("designaOperatore(): Eccezione= " + e);
		throw new Exception("Errore eseguendo una designaOperatore() - "+  e);
	}
	}
	private void updateAssAnag(ISASConnection dbc,Hashtable h,String tipo)
	throws Exception	{
		//tipo R --> rimosso  I -->inserim
		try{
			String tipo_op=getTipoOperatore(dbc,h.get("cod_oper").toString());
			System.out.println("updateAssAnag(): h=["+h.toString()+"] tipoOp="+tipo_op);
			String data_vis=""+h.get("data");
			String sel="select * from ass_anagrafica where progressivo="+h.get("num_scheda");
			debugMessage("updateAssAnag select ass_anagrafica per aggiornamento \n"+sel);
			ISASRecord dbr=dbc.readRecord(sel);
			if(dbr!=null){
				if (tipo_op.equals("01")){//ass soc
					if(tipo.equals("I")){
						dbr.put("soc_data",ndf.formDate(data_vis,"aaaa-mm-gg"));
						dbr.put("soc_cod",h.get("cod_oper"));
						//gb 03/11/08
						String strCodOper = (String) h.get("cod_oper");
						String strCodPresidio = util.getDecode(dbc, "operatori", "codice", strCodOper,
										"cod_presidio", "");
						dbr.put("soc_cod_presidio",strCodPresidio);
						//gb 03/11/08: fine
					}else if(tipo.equals("R")){
						dbr.put("soc_data",null);
						dbr.put("soc_cod","");
						dbr.put("soc_cod_presidio","");	//gb 03/11/08
					}
				}else if (tipo_op.equals("02")){//inferm
					if(tipo.equals("I")){
						dbr.put("san_data",ndf.formDate(data_vis,"aaaa-mm-gg"));
						dbr.put("san_cod",h.get("cod_oper"));
						//gb 03/11/08
						String strCodOper = (String) h.get("cod_oper");
						String strCodPresidio = util.getDecode(dbc, "operatori", "codice", strCodOper,
										"cod_presidio", "");
						dbr.put("san_cod_presidio",strCodPresidio);
						//gb 03/11/08: fine
					}else if(tipo.equals("R")){
						dbr.put("san_data",null);
						dbr.put("san_cod","");
						dbr.put("san_cod_presidio","");	//gb 03/11/08								
					}
				}
				if(dbr.get("pr_data")!=null && !(dbr.get("pr_data").toString()).equals("")&&
						dbr.get("pr_progr")!=null && !(dbr.get("pr_progr").toString()).equals("")){
					updatePuauvm(dbc,dbr);
				}
				debugMessage("aggiorno ass_anagrafica "+dbr.getHashtable().toString());
				dbc.writeRecord(dbr);				
			}
		}catch(ISASPermissionDeniedException e){
			System.out.println("CollSinsPua.updateAssAnag(): Eccezione= " + e);
			throw new ISASPermissionDeniedException("Errore eseguendo una updateAssAnag() - "+  e);
		}catch(Exception e){
			System.out.println("updateAssAnag(): Eccezione= " + e);
			throw new Exception("Errore eseguendo una updateAssAnag() - "+  e);
		}
	}
	private void updatePuauvm(ISASConnection dbc,ISASRecord dbr)
	throws Exception	{
		try{
			System.out.println("updatePuauvm(): h=["+dbr.getHashtable().toString()+"]");			
			String sel="select * from puauvm where n_cartella="+dbr.get("n_cartella")+
			" and pr_progr="+dbr.get("pr_progr")+
			" and pr_data="+formatDate(dbc,dbr.get("pr_data").toString());
			debugMessage("Select su puauvm per update "+sel);
			ISASRecord dbrP=dbc.readRecord(sel);
			if(dbrP!=null){				
				if ((dbr.get("soc_data") != null) && (dbr.get("soc_cod") != null)) { // 27/10/08
					dbrP.put("pr_soc_data_visita",ndf.formDate(dbr.get("soc_data").toString(),"aaaa-mm-gg"));
					dbrP.put("pr_soc_codice",dbr.get("soc_cod").toString());

					//gb 12/11/08
					if (dbr.get("soc_cod_presidio") != null)
					  dbrP.put("pr_soc_presidio",dbr.get("soc_cod_presidio").toString());
				}
				if ((dbr.get("san_data") != null) && (dbr.get("san_cod") != null)) { // 27/10/08
					dbrP.put("pr_inf_data_visita",ndf.formDate(dbr.get("san_data").toString(),"aaaa-mm-gg"));
					dbrP.put("pr_inf_codice",dbr.get("san_cod").toString());

					//gb 12/11/08
					if (dbr.get("san_cod_presidio") != null)
					  dbrP.put("pr_inf_presidio",dbr.get("san_cod_presidio").toString());
				}

				debugMessage("aggiorno puauvm "+dbrP.getHashtable().toString());
				dbc.writeRecord(dbrP);
			}
		}catch(ISASPermissionDeniedException e){
			System.out.println("CollSinsPua.updatePuauvm(): Eccezione= " + e);
			throw new ISASPermissionDeniedException("Errore eseguendo una updatePuauvm() - "+  e);
		}catch(Exception e){
			System.out.println("updatePuauvm(): Eccezione= " + e);
			throw new Exception("Errore eseguendo una updatePuauvm() - "+  e);
		}
	}
	private String getTipoOperatore(ISASConnection dbc,String codice)throws SQLException, ISASPermissionDeniedException{
		try{
			String mysel ="SELECT tipo from operatori"+
			" where codice='"+codice.trim()+"'";
			ISASRecord dbr=dbc.readRecord(mysel);
			if (dbr != null){
				return (String)dbr.get("tipo");
			}
			return "";
		}catch(ISASPermissionDeniedException e){
			System.out.println("CollSinsPua.getTipoOperatore(): Eccezione= " + e);
			throw new ISASPermissionDeniedException("Errore eseguendo una getTipoOperatore() - "+  e);
		}catch(Exception e1){
			debugMessage(""+e1);
			throw new SQLException("Errore eseguendo una getTipoOperatore() - "+  e1);
		}
		
	}
}

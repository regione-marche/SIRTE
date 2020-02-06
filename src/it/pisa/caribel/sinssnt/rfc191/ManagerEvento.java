package it.pisa.caribel.sinssnt.rfc191;

import java.util.Vector;

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;

public class ManagerEvento extends SINSSNTConnectionEJB  {
	
	public static final String evt_stat_ritorno_ok = "2";//Ritorno di Esito Positivo da R.T.
	public static final String evt_stat_da_inviare = "0"; //Evento da essere inviato
	public static final String evt_stat_in_sospeso = "-9";//Evento in sospeso
	
	public ManagerEvento(){}
	
	public void insertHspBind(ISASConnection dbc,String evt_prog, String tipo_evento, String id_evento, String id_percorso,
			String n_cartella, String n_contatto,
			String int_anno,String int_contatore, String cod_prest ) throws Exception{
		String nomeMetodo = "insertHspBind";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			ISASRecord dbrBIND = dbc.newRecord("hsp_bind");
			
			fase = 20;  dbrBIND.put("evt_prog", evt_prog);
			fase = 30;  dbrBIND.put("tipo_evento",tipo_evento);
			fase = 40;  dbrBIND.put("id_evento", id_evento);
			fase = 50;  dbrBIND.put("id_percorso", id_percorso);
			  
			fase = 60;  dbrBIND.put("n_cartella", n_cartella);
			fase = 70;  dbrBIND.put("n_contatto", n_contatto);
			
			fase = 80;  dbrBIND.put("anno", int_anno);
			fase = 90;  dbrBIND.put("contatore", int_contatore);
			fase = 100; dbrBIND.put("pre_cod_prest", cod_prest);
			
			fase = 110; 
			if(tipo_evento.equals("CA"))//Evento di cancellazione 
				dbrBIND.put("stato", "C");
			else
				dbrBIND.put("stato", "V");//Valido
			
			dbc.writeRecord(dbrBIND);
			
			LOG.info(nomeMetodo+" - fine ");
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: n_cartella="+n_cartella+", n_contatto="+n_contatto+", int_anno="+int_anno+
					", int_contatore="+int_contatore+", cod_prest="+cod_prest);
			throw e;
		}finally{

		}
	}
	
	public boolean esisteEvento(ISASConnection dbc, String n_cartella,String n_contatto,
			String anno, String contatore, String cod_prest, String tipo_evento) throws Exception{
		String nomeMetodo = "esisteEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");

			fase = 10;
			String myselect="Select * " +
					" from hsp_bind " +
					" where 0=0 ";
			fase = 20;
			if(n_cartella!=null && !n_cartella.equals(""))
				myselect+=" and n_cartella = "+n_cartella;
			fase = 30;
			if(n_contatto!=null && !n_contatto.equals(""))	
				myselect+=" and n_contatto = "+n_contatto;
			fase = 40;
			if(anno!=null && !anno.equals(""))	
				myselect+=" and anno = '"+anno+"'";
			fase = 50;
			if(contatore!=null && !contatore.equals(""))	
				myselect+=" and contatore = "+contatore;
			fase = 60;
			if(cod_prest!=null && !cod_prest.equals(""))	
				myselect+=" and pre_cod_prest = "+cod_prest;
			fase = 70;
			if(tipo_evento!=null && !tipo_evento.equals(""))	
				myselect+=" and tipo_evento = '"+tipo_evento+"'";
			
			myselect+="and stato = 'V'";//Valido

			LOG.trace(nomeMetodo+" - myselect: "+myselect);
			
			fase = 80;
			ISASRecord dbrBIND = dbc.readRecord(myselect);
			LOG.info(nomeMetodo+" - fine ");
			
			if(dbrBIND==null)
				return false;
			else
				return true;			

		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: n_cartella="+n_cartella+", n_contatto="+n_contatto+
					", anno="+anno+", contatore="+contatore+", cod_prest="+cod_prest+"" +
					", tipo_evento="+tipo_evento);
			throw e;
		}finally{

		}	
	}
	
	public ISASRecord getEvento(ISASConnection dbc, String evt_prog) throws Exception{
		String nomeMetodo = "getEvento(evt_prog)";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			fase = 10;
			String myselect =
					" Select * " +
					" from hsp_eventi_dati " +
					" where evt_prog = "+evt_prog;

			LOG.trace(nomeMetodo+" - myselect: "+myselect);
			fase = 20;
			ISASRecord dbrEvento = dbc.readRecord(myselect);
			fase = 30;
			LOG.info(nomeMetodo+" - fine ");
			return dbrEvento;
			
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: evt_prog="+evt_prog);
			throw e;
		}finally{

		}	
	}
	
	
	public ISASRecord getEvento(ISASConnection dbc, String n_cartella,String n_contatto,
			String anno, String contatore, String cod_prest, String tipo_evento) throws Exception{
		String nomeMetodo = "getEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");

			fase = 10;
			String myselect="Select * " +
					" from hsp_bind " +
					" where 0=0 ";
			fase = 20;
			if(n_cartella!=null && !n_cartella.equals(""))
				myselect+=" and n_cartella = "+n_cartella;
			fase = 30;
			if(n_contatto!=null && !n_contatto.equals(""))	
				myselect+=" and n_contatto = "+n_contatto;
			fase = 40;
			if(anno!=null && !anno.equals(""))	
				myselect+=" and anno = '"+anno+"'";
			fase = 50;
			if(contatore!=null && !contatore.equals(""))	
				myselect+=" and contatore = "+contatore;
			fase = 60;
			if(cod_prest!=null && !cod_prest.equals(""))	
				myselect+=" and pre_cod_prest = '"+cod_prest+"'";
			fase = 70;
			if(tipo_evento!=null && !tipo_evento.equals(""))	
				myselect+=" and tipo_evento = '"+tipo_evento+"'";
			
			myselect+="and stato = 'V'";//Valido

			LOG.trace(nomeMetodo+" - myselect: "+myselect);

			fase = 80;
			ISASRecord dbrBIND = dbc.readRecord(myselect);
			
			fase = 90;
			if(dbrBIND==null){
				fase = 100;
				LOG.info(nomeMetodo+" - fine ");
				return null;
			}else{
				fase = 110;
				String evt_prog = ""+dbrBIND.get("evt_prog");
				fase = 120;
				ISASRecord dbrEvento = getEvento(dbc, evt_prog);
				LOG.info(nomeMetodo+" - fine ");
				return dbrEvento;
			}

		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: n_cartella="+n_cartella+", n_contatto="+n_contatto+
					", anno="+anno+", contatore="+contatore+", cod_prest="+cod_prest
					+", tipo_evento="+tipo_evento);
			throw e;
		}finally{

		}	
	}
	
	public Vector getEventi(ISASConnection dbc, String n_cartella,String n_contatto,
			String anno, String contatore, String cod_prest, String tipo_evento) throws Exception{
		String nomeMetodo = "getEventi";
		int fase = 0;
		ISASCursor dbcur=null;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			fase = 10;
			String myselect="Select b.n_cartella, b.n_contatto, d.* " +
					" from hsp_bind b, hsp_eventi_dati d " +
					" where b.evt_prog = d.evt_prog ";
			fase = 20;
			if(n_cartella!=null && !n_cartella.equals(""))
				myselect+=" and b.n_cartella = "+n_cartella;
			fase = 30;
			if(n_contatto!=null && !n_contatto.equals(""))	
				myselect+=" and b.n_contatto = "+n_contatto;
			fase = 40;
			if(anno!=null && !anno.equals(""))	
				myselect+=" and b.anno = '"+anno+"'";
			fase = 50;
			if(contatore!=null && !contatore.equals(""))	
				myselect+=" and b.contatore = "+contatore;
			fase = 60;
			if(cod_prest!=null && !cod_prest.equals(""))	
				myselect+=" and b.pre_cod_prest = '"+cod_prest+"'";
			fase = 70;
			if(tipo_evento!=null && !tipo_evento.equals(""))	
				myselect+=" and b.tipo_evento = '"+tipo_evento+"'";
			
			myselect+="and b.stato = 'V'";//Valido

			LOG.trace(nomeMetodo+" - myselect: "+myselect);

			fase = 80;			
			dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			
			LOG.info(nomeMetodo+" - fine ");
			return vdbr;

		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: n_cartella="+n_cartella+", n_contatto="+n_contatto+
					", anno="+anno+", contatore="+contatore+", cod_prest="+cod_prest
					+", tipo_evento="+tipo_evento);
			throw e;
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
		}	
	}
	
	public void inviaEvento(ISASConnection dbc, String evt_prog, String evt_typ) throws Exception{
		String nomeMetodo = "inviaEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			String myselect = "Select * " +
					" from hsp_eventi " +
					" where evt_prog = "+evt_prog;
			
			LOG.trace(nomeMetodo+" - myselect: "+myselect);
			
			fase = 10;
			ISASRecord dbrInvio = dbc.readRecord(myselect);
			String evt_stat = getEvtStatPerInvio(dbc,evt_prog,evt_typ);
			if(dbrInvio==null){
				fase = 20;
				dbrInvio = dbc.newRecord("hsp_eventi");
				dbrInvio.put("evt_prog", evt_prog);
				dbrInvio.put("evt_typ", evt_typ);
				dbrInvio.put("evt_stat", evt_stat);
			}else{
				fase = 30;
				dbrInvio.put("evt_stat", evt_stat);
				/*
				vedi mail del 13/12/2012 di Davide Venturi Codices s.r.l.
				dbrInvio.put("evt_dnot", "");
				dbrInvio.put("evt_drit", ""); 
				dbrInvio.put("evt_msge", ""); 
				dbrInvio.put("evt_idpicasso", ""); 
				dbrInvio.put("evt_idegov", ""); 
				dbrInvio.put("cod_inviante",""); 
				dbrInvio.put("dt_invio", ""); 
				dbrInvio.put("cod_msg", ""); 
				dbrInvio.put("codice_progetto", ""); 
				dbrInvio.put("completa", ""); 
				dbrInvio.put("inviare", "");
				*/ 
			}
			fase = 40;
			dbc.writeRecord(dbrInvio);
			
			//Ripulisco HSP_ERRORI
			ripulisciEventoErrori(dbc,evt_prog);
			
			LOG.info(nomeMetodo+" - fine ");
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: evt_prog="+evt_prog);
			throw e;
		}finally{
			
		}	
	}
	
	public String getEvtStatPerInvio(ISASConnection dbc, String evt_prog, String evt_typ) throws Exception {
		String nomeMetodo = "getEvtStatPerInvio";
		int fase = 0;
		String ret = "";
		try{
			LOG.info(nomeMetodo+" - inizio ");

			if(evt_typ.equals(EvePresaInCarico.tipo_evento)){
				//########################################################################
				//Evento di presa in carico; verifico se il patient_id � stato valorizzato
				//########################################################################
				fase = 10;
				String myselect = 	
						" select * " +
						" from hsp_eventi_dati " +
						" where evt_prog="+evt_prog;
				ISASRecord dbrEventiDati = dbc.readRecord(myselect);
				fase = 20;
				String id_assistito = (String)dbrEventiDati.get("id_assistito");
				fase = 30;
				if(id_assistito!=null && !id_assistito.trim().equals("")){
					//L'evento di presa in carico puo' essere inviato
					ret = evt_stat_da_inviare;
				}else{
					//L'evento di presa in carico deve rimanere sospeso aspettando che il thread
					//bonifichi il patient_id
					ret = evt_stat_in_sospeso;
				}
			}else{
				//#####################################################################################
				//Altri eventi; verifico se il relativo evento di presa in carico e' andato a buon fine.
				//#####################################################################################
				fase = 40;				
				String myselect = 
						" select b.* "+
						" from hsp_bind b "+
						" where (b.n_cartella, b.n_contatto, b.id_percorso) in("+
						"    select b1.n_cartella, b1.n_contatto, b1.id_percorso"+
						"    from hsp_bind b1"+
						"    where b1.evt_prog = "+evt_prog+
						" )"+
						" and b.TIPO_EVENTO = '"+EvePresaInCarico.tipo_evento+"'";
				fase = 60;
				ISASRecord dbrHspBind = dbc.readRecord(myselect);
				fase = 70;
				String evt_prog_presa_in_car = ""+dbrHspBind.get("evt_prog");
				fase = 80;
				//recupero l'esito di trasmissione dell'evento di presa in carico
				myselect = 	" select * " +
							" from hsp_eventi " +
							" where evt_prog="+evt_prog_presa_in_car;
				fase = 90;
				ISASRecord dbrEventi = dbc.readRecord(myselect);
				fase = 100;
				String evt_stat_presa_in_car = ""+dbrEventi.get("evt_stat");
				fase = 110;
				if(evt_stat_presa_in_car.equals(evt_stat_ritorno_ok)){
					//L'evento puo' essere inviato
					ret = evt_stat_da_inviare;
				}else{
					//L'evento deve rimanere sospeso aspettando che la presa in carico riceva l'ok da R.T.
					//e che il thread rimetta in stato da inviare questo evento.
					ret = evt_stat_in_sospeso;
				}
			}
			
			if(ret.equals(evt_stat_da_inviare))
				LOG.info(nomeMetodo+" - Evento evt_prog:"+evt_prog+" puo' essere inviato");
			else if(ret.equals(evt_stat_in_sospeso))
				LOG.info(nomeMetodo+" - Evento evt_prog:"+evt_prog+" rimane sospeso");
			else
				LOG.error(nomeMetodo+" - Evento evt_prog:"+evt_prog+" ha evt_stat:"+ret);
			
			LOG.info(nomeMetodo+" - fine ");
			return ret;
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+e +
					" - Dettagli evento: evt_prog="+evt_prog);
			throw e;
		}finally{

		}	
	}

	public void ripulisciEventoErrori(ISASConnection dbc, String evt_prog) throws Exception{
		String nomeMetodo = "ripulisciEventoErrori";
		int fase = 0;
		ISASCursor dbcur=null;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			fase = 10;
			String myselect = "SELECT * " +
					" FROM hsp_errori " +
					" WHERE evt_prog="+evt_prog;
			
			fase = 20;
			dbcur=dbc.startCursor(myselect);
			fase = 30;
			while(dbcur.next()) {
				fase = 40;
				ISASRecord dbCorr=dbc.readRecord(myselect);
				fase = 50;
				dbc.deleteRecord(dbCorr);
			}
			
			LOG.info(nomeMetodo+" - fine ");
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+e +
					" - Dettagli evento: evt_prog="+evt_prog);
			throw e;
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
		}	
	}

	public void invalidaEventoCancellato(ISASConnection dbc, String evt_prog) throws Exception{
		String nomeMetodo = "invalidaEventoCancellato";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			fase = 10;
			
			String myselect="Select * " +
					" from hsp_bind " +
					" where evt_prog = "+evt_prog;
			LOG.trace(nomeMetodo+" - myselect: "+myselect);
			
			fase = 20;
			ISASRecord dbrBIND = dbc.readRecord(myselect);
			fase = 30;
			dbrBIND.put("stato","C");//Cancellato
			fase = 40;
			dbc.writeRecord(dbrBIND);

			LOG.info(nomeMetodo+" - fine ");
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e +
					" - Dettagli evento: evt_prog="+evt_prog);
			throw e;
		}finally{

		}	

	}
	
	
	
}
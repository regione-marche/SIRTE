package it.pisa.caribel.sinssnt.rfc191;

import java.util.Vector;

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;

public class EveCancellazione extends SINSSNTConnectionEJB  {
	
	public static final String tipo_evento = "CA";
	public static final String tipo_evento_descr = "Cancellazione";
	
	private ManagerCodifiche mc = new ManagerCodifiche(); 
	private ManagerEvento me = new ManagerEvento();    
	
	public EveCancellazione(){}

	
	public void gestisciEvento(ISASConnection dbc,ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "gestisciEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");

			fase = 10;
			String n_cartella = ""+dbrSkMedPal.get("n_cartella");
			String n_contatto = ""+dbrSkMedPal.get("n_contatto");
			Vector vdbr = me.getEventi(dbc, n_cartella, n_contatto,null,null,null,null);
			
			// 08/01/15 mv: aggiunto passaggio codOper
			String codOper = dbrSkMedPal.get("cod_operatore").toString();
			
			LOG.info(nomeMetodo+" - inizio2 - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			
			fase = 20;
			String evt_prog = "";
			String id_evento = "";
			String id_percorso = "";
			String anno = "";
			ISASRecord dbrCorr = null;
			for(int i=0; i<vdbr.size();i++){
				fase = 30;
				dbrCorr = (ISASRecord)vdbr.get(i);
				fase = 40;
				evt_prog 	= ""+dbrCorr.get("evt_prog"); 
				id_evento 	= ""+dbrCorr.get("id_evento"); 
				id_percorso = ""+dbrCorr.get("id_percorso"); 
				anno 		= ""+dbrCorr.get("anno"); 
				fase = 50;
				insertEvento(dbc,n_cartella, n_contatto,evt_prog,id_evento,id_percorso, anno, mc.getCodReg(dbc), mc.getCodAsl4Invio(dbc, codOper));
			}
			
			LOG.info(nomeMetodo+" - fine - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw e;
		}finally{
			
		}	
	}
	
	public void insertEvento(ISASConnection dbc,String n_cartella, String n_contatto,
			String evt_prog, String id_evento, String id_percorso, String anno, String cod_reg, String cod_asl) throws Exception{
		String nomeMetodo = "insertEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio - n_cartella="+n_cartella+" ,n_contatto="+n_contatto+" ,evt_prog="+evt_prog+" ,id_evento="+id_evento);
			
			fase = 10;
			int int_new_evt_prog = selectProgressivo(dbc, "HSP_EVENTI_DATI");
			Integer new_evt_prog = new Integer(int_new_evt_prog);

			fase = 20;
			ISASRecord dbrEvento = dbc.newRecord("hsp_eventi_dati");
			
			fase = 30;  dbrEvento.put("evt_prog", new_evt_prog);
			fase = 40;  dbrEvento.put("tipo_operazione", this.tipo_evento);
			fase = 50;  //dbrPI.put("data_registrazione", sysdate); si usa il trigger perch� non gestiamo anche l'ora
			fase = 60;  dbrEvento.put("id_evento",id_evento); 
			fase = 70;  dbrEvento.put("id_percorso", mc.aggZeri(id_percorso,"P",6));
			fase = 80;  dbrEvento.put("anno",anno);
			fase = 90;  dbrEvento.put("cod_reg_inviante",cod_reg);                                
			fase = 100; dbrEvento.put("cod_asl_inviante",cod_asl); 
			fase = 101; dbrEvento.put("cod_evento_da_eliminare",id_evento);//inutile!? 
			
			fase = 110;
			dbc.writeRecord(dbrEvento);
			
			fase = 120;
			String mySelectEvento = "Select * " +
					" from hsp_eventi_dati " +
					" where evt_prog = "+new_evt_prog;
			fase = 130;
			dbrEvento = dbc.readRecord(mySelectEvento);//sempre rileggere dopo una write!!
			
			fase = 140;
			String dbr_id_evento   = ""+dbrEvento.get("id_evento");  
			String dbr_id_percorso = ""+dbrEvento.get("id_percorso");
			me.insertHspBind(dbc,""+new_evt_prog,this.tipo_evento,dbr_id_evento,dbr_id_percorso,n_cartella,n_contatto,null,null,null);
			
			fase = 150;
			me.inviaEvento(dbc,""+new_evt_prog,this.tipo_evento);
			
			me.invalidaEventoCancellato(dbc,evt_prog);
			
			LOG.info(nomeMetodo+" - fine - n_cartella="+n_cartella+" ,n_contatto="+n_contatto+" ,evt_prog="+evt_prog+" ,id_evento="+id_evento);
			
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli: id_evento="+id_evento+" ,anno="+anno+" ,cod_reg="+cod_reg+" ,cod_asl="+cod_asl);
			throw e;
		}finally{
			
		}	
	}
	
	
	

}

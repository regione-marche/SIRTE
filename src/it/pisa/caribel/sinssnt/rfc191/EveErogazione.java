package it.pisa.caribel.sinssnt.rfc191;

import java.util.Vector;

import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

public class EveErogazione extends SINSSNTConnectionEJB  {
	
	public static final String tipo_evento = "ER";
	public static final String tipo_evento_descr = "Erogazione";
	
	private ManagerCodifiche mc = new ManagerCodifiche(); 
	private ManagerEvento me = new ManagerEvento(); 
	
	private String msgError = "Si e' verificato un errore durante la generazione dell'evento RFC191 per la regione.\n"+
							  "L'evento di PRESA IN CARICO non esiste, impossibile procedere con l'evento di EROGAZIONE!\n";
	
	public EveErogazione(){}
	
	public void gestisciEvento(ISASConnection dbc,String anno, String contatore) throws CariException, Exception{
		String nomeMetodo = "gestisciEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio anno="+anno+" ,contatore="+contatore);
			
			fase = 10;
			ISASRecord dbrInterv = mc.getIntervento(dbc, anno, contatore);
			fase = 20;
			String int_cartella = ((Object)ISASUtil.getObjectField(dbrInterv,"int_cartella",'O')).toString();
			fase = 30;
			String int_contatto = ((Object)ISASUtil.getObjectField(dbrInterv,"int_contatto",'O')).toString();
			fase = 40;
			ManagerEvento me = new ManagerEvento(); 
			if(!me.esisteEvento(dbc,int_cartella,int_contatto,null,null,null,EvePresaInCarico.tipo_evento)){
				throw new CariException(msgError);
			}

			fase = 50;
			if(me.esisteEvento(dbc,null,null,anno,contatore,null,this.tipo_evento)){
				fase = 60;				
				// 08/01/15 mv: aggiunto passaggio codOper
				String codOper = dbrInterv.get("int_cod_oper").toString();
				
				fase = 61;
				cancellaEvento(dbc,anno, contatore, codOper);
				fase = 62;
				insertEvento(dbc,anno, contatore);
			}else{
				fase = 70;
				insertEvento(dbc, anno,contatore);
			}
			
			LOG.info(nomeMetodo+" - fine anno="+anno+" ,contatore="+contatore);
			
		}catch(CariException ce){
			throw ce;
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli erogazione: anno="+anno+" ,contatore="+contatore);
			throw e;
		}finally{
			
		}	
	}
	
	public void insertEvento(ISASConnection dbc,String anno, String contatore) throws Exception{
		String nomeMetodo = "insertEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio anno="+anno+" ,contatore="+contatore);
			
			//Intervento
			fase = 10;
			ISASRecord dbrInterv = mc.getIntervento(dbc, anno, contatore);
			fase = 20;
			String int_cartella = ((Object)ISASUtil.getObjectField(dbrInterv,"int_cartella",'O')).toString();
			fase = 30;
			String int_contatto = ((Object)ISASUtil.getObjectField(dbrInterv,"int_contatto",'O')).toString();
			fase = 40;
			String int_data_prest = ((Object)ISASUtil.getObjectField(dbrInterv,"int_data_prest",'O')).toString();
			fase = 50;
			String int_tipo_oper = ((Object)ISASUtil.getObjectField(dbrInterv,"int_tipo_oper",'O')).toString();
			fase = 51;				
			// 08/01/15 mv: aggiunto passaggio codOper
			String codOper = dbrInterv.get("int_cod_oper").toString();
			
			//Prestazioni
			fase = 60;
			Vector vdbrPrest = mc.getPrestazioni(dbc, anno, contatore);
			
			//Scheda medico Pal
			fase = 70;
			ISASRecord dbrSkMedPal = mc.getSkMedPal(dbc, int_cartella, int_contatto);
			fase = 80;
			String skm_id_percorso = ""+dbrSkMedPal.get("skm_id_percorso");
			fase = 90;
			String skm_anno =mc.getAnnoFromStrSqlDate(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString());
			
			
			fase = 100;
			ISASRecord dbrCorr = null;
			String pre_cod_prest = "";
			String pre_cod_prest_ICD9 = "";
			for(int i=0; i<vdbrPrest.size();i++){
				fase = 110;
				dbrCorr = (ISASRecord)vdbrPrest.get(i);
				pre_cod_prest = ((Object)ISASUtil.getObjectField(dbrCorr,"pre_cod_prest",'O')).toString();
				fase = 111;
				pre_cod_prest_ICD9 = mc.getDecodCodPrestErog(dbc, pre_cod_prest);

				//Solo se la prestazione ha il codice nomenclatore inserisco l'evento
				//altrimenti comunque sarebbe non valido l'evento e non partirebbe in RFC
				if(pre_cod_prest_ICD9!=null && !pre_cod_prest_ICD9.equals("")){
					fase = 120;
					int int_evt_prog = selectProgressivo(dbc, "HSP_EVENTI_DATI");
					Integer evt_prog = new Integer(int_evt_prog);
					fase = 130;
					int int_id_evento = selectProgressivo(dbc, "HSP_ID_EVENTO");
					fase = 140;
					Integer id_evento = new Integer(int_id_evento);
					fase = 150;
					String str_id_evento = mc.aggZeri(id_evento,"P",10);
					fase = 160;
					ISASRecord dbrEvento = dbc.newRecord("hsp_eventi_dati");
					
					fase = 170; dbrEvento.put("evt_prog", evt_prog);
					fase = 180; dbrEvento.put("tipo_operazione", this.tipo_evento);
					fase = 190; //dbrPI.put("data_registrazione", sysdate); si usa il trigger perch� non gestiamo anche l'ora
					fase = 200; dbrEvento.put("id_evento",this.tipo_evento + str_id_evento); 
					fase = 210; dbrEvento.put("cod_reg_inviante",mc.getCodReg(dbc));                                
					fase = 220; dbrEvento.put("cod_asl_inviante",mc.getCodAsl4Invio(dbc, codOper));                                
					fase = 230; dbrEvento.put("id_percorso", mc.aggZeri(skm_id_percorso,"P",6));
					fase = 240; dbrEvento.put("anno",skm_anno);
					fase = 250; dbrEvento.put("data_erogazione", int_data_prest);
					fase = 260; dbrEvento.put("cod_prest_erogata", pre_cod_prest_ICD9);
					fase = 270; dbrEvento.put("cod_tipo_operatore",	mc.getDecodTipoOperatore(int_tipo_oper));
					
					fase = 280;
					dbc.writeRecord(dbrEvento);
					
					fase = 290;
					String mySelectEvento = "Select * " +
							" from hsp_eventi_dati " +
							" where evt_prog = "+evt_prog;
					fase = 300;
					dbrEvento = dbc.readRecord(mySelectEvento);//sempre rileggere dopo una write!!
					
					fase = 310;			
					String dbr_id_evento = ""+dbrEvento.get("id_evento");  
					String dbr_id_percorso = ""+dbrEvento.get("id_percorso");  
					me.insertHspBind(dbc,""+evt_prog,this.tipo_evento,dbr_id_evento,dbr_id_percorso,int_cartella, int_contatto, anno, contatore, pre_cod_prest);
					
					fase = 320;
					me.inviaEvento(dbc,""+evt_prog,this.tipo_evento);
				}else{
					LOG.warn(nomeMetodo+" - ATTENZIONE: per la prestazione con codice: "+pre_cod_prest+" non e' valorizzato il relativo nomenclatore ICD9" +
							" - (Vedi tabella HSP_PRESTAZ colonna PREST_CODREG). NON INSERISCO EVENTO DI EROGAZIONE IN TABELLE DI FRONTIERA! ");
				}
					
				
			}
			
			LOG.info(nomeMetodo+" - fine anno="+anno+" ,contatore="+contatore);
			
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
					+" - Dettagli erogazione: anno="+anno+" ,contatore="+contatore);
			throw e;
		}finally{
			
		}	
	}
	
	// 08/01/15 mv: aggiunto passaggio codOper
	public void cancellaEvento(ISASConnection dbc, String int_anno,String int_contatore,
			String codOper) throws Exception{
		String nomeMetodo = "cancellaEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio anno="+int_anno+" ,contatore="+int_contatore);
			
			//Eventi gia' inviati
			fase = 30;
			Vector vdbrEventiGiaInviati = me.getEventi(dbc, null,null,int_anno,int_contatore,null,this.tipo_evento);
			
			//Procedo alla cancellazione di tutti gli eventi gia' inviati
			fase = 40;
			ISASRecord dbrEvento = null;
			String n_cartella = "";
			String n_contatto = "";
			String evt_prog = "";
			String id_evento = "";
			String id_percorso = "";
			String anno = "";
			for(int i=0; i<vdbrEventiGiaInviati.size();i++){		
				fase = 50;
				dbrEvento = (ISASRecord)vdbrEventiGiaInviati.get(i);
			
				fase = 60;
				n_cartella 	= ""+dbrEvento.get("n_cartella"); 
				n_contatto 	= ""+dbrEvento.get("n_contatto"); 
				evt_prog 	= ""+dbrEvento.get("evt_prog"); 
				id_evento 	= ""+dbrEvento.get("id_evento"); 
				id_percorso = ""+dbrEvento.get("id_percorso"); 
				anno 		= ""+dbrEvento.get("anno"); 
				
				fase = 70;
				EveCancellazione canc = new EveCancellazione();
				canc.insertEvento(dbc,n_cartella,n_contatto,evt_prog,id_evento,id_percorso, anno, mc.getCodReg(dbc), mc.getCodAsl4Invio(dbc, codOper));
				
			}


			LOG.info(nomeMetodo+" - fine anno="+int_anno+" ,contatore="+int_contatore);
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
					+" - Dettagli erogazione: anno="+int_anno+" ,contatore="+int_contatore);
			throw e;
		}finally{

		}
	}

}

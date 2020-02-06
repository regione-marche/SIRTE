package it.pisa.caribel.sinssnt.rfc191;

import java.util.Vector;

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

public class EveConclusione extends SINSSNTConnectionEJB  {
	
	public static final String tipo_evento = "CO";
	public static final String tipo_evento_descr = "Conclusione";
	
	private ManagerCodifiche mc = new ManagerCodifiche();
	private ManagerEvento me = new ManagerEvento();      
	
	public EveConclusione(){}
	
	public void gestisciEvento(ISASConnection dbc,ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "gestisciEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");

			fase = 10;
			String  n_cartella = ""+dbrSkMedPal.get("n_cartella");
			String  n_contatto = ""+dbrSkMedPal.get("n_contatto");
			
			LOG.info(nomeMetodo+" - inizio2 - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
						
			fase = 20;
			boolean possoConcludere = 
					dbrSkMedPal.get("skm_presacarico_data")!=null && 
					dbrSkMedPal.get("skm_presacarico")!=null &&
					((String)dbrSkMedPal.get("skm_presacarico")).equals("S") &&
					dbrSkMedPal.get("skm_data_chiusura")!=null;
			
			if(possoConcludere){
				fase = 30;
				if(me.esisteEvento(dbc,n_cartella,n_contatto,null,null,null,this.tipo_evento)){
					fase = 40;
					updateEvento(dbc,dbrSkMedPal);
				}else{
					fase = 50;
					insertEvento(dbc, dbrSkMedPal);
				}
			}
			
			LOG.info(nomeMetodo+" - fine - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw e;
		}finally{
			
		}	
	}
	
	private void updateEvento(ISASConnection dbc,ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "updateEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			fase = 10;
			String  n_cartella = ""+dbrSkMedPal.get("n_cartella");
			String  n_contatto = ""+dbrSkMedPal.get("n_contatto");
			
			LOG.info(nomeMetodo+" - inizio2 - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			
			fase = 20;
			ISASRecord dbrEvento = me.getEvento(dbc, n_cartella,n_contatto,null,null,null,this.tipo_evento);
			
			fase = 30;
			if(isDatiModificati(dbc,dbrEvento,dbrSkMedPal)){
				fase = 40;
				mergeDati(dbc,dbrEvento,dbrSkMedPal);
				fase = 50;
				dbc.writeRecord(dbrEvento);
				
				fase = 70;
				String evt_prog = ""+dbrEvento.get("evt_prog");
				String tipo_operazione = (String)dbrEvento.get("tipo_operazione");
				fase = 80;
				
				me.inviaEvento(dbc,evt_prog,tipo_operazione);
			}
			
			LOG.info(nomeMetodo+" - fine - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw e;
		}finally{
			
		}	
	}
	
	public void insertEvento(ISASConnection dbc,ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "insertEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			String  n_cartella = ""+dbrSkMedPal.get("n_cartella");
			String  n_contatto = ""+dbrSkMedPal.get("n_contatto");
			
			// 08/01/15 mv: aggiunto passaggio codOper
			String codOper = dbrSkMedPal.get("cod_operatore").toString();
			
			LOG.info(nomeMetodo+" - inizio2 - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			
			fase = 10;
			int int_evt_prog = selectProgressivo(dbc, "HSP_EVENTI_DATI");
			Integer evt_prog = new Integer(int_evt_prog);
			
			fase = 20;
			int int_id_evento = selectProgressivo(dbc, "HSP_ID_EVENTO");
			fase = 30;
			Integer id_evento = new Integer(int_id_evento);
			fase = 40;
			String str_id_evento = mc.aggZeri(id_evento,"P",10);
			fase = 50;
			ISASRecord dbrEvento = dbc.newRecord("hsp_eventi_dati");
			
			fase = 60;  dbrEvento.put("evt_prog", evt_prog);
			fase = 70;  dbrEvento.put("tipo_operazione", this.tipo_evento);
			fase = 80;  //dbrPI.put("data_registrazione", sysdate); si usa il trigger perche' non gestiamo anche l'ora
			fase = 90;  dbrEvento.put("id_evento",this.tipo_evento + str_id_evento); 
			fase = 100; dbrEvento.put("cod_reg_inviante",mc.getCodReg(dbc));                                
			fase = 110; dbrEvento.put("cod_asl_inviante",mc.getCodAsl4Invio(dbc, codOper));                                
			fase = 140; mergeDati(dbc,dbrEvento,dbrSkMedPal);
			
			fase = 150;
			dbc.writeRecord(dbrEvento);
			
			fase = 160;
			String mySelectEvento = "Select * " +
					" from hsp_eventi_dati " +
					" where evt_prog = "+evt_prog;
			fase = 170;
			dbrEvento = dbc.readRecord(mySelectEvento);//sempre rileggere dopo una write!!
			
			fase = 180;
			String dbr_id_evento = ""+dbrEvento.get("id_evento");  
			String dbr_id_percorso = ""+dbrEvento.get("id_percorso");  
			me.insertHspBind(dbc,""+evt_prog,this.tipo_evento,dbr_id_evento,dbr_id_percorso,n_cartella,n_contatto,null,null,null);
			
			fase = 190;
			me.inviaEvento(dbc,""+evt_prog,this.tipo_evento);
			
			LOG.info(nomeMetodo+" - fine - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw e;
		}finally{
			
		}	
	}
	
	// 08/01/15 mv: aggiunto passaggio codOper
	public void cancellaEvento(ISASConnection dbc, String n_cartella,String n_contatto,
			String codOper) throws Exception{
		String nomeMetodo = "cancellaEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			
			fase = 10;
			ISASRecord dbrEvento = me.getEvento(dbc, n_cartella,n_contatto,null,null,null,this.tipo_evento);
			fase = 20;
			EveCancellazione canc = new EveCancellazione();
			fase = 30;
			String evt_prog 	= ""+dbrEvento.get("evt_prog"); 
			fase = 40;
			String id_evento 	= ""+dbrEvento.get("id_evento"); 
			fase = 50;
			String id_percorso  = ""+dbrEvento.get("id_percorso"); 
			fase = 60;
			String anno 		= ""+dbrEvento.get("anno"); 
			fase = 70;
			canc.insertEvento(dbc,n_cartella,n_contatto,evt_prog,id_evento,id_percorso, anno, mc.getCodReg(dbc), mc.getCodAsl4Invio(dbc, codOper));

			LOG.info(nomeMetodo+" - fine anno="+n_cartella+" ,contatore="+n_contatto);
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
					+" - Dettagli: n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			throw e;
		}finally{

		}
	}
	
	private void mergeDati(ISASConnection dbc,ISASRecord dbrEvento, ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "mergeDati";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			String id_percorso = ""+dbrSkMedPal.get("skm_id_percorso"); 
			
			
			fase = 11; dbrEvento.put("id_percorso", 			mc.aggZeri(id_percorso,"P",6));
			fase = 12; dbrEvento.put("anno",					mc.getAnnoFromStrSqlDate(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString()));
			fase = 13; dbrEvento.put("ssp1",					(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_ssp1",'S'));
			fase = 14; dbrEvento.put("ssp2",					(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_ssp2",'S'));
			fase = 15; dbrEvento.put("sss1",					(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_sss1",'S'));
			fase = 16; dbrEvento.put("sss2",					(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_sss2",'S'));
			fase = 17; dbrEvento.put("cod_macro_prest01",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_01",'S'));
			fase = 18; dbrEvento.put("cod_macro_prest02",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_02",'S'));
			fase = 19; dbrEvento.put("cod_macro_prest03",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_03",'S'));
			fase = 20; dbrEvento.put("cod_macro_prest04",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_04",'S'));
			fase = 21; dbrEvento.put("cod_macro_prest05",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_05",'S'));
			fase = 22; dbrEvento.put("cod_macro_prest06",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_06",'S'));
			fase = 23; dbrEvento.put("cod_macro_prest07",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_07",'S'));
			fase = 24; dbrEvento.put("cod_macro_prest08",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_08",'S'));
			fase = 25; dbrEvento.put("cod_macro_prest09",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_09",'S'));
			fase = 26; dbrEvento.put("cod_macro_prest10",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_99",'S'));
			fase = 27; dbrEvento.put("data_dimissione",			((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_data_chiusura",'O')).toString());
			fase = 28; dbrEvento.put("cod_mod_dimissione",		getMotivoChiusura(dbrSkMedPal));
			
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw e;
		}finally{
			
		}
	}

	private boolean isDatiModificati(ISASConnection dbc, ISASRecord dbrEvento,ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "isDatiModificati";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			
			Vector vEvento = new Vector();
			Vector vSkMedPal = new Vector();
			
			fase = 50;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"id_percorso",'O')).toString());
			fase = 60;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"anno",'O')).toString());
			fase = 70;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"ssp1",'O')).toString());
			fase = 80;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"ssp2",'O')).toString());
			fase = 90;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"sss1",'O')).toString());
			fase = 100; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"sss2",'O')).toString());
			fase = 110; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest01",'O')).toString());
			fase = 120; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest02",'O')).toString());
			fase = 130; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest03",'O')).toString());
			fase = 140; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest04",'O')).toString());
			fase = 150; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest05",'O')).toString());
			fase = 160; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest06",'O')).toString());
			fase = 170; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest07",'O')).toString());
			fase = 180; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest08",'O')).toString());
			fase = 190; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest09",'O')).toString());
			fase = 200; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_macro_prest10",'O')).toString());
			fase = 210; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"data_dimissione",'O')).toString());    
			fase = 220; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_mod_dimissione",'O')).toString());    			
			
			
			fase = 230; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_id_percorso",'O')).toString());
			fase = 240; vSkMedPal.add(mc.getAnnoFromStrSqlDate(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString()));
			fase = 250; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_ssp1",'O')).toString());                        
			fase = 260; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_ssp2",'O')).toString());                        
			fase = 270; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_sss1",'O')).toString());                        
			fase = 280; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_sss2",'O')).toString());                        
			fase = 290; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_01",'O')).toString());              
			fase = 300; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_02",'O')).toString());              
			fase = 310; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_03",'O')).toString());              
			fase = 320; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_04",'O')).toString());              
			fase = 330; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_05",'O')).toString());              
			fase = 340; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_06",'O')).toString());              
			fase = 350; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_07",'O')).toString());              
			fase = 360; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_08",'O')).toString());              
			fase = 370; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_09",'O')).toString());              
			fase = 360; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_macro_prest_99",'O')).toString());              
			fase = 370; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_data_chiusura",'O')).toString());  
			fase = 380; vSkMedPal.add(getMotivoChiusura(dbrSkMedPal));                
			
			
			fase = 390;
			boolean trovataDiversita = false;
			for(int i=0;i<vEvento.size();i++){
				fase = 400;
				if(!((Object)vEvento.get(i)).equals(((Object)vSkMedPal.get(i)))){
					fase = 410;
					trovataDiversita = true;
					LOG.info(nomeMetodo+" - trovata diversita alla colonna num "+(i+1));
					break;
				}
			}
			
			LOG.info(nomeMetodo+" - fine ");
			return trovataDiversita;
		
		}catch(Exception e){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ e
			+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw e;
		}finally{
			
		}
	}
	
	private String getMotivoChiusura(ISASRecord dbrSkMedPal) throws Exception{
		String skm_motivo_chius = (String)ISASUtil.getObjectField(dbrSkMedPal,"skm_motivo_chius",'S');
		if(skm_motivo_chius!=null && !skm_motivo_chius.equals("")){
			if( !skm_motivo_chius.equals("1") &&
				!skm_motivo_chius.equals("2") &&
				!skm_motivo_chius.equals("3") &&
				!skm_motivo_chius.equals("4") &&
				!skm_motivo_chius.equals("5") &&
				!skm_motivo_chius.equals("6") &&
				!skm_motivo_chius.equals("7") &&
				!skm_motivo_chius.equals("8")	
			)
				skm_motivo_chius="9"; //Riconduco tutti gli altri motivi non previsti dalla RFC191 alla caso Altro
		}else{
			skm_motivo_chius="9"; //Altro
		}
		return skm_motivo_chius;
	}
	
}

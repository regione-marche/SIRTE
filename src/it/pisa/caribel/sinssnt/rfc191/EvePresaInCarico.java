package it.pisa.caribel.sinssnt.rfc191;

import java.util.Vector;

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

public class EvePresaInCarico extends SINSSNTConnectionEJB  {
	
	public static final String tipo_evento = "PI";
	public static final String tipo_evento_descr = "Presa in carico";
	
	private ManagerCodifiche mc = new ManagerCodifiche(); 
	private ManagerEvento me = new ManagerEvento();    
	
	public EvePresaInCarico(){}
	
	public void gestisciEvento(ISASConnection dbc,ISASRecord dbrSkMedPal) throws Exception{
		String nomeMetodo = "gestisciEvento";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");

			fase = 10;
			String  n_cartella = ""+dbrSkMedPal.get("n_cartella");
			String  n_contatto = ""+dbrSkMedPal.get("n_contatto");
			// 08/01/15 mv: aggiunto passaggio codOper
			String codOper = dbrSkMedPal.get("cod_operatore").toString();
			
			LOG.info(nomeMetodo+" - inizio2 - n_cartella="+n_cartella+" ,n_contatto="+n_contatto);
			
			String skm_presacarico_data = ((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString();
			if(skm_presacarico_data!=null && !skm_presacarico_data.equals("")){
				fase = 20;
				//Se esiste l'evento di conclusione va cancellato
				//Sono nel caso in cui il contatto chiuso � stato riaperto
				if(me.esisteEvento(dbc,n_cartella,n_contatto,null,null,null,EveConclusione.tipo_evento)){
					fase = 30;
					EveConclusione conc = new EveConclusione();
					conc.cancellaEvento(dbc, n_cartella, n_contatto, codOper);
				}
				
				fase = 60;
				if(me.esisteEvento(dbc,n_cartella,n_contatto,null,null,null,this.tipo_evento)){
					fase = 70;
					updateEvento(dbc,dbrSkMedPal);
				}else{
					fase = 80;
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
			fase = 80;  //dbrPI.put("data_registrazione", sysdate); si usa il trigger perch� non gestiamo anche l'ora
			fase = 90;  dbrEvento.put("id_evento",this.tipo_evento + str_id_evento); 
			fase = 100; dbrEvento.put("cod_reg_inviante",mc.getCodReg(dbc));                                
			fase = 110; dbrEvento.put("cod_asl_inviante",mc.getCodAsl4Invio(dbc, codOper));                                
			fase = 120; dbrEvento.put("cod_reg_hospice",mc.getCodReg(dbc));                                 
			fase = 130; dbrEvento.put("cod_asl_hospice",mc.getCodAsl4Invio(dbc, codOper));
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
	
	public void cancellaEvento(ISASConnection dbc, String n_cartella,String n_contatto) throws Exception{
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
			// 08/01/15 mv: aggiunto passaggio codOper
			ISASRecord dbrHspScheda = mc.getSkMedPal(dbc, n_cartella, n_contatto);
			String codOper = dbrHspScheda.get("cod_operatore").toString();
			
			fase = 80;
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
			
			fase = 1;
			String skm_id_percorso = ""+dbrSkMedPal.get("skm_id_percorso");
			fase = 2;
			String skm_strut_erog = (String)ISASUtil.getObjectField(dbrSkMedPal,"skm_strut_erog",'S');
			fase = 3;
			String tariffa_gg = getTariffa(dbrSkMedPal);
				
			
			String patient_id = (String)ISASUtil.getObjectField(dbrEvento,"id_assistito",'S');
			fase = 5;
			if(patient_id==null || patient_id.equals(""))//Per evitare accessi inutili e lenti su FASSI 
					   dbrEvento.put("id_assistito",				mc.getIdPatientFromCartella(dbc, ""+dbrSkMedPal.get("n_cartella")));
						
			fase = 6;
			String sts11 = "";
			String hsp11 = "";
			String hsp11bis = "";
			
			// 08/01/15 mv: aggiunto passaggio codOper
			String codOper = dbrSkMedPal.get("cod_operatore").toString();
			
			fase = 7;
			ISASRecord presidio = mc.getPresidio(dbc, mc.getCodReg(dbc), mc.getCodAsl4Presidio(dbc, codOper), skm_strut_erog);
			if(presidio!=null){
				sts11 	 = (String)ISASUtil.getObjectField(presidio,"hsp_sts11",'S');
				hsp11 	 = (String)ISASUtil.getObjectField(presidio,"hsp_hsp11",'S');
				hsp11bis = (String)ISASUtil.getObjectField(presidio,"hsp_hsp11bis",'S');
				if(sts11!=null && !sts11.equals(""))
					sts11 = mc.aggZeri(sts11,"P",6);
				if(hsp11!=null && !hsp11.equals(""))
					hsp11 = mc.aggZeri(hsp11,"P",6);
				if(hsp11bis!=null && !hsp11bis.equals(""))
					hsp11bis = mc.aggZeri(hsp11bis,"P",6);
			}
			fase = 8;  dbrEvento.put("sts11",sts11);
			fase = 9;  dbrEvento.put("hsp11",hsp11);
			fase = 10; dbrEvento.put("hsp11bis",hsp11bis);
			
			fase = 11; dbrEvento.put("cod_titolo_studio",			(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_titolo_stud",'S'));
			fase = 12; dbrEvento.put("id_percorso", 				mc.aggZeri(skm_id_percorso,"P",6));
			fase = 13; dbrEvento.put("anno",						mc.getAnnoFromStrSqlDate(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString()));
			fase = 14; dbrEvento.put("data_ricovero",				((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString());
			fase = 15; dbrEvento.put("tariffa_gg",					tariffa_gg);			
			fase = 16; dbrEvento.put("data_ric_ric",				((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_data_rich_ricov",'O')).toString());
			fase = 17; dbrEvento.put("data_ric_ric_ric",			((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_data_ric_rich_ricov",'O')).toString());
			fase = 18; dbrEvento.put("cod_patologia_resp",			(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_patologia_resp",'S'));
			fase = 19; dbrEvento.put("cod_motivo_prevalente_pic",	(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_motivo_carico",'S'));
			fase = 20; dbrEvento.put("ss1",							(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_ss1",'S'));
			fase = 21; dbrEvento.put("ss2",							(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_ss2",'S'));
			fase = 22; dbrEvento.put("mnc1",						(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_mnc1",'S'));
			fase = 23; dbrEvento.put("mnc2",						(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_mnc2",'S'));
			fase = 24; dbrEvento.put("cod_tipo_caregiver",			(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_caregiver",'S'));
			fase = 25; dbrEvento.put("indice_karnofsky",			(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_karnofsky",'S'));
			fase = 26; dbrEvento.put("cons_paz_diag",				(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_consap_diagnosi",'S'));
			fase = 27; dbrEvento.put("cons_paz_prog",				(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_consap_prognosi",'S'));
			fase = 28; dbrEvento.put("cod_stru_provenienza",		(String)ISASUtil.getObjectField(dbrSkMedPal,"skm_strut_prov",'S'));
			
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
			
			fase = 10;
			String patient_id_Evento = (String)ISASUtil.getObjectField(dbrEvento,"id_assistito",'S');
			fase = 20;
			//Se il PATIENT_ID l'ho recuperato gi� alla prima volta evito di andarlo e rileggerlo da FASSI perch� lento
			if(patient_id_Evento==null || patient_id_Evento.equals("")){//Verifico se nel frattempo � stato aggiornato su fassi
				fase = 30;
				String patient_id_SkMedPal = mc.getIdPatientFromCartella(dbc, ""+dbrSkMedPal.get("n_cartella"));
				if(patient_id_SkMedPal!=null && !patient_id_SkMedPal.equals(""))
					return true;
			}
			fase = 31;
			String tariffa_gg = getTariffa(dbrSkMedPal);
			String sts11 = "";
			String hsp11 = "";
			String hsp11bis = "";
			
			// 08/01/15 mv: aggiunto passaggio codOper
			String codOper = dbrSkMedPal.get("cod_operatore").toString();
			
			ISASRecord presidio = mc.getPresidio(dbc, mc.getCodReg(dbc), mc.getCodAsl4Presidio(dbc, codOper), ((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_strut_erog",'O')).toString());
			fase = 32;
			if(presidio!=null){
				fase = 33;
				sts11 	 = (String)ISASUtil.getObjectField(presidio,"hsp_sts11",'S');
				hsp11 	 = (String)ISASUtil.getObjectField(presidio,"hsp_hsp11",'S');
				hsp11bis = (String)ISASUtil.getObjectField(presidio,"hsp_hsp11bis",'S');
				fase = 34;
				if(sts11!=null && !sts11.equals(""))
					sts11 = mc.aggZeri(sts11,"P",6);
				if(hsp11!=null && !hsp11.equals(""))
					hsp11 = mc.aggZeri(hsp11,"P",6);
				if(hsp11bis!=null && !hsp11bis.equals(""))
					hsp11bis = mc.aggZeri(hsp11bis,"P",6);
			}
			
			Vector vEvento = new Vector();
			Vector vSkMedPal = new Vector();
						
			fase = 40;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_titolo_studio",'O')).toString());
			fase = 50;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"id_percorso",'O')).toString());
			fase = 60;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"anno",'O')).toString());
			fase = 70;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"data_ricovero",'O')).toString());
			fase = 80;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"tariffa_gg",'O')).toString());
			fase = 90;  vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"data_ric_ric",'O')).toString());
			fase = 100; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"data_ric_ric_ric",'O')).toString());
			fase = 110; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_patologia_resp",'O')).toString());
			fase = 120; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_motivo_prevalente_pic",'O')).toString());
			fase = 130; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"ss1",'O')).toString());
			fase = 140; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"ss2",'O')).toString());
			fase = 150; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"mnc1",'O')).toString());
			fase = 160; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"mnc2",'O')).toString());
			fase = 170; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_tipo_caregiver",'O')).toString());
			fase = 180; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"indice_karnofsky",'O')).toString());
			fase = 190; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cons_paz_diag",'O')).toString());
			fase = 200; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cons_paz_prog",'O')).toString());
			fase = 201; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"cod_stru_provenienza",'O')).toString());
			fase = 202; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"STS11",'O')).toString());
			fase = 203; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"HSP11",'O')).toString());
			fase = 204; vEvento.add(((Object)ISASUtil.getObjectField(dbrEvento,"HSP11BIS",'O')).toString());
			
			fase = 210; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_titolo_stud",'O')).toString());
			fase = 220; vSkMedPal.add(mc.aggZeri(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_id_percorso",'O')).toString(),"P",6));
			fase = 230; vSkMedPal.add(mc.getAnnoFromStrSqlDate(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString()));
			fase = 240; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString());
			fase = 250; vSkMedPal.add(tariffa_gg);
			fase = 260; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_data_rich_ricov",'O')).toString());
			fase = 270; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_data_ric_rich_ricov",'O')).toString());
			fase = 280; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_patologia_resp",'O')).toString());
			fase = 290; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_motivo_carico",'O')).toString());
			fase = 300; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_ss1",'O')).toString());
			fase = 310; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_ss2",'O')).toString());
			fase = 320; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_mnc1",'O')).toString());
			fase = 330; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_mnc2",'O')).toString());
			fase = 340; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_caregiver",'O')).toString());
			fase = 350; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_karnofsky",'O')).toString());
			fase = 360; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_consap_diagnosi",'O')).toString());
			fase = 370; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_consap_prognosi",'O')).toString());
			fase = 371; vSkMedPal.add(((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_strut_prov",'O')).toString());
			fase = 372; vSkMedPal.add(sts11);
			fase = 373; vSkMedPal.add(hsp11);
			fase = 374; vSkMedPal.add(hsp11bis);
			
			fase = 380;
			boolean trovataDiversita = false;
			for(int i=0;i<vEvento.size();i++){
				fase = 390;
				if(!((Object)vEvento.get(i)).equals(((Object)vSkMedPal.get(i)))){
					fase = 400;
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
	
	private String getTariffa(ISASRecord dbrSkMedPal) throws Exception{
		String tariffa_gg = ((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_strut_tariffa",'O')).toString();
		if(tariffa_gg!=null && tariffa_gg.substring(tariffa_gg.indexOf(".")).length()<3)
			tariffa_gg+="0"; //Aggiungo uno zero dopo la virgola
		return tariffa_gg;
	}

}

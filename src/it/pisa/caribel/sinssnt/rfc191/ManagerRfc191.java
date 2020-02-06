package it.pisa.caribel.sinssnt.rfc191;

import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

public class ManagerRfc191 extends SINSSNTConnectionEJB  {

	private String msgError = "Si e' verificato un errore durante la generazione dell'evento RFC191 per la regione.\n"+
			"I dati non sono stati salvati. Contattare l'assistenza!";

	ManagerCodifiche mc = new ManagerCodifiche();

	public ManagerRfc191(){}

	public void gestisciPresaCarico(ISASConnection dbc, ISASRecord dbrSkMedPal)throws CariException {
		String nomeMetodo = "gestisciPresaCarico";
		try{
			LOG.info(nomeMetodo+" - inizio ");
			if(isAbilWriteInFrontiera(dbc)){
				EvePresaInCarico pic = new EvePresaInCarico();
				pic.gestisciEvento(dbc, dbrSkMedPal);
				LOG.info(nomeMetodo+" - INSERITO EVENTO DI PRESA_IN_CARICO");
			}
			LOG.info(nomeMetodo+" - fine ");
		}catch(CariException ce){
			throw ce;
		}catch(Exception ex){
			throw new CariException(msgError);
		}
	}

	public void gestisciConclusione(ISASConnection dbc, ISASRecord dbrSkMedPal)throws CariException {
		String nomeMetodo = "gestisciConclusione";
		try{
			LOG.info(nomeMetodo+" - inizio ");
			if(isAbilWriteInFrontiera(dbc)){
				EveConclusione con = new EveConclusione();
				con.gestisciEvento(dbc, dbrSkMedPal);
				LOG.info(nomeMetodo+" - INSERITO EVENTO DI CONCLUSIONE");
			}
			LOG.info(nomeMetodo+" - fine ");
		}catch(CariException ce){
			throw ce;
		}catch(Exception ex){
			throw new CariException(msgError);
		}
	}

	public void gestisciErogazione(ISASConnection dbc,String anno, String contatore)throws CariException {
		String nomeMetodo = "gestisciErogazione";
		try{
			LOG.info(nomeMetodo+" - inizio ");
			if(isAbilWriteInFrontiera(dbc)){
				EveErogazione ero = new EveErogazione();
				ero.gestisciEvento(dbc, anno, contatore);
				LOG.info(nomeMetodo+" - GESTITO EVENTO DI EROGAZIONE");
			}
			LOG.info(nomeMetodo+" - fine ");
		}catch(CariException ce){
			throw ce;
		}catch(Exception ex){
			throw new CariException(msgError);
		}
	}

	public void gestisciCancellazione(ISASConnection dbc, ISASRecord dbrSkMedPal)throws CariException {
		String nomeMetodo = "gestisciCancellazione";
		try{
			LOG.info(nomeMetodo+" - inizio ");
			if(isAbilWriteInFrontiera(dbc)){
				EveCancellazione can = new EveCancellazione();
				can.gestisciEvento(dbc, dbrSkMedPal);
				LOG.info(nomeMetodo+" - GESTITO EVENTO DI CANCELLAZIONE");
			}
			LOG.info(nomeMetodo+" - fine ");
		}catch(CariException ce){
			throw ce;
		}catch(Exception ex){
			throw new CariException(msgError);
		}
	}


	public Integer newIdPercorsoPerRfc191(ISASConnection dbc, ISASRecord dbrSkMedPal)throws Exception{
		String nomeMetodo = "newIdPercorsoPerRfc191";
		int fase = 0;
		try{
			LOG.info(nomeMetodo+" - inizio ");
			Integer ret = null;
			String skm_id_percorso = ((String)ISASUtil.getObjectField(dbrSkMedPal,"skm_id_percorso",'S'));
			if(skm_id_percorso!=null && !skm_id_percorso.trim().equals("")){
				ret = Integer.valueOf(skm_id_percorso);
			}else{
				fase = 10;
				String skm_presacarico_data = ((Object)ISASUtil.getObjectField(dbrSkMedPal,"skm_presacarico_data",'O')).toString();
				fase = 20;
				if(skm_presacarico_data!=null && !skm_presacarico_data.equals("")){
					fase = 30;
					String anno = mc.getAnnoFromStrSqlDate(skm_presacarico_data);
					fase = 40;
					int int_skm_id_percorso = selectProgressivo(dbc, "HSP_SCHEDA"+anno);
					fase = 50;
					ret = new Integer(int_skm_id_percorso);
				}
			}
			LOG.info(nomeMetodo+" - fine ");
			return ret;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ ex
					+" - Dettagli dbrSkMedPal: "+dbrSkMedPal.getHashtable().toString());
			throw ex;
		}

	}

	private boolean isAbilWriteInFrontiera(ISASConnection dbc)throws Exception{
		String nomeMetodo = "isAbilWriteInFrontiera";
		int fase = 0;
		try{
			boolean abilitata = false;
			String abil_rfc191front = getConfStringField(dbc,"SINS","abil_rfc191front","conf_txt");
			fase = 10;
			if(abil_rfc191front!=null && abil_rfc191front.equalsIgnoreCase("SI")){
				abilitata = true;
			}else{
				LOG.warn(nomeMetodo+" - SCRITTURA IN TABELLE DI FRONTIERA DISABILITATA - VEDI CHIAVE abil_rfc191front IN TABELLA CONF");
			}
			return abilitata;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Si e' verificato un errore alla fase: "+fase+" Exception:"+ ex);
			throw ex;
		}
	}



}



package it.pisa.caribel.sinssnt.controlli;

import java.util.Hashtable;

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.utilProcedure.ManagerAnagrafica;

public class ChiusuraAnagraficaEJB extends SINSSNTConnectionEJB {

	ManagerAnagrafica ma = new ManagerAnagrafica();

	public String eseguiCheckUp(myLogin mylogin,Hashtable h) throws Exception {
		String nomeMetodo = "eseguiCheckUp";
		ISASConnection dbc=null;
		try{
			LOG.debug(nomeMetodo+" inizio metodo");
			dbc=super.logIn(mylogin);	
			String cod_usl			=	(String)h.get(ManagerAnagrafica.COD_USL);
			String motivo_chiusura	=	(String)h.get(ManagerAnagrafica.MOTIVO_CHIUSURA);
			String data_chiusura	=	(String)h.get(ManagerAnagrafica.DATA_CHIUSURA);
			String cod_operatore	=	(String)h.get(ManagerAnagrafica.COD_OPERATORE);
			String chiusura_forzata	=	(String)h.get(ManagerAnagrafica.CHIUSURA_FORZATA);

			String n_cartella = ma.getNCartellaFromCodUsl(dbc, cod_usl);
			
			String esitoCheckUp="";
			boolean isChiusuraForzata = (chiusura_forzata!=null && chiusura_forzata.equals("SI"));
			CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure(isChiusuraForzata,	cod_usl, data_chiusura, motivo_chiusura); // gb 25/09/07

			// gb 28/09/07: Controlli data chiusura cartella con:
			// date prestazioni erogate della tabella interv.
			// date aper. e date chius. della cheda valutazione.
			// date aper. e date chius. dei progetti e contatti.
			// date aper. e date chius. dei piani assitenziali.
			// date aper. dei piani accessi.
			// date aper. e date chius. degli obiettivi, interventi e verifiche.
			String strMsgCheckDtCh = clCcec.checkDtChDaCartGTDtApeDtCh(dbc, n_cartella, data_chiusura, isChiusuraForzata);
			if (!strMsgCheckDtCh.equals("")){
				if(!isChiusuraForzata){
					esitoCheckUp= strMsgCheckDtCh;
				}else{
					loggaErroreInChiusuraForzata(new Exception(strMsgCheckDtCh), dbc, cod_usl, data_chiusura, motivo_chiusura);
				}
			}
			LOG.debug(nomeMetodo+" fine metodo");
			return esitoCheckUp;
		}catch(Exception e){
			e.printStackTrace();
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			logout_nothrow(nomeMetodo, dbc);
		}
	}


	public void eseguiChiusura(myLogin mylogin,Hashtable h) throws Exception {
		String nomeMetodo = "eseguiChiusura";
		ISASConnection dbc=null;
		try{
			LOG.debug(nomeMetodo+" inizio metodo");
			dbc=super.logIn(mylogin);
			String cod_usl			=	(String)h.get(ManagerAnagrafica.COD_USL);
			String motivo_chiusura	=	(String)h.get(ManagerAnagrafica.MOTIVO_CHIUSURA);
			String data_chiusura	=	(String)h.get(ManagerAnagrafica.DATA_CHIUSURA);
			String cod_operatore	=	(String)h.get(ManagerAnagrafica.COD_OPERATORE);
			String chiusura_forzata	=	(String)h.get(ManagerAnagrafica.CHIUSURA_FORZATA);
			
			String n_cartella = ma.getNCartellaFromCodUsl(dbc, cod_usl);

			boolean isChiusuraForzata = (chiusura_forzata!=null && chiusura_forzata.equals("SI"));
			CartCntrlEtChiusure clCcec = new CartCntrlEtChiusure(isChiusuraForzata,	cod_usl, data_chiusura, motivo_chiusura); // gb 25/09/07

			clCcec.chiudoSkSO(mylogin, dbc, n_cartella, data_chiusura, motivo_chiusura, isChiusuraForzata);
			clCcec.chiudoDaCartellaInGiu(dbc, n_cartella, data_chiusura, motivo_chiusura, cod_operatore);
			LOG.debug(nomeMetodo+" fine metodo");

		}catch(Exception e){
			e.printStackTrace();
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			logout_nothrow(nomeMetodo, dbc);
		}
	}

}
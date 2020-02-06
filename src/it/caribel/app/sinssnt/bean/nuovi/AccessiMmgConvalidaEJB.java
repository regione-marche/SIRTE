package it.caribel.app.sinssnt.bean.nuovi;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;

import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.millewin.ejb.MillenniumEJB;
import it.pisa.caribel.util.ISASUtil;

public class AccessiMmgConvalidaEJB extends MillenniumEJB {

	public AccessiMmgConvalidaEJB() {}

	public Vector<?> queryPaginate(myLogin mylogin, Hashtable<?, ?> h) throws SQLException {
		return query(mylogin, h,  true);
	}

	public Vector<?> query(myLogin mylogin, Hashtable<?, ?> h) throws SQLException {
		return query(mylogin, h,  false);
	}

	private Vector<?> query(myLogin mylogin,Hashtable<?, ?> h,boolean paginated) throws  SQLException 
	{
		String nomeMetodo = "query";
		ISASConnection dbc=null;
		ISASCursor dbcur = null;
		try{
			dbc = super.logIn(mylogin);

			String myselect = getSqlQryXConsulta(dbc, h);
			dbcur = dbc.startCursor(myselect);
			Vector<Object> vdb=new Vector<Object>();
			if(paginated){
				int start = Integer.parseInt((String) h.get("start"));
				int stop = Integer.parseInt((String) h.get("stop"));
				vdb = dbcur.paginate(start, stop);
			}else{
				vdb = dbcur.getAllRecord();
			}
			vdb = decodificaVectorISASRecord(dbc, vdb);
			return vdb;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
			logout_nothrow(nomeMetodo, dbc);
		}
	}
	
	
	private Vector<Object> decodificaVectorISASRecord(ISASConnection dbc,Vector<Object> vdbr)throws Exception{
		String nomeMetodo = "decodificaVectorISASRecord";
		try{
			for (int i =0;i<vdbr.size();i++ ) {
				Object obj = vdbr.get(i);
				if(obj instanceof ISASRecord){
					ISASRecord dbr = (ISASRecord)vdbr.get(i);	
					dbr = (ISASRecord)vdbr.elementAt(i);
					dbr = decodificaISASRecord(dbc, dbr,false);
				}
			}
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT["+vdbr.size()+"] OUTPUT["+vdbr.size()+"]");
			return vdbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	private ISASRecord decodificaISASRecord(ISASConnection dbc,ISASRecord dbr,boolean aggiungiDettagliScheda)throws Exception{
		String nomeMetodo = "decodificaISASRecord";
		try{
			if(dbr!=null){			
				//decodifico il medico
				dbr.put("medico",ISASUtil.getDecode(dbc,"medici","mecodi",
						(String)ISASUtil.getObjectField(dbr,"cod_medico",'S'),
						"(nvl(trim(mecogn),'') || ' ' || nvl(trim(menome),''))","medico"));
				
				//decodifico il tipo_prestazione
				String tipo_prestazione= (String)ISASUtil.getObjectField(dbr,"tipo_prestazione",'S');
				String cod_prestazione= (String)ISASUtil.getObjectField(dbr,"cod_prestazione",'S');
				dbr.put("desc_tipo_prestazione", decodTipoPrestazione(dbc, cod_prestazione, tipo_prestazione));
				
				//decodifico la forzatura
				String forzatura= (String)ISASUtil.getObjectField(dbr,"forzatura",'S');
				if(forzatura!=null && forzatura.equals("F"))
					dbr.put("descr_forzatura", Labels.getLabel("common.yes"));
				else
					dbr.put("descr_forzatura", "");

				//bargi 28/08/2013 nel caso come nel veneto in cui non è presente la descrizione delle prestazioni passate
				//si decodifica quelle presenti in tabella
				if(dbr.get("des_prestazione")==null||dbr.get("des_prestazione").toString().equals("")) {
					Hashtable htCodsPrest = getCodsPrestaz(dbc, dbr);
					if (htCodsPrest != null) {
						Hashtable<String, Object> htDatiDaTabpipp = getDatiDaTabpipp(dbc, dbr, htCodsPrest);			
						if (htDatiDaTabpipp != null) 
							dbr.put("des_prestazione",htDatiDaTabpipp.get("pipp_des"));
					}
				}
				int err_cart=0;
				int err_pres=0;
				int err_auto=0;

				if(dbr.get("flag_err_assistito")!=null)
					err_cart=Integer.parseInt((String)ISASUtil.getObjectField(dbr,"flag_err_assistito",'I'));
				if(dbr.get("flag_err_prestazione")!=null)
					err_pres=Integer.parseInt((String)ISASUtil.getObjectField(dbr,"flag_err_prestazione",'I'));
				if(dbr.get("flag_err_autorizzazioni")!=null)
					err_auto=Integer.parseInt((String)ISASUtil.getObjectField(dbr,"flag_err_autorizzazioni",'I'));
				if(err_cart==0 && err_pres==0 && err_auto==0){
					dbr.put("des_errore", "Record corretto");
					ctrPrestDupl(dbc,dbr);
				}else{
					if(err_cart==1)
						dbr.put("des_errore", "Assistito non presente in anagrafe assistiti");
					else if (err_pres==1)
						dbr.put("des_errore", "Prestazione non presente in tabella di decodifica");
					else if (err_auto!=0){
						if(err_auto==1)         dbr.put("des_errore", "Manca l'autorizzazione");
						else if(err_auto==2)    dbr.put("des_errore", "Numero di accessi nel mese maggiore del preventivato");
						else if(err_auto==3)    dbr.put("des_errore", "Data accesso maggiore di data fine autorizzazione");
						else if(err_auto==4)    dbr.put("des_errore", "Data accesso successiva a data ricovero");
						else if(err_auto==5)    dbr.put("des_errore", "Data accesso successiva ad ingresso RSA");
						ctrPrestDupl(dbc,dbr);
					}
				}
				
			}
			LOG.info(nomeMetodo+" -  Metodo eseguito INPUT[dbr,"+aggiungiDettagliScheda+"]");
			return dbr;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
	
	private String decodTipoPrestazione(ISASConnection dbc, String cod_prestazione, String tipo_prestazione)throws Exception {
		String nomeMetodo = "decodTipoPrestazione";
		try{
			String desc_tipo_prestazione="ERRATO";
			String sel = 
					" SELECT t.*" +
					" FROM tippipp t, trcod_pipp p"+
					" WHERE t.tip_cod = p.pipp_tipo"+
					" AND p.est_codi = '"+cod_prestazione+"'"+
					" AND p.est_tipo = '"+tipo_prestazione+"'";
			ISASRecord dbr = dbc.readRecord(sel);
			if (dbr != null){
				desc_tipo_prestazione = (String)dbr.get("tip_des");
			}
			return desc_tipo_prestazione;
		}catch(Exception e){
			throw newEjbException("Errore in "+nomeMetodo+": "+ e.getMessage(),e);
		}
	}
}
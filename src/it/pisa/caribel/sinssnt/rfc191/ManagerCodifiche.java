package it.pisa.caribel.sinssnt.rfc191;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Vector;

import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.isas2.ISASCursor;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;
import it.pisa.caribel.util.ISASUtil;

public class ManagerCodifiche extends SINSSNTConnectionEJB  {

	private String cod_reg = "";
	private String cod_asl = "";
	
	// 08/01/15: codifica specifica Ospedale Meyer (gli ospedali sono codificati
	// separatamente dalla ASL)
//	public static final String COD_USL_OSP_MEYER = "904";
//	public static final String COD_ZONA_MEYER = "5";
	
	private final String KEY_CONF_CODUSL_OSPMEYER =  "COD_USL_OSPMEYER";
	private final String KEY_CONF_ZONA_OSPMEYER =  "COD_ZONA_OPMEYER";
	
	// 06/06/16
	public final boolean USA_ASL_OLD = true;
	public final boolean USA_ASL_NEW = false;
	
	public ManagerCodifiche(){}
	
	public String getCodReg(ISASConnection dbc)throws Exception{
		if(this.cod_reg==null || this.cod_reg.equals(""))
			this.cod_reg = getConfStringField(dbc,"SINS","codice_regione","conf_txt");
		return this.cod_reg;
	}
	
	// 06/06/16
	public String getCodAsl4Invio(ISASConnection dbc, String codOper) throws Exception{	
		return getCodAsl(dbc, codOper, true);
	}
	
	// 06/06/16
	public String getCodAsl4Presidio(ISASConnection dbc, String codOper) throws Exception{	
		return getCodAsl(dbc, codOper, false);
	}
	
	public String getCodAsl(ISASConnection dbc, String codOper, boolean oldCodAsl)throws Exception{		 
		if (isOperMeyer(dbc, codOper)) {
			String codUsl_ospMeyer = getConfStringField(dbc, "SINS", KEY_CONF_CODUSL_OSPMEYER, "conf_txt");
			if ((codUsl_ospMeyer != null) && (!codUsl_ospMeyer.trim().equals("")))
				return codUsl_ospMeyer;
			else {
				String msg_ex = "Codifica Ospedale Meyer non presente su configuratore.";
				throw new CariException(msg_ex, -2);
			}
		}
		return this.getCodAsl(dbc, oldCodAsl);
	}
	
	public String getCodAsl(ISASConnection dbc, boolean oldCodAsl)throws Exception{
		String nmFldConf_ASL = "codice_usl";
		if (oldCodAsl)
			nmFldConf_ASL += "_old";
		return getConfStringField(dbc,"SINS", nmFldConf_ASL, "conf_txt");
	}
	
	
	
	public String getIdPatientFromCartella(ISASConnection dbc, String n_cartella)throws Exception {
		String nomeMetodo = "getIdPatientFromCartella";
		String cod_usl = null;
		String cod_fisc = null;
		String ret = null;
		try{
			String myselect="Select cod_usl, cod_fisc" +
					" from cartella " +
					" where n_cartella = "+n_cartella;
			
			ISASRecord dbr=dbc.readRecord(myselect);
			cod_usl = (String)dbr.get("cod_usl");
			cod_fisc = (String)dbr.get("cod_fisc");
			
			ret = getIdPatient(dbc,cod_usl,cod_fisc);
			
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			return null;
		}
		return ret;
	}
		
		
	private String getIdPatient(ISASConnection dbc, String cod_usl, String cod_fisc)throws Exception {
		int conta  = 0; 
		String ret = null;
		//TEST String ret = "REG999999200800000000001";
		try{
			if(cod_usl!=null && !cod_usl.trim().equals("")){
				//##########################################################################
				//#############  RECUPERO TRAMITE COD_USL           ########################
				//##########################################################################
				cod_usl = cod_usl.toUpperCase();
				cod_usl = cod_usl.trim();
				
				String myselect2="Select *" +
						" from fassi " +
						" where cod_usl = '"+cod_usl+"'";

				LOG.trace("getIdPatient per COD_USL: "+cod_usl+" - myselect2="+myselect2);

				ISASRecord dbr2=dbc.readRecord(myselect2);
				if(dbr2!=null)
					ret = (String)dbr2.get("patient_id");
				
				
			}else if (cod_fisc!=null && !cod_fisc.trim().equals("")){
				//##########################################################################
				//#############  RECUPERO TRAMITE CODICE FISCALE    ########################
				//##########################################################################
				cod_fisc = cod_fisc.toUpperCase();
				cod_fisc = cod_fisc.trim();
				
				//Controllo ambiguita su codice fiscale
				String myselect="Select count(*) as conta" +
				" from fassi " +
				" where afisc = '"+cod_fisc+"'";

				LOG.trace("getIdPatient per CF: "+cod_fisc+" - CONTA myselect="+myselect);
				ISASRecord dbr=dbc.readRecord(myselect);
				if(dbr!=null){
					String strConta = ""+dbr.get("conta");
					conta = Integer.parseInt(strConta);
				}
				if(conta>1){
					LOG.error("getIdPatient per CF: "+cod_fisc+" TROVATA AMBIGUITA");
					return null;
				}
				if(conta==0){
					LOG.error("getIdPatient per CF: "+cod_fisc+" DECEDUTO NON RECUPERATO SU FASSI ");
					return null;
				}
				//fine controllo ambiguita

				String myselect2="Select *" +
				" from fassi " +
				" where afisc = '"+cod_fisc+"'";

				LOG.trace("getIdPatient per CF: "+cod_fisc+" - myselect2="+myselect2);

				ISASRecord dbr2=dbc.readRecord(myselect2);
				if(dbr2!=null)
					ret = (String)dbr2.get("patient_id");
			}
		}catch(Exception ex){
			LOG.error("getIdPatient() - Exception: "+ex);
			return null;
		}
		return ret;
	}
	
	public java.sql.Date getSqlDateFromString(String data) throws Exception{
		String nomeMetodo = "getSqlDateFromString";
		try{
			SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date dt=format.parse(data,new ParsePosition(0));
			return new java.sql.Date(dt.getTime());
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}
	}
	
	public String getAnnoFromStrSqlDate(String sqlDate) throws Exception{
		String nomeMetodo = "getAnnoFromStrSqlDate";
		//formato sqlDate = yyyy-MM-dd
		try{
			String anno = "";
			if(sqlDate!=null && !sqlDate.equals("")&& !sqlDate.equals("null"))
				anno = sqlDate.substring(0,4);
			return anno;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}
	}
	
	public String getAnnoFromStrItaDate(String itaDate) throws Exception{
		String nomeMetodo = "getAnnoFromStrItaDate";
		//formato itaDate = dd/MM/yyyy
		try{
			String anno = "";
			if(itaDate!=null && !itaDate.equals("")&& !itaDate.equals("null"))
				anno = itaDate.substring(7,10);
			return anno;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}
	}
	
	/** aggiunge zeri alla stringa s fino a che la Stringa
	 *  non � lunga nr. Li aggiunge prima
	 *  se dove � uguale a P, dopo se dove � uguale a S.
	 *  Restituisce la stringa con gli zeri aggiunti.
	 */
	public String aggZeri(Object sO,String dove,int nr)
	{
		String s="";
		if(sO != null)
		{ if (sO instanceof Integer)
			s=((Integer)sO).toString();
		else
			s=(String)sO;
		}
		if (s.length()>nr)s=s.substring(0,nr);
		while(s.length() < nr)
		{
			if(dove.equals("P"))
				s="0"+s;
			else if(dove.equals("D"))
				s+="0";
		}//end while
		return s;
	}//end method aggZeri
	
	/** aggiunge tanti spazi alla stringa s fino a che la
	 *  sua lunghezza non e' nr. Li aggiunge prima
	 *  se dove e' uguale a P, dopo se dove e' uguale a S.
	 *  Restituisce la stringa con gli spazi aggiunti.
	 */
	public String aggSpazi(Object sO,String dove,int nr)
	{
		String s="";
		if(sO != null)
		{ if (sO instanceof Integer)
			s=((Integer)sO).toString();
		else
			s=(String)sO;
		}
		if (s.length()>nr)s=s.substring(0,nr);
		while(s.length() < nr)
		{
			if(dove.equals("P"))
				s=" "+s;
			else if(dove.equals("D"))
				s+=" ";
		}//end while
		return s;
	}//end method aggSpazi

	
	public ISASRecord getPresidio(ISASConnection dbc,String codreg, String codazsan, String codpres)throws Exception {
		String nomeMetodo = "getPresidio";
		try {
			String myselect = "SELECT p.*, " +
					" i.hsp_numpost,i.hsp_tariffa,i.hsp_sts11,i.hsp_hsp11,i.hsp_hsp11bis "+
					" FROM presidi p, hsp_presidi_info i"+
					" where p.codreg='"+codreg+"'"+
					" and p.codazsan='"+codazsan+"'"+
					" and p.codpres='"+codpres+"'"+
					" and p.codreg=i.codreg"+
					" and p.codazsan=i.codazsan"+
					" and p.codpres=i.codpres";
			
			ISASRecord dbr = dbc.readRecord(myselect);
			return dbr;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{

		}
	}
	
	
	public ISASRecord getIntervento(ISASConnection dbc,String anno, String contatore)throws Exception {
		String nomeMetodo = "getIntervento";
		try {
			String myselect = "SELECT * " +
					" FROM hsp_interv"+
					" where int_anno='"+anno+"'"+
					" and int_contatore="+contatore;
			
			ISASRecord dbr = dbc.readRecord(myselect);
			return dbr;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{

		}
	}

	public String getDecodTipoOperatore(String tipoOperatore)throws Exception {
		String nomeMetodo = "getDecodTipoOperatore";
		String ret_tipo_op = "99";//altro
		try {
			if(tipoOperatore!=null){
				if(tipoOperatore.equals("02"))
					ret_tipo_op="1";//Infermiere
				else if(tipoOperatore.equals("98"))
					ret_tipo_op="2";//Medico specialista
				else if(tipoOperatore.equals("52"))
					ret_tipo_op="3";//Medico esperto in cure palliative
				else if(tipoOperatore.equals("09"))
					ret_tipo_op="4";//Psicologo
				else if(tipoOperatore.equals("04"))
					ret_tipo_op="5";//Fisioterapista				
			}
			return ret_tipo_op;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{

		}
	}
	
	public String getDecodCodPrestErog(ISASConnection dbc, String pre_cod_prest)throws Exception {
		String nomeMetodo = "getDecodCodPrestErog";
		String ret = "";
		try {

			ret = ISASUtil.getDecode(dbc, "prestaz", "prest_cod", pre_cod_prest, "prest_codreg");
			ret.replaceAll(".", "");
			
			return ret;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{

		}
	}
	
	
	public ISASRecord getSkMedPal(ISASConnection dbc,String cartella, String contatto)throws Exception {
		String nomeMetodo = "getSkMedPal";
		try {
			String myselect = "SELECT * " +
					" FROM hsp_scheda"+
					" where n_cartella="+cartella+
					" and n_contatto="+contatto;
			
			ISASRecord dbr = dbc.readRecord(myselect);
			return dbr;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{

		}
	}
	
	public Vector getPrestazioni(ISASConnection dbc,String anno, String contatore)throws Exception {
		String nomeMetodo = "getPrestazioni";
		ISASCursor dbcur=null;
		try {
			String myselect = "SELECT * " +
					" FROM hsp_intpre"+
					" where pre_anno='"+anno+"'"+
					" and pre_contatore="+contatore;
			
			dbcur=dbc.startCursor(myselect);
			Vector vdbr=dbcur.getAllRecord();
			
			return vdbr;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{
			close_dbcur_nothrow(nomeMetodo, dbcur);
		}
	}
	
	public String decodTipoEvento(String tipoEvento)throws Exception {
		String nomeMetodo = "decodTipoEvento";
		try {
			String ret = "";
			if(tipoEvento!=null){
				if(tipoEvento.equals(EvePresaInCarico.tipo_evento))
					ret = EvePresaInCarico.tipo_evento_descr;
				if(tipoEvento.equals(EveErogazione.tipo_evento))
					ret = EveErogazione.tipo_evento_descr;
				else if(tipoEvento.equals(EveConclusione.tipo_evento))
					ret = EveConclusione.tipo_evento_descr;
				else if(tipoEvento.equals(EveCancellazione.tipo_evento))
					ret = EveCancellazione.tipo_evento_descr;
			}
			
			return ret;
		}catch(Exception ex){
			LOG.error(nomeMetodo+" - Exception: "+ex);
			throw ex;
		}finally{
			
		}
	}

	// 08/01/15
	public boolean isOperMeyer(ISASConnection dbc, String codOper)throws Exception {
		String nomeMetodo = "isOperMeyer";
		String zonaOper = "";
		
		if (codOper == null) {
			LOG.debug(nomeMetodo+" - codifica operatore NULLA!!!");
			return false;
		}
		
		String sel = "SELECT * FROM operatori WHERE codice = '" + codOper + "'";
		ISASRecord dbr = dbc.readRecord(sel);
		if ((dbr != null) && (dbr.get("cod_zona") != null))
			zonaOper = dbr.get("cod_zona").toString().trim();
		else
			LOG.info(nomeMetodo+" - operatore=["+codOper+"] NON associato ad alcuna ZONA!!!");
		
		String codZona_operMeyer = getConfStringField(dbc, "SINS", KEY_CONF_ZONA_OSPMEYER, "conf_txt");
		if ((codZona_operMeyer != null) && (!codZona_operMeyer.trim().equals("")))
			return codZona_operMeyer.equals(zonaOper);
		return false; 
	}
	

}



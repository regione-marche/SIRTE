package it.caribel.app.sinssnt.bean;

//==========================================================================
//CARIBEL S.r.l.
//--------------------------------------------------------------------------

import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.anagcom.getAnagComunale;
import it.pisa.caribel.sinssnt.connection.*;
public class AnagComunaleEJB extends SINSSNTConnectionEJB
{
	myLogin mylogin_global=null;
	it.pisa.caribel.util.ISASUtil util = new it.pisa.caribel.util.ISASUtil();
	it.pisa.caribel.util.NumberDateFormat ndf = new it.pisa.caribel.util.NumberDateFormat();
	String messaggio_carica="";
	String messaggio_scarica="";

	public AnagComunaleEJB() {}	
	/**
	 @param mylogin mylogin
	 @param h hashtable contenente: ""zona operatore e codice fiscale assistito													
	 */
	public Hashtable  nuovoAssistito(myLogin mylogin,Hashtable h) throws  Exception {		
       ISASConnection dbc=null;
       boolean done=false;
       try {
       dbc=super.logIn(mylogin);
       getAnagComunale anagC=new getAnagComunale();
       Hashtable hR=anagC.nuovoAssistito(dbc,h);       
      dbc.close();
	  super.close(dbc);
      return hR;
       }catch(Exception e){
			e.printStackTrace();
			throw new Exception("Errore eseguendo reperimento dati comune()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){System.out.println(e1);}
			}
		}
	}
	
	/**
	 @param mylogin mylogin
	 @param h hashtable contenente: ""zona operatore e codice fiscale assistito													
	 */
	public Vector  nucleoAssistito(myLogin mylogin,Hashtable ht) throws  Exception {
		boolean done=false;
		String scr=" ";
       ISASConnection dbc=null;
		System.out.println("nucleoAssistito  h passato"+ht.toString());
		Vector vRet=new Vector();
		try{	
			dbc=super.logIn(mylogin);
			getAnagComunale anagC=new getAnagComunale();
		       vRet=anagC.nucleoAssistito(dbc,ht);       
				dbc.close();
				super.close(dbc);
				done=true;
				return vRet;
				}catch(Exception e){
					e.printStackTrace();
					throw new Exception("Errore eseguendo reperimento dati comune()  ");
				}finally{
					if(!done){
						try{
							dbc.close();
							super.close(dbc);
						}catch(Exception e1){System.out.println(e1);}
					}
				}
			}
}


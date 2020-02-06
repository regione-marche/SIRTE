package it.caribel.app.sinssnt.bean;
// ============================================================================
// CARIBEL S.r.l.
// ----------------------------------------------------------------------------
//
// 14/02/2005 - EJB di connessione alla procedura SINS Tabella FoTipoUteDimisEJB
//
// Ilaria Mancini
//
// ============================================================================

import java.io.*;
import java.sql.*;
import java.util.*;


import it.pisa.caribel.isas2.*;
import it.pisa.caribel.merge.*;
import it.pisa.caribel.ndo.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.util.*;


public class FoTipoUteDimisEJB extends SINSSNTConnectionEJB {
	String dom_res;
	String dr;
public FoTipoUteDimisEJB() {}

/*hashtable dove carico le descrizioni delle righe
e delle colonne,in modo da non passarle sempre all'"albero" dell NDO
perch� questo rallenterebbe molto il programma
*/
Hashtable h_1= new Hashtable();
Hashtable h_2= new Hashtable();
Hashtable h_3= new Hashtable();
Hashtable h_4= new Hashtable();
Hashtable h_5= new Hashtable();    
Hashtable h_6= new Hashtable();
Hashtable h_7= new Hashtable();
boolean inPiemonte = false;
//colonne
Hashtable HDescCol= new Hashtable();
//tipo Utente
Hashtable hDescTipoUte=new Hashtable();
Hashtable hDescMotivo=new Hashtable();    
//Assistito
Hashtable hAssistito=new Hashtable();


private String codice_usl="";
private String codice_regione="";
private String MIONOME ="36-FoTipoUteDimisEJB.";
private String CONSTANTS_NESSUNA_DIVISIONE ="NESSUNA DIVISIONE";
private void preparaLayout(ISASConnection dbc,Hashtable par ,NDOContainer cnt) {
	String txt = "";
	boolean done = false;
	String titolo = "";
	ISASCursor dbconf = null;
	
	try {
		String  mysel = "SELECT conf_txt,conf_key FROM conf WHERE "+
			"conf_kproc='SINS' AND (conf_key='codice_regione'"+
			" OR conf_key='ragione_sociale'"+
			" OR conf_key='codice_usl')";
		debugMessage("FoTipoUteDimisEJB.preparaLayout(): "+mysel);

		dbconf = dbc.startCursor(mysel);
		while (dbconf.next()) {
			ISASRecord dbtxt=dbconf.getRecord();
			if (((String)dbtxt.get("conf_key")).equals("ragione_sociale"))
				txt=(String)dbtxt.get("conf_txt");
			if (((String)dbtxt.get("conf_key")).equals("codice_regione"))
				codice_regione=(String)dbtxt.get("conf_txt");
			if (((String)dbtxt.get("conf_key")).equals("codice_usl"))
				codice_usl=(String)dbtxt.get("conf_txt");
		}
		dbconf.close();
		cnt.setFooter(txt);

		Vector vtitoli = new Vector();

// 17/11/11	inPiemonte = ((par.get("piem")!=null && par.get("piem").toString().equals("S"))?true:false);		
		inPiemonte = ((par.get("piem") != null) && ((par.get("piem").toString()).equals("S")));
		
		String tipoStampa=(String)par.get("terr");
		StringTokenizer st = new StringTokenizer(tipoStampa,"|");

		String sZona=st.nextToken();
		String sDis=st.nextToken();
		String sCom=st.nextToken();
		String sTipo=st.nextToken();
		String sEta=st.nextToken();
		String sAss=st.nextToken();
		
		String motivo = ISASUtil.getValoreStringa(par, "motivo");
		String subTitolo="";

		if (sZona.equals("1")){
		   vtitoli.add("Zona");
		   subTitolo="Zona: "+DecodificaZona(dbc,(String)par.get("zona"));
		}
		else
		   subTitolo="Zona: Nessuna divisione";

		if (sDis.equals("1")){
			vtitoli.add("Distretto");
			subTitolo=subTitolo+" - Distretto: "+DecodificaDistretto(dbc,(String)par.get("distretto"));
		}
		else
			subTitolo=subTitolo+" - Distretto: Nessuna divisione";

   	    String ragg =(String)par.get("ragg");
        String tipopca="";
		
//                if (ragg.equals("A")) {
//                    tipopca = " Area distrettuale ";
//                } else if (ragg.equals("C")) {
//                        tipopca = " Comune ";
//                } else if (ragg.equals("P"))
//                        tipopca = " Presidio ";

		if(this.dom_res==null)   {
			if (ragg.equals("A")) 
				tipopca = " Area distrettuale ";
			else if (ragg.equals("C")) 
				tipopca = " Comune ";
			else if (ragg.equals("P"))
				tipopca = " Presidio ";
		}else if (this.dom_res.equals("D")) {
			if (ragg.equals("A")) 
				tipopca = " Area distrettuale di Domicilio ";
			else if (ragg.equals("C")) 
				tipopca = " Comune di Domicilio ";
		}else if (this.dom_res.equals("R")) {
			if (ragg.equals("A")) 
				tipopca = " Area distrettuale di Residenza ";
			else if (ragg.equals("C")) 
				tipopca = " Comune di Residenza ";
		} 	        			
                
		if (sCom.equals("1")){
            vtitoli.add(tipopca);
            subTitolo=subTitolo+" - "+tipopca+": "+DecodificaLiv3(dbc,(String)par.get("pca"), ragg);
		}
        else
            subTitolo=subTitolo+" - "+tipopca+" Nessuna divisione";

        if (inPiemonte) {
			if (sTipo.equals("1")){
				vtitoli.add("Tipologia di Cura");
// 17/11/11		           subTitolo=subTitolo+" - Tipologia di Cura: "+ISASUtil.getDecode(dbc,"tab_voci", "tab_cod", "tab_val","SAOADI", par.get("ute"), "tab_descrizione");
				subTitolo=subTitolo+" - Tipologia di Cura: "+DecodificaTipoUtente(dbc,(String)par.get("ute"), inPiemonte);
			}
            else
                subTitolo=subTitolo+" - Tipologia di Cura: Nessuna divisione";
		}
		else {
			if (sTipo.equals("1")){
                vtitoli.add("Tipologia Utente");
// 17/11/11			subTitolo=subTitolo+" - Tipologia Utente: "+DecodificaTipoUtente(dbc,(String)par.get("ute"));
                subTitolo=subTitolo+" - Tipologia Utente: "+DecodificaTipoUtente(dbc,(String)par.get("ute"), inPiemonte);
			}
            else
                subTitolo=subTitolo+" - Tipologia Utente: Nessuna divisione";
		}
		
        if (sEta.equals("1")){
            vtitoli.add("Fasce Eta'");
            subTitolo=subTitolo+" - Fasce Eta': "+DecodificaEta((String)par.get("eta"));
		}
        else
            subTitolo=subTitolo+" - Fasce Eta': Nessuna divisione";

        if (ISASUtil.valida(motivo)){
        	if (motivo.equals("NESDIV")){
        		subTitolo+= " - Motivo contatto: Nessuna divisione ";
        	}else {
        		subTitolo+= " - Motivo contatto: "+ decodificaMotivoContatto(dbc, motivo);
        		vtitoli.add("Motivo contatto ");
        	}
        }else {
        	vtitoli.add("Motivo contatto ");
        	subTitolo+= " - Motivo contatto: Tutti ";
        }
       
        
        
        if (sAss.equals("1")){
            vtitoli.add("Assistiti");
            subTitolo=subTitolo+" - Assistiti: Elenco completo";
        }
        else
            subTitolo=subTitolo+" - Assistiti: NO";

        cnt.setGroupTitles(vtitoli);
		
// 17/11/11	titolo=" Servizio sociale : Riepilogo attivita' in funzione del tipo utente e motivo dimissione";
        titolo=" Servizio sociale : Riepilogo attivita' in funzione del tipo ";
		if (inPiemonte)
			titolo += "di cura";
		else
			titolo += "utente";
		titolo += " e motivo dimissione";
		
		String meseIni = (String)par.get("me_ini");
		String meseFine = (String)par.get("me_fine");
        if (meseIni.equals(meseFine))
             titolo=titolo+" per il mese: "+DecodMese(meseIni);
        else
            titolo=titolo+" dal Mese: "+DecodMese(meseIni)+" al Mese: "+DecodMese(meseFine);
        titolo=titolo+ " - anno: "+(String) par.get("an");

		cnt.setHeader(titolo);
		cnt.setSubTitle(subTitolo);
		done = true;
	} catch (Exception ex) {
	//	cnt.setHeader(titolo);
	}finally{
		if(!done){
			try{
                             if (dbconf!=null)
                                      dbconf.close();
			}catch(Exception e1){
				System.out.println("FoTipoUteDimisEJB.preparaLayout(): "+e1);
			}
		}
	}
}

private String decodificaMotivoContatto(ISASConnection dbc, String motivo) {
	String punto = MIONOME + "decodificaMotivoContatto ";
	String descrizioni = "";
	String query = "select descrizione from motivo_s where codice = '" +motivo +"' ";
	System.out.println(punto + " Query>"+query+"<");
	try {
		ISASRecord dbrMotivoS = dbc.readRecord(query);
		descrizioni = ISASUtil.getValoreStringa(dbrMotivoS, "descrizione");
	} catch (Exception e) {
		System.out.println(punto + " Errore in query>"+query+"<");
		e.printStackTrace();
	}
	return descrizioni;
}


private String DecodificaLiv3(ISASConnection dbc,String pca,String ragg){
  String ret = "";
  try {
    if (pca!=null){
      if (pca.equals("") && !ragg.equals("A"))
        ret = " TUTTI";
      else if (pca.equals("") && ragg.equals("A"))
        ret = " TUTTE";
      else{
        ISASRecord dbr = null;
        String sel = "";
        if (ragg.equals("P")){
          sel = "SELECT despres des FROM presidi WHERE"+
            " codpres='"+pca+"' AND codreg='"+codice_regione+
            "' AND codazsan='"+codice_usl+"'";
          dbr = dbc.readRecord(sel);
        }else if (ragg.equals("C")){
          sel = "SELECT descrizione des FROM comuni WHERE"+
            " codice='"+pca+"'";
          dbr = dbc.readRecord(sel);
        }else if (ragg.equals("A")){
          sel = "SELECT descrizione des FROM areadis WHERE"+
            " codice='"+pca+"'";
          dbr = dbc.readRecord(sel);
        }
        if (dbr!=null && dbr.get("des")!=null)
          ret = (String)dbr.get("des");
      }
     }
    }catch (Exception ex) {}
    return ret;
}

private String DecodificaZona(ISASConnection dbc,String zona){
  String ret = "";
  try {
    if (zona!=null){
      if (zona.equals(""))
        ret = "TUTTE";
      else{
        String sel = "SELECT descrizione_zona FROM zone WHERE"+
          " codice_zona='"+zona+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("descrizione_zona")!=null)
          ret = (String)dbr.get("descrizione_zona");
      }
    }
  } catch (Exception ex) { }
  return ret;
}

// 17/11/11 private String DecodificaTipoUtente(ISASConnection dbc,String tipoute){
private String DecodificaTipoUtente(ISASConnection dbc,String tipoute, boolean inPiem){
	String ret = "";
	try {
		if (tipoute!=null){
			if (tipoute.equals(""))
				ret = "TUTTE";
			else{
				if (inPiem) {// 17/11/11
					ret = ISASUtil.getDecode(dbc,"tab_voci", "tab_cod", "tab_val","SAOADI", tipoute, "tab_descrizione");
				} else {
					String sel = "SELECT descrizione FROM tipute_s WHERE"+
							" codice='"+tipoute+"'";
					ISASRecord dbr = dbc.readRecord(sel);
					if (dbr!=null && dbr.get("descrizione")!=null)
						ret = (String)dbr.get("descrizione");
				}
			}
		}
	} catch (Exception e) {
   		debugMessage("FoTipoUteDimisEJB.DecodificaTipoUtente(): "+e);
	}
	return ret;
}

private String DecodificaDimissioni(ISASConnection dbc,String tipoute){
  String ret = "NON DEFINITO";
  try {
    if (tipoute!=null){
        String sel = "SELECT tab_descrizione FROM tab_voci WHERE"+
          " tab_cod='ICHIUS' AND tab_val='"+tipoute+"'" ;
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("tab_descrizione")!=null)
        {
          ret = (String)dbr.get("tab_descrizione");
          if((ret.trim()).equals(".")) ret="NON DEFINITO";
        }
    }
  } catch (Exception e) {
     		debugMessage("FoTipoUteDimisEJB.DecodificaDimissioni(): "+e);
}
  return ret;
}
private String DecodificaEta(String eta){
  String ret = "";
    if (eta.equals(""))
        ret ="TUTTI";
    else if (eta.equals("NESDIV"))
        ret ="NESSUNA DIVISIONE";
    else if (eta.equals("1"))
        ret ="MINORE DI 64 ANNI";
    else if (eta.equals("2"))
        ret ="DA 65 A 74 ANNI";
    else if (eta.equals("3"))
        ret = "MAGGIORE DI 75 ANNI";
 return ret;
}
private String DecodificaDistretto(ISASConnection dbc,String distr) {
  String ret = "";
  try {
    if (distr!=null){
      if (distr.equals(""))
        ret = "TUTTI";
      else{
        String sel = "SELECT des_distr FROM distretti WHERE"+
          " cod_distr='"+distr+"'";
        ISASRecord dbr = dbc.readRecord(sel);
        if (dbr!=null && dbr.get("des_distr")!=null)
          ret = (String)dbr.get("des_distr");
      }
    }
  }catch (Exception e) {
  		debugMessage("FoTipoUteDimisEJB.DecodificaDistretto(): "+e);
    }
  return ret;
}
private void preparaBody( ISASCursor dbcur,NDOContainer cnt,
                          NDOUtil unt,Hashtable par,ISASConnection  dbc)
	throws Exception {
	try {
              String selTemp=critTemporale(par);
               while (dbcur.next())
               {
                	ISASRecord dbr=dbcur.getRecord();
                        ElaboraDati(dbr,cnt,unt,par,dbc,selTemp);
		}
                cnt.calculate();
		//cnt.debugPrint();	// DEBUG
	} catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.preparaBody(): "+e);
		throw new SQLException("Errore eseguendo preparaBody()");
	}
}
/*
private String faiSelAccessi(ISASConnection dbc,ISASRecord dbr,Hashtable par)
throws Exception {
    try{
      String data_fine="";
      if (par.get("dataf") != null){
              data_fine=(String)(par.get("dataf"));
      }
      String data_ini="";
      if (par.get("datai") != null){
         data_ini=(String)(par.get("datai"));
      }
      String cartella=""+dbr.get("n_cartella");
      String contatto=""+dbr.get("n_contatto");
      String  selectAccessi = "SELECT COUNT(*) somma "+
                              " FROM interv  WHERE "+
                              " int_cartella="+cartella+
                              " AND int_contatto="+contatto+
                              " AND int_data_prest<="+formatDate(dbc,data_fine)+
                              " AND int_data_prest>="+formatDate(dbc,data_ini)+
                              " AND int_tipo_oper='02'"+
                              " GROUP BY int_cartella";
//        System.out.println("FoTipoUteDimisEJB.faiSelAccessi(): "+selectAccessi);
       return selectAccessi;
    } catch(Exception e) {
            debugMessage("FoTipoUteDimisEJB.faiSelAccessi(): "+e);
            throw new SQLException("Errore eseguendo faiSelAccessi()");
    }
}*/

private String critTemporale(Hashtable par)
{/*mi costruisce la parte che mi filtra il tempo
  che � comune a tutte le select*/

    String meseIni = (String)par.get("me_ini");
    String meseFine = (String)par.get("me_fine");
    String annoIni = (String)par.get("an");
    String sTemp=" p.anno='"+annoIni+"'";

    int meseI=Integer.parseInt(meseIni);
    int meseF=Integer.parseInt(meseFine);
    //se hanno scelto un anno intero non metto il filtro per il mese
    String sMese="";
/*    if (meseI==1 && meseF==12)
           sMese="";
    else{
*/
          for (int i=meseI; i<=meseF; i++){
                  if(i<10)
                      sMese=sMese+ "0"+i+",";
                  else
                      sMese=sMese+i+"," ;
          }
          sMese=sMese.substring(0,sMese.length()-1);//tolgo l'ultima virgola
          sTemp=sTemp+ " AND p.mese IN ("+sMese+")";
  //  }
    return sTemp;
}

private String faiSelAccessi(ISASConnection dbc,ISASRecord dbr,Hashtable par,String selTemp)
throws SQLException{
try{
        //non li testo perch� sono chiave in skinf
        String cartella=""+dbr.get("n_cartella");
        String contatto=""+dbr.get("n_contatto");
/** 17/11/11		
        String  selectAccessi = "SELECT sum (p.accessi1+p.accessi2+p.accessi3) somma123 "+
                                " FROM adi_utenti p WHERE " +selTemp+
                                " AND p.contatto="+contatto+
                                " AND p.n_cartella="+cartella+
                                " AND  p.tipo_operatore='02'";
**/
		String dtI = par.get("an")+"-"+par.get("me_ini")+"-01";
		String dtF = getDtFinePeriodo(par, true);
		
		String  selectAccessi = "SELECT COUNT(*) conta FROM interv"
								+ " WHERE int_cartella = " + cartella
								+ " AND int_contatto = " + contatto
								+ " AND int_tipo_oper = '02'"
								+ " AND int_data_prest >= " + formatDate(dbc, dtI)
								+ " AND int_data_prest <= " + formatDate(dbc, dtF);
		
        return selectAccessi;
    }
    catch(Exception e){
        debugMessage("FoTipoUteDimisEJB.faiSelAccessi(): "+e);
        throw new SQLException("Errore eseguendo faiSelAccessi()");
    }
}
private void settaTitoliColonneDimissioni(NDOUtil unt,NDOContainer cnt,String chiave,ISASConnection dbc,
            String dimissioni)
{
   /*Controllo che non sia inserito nella hashtable HDescCol
   Se lo trovo nella Hashtable vuol dire che per quella chiave ho
   gi� inserito la descrizione*/

   if (!HDescCol.containsKey(chiave)){
         cnt.setColTitle(unt.mkPar("DIM",chiave),dimissioni);
   }
   HDescCol.put(chiave,"");
}

private void settaTitoliRiga(Vector vDesc,Vector vCod,
                          NDOContainer cnt ,NDOUtil unt)
throws Exception{
	String punto = MIONOME + "settaTitoliRiga ";
    try  {
           int dim =vCod.size();

           Vector vChiave=new Vector();

           for (int i=0;i<vCod.size();i++) {
              if (i==0)
                 vChiave.add(i,""+vCod.elementAt(i));
              else {
                vChiave.add(i,(""+vChiave.elementAt(i-1))+"|"+vCod.elementAt(i));
              }
           }
           
//       	for (int i = 0; i < vChiave.size(); i++) {
//    		stampa(punto + i+"i>"+vChiave.get(i));
//		}
//           stampa(punto + " dim>"+dim+"<");
           if(dim==0)
                            cnt.setRowTitle(unt.mkPar("TOT"),"Totali generali");
           else{
//        	   stampa(punto + "1 ddddd ");
                if (!h_1.containsKey(""+vChiave.elementAt(0))){
                    /*Se non ho la descrizione per il livello pi� alto certamente non
                      cel'ho per i sottolivelli
                    */
                   cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0)),""+vDesc.elementAt(0));
                   h_1.put(""+vChiave.elementAt(0),""+vDesc.elementAt(0));
//                   stampa(punto + "1 bbbbbbbbb ");
                   if(dim>1){
                       cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1)),""+vDesc.elementAt(1));
                       h_2.put(""+vChiave.elementAt(1),""+vDesc.elementAt(1));
                       if (dim>2){
//                    	   stampa(punto + "1 ccccc ");
                          cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2)),""+vDesc.elementAt(2));
                          h_3.put(""+vChiave.elementAt(2),""+vDesc.elementAt(2));
                           if (dim>3){
//                        	   stampa(punto + "1 eeeeeee");
                               cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3)),""+vDesc.elementAt(3));
                               h_4.put(""+vChiave.elementAt(3),""+vDesc.elementAt(3));
                               if (dim>4){
//                            	   stampa(punto + "1 ffffffff ");
                                    cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                ""+vCod.elementAt(3),""+vCod.elementAt(4) ),""+vDesc.elementAt(4) );
                                    h_5.put(""+vChiave.elementAt(4),""+vDesc.elementAt(4));
                                   if (dim>5){
//                                	   stampa(punto + "1 gggggg ");
                                        cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                    ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5)),
                                                    ""+vDesc.elementAt(5));
                                        h_6.put(""+vChiave.elementAt(5),""+vDesc.elementAt(5));
                                        if (dim>6){
//                                        	stampa(punto + "1 valori >"+vDesc.elementAt(5)+"<");
                                        	
                                            cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                        ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                        ""+vDesc.elementAt(6));
                                            h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                            
//                                            for (int i = 0; i < vCod.size(); i++) {
//                                            	stampa(punto + i+" key>"+vCod.get(i)+"="+vDesc.get(i));
//                                            }
                                        }else {
//                                        	stampa(punto + "1 iiiiii ");
                                        }
                                   		}
                                   }}}}//fine dim
                }else{
//                	stampa(punto + " \n stampo qui >>>>");
                   if (dim>1 && !h_2.containsKey(""+vChiave.elementAt(1))){
                   /*controllo i livelli inferiori*/
                       cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1)),""+vDesc.elementAt(1));
                       h_2.put(""+vChiave.elementAt(1),""+vDesc.elementAt(1));
                       if (dim>2){
                          cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2)),""+vDesc.elementAt(2));
                          h_3.put(""+vChiave.elementAt(2),""+vDesc.elementAt(2));
                           if (dim>3){
                               cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3)),""+vDesc.elementAt(3));
                               h_4.put(""+vChiave.elementAt(3),""+vDesc.elementAt(3));
                               if (dim>4){
                                    cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                ""+vCod.elementAt(3),""+vCod.elementAt(4)),""+vDesc.elementAt(4));
                                    h_5.put(""+vChiave.elementAt(4),""+vDesc.elementAt(4));
                                   if (dim>5){
                                        cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                    ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5)),
                                                    ""+vDesc.elementAt(5));
                                        h_6.put(""+vChiave.elementAt(5),""+vDesc.elementAt(5));
                                        if (dim>6){
                                            cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                        ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                        ""+vDesc.elementAt(6));
                                            h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                        }
                     }}}}//fine dim
                     }else{
//                    	 stampa(punto + "2 ");
                       if (dim>2 && !h_3.containsKey(""+vChiave.elementAt(2))){
                          cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2)),""+vDesc.elementAt(2));
                          h_3.put(""+vChiave.elementAt(2),""+vDesc.elementAt(2));
                          if (dim>3) {
                               cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3)),""+vDesc.elementAt(3));
                               h_4.put(""+vChiave.elementAt(3),""+vDesc.elementAt(3));
                               if (dim>4){
                                    cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                ""+vCod.elementAt(3),""+vCod.elementAt(4)),""+vDesc.elementAt(4));
                                    h_5.put(""+vChiave.elementAt(4),""+vDesc.elementAt(4));
                               if (dim>5){
                                        cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                    ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5)),
                                                    ""+vDesc.elementAt(5));
                                        h_6.put(""+vChiave.elementAt(5),""+vDesc.elementAt(5));
                                        if (dim>6){
                                            cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                        ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                        ""+vDesc.elementAt(6));
                                            h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                        }
                         }}}//fine dim
                        }else{
//                        	stampa(punto + "3 ");
                         if (dim>3 && !h_4.containsKey(""+vChiave.elementAt(3))){
                               cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3)),""+vDesc.elementAt(3));
                               h_4.put(""+vChiave.elementAt(3),""+vDesc.elementAt(3));
                               if (dim>4) {
                                    cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                ""+vCod.elementAt(3),""+vCod.elementAt(4)),""+vDesc.elementAt(4));
                                    h_5.put(""+vChiave.elementAt(4),""+vDesc.elementAt(4));
                                     if (dim>5){
                                        cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                    ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5)),
                                                    ""+vDesc.elementAt(5));
                                        h_6.put(""+vChiave.elementAt(5),""+vDesc.elementAt(5));
                                        if (dim>6){
                                            cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                        ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                        ""+vDesc.elementAt(6));
                                            h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                        }
                         }}//fine dim
                         }else{
//                        	 stampa(punto + " 4 ");
                           if (dim>4 && !h_5.containsKey(""+vChiave.elementAt(4))) {
                              cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3),""+vCod.elementAt(4)),""+vDesc.elementAt(4));
                              h_5.put(""+vChiave.elementAt(4),""+vDesc.elementAt(4));
                               if (dim>5){
                                        cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                    ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5)),
                                                    ""+vDesc.elementAt(5));
                                        h_6.put(""+vChiave.elementAt(5),""+vDesc.elementAt(5));
                                        if (dim>6){
                                            cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                        ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                        ""+vDesc.elementAt(6));
                                            h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                        }
                                }//fine dim
                             } else{
//                            	 stampa(punto + " 4 ");
                                 if (dim>5 && !h_6.containsKey(""+vChiave.elementAt(5))) {
                                        cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                    ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5)),
                                                    ""+vDesc.elementAt(5));
                                        h_6.put(""+vChiave.elementAt(5),""+vDesc.elementAt(5));
                                        if (dim>6){
                                            cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                        ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                        ""+vDesc.elementAt(6));
                                            h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                        }
                                 }else {
//                                	 stampa(punto + " 5 ");
                                         if (dim>6){
                                             cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0),""+vCod.elementAt(1),""+vCod.elementAt(2),
                                                         ""+vCod.elementAt(3),""+vCod.elementAt(4),""+vCod.elementAt(5),""+vCod.elementAt(6)),
                                                         ""+vDesc.elementAt(6));
                                             h_7.put(""+vChiave.elementAt(6),""+vDesc.elementAt(6));
                                         }else {
//                                        	 stampa(punto + " 6 ");
                                         }
                                 }
                             }
                         }
                        }
                     }
                }
          }//fine if dim=0
//           stampa(punto + " h7>"+h_7);
    }
    catch(Exception e)
    {
         debugMessage("FoTipoUteDimisEJB.settaTitoliRiga: "+e);
          throw new Exception("Errore eseguendo settaTitoliRiga()");
   }
}

private void  ElaboraDati(ISASRecord dbr,NDOContainer cnt,NDOUtil unt,Hashtable par,ISASConnection dbc,
            String selTemp)
throws Exception {
	String punto = MIONOME + "ElaboraDati ";
	try {
          String tipoStampa=(String)par.get("terr");
          StringTokenizer st = new StringTokenizer(tipoStampa,"|");
          /*se trovo 0 vuol dire Nessuna divisione-->non devo stampare il livello
          se trovo 1 vuol dire che lo devo stampare
          prima posizione � la zona
          seconda -->il distretto
          terza --> il comune/Areadis
          quarta-->tipo utenza
          quinta -->et�
          sesta-->assistiti
          */

          String sZona=st.nextToken();
          String sDis=st.nextToken();
          String sCom=st.nextToken();
          String sTipo=st.nextToken();
          String sEta=st.nextToken();
          String sAss=st.nextToken();
          String motivo = ISASUtil.getValoreStringa(par, "motivo");
          
          Vector vRiga=new Vector();//carico in un vettore gli eventuali elementi di riga
          Vector vDecRiga=new Vector();//carico in un vettore le descrizioni

          if(sZona.equals("1")){
             vRiga=Aggiungi(vRiga,"zona_cod", dbr);
             vDecRiga=Aggiungi(vDecRiga,"zona_desc", dbr);
          }
		  
          if(sDis.equals("1")){
             vRiga=Aggiungi(vRiga,"distr_cod", dbr);
             vDecRiga=Aggiungi(vDecRiga,"distr_desc", dbr);
          }
		  
          if(sCom.equals("1")){
             vRiga=Aggiungi(vRiga,"pca_cod", dbr);
             vDecRiga=Aggiungi(vDecRiga,"pca_desc", dbr);
          }
		  
        if(sTipo.equals("1")){
			String  tipoUte="NON DEFINITO";
			String  descUte="NON DEFINITO";
			// 17/11/11 ------------------
			String nmFldTipo = "alias_fld_tipo";
			// 17/11/11 ------------------		   
			if (dbr.get(nmFldTipo)!=null) {
				tipoUte=(String)dbr.get(nmFldTipo);
				if(!(tipoUte.trim()).equals("")){
                    if (!hDescTipoUte.containsKey(tipoUte))
                        descUte=DecodificaTipoUtente(dbc,tipoUte,inPiemonte);
                    else
                        hDescTipoUte.put(tipoUte,descUte);
               }else{
                    tipoUte="NON DEFINITO";
                    descUte="NON DEFINITO";
               }
           }
           vRiga.add(tipoUte);
           vDecRiga.add(descUte);
        }
		  
          if(sEta.equals("1")){
              String codEta="0";
              String descEta="0";
              //l'eta la devo calcolare in base alla data di fine periodo
              int eta=CalcolaEta(dbr,par);
              if(eta<=64){
                  codEta="0";
                  descEta=" MINORE 64 ANNI";
              }
              else if(eta>=65 && eta<=74){
                  codEta="64";
                  descEta="DA 65 A 74 ANNI";
              }
              else if(eta>=75 ){
                  codEta="75";
                  descEta="MAGGIORE DI 75 ANNI";
              }
              vRiga.add(codEta);
              vDecRiga.add(descEta);
          }
          
          
      	if (  (!ISASUtil.valida(motivo)) || ((ISASUtil.valida(motivo) && !motivo.equals("NESDIV")) ) ) {
			String skiMotivo = ISASUtil.getValoreStringa(dbr, "ski_motivo"); 
			String descMotivo = "NON DEFINITO";
			if (ISASUtil.valida(skiMotivo)) {
				if (!skiMotivo.equals(CONSTANTS_NESSUNA_DIVISIONE)) {
					if (ISASUtil.valida(skiMotivo)) {
						if (!hDescMotivo.containsKey(skiMotivo)){
							descMotivo = decodificaMotivoContatto(dbc, skiMotivo);
							hDescMotivo.put(skiMotivo, descMotivo);
						}else {
							descMotivo = hDescMotivo.get(skiMotivo)+"";
						}
					} else {
						skiMotivo = "NON DEFINITO";
						descMotivo = "NON DEFINITO";
					}
				}else {
					skiMotivo = "NON DEFINITO";
					descMotivo = "NON DEFINITO";
				}
			}
//			stampa(punto + " riga aggiungo>"+descMotivo +"skiMotivo>"+skiMotivo+"< \n");
			vRiga.add(skiMotivo);
//			stampa(punto + " descri aggiungo>"+descMotivo +"skiMotivo>"+skiMotivo+"< \n");
			vDecRiga.add(descMotivo);
//			stampa(punto + " fine >"+hDescMotivo +"skiMotivo>"+skiMotivo+"< \n");
		}
		  
          if(sAss.equals("1")){
             vRiga=Aggiungi(vRiga,"n_cartella", dbr);
             vDecRiga=Aggiungi(vDecRiga,"assistito", dbr);
//             stampa(punto + " inserisco assistito>"+vRiga.size()+"<"); 
          }
          
//          for (int i = 0; i < vRiga.size(); i++) {
//        	  stampa(punto + i+ "elemento>"+vRiga.get(i));
//          }
          
          
/*  17/11/11: X DEBUG
for (Enumeration en=vRiga.elements(); en.hasMoreElements(); )		  
		  debugMessage("FoTipoUteDimisEJB.ElaboraDati(): vRiga elemnts=["+en.nextElement()+"]");		  
 17/11/11: X DEBUG */
          caricaNDO(vRiga,vDecRiga,dbr,unt,cnt,dbc,par,selTemp);

        } catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.ElaboraDati(): "+e);
		throw new SQLException("Errore eseguendo ElaboraDati()");
	}
}

private int ContaAccessi(ISASRecord dbr,ISASConnection dbc,Hashtable par,String selTemp)
throws Exception {
   try{
      int ret=0;
      String sel =faiSelAccessi(dbc,dbr,par,selTemp);
      ISASRecord dbrAcc = dbc.readRecord(sel);
      if (dbrAcc!=null )
// 17/11/11	ret=convNumDBToInt("somma123", dbrAcc);
		ret=convNumDBToInt("conta", dbrAcc);
      return ret;
   } catch(Exception e) {
            debugMessage("FoTipoUteDimisEJB.ContaAccessi(): "+e);
            throw new SQLException("Errore eseguendo ContaAccessi()");
   }
}

private int CalcolaEta(ISASRecord dbr,Hashtable par)
throws Exception {
   try
   {
        int eta=0;
        String mydate="";
        String meseFine = (String)par.get("me_fine");
        String anno= (String) par.get("an");
        if(meseFine.equals("12"))
        {
              int ianno=Integer.parseInt(anno);
              ianno++;
              anno=""+ianno;
        }
        String data_fine=anno+"/"+meseFine+"/"+"01";
        if (dbr.get("data_nasc")!=null)
        {
            mydate=((java.sql.Date)dbr.get("data_nasc")).toString();
            if (!mydate.equals(""))
              eta=this.ConvertData(mydate, data_fine);
            else eta=0;
        }
        return eta;
     } catch(Exception e) {
              debugMessage("FoTipoUteDimisEJB.CalcolaEta(): "+e);
              throw new SQLException("Errore eseguendo CalcolaEta()");
      }
}

private int ContaGiorni(ISASRecord dbr,Hashtable  par)
throws Exception {
   try{
/*
Conto i giorni in cui la persona � stata in assistenza
I giorni di assistenza sono quelli per i quali il contatto � aperto.
Devo fare la differenza fra due date data_1 e data_2
Se la data apertura contatto � minore della data inizio periodo-->data_1 =data inizio periodo
Se la data apertura contatto � maggiore della data inizio periodo-->data_1 =data apertura contatto
Se la data chiusura contatto � nulla o � maggiore della data fine periodo-->data_2 =data fine periodo
Se la data chiusura contatto  minore della data fine periodo-->data_2 =data chiusura contatto
*/
      String data_1="";
      String data_2="";
      int giorni=0;

      String meseIni = (String)par.get("me_ini");
      String meseFine = (String)par.get("me_fine");
      String anno= (String) par.get("an");

      String data_ini=anno+"/"+meseIni+"/01";

      String data_fine=anno+"/"+meseFine+"/";

      //devo prendere il giorno fine mese
      java.util.GregorianCalendar DefGreg = new java.util.GregorianCalendar();
      int imese=Integer.parseInt(meseIni);
      imese=imese-1;
      String sMese="";
      if (imese<10)
            sMese="0"+imese;
      else
            sMese=""+imese;
      DataWI dataWI=new DataWI("01"+sMese+anno);
      DefGreg.setTime(dataWI.getSqlDate());
      int gio=DefGreg.getActualMaximum(GregorianCalendar.DAY_OF_MONTH ) ;
      data_fine=data_fine+gio;

      String data_apertura="";
      if (dbr.get("ski_data_apertura")!=null)
           data_apertura=((java.sql.Date)dbr.get("ski_data_apertura")).toString();
      if(data_apertura.equals(""))return giorni;
      String data_chiusura="";
      if (dbr.get("ski_data_uscita")!=null)
           data_chiusura=((java.sql.Date)dbr.get("ski_data_uscita")).toString();
      else{
         if (dbr.get("data_chiusura")!=null)
           data_chiusura=((java.sql.Date)dbr.get("data_chiusura")).toString();
      }

      int iDiff=controllaData(data_apertura,data_ini);
      if(iDiff==1)
      {//data_apertura>data_ini
              data_1=data_apertura;
      }else if (iDiff==2){
      //data_apertura<data_ini
              data_1=data_ini;
      }else if (iDiff==0){
      //data_apertura=data_ini
              data_1=data_ini;
      }else{// errore
      return giorni;}
      if (data_chiusura.equals(""))
      {//la data chiusura � nulla
           data_2=data_fine;
      }
	  else

	  {//data chiusura valorizzata
          iDiff=controllaData(data_chiusura,data_fine);
          if(iDiff==1)
          {//data_chiusura>data_fine
                  data_2=data_fine;
          }else if (iDiff==2){
          //data_chiusura<data_fine
                  data_2=data_chiusura;
          }else if (iDiff==0){
          //data_chiusura=data_fine
                  data_2=data_fine;
          }else{// errore
          return giorni;}
      }
      giorni=DiffDate(data_1,data_2);
      return giorni;
     } catch(Exception e) {
              debugMessage("FoTipoUteDimisEJB.ContaGiorni(): "+e);
              throw new SQLException("Errore eseguendo ContaGiorni()");
      }
}

/**
* se data_inizio � maggiore di data_fine restituisce 1
* se data_inizio � minore di data_fine restituisce 2
* se data_inizio � = di data_fine restituisce 0
* se da errore -1
*/
private int controllaData(String data_inizio,String data_fine) {
/*        if (data_inizio.length()!=10 && data_fine.length()!=10)
                return -1;
        data_inizio = data_inizio.substring(0,4)+
                data_inizio.substring(5,7)+data_inizio.substring(8,10);
        DataWI dataINIZIO=new DataWI(data_inizio);
        data_fine = data_fine.substring(0,4)+
                data_fine.substring(5,7)+data_fine.substring(8,10);
        int rit = dataINIZIO.confrontaConDt(data_fine);
        return rit;
*/
  if (data_inizio.length()!=10 && data_fine.length()!=10)
  return -1;
  data_inizio=data_inizio.substring(8,10)+
	data_inizio.substring(5,7)+data_inizio.substring(0,4);
  DataWI dataINIZIO=new DataWI(data_inizio);

        data_fine = data_fine.substring(0,4)+
                data_fine.substring(5,7)+data_fine.substring(8,10);

//  data_fine=data_fine.substring(6,10)+data_fine.substring(3,5)+data_fine.substring(0,2);
  int rit=dataINIZIO.confrontaConDt(data_fine);
  return rit;

}

private Vector mkRiga(Vector vCod,NDOUtil unt)
{
           int dim=vCod.size();
           Vector vRiga=new Vector();
           switch (dim)
           {
                    case 0:
                          vRiga=unt.mkPar("TOT");
                          break;
                    case 1:
                          vRiga=unt.mkPar(""+vCod.elementAt(0));
                          break;
                    case 2:
                          vRiga=unt.mkPar(""+vCod.elementAt(0),
                                          ""+vCod.elementAt(1));
                          break;
                    case 3:
                          vRiga=unt.mkPar(""+vCod.elementAt(0),
                                          ""+vCod.elementAt(1),
                                          ""+vCod.elementAt(2));
                                 break;
                    case 4:
                          vRiga=unt.mkPar(""+vCod.elementAt(0),
                                          ""+vCod.elementAt(1),
                                          ""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3));
                                 break;
                    case 5:
                         vRiga=unt.mkPar(""+vCod.elementAt(0),
                                          ""+vCod.elementAt(1),
                                          ""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3),
                                          ""+vCod.elementAt(4));
                                 break;
                    case 6:
                         vRiga=unt.mkPar(""+vCod.elementAt(0),
                                          ""+vCod.elementAt(1),
                                          ""+vCod.elementAt(2),
                                          ""+vCod.elementAt(3),
                                          ""+vCod.elementAt(4),
                                          ""+vCod.elementAt(5));
                                  break;
                    case 7:
                        vRiga=unt.mkPar(""+vCod.elementAt(0),
                                         ""+vCod.elementAt(1),
                                         ""+vCod.elementAt(2),
                                         ""+vCod.elementAt(3),
                                         ""+vCod.elementAt(4),
                                         ""+vCod.elementAt(5),
                                         ""+vCod.elementAt(6));
                                 break;           

                  }
       return vRiga;

}

private int DiffDate(String data_ini,String data_fine){

        int ret = 0;
        int anno = Integer.parseInt(data_ini.substring(0,4));
        int mese = Integer.parseInt(data_ini.substring(5,7));
        int giorno = Integer.parseInt(data_ini.substring(8,10));
        DataWI dt1 = new DataWI(anno,mese,giorno);
        ret = dt1.contaGgTra(data_fine.substring(0,4)+
                data_fine.substring(5,7)+data_fine.substring(8,10));
        //aggiungo 1 perch� mi perde un giorno
        ret =ret+1;
        return ret;
}



private void caricaNDO(Vector vCod,Vector vDesc,ISASRecord dbr,
            NDOUtil unt,NDOContainer cnt,ISASConnection dbc,Hashtable par,String selTemp)
throws SQLException {
	String punto = MIONOME + "caricaNDO ";
        try
        {
           Vector vRiga=new Vector();
           vRiga= mkRiga(vCod,unt);
           /*Per gli assistiti se uno ha pi� contatti lo devo contare una volta sola
           mi creo allora una chiave con i valori di riga + n_cartella e lo inserisco in un Hashtable
           Se la chiave esiste vuol dire che l'ho gi� inserito.
           Devo salvarmi tutto l'albero (riga) perch� un assistito posso averlo in pi� righe
           */
           String chiave="";
           if(dbr.get("n_cartella")!=null)
                  chiave=""+dbr.get("n_cartella");
           for (int i=0 ;i<vCod.size();i++)
               chiave=chiave+"|"+vCod.elementAt(i);
//           stampa(punto + " chiave>"+chiave+"< ");
            if (!hAssistito.containsKey(chiave))
            {
                  //Sesso
                  String sesso="";
                  if(dbr.get("sesso")!=null)
                      sesso=(String)dbr.get("sesso");
                  cnt.put(vRiga,unt.mkPar("Sesso",sesso),new Integer(1));
                  //totale sesso
                  cnt.put(vRiga,unt.mkPar("Sesso","TOT"),new Integer(1));
                  hAssistito.put(chiave,"");
            }

            //Accessi
            int risuSele=0;
            risuSele=ContaAccessi(dbr,dbc,par,selTemp);
            cnt.put(vRiga,unt.mkPar("AC","TOT"),new Integer(risuSele));
			
            //Numero giornate
            int numGior=0;//devo calcolare il numero giornate
            numGior=ContaGiorni(dbr,par);
            cnt.put(vRiga,unt.mkPar("NUM","TOT"),new Integer(numGior));
			
            //Controllo che il contatto sia chiuso per analizzare il motivo di dimissione
/** 17/11/11			
			// 26/05/06 G.Brogi verifica che il contatto sia aperto nel periodo
		            if(dbr.get("ski_data_uscita")==null
			|| ContattoApertoNelPeriodo(dbr,par))
			{//contatto aperto nel periodo		
**/					
			// 17/11/11: contatti ancora aperti a fine periodo (= dtChius NULL o > dtFine)
			if (isContattoApertoAFinePeriodo(dbr, par))
                cnt.put(vRiga,unt.mkPar("CONT","TOT"),new Integer(1));
            else 
            {
                      //Dimissioni
                      String dimissioni="NON DEFINITO";
                      String descDim="NON DEFINITO";
                      if(dbr.get("ski_dimissioni")!=null)
                      {
                            dimissioni=""+dbr.get("ski_dimissioni");
                            descDim=DecodificaDimissioni(dbc,dimissioni);
                            if(descDim.equals("NON DEFINITO"))dimissioni="NON DEFINITO";
                      }
                      cnt.put(vRiga,unt.mkPar("DIM",dimissioni),new Integer(1));
                      //totale dimissioni
                      cnt.put(vRiga,unt.mkPar("DIM","TOT"),new Integer(1));
                      settaTitoliColonneDimissioni(unt,cnt,dimissioni,dbc,descDim);
            }
			 
			// 17/11/11: conteggio totale attivi
			cnt.put(vRiga,unt.mkPar("ATT","TOT"),new Integer(1));
			 
//			 cnt.put(vRiga, obj, vector)
//			 modificato in quanto in questo modo si velocizza rispetto all'uso delle hashtable
//            settaTitoliRiga(vDesc,vCod,cnt,unt);
			settaTitoliRigaNew(vDesc, vCod, cnt, unt);
        } catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.caricaNDO(): "+e);
		throw new SQLException("Errore eseguendo caricaNDO()");
	}
}

private void settaTitoliRigaNew(Vector vDesc, Vector cod, NDOContainer cnt, NDOUtil unt) {
	String punto = MIONOME+ "settaTitoliRigaNew ";

//		stampa(punto + " procedo stampa in ndo" );
	for (int i = 0; i < cod.size(); i++) {
		Vector key= mkRiga(cod, unt,i+1);
//		String elementi = "";
//		for (int j = 0; j < key.size(); j++) {
//			elementi+=key.get(j)+"*";
//		}
//		stampa(punto + i+"elementi>"+elementi+"< cod>"+cod.get(i)+"="+vDesc.elementAt(i)+"<");
		cnt.setRowTitle(key,""+vDesc.elementAt(i));
//		cnt.setRowTitle(unt.mkPar(""+vCod.elementAt(0)),""+vDesc.elementAt(0));
	}
	
}

private Vector mkRiga(Vector vCod, NDOUtil unt, int dim) {
String punto = MIONOME + "mkRiga ";
//	           int dim=vCod.size();
	           Vector vRiga=new Vector();
//	           stampa(punto + " i>"+ dim+"<");
	           switch (dim)
	           {
	                    case 0:
	                          vRiga=unt.mkPar("TOT");
	                          break;
	                    case 1:
	                          vRiga=unt.mkPar(""+vCod.elementAt(0));
	                          break;
	                    case 2:
	                          vRiga=unt.mkPar(""+vCod.elementAt(0),
	                                          ""+vCod.elementAt(1));
	                          break;
	                    case 3:
	                          vRiga=unt.mkPar(""+vCod.elementAt(0),
	                                          ""+vCod.elementAt(1),
	                                          ""+vCod.elementAt(2));
	                                 break;
	                    case 4:
	                          vRiga=unt.mkPar(""+vCod.elementAt(0),
	                                          ""+vCod.elementAt(1),
	                                          ""+vCod.elementAt(2),
	                                          ""+vCod.elementAt(3));
	                                 break;
	                    case 5:
	                         vRiga=unt.mkPar(""+vCod.elementAt(0),
	                                          ""+vCod.elementAt(1),
	                                          ""+vCod.elementAt(2),
	                                          ""+vCod.elementAt(3),
	                                          ""+vCod.elementAt(4));
	                                 break;
	                    case 6:
	                         vRiga=unt.mkPar(""+vCod.elementAt(0),
	                                          ""+vCod.elementAt(1),
	                                          ""+vCod.elementAt(2),
	                                          ""+vCod.elementAt(3),
	                                          ""+vCod.elementAt(4),
	                                          ""+vCod.elementAt(5));
	                                  break;
	                    case 7:
	                         vRiga=unt.mkPar(""+vCod.elementAt(0),
	                                          ""+vCod.elementAt(1),
	                                          ""+vCod.elementAt(2),
	                                          ""+vCod.elementAt(3),
	                                          ""+vCod.elementAt(4),
	                                          ""+vCod.elementAt(5),
	                                          ""+vCod.elementAt(6));
	                                 break;           

	                  }
	       return vRiga;

	}
	
	
	
/** 17/11/11
private boolean ContattoApertoNelPeriodo(ISASRecord dbr, Hashtable par){
	try{
	      String data_fine="";
	      String Mese = ""+par.get("me_fine");
	      String anno = ""+par.get("an");
	      int imese=Integer.parseInt(Mese);

	      java.util.GregorianCalendar DefGreg = new java.util.GregorianCalendar();
	      java.sql.Date dat = java.sql.Date.valueOf(anno+"-"+Mese+"-"+"01");
      	  DefGreg.setTime(dat);

      	      int gio=DefGreg.getActualMaximum(GregorianCalendar.DAY_OF_MONTH ) ;
      	      data_fine=anno+"-"+Mese+"-"+gio;
//	      java.sql.Date Dtfine = java.sql.Date.valueOf(data_fine);

//	      java.sql.Date dt = (java.sql.Date)dbr.get("ski_data_uscita");

System.out.println("Data chiusura = "+dbr.get("ski_data_uscita"));
System.out.println("Data fine periodo = "+data_fine);
	      int controllo = controllaData(""+dbr.get("ski_data_uscita"),
				data_fine);
System.out.println("controllo = "+controllo);
	      return (controllo == 1);

	}catch(Exception e){
		System.out.println("Errore eseguendo una ContattoApertoNelPeriodo: "+e);
		return true;
	}
}
**/

	// 17/11/11
	private boolean isContattoApertoAFinePeriodo(ISASRecord dbr, Hashtable par)
	{
		try{
			// sicuramente aperto
			if (dbr.get("ski_data_uscita") == null)
				return true;
		
		    String data_fine = getDtFinePeriodo(par, false);
		    
//System.out.println("Data chiusura = "+dbr.get("ski_data_uscita"));
//System.out.println("Data fine periodo = "+data_fine);

			// controllo che la dtChiusura sia >= dtFinePeriodo
			String dtChiu = ""+dbr.get("ski_data_uscita");
			DataWI dtChiuWI = new DataWI(dtChiu.substring(8,10)+
									dtChiu.substring(5,7)+dtChiu.substring(0,4));
									
			return (dtChiuWI.isUguOSucc(data_fine));			
		}catch(Exception e){
			System.out.println("Errore eseguendo una isContattoApertoAFinePeriodo: "+e);
			return true;
		}
	}	

	// 17/11/11
	private String getDtFinePeriodo(Hashtable par, boolean conSep)
	{
		int mm = Integer.parseInt(""+par.get("me_fine"));
		int aa = Integer.parseInt(""+par.get("an"));

		java.util.GregorianCalendar defGreg = new java.util.GregorianCalendar(aa, (mm - 1), 1);

		int gioF = defGreg.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		return ("" + aa + (conSep?"-":"") + par.get("me_fine") + (conSep?"-":"") + gioF);	
	}
	
	
private int convNumDBToInt(String nomeCampo, ISASRecord mydbr)throws Exception
{
        int numero = 0;
        Object numDB = (Object)mydbr.get(nomeCampo);
        if (numDB != null) {
                if (numDB.getClass().getName().endsWith("Double"))
                        numero = ((Double)mydbr.get(nomeCampo)).intValue();
                else if (numDB.getClass().getName().endsWith("Integer"))
                        numero = ((Integer)mydbr.get(nomeCampo)).intValue();
        }

        return numero;
}// END convNumDBToInt
public int ConvertData (String dataold,String datanew)
{
        //inizializzazione della variabile eta
        int eta=0;
        int tempeta=0;

        //preparazione primo array
        int[] datavecchia= new int[3];
        Integer giorno = new Integer(dataold.substring(8,10));
        datavecchia[0]= giorno.intValue();
	Integer mese = new Integer(dataold.substring(5,7));
        datavecchia[1]= mese.intValue();
        Integer anno = new Integer(dataold.substring(0,4));
         datavecchia[2]= anno.intValue();

        //preparazione secondo array

        int[] datanuova= new int[3];
        Integer day = new Integer(datanew.substring(8,10));
        datanuova[0]= day.intValue();
        Integer mounth = new Integer(datanew.substring(5,7));
        datanuova[1]= mounth.intValue();
         Integer year = new Integer(datanew.substring(0,4));
        datanuova[2]= year.intValue();

        tempeta= datanuova[2]-datavecchia[2];
        //confronto mese
        if (datanuova[1] < datavecchia[1])
                tempeta=tempeta-1;      // anni non ancora compiuti
        else if (datanuova[1] == datavecchia[1])
                if (datanuova[0] < datavecchia[0])      //confronto giorno
                        tempeta=tempeta-1;      // anni non ancora compiuti
        eta=tempeta;
        return eta;
}
private Vector Aggiungi(Vector v,String campo,ISASRecord dbr)
throws Exception {
  try{

       if (dbr.get(campo)!=null)
            v.add(""+dbr.get(campo));
        return v;
    } catch(Exception e)
    {
		debugMessage("FoTipoUteDimisEJB.Aggiungi(): "+e);
		throw new SQLException("Errore eseguendo Aggiungi()");
	}
}

private String mkDefinisciSelect(ISASConnection dbc, Hashtable par)
{
          String s="";
          String tipoStampa=(String)par.get("terr");
          StringTokenizer st = new StringTokenizer(tipoStampa,"|");
          String sZona=st.nextToken();
          String sDis=st.nextToken();
          String sCom=st.nextToken();
          boolean stampaTerr=false;
          if(sZona.equals("1") || sDis.equals("1") || sCom.equals("1"))
                stampaTerr=true;
/** 17/11/11				
		if(stampaTerr)
                s=mkSelectTot(dbc,par);
          else
                s=mkSelectNoTerr(dbc,par);
**/
			s=mkSelectTot(dbc,par);
     return s;
}


private String mkSelectTot(ISASConnection dbc, Hashtable par){
     ServerUtility su =new ServerUtility();

     String meseIni = (String)par.get("me_ini");
     String meseFine = (String)par.get("me_fine");
     String anno= (String) par.get("an");
	
     String data_ini="01/"+meseIni+"/"+anno;
     if(meseFine.equals("12"))
     {
          int ianno=Integer.parseInt(anno);
          ianno++;
          anno=""+ianno;
	 // G.Brogi
	  meseFine = "01";
     }else {// G.Brogi
	int Mesef = Integer.parseInt(meseFine);
	if(Mesef<12) Mesef = Mesef+1;
	if(Mesef<10)
		meseFine = "0"+Mesef;
	else
		meseFine = ""+Mesef;

     }
	 	String data_fine = "01/"+meseFine+"/"+anno;
	/*bisogna considerare lo storico di anagra_c in maniera particolare:
Se nell'intervallo di tempo indicato dal client un assistito ha cambiato
 residenza/domicilio devo considerarlo pi� volte (tante quante ha cambiato residenza)
Non devo prendere pi� l'ultimo comune di domicilio inserito ma quello relativo al periodo scelto.
*/
	String clausola_date = " AND co.ski_data_apertura<"+formatDate(dbc,data_fine)+
						" AND (co.ski_data_uscita IS NULL OR"+
							" co.ski_data_uscita>="+formatDate(dbc,data_ini)+
						")"+
						" AND a.data_variazione IN ("+
							" SELECT MAX (an.data_variazione)"+
							" FROM anagra_c an WHERE an.n_cartella=c.n_cartella"+
							" AND ((an.data_variazione<"+formatDate(dbc,data_fine)+"AND co.ski_data_uscita is null)"+
							" OR (an.data_variazione <=co.ski_data_uscita AND co.ski_data_uscita is not null))"+
						") ";
/** 17/11/11: contatti ATTIVI nel periodo anche in PIEMONTE						
	if (inPiemonte) //aggiunto controllo per Piemonte su: data apertura> data inizio, inclusione defunti, 
		clausola_date = " AND co.ski_data_apertura<"+formatDate(dbc,data_fine)+
						" AND co.ski_data_apertura>"+formatDate(dbc,data_ini)+
						" AND a.data_variazione IN ("+
							" SELECT MAX (an.data_variazione)"+
							" FROM anagra_c an WHERE an.n_cartella=c.n_cartella"+
							" AND ((an.data_variazione<"+formatDate(dbc,data_fine)+" AND co.ski_data_uscita is null)"+
							" OR (an.data_variazione <=co.ski_data_uscita AND co.ski_data_uscita is not null))"+
						") ";
**/	

		// 17/11/11: per estrazione con filtro = 'NESSUNA DIVISIONE' ------
		String tipoStampa=(String)par.get("terr");
		StringTokenizer st = new StringTokenizer(tipoStampa,"|");

		String sZona=st.nextToken();
		String sDis=st.nextToken();
		String sCom=st.nextToken();
		String sTipo=st.nextToken();
		String sEta=st.nextToken();
		String sAss=st.nextToken();
		String motivo = ISASUtil.getValoreStringa(par, "motivo");
		String descrMotivo = "";
			
		if(sZona.equals("1"))
			sZona=" u.cod_zona zona_cod, u.des_zona zona_desc," ;
		else  
			sZona= " 'NESSUNA DIVISIONE' zona_cod, 'NESSUNA DIVISIONE' zona_desc,";

		if(sDis.equals("1"))
			sDis= " u.cod_distretto distr_cod, u.des_distretto distr_desc,";
		else  
			sDis= " 'NESSUNA DIVISIONE' distr_cod, 'NESSUNA DIVISIONE' distr_desc,";
		
		if(sCom.equals("1"))
			sCom= " u.codice pca_cod, u.descrizione pca_desc,";
		else  
			sCom= " 'NESSUNA DIVISIONE' pca_cod, 'NESSUNA DIVISIONE' pca_desc,";
			
		if(motivo.equals("NESDIV"))
			descrMotivo= "'" + CONSTANTS_NESSUNA_DIVISIONE + "' ski_motivo, ";	
		else  
			descrMotivo= " ski_motivo, ";
		
		
		if(sTipo.equals("1")) {
			if (inPiemonte)
				sTipo = " co.ski_tipocura alias_fld_tipo,";
			else
				sTipo = " co.ski_tipout alias_fld_tipo,";
		} else 
			sTipo= " 'NESSUNA DIVISIONE' alias_fld_tipo,";		
		// 17/11/11: per estrazione con filtro = 'NESSUNA DIVISIONE' ------

     String s="SELECT DISTINCT c.n_cartella, c.sesso, c.data_nasc," +
              " nvl(trim(c.cognome),'') ||' ' ||nvl(trim(c.nome),'') assistito," +
			  sZona + sDis + sCom +  descrMotivo +             
              " co.n_contatto, co.ski_data_apertura, co.ski_data_uscita," +
              " co.ski_dimissioni," +
			  sTipo +
			  " c.data_chiusura," +
              " a.data_variazione" +
			" FROM cartella c,skinf co"+
              " ,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" u,anagra_c a "+
              " WHERE co.n_cartella=c.n_cartella AND " +
			" c.n_cartella=a.n_cartella"+ clausola_date;

              String ragg = (String)par.get("ragg");
              s = su.addWhere(s, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

              s = su.addWhere(s, su.REL_AND, "u.cod_zona",
                      su.OP_EQ_STR, (String)par.get("zona"));
              s = su.addWhere(s, su.REL_AND, "u.cod_distretto",
                      su.OP_EQ_STR, (String)par.get("distretto"));
              s = su.addWhere(s, su.REL_AND, "u.codice",
                      su.OP_EQ_STR, (String)par.get("pca"));

//              if (ragg.equals("C"))
//                  s +=" AND u.codice=a.dom_citta";
//                else if (ragg.equals("A"))
//                  s +=  " AND u.codice=a.dom_areadis";

              //Aggiunto Controllo Domicilio/Residenza (BYSP)
              if((String)par.get("dom_res") == null)
              {
                  if (ragg.equals("C"))
                      s +=" AND u.codice=a.dom_citta";
                    else if (ragg.equals("A"))
                      s +=  " AND u.codice=a.dom_areadis";
                    else if (ragg.equals("P"))
                    	s += " AND u.codice=co.ski_cod_presidio";
              }
              else if (((String)par.get("dom_res")).equals("D"))
                                        {
                                         if (ragg.equals("C"))
                        s += " AND u.codice=a.dom_citta";
                                          else if (ragg.equals("A"))
                        s += " AND u.codice=a.dom_areadis";
                                          else if (ragg.equals("P"))
                                          	s += " AND u.codice=co.ski_cod_presidio";
                                        }

              else if (((String)par.get("dom_res")).equals("R"))
                              {
                              if (ragg.equals("C"))
                        s += " AND u.codice=a.citta";
                      else if (ragg.equals("A"))
                        s += " AND u.codice=a.areadis";
                      else if (ragg.equals("P"))
                      	s += " AND u.codice=co.ski_cod_presidio";        
                              }

            //controllo et�
            String eta = (String)par.get("eta");
           // if (!eta.equals(""))
                  s=s+whereEta(eta,dbc,data_fine);
            //controllo la tipologia utente
			if (inPiemonte)
			    s = su.addWhere(s, su.REL_AND, "co.ski_tipocura",
                      su.OP_EQ_STR, (String)par.get("ute"));
			else 
				s = su.addWhere(s, su.REL_AND, "co.ski_tipout",
                      su.OP_EQ_STR, (String)par.get("ute"));
			
			if (ISASUtil.valida(motivo)&& (!motivo.equals("NESDIV"))){
				s = su.addWhere(s,su.REL_AND, "co.ski_motivo", su.OP_EQ_STR, motivo);
			}

		// 17/11/11
		s += " ORDER BY zona_desc, zona_cod, distr_desc, distr_cod, pca_desc, pca_cod, ski_motivo, "
				+ " assistito, c.n_cartella, co.n_contatto";
      debugMessage("FoTipoUteDimisEJB.mkSelectTot(): "+s);
       return s;
}


private String DecodMese(String iMese)
{
    String mese="";
    if(iMese.equals("01"))
        mese="Gennaio";
    else if(iMese.equals("02"))
        mese="Febbraio";
    else if(iMese.equals("03"))
        mese="Marzo";
    else if(iMese.equals("04"))
        mese="Aprile";
    else if(iMese.equals("05"))
        mese="Maggio";
    else if(iMese.equals("06"))
        mese="Giugno";
    else if(iMese.equals("07"))
        mese="Luglio";
    else if(iMese.equals("08"))
        mese="Agosto";
    else if(iMese.equals("09"))
        mese="Settembre";
    else if(iMese.equals("10"))
        mese="Ottebre";
    else if(iMese.equals("11"))
        mese="Novembre";
    else if(iMese.equals("12"))
        mese="Dicembre";
    return mese;
}

/** 17/11/11
private String mkSelectNoTerr(ISASConnection dbc, Hashtable par){
     ServerUtility su =new ServerUtility();
     String meseIni = (String)par.get("me_ini");
     String meseFine = (String)par.get("me_fine");
     String anno= (String) par.get("an");

     String data_ini="01/"+meseIni+"/"+anno;
     if(meseFine.equals("12"))
     {
          int ianno=Integer.parseInt(anno);
          ianno++;
          anno=""+ianno;
// G.Brogi
          meseFine = "01";
     }else {// G.Brogi
        int Mesef = Integer.parseInt(meseFine);
        if(Mesef<12) Mesef = Mesef+1;
        if(Mesef<10)
                meseFine = "0"+Mesef;
        else
                meseFine = ""+Mesef;

     }

     String data_fine="01/"+meseFine+"/"+anno;
	 String clausola_date = " AND (co.ski_data_uscita is null OR"+
              " co.ski_data_uscita>="+formatDate(dbc,data_ini)+" )";
               
	if (inPiemonte)
	clausola_date = " AND (co.ski_data_apertura >="+formatDate(dbc,data_ini);
             
						
						
     String s="SELECT DISTINCT c.n_cartella,c.sesso,c.data_nasc,"+
              " nvl(trim(c.cognome),'') ||' ' ||nvl(trim(c.nome),'') assistito,"+
              " co.n_contatto ,co.ski_data_apertura,co.ski_data_uscita,"+
              " co.ski_dimissioni, co.ski_tipout, c.data_chiusura,"+
              " FROM cartella c,skinf co"+
              " WHERE co.n_cartella=c.n_cartella " +
 	      " AND co.ski_data_apertura<"+formatDate(dbc,data_fine)
	      + clausola_date;
                  
                   //controllo et�
                  String eta = (String)par.get("eta");
                 // if (!eta.equals(""))
                        s=s+whereEta(eta,dbc,data_fine);
						
						//controllo la tipologia utente
				  if (inPiemonte)
			    s = su.addWhere(s, su.REL_AND, "co.ski_tipocura",
                      su.OP_EQ_STR, (String)par.get("ute"));
			else s = su.addWhere(s, su.REL_AND, "co.ski_tipout",
                      su.OP_EQ_STR, (String)par.get("ute"));
				  
				 
            debugMessage("FoTipoUteDimisEJB.mkSelectNoTerr(): "+s);
       return s;
}
**/

public static String getjdbcDate()
{
        java.util.Date d=new java.util.Date();
        java.text.SimpleDateFormat local_dateFormat =
        new java.text.SimpleDateFormat("yyyy-MM-dd");
        return local_dateFormat.format(d);
}


private String whereEta(String eta,ISASConnection dbc,String dataFine)
{
        String selWhere="";
        java.util.GregorianCalendar DefGreg = new java.util.GregorianCalendar();
/*Non devo considerare la data del sistema perch� altrimenti, se lancio la stampa
in giorni differenti, per uno stesso periodo posso avere conteggi differenti .
Si considera come data di riferimento la data di fine periodo
 */
        dataFine=dataFine.substring(0,2)+ dataFine.substring(3,5)+dataFine.substring(6,10);
        DataWI dataWI=new DataWI(dataFine);
        DefGreg.setTime(dataWI.getSqlDate());
        if (eta.equals("1"))
          {//da 0 a 64
              java.sql.Date df = new java.sql.Date((DefGreg.getTime()).getTime());
              selWhere =selWhere + " AND c.data_nasc<="+formatDate(dbc,""+df);
              DefGreg = new java.util.GregorianCalendar();
              DefGreg.setTime(dataWI.getSqlDate());
              DefGreg.add(java.util.Calendar.YEAR, -65);
              df = new java.sql.Date((DefGreg.getTime()).getTime());
              selWhere =selWhere + " AND c.data_nasc> "+formatDate(dbc,""+df);
        }
        else if (eta.equals("2"))
          {//da 65 a 74
              DefGreg.add(java.util.Calendar.YEAR, -75);
              java.sql.Date df = new java.sql.Date((DefGreg.getTime()).getTime());
              selWhere =selWhere + " AND c.data_nasc> "+formatDate(dbc,""+df);
              DefGreg = new java.util.GregorianCalendar();
              DefGreg.setTime(dataWI.getSqlDate());
              DefGreg.add(java.util.Calendar.YEAR, -65);
              df = new java.sql.Date((DefGreg.getTime()).getTime());
              selWhere =selWhere + " AND c.data_nasc<="+formatDate(dbc,""+df);
        }
        else if (eta.equals("3"))
        {//oltre 75
            DefGreg.add(java.util.Calendar.YEAR, -75);
            java.sql.Date df = new java.sql.Date((DefGreg.getTime()).getTime());
            selWhere =selWhere + " AND c.data_nasc<= "+formatDate(dbc,""+df);
        }
        else
        {/*senon hannoscelto l'eta devo comunque filtrare per datanasc < fineperiodo
          altrimenti andrei a considerare anche quelli che in quel periodo non erano
          ancora nati*/
              java.sql.Date df = new java.sql.Date((DefGreg.getTime()).getTime());
              selWhere =selWhere + " AND c.data_nasc<="+formatDate(dbc,""+df);
        }
           System.out.println("FINE ");
        return selWhere;
}


public byte[] query_utedim(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {
	String punto = MIONOME + "query_utedim ";

	ISASConnection dbc = null;
	boolean done = false;
	debugMessage("FoTipoUteDimisEJB.query_utedim(): inizio...");

	stampa(punto + "\n dati che ricevo>"+par+"< \n");
	try {
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}
		myLogin lg = new myLogin();
		lg.put(utente,passwd);
		dbc = super.logIn(lg);

		NDOContainer cnt = new NDOContainer();

		NDOUtil unt = new NDOUtil ();

		preparaLayout(dbc,par,cnt);
                stampa(punto + " FoTipoUteDimisEJB query INIZIO"+currentTime());
		ISASCursor dbcur = dbc.startCursor(mkDefinisciSelect(dbc,par));
		stampa(punto + "FoTipoUteDimisEJB query FINE"+currentTime() 
				+ "\nFoTipoUteDimisEJB RECORD TROVATI-->"+dbcur.getDimension() );
		if (dbcur.getDimension() <= 0) {
			debugMessage("FoTipoUteDimisEJB.query_utedim(): vuoto");
			cnt.setSubTitle("NESSUNA INFORMAZIONE REPERITA");
		} else {
			preparaBody(dbcur,cnt,unt,par,dbc);
		}
                System.out.println("FoTipoUteDimisEJB  FINE ELABORAZIONE "+currentTime());
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return StampaNDO(cnt,unt,par);
	} catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.query_utedim(): "+e);
		throw new SQLException("FoTipoUteDimisEJB.query_utedim(): "+e);
	} finally {
		debugMessage("FoTipoUteDimisEJB.query_utedim(): ...fine.");
		if (!done) {
			try {
				dbc.close();
				super.close(dbc);
			} catch(Exception e1) {
				System.out.println("FoTipoUteDimisEJB.query_utedim(): "+e1);
			}
		}
	}
}

private void stampa(String messaggio) {
	System.out.println(messaggio);
}
private byte[] StampaNDO(NDOContainer cnt,NDOUtil unt, Hashtable par)
throws Exception {

	try {

                //setto i titoli di colonne fissi
                //inseriscole descrizioni in modo tale che la sort me le ordini come voglio io
                //nella stampaNDo poi do la descrizione vera

                //prima parte-->sesso
                cnt.setColTitle(unt.mkPar("Sesso"),"AAAAAA");
                cnt.setColTitle(unt.mkPar("Sesso","TOT"),"ZZZZZZ");
                //seconda parte accessi
                cnt.setColTitle(unt.mkPar("AC"),"BBBBBB");
                //terza parte giornate
                cnt.setColTitle(unt.mkPar("NUM"),"CCCCCC");
                //terza contatti
                cnt.setColTitle(unt.mkPar("CONT"),"FFFFFFFFF");
                //quarta parte
                cnt.setColTitle(unt.mkPar("DIM"),"MMMMMMM");
                cnt.setColTitle(unt.mkPar("DIM","NON DEFINITO"),"ZZZZZU");
                cnt.setColTitle(unt.mkPar("DIM","TOT"),"ZZZZZZ");
				// 17/11/11: quinta parte
				cnt.setColTitle(unt.mkPar("ATT"),"NNNNNNNN");
				
                //li ordino
                cnt.colSort();
				cnt.rowSort();
				
                //metto le vere descrizioni
// 17/11/11			cnt.setColTitle(unt.mkPar("Sesso"),"SESSO");
				cnt.setColTitle(unt.mkPar("Sesso"),"ASSISTITI");
                cnt.setColTitle(unt.mkPar("Sesso","M"),"Maschi");
                cnt.setColTitle(unt.mkPar("Sesso","F"),"Femmine");
                cnt.setColTitle(unt.mkPar("Sesso","TOT"),"Totale");
                cnt.setColTitle(unt.mkPar("AC"),"ACCESSI");
                cnt.setColTitle(unt.mkPar("AC","TOT"),"Totale");
                cnt.setColTitle(unt.mkPar("NUM"),"NUMERO GIORNATE IN ASSISTENZA");
                cnt.setColTitle(unt.mkPar("NUM","TOT"),"Totale");
                cnt.setColTitle(unt.mkPar("CONT"),"CONTATTI ATTIVI");
                cnt.setColTitle(unt.mkPar("CONT","TOT"),"Totale   (1)");
                cnt.setColTitle(unt.mkPar("DIM"),"MOTIVO DIMISSIONI");
                cnt.setColTitle(unt.mkPar("DIM","NON DEFINITO"),"NON DEFINITO");
                cnt.setColTitle(unt.mkPar("DIM","TOT"),"Totale   (2)");
				// 17/11/11
				cnt.setColTitle(unt.mkPar("ATT"),"CONTATTI APERTI");
                cnt.setColTitle(unt.mkPar("ATT","TOT"),"Totale (1+2)");

				NDOPrinter prt = new NDOPrinter();
                String tipoStampa=(String)par.get("terr");
                StringTokenizer st = new StringTokenizer(tipoStampa,"|");
                stampa("tipoStampa>"+tipoStampa);
                String sZona=st.nextToken();
                String sDis=st.nextToken();
                String sCom=st.nextToken();
                String sTipo=st.nextToken();
                String sEta=st.nextToken();
                String motivo = ISASUtil.getValoreStringa(par, "motivo");
                String sAss=st.nextToken();
                
                int iZona=Integer.parseInt(sZona);
                int iDis=Integer.parseInt(sDis);
                int iCom=Integer.parseInt(sCom);
                int iTipo=Integer.parseInt(sTipo);
                int iEta=Integer.parseInt(sEta);
//                int imotivo=(ISASUtil.valida(motivo)? 1 : 0);
                int imotivo = 1 ;
                if (ISASUtil.valida(motivo) && (motivo.equals("NESDIV") )){
                	imotivo= 0;
                }
                int iAss=Integer.parseInt(sAss);

                int iLivello=(iZona+iDis+iCom+iTipo+iEta+imotivo+iAss)-1;
                if(iLivello>=0)
        	        prt.addContainer(cnt,true,false,iLivello,1,true,false);
                else
                    prt.addContainer(cnt,true,false,0,1,false,false);

                String formato=(String)par.get("formato");
                return prt.getDocument(Integer.parseInt(formato));
	} catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.StampaNDO(): "+e);
		throw new SQLException("Errore eseguendo StampaNDO()");
	}
}



public String currentTime() {
  Calendar cal = Calendar.getInstance(TimeZone.getDefault());
  java.text.SimpleDateFormat sdf =
          new java.text.SimpleDateFormat(" HH:mm:ss");
  sdf.setTimeZone(TimeZone.getDefault());
  return sdf.format(cal.getTime());
}


//ANTEPRIMA INIZIO
private void  ElaboraDatiVuoto(NDOContainer cnt,NDOUtil unt,Hashtable par)
throws Exception {
	try {
          String tipoStampa=(String)par.get("terr");
          StringTokenizer st = new StringTokenizer(tipoStampa,"|");
          /*se trovo 0 vuol dire Nessuna divisione-->non devo stampare il livello
          se trovo 1 vuol dire che lo devo stampare
          prima posizione � la zona
          seconda -->il distretto
          terza --> il comune/Areadis
          quarta-->tipo utenza
          quinta -->et�
          sesta-->assistiti
          */

          String sZona=st.nextToken();
          String sDis=st.nextToken();
          String sCom=st.nextToken();
          String sTipo=st.nextToken();
          String sEta=st.nextToken();
          String sAss=st.nextToken();
          for (int i=0;i<3;i++)
          {
              Vector vRiga=new Vector();
              Vector vDecRiga=new Vector();
              if(sZona.equals("1")){
                   vRiga.add("cod"+i);
                   vDecRiga.add("Zona "+i);
              }
              if(sDis.equals("1")){
                   vRiga.add("cod"+i+1);
                   vDecRiga.add("Distretto "+i);
              }
              if(sCom.equals("1")){
                   vRiga.add("cod"+i);
                   vDecRiga.add("comune "+i);
              }
              if(sTipo.equals("1")){
               vRiga.add("tipo"+i);
               vDecRiga.add("Tipologia utente "+i);
              }
              if(sEta.equals("1")){
                  vRiga.add("Eta"+i);
                  vDecRiga.add("Fascia eta' "+i);
              }
              if(sAss.equals("1")){
                  vRiga.add("Ass"+i);
                  vDecRiga.add("Assistito "+i);
              }
              caricaNDOVuoto(vRiga,vDecRiga,unt,cnt,par);
            }
        } catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.ElaboraDatiVuoto(): "+e);
		throw new SQLException("Errore eseguendo ElaboraDatiVuoto()");
	}
}

private void caricaNDOVuoto(Vector vCod,Vector vDesc,
    NDOUtil unt,NDOContainer cnt,Hashtable par)
throws SQLException {
        try
        {
           Vector vRiga=new Vector();
           vRiga= mkRiga(vCod,unt);
           cnt.put(vRiga,unt.mkPar("Sesso","F"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("Sesso","M"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("Sesso","TOT"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("AC","TOT"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("NUM","TOT"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("CONT","TOT"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("DIM","dim1"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("DIM","dim2"),new Integer(0));
           cnt.put(vRiga,unt.mkPar("DIM","TOT"),new Integer(0));
           settaTitoliRiga (vDesc,vCod,cnt,unt);
        } catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.caricaNDO(): "+e);
		throw new SQLException("Errore eseguendo caricaNDO()");
	}
}

public byte[] query_anteprima(String utente, String passwd, Hashtable par,
	mergeDocument eve) throws SQLException {

	debugMessage("FoTipoUteDimisEJB.query_anteprima(): inizio...");

	try {
		this.dom_res=(String)par.get("dom_res");
		if (this.dom_res != null)
		{
		if (this.dom_res.equals("R")) this.dr="Residenza";
		else if (this.dom_res.equals("D")) this.dr="Domicilio";
		}
		NDOContainer cnt = new NDOContainer();

		NDOUtil unt = new NDOUtil ();
                String tipoStampa=(String)par.get("terr");
                StringTokenizer st = new StringTokenizer(tipoStampa,"|");
		Vector vtitoli = new Vector();
                String sZona=st.nextToken();
                String sDis=st.nextToken();
                String sCom=st.nextToken();
                String sTipo=st.nextToken();
                String sEta=st.nextToken();
                String sAss=st.nextToken();

                String subTitolo="ANTEPRIMA";

                if (sZona.equals("1"))
                   vtitoli.add("Zona");
                if (sDis.equals("1"))
                        vtitoli.add("Distretto");
   	        String ragg =(String)par.get("ragg");
                String tipopca="";
//                if (ragg.equals("A")) {
//                    tipopca = " Area distrettuale ";
//                } else if (ragg.equals("C")) {
//                        tipopca = " Comune ";
//                } else if (ragg.equals("P"))
//                        tipopca = " Presidio ";
                
                if(this.dom_res==null)
                {
                	if (ragg.equals("A")) 
                        tipopca = " Area distrettuale ";
                     else if (ragg.equals("C")) 
                            tipopca = " Comune ";
                     else if (ragg.equals("P"))
                            tipopca = " Presidio ";

                }else if (this.dom_res.equals("D"))
                {
                	if (ragg.equals("A")) 
                        tipopca = " Area distrettuale di Domicilio ";
                     else if (ragg.equals("C")) 
                            tipopca = " Comune di Domicilio ";
                }else if (this.dom_res.equals("R"))
                    {
                    	if (ragg.equals("A")) 
                            tipopca = " Area distrettuale di Residenza ";
                         else if (ragg.equals("C")) 
                                tipopca = " Comune di Residenza ";
                    } 	        			
                
                if (sCom.equals("1"))
                     vtitoli.add(tipopca);
                if (sTipo.equals("1"))
                     vtitoli.add("Tipologia Utente");
                if (sEta.equals("1"))
                     vtitoli.add("Fasce Eta'");
                if (sAss.equals("1"))
                   vtitoli.add("Assistiti");
                cnt.setGroupTitles(vtitoli);
    	        cnt.setSubTitle(subTitolo);
                ElaboraDatiVuoto(cnt,unt, par);
		return StampaNDO(cnt,unt,par);
	} catch(Exception e) {
		debugMessage("FoTipoUteDimisEJB.query_utedim(): "+e);
		throw new SQLException("FoTipoUteDimisEJB.query_utedim(): "+e);
	} finally {
		debugMessage("FoTipoUteDimisEJB.query_utedim(): ...fine.");
	}
}
//ANTEPRIMA FINE



}	// End of FoTipoUteDimisEJB class

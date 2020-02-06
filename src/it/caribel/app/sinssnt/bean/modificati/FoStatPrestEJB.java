package it.caribel.app.sinssnt.bean.modificati;
//==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 13/09/2002 - EJB per la stampa statistica prestazioni
//
// 07/06/06 modifiche dalla vers. 06.01
// inserito il conteggio degli assistiti con accessi occasionali
// inserita la outer join con la tabella skinf/contatti nel caso di    
// infermieri/ass.sociali
// ==========================================================================

import it.pisa.caribel.util.*;
import java.util.*;

import it.pisa.caribel.sinssnt.connection.*;
import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import java.sql.*;
import java.io.*;
import it.pisa.caribel.merge.*; // fo merge
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

public class FoStatPrestEJB extends SINSSNTConnectionEJB {
	String dom_res;
	String dr;

	private static final String MIONOME = "7-FoStatPrestEJB.";

	public FoStatPrestEJB() {
	}

	public static ndo_util ndoUtil = new ndo_util();
	private static final String VALORE_ENTRAMBI = "EN";
	private static final String VALORE_ACCESSI_PRESTAZIONE = "AP";
	private static final String VALORE_ACCESSI_OCCASIONALI = "AO";

	public byte[] query_statprest(String utente, String passwd, Hashtable par, mergeDocument eve) throws SQLException {
		String punto = MIONOME + "query_statprest ";
		stampaInizio(punto, par);
		boolean done = false;
		ISASConnection dbc = null;
		try {
			
			this.dom_res=(String)par.get("dom_res");
			if (this.dom_res != null)
			{
			if (this.dom_res.equals("R")) this.dr="Residenza";
			else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			boolean entrato = false;
			String myselect = "";
			String myselect_motivo = "";
			String myselect_qualifica = "";//M.Minerba 06/03/2013
			String sel = "";
			String dataini = "";
			String datafine = "";
			String tipo = "";
			String tipoAccesso = getValoreStringa(par, "tipo_accesso");
			String conf = "";
			// CJ 13/02/2006
			String motivo = "";
			String qualifica = "";//M.Minerba 06/03/2013
			String unifun = "";
			String list = "";
			String from_uni = "";
			String from_cont = "";
			String where_uni = "";
			String where_cont = "";
			// Fine CJ
			String pca = "";

			Integer cartella = new Integer(0);
			Hashtable cartella_cont_old = new Hashtable();
			Hashtable cartella_occ_old = new Hashtable();
			Hashtable accesso_old = new Hashtable();

			// G.Brogi 07/06/06 uso una outer join per tirarmi fuori
			// anche le cartelle con accessi occasionali (senza contatti)
			// Non uso la outerjoin se scelgo un motivo
			String outJoin_1 = dbc.getoutTab();
			String outJoin_2 = dbc.getoutCrit();
			String campi = "";
			String campi_ord = "";
			// fine 07/06/06

			ServerUtility su = new ServerUtility();

			unifun = (String) par.get("unifun");
			if (!unifun.equals("") && !unifun.equals("TUTTO")) {
				from_uni = ",tabuf t";
				list = "t.descrizione,";
			}

			dataini = (String) par.get("dataini");
			datafine = (String) par.get("datafine");
			String num_cartella = (String) par.get("cartella");

			tipo = (String) par.get("tipo");
			motivo = (String) par.get("motivo");
			qualifica = (String) par.get("qualifica");
			if (tipo.equals("01")) { // assistente sociale
				if (!motivo.equals("") && !motivo.equals("TUTTO")) {

					from_cont = ",ass_progetto co "; // gb 07/06/07
					where_cont = " AND co.n_cartella=i.int_cartella" + " AND co.n_progetto = i.n_progetto" + // gb
							" AND co.ap_motivo='" + motivo + "'";
					if (uguale(tipoAccesso, VALORE_ACCESSI_PRESTAZIONE)) {
						where_cont += " AND i.n_progetto > 0 ";
					} else {
						if (uguale(tipoAccesso, VALORE_ACCESSI_OCCASIONALI)) {
							where_cont += " AND i.n_progetto = 0 ";
						}
					}
				} else if (motivo.equals("TUTTO")) {
					if (uguale(tipoAccesso, VALORE_ACCESSI_PRESTAZIONE)) {
						campi = ",nvl(''||co.n_cartella,'ZZZ') contcartella";
						campi_ord = "cont,";
						from_cont = ", ass_progetto co ";
						where_cont = " AND co.n_cartella = i.int_cartella " + " AND co.n_progetto = i.n_progetto "
								+ " AND co.n_progetto >0 "; // gb 07/06/07
					} else {
						if (uguale(tipoAccesso, VALORE_ACCESSI_OCCASIONALI)) {
							campi = ",nvl(''||co.n_cartella,'ZZZ') contcartella";
							campi_ord = "cont,";
							from_cont = "," + outJoin_1 + " ass_progetto co ";
							where_cont = " AND co.n_cartella " + outJoin_2 + "=i.int_cartella" + " AND co.n_progetto"
									+ outJoin_2 + "=i.n_progetto" + " AND i.n_progetto = 0";
						} else {
							campi = ",nvl(''||co.n_cartella,'ZZZ') contcartella";
							campi_ord = "cont,";
							from_cont = "," + outJoin_1 + " ass_progetto co "; // gb
							// 07/06/07
							where_cont = " AND co.n_cartella " + outJoin_2 + "=i.int_cartella" + " AND co.n_progetto"
									+ outJoin_2 + " = i.n_progetto ";
							// + // gb 07/06/07 " AND i.n_progetto = 0"; // gb
							// 07/06/07
						}
					}
				}
			} else {
				// sono sul sanitario
				if (tipo.equals("02")) {
					if (!motivo.equals("") && !motivo.equals("TUTTO")) {
						// e' stato scelto un motivo di dimissione: devo
						// estrarre
						// solo i dimesi nel periodo per quel motivo
						from_cont = ", skinf sk ";
						where_cont = " AND sk.n_cartella=i.int_cartella" + " AND sk.n_contatto=i.int_contatto"
								+ " AND sk.ski_motivo='" + motivo + "'";
					} else if (motivo.equals("TUTTO")) {
						campi = ",nvl(''||sk.n_cartella,'ZZZ') contcartella";
						campi_ord = "sk.n_cartella,";
						from_cont = ", " + outJoin_1 + " skinf sk ";
						where_cont = " AND sk.n_cartella " + outJoin_2 + "=i.int_cartella" + " AND sk.n_contatto "
								+ outJoin_2 + "=i.int_contatto";
					}
				}

				if (uguale(tipoAccesso, VALORE_ACCESSI_PRESTAZIONE)) {
					where_cont += " AND ( i.int_contatto <> 0 "
							+ (uguale(tipo, "00") ? " OR (int_contatto =0 and  n_progetto >0) " : "") + ")";
				} else {
					if (uguale(tipoAccesso, VALORE_ACCESSI_OCCASIONALI)) {
						where_cont += " AND i.int_contatto = 0 "
								+ (uguale(tipo, "00") ? " AND (n_progetto = 0 OR n_progetto is null) " : "");
					}
				}
			}
			
			

			String ragg = (String) par.get("ragg");
			/* Jessica 30/03/2009 */
			String zona = "";
			String distretto = "";
			String comune = "";
			String tipoStampa = (String) par.get("terr");
			StringTokenizer st = new StringTokenizer(tipoStampa, "|");
			String sZona = st.nextToken();
			String sDis = st.nextToken();
			String sCom = st.nextToken();

			if (sZona.equals("1"))
				zona = " u.cod_zona,u.des_zona, ";
			else
				zona = " 'NESSUNA DIVISIONE' cod_zona,'NESSUNA DIVISIONE' des_zona, ";

			if (sDis.equals("1"))
				distretto = " u.des_distretto,u.cod_distretto, ";
			else
				distretto = " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto, ";

			if (sCom.equals("1"))
				comune = " u.codice ,u.descrizione ";
			else
				comune = " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione ";
			/* fine Jessica 30/03/2009 */
			pca = (String) par.get("pca");
			// 30/03/2009myselect="SELECT u.des_zona, u.des_distretto, u.codice, u.descrizione,"+
			myselect = "SELECT " + zona + distretto + comune + ","
					+ "p.pre_des_prest,p.pre_numero,p.pre_cod_prest,i.int_cartella,"
					+ "p.pre_anno,p.pre_contatore,i.int_coduf,i.int_tempo," + list
					+ " u.cod_distretto int_coddistr,u.cod_zona int_codzona" + campi
					+ " FROM intpre p,interv i,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +"  u " + from_uni + from_cont
					+ " WHERE p.pre_contatore=i.int_contatore AND " + " i.int_anno=p.pre_anno ";
			myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
			//Minerba 06/03/2013			
			if (!(tipo.equals("00"))){
			if (qualifica!=null && !(qualifica.equals(""))&&!(qualifica.equals("TUTTO"))){		
				
				myselect += " AND i.int_qual_oper='"+qualifica+"'";
						
				
			}
			}//fine Minerba 06/03/2013
			
			if (num_cartella!=null && !(num_cartella.equals(""))){		
				System.out.println("CARTELLA" + num_cartella);
				myselect += " AND i.int_cartella="+num_cartella;
				
			}
			myselect_motivo = "SELECT DISTINCT i.int_cartella,u.cod_distretto int_coddistr,"
					+ "u.des_zona, u.des_distretto, u.codice, u.descrizione,"
					+
					// 05/12/2006 Vado a tirare fuori anche il codice zona,
					// unit� funzionale
					// e presidio nel caso in cui la stampa venga mandata per
					// zona
					" u.cod_zona int_codzona";
			
			System.out.println("QUALIFICA" +qualifica);
			// 31/01/11	
			if (unitaFunzDaUsare(dbc))
				myselect_motivo += ", i.int_coduf";
				
			myselect_motivo += " FROM intpre p,interv i,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +"  u " + from_uni
					+ from_cont + " WHERE p.pre_contatore=i.int_contatore AND " + " i.int_anno=p.pre_anno ";
			myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
//			if (ragg.equals("P")) {
//				myselect += " AND i.int_codpres=u.codice";
//				myselect_motivo += " AND i.int_codpres=u.codice";
//				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_codpres", su.OP_EQ_STR, pca);
//				myselect = su.addWhere(myselect, su.REL_AND, "i.int_codpres", su.OP_EQ_STR, pca);
//			} else if (ragg.equals("A")) {
//				myselect += " AND i.int_cod_areadis=u.codice";
//				myselect_motivo += " AND i.int_cod_areadis=u.codice";
//				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
//				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
//			} else if (ragg.equals("C")) {
//				myselect += " AND i.int_cod_comune=u.codice";
//				myselect_motivo += " AND i.int_cod_comune=u.codice";
//				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
//				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
//			}
			if (this.dom_res==null)
			{
				if (ragg.equals("P")) {
					myselect += " AND i.int_codpres=u.codice";
					myselect_motivo += " AND i.int_codpres=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_codpres", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_codpres", su.OP_EQ_STR, pca);
				} else if (ragg.equals("A")) {
					myselect += " AND i.int_cod_areadis=u.codice";
					myselect_motivo += " AND i.int_cod_areadis=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND i.int_cod_comune=u.codice";
					myselect_motivo += " AND i.int_cod_comune=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
				}
			}else if (this.dom_res.equals("D"))
			{
				 if (ragg.equals("A")) {
					myselect += " AND i.int_cod_areadis=u.codice";
					myselect_motivo += " AND i.int_cod_areadis=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND i.int_cod_comune=u.codice";
					myselect_motivo += " AND i.int_cod_comune=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
				}
			}else if (this.dom_res.equals("R"))
			{
				if (ragg.equals("A")) {
					myselect += " AND i.int_cod_areadis=u.codice";
					myselect_motivo += " AND i.int_cod_areadis=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
				} else if (ragg.equals("C")) {
					myselect += " AND i.int_cod_comune=u.codice";
					myselect_motivo += " AND i.int_cod_comune=u.codice";
					myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
					myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
				}
			}
			sel = su.addWhere(sel, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			sel = su.addWhere(sel, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
			sel = su.addWhere(sel, su.REL_AND, "i.int_data_prest", su.OP_GE_NUM, formatDate(dbc, dataini));
			sel = su.addWhere(sel, su.REL_AND, "i.int_data_prest", su.OP_LE_NUM, formatDate(dbc, datafine));

			if (!tipo.equals("00"))
				sel = su.addWhere(sel, su.REL_AND, "i.int_tipo_oper", su.OP_EQ_STR, tipo);

			// 09/01/2004 Jessica inserito filtro flag tipo accertamento:
			// domiciliare o ambulatoriale o entrambi(nessun filtro)
			String tipo_prest = "";
			if (!par.get("tipo_prest").equals("E")) {
				tipo_prest = (String) par.get("tipo_prest");
				sel = su.addWhere(sel, su.REL_AND, "i.int_ambdom", su.OP_EQ_STR, tipo_prest);
			}

			if (!unifun.equals("") && !unifun.equals("TUTTO")) {
				sel += " AND i.int_coduf=t.codice ";
				if (!unifun.equals("TUTTO"))
					sel += " AND t.codice='" + unifun + "'";
			}

			myselect += " AND " + sel + where_cont;
			myselect_motivo += " AND " + sel + where_cont;

			myselect += " ORDER BY u.des_zona, u.des_distretto, u.descrizione ,int_codzona, int_coddistr, u.codice,p.pre_des_prest";

			stampaQuery(punto, myselect);

			ISASCursor dbcur = dbc.startCursor(myselect);

			// Inserisco la decodifica del tipo prestazione
			if (tipo_prest.equals("D"))
				tipo_prest = "DOMICILIARI";
			else if (tipo_prest.equals("A"))
				tipo_prest = "AMBULATORIALI";
			// istanzio il container
			ndo_container ndoCnt = new ndo_container();
			ndo_container ndoCntTot1 = new ndo_container();
			ndo_container ndoCntTot = new ndo_container();
			ndo_container ndoCntTotOre = new ndo_container();

			int i = 0;
			// J22/03/04
			String tit_stampa = "";
			/*
			 * if (((String)par.get("livello")).equals("2U"))
			 * tit_stampa="DELL'UNITA' FUNZIONALE"; if
			 * (((String)par.get("livello")).equals("2Z"))
			 * tit_stampa="DELLA ZONA"; else if
			 * (((String)par.get("livello")).equals("2P"))
			 * tit_stampa="DEL PRESIDIO"; else if
			 * (((String)par.get("livello")).equals("2"))
			 * tit_stampa="DEL DISTRETTO";
			 */
			// inserisco nel container il titolo iniziale e il footer
			dataini = dataini.substring(8, 10) + "/" + dataini.substring(5, 7) + "/" + dataini.substring(0, 4);
			datafine = datafine.substring(8, 10) + "/" + datafine.substring(5, 7) + "/" + datafine.substring(0, 4);
			ndoCntTot.putHeadTitle("RIEPILOGO ASSISTITI SEGUITI DAGLI OPERATORI " + tit_stampa);
			ndoCntTot1.putHeadTitle("RIEPILOGO ACCESSI");
			ndoCntTotOre.putHeadTitle("RIEPILOGO ORE");
			if (tipo.equals("")) {
				ndoCnt.putHeadTitle("PRESTAZIONI " + tipo_prest + " EFFETTUATE " 
						+ getDescrizioneTipoAccesso(tipoAccesso) + " dal " + dataini + " al " + datafine);
				// stampa(punto + "\n tipo vale>" + tipo + "<\ntitolo\n>" +
				// "PRESTAZIONI " + tipo_prest
				// + " EFFETTUATE DA " + tipo +
				// getDescrizioneTipoAccesso(tipoAccesso) + " dal " + dataini +
				// " al "
				// + datafine + "<\n");
			} else {
				if (tipo.equals("01"))
					tipo = "ASSISTENTI SOCIALI";
				else if (tipo.equals("02"))
					tipo = "INFERMIERI";
				else if (tipo.equals("03"))
					tipo = "MEDICI";
				else if (tipo.equals("04"))
					tipo = "FISIOTERAPISTI";
				else if (tipo.equals("52"))
					tipo = "ONCOLOGI";
				else if (tipo.equals("98"))
					tipo = "MEDICI SPECIALISTI";
				else if (tipo.equals("00"))
					tipo = " TUTTI I TIPI DI OPERATORI ";
				
				if (qualifica.equals(""))
					qualifica = " TUTTE LE QUALIFICHE ";
				else if (qualifica.equals("F"))
					qualifica = "LOGOPEDISTA ";
				else if (qualifica.equals("H"))
					qualifica = "PSICOLOGO ";
				else if (qualifica.equals("1"))
					qualifica = "ASSISTENTE SOCIALE ";
				else if (qualifica.equals("2"))
					qualifica = "INFERMIERE ";
				else if (qualifica.equals("A"))
					qualifica = "OSA ";
				else if (qualifica.equals("O"))
					qualifica = "OTA ";
				else if (qualifica.equals("4"))
					qualifica = "FISIATRA ";
				else if (qualifica.equals("3"))
					qualifica = "FISIOTERAPISTA ";
				else if (qualifica.equals("5"))
					qualifica = "MEDICO ";
				else if (qualifica.equals("6"))
					qualifica = "AMMINISTRATIVO 1.LIV ";
				else if (qualifica.equals("7"))
					qualifica = "AMMINISTRATIVO 2.LIV ";
				else if (qualifica.equals("8"))
					qualifica = "CAPO SALA ";
				else if (qualifica.equals("9"))
					qualifica = "O.S.S. ";
				else if (qualifica.equals("G"))
					qualifica = "MED. MEDICINA GENER. ";
				
				ndoCnt.putHeadTitle("PRESTAZIONI " + tipo_prest + " EFFETTUATE DA " + tipo + " QUALIFICA: " + qualifica
						+ getDescrizioneTipoAccesso(tipoAccesso) + " dal " + dataini + " al " + datafine);
			}
			String selconf = "SELECT conf_txt from conf " + "WHERE conf_kproc='SINS' AND"
					+ " conf_key='ragione_sociale'";
			ISASRecord dbconf = dbc.readRecord(selconf);
			if (dbconf != null)
				if (dbconf.get("conf_txt") != null && !((String) dbconf.get("conf_txt")).equals("")) {
					conf = (String) dbconf.get("conf_txt");
					ndoCnt.putFootTitle(conf);
				} else
					ndoCnt.putFootTitle(" ");
			else
				ndoCnt.putFootTitle(" ");

			String l = "";
			String stampa = "";
			int liv = 0;
			while (dbcur.next()) {
				entrato = true;
				i++;
				ISASRecord dbr = dbcur.getRecord();
				String col1 = "";
				String col2 = "";
				String col3 = "";
				String col4 = "";
				String cod_riga = "";
				Integer num_prest;

				if (dbr.get("int_cartella") != null)
					cartella = (Integer) dbr.get("int_cartella");
				// G.Brogi 19/0/07: introdotto il pannello del territorio; il
				// livello
				// e' sempre 3 o 4 (se c'e' l'unita' funz.)!
				if (sCom.equals("1"))
					l = "4";
				else {
					if (sDis.equals("1"))
						l = "3";
					else if (sZona.equals("1"))
						l = "2";
					else
						l = "0";// caso di NESSUNA DIVISIONE ovunque
				}

				if (!(unifun == null || unifun.equals(""))) {
					// 30/03/2009 incremento il valore del livello perch� non �
					// pi� stabile
					int i_l = Integer.parseInt(l);
					i_l++;
					l = "" + i_l;
					// l="5";
				} else {
					// lo lascio invariato
					// l = "4";
				}

				if (sZona.equals("1")) {
					if (dbr.get("int_codzona") != null && !((String) dbr.get("int_codzona")).equals(""))
						col1 = (String) dbr.get("int_codzona");
					else
						col1 = "zz1";
				}
				if (sDis.equals("1")) {
					if (dbr.get("int_coddistr") != null && !((String) dbr.get("int_coddistr")).equals(""))
						col2 = (String) dbr.get("int_coddistr");
					else
						col2 = "xx1";
				}
				if (sCom.equals("1")) {
					if (dbr.get("codice") != null && !((String) dbr.get("codice")).equals(""))
						col3 = (String) dbr.get("codice");
					else
						col3 = "yy1";
				}
				if (dbr.get("int_coduf") != null && !((String) dbr.get("int_coduf")).equals(""))
					col4 = (String) dbr.get("int_coduf");
				else
					col4 = "ww1";

				String colonna = "";
				/* Jessica 30/03/2009 */
				if (!col1.equals("")) {
					colonna += col1;
					if (!col2.equals("")) {
						colonna += "|" + col2;
						if (!col3.equals(""))
							colonna += "|" + col3;
					}
				} else
					// caso in cui sia tutto NESSUNA DIVISIONE
					colonna = "N";
				System.out.println("colonna:" + colonna);
				if (!(unifun == null || unifun.equals(""))) {
					colonna += "|" + col4;
				}

				if (!("" + dbr.get("contcartella")).equals("ZZZ"))
					cartella_cont_old.put(colonna + "|" + cartella, "1");
				else
					cartella_occ_old.put(colonna + "|" + cartella, "1");

				String pre_con = "" + dbr.get("pre_contatore");
				String pre_anno = "" + dbr.get("pre_anno");
				if (!accesso_old.containsKey(pre_con + "|" + pre_anno)) {
					if (!col1.equals("")) {
						if (!col2.equals("")) {
							if (!col3.equals("")) {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2, col3, col4), new Integer(1));
								} else {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2, col3), new Integer(1));
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2, col4), new Integer(1));
								} else {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2), new Integer(1));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col4), new Integer(1));
							} else {
								ndoCntTot1.put(ndoUtil.mkPar("ACC", col1), new Integer(1));
							}
						}
					} else {
						if (!(unifun == null || unifun.equals(""))) {
							ndoCntTot1.put(ndoUtil.mkPar("ACC", col4), new Integer(1));
						} else {
							System.out.println("ENTRO QUIIIIII");
							ndoCntTot1.put(ndoUtil.mkPar("ACC", "N"), new Integer(1));
						}
					}
					//System.out.println("Esco????");
					if (dbr.get("int_tempo") != null) {
						// calcolo le ore
						Integer tminuti = (Integer) dbr.get("int_tempo");
						int minut = tminuti.intValue();
						int ore = minut / 60;

						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3, col4),
												new Integer(ore));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3), new Integer(ore));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col4), new Integer(ore));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2), new Integer(ore));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col4), new Integer(ore));
								} else {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1), new Integer(ore));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", col4), new Integer(ore));
							} else {
								System.out.println("ENTRO QUIIIIII ore");
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", "N"), new Integer(ore));
							}
						}
					} else {
						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3, col4), new Integer(0));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3), new Integer(0));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col4), new Integer(0));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2), new Integer(0));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col4), new Integer(0));
								} else {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1), new Integer(0));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", col4), new Integer(0));
							} else {
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", "N"), new Integer(0));
							}
						}
					}
					accesso_old.put(pre_con + "|" + pre_anno, "1");
				}

				// inserisco le righe delle prestazioni
				if (dbr.get("pre_cod_prest") != null && !((String) dbr.get("pre_cod_prest")).equals("")) {
					cod_riga = (String) dbr.get("pre_cod_prest");
				} else
					cod_riga = "rr1";

				if (dbr.get("pre_numero") != null)
					num_prest = (Integer) dbr.get("pre_numero");
				else
					num_prest = new Integer(0);
				// System.out.println("***col1:"+col1+" col2:"+col2);

				/*
				 * if(!(unifun==null || unifun.equals(""))){
				 * ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2,col3,col4),
				 * num_prest); }else{ ndoCnt.put(ndoUtil.mkPar(cod_riga, col1,
				 * col2,col3), num_prest); }
				 */

				if (!col1.equals("")) {
					if (!col2.equals("")) {
						if (!col3.equals("")) {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2, col3, col4), num_prest);
							} else {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2, col3), num_prest);
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2, col4), num_prest);
							} else {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2), num_prest);
							}
						}
					} else {
						if (!(unifun == null || unifun.equals(""))) {
							ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col4), num_prest);
						} else {
							ndoCnt.put(ndoUtil.mkPar(cod_riga, col1), num_prest);
						}
					}
				} else {
					if (!(unifun == null || unifun.equals(""))) {
						ndoCnt.put(ndoUtil.mkPar(cod_riga, col4), num_prest);
					} else {
						System.out.println("ENTRO QUI numero prestazioni");
						ndoCnt.put(ndoUtil.mkPar(cod_riga, "N"), num_prest);
					}
				}

			}// end while
			if (entrato) {
				if (motivo.equals("TUTTO")) {

					// scorro prima i soggetti con accessi occasionali - senza
					// contatto
					// Se hanno anche un contatto, vanno conteggiati nella riga
					// degli
					// assistiti con contatto
                                        System.out.println("FoStatPrestEJB.query_statprest(): cartella_occ_old=" + cartella_occ_old.size());
                                        int conta_occ = cartella_occ_old.size();
					Enumeration kocc = orderedKeys(cartella_occ_old);
					// booleano primavolta serve ad inserire sempre per prima la
					// riga degli assistiti con un contatto
					boolean primavolta = true;
					while (kocc.hasMoreElements()) {
                                                if (((--conta_occ)%100) == 0) {
                                                        System.out.println("FoStatPrestEJB.query_statprest(): conta_occ=" + conta_occ);
                                                        Thread.sleep(20);
                                                }
						String e = (String) kocc.nextElement();
						StringTokenizer tk = new StringTokenizer(e, "|");
						String col1 = "";
						String col2 = "";
						String col3 = "";
						String col4 = "";
						if (sZona.equals("1")) {
							col1 = tk.nextToken();
							if (sDis.equals("1")) {
								col2 = tk.nextToken();
								if (sCom.equals("1"))
									col3 = tk.nextToken();
							}
						}
						if (!(unifun == null || unifun.equals(""))) {
							col4 = tk.nextToken();
						}

						if (cartella_cont_old.containsKey(e)) {
							// System.out.println("***Ass con contatto");
							// caso assistito con contatto

							if (!col1.equals("")) {
								if (!col2.equals("")) {
									if (!col3.equals("")) {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4), new Integer(1));
										} else {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(1));
										}
									} else {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(1));
										} else {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(1));
										}
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCnt.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(1));
									} else {
										ndoCnt.put(ndoUtil.mkPar("ASS", col1), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCnt.put(ndoUtil.mkPar("ASS", col4), new Integer(1));
								} else {
									ndoCnt.put(ndoUtil.mkPar("ASS", "N"), new Integer(1));
								}
							}

							// se lo conteggio ora non lo devo conteggiare dopo,
							// quando
							// scorrero' cartella_cont_old => lo rimuovo da li'
							cartella_cont_old.remove(e);
						} else {
							// caso assistito senza contatto
							// ma con accesso occasionale
							if (primavolta) {
								if (!col1.equals("")) {
									if (!col2.equals("")) {
										if (!col3.equals("")) {
											if (!(unifun == null || unifun.equals(""))) {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4),
														new Integer(0));
											} else {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(0));
											}
										} else {
											if (!(unifun == null || unifun.equals(""))) {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(0));
											} else {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(0));
											}
										}
									} else {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(0));
										} else {
											ndoCntTot.put(ndoUtil.mkPar("ASS", col1), new Integer(0));
										}
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col4), new Integer(0));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", "N"), new Integer(0));
									}
								}
								/*
								 * 31/03/09if(!(unifun==null ||
								 * unifun.equals(""))){
								 * ndoCntTot.put(ndoUtil.mkPar("ASS",
								 * col1,col2,col3,col4), new Integer(0)); }else{
								 * ndoCntTot.put(ndoUtil.mkPar("ASS",
								 * col1,col2,col3), new Integer(0)); }
								 */
								primavolta = false;
							}
							if (!col1.equals("")) {
								if (!col2.equals("")) {
									if (!col3.equals("")) {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2, col3, col4), new Integer(
													1));
										} else {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2, col3), new Integer(1));
										}
									} else {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2, col4), new Integer(1));
										} else {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2), new Integer(1));
										}
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col4), new Integer(1));
								} else {
									ndoCntTot.put(ndoUtil.mkPar("ASSOCC", "N"), new Integer(1));
								}
							}
							/*
							 * 31/03/09if(!(unifun==null || unifun.equals(""))){
							 * ndoCntTot.put(ndoUtil.mkPar("ASSOCC",
							 * col1,col3,col4), new Integer(1)); }else{
							 * ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1,col3),
							 * new Integer(1)); }
							 */
						}
					}// end while kocc

					// scorrimento hashtable assistiti con contatto (e' gia'
					// stata
					// ripulita da quelli conteggiati nel while precedente)
                                        System.out.println("FoStatPrestEJB.query_statprest(): cartella_cont_old=" + cartella_cont_old.size());
                                        int conta_cont = cartella_cont_old.size();

					Enumeration kcont = orderedKeys(cartella_cont_old);
					while (kcont.hasMoreElements()) {
                                                if (((--conta_cont)%100) == 0) {
                                                        System.out.println("FoStatPrestEJB.query_statprest(): conta_cont=" + conta_cont);
                                                        Thread.sleep(20);
                                                }

						String e = (String) kcont.nextElement();
						StringTokenizer tk = new StringTokenizer(e, "|");
						String col1 = "";
						String col2 = "";
						String col3 = "";
						String col4 = "";
						if (sZona.equals("1")) {
							col1 = tk.nextToken();
							if (sDis.equals("1")) {
								col2 = tk.nextToken();
								if (sCom.equals("1"))
									col3 = tk.nextToken();
							}
						}
						if (!(unifun == null || unifun.equals(""))) {
							col4 = tk.nextToken();
						}

						// caso assistito con contatto
						/*
						 * 31/03/09if(!(unifun==null || unifun.equals(""))) {
						 * ndoCntTot.put(ndoUtil.mkPar("ASS",
						 * col1,col2,col3,col4), new Integer(1)); }else{
						 * ndoCntTot.put(ndoUtil.mkPar("ASS", col1,col2,col3),
						 * new Integer(1)); }
						 */
						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(1));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(1));
								} else {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1), new Integer(1));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTot.put(ndoUtil.mkPar("ASS", col4), new Integer(1));
							} else {
								ndoCntTot.put(ndoUtil.mkPar("ASS", "N"), new Integer(1));
							}
						}
					}
				}// end if(motivo=tutto
				else {// ho scelto un motivo -> conteggio la persona come
					// assistito solo se
					if (tipo.equals("ASSISTENTI SOCIALI")) {
						// gb 07/06/07 *******
						myselect_motivo += " AND co.ap_data_apertura IN "
								+ "(SELECT MAX (t.ap_data_apertura) FROM ass_progetto t where "
								+ "t.n_cartella=co.n_cartella" + " AND t.ap_data_apertura" + "<="
								+ formatDate(dbc, datafine) + ")" + " ORDER BY u.cod_distretto";
						// gb 07/06/07: fine *******
					}/*
					 * Jessica 30/03/2009 questa modifica non andava fatta else
					 * if(tipo.equals("INFERMIERI")){ myselect_motivo
					 * +=" AND sk.ski_data_apertura IN "+
					 * "(SELECT MAX (t.ski_data_apertura) FROM skinf t where "+
					 * "t.n_cartella=sk.n_cartella"+ " AND t.ski_data_apertura"+
					 * "<="+ formatDate(dbc,datafine)+ ")"+
					 * " ORDER BY u.cod_distretto"; }
					 */
					else if (tipo.equals("INFERMIERI")) {
						myselect_motivo += " AND sk.ski_data_apertura IN "
								+ "(SELECT MAX (z.ski_data_apertura) FROM skinf z, "
								+ "intpre a,interv b,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" c " + "WHERE z.n_cartella=sk.n_cartella"
								+ " AND z.ski_data_apertura" + "<=" + formatDate(dbc, datafine);
						myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_data_prest", su.OP_GE_NUM,
								formatDate(dbc, dataini));
						myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_data_prest", su.OP_LE_NUM,
								formatDate(dbc, datafine));

						if (!((String) par.get("tipo")).equals("00"))
							myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_tipo_oper", su.OP_EQ_STR,
									(String) par.get("tipo"));
						if (!((String) par.get("tipo_prest")).equals("E"))
							myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_ambdom", su.OP_EQ_STR,
									(String) par.get("tipo_prest"));

						if (!unifun.equals("") && !unifun.equals("TUTTO")) {
							myselect_motivo += " AND b.int_coduf=t.codice ";
							if (!unifun.equals("TUTTO"))
								myselect_motivo += " AND t.codice='" + unifun + "'";
						}
						myselect_motivo += " AND z.n_cartella=b.int_cartella" + " AND z.n_contatto=b.int_contatto"
								+ " AND z.ski_motivo='" + motivo + "'" + " AND a.pre_contatore=b.int_contatore AND "
								+ " b.int_anno=a.pre_anno AND " + " c.tipo='P' AND b.int_codpres=c.codice "
								+ ") ORDER BY u.cod_distretto";
					}
					System.out.println("Select per assistiti: " + myselect_motivo);
					ISASCursor dbcurmot = dbc.startCursor(myselect_motivo);
					while (dbcurmot.next()) {
						ISASRecord dbmot = (ISASRecord) dbcurmot.getRecord();
						/*
						 * 05/12/2006 controllo per che cosa � stata lanciata la
						 * stampa e prendo il codice corrispondente
						 */
						String col1 = "";
						String col2 = "";
						String col3 = "";
						String col4 = "";

						col1 = "" + dbmot.get("int_codzona");
						col2 = "" + dbmot.get("int_coddistr");
						col3 = "" + dbmot.get("codice");
						if (dbmot.get("int_coduf") != null) {
							col4 = "" + dbmot.get("int_coduf");
						}
						/*
						 * 31/03/09if(!(unifun==null || unifun.equals(""))){
						 * ndoCntTot.put(ndoUtil.mkPar("ASS",
						 * col1,col2,col3,col4), new Integer(1)); }else{
						 * ndoCntTot.put(ndoUtil.mkPar("ASS", col1,col2,col3),
						 * new Integer(1)); }
						 */
						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(1));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(1));
								} else {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1), new Integer(1));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTot.put(ndoUtil.mkPar("ASS", col4), new Integer(1));
							} else {
								ndoCntTot.put(ndoUtil.mkPar("ASS", "N"), new Integer(1));
							}
						}
					}
				}// end else motivo=tutto
				// definisco i titoli da associare alle chiavi di colonna
				System.out.println("*****ndoCnt");
				definisciTitoliColonna(dbc, ndoCnt, ragg, unifun);
				System.out.println("*****ndoCntTot");
				definisciTitoliColonna(dbc, ndoCntTot, ragg, unifun);
				System.out.println("*****ndoCntTot1");
				definisciTitoliColonna(dbc, ndoCntTot1, ragg, unifun);
				System.out.println("*****ndoCntTotOre");
				definisciTitoliColonna(dbc, ndoCntTotOre, ragg, unifun);
				System.out.println("*****DOPO TUTTO");
				// System.out.println("Tipo:"+tipo+" motivo:"+motivo);
				// definisco i titoli di riga
				if ((tipo.equals("ASSISTENTI SOCIALI") || tipo.equals("INFERMIERI")) && motivo.equals("TUTTO")) {
					ndoCntTot.setRowDescription("ASSISTITI CON ALMENO UN CONTATTO", 0);
					ndoCntTot.setRowDescription("ASSISTITI CON ACCESSI OCCASIONALI", 1);
				} else {
					ndoCntTot.setRowDescription("ASSISTITI CON ALMENO UN CONTATTO", 0);
				}
				ndoCntTot1.setRowDescription("ACCESSI", 0);
				ndoCntTotOre.setRowDescription("ORE", 0);
				definisciTitoliRiga(dbc, ndoCnt);
			} else {
				l = "2";
				ndoCnt.putHeadTitle("PRESTAZIONI " + tipo_prest + " EFFETTUATE DA " + tipo + " QUALIFICA: " + qualifica
						+ getDescrizioneTipoAccesso(tipoAccesso) + " dal " + dataini + " al " + datafine
						+ " NON ESISTONO DATI");
				ndoCnt.put(ndoUtil.mkPar("PRESTAZIONE", " ", " "), new Integer(0));
				// ndoCnt.setRowDescription("UTENTI ASSISTITI", 0);
				ndoCntTot1.putHeadTitle("RIEPILOGO ACCESSI");
				ndoCntTot1.put(ndoUtil.mkPar("UTENTI ASSISTITI", " ", " "), new Integer(0));
				ndoCntTot.putHeadTitle("RIEPILOGO ASSISTITI SEGUITI DAGLI OPERATORI " + tit_stampa);
				ndoCntTot.put(ndoUtil.mkPar("ACCESSI", " ", " "), new Integer(0));
				// ndoCntTot.setRowDescription(" ASSISTITI", 0);
			}
			// ora posso produrre la stampa
			Vector vNdoCnt = new Vector();
			vNdoCnt.addElement(ndoCnt);
			vNdoCnt.addElement(ndoCntTot);
			vNdoCnt.addElement(ndoCntTot1);
			vNdoCnt.addElement(ndoCntTotOre);
			ndo_multiPrinter ndoPrt = new ndo_multiPrinter(vNdoCnt);
			// definizione livelli di stampa e tipo
			System.out.println("livello[" + l + "]");
			liv = (new Integer(l)).intValue();

			String tipo_st = (String) par.get("TYPE");
			byte[] rit = null;
			if (tipo_st.equals("PDF"))
				rit = ndoPrt.getDocument(ndoPrt.FOP_TYPE, liv);
			else
				rit = ndoPrt.getDocument(ndoPrt.HTML_TYPE, liv);
			// String by= new String(rit);
			// System.out.println("Stringa del byte array   :"+by);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_StatPrest()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.out.println(e1);
				}
			}
		}
	}

	private String getDescrizioneTipoAccesso(String tipoAccesso) {
		String punto = MIONOME + "getDescrizioneTipoAccesso ";
		String valoreDescrizione = "";

		if (uguale(tipoAccesso, VALORE_ACCESSI_OCCASIONALI)) {
			valoreDescrizione = " CON ACCESSI OCCASIONALI - SPECIALISTI ";
		} else {
			if (uguale(tipoAccesso, VALORE_ACCESSI_PRESTAZIONE)) {
				valoreDescrizione = " CON ACCESSI/PRESTAZIONI ";
			} else {
				valoreDescrizione = "";
			}
		}
		// stampa(punto + "valore descrizione>" + valoreDescrizione + "<");
		return valoreDescrizione;
	}

	private void stampa(String messaggio) {
		System.out.println(messaggio);
	}

	private boolean uguale(String tipoAccesso, String valoreAccessiPrestazione) {
		boolean stringUguali = false;

		if (tipoAccesso != null && valoreAccessiPrestazione != null) {
			stringUguali = tipoAccesso.equalsIgnoreCase(valoreAccessiPrestazione);
		}
		return stringUguali;
	}

	public byte[] query_statprestOld(String utente, String passwd, Hashtable par, mergeDocument eve)
			throws SQLException {
		String punto = MIONOME + "query_statprest ";
		stampaInizio(punto, par);
		boolean done = false;
		ISASConnection dbc = null;
		try {
			
			this.dom_res=(String)par.get("dom_res");
			if (this.dom_res != null)
			{
			if (this.dom_res.equals("R")) this.dr="Residenza";
			else if (this.dom_res.equals("D")) this.dr="Domicilio";
			}
			
			myLogin lg = new myLogin();
			lg.put(utente, passwd);
			dbc = super.logIn(lg);

			boolean entrato = false;
			String myselect = "";
			String myselect_motivo = "";
			String sel = "";
			String dataini = "";
			String datafine = "";
			String tipo = "";
			String tipoAccesso = getValoreStringa(par, "tipo_accesso");
			String conf = "";
			// CJ 13/02/2006
			String motivo = "";
			String unifun = "";
			String list = "";
			String from_uni = "";
			String from_cont = "";
			String where_uni = "";
			String where_cont = "";
			// Fine CJ
			String pca = "";

			Integer cartella = new Integer(0);
			Hashtable cartella_cont_old = new Hashtable();
			Hashtable cartella_occ_old = new Hashtable();
			Hashtable accesso_old = new Hashtable();

			// G.Brogi 07/06/06 uso una outer join per tirarmi fuori
			// anche le cartelle con accessi occasionali (senza contatti)
			// Non uso la outerjoin se scelgo un motivo
			String outJoin_1 = dbc.getoutTab();
			String outJoin_2 = dbc.getoutCrit();
			String campi = "";
			String campi_ord = "";
			// fine 07/06/06

			ServerUtility su = new ServerUtility();

			unifun = (String) par.get("unifun");
			if (!unifun.equals("") && !unifun.equals("TUTTO")) {
				from_uni = ",tabuf t";
				list = "t.descrizione,";
			}

			dataini = (String) par.get("dataini");
			datafine = (String) par.get("datafine");

			tipo = (String) par.get("tipo");
			motivo = (String) par.get("motivo");
			if (tipo.equals("01")) { // assistente sociale
				if (!motivo.equals("") && !motivo.equals("TUTTO")) {
					from_cont = ",ass_progetto co "; // gb 07/06/07
					where_cont = " AND co.n_cartella=i.int_cartella" + " AND co.n_progetto = i.n_progetto" + // gb
							// 07/06/07
							" AND i.n_progetto IS NOT NULL" + // gb 07/06/07
							" AND co.ap_motivo='" + motivo + "'"; // gb 07/06/07
				} else if (motivo.equals("TUTTO")) {
					campi = ",nvl(''||co.n_cartella,'ZZZ') contcartella";
					campi_ord = "cont,";
					from_cont = "," + outJoin_1 + " ass_progetto co "; // gb
					// 07/06/07
					where_cont = " AND co.n_cartella " + outJoin_2 + "=i.int_cartella" + " AND co.n_progetto"
							+ outJoin_2 + "=i.n_progetto" + // gb 07/06/07
							" AND i.n_progetto IS NOT NULL"; // gb 07/06/07
				}

			} else if (tipo.equals("02")) {
				if (!motivo.equals("") && !motivo.equals("TUTTO")) {
					// e' stato scelto un motivo di dimissione: devo estrarre
					// solo i dimesi nel periodo per quel motivo
					from_cont = ", skinf sk ";
					where_cont = " AND sk.n_cartella=i.int_cartella" + " AND sk.n_contatto=i.int_contatto"
							+ " AND sk.ski_motivo='" + motivo + "'";
				} else if (motivo.equals("TUTTO")) {
					campi = ",nvl(''||sk.n_cartella,'ZZZ') contcartella";
					campi_ord = "sk.n_cartella,";
					from_cont = ", " + outJoin_1 + " skinf sk ";
					where_cont = " AND sk.n_cartella " + outJoin_2 + "=i.int_cartella" + " AND sk.n_contatto "
							+ outJoin_2 + "=i.int_contatto";
				}
			} else {
				if (uguale(tipo, "00")) {

				}
			}

			String ragg = (String) par.get("ragg");
			/* Jessica 30/03/2009 */
			String zona = "";
			String distretto = "";
			String comune = "";
			String tipoStampa = (String) par.get("terr");
			StringTokenizer st = new StringTokenizer(tipoStampa, "|");
			String sZona = st.nextToken();
			String sDis = st.nextToken();
			String sCom = st.nextToken();

			if (sZona.equals("1"))
				zona = " u.cod_zona,u.des_zona, ";
			else
				zona = " 'NESSUNA DIVISIONE' cod_zona,'NESSUNA DIVISIONE' des_zona, ";

			if (sDis.equals("1"))
				distretto = " u.des_distretto,u.cod_distretto, ";
			else
				distretto = " 'NESSUNA DIVISIONE' des_distretto,'NESSUNA DIVISIONE' cod_distretto, ";

			if (sCom.equals("1"))
				comune = " u.codice ,u.descrizione ";
			else
				comune = " 'NESSUNA DIVISIONE' codice,'NESSUNA DIVISIONE' descrizione ";
			/* fine Jessica 30/03/2009 */
			pca = (String) par.get("pca");
			// 30/03/2009myselect="SELECT u.des_zona, u.des_distretto, u.codice, u.descrizione,"+
			myselect = "SELECT " + zona + distretto + comune + ","
					+ "p.pre_des_prest,p.pre_numero,p.pre_cod_prest,i.int_cartella,"
					+ "p.pre_anno,p.pre_contatore,i.int_coduf,i.int_tempo," + list
					+ " u.cod_distretto int_coddistr,u.cod_zona int_codzona" + campi
					+ " FROM intpre p,interv i,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +"  u " + from_uni + from_cont
					+ " WHERE p.pre_contatore=i.int_contatore AND " + " i.int_anno=p.pre_anno ";
			myselect = su.addWhere(myselect, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);

			myselect_motivo = "SELECT DISTINCT i.int_cartella,u.cod_distretto int_coddistr,"
					+ "u.des_zona, u.des_distretto, u.codice, u.descrizione,"
					+
					// 05/12/2006 Vado a tirare fuori anche il codice zona,
					// unit� funzionale
					// e presidio nel caso in cui la stampa venga mandata per
					// zona
					" u.cod_zona int_codzona";
					
			// 31/01/11	
			if (unitaFunzDaUsare(dbc))
				myselect_motivo += ", i.int_coduf";
				
			myselect_motivo +=	" FROM intpre p,interv i,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +"  u " + from_uni
					+ from_cont + " WHERE p.pre_contatore=i.int_contatore AND " + " i.int_anno=p.pre_anno ";
			myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "u.tipo", su.OP_EQ_STR, ragg);
		if (this.dom_res==null)
		{
			if (ragg.equals("P")) {
				myselect += " AND i.int_codpres=u.codice";
				myselect_motivo += " AND i.int_codpres=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_codpres", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_codpres", su.OP_EQ_STR, pca);
			} else if (ragg.equals("A")) {
				myselect += " AND i.int_cod_areadis=u.codice";
				myselect_motivo += " AND i.int_cod_areadis=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
			} else if (ragg.equals("C")) {
				myselect += " AND i.int_cod_comune=u.codice";
				myselect_motivo += " AND i.int_cod_comune=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
			}
		}else if (this.dom_res.equals("D"))
		{
			 if (ragg.equals("A")) {
				myselect += " AND i.int_cod_areadis=u.codice";
				myselect_motivo += " AND i.int_cod_areadis=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
			} else if (ragg.equals("C")) {
				myselect += " AND i.int_cod_comune=u.codice";
				myselect_motivo += " AND i.int_cod_comune=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
			}
		}else if (this.dom_res.equals("R"))
		{
			if (ragg.equals("A")) {
				myselect += " AND i.int_cod_areadis=u.codice";
				myselect_motivo += " AND i.int_cod_areadis=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_areadis", su.OP_EQ_STR, pca);
			} else if (ragg.equals("C")) {
				myselect += " AND i.int_cod_comune=u.codice";
				myselect_motivo += " AND i.int_cod_comune=u.codice";
				myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
				myselect = su.addWhere(myselect, su.REL_AND, "i.int_cod_comune", su.OP_EQ_STR, pca);
			}
		}

			sel = su.addWhere(sel, su.REL_AND, "u.cod_zona", su.OP_EQ_STR, (String) par.get("zona"));
			sel = su.addWhere(sel, su.REL_AND, "u.cod_distretto", su.OP_EQ_STR, (String) par.get("distretto"));
			sel = su.addWhere(sel, su.REL_AND, "i.int_data_prest", su.OP_GE_NUM, formatDate(dbc, dataini));
			sel = su.addWhere(sel, su.REL_AND, "i.int_data_prest", su.OP_LE_NUM, formatDate(dbc, datafine));

			if (!tipo.equals("00"))
				sel = su.addWhere(sel, su.REL_AND, "i.int_tipo_oper", su.OP_EQ_STR, tipo);

			// 09/01/2004 Jessica inserito filtro flag tipo accertamento:
			// domiciliare o ambulatoriale o entrambi(nessun filtro)
			String tipo_prest = "";
			if (!par.get("tipo_prest").equals("E")) {
				tipo_prest = (String) par.get("tipo_prest");
				sel = su.addWhere(sel, su.REL_AND, "i.int_ambdom", su.OP_EQ_STR, tipo_prest);
			}

			if (!unifun.equals("") && !unifun.equals("TUTTO")) {
				sel += " AND i.int_coduf=t.codice ";
				if (!unifun.equals("TUTTO"))
					sel += " AND t.codice='" + unifun + "'";
			}

			myselect += " AND " + sel + where_cont;
			myselect_motivo += " AND " + sel + where_cont;

			myselect += " ORDER BY u.des_zona, u.des_distretto, u.descrizione ,int_codzona, int_coddistr, u.codice,p.pre_des_prest";

			stampaQuery(punto, myselect);

			ISASCursor dbcur = dbc.startCursor(myselect);

			// Inserisco la decodifica del tipo prestazione
			if (tipo_prest.equals("D"))
				tipo_prest = "DOMICLIARI";
			else if (tipo_prest.equals("A"))
				tipo_prest = "AMBULATORIALI";
			// istanzio il container
			ndo_container ndoCnt = new ndo_container();
			ndo_container ndoCntTot1 = new ndo_container();
			ndo_container ndoCntTot = new ndo_container();
			ndo_container ndoCntTotOre = new ndo_container();

			int i = 0;
			// J22/03/04
			String tit_stampa = "";
			/*
			 * if (((String)par.get("livello")).equals("2U"))
			 * tit_stampa="DELL'UNITA' FUNZIONALE"; if
			 * (((String)par.get("livello")).equals("2Z"))
			 * tit_stampa="DELLA ZONA"; else if
			 * (((String)par.get("livello")).equals("2P"))
			 * tit_stampa="DEL PRESIDIO"; else if
			 * (((String)par.get("livello")).equals("2"))
			 * tit_stampa="DEL DISTRETTO";
			 */
			// inserisco nel container il titolo iniziale e il footer
			dataini = dataini.substring(8, 10) + "/" + dataini.substring(5, 7) + "/" + dataini.substring(0, 4);
			datafine = datafine.substring(8, 10) + "/" + datafine.substring(5, 7) + "/" + datafine.substring(0, 4);
			ndoCntTot.putHeadTitle("RIEPILOGO ASSISTITI SEGUITI DAGLI OPERATORI " + tit_stampa);
			ndoCntTot1.putHeadTitle("RIEPILOGO ACCESSI");
			ndoCntTotOre.putHeadTitle("RIEPILOGO ORE");
			if (tipo.equals(""))
				ndoCnt.putHeadTitle("PRESTAZIONI " + tipo_prest + " EFFETTUATE dal " + dataini + " al " + datafine);
			else {
				if (tipo.equals("01"))
					tipo = "ASSISTENTI SOCIALI";
				else if (tipo.equals("02"))
					tipo = "INFERMIERI";
				else if (tipo.equals("03"))
					tipo = "MEDICI";
				else if (tipo.equals("04"))
					tipo = "FISIOTERAPISTI";
				else if (tipo.equals("52"))
					tipo = "ONCOLOGI";
				else if (tipo.equals("98"))
					tipo = "MEDICI SPECIALISTI";
				ndoCnt.putHeadTitle("PRESTAZIONI " + tipo_prest + " EFFETTUATE DA " + tipo + " dal " + dataini + " al "
						+ datafine);
			}
			String selconf = "SELECT conf_txt from conf " + "WHERE conf_kproc='SINS' AND"
					+ " conf_key='ragione_sociale'";
			ISASRecord dbconf = dbc.readRecord(selconf);
			if (dbconf != null)
				if (dbconf.get("conf_txt") != null && !((String) dbconf.get("conf_txt")).equals("")) {
					conf = (String) dbconf.get("conf_txt");
					ndoCnt.putFootTitle(conf);
				} else
					ndoCnt.putFootTitle(" ");
			else
				ndoCnt.putFootTitle(" ");

			String l = "";
			String stampa = "";
			int liv = 0;
			while (dbcur.next()) {
				entrato = true;
				i++;
				ISASRecord dbr = dbcur.getRecord();
				String col1 = "";
				String col2 = "";
				String col3 = "";
				String col4 = "";
				String cod_riga = "";
				Integer num_prest;

				if (dbr.get("int_cartella") != null)
					cartella = (Integer) dbr.get("int_cartella");
				// G.Brogi 19/0/07: introdotto il pannello del territorio; il
				// livello
				// e' sempre 3 o 4 (se c'e' l'unita' funz.)!
				if (sCom.equals("1"))
					l = "4";
				else {
					if (sDis.equals("1"))
						l = "3";
					else if (sZona.equals("1"))
						l = "2";
					else
						l = "0";// caso di NESSUNA DIVISIONE ovunque
				}

				if (!(unifun == null || unifun.equals(""))) {
					// 30/03/2009 incremento il valore del livello perch� non �
					// pi� stabile
					int i_l = Integer.parseInt(l);
					i_l++;
					l = "" + i_l;
					// l="5";
				} else {
					// lo lascio invariato
					// l = "4";
				}

				if (sZona.equals("1")) {
					if (dbr.get("int_codzona") != null && !((String) dbr.get("int_codzona")).equals(""))
						col1 = (String) dbr.get("int_codzona");
					else
						col1 = "zz1";
				}
				if (sDis.equals("1")) {
					if (dbr.get("int_coddistr") != null && !((String) dbr.get("int_coddistr")).equals(""))
						col2 = (String) dbr.get("int_coddistr");
					else
						col2 = "xx1";
				}
				if (sCom.equals("1")) {
					if (dbr.get("codice") != null && !((String) dbr.get("codice")).equals(""))
						col3 = (String) dbr.get("codice");
					else
						col3 = "yy1";
				}
				if (dbr.get("int_coduf") != null && !((String) dbr.get("int_coduf")).equals(""))
					col4 = (String) dbr.get("int_coduf");
				else
					col4 = "ww1";

				String colonna = "";
				/* Jessica 30/03/2009 */
				if (!col1.equals("")) {
					colonna += col1;
					if (!col2.equals("")) {
						colonna += "|" + col2;
						if (!col3.equals(""))
							colonna += "|" + col3;
					}
				} else
					// caso in cui sia tutto NESSUNA DIVISIONE
					colonna = "N";
				System.out.println("colonna:" + colonna);
				if (!(unifun == null || unifun.equals(""))) {
					colonna += "|" + col4;
				}

				if (!("" + dbr.get("contcartella")).equals("ZZZ"))
					cartella_cont_old.put(colonna + "|" + cartella, "1");
				else
					cartella_occ_old.put(colonna + "|" + cartella, "1");

				String pre_con = "" + dbr.get("pre_contatore");
				String pre_anno = "" + dbr.get("pre_anno");
				if (!accesso_old.containsKey(pre_con + "|" + pre_anno)) {
					if (!col1.equals("")) {
						if (!col2.equals("")) {
							if (!col3.equals("")) {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2, col3, col4), new Integer(1));
								} else {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2, col3), new Integer(1));
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2, col4), new Integer(1));
								} else {
									ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col2), new Integer(1));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTot1.put(ndoUtil.mkPar("ACC", col1, col4), new Integer(1));
							} else {
								ndoCntTot1.put(ndoUtil.mkPar("ACC", col1), new Integer(1));
							}
						}
					} else {
						if (!(unifun == null || unifun.equals(""))) {
							ndoCntTot1.put(ndoUtil.mkPar("ACC", col4), new Integer(1));
						} else {
							System.out.println("ENTRO QUIIIIII");
							ndoCntTot1.put(ndoUtil.mkPar("ACC", "N"), new Integer(1));
						}
					}
					//System.out.println("Esco????");
					if (dbr.get("int_tempo") != null) {
						// calcolo le ore
						Integer tminuti = (Integer) dbr.get("int_tempo");
						int minut = tminuti.intValue();
						int ore = minut / 60;

						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3, col4),
												new Integer(ore));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3), new Integer(ore));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col4), new Integer(ore));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2), new Integer(ore));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col4), new Integer(ore));
								} else {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1), new Integer(ore));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", col4), new Integer(ore));
							} else {
								System.out.println("ENTRO QUIIIIII ore");
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", "N"), new Integer(ore));
							}
						}
					} else {
						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3, col4), new Integer(0));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col3), new Integer(0));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2, col4), new Integer(0));
									} else {
										ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col2), new Integer(0));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1, col4), new Integer(0));
								} else {
									ndoCntTotOre.put(ndoUtil.mkPar("HORE", col1), new Integer(0));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", col4), new Integer(0));
							} else {
								ndoCntTotOre.put(ndoUtil.mkPar("HORE", "N"), new Integer(0));
							}
						}
					}
					accesso_old.put(pre_con + "|" + pre_anno, "1");
				}

				// inserisco le righe delle prestazioni
				if (dbr.get("pre_cod_prest") != null && !((String) dbr.get("pre_cod_prest")).equals("")) {
					cod_riga = (String) dbr.get("pre_cod_prest");
				} else
					cod_riga = "rr1";

				if (dbr.get("pre_numero") != null)
					num_prest = (Integer) dbr.get("pre_numero");
				else
					num_prest = new Integer(0);
				// System.out.println("***col1:"+col1+" col2:"+col2);

				/*
				 * if(!(unifun==null || unifun.equals(""))){
				 * ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2,col3,col4),
				 * num_prest); }else{ ndoCnt.put(ndoUtil.mkPar(cod_riga, col1,
				 * col2,col3), num_prest); }
				 */

				if (!col1.equals("")) {
					if (!col2.equals("")) {
						if (!col3.equals("")) {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2, col3, col4), num_prest);
							} else {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2, col3), num_prest);
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2, col4), num_prest);
							} else {
								ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col2), num_prest);
							}
						}
					} else {
						if (!(unifun == null || unifun.equals(""))) {
							ndoCnt.put(ndoUtil.mkPar(cod_riga, col1, col4), num_prest);
						} else {
							ndoCnt.put(ndoUtil.mkPar(cod_riga, col1), num_prest);
						}
					}
				} else {
					if (!(unifun == null || unifun.equals(""))) {
						ndoCnt.put(ndoUtil.mkPar(cod_riga, col4), num_prest);
					} else {
						System.out.println("ENTRO QUI numero prestazioni");
						ndoCnt.put(ndoUtil.mkPar(cod_riga, "N"), num_prest);
					}
				}

			}// end while
			if (entrato) {
				if (motivo.equals("TUTTO")) {

					// scorro prima i soggetti con accessi occasionali - senza
					// contatto
					// Se hanno anche un contatto, vanno conteggiati nella riga
					// degli
					// assistiti con contatto
					Enumeration kocc = orderedKeys(cartella_occ_old);
					// booleano primavolta serve ad inserire sempre per prima la
					// riga degli assistiti con un contatto
					boolean primavolta = true;
					while (kocc.hasMoreElements()) {
						String e = (String) kocc.nextElement();
						StringTokenizer tk = new StringTokenizer(e, "|");
						String col1 = "";
						String col2 = "";
						String col3 = "";
						String col4 = "";
						if (sZona.equals("1")) {
							col1 = tk.nextToken();
							if (sDis.equals("1")) {
								col2 = tk.nextToken();
								if (sCom.equals("1"))
									col3 = tk.nextToken();
							}
						}
						if (!(unifun == null || unifun.equals(""))) {
							col4 = tk.nextToken();
						}

						if (cartella_cont_old.containsKey(e)) {
							// System.out.println("***Ass con contatto");
							// caso assistito con contatto

							if (!col1.equals("")) {
								if (!col2.equals("")) {
									if (!col3.equals("")) {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4), new Integer(1));
										} else {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(1));
										}
									} else {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(1));
										} else {
											ndoCnt.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(1));
										}
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCnt.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(1));
									} else {
										ndoCnt.put(ndoUtil.mkPar("ASS", col1), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCnt.put(ndoUtil.mkPar("ASS", col4), new Integer(1));
								} else {
									ndoCnt.put(ndoUtil.mkPar("ASS", "N"), new Integer(1));
								}
							}

							// se lo conteggio ora non lo devo conteggiare dopo,
							// quando
							// scorrero' cartella_cont_old => lo rimuovo da li'
							cartella_cont_old.remove(e);
						} else {
							// caso assistito senza contatto
							// ma con accesso occasionale
							if (primavolta) {
								if (!col1.equals("")) {
									if (!col2.equals("")) {
										if (!col3.equals("")) {
											if (!(unifun == null || unifun.equals(""))) {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4),
														new Integer(0));
											} else {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(0));
											}
										} else {
											if (!(unifun == null || unifun.equals(""))) {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(0));
											} else {
												ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(0));
											}
										}
									} else {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(0));
										} else {
											ndoCntTot.put(ndoUtil.mkPar("ASS", col1), new Integer(0));
										}
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col4), new Integer(0));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", "N"), new Integer(0));
									}
								}
								/*
								 * 31/03/09if(!(unifun==null ||
								 * unifun.equals(""))){
								 * ndoCntTot.put(ndoUtil.mkPar("ASS",
								 * col1,col2,col3,col4), new Integer(0)); }else{
								 * ndoCntTot.put(ndoUtil.mkPar("ASS",
								 * col1,col2,col3), new Integer(0)); }
								 */
								primavolta = false;
							}
							if (!col1.equals("")) {
								if (!col2.equals("")) {
									if (!col3.equals("")) {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2, col3, col4), new Integer(
													1));
										} else {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2, col3), new Integer(1));
										}
									} else {
										if (!(unifun == null || unifun.equals(""))) {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2, col4), new Integer(1));
										} else {
											ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col2), new Integer(1));
										}
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col4), new Integer(1));
								} else {
									ndoCntTot.put(ndoUtil.mkPar("ASSOCC", "N"), new Integer(1));
								}
							}
							/*
							 * 31/03/09if(!(unifun==null || unifun.equals(""))){
							 * ndoCntTot.put(ndoUtil.mkPar("ASSOCC",
							 * col1,col3,col4), new Integer(1)); }else{
							 * ndoCntTot.put(ndoUtil.mkPar("ASSOCC", col1,col3),
							 * new Integer(1)); }
							 */
						}
					}// end while kocc

					// scorrimento hashtable assistiti con contatto (e' gia'
					// stata
					// ripulita da quelli conteggiati nel while precedente)
					Enumeration kcont = orderedKeys(cartella_cont_old);
					while (kcont.hasMoreElements()) {
						String e = (String) kcont.nextElement();
						StringTokenizer tk = new StringTokenizer(e, "|");
						String col1 = "";
						String col2 = "";
						String col3 = "";
						String col4 = "";
						if (sZona.equals("1")) {
							col1 = tk.nextToken();
							if (sDis.equals("1")) {
								col2 = tk.nextToken();
								if (sCom.equals("1"))
									col3 = tk.nextToken();
							}
						}
						if (!(unifun == null || unifun.equals(""))) {
							col4 = tk.nextToken();
						}

						// caso assistito con contatto
						/*
						 * 31/03/09if(!(unifun==null || unifun.equals(""))) {
						 * ndoCntTot.put(ndoUtil.mkPar("ASS",
						 * col1,col2,col3,col4), new Integer(1)); }else{
						 * ndoCntTot.put(ndoUtil.mkPar("ASS", col1,col2,col3),
						 * new Integer(1)); }
						 */
						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(1));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(1));
								} else {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1), new Integer(1));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTot.put(ndoUtil.mkPar("ASS", col4), new Integer(1));
							} else {
								ndoCntTot.put(ndoUtil.mkPar("ASS", "N"), new Integer(1));
							}
						}
					}
				}// end if(motivo=tutto
				else {// ho scelto un motivo -> conteggio la persona come
					// assistito solo se
					if (tipo.equals("ASSISTENTI SOCIALI")) {
						// gb 07/06/07 *******
						myselect_motivo += " AND co.ap_data_apertura IN "
								+ "(SELECT MAX (t.ap_data_apertura) FROM ass_progetto t where "
								+ "t.n_cartella=co.n_cartella" + " AND t.ap_data_apertura" + "<="
								+ formatDate(dbc, datafine) + ")" + " ORDER BY u.cod_distretto";
						// gb 07/06/07: fine *******
					}/*
					 * Jessica 30/03/2009 questa modifica non andava fatta else
					 * if(tipo.equals("INFERMIERI")){ myselect_motivo
					 * +=" AND sk.ski_data_apertura IN "+
					 * "(SELECT MAX (t.ski_data_apertura) FROM skinf t where "+
					 * "t.n_cartella=sk.n_cartella"+ " AND t.ski_data_apertura"+
					 * "<="+ formatDate(dbc,datafine)+ ")"+
					 * " ORDER BY u.cod_distretto"; }
					 */
					else if (tipo.equals("INFERMIERI")) {
						myselect_motivo += " AND sk.ski_data_apertura IN "
								+ "(SELECT MAX (z.ski_data_apertura) FROM skinf z, "
								+ "intpre a,interv b,"+ ((par.get("socsan")!=null && par.get("socsan").equals("01"))?"ubicazioni_n_soc":"ubicazioni_n") +" c " + "WHERE z.n_cartella=sk.n_cartella"
								+ " AND z.ski_data_apertura" + "<=" + formatDate(dbc, datafine);
						myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_data_prest", su.OP_GE_NUM,
								formatDate(dbc, dataini));
						myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_data_prest", su.OP_LE_NUM,
								formatDate(dbc, datafine));

						if (!((String) par.get("tipo")).equals("00"))
							myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_tipo_oper", su.OP_EQ_STR,
									(String) par.get("tipo"));
						if (!((String) par.get("tipo_prest")).equals("E"))
							myselect_motivo = su.addWhere(myselect_motivo, su.REL_AND, "b.int_ambdom", su.OP_EQ_STR,
									(String) par.get("tipo_prest"));

						if (!unifun.equals("") && !unifun.equals("TUTTO")) {
							myselect_motivo += " AND b.int_coduf=t.codice ";
							if (!unifun.equals("TUTTO"))
								myselect_motivo += " AND t.codice='" + unifun + "'";
						}
						myselect_motivo += " AND z.n_cartella=b.int_cartella" + " AND z.n_contatto=b.int_contatto"
								+ " AND z.ski_motivo='" + motivo + "'" + " AND a.pre_contatore=b.int_contatore AND "
								+ " b.int_anno=a.pre_anno AND " + " c.tipo='P' AND b.int_codpres=c.codice "
								+ ") ORDER BY u.cod_distretto";
					}
					System.out.println("Select per assistiti: " + myselect_motivo);
					ISASCursor dbcurmot = dbc.startCursor(myselect_motivo);
					while (dbcurmot.next()) {
						ISASRecord dbmot = (ISASRecord) dbcurmot.getRecord();
						/*
						 * 05/12/2006 controllo per che cosa � stata lanciata la
						 * stampa e prendo il codice corrispondente
						 */
						String col1 = "";
						String col2 = "";
						String col3 = "";
						String col4 = "";

						col1 = "" + dbmot.get("int_codzona");
						col2 = "" + dbmot.get("int_coddistr");
						col3 = "" + dbmot.get("codice");
						if (dbmot.get("int_coduf") != null) {
							col4 = "" + dbmot.get("int_coduf");
						}
						/*
						 * 31/03/09if(!(unifun==null || unifun.equals(""))){
						 * ndoCntTot.put(ndoUtil.mkPar("ASS",
						 * col1,col2,col3,col4), new Integer(1)); }else{
						 * ndoCntTot.put(ndoUtil.mkPar("ASS", col1,col2,col3),
						 * new Integer(1)); }
						 */
						if (!col1.equals("")) {
							if (!col2.equals("")) {
								if (!col3.equals("")) {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col3), new Integer(1));
									}
								} else {
									if (!(unifun == null || unifun.equals(""))) {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2, col4), new Integer(1));
									} else {
										ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col2), new Integer(1));
									}
								}
							} else {
								if (!(unifun == null || unifun.equals(""))) {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1, col4), new Integer(1));
								} else {
									ndoCntTot.put(ndoUtil.mkPar("ASS", col1), new Integer(1));
								}
							}
						} else {
							if (!(unifun == null || unifun.equals(""))) {
								ndoCntTot.put(ndoUtil.mkPar("ASS", col4), new Integer(1));
							} else {
								ndoCntTot.put(ndoUtil.mkPar("ASS", "N"), new Integer(1));
							}
						}
					}
				}// end else motivo=tutto
				// definisco i titoli da associare alle chiavi di colonna
				System.out.println("*****ndoCnt");
				definisciTitoliColonna(dbc, ndoCnt, ragg, unifun);
				System.out.println("*****ndoCntTot");
				definisciTitoliColonna(dbc, ndoCntTot, ragg, unifun);
				System.out.println("*****ndoCntTot1");
				definisciTitoliColonna(dbc, ndoCntTot1, ragg, unifun);
				System.out.println("*****ndoCntTotOre");
				definisciTitoliColonna(dbc, ndoCntTotOre, ragg, unifun);
				System.out.println("*****DOPO TUTTO");
				// System.out.println("Tipo:"+tipo+" motivo:"+motivo);
				// definisco i titoli di riga
				if ((tipo.equals("ASSISTENTI SOCIALI") || tipo.equals("INFERMIERI")) && motivo.equals("TUTTO")) {
					ndoCntTot.setRowDescription("ASSISTITI CON ALMENO UN CONTATTO", 0);
					ndoCntTot.setRowDescription("ASSISTITI CON ACCESSI OCCASIONALI", 1);
				} else {
					ndoCntTot.setRowDescription("ASSISTITI CON ALMENO UN CONTATTO", 0);
				}
				ndoCntTot1.setRowDescription("ACCESSI", 0);
				ndoCntTotOre.setRowDescription("ORE", 0);
				definisciTitoliRiga(dbc, ndoCnt);
			} else {
				l = "2";
				ndoCnt.putHeadTitle("PRESTAZIONI " + tipo_prest + " EFFETTUATE DA " + tipo + " dal " + dataini + " al "
						+ datafine + " NON ESISTONO DATI");
				ndoCnt.put(ndoUtil.mkPar("PRESTAZIONE", " ", " "), new Integer(0));
				// ndoCnt.setRowDescription("UTENTI ASSISTITI", 0);
				ndoCntTot1.putHeadTitle("RIEPILOGO ACCESSI");
				ndoCntTot1.put(ndoUtil.mkPar("UTENTI ASSISTITI", " ", " "), new Integer(0));
				ndoCntTot.putHeadTitle("RIEPILOGO ASSISTITI SEGUITI DAGLI OPERATORI " + tit_stampa);
				ndoCntTot.put(ndoUtil.mkPar("ACCESSI", " ", " "), new Integer(0));
				// ndoCntTot.setRowDescription(" ASSISTITI", 0);
			}
			// ora posso produrre la stampa
			Vector vNdoCnt = new Vector();
			vNdoCnt.addElement(ndoCnt);
			vNdoCnt.addElement(ndoCntTot);
			vNdoCnt.addElement(ndoCntTot1);
			vNdoCnt.addElement(ndoCntTotOre);
			ndo_multiPrinter ndoPrt = new ndo_multiPrinter(vNdoCnt);
			
			// definizione livelli di stampa e tipo
			System.out.println("livello[" + l + "]");
			liv = (new Integer(l)).intValue();

			String tipo_st = (String) par.get("TYPE");
			byte[] rit = null;
			if (tipo_st.equals("PDF"))
				rit = ndoPrt.getDocument(ndoPrt.FOP_TYPE, liv);
			else 
				rit = ndoPrt.getDocument(ndoPrt.HTML_TYPE, liv);
			// String by= new String(rit);
			// System.out.println("Stringa del byte array   :"+by);

			dbcur.close();
			dbc.close();
			super.close(dbc);
			done = true;
			return rit;
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una query_StatPrest()  ");
		} finally {
			if (!done) {
				try {
					dbc.close();
					super.close(dbc);
				} catch (Exception e1) {
					e1.printStackTrace();
					System.out.println(e1);
				}
			}
		}
	}

	private void stampaInizio(String punto, Hashtable par) {
		System.out.println(punto + "\n INIZIO CON DATI>" + par + "<\n");
	}

	public String getValoreStringa(Hashtable hashtable, String key) {
		String punto = MIONOME + "getValoreStringa";
		String valoreLetto = "";
		try {
			valoreLetto = hashtable.get(key) + "";
		} catch (Exception e) {
			System.out.println(punto + "Errore nella lettura della key>" + key + "<\nhashtable>" + hashtable + "<");
		}

		return valoreLetto;
	}

	private void stampaQuery(String punto, String query) {
		System.out.println(punto + "Query>" + query + "<");
	}

	public static void definisciTitoliColonna(ISASConnection dbc, ndo_container c, String ragg, String unifun)
			throws SQLException {
		ndoUtil.getKeysStart(c);
		Vector v;
		int i = 0;
		try {
			ServerUtility su = new ServerUtility();
			while ((v = ndoUtil.getKeysNext()) != null) {

				String t = " ";
				System.out.println("Vettore=size:" + v.size());
				if (v.size() == 2) {
					if (!((String) v.elementAt(1)).equals("N")) {
						System.out.println("v.size=2");
						i = 1;
						String mysel = "SELECT descrizione_zona FROM zone WHERE ";
						String sel = "";
						sel = su.addWhere(sel, su.REL_AND, "codice_zona", su.OP_EQ_STR, (String) v.elementAt(i));
						mysel = mysel + sel;
						ISASRecord dbcom = dbc.readRecord(mysel);
						if (dbcom != null)
							if (dbcom.get("descrizione_zona") != null
									&& !((String) dbcom.get("descrizione_zona")).equals(""))
								t = (String) dbcom.get("descrizione_zona");
							else
								t = " ";
						else
							t = " ";
					} else
						t = " ";
				} else if (v.size() == 3) {
					System.out.println("v.size=3");
					i = 2;
					String mysel = "SELECT des_distr FROM distretti  WHERE ";
					String sel = "";
					sel = su.addWhere(sel, su.REL_AND, "cod_distr", su.OP_EQ_STR, (String) v.elementAt(i));
					mysel = mysel + sel;
					ISASRecord dbr = dbc.readRecord(mysel);
					if (dbr != null)
						if (dbr.get("des_distr") != null && !((String) dbr.get("des_distr")).equals(""))
							t = (String) dbr.get("des_distr");
				} else if (v.size() == 4) {
					System.out.println("v.size=4");
					i = 3;
					String mysel = "";
					String sel = "";
					if (ragg.equals("P")) {
						mysel = "SELECT despres descr FROM presidi WHERE ";
						sel = su.addWhere(sel, su.REL_AND, "codpres", su.OP_EQ_STR, (String) v.elementAt(i));
						mysel = mysel + sel;
					} else if (ragg.equals("C")) {
						mysel = "SELECT descrizione descr FROM comuni WHERE ";
						sel = su.addWhere(sel, su.REL_AND, "codice", su.OP_EQ_STR, (String) v.elementAt(i));
						mysel = mysel + sel;
					} else if (ragg.equals("A")) {
						mysel = "SELECT descrizione descr FROM areadis WHERE ";
						sel = su.addWhere(sel, su.REL_AND, "codice", su.OP_EQ_STR, (String) v.elementAt(i));
						mysel = mysel + sel;
					}
					ISASRecord dbcom = dbc.readRecord(mysel);
					if (dbcom != null)
						if (dbcom.get("descr") != null && !((String) dbcom.get("descr")).equals(""))
							t = (String) dbcom.get("descr");
						else
							t = " ";
					else
						t = " ";
				} else if (v.size() == 5 && !(unifun == null || unifun.equals(""))) {
					System.out.println("v.size=5");
					i = 4;
					String mysel = "SELECT descrizione FROM tabuf WHERE ";
					String sel = "";
					if (v.elementAt(i) != null && !((String) v.elementAt(i)).equals("")) {
						sel = su.addWhere(sel, su.REL_AND, "codice", su.OP_EQ_STR, (String) v.elementAt(i));
						mysel = mysel + sel;
						ISASRecord dbcom = dbc.readRecord(mysel);
						if (dbcom != null)
							if (dbcom.get("descrizione") != null && !((String) dbcom.get("descrizione")).equals(""))
								t = (String) dbcom.get("descrizione");
							else
								t = " ";
						else
							t = " ";
					} else
						t = " ";
				}
				c.addTitle(v, t);
			}
			System.out.println("Esco da definisci titoliColonna");
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una definisciTitoloColonna()  ");
		}
	}

	// definisce i titoli da associare alle chiavi di riga
	public static void definisciTitoliRiga(ISASConnection dbc, ndo_container c) throws SQLException {
		try {
			ServerUtility su = new ServerUtility();
			int nr = c.getRowCount();
			// ci sono due righe fisse!
			// c.setRowDescription("UTENTI ASSISTITI", 0);
			// c.setRowDescription("VISITE-ACCESSI", 1);

			for (int r = 0; r < nr; r++) {
				String curRowKey = (String) c.getRowKey(r);
				String myselect = "SELECT prest_des FROM prestaz WHERE ";
				String sel = "";
				sel = su.addWhere(sel, su.REL_AND, "prest_cod", su.OP_EQ_STR, curRowKey);
				myselect = myselect + sel;
				ISASRecord dbprest = dbc.readRecord(myselect);
				String t = " ";
				if (dbprest != null)
					if (dbprest.get("prest_des") != null && !((String) dbprest.get("prest_des")).equals(""))
						t = (String) dbprest.get("prest_des");
					else
						t = "NON SPECIFICATA";
				else
					t = "NON SPECIFICATA";
				c.setRowDescription(t, r);

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new SQLException("Errore eseguendo una definisciTitoloColonna()  ");
		}

	}

	private boolean unitaFunzDaUsare(ISASConnection dbc) throws Exception 
	{
		boolean ret = false;
		String selConf = "SELECT * FROM conf WHERE conf_key = 'ABIL_ACC_UNIFUN' AND conf_kproc = 'SINS'";
		ISASRecord dbrC = dbc.readRecord(selConf);
		if ((dbrC != null) && (dbrC.get("conf_txt") != null))
			ret = ("SI".equals(dbrC.get("conf_txt").toString()));
		return ret;
	}
	
	
	public String currentTime() {
		Calendar cal = Calendar.getInstance(TimeZone.getDefault());
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		sdf.setTimeZone(TimeZone.getDefault());
		return sdf.format(cal.getTime());
	}

	public Enumeration orderedKeys(Hashtable hOrdinare) {
		Enumeration keys = hOrdinare.keys();
		Vector temp = new Vector();
		while (keys.hasMoreElements()) {
			temp.addElement("" + keys.nextElement());
		}
		Collections.sort(temp);
		return temp.elements();
	}

} // End of FoStatPrest class

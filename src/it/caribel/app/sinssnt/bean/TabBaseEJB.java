package it.caribel.app.sinssnt.bean;
// ==========================================================================
// CARIBEL S.r.l.
// --------------------------------------------------------------------------
//
// 21/11/2005 - EJB di connessione alla procedura SINS Tabella sil_tabase
//
// ==========================================================================

import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.*;
import java.sql.*;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.dbinterf2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.sinssnt.connection.*;

public class TabBaseEJB extends SINSSNTConnectionEJB  {

public TabBaseEJB() {}

public ISASRecord queryKey(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="Select * from sil_tabase where "+
			"tb_cod='"+(String)h.get("tb_cod")+"' AND "+
                        "tb_val='"+(String)h.get("tb_val")+"'";
                System.out.println("QueryKey: "+myselect);
		ISASRecord dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una queryKey()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}

public String duplicateChar(String s, String c) {
        if ((s == null) || (c == null)) return s;
        String mys = new String(s);
        int p = 0;
        while (true) {
                int q = mys.indexOf(c, p);
                if (q < 0) return mys;
                StringBuffer sb = new StringBuffer(mys);
                StringBuffer sb1 = sb.insert(q, c);
                mys = sb1.toString();
                p = q + c.length() + 1;
        }
}


    public Hashtable queryAllCombo(myLogin mylogin, Hashtable h, Vector vKey) throws SQLException
	{
        boolean done = false;
        ISASConnection dbc = null;
        Hashtable h_res = new Hashtable();

        try {
            dbc = super.logIn(mylogin);

            for(int i=0; i<vKey.size(); i++) {
                // il nomeCombo e' del tipo "combo_xxxxxxxx"
                String nomeCombo = (String)vKey.elementAt(i);
                // quindi codTab=XXXXXXXX
                String codTab = nomeCombo.substring(6).toUpperCase();
				Vector vettdbr = new Vector();
				//la combo tiposerv2 deve caricare gli stessi dati di segnalante
				if(codTab.equals("TIPOSERV2"))
					codTab="SEGNALA";
				else if(codTab.equals("AZASSIMP1") || codTab.equals("AZASSIMP2") ||
						codTab.equals("AZDISPON1") || codTab.equals("AZDISPON2") ||
						codTab.equals("TIPOSERV1"))
					codTab = codTab.substring(0,codTab.length()-1);
		
				vettdbr = (Vector) queryCombo(dbc, codTab);
                h_res.put(nomeCombo, vettdbr);

            }

            dbc.close();
			super.close(dbc);
            done = true;

            return h_res;
        }catch(Exception e){
			System.out.println("TabBaseEJB: queryAllCombo - Eccezione=" + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					dbc.close();
					super.close(dbc);
				}catch(Exception e1){
					System.out.println("TabBaseEJB: queryCombo - Eccezione nella chiusura della connessione= " + e1);
				}
            }
        }
    }// END queryAllCombo

    // Si leggono tutti i record con 1 determinata chiave, corrispondente
    // alla combo da caricare (=> anche il rec con tb_val='#', la cui descrizione
    // costituisce il testo da inserire nella JLabel). Sara' il client a preoccuparsi
    // di gestire il contenuto della comboBox e della label.
	private Vector queryCombo(ISASConnection mydbc, String codTab) throws  SQLException
	{
		boolean done = false;
		ISASCursor dbcur = null;

		try{
			String myselect = "SELECT " +
                                " TRIM(tb_val) codice,"+
				" TRIM(tb_descrizione) descriz" +
                                " FROM sil_tabase" +
                                " WHERE tb_cod = '" + codTab + "'" +
								" ORDER BY tb_val";

//	        System.out.println("TabBaseEJB: queryCombo x codTab=[" + codTab + "] - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			ISASRecord recVuoto = mydbc.newRecord("sil_tabase");
			recVuoto.put("codice", "99999999");
			recVuoto.put("descriz", " ");
			if ((vdbr != null) && (vdbr.size() > 1))
			    vdbr.add(1, recVuoto); // nella posizione 0 c'e' il testo x la JLabel

			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("TabBaseEJB: queryCombo con codTab=" + codTab + " - Eccezione=" + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					mydbc.close();
					super.close(mydbc);
				}catch(Exception e1){
					System.out.println("TabBaseEJB: queryCombo - Eccezione nella chiusura della connessione= " + e1);
				}
	   	    }
	   	}
	}// END queryCombo


public Vector query(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM sil_tabase WHERE "+
                                "tb_cod='"+(String)h.get("tb_cod")+
                                "' AND tb_val <> '#'";
                //Viene messo questo filtro perch� nella tabella sil_tabase il primo record
                //ha valore # e indica il nome della combo

                myselect=myselect+" ORDER BY tb_val ";
                System.out.println("query GridTabBase: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
   	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}
}


public Vector queryPaginate(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
        String scr=" ";
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT * FROM sil_tabase WHERE "+
                                "tb_cod='"+(String)h.get("tb_cod")+								
                                "' AND tb_val <> '#'";
                //Viene messo questo filtro perch� nella tabella sil_tabase il primo record
                //ha valore # e indica il nome della combo

                myselect=myselect+" ORDER BY tb_val ";
                System.out.println("query GridTabBase: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
//		Vector vdbr=dbcur.getAllRecord();
                int start = Integer.parseInt((String)h.get("start"));
                int stop = Integer.parseInt((String)h.get("stop"));
                Vector vdbr = dbcur.paginate(start, stop);
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
   	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
   	        }
   	}
}

public ISASRecord insert(myLogin mylogin,Hashtable h)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String codice=null;
        String valore=null;
	ISASConnection dbc=null;
	try {
		codice=(String)h.get("tb_cod");
                valore=(String)h.get("tb_val");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		ISASRecord dbr=dbc.newRecord("sil_tabase");
		Enumeration n=h.keys();
		while(n.hasMoreElements()){
			String e=(String)n.nextElement();
			dbr.put(e,h.get(e));
		}
		dbc.writeRecord(dbr);
		String myselect="Select * from sil_tabase where "+
			        "tb_cod='"+codice+"' AND "+
                                "tb_val='"+valore+"'";
		dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e1){
		System.out.println(e1);
		throw new SQLException("Errore eseguendo una insert() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}


public ISASRecord update(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	String codice=null;
        String valore=null;
	ISASConnection dbc=null;
	try {
		codice=(String)dbr.get("tb_cod");
                valore=(String)dbr.get("tb_val");
	}catch (Exception e){
		e.printStackTrace();
		throw new SQLException("Errore: manca la chiave primaria");
	}
	try{
		dbc=super.logIn(mylogin);
		dbc.writeRecord(dbr);
		String myselect="Select * from sil_tabase where "+
			        "tb_cod='"+codice+"' AND "+
                                "tb_val='"+valore+"'";
		dbr=dbc.readRecord(myselect);
		dbc.close();
		super.close(dbc);
		done=true;
		return dbr;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e1){
		System.out.println(e1);
		throw new SQLException("Errore eseguendo una update() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
		}
	}
}


public void delete(myLogin mylogin,ISASRecord dbr)
throws DBRecordChangedException, ISASPermissionDeniedException, SQLException {
	boolean done=false;
	ISASConnection dbc=null;
	try{
		dbc=super.logIn(mylogin);
		dbc.deleteRecord(dbr);
		dbc.close();
		super.close(dbc);
		done=true;
	}catch(DBRecordChangedException e){
		e.printStackTrace();
		throw e;
	}catch(ISASPermissionDeniedException e){
		e.printStackTrace();
		throw e;
	}catch(Exception e1){
		System.out.println(e1);
		throw new SQLException("Errore eseguendo una delete() - "+  e1);
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e2){System.out.println(e2);}
   	        }
   	}
}

	public Vector query_combo_tiposerv1(myLogin mylogin, Hashtable h) throws SQLException
	{
	  return query_combo_tiposerv(mylogin, h);
	}


	public Vector query_combo_tiposerv2(myLogin mylogin, Hashtable h) throws SQLException
	{
	  //return query_combo_tiposerv(mylogin, h);
		return query_combo_segnala(mylogin,h);	
	}

	public Vector query_combo_tiposerv(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "TIPOSERV");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_tiposerv - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_tiposerv - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_tiposerv

	public Vector query_combo_tipoass(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "TIPOASS");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_tipoass - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_tipoass - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_tipoass


	public Vector query_combo_incentiv(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "INCENTIV");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_incentiv - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_incentiv - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_incentiv

	public Vector query_combo_aztipazi(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "AZTIPAZI");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_aztipazi - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_aztipazi - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_aztipazi

	public Vector query_combo_azsetpro(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "AZSETPRO");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_azsetpro - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_azsetpro - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_azsetpro

	public Vector query_combo_azassimp1(myLogin mylogin, Hashtable h) throws SQLException{
	    return query_combo_azassimp(mylogin, h);
	}
	public Vector query_combo_azassimp2(myLogin mylogin, Hashtable h) throws SQLException{
	    return query_combo_azassimp(mylogin, h);
	}

	public Vector query_combo_azassimp(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "AZASSIMP");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_azassimp - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_azassimp - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_azassimp

	public Vector query_combo_azdispon1(myLogin mylogin, Hashtable h) throws SQLException
	{
	    return query_combo_azdispon(mylogin, h);
	}

	public Vector query_combo_azdispon2(myLogin mylogin, Hashtable h) throws SQLException
	{
	    return query_combo_azdispon(mylogin, h);
	}

	public Vector query_combo_azdispon(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "AZDISPON");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_azdispon - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_azdispon - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_azdispon

	public Vector query_combo_statusut(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "STATUSUT");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_statusut - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_statusut - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_statusut

	public Vector query_combo_modinser(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "MODINSER");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_modinser - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_modinser - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_modinser

	public Vector query_combo_segnala(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "SEGNALA");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_segnala - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_segnala - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_segnala

	public Vector query_combo_terapcsm(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "TERAPCSM");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_terapcsm - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_terapcsm - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_terapcsm

	public Vector query_combo_motsegna(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "MOTSEGNA");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_motsegna - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_motsegna - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_motsegna

	public Vector query_combo_progesil(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "PROGESIL");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_progesil - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_progesil - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_progesil

	public Vector query_combo_capaclav(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "CAPACLAV");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_capaclav - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_capaclav - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_capaclav

	public Vector query_combo_tipouten(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "TIPOUTEN");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_tipouten - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_tipouten - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_tipouten

	public Vector query_combo_naturdis(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "NATURDIS");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_naturdis - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_naturdis - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_naturdis

	public Vector query_combo_tipohand(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "TIPOHAND");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_tipohand - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_tipohand - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_tipohand

	public Vector query_combo_obblcivi(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "OBBLCIVI");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_obblcivi - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_obblcivi - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_obblcivi

	public Vector query_combo_tiporedd(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "TIPOREDD");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_tiporedd - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_tiporedd - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_tiporedd

	public Vector query_combo_servizi(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "SERVIZI");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: queryCombo_servizi - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: queryCombo_servizi - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_servizi


public Vector query_combo_parent(myLogin mylogin,Hashtable h) throws  SQLException {
	boolean done=false;
	ISASConnection dbc=null;
        String scr=" ";
	try{
		dbc=super.logIn(mylogin);
		String myselect="SELECT TRIM(codice) codice,TRIM(descrizione) descriz "+
			        "FROM parent ORDER BY descrizione ";
		System.out.println("query GridParent: "+myselect);
		ISASCursor dbcur=dbc.startCursor(myselect);
		Vector vdbr=dbcur.getAllRecord();
		dbcur.close();
		dbc.close();
		super.close(dbc);
		done=true;
		return vdbr;
	}catch(Exception e){
		e.printStackTrace();
		throw new SQLException("Errore eseguendo una query()  ");
	}finally{
		if(!done){
			try{
				dbc.close();
				super.close(dbc);
			}catch(Exception e1){System.out.println(e1);}
		}
	}
}


	private Vector queryComboParent(ISASConnection mydbc) throws  SQLException
	{
		boolean done = false;
		ISASCursor dbcur = null;

		try{
			String myselect = "SELECT * " +
                                " FROM parent" +
                                " ORDER BY descrizione";

	        System.out.println("TabBaseEJB: queryComboParent - myselect=[" + myselect + "]");

			dbcur = mydbc.startCursor(myselect);
			Vector vdbr = dbcur.getAllRecord();
			dbcur.close();

			ISASRecord recVuoto = mydbc.newRecord("parent");
			recVuoto.put("codice", "99");
			recVuoto.put("descriz", " ");
			if ((vdbr != null) && (vdbr.size() > 0))
			    vdbr.add(0, recVuoto);

			done = true;

			return vdbr;
		}catch(Exception e){
			System.out.println("TabBaseEJB: queryComboParent - Eccezione=" + e);
			throw new SQLException("Errore eseguendo una query()  ");
		}finally{
			if(!done){
				try{
					if (dbcur != null)
						dbcur.close();
					mydbc.close();
					super.close(mydbc);
				}catch(Exception e1){
				    System.out.println("TabBaseEJB: queryComboParent - Eccezione nella chiusura della connessione= " + e1);
				}
	   	    }
	   	}
	}// END queryComboParent

	public Vector query_combo_finanzia(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "FINANZIA");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: query_combo_finanzia - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: query_combo_finanzia - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_finanzia

	public Vector query_combo_libero1(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "LIBERO1");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: query_combo_libero1 - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: query_combo_libero1 - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_libero1
	
	public Vector query_combo_libero2(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "LIBERO2");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: query_combo_libero2 - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: query_combo_libero2 - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_libero2

	public Vector query_combo_libero3(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "LIBERO3");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: query_combo_libero3 - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: query_combo_libero3 - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_libero3

	public Vector query_combo_distrett(myLogin mylogin, Hashtable h) throws SQLException
	{
	      boolean done = false;
	      ISASConnection dbc = null;

	      try {
		  dbc = super.logIn(mylogin);

		  Vector vettdbr = (Vector)queryCombo(dbc, "DISTRETT");

		  dbc.close();
		  super.close(dbc);
		  done = true;

		  return vettdbr;
	      }catch(Exception e){
			      System.out.println("TabBaseEJB: query_combo_distrett - " + e);
			      throw new SQLException("Errore eseguendo una query()  ");
		      }finally{
			      if(!done){
				      try{
					      dbc.close();
					      super.close(dbc);
				      }catch(Exception e1){
					      System.out.println("TabBaseEJB: query_combo_distrett - Eccezione nella chiusura della connessione= " + e1);
				      }
		  }
        }
    }// END query_combo_distrett

	}

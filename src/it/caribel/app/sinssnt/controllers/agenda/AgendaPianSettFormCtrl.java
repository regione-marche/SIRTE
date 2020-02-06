package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.app.sinssnt.bean.modificati.AgendaSettEJB;
import it.caribel.app.sinssnt.controllers.CaribelAggiornaCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForBinding;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class AgendaPianSettFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = 1L;

	private String myKeyPermission = "PIANOASS";
	private AgendaSettEJB myEJB = new AgendaSettEJB();
	
	public static final String myPathZul = "/web/ui/sinssnt/agenda/agendaPianSettForm.zul";
	protected String pathGridZul = "/web/ui/sinssnt/interventi/accessiGrid.zul";

	private ISASUser iu;

	private final int WAIT = 0;
    private final int INSERT = 1;
    private final int UPDATE_DELETE = 2;
    private final int CONSULTA = 3;

    Hashtable hashDaPassare=new Hashtable();

	private CaribelListbox caribellbAgendaAssistito;
	private CaribelListbox caribellbAgenda;


	private Label riepilogoAssistito;

	private CaribelTextbox riepilogoAltroAssistito;

	protected Integer statoPian;
	Vector<String>[] add    = new Vector[CostantiAgenda.NUMFASCIEORARIE];
	Vector<String>[] remove = new Vector[CostantiAgenda.NUMFASCIEORARIE];
	
	Vector<String> vMat = new Vector<String>();
	Vector<String> vPom = new Vector<String>();
	Vector<String> vMat_rm = new Vector<String>();
	Vector<String> vPom_rm = new Vector<String>();

	private String data_input;
	
	int modifica = 0;
    
	public void doInitForm() {
		try {
			super.initCaribelFormCtrl(myEJB,myKeyPermission);
			super.setMethodNameForQuery("query_agenda");
			super.setMethodNameForInsert("insert_agenda");
			super.setMethodNameForUpdate("update_agenda");
			
			hashDaPassare.putAll(arg);
//			hashDaPassare.put("esecutore", value)
			caribellbAgenda.setItemRenderer(new AgendaGridItemRenderer());
			caribellbAgendaAssistito.setItemRenderer(new AgendaGridItemRenderer());
			
			caribellbAgenda.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
				public void onEvent(Event event) throws Exception {
					executeSelect();
				}});
//			caribellbAgendaAssistito.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
//				public void onEvent(Event event) throws Exception {
////					String chiave = event.getTarget().getId();
////					executeAddRemove(chiave);
//				}});
			jbInit();
			String riepilogo = Labels.getLabel("accessiPrestazioni.assistito")+": "+arg.get("n_cartella")+" "+arg.get("assistito") +
					" - "+ Labels.getLabel("accessiPrestazioni.contatto")+": " + arg.get("n_progetto");
			if(!ManagerProfile.isConfigurazioneMarche(getProfile())){
				riepilogo += " - "+ Labels.getLabel("common.obiettivo")+": "+arg.get("cod_obbiettivo") +
						" - "+ Labels.getLabel("common.intervento")+": "+arg.get("n_intervento");
			}
			for (int i = 0; i < add.length; i++) {
				add[i]    = new Vector<String>();
				remove[i] = new Vector<String>();
			}
			
			gestionePianificazionePAI_CP();
			
			riepilogoAssistito.setValue(riepilogo);
			doFreezeForm();
			iu = CaribelSessionManager.getInstance().getIsasUser();	
		}catch(Exception e){
			doShowException(e);
		}
	}

	protected void executeSelect() throws ISASMisuseException {
		if(caribellbAgenda.getSelectedIndex()>-1){
			Object o = caribellbAgenda.getModel().getElementAt(caribellbAgenda.getSelectedIndex());
			String val = "";
			if(o instanceof ISASRecord){
				val = (String) ((ISASRecord) o).get("valore") + " "+ Labels.getLabel("cartella.reperibilita.indirizzo") + ":" + (String) ((ISASRecord) o).get("indirizzo");
			}else if(o instanceof Hashtable){
				val = (String) ((Hashtable) o).get("valore") + " " + Labels.getLabel("cartella.reperibilita.indirizzo") + ":" +(String) ((Hashtable) o).get("indirizzo");
			}
			riepilogoAltroAssistito.setValue(val);
		}
	}
	
	public void onDoubleClickedCell(ForwardEvent event) throws Exception {
		if(statoPian != this.CONSULTA){
			Component comp = event.getOrigin().getTarget();
			Object o = comp.getAttribute("dbName", true);
			if(o.toString().length()<2){
				executeAddRemove(o.toString());
			}
		}
	}
	
	protected void executeAddRemove(String y) throws ISASMisuseException {
		int x = caribellbAgendaAssistito.getSelectedIndex();
		int gg = -3;
		try {
			gg = Integer.parseInt(y);
			gg = gg+2;
		} catch (Exception e) {
			doShowException(e);
		}
		CaribelListModel clmAssistito = (CaribelListModel) caribellbAgendaAssistito.getModel();
//		String y = caribellbAgendaAssistito.getSelectedColumn();
		if(x>-1){
			String valore = (String)clmAssistito.getFromRow(x, y);
			logger.debug("inserisco la prestazione in cella valore==>["+valore+"]");

			//String nuova_prest=((String)hashDaPassare.get("pi_prest_cod")).trim();
			String nuova_prest=((String)hashDaPassare.get("prestazioni")).trim();
			logger.debug("inserisco la prestazione in cella prestaz==>["+nuova_prest+"]");
			//gb 05/09/07                if((x==0 || x==1) && y!=0)
			if((x>=0 && x<CostantiAgenda.NUMFASCIEORARIE) && !y.isEmpty() && !y.equals("assistito")){ //gb 05/09/07
				if (valore == null || valore.isEmpty())
					clmAssistito.setValueAt(nuova_prest, x, y);
				else{
					if(!isPresente(nuova_prest,valore)){
						clmAssistito.setValueAt(valore+"-"+nuova_prest, x, y);
						modifica++;
					}else{
						if(statoPian==UPDATE_DELETE)//{
							UtilForUI.standardExclamation(Labels.getLabel("agendaPianSett.msg.prestazioneRimossa", new String[]{nuova_prest}));
//							new it.pisa.caribel.swing2.cariInfoDialog(null,"La Prestazione: ["+nuova_prest+"] verrà rimossa","Attenzione!").show();
						StringTokenizer st_new=new StringTokenizer(nuova_prest,"-");
						while(st_new.hasMoreTokens()){
							String tok_new=(st_new.nextToken()).trim();
							int inizio=valore.indexOf(tok_new);
							//logger.debug("INIZIO =="+inizio);
							int fine=inizio+tok_new.length();
							//logger.debug("FINE =="+fine);
							if(inizio>=0){
							if (inizio!=0)inizio=inizio-1;
							StringBuffer bf=new StringBuffer(valore);
							bf.replace(inizio,fine,"");
							String w_bf=bf.toString();
							if(w_bf.startsWith("-"))
								w_bf=w_bf.substring(1);
							clmAssistito.setValueAt(w_bf, x, y);
							valore = w_bf;
							//devo controllare se presente e rimuoverlo!
							}
						}
						if(add[x].contains(""+gg))add[x].remove(""+gg);
//						if (x==0){
//							if(vMat.contains("M"+gg))vMat.remove("M"+gg);
//
//						}else if (x==1){
//							if(vPom.contains("P"+gg))vPom.remove("P"+gg);
//
//						}
						if(statoPian==UPDATE_DELETE){
							remove[x].add(""+gg);
//
//							if (x==0)
//								vMat_rm.add("M"+gg);
//							else if (x==1)
//								vPom_rm.add("P"+gg);
						}
						hashDaPassare.put("updAppuntam","false");
						caribellbAgendaAssistito.setModel(clmAssistito);
						caribellbAgendaAssistito.invalidate();
						modifica--;
						return;
					}
				}
				add[x].add(""+gg);
//				//logger.debug("aggiungo--> M"+y+" P"+y);
//				if (x==0){
//					vMat.add("M"+gg);
//					//logger.debug("aggiungo--> M"+y);
//				}else if (x==1){
//					vPom.add("P"+gg);
//					//logger.debug("aggiungo--> P"+gg);
//				}
			hashDaPassare.put("updAppuntam","false");
			}
		}
		caribellbAgendaAssistito.setModel(clmAssistito);
		caribellbAgendaAssistito.invalidate();
	}	

	boolean isPresente(String nuova_prest,String stringa){
		StringTokenizer st_new=new StringTokenizer(nuova_prest,"-");
		while(st_new.hasMoreTokens()){
			String tok_new=(st_new.nextToken()).trim();
			StringTokenizer st=new StringTokenizer(stringa,"-");
			while(st.hasMoreTokens()){
				String tok=(st.nextToken()).trim();
				if(tok_new.equals(tok))
					return true;
			}
		}
		return false;
	}
	
	private void caricaPerAgenda(){
		logger.info("caricaPeragenda");
		for(int x=0;x<caribellbAgendaAssistito.getItemCount();x++){ // JCariTableAgendaAssistito.getRowCount()
			for(int y=0;y<=6;y++){
				String valore = (String)((CaribelListModel) caribellbAgendaAssistito.getModel()).getFromRow(x, String.valueOf(y));
				String nuova_prest=((String)hashDaPassare.get("prestazioni")).trim();
				logger.info("caricaPeragenda val "+valore);
				logger.info("caricaPeragenda prest "+nuova_prest);
				logger.info("caricaPeragenda x,y="+x+","+y);
				if(isPresente(nuova_prest,valore)){
					remove[x].add(""+y);
					add[x].add(""+y);
//					if (x==0){
//						vMat_rm.add("M"+y);
//						vMat.add("M"+y);
//					}else if (x==1){
//						vPom_rm.add("P"+y);
//						vPom.add("P"+y);
//					}
				}
			}
		}
	}
	public int preparaVettoreHash(Vector<Vector<String>> vh){
		for (int i = 0; i < add.length; i++) {
			vh.add(i, add[i]);
		}
//		vh.add(0,vMat);
//		vh.add(1,vPom);
		return 1;
	}
	public int preparaVettoreHashRemove(Vector<Vector<String>> vh){
		for (int i = 0; i < remove.length; i++) {
			vh.add(i, remove[i]);
		}
//		vh.add(0,vMat_rm);
//		vh.add(1,vPom_rm);
		return 1;
	}
	
	private void jbInit() throws Exception {
		((Window)getForm()).setTitle(Labels.getLabel("agendaPianSett.title", new String[]{hashDaPassare.get("esecutore")+""}));
//		caribellbPian.clear();
//		caribellbAgenda.clear();
		statoPian = (Integer) arg.get("statoPianoAcc");//ISASUtil.getValoreStringa(arg, "statoPianoAcc");
//		statoPian = (String) arg.get("statoPianoAcc");
		ExecSelect();
		logger.info("InitForm / prestazioni (2): " + (String)hashDaPassare.get("prestazioni"));
		String prestazioni=(String)hashDaPassare.get("prestazioni");
//		StringTokenizer st=new StringTokenizer(prestazioni,"-");
//		String firstPrest=st.nextToken();
//		logger.info("InitForm / firstPrest: " + firstPrest);
	//FIXME gestione dei renderer
	//		PianSettTableCellRenderer renderer = new PianSettTableCellRenderer(caribellbAgendaAssistito,firstPrest);
	//		//(String)hashDaPassare.get("pi_prest_cod"));
	//		logger.info("InitForm / dopo la new PianSettTableCellRenderer(..)");
	//		try
	//		{
	//			caribellbAgendaAssistito.setDefaultRenderer(Class.forName("java.lang.String"), renderer);
	//		}
	//		catch( ClassNotFoundException ex )
	//		{
	//			System.exit( 0 );
	//		}
//		logger.info("InitForm / dopo la caribellbAgendaAssistito.setDefaultRenderer(..)");
//		logger.info("InitForm / dopo la caribellbAgenda.setColumnSizes()");
//		settaRendererTabellaAgenda();
		logger.info("InitForm / dopo la settaRendererTabellaAgenda()");
	}

	@SuppressWarnings("rawtypes")
	private int ExecSelect() throws Exception {
		caribellbAgenda.setModel(new CaribelListModel());
		int count = 0;
		logger.info("JFrameAgendaPianSett **** HASH che invio per selezAgenda..."+hashDaPassare.toString());
		Object o= invokeGenericSuEJB(myEJB, hashDaPassare, "query_agenda"); //db.selezAgenda(hashDaPassare);
		Vector griglia=new Vector();
		if(o==null){
			UtilForUI.standardExclamation(Labels.getLabel("common.msg.ko.notification"));
			return -1;
		}else{
			if(((Vector)o).size()<=0){
				caricoAssistito();
				return 0;
			}else{
				griglia=(Vector)o;
			}
		}

		Enumeration en=griglia.elements();
		String chiave_new="";
		String chiave_old="";
		boolean w_due=false;
		boolean w_prima=true;
		int riga=0;
		int numRighe=0;
		Hashtable ht=new Hashtable();
		String assistito = (String)hashDaPassare.get("n_cartella") + "-" +
				(String)hashDaPassare.get("n_progetto") + "-" +
				( (String)hashDaPassare.get("cod_obbiettivo") ).trim()+ "-" +
				(String)hashDaPassare.get("n_intervento") + "-" +
				formDate(hashDaPassare.get("pa_data"),"gg/mm/aaaa"); //gb 30/08/07
		//logger.info("ASSISTITO==>["+assistito+"]");
		boolean presente=false;
		Hashtable[] righeAssistito = new Hashtable[CostantiAgenda.NUMFASCIEORARIE];
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			Hashtable h=is.getHashtable();
			//gb 30/08/07          chiave_new=((Integer)h.get("n_cartella")).toString()+"-"+((Integer)h.get("n_contatto")).toString()+"-"+formDate(h.get("as_data"),"gg/mm/aaaa");
			chiave_new = ((Integer)h.get("n_cartella")).toString() + "-" +
					((Integer)h.get("n_contatto")).toString() + "-" +
					((String)h.get("cod_obbiettivo")).trim() + "-" +
					((Integer)h.get("n_intervento")).toString() + "-" +
					formDate(h.get("as_data"),"gg/mm/aaaa"); //gb 30/08/07
			//logger.info("ASSISTITO che carico==>["+chiave_new+"]");
			if(assistito.equals(chiave_new)){
				presente=true;
			}
			riga = ((Integer)h.get("as_orario")).intValue();
			if (chiave_new.equals(chiave_old)){
				righeAssistito[riga] = h;
			}else{
				caricaRighe(righeAssistito);
				righeAssistito = new Hashtable[CostantiAgenda.NUMFASCIEORARIE];
				righeAssistito[riga] = h;
				chiave_old=chiave_new;
			}
		}
		caricaRighe(righeAssistito);
/*			
			
			if (w_prima==true){
				caricaRiga(h);
				ht=(Hashtable) h.clone();
				w_prima=false;
				w_due=false;
				chiave_old=chiave_new;
				//devo controllare se l'assistito a cui
				//sto inserendo le prestazioni è gia presente
				//nella pianificazioni in modo da i
				//onserire eventualm le righe vuote
				if(assistito.equals(chiave_new)){
					presente=true;
				}
			}else{
				if (chiave_new.equals(chiave_old)){
					caricaRiga(h);
					w_due=true;
				}else{
					chiave_old=chiave_new;
					//devo controllare se l'assistito a cui sto inserendo le prestazioni è gia presente
					//nella pianificazioni in modo da inserire eventualmente le righe vuote
					if(assistito.equals(chiave_new)){
						presente=true;
					}
					if (w_due!=true){
						casoRigaVuota(ht);
						w_due=true;
					}
					ht=h;
					caricaRiga(h);
					w_due=false;
				}
			}
		}

		if (w_due!=true){
			casoRigaVuota(ht);
		}
*/
		if(presente==false) 
			caricoAssistito();
		else 
			caricoAssistitoInizio();
		return count;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void caricaRighe(Hashtable[] righeAssistito) {
		boolean found = false;
		Hashtable perRigheVuote = null;
		//cerco se è valorizzata almeno una riga
		int i = 0;
		for (; i < righeAssistito.length && !found; i++) {
			found = found || righeAssistito[i]!=null;
		}
		if(!found){
			//se non ho da visualizzare nulla ritorno
			return;
		}
		//mi salvo una riga sicuramente piena come template per generare le righe vuote 
		perRigheVuote = righeAssistito[--i];
		Hashtable h;
		//ciclo su tutte le fasce orarie
		for (i = 0; i < righeAssistito.length; i++) {
			h = righeAssistito[i];
			if(h == null){
				//carico la riga vuota
				perRigheVuote.put("as_orario_perRigheVuote", i);
				caricaRigaVuota(perRigheVuote, caribellbAgenda.getModel().getSize());
				perRigheVuote.remove("as_orario_perRigheVuote");
			}else{
				//carico una riga nella griglia con i valori di h
				caricaRiga(h);
			}
			
		}
			
	}
	
	private void caricoAssistito() {
		Vector model = new Vector();
		Hashtable rigaAssistito;
		String assistito = (String)hashDaPassare.get("assistito");
		for (int i = 0; i < CostantiAgenda.NUMFASCIEORARIE; i++) {
			rigaAssistito = new Hashtable();
			rigaAssistito.put("assistito", assistito);
			rigaAssistito.put("matt_pom", Labels.getLabel("agenda.fasce."+i));
			rigaAssistito.put("as_orario", i);
			model.add(rigaAssistito);
		}
//		Hashtable<String, String> h1 = new Hashtable<String, String>();
//		h1.put("assistito", (String)hashDaPassare.get("assistito"));
//		h1.put("matt_pom", "M");
////		(String)hashDaPassare.get("n_cartella")+"##"+(String)hashDaPassare.get("n_progetto")+"##" +((String)hashDaPassare.get("cod_obbiettivo")).trim()+"##" +(String)hashDaPassare.get("n_intervento")+"##" +(String)hashDaPassare.get("pa_data")+"##"+" ",0
//		Hashtable<String, String> h2 = new Hashtable<String, String>();
//		h2.put("indirizzo", (String)hashDaPassare.get("indirizzo"));
//		h2.put("matt_pom", "P");
//		model.add(h1);
//		model.add(h2);
		caribellbAgendaAssistito.setModel(new CaribelListModel(model));
	}

	private	String formDate(Object dataI,String formato){
		return formDate(dataI,formato,false);//false =>	aaaammgg
	}
	
	private	String formDate(Object dataI,String formato,boolean it){
		String giorno="00";
		String mese="00";
		String anno="0000";
		String data="";
		if(dataI!=null){
			if(dataI instanceof java.sql.Date) data=((java.sql.Date)dataI).toString();
			else if(dataI instanceof String) data=(String)dataI;
		}
		//it=false =>	aaaammgg
		//it=true	=> ggmmaaaa
		if(data!=null	&& !data.equals("")){

			if(data.charAt(2)=='-' ||data.charAt(2)=='/')//gg-mm-aaaa
			{
				giorno=data.substring(0,2);
				mese=data.substring(3,5);
				anno=data.substring(6,10);
			}else	 if(data.charAt(4)=='-'	||data.charAt(4)=='/')//aaaa-mm-gg
			{
				giorno=data.substring(8,10);
				mese=data.substring(5,7);
				anno=data.substring(0,4);
			}else	//ggmmaaaa aaaammgg
			{	if(it==false)//aaaammgg
			{
				giorno=data.substring(6,8);
				mese=data.substring(4,6);
				anno=data.substring(0,4);
			}
			else//ggmmaaaa
			{
				giorno=data.substring(0,2);
				mese=data.substring(2,4);
				anno=data.substring(4,8);
			}
			}
		}
		if(formato.equals("gg-mm-aaaa"))
			return giorno+"-"+mese+"-"+anno;
		else if(formato.equals("aaaa-mm-gg"))
			return anno+"-"+mese+"-"+giorno;
		else if(formato.equals("ggmmaaaa"))
			return giorno+mese+anno;
		else if(formato.equals("aaaammgg"))
			return anno+mese+giorno;
		else if(formato.equals("gg/mm/aaaa"))
			return giorno+"/"+mese+"/"+anno;
		else if(formato.equals("aaaa/mm/gg"))
			return anno+"/"+mese+"/"+giorno;
		else return data;
	}
	
	private void casoRigaVuota(Hashtable ht){
		int val=((Integer)ht.get("as_orario")).intValue();
		int riga=0;
		int numRighe=0;
		if(val==0){
			ht.put("as_orario",new Integer(1));
			//   riga=1;
			numRighe=caribellbAgenda.getItemCount(); //this.cariStringTableModelAgenda.getNumRows();
			riga=numRighe;
			caricaRigaVuota(ht,numRighe);
		}else{
			ht.put("as_orario",new Integer(0));
			numRighe=this.caribellbAgenda.getItemCount()-1;
//			Listitem st1=caribellbAgenda.getItemAtIndex(numRighe);
//			caribellbAgenda.removeItemAt(numRighe);
			caricaRigaVuota(ht,numRighe);
//			((CaribelListModel) caribellbAgenda.getModel()).add(numRighe+1, st1);
		}
	}

	private void caricoAssistitoInizio(){
//		int col_ca=caribellbAgenda.getColumnLocationByName("n_cartella");
//		int col_co=caribellbAgenda.getColumnLocationByName("n_contatto");
//		int col_cob=caribellbAgenda.getColumnLocationByName("cod_obbiettivo");
//		int col_in=caribellbAgenda.getColumnLocationByName("n_intervento");
//		int col_dt=caribellbAgenda.getColumnLocationByName("as_data");
		CaribelListModel clm = (CaribelListModel) caribellbAgenda.getModel();
		int numRighe=clm.getSize();
		String st1="";
		String st2="";
		
		Object l1 = null;
		Object l2 = null;
		Object l3 = null;
		Object l4 = null;

		CaribelListModel clmAssistito = new CaribelListModel();
		for(int r=0;r<numRighe;r++){
			if (((String)hashDaPassare.get("n_cartella")).equals(clm.getFromRow(r, "n_cartella").toString()))//clm.getFromRow(r,col_ca)))
				if (((String)hashDaPassare.get("n_progetto")).equals(clm.getFromRow(r,"n_contatto").toString()))
					//gb 30/08/07 *******
					if ((((String)hashDaPassare.get("cod_obbiettivo")).trim()).equals(clm.getFromRow(r,"cod_obbiettivo").toString()))
						if (((String)hashDaPassare.get("n_intervento")).equals(clm.getFromRow(r,"n_intervento").toString()))
							//gb 30/08/07: fine *******
							//gb 30/08/07                if (((String)hashDaPassare.get("skpa_data")).equals((String)clm.getFromRow(r,col_dt)))
							if (((String)hashDaPassare.get("pa_data")).equals(UtilForBinding.getValueForIsas((java.sql.Date) clm.getFromRow(r,"as_data")))){
								//logger.debug("sono in caricoAssistitoInizio...riga="+r+"--->"+cariStringTableModelAgenda.getRow(r));
//								st1=cariStringTableModelAgenda.getRow(r);
//								st2=cariStringTableModelAgenda.getRow(r+1);
//								cariStringTableModelAgenda.removeRow(r);
//								cariStringTableModelAgenda.removeRow(r);
								int i = 0;
								for (i = 0; i < CostantiAgenda.NUMFASCIEORARIE; i++) {
									clmAssistito.add(i,clm.getElementAt(r+i));
								}
								i--;
								for (; i >=0 ; i--) {
									clm.remove(r+i);
								}
//								l1=clm.getElementAt(r);
//								l2=clm.getElementAt(r+1);
//								clm.remove(r+1);
//								clm.remove(r);
//								l1=caribellbAgenda.getItemAtIndex(r);
//								l2=caribellbAgenda.getItemAtIndex(r+1);
								break;
							}
		}
//		clm.add(0, l1);
//		clm.add(1, l2);
		caribellbAgendaAssistito.setModel(clmAssistito);
//		cariStringTableModelAssistito.insertRowAt(st1,0);
//		cariStringTableModelAssistito.insertRowAt(st2,1);
//		caribellbAgendaAssistito.setColumnSizes();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int caricaRigaVuota(Hashtable hash,int riga)
	{
		int count=0;
		Hashtable nuovaRiga = new Hashtable<String, String>();
//		StringBuffer st = new StringBuffer();
//		// inserimento di un nuovo record
//		String chiavi = caribellbAgenda.getColumnDB(sepChar);
//		StringTokenizer rowChiavi = new StringTokenizer(chiavi,sepChar);
		Enumeration keys = hash.keys();
		while(keys.hasMoreElements()){//rowChiavi.hasMoreTokens()){
			String rowItem = (String) keys.nextElement(); //rowChiavi.nextToken();
			if(rowItem.equals("matt_pom")){
				int val=riga%CostantiAgenda.NUMFASCIEORARIE;
				nuovaRiga.put(rowItem, Labels.getLabel("agenda.fasce."+val));
				nuovaRiga.put("as_orario",  new Integer(val));
			}else if(rowItem.equals("valore")){
				String val;
				if(ManagerProfile.isConfigurazioneMarche(getProfile())){
					val = hash.get("assistito")+
							" cartella= "+hash.get("n_cartella")+
							"; contatto= "+hash.get("n_contatto")+
							"; piano del "+formDate(hash.get("as_data"),"gg/mm/aaaa");
				}else{
					val = hash.get("assistito")+
							" cartella= "+hash.get("n_cartella")+
							"; contatto= "+hash.get("n_contatto")+
							"; obiettivo= "+hash.get("cod_obbiettivo")+ //gb 30/08/07
							"; intervento= "+hash.get("n_intervento")+ //gb 30/08/07
							"; piano del "+formDate(hash.get("as_data"),"gg/mm/aaaa");
				}
				nuovaRiga.put(rowItem, val);
			}
			
			if(rowItem.equals("assistito")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}if(rowItem.equals("indirizzo")){
				nuovaRiga.put(rowItem, hash.get(rowItem));
			}else if(rowItem.equals("n_cartella")){
//					int val=((Integer)hash.get("n_cartella")).intValue();
//					st.append(""+val);
					nuovaRiga.put(rowItem, hash.get(rowItem));
				}else if(rowItem.equals("n_contatto")){nuovaRiga.put(rowItem, hash.get(rowItem));
//					int val=((Integer)hash.get("n_contatto")).intValue();
//					st.append(""+val);
				}else if(rowItem.equals("cod_obbiettivo")){nuovaRiga.put(rowItem, hash.get(rowItem));
//					String val=(String)hash.get("cod_obbiettivo");
//					st.append(""+val.trim());
				}else if(rowItem.equals("n_intervento")){nuovaRiga.put(rowItem, hash.get(rowItem));
//					int val=((Integer)hash.get("n_intervento")).intValue();
//					st.append(""+val);
				}else if(rowItem.equals("as_data")){nuovaRiga.put(rowItem, hash.get(rowItem));
//					String mydate=formDate(hash.get(rowItem),"gg/mm/aaaa");// data in JDBC
//					if(mydate.equals("00000000")|| mydate.trim().equals("") || mydate.equals("01/01/1000"))
//						st.append(" ");
//					else {
//						st.append(mydate);
					}
//				}else
//					st.append(" ");
//			} else {
//				//la tabella non contiene la chiave
//				st.append(" ");
//			}
//			if (rowChiavi.hasMoreTokens())
//				st.append(sepChar);
		}//end while
//		caribellbAgenda.insertRowAt(st.toString(),riga);

		//è una riga vuota devo inizializzare as_orario con il valore corretto
		nuovaRiga.put("as_orario", hash.get("as_orario_perRigheVuote"));
		
		((CaribelListModel) caribellbAgenda.getModel()).add(riga, nuovaRiga);

		return 1;
	}//end caricaRigaVuota

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private int caricaRiga(Hashtable hash){
		//inserimento dati dai controlli alla griglia
		logger.trace("carica la riga "+hash.toString());
		int count=0;
		String val;
		if(ManagerProfile.isConfigurazioneMarche(getProfile())){
			val = hash.get("assistito")+
					" cartella= "+hash.get("n_cartella")+
					"; contatto= "+hash.get("n_contatto")+
					"; piano del "+formDate(hash.get("as_data"),"gg/mm/aaaa");
		}else{
			val = hash.get("assistito")+
					" cartella= "+hash.get("n_cartella")+
					"; contatto= "+hash.get("n_contatto")+
					"; obiettivo= "+hash.get("cod_obbiettivo")+ //gb 30/08/07
					"; intervento= "+hash.get("n_intervento")+ //gb 30/08/07
					"; piano del "+formDate(hash.get("as_data"),"gg/mm/aaaa");
		}

		hash.put("valore", val);
//		cariStringTableModelAgenda.addRow(st.toString());
		((CaribelListModel) caribellbAgenda.getModel()).add(hash);
		//logger.debug("CARICO LA RIGA ..."+st.toString());
		return 1;
	}//end caricaRiga
	
	@Override
	protected boolean doValidateForm() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}
	
	protected boolean doSaveForm() throws Exception{
		data_input="";
		if(statoPian.equals(CONSULTA)){
			Clients.showNotification(Labels.getLabel("agendaPianSett.msg.statoConsultazione"), "error", null,"middle_center", 2500);
			return false;
		}
		//TEST bargi 12/06/2012  if(((String)hashDaPassare.get("updAppuntam")).equals("false"))
		if(hashDaPassare.get("data_input")==null || ((String)hashDaPassare.get("data_input")).equals("")){
			//        	String mess="Vuoi che sia aggiornata la tua AGENDA (solo per le settimane caricate)?\n"+
			//             "Inserisci la data di inizio aggiornamento.";
			//             String titolo="Attenzione!";
			//              JCariDateInputDialog dial = new JCariDateInputDialog(null,this,titolo,mess,70);//70
			//             String data =  procdate.getitaDate();
			//             data_input =dial.showMsg(data);
			//             if(data_input!=null && data_input.equals("/__/")){
			//            	 data_input=null;
			//             }
			//             if(data_input!=null){
			//            	 hashDaPassare.put("data_input",data_input);
			//             }
			Hashtable ht = new Hashtable<String, String>();
			String mess=Labels.getLabel("agendaPianSett.msg.chiediDataInserimentoinAgenda");
			ht.put("mess", mess);
			ht.put("titolo", Labels.getLabel("messagebox.attention"));
			if(hashDaPassare.containsKey("minDate") ){
				ht.put("dataProposta", UtilForBinding.getDateFromIsas((String) hashDaPassare.get("minDate")));
			}
			Executions.getCurrent().createComponents(PannelloDataFormCtrl.myZul, self, ht);
			return false;
		}
		return doSuperSaveForm();
	}

	@SuppressWarnings("rawtypes")
	private boolean doSuperSaveForm() {
		Vector<Vector<String>> vh = new Vector();
		preparaVettoreHash(vh);
		hashDaPassare.put("peragenda",vh); 
		hashDaPassare.remove("assistito");
		hashDaPassare.remove("indirizzo");
		String prog_piano="";
		Object o=null;
		try {
			if(statoPian.equals(INSERT)){
				o=invokeGenericSuEJB(myEJB, hashDaPassare, "insert_agenda");
			}else if(statoPian.equals(UPDATE_DELETE)){
				Vector<Vector<String>> vh_rm = new Vector();
				preparaVettoreHashRemove(vh_rm); 
				hashDaPassare.put("peragenda_rm",vh_rm);
				if(((String)hashDaPassare.get("updAppuntam")).equals("false"))//vuol dire che prima non ho aggiornato l'agenda ma lo faccio ora
					if(ISEmpty(vh)&& ISEmpty(vh_rm)){
						caricaPerAgenda();
						Vector<Vector<String>> v1=new Vector();
						Vector<Vector<String>> v2=new Vector();
						preparaVettoreHash(v1);
						preparaVettoreHashRemove(v2);
						hashDaPassare.put("peragenda_rm",v2);
						hashDaPassare.put("peragenda",v1); 
					}
				logger.info("****HASH che invio per updateAgenda..."+hashDaPassare.toString());
				o=invokeGenericSuEJB(myEJB, hashDaPassare, "update_agenda");
				//db.UpdateAgenda(hashDaPassare);
			}
		} catch (Exception e) {
			doShowException(e);
		}
		
		if (o!=null){
			Hashtable hRit=(Hashtable)o;
			//bargi 02/07/2007       
			if(hRit.get("msg")!=null && !hRit.get("msg").toString().equals("")){
				logger.debug("Il caricamento dell'agenda risponde: " + hRit.get("msg"));
//	            Map params = new HashMap();
//	            params.put("width", 500);
//				UtilForUI.standardExclamation((String)hRit.get("msg"), params);
				Clients.showNotification(Labels.getLabel("form.save.ok.notification"),"info", null,"middle_center",2500);
			//               new it.pisa.caribel.swing2.cariInfoDialog(null,(String)hRit.get("msg"),"Attenzione!").show();
			}
			prog_piano=(String)hRit.get("prog");
		}else{
			if(statoPian.equals(CONSULTA)){
				Clients.showNotification(Labels.getLabel("agendaPianSett.msg.statoConsultazione"), "error", null,"middle_center", 2500);
			}else{
				Clients.showNotification(Labels.getLabel("form.save.ko.notification"), "error", null,"middle_center",2500);
			}
//			UtilForUI.standardExclamation(Labels.getLabel("common.msg.ko.notification"));
			//           new it.pisa.caribel.swing2.cariInfoDialog(null,"Operazione non riuscita.","Attenzione!").show();
			return false;
		}
		Object parCtrl = self.getParent().getAttribute(MY_CTRL_KEY);
		if (parCtrl instanceof CaribelAggiornaCtrl){
			((CaribelAggiornaCtrl) parCtrl).aggiornaDatiEsterni();
		}
		self.detach();
		return true;
	}

	  private boolean ISEmpty(Vector v){
		  Enumeration en=v.elements();
	      int elem=0;
	      while(en.hasMoreElements()){
	      	Vector vt=(Vector)en.nextElement();
	      	elem+=vt.size();
	      }
	      if(elem==0)return true;
	      else return false;   
	  }

	public void setDataInput(String data_input) {
		this.data_input = data_input;
		hashDaPassare.put("data_input", data_input);
		doSuperSaveForm();
	}
	
	protected void gestionePianificazionePAI_CP() throws ISASMisuseException {
		if(hashDaPassare.containsKey("pianificati")){
			//ci sono prestazioni che non sono ancora state pianificate ma per le quali è stata definita una pianificazine in fase di PAI/CP 
			StringTokenizer prestazioni = new StringTokenizer((String) hashDaPassare.get("prestazioni"),"-");
			StringTokenizer pianificati = new StringTokenizer((String) hashDaPassare.get("pianificati"),"-");
			while (pianificati.hasMoreTokens()) {
				String prestazione = prestazioni.nextToken();
				StringTokenizer panificato =  new StringTokenizer(pianificati.nextToken(),"#");
				for (int fascia = 0; fascia < CostantiAgenda.NUMFASCIEORARIE; fascia++) {
					//ciclo sulle fasce orarie
					String settimana = panificato.nextToken();
					for (int i = 0; i < settimana.length(); i++) {
						if(settimana.charAt(i)=='S'){
							//per ogni valore S che trovo richiedo l'inserimento di quella prestazione nel giorno e nella fascia specficato
							executeAdd(fascia+"", i+"", prestazione);
						}
					}
				}
			}
		}
	}
	
	protected void executeAdd(String fascia, String y, String prestazione) throws ISASMisuseException {
		int x = Integer.parseInt(fascia);
		int gg = -3;
		try {
			gg = Integer.parseInt(y);
			gg = gg+2;
		} catch (Exception e) {
			doShowException(e);
		}
		CaribelListModel clmAssistito = (CaribelListModel) caribellbAgendaAssistito.getModel();
		if(x>-1){
			String valore = (String)clmAssistito.getFromRow(x, y);
			logger.debug("inserisco la prestazione in cella valore==>["+valore+"]");

			String nuova_prest= prestazione.trim();
			logger.debug("inserisco la prestazione in cella prestaz==>["+nuova_prest+"]");
			if((x>=0 && x<CostantiAgenda.NUMFASCIEORARIE) && !y.isEmpty() && !y.equals("assistito")){ //gb 05/09/07
				if (valore == null || valore.isEmpty())
					clmAssistito.setValueAt(nuova_prest, x, y);
				else{
					if(!isPresente(nuova_prest,valore)){
						clmAssistito.setValueAt(valore+"-"+nuova_prest, x, y);
					}
				}
				if(!add[x].contains(""+gg)){
					add[x].add(""+gg);
				}
			hashDaPassare.put("updAppuntam","false");
			}
		}
		caribellbAgendaAssistito.setModel(clmAssistito);
		caribellbAgendaAssistito.invalidate();
	}
}

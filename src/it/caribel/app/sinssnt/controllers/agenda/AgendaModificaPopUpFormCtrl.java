package it.caribel.app.sinssnt.controllers.agenda;


import it.caribel.app.sinssnt.bean.AgendaEditEJB;
import it.caribel.app.sinssnt.bean.modificati.AgendaEJB;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.util.UtilForComponents;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.profile2.profile;
import it.pisa.caribel.util.procdate;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;

@SuppressWarnings({"rawtypes","unchecked"})
public class AgendaModificaPopUpFormCtrl extends AgendaGenericPopupFormCtrl {

	private static final long serialVersionUID = 8110873716088864092L;
	public static final String myPathZul = "/web/ui/sinssnt/agenda/agendaModificaPopupForm.zul";
	protected String keyPermission = "AG_SPO";

	private int stato=0;
	private int stato_form=0;
	private boolean salvato=false;
	protected Hashtable hashDapassare =new Hashtable();
	protected int ultimo_y=0;
	protected String dataOdierna=procdate.getitaDate();
	private Label lbl_selezionaPrestazioniErogate;
	private Listbox tablePrestazioni;
	private Button btn_confermaSelezione;
	private CaribelTextbox assistito;

	private AgendaEJB agendaEJB = new AgendaEJB();
	private AgendaEditEJB agendaEditEJB = new AgendaEditEJB();

	@Override
	public void doInitForm() {
		try {
			super.doInitForm();
			CaribelListModel emptyModel = new CaribelListModel();
			emptyModel.setMultiple(true);
			tablePrestazioni.setModel(emptyModel);

			logger.info("AgendaRegistraPopUpFormCtrl/Inizializzazione arg in input: " + arg.toString());
			stato_form = (Integer) arg.get("stato_interv");
			if (stato_form!=0){
				lbl_selezionaPrestazioniErogate.setValue(Labels.getLabel("agendaRegistraPopup.prestazioniErogate"));
				UtilForComponents.disableListBox(tablePrestazioni, true);
				UtilForComponents.disableListBox(tableGrigliaPrestazioni, true);
				btn_confermaSelezione.setDisabled(true);
			}
			hashDapassare.putAll(arg);
			String w_data = formDate(hashDapassare.get("ag_data"), "gg-mm-aaaa");
			String msg = Labels.getLabel("agendaRegistraPopup.assistito", 
					new String[]{(String) hashDapassare.get("assistito"), 
					w_data,
					Labels.getLabel("agenda.common.mattinaPomeriggio." + hashDapassare.get("ag_orario"))});
			assistito.setValue(msg);
			
            String w_stato=(String)arg.get("stato");
            if(w_stato.equals("UPDATE"))
            	execSelect();
			if (stato_form!=0){
				this.setReadOnly(true);
//				lbl_selezionaPrestazioniErogate.setValue(Labels.getLabel("agendaRegistraPopup.prestazioniErogate"));
			} else {
				doCheckPermission();
			}
			execSelectPrestazioni();

		} catch (Exception e) {
			doShowException(e);
		}

	}

	private int execSelectPrestazioni() throws Exception{
		modelloPrestazioni = new CaribelListModel();
		modelloPrestazioni.setMultiple(true);
		tableGrigliaPrestazioni.setModel(modelloPrestazioni);
		//griglia prestazioni
		Hashtable h = new Hashtable();
		//h.put("tipo_oper",profile.getParameter("tipo_operatore"));
		Vector griglia=new Vector();
		h.put("referente",hashDapassare.get("tipo_operatore"));
		h.put("JFrameAgendaModificaPopUp","JFrameAgendaModificaPopUp");

		Object obj=profile.getObject((String)hashDapassare.get("referente")+"_prestazioniRefer"); 
		//profile.getObject((String)hashDapassare.get("referente")+"_prestazioniRefer");
		if(obj != null){
			griglia=(Vector)obj;
		}else{
			Object o= invokeGenericSuEJB(agendaEJB, h, "CaricaTabellaPrestazioni");//db.CaricaPrest(h);
			if (o!=null){
				if(((Vector)o).size()<=0){
					UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
					//	                new it.pisa.caribel.swing2.cariInfoDialog(null,"Non esistono prestazioni!","Attenzione!").show();
					return -1;
				}
				else griglia=(Vector)o;
			}
			//System.out.println("REGISTR put profile"+(String)hashDapassare.get("referente")+"_prestazioniRefer");
			profile.putParameter((String)hashDapassare.get("referente")+"_prestazioniRefer",griglia);
		}

		Enumeration en=griglia.elements();
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			Hashtable ht=is.getHashtable();
			//System.out.println("carico pannello in hash=="+ht.toString());
			caricaTabPrestazioni(ht);
		}
		//	        this.JCariTableGrigliaPrestaz.setColumnSizes();
		return 1;
	}

	//carico tabella prestaz
	private int caricaTabPrestazioni(Hashtable hash){
		// TODO VFR controllare il cambio
//		((CaribelListModel) tableGrigliaPrestazioni.getModel()).add(hash);
		modelloPrestazioni.add(hash);

		//		StringBuffer st = new StringBuffer();
		//		String chiavi = cariObjectTableModel1.getColumnDB(sepChar);
		//		StringTokenizer rowChiavi = new StringTokenizer(chiavi,sepChar);
		//		while(rowChiavi.hasMoreTokens()){
		//			String rowItem = rowChiavi.nextToken();
		//			if(hash.containsKey(rowItem)){
		//				if(((String)hash.get(rowItem)).equals(""))
		//					st.append(" ");
		//				else
		//					st.append((String) hash.get(rowItem));
		//			}else {
		//				//la tabella non contiene la chiave
		//				st.append(" ");
		//			}
		//			if (rowChiavi.hasMoreTokens())
		//				st.append(sepChar);
		//		}//end while
		//		cariObjectTableModel1.addRow(st.toString());
		//		//System.out.println("carico su griglia:"+st.toString());
		return 1;
	}//end caricaPrest

	private int execSelect() throws Exception {
		Object o= invokeGenericSuEJB(agendaEJB, hashDapassare, "query_agendapopup"); // db.selezAgenda(hashDapassare);
		Vector griglia=new Vector();
		if (o!=null){
			if(((Vector)o).size()<=0){
				UtilForUI.standardExclamation(Labels.getLabel("pianoAssistenziale.msg.noPrestazioni"));
				//              new it.pisa.caribel.swing2.cariInfoDialog(null,"Non esistono prestazioni!","Attenzione!").show();
				return -1;
			}
			else griglia=(Vector)o;}
		Enumeration en=griglia.elements();
		while(en.hasMoreElements()){
			ISASRecord is =(ISASRecord)en.nextElement();
			Hashtable h=is.getHashtable();
			logger.trace("carico prestazioni della cella selezionata in tabella hash=="+h.toString());
			caricaRigaPrest(h);
		}
		//        this.jCariTablePrestaz.setColumnSizes();
		return 0;
	}

	private int caricaRigaPrest(Hashtable hash){
		//inserimento dati dai controlli alla griglia
		//	    StringBuffer st = new StringBuffer();
		//	    // inserimento di un nuovo record
		//	      String chiavi = cariObjectTableModelPrestaz.getColumnDB(sepChar);
		//	      StringTokenizer rowChiavi = new StringTokenizer(chiavi,sepChar);
		//	      while(rowChiavi.hasMoreTokens()){
		//	        String rowItem = rowChiavi.nextToken();
		//	       // System.out.println("rowItem..."+rowItem);
		//	        if(rowItem.equals("flag")){//per ora le prestazioni che ho caricato ci restano!!
		//	         if(((String)hash.get("ap_stato")).equals("0")){//==>posso non inserirlo?
		//	            st.append("false");
		//	         }else{
		//	            st.append("true");
		//	         }
		//	        }else if(rowItem.equals("ap_prest_qta")){
		//	         st.append((Integer) hash.get(rowItem));
		//	        }
		//	        else if(hash.containsKey(rowItem))
		//	        {
		//	            if(((String)hash.get(rowItem)).equals(""))
		//	                st.append(" ");
		//	            else
		//	                st.append((String) hash.get(rowItem));
		//	        }
		//	        else {
		//	        //la tabella non contiene la chiave
		//	              st.append(" ");
		//	        }
		//	        if (rowChiavi.hasMoreTokens())
		//	             st.append(sepChar);
		//	      }//end while
		//	      //System.out.println("stringa che carico="+st.toString());
		//	      cariObjectTableModelPrestaz.addRow(st.toString());
		((CaribelListModel) tablePrestazioni.getModel()).add(hash);
		return 1;
	}//end caricaRigaPrest

	@Override
	protected boolean doValidateForm() throws Exception {
		return false;
	}
	public int ControlloData (String dataold,String datanew)
	{
		// controlla se dataold < datanew
		//preparazione primo array
		int[] datavecchia= new int[3];

		Integer giorno = new Integer(dataold.substring(0,2));
		datavecchia[0]= giorno.intValue();
		Integer mese = new Integer(dataold.substring(3,5));
		datavecchia[1]= mese.intValue();
		Integer anno = new Integer(dataold.substring(6,10));
		datavecchia[2]= anno.intValue();
		//preparazione secondo array
		int[] datanuova= new int[3];

		Integer day = new Integer(datanew.substring(0,2));
		datanuova[0]= day.intValue();
		Integer mounth = new Integer(datanew.substring(3,5));
		datanuova[1]= mounth.intValue();
		Integer year = new Integer(datanew.substring(6,10));
		datanuova[2]= year.intValue();

		//confronto anno

		if (datanuova[2] < datavecchia[2])
		{
			//new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
			return -1;
		}
		else
			if (datanuova[2] == datavecchia[2])

				//confronto mes
				if (datavecchia[1] > datanuova[1])
				{
					//  new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
					return -1;
				}
				else
					if (datanuova[1] == datavecchia[1])
						//confronto giorno
						if (datanuova[0] < datavecchia[0])
						{
							//  new cariInfoDialog(null,"Intervallo non valido,invertire le date ","Attenzione!").show();
							return -1;
						}
		return 1;

	}
	public void onConfermaSelezione(Event event) throws Exception{
		int ret =this.caricaPrestazioniSelezionate();
		if(ret==-1){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniOperatore"));
			return;
		}
		else if (ret==-2){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
			return;
		}
		tableGrigliaPrestazioni.clearSelection();
	}
	public int caricaPrestazioniSelezionate(){
		ListModel<Object> modello = tableGrigliaPrestazioni.getModel();
		CaribelListModel modelloGriglia = (CaribelListModel) tablePrestazioni.getModel();
		if (modello.getSize() == 0)
			return -1;
		boolean trovato=false;
		//		for(int k=0; k< modello.getRowCount(); k++){
		//			int col=modello.getColumnLocationByName("pr_sel");
		//			boolean valore = ((Boolean)modello.getValueAt(k, col)).booleanValue();
		//			if(valore){
		//				modello.setValueAt(new Boolean(false),k,col);
		//				//(jCariTablePrestaz.getModel()).setValueAt(new Boolean(b) ,i,0);
		for (Iterator iterator = tableGrigliaPrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			trovato=true ;
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			if (itemGrid instanceof Hashtable) {
				//				int colonnaCodice=modello.getColumnLocationByName("prest_cod");
				//				String cod_prest = (String)modello.getValueAt(k, colonnaCodice);
				String cod_prest = (String) ((Hashtable) itemGrid).get("prest_cod");
				/*Controllo se e' gia' presente nella griglia un record con lo stesso codice prestazione
	                  ed eventualmente devo aumentare la quantità
				 */
				Hashtable hNuovo=new Hashtable() ;
				hNuovo.put("ap_prest_cod",cod_prest.trim());
				//				int num_riga=cariObjectTableModelPrestaz.columnsContains(hNuovo);
				int num_riga = modelloGriglia.columnsContains(hNuovo);

				//int quantita=1;
				//int Col_quantita_nuova=modello.getColumnLocationByName("pre_numero");
				int quantita=1;//Integer.parseInt((String)modello.getValueAt(k, Col_quantita_nuova));
				String frequenza="";
				String ap_alert="";
				if (num_riga!=-1){
					//l'ho inserito devo aumentare la quantità
					//aggiorno tutto
					//					int Col_quantita_vecchia=//cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_qta");
					//					String qta=(String)cariObjectTableModelPrestaz.getValueAt(num_riga, Col_quantita_vecchia);
					//					if((qta.trim()).equals(""))qta="1";
					int quant_vecchia= (Integer) modelloGriglia.getFromRow(num_riga, "ap_prest_qta"); //Integer.parseInt(qta);
					quantita=quant_vecchia+quantita;
					frequenza=(String) modelloGriglia.getFromRow(num_riga, "frequenza");//cariObjectTableModelPrestaz.getValueAt(num_riga,cariObjectTableModelPrestaz.getColumnLocationByName("frequenza"));
					ap_alert=(String) modelloGriglia.getFromRow(num_riga, "ap_alert");//cariObjectTableModelPrestaz.getValueAt(num_riga,cariObjectTableModelPrestaz.getColumnLocationByName("frequenza"));
				}
				//  Questo perché se la frequenza è una stringa vuota allora la concatenazione che segue
				//  produrrebbe una riga con due separatori contigui.
				if (frequenza.equals(""))
					frequenza=" ";
				//gb 18/09/07: fine *******
				Hashtable<String, Object> hDati = (Hashtable<String, Object>) itemGrid;
				hDati.put("ap_prest_qta", quantita);
				hDati.put("frequenza", frequenza);
				hDati.put("ap_alert", ap_alert);
				hDati.put("ap_prest_cod", cod_prest);
				hDati.put("ap_prest_desc", (String) ((Hashtable) itemGrid).get("prest_des"));
				
//				String  riga="true"+"##"+cod_prest+"##"+
//						(!(((String)modello.getValueAt(k,modello.getColumnLocationByName("prest_des"))).equals(""))?
//								(String)modello.getValueAt(k,modello.getColumnLocationByName("prest_des")):" ")+
//								"##"+frequenza+"##"+quantita+"##"+"0##";
				if (num_riga!=-1){
					modelloGriglia.remove(num_riga);
					modelloGriglia.add(num_riga, hDati);
					//					cariObjectTableModelPrestaz.removeRow(num_riga);
					//					cariObjectTableModelPrestaz.insertRowAt(riga,num_riga);
					modelloGriglia.addToSelection(modelloGriglia.get(num_riga));
				}else{
					modelloGriglia.add(hDati);
					modelloGriglia.addToSelection(modelloGriglia.get(modelloGriglia.size()-1));
					//					cariObjectTableModelPrestaz.insertRowAt(riga,cariObjectTableModelPrestaz.getRowCount());
				}
			}
		}
		if(trovato)
			return 1;
		else
			return -2;
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
	
	@Override
	protected boolean doSaveForm() throws Exception {
		int num=tablePrestazioni.getSelectedCount(); //this.jCariTablePrestaz.getModel().getRowCount();
		if(num==0){
			UtilForUI.standardExclamation(Labels.getLabel("accessiPrestazioni.msg.noPrestazioniSelezionate"));
			return false;
		}
		Vector v=new Vector();
		Vector q=new Vector();
		Vector f=new Vector();
//		int col_qta=cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_qta");
//		int col_pr=cariObjectTableModelPrestaz.getColumnLocationByName("ap_prest_cod");
//		
		for (Iterator iterator = tablePrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
			Listitem litem = (Listitem) iterator.next();
			Object itemGrid = litem.getAttribute("ht_from_grid");
			v.addElement((String) ((Hashtable)itemGrid).get("ap_prest_cod"));
//				v.addElement((String)cariObjectTableModelPrestaz.getValueAt(i,col_pr));
				Integer qta = (Integer) ((Hashtable)itemGrid).get("ap_prest_qta");//((String)cariObjectTableModelPrestaz.getValueAt(i,col_qta));
				if(qta == null)q.addElement("1");
				else q.addElement(qta.toString());
				f.addElement(((Hashtable)itemGrid).get("ap_alert"));
				//System.out.println("carico nel vettore="+check.JCariTextFieldCodPrest.getUnmaskedText());
		}
		//System.out.println("vettore="+v.toString());

	       if(v.size()==0){
	     	  if(((String)hashDapassare.get("stato")).equals("UPDATE")){  
	     		 UtilForUI.standardYesOrNo(Labels.getLabel("agendaModificaPopup.rimuovereTutte"), new EventListener<Event>(){
	 				public void onEvent(Event event) throws Exception{
	 					if (Messagebox.ON_YES.equals(event.getName())){	
	 						Object o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "cancella_intervCella"); //db.deleteInterv(hashDapassare);
	 						((AgendaRegistraFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
	 					}
	 				}
	 			} );
	     		 return false;
	     	  }
	       }
		
		this.hashDapassare.put("prestazioni",v);
		this.hashDapassare.put("quantita",q);
        this.hashDapassare.put("frequenza",f);

		//System.out.println("Registro hashtable="+hashDapassare.toString());
		Object o=null;

		System.out.println("doSaveForm/hashDapassare: " + hashDapassare.toString());
	       if(((String)hashDapassare.get("stato")).equals("UPDATE")){
	    	   o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_prestazCella"); //db.registraPrest(hashDapassare);
	       }else{
	    	   o = invokeGenericSuEJB(agendaEditEJB, hashDapassare, "inserisci_intervCella");//db.registraInterv(hashDapassare);
	       }
		((AgendaGenericFormCtrl)self.getParent().getAttribute(MY_CTRL_KEY)).aggiorna();
		self.detach();
		
		return true;
//		padre.aggiornaTabella();
	}
}

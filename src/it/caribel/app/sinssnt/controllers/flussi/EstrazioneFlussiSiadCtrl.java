package it.caribel.app.sinssnt.controllers.flussi;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.bean.nuovi.FlussiADRSAMarcheEJB;
import it.caribel.app.sinssnt.bean.nuovi.FlussiSIADEstrattiEJB;
import it.caribel.app.sinssnt.bean.nuovi.GestoreFlussiAdrsaMarcheEJB;
import it.caribel.app.sinssnt.controllers.flussi.renderer.FlussiEstrattiRenderer;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.controllers.scaleValutazione.ComboRugSiadRepository;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.ChiaviIsasBase;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.app.sinssnt.util.ManagerChiaviISAS;
import it.caribel.util.CaribelComboRepository;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.generic_controllers.CaribelSortPaginateCtrl;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.exception.CariException;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.isas2.ISASUser;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.flussi_min.FlsProperties;
import it.pisa.caribel.util.ServerUtility;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Toolbarbutton;

public class EstrazioneFlussiSiadCtrl extends CaribelFormCtrl {

	protected CaribelListbox caribellb2;

	protected Paging pagcaribellb2;
	protected Toolbarbutton btn_sort_paginate;
	protected transient CaribelSortPaginateCtrl caribelSortPaginateCtrl;
	private int _selectedIndex = -1;
	private int _firstResult = -1;
	private int _maxResult = -1;

	private static final long serialVersionUID = 1L;

	private String myKeyPermission = ChiaviIsasBase.ESTRAZIONE_FLUSSI;
	private String myKeyPermissionSu = ChiaviIsasBase.ESTRAZIONE_FLUSSI_SU;
	private FlussiSIADEstrattiEJB myEJB = new FlussiSIADEstrattiEJB();
	private String myPathFormZul = "/web/ui/sinssnt/flussi/estrattore_flussi_siad.zul";

	private CaribelTextbox cod_operatore;
	protected CaribelCombobox zona;
	protected CaribelCombobox distretto;
	protected CaribelCombobox presidio_comune_area;

	CaribelListModel modelTable = new CaribelListModel();

	ServerUtility su = new ServerUtility();

	private CaribelCombobox anno;
	private CaribelCombobox mese;
	private Button anteprima;
	private Button estrazione;
	private Button convalida;
	private Button invalida;
	private Toolbarbutton btn_open;
	private PanelUbicazioneCtrl c;
	
	boolean admin_flussi = false;
	boolean estrAreaVasta = false;
	
	String current_ticket = "";
	String current_mese	="";
	String current_anno	="";
	
	private FlsProperties prop = FlsProperties.getInstance();
	
    
	GestoreFlussiAdrsaMarcheEJB gestore_flussi = new GestoreFlussiAdrsaMarcheEJB();
	final myLogin ml = CaribelSessionManager.getInstance().getMyLogin();


	private boolean runningAnteprima = false;
	private boolean runningDefinitiva = false;


	public void doInitForm() {
		admin_flussi = getProfile().getIsasUser().canIUse(ChiaviISASSinssntWeb.EXFLUSSI);
		super.initCaribelFormCtrl(myEJB, myKeyPermission);
		super.setMethodNameForQueryPaginate("query");
		CaribelComboRepository.populateCombobox(mese, ComboRugSiadRepository.MESI, false);
		CaribelComboRepository.populateCombobox(anno, getAnniSiad(), false);
		
		
		cod_operatore.setValue(getProfile().getStringFromProfile("codice_operatore"));
		Component p = self.getFellow("panel_ubicazione");
		c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
		estrAreaVasta = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ESTRAZIONE_FLUSSI_SIAD_X_AREA);
		
		c.setDistrettiVoceTutti(estrAreaVasta);
		
		c.doInitPanel();
		c.settaRaggrContatti("CA");
		c.setVisibleZona(false);
		c.setVisibleDistretto(true);
		c.setVisiblePresidioComuneAreaDis(false);

		zona.setSelectedValue(getProfile().getStringFromProfile(ManagerProfile.ZONA_OPERATORE));
		
		c.setDistrettoValue(admin_flussi&&estrAreaVasta?CostantiSinssntW.CTS_DISTRETTI_VOCE_TUTTI:getProfile().getStringFromProfile(ManagerProfile.DISTRETTO_OPERATORE));
		c.setDistrettoRequired(!estrAreaVasta);
		c.setDistrettoDisabilita(!admin_flussi);
		estrazione.setDisabled(!getProfile().getIsasUser().canIUse(ChiaviISASSinssntWeb.EXFLUSSI));
		convalida.setDisabled(true);
		invalida.setDisabled(true);
		btn_open.setDisabled(!admin_flussi);
		
		

		if (caribellb2 == null) {
			caribellb2 = (CaribelListbox) self.getFellowIfAny("estrazioni_flussi").getFellowIfAny("caribellb2", true);
		}

		this.self.addEventListener("onAnteprima", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doAnteprima();
//				Clients.clearBusy();
			}
		});
		this.self.addEventListener("onEffettiva", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doEffettiva();
//				Clients.clearBusy();
			}
		});
		this.self.addEventListener("onEstrazioneEffettiva", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				estrazioneEffettiva();
//				Clients.clearBusy();
			}
		});

		this.self.addEventListener("onConvalida", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doConvalida();
				Clients.clearBusy();
			}
		});
		this.self.addEventListener("onInvalida", new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				doInvalida();
				Clients.clearBusy();
			}
		});

		caribellb2.setItemRenderer(new FlussiEstrattiRenderer());
		caribellb2.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				if (event.getData() instanceof String)
				downloadFile((String) event.getData());
			}
		});

	}

	private Map<String, String> getAnniSiad() {
		return myEJB.getAnniSiad(CaribelSessionManager.getInstance().getMyLogin());		
	}

	protected void downloadFile(String data) throws IOException {
		String contentType = "";
		int index = caribellb2.getSelectedIndex();
		String ticket = ((Integer) ((CaribelListModel) caribellb2.getModel()).getFromRow(index, "ticket_elab")).toString();
		String filename = (String) ((CaribelListModel) caribellb2.getModel()).getFromRow(index, "filename");
	    
		if (data != null) {
			filename+="."+data;
			contentType = CostantiSinssntW.MIME_TYPE_ZIP;			
			try {
				Filedownload.save(getArrayBytesFromFile(ticket), contentType, filename);
			} catch (Exception e) {
				Clients.showNotification(Labels.getLabel("exception.filenotfound"));
			}
		}
	}

	private byte[] getArrayBytesFromFile(String ticket) throws Exception {
	    GestoreFlussiAdrsaMarcheEJB gestoreFlussi = new GestoreFlussiAdrsaMarcheEJB();
	     return gestoreFlussi.getFile(CaribelSessionManager.getInstance().getMyLogin(), ticket);

	}

//	private byte[] getArrayBytesFromFile(File file) throws IOException {
//		FileInputStream fileInputStream = null;
//
//		byte[] bFile = new byte[(int) file.length()];
//
//		// convert file into array of bytes
//		fileInputStream = new FileInputStream(file);
//		fileInputStream.read(bFile);
//		fileInputStream.close();
//
//		return bFile;
//	}

	public void doStampa() {

	}

	public void onClick$btn_open() {
		try {

			if (!anno.getSelectedValue().equals("") && !mese.getSelectedValue().equals("")
					&& (estrAreaVasta || (!estrAreaVasta && !c.getDistrettoValue().equals("") && !c.getDistrettoValue().equals("TUTTI")))) {
				super.hParameters.put(cod_operatore.getDb_name(), cod_operatore.getValue().toUpperCase());
				super.hParameters.put(anno.getDb_name(), anno.getSelectedValue());
				super.hParameters.put(mese.getDb_name(), mese.getSelectedValue());

				if (!zona.getSelectedItem().getValue().equals("TUTTO") && !zona.getSelectedItem().getValue().equals(""))
					super.hParameters.put(zona.getDb_name(), zona.getSelectedItem().getValue());
				if (!distretto.getSelectedItem().getValue().equals("TUTTO")
						&& !distretto.getSelectedItem().getValue().equals(""))
					super.hParameters.put(distretto.getDb_name(), distretto.getSelectedItem().getValue());
				if (presidio_comune_area.getSelectedItem() != null
						&& !presidio_comune_area.getSelectedItem().equals(""))
					super.hParameters.put(presidio_comune_area.getDb_name(), presidio_comune_area.getSelectedItem()
							.getValue());

				doRefresh();
				caribellb2.setCheckmark(false);
				caribellb2.setMultiple(false);

				zona.setDisabled(true);
				distretto.setDisabled(true);
				ISASUser user= getProfile().getIsasUser(); 
				if (user.canIUse(myKeyPermissionSu, ManagerChiaviISAS.MODI)){
					distretto.setDisabled(false);
				}
				presidio_comune_area.setDisabled(true);

			} else {
				Messagebox.show(Labels.getLabel(estrAreaVasta?"exception.mese_anno_obbligatori.msg":"exception.mese_anno_distretto_obbligatori.msg"),
						Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);

			}
		} catch (Exception e) {
			doShowException(e);
		}
	}

	public void onClick$btn_undo() {
		doPulisciRicerca();

	}



	public void doPulisciRicerca() {
		try {
			setDefault();

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(this.getClass().getName()
					+ ": Impossibile inizializzare l'operatore, rivolgersi all'assistenza");
		}
	}

	private void setDefault() throws Exception {

		if (caribellb2.getItemCount() > 0) {
			caribellb2.getItems().clear(); 
			caribellb2.setModel(new CaribelListModel<Object>(new Vector<Object>()));
		}

		
		cod_operatore.setValue(getProfile().getStringFromProfile("codice_operatore"));
		c.settaRaggrContatti("CA");
		c.setVisibleZona(false);
		c.setVisibleDistretto(true);
		c.setVisiblePresidioComuneAreaDis(false);
		
		anno.setText("");
		mese.setText("");
		c.setDistrettiVoceTutti(true);
		c.setDistrettoValue(admin_flussi?CostantiSinssntW.CTS_DISTRETTI_VOCE_TUTTI:getProfile().getStringFromProfile(ManagerProfile.DISTRETTO_OPERATORE));
		zona.setSelectedValue(getProfile().getStringFromProfile(ManagerProfile.ZONA_OPERATORE));
		c.setDistrettoDisabilita(!admin_flussi);
		estrazione.setDisabled(!admin_flussi);
		convalida.setDisabled(true);
		invalida.setDisabled(true);		
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	public void doRefresh() throws Exception {
		if (pagcaribellb2 != null) {
			// Recupero la stringa per la clausola ORDER BY
			if (this.caribelSortPaginateCtrl != null) {
				String strOrderBy = "";
				strOrderBy = this.caribelSortPaginateCtrl.getStrOrderBy();
				if (strOrderBy != null && !strOrderBy.isEmpty())
					hParameters.put("ordinamento", strOrderBy);
			}
			// Resetto il paging affinche ricominci dalla prima pagina
			// if(this._firstResult==-1){
			this._firstResult = 0;
			this._maxResult = pagcaribellb2.getPageSize();
			// }
			doQueryPaginate(_firstResult, _maxResult);
			if (_selectedIndex != -1 && caribellb2.getItemCount() > _selectedIndex)
				caribellb2.setSelectedIndex(_selectedIndex);
		} else {
			doQuery();
			if (_selectedIndex != -1 && caribellb2.getItemCount() > _selectedIndex)
				caribellb2.setSelectedIndex(_selectedIndex);
			
		}
		hParameters.put("distr", distretto.getSelectedValue());
		String estrazione_in_convalida = existsEstrazioneInConvalida(hParameters); 
		if (!estrazione_in_convalida.equals("")){
			convalida.setDisabled(false);
			invalida.setDisabled(false);
		}
		else{
			convalida.setDisabled(true);
			invalida.setDisabled(true);
		}

	}

	private void doQueryPaginate(int firstResult, int maxResult) {
		try {
			int dimension = executeQueryPaginate(firstResult, maxResult);
			if (dimension == 0)
				UtilForUI.doNotificationNoRows(self);
		} catch (Exception e) {
			doShowException(e);
		}
	}

	private void doQuery() {
		try {
			int count = executeQuery();
			if (count == 0) {
				UtilForUI.doNotificationNoRows(self);
			} else
				Clients.showNotification(Labels.getLabel("grid.search.total.rows", new String[] { "" + count }),
						"info", self, "middle_center", 2500);
		} catch (Exception e) {
			e.printStackTrace();
			doShowException(e);
		}
	}

	private int executeQueryPaginate(int firstResult, int maxResult) throws Exception {
		this._firstResult = firstResult;
		this._maxResult = maxResult;

		hParameters.put("start", "" + firstResult);
		hParameters.put("stop", "" + maxResult);
		Vector<Object> vDbr = queryPaginateSuEJB(currentBean, hParameters);
		int posUltimo = vDbr.size() - 1;
		@SuppressWarnings("rawtypes")
		Hashtable ultimo = (Hashtable) vDbr.get(posUltimo);
		// int currentRecord =
		// ((Integer)ultimo.get("currentRecord")).intValue();
		int dimension = ((Integer) ultimo.get("dimension")).intValue();
		vDbr.remove(posUltimo);

		pagcaribellb2.setTotalSize(dimension);

		caribellb2.getItems().clear();
		caribellb2.setModel(new CaribelListModel<Object>(vDbr));

		return dimension;
	}

	private int executeQuery() throws Exception {
		Vector<ISASRecord> vDbr = querySuEJB(this.currentBean, this.hParameters);
		caribellb2.getItems().clear();
		if (vDbr != null && vDbr.size() > 0) {
			caribellb2.setModel(new CaribelListModel<ISASRecord>(vDbr));
			return vDbr.size();
		} else
			return 0;
	}

//	public void onClick$anteprima() throws Exception {
//		Clients.showBusy(this.self, "please wait...");
//		Events.echoEvent("doAnteprima", this.self.getParent(), null);
//	}

	
	public void doAnteprima() throws Exception {
		if (!runningAnteprima){
		if (!anno.getSelectedValue().equals("") && !mese.getSelectedValue().equals("")) {

			current_anno = anno.getText();
			current_mese = mese.getText();

			hParameters.put("anno", anno.getSelectedValue());
			hParameters.put("mese", mese.getSelectedValue());
			hParameters.put("cod_operatore", cod_operatore.getText());
			hParameters.put("zona",this.zona.getSelectedValue());
			hParameters.put("distr",this.distretto.getSelectedValue());
			
			String estrazione_in_convalida = existsEstrazioneInConvalida(hParameters); 
			if (estrazione_in_convalida.equals("")){
			
			try {
				
				current_ticket = gestore_flussi.getNextTicket(CaribelSessionManager.getInstance().getMyLogin());
				
			
				hParameters.put("ticket", current_ticket);
						
				
				// Sgancio l'elaborazione dell'estrazione dalla form, in modo da evitare i timeout
				Thread t = new Thread( new Runnable()
		        {
		            public void run()
		            {
		            	
		            	FlussiADRSAMarcheEJB estrattore = new FlussiADRSAMarcheEJB();
		            	try {
		            		estrattore.anteprimaEstrazione(ml, hParameters);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				
		            }
		        });
				
				t.start();
				
				runningAnteprima = true;
				
			}catch (Exception e) {
				e.printStackTrace();
				Clients.clearBusy();
				doShowException(e);
			}
			}else {
				int m = Integer.parseInt(estrazione_in_convalida.split("-")[0]);
				String anno = estrazione_in_convalida.split("-")[1];
				String mese = Labels.getLabel("generic.mese"+m);
				String msg = "("+mese+" "+anno+")";
				Messagebox.show(Labels.getLabel("exception.estrazione_in_convalida.msg",new String[]{msg}),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);
				Clients.clearBusy();

			}
			
		}
		 else {
			 Messagebox.show(Labels.getLabel(estrAreaVasta?"exception.mese_anno_obbligatori.msg":"exception.mese_anno_distretto_obbligatori.msg"),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);
			 Clients.clearBusy();
		}

			}
		if (runningAnteprima) {
			Thread.sleep(1000);
			if (gestore_flussi.inElaborazione(ml, current_ticket)) {
				Events.echoEvent("onAnteprima", this.self, null);
				System.out.println("Elaborazione non ancora pronta: mi richiamo...");
			} else {
				runningAnteprima = false;
				System.out.println("Elaborazione pronta: non mi richiamo...");
				byte[] ret = gestore_flussi.getFile(CaribelSessionManager.getInstance().getMyLogin(), current_ticket);

				if (ret != null) {
					try {
						Filedownload.save(ret, CostantiSinssntW.MIME_TYPE_ZIP, "SIAD_anteprimaEstrazione_"
								+ current_mese + "_" + current_anno + ".zip");
					} catch (Exception e) {
						Clients.clearBusy();
						Clients.showNotification(Labels.getLabel("exception.filenotfound"));
					}
				}
				Clients.clearBusy();
			}
		}		

//				
			
			
	}
	
	public void doEffettiva() {
		try{	
			
		if (!anno.getSelectedValue().equals("") && !mese.getSelectedValue().equals("")
				&& (estrAreaVasta || (!estrAreaVasta && !c.getDistrettoValue().equals("") && !c.getDistrettoValue().equals("TUTTI")) )) {

			
			hParameters.put("anno", anno.getSelectedValue());
			hParameters.put("mese", mese.getSelectedValue());
			hParameters.put("cod_operatore",this.cod_operatore.getText());
			hParameters.put("zona",this.zona.getSelectedValue());
			if (!ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ESTRAZIONE_FLUSSI_SIAD_X_AREA))
				hParameters.put("distr", distretto.getSelectedValue());
			String estrazione_in_convalida = existsEstrazioneInConvalida(hParameters); 
			if (estrazione_in_convalida.equals("")){
			hParameters.put(Costanti.ESTRAZIONE_EFFETTIVA,"SI");
			
			Messagebox.show(Labels.getLabel("flussi_siad.estrazione_effetiva.warning"), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())){
								Clients.showBusy("Estrazione dati in corso. Attendere prego..."); //show a busy message to user
								estrazioneEffettiva();
//								if (res){
//									Messagebox.show(Labels.getLabel("flussi_siad.estrazione.ok.msg"));
//								}
//								else Messagebox.show(Labels.getLabel("flussi_siad.estrazione.ko.msg"));
//								doRefresh();
								}							
							if (Messagebox.ON_CANCEL.equals(event.getName())){
								return;			
							}						
						}

					
					});
		}
		else {
			int m = Integer.parseInt(estrazione_in_convalida.split("-")[0]);
			String anno = estrazione_in_convalida.split("-")[1];
			String mese = Labels.getLabel("generic.mese"+m);
			String msg = "("+mese+" "+anno+")";
			Messagebox.show(Labels.getLabel("exception.estrazione_in_convalida.msg",new String[]{msg}),
				Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);
			Clients.clearBusy();
		}
		}
		else {
			Messagebox.show(Labels.getLabel(estrAreaVasta?"exception.mese_anno_obbligatori.msg":"exception.mese_anno_distretto_obbligatori.msg"),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);
			Clients.clearBusy();
			
		}
		}catch(Exception e){
			doShowException(e);
			Clients.clearBusy();
		}
	}
	
	private Boolean estrazioneEffettiva() throws Exception {
		boolean ret = false;
		if (!runningDefinitiva){
		current_ticket = gestore_flussi.getNextTicket(ml);

		
		hParameters.put("ticket", current_ticket);
				
		// Sgancio l'elaborazione dell'estrazione dalla form, in modo da evitare i timeout
		new Thread( new Runnable()
        {
            public void run()
            {
            	FlussiADRSAMarcheEJB estrattore = new FlussiADRSAMarcheEJB();
            	try {
            		estrattore.estrazioneFlussiEffettiva(ml, hParameters);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
            }
        }).start();
		runningDefinitiva = true;
		}
		
		if (gestore_flussi.inElaborazione(CaribelSessionManager.getInstance().getMyLogin(),current_ticket)&& runningDefinitiva){
			Thread.sleep(10000);
			Events.echoEvent("onEstrazioneEffettiva",this.self,null);
		}
		else {
			runningDefinitiva = false;
		ret = gestore_flussi.getElaborazioneDefinitiva(CaribelSessionManager.getInstance().getMyLogin(),current_ticket);
		if (ret)
			Messagebox.show(Labels.getLabel("flussi_siad.estrazione.ok.msg"));
		else 
			Messagebox.show(Labels.getLabel("flussi_siad.estrazione.ko.msg"));
		
		
		Clients.clearBusy();
		doRefresh();
		}
		return ret;

	
	}							
	
	public String existsEstrazioneInConvalida(Hashtable h) throws Exception {
		return ((String)(invokeGenericSuEJB(myEJB, h, "existsEstrazioneInConvalida"))); 
	}

	public void doConvalida() throws Exception {
		try{
			if (!anno.getSelectedValue().equals("") && !mese.getSelectedValue().equals("")
					&& (estrAreaVasta || (!estrAreaVasta && !c.getDistrettoValue().equals("") && !c.getDistrettoValue().equals("TUTTI")))) {

				hParameters.put("mese", mese.getSelectedValue());
				hParameters.put("anno", anno.getSelectedValue());
				hParameters.put("zona",this.zona.getSelectedValue());
				if (!estrAreaVasta) hParameters.put("distr",this.distretto.getSelectedValue());
				Messagebox.show(Labels.getLabel("flussi_siad.convalida_estrazione.warning"), 
						Labels.getLabel("messagebox.attention"),
						Messagebox.YES+Messagebox.CANCEL, Messagebox.QUESTION,
						new EventListener<Event>() {
							public void onEvent(Event event)throws Exception {
								if (Messagebox.ON_YES.equals(event.getName())){
									Boolean res = (Boolean)invokeGenericSuEJB(myEJB, hParameters, "convalidaEstrazione");
									Clients.clearBusy();
									if (res){
										Messagebox.show(Labels.getLabel("flussi_siad.convalida.ok.msg"));
									}
									else Messagebox.show(Labels.getLabel("flussi_siad.convalida.ko.msg"));
									doRefresh();
									}							
								if (Messagebox.ON_CANCEL.equals(event.getName())){
									Clients.clearBusy();
									return;			
								}						
							}							
						});
					}
				 else {
					 Messagebox.show(Labels.getLabel(estrAreaVasta?"exception.mese_anno_obbligatori.msg":"exception.mese_anno_distretto_obbligatori.msg"),
						Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);

			}
			}catch(Exception e){
				Clients.clearBusy();
				doShowException(e);
			}

	}
	
	public void doInvalida() {
		try{
		if (!anno.getSelectedValue().equals("") && !mese.getSelectedValue().equals("")
				&& (estrAreaVasta || (!estrAreaVasta && !c.getDistrettoValue().equals("") && !c.getDistrettoValue().equals("TUTTI")))) {

			hParameters.put("mese", mese.getSelectedValue());
			hParameters.put("anno", anno.getSelectedValue());
			hParameters.put("zona",this.zona.getSelectedValue());
			if (!estrAreaVasta) hParameters.put("distr",this.distretto.getSelectedValue());
			Messagebox.show(Labels.getLabel("flussi_siad.scarta_estrazione.warning"), 
					Labels.getLabel("messagebox.attention"),
					Messagebox.YES+Messagebox.CANCEL, Messagebox.QUESTION,
					new EventListener<Event>() {
						public void onEvent(Event event)throws Exception {
							if (Messagebox.ON_YES.equals(event.getName())){
								Boolean res = (Boolean)invokeGenericSuEJB(myEJB, hParameters, "scartaEstrazione");
								if (res)
									Messagebox.show(Labels.getLabel("flussi_siad.invalida.ok.msg"));
								else 
									Messagebox.show(Labels.getLabel("flussi_siad.invalida.ko.msg"));
								Clients.clearBusy();
								doRefresh();
								}							
							if (Messagebox.ON_CANCEL.equals(event.getName())){
								Clients.clearBusy();
								return;			
							}						
						}							
					});
				}
			 else {
				 Messagebox.show(Labels.getLabel(estrAreaVasta?"exception.mese_anno_obbligatori.msg":"exception.mese_anno_distretto_obbligatori.msg"),
					Labels.getLabel("messagebox.attention"), Messagebox.OK, Messagebox.INFORMATION);

		}
		}catch(Exception e){
			Clients.clearBusy();
			doShowException(e);
		}
	}
	
	
	

	

	
	
}

package it.caribel.app.sinssnt.controllers.contattoInfermieristico;

import it.caribel.app.sinssnt.util.CostantiSinssntW;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import java.util.Collection;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treeitem;

public class StampaContattoInfCtrl extends CaribelFormCtrl {
	private static final long serialVersionUID = 1L;
	
	public static String CTS_FILE_ZUL = "/web/ui/sinssnt/contatto_infermieristico/stampa_contatto_inf.zul";
	
	
	CaribelTextbox key_cartella;
	CaribelTextbox key_contatto;
	CaribelTextbox key_operatore;
	CaribelTextbox key_assistito;
	CaribelDatebox key_data_apertura;
	
	Tree tree_stampa_inf;
	
	
	String n_cartella 	= "";
	String n_contatto 	= "";
	String data_ap 		= "";
	String data_chiu 	= "";
	String assistito 	= "";
	String operatore 	= "";
	
	
	public void doInitForm() {
		try {
			n_cartella 	= (String)arg.get(CostantiSinssntW.N_CARTELLA);
			n_contatto 	= (String)arg.get(CostantiSinssntW.N_CONTATTO);
			data_ap 	= (String)arg.get("data_ap");
			data_chiu 	= (String)arg.get("data_chiu");
			assistito 	= (String)arg.get("assistito");
			operatore 	= (String)arg.get("operatore");
			if(n_cartella!=null)
				key_cartella.setText(n_cartella);
			if(n_contatto!=null)
				key_contatto.setText(n_contatto);
			if(operatore!=null)
				key_operatore.setText(operatore);
			if(assistito!=null)
				key_assistito.setText(assistito);
			if(data_ap!=null)
				key_data_apertura.setText(data_ap);
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	public void doStampa() {
		try {
			String metodo = "query_skinfe";
			String report="infermieri.fo";
			
			String pagineSel = caricaPagine();
			
			if(pagineSel==null || pagineSel.equals("")){
				Messagebox.show(
						Labels.getLabel("common.print.page.not_selected"),
						Labels.getLabel("messagebox.attention"),
						Messagebox.OK,
						Messagebox.INFORMATION);
				return;
			}
						
			
			String u = "/SINSSNTFoServlet/SINSSNTFoServlet"+
					"?EJB=SINS_FOSKINFE"+
					"&USER="+CaribelSessionManager.getInstance().getMyLogin().getUser()+
					"&ID509="+CaribelSessionManager.getInstance().getMyLogin().getPassword()+
					"&METHOD="+metodo+				
					"&n_cart=" + n_cartella +
					"&n_conta=" + n_contatto+
			        "&data_apertura="+ key_data_apertura.getValueForIsas() +
			        "&data_chiusura="+ data_chiu +
					"&pagine=" + pagineSel+
					"&REPORT="+report;
			
			it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,u);
		}catch(Exception e){
			doShowException(e);
		}
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}
	
	
	
	private String caricaPagine()
	 {
	  String idCorr = "";
	  String ret = "";
	  Collection<Treeitem> figli = tree_stampa_inf.getTreechildren().getItems();
	  for(Treeitem corr:figli){ 
	   if(corr.getTreechildren()==null && corr.isSelected()){
	    idCorr = ((Treecell)corr.getTreerow().getFirstChild()).getId();
	    if(idCorr.indexOf("_")>=0){
	     idCorr = idCorr.substring(idCorr.indexOf("_")+1);
	     ret+=idCorr;
	    }
	   }
	  }
	  return ret;
	 }

}
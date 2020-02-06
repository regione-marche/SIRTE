package it.caribel.app.sinssnt.controllers.report.common;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import it.caribel.zk.generic_controllers.CaribelForwardComposer;

public class PanelFasciaEtaCtrl extends CaribelForwardComposer {
	private static final long serialVersionUID = 1L;

	// N.B.: =================================================================================================
	// 1) Il pannello crea tante fasce quanti sono i valori presenti nell'array dei
	//    testi x le label. Tale array e' inizializzato con 4 testi, ma passandolo
	//    come parametro nel 2° costruttore del pannello, si possono ottenere da 1
	//    a n fasce.
	// 2) I valori iniziali x le singole fasce vengono letti nell'ordine da CONF.
	//    Se non trovati, sono posti = "".
	// =======================================================================================================

	private String[] arrTestiLabel;	

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
	}
	
	public void generaPannello(String[] arrTestiLabel){
		this.arrTestiLabel = arrTestiLabel;
		
		String pathZul = "/web/ui/report/common/PanelSingleFasciaEta.zul";

		if (arrTestiLabel != null) {
			// fasce eta da configuratore
			StringTokenizer strTkzFasce = null;
			int contaTknFasce = 0;
			String fasce = getProfile().getStringFromProfile("fasce_eta");
			if ((fasce != null) && (!fasce.trim().equals("")) && (!fasce.trim().equals("NO"))) {
				strTkzFasce = new StringTokenizer(fasce, "|");
				contaTknFasce = strTkzFasce.countTokens();
			}		

			for(int i=0; i<arrTestiLabel.length; i++){				
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("single_panel_id", "single_panel_id"+i);
				map.put("lbl1_id", "lbl1_id"+i);
				map.put("txt1_id", "txt1_id"+i);
				map.put("txt2_id", "txt2_id"+i);
				map.put("lbl1_value", arrTestiLabel[i]);
				map.put("checkbox_id", "checkbox_id"+i);
				// inizializzo pannelli singola fascia con valori eta
				if ((strTkzFasce != null) && (i < contaTknFasce)) {
					String singolaFascia = (String)strTkzFasce.nextToken();
					int pos = singolaFascia.indexOf("-");		
					map.put("txt1_value", singolaFascia.substring(0,pos));	
					map.put("txt2_value", singolaFascia.substring(pos+1));
				}
				else{
					map.put("txt1_value", "0");	
					map.put("txt2_value", "999");
					
				}

				Component rigaSingleFasciaEta = Executions.getCurrent().createComponents(pathZul, self, map);	
				self.appendChild(rigaSingleFasciaEta);
			}
		}
	}
	
	
	  public boolean checkFasceEta()
	  {
	    // hashtable x ordinare le fasce: key=etaDA, val=JPanelSingleFascia.
	    // contiene solo quelle selezionate e corrette singolarmente.
	    Hashtable h_xord = new Hashtable();

	    // 1) ctrl correttezza singole fasce: dataA >= dataDA
	    if (!checkSingoleFasce(h_xord))
	      return false;

	    // 2) ctrl correttezza con le altre fasce: non devono sovrapporsi
	    if (!checkFasciaFasce(h_xord))
	      return false;

	    return true;
	  }

	  // controlla che ogni fascia sia valida e la pone in un'hashtable
	  private boolean checkSingoleFasce(Hashtable h_xord)
	  {
	    int j = 0;
	    boolean tuttoOk = true;
	    
	    Integer etaDaTmp=0;
	    Integer etaATmp=0;

	    Component singlePanelCorr;
	    while ((j < arrTestiLabel.length) && (tuttoOk)) {
	      
	    	singlePanelCorr = self.getFellow("single_panel_id"+j);
	    	PanelSingleFasciaEtaCtrl c1 = (PanelSingleFasciaEtaCtrl)singlePanelCorr.getAttribute(MY_CTRL_KEY);
	    	
	    	if (c1.isChecked()) {
    		etaDaTmp=c1.getEtaDa();
    		etaATmp=c1.getEtaA();
	        tuttoOk = c1.isValid();
	        if (tuttoOk) {// fascia selezionata ed OK
	          String keyFascia = aggiungiZeri(c1.getEtaDa());
	          if (!h_xord.containsKey(keyFascia))// la metto nell'hashtable
	            h_xord.put(keyFascia, c1);
	          else {// esiste gia' un'altra fascia con la stessa dataDA
	            // incremento la dataDA e riprovo a fare il ctrl
	            int nuovaDataDa = c1.getEtaDa() + 1;
	            c1.setEtaDa(nuovaDataDa);
	            h_xord.clear();
	            j = -1;
	          }
	        }
	      }
	      j++;
	    }

	    if (!tuttoOk)    
	    	Messagebox.show(
	    			Labels.getLabel("SinglePanelFasciaEta.msg.error1",new String[] {arrTestiLabel[j-1],etaATmp.toString(),etaDaTmp.toString()}),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
		
	    return tuttoOk;
	  }

	  // controlla che le fasce selezionate non siano sovrapposte
	  private boolean checkFasciaFasce(Hashtable h_xord)
	  {
	    int k = 0;
	    boolean tuttoOk = true;
	    int etaA_old = -1;
	    // ordino le fasce secondo le etaDA
	    Vector vKeyOrd = orderedKeys(h_xord);

	    while ((k < vKeyOrd.size()) && (tuttoOk)) {
	    	PanelSingleFasciaEtaCtrl jPSFE = (PanelSingleFasciaEtaCtrl)h_xord.get((String)vKeyOrd.elementAt(k));
	      // ctrl che etaDa della fascia successiva sia > della etaA della precedente
	      tuttoOk = (jPSFE.getEtaDa() > etaA_old);
	      if (tuttoOk)
	        etaA_old = jPSFE.getEtaA();
	      k++;
	    }

	    if (!tuttoOk){
	      Integer tit_1 = ((PanelSingleFasciaEtaCtrl)h_xord.get((String)vKeyOrd.elementAt(k-2))).getEtaDa();
	      Integer tit_2 = ((PanelSingleFasciaEtaCtrl)h_xord.get((String)vKeyOrd.elementAt(k-1))).getEtaA();
	      Messagebox.show(
	    		  	Labels.getLabel("SinglePanelFasciaEta.msg.error2",new String[] {tit_1.toString(),tit_2.toString()}),
					Labels.getLabel("messagebox.attention"),
					Messagebox.OK,
					Messagebox.ERROR);
	    }
	    return tuttoOk;
	  }

	  // costruzione valori da ritornare: etaDA_1-etaA_1|etaDA_2-etaA_2|....|etaDA_n-etaA_n
	  public String getValFasce()
	  {
		Component singlePanelCorr;
	    StringBuffer strBufVal = new StringBuffer();
	    for (int j=0; j<arrTestiLabel.length; j++) {
		  singlePanelCorr = self.getFellow("single_panel_id"+j);
		  PanelSingleFasciaEtaCtrl c1 = (PanelSingleFasciaEtaCtrl)singlePanelCorr.getAttribute(MY_CTRL_KEY);
	      if ((strBufVal.length() > 0) && (c1.isChecked()))
	        strBufVal.append("|");
	      strBufVal.append(c1.getValSingleFascia());
	    }
	    return strBufVal.toString();
	  }


	  public void setArrTestoLabel(String[] aTLab)
	  {
	    this.arrTestiLabel = aTLab;
	  }

	  public String[] getArrTestoLabel()
	  {
	    return this.arrTestiLabel;
	  }

	  private Vector orderedKeys(Hashtable hOrdinare)
	  {
	    Enumeration keys = hOrdinare.keys();
	    Vector temp = new Vector();
	    while(keys.hasMoreElements()) {
	      temp.addElement("" + keys.nextElement());
	    }
	    Collections.sort(temp);
	    return temp;
	  }

	  private String aggiungiZeri(int num)
	  {
	    String num_str = "" + num;
	    while (num_str.length() < 3)
	      num_str = "0" + num_str;
	    return num_str;
	  }
	
	
	
	
	
	
	
	
}
package it.caribel.app.sinssnt.controllers.pianoAssistenziale;

import it.caribel.app.sinssnt.bean.modificati.PianoAssistEJB;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.caribel.zk.util.UtilForUI;
import it.pisa.caribel.util.procdate;

import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;


public class PannelloDataFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = -5295859076639542148L;

	public final static String myZul = "/web/ui/sinssnt/piano_assistenziale/pannelloData.zul";
	
	private CaribelDatebox data;
	private Label message;
	
	public void doInitForm() {
		try {
			((Window) getForm()).setTitle((String) arg.get("titolo"));
			if(arg.containsKey("mess")){
				message.setValue((String) arg.get("mess"));
			}
			data.setValue(procdate.getDate());
		}catch(Exception e){
			doShowException(e);
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void onOk(ForwardEvent e) throws Exception{
		String data_input = data.getValueForIsas();
		if((data_input != null) && !data_input.equals("/__/")){
			//gb 08/08/07 *******
//			String data_ultima = arg.get("dataUltma");
			String dt1=(String) arg.get("dataUltima");;//data ultimo piano inserito
			String dt2=data_input;//data inserita che deve essere maggiore stretta
//			dt2=dt2.substring(0,2)+dt2.substring(3,5)+dt2.substring(6,10);
//			DataWI dataINIZIO=new DataWI(dt2);
//			dt1=dt1.substring(6,10)+dt1.substring(3,5)+dt1.substring(0,2);
//			int rit=dataINIZIO.confrontaConDt(dt1);
			if(dt2.compareTo(dt1)<=0){//uguale o minore
				UtilForUI.standardExclamation(Labels.getLabel("pianoassistenziale.msg.dataMaggioreDellaPrecedente", new String[]{dt1}));
				return;
			}

			// Chiamo la query che mi va a chiudere i piani aperti
			// l'EJB si aspetta la data in formato aaaa-mm-gg.
			//logger.info("\n-->> revData: " + revData + "\n");
			Hashtable<String, String> ht = new Hashtable<String, String>();
			ht.putAll(arg);
			ht.put("data_chiusura", data_input);
			int count1 = (Integer) invokeGenericSuEJB(new PianoAssistEJB(), ht, "chiudi_piani");//db.ChiudiPiani(ht);
			//gb 08/08/07 *******
//TODO VFR passare indietro i dati
			if (count1 >= 0){
//				gl_strUltimaDataChiusura = data_input;
//			//gb 08/08/07: fine *******
//			return (count1 >= 0);
				((PianoAssistenzialeGridCtrl) this.getForm().getParent().getAttribute(MY_CTRL_KEY)).setInfoJCariContainer(1, data_input);
				getForm().detach();
			}
		}
	}

	public void onClose(ForwardEvent e) throws Exception{
		getForm().detach();
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}
	
	@Override
	protected boolean doSaveForm() throws Exception {
		return false;
	}
	
	
	
}

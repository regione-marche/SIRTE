package it.caribel.app.sinssnt.controllers.agenda;

import org.zkoss.zk.ui.event.ForwardEvent;


public class PannelloDataFormCtrl extends it.caribel.zk.generic_controllers.messagebox.PannelloDataFormCtrl{

	private static final long serialVersionUID = -5295859076639542148L;

	public final static String myZul = "/web/ui/sinssnt/agenda/pannelloData.zul";
	
	
	
	@SuppressWarnings("unchecked")
	public void onOk(ForwardEvent e) throws Exception{
		String data_input = data.getValueForIsas();
		if((data_input != null) && !data_input.equals("/__/")){
			String dt1=(String) arg.get("dataUltima");;//data ultimo piano inserito
			String dt2=data_input;//data inserita che deve essere maggiore stretta

			((AgendaPianSettFormCtrl) this.getForm().getParent().getAttribute(MY_CTRL_KEY)).setDataInput(data_input);
			getForm().detach();
		}
	}

	public void onClose(ForwardEvent e) throws Exception{
		getForm().detach();
	}
}

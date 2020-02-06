package it.caribel.app.sinssnt.controllers.accessi_mmg;

import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

import it.caribel.app.sinssnt.bean.nuovi.AccessiMmgWebAppEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMSkSOEJB;
import it.caribel.zk.composite_components.CaribelListModel;
import it.pisa.caribel.isas2.ISASRecord;


public class AccessiMmgViewWACtrl extends AccessiMmgViewCtrl{

	private static final long serialVersionUID = 1L;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		try{
			super.dataCtrl = new AccessiMmgDataCtrl(new AccessiMmgWebAppEJB());
			super.doAfterCompose(comp);
		}catch(Exception e){
			doShowException(e);
		}
	}
	@Override
	public void doLoadPrestazErogate()throws Exception  {
		boolean res = true;
		try{
			Hashtable h = new Hashtable();
			h.put("n_cartella",n_cartella.getValue());
			if (!n_cartella.getValue().equals("")){
				ISASRecord dbr = (ISASRecord)invokeGenericSuEJB(new RMSkSOEJB(), h, "selectZonaSkValCorrente");
				if (dbr!=null){
					final String distretto_cod = dbr.get("cod_distretto_verbale").toString();
					final String zona_cod = dbr.get("zona_cod").toString();
					final String id_skso = dbr.get("id_skso").toString();
					
					String zona = dbr.get("gid").toString();
					String zona_desc = dbr.get("zona_desc").toString();
					if(!getProfile().getIsasUser().isInGroup(new Integer(zona))){				
				Messagebox.show(Labels.getLabel("accessi.eccezione.altra_zona",new String[]{zona_desc}), 
						Labels.getLabel("messagebox.attention"), Messagebox.OK,Messagebox.EXCLAMATION
						);
					res = false;
					}
				}
			}
		
			}catch (Exception ex2) {
				doShowException(ex2);
			}
		if (res)
		super.doLoadPrestazErogate();
		else {
			this.n_cartella.setText("");
			this.cognomeAss.setText("");
		}
	}
	
	
}

	

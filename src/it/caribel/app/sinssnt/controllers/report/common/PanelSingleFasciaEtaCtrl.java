package it.caribel.app.sinssnt.controllers.report.common;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelIntbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelForwardComposer;
import it.caribel.zk.util.UtilForComponents;

public class PanelSingleFasciaEtaCtrl extends CaribelForwardComposer {
	private static final long serialVersionUID = 1L;

	protected CaribelIntbox tb1;
	protected CaribelIntbox tb2;
	protected CaribelCheckbox cbx;
    
    public void doAfterCompose(Component comp) throws Exception {
    	super.doAfterCompose(comp);
    	
    	List<Component> listComp = UtilForComponents.getAllChildren(comp);
		Component corrComp = null;
		for(int i=0; i<listComp.size();i++){
			corrComp = listComp.get(i);
			if(corrComp instanceof CaribelIntbox) {
				if(tb1 ==null)
					tb1 = (CaribelIntbox)corrComp;
				else
					tb2 = (CaribelIntbox)corrComp;
			}else if(corrComp instanceof CaribelCheckbox) {
				cbx = (CaribelCheckbox)corrComp;
			}
		}
		
		tb1.setReadonly(true);
		tb2.setReadonly(true);
		
		cbx.addEventListener(Events.ON_CHECK, new EventListener<Event>() {			
			public void onEvent(Event event) throws Exception {
				if(tb1.getValue()==null || tb1.getText().equals(""))					
					tb1.setValue(0);
				if(tb2.getValue()==null || tb2.getText().equals(""))
					tb2.setValue(999);
				tb1.setRequired(cbx.isChecked());
				tb2.setRequired(cbx.isChecked());
				tb1.setReadonly(!cbx.isChecked());
				tb2.setReadonly(!cbx.isChecked());
				tb1.setFocus(cbx.isChecked());
			}});
    }
    
    public boolean isChecked(){
    	return cbx.isChecked();  	
    }
    
    public boolean isValid(){
    	if(tb1.getValue() < tb2.getValue())
    		return true;
    	return false;
    }
    
    public int getEtaDa(){
    	return tb1.getValue();
    }
    
    public int getEtaA(){
    	return tb2.getValue();
    }
    
    public void setEtaDa(int eta){
    	tb1.setValue(eta);    	
    }
    
    public String getValSingleFascia()
    {
      String val = "";
      if (cbx.isChecked())
        val = getEtaDa() + "-" + getEtaA();
      return val;
    }
    
}
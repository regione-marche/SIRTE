package it.caribel.app.sinssnt.controllers.palliat_protesica;

import it.caribel.app.sinssnt.bean.modificati.SkInfEJB;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.zk.composite_components.CaribelRadiogroup;
import it.caribel.zk.generic_controllers.CaribelGridCtrl;
import it.caribel.zk.util.UtilForBinding;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Radio;


public class ProtesicaGridCtrl extends CaribelGridCtrl 
{
	private static final long serialVersionUID = 1L;
	
	private String myKeyPermission = "";  
	private SkInfEJB myEJB = new SkInfEJB();
	public static final String myPathFormZul = "/web/ui/sinssnt/contatto_palliativista/protesicaForm.zul";

	private CaribelRadiogroup mte_tipocons;	
	private Radio mte_tipocons_tutti;
	private Radio mte_tipocons_proprieta;
	private Radio mte_tipocons_uso;
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	
	public void doAfterCompose(Component comp) throws Exception 
	{
		super.initCaribelGridCtrl(myEJB, myKeyPermission, myPathFormZul);
		super.doAfterCompose(comp);
	
//		this.setMethodNameForQuery("queryPaginate");
		
		if(super.caribelSearchCtrl!=null)
		{
			super.hParameters.putAll(super.caribelSearchCtrl.getLinkedParameterForQuery());
			UtilForBinding.bindDataToComponent(logger,super.caribelSearchCtrl.getLinkedParameterForQuery(),self);
			String textToSearch = (String)arg.get(Costanti.N_CARTELLA);
			if(textToSearch!=null && !textToSearch.trim().equals("")){
				textToSearch = textToSearch.toUpperCase();
				
				doCerca();
			}
		}
		if(super.caribelContainerCtrl!=null)
		{
			Object ncartella = super.caribelContainerCtrl.hashChiaveValore.get("n_cartella");
											
			super.hParameters.put(Costanti.N_CARTELLA, ncartella.toString());
			super.hParameters.put("tipo", mte_tipocons.getSelectedValue());

			doRefresh();
		}
    }
	
	/* ----------------------------------------------------------------------------------------- */
	
	public void onCheck$mte_tipocons_tutti()
	{
		super.hParameters.put("tipo", "T");
		doRefresh();		
	}
	
	public void onCheck$mte_tipocons_uso()
	{
		super.hParameters.put("tipo", "U");
		doRefresh();		
	}
	
	public void onCheck$mte_tipocons_proprieta()
	{
		super.hParameters.put("tipo", "P");
		doRefresh();		
	}
	
	/* ----------------------------------------------------------------------------------------- */
	
	@Override
	protected void doStampa() 
	{
		// TODO Auto-generated method stub
	}



	@Override
	public void doCerca() 
	{
		// TODO Auto-generated method stub		
	}
	
	/* ------------------------------------------------------------------------------------------------------------------------- */
	

	
}

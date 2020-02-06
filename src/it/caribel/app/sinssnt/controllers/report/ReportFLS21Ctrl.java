package it.caribel.app.sinssnt.controllers.report;

import it.caribel.app.common.controllers.ubicazione.PanelUbicazioneCtrl;
import it.caribel.app.sinssnt.controllers.login.ManagerProfile;
import it.caribel.app.sinssnt.util.ChiaviISASSinssntWeb;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.app.sinssnt.util.ManagerChiaviISAS;
import it.caribel.util.CaribelSessionManager;
import it.caribel.zk.composite_components.CaribelCombobox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;
import it.pisa.caribel.isas2.ISASUser;

import org.zkoss.zk.ui.Component;

public class ReportFLS21Ctrl extends CaribelFormCtrl{
//	protected CaribelRadiogroup formatoStampa;
	
	protected CaribelTextbox anno;
	private String myKeyPermissionSu = ChiaviISASSinssntWeb.ESTRAZIONE_FLUSSI_FLS21;
	protected CaribelCombobox distretto;
	protected CaribelCombobox zona;
//	protected Radio pdf;
//	protected Radio html;
//	protected Radio fls21;
	boolean estrAreaVasta = false;
	private PanelUbicazioneCtrl c;

	@Override
	public void doInitForm() {
		Component p = self.getFellow("panel_ubicazione");
		c = (PanelUbicazioneCtrl) p.getAttribute(MY_CTRL_KEY);
		//estrAreaVasta = ManagerProfile.isAbilitazione(getProfile(), ManagerProfile.ESTRAZIONE_FLUSSI_FLS21_X_AREA);
		//x debug
		estrAreaVasta = false;
		
		c.setDistrettiVoceTutti(estrAreaVasta);
		
		c.doInitPanel();
		c.settaRaggrContatti("CA");
		c.setVisibleZona(estrAreaVasta);
		c.setVisibleDistretto(!estrAreaVasta);
		c.setVisiblePresidioComuneAreaDis(false);

		zona.setSelectedValue(getProfile().getStringFromProfile(ManagerProfile.ZONA_OPERATORE));
		
		c.setDistrettoValue(estrAreaVasta?Costanti.CTS_DISTRETTI_VOCE_TUTTI:getProfile().getStringFromProfile(ManagerProfile.DISTRETTO_OPERATORE));
		c.setDistrettoRequired(!estrAreaVasta);
		c.setDistrettoDisabilita(!estrAreaVasta);
		
		ISASUser user= getProfile().getIsasUser(); 
		if (user.canIUse(myKeyPermissionSu, ManagerChiaviISAS.MODI)){
			c.setDistrettoDisabilita(false);
		}
		
		
	}

	@Override
	protected boolean doValidateForm() throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	
	public void doStampa() {
		try {
			
		    
              String y = anno.getValue();
              String distr = distretto.getSelectedValue();
              String area = zona.getSelectedValue();
              String tipo = "A";
              if (!this.estrAreaVasta) tipo = "D";
//            String formato = this.formatoStampa.getSelectedValue();
            String report="";
            String TYPE="";
            String ejb="";
//            if (formato.equals("P")){
//              report="flussi_21.fo";
//              TYPE="PDF";
//              ejb ="SINS_FOFLUSSI21L";
//            }
//            else if (formato.equals("E")){
//              report="flussi_21.html";
//              TYPE="application/vnd.ms-excel";
//              ejb ="SINS_FOFLUSSI21L";
//            }
//            else if (formato.equals("F")){
              report="flussi_21_word.fo";
              TYPE="PDF";
              ejb ="SINS_FOFLUSSI21";
//            }

            //invocazione alla servlet
            String servlet = "/SINSSNTFoServlet/SINSSNTFoServlet"+ "?EJB=" + ejb +
                        "&METHOD=query_flussi" +
                        "&anno=" + y +
                        "&distr=" + distr +
                        "&area=" + area +
                        "&tipo="+tipo+
                        "&USER=" + CaribelSessionManager.getInstance().getMyLogin().getUser() +
                        "&ID509=" + CaribelSessionManager.getInstance().getMyLogin().getPassword() 
                        + "&REPORT="+report+"&TYPE="+TYPE;
            System.out.println("percorso servlet FoFlussi21 "+servlet);
           
            it.caribel.app.common.controllers.report.ReportLauncher.launchReport(self,servlet);
           
			//self.detach();
		}catch(Exception e){
			doShowException(e);
		}
	}
	
}

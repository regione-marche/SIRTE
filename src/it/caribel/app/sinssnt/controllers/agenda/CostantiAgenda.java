/**
 * 
 */
package it.caribel.app.sinssnt.controllers.agenda;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import org.zkoss.util.resource.Labels;

/**
 * @author Valerio Franchi
 *  
 */
public class CostantiAgenda extends CostantiAgendaBase {

//	public static final int NUMFASCIEORARIE = 4;
//	private static final String DELIMITATORE_GIORNI = "#";
//	public static final String PIANIFICAZIONE_PAI_VUOTA = "NNNNNNN";
	
	public static Vector<Hashtable<String, String>> getHashtablePianificazione(String pianificazione){
		Vector<Hashtable<String, String>> ret = new Vector<Hashtable<String, String>>();
		Hashtable<String, String> fascia;
		if(pianificazione!= null && !pianificazione.isEmpty()){
			StringTokenizer strT = new StringTokenizer(pianificazione, DELIMITATORE_GIORNI);
			int i=0;
			for (; i < NUMFASCIEORARIE && strT.hasMoreTokens(); i++) {
				fascia = new Hashtable<String, String>();
				String checks = (String) strT.nextToken();
				int j = 0;
				for (; j < checks.length(); j++) {
					fascia.put(j+"", checks.charAt(j)+"");
				}
				if(j<7){
					for (; j < 7; j++) {
						fascia.put(j+"", "N");
					}
				}
				fascia.put("matt_pom", Labels.getLabel("agenda.fasce."+i));
				ret.add(fascia);
			}
			if(i < NUMFASCIEORARIE){
				for (; i < NUMFASCIEORARIE; i++) {
					ret.add(new Hashtable<String, String>());
				}
			}
		}
		return ret;
	}

//	public static String getPianificazioneVuota() {
//		StringBuffer tmp = new StringBuffer();
//		for (int i = 0; i < NUMFASCIEORARIE; i++) {
//			tmp.append(PIANIFICAZIONE_PAI_VUOTA);
//			tmp.append(DELIMITATORE_GIORNI);
//		}
//		return tmp.toString().substring(0, tmp.length()-2);
//	}

	public static String getStringPianificazione(Vector<Hashtable<String, String>> dataFromGrid) {
		Hashtable<String, String> hashtable;
		StringBuffer pattern = new StringBuffer();
		String value="";
		for (Iterator<Hashtable<String, String>> iterator = dataFromGrid.iterator(); iterator.hasNext();) {
			hashtable = (Hashtable<String, String>) iterator.next();
			for (int i = 0; i < 7; i++) {
				value = hashtable.get(i+"");
				pattern.append(value.equals("S") ? value : "N");
			}
			pattern.append(DELIMITATORE_GIORNI);
		}
		return pattern.toString().substring(0, pattern.length()-1);
	}
}

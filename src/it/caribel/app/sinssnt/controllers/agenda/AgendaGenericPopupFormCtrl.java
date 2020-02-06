package it.caribel.app.sinssnt.controllers.agenda;

import it.caribel.util.FiltroCodiceDescrizione;
import it.caribel.zk.composite_components.CaribelListModel;
import it.caribel.zk.composite_components.CaribelListbox;
import it.caribel.zk.composite_components.CaribelTextbox;
import it.caribel.zk.generic_controllers.CaribelFormCtrl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.impl.InputElement;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class AgendaGenericPopupFormCtrl extends CaribelFormCtrl{

	private static final long serialVersionUID = -1344487534855481532L;
	protected Predicate filtroPrestazioni;
	protected String codicePrestazioneGriglia 		= "prest_cod";
	protected String descrizionePrestazioneGriglia	= "prest_des";
	protected CaribelListbox tableGrigliaPrestazioni;
	protected CaribelTextbox filtroDescrizione;
	protected CaribelListModel modelloPrestazioni = new CaribelListModel();
	
	@Override
	public void doInitForm() {
		filtroPrestazioni = new FiltroCodiceDescrizione(null, filtroDescrizione, tableGrigliaPrestazioni, codicePrestazioneGriglia, descrizionePrestazioneGriglia);
		((FiltroCodiceDescrizione)filtroPrestazioni).setMantieniSelezione(true);
		filtroDescrizione.addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
			public void onEvent(Event event){
				try{
					if(event.getName().equals(Events.ON_CHANGING)){
						((InputElement) event.getTarget()).setRawValue(((InputEvent)event).getValue().toUpperCase());
						Set<String> prestazioni = new TreeSet<String>();
						for (Iterator<Listitem> iterator = tableGrigliaPrestazioni.getSelectedItems().iterator(); iterator.hasNext();) {
							Listitem litem = (Listitem) iterator.next();
							String cod_prest = (String)((CaribelListModel<?>) tableGrigliaPrestazioni.getModel()).getFromRow(litem, codicePrestazioneGriglia);
							prestazioni.add(cod_prest.trim());
						}
						Collection<?> col = getPrestazioni();
						CaribelListModel mod = new CaribelListModel(col);
						tableGrigliaPrestazioni.setModel(mod);

						Set tmp = new HashSet(tableGrigliaPrestazioni.getSelectedItems());

						for (Iterator iterator = prestazioni.iterator(); iterator.hasNext();) {
							String codice = (String) iterator.next();
							if (codice != null) {
								//ricerco il codice nella griglia
								Hashtable hTrova = new Hashtable();
								hTrova.put(codicePrestazioneGriglia, codice);
								int riga = mod.columnsContains(hTrova);
								if (riga != -1) {
									logger.trace("Selezione della prestazione: "+codice);
									Object o = mod.remove(riga);
									mod.add(0, o);
									tmp.add(tableGrigliaPrestazioni.getItemAtIndex(0));
								}
							}
						}//fine for
						mod.setMultiple(true);
						tableGrigliaPrestazioni.setSelectedItems(tmp);
						tableGrigliaPrestazioni.invalidate();
					}
				}catch(Exception e){
					doShowException(e);
				}
			}});
	}

	public Collection<?> getPrestazioni() {
		return CollectionUtils.select(modelloPrestazioni != null ? modelloPrestazioni : CollectionUtils.EMPTY_COLLECTION, filtroPrestazioni);
	}
	
	@Override
	protected boolean doValidateForm() throws Exception {
		return true;
	}


}

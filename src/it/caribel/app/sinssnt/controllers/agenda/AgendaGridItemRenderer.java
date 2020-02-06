package it.caribel.app.sinssnt.controllers.agenda;


import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.sys.ComponentsCtrl;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;

@SuppressWarnings("rawtypes")
public class AgendaGridItemRenderer implements ListitemRenderer{

	private EventListener<? extends Event> eventListener = null;

	public AgendaGridItemRenderer(){
		super();
	}

	public AgendaGridItemRenderer(EventListener<Event> evtl){
		super();
		this.eventListener = evtl;
	}
	
	@SuppressWarnings("unchecked")
	public void render(Listitem item, Object myData, int index) throws Exception {
		CaribelDatebox temp = new CaribelDatebox();
//		ComponentsCtrl.applyForward(item, "onDoubleClick=onDoubleClickedItem");

		final Listbox listbox = (Listbox)item.getParent();
		Listhead testa = listbox.getListhead();

		Hashtable<String, ?> data = null;
		if(myData instanceof ISASRecord){
			data = ((ISASRecord) myData).getHashtable();
			item.setAttribute("dbr_from_grid", myData);
			item.setAttribute("ht_from_grid", data);
		}else if(myData instanceof Hashtable){
			data = (Hashtable<String, ?>)myData;
			item.setAttribute("dbr_from_grid", null);
			item.setAttribute("ht_from_grid", myData);
		}

		if((item.getIndex()/CostantiAgenda.NUMFASCIEORARIE)%2==0){
			item.setSclass("z-listbox-odd");
		}
		
		if(testa != null){
			boolean column_editable = false;
			String column_type = "";
			String dbName = "";
			String valCorr = "";
			Object obj = null;
			for (Object header : testa.getChildren()) {
				dbName = ((CaribelListheader)header).getDb_name();
				column_editable = ((CaribelListheader)header).isColumn_editable();
				column_type = ((CaribelListheader)header).getColumn_type();
				obj = data.get(dbName);
				if(dbName.equals("assistito")){
					int ind = index%CostantiAgenda.NUMFASCIEORARIE;
					switch (ind) {
					case 1:
						obj = ISASUtil.getValoreStringa(data, "indirizzo") + " " + ISASUtil.getValoreStringa(data, "telefono") + " " + ISASUtil.getValoreStringa(data, "medico");
						break;
					case 2:
						obj = data.get("telefono");
						break;
					case 3:
						obj = data.get("medico");
						break;
					default:
						obj = data.get(dbName) + " " + ISASUtil.getValoreStringa(data, "ag_cartella") + ISASUtil.getValoreStringa(data, "n_cartella");
						
						break;
					}
				}
				Integer fascia = data.containsKey("ag_orario") ? (Integer)data.get("ag_orario"):(Integer)data.get("as_orario");
				
				if(dbName.equals("matt_pom") && (obj == null || obj.toString().equals(""))){
					obj = Labels.getLabel("agenda.fasce."+fascia);
				}
				if(obj==null)
					valCorr = "";
				else if(obj instanceof java.sql.Date){
					temp.setValue((java.sql.Date)obj);
					valCorr=temp.getText();
				}else if(obj instanceof java.util.Date){
					temp.setValue((java.util.Date)obj);
					valCorr=temp.getText();
				}else if(obj instanceof java.lang.Integer){
					valCorr =((java.lang.Integer)obj).toString();
				}else if(obj instanceof java.lang.Double){
					valCorr = new DecimalFormat("#,##0.00").format(((java.lang.Double)obj));
				}else if(obj instanceof BigDecimal){
					valCorr = new DecimalFormat("#,##0.00").format(((BigDecimal)obj).doubleValue());
				}else{
					valCorr= (String)obj;
				}
					
				if(column_editable && !column_type.equals("")){
					//TODO LEO realizzare tutti i casi di column_editable ;
					//per ogni tipo di componente editabile si dovrebbe costruire il valore opportunemente:
					//se myData instanceof Hashtable i dati non sono tipizzati (sono tutte stringhe)!!
					//
					if(column_type.equals(CaribelListheader.COLUMN_TYPE_DATE)){					
						CaribelDatebox corr = new CaribelDatebox();
						if(obj instanceof java.sql.Date){
							corr.setValue((java.sql.Date)obj);
						}
						Listcell cell = new Listcell();
						cell.appendChild(corr);
						item.appendChild(cell);
					}else if(column_type.equals(CaribelListheader.COLUMN_TYPE_BOOL)){
						CaribelCheckbox corrComp = new CaribelCheckbox();
						if(obj instanceof Boolean){
							corrComp.setChecked(((Boolean)obj).booleanValue());
						}else if(obj instanceof String){
							corrComp.setChecked(((String)obj).equals("S"));
//							corrComp.setSelectedValue(obj);
						}
						Listcell cell = new Listcell();
						ComponentsCtrl.applyForward(corrComp, "onCheck=onCheckCell");
						ComponentsCtrl.applyForward(cell, "onClick=onClickedCell");
						cell.appendChild(corrComp);
						item.appendChild(cell);
					}else if(column_type.equals(CaribelListheader.COLUMN_TYPE_BUTTON)){
						Button btn = new Button(((CaribelListheader)header).getLabel());
						btn.addEventListener(Events.ON_CLICK, eventListener);
						Listcell cell = new Listcell();
						cell.appendChild(btn);
						item.appendChild(cell);
					}else{
						item.appendChild(new Listcell(valCorr));
					}
				}else{
					Listcell lc = new Listcell(valCorr);
					String sclass = "cellNumber";
					if(!dbName.equals("assistito")){
//						Integer.parseInt(dbName);
						if(dbName.length()==1){
							Object stato = data.get("stato"+dbName);
							if(stato!=null){
								sclass=sclass + " agenda"+stato;
							}
							if(valCorr!=null){
								lc.setTooltiptext((String) data.get("prestazioni_desc"+dbName));
							}
						}
						lc.setSclass(sclass);
					}else{
						if(index%CostantiAgenda.NUMFASCIEORARIE==0){
							lc.setSclass("assistitoInAgenda");
							String intensita = (String) data.get("sk_motivo");
							if(intensita!= null && intensita.equals(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
								lc.setSclass("assistitoInAgenda assistitoADI");
							}
						}
						if(index%CostantiAgenda.NUMFASCIEORARIE==1){
							lc.setWidgetAttribute("rowspan", "3");
						}else if(index%CostantiAgenda.NUMFASCIEORARIE>1){
							continue;
						}
					}
//					} catch (NumberFormatException e) {
//					}
					if(listbox.getId().equals("caribellbAgendaAssistito")){
						ComponentsCtrl.applyForward(lc, "onDoubleClick=onDoubleClickedCell");
						ComponentsCtrl.applyForward(lc, "onClick=onClickedCell");
						lc.setAttribute("dbName", dbName);
					}
					item.appendChild(lc);
				}
			}
		}
	}
}
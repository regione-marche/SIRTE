package it.caribel.app.sinssnt.controllers.agenda.agendaMultiOperatore;


import it.caribel.app.common.ejb.DiarioEJB;
import it.caribel.app.sinssnt.bean.nuovi.RMDiarioEJB;
import it.caribel.app.sinssnt.controllers.agenda.CostantiAgenda;
import it.caribel.app.sinssnt.util.Costanti;
import it.caribel.util.CaribelSessionManager;
import it.caribel.util.StripTags;
import it.caribel.zk.composite_components.CaribelCheckbox;
import it.caribel.zk.composite_components.CaribelDatebox;
import it.caribel.zk.composite_components.CaribelListheader;
import it.pisa.caribel.isas2.ISASRecord;
import it.pisa.caribel.util.ISASUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Vector;

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
import org.zkoss.zul.Window;

@SuppressWarnings("rawtypes")
public class AgendaMultiOperatoreGridItemRenderer implements ListitemRenderer{

	private EventListener<? extends Event> eventListener = null;

	public AgendaMultiOperatoreGridItemRenderer(){
		super();
	}

	public AgendaMultiOperatoreGridItemRenderer(EventListener<Event> evtl){
		super();
		this.eventListener = evtl;
	}
	
	@SuppressWarnings("unchecked")
	public void render(Listitem item, Object myData, int index) throws Exception {
		CaribelDatebox temp = new CaribelDatebox();
//		ComponentsCtrl.applyForward(item, "onDoubleClick=onDoubleClickedItem");

		final Listbox listbox = (Listbox)item.getParent();
		Listhead testa = listbox.getListhead();

		Hashtable<String, Object> data = null;
		if(myData instanceof ISASRecord){
			data = ((ISASRecord) myData).getHashtable();
			item.setAttribute("dbr_from_grid", myData);
			item.setAttribute("ht_from_grid", data);
		}else if(myData instanceof Hashtable){
			data = (Hashtable<String, Object>)myData;
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
					obj = Labels.getLabel("agendaRegistra.assistito",
							new String[]{ISASUtil.getValoreStringa(data, "assistito"),
							 			ISASUtil.getValoreStringa(data, "ag_cartella") + ISASUtil.getValoreStringa(data, "n_cartella"),
										 ISASUtil.getValoreStringa(data, "indirizzo"),
										 ISASUtil.getValoreStringa(data, "telefono"),
										 ISASUtil.getValoreStringa(data, "medico"),
										});
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
					if(dbName.equals("assistito")){
						if(index%CostantiAgenda.NUMFASCIEORARIE==0){
							sclass = "assistitoInAgenda";
							lc.setWidgetAttribute("rowspan", "4");
							String intensita = (String) data.get("sk_motivo");
							if(intensita!= null && intensita.equals(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
								sclass = "assistitoInAgenda assistitoADI";
							}
							Button btn = new Button();
							btn.setImage("/web/img/agenda/diario.png");
							btn.setTooltiptext(Labels.getLabel("agenda.tooltiptext.apriDiario"));
							btn.setStyle("background-color: Transparent; background-repeat:no-repeat; border: none; cursor:pointer; overflow: hidden; padding-left: 0px;");
							btn.addEventListener(Events.ON_CLICK, new  AgendaEL(data));
//							btn.addEventListener(Events.ON_CLICK, eventListener);
							lc.appendChild(btn);
							Button btn2 = new Button();
							btn2.setImage("/web/img/patient_chart.png");
							btn2.setTooltiptext(Labels.getLabel("agenda.tooltiptext.stampaScheda"));
							btn2.setStyle("background-color: Transparent; background-repeat:no-repeat; border: none; cursor:pointer; overflow: hidden; padding-left: 0px;");
							btn2.addEventListener("onClick", new  AgendaEL(data));
							lc.appendChild(btn2);
						
						}else {
							continue;
						}
//						if(index%CostantiAgenda.NUMFASCIEORARIE==0){
//							lc.setSclass("assistitoInAgenda");
//							String intensita = (String) data.get("sk_motivo");
//							if(intensita!= null && intensita.equals(Costanti.CTS_COD_CURE_DOMICILIARI_INTEGRATE)){
//								lc.setSclass("assistitoInAgenda assistitoADI");
//							}
//						}
//						if(index%CostantiAgenda.NUMFASCIEORARIE==1){
//							lc.setWidgetAttribute("rowspan", "3");
//						}else if(index%CostantiAgenda.NUMFASCIEORARIE>1){
//							continue;
//						}
					}if(dbName.equals("ag_oper_ref")){ 
						if(index%CostantiAgenda.NUMFASCIEORARIE==0){
							lc.setLabel((data.containsKey("ag_oper_ref_desc") ? data.get("ag_oper_ref_desc"):"")+"("+ lc.getLabel() +")"); 
							lc.setWidgetAttribute("rowspan", "4");
						}else if(index%CostantiAgenda.NUMFASCIEORARIE>0){
							continue;
						}
					}else{
						if(dbName.length()==1){
							Object stato = data.get("stato"+dbName);
							Object prestazioni = valCorr;
							if(stato!=null && prestazioni!=null && !((String)prestazioni).isEmpty()){
								sclass=sclass + " agenda"+stato;
							}
							if(valCorr!=null){
								lc.setTooltiptext((String) data.get("prestazioni_desc"+dbName));
							}
							if(stato!=null){
								int statoReg = Integer.parseInt(stato+"");
								if(statoReg>0){
									Hashtable h = new Hashtable<String, String>();		
									h.put("n_cartella", data.get("ag_cartella")+"");
									h.put("n_contatto", data.get("ag_contatto")+"");
									h.put("tipo_operatore", data.get("ag_tipo_oper"));
									h.put("dadata", data.get("ag_data"));
									h.put("adata", data.get("ag_data"));
	
									RMDiarioEJB diarioEJB = new RMDiarioEJB();
									Vector<ISASRecord> obj1 = diarioEJB .query(CaribelSessionManager.getInstance().getMyLogin(), h );
									if(obj != null && obj1.size()>0){
										ISASRecord diar = obj1.get(0);
										h.put("progr_inse", diar.get("progr_inse")+"");
										h.put("progr_modi", diar.get("progr_modi")+"");
										diar = diarioEJB.queryKey(CaribelSessionManager.getInstance().getMyLogin(), h);
										String note = (String) diar.get("testo");
										if(note!= null && !note.isEmpty()){
											note = StripTags.stripBlanks(StripTags.stripNewLines(StripTags.strip(note)));
											data.put("note", note);
											Button btn = new Button();
											btn.setImage("~./zul/img/grid_update_16x16.png");
											btn.setTooltiptext(note);//Labels.getLabel("agenda.tooltiptext.noteAccesso"));
											btn.setStyle("background-color: Transparent; background-repeat:no-repeat; border: none; cursor:pointer; overflow: hidden; padding-left: 0px;");
											lc.appendChild(btn);
	//										btn.addEventListener(Events.ON_CLICK, new  AgendaEL(data));
										}
									}
								}
							}
						}
						lc.setSclass(sclass);
					}
//					} catch (NumberFormatException e) {
//					}
					if(listbox.getId().equals("caribellbAgendaAssistito")){
						ComponentsCtrl.applyForward(lc, "onDoubleClick=onDoubleClickedCell");
						ComponentsCtrl.applyForward(lc, "onClick=onClickedCell");
						lc.setAttribute("dbName", dbName);
					}
					item.appendChild(lc);
					item.invalidate();
				}
			}
		}
	}


	public class AgendaEL implements EventListener<Event>{
		
		private Hashtable data;
		
		public AgendaEL(Hashtable data) {
			this.data = data;
		}
		
		@Override
		public void onEvent(Event event) throws Exception{
			Window myWin = (Window)event.getTarget().getParent().getSpaceOwner();
			String tool = ((Button) event.getTarget()).getTooltiptext();
			if (Labels.getLabel("agenda.tooltiptext.apriDiario").equals(tool)){
            	Events.sendEvent(AgendaMultiOperatoreRegistraFormCtrl.APRIDIARIO,myWin,data); 
			}else if(Labels.getLabel("agenda.tooltiptext.stampaScheda").equals(tool)){
            	Events.sendEvent(AgendaMultiOperatoreRegistraFormCtrl.STAMPASCHEDA,myWin,data); 
//			}else if(){
//				
			}
		}
	}
}
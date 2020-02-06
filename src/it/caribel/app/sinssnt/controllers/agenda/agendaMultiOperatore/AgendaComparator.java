package it.caribel.app.sinssnt.controllers.agenda.agendaMultiOperatore;

import it.pisa.caribel.isas2.ISASMisuseException;
import it.pisa.caribel.isas2.ISASRecord;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Hashtable;
 

public class AgendaComparator implements Comparator<Object>, Serializable {
    private static final long serialVersionUID = -2127053833562854322L;
     
    private boolean asc = true;
    private int type = 0;
 
    public AgendaComparator(boolean asc, int type) {
        this.asc = asc;
        this.type = type;
    }
 
    public int getType() {
        return type;
    }
 
    public void setType(int type) {
        this.type = type;
    }
 
    @Override
    public int compare(Object o1, Object o2) {
        Hashtable rec1 = (Hashtable) o1;
        Hashtable rec2 = (Hashtable) o2;
        Integer a = (Integer)rec1.get("o"+type);
        Integer b = (Integer)rec2.get("o"+type);
        if(a==null){
        	if(b==null){
        		return 0;
        	}else{
        		return (asc ? 1 : -1);
        	}
        }
        if(b==null){
        	return (asc ? -1 : 1);
        }else{
        	String comp = a+rec1.get("ag_cartella").toString()+rec1.get("ag_oper_ref").toString();//+rec1.get("ag_orario").toString();
        	String tocomp = b+rec2.get("ag_cartella").toString()+rec2.get("ag_oper_ref").toString();//+rec2.get("ag_orario").toString();
        	return comp.compareTo(tocomp) * (asc ? 1 : -1);
        }
//        switch (type) {
//        case 0: // Compare First Name
//        case 1: // Compare Title
//            return ((Integer)rec1.get("o1")).compareTo((Integer)rec2.get("o1")) * (asc ? 1 : -1);
//        case 2: // Compare First Name
//            return ((Integer)rec1.get("o2")).compareTo((Integer)rec2.get("o2")) * (asc ? 1 : -1);
//        case 3: // Compare Last Name
//            return ((Integer)rec1.get("o3")).compareTo((Integer)rec2.get("o3")) * (asc ? 1 : -1);
//        case 4: // Compare Extension
//            return ((Integer)rec1.get("o4")).compareTo((Integer)rec2.get("o4")) * (asc ? 1 : -1);
//        case 5: // Compare First Name
//            return ((Integer)rec1.get("o5")).compareTo((Integer)rec2.get("o5")) * (asc ? 1 : -1);
//        case 6: // Compare Last Name
//            return ((Integer)rec1.get("o6")).compareTo((Integer)rec2.get("o6")) * (asc ? 1 : -1);
//        default: // Full Name
//            return -1;
//        }
        
    }
 
}
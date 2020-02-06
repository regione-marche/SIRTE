package it.caribel.app.sinssnt.util;

import it.pisa.caribel.isas2.ISASRecord;


public class DecodificheVcoLesioni {

	public String getTipoLesione(ISASRecord dbr) {
		  try {
		          switch (((Integer)dbr.get("tipo_lesione")).intValue()) {
		            case 1: return "LdP";
		            case 2: return "venosa";
		            case 3: return "arteriosa";
		            case 4: return "mista";
		            case 5: return "altro";
		            default : return "";
		          }
		  } catch(Exception e) {
		          return "";
		  }
		}
		public String getNpuap(ISASRecord dbr) {	
			try {
				switch (((Integer)dbr.get("npuap")).intValue()) {
		            case 1: return "stadio I (arrossamento che non scompare alla digitopressione)";
		            case 2: return "stadio II (abrasione o flittene)";
		            case 3: return "stadio III (interessamento sottocutaneo)";
		            case 4: return "stadio IV (interessamento muscoli e ossa)";
		            default : return "";
		          }
		  } catch(Exception e) {
		          return "";
		  }
		}


		public String getProfAltreLesioni(ISASRecord dbr) {
			  try {
			          switch (((Integer)dbr.get("prof_lesioni")).intValue()) {
			            case 1: return "superficiale (epidermide, derma)";
			            case 2: return "profonda (sottocute, muscolo, ossa)";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}



		public String getBordi(ISASRecord dbr) {
			try {
			          switch (((Integer)dbr.get("bordi")).intValue()) {
			            case 1: return "piani";
			            case 2: return "introflessi";
			            case 3: return "frastagliati";
			            case 4: return "a stampo";
			            case 5: return "sotto minati";
			            case 6: return "callosi";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}



		public String getCutePerilesionale(ISASRecord dbr) {
			  try {
			          switch (((Integer)dbr.get("cute")).intValue()) {
			            case 1: return "intatta";
			            case 2: return "macerata";
			            case 3: return "edematosa";
			            case 4: return "secca";
			            case 5: return "disepitelizzata";
			            case 6: return "indurimento";
			            case 7: return "arrossamento";
			            case 8: return "ecchimosi/petecchie";
			            case 9: return "altro";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}



		public String getColore(ISASRecord dbr) {
			  try {
			          switch (((Integer)dbr.get("colore")).intValue()) {
			            case 1: return "nero (escara secca)";
			            case 2: return "verde (lesione infetta)";
			            case 3: return "giallo (fibrina/slough e necrosi umida)";
			            case 4: return "rosso (granuleggiante)";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}



		public String getEssudatoQta(ISASRecord dbr) {
			  try {
			          switch (((Integer)dbr.get("essudato_qta")).intValue()) {
			            case 1: return "assente / scarso";
			            case 2: return "medio";
			            case 3: return "abbondante";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}



		public String getEssudatoQual(ISASRecord dbr) {
			  try {
			          switch (((Integer)dbr.get("essudato_qual")).intValue()) {
			            case 1: return "sieroso";
			            case 2: return "siero / ematico";
			            case 3: return "purulento";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}


		public String getInfezione(ISASRecord dbr) {
			  try {
			          switch (((Integer)dbr.get("infezione")).intValue()) {
			            case 1: return "iperemia persistente";
			            case 2: return "cellulite";
			            case 3: return "comparsa (o aumento) dolore";
			            case 4: return "odore";
			            case 5: return "aumento quantita' essudato";
			            case 6: return "essudato purulento";
			            case 7: return "peggioramento lesione";
			            case 8: return "granulazione friabile";
			            case 9: return "febbre";
			            default : return "";
			          }
			  } catch(Exception e) {
			          return "";
			  }
			}
}

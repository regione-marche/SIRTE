package it.caribel.app.sinssnt.bean;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BarcodeDatamatrix;

import java.io.ByteArrayOutputStream; 
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import it.pisa.caribel.isas2.*;
import it.pisa.caribel.profile2.*;
import it.pisa.caribel.util.*;
import it.pisa.caribel.sinssnt.connection.*;

public class AsterDroidQRCodeEJB60 extends SINSSNTConnectionEJB {
	
private class QRCodeBean {
	
	private String	cartella = "",
			cognome = "",
			nome = "",
			indirizzo = "",
			comune = "",
			codiceFiscale = "",
			telefono1 = "",
			telefono2 = "",
			cognomeMedico = "",
			nomeMedico = "",
			comuneNascita = "",
			dataNascita = "",
			residenzaIndirizzo = "",
			residenzaComune = "",
			residenzaLocalita = "",
			residenzaArea = "",
			domicilioIndirizzo = "",
			domicilioComune = "",
			domicilioLocalita = "",
			domicilioArea = "",
			reperibilitaIndirizzo = "",
			reperibilitaCampanello = "",
			reperibilitaComune = "",
			reperibilitaArea = "";

	public String getCartella() {
		return cartella;
	}

	public void setCartella(String cartella) {
		this.cartella = cartella;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getIndirizzo() {
		return indirizzo;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	public String getComune() {
		return comune;
	}

	public void setComune(String comune) {
		this.comune = comune;
	}

	public String getCodiceFiscale() {
		return codiceFiscale;
	}

	public void setCodiceFiscale(String codiceFiscale) {
		this.codiceFiscale = codiceFiscale;
	}

	public String getTelefono1() {
		return telefono1;
	}

	public void setTelefono1(String telefono1) {
		this.telefono1 = telefono1;
	}

	public String getTelefono2() {
		return telefono2;
	}

	public void setTelefono2(String telefono2) {
		this.telefono2 = telefono2;
	}

	public String getCognomeMedico() {
		return cognomeMedico;
	}

	public void setCognomeMedico(String cognomeMedico) {
		this.cognomeMedico = cognomeMedico;
	}

	public String getNomeMedico() {
		return nomeMedico;
	}

	public void setNomeMedico(String nomeMedico) {
		this.nomeMedico = nomeMedico;
	}

	public String getComuneNascita() {
		return comuneNascita;
	}

	public void setComuneNascita(String comuneNascita) {
		this.comuneNascita = comuneNascita;
	}

	public String getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(String dataNascita) {
		this.dataNascita = dataNascita;
	}

	public String getResidenzaIndirizzo() {
		return residenzaIndirizzo;
	}

	public void setResidenzaIndirizzo(String residenzaIndirizzo) {
		this.residenzaIndirizzo = residenzaIndirizzo;
	}

	public String getResidenzaComune() {
		return residenzaComune;
	}

	public void setResidenzaComune(String residenzaComune) {
		this.residenzaComune = residenzaComune;
	}

	public String getResidenzaLocalita() {
		return residenzaLocalita;
	}

	public void setResidenzaLocalita(String residenzaLocalita) {
		this.residenzaLocalita = residenzaLocalita;
	}

	public String getResidenzaArea() {
		return residenzaArea;
	}

	public void setResidenzaArea(String residenzaArea) {
		this.residenzaArea = residenzaArea;
	}

	public String getDomicilioIndirizzo() {
		return domicilioIndirizzo;
	}

	public void setDomicilioIndirizzo(String domicilioIndirizzo) {
		this.domicilioIndirizzo = domicilioIndirizzo;
	}

	public String getDomicilioComune() {
		return domicilioComune;
	}

	public void setDomicilioComune(String domicilioComune) {
		this.domicilioComune = domicilioComune;
	}

	public String getDomicilioLocalita() {
		return domicilioLocalita;
	}

	public void setDomicilioLocalita(String domicilioLocalita) {
		this.domicilioLocalita = domicilioLocalita;
	}

	public String getDomicilioArea() {
		return domicilioArea;
	}

	public void setDomicilioArea(String domicilioArea) {
		this.domicilioArea = domicilioArea;
	}

	public String getReperibilitaIndirizzo() {
		return reperibilitaIndirizzo;
	}

	public void setReperibilitaIndirizzo(String reperibilitaIndirizzo) {
		this.reperibilitaIndirizzo = reperibilitaIndirizzo;
	}

	public String getReperibilitaCampanello() {
		return reperibilitaCampanello;
	}

	public void setReperibilitaCampanello(String reperibilitaCampanello) {
		this.reperibilitaCampanello = reperibilitaCampanello;
	}

	public String getReperibilitaComune() {
		return reperibilitaComune;
	}

	public void setReperibilitaComune(String reperibilitaComune) {
		this.reperibilitaComune = reperibilitaComune;
	}

	public String getReperibilitaArea() {
		return reperibilitaArea;
	}

	public void setReperibilitaArea(String reperibilitaArea) {
		this.reperibilitaArea = reperibilitaArea;
	}
}
	
	private static Log MYLOG = LogFactory.getLog(AsterDroidQRCodeEJB60.class);

	private Font fontCodiceNero  	= FontFactory.getFont(FontFactory.HELVETICA_BOLD,	55f);
	private Font fontCodiceRosso 	= FontFactory.getFont(FontFactory.HELVETICA_BOLD,	55f);
	private Font fontCodiceVerde 	= FontFactory.getFont(FontFactory.HELVETICA_BOLD, 	55f);
	private Font fontCodiceBlu 	= FontFactory.getFont(FontFactory.HELVETICA_BOLD,	55f);
	private Font fontCodiceGiallo 	= FontFactory.getFont(FontFactory.HELVETICA_BOLD,	55f);
	private Font fontLabel 		= FontFactory.getFont(FontFactory.HELVETICA,		10f);
	private Font fontSep 	 	= FontFactory.getFont(FontFactory.HELVETICA,		6f);

	public ByteArrayOutputStream generaEtichetta(QRCodeBean qrBean) throws Exception {
		
		fontCodiceRosso.setColor(BaseColor.RED);
		fontCodiceVerde.setColor(BaseColor.GREEN);
		fontCodiceBlu.setColor(BaseColor.BLUE);
		fontCodiceGiallo.setColor(BaseColor.ORANGE);
		
		Document document = new Document(PageSize.A4, 10, 10, 10, 10);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter writer = null;
		
		try{
		writer = PdfWriter.getInstance(document, baos);
		
		document.open();
		//document.add(new Phrase("ASL n.XX", fontLabel));

		PdfContentByte cb = writer.getDirectContent();

		PdfPTable table = new PdfPTable(3);
		table.setSpacingBefore(1000);
		// table.
		table.setWidthPercentage(90f);		
		table.setWidths(new int[] { 1, 1, 2 });
		table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
		table.getDefaultCell().setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		
		PdfPTable tableLabelPersona = new PdfPTable(1);
		tableLabelPersona.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
		PdfPCell cellLabelCartella 	= new PdfPCell(new Phrase("Cartella:", 		fontLabel));
		PdfPCell cellLabelCognome 	= new PdfPCell(new Phrase("Cognome e nome:", 	fontLabel));
		PdfPCell cellLabelIndirizzo	= new PdfPCell(new Phrase("Indirizzo:", 	fontLabel));
		PdfPCell cellLabelComune 	= new PdfPCell(new Phrase("Comune:", 		fontLabel));
		PdfPCell cellLabelCodFis 	= new PdfPCell(new Phrase("Codice Fiscale:", 	fontLabel));
		PdfPCell cellLabelSep 		= new PdfPCell(new Phrase(" ", 			fontSep));
		PdfPCell cellLabelelefoni 	= new PdfPCell(new Phrase("Telefoni:", 		fontLabel));
		PdfPCell cellLabelMedico 	= new PdfPCell(new Phrase("Medico:", 		fontLabel));
		PdfPCell cellLabelComuneNas	= new PdfPCell(new Phrase("Comune di Nascita:", fontLabel));
		PdfPCell cellLabelDataNas 	= new PdfPCell(new Phrase("Data di Nascita:", 	fontLabel));
		cellLabelCartella.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelCognome.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelIndirizzo.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelComune.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelCodFis.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelSep.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelelefoni.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelMedico.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelComuneNas.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelDataNas.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelCartella.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelCognome.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelIndirizzo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelComune.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelCodFis.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelSep.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelelefoni.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelMedico.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelComuneNas.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelDataNas.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableLabelPersona.addCell(cellLabelCartella);
		tableLabelPersona.addCell(cellLabelCognome);
		tableLabelPersona.addCell(cellLabelIndirizzo);
		tableLabelPersona.addCell(cellLabelComune);
		tableLabelPersona.addCell(cellLabelCodFis);
		tableLabelPersona.addCell(cellLabelSep);
		tableLabelPersona.addCell(cellLabelelefoni);
		tableLabelPersona.addCell(cellLabelMedico);  
		tableLabelPersona.addCell(cellLabelComuneNas);
		tableLabelPersona.addCell(cellLabelDataNas);
		
		PdfPTable tableDatilPersona = new PdfPTable(1);
		tableDatilPersona.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
		PdfPCell cellDataCartella 	= new PdfPCell(new Phrase(qrBean.getCartella(), 			fontLabel));
		PdfPCell cellDataCognome 	= new PdfPCell(new Phrase(qrBean.getCognome() + " " + qrBean.getNome(), 		fontLabel));
		PdfPCell cellDataIndirizzo	= new PdfPCell(new Phrase(qrBean.getIndirizzo(), 			fontLabel));
		PdfPCell cellDataComune 	= new PdfPCell(new Phrase(qrBean.getComune(), 				fontLabel));
		PdfPCell cellDataCodFis 	= new PdfPCell(new Phrase(qrBean.getCodiceFiscale(), 			fontLabel));
		PdfPCell cellDataSep 		= new PdfPCell(new Phrase(" ", 						fontSep));
		PdfPCell cellDataTelelefoni	= new PdfPCell(new Phrase(qrBean.getTelefono1() + " " + qrBean.getTelefono2(), 		fontLabel));
		PdfPCell cellDataMedico 	= new PdfPCell(new Phrase(qrBean.getCognomeMedico() + " " + qrBean.getNomeMedico(),	fontLabel));
		PdfPCell cellDataComuneNas	= new PdfPCell(new Phrase(qrBean.getComuneNascita(),			fontLabel));
		PdfPCell cellDataDataNas 	= new PdfPCell(new Phrase(qrBean.getDataNascita(), 			fontLabel));
		cellDataCartella.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataCognome.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataIndirizzo.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataComune.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataCodFis.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataSep.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataTelelefoni.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataMedico.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataComuneNas.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataDataNas.setHorizontalAlignment(Element.ALIGN_LEFT);
		cellDataCartella.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataCognome.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataIndirizzo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataComune.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataCodFis.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataSep.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataTelelefoni.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataMedico.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataComuneNas.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDataDataNas.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableDatilPersona.addCell(cellDataCartella);
		tableDatilPersona.addCell(cellDataCognome);
		tableDatilPersona.addCell(cellDataIndirizzo);
		tableDatilPersona.addCell(cellDataComune);
		tableDatilPersona.addCell(cellDataCodFis);
		tableDatilPersona.addCell(cellDataSep);
		tableDatilPersona.addCell(cellDataTelelefoni);
		tableDatilPersona.addCell(cellDataMedico);  
		tableDatilPersona.addCell(cellDataComuneNas);
		tableDatilPersona.addCell(cellDataDataNas);
		
		//BarcodeQRCode qrcode = new BarcodeQRCode(qrBean.getNome() + " " + qrBean.getCognome() + " " + qrBean.getCodiceFiscale(), 1, 1, null);
		String strAss =	qrBean.getCartella()           + "\n" + 
				qrBean.getCognome()            + "\n" + 
				qrBean.getNome()               + "\n" + 
				qrBean.getDomicilioIndirizzo() + "\n" + 
				qrBean.getDomicilioComune()    + "\n" + 
				qrBean.getCodiceFiscale();
		BarcodeQRCode qrcode = new BarcodeQRCode(strAss, 1, 1, null);
		 
		Image  img = qrcode.getImage();
	      
		img.scaleToFit(160, 160);
		
		PdfPCell cellLeft = new PdfPCell(img);
		
		cellLeft.setPadding(10f);
		
		PdfPCell cellCenter = new PdfPCell(tableLabelPersona);
		PdfPCell cellRight = new PdfPCell(tableDatilPersona);
		
		cellLeft.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLeft.enableBorderSide(com.itextpdf.text.Rectangle.LEFT); 
		cellLeft.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellCenter.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellCenter.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellRight.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRight.enableBorderSide(com.itextpdf.text.Rectangle.RIGHT); 
		cellRight.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		
		table.addCell(cellLeft);
		table.addCell(cellCenter);
		table.addCell(cellRight);
		
		PdfPTable tableIndirizzo = new PdfPTable(1);
		tableIndirizzo.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
		PdfPCell cellLabelIndirizz 	= new PdfPCell(new Phrase("Indirizzo:", 	fontLabel));
		PdfPCell cellLabelComun		= new PdfPCell(new Phrase("Comune:", 		fontLabel));
		PdfPCell cellLabelLocalita 	= new PdfPCell(new Phrase("Localita':",		fontLabel));
		PdfPCell cellLabelArea 		= new PdfPCell(new Phrase("Area distrettuale:", fontLabel));
		cellLabelIndirizz.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelComun.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelLocalita.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelArea.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelIndirizz.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelComun.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelLocalita.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelArea.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableIndirizzo.addCell(cellLabelIndirizz);
		tableIndirizzo.addCell(cellLabelComun);
		tableIndirizzo.addCell(cellLabelLocalita);
		tableIndirizzo.addCell(cellLabelArea);
		
		PdfPTable tableResidenza = new PdfPTable(1);
		PdfPCell cellResidenzaIndirizzo1	= new PdfPCell(new Phrase(qrBean.getResidenzaIndirizzo(),	fontLabel));
		PdfPCell cellResidenzaComune1 		= new PdfPCell(new Phrase(qrBean.getResidenzaComune(),		fontLabel));
		PdfPCell cellResidenzaLocalita1		= new PdfPCell(new Phrase(qrBean.getResidenzaLocalita(),	fontLabel));
		PdfPCell cellResidenzaArea1 		= new PdfPCell(new Phrase(qrBean.getResidenzaArea(),		fontLabel));
		cellResidenzaIndirizzo1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellResidenzaComune1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellResidenzaLocalita1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellResidenzaArea1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableResidenza.addCell(cellResidenzaIndirizzo1);
		tableResidenza.addCell(cellResidenzaComune1);
		tableResidenza.addCell(cellResidenzaLocalita1);
		tableResidenza.addCell(cellResidenzaArea1);
		PdfPCell cellLeft1 	= new PdfPCell(new Phrase("Residenza", fontLabel));
		PdfPCell cellCenter1 	= new PdfPCell(tableIndirizzo);
		PdfPCell cellRight1 	= new PdfPCell(tableResidenza);
		
		cellLeft1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLeft1.enableBorderSide(com.itextpdf.text.Rectangle.LEFT); 
		cellLeft1.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellCenter1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellCenter1.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellRight1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRight1.enableBorderSide(com.itextpdf.text.Rectangle.RIGHT); 
		cellRight1.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		
		table.addCell(cellLeft1);
		table.addCell(cellCenter1);
		table.addCell(cellRight1);
		
		PdfPTable tableDomicilio = new PdfPTable(1);
		PdfPCell cellDomicilioIndirizzo1	= new PdfPCell(new Phrase(qrBean.getDomicilioIndirizzo(),	fontLabel));
		PdfPCell cellDomicilioComune1 		= new PdfPCell(new Phrase(qrBean.getDomicilioComune(),		fontLabel));
		PdfPCell cellDomicilioLocalita1		= new PdfPCell(new Phrase(qrBean.getDomicilioLocalita(),	fontLabel));
		PdfPCell cellDomicilioArea1 		= new PdfPCell(new Phrase(qrBean.getDomicilioArea(),		fontLabel));
		cellDomicilioIndirizzo1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDomicilioComune1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDomicilioLocalita1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellDomicilioArea1.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableDomicilio.addCell(cellDomicilioIndirizzo1);
		tableDomicilio.addCell(cellDomicilioComune1);
		tableDomicilio.addCell(cellDomicilioLocalita1);
		tableDomicilio.addCell(cellDomicilioArea1);
		PdfPCell cellLeft2 = new PdfPCell(new Phrase("Domicilio", fontLabel));
		PdfPCell cellCenter2 = new PdfPCell(tableIndirizzo);
		PdfPCell cellRight2 = new PdfPCell(tableDomicilio);
		
		cellLeft2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLeft2.enableBorderSide(com.itextpdf.text.Rectangle.LEFT); 
		cellLeft2.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellCenter2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellCenter2.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellRight2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRight2.enableBorderSide(com.itextpdf.text.Rectangle.RIGHT); 
		cellRight2.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		
		table.addCell(cellLeft2);
		table.addCell(cellCenter2);
		table.addCell(cellRight2);
		
		PdfPTable tableReperibilita = new PdfPTable(1);
		tableReperibilita.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
		PdfPCell cellLabelIndirizz2 	= new PdfPCell(new Phrase("Indirizzo:", 	fontLabel));
		PdfPCell cellLabelCampanello	= new PdfPCell(new Phrase("Nome campanello:", 	fontLabel));
		PdfPCell cellLabelComune2	= new PdfPCell(new Phrase("Comune:", 		fontLabel));
		PdfPCell cellLabelArea2 	= new PdfPCell(new Phrase("Area distrettuale:", fontLabel));
		cellLabelIndirizz2.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelCampanello.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelComune2.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelArea2.setHorizontalAlignment(Element.ALIGN_RIGHT);
		cellLabelIndirizz2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelCampanello.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelComune2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLabelArea2.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableReperibilita.addCell(cellLabelIndirizz2);
		tableReperibilita.addCell(cellLabelCampanello);
		tableReperibilita.addCell(cellLabelComune2);
		tableReperibilita.addCell(cellLabelArea2);
		
		PdfPTable tableRep = new PdfPTable(1);
		PdfPCell cellRepIndirizzo	= new PdfPCell(new Phrase(qrBean.getReperibilitaIndirizzo(),	fontLabel));
		PdfPCell cellRepNome		= new PdfPCell(new Phrase(qrBean.getReperibilitaCampanello(),	fontLabel));
		PdfPCell cellRepComune 		= new PdfPCell(new Phrase(qrBean.getReperibilitaComune(),	fontLabel));
		PdfPCell cellRepArea 		= new PdfPCell(new Phrase(qrBean.getReperibilitaArea(),		fontLabel));
		cellRepIndirizzo.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRepNome.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRepComune.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRepArea.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		tableRep.addCell(cellRepIndirizzo);
		tableRep.addCell(cellRepNome);
		tableRep.addCell(cellRepComune);
		tableRep.addCell(cellRepArea);
		PdfPCell cellLeft3 	= new PdfPCell(new Phrase("Reperibilita'", fontLabel));
		PdfPCell cellCenter3 	= new PdfPCell(tableReperibilita);
		PdfPCell cellRight3	= new PdfPCell(tableRep);
		
		cellLeft3.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellLeft3.enableBorderSide(com.itextpdf.text.Rectangle.LEFT); 
		cellLeft3.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellLeft3.enableBorderSide(com.itextpdf.text.Rectangle.BOTTOM); 
		cellCenter3.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellCenter3.enableBorderSide(com.itextpdf.text.Rectangle.TOP); 
		cellCenter3.enableBorderSide(com.itextpdf.text.Rectangle.BOTTOM); 
		cellRight3.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
		cellRight3.enableBorderSide(com.itextpdf.text.Rectangle.RIGHT); 
		cellRight3.enableBorderSide(com.itextpdf.text.Rectangle.TOP);
		cellRight3.enableBorderSide(com.itextpdf.text.Rectangle.BOTTOM);
		
		table.addCell(cellLeft3);
		table.addCell(cellCenter3);
		table.addCell(cellRight3);
		
		document.add(table);
		document.close();
		} catch (Exception e) {
			e.printStackTrace();
			baos.reset();
			throw e;
		} finally {
			if (document != null){
				document.close();
			}
			if (writer != null){
				writer.close();
			}
		}
		return baos;
	}

public byte[] print(String utente, String passwd, Hashtable par) throws Exception {
	ISASConnection dbc = null;
	String cognomeMedico = "";
	String nomeMedico = "";
	try {
		myLogin lg = new myLogin();
		lg.put(utente, passwd);
		dbc = super.logIn(lg);

		String s = "SELECT c.*, a.* FROM cartella c, anagra_c a "+
			"WHERE c.n_cartella = "+par.get("n_cartella")+" AND "+
			"c.n_cartella = a.n_cartella AND a.data_variazione = "+
			"(SELECT MAX(x.data_variazione) FROM anagra_c x WHERE "+
			"x.n_cartella = a.n_cartella)";
		System.out.println("AsterDroidQRCodeEJB.print(): "+s);
		ISASRecord dbr = dbc.readRecord(s);
		if (dbr != null) {
			QRCodeBean qrBean = new QRCodeBean();
			qrBean.setCartella(getString(dbr, "n_cartella"));
			qrBean.setCognome(getString(dbr, "cognome"));
			qrBean.setNome(getString(dbr, "nome"));
			qrBean.setIndirizzo(getString(dbr, "dom_indiriz"));
			qrBean.setComune(getComune(dbc, getString(dbr, "dom_citta")));
			qrBean.setCodiceFiscale(getString(dbr, "cod_fisc"));
			qrBean.setComuneNascita(getComune(dbc, getString(dbr, "cod_com_nasc")));
			qrBean.setTelefono1(getString(dbr, "telefono1"));
			qrBean.setTelefono2(getString(dbr, "telefono2"));
			qrBean.setDataNascita(getString(dbr, "data_nasc"));
			qrBean.setResidenzaIndirizzo(getString(dbr, "indirizzo"));
			qrBean.setResidenzaLocalita(getString(dbr, "localita"));
			qrBean.setResidenzaComune(getComune(dbc, getString(dbr, "citta")));
			qrBean.setResidenzaArea(getArea(dbc, getString(dbr, "areadis")));
			qrBean.setDomicilioIndirizzo(getString(dbr, "dom_indiriz"));
			qrBean.setDomicilioLocalita(getString(dbr, "dom_localita"));
			qrBean.setDomicilioComune(getComune(dbc, getString(dbr, "dom_citta")));
			qrBean.setDomicilioArea(getArea(dbc, getString(dbr, "dom_areadis")));
			qrBean.setReperibilitaIndirizzo(getString(dbr, "indirizzo_rep"));
			qrBean.setReperibilitaCampanello(getString(dbr, "nome_camp"));
			qrBean.setReperibilitaComune(getComune(dbc, getString(dbr, "comune_rep")));
			qrBean.setReperibilitaArea(getArea(dbc, getString(dbr, "areadis_rep")));

			try {
				String m = "SELECT * FROM medici WHERE mecodi = '"+getString(dbr, "cod_med")+"'";
				//System.out.println("AsterDroidQRCodeEJB.print(): "+m);
				ISASRecord med = dbc.readRecord(m);
				cognomeMedico = getString(med, "mecogn");
				nomeMedico = getString(med, "menome");
			} catch(Exception e) {
				System.out.println("AsterDroidQRCodeEJB.getMedico("+getString(dbr, "cod_med")+"): "+e);
				cognomeMedico = "";
				nomeMedico = "";
			}
			qrBean.setCognomeMedico(cognomeMedico);
			qrBean.setNomeMedico(nomeMedico);

			ByteArrayOutputStream doc = generaEtichetta(qrBean);
			return doc.toByteArray();
		} else {
			return null;
		}
	} catch(Exception e) {
		System.out.println("AsterDroidQRCodeEJB.print("+utente+"): "+e);
		e.printStackTrace();
	}finally{
		try{
			if (dbc != null) dbc.close();
			super.close(dbc);
		} catch(Exception e1) {
			System.out.println("AsterDroidQRCodeEJB.print("+utente+"): ERRORE "+e1);
			e1.printStackTrace();
		}
	}
	return null;
}

private String getString(ISASRecord r, String k) {
	try {
		String s = r.get(k).toString();
		if (s == null) s = "";
		return s;
	} catch(Exception e) {
		System.out.println("AsterDroidQRCodeEJB.getString("+k+"): "+e);
		return "";
	}
}

private String getComune(ISASConnection dbc, String k) {
	String r = "";
	String s = "SELECT * FROM comuni WHERE codice = '"+k+"'";
	try {
		ISASRecord dbr = dbc.readRecord(s);
		r = getString(dbr, "descrizione");
	} catch(Exception e) { 
		System.out.println("AsterDroidQRCodeEJB.getComune("+s+"): "+e);
	}
	return r;
}

private String getArea(ISASConnection dbc, String k) {
	String r = "";
	String s = "SELECT * FROM areadis WHERE codice = '"+k+"'";
	try {
		ISASRecord dbr = dbc.readRecord(s);
		r = getString(dbr, "descrizione");
	} catch(Exception e) { 
		System.out.println("AsterDroidQRCodeEJB.getArea("+s+"): "+e);
	}
	return r;
}

/*
	public static void main(String[] args) {

		try {

			QRCodeBean qrBean = new QRCodeBean();
			qrBean.setCartella("123456");
			qrBean.setCognome("BONSIGNORI");
			qrBean.setNome("ROBERTO");
			qrBean.setIndirizzo("VIALE MAZZINI 13");
			

			StampaQRCodeItext2 sm = new StampaQRCodeItext2();
			ByteArrayOutputStream doc = sm.generaPdfEtichetteUnitaArchiviazione(qrBean);
			byte[] docByte = doc.toByteArray();
			
			FileUtils.writeByteArrayToFile(new File("C:/QR_CODE2.pdf"), docByte);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}
*/

}

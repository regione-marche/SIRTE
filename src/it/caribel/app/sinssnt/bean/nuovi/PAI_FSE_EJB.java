package it.caribel.app.sinssnt.bean.nuovi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Hashtable;
import javax.servlet.ServletContext;
import javax.xml.bind.DatatypeConverter;

import oracle.jdbc.driver.OracleResultSet;
import oracle.sql.BLOB;
import oracle.sql.CLOB;

import org.xml.sax.InputSource;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Filedownload;

import it.caribel.app.common.controllers._RegioneMarche.FSE.RemoteSignerSystem.RemoteSignerManager;
import it.caribel.app.sinssnt.bean.FassiMarcheEJB;
import it.pisa.caribel.isas2.ISASConnection;
import it.pisa.caribel.merge.mergeDocument;
import it.pisa.caribel.profile2.myLogin;
import it.pisa.caribel.sinssnt.connection.SINSSNTConnectionEJB;

public class PAI_FSE_EJB extends SINSSNTConnectionEJB  
{
	public PAI_FSE_EJB() {}

	public String getDocumentPAI(myLogin mylogin, Hashtable par) throws Exception {
		String methodName = "getDocumentPAI";
		try {
			//Instanzio il mergedocument
			ServletContext servletContext = Executions.getCurrent().getDesktop().getWebApp().getServletContext();
			InputStream is = servletContext.getResourceAsStream("/WEB-INF/REPORT/elencoPaiAssistito.fo");
			mergeDocument mdoc = new mergeDocument(is, 1);
			//Passo il mergedocument all'EJB di stampa
			FoStampaPaiEJB myFoEjb = new FoStampaPaiEJB();
			byte[] mergedDoc = myFoEjb.stampaPai(mylogin.getUser(), mylogin.getPassword(), par, mdoc);
			//Passo il merged document alla renderizzazione
			byte[] docPdf = invocaFop020(mergedDoc);
			
			if(false){
				Filedownload.save(docPdf, "application/pdf", "PAI.pdf");
			}
			
			//Codifico il documento pdf in Base64
			String docPdfEncodedBase64 = DatatypeConverter.printBase64Binary(docPdf);
//			System.out.println(docPdfEncodedBase64);
//			byte[] docPdfDecoded= DatatypeConverter.parseBase64Binary(docPdfEncodedBase64);
//			System.out.println(new String(docPdfDecoded));
			
			return docPdfEncodedBase64;
		} catch(Exception e){
			throw newEjbException("Errore eseguendo "+ methodName+ ": " + e.getMessage(), e);
		}
	}
	
	public String getAsrEmpi(myLogin mylogin, String n_cartella) throws Exception {
		FassiMarcheEJB fassiEjb = new FassiMarcheEJB();
		String idAsrEmpi = fassiEjb.getAndSetAsrEmpi(mylogin, n_cartella);
		return idAsrEmpi;
	}
	
	public String getDocumentPAISigned(myLogin mylogin, String idAsrEmpi, String docPdfEncodedBase64, String signer, String pin_code, String otp_code) throws Exception {
		RemoteSignerManager rsm = new RemoteSignerManager();
		String docPdfEncodedBase64Signed = rsm.getDocumentSigned(idAsrEmpi, docPdfEncodedBase64,signer, pin_code, otp_code);
		return docPdfEncodedBase64Signed;
	}
	
	public void caricaSuTabellaDiFrontiera(myLogin mylogin, String idAsrEmpi,String firmatario, String docPdfEncodedBase64Signed) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rset = null;
		Statement stmt = null;
		
		boolean done = false;
		ISASConnection dbc = null;
		try {
			dbc = super.logIn(mylogin);
			dbc.conn.startTransaction();
			Connection jdbc = dbc.conn.getConnection();
			
			//dati supplementari da inserire in tabella di frontiera
			int id = selectProgressivo(dbc, "PAI_SU_FSE");
			Calendar calendar = Calendar.getInstance();
			java.util.Date dtIns = calendar.getTime();
			String opcreazione = mylogin.getUser();
			String mime_type = "application/pdf";
			String author_person = firmatario;
			String patient_cf = "";
			int versione = 1;

			// Inserisco un nuovo record
			String sqlText = 
					"INSERT INTO PAI_DOCUMENTI(docu0,document_unique_id,firmatario,documento," +
					"versione,dtcreazione,opcreazione,mime_type," +
					"author_person,patient_cf,patient_id) "
					+ "VALUES(?,?,?,EMPTY_BLOB(),?,?,?,?,?,?,?)";

			pstmt = jdbc.prepareStatement(sqlText);
			pstmt.setInt(1, id);
			pstmt.setString(2, id+"");
			pstmt.setString(3, firmatario);
			
			pstmt.setInt(5, versione);
			pstmt.setDate(6, new Date(dtIns.getTime()));
			pstmt.setString(7, opcreazione);
			pstmt.setString(8, mime_type);
			
			pstmt.setString(9, author_person);
			pstmt.setString(10, patient_cf);
			pstmt.setString(11, idAsrEmpi);
			
			
			pstmt.executeUpdate();

			String sql4update = " SELECT *"
					+ " FROM  PAI_DOCUMENTI"
					+ " WHERE  docu0 = " + id;
			LOG.debug("sql4update = " + sql4update);

			stmt = jdbc.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

			rset = stmt.executeQuery(sql4update);
			rset.next();
			BLOB blob = ((OracleResultSet) rset).getBLOB("documento");
			blob.putBytes(1, docPdfEncodedBase64Signed.getBytes());
			dbc.conn.commitTransaction();
			done = true;
		} finally {
			if (!done)
				dbc.conn.rollbackTransaction();
			close_rset_nothrow(this.getClass().getName(), rset);
			close_stmt_nothrow(this.getClass().getName(), stmt);
			logout_nothrow(this.getClass().getName(), dbc);
		}
	}
	
	private byte[] invocaFop020(byte[] mergedDocument) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Class driverClass = Class.forName("org.apache.fop.apps.Driver");
		Constructor costr = driverClass.getConstructor(new Class[] { InputSource.class, OutputStream.class });
		Object istanza = costr.newInstance(new Object[] {
				new InputSource(new ByteArrayInputStream(mergedDocument)),
				out
		});
		driverClass.getMethod("run", (Class[])null).invoke(istanza, (Object[])null);
		return out.toByteArray();
	}
	
}
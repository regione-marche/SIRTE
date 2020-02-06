<%@page import="java.io.*" %>
<%@page import="java.util.*" %>
<%!
	private static String start = null;
	private static int skip = 0;
	private static Vector tuttiPdf = null;
	private static Hashtable pdfProperties = null;
	
	private String normalizza(String orig){
		if (!File.separator.equals("/")){
			orig = orig.replace('\\', '/');
		}
		return orig;
	}
	
	public void jspInit(){
		start = normalizza( this.getServletContext().getRealPath("/") );
		if (start.endsWith("/"))
			start = start.substring(0, start.length()-1);
		
		skip = start.lastIndexOf( "/" );
		if (skip<0) skip=0;
		pdfProperties = new Hashtable();
		tuttiPdf = new Vector();
		getPdf( start );
	}

	private File[] getFiles(String file){
		File f = new File(file);
	    File [] ris = f.listFiles();
		return ris;
	}

	private void getPdf(String from){
		File[] f = getFiles(from);
		for(int i=0; i<f.length; i++){
			if (f[i].isDirectory() && !f[i].getName().equalsIgnoreCase("WEB-INF")){
				getPdf( normalizza(f[i].getAbsolutePath()) );
			}
			else if (f[i].getName().toLowerCase().endsWith("pdf")){
				tuttiPdf.add(f[i]);
				tuttiPdf.add( normalizza(f[i].getAbsolutePath()).substring(skip));
			}else if (f[i].getName().toLowerCase().endsWith("pdf.properties")){
				Properties p = new Properties();
				try{
					FileInputStream fio = new FileInputStream( f[i] );
					p.load( fio );
					fio.close();
					pdfProperties.put(f[i].getName(), p);
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}
	}
	
%>
<%
if (request.getParameter("print")!=null){
	out.print("tuttiPdf: "+tuttiPdf+"<br/>");	
	out.print("pdfProperties: "+pdfProperties+"<br/>");	

}else
try{
	ObjectOutputStream o = new ObjectOutputStream( response.getOutputStream() );
	o.writeObject( tuttiPdf );
	o.writeObject( pdfProperties );
	o.flush();
	//out.clear();
	//out.close();
}catch(Exception e){
	System.out.println(new java.util.Date() + "- Error invoking '"+request.getRequestURI()+"'");
	//e.printStackTrace();
}

%>

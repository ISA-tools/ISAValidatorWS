package org.isatools.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response;

import org.apache.log4j.Level;
import org.isatools.isatab.gui_invokers.GUIISATABValidator;
import org.isatools.isatab.gui_invokers.GUIInvokerResult;
import org.isatools.isatab.isaconfigurator.ISAConfigurationSet;
import org.isatools.tablib.utils.BIIObjectStore;
import org.isatools.tablib.utils.logging.TabLoggingEventWrapper;
import org.omg.CORBA_2_3.portable.OutputStream;

import org.isatools.errorreporter.model.ErrorUtils;

import uk.ac.ebi.bioinvindex.model.Investigation;
import uk.ac.ebi.bioinvindex.model.Study;
import uk.ac.ebi.bioinvindex.model.processing.Assay;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class ISATABNanoValidate {

	static {
		ISAConfigurationSet.setConfigPath("C:/bin/apache-tomcat-6.0.37/bin/config/default-config");
	}
    private static Logger log = Logger.getLogger(ISATABNanoValidate.class.getName());

    @Context
    private UriInfo context;

    // Creates a new instance of MICheckout

    public ISATABNanoValidate() {
    }

   

    /**
     * Retrieves representation of an instance of MICheckout
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Path("test")
    @Produces("text/plain")
    public String getTestChecklist(@QueryParam("name") String name) {

        return "This is a test output " + name + "! :D The service is working...";
    }
    
    /**
     * Retrieves the ISATAB Nano Zip file, stores it locally, perform the validation and return the response.
     *
     * @return an instance of java.lang.String
     */
    @POST
    @Path("validate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("text/plain")
    public String getZipFile(@FormDataParam("ZipFile") InputStream uploadedFile,
    		@FormDataParam("ZipFile") FormDataContentDisposition fileDetail) {
    	
        
		try {
			File isatabFile = saveFileToSystem(uploadedFile);
	        ValidationReport result;
	        
			result = validateISATabFile(isatabFile);
			
			if(result.result == GUIInvokerResult.SUCCESS) {
				return "Validation successful for " + fileDetail.getFileName() + "\n" + result.report;
			} else {
				return "Validation Unsuccessful for " + fileDetail.getFileName() + "\n" + result.report;	
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			 return "Error occurred when unzipping files." + e.getMessage();
		}
       
     }
    
    private File saveFileToSystem(InputStream isatabFile) throws IOException {
    	BufferedOutputStream bos = null;
    	
    	File file = new File(System.getProperty("java.io.tmpdir") + "/ISATabNano-" + System.currentTimeMillis() + ".zip");
        System.out.println("Saving to " + file.getAbsolutePath());
    	
        FileOutputStream out = new FileOutputStream(file);
        int read =0;
        byte[] bytes = new byte[1024];
        
        while((read = isatabFile.read(bytes)) != -1) {
        	out.write(bytes, 0, read);
        }
        out.flush();
        out.close();
        
        return file;
        
    }
    
    private ValidationReport validateISATabFile(File isatab) throws IOException {
    	GUIISATABValidator validator = new GUIISATABValidator();
    	
    	String path = isatab.getAbsolutePath();
    	if(!isatab.isDirectory()) {
    		System.out.println("Unzipping!");
    		path = FileUnzipper.unzip(isatab);
    	}
    	
    	GUIInvokerResult result = validator.validate(path);
    	ValidationReport report = new ValidationReport(result, "");
    	
    	if(result == GUIInvokerResult.SUCCESS) {
    		report.report = validator.report();
    		
    	} else {
    		populateErrorReport(report, validator.getLog());
    	}
    	return report;
    }
    
    private void populateErrorReport(ValidationReport report, List<TabLoggingEventWrapper> logEvents) {
    	StringBuilder builder = new StringBuilder();
    	
    	for(TabLoggingEventWrapper event : logEvents) {
    		String fileName = ErrorUtils.extractFileInformation(event.getLogEvent());
    		if(fileName != null) {
    			if(event.getLogEvent().getLevel().toInt() >= Level.WARN_INT) {
    				builder.append("File name " + fileName).append("\n");
    	    		builder.append("\t" + event.getLogEvent().getMessage());
    			}
    		}
    	
    	}
    	
    	report.report = builder.toString();
    }
    
    class ValidationReport {
    	GUIInvokerResult result;
    	String report;
    	ValidationReport(GUIInvokerResult result, String report) {
    		this.result= result;
    		this.report = report;
    	}	
    }
    
 

}
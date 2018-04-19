package com.test.pdf;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.xmlgraphics.util.MimeConstants;
 
/**
 * Utility class for generating PDF files from Apache FOP templates.
 *
 * @author Tomasz Zabkowicz / IT Crowd LLC
 */
public class PDFGenerator implements Serializable {
 
    /**
     * Generates PDF file with given name basing on provided Apache FOP template with Velocity markers which will be populated with given parameter values.
     *
     * @param fileName         a name of the PDF file that should be created. The name should have format '*.pdf'.
     * @param templateFilePath a path to Apache FOP template basing on which PDF will be generated. Example path: '/layout/pdf/purchaseOrderPDF.fo'.
     * @param params           a map containing values for Velocity parameters placed in the template. Parameter values should be accessed the following
     *                         way in the FOP template: $param.[param_key].
     *
     * @throws IOException
     * @throws FOPException
     * @throws TransformerException
     */
    public void generate(String fileName, InputStream finTemplate, Map<String, Object> params) throws IOException, FOPException, TransformerException, URISyntaxException
    {
        // Obtain server context and template file
        final String template = new Scanner(finTemplate).useDelimiter("\\Z").next();
        final StringWriter content = new StringWriter();
 
        // Use Velocity to replace placeholders in template with desired values
        Velocity.init();
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("param", params);
        Velocity.evaluate(velocityContext, content, "", template);
        final String sourceXml = content.toString();

        // Run FOP transformer which will generate PDF and write it to the stream
        final OutputStream out = new FileOutputStream(fileName);
        final Fop fop = FopFactory.newInstance(getClass().getResource("PDFGenerator.conf").toURI()).newFop(MimeConstants.MIME_PDF, out);
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setParameter("versionParam", "2.0");
        final Source src = new StreamSource(new StringReader(sourceXml));
        final Result res = new SAXResult(fop.getDefaultHandler());
        transformer.transform(src, res);
 
        // Close stream and http response - PDF file will be downloaded to user's machine
        out.close();
    }
    
    public static void main(String[] args) throws Exception {
    	PDFGenerator pdfGenerator = new PDFGenerator();
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("header", "Simple document with 2 blocks of text");
        params.put("block1", "Lorem ipsum dolor sit amet.");
        params.put("block2", "Sed dapibus tempus ultrices. Mauris faucibus bibendum lacus, quis mattis eros.");
        pdfGenerator.generate("target/Simple.pdf", PDFGenerator.class.getResourceAsStream("PDFGenerator.fo.xml"), params);
	}
}
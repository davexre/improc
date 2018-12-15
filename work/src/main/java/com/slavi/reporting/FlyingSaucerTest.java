package com.slavi.reporting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.xhtmlrenderer.pdf.ITextRenderer;

public class FlyingSaucerTest {
    public static void main(String[] args) throws Exception {
        OutputStream os = null;
        try {
            // create some simple, fake documents; nothing special about these, anything that Flying Saucer
            // can otherwise render
            final String[] inputs = new String[]{
                    newPageHtml(1, "red"),
                    newPageHtml(2, "blue"),
                    newPageHtml(3, "green")
            };

//            final File outputFile = File.createTempFile("FlyingSacuer.PDFRenderToMultiplePages", ".pdf");
            final File outputFile = new File("target/FlyingSacuer.PDFRenderToMultiplePages.pdf");
            os = new FileOutputStream(outputFile);

            ITextRenderer renderer = new ITextRenderer();

            // we need to create the target PDF
            // we'll create one page per input string, but we call layout for the first
            renderer.setDocumentFromString(inputs[0]);
            renderer.layout();
            renderer.createPDF(os, false);

            // each page after the first we add using layout() followed by writeNextDocument()
            for (int i = 1; i < inputs.length; i++) {
                renderer.setDocumentFromString(inputs[i]);
                renderer.layout();
                renderer.writeNextDocument();
            }

            // complete the PDF
            renderer.finishPDF();

            System.out.println("Sample file with " + inputs.length + " documents rendered as PDF to " + outputFile);
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) { /*ignore*/ }
            }
        }
    }

    private static String newPageHtml(int pageNo, String color) {
        return "<html style='color: " + color + "' >" +
                "    Page" + pageNo +
                "</html>";
}}

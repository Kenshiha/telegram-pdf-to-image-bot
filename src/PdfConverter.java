import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfConverter {

    public static List<File> convertPdfToImages(File pdfFile){
        List<File> imageFile = new ArrayList<>();

        String projectRoot = System.getProperty("user.dir");
        File outputDir = new File(projectRoot,"extracted_pages");
        if(!outputDir.exists()){
            boolean created = outputDir.mkdirs();
            System.out.println("[PdfConverter] Created Directory " + outputDir.getAbsolutePath() + "?" + created);
        }

        try(PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            System.out.println("[PDFConverter] Converting " + totalPages + " Pages to Images...");

            for (int i = 0; i < totalPages; i++){
                //Render Pages at 150 DPI for clean , readable balance
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150);

                //Saving file as images
                // File outputImage =  new File("page_" + (i + 1) + ".png");
                File outputImage = new File(outputDir,"page_" + (i + 1) + ".png").getAbsoluteFile();

                boolean written = ImageIO.write(bim, "PNG", outputImage);
                if(written && outputImage.exists()){
                    imageFile.add(outputImage.getAbsoluteFile());
                    System.out.println("[PdfConverter] Successfully saved "+ outputImage.getAbsolutePath());
                }else {
                    System.err.println("[Error] Failed to write to disk for page " + (i + 1));
                }

                System.out.println("[PdfConverter] Successfully rendered: " + outputImage.getName());
            }
        } catch (IOException e) {
            System.err.println("[Error] Failed to render PDF pages: " + e.getMessage());
        }

       return imageFile;
    }
}

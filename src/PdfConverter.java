import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class PdfConverter {

    public static List<File> convertPdfToImages(File pdfFile, File jobOutputDir){
        List<File> imageFile = new ArrayList<>();

        if (!jobOutputDir.exists()){
            jobOutputDir.mkdirs();
        }

//        String projectRoot = System.getProperty("user.dir");
//        File outputDir = new File(projectRoot,"extracted_pages");
//        if(!outputDir.exists()){
//            boolean created = outputDir.mkdirs();
//           // System.out.println("[PdfConverter] Created Directory " + outputDir.getAbsolutePath() + "?" + created);
//        }

        try(PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int totalPages = document.getNumberOfPages();

            System.out.println("[PDFConverter] Converting " + totalPages + " Pages to Images...");

            for (int i = 0; i < totalPages; i++){
                //Render Pages at 150 DPI for clean , readable balance
                BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150);

                //Saving file as images
                // File outputImage =  new File("page_" + (i + 1) + ".png");
                File outputImage = new File(jobOutputDir,"page_" + (i + 1) + ".png").getAbsoluteFile();

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

    public static void cleanupJobFiles(File downloadedPdf, List<File> renderedImage, File jobOutputDir){
        if (renderedImage != null){
            for (File img : renderedImage){
                deleteQuitely(img, "rendered image");
            }
        }

        if (jobOutputDir != null && jobOutputDir.exists()){
            File[] leftoverFiles = jobOutputDir.listFiles();
            if(leftoverFiles != null){
                for(File file : leftoverFiles){
                    deleteQuitely(file, "leftover output file");
                }
            }
            deleteQuitely(jobOutputDir, "job output directory");
        }
        deleteQuitely(downloadedPdf, "downloaded pdf");
    }

    private static void deleteQuitely(File target, String label){
        if(target == null || !target.exists()){
            return;
        }
        int maxAttempt = 3;
        for(int attempt = 1; attempt <= maxAttempt; attempt++) {
            try {
                if (Files.deleteIfExists(target.toPath())){
                    return;
                }
                return;

            } catch (IOException e) {
                if (attempt == maxAttempt){
                    System.err.println("Failed to delete " + label + " (" + target.getAbsolutePath() + "): " + e.getMessage());
                }else {
                    try {
                        Thread.sleep(100L * attempt);
                    }catch (InterruptedException ie){
                        Thread.currentThread().interrupt();
                        System.err.println("Interrupted while retrying delete for " + label + ":" + target.getAbsolutePath());
                        return;
                    }
                }
            }
        }
    }
}

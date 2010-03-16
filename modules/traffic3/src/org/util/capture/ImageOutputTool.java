package org.util.capture;

import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.awt.image.BufferedImage;

/**
 *
 */
public class ImageOutputTool {

    private int imageMaximum = 1000;
    private File rootFolder;
    private int imageCounter;
    private double minimumInterval = 0;
    private Double lastImageTime;
    private List<ImageObject> imageList;
    private String filenameA = "p";
    private String filenameB = ".png";
    private String formatName = "png";
    private int exceptionCounter;

    /**
     * constructor.
     * @param folder folder
     * @throws NullPointerException folder cannot be null
     */
    public ImageOutputTool(File folder) throws NullPointerException, FileNotFoundException {
        init(folder);
    }

    private void init(File folder) throws NullPointerException, FileNotFoundException {
        rootFolder = folder;
        if (rootFolder == null) {
            throw new NullPointerException();
        }
        if (!rootFolder.exists()) {
            if (!rootFolder.mkdir()) {
                throw new FileNotFoundException("cannot create directory: " + rootFolder.getAbsolutePath());
            }
        }

        imageCounter = -1;
        imageList = new ArrayList<ImageObject>();
        lastImageTime = null;
    }

    /**
     * set minimum interval.
     * While any minimum interval time, add method will not save more than one image.
     * @param rate rate
     */
    public void setMinimumInterval(double rate) {
        minimumInterval = rate;
    }

    /**
     * add image.
     * @param bi image
     * @param time time[ms]
     * @throws IOException io
     * @throws ImageOutputCountExceededLimitException cannot save image
     */
    public void add(BufferedImage bi, double time) throws IOException, ImageOutputCountExceededLimitException {
        if (lastImageTime != null && time < lastImageTime + minimumInterval) {
            return;
        }
        if (imageCounter + 1 > imageMaximum) {
            if (exceptionCounter > 1) {
                return ;
            }
            exceptionCounter++;
            throw new ImageOutputCountExceededLimitException("maximum: " + imageMaximum);
        }
        imageCounter++;
        String fileName = filenameA + imageCounter + filenameB;
        File file = new File(rootFolder, fileName);
        ImageIO.write(bi, formatName, file);
        ImageObject io = new ImageObject(file, time);
        imageList.add(io);
        lastImageTime = time;
    }

    /**
     * get time of last saved iamge. this parameter can be null at the biginning.
     * @return last last saved time
     */
    public Double getLastTime() {
        return lastImageTime;
    }
    /**
     * get length of images
     * @return length
     */
    public int getLength() {
        return imageCounter + 1;
    }

    /**
     * this is the finish operation. (output html and JavaScript files which can show you images as video in browser)
     */
    public void outputHTMLFiles() throws IOException {
        final String handleURL = "data/resources/handle.png";
        final String videoHTMLURL = "data/resources/video.html";
        final String videoPlayerURL = "data/resources/videoPlayer.js";
        copy(handleURL, new File(rootFolder, "handle.png"));
        copy(videoHTMLURL, new File(rootFolder, "video.html"));
        copy(videoPlayerURL, new File(rootFolder, "videoPlayer.js"));
        StringBuffer sb = new StringBuffer("");
        final String before = "p";
        final String after = ".png";
        final String cmax = String.valueOf(imageCounter);
        final String wait = String.valueOf(minimumInterval);
        sb.append("var before = '" + before + "';\n");
        sb.append("var after = '" + after + "';\n");
        sb.append("var cmax = '" + cmax + "';\n");
        sb.append("var wait = '" + wait + "';\n");
        copy(new ByteArrayInputStream((sb.toString()).getBytes()), new FileOutputStream(new File(rootFolder, "videoInformation.js")));
    }


    private static void copy(String path, File ofile) throws IOException {
        InputStream is = org.util.Handy.getResourceAsStream(path);
        OutputStream os = new FileOutputStream(ofile);
        copy(is, os);
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        for (int length = in.read(buf, 0, buf.length); length != -1; length = in.read(buf, 0, buf.length)) {
            out.write(buf, 0, length);
        }
        in.close();
        out.flush();
        out.close();
    }
    /*
    private static InputStream getResourceAsStream(String path) throws IOException {
        //        URL url = getClass().getClassLoader().getResource("org/util/capture/ImageOutputTool.class");
        URL url = ClassLoader.getSystemClassLoader().getResource(path);
        if (url !=null) {
            return url.openStream();
        }
        url = ClassLoader.getSystemClassLoader().getResource("org/util/capture/ImageOutputTool.class");
        String rootFileName = url.toString();
        final int start = 9;
        final int end = rootFileName.indexOf("!");
        if (rootFileName.startsWith("jar:file:") && end != -1) {
            String jarfile = rootFileName.substring(start, end);
            File tfile = new File(jarfile);
            File file = new File(tfile.getParent(), path);
            System.err.println(file.getAbsolutePath());
            if (file.exists()) {
                return new FileInputStream(file);
            }
        }
        throw new IOException("cannot find resource: " + path);
    }
    */
    private class ImageObject {
        File savedFile;
        Double time;
        ImageObject(File f, Double t) {
            savedFile = f;
            time = t;
        }
    }
}

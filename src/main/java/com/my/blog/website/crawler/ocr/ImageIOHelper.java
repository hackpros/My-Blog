package com.my.blog.website.crawler.ocr;


import com.github.jaiimageio.plugins.tiff.TIFFImageWriteParam;

import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;


public class ImageIOHelper {
    /**
     * 图片文件转换为tif格式
     * 
     * @param imageFile
     *            文件路径
     * @param imageFormat
     *            文件扩展名
     * @return
     */
    public static File createImage(File imageFile, String imageFormat) {
        File tempFile = null;
        try {
            Iterator<ImageReader> readers = ImageIO
                    .getImageReadersByFormatName(imageFormat);
            ImageReader reader = readers.next();

            ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
            reader.setInput(iis);
            IIOMetadata streamMetadata = reader.getStreamMetadata();

            TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(
                    Locale.CHINESE);
            tiffWriteParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);

            Iterator<ImageWriter> writers = ImageIO
                    .getImageWritersByFormatName("tiff");
            ImageWriter writer = writers.next();

            BufferedImage bi = reader.read(0);
            IIOImage image = new IIOImage(bi, null, reader.getImageMetadata(0));
            tempFile = tempImageFile(imageFile);
            ImageOutputStream ios = ImageIO.createImageOutputStream(tempFile);
            writer.setOutput(ios);
            writer.write(streamMetadata, image, tiffWriteParam);
            ios.close();

            writer.dispose();
            reader.dispose();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    private static File tempImageFile(File imageFile) {
        String path = imageFile.getPath();
        StringBuffer strB = new StringBuffer(path);
        strB.insert(path.lastIndexOf('.'), 0);
        return new File(strB.toString().replaceFirst("(?<=//.)(//w+)$", "tif"));
    }

}
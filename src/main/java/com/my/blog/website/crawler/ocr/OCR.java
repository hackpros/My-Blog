package com.my.blog.website.crawler.ocr;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class OCR {
    private final String LANG_OPTION = "-l"; // 英文字母小写l，并非数字1
    private final String EOL = System.getProperty("line.separator");
    private String tessPath = "D://Tesseract-OCR";//Tesseract安装路径

    public String recognizeText(File imageFile, String imageFormat)
            throws Exception {
        File tempImage = ImageIOHelper.createImage(imageFile, imageFormat);
        File outputFile = new File(imageFile.getParentFile(), "output");
        StringBuffer strB = new StringBuffer();
        List<String> cmd = new ArrayList<String>();
        if (isOSLinux()) {
            cmd.add("tesseract");
        } else {
            cmd.add(tessPath + "//tesseract");
        }
        cmd.add("");
        cmd.add(outputFile.getName());

        ProcessBuilder pb = new ProcessBuilder();
        pb.directory(imageFile.getParentFile());

        cmd.set(1, tempImage.getName());
        pb.command(cmd);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int w = process.waitFor();

        // 删除临时正在工作文件
        tempImage.delete();

        if (w == 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(outputFile.getAbsolutePath() + ".txt"),
                    "UTF-8"));

            String str;
            while ((str = in.readLine()) != null) {
                strB.append(str).append(EOL);
            }
            in.close();
        } else {
            String msg;
            switch (w) {
                case 1:
                    msg = "Errors accessing files.There may be spaces in your image's filename.";
                    break;
                case 29:
                    msg = "Cannot recongnize the image or its selected region.";
                    break;
                case 31:
                    msg = "Unsupported image format.";
                    break;
                default:
                    msg = "Errors occurred.";
            }
            tempImage.delete();
        }
        new File(outputFile.getAbsolutePath() + ".txt").delete();
        return strB.toString();
    }

    public static boolean isOSLinux() {
        Properties prop = System.getProperties();
        String os = prop.getProperty("os.name");
        if (os != null && os.toLowerCase().indexOf("linux") > -1) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        String valCode = null;
        String path = "D:\\captcha\\yha68rf2bnwgz7v5k4cxe90s3iqtpduom.png";
        try {
            valCode = new OCR().recognizeText(new File(path), "png");
            System.out.println("验证码为：" + valCode);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
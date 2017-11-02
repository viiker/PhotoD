package com.viisoo.PhotoD;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.Iterator;

public class ImageCompose {

    protected static Logger logger = Logger.getLogger(ImageCompose.class);
    protected static int newImageWidth=0;

    private static double xShiftD;
    private static double yShiftD;

    public ImageCompose() {
    }

    public static void call(File imgFile, String inPath, String outPath, String domain, double xShift, double yShift) {
        xShiftD = xShift;
        yShiftD = yShift;
        // 生成下载链接
        String downloadUrl = genDownloadUrl(imgFile, inPath, domain);
        // 计算二维码图片大小
        int qrcImgSize = calQRCodeImgSize(imgFile);
        // 生成二维码图片
        String qrcImagePath = QRCode.QRCodeGenerate(imgFile.getPath(), downloadUrl, qrcImgSize);
        // 将二维码合成到原始照片中
        combineImages(imgFile.getPath(), qrcImagePath, inPath, outPath);
    }

    private static String genDownloadUrl(File imgFile, String inPath, String domain) {
        String imgRelName = imgFile.toString().substring(inPath.length());
        String imgUriStr = null;
        try {
            imgUriStr = URLEncoder.encode(FileUtility.getFileModifyDate(imgFile.toString()) + imgRelName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String resultUrl = "http://" + domain + "/download.html?url=/" + imgUriStr;
        return resultUrl;
    }

    private static int calQRCodeImgSize(File imgFile) {
        int qrcSize = 0;
        BufferedImage ImageFile;
        try {
            ImageFile = ImageIO.read(imgFile);
            int width = ImageFile.getWidth();// 图片宽度
            int height = ImageFile.getHeight();// 图片高度
            if (height > width) {
                qrcSize = width/10;
            } else {
                qrcSize = height/10;
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("解析图片长度出错!");
        }

        return qrcSize;
    }

    private static void combineImages(String srcImgName, String qrcFileName, String imgInPath, String imgOutPath) {
        int width=0, height=0, qrcImgWidth=0;
        // 读取照片文件
        File imageFile = new File(srcImgName);
        BufferedImage imageBuffer = null;
        try {
            imageBuffer = ImageIO.read(imageFile);
            width = imageBuffer.getWidth();// 图片宽度
            height = imageBuffer.getHeight();// 图片高度
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 读取二维码文件
        File qrcFile = new File(qrcFileName);
        BufferedImage qrcBuffer = null;
        try {
            qrcBuffer = ImageIO.read(qrcFile);
            qrcImgWidth = qrcBuffer.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int[] qrcImageArray = new int[qrcImgWidth * qrcImgWidth];

        // 解析二维码图片文件
        qrcImageArray = qrcBuffer.getRGB(0, 0, qrcImgWidth, qrcImgWidth, qrcImageArray, 0, qrcImgWidth);
        int widthShift = 0;
        int heightShift = 0;
        if(width > height) {
            widthShift = (int)((float)qrcImgWidth * (1.0F + xShiftD));
            heightShift = (int)((float)qrcImgWidth * (1.0F + yShiftD));
        } else {
            widthShift = (int)((float)qrcImgWidth * (1.0F + yShiftD));
            heightShift = (int)((float)qrcImgWidth * (1.0F + xShiftD));
        }
        // 合成带二维码的图片
        imageBuffer.setRGB(width-widthShift, height-heightShift, qrcImgWidth, qrcImgWidth,
                qrcImageArray, 0, qrcImgWidth);// 设置覆盖部分的RGB

        File outputDir = new File(imgOutPath);
        if(!outputDir.exists()){
            outputDir.mkdirs();
        }
        String desImageName = outputDir.getPath() + srcImgName.substring(imgInPath.length(), srcImgName.length());
        File desImageDir = new File(desImageName.substring(0, desImageName.lastIndexOf(File.separator)));
        if (!desImageDir.exists()) {
            desImageDir.mkdirs();
        }
        // 将合成的图片buffer写到文件
        try {
            writeJPG(imageBuffer, new FileOutputStream(desImageName), 1.0f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 删除二维码图片文件
        qrcFile.delete();
    }

    public static void writeJPG(BufferedImage bufferedImage, OutputStream outputStream, float quality){
        Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("jpg");
        ImageWriter imageWriter = iterator.next();
        ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionQuality(quality);
        ImageOutputStream imageOutputStream =
                new MemoryCacheImageOutputStream(outputStream);
        imageWriter.setOutput(imageOutputStream);
        IIOImage iioimage = new IIOImage(bufferedImage, null, null);
        try {
            imageWriter.write(null, iioimage, imageWriteParam);
            imageOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                imageOutputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

    }
}

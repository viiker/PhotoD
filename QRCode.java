package com.viisoo.PhotoD;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by niweihua on 2017/10/22.
 */
public class QRCode {
    protected static Logger logger = Logger.getLogger(QRCode.class);

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;

    public static String QRCodeGenerate(String imagePath, String imageURL, int QRCodeSize) {
        String qrcImagePath="";
        // 解析生成二维码图片
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(imageURL,
                    BarcodeFormat.QR_CODE, QRCodeSize, QRCodeSize, hints);

            String imageName = imagePath.substring(0, imagePath.lastIndexOf("."));
            String qrcPath = imageName + ".png";
            File qrcImageFile = new File(qrcPath);
            try {
                writeToFile(bitMatrix, "png", qrcImageFile);
                qrcImagePath = qrcImageFile.getPath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return qrcImagePath;
    }

    private static void writeToFile(BitMatrix matrix, String format, File file) throws IOException {
        BufferedImage image = genBufferedImage(matrix);
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("无法生成二维码图片，请联系客服解决。");
        }
    }

    private static BufferedImage genBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }
}

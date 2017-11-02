package com.viisoo.PhotoD;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.config.Config;
import com.qiniu.api.io.IoApi;
import com.qiniu.api.io.PutExtra;
import com.qiniu.api.io.PutRet;
import com.qiniu.api.rs.PutPolicy;
import org.apache.log4j.Logger;
import org.json.JSONException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by niweihua on 2017/10/22.
 */
public class ImageUpload {
    protected static Logger logger = Logger.getLogger(ImageUpload.class);

    private static String accessKey;
    private static String secretKey;
    private static String bucketName;
    private static String domain;

    public ImageUpload() {}

    public static void call(File imgFile, String inPath, String qnUserName, int photoSize){
        // 获取七牛账号信息
        fetchQiniuAccount(qnUserName);
        // 生成按比例缩小的图片
        String srcImagePath = imgFile.getPath();
        String smallImagePath = srcImagePath.substring(0, srcImagePath.lastIndexOf(".")) + "_small_" +
                srcImagePath.substring(srcImagePath.lastIndexOf("."), srcImagePath.length());
        genScaledImage(imgFile, smallImagePath, photoSize, true);
        // 上传已经缩小的图片
        uploadImage(imgFile.getPath(), smallImagePath, inPath);
    }

    private static void fetchQiniuAccount(String qnUserName) {
        Object akObj = InitQiNiuUrl.getInstance().confMap.get("ACCESS_KEY");
        if (akObj == null) {
            accessKey = Constants.ACCESS_KEY;
        } else {
            accessKey = InitQiNiuUrl.getInstance().confMap.get("ACCESS_KEY").toString();
        }
        Object skObj = InitQiNiuUrl.getInstance().confMap.get("SECRET_KEY");
        if (skObj == null) {
            secretKey = Constants.SECRET_KEY;
        } else {
            secretKey = InitQiNiuUrl.getInstance().confMap.get("SECRET_KEY").toString();
        }
        // 获取上传账户信息
        Map<String, String> userInfoMap = (Map<String, String>) InitQiNiuUrl.getInstance().confMap.get(qnUserName);
        if (userInfoMap == null && userInfoMap.isEmpty()) {
            logger.info("获取账号信息失败，请退出程序，并联系客服解决!");
        } else {
            bucketName = userInfoMap.get("bucketName");
            domain = userInfoMap.get("domain");
        }
    }

    private static void genScaledImage(File srcImgFile, String desImgPath, int photoSize, boolean highQuality) {
        BufferedImage imgBuffer = null;
        try {
            imgBuffer = ImageIO.read(srcImgFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int type = (imgBuffer.getTransparency() == Transparency.OPAQUE) ?
                BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;

        BufferedImage retBuffer = (BufferedImage) imgBuffer;

        int w = imgBuffer.getWidth(), h = imgBuffer.getHeight();
        int targetWidth = w, targetHeight = h;
        if (w > h && w > photoSize) {
            targetWidth = photoSize;
            targetHeight = h * photoSize / w;
        } else if (w < h && h > photoSize) {
            targetWidth = w * photoSize / h;
            targetHeight = photoSize;
        }

        do {
            if (highQuality && w > targetWidth) {
                w /= 2;
                if (w < targetWidth) {
                    w = targetWidth;
                }
            }
            if (highQuality && h > targetHeight) {
                h /= 2;
                if (h < targetHeight) {
                    h = targetHeight;
                }
            }

            BufferedImage tmpBuffer = new BufferedImage(w, h, type);
            Graphics2D g2 = tmpBuffer.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(retBuffer, 0, 0, w, h, null);
            g2.dispose();

            retBuffer = tmpBuffer;
        } while (w != targetWidth || h != targetHeight);

        try {
            ImageCompose.writeJPG(retBuffer, new FileOutputStream(desImgPath), 0.92f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void uploadImage(String srcImagePath, String smallImagePath, String inputPath) {
        // 创建上传机制
        Config.ACCESS_KEY = accessKey;
        Config.SECRET_KEY = secretKey;
        Mac mac = new Mac(Config.ACCESS_KEY, Config.SECRET_KEY);
        PutPolicy putPolicy = new PutPolicy(bucketName);
        String upToken = null;
        try {
            upToken = putPolicy.token(mac);
        } catch (AuthException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        PutExtra extra = new PutExtra();
        // 获取照片文件相对路径名
        String imgRelName = srcImagePath.substring(inputPath.length());
        String imgUriStr = FileUtility.getFileModifyDate(srcImagePath) + imgRelName;
        // 开始上传照片
        PutRet putRet = IoApi.putFile(upToken, imgUriStr, smallImagePath, extra);
        if(putRet.ok()) {
            FileUtility.deleteFile(smallImagePath);
        } else {
            FileUtility.deleteFile(smallImagePath);
            putRet.getResponse();
            logger.info("[上传失败]：" + imgRelName.substring(1));
        }
    }
}

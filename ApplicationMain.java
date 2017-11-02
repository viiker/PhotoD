package com.viisoo.PhotoD;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLDecoder;
import java.util.Map;

/**
 * Created by niweihua on 2017/10/21.
 * 程序入口
 */
public class ApplicationMain {

    protected Logger logger = Logger.getLogger(ApplicationMain.class);

    private static Map<String,Object> confMap;

    private static MyProperties property;

    public ApplicationMain() {
        property = new MyProperties();
    }

    public void runApp(){

        boolean result;

        if (!property.exists()) {
            this.logger.info("配置文件不存在，请退出程序，并联系客服解决!");
            return;
        }

        String isOnline = property.getPropertiesValue("IS_ONLINE");
        if (StringUtils.isNotBlank(isOnline) && isOnline.equals("true")) {
            this.logger.info("正在运行微速照片发布程序【在线模式】\n");
            confMap = InitQiNiuUrl.getInstance().readUrl();
            if (null == confMap) {
                this.logger.info("获取在线配置信息失败，请确认电脑是否已经联网。");
                return;
            }
            result = this.checkOnlineParameter();
        } else {
            this.logger.info("正在运行微速照片发布程序【离线模式】\n");
            result = this.checkOfflineParameter();
        }

        result = this.checkInOutPath();

        if(result) {
            CommonPathWorker worker = new CommonPathWorker();
            worker.handleRestImage();
            worker.monitorDirectory();
        }
    }

    private boolean checkOnlineParameter() {
        boolean ret = true;
        String qnUserName = property.getPropertiesValue("USERNAME");
        if(StringUtils.isBlank(qnUserName)){
            ret = false;
        } else {
            Map<String,String> userInfoMap = (Map<String,String>)InitQiNiuUrl.getInstance().confMap.get(qnUserName);
            if(userInfoMap == null || userInfoMap.isEmpty()){
                ret = false;
            }
        }

        Object akObj = InitQiNiuUrl.getInstance().confMap.get("ACCESS_KEY");
        Object skObj = InitQiNiuUrl.getInstance().confMap.get("SECRET_KEY");
        if(akObj == null||skObj == null){
            ret = false;
        }

        if(!ret) {
            this.logger.info("用户名不存在，请退出程序，并联系客服解决!");
        }
        return ret;
    }

    private boolean checkOfflineParameter() {
        String qnDomain = property.getPropertiesValue("DOMAIN");
        if(StringUtils.isBlank(qnDomain)){
            this.logger.info("离线地址不正确，请退出程序，并联系客服解决!");
            return false;
        } else {
            return true;
        }
    }

    private boolean checkInOutPath() {
        String inPath = property.getPropertiesValue("PHOTO_IN_PATH");
        String outPath = property.getPropertiesValue("PHOTO_OUT_PATH");

        if(null == inPath) {
            this.logger.info("未设置照片回收目录,请退出程序并重新设置！");
            return false;
        }

        if(null == outPath) {
            this.logger.info("未设置照片打印目录,请退出程序并重新设置！");
            return false;
        }

        if(inPath.equalsIgnoreCase(outPath)) {
            this.logger.info("照片回收目录和打印目录相同,将导致程序运行出错，请退出并重新设置！");
            return false;
        }

        File checkInPath = new File(inPath);
        if(!checkInPath.exists() || !checkInPath.isDirectory()) {
            this.logger.info("照片回收目录不存在,请退出程序并重新设置！");
            return false;
        } else {
            this.logger.info("照片回收目录: " + checkInPath.getPath());
        }
        File checkOutPath = new File(outPath);
        if(!checkOutPath.exists() || !checkOutPath.isDirectory()) {
            this.logger.info("照片打印目录不存在,请退出程序并重新设置！");
            return false;
        } else {
            this.logger.info("照片打印目录: " + checkOutPath.getPath() + "\n");
        }
        return true;
    }

    public static void main(String[] args) {
        Console console = System.console();
        if(console != null) {
            ApplicationMain main = new ApplicationMain();
            main.runApp();
            console.readLine();
        }
    }
}

package com.viisoo.PhotoD;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.Console;
import java.io.IOException;

/**
 * Created by niweihua on 2017/10/28.
 */
public class SwitchModeMain {

    protected Logger logger = Logger.getLogger(ApplicationMain.class);

    private static MyProperties property;

    public SwitchModeMain() {
        property = new MyProperties();
    }

    public void runApp(){
        if (!property.exists()) {
            this.logger.info("配置文件不存在，请退出程序，并联系客服解决!");
            return;
        }

        String isOnline = property.getPropertiesValue("IS_ONLINE");
        if (StringUtils.isNotBlank(isOnline) && isOnline.equals("true")) {
            this.logger.info("正在运行微速照片发布程序【在线模式】，是否切换成【离线模式】？\n1.是，2.否");
            try {
                char i = (char) System.in.read();
                if(i == '1') {
                    switchMode("false");
                    this.logger.info("已切换成【离线模式】，请退出并重启照片发布程序。");
                } else {
                    this.logger.info("模式未切换，请退出程序。");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.logger.info("正在运行微速照片发布程序【离线模式】，是否切换成【在线模式】？\n1.是，2.否");
            try {
                char i = (char) System.in.read();
                if(i == '1') {
                    switchMode("true");
                    this.logger.info("已切换成【在线模式】，请退出并重启照片发布程序。");
                } else {
                    this.logger.info("模式未切换，请退出程序。");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void switchMode(String value) {
        property.setPropertiesValue("IS_ONLINE", value);
    }

    public static void main(String[] args) {
        Console console = System.console();
        if(console != null) {
            SwitchModeMain main = new SwitchModeMain();
            main.runApp();
            console.readLine();
        }
    }
}

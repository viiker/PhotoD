package com.viisoo.PhotoD;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Created by niweihua on 2017/10/21.
 */
public class MyProperties {
    protected Logger logger = Logger.getLogger(MyProperties.class);

    private String propertyPath;
    private Properties property = new Properties();

    public MyProperties() {
        propertyPath = MyProperties.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        propertyPath = propertyPath.substring(1, propertyPath.length());
        int endIndex = propertyPath.lastIndexOf("/");
        propertyPath = propertyPath.substring(0, endIndex + 1);

        try {
            propertyPath = java.net.URLDecoder.decode(propertyPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {
            InputStream in = new BufferedInputStream(new FileInputStream(this.getPropertiesPath()));
            try {
                property.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean exists() {
        File propertiesFile = new File(this.getPropertiesPath());
        if(propertiesFile.exists()){
            return true;
        } else {
            return false;
        }
    }

    public String getPropertiesValue(String key) {
        if(null == key || key == "") {
            return null;
        } else {
            return property.getProperty(key);
        }
    }

    public void setPropertiesValue(String key, String value) {
        if(null != key && key != "") {
            try {
                OutputStream out = new FileOutputStream(getPropertiesPath());
                property.setProperty(key, value);
                try {
                    property.store(out, "");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPropertiesPath() {
        return propertyPath + "conf.properties";
    }

}

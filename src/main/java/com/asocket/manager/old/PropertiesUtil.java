package com.asocket.manager.old;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Qu SongTao@低调火药 on 2015/8/6.
 */
public class PropertiesUtil {

    /**
     *
     * 按照key读取value
     *
     * @param filePath
     * @param key
     * @return
     */
    public static String getValue(String filePath,String key) {
        Properties props = new Properties();
        InputStream in = null;
        String value = null;
        String url;
        try {
            url = PropertiesUtil.class.getClassLoader().getResource("").getPath();
            in = new BufferedInputStream (new FileInputStream(url+filePath));
            props.load(in);
            value = props.getProperty (key);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                in.close();
            }catch (Exception e){

            }
            return value;
        }
    }

    /**
     * 读取properties的全部信息
     *
     * @param filePath
     */
    public static void readProperties(String filePath) {
        Properties props = new Properties();
        InputStream in = null;
        try {
            in = new BufferedInputStream (new FileInputStream(filePath));
            props.load(in);
            Enumeration en = props.propertyNames();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                String Property = props.getProperty (key);
                System.out.println(key+Property);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try{
                in.close();
            }catch (Exception e){

            }
        }
    }

    /**
     * 写入properties信息
     *
     * @param filePath
     * @param parameterName
     * @param parameterValue
     */
    public static void setValue(String filePath,String parameterName,String parameterValue) {
        Properties prop = new Properties();
        InputStream fis=null;
        OutputStream fos = null;
        String url;
        try {
            url = PropertiesUtil.class.getClassLoader().getResource("").getPath();
            System.out.println(url);
            fis = new FileInputStream(url + filePath);
            prop.load(fis);
            fos = new FileOutputStream(url + filePath);
            prop.setProperty(parameterName, parameterValue);
            prop.store(fos, "Update '" + parameterName + "' value");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Visit "+filePath+" for updating "+parameterName+" value error");
        }finally {
            try{
                fos.flush();
                fos.close();
                fis.close();
            }catch (Exception e){

            }
        }
    }
    
    
     public static String getNowDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        return formatter.format(currentTime);
    }


}

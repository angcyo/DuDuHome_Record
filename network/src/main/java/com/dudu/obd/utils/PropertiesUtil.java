package com.dudu.obd.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * 配置文件工具
 * 〈功能详细描述〉
 * @author    Bruce
 * @date      2016年1月12日 下午3:22:19
 */
public class PropertiesUtil {

    public static Properties          prop   = new Properties();

    /**
     * 配置文件的key,value
     */
    public static Map<String, Object> CONFIG = new HashMap<String, Object>();

    /**
     * 私有构造函数
     */
    private PropertiesUtil() {
    }

    /**
     * 初始化配置文件
     */
    static {
        Properties prop = new Properties();
        try {
            prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties"));
            Iterator<String> iterator = prop.stringPropertyNames().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                CONFIG.put(key, prop.getProperty(key).trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取配置文件，优先读取缓存，没有缓存则读取文件然后放入缓存中
     * @param key 配置文件中的key
     * @return
     * @author Bruce
     * @date 2016年1月12日 下午3:22:42
     */
    public static String readProperties(String key) {

        String value = null;
        value = (String) CONFIG.get(key);
        if (null == value || "".equals(value)) {
            try {
                prop.load(PropertiesUtil.class.getClassLoader().getResourceAsStream("config.properties"));
                value = prop.getProperty(key).trim();
                if (null != value || !"".equals(value)) {
                    CONFIG.put(key, value);
                }
            } catch (IOException e) {
                System.out.println("读取配置文件失败");;
                e.printStackTrace();
            }
        }
        return value == null ? "" : value;
    }

}

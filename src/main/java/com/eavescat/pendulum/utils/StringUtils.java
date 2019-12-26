package com.eavescat.pendulum.utils;

/**
 * 自定义字符串操作工具类
 * Created by wendrewshay on 2019/7/13 15:00
 */
public class StringUtils {

    /**
     * 连接字符串
     * @author wendrewshay
     * @date 2019/7/13 15:05
     * @param args 需要被连接的字符串参数
     * @return String
     */
    public static String concat(String... args) {
        if (args != null && args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String arg : args) {
                sb.append(arg);
            }
            return sb.toString();
        }
        return null;
    }
}

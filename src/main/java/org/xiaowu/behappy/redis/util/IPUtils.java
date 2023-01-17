package org.xiaowu.behappy.redis.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ip工具类
 * @author 94391
 */
@Slf4j
public class IPUtils {

    private IPUtils() {
    }

    /**
     * 检查IP是否合法
     * @param ip
     * @return
     */
    public static boolean ipValid(String ip) {
        String regex0 = "(2[0-4]\\d)" + "|(25[0-5])";
        String regex1 = "1\\d{2}";
        String regex2 = "[1-9]\\d";
        String regex3 = "\\d";
        String regex = "(" + regex0 + ")|(" + regex1 + ")|(" + regex2 + ")|(" + regex3 + ")";
        regex = "(" + regex + ").(" + regex + ").(" + regex + ").(" + regex  + ")";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(ip);
        return m.matches();
    }

    /**
     * 获取本地ip 适合windows与linux
     *
     * @return
     */
    public static String getLocalIP() {
        String localIP = "127.0.0.1";
        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) netInterfaces.nextElement();
                InetAddress ip = ni.getInetAddresses().nextElement();
                if (!ip.isLoopbackAddress() && !ip.getHostAddress().contains(":")) {
                    localIP = ip.getHostAddress();
                    break;
                }
            }
        } catch (Exception e) {
            try {
                localIP = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
        }
        return localIP;
    }

    /**
     * 获取客户机的ip地址
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        String unknownStr = "unknown";
        if (ip == null || ip.length() == 0 || unknownStr.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknownStr.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknownStr.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip == null || ip.length() == 0 || unknownStr.equalsIgnoreCase(ip)) {
            ip = request.getHeader("http_client_ip");
        }
        if (ip == null || ip.length() == 0 || unknownStr.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        // 如果是多级代理，那么取第一个ip为客户ip
        if (ip != null && ip.contains(",")) {
            ip = ip.substring(ip.lastIndexOf(",") + 1).trim();
        }
        return ip;
    }


    /**
     * 把ip转化为整数
     * @param ip
     * @return
     */
    public static long translateIP2Int(String ip){
        String[] intArr = ip.split("\\.");
        int[] ipInt = new int[intArr.length];
        for (int i = 0; i <intArr.length ; i++) {
            ipInt[i] = Integer.parseInt(intArr[i]);
        }
        return (long) ipInt[0] * 256 * 256 * 256 + + ipInt[1] * 256 * 256 + ipInt[2] * 256L + ipInt[3];
    }
}

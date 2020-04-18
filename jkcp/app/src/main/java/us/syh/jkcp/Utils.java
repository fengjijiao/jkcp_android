package us.syh.jkcp;

import android.util.Log;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Utils {
    private static String TAG = "Utils";

    /**
     * 获取所有网卡名
     * @return
     */
    public static String[] getAllNetInterface() {
        List<String> availableInterface = new ArrayList<>();
        String [] interfaces = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }

                    String ip = ia.getHostAddress();
                    Log.d(TAG,"getAllNetInterface,available interface:"+ni.getName()+",address:"+ip);
                    // 过滤掉127段的ip地址
                    if (!"127.0.0.1".equals(ip)) {
                        availableInterface.add(ni.getName());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"all interface:"+availableInterface.toString());
        int size = availableInterface.size();
        if (size > 0) {
            interfaces = new String[size];
            for(int i = 0; i < size; i++) {
                interfaces[i] = availableInterface.get(i);
            }
        }
        return interfaces;
    }

    /**
     * 获取所有ip
     * @param netInterface
     * @param type IP类型，ipv6,ipv4,local-ipv6
     * @return IP地址集
     * @throws SocketException
     */
    public static String[] getIpAddress(String netInterface, String type) throws SocketException {
        List<String> availableIpAddress = new ArrayList<>();
        String[] hostIps = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                //Log.d(TAG,"getIpAddress,interface:"+ni.getName());
                if (ni.getName().equals(netInterface)) {
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement();
                        if (ia instanceof Inet6Address && type.equals("ipv4")) {
                            continue;// skip ipv6
                        }
                        if (ia instanceof Inet4Address && !type.equals("ipv4")) {
                            continue;// skip ipv4
                        }
                        String ip = ia.getHostAddress();
                        // 过滤掉127段的ip地址
                        if (!"127.0.0.1".equals(ip) && !ip.endsWith(netInterface)) {
                            if(type.equals("local-ipv6") ^ ip.startsWith("f")) continue;
                            availableIpAddress.add(ia.getHostAddress());
                            Log.d(TAG,"getIpAddress,ip:" + ia.getHostAddress());
                            //break;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        hostIps = new String[availableIpAddress.size()];
        for (int i =0; i< availableIpAddress.size(); i++) {
            hostIps[i] = availableIpAddress.get(i);
        }
        return hostIps;
    }
}

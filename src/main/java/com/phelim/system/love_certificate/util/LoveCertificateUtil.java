package com.phelim.system.love_certificate.util;

public class LoveCertificateUtil {

    public static String maskIp(String ip) {
        if (ip == null || ip.isBlank()) return null;

        // normalize localhost IPv6
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        // IPv4: 192.168.1.10 => 192.168.xxx.xxx
        if (ip.contains(".")) {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                return parts[0] + "." + parts[1] + ".xxx.xxx";
            }
            return ip; // fallback
        }

        // IPv6
        if (ip.contains(":")) {
            String[] parts = ip.split(":");
            if (parts.length >= 2) {
                return parts[0] + ":" + parts[1] + ":xxxx:xxxx";
            }
            return "xxxx:xxxx";
        }
        return ip; // fallback
    }
    //127.0.0.1                                 127.0.xxx.xxx
    //192.168.1.10                              192.168.xxx.xxx
    //0:0:0:0:0:0:0:1                           127.0.0.1
    //2001:db8:abcd:0012:0000:0000:0000:0001    2001:db8:xxxx:xxxx
}

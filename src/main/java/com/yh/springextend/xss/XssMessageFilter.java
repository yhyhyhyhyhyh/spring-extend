package com.yh.springextend.xss;

/**
 * @ClassName XssMessageFilter
 * @Description 字符串xss过滤
 * @Author yh
 * @Date 2020-06-24 17:56
 * @Version 1.0
 */
public class XssMessageFilter {

    /**
     * xss过滤逻辑
     * @param src
     * @return
     */
    public static String filt(String src) {
        return XssEncodeUtil.xssEncode(src);
    }

    private XssMessageFilter() {

    }
}

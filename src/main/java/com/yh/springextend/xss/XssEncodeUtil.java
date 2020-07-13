package com.yh.springextend.xss;


/***
 * xss特殊字符转码问题
 * @author yanfenglan
 * @version 1.0 20161024
 *
 */
public class XssEncodeUtil {

	private XssEncodeUtil() {

	}

	public static String xssEncode(String param) {
		if(param == null || param == "") {
            return param;
        }
		String value = param;
		value = value.replace('<','＜');
		value = value.replace('>','＞'); 
		value = value.replace('\\','＼');
		value = value.replace('\'','＇');
		value = value.replace('(','（'); 
		value = value.replace(')','）'); 
		value = value.replace('&','＆'); 
		return value;
	}


}

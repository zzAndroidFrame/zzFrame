package xtom.frame.util;

import org.json.JSONObject;

import xtom.frame.exception.DataParseException;

/**
 * JSON工具类
 */
public class XtomJsonUtil {
	/**
	 * 字符串转JSON
	 * 
	 * @param s
	 *            需要转换的字符串
	 * @return JSONObject
	 * @throws DataParseException
	 */
	public static JSONObject toJsonObject(String s) throws DataParseException {
		if (s != null && s.startsWith("\ufeff")) // 避免低版本utf-8bom头问题
			s = s.substring(1);
		try {
			return new JSONObject(s.trim());
		} catch (Exception e) {
			throw new DataParseException(e);
		}
	}
}

package xtom.frame;

import org.json.JSONException;
import org.json.JSONObject;

import xtom.frame.util.XtomBaseUtil;
import xtom.frame.util.XtomLogger;

/**
 * 相当于Object，集成了log_v(msg)等打印方法以及println(Object)。
 */
public class XtomObject {
	/**
	 * 打印TAG，类名
	 */
	private String TAG;

	public XtomObject() {
		TAG = getLogTag();
	}

	/**
	 * 获取打印TAG，即类名
	 * 
	 * @return
	 */
	private String getLogTag() {
		return getClass().getSimpleName();
	}

	/**
	 * 打印v级别信息
	 * 
	 * @param msg
	 */
	protected void log_v(String msg) {
		XtomLogger.v(TAG, msg);
	}

	/**
	 * 打印d级别信息
	 * 
	 * @param msg
	 */
	protected void log_d(String msg) {
		XtomLogger.d(TAG, msg);
	}

	/**
	 * 打印i级别信息
	 * 
	 * @param msg
	 */
	protected void log_i(String msg) {
		XtomLogger.i(TAG, msg);
	}

	/**
	 * 打印w级别信息
	 * 
	 * @param msg
	 */
	protected void log_w(String msg) {
		XtomLogger.w(TAG, msg);
	}

	/**
	 * 打印e级别信息
	 * 
	 * @param msg
	 */
	protected void log_e(String msg) {
		XtomLogger.e(TAG, msg);
	}

	/**
	 * 打印
	 * 
	 * @param msg
	 */
	protected void println(Object msg) {
		XtomLogger.println(msg);
	}

	/**
	 * 解析时，判断是否为空
	 * 
	 * @param jsonObject
	 * @param s
	 * @return
	 * @throws JSONException
	 */
	protected String get(JSONObject jsonObject, String s) throws JSONException {
		if (!jsonObject.isNull(s)) {
			return jsonObject.getString(s);
		}
		return null;
	}

	/**
	 * 解析时，判断是否为空
	 * 
	 * @param jsonObject
	 * @param s
	 * @return 若为空返回0
	 * @throws JSONException
	 */
	protected int getInt(JSONObject jsonObject, String s) throws JSONException {
		if (!jsonObject.isNull(s)) {
			return jsonObject.getInt(s);
		}
		return 0;
	}

	/**
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return true如果该字符串为null或者"",否则false
	 */
	protected boolean isNull(String str) {
		return XtomBaseUtil.isNull(str);
	}
}

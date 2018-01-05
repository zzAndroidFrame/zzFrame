package xtom.frame;

import xtom.frame.util.XtomBaseUtil;
import xtom.frame.util.XtomLogger;
import android.app.Application;

public class XtomApplication extends Application {
	/**
	 * 打印TAG，类名
	 */
	private String TAG;

	protected XtomApplication() {
		TAG = getLogTag();
	}

	// 获取打印TAG，即类名
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
	 * 判断字符串是否为空
	 * 
	 * @param str
	 * @return true如果该字符串为null或者"",否则false
	 */
	protected boolean isNull(String str) {
		return XtomBaseUtil.isNull(str);
	}

}

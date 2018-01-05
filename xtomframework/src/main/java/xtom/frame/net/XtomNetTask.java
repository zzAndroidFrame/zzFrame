package xtom.frame.net;

import java.util.HashMap;

import org.json.JSONObject;

import xtom.frame.XtomObject;
import xtom.frame.exception.DataParseException;

/**
 * 网络请求任务
 */
public abstract class XtomNetTask extends XtomObject {
	private int id;
	private String path;
	private String description;
	private HashMap<String, String> params;
	private HashMap<String, String> files;

	private int tryTimes = 0;

	/**
	 * 网络请求任务
	 * 
	 * @param id
	 *            任务ID
	 * @param path
	 *            url路径
	 * @param params
	 *            任务参数集(参数名,参数值)
	 * @param description
	 *            任务描述,如"获取xx列表"
	 */
	public XtomNetTask(int id, String path, HashMap<String, String> params,
			String description) {
		this(id, path, params);
		this.description = description;
	}

	/**
	 * 网络请求任务
	 * 
	 * @param id
	 *            任务ID
	 * @param path
	 *            url路径
	 * @param params
	 *            任务参数集(参数名,参数值)
	 * @param files
	 *            任务文件集(参数名,文件的本地路径)
	 * @param description
	 *            任务描述,如"获取xx列表"
	 */
	public XtomNetTask(int id, String path, HashMap<String, String> params,
			HashMap<String, String> files, String description) {
		this(id, path, params, files);
		this.description = description;
	}

	/**
	 * 网络请求任务
	 * 
	 * @param id
	 *            任务ID
	 * @param path
	 *            url路径
	 * @param params
	 *            任务参数集(参数名,参数值)
	 * @param files
	 *            任务文件集(参数名,文件的本地路径)
	 */
	public XtomNetTask(int id, String path, HashMap<String, String> params,
			HashMap<String, String> files) {
		this(id, path, params);
		this.files = files;
	}

	/**
	 * 网络请求任务
	 * 
	 * @param id
	 *            任务ID
	 * @param path
	 *            url路径
	 * @param params
	 *            任务参数集(参数名,参数值)
	 */
	public XtomNetTask(int id, String path, HashMap<String, String> params) {
		this.id = id;
		this.path = path;
		this.params = params;
	}

	/**
	 * 此方法将JSONObject解析为我们自定义的实体类
	 * 
	 * @param jsonObject
	 * @return
	 * @throws DataParseException
	 */
	public abstract Object parse(JSONObject jsonObject)
			throws DataParseException;

	/**
	 * 获取 任务ID
	 * 
	 * @return 任务ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * 获取任务参数集(参数名,参数值)
	 * 
	 * @return 任务参数集(参数名,参数值)
	 */
	public HashMap<String, String> getParams() {
		return params;
	}

	/**
	 * 获取请求地址
	 * 
	 * @return 请求地址
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 获取任务文件集(参数名,文件的本地路径)
	 * 
	 * @return 任务文件集(参数名,文件的本地路径)
	 */
	public HashMap<String, String> getFiles() {
		return files;
	}

	/**
	 * 获取尝试次数
	 * 
	 * @return 尝试次数
	 */
	public int getTryTimes() {
		return tryTimes;
	}

	public void setTryTimes(int tryTimes) {
		this.tryTimes = tryTimes;
	}

	/**
	 * 获取任务描述,如"获取xx列表"
	 * 
	 * @return 任务描述,如"获取xx列表"
	 */
	public String getDescription() {
		return description;
	}

}

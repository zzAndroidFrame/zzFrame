package xtom.frame.image.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xtom.frame.XtomObject;

/**
 * 图片在不同场合中的使用情况记录
 */
public class XtomImageCacheInMemByObject extends XtomObject {
	private HashMap<Object, ArrayList<String>> mMemoryCache;

	/**
	 * 实例化
	 */
	public XtomImageCacheInMemByObject() {
		mMemoryCache = new HashMap<Object, ArrayList<String>>();
	}

	public void add(String data, Object object) {
		synchronized (this) {
			if (!mMemoryCache.containsKey(object)) {
				ArrayList<String> keys = new ArrayList<String>();
				keys.add(data);
				mMemoryCache.put(object, keys);
			} else {
				if (!contains(mMemoryCache.get(object), data))
					mMemoryCache.get(object).add(data);
			}
		}
	}

	public void clear() {
		synchronized (this) {
			mMemoryCache.clear();
		}
	}

	public void remove(Object object) {
		synchronized (this) {
			mMemoryCache.remove(object);
		}
	}

	public void remove(String key) {
		synchronized (this) {
			for (Map.Entry<Object, ArrayList<String>> entry : mMemoryCache
					.entrySet()) {
				ArrayList<String> strings = entry.getValue();
				if (contains(strings, key))
					strings.remove(key);
			}
		}
	}

	private boolean contains(ArrayList<String> strings, String str) {
		if (strings == null)
			return false;
		return strings.contains(str);
	}

	/**
	 * 判断图片是否可以被回收
	 * 
	 * @param key
	 *            图片地址
	 * @param c
	 *            若c==1表示最多有一个页面在使用该图片时可回收，其余值表示没有页面使用时方可回收
	 * @return
	 */
	public boolean isCanClear(String key, int c) {
		synchronized (this) {
			int i = 0;
			for (Map.Entry<Object, ArrayList<String>> entry : mMemoryCache
					.entrySet()) {
				i = (contains(entry.getValue(), key)) ? i + 1 : i;
			}
			return (c == 1) ? i <= 1 : i <= 0;
		}
	}
}

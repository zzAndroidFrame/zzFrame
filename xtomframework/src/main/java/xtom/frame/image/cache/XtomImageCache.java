package xtom.frame.image.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import xtom.frame.XtomConfig;
import xtom.frame.XtomObject;
import xtom.frame.image.load.XtomImageTask;
import xtom.frame.image.load.XtomImageTask.Size;
import xtom.frame.util.XtomFileUtil;
import xtom.frame.util.XtomImageUtil;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.os.Message;

/**
 * 图片缓存
 */
public class XtomImageCache extends XtomObject {
	private Context mContext;
	private static XtomImageCache imageCache;
	private HashMap<String, SoftReference<Bitmap>> mMemoryCache;// 软引用
	private String cachePath_external;// 外部缓存路径
	private String cachePath_internal;// 内部缓存路径
	private XtomImageCacheInMemByObject cacheInMemByObject;// 图片在不同场合中的使用情况

	/**
	 * 获取缓存实例
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized XtomImageCache getInstance(Context context) {
		return (imageCache == null) ? imageCache = new XtomImageCache(
				context.getApplicationContext()) : imageCache;
	}

	/**
	 * 获取缓存实例
	 * 
	 * @return
	 */
	public static synchronized XtomImageCache get() {
		return imageCache;
	}

	private XtomImageCache(Context context) {
		mContext = context;
		cachePath_internal = context.getCacheDir().getPath() + "/images/";// 获取内部缓存路径
		File file = context.getExternalCacheDir();
		cachePath_external = (file != null) ? file.getPath() + "/images/"
				: null;// 获取外部缓存路径
		mMemoryCache = new HashMap<String, SoftReference<Bitmap>>();// 实例化软引用
		cacheInMemByObject = new XtomImageCacheInMemByObject();// 实例化图片在不同Activity中的使用情况
		deleteCache(cachePath_external, XtomConfig.IMAGES_EXTERNAL);// 若外部缓存中那个图片数量超过限制,删除一半的缓存
		deleteCache(cachePath_internal, XtomConfig.IMAGES_INTERNAL);// 若内部缓存中那个图片数量超过限制,删除一半的缓存
	}

	// 删除缓存
	private void deleteCache(String cache_path, int num) {
		if (cache_path == null)
			return;
		File cacheDir = new File(cache_path);
		if (!cacheDir.exists())
			return;
		File[] files = cacheDir.listFiles();
		if (files == null)
			return;
		if (files.length > num)
			for (int i = 0; i < num / 2; i++) {
				files[i].delete();
			}
	}

	/**
	 * 获取缓存大小byte
	 */
	public long getCacheSize() {
		return getCacheSize(cachePath_external)
				+ getCacheSize(cachePath_internal);
	}

	// 获取缓存大小
	public long getCacheSize(String cache_path) {
		if (cache_path == null)
			return 0;
		File cacheDir = new File(cache_path);
		if (!cacheDir.exists())
			return 0;
		File[] files = cacheDir.listFiles();
		long length = 0;
		for (File file : files) {
			length += file.length();
		}
		return length;
	}

	// 删除缓存
	private int deleteCache(String cache_path) {
		if (cache_path == null)
			return 0;
		File cacheDir = new File(cache_path);
		if (!cacheDir.exists())
			return 0;
		File[] files = cacheDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			files[i].delete();
		}
		return files.length;
	}

	/**
	 * 删除缓存
	 */
	public void deleteCache() {
		int s = deleteCache(cachePath_external);
		int i = deleteCache(cachePath_internal);
		log_d("delete " + (s + i) + " images");
	}

	private void cacheInMemByObj(XtomImageTask task, Bitmap bitmap) {
		if (bitmap != null && task.getContext() != null) {
			cacheInMemByObject.add(task.getKeyForMemCache(), task.getContext());
		}
	}

	/**
	 * 获取图片在本地的存储位置
	 * 
	 * @param path
	 *            图片url值
	 * @return String
	 */
	public String getPathAtLoacal(String path) {
		File file = new File(path);
		if (file.exists())
			return path;
		String temp = XtomFileUtil.getKeyForCache(path);
		return XtomFileUtil.isExternalMemoryAvailable() ? cachePath_external
				+ temp : cachePath_internal + temp;
	}

	/**
	 * 从本地或缓存中获取图片
	 * 
	 * @param task
	 * @return
	 */
	public Bitmap getBitmapFromLocal(XtomImageTask task) {
		URL url = task.getUrl();
		Bitmap bitmap = null;
		if (url != null) {// =================网络图片,执行以下操作=================
			String path = url.toString();
			bitmap = getFromMemCache(task);// 先从内存中取
			if (bitmap != null) {
				log_d("Mem  has !");
			}
			if (bitmap == null) // 若内存中没有,在本地缓存(外部缓存)中取
			{
				String lpath = cachePath_external
						+ XtomFileUtil.getKeyForCache(path);
				bitmap = getLocalPicture(lpath, task.getSize());
				if (bitmap != null)
					log_d("external_cache  has !");
			}
			if (bitmap == null)// 若本地缓存(外部缓存)中没有,在本地缓存(内部缓存)中取
			{
				String lpath = cachePath_internal
						+ XtomFileUtil.getKeyForCache(path);
				bitmap = getLocalPicture(lpath, task.getSize());

				if (bitmap != null)
					log_d("internal_cache  has !");
			}
			if (bitmap != null)// 得到图片后,添加进内存软引用
				addToMemCache(task.getKeyForMemCache(), bitmap);
		} else {// ==========================本地图片,执行以下操作==============================
			String path = task.getPath();
			if (path != null) {
				bitmap = getLocalPicture(path, task.getSize());
				if (bitmap != null)
					log_d("get the local pic success !");
				if (bitmap != null)
					addToMemCache(task.getKeyForMemCache(), bitmap);
			}
		}
		cacheInMemByObj(task, bitmap);
		return bitmap;
	}

	/**
	 * 从服务器获取图片
	 * 
	 * @param task
	 * @return
	 */
	public Bitmap getBitmapFromServer(XtomImageTask task) {
		Bitmap bitmap = getBitmapFromLocal(task);// 先从本地拿
		URL url = task.getUrl();
		if (url != null) {
			String path = url.toString();
			if (bitmap == null)// 若本地缓存没有,去服务器取
				bitmap = load(url, path, task);
			if (bitmap != null)// 得到图片后,添加进内存软引用
				addToMemCache(task.getKeyForMemCache(), bitmap);
		}
		cacheInMemByObj(task, bitmap);
		return bitmap;
	}

	// 获取本地图片
	private Bitmap getLocalPicture(String path, Size size) {
		Bitmap bitmap;
		try {
			if (size == null)
				bitmap = XtomImageUtil.getLocalPicture(path);
			else
				bitmap = XtomImageUtil.getLocPicByDBYS(path, size.getHeight(),
						size.getWidth());
			return bitmap;
		} catch (IOException e) {
			bitmap = null;
		}
		return bitmap;
	}

	// 从服务器获取图片
	private Bitmap load(URL url, String path, XtomImageTask task) {
		if (!isCanLoad()) {
			return null;
		}

		InputStream is = null;
		FileOutputStream fos = null;
		try {
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(XtomConfig.TIMEOUT_CONNECT_IMAGE);
			conn.setReadTimeout(XtomConfig.TIMEOUT_READ_IMAGE);
			is = (url.getHost().equals("")) ? null : conn.getInputStream();
			int l = conn.getContentLength();
			File file;
			File dir;
			String strdir;
			if (XtomFileUtil.isExternalMemoryAvailable())
				strdir = cachePath_external;
			else
				strdir = cachePath_internal;
			dir = new File(strdir);
			if (!dir.exists())
				dir.mkdir();
			file = new File(strdir + XtomFileUtil.getKeyForCache(path));
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[1204];
			int len = 0;
			int downlen = 0;
			while ((len = is.read(buffer)) != -1) {
				downlen += len;
				fos.write(buffer, 0, len);
				float percent = (float) downlen / (float) l;
				Message message = task.getHandler().obtainMessage(
						XtomImageTask.LOAD_ING, percent);
				task.getHandler().sendMessage(message);
			}
			return getLocalPicture(file.getPath(), task.getSize());
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (is != null)
					is.close();
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}

	/**
	 * 在内存中获取图片
	 * 
	 * @param url
	 * @return
	 */
	public Bitmap getFromMemCache(XtomImageTask task) {
		String key = task.getKeyForMemCache();
		if (mMemoryCache.containsKey(key)) {
			Bitmap bit = mMemoryCache.get(key).get();
			if (bit != null && !bit.isRecycled()) {
				cacheInMemByObj(task, bit);
				return bit;
			} else
				remove(key);
		}
		return null;
	}

	// 删除该图片记录
	private void remove(String key) {
		cacheInMemByObject.remove(key);
		mMemoryCache.remove(key);
	}

	private synchronized void addToMemCache(String key, Bitmap bitmap) {
		if (bitmap == null)
			return;
		if (key == null)
			return;
		if (!mMemoryCache.containsKey(key)
				|| mMemoryCache.get(key).get() == null)
			mMemoryCache.put(key, new SoftReference<Bitmap>(bitmap));
	}

	public void reMoveCacheInMemByObj(Object obj) {
		cacheInMemByObject.remove(obj);
	}

	/**
	 * 释放可以回收的图片
	 */
	public void recyclePics() {
		ArrayList<String> recKeys = new ArrayList<String>();
		ArrayList<String> sofrecKeys = new ArrayList<String>();
		for (Map.Entry<String, SoftReference<Bitmap>> entry : mMemoryCache
				.entrySet()) {
			String key = entry.getKey();
			Bitmap bit = mMemoryCache.get(key).get();
			if (bit != null && cacheInMemByObject.isCanClear(key, 0)) {
				if (!bit.isRecycled())
					bit.recycle();
				recKeys.add(key);
			}
			if (bit == null) {
				sofrecKeys.add(key);
			}
		}
		log_d("Recycled " + recKeys.size() + " PIC");
		log_d("SoftReference Recycled " + sofrecKeys.size() + " PIC");
		for (String key : recKeys)
			remove(key);
		for (String key : sofrecKeys)
			remove(key);
		System.gc();

	}

	/**
	 * 释放该图片
	 * 
	 * @param key
	 */
	public void recyclePic(String key) {
		Bitmap bit = mMemoryCache.get(key).get();
		if (bit != null && !bit.isRecycled())
			bit.recycle();
		remove(key);
		System.gc();
	}

	/**
	 * 释放这些中可以回收的图片
	 * 
	 * @param keys
	 */
	public void recyclePics(ArrayList<String> keys) {
		int i = 0;
		for (String key : keys)
			if (mMemoryCache.containsKey(key)
					&& cacheInMemByObject.isCanClear(key, 1)) {
				i++;
				Bitmap bit = mMemoryCache.get(key).get();
				if (bit != null && !bit.isRecycled())
					bit.recycle();
				remove(key);
			}
		System.gc();
		log_d("Recycled " + i + " PIC");
	}

	private boolean isCanLoad() {
		if (!XtomConfig.IMAGELOAD_ONLYWIFI) {
			return true;
		}
		boolean isWifi = true;
		try {
			ConnectivityManager connManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifi = connManager.getNetworkInfo(
					ConnectivityManager.TYPE_WIFI).getState();
			if (State.CONNECTED == wifi) { // 判断是否正在使用WIFI网络
				isWifi = true;
			} else {
				isWifi = false;
			}
		} catch (Exception e) {
			isWifi = true;
		}
		return isWifi;
	}

}

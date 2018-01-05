package xtom.frame.image.load;

import java.net.URL;

import xtom.frame.XtomObject;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;

/**
 * 图片下载任务
 */
public class XtomImageTask extends XtomObject {
	protected ImageView imageView;// 图片显示控件
	protected URL url; // 图片网络url地址
	protected String path;// 图片本地地址
	protected Size mSize;// 图片尺寸
	protected Object context;// 使用场合
	protected Bitmap bitmap;// 图片位图
	protected View fatherView;// imageView的父View
	protected Object tag;// imageView的tag
	protected Object tempTag;// 临时tag
	protected EventHandler eventHandler;
	protected int tryTimes = 0;

	public static final int LOAD_SUCCESS = 1;
	public static final int LOAD_FAILED = -1;
	public static final int LOAD_ING = 0;
	public static final int LOAD_BEFORE = 2;

	public XtomImageTask(ImageView imageView, Object context) {
		this.imageView = imageView;
		this.context = context;

		Looper looper;
		if ((looper = Looper.myLooper()) != null) {
			eventHandler = new EventHandler(this, looper);
		} else if ((looper = Looper.getMainLooper()) != null) {
			eventHandler = new EventHandler(this, looper);
		} else {
			eventHandler = null;
		}
	}

	/**
	 * 图片下载任务
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, URL url, Object context) {
		this(imageView, context);
		this.url = url;
	}

	/**
	 * 图片任务(用于获取本地图片)
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, String path, Object context) {
		this(imageView, context);
		this.path = path;
	}

	/**
	 * 图片任务(用于获取本地图片)
	 * <p>
	 * <b>Note：调用此构造函数,在设置imageView的tag属性时(由于在图片加载过程中会设置临时的tag),最好调用
	 * {@link android.widget.ImageView#setTag(int, Object) setTag(int, Object)}
	 * 方法 </b>
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, String path, Object context,
			View fatherView) {
		this(imageView, path, context);
		this.fatherView = fatherView;
		setTempTag();
	}

	/**
	 * 图片下载任务
	 * <p>
	 * <b>Note：调用此构造函数,在设置imageView的tag属性时(由于在图片加载过程中会设置临时的tag),最好调用
	 * {@link android.widget.ImageView#setTag(int, Object) setTag(int, Object)}
	 * 方法 </b>
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, URL url, Object context,
			View fatherView) {
		this(imageView, url, context);
		this.fatherView = fatherView;
		setTempTag();
	}

	/**
	 * 图片任务(用于获取本地图片)
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, String path, Object context,
			Size size) {
		this(imageView, path, context);
		mSize = size;
	}

	/**
	 * 图片下载任务
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, URL url, Object context, Size size) {
		this(imageView, url, context);
		this.mSize = size;
	}

	/**
	 * 图片任务(用于获取本地图片)
	 * <p>
	 * <b>Note：调用此构造函数,在设置imageView的tag属性时(由于在图片加载过程中会设置临时的tag),最好调用
	 * {@link android.widget.ImageView#setTag(int, Object) setTag(int, Object)}
	 * 方法 </b>
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, String path, Object context,
			View fatherView, Size size) {
		this(imageView, path, context, fatherView);
		mSize = size;
	}

	/**
	 * 图片下载任务
	 * <p>
	 * <b>Note：调用此构造函数,在设置imageView的tag属性时(由于在图片加载过程中会设置临时的tag),最好调用
	 * {@link android.widget.ImageView#setTag(int, Object) setTag(int, Object)}
	 * 方法 </b>
	 * 
	 * @param context
	 *            使用场合
	 */
	public XtomImageTask(ImageView imageView, URL url, Object context,
			View fatherView, Size size) {
		this(imageView, url, context, fatherView);
		this.mSize = size;
	}

	/**
	 * 下载进度(可根据需求重写该方法)
	 * 
	 * @param percent
	 *            百分比
	 */
	public void onProgressUpdate(float percent) {

	}

	/**
	 * 下载成功(默认处理方式为:显示在imagView上。可根据需求重写该方法)
	 * 
	 * @param bitmap
	 */
	public void success() {
		setBitmapToImageView();
	}

	public void successInUIThread() {
		success();
		resetTag();
	}

	public ImageView getImageViewFromFather() {
		if (fatherView == null)
			return null;
		ImageView iv = (ImageView) fatherView.findViewWithTag(getTempTag());
		return iv;
	}

	public void setBitmapToImageView() {
		ImageView iv = getImageViewFromFather();
		if (iv != null) {
			iv.setImageBitmap(bitmap);
		} else {
			if (imageView != null)
				imageView.setImageBitmap(bitmap);
		}
	}

	/**
	 * 下载失败(可根据需求重写该方法)
	 * 
	 * @param bitmap
	 */
	public void failed() {
		String path = null == url ? this.path : url.toString();
		log_w("Get image " + path + " failed!!!");
		setBitmapToImageView();
	}

	/**
	 * 需要从服务器获取时调用(可根据需求重写该方法)
	 */
	public void beforeload() {
		if (imageView == null)
			return;
		imageView.setImageBitmap(null);
	}

	private void setTempTag() {
		if (imageView == null)
			return;
		tag = imageView.getTag();
		tempTag = getTemptag();
		imageView.setTag(tempTag);
	}

	private void resetTag() {
		ImageView iv = getImageViewFromFather();
		if (iv != null) {
			iv.setTag(tag);
		} else {
			if (imageView != null)
				imageView.setTag(tag);
		}
	}

	private Object getTemptag() {
		return new Object();
	}

	/**
	 * 获取图片显示控件
	 * 
	 * @return
	 */
	public ImageView getImageView() {
		return imageView;
	}

	/**
	 * 获取Handler
	 * 
	 * @return
	 */
	public Handler getHandler() {
		return eventHandler;
	}

	/**
	 * 设置图片显示控件
	 * 
	 * @param imageView
	 */
	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	/**
	 * 获取使用场合
	 * 
	 * @return
	 */
	public Object getContext() {
		return context;
	}

	/**
	 * 设置使用场合
	 * 
	 * @param context
	 */
	public void setContext(Object context) {
		this.context = context;
	}

	/**
	 * 获取图片网络url地址
	 * 
	 * @return
	 */
	public URL getUrl() {
		return url;
	}

	/**
	 * 获取位图
	 * 
	 * @return
	 */
	public Bitmap getBitmap() {
		return bitmap;
	}

	/**
	 * 设置位图
	 * 
	 * @param bitmap
	 */
	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	/**
	 * 获取imageView的TAG
	 * 
	 * @return
	 */
	public Object getTag() {
		return tag;
	}

	/**
	 * 获取imageView的父View
	 * 
	 * @return
	 */
	public View getFatherView() {
		return fatherView;
	}

	/**
	 * 获取临时Tag
	 * 
	 * @return
	 */
	public Object getTempTag() {
		return tempTag;
	}

	/**
	 * 获取图片本地地址
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * 获取需求的图片尺寸
	 * 
	 * @return
	 */
	public Size getSize() {
		return mSize;
	}

	public int getTryTimes() {
		return tryTimes;
	}

	public void setTryTimes(int tryTimes) {
		this.tryTimes = tryTimes;
	}

	/**
	 * 获取缓存名
	 * 
	 * @return
	 */
	public String getKeyForMemCache() {
		String key = url != null ? url.toString() : path;
		String size = "";
		if (mSize != null) {
			int width = mSize.getWidth();
			int height = mSize.getHeight();
			size = "(width=" + width + ",height=" + height + ")";
		}
		key += size;
		return key;
	}

	private static class EventHandler extends Handler {
		private XtomImageTask imageTask;

		public EventHandler(XtomImageTask imageTask, Looper looper) {
			super(looper);
			this.imageTask = imageTask;
		}

		public void handleMessage(Message message) {
			switch (message.what) {
			case LOAD_SUCCESS:
				imageTask.success();
				imageTask.resetTag();
				break;
			case LOAD_FAILED:
				imageTask.failed();
				imageTask.resetTag();
				break;
			case LOAD_BEFORE:
				imageTask.beforeload();
				break;
			case LOAD_ING:
				Float percent = (Float) message.obj;
				imageTask.onProgressUpdate(percent);
				break;
			}
		}
	}

	/**
	 * 图片尺寸
	 */
	public static class Size {
		private int width;// 宽
		private int height;// 高

		/**
		 * 实例化一个图片尺寸
		 * 
		 * @param width
		 *            宽
		 * @param height
		 *            高
		 */
		public Size(int width, int height) {
			this.width = width;
			this.height = height;
		}

		/**
		 * 宽
		 * 
		 * @return
		 */
		public int getWidth() {
			return width;
		}

		/**
		 * 高
		 * 
		 * @return
		 */
		public int getHeight() {
			return height;
		}
	}

}

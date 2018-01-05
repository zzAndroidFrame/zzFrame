package xtom.frame.image.load;

import java.util.ArrayList;

import xtom.frame.XtomConfig;
import xtom.frame.XtomObject;
import xtom.frame.image.cache.XtomImageCache;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Message;

/**
 * 图片加载器
 */
public class XtomImageWorker extends XtomObject {
	private Context mContext;
	private ImageThread imthread = null;
	private XtomImageCache imageCache;
	private boolean isThreadControlable = false;// 子线程是否可控
	private ArrayList<XtomImageTask> threadTasks;

	/**
	 * 实例化
	 * 
	 * @param context
	 */
	public XtomImageWorker(Context context) {
		this.mContext = context.getApplicationContext();
		this.imageCache = XtomImageCache.getInstance(mContext);
	}

	/**
	 * 清除图片下载任务
	 */
	public void clearTasks() {
		clearThreadTasks();
		if (imthread != null)
			imthread.cancelTasks();
	}

	/**
	 * 获取图片
	 * 
	 * @param task
	 * @return 该任务是否需要异步执行
	 */
	public boolean loadImage(XtomImageTask task) {
		log_d("Get image:---" + task.getKeyForMemCache() + "---From Mem. ");
		// 若内存中有,直接在UI线程中从内存取出之
		Bitmap bitmap = imageCache.getFromMemCache(task);
		if (bitmap != null) {
			log_d("Mem has.");
			task.setBitmap(bitmap);
			task.successInUIThread();
			return false;
		} else {
			log_d("Mem not has. do it in childThread.");
			// 若内存中没有,交由子线程处理
			task.beforeload();
			if (!isThreadControlable)
				loadImageByThread(task);
			else
				addThreadTask(task);
			return true;
		}
	}

	// 添加进异步线程任务队列,等待执行
	private void addThreadTask(XtomImageTask task) {
		if (threadTasks == null)
			threadTasks = new ArrayList<XtomImageTask>();
		threadTasks.add(task);
	}

	/**
	 * 异步执行这些任务
	 * 
	 * @param threadTasks
	 */
	public void excuteThreadTasks(ArrayList<XtomImageTask> threadTasks) {
		if (threadTasks == null)
			return;
		for (XtomImageTask task : threadTasks) {
			loadImageByThread(task);
		}
		threadTasks.clear();
	}

	/**
	 * 执行异步任务
	 */
	public void excuteThreadTasks() {
		if (threadTasks == null)
			return;
		for (XtomImageTask task : threadTasks) {
			loadImageByThread(task);
		}
		clearThreadTasks();
	}

	/**
	 * 清空异步任务
	 */
	public void clearThreadTasks() {
		if (threadTasks != null)
			threadTasks.clear();
	}

	/**
	 * 异步获取图片
	 * 
	 * @param task
	 */
	public void loadImageByThread(XtomImageTask task) {
		synchronized (this) {
			if (imthread == null) {
				imthread = new ImageThread(task);
				imthread.start();
				log_d("图片线程不存在或已执行完毕,开启新线程：" + imthread.getName());
			} else {
				log_d(imthread.getName() + "执行中,添加图片任务");
				imthread.addTask(task);
			}
		}
	}

	/**
	 * 异步线程可控性
	 * 
	 * @return 若为true则异步任务不会自动执行,需调用excuteThreadTasks()
	 */
	public boolean isThreadControlable() {
		return isThreadControlable;
	}

	/**
	 * 异步线程可控性
	 * 
	 * @param isThreadControlable
	 *            若为true则异步任务不会自动执行,需调用excuteThreadTasks()
	 */
	public void setThreadControlable(boolean isThreadControlable) {
		this.isThreadControlable = isThreadControlable;
	}

	private class ImageThread extends Thread {
		private ArrayList<XtomImageTask> tasks = new ArrayList<XtomImageTask>();

		private boolean isRun = true;

		public ImageThread(XtomImageTask task) {
			super();
			addTask(task);
			setName("图片线程(" + getName() + ")");
		}

		public void addTask(XtomImageTask task) {
			synchronized (XtomImageWorker.this) {
				tasks.add(task);
			}
		}

		public void cancelTasks() {
			synchronized (XtomImageWorker.this) {
				tasks.clear();
				imthread = null;
				isRun = false;
			}
		}

		boolean isHaveTask() {
			return tasks.size() > 0;
		}

		@Override
		public void run() {
			log_d(getName() + "开始执行");
			while (isRun) {
				synchronized (XtomImageWorker.this) {
					if (!isHaveTask()) {
						isRun = false;
						imthread = null;
						break;
					}
				}
				if (tasks.size() > 0) {
					XtomImageTask task = tasks.get(0);
					load(task);
				}
			}
			log_d(getName() + "执行完毕");
		}

		private boolean load(XtomImageTask task) {
			if (task == null)
				return false;
			log_d("Get image:---" + task.getKeyForMemCache()
					+ "---From Server. Try " + (task.getTryTimes() + 1));
			Bitmap bitmap;
			bitmap = imageCache.getBitmapFromServer(task);
			if (bitmap != null) {
				success(bitmap, task);
				return true;
			} else {
				tryAgain(task);
			}
			return false;
		}

		private void success(Bitmap bitmap, XtomImageTask task) {
			task.setBitmap(bitmap);
			Message message = task.getHandler().obtainMessage(
					XtomImageTask.LOAD_SUCCESS, task);
			tasks.remove(task);
			task.getHandler().sendMessage(message);
		}

		private void tryAgain(XtomImageTask task) {
			task.setTryTimes(task.getTryTimes() + 1);
			if (task.getTryTimes() >= XtomConfig.TRYTIMES_IMAGE) {
				Message message = task.getHandler().obtainMessage(
						XtomImageTask.LOAD_FAILED, task);
				tasks.remove(task);
				task.getHandler().sendMessage(message);
			}
		}
	}

	public interface OnTaskExecuteListener {
		/**
		 * Runs on the UI thread before the task run.
		 */
		public void onPreExecute(XtomImageTask task);

		/**
		 * Runs on the UI thread after the task run.
		 */
		public void onPostExecute(XtomImageTask task);

		/**
		 * Runs on the UI thread when the task run success.
		 * 
		 * @param result
		 *            the result of the server back.
		 */
		public void onExecuteSuccess(XtomImageTask task);

		/**
		 * Runs on the UI thread when the task run failed.
		 */
		public void onExecuteFailed(XtomImageTask task);
	}
}
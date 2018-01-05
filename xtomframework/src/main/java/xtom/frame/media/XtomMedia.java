package xtom.frame.media;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import xtom.frame.XtomObject;
import xtom.frame.util.XtomFileUtil;
import xtom.frame.util.XtomToastUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * 语音文件播放器(若为网络文件会先下载后播放)
 * 
 * @deprecated 推荐使用XtomVoicePlayer
 */
public class XtomMedia extends XtomObject {
	private String cachePath_external;// 外部缓存路径
	private String cachePath_internal;// 内部缓存路径
	private Context context;
	private String path;
	private String localPath;
	private MediaPlayer mPlayer;
	private XtomMediaListener mediaListener;
	private XtomLoadListener loadListener;
	private int type;
	private TimeThread timeThread;
	private boolean isPlayableAfterDownload = true;// 下载完成时是否自动播放

	/**
	 * 获取一个播放器实例(删除音频缓存时可用)
	 * 
	 * @param context
	 * @return
	 */
	public static XtomMedia get(Context context) {
		return new XtomMedia(context);
	}

	private XtomMedia(Context context) {
		this.context = context;
		cachePath_internal = context.getCacheDir().getPath() + "/meidafiles/";
		File file = context.getExternalCacheDir();
		cachePath_external = (file != null) ? file.getPath() + "/meidafiles/"
				: null;
	}

	/**
	 * 下载完成时是否自动播放
	 * 
	 * @return
	 */
	public boolean isPlayableAfterDownload() {
		return isPlayableAfterDownload;
	}

	/**
	 * 设置下载完成时是否自动播放
	 * 
	 * @param isPlayableAfterDownload
	 */
	public void setPlayableAfterDownload(boolean isPlayableAfterDownload) {
		this.isPlayableAfterDownload = isPlayableAfterDownload;
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

	/**
	 * 语音文件播放器(若为网络文件会先下载后播放)
	 * 
	 * @param context
	 *            上下文环境
	 * @param path
	 *            语音文件地址
	 * @param type
	 *            语音文件类型(1本地文件2网络文件)
	 */
	public XtomMedia(Context context, String path, int type) {
		this(context);
		if (isNull(path))
			throw new IllegalStateException("语音文件地址不能为空");

		this.path = path;
		this.type = type;
		if (type == 1)
			localPath = path;
		else {
			if (XtomFileUtil.isExternalMemoryAvailable())
				localPath = cachePath_external
						+ XtomFileUtil.getKeyForCache(path);
			else
				localPath = cachePath_internal
						+ XtomFileUtil.getKeyForCache(path);
		}
	}

	public void seekTo(int seek) {
		int msec = mPlayer.getDuration() * seek / 100;
		mPlayer.seekTo(msec);
	}

	/**
	 * 开始播放
	 */
	public void start() {
		if (type == 2) {
			if (isLoading) {
				XtomToastUtil.showShortToast(context, "正在等待播放,请稍后。");
				return;
			}
			File file = new File(localPath);
			if (!file.exists()) {
				load();
				XtomToastUtil.showShortToast(context, "正在等待播放,请稍后。");
				return;
			}
		}
		try {
			if (mPlayer == null) {
				mPlayer = new MediaPlayer();
				mPlayer.setOnCompletionListener(new OnCompletionListener() {

					@Override
					public void onCompletion(MediaPlayer mp) {
						mPlayer.release();
						mPlayer = null;
						if (timeThread != null)
							timeThread.cancel();
						if (mediaListener != null)
							mediaListener.onComplete();
					}
				});
				mPlayer.setDataSource(localPath);
				mPlayer.prepare();
			}
			mPlayer.start();
			if (mediaListener != null)
				mediaListener.onStart();
			timeThread = new TimeThread();
			timeThread.start();
		} catch (Exception e) {
			XtomToastUtil.showShortToast(context, "文件损坏,播放失败.");
			log_i("播放失败");
			if (type == 2) {
				log_i("该语音文件为网络文件,删除，重新下载");
				File file = new File(localPath);
				if (file.exists())
					file.delete();
				load();
			}
		}
	}

	/**
	 * 停止播放
	 */
	public void pause() {
		if (timeThread != null)
			timeThread.cancel();
		if (mPlayer != null) {
			mPlayer.pause();
		}
		if (mediaListener != null)
			mediaListener.onPause();
	}

	/**
	 * 停止播放
	 */
	public void stop() {
		if (timeThread != null)
			timeThread.cancel();
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
		if (mediaListener != null)
			mediaListener.onStop();
	}

	/**
	 * 设置播放监听
	 * 
	 * @param listener
	 */
	public void setXtomMediaListener(XtomMediaListener listener) {
		this.mediaListener = listener;
	}

	/**
	 * 设置下载监听
	 * 
	 * @param listener
	 */
	public void setXtomLoadListener(XtomLoadListener listener) {
		this.loadListener = listener;
	}

	/**
	 * 是否正在播放
	 * 
	 * @return
	 */
	public boolean isPlaying() {
		return mPlayer == null ? false : mPlayer.isPlaying();
	}

	private boolean isLoading = false;

	private void load() {
		new AsyncTask<Object, Object, Object>() {
			private boolean isSuccess = false;

			@Override
			protected Object doInBackground(Object... params) {
				InputStream is = null;
				FileOutputStream fos = null;
				File file = null;
				try {
					URL url = new URL(path);
					URLConnection conn = url.openConnection();
					is = (url.getHost().equals("")) ? null : conn
							.getInputStream();
					int length = conn.getContentLength();
					File dir;
					String strdir;
					if (XtomFileUtil.isExternalMemoryAvailable())
						strdir = cachePath_external;
					else
						strdir = cachePath_internal;
					dir = new File(strdir);
					if (!dir.exists())
						dir.mkdirs();
					file = new File(strdir + XtomFileUtil.getKeyForCache(path));
					fos = new FileOutputStream(file);
					byte[] buffer = new byte[1204];
					int len = 0;
					int downlen = 0;
					while ((len = is.read(buffer)) != -1) {
						downlen += len;
						fos.write(buffer, 0, len);
						float percent = (float) downlen / (float) length;
						if (loadListener != null)
							loadListener.onloading(percent);
					}
					isSuccess = true;
					log_i("loading meidafile === " + path + " === success");
					return file.getPath();
				} catch (Exception e) {
					e.printStackTrace();
					log_i("loading meidafile === " + path + " === failed");
					if (file != null && file.exists())
						file.delete();
					log_i("文件下载失败，删除之");
					return null;
				} finally {
					try {
						if (is != null)
							is.close();
						if (fos != null)
							fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			protected void onPostExecute(Object result) {
				isLoading = false;
				if (loadListener != null)
					loadListener.onFinish();
				if (isSuccess) {
					XtomToastUtil.showShortToast(context, "开始播放");
					if (isPlayableAfterDownload)
						start();
				} else {
					XtomToastUtil.showShortToast(context, "播放失败");
				}
				super.onPostExecute(result);
			}

			@Override
			protected void onPreExecute() {
				log_i("start loading meidafile === " + path);
				if (loadListener != null)
					loadListener.onStart();
				isLoading = true;
				super.onPreExecute();
			}

		}.execute();
	}

	private class TimeThread extends Thread {
		private boolean isRun = true;
		@SuppressLint("HandlerLeak")
		private Handler handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (mediaListener != null)
					mediaListener.onPlaying(msg.arg1, mPlayer == null ? 0
							: mPlayer.getDuration());
			}

		};

		private TimeThread() {

		}

		@Override
		public void run() {
			while (isRun) {
				try {
					Message message = handler.obtainMessage();
					message.what = 1;
					message.arg1 = mPlayer.getCurrentPosition();
					handler.sendMessage(message);
					Thread.sleep(1000);
					if (!isRun)
						break;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void cancel() {
			isRun = false;
		}
	}

	/**
	 * 删除缓存
	 */
	public void deleteCache() {
		int s = deleteCache(cachePath_external);
		int i = deleteCache(cachePath_internal);
		log_d("delete " + (s + i) + " mediafiles");
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
	 * 播放状态监听
	 */
	public interface XtomMediaListener {
		/**
		 * 开始播放
		 */
		public void onStart();

		/**
		 * 暂停播放
		 */
		public void onPause();

		/**
		 * 正在播放，秒
		 */
		/**
		 * 
		 * @param current
		 *            毫秒
		 * @param all
		 *            毫秒
		 */
		public void onPlaying(int current, int all);

		/**
		 * 播放停止
		 */
		public void onStop();

		/**
		 * 播放完成
		 */
		public void onComplete();
	}

	/**
	 * 下载状态监听
	 */
	public interface XtomLoadListener {
		/**
		 * 开始下载(可直接操作UI)
		 */
		public void onStart();

		/**
		 * 正在下载(可直接操作UI)
		 * 
		 * @param percent
		 *            下载进度百分比
		 */
		public void onloading(float percent);

		/**
		 * 下载完成(可直接操作UI)
		 */
		public void onFinish();
	}

}
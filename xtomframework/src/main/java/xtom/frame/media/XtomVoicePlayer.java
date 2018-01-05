package xtom.frame.media;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import xtom.frame.XtomObject;
import xtom.frame.fileload.XtomFileDownLoader;
import xtom.frame.fileload.XtomFileDownLoader.XtomDownLoadListener;
import xtom.frame.util.XtomFileUtil;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * 语音播放器
 */
public class XtomVoicePlayer extends XtomObject {
	private static final int PLAYING = 0;

	private EventHandler eventHandler;
	private String cachePath_external;// 外部缓存路径
	private String cachePath_internal;// 内部缓存路径

	private String path;
	private String localPath;
	private VoicePlayer mPlayer;

	TimeThread timeThread;
	private XtomFileDownLoader downLoader;

	private XtomVoicePlayer(Context context) {
		Looper looper;
		if ((looper = Looper.myLooper()) != null) {
			eventHandler = new EventHandler(this, looper);
		} else if ((looper = Looper.getMainLooper()) != null) {
			eventHandler = new EventHandler(this, looper);
		} else {
			eventHandler = null;
		}

		cachePath_internal = context.getCacheDir().getPath() + "/meidafiles/";
		File file = context.getExternalCacheDir();
		cachePath_external = (file != null) ? file.getPath() + "/meidafiles/"
				: null;
	}

	/**
	 * 获取一个实例，仅供清除缓存时使用
	 * 
	 * @param context
	 * @return
	 */
	public static XtomVoicePlayer getInstance(Context context) {
		return new XtomVoicePlayer(context);
	}

	/**
	 * 语音播放器
	 * 
	 * @param context
	 *            上下文环境
	 * @param path
	 *            语音文件地址
	 */
	public XtomVoicePlayer(Context context, String path) {
		this(context);
		if (isNull(path))
			throw new IllegalStateException("语音文件地址不能为空");
		this.path = path;

		String host;
		try {
			URL url = new URL(path);
			host = url.getHost();
		} catch (MalformedURLException e) {
			host = null;
		}

		if (isNull(host)) {
			localPath = path;
			log_d("本地音频文件");
		} else {
			log_d("网络音频文件");
			String key = XtomFileUtil.getKeyForCache(path);
			if (XtomFileUtil.isExternalMemoryAvailable())
				localPath = cachePath_external + key;
			else
				localPath = cachePath_internal + key;
			downLoader = new XtomFileDownLoader(context, path, localPath);
			downLoader.setXtomDownLoadListener(new DownLoadListener());
		}
	}

	/**
	 * 开始缓冲
	 */
	public void startLoad() {
		if (downLoader != null)
			downLoader.start();
	}

	/**
	 * 停止缓冲
	 */
	public void stopLoad() {
		if (downLoader != null)
			downLoader.stop();
	}

	/**
	 * @return 获取语音文件本地缓存地址
	 */

	public String getLocalPath() {
		return localPath;
	}

	/**
	 * @return 获取语音文件地址
	 */
	public String getPath() {
		return path;
	}

	// 是否缓冲完成
	private boolean isLoaded() {
		return downLoader == null ? true : downLoader.isFileLoaded();
	}

	/**
	 * 开始播放
	 * <p>
	 * <b>Note:调用过该方法之后,请在适当时机调用{@link #release()}方法</b>
	 */
	public void start() {
		if (mPlayer == null)
			mPlayer = new VoicePlayer(this);

		// 正在播放的话,返回
		if (mPlayer.isPlaying())
			return;
		// 需要缓冲的话,先缓冲
		if (!isLoaded()) {
			downLoader.start();
			return;
		}

		try {
			if (!mPlayer.isPrepared()) {
				mPlayer.setDataSource(localPath);
				mPlayer.prepare();
			}
			mPlayer.start();
			if (xtomVoicePlayListener != null)
				xtomVoicePlayListener.onStart(this);
			timeThread = new TimeThread();
			timeThread.start();
		} catch (Exception e) {
			if (xtomVoicePlayListener != null)
				xtomVoicePlayListener.onError(this);
		}
	}

	/**
	 * 暂停播放
	 */
	public void pause() {
		cancelTimeThread();
		if (mPlayer != null)
			mPlayer.pause();
		if (xtomVoicePlayListener != null)
			xtomVoicePlayListener.onPause(this);
	}

	void cancelTimeThread() {
		if (timeThread != null)
			timeThread.cancel();
	}

	/**
	 * 停止播放
	 */
	public void stop() {
		cancelTimeThread();
		if (mPlayer != null)
			mPlayer.stop();
		if (xtomVoicePlayListener != null)
			xtomVoicePlayListener.onStop(this);
	}

	/**
	 * 
	 * @return 语音文件时长(毫秒)
	 */
	public int getDuration() {
		return mPlayer == null ? 0 : mPlayer.getDuration();
	}

	/**
	 * @return 语音文件当前播放时长(毫秒)
	 */
	public int getCurrentPosition() {
		return mPlayer == null ? 0 : mPlayer.getCurrentPosition();
	}

	/**
	 * 释放播放器资源
	 */
	public void release() {
		stop();
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
	}

	/**
	 * 是否正在播放
	 * 
	 * @return
	 */
	public boolean isPlaying() {
		return mPlayer == null ? false : mPlayer.isPlaying();
	}

	/**
	 * 跳至某一位置
	 * 
	 * @param seek
	 *            (0--100)
	 */
	public void seekTo(int seek) {
		if (seek < 0)
			seek = 0;
		if (seek > 100)
			seek = 100;
		int msec = mPlayer.getDuration() * seek / 100;
		mPlayer.seekTo(msec);
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

	private XtomVoicePlayListener xtomVoicePlayListener;

	/**
	 * @return 播放状态监听
	 */
	public XtomVoicePlayListener getXtomVoicePlayListener() {
		return xtomVoicePlayListener;
	}

	/**
	 * 设置播放状态监听
	 * 
	 * @param xtomVoicePlayListener
	 */
	public void setXtomVoicePlayListener(
			XtomVoicePlayListener xtomVoicePlayListener) {
		this.xtomVoicePlayListener = xtomVoicePlayListener;
	}

	/**
	 * 播放状态监听
	 */
	public interface XtomVoicePlayListener {
		/**
		 * 开始加载
		 */
		public void loadStart(XtomVoicePlayer player, XtomFileDownLoader loader);

		/**
		 * 正在加载
		 */
		public void loading(XtomVoicePlayer player, XtomFileDownLoader loader);

		/**
		 * 加载成功
		 */
		public void loadSuccess(XtomVoicePlayer player,
				XtomFileDownLoader loader);

		/**
		 * 加载失败
		 */
		public void loadFailed(XtomVoicePlayer player, XtomFileDownLoader loader);

		/**
		 * 开始播放
		 */
		public void onStart(XtomVoicePlayer player);

		/**
		 * 暂停播放
		 */
		public void onPause(XtomVoicePlayer player);

		/**
		 * 正在播放
		 */
		public void onPlaying(XtomVoicePlayer player);

		/**
		 * 播放停止
		 */
		public void onStop(XtomVoicePlayer player);

		/**
		 * 播放完成
		 */
		public void onComplete(XtomVoicePlayer player);

		/**
		 * 播放错误
		 */
		public void onError(XtomVoicePlayer player);

	}

	// 获取缓存大小
	private long getCacheSize(String cache_path) {
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
	 * 获取缓存大小byte
	 */
	public long getCacheSize() {
		return getCacheSize(cachePath_external)
				+ getCacheSize(cachePath_internal);
	}

	private class TimeThread extends Thread {
		private boolean isRun = true;

		@Override
		public void run() {
			while (isRun) {
				try {
					eventHandler.sendEmptyMessage(PLAYING);
					sleep(1000);
				} catch (Exception e) {
					// ignore
				}
			}
		}

		private void cancel() {
			isRun = false;
		}
	}

	private static class EventHandler extends Handler {
		private XtomVoicePlayer player;

		public EventHandler(XtomVoicePlayer player, Looper looper) {
			super(looper);
			this.player = player;
		}

		@Override
		public void handleMessage(Message msg) {
			XtomVoicePlayListener listener = player.getXtomVoicePlayListener();
			if (listener != null)
				switch (msg.what) {
				case PLAYING:
					listener.onPlaying(player);
					break;
				}
		}
	}

	private class DownLoadListener implements XtomDownLoadListener {

		@Override
		public void onStart(XtomFileDownLoader loader) {
			if (xtomVoicePlayListener != null)
				xtomVoicePlayListener.loadStart(XtomVoicePlayer.this,
						downLoader);
		}

		@Override
		public void onSuccess(XtomFileDownLoader loader) {
			if (xtomVoicePlayListener != null)
				xtomVoicePlayListener.loadSuccess(XtomVoicePlayer.this,
						downLoader);
		}

		@Override
		public void onFailed(XtomFileDownLoader loader) {
			if (xtomVoicePlayListener != null)
				xtomVoicePlayListener.loadFailed(XtomVoicePlayer.this,
						downLoader);
		}

		@Override
		public void onLoading(XtomFileDownLoader loader) {
			if (xtomVoicePlayListener != null)
				xtomVoicePlayListener.loading(XtomVoicePlayer.this, downLoader);
		}

		@Override
		public void onStop(XtomFileDownLoader loader) {
			log_i("停止缓冲");
		}
	}
}
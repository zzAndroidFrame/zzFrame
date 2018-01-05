package xtom.frame;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import xtom.frame.util.XtomFileUtil;
import xtom.frame.util.XtomToastUtil;
import android.annotation.SuppressLint;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

/**
 * 软件升级
 */
public class XtomUpGrade extends XtomObject {
	private String appName;
	private String url;
	private Context mContext;
	private ProgressDialog pBar;
	private int downstate = 0;
	private Builder ab;
	private String saveappdir;

	/**
	 * 实例化
	 * 
	 * @param mContext
	 *            上下文环境
	 * @param appName
	 *            app名称
	 * @param url
	 *            app网络地址(注意：改地址不包含appName)
	 */
	public XtomUpGrade(Context mContext, String appName, String url) {
		this.mContext = mContext;
		this.appName = appName;
		this.url = url;
	}

	/**
	 * 显示升级对话框
	 */
	public void alert() {
		if (ab == null) {
			ab = new Builder(mContext);
			ab.setTitle("软件更新");
			ab.setMessage("有最新的软件版本，是否升级？");
			ab.setPositiveButton("升级", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					pBar = new ProgressDialog(mContext) {
						@Override
						public void onBackPressed() {
							super.onBackPressed();
							if (downstate == 1) {
								downstate = 2;
							}
						}
					};
					pBar.setTitle("正在下载");
					pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					downFile(url + appName);
					log_i("URL+APPNAME=" + url + appName);
				}
			});
			ab.setNegativeButton("取消", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		}
		ab.show();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		// 处理消息handleMessage
		@Override
		public void handleMessage(Message msg) {
			if (pBar != null) {
				int max = msg.arg1;
				// 设置进度条最大值
				pBar.setMax(100);
				if (msg.what == 0) {
					// 下载进行中，更新进度条
					int nowSize = msg.arg2;
					nowSize = nowSize * 100 / max;
					pBar.setProgress(nowSize);
					// Log.e("down", "正在下载。。。。" + (nowSize));
				} else if (msg.what == 1) {
					update();
				} else if (msg.what == 2) {
					showerr();
				} else if (msg.what == 3) {
					showcancel();
				}
			}
			super.handleMessage(msg);
		}

	};

	void update() {
		pBar.cancel();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(saveappdir, appName)),
				"application/vnd.android.package-archive");
		mContext.startActivity(intent);
	}

	void showerr() {
		pBar.cancel();
		XtomToastUtil.showShortToast(mContext, "下载出错");
	}

	void showcancel() {
		File file = new File(saveappdir, appName);
		// 如果文件存在，则删除该文件。
		if (file.exists())
			file.delete();
		XtomToastUtil.showShortToast(mContext, "更新已取消");
	}

	void downFile(final String url) {
		pBar.show();
		new Thread() {
			public void run() {
				InputStream is = null;
				RandomAccessFile randomAccessFile = null;
				try {
					downstate = 1;
					// 创建、打开连接
					URL myUrl = new URL(url);
					URLConnection connection = (URLConnection) myUrl
							.openConnection();
					connection.setConnectTimeout(8000);
					connection.connect();
					if (downstate == 2) {
						downstate = 0;
						Message err = new Message();
						err.what = 3;
						mHandler.sendMessage(err);
						return;
					}

					// 得到访问内容并保存在输入流中。
					is = connection.getInputStream();
					// 得到文件的总长度。注意这里有可能因得不到文件大小而抛出异常
					int len = connection.getContentLength();
					if (is != null) {
						saveappdir = XtomFileUtil.getTempFileDir(mContext);
						File dir = new File(saveappdir);
						if (!dir.exists())
							dir.mkdir();
						File file = new File(saveappdir, appName);
						// 如果文件存在，则删除该文件。
						if (file.exists())
							file.delete();
						// RandomAccessFile随机访问的文件类，可以从指定访问位置，为以后实现断点下载提供支持
						randomAccessFile = new RandomAccessFile(file, "rwd");
						byte[] buffer = new byte[4096];
						int length = -1;
						int downlength = 0;
						while ((length = is.read(buffer)) != -1) {
							randomAccessFile.write(buffer, 0, length);
							Message msg = new Message();
							msg.arg1 = len;// 将文件大小保存
							// 用what变量来标示当前的状态
							msg.what = 0;
							// arg2标示本次循环完成的进度
							downlength += length;
							msg.arg2 = downlength;
							mHandler.sendMessage(msg);
							if (downstate == 2) {
								downstate = 0;
								is.close();
								randomAccessFile.close();
								Message err = new Message();
								err.what = 3;
								mHandler.sendMessage(err);
								return;
							}
						}
						// 结束以后，标记为结束状态。
						Message end = new Message();
						end.what = 1;
						mHandler.sendMessage(end);
					}
				} catch (Exception e) {
					Message err = new Message();
					err.what = 2;
					mHandler.sendMessage(err);
				}
				try {
					if (is != null) {
						is.close();
					}
					if (randomAccessFile != null) {
						randomAccessFile.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}

package xtom.frame.test;

import java.io.File;

import xtom.frame.R;
import xtom.frame.fileload.FileInfo;
import xtom.frame.fileload.XtomFileDownLoader;
import xtom.frame.fileload.XtomFileDownLoader.XtomDownLoadListener;
import xtom.frame.util.XtomFileUtil;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DownLoadActivity extends Activity {
	Button button;
	Button pause;
	Button delete;
	TextView textView;
	XtomFileDownLoader downLoader;

	String downPath = "http://www.mmzzb.com/download/mmzzb.apk";
	String savePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_download);
		super.onCreate(savedInstanceState);
		button = (Button) findViewById(R.id.button);
		pause = (Button) findViewById(R.id.pause);
		delete = (Button) findViewById(R.id.delete);
		textView = (TextView) findViewById(R.id.textview);
		savePath = XtomFileUtil.getTempFileDir(this) + "333.apk";

		downLoader = new XtomFileDownLoader(getApplicationContext(), downPath,
				savePath);
		downLoader.setThreadCount(8);

		downLoader.setXtomDownLoadListener(new XtomDownLoadListener() {

			@Override
			public void onSuccess(XtomFileDownLoader loader) {
				String d = loader.getDownPath();
				String s = loader.getSavePath();
				String str = "下载成功" + d + "\n 保存路径" + s;
				textView.setText(str);
				System.out.println("onSuccess");
			}

			@Override
			public void onStart(XtomFileDownLoader loader) {
				String d = loader.getDownPath();
				String s = loader.getSavePath();
				String str = "开始下载" + d + "\n 保存路径" + s;
				textView.setText(str);
				System.out.println("onStart");
			}

			@Override
			public void onLoading(XtomFileDownLoader loader) {
				FileInfo i = loader.getFileInfo();
				String d = loader.getDownPath();
				String s = loader.getSavePath();
				int a = i.getContentLength();
				int c = i.getCurrentLength();

				String str = "正在下载" + d + "\n 保存路径" + s + "\n 下载进度" + c + "/"
						+ a;
				textView.setText(str);
				System.out.println("onLoading a=" + a + " c=" + c);

			}

			@Override
			public void onFailed(XtomFileDownLoader loader) {
				String d = loader.getDownPath();
				String s = loader.getSavePath();
				String str = "下载失败" + d + "\n 保存路径" + s;
				textView.setText(str);
				System.out.println("onFailed");
			}

			@Override
			public void onStop(XtomFileDownLoader loader) {
				FileInfo i = loader.getFileInfo();
				String d = loader.getDownPath();
				String s = loader.getSavePath();
				int a = i.getContentLength();
				int c = i.getCurrentLength();

				String str = "下载暂停" + d + "\n 保存路径" + s + "\n 下载进度" + c + "/"
						+ a;
				textView.setText(str);
				System.out.println("onStop a=" + a + " c=" + c);

			}

		});

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				downLoader.start();
			}
		});
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				downLoader.stop();
			}
		});
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file = new File(savePath);
				file.delete();
			}
		});
	}

}

package xtom.frame.test;

import java.io.File;

import xtom.frame.R;
import xtom.frame.fileload.XtomFileDownLoader;
import xtom.frame.media.XtomVoicePlayer;
import xtom.frame.media.XtomVoicePlayer.XtomVoicePlayListener;
import xtom.frame.util.XtomToastUtil;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class VoicePlayerActivity extends Activity {
	Button start;
	Button pause;
	Button stop;
	Button startLoad;
	Button stopLoad;
	Button delete;
	TextView textView;

	XtomVoicePlayer player;
	String voicePath = "http://124.128.23.75:8008/yyzj/2.5.0/webservice/uploads/where_time.mp3";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_voiceplayer);
		super.onCreate(savedInstanceState);
		start = (Button) findViewById(R.id.start);
		pause = (Button) findViewById(R.id.pause);
		stop = (Button) findViewById(R.id.stop);
		startLoad = (Button) findViewById(R.id.startLoad);
		stopLoad = (Button) findViewById(R.id.stopLoad);
		delete = (Button) findViewById(R.id.delete);
		textView = (TextView) findViewById(R.id.textview);

		player = new XtomVoicePlayer(getApplicationContext(), voicePath);
		player.setXtomVoicePlayListener(new VoicePlayListener());

		start.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player.start();
			}
		});
		pause.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player.pause();
			}
		});
		stop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player.stop();
			}
		});
		startLoad.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player.startLoad();
			}
		});
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				File file = new File(player.getLocalPath());
				file.delete();
			}
		});
		stopLoad.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				player.stopLoad();
			}
		});
	}

	@Override
	protected void onDestroy() {
		player.release();// 在恰当时机释放播放器资源（此处选择页面销毁时）
		super.onDestroy();
	}

	private class VoicePlayListener implements XtomVoicePlayListener {

		@Override
		public void onStop(XtomVoicePlayer player) {
			System.out.println("onStop");
		}

		@Override
		public void onStart(XtomVoicePlayer player) {
			System.out.println("onStart");
		}

		@Override
		public void onPlaying(XtomVoicePlayer player) {
			int d = player.getDuration();
			int c = player.getCurrentPosition();

			System.out.println("onPlaying " + c + "/" + d);
		}

		@Override
		public void onPause(XtomVoicePlayer player) {
			System.out.println("onPause");
		}

		@Override
		public void onError(XtomVoicePlayer player) {
			System.out.println("onError");
		}

		@Override
		public void onComplete(XtomVoicePlayer player) {
			System.out.println("onComplete");
		}

		@Override
		public void loadStart(XtomVoicePlayer player, XtomFileDownLoader loader) {
			System.out.println("开始缓冲");
		}

		@Override
		public void loading(XtomVoicePlayer player, XtomFileDownLoader loader) {
			int c = loader.getFileInfo().getCurrentLength();
			int a = loader.getFileInfo().getContentLength();

			int p = (int) ((float) c / (float) a * 100);
			String string = "正在缓冲(" + p + "%)";
			System.out.println(string);
			XtomToastUtil.showShortToast(VoicePlayerActivity.this, string);
		}

		@Override
		public void loadSuccess(XtomVoicePlayer player,
				XtomFileDownLoader loader) {
			System.out.println("缓冲完成   开始播放");
			player.start();
		}

		@Override
		public void loadFailed(XtomVoicePlayer player, XtomFileDownLoader loader) {
			System.out.println("缓冲失败");
		}
	}
}

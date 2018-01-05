package xtom.frame.test;

import xtom.frame.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class MainActivity extends Activity {
	private String[] strings = { "下拉刷新上拉加载布局", "文件下载", "语音播放器", "侧边栏布局" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_main);
		super.onCreate(savedInstanceState);

		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(new Adapter(this, strings));
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent it = null;
				switch (position) {
				case 0:
					it = new Intent(MainActivity.this,
							RefreshAndLoadmoreActivity.class);
					break;
				case 1:
					it = new Intent(MainActivity.this, DownLoadActivity.class);
					break;
				case 2:
					it = new Intent(MainActivity.this,
							VoicePlayerActivity.class);
					break;
				case 3:
					it = new Intent(MainActivity.this,
							SlidingPaneActivity.class);
					break;
				}
				startActivity(it);
			}
		});
	}

}

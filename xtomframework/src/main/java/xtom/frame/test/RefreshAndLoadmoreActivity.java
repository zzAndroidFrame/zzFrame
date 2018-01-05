package xtom.frame.test;

import xtom.frame.R;
import xtom.frame.view.XtomRefreshLoadmoreLayout;
import xtom.frame.view.XtomRefreshLoadmoreLayout.OnStartListener;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class RefreshAndLoadmoreActivity extends Activity {
	XtomRefreshLoadmoreLayout rl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_refreshandmore);
		super.onCreate(savedInstanceState);
		rl = (XtomRefreshLoadmoreLayout) findViewById(R.id.swipe_container);
		// rl.setLoadmoreView(R.layout.loadmore);
		// rl.setRefreshView(R.layout.refresh);

		ListView listView = (ListView) findViewById(R.id.listview);
		listView.setAdapter(new RefreshAndLoadmoreAdapter(this));

		rl.setRefreshable(false);
		rl.setLoadmoreable(false);
		// rl.setAnimationDuration(10000);
		rl.setOnStartListener(new OnStartListener() {

			@Override
			public void onStartRefresh(XtomRefreshLoadmoreLayout v) {
				// 模拟刷新
				rl.postDelayed(new Runnable() {

					@Override
					public void run() {
						System.out.println("刷新");
						rl.refreshSuccess();
						// rl.refreshFailed();
						// rl.stopRefresh();
					}
				}, 3000);
			}

			@Override
			public void onStartLoadmore(XtomRefreshLoadmoreLayout v) {
				// 模拟加载
				rl.postDelayed(new Runnable() {

					@Override
					public void run() {
						System.out.println("加载");
						rl.loadmoreSuccess();
						// rl.loadmoreFailed();
						// rl.stopLoadmore();
					}
				}, 3000);
			}
		});
	}

}

package xtom.frame.test;

import java.util.List;

import xtom.frame.R;
import xtom.frame.XtomFragment;
import xtom.frame.XtomFragmentActivity;
import xtom.frame.net.XtomNetTask;
import xtom.frame.view.XtomSlidingPaneLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SlidingPaneActivity extends XtomFragmentActivity {
	XtomSlidingPaneLayout slidingPaneLayout;
	Button button1;
	Button button2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_slidingpane);
		super.onCreate(savedInstanceState);
		// slidingPaneLayout.setSliderFadeColor(color);
		// slidingPaneLayout.setParallaxDistance(parallaxBy);

		toogleFragment(Fragment1.class);
	}

	/**
	 * 显示或更换Fragment
	 * 
	 * @param c
	 */
	public void toogleFragment(Class<? extends Fragment> c) {
		FragmentManager manager = getSupportFragmentManager();
		String tag = c.getName();
		FragmentTransaction transaction = manager.beginTransaction();
		Fragment fragment = manager.findFragmentByTag(tag);

		if (fragment == null) {
			try {
				fragment = c.newInstance();
				// 替换时保留Fragment,以便复用
				transaction.add(R.id.content_frame, fragment, tag);
			} catch (Exception e) {
				// ignore
			}
		} else {
			// nothing
		}
		// 遍历存在的Fragment,隐藏其他Fragment
		List<Fragment> fragments = manager.getFragments();
		if (fragments != null)
			for (Fragment fm : fragments)
				if (!fm.equals(fragment))
					transaction.hide(fm);

		transaction.show(fragment);
		transaction.commit();
		closeSidebar();
	}

	/**
	 * 打开侧边栏
	 */
	public void openSidebar() {
		slidingPaneLayout.openPane();
	}

	/**
	 * 关闭侧边栏
	 */
	public void closeSidebar() {
		slidingPaneLayout.closePane();
	}

	@Override
	protected boolean onKeyBack() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean onKeyMenu() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void findView() {
		slidingPaneLayout = (XtomSlidingPaneLayout) findViewById(R.id.drawer_layout);
		button1 = (Button) findViewById(R.id.button1);
		button2 = (Button) findViewById(R.id.button2);
	}

	@Override
	protected void getExras() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void setListener() {
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toogleFragment(Fragment1.class);
			}
		});
		button2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toogleFragment(Fragment2.class);
			}
		});
	}

	@Override
	protected void callBeforeDataBack(XtomNetTask netTask) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void callAfterDataBack(XtomNetTask netTask) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void callBackForGetDataSuccess(XtomNetTask netTask, Object result) {
		// TODO Auto-generated method stub

	}

	public static class Fragment1 extends XtomFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			setContentView(R.layout.fragment1);
			super.onCreate(savedInstanceState);
		}

		@Override
		protected void findView() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void setListener() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void callBeforeDataBack(XtomNetTask netTask) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void callAfterDataBack(XtomNetTask netTask) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void callBackForGetDataSuccess(XtomNetTask netTask,
				Object result) {
			// TODO Auto-generated method stub

		}

	}

	public static class Fragment2 extends XtomFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			setContentView(R.layout.fragment2);
			super.onCreate(savedInstanceState);
		}

		@Override
		protected void findView() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void setListener() {
			// TODO Auto-generated method stub

		}

		@Override
		protected void callBeforeDataBack(XtomNetTask netTask) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void callAfterDataBack(XtomNetTask netTask) {
			// TODO Auto-generated method stub

		}

		@Override
		protected void callBackForGetDataSuccess(XtomNetTask netTask,
				Object result) {
			// TODO Auto-generated method stub

		}

	}
}

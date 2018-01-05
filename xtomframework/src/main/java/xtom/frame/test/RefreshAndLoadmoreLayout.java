package xtom.frame.test;

import xtom.frame.R;
import xtom.frame.view.XtomRefreshLoadmoreLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class RefreshAndLoadmoreLayout extends XtomRefreshLoadmoreLayout {

	public RefreshAndLoadmoreLayout(Context context) {
		this(context, null);
	}

	public RefreshAndLoadmoreLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		System.out.println("RefreshAndLoadmoreLayout1111111");
		setRefreshView(R.layout.refresh_normal, new RefeshListener());
		setLoadmoreView(R.layout.loadmore_normal, new LoadmoreListener());
	}
	
//	public RefreshAndLoadmoreLayout(Context context, AttributeSet attrs,int i) {
//		super(context, attrs,i);
//		System.out.println("RefreshAndLoadmoreLayout222222222");
//		setRefreshView(R.layout.refresh_normal, new RefeshListener());
//		setLoadmoreView(R.layout.loadmore_normal, new LoadmoreListener());
//	}

	private class RefeshListener implements RefreshViewListener {
		private final int ROTATE_ANIM_DURATION = 180;
		private Animation mRotateUpAnim;
		private Animation mRotateDownAnim;

		private ImageView arrowView;
		private TextView textView;
		private ProgressBar progressBar;

		private boolean pull_min1 = true;
		private boolean pull_max1 = false;

		private RefeshListener() {
			mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
			mRotateUpAnim.setFillAfter(true);
			mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
			mRotateDownAnim.setFillAfter(true);
		}

		@Override
		public void onPulling(View refreshView, float percent) {
			findView(refreshView);
			if (percent < 1) {
				if (pull_max1) {
					textView.setText("下拉刷新");
					arrowView.startAnimation(mRotateDownAnim);
				}
				pull_min1 = true;
				pull_max1 = false;
			} else {
				if (pull_min1) {
					textView.setText("松开刷新");
					arrowView.startAnimation(mRotateUpAnim);
				}
				pull_min1 = false;
				pull_max1 = true;
			}
		}

		@Override
		public void onReset(View refreshView) {
			findView(refreshView);
			arrowView.clearAnimation();
			arrowView.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.INVISIBLE);
			textView.setText("下拉刷新");

		}

		@Override
		public void onRefresh(View refreshView) {
			findView(refreshView);
			arrowView.clearAnimation();
			arrowView.setVisibility(View.INVISIBLE);
			progressBar.setVisibility(View.VISIBLE);
			textView.setText("正在刷新");
		}

		private void findView(View fartherView) {
			if (arrowView == null || textView == null || progressBar == null) {
				arrowView = (ImageView) fartherView
						.findViewById(R.id.refresh_arrow);
				textView = (TextView) fartherView
						.findViewById(R.id.refresh_textview);
				progressBar = (ProgressBar) fartherView
						.findViewById(R.id.refresh_progressbar);
			}
		}

		@Override
		public void onSuccess(View refreshView) {
			arrowView.setVisibility(View.GONE);
			progressBar.setVisibility(View.GONE);
			textView.setText("刷新成功");
		}

		@Override
		public void onFailed(View refreshView) {
			arrowView.setVisibility(View.GONE);
			progressBar.setVisibility(View.GONE);
			textView.setText("刷新失败");
		}
	}

	private class LoadmoreListener implements LoadmoreViewListener {
		private TextView textView;
		private ProgressBar progressBar;

		@Override
		public void onPulling(View loadmoreView, float percent) {
			findView(loadmoreView);
			if (percent < 1) {
				textView.setText("上拉加载");
			} else {
				textView.setText("松开加载");
			}

		}

		@Override
		public void onReset(View loadmoreView) {
			findView(loadmoreView);
			progressBar.setVisibility(View.GONE);
			textView.setText("上拉加载");

		}

		@Override
		public void onLoadmore(View loadmoreView) {
			findView(loadmoreView);
			progressBar.setVisibility(View.VISIBLE);
			textView.setText("正在加载");

		}

		private void findView(View fartherView) {
			if (textView == null || progressBar == null) {
				textView = (TextView) fartherView
						.findViewById(R.id.loadmore_textview);
				progressBar = (ProgressBar) fartherView
						.findViewById(R.id.loadmore_progressbar);
			}
		}

		@Override
		public void onSuccess(View loadmoreView) {
			progressBar.setVisibility(View.GONE);
			textView.setText("加载成功");
		}

		@Override
		public void onFailed(View loadmoreView) {
			progressBar.setVisibility(View.GONE);
			textView.setText("加载失败");
		}
	}
}

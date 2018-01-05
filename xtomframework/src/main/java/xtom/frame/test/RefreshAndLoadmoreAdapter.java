package xtom.frame.test;

import xtom.frame.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RefreshAndLoadmoreAdapter extends BaseAdapter {
	Context context;

	public RefreshAndLoadmoreAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 20;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = LayoutInflater.from(context).inflate(R.layout.le, null);
		TextView textView = (TextView) view.findViewById(R.id.textview);
		textView.setText("条目  " + position);
		
		System.out.println("条目  " + position);

		return view;
	}

}

package xtom.frame.test;

import xtom.frame.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Adapter extends BaseAdapter {
	Context context;
	String[] strings;

	public Adapter(Context context,String[] strings) {
		this.context = context;
		this.strings=strings;
	}

	@Override
	public int getCount() {
		return strings.length;
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
		textView.setText(strings[position]);
		return view;
	}

}

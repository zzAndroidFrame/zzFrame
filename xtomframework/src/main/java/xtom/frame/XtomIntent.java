package xtom.frame;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class XtomIntent extends Intent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public XtomIntent() {
		super();
	}

	public XtomIntent(Context packageContext, Class<?> cls) {
		super(packageContext, cls);
	}

	public XtomIntent(Intent o) {
		super(o);
	}

	public XtomIntent(String action, Uri uri, Context packageContext,
			Class<?> cls) {
		super(action, uri, packageContext, cls);
	}

	public XtomIntent(String action, Uri uri) {
		super(action, uri);
	}

	public XtomIntent(String action) {
		super(action);
	}

}

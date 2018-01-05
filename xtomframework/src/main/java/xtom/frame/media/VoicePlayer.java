package xtom.frame.media;

import xtom.frame.media.XtomVoicePlayer.XtomVoicePlayListener;
import xtom.frame.util.XtomLogger;
import android.media.MediaPlayer;

class VoicePlayer extends MediaPlayer {
	private static final String TAG = "VoicePlayer";

	private boolean prepared;
	private XtomVoicePlayer xtomVoicePlayer;

	private PreparedListener preparedListener;
	private CompletionListener completionListener;
	private SeekCompleteListener seekCompleteListener;
	private ErrorListener errorListener;

	VoicePlayer(XtomVoicePlayer xtomVoicePlayer) {
		this.xtomVoicePlayer = xtomVoicePlayer;
		this.preparedListener = new PreparedListener();
		this.completionListener = new CompletionListener();
		this.seekCompleteListener = new SeekCompleteListener();
		this.errorListener = new ErrorListener();
		setListener();
	}

	private void setListener() {
		setOnPreparedListener(preparedListener);
		setOnErrorListener(errorListener);
		setOnCompletionListener(completionListener);
		setOnSeekCompleteListener(seekCompleteListener);
	}

	@Override
	public void reset() {
		super.reset();
		prepared = false;
		setListener();
	}

	@Override
	public void stop() throws IllegalStateException {
		super.stop();
		reset();
	}

	private class PreparedListener implements OnPreparedListener {

		@Override
		public void onPrepared(MediaPlayer mp) {
			prepared = true;
		}
	}

	private class CompletionListener implements OnCompletionListener {

		@Override
		public void onCompletion(MediaPlayer mp) {
			xtomVoicePlayer.cancelTimeThread();
			XtomVoicePlayListener listener = xtomVoicePlayer
					.getXtomVoicePlayListener();
			if (listener != null)
				listener.onComplete(xtomVoicePlayer);

		}
	}

	private class SeekCompleteListener implements OnSeekCompleteListener {

		@Override
		public void onSeekComplete(MediaPlayer mp) {
			XtomLogger.i(TAG, "onSeekComplete");
		}
	}

	private class ErrorListener implements OnErrorListener {

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			XtomVoicePlayListener listener = xtomVoicePlayer
					.getXtomVoicePlayListener();
			if (listener != null)
				listener.onError(xtomVoicePlayer);
			return false;
		}
	}

	public boolean isPrepared() {
		return prepared;
	}

}

package com.example.androidlamemp3;

import java.io.IOException;



import com.example.lamemp3.MP3Recorder;

import android.media.MediaPlayer;



/**
 * 
 * @author zhaoyangyang
 * 
 */
public class RecorderAndPlayUtil {

	public interface onPlayerCompletionListener {
		public void onPlayerCompletion();
	}

	private onPlayerCompletionListener mPlayerCompletion = new onPlayerCompletionListener() {

		@Override
		public void onPlayerCompletion() {
			// do nothing
		}
	};

	public void setOnPlayerCompletionListener(onPlayerCompletionListener l) {
		mPlayerCompletion = l;
	}

	private MediaPlayer mPlayer = null;
	private String mPlayingPath = null;
	private MP3Recorder mRecorder = null;

	public RecorderAndPlayUtil() {
		mPlayer = new MediaPlayer();
		mRecorder = new MP3Recorder();
	}

	public void startRecording() {
		mRecorder.start();
	}

	public void stopRecording() {
		mRecorder.stop();
	}

	public void startPlaying(String filePath) {
		if (filePath == null) {
			return;
		}
		if (mPlayingPath != null && mPlayingPath.equals(filePath)
				&& mPlayer != null && mPlayer.isPlaying()) {
			stopPlaying();
			mPlayingPath = null;
			return;
		}
		mPlayingPath = filePath;
		stopPlaying();
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mPlayerCompletion.onPlayerCompletion();
			}
		});

		try {
			mPlayer.setDataSource(filePath);
			mPlayer.prepare();
			mPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stopPlaying() {
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
		}
	}

	public void release() {
		stopRecording();
		if (mPlayer != null) {
			if (mPlayer.isPlaying()) {
				mPlayer.stop();
			}
			mPlayer.release();
		}
	}

	public String getRecorderPath() {
		return mRecorder.getFilePath();
	}

	public MP3Recorder getRecorder() {
		return mRecorder;
	}
}

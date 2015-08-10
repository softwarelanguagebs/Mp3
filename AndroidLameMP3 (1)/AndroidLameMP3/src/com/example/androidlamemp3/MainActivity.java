package com.example.androidlamemp3;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.example.lamemp3.MP3Recorder;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private LinearLayout mRecorderLayout = null;
	private TextView mShowTime = null;

	private boolean mIsRecording = false;
	private boolean mIsLittleTime = false;
	private boolean mIsSendVoice = false;

	private RecorderAndPlayUtil mRecorder = null;
	private TimerTask mTimerTask = null;
	private Timer mTimer = null;
	private Thread mDelayedThread = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRecorderLayout = (LinearLayout) findViewById(R.id.recorder_layout);
		mShowTime = (TextView) findViewById(R.id.show_time_tv);
		mShowTime.setText("0\"");

		mRecorder = new RecorderAndPlayUtil();

		mRecorder.getRecorder().setHandle(new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case MP3Recorder.MSG_REC_STARTED:
					// ��ʼ¼��
					break;
				case MP3Recorder.MSG_REC_STOPPED:
					// ֹͣ¼��
					if (mIsSendVoice) {// �Ƿ���¼��
						mIsSendVoice = false;
						if (mIsLittleTime) {
							if (mRecorder.getRecorderPath() != null) {
								File file = new File(mRecorder
										.getRecorderPath());
								file.delete();
							}
							Toast.makeText(getApplicationContext(), "¼��ʱ��̫��",
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
					break;
				case MP3Recorder.MSG_ERROR_GET_MIN_BUFFERSIZE:
					initRecording();
					Toast.makeText(getApplicationContext(), "�������ֻ���֧��",
							Toast.LENGTH_SHORT).show();
					break;
				case MP3Recorder.MSG_ERROR_CREATE_FILE:
					initRecording();
					Toast.makeText(getApplicationContext(), "������Ƶ�ļ�����",
							Toast.LENGTH_SHORT).show();
					break;
				case MP3Recorder.MSG_ERROR_REC_START:
					initRecording();
					Toast.makeText(getApplicationContext(), "��ʼ��¼��������",
							Toast.LENGTH_SHORT).show();
					break;
				case MP3Recorder.MSG_ERROR_AUDIO_RECORD:
					initRecording();
					Toast.makeText(getApplicationContext(), "¼����ʱ�����",
							Toast.LENGTH_SHORT).show();
					break;
				case MP3Recorder.MSG_ERROR_AUDIO_ENCODE:
					initRecording();
					Toast.makeText(getApplicationContext(), "�������",
							Toast.LENGTH_SHORT).show();
					break;
				case MP3Recorder.MSG_ERROR_WRITE_FILE:
					initRecording();
					Toast.makeText(getApplicationContext(), "�ļ�д�����",
							Toast.LENGTH_SHORT).show();
					break;
				case MP3Recorder.MSG_ERROR_CLOSE_FILE:
					initRecording();
					Toast.makeText(getApplicationContext(), "�ļ����رճ���",
							Toast.LENGTH_SHORT).show();
					break;
				}
			}
		});

		Button play = (Button) findViewById(R.id.play_bt);
		play.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mRecorder.startPlaying(mRecorder.getRecorderPath());
			}
		});
		Button recorder = (Button) findViewById(R.id.recorder_bt);
		recorder.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (mDelayedThread == null) {
						mDelayedThread = new Thread(new Runnable() {

							@Override
							public void run() {
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								} finally {
									mDelayedThread = null;
								}
							}
						});
						mDelayedThread.start();
					} else {
						return true;
					}
					mIsRecording = true;
					mIsLittleTime = true;
					mTimerTask = new TimerTask() {
						int i = 30;

						@Override
						public void run() {
							mIsLittleTime = false;
							i--;
							handler.post(new Runnable() {

								@Override
								public void run() {
									mShowTime.setText((30 - i) + "\"");
									if (i < 11 && i != 0 && (i % 2 == 0)) {
										String message = "��ʣ" + i + "��¼������";
										Toast.makeText(getApplicationContext(),
												message, Toast.LENGTH_SHORT)
												.show();
									}
								}
							});
							if (i == 0) {
								mIsSendVoice = true;
								handler.post(new Runnable() {

									@Override
									public void run() {
										Toast.makeText(getApplicationContext(),
												"¼������", Toast.LENGTH_SHORT)
												.show();
										mTimer.cancel();
										mTimerTask.cancel();
										mRecorderLayout
												.setVisibility(View.GONE);
										mShowTime.setText("0\"");
										mIsRecording = false;
										// ¼������
										mRecorder.stopRecording();
									}
								});
							}
							if (i < 0) {
								mTimer.cancel();
								mTimerTask.cancel();
							}
						}
					};
					mRecorder.startRecording();
					mTimer = new Timer(true);
					mTimer.schedule(mTimerTask, 1000, 1000);
					mRecorderLayout.setVisibility(View.VISIBLE);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (mIsRecording) {
						initRecording();
					}
				}
				return false;
			}
		});

		recorder.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsSendVoice = true;
			}
		});
	}

	Handler handler = new Handler();

	private void initRecording() {
		mTimer.cancel();
		mTimerTask.cancel();
		mRecorder.stopRecording();
		mRecorderLayout.setVisibility(View.GONE);
		mShowTime.setText("0\"");
		mIsRecording = false;
	}
}

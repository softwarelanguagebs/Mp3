package com.example.lamemp3;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.example.androidlamemp3.util.StorageUtil;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;

public class MP3Recorder {

	private String mFilePath = null;
	private int sampleRate = 0;
	private boolean isRecording = false;
	private boolean isPause = false;
	private Handler handler = null;

	/**
	 * ��ʼ¼��
	 */
	public static final int MSG_REC_STARTED = 1;

	/**
	 * ����¼��
	 */
	public static final int MSG_REC_STOPPED = 2;

	/**
	 * ��ͣ¼��
	 */
	public static final int MSG_REC_PAUSE = 3;

	/**
	 * ����¼��
	 */
	public static final int MSG_REC_RESTORE = 4;

	/**
	 * ����������,�������ֻ���֧��
	 */
	public static final int MSG_ERROR_GET_MIN_BUFFERSIZE = -1;

	/**
	 * �����ļ�ʱ�˽���
	 */
	public static final int MSG_ERROR_CREATE_FILE = -2;

	/**
	 * ��ʼ��¼����ʱ�˽���
	 */
	public static final int MSG_ERROR_REC_START = -3;

	/**
	 * ¼����ʱ�����
	 */
	public static final int MSG_ERROR_AUDIO_RECORD = -4;

	/**
	 * ����ʱ����
	 */
	public static final int MSG_ERROR_AUDIO_ENCODE = -5;

	/**
	 * д�ļ�ʱ����
	 */
	public static final int MSG_ERROR_WRITE_FILE = -6;

	/**
	 * û���ر��ļ���
	 */
	public static final int MSG_ERROR_CLOSE_FILE = -7;

	public MP3Recorder() {
		this.sampleRate = 8000;
	}

	/**
	 * ��Ƭ
	 */
	public void start() {
		if (isRecording) {
			return;
		}

		new Thread() {
			@Override
			public void run() {
				String fileDir = StorageUtil.getSDPath() + "LameMP3/Voice/";

				File dir = new File(fileDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				mFilePath = StorageUtil.getSDPath() + "LameMP3/Voice/"
						+ System.currentTimeMillis() + ".mp3";

				System.out.println(mFilePath);
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				// ���ݶ���õļ������ã�����ȡ���ʵĻ����С
				final int minBufferSize = AudioRecord.getMinBufferSize(
						sampleRate, AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				if (minBufferSize < 0) {
					if (handler != null) {
						handler.sendEmptyMessage(MSG_ERROR_GET_MIN_BUFFERSIZE);
					}
					return;
				}
				AudioRecord audioRecord = new AudioRecord(
						MediaRecorder.AudioSource.MIC, sampleRate,
						AudioFormat.CHANNEL_IN_MONO,
						AudioFormat.ENCODING_PCM_16BIT, minBufferSize * 2);

				// 5��Ļ���
				short[] buffer = new short[sampleRate * (16 / 8) * 1 * 5];
				byte[] mp3buffer = new byte[(int) (7200 + buffer.length * 2 * 1.25)];

				FileOutputStream output = null;
				try {
					output = new FileOutputStream(new File(mFilePath));
				} catch (FileNotFoundException e) {
					if (handler != null) {
						handler.sendEmptyMessage(MSG_ERROR_CREATE_FILE);
					}
					return;
				}
				MP3Recorder.init(sampleRate, 1, sampleRate, 32);
				isRecording = true; // ¼��״̬
				isPause = false; // ¼��״̬
				try {
					try {
						audioRecord.startRecording(); // ����¼����ȡ��Ƶ����
					} catch (IllegalStateException e) {
						// ����¼��...
						if (handler != null) {
							handler.sendEmptyMessage(MSG_ERROR_REC_START);
						}
						return;
					}

					try {
						// ��ʼ¼��
						if (handler != null) {
							handler.sendEmptyMessage(MSG_REC_STARTED);
						}

						int readSize = 0;
						boolean pause = false;
						while (isRecording) {
							/*--��ͣ--*/
							if (isPause) {
								if (!pause) {
									handler.sendEmptyMessage(MSG_REC_PAUSE);
									pause = true;
								}
								continue;
							}
							if (pause) {
								handler.sendEmptyMessage(MSG_REC_RESTORE);
								pause = false;
							}
							/*--End--*/
							/*--ʵʱ¼��д����--*/
							readSize = audioRecord.read(buffer, 0,
									minBufferSize);
							if (readSize < 0) {
								if (handler != null) {
									handler.sendEmptyMessage(MSG_ERROR_AUDIO_RECORD);
								}
								break;
							} else if (readSize == 0) {
								;
							} else {
								int encResult = MP3Recorder.encode(buffer,
										buffer, readSize, mp3buffer);
								if (encResult < 0) {
									if (handler != null) {
										handler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
									}
									break;
								}
								if (encResult != 0) {
									try {
										output.write(mp3buffer, 0, encResult);
									} catch (IOException e) {
										if (handler != null) {
											handler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
										}
										break;
									}
								}
							}
							/*--End--*/
						}
						/*--¼����--*/
						int flushResult = MP3Recorder.flush(mp3buffer);
						if (flushResult < 0) {
							if (handler != null) {
								handler.sendEmptyMessage(MSG_ERROR_AUDIO_ENCODE);
							}
						}
						if (flushResult != 0) {
							try {
								output.write(mp3buffer, 0, flushResult);
							} catch (IOException e) {
								if (handler != null) {
									handler.sendEmptyMessage(MSG_ERROR_WRITE_FILE);
								}
							}
						}
						try {
							output.close();
						} catch (IOException e) {
							if (handler != null) {
								handler.sendEmptyMessage(MSG_ERROR_CLOSE_FILE);
							}
						}
						/*--End--*/
					} finally {
						audioRecord.stop();
						audioRecord.release();
					}
				} finally {
					MP3Recorder.close();
					isRecording = false;
				}
				if (handler != null) {
					handler.sendEmptyMessage(MSG_REC_STOPPED);
				}
			}
		}.start();
	}

	public void stop() {
		isRecording = false;
	}

	public void pause() {
		isPause = true;
	}

	public void restore() {
		isPause = false;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public boolean isPaus() {
		if (!isRecording) {
			return false;
		}
		return isPause;
	}

	public String getFilePath() {
		return mFilePath;
	}

	/**
	 * ¼��״̬����
	 * 
	 * @see RecMicToMp3#MSG_REC_STARTED
	 * @see RecMicToMp3#MSG_REC_STOPPED
	 * @see RecMicToMp3#MSG_REC_PAUSE
	 * @see RecMicToMp3#MSG_REC_RESTORE
	 * @see RecMicToMp3#MSG_ERROR_GET_MIN_BUFFERSIZE
	 * @see RecMicToMp3#MSG_ERROR_CREATE_FILE
	 * @see RecMicToMp3#MSG_ERROR_REC_START
	 * @see RecMicToMp3#MSG_ERROR_AUDIO_RECORD
	 * @see RecMicToMp3#MSG_ERROR_AUDIO_ENCODE
	 * @see RecMicToMp3#MSG_ERROR_WRITE_FILE
	 * @see RecMicToMp3#MSG_ERROR_CLOSE_FILE
	 */
	public void setHandle(Handler handler) {
		this.handler = handler;
	}

	/*--����ΪNative����--*/
	static {
		System.loadLibrary("mp3lame");
	}

	/**
	 * ��ʼ��¼�Ʋ���
	 */
	public static void init(int inSamplerate, int outChannel,
			int outSamplerate, int outBitrate) {
		init(inSamplerate, outChannel, outSamplerate, outBitrate, 7);
	}

	/**
	 * ��ʼ��¼�Ʋ��� quality:0=�ܺú��� 9=�ܲ�ܿ�
	 */
	public native static void init(int inSamplerate, int outChannel,
			int outSamplerate, int outBitrate, int quality);

	/**
	 * ��Ƶ���ݱ���(PCM���,PCM�ҽ�,MP3���)
	 */
	public native static int encode(short[] buffer_l, short[] buffer_r,
			int samples, byte[] mp3buf);

	/**
	 * ˢ�ɾ�������
	 */
	public native static int flush(byte[] mp3buf);

	/**
	 * ��������
	 */
	public native static void close();
}

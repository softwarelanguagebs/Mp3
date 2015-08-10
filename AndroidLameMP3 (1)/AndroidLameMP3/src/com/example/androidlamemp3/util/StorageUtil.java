package com.example.androidlamemp3.util;

import java.security.PublicKey;

import android.os.Environment;
import android.util.Log;

public class StorageUtil {
	
	private static String TAG = StorageUtil.class.getName();
	
	/**
	 * SD���Ƿ�����
	 * @return
	 */
	public  static boolean isStorageAvailable() {
        String sdStatus = Environment.getExternalStorageState();
        if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // ���sd�Ƿ����
            Log.v(TAG, "SD��������");
            return false;
        }
        return true;
	}

	
	public static String getSDPath(){
		if(isStorageAvailable()){
			
			return Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
		}else{
			return null;
		}
		
	}
}

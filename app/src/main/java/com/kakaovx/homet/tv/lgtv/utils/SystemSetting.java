package com.kakaovx.homet.tv.lgtv.utils;

import android.annotation.SuppressLint;
import android.os.Build;

import com.kakaovx.homet.tv.lgtv.OMAReceiver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class SystemSetting {

	/**
	 * /tmp 폴더 사용 권한문제로 /data/lgu_app/tmp 폴더를 사용하기로 함
	 *
	 */

	/**
	 * 모델명을 추출하는 Method
	 * @return 모델명
	 */
	public static String getModel()
	{
		return Build.MODEL;
	}

	@SuppressLint("SetWorldWritable")
	public static int createTempFile(String filename, String value) {

		// /tmp 폴더 경로 변경으로 로직수정 2018.03.09 modify by kook

		if(filename == null)
			return -1;

		String path = OMAReceiver.COMMON_PATH_TMP + filename;
		File file = new File(path);
		try{
			//LogUtil.d(LogUtil.DEBUG_LEVEL_3,"file " + file.exists());
			//LogUtil.d(LogUtil.DEBUG_LEVEL_3,"file " + file.getAbsolutePath());
			FileOutputStream fos = new FileOutputStream(file);

			if(value != null){
				fos.write(value.getBytes());
			}

			fos.close();


		} catch(IOException e){
			e.printStackTrace();
			return -1;
		}

		file.setWritable(true, false);
		file.setReadable(true, false);
		file.setExecutable(true,false);

		return 0;
	}

	public static String checkTempFile(String filename) {

		// /tmp 폴더 경로 변경으로 로직수정 2018.03.09 modify by kook

		String result = "";
		try {
			File tempFile = new File(OMAReceiver.COMMON_PATH_TMP + filename);
			if(tempFile != null) {
				BufferedReader in = new BufferedReader(new FileReader(tempFile));
				String out;
				while ((out = in.readLine()) != null) {
					result += out;
				}
				LogUtil.v(LogUtil.DEBUG_LEVEL_2, "read temp file : " + filename + " -> " + result);
				in.close();
			}
		} catch (Exception e) {
			e.getStackTrace();
		}

		return result;
	}

	public static boolean existTempFile(String filename){
		boolean rVal = false;
		File tempFile = new File(OMAReceiver.COMMON_PATH_TMP + filename);
		if (tempFile.exists()) {
			LogUtil.d(LogUtil.DEBUG_LEVEL_2, filename +" is exists()");
			rVal = true;
		}
		return rVal;
	}

	public static int deleteTempFile(String filename) {

		// /tmp 폴더 경로 변경으로 로직수정 2018.03.09 modify by kook

		if(filename != null){

			File file = new File(OMAReceiver.COMMON_PATH_TMP + filename);

			if(file != null && file.exists()){
				file.delete();
				return 0;
			}else{
				LogUtil.e(3, "file not found : " + OMAReceiver.COMMON_PATH_TMP + filename);
			}

		}
		return -1;
	}

	
}

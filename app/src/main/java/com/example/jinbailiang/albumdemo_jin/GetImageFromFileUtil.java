package com.example.jinbailiang.albumdemo_jin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;

public class GetImageFromFileUtil {

	/**
	 * 从sd卡获取图片资源
	 * @return
	 */
	public  static List<String> getImagePathFromSD(String filePath) {
		List<String> imagePathList = new ArrayList<String>();
		try {

			// 图片列表

			// 得到sd卡内image文件夹的路径   File.separator(/)
			//        String filePath = Environment.getExternalStorageDirectory().toString() + File.separator
			//                + "image";
			// 得到该路径文件夹下所有的文件
			if(filePath == null)return imagePathList;
			File fileAll = new File(filePath);
			File[] files = fileAll.listFiles();
			// 将所有的文件存入ArrayList中,并过滤所有图片格式的文件
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if (checkIsImageFile(file.getPath())) {
					imagePathList.add(file.getPath());
				}
			}
			// 返回得到的图片列表


		} catch (Exception e) {
			e.printStackTrace();
		}
		return imagePathList;
	}

	/**
	 * 检查扩展名，得到图片格式的文件
	 * @param fName  文件名
	 * @return
	 */
	@SuppressLint("DefaultLocale")
	public static boolean checkIsImageFile(String fName) {
		boolean isImageFile = false;
		// 获取扩展名
		String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
				fName.length()).toLowerCase();
		if (FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
				|| FileEnd.equals("jpeg")|| FileEnd.equals("bmp") ) {
			isImageFile = true;
		} else {
			isImageFile = false;
		}
		return isImageFile;
	}

}

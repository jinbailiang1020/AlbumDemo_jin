package com.example.jinbailiang.albumdemo_jin;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class ImageUtil {


	@SuppressLint("NewApi")
	public static Bitmap getimageForLimitWidthHeigth(String srcPath , Context context,int w, int h) {

		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空??????????????????????????jinbailiang
		newOpts.inJustDecodeBounds = false;
		int opw = newOpts.outWidth;
		int oph = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be;//be=1表示不缩放
		be = opw/w > oph/h ? opw/w : oph/h ;//取大的  （图 是取小的）
		if (be <= 0) be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		if(bitmap!=null)System.out.println("length = "+bitmap.getAllocationByteCount());
//		bitmap = ThumbnailUtils.extractThumbnail(bitmap, w, h);
		return bitmap;//压缩好比例大小后再进行质量压缩
	}


	public static Bitmap getimage(String srcPath , Context context) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空??????????????????????????jinbailiang
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap ,context);//压缩好比例大小后再进行质量压缩
	}

	//压缩图片
	public static Bitmap compressImage(Bitmap image , Context context) {
		if(image ==null){
			System.out.println("图片压缩bitmap为空");
			//			Toast.makeText(context, "图片压缩bitmap为空", Toast.LENGTH_SHORT).show();
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while ( baos.toByteArray().length / 1024>100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
			options -= 10;//每次都减少10
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	public static Bitmap getBitmapFromUri(Uri uri,Context context)
	{
		try
		{
			// 读取uri所在的图片
			return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
		}
		catch (Exception e)
		{
			Log.e("[Android]", e.getMessage());
			Log.e("[Android]", "目录为：" + uri);
			e.printStackTrace();
			return null;
		}
	}



	public static Bitmap compressByProportion(Bitmap image,Context context) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		if(image == null)return null;
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		while (baos.toByteArray().length / 1024>1024) {
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
		}
		/*	if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
			baos.reset();//重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
		}*/
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		//开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		//现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;//这里设置高度为800f
		float ww = 480f;//这里设置宽度为480f
		//缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;//be=1表示不缩放
		if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;//设置缩放比例
		newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565
		//重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		//		return compressImage(bitmap ,context);//压缩好比例大小后再进行质量压缩
		return bitmap;
	}

	/**
	 *
	 * @Title: chooseFile
	 * @Description: TODO(选择照片)
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public static  void chooseFile(Context context , int TO_ALBUM_PAGE) {
		try {
			Intent i = new Intent(
					Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			Intent getAlbum2 = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			//			getAlbum2.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
			((Activity) context).startActivityForResult(Intent.createChooser(getAlbum2, "选择上传文件"),TO_ALBUM_PAGE);
		} catch (android.content.ActivityNotFoundException e) {
			e.printStackTrace();
			Toast.makeText(context,	"请安装一个文件管理器", Toast.LENGTH_LONG).show();
		}
	}

	/*	*//**
	 * @Title: takePhoto
	 * @Description: TODO(拍照)
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 *//*
	public static Uri takePhoto(Context context ,int TO_CAMERA_PAGE) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		String rootPath = Environment.getExternalStorageDirectory().getPath(); // 获取SD卡的根目录
		File file = new File(rootPath, CRMConstant.imagePath);
		if (!file.exists()) {
			file.mkdirs();
		}
		File img = new File(file, System.currentTimeMillis() + ".jpeg");
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(img));
		((Activity) context).startActivityForResult(intent,TO_CAMERA_PAGE );
		return Uri.fromFile(img);
	}
	  */

	/**
	 * 保存文件
	 * @param bm
	 * @param fileName
	 * @throws IOException
	 */
	public void saveFile(Bitmap bm, String fileName ,String filePath) throws IOException {
		File dirFile = new File(filePath);
		if(!dirFile.exists()){
			dirFile.mkdir();
		}
		File myCaptureFile = new File(filePath + fileName);
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
		bm.compress(Bitmap.CompressFormat.JPEG, 80, bos);
		bos.flush();
		bos.close();
	}



	//下载图片的主方法
	/**
	 *  true.成功
	 *  0.失败
	 * @param strUrl
	 * @return
	 */
	//	public  static boolean downloadPicture(String urlStr,Map<String,Object> paramsMap , String imageName) {
	//
	//		InputStream is = null;
	//		FileOutputStream fos = null;
	//		try {
	//			//构建图片的url地址
	//			//            url = new URL("http://avatar.csdn.net/C/6/8/1_bz419927089.jpg");
	//			urlStr += "?";
	//			for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
	//				System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
	//				urlStr += entry.getKey() + "=" + entry.getValue() + "&";
	//			}
	//			urlStr = urlStr.substring(0, urlStr.length() -1);
	//			URL url = new URL(urlStr);
	//			System.out.println("下载："+urlStr);
	//			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	//			conn.setConnectTimeout(5000);
	//			conn.setRequestMethod("GET");
	//			if (conn.getResponseCode() == 200) {
	//				is = conn.getInputStream();
	//				String rootPath = Environment.getExternalStorageDirectory().getPath(); // 获取SD卡的根目录
	//				File file = new File(rootPath +"/"+ CRMConstant.imagePath);
	//				if (!file.exists()) {
	//					file.mkdirs();
	//				}
	//				File imageFile = new File(file, imageName);
	//				fos = new FileOutputStream(imageFile);
	//				int len = 0;
	//				byte[] buffer = new byte[1024];
	//				while ((len = is.read(buffer)) != -1) {
	//					fos.write(buffer, 0, len);
	//				}
	//				fos.flush();
	//				return true;
	//			}else{
	//				return false;
	//			}
	//		} catch (Exception e) {
	//			//告诉handler，图片已经下载失败
	//			//            handler.sendEmptyMessage(LOAD_ERROR);
	//			e.printStackTrace();
	//			return false;
	//		} finally {
	//			//在最后，将各种流关闭
	//			try {
	//				if (is != null) {
	//					is.close();
	//				}
	//				if (fos != null) {
	//					fos.close();
	//				}
	//			} catch (Exception e) {
	//				//                handler.sendEmptyMessage(LOAD_ERROR);
	//				e.printStackTrace();
	//				return false;
	//			}
	//
	//		}
	//	}

	/**
	 * 判断文件是否存在
	 * @param fileAbsolutePath //文件路劲
	 * @param yourPath  /判断的图片名
	 * @return true 有相同
	 */
	public static boolean ifHaveThisFile(String fileAbsolutePath,String yourPath) {
		//		Vector vecFile = new Vector();
		File file = new File(fileAbsolutePath);
		if(! file.exists()){
			file.mkdirs();
		}
		File[] subFile = file.listFiles();
		if(subFile == null)return false;
		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// 判断是否为文件夹
			if((yourPath+"").equals(subFile[iFileLength].getName())){
				return true;
			}
		}
		return false;
	}

	public static Bitmap drawableToBitamp(Drawable drawable)
	{
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		System.out.println("Drawable转Bitmap");
		Bitmap.Config config =
				drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
						: Bitmap.Config.RGB_565;
		Bitmap bitmap = Bitmap.createBitmap(w,h,config);
		//		//注意，下面三行代码要用到，否在在View或者surfaceview里的canvas.drawBitmap会看不到图
		//		Canvas canvas = new Canvas(bitmap);
		//		drawable.setBounds(0, 0, w, h);
		//		drawable.draw(canvas);
		return bitmap;
	}

	//拍照后直接压缩图片  来自销售
	public static  void compressImage(File file, Context context) {
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),
				newOpts);// 此时返回bm为空
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 1200f;// 这里设置高度为800f
		float ww = 1200f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) Math.ceil(newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) Math.ceil(newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		newOpts.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), newOpts);
		System.out.println(file.getAbsolutePath());
		System.out.println(file.getPath());
		compressBmpToFile(bitmap, file);
		return;// 压缩好比例大小后再进行质量压缩
	}

	public static String handleFileChooseUri(Uri uri, Context context) {

		String uploadFilePath = "";
		if ("content".equalsIgnoreCase(uri.getScheme())) {
			String[] projection = { "_data" };
			Cursor cursor = null;
			try {
				cursor = context.getContentResolver().query(uri, projection, null,
						null, null);
				int columnIndex = cursor.getColumnIndexOrThrow("_data");
				if (cursor.moveToFirst()) {
					uploadFilePath = cursor.getString(columnIndex);
					File file = new File(uploadFilePath);
					compressImage(file ,context);
					//					attachment.setText(file.getName());
					//					attachment.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			uploadFilePath = uri.getPath();
			int index01 = uploadFilePath.lastIndexOf(".");

			String fileSuffix = uploadFilePath.substring(index01 + 1,
					uploadFilePath.length()).toLowerCase(Locale.getDefault());
			if (fileSuffix.equals("jpg") || fileSuffix.equals("jpeg")
					|| fileSuffix.equals("png") || fileSuffix.equals("bmp")
					|| fileSuffix.equals("webp") || fileSuffix.equals("gif")) {
				File file = new File(uploadFilePath);
				compressImage(file ,context);
				//				attachment.setText(file.getName());
				//				attachment.setVisibility(View.VISIBLE);
			} else {
				Toast.makeText(context, "不支持该格式文件上传！", 0).show();
				uploadFilePath = "";
				//				attachment.setVisibility(View.GONE);
			}
		}
		return uploadFilePath;
	}

	public static void compressBmpToFile(Bitmap bmp, File file) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int options = 90;// 个人喜欢从80开始,
		if(bmp ==null)return;
		bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
		while (baos.toByteArray().length / 1024 > 300) {
			baos.reset();
			options -= 10;
			bmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
		}
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baos.toByteArray());
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//	public static  Uri takePhoto( Context context ,int requestCode) {
	//		if (!checkSD(context))return  null;
	//		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	//		String rootPath = Environment.getExternalStorageDirectory().getPath(); // 获取SD卡的根目录
	//		File file = new File(rootPath, CRMConstant.imagePath);
	//		if (!file.exists()) {
	//			file.mkdirs();
	//		}
	//		File img = new File(file, System.currentTimeMillis() + ".jpeg");
	//		Uri u1 = Uri.fromFile(img);
	//		intent.putExtra(MediaStore.EXTRA_OUTPUT, u1);
	//		((Activity)context).startActivityForResult(intent, requestCode);
	//		return u1;
	//	}

	public static boolean checkSD(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) { // 判断是否存在SD卡
			return true;
		} else {
			new AlertDialog.Builder(context).setMessage("检测到手机存储卡不可用！")
			.setPositiveButton("确定", null).show();
			return false;
		}
	}


}
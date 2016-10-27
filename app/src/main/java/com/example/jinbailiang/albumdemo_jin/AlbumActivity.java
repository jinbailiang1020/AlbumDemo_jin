package com.example.jinbailiang.albumdemo_jin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jinbailiang.R;



@SuppressLint("HandlerLeak") public class AlbumActivity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {
	//	private ArrayList<String> bitMapList;
	//	private Cursor c;
	private GridViewAdapter gridViewAdapter;
	private GridView gv;
	Handler handler;
	private Vector<String> imageList = new Vector<>();
	//	private HashMap<String, String> imageMap;
	//	private int lastpos;
	//	private ImageListFileAdapter listFileAdapter;
	//	private Map<String, View> map;
	private PopupWindow popupWindow;
	private ArrayList<String> selectedImageUrls;
	private LinearLayout showAll;
	private WindowManager wm;
	private ProgressDialog mProgressDialog;
	private final int  SCAN_OK = 110;
	private ArrayList<String>imageCatalogue = new ArrayList<>();//Image Catalogue 目录
	private View contentView;
	private GroupAdapter catalogueAdapter;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SCAN_OK:
				catalogueGridView.setAdapter(catalogueAdapter);
				gridViewAdapter.notifyDataSetChanged();
				mProgressDialog.dismiss();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	private GridView catalogueGridView;

	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(0x1);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		getImages();
	}

	private void setImageCatalogueToPopuWindow() {
		try {
			contentView = View.inflate(AlbumActivity.this, R.layout.list_gridview, null);
			catalogueGridView = (GridView) contentView.findViewById(R.id.main_grid);
			catalogueGridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					mProgressDialog.show();
					imageList.clear();
					String parentPath = imageCatalogue.get(position);
					File parentFile = new File(parentPath);
					for (File file : parentFile.listFiles()) {
						imageList.add(file.getAbsolutePath());
					}
					/**
					 * google了一下：
					 * JDK7中的Collections.Sort方法实现中，
					 * 如果两个值是相等的，
					 * 那么compare方法需要返回0
					 * ，否则可能会在排序时抛错，而JDK6是没有这个限制的。
					 */
					Collections.sort(imageList, new Comparator<String>(){

						@Override
						public int compare(String lhs, String rhs) {
							File f1 = new File(lhs);
							File f2 = new File(rhs);//==0会报错；
							if(f1.lastModified()-f2.lastModified()==0)return 0;
							int compare = f1.lastModified()-f2.lastModified() > 0 ? -1 : 1;
							compare = compare !=0?compare:1;
							return compare;
						}
					});
					gridViewAdapter.setIsFirstComing(Boolean.valueOf(true));//进入自动加载图片
					gridViewAdapter.notifyDataSetChanged();
					mProgressDialog.dismiss();
					popupWindow.dismiss();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("WrongConstant")
	private void init() {
		wm = (WindowManager)getSystemService("window");
		showAll = (LinearLayout)findViewById(R.id.bootom);
		gv = (GridView)findViewById(R.id.gv);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.ok).setOnClickListener(this);
		showAll.setOnClickListener(this);
		gridViewAdapter = new GridViewAdapter(this, gv, imageList);
		gv.setAdapter(gridViewAdapter);
		gv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(AlbumActivity.this,ImageDetailActivity.class);
				intent.putExtra("path",imageList.get(position));
				startActivity(intent);
			}
		});
	}

	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.bootom:
			showPopWindow();
			break;
		case R.id.ok:
			LinkedList<String> images = gridViewAdapter.getSelectedImageUrl();
			System.out.println(Arrays.toString(images.toArray()));
			Toast.makeText(this, Arrays.toString(images.toArray()), Toast.LENGTH_SHORT).show();
			break;
		}
	}


	/**
	 *   get all images in mobilePhone  by ContentProvider;
	 *      setData : 1 image catalogue(showing at GridView by popuWindow);
	 *                2 image detail(showing at GridView by Activity);
	 */
	private void getImages() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, "Environment.MEDIA_MOUNTED", Toast.LENGTH_SHORT).show();
			return;
		}
		mProgressDialog = ProgressDialog.show(this, null, "请稍等。。。");

		new Thread(new Runnable() {
			boolean onlyOnce = true;

			@Override
			public void run() {
				Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				ContentResolver mContentResolver = AlbumActivity.this.getContentResolver();


				Cursor mCursor = mContentResolver.query(mImageUri, null,
						MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=? or "
								+ MediaStore.Images.Media.MIME_TYPE + "=?",
								new String[] { "image/jpeg", "image/png" ,"image/jpg","image/bmp","image/gif"}, MediaStore.Images.Media.DATE_MODIFIED);

				while (mCursor.moveToNext()) {
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));//得到手机中所有图片；
					File file1 = new File(path);
					String parentName = file1.getParentFile().getAbsolutePath();
					if(!imageCatalogue.contains(parentName))
						imageCatalogue.add(parentName);
					//首次默认加载camera中的照片；
					if(onlyOnce  &&  file1.getParentFile().getName().equals("Camera")){
						onlyOnce = false;
						File parentFile = new File(parentName);
						imageList.clear();
						for (File file : parentFile.listFiles()) {
							imageList.add(file.getAbsolutePath());
						}
					}
				}
				System.out.println(Arrays.toString(imageList.toArray()));;
				mCursor.close();
				setImageCatalogueToPopuWindow();
				catalogueAdapter = new GroupAdapter(AlbumActivity.this, imageCatalogue, catalogueGridView);
				mHandler .sendEmptyMessage(SCAN_OK );
			}
		}).start();

	}

	private void showPopWindow() {
		if(popupWindow == null) popupWindow = new PopupWindow(contentView, wm.getDefaultDisplay().getWidth(), ((wm.getDefaultDisplay().getHeight() * 0x3) / 0x4), true);
		popupWindow.setBackgroundDrawable(getWallpaper());
		if(popupWindow.isShowing()){
			popupWindow.dismiss();
			return;
		}
		int[] location = new int[2];
		findViewById(R.id.bootom).getLocationOnScreen(location);
		//		listFileAdapter.notifyDataSetChanged();//jinbailiang
		//popupWindow.showAtLocation(findViewById(R.id.bootom), 0, location[0], 0);
		popupWindow.showAtLocation(findViewById(R.id.bootom), Gravity.NO_GRAVITY, location[0], (location[1] - popupWindow.getHeight()));
	}


	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if(imageCatalogue.size() > 0) {//imagePathList
		}
		synchronized(this) {
			imageList.clear();
			imageList.addAll(GetImageFromFileUtil.getImagePathFromSD(imageCatalogue.get(position)));
			System.out.println(imageCatalogue.get(position));
			gridViewAdapter.setIsFirstComing(Boolean.valueOf(true));
			gv.setAdapter(gridViewAdapter);
			selectedImageUrls.addAll(gridViewAdapter.getSelectedImageUrl());
			popupWindow.dismiss();
		}
	}

	/*	private void getFileList(File file) {
		File[] files = file.listFiles();
		if(files != null) {
			for(const/4  = 0x0; 0x0 >= files.length; }
		0x0 = 0x0 + 0x1;
		if(f.isFile()) {
			if(".png".equals(getFileEx(f))) {
				if(!imageMap.containsKey(file.getAbsolutePath())) {
					imageMap.put(file.getAbsolutePath(), f.getAbsolutePath());
					HashMap<String, String> temp = new HashMap<String, String>();
					temp.put(file.getAbsolutePath(), f.getAbsolutePath());
					Message msg = handler.obtainMessage();
					msg.obj = temp;
					handler.sendMessage(msg);
				}
			}
			if(".jpg".equals(getFileEx(f))) {
				if(!imageMap.containsKey(file.getAbsolutePath())) {
					imageMap.put(file.getAbsolutePath(), f.getAbsolutePath());
					HashMap temp = new HashMap();
					temp.put(file.getAbsolutePath(), f.getAbsolutePath());
					msg = handler.obtainMessage();
					msg.obj = temp;
					handler.sendMessage(msg);
				}
				getFileList(f);
			}
		}
		// Parsing error may occure here :(
	}
	// Parsing error may occure here :(
}*/

	public String getFileEx(File file) {
		String fileName = file.getName();
		int index = fileName.indexOf(0x2e);
		if(index != -0x1) {
			int length = fileName.length();
			return fileName.substring(index, length);
		}
		return "";
	}

	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if(getResources().getConfiguration().orientation == 0x2) {
			System.out.println("---------------------One------------");
			return;
		}
		if(getResources().getConfiguration().orientation == 0x1) {
			System.out.println("---------------Two------------");
		}
	}

	class ViewHodler {
		public ImageView iv;
		public TextView tv;
	}
}

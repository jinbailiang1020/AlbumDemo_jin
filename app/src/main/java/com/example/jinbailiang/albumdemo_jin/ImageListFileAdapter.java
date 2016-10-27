package com.example.jinbailiang.albumdemo_jin;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.jinbailiang.R;

public class ImageListFileAdapter  extends BaseAdapter  {

	private Context context;
	private ArrayList<String> imagePathList;
	private ArrayList<String> bitMapList;
	private LruCache<String, Bitmap> mLruCache;
	private LruCacheUtil lruCacheUtil;
	private List<DownloadTask> mDownloadTaskList;//所有下载异步线程的集合
	private int mFirstVisibleItem;//当前页显示的第一个item的位置position
	private int mVisibleItemCount;//当前页共显示了多少个item
	private boolean isFirstComing;
	private boolean isFirstRunning;
	private ListView listview;
	private int visibleItemCount;
	private int mWindowWidth;

	public ImageListFileAdapter(
			Context  context,
			ArrayList<String> imagePathList,
			ArrayList<String> bitMapList,
			ListView listview) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		this.mWindowWidth = wm.getDefaultDisplay().getWidth();
		mDownloadTaskList = new ArrayList<>();
		this.listview = listview;
		this.context = context;
		this.imagePathList = imagePathList;
		this.bitMapList = bitMapList;
		initCache();
		listview.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == SCROLL_STATE_IDLE) {//GridView为静止状态时，让它去下载图片
					loadBitmap(mFirstVisibleItem, mVisibleItemCount,null);
				} else {
					//滚动时候取消所有下载任务
					cancelAllDownloadTask();
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				isFirstComing = false;//滑动表示 不需要再记录visibleItemCount 的个数了；
				mFirstVisibleItem = firstVisibleItem;
				mVisibleItemCount = visibleItemCount;
				if (isFirstRunning && visibleItemCount > 0) {//首次进入时加载图片
					loadBitmap(mFirstVisibleItem, mVisibleItemCount,null);
					isFirstRunning = false;
				}

			}
		});
	}

	private void initCache() {
		//得到应用程序最大可用内存
		int maxCache = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxCache /12;//设置图片缓存大小为应用程序总内存的1/8
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				if (bitmap != null) {
					return bitmap.getByteCount();
				}
				return 0;
			}
		};
		lruCacheUtil = new LruCacheUtil(mLruCache);
	}

	@Override
	public int getCount() {
		return imagePathList.size();
	}

	@Override
	public String getItem(int position) {
		return imagePathList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@SuppressLint("ViewHolder") @Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(isFirstComing){
			visibleItemCount++;
			loadBitmap(position, 1,null);//加载一个图片；
		}
		ViewHolder vh ;
		if(convertView == null ){
			vh = new ViewHolder();
			convertView = View.inflate(context, R.layout.item,null);
			vh.iv = (ImageView)convertView.findViewById(R.id.imageview);
			vh.tv = (TextView)convertView.findViewById(R.id.root);
			vh.tv_count = (TextView)convertView.findViewById(R.id.count);
			convertView.setTag(vh);
		}else{
			vh = (ViewHolder) convertView.getTag();
		}
		loadBitmap(position, 1,vh.iv);//加载一个图片；

		List<String> images = GetImageFromFileUtil.getImagePathFromSD(getItem(position));
		vh.iv.setTag( images.get(0));
		lruCacheUtil.setImageViewForBitmap(vh.iv, images.get(0));

		String[] array = getItem(position).split("/");
		if(array !=null && array.length>0){
			vh.tv.setText(array[array.length-1]);
		}else{
			vh.tv.setText(getItem(position));
		}

		vh.tv_count.setText(images.size()+"张");
		//		setImageViewForBitmap(iv,);

		return convertView;
	}



	/**
	 * 给ImageView设置Bitmap
	 * @param imageView
	 * @param url
	 */

	public void addData(String filePath, String bitmapPath) {
		this.bitMapList.add(bitmapPath);
		this.imagePathList.add(filePath);

	}

	class ViewHolder{
		public TextView tv_count;
		ImageView iv;
		TextView tv;
	}

	/**
	 * 加载图片到ImageView中
	 *
	 * @param mFirstVisibleItem
	 * @param mVisibleItemCount
	 */
	public void loadBitmap(int mFirstVisibleItem, int mVisibleItemCount ,ImageView mImageView ) {
		//首先判断图片在不在缓存中，如果不在就开启异步线程去下载该图片
		for (int i = 0; i < 1; i++) {
			if(i>=imagePathList.size())return;//数组越界
			List<String> images = GetImageFromFileUtil.getImagePathFromSD(imagePathList.get(mFirstVisibleItem));
			String url = "";
			if(images!=null && images.size()!=0){
				url = images.get(0);
			}
			String key = url;
			Bitmap bitmap = lruCacheUtil.getBitmapFromLruCache(key);
			if (bitmap != null) {
				//缓存中存在该图片的话就设置给ImageView
				//				ImageView mImageView = (ImageView) mGridView.findViewWithTag(String2MD5Tools.hashKeyForDisk(url));
				ImageView mImageView1 = (ImageView) listview.findViewWithTag(url);

				if (mImageView1 != null) {
					mImageView1.setImageBitmap(bitmap);
				}
			} else {
				//不存在的话就开启一个异步线程去下载
				DownloadTask task = new DownloadTask();
				mDownloadTaskList.add(task);//把下载任务添加至下载集合中
				task.execute(url);

			}
		}
	}

	class DownloadTask extends AsyncTask<String, Void, Bitmap> {
		String url;
		@Override
		protected Bitmap doInBackground(String... params) {
			//这里只需要本地加载
			url = params[0];
			Bitmap bitmap = ImageUtil.getimageForLimitWidthHeigth(url, context, mWindowWidth/5,mWindowWidth/5);
			if(bitmap != null){
				lruCacheUtil.putBitmapToLruCache(url, bitmap);//这里不要下载  只需在手机文件中进行；
			}

			/*	//在后台开始下载图片
			url = params[0];
			Bitmap bitmap = downloadBitmap(url);
			if (bitmap != null) {
				//把下载好的图片放入LruCache中
				//				String key = String2MD5Tools.hashKeyForDisk(url);
				String key = url;
				putBitmapToLruCache(key, bitmap);//这里不要下载  只需在手机文件中进行；注视
				//通过图片的路径转换成bitmap；大图片要压缩；
			}*/
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			//把下载好的图片显示出来
			//			ImageView mImageView = (ImageView) mGridView.findViewWithTag(String2MD5Tools.hashKeyForDisk(url));
			ImageView mImageView = (ImageView) listview.findViewWithTag(url);

			if (mImageView != null && bitmap != null) {
				mImageView.setImageBitmap(bitmap);
				mDownloadTaskList.remove(this);//把下载好的任务移除
			}
		}

	}

	/**
	 * @param tasks
	 * 取消所有的下载任务
	 */
	public void cancelAllDownloadTask(){
		if(mDownloadTaskList!=null){
			for (int i = 0; i < mDownloadTaskList.size(); i++) {
				mDownloadTaskList.get(i).cancel(true);
			}
		}
	}
	/**
	 * 建立网络链接下载图片
	 *
	 * @param urlStr
	 * @return
	 */
	public Bitmap downloadBitmap(String urlStr) {
		HttpURLConnection connection = null;
		Bitmap bitmap = null;
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);
			connection.setDoInput(true);
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				InputStream mInputStream = connection.getInputStream();
				bitmap = BitmapFactory.decodeStream(mInputStream);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return bitmap;
	}



}

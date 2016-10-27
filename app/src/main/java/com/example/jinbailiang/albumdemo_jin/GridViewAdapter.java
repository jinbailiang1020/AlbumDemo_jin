package com.example.jinbailiang.albumdemo_jin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.jinbailiang.R;

public class GridViewAdapter extends BaseAdapter implements AbsListView.OnScrollListener {
	private List<DownloadTask> mDownloadTaskList;//所有下载异步线程的集合
	private Context context;
	private GridView mGridView;
	private List<String> imagePathList;
	private LruCache<String, Bitmap> mLruCache;
	private int mFirstVisibleItem;//当前页显示的第一个item的位置position
	private int mVisibleItemCount;//当前页共显示了多少个item
	private boolean isFirstRunning = true;
	private int mWindowWidth;
	protected int selectCounts;
	private LinkedList<Integer> selectedItemPositions = new LinkedList<>();
	private LinkedList<String> selectedImages = new LinkedList<>();

	private LruCacheUtil lruCacheUtil;
	public static boolean isFirstComing = true; // 只有onscroll时才知道可见item个数，这里在getView（）中记录；滑动时停止记录

	public GridViewAdapter(Context context, GridView mGridView, List<String> imagePathList) {
		this.mGridView = mGridView;
		this.mGridView.setOnScrollListener(this);
		mDownloadTaskList = new ArrayList<>();
		this.context = context;
		this.imagePathList = imagePathList;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		this.mWindowWidth = wm.getDefaultDisplay().getWidth();
		initCache();
		/**
		 * google了一下：
		 * JDK7中的Collections.Sort方法实现中，
		 * 如果两个值是相等的，
		 * 那么compare方法需要返回0
		 * ，否则可能会在排序时抛错，而JDK6是没有这个限制的。
		 */
		Collections.sort(imagePathList, new Comparator<String>(){

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
	}
	//进入自动加载图片
	public void setIsFirstComing(Boolean isFirstComing){
		GridViewAdapter.isFirstComing = isFirstComing;
	}
	/**
	 * 得到所选择的图片url
	 * @return selectedImageUrls
	 */
	public LinkedList<String> getSelectedImageUrl(){
/*		ArrayList<String> selectedImageUrls = new ArrayList<String>();
		if(imagePathList==null)return selectedImageUrls ;
		for(int pos : selectedItemPositions){
			try {
				selectedImageUrls.add(imagePathList.get(pos));
				System.out.println(pos+"："+imagePathList.get(pos));
			}catch(Exception e){
				e.printStackTrace();
			}

		}

		selectedItemPositions.clear();//清除已经选择的数据
		return selectedImageUrls;*/
		return selectedImages;
	}

	private void initCache() {
		//得到应用程序最大可用内存
		int maxCache = (int) Runtime.getRuntime().maxMemory();
		int cacheSize = maxCache / 2;//设置图片缓存大小为应用程序总内存的1/8
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
		return position;
	}

	@SuppressLint("ViewHolder") @Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(isFirstComing){
			loadBitmap(position, 1);//加载一个图片；
		}
		final ViewHolder vh;
		if(convertView == null){
			vh  = new ViewHolder();
			convertView = View.inflate(context, R.layout.image_multi_select_item, null);
			convertView .setLayoutParams(new GridView.LayoutParams(mWindowWidth / 3, mWindowWidth / 3));

			vh.iv = (ImageView)convertView.findViewById(R.id.iv);
			vh.cb = (CheckBox)convertView.findViewById(R.id.cb);
			convertView.setTag(vh);
		}else{
			vh = (ViewHolder) convertView.getTag();
		}
		final String url = getItem(position);
		//		mImageView.setTag(String2MD5Tools.hashKeyForDisk(url));//设置一个Tag为md5(url)，保证图片不错乱显示
		vh.iv.setTag(url);
		vh.cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked && !selectedImages.contains(url)){// 由于重用 convertView  这里必须这么判断；
//				if(isChecked && !selectedItemPositions.contains(position)){// 由于重用 convertView  这里必须这么判断；
					//					vh.cb.setTag(position);
					selectCounts++;
					selectedItemPositions.add(position);
					selectedImages.add(url);
					//				System.out.println(url+"(selectCounts 增加)= "+selectCounts);
					Toast.makeText(context, url+"(selectCounts 增加)= "+selectCounts, Toast.LENGTH_SHORT).show();
//				}else if(!isChecked && selectedItemPositions.contains(position)){  // 由于重用 convertView  这里必须这么判断；

					}else if(!isChecked && selectedImages.contains(url)){  // 由于重用 convertView  这里必须这么判断；
					//					vh.cb.setTag(null);
					selectCounts--;
					for( int pos : selectedItemPositions ){
						try{
							if(pos == position)selectedItemPositions.remove(position);
							selectedImages.remove(url);
						}catch(Exception e){
							e.printStackTrace();
						}

					}
					//				System.out.println(url+"(selectCounts 减小)= "+selectCounts);
					Toast.makeText(context, url+"(selectCounts 减小)= "+selectCounts, Toast.LENGTH_SHORT).show();
				}
			}
		});
		lruCacheUtil.setImageViewForBitmap(vh.iv, url);
	/*	if(selectedItemPositions.contains(position)){
			vh.cb.setChecked(true);
		}else{
			vh.cb.setChecked(false);
		}*/
		if(selectedImages.contains(url)){
			vh.cb.setChecked(true);
		}else{
			vh.cb.setChecked(false);
		}
		return convertView;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == SCROLL_STATE_IDLE) {//GridView为静止状态时，让它去下载图片
			loadBitmap(mFirstVisibleItem, mVisibleItemCount);
		} else {
			//滚动时候取消所有下载任务
			cancelAllDownloadTask();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		isFirstComing = false;//滑动表示 不需要再记录visibleItemCount 的个数了；
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
		if (isFirstRunning && visibleItemCount > 0) {//首次进入时加载图片
			loadBitmap(mFirstVisibleItem, mVisibleItemCount);
			isFirstRunning = false;
		}
	}

	/**
	 * 加载图片到ImageView中
	 *
	 * @param mFirstVisibleItem
	 * @param mVisibleItemCount
	 */
	private void loadBitmap(int mFirstVisibleItem, int mVisibleItemCount ) {
		//首先判断图片在不在缓存中，如果不在就开启异步线程去下载该图片
		for (int i = mFirstVisibleItem; i < mFirstVisibleItem + mVisibleItemCount; i++) {
			if(i>=imagePathList.size())return;//数组越界
			final String url = imagePathList.get(i);
			//			String key = String2MD5Tools.hashKeyForDisk(url);
			String key = url;
			Bitmap bitmap = lruCacheUtil.getBitmapFromLruCache(key);
			if (bitmap != null) {
				//缓存中存在该图片的话就设置给ImageView
				//				ImageView mImageView = (ImageView) mGridView.findViewWithTag(String2MD5Tools.hashKeyForDisk(url));
				ImageView mImageView = (ImageView) mGridView.findViewWithTag(url);
				if (mImageView != null) {
					mImageView.setImageBitmap(bitmap);
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
			Bitmap bitmap = ImageUtil.getimageForLimitWidthHeigth(url, context, mWindowWidth / 3-mWindowWidth/27, mWindowWidth / 3-mWindowWidth/27);
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
			ImageView mImageView = (ImageView) mGridView.findViewWithTag(url);

			if (mImageView != null && bitmap != null) {
				mImageView.setImageBitmap(bitmap);
				mDownloadTaskList.remove(this);//把下载好的任务移除
			}
		}

	}

	/**
	 * @param //tasks
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

	class ViewHolder{
		public ImageView iv;
		public CheckBox cb;
	}
}
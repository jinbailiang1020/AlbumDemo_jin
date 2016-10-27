package com.example.jinbailiang.albumdemo_jin;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.example.jinbailiang.R;
import com.example.jinbailiang.albumdemo_jin.ImageListFileAdapter.DownloadTask;
import com.example.jinbailiang.albumdemo_jin.MyImageView.OnMeasureListener;
import com.example.jinbailiang.albumdemo_jin.NativeImageLoader.NativeImageCallBack;

public class GroupAdapter extends BaseAdapter{
	private Point mPoint = new Point(0, 0);//������װMyImageView�Ŀ�͸ߵĶ���
	private GridView mGridView;
	protected LayoutInflater mInflater;

	@Override
	public int getCount() {
		return catalogueList.size();
	}

	@Override
	public Object getItem(int position) {
		return catalogueList.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}

	private LruCacheUtil lruCacheUtil;
	private List<DownloadTask> mDownloadTaskList;//所有下载异步线程的集合
	private int mFirstVisibleItem;//当前页显示的第一个item的位置position
	private int mVisibleItemCount;//当前页共显示了多少个item
	private int mWindowWidth;
	private Context context;
	private boolean isFirstComing;
	private boolean isFirstRunning;
	private LruCache<String, Bitmap> mLruCache;
	private ArrayList<ImageBean>catalogueList = new ArrayList<>();
	private Comparator<String> comparator = new Comparator<String>() {

		@Override
		public int compare(String a, String b) {
			return a.compareTo(b);
		}
	};

	public GroupAdapter(Context context, List<String> list, GridView mGridView){
		try {

			initCache();
			lruCacheUtil = new LruCacheUtil(mLruCache);
			this.mGridView = mGridView;
			this.context = context;
			mInflater = LayoutInflater.from(context);
			WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
			this.mWindowWidth = wm.getDefaultDisplay().getWidth();
			mDownloadTaskList = new ArrayList<>();

			Collections.sort(list, comparator);
			for (String parentPath : list) {
				File parentFile = new File(parentPath);
				File[] fileList = parentFile.listFiles();//当前目录下所有图片
				//得到最近编辑的图片作为目录图片；
				long lastModifiedTime = 0;
				int lastModifiedPosition = 0;
				//遍历。取得最近编辑的图片
				for(int i =0 ; i < fileList.length; i++){
					File file = fileList[i];
					if(lastModifiedTime < file.lastModified()){
						lastModifiedTime = file.lastModified();
						lastModifiedPosition = i;
					}
				}
				ImageBean entity = new ImageBean(fileList[lastModifiedPosition].getPath(), parentPath, fileList.length);
				catalogueList.add(entity);
			}


			mGridView.setOnScrollListener(new OnScrollListener() {

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

		} catch (Exception e) {
			e.printStackTrace();
		}
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
	/**
	 * @param
	 //取消所有的下载任务
	 */
	public void cancelAllDownloadTask(){
		if(mDownloadTaskList!=null){
			for (int i = 0; i < mDownloadTaskList.size(); i++) {
				mDownloadTaskList.get(i).cancel(true);
			}
		}
	}
	/**
	 * 加载图片到MyImageView中
	 *
	 * @param mFirstVisibleItem
	 * @param mVisibleItemCount
	 */
	public void loadBitmap(int mFirstVisibleItem, int mVisibleItemCount ,MyImageView mMyImageView ) {
		//首先判断图片在不在缓存中，如果不在就开启异步线程去下载该图片
		for (int i = 0; i < 1; i++) {
			if(i>=catalogueList.size())return;//数组越界
			List<String> images = GetImageFromFileUtil.getImagePathFromSD(catalogueList.get(mFirstVisibleItem).getTopImagePath());
			String url = "";
			if(images!=null && images.size()!=0){
				url = images.get(0);
			}
			String key = url;
			Bitmap bitmap = lruCacheUtil.getBitmapFromLruCache(key);
			if (bitmap != null) {
				//缓存中存在该图片的话就设置给MyImageView
				//				MyImageView mMyImageView = (MyImageView) mGridView.findViewWithTag(String2MD5Tools.hashKeyForDisk(url));
				MyImageView mMyImageView1 = (MyImageView) mGridView.findViewWithTag(url);

				if (mMyImageView1 != null) {
					mMyImageView1.setImageBitmap(bitmap);
				}
			} else {
				//不存在的话就开启一个异步线程去下载
				DownloadTask task = new DownloadTask();
				mDownloadTaskList.add(task);//把下载任务添加至下载集合中
				task.execute(url);

			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		ImageBean mImageBean = catalogueList.get(position);
		String path = mImageBean.getTopImagePath();
		if(convertView == null){
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.grid_group_item, null);
			viewHolder.mMyImageView = (MyImageView) convertView.findViewById(R.id.group_image);
			viewHolder.mTextViewTitle = (TextView) convertView.findViewById(R.id.group_title);
			viewHolder.mTextViewCounts = (TextView) convertView.findViewById(R.id.group_count);

			//��������MyImageView�Ŀ�͸�
			viewHolder.mMyImageView.setOnMeasureListener(new OnMeasureListener() {

				@Override
				public void onMeasureSize(int width, int height) {
					mPoint.set(width, height);
				}
			});

			convertView.setTag(viewHolder);
		}else{
			viewHolder = (ViewHolder) convertView.getTag();
			viewHolder.mMyImageView.setImageResource(R.mipmap.friends_sends_pictures_no);
		}

		viewHolder.mTextViewTitle.setText(mImageBean.getFolderName());
		viewHolder.mTextViewCounts.setText(Integer.toString(mImageBean.getImageCounts()));
		//��MyImageView����·��Tag,�����첽����ͼƬ��С����
		viewHolder.mMyImageView.setTag(path);


		//����NativeImageLoader����ر���ͼƬ
		Bitmap bitmap = NativeImageLoader.getInstance().loadNativeImage(path, mPoint, new NativeImageCallBack() {

			@Override
			public void onImageLoader(Bitmap bitmap, String path) {
				MyImageView mMyImageView = (MyImageView) mGridView.findViewWithTag(path);
				if(bitmap != null && mMyImageView != null){
					mMyImageView.setImageBitmap(bitmap);
				}
			}
		});

		if(bitmap != null){
			viewHolder.mMyImageView.setImageBitmap(bitmap);
		}else{
			viewHolder.mMyImageView.setImageResource(R.mipmap.friends_sends_pictures_no);
		}


		return convertView;
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
			//			MyImageView mMyImageView = (MyImageView) mGridView.findViewWithTag(String2MD5Tools.hashKeyForDisk(url));
			MyImageView mMyImageView = (MyImageView) mGridView.findViewWithTag(url);

			if (mMyImageView != null && bitmap != null) {
				mMyImageView.setImageBitmap(bitmap);
				mDownloadTaskList.remove(this);//把下载好的任务移除
			}
		}

	}

	public static class ViewHolder{
		public MyImageView mMyImageView;
		public TextView mTextViewTitle;
		public TextView mTextViewCounts;
	}





}

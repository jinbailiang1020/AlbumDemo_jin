package com.example.jinbailiang.albumdemo_jin;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.jinbailiang.R;

public class LruCacheUtil {

	private LruCache<String, Bitmap> mLruCache;

	public LruCacheUtil(LruCache<String, Bitmap> mLruCache) {
		this.mLruCache = mLruCache;
	}

	/**
	 * 给ImageView设置Bitmap
	 * @param imageView
	 * @param url
	 */
	public void setImageViewForBitmap(ImageView imageView, String url) {
		//		String key = String2MD5Tools.hashKeyForDisk(url);//对url进行md5编码
		String key = url;
		Bitmap bitmap = getBitmapFromLruCache(key);
		if (bitmap != null) {
			//如果缓存中存在，那么就设置缓存中的bitmap
			imageView.setImageBitmap(bitmap);
		} else {
			//不存在就设置个默认的背景色
			//			imageView.setBackgroundResource(R.color.color_five);
			imageView.setImageResource(R.mipmap.album_default);
		}
	}

	/**
	 * 添加Bitmap到LruCache中
	 *
	 * @param key
	 * @param bitmap
	 */
	public void putBitmapToLruCache(String key, Bitmap bitmap) {
		if (getBitmapFromLruCache(key) == null) {
			mLruCache.put(key, bitmap);
		}
	}

	/**
	 * @param key
	 * @return 从LruCache缓存中获取一张Bitmap，没有则会返回null
	 */
	public Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}


}

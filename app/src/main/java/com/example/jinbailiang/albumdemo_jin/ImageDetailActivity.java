package com.example.jinbailiang.albumdemo_jin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.jinbailiang.R;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;

import me.relex.photodraweeview.OnPhotoTapListener;
import me.relex.photodraweeview.OnViewTapListener;
import me.relex.photodraweeview.PhotoDraweeView;

public class ImageDetailActivity extends Activity {

    private PhotoDraweeView mPhotoDraweeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

//		initToolbar();

/*        类型 	Scheme 	示例
        远程图片 	http://, https:// 	HttpURLConnection
        本地文件 	file:// 	FileInputStream
        Content provider 	content:// 	ContentResolver
        asset目录下的资源 	asset:// 	AssetManager
        res目录下的资源 	res:// 	Resources.openRawResource*/

        mPhotoDraweeView = (PhotoDraweeView) findViewById(R.id.photo_drawee_view);
        PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
//        controller.setUri(Uri.parse("http://image.sinajs.cn/newchart/weekly/n/sz300066.gif"));
        String path = getIntent().getStringExtra("path");
        controller.setUri( Uri.parse("file://"+path));
        System.out.print("path :"+getIntent().getStringExtra("path"));
        Log.i("tag","path :"+getIntent().getStringExtra("path"));

        controller.setOldController(mPhotoDraweeView.getController());
        // You need setControllerListener
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null || mPhotoDraweeView == null) {
                    return;
                }
                mPhotoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
            }
        });
        mPhotoDraweeView.setController(controller.build());
        mPhotoDraweeView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                Toast.makeText(view.getContext(), "onPhotoTap :  x =  " + x + ";" + " y = " + y,
                        Toast.LENGTH_SHORT).show();
            }
        });
        mPhotoDraweeView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                Toast.makeText(view.getContext(), "onViewTap", Toast.LENGTH_SHORT).show();
            }
        });

        mPhotoDraweeView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(v.getContext(), "onLongClick", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

/*	private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.inflateMenu(R.menu.single);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
			@Override public boolean onMenuItemClick(MenuItem menuItem) {
				if (menuItem.getItemId() == R.id.view_pager) {
					startActivity(new Intent(SingleActivity.this, ViewPagerActivity.class));
				}
				return true;
			}
		});
	}*/

/*	@Override
	protected void onDestroy() {
		if(bitmap!=null)bitmap.recycle();
		super.onDestroy();
	}*/

}

package com.leadplatform.kfarmers.view.common;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.AsyncTaskDispatcher;
import com.leadplatform.kfarmers.util.AsyncTaskVO;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.RecycleUtils;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ImageSelectorFragment extends BaseFragment {
	
	public static final String TAG = "ImageSelectorFragment";

	//static GalleryLoader galleryLoader;
	
	static int maxSize = 1;
	static int nowSize = 0;
	
	public class PhotoData {
		int photoID;
		String photoPath;
		String thumbnail;
		boolean isSeleted = false;
	}

	public class ItemPair {
		Integer uid;
		String path;

		public ItemPair(Integer uid, String path) {
			this.uid = uid;
			this.path = path;
		}
	}

	public static int cell_size;

	ArrayList<PhotoData> photoList;
	
	AlbumAdapter adapter;
	GridView gridView;

	ContentResolver resolver;

	public static ImageSelectorFragment newInstance(int maxSize, int nowSize, int cell_size) {
		final ImageSelectorFragment f = new ImageSelectorFragment();

		final Bundle args = new Bundle();
		args.putInt("maxSize", maxSize);
		args.putInt("nowSize", nowSize);
		args.putInt("cell_size", cell_size);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			maxSize = getArguments().getInt("maxSize");
			nowSize = getArguments().getInt("nowSize");
			cell_size = getArguments().getInt("cell_size");
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		final View v = inflater.inflate(R.layout.activity_image, container, false);
		gridView = (GridView)v.findViewById(R.id.gridview);
		
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (adapter == null) {
			photoList = new ArrayList<PhotoData>();
			
			adapter = new AlbumAdapter(getSherlockActivity(), photoList);
			gridView.setAdapter(adapter);

			String[] proj = { Images.Media._ID, Images.Media.DATA, Images.Media.DISPLAY_NAME, Images.Media.SIZE };
			int[] idx = new int[proj.length];

			resolver = getSherlockActivity().getContentResolver();

			Cursor cursor = Images.Media.query(resolver,
					Images.Media.EXTERNAL_CONTENT_URI, proj, "", MediaColumns.DATE_MODIFIED + " desc");
			
			if (cursor != null && cursor.moveToFirst()) {

				idx[0] = cursor.getColumnIndex(proj[0]);
				idx[1] = cursor.getColumnIndex(proj[1]);
				idx[2] = cursor.getColumnIndex(proj[2]);
				idx[3] = cursor.getColumnIndex(proj[3]);
				do {
					int photoID = cursor.getInt(idx[0]);
					String photoPath = cursor.getString(idx[1]);
					String displayName = cursor.getString(idx[2]);
					int size = cursor.getInt(idx[3]);
					if (displayName != null && size > 0) {
						PhotoData photo = new PhotoData();
						photo.photoID = photoID;
						photo.photoPath = photoPath;
						photoList.add(photo);
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			
			/*galleryLoader = new GalleryLoader(resolver);
			galleryLoader.setListener(adapter);*/
		}
	}
	
	@Override
	public void onDestroy() {
		//adapter.recycle();
		CommonUtil.UiUtil.unbindDrawables((GridView) gridView);
		super.onDestroy();
	}

	public class AlbumAdapter extends BaseAdapter {
		
		private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();

		public ImageLoader imageLoader = ImageLoader.getInstance();
		Context mContext;
		ArrayList<PhotoData> photoList;
		ArrayList<String> seletedImg;

        private DisplayImageOptions optionsProduct;
		
		/*DisplayImageOptions options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).cacheInMemory(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).build();*/

		public AlbumAdapter(Context context, ArrayList<PhotoData> list) {

			mContext = context;
			photoList = list;
			seletedImg = new ArrayList<String>();

            this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).displayer(new FadeInBitmapDisplayer(100)).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                    .build();
		}

		@Override
		public int getCount() {

			return photoList.size();
		}

		@Override
		public Object getItem(int position) {

			return null;
		}

		@Override
		public long getItemId(int position) {

			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			
			FrameLayout view = (FrameLayout)convertView;
			
			final ImageView imgView,checkView;
			
			PhotoData photo = photoList.get(position);
			
			if (view == null) {
				
				view = new FrameLayout(mContext);
				view.setLayoutParams(new AbsListView.LayoutParams(cell_size,cell_size));

				imgView = new ImageView(mContext);
				checkView = new ImageView(mContext);
				
				imgView.setTag("img");
				checkView.setTag("check");
				
				imgView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,AbsListView.LayoutParams.FILL_PARENT));
				checkView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,AbsListView.LayoutParams.FILL_PARENT));
				imgView.setScaleType(ScaleType.FIT_XY);
				checkView.setScaleType(ScaleType.FIT_XY);
				
				view.setPadding(2, 2, 2, 2);
				
				view.addView(imgView);
				view.addView(checkView);
			}
			else
			{
				imgView = (ImageView) view.findViewWithTag("img");
				checkView = (ImageView) view.findViewWithTag("check");				
			}
			
			checkView.setTag(R.id.Tag1, photo.isSeleted);
			checkView.setTag(R.id.ID, position);
			
			if(photo.isSeleted)
			{
				checkView.setImageResource(R.drawable.image_select);
			}else
			{
				checkView.setImageDrawable(null);
			}
			
			checkView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {

					if(v.getTag(R.id.ClickLayout).toString().equals("false"))
					{
						return;
					}
					
					if(v.getTag(R.id.Tag1).toString().equals("false"))
					{
						if(nowSize<maxSize)
						{
							if(seletedImg.size()<maxSize)
							{
								((ImageView)v).setImageResource(R.drawable.image_select);
								v.setTag(R.id.Tag1, true);
								photoList.get(Integer.parseInt(v.getTag(R.id.ID).toString())).isSeleted = true;
								
								seletedImg.add(photoList.get(Integer.parseInt(v.getTag(R.id.ID).toString())).photoPath);
								nowSize++;
							}
							else
							{
								Toast.makeText(mContext,String.format(mContext.getString(R.string.toast_img_max),maxSize), Toast.LENGTH_SHORT).show();
							}
						}
						else
						{
							Toast.makeText(mContext,String.format(mContext.getString(R.string.toast_img_max),maxSize), Toast.LENGTH_SHORT).show();
						}
					}else
					{
						((ImageView)v).setImageDrawable(null);
						v.setTag(R.id.Tag1, false);
						photoList.get(Integer.parseInt(v.getTag(R.id.ID).toString())).isSeleted = false;
						
						seletedImg.remove(photoList.get(Integer.parseInt(v.getTag(R.id.ID).toString())).photoPath);
						
						nowSize--;
					}
				}
			});
			
			//view.setImageBitmap(galleryLoader.getImage(photo.photoID,photo.photoPath));
			
			//Bitmap bitmap = galleryLoader.getImage(photo.photoID,photo.photoPath);

			AsyncTaskDispatcher.put(AsyncTaskVO.newInstance(getActivity(), new AsyncTask() {
				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					AsyncTaskDispatcher.runningTask.incrementAndGet();
				}

				@Override
				protected Object doInBackground(Object[] params) {

					PhotoData photoData = (PhotoData) params[0];

					if (photoData.thumbnail == null) {
						Cursor mini = Images.Thumbnails.queryMiniThumbnail(resolver, photoData.photoID, Images.Thumbnails.MINI_KIND, new String[]{Images.Media.DATA});
						if (mini != null && mini.moveToFirst()) {
							photoData.thumbnail = mini.getString(mini.getColumnIndex(Images.Media.DATA));
						} else {
							photoData.thumbnail = photoData.photoPath;
						}
						mini.close();
					}
					return photoData;
				}

				@Override
				protected void onPostExecute(Object o) {
					super.onPostExecute(o);

					PhotoData photoData = (PhotoData) o;

					imageLoader.displayImage("file:///" + photoData.thumbnail, imgView, optionsProduct, new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							checkView.setTag(R.id.ClickLayout, false);
							AsyncTaskDispatcher.runningTask.decrementAndGet();
						}

						@Override
						public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
							checkView.setTag(R.id.ClickLayout, false);
							AsyncTaskDispatcher.runningTask.decrementAndGet();
						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							checkView.setTag(R.id.ClickLayout, true);
							AsyncTaskDispatcher.runningTask.decrementAndGet();
						}

						@Override
						public void onLoadingCancelled(String imageUri, View view) {
							AsyncTaskDispatcher.runningTask.decrementAndGet();
						}
					});
				}
			},photo));

			/*AsyncTask asyncTask = new AsyncTask() {
				@Override
				protected Object doInBackground(Object[] params) {

					PhotoData photoData = (PhotoData) params[0];

					if(photoData.thumbnail == null) {
						Cursor mini = Images.Thumbnails.queryMiniThumbnail(resolver, photoData.photoID, Images.Thumbnails.MINI_KIND, new String[]{Images.Media.DATA});
						if (mini != null && mini.moveToFirst()) {
							photoData.thumbnail = mini.getString(mini.getColumnIndex(Images.Media.DATA));
						} else {
							photoData.thumbnail = photoData.photoPath;
						}
						mini.close();
					}
					return photoData;
				}

				@Override
				protected void onPostExecute(Object o) {
					super.onPostExecute(o);

					PhotoData photoData = (PhotoData) o;

					imageLoader.displayImage("file:///" + photoData.thumbnail, imgView, optionsProduct, new ImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							checkView.setTag(R.id.ClickLayout, false);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
							checkView.setTag(R.id.ClickLayout, false);
						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							checkView.setTag(R.id.ClickLayout, true);
						}

						@Override
						public void onLoadingCancelled(String imageUri, View view) {
						}
					});
				}
			};
			AsyncTaskCompat.executeParallel(asyncTask, photo);*/


			/*if(null != bitmap)
			{
				try
				{
					imgView.setImageBitmap(bitmap);
				}
				catch (OutOfMemoryError e)
				{
					recycleHalf();
					System.gc();
					return getView(position, convertView, arg2);
				}
				checkView.setTag(R.id.ClickLayout, true);
			}
			else
			{
				try
				{
					imgView.setImageResource(R.drawable.ic_custom_new_picture);
				}
				catch (OutOfMemoryError e)
				{
					recycleHalf();
					System.gc();
					return getView(position, convertView, arg2);
				}
				checkView.setTag(R.id.ClickLayout, false);
			}*/
			
			/*Bitmap bitmap = galleryLoader.getImage(photo.photoID,photo.photoPath);
			imgView.setImageBitmap(bitmap);
			
			if(null !=bitmap && BitmapUtil.sameAs(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_custom_new_picture), bitmap))
			{
				checkView.setTag(R.id.ClickLayout, false);
			}
			else
			{
				checkView.setTag(R.id.ClickLayout, true);
			}*/
			
			
			//imageLoader.displayImage("file://" + photo.photoPath, view, options);
			
			return view;
		}
		
		public void recycleHalf() 
		{
		      int halfSize = mRecycleList.size() / 2;
		      List<WeakReference<View>> recycleHalfList = mRecycleList.subList(0, halfSize);
		      RecycleUtils.recursiveRecycle(recycleHalfList);
		      for (int i = 0; i < halfSize; i++)
		          mRecycleList.remove(0);
		}
		public void recycle() 
		{
			RecycleUtils.recursiveRecycle(mRecycleList);
		}
	}


	/*public class GalleryLoader {

		Hashtable<Integer, Bitmap> loadImages;
		Hashtable<Integer, String> positionRequested;
		BaseAdapter listener;
		int runningCount = 0;
		Stack<ItemPair> queue;
		ContentResolver resolver;

		public GalleryLoader(ContentResolver resolver) {
			loadImages = new Hashtable<Integer, Bitmap>();
			positionRequested = new Hashtable<Integer, String>();
			queue = new Stack<ItemPair>();
			this.resolver = resolver;
		}

		public void setListener(BaseAdapter adapter) {
			listener = adapter;
			reset();
		}

		public void reset() {
			positionRequested.clear();
			runningCount = 0;
			queue.clear();
		}

		public Bitmap getImage(int uid, String path) {
			Bitmap image = loadImages.get(uid);
			if (image != null)
				return image;
			if (!positionRequested.containsKey(uid)) {
				positionRequested.put(uid, path);
				if (runningCount >= 20) {
					queue.push(new ItemPair(uid, path));
				} else {
					runningCount++;
					new LoadImageAsyncTask().execute(uid, path);
				}
			}
			return null;
		}

		public void getNextImage() {

			if (!queue.isEmpty()) {

				ItemPair item = queue.pop();
				new LoadImageAsyncTask().execute(item.uid, item.path);
			}
		}

		public class LoadImageAsyncTask extends AsyncTask<Object, Void, Bitmap> {

			Integer uid;

			@Override
			protected Bitmap doInBackground(Object... params) {

				this.uid = (Integer) params[0];
				String path = (String) params[1];
				String[] proj = { Images.Thumbnails.DATA };

				Bitmap micro = Images.Thumbnails.getThumbnail(resolver, uid,
						Images.Thumbnails.MINI_KIND, null);
				if (micro != null) {
					
					int degree = ImageUtil.exifOrientationToDegrees(path);
					micro = ImageUtil.rotate(micro,degree);
					
					if(micro != null)
					{
						return micro;
					}
				} *//*else {

					Cursor mini = Images.Thumbnails.queryMiniThumbnail(
							resolver, uid, Images.Thumbnails.MINI_KIND, proj);
					if (mini != null && mini.moveToFirst()) {

						path = mini.getString(mini.getColumnIndex(proj[0]));
					}
				}*//*

				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(path, options);
				options.inJustDecodeBounds = false;
				options.inSampleSize = 1;
				if (options.outWidth > cell_size) {
					int ws = options.outWidth / cell_size + 1;
					if (ws > options.inSampleSize)
						options.inSampleSize = ws+3;
				}
				if (options.outHeight > cell_size) {
					int hs = options.outHeight / cell_size + 1;
					if (hs > options.inSampleSize)
						options.inSampleSize = hs+3;
				}
				micro = BitmapFactory.decodeFile(path, options);
				int degree = ImageUtil.exifOrientationToDegrees(path);
				micro =  ImageUtil.rotate(micro,degree);
				
				if(micro != null)
				{
					return micro;
				}
				else
				{
					//return BitmapFactory.decodeResource(getResources(), R.drawable.ic_custom_new_picture);	
					return null;
				}
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				runningCount--;
				if (result != null) {
					loadImages.put(uid, result);
					listener.notifyDataSetChanged();
					getNextImage();
				}
			}
		}
	}*/
}

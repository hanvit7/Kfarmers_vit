package com.leadplatform.kfarmers.view.common;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout.LayoutParams;
import com.facebook.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.TimeUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreGridFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ImageSelectorFaceBookFragment extends BaseRefreshMoreGridFragment {

	public static final String TAG = "ImageSelectorFaceBookFragment";

	static int maxSize = 1;
	static int nowSize = 0;
	String faceBookId = "";
	static String faceBookDate = "";

	String nextId = "";
	boolean bMoreFlag = false;
	
	String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'+0000'";
	String dateFormat2 = "yyyy-MM-dd";
	String dateFormat3 = "yyyy-MM-dd HH:mm:ss";
	
	SimpleDateFormat format;
	SimpleDateFormat format2;
	
	TextView textview;
	
	static ArrayList<String> seletedImg;


	public class PhotoData {
		int photoID;
		String photoPath;
		String date;
		boolean isSeleted = false;
	}

	public static int cell_size;

	AlbumAdapter adapter;
	GridView gridView;

	public static ImageSelectorFaceBookFragment newInstance(int maxSize,
			int nowSize, int cell_size, String faceBookId, String faceBookDate) {
		final ImageSelectorFaceBookFragment f = new ImageSelectorFaceBookFragment();

		final Bundle args = new Bundle();
		args.putInt("maxSize", maxSize);
		args.putInt("nowSize", nowSize);
		args.putInt("cell_size", cell_size);
		args.putString("faceBookId", faceBookId);
		args.putString("faceBookDate", faceBookDate);

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
			faceBookId = getArguments().getString("faceBookId");
			faceBookDate = getArguments().getString("faceBookDate");	
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.activity_image, container,
				false);
		gridView = (GridView) v.findViewById(R.id.gridview);
		textview = (TextView) v.findViewById(R.id.textview);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (adapter == null) {
			setMoreListView(getSherlockActivity(), gridView, loadMoreListener);
			seletedImg = new ArrayList<String>();
			adapter = new AlbumAdapter(getSherlockActivity(), R.layout.item_grid, new ArrayList<PhotoData>(),
					((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			gridView.setAdapter(adapter);
			
			format = new SimpleDateFormat(dateFormat);
			format2 = new SimpleDateFormat(dateFormat2);
			
			SimpleDateFormat format3 = new SimpleDateFormat(dateFormat3);
			
			try {
				faceBookDate = format2.format(format3.parse(faceBookDate).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			getListPhoto();
		}


		/*setMoreListView(getSherlockActivity(), gridView, loadMoreListener);

		if (adapter == null) {
			photoList = new ArrayList<PhotoData>();

			adapter = new AlbumAdapter(getSherlockActivity(),
					R.layout.fragment_base, photoList,
					((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			gridView.setAdapter(adapter);

			getListPhoto();

		}*/
	}

	@Override
	public void onDestroy() {
		//adapter.recycle();
		CommonUtil.UiUtil.unbindDrawables((GridView) gridView);
		super.onDestroy();
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getListPhoto();
			}
		}
	};

	private void getListPhoto() {

		GraphRequest graphRequest = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(), faceBookId + "/photos", new GraphRequest.Callback() {
					@Override
					public void onCompleted(GraphResponse graphResponse) {
						onRefreshComplete();
						onLoadMoreComplete();
						try {
							JsonNode data = JsonUtil.parseTree(graphResponse.getJSONObject().toString());
							JsonNode subData = data.get("data");

							if (subData.size() <= 0) {
								textview.setVisibility(View.VISIBLE);
								gridView.setVisibility(View.GONE);
							} else {
								textview.setVisibility(View.GONE);
								gridView.setVisibility(View.VISIBLE);
							}
							for (JsonNode node : subData) {
								PhotoData photoData = new PhotoData();

								photoData.date = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(node.get("created_time").textValue()).getTime()));
								photoData.photoPath = node.get("source").textValue();

								adapter.add(photoData);
							}

							if (data.get("paging").has("next")) {
								bMoreFlag = true;
								nextId = data.get("paging").get("cursors").get("after")
										.textValue();
							} else {
								nextId = "";
								bMoreFlag = false;
							}


							adapter.notifyDataSetChanged();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

				Bundle bundle = new Bundle();
				bundle.putString("fields", "created_time");
				bundle.putString("fields", "source");
				bundle.putString("after", nextId);

				graphRequest.setParameters(bundle);
				graphRequest.executeAsync();

				/*Request.Callback callback = new Request.Callback() {
					@Override
					public void onCompleted(Response response) {
						onRefreshComplete();
						onLoadMoreComplete();
						try {
							JsonNode data = JsonUtil.parseTree(response
									.getGraphObject().getInnerJSONObject().toString());
							JsonNode subData = data.get("data");

							if (subData.size() <= 0) {
								textview.setVisibility(View.VISIBLE);
								gridView.setVisibility(View.GONE);
							} else {
								textview.setVisibility(View.GONE);
								gridView.setVisibility(View.VISIBLE);
							}
							for (JsonNode node : subData) {
								PhotoData photoData = new PhotoData();

								photoData.date = format2.format(TimeUtil.convertUTCToLocalTime(format.parse(node.get("created_time").textValue()).getTime()));
								photoData.photoPath = node.get("source").textValue();

								adapter.add(photoData);
							}

							if (data.get("paging").has("next")) {
								bMoreFlag = true;
								nextId = data.get("paging").get("cursors").get("after")
										.textValue();
							} else {
								nextId = "";
								bMoreFlag = false;
							}


							adapter.notifyDataSetChanged();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};*/

				/*Bundle bundle = new Bundle();
				bundle.putString("fields", "created_time");
				bundle.putString("fields", "source");
				bundle.putString("after", nextId);

				Request request = new Request(fSession, faceBookId + "/photos", bundle,
						HttpMethod.GET, callback);
				RequestAsyncTask asyncTask = new RequestAsyncTask(request);
				asyncTask.execute();*/
			}


			public static class AlbumAdapter extends ArrayAdapter<PhotoData> {

				private ImageLoader imageLoader;
				private DisplayImageOptions options;

				Context mContext;
				ArrayList<PhotoData> photoList;

				Toast toast = null;

				public AlbumAdapter(Context context, int itemLayoutResourceId,
									ArrayList<PhotoData> items, ImageLoader imageLoader) {
					super(context, itemLayoutResourceId, items);
					this.imageLoader = imageLoader;
					photoList = items;
					mContext = context;
					this.options = new DisplayImageOptions.Builder().cacheOnDisc(true)
							.imageScaleType(ImageScaleType.EXACTLY)
							.bitmapConfig(Config.RGB_565)
							.displayer(new FadeInBitmapDisplayer(200))
							.showImageOnLoading(R.drawable.common_dummy).build();

					toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
				}

				@Override
				public View getView(int position, View convertView, ViewGroup arg2) {

					ImageView imgView, checkView, dateView;

					TextView txtDate;

					PhotoData photo = photoList.get(position);

					if (convertView == null) {

						convertView = new RelativeLayout(mContext);
						convertView.setLayoutParams(new AbsListView.LayoutParams(
								cell_size, cell_size));

						imgView = new ImageView(mContext);
						checkView = new ImageView(mContext);
						dateView = new ImageView(mContext);

						txtDate = new TextView(mContext);

						imgView.setTag("img");
						checkView.setTag("check");
						dateView.setTag("dateImg");

						txtDate.setTag("date");

						imgView.setLayoutParams(new AbsListView.LayoutParams(
								AbsListView.LayoutParams.FILL_PARENT,
								AbsListView.LayoutParams.FILL_PARENT));
						checkView.setLayoutParams(new AbsListView.LayoutParams(
								AbsListView.LayoutParams.FILL_PARENT,
								AbsListView.LayoutParams.FILL_PARENT));
						dateView.setLayoutParams(new AbsListView.LayoutParams(
								AbsListView.LayoutParams.FILL_PARENT,
								AbsListView.LayoutParams.FILL_PARENT));

						imgView.setScaleType(ScaleType.FIT_XY);
						checkView.setScaleType(ScaleType.FIT_XY);
						dateView.setScaleType(ScaleType.FIT_XY);

						dateView.setBackgroundColor(Color.argb(200, 189, 189, 189));
						dateView.setVisibility(View.GONE);


						RelativeLayout.LayoutParams layoutParams = new LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

						txtDate.setLayoutParams(layoutParams);
						txtDate.setGravity(Gravity.CENTER_HORIZONTAL);
						txtDate.setBackgroundColor(Color.argb(150, 240, 240, 240));

						convertView.setPadding(2, 2, 2, 2);

						((ViewGroup) convertView).addView(imgView);
						((ViewGroup) convertView).addView(dateView);
						((ViewGroup) convertView).addView(txtDate);
						((ViewGroup) convertView).addView(checkView);

					} else {
						imgView = (ImageView) convertView.findViewWithTag("img");
						checkView = (ImageView) convertView.findViewWithTag("check");
						dateView = (ImageView) convertView.findViewWithTag("dateImg");
						txtDate = (TextView) convertView.findViewWithTag("date");
					}

					checkView.setTag(R.id.Tag1, photo.isSeleted);
					checkView.setTag(R.id.ID, position);

					txtDate.setText(photo.date);

					if (!photo.date.equals(faceBookDate)) {
						dateView.setVisibility(View.VISIBLE);
					} else {
						dateView.setVisibility(View.GONE);
					}


					if (photo.isSeleted) {
						checkView.setImageResource(R.drawable.image_select);
					} else {
						checkView.setImageDrawable(null);
					}

					checkView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							PhotoData photoData = photoList.get(Integer.parseInt(v.getTag(R.id.ID).toString()));

							if (!ImageUtil.isFilePath(photoData.photoPath)) {
								toast.setText("로딩중인 이미지 입니다.");
								toast.show();
								return;
							}

							if (!photoData.date.equals(faceBookDate)) {
								toast.setText("등록하신 글의 날짜와 맞는 사진만 선택 가능합니다.");
								toast.show();
								return;
							}

							if (v.getTag(R.id.Tag1).toString().equals("false")) {
								if (nowSize < maxSize) {
									if (seletedImg.size() < maxSize) {
										((ImageView) v)
												.setImageResource(R.drawable.image_select);
										v.setTag(R.id.Tag1, true);
										photoData.isSeleted = true;
										seletedImg.add(photoData.photoPath);
										nowSize++;
									} else {
										toast.setText(String.format(
												mContext.getString(R.string.toast_img_max),
												maxSize));
										toast.show();
										return;
									}
								} else {
									toast.setText(String.format(
											mContext.getString(R.string.toast_img_max),
											maxSize));
									toast.show();
									return;
								}
							} else {
								((ImageView) v).setImageDrawable(null);
								v.setTag(R.id.Tag1, false);
								photoData.isSeleted = false;
								seletedImg.remove(photoData.photoPath);
								nowSize--;
							}
						}
					});

					imageLoader.displayImage(photo.photoPath, imgView, options);

					if (seletedImg.contains(photo.photoPath)) {
						checkView.setTag(R.id.ClickLayout, false);
					} else {
						checkView.setTag(R.id.ClickLayout, true);
					}

					return convertView;
				}
			}
		}

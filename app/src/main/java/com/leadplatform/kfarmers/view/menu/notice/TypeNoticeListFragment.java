package com.leadplatform.kfarmers.view.menu.notice;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.NoticeListHolder;
import com.leadplatform.kfarmers.model.json.NoticeListJson;
import com.leadplatform.kfarmers.model.parcel.NoticeListData;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class TypeNoticeListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "TypeNoticeListFragment";

	private final int limit = 20;
	private String oldIndex = "";
	private boolean bMoreFlag = false;
	private String noticeType,noticeIndex;
	private ArrayList<NoticeListJson> noticeList;
	private NoticeListAdapter noticeListAdapter;
	private LinearLayout mEmptyView;

	public static TypeNoticeListFragment newInstance(String noticType, String noticeIndex) {
		final TypeNoticeListFragment f = new TypeNoticeListFragment();

		final Bundle args = new Bundle();
		args.putString("noticeType", noticType);
		args.putString("noticeIndex", noticeIndex);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			noticeType = getArguments().getString("noticeType");
			noticeIndex = getArguments().getString("noticeIndex");
		}

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PRODUCER_COMMUNICATION_LIST, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);

		mEmptyView = (LinearLayout) v.findViewById(R.id.EmptyView);
		((TextView) v.findViewById(R.id.EmptyText)).setText("데이터가 없습니다.");
		((ImageView) v.findViewById(R.id.EmptyImage)).setImageResource(R.drawable.icon_info);

		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// initUserInfo();
		registerForContextMenu(getListView());
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (noticeListAdapter == null) {
			noticeList = new ArrayList<NoticeListJson>();
			noticeListAdapter = new NoticeListAdapter(getSherlockActivity());
			getListView().setAdapter(noticeListAdapter);
			getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					((TypeNoticeActivity)getActivity()).noticeIndex = noticeList.get(position).Index;
					((TypeNoticeActivity)getActivity()).detailFragment();
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCER_COMMUNICATION_LIST, "Click_Item", null);
				}
			});


			// ((ExpandableListView)getListView()).setOnItemLongClickListener(new OnItemLongClickListener()
			// {
			//
			// @Override
			// public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
			// {
			// Log.e(TAG, "============= position = " + position);
			// getSherlockActivity().registerForContextMenu(v);
			// getSherlockActivity().openContextMenu(v);
			// getSherlockActivity().unregisterForContextMenu(v);
			// return false;
			// }
			//
			// });

			getNoticeList(false);
		}
	}

	// private void initUserInfo()
	// {
	// try
	// {
	// String profile = DbController.queryProfileContent(getSherlockActivity());
	// JsonNode root = JsonUtil.parseTree(profile);
	// ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
	// userIndex = profileData.Index;
	// userType = profileData.Type;
	// }
	// catch (Exception e)
	// {
	// e.printStackTrace();
	// }
	// }

	private void getNoticeList(Boolean isClear) {

		if (isClear) {
			bMoreFlag = false;
			noticeList.clear();
			oldIndex = "";
			noticeListAdapter.notifyDataSetChanged();
		}

		CenterController.getListNoticeType(noticeType,"0",oldIndex, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.findPath("List").isArray()) {
								int count = 0;
								Iterator<JsonNode> it = root.findPath("List").iterator();
								while (it.hasNext()) {
									NoticeListJson notice = (NoticeListJson) JsonUtil.jsonToObject(it.next().toString(), NoticeListJson.class);
									noticeList.add(notice);
									count++;
								}
								if (count == 20)
									bMoreFlag = true;
								else
									bMoreFlag = false;
								noticeListAdapter.notifyDataSetChanged();
								if(noticeList.size()>0)
									oldIndex = noticeList.get(noticeList.size()-1).Index;

								if(noticeList.size()>0)
								{
									mEmptyView.setVisibility(View.GONE);
								}
								else
								{
									mEmptyView.setVisibility(View.VISIBLE);
								}
							}
							break;
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				onRefreshComplete();
				onLoadMoreComplete();
				super.onFailure(statusCode, headers, content, error);
			}
		});

	}

	/*private void getNoticeChildItem(final String userType,String index) {
		if (userType.equals("F")) {
			CenterController.getChildFarmerNotice(index, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					try {
						switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							NoticeChildJson noticeChild = (NoticeChildJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), NoticeChildJson.class);
							noticeListAdapter.setChild(position, noticeChild);
							noticeListAdapter.notifyDataSetChanged();
							break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					super.onFailure(statusCode, headers, content, error);
				}
			});
		} else if (userType.equals("V")) {
			CenterController.getChildVillageNotice(index, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					try {
						switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							NoticeChildJson noticeChild = (NoticeChildJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), NoticeChildJson.class);
							noticeListAdapter.setChild(position, noticeChild);
							noticeListAdapter.notifyDataSetChanged();
							break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					super.onFailure(statusCode, headers, content, error);
				}
			});
		} else {
			CenterController.getChildAppNotice(index, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					try {
						switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							NoticeChildJson noticeChild = (NoticeChildJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), NoticeChildJson.class);
							noticeListAdapter.setChild(position, noticeChild);
                            noticeListAdapter.notifyDataSetChanged();

                            getListView().post(new Runnable() {
                                @Override
                                public void run() {

                                    for(int i= 0; i< noticeListAdapter.getGroupCount();i++)
                                    {
                                        if(position!=i) {
                                            ((ExpandableListView) getListView()).collapseGroup(i);
                                        }
                                    }

                                    getListView().setSelection(position);
                                }
                            });

							break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					super.onFailure(statusCode, headers, content, error);
				}
			});
		}
	}*/

	private class NoticeListAdapter extends BaseAdapter {
		private LayoutInflater inflater = null;
		private NoticeListHolder viewHolder = null;
		private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(200)).build();

		public NoticeListAdapter(Context c){
			this.inflater = LayoutInflater.from(c);
		}

		@Override
		public int getCount() {
			return noticeList.size();
		}

		@Override
		public Object getItem(int position) {
			return noticeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				viewHolder = new NoticeListHolder();
				v = inflater.inflate(R.layout.item_notice_group, parent, false);
				viewHolder.GroupLayout = (LinearLayout) v.findViewById(R.id.GroupLayout);
				viewHolder.GroupTitle = (TextView) v.findViewById(R.id.Title);
				viewHolder.GroupDate = (TextView) v.findViewById(R.id.Date);
				viewHolder.GroupArrow = (ImageView) v.findViewById(R.id.Arrow);
				v.setTag(viewHolder);
			} else {
				viewHolder = (NoticeListHolder) v.getTag();
			}

			NoticeListJson item = noticeList.get(position);

			viewHolder.GroupDate.setText(item.Date);
			viewHolder.GroupTitle.setText(item.Title);

			return v;
		}

		/*@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				viewHolder = new NoticeListHolder();
				v = inflater.inflate(R.layout.item_notice_child, null);
				viewHolder.ChildLayout = (LinearLayout) v.findViewById(R.id.ChildLayout);
				// viewHolder.ChildContent = (TextView) v.findViewById(R.id.Content);
				v.setTag(viewHolder);
			} else {
				viewHolder = (NoticeListHolder) v.getTag();
			}

			viewHolder.ChildLayout.removeAllViews();

			NoticeChildJson child = getChild(groupPosition, childPosition);
			if (child != null && child.Rows != null) {
				for (RowJson item : child.Rows) {
					if (item.Type.equals("Text")) {
						TextView view = (TextView) inflater.inflate(R.layout.item_detail_text, null);
						view.setText(item.Value);
						viewHolder.ChildLayout.addView(view);
					} else if (item.Type.equals("Image")) {
						DynamicRatioImageView view = (DynamicRatioImageView) inflater.inflate(R.layout.item_detail_image, null);
						ImageLoader.getInstance().displayImage(item.Value, view, options);
						viewHolder.ChildLayout.addView(view);
					}
				}
			}

			return v;
		}*/
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getNoticeList(false);
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getNoticeList(true);
		}
	};

	private NoticeListData makeNoticeListData(boolean initFlag, int oldIndex) {
		NoticeListData data = new NoticeListData();
		data.setUserIndex(noticeType);
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);

		return data;
	}
}
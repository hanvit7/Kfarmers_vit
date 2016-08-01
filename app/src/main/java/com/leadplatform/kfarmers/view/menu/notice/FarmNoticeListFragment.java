package com.leadplatform.kfarmers.view.menu.notice;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.NoticeListHolder;
import com.leadplatform.kfarmers.model.json.NoticeChildJson;
import com.leadplatform.kfarmers.model.json.NoticeListJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.parcel.NoticeListData;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.DynamicRatioImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FarmNoticeListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "FarmNoticeListFragment";

	private boolean bMoreFlag = false;
	private String userType, userIndex, noticeIndex;
	private ArrayList<NoticeListJson> noticeGroupItemList;
	private ArrayList<ArrayList<NoticeChildJson>> noticeChildItemList;
	private NoticeListAdapter noticeListAdapter;

	public static FarmNoticeListFragment newInstance(String userType, String userIndex, String noticeIndex) {
		final FarmNoticeListFragment f = new FarmNoticeListFragment();

		final Bundle args = new Bundle();
		args.putString("userType", userType);
		args.putString("userIndex", userIndex);
		args.putString("noticeIndex", noticeIndex);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			userType = getArguments().getString("userType");
			userIndex = getArguments().getString("userIndex");
			noticeIndex = getArguments().getString("noticeIndex");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_notice_list, container, false);

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
			noticeGroupItemList = new ArrayList<NoticeListJson>();
			noticeChildItemList = new ArrayList<ArrayList<NoticeChildJson>>();
			noticeListAdapter = new NoticeListAdapter(getSherlockActivity(), noticeGroupItemList, noticeChildItemList);
			((ExpandableListView) getListView()).setAdapter(noticeListAdapter);
			((ExpandableListView) getListView()).setOnGroupExpandListener(new OnGroupExpandListener() {
				@Override
				public void onGroupExpand(final int groupPosition) {
					NoticeListJson group = noticeListAdapter.getGroup(groupPosition);
					NoticeChildJson child = noticeListAdapter.getChild(groupPosition, 0);
					if (child.Rows == null) {
                        getNoticeChildItem(userType, groupPosition, group.Index);
                    }
                    else
                    {
                        getListView().post(new Runnable() {
                            @Override
                            public void run() {

                                for(int i= 0; i< noticeListAdapter.getGroupCount();i++)
                                {
                                    if(groupPosition!=i) {
                                        ((ExpandableListView) getListView()).collapseGroup(i);
                                    }
                                }

                                getListView().setSelection(groupPosition);
                            }
                        });
                    }
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

			getNoticeList(makeNoticeListData(true, 0));
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

	private void getNoticeList(final NoticeListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			noticeListAdapter.clear();
			noticeListAdapter.notifyDataSetChanged();
		}

		if (data.getUserType().equals("F")) {
			CenterController.getListFarmerNotice(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onRefreshComplete();
					onLoadMoreComplete();
					try {
						switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.findPath("List").isArray()) {
								int count = 0, expandIndex = -1;
								Iterator<JsonNode> it = root.findPath("List").iterator();
								while (it.hasNext()) {
									NoticeListJson notice = (NoticeListJson) JsonUtil.jsonToObject(it.next().toString(), NoticeListJson.class);
									noticeListAdapter.addGroup(notice);
									noticeListAdapter.addChild(count, new NoticeChildJson());
									// getNoticeChildItem(data, count, notice.Index);
									if (noticeIndex.equals(notice.Index)) {
										expandIndex = count;
									}
									count++;
								}

								if (count == 20)
									bMoreFlag = true;
								else
									bMoreFlag = false;

								if (expandIndex != -1) {
									((ExpandableListView) getListView()).expandGroup(expandIndex);
									// ((ExpandableListView) getListView()).setSelection(expandIndex);
									// ((ExpandableListView) getListView()).setSelectionFromTop(expandIndex, 0);
									// ((ExpandableListView) getListView()).smoothScrollToPosition(expandIndex);
								}
								noticeListAdapter.notifyDataSetChanged();
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
		} else if (data.getUserType().equals("V")) {
			CenterController.getListVillageNotice(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onRefreshComplete();
					onLoadMoreComplete();
					try {
						switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.findPath("List").isArray()) {
								int count = 0, expandIndex = -1;
								Iterator<JsonNode> it = root.findPath("List").iterator();
								while (it.hasNext()) {
									NoticeListJson notice = (NoticeListJson) JsonUtil.jsonToObject(it.next().toString(), NoticeListJson.class);
									noticeListAdapter.addGroup(notice);
									noticeListAdapter.addChild(count, new NoticeChildJson());
									// getNoticeChildItem(data, count, notice.Index);
									if (noticeIndex.equals(notice.Index)) {
										expandIndex = count;
									}
									count++;
								}

								if (count == 20)
									bMoreFlag = true;
								else
									bMoreFlag = false;

								if (expandIndex != -1) {
									((ExpandableListView) getListView()).expandGroup(expandIndex);
									// ((ExpandableListView) getListView()).setSelection(expandIndex);
									// ((ExpandableListView) getListView()).setSelectionFromTop(expandIndex, 0);
									// ((ExpandableListView) getListView()).smoothScrollToPosition(expandIndex);
								}
								noticeListAdapter.notifyDataSetChanged();
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
		} else {
			CenterController.getListAppNotice(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onRefreshComplete();
					onLoadMoreComplete();
					try {
						switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.findPath("List").isArray()) {
								int count = 0, expandIndex = -1;
								Iterator<JsonNode> it = root.findPath("List").iterator();
								while (it.hasNext()) {
									NoticeListJson notice = (NoticeListJson) JsonUtil.jsonToObject(it.next().toString(), NoticeListJson.class);
									noticeListAdapter.addGroup(notice);
									noticeListAdapter.addChild(count, new NoticeChildJson());
									// getNoticeChildItem(data, count, notice.Index);
									if (noticeIndex.equals(notice.Index)) {
										expandIndex = count;
									}
									count++;
								}

								if (count == 20)
									bMoreFlag = true;
								else
									bMoreFlag = false;

								if (expandIndex != -1) {
									((ExpandableListView) getListView()).expandGroup(expandIndex);
									// ((ExpandableListView) getListView()).setSelection(expandIndex);
									// ((ExpandableListView) getListView()).setSelectionFromTop(expandIndex, 0);
									// ((ExpandableListView) getListView()).smoothScrollToPosition(expandIndex);
								}
								noticeListAdapter.notifyDataSetChanged();
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
	}

	private void getNoticeChildItem(final String userType, final int position, String index) {
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
			CenterController.getDetailNotice(index, new CenterResponseListener(getSherlockActivity()) {
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

	}

	private class NoticeListAdapter extends BaseExpandableListAdapter {
		private ArrayList<NoticeListJson> groupList = null;
		private ArrayList<ArrayList<NoticeChildJson>> childList = null;
		private LayoutInflater inflater = null;
		private NoticeListHolder viewHolder = null;
		private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(200)).build();

		public NoticeListAdapter(Context c, ArrayList<NoticeListJson> groupList, ArrayList<ArrayList<NoticeChildJson>> childList) {
			super();
			this.inflater = LayoutInflater.from(c);
			this.groupList = groupList;
			this.childList = childList;
		}

		public void addGroup(NoticeListJson item) {
			groupList.add(item);
		}

		public void deleteGroup(int index) {
			groupList.remove(index);
		}

		@Override
		public NoticeListJson getGroup(int groupPosition) {
			return groupList.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return groupList.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
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

			// if (isExpanded)
			// {
			// viewHolder.GroupArrow.setImageResource(R.drawable.button_category_dropdown);
			// }
			// else
			// {
			// viewHolder.GroupArrow.setImageResource(R.drawable.button_category_expand);
			// }

			viewHolder.GroupDate.setText(getGroup(groupPosition).Date);
			viewHolder.GroupTitle.setText(getGroup(groupPosition).Title);

			return v;
		}

		public void addChild(int groupPosition, NoticeChildJson item) {
			ArrayList<NoticeChildJson> child = new ArrayList<NoticeChildJson>();
			child.add(item);
			childList.add(groupPosition, child);
		}

		public void deleteChild(int index) {
			childList.remove(index);
		}

		public void setChild(int groupPosition, NoticeChildJson item) {
			ArrayList<NoticeChildJson> child = new ArrayList<NoticeChildJson>();
			child.add(item);
			childList.set(groupPosition, child);
		}

		@Override
		public NoticeChildJson getChild(int groupPosition, int childPosition) {
			return childList.get(groupPosition).get(childPosition);
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return childList.get(groupPosition).size();
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
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
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		public void clear() {
			groupList.clear();
			childList.clear();
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getNoticeList(makeNoticeListData(false, Integer.valueOf(noticeListAdapter.getGroup(noticeListAdapter.getGroupCount() - 1).Index)));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getNoticeList(makeNoticeListData(true, 0));
		}
	};

	private NoticeListData makeNoticeListData(boolean initFlag, int oldIndex) {
		NoticeListData data = new NoticeListData();
		data.setUserIndex(userIndex);
		data.setUserType(userType);
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);

		return data;
	}

	private String getUserIndex() {
		try {
			String profile = DbController.queryProfileContent(getSherlockActivity());
			if (profile != null) {
				JsonNode root = JsonUtil.parseTree(profile);
				ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
				return profileData.Index;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (getSherlockActivity() instanceof FarmNoticeActivity) {
			if (userIndex.equals(getUserIndex())) {
				getSherlockActivity().getMenuInflater().inflate(R.menu.menu_reply_item, menu);
				menu.setHeaderTitle(R.string.context_menu_title);
			}
			return;
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.btn_delete:
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item.getMenuInfo();
			final int groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			// final int childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

			NoticeListJson notice = noticeListAdapter.getGroup(groupPos);
			CenterController.deleteNotice(notice.Index, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					switch (Code) {
					case 0000:
						noticeListAdapter.deleteGroup(groupPos);
						noticeListAdapter.deleteChild(groupPos);
						noticeListAdapter.notifyDataSetChanged();
						break;

					default:
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
						break;
					}
				}
			});
			return true;
		}
		return super.onContextItemSelected(item);
	}
}

package com.leadplatform.kfarmers.view.menu.blog;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.Header;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.BlogListHolder;
import com.leadplatform.kfarmers.model.json.BlogListJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.parcel.BlogListData;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BlogListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "BlogListFragment";

	private String userIndex;
	private boolean bMoreFlag = false;
	private ArrayList<BlogListJson> blogObjectList;
	private BlogListAdapter blogListAdapter;

	public static BlogListFragment newInstance(String userIndex) {
		final BlogListFragment f = new BlogListFragment();

		final Bundle args = new Bundle();
		args.putString("userIndex", userIndex);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			userIndex = getArguments().getString("userIndex");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_blog, container, false);

		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		registerForContextMenu(getListView());
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				BlogListJson blog = blogListAdapter.getItem(position);
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(blog.PostingAddress));
				startActivity(intent);
			}
		});

		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (blogListAdapter == null) {
			blogObjectList = new ArrayList<BlogListJson>();
			blogListAdapter = new BlogListAdapter(getSherlockActivity(), R.layout.item_blog, blogObjectList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			getListView().setAdapter(blogListAdapter);
			getBlogList(makeListBlogData(true, 0));
		}
	}

	private BlogListData makeListBlogData(boolean initFlag, int oldIndex) {
		BlogListData data = new BlogListData();
		data.setFarmerIndex(userIndex);
		data.setCategory(null);
		data.setInitFlag(initFlag);
		data.setOldIndex(oldIndex);

		return data;
	}

	private void getBlogList(final BlogListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			blogListAdapter.clear();
			blogListAdapter.notifyDataSetChanged();
		}

		CenterController.getListBlog(data, new CenterResponseListener(getSherlockActivity()) {
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
								BlogListJson favorite = (BlogListJson) JsonUtil.jsonToObject(it.next().toString(), BlogListJson.class);
								blogListAdapter.add(favorite);
								count++;
							}

							if (count == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							blogListAdapter.notifyDataSetChanged();
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

	private class BlogListAdapter extends ArrayAdapter<BlogListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;

		// private DisplayImageOptions options;

		public BlogListAdapter(Context context, int itemLayoutResourceId, ArrayList<BlogListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			// this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			// .bitmapConfig(Config.RGB_565).displayer(new RoundedBitmapDisplayer(100)).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			BlogListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new BlogListHolder();
				holder.iconImage = (ImageView) convertView.findViewById(R.id.iconImage);
				holder.categoryText = (TextView) convertView.findViewById(R.id.categoryText);
				holder.dateText = (TextView) convertView.findViewById(R.id.dateText);
				holder.titleText = (TextView) convertView.findViewById(R.id.titleText);
				holder.contentText = (TextView) convertView.findViewById(R.id.contentText);

				convertView.setTag(holder);
			} else {
				holder = (BlogListHolder) convertView.getTag();
			}

			BlogListJson item = getItem(position);

			if (item != null) {
				if (!PatternUtil.isEmpty(item.BlogType)) {
					if (item.BlogType.equals("N")) {
						holder.iconImage.setImageResource(R.drawable.icon_post_naver);
					} else if (item.BlogType.equals("D")) {
						holder.iconImage.setImageResource(R.drawable.icon_post_daum);
					} else if (item.BlogType.equals("T")) {
						holder.iconImage.setImageResource(R.drawable.icon_post_tistory);
					}
				}

				if (!PatternUtil.isEmpty(item.CategoryName)) {
					holder.categoryText.setText(item.CategoryName);
				} else {
					holder.categoryText.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Date)) {
					holder.dateText.setText(item.Date);
				} else {
					holder.dateText.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Title)) {
					holder.titleText.setText(item.Title);
				} else {
					holder.titleText.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Description)) {
					holder.contentText.setText(item.Description);
				} else {
					holder.contentText.setVisibility(View.INVISIBLE);
				}
			}

			return convertView;
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getBlogList(makeListBlogData(false, Integer.valueOf(blogListAdapter.getItem(blogListAdapter.getCount() - 1).Index)));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			getBlogList(makeListBlogData(true, 0));
		}
	};

	private String getUserIndex() {
		try {
			UserDb user = DbController.queryCurrentUser(getSherlockActivity());
			JsonNode root = JsonUtil.parseTree(user.getProfileContent());
			ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
			return profileData.Index;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (getSherlockActivity() instanceof BlogActivity) {
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
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			final BlogListJson blog = blogListAdapter.getItem(info.position);

			CenterController.deleteBlog(blog.Index, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					switch (Code) {
					case 0000:
						blogListAdapter.remove(blog);
						blogListAdapter.notifyDataSetChanged();
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

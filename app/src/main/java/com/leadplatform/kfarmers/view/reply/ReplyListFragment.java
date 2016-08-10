package com.leadplatform.kfarmers.view.reply;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ReplyListHolder;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.ReplyListJson;
import com.leadplatform.kfarmers.model.parcel.ReplyListData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.diary.DiaryDetailActivity;
import com.leadplatform.kfarmers.view.diary.StoryViewActivity;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

public class ReplyListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "ReplyListFragment";

	private boolean bMoreFlag = false;
	private int replyType;
	private String diaryTitle, diaryIndex;
	private TextView actionBarTitleText;
	private EditText commentEdit;
	private ImageView sendBtn;
	private ImageButton homeBtn;
	private LinearLayout writeCommentLayout;
	private ArrayList<ReplyListJson> parentObject;
	private ArrayList<ReplyListJson> replyObjectList;
	private ReplyListAdapter replyListAdapter;
	private int offsetCount = 0;
	
	private int textLine = 0;

	public static ReplyListFragment newInstance(int replyType, String diaryTitle, String diaryIndex) {
		final ReplyListFragment f = new ReplyListFragment();

		final Bundle args = new Bundle();
		args.putInt("replyType", replyType);
		args.putString("diaryTitle", diaryTitle);
		args.putString("diaryIndex", diaryIndex);

		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			replyType = getArguments().getInt("replyType");
			diaryTitle = getArguments().getString("diaryTitle");
			diaryIndex = getArguments().getString("diaryIndex");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_reply, container, false);

		actionBarTitleText = (TextView) v.findViewById(R.id.actionbar_title_text_view);
		writeCommentLayout = (LinearLayout) v.findViewById(R.id.writeCommentLayout);
		commentEdit = (EditText) v.findViewById(R.id.commentEdit);
		sendBtn = (ImageView) v.findViewById(R.id.sendBtn);
		homeBtn = (ImageButton) v.findViewById(R.id.actionbar_home_image_button);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		parentObject = new ArrayList<ReplyListJson>();
		
		actionBarTitleText.setText(diaryTitle);
		homeBtn.setVisibility(View.VISIBLE);
		homeBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				((ReplyActivity) getSherlockActivity()).runMainActivity();
			}
		});

		registerForContextMenu(getListView());
		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_REPLY, "Click_Item", null);
				onListItemClicked(position);
			}
		});

		if (replyType != ReplyActivity.REPLY_TYPE_ADMIN) {
			writeCommentLayout.setVisibility(View.VISIBLE);
			
			commentEdit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					int selection = commentEdit.getSelectionStart();
					
					if(textLine>selection)
					{
						try {
							commentEdit.setSelection(textLine);
						} catch (Exception e) 
						{
						}
					}
				}
			});
			
			commentEdit.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_DEL)
					{
						int pos = commentEdit.getSelectionStart();
						if(textLine >= pos)
						{
							return true;
						}
					}
					return false;
				}
			});
			
			commentEdit.setOnTouchListener(new OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					int selection = commentEdit.getSelectionStart();
					if(textLine>selection)
					{
						try {
							commentEdit.setSelection(textLine);
						} catch (Exception e) 
						{
						}
							
					}
					return false;
				}
			});
			
			
			commentEdit.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						if (!AppPreferences.getLogin(getSherlockActivity())) {
							writeCommentLayout.requestFocus();
							Intent intent = new Intent(getSherlockActivity(), LoginActivity.class);
							startActivity(intent);
							return;
						}
					}
				}
			});

			sendBtn.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_REPLY, "Click_Send", null);
					centerWriteComment(commentEdit.getText().toString());
				}
			});
		} else {
			writeCommentLayout.setVisibility(View.GONE);
		}

		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (replyListAdapter == null) {
			replyObjectList = new ArrayList<ReplyListJson>();
			replyListAdapter = new ReplyListAdapter(getSherlockActivity(), R.layout.item_reply, replyObjectList, ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			setListAdapter(replyListAdapter);
			getReplyList(makeListReplyData(true, diaryIndex, 0));
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	private ReplyListData makeListReplyData(boolean initFlag, String diaryIndex, int offset) {
		ReplyListData data = new ReplyListData();
		data.setInitFlag(initFlag);
		data.setDiaryIndex(diaryIndex);
		data.setOffset(offset);

		return data;
	}

	private void getReplyList(final ReplyListData data) {
		if (data == null)
			return;

		if (data.isInitFlag()) {
			bMoreFlag = false;
			offsetCount = 0;
			replyListAdapter.clear();
			replyListAdapter.notifyDataSetChanged();
		}

		if (replyType == ReplyActivity.REPLY_TYPE_FARMER) {
			CenterController.getListFarmerReply(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onLoadMoreComplete();
					try {
						switch (Code) {
						case 0000:
							parseReplyJson(content);
							break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					onLoadMoreComplete();
					super.onFailure(statusCode, headers, content, error);
				}
			});
		} else if (replyType == ReplyActivity.REPLY_TYPE_VILLAGE) {
			CenterController.getListVillageReply(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onLoadMoreComplete();
					try {
						switch (Code) {
						case 0000:
							parseReplyJson(content);
							break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					onLoadMoreComplete();
					super.onFailure(statusCode, headers, content, error);
				}
			});

		} else if (replyType == ReplyActivity.REPLY_TYPE_CONSUMER || replyType == ReplyActivity.REPLY_TYPE_INTERVIEW || replyType == ReplyActivity.REPLY_TYPE_NORMAL || replyType == ReplyActivity.REPLY_TYPE_CHATTER) {
			CenterController.getListNormalReply(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onLoadMoreComplete();
					try {
						switch (Code) {
							case 0000:
								parseReplyJson(content);
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					onLoadMoreComplete();
					super.onFailure(statusCode, headers, content, error);
				}
			});
		} else if (replyType == ReplyActivity.REPLY_TYPE_ADMIN) {
			CenterController.getListAdminReply(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onLoadMoreComplete();
					try {
						switch (Code) {
							case 0000:
								parseReplyJson(content);
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					onLoadMoreComplete();
					super.onFailure(statusCode, headers, content, error);
				}
			});
		} else if (replyType == ReplyActivity.REPLY_TYPE_REVIEW) {

			CenterController.getListReviewReply(data, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					onLoadMoreComplete();
					try {
						switch (Code) {
							case 0000:
								parseReplyJson(content);
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
					onLoadMoreComplete();
					super.onFailure(statusCode, headers, content, error);
				}
			});
		}
	}

	private void parseReplyJson(String content) throws Exception {
		JsonNode root = JsonUtil.parseTree(content);
		if (root.findPath("List").isArray()) {
			int count = 0;
			Iterator<JsonNode> it = root.findPath("List").iterator();
			while (it.hasNext()) {
				ReplyListJson reply = (ReplyListJson) JsonUtil.jsonToObject(it.next().toString(), ReplyListJson.class);
				replyListAdapter.add(reply);
				count++;
			}

			if (count == 20)
				bMoreFlag = true;
			else
				bMoreFlag = false;
			offsetCount += count;

			replyListAdapter.notifyDataSetChanged();
		}
	}

	private class ReplyListAdapter extends ArrayAdapter<ReplyListJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		public ReplyListAdapter(Context context, int itemLayoutResourceId, ArrayList<ReplyListJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			 this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).displayer(new RoundedBitmapDisplayer(100)).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ReplyListHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);

				holder = new ReplyListHolder();
				holder.Farm = (TextView) convertView.findViewById(R.id.Farm);
				holder.Date = (TextView) convertView.findViewById(R.id.Date);
				holder.Name = (TextView) convertView.findViewById(R.id.Name);
				holder.Description = (TextView) convertView.findViewById(R.id.Description);
				holder.profile = (ImageView) convertView.findViewById(R.id.profile);
				holder.FarmerIcon = (ImageView) convertView.findViewById(R.id.FarmerIcon);

				convertView.setTag(holder);
			} else {
				holder = (ReplyListHolder) convertView.getTag();
			}

			ReplyListJson item = getItem(position);

			if (item != null) {
				if (!PatternUtil.isEmpty(item.TargetName)) {
					holder.Farm.setText(item.TargetName);
					holder.Farm.setVisibility(View.VISIBLE);
				} else {
					holder.Farm.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Date)) {
					holder.Date.setText(item.Date);
					holder.Date.setVisibility(View.VISIBLE);
				} else {
					holder.Date.setVisibility(View.INVISIBLE);
				}

				if (!PatternUtil.isEmpty(item.Description)) {
					String description="";
					/*if (PatternUtil.isEmpty(item.ParentIndex) || PatternUtil.isEmpty(item.ParentName) || item.ParentIndex.equals("0")) {
						description = item.Description;
					} else {
						description = "<font color=#4b99b7>" + item.ParentName + "</font> " + item.Description;
					}*/
					
					

					/*if(i == 0)
					{
						name +="[*!Name*]";
					}
					name += parentObject.get(i).TargetName;
					
					if(i == parentObject.size()-1)
					{
						name +="[*!Name*]";
					}
					else
					{
						name +="[*!!!*]";
					}*/
				
					
					String des[] = item.Description.split("==]Name]==");
					
					if(des.length>1)
					{
						String subDes[] = des[0].split("==],]==");
						
						for(int i=0 ; i < subDes.length;i++)
						{
							description += "<font color=#4b99b7>" + subDes[i] + "</font> ";	
							
							if(i< subDes.length-1)
							{
								description +=" ";
							}
						}
						
						holder.Name.setText(Html.fromHtml(description));
						holder.Name.setVisibility(View.VISIBLE);
						holder.Description.setText(des[1]);
						
						//description += "<br><br>";
						//description += des[1];
					}
					else
					{
						holder.Name.setText("");
						holder.Name.setVisibility(View.GONE);
						holder.Description.setText(item.Description);
					}
					
					
					holder.Description.setVisibility(View.VISIBLE);
				} else {
					holder.Description.setVisibility(View.INVISIBLE);
				}
				
				holder.profile.setImageResource(R.drawable.icon_empty_profile);
				if (!PatternUtil.isEmpty(item.ProfileImage))
				{
					imageLoader.displayImage(item.ProfileImage, holder.profile, options);
				}

				if((item.PermissionFlag!= null && item.PermissionFlag.equals("Y")) && (item.TemporaryPermissionFlag!= null && item.TemporaryPermissionFlag.equals("Y"))) {
					holder.FarmerIcon.setVisibility(View.VISIBLE);
					holder.FarmerIcon.setTag(R.integer.tag_st, item.TargetType);
					holder.FarmerIcon.setTag(R.integer.tag_id,item.TargetIndex);
					holder.FarmerIcon.setOnClickListener(new ViewOnClickListener() {
						@Override
						public void viewOnClick(View v) {
							Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
							intent.putExtra("userType", (String) v.getTag(R.integer.tag_st));
							intent.putExtra("userIndex", (String) v.getTag(R.integer.tag_id));
							startActivity(intent);
						}
					});
				} else {
					holder.FarmerIcon.setVisibility(View.GONE);
				}
				
				// holder.Delete.setTag(position);
				// holder.Delete.setOnClickListener(new ViewOnClickListener()
				// {
				// @Override
				// public void viewOnClick(View v)
				// {
				// int position = (Integer) v.getTag();
				// ReplyListJson item = getItem(position);
				// centerSetFavorite(item);
				// }
				// });
			}

			return convertView;
		}
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				// getReplyList(makeListReplyData(false, diaryIndex, Integer.valueOf(replyListAdapter.getItem(replyListAdapter.getCount() - 1).CommentIndex)));
				getReplyList(makeListReplyData(false, diaryIndex, offsetCount));
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private void onListItemClicked(int position) {
		if (replyType != ReplyActivity.REPLY_TYPE_ADMIN) {
			//if (commentEdit.getText().length() == 0) {
			
			if (!AppPreferences.getLogin(getSherlockActivity())) {
				Intent intent = new Intent(getSherlockActivity(), LoginActivity.class);
				startActivity(intent);
				return;
			}
			
			boolean isData = false;
			ReplyListJson object = replyListAdapter.getItem(position);
			
			if(getUserIndex().equals(object.TargetIndex))
			{
				return;
			}
			
			if(commentEdit.getText().toString().length()==0)
			{
				parentObject = new ArrayList<ReplyListJson>();
				textLine = 0;
			}
			
			
			
			for (Iterator<ReplyListJson> iterator = parentObject.iterator(); iterator.hasNext(); ) {
				ReplyListJson json = iterator.next();
				if(json.TargetIndex.equals(object.TargetIndex))
				{
					iterator.remove();
					isData = true;
				}
			}
			
			if(!isData)
			{
				parentObject.add(object);
			}
			
			String item = "";
			
			for(ReplyListJson json : parentObject)
			{
				if(!item.equals(""))
				{
					item += " ";
				}
				item += json.TargetName;
			}
			
			item +=" ";
			
			item += commentEdit.getText().toString().substring(textLine);
			
			
			
			SpannableString text = new SpannableString(item);
			
			int pos = 0;
			for(ReplyListJson json : parentObject)
			{
				if(pos>0)
				{
					pos +=1;
				}
				text.setSpan(new ForegroundColorSpan(Color.parseColor("#478ca6")), pos, pos+json.TargetName.length(), 0);
				text.setSpan(new BackgroundColorSpan(Color.parseColor("#c4dbe3")), pos, pos+json.TargetName.length(), 0);
				pos += json.TargetName.length();
			}
			pos +=1;
			textLine = pos;
			
			commentEdit.setText(text, BufferType.SPANNABLE);
			commentEdit.setSelected(true);
			commentEdit.setSelection(commentEdit.length());
			//}
		} else {
			ReplyListJson replyObject = replyListAdapter.getItem(position);
			Intent intent = null;
			if (replyObject.BoardType.equals("F")) {
				intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
				intent.putExtra("diary", replyObject.DiaryIndex);
				intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
			} else if (replyObject.BoardType.equals("V")) {
				intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
				intent.putExtra("diary", replyObject.DiaryIndex);
				intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
			} else if (replyObject.BoardType.equals("C")) {
				intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
				intent.putExtra("type","daily");
				intent.putExtra("diary",replyObject.DiaryIndex);
				intent.putExtra("name","");
			} else if (replyObject.BoardType.equals("D")) {
				intent = new Intent(getSherlockActivity(), StoryViewActivity.class);
				intent.putExtra("type","story");
				intent.putExtra("diary",replyObject.DiaryIndex);
				intent.putExtra("name","");
			}

			if (intent != null)
				startActivity(intent);
		}
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
		if (getSherlockActivity() instanceof ReplyActivity) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			ReplyListJson comment = replyListAdapter.getItem(info.position);

			if (comment.TargetIndex.equals(getUserIndex())) {
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
			final ReplyListJson comment = replyListAdapter.getItem(info.position);

			if (replyType == ReplyActivity.REPLY_TYPE_REVIEW)
			{
				CenterController.deleteCommentReview(comment.BoardType,comment.CommentIndex, new CenterResponseListener(getSherlockActivity()) {
					@Override
					public void onSuccess(int Code, String content) {
						switch (Code) {
							case 0000:
								replyListAdapter.remove(comment);
								replyListAdapter.notifyDataSetChanged();
								break;

							default:
								UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
								break;
						}
					}
				});
			}
			else
			{
				CenterController.deleteComment(comment.BoardType, comment.CommentIndex, new CenterResponseListener(getSherlockActivity()) {
					@Override
					public void onSuccess(int Code, String content) {
						switch (Code) {
							case 0000:
								replyListAdapter.remove(comment);
								replyListAdapter.notifyDataSetChanged();
								break;

							default:
								UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
								break;
						}
					}
				});
			}
			return true;
		}
		return super.onContextItemSelected(item);
	}

	private void centerWriteComment(String description) {
		if (!AppPreferences.getLogin(getSherlockActivity())) {
			Intent intent = new Intent(getSherlockActivity(), LoginActivity.class);
			startActivity(intent);
			return;
		}
		
		//if (PatternUtil.isEmpty(description.trim())) {
		if(description.length()-textLine<=0){
			UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_comment);
			return;
		}
		
		String board = "";
		
		//CommentData data = new CommentData();
		switch (replyType) {
		case ReplyActivity.REPLY_TYPE_FARMER:
			//data.setBoardType("F");
			board = "F";
			break;
		case ReplyActivity.REPLY_TYPE_VILLAGE:
			//data.setBoardType("V");
			board = "V";
			break;
		case ReplyActivity.REPLY_TYPE_NORMAL:
			//data.setBoardType("C");
			board = "C";
			break;
		case ReplyActivity.REPLY_TYPE_CHATTER:
			//data.setBoardType("D");
			board = "D";
			break;
		case ReplyActivity.REPLY_TYPE_REVIEW:
			//data.setBoardType("D");
			board = "R";
			break;
		// case ReplyActivity.REPLY_TYPE_NORMAL:
		// data.setBoardType("T");
		// break;
		}
		/*data.setDiaryIndex(diaryIndex);
		if (parentObject != null) {
			//data.setParentCommentIndex(parentObject.CommentIndex);
			//description = description.substring(parentObject.TargetName.length());
		}
		data.setDescription(description);*/
		
		
		String mBoardType[] = new String[parentObject.size()];
		String mDiaryIndex[] = new String[parentObject.size()];
		String mParentComentIndex[] = new String[parentObject.size()];
		String mDes[] = new String[parentObject.size()];
		
		if(parentObject.size()>0)
		{
			for(int i = 0 ; i < parentObject.size();i++)
			{
				mBoardType[i] = board;
			}
			for(int i = 0 ; i < parentObject.size();i++)
			{
				mDiaryIndex[i] = diaryIndex;
			}
			for(int i = 0 ; i < parentObject.size();i++)
			{
				mParentComentIndex[i] = parentObject.get(i).CommentIndex;
			}
			
			String name = "";
			for(int i = 0 ; i < parentObject.size();i++)
			{
				name += parentObject.get(i).TargetName;
				
				if(i == parentObject.size()-1)
				{
					
					name +="==]Name]==";
				}
				else
				{
					name +="==],]==";
				}
			}
			
			for(int i = 0 ; i < parentObject.size();i++)
			{
				mDes[i] = name+description.substring(textLine);
			}
		}
		else
		{
			mBoardType = new String[1];
			mDiaryIndex = new String[1];
			mParentComentIndex = new String[1];
			mDes = new String[1];
			
			mBoardType[0] = board;
			mDiaryIndex[0] = diaryIndex;
			mParentComentIndex[0] = "0";
			mDes[0] = description.substring(textLine); 
		}

		if (replyType == ReplyActivity.REPLY_TYPE_REVIEW)
		{
			CenterController.writeCommentReview(mBoardType, mDiaryIndex, mParentComentIndex, mDes, new CenterResponseListener(getSherlockActivity()) {
				@Override
				public void onSuccess(int Code, String content) {
					super.onSuccess(Code, content);
					onLoadMoreComplete();
					try {
						switch (Code) {
							case 0000:
								commentEdit.setText("");
								textLine = 0;
								parentObject = new ArrayList<ReplyListJson>();
								getReplyList(makeListReplyData(true, diaryIndex, 0));
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}
			});
		} else if (replyType == ReplyActivity.REPLY_TYPE_NORMAL || replyType == ReplyActivity.REPLY_TYPE_CHATTER) {
			CenterController.writeCommentUser(mBoardType, mDiaryIndex, mParentComentIndex, mDes,new CenterResponseListener(getSherlockActivity()){
				@Override
				public void onSuccess(int Code, String content) {
					super.onSuccess(Code, content);
					onLoadMoreComplete();
					try {
						switch (Code) {
							case 0000:
								commentEdit.setText("");
								textLine = 0;
								parentObject = new ArrayList<ReplyListJson>();
								getReplyList(makeListReplyData(true, diaryIndex, 0));
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}
			});
		} else
		{
			CenterController.writeComment(mBoardType, mDiaryIndex, mParentComentIndex, mDes,new CenterResponseListener(getSherlockActivity()){
				@Override
				public void onSuccess(int Code, String content) {
					super.onSuccess(Code, content);
					onLoadMoreComplete();
					try {
						switch (Code) {
							case 0000:
								commentEdit.setText("");
								textLine = 0;
								parentObject = new ArrayList<ReplyListJson>();
								getReplyList(makeListReplyData(true, diaryIndex, 0));
								break;
						}
					} catch (Exception e) {
						UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				}
			});
		}


		/*TokenApiController.writeComment(getSherlockActivity(), mBoardType, mDiaryIndex, mParentComentIndex, mDes, new TokenResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				super.onSuccess(Code, content);
				onLoadMoreComplete();
				try {
					switch (Code) {
						case 0000:
							commentEdit.setText("");
							textLine = 0;
							parentObject = new ArrayList<ReplyListJson>();
							getReplyList(makeListReplyData(true, diaryIndex, 0));
							break;
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
								  byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				onLoadMoreComplete();
			}
		});*/
		

		/*CenterController.writeComment(data, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onLoadMoreComplete();
				try {
					switch (Code) {
					case 0000:
						commentEdit.setText("");
						getReplyList(makeListReplyData(true, diaryIndex, 0));
						break;
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				onLoadMoreComplete();
				super.onFailure(statusCode, headers, content, error);
			}
		});*/
	}
}

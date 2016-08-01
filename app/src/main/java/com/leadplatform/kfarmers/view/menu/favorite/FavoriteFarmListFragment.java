package com.leadplatform.kfarmers.view.menu.favorite;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.FavoriteJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.inquiry.InquiryActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FavoriteFarmListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "FavoriteFarmListFragment";

	private boolean bMoreFlag = false;
	private FavoriteListAdapter favoriteListAdapter;

	private String mIndex,mUserType,mUserId,mOldIndex;

	private UserDb user;

	private LinearLayout mBottomLayout;
	private Button mAllSend_btn;

	private View mEmptyView;
	private Button mEmptyButton;

	public static FavoriteFarmListFragment newInstance(String type, String index,String id) {
		final FavoriteFarmListFragment f = new FavoriteFarmListFragment();

		final Bundle args = new Bundle();
		args.putString("type", type);
		args.putString("id",id);
		args.putString("index", index);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_FAVORITE_FARM, null);

		if (getArguments() != null) {
			mUserType = getArguments().getString("type");
			mUserId = getArguments().getString("id");
			mIndex = getArguments().getString("index");
		}
		mOldIndex = "";
		user = DbController.queryCurrentUser(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_favorite_farm_list, container, false);
		mBottomLayout = (LinearLayout) v.findViewById(R.id.BottomLayout);
		mAllSend_btn = (Button) v.findViewById(R.id.allSend_btn);

		mEmptyView =  v.findViewById(R.id.EmptyView);
		mEmptyButton = (Button) v.findViewById(R.id.EmptyButton);

		mEmptyButton.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				centerSetFavorite(mUserType, mIndex);
			}
		});
		mBottomLayout.setVisibility(View.VISIBLE);

		if(user != null && user.getUserID().equals(mUserId)) {

			mAllSend_btn.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {
					getMultiMsgCount();
				}
			});
		} else {
			mAllSend_btn.setOnClickListener(new ViewOnClickListener() {
				@Override
				public void viewOnClick(View v) {
					UiDialog.showDialog(getActivity(), "메시지는 해당 농장주만 보낼 수 있습니다.");
				}
			});
		}
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (favoriteListAdapter == null) {
			favoriteListAdapter = new FavoriteListAdapter(getSherlockActivity(), R.layout.item_favorite_farm, new ArrayList<FavoriteJson>(), ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			getListView().setAdapter(favoriteListAdapter);
			getFarmFavorite();
		}
	}

	private void centerSetFavorite(String userType, String userIndex)
	{
		CenterController.setFavorite(userType, userIndex, "I", new CenterResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							UiController.toastAddFavorite(getActivity());
							getFarmFavorite();
							break;
						case 1005:
							UiController.showDialog(getActivity(), R.string.dialog_already_favorite);
							getFarmFavorite();
							break;
						case 1006:
							UiController.showDialog(getActivity(), R.string.dialog_my_favorite);
							break;
						default:
							UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
							getFarmFavorite();
							break;
					}
				} catch (Exception e) {
					UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
				}
			}
		});
	}

	private void getFarmFavorite() {
		CenterController.getListFavoriteFarm(mUserType, mIndex, mOldIndex, new CenterResponseListener(getSherlockActivity()) {
			@Override
			public void onSuccess(int Code, String content) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							ArrayList<FavoriteJson> favoriteObjectList = (ArrayList<FavoriteJson>) JsonUtil.jsonToArrayObject(root.findPath("List"), FavoriteJson.class);

							if (favoriteObjectList.size() > 0) {
								mOldIndex = favoriteObjectList.get(favoriteObjectList.size() - 1).Index;
								favoriteListAdapter.addAll(favoriteObjectList);
								favoriteListAdapter.notifyDataSetChanged();
							}
							if (favoriteObjectList.size() == 20)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							if (favoriteListAdapter.getCount() == 0) {
								mEmptyView.setVisibility(View.VISIBLE);
							} else {
								mEmptyView.setVisibility(View.GONE);
							}
							break;
						default:
							UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
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

	private void getMultiMsgCount() {
		SnipeApiController.farmMultiMgsCount(user.getUserID(), new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					switch (Code) {
						case 200:
							int count = Integer.parseInt(JsonUtil.parseTree(content).path("count").asText());
							if(count<=0) {
								Toast.makeText(getActivity(), "이번달 전체 메시지 전송 횟수를 모두 사용 하였습니다.", Toast.LENGTH_SHORT).show();
								return;
							}
							//Toast.makeText(getActivity(), "사용가능한 전체메세지 : "+count+"개", Toast.LENGTH_SHORT).show();
							LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							View convertView = inflater.inflate(R.layout.item_popup_all_msg, null);
							final EditText editText = (EditText) convertView.findViewById(R.id.commentEdit);
							editText.setHint("보내실 내용을 입력해 주세요.\n\n사용가능한 메시지 전송 횟수 : "+count+"회");
							UiDialog.showDialog(getActivity(), "전체 메시지", convertView, R.string.dialog_ok, R.string.dialog_close, new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
									if (UiDialog.DIALOG_POSITIVE_LISTENER == type) {
										if (editText.getText().toString().trim().isEmpty()) {
											Toast.makeText(getActivity(), "내용을 입력해주세요", Toast.LENGTH_SHORT).show();
										} else {
											SnipeApiController.farmMultiMgs(user.getUserID(), editText.getText().toString().trim(), new SnipeResponseListener(getActivity()) {
												@Override
												public void onSuccess(int Code, String content, String error) {
													switch (Code) {
														case 200:
															Toast.makeText(getActivity(), "전체 메시지를 전송 했습니다.", Toast.LENGTH_SHORT).show();
															break;
														case 401:
															Toast.makeText(getActivity(), "이번달 전체 메시지 전송 횟수를 모두 사용 하였습니다.", Toast.LENGTH_SHORT).show();
															break;
													}
												}
											});
										}
									}
								}
							});
							break;
						default:
							UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
					}
				} catch (Exception e) {
					UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
				}
			}
		});
	}

	private class FavoriteListAdapter extends ArrayAdapter<FavoriteJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions options;

		public FavoriteListAdapter(Context context, int itemLayoutResourceId, ArrayList<FavoriteJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;
			this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
					.displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 40) / 2)).build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}

			ImageView profile = ViewHolder.get(convertView, R.id.profile);
			TextView name = ViewHolder.get(convertView, R.id.name);
			TextView send = ViewHolder.get(convertView, R.id.sendMsg);

			FavoriteJson item = getItem(position);
			if (item != null) {
				profile.setImageResource(R.drawable.icon_empty_profile);
				if(item.ProfileImage != null && !item.ProfileImage.isEmpty())
					imageLoader.displayImage(item.ProfileImage, profile, options);
				name.setText(item.Name);
				send.setVisibility(View.VISIBLE);
				if(user != null && user.getUserID().equals(mUserId)) {
					if (user.getUserID().equals(item.ID)) {
						send.setVisibility(View.GONE);
					} else {
						send.setTag(item.ID);
						send.setOnClickListener(viewOnClickListener);
					}
				} else {
					//send.setVisibility(View.GONE);
					send.setOnClickListener(viewOnClickListener);
				}
			}
			return convertView;
		}

		ViewOnClickListener viewOnClickListener = new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				String id = (String) v.getTag();

				if(id == null || id.isEmpty())  {
					UiDialog.showDialog(getActivity(), "메시지는 해당 농장주만 보낼 수 있습니다.");
					return;
				}

				SnipeApiController.checkChatRoom(user.getUserID(), id, new SnipeResponseListener(getActivity()) {
					@Override
					public void onSuccess(int Code, String content, String error) {
						try {
							switch (Code) {
								case 200:
									Intent intent = new Intent(getActivity(), InquiryActivity.class);
									intent.putExtra("index", content);
									startActivity(intent);
									break;
								default:
									UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
							}
						} catch (Exception e) {
							UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
						}
					}
				});
			}
		};
	}

	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getFarmFavorite();
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			bMoreFlag = false;
			mOldIndex = "";
			favoriteListAdapter.clear();
			getFarmFavorite();
		}
	};
}

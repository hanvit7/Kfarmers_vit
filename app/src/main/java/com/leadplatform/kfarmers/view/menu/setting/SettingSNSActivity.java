package com.leadplatform.kfarmers.view.menu.setting;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.sns.SnsActivity;

public class SettingSNSActivity extends BaseFragmentActivity {
	public static final String TAG = "SettingSNSActivity";

	private String permissionFlag = "Y";
	private TextView naverConnectBtn, kakaoConnectBtn, facebookConnectBtn, tistoryConnectBtn, twitterConnectBtn, daumConnectBtn;
	
	private TextView naverConnectBtn1, daumConnectBtn1, tistoryConnectBtn1;

	/***************************************************************/
	// Override

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SNS_CONNECT, null);
	}
	/***************************************************************/

	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_setting_sns);

		//initUserType();
		initContentView(savedInstanceState);
	}

	private void initContentView(Bundle savedInstanceState) {
		
		naverConnectBtn1 = (TextView)findViewById(R.id.naverConnectBtn1);
		daumConnectBtn1 = (TextView)findViewById(R.id.daumConnectBtn1);
		tistoryConnectBtn1 = (TextView)findViewById(R.id.tistoryConnectBtn1);
		
		naverConnectBtn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SettingSNSActivity.this, SnsActivity.class);
				intent.putExtra("snsType", Constants.REQUEST_SNS_BLOG_NAVER);
				startActivityForResult(intent, Constants.REQUEST_SNS_BLOG_NAVER);
			}
		});
		
		daumConnectBtn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_GetConnect", "다음");
				Intent intent = new Intent(SettingSNSActivity.this, SnsActivity.class);
				intent.putExtra("snsType", Constants.REQUEST_SNS_BLOG_DAUM);
				startActivityForResult(intent, Constants.REQUEST_SNS_BLOG_DAUM);
			}
		});
		
		tistoryConnectBtn1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_GetConnect", "티스토리");
				Intent intent = new Intent(SettingSNSActivity.this, SnsActivity.class);
				intent.putExtra("snsType", Constants.REQUEST_SNS_BLOG_TSTORY);
				startActivityForResult(intent, Constants.REQUEST_SNS_BLOG_TSTORY);
			}
		});
		
		findViewById(R.id.kakaoConnectBtn1).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(SettingSNSActivity.this, SnsActivity.class);
                intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO_CH);
                startActivityForResult(intent, Constants.REQUEST_SNS_KAKAO_CH);
            }
        });
		
		naverConnectBtn = (TextView) findViewById(R.id.naverConnectBtn2);
		naverConnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v)
			{
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_Connect", "네이버");
				onNaverBlogBtnClicked();
			}
		});
		kakaoConnectBtn = (TextView) findViewById(R.id.kakaoConnectBtn2);
		kakaoConnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_Connect", "카카오스토리");
				onKakaoBtnClicked();
			}
		});
		facebookConnectBtn = (TextView) findViewById(R.id.facebookConnectBtn);
		facebookConnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_Connect", "페이스북");
				onFacebookBtnClicked();
			}
		});
		tistoryConnectBtn = (TextView) findViewById(R.id.tistoryConnectBtn2);
		tistoryConnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_Connect", "티스토리");
				onTiStoryBtnClicked();
			}
		});
		twitterConnectBtn = (TextView) findViewById(R.id.twitterConnectBtn);
		twitterConnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_Connect", "트위터");
				onTwitterBtnClicked();
			}
		});
		daumConnectBtn = (TextView) findViewById(R.id.daumConnectBtn2);
		daumConnectBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_SNS_CONNECT, "Click_Connect", "다음");
				onDaumBlogBtnClicked();
			}
		});

		refreshBtn();
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar);
		TextView title = (TextView) findViewById(R.id.title);
		title.setText(R.string.setting_sns_title);
		initActionBarHomeBtn();
	}


	@Override
	protected void onResume() {
		super.onResume();
		refreshBtn();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_SNS_NAVER) {
				DbController.updateNaverFlag(SettingSNSActivity.this, true);
			} else if (requestCode == Constants.REQUEST_SNS_TISTORY) {
				DbController.updateTistoryFlag(SettingSNSActivity.this, true);
			} else if (requestCode == Constants.REQUEST_SNS_DAUM) {
				DbController.updateDaumFlag(SettingSNSActivity.this, true);
			} else if (requestCode == Constants.REQUEST_SNS_FACEBOOK) {
				DbController.updateFaceBookFlag(SettingSNSActivity.this, true);
			} else if (requestCode == Constants.REQUEST_SNS_TWITTER) {
				DbController.updateTwitterFlag(SettingSNSActivity.this, true);
			} else if (requestCode == Constants.REQUEST_SNS_KAKAO) {
				DbController.updateKakaoFlag(SettingSNSActivity.this, true);
			}
			refreshBtn();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initUserType() {
		try {
			UserDb user = DbController.queryCurrentUser(this);
			JsonNode root = JsonUtil.parseTree(user.getProfileContent());
			ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
			permissionFlag = profileData.TemporaryPermissionFlag;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void refreshBtn() {
		if (DbController.queryNaverFlag(this)) {
			naverConnectBtn.setText("연결 해제");
			naverConnectBtn.setTextColor(Color.parseColor("#b5b5b5"));
		} else {
			naverConnectBtn.setText("동시등록 연결");
			naverConnectBtn.setTextColor(Color.parseColor("#8dd427"));
		}

		if (DbController.queryKakaoFlag(this)) {
			kakaoConnectBtn.setText("연결 해제");
			kakaoConnectBtn.setTextColor(Color.parseColor("#b5b5b5"));
		} else {
			kakaoConnectBtn.setText("동시등록 연결");
			kakaoConnectBtn.setTextColor(Color.parseColor("#8dd427"));
		}

		if (DbController.queryFaceBookFlag(this)) {
			facebookConnectBtn.setText("연결 해제");
			facebookConnectBtn.setTextColor(Color.parseColor("#b5b5b5"));
		} else {
			facebookConnectBtn.setText("동시등록 연결");
			facebookConnectBtn.setTextColor(Color.parseColor("#8dd427"));
		}

		if (DbController.queryTistoryFlag(this)) {
			tistoryConnectBtn.setText("연결 해제");
			tistoryConnectBtn.setTextColor(Color.parseColor("#b5b5b5"));
		} else {
			tistoryConnectBtn.setText("동시등록 연결");
			tistoryConnectBtn.setTextColor(Color.parseColor("#8dd427"));
		}

		if (DbController.queryTwitterFlag(this)) {
			twitterConnectBtn.setText("연결 해제");
			twitterConnectBtn.setTextColor(Color.parseColor("#b5b5b5"));
		} else {
			twitterConnectBtn.setText("동시등록 연결");
			twitterConnectBtn.setTextColor(Color.parseColor("#8dd427"));
		}

		if (DbController.queryDaumFlag(this)) {
			daumConnectBtn.setText("연결 해제");
			daumConnectBtn.setTextColor(Color.parseColor("#b5b5b5"));
		} else {
			daumConnectBtn.setText("동시등록 연결");
			daumConnectBtn.setTextColor(Color.parseColor("#8dd427"));
		}

		if (PatternUtil.isEmpty(DbController.queryBlogNaver(this)))
		{
			naverConnectBtn1.setText("가져오기 연결");
			naverConnectBtn1.setTextColor(Color.parseColor("#8dd427"));
		} else {
			naverConnectBtn1.setText("가져오기 수정");
			naverConnectBtn1.setTextColor(Color.parseColor("#b5b5b5"));
		}
		
		if (PatternUtil.isEmpty(DbController.queryBlogDaum(this)))
		{
			daumConnectBtn1.setText("가져오기 연결");
			daumConnectBtn1.setTextColor(Color.parseColor("#8dd427"));
		} else {
			daumConnectBtn1.setText("가져오기 수정");
			daumConnectBtn1.setTextColor(Color.parseColor("#b5b5b5"));
		}
		
		if (PatternUtil.isEmpty(DbController.queryBlogTstory(this)))
		{
			tistoryConnectBtn1.setText("가져오기 연결");
			tistoryConnectBtn1.setTextColor(Color.parseColor("#8dd427"));
		} else {
			tistoryConnectBtn1.setText("가져오기 수정");
			tistoryConnectBtn1.setTextColor(Color.parseColor("#b5b5b5"));
		}
		
	}

	public void onNaverBlogBtnClicked() {
		if (permissionFlag.equals("N")) {
			UiController.showDialog(this, R.string.dialog_wait_approve);
			return;
		}

		Intent intent = new Intent(this, SnsActivity.class);
		intent.putExtra("snsType", Constants.REQUEST_SNS_NAVER);
		startActivityForResult(intent, Constants.REQUEST_SNS_NAVER);
		
		/*if (DbController.queryNaverFlag(this)) {
			UiController.showDialog(mContext, R.string.dialog_disconnect_sns, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
						DbController.updateNaverFlag(SettingSNSActivity.this, false);
						NaverLoginHelper.naverLogout(mContext);
						refreshBtn();
					}
				}
			});
		} else {
			Intent intent = new Intent(this, SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_NAVER);
			startActivityForResult(intent, Constants.REQUEST_SNS_NAVER);
		}*/
	}

	public void onTiStoryBtnClicked() {
		if (permissionFlag.equals("N")) {
			UiController.showDialog(this, R.string.dialog_wait_approve);
			return;
		}

		if (DbController.queryTistoryFlag(this)) {
			UiController.showDialog(mContext, R.string.dialog_disconnect_sns, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
						DbController.updateTistoryFlag(SettingSNSActivity.this, false);
						refreshBtn();
					}
				}
			});
		} else {
			Intent intent = new Intent(this, SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_TISTORY);
			startActivityForResult(intent, Constants.REQUEST_SNS_TISTORY);
		}
	}

	public void onDaumBlogBtnClicked() {
		if (permissionFlag.equals("N")) {
			UiController.showDialog(this, R.string.dialog_wait_approve);
			return;
		}

		if (DbController.queryDaumFlag(this)) {
			UiController.showDialog(mContext, R.string.dialog_disconnect_sns, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
						DbController.updateDaumFlag(SettingSNSActivity.this, false);
						refreshBtn();
					}
				}
			});
		} else {
			Intent intent = new Intent(this, SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_DAUM);
			startActivityForResult(intent, Constants.REQUEST_SNS_DAUM);
		}
	}

	public void onFacebookBtnClicked() {
		if (permissionFlag.equals("N")) {
			UiController.showDialog(this, R.string.dialog_wait_approve);
			return;
		}

		if (DbController.queryFaceBookFlag(this)) {
			UiController.showDialog(mContext, R.string.dialog_disconnect_sns, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {

						if (AccessToken.getCurrentAccessToken() != null) {
							LoginManager.getInstance().logOut();
						}

						DbController.updateFaceBookFlag(SettingSNSActivity.this, false);
						refreshBtn();
					}
				}
			});
		} else {
			Intent intent = new Intent(this, SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_FACEBOOK);
			startActivityForResult(intent, Constants.REQUEST_SNS_FACEBOOK);
		}
	}

	public void onTwitterBtnClicked() {
		if (permissionFlag.equals("N")) {
			UiController.showDialog(this, R.string.dialog_wait_approve);
			return;
		}

		if (DbController.queryTwitterFlag(this)) {
			UiController.showDialog(mContext, R.string.dialog_disconnect_sns, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
						DbController.updateTwitterFlag(SettingSNSActivity.this, false);
						refreshBtn();
					}
				}
			});
		} else {
			Intent intent = new Intent(this, SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_TWITTER);
			startActivityForResult(intent, Constants.REQUEST_SNS_TWITTER);
		}
	}

	public void onKakaoBtnClicked() {
		if (permissionFlag.equals("N")) {
			UiController.showDialog(this, R.string.dialog_wait_approve);
			return;
		}

		if (DbController.queryKakaoFlag(this)) {
			UiController.showDialog(mContext, R.string.dialog_disconnect_sns, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				@Override
				public void onDialog(int type) {
					if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
						try
						{
							com.kakao.Session.initializeSession(mContext, null);
							if(!com.kakao.Session.getCurrentSession().isClosed())
							{
								com.kakao.Session.getCurrentSession().close(null);
							}
							DbController.updateKakaoFlag(SettingSNSActivity.this, false);
						} catch (Exception e) {}
				        refreshBtn();
					}
				}
			});
		} else {
			Intent intent = new Intent(this, SnsActivity.class);
			intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO);
			startActivityForResult(intent, Constants.REQUEST_SNS_KAKAO);
		}
	}

}

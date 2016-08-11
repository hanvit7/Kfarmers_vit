package com.leadplatform.kfarmers.view.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.Session;
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
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.gcm.GcmIntentService;
import com.leadplatform.kfarmers.view.diary.DiaryWriteActivity;
import com.leadplatform.kfarmers.view.diary.StoryWriteActivity;
import com.leadplatform.kfarmers.view.login.KakaoLoginHelper;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;
import com.leadplatform.kfarmers.view.main.MainActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;

//탭UI 상속받은 베이스 프래그먼트 엑티비티
public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    private final static String TAG = "BaseFragmentActivity";
    public Context mContext = null;
    public ImageLoader imageLoader = ImageLoader.getInstance();

    public static ArrayList<Activity> mActivityList = new ArrayList<Activity>();

    public abstract void onCreateView(Bundle savedInstanceState);

    public abstract void initActionBar();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        mContext = BaseFragmentActivity.this;

        if (mActivityList != null) {
            for (Activity activity : mActivityList) {
                try {
                    activity.finish();
                } catch (Exception e) {
                }
            }
            mActivityList.clear();
        }

        onCreateView(savedInstanceState);
        initActionBar();
    }

    public void runMainActivity() {
        Log.d(TAG, "runMainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    public void runMainActivity(MainActivity.MainTab currentTab) {
        Log.d(TAG, "runMainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(MainActivity.EXTRA_CURRENT_TAB, currentTab);
        startActivity(intent);
    }

    public void runMainActivity(HashMap<String, String> hashMap) {
        Log.d(TAG, "runMainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(GcmIntentService.PUSH_BUNDLE, hashMap);
        startActivity(intent);
    }

    public void runReplyActivity(int replyType, String diaryTitle,
                                 String diaryIndex) {
        Log.d(TAG, "runReplyActivity");
        Intent intent = new Intent(this, ReplyActivity.class);
        intent.putExtra("replyType", replyType);
        intent.putExtra("diaryTitle", diaryTitle);
        intent.putExtra("diaryIndex", diaryIndex);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.nothing);
    }

    public void runWriteDiaryActivity(String type) {
        Log.d(TAG, "runWriteDiaryActivity");
        if (AppPreferences.getLogin(this)) {
            if (type.equals("U")) {
                Intent intent = new Intent(BaseFragmentActivity.this, StoryWriteActivity.class);
                startActivity(intent);
            } else if (!type.equals("G")) {
                ProfileJson profileJson = getProfile();
                if (profileJson != null) {
                    if (profileJson.PermissionFlag.equals("Y")) {
                        UiController.showDialog(this, R.string.dialog_write_type, R.string.dialog_write_sns, R.string.dialog_write_me, new CustomDialogListener() {
                            @Override
                            public void onDialog(int type) {
                                Log.d(TAG, "onDialog");
                                if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_ActionBar-Write", "가져오기");
                                    Intent intent = DiaryWriteActivity.newIntent(
                                            BaseFragmentActivity.this,
                                            DiaryWriteActivity.DiaryWriteState.IMPORT_FROM_SNS,
                                            null);
                                    startActivity(intent);

                                } else if (type == UiDialog.DIALOG_NEGATIVE_LISTENER) {
                                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_MAIN, "Click_ActionBar-Write", "직접등록");
                                    Intent intent = DiaryWriteActivity.newIntent(
                                            BaseFragmentActivity.this,
                                            DiaryWriteActivity.DiaryWriteState.DIRECT_WRITE,
                                            null);
                                    startActivity(intent);
                                }
                            }
                        });
                    } else {
                        UiController.showDialog(mContext, R.string.dialog_wait_approve, R.string.dialog_call, R.string.dialog_cancel,
                                new CustomDialogListener() {
                                    @Override
                                    public void onDialog(int type) {
                                        Log.d(TAG, "onDialog");
                                        if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                            CommonUtil.AndroidUtil.actionDial(mContext, getResources().getString(R.string.setting_service_center_phone));
                                        }
                                    }
                                });
                    }
                }
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    public void initActionBarHomeBtn() {
        Log.d(TAG, "initActionBarHomeBtn");
        ImageButton homeBtn = (ImageButton) findViewById(R.id.actionbar_home_image_button);
        homeBtn.setVisibility(View.VISIBLE);
        homeBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Log.d(TAG, "viewOnClick");
                runMainActivity();
            }
        });
    }

    public void initActionBarBack() {
        Log.d(TAG, "initActionBarBack");
        ImageButton homeBtn = (ImageButton) findViewById(R.id.actionbar_home_image_button);
        homeBtn.setVisibility(View.VISIBLE);
        homeBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Log.d(TAG, "viewOnClick");
                onBackPressed();
            }
        });
    }

    public void centerLikeDiary(final String index, final OnLikeDiaryListener listener) {
        Log.d(TAG, "centerLikeDiary");
        if (!AppPreferences.getLogin(this)) {
            CenterController.likeNoMemberDiary(index, mContext, new CenterResponseListener(BaseFragmentActivity.this) {
                @Override
                public void onSuccess(int Code, String content) {
                    Log.d(TAG, "onSuccess");
                    try {
                        switch (Code) {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                String type = root.findValue("Type").textValue();

                                if (type.equals("Add")) {
                                    UiController
                                            .toastAddLike(BaseFragmentActivity.this);
                                    listener.onResult(Code, true);
                                } else {
                                    UiController
                                            .toastCancelLike(BaseFragmentActivity.this);
                                    listener.onResult(Code, false);
                                }
                                break;

                            default:
                                listener.onResult(-1, false);
                                UiController.showDialog(BaseFragmentActivity.this,
                                        R.string.dialog_unknown_error);
                                break;
                        }
                    } catch (Exception e) {
                        listener.onResult(-1, false);
                        UiController.showDialog(BaseFragmentActivity.this,
                                R.string.dialog_unknown_error);
                    }
                }
            });
            return;
        }

        CenterController.likeDiary(index, new CenterResponseListener(
                BaseFragmentActivity.this) {
            @Override
            public void onSuccess(int Code, String content) {
                Log.d(TAG, "onSuccess");
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);
                            String type = root.findValue("Type").textValue();

                            if (type.equals("Add")) {
                                UiController
                                        .toastAddLike(BaseFragmentActivity.this);
                                listener.onResult(Code, true);
                            } else {
                                UiController
                                        .toastCancelLike(BaseFragmentActivity.this);
                                listener.onResult(Code, false);
                            }
                            break;

                        default:
                            listener.onResult(-1, false);
                            UiController.showDialog(BaseFragmentActivity.this,
                                    R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    listener.onResult(-1, false);
                    UiController.showDialog(BaseFragmentActivity.this,
                            R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void centerLikeUserDiary(final String index,
                                    final OnLikeDiaryListener listener) {
        Log.d(TAG, "centerLikeUserDiary");
        if (!AppPreferences.getLogin(this)) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            return;
        }

        CenterController.likeUserDiary(index, new CenterResponseListener(
                BaseFragmentActivity.this) {
            @Override
            public void onSuccess(int Code, String content) {
                Log.d(TAG, "onSuccess");
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);
                            String type = root.findValue("Type").textValue();
                            if (type.equals("Add")) {
                                UiController
                                        .toastAddLike(BaseFragmentActivity.this);
                                listener.onResult(Code, true);
                            } else {
                                UiController
                                        .toastCancelLike(BaseFragmentActivity.this);
                                listener.onResult(Code, false);
                            }
                            break;

                        default:
                            listener.onResult(-1, false);
                            UiController.showDialog(BaseFragmentActivity.this,
                                    R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    listener.onResult(-1, false);
                    UiController.showDialog(BaseFragmentActivity.this,
                            R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void centerDeleteDiary(final String index,
                                  final OnDeleteDiaryListener listener) {
        Log.d(TAG, "centerDeleteDiary");
        CenterController.deleteDiary(index, new CenterResponseListener(this) {
            @Override
            public void onSuccess(int Code, String content) {
                Log.d(TAG, "onSuccess");
                try {
                    switch (Code) {
                        case 0000:
                            listener.onResult(true);
                            break;

                        default:
                            listener.onResult(false);
                            UiController.showDialog(BaseFragmentActivity.this,
                                    R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    listener.onResult(false);
                    UiController.showDialog(BaseFragmentActivity.this,
                            R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void centerDeleteConsumer(final String index,
                                     final OnDeleteDiaryListener listener) {
        Log.d(TAG, "centerDeleteConsumer");
        CenterController.deleteConsumer(index,
                new CenterResponseListener(this) {
                    @Override
                    public void onSuccess(int Code, String content) {
                        Log.d(TAG, "onSuccess");
                        try {
                            switch (Code) {
                                case 0000:
                                    listener.onResult(true);
                                    break;

                                default:
                                    listener.onResult(false);
                                    UiController.showDialog(
                                            BaseFragmentActivity.this,
                                            R.string.dialog_unknown_error);
                                    break;
                            }
                        } catch (Exception e) {
                            listener.onResult(false);
                            UiController.showDialog(BaseFragmentActivity.this,
                                    R.string.dialog_unknown_error);
                        }
                    }
                });
    }

    public void centerDeleteInterview(final String index,
                                      final OnDeleteDiaryListener listener) {
        Log.d(TAG, "centerDeleteInterview");
        CenterController.deleteInterview(index,
                new CenterResponseListener(this) {
                    @Override
                    public void onSuccess(int Code, String content) {
                        Log.d(TAG, "onSuccess");
                        try {
                            switch (Code) {
                                case 0000:
                                    listener.onResult(true);
                                    break;

                                default:
                                    listener.onResult(false);
                                    UiController.showDialog(
                                            BaseFragmentActivity.this,
                                            R.string.dialog_unknown_error);
                                    break;
                            }
                        } catch (Exception e) {
                            listener.onResult(false);
                            UiController.showDialog(BaseFragmentActivity.this,
                                    R.string.dialog_unknown_error);
                        }
                    }
                });
    }

    public ProfileJson getProfile() {
        Log.d(TAG, "getProfile");
        ProfileJson profileJson = null;
        try {
            String profile = DbController.queryProfileContent(mContext);
            JsonNode root = JsonUtil.parseTree(profile);
            profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return profileJson;
    }

    public void leaveMember() {
        Log.d(TAG, "leaveMember");
        try {
            String profile = DbController.queryProfileContent(mContext);
            JsonNode root = JsonUtil.parseTree(profile);
            ProfileJson profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
            String regId = AppPreferences.getGcmRegistrationId(mContext);

            CenterController.leaveMember(profileJson.ID, profileJson.Type, regId, new CenterResponseListener(mContext) {
                @Override
                public void onSuccess(int Code, String content) {
                    Log.d(TAG, "onSuccess");
                    try {
                        switch (Code) {
                            case 0000:
                                DbController.clearDb(mContext);
                                AppPreferences.setLogin(mContext, false);
                                AppPreferences.setGcmSend(mContext, false);

                                if (AccessToken.getCurrentAccessToken() != null) {
                                    LoginManager.getInstance().logOut();
                                }

                                NaverLoginHelper.naverLogout(mContext);

                                Session.initializeSession(mContext, null);
                                if (!Session.getCurrentSession().isClosed()) {
                                    Session.getCurrentSession().close(null);
                                }

                                SnipeApiController.tokenExpired(new SnipeResponseListener(mContext) {
                                    @Override
                                    public void onSuccess(int Code, String content, String error) {
                                        Log.d(TAG, "onSuccess");
                                        getToken();
                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                                        Log.d(TAG, "onFailure");
                                        super.onFailure(statusCode, headers, content, error);
                                        getToken();
                                    }
                                });
                                break;
                            default:
                                UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    }
                }
            });
        } catch (Exception e) {
        }
    }

    public void logout() {
        Log.d(TAG, "logout");
        String regId = AppPreferences.getGcmRegistrationId(mContext);

        CenterController.logout(regId, new CenterResponseListener(mContext) {
            @Override
            public void onSuccess(int Code, String content) {
                Log.d(TAG, "onSuccess");
                try {
                    switch (Code) {
                        case 0000:
                            DbController.clearDb(mContext);

                            AppPreferences.setLogin(mContext, false);
                            AppPreferences.setGcmSend(mContext, false);


                            if (AccessToken.getCurrentAccessToken() != null) {
                                LoginManager.getInstance().logOut();
                            }

                            NaverLoginHelper.naverLogout(mContext);

                            Session.initializeSession(mContext, null);
                            if (!Session.getCurrentSession().isClosed()) {
                                KakaoLoginHelper.kakaoLogout(mContext);
                                Session.getCurrentSession().close(null);
                            }

                            SnipeApiController.tokenExpired(new SnipeResponseListener(mContext) {
                                @Override
                                public void onSuccess(int Code, String content, String error) {
                                    Log.d(TAG, "onSuccess");
                                    getToken();
                                }

                                @Override
                                public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                                    Log.d(TAG, "onFailure");
                                    super.onFailure(statusCode, headers, content, error);
                                    getToken();
                                }
                            });
                            break;
                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                }
            }
        });
    }

    public void getToken() {
        Log.d(TAG, "getToken");
        String id = "";
        String pw = "";

        UserDb user = DbController.queryCurrentUser(mContext);
        if (user != null && user.getAutoLoginFlag() == 1) {
            id = user.getUserID();
            pw = user.getUserPwDecrypt();
        }

        SnipeApiController.getToken(id, pw, new SnipeResponseListener(mContext) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                Log.d(TAG, "onSuccess");
                switch (Code) {
                    case 600:
                        DbController.updateApiToken(mContext, content);
                        SnipeApiController.setToken(content);
                        runMainActivity();
                        break;
                    default:
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                        DbController.updateApiToken(mContext, content);
                        SnipeApiController.setToken(content);
                        runMainActivity();
                        break;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                Log.d(TAG, "onFailure");
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }
}

package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.item.SnsPostItem;
import com.leadplatform.kfarmers.model.item.WDiaryItem;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.model.json.DiaryDetailJson;
import com.leadplatform.kfarmers.model.json.MyFaceBookAlbumJson;
import com.leadplatform.kfarmers.model.json.MyFaceBookJson;
import com.leadplatform.kfarmers.model.json.MyKakaoStoryJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.WeatherJson;
import com.leadplatform.kfarmers.model.parcel.RowsData;
import com.leadplatform.kfarmers.model.parcel.WriteDiaryData;
import com.leadplatform.kfarmers.model.preference.AppPreferences;
import com.leadplatform.kfarmers.service.SnsPostingService;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.OnLoadingCompleteListener;
import com.leadplatform.kfarmers.view.common.DialogFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.leadplatform.kfarmers.view.common.TagActivity;
import com.leadplatform.kfarmers.view.common.WeatherActivity;
import com.leadplatform.kfarmers.view.diary.DiaryWriteDragListFragment.DragAdapter;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.leadplatform.kfarmers.view.sns.SnsActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class DiaryWriteActivity extends BaseFragmentActivity implements OnLoadingCompleteListener {
    public static final String TAG = "DiaryWriteActivity";

    // 소비자 글쓰기가 이전에 있었는데 지금은 안쓰임 관련내용 우선 주석처리 해놈
//    private enum UserType {
//        FARMER, EXPERIENCE_VILLAGE, CONSUMER
//    }
//
//    private UserType mUserType = UserType.FARMER;

    public enum DiaryWriteState {DIRECT_WRITE, MODIFY, IMPORT_FROM_SNS, IMPORT_COMPLETED}
    private DiaryWriteState mDiaryWriteState = DiaryWriteState.DIRECT_WRITE;

    public final static int FACEBOOK_ALBUM = 1000;

    private ProfileJson profileData;
    private RelativeLayout mWriteDiaryCategory,
            mWriteDiaryTitleForSnsNotice;
    private TextView mWriteDiaryCategoryTextView;
    private EditText mWriteDiaryTitleEditTextForSNSnNotice;

    private ImageView mWriteDiaryFooterPictureImageView,
            mWriteDiaryFooterTagImageView,
            mWriteDiaryFooterExportSNSImageView,
            mWriteDiaryFooterWeatherImageView;
    private Button mWriteDiaryFooterTextButton;
    private View mWriteDiaryFooterDivider1,
            mWriteDiaryFooterDivider2,
            mWriteDiaryFooterDivider3,
            mWriteDiaryFooterDivider4;
    private String tag, tempTag, weather, temperature, humidity;
    private int categoryIndex = 0, noticeIndex = 0;

    private RelativeLayout mWriteDiaryExportSnsLayout;
    private ToggleButton mWriteDiaryAlignImageForSNSnNotice,
            mWriteDiaryExportNaverBlogButton,
            mWriteDiaryExportTistoryButton,
            mWriteDiaryExportDaumBlogButton,
            mWriteDiaryExportFacebookButton,
            mWriteDiaryExportTwitterButton,
            mWriteDiaryExportKakaoStoryButton;

    private ArrayList<String> categoryList;
    private ArrayList<CategoryJson> categoryObjectList;
    private DiaryDetailJson detailData;
    private String mDetailData;
    private String kakaoStoryText = null;
    private String naverBlogText = null;
    private ArrayList<File> imageArrayList = null;
    //    private String mWritePermission;
    private boolean TemporaryDiary = false;
    private String date;
    private String snsType = "";

    private ArrayList<String> imgPath;

    private Button mActionBarUploadButton;

    private ArrayList<MyFaceBookAlbumJson> albumJsons;
    public ImageLoader imageLoader = ImageLoader.getInstance();
    private ProgressDialog mDialog;

    private static final String EXTRA_DETAIL_DATA = "kr.kfarmers.detail_data";
    private static final String EXTRA_DIARY_WRITE_STATE = "kr.kfarmers.diary_write_state";

    public static Intent newIntent(Context packageContext, DiaryWriteState diaryWriteState,
                                   String detailData) {
        Intent intent = new Intent(packageContext, DiaryWriteActivity.class);
        intent.putExtra(EXTRA_DIARY_WRITE_STATE, diaryWriteState);
        intent.putExtra(EXTRA_DETAIL_DATA, detailData);
        return intent;
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        setContentView(R.layout.activity_write_diary);

        Intent intent = getIntent();
        if (intent != null) {
            mDiaryWriteState = (DiaryWriteState) getIntent()
                    .getSerializableExtra(EXTRA_DIARY_WRITE_STATE);

            if (getIntent().getStringExtra(EXTRA_DETAIL_DATA) != null) {
                mDetailData = getIntent().getStringExtra(EXTRA_DETAIL_DATA);
            }
        }

        detailData = new DiaryDetailJson();
        imgPath = new ArrayList<>();

        getUserProfile();
        initContentView(savedInstanceState);
        requestCategory();
        requestWeather();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume ");
        super.onResume();

        //소 비 자
//        if (mUserType != UserType.FARMER
//                && mUserType != UserType.EXPERIENCE_VILLAGE) {
//            if (!checkEditDiary() && mDiaryWriteState != DiaryWriteState.IMPORT_FROM_SNS)
//                checkTemporaryDiary();
//        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed ");
        if (mWriteDiaryExportSnsLayout.getVisibility() == View.VISIBLE) {
            mWriteDiaryExportSnsLayout.setVisibility(View.GONE);
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        DiaryBlogWriteFragment fragment = (DiaryBlogWriteFragment) fm.findFragmentByTag(DiaryBlogWriteFragment.TAG);

        if (fragment != null) {
            if (fragment.webView.canGoBack()) {
                fragment.webView.goBack();
                return;
            }
        }

        onTextFooterClicked();
        super.onBackPressed();
    }

    private void getUserProfile() {
        Log.d(TAG, "getUserProfile ");
        try {
            UserDb user = DbController.queryCurrentUser(this);
            JsonNode root = JsonUtil.parseTree(user.getProfileContent());
            profileData = (ProfileJson) JsonUtil.jsonToObject(
                    root.findPath("Row").toString(), ProfileJson.class);
//            mWritePermission =
//
//            switch (profileData.Type) {
//                case "F":
//                    mUserType = UserType.FARMER;
//                    break;
//                case "V":
//                    mUserType = UserType.EXPERIENCE_VILLAGE;
//                    break;
//                case "U":
////                    mUserType = UserType.CONSUMER;
//                    Assert.fail("unused user type");
//                    break;
//                default:
//                    Assert.fail("unused user type");
//                    break;
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initContentView(Bundle savedInstanceState) {
        Log.d(TAG, "initContentView ");
        mWriteDiaryCategory = (RelativeLayout) findViewById(R.id.write_diary_category);
        mWriteDiaryCategory.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onCategoryButtonClicked();
            }
        });

        mWriteDiaryCategoryTextView = (TextView) findViewById(R.id.write_diary_category_text_view);
        mWriteDiaryTitleForSnsNotice = (RelativeLayout) findViewById(R.id.write_diary_title_for_sns_n_notice);
        mWriteDiaryTitleEditTextForSNSnNotice = (EditText) findViewById(R.id.write_diary_title_edit_text_for_sns_n_notice);
        mWriteDiaryAlignImageForSNSnNotice = (ToggleButton) findViewById(R.id.write_diary_align_image_for_sns_n_notice);

        mWriteDiaryFooterPictureImageView = (ImageView) findViewById(R.id.write_diary_footer_picture_Image_view);
        mWriteDiaryFooterPictureImageView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onPictureFooterClicked(v);//camera button
            }
        });

        mWriteDiaryFooterTagImageView = (ImageView) findViewById(R.id.write_diary_footer_tag_image_view);
        mWriteDiaryFooterTagImageView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Tag", null);
                onTagFooterClicked();
            }
        });

        mWriteDiaryFooterExportSNSImageView = (ImageView) findViewById(R.id.write_diary_footer_export_sns_image_view);
        mWriteDiaryFooterExportSNSImageView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share", null);
                onExportFooterClicked();
            }
        });

        mWriteDiaryFooterWeatherImageView = (ImageView) findViewById(R.id.write_diary_footer_weather_image_view);
        mWriteDiaryFooterWeatherImageView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Weather", null);
                onWeatherFooterClicked();
            }
        });

        mWriteDiaryFooterTextButton = (Button) findViewById(R.id.write_diary_footer_temporary_save_button);
        mWriteDiaryFooterTextButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_TempSave", null);
                onTextFooterClicked();
            }
        });

        mWriteDiaryFooterDivider1 = findViewById(R.id.write_diary_footer_divider1);
        mWriteDiaryFooterDivider2 = findViewById(R.id.write_diary_footer_divider2);
        mWriteDiaryFooterDivider3 = findViewById(R.id.write_diary_footer_divider3);
        mWriteDiaryFooterDivider4 = findViewById(R.id.write_diary_footer_divider4);

        mWriteDiaryExportSnsLayout = (RelativeLayout) findViewById(R.id.write_diary_export_sns_layout);

        mWriteDiaryExportNaverBlogButton = (ToggleButton) findViewById(R.id.write_diary_export_naver_blog_button);
        mWriteDiaryExportNaverBlogButton.setChecked(DbController.queryNaverFlag(this));
        mWriteDiaryExportNaverBlogButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share-Sns", "네이버");
                onNaverBlogBtnClicked();
            }
        });

        mWriteDiaryExportTistoryButton = (ToggleButton) findViewById(R.id.write_diary_export_tistory_button);
        mWriteDiaryExportTistoryButton.setChecked(DbController.queryTistoryFlag(this));
        mWriteDiaryExportTistoryButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share-Sns", "티스토리");
                onTiStoryBtnClicked();
            }
        });

        mWriteDiaryExportDaumBlogButton = (ToggleButton) findViewById(R.id.write_diary_export_daum_blog_button);
        mWriteDiaryExportDaumBlogButton.setChecked(DbController.queryDaumFlag(this));
        mWriteDiaryExportDaumBlogButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share-Sns", "다음");
                onDaumBlogBtnClicked();
            }
        });

        mWriteDiaryExportFacebookButton = (ToggleButton) findViewById(R.id.write_diary_export_facebook_button);
        mWriteDiaryExportFacebookButton.setChecked(DbController.queryFaceBookFlag(this));
        mWriteDiaryExportFacebookButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share-Sns", "페이스북");
                onFacebookBtnClicked();
            }
        });

        mWriteDiaryExportTwitterButton = (ToggleButton) findViewById(R.id.write_diary_export_twitter_button);
        mWriteDiaryExportTwitterButton.setChecked(DbController.queryTwitterFlag(this));
        mWriteDiaryExportTwitterButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share-Sns", "트위터");
                onTwitterBtnClicked();
            }
        });

        mWriteDiaryExportKakaoStoryButton = (ToggleButton) findViewById(R.id.write_diary_export_kakao_story_button);
        mWriteDiaryExportKakaoStoryButton.setChecked(DbController.queryKakaoFlag(this));
        mWriteDiaryExportKakaoStoryButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Share-Sns", "카카오스토리");
                onKakaoBtnClicked();
            }
        });

        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            if (mDiaryWriteState == DiaryWriteState.IMPORT_FROM_SNS) {
//                DiaryBlogWriteFragment fragment = ;
                ft.replace(
                        R.id.fragment_container,
                        DiaryBlogWriteFragment.newInstance(),
                        DiaryBlogWriteFragment.TAG);

                KfarmersAnalytics.onScreen(KfarmersAnalytics.S_WRITE_SNS);
            } else {
                DiaryWriteDragListFragment fragment = DiaryWriteDragListFragment.newInstance(
                        (mDetailData == null)
                                ? DiaryWriteState.DIRECT_WRITE
                                : DiaryWriteState.IMPORT_FROM_SNS);
                ft.replace(R.id.fragment_container, fragment, DiaryWriteDragListFragment.TAG);

                if (mDetailData == null) {
                    KfarmersAnalytics.onScreen(KfarmersAnalytics.S_WRITE);
                } else {
                    KfarmersAnalytics.onScreen(KfarmersAnalytics.S_WRITE_MODIFY);
                }
            }
            ft.commit();
        }
    }

    @Override
    public void initActionBar() {
        Log.d(TAG, "initActionBar ");
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView actionBarTitleTextView = (TextView) findViewById(R.id.actionbar_title_text_view);
        actionBarTitleTextView.setText(R.string.write_diary_title);

        Button actionBarCancelButton = (Button) findViewById(R.id.actionbar_left_button);
        actionBarCancelButton.setVisibility(View.VISIBLE);
        actionBarCancelButton.setText(R.string.actionbar_cancel);
        actionBarCancelButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Cancel", null);
                onActionBarCancelButtonClicked();
            }
        });

        mActionBarUploadButton = (Button) findViewById(R.id.actionbar_right_button);
        mActionBarUploadButton.setVisibility(View.GONE);
        mActionBarUploadButton.setText(R.string.actionbar_upload);
        mActionBarUploadButton.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(getType(), "Click_Write", null);
                onActionBarUploadButtonClicked();
            }
        });

        displayInitButton();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu ");
        if (snsType.equals("페이스북")) {
            menu.setHeaderTitle("페이스북 사진첩");

            int i = 0;
            for (MyFaceBookAlbumJson albumJson : albumJsons) {
                menu.add(0, i++, 0, albumJson.name);
            }
        } else {
            menu.setHeaderTitle(R.string.context_menu_camera_title);
            getMenuInflater().inflate(R.menu.menu_camera, menu);
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(TAG, "onContextItemSelected ");
        if (snsType.equals("페이스북")) {
            try {
                KfarmersAnalytics.onClick(getType(), "Click_Picture", "페이스북");
                galleryChoice(
                        albumJsons.get(item.getItemId()).id,
                        date,
                        DiaryWriteDragListFragment.MAX_FACEBOOK_PICTURE_NUMBER);
            } catch (Exception e) {
            }
        } else {
            switch (item.getItemId()) {
                case R.id.btn_camera_capture:
                    imgPath.clear();
                    imgPath.add(ImageUtil.takePictureFromCamera(
                            this, Constants.REQUEST_TAKE_CAPTURE));
                    KfarmersAnalytics.onClick(getType(), "Click_Picture", "촬영");
                    return true;

                case R.id.btn_camera_gallery:
                    // 기본 안드로이드 갤러리
                    //ImageUtil.takePictureFromGallery(this, Constants.REQUEST_TAKE_PICTURE);
                    galleryChoice(
                            "",
                            "",
                            DiaryWriteDragListFragment.MAX_PICTURE_NUMBER);
                    KfarmersAnalytics.onClick(getType(), "Click_Picture", "불러오기");
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }

    public void galleryChoice(String faceBookId, String faceBookDate, int maxCount) {
        Log.d(TAG, "galleryChoice ");
        int pictureNum = 0;
        FragmentManager fm = getSupportFragmentManager();
        DiaryWriteDragListFragment fragment =
                (DiaryWriteDragListFragment) fm.findFragmentByTag(DiaryWriteDragListFragment.TAG);

        if (fragment != null) {
            pictureNum = fragment.getPictureNum();
        }
        ImageUtil.takeSelectFromGallery(mContext, maxCount, pictureNum, faceBookId, faceBookDate, Constants.REQUEST_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult ");
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_TAKE_CAPTURE) {
                runImageRotateActivity(Constants.REQUEST_TAKE_CAPTURE, imgPath);
                return;
            } else if (requestCode == Constants.REQUEST_TAKE_PICTURE) {
                if (null == data.getData()) {
                    return;
                }

                imgPath.add(ImageUtil.getConvertPathMediaStoreImageFile(this, data));
                runImageRotateActivity(Constants.REQUEST_TAKE_PICTURE, imgPath);
                return;
            } else if (requestCode == Constants.REQUEST_GALLERY) {
                imgPath = data.getStringArrayListExtra("imagePath");
                runImageRotateActivity(Constants.REQUEST_GALLERY, imgPath);
                return;
            } else if (requestCode == Constants.REQUEST_ROTATE_PICTURE) {
                imgPath = data.getStringArrayListExtra("imagePath");
                addPictureListView(imgPath);
                return;
            } else if (requestCode == Constants.REQUEST_TAG) {
                tag = data.getStringExtra("tag");
                return;
            } else if (requestCode == Constants.REQUEST_WEATHER) {
                weather = data.getStringExtra("weather");
                temperature = data.getStringExtra("temperature");
                humidity = data.getStringExtra("humidity");
                return;
            } else if (requestCode == Constants.REQUEST_SNS_NAVER) {
                mWriteDiaryExportNaverBlogButton.setChecked(true);
                DbController.updateNaverFlag(DiaryWriteActivity.this, true);
                return;
            } else if (requestCode == Constants.REQUEST_SNS_TISTORY) {
                mWriteDiaryExportTistoryButton.setChecked(true);
                DbController.updateTistoryFlag(DiaryWriteActivity.this, true);
                return;
            } else if (requestCode == Constants.REQUEST_SNS_DAUM) {
                mWriteDiaryExportDaumBlogButton.setChecked(true);
                DbController.updateDaumFlag(DiaryWriteActivity.this, true);
                return;
            } else if (requestCode == Constants.REQUEST_SNS_FACEBOOK) {
                mWriteDiaryExportFacebookButton.setChecked(true);
                DbController.updateFaceBookFlag(DiaryWriteActivity.this, true);
                return;
            } else if (requestCode == Constants.REQUEST_SNS_TWITTER) {
                mWriteDiaryExportTwitterButton.setChecked(true);
                DbController.updateTwitterFlag(DiaryWriteActivity.this, true);
                return;
            } else if (requestCode == Constants.REQUEST_SNS_KAKAO) {
                mWriteDiaryExportKakaoStoryButton.setChecked(true);
                DbController.updateKakaoFlag(DiaryWriteActivity.this, true);
                return;
            } else if (requestCode == FACEBOOK_ALBUM) {
                galleryChoice(
                        data.getStringExtra("id"),
                        date,
                        DiaryWriteDragListFragment.MAX_FACEBOOK_PICTURE_NUMBER);
                return;
            }
        } else {
            //imgPath.clear();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void bloginitButton() {
        Log.d(TAG, "bloginitButton ");
        mWriteDiaryFooterTextButton.setVisibility(View.VISIBLE);
    }

    public void kakaoinitButton() {
        Log.d(TAG, "kakaoinitButton ");
        mWriteDiaryFooterTextButton.setVisibility(View.GONE);
    }

    private void displayInitButton() {
        Log.d(TAG, "displayInitButton ");
        switch (mDiaryWriteState) {
            case DIRECT_WRITE:
                mActionBarUploadButton.setVisibility(View.VISIBLE);

                mWriteDiaryCategory.setVisibility(View.VISIBLE);
                mWriteDiaryTitleForSnsNotice.setVisibility(View.VISIBLE);

                mWriteDiaryFooterPictureImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider1.setVisibility(View.VISIBLE);
                mWriteDiaryFooterTagImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider2.setVisibility(View.VISIBLE);
                mWriteDiaryFooterExportSNSImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider3.setVisibility(View.VISIBLE);
                mWriteDiaryFooterWeatherImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider4.setVisibility(View.VISIBLE);
                mWriteDiaryFooterTextButton.setText(getString(R.string.WriteDiaryButtonSave));
                break;

            case MODIFY:
                mWriteDiaryTitleEditTextForSNSnNotice.setTextColor(Color.GRAY);
                mWriteDiaryTitleEditTextForSNSnNotice.setEnabled(false);
                mWriteDiaryAlignImageForSNSnNotice.setBackgroundResource(R.drawable.btn_write_diary_align_disable);
                mWriteDiaryAlignImageForSNSnNotice.setEnabled(false);
                mWriteDiaryFooterTagImageView.setImageResource(R.drawable.button_blog_write_label_default_disable);
                mWriteDiaryFooterTagImageView.setEnabled(false);
                mWriteDiaryFooterExportSNSImageView.setImageResource(R.drawable.button_blog_write_share_default_disable);
                mWriteDiaryFooterExportSNSImageView.setEnabled(false);
                mWriteDiaryFooterDivider4.setVisibility(View.INVISIBLE);
                mWriteDiaryFooterTextButton.setVisibility(View.INVISIBLE);
                break;

            case IMPORT_FROM_SNS:
                mActionBarUploadButton.setVisibility(View.GONE);

                mWriteDiaryCategory.setVisibility(View.GONE);
                mWriteDiaryTitleForSnsNotice.setVisibility(View.GONE);

                mWriteDiaryFooterPictureImageView.setVisibility(View.GONE);
                mWriteDiaryFooterDivider1.setVisibility(View.GONE);
                mWriteDiaryFooterTagImageView.setVisibility(View.GONE);
                mWriteDiaryFooterDivider2.setVisibility(View.GONE);
                mWriteDiaryFooterExportSNSImageView.setVisibility(View.GONE);
                mWriteDiaryFooterDivider3.setVisibility(View.GONE);
                mWriteDiaryFooterWeatherImageView.setVisibility(View.GONE);
                mWriteDiaryFooterDivider4.setVisibility(View.GONE);
                mWriteDiaryFooterTextButton.setText("가져오기");
                mWriteDiaryFooterTextButton.setVisibility(View.GONE);
                break;

            case IMPORT_COMPLETED:
                mActionBarUploadButton.setVisibility(View.VISIBLE);

                mWriteDiaryCategory.setVisibility(View.VISIBLE);
                mWriteDiaryTitleForSnsNotice.setVisibility(View.GONE);

                mWriteDiaryFooterPictureImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider1.setVisibility(View.VISIBLE);
                mWriteDiaryFooterTagImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider2.setVisibility(View.GONE);
                mWriteDiaryFooterExportSNSImageView.setVisibility(View.GONE);
                mWriteDiaryFooterDivider3.setVisibility(View.VISIBLE);
                mWriteDiaryFooterWeatherImageView.setVisibility(View.VISIBLE);
                mWriteDiaryFooterDivider4.setVisibility(View.GONE);
                mWriteDiaryFooterTextButton.setVisibility(View.GONE);
                break;
        }

//        if (mDiaryWriteState == DiaryWriteState.IMPORT_FROM_SNS) {
//        } else if (mDiaryWriteState == DiaryWriteState.IMPORT_COMPLETED) {
//        } else {
//        }

//        if (mUserType == UserType.CONSUMER
//            /*|| mUserType == TYPE_USER_NORMAL
//              || mUserType == TYPE_USER_INTERVIEW*/) {
//            mWriteDiaryTitleForSnsNotice.setVisibility(View.GONE);
//            mWriteDiaryFooterTagImageView.setVisibility(View.INVISIBLE);
//            mWriteDiaryFooterDivider2.setVisibility(View.INVISIBLE);
//            mWriteDiaryFooterExportSNSImageView.setVisibility(View.INVISIBLE);
//            mWriteDiaryFooterDivider3.setVisibility(View.INVISIBLE);
//            mWriteDiaryFooterWeatherImageView.setVisibility(View.INVISIBLE);
//        }
//        if (mDiaryWriteState == DiaryWriteState.MODIFY) {
//        }

//        if (mUserType == UserType.FARMER
//                || mUserType == UserType.EXPERIENCE_VILLAGE) {
//        mWritePermission
        if (profileData.TemporaryPermissionFlag.equals("N")) {//do it test
            mWriteDiaryFooterTagImageView.setImageResource(R.drawable.button_blog_write_label_default_disable);
            mWriteDiaryFooterTagImageView.setEnabled(false);
            mWriteDiaryFooterExportSNSImageView.setImageResource(R.drawable.button_blog_write_share_default_disable);
            mWriteDiaryFooterExportSNSImageView.setEnabled(false);
        }
//        }
    }

    private void runImageRotateActivity(int takeType, ArrayList<String> imgPath) {
        Log.d(TAG, "runImageRotateActivity ");
        Intent intent = new Intent(this, ImageRotateActivity.class);
        intent.putExtra("takeType", takeType);
        //intent.putExtra("imagePath", path);
        intent.putStringArrayListExtra("imagePath", imgPath);
        startActivityForResult(intent, Constants.REQUEST_ROTATE_PICTURE);
    }

    private void addPictureListView(ArrayList<String> imgPath) {
        Log.d(TAG, "addPictureListView ");
        FragmentManager fm = getSupportFragmentManager();
        DiaryWriteDragListFragment fragment = (DiaryWriteDragListFragment) fm.findFragmentByTag(DiaryWriteDragListFragment.TAG);

        if (fragment != null) {
            fragment.addListViewPictureItem(imgPath);
        }
    }

    public void onActionBarCancelButtonClicked() {
        Log.d(TAG, "onActionBarCancelButtonClicked ");
        onTextFooterClicked();
        finish();
    }

    public void onActionBarUploadButtonClicked() {
        Log.d(TAG, "onActionBarUploadButtonClicked ");
//        if (mUserType == UserType.FARMER
//                || mUserType == UserType.EXPERIENCE_VILLAGE) {
        if (categoryIndex == 0) {
            UiController.showDialog(mContext, R.string.dialog_product_reg_category);
            return;
        } else if (categoryList.get(categoryIndex).equals("공지사항")) {
            if (PatternUtil.isEmpty(mWriteDiaryTitleEditTextForSNSnNotice.getText().toString())) {
                UiController.showDialog(mContext, R.string.dialog_empty_title);
                return;
            }
        }
//        }

        mDialog = new ProgressDialog(DiaryWriteActivity.this);
        mDialog.setMessage("글 등록 중입니다.");
        mDialog.show();
        mDialog.setCancelable(false);

        FragmentManager fm = getSupportFragmentManager();
        DiaryWriteDragListFragment fragment = (DiaryWriteDragListFragment) fm.findFragmentByTag(DiaryWriteDragListFragment.TAG);
        if (fragment != null) {
            int textCount = 0;
            int pictureCount = 0;

            kakaoStoryText = "";
            naverBlogText = "";
            imageArrayList = new ArrayList<>();

            ArrayList<RowsData> rows = new ArrayList<>();
            ArrayList<String> images = new ArrayList<>();
            DragAdapter adapter = (DragAdapter) fragment.getListAdapter();

            for (int index = 0; index < adapter.getCount(); index++) {
                RowsData row = new RowsData();
                WDiaryItem item = adapter.getItem(index);
                if (item.getType() == WDiaryItem.TEXT_TYPE) {
                    row.Type = "Text";
                    row.Value = item.getTextContent();
                    if (item.getTextContent() != null && item.getTextContent().trim().length() > 0) {
                        textCount++;
                        kakaoStoryText += (item.getTextContent() + "\n");
                        naverBlogText += (item.getTextContent() + "\n");
                    }
                } else if (item.getType() == WDiaryItem.PICTURE_TYPE) {
                    row.Type = "Image";
                    row.Value = String.valueOf(pictureCount);
                    images.add(item.getPictureContent());
                    imageArrayList.add(new File(item.getPictureContent()));
                    naverBlogText += ("<img src='#" + pictureCount + "'/>" + "\n");
                    pictureCount++;
                }
                naverBlogText += "\n";

                if (!TextUtils.isEmpty(row.Value))
                    rows.add(row);
            }

            if (textCount == 0 || pictureCount == 0) {
                UiController.showDialog(mContext, R.string.dialog_empty_contents);
                mDialog.dismiss();
                return;
            }

            kakaoStoryText += "\n";
            kakaoStoryText += "\n";
            kakaoStoryText += "#" + mWriteDiaryCategoryTextView.getText().toString().trim() + " ";

            if (tag != null && !tag.isEmpty()) {
                String str[] = tag.split(",");
                tempTag = mWriteDiaryCategoryTextView.getText().toString();

                if (str.length > 0) {
                    for (String subStr : str) {
                        kakaoStoryText += "#" + subStr + " ";
                        tempTag += "," + subStr;
                    }
                }
            } else {
                tempTag = mWriteDiaryCategoryTextView.getText().toString();
            }

            WriteDiaryData data = new WriteDiaryData();

//            if (mUserType == UserType.FARMER
//                    || mUserType == UserType.EXPERIENCE_VILLAGE) {
            if (categoryObjectList.get(categoryIndex).SubIndex != null)
                data.setCategory(Integer.valueOf(categoryObjectList.get(categoryIndex).SubIndex));
//            }

            data.setBlogTitle(mWriteDiaryTitleEditTextForSNSnNotice.getText().toString());
            data.setbAlign(mWriteDiaryAlignImageForSNSnNotice.isChecked());
            data.setBlogTag(tempTag);

            data.setbNaver(false);
            data.setbTistory(false);
            data.setbDaum(false);
            data.setbFacebook(false);
            data.setbTwitter(false);
            data.setbKakao(false);

            if (mWriteDiaryFooterExportSNSImageView.isEnabled()) {
                data.setbTistory(mWriteDiaryExportTistoryButton.isChecked());
                data.setbDaum(mWriteDiaryExportDaumBlogButton.isChecked());
                data.setbFacebook(mWriteDiaryExportFacebookButton.isChecked());
                data.setbTwitter(mWriteDiaryExportTwitterButton.isChecked());
                data.setbNaver(mWriteDiaryExportNaverBlogButton.isChecked());
                data.setbKakao(mWriteDiaryExportKakaoStoryButton.isChecked());
            }

            data.setWeather(weather);
            data.setTemperature(temperature);
            data.setHumidity(humidity);
            data.setRows(rows);
            data.setImages(images);
            data.setDate(date);
            data.setFrom(snsType);

            if (mDiaryWriteState == DiaryWriteState.DIRECT_WRITE
                    || mDiaryWriteState == DiaryWriteState.IMPORT_COMPLETED) {
//                if (mUserType == UserType.FARMER
//                        || mUserType == UserType.EXPERIENCE_VILLAGE) {
                if (data.getCategory() == 0) {
                    CenterController.writeNotice(data, new CenterResponseListener(this) {
                        @Override
                        public void onSuccess(int Code, String content) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    mDialog.dismiss();
                                }
                            });

                            try {
                                switch (Code) {
                                    case 0:
                                        DbController.updateTemporaryDiary(DiaryWriteActivity.this, "");
                                        Toast toast = Toast.makeText(mContext, R.string.toast_success_write_diary, Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();

                                        try {
                                            Intent intent = new Intent(mContext, FarmActivity.class);
                                            intent.putExtra("userType", profileData.Type);
                                            intent.putExtra("userIndex", profileData.Index);
                                            startActivity(intent);
                                        } catch (Exception e) {
                                        }
                                        finish();
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

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                            super.onFailure(statusCode, headers, content, error);
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    mDialog.dismiss();
                                }
                            });
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                        }
                    });
                } else {
                    downloadImageAndPost(data);
                }
//                }
            } else {
//                if (mUserType == UserType.FARMER
//                        || mUserType == UserType.EXPERIENCE_VILLAGE) {
                CenterController.editDiary(detailData.Diary, data, new CenterResponseListener(this) {
                    @Override
                    public void onSuccess(int Code, String content) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                            }
                        });

                        try {
                            switch (Code) {
                                case 0:
                                    Toast toast = Toast.makeText(mContext, R.string.toast_success_edit_diary, Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();

                                    try {
                                        Intent intent = new Intent(mContext, FarmActivity.class);
                                        intent.putExtra("userType", profileData.Type);
                                        intent.putExtra("userIndex", profileData.Index);
                                        startActivity(intent);
                                    } catch (Exception e) {
                                    }

                                    finish();
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

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                        super.onFailure(statusCode, headers, content, error);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                            }
                        });
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    }
                });
//                }
            }
        } else {
            mDialog.dismiss();
            UiController.showDialog(mContext, R.string.dialog_unknown_error);
        }
    }

    private void getSnsDiaryData() {
        Log.d(TAG, "getSnsDiaryData ");
        FragmentManager fm = getSupportFragmentManager();
        DiaryBlogWriteFragment fragment = (DiaryBlogWriteFragment) fm.findFragmentByTag(DiaryBlogWriteFragment.TAG);

        if (fragment != null) {
            UiController.showProgressDialog(mContext);

            switch (fragment.snsIndex) {
                case 1:
                case 2:
                case 3:
                    fragment.getHtml();
                    break;

                case 4:
                    if (fragment.selectItem >= 0) {
                        startKakaoWrite(fragment);
                    }
                    UiController.hideProgressDialog(mContext);
                    break;

                case 5:
                    if (fragment.selectItem >= 0) {
                        startFaceBookWrite(fragment);
                    }
                    break;

                default:
                    UiController.hideProgressDialog(mContext);
                    break;
            }
        }
    }

    private void startFaceBookWrite(DiaryBlogWriteFragment fragment) {
        Log.d(TAG, "startFaceBookWrite ");
        if (fragment != null) {
            final MyFaceBookJson info = (MyFaceBookJson) fragment.getListView().getItemAtPosition(fragment.selectItem);
            startFaceBookWriteSub(info);
        } else {
            UiController.hideProgressDialog(mContext);
        }
    }

    private void startFaceBookWriteSub(MyFaceBookJson info) {
        Log.d(TAG, "startFaceBookWriteSub ");
        mDiaryWriteState = DiaryWriteState.IMPORT_COMPLETED;

        date = info.created_time;
        snsType = "페이스북";
        albumJsons = null;

        ArrayList<RowJson> rows = new ArrayList<>();

        RowJson row = new RowJson();
        row.Type = "Text";

        if (info.message != null) {
            row.Value = info.message;
        } else if (info.description != null) {
            row.Value = info.description;
        }

        rows.add(row);

        if (info.source != null) {
            row = new RowJson();
            row.Type = "Image";
            row.Value = info.source;
            rows.add(row);
        }

        DiaryDetailJson diaryDetailJson = new DiaryDetailJson();
        diaryDetailJson.Rows = rows;

        try {
            mDetailData = JsonUtil.objectToJson(diaryDetailJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        DiaryWriteDragListFragment diaryWriteDragListFragment = DiaryWriteDragListFragment.newInstance(mDiaryWriteState);
        ft.replace(R.id.fragment_container, diaryWriteDragListFragment, DiaryWriteDragListFragment.TAG);
        ft.commit();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                displayInitButton();
                UiController.hideProgressDialog(mContext);
            }
        });
    }

    private void startKakaoWrite(DiaryBlogWriteFragment fragment) {
        Log.d(TAG, "startKakaoWrite ");
        if (fragment != null) {
            boolean isEmpty = true;

            MyKakaoStoryJson info = (MyKakaoStoryJson) fragment.getListView().getItemAtPosition(fragment.selectItem);
            String html = info.content;

            int str = html.lastIndexOf("http://m.kfarmers.kr/");

            if (str > 0) {
                html = html.replace(html.substring(str, html.length()), "");
            }

            str = html.lastIndexOf(Constants.KFARMERS_FULL_M_DOMAIN + "/");

            if (str > 0) {
                html = html.replace(html.substring(str, html.length()), "");
            }

            tag = fragment.tag;

            mDiaryWriteState = DiaryWriteState.IMPORT_COMPLETED;

            date = info.created_at;
            snsType = "카카오스토리";

            ArrayList<RowJson> rows = new ArrayList<>();

            if (!PatternUtil.isEmpty(html.trim())) {
                RowJson row = new RowJson();
                row.Type = "Text";
                row.Value = html;
                rows.add(row);

                isEmpty = false;
            }

            if (info.media_type.equals("PHOTO") || info.media_type.equals("image")) {
                for (int i = 0; i < info.media.length; i++) {
                    RowJson row = new RowJson();
                    row.Type = "Image";
                    row.Value = info.media[i];
                    rows.add(row);

                    isEmpty = false;
                }
            }

            if (isEmpty) {
                return;
            }

            DiaryDetailJson diaryDetailJson = new DiaryDetailJson();
            diaryDetailJson.Rows = rows;

            try {
                mDetailData = JsonUtil.objectToJson(diaryDetailJson);
            } catch (Exception e) {
                e.printStackTrace();
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            DiaryWriteDragListFragment diaryWriteDragListFragment = DiaryWriteDragListFragment.newInstance(mDiaryWriteState);
            ft.replace(R.id.fragment_container, diaryWriteDragListFragment, DiaryWriteDragListFragment.TAG);

            ft.commit();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    displayInitButton();
                }
            });
        }
    }

    private void startBlogWrite() {
        Log.d(TAG, "startBlogWrite ");
        FragmentManager fm = getSupportFragmentManager();
        DiaryBlogWriteFragment fragment =
                (DiaryBlogWriteFragment) fm.findFragmentByTag(DiaryBlogWriteFragment.TAG);

        if (fragment != null) {
            String html = fragment.content;
            tag = fragment.tag;
            date = fragment.date;
            snsType = fragment.snsText.getText().toString();

            if (!PatternUtil.isEmpty(html)) {
                mDiaryWriteState = DiaryWriteState.IMPORT_COMPLETED;

                String str[] = html.split("==]img]==");

                ArrayList<RowJson> rows = new ArrayList<>();

                for (String aStr : str) {
                    if (!aStr.trim().equals("")) {
                        RowJson row = new RowJson();

                        if (aStr.startsWith("http://")) {
                            row.Type = "Image";
                        } else {
                            row.Type = "Text";
                        }
                        row.Value = aStr.trim();
                        rows.add(row);
                    }
                }

                DiaryDetailJson diaryDetailJson = new DiaryDetailJson();
                diaryDetailJson.Rows = rows;

                try {
                    mDetailData = JsonUtil.objectToJson(diaryDetailJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                DiaryWriteDragListFragment diaryWriteDragListFragment =
                        DiaryWriteDragListFragment.newInstance(mDiaryWriteState);
                ft.replace(R.id.fragment_container, diaryWriteDragListFragment, DiaryWriteDragListFragment.TAG);
                ft.commit();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayInitButton();
                    }
                });
            } else {
                UiController.showDialog(mContext, R.string.dialog_blog_no_post);
            }
        }
    }

    //임 시 저 장
    private String makeWriteDiaryData() {
        Log.d(TAG, "makeWriteDiaryData ");
        boolean bEmptyContentFlag = true;
        DiaryDetailJson data = new DiaryDetailJson();

        FragmentManager fm = getSupportFragmentManager();
        DiaryWriteDragListFragment fragment = (DiaryWriteDragListFragment) fm.findFragmentByTag(DiaryWriteDragListFragment.TAG);

        if (fragment != null) {
            ArrayList<RowJson> rows = new ArrayList<>();
            DragAdapter adapter = (DragAdapter) fragment.getListAdapter();

            for (int index = 0; index < adapter.getCount(); index++) {
                RowJson row = new RowJson();
                WDiaryItem item = adapter.getItem(index);

                if (item.getType() == WDiaryItem.TEXT_TYPE) {
                    row.Type = "Text";
                    row.Value = item.getTextContent();
                } else if (item.getType() == WDiaryItem.PICTURE_TYPE) {
                    row.Type = "Image";
                    row.Value = item.getPictureContent();
                }

                if (!TextUtils.isEmpty(row.Value))
                    rows.add(row);

                if (!TextUtils.isEmpty(row.Value))
                    bEmptyContentFlag = false;
            }

            if (categoryObjectList != null)
                data.CategoryIndex = categoryObjectList.get(categoryIndex).SubIndex;
            data.BlogTitle = mWriteDiaryTitleEditTextForSNSnNotice.getText().toString();
            data.BlogAlign = (mWriteDiaryAlignImageForSNSnNotice.isChecked() ? "C" : "L");
            data.BlogTag = tag;
            data.Sky = weather;
            data.Temperature = temperature;
            data.Humidity = humidity;
            data.Rows = rows;
        }

        try {
            if (bEmptyContentFlag)
                return null;

            return JsonUtil.objectToJson(data);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onPictureFooterClicked(View view) {
        Log.d(TAG, "onPictureFooterClicked");
        FragmentManager fm = getSupportFragmentManager();
        DiaryWriteDragListFragment dragListFragment =
                (DiaryWriteDragListFragment) fm.findFragmentByTag(DiaryWriteDragListFragment.TAG);

        if (dragListFragment != null) {
            int count = dragListFragment.getPictureNum();

            if (snsType.equals("페이스북")) {
                if (count >= DiaryWriteDragListFragment.MAX_FACEBOOK_PICTURE_NUMBER) {
                    UiController.showDialog(mContext, String.format(mContext.getString(R.string.toast_img_max), DiaryWriteDragListFragment.MAX_FACEBOOK_PICTURE_NUMBER));
                    return;
                }
            } else {
                if (count >= DiaryWriteDragListFragment.MAX_PICTURE_NUMBER) {
                    UiController.showDialog(mContext, String.format(mContext.getString(R.string.toast_img_max), DiaryWriteDragListFragment.MAX_PICTURE_NUMBER));
                    return;
                }
            }

            if (mDiaryWriteState == DiaryWriteState.IMPORT_COMPLETED) {
                if (snsType.equals("페이스북")) {
                    if (albumJsons == null) {
                        //albumJsons = new ArrayList<MyFaceBookAlbumJson>();
                        //faceBookAlbumList("");
                        startActivityForResult(new Intent(mContext, FacebookAlbumActivity.class), FACEBOOK_ALBUM);
                    } else {
                        registerForContextMenu(view);
                        openContextMenu(view);
                        unregisterForContextMenu(view);
                    }
                } else {
                    KfarmersAnalytics.onClick(getType(), "Click_Picture", "불러오기");
                    galleryChoice(
                            "",
                            "",
                            DiaryWriteDragListFragment.MAX_PICTURE_NUMBER);
                }
            } else {
                registerForContextMenu(view);
                openContextMenu(view);
                unregisterForContextMenu(view);
            }
        }
    }

    public void onTagFooterClicked() {
        Log.d(TAG, "onTagFooterClicked");
        Intent intent = new Intent(this, TagActivity.class);
        intent.putExtra("tag", tag);

        if (categoryIndex != 0) {
            intent.putExtra("categoryTag", mWriteDiaryCategoryTextView.getText().toString());
        } else {
            intent.putExtra("categoryTag", "");
        }

        startActivityForResult(intent, Constants.REQUEST_TAG);
    }

    public void onExportFooterClicked() {
        Log.d(TAG, "onExportFooterClicked");
        if (mWriteDiaryExportSnsLayout.getVisibility() == View.GONE) {
            mWriteDiaryExportSnsLayout.setVisibility(View.VISIBLE);
        } else {
            mWriteDiaryExportSnsLayout.setVisibility(View.GONE);
        }
    }

    public void onWeatherFooterClicked() {
        Log.d(TAG, "onWeatherFooterClicked");
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra("weather", weather);
        intent.putExtra("temperature", temperature);
        intent.putExtra("humidity", humidity);
        startActivityForResult(intent, Constants.REQUEST_WEATHER);
    }

    public void onTextFooterClicked() {
        Log.d(TAG, "onTextFooterClicked");
        switch (mDiaryWriteState) {
            case DIRECT_WRITE:
                String data = makeWriteDiaryData();
                if (data != null) {
                    DbController.updateTemporaryDiary(DiaryWriteActivity.this, data);
                    Toast.makeText(DiaryWriteActivity.this, R.string.dialog_success_temporary_diary, Toast.LENGTH_LONG).show();
                    // UiController.showDialog(DiaryWriteActivity.this, R.string.dialog_success_temporary_diary);
                }
                break;

            case MODIFY:
                break;

            case IMPORT_FROM_SNS:
                getSnsDiaryData();
                break;

            case IMPORT_COMPLETED:
//                data = makeWriteDiaryData();//data define check vit
//                if (data != null) {
                    //DbController.updateTemporaryDiary(DiaryWriteActivity.this, data);
//                    Toast.makeText(DiaryWriteActivity.this, R.string.dialog_success_temporary_diary, Toast.LENGTH_LONG).show();
                    // UiController.showDialog(DiaryWriteActivity.this, R.string.dialog_success_temporary_diary);
//                }
                break;
        }
    }

    private void requestWeather() {
        Log.d(TAG, "requestWeather");
        double latitude = AppPreferences.getLatitude(this);
        double longitude = AppPreferences.getLongitude(this);

        //소 비 자
//        if (mUserType != UserType.FARMER && mUserType != UserType.EXPERIENCE_VILLAGE)
//            return;

        // UserDb user = DbController.queryCurrentUser(DiaryWriteActivity.this);
        if (latitude == 0 || longitude == 0)
            return;

        CenterController.weather(latitude, longitude, new CenterResponseListener(this) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0:
                            JsonNode root = JsonUtil.parseTree(content);
                            WeatherJson weatherJson = (WeatherJson) JsonUtil.jsonToObject(root.findPath("Rows").toString(), WeatherJson.class);

                            if (weatherJson != null) {
                                temperature = weatherJson.Temperature;
                                humidity = weatherJson.Humidity;
                                weather = weatherJson.Sky;
                            }
                            break;

                        case 1001:
                        case 1002:
                            UiController.showDialog(mContext, R.string.dialog_invalid_location);
                            break;

                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void requestCategory() {
        Log.d(TAG, "requestCategory");
        if (categoryObjectList == null) {
            UserDb user = DbController.queryCurrentUser(this);
            categoryList = new ArrayList<>();
            categoryObjectList = new ArrayList<>();

            //소 비 자
//            if (mUserType != UserType.FARMER && mUserType != UserType.EXPERIENCE_VILLAGE) {
//                categoryIndex = 0;
//                categoryList.add("소비자이야기");
//                categoryObjectList.add(new CategoryJson());
//                mWriteDiaryCategoryTextView.setText(categoryList.get(0));
//                return;
//            } else {
            categoryList.add("카테고리 선택");
            categoryObjectList.add(new CategoryJson());
//            }

            CenterController.getCategory(user.getUserID(), new CenterResponseListener(this) {
                @Override
                public void onSuccess(int Code, String content) {
                    try {
                        switch (Code) {
                            case 0:
                                JsonNode root = JsonUtil.parseTree(content);
                                if (root.findPath("List").isArray()) {
                                    for (JsonNode jsonNode : root.findPath("List")) {
                                        CategoryJson category = (CategoryJson) JsonUtil.jsonToObject(jsonNode.toString(), CategoryJson.class);
                                        categoryList.add(category.SubName);
                                        categoryObjectList.add(category);
                                    }
                                    categoryList.add("공지사항");
                                    categoryObjectList.add(new CategoryJson());

                                    categoryIndex = 0;
                                    noticeIndex = categoryList.size() - 1;
                                    mWriteDiaryCategoryTextView.setText(categoryList.get(0));

                                    if (!checkEditDiary() && mDiaryWriteState != DiaryWriteState.IMPORT_FROM_SNS) {
                                        checkTemporaryDiary();
                                    }
                                }
                                break;

                            case 1002:
                                UiController.showDialog(DiaryWriteActivity.this, R.string.dialog_empty_release);
                                break;

                            default:
                                UiController.showDialog(DiaryWriteActivity.this, R.string.dialog_unknown_error);
                                break;
                        }
                    } catch (Exception e) {
                        UiController.showDialog(DiaryWriteActivity.this, R.string.dialog_unknown_error);
                    }
                }
            });
        }
    }

    public void onNaverBlogBtnClicked() {
        Log.d(TAG, "onNaverBlogBtnClicked");
        if (mWriteDiaryExportNaverBlogButton.isChecked() && !DbController.queryNaverFlag(this)) {
            mWriteDiaryExportNaverBlogButton.setChecked(false);
            Intent intent = new Intent(this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_NAVER);
            startActivityForResult(intent, Constants.REQUEST_SNS_NAVER);
        }
    }

    public void onTiStoryBtnClicked() {
        Log.d(TAG, "onTiStoryBtnClicked");
        if (mWriteDiaryExportTistoryButton.isChecked() && !DbController.queryTistoryFlag(this)) {
            mWriteDiaryExportTistoryButton.setChecked(false);
            Intent intent = new Intent(this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_TISTORY);
            startActivityForResult(intent, Constants.REQUEST_SNS_TISTORY);
        }
    }

    public void onDaumBlogBtnClicked() {
        Log.d(TAG, "onDaumBlogBtnClicked");
        if (mWriteDiaryExportDaumBlogButton.isChecked() && !DbController.queryDaumFlag(this)) {
            mWriteDiaryExportDaumBlogButton.setChecked(false);
            Intent intent = new Intent(this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_DAUM);
            startActivityForResult(intent, Constants.REQUEST_SNS_DAUM);
        }
    }

    public void onFacebookBtnClicked() {
        Log.d(TAG, "onFacebookBtnClicked");
        if (mWriteDiaryExportFacebookButton.isChecked() && !DbController.queryFaceBookFlag(this)) {
            mWriteDiaryExportFacebookButton.setChecked(false);
            Intent intent = new Intent(this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_FACEBOOK);
            startActivityForResult(intent, Constants.REQUEST_SNS_FACEBOOK);
        }
    }

    public void onTwitterBtnClicked() {
        Log.d(TAG, "onTwitterBtnClicked");
        if (mWriteDiaryExportTwitterButton.isChecked() && !DbController.queryTwitterFlag(this)) {
            mWriteDiaryExportTwitterButton.setChecked(false);
            Intent intent = new Intent(this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_TWITTER);
            startActivityForResult(intent, Constants.REQUEST_SNS_TWITTER);
        }
    }

    public void onKakaoBtnClicked() {
        Log.d(TAG, "onKakaoBtnClicked");
        if (mWriteDiaryExportKakaoStoryButton.isChecked() && !DbController.queryKakaoFlag(this)) {
            mWriteDiaryExportKakaoStoryButton.setChecked(false);
            Intent intent = new Intent(this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO);
            startActivityForResult(intent, Constants.REQUEST_SNS_KAKAO);
        }
    }

    private void onCategoryButtonClicked() {
        Log.d(TAG, "onCategoryButtonClicked");
        DialogFragment fragment = DialogFragment.newInstance(
                0,
                categoryIndex,
                categoryList.toArray(new String[categoryList.size()]),
                "");
        fragment.setOnCloseCategoryDialogListener(new OnCloseCategoryDialogListener() {
            @Override
            public void onDialogSelected(int subMenuType, int position) {
                if (categoryIndex != position) {
                    categoryIndex = position;
                    mWriteDiaryCategoryTextView.setText(categoryList.get(position));

                    KfarmersAnalytics.onClick(getType(), "Click_Category", categoryList.get(position));

                    if (mDiaryWriteState == DiaryWriteState.DIRECT_WRITE
//                            && (mUserType == UserType.FARMER
//                            || mUserType == UserType.EXPERIENCE_VILLAGE)
                            ) {
                        if (categoryIndex == noticeIndex) {
                            mWriteDiaryAlignImageForSNSnNotice.setBackgroundResource(R.drawable.btn_write_diary_align_disable);
                            mWriteDiaryAlignImageForSNSnNotice.setEnabled(false);
                            mWriteDiaryFooterTagImageView.setImageResource(R.drawable.button_blog_write_label_default_disable);
                            mWriteDiaryFooterTagImageView.setEnabled(false);
                            mWriteDiaryFooterExportSNSImageView.setImageResource(R.drawable.button_blog_write_share_default_disable);
                            mWriteDiaryFooterExportSNSImageView.setEnabled(false);
                            mWriteDiaryFooterWeatherImageView.setImageResource(R.drawable.button_blog_write_sky_default_disable);
                            mWriteDiaryFooterWeatherImageView.setEnabled(false);
                        } else {
                            mWriteDiaryAlignImageForSNSnNotice.setBackgroundResource(R.drawable.btn_write_diary_align);
                            mWriteDiaryAlignImageForSNSnNotice.setEnabled(true);
                            mWriteDiaryFooterTagImageView.setImageResource(R.drawable.button_blog_write_label_default);
                            mWriteDiaryFooterTagImageView.setEnabled(true);
                            mWriteDiaryFooterExportSNSImageView.setImageResource(R.drawable.button_blog_write_share_default);
                            mWriteDiaryFooterExportSNSImageView.setEnabled(true);
                            mWriteDiaryFooterWeatherImageView.setImageResource(R.drawable.button_blog_write_sky_default);
                            mWriteDiaryFooterWeatherImageView.setEnabled(true);

                            displayInitButton();
                        }
                    }
                }
            }
        });
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, DialogFragment.TAG);
    }

    private void checkTemporaryDiary() {
        Log.d(TAG, "checkTemporaryDiary");
        if (!TemporaryDiary) {
            TemporaryDiary = true;
            String diary = DbController.queryTemporaryDiary(this);

            if (diary != null && !diary.isEmpty()) {
                UiController.showDialog(
                        DiaryWriteActivity.this,
                        R.string.dialog_load_temporary_diary,
                        R.string.actionbar_ok,
                        R.string.actionbar_cancel,
                        new CustomDialogListener() {
                            @Override
                            public void onDialog(int type) {
                                if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                    if (mDiaryWriteState == DiaryWriteState.IMPORT_FROM_SNS) {
                                        mDiaryWriteState = DiaryWriteState.IMPORT_COMPLETED;
                                        displayInitButton();
                                        mDetailData = DbController.queryTemporaryDiary(DiaryWriteActivity.this);

                                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                        DiaryWriteDragListFragment diaryWriteDragListFragment = DiaryWriteDragListFragment.newInstance(mDiaryWriteState);
                                        ft.replace(R.id.fragment_container, diaryWriteDragListFragment, DiaryWriteDragListFragment.TAG);
                                        ft.remove(getSupportFragmentManager().findFragmentByTag(DialogFragment.TAG));
                                        ft.commit();
                                    } else {
                                        displayUpdateDiaryData(DbController.queryTemporaryDiary(DiaryWriteActivity.this));//load temporary diary
                                    }
                                } else {
                                    DbController.updateTemporaryDiary(DiaryWriteActivity.this, "");//delete temporary diary
                                }
                            }
                        });
            }
        }
    }

    private boolean checkEditDiary() {
        Log.d(TAG, "checkEditDiary");
        if (mDetailData != null) {
            displayUpdateDiaryData(mDetailData);
            //mDetailData = null;
            return true;
        }
        return false;
    }

    private void displayUpdateDiaryData(String diary) {
        Log.d(TAG, "displayUpdateDiaryData");
        try {
            detailData = (DiaryDetailJson) JsonUtil.jsonToObject(diary, DiaryDetailJson.class);

            if (categoryObjectList != null) {
                int index = 0;
                for (CategoryJson category : categoryObjectList) {
                    if (category.SubIndex != null && category.SubIndex.equals(detailData.CategoryIndex)) {
                        categoryIndex = index;
                        mWriteDiaryCategoryTextView.setText(category.SubName);
                    }
                    index++;
                }
            }

            if (!TextUtils.isEmpty(detailData.BlogTitle))
                mWriteDiaryTitleEditTextForSNSnNotice.setText(detailData.BlogTitle);

            if (!TextUtils.isEmpty(detailData.BlogAlign))
                mWriteDiaryAlignImageForSNSnNotice.setChecked(detailData.BlogAlign.equals("C"));

            if (!TextUtils.isEmpty(detailData.BlogTag))
                tag = detailData.BlogTag;

            if (!TextUtils.isEmpty(detailData.Sky))
                weather = detailData.Sky;

            if (!TextUtils.isEmpty(detailData.Temperature))
                temperature = detailData.Temperature;

            if (!TextUtils.isEmpty(detailData.Humidity))
                humidity = detailData.Humidity;

            if (detailData.Rows != null) {
                FragmentManager fm = getSupportFragmentManager();
                DiaryWriteDragListFragment fragment = (DiaryWriteDragListFragment) fm.findFragmentByTag(DiaryWriteDragListFragment.TAG);
                if (fragment != null) {
                    DragAdapter adapter = (DragAdapter) fragment.getListAdapter();
                    if (adapter != null) {
                        adapter.clear();

                        for (RowJson row : detailData.Rows) {
                            WDiaryItem item = new WDiaryItem();

                            if (row.Type.equals("Text")) {
                                item.setType(WDiaryItem.TEXT_TYPE);
                                item.setTextContent(row.Value);
                            } else if (row.Type.equals("Image")) {
                                item.setType(WDiaryItem.PICTURE_TYPE);
                                item.setPictureContent(row.Value);
                            }
                            adapter.add(item);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void downloadImageAndPost(WriteDiaryData data) {
        Log.d(TAG, "downloadImageAndPost");
        final DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true)
                .build();

        AsyncTask<WriteDiaryData, Void, WriteDiaryData> task = new AsyncTask<WriteDiaryData, Void, WriteDiaryData>() {
            @Override
            protected WriteDiaryData doInBackground(WriteDiaryData... data) {
                ArrayList<String> img = new ArrayList<>();
                for (String path : data[0].getImages()) {
                    try {
                        if (path.contains("http")) {
                            if (imageLoader.getDiskCache().get(path) == null
                                    || !imageLoader.getDiskCache().get(path).isFile()) {
                                imageLoader.loadImageSync(path, options);
                            }
                        }
                        img.add(ImageUtil.getFilePath(path));
                    } catch (Exception e) {
                    }
                }
                data[0].setImages(img);
                return data[0];
            }

            @Override
            protected void onPostExecute(final WriteDiaryData data) {
                super.onPostExecute(data);

                CenterController.writeDiary(data, new CenterResponseListener(DiaryWriteActivity.this) {
                    @Override
                    public void onSuccess(int Code, String content) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                            }
                        });
                        try {
                            switch (Code) {
                                case 0:
                                    if (mDiaryWriteState == DiaryWriteState.IMPORT_COMPLETED) {
                                        UiDialog.showDialog(
                                                mContext,
                                                getString(R.string.dialog_blog_info4),
                                                new CustomDialogListener() {
                                                    @Override
                                                    public void onDialog(int type) {
                                                        Toast toast = Toast.makeText(
                                                                mContext,
                                                                R.string.toast_success_write_diary,
                                                                Toast.LENGTH_SHORT);
                                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                                        toast.show();

                                                        try {
                                                            Intent intent = new Intent(mContext, FarmActivity.class);
                                                            intent.putExtra("userType", profileData.Type);
                                                            intent.putExtra("userIndex", profileData.Index);
                                                            startActivity(intent);
                                                        } catch (Exception e) {
                                                        }

                                                        finish();
                                                    }
                                                });
                                    } else {
                                        if (data.isbKakao() || data.isbNaver() || data.isbFacebook()) {
                                            try {
                                                SnsPostItem snsPostItem = new SnsPostItem();

                                                snsPostItem.setIsFaceBook(data.isbFacebook());
                                                snsPostItem.setIsKakao(data.isbKakao());
                                                snsPostItem.setIsNaver(data.isbNaver());

                                                HashMap<String, String> faceBookData = new HashMap<>();
                                                faceBookData.put(SnsPostItem.TEXT, kakaoStoryText);
                                                faceBookData.put(SnsPostItem.TAG, profileData.ID);
                                                snsPostItem.setFaceBookData(faceBookData);

                                                HashMap<String, String> kakaoData = new HashMap<>();
                                                kakaoData.put(SnsPostItem.TEXT, kakaoStoryText);
                                                snsPostItem.setKakaoData(kakaoData);

												/*naverBlogText += "\n\n<a href=\"http://kfarmers.kr/link\">새로운 직거래 생태계, K파머스</a>";*/
                                                HashMap<String, String> naverData = new HashMap<>();
                                                naverData.put(SnsPostItem.TITLE, mWriteDiaryTitleEditTextForSNSnNotice.getText().toString());
                                                naverData.put(SnsPostItem.TEXT, naverBlogText);
                                                naverData.put(SnsPostItem.TAG, data.getBlogTag());
                                                snsPostItem.setNaverData(naverData);

                                                snsPostItem.setImages(data.getImages());

                                                Intent intent = new Intent(mContext, SnsPostingService.class);
                                                intent.putExtra(SnsPostingService.DATA, snsPostItem);
                                                startService(intent);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        DbController.updateTemporaryDiary(DiaryWriteActivity.this, "");
                                        Toast toast = Toast.makeText(
                                                mContext,
                                                R.string.toast_success_write_diary,
                                                Toast.LENGTH_SHORT);
                                        toast.setGravity(Gravity.CENTER, 0, 0);
                                        toast.show();

                                        try {
                                            Intent intent = new Intent(mContext, FarmActivity.class);
                                            intent.putExtra("userType", profileData.Type);
                                            intent.putExtra("userIndex", profileData.Index);
                                            startActivity(intent);
                                        } catch (Exception e) {
                                        }

                                        finish();
                                    }
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

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                        super.onFailure(statusCode, headers, content, error);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                            }
                        });
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                    }

                    @Override
                    public void onCancel() {
                        super.onCancel();
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mDialog.dismiss();
                            }
                        });
                        UiController.showDialog(mContext, "이미지 파일을 찾을 수가 없습니다.\n 이미지를 삭제 후 다시 추가해주세요");
                    }
                });
            }
        };
        task.execute(data);
    }

    @Override
    public void OnLoadingComplete(String tag, boolean success) {
        Log.d(TAG, "OnLoadingComplete");
        if (tag.equals(DiaryWriteDragListFragment.TAG)) {
            checkEditDiary();
        } else if (tag.equals(DiaryBlogWriteFragment.TAG)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "OnLoadingComplete run");
                    UiController.hideProgressDialog(mContext);
                }
            });

            if (success) {
                startBlogWrite();
            } else {
                UiController.showDialog(mContext, R.string.dialog_blog_no_post);
            }
        }

    }

    private String getType() {
        Log.d(TAG, "getType");
        if (mDiaryWriteState == DiaryWriteState.DIRECT_WRITE) {
            return KfarmersAnalytics.S_WRITE;
        } else if (mDiaryWriteState == DiaryWriteState.IMPORT_COMPLETED) {
            return KfarmersAnalytics.S_WRITE_SNS;
        } else {
            return KfarmersAnalytics.S_WRITE_MODIFY;
        }
    }
}

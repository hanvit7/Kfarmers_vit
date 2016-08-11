package com.leadplatform.kfarmers.view.diary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.kakao.SessionCallback;
import com.kakao.exception.KakaoException;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.item.SnsPostItem;
import com.leadplatform.kfarmers.model.json.RowJson;
import com.leadplatform.kfarmers.model.json.StoryDetailJson;
import com.leadplatform.kfarmers.model.parcel.RowsData;
import com.leadplatform.kfarmers.model.parcel.WriteDiaryData;
import com.leadplatform.kfarmers.service.SnsPostingService;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.DialogFragment;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.leadplatform.kfarmers.view.sns.SnsActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class StoryWriteActivity extends BaseFragmentActivity {
    public static final String TAG = "StoryWriteActivity";

    LinearLayout mImageLayout;
    RelativeLayout mLayoutCategory;
    TextView mTextCategory;
    EditText mEditContent;
    ImageView mImagePicture, mImageShare;
    Button mBtnTemp;

    private LayoutInflater mInflater;
    private DisplayImageOptions mImageOption;

    private int mCategoryIndex = -1;
    private ArrayList<String> mCategoryList;
    private ArrayList<String> tempPath;
    private ArrayList<String> mImgPath;

    private String mModifyData, mType, mIndex;

    private View mTagLayout, mTagLayout1, mTagLayout2, mTagLayout3;

    private EditText mEditTag1, mEditTag2, mEditTag3;
    private Button mBtnTag1, mBtnTag2, mBtnTag3;
    private TextView mTextNotice;

    private ProgressDialog mDialog;

    /***************************************************************/
    // Override

    /***************************************************************/

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_write_story);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getStringExtra("detail") != null) {
                mModifyData = getIntent().getStringExtra("detail");
                mType = getIntent().getStringExtra("type");
                mIndex = getIntent().getStringExtra("index");
            }
        }

        mTextNotice = (TextView) findViewById(R.id.noticeText);

        mTagLayout = findViewById(R.id.tagLayout);
        mTagLayout1 = findViewById(R.id.tagLayout1);
        mTagLayout2 = findViewById(R.id.tagLayout2);
        mTagLayout3 = findViewById(R.id.tagLayout3);

        mBtnTag1 = (Button) findViewById(R.id.button1);
        mBtnTag2 = (Button) findViewById(R.id.button2);
        mBtnTag3 = (Button) findViewById(R.id.button3);

        mBtnTag1.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (mTagLayout2.getVisibility() == View.GONE) {
                    mTagLayout2.setVisibility(View.VISIBLE);
                } else if (mTagLayout3.getVisibility() == View.GONE) {
                    mTagLayout3.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(StoryWriteActivity.this, R.string.dialog_tag_max, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnTag2.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                mTagLayout2.setVisibility(View.GONE);
            }
        });

        mBtnTag3.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                mTagLayout3.setVisibility(View.GONE);
            }
        });

        mEditTag1 = (EditText) findViewById(R.id.tagEdit1);
        mEditTag2 = (EditText) findViewById(R.id.tagEdit2);
        mEditTag3 = (EditText) findViewById(R.id.tagEdit3);

        InputFilter filterKor = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\u318D\\u119E\\u11A2\\u2022\\u2025a\\u00B7\\uFE55]+$");
                if (!ps.matcher(source).matches()) {
                    return "";
                }
                return null;
            }
        };

        mEditTag1.setFilters(new InputFilter[]{filterKor});
        mEditTag2.setFilters(new InputFilter[]{filterKor});
        mEditTag3.setFilters(new InputFilter[]{filterKor});

        mLayoutCategory = (RelativeLayout) findViewById(R.id.CategoryLayout);
        mTextCategory = (TextView) findViewById(R.id.CategoryText);

        mEditContent = (EditText) findViewById(R.id.EditDes);

        mImagePicture = (ImageView) findViewById(R.id.pictureBtn);
        mImageShare = (ImageView) findViewById(R.id.shareBtn);
        mBtnTemp = (Button) findViewById(R.id.tempBtn);

        mImageLayout = (LinearLayout) findViewById(R.id.ImageLayout);

        mImageShare.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (!DbController.querySnsKakaoUse(mContext)) {
                    kakaoCheck();
                } else {
                    DbController.updateSnsKakaoUse(mContext, false);
                    snsSettiong(false);
                }
            }
        });

        mLayoutCategory.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                categoryBtnClicked();
            }
        });

        mImagePicture.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (mImgPath.size() >= DiaryWriteDragListFragment.MAX_PICTURE_NUMBER) {

                } else {
                    pictureBtnClicked(v);
                }
            }
        });

        mBtnTemp.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onSaveBtnClicked();
            }
        });

        mCategoryList = new ArrayList<String>();
        mCategoryList.add("밥상수다");
        mCategoryList.add("일상");

        tempPath = new ArrayList<String>();
        mImgPath = new ArrayList<String>();

        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();

        if (mModifyData != null) {
            mTextNotice.setVisibility(View.GONE);
            makeModify();
            mImageShare.setVisibility(View.GONE);
            mBtnTemp.setVisibility(View.GONE);
        } else {
            mTextNotice.setVisibility(View.VISIBLE);
            checkTemporaryDiary();
            snsSettiong(true);
        }
    }

    @Override
    public void onBackPressed() {
        onSaveBtnClicked();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    public void onSaveBtnClicked() {
        if (mModifyData == null) {
            String data = makeWriteStroyData();
            if (data != null) {
                DbController.updateTemporaryDiary(StoryWriteActivity.this, data);
                Toast.makeText(StoryWriteActivity.this, R.string.dialog_success_temporary_diary, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void snsSettiong(boolean isinit) {
        if (!DbController.querySnsKakaoUse(mContext)) {
            mImageShare.setImageResource(R.drawable.icon_kako_s_n);
            if (!isinit)
                Toast.makeText(StoryWriteActivity.this, "카카오스토리 공유 해제 하셨습니다", Toast.LENGTH_SHORT).show();
        } else {
            mImageShare.setImageResource(R.drawable.icon_kako_s_y);
            if (!isinit)
                Toast.makeText(StoryWriteActivity.this, "카카오스토리 공유 설정 하셨습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void kakaoCheck() {
        if (com.kakao.Session.initializeSession(this, new SessionCallback() {
            @Override
            public void onSessionOpened() {
                UiController.hideProgressDialog(StoryWriteActivity.this);
                DbController.updateSnsKakaoUse(mContext, true);
                snsSettiong(false);
            }

            @Override
            public void onSessionClosed(KakaoException exception) {
                UiController.hideProgressDialog(StoryWriteActivity.this);
                DbController.updateKakaoFlag(StoryWriteActivity.this, false);
                DbController.updateSnsKakaoUse(mContext, false);
                snsSettiong(false);
            }
        })) {
            UiController.showProgressDialog(StoryWriteActivity.this);
        } else if (com.kakao.Session.getCurrentSession().isOpened()) {
            UiController.hideProgressDialog(StoryWriteActivity.this);
            DbController.updateSnsKakaoUse(mContext, true);
            snsSettiong(false);
        } else {
            Intent intent = new Intent(StoryWriteActivity.this, SnsActivity.class);
            intent.putExtra("snsType", Constants.REQUEST_SNS_KAKAO);
            startActivityForResult(intent, Constants.REQUEST_SNS_KAKAO);
        }
    }


    private void checkTemporaryDiary() {

        String diary = DbController.queryTemporaryDiary(this);
        if (diary != null && !diary.isEmpty()) {
            UiController.showDialog(this, R.string.dialog_load_temporary_diary, R.string.actionbar_ok, R.string.actionbar_cancel, new CustomDialogListener() {
                @Override
                public void onDialog(int type) {
                    if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                        displayUpdateStoryData(DbController.queryTemporaryDiary(StoryWriteActivity.this));
                        mTextNotice.setVisibility(View.GONE);
                    } else {
                        DbController.updateTemporaryDiary(StoryWriteActivity.this, "");
                        mTextNotice.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private void addTag(String tag) {
        String[] str = tag.split(",");
        if (str[0] != null) {
            mEditTag1.setText(str[0]);
        }
        if (str.length > 1 && str[1] != null) {
            mEditTag2.setText(str[1]);
            mTagLayout2.setVisibility(View.VISIBLE);
        } else {
            mTagLayout2.setVisibility(View.GONE);
        }
        if (str.length > 2 && str[2] != null) {
            mEditTag3.setText(str[2]);
            mTagLayout3.setVisibility(View.VISIBLE);
        } else {
            mTagLayout3.setVisibility(View.GONE);
        }
    }

    private String getTag() {
        String tag = "";
        if (!mEditTag1.getText().toString().trim().isEmpty()) {
            tag += mEditTag1.getText().toString().trim();
        }
        if (mTagLayout2.getVisibility() == View.VISIBLE && !mEditTag2.getText().toString().trim().isEmpty()) {
            if (tag.isEmpty()) {
                tag += mEditTag2.getText().toString().trim();
            } else {
                tag += "," + mEditTag2.getText().toString().trim();
            }
        }
        if (mTagLayout3.getVisibility() == View.VISIBLE && !mEditTag3.getText().toString().trim().isEmpty()) {
            if (tag.isEmpty()) {
                tag += mEditTag3.getText().toString().trim();
            } else {
                tag += "," + mEditTag3.getText().toString().trim();
            }
        }
        return tag;
    }

    private void displayUpdateStoryData(String diary) {
        try {
            JsonNode root = JsonUtil.parseTree(diary);
            WriteDiaryData detailStoryData = (WriteDiaryData) JsonUtil.jsonToObject(root.toString(), WriteDiaryData.class);
            if (detailStoryData != null) {

                if (detailStoryData.getBoardType() != null) {
                    if (detailStoryData.getBoardType().equals("D")) {
                        mCategoryIndex = 0;
                        mTextCategory.setText(mCategoryList.get(mCategoryIndex));
                        mTagLayout.setVisibility(View.VISIBLE);
                    } else {
                        mCategoryIndex = 1;
                        mTextCategory.setText(mCategoryList.get(mCategoryIndex));
                        mTagLayout.setVisibility(View.GONE);
                    }
                }

                if (detailStoryData.getBlogTag().toString().trim() != null)
                    addTag(detailStoryData.getBlogTag().toString().trim());
                for (RowsData row : detailStoryData.getRows()) {
                    if (row.Type.equals("Text"))
                        mEditContent.setText(row.Value);
                }

                if (detailStoryData.getImages() != null)
                    mImgPath = detailStoryData.getImages();

                imageSetting();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void makeModify() {
        if (mModifyData != null)
            try {
                mImageShare.setVisibility(View.GONE);

                if (mType.equals("story")) {
                    mCategoryIndex = 0;
                    mTextCategory.setText(mCategoryList.get(mCategoryIndex));
                    mTagLayout.setVisibility(View.VISIBLE);
                } else {
                    mCategoryIndex = 1;
                    mTextCategory.setText(mCategoryList.get(mCategoryIndex));
                    mTagLayout.setVisibility(View.GONE);
                }
                mLayoutCategory.setOnClickListener(null);

                JsonNode root = JsonUtil.parseTree(mModifyData);
                StoryDetailJson detailStoryData = (StoryDetailJson) JsonUtil.jsonToObject(root.toString(), StoryDetailJson.class);
                if (detailStoryData != null) {
                    if (detailStoryData.BlogTag != null)
                        addTag(detailStoryData.BlogTag);
                    for (RowJson row : detailStoryData.Rows) {
                        if (row.Type.equals("Text"))
                            mEditContent.setText(row.Value);
                        else if (row.Type.equals("Image"))
                            mImgPath.add(row.Value);
                    }
                    imageSetting();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public boolean dataCheck() {
        if (mCategoryIndex == -1) {
            UiController.showDialog(mContext, R.string.dialog_product_reg_category);
            mDialog.dismiss();
            return false;
        }

        if (mEditContent.getText().toString().trim().isEmpty()) {
            UiController.showDialog(mContext, R.string.dialog_empty_content);
            mDialog.dismiss();
            return false;
        }

		/*if(mImgPath.size() == 0) {
            UiController.showDialog(mContext, R.string.dialog_empty_contents);
			return false;
		}*/

        if (!mTextCategory.getText().toString().trim().equals("일상")) {
            if (getTag().isEmpty()) {
                UiController.showDialog(mContext, R.string.dialog_empty_tag);
                mDialog.dismiss();
                return false;
            }
        }
        return true;
    }

    private String makeWriteStroyData() {

        if (mEditContent.getText().toString().trim().isEmpty()) {
            return null;
        }

        WriteDiaryData data = new WriteDiaryData();

        if (mTextCategory.getText().toString().trim().equals("일상")) {
            data.setBoardType("C");
        } else if (mTextCategory.getText().toString().trim().equals("밥상수다")) {
            data.setBoardType("D");
        }

        ArrayList<RowsData> rows = new ArrayList<RowsData>();

        RowsData row = new RowsData();
        row.Type = "Text";
        row.Value = mEditContent.getText().toString();
        rows.add(row);

        for (int i = 0; i < mImgPath.size(); i++) {
            row = new RowsData();
            row.Type = "Image";
            row.Value = String.valueOf(i);
            rows.add(row);
        }
        data.setImages(mImgPath);
        data.setRows(rows);

        String tag[] = getTag().split(",");
        String kakaoTag = "";

        for (String str : tag) {
            if (!str.trim().isEmpty()) {
                if (!kakaoTag.isEmpty()) {
                    kakaoTag += ",";
                }
                kakaoTag += str;
            }
        }

        data.setBlogTag(kakaoTag);

        try {
            String ret = JsonUtil.objectToJson(data);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveStory() {

        mDialog = new ProgressDialog(StoryWriteActivity.this);
        mDialog.setMessage("글 등록 중입니다.");
        mDialog.show();
        mDialog.setCancelable(false);

        if (!dataCheck()) {
            return;
        }

        WriteDiaryData data = new WriteDiaryData();

        if (mTextCategory.getText().toString().trim().equals("일상")) {
            data.setBoardType("C");
        } else {
            data.setBoardType("D");
        }

        ArrayList<RowsData> rows = new ArrayList<RowsData>();

        RowsData row = new RowsData();
        row.Type = "Text";
        row.Value = mEditContent.getText().toString();
        rows.add(row);

        for (int i = 0; i < mImgPath.size(); i++) {
            row = new RowsData();
            row.Type = "Image";
            row.Value = String.valueOf(i);
            rows.add(row);
        }
        data.setImages(mImgPath);
        data.setRows(rows);

        String tag[] = getTag().split(",");
        String kakaoTag = "";

        for (String str : tag) {
            if (!str.trim().isEmpty()) {
                if (!kakaoTag.isEmpty()) {
                    kakaoTag += ",";
                }
                kakaoTag += str;
            }
        }
        data.setBlogTag(kakaoTag);

        CenterController.writeUserStroye(data, new CenterResponseListener(StoryWriteActivity.this) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            if (mModifyData == null && DbController.querySnsKakaoUse(mContext)) {
                                kakaoSand();
                            }
                            mDialog.dismiss();
                            DbController.updateTemporaryDiary(StoryWriteActivity.this, "");
                            Toast toast = Toast.makeText(mContext, R.string.toast_success_write_diary, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            finish();
                            break;
                        default:
                            mDialog.dismiss();
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mDialog.dismiss();
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                mDialog.dismiss();
                UiController.showDialog(mContext, R.string.dialog_unknown_error);
            }

            @Override
            public void onCancel() {
                super.onCancel();
                mDialog.dismiss();
                UiController.showDialog(mContext, "이미지 파일을 찾을 수가 없습니다.\n 이미지를 삭제 후 다시 추가해주세요");
            }
        });
    }

    void kakaoSand() {

        SnsPostItem snsPostItem = new SnsPostItem();

        snsPostItem.setIsFaceBook(false);
        snsPostItem.setIsKakao(true);
        snsPostItem.setIsNaver(false);

        HashMap<String, String> kakaoData = new HashMap<String, String>();

        String tag[] = getTag().split(",");

        String kakaoText = mEditContent.getText().toString();
        String kakoaTag = "";

        for (String str : tag) {
            if (!str.trim().isEmpty()) {
                kakoaTag += "#" + str + " ";
            }
        }

        kakaoText += "\n\n" + kakoaTag;

        kakaoData.put(SnsPostItem.TEXT, kakaoText);
        snsPostItem.setKakaoData(kakaoData);

        snsPostItem.setImages(mImgPath);
        Intent intent = new Intent(mContext, SnsPostingService.class);
        intent.putExtra(SnsPostingService.DATA, snsPostItem);
        startService(intent);
    }

    public void editStroy() {

        mDialog = new ProgressDialog(StoryWriteActivity.this);
        mDialog.setMessage("글 등록 중입니다.");
        mDialog.show();
        mDialog.setCancelable(false);

        if (!dataCheck()) {
            return;
        }

        WriteDiaryData data = new WriteDiaryData();

        if (mTextCategory.getText().toString().trim().equals("일상")) {
            data.setBoardType("C");
        } else {
            data.setBoardType("D");
        }

        if (mCategoryIndex == -1) {
            UiController.showDialog(mContext, R.string.dialog_product_reg_category);
            return;
        }

        ArrayList<RowsData> rows = new ArrayList<RowsData>();

        RowsData row = new RowsData();
        row.Type = "Text";
        row.Value = mEditContent.getText().toString();
        rows.add(row);

        for (int i = 0; i < mImgPath.size(); i++) {
            row = new RowsData();
            row.Type = "Image";
            row.Value = String.valueOf(i);
            rows.add(row);
        }
        data.setImages(mImgPath);

        data.setRows(rows);
        data.setBlogTag(getTag());

        CenterController.editUserStory(mIndex, data, new CenterResponseListener(this) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            Toast toast = Toast.makeText(mContext, R.string.toast_success_edit_diary, Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
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
                UiController.showDialog(mContext, R.string.dialog_unknown_error);
            }
        });
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.write_diary_title);

        Button leftBtn = (Button) findViewById(R.id.actionbar_left_button);
        leftBtn.setVisibility(View.VISIBLE);
        leftBtn.setText(R.string.actionbar_cancel);
        leftBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onSaveBtnClicked();
                finish();
            }
        });

        Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
        rightBtn.setText(R.string.actionbar_upload);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (mIndex == null)
                    saveStory();
                else
                    editStroy();
            }
        });
    }

    public void pictureBtnClicked(View view) {

        registerForContextMenu(view);
        openContextMenu(view);
        unregisterForContextMenu(view);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.context_menu_camera_title);
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        tempPath.clear();

        switch (item.getItemId()) {
            case R.id.btn_camera_capture:
                tempPath.add(ImageUtil.takePictureFromCamera(this, Constants.REQUEST_TAKE_CAPTURE));
                return true;
            case R.id.btn_camera_gallery:
                galleryChoice(DiaryWriteDragListFragment.MAX_PICTURE_NUMBER);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void galleryChoice(int maxCount) {
        int count = mImgPath.size();
        ImageUtil.takeSelectFromGallery(mContext, maxCount, count, "", "", Constants.REQUEST_GALLERY);
    }

    private void runImageRotateActivity(int takeType, ArrayList<String> imgPath) {
        Intent intent = new Intent(this, ImageRotateActivity.class);
        intent.putExtra("takeType", takeType);
        intent.putStringArrayListExtra("imagePath", imgPath);
        startActivityForResult(intent, Constants.REQUEST_ROTATE_PICTURE);
    }

    private void imageSetting() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.CommonSmallMargin), 0);
        mImageLayout.removeAllViews();
        int i = 0;
        for (String str : mImgPath) {
            View view = mInflater.inflate(R.layout.item_image, null);
            if (str.startsWith("http")) {
                imageLoader.displayImage(str, (ImageView) view.findViewById(R.id.Image), mImageOption);
            } else {
                imageLoader.displayImage("file://" + str, (ImageView) view.findViewById(R.id.Image), mImageOption);
            }
            view.setTag(i);
            view.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    ArrayList<String> imgArr = new ArrayList<String>();
                    for (String str : mImgPath) {
                        if (str.startsWith("http")) {
                            imgArr.add(str);
                        } else {
                            imgArr.add("file://" + str);
                        }
                    }
                    int i = (int) v.getTag();
                    Intent intent = new Intent(StoryWriteActivity.this, ImageViewActivity.class);
                    intent.putExtra("pos", i);
                    intent.putStringArrayListExtra("imageArrary", imgArr);
                    startActivity(intent);
                }
            });

            ImageView delete = (ImageView) view.findViewById(R.id.Delete);
            delete.setTag(i);
            delete.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    int i = (int) v.getTag();
                    mImgPath.remove(i);
                    imageSetting();
                }
            });
            mImageLayout.addView(view, layoutParams);
            i++;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_TAKE_CAPTURE) {
                runImageRotateActivity(Constants.REQUEST_TAKE_CAPTURE, tempPath);
                return;
            } else if (requestCode == Constants.REQUEST_GALLERY) {
                tempPath = data.getStringArrayListExtra("imagePath");
                runImageRotateActivity(Constants.REQUEST_GALLERY, tempPath);
                return;
            } else if (requestCode == Constants.REQUEST_ROTATE_PICTURE) {
                mImgPath.addAll(data.getStringArrayListExtra("imagePath"));
                tempPath.clear();
                imageSetting();
                return;
            } else if (requestCode == Constants.REQUEST_SNS_KAKAO) {
                DbController.updateKakaoFlag(StoryWriteActivity.this, true);
                DbController.updateSnsKakaoUse(StoryWriteActivity.this, true);
                snsSettiong(false);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void categoryBtnClicked() {
        DialogFragment fragment = DialogFragment.newInstance(
                0,
                mCategoryIndex,
                mCategoryList.toArray(new String[mCategoryList.size()]),
                "");
        fragment.setOnCloseCategoryDialogListener(new DialogFragment.OnCloseCategoryDialogListener() {
            @Override
            public void onDialogSelected(int subMenuType, int position) {
                if (mCategoryIndex != position) {
                    mCategoryIndex = position;
                    mTextCategory.setText(mCategoryList.get(position));
                    mTextNotice.setVisibility(View.GONE);
                    if (mCategoryList.get(position).equals("밥상수다")) {
                        mTagLayout.setVisibility(View.VISIBLE);
						/*CenterController.getUserStoryTag(new CenterResponseListener(StoryWriteActivity.this) {
							@Override
							public void onSuccess(int Code, String content) {
								try {
									switch (Code) {
										case 0000:
											JsonNode root = JsonUtil.parseTree(content);
											if (root.findPath("List").isArray()) {
												Iterator<JsonNode> it = root.findPath("List").iterator();
												while (it.hasNext()) {
													mTodayTagData = (TodayTagJson) JsonUtil.jsonToObject(it.next().toString(), TodayTagJson.class);
													break;
												}
												if (mTodayTagData != null) {
													UiController.showDialog(StoryWriteActivity.this, "오늘의 생생밥상 키워드\n" + mTodayTagData.Keyword + " 입니다.\n오늘의 키워드를 작성하시겠습니까?", R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
														@Override
														public void onDialog(int type) {
															if (UiDialog.DIALOG_POSITIVE_LISTENER == type) {
																mEditTag.setText(mTodayTagData.Keyword);
															}
														}
													});
												}
											}
											break;
									}
								} catch (Exception e) {
									UiController.showDialog(StoryWriteActivity.this, R.string.dialog_unknown_error);
								}
							}

							@Override
							public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
								super.onFailure(statusCode, headers, content, error);
							}
						});*/
                    } else {
                        mTagLayout.setVisibility(View.GONE);
                        mTextNotice.setVisibility(View.GONE);
                    }
                }
            }
        });

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, DialogFragment.TAG);
    }
}

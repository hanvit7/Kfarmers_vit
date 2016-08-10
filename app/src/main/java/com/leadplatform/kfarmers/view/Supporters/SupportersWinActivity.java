package com.leadplatform.kfarmers.view.Supporters;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import org.apache.http.Header;

public class SupportersWinActivity extends BaseFragmentActivity {
    public static final String TAG = "SupportersWinActivity";

    public static final String EVENT_IDX = "EVENT_IDX";

    private DisplayImageOptions mImageOption;

    private String mEventIdx;

    private TextView mContentText;
    private ImageView mImageView;

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_detail);
        ((TextView) findViewById(R.id.actionbar_title_text_view)).setText("당첨자 발표");
        initActionBarHomeBtn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            mEventIdx = getIntent().getStringExtra(EVENT_IDX);
        }
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.item_event_win);

        mContentText = (TextView) findViewById(R.id.Content);
        mImageView = (ImageView) findViewById(R.id.Image);

        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                .build();

        getData();
    }

    private void getData() {
        UiController.showProgressDialog(this);
        SnipeApiController.getProductList(String.valueOf(10), "", "0", "", "", false,null,null, new SnipeResponseListener(SupportersWinActivity.this) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    if (Code == 200) {
                        JsonNode root = JsonUtil.parseTree(content).get("item");

                        mContentText.setText("건조한 겨울철 피부에 제대로된 보습 효과를 위한 필수 아이템!\n" +
                                "\n" +
                                "바로 No.1 진동클렌저 비자퓨어죠!  \n" +
                                "\n" +
                                " \n" +
                                "\n" +
                                "필립스 뷰티 이벤트에 참여해주신 모든 분들께 감사드립니다.\n" +
                                "\n" +

                                "박지혜 ( http://facebook.com/100003493725018\t)\n" +
                                "\n" +
                                "신해솔 ( http://facebook.com/100002656386425\t)\n" +
                                "\n" +
                                "우주희 ( http://facebook.com/100007438740884\t)\n" +
                                "\n" +
                                "윤혜빈 ( http://facebook.com/100007508701110\t)\n" +
                                "\n" +
                                "임미선 ( http://facebook.com/100002663982659\t)\n" +
                                "\n" +
                                "정소영 ( http://facebook.com/100006589053570\t)\n" +
                                "\n" +
                                "최다은 ( http://facebook.com/100004354444508\t)\n");

                        imageLoader.displayImage("http://www.intercrewkorea.co.kr/news/img_data/이마트-15주년-이벤트-당첨.jpg", mImageView);
                    } else {
                        UiController.showDialog(SupportersWinActivity.this, R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(SupportersWinActivity.this, R.string.dialog_unknown_error);
                }
                UiController.hideProgressDialog(SupportersWinActivity.this);
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                UiController.hideProgressDialog(SupportersWinActivity.this);
            }
        });
    }
}


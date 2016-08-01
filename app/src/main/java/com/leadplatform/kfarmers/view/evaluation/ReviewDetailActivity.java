package com.leadplatform.kfarmers.view.evaluation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.snipe.OrderItemJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.json.snipe.ReviewListJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.DynamicRatioImageView;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;
import org.json.JSONArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ReviewDetailActivity extends BaseFragmentActivity {
    public static final String TAG = "ReviewDetailActivity";

    private DisplayImageOptions mImageOption,mImageOtionsProfile;

    ReviewListJson mReviewListJson;

    View contentView;

    ImageView profile;
    TextView name;
    TextView ratingText;
    TextView date;
    RatingBar ratingBar;
    LinearLayout mContentView;

    LinearLayout mFooterView;
    TextView mReplyText;

    String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
    String dateFormat2 = "yyyy-M-d";
    SimpleDateFormat format, format2;

    private UserDb mUserDb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_review_view);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_REVIEW_DETAIL);

        if(getIntent().hasExtra("data")) {
            mReviewListJson = (ReviewListJson) getIntent().getSerializableExtra("data");
        }

        format = new SimpleDateFormat(dateFormat1);
        format2 = new SimpleDateFormat(dateFormat2);

        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();
        mImageOtionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(this, 50) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();

        contentView = findViewById(R.id.contentView);

        profile = (ImageView) findViewById(R.id.Profile);
        name = (TextView) findViewById(R.id.Name);
        ratingText = (TextView) findViewById(R.id.RatingText);
        date = (TextView) findViewById(R.id.Date);
        ratingBar = (RatingBar) findViewById(R.id.Ratingbar);

        mContentView = (LinearLayout) findViewById(R.id.Body);
        mFooterView = (LinearLayout) findViewById(R.id.Footer);

        mReplyText = (TextView) findViewById(R.id.ReplyText);

        mUserDb = DbController.queryCurrentUser(this);

        if(mUserDb != null && mUserDb.getUserID().equals(mReviewListJson.id)) {
            registerForContextMenu(contentView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getReview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterForContextMenu(contentView);
    }

    private void getReview() {

        JSONArray jsonArrayCode = new JSONArray();
        JSONArray jsonArrayOrder = new JSONArray();

        jsonArrayOrder.put(mReviewListJson.unique_individual);
        jsonArrayCode.put(mReviewListJson.code);

        SnipeApiController.getReviewItems(mReviewListJson.id, jsonArrayCode.toString(), jsonArrayOrder.toString(), new SnipeResponseListener(this) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            List<ReviewListJson> arrayList = (List<ReviewListJson>) JsonUtil.jsonToArrayObject(root, ReviewListJson.class);
                            String count = mReviewListJson.comments_count;
                            ProductJson productJson = mReviewListJson.prodcut;
                            mReviewListJson = arrayList.get(0);
                            mReviewListJson.comments_count = count;
                            mReviewListJson.prodcut = productJson;
                            makeView();
                            break;
                        default:
                            UiController.showDialog(ReviewDetailActivity.this, R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(ReviewDetailActivity.this, R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });

        /*SnipeApiController.getReviewItem(mUserDb.getUserID(), mItemList.get(mNowIndex).code, "", mOrderNo, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                UiController.hideProgressDialogFragment(getView());
                try {
                    switch (Code) {
                        case 200:
                            if (!content.isEmpty()) {
                                ReviewListJson reviewListJson = (ReviewListJson) JsonUtil.jsonToObject(content, ReviewListJson.class);

                                for (int i = 0; i < reviewListJson.file.size(); i++) {
                                    reviewListJson.file.get(i).path = Constants.KFARMERS_SNIPE_IMG + reviewListJson.file.get(i).path;
                                }
                                mReviewMap.put(reviewListJson.code, reviewListJson);

                                mItemText.setText(mItemList.get(mNowIndex).name);
                                mRatingBar.setRating(Float.parseFloat(reviewListJson.rating) / 2f);
                                mEditDes.setText(reviewListJson.comment);
                                mSendBtn.setText("수정하기");
                            } else {
                                ReviewListJson reviewListJson = new ReviewListJson();
                                reviewListJson.file = new ArrayList<>();
                                mReviewMap.put(mItemList.get(mNowIndex).code, reviewListJson);

                                mItemText.setText(mItemList.get(mNowIndex).name);
                                mRatingBar.setRating(5f);
                                mEditDes.setText("");
                                mSendBtn.setText("등록하기");
                            }
                            imageSetting();
                            if(mNowType == EvaluationActivity.type.evaluation) evaluationItem();
                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                UiController.hideProgressDialogFragment(getView());
            }
        });*/
    }

    private void makeView() {

        if(mReviewListJson.member_profile_image != null && !mReviewListJson.member_profile_image.isEmpty()) {
            imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + mReviewListJson.member_profile_image, profile, mImageOtionsProfile);
        } else {
            profile.setImageResource(R.drawable.icon_empty_profile);
        }

        if (mReviewListJson.member_name == null || mReviewListJson.member_name.isEmpty()) {
            name.setText("소비자 회원");
        } else {
            name.setText(mReviewListJson.member_name);
        }

        ratingText.setText(mReviewListJson.rating + "점");

        try {
            date.setText(format2.format((format.parse(mReviewListJson.datetime).getTime())));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        float rating = Float.parseFloat(mReviewListJson.rating) / 2f;
        ratingBar.setRating(rating);

        mContentView.removeAllViews();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TextView view = (TextView) inflater.inflate(R.layout.item_detail_text, null);
        view.setText(mReviewListJson.comment);
        mContentView.addView(view);

        if (mReviewListJson.file != null) {
            for (ReviewListJson.File file : mReviewListJson.file) {
                DynamicRatioImageView imageView = (DynamicRatioImageView) inflater.inflate(R.layout.item_detail_image, null);
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + file.path, imageView, mImageOption);
                mContentView.addView(imageView);
            }
        }

        mReplyText.setText("(" + mReviewListJson.comments_count + ") 댓글달기");

        mFooterView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                runReplyActivity(ReplyActivity.REPLY_TYPE_REVIEW, "구매후기 댓글", mReviewListJson.idx);
            }
        });

        if(mReviewListJson.prodcut != null) {

            LinearLayout root = (LinearLayout) findViewById(R.id.root);

            ImageView img = (ImageView) findViewById(R.id.image);
            ImageView img_profile = (ImageView) findViewById(R.id.image_profile);
            img_profile.setVisibility(View.GONE);

            TextView des = (TextView) findViewById(R.id.des);
            TextView price = (TextView) findViewById(R.id.price);
            TextView dcPrice = (TextView) findViewById(R.id.dcPrice);
            TextView summary = (TextView) findViewById(R.id.summary);
            TextView dDay = (TextView) findViewById(R.id.textDday);

            imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + mReviewListJson.prodcut.image1, img, mImageOption);

            if(mReviewListJson.prodcut.summary != null && !mReviewListJson.prodcut.summary.isEmpty())
            {
                summary.setVisibility(View.VISIBLE);
                summary.setText(mReviewListJson.prodcut.summary);
            }
            else
            {
                summary.setVisibility(View.GONE);
            }

            //des.setText(item.name);
            des.setText(mReviewListJson.prodcut.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

            int itemPrice = (int)Double.parseDouble(mReviewListJson.prodcut.price);
            int itemBuyPrice = (int) Double.parseDouble(mReviewListJson.prodcut.buyprice);

            if(itemPrice > itemBuyPrice)
            {
                price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
                dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(mReviewListJson.prodcut.buyprice)) + getResources().getString(R.string.korea_won));
                price.setVisibility(View.VISIBLE);

                price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            else
            {
                price.setText("");
                dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(mReviewListJson.prodcut.buyprice)) + getResources().getString(R.string.korea_won));
                price.setVisibility(View.GONE);
                price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

            }

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_REVIEW_DETAIL, "Click_Product", mReviewListJson.prodcut.name);
                    Intent intent = new Intent(ReviewDetailActivity.this, ProductActivity.class);
                    intent.putExtra("productIndex", mReviewListJson.prodcut.idx);
                    startActivity(intent);
                }
            });

            if(mReviewListJson.prodcut.duration.startsWith("D") && !mReviewListJson.prodcut.duration.equals("D-day")) {
                dDay.setText(mReviewListJson.prodcut.duration);
                dDay.setVisibility(View.VISIBLE);
            } else {
                dDay.setVisibility(View.GONE);
            }
        } else {

        }
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView mActionBarTitle = (TextView) findViewById(R.id.title);
        mActionBarTitle .setText(getString(R.string.review_realtime_title));
        initActionBarHomeBtn();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if(mUserDb != null && mUserDb.getUserID().equals(mReviewListJson.id)) {
            getMenuInflater().inflate(R.menu.menu_review_item, menu);
            menu.setHeaderTitle(R.string.context_menu_title);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(mUserDb != null && mUserDb.getUserID().equals(mReviewListJson.id)) {
            switch (item.getItemId()) {
                case R.id.btn_copy:
                    return true;
                case R.id.btn_edit:
                    try {
                        ArrayList<OrderItemJson> orderItemJsons = new ArrayList<OrderItemJson>();
                        OrderItemJson orderItemJson = new OrderItemJson();
                        orderItemJson.item_idx = mReviewListJson.item_idx;
                        orderItemJson.name = mReviewListJson.prodcut.name;
                        orderItemJson.image1 = mReviewListJson.prodcut.image1;
                        orderItemJson.code = mReviewListJson.code;
                        orderItemJson.unique = mReviewListJson.unique_individual;
                        orderItemJsons.add(orderItemJson);

                        Intent intent = new Intent(this, EvaluationActivity.class);
                        intent.putExtra("type",EvaluationActivity.type.review);
                        intent.putExtra("data",orderItemJsons);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                case R.id.btn_delete:
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }
}

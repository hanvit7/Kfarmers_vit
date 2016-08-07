package com.leadplatform.kfarmers.view.evaluation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ReviewListJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.market.ProductActivity;
import com.leadplatform.kfarmers.view.reply.ReplyActivity;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ReviewListFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "ReviewListFragment";

    private final String mLimit = "20";
    private  int mPage = 1;
    private boolean mMoreFlag = false;

    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOption,mImageOtionsProfile,mImageOtionsProfile2;

    private ListAdapter mListAdapter;

    private LayoutInflater mInflater;

    private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;

    private ImageButton mTopDiaryBtn;

    private String userId = "";

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (mMoreFlag == true) {
                mMoreFlag = false;
                getReviewData(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getReviewData(true);
        }
    };

    public static ReviewListFragment newInstance(String id) {
        final ReviewListFragment f = new ReviewListFragment();
        final Bundle args = new Bundle();
        args.putString("id", id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("id")) {
            userId = getArguments().getString("id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);

        mTopDiaryBtn = (ImageButton) v.findViewById(R.id.back_to_top_button);
        mTopDiaryBtn.setVisibility(View.GONE);

        mTopDiaryBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                getListView().smoothScrollBy(0, 0);
                getListView().setSelectionFromTop(0, 0);
            }
        });

        mInflater = inflater;

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();
        mImageOtionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();

        mImageOtionsProfile2 = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 30) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).build();

        setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);

        AbsListView.OnScrollListener onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if(firstVisibleItem>1) {
                    mTopDiaryBtn.setVisibility(View.VISIBLE);
                } else {
                    mTopDiaryBtn.setVisibility(View.GONE);
                }
            }
        };
        registerExtraOnScrollListener(onScrollListener);

        if (mListAdapter  == null) {
            mListAdapter = new ListAdapter(getActivity(), R.layout.item_review_list);

            mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mListAdapter);
            mSwingBottomInAnimationAdapter.setAbsListView(getListView());

            assert mSwingBottomInAnimationAdapter.getViewAnimator() != null;
            mSwingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
            setListAdapter(mSwingBottomInAnimationAdapter);
            getReviewData(true);
        }
    }

    private void getReviewData(boolean isClear) {

        if (isClear) {
            mPage = 1;
            mMoreFlag = false;
            mListAdapter.clear();
        }
        SnipeApiController.getReviewList("", "",userId, String.valueOf(mPage), mLimit, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.path("item").size() > 0) {
                                ArrayList<ReviewListJson> evaluationList = (ArrayList<ReviewListJson>) JsonUtil.jsonToArrayObject(root.path("item"), ReviewListJson.class);
                                mListAdapter.addAll(evaluationList);

                                if (evaluationList.size() == Integer.parseInt(mLimit)) {
                                    mPage++;
                                    mMoreFlag = true;
                                } else {
                                    mMoreFlag = false;
                                }

                                break;
                            }
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
                onRefreshComplete();
                onLoadMoreComplete();
            }
        });
    }

    public class ListAdapter extends ArrayAdapter {
        private int itemLayoutResourceId;
        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d";
        SimpleDateFormat format, format2;


        public ListAdapter(Context context, int resource) {
            super(context, resource);
            itemLayoutResourceId = resource;
            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(itemLayoutResourceId, null);
            }
            ReviewListJson item = (ReviewListJson) getItem(position);

            if (item != null) {

                View view = ViewHolder.get(convertView,R.id.RootView);
                ImageView profile = ViewHolder.get(convertView, R.id.Profile);
                TextView name = ViewHolder.get(convertView, R.id.Name);
                TextView ratingText = ViewHolder.get(convertView, R.id.RatingText);
                TextView description = ViewHolder.get(convertView, R.id.Description);
                TextView date = ViewHolder.get(convertView, R.id.Date);
                //TextView typeText = ViewHolder.get(convertView, R.id.TypeText);
                View replyLatyout = ViewHolder.get(convertView, R.id.reply_layout);
                View replyOnLatyout = ViewHolder.get(convertView, R.id.reply_on);
                View replyOffLatyout = ViewHolder.get(convertView, R.id.reply_off);

                ImageView reply_profile = ViewHolder.get(convertView, R.id.reply_profile);
                TextView reply_name = ViewHolder.get(convertView, R.id.reply_name);
                TextView reply_text = ViewHolder.get(convertView, R.id.reply_text);
                TextView reply_count = ViewHolder.get(convertView, R.id.reply_count);

                RatingBar ratingBar = ViewHolder.get(convertView, R.id.Ratingbar);

                //LinearLayout imageLayout = ViewHolder.get(convertView, R.id.ImageLayout);

                ImageView imageView = ViewHolder.get(convertView, R.id.imageview);

                view.setTag(position);
                view.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int pos = (int) v.getTag();
                        Intent intent = new Intent(getActivity(),ReviewDetailActivity.class);
                        intent.putExtra("data", (Serializable) getItem(pos));
                        startActivity(intent);
                    }
                });

                if(item.file != null && item.file.size()>0) {
                    mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.file.get(0).path, imageView, mImageOption);
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }

                if(item.member_profile_image != null && !item.member_profile_image.isEmpty()) {
                    mImageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.member_profile_image, profile, mImageOtionsProfile);
                } else {
                    profile.setImageResource(R.drawable.icon_empty_profile);
                }

                /*LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.CommonSmallMargin), 0);

                imageLayout.removeAllViews();

                int i = 0;
                for (ReviewListJson.File file : item.file) {
                    View view = mInflater.inflate(R.layout.item_review_big_image, null);
                    ImageView imageView = (ImageView) view.findViewById(R.id.Image);
                    mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + file.path, imageView, mImageOption);
                    imageLayout.addView(view, layoutParams);

                    imageView.setTag(R.integer.tag_pos,position);
                    imageView.setTag(R.integer.tag_id,i);

                    imageView.setOnClickListener(new ViewOnClickListener() {
                        @Override
                        public void viewOnClick(View v) {
                            int pos = (int) v.getTag(R.integer.tag_pos);
                            int i = (int) v.getTag(R.integer.tag_id);

                            ArrayList<String> imgArrayList = new ArrayList<String>();
                            ReviewListJson item = (ReviewListJson) getItem(pos);

                            for (ReviewListJson.File file : item.file) {
                                imgArrayList.add(Constants.KFARMERS_SNIPE_IMG + file.path);
                            }
                            Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                            intent.putExtra("pos", i);
                            intent.putStringArrayListExtra("imageArrary", imgArrayList);
                            startActivity(intent);
                        }
                    });
                    i++;
                }*/

                if (item.member_name == null || item.member_name.isEmpty()) {
                    name.setText("소비자 회원");
                } else {
                    name.setText(item.member_name);
                }

                ratingText.setText(item.rating + "점");
                description.setText(item.comment);

                /*description.setTag(position);
                description.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int i = (int) v.getTag();
                        if (((TextView) v).getLineCount() >= 2) {
                            ReviewListJson reviewListJson = (ReviewListJson) getItem(i);
                            UiDialog.showDialog(getActivity(), "리뷰", reviewListJson.comment);
                        }
                    }
                });*/

                try {
                    date.setText(format2.format((format.parse(item.datetime).getTime())));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                /*if (item.division.equals("E")) {
                    typeText.setText(item.division_text);
                    typeText.setVisibility(View.VISIBLE);
                } else {
                    typeText.setText("");
                    typeText.setVisibility(View.GONE);
                }*/

                float rating = Float.parseFloat(item.rating) / 2f;
                ratingBar.setRating(rating);

                if(item.prodcut != null) {

                    LinearLayout root = ViewHolder.get(convertView, R.id.root);

                    ImageView img = ViewHolder.get(convertView, R.id.image);
                    ImageView img_profile = ViewHolder.get(convertView, R.id.image_profile);

                    TextView des = ViewHolder.get(convertView, R.id.des);
                    TextView price = ViewHolder.get(convertView, R.id.price);
                    TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
                    TextView summary = ViewHolder.get(convertView, R.id.summary);
                    TextView dDay = ViewHolder.get(convertView, R.id.textDday);
                    img_profile.setVisibility(View.GONE);

                    mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.prodcut.image1, img, mImageOption);

                    if(item.prodcut.summary != null && !item.prodcut.summary.isEmpty())
                    {
                        summary.setVisibility(View.VISIBLE);
                        summary.setText(item.prodcut.summary);
                    }
                    else
                    {
                        summary.setVisibility(View.GONE);
                    }

                    //des.setText(item.name);
                    des.setText(item.prodcut.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                    int itemPrice = (int)Double.parseDouble(item.prodcut.price);
                    int itemBuyPrice = (int) Double.parseDouble(item.prodcut.buyprice);

                    if(itemPrice > itemBuyPrice)
                    {
                        price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
                        dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.prodcut.buyprice)) + getResources().getString(R.string.korea_won));
                        price.setVisibility(View.VISIBLE);

                        price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    else
                    {
                        price.setText("");
                        dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.prodcut.buyprice)) + getResources().getString(R.string.korea_won));
                        price.setVisibility(View.GONE);
                        price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                    }

                    root.setTag(position);
                    root.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            int pos = (int) v.getTag();
                            ReviewListJson item = (ReviewListJson) getItem(pos);

                            KfarmersAnalytics.onClick(KfarmersAnalytics.S_REVIEW_LIST, "Click_Product", item.prodcut.name);

                            Intent intent = new Intent(getActivity(), ProductActivity.class);
                            intent.putExtra("productIndex", item.prodcut.idx);
                            startActivity(intent);
                        }
                    });

                    if(item.prodcut.duration.startsWith("D") && !item.prodcut.duration.equals("D-day")) {
                        dDay.setText(item.prodcut.duration);
                        dDay.setVisibility(View.VISIBLE);
                    } else {
                        dDay.setVisibility(View.GONE);
                    }
                }

                if(item.comments != null) {
                    replyOnLatyout.setVisibility(View.VISIBLE);
                    replyOffLatyout.setVisibility(View.VISIBLE);

                    if(item.comments.profile_image != null && !item.comments.profile_image.isEmpty()) {
                        mImageLoader.displayImage(item.comments.profile_image, reply_profile, mImageOtionsProfile2);
                    } else {
                        reply_profile.setImageResource(R.drawable.icon_empty_profile);
                    }

                    reply_name.setText(item.comments.target_name);

                    String des[] = item.comments.description.split("==]Name]==");
                    if(des.length>1) {
                        reply_text.setText(des[1]);
                    }
                    else {
                        reply_text.setText(item.comments.description);
                    }
                    reply_count.setText("("+item.comments_count+")");
                } else {
                    replyOnLatyout.setVisibility(View.GONE);
                    replyOffLatyout.setVisibility(View.VISIBLE);
                    reply_count.setText("");
                }

                replyLatyout.setTag(item.idx);
                replyLatyout.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        String idx = (String) v.getTag();
                        ((ReviewActivity)getActivity()).runReplyActivity(
                                ReplyActivity.REPLY_TYPE_REVIEW,
                                "구매후기 댓글",
                                idx);
                    }
                });
            }
            return convertView;
        }
    }
}
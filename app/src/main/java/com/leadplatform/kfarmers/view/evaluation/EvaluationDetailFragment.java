package com.leadplatform.kfarmers.view.evaluation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.snipe.OrderItemJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.model.json.snipe.ReviewListJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.ImageViewActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;
import org.json.JSONArray;

import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EvaluationDetailFragment extends BaseFragment {
    public static final String TAG = "EvaluationDetailFragment";

    private EvaluationActivity.type mNowType;

    private UserDb mUserDb;

    private String mPlace, mItem;

    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOption;

    private LayoutInflater mInflater;

    private LinearLayout mItemLayout, mItemView;

    private TextView mTitle, mItemText, mScore;
    private RatingBar mRatingBar;
    private EditText mEditDes;
    private Button mSendBtn;
    private ImageView mImageBtn;

    private LinearLayout mImageLayout;

    private int mNowIndex = 0;

    private ArrayList<ProductJson> mItemList = new ArrayList<>();
    private HashMap<String, ReviewListJson> mReviewMap = new HashMap<>();

    public static EvaluationDetailFragment newInstance(String place, String item) {
        final EvaluationDetailFragment f = new EvaluationDetailFragment();
        final Bundle args = new Bundle();
        args.putString("place", place);
        args.putString("item", item);
        args.putSerializable("type", EvaluationActivity.type.evaluation);
        f.setArguments(args);
        return f;
    }

    public static EvaluationDetailFragment newInstance(ArrayList<OrderItemJson> orderItemJson) {
        final EvaluationDetailFragment f = new EvaluationDetailFragment();
        final Bundle args = new Bundle();
        args.putSerializable("data",orderItemJson);
        args.putSerializable("type", EvaluationActivity.type.review);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mNowType = (EvaluationActivity.type) getArguments().getSerializable("type");

            if(mNowType == EvaluationActivity.type.evaluation) {
                mItem = getArguments().getString("item");
                mPlace = getArguments().getString("place");
            } else {
               ArrayList<OrderItemJson> orderItemJson = (ArrayList<OrderItemJson>) getArguments().getSerializable("data");

                for(OrderItemJson itemJson : orderItemJson) {

                    ProductJson productJson = new ProductJson();

                    productJson.idx = itemJson.item_idx;
                    productJson.name = itemJson.name;
                    productJson.image1 = itemJson.image1;
                    productJson.code = itemJson.code;
                    productJson.unique = itemJson.unique;
                    mItemList.add(productJson);

                    ReviewListJson reviewListJson = new ReviewListJson();
                    reviewListJson.file = new ArrayList<ReviewListJson.File>();
                    mReviewMap.put(itemJson.code, reviewListJson);
                }
            }
        }
        mUserDb = DbController.queryCurrentUser(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_evaluation, container, false);
        mInflater = inflater;

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();

        mItemLayout = (LinearLayout) v.findViewById(R.id.ItemLayout);

        mItemView = (LinearLayout) v.findViewById(R.id.ItemView);

        mItemView.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                hideKeyboard();
            }
        });


        mTitle = (TextView) v.findViewById(R.id.Title);
        mItemText = (TextView) v.findViewById(R.id.ItemText);
        mScore = (TextView) v.findViewById(R.id.Score);
        mRatingBar = (RatingBar) v.findViewById(R.id.Ratingbar);
        mEditDes = (EditText) v.findViewById(R.id.EditDes);
        mSendBtn = (Button) v.findViewById(R.id.SendBtn);

        mImageLayout = (LinearLayout) v.findViewById(R.id.ImageLayout);
        mImageBtn = (ImageView) v.findViewById(R.id.ImageBtn);

        mImageBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                getActivity().registerForContextMenu(v);
                getActivity().openContextMenu(v);
                getActivity().unregisterForContextMenu(v);
            }
        });

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                mScore.setText(((int) (rating * 2)) + "점");
            }
        });

        mSendBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (mItemList.size() > 0) sendReview();
            }
        });

        // 스크롤뷰 안 스크롤 작동 코드
        mEditDes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.EditDes) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mNowType == EvaluationActivity.type.evaluation) {
            getEvaluationData();
        } else {
            mTitle.setVisibility(View.GONE);
            itemMakeView();
        }
    }

    private void getEvaluationData() {

        UiController.showProgressDialogFragment(getView());
        SnipeApiController.getEvaluationItem(mPlace, mItem, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                UiController.hideProgressDialogFragment(getView());
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            List<ProductJson> arrayList = new ArrayList<ProductJson>();

                            if (root.path("item").size() > 0) {
                                String title = root.get("item").get("subject").textValue();
                                String date = root.get("item").get("datetime").textValue();

                                String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
                                String dateFormat2 = "yyyy-M-d a h시 ";
                                SimpleDateFormat format = new SimpleDateFormat(dateFormat1);
                                SimpleDateFormat format2 = new SimpleDateFormat(dateFormat2);
                                String str = format2.format((format.parse(date).getTime()));
                                str += title;
                                mTitle.setText(str);
                            }

                            if (root.path("product").size() > 0) {
                                arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("product"), ProductJson.class);
                                mItemList = (ArrayList<ProductJson>) arrayList;

                                for (ProductJson productJson : mItemList) {
                                    ReviewListJson reviewListJson = new ReviewListJson();
                                    reviewListJson.file = new ArrayList<ReviewListJson.File>();
                                    mReviewMap.put(productJson.code, reviewListJson);
                                }
                                itemMakeView();
                                mSendBtn.setEnabled(true);
                            } else {
                                mSendBtn.setEnabled(false);
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
                UiController.hideProgressDialogFragment(getView());
            }
        });
    }

    public void selectItem()
    {
        for (int i = 0; i < mItemLayout.getChildCount(); i++) {
            LinearLayout subView = (LinearLayout) mItemLayout.getChildAt(i);
            for (int j = 0; j < subView.getChildCount(); j++) {
                RelativeLayout relativeLayout = (RelativeLayout) subView.getChildAt(j);

                if (relativeLayout.getTag() == null || mNowIndex != (int) relativeLayout.getTag()) {
                    (relativeLayout.getChildAt(1)).setVisibility(View.GONE);
                } else {
                    (relativeLayout.getChildAt(1)).setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void evaluationItem()
    {
        for (int i = 0; i < mItemLayout.getChildCount(); i++) {
            LinearLayout subView = (LinearLayout) mItemLayout.getChildAt(i);
            for (int j = 0; j < subView.getChildCount(); j++) {
                RelativeLayout relativeLayout = (RelativeLayout) subView.getChildAt(j);

                if (relativeLayout.getTag() != null)
                {
                    int pos = (int) relativeLayout.getTag();
                    ReviewListJson reviewListJson = mReviewMap.get(mItemList.get(pos).code);

                    if(reviewListJson.code != null && !reviewListJson.code.isEmpty()) {
                        (relativeLayout.getChildAt(2)).setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        (relativeLayout.getChildAt(2)).setVisibility(View.GONE);
                    }
                }
                else
                {
                    break;
                }
            }
        }
    }

    public void itemMakeView() {
        int count = (mItemList.size() / 4) + ((mItemList.size() % 4) > 0 ? 1 : 0);
        for (int i = 0; i < count; i++) {
            View view = mInflater.inflate(R.layout.item_evaluation_image, null);

            RelativeLayout image1 = (RelativeLayout) view.findViewById(R.id.item1);
            RelativeLayout image2 = (RelativeLayout) view.findViewById(R.id.item2);
            RelativeLayout image3 = (RelativeLayout) view.findViewById(R.id.item3);
            RelativeLayout image4 = (RelativeLayout) view.findViewById(R.id.item4);

            ImageView imageView1 = (ImageView) view.findViewById(R.id.image1);
            ImageView imageView2 = (ImageView) view.findViewById(R.id.image2);
            ImageView imageView3 = (ImageView) view.findViewById(R.id.image3);
            ImageView imageView4 = (ImageView) view.findViewById(R.id.image4);

            ImageView imageViewOk1 = (ImageView) view.findViewById(R.id.imageOk1);
            ImageView imageViewOk2 = (ImageView) view.findViewById(R.id.imageOk2);
            ImageView imageViewOk3 = (ImageView) view.findViewById(R.id.imageOk3);
            ImageView imageViewOk4 = (ImageView) view.findViewById(R.id.imageOk4);

            if(mNowType == EvaluationActivity.type.evaluation) {
                imageViewOk1.setImageResource(R.drawable.evaluation_ok);
                imageViewOk2.setImageResource(R.drawable.evaluation_ok);
                imageViewOk3.setImageResource(R.drawable.evaluation_ok);
                imageViewOk4.setImageResource(R.drawable.evaluation_ok);
            } else {
                imageViewOk1.setImageResource(R.drawable.icon_review);
                imageViewOk2.setImageResource(R.drawable.icon_review);
                imageViewOk3.setImageResource(R.drawable.icon_review);
                imageViewOk4.setImageResource(R.drawable.icon_review);
            }

            ViewOnClickListener viewOnClickListener = new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {

                    if (mNowIndex == (int) v.getTag()) {
                        return;
                    }
                    mNowIndex = (int) v.getTag();

                    selectItem();
                    getReview();
                }
            };

            int pos = (i * 4) + 0;
            if (mItemList.size() > pos) {
                ProductJson recommendListJson1 = mItemList.get(pos);
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + recommendListJson1.image1, imageView1, mImageOption);
                image1.setTag(pos);
                image1.setOnClickListener(viewOnClickListener);
            }

            pos = (i * 4) + 1;
            if (mItemList.size() > pos) {
                ProductJson recommendListJson2 = mItemList.get(pos);
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + recommendListJson2.image1, imageView2, mImageOption);
                image2.setTag(pos);
                image2.setOnClickListener(viewOnClickListener);
            }

            pos = (i * 4) + 2;
            if (mItemList.size() > pos) {
                ProductJson recommendListJson3 = mItemList.get(pos);
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + recommendListJson3.image1, imageView3, mImageOption);
                image3.setTag(pos);
                image3.setOnClickListener(viewOnClickListener);
            }

            pos = (i * 4) + 3;
            if (mItemList.size() > pos) {
                ProductJson recommendListJson4 = mItemList.get(pos);
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + recommendListJson4.image1, imageView4, mImageOption);
                image4.setTag(pos);
                image4.setOnClickListener(viewOnClickListener);
            }

            mItemLayout.addView(view);
        }
        ((RelativeLayout) ((LinearLayout) mItemLayout.getChildAt(0)).getChildAt(0)).getChildAt(1).setVisibility(View.VISIBLE);
        getReview();
    }

    public void addImages(ArrayList<String> path) {
        for (String str : path) {
            ReviewListJson.File file = new ReviewListJson.File();
            file.path = str;
            mReviewMap.get(mItemList.get(mNowIndex).code).file.add(file);
        }
        imageSetting();
    }

    private void imageSetting() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, getResources().getDimensionPixelSize(R.dimen.CommonSmallMargin), 0);
        mImageLayout.removeAllViews();
        int i = 0;
        for (ReviewListJson.File file : mReviewMap.get(mItemList.get(mNowIndex).code).file) {
            View view = mInflater.inflate(R.layout.item_image, null);

            if (file.path.startsWith("http")) {
                mImageLoader.displayImage(file.path, (ImageView) view.findViewById(R.id.Image), mImageOption);
                view.setTag(file.path);
            } else {
                mImageLoader.displayImage("file://" + file.path, (ImageView) view.findViewById(R.id.Image), mImageOption);
                view.setTag("file://" + file.path);
            }

            view.setTag(i);
            view.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    ArrayList<String> imgArr = new ArrayList<String>();
                    for (ReviewListJson.File file : mReviewMap.get(mItemList.get(mNowIndex).code).file) {
                        if (file.path.startsWith("http")) {
                            imgArr.add(file.path);
                        } else {
                            imgArr.add("file://" + file.path);
                        }
                    }
                    int i = (int) v.getTag();
                    Intent intent = new Intent(getActivity(), ImageViewActivity.class);
                    intent.putExtra("pos", i);
                    intent.putStringArrayListExtra("imageArrary",imgArr);
                    startActivity(intent);
                }
            });

            ImageView delete = (ImageView) view.findViewById(R.id.Delete);
            delete.setTag(i);
            delete.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    int i = (int) v.getTag();
                    if ((mReviewMap.get(mItemList.get(mNowIndex).code).file.get(i).idx != null && !(mReviewMap.get(mItemList.get(mNowIndex).code).file.get(i).idx.isEmpty()))) {
                        mReviewMap.get(mItemList.get(mNowIndex).code).file_delete.add((mReviewMap.get(mItemList.get(mNowIndex).code).file.get(i).idx));
                    }
                    mReviewMap.get(mItemList.get(mNowIndex).code).file.remove(i);
                    imageSetting();
                }
            });
            mImageLayout.addView(view, layoutParams);
            i++;
        }
    }

    private void getReview() {

        UiController.showProgressDialogFragment(getView());

        JSONArray jsonArrayCode = new JSONArray();
        JSONArray jsonArrayOrder = new JSONArray();
        for(ProductJson productJson : mItemList) {
            if(productJson.unique != null && !productJson.unique.isEmpty()) jsonArrayOrder.put(productJson.unique);
            jsonArrayCode.put(productJson.code);
        }

        SnipeApiController.getReviewItems(mUserDb.getUserID(), jsonArrayCode.toString(), jsonArrayOrder.length() > 0 ? jsonArrayOrder.toString() : "", new SnipeResponseListener(getActivity()) {

            @Override
            public void onSuccess(int Code, String content, String error) {
                UiController.hideProgressDialogFragment(getView());
                try {
                    switch (Code) {
                        case 200:
                            if (!content.isEmpty()) {

                                JsonNode root = JsonUtil.parseTree(content);
                                List<ReviewListJson> arrayList = (List<ReviewListJson>) JsonUtil.jsonToArrayObject(root, ReviewListJson.class);

                                boolean isNowView = false;

                                for(ReviewListJson reviewListJson : arrayList) {
                                    for (int i = 0; i < reviewListJson.file.size(); i++) {
                                        reviewListJson.file.get(i).path = Constants.KFARMERS_SNIPE_IMG + reviewListJson.file.get(i).path;
                                    }
                                    mReviewMap.put(reviewListJson.code, reviewListJson);

                                    if(mItemList.get(mNowIndex).code.equals(reviewListJson.code)) {
                                        mItemText.setText(mItemList.get(mNowIndex).name);
                                        mRatingBar.setRating(Float.parseFloat(reviewListJson.rating) / 2f);
                                        mEditDes.setText(reviewListJson.comment);
                                        mSendBtn.setText("수정하기");
                                        isNowView = true;
                                    }
                                }
                                if(!isNowView) {
                                    mItemText.setText(mItemList.get(mNowIndex).name);
                                    mRatingBar.setRating(5f);
                                    mEditDes.setText("");
                                    mSendBtn.setText("등록하기");
                                }

                            } else {
                                /*ReviewListJson reviewListJson = new ReviewListJson();
                                reviewListJson.file = new ArrayList<>();
                                mReviewMap.put(mItemList.get(mNowIndex).code, reviewListJson);*/

                                mItemText.setText(mItemList.get(mNowIndex).name);
                                mRatingBar.setRating(5f);
                                mEditDes.setText("");
                                mSendBtn.setText("등록하기");
                            }
                            imageSetting();
                            evaluationItem();
                            //if (mNowType == EvaluationActivity.type.evaluation) evaluationItem();
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

    private void sendReview() {
        if (mRatingBar.getRating() <= 0f) {
            UiController.showDialog(getActivity(), "별점을 입력해 주세요");
            return;
        }

        if (mEditDes.getText().toString().trim().isEmpty()) {
            UiController.showDialog(getActivity(), "후기를 남겨주세요.");
            return;
        }

        if (mEditDes.getText().toString().trim().length()<= 20) {
            UiController.showDialog(getActivity(), "20글자 이상 후기를 남겨주세요.");
            return;
        }

        UiController.showProgressDialogFragment(getView());

        new AsyncTask<Void, Void, Void>() {
            HashMap<String, String> newImage;
            HashMap<String, String> deleteImage;

            @Override
            protected Void doInBackground(Void... params) {
                newImage = new HashMap<>();
                deleteImage = new HashMap<>();
                int i = 0;
                for (ReviewListJson.File file : mReviewMap.get(mItemList.get(mNowIndex).code).file) {
                    if (file.idx == null || file.idx.isEmpty()) {
                        try {
                            RandomAccessFile f = new RandomAccessFile(file.path, "r");
                            byte[] b = new byte[(int) f.length()];
                            f.read(b);
                            newImage.put(String.valueOf(i), Base64.encodeToString(b, 0));
                            f.close();
                            i++;
                        } catch (Exception e) {}
                    }
                }

                i = 0;
                for (String s : mReviewMap.get(mItemList.get(mNowIndex).code).file_delete) {
                    deleteImage.put(String.valueOf(i), s);
                    i++;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                String star = String.valueOf((int) (mRatingBar.getRating() * 2));
                String comment = mEditDes.getText().toString().trim().replaceAll("\\n{2,}", "\n\n");

                String addType = "E";
                if(mNowType == EvaluationActivity.type.review) addType = "D";

                SnipeApiController.addReview(mReviewMap.get(mItemList.get(mNowIndex).code).idx,mItemList.get(mNowIndex).unique,addType, mUserDb.getUserID(), mItemList.get(mNowIndex).code, comment, star, newImage, deleteImage, new SnipeResponseListener(getActivity()) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {
                        UiController.hideProgressDialogFragment(getView());
                        try {
                            switch (Code) {
                                case 200:
                                    if(mNowType == EvaluationActivity.type.evaluation) {
                                        UiController.showDialog(getSherlockActivity(), "해당 상품을 검증했습니다. 다음 검증 상품으로 이동합니다.");
                                    } else {
                                        //UiController.showDialog(getSherlockActivity(), "후기를 남겼습니다.");
                                        UiController.showDialog(getActivity(), R.string.dialog_review_add_ok, new CustomDialogListener() {
                                            @Override
                                            public void onDialog(int type) {
                                                getActivity().finish();
                                            }
                                        });
                                    }

                                    ReviewListJson reviewListJson = new ReviewListJson();
                                    reviewListJson.code = mItemList.get(mNowIndex).code;
                                    mReviewMap.put(reviewListJson.code, reviewListJson);

                                    if (mNowIndex < mItemList.size() - 1) {
                                        mNowIndex++;
                                    } else {
                                        mNowIndex = 0;
                                    }
                                    selectItem();
                                    getReview();
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
                });
            }
        }.execute(null, null, null);
    }

    public int getImageSize() {
        return mReviewMap.get(mItemList.get(mNowIndex).code).file.size();
    }

    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(mEditDes.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

    }

}


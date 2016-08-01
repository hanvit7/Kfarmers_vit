package com.leadplatform.kfarmers.view.evaluation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.market.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

public class EvaluationResultFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "EvaluationResultFragment";

    private ImageLoader mImageLoader;
    private DisplayImageOptions mImageOption;

    private LayoutInflater mInflater;

    private ArrayList<ProductJson> mItemList = new ArrayList<>();

    private String mPlace, mItem;

    private ListAdapter mListAdapter;


    public static EvaluationResultFragment newInstance(String place, String item) {
        final EvaluationResultFragment f = new EvaluationResultFragment();
        final Bundle args = new Bundle();
        args.putString("place", place);
        args.putString("item", item);
        args.putSerializable("type", EvaluationActivity.type.evaluation);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mItem = getArguments().getString("item");
            mPlace = getArguments().getString("place");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_base_pull_list, container, false);
        mInflater = inflater;

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mImageOption = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).build();

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        if (mListAdapter  == null) {
            mListAdapter = new ListAdapter(getActivity(), R.layout.item_home_evaluation_list);
            getListView().setAdapter(mListAdapter);
            getEvaluationData();
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
                            }

                            if (root.path("product").size() > 0) {
                                arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("product"), ProductJson.class);
                                mItemList = (ArrayList<ProductJson>) arrayList;
                                mListAdapter.addAll(mItemList);
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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        ProductJson item = (ProductJson) mListAdapter.getItem(position);
        Intent intent = new Intent(getActivity(), ProductActivity.class);
        intent.putExtra("productIndex", item.idx);
        startActivity(intent);
    }

    public class ListAdapter extends ArrayAdapter {
        private int itemLayoutResourceId;

        public ListAdapter(Context context, int resource) {
            super(context, resource);
            itemLayoutResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(itemLayoutResourceId, null);
            }

            ProductJson item = (ProductJson) getItem(position);

            if(item != null) {
                ImageView imageView = ViewHolder.get(convertView, R.id.image);
                TextView summary = ViewHolder.get(convertView, R.id.summary);
                TextView des = ViewHolder.get(convertView, R.id.des);
                TextView star = ViewHolder.get(convertView, R.id.star);
                TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
                TextView price = ViewHolder.get(convertView, R.id.price);

                RatingBar ratingBar = ViewHolder.get(convertView, R.id.ratingbar);

                if(!item.summary.isEmpty()) {
                    summary.setText(item.summary);
                    summary.setVisibility(View.VISIBLE);
                } else {
                    summary.setText("");
                    summary.setVisibility(View.GONE);
                }

                des.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                if(!item.rating_count.equals("0")) {
                    star.setText(item.rating + "%");
                    float rating = (Float.parseFloat(item.rating)/10)/2f;
                    ratingBar.setRating(rating);
                } else {
                    star.setVisibility(View.GONE);
                    ratingBar.setVisibility(View.GONE);
                }

                int itemPrice = (int)Double.parseDouble(item.price);
                int itemBuyPrice = (int) Double.parseDouble(item.buyprice);

                if(itemPrice > itemBuyPrice)
                {
                    price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
                    dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
                    price.setVisibility(View.VISIBLE);

                    price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else
                {
                    price.setText("");
                    dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(item.buyprice)) + getResources().getString(R.string.korea_won));
                    price.setVisibility(View.GONE);
                    price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                }
                mImageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, imageView, mImageOption);
            }
            return convertView;
        }
    }
}


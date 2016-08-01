package com.leadplatform.kfarmers.view.inquiry;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.InquiryDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.ChatJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.base.DynamicFrameLayout;
import com.leadplatform.kfarmers.view.market.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class InquiryChatFragment extends BaseRefreshMoreListFragment {
    public static final String TAG = "InquiryChatFragment";

    enum Type {
        left,
        right
    }

    ImageLoader imageLoader;
    DisplayImageOptions mOptionsProduct;
    DisplayImageOptions mOptionsProfile;

    private ProfileJson mProfile;

    private String mIdx;

    private final String mLimit = "100";
    private String mLastIdx = "";
    private boolean bMoreFlag = false;

    private EditText mCommentEdit;
    ImageView mSendBtn,mMoreBtn;
    View moreView;

    ItemAdapter mItemAdapter;

    int mCount = -100;

    private LayoutInflater mInflater;

    public static InquiryChatFragment newInstance(String idx) {
        final InquiryChatFragment f = new InquiryChatFragment();
        final Bundle args = new Bundle();
        args.putString("idx",idx);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mProfile = ((BaseFragmentActivity)getActivity()).getProfile();
        if (getArguments() != null) {
            mIdx = getArguments().getString("idx");
        }

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_INQUIRY_DETAIL, null);

        mOptionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageOnLoading(R.drawable.common_dummy).build();
        imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mOptionsProfile = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 50) / 2))
                .showImageOnLoading(R.drawable.icon_empty_profile).showImageOnFail(R.drawable.icon_empty_profile).build();

        InquiryDb inquiryDb = new InquiryDb();
        inquiryDb.idx = mIdx;
        inquiryDb.read = "T";
        DbController.updateInquiry(getActivity(), inquiryDb);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_inquiry_chat,container, false);

        mInflater = inflater;

        mCommentEdit = (EditText) v.findViewById(R.id.CommentEdit);
        mSendBtn = (ImageView) v.findViewById(R.id.SendBtn);
        mMoreBtn = (ImageView) v.findViewById(R.id.moreBtn);
        moreView = v.findViewById(R.id.moreView);

        if(mProfile.Type.equals("F")) {
            moreView.setVisibility(View.VISIBLE);
            mMoreBtn.setOnClickListener(new ViewOnClickListener() {
                @Override
                public void viewOnClick(View v) {
                    getListShop();
                }
            });
        } else {
            moreView.setVisibility(View.GONE);
        }

        mSendBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (mCommentEdit.getText().toString().trim().length() <= 0) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_comment);
                    return;
                }
                insertChat(mCommentEdit.getText().toString().trim(), "comment");
            }
        });
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);
        setTopMoreListView(getSherlockActivity(), R.layout.view_list_footer_transparent, loadMoreListener);

        if (mItemAdapter == null) {
            mItemAdapter = new ItemAdapter(getActivity(),R.layout.item_chat);
            setListAdapter(mItemAdapter);
            getListChat(true);
        }
    }

    public class ItemAdapter extends ArrayAdapter<ChatJson> {
        int resource;
        String dateFormat1 = "yyyy-MM-dd HH:mm:ss";
        String dateFormat2 = "yyyy-M-d a h:mm";

        SimpleDateFormat format,format2;

        public ItemAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            format = new SimpleDateFormat(dateFormat1);
            format2 = new SimpleDateFormat(dateFormat2);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = mInflater.inflate(resource, null);
            }

            ChatJson item = getItem(position);

            View view = ViewHolder.get(convertView, R.id.RootView);

            View leftMsgLayout = ViewHolder.get(convertView, R.id.LeftMsgLayout);
            ImageView leftProfile = ViewHolder.get(convertView, R.id.LeftProfile);
            TextView leftName = ViewHolder.get(convertView, R.id.LeftName);
            TextView leftTime = ViewHolder.get(convertView, R.id.LeftTime);
            RelativeLayout leftContent = ViewHolder.get(convertView, R.id.LeftContent);

            View rightMsgLayout = ViewHolder.get(convertView, R.id.RightMsgLayout);
            TextView rightTime = ViewHolder.get(convertView, R.id.RightTime);
            RelativeLayout rightContent = ViewHolder.get(convertView, R.id.RightContent);

            if(item != null)
            {
                if(getCount()-1 == position)
                {
                    view.setPadding(view.getPaddingLeft(),view.getPaddingTop(),view.getPaddingRight(),view.getPaddingTop());
                }
                else
                {
                    view.setPadding(view.getPaddingLeft(),view.getPaddingTop(),view.getPaddingRight(),0);
                }

                if(mProfile.ID.equals(item.member))
                {
                    leftMsgLayout.setVisibility(View.GONE);
                    rightMsgLayout.setVisibility(View.VISIBLE);

                    //TextView righttMsg = ViewHolder.get(convertView, R.id.RightMsg);
                    //righttMsg.setText(item.message);
                    msgMakeView(item,rightContent,Type.right);
                    try {
                        String str = format2.format((format.parse(item.datetime).getTime()));
                        rightTime.setText(str);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    leftMsgLayout.setVisibility(View.VISIBLE);
                    rightMsgLayout.setVisibility(View.GONE);

                    if(item.profile_image != null && !item.profile_image.isEmpty()) {
                        imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.profile_image, leftProfile, mOptionsProfile);
                    } else {
                        leftProfile.setImageResource(R.drawable.icon_empty_profile);
                    }
                    leftName.setText(item.nick);
                    leftTime.setText(item.datetime);
                    //leftMsg.setText(item.message);
                    msgMakeView(item,leftContent,Type.left);
                    try {
                        String str = format2.format((format.parse(item.datetime).getTime()));
                        leftTime.setText(str);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            return convertView;
        }

        private void msgMakeView(ChatJson item,RelativeLayout contentView, Type type) {

            TextView textView;
            DynamicFrameLayout itemView;
            View productView;

            if(type == Type.left) {
                textView = ViewHolder.get(contentView, R.id.LeftMsg);
                itemView = ViewHolder.get(contentView, R.id.LeftItem);
                productView = ViewHolder.get(contentView, R.id.LeftProduct);
            } else {
                textView = ViewHolder.get(contentView, R.id.RightMsg);
                itemView = ViewHolder.get(contentView, R.id.RightItem);
                productView = ViewHolder.get(contentView, R.id.RightProduct);
            }
            textView.setText("");

            if(item.message_type.equals("product")) {
                textView.setVisibility(View.GONE);
                itemView.setVisibility(View.VISIBLE);
                try {

                    ProductJson productJson = (ProductJson) item.jsonData;

                    ImageView img = ViewHolder.get(productView, R.id.image);
                    TextView des = ViewHolder.get(productView, R.id.des);
                    TextView price = ViewHolder.get(productView, R.id.price);
                    TextView dcPrice = ViewHolder.get(productView, R.id.dcPrice);
                    TextView summary = ViewHolder.get(productView, R.id.summary);
                    TextView dDay = ViewHolder.get(productView, R.id.textDday);
                    TextView productType = ViewHolder.get(productView, R.id.productType);
                    ImageView img_profile = ViewHolder.get(productView, R.id.image_profile);

                    img_profile.setVisibility(View.GONE);
                    imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + productJson.image1, img, mOptionsProduct);

                    if(productJson.summary != null && !productJson.summary.isEmpty())
                    {
                        summary.setVisibility(View.VISIBLE);
                        summary.setText(productJson.summary);
                    }
                    else
                    {
                        summary.setVisibility(View.GONE);
                    }
                    productType.setVisibility(View.VISIBLE);
                    if(productJson.verification.equals(ProductActivity.TYPE3)) {
                        productType.setText("일반");
                        productType.setBackgroundResource(R.color.minicart_cart_enabled);
                        productType.setVisibility(View.VISIBLE);
                    } else {
                        productType.setText("검증");
                        productType.setBackgroundResource(R.color.pink);
                        productType.setVisibility(View.GONE);
                    }

                    //des.setText(item.name);
                    des.setText(productJson.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                    int itemPrice = (int)Double.parseDouble(productJson.price);
                    int itemBuyPrice = (int) Double.parseDouble(productJson.buyprice);

                    if(itemPrice > itemBuyPrice)
                    {
                        price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice)+getResources().getString(R.string.korea_won));
                        dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(productJson.buyprice)) + getResources().getString(R.string.korea_won));
                        price.setVisibility(View.VISIBLE);

                        price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    else
                    {
                        price.setText("");
                        dcPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int) Double.parseDouble(productJson.buyprice)) + getResources().getString(R.string.korea_won));
                        price.setVisibility(View.GONE);
                        price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

                    }

                    productView.setTag(R.integer.tag_id,productJson.idx);
                    productView.setTag(R.integer.tag_pos,productJson.name);

                    productView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            KfarmersAnalytics.onClick(KfarmersAnalytics.S_INQUIRY_DETAIL, "Click_Product", (String) v.getTag(R.integer.tag_pos));
                            Intent intent = new Intent(getActivity(),ProductActivity.class);
                            intent.putExtra("productIndex", (String) v.getTag(R.integer.tag_id));
                            startActivity(intent);
                        }
                    });

                    if(productJson.duration.startsWith("D") && !productJson.duration.equals("D-day")) {
                        dDay.setText(productJson.duration);
                        dDay.setVisibility(View.VISIBLE);
                    } else {
                        dDay.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                textView.setText(item.message);
                textView.setVisibility(View.VISIBLE);
                itemView.setVisibility(View.GONE);
            }
        }
    }

    private OnRefreshListener refreshListener = new OnRefreshListener() {
        @Override
        public void onRefreshStarted(View view) {
            getListChat(true);
        }
    };

    private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
        @Override
        public void onLoadMore() {
            if (bMoreFlag == true) {
                bMoreFlag = false;
                getListChat(false);
            } else {
                onLoadMoreComplete();
            }
        }
    };

    private void insertChat(String msg, String type) {
        SnipeApiController.insertChat(mIdx, mProfile.ID, msg, type, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            mCommentEdit.setText("");
                            mLastIdx = "";
                            bMoreFlag = false;
                            getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                            getListChat(true);
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
                super.onFailure(statusCode, headers, content, error);
                onLoadMoreComplete();
            }
        });
    }

    public void getListChat(Boolean isClear)
    {
        if (isClear) {
            mCount = -100;
            mLastIdx = "";
            bMoreFlag = false;
            mItemAdapter.clear();
            getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        }

        SnipeApiController.getChatItem(mIdx, mProfile.ID, mLimit, mLastIdx, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            if (JsonUtil.parseTree(content).get("item").isArray()) {
                                JsonNode root = JsonUtil.parseTree(content);
                                List<ChatJson<ProductJson>> arrayList = new ArrayList<>();

                                if (root.path("item").size() > 0) {
                                    arrayList = (List<ChatJson<ProductJson>>) JsonUtil.jsonToArrayObject(root.path("item"), ChatJson.class);
                                    for (int i = 0; i < arrayList.size(); i++) {
                                        if (arrayList.get(i).info != null && arrayList.get(i).info.size() > 0) {
                                            arrayList.get(i).jsonData = (ProductJson) JsonUtil.jsonToObject(arrayList.get(i).info.toString(), ProductJson.class);
                                        }
                                        mItemAdapter.insert(arrayList.get(i), 0);
                                    }
                                    mCount = arrayList.size() - 1;
                                }

                                mLastIdx = mItemAdapter.getItem(0).idx;

                                if (arrayList.size() == Integer.valueOf(mLimit))
                                    bMoreFlag = true;
                                else
                                    bMoreFlag = false;

                                getListView().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCount >= 0) {
                                            getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_DISABLED);
                                            getListView().setSelection(mCount);
                                        }
                                        onRefreshComplete();
                                        onLoadMoreComplete();
                                    }
                                });
                            } else {
                                onRefreshComplete();
                                onLoadMoreComplete();
                            }
                            //onRefreshComplete();
                            //onLoadMoreComplete();

                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            onRefreshComplete();
                            onLoadMoreComplete();
                    }
                    UiController.hideProgressDialogFragment(getView());
                } catch (Exception e) {
                    UiController.hideProgressDialogFragment(getView());
                    onRefreshComplete();
                    onLoadMoreComplete();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
                UiController.hideProgressDialogFragment(getView());
                super.onFailure(statusCode, headers, content, error);
            }
        });

        /*SnipeApiController.getChatList(mUser.getUserID(), mLimit, mLastIdx, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    if(Code == 200)
                    {
                        JsonNode root = JsonUtil.parseTree(content).get("item");
                        List<ProductJson> arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root, ProductJson.class);
                        mItemAdapter.addAll(arrayList);

                        if (arrayList.size() == Integer.parseInt(mLimit))
                            bMoreFlag = true;
                        else
                            bMoreFlag = false;

                        mLastIdx = mItemAdapter.getItem(mItemAdapter.getCount()).idx;

                    }
                    else
                    {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });*/
    }

    private void getListShop() {
        SnipeApiController.getProductList("9999", "", "", mProfile.ID, "", false, null, null, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                onRefreshComplete();
                onLoadMoreComplete();
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            List<ProductJson> arrayList = new ArrayList<ProductJson>();

                            if (root.path("item").size() > 0) {
                                arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
                                productView(arrayList);
                            } else {
                                UiController.showDialog(getSherlockActivity(), "승인된 상품이 없습니다.");
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
                onRefreshComplete();
                onLoadMoreComplete();
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }

    private void productView(List<ProductJson> productJsons) {

        ListView listView = new ListView(getActivity());
        listView.setAdapter(new MainAllListAdapter(getActivity(), R.layout.item_product_list, (ArrayList<ProductJson>) productJsons));

        //UiDialog.showDialog(getActivity(), "상품 선택", listView, R.string.dialog_ok, null);
        AlertDialog mdialog = null;
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
        alert.setTitle("상품 선택");
        alert.setView(listView);
        alert.setNegativeButton("닫기", null);
        if (!getActivity().isFinishing()) {
            mdialog = alert.show();
        }

        final AlertDialog finalMdialog = mdialog;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String idx = ((ProductJson) parent.getItemAtPosition(position)).code;
                UiController.showDialog(getActivity(), "해당 상품을 메세지로 전송할까요?", R.string.dialog_ok, R.string.dialog_close, new CustomDialogListener() {
                    @Override
                    public void onDialog(int type) {
                        if (type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                            insertChat(idx, "product");
                            finalMdialog.dismiss();
                        }
                    }
                });
            }
        });


    }

    private class MainAllListAdapter extends ArrayAdapter<ProductJson> {

        public MainAllListAdapter(Context context, int itemLayoutResourceId, ArrayList<ProductJson> items) {
            super(context, itemLayoutResourceId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_product_list, null);
            }

            ImageView img = ViewHolder.get(convertView, R.id.image);
            ImageView img_profile = ViewHolder.get(convertView, R.id.image_profile);
            TextView des = ViewHolder.get(convertView, R.id.des);
            TextView price = ViewHolder.get(convertView, R.id.price);
            TextView dcPrice = ViewHolder.get(convertView, R.id.dcPrice);
            TextView summary = ViewHolder.get(convertView, R.id.summary);
            TextView dDay = ViewHolder.get(convertView, R.id.textDday);
            TextView productType = ViewHolder.get(convertView, R.id.productType);

            img_profile.setVisibility(View.GONE);

            final ProductJson item = (ProductJson) getItem(position);
            if (item != null)
            {
                /*if(item.profile_image != null && !item.profile_image.isEmpty()) {
                    imageLoader.displayImage(Constants.KFARMERS_SNIPE_PRIFILE_IMG + item.profile_image, img_profile, mOptionsProfile);
                }
                else {
                    img_profile.setImageResource(0);
                }*/
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, img, mOptionsProduct);

                if(item.summary != null && !item.summary.isEmpty())
                {
                    summary.setVisibility(View.VISIBLE);
                    summary.setText(item.summary);
                }
                else
                {
                    summary.setVisibility(View.GONE);
                }

                productType.setVisibility(View.VISIBLE);
                if(item.verification.equals(ProductActivity.TYPE3)) {
                    productType.setText("일반");
                    productType.setBackgroundResource(R.color.minicart_cart_enabled);
                    productType.setVisibility(View.VISIBLE);
                } else {
                    productType.setText("검증");
                    productType.setBackgroundResource(R.color.pink);
                    productType.setVisibility(View.GONE);
                }

                //des.setText(item.name);
                des.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

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

                if(item.duration != null && item.duration.startsWith("D") && !item.duration.equals("D-day")) {
                    dDay.setText(item.duration);
                    dDay.setVisibility(View.VISIBLE);
                } else {
                    dDay.setVisibility(View.GONE);
                }
            }
            return convertView;
        }
    }

    private void scrollListViewToBottom() {
        getListView().post(new Runnable() {
            @Override
            public void run() {
                getListView().setSelection(mItemAdapter.getCount() - 1);
            }
        });
    }
}


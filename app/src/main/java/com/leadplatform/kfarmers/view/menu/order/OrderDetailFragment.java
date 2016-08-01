package com.leadplatform.kfarmers.view.menu.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.OrderDetailJson;
import com.leadplatform.kfarmers.model.json.snipe.OrderItemJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseListFragment;
import com.leadplatform.kfarmers.view.evaluation.EvaluationActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;

public class OrderDetailFragment extends BaseListFragment {
    public static final String TAG = "OrderDetailFragment";

    private ItemAdapter mItemAdapter;

    private OrderDetailJson mDetailData = new OrderDetailJson();

    private View mFooterView;
    private View mHeaderView;

    private String mOrderNo = "";

    private int mTempConfirmPos = 0;
    private String mTempsubOrderNo = "";

    UserDb mUser;

    public static OrderDetailFragment newInstance(String orderNo) {
        final OrderDetailFragment f = new OrderDetailFragment();
        final Bundle args = new Bundle();
        args.putString("orderNo",orderNo);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mOrderNo = getArguments().getString("orderNo");
        }

        mUser = DbController.queryCurrentUser(getActivity());
        if (mUser == null)
        {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_base_list,container, false);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_ORDER_DETAIL, null);

        initHeaderView(inflater);
        initFooterView(inflater);

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
        if (mItemAdapter == null) {

            getListView().addHeaderView(mHeaderView, null,false);
            getListView().addFooterView(mFooterView, null,false);


            mItemAdapter = new ItemAdapter(getActivity(),R.layout.item_order_detail);
            setListAdapter(mItemAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mFooterView.setVisibility(View.GONE);
        getListOrder(true);
    }

    public void initHeaderView(LayoutInflater inflater)
    {
        mHeaderView = inflater.inflate(R.layout.item_order_detail_top,null, false);
    }

    public void initFooterView(LayoutInflater inflater)
    {
        mFooterView = inflater.inflate(R.layout.item_order_detail_info,null, false);
    }

    public void setData()
    {
        int bankPoint = (int)Double.parseDouble(mDetailData.mInfo.point);
        int bankPirce = (int)Double.parseDouble(mDetailData.mPayment.bank_price);
        int refundPrice = (int)Double.parseDouble(mDetailData.mRefund.price);
        int refundPoint = (int)Double.parseDouble(mDetailData.mRefund.point);
        int productPrice = (int)Double.parseDouble(mDetailData.mPayment.price);

        int addPoint = 0;

        int totalDeliverPrice = 0;

        for(OrderItemJson itemJson : mDetailData.mItemArrayList)
        {
            totalDeliverPrice += (int)Double.parseDouble(itemJson.delivery_price);

            for(OrderItemJson json : itemJson.option) {
                if(json.status.startsWith("구매확정")) {
                    addPoint += Integer.parseInt(json.provide_point) * Integer.parseInt(json.cnt);
                }
            }
        }

        //int totalPrice = productPrice + totalDeliverPrice;

        String bankType = "";
        if(mDetailData.mPayment.payment_type.equals("bank"))
        {
            bankType = "(무통장)";
        }else if(mDetailData.mPayment.payment_type.equals("account"))
        {
            bankType = "(계좌이체)";
        }
        else
        {
            bankType = "(카드)";
        }

        ((TextView)mFooterView.findViewById(R.id.DeliveryName)).setText(mDetailData.mReceiver.receive_name);
        ((TextView)mFooterView.findViewById(R.id.DeliveryPhone)).setText(mDetailData.mReceiver.receive_hp);
        ((TextView)mFooterView.findViewById(R.id.DeliveryAddress)).setText(mDetailData.mReceiver.receive_addr);
        ((TextView)mFooterView.findViewById(R.id.DeliveryRequest)).setText(mDetailData.mItemArrayList.get(0).option.get(0).delivery_comment);

        //CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit() + " 원"
        ((TextView)mFooterView.findViewById(R.id.PayProductPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(productPrice) + " 원");
        ((TextView)mFooterView.findViewById(R.id.PayDeliveryPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(totalDeliverPrice) + " 원");
        ((TextView)mFooterView.findViewById(R.id.PayPointPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(bankPoint) + " P");
        ((TextView)mFooterView.findViewById(R.id.PayTotalPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(bankPirce) + " 원");
        ((TextView)mFooterView.findViewById(R.id.PayType)).setText(bankType);

        ((TextView)mFooterView.findViewById(R.id.AddPoint)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(addPoint) + " P");


        if(mDetailData.mPayment.payment_type.equals("bank") && bankPirce > 0 && productPrice<bankPirce)
        {
            mHeaderView.findViewById(R.id.BankView).setVisibility(View.VISIBLE);
            ((TextView)mHeaderView.findViewById(R.id.BankInfo)).setText(mDetailData.mPayment.bank_name+" "+mDetailData.mPayment.bank_account);
            ((TextView)mHeaderView.findViewById(R.id.BankDepositor)).setText(mDetailData.mPayment.bank_depositor);
            ((TextView)mHeaderView.findViewById(R.id.BankPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(bankPirce + totalDeliverPrice) + " 원");
            ((TextView)mHeaderView.findViewById(R.id.BankSendName)).setText(mDetailData.mPayment.deposit_name);

            mFooterView.findViewById(R.id.PaymentView).setVisibility(View.GONE);
            mFooterView.findViewById(R.id.CanCelView).setVisibility(View.GONE);
        }
        else
        {
            mHeaderView.findViewById(R.id.BankView).setVisibility(View.GONE);
            mFooterView.findViewById(R.id.PaymentView).setVisibility(View.VISIBLE);

            if(refundPrice > 0 || refundPoint > 0)
            {
                mFooterView.findViewById(R.id.CanCelView).setVisibility(View.VISIBLE);
                ((TextView)mFooterView.findViewById(R.id.CancelPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(refundPrice) + " 원");
                ((TextView)mFooterView.findViewById(R.id.CancelPoint)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(refundPoint) + " P");
                ((TextView)mFooterView.findViewById(R.id.CancelTotalPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(refundPrice+refundPoint) + " 원");
            }
            else
            {
                mFooterView.findViewById(R.id.CanCelView).setVisibility(View.GONE);
            }
        }

        ((TextView)mHeaderView.findViewById(R.id.Date)).setText(mDetailData.mItemArrayList.get(0).option.get(0).datetime.substring(0,10));
        ((TextView)mHeaderView.findViewById(R.id.OrderNum)).setText(mDetailData.mItemArrayList.get(0).unique);

        mHeaderView.setVisibility(View.VISIBLE);
        mFooterView.setVisibility(View.VISIBLE);
    }

    public class ItemAdapter extends ArrayAdapter<OrderItemJson> {
        ImageLoader imageLoader;
        DisplayImageOptions optionsProduct;
        LayoutInflater inflater;
        int resource;

        public ItemAdapter(Context context, int resource) {
            super(context, resource);
            this.resource = resource;
            this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).showImageOnFail(R.drawable.common_dummy).showImageForEmptyUri(R.drawable.common_dummy)
                    .build();
            imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }

            RelativeLayout itemBox = ViewHolder.get(convertView, R.id.ItemBox);
            ImageView img = ViewHolder.get(convertView, R.id.ProductImg);
            TextView name = ViewHolder.get(convertView, R.id.ProductName);
            TextView review = ViewHolder.get(convertView, R.id.reviewText);

            LinearLayout optionView = ViewHolder.get(convertView, R.id.OptionView);
            optionView.removeAllViews();

            OrderItemJson item = mDetailData.mItemArrayList.get(position);
            if (item != null) {

                if(item.reivews.equals("T")) {
                    review.setText("후기 수정");
                } else {
                    review.setText("후기 작성");
                }

                review.setTag(position);
                review.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int pos = (int) v.getTag();

                        ArrayList<OrderItemJson> orderItemJson = new ArrayList<OrderItemJson>();
                        orderItemJson.add(mDetailData.mItemArrayList.get(pos));

                        Intent intent = new Intent(getActivity(), EvaluationActivity.class);
                        intent.putExtra("type",EvaluationActivity.type.review);
                        intent.putExtra("data",orderItemJson);
                        startActivity(intent);
                    }
                });

                itemBox.setTag(position);
                itemBox.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int pos = (Integer) v.getTag();
                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        intent.putExtra("productIndex", mDetailData.mItemArrayList.get(pos).item_idx);
                        startActivity(intent);
                    }
                });

                img.setImageDrawable(null);
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, img, optionsProduct);
                name.setText(item.name.replace(" ", "\u00A0")); // 문자단위 줄바꿈

                int sPos = 0;

                boolean isBuyConfirm = false;

                for (OrderItemJson itemJson : item.option)
                {
                    View subView = inflater.inflate(R.layout.item_order_detail_cell, null);

                    TextView opName = ViewHolder.get(subView, R.id.Name);
                    TextView opCount = ViewHolder.get(subView, R.id.Count);
                    TextView opPrice = ViewHolder.get(subView, R.id.Price);
                    TextView opStatus = ViewHolder.get(subView, R.id.ItemStatus);
                    Button opSearch = ViewHolder.get(subView, R.id.ItemSearch);
                    Button opConfirm = ViewHolder.get(subView, R.id.ItemConfirm);

                    View bottomLine = ViewHolder.get(subView, R.id.BottomLine);

                    opName.setText(itemJson.option_name.replace(" ", "\u00A0")); // 문자단위 줄바꿈
                    opCount.setText(itemJson.cnt);

                    if(itemJson.status.equals("구매확정(시)")) {
                        opStatus.setText("구매확정");
                    } else {
                        opStatus.setText(itemJson.status);
                    }

                    if(itemJson.delivery_use.equals("T"))
                    {
                        opSearch.setVisibility(View.VISIBLE);
                        opSearch.setTag(itemJson.delivery_url);
                        opSearch.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {
                                try{
                                    Uri uri = Uri.parse((String) v.getTag());
                                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_ORDER_DETAIL, "Click_Item-Deliver", null);
                                }catch (Exception e){}
                            }
                        });
                    }
                    else
                    {
                        opSearch.setTag("");
                        opSearch.setOnClickListener(null);
                        opSearch.setVisibility(View.GONE);
                    }

                    if(itemJson.status.equals("배송중") || itemJson.status.equals("배송완료")) {
                        opConfirm.setVisibility(View.VISIBLE);
                        opConfirm.setTag(R.integer.tag_pos,position);
                        opConfirm.setTag(R.integer.tag_id,itemJson.unique_individual);
                        opConfirm.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {

                                mTempConfirmPos = (int) v.getTag(R.integer.tag_pos);
                                mTempsubOrderNo = (String) v.getTag(R.integer.tag_id);

                                UiDialog.showDialog(getActivity(), "구매확정","구매확정을 하겠습니까?\n확정후 반품,교환,환불이 불가능 합니다.", R.string.dialog_ok, R.string.dialog_close, new CustomDialogListener() {
                                    @Override
                                    public void onDialog(int type) {
                                        if(type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                            SnipeApiController.getOrderConfirm(mUser.getUserID(), mTempsubOrderNo, new SnipeResponseListener(getActivity()) {
                                                @Override
                                                public void onSuccess(int Code, String content, String error) {
                                                    try {
                                                        switch (Code) {
                                                            case 200:
                                                                if(mDetailData.mItemArrayList.get(mTempConfirmPos).reivews.equals("F")) {
                                                                    UiDialog.showDialog(getActivity(), "구매확정","구매확정 하였습니다.\n후기 작성시 추가 포인트를 드립니다.", R.string.dialog_review, R.string.dialog_close, new CustomDialogListener() {
                                                                        @Override
                                                                        public void onDialog(int type) {
                                                                            if(type == UiDialog.DIALOG_POSITIVE_LISTENER) {
                                                                                ArrayList<OrderItemJson> orderItemJson = new ArrayList<OrderItemJson>();
                                                                                orderItemJson.add(mDetailData.mItemArrayList.get(mTempConfirmPos));
                                                                                Intent intent = new Intent(getActivity(), EvaluationActivity.class);
                                                                                intent.putExtra("type",EvaluationActivity.type.review);
                                                                                intent.putExtra("data",orderItemJson);
                                                                                startActivity(intent);
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    UiDialog.showDialog(getActivity(), "구매확정","구매확정을 하였습니다.");
                                                                }
                                                                getListOrder(true);
                                                                break;
                                                            default:
                                                                UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                                                                break;
                                                        }
                                                    }catch (Exception e) {
                                                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        opConfirm.setVisibility(View.GONE);
                    }

                    int item_price = ((int)Double.parseDouble(itemJson.price));
                    int item_total_price = item_price * Integer.valueOf(itemJson.cnt);

                    opPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(item_total_price)) + " 원");

                    if(sPos >= item.option.size()-1) {
                        bottomLine.setVisibility(View.GONE);
                    }
                    optionView.addView(subView);
                    sPos = sPos + 1;

                    if(itemJson.status.equals("구매확정(시)") || itemJson.status.equals("구매확정")) {
                        isBuyConfirm = true;
                    }
                }

                if(isBuyConfirm) {
                    review.setVisibility(View.VISIBLE);
                } else {
                    review.setVisibility(View.GONE);
                }
            }
            return convertView;
        }
    }

    private void getListOrder(Boolean isClear)
    {
        if (isClear) {
            mItemAdapter.clear();
            mItemAdapter.notifyDataSetChanged();
        }

        SnipeApiController.getOrderDetail(mUser.getUserID(), mOrderNo, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:

                            JsonNode root = JsonUtil.parseTree(content);

                            mDetailData.mInfo = (OrderDetailJson.Info) JsonUtil.jsonToObject(root.get("info").toString(),OrderDetailJson.Info.class);
                            mDetailData.mPayment = (OrderDetailJson.Payment) JsonUtil.jsonToObject(root.get("payment").toString(),OrderDetailJson.Payment.class);
                            mDetailData.mReceiver = (OrderDetailJson.Receiver) JsonUtil.jsonToObject(root.get("receiver").toString(),OrderDetailJson.Receiver.class);
                            mDetailData.mRefund = (OrderDetailJson.Refund) JsonUtil.jsonToObject(root.get("refund").toString(),OrderDetailJson.Refund.class);
                            mDetailData.mItemArrayList = (ArrayList<OrderItemJson>) JsonUtil.jsonToArrayObject(root.path("item"), OrderItemJson.class);

                            mItemAdapter.clear();
                            mItemAdapter.addAll(mDetailData.mItemArrayList);
                            mItemAdapter.notifyDataSetChanged();
                            setData();
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }
}


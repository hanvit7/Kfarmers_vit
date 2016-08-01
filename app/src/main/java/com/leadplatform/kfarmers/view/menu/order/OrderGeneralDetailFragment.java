package com.leadplatform.kfarmers.view.menu.order;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.snipe.OrderGeneralItemJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.login.LoginActivity;

import java.util.ArrayList;

public class OrderGeneralDetailFragment extends BaseFragment {
    public static final String TAG = "OrderGeneralDetailFragment";
    private String mOrderNo = "";
    private UserDb mUser;
    private OrderGeneralItemJson mData;
    private ArrayList<String> mDeliveryCompany;

    enum StateNo {
        B1(R.id.radio1),
        B2(R.id.radio2),
        D3(R.id.radio3),
        C6(R.id.radio4);
        //P8(R.id.radio5);

        private int id;
        private StateNo(int id) {
            this.id = id;
        }
        public int getId() {
           return id;
        }
    }

    LinearLayout mFooter;
    Button mState_btn,adminBtn;
    TextView mStatusText;
    TextView mDeliveryText;
    RelativeLayout mProduct;
    TextView mProductText;
    LinearLayout mRelativeLayout;
    TextView mCountText;
    TextView mPriceText;
    TextView mTotalPriceText;
    TextView mOrderNoText;
    TextView mDateText;
    TextView mBankText;
    TextView mDepositNameText;
    TextView mOrderNameText;
    TextView mOrderPhoneText;
    TextView mOrderEmailText;
    TextView mDeliveryNameText;
    TextView mDeliveryPhoneText;
    TextView mDeliveryAddressText;
    TextView mPsText;

    private void assignViews(View v) {
        mFooter = (LinearLayout) v.findViewById(R.id.Footer);
        mState_btn = (Button) v.findViewById(R.id.state_btn);
        mStatusText = (TextView) v.findViewById(R.id.status_text);
        mDeliveryText = (TextView) v.findViewById(R.id.delivery_text);
        mProduct = (RelativeLayout) v.findViewById(R.id.product);
        mProductText = (TextView) v.findViewById(R.id.product_text);
        mRelativeLayout = (LinearLayout) v.findViewById(R.id.relativeLayout);
        mCountText = (TextView) v.findViewById(R.id.count_text);
        mPriceText = (TextView) v.findViewById(R.id.price_text);
        mTotalPriceText = (TextView) v.findViewById(R.id.totalPrice_text);
        mOrderNoText = (TextView) v.findViewById(R.id.orderNo_text);
        mDateText = (TextView) v.findViewById(R.id.date_text);
        mBankText = (TextView) v.findViewById(R.id.bank_text);
        mDepositNameText = (TextView) v.findViewById(R.id.depositName_text);
        mOrderNameText = (TextView) v.findViewById(R.id.orderName_text);
        mOrderPhoneText = (TextView) v.findViewById(R.id.orderPhone_text);
        mOrderEmailText = (TextView) v.findViewById(R.id.orderEmail_text);
        mDeliveryNameText = (TextView) v.findViewById(R.id.deliveryName_text);
        mDeliveryPhoneText = (TextView) v.findViewById(R.id.deliveryPhone_text);
        mDeliveryAddressText = (TextView) v.findViewById(R.id.deliveryAddress_text);
        mPsText = (TextView) v.findViewById(R.id.ps_text);

        adminBtn = (Button) v.findViewById(R.id.adminBtn);
    }

    public static OrderGeneralDetailFragment newInstance(String orderNo) {
        final OrderGeneralDetailFragment f = new OrderGeneralDetailFragment();
        final Bundle args = new Bundle();
        args.putString("orderNo", orderNo);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        final View v = inflater.inflate(R.layout.fragment_order_general_detail,container, false);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_ORDER_DETAIL, null);
        assignViews(v);

        adminBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kfarmers.net/order/manager"));
                startActivity(intent);
            }
        });
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getOrderDetail();
    }

    private void getOrderDetail() {
        SnipeApiController.getOrderGeneralDetail(mUser.getUserID(), mOrderNo, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            mData = (OrderGeneralItemJson) JsonUtil.jsonToObject(content, OrderGeneralItemJson.class);
                            makeView();
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

    private void getDeliveryCompany() {
        SnipeApiController.getDeliveryCompany(new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            mDeliveryCompany = (ArrayList<String>) JsonUtil.jsonToObject(content, ArrayList.class);
                            mDeliveryCompany.add(0,"배송업체 선택");
                            showDialog();
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

    private void showDialog() {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_order_general_dialog, null);

        StateNo no = StateNo.valueOf(mData.status_text.code);
        final RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.state_radio);
        radioGroup.check(no.getId());

        final Spinner deliverySpinner = (Spinner) view.findViewById(R.id.deliveryCompany_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.item_spinner_text, mDeliveryCompany);
        deliverySpinner.setAdapter(adapter);

        if(mData.delivery_company != null && !mData.delivery_company.trim().toString().isEmpty()) {
            deliverySpinner.setSelection(mDeliveryCompany.indexOf(mData.delivery_company));
        }

        final EditText mDeliveryNo = (EditText) view.findViewById(R.id.deliveryNo_editText);
        mDeliveryNo.setText(mData.delivery_code);

        UiDialog.showDialog(getActivity(), "주문상태 변경", view, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
            @Override
            public void onDialog(int type) {
                if(type == UiDialog.DIALOG_POSITIVE_LISTENER) {

                    String delivery = "";
                    String no = "";

                    if(deliverySpinner.getSelectedItemPosition()>0) {
                        delivery = (String) deliverySpinner.getSelectedItem();
                        no = mDeliveryNo.getText().toString().trim();
                    }

                    String state = "";
                    for (StateNo stateNo : StateNo.values()) {
                        if(stateNo.getId() == radioGroup.getCheckedRadioButtonId()) {
                            state = stateNo.name();
                            break;
                        }
                    }

                    final String finalDelivery = delivery;
                    final String finalNo = no;
                    SnipeApiController.getOrderGeneralStatus(mUser.getUserID(), mOrderNo, state, new SnipeResponseListener(getActivity()) {
                        @Override
                        public void onSuccess(int Code, String content, String error) {
                            try {
                                switch (Code) {
                                    case 200:
                                        SnipeApiController.getOrderGeneralDelivery(mUser.getUserID(), mOrderNo, finalDelivery, finalNo, new SnipeResponseListener(getActivity()) {
                                            @Override
                                            public void onSuccess(int Code, String content, String error) {
                                                try {
                                                    switch (Code) {
                                                        case 200:
                                                            UiDialog.showDialog(getActivity(),"주문 상태를 업데이트 하였습니다.");
                                                            getOrderDetail();
                                                            break;
                                                    }
                                                } catch (Exception e) {
                                                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                                                }
                                            }
                                        });
                                        break;
                                }
                            } catch (Exception e) {
                                UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            }
                        }
                    });
                }
            }
        });
    }

    private void makeView() {
        mStatusText.setText(mData.status_text.msg);
        mDeliveryText.setText(mData.delivery_company + " " + mData.delivery_code);

        mProductText.setText(mData.name);
        mCountText.setText(mData.cnt  +" 개");

        mPriceText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf((int) Double.parseDouble(mData.price))) + getResources().getString(R.string.korea_won));

        int price = Integer.parseInt(mData.price) * Integer.parseInt(mData.cnt);
        mTotalPriceText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(price) + getResources().getString(R.string.korea_won));

        mOrderNoText.setText(mData.unique_key);
        mDateText.setText(mData.datetime);
        mBankText.setText(mData.bank_account);
        mDepositNameText.setText(mData.deposit_name);

        mOrderNameText.setText(mData.send_name);
        mOrderPhoneText.setText(mData.send_hp);
        mOrderEmailText.setText(mData.send_email);

        mDeliveryNameText.setText(mData.receive_name);
        mDeliveryPhoneText.setText(mData.receive_hp);
        mDeliveryAddressText.setText(mData.receive_addr);
        mPsText.setText(mData.delivery_comment);

        mState_btn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if(mDeliveryCompany == null) {
                    getDeliveryCompany();
                } else {
                    showDialog();
                }
            }
        });
    }
}


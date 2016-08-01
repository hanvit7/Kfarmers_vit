package com.leadplatform.kfarmers.view.payment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.AddressJson;
import com.leadplatform.kfarmers.model.json.ProfileJson;
import com.leadplatform.kfarmers.model.json.snipe.AccountJson;
import com.leadplatform.kfarmers.model.json.snipe.CartItemJson;
import com.leadplatform.kfarmers.model.json.snipe.PayTypeJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentFragment extends BaseFragment {
    public static final String TAG = "PaymentFragment";

    enum AddressType
    {
        list,input
    }

    private int mTotalPrice = 0;
    private int mPayPrice = 0;
    private int mUserPoint = 0;
    private int mUserUsePoint = 0;

    private UserDb mUser;

    private ImageLoader imageLoader;
    private DisplayImageOptions optionsProduct;

    private LayoutInflater mInflater;

    private LinearLayout mMainView, mItemViewList,mDeliveryListViewMain,mDeliveryListViewMain_Sub,mDeliveryViewInput;

    private List<CartItemJson> mCartArrayList;
    private ArrayList<AddressJson> mAddressList;


    private Button mAddressListBtn,mAddressInputBtn;
    private List<String> mDeliveryInfoList;
    private Spinner mDeliverySpinner;
    private TextView mDeliveryInfoText;

    private CheckBox mAddressInput_Check;
    private Button mAddressInput_Search;
    public EditText mAddressInput_Name,mAddressInput_Phone,mAddressInput_Addr1,mAddressInput_Addr2;

    private boolean mIsBuyer = false;
    private LinearLayout mBuyerLayout;
    private RadioButton mBuyerCheck;
    private EditText mBuyerName,mBuyerPhone;


    public AddressJson mAddressAdd;
    private String mSelectedAddressIdx = "";
    private AddressType mSelectedAddressType;

    private ArrayList<String> mSelectedItem;
    private String mBuyDirect;

    private PayTypeJson mPayTypeJson;

    private ArrayList<AccountJson> mAccountArrayList;

    private RadioGroup mRadioPayGroup;
    private RadioButton mRadioPayCard,mRadioPayAccount,mRadioPayBank;

    private TextView mTotalItemText,mDeliveryPriceText,mTotalPriceText,mTotalPointText;

    private LinearLayout mPaymentStartBtn;

    private Spinner mCashSpinner;
    private Spinner mCashReceiptSpinner;

    private RadioGroup mRadioCashGroup;
    private RadioButton mRadioCashPersonal,mRadioCashLicensee,mRadioCashBin;

    private LinearLayout mCashView,mCashPhoneView,mCashLicenseeView;
    private RelativeLayout mCashReceiptSpinnerLayout;

    private EditText mCashPhoneEdit1,mCashPhoneEdit2,mCashPhoneEdit3;
    private EditText mCashLicenseeEdit1,mCashLicenseeEdit2,mCashLicenseeEdit3,mCashLicenseeEdit4;

    private EditText mDepositnameEdit;

    private String mItemOrignalData;

    private LinearLayout mLayoutPointView;
    private TextView mTxtPointBtn,mTxtPointTotal,mTxtPointNow,mTxtPointUseBtn;
    private EditText mEditPointUse;

    public static PaymentFragment newInstance(ArrayList<String> selectedItem, String direct) {
        final PaymentFragment f = new PaymentFragment();
        final Bundle args = new Bundle();
        args.putStringArrayList("idx", selectedItem);
        args.putString("direct", direct);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mSelectedItem = getArguments().getStringArrayList("idx");
            mBuyDirect = getArguments().getString("direct");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_payment, container, false);

        mInflater = inflater;
        mMainView = (LinearLayout) v.findViewById(R.id.MainView);
        mItemViewList = (LinearLayout) v.findViewById(R.id.ItemViewList);

        mPaymentStartBtn = (LinearLayout) v.findViewById(R.id.PaymentStartBtn);

        mAddressList = new ArrayList<>();

        setAddressInputView(v);
        setRadioPayView(v);
        setTotalPriceView(v);

        mPaymentStartBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Payment", null);
                cehckPayment();
            }
        });

        return v;
    }

    public void cehckPayment()
    {
        if(mIsBuyer)
        {
            if(mBuyerName.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "주문자 성함을 입력해주세요.");
                return;
            }
            else if(mBuyerPhone.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "주문자 연락처를 입력해주세요.");
                return;
            }
        }

        if(mSelectedAddressType == AddressType.list)
        {
            if(mSelectedAddressIdx != null && mSelectedAddressIdx.trim().isEmpty())
            {
                if(mAddressList == null || mAddressList.size() == 0) {
                    UiController.showDialog(getActivity(), "배송지 목록이 없습니다. 직접 입력해주세요.");
                    addressInputView();
                }
                else
                {
                    UiController.showDialog(getActivity(), "배송지를 선택해 주세요.");
                }
                return;
            }
        }
        else
        {
            if(mAddressInput_Name.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "받는사람 성함을 입력해주세요.");
                return;
            }
            else if(mAddressInput_Phone.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "받는사람 연락처를 입력해주세요.");
                return;
            }
            else if(mAddressInput_Addr1.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "주소를 검색해주세요.");
                return;
            }
            else if(mAddressInput_Addr2.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "나머지 주소를 입력해주세요.");
                return;
            }
        }

        if(mRadioPayGroup.getCheckedRadioButtonId()<=0) {
            UiController.showDialog(getActivity(), "결제 방식을 선택해주세요.");
            return;
        }


        String checkType = "";
        if(mRadioPayCard.isChecked())
        {
            checkType = "card";
        }
        else if(mRadioPayAccount.isChecked())
        {
            checkType = "account";
        }
        else if(mRadioPayBank.isChecked())
        {
            checkType = "bank";
        }

        if(checkType.equals("bank"))
        {
            if(mDepositnameEdit.getText().toString().trim().isEmpty())
            {
                UiController.showDialog(getActivity(), "입금자명을 입력해 주세요.");
                return;
            }

            if(mRadioCashGroup.getCheckedRadioButtonId() ==  R.id.RadioCashPersonal)
            {
                if(mCashReceiptSpinner.getSelectedItemPosition() == 0)
                {
                    if(mCashPhoneEdit1.getText().toString().trim().isEmpty() || mCashPhoneEdit2.getText().toString().trim().isEmpty() ||mCashPhoneEdit3.getText().toString().trim().isEmpty())
                    {
                        UiController.showDialog(getActivity(), "현금영수증 정보를 입력해 주세요.");
                        return;
                    }
                }
                if(mCashReceiptSpinner.getSelectedItemPosition() == 1)
                {
                    if(mCashLicenseeEdit1.getText().toString().trim().isEmpty() || mCashLicenseeEdit2.getText().toString().trim().isEmpty() || mCashLicenseeEdit3.getText().toString().trim().isEmpty() || mCashLicenseeEdit4.getText().toString().trim().isEmpty())
                    {
                        UiController.showDialog(getActivity(), "현금영수증 정보를 입력해 주세요.");
                        return;
                    }
                }
            }
            else if(mRadioCashGroup.getCheckedRadioButtonId() ==  R.id.RadioCashLicensee)
            {
                if(mCashPhoneEdit1.getText().toString().trim().isEmpty() || mCashPhoneEdit2.getText().toString().trim().isEmpty() ||mCashPhoneEdit3.getText().toString().trim().isEmpty())
                {
                    UiController.showDialog(getActivity(), "현금영수증 정보를 입력해 주세요.");
                    return;
                }
            }
        }

        String profile = DbController.queryProfileContent(getSherlockActivity());
        ProfileJson profileJson;
        if (profile == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }

        try
        {
            JsonNode root = JsonUtil.parseTree(profile);
            profileJson = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JSONObject sender = new JSONObject();
        JSONObject receiver = new JSONObject();
        JSONObject info = new JSONObject();
        JSONObject tax = new JSONObject();
        JSONObject item = new JSONObject();

        if(mIsBuyer)
        {
            sender.put("send_name",mBuyerName.getText().toString().trim());
            sender.put("send_hp",mBuyerPhone.getText().toString().trim());
        }
        else
        {
            if(mSelectedAddressType == AddressType.list)
            {
                AddressJson addressJson = mAddressList.get(Integer.parseInt(mSelectedAddressIdx));
                sender.put("send_name",addressJson.getShippingName());
                sender.put("send_hp",addressJson.getPhoneNo());
            }
            else
            {
                sender.put("send_name",mAddressInput_Name.getText().toString().trim());
                sender.put("send_hp",CommonUtil.PatternUtil.convertPhoneFormat(mAddressInput_Phone.getText().toString().trim()));
            }
        }

        sender.put("send_email",profileJson.Email == null ? "":profileJson.Email);
        sender.put("send_tel","");
        sender.put("send_zipcode","");
        sender.put("send_addr","");
        sender.put("send_addr_new","");

        if(mSelectedAddressType == AddressType.list)
        {
            AddressJson addressJson = mAddressList.get(Integer.parseInt(mSelectedAddressIdx));
            receiver.put("receive_name",addressJson.getShippingName());
            receiver.put("receive_tel","");
            receiver.put("receive_hp",addressJson.getPhoneNo());
            receiver.put("receive_zipcode",addressJson.getZipCode());
            receiver.put("receive_addr",addressJson.getAddress() +" " + addressJson.getAddress2());
            receiver.put("receive_addr_new","");
            receiver.put("receive_email","");
        }
        else
        {
            receiver.put("receive_name",mAddressInput_Name.getText().toString().trim());
            receiver.put("receive_tel","");
            receiver.put("receive_hp",CommonUtil.PatternUtil.convertPhoneFormat(mAddressInput_Phone.getText().toString().trim()));
            receiver.put("receive_zipcode",mAddressAdd.getZipCode());
            receiver.put("receive_addr",mAddressAdd.getAddress() +" " + mAddressInput_Addr2.getText().toString().trim());
            receiver.put("receive_addr_new","");
            receiver.put("receive_email","");
        }

        info.put("delivery_massage", mDeliveryInfoText.getText().toString().trim());
        info.put("payment_type",checkType);

        if(checkType.equals("bank")) {
            info.put("deposit_name",mDepositnameEdit.getText().toString().trim());
            info.put("bank_account", mAccountArrayList.get(mCashSpinner.getSelectedItemPosition()).site + ":" + mAccountArrayList.get(mCashSpinner.getSelectedItemPosition()).key + ":" + mAccountArrayList.get(mCashSpinner.getSelectedItemPosition()).add);
        }
        else
        {
            info.put("deposit_name","");
            info.put("bank_account", "");
        }

        info.put("price",mPayPrice);
        info.put("point",mUserUsePoint);

        String type = "";
        String sub_type = "";
        String data = "";

        if(checkType.equals("bank")) {
            if (mRadioCashGroup.getCheckedRadioButtonId() == R.id.RadioCashPersonal) {
                type = "A";

                if (mCashReceiptSpinner.getSelectedItemPosition() == 0) {
                    sub_type = "H";

                    data = mCashPhoneEdit1.getText().toString().trim() + "-" + mCashPhoneEdit2.getText().toString().trim() + "-" + mCashPhoneEdit3.getText().toString().trim();
                }
                if (mCashReceiptSpinner.getSelectedItemPosition() == 1) {
                    sub_type = "G";
                    data = mCashLicenseeEdit1.getText().toString().trim() + "-" + mCashLicenseeEdit2.getText().toString().trim() + "-" + mCashLicenseeEdit3.getText().toString().trim() + "-" + mCashLicenseeEdit4.getText().toString().trim();
                }
            } else if (mRadioCashGroup.getCheckedRadioButtonId() == R.id.RadioCashLicensee) {
                type = "B";
                sub_type = "T";
                data = mCashPhoneEdit1.getText().toString().trim() + "-" + mCashPhoneEdit2.getText().toString().trim() + "-" + mCashPhoneEdit3.getText().toString().trim();
            } else {
                type = "C";
            }
        }

        tax.put("type",type);
        tax.put("sub_type",sub_type);
        tax.put("data",data);

        final JSONArray jsonArray = new JSONArray();
        for(String str : mSelectedItem)
        {
            jsonArray.add(str);
        }
        item.put("idx",jsonArray);

        SnipeApiController.callCheckOutOrder(profileJson.ID,sender,receiver,info,item,tax,mBuyDirect, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                switch (Code)
                {
                    case 200:
                        if(mSelectedAddressType == AddressType.input && mAddressList.size()<5 && mAddressInput_Check.isChecked())
                        {
                            mAddressAdd.setShippingName(mAddressInput_Name.getText().toString().trim());
                            mAddressAdd.setPhoneNo(CommonUtil.PatternUtil.convertPhoneFormat(mAddressInput_Phone.getText().toString().trim()));
                            mAddressAdd.setAddress2(mAddressInput_Addr2.getText().toString().trim());

                            CenterController.addAddress(mAddressAdd, new CenterResponseListener(getActivity())
                            {
                                public void onSuccess(int Code, String content) {}

                                public void onFailure(int statusCode,
                                                      org.apache.http.Header[] headers, byte[] content,
                                                      Throwable error)
                                {}
                            });
                            /*TokenApiController.addAddress(getActivity(), mAddressAdd, new TokenResponseListener(getActivity()) {
                                public void onSuccess(int Code, String content) {}

                                public void onFailure(int statusCode,
                                                      org.apache.http.Header[] headers, byte[] content,
                                                      Throwable error) {}
                            });*/
                        }
                        Intent intent = new Intent(getActivity(),PaymentWebActivity.class);
                        intent.putExtra("orderNo",content);
                        intent.putExtra("orderData",mItemOrignalData);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    case 400:
                        UiController.showDialog(getActivity(), R.string.dialog_product_sell_stop, new CustomDialogListener() {
                            @Override
                            public void onDialog(int type) {
                                getActivity().setResult(PaymentActivity.PAYMENT_CHECK_ITEM);
                                getActivity().finish();
                            }
                        });
                        break;
                    case 401:
                        UiController.showDialog(getActivity(), R.string.dialog_product_sell_chg, new CustomDialogListener() {
                            @Override
                            public void onDialog(int type) {
                                getActivity().setResult(PaymentActivity.PAYMENT_CHECK_OPTION);
                                getActivity().finish();
                            }
                        });
                        break;
                    default:
                        UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
                            @Override
                            public void onDialog(int type) {
                                getActivity().setResult(PaymentActivity.PAYMENT_CHECK_ITEM);
                                getActivity().finish();
                            }
                        });
                        break;
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }

    private void getUserPoint() {

        SnipeApiController.getUserUsePoint(mUser.getUserID(), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);
                            if(root != null ){
                                if(mLayoutPointView.getVisibility() == View.GONE) {
                                    mLayoutPointView.setVisibility(View.VISIBLE);
                                }
                                else {
                                    mLayoutPointView.setVisibility(View.GONE);
                                }
                                mUserPoint = Integer.parseInt(root.get("member_point").asText());
                                mTxtPointNow.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(mUserPoint)) + " P");
                            }
                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            mUserPoint = 0;
                            mUserUsePoint = 0 ;
                            mPayPrice = mTotalPrice;
                            mTxtPointNow.setText("0 P");
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }


    public void setTotalPriceView(View v)
    {
        mTotalItemText = (TextView) v.findViewById(R.id.TotalItemText);
        mDeliveryPriceText = (TextView) v.findViewById(R.id.DeliveryPriceText);
        mTotalPriceText = (TextView) v.findViewById(R.id.TotalPriceText);

        mTxtPointBtn = (TextView) v.findViewById(R.id.TxtPointBtn);
        mTxtPointTotal = (TextView) v.findViewById(R.id.TxtPointTotal);
        mTxtPointNow = (TextView) v.findViewById(R.id.TxtPointNow);
        mTxtPointUseBtn = (TextView) v.findViewById(R.id.TxtPointUseBtn);
        mEditPointUse = (EditText) v.findViewById(R.id.EditPointUse);

        mTotalPointText = (TextView) v.findViewById(R.id.TotalPointText);

        mEditPointUse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                pointUseCheck();
                CommonUtil.UiUtil.hideSoftKeyboard(getActivity(), mTxtPointUseBtn);
                return true;
            }
        });

        mLayoutPointView = (LinearLayout) v.findViewById(R.id.LayoutPointView);

        mTxtPointBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                getUserPoint();
            }
        });

        mTxtPointUseBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                pointUseCheck();
                CommonUtil.UiUtil.hideSoftKeyboard(getActivity(),mTxtPointUseBtn);
            }
        });
    }

    public void pointUseCheck() {

        String pointText = mEditPointUse.getText().toString().trim();
        int point = 0;

        if(pointText.length() == 0) {
            UiController.showDialog(getActivity(),"사용하실 포인트를 입력해 주세요");
            return;
        }

        point = Integer.parseInt(mEditPointUse.getText().toString().trim());

        if(mUserPoint < point) {
            UiController.showDialog(getActivity(),"보유포인트가 부족합니다.");
            return;
        }
        if(mTotalPrice < point) {
            UiController.showDialog(getActivity(),"결제하실 금액보다 큽니다.");
            return;
        }

        if(!pointText.substring(pointText.length() - 1).equals("0")) {
            UiController.showDialog(getActivity(),"10P 단위로 사용 가능합니다");
            return;
        }

        mUserUsePoint = point;
        mTxtPointTotal.setText("- " + CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(mUserUsePoint)) + " P");

        mPayPrice = mTotalPrice - mUserUsePoint;
        mTotalPriceText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(mPayPrice)) + " 원");
        mLayoutPointView.setVisibility(View.GONE);

    }

    public void setRadioPayView(View v)
    {
        mRadioPayGroup = (RadioGroup) v.findViewById(R.id.RadioPayGroup);
        mRadioPayCard = (RadioButton) v.findViewById(R.id.RadioPayCard);
        mRadioPayAccount = (RadioButton) v.findViewById(R.id.RadioPayAccount);
        mRadioPayBank = (RadioButton) v.findViewById(R.id.RadioPayBank);

        mRadioCashGroup = (RadioGroup) v.findViewById(R.id.RadioCashGroup);
        mRadioCashPersonal = (RadioButton) v.findViewById(R.id.RadioCashPersonal);
        mRadioCashLicensee = (RadioButton) v.findViewById(R.id.RadioCashLicensee);
        mRadioCashBin = (RadioButton) v.findViewById(R.id.RadioCashBin);

        mCashSpinner = (Spinner) v.findViewById(R.id.CashSpinner);
        mCashReceiptSpinner = (Spinner) v.findViewById(R.id.CashReceiptSpinner);

        mCashPhoneView = (LinearLayout) v.findViewById(R.id.CashPhoneView);
        mCashLicenseeView = (LinearLayout) v.findViewById(R.id.CashLicenseeView);

        mCashView = (LinearLayout) v.findViewById(R.id.CashView);

        mCashView.setVisibility(View.GONE);

        mCashReceiptSpinnerLayout = (RelativeLayout) v.findViewById(R.id.CashReceiptSpinnerLayout);

        mCashPhoneEdit1 = (EditText) v.findViewById(R.id.CashPhoneEdit1);
        mCashPhoneEdit2 = (EditText) v.findViewById(R.id.CashPhoneEdit2);
        mCashPhoneEdit3 = (EditText) v.findViewById(R.id.CashPhoneEdit3);

        mCashLicenseeEdit1 = (EditText) v.findViewById(R.id.CashLicenseeEdit1);
        mCashLicenseeEdit2 = (EditText) v.findViewById(R.id.CashLicenseeEdit2);
        mCashLicenseeEdit3 = (EditText) v.findViewById(R.id.CashLicenseeEdit3);
        mCashLicenseeEdit4 = (EditText) v.findViewById(R.id.CashLicenseeEdit4);

        mDepositnameEdit = (EditText) v.findViewById(R.id.DepositnameEdit);

        ArrayList<String> subMenuList = new ArrayList<>();
        Collections.addAll(subMenuList, getResources().getStringArray(R.array.CashReceiptList));

        ArrayAdapter<String> receiptAdapter = new ArrayAdapter<String>(getActivity(),R.layout.item_spinner_text, subMenuList);
        mCashReceiptSpinner.setAdapter(receiptAdapter);

        mCashReceiptSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position == 0)
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click-CashReceipt-Personal-Type", "휴대폰 번호");
                    mCashPhoneView.setVisibility(View.VISIBLE);
                    mCashLicenseeView.setVisibility(View.GONE);
                }
                else
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click-CashReceipt-Personal-Type", "현금영수증 카드번호");
                    mCashPhoneView.setVisibility(View.GONE);
                    mCashLicenseeView.setVisibility(View.VISIBLE);
                }

                mCashPhoneEdit1.setText("");
                mCashPhoneEdit2.setText("");
                mCashPhoneEdit3.setText("");

                mCashLicenseeEdit1.setText("");
                mCashLicenseeEdit2.setText("");
                mCashLicenseeEdit3.setText("");
                mCashLicenseeEdit4.setText("");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mRadioCashGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.RadioCashPersonal: {
                        if (mCashReceiptSpinner.getSelectedItemPosition() == 0) {
                            mCashPhoneView.setVisibility(View.VISIBLE);
                            mCashLicenseeView.setVisibility(View.GONE);
                        } else {
                            mCashPhoneView.setVisibility(View.GONE);
                            mCashLicenseeView.setVisibility(View.VISIBLE);
                        }

                        mCashReceiptSpinnerLayout.setVisibility(View.VISIBLE);

                        mCashPhoneEdit1.setText("");
                        mCashPhoneEdit2.setText("");
                        mCashPhoneEdit3.setText("");

                        mCashLicenseeEdit1.setText("");
                        mCashLicenseeEdit2.setText("");
                        mCashLicenseeEdit3.setText("");
                        mCashLicenseeEdit4.setText("");

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click-CashReceipt", "개인 소득 공제용");

                        break;
                    }
                    case R.id.RadioCashLicensee: {
                        mCashReceiptSpinnerLayout.setVisibility(View.GONE);
                        mCashPhoneView.setVisibility(View.VISIBLE);
                        mCashLicenseeView.setVisibility(View.GONE);

                        mCashPhoneEdit1.setText("");
                        mCashPhoneEdit2.setText("");
                        mCashPhoneEdit3.setText("");

                        mCashLicenseeEdit1.setText("");
                        mCashLicenseeEdit2.setText("");
                        mCashLicenseeEdit3.setText("");
                        mCashLicenseeEdit4.setText("");

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click-CashReceipt", "사업자증빙용");
                        break;
                    }
                    case R.id.RadioCashBin: {
                        mCashReceiptSpinnerLayout.setVisibility(View.GONE);
                        mCashPhoneView.setVisibility(View.GONE);
                        mCashLicenseeView.setVisibility(View.GONE);

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click-CashReceipt", "신청안함");
                        break;
                    }
                }
            }
        });

        mRadioPayGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.RadioPayCard: {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Payment-Type ", "카드");
                        mCashView.setVisibility(View.GONE);
                        break;
                    }
                    case R.id.RadioPayAccount: {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Payment-Type ", "계좌이체");
                        mCashView.setVisibility(View.GONE);
                        break;
                    }
                    case R.id.RadioPayBank: {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Payment-Type ", "무통장");
                        mCashView.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
        });

        ArrayList<String> arr = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.item_spinner_text, arr);
        mCashSpinner.setAdapter(adapter);

    }

    public void setAddressInputView(View v)
    {
        mBuyerLayout = (LinearLayout) v.findViewById(R.id.BuyerLayout);
        mBuyerCheck = (RadioButton) v.findViewById(R.id.BuyerCheck);
        mBuyerName = (EditText) v.findViewById(R.id.BuyerName);
        mBuyerPhone = (EditText) v.findViewById(R.id.BuyerPhone);

        mAddressListBtn = (Button) v.findViewById(R.id.AddressListBtn);
        mAddressInputBtn = (Button) v.findViewById(R.id.AddressInputBtn);

        mDeliveryListViewMain = (LinearLayout) v.findViewById(R.id.DeliveryListViewMain);
        mDeliveryListViewMain_Sub = (LinearLayout) v.findViewById(R.id.DeliveryListViewMain_Sub);
        mDeliveryViewInput = (LinearLayout) v.findViewById(R.id.DeliveryViewInput);


        mAddressInput_Check = (CheckBox) v.findViewById(R.id.AddressInput_Check);
        mAddressInput_Search = (Button) v.findViewById(R.id.AddressInput_Search);

        mBuyerCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsBuyer)
                {
                    mIsBuyer = false;
                    mBuyerCheck.setChecked(mIsBuyer);
                    mBuyerLayout.setVisibility(View.GONE);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Present ", "false");
                }
                else
                {
                    mIsBuyer = true;
                    mBuyerCheck.setChecked(mIsBuyer);
                    mBuyerLayout.setVisibility(View.VISIBLE);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Present ", "true");
                }
            }
        });

        mAddressInput_Check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (mAddressList.size() >= 5) {
                    UiController.showDialog(getActivity(), "배송지 저장은 5개까지 가능 합니다.\n저장된 배송지를 삭제해주세요.");
                    mAddressInput_Check.setChecked(false);
                    return;
                } else {

                    if (!isChecked) {
                        mAddressInput_Check.setChecked(false);
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Address-Save ", "false");
                    }
                    else
                    {
                        mAddressInput_Check.setChecked(true);
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Address-Save ", "true");
                    }
                }
            }
        });

        mAddressInput_Search.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Address-Search ", null);
                ((PaymentActivity) getActivity()).showSearchWebViewFragment();
            }
        });

        mAddressInput_Name = (EditText) v.findViewById(R.id.AddressInput_Name);
        mAddressInput_Phone = (EditText) v.findViewById(R.id.AddressInput_Phone);
        mAddressInput_Addr1 = (EditText) v.findViewById(R.id.AddressInput_Addr1);
        mAddressInput_Addr2 = (EditText) v.findViewById(R.id.AddressInput_Addr2);


        mDeliverySpinner = (Spinner) v.findViewById(R.id.DeliveryInfoSpinner);
        mDeliveryInfoText = (TextView) v.findViewById(R.id.DeliveryInfoText);

        mDeliveryInfoList = new ArrayList<>();
        //Collections.addAll(mDeliveryInfoList, getResources().getStringArray(R.array.DeliveryInfoList));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),R.layout.item_spinner_text, mDeliveryInfoList);
        mDeliverySpinner.setAdapter(adapter);



        mAddressListBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Address", "배송지목록");
                addressListView();
            }
        });

        mAddressInputBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Address", "직접입력");
                addressInputView();
            }
        });

        mDeliverySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position == 0) {
                    mDeliveryInfoText.setText("");
                } else {
                    mDeliveryInfoText.setText(mDeliveryInfoList.get(position));
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PAYMENT, "Click_Delivery-Ask", mDeliveryInfoList.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void addressInputView()
    {
        mDeliveryListViewMain.setVisibility(View.GONE);
        mDeliveryViewInput.setVisibility(View.VISIBLE);
        mSelectedAddressType = AddressType.input;
        mAddressListBtn.setSelected(false);
        mAddressInputBtn.setSelected(true);
    }
    public void addressListView()
    {
        mDeliveryListViewMain.setVisibility(View.VISIBLE);
        mDeliveryViewInput.setVisibility(View.GONE);
        mSelectedAddressType = AddressType.list;
        mAddressListBtn.setSelected(true);
        mAddressInputBtn.setSelected(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();

        mUser = DbController.queryCurrentUser(getActivity());
        if (mUser == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
            return;
        }

        getCheckOutInfo();
        addressGet();
    }

    private void getCheckOutInfo() {

        final JSONArray jsonArray = new JSONArray();

        for(String str : mSelectedItem)
        {
            jsonArray.add(str);
        }

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("idx", jsonArray);

        SnipeApiController.getCheckOutInfo(mUser.getUserID(), jsonObject, mBuyDirect, new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:
                            JsonNode root = JsonUtil.parseTree(content);

                            mItemOrignalData = root.get("item").toString();
                            mCartArrayList = (List<CartItemJson>) JsonUtil.jsonToArrayObject(root.get("item"), CartItemJson.class);
                            mPayTypeJson = (PayTypeJson) JsonUtil.jsonToObject(root.get("pay_type").toString(), PayTypeJson.class);

                            mAccountArrayList = (ArrayList<AccountJson>) JsonUtil.jsonToArrayObject(root.get("account"), AccountJson.class);


                            ArrayList<String> stringArrayList = new ArrayList<String>();
                            for (AccountJson accountJson : mAccountArrayList) {
                                stringArrayList.add(accountJson.site);
                            }

                            ArrayAdapter<String> cashAdapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner_text, stringArrayList);
                            mCashSpinner.setAdapter(cashAdapter);


                            setViewPayData();

                            JsonNode jsonArray = root.get("delivery_message");

                            for (int i = 0; i < jsonArray.size(); i++) {
                                mDeliveryInfoList.add(jsonArray.get(i).textValue());
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.item_spinner_text, mDeliveryInfoList);
                            mDeliverySpinner.setAdapter(adapter);

                            setViewCartData();
                            break;

                        case 400:
                            UiController.showDialog(getActivity(), R.string.dialog_product_sell_stop, new CustomDialogListener() {
                                @Override
                                public void onDialog(int type) {
                                    getActivity().setResult(PaymentActivity.PAYMENT_CHECK_ITEM);
                                    getActivity().finish();
                                }
                            });
                            break;
                        case 401:
                            UiController.showDialog(getActivity(), R.string.dialog_product_sell_chg, new CustomDialogListener() {
                                @Override
                                public void onDialog(int type) {
                                    getActivity().setResult(PaymentActivity.PAYMENT_CHECK_OPTION);
                                    getActivity().finish();
                                }
                            });
                            break;
                        default:
                            UiController.showDialog(getActivity(), R.string.dialog_unknown_error, new CustomDialogListener() {
                                @Override
                                public void onDialog(int type) {
                                    getActivity().setResult(PaymentActivity.PAYMENT_CHECK_ITEM);
                                    getActivity().finish();
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

    public void addressGet() {

        CenterController.getAddress(new CenterResponseListener(getActivity()){

            @Override
            public void onSuccess(int Code, String content) {
                super.onSuccess(Code, content);

                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content).get("Data").get("List");
                            mAddressList = (ArrayList<AddressJson>) JsonUtil.jsonToArrayObject(root, AddressJson.class);
                            mSelectedAddressIdx ="";

                            if(mAddressList.size()>0) {
                                mSelectedAddressType = AddressType.list;
                                mAddressListBtn.setClickable(true);
                                mDeliveryListViewMain.setVisibility(View.VISIBLE);
                                mDeliveryViewInput.setVisibility(View.GONE);

                                mAddressListBtn.setSelected(true);
                                mAddressInputBtn.setSelected(false);
                                setViewAddressData();

                            }
                            else
                            {
                                mSelectedAddressType = AddressType.input;
                                mAddressListBtn.setClickable(false);
                                mDeliveryListViewMain.setVisibility(View.GONE);
                                mDeliveryViewInput.setVisibility(View.VISIBLE);

                                mAddressListBtn.setSelected(false);
                                mAddressInputBtn.setSelected(true);
                            }

                            if(mAddressList.size()>=5)
                            {
                                mAddressInput_Check.setChecked(false);
                            }
                            else
                            {
                                mAddressInput_Check.setChecked(true);
                            }

                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }

        });

        /*TokenApiController.getAddress(getActivity(), new TokenResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                super.onSuccess(Code, content);

                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content).get("rstContent");
                            mAddressList = (ArrayList<AddressJson>) JsonUtil.jsonToArrayObject(root, AddressJson.class);
                            mSelectedAddressIdx ="";

                            if(mAddressList.size()>0) {
                                mSelectedAddressType = AddressType.list;
                                mAddressListBtn.setClickable(true);
                                mDeliveryListViewMain.setVisibility(View.VISIBLE);
                                mDeliveryViewInput.setVisibility(View.GONE);

                                mAddressListBtn.setSelected(true);
                                mAddressInputBtn.setSelected(false);
                                setViewAddressData();

                            }
                            else
                            {
                                mSelectedAddressType = AddressType.input;
                                mAddressListBtn.setClickable(false);
                                mDeliveryListViewMain.setVisibility(View.GONE);
                                mDeliveryViewInput.setVisibility(View.VISIBLE);

                                mAddressListBtn.setSelected(false);
                                mAddressInputBtn.setSelected(true);
                            }

                            if(mAddressList.size()>=5)
                            {
                                mAddressInput_Check.setChecked(false);
                            }
                            else
                            {
                                mAddressInput_Check.setChecked(true);
                            }

                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers,
                                  byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });*/
    }

    private void setViewCartData() {
        int totalItemPrice = 0;
        int totalDeliveryPrice = 0;
        int totalPoint = 0;

        for (CartItemJson json : mCartArrayList) {
            View view = mInflater.inflate(R.layout.item_payment, null);
            LinearLayout optionView = (LinearLayout) view.findViewById(R.id.OptionView);

            int totalPrice = 0;

            ImageView imageView = (ImageView) view.findViewById(R.id.ProductImg);
            imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+json.image1, imageView, optionsProduct);

            ((TextView)view.findViewById(R.id.ProductName)).setText(json.name);
            //((TextView)view.findViewById(R.id.DeliveryPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf((int) Double.parseDouble(json.delivery_price))) + " 원");

            int count = 0;
            for (CartItemJson subJson : json.option) {
                View subView = mInflater.inflate(R.layout.item_payment_item_cell, null);

                ((TextView)subView.findViewById(R.id.Name)).setText(subJson.option_name);
                ((TextView)subView.findViewById(R.id.Count)).setText(subJson.cnt+"개");

                int price = (int)Double.parseDouble(subJson.buyprice) * Integer.parseInt(subJson.cnt);
                totalPrice = totalPrice + price;

                ((TextView)subView.findViewById(R.id.Price)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(price)) + " 원");

                int point = (int)Double.parseDouble(subJson.provide_point) * Integer.parseInt(subJson.cnt);
                totalPoint += point;

                if(count >= json.option.size()-1) {
                    subView.findViewById(R.id.BottomLine).setVisibility(View.GONE);
                }
                optionView.addView(subView);
                count++;
            }

            ((TextView)view.findViewById(R.id.TotalPrice)).setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalPrice)) + " 원");

            mItemViewList.addView(view);

            totalItemPrice = totalItemPrice + totalPrice;
            totalDeliveryPrice = totalDeliveryPrice + ((int) Double.parseDouble(json.delivery_price));
        }

        mTotalItemText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalItemPrice)) + " 원");
        mDeliveryPriceText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalDeliveryPrice)) + " 원");
        mTotalPriceText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalItemPrice+totalDeliveryPrice)) + " 원");
        mTotalPointText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalPoint)) + " P");

        mTotalPrice = totalItemPrice+totalDeliveryPrice;
        mPayPrice = mTotalPrice;
    }

    private void setViewPayData()
    {
        if(mPayTypeJson.card.equals("T"))
        {
            mRadioPayCard.setVisibility(View.VISIBLE);
        }
        else
        {
            mRadioPayCard.setVisibility(View.GONE);
        }
        if(mPayTypeJson.account.equals("T"))
        {
            mRadioPayAccount.setVisibility(View.VISIBLE);
        }
        else
        {
            mRadioPayAccount.setVisibility(View.GONE);
        }
        if(mPayTypeJson.bank.equals("T"))
        {
            mRadioPayBank.setVisibility(View.VISIBLE);
        }
        else
        {
            mRadioPayBank.setVisibility(View.GONE);
        }
    }

    private void setViewAddressData()
    {
        mDeliveryListViewMain_Sub.removeAllViews();

        int pos = 0;
        for(AddressJson address : mAddressList)
        {
            if (address != null) {

                View view = mInflater.inflate(R.layout.item_payment_address, null);
                view.setTag(pos);

                ((TextView) view.findViewById(R.id.Name)).setText(address.getShippingName());
                ((TextView)view.findViewById(R.id.Address)).setText("[" + address.getZipCode() + "] " + address.getAddress() + " " + address.getAddress2());
                ((TextView)view.findViewById(R.id.Phone)).setText(address.getPhoneNo());

                ImageButton dellBtn = (ImageButton)view.findViewById(R.id.DellBtn);
                dellBtn.setTag(address);

                dellBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        final AddressJson address = (AddressJson) v.getTag();

                        CenterController.delAddress(address, new CenterResponseListener(getActivity()) {
                            public void onSuccess(int Code, String content) {
                                mAddressList.remove(address);
                                mSelectedAddressIdx = "";
                                setViewAddressData();
                            }

                            public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] content, Throwable error) {
                            }
                        });

                        /*TokenApiController.delAddress(address,
                                new TokenResponseListener(getActivity()) {
                                    public void onSuccess(int Code, String content) {
                                        mAddressList.remove(address);
                                        mSelectedAddressIdx = "";
                                        setViewAddressData();
                                    }

                                    public void onFailure(int statusCode, org.apache.http.Header[] headers, byte[] content, Throwable error) {
                                    }
                                });*/
                    }
                });

                view.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {

                        int pos = (int) v.getTag();
                        mSelectedAddressIdx = pos + "";

                        for (int i = 0; i < mDeliveryListViewMain_Sub.getChildCount(); i++) {
                            View subView = mDeliveryListViewMain_Sub.getChildAt(i);
                            RadioButton radioButton = (RadioButton) subView.findViewById(R.id.RadioBtn);

                            if (pos == i) {
                                radioButton.setChecked(true);
                            } else {
                                radioButton.setChecked(false);
                            }
                        }
                    }
                });

                mDeliveryListViewMain_Sub.addView(view);

            }
            pos++;
        }
    }

}


package com.leadplatform.kfarmers.view.inquiry;

import android.os.Bundle;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.SnipeApiController;
import com.leadplatform.kfarmers.controller.SnipeResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.market.ProductActivity;

import java.util.List;

public class ProductInquiryActivity extends BaseFragmentActivity {
    public static final String TAG = "ProductInquiryActivity";

    public static final String DATA = "data";
    public static final String OPTION = "option";

    public LinearLayout mBankLayout,mInfoLayout;

    public TextView mProductName,mBankNo,mInfoText;
    public EditText mDes;

    public Button mInquiryBtn;

    public ProductJson mProductJson;
    public List<ProductJson> mOptionList;

    public String mSendText = "";

    UserDb mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_inquiry);

        if (getIntent() != null) {
            mProductJson = (ProductJson) getIntent().getSerializableExtra(DATA);
            mOptionList = (List<ProductJson>)getIntent().getSerializableExtra(OPTION);
        }


        mUser = DbController.queryCurrentUser(ProductInquiryActivity.this);

        mInfoText = (TextView)findViewById(R.id.InfoText);
        mBankLayout = (LinearLayout)findViewById(R.id.BankLayout);
        mProductName = (TextView)findViewById(R.id.ProductName);
        mBankNo = (TextView)findViewById(R.id.BankNo);
        mDes = (EditText)findViewById(R.id.Des);
        mInquiryBtn = (Button) findViewById(R.id.InquiryBtn);

       // 스크롤뷰 안 스크롤 작동 코드
        mDes.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.Des) {
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

        mInfoText.setText(Html.fromHtml(getString(R.string.product_type_normal)));

        for(ProductJson productJson : mOptionList)
        {
            if(!mSendText.equals(""))
            {
                mSendText +="\n";
            }
            mSendText += productJson.name + " " + productJson.buyprice+"원";
        }

        mProductName.setText(mSendText);

        String bankText = "";
        if(mProductJson.verification.equals(ProductActivity.TYPE3) && mProductJson.bank_account != null && !mProductJson.bank_account.isEmpty()) {
            String bank[] = mProductJson.bank_account.split(":");
            if (bank != null && bank.length > 2) {
                bankText += "예금주 : "+bank[0]+"\n";
                bankText += "은행명 : "+bank[1]+"\n";
                bankText += "계좌번호 : "+bank[2];

                mSendText += "\n\n계좌정보\n";
                mSendText += bankText;

                mBankLayout.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            mBankLayout.setVisibility(View.GONE);
            mSendText += "\n";
        }
        mBankNo.setText(bankText);

        mInquiryBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                if(mDes.getText().toString().trim().isEmpty())
                {
                    UiDialog.showDialog(ProductInquiryActivity.this,"문의 내용을 입력해 주세요.");
                    return;
                }
                SnipeApiController.checkChatRoom(mUser.getUserID(), mProductJson.id, new SnipeResponseListener(ProductInquiryActivity.this) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {
                        try {
                            switch (Code) {
                                case 200:
                                    String des = "상품정보\n"+mSendText + "\n문의내용\n" + mDes.getText().toString().trim();
                                    SnipeApiController.insertChat(content, mUser.getUserID(), des, "comment", new SnipeResponseListener(ProductInquiryActivity.this) {
                                        @Override
                                        public void onSuccess(int Code, String content, String error) {
                                            try {
                                                switch (Code) {
                                                    case 200:
                                                        Toast.makeText(mContext,"판매자에게 문의글을 전송 했습니다.",Toast.LENGTH_SHORT).show();
                                                        finish();
                                                        break;
                                                    default:
                                                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                                }
                                            } catch (Exception e) {
                                                UiController.showDialog(mContext, R.string.dialog_unknown_error);
                                            }
                                        }
                                    });
                                    break;
                                default:
                                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            }
                        } catch (Exception e) {
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_detail);
        TextView actionBarTitleText = (TextView) findViewById(R.id.actionbar_title_text_view);
        actionBarTitleText.setText(mProductJson.farm_name);
        initActionBarHomeBtn();
    }
}

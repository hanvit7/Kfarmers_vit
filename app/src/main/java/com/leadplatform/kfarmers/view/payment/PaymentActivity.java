package com.leadplatform.kfarmers.view.payment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.json.AddressJson;
import com.leadplatform.kfarmers.model.json.DataGoAddressJson;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.address.AddressSearchFragment;
import com.leadplatform.kfarmers.view.address.AddressSearchWebFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

import java.util.ArrayList;

public class PaymentActivity extends BaseFragmentActivity {
    public static final String TAG = "PaymentActivity";

    public static final int PAYMENT_CHECK =  9000;
    public static final int PAYMENT_CHECK_ITEM =  9001;
    public static final int PAYMENT_CHECK_OPTION =  9002;

    private ArrayList<String> mSelectedItem;
    private String mBuyDirect;

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PAYMENT);
        mActivityList.add(this);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);

        if(getIntent().getStringArrayListExtra("idx") != null)
        {
            mSelectedItem = getIntent().getStringArrayListExtra("idx");
            mBuyDirect = getIntent().getStringExtra("direct");
        }
        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState) {
        PaymentFragment fragment = PaymentFragment.newInstance(mSelectedItem,mBuyDirect);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, PaymentFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        ((TextView) findViewById(R.id.title)).setText("주문하기");
        initActionBarHomeBtn();
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {

        super.onActivityResult(arg0, arg1, arg2);
    }

    public PaymentFragment getAddFragment() {
        FragmentManager fm = getSupportFragmentManager();
        PaymentFragment fragment = (PaymentFragment) fm.findFragmentByTag(PaymentFragment.TAG);
        if (fragment != null) {
            return fragment;
        }
        return null;
    }

    public void showSearchFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        AddressSearchFragment addressSearchFragment = AddressSearchFragment
                .newInstance();
        ft.addToBackStack(null);

        addressSearchFragment.show(ft, AddressSearchFragment.TAG);
    }

    public void showSearchWebViewFragment() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        AddressSearchWebFragment addressSearchWebFragment = AddressSearchWebFragment.newInstance();
        ft.addToBackStack(null);
        addressSearchWebFragment.show(ft, AddressSearchWebFragment.TAG);
    }

    public AddressSearchFragment getSearchFragment() {
        FragmentManager fm = getSupportFragmentManager();
        AddressSearchFragment fragment = (AddressSearchFragment) fm
                .findFragmentByTag(AddressSearchFragment.TAG);
        if (fragment != null) {
            return fragment;
        }
        return null;
    }

    public AddressSearchWebFragment getSearchWebFragment() {
        FragmentManager fm = getSupportFragmentManager();
        AddressSearchWebFragment fragment = (AddressSearchWebFragment) fm
                .findFragmentByTag(AddressSearchWebFragment.TAG);
        if (fragment != null) {
            return fragment;
        }
        return null;
    }

    public void setSearchData(DataGoAddressJson json) {
        final PaymentFragment fragment = getAddFragment();
        if (fragment != null) {

            String address1 = json.getAdres().replaceAll("\\s{2,}"," ");

            fragment.mAddressAdd = new AddressJson();
            fragment.mAddressAdd.setAddress(address1);
            fragment.mAddressAdd.setZipCode(json.getZipNo());
            fragment.mAddressAdd.setZipCodeCategory("1");
            fragment.mAddressInput_Addr1.setText("[" + json.getZipNo() + "] "
                    + address1);
            fragment.mAddressInput_Addr2.setText("");

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run()
                {
                    fragment.mAddressInput_Addr2.requestFocus();
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.showSoftInput(fragment.mAddressInput_Addr2, 0);
                }
            }, 100);
        }
    }

    public void setSearchWebData(AddressJson addressJson) {
        final PaymentFragment fragment = getAddFragment();
        if (fragment != null) {

            fragment.mAddressAdd = addressJson;

            fragment.mAddressInput_Addr1.setText("[" + addressJson.getZipCode() + "] " + addressJson.getAddress());
            fragment.mAddressInput_Addr2.setText("");

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run()
                {
                    fragment.mAddressInput_Addr2.requestFocus();
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.showSoftInput(fragment.mAddressInput_Addr2, 0);
                }
            }, 100);
        }
    }

    public void hideSearchFragment() {
        AddressSearchFragment fragment = getSearchFragment();
        if (fragment != null) {
            fragment.dismiss();
        }
    }

    public void hideSearchWebFragment() {
        AddressSearchWebFragment fragment = getSearchWebFragment();
        if (fragment != null) {
            fragment.dismiss();
        }
    }
}

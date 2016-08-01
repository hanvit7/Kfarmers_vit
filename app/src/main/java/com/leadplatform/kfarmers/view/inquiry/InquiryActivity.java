package com.leadplatform.kfarmers.view.inquiry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class InquiryActivity extends BaseFragmentActivity {
    public static final String TAG = "InquiryActivity";

    String mIdx = "";

    //ContentObserver mContentObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);

        fragmentInquiryList();

        if (getIntent().getExtras() != null)
        {
            fragmentChat(getIntent().getStringExtra("index"));
        }

        /*mContentObserver = new ContentObserver(new Handler()){
            public void onChange( boolean selfChange ){
                super.onChange(selfChange);
                Uri uri = getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI+InquiryProvider.INQUIRY_GET), new ContentValues());
                List<String> data = uri.getPathSegments();
                String idx = data.get(0);

                if(mIdx.equals(idx))
                {
                    FragmentManager fm = getSupportFragmentManager();
                    InquiryChatFragment fragment = (InquiryChatFragment) fm.findFragmentByTag(InquiryChatFragment.TAG);
                    if (fragment != null) {
                        ContentValues cv = new ContentValues();
                        cv.put("show", "true");
                        getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI+InquiryProvider.INQUIRY_CHECK), cv);
                        fragment.getListChat(true);
                    }
                    else
                    {
                        ContentValues cv = new ContentValues();
                        cv.put("show", "false");
                        getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI+InquiryProvider.INQUIRY_CHECK), cv);
                    }
                }
                else
                {
                    ContentValues cv = new ContentValues();
                    cv.put("show", "false");
                    getContentResolver().insert(Uri.parse(InquiryProvider.CONTENT_URI+InquiryProvider.INQUIRY_CHECK), cv);
                }
            }
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }

            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }
        };*/
    }

    BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle results = getResultExtras(true);
            String idx = intent.getStringExtra("idx");

            if(mIdx.equals(idx))
            {
                FragmentManager fm = getSupportFragmentManager();
                InquiryChatFragment fragment = (InquiryChatFragment) fm.findFragmentByTag(InquiryChatFragment.TAG);
                if (fragment != null) {
                    results.putString("result","true");
                    fragment.getListChat(true);
                }
                else
                {
                    results.putString("result","false");
                }
            }
            else
            {
                results.putString("result", "false");
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.leadplatform.kfarmers.view.inquiry.chat");
        registerReceiver(chatReceiver, filter);

        //getContentResolver().registerContentObserver(InquiryProvider.CONTENT_URI, true, mContentObserver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(chatReceiver);
        //getContentResolver().unregisterContentObserver(mContentObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void fragmentInquiryList() {
        InquiryListFragment fragment = InquiryListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, InquiryListFragment.TAG);
        ft.commit();
    }

    public void fragmentChat(String idx) {
        mIdx = idx;
        InquiryChatFragment fragment = InquiryChatFragment.newInstance(idx);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, InquiryChatFragment.TAG);
        ft.addToBackStack(InquiryChatFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        ((TextView) findViewById(R.id.title)).setText(getString(R.string.MenuLInquiry));
        initActionBarHomeBtn();
    }

    @Override
    public void onBackPressed() {

        if(getSupportFragmentManager().getBackStackEntryCount()>0)
        {
            getSupportFragmentManager().popBackStack();

            FragmentManager fm = getSupportFragmentManager();
            InquiryListFragment fragment = (InquiryListFragment) fm.findFragmentByTag(InquiryListFragment.TAG);
            if (fragment != null) {
                fragment.onForceUpdate();
            }
        }
        else
        {
            super.onBackPressed();
        }
    }
}

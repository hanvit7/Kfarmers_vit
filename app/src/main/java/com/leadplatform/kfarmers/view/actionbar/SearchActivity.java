package com.leadplatform.kfarmers.view.actionbar;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class SearchActivity extends BaseFragmentActivity
{
    public static final String TAG = "SearchActivity";

    private EditText searchEdit;
    private ImageView closeBtn;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_search);

        initContentView(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SEARCH_MAIN);

    }

    private void initContentView(Bundle savedInstanceState)
    {

    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

        getSupportActionBar().setCustomView(R.layout.view_actionbar_search);
        searchEdit = (EditText) findViewById(R.id.searchEdit);
        closeBtn = (ImageView) findViewById(R.id.closeBtn);

        searchEdit.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearchActionBtnClicked();
                    return true;
                }
                return false;
            }
        });

        closeBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onCloseBtnClicked();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(searchEdit, InputMethodManager.SHOW_FORCED);
            }
        }, 100);
    }

    private void onSearchActionBtnClicked()
    {
        if (searchEdit.getText().toString().trim().length() == 0)
        {
            UiController.showDialog(this, R.string.dialog_empty_search);
        }
        else
        {
            KfarmersAnalytics.onClick(KfarmersAnalytics.S_SEARCH_MAIN, "Click_Search", searchEdit.getText().toString());

            SearchListFragment fragment = SearchListFragment.newInstance(searchEdit.getText().toString());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.fragment_container, fragment, SearchListFragment.TAG);
            ft.commit();
        }
    }

    private void onCloseBtnClicked()
    {
        searchEdit.setText("");
    }
}

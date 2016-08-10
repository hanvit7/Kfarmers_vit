package com.leadplatform.kfarmers.view.market;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.util.OnKeyBackPressedListener;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.cart.CartActivity;
import com.leadplatform.kfarmers.view.common.YoutubePlayActivity;
import com.leadplatform.kfarmers.view.common.YoutubePlayFragment;
import com.leadplatform.kfarmers.view.login.LoginActivity;

public class ProductActivity extends BaseFragmentActivity {
    public static final String TAG = "ProductActivity";

    //검증 상품
    public static final String TYPE = "T";

    //소비자 검증
    public static final String TYPE1 = "T1";
    //MD검증
    public static final String TYPE2 = "T2";
    //일반
    public static final String TYPE3 = "F";

    private String productIndex;
    private TextView actionBarTitleText;


    private OnKeyBackPressedListener mOnBackKeyPressedListener;

    public void setOnKeyBackPressedListener(OnKeyBackPressedListener listener) {
        mOnBackKeyPressedListener = listener;
    }

    /***************************************************************/
    // Override

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.7f;
        getWindow().setAttributes(layoutParams);*/
    }

    /**
     * ***********************************************************
     */


    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            productIndex = intent.getStringExtra("productIndex");
        }

        ProductFragment fragment = ProductFragment.newInstance(productIndex);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, ProductFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar_product);
        actionBarTitleText = (TextView) findViewById(R.id.actionbar_title_text_view);
        initActionBarHomeBtn();

        ImageButton cartBtn = (ImageButton) findViewById(R.id.CartBtn);
        cartBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                UserDb user = DbController.queryCurrentUser(mContext);
                if (user == null)
                {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                else
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_ActionBar-Cart", null);
                    startActivity(new Intent(mContext, CartActivity.class));
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
    }

    /***************************************************************/
    // Display

    /**
     * ***********************************************************
     */
    public void displayActionBarTitleText(String title) {
        if (actionBarTitleText != null)
            actionBarTitleText.setText(title);
    }

    /***************************************************************/
    // Method

    /**
     * ***********************************************************
     */

    @Override
    public void onBackPressed() {
        if (mOnBackKeyPressedListener != null) {
            mOnBackKeyPressedListener.onBack();
        }else {
            if(getSupportFragmentManager().getBackStackEntryCount()>0)
            {
                YoutubePlayFragment fragment = (YoutubePlayFragment) getSupportFragmentManager().findFragmentByTag(YoutubePlayFragment.TAG);
                if (fragment != null)
                {
                    getSupportFragmentManager().popBackStack();
                }
            }
            else
            {
                super.onBackPressed();
            }
        }
    }


    void youtubeFragment(final String idx)
    {
        /*YoutubePlayFragment fragment = YoutubePlayFragment.newInstance(idx);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, YoutubePlayFragment.TAG);
        ft.addToBackStack(YoutubePlayFragment.TAG);
        ft.commit();*/

        /*Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ProductActivity.this, YoutubePlayActivity.class);
                intent.putExtra("idx",idx);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        };
        BlurBehind.getInstance().execute(ProductActivity.this, runnable);*/

        Intent intent = new Intent(ProductActivity.this, YoutubePlayActivity.class);
        intent.putExtra("idx",idx);
        startActivity(intent);

    }
}

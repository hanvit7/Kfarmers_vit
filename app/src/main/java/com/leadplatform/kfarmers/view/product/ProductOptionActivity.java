package com.leadplatform.kfarmers.view.product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.LimitListView;

import java.util.ArrayList;
import java.util.List;

public class ProductOptionActivity extends BaseFragmentActivity {
    public static final String TAG = "ProductOptionActivity";

    public static final int REQUEST_OPTION = 1000;

    public static final String INTENT_OPTION_LIST = "data";
    public static final String INTENT_OPTION_NAME = "name";

    public static final int RESULT_CART = 100;
    public static final int RESULT_BUY = 200;

    enum type
    {
        init_list,
        select_list,
        optin_list
    }

    private int mScreenHeight;
    private View mSelectView,mOptionView;

    private String ProductName;
    private ArrayList<ProductJson> mOptionArrayList;
    private ArrayList<ProductJson> mSelectArrayList;

    private LimitListView mSelectListView,mOptionListView;

    private SelectListAdapter mSelectListAdapter;
    private OptionListAdapter mOptionListAdapter;

    private RelativeLayout mSelectViewBox;
    private TextView mSelectViewBoxText;

    private Button mCartBtn;
    private Button mBuyBtn;

    private RelativeLayout mRootView;

    private boolean mNowShowSelectView = false;
    private boolean mIsAni = false;

    private Toast mToast;

    @Override
    public void initActionBar() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<ProductJson> arrayList = (ArrayList<ProductJson>) intent.getSerializableExtra(INTENT_OPTION_LIST);
            mOptionArrayList = arrayList;
            mSelectArrayList = new ArrayList<>();
            ProductName = intent.getStringExtra(INTENT_OPTION_NAME);
            SetProductInitView();
        }

        mToast = new Toast(mContext);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PRODUCT_OPTION,ProductName);
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_product_option);

        mRootView = (RelativeLayout) findViewById(R.id.RootView);

        mSelectView = findViewById(R.id.SelectView);
        mOptionView = findViewById(R.id.OptionView);

        mSelectListView = (LimitListView) findViewById(R.id.SelectListView);
        mOptionListView = (LimitListView) findViewById(R.id.OptionListView);

        mSelectViewBox = (RelativeLayout)mSelectView.findViewById(R.id.OptionBtn);
        mSelectViewBoxText = (TextView)mSelectView.findViewById(R.id.OptionText);

        mCartBtn = (Button) mSelectView.findViewById(R.id.CartBtn);
        mBuyBtn = (Button) mSelectView.findViewById(R.id.BuyBtn);

        mSelectListView.setDivider(null);
        mSelectListView.setDividerHeight(0);

        mOptionListView.setDivider(null);
        mOptionListView.setDividerHeight(0);

        mCartBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Cart", null);
                Intent intent = new Intent();
                intent.putExtra(INTENT_OPTION_LIST, mSelectArrayList);
                setResult(RESULT_CART, intent);

                if(mNowShowSelectView)
                {
                    centerToBottom(type.select_list,true,true,300L);
                }
                else
                {
                    centerToBottom(type.optin_list,true,true,300L);
                }
            }
        });

        mBuyBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Buy", null);

                Intent intent = new Intent();
                intent.putExtra(INTENT_OPTION_LIST, mSelectArrayList);
                setResult(RESULT_BUY, intent);

                if(mNowShowSelectView)
                {
                    centerToBottom(type.select_list,true,true,300L);
                }
                else
                {
                    centerToBottom(type.optin_list,true,true,300L);
                }
            }
        });

        mSelectViewBox.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                if (mOptionArrayList.size() > 1 && mIsAni == false) {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Option", null);
                    showView(type.optin_list);
                }
            }
        });
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        showView(type.init_list);
        //mRootView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_ban_in));
        Animation animation = AnimationUtils.loadAnimation(ProductOptionActivity.this, R.anim.fade_ban_in);
        mRootView.startAnimation(animation);
    }

    public void SetProductInitView()
    {
        setSelectListView();
        setOptionListView();
    }

    public void setSelectListView()
    {
        mSelectListAdapter = new SelectListAdapter(mContext,R.layout.item_minicart_select_cell,mSelectArrayList);
        mSelectListView.setAdapter(mSelectListAdapter);
    }
    public void setOptionListView()
    {
        mOptionListAdapter = new OptionListAdapter(mContext,R.layout.item_minicart_option_list_cell,mOptionArrayList);
        mOptionListView.setAdapter(mOptionListAdapter);

        mOptionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(mOptionView.getVisibility() == View.GONE)
                {
                    return;
                }
                if(mIsAni) {
                    return;
                }

                ProductJson optionJson = mOptionArrayList.get(position);

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Item", optionJson.name);

                if (optionJson.soldout.equals("T") || Integer.valueOf(optionJson.stock) == 0) {
                    mToast.makeText(mContext, "품절된 상품입니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (ProductJson json : mSelectArrayList) {
                    if (json.idx.equals(optionJson.idx)) {
                        mToast.makeText(mContext, "선택된 옵션입니다.", Toast.LENGTH_SHORT).show();
                        showView(type.select_list);
                        return;
                    }
                }
                optionJson.count = "1";
                mSelectArrayList.add(optionJson);
                showView(type.select_list);
            }
        });
    }

    public void showView(type num) {

        if(mIsAni) return;
        mIsAni = true;

        if (num == type.init_list) {

            if(mOptionArrayList.size()>1)
            {
                mNowShowSelectView = false;
                bottomToCenter(type.optin_list,true);
            }
            else
            {
                ProductJson optionJson = mOptionArrayList.get(0);

                if(optionJson.soldout.equals("T") || Integer.valueOf(optionJson.stock) == 0)
                {
                    mSelectViewBox.setVisibility(View.VISIBLE);
                    mSelectViewBoxText.setText("품절된 상품입니다.");

                    mCartBtn.setEnabled(false);
                    mBuyBtn.setEnabled(false);
                }
                else
                {
                    mSelectViewBox.setVisibility(View.GONE);
                    optionJson.count = "1";
                    mSelectArrayList.add(optionJson);
                }
                mNowShowSelectView = true;
                bottomToCenter(type.select_list,true);
            }
        }

        else if (num == type.select_list) {
            mSelectListAdapter.notifyDataSetChanged();
            mNowShowSelectView = true;
            centerToTop();
            bottomToCenter(type.select_list,false);
            /*mOptionView.setVisibility(View.GONE);
            mSelectView.setVisibility(View.VISIBLE);*/

        } else {
            mNowShowSelectView = false;
            mOptionListAdapter.notifyDataSetChanged();
            centerToBottom(type.select_list,true,false,600L);
            topToCenter();



            /*mBinLayout.setVisibility(View.GONE);
            mSelectView.setVisibility(View.GONE);
            mOptionView.setVisibility(View.VISIBLE);*/

        }

    }


    private void bottomToCenter(type type, final boolean isAni)
    {
        TranslateAnimation localTranslateAnimation = new TranslateAnimation(0.0F, 0.0F, this.mScreenHeight, 0.0F);
        switch (type)
        {
            case optin_list:
                //localTranslateAnimation.setInterpolator(new OutInterpolator(0.1F));
                localTranslateAnimation.setInterpolator(new OvershootInterpolator(1F));
                localTranslateAnimation.setDuration(500L);
                localTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation paramAnonymousAnimation) {
                        mOptionView.setVisibility(View.VISIBLE);

                        if (isAni) {
                            mIsAni = false;
                        }
                    }

                    public void onAnimationRepeat(Animation paramAnonymousAnimation) {
                    }

                    public void onAnimationStart(Animation paramAnonymousAnimation) {
                        mOptionView.setVisibility(View.VISIBLE);
                    }
                });
                ProductOptionActivity.this.mOptionView.startAnimation(localTranslateAnimation);
                break;
            case select_list:
                localTranslateAnimation = new TranslateAnimation(0.0F, 0.0F, this.mScreenHeight, 0.0F);
                //localTranslateAnimation.setInterpolator(new OutInterpolator(0.1F));
                localTranslateAnimation.setInterpolator(new OvershootInterpolator(1F));
                localTranslateAnimation.setDuration(500L);
                localTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation paramAnonymousAnimation) {
                        mSelectView.setVisibility(View.VISIBLE);
                        if (isAni) {
                            mIsAni = false;
                        }
                    }

                    public void onAnimationRepeat(Animation paramAnonymousAnimation) {
                    }

                    public void onAnimationStart(Animation paramAnonymousAnimation) {
                        mSelectView.setVisibility(View.VISIBLE);
                    }
                });
                ProductOptionActivity.this.mSelectView.startAnimation(localTranslateAnimation);
                break;
        }
    }

    private void centerToTop()
    {
        TranslateAnimation localTranslateAnimation = new TranslateAnimation(0.0F, 0.0F, 0.0F, -this.mScreenHeight);
        //localTranslateAnimation.setInterpolator(new OutInterpolator(0.1F));
        localTranslateAnimation.setInterpolator(new OvershootInterpolator(1F));
        localTranslateAnimation.setDuration(600L);
        localTranslateAnimation.setStartOffset(100L);
        localTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation paramAnonymousAnimation) {
                mOptionView.setVisibility(View.GONE);

                mIsAni = false;
            }

            public void onAnimationRepeat(Animation paramAnonymousAnimation) {
            }

            public void onAnimationStart(Animation paramAnonymousAnimation) {
                mOptionView.setVisibility(View.VISIBLE);
            }
        });
        ProductOptionActivity.this.mOptionView.startAnimation(localTranslateAnimation);
    }
    private void topToCenter()
    {
        TranslateAnimation localTranslateAnimation = new TranslateAnimation(0.0F, 0.0F, -this.mScreenHeight, 0.0F);
        //localTranslateAnimation.setInterpolator(new OutInterpolator(0.1F));
        localTranslateAnimation.setInterpolator(new OvershootInterpolator(1F));
        localTranslateAnimation.setDuration(500L);
        localTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationEnd(Animation paramAnonymousAnimation) {
                mOptionView.setVisibility(View.VISIBLE);
            }

            public void onAnimationRepeat(Animation paramAnonymousAnimation) {
            }

            public void onAnimationStart(Animation paramAnonymousAnimation) {
                mOptionView.setVisibility(View.VISIBLE);
            }
        });
        ProductOptionActivity.this.mOptionView.startAnimation(localTranslateAnimation);
    }

    private void centerToBottom(type type, final boolean isAni, final boolean isFinish, long speed)
    {
        TranslateAnimation localTranslateAnimation = new TranslateAnimation(0.0F, 0.0F, 0.0F, this.mScreenHeight);
        switch (type)
        {
            case optin_list:
                //localTranslateAnimation.setInterpolator(new OutInterpolator(0.1F));
                localTranslateAnimation.setInterpolator(new OvershootInterpolator(1F));
                localTranslateAnimation.setDuration(speed);
                localTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation paramAnonymousAnimation) {
                        mOptionView.setVisibility(View.GONE);
                        if(isFinish)
                        {
                            finish();
                        }
                    }
                    public void onAnimationRepeat(Animation paramAnonymousAnimation) {
                    }

                    public void onAnimationStart(Animation paramAnonymousAnimation) {
                        mOptionView.setVisibility(View.VISIBLE);
                        if(isFinish) {
                            Animation animation = AnimationUtils.loadAnimation(ProductOptionActivity.this, R.anim.fade_ban_out);
                            mRootView.startAnimation(animation);
                        }
                    }
                });
                ProductOptionActivity.this.mOptionView.startAnimation(localTranslateAnimation);
                break;
            case select_list:
                //localTranslateAnimation.setInterpolator(new OutInterpolator(0.1F));
                localTranslateAnimation.setInterpolator(new OvershootInterpolator(1F));
                localTranslateAnimation.setDuration(speed);
                localTranslateAnimation.setStartOffset(100L);
                localTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationEnd(Animation paramAnonymousAnimation) {
                        mSelectView.setVisibility(View.GONE);

                        if(isAni)
                        {
                            mIsAni = false;
                        }
                        if(isFinish)
                        {
                            finish();
                        }
                    }

                    public void onAnimationRepeat(Animation paramAnonymousAnimation) {
                    }

                    public void onAnimationStart(Animation paramAnonymousAnimation) {
                        mSelectView.setVisibility(View.VISIBLE);
                        if(isFinish) {
                            Animation animation = AnimationUtils.loadAnimation(ProductOptionActivity.this, R.anim.fade_ban_out);
                            mRootView.startAnimation(animation);
                        }
                    }
                });
                ProductOptionActivity.this.mSelectView.startAnimation(localTranslateAnimation);
                break;
        }
    }

    public class SelectListAdapter extends ArrayAdapter<ProductJson> {

        int resource;

        public SelectListAdapter(Context context, int resource,List<ProductJson> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }

            TextView name = ViewHolder.get(convertView, R.id.Name);
            TextView count = ViewHolder.get(convertView, R.id.Count);
            TextView price = ViewHolder.get(convertView, R.id.Price);
            TextView oriPrice = ViewHolder.get(convertView, R.id.OriPrice);


            ImageButton minusBtn = ViewHolder.get(convertView,R.id.MInus);
            ImageButton plusBtn = ViewHolder.get(convertView,R.id.Plus);
            ImageButton dellBtn = ViewHolder.get(convertView,R.id.DellBtn);

            final ProductJson item = getItem(position);

            if (item != null) {
                name.setText(item.name);
                count.setText(item.count);

                int item_price = (int)Double.parseDouble((item.buyprice));
                int total_price = item_price * Integer.valueOf(item.count);

                price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(total_price)) + " 원");

                if (mOptionArrayList.size() > 1) {
                    dellBtn.setVisibility(View.VISIBLE);
                } else {
                    dellBtn.setVisibility(View.GONE);
                }

                if (Integer.valueOf(item.count) <= 1) {
                    minusBtn.setEnabled(false);
                } else {
                    minusBtn.setEnabled(true);
                }

                if (Integer.valueOf(item.stock) <= 0) {
                    plusBtn.setEnabled(false);
                } else {
                    plusBtn.setEnabled(true);
                }


                minusBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Minus", mSelectArrayList.get(position).name);

                        int count = Integer.valueOf(mSelectArrayList.get(position).count);

                        if (count <= 1) {
                            mToast.makeText(mContext, "상품 갯수를 뺄수 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        count = count - 1;
                        mSelectArrayList.get(position).count = String.valueOf(count);
                        //setProductMiniCart(type.select_list);
                        notifyDataSetChanged();
                    }
                });

                plusBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Plus", mSelectArrayList.get(position).name);

                        int stock = Integer.valueOf(mSelectArrayList.get(position).stock);
                        int count = Integer.valueOf(mSelectArrayList.get(position).count);

                        if (mSelectArrayList.get(position).soldout.equals("T") || stock == 0) {
                            mToast.makeText(mContext, "품절된 상품입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (count >= stock) {
                            mToast.makeText(mContext, "재고 수량을 초과하였습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        count = count + 1;
                        mSelectArrayList.get(position).count = String.valueOf(count);
                        //setProductMiniCart(type.select_list);
                        notifyDataSetChanged();
                    }
                });

                dellBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_OPTION, "Click_Delete", mSelectArrayList.get(position).name);
                        mSelectArrayList.remove(position);
                        //setProductMiniCart(type.select_list);
                        notifyDataSetChanged();
                    }
                });
            }

            return convertView;
        }
    }

    public class OptionListAdapter extends ArrayAdapter<ProductJson> {

        int resource;

        public OptionListAdapter(Context context, int resource,List<ProductJson> objects) {
            super(context, resource, objects);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(resource, null);
            }

            TextView name = ViewHolder.get(convertView, R.id.Name);
            TextView price = ViewHolder.get(convertView, R.id.Price);
            TextView oriPrice = ViewHolder.get(convertView, R.id.OriPrice);
            TextView textPointAdd = ViewHolder.get(convertView, R.id.TextPointAdd);
            TextView selected = ViewHolder.get(convertView, R.id.IsSelectd);
            View line = ViewHolder.get(convertView, R.id.Line);


            final ProductJson item = getItem(position);

            if(getCount()-1 == position)
            {
                line.setVisibility(View.INVISIBLE);
            }
            else
            {
                line.setVisibility(View.VISIBLE);
            }

            if (item != null) {
                name.setText(item.name);

                if(item.soldout.equals("T") || Integer.valueOf(item.stock) == 0)
                {
                    name.setEnabled(false);
                    price.setEnabled(false);

                    price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int)Double.parseDouble(item.buyprice))+mContext.getResources().getString(R.string.korea_won) +" / 품절");
                }
                else
                {
                    name.setEnabled(true);
                    price.setEnabled(true);

                    int itemPrice = (int)Double.parseDouble(item.price);
                    int itemBuyPrice = (int) Double.parseDouble(item.buyprice);

                    if(itemPrice > itemBuyPrice)
                    {
                        price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemBuyPrice)+mContext.getResources().getString(R.string.korea_won));
                        oriPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemPrice) + mContext.getResources().getString(R.string.korea_won));
                        oriPrice.setVisibility(View.VISIBLE);
                        oriPrice.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    else
                    {
                        price.setText("");
                        oriPrice.setText("");
                        price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemBuyPrice)+mContext.getResources().getString(R.string.korea_won));
                        oriPrice.setVisibility(View.GONE);
                        oriPrice.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
                    }

                    if(Integer.parseInt(item.provide_point) > 0) {
                        textPointAdd.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int)Double.parseDouble(item.provide_point)) + "P 적립");
                    } else {
                        textPointAdd.setText("");
                    }
                    //price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit((int)Double.parseDouble(item.buyprice))+mContext.getResources().getString(R.string.korea_won));
                }

                selected.setVisibility(View.GONE);
                for(ProductJson json : mSelectArrayList)
                {
                    if(json.idx.equals(item.idx))
                    {
                        selected.setVisibility(View.VISIBLE);
                        break;
                    }
                }
            }
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        if(mIsAni) return;

        if(mNowShowSelectView)
        {
            mIsAni = true;
            centerToBottom(type.select_list,true,true,300L);
            return;
        }
        else
        {
            mIsAni = true;
            centerToBottom(type.optin_list,true,true,300L);
            return;
        }
    }
}

package com.leadplatform.kfarmers.view.product;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.common.WrappingSlidingDrawer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015-01-27.
 */
public class ProductMiniCartView {

    MiniCartListener mMiniCartListener;

    public interface MiniCartListener {
        public void onCartBtn(ArrayList<ProductJson> productJsons);
        public void onBuyBtn(ArrayList<ProductJson> productJsons);
    }

    enum type
    {
        init_list,
        select_list,
        optin_list
    }

    private LayoutInflater mInflater;

    private Context mContext;
    private WrappingSlidingDrawer mWrappingSlidingDrawer;
    private ArrayList<ProductJson> mOptionArrayList;
    private ArrayList<ProductJson> mSelectArrayList;

    //private View mTotalPriceView;
    //private TextView mPriceText;
    private Button mCartBtn;
    private Button mBuyBtn;

    private View mBinLayout;

    private View mSelectView;
    private ListView mSelectViewList;
    private TextView mSelectViewBoxText;

    private SelectListAdapter mSelectListAdapter;

    private View mOptionView;
    private ListView mOptionViewList;
    private TextView mOptionViewBoxText;

    private OptionListAdapter mOptionListAdapter;

    public ProductMiniCartView(WrappingSlidingDrawer wrappingSlidingDrawer, Context context) {
        mWrappingSlidingDrawer = wrappingSlidingDrawer;
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public boolean isShow()
    {
        return mWrappingSlidingDrawer.isOpened();
    }

    public void setData(ArrayList<ProductJson> arrayList)
    {
        try
        {
            ((FrameLayout) mWrappingSlidingDrawer.getContent()).removeAllViews();

            if(mOptionArrayList != null)
                mOptionArrayList.clear();
            if(mSelectArrayList != null)
                mSelectArrayList.clear();
            mSelectArrayList = null;
            mOptionArrayList = null;
        }
        catch (Exception e){}

        mOptionArrayList = arrayList;
        mSelectArrayList = new ArrayList<>();

        SetProductInitView();
    }

    public void setBtnListener(MiniCartListener miniCartListener)
    {
        mMiniCartListener = miniCartListener;
    }

    public void SetProductInitView()
    {
        setSelectListView();
        setOptionListView();

        setProductMiniCart(type.init_list);
    }

    public void setSelectListView()
    {
        mSelectView = mInflater.inflate(R.layout.item_minicart_list,null);
        mSelectViewList = (ListView)mSelectView.findViewById(R.id.ListView);
        mSelectViewBoxText = (TextView)mSelectView.findViewById(R.id.Name);
        mSelectViewList.setBackgroundResource(R.drawable.w_minicart_list_bg);

        //mPriceText = (TextView) mSelectView.findViewById(R.id.PriceText);
        //mTotalPriceView = mSelectView.findViewById(R.id.TotalPriceLayout);
        mCartBtn = (Button) mSelectView.findViewById(R.id.CartBtn);
        mBuyBtn = (Button) mSelectView.findViewById(R.id.BuyBtn);

        mBinLayout = mSelectView.findViewById(R.id.BinLayout);

        mSelectListAdapter = new SelectListAdapter(mContext,R.layout.item_minicart_select_cell,mSelectArrayList);
        mSelectViewList.setAdapter(mSelectListAdapter);

        mCartBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                mMiniCartListener.onCartBtn(mSelectArrayList);
                hideView();
            }
        });

        mBuyBtn.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                mMiniCartListener.onBuyBtn(mSelectArrayList);
                hideView();
            }
        });

        mSelectViewBoxText.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                if(mOptionArrayList.size()>1)
                {
                    setProductMiniCart(type.optin_list);
                }
            }
        });
        ((FrameLayout) mWrappingSlidingDrawer.getContent()).addView(mSelectView);
    }
    public void setOptionListView()
    {
        mOptionView = mInflater.inflate(R.layout.item_minicart_list,null);
        mOptionViewList = (ListView)mOptionView.findViewById(R.id.ListView);
        mOptionViewBoxText = (TextView)mOptionView.findViewById(R.id.Name);

        mOptionViewList.setBackgroundResource(R.drawable.w_minicart_list_bg);
        mOptionView.findViewById(R.id.Footer).setVisibility(View.GONE);

        mOptionListAdapter = new OptionListAdapter(mContext,R.layout.item_minicart_option_list_cell,mOptionArrayList);
        mOptionViewList.setAdapter(mOptionListAdapter);

        mOptionViewBoxText.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                setProductMiniCart(type.select_list);
            }
        });

        mOptionViewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ProductJson optionJson = mOptionArrayList.get(position);

                if(optionJson.soldout.equals("T") || Integer.valueOf(optionJson.stock) == 0)
                {
                    Toast.makeText(mContext,"품절된 상품입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }

                for(ProductJson json : mSelectArrayList)
                {
                    if(json.idx.equals(optionJson.idx))
                    {
                        Toast.makeText(mContext, "선택된 옵션입니다.", Toast.LENGTH_SHORT).show();
                        setProductMiniCart(type.select_list);
                        return;
                    }
                }

                optionJson.count = "1";
                mSelectArrayList.add(optionJson);

                setProductMiniCart(type.select_list);
            }
        });

        ((FrameLayout) mWrappingSlidingDrawer.getContent()).addView(mOptionView);
    }

   private void setTotalPriceView()
   {
       if(mSelectArrayList.size()>0)
       {
           mCartBtn.setEnabled(true);
           mBuyBtn.setEnabled(true);
           mBinLayout.setVisibility(View.GONE);

           /*mTotalPriceView.setVisibility(View.VISIBLE);
           int totalPirce = 0;
           for(ProductJson optionJson : mSelectArrayList)
           {
                totalPirce = totalPirce + ((int)Double.parseDouble(optionJson.buyprice))*((int)Double.parseDouble(optionJson.count));
           }
           mPriceText.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalPirce))+mContext.getResources().getString(R.string.korea_won));*/
       }
       else {

           /*mPriceText.setText("0 원");
           mTotalPriceView.setVisibility(View.GONE);*/

           mCartBtn.setEnabled(false);
           mBuyBtn.setEnabled(false);
           mBinLayout.setVisibility(View.VISIBLE);
       }

   }

    public void setProductMiniCart(type num) {

        if (num == type.init_list) {

            setTotalPriceView();

            mOptionView.setVisibility(View.GONE);
            mSelectView.setVisibility(View.VISIBLE);

            if(mOptionArrayList.size()>1)
            {
                mSelectViewBoxText.setVisibility(View.VISIBLE);
            }
            else
            {
                ProductJson optionJson = mOptionArrayList.get(0);

                if(optionJson.soldout.equals("T") || Integer.valueOf(optionJson.stock) == 0)
                {
                    mSelectViewBoxText.setVisibility(View.VISIBLE);
                    mSelectViewBoxText.setText("품절된 상품입니다.");
                }
                else
                {
                    mSelectViewBoxText.setVisibility(View.GONE);
                    optionJson.count = "1";
                    mSelectArrayList.add(optionJson);
                }
                setProductMiniCart(type.select_list);
            }

            mSelectListAdapter.notifyDataSetChanged();
            mOptionListAdapter.notifyDataSetChanged();
        }

        else if (num == type.select_list) {

            setTotalPriceView();

            mSelectListAdapter.notifyDataSetChanged();

            mOptionView.setVisibility(View.GONE);
            mSelectView.setVisibility(View.VISIBLE);

        } else {

            mOptionListAdapter.notifyDataSetChanged();

            mBinLayout.setVisibility(View.GONE);
            mSelectView.setVisibility(View.GONE);
            mOptionView.setVisibility(View.VISIBLE);

        }

    }

    public void hideHandleView() {
        mWrappingSlidingDrawer.hideHandle();
    }

    public void hideView() {
        mWrappingSlidingDrawer.animateClose();
    }

    public void showVew() {
        mWrappingSlidingDrawer.animateOpen();
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
                convertView = mInflater.inflate(resource, null);
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
                        int count = Integer.valueOf(mSelectArrayList.get(position).count);

                        if (count <= 1) {
                            Toast.makeText(mContext, "상품 갯수를 뺄수 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        count = count - 1;
                        mSelectArrayList.get(position).count = String.valueOf(count);
                        setProductMiniCart(type.select_list);
                    }
                });

                plusBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {

                        int stock = Integer.valueOf(mSelectArrayList.get(position).stock);
                        int count = Integer.valueOf(mSelectArrayList.get(position).count);

                        if (mSelectArrayList.get(position).soldout.equals("T") || stock == 0) {
                            Toast.makeText(mContext, "품절된 상품입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (count >= stock) {
                            Toast.makeText(mContext, "재고 수량을 초과하였습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        count = count + 1;
                        mSelectArrayList.get(position).count = String.valueOf(count);
                        setProductMiniCart(type.select_list);
                    }
                });

                dellBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        mSelectArrayList.remove(position);
                        setProductMiniCart(type.select_list);
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
                convertView = mInflater.inflate(resource, null);
            }

            TextView name = ViewHolder.get(convertView, R.id.Name);
            TextView price = ViewHolder.get(convertView, R.id.Price);
            TextView oriPrice = ViewHolder.get(convertView, R.id.OriPrice);
            TextView selected = ViewHolder.get(convertView, R.id.IsSelectd);


            final ProductJson item = getItem(position);

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


}

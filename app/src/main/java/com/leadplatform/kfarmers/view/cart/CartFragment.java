package com.leadplatform.kfarmers.view.cart;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.leadplatform.kfarmers.model.json.snipe.CartItemJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseListFragment;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.main.MainActivity;
import com.leadplatform.kfarmers.view.payment.PaymentActivity;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class CartFragment extends BaseListFragment
{
	public static final String TAG = "CartFragment";

    View mCartView,mCartViewEmpty;
    View mHeaderView;
    View mFooterView;
    TextView mTextFooterProduct,mTextFooterDelivery,mTextFooterTotal,mTextFooterPointTotal;
    Button mBtnDel,mBtnBuy,mProductShow;
    CheckBox mCheckAll;

    private ArrayList<CartItemJson> mCartArrayList;
    private CartListAdapter mCartListAdapter;

    private ArrayList<String> mSelectedItem;

	public static CartFragment newInstance() {
		final CartFragment f = new CartFragment();
		final Bundle args = new Bundle();
		f.setArguments(args);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_CART, null);
	}


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_cart, container,false);

        mCartView = v.findViewById(R.id.CartView);
        mCartViewEmpty = v.findViewById(R.id.CartViewEmpty);

        mBtnDel = (Button) v.findViewById(R.id.DelBtn);
        mBtnBuy = (Button) v.findViewById(R.id.BuyBtn);
        mProductShow = (Button) v.findViewById(R.id.ProductShow);

        mProductShow.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                ((CartActivity)getActivity()).runMainActivity(MainActivity.MainTab.MARKET);
                getActivity().finish();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Recommend", null);
            }
        });

        mBtnBuy.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Buy", null);
                updateCartData();
            }
        });

        mBtnDel.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {

                KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_SelectDelete", null);

                JSONArray jsonArray = new JSONArray();
                JSONArray removeArray = new JSONArray();

                int i = 0;
                for (CartItemJson cartItemJson : mCartArrayList) {

                    if(cartItemJson.isCheck)
                    {
                        JSONObject itemObject = new JSONObject();
                        itemObject.put("idx", cartItemJson.item_idx);
                        // option
                        JSONArray optionJsonArray = new JSONArray();
                        itemObject.put("option",optionJsonArray);
                        jsonArray.add(itemObject);

                        //삭제 데이터
                        JSONObject removeObject = new JSONObject();
                        removeObject.put("idx", i);
                        // option
                        JSONArray removeJsonArray = new JSONArray();
                        removeObject.put("option",removeJsonArray);
                        removeArray.add(removeObject);

                    }

                    i++;
                }
                if(jsonArray.size()>0) {
                    deleteCartData(jsonArray,removeArray);
                }
            }
        });

        setListAddView(inflater);

        return v;
	}

    public void deleteCartData(JSONArray jsonArray , final JSONArray removeData)
    {
        try {
            UserDb user = DbController.queryCurrentUser(getActivity());
            if (user == null) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            SnipeApiController.deleteCartItem(user.getUserID(),jsonArray,new SnipeResponseListener(getActivity()) {
                @Override
                public void onSuccess(int Code, String content, String error)
                {
                    switch (Code) {
                        case 200:

                            for(int i=0; i< removeData.size();i++)
                            {
                                JSONObject delJson = (JSONObject) removeData.get(i);
                                JSONArray delArray = (JSONArray) delJson.get("option");

                                int mPos = (Integer)delJson.get("idx");

                                if(delArray.size()>0)
                                {
                                    int sPos = (Integer) delArray.get(0);
                                    mCartArrayList.get(mPos).option.remove(sPos);
                                }
                                else
                                {
                                    mCartArrayList.remove(mPos);
                                }
                            }
                            mCartListAdapter.notifyDataSetChanged();
                            setFooterData();

                            if(mCartArrayList.size()>0)
                            {
                                mCartView.setVisibility(View.VISIBLE);
                                mCartViewEmpty.setVisibility(View.GONE);
                            }
                            else
                            {
                                mCartView.setVisibility(View.GONE);
                                mCartViewEmpty.setVisibility(View.VISIBLE);
                            }

                            break;
                        default:
                            getListCart();
                            break;
                    }
                    //getListCart();
                }
            });
        }catch (Exception e){
            getListCart();
        }
    }
    public void updateCartData()
    {
        mSelectedItem = new ArrayList<>();

        try {
            UserDb user = DbController.queryCurrentUser(getActivity());
            if (user == null)
            {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                return;
            }
            else {

                String id = user.getUserID();

                // item
                JSONArray itemJsonArray = new JSONArray();
                for (CartItemJson cartItemJson : mCartArrayList) {

                    if(cartItemJson.isCheck)
                    {
                        JSONObject itemObject = new JSONObject();
                        itemObject.put("idx", cartItemJson.item_idx);
                        mSelectedItem.add(cartItemJson.item_idx);

                        // option
                        JSONArray optionJsonArray = new JSONArray();
                        for (CartItemJson subItemJson : cartItemJson.option)
                        {
                            JSONObject optionObject = new JSONObject();
                            optionObject.put("idx", subItemJson.option_idx);
                            optionObject.put("cnt", subItemJson.cnt);
                            optionObject.put("price", subItemJson.buyprice);
                            optionObject.put("point", subItemJson.provide_point);
                            optionJsonArray.add(optionObject);
                        }
                        itemObject.put("option",optionJsonArray);
                        itemJsonArray.add(itemObject);
                    }
                }

                if(itemJsonArray.size()<=0)
                {
                    return;
                }

                SnipeApiController.updateCartItem(id, "F", itemJsonArray, new SnipeResponseListener(getActivity()) {
                    @Override
                    public void onSuccess(int Code, String content, String error) {

                        switch (Code)
                        {
                            case 200:
                            {
                                Intent intent = new Intent(getActivity(), PaymentActivity.class);
                                intent.putExtra("idx", mSelectedItem);
                                intent.putExtra("direct", "F");

                                startActivityForResult(intent,PaymentActivity.PAYMENT_CHECK);
                                break;
                            }
                        }
                    }
                });
            }
        }
        catch (Exception e)
        {}
    }

    public void setListAddView(LayoutInflater inflater)
    {
        mHeaderView = inflater.inflate(R.layout.item_cart_header,null);
        mFooterView = inflater.inflate(R.layout.item_cart_footer,null);

        mCheckAll = (CheckBox) mHeaderView.findViewById(R.id.AllCartCheckBox);
        mCheckAll.setChecked(true);

        mCheckAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    for (CartItemJson itemJson : mCartArrayList) {
                        itemJson.isCheck = true;
                    }
                } else {
                    for (CartItemJson itemJson : mCartArrayList) {
                        itemJson.isCheck = false;
                    }
                }

                mCartListAdapter.notifyDataSetChanged();
                setFooterData();
            }
        });

        mTextFooterProduct = (TextView) mFooterView.findViewById(R.id.TotalProductPrice);
        mTextFooterDelivery = (TextView) mFooterView.findViewById(R.id.TotalProductDelivery);
        mTextFooterTotal = (TextView) mFooterView.findViewById(R.id.TotalPrice);
        mTextFooterPointTotal = (TextView) mFooterView.findViewById(R.id.TotalPoint);
    }

    public void setFooterData()
    {
        int delivery = 0;
        int product = 0;
        int point = 0;

        for(CartItemJson mainItem : mCartArrayList)
        {
            if(mainItem.isCheck) {
                delivery += ((int)Double.parseDouble(mainItem.delivery_price));
                for (CartItemJson subItem : mainItem.option) {
                    product += Integer.valueOf(subItem.cnt) * ((int) Double.parseDouble(subItem.buyprice));
                    point += Integer.valueOf(subItem.cnt) * ((int) Double.parseDouble(subItem.provide_point));
                }
            }
        }

        mTextFooterProduct.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(product)) + " 원");
        mTextFooterDelivery.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(delivery)) + " 원");
        mTextFooterTotal.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(delivery+product)) + " 원");
        mTextFooterPointTotal.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(point)) + " P");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mCartListAdapter == null) {

            getListView().setDivider(null);
            getListView().setDividerHeight(0);

            getListView().addHeaderView(mHeaderView);
            getListView().addFooterView(mFooterView);

            mHeaderView.setVisibility(View.GONE);
            mFooterView.setVisibility(View.GONE);

            mCartArrayList = new ArrayList<>();
            mCartListAdapter = new CartListAdapter();

	        setListAdapter(mCartListAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCartArrayList.clear();
        mCartListAdapter.notifyDataSetChanged();
        mHeaderView.setVisibility(View.GONE);
        mFooterView.setVisibility(View.GONE);
        getListCart();
    }

    private void getListCart() {

        UserDb user = DbController.queryCurrentUser(getActivity());
        if (user == null)
        {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            return;
        }

        SnipeApiController.getCartItem(user.getUserID(), new SnipeResponseListener(getActivity()) {
            @Override
            public void onSuccess(int Code, String content, String error) {
                try {
                    switch (Code) {
                        case 200:

                            JsonNode root = JsonUtil.parseTree(content);

                            mSelectedItem = new ArrayList<>();
                            mCartArrayList = (ArrayList<CartItemJson>) JsonUtil.jsonToArrayObject(root.path("item"), CartItemJson.class);
                            mCartListAdapter.notifyDataSetChanged();
                            setFooterData();
                            mHeaderView.setVisibility(View.VISIBLE);
                            mFooterView.setVisibility(View.VISIBLE);

                            if(mCartArrayList.size()>0)
                            {
                                mCartView.setVisibility(View.VISIBLE);
                                mCartViewEmpty.setVisibility(View.GONE);
                            }
                            else
                            {
                                mCartView.setVisibility(View.GONE);
                                mCartViewEmpty.setVisibility(View.VISIBLE);
                            }
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    mCartView.setVisibility(View.GONE);
                    mCartViewEmpty.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public class CartListAdapter extends BaseAdapter
    {
        private LayoutInflater inflater;
        private ImageLoader imageLoader;
        private DisplayImageOptions optionsProduct;

        int mTag = R.string.mPos;
        int sTag = R.string.sPos;

        public CartListAdapter()
        {
            imageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
            optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).build();
        }

        @Override
        public int getCount() {
            return mCartArrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return mCartArrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            if (convertView == null) {
                inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_cart, null);
            }

            ImageButton dellBtn = ViewHolder.get(convertView,R.id.DellBtn);
            CheckBox checkBox = ViewHolder.get(convertView,R.id.IsCheck);
            ImageView img = ViewHolder.get(convertView, R.id.ProductImg);
            TextView name = ViewHolder.get(convertView, R.id.ProductName);
            TextView info = ViewHolder.get(convertView, R.id.ProductInfo);

            TextView delivery = ViewHolder.get(convertView, R.id.DeliveryPrice);
            TextView total = ViewHolder.get(convertView, R.id.TotalPrice);

            LinearLayout optionView = ViewHolder.get(convertView, R.id.OptionView);

            RelativeLayout ItemBox = ViewHolder.get(convertView, R.id.ItemBox);


            optionView.removeAllViews();

            CartItemJson item = mCartArrayList.get(position);
            if (item != null) {

                ItemBox.setTag(position);
                ItemBox.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(View v) {
                        int pos = (Integer) v.getTag();
                        Intent intent = new Intent(getActivity(), ProductActivity.class);
                        intent.putExtra("productIndex", mCartArrayList.get(pos).item_idx);
                        startActivity(intent);
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Item-Move", mCartArrayList.get(pos).name);
                    }
                });

                img.setImageDrawable(null);
                imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, img, optionsProduct);
                name.setText(item.name);

                int deliveryPrice = ((int)Double.parseDouble(item.delivery_price));
                delivery.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(deliveryPrice)) + " 원");

                info.setVisibility(View.GONE);
                if(!item.display.equals("T")) {
                    info.setText("해당 상품은 현재 구매 하실수 없는 상품입니다.");
                    info.setVisibility(View.VISIBLE);
                }

                dellBtn.setTag(position);
                dellBtn.setTag(R.integer.tag_id,item.name);
                dellBtn.setOnClickListener(new ViewOnClickListener() {
                    @Override
                    public void viewOnClick(final View v) {

                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Item-Delete", (String) v.getTag(R.integer.tag_id));

                        final int pos = (int) v.getTag();

                        UiDialog.showDialog(getActivity(),"삭제하시겠습니까?",R.string.dialog_ok,R.string.dialog_cancel,new CustomDialogListener() {
                            @Override
                            public void onDialog(int type) {

                                if(type == UiDialog.DIALOG_POSITIVE_LISTENER)
                                {
                                    JSONArray jsonArray = new JSONArray();
                                    JSONArray removeArray = new JSONArray();

                                    JSONObject itemObject = new JSONObject();
                                    itemObject.put("idx", mCartArrayList.get(pos).item_idx);
                                    // option
                                    JSONArray optionJsonArray = new JSONArray();
                                    itemObject.put("option", optionJsonArray);
                                    jsonArray.add(itemObject);

                                    //삭제 데이터
                                    JSONObject removeObject = new JSONObject();
                                    removeObject.put("idx", pos);
                                    // option
                                    JSONArray removeJsonArray = new JSONArray();
                                    removeObject.put("option", removeJsonArray);
                                    removeArray.add(removeObject);

                                    if(jsonArray.size()>0) {
                                        deleteCartData(jsonArray, removeArray);
                                    }
                                }
                            }
                        });
                    }
                });

                checkBox.setTag(position);
                checkBox.setChecked(item.isCheck);

                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        int pos = (int) buttonView.getTag();
                        if(isChecked)
                        {
                            mCartArrayList.get(pos).isCheck = true;
                        }
                        else
                        {
                            mCartArrayList.get(pos).isCheck = false;
                        }
                        mCartListAdapter.notifyDataSetChanged();
                        setFooterData();
                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Item-ChangeBuySelected", mCartArrayList.get(pos).name);
                    }
                });

                int sPos = 0;
                int totalPirce = 0;

                for (CartItemJson opCartItemJson : item.option)
                {
                    View subView = inflater.inflate(R.layout.item_minicart_select_cell, null);

                    TextView opNotic = ViewHolder.get(subView, R.id.NoticText);
                    TextView opName = ViewHolder.get(subView, R.id.Name);
                    TextView opCount = ViewHolder.get(subView, R.id.Count);
                    TextView opPrice = ViewHolder.get(subView, R.id.Price);

                    ImageButton opMinusBtn = ViewHolder.get(subView,R.id.MInus);
                    ImageButton opPlusBtn = ViewHolder.get(subView,R.id.Plus);
                    ImageButton opDellBtn = ViewHolder.get(subView,R.id.DellBtn);


                    opName.setText(opCartItemJson.option_name);
                    opCount.setText(opCartItemJson.cnt);

                    opNotic.setVisibility(View.GONE);

                    int item_price = ((int)Double.parseDouble(opCartItemJson.buyprice));
                    int item_total_price = item_price * Integer.valueOf(opCartItemJson.cnt);
                    totalPirce += item_total_price;

                    opPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(item_total_price)) + " 원");

                    opMinusBtn.setTag(mTag,position);
                    opMinusBtn.setTag(sTag,sPos);

                    opPlusBtn.setTag(mTag,position);
                    opPlusBtn.setTag(sTag,sPos);

                    opDellBtn.setTag(mTag,position);
                    opDellBtn.setTag(sTag,sPos);

                    if(item.option.size() <= 1)
                    {
                        opDellBtn.setVisibility(View.GONE);
                    }

                    if(!opCartItemJson.display.equals("T")) {
                        opNotic.setText("해당 옵션은 현재 구매 하실수 없는 상품입니다.");
                        opNotic.setVisibility(View.VISIBLE);

                        opMinusBtn.setEnabled(false);
                        opPlusBtn.setEnabled(false);
                    }
                    else if(!opCartItemJson.option_use_flag.equals("T"))
                    {
                        opNotic.setVisibility(View.VISIBLE);
                        opNotic.setText("해당 옵션은 현재 구매 하실수 없는 상품입니다.");

                        opMinusBtn.setEnabled(false);
                        opPlusBtn.setEnabled(false);
                    }
                    else if(opCartItemJson.soldout.equals("T") || Integer.valueOf(opCartItemJson.stock) <= 0)
                    {
                        opNotic.setVisibility(View.VISIBLE);
                        opNotic.setText("품절된 상품입니다.");

                        opMinusBtn.setEnabled(false);
                        opPlusBtn.setEnabled(false);

                    }
                    else if((int)Double.parseDouble(opCartItemJson.buyprice) != (int)Double.parseDouble(opCartItemJson.current_buyprice))
                    {
                        opNotic.setVisibility(View.VISIBLE);
                        opNotic.setText("해당 옵션은 상품 가격이 변동 되었습니다.\n삭제후 재구매 해주세요.");

                        opMinusBtn.setEnabled(false);
                        opPlusBtn.setEnabled(false);
                    }
                    else
                    {
                        if(Integer.valueOf(opCartItemJson.stock) < Integer.valueOf(opCartItemJson.cnt))
                        {
                            int sellCount = Integer.valueOf(opCartItemJson.stock);

                            opNotic.setVisibility(View.VISIBLE);
                            opNotic.setText("해당 상품은 "+ sellCount + "개 이하로 주문 가능합니다.");

                            opPlusBtn.setEnabled(false);
                        }
                        else
                        {
                            opNotic.setVisibility(View.GONE);

                            opMinusBtn.setEnabled(true);
                            opPlusBtn.setEnabled(true);

                            if (Integer.valueOf(opCartItemJson.cnt) <= 1) {
                                opMinusBtn.setEnabled(false);
                            } else {
                                opMinusBtn.setEnabled(true);
                            }
                        }

                        opMinusBtn.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {

                                int mPos = (int) v.getTag(mTag);
                                int sPos = (int) v.getTag(sTag);

                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Item-OptionMinus", mCartArrayList.get(mPos).option.get(sPos).name);

                                int count = Integer.valueOf(mCartArrayList.get(mPos).option.get(sPos).cnt);

                                if (count <= 1) {
                                    Toast.makeText(getActivity(), "선택한 상품의 수량이 최소치 입니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                count = count - 1;
                                mCartArrayList.get(mPos).option.get(sPos).cnt = String.valueOf(count);
                                mCartListAdapter.notifyDataSetChanged();
                                setFooterData();
                            }
                        });

                        opPlusBtn.setOnClickListener(new ViewOnClickListener() {
                            @Override
                            public void viewOnClick(View v) {

                                int mPos = (int) v.getTag(mTag);
                                int sPos = (int) v.getTag(sTag);

                                KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Item-OptionPlus", mCartArrayList.get(mPos).option.get(sPos).name);

                                int stock = Integer.valueOf(mCartArrayList.get(mPos).option.get(sPos).stock);
                                int count = Integer.valueOf(mCartArrayList.get(mPos).option.get(sPos).cnt);
                                String soldOut = mCartArrayList.get(mPos).option.get(sPos).soldout;

                                if (soldOut.equals("T") || stock == 0) {
                                    Toast.makeText(getActivity(), "품절된 상품입니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (count >= stock) {
                                    Toast.makeText(getActivity(), "재고 수량을 초과하였습니다.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                count = count + 1;
                                mCartArrayList.get(mPos).option.get(sPos).cnt = String.valueOf(count);
                                mCartListAdapter.notifyDataSetChanged();
                                setFooterData();
                            }
                        });
                    }

                    opDellBtn.setOnClickListener(new ViewOnClickListener() {
                        @Override
                        public void viewOnClick(final View v) {

                            final int mPos = (int) v.getTag(mTag);
                            final int sPos = (int) v.getTag(sTag);

                            UiDialog.showDialog(getActivity(),"삭제하시겠습니까?",R.string.dialog_ok,R.string.dialog_cancel,new CustomDialogListener() {
                                @Override
                                public void onDialog(int type) {

                                    if(type == UiDialog.DIALOG_POSITIVE_LISTENER)
                                    {
                                        KfarmersAnalytics.onClick(KfarmersAnalytics.S_CART, "Click_Item-OptionDelete", mCartArrayList.get(mPos).option.get(sPos).name);

                                        JSONArray jsonArray = new JSONArray();
                                        JSONArray removeArray = new JSONArray();

                                        JSONObject itemObject = new JSONObject();
                                        itemObject.put("idx", mCartArrayList.get(mPos).item_idx);
                                        // option
                                        JSONArray optionJsonArray = new JSONArray();
                                        optionJsonArray.add(mCartArrayList.get(mPos).option.get(sPos).option_idx);
                                        itemObject.put("option",optionJsonArray);
                                        jsonArray.add(itemObject);

                                        //삭제 데이터
                                        JSONObject removeObject = new JSONObject();
                                        removeObject.put("idx", mPos);
                                        // option
                                        JSONArray removeJsonArray = new JSONArray();
                                        removeJsonArray.add(sPos);
                                        removeObject.put("option",removeJsonArray);
                                        removeArray.add(removeObject);

                                        if(jsonArray.size()>0) {
                                            deleteCartData(jsonArray, removeArray);
                                        }
                                    }
                                }
                            });
                        }
                    });

                    if(sPos >= item.option.size()-1) {
                        subView.findViewById(R.id.BottomLine).setVisibility(View.GONE);
                    }

                    optionView.addView(subView);
                    sPos = sPos + 1;
                }
                total.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(totalPirce)) + " 원");
            }
            return convertView;

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PaymentActivity.PAYMENT_CHECK)
        {
            if(resultCode == PaymentActivity.PAYMENT_CHECK_ITEM)
            {
                getListCart();
            }
            else if(resultCode == PaymentActivity.PAYMENT_CHECK_OPTION)
            {
                getListCart();
                //checkProductItem();
            }
        }
    }
}

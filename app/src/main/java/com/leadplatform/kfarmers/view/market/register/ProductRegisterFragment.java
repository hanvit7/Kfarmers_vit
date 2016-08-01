package com.leadplatform.kfarmers.view.market.register;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

public class ProductRegisterFragment extends BaseFragment {
	public static final String TAG = "ProductRegisterFragment";//ProductRegisterActivity에서 참조

	private LayoutInflater mInflater;
	private ArrayList<View> mOptionList;
	private int mMarginsize;

	private boolean isSanding = false;
	public boolean isModify = false;

	private RelativeLayout mCategoryLayout;
	private TextView mCategoryText;
	private int categoryIndex = 0;

	private String productIdx;
	private ProductJson mProductItemJson;
	private ArrayList<ProductJson> mProductOptionList;

	private LinearLayout mProdcutOptionLayout;
	private EditText mProductName, mProductPrice, mProductDisPrice,
			mProdcutDeliveryPrice, mProductDes, mProductCount;

	private CheckBox mProductSoldOut;

	private Button mSendBtn;

	//private RelativeLayout mViewOptionTitle;
	//private RadioGroup mRadioGroup;

	//private boolean mDeliveryFree = true;

	private final int CONTEXT_MENU_GROUP_ID = 2;
	private String imagePath[] = new String[10];
	private int imageIndex = 0;
	private ImageView imageBtn[] = new ImageView[10];
	private final int imageBtnID[] = { R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5, R.id.image6, R.id.image7, R.id.image8, R.id.image9, R.id.image10 };
	private boolean imageChange[] = new boolean[10];

	public static ProductRegisterFragment newInstance(String idx) {
		final ProductRegisterFragment f = new ProductRegisterFragment();
		
		if(idx != null)
		{
			final Bundle args = new Bundle();
			args.putString("idx", idx);
			f.setArguments(args);
		}
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments() != null) {
			productIdx = getArguments().getString("idx");
			isModify = true;
		}

		KfarmersAnalytics.onScreen(getType(), null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.mInflater = inflater;
		final View v = this.mInflater.inflate(
				R.layout.fragment_product_register, container, false);

		mProdcutOptionLayout = (LinearLayout) v
				.findViewById(R.id.prodcutOption);
		mProductName = (EditText) v.findViewById(R.id.productName);
		mProductPrice = (EditText) v.findViewById(R.id.productPrice);
		mProductDisPrice = (EditText) v.findViewById(R.id.productDisPrice);
		mProdcutDeliveryPrice = (EditText) v
				.findViewById(R.id.prodcutDeliveryPrice);
		mProductDes = (EditText) v.findViewById(R.id.productDes);
		mProductCount = (EditText) v.findViewById(R.id.productCount);

		mProductSoldOut = (CheckBox) v.findViewById(R.id.productSoldOut);

		// 스크롤뷰 안 스크롤 작동 코드
		mProductDes.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v.getId() == R.id.productDes) {
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

		mCategoryLayout = (RelativeLayout) v.findViewById(R.id.categoryLayout);
		mCategoryText = (TextView) v.findViewById(R.id.categoryText);

		//mViewOptionTitle = (RelativeLayout) v.findViewById(R.id.viewOptionTitle);

		mCategoryText.setText(((ProductRegisterActivity) getActivity()).mFarmerCategory.get(0).SubName);

		Button optionAdd = (Button) v.findViewById(R.id.prodcutOptionAdd);
		
		optionAdd.setOnClickListener(new ViewOnClickListener() {

			@Override
			public void viewOnClick(View v) {
				if(mOptionList.size() >= 10)
				{
					UiDialog.showDialog(getSherlockActivity(), "옵션은 10개까지 등록 가능합니다");
				}
				else
				{
					KfarmersAnalytics.onClick(getType(), "Click_Option-Add", null);
					//mViewOptionTitle.setVisibility(View.VISIBLE);
					addOption("");
				}
			}
		});

		mCategoryLayout.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				((ProductRegisterActivity) getActivity()).onCategoryBtnClicked(
						categoryIndex, new OnCloseCategoryDialogListener() {
							@Override
							public void onDialogListSelection(int subMenuType,
															  int position) {
								if (categoryIndex != position) {
									categoryIndex = position;
									mCategoryText.setText(((ProductRegisterActivity) getActivity()).mFarmerCategory.get(position).SubName);
									KfarmersAnalytics.onClick(getType(), "Click_Category", ((ProductRegisterActivity) getActivity()).mFarmerCategory.get(position).SubName);
								}
							}
						});
			}
		});

		mSendBtn = (Button) v.findViewById(R.id.productRegisterBtn);
		mSendBtn.setOnClickListener(new ViewOnClickListener() {

			@Override
			public void viewOnClick(View v) {
				if(productIdx != null && !productIdx.isEmpty())
				{
					productModify();
				}
				else
				{
					productRegister();
				}
			}
		});

		DisplayMetrics dm = getResources().getDisplayMetrics();
		mMarginsize = Math.round(10 * dm.density);

		mOptionList = new ArrayList<View>();

		for (int i = 0; i < imageBtn.length; i++) {
			imageBtn[i] = (ImageView) v.findViewById(imageBtnID[i]);
		}

		/*mRadioGroup = (RadioGroup) v.findViewById(R.id.prodcutDeliveryRadio);
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				if (R.id.prodcutDeliveryRadio1 == checkedId) {
					mProdcutDeliveryPrice.setEnabled(false);
					mDeliveryFree = true;
					mProdcutDeliveryPrice.setText("");
				} else {
					mProdcutDeliveryPrice.setEnabled(true);
					mDeliveryFree = false;
				}
			}
		});

		mProdcutDeliveryPrice.setEnabled(false);
		mProdcutDeliveryPrice
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							mDeliveryFree = false;
							mRadioGroup.check(R.id.prodcutDeliveryRadio2);
						}
					}
				});*/

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		for (int i = 0; i < imageBtn.length; i++) {
			registerForContextMenu(imageBtn[i]);
			imageBtn[i].setOnClickListener(imageViewOnClickListener);
			imageBtn[i].setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					return true;
				}
			});
		}

		if(productIdx != null && !productIdx.isEmpty())
		{
			mSendBtn.setText("상품 수정");
			getProductData();
		}
		else
		{
			mSendBtn.setText("상품 등록");
		}
	}

	public void makeModifyView()
	{
		imageChange = new boolean[]{false,false,false,false,false,false,false,false,false,false};

		for(int i = 1 ; i < ((ProductRegisterActivity) getActivity()).mFarmerCategory.size();i++)
		{
			if(((ProductRegisterActivity) getActivity()).mFarmerCategory.get(i).SubIndex.equals(mProductItemJson.category))
			{
				categoryIndex = i;
				mCategoryText.setText(((ProductRegisterActivity) getActivity()).mFarmerCategory.get(i).SubName);
				break;
			}
		}

		mProductDes.setText(Html.fromHtml(mProductItemJson.contents));
		//mProductDes.setText(mProductItemJson.contents);

		/*if(mProductItemJson.deliver_price_opt.equals("Y"))
		{
			mProdcutDeliveryPrice.setText(mProductItemJson.deliver_price);
			((RadioButton)mRadioGroup.getChildAt(1)).setChecked(true);
		}*/

		DisplayImageOptions optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
				.build();

		if(mProductItemJson.image1 != null && !mProductItemJson.image1.isEmpty())
		{
			imagePath[0] = mProductItemJson.image1;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + imagePath[0], imageBtn[0], optionsProduct);
		}
		if(mProductItemJson.image2 != null && !mProductItemJson.image2.isEmpty())
		{
			imagePath[1] = mProductItemJson.image2;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + imagePath[1], imageBtn[1], optionsProduct);
		}
		if(mProductItemJson.image3 != null && !mProductItemJson.image3.isEmpty())
		{
			imagePath[2] = mProductItemJson.image3;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + imagePath[2], imageBtn[2], optionsProduct);
		}
		if(mProductItemJson.image4 != null && !mProductItemJson.image4.isEmpty())
		{
			imagePath[3] = mProductItemJson.image4;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[3], imageBtn[3], optionsProduct);
		}
		if(mProductItemJson.image5 != null && !mProductItemJson.image5.isEmpty())
		{
			imagePath[4] = mProductItemJson.image5;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[4], imageBtn[4], optionsProduct);
		}
		if(mProductItemJson.image6 != null && !mProductItemJson.image6.isEmpty())
		{
			imagePath[5] = mProductItemJson.image6;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[5], imageBtn[5], optionsProduct);
		}
		if(mProductItemJson.image7 != null && !mProductItemJson.image7.isEmpty())
		{
			imagePath[6] = mProductItemJson.image7;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[6], imageBtn[6], optionsProduct);
		}
		if(mProductItemJson.image8 != null && !mProductItemJson.image8.isEmpty())
		{
			imagePath[7] = mProductItemJson.image8;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[7], imageBtn[7], optionsProduct);
		}
		if(mProductItemJson.image9 != null && !mProductItemJson.image9.isEmpty())
		{
			imagePath[8] = mProductItemJson.image9;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[8], imageBtn[8], optionsProduct);
		}
		if(mProductItemJson.image10 != null && !mProductItemJson.image10.isEmpty())
		{
			imagePath[9] = mProductItemJson.image10;
			((BaseFragmentActivity) getSherlockActivity()).imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG+imagePath[9], imageBtn[9], optionsProduct);
		}

		if(mProductOptionList != null)
		{
			if(mProductOptionList.size()>1)
			{
				//mViewOptionTitle.setVisibility(View.VISIBLE);
			}

			for(int i =0 ; i< mProductOptionList.size();i++)
			{
				if(i == 0) {
					int itemPrice = (int)Double.parseDouble(mProductOptionList.get(i).price);
					int itemDcPrice = (int) Double.parseDouble(mProductOptionList.get(i).dcprice);

					mProductPrice.setText(mProductOptionList.get(i).price);
					mProductDisPrice.setText(mProductOptionList.get(i).dcprice);
					/*mProductPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(itemPrice)));
					mProductDisPrice.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(itemDcPrice)));*/
					mProductName.setText(mProductOptionList.get(i).name);
					mProductCount.setText(mProductOptionList.get(i).stock);

					if(mProductOptionList.get(i).soldout.equals("T")) {
						mProductSoldOut.setChecked(true);
					} else {
						mProductSoldOut.setChecked(false);
					}
				}
				else
				{
					addOption(mProductOptionList.get(i).idx);
					View view = mOptionList.get(i-1);

					EditText name = (EditText) view.findViewById(R.id.optionName);
					EditText price = (EditText) view.findViewById(R.id.optionPrice);
					EditText priceDc = (EditText) view.findViewById(R.id.optionPriceDc);
					Button button = (Button) view.findViewById(R.id.optionDel);
					EditText count = (EditText) view.findViewById(R.id.optionCount);
					CheckBox soldout = (CheckBox) view.findViewById(R.id.optionSoldOut);

					name.setText(mProductOptionList.get(i).name);
					button.setTag(mProductOptionList.get(i).idx);

					price.setText(mProductOptionList.get(i).price);
					priceDc.setText(mProductOptionList.get(i).dcprice);

					count.setText(mProductOptionList.get(i).stock);

					if(mProductOptionList.get(i).soldout.equals("T")) {
						soldout.setChecked(true);
					} else {
						soldout.setChecked(false);
					}

					/*int itemPrice = (int)Double.parseDouble(mProductOptionList.get(i).price);
					int itemDcPrice = (int) Double.parseDouble(mProductOptionList.get(i).dcprice);

					price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(itemPrice)));
					priceDc.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(itemDcPrice)));*/
				}
			}
		}
	}

	public void getProductData()
	{
		JSONArray jsonArray = new JSONArray();
		jsonArray.add(productIdx);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("idx", jsonArray);

		SnipeApiController.getProductItem(jsonObject, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					switch (Code) {
						case 200:
							JsonNode root = JsonUtil.parseTree(content);
							mProductItemJson = (ProductJson) JsonUtil.jsonToObject(root.path("item").toString(), ProductJson.class);
							mProductOptionList = (ArrayList<ProductJson>) JsonUtil.jsonToArrayObject(root.path("option"), ProductJson.class);
							makeModifyView();
							break;
						default:
							UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
			}
		});
	}

	public boolean productCheck() {

		if (categoryIndex == 0) {
			UiDialog.showDialog(getSherlockActivity(),
					R.string.dialog_product_reg_category);
			return false;
		}
		if (CommonUtil.PatternUtil.isEmpty(mProductName.getText().toString()
				.trim())) {
			UiDialog.showDialog(getSherlockActivity(),
					R.string.dialog_product_reg_name);
			return false;
		}
		if (CommonUtil.PatternUtil.isEmpty(mProductPrice.getText().toString()
				.trim())) {
			UiDialog.showDialog(getSherlockActivity(),
					R.string.dialog_product_reg_price);
			return false;
		}
		if (CommonUtil.PatternUtil.isEmpty(mProductDisPrice.getText()
				.toString().trim())) {
			UiDialog.showDialog(getSherlockActivity(),
					R.string.dialog_product_reg_dis_price);
			return false;
		}
		/*if (CommonUtil.PatternUtil.isEmpty(mProductCount.getText()
				.toString().trim())) {
			UiDialog.showDialog(getSherlockActivity(),
					R.string.dialog_product_reg_count);
			return false;
		}*/

		/*if (!mDeliveryFree) {
			if (CommonUtil.PatternUtil.isEmpty(mProdcutDeliveryPrice.getText()
					.toString().trim())) {
				UiDialog.showDialog(getSherlockActivity(),
						R.string.dialog_product_reg_delivery_price);
				return false;
			}
		}*/

		if (CommonUtil.PatternUtil.isEmpty(mProductDes.getText().toString()
				.trim())) {
			UiDialog.showDialog(getSherlockActivity(),
					R.string.dialog_product_reg_des);
			return false;
		}

		if(mOptionList.size() >= 10)
		{
			UiDialog.showDialog(getSherlockActivity(), "옵션은 10개까지 등록 가능합니다");
			return false;
		}

		for (int i = 0; i < mOptionList.size(); i++) {
			View view = mOptionList.get(i);

			EditText name = (EditText) view.findViewById(R.id.optionName);
			EditText price = (EditText) view.findViewById(R.id.optionPrice);
			EditText priceDc = (EditText) view.findViewById(R.id.optionPriceDc);
			//EditText count = (EditText) view.findViewById(R.id.optionCount);

			if (CommonUtil.PatternUtil.isEmpty(name.getText().toString()
					.trim())) {
				UiDialog.showDialog(getSherlockActivity(),
						R.string.dialog_product_reg_op_bin);
				return false;
			}

			if (CommonUtil.PatternUtil.isEmpty(price.getText().toString()
					.trim())) {
				UiDialog.showDialog(getSherlockActivity(),
						R.string.dialog_product_reg_op_bin);
				return false;
			}

			if (CommonUtil.PatternUtil.isEmpty(priceDc.getText().toString()
					.trim())) {
				UiDialog.showDialog(getSherlockActivity(),
						R.string.dialog_product_reg_op_bin);
				return false;
			}
			if (CommonUtil.PatternUtil.isEmpty(priceDc.getText().toString()
					.trim())) {
				UiDialog.showDialog(getSherlockActivity(),
						R.string.dialog_product_reg_op_bin);
				return false;
			}
			/*if (CommonUtil.PatternUtil.isEmpty(count.getText().toString()
					.trim())) {
				UiDialog.showDialog(getSherlockActivity(),
						R.string.dialog_product_reg_op_bin);
				return false;
			}*/
		}

		boolean isImg = false;
		for (int i = 0; i < imagePath.length; i++) {
			if (imagePath[i] != null && !PatternUtil.isEmpty(imagePath[i]))
			{
				isImg = true;
				break;
			}
			else if(i == 0) {
				break;
			}
		}
		if (!isImg) {
			UiController.showDialog(getSherlockActivity(), R.string.dialog_invalid_image);
			return false;
		}

		return true;
	}

	public void productModify() {

		if(isSanding) return;

		if(!productCheck())
		{
			isSanding = false;
			return;
		}

		isSanding = true;

		KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_EDIT, "Click_Edit", null);

		UserDb mUserData = DbController.queryCurrentUser(getActivity());

		JSONObject product = new JSONObject();
		JSONObject option = new JSONObject();
		HashMap<String,String> images = new HashMap<>();

		product.put("idx",mProductItemJson.idx);
		product.put("code",mProductItemJson.code);
		product.put("category",((ProductRegisterActivity) getActivity()).mFarmerCategory.get(categoryIndex).SubIndex);
		product.put("name",mProductName.getText().toString().trim());
		product.put("price", mProductPrice.getText().toString().trim());
		product.put("dcprice", mProductDisPrice.getText().toString().trim());
		product.put("stock", mProductCount.getText().toString().trim());
		product.put("soldout", mProductSoldOut.isChecked() == true ? "T" : "F" );
		//product.put("contents",mProductDes.getText().toString());
		product.put("contents",Html.toHtml(new SpannableString(mProductDes.getText().toString())));

		if(mProductOptionList.size()>0) {
			product.put("option",mProductOptionList.get(0).idx);
		}


		JSONObject idxObject = new JSONObject();
		JSONObject nameObject = new JSONObject();
		JSONObject priceObject = new JSONObject();
		JSONObject dcPriceObject = new JSONObject();
		JSONObject displayObject = new JSONObject();
		JSONObject countObject = new JSONObject();
		JSONObject soldOutObject = new JSONObject();

		for (int i = 0; i < mProductOptionList.size()-1; i++) {

			String isDisplay = mProductOptionList.get(i+1).display;

			idxObject.put(i, mProductOptionList.get(i+1).idx);
			displayObject.put(i, isDisplay);

			if(isDisplay.equals("T"))
			{
				for(int j=0 ; j< mOptionList.size();j++)
				{
					View view = mOptionList.get(j);
					EditText name = (EditText) view.findViewById(R.id.optionName);
					EditText price = (EditText) view.findViewById(R.id.optionPrice);
					EditText dcPrice = (EditText) view.findViewById(R.id.optionPriceDc);
					Button button = (Button) view.findViewById(R.id.optionDel);
					EditText count = (EditText) view.findViewById(R.id.optionCount);
					CheckBox soldout = (CheckBox) view.findViewById(R.id.optionSoldOut);

					String idx = (String) button.getTag();

					if(idx != null && !idx.isEmpty() && mProductOptionList.get(i+1).idx.equals(idx))
					{
						nameObject.put(mProductOptionList.size()+i+1, name.getText().toString().trim());
						priceObject.put(mProductOptionList.size()+i+1,price.getText().toString().trim());
						dcPriceObject.put(mProductOptionList.size()+i+1, dcPrice.getText().toString().trim());
						countObject.put(mProductOptionList.size()+i+1, count.getText().toString().trim());
						soldOutObject.put(mProductOptionList.size()+i+1, soldout.isChecked() == true ? "T":"F");
						idxObject.put(mProductOptionList.size()+i+1, mProductOptionList.get(i+1).idx);
						displayObject.put(mProductOptionList.size()+i+1,"T");
						break;
					}
				}
			}
			else
			{
				nameObject.put(i, mProductOptionList.get(i+1).name);
				priceObject.put(i, mProductOptionList.get(i+1).price);
				dcPriceObject.put(i, mProductOptionList.get(i+1).dcprice);
				countObject.put(i, mProductOptionList.get(i+1).stock);
				soldOutObject.put(i, mProductOptionList.get(i+1).soldout);
			}
		}

		for (int i = 0; i < mOptionList.size(); i++) {

			View view = mOptionList.get(i);
			EditText name = (EditText) view.findViewById(R.id.optionName);
			EditText price = (EditText) view.findViewById(R.id.optionPrice);
			EditText dcPrice = (EditText) view.findViewById(R.id.optionPriceDc);
			Button button = (Button) view.findViewById(R.id.optionDel);
			EditText count = (EditText) view.findViewById(R.id.optionCount);
			CheckBox soldout = (CheckBox) view.findViewById(R.id.optionSoldOut);

			String idx = (String) button.getTag();

			if(idx == null || idx.isEmpty())
			{
				nameObject.put(mProductOptionList.size()+i+1, name.getText().toString().trim());
				priceObject.put(mProductOptionList.size()+i+1,price.getText().toString().trim());
				dcPriceObject.put(mProductOptionList.size()+i+1, dcPrice.getText().toString().trim());
				countObject.put(mProductOptionList.size()+i+1, count.getText().toString().trim());
				soldOutObject.put(mProductOptionList.size()+i+1, soldout.isChecked() == true ? "T":"F");
				idxObject.put(mProductOptionList.size()+i+1, "");
				displayObject.put(mProductOptionList.size()+i+1,"T");
			}
		}
		option.put("option_name",nameObject);
		option.put("option_price",priceObject);
		option.put("option_dcprice", dcPriceObject);
		option.put("option_stock",countObject);
		option.put("option_soldout",soldOutObject);
		option.put("option_idx", idxObject);
		option.put("option_display", displayObject);


		if(imagePath.length>0)
		{
			for (int i = 0; i < imagePath.length; i++) {

				if(imageChange[i] == true) {
					try {
						RandomAccessFile f = new RandomAccessFile(imagePath[i], "r");
						byte[] b = new byte[(int) f.length()];
						f.read(b);
						images.put(String.valueOf(i),Base64.encodeToString(b, 0));
						f.close();
					} catch (Exception e) {
					}
				}
			}
		}

		SnipeApiController.updateProduct(mUserData.getUserID(), product.toJSONString(), option.toJSONString(), images, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					isSanding = false;
					switch (Code) {
						case 200:
							UiController.showDialog(getSherlockActivity(), R.string.dialog_product_reg_modify_ok, new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
									(getActivity().getSupportFragmentManager()).popBackStack();
									((ProductRegisterActivity) getActivity()).registerListRefresh();
									((ProductRegisterActivity)getActivity()).actionBatRightBtn.setText("등록");
								}
							});
							break;
						default:
							UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
					}
				} catch (Exception e) {
					UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				isSanding = false;
			}
		});

	}

	public void productRegister() {

		if(isSanding) return;

		if(!productCheck())
		{
			isSanding = false;
			return;
		}

		isSanding = true;

		KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_ADD, "Click_Add", null);

		UserDb mUserData = DbController.queryCurrentUser(getActivity());

		JSONObject product = new JSONObject();
		JSONObject option = new JSONObject();
		HashMap<String,String> images = new HashMap<>();

		product.put("category",((ProductRegisterActivity) getActivity()).mFarmerCategory.get(categoryIndex).SubIndex);
		product.put("name",mProductName.getText().toString().trim());
		product.put("price", mProductPrice.getText().toString().trim());
		product.put("dcprice", mProductDisPrice.getText().toString().trim());
		product.put("stock", mProductCount.getText().toString().trim());
		product.put("soldout", mProductSoldOut.isChecked() == true ? "T" : "F" );
		//product.put("contents", mProductDes.getText().toString());
		product.put("contents",Html.toHtml(new SpannableString(mProductDes.getText().toString())));

		if(mOptionList.size()>0)
		{
			JSONObject nameObject = new JSONObject();
			JSONObject priceObject = new JSONObject();
			JSONObject dcPriceObject = new JSONObject();
			JSONObject countObject = new JSONObject();
			JSONObject soldOutObject = new JSONObject();

			for (int i = 0; i < mOptionList.size(); i++) {
				View view = mOptionList.get(i);
				EditText name = (EditText) view.findViewById(R.id.optionName);
				EditText price = (EditText) view.findViewById(R.id.optionPrice);
				EditText dcPrice = (EditText) view.findViewById(R.id.optionPriceDc);
				EditText count = (EditText) view.findViewById(R.id.optionCount);
				CheckBox soldout = (CheckBox) view.findViewById(R.id.optionSoldOut);

				nameObject.put(i, name.getText().toString().trim());
				priceObject.put(i,price.getText().toString().trim());
				dcPriceObject.put(i, dcPrice.getText().toString().trim());
				countObject.put(i, count.getText().toString().trim());
				soldOutObject.put(i, soldout.isChecked() == true ? "T" : "F");
			}
			option.put("option_name",nameObject);
			option.put("option_price",priceObject);
			option.put("option_dcprice",dcPriceObject);
			option.put("option_stock",countObject);
			option.put("option_soldout",soldOutObject);
		}

		if(imagePath.length>0)
		{
			for (int i = 0; i <= imagePath.length; i++) {
				try {
					RandomAccessFile f = new RandomAccessFile(imagePath[i], "r");
					byte[] b = new byte[(int) f.length()];
					f.read(b);
					images.put(String.valueOf(i), Base64.encodeToString(b, 0));
					f.close();
				} catch (Exception e) {
				}
			}
		}

		SnipeApiController.InsertProduct(mUserData.getUserID(), product.toJSONString(), option.toJSONString(), images, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				try {
					isSanding = false;
					switch (Code) {
						case 200:
							UiController.showDialog(getActivity(), R.string.dialog_product_reg_ok, new CustomDialogListener() {
								@Override
								public void onDialog(int type) {
									try {
										(getActivity().getSupportFragmentManager()).popBackStack();
										((ProductRegisterActivity) getActivity()).registerListRefresh();
									}
									catch (Exception e){}
								}
							});
							break;
						default:
							UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
					}
				} catch (Exception e) {
					UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
				super.onFailure(statusCode, headers, content, error);
				isSanding = false;
			}
		});
	}


	//////////////////////// 옵션

	public void addOption(String idx) {

		final View optionView = mInflater.inflate(R.layout.item_product_option,
				null, false);

		Button button = (Button) optionView.findViewById(R.id.optionDel);
		button.setText("-");
		button.setTag(idx);
		button.setOnClickListener(new ViewOnClickListener() {

			@Override
			public void viewOnClick(View v) {
				String idx = (String) v.getTag();
				delOption(optionView, idx);
				KfarmersAnalytics.onClick(getType(), "Click_Option-Delete", null);
			}
		});

		LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		param.topMargin = mMarginsize;
		optionView.setLayoutParams(param);

		mProdcutOptionLayout.addView(optionView);
		mOptionList.add(optionView);
	}

	public void delOption(View view, String idx) {
		mOptionList.remove(view);
		mProdcutOptionLayout.removeView(view);

		if(mOptionList.size() == 0) {
			//mViewOptionTitle.setVisibility(View.GONE);
		}

		try {
			for(int i=0 ; i< mProductOptionList.size();i++)
			{
				if(mProductOptionList.get(i).idx.equals(idx))
				{
					mProductOptionList.get(i).display = "F";
					break;
				}
			}
		}catch (Exception e){}
	}

	//////////////////////// 이미지

	private void runImageRotateActivity(int takeType, String path) {
		if (getSherlockActivity() instanceof ProductRegisterActivity) {
			Intent intent = new Intent(getSherlockActivity(),
					ImageRotateActivity.class);
			intent.putExtra("takeType", takeType);
			intent.putExtra("imagePath", path);
			getSherlockActivity()
					.startActivityFromFragment(this, intent,
							Constants.REQUEST_ROTATE_PICTURE);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (getSherlockActivity() instanceof ProductRegisterActivity) {
			menu.add(CONTEXT_MENU_GROUP_ID, R.id.btn_camera_capture, 0,
					R.string.context_menu_camera_capture);
			menu.add(CONTEXT_MENU_GROUP_ID, R.id.btn_camera_gallery, 0,
					R.string.context_menu_camera_gallery);
			menu.setHeaderTitle(R.string.context_menu_camera_title);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item.getGroupId() == CONTEXT_MENU_GROUP_ID) {
			switch (item.getItemId()) {
			case R.id.btn_camera_capture:
				if (getSherlockActivity() instanceof ProductRegisterActivity) {
					imagePath[imageIndex] = ImageUtil
							.takePictureFromCameraFragment(
									getSherlockActivity(),
									ProductRegisterFragment.this,
									Constants.REQUEST_TAKE_CAPTURE);
					KfarmersAnalytics.onClick(getType(), "Click_ProductImage", "촬영");
				}
				return true;
			case R.id.btn_camera_gallery:
				if (getSherlockActivity() instanceof ProductRegisterActivity) {
					ImageUtil.takePictureFromGalleryFragment(
							getSherlockActivity(),
							ProductRegisterFragment.this,
							Constants.REQUEST_TAKE_PICTURE);
					KfarmersAnalytics.onClick(getType(), "Click_ProductImage", "불러오기");
				}
				return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Constants.REQUEST_TAKE_CAPTURE) {
				runImageRotateActivity(Constants.REQUEST_TAKE_CAPTURE,
						imagePath[imageIndex]);
				return;
			} else if (requestCode == Constants.REQUEST_TAKE_PICTURE) {
				if (null == data.getData()) {
					return;
				}
				imagePath[imageIndex] = ImageUtil.getConvertPathMediaStoreImageFile(getSherlockActivity(), data);
				runImageRotateActivity(Constants.REQUEST_TAKE_PICTURE, imagePath[imageIndex]);
				return;
			} else if (requestCode == Constants.REQUEST_ROTATE_PICTURE) {
				imagePath[imageIndex] = data.getStringExtra("imagePath");
				displayImagesView(imagePath[imageIndex]);
				imageChange[imageIndex] = true;
				return;
			}
		} else {
			imagePath[imageIndex] = null;
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void displayImagesView(String path) {
		if (path == null)
			return;

		if (getSherlockActivity() instanceof ProductRegisterActivity) {
			((ProductRegisterActivity) getSherlockActivity()).imageLoader
					.loadImage("file://" + path, new ImageSize(
									Constants.RESIZE_IMAGE_WIDTH,
									Constants.RESIZE_IMAGE_HEIGHT),
							new SimpleImageLoadingListener() {
								@Override
								public void onLoadingComplete(String imageUri,
															  View view, Bitmap loadedImage) {
									imageBtn[imageIndex]
											.setScaleType(ScaleType.CENTER_CROP);
									imageBtn[imageIndex]
											.setImageBitmap(loadedImage);
								}
							});


		}
	}

	private final ViewOnClickListener imageViewOnClickListener = new ViewOnClickListener() {
		@Override
		public void viewOnClick(View v) {
			switch (v.getId()) {
			case R.id.image1:
				imageIndex = 0;
				break;
			case R.id.image2:
				imageIndex = 1;
				break;
			case R.id.image3:
				imageIndex = 2;
				break;
			case R.id.image4:
				imageIndex = 3;
				break;
			case R.id.image5:
				imageIndex = 4;
				break;
			case R.id.image6:
				imageIndex = 5;
				break;
			case R.id.image7:
				imageIndex = 6;
				break;
			case R.id.image8:
				imageIndex = 7;
				break;
			case R.id.image9:
				imageIndex = 8;
				break;
			case R.id.image10:
				imageIndex = 9;
				break;
			}
			v.showContextMenu();
		}
	};

	public String getType()
	{
		if(isModify)
		{
			return KfarmersAnalytics.S_PRODUCT_MANAGE_EDIT;
		}
		else
		{
			return KfarmersAnalytics.S_PRODUCT_MANAGE_ADD;
		}
	}
}

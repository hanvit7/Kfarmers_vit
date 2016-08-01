package com.leadplatform.kfarmers.view.product.register;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
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
import com.leadplatform.kfarmers.model.holder.ViewHolder;
import com.leadplatform.kfarmers.model.json.snipe.ProductJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseRefreshMoreListFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment;
import com.leadplatform.kfarmers.view.product.ProductActivity;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ProductRegisterListFragment extends BaseRefreshMoreListFragment {
	public static final String TAG = "ProductRegisterListFragment";

	private final int limit = 20;
	private String oldIndex = "";
	private boolean bMoreFlag = false;

	private LinearLayout mViewEmpty;
	private Button mRegisterBtn;

	private RelativeLayout mCategoryLayout;
	private TextView mCategoryText;
	private int categoryIndex = 0;

	private MainAllListAdapter mAllListAdapter;

	private UserDb mUserData;
	
	public static ProductRegisterListFragment newInstance() {
		final ProductRegisterListFragment f = new ProductRegisterListFragment();
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		KfarmersAnalytics.onScreen(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_product_register_list, container, false);
		
		mCategoryLayout = (RelativeLayout) v.findViewById(R.id.categoryLayout);
		mCategoryText = (TextView) v.findViewById(R.id.categoryText);
		mRegisterBtn = (Button) v.findViewById(R.id.ProductInsert);
		mViewEmpty = (LinearLayout) v.findViewById(R.id.ViewEmpty);
		
		
		mCategoryLayout.setOnClickListener(new ViewOnClickListener() {
		@Override
		public void viewOnClick(View v) 
		{
			((ProductRegisterActivity)getActivity()).onCategoryBtnClicked(categoryIndex, new CategoryDialogFragment.OnCloseCategoryDialogListener() {
				@Override
				public void onDialogListSelection(int subMenuType, int position) {
					if (categoryIndex != position) {
						categoryIndex = position;
						mCategoryText.setText(((ProductRegisterActivity)getActivity()).mFarmerCategory.get(position).SubName);

						oldIndex = "";
						bMoreFlag = false;

						mAllListAdapter.clear();
						getProductRegisterList();

						KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, "Click_Category", ((ProductRegisterActivity)getActivity()).mFarmerCategory.get(position).SubName);
					}
				}
			});
		}});
		
		mRegisterBtn.setOnClickListener(new ViewOnClickListener() {

			@Override
			public void viewOnClick(View v) {
				((ProductRegisterActivity)getActivity()).registerFragment(null);
				KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, "Click_Product-Add", null);
			}
		});
		
		mCategoryText.setText(((ProductRegisterActivity) getActivity()).mFarmerCategory.get(0).SubName);
		setRefreshListView(getSherlockActivity(), v, R.id.refresh_layout, refreshListener);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mUserData = DbController.queryCurrentUser(getActivity());

		getListView().setDivider(null);
		getListView().setDividerHeight(0);
		setMoreListView(getSherlockActivity(), R.layout.view_list_footer, loadMoreListener);
		if (mAllListAdapter == null) {
			mAllListAdapter = new MainAllListAdapter(getSherlockActivity(), R.layout.item_register_product, new ArrayList<ProductJson>(),
					((BaseFragmentActivity) getSherlockActivity()).imageLoader);
			
	        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mAllListAdapter);
	        swingBottomInAnimationAdapter.setAbsListView(getListView());
	        
	        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
	        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(300);
	        
			setListAdapter(swingBottomInAnimationAdapter);
			getProductRegisterList();
		}
	}
	
	public void getProductRegisterList() 
	{
		String category = ((ProductRegisterActivity)getActivity()).mFarmerCategory.get(categoryIndex).SubIndex;
		SnipeApiController.getProductList(String.valueOf(limit), oldIndex, "", mUserData.getUserID(), category, false, "R",null, new SnipeResponseListener(getActivity()) {
			@Override
			public void onSuccess(int Code, String content, String error) {
				onRefreshComplete();
				onLoadMoreComplete();
				try {
					switch (Code) {
						case 200:
							JsonNode root = JsonUtil.parseTree(content);
							List<ProductJson> arrayList = new ArrayList<ProductJson>();

							if(root.path("item").size() > 0)
							{
								arrayList = (List<ProductJson>) JsonUtil.jsonToArrayObject(root.path("item"), ProductJson.class);
								mAllListAdapter.addAll(arrayList);
								oldIndex = mAllListAdapter.getItem(mAllListAdapter.getCount() - 1).idx;
							}

							if (arrayList.size() == limit)
								bMoreFlag = true;
							else
								bMoreFlag = false;

							if(mAllListAdapter.getCount() <=0)
							{
								mViewEmpty.setVisibility(View.VISIBLE);
							}
							else
							{
								mViewEmpty.setVisibility(View.GONE);
							}

							mAllListAdapter.notifyDataSetChanged();

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
				onRefreshComplete();
				onLoadMoreComplete();
				super.onFailure(statusCode, headers, content, error);
			}
		});

		/*String category = ((ProductRegisterActivity)getActivity()).mFarmerCategory.get(categoryIndex).farmer_food_category_idx;
		TokenApiController.getProductRegisterList(getActivity(),mPage,category, new TokenResponseListener(getActivity())
        {
        	public void onSuccess(int Code, String content) 
        	{
        		onRefreshComplete();
				onLoadMoreComplete();
				
        		if(Code == TokenResponseListener.SUCCESS)
        		{
					try {
						JsonNode jsonNode = JsonUtil.parseTree(content);
						ArrayList<ProductItemJson> productItemJsons = (ArrayList<ProductItemJson>) JsonUtil.jsonToArrayObject(jsonNode.get("rstContent"), ProductItemJson.class);
						
						ArrayList<ProductItemJson> productSubItemJsons = (ArrayList<ProductItemJson>) JsonUtil.jsonToArrayObject(jsonNode.get("rstSubContent"), ProductItemJson.class);
						
						if(productItemJsons != null)
						{
							if(productSubItemJsons != null)
							{
								Collections.reverse(productSubItemJsons);
								
								HashMap<String, ArrayList<ProductItemJson>> hashMap = new HashMap<String, ArrayList<ProductItemJson>>();
								
								for(int i =0; i< productSubItemJsons.size(); i++)
								{
									ArrayList<ProductItemJson> productTempList = new ArrayList<ProductItemJson>();
									
									if(hashMap.containsKey(productSubItemJsons.get(i).itm_grp_code))
									{
										productTempList = hashMap.get(productSubItemJsons.get(i).itm_grp_code);
									}
									productTempList.add(productSubItemJsons.get(i));
									hashMap.put(productSubItemJsons.get(i).itm_grp_code, productTempList);
								}
								
						        for (String key : hashMap.keySet())
						        {
						        	for(int i=0 ; i< productItemJsons.size();i++)
						        	{
						        		if(productItemJsons.get(i).itm_grp_code.equals(key))
						        		{
						        			productItemJsons.get(i).optionList = hashMap.get(key);
						        			break;
						        		}
						        	}
						        }
							}
							mAllListAdapter.addAll(productItemJsons);
						}
						
						if(mAllListAdapter.getCount() <=0)
						{
							mRegisterBtn.setVisibility(View.VISIBLE);
						}
						else
						{
							mRegisterBtn.setVisibility(View.GONE);
						}
						
						if (productItemJsons != null && productItemJsons.size() == 20)
							bMoreFlag = true;
						else
							bMoreFlag = false;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
        		}
        		else
        		{
        			UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
        		}
        	}

            @Override
        	public void onFailure(int statusCode, Header[] headers,
        			byte[] content, Throwable error) {
        		super.onFailure(statusCode, headers, content, error);
        		onRefreshComplete();
				onLoadMoreComplete();
        	}
        });*/
	}
	
	public void setProductSoldOut(final String pos, String isSoldOut) 
	{
		/*final ProductItemJson itemJson = mAllListAdapter.getItem(Integer.parseInt(pos));
		
		TokenApiController.setProductSoldOut(itemJson.idx,isSoldOut, new TokenResponseListener(getActivity())
        {
        	public void onSuccess(int Code, String content) 
        	{
        		onRefreshComplete();
				onLoadMoreComplete();
				
        		if(Code == TokenResponseListener.SUCCESS)
        		{
        			if(itemJson.itm_sold_out_fl.equals("N"))
        			{
        				mAllListAdapter.getItem(Integer.parseInt(pos)).itm_sold_out_fl = "Y";
        				UiController.showDialog(getActivity(),"품절상태로 변경되었습니다");
        			}
        			else
        			{
        				mAllListAdapter.getItem(Integer.parseInt(pos)).itm_sold_out_fl = "N";
        				UiController.showDialog(getActivity(),"판매중으로 변경되었습니다");
        			}
        			
        			mAllListAdapter.notifyDataSetChanged();
        		}
        		else
        		{
        			UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
        		}
        	}

            @Override
        	public void onFailure(int statusCode, Header[] headers,
        			byte[] content, Throwable error) {
        		super.onFailure(statusCode, headers, content, error);
        		onRefreshComplete();
				onLoadMoreComplete();
				UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
        	}
        });*/
	}
	
	private class MainAllListAdapter extends ArrayAdapter<ProductJson> {
		private int itemLayoutResourceId;
		private ImageLoader imageLoader;
		private DisplayImageOptions optionsProduct;

		public MainAllListAdapter(Context context, int itemLayoutResourceId, ArrayList<ProductJson> items, ImageLoader imageLoader) {
			super(context, itemLayoutResourceId, items);
			this.itemLayoutResourceId = itemLayoutResourceId;
			this.imageLoader = imageLoader;

			this.optionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565).showImageOnLoading(R.drawable.common_dummy)
					.build();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(itemLayoutResourceId, null);
			}
			
			View rootView = ViewHolder.get(convertView, R.id.Top);
			ImageView image = ViewHolder.get(convertView, R.id.productImg);
			TextView des = ViewHolder.get(convertView, R.id.des);
			TextView price = ViewHolder.get(convertView, R.id.price);
			TextView priceDc = ViewHolder.get(convertView, R.id.priceDc);
			TextView publish = ViewHolder.get(convertView, R.id.publish);

			TextView type = ViewHolder.get(convertView, R.id.productType);
			
			/*Button soldYes = ViewHolder.get(convertView, R.id.prodcutSoldOutYes);
			Button soldNo = ViewHolder.get(convertView, R.id.prodcutSoldOutNo);*/
			
			LinearLayout modifyBtn = ViewHolder.get(convertView, R.id.prodcutModify);
			LinearLayout delBtn = ViewHolder.get(convertView, R.id.productDel);
			LinearLayout urlBtn = ViewHolder.get(convertView, R.id.prodcutUrl);
			
			final ProductJson item = getItem(position);

			if (item != null)
			{
				if(item.image1 != null && !item.image1.isEmpty()) {
					imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image1, image, optionsProduct);
				}else if(item.image2 != null && !item.image2.isEmpty()) {
					imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image2, image, optionsProduct);
				}else if(item.image3 != null && !item.image3.isEmpty()) {
					imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image3, image, optionsProduct);
				}else if(item.image4 != null && !item.image4.isEmpty()) {
					imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image4, image, optionsProduct);
				}else if(item.image5 != null && !item.image5.isEmpty()) {
					imageLoader.displayImage(Constants.KFARMERS_SNIPE_IMG + item.image5, image, optionsProduct);
				}
				des.setText(item.name);

				int itemPrice = (int)Double.parseDouble(item.price);
				int itemBuyPrice = (int) Double.parseDouble(item.buyprice);

				price.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(Long.valueOf(itemPrice)) + getResources().getString(R.string.korea_won));

				if(itemPrice >itemBuyPrice)
				{
					priceDc.setText(CommonUtil.SimpleFormatUtil.convertUnitToCommaUnit(itemBuyPrice)+getResources().getString(R.string.korea_won));
					price.setPaintFlags(price.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					priceDc.setVisibility(View.VISIBLE);
				}
				else
				{
					price.setPaintFlags( price.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
					priceDc.setVisibility(View.GONE);
				}

				publish.setBackgroundColor(Color.parseColor("#AA000000"));

				if(item.display_text.equals("승인")) {
					publish.setBackgroundColor(Color.parseColor("#ff8686"));
					publish.setText(item.display_text);
				}
				else {
					publish.setText(item.display_text);
				}

				/*if(item.itm_sold_out_fl.equals("N"))
				{
					soldYes.setVisibility(View.INVISIBLE);
					soldNo.setVisibility(View.VISIBLE);
				}
				else
				{
					soldYes.setVisibility(View.VISIBLE);
					soldNo.setVisibility(View.INVISIBLE);
				}

				soldYes.setTag(position+"");
				soldNo.setTag(position+"");

				soldYes.setOnClickListener(soldBtnClickListener);
				soldNo.setOnClickListener(soldBtnClickListener);*/

				if (item.verification.equals(ProductActivity.TYPE1)) {
					type.setText("소비자검증");
				} else if (item.verification.equals(ProductActivity.TYPE2)) {
					type.setText("MD검증");
				} else {
					type.setText("일반상품");
				}

				urlBtn.setTag(item.link_url);
				urlBtn.setOnClickListener(new ViewOnClickListener() {
					@Override
					public void viewOnClick(View v) {
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, "Click_Item-UrlCopy", null);
						String url = (String) v.getTag();
						ClipboardManager clip = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
						clip.setText(url);
						UiDialog.showDialog(getActivity(),url+"\n해당 상품의 주소를 복사 하였습니다.");
					}
				});

				modifyBtn.setTag(item.idx);
				modifyBtn.setOnClickListener(new ViewOnClickListener() {

					@Override
					public void viewOnClick(View v) {
						String idx = (String) v.getTag();
						((ProductRegisterActivity) getActivity()).registerFragment(idx);
						((ProductRegisterActivity) getActivity()).actionBatRightBtn.setText("수정");
						KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, "Click_Item-Edit", null);
					}
				});
				delBtn.setTag(item.idx);
				delBtn.setOnClickListener(new ViewOnClickListener() {

					@Override
					public void viewOnClick(View v) {
						final String idx = (String) v.getTag();
						UiDialog.showDialog(getActivity(), getString(R.string.dialog_product_reg_del_query), R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
							@Override
							public void onDialog(int type) {
								if (UiDialog.DIALOG_POSITIVE_LISTENER == type) {
									KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, "Click_Item-Delete", null);
									SnipeApiController.delProduct(mUserData.getUserID(), idx, new SnipeResponseListener(getActivity()) {
										@Override
										public void onSuccess(int Code, String content, String error) {
											try {
												switch (Code) {
													case 200:
														oldIndex = "";
														bMoreFlag = false;
														mAllListAdapter.clear();
														getProductRegisterList();
														UiController.showDialog(getActivity(), R.string.dialog_product_reg_del_ok);
														break;
													default:
														UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
												}
											} catch (Exception e) {
												UiController.showDialog(getActivity(), R.string.dialog_unknown_error);
											}
										}
									});
								}
							}
						});
					}
				});
			}

			return convertView;
		}
	}
	
	/*ViewOnClickListener soldBtnClickListener = new ViewOnClickListener() {
		
		@Override
		public void viewOnClick(final View v) {
			
			String mes = "판매중으로 변경 하시겠습니까?";
			
			if(v.getId() != R.id.prodcutSoldOutYes)
			{
				mes = "품절 상태로 변경 하시겠습니까?";		
			}
			
			final String pos = (String) v.getTag();
			
			UiDialog.showDialog(getActivity(), mes, R.string.dialog_ok, R.string.dialog_cancel, new CustomDialogListener() {
				
				@Override
				public void onDialog(int type) {
					
					if(type == UiDialog.DIALOG_POSITIVE_LISTENER)
					{
						String isSoldout = "N";
						
						if(v.getId() != R.id.prodcutSoldOutYes)
						{
							isSoldout = "Y";		
						}

						setProductSoldOut(pos,isSoldout);
					}
				}
			});
		}
	};*/
	
	private OnLoadMoreListener loadMoreListener = new OnLoadMoreListener() {
		@Override
		public void onLoadMore() {
			if (bMoreFlag == true) {
				bMoreFlag = false;
				getProductRegisterList();
			} else {
				onLoadMoreComplete();
			}
		}
	};

	private OnRefreshListener refreshListener = new OnRefreshListener() {
		@Override
		public void onRefreshStarted(View view) {
			oldIndex = "";
			bMoreFlag = false;
			mAllListAdapter.clear();
			getProductRegisterList();
		}
	};

	public void listRefresh()
	{
		oldIndex = "";
		bMoreFlag = false;
		mAllListAdapter.clear();
		getProductRegisterList();
	}


}

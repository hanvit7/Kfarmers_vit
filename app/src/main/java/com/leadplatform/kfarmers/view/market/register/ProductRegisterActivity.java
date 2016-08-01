package com.leadplatform.kfarmers.view.market.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.custom.dialog.CustomDialogListener;
import com.leadplatform.kfarmers.custom.dialog.UiDialog;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment.OnCloseCategoryDialogListener;

import java.util.ArrayList;
import java.util.Iterator;

public class ProductRegisterActivity extends BaseFragmentActivity {
	public static final String TAG = "ProductRegisterActivity";

	private String productIndex;
	private TextView actionBarTitleText;
	private TextView actionBarLeftBtn;
	private ImageButton actionBatLeftHome;
	public TextView actionBatRightBtn;
	
	public ArrayList<CategoryJson> mFarmerCategory;
	private ArrayList<String> mFarmerCategoryStr;

	/***************************************************************/
	// Override
	/***************************************************************/
	@Override
	public void onCreateView(Bundle savedInstanceState) {
		setContentView(R.layout.activity_base);

		getCategory();
	}

	@Override
	public void initActionBar() {
		getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		getSupportActionBar().setCustomView(R.layout.view_actionbar_product_register);

		actionBarTitleText = (TextView) findViewById(R.id.title);

		displayActionBarTitleText(getString(R.string.MenuRightFarmerTextProdcut));

		initActionBarHomeBtn();
		actionBatLeftHome = (ImageButton) findViewById(R.id.homeBtn);

		actionBarLeftBtn = (Button) findViewById(R.id.leftBtn);
		actionBatRightBtn = (TextView) findViewById(R.id.rightBtn);

		actionBarLeftBtn.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				onBackPressed();
			}
		});

		actionBatRightBtn.setOnClickListener(new ViewOnClickListener() {
			
			@Override
			public void viewOnClick(View v) {
				if(getSupportFragmentManager().getBackStackEntryCount()>0)
				{
					FragmentManager fm = getSupportFragmentManager();
					ProductRegisterFragment fragment = (ProductRegisterFragment) fm.findFragmentByTag(ProductRegisterFragment.TAG);
					if (fragment != null)
					{
						if(fragment.isModify) {
							fragment.productModify();
						}else
						{
							fragment.productRegister();
						}
					}
				}
				else
				{
					registerFragment(null);
					KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_LIST, "Click_Add", null);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
	}

	@Override
	public void onBackPressed() {

		if(getSupportFragmentManager().getBackStackEntryCount()>0)
		{
			int string = R.string.dialog_product_reg_exit;
			FragmentManager fm = getSupportFragmentManager();
			final ProductRegisterFragment fragment = (ProductRegisterFragment) fm.findFragmentByTag(ProductRegisterFragment.TAG);
			if (fragment != null)
			{
				if(fragment.isModify)
				{
					string = R.string.dialog_product_reg_modify_exit;	
				}
			}


			UiDialog.showDialog(mContext, string,R.string.dialog_ok, R.string.dialog_cancel,new CustomDialogListener() {
				@Override
				public void onDialog(int type) 
				{
					if(UiDialog.DIALOG_POSITIVE_LISTENER == type)
					{
						if(fragment.isModify)
						{
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_EDIT, "Click_Cancel", null);
						}
						else
						{
							KfarmersAnalytics.onClick(KfarmersAnalytics.S_PRODUCT_MANAGE_ADD, "Click_Cancel", null);
						}

						getSupportFragmentManager().popBackStack();
						registerListRefresh();
						actionBarLeftBtn.setVisibility(View.GONE);
						actionBatLeftHome.setVisibility(View.VISIBLE);
						actionBatRightBtn.setText("등록");
					}
				}
			});	
		}
		else
		{
			super.onBackPressed();
		}
	}
	
	/***************************************************************/
	// Display
	/***************************************************************/
	public void displayActionBarTitleText(String title) {
		if (actionBarTitleText != null)
			actionBarTitleText.setText(title);
	}

	/***************************************************************/
	// Method
	/***************************************************************/

	public void registerListRefresh() {
		FragmentManager fm = getSupportFragmentManager();
		ProductRegisterListFragment fragment = (ProductRegisterListFragment) fm.findFragmentByTag(ProductRegisterListFragment.TAG);
		if (fragment != null)
		{
			fragment.listRefresh();
		}	
	}
	
	public void registerListFragment() {
		ProductRegisterListFragment fragment = ProductRegisterListFragment.newInstance();
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setBreadCrumbTitle(ProductRegisterListFragment.TAG);
		ft.add(R.id.fragment_container, fragment, ProductRegisterListFragment.TAG);
		ft.commit();
			
	}
	
	public void registerFragment(String idx) {
		ProductRegisterFragment fragment = ProductRegisterFragment.newInstance(idx);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.setBreadCrumbTitle(ProductRegisterFragment.TAG);
		ft.add(R.id.fragment_container, fragment, ProductRegisterFragment.TAG);
		ft.addToBackStack(null);
		ft.commit();
		actionBarLeftBtn.setVisibility(View.VISIBLE);
		actionBatLeftHome.setVisibility(View.GONE);
	}
	
	public void getCategory() 
	{
		mFarmerCategory = new ArrayList<>();
		UserDb user = DbController.queryCurrentUser(mContext);
		CenterController.getCategory(user.getUserID(), new CenterResponseListener(this) {
			@Override
			public void onSuccess(int Code, String content) {
				try {
					switch (Code) {
						case 0000:
							JsonNode root = JsonUtil.parseTree(content);
							if (root.findPath("List").isArray()) {

								CategoryJson tempCategory = new CategoryJson();
								tempCategory.SubName = "전체 카테고리";
								mFarmerCategory.add(tempCategory);

								Iterator<JsonNode> it = root.findPath("List").iterator();
								while (it.hasNext()) {
									CategoryJson category = (CategoryJson) JsonUtil.jsonToObject(it.next().toString(), CategoryJson.class);

									if( !category.PrimaryIndex.equals("7") && !category.PrimaryIndex.equals("8"))
									{
										mFarmerCategory.add(category);
									}
								}
								mFarmerCategoryStr = new ArrayList<String>();
								for(CategoryJson categoryJson : mFarmerCategory)
								{
									mFarmerCategoryStr.add(categoryJson.SubName);
								}
							}
							registerListFragment();
							break;
						case 1002:
							UiController.showDialog(mContext, R.string.dialog_empty_release);
							actionBatRightBtn.setVisibility(View.GONE);
							break;
						default:
							UiController.showDialog(mContext, R.string.dialog_unknown_error);
							break;
					}
				} catch (Exception e) {
					UiController.showDialog(mContext, R.string.dialog_unknown_error);
				}
			}
		});
	}
	
	public void onCategoryBtnClicked(int categoryIndex , OnCloseCategoryDialogListener onCloseCategoryDialogListener) {
		CategoryDialogFragment fragment = CategoryDialogFragment.newInstance(0, categoryIndex, mFarmerCategoryStr.toArray(new String[mFarmerCategoryStr.size()]), "");
		fragment.setOnCloseCategoryDialogListener(onCloseCategoryDialogListener);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		fragment.show(ft, CategoryDialogFragment.TAG);
	}
}

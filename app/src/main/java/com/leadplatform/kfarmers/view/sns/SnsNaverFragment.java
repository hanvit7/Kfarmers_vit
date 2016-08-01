package com.leadplatform.kfarmers.view.sns;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.CommonResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.NaverCategoryJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.xml.JSONObject;
import com.leadplatform.kfarmers.util.xml.XML;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment;
import com.leadplatform.kfarmers.view.common.CategoryDialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.login.NaverLoginHelper;
import com.leadplatform.kfarmers.view.login.OpenIdLoginListener;
import com.leadplatform.kfarmers.view.menu.setting.SettingSNSActivity;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.data.OAuthLoginState;

public class SnsNaverFragment extends BaseFragment
{
    public static final String TAG = "SnsNaverFragment";
	
    private Button mBtnCategory;
    private RelativeLayout mLayoutNaver;
    private TextView mTextView,mTextCategory;
    private ArrayList<NaverCategoryJson> mArrayList;
    private ArrayList<String> mCategoryStr;
    
	private int categoryIndex = 0;
    
    public static SnsNaverFragment newInstance()
    {
        final SnsNaverFragment f = new SnsNaverFragment();
        return f;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater,
    		@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    
    	final View v = inflater.inflate(R.layout.fragment_sns_naver, container, false);
    	
    	mLayoutNaver = (RelativeLayout) v.findViewById(R.id.naverLayout);
    	mTextView = (TextView) v.findViewById(R.id.textview);
    	mBtnCategory = (Button)v.findViewById(R.id.categoryBtn);
    	mTextCategory = (TextView) v.findViewById(R.id.categoryText);
    	
    	if(OAuthLoginState.OK == NaverLoginHelper.naverState(getSherlockActivity()) || OAuthLoginState.NEED_REFRESH_TOKEN == NaverLoginHelper.naverState(getSherlockActivity()))
    	{	
    		mTextView.setText("네이버 로그아웃");
    	}
    	else
    	{
    		mTextView.setText("네이버 아이디로 로그인");
    	}
    	
    	if(!PatternUtil.isEmpty(DbController.queryCurrentUser(getSherlockActivity()).getNaverCategoryName()))
		{
			mTextCategory.setText("선택한 카테고리 : " + DbController.queryCurrentUser(getSherlockActivity()).getNaverCategoryName());
		}
    	
    	mBtnCategory.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				
				if(OAuthLoginState.OK == NaverLoginHelper.naverState(getSherlockActivity()))
		    	{
					getCategory();
		    	}
				else if(OAuthLoginState.NEED_REFRESH_TOKEN == NaverLoginHelper.naverState(getSherlockActivity()))
		    	{
					NaverLoginHelper.naverTokenRefresh(getSherlockActivity(), new OpenIdLoginListener() {
						@Override
						public void onResult(boolean isSuccess, String content) {
							if(OAuthLoginState.OK == NaverLoginHelper.naverState(getSherlockActivity()))
					    	{
								getCategory();
					    	}
					    	
					    	else
					    	{
								NaverLoginHelper.naverLogout(getSherlockActivity());
								mTextView.setText("네이버 아이디로 로그인");
								mTextCategory.setText(getString(R.string.sns_no_category));
								NaverLoginHelper.clearNaverDb(getSherlockActivity());
					    	}
						}
					});
		    	}
				else
				{
					NaverLoginHelper.naverLogout(getSherlockActivity());
					mTextView.setText("네이버 아이디로 로그인");
					mTextCategory.setText(getString(R.string.sns_no_category));
					NaverLoginHelper.clearNaverDb(getSherlockActivity());
					naverLogin();
				}

			}
        });
    	
    	mLayoutNaver.setOnClickListener(new ViewOnClickListener() {
			@Override
			public void viewOnClick(View v) {
				if(OAuthLoginState.OK == NaverLoginHelper.naverState(getSherlockActivity()) || OAuthLoginState.NEED_REFRESH_TOKEN == NaverLoginHelper.naverState(getSherlockActivity()))
		    	{
					NaverLoginHelper.naverLogout(getSherlockActivity());
					mTextView.setText("네이버 아이디로 로그인");
					mTextCategory.setText(getString(R.string.sns_no_category));
					NaverLoginHelper.clearNaverDb(getSherlockActivity());
		    	}
				else
				{
					naverLogin();
				}
			}
		});
    	return v;
    }
    
    public void getCategory()
    {
    	NaverLoginHelper.naverUserInfo(getSherlockActivity(), new OpenIdLoginListener() {
			
			@Override
			public void onResult(boolean isSuccess, String content) {
				
				JSONObject jsonObject = XML.toJSONObject(content);
				jsonObject = jsonObject.getJSONObject("data");
				
				if(jsonObject != null && jsonObject.getJSONObject("result").get("resultcode").equals("00"))
				{
					String id = jsonObject.getJSONObject("response").get("email").toString();
					id = id.split("@")[0];
					categoryList(id);
				}
				else
				{
					UiController.showDialog(getSherlockActivity(), jsonObject.getJSONObject("result").get("message").toString());
				}
			}
		});
    }
    
    public void categoryList(String id)
    {
		NaverLoginHelper.naverCategory(getSherlockActivity(), id, new CommonResponseListener(getSherlockActivity()){
			@Override
			public void onSuccess(int Code, String content) 
			{
				try {
					
					JsonNode jsonNode = JsonUtil.parseTree(content);
					jsonNode = jsonNode.get("message").get("result");
					
					mArrayList = new ArrayList<NaverCategoryJson>();
					
					for(int i = 0 ; i< jsonNode.size(); i++)
					{
						NaverCategoryJson categoryJson = new NaverCategoryJson();
						categoryJson.name = jsonNode.get(i).get("name").textValue().trim();
						categoryJson.categoryNo = jsonNode.get(i).get("categoryNo").toString().trim();
						mArrayList.add(categoryJson);
						
						JsonNode jsonNodeSub = jsonNode.get(i).get("subCategories");
						for(int j = 0 ; j< jsonNodeSub.size(); j++)
						{
							categoryJson = new NaverCategoryJson();
							categoryJson.name = "  - "+jsonNodeSub.get(j).get("name").textValue().trim();
							categoryJson.categoryNo = jsonNodeSub.get(j).get("categoryNo").toString().trim();
							mArrayList.add(categoryJson);	
						}
					}
					
					mCategoryStr = new ArrayList<String>();
					for(NaverCategoryJson categoryJson : mArrayList)
					{
						mCategoryStr.add(categoryJson.name);
					}
					
					onCategoryBtnClicked(categoryIndex, new OnCloseCategoryDialogListener() {
						@Override
						public void onDialogListSelection(int subMenuType, int position) {
								categoryIndex = position;
								mTextCategory.setText("선택한 카테고리 : " +mArrayList.get(position).name);
								DbController.updateNaverCategory(getSherlockActivity(), mArrayList.get(position).categoryNo, mCategoryStr.get(position));
						}
					});
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}});
    }
    public void naverLogin()
    {
    	NaverLoginHelper.NaverBlogLogin(getSherlockActivity(), new OAuthLoginHandler() {
			@Override
			public void run(boolean success) {
				if (success) {
					NaverLoginHelper.naverUserInfo(getSherlockActivity(), new OpenIdLoginListener() {
						@Override
						public void onResult(boolean isSuccess, String content) {
							
							JSONObject jsonObject = XML.toJSONObject(content);
							jsonObject = jsonObject.getJSONObject("data");
							
							if(jsonObject != null && jsonObject.getJSONObject("result").get("resultcode").equals("00"))
							{
								mTextView.setText("네이버 로그아웃");
								UiController.showDialog(getSherlockActivity(), R.string.dialog_connect_sns);
								DbController.updateNaverFlag(getSherlockActivity(), true);
								
								String id = jsonObject.getJSONObject("response").get("email").toString();
								id = id.split("@")[0];
								DbController.updateBlogNaver(getSherlockActivity(), id);
								categoryList(id);
							}
							else
							{
								UiController.showDialog(getSherlockActivity(), jsonObject.getJSONObject("result").get("message").toString());
							}
						}
					});
					
				} else {
					mTextView.setText("네이버 아이디로 로그인");
					NaverLoginHelper.clearNaverDb(getSherlockActivity());
					//((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
				}											
			}
		});
    }

    
    /*public void onSnsNaverBtnClicked(final String id, final String api)
    {
        if (CommonUtil.PatternUtil.isEmpty(id))
        {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_id);
            return;
        }

        if (CommonUtil.PatternUtil.isEmpty(api))
        {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_api);
            return;
        }

        UiController.hideSoftKeyboard(getSherlockActivity());
        CenterController.snsNaverBlog(id, api, new CenterResponseListener(getSherlockActivity())
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            DbController.updateNaverSession(getSherlockActivity(), id, api);
                            ((SnsActivity) getSherlockActivity()).displaySuccessConnectSNS();
                            break;

                        case 1001:
                        case 1002:
                        case 1003:
                            ((SnsActivity) getSherlockActivity()).displayUnknownAccountSNS();
                            break;

                        default:
                            ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                            break;
                    }
                }
                catch (Exception e)
                {
                    ((SnsActivity) getSherlockActivity()).displayErrorConnectSNS();
                }
            }
        });
    }*/
    
	public void onCategoryBtnClicked(int categoryIndex , OnCloseCategoryDialogListener onCloseCategoryDialogListener) {
		CategoryDialogFragment fragment = CategoryDialogFragment.newInstance(0, categoryIndex, mCategoryStr.toArray(new String[mCategoryStr.size()]), "");
		fragment.setOnCloseCategoryDialogListener(onCloseCategoryDialogListener);
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		fragment.show(ft, CategoryDialogFragment.TAG);
	}
}

package com.leadplatform.kfarmers.view.farm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.model.json.FarmJson;
import com.leadplatform.kfarmers.model.json.NoticeListJson;
import com.leadplatform.kfarmers.model.parcel.NoticeListData;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.menu.notice.FarmNoticeActivity;

import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Iterator;

public class FarmIntroductionFragment extends BaseFragment
{
    public static final String TAG = "IntroductionFragment";

    private String farmType, farmData;
    private FarmJson farmJson;
    private TextView Introduction, CategoryTitle;
    private LinearLayout CategoryLayout, NoticeLayout;
    private ArrayList<NoticeListJson> noticeGroupItemList;

    public static FarmIntroductionFragment newInstance(String farmType, String farmData)
    {
        final FarmIntroductionFragment f = new FarmIntroductionFragment();

        final Bundle args = new Bundle();
        args.putString("farmType", farmType);
        args.putString("farmData", farmData);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_FARMINFO);

        if (getArguments() != null)
        {
            farmType = getArguments().getString("farmType");
            farmData = getArguments().getString("farmData");
            try {
                farmJson = (FarmJson) JsonUtil.jsonToObject(farmData, FarmJson.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_introduction, container, false);

        Introduction = (TextView) v.findViewById(R.id.Introduction);
        CategoryTitle = (TextView) v.findViewById(R.id.CategoryTitle);
        CategoryLayout = (LinearLayout) v.findViewById(R.id.CategoryLayout);
        NoticeLayout = (LinearLayout) v.findViewById(R.id.NoticeLayout);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        try
        {
            if (noticeGroupItemList == null)
            {
                noticeGroupItemList = new ArrayList<NoticeListJson>();
                NoticeListData noticeData = new NoticeListData();
                noticeData.setInitFlag(true);
                noticeData.setUserType(farmType);
                noticeData.setUserIndex(farmJson.FarmerIndex);
                getNoticeList(noticeData);
            }

            displayActionBarTitleText();
            displayIntroductionText();
            displayCategoryList();

            FarmLocationFragment fragment = FarmLocationFragment.newInstance(farmJson.Latitude, farmJson.Longitude, farmJson.Address);
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_map, fragment);
            fragmentTransaction.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getNoticeList(final NoticeListData data)
    {
        if (data == null)
            return;

        if (data.getUserType().equals("F"))
        {
            CenterController.getListFarmerNotice(data, new CenterResponseListener(getSherlockActivity())
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                if (root.findPath("List").isArray())
                                {
                                    Iterator<JsonNode> it = root.findPath("List").iterator();
                                    while (it.hasNext())
                                    {
                                        NoticeListJson notice = (NoticeListJson) JsonUtil.jsonToObject(it.next().toString(), NoticeListJson.class);
                                        noticeGroupItemList.add(notice);
                                    }
                                }
                                displayNoticeList();
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error)
                {
                    super.onFailure(statusCode, headers, content, error);
                }
            });
        }
        else if (data.getUserType().equals("V"))
        {
            CenterController.getListVillageNotice(data, new CenterResponseListener(getSherlockActivity())
            {
                @Override
                public void onSuccess(int Code, String content)
                {
                    try
                    {
                        switch (Code)
                        {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                if (root.findPath("List").isArray())
                                {
                                    Iterator<JsonNode> it = root.findPath("List").iterator();
                                    while (it.hasNext())
                                    {
                                        NoticeListJson notice = (NoticeListJson) JsonUtil.jsonToObject(it.next().toString(), NoticeListJson.class);
                                        noticeGroupItemList.add(notice);
                                    }
                                }
                                displayNoticeList();
                                break;
                        }
                    }
                    catch (Exception e)
                    {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error)
                {
                    super.onFailure(statusCode, headers, content, error);
                }
            });
        }
    }

    private void displayActionBarTitleText()
    {
        if (farmJson != null)
        {
            ((FarmIntroductionActivity) getSherlockActivity()).displayActionBarTitleText(farmJson.Farm);
        }
    }

    private void displayIntroductionText()
    {
        if (farmJson != null)
        {
            Introduction.setText(farmJson.Introduction);
        }
    }

    private View displayCreateCategoryView()
    {
        LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_introduction_category, null);
        return view;
    }

    private void displayCategoryList()
    {
        if (farmJson != null)
        {
            if (farmType.equals("F"))
                CategoryTitle.setText("생산정보");
            else
                CategoryTitle.setText("체험정보");

            CategoryLayout.removeAllViews();
            ArrayList<CategoryJson> categoryList = farmJson.Categories;

            for (int index = 0; index < categoryList.size(); index++)
            {
                CategoryJson category = categoryList.get(index);
                if(category.SubName.equals("일상"))
                {
                    categoryList.remove(category);
                    break;
                }
            }
            
            for (int index = 0; index < categoryList.size(); index++)
            {
                View view = displayCreateCategoryView();
                TextView categoryText = (TextView) view.findViewById(R.id.Category);
                TextView ReleaseText = (TextView) view.findViewById(R.id.Release);
                View Line = view.findViewById(R.id.Line);

                CategoryJson category = categoryList.get(index);
                categoryText.setText(category.SubName);
                ReleaseText.setText(category.ReleaseDate);
                if (index == (categoryList.size() - 1))
                    Line.setVisibility(View.GONE);

                CategoryLayout.addView(view);
            }
        }
    }

    private View displayCreateNoticeView()
    {
        LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_introduction_notice, null);
        return view;
    }

    private void displayNoticeList()
    {
        if (noticeGroupItemList != null)
        {
            NoticeLayout.removeAllViews();
            for (int index = 0; index < noticeGroupItemList.size(); index++)
            {
                View view = displayCreateNoticeView();
                TextView noticeText = (TextView) view.findViewById(R.id.Notice);
                TextView dateText = (TextView) view.findViewById(R.id.Date);
                View Line = view.findViewById(R.id.Line);

                NoticeListJson notice = noticeGroupItemList.get(index);
                noticeText.setText(notice.Title);
                dateText.setText(notice.Date);
                if (index == (noticeGroupItemList.size() - 1))
                    Line.setVisibility(View.GONE);

                view.setTag(notice);
                view.setOnClickListener(new OnClickListener()
                {                    
                    @Override
                    public void onClick(View v)
                    {
                        NoticeListJson notice = (NoticeListJson)v.getTag();
                        Intent intent = new Intent(getSherlockActivity(), FarmNoticeActivity.class);
                        intent.putExtra("userType", farmType);
                        intent.putExtra("userIndex", farmJson.FarmerIndex);
                        intent.putExtra("noticeIndex", notice.Index);                        
                        startActivity(intent);
                    }
                });
                NoticeLayout.addView(view);
            }
        }
    }
}

package com.leadplatform.kfarmers.view.menu.release;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.model.parcel.ReleaseListData;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseListFragment;

import java.util.ArrayList;
import java.util.Iterator;

public class FarmerReleaseListFragment extends BaseListFragment
{
    public static final String TAG = "FarmerReleaseListFragment";

    private final String farmerPrimaryIndexTable[] = { "1", "2", "3", "4", "5", "6" };
    private ArrayList<CategoryJson> releaseItemList;
    private ReleaseListAdapter releaseListAdapter;

    public static FarmerReleaseListFragment newInstance()
    {
        final FarmerReleaseListFragment f = new FarmerReleaseListFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        KfarmersAnalytics.onScreen(KfarmersAnalytics.S_SHIP_MANAGE_LIST, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_release_list, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (releaseListAdapter == null)
        {
            releaseItemList = new ArrayList<CategoryJson>();
            releaseListAdapter = new ReleaseListAdapter(getSherlockActivity(), R.layout.item_release, releaseItemList, new ViewOnClickListener()
            {
                @Override
                public void viewOnClick(View v)
                {
                    int position = (Integer) v.getTag();
                    CategoryJson item = (CategoryJson) getListAdapter().getItem(position);

                    ReleaseListData listData = new ReleaseListData();
                    listData.setPrimaryIndex(item.PrimaryIndex);
                    listData.setPrimaryName(item.PrimaryName);
                    listData.setSubIndex(item.SubIndex);
                    listData.setSubName(item.SubName);

                    ((FarmerReleaseActivity) getSherlockActivity()).runReleaseEditFragment(listData);
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_SHIP_MANAGE_LIST, "Click_Item", item.SubName);
                }
            });
            setListAdapter(releaseListAdapter);

            UserDb user = DbController.queryCurrentUser(getSherlockActivity());
            getReleaseList(user.getUserID());
        }
    }

    public void getReleaseList(String id)
    {
        CenterController.getCategory(id, new CenterResponseListener(getSherlockActivity())
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
                                    CategoryJson category = (CategoryJson) JsonUtil.jsonToObject(it.next().toString(), CategoryJson.class);
                                    for (int index = 0; index < farmerPrimaryIndexTable.length; index++)
                                    {
                                        if (category.PrimaryIndex.equals(farmerPrimaryIndexTable[index]))
                                        {
                                            releaseListAdapter.add(category);
                                            break;
                                        }
                                    }
                                }
                                releaseListAdapter.notifyDataSetChanged();
                            }
                            break;

                        case 1002:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_release);
                            break;
                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

}

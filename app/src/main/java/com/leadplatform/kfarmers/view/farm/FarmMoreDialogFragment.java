package com.leadplatform.kfarmers.view.farm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.model.json.FarmJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;
import com.leadplatform.kfarmers.view.menu.blog.BlogActivity;

public class FarmMoreDialogFragment extends BaseDialogFragment
{
    public static final String TAG = "FarmMoreDialogFragment";

    private String farmType, farmData;
    private FarmJson farmJson;
    private TextView Product, Blog;

    public static FarmMoreDialogFragment newInstance(String farmType, String farmData)
    {
        final FarmMoreDialogFragment f = new FarmMoreDialogFragment();

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
        if (getArguments() != null)
        {
            farmType = getArguments().getString("farmType");
            farmData = getArguments().getString("farmData");
        }
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_farm_dialog, container, false);

        Product = (TextView) v.findViewById(R.id.Product);
        // Experience = (TextView) v.findViewById(R.id.Experience);
        Blog = (TextView) v.findViewById(R.id.Blog);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        try
        {
            farmJson = (FarmJson) JsonUtil.jsonToObject(farmData, FarmJson.class);
            if (farmType.equals("F"))
            {
                boolean experienceFlag = false;
                for (CategoryJson category : farmJson.Categories)
                {
                    if (category.PrimaryIndex.equals("7"))
                    {
                        experienceFlag = true;
                    }
                }

                // if (!experienceFlag)
                // Experience.setVisibility(View.GONE);
            }
            else
            {
                Product.setVisibility(View.GONE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Product.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                UiController.showDialog(getSherlockActivity(), R.string.dialog_ready_product);
            }
        });

        // Experience.setOnClickListener(new ViewOnClickListener()
        // {
        // @Override
        // public void viewOnClick(View v)
        // {
        //
        // }
        // });

        Blog.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                Intent intent = new Intent(getSherlockActivity(), BlogActivity.class);
                intent.putExtra("userIndex", farmJson.FarmerIndex);
                startActivity(intent);
            }
        });
    }
}

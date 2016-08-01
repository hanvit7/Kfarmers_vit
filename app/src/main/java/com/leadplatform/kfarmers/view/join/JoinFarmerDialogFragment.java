package com.leadplatform.kfarmers.view.join;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;
import com.leadplatform.kfarmers.view.login.LoginActivity;

public class JoinFarmerDialogFragment extends BaseDialogFragment
{
    public static final String TAG = "JoinFarmerDialogFragment";

    private RelativeLayout farmerBtn;
    private RelativeLayout villageBtn;
    private Button cancelBtn;

    public static JoinFarmerDialogFragment newInstance()
    {
        final JoinFarmerDialogFragment f = new JoinFarmerDialogFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_join_farmer_dialog, container, false);

        farmerBtn = (RelativeLayout) v.findViewById(R.id.farmerBtn);
        villageBtn = (RelativeLayout) v.findViewById(R.id.villageBtn);
        cancelBtn = (Button) v.findViewById(R.id.cancelBtn);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        farmerBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Join", "farmer");
                    ((LoginActivity) getSherlockActivity()).onJoinDialogFarmerClicked();
                }
                dismiss();
            }
        });

        villageBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                if (getSherlockActivity() instanceof LoginActivity)
                {
                    KfarmersAnalytics.onClick(KfarmersAnalytics.S_LOGIN, "Click_Join", "village");
                    ((LoginActivity) getSherlockActivity()).onJoinDialogVillageClicked();
                }
                dismiss();
            }
        });

        cancelBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                dismiss();
            }
        });
    }
}

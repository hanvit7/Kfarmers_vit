package com.leadplatform.kfarmers.view.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.util.KfarmersAnalytics;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseMenuFragmentActivity;
import com.leadplatform.kfarmers.view.common.LicenseeInfoActivity;
import com.leadplatform.kfarmers.view.join.JoinTermsActivity;
import com.leadplatform.kfarmers.view.login.LoginActivity;
import com.leadplatform.kfarmers.view.main.MainActivity;
import com.leadplatform.kfarmers.view.menu.setting.SettingActivity;
import com.leadplatform.kfarmers.view.menu.setting.SettingServiceActivity;
import com.leadplatform.kfarmers.view.menu.story.FarmStoryActivity;

import java.util.ArrayList;
//비회원 메뉴
public class MenuFragment extends BaseFragment
{
    public static final String TAG = "MenuFragment";

    private final int menuBtnResourceId[] = {R.id.Login, R.id.Home, R.id.Impressive, R.id.Product, R.id.Event, R.id.Setting, R.id.Terms, R.id.ServiceInfo, R.id.Company};
    private ArrayList<View> menuBtnArrayList;

    public static MenuFragment newInstance()
    {
        final MenuFragment f = new MenuFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View v = inflater.inflate(R.layout.fragment_menu_left, container, false);

        menuBtnArrayList = new ArrayList<View>();

        for (int resourceId : menuBtnResourceId)
        {
            View menuBtn = (View) v.findViewById(resourceId);
            menuBtn.setTag(resourceId);
            menuBtn.setOnClickListener(menuBtnOnClickListener);
            menuBtnArrayList.add(menuBtn);
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        displaySelectMenuBtn(R.id.actionbar_home_image_button);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_LEFT_MENU)
        {
            displaySelectMenuBtn(R.id.actionbar_home_image_button);
        }
    }

    private ViewOnClickListener menuBtnOnClickListener = new ViewOnClickListener()
    {
        @Override
        public void viewOnClick(View v)
        {
            int resourceId = (Integer) v.getTag();
            displaySelectMenuBtn(resourceId);
            runMenuActivity(resourceId);
        }
    };

    private void displaySelectMenuBtn(int resourceId) {
        for (View menuBtn : menuBtnArrayList) {
            int menuId = (Integer) menuBtn.getTag();
            if (menuId == resourceId) {
                if (menuBtn instanceof TextView) {
                    menuBtn.setSelected(true);
                }
            } else {
                if (menuBtn instanceof TextView) {
                    menuBtn.setSelected(false);
                }
            }
        }
    }

    private void runMenuActivity(int resourceId)
    {
        Intent intent = null;
        switch (resourceId)
        {
            //R.id.LoginLayout, R.id.Home, R.id.Impressive, R.id.Product, R.id.Event, R.id.Setting, R.id.Terms, R.id.ServiceInfo, R.id.Company
            case R.id.Login:
                intent = new Intent(getActivity(), LoginActivity.class);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Login", null);
                break;
            case R.id.Home:
                ((MainActivity) getActivity()).setCurrentTab(MainActivity.MainTab.HOME);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Home", null);
                break;
            case R.id.Impressive:
                intent = new Intent(getActivity(), FarmStoryActivity.class);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Impressive", null);
                break;
            case R.id.Product:
                ((MainActivity) getActivity()).setCurrentTab(MainActivity.MainTab.MARKET);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Product", null);
                break;
            case R.id.Event:
                ((MainActivity) getActivity()).setCurrentTab(MainActivity.MainTab.SUPPORTERS);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Event", null);
                break;
            case R.id.Setting:
                intent = new Intent(getActivity(), SettingActivity.class);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Setting", null);
                break;
            case R.id.Terms:
                intent = new Intent(getActivity(), JoinTermsActivity.class);
                intent.putExtra("page","1");
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Terms", "이용약관");
                break;
            case R.id.ServiceInfo:
                intent = new Intent(getActivity(), SettingServiceActivity.class);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_Service", null);
                break;
            case R.id.Company:
                intent = new Intent(getActivity(), LicenseeInfoActivity.class);
                ((BaseMenuFragmentActivity) getActivity()).toggle();
                KfarmersAnalytics.onClick(KfarmersAnalytics.S_MYPAGE_NONMEMBER, "Click_CompanyInfo", null);
                break;
        }

        if (intent != null)
        {
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            getSherlockActivity().startActivityFromFragment(this, intent, Constants.REQUEST_LEFT_MENU);
        }
    }
}

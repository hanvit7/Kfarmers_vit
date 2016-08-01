package com.leadplatform.kfarmers.view.base;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityHelper;

public abstract class BaseMenuFragmentActivity extends BaseFragmentActivity implements SlidingActivityBase
{
    private SlidingActivityHelper navigationHelper;

    public abstract void initNavigationMenuSetting();

    public abstract void initLeftNavigationMenu(Bundle savedInstanceState);

    public abstract void initRightNavigationMenu(Bundle savedInstanceState);

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        navigationHelper = new SlidingActivityHelper(this);
        navigationHelper.onCreate(savedInstanceState);

        super.onCreate(savedInstanceState);

        initNavigationMenuSetting();
        initLeftNavigationMenu(savedInstanceState);
        initRightNavigationMenu(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        navigationHelper.onSaveInstanceState(outState);
    }

    /***************************************************************/
    // Navigation Menu
    /***************************************************************/
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        navigationHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public View findViewById(int id)
    {
        View v = super.findViewById(id);
        if (v != null)
            return v;
        return navigationHelper.findViewById(id);
    }

    @Override
    public void setContentView(int id)
    {
        setContentView(getLayoutInflater().inflate(id, null));
    }

    @Override
    public void setContentView(View v)
    {
        setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View v, LayoutParams params)
    {
        super.setContentView(v, params);
        navigationHelper.registerAboveContentView(v, params);
    }

    public void setBehindContentView(int id)
    {
        setBehindContentView(getLayoutInflater().inflate(id, null));
    }

    public void setBehindContentView(View v)
    {
        setBehindContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setBehindContentView(View v, LayoutParams params)
    {
        navigationHelper.setBehindContentView(v, params);
    }

    public SlidingMenu getSlidingMenu()
    {
        return navigationHelper.getSlidingMenu();
    }

    public void toggle()
    {
        navigationHelper.toggle();
    }

    public void showContent()
    {
        navigationHelper.showContent();
    }

    public void showMenu()
    {
        navigationHelper.showMenu();
    }

    public void showSecondaryMenu()
    {
        navigationHelper.showSecondaryMenu();
    }

    public void setSlidingActionBarEnabled(boolean b)
    {
        navigationHelper.setSlidingActionBarEnabled(b);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        boolean b = navigationHelper.onKeyUp(keyCode, event);
        if (b)
            return b;
        return super.onKeyUp(keyCode, event);
    }
}

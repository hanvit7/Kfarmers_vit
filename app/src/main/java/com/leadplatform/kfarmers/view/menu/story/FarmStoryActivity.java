package com.leadplatform.kfarmers.view.menu.story;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class FarmStoryActivity extends BaseFragmentActivity
{
    public static final String TAG = "FarmStoryActivity";

    private String userType = null;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_base);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        /*initUserInfo();
        if (userType != null && (userType.equals("F") || userType.equals("V")))
        {
            ImageButton writeDiaryBtn = (ImageButton) findViewById(R.id.writeDiaryBtn);
            writeDiaryBtn.setVisibility(View.VISIBLE);
            writeDiaryBtn.setOnClickListener(new ViewOnClickListener()
            {
                @Override
                public void viewOnClick(View v)
                {
                    runWriteDiaryActivity();
                }
            });
        }*/

        FarmStoryListFragment fragment = FarmStoryListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, FarmStoryListFragment.TAG);
        ft.commit();
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.title_impressive);
        initActionBarHomeBtn();
    }

    /*private void initUserInfo()
    {
        try
        {
            String profile = DbController.queryProfileContent(this);
            if (profile != null)
            {
                JsonNode root = JsonUtil.parseTree(profile);
                ProfileJson profileData = (ProfileJson) JsonUtil.jsonToObject(root.findPath("Row").toString(), ProfileJson.class);
                userType = profileData.Type;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }*/

}

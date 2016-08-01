package com.leadplatform.kfarmers.view.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class TagActivity extends BaseFragmentActivity
{
    private static final String TAG = "TagActivity";

    private EditText tagEdit,tagEdit1,tagEdit2;

    private String tag;

    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_tag);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        tagEdit = (EditText) findViewById(R.id.Tag);
        tagEdit1 = (EditText) findViewById(R.id.Tag1);
        tagEdit2 = (EditText) findViewById(R.id.Tag2);

        Intent intent = getIntent();
        if (intent != null)
        {
            String tag = intent.getStringExtra("tag");
            String categoryTag = intent.getStringExtra("categoryTag");

            this.tag = tag;
            if(tag != null && !tag.isEmpty())
            {
                String str[] = tag.split(",");
                if(str != null && str.length>0)
                {
                    try {
                        tagEdit1.setText(str[0].trim());
                        tagEdit2.setText(str[1].trim());
                    }catch (Exception e){}
                }
            }
            if(categoryTag != null && !categoryTag.isEmpty())
            {
                tagEdit.setText(categoryTag);
            }
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(R.string.WriteDiaryTagTitle);

        Button leftBtn = (Button) findViewById(R.id.leftBtn);
        leftBtn.setVisibility(View.VISIBLE);
        leftBtn.setText(R.string.actionbar_cancel);
        leftBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onActionBarLeftBtnClicked();
            }
        });

        Button rightBtn = (Button) findViewById(R.id.rightBtn);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_save);
        rightBtn.setOnClickListener(new ViewOnClickListener()
        {
            @Override
            public void viewOnClick(View v)
            {
                onActionBarRightBtnClicked();
            }
        });
    }

    /***************************************************************/
    // Method
    /***************************************************************/
    public void onActionBarLeftBtnClicked()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void onActionBarRightBtnClicked()
    {
        tag ="";
        if(!tagEdit1.getText().toString().trim().isEmpty()) {
            tag += tagEdit1.getText().toString().trim().replaceAll(",", "");
        }
        if(!tagEdit2.getText().toString().trim().isEmpty()) {
            if(!tag.toString().trim().isEmpty()) {
                tag += ",";
            }
            tag += tagEdit2.getText().toString().trim().replaceAll(",", "");
        }

        Intent intent = new Intent();
        intent.putExtra("tag", tag);
        setResult(RESULT_OK, intent);
        finish();
    }
}

package com.leadplatform.kfarmers.view.diary;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;

public class DiaryEditActivity extends BaseFragmentActivity
{
    public static final String TAG = "DiaryEditActivity";

    private int position;
    private String textContent;
    private EditText textEdit;

    private boolean backPressFlag = false;

    /***************************************************************/
    // Override
    /***************************************************************/
    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_diary_edit);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        textEdit = (EditText) findViewById(R.id.textEdit);

        Intent intent = getIntent();
        if (intent != null)
        {
            position = getIntent().getIntExtra("position", 0);
            textContent = getIntent().getStringExtra("textContent");
            textEdit.setText(textContent);
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.WriteDiaryEditTitle);

        Button leftBtn = (Button) findViewById(R.id.actionbar_left_button);
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

        Button rightBtn = (Button) findViewById(R.id.actionbar_right_button);
        rightBtn.setVisibility(View.VISIBLE);
        rightBtn.setText(R.string.actionbar_input);
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
    // Display
    /***************************************************************/

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
        textContent = textEdit.getText().toString();
        Intent intent = new Intent();
        intent.putExtra("position", position);
        intent.putExtra("textContent", textContent);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!backPressFlag) {
            Toast.makeText(this, "작성중인 글을 취소하시려면 뒤로가기 버튼을 한번더 클릭해 주세요.", Toast.LENGTH_SHORT).show();
            backPressFlag = true;
            backPressHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            super.onBackPressed();
        }
    }

    private final Handler backPressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            backPressFlag = false;
        }
    };
}

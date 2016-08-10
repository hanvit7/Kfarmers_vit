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

public class WeatherActivity extends BaseFragmentActivity
{
    private static final String TAG = "WeatherActivity";

    private EditText temperatureEdit;
    private EditText humidityEdit;
    private EditText weatherEdit;

    @Override
    public void onCreateView(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_weather);

        initContentView(savedInstanceState);
    }

    private void initContentView(Bundle savedInstanceState)
    {
        temperatureEdit = (EditText) findViewById(R.id.temperatureEdit);
        humidityEdit = (EditText) findViewById(R.id.humidityEdit);
        weatherEdit = (EditText) findViewById(R.id.weatherEdit);
        
        Intent intent = getIntent();
        if(intent != null)
        {
            temperatureEdit.setText(intent.getStringExtra("temperature"));
            humidityEdit.setText(intent.getStringExtra("humidity"));
            weatherEdit.setText(intent.getStringExtra("weather"));
        }
    }

    @Override
    public void initActionBar()
    {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.actionbar_title_text_view);
        title.setText(R.string.WriteDiarySkyTitle);

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
    // Diaplay
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
        Intent intent = new Intent();
        intent.putExtra("weather", weatherEdit.getText().toString());
        intent.putExtra("temperature", temperatureEdit.getText().toString());
        intent.putExtra("humidity", humidityEdit.getText().toString());
        setResult(RESULT_OK, intent);
        finish();
    }
}

package com.leadplatform.kfarmers.view.common;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import org.apache.http.Header;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShopAgreementActivity extends BaseFragmentActivity {
    public static final String TAG = "ShopAgreementActivity";

    private WebView mTermsWebView;

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText("이용약관");
        initActionBarHomeBtn();
    }

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_shop_agreement);

        mTermsWebView = (WebView) findViewById(R.id.TermsWebView);
        mTermsWebView.loadUrl("http://kfarmers.kr/API/Agreement.php?Page=4");

        findViewById(R.id.AgreeBtn).setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                CenterController.setProfilePayAgreement(new CenterResponseListener(mContext){
                    @Override
                    public void onSuccess(int Code, String content) {
                        super.onSuccess(Code, content);

                        if(Code == 0000)
                        {
                            requestUserProfile();
                        }
                        else
                        {
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            finish();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                        super.onFailure(statusCode, headers, content, error);
                        UiController.showDialog(mContext, R.string.dialog_unknown_error);
                        finish();
                    }
                });
            }
        });
    }

    private void requestUserProfile()
    {
        CenterController.getProfile(new CenterResponseListener(this)
        {
            @Override
            public void onSuccess(int Code, String content)
            {
                try
                {
                    switch (Code)
                    {
                        case 0000:
                            DbController.updateProfileContent(mContext, content);
                            Toast.makeText(mContext,"약관 동의가 완료 되었습니다.",Toast.LENGTH_SHORT).show();
                            finish();
                            break;
                        default:
                            UiController.showDialog(mContext, R.string.dialog_unknown_error);
                            finish();
                            break;
                    }
                }
                catch (Exception e)
                {
                    UiController.showDialog(mContext, R.string.dialog_unknown_error);
                }
            }
        });
    }

    private String getText(String content)
    {
        Pattern SCRIPTS = Pattern.compile("<(no)?script[^>]*>.*?</(no)?script>",Pattern.DOTALL);
        Pattern STYLE = Pattern.compile("<style[^>]*>.*</style>",Pattern.DOTALL);
        Pattern TAGS = Pattern.compile("<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>");
        Pattern nTAGS = Pattern.compile("<\\w+\\s+[^<]*\\s*>");
        Pattern ENTITY_REFS = Pattern.compile("&[^;]+;");
        Pattern WHITESPACE = Pattern.compile("\\s\\s+");

        Matcher m;

        m = SCRIPTS.matcher(content);
        content = m.replaceAll("");
        m = STYLE.matcher(content);
        content = m.replaceAll("");
        m = TAGS.matcher(content);
        content = m.replaceAll("");
        m = ENTITY_REFS.matcher(content);
        content = m.replaceAll("");
        m = WHITESPACE.matcher(content);
        content = m.replaceAll(" ");

        return content;
    }
}

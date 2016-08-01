package com.leadplatform.kfarmers.view.more;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.model.json.DiaryListJson;
import com.leadplatform.kfarmers.model.parcel.DiaryListData;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import org.apache.http.Header;

import java.util.ArrayList;

public class MoreFragment extends BaseFragment {
    public static final String TAG = "MoreFragment";

    ImageLoader mImageLoader;
    DisplayImageOptions mOptionsProduct;

    ImageView mImageViewImpressive, mImageViewGeneral;
    TextView mTextViewImpressive, mTextViewGeneral;
    TextView mTextViewImpressiveContent, mTextViewGeneralContent;

    DiaryListJson mDiaryImpressive, mDiaryGeneral;

    public static MoreFragment newInstance() {
        final MoreFragment f = new MoreFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = ((BaseFragmentActivity) getSherlockActivity()).imageLoader;
        mOptionsProduct = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Bitmap.Config.RGB_565).showImageOnLoading(R.drawable.common_dummy).displayer(new FadeInBitmapDisplayer(1000, true, true, true))
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_more, container, false);

        mImageViewImpressive = (ImageView) v.findViewById(R.id.ImageViewImpressive);
        mImageViewGeneral = (ImageView) v.findViewById(R.id.ImageViewGeneral);

        mTextViewImpressive = (TextView) v.findViewById(R.id.TextImpressive);
        mTextViewGeneral = (TextView) v.findViewById(R.id.TextGeneral);

        mTextViewImpressiveContent = (TextView) v.findViewById(R.id.TextImpressiveContent);
        mTextViewGeneralContent = (TextView) v.findViewById(R.id.TextGeneralContent);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getStory(true);
        getStory(false);
    }


    private void getStory(final boolean isImpressive) {

        DiaryListData data = new DiaryListData();
        if (isImpressive) {
            data.setImpressive(true);
        } else {
            data.setCategory1("8");
        }

        CenterController.getListDiary(data, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:

                            JsonNode root = JsonUtil.parseTree(content);
                            ArrayList<DiaryListJson> jsonArrayList = (ArrayList<DiaryListJson>) JsonUtil.jsonToArrayObject(root.findPath("List"), DiaryListJson.class);

                            if (jsonArrayList.size() > 0) {
                                if (isImpressive) {
                                    mDiaryImpressive = jsonArrayList.get(0);
                                } else {
                                    mDiaryGeneral = jsonArrayList.get(0);
                                }
                            }
                            makeStory(isImpressive);
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
                UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
            }
        });
    }

    private void makeStory(boolean isImpressive) {
        if (isImpressive) {
            mImageLoader.displayImage(mDiaryImpressive.ProductImage1, mImageViewImpressive, mOptionsProduct);

            String chText = "";
            String[] lines = mDiaryImpressive.Description.split(System.getProperty("line.separator"));

            for(int i=0; i<= lines.length;i++)
            {
                while(lines[i].startsWith("\n"))
                {
                    lines[i] = lines[i].replaceFirst("\n", "");
                }
                if(!lines[i].trim().isEmpty())
                {
                    chText+=lines[i]+"\n";

                    if(chText.split(System.getProperty("line.separator")).length >1)
                    {
                        break;
                    }
                }
            }
            mTextViewImpressiveContent.setText(chText);
        } else {
            mImageLoader.displayImage(mDiaryGeneral.ProductImage1, mImageViewGeneral, mOptionsProduct);

            String chText = "";
            String[] lines = mDiaryGeneral.Description.split(System.getProperty("line.separator"));

            for(int i=0; i<= lines.length;i++)
            {
                while(lines[i].startsWith("\n"))
                {
                    lines[i] = lines[i].replaceFirst("\n", "");
                }
                if(!lines[i].trim().isEmpty())
                {
                    chText+=lines[i]+"\n";

                    if(chText.split(System.getProperty("line.separator")).length >1)
                    {
                        break;
                    }
                }
            }
            mTextViewGeneralContent.setText(chText);
        }
    }
}


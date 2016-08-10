package com.leadplatform.kfarmers.view.evaluation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.leadplatform.kfarmers.Constants;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.model.json.snipe.OrderItemJson;
import com.leadplatform.kfarmers.util.ImageUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.common.ImageRotateActivity;
import com.leadplatform.kfarmers.view.diary.DiaryWriteDragListFragment;

import java.io.Serializable;
import java.util.ArrayList;

public class EvaluationActivity  extends BaseFragmentActivity {
    public static final String TAG = "EvaluationActivity";

    public enum type implements Serializable{
        evaluation,
        review
    }

    private type mNowType = type.evaluation;

    private ArrayList<String> imgPath;
    public TextView mActionBarTitle;

    @Override
    public void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_base);
        imgPath = new ArrayList<String>();

        if(getIntent() != null && getIntent().hasExtra("type")) {
            mNowType = (type) getIntent().getSerializableExtra("type");
        }

        if(mNowType == type.review) {
            ArrayList<OrderItemJson> orderItemJson = (ArrayList<OrderItemJson>) getIntent().getSerializableExtra("data");
            fragmentDetail(orderItemJson);
        } else {
            fragmentList();
        }
    }

    @Override
    public void initActionBar() {
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.view_actionbar);
        mActionBarTitle = (TextView) findViewById(R.id.actionbar_title_text_view);

        if(mNowType == type.review) {
            mActionBarTitle .setText(getString(R.string.review_title));
        } else {
            mActionBarTitle .setText(getString(R.string.evaluation_title));
        }
        initActionBarHomeBtn();
    }

    public void fragmentList() {
        EvaluationListFragment fragment = EvaluationListFragment.newInstance();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, EvaluationListFragment.TAG);
        ft.commit();
    }

    public void fragmentDetail(String place, String item) {
        EvaluationDetailFragment fragment = EvaluationDetailFragment.newInstance(place,item);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, EvaluationDetailFragment.TAG);
        ft.addToBackStack(EvaluationDetailFragment.TAG);
        ft.commit();
    }

    public void fragmentDetail(ArrayList<OrderItemJson> orderItemJson) {
        EvaluationDetailFragment fragment = EvaluationDetailFragment.newInstance(orderItemJson);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, EvaluationDetailFragment.TAG);
        ft.commit();
    }

    public void fragmentResult(String place, String item) {
        EvaluationResultFragment fragment = EvaluationResultFragment.newInstance(place,item);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.fragment_container, fragment, EvaluationResultFragment.TAG);
        ft.addToBackStack(EvaluationResultFragment.TAG);
        ft.commit();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle(R.string.context_menu_camera_title);
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.btn_camera_capture:
                imgPath.clear();
                imgPath.add(ImageUtil.takePictureFromCamera(this, Constants.REQUEST_TAKE_CAPTURE));

                return true;
            case R.id.btn_camera_gallery:
                galleryChoice(DiaryWriteDragListFragment.MAX_PICTURE_COUNT);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    public void galleryChoice(int maxCount)
    {
        int count = 0;
        FragmentManager fm = getSupportFragmentManager();
        EvaluationDetailFragment fragment = (EvaluationDetailFragment) fm.findFragmentByTag(EvaluationDetailFragment.TAG);
        if (fragment != null) {
            count = fragment.getImageSize();
        }
        ImageUtil.takeSelectFromGallery(mContext, maxCount,count,"","", Constants.REQUEST_GALLERY);
    }

    private void runImageRotateActivity(int takeType, ArrayList<String> imgPath) {
        Intent intent = new Intent(this, ImageRotateActivity.class);
        intent.putExtra("takeType", takeType);
        intent.putStringArrayListExtra("imagePath", imgPath);
        startActivityForResult(intent, Constants.REQUEST_ROTATE_PICTURE);
    }

    private void addPictureListView(ArrayList<String> imgPath) {
        FragmentManager fm = getSupportFragmentManager();
        EvaluationDetailFragment fragment = (EvaluationDetailFragment) fm.findFragmentByTag(EvaluationDetailFragment.TAG);
        if (fragment != null) {
            fragment.addImages(imgPath);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.REQUEST_TAKE_CAPTURE) {
                runImageRotateActivity(Constants.REQUEST_TAKE_CAPTURE, imgPath);
                return;
            } else if (requestCode == Constants.REQUEST_GALLERY) {
                imgPath = data.getStringArrayListExtra("imagePath");
                runImageRotateActivity(Constants.REQUEST_GALLERY, imgPath);
                return;
            } else if (requestCode == Constants.REQUEST_ROTATE_PICTURE) {
                imgPath = data.getStringArrayListExtra("imagePath");
                addPictureListView(imgPath);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

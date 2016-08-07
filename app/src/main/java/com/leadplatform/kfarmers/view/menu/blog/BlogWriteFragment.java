package com.leadplatform.kfarmers.view.menu.blog;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.DbController;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.database.UserDb;
import com.leadplatform.kfarmers.model.json.CategoryJson;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment.OnCloseCategoryDialogListener;

import java.util.ArrayList;
import java.util.Iterator;

public class BlogWriteFragment extends BaseFragment {
    public static final String TAG = "BlogWriteFragment";

    private RelativeLayout categoryLayout;
    private TextView categoryText;
    private int categoryIndex = 0;
    private RadioButton naverRadio, daumRadio, tistoryRadio;
    private EditText postEdit;
    //    private Button uploadBtn;
    private ArrayList<String> categoryList;
    private ArrayList<CategoryJson> categoryObjectList;

    public static BlogWriteFragment newInstance() {
        final BlogWriteFragment f = new BlogWriteFragment();

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_blog_write, container, false);

        categoryLayout = (RelativeLayout) v.findViewById(R.id.CategoryLayout);
        categoryText = (TextView) v.findViewById(R.id.CategoryText);
        naverRadio = (RadioButton) v.findViewById(R.id.post_naver);
        daumRadio = (RadioButton) v.findViewById(R.id.post_daum);
        tistoryRadio = (RadioButton) v.findViewById(R.id.post_tstory);
        postEdit = (EditText) v.findViewById(R.id.write_post_input);
//        uploadBtn = (Button) v.findViewById(R.id.post_upload);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        categoryLayout.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onCategoryBtnClicked();
            }
        });

//        uploadBtn.setOnClickListener(new ViewOnClickListener()
//        {
//            @Override
//            public void viewOnClick(View v)
//            {
//                onUploadBtnClicked();
//            }
//        });

        requestCategory();
        ((BlogActivity) getSherlockActivity()).displayBlogWriteActionBar();
    }

    @Override
    public void onDetach() {
        ((BlogActivity) getSherlockActivity()).displayBlogListActionBar();
        super.onDetach();
    }

    private void onCategoryBtnClicked() {
        DialogFragment fragment = DialogFragment.newInstance(
                0,
                categoryIndex,
                categoryList.toArray(new String[categoryList.size()]),
                "");
        fragment.setOnCloseCategoryDialogListener(new OnCloseCategoryDialogListener() {
            @Override
            public void onDialogSelected(int subMenuType, int position) {
                if (categoryIndex != position) {
                    categoryIndex = position;
                    categoryText.setText(categoryList.get(position));
                }
            }
        });
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, DialogFragment.TAG);
    }

    public void onUploadBtnClicked() {
        String category, blogtype = "N", address;

        if (categoryIndex == 0) {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_invalid_category);
            return;
        }

        if (TextUtils.isEmpty(postEdit.getText().toString())) {
            UiController.showDialog(getSherlockActivity(), R.string.dialog_invalid_category);
            return;
        }

        category = categoryObjectList.get(categoryIndex).SubIndex;
        address = postEdit.getText().toString();

        if (naverRadio.isChecked())
            blogtype = "N";
        else if (daumRadio.isChecked())
            blogtype = "D";
        else if (tistoryRadio.isChecked())
            blogtype = "T";

        UiController.hideSoftKeyboard(getSherlockActivity());
        CenterController.writePost(category, blogtype, address, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            Toast.makeText(getSherlockActivity(), R.string.toast_success_write_post, Toast.LENGTH_SHORT).show();
                            getSherlockActivity().getSupportFragmentManager().popBackStack();
                            break;

                        default:
                            UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }
        });
    }

    private void requestCategory() {
        if (categoryObjectList == null) {
            UserDb user = DbController.queryCurrentUser(getSherlockActivity());
            categoryList = new ArrayList<String>();
            categoryList.add("카테고리 선택");
            categoryObjectList = new ArrayList<CategoryJson>();
            categoryObjectList.add(new CategoryJson());

            CenterController.getCategory(user.getUserID(), new CenterResponseListener(getSherlockActivity()) {
                @Override
                public void onSuccess(int Code, String content) {
                    try {
                        switch (Code) {
                            case 0000:
                                JsonNode root = JsonUtil.parseTree(content);
                                if (root.findPath("List").isArray()) {
                                    Iterator<JsonNode> it = root.findPath("List").iterator();
                                    while (it.hasNext()) {
                                        CategoryJson category = (CategoryJson) JsonUtil.jsonToObject(it.next().toString(), CategoryJson.class);
                                        categoryList.add(category.SubName);
                                        categoryObjectList.add(category);
                                    }

                                    categoryIndex = 0;
                                    categoryText.setText(categoryList.get(0));
                                }
                                break;
                            case 1002:
                                UiController.showDialog(getSherlockActivity(), R.string.dialog_empty_release);
                                break;
                            default:
                                UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                                break;
                        }
                    } catch (Exception e) {
                        UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                    }
                }
            });
        }
    }
}

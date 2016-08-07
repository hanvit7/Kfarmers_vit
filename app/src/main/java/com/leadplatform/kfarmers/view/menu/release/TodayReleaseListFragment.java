package com.leadplatform.kfarmers.view.menu.release;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fasterxml.jackson.databind.JsonNode;
import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.controller.CenterController;
import com.leadplatform.kfarmers.controller.CenterResponseListener;
import com.leadplatform.kfarmers.controller.UiController;
import com.leadplatform.kfarmers.custom.button.ViewOnClickListener;
import com.leadplatform.kfarmers.model.holder.ReleaseTodayListHolder;
import com.leadplatform.kfarmers.model.json.ReleaseTodayCategoryJson;
import com.leadplatform.kfarmers.model.json.ReleaseTodayJson;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.util.CommonUtil.PatternUtil;
import com.leadplatform.kfarmers.util.JsonUtil;
import com.leadplatform.kfarmers.view.base.BaseFragmentActivity;
import com.leadplatform.kfarmers.view.base.BaseListFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment;
import com.leadplatform.kfarmers.view.common.DialogFragment.OnCloseCategoryDialogListener;
import com.leadplatform.kfarmers.view.farm.FarmActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.apache.http.Header;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class TodayReleaseListFragment extends BaseListFragment implements OnCloseCategoryDialogListener {
    public static final String TAG = "TodayReleaseListFragment";

    private RelativeLayout SubMenuBox;
    private TextView SubMenu, todayText;
    private int subMenuIndex = 0;
    private ArrayList<String> subMenuList;
    private ArrayList<ReleaseTodayCategoryJson> subMenuObjectList;

    private ArrayList<ReleaseTodayJson> releaseTodayObjectList;
    private ReleaseTodayListAdapter releaseTodayListAdapter;

    public static TodayReleaseListFragment newInstance() {
        final TodayReleaseListFragment f = new TodayReleaseListFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_today_release, container, false);

        SubMenuBox = (RelativeLayout) v.findViewById(R.id.SubMenuBox);
        SubMenu = (TextView) v.findViewById(R.id.SubMenu);
        todayText = (TextView) v.findViewById(R.id.todayText);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        todayText.setText(simpleTodayDateFormat());
        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        if (subMenuObjectList == null) {
            subMenuList = new ArrayList<String>();
            subMenuObjectList = new ArrayList<ReleaseTodayCategoryJson>();
            initReleaseTodayCategory();
        }

        if (releaseTodayListAdapter == null) {
            releaseTodayObjectList = new ArrayList<ReleaseTodayJson>();
            releaseTodayListAdapter = new ReleaseTodayListAdapter(getSherlockActivity(), R.layout.item_release_today, releaseTodayObjectList,
                    ((BaseFragmentActivity) getSherlockActivity()).imageLoader);
            setListAdapter(releaseTodayListAdapter);
            getListView().setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ReleaseTodayJson item = (ReleaseTodayJson) getListAdapter().getItem(position);
                    Intent intent = new Intent(getSherlockActivity(), FarmActivity.class);
                    intent.putExtra("userType", "F");
                    intent.putExtra("userIndex", item.FarmerIndex);
                    startActivity(intent);
                }
            });
        }

        SubMenuBox.setOnClickListener(new ViewOnClickListener() {
            @Override
            public void viewOnClick(View v) {
                onSubMenuBtnClicked();
            }
        });

    }

    private void onSubMenuBtnClicked() {
        DialogFragment fragment = DialogFragment.newInstance(
                0,
                subMenuIndex,
                subMenuList.toArray(new String[subMenuList.size()]),
                TodayReleaseListFragment.TAG);
        FragmentTransaction ft = getSherlockActivity().getSupportFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        fragment.show(ft, DialogFragment.TAG);
    }

    @Override
    public void onDialogSelected(int subMenuType, int position) {
        boolean updateFlag = false;

        if (subMenuIndex != position) {
            updateFlag = true;
            subMenuIndex = position;
            SubMenu.setText(subMenuList.get(position));
        }

        if (updateFlag) {
            getReleaseTodayList(makeListReleaseTodayData(subMenuIndex));
        }
    }

    private void initReleaseTodayCategory() {
        CenterController.getReleaseTodayCategory(new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.findPath("List").isArray()) {
                                int count = 0;
                                for (int categoryIndex = 1; categoryIndex < 7; categoryIndex++) {
                                    Iterator<JsonNode> it = root.findPath("List").iterator();
                                    while (it.hasNext()) {
                                        ReleaseTodayCategoryJson category = (ReleaseTodayCategoryJson) JsonUtil.jsonToObject(it.next().toString(), ReleaseTodayCategoryJson.class);
                                        if (Integer.valueOf(category.PrimaryIndex) == categoryIndex) {
                                            subMenuObjectList.add(category);
                                            subMenuList.add(category.PrimaryName + "(" + category.Count + ")");
                                            count += Integer.valueOf(category.Count);
                                            break;
                                        }
                                    }
                                }
                                ReleaseTodayCategoryJson allCategory = new ReleaseTodayCategoryJson();
                                allCategory.PrimaryIndex = "1,2,3,4,5,6";
                                subMenuObjectList.add(0, allCategory);
                                subMenuList.add(0, "전체" + "(" + count + ")");
                                SubMenu.setText(subMenuList.get(0));
                                getReleaseTodayList(makeListReleaseTodayData(0));
                            }
                            break;
                    }
                } catch (Exception e) {
                    UiController.showDialog(getSherlockActivity(), R.string.dialog_unknown_error);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] content, Throwable error) {
                super.onFailure(statusCode, headers, content, error);
            }
        });
    }

    private String makeListReleaseTodayData(int subMenuIndex) {
        String category = null;

        if (subMenuObjectList != null) {
            category = subMenuObjectList.get(subMenuIndex).PrimaryIndex;
        }

        return category;
    }

    private class ReleaseTodayListAdapter extends ArrayAdapter<ReleaseTodayJson> {
        private int itemLayoutResourceId;
        private ImageLoader imageLoader;
        private DisplayImageOptions options;

        public ReleaseTodayListAdapter(Context context, int itemLayoutResourceId, ArrayList<ReleaseTodayJson> items, ImageLoader imageLoader) {
            super(context, itemLayoutResourceId, items);
            this.itemLayoutResourceId = itemLayoutResourceId;
            this.imageLoader = imageLoader;
            this.options = new DisplayImageOptions.Builder().cacheOnDisc(true).imageScaleType(ImageScaleType.IN_SAMPLE_INT).bitmapConfig(Config.RGB_565)
                    .displayer(new RoundedBitmapDisplayer(CommonUtil.AndroidUtil.pixelFromDp(getSherlockActivity(), 40) / 2)).build();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ReleaseTodayListHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSherlockActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemLayoutResourceId, null);

                holder = new ReleaseTodayListHolder();
                holder.profileImageView = (ImageView) convertView.findViewById(R.id.diary_writing_profile_image_view);
                holder.farmText = (TextView) convertView.findViewById(R.id.farmText);
                holder.addressText = (TextView) convertView.findViewById(R.id.addressText);
                holder.categoryText = (TextView) convertView.findViewById(R.id.categoryText);

                convertView.setTag(holder);
            } else {
                holder = (ReleaseTodayListHolder) convertView.getTag();
            }

            ReleaseTodayJson item = getItem(position);

            if (item != null) {
                // holder.rootLayout.setTag(new String(item.Diary));
                // holder.rootLayout.setOnClickListener(new ViewOnClickListener()
                // {
                // @Override
                // public void viewOnClick(View v)
                // {
                // String diary = (String) v.getTag();
                // Intent intent = new Intent(getSherlockActivity(), DiaryDetailActivity.class);
                // intent.putExtra("diary", diary);
                // intent.putExtra("type", DiaryDetailActivity.DETAIL_DIARY);
                // startActivity(intent);
                // }
                // });

                if (!PatternUtil.isEmpty(item.ProfileImage)) {
                    imageLoader.displayImage(item.ProfileImage, holder.profileImageView, options);
                }

                if (!PatternUtil.isEmpty(item.Farm)) {
                    holder.farmText.setText(item.Farm);
                } else {
                    holder.farmText.setVisibility(View.INVISIBLE);
                }

                // if (!PatternUtil.isEmpty(item.AddressKeyword1) && !PatternUtil.isEmpty(item.AddressKeyword2))
                // {
                // holder.addressText.setText(item.AddressKeyword1 + " > " + item.AddressKeyword2);
                // }
                if (!PatternUtil.isEmpty(item.AddressKeyword2)) {
                    holder.addressText.setText(item.AddressKeyword2);
                } else {
                    holder.addressText.setVisibility(View.GONE);
                }

                if (!PatternUtil.isEmpty(item.CategoryName)) {
                    holder.categoryText.setText(item.CategoryName);
                } else {
                    holder.categoryText.setVisibility(View.INVISIBLE);
                }
            }

            return convertView;
        }
    }

    public void getReleaseTodayList(String category) {
        CenterController.getReleaseTodayList(category, new CenterResponseListener(getSherlockActivity()) {
            @Override
            public void onSuccess(int Code, String content) {
                try {
                    switch (Code) {
                        case 0000:
                            releaseTodayListAdapter.clear();
                            JsonNode root = JsonUtil.parseTree(content);
                            if (root.findPath("List").isArray()) {
                                Iterator<JsonNode> it = root.findPath("List").iterator();
                                while (it.hasNext()) {
                                    ReleaseTodayJson release = (ReleaseTodayJson) JsonUtil.jsonToObject(it.next().toString(), ReleaseTodayJson.class);
                                    releaseTodayObjectList.add(release);
                                }
                                releaseTodayListAdapter.notifyDataSetChanged();
                            }
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

    public static String simpleTodayDateFormat() {
        String sDate = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 현재");
        try {
            sDate = format.format(Calendar.getInstance().getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sDate;
    }
}

package com.leadplatform.kfarmers.view.common;

// import DialogFragment in Support library v4

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leadplatform.kfarmers.R;
import com.leadplatform.kfarmers.util.CommonUtil;
import com.leadplatform.kfarmers.view.base.BaseDialogFragment;

public class JoinDialogFragment extends BaseDialogFragment {

    static final int NUM_PAGES = 5;

    ViewPager pager;
    PagerAdapter pagerAdapter;
    LinearLayout circles;
    Button next;
    boolean isOpaque = true;

    public JoinDialogFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setCancelable(false);
        Window window = getDialog().getWindow();
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View v = inflater.inflate(R.layout.fragment_tutorial, container);
        //getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        circles = (LinearLayout) v.findViewById(R.id.circles);

        next = (Button) v.findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() + 1, true);
            }
        });

        pager = (ViewPager) v.findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter();
        pager.setAdapter(pagerAdapter);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                setIndicator(position);
                if (position == NUM_PAGES - 2) {
                    next.setText("완료");
                } else if (position < NUM_PAGES - 2) {
                    next.setText("다음");
                } else if (position == NUM_PAGES - 1) {
                    endTutorial();
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        buildCircles();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = (int)(getActivity().getWindowManager().getDefaultDisplay().getHeight()/1.5);
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(pager!=null){
        }
    }

    private void buildCircles(){

        float scale = getResources().getDisplayMetrics().density;
        int padding = (int) (5 * scale + 0.5f);

        for(int i = 0 ; i < NUM_PAGES - 1 ; i++){
            ImageView circle = new ImageView(getActivity());
            circle.setImageResource(R.drawable.button_farm_paging_on);
            circle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            circle.setAdjustViewBounds(true);
            circle.setPadding(padding, 0, padding, 0);
            circles.addView(circle);
        }

        setIndicator(0);
    }

    private void setIndicator(int index){
        if(index < NUM_PAGES){
            for(int i = 0 ; i < NUM_PAGES - 1 ; i++){
                ImageView circle = (ImageView) circles.getChildAt(i);
                if(i == index){
                    circle.setColorFilter(getResources().getColor(R.color.CommonText));
                }else {
                    circle.setColorFilter(getResources().getColor(android.R.color.transparent));
                }
            }
        }
    }

    private void endTutorial(){
        dismiss();
    }

    private class ScreenSlidePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View v = inflater.inflate(R.layout.view_join, container, false);
            TextView content = (TextView) v.findViewById(R.id.content);

            String str = "";
            switch (position) {
                case 0:
                    str = "K파머스는 실시간 생산정보기반 직거래서비스입니다.\n꾸준한 생산정보 등록이 핵심입니다.";
                    str = str.replace(" ", "\u00A0");
                    content.setText(str);
                    break;
                case 1:
                    str = "생산자 가입시\n생산자 프로필사진,농장소개,농장주소,연락처가 정확히 등록되지않으면 서비스이용이 불가합니다.";
                    str = str.replace(" ", "\u00A0");
                    content.setText(str);
                    break;
                case 2:
                    str = "생산정보 등록시\n순수한 생산정보가 아닌 상품홍보,효능효과 관련정보가 등록될 경우통보없이 삭제조치합니다.\n※지속적으로 등록시 계정이 차단됩니다.";
                    str = str.replace(" ", "\u00A0");
                    content.setText(str);
                    break;
                case 3:
                    str = "모든 것을 내려놓을 때\n진짜 이야기가 시작됩니다!\n준비되셨나요?";
                    str = str.replace(" ", "\u00A0");
                    content.setText(str);
                    break;
            }
            container.addView(v, container.getChildCount() > position ? position : container.getChildCount());
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
            CommonUtil.UiUtil.unbindDrawables((LinearLayout) object);
            object = null;
        }
    }
}


package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductJson implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String idx;

    public String id;

    public String code;

    public String name;

    public String price;

    public String dcprice;

    public String stock;

    public String soldout;

    public String count;

    public String delivery_price;

    public String free_delivery;

    public String display; // 노출

    public String use_flag; // 사용

    public String recommend; // 추천

    public String notification_type;

    public String image1;

    public String image2;

    public String image3;

    public String image4;

    public String image5;

    public String image6;

    public String image7;

    public String image8;

    public String image9;

    public String image10;

    public String datetime;

    public String category_name;

    public String profile_image;

    public String delivery;

    public String exchange;

    public String category;

    public ArrayList<Categorys> categorys;

    public String farm_name;

    public String member_idx;

    public String contents;

    public String buyprice;

    public String video_url;

    public String display_text;

    public String option;

    public String summary;

    // 상품 검증
    public String verification;

    public String phone;

    public String bank_account;

    public String rating;

    public String rating_count;

    public String unique;


    public String provide_point;

    public String duration;

    public String link_url;


    static public class Categorys implements Serializable {
        public String idx;
        public String category_name;
    }

}



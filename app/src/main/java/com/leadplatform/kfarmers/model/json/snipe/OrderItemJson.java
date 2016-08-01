package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderItemJson implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    // 메인 정보
    public String unique;

    // 옵션 정보
    public String idx;
    public String id;
    public String item_idx;
    public String price;
    public String buy_direct;
    public String use_flag;
    public String datetime;
    public String image1;
    public String name;
    public String cnt;
    public String option_name;
    public String delivery_price;
    public String delivery_comment;
    public String delivery_company;
    public String delivery_code;
    public String delivery_url;
    public String delivery_use;
    public String status;
    public String code;
    public String unique_individual;
    public String reivews;
    public String provide_point;


    public ArrayList<OrderItemJson> option;
}


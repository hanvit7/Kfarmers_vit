package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class CartItemJson implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String idx;
    public String item_idx;
    public String cart_idx;
    public String option_idx;
    public String option_name;
    public String option_price;
    public String option_dcprice;
    public String option_use_flag;
    public String current_price;
    public String current_dcprice;
    public String current_buyprice;
	public String name;
	public String image1;
    //public String price;
    public String buyprice;
    public String provide_point;
    public String cnt;
    public String soldout;
    public String stock;
    public String free_delivery;
    public String delivery_price;
    public String display;
    public String use_flag;
    public Boolean isCheck = true;
    public ArrayList<CartItemJson> option;
}


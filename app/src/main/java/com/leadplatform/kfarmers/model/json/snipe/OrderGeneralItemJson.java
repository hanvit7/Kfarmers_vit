package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;

public class OrderGeneralItemJson implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    public String idx;
    public String unique;
    public String unique_key;
    public String image1;
    public String name;
    public String price;
    public String cnt;
    public String delivery_comment;
    public String delivery_company;
    public String delivery_code;
    public String datetime;
    public String send_name;
    public String send_email;
    public String send_hp;
    public String receive_name;
    public String receive_hp;
    public String receive_addr;
    public String payment_type;
    public String deposit_name;
    public String bank_account;
    public Status status_text;

    static public class Status {
        public String code;
        public String msg;
    }



}


package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class OrderDetailJson implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Info mInfo;
    public Payment mPayment;
    public Refund mRefund;
    public Receiver mReceiver;
    public ArrayList<OrderItemJson> mItemArrayList;

    static public class Info {
        public String point;
    }

    static public class Payment {
        public String payment_type;
        public String bank_name; // 은행명
        public String bank_account; // 계좌번호
        public String bank_price; // 입금액
        public String bank_depositor; // 예금주
        public String deposit_name; // 입금자
        public String price;
    }

    static public class Refund {
        public String price;
        public String point;
    }

    static public class Receiver {
        public String receive_name;
        public String receive_tel;
        public String receive_hp;
        public String receive_zipcode;
        public String receive_addr;
        public String receive_addr_new;
        public String receive_email;
        public String delevery_massage;
    }


}


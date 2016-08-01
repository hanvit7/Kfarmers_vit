package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class EvaluationListJson implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;


    public String idx;
    public String place;
    public String location;
    public String explain;
    public String use_flag;
    public String datetime;

    public ArrayList<Item> item;
    public ArrayList<File> file;

    static public class Item {
        public String idx;
        public String place_idx;
        public String subject;
        public String status;
        public String codename;
        public String use_flag;
        public String datetime;
        public String status_text;
    }

    static public class File {
        public String path;
    }
}


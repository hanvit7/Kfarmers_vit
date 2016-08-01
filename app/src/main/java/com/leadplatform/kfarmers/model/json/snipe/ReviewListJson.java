package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class ReviewListJson implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;


    public String idx;
    public String id;
    public String code;
    public String rating;
    public String comment;
    public String division;
    public String use_flag;
    public String datetime;
    public String item_name;
    public String member_name;
    public String division_text;
    public String use_flag_text;
    public String member_profile_image;
    public String comments_count;
    public String unique_individual;
    public String item_idx;

    public ProductJson prodcut;

    public NewReplyListJson comments;

    public ArrayList<File> file;

    public ArrayList<String> file_delete = new ArrayList<>();

    static public class File implements Serializable {
        public String idx;
        public String path;
    }
}


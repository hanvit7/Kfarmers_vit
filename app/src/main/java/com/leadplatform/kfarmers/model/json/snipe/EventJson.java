package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class EventJson implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String idx;
    public String title;
    public String sub_title;
    public String start_date;
    public String end_date;
    public String info;
    public String content_title;
    public String content;
    public String prizewinner;
    public String link;
    public String use_flag;
    public String datetime;
    public String status;
    public String duration;
    //public String participant;
    public String product;
    public String farm;
    public String hashtag;
    public String point;

    public ImagePath file;

    static public class ImagePath {
        public Path title;
        public ArrayList<Path> image;
        public Path prizewinner;

        static public class Path {
            public String path;
        }
    }
}


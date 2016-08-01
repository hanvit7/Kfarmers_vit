package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;
import java.util.ArrayList;

public class RecipeJson implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String idx;
    public String title;
    public String explain;
    public String cooking;
    public String number;
    public String material;
    public String use_flag;
    public String datetime;
    public String path;
    public String recommend_count;
    public String comment_count;
    public String recommend_used;
    public String picture;
    public String land_picture;

    public ArrayList<Recipe> recipe;

    public ArrayList<ProductJson> product;

    static public class Recipe implements Serializable {
        public String idx;
        public String path;
        public String content;
    }


}


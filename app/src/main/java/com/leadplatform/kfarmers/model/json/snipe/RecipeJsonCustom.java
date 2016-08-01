package com.leadplatform.kfarmers.model.json.snipe;

import java.io.Serializable;

public class RecipeJsonCustom implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public enum type {
        title,
        material,
        recipe
    }

    public type nType;
    public String idx;
    public String title;
    public String explain;
    public String cooking;
    public String number;
    public String material;
    public String path;
    public String content;
    public String no;
}


package com.leadplatform.kfarmers.model.json;

import com.leadplatform.kfarmers.model.json.snipe.ProductJson;

import java.io.Serializable;
import java.util.ArrayList;

public class TodayTagJson implements Serializable {

    private static final long serialVersionUID = 1L;

	public String Idx;
	public String Keyword;
	public String Explain;
	public String DateTime;
	public String Picture;
	public ArrayList<DiaryList> DiaryList;
	public ArrayList<ProductJson> ProductList;

	public static class DiaryList implements Serializable {

		public String Index;
		public String Image;
	}
}

package com.leadplatform.kfarmers.model.json;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductItemJson implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public String category;
	public Product product;
	public ArrayList<Option> option;

	public ArrayList<String> images;
	public ArrayList<String> images_delete;


	public class Product
	{
		String idx;
		String category;
		String name;
		String price;
		String dcprice;
		String content;
		String option;
	}

	public class Option
	{
		String option_idx;
		String option_name;
		String option_price;
		String option_dcprice;
		String option_display;
	}
}

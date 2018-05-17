package com.example.android.lab1.ui.searchbooks;

public abstract class FiltersValues {

    public static int SEARCH_BY_TITLE = 0;
    public static int SEARCH_BY_AUTHOR = 1;
    public static int SEARCH_BY_PUBLISHER = 2;
    public static int SEARCH_BY_TAGS = 3;

    public static int RATING_FILTER = 0;
    public static int POSITION_FILTER = 1;

    public static int MAX_POSITION = 290;

    public static int ORDER_BY_DATE = 0;
    public static int ORDER_BY_RATING = 1;
    public static int ORDER_BY_CONDITION = 2;
    public static int ORDER_BY_POSITION = 3;

    public static String FILTER_BUNDLE = "filter_bundle";
    public static String SEARCH_BY = "search_by";
    public static String SEEK_BARS = "seek_bars";
    public static String ORDER_BY = "order_by";
}

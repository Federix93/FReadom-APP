package com.example.android.lab1.ui.searchbooks;

public class FilterObject {

    private boolean searchByFilters[];
    private int seekBarFilters[];
    private boolean orderByFilters[];

    public FilterObject(boolean searchBy[], int seekBars[], boolean orderBy[])
    {
        searchByFilters = searchBy.clone();
        seekBarFilters = seekBars.clone();
        orderByFilters = orderBy.clone();
    }

    public boolean[] getSearchByFilters() {
        return searchByFilters;
    }

    public int[] getSeekBarFilters() {
        return seekBarFilters;
    }

    public boolean[] getOrderByFilters() {
        return orderByFilters;
    }
}

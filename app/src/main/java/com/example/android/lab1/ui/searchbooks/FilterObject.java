package com.example.android.lab1.ui.searchbooks;

public class FilterObject {

    private boolean searchByFilters[];
    private int seekBarFilters[];

    public FilterObject(boolean searchBy[], int seekBars[])
    {
        searchByFilters = searchBy.clone();
        seekBarFilters = seekBars.clone();
    }

    public boolean[] getSearchByFilters() {
        return searchByFilters;
    }

    public int[] getSeekBarFilters() {
        return seekBarFilters;
    }

}

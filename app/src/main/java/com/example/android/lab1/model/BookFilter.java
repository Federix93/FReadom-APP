package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class BookFilter
{
    /*
    * Class used to filter a collection of book based on some params
    * FOR NOW JUST FILTERS GENDERS
     */

    private List<Integer> mAllowedGenders;

    private List<Integer> getGenders()
    {
        return mAllowedGenders;
    }

    private void setGenders(ArrayList<Integer> allowedGenders)
    {
        mAllowedGenders = allowedGenders;
    }

    public static BookFilter buildGenderFilter(ArrayList<Integer> allowedGenders)
    {
        BookFilter b = new BookFilter();
        b.setGenders(allowedGenders);
        return b;
    }

    public List<String> getFilteredIds(List<String> ids, List<Book> books){
        if (mAllowedGenders != null && ! mAllowedGenders.isEmpty()) {
            List<String> filteredIds = new ArrayList<>();
            if (books.size() < ids.size()) throw new IndexOutOfBoundsException();
            for (int i = 0; i < ids.size(); i++) {
                if (!mAllowedGenders.contains(books.get(i).getGenre()))
                    filteredIds.add(ids.get(i));
            }
            return filteredIds;
        }
        return new ArrayList<>();
    }



}

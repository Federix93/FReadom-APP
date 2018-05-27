package com.example.android.lab1.model;

import java.util.ArrayList;
import java.util.List;

public class FavoriteBooks {

    public FavoriteBooks() {}
    private List<String> mBookIds;

    public FavoriteBooks (List<String> favoriteBooks) { this.mBookIds = favoriteBooks; }

    public List<String> getBookIds() {
        return mBookIds;
    }

    public void setBookIds(ArrayList<String> bookIds) {
        this.mBookIds = bookIds;
    }




}

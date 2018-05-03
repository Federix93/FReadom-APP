package com.example.android.lab1.ui.searchbooks;

import android.os.Parcel;

public class SearchSuggestion implements com.arlib.floatingsearchview.suggestions.model.SearchSuggestion {

    private String mSuggestion;
    private boolean mIsHistory = false;

    public SearchSuggestion(String suggestion)
    {
        mSuggestion = suggestion.toLowerCase();
    }

    public SearchSuggestion(Parcel source) {
        mSuggestion = source.readString();
        mIsHistory = source.readInt() != 0;
    }

    public void setIsHistory(boolean isHistory) {
        mIsHistory = isHistory;
    }

    public boolean getIsHistory() {
        return mIsHistory;
    }

    @Override
    public String getBody() {
        return mSuggestion;
    }

    public static final Creator<SearchSuggestion> CREATOR = new Creator<SearchSuggestion>() {
        @Override
        public SearchSuggestion createFromParcel(Parcel in) {
            return new SearchSuggestion(in);
        }

        @Override
        public SearchSuggestion[] newArray(int size) {
            return new SearchSuggestion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSuggestion);
        dest.writeInt(mIsHistory ? 1 : 0);
    }
}

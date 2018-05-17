package com.example.android.lab1.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.ui.searchbooks.BookSearchItem;
import com.example.android.lab1.utils.Utilities;

import java.util.ArrayList;

public class RecyclerSearchAdapter extends RecyclerView.Adapter<RecyclerSearchAdapter.ResultViewHolder> {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_USERS_INDEX_NAME = "users";

    private ArrayList<BookSearchItem> mBookDataSet;
    private double mCurrentLat, mCurrentLong;

    public RecyclerSearchAdapter(ArrayList<BookSearchItem> bookDataSet, double currentLat, double currentLong) {
        mBookDataSet = new ArrayList<>(bookDataSet);
        mCurrentLat = currentLat;
        mCurrentLong = currentLong;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.book_search_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        holder.bind(mBookDataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return mBookDataSet.size();
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mTitle, mAuthor, mRating, mConditionsText, mBookDistance;
        ImageView mThumbnail, mUserPicture, mConditionsImage;
        String mBookId;

        public ResultViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.search_book_title);
            mAuthor = itemView.findViewById(R.id.search_book_author);
            mThumbnail = itemView.findViewById(R.id.search_book_thumbnail);
            mUserPicture = itemView.findViewById(R.id.search_book_user_picture);
            mBookDistance = itemView.findViewById(R.id.search_book_distance);
            mRating = itemView.findViewById(R.id.search_book_user_rating);
            mConditionsText = itemView.findViewById(R.id.search_book_conditions_text);
            mConditionsImage = itemView.findViewById(R.id.search_book_conditions_image);

            itemView.setOnClickListener(this);
        }

        void bind(BookSearchItem book) {
            mTitle.setText(book.getTitle());
            mAuthor.setText(book.getAuthors());

            mBookId = book.getBookID();

            if (book.getWebThumbnail().isEmpty())
                Glide.with(itemView.getContext()).load(R.drawable.book_placeholder_thumbnail).into(mThumbnail);
            else
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mThumbnail);

            if (!book.getUserImage().isEmpty())
                Glide.with(itemView.getContext()).load(book.getUserImage()).apply(RequestOptions.circleCropTransform()).into(mUserPicture);
            else {
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_person_black_24dp)).apply(RequestOptions.circleCropTransform()).into(mUserPicture);
            }
            mRating.setText(String.format(itemView.getContext().getResources().getConfiguration().locale, "%.1f", book.getUserRating()));

            if (mCurrentLat != 0 && mCurrentLong != 0) {
                double distance = Utilities.distanceBetweenGeoPoints(book.getGeoPoint().getLatitude(), book.getGeoPoint().getLongitude(),
                        mCurrentLat, mCurrentLong, 'K');
                mBookDistance.setText(String.format(itemView.getResources().getString(R.string.search_book_distance), distance));

            } else {
                mBookDistance.setVisibility(View.GONE);
            }

            int condition = book.getCondition();
            mConditionsText.setText(Condition.getCondition(itemView.getContext(), condition));
            mConditionsImage.setColorFilter(Condition.getConditionColor(itemView.getContext(), condition), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("ID_BOOK_SELECTED", mBookId);
            v.getContext().startActivity(intent);
        }
    }


}

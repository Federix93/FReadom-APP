package com.example.android.lab1.adapter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Condition;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RecyclerSearchAdapter extends RecyclerView.Adapter<RecyclerSearchAdapter.ResultViewHolder> {

    private final static String ALGOLIA_APP_ID = "2TZTD61TRP";
    private final static String ALGOLIA_SEARCH_API_KEY = "e78db865fd37a6880ec1c3f6ccef046a";
    private final static String ALGOLIA_USERS_INDEX_NAME = "users";

    private JSONArray mSearchResults;
    private double mCurrentLat, mCurrentLong;
    private Client mClient;
    private Index mIndex;

    public RecyclerSearchAdapter(JSONArray results, double currentLat, double currentLong)
    {
        mSearchResults = results;
        mCurrentLat = currentLat;
        mCurrentLong = currentLong;

        mClient = new Client(ALGOLIA_APP_ID, ALGOLIA_SEARCH_API_KEY);
        mIndex = mClient.getIndex(ALGOLIA_USERS_INDEX_NAME);
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
        holder.bind(mSearchResults.optJSONObject(position));
    }

    @Override
    public int getItemCount() {
        return mSearchResults.length();
    }

    public class ResultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

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

        void bind(JSONObject book) {
            mTitle.setText(book.optString("title"));
            mAuthor.setText(book.optString("author"));

            mBookId = book.optString("objectID");

            if(book.optString("thumbnail").isEmpty())
                Glide.with(itemView.getContext()).load(R.drawable.book_placeholder_thumbnail).into(mThumbnail);
            else
                Glide.with(itemView.getContext()).load(book.optString("thumbnail")).into(mThumbnail);

            mIndex.getObjectAsync(book.optString("uid"), new CompletionHandler() {
                @Override
                public void requestCompleted(JSONObject userResult, AlgoliaException e) {

                    Glide.with(itemView.getContext()).load(userResult.optString("image")).apply(RequestOptions.circleCropTransform()).into(mUserPicture);
                    mRating.setText(String.format(itemView.getContext().getResources().getConfiguration().locale, "%.1f", userResult.optDouble("rating")));

                }
            });

            int condition = book.optInt("conditions");
            if(book.optJSONObject("_geoloc") != null)
            {
                double distance = 0;
                JSONObject geoLocation = book.optJSONObject("_geoloc");
                try {
                    distance = Utilities.distanceBetweenGeoPoints(geoLocation.getDouble("lat"), geoLocation.getDouble("lng"),
                            mCurrentLat, mCurrentLong, 'K');
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mBookDistance.setText(String.format("A %.1f km da te", distance));
            }

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

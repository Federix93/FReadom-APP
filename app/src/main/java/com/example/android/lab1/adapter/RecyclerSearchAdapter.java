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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.android.lab1.R;

import com.example.android.lab1.model.Condition;
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.example.android.lab1.utils.Utilities;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RecyclerSearchAdapter extends RecyclerView.Adapter<RecyclerSearchAdapter.ResultViewHolder> {

    private JSONArray mSearchResults;
    private HashMap<String, User> mUserHashMap;
    private double mCurrentLat, mCurrentLong;

    public RecyclerSearchAdapter(JSONArray results, HashMap<String, User> userHashMap, double currentLat, double currentLong)
    {
        mSearchResults = results;
        mUserHashMap = new HashMap<>(userHashMap);
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
            if(book.optString("thumbnail").isEmpty())
                Glide.with(itemView.getContext()).load(R.drawable.book_placeholder_thumbnail).into(mThumbnail);
            else
                Glide.with(itemView.getContext()).load(book.optString("thumbnail")).into(mThumbnail);

            if(!book.optString("uid").isEmpty()) {

                User bookUser = mUserHashMap.get(book.optString("uid"));
                if(bookUser.getImage() != null)
                    Glide.with(itemView.getContext()).load(bookUser.getImage()).apply(RequestOptions.circleCropTransform()).into(mUserPicture);
                mRating.setText(String.format(itemView.getContext().getResources().getConfiguration().locale, "%.1f", bookUser.getRating()));
            }

            int condition = book.optInt("conditions");
            Log.d("WIDO", "book: "+book);
            Log.d("WIDO", "geoloc: "+book.optJSONObject("_geoloc"));
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

            mBookId = book.optString("objectID");
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

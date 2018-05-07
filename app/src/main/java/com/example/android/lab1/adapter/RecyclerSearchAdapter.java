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
import com.example.android.lab1.model.User;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

public class RecyclerSearchAdapter extends RecyclerView.Adapter<RecyclerSearchAdapter.ResultViewHolder> {

    private JSONArray mSearchResults;
    private FirebaseFirestore mFirebaseFirestore;

    public RecyclerSearchAdapter(JSONArray results)
    {
        mSearchResults = results;
        mFirebaseFirestore =  FirebaseFirestore.getInstance();
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

        TextView mTitle, mAuthor, mRating, mConditionsText;
        ImageView mThumbnail, mUserPicture, mConditionsImage;
        String mBookId;

        public ResultViewHolder(View itemView) {
            super(itemView);

            mTitle = itemView.findViewById(R.id.search_book_title);
            mAuthor = itemView.findViewById(R.id.search_book_author);
            mThumbnail = itemView.findViewById(R.id.search_book_thumbnail);
            mUserPicture = itemView.findViewById(R.id.search_book_user_picture);
            mRating = itemView.findViewById(R.id.search_book_user_rating);
            mConditionsText = itemView.findViewById(R.id.search_book_conditions_text);
            mConditionsImage = itemView.findViewById(R.id.search_book_conditions_image);

            itemView.setOnClickListener(this);
        }

        void bind(JSONObject book)
        {
            mTitle.setText(book.optString("title"));
            mAuthor.setText(book.optString("author"));
            if(book.optString("thumbnail").isEmpty())
                Glide.with(itemView.getContext()).load(R.drawable.book_placeholder_thumbnail).into(mThumbnail);
            else
                Glide.with(itemView.getContext()).load(book.optString("thumbnail")).into(mThumbnail);

            if(!book.optString("uid").isEmpty()) {

                DocumentReference mDocRef = mFirebaseFirestore.collection("users").document(book.optString("uid"));
                mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        User mUser = documentSnapshot.toObject(User.class);
                        Glide.with(itemView.getContext()).load(mUser.getImage()).apply(RequestOptions.circleCropTransform()).into(mUserPicture);
                        mRating.setText(String.format(itemView.getContext().getResources().getConfiguration().locale, "%.1f", mUser.getRating()));
                    }
                });
            }

            int condition = book.optInt("conditions");

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

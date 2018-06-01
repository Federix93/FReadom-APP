package com.example.android.lab1.adapter;

import android.content.Context;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.lab1.R;
import com.example.android.lab1.model.Book;
import com.example.android.lab1.ui.BookDetailsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ProfileBookAdapter extends RecyclerView.Adapter<ProfileBookAdapter.MyViewHolder> {

    private List<Book> mBooks;
    private List<String> mBookIds;
    Context mContext;
    private GeoCodingTask mCurrentlyExecuting;
    private Location mCurrentlyResolving;
    private Location mResolveLater;

    public ProfileBookAdapter (List<Book> books, List<String> mBookIds) {
        this.mBooks = books;
        this.mBookIds = mBookIds;
    }
    public  ProfileBookAdapter (List<Book> books) {
        this.mBooks = books;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View cardView = inflater.inflate(R.layout.recycler_book_item_global_profile, parent, false);

        return new MyViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(mBooks.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView mBookTitle;
        TextView mBookEditor;
        TextView mBookCity;
        ImageView mBookThumbnail;

        public MyViewHolder (View itemView) {
            super (itemView);
            mBookTitle = itemView.findViewById(R.id.rv_book_title);
            mBookEditor = itemView.findViewById(R.id.rv_book_editor);
            mBookCity = itemView.findViewById(R.id.rv_book_city);
            mBookThumbnail = itemView.findViewById(R.id.rv_book_thumbnail);
        }
        public void bind(Book book, final int position){

            mBookTitle.setText(book.getTitle());
            mBookTitle.setTextColor(mContext.getResources().getColor(R.color.black));
            mBookEditor.setText(book.getPublisher());
            if (book.getGeoPoint() != null) {
                Location location = new Location("location");
                location.setLongitude(book.getGeoPoint().getLongitude());
                location.setLatitude(book.getGeoPoint().getLatitude());

                resolveCityLocation(location);
            }
            else
                mBookCity.setText(mContext.getResources().getString(R.string.position_not_available));
            if (book.getWebThumbnail() != null) {
                Glide.with(itemView.getContext()).load(book.getWebThumbnail()).into(mBookThumbnail);
            } else if (book.getUserBookPhotosStoragePath() != null && book.getUserBookPhotosStoragePath().size() > 0) {
                StorageReference storage = FirebaseStorage.getInstance().getReferenceFromUrl(book.getUserBookPhotosStoragePath().get(0));
                Glide.with(itemView.getContext()).load(storage).into(mBookThumbnail);
            } else
                Glide.with(itemView.getContext()).load(itemView.getResources().getDrawable(R.drawable.ic_no_book_photo)).into(mBookThumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), BookDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    intent.putExtra("BookSelected", mBooks.get(position));
                    v.getContext().startActivity(intent);
                }
            });

        }

        private void resolveCityLocation(Location location) {
            // This method will change address mResultAddress var
            mCurrentlyResolving = location;
            mCurrentlyExecuting = new GeoCodingTask(mBookCity);
            mCurrentlyExecuting.execute(new LatLng(location.getLatitude(),
                    location.getLongitude()));
        }

    }
    private class GeoCodingTask extends AsyncTask<LatLng, String, String> {

        private TextView mTarget;

        public GeoCodingTask(TextView target) {
            this.mTarget = target;
        }

        @Override
        protected String doInBackground(LatLng... latLngs) {
            if (latLngs.length > 0) {
                Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
                List<android.location.Address> fromLocation = null;
                try {
                    if (!isCancelled())
                        fromLocation = geocoder.getFromLocation(latLngs[0].latitude,
                                latLngs[0].longitude,
                                1);
                    else
                        return null;

                } catch (IOException e) {
                    return null;
                }
                if (fromLocation == null || fromLocation.size() == 0)
                    return null;
                else {
                    return fromLocation.get(0).getLocality();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String city) {
            if (mTarget != null && !this.isCancelled()) {
                if (city == null) {
                    mTarget.setText(R.string.no_address_found);
                } else {
                    mTarget.setText(city);
                    mCurrentlyExecuting = null;
                    mCurrentlyResolving = null;
                    mResolveLater = null;
                }
            }

        }
    }
}

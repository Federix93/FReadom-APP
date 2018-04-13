package com.example.android.lab1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.util.ArrayList;

public class ImagePagerAdapter extends PagerAdapter
{
    Context mContext;
    LayoutInflater mInflater;
    ArrayList<String> mPhotosPath;

    public ImagePagerAdapter(Context context, ArrayList<String> imagesPath)
    {
        mContext = context;
        mPhotosPath = imagesPath;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return mPhotosPath != null? mPhotosPath.size() : 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View itemView = mInflater.inflate(R.layout.gallery_pager_item, container, false);
        ImageView imageView = itemView.findViewById(R.id.user_book_gallery_photo);

        if (mPhotosPath.size() > 0) {
            File sd = Environment.getExternalStorageDirectory();
            File image = new File(sd + mPhotosPath.get(position));
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
            bitmap = Bitmap.createScaledBitmap(bitmap, container.getWidth(), container.getHeight(), true);
            imageView.setImageBitmap(bitmap);
        }
        else
        {
            // set it to a place holder

        }
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    public void addPhoto(String absolutePath) {
        mPhotosPath.add(absolutePath);
    }
}

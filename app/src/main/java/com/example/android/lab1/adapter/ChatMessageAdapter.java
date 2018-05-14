package com.example.android.lab1.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.lab1.R;
import com.example.android.lab1.model.chatmodels.Message;
import com.example.android.lab1.utils.glideimageloader.GlideApp;

import java.util.List;

public class ChatMessageAdapter extends ArrayAdapter<Message> {


    public ChatMessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.message_chat_item, parent, false);
        }

        TextView messageTextView = convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = convertView.findViewById(R.id.nameTextView);
        ImageView photoImageView = convertView.findViewById(R.id.photoPickerImageView);

        Message message = getItem(position);

        if (message != null) {
            if(message.getPhotoURL() != null){
                messageTextView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.VISIBLE);
                GlideApp.with(photoImageView.getContext()).load(message.getPhotoURL()).into(photoImageView);
                authorTextView.setText(message.getUsername());
            }else{
                messageTextView.setVisibility(View.VISIBLE);
                photoImageView.setVisibility(View.GONE);
                authorTextView.setText(message.getUsername());
                messageTextView.setText(message.getTextMessage());
            }
        }
        return convertView;
    }
}




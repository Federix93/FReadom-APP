package com.example.android.lab1.ui.chat;

public class CurrentOpenChat {

    private static String mOpenChatID = null;

    public static String getOpenChatID() {
        return mOpenChatID;
    }

    public static void setOpenChatID(String openChatID) {
        CurrentOpenChat.mOpenChatID = openChatID;
    }
}